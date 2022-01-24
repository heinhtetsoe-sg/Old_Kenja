/**
 *
 *	学校教育システム 賢者 [成績管理]
 *
 *					＜KNJD126E＞  観点別成績・評価チェックリスト
 *
 *	2011/01/07 nakamoto 作成日
 */

package servletpack.KNJD;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

public class KNJD126E {

    private static final Log log = LogFactory.getLog(KNJD126E.class);

    private boolean _hasData;

    Param _param;

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
        if (!_param._kantenHyouji5) {
            svf.VrSetForm("KNJD126E_2.frm", 1);
        } else {
            svf.VrSetForm("KNJD126E.frm", 1);
        }

        setHead(db2, svf);

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getSelectQuery();
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            int lineCnt = 1;
            while (rs.next()) {
                if (lineCnt > 50) {
                    svf.VrEndPage();
                    setHead(db2, svf);
                    lineCnt = 1;
                }
                svf.VrsOutn("HR_NAME", lineCnt, rs.getString("HR_NAME") + "-" + rs.getString("ATTENDNO"));
                svf.VrsOutn("NAME", lineCnt, rs.getString("SCHREGNO") + "　" + rs.getString("NAME"));
                for (Iterator iterator = _param._jviewNameMap.keySet().iterator(); iterator.hasNext();) {
                    String key = (String) iterator.next();
                    final JviewName jviewName = (JviewName) _param._jviewNameMap.get(key);

                    svf.VrsOutn("STATUS" + jviewName._no, lineCnt, rs.getString("STATUS" + jviewName._no));
                }
                lineCnt++;
            }
        } catch (SQLException e) {
            log.error("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }

        if (_hasData) {
            svf.VrEndPage();
        }
    }

    private void setHead(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrsOut("NENDO", _param.getNendo());
        svf.VrsOut("SEMESTER", _param._semesterName);
        svf.VrsOut("CLASS", _param._className);
        svf.VrsOut("HRTITLE", "クラス");
        svf.VrsOut("HR_CLASS", _param._fiHrName);

        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DATE);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        int sec = cal.get(Calendar.SECOND);
        final String nendo = KenjaProperties.gengou(year);
        DecimalFormat df = new DecimalFormat("00");
        final String setDateTime = nendo + "年" + df.format(month) + "月" + df.format(day) + "日" +
                     df.format(hour) + "時" + df.format(minute) + "分" + " 現在";
        svf.VrsOut("DATE", setDateTime);

        int viewCnt = 1;
        for (Iterator iterator = _param._jviewNameMap.keySet().iterator(); iterator.hasNext();) {
            String key = (String) iterator.next();

            final JviewName jviewName = (JviewName) _param._jviewNameMap.get(key);
            if (!"9".equals(jviewName._no)) {
                svf.VrsOut("VIEWCD1_" + viewCnt, String.valueOf(viewCnt));
                svf.VrsOutn("VIEWCD2", viewCnt, String.valueOf(viewCnt));
                svf.VrsOutn("VIEWNAME", viewCnt, jviewName._detailViewName);
                viewCnt++;
            }
        }
        _hasData = true;
    }

    private String getSelectQuery() {
        final StringBuffer stb = new StringBuffer();

        stb.append(" WITH SCHINFO AS ( ");
        stb.append("    SELECT ");
        stb.append("       T1.HR_NAME, ");
        stb.append("       T2.GRADE, ");
        stb.append("       T2.HR_CLASS, ");
        stb.append("       T2.ATTENDNO, ");
        stb.append("       T2.SCHREGNO, ");
        stb.append("       T3.NAME ");
        stb.append("    FROM ");
        stb.append("       SCHREG_REGD_FI_HDAT T1, ");
        stb.append("       SCHREG_REGD_FI_DAT T2, ");
        stb.append("       SCHREG_BASE_MST T3 ");
        stb.append("    WHERE ");
        stb.append("       T1.YEAR      = T2.YEAR AND ");
        stb.append("       T1.SEMESTER  = T2.SEMESTER AND ");
        stb.append("       T1.GRADE     = T2.GRADE AND ");
        stb.append("       T1.HR_CLASS  = T2.HR_CLASS AND ");
        stb.append("       T1.GRADE || T1.HR_CLASS  = '" + _param._grade + _param._hrclass + "' AND ");
        stb.append("       T2.YEAR      = '" + _param._year + "' AND ");
        stb.append("       T2.SEMESTER  = '" + _param._semester + "' AND ");
        stb.append("       T2.SCHREGNO  = T3.SCHREGNO ");
        stb.append(" ), VIEWSTAT AS ( ");
        stb.append("    SELECT ");
        stb.append("        T1.SCHREGNO ");
        for (Iterator iterator = _param._jviewNameMap.keySet().iterator(); iterator.hasNext();) {
            String key = (String) iterator.next();
            final JviewName jviewName = (JviewName) _param._jviewNameMap.get(key);

            stb.append("   ,MAX(CASE WHEN T2.VIEWCD = '" + jviewName._viewCd + "' THEN T2.STATUS ELSE NULL END) AS STATUS" + jviewName._no);
        }
        stb.append("    FROM ");
        stb.append("        SCHINFO T1, ");
        stb.append("        JVIEWSTAT_RECORD_DAT T2 ");
        stb.append("    WHERE ");
        stb.append("       T2.YEAR = '" + _param._year + "' AND ");
        stb.append("       T2.SEMESTER = '" + _param._semester + "' AND ");
        stb.append("       T1.SCHREGNO = T2.SCHREGNO AND ");
        stb.append("       T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD = '" + _param._subclassCd + "' AND ");
        stb.append("       T2.VIEWCD IN " + _param._viewCdIn );
        stb.append("   GROUP BY T1.SCHREGNO ");
        stb.append(" ) ");

        stb.append(" SELECT ");
        stb.append("   T1.HR_NAME, ");
        stb.append("   T1.GRADE, ");
        stb.append("   T1.HR_CLASS, ");
        stb.append("   T1.ATTENDNO, ");
        stb.append("   T1.SCHREGNO, ");
        stb.append("   T1.NAME ");
        for (Iterator iterator = _param._jviewNameMap.keySet().iterator(); iterator.hasNext();) {
            String key = (String) iterator.next();
            final JviewName jviewName = (JviewName) _param._jviewNameMap.get(key);

            if (!"9".equals(jviewName._no)) {
                stb.append(" ,T2.STATUS" + jviewName._no);
            } else {
                stb.append(" ,T3.VALUE AS STATUS" + jviewName._no);
            }
        }

        stb.append(" FROM ");
        stb.append("    SCHINFO T1 ");
        stb.append("    LEFT JOIN VIEWSTAT T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("    LEFT JOIN RECORD_SCORE_DAT T3 ");
        stb.append("             ON T3.YEAR         = '" + _param._year + "' ");
        stb.append("            AND T3.SEMESTER     = '" + _param._semester + "' ");
        stb.append("            AND T3.TESTKINDCD   = '99' ");
        stb.append("            AND T3.TESTITEMCD   = '00' ");
        stb.append("            AND T3.SCORE_DIV    = '00' ");
        stb.append("            AND T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD = '" + _param._subclassCd + "' ");
        stb.append("            AND T3.SCHREGNO     = T1.SCHREGNO ");
        stb.append(" ORDER BY ");
        stb.append("   T1.GRADE, T1.HR_CLASS, T1.ATTENDNO ");

        return stb.toString();
    }

    private class JviewName {
        final String _viewCd;
        final String _viewName;
        final String _detailViewName;
        final String _no;

        JviewName(
                final String viewCd,
                final String viewName,
                final String detailViewName,
                final String no
                ) {
            _viewCd = viewCd;
            _viewName = viewName;
            _detailViewName = detailViewName;
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
        private final String _semesterName;
        private final String _grade;
        private final String _hrclass;
        private final String _fiHrName;
        private final String _subclassCd;
        private final String _kantenHyouji;
        private final boolean _kantenHyouji5;
        private final String _classCd;
        private final String _classKeitai;

        private final String _schregSemester;

        private final boolean _seirekiFlg;
        private final String _className;
        private final Map _jviewNameMap;
        private String _viewCdIn;

        Param(final DB2UDB db2, final HttpServletRequest request) throws Exception {
            _year = request.getParameter("CTRL_Y");
            _ctrlSemester = request.getParameter("CTRL_S");
            _ctrlDate = request.getParameter("CTRL_D");
            _semester = request.getParameter("SEMESTER");
            _semesterName = getSemeName(db2);
            final String gradeHr = request.getParameter("GRADE_HR_CLASS");
            _grade = gradeHr.substring(0, 2);
            _hrclass = gradeHr.substring(2);
            _fiHrName = getFiName(db2);
            _classCd = request.getParameter("CLASSCD");
            _subclassCd = request.getParameter("SUBCLASSCD");
            _kantenHyouji = request.getParameter("kantenHyouji");
            _kantenHyouji5 = !"6".equals(_kantenHyouji);
            _classKeitai = request.getParameter("classKeitai");

            _schregSemester = "9".equals(_semester) ? _ctrlSemester : _semester;

            _seirekiFlg = getSeirekiFlg(db2);
            _className = getClassName(db2);
            _jviewNameMap = getJviewNameMap(db2);
        }

        private String getFiName(final DB2UDB db2) {
            String retStr = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String semeSql = "SELECT HR_NAME FROM SCHREG_REGD_FI_HDAT WHERE YEAR = '" + _year + "' AND SEMESTER = '" + _semester + "' AND GRADE = '" + _grade + "' AND HR_CLASS = '" + _hrclass + "'";
                ps = db2.prepareStatement(semeSql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    retStr = rs.getString("HR_NAME");
                }
            } catch (Exception e) {
                log.error("getSeirekiFlg Exception", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }

            return retStr;
        }

        private String getSemeName(final DB2UDB db2) {
            String retStr = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String semeSql = "SELECT SEMESTERNAME FROM SEMESTER_MST WHERE YEAR = '" + _year + "' AND SEMESTER = '" + _semester + "'";
                ps = db2.prepareStatement(semeSql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    retStr = rs.getString("SEMESTERNAME");
                }
            } catch (Exception e) {
                log.error("getSeirekiFlg Exception", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }

            return retStr;
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
                sql = "SELECT CLASSNAME FROM CLASS_MST WHERE CLASSCD || '-' || SCHOOL_KIND = '" + _classCd + "' ";
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
                    JviewName jviewName = new JviewName(viewCd, rs.getString("VIEWNAME"), rs.getString("DETAIL_VIEWNAME"), String.valueOf(++cnt));
                    rtn.put(viewCd, jviewName);
                    _viewCdIn = _viewCdIn + seq + viewCd;
                    seq = "','";
                }
                if (0 < cnt) {
                    final String viewCd = _classCd + "99";
                    JviewName jviewName = new JviewName(viewCd, "評定", "評定", "9");
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
            stb.append("     T2.VIEWNAME, ");
            stb.append("     L1.REMARK1 AS DETAIL_VIEWNAME ");
            stb.append(" FROM ");
            stb.append("     JVIEWNAME_GRADE_YDAT T1, ");
            stb.append("     JVIEWNAME_GRADE_MST T2 ");
            stb.append("     INNER JOIN JVIEWNAME_GRADE_DETAIL_MST L1 ON L1.GRADE = T2.GRADE ");
            stb.append("           AND L1.CLASSCD = T2.CLASSCD ");
            stb.append("           AND L1.SCHOOL_KIND = T2.SCHOOL_KIND ");
            stb.append("           AND L1.CURRICULUM_CD = T2.CURRICULUM_CD ");
            stb.append("           AND L1.SUBCLASSCD = T2.SUBCLASSCD ");
            stb.append("           AND L1.VIEWCD = T2.VIEWCD ");
            stb.append("           AND L1.VIEW_SEQ = '00" + _semester + "' ");
            stb.append("           AND L1.REMARK1 IS NOT NULL ");
            stb.append("     INNER JOIN JVIEWNAME_GRADE_DETAIL_MST L2 ON L1.GRADE = L2.GRADE ");
            stb.append("           AND L1.CLASSCD = L2.CLASSCD ");
            stb.append("           AND L1.SCHOOL_KIND = L2.SCHOOL_KIND ");
            stb.append("           AND L1.CURRICULUM_CD = L2.CURRICULUM_CD ");
            stb.append("           AND L1.SUBCLASSCD = L2.SUBCLASSCD ");
            stb.append("           AND L1.VIEWCD = L2.VIEWCD ");
            stb.append("           AND L2.VIEW_SEQ = '004' ");
            stb.append("           AND L2.REMARK1 = '" + _classKeitai + "' ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _year + "' ");
            stb.append("     AND T1.GRADE = '" + _grade + "' ");
            stb.append("     AND T1.GRADE = T2.GRADE ");
            stb.append("     AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '" + _subclassCd + "' ");
            stb.append("     AND T1.CLASSCD       = T2.CLASSCD ");
            stb.append("     AND T1.SCHOOL_KIND   = T2.SCHOOL_KIND ");
            stb.append("     AND T1.CURRICULUM_CD = T2.CURRICULUM_CD ");
            stb.append("     AND T1.SUBCLASSCD    = T2.SUBCLASSCD ");
            stb.append("     AND T1.VIEWCD = T2.VIEWCD ");
            stb.append("     AND SUBSTR(T1.VIEWCD,1,2) = '" + _classCd.substring(0, 2) + "' ");
            stb.append(" ORDER BY ");
            stb.append("    T1.VIEWCD ");

            return stb.toString();
        }

    }

}
