/**     プロジェクト/プロパティ/javaコード・スタイル/コード・テンプレート/パターン       ***/

/*
 * $Id: 9c592c179fd7ebb3400c637c6fadea2a564ed2c6 $
 *
 * 作成日: 2020/01/15
 * 作成者: yamashiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJP;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

public class KNJP375 {

    private static final Log log = LogFactory.getLog(KNJP375.class);

    private boolean _hasData;
    private final int MAX_LINE = 15;

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
        svf.VrSetForm("KNJP375.frm", 1);
        final List printList = getList(db2);
        final int page = printList.size() / MAX_LINE;
        final int pageRemainder = printList.size() % MAX_LINE;
        final int pageAll = page + (pageRemainder > 0 ? 1 : 0);
        int pcnt = 1;

        titleSet(db2, svf, pageAll, pcnt);

        int lineCnt = 1;
        int subTotalMoney = 0;
        int totalMoney = 0;
        for (Iterator iterator = printList.iterator(); iterator.hasNext();) {
            if (lineCnt > MAX_LINE) {
                svf.VrsOut("NUMBER", String.valueOf(lineCnt - 1));
                svf.VrsOut("SUBTOTAL", String.valueOf(subTotalMoney));
                svf.VrEndPage();
                lineCnt = 1;
                pcnt++;
                subTotalMoney = 0;
                titleSet(db2, svf, pageAll, pcnt);
            }
            final PrintStd printStd = (PrintStd) iterator.next();
            svf.VrsOutn("BANK2", lineCnt, printStd._banknameKana);
            svf.VrsOutn("BRANCH2", lineCnt, printStd._branchnameKana);
            svf.VrsOutn("DEPOSIT_ITEM", lineCnt, printStd._deposit);
            svf.VrsOutn("ACCOUNTNO", lineCnt, printStd._accountNo);
            svf.VrsOutn("NAME", lineCnt, printStd._accountName);
            svf.VrsOutn("MONEY", lineCnt, printStd._transferMoney);
            subTotalMoney += Integer.parseInt(printStd._transferMoney);
            totalMoney += Integer.parseInt(printStd._transferMoney);
            lineCnt++;

            _hasData = true;
        }
        if (_hasData) {
            svf.VrsOut("NUMBER", String.valueOf(lineCnt - 1));
            svf.VrsOut("SUBTOTAL", String.valueOf(subTotalMoney));
            svf.VrsOut("TOTAL_NUMBER", String.valueOf(printList.size()));
            svf.VrsOut("TOTAL", String.valueOf(totalMoney));
            svf.VrEndPage();
        }
    }

    private void titleSet(final DB2UDB db2, final Vrw32alp svf, final int pageAll, int pcnt) {
        svf.VrsOut("TITLE", "振込依頼書(連記式)");
        svf.VrsOut("PAGE", pageAll + "-" + pcnt);
        svf.VrsOut("BANK1", _param._bankName);
        svf.VrsOut("BRANCH1", _param._branchName);
        svf.VrsOut("SCHOOLNAME", _param._accountName);
        svf.VrsOut("SCHOOLNAME_KANA", _param._accountKana);
        svf.VrsOut("CHARGE", _param._staffName);
        svf.VrsOut("PHONE", _param._schoolTel);
        svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(db2, _param._printDate));
        svf.VrsOut("SCHOOL_DEPOSIT_ITEM", _param._depositItem);
        svf.VrsOut("SCHOOL_ACCOUNTNO", _param._accountNo);
    }

    private List getList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getSql();
            log.fatal(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String schregNo       = rs.getString("SCHREGNO");
                final String banknameKana   = rs.getString("BANKNAME_KANA");
                final String branchnameKana = rs.getString("BRANCHNAME_KANA");
                final String deposit        = rs.getString("ABBV1");
                final String accountNo      = rs.getString("ACCOUNTNO");
                final String accountName    = rs.getString("ACCOUNTNAME");
                final String transferMoney  = rs.getString("TRANSFER_MONEY");

                final PrintStd printStd = new PrintStd(schregNo, banknameKana, branchnameKana, deposit, accountNo, accountName, transferMoney);
                retList.add(printStd);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String getSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     TRAN_STD.SCHREGNO, ");
        stb.append("     BANK.BANKNAME_KANA, ");
        stb.append("     BANK.BRANCHNAME_KANA, ");
        stb.append("     G203.ABBV1, ");
        stb.append("     RBANK.ACCOUNTNO, ");
        stb.append("     RBANK.ACCOUNTNAME, ");
        stb.append("     TRAN_STD.TRANSFER_MONEY ");
        stb.append(" FROM ");
        stb.append("     MONEY_TRANSFER_STD_DAT TRAN_STD ");
        stb.append("     LEFT JOIN REGISTBANK_DAT RBANK ON TRAN_STD.SCHREGNO = RBANK.SCHREGNO ");
        stb.append("     LEFT JOIN BANK_MST BANK ON RBANK.BANKCD = BANK.BANKCD ");
        stb.append("           AND RBANK.BRANCHCD = BANK.BRANCHCD ");
        stb.append("     LEFT JOIN NAME_MST G203 ON G203.NAMECD1 = 'G203' ");
        stb.append("           AND RBANK.DEPOSIT_ITEM = G203.NAMECD2 ");
        stb.append(" WHERE ");
        stb.append("     TRAN_STD.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("     AND TRAN_STD.TRANSFER_DIV IN " + SQLUtils.whereIn(true, _param._transferDivArray) + " ");
        stb.append("     AND EXISTS( ");
        stb.append("         SELECT ");
        stb.append("             'x' ");
        stb.append("         FROM ");
        stb.append("             SCHREG_REGD_DAT REGD ");
        stb.append("             INNER JOIN SCHREG_REGD_GDAT GDAT ON REGD.YEAR = GDAT.YEAR ");
        stb.append("                   AND REGD.GRADE = GDAT.GRADE ");
        stb.append("                   AND GDAT.SCHOOL_KIND = '" + _param._schoolKind + "' ");
        stb.append("         WHERE ");
        stb.append("             TRAN_STD.YEAR = REGD.YEAR ");
        stb.append("             AND TRAN_STD.SCHREGNO = REGD.SCHREGNO ");
        stb.append("     ) ");
        stb.append(" ORDER BY ");
        stb.append("     TRAN_STD.SCHREGNO, ");
        stb.append("     TRAN_STD.TRANSFER_DIV ");
        return stb.toString();
    }

    private class PrintStd {
        final String _schregNo;
        final String _banknameKana;
        final String _branchnameKana;
        final String _deposit;
        final String _accountNo;
        final String _accountName;
        final String _transferMoney;
        public PrintStd(
                final String schregNo,
                final String banknameKana,
                final String branchnameKana,
                final String deposit,
                final String accountNo,
                final String accountName,
                final String transferMoney
        ) {
            _schregNo          = schregNo;
            _banknameKana      = banknameKana;
            _branchnameKana    = branchnameKana;
            _deposit           = deposit;
            _accountNo         = accountNo;
            _accountName       = accountName;
            _transferMoney     = transferMoney;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 71776 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _schoolKind;
        private final String _printDate;
        private final String[] _transferDivArray;
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final String _staffCd;

        private final String _staffName;
        private final String _schoolName;
        private final String _schoolTel;
        private final String _bankName;
        private final String _branchName;
        private final String _depositItem;
        private final String _accountNo;
        private final String _accountName;
        private final String _accountKana;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _schoolKind = request.getParameter("SCHOOL_KIND");
            _printDate = request.getParameter("PRINT_DATE");
            _transferDivArray = request.getParameterValues("CATEGORY_SELECTED");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _staffCd = request.getParameter("STAFF");

            _staffName = getStaffName(db2);
            final String[] schoolInfo = getSchoolInfo(db2);
            _schoolName = schoolInfo[0];
            _schoolTel = schoolInfo[1];
            final String[] bankInfo = getBankInfo(db2);
            _bankName = bankInfo[0];
            _branchName = bankInfo[1];
            _depositItem = bankInfo[2];
            _accountNo = bankInfo[3];
            _accountName = bankInfo[4];
            _accountKana = bankInfo[5];
        }

        /** 担当者名 */
        private String getStaffName(final DB2UDB db2) {
            String retStr = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = "SELECT STAFFNAME FROM STAFF_MST WHERE STAFFCD = '" + _staffCd + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    retStr = rs.getString("STAFFNAME");
                }
            } catch (SQLException ex) {
                log.debug("StaffName!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retStr;
        }

        private String[] getSchoolInfo(final DB2UDB db2) {
            String[] retStr = new String[2];
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement("SELECT SCHOOLNAME1, SCHOOLTELNO FROM SCHOOL_MST WHERE YEAR = '" + _ctrlYear + "' ");
                rs = ps.executeQuery();
                if (rs.next()) {
                    retStr[0] = rs.getString("SCHOOLNAME1");
                    retStr[1] = rs.getString("SCHOOLTELNO");
                }
            } catch (SQLException ex) {
                log.debug("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retStr;
        }

        private String[] getBankInfo(final DB2UDB db2) {
            String[] retStr = new String[6];
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     TRADER.*, ");
                stb.append("     BANK.BANKNAME, ");
                stb.append("     BANK.BRANCHNAME, ");
                stb.append("     G203.NAME1 ");
                stb.append(" FROM ");
                stb.append("     TRADER_MST TRADER ");
                stb.append("     INNER JOIN TRADER_YDAT TRADER_Y ON TRADER_Y.YEAR = '" + _ctrlYear + "' ");
                stb.append("           AND TRADER.TRADER_CD = TRADER_Y.TRADER_CD ");
                stb.append("     INNER JOIN BANK_MST BANK ON TRADER.BANKCD = BANK.BANKCD ");
                stb.append("           AND TRADER.BRANCHCD = BANK.BRANCHCD ");
                stb.append("     LEFT JOIN NAME_MST G203 ON G203.NAMECD1 = 'G203' ");
                stb.append("           AND TRADER.BANK_DEPOSIT_ITEM = G203.NAMECD2 ");
                stb.append(" ORDER BY ");
                stb.append("     TRADER.TRADER_CD ");

                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    retStr[0] = rs.getString("BANKNAME");
                    retStr[1] = rs.getString("BRANCHNAME");
                    retStr[2] = rs.getString("NAME1");
                    retStr[3] = rs.getString("BANK_ACCOUNTNO");
                    retStr[4] = rs.getString("ACCOUNTNAME");
                    retStr[5] = rs.getString("ACCOUNTNAME_KANA");
                }
            } catch (SQLException ex) {
                log.debug("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retStr;
        }

    }
}

// eof
