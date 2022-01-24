// kanji=漢字
/*
 * 作成日: 2021/03/16 15:47:43 - JST 作成者: shimoji
 *
 * Copyright(C) 2009-2009 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJF;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 * <<クラスの説明>>。
 *
 * @author shimoji
 */
public class KNJF150F {

    private static final Log log = LogFactory.getLog(KNJF150F.class);

    private boolean _hasData;

    private Param _param;

    private static final String NAME_MST_NAMECD1_NAIKA = "F200"; // 1:内科

    private static final String NAME_MST_NAMECD1_GEKA = "F201"; // 2:外科

    private static final String TYPE_NAIKA = "1"; // 1:内科

    private static final String TYPE_GEKA = "2"; // 2:外科

    /**
     * @param request リクエスト
     * @param response レスポンス
     */
    public void svf_out(final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {

        final Vrw32alp svf = new Vrw32alp();
        DB2UDB db2 = null;

        try {
            response.setContentType("application/pdf");

            svf.VrInit();
            svf.VrSetSpoolFileStream(response.getOutputStream());

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(request, db2);

            _hasData = false;

            printMain(svf, db2);
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }

            if (null != db2) {
                db2.commit();
                db2.close();
            }
            svf.VrQuit();
        }

    }

    /**
     * 印刷処理（メイン）
     */
    private void printMain(final Vrw32alp svf, final DB2UDB db2) {
        svf.VrSetForm("KNJF150F.frm", 4);

        Map<String, String> totalHrClassMap = getTotalHrClass(db2);
        Map<String, String> totalStudentMap = getTotalStudent(db2);
        Map<String, String> totalUsePreMap = getTotalUsePre(db2);
        Map<String, String> totalUsePreNaikaMap = getTotalUsePreType(db2, TYPE_NAIKA);
        Map<String, String> totalUsePreGekaMap = getTotalUsePreType(db2, TYPE_GEKA);
        int totalUsePreNaika = 0;
        int totalUsePreGeka = 0;

        svf.VrsOut("TITLE", "保健室来室者統計調査票【" + _param._gengou + "年度(" + _param._ctrlYear + "年度)】");
        svf.VrsOut("SCHOOL_NAME", _param._schoolName);
        svf.VrsOut("HR_NUM", totalHrClassMap.get("HR_CLASS_CNT"));
        svf.VrsOut("ATTEND_NUM", totalStudentMap.get("STUDENT_CNT"));

        svf.VrsOut("GRPCD", "1");
        svf.VrsOut("ITEM1", "");
        svf.VrsOut("ITEM2", "０回");
        svf.VrsOut("ITEM_NUM", totalUsePreMap.get("PRE_USE0"));
        svf.VrEndRecord();

        svf.VrsOut("GRPCD", "1");
        svf.VrsOut("ITEM1", "");
        svf.VrsOut("ITEM2", "１回");
        svf.VrsOut("ITEM_NUM", totalUsePreMap.get("PRE_USE1"));
        svf.VrEndRecord();

        svf.VrsOut("GRPCD", "1");
        svf.VrsOut("ITEM1", "");
        svf.VrsOut("ITEM2", "２回");
        svf.VrsOut("ITEM_NUM", totalUsePreMap.get("PRE_USE2"));
        svf.VrEndRecord();

        svf.VrsOut("GRPCD", "1");
        svf.VrsOut("ITEM1", "利用回数");
        svf.VrsOut("ITEM2", "３回");
        svf.VrsOut("ITEM_NUM", totalUsePreMap.get("PRE_USE3"));
        svf.VrEndRecord();

        svf.VrsOut("GRPCD", "1");
        svf.VrsOut("ITEM1", "（人数）");
        svf.VrsOut("ITEM2", "４～５回");
        svf.VrsOut("ITEM_NUM", totalUsePreMap.get("PRE_USE4_5"));
        svf.VrEndRecord();

        svf.VrsOut("GRPCD", "1");
        svf.VrsOut("ITEM1", "");
        svf.VrsOut("ITEM2", "６～９回");
        svf.VrsOut("ITEM_NUM", totalUsePreMap.get("PRE_USE6_9"));
        svf.VrEndRecord();

        svf.VrsOut("GRPCD", "1");
        svf.VrsOut("ITEM1", "");
        svf.VrsOut("ITEM2", "１０～１９回");
        svf.VrsOut("ITEM_NUM", totalUsePreMap.get("PRE_USE10_19"));
        svf.VrEndRecord();

        svf.VrsOut("GRPCD", "1");
        svf.VrsOut("ITEM1", "");
        svf.VrsOut("ITEM2", "２０回以上（※）");
        svf.VrsOut("ITEM_NUM", totalUsePreMap.get("PRE_USE20_OVER"));
        svf.VrEndRecord();

        int naikaCnt = 1;
        for (String namecd2 : _param._nameMstNaika.keySet()) {
            String name1 = _param._nameMstNaika.get(namecd2);
            String cnt = StringUtils.defaultString(totalUsePreNaikaMap.get(namecd2), "0");
            totalUsePreNaika += Integer.parseInt(cnt);

            svf.VrsOut("GRPCD", "2");
            if (naikaCnt == 1) {
                svf.VrsOut("ITEM1", "主症状");
            } else if (naikaCnt == 2) {
                svf.VrsOut("ITEM1", "内科系");
            } else if (naikaCnt == 3) {
                svf.VrsOut("ITEM1", "（人数）");
            } else {
                svf.VrsOut("ITEM1", "");
            }
            svf.VrsOut("ITEM2", name1);
            svf.VrsOut("ITEM_NUM", cnt);

            naikaCnt++;
            svf.VrEndRecord();
        }
        svf.VrsOut("GRPCD", "2");
        svf.VrsOut("ITEM1", "");
        svf.VrsOut("ITEM2", "内科合計");
        svf.VrsOut("ITEM_NUM", String.valueOf(totalUsePreNaika));
        svf.VrEndRecord();

        int gekaCnt = 1;
        for (String namecd2 : _param._nameMstGeka.keySet()) {
            String name1 = _param._nameMstGeka.get(namecd2);
            String cnt = StringUtils.defaultString(totalUsePreGekaMap.get(namecd2), "0");
            totalUsePreGeka += Integer.parseInt(cnt);

            svf.VrsOut("GRPCD", "3");
            if (gekaCnt == 1) {
                svf.VrsOut("ITEM1", "主症状");
            } else if (gekaCnt == 2) {
                svf.VrsOut("ITEM1", "外科系");
            } else if (gekaCnt == 3) {
                svf.VrsOut("ITEM1", "（人数）");
            } else {
                svf.VrsOut("ITEM1", "");
            }
            svf.VrsOut("ITEM2", name1);
            svf.VrsOut("ITEM_NUM", cnt);

            gekaCnt++;
            svf.VrEndRecord();
        }
        svf.VrsOut("GRPCD", "3");
        svf.VrsOut("ITEM1", "");
        svf.VrsOut("ITEM2", "外科合計");
        svf.VrsOut("ITEM_NUM", String.valueOf(totalUsePreGeka));
        svf.VrEndRecord();

        svf.VrsOut("TOTAL_NAME", "主症状総計");
        svf.VrsOut("TOTAL_NUM", String.valueOf(totalUsePreNaika + totalUsePreGeka));
        svf.VrEndRecord();

        _hasData = true;
    }

