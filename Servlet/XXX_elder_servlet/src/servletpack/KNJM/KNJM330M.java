/*
 * $Id: 4d9d6aeb2eb39a053532a3057198270b01ae92be $
 *
 * 作成日: 2012/11/21
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJM;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;

/**
 * レポート提出記録カード
 */
public class KNJM330M {

    private static final Log log = LogFactory.getLog(KNJM330M.class);

    private static DecimalFormat df2 = new DecimalFormat("00");

    private boolean _hasData;

    private Param _param;

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
            response.setContentType("application/pdf");

            svf.VrInit();
            svf.VrSetSpoolFileStream(response.getOutputStream());

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
            if (null != db2) {
                db2.commit();
                db2.close();
            }
            svf.VrQuit();
        }
    }
    
    private static int getMS932Length(final String s) {
        if (null != s) {
            try {
                return s.getBytes("MS932").length;
            } catch (Exception e) {
                log.fatal("exception!", e);
            }
        }
        return 0;
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            final String sql = sql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            
            int n = 0;
            String oldschregno = null;
            while (rs.next()) {
                final String schregno = rs.getString("SCHREGNO");
                if (null == oldschregno || !oldschregno.equals(schregno) || n >= 6) {
                    if (_hasData) {
                        svf.VrEndPage();
                    }
                    svf.VrSetForm("KNJM330M.frm", 1);
                    n = 0;
                }
                n += 1;
                final int row = (n + 1) / 2;
                final int col = n % 2 == 0 ? 2 : 1;
                
                svf.VrsOutn("SUBCLASS_CD" + col + "_1", row, rs.getString("SUBCLASSCD"));
                if (getMS932Length(rs.getString("SUBCLASSNAME")) > 20) {
                    svf.VrsOutn("SUBCLASS_NAME" + col + "_3", row, rs.getString("SUBCLASSNAME"));
                } else if (getMS932Length(rs.getString("SUBCLASSNAME")) > 14) {
                    svf.VrsOutn("SUBCLASS_NAME" + col + "_2", row, rs.getString("SUBCLASSNAME"));
                } else {
                    svf.VrsOutn("SUBCLASS_NAME" + col + "_1", row, rs.getString("SUBCLASSNAME"));
                }
                if (null != rs.getString("STANDARD_DATE")) {
                    svf.VrsOutn("LIMIT_DATE" + col, row, "提出基準日 " + rs.getString("STANDARD_DATE").replace('-', '/'));
                }
                if (schregno.length() > 4) {
                    // 前4桁、前0表示無し
                    svf.VrsOutn("YEAR" + col + "_1", row, String.valueOf(Integer.parseInt(schregno.substring(0, 4))));
                }
                if (schregno.length() > 4) {
                    // 下4桁、前0表示無し
                    svf.VrsOutn("NO" + col + "_1", row, String.valueOf(Integer.parseInt(schregno.substring(schregno.length() - 4))));
                }
                int standardSeq = rs.getInt("STANDARD_SEQ");
                svf.VrsOutn("TIMES" + col + "_1", row, String.valueOf(standardSeq));
                if (getMS932Length(rs.getString("NAME")) > 30) {
                    svf.VrsOutn("NAME" + col + "_4", row, rs.getString("NAME"));
                } else if (getMS932Length(rs.getString("NAME")) > 20) {
                    svf.VrsOutn("NAME" + col + "_3", row, rs.getString("NAME"));
                } else if (getMS932Length(rs.getString("NAME")) > 14) {
                    svf.VrsOutn("NAME" + col + "_2", row, rs.getString("NAME"));
                } else {
                    svf.VrsOutn("NAME" + col + "_1", row, rs.getString("NAME"));
                }

                final String curriculumCd = rs.getString("CURRICULUM_CD");
                final String subclassCd = rs.getString("SUBCLASSCD");
                final String barcode = schregno + _param._year.substring(_param._year.length() - 1) + curriculumCd + subclassCd + df2.format(standardSeq);
                svf.VrsOutn("BAR" + col + "_1", row, barcode);
                
                final String d1 = df2.format(rs.getInt("NENREI"));
                final String d2 = df2.format(rs.getInt("SUBCLASS_STD_CREDIT"));
                final String d3 = df2.format(rs.getInt("BEFORE_CREDIT") + rs.getInt("SUBCLASS_STD_CREDIT") + rs.getInt("SATEI_TANNI"));
                svf.VrsOutn("REP_NO" + col, row, d1 + d2 + d3);
                _hasData = true;
                oldschregno = schregno;
            }
            
        } catch (final SQLException ex) {
            log.fatal("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        if (_hasData) {
            svf.VrEndPage();
        }
    }

    public String sql() {
        final StringBuffer stb = new StringBuffer();
        
        stb.append(" WITH STUDYREC_BEFORE AS ( ");
        stb.append("   SELECT ");
        stb.append("       SCHREGNO, SUM(VALUE(GET_CREDIT, 0)) + SUM(VALUE(ADD_CREDIT, 0)) AS CREDIT");
        stb.append("   FROM ");
        stb.append("       SCHREG_STUDYREC_DAT ");
        stb.append("   WHERE ");
        stb.append("       YEAR < '" + _param._year + "' ");
        stb.append("       AND SCHOOLCD <> '1' ");
        stb.append("   GROUP BY ");
        stb.append("       SCHREGNO ");
        stb.append(" ), SUBCLASS_STD0 AS ( ");
        stb.append("   SELECT ");
        stb.append("       T1.SCHREGNO, SUM(T3.CREDITS) AS CREDITS");
        stb.append("   FROM ");
        stb.append("       SUBCLASS_STD_SELECT_DAT T1 ");
        stb.append("       INNER JOIN SCHREG_REGD_DAT T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("           AND T2.YEAR = T1.YEAR ");
        stb.append("           AND T2.SEMESTER = '" + _param._semester + "' ");
        stb.append("       INNER JOIN CREDIT_MST T3 ON T3.YEAR = T1.YEAR ");
        stb.append("           AND T3.COURSECD = T2.COURSECD ");
        stb.append("           AND T3.MAJORCD = T2.MAJORCD ");
        stb.append("           AND T3.GRADE = T2.GRADE ");
        stb.append("           AND T3.COURSECODE = T2.COURSECODE ");
        stb.append("           AND T3.CLASSCD = T1.CLASSCD ");
        stb.append("           AND T3.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("           AND T3.CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("           AND T3.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("   WHERE ");
        stb.append("       T1.YEAR = '" + _param._year + "' ");
        stb.append("       AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append("   GROUP BY ");
        stb.append("       T1.SCHREGNO ");
        stb.append(" ), KOUGAI1 AS ( ");
        stb.append("     SELECT ");
        stb.append("         SUM(T6.CREDITS) AS CREDITS, "); // 高認の場合、単位マスタの単位
        stb.append("         T1.SCHREGNO ");
        stb.append("     FROM ");
        stb.append("         SCH_COMP_DETAIL_DAT T1 ");
        stb.append("     LEFT JOIN SCHREG_REGD_DAT T5 ON T5.SCHREGNO = T1.SCHREGNO ");
        stb.append("         AND T5.YEAR = T1.YEAR ");
        stb.append("         AND T5.SEMESTER = '" + _param._semester + "' ");
        stb.append("     LEFT JOIN CREDIT_MST T6 ON T6.YEAR = T1.YEAR ");
        stb.append("         AND T6.COURSECD = T5.COURSECD ");
        stb.append("         AND T6.GRADE = T5.GRADE ");
        stb.append("         AND T6.MAJORCD = T5.MAJORCD ");
        stb.append("         AND T6.COURSECODE = T5.COURSECODE ");
        stb.append("         AND T6.CLASSCD = T1.CLASSCD ");
        stb.append("         AND T6.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("         AND T6.CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("         AND T6.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("     WHERE ");
        stb.append("         T1.YEAR = '" + _param._year + "' ");
        stb.append("         AND T1.CLASSCD <= '90' ");
        stb.append("         AND T1.KOUNIN = '1' ");
        stb.append("     GROUP BY ");
        stb.append("         T1.SCHREGNO ");
        stb.append(" ), KOUGAI2 AS ( ");
        stb.append("     SELECT ");
        stb.append("         SUM(T1.ADD_CREDIT) AS CREDITS, "); // 増単の場合、ADD_CREDIT
        stb.append("         T1.SCHREGNO ");
        stb.append("     FROM ");
        stb.append("         SCH_COMP_DETAIL_DAT T1 ");
        stb.append("     WHERE ");
        stb.append("         T1.YEAR = '" + _param._year + "' ");
        stb.append("         AND T1.CLASSCD <= '90' ");
        stb.append("         AND T1.KOUNIN IS NULL ");
        stb.append("         AND T1.ADD_CREDIT IS NOT NULL ");
        stb.append("     GROUP BY ");
        stb.append("         T1.SCHREGNO ");
        stb.append(" ), SUBCLASS_STD AS ( ");
        stb.append("   SELECT ");
        stb.append("       T1.SCHREGNO, SUM(VALUE(T2.CREDITS, 0) + VALUE(T3.CREDITS, 0) + VALUE(T4.CREDITS, 0)) AS CREDIT");
        stb.append("   FROM ");
        stb.append("       (");
        stb.append("        SELECT SCHREGNO FROM SUBCLASS_STD0 ");
        stb.append("        UNION ");
        stb.append("        SELECT SCHREGNO FROM KOUGAI1 ");
        stb.append("        UNION ");
        stb.append("        SELECT SCHREGNO FROM KOUGAI2");
        stb.append("       ) T1 ");
        stb.append("       LEFT JOIN SUBCLASS_STD0 T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("       LEFT JOIN KOUGAI1 T3 ON T3.SCHREGNO = T1.SCHREGNO ");
        stb.append("       LEFT JOIN KOUGAI2 T4 ON T4.SCHREGNO = T1.SCHREGNO ");
        stb.append("   GROUP BY ");
        stb.append("       T1.SCHREGNO ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     T1.YEAR, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     NMM002.NAME1 AS RECPORTDIV_NAME, ");
        stb.append("     T4.CURRICULUM_CD, ");
        stb.append("     T4.SUBCLASSCD, ");
        stb.append("     T6.SUBCLASSNAME, ");
        stb.append("     T4.STANDARD_DATE, ");
        stb.append("     T4.STANDARD_SEQ, ");
        stb.append("     T7.NAME, ");
        stb.append("     CASE WHEN T7.BIRTHDAY IS NULL THEN 0 ELSE MOD(" + _param._year + " - INT(FISCALYEAR(T7.BIRTHDAY)) - 1, 100) END AS NENREI, ");
        stb.append("     VALUE(T8.CREDIT, 0) AS BEFORE_CREDIT, ");
        stb.append("     VALUE(T9.CREDIT, 0) AS SUBCLASS_STD_CREDIT, ");
        stb.append("     INT(VALUE(T10.BASE_REMARK1, '0')) AS SATEI_TANNI ");
        stb.append(" FROM CHAIR_STD_DAT T1 ");
        stb.append(" INNER JOIN CHAIR_DAT T2 ON T2.YEAR = T1.YEAR ");
        stb.append("     AND T2.SEMESTER = T1.SEMESTER ");
        stb.append("     AND T2.CHAIRCD = T1.CHAIRCD ");
        stb.append(" INNER JOIN CHAIR_CORRES_DAT T3 ON T3.YEAR = T2.YEAR ");
        stb.append("     AND T3.CHAIRCD = T2.CHAIRCD ");
        stb.append("     AND T3.CLASSCD = T2.CLASSCD ");
        stb.append("     AND T3.SCHOOL_KIND = T2.SCHOOL_KIND ");
        stb.append("     AND T3.CURRICULUM_CD = T2.CURRICULUM_CD ");
        stb.append("     AND T3.SUBCLASSCD = T2.SUBCLASSCD ");
        stb.append(" INNER JOIN SCHREG_REGD_DAT T5 ON T5.SCHREGNO = T1.SCHREGNO ");
        stb.append("     AND T5.YEAR = T1.YEAR ");
        stb.append("     AND T5.SEMESTER = T1.SEMESTER ");

        if ("1".equals(_param._useRepStandarddateCourseDat)) {
            stb.append(" INNER JOIN REP_STANDARDDATE_COURSE_DAT T4 ON T4.YEAR = T2.YEAR  ");
            stb.append("     AND T4.CLASSCD = T2.CLASSCD ");
            stb.append("     AND T4.SCHOOL_KIND = T2.SCHOOL_KIND ");
            stb.append("     AND T4.CURRICULUM_CD = T2.CURRICULUM_CD ");
            stb.append("     AND T4.SUBCLASSCD = T2.SUBCLASSCD ");
            stb.append("     AND T4.COURSECD = T5.COURSECD ");
            stb.append("     AND T4.MAJORCD = T5.MAJORCD ");
            stb.append("     AND T4.COURSECODE = T5.COURSECODE ");
            stb.append("     AND T4.STANDARD_SEQ <= T3.REP_START_SEQ + T3.REP_SEQ_ALL - 1 ");
        } else {
            stb.append(" INNER JOIN REP_STANDARDDATE_DAT T4 ON T4.YEAR = T2.YEAR  ");
            stb.append("     AND T4.CLASSCD = T2.CLASSCD ");
            stb.append("     AND T4.SCHOOL_KIND = T2.SCHOOL_KIND ");
            stb.append("     AND T4.CURRICULUM_CD = T2.CURRICULUM_CD ");
            stb.append("     AND T4.SUBCLASSCD = T2.SUBCLASSCD ");
            stb.append("     AND T4.STANDARD_SEQ <= T3.REP_START_SEQ + T3.REP_SEQ_ALL - 1 ");
        }

        stb.append(" LEFT JOIN NAME_MST NMM002 ON NMM002.NAMECD1 = 'M002' ");
        stb.append("     AND NMM002.NAMECD2 = T4.REPORTDIV ");
        stb.append(" INNER JOIN SUBCLASS_MST T6 ON T6.CLASSCD = T2.CLASSCD ");
        stb.append("     AND T6.SCHOOL_KIND = T2.SCHOOL_KIND ");
        stb.append("     AND T6.CURRICULUM_CD = T2.CURRICULUM_CD ");
        stb.append("     AND T6.SUBCLASSCD = T2.SUBCLASSCD ");
        stb.append(" INNER JOIN SCHREG_BASE_MST T7 ON T7.SCHREGNO = T1.SCHREGNO ");
        stb.append(" LEFT JOIN STUDYREC_BEFORE T8 ON T8.SCHREGNO = T1.SCHREGNO ");
        stb.append(" LEFT JOIN SUBCLASS_STD T9 ON T9.SCHREGNO = T1.SCHREGNO ");
        stb.append(" LEFT JOIN SCHREG_BASE_DETAIL_MST T10 ON T10.SCHREGNO = T1.SCHREGNO ");
        stb.append("     AND T10.BASE_SEQ = '004' ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
        if ("2".equals(_param._output)) {
            stb.append("     AND T1.SCHREGNO >= '" + _param._sSchregno  + "' ");
            stb.append("     AND T1.SCHREGNO <= '" + _param._eSchregno  + "' ");
        } else {
            stb.append("     AND T5.GRADE || T5.HR_CLASS = '" + _param._gradeHrclass  + "' ");
        }
        if (!StringUtils.isBlank(_param._standardSeq)) {
            stb.append("     AND T4.STANDARD_SEQ = " + _param._standardSeq  + " ");
        }
        if (!StringUtils.isBlank(_param._subclassCd)) {
            stb.append("     AND T4.CLASSCD || '-' || T4.SCHOOL_KIND || '-' || T4.CURRICULUM_CD || '-' || T4.SUBCLASSCD = '" + _param._subclassCd  + "' ");
        }
        stb.append(" ORDER BY ");
        stb.append("     T1.SCHREGNO, T3.CLASSCD, T3.SCHOOL_KIND, T3.CURRICULUM_CD, T3.SUBCLASSCD, T4.STANDARD_SEQ, T4.STANDARD_DATE ");
        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 74257 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _year;
        final String _semester;
        final String _gradeHrclass;
        final String _output;
        final String _sSchregno;
        final String _eSchregno;
        final String _standardSeq;
        final String _subclassCd;
        final String _useRepStandarddateCourseDat;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _gradeHrclass = request.getParameter("GRADE_HR_CLASS");
            _output = request.getParameter("OUTPUT");
            _sSchregno = request.getParameter("S_SCHREGNO");
            _eSchregno = request.getParameter("E_SCHREGNO");
            _standardSeq = request.getParameter("TKAISU");
            _subclassCd = request.getParameter("KAMOKU");
            _useRepStandarddateCourseDat = request.getParameter("useRepStandarddateCourseDat");
        }
    }
}

// eof

