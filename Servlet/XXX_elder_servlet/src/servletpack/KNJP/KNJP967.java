/*
 * $Id: f0daf766c399970bfcce280996e43bbaeb7adf37 $
 *
 * 作成日: 2015/03/13
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJP;


import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

public class KNJP967 {

    private static final Log log = LogFactory.getLog(KNJP967.class);

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
        final List lineList = getList(db2);
        final int allOutGoGk = getAllOutGoGk(db2);
        svf.VrSetForm("KNJP967.frm", 4);

        int zanDaka = 0;
        for (Iterator itLine = lineList.iterator(); itLine.hasNext();) {
            LineData lineData = (LineData) itLine.next();
            svf.VrsOut("TITLE", "支払内訳書(" + KNJ_EditDate.h_format_JP_MD(_param._outgoDate) + "支払い)");

            // 印鑑等
            printStamp(svf);

            svf.VrsOut("BILL_NO", lineData._traderSeikyuNo);
            svf.VrsOut("COMPANY_NAME1", lineData._traderName);
            svf.VrsOut("MONTH", lineData._seikyuMonth);
            svf.VrsOut("PAY_MONEY", lineData._totalPrice);
            svf.VrsOut("BANK_NAME", lineData._bankname);
            svf.VrsOut("BRANCH_NAME", lineData._branchname);
            svf.VrsOut("DEPOSIT_ITEM", lineData._yokinSyumoku);
            svf.VrsOut("ACCOUNT_NO", lineData._bankAccountno);
            svf.VrsOut("PAY_METHOD", lineData._payDivName);

            zanDaka = zanDaka + Integer.parseInt(lineData._totalPrice);
            svf.VrsOut("TOTAL", String.valueOf(zanDaka));
            svf.VrsOut("PAY_TOTAL", String.valueOf(allOutGoGk));
            svf.VrEndRecord();
            _hasData = true;
        }

    }

    /**
     * 印鑑
     */
    private void printStamp(final Vrw32alp svf) {
        for (Iterator iterator = _param._stampData.keySet().iterator(); iterator.hasNext();) {
            final String key = (String) iterator.next();
            final StampData stampData = (StampData) _param._stampData.get(key);

            // 役職
            svf.VrsOut("JOB_NAME" + (Integer.parseInt(stampData._seq)), stampData._title);

            // 印鑑
            svf.VrsOut("STAMP" + (Integer.parseInt(stampData._seq)), _param.getStampImageFile(stampData._stampName));
        }
    }

    private int getAllOutGoGk(final DB2UDB db2) {
        int retInt = 0;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getTotalOutGoGk();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                retInt = rs.getInt("TOTAL_PRICE");
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retInt;
    }

    private String getTotalOutGoGk() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     VALUE(SUM(VALUE(T1.TOTAL_PRICE, 0) + VALUE(I1.REQUEST_TESUURYOU, 0)), 0) AS TOTAL_PRICE ");
        stb.append(" FROM ");
        stb.append("     LEVY_REQUEST_OUTGO_MEISAI_DAT T1 ");
        stb.append("     INNER JOIN LEVY_REQUEST_OUTGO_DAT I1 ON T1.SCHOOLCD    = I1.SCHOOLCD ");
        stb.append("                                         AND T1.SCHOOL_KIND = I1.SCHOOL_KIND ");
        stb.append("                                         AND T1.YEAR        = I1.YEAR ");
        stb.append("                                         AND T1.OUTGO_L_CD  = I1.OUTGO_L_CD ");
        stb.append("                                         AND T1.OUTGO_M_CD  = I1.OUTGO_M_CD ");
        stb.append("                                         AND T1.REQUEST_NO  = I1.REQUEST_NO ");
        stb.append("                                         AND VALUE(I1.OUTGO_CANCEL, '0') = '0' ");
        stb.append("                                         AND I1.OUTGO_DATE <= '" + _param._outgoDate.replace('/', '-') + "' ");
        stb.append(" WHERE ");
        stb.append("         T1.SCHOOLCD    = '" + _param._schoolCd + "' ");
        stb.append("     AND T1.SCHOOL_KIND = '" + _param._schoolKind + "' ");
        stb.append("     AND T1.YEAR        = '" + _param._year + "' ");

        return stb.toString();
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

    private List getList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getPrintDataSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String traderCd       = rs.getString("TRADER_CD");
                final String traderSeikyuNo = rs.getString("TRADER_SEIKYU_NO");
                final String traderName     = rs.getString("TRADER_NAME");
                final String seikyuMonth    = rs.getString("SEIKYU_MONTH");
                final String bankname       = rs.getString("BANKNAME");
                final String branchname     = rs.getString("BRANCHNAME");
                final String yokinSyumoku   = rs.getString("YOKIN_SYUMOKU");
                final String bankAccountno  = rs.getString("BANK_ACCOUNTNO");
                final String payDivName     = rs.getString("PAY_DIV_NAME");
                final String totalPrice     = rs.getString("TOTAL_PRICE");

                final LineData lineData = new LineData(traderCd, traderSeikyuNo, traderName, seikyuMonth, bankname, branchname, yokinSyumoku, bankAccountno, payDivName, totalPrice);
                retList.add(lineData);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String getPrintDataSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     VALUE(I1.TRADER_CD, '99999999') AS TRADER_CD, ");
        stb.append("     T1.TRADER_SEIKYU_NO, ");
        stb.append("     CASE WHEN I1.TRADER_CD IS NOT NULL ");
        stb.append("          THEN L1.TRADER_NAME ");
        stb.append("          ELSE I1.TRADER_NAME ");
        stb.append("     END AS TRADER_NAME, ");
        stb.append("     VALUE(T1.SEIKYU_MONTH, '') AS SEIKYU_MONTH, ");
        stb.append("     L2.BANKNAME, ");
        stb.append("     L2.BRANCHNAME, ");
        stb.append("     N1.NAME1 AS YOKIN_SYUMOKU, ");
        stb.append("     I1.BANK_ACCOUNTNO, ");
        stb.append("     N2.NAME1 AS PAY_DIV_NAME, ");
        stb.append("     SUM(VALUE(T1.TOTAL_PRICE, 0) + VALUE(I1.REQUEST_TESUURYOU, 0)) AS TOTAL_PRICE ");
        stb.append(" FROM ");
        stb.append("     LEVY_REQUEST_OUTGO_MEISAI_DAT T1 ");
        stb.append("     INNER JOIN LEVY_REQUEST_OUTGO_DAT I1 ON T1.SCHOOLCD    = I1.SCHOOLCD ");
        stb.append("                                         AND T1.SCHOOL_KIND = I1.SCHOOL_KIND ");
        stb.append("                                         AND T1.YEAR        = I1.YEAR ");
        stb.append("                                         AND T1.OUTGO_L_CD  = I1.OUTGO_L_CD ");
        stb.append("                                         AND T1.OUTGO_M_CD  = I1.OUTGO_M_CD ");
        stb.append("                                         AND T1.REQUEST_NO  = I1.REQUEST_NO ");
        stb.append("                                         AND VALUE(I1.OUTGO_CANCEL, '0') = '0' ");
        stb.append("                                         AND I1.OUTGO_DATE  = '" + _param._outgoDate.replace('/', '-') + "' ");
        stb.append("     LEFT JOIN TRADER_MST L1 ON I1.TRADER_CD = L1.TRADER_CD ");
        stb.append("     LEFT JOIN BANK_MST L2 ON I1.BANKCD   = L2.BANKCD ");
        stb.append("                          AND I1.BRANCHCD = L2.BRANCHCD ");
        stb.append("     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'G203' ");
        stb.append("                          AND I1.BANK_DEPOSIT_ITEM = N1.NAMECD2 ");
        stb.append("     LEFT JOIN NAME_MST N2 ON N2.NAMECD1 = 'G217' ");
        stb.append("                          AND I1.PAY_DIV = N2.NAMECD2 ");
        stb.append(" WHERE ");
        stb.append("         T1.SCHOOLCD    = '" + _param._schoolCd + "' ");
        stb.append("     AND T1.SCHOOL_KIND = '" + _param._schoolKind + "' ");
        stb.append("     AND T1.YEAR        = '" + _param._year + "' ");
        stb.append(" GROUP BY ");
        stb.append("     T1.SCHOOLCD, ");
        stb.append("     T1.SCHOOL_KIND, ");
        stb.append("     T1.YEAR, ");
        stb.append("     VALUE(I1.TRADER_CD, '99999999'), ");
        stb.append("     T1.TRADER_SEIKYU_NO, ");
        stb.append("     CASE WHEN I1.TRADER_CD IS NOT NULL ");
        stb.append("          THEN L1.TRADER_NAME ");
        stb.append("          ELSE I1.TRADER_NAME ");
        stb.append("     END , ");
        stb.append("     VALUE(T1.SEIKYU_MONTH, ''), ");
        stb.append("     L2.BANKNAME, ");
        stb.append("     L2.BRANCHNAME, ");
        stb.append("     N1.NAME1, ");
        stb.append("     I1.BANK_ACCOUNTNO, ");
        stb.append("     N2.NAME1 ");
        stb.append(" ORDER BY ");
        stb.append("     TRADER_CD, ");
        stb.append("     TRADER_NAME, ");
        stb.append("     SEIKYU_MONTH, ");
        stb.append("     TRADER_SEIKYU_NO ");

        return stb.toString();
    }

    private class Lmst {
        private final String _lCd;
        private final String _lName;
        private int _totalIncome;
        private int _totalOutGo;
        private final List _exeMonth;
        public Lmst(
                final String lCd,
                final String lName
        ) {
            _lCd = lCd;
            _lName = lName;
            _exeMonth = new ArrayList();
            _totalIncome = 0;
            _totalOutGo = 0;
        }
    }

    private class ExeMonth {
        public String _hrName;
        private final String _month;
        private final List _lineList;
        public ExeMonth(
                final String month
        ) {
            _month   = month;
            _lineList = new ArrayList();
        }
    }

    private class LineData {
        final String _traderCd;
        final String _traderSeikyuNo;
        final String _traderName;
        final String _seikyuMonth;
        final String _bankname;
        final String _branchname;
        final String _yokinSyumoku;
        final String _bankAccountno;
        final String _payDivName;
        final String _totalPrice;

        public LineData(
                final String traderCd,
                final String traderSeikyuNo,
                final String traderName,
                final String seikyuMonth,
                final String bankname,
                final String branchname,
                final String yokinSyumoku,
                final String bankAccountno,
                final String payDivName,
                final String totalPrice
        ) {
            _traderCd       = traderCd;
            _traderSeikyuNo = traderSeikyuNo;
            _traderName     = traderName;
            _seikyuMonth    = seikyuMonth;
            _bankname       = bankname;
            _branchname     = branchname;
            _yokinSyumoku   = yokinSyumoku;
            _bankAccountno  = bankAccountno;
            _payDivName     = payDivName;
            _totalPrice     = totalPrice;
        }

    }

    private class StampData {
        private final String _seq;
        private final String _title;
        private final String _stampName;
        public StampData(
                final String seq,
                final String title,
                final String stampName
                ) {
            _seq       = seq;
            _title     = title;
            _stampName = stampName;
        }
    }

    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 65427 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _schoolCd;
        private final String _schoolKind;
        private final String _outgoDate;
        private final String _prgid;
        private final String _year;
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final String _schoolName;
        /** 写真データ格納フォルダ */
        private final String _imageDir;
        /** 写真データの拡張子 */
        private final String _imageExt;
        /** 陰影保管場所(陰影出力に関係する) */
        private final String _documentRoot;
        private final Map _stampData;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _schoolCd       = request.getParameter("SCHOOLCD");
            _schoolKind     = request.getParameter("SCHOOL_KIND");
            _outgoDate      = request.getParameter("OUTGO_DATE");
            _prgid          = request.getParameter("PRGID");
            _year           = request.getParameter("YEAR");
            _ctrlYear       = request.getParameter("CTRL_YEAR");
            _ctrlSemester   = request.getParameter("CTRL_SEMESTER");
            _ctrlDate       = request.getParameter("CTRL_DATE");
            _schoolName     = getSchoolName(db2, _year, _schoolCd, _schoolKind);
            _imageDir       = "image/stamp";
            _imageExt       = "bmp";
            _documentRoot   = request.getParameter("DOCUMENTROOT"); // 陰影保管場所 NO001
            _stampData      = getStampData(db2);
        }

        private String getSchoolName(final DB2UDB db2, final String year, final String schoolCd, final String schoolKind) {
            String retSchoolName = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement("SELECT SCHOOLNAME1 FROM SCHOOL_MST WHERE YEAR = '" + year + "' AND SCHOOLCD = '" + schoolCd + "' AND SCHOOL_KIND = '" + schoolKind + "' ");
                rs = ps.executeQuery();
                if (rs.next()) {
                    retSchoolName = rs.getString("SCHOOLNAME1");
                }
            } catch (SQLException ex) {
                log.debug("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retSchoolName;
        }

        private Map getStampData(final DB2UDB db2) throws SQLException {
            final Map retMap = new TreeMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            PreparedStatement psStampNo = null;
            ResultSet rsStampNo = null;
            final String stamPSql = getStampData();
            try {
                ps = db2.prepareStatement(stamPSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String stampNoSql = getMaxStampNo(rs.getString("FILE_NAME"));
                    psStampNo = db2.prepareStatement(stampNoSql);
                    rsStampNo = psStampNo.executeQuery();
                    rsStampNo.next();
                    final String stampNo = rsStampNo.getString("STAMP_NO");
                    final StampData stampData = new StampData(rs.getString("SEQ"), rs.getString("TITLE"), stampNo);
                    retMap.put(rs.getString("SEQ"), stampData);
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                DbUtils.closeQuietly(null, psStampNo, rsStampNo);
                db2.commit();
            }
            return retMap;
        }

        private String getStampData() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     SEQ, ");
            stb.append("     FILE_NAME, ");
            stb.append("     TITLE ");
            stb.append(" FROM ");
            stb.append("     PRG_STAMP_DAT ");
            stb.append(" WHERE ");
            stb.append("         YEAR        = '" + _year + "' ");
            stb.append("     AND SEMESTER    = '9' ");
            stb.append("     AND SCHOOLCD    = '" + _schoolCd + "' ");
            stb.append("     AND SCHOOL_KIND = '" + _schoolKind + "' ");
            stb.append("     AND PROGRAMID   = '" + _prgid + "' ");
            return stb.toString();
        }

        private String getMaxStampNo(final String staffcd) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     MAX(STAMP_NO) AS STAMP_NO ");
            stb.append(" FROM ");
            stb.append("     ATTEST_INKAN_DAT ");
            stb.append(" WHERE ");
            stb.append("     STAFFCD = '" + staffcd + "' ");

            return stb.toString();
        }

        /**
         * 写真データファイルの取得
         */
        private String getStampImageFile(final String filename) {
            if (null == filename) {
                return null;
            }
            if (null == _documentRoot) {
                return null;
            } // DOCUMENTROOT
            if (null == _imageDir) {
                return null;
            }
            if (null == _imageExt) {
                return null;
            }
            final StringBuffer stb = new StringBuffer();
            stb.append(_documentRoot);
            stb.append("/");
            stb.append(_imageDir);
            stb.append("/");
            stb.append(filename);
            stb.append(".");
            stb.append(_imageExt);
            File file1 = new File(stb.toString());
            if (!file1.exists()) {
                return null;
            } // 写真データ存在チェック用
            return stb.toString();
        }

    }
}

// eof

