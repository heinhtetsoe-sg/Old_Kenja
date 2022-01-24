/**
 *
 *	学校教育システム 賢者 [成績管理]
 *
 *					＜KNJD126F＞  観点別成績・評価チェックリスト
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

public class KNJD126F {

    private static final Log log = LogFactory.getLog(KNJD126F.class);

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
            svf.VrSetForm("KNJD126F_2.frm", 1);
        } else {
            svf.VrSetForm("KNJD126F.frm", 1);
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
        svf.VrsOut("CHAIRTITLE", "講座");
        svf.VrsOut("CHAIRNAME", _param._chairName);

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

        stb.append(" WITH VIEWSTAT AS ( ");
        stb.append("     SELECT ");
        stb.append("         schregno ");
        for (Iterator iterator = _param._jviewNameMap.keySet().iterator(); iterator.hasNext();) {
            String key = (String) iterator.next();
            final JviewName jviewName = (JviewName) _param._jviewNameMap.get(key);

            stb.append("   ,MAX(CASE WHEN VIEWCD = '" + jviewName._viewCd + "' THEN STATUS ELSE NULL END) AS STATUS" + jviewName._no);
        }
        stb.append("     FROM ");
        stb.append("         JVIEWSTAT_RECORD_DAT ");
        stb.append("     WHERE ");
        stb.append("         year = '" + _param._year + "' AND ");
        stb.append("         semester = '" + _param._semester + "' AND ");
        stb.append("         CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '" + _param._subclassCd + "' AND ");
        stb.append("         viewcd in " + _param._viewCdIn + " ");
        stb.append("     GROUP BY schregno ");
        stb.append("     ) ");
        stb.append(" SELECT ");
        stb.append("     tbl1.RECORD_DIV, tbl1.hr_name, tbl1.grade, tbl1.hr_class, tbl1.attendno, tbl1.schregno, tbl1.name ");
        for (Iterator iterator = _param._jviewNameMap.keySet().iterator(); iterator.hasNext();) {
            String key = (String) iterator.next();
            final JviewName jviewName = (JviewName) _param._jviewNameMap.get(key);
            if (!"9".equals(jviewName._no)) {
                stb.append(" ,tbl2.status" + jviewName._no);
            } else {
                stb.append(" ,T3.VALUE AS STATUS" + jviewName._no);
            }
        }
        stb.append("  FROM ");
        stb.append("     (SELECT ");
        stb.append("         t1.RECORD_DIV, t1.hr_name, t2.grade, t2.hr_class, t2.attendno, t4.schregno, t3.name, ");
        stb.append("         L1.CLASSCD || L1.SCHOOL_KIND || L1.CURRICULUM_CD || L1.SUBCLASSCD AS SUBCLASSCD ");
        stb.append("     FROM ");
        stb.append("         SCHREG_REGD_FI_HDAT t1, ");
        stb.append("         SCHREG_REGD_FI_DAT t2, ");
        stb.append("         schreg_base_mst t3, ");
        stb.append("         chair_std_dat t4 ");
        stb.append("         LEFT JOIN CHAIR_DAT L1 ON T4.YEAR = L1.YEAR ");
        stb.append("              AND T4.SEMESTER = L1.SEMESTER ");
        stb.append("              AND T4.CHAIRCD = L1.CHAIRCD ");
        stb.append("     WHERE ");
        stb.append("         t1.year      = t2.year AND ");
        stb.append("         t1.semester  = t2.semester AND ");
        stb.append("         t1.grade     = t2.grade AND ");
        stb.append("         t1.hr_class  = t2.hr_class AND ");
        stb.append("         t2.year      = '" + _param._year + "' AND ");
        stb.append("         t2.semester  = '" + _param._schregSemester + "' AND ");
        stb.append("         t2.schregno  = t3.schregno AND ");
        stb.append("         t4.year      = t2.year AND ");
        stb.append("         t4.chaircd   = '" + _param._chairCd + "' AND ");
        stb.append("         t4.schregno  = t2.schregno AND ");
        stb.append("         '" + _param._ctrlDate + "' BETWEEN t4.appdate AND t4.appenddate ");
        stb.append("     ) tbl1 ");
        stb.append("     left join VIEWSTAT tbl2 on tbl2.schregno = tbl1.schregno ");
        stb.append("      LEFT JOIN RECORD_SCORE_DAT T3 ");
        stb.append("               ON T3.YEAR         = '" + _param._year + "' ");
        stb.append("              AND T3.SEMESTER     = '" + _param._semester + "' ");
        stb.append("              AND T3.TESTKINDCD   = '99' ");
        stb.append("              AND T3.TESTITEMCD   = '00' ");
        stb.append("              AND T3.SCORE_DIV    = '00' ");
        stb.append("              AND T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD = '" + _param._subclassCd + "' ");
        stb.append("              AND T3.SCHREGNO     = tbl1.SCHREGNO ");
        stb.append("  WHERE ");
        stb.append("      tbl1.SUBCLASSCD NOT IN ");
        stb.append("      (SELECT DISTINCT ");
        stb.append("           W1.CLASSCD || W1.SCHOOL_KIND || W1.CURRICULUM_CD || W1.SUBCLASSCD ");
        stb.append("       FROM ");
        stb.append("           SUBCLASS_GRADE_DAT W1 ");
        stb.append("       WHERE ");
        stb.append("           W1.YEAR = '" + _param._year + "' ");
        stb.append("           AND W1.GRADE = tbl1.GRADE ");
        stb.append("           AND W1.RECORD_DIV = tbl1.RECORD_DIV ");
        stb.append("           AND VALUE(W1.TEXT_HYOKA_FLG, '0') = '1') ");
        stb.append("  ORDER BY ");
        stb.append("     tbl1.grade, tbl1.hr_class, tbl1.attendno ");

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
        private final String _subclassCd;
        private final String _chairCd;
        private final String _kantenHyouji;
        private final boolean _kantenHyouji5;
        private final String _classCd;
        private final String _classKeitai;

        private final String _schregSemester;

        private final boolean _seirekiFlg;
        private final String _className;
        private final String _chairName;
        private final Map _jviewNameMap;
        private String _viewCdIn;

        Param(final DB2UDB db2, final HttpServletRequest request) throws Exception {
            _year = request.getParameter("CTRL_Y");
            _ctrlSemester = request.getParameter("SEMESTER2");
            _ctrlDate = request.getParameter("CTRL_D");
            _semester = request.getParameter("SEMESTER");
            _semesterName = getSemeName(db2);
            _classCd = request.getParameter("CLASSCD");
            final String getChairSub = request.getParameter("CHAIRCD");
            _chairCd = getChairSub.substring(0, 7);
            _subclassCd = getChairSub.substring(8);
            _kantenHyouji = request.getParameter("kantenHyouji");
            _kantenHyouji5 = !"6".equals(_kantenHyouji);
            _classKeitai = request.getParameter("classKeitai");

            _schregSemester = "9".equals(_semester) ? _ctrlSemester : _semester;

            _seirekiFlg = getSeirekiFlg(db2);
            _className = getClassName(db2);
            _chairName = getChairName(db2);
            _jviewNameMap = getJviewNameMap(db2);
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

        private String getChairName(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            String name = null;
            try {
                String sql = "SELECT CHAIRNAME FROM CHAIR_DAT WHERE YEAR = '" + _year + "' AND SEMESTER = '" + _schregSemester + "' AND CHAIRCD = '" + _chairCd + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    name = rs.getString("CHAIRNAME");
                    break;
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

            stb.append(" WITH MAX_GRADE AS ( ");
            stb.append("     SELECT ");
            stb.append("         max(t2.grade) as grade ");
            stb.append("     FROM ");
            stb.append("         SCHREG_REGD_FI_HDAT t1, ");
            stb.append("         SCHREG_REGD_FI_DAT t2, ");
            stb.append("         schreg_base_mst t3, ");
            stb.append("         chair_std_dat t4 ");
            stb.append("     WHERE ");
            stb.append("         t1.year      = t2.year AND ");
            stb.append("         t1.semester  = t2.semester AND ");
            stb.append("         t1.grade     = t2.grade AND ");
            stb.append("         t1.hr_class  = t2.hr_class AND ");
            stb.append("         t2.year      = '" + _year + "' AND ");
            stb.append("         t2.semester  = '" + _schregSemester + "' AND ");
            stb.append("         t2.schregno  = t3.schregno AND ");
            stb.append("         t4.year      = t2.year AND ");
            stb.append("         t4.chaircd   = '" + _chairCd + "' AND ");
            stb.append("         t4.schregno  = t2.schregno AND ");
            stb.append("         '" + _ctrlDate + "' BETWEEN t4.appdate AND t4.appenddate ");
            stb.append("     ) ");
            stb.append(" SELECT ");
            stb.append("     t1.viewcd, ");
            stb.append("     t2.viewname, ");
            stb.append("     L1.REMARK1 AS DETAIL_VIEWNAME ");
            stb.append(" FROM ");
            stb.append("     jviewname_grade_ydat t1, ");
            stb.append("     jviewname_grade_mst t2 ");
            stb.append("     INNER JOIN JVIEWNAME_GRADE_DETAIL_MST L1 ON T2.GRADE = L1.GRADE ");
            stb.append("           AND T2.CLASSCD = L1.CLASSCD ");
            stb.append("           AND T2.SCHOOL_KIND = L1.SCHOOL_KIND ");
            stb.append("           AND T2.CURRICULUM_CD = L1.CURRICULUM_CD ");
            stb.append("           AND T2.SUBCLASSCD = L1.SUBCLASSCD ");
            stb.append("           AND T2.VIEWCD = L1.VIEWCD ");
            stb.append("           AND L1.VIEW_SEQ = '00" + _semester + "' ");
            stb.append("           AND L1.REMARK1 IS NOT NULL ");
            stb.append("      INNER JOIN JVIEWNAME_GRADE_DETAIL_MST L2 ON L1.GRADE = L2.GRADE ");
            stb.append("                                             AND L1.CLASSCD = L2.CLASSCD ");
            stb.append("                                             AND L1.SCHOOL_KIND = L2.SCHOOL_KIND ");
            stb.append("                                             AND L1.CURRICULUM_CD = L2.CURRICULUM_CD ");
            stb.append("                                             AND L1.SUBCLASSCD = L2.SUBCLASSCD ");
            stb.append("                                             AND L1.VIEWCD = L2.VIEWCD ");
            stb.append("                                             AND L2.VIEW_SEQ = '004' ");
            stb.append("                                             AND L2.REMARK1 = '" + _classKeitai + "' ");
            stb.append(" WHERE ");
            stb.append("     t1.year = '" + _year + "' AND ");
            stb.append("     t1.grade = t2.grade AND ");
            stb.append("     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '" + _subclassCd + "' AND ");
            stb.append("     t1.CLASSCD       = t2.CLASSCD AND ");
            stb.append("     t1.SCHOOL_KIND   = t2.SCHOOL_KIND AND ");
            stb.append("     t1.CURRICULUM_CD = t2.CURRICULUM_CD AND ");
            stb.append("     t1.SUBCLASSCD    = t2.SUBCLASSCD AND ");
            stb.append("     t1.viewcd = t2.viewcd AND ");
            stb.append("     SUBSTR(t1.VIEWCD,1,2) = '" + _classCd.substring(0, 2) + "' ");
            stb.append("     AND t1.grade in (SELECT w1.grade FROM MAX_GRADE w1) ");
            stb.append(" ORDER BY ");
            stb.append("     t1.viewcd ");

            return stb.toString();
        }

    }

}
