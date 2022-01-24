// kanji=漢字
/*
 * $Id: a3f1a6673972e59d11a7da3d970ea59c941dca1c $
 *
 * 作成日: 2009/04/10 18:40:23 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2014 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJWP;

import java.io.IOException;

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
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: a3f1a6673972e59d11a7da3d970ea59c941dca1c $
 */
public class KNJWP133 {

    private static final Log log = LogFactory.getLog("KNJWP133.class");

    private static final String FORM_NAME = "KNJWP133.frm";
    private boolean _hasData;

    Param _param;
    /**
     * KNJW.classから呼ばれる処理
     * @param request リクエスト
     * @param response レスポンス
     * @throws Exception I/O例外
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

    private void closeDb(final DB2UDB db2) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
    }
    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        svf.VrSetForm(FORM_NAME, 4);

        svf.VrsOut("FROM_PAYDAY", _param._printFromPayDay);
        svf.VrsOut("TO_PAYDAY", _param._printToPayDay);
        svf.VrsOut("LOGINDATE", KNJ_EditDate.h_format_JP(_param._loginDate));
        final String cancelSql = getCancelSql();
        ResultSet rs = null;
        try {
            db2.query(cancelSql);
            rs = db2.getResultSet();
            while (rs.next()) {
                svf.VrsOut("PAYDAY", rs.getString("PAYMENT_DATE").replace('-', '/'));
                svf.VrsOut("CANCELDAY", rs.getString("CANCEL_DATE").replace('-', '/'));
                final String paymentDiv = getPaymentDiv(rs.getString("PAYMENT_DIV"));
                svf.VrsOut("PAYMENT_DIV", paymentDiv);
                svf.VrsOut("SCHOOLNAME1", rs.getString("SCHOOLNAME1"));
                svf.VrsOut("APPLICANTNO", rs.getString("APPLICANTNO"));
                svf.VrsOut("SCHREGNO", rs.getString("SCHREGNO"));
                final int nameField = rs.getString("NAME").length() > 10 ? 2 : 1;
                svf.VrsOut("NAME" + nameField, rs.getString("NAME"));
                svf.VrsOut("PAYMENT_MONEY", rs.getString("PAYMENT_MONEY"));
                final int payStaffField = rs.getString("PAY_STAFF").length() > 10 ? 2 : 1;
                svf.VrsOut("PAY_STAFF" + payStaffField, rs.getString("PAY_STAFF"));
                final int cancelStaffField = rs.getString("CANCEL_STAFF").length() > 10 ? 2 : 1;
                svf.VrsOut("CANCEL_STAFF" + cancelStaffField, rs.getString("CANCEL_STAFF"));
                _hasData = true;
                svf.VrEndRecord();
            }
        } finally {
            db2.commit();
            DbUtils.closeQuietly(rs);
        }
    }

    private String getPaymentDiv(final String div) {
        if (div.equals("01")) {
            return "仮想口座";
        } else if (div.equals("02")) {
            return "信販";
        } else if (div.equals("03")) {
            return "現金";
        } else if (div.equals("04")) {
            return "その他";
        }
        return "";
    }

    private String getCancelSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.APPLICANTNO, ");
        stb.append("     T1.PAYMENT_DATE, ");
        stb.append("     T1.PAYMENT_DIV, ");
        stb.append("     T1.PAYMENT_MONEY, ");
        stb.append("     T1.CANCEL_REGISTERCD, ");
        stb.append("     L5.STAFFNAME AS PAY_STAFF, ");
        stb.append("     DATE(T1.CANCEL_UPDATED) AS CANCEL_DATE, ");
        stb.append("     T1.REGISTERCD, ");
        stb.append("     L6.STAFFNAME AS CANCEL_STAFF, ");
        stb.append("     L1.SCHREGNO, ");
        stb.append("     CASE WHEN L2.NAME IS NOT NULL ");
        stb.append("          THEN L2.NAME ");
        stb.append("          ELSE L1.NAME ");
        stb.append("     END AS NAME, ");
        stb.append("     L3.GRADE, ");
        stb.append("     L4.SCHOOLNAME1 ");
        stb.append(" FROM ");
        stb.append("     PAYMENT_MONEY_CANCEL_DAT T1 ");
        stb.append("     LEFT JOIN APPLICANT_BASE_MST L1 ON T1.APPLICANTNO = L1.APPLICANTNO ");
        stb.append("     LEFT JOIN SCHREG_BASE_MST L2 ON L1.SCHREGNO = L2.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_REGD_DAT L3 ON CASE WHEN MONTH(T1.PAYMENT_DATE) < 4 ");
        stb.append("                                          THEN YEAR(T1.PAYMENT_DATE) - 1 ");
        stb.append("                                          ELSE YEAR(T1.PAYMENT_DATE) ");
        stb.append("                                     END = INT(L3.YEAR) ");
        stb.append("          AND L3.SEMESTER = '" + _param._semester + "' ");
        stb.append("          AND L1.SCHREGNO = L3.SCHREGNO ");
        stb.append("     LEFT JOIN BELONGING_MST L4 ON L3.GRADE = L4.BELONGING_DIV ");
        stb.append("     LEFT JOIN STAFF_MST L5 ON T1.CANCEL_REGISTERCD = L5.STAFFCD ");
        stb.append("     LEFT JOIN STAFF_MST L6 ON T1.REGISTERCD = L6.STAFFCD ");
        stb.append(" WHERE ");
        stb.append("     T1.PAYMENT_DATE BETWEEN '" + _param._fromPayDay + "' AND '" + _param._toPayDay + "' ");
        stb.append(" ORDER BY ");
        stb.append("     T1.APPLICANTNO, ");
        stb.append("     T1.PAYMENT_DATE ");

        log.debug(stb);
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
        private final String _fromPayDay;
        private final String _printFromPayDay;
        private final String _toPayDay;
        private final String _printToPayDay;
        private final String _year;
        private final String _semester;
        private final String _loginDate;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _fromPayDay = request.getParameter("FROM_PAYDAY").replace('/', '-');
            _printFromPayDay = KNJ_EditDate.h_format_JP(_fromPayDay);
            _toPayDay = request.getParameter("TO_PAYDAY").replace('/', '-');
            _printToPayDay = KNJ_EditDate.h_format_JP(_toPayDay);
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _loginDate = request.getParameter("LOGIN_DATE");
        }
    }

}
 // KNJWP133

// eof
