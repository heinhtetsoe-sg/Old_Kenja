/*
 * $Id: 841e823b88a47511f53103350953b173535626f8 $
 *
 * 作成日: 2010/10/07
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJP;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
*
*  学校教育システム 賢者 [校納金管理]
*
*                  ＜ＫＮＪＰ３４０Ｋ＞  授業料軽減補助金調整対象者一覧
*/

public class KNJP340K {

    private static final Log log = LogFactory.getLog(KNJP340K.class);
    private static final String TYOUSEI = "1";
    private static final String GAKKOUFUTAN = "2";

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
            response.setContentType("application/pdf");

            svf.VrInit();
            svf.VrSetSpoolFileStream(response.getOutputStream());

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);

            _hasData = false;

            printMain(db2, svf);

        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            if (null != db2) {
                db2.commit();
                db2.close();
            }
            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
            svf.VrQuit();
        }
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrSetForm("KNJP340K.frm", 4);
        
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = TYOUSEI.equals(_param._printDiv) ? getReductionAdjustmentSql() : getGakkouFutanSql();
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                svf.VrsOut("NENDO", KNJ_EditDate.h_format_JP_N(_param._ctrlYear + "-04-01") + "度");
                final String title = TYOUSEI.equals(_param._printDiv) ? "　授業料軽減補助調整金対象者一覧" : "　授業料学校負担金対象者一覧";
                svf.VrsOut("TITLE", title);
                final String header = TYOUSEI.equals(_param._printDiv) ? "差額" : "学校負担金";
                svf.VrsOut("HEADER", header);
                svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._ctrlDate));
                svf.VrsOut("HR_NAME", (rs.getString("HR_NAME") == null ? "" : rs.getString("HR_NAME")) + (rs.getString("ATTENDNO") == null ? "" : " - " + rs.getString("ATTENDNO")));
                svf.VrsOut("NAME", rs.getString("NAME"));
                svf.VrsOut("GUARDIANNAME", rs.getString("GUARANTOR_NAME"));
                svf.VrsOut("PREF", rs.getString("PREF"));
                svf.VrsOut("TOTAL_REDUCTION", rs.getString("MONEY_SUM"));
                svf.VrsOut("PREF_MONEY", rs.getString("REDUCTIONMONEY"));
                svf.VrsOut("COUNTRY_MONEY", rs.getString("REDUCTION_COUNTRY_MONEY"));
                svf.VrsOut("TUITION", rs.getString("TOTAL_LESSON_MONEY"));
                svf.VrsOut("DIFFERENCE", rs.getString("ADJUSTMENT_MONEY"));
                svf.VrEndRecord();
                _hasData = true;
            }
        } catch (SQLException ex) {
            log.error("exception!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }

    private String getReductionAdjustmentSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T4.HR_NAME, ");
        stb.append("     T3.ATTENDNO, ");
        stb.append("     T2.NAME, ");
        stb.append("     T5.GUARANTOR_NAME, ");
        stb.append("     T6.PREF, ");
        stb.append("     T1.TOTAL_LESSON_MONEY, ");
        stb.append("     VALUE(T1.TOTAL_REDUCTIONMONEY, 0) + VALUE(T1.TOTAL_REDUCTION_COUNTRY_MONEY, 0) AS MONEY_SUM, ");
        stb.append("     T1.TOTAL_REDUCTIONMONEY AS REDUCTIONMONEY, ");
        stb.append("     T1.TOTAL_REDUCTION_COUNTRY_MONEY AS REDUCTION_COUNTRY_MONEY, ");
        stb.append("     T1.TOTAL_ADJUSTMENT_MONEY AS ADJUSTMENT_MONEY ");
        stb.append(" FROM ");
        stb.append("     REDUCTION_ADJUSTMENT_DAT T1 ");
        stb.append("     INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_REGD_DAT T3 ON T3.SCHREGNO = T1.SCHREGNO ");
        stb.append("         AND T3.YEAR = T1.YEAR ");
        stb.append("         AND T3.SEMESTER = '" + _param._ctrlSemester + "' ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT T4 ON T4.YEAR = T3.YEAR ");
        stb.append("         AND T4.SEMESTER = T3.SEMESTER ");
        stb.append("         AND T4.GRADE = T3.GRADE ");
        stb.append("         AND T4.HR_CLASS = T3.HR_CLASS ");
        stb.append("     LEFT JOIN GUARDIAN_DAT T5 ON T5.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN (  ");
        stb.append("         SELECT  ");
        stb.append("             ZIP.NEW_ZIPCD,  ");
        stb.append("             MAX(ZIP.PREF) AS PREF  ");
        stb.append("         FROM  ");
        stb.append("             ZIPCD_MST ZIP  ");
        stb.append("         GROUP BY ZIP.NEW_ZIPCD  ");
        stb.append("     ) T6 ON T5.GUARANTOR_ZIPCD = T6.NEW_ZIPCD  ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._ctrlYear + "' ");
        stb.append(" ORDER BY ");
        stb.append("     T3.GRADE, T3.HR_CLASS, T3.ATTENDNO ");
        return stb.toString();
    }

    private String getGakkouFutanSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH MONEY_DUE_T AS ( ");
        stb.append(" SELECT ");
        stb.append("     SCHREGNO, ");
        stb.append("     SUM(MONEY_DUE) AS MONEY_DUE ");
        stb.append(" FROM ");
        stb.append("     MONEY_DUE_M_DAT ");
        stb.append(" WHERE ");
        stb.append("    YEAR = '" + _param._ctrlYear + "' ");
        stb.append("    AND EXPENSE_M_CD IN ('11', '12', '13') ");
        stb.append(" GROUP BY ");
        stb.append("     SCHREGNO ");
        stb.append(" ), REDUC_T AS ( ");
        stb.append(" SELECT ");
        stb.append("     SCHREGNO, ");
        stb.append("     SUM(CASE WHEN REDUC_DEC_FLG_1 = '1' ");
        stb.append("              THEN VALUE(REDUCTIONMONEY_1, 0) ");
        stb.append("              ELSE 0 ");
        stb.append("         END ");
        stb.append("         + ");
        stb.append("         CASE WHEN REDUC_DEC_FLG_2 = '1' ");
        stb.append("              THEN VALUE(REDUCTIONMONEY_2, 0) ");
        stb.append("              ELSE 0 ");
        stb.append("         END ");
        stb.append("     ) AS REDUCTIONMONEY ");
        stb.append(" FROM ");
        stb.append("     REDUCTION_DAT ");
        stb.append(" WHERE ");
        stb.append("    YEAR = '" + _param._ctrlYear + "' ");
        stb.append("    AND (REDUC_DEC_FLG_1 = '1' ");
        stb.append("         OR ");
        stb.append("         REDUC_DEC_FLG_2 = '1') ");
        stb.append(" GROUP BY ");
        stb.append("     SCHREGNO ");
        stb.append(" ), REDUC_COUNTRY_T AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     SUM(CASE WHEN VALUE(T1.PLAN_CANCEL_FLG, '0') = '0' ");
        stb.append("              THEN VALUE(T1.PLAN_MONEY, 0) ");
        stb.append("              ELSE 0 ");
        stb.append("         END ");
        stb.append("         + ");
        stb.append("         CASE WHEN VALUE(T1.ADD_PLAN_CANCEL_FLG, '0') = '0' ");
        stb.append("              THEN VALUE(T1.ADD_PLAN_MONEY, 0) ");
        stb.append("              ELSE 0 ");
        stb.append("         END ");
        stb.append("     ) AS REDUCTION_C_MONEY ");
        stb.append(" FROM ");
        stb.append("     REDUCTION_COUNTRY_PLAN_DAT T1 ");
        stb.append(" WHERE ");
        stb.append("    T1.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("    AND (VALUE(T1.PLAN_CANCEL_FLG, '0') = '0' ");
        stb.append("         OR ");
        stb.append("         VALUE(T1.ADD_PLAN_CANCEL_FLG, '0') = '0') ");
        stb.append(" GROUP BY ");
        stb.append("     T1.SCHREGNO ");

        stb.append(" ), OVER_MONEY AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     VALUE(L1.MONEY_DUE, 0) AS TOTAL_LESSON_MONEY, ");
        stb.append("     VALUE(L2.REDUCTIONMONEY, 0) + VALUE(L3.REDUCTION_C_MONEY, 0) AS MONEY_SUM, ");
        stb.append("     VALUE(L2.REDUCTIONMONEY, 0) AS REDUCTIONMONEY, ");
        stb.append("     VALUE(L3.REDUCTION_C_MONEY, 0) AS REDUCTION_COUNTRY_MONEY, ");
        stb.append("     VALUE(T1.TOTAL_BURDEN_CHARGE, 0) AS ADJUSTMENT_MONEY ");
        stb.append(" FROM ");
        stb.append("     REDUCTION_BURDEN_CHARGE_DAT T1 ");
        stb.append("     LEFT JOIN MONEY_DUE_T L1 ON T1.SCHREGNO = L1.SCHREGNO ");
        stb.append("     LEFT JOIN REDUC_T L2 ON T1.SCHREGNO = L2.SCHREGNO ");
        stb.append("     LEFT JOIN REDUC_COUNTRY_T L3 ON T1.SCHREGNO = L3.SCHREGNO ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._ctrlYear + "' ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     t5.GUARANTOR_NAME, ");
        stb.append("     t3.HR_NAME, ");
        stb.append("     t2.ATTENDNO, ");
        stb.append("     t2.GRADE || t2.HR_CLASS || t2.ATTENDNO AS GRD_CLASS, ");
        stb.append("     t4.NAME, ");
        stb.append("     T6.PREF, ");
        stb.append("     VALUE(OVER_M.TOTAL_LESSON_MONEY, 0) AS TOTAL_LESSON_MONEY, ");
        stb.append("     VALUE(OVER_M.MONEY_SUM, 0) AS MONEY_SUM, ");
        stb.append("     VALUE(OVER_M.REDUCTIONMONEY, 0) AS REDUCTIONMONEY, ");
        stb.append("     VALUE(OVER_M.REDUCTION_COUNTRY_MONEY, 0) AS REDUCTION_COUNTRY_MONEY, ");
        stb.append("     VALUE(OVER_M.ADJUSTMENT_MONEY, 0) AS ADJUSTMENT_MONEY ");
        stb.append(" FROM ");
        stb.append("     OVER_MONEY OVER_M ");
        stb.append("     INNER JOIN SCHREG_REGD_DAT t2 ON t2.SCHREGNO = OVER_M.SCHREGNO ");
        stb.append("           AND t2.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("           AND t2.SEMESTER = '" + _param._ctrlSemester + "' ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT t3 ON t3.GRADE || t3.HR_CLASS = t2.GRADE || t2.HR_CLASS ");
        stb.append("          AND t3.YEAR = t2.YEAR ");
        stb.append("          AND t3.SEMESTER = t2.SEMESTER ");
        stb.append("     LEFT JOIN SCHREG_BASE_MST t4 ON t4.SCHREGNO = OVER_M.SCHREGNO ");
        stb.append("     INNER JOIN GUARDIAN_DAT t5 ON t5.SCHREGNO = OVER_M.SCHREGNO ");
        stb.append("     LEFT JOIN (  ");
        stb.append("         SELECT  ");
        stb.append("             ZIP.NEW_ZIPCD,  ");
        stb.append("             MAX(ZIP.PREF) AS PREF  ");
        stb.append("         FROM  ");
        stb.append("             ZIPCD_MST ZIP  ");
        stb.append("         GROUP BY ZIP.NEW_ZIPCD  ");
        stb.append("     ) T6 ON T5.GUARANTOR_ZIPCD = T6.NEW_ZIPCD  ");
        stb.append(" ORDER BY ");
        stb.append("     GRD_CLASS ");

        return stb.toString();
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
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final String _printDiv;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _printDiv = request.getParameter("PRINT_DIV");
        }
    }
}

// eof

