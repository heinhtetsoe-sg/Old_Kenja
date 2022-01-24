// kanji=漢字
/*
 * $Id: 8f468955df84d72c0eb899d4ef97f6be4c3362c8 $
 *
 * 作成日: 2011/12/05 13:27:40 - JST
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2011 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJE;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 * <<クラスの説明>>。
 * @author nakamoto
 * @version $Id: 8f468955df84d72c0eb899d4ef97f6be4c3362c8 $
 */
public class KNJE151E {

    private static final Log log = LogFactory.getLog("KNJE151E.class");

    private boolean _hasData;

    Param _param;

    /**
     * @param request リクエスト
     * @param response レスポンス
     */
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {
        
        final Vrw32alp svf = new Vrw32alp();
        DB2UDB db2 = null;
        try {
            init(response, svf);

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);

            _hasData = false;

            printMain(db2, svf);

            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            closeDb(db2);
            svf.VrQuit();
        }

    }

    private void init(
            final HttpServletResponse response,
            final Vrw32alp svf
    ) throws IOException {
        response.setContentType("application/pdf");

        svf.VrInit();
        svf.VrSetSpoolFileStream(response.getOutputStream());
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        svf.VrSetForm("KNJE151E.frm", 1);

        int k = 0;
        String s = "0";
        final List jvsList = getJviewStatList(db2);
        for (final Iterator jvsIt = jvsList.iterator(); jvsIt.hasNext();) {
            final JviewStat jvs = (JviewStat) jvsIt.next();

            if (!s.equals("0") && !s.equals(jvs._schregNo)) {
                k++;
            }
            if (49 < k) {
                printHeader(svf);
                if (!"".equals(_param._viewCdIn)) {
                    printJviewname(svf);
                }
                svf.VrEndPage();
                k = 0;
            }
            printExam(svf, jvs, k);
            s = jvs._schregNo;
            _hasData = true;
        }
        if (_hasData) {
            printHeader(svf);
            if (!"".equals(_param._viewCdIn)) {
                printJviewname(svf);
            }
            svf.VrEndPage();
        }
    }

    private void printHeader(final Vrw32alp svf) {
        svf.VrsOut("NENDO", _param.getNendo());
        svf.VrsOut("DATE", _param.getLoginDateString() + _param.getTimeString());
        svf.VrsOut("CLASS", _param._className);
        svf.VrsOut("CHAIRTITLE", "クラス");
        svf.VrsOut("CHAIRNAME", _param._hrName);
    }

    private void printExam(final Vrw32alp svf, final JviewStat jvs, final int i) {
        svf.VrsOutn("HR_NAME", i + 1, jvs._hrName + "-" + jvs._attendNo);
        svf.VrsOutn("NAME", i + 1, jvs._schregNo + "　" + jvs._name);
        //観点状況および評定
        if (jvs._viewCd != null && !"".equals(_param._viewCdIn)) {
            if (_param._jviewNameMap.containsKey(jvs._viewCd)) {
                final JviewName jvn = (JviewName) _param._jviewNameMap.get(jvs._viewCd);
                svf.VrsOutn("STATUS" + jvn._no, i + 1, jvs._status);
            }
        }
    }

    private void printJviewname(final Vrw32alp svf) {
        int j1 = 1;
        for (final Iterator jvnIt = _param._jviewNameMap.keySet().iterator(); jvnIt.hasNext();) {
            final String viewCd = (String) jvnIt.next();
            final JviewName jvn = (JviewName) _param._jviewNameMap.get(viewCd);

            if ("9".equals(jvn._no)) continue;
            int i1 = Integer.parseInt(jvn._no) - 1;

            svf.VrsOut("VIEWCD1_" + jvn._no, jvn._no);

            String viewname = jvn._viewName;
            int mojisuu = viewname.length();
            for (int k = 0; k < 7; k++) {
                int s = 10 * k;
                int e = 10 * (k + 1);
                if (0 < k) {
                    j1++;
                    svf.VrsOutn("VIEWCD2", i1 + j1, "");
                } else {
                    svf.VrsOutn("VIEWCD2", i1 + j1, jvn._no);
                }
                if (e < mojisuu) {
                    svf.VrsOutn("VIEWNAME", i1 + j1, viewname.substring(s, e));
                } else {
                    svf.VrsOutn("VIEWNAME", i1 + j1, viewname.substring(s));
                    break;
                }
            }
        }
    }

    private List getJviewStatList(final DB2UDB db2) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        final List rtn = new ArrayList();
        try {
            final String sql = getJviewStatSql();
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                JviewStat jviewStat = new JviewStat(rs.getString("HR_NAME"),
                                                    rs.getString("ATTENDNO"),
                                                    rs.getString("SCHREGNO"),
                                                    rs.getString("NAME_SHOW"),
                                                    rs.getString("VIEWCD"),
                                                    rs.getString("STATUS"));
                rtn.add(jviewStat);
            }
        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return rtn;
    }

    private String getJviewStatSql() {
        final StringBuffer stb = new StringBuffer();

        stb.append(" WITH SCHNO AS ( ");
        stb.append("     SELECT ");
        stb.append("         T1.HR_NAME, T2.GRADE, T2.HR_CLASS, T2.ATTENDNO, T2.SCHREGNO, T3.NAME_SHOW ");
        stb.append("     FROM ");
        stb.append("         SCHREG_REGD_FI_HDAT T1, ");
        stb.append("         SCHREG_REGD_FI_DAT T2, ");
        stb.append("         SCHREG_BASE_MST T3 ");
        stb.append("     WHERE ");
        stb.append("         T1.YEAR      = T2.YEAR AND ");
        stb.append("         T1.SEMESTER  = T2.SEMESTER AND ");
        stb.append("         T1.GRADE     = T2.GRADE AND ");
        stb.append("         T1.HR_CLASS  = T2.HR_CLASS AND ");
        stb.append("         T1.GRADE || T1.HR_CLASS  = '" + _param._gradeHrClass + "' AND ");
        stb.append("         T2.YEAR      = '" + _param._year + "' AND ");
        stb.append("         T2.SEMESTER  = '" + _param._schregSemester + "' AND ");
        stb.append("         T2.SCHREGNO  = T3.SCHREGNO ");
        stb.append(" ), JVIEWSTAT AS ( ");
        stb.append("     SELECT ");
        stb.append("         SCHREGNO, VIEWCD, STATUS ");
        stb.append("     FROM ");
        stb.append("         JVIEWSTAT_SUB_DAT ");
        stb.append("     WHERE ");
        stb.append("         YEAR = '" + _param._year + "' AND ");
        stb.append("         SEMESTER = '" + _param._semester + "' AND ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || ");
        }
        stb.append("         SUBCLASSCD = '" + _param._subclassCd + "' AND ");
        if (!"".equals(_param._viewCdIn)) {
            stb.append("         VIEWCD IN " + _param._viewCdIn + " AND ");
        } else {
            stb.append("         SUBSTR(VIEWCD,1,2) = '" + _param._viewcdHead + "' AND ");
        }
        stb.append("         VIEWCD NOT LIKE '%99' AND ");
        stb.append("         STATUS IS NOT NULL ");
        stb.append("     UNION ALL ");
        stb.append("     SELECT ");
        stb.append("         SCHREGNO, ");
        stb.append("         '" + _param._viewcdHead + "99' AS VIEWCD, ");
        stb.append("         RTRIM(CHAR(VALUATION)) AS STATUS ");
        stb.append("     FROM ");
        stb.append("         SCHREG_STUDYREC_DAT ");
        stb.append("     WHERE ");
        stb.append("         SCHOOLCD = '0' AND ");
        stb.append("         YEAR = '" + _param._year + "' AND ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || ");
        }
        stb.append("         SUBCLASSCD = '" + _param._subclassCd + "' AND ");
        stb.append("         VALUATION IS NOT NULL ");
        stb.append(" ) ");

        stb.append(" SELECT  TBL1.HR_NAME,TBL1.GRADE,TBL1.HR_CLASS,TBL1.ATTENDNO,TBL1.SCHREGNO,TBL1.NAME_SHOW, ");
        stb.append("         TBL2.VIEWCD, TBL2.STATUS ");
        stb.append(" FROM    SCHNO TBL1 ");
        stb.append("         LEFT JOIN JVIEWSTAT TBL2 ON TBL2.SCHREGNO = TBL1.SCHREGNO ");
        stb.append(" ORDER BY TBL1.GRADE, TBL1.HR_CLASS, TBL1.ATTENDNO, TBL2.VIEWCD ");

        return stb.toString();
    }

    private class JviewStat {
        final String _hrName;
        final String _attendNo;
        final String _schregNo;
        final String _name;
        final String _viewCd;
        final String _status;

        JviewStat(
                final String hrName,
                final String attendNo,
                final String schregNo,
                final String name,
                final String viewCd,
                final String status
                ) {
            _hrName = hrName;
            _attendNo = attendNo;
            _schregNo = schregNo;
            _name = name;
            _viewCd = viewCd;
            _status = status;
        }
    }

    private class JviewName {
        final String _viewCd;
        final String _viewName;
        final String _no;

        JviewName(
                final String viewCd,
                final String viewName,
                final String no
                ) {
            _viewCd = viewCd;
            _viewName = viewName;
            _no = no;
        }
    }

    private void closeDb(final DB2UDB db2) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 56595 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final String _semester;
        private final String _gradeHrClass;
        private final String _classCd;
        private final String _subclassCd;
        private final String _useCurriculumcd;
        private final String _viewcdHead;

        private final String _schregSemester;

        private final boolean _seirekiFlg;
        private final String _className;
        private final String _hrName;
        private final Map _jviewNameMap;
        private String _viewCdIn;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("CTRL_Y");
            _ctrlSemester = request.getParameter("CTRL_S");
            _ctrlDate = request.getParameter("CTRL_D");
            _semester = request.getParameter("SEMESTER");
            _gradeHrClass = request.getParameter("GRADE_HR_CLASS");
            _classCd = request.getParameter("CLASSCD");
            _subclassCd = request.getParameter("SUBCLASSCD");
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            if ("1".equals(_useCurriculumcd)) {
                _viewcdHead = StringUtils.split(_classCd, "-")[0];
            } else {
                _viewcdHead = _classCd;
            }

            _schregSemester = "9".equals(_semester) ? _ctrlSemester : _semester;

            _seirekiFlg = getSeirekiFlg(db2);
            _className = getClassName(db2);
            _hrName = getHrName(db2);
            _jviewNameMap = getJviewNameMap(db2);
        }

        private String getNendo() {
            return _seirekiFlg ? _year + "年度" : KNJ_EditDate.h_format_JP_N(_year + "-01-01") + "度";
        }

        private String getLoginDateString() {
            return getDateString(_ctrlDate);
        }

        private String getDateString(String dateFormat) {
            if (null != dateFormat) {
                return _seirekiFlg ?
                        dateFormat.substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(dateFormat):
                            KNJ_EditDate.h_format_JP(dateFormat ) ;        
            }
            return null;
        }

        private String getTimeString() {
            if (null != _ctrlDate) {
                Calendar cal = Calendar.getInstance();
                int hour = cal.get(Calendar.HOUR_OF_DAY);
                int minute = cal.get(Calendar.MINUTE);
                DecimalFormat df = new DecimalFormat("00");
                return df.format(hour) + "時" + df.format(minute) + "分　現在";
            }
            return "";
        }

        private boolean getSeirekiFlg(final DB2UDB db2) {
            boolean seirekiFlg = false;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1='Z012' AND NAMECD2='00' AND NAME1 IS NOT NULL ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    if (rs.getString("NAME1").equals("2")) seirekiFlg = true; //西暦
                }
            } catch (Exception e) {
                log.error("getSeirekiFlg Exception", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return seirekiFlg;
        }

        private String getClassName(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            String name = null;
            try {
                String sql = null;
                if ("1".equals(_useCurriculumcd)) {
                    sql = "SELECT CLASSNAME FROM CLASS_MST WHERE CLASSCD || '-' || SCHOOL_KIND = '" + _classCd + "' ";
                } else {
                    sql = "SELECT CLASSNAME FROM CLASS_MST WHERE CLASSCD = '" + _classCd + "' ";
                }
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    name = rs.getString("CLASSNAME");
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return name;
        }

        private String getHrName(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            String name = null;
            try {
                String sql = "SELECT HR_NAME FROM SCHREG_REGD_FI_HDAT WHERE YEAR = '" + _year + "' AND SEMESTER = '" + _schregSemester + "' AND GRADE || HR_CLASS = '" + _gradeHrClass + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    name = rs.getString("HR_NAME");
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return name;
        }

        private Map getJviewNameMap(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map rtn = new TreeMap();
            _viewCdIn = "";
            try {
                final String sql = getJviewNameSql();
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                int cnt = 0;
                String seq = "";
                while (rs.next()) {
                    if (cnt == 5) break;
                    final String viewCd = rs.getString("VIEWCD");
                    JviewName jviewName = new JviewName(viewCd, rs.getString("VIEWNAME"), String.valueOf(++cnt));
                    rtn.put(viewCd, jviewName);
                    _viewCdIn = _viewCdIn + seq + viewCd;
                    seq = "','";
                }
                if (0 < cnt) {
                    final String viewCd = _viewcdHead + "99";
                    JviewName jviewName = new JviewName(viewCd, "評定", "9");
                    rtn.put(viewCd, jviewName);
                    _viewCdIn = "('" + _viewCdIn + seq + viewCd + "')";
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }

        private String getJviewNameSql() {
            final StringBuffer stb = new StringBuffer();

            stb.append(" SELECT ");
            stb.append("     T1.VIEWCD, ");
            stb.append("     T2.VIEWNAME ");
            stb.append(" FROM ");
            stb.append("     JVIEWNAME_SUB_YDAT T1, ");
            stb.append("     JVIEWNAME_SUB_MST T2 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _year + "' ");
            stb.append("     AND ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append("         T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
            }
            stb.append("         T1.SUBCLASSCD = '" + _subclassCd + "' ");
            stb.append("     AND ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append("         T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
            }
            stb.append("         T1.SUBCLASSCD = ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append("         T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || ");
            }
            stb.append("         T2.SUBCLASSCD ");
            stb.append("     AND T1.VIEWCD = T2.VIEWCD ");
            stb.append("     AND SUBSTR(T1.VIEWCD,1,2) = '" + _viewcdHead + "' ");
            stb.append("     AND T2.SCHOOL_KIND IN( ");
            stb.append("         SELECT ");
            stb.append("             SCHOOL_KIND ");
            stb.append("         FROM ");
            stb.append("             SCHREG_REGD_GDAT ");
            stb.append("         WHERE ");
            stb.append("             YEAR = '" + _year + "'  AND ");
            stb.append("             GRADE = SUBSTR('" + _gradeHrClass + "',1,2) ");
            stb.append("         ) ");
            stb.append(" ORDER BY ");
            stb.append("    T1.VIEWCD ");

            return stb.toString();
        }

    }
}

// eof