    private Map<String, String> getTotalHrClass(final DB2UDB db2) {
        Map<String, String> hrcMap = new HashMap<String, String>();

        final String totalHrClassSql = getTotalHrClassSql();
        log.debug(" total hr class sql = " + totalHrClassSql);

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = db2.prepareStatement(totalHrClassSql);
            rs = ps.executeQuery();

            while (rs.next()) {
                hrcMap.put("HR_CLASS_CNT", rs.getString("CNT"));
            }

            db2.commit();
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }

        return hrcMap;
    }

    private Map<String, String> getTotalStudent(final DB2UDB db2) {
        Map<String, String> hrcMap = new HashMap<String, String>();

        final String totalStudentSql = getTotalStudentSql();
        log.debug(" total student sql = " + totalStudentSql);

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = db2.prepareStatement(totalStudentSql);
            rs = ps.executeQuery();

            while (rs.next()) {
                hrcMap.put("STUDENT_CNT", rs.getString("CNT"));
            }

            db2.commit();
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }

        return hrcMap;
    }

    private Map<String, String> getTotalUsePre(final DB2UDB db2) {
        Map<String, String> hrcMap = new LinkedHashMap<String, String>();

        final String totalUsePreSql = getTotalUsePreSql();
        log.debug(" total use pre sql = " + totalUsePreSql);

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = db2.prepareStatement(totalUsePreSql);
            rs = ps.executeQuery();

            while (rs.next()) {
                hrcMap.put("PRE_USE0", rs.getString("PRE_USE0"));
                hrcMap.put("PRE_USE1", rs.getString("PRE_USE1"));
                hrcMap.put("PRE_USE2", rs.getString("PRE_USE2"));
                hrcMap.put("PRE_USE3", rs.getString("PRE_USE3"));
                hrcMap.put("PRE_USE4_5", rs.getString("PRE_USE4_5"));
                hrcMap.put("PRE_USE6_9", rs.getString("PRE_USE6_9"));
                hrcMap.put("PRE_USE10_19", rs.getString("PRE_USE10_19"));
                hrcMap.put("PRE_USE20_OVER", rs.getString("PRE_USE20_OVER"));
            }

            db2.commit();
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }

        return hrcMap;
    }

    private Map<String, String> getTotalUsePreType(final DB2UDB db2, String type) {
        Map<String, String> hrcMap = new LinkedHashMap<String, String>();

        final String totalUsePreTypeSql = getTotalUsePreTypeSql(type);
        log.debug(" total pre use type(" + type + ") sql = " + totalUsePreTypeSql);

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = db2.prepareStatement(totalUsePreTypeSql);
            rs = ps.executeQuery();

            while (rs.next()) {
                String key = rs.getString("VISIT_REASON");
                String value = rs.getString("CNT");

                hrcMap.put(key, value);
            }

            db2.commit();
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }

        return hrcMap;
    }

    private String getTotalHrClassSql() {
        StringBuffer stb = new StringBuffer();

        stb.append(" WITH HR_CLASS_T AS ( ");
        stb.append("     SELECT DISTINCT ");
        stb.append("         HR_CLASS ");
        stb.append("     FROM ");
        stb.append("         SCHREG_REGD_DAT ");
        stb.append("     WHERE ");
        stb.append("         YEAR     = '" + _param._ctrlYear + "' AND ");
        stb.append("         SEMESTER = '" + _param._ctrlSemester + "' ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     COUNT(HR_CLASS) CNT ");
        stb.append(" FROM ");
        stb.append("     HR_CLASS_T ");

        return stb.toString();
    }

    private String getTotalStudentSql() {
        StringBuffer stb = new StringBuffer();

        stb.append(" SELECT ");
        stb.append("     COUNT(SCHREGNO) CNT ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT ");
        stb.append(" WHERE ");
        stb.append("     YEAR     = '" + _param._ctrlYear + "' AND ");
        stb.append("     SEMESTER = '" + _param._ctrlSemester + "' ");

        return stb.toString();
    }

    private String getTotalUsePreSql() {
        StringBuffer stb = new StringBuffer();

        stb.append(" WITH VISITREC AS ( ");
        stb.append(getPopulationSql());
        stb.append(" ), ");
        stb.append(" TOTAL_PRE_USE0 AS ( ");
        stb.append("     SELECT ");
        stb.append("         REGD.SCHREGNO, ");
        stb.append(
                "         CASE WHEN COUNT(VISITREC.SCHREGNO) = 0 THEN 1 ELSE 0 END AS PRE_USE0, ");
        stb.append(
                "         CASE WHEN COUNT(VISITREC.SCHREGNO) = 1 THEN 1 ELSE 0 END AS PRE_USE1, ");
        stb.append(
                "         CASE WHEN COUNT(VISITREC.SCHREGNO) = 2 THEN 1 ELSE 0 END AS PRE_USE2, ");
        stb.append(
                "         CASE WHEN COUNT(VISITREC.SCHREGNO) = 3 THEN 1 ELSE 0 END AS PRE_USE3, ");
        stb.append(
                "         CASE WHEN COUNT(VISITREC.SCHREGNO) BETWEEN  4 AND  5  THEN 1 ELSE 0 END AS PRE_USE4_5, ");
        stb.append(
                "         CASE WHEN COUNT(VISITREC.SCHREGNO) BETWEEN  6 AND  9  THEN 1 ELSE 0 END AS PRE_USE6_9, ");
        stb.append(
                "         CASE WHEN COUNT(VISITREC.SCHREGNO) BETWEEN 10 AND 19  THEN 1 ELSE 0 END AS PRE_USE10_19, ");
        stb.append(
                "         CASE WHEN COUNT(VISITREC.SCHREGNO) >= 20 THEN 1 ELSE 0 END AS PRE_USE20_OVER ");
        stb.append("     FROM ");
        stb.append("         SCHREG_REGD_DAT REGD ");
        stb.append("         LEFT JOIN VISITREC ");
        stb.append("                ON VISITREC.YEAR     = REGD.YEAR ");
        stb.append("               AND VISITREC.SEMESTER = REGD.SEMESTER ");
        stb.append("               AND VISITREC.SCHREGNO = REGD.SCHREGNO ");
        stb.append("     WHERE ");
        stb.append("         REGD.YEAR     = '" + _param._ctrlYear + "' AND ");
        stb.append("         REGD.SEMESTER = '" + _param._ctrlSemester + "' ");
        stb.append("     ");
        stb.append("     GROUP BY ");
        stb.append("         REGD.SCHREGNO ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     SUM(PRE_USE0) AS PRE_USE0, ");
        stb.append("     SUM(PRE_USE1) AS PRE_USE1, ");
        stb.append("     SUM(PRE_USE2) AS PRE_USE2, ");
        stb.append("     SUM(PRE_USE3) AS PRE_USE3, ");
        stb.append("     SUM(PRE_USE4_5) AS PRE_USE4_5, ");
        stb.append("     SUM(PRE_USE6_9) AS PRE_USE6_9, ");
        stb.append("     SUM(PRE_USE10_19) AS PRE_USE10_19, ");
        stb.append("     SUM(PRE_USE20_OVER) AS PRE_USE20_OVER ");
        stb.append(" FROM ");
        stb.append("     TOTAL_PRE_USE0 ");

        return stb.toString();
    }

    private String getTotalUsePreTypeSql(String type) {
        StringBuffer stb = new StringBuffer();

        stb.append(" WITH VISITREC AS ( ");
        stb.append(getPopulationSql());
        stb.append(" ), ");
        stb.append(" VISITREC_CNT AS ( ");
        stb.append(" SELECT ");
        stb.append("     VISIT_REASON1 AS VISIT_REASON, ");
        stb.append("     COUNT(SCHREGNO) AS CNT ");
        stb.append(" FROM ");
        stb.append("     VISITREC ");
        stb.append(" WHERE ");
        stb.append("     TYPE = '" + type + "' ");
        stb.append(" AND VISIT_REASON1 IS NOT NULL ");
        stb.append(" GROUP BY ");
        stb.append("     VISIT_REASON1 ");
        stb.append(" UNION ALL ");
        stb.append(" SELECT ");
        stb.append("     VISIT_REASON2 AS VISIT_REASON, ");
        stb.append("     COUNT(SCHREGNO) AS CNT ");
        stb.append(" FROM ");
        stb.append("     VISITREC ");
        stb.append(" WHERE ");
        stb.append("     TYPE = '" + type + "' ");
        stb.append(" AND VISIT_REASON2 IS NOT NULL ");
        stb.append(" GROUP BY ");
        stb.append("     VISIT_REASON2 ");
        stb.append(" UNION ALL ");
        stb.append(" SELECT ");
        stb.append("     VISIT_REASON3 AS VISIT_REASON, ");
        stb.append("     COUNT(SCHREGNO) AS CNT ");
        stb.append(" FROM ");
        stb.append("     VISITREC ");
        stb.append(" WHERE ");
        stb.append("     TYPE = '" + type + "' ");
        stb.append(" AND VISIT_REASON3 IS NOT NULL ");
        stb.append(" GROUP BY ");
        stb.append("     VISIT_REASON3 ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     VISIT_REASON, ");
        stb.append("     CNT ");
        stb.append(" FROM ");
        stb.append("     VISITREC_CNT ");
        stb.append(" ORDER BY ");
        stb.append("     VISIT_REASON ");

        return stb.toString();
    }

    private String getPopulationSql() {
        StringBuffer stb = new StringBuffer();

        stb.append("     SELECT  ");
        stb.append("         REGD.YEAR, ");
        stb.append("         REGD.SEMESTER, ");
        stb.append("         REGD.GRADE, ");
        stb.append("         REGD.HR_CLASS, ");
        stb.append("         REGD.SCHREGNO, ");
        stb.append("         VISITREC.TYPE, ");
        stb.append("         VISIT_REASON1, ");
        stb.append("         VISIT_REASON2, ");
        stb.append("         VISIT_REASON3 ");
        stb.append("     FROM ");
        stb.append("         SCHREG_REGD_DAT REGD ");
        stb.append("         INNER JOIN NURSEOFF_VISITREC_DAT VISITREC ");
        stb.append("                 ON VISITREC.SCHREGNO = REGD.SCHREGNO ");
        stb.append("     WHERE ");
        stb.append("         REGD.YEAR = '" + _param._ctrlYear + "' AND ");
        stb.append("         REGD.SEMESTER = '" + _param._ctrlSemester + "' AND ");
        stb.append("         VISITREC.VISIT_DATE BETWEEN TO_DATE('" + _param._sDate
                + "', 'YYYY/MM/DD') AND TO_DATE('" + _param._eDate + "', 'YYYY/MM/DD') ");
        stb.append("     ORDER BY ");
        stb.append("         REGD.GRADE, ");
        stb.append("         REGD.HR_CLASS, ");
        stb.append("         REGD.SCHREGNO, ");
        stb.append("         VISITREC.VISIT_DATE, ");
        stb.append("         VISITREC.VISIT_HOUR, ");
        stb.append("         VISITREC.VISIT_MINUTE, ");
        stb.append("         VISITREC.TYPE ");

        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final HttpServletRequest request, final DB2UDB db2) throws Exception {
        final Param param = new Param(request, db2);
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final String _schoolCd;
        private final String _schoolKind;
        private final String _sDate;
        private final String _eDate;
        private final String _gengou;
        private final String _schoolName;
        private Map<String, String> _nameMstNaika;
        private Map<String, String> _nameMstGeka;

        Param(final HttpServletRequest request, final DB2UDB db2) throws SQLException {
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _schoolCd = request.getParameter("SCHOOLCD");
            _schoolKind = request.getParameter("SCHOOLKIND");
            _sDate = request.getParameter("SDATE");
            _eDate = request.getParameter("EDATE");
            _gengou = KNJ_EditDate.dateGengou(db2, Integer.parseInt(_ctrlYear), 4, 1);
            _schoolName = getSchoolName(db2);
            _nameMstNaika = getNameMst(db2, NAME_MST_NAMECD1_NAIKA);
            _nameMstGeka = getNameMst(db2, NAME_MST_NAMECD1_GEKA);
        }

        private String getSchoolName(final DB2UDB db2) {
            StringBuffer stb = new StringBuffer();

            stb.append(" SELECT  ");
            stb.append("     SCHOOLNAME1 ");
            stb.append(" FROM ");
            stb.append("     SCHOOL_MST ");
            stb.append(" WHERE ");
            stb.append("     YEAR        = '" + _ctrlYear + "' AND ");
            stb.append("     SCHOOLCD    = '" + _schoolCd + "' AND ");
            stb.append("     SCHOOL_KIND = '" + _schoolKind + "'  ");

            String sql = stb.toString();
            log.debug(" sql = " + sql);

            String rtn = "";

            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    rtn = rs.getString("SCHOOLNAME1");
                }

                db2.commit();
            } catch (SQLException ex) {
                log.debug("Exception:", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }

            return rtn;
        }

        private Map<String, String> getNameMst(final DB2UDB db2, final String namecd1) {
            StringBuffer stb = new StringBuffer();

            stb.append(" SELECT  ");
            stb.append("     NAMECD2, ");
            stb.append("     NAME1 ");
            stb.append(" FROM ");
            stb.append("     V_NAME_MST ");
            stb.append(" WHERE ");
            stb.append("     YEAR    = '" + _ctrlYear + "' AND ");
            stb.append("     NAMECD1 = '" + namecd1 + "'  ");
            stb.append(" ORDER BY ");
            stb.append("     NAMECD2 ");

            String sql = stb.toString();
            log.debug(" sql = " + sql);

            Map rtnMap = new TreeMap<String, String>();

            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    String namecd2 = rs.getString("NAMECD2");
                    String name1 = rs.getString("NAME1");

                    rtnMap.put(namecd2, name1);
                }

                db2.commit();
            } catch (SQLException ex) {
                log.debug("Exception:", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }

            return rtnMap;
        }
    }
}

// eof
