/*
 * $Id: 1fdbd0faaf3534e971b928a1841c06c80436b34e $
 *
 * 作成日: 2017/01/23
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

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

public class KNJP717 {

    private static final Log log = LogFactory.getLog(KNJP717.class);

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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrSetForm("KNJP717.frm", 1);

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final String sql = sql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            final String nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_param._year)) + "年度 ";
            int fieldNo = 1;
            int totalMoney = 0;
            while (rs.next()) {
                svf.VrsOutn("RECEPT_NO", 1, _param._slipNo);
                svf.VrsOutn("RECEPT_NO", 2, _param._slipNo);
                svf.VrsOutn("TITLE", 1, nendo + "　授業料諸経費納付書");
                svf.VrsOutn("TITLE", 2, nendo + "　授業料諸経費納付書");
                svf.VrsOutn("SCHREG_NO", 1, rs.getString("SCHREGNO"));
                svf.VrsOutn("SCHREG_NO", 2, rs.getString("SCHREGNO"));
                svf.VrsOutn("HR_NAME", 1, rs.getString("HR_NAME"));
                svf.VrsOutn("HR_NAME", 2, rs.getString("HR_NAME"));
                svf.VrsOutn("STAMP_TITLE", 1, "納付確認印欄");
                svf.VrsOutn("STAMP_TITLE", 2, "領収印欄");
                final String nameField = getMS932Bytecount(rs.getString("NAME")) > 30 ? "2" : "1";
                svf.VrsOutn("NAME" + nameField, 1, rs.getString("NAME"));
                svf.VrsOutn("NAME" + nameField, 2, rs.getString("NAME"));
                svf.VrsOutn("MONEY_TITLE1", 1, "授業料");
                svf.VrsOutn("MONEY_TITLE1", 2, "授業料");
                svf.VrsOutn("MONEY_TITLE2", 1, "諸経費内訳");
                svf.VrsOutn("MONEY_TITLE2", 2, "諸経費内訳");
                final int setMoney = rs.getInt("COLLECT_CNT") * rs.getInt("MONEY_DUE");
                if ("1".equals(rs.getString("GAKUNOKIN_DIV"))) {
                    svf.VrsOutn("DETAIL1", 1, rs.getString("COLLECT_M_NAME") + "　" + rs.getString("COLLECT_CNT") + "単位×" + rs.getString("MONEY_DUE") + "円");
                    svf.VrsOutn("DETAIL1", 2, rs.getString("COLLECT_M_NAME") + "　" + rs.getString("COLLECT_CNT") + "単位×" + rs.getString("MONEY_DUE") + "円");
                    svf.VrsOutn("MONEY1", 1, String.valueOf(setMoney));
                    svf.VrsOutn("MONEY1", 2, String.valueOf(setMoney));
                } else {
                    svf.VrsOutn("DETAIL2_" + fieldNo, 1, rs.getString("COLLECT_M_NAME"));
                    svf.VrsOutn("DETAIL2_" + fieldNo, 2, rs.getString("COLLECT_M_NAME"));
                    svf.VrsOutn("MONEY2_" + fieldNo, 1, String.valueOf(setMoney));
                    svf.VrsOutn("MONEY2_" + fieldNo, 2, String.valueOf(setMoney));
                    fieldNo++;
                }
                totalMoney += setMoney;
                svf.VrsOutn("TOTAL_MONEY", 1, String.valueOf(totalMoney));
                svf.VrsOutn("TOTAL_MONEY", 2, String.valueOf(totalMoney));

                _hasData = true;
            }

            if (_hasData) {
                svf.VrEndPage();
            }
            db2.commit();
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
    }

    private static int getMS932Bytecount(String str) {
        int count = 0;
        if (null != str) {
            try {
                count = str.getBytes("MS932").length;
            } catch (Exception e) {
                log.error(e);
            }
        }
        return count;
    }

    private String sql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     REGD.SCHREGNO, ");
        stb.append("     REGD.GRADE, ");
        stb.append("     REGD.HR_CLASS, ");
        stb.append("     REGD.ATTENDNO, ");
        stb.append("     REGD_H.HR_NAME, ");
        stb.append("     BASE.NAME, ");
        stb.append("     DUE_M.SEQ, ");
        stb.append("     COL_MM.GAKUNOKIN_DIV, ");
        stb.append("     DUE_M.SCHREGNO, ");
        stb.append("     DUE_M.COLLECT_L_CD, ");
        stb.append("     DUE_M.COLLECT_M_CD, ");
        stb.append("     COL_MM.COLLECT_M_NAME, ");
        stb.append("     DUE_M.MONEY_DUE, ");
        stb.append("     DUE_M.COLLECT_CNT ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT REGD ");
        stb.append("     LEFT JOIN SCHREG_BASE_MST BASE ON REGD.SCHREGNO = BASE.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT REGD_H ON REGD.YEAR = REGD_H.YEAR ");
        stb.append("          AND REGD.SEMESTER = REGD_H.SEMESTER ");
        stb.append("          AND REGD.GRADE = REGD_H.GRADE ");
        stb.append("          AND REGD.HR_CLASS = REGD_H.HR_CLASS ");
        stb.append("     LEFT JOIN COLLECT_SLIP_MONEY_DUE_M_DAT DUE_M ON REGD.YEAR = DUE_M.YEAR ");
        stb.append("          AND DUE_M.SLIP_NO = '" + _param._slipNo + "' ");
        stb.append("          AND REGD.SCHREGNO = DUE_M.SCHREGNO ");
        stb.append("     LEFT JOIN COLLECT_M_MST COL_MM ON DUE_M.YEAR = COL_MM.YEAR ");
        stb.append("          AND DUE_M.COLLECT_L_CD = COL_MM.COLLECT_L_CD ");
        stb.append("          AND DUE_M.COLLECT_M_CD = COL_MM.COLLECT_M_CD ");
        stb.append(" WHERE ");
        stb.append("     REGD.YEAR = '" + _param._year + "' ");
        stb.append("     AND REGD.SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND REGD.SCHREGNO = '" + _param._schregNo + "' ");
        stb.append(" ORDER BY ");
        stb.append("     COL_MM.GAKUNOKIN_DIV, ");
        stb.append("     DUE_M.SEQ ");

        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 68906 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _semester;
        private final String _slipNo;
        private final String _schregNo;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("CTRL_SEMESTER");
            _slipNo = request.getParameter("SLIP_NO");
            _schregNo = request.getParameter("SCHREGNO");
        }

    }
}

// eof

