/*
 * $Id: 0e2c290f538c8742e694a48c79458c5be2ead290 $
 *
 * 作成日: 2015/03/13
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJMP;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJMP965 {

    private static final Log log = LogFactory.getLog(KNJMP965.class);

    private final String INCOME = "1";
    private final String OUTGO = "2";

    private final int MAXLINE = 16;

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
        final List printLmst = getList(db2);

        int lineCnt = 1;
        for (Iterator iterator = printLmst.iterator(); iterator.hasNext();) {
            Lmst lmst = (Lmst) iterator.next();

            for (Iterator itMonth = lmst._exeMonth.iterator(); itMonth.hasNext();) {
                ExeMonth exeMonth = (ExeMonth) itMonth.next();

                svf.VrSetForm("KNJMP965.frm", 1);
                svf.VrsOut("BILL_NAME", lmst._lName);

                lineCnt = 1;
                int zanDaka = 0;
                for (Iterator itLine = exeMonth._lineList.iterator(); itLine.hasNext();) {
                    if (MAXLINE < lineCnt) {
                        svf.VrEndPage();
                        lineCnt = 1;
                    }
                    LineData lineData = (LineData) itLine.next();
                    if (INCOME.equals(lineData._inoutDiv)) {
                        svf.VrsOutn("INCOME", lineCnt, lineData._totalPrice);
                        zanDaka = zanDaka + Integer.parseInt(lineData._totalPrice);
                    } else {
                        svf.VrsOutn("EXPENSES", lineCnt, lineData._totalPrice);
                        zanDaka = zanDaka - Integer.parseInt(lineData._totalPrice);
                    }
                    final String[] dateArray = StringUtils.split(lineData._exeDate, "-");
                    svf.VrsOutn("DATE", lineCnt, dateArray[1] + "/" + dateArray[2]);
                    final String billField = KNJ_EditEdit.getMS932ByteLength(lineData._mName) > 10 ? "_2" : "_1";
                    svf.VrsOutn("BILL_NAME1" + billField, lineCnt, lineData._mName);
                    svf.VrsOutn("REMARK1", lineCnt, lineData._sName);
                    svf.VrsOutn("BILL_NO", lineCnt, lineData._requestNo);
                    svf.VrsOutn("REST", lineCnt, String.valueOf(zanDaka));
                    lineCnt++;
                }
                if (MAXLINE < lineCnt) {
                    svf.VrEndPage();
                }
                final int lastDay = getLastDayOfMonth(Integer.parseInt(_param._ctrlYear), Integer.parseInt(exeMonth._month) - 1);
                if (zanDaka < 0) {
                    svf.VrsOutn("INCOME", MAXLINE, String.valueOf(zanDaka * -1));
                } else {
                    svf.VrsOutn("EXPENSES", MAXLINE, String.valueOf(zanDaka));
                }
                svf.VrsOutn("DATE", MAXLINE, exeMonth.getMonth() + "/" + lastDay);
                final String setTekiyou = Integer.parseInt(exeMonth._month) == 3 ? "次年度繰越" : "次月繰越";
                svf.VrsOutn("REMARK1", MAXLINE, setTekiyou);
                svf.VrsOutn("REST", MAXLINE, "0");
                lineCnt = 1;
                svf.VrEndPage();
                _hasData = true;
            }
        }
        if (_hasData) {
            if (MAXLINE < lineCnt) {
                svf.VrEndPage();
                lineCnt = 1;
            } else {

            }
            svf.VrEndPage();
        }

    }

    private int getLastDayOfMonth(final int year, final int month) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        int lastDayOfMonth = cal.getActualMaximum(Calendar.DATE);

        return lastDayOfMonth;
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

            String befLcd = "";
            String befExeMonth = "";
            String nextMonth = _param._monthF;
            Lmst lmst = null;
            ExeMonth setExeMonth = null;
            while (rs.next()) {
                final String inoutDiv   = rs.getString("INOUT_DIV");
                final String year       = rs.getString("YEAR");
                final String exeDate    = rs.getString("EXE_DATE");
                final String exeMonth   = rs.getString("EXE_MONTH");
                final String requestNo  = rs.getString("REQUEST_NO");
                final String lineNo     = rs.getString("LINE_NO");
                final String lCd        = rs.getString("LCD");
                final String lName      = rs.getString("LNAME");
                final String mCd        = rs.getString("MCD");
                final String mName      = rs.getString("MNAME");
                final String sName      = rs.getString("SNAME");
                final String totalPrice = rs.getString("TOTAL_PRICE");

                if (!befLcd.equals(lCd)) {
                    if (!"".equals(befLcd)) {
                        int intExeMonth = Integer.parseInt(nextMonth) > 3 ? Integer.parseInt(nextMonth) : Integer.parseInt(nextMonth) + 12;
                        int intNextMonth = Integer.parseInt(_param._monthT) > 3 ? Integer.parseInt(_param._monthT) : Integer.parseInt(_param._monthT) + 12;
                        if (intExeMonth != intNextMonth) {
                            for (int i = intExeMonth; i <= intNextMonth; i++) {
                                final int setMonth = i > 12 ? i - 12 : i;
                                setExeMonth = new ExeMonth(String.valueOf(setMonth));
                                setExeMonth.setLineListZenGetu(db2, lCd);
                                lmst._exeMonth.add(setExeMonth);
                            }
                        }
                    }
                    lmst = new Lmst(lCd, lName);
                    retList.add(lmst);
                    nextMonth = _param._monthF;
                    int intExeMonth = Integer.parseInt(exeMonth) > 3 ? Integer.parseInt(exeMonth) : Integer.parseInt(exeMonth) + 12;
                    int intNextMonth = Integer.parseInt(nextMonth) > 3 ? Integer.parseInt(nextMonth) : Integer.parseInt(nextMonth) + 12;
                    if (intExeMonth != intNextMonth) {
                        for (int i = intNextMonth; i < intExeMonth; i++) {
                            final int setMonth = i > 12 ? i - 12 : i;
                            setExeMonth = new ExeMonth(String.valueOf(setMonth));
                            setExeMonth.setLineListZenGetu(db2, lCd);
                            lmst._exeMonth.add(setExeMonth);
                        }
                    }
                    nextMonth = String.valueOf(intExeMonth + 1);
                    setExeMonth = new ExeMonth(exeMonth);
                    setExeMonth.setLineListZenGetu(db2, lCd);
                    lmst._exeMonth.add(setExeMonth);
                } else if (!befExeMonth.equals(exeMonth)) {
                    int intExeMonth = Integer.parseInt(exeMonth) > 3 ? Integer.parseInt(exeMonth) : Integer.parseInt(exeMonth) + 12;
                    int intNextMonth = Integer.parseInt(nextMonth) > 3 ? Integer.parseInt(nextMonth) : Integer.parseInt(nextMonth) + 12;
                    if (intExeMonth != intNextMonth) {
                        for (int i = intNextMonth; i < intExeMonth; i++) {
                            final int setMonth = i > 12 ? i - 12 : i;
                            setExeMonth = new ExeMonth(String.valueOf(setMonth));
                            setExeMonth.setLineListZenGetu(db2, lCd);
                            lmst._exeMonth.add(setExeMonth);
                        }
                    }
                    nextMonth = String.valueOf(intExeMonth + 1);
                    setExeMonth = new ExeMonth(exeMonth);
                    setExeMonth.setLineListZenGetu(db2, lCd);
                    lmst._exeMonth.add(setExeMonth);
                }

                final LineData lineData = new LineData(inoutDiv, year, exeDate, exeMonth, requestNo, lineNo, lCd, lName, mCd, mName, sName, totalPrice);
                setExeMonth._lineList.add(lineData);

                if ("1".equals(inoutDiv)) {
                    lmst._totalIncome += Integer.parseInt(totalPrice);
                } else {
                    lmst._totalOutGo += Integer.parseInt(totalPrice);
                }

                befLcd = lCd;
                befExeMonth = exeMonth;
            }
            int intExeMonth = Integer.parseInt(nextMonth) > 3 ? Integer.parseInt(nextMonth) : Integer.parseInt(nextMonth) + 12;
            int intNextMonth = Integer.parseInt(_param._monthT) > 3 ? Integer.parseInt(_param._monthT) : Integer.parseInt(_param._monthT) + 12;
            for (int i = intExeMonth; i <= intNextMonth; i++) {
                final int setMonth = i > 12 ? i - 12 : i;
                setExeMonth = new ExeMonth(String.valueOf(setMonth));
                setExeMonth.setLineListZenGetu(db2, befLcd);
                lmst._exeMonth.add(setExeMonth);
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
        stb.append("     '1' AS INOUT_DIV, ");
        stb.append("     T1.YEAR, ");
        stb.append("     I1.INCOME_DATE AS EXE_DATE, ");
        stb.append("     MONTH(I1.INCOME_DATE) AS EXE_MONTH, ");
        stb.append("     T1.REQUEST_NO, ");
        stb.append("     T1.LINE_NO, ");
        stb.append("     T1.INCOME_L_CD AS LCD, ");
        stb.append("     L1.LEVY_L_NAME AS LNAME, ");
        stb.append("     T1.INCOME_M_CD AS MCD, ");
        stb.append("     L2.LEVY_M_NAME AS MNAME, ");
        stb.append("     T1.COMMODITY_NAME AS SNAME, ");
        stb.append("     VALUE(T1.TOTAL_PRICE, 0) AS TOTAL_PRICE ");
        stb.append(" FROM ");
        stb.append("     LEVY_REQUEST_INCOME_MEISAI_DAT T1 ");
        stb.append("     INNER JOIN LEVY_REQUEST_INCOME_DAT I1 ON T1.YEAR = I1.YEAR ");
        stb.append("           AND T1.INCOME_L_CD = I1.INCOME_L_CD ");
        stb.append("           AND T1.INCOME_M_CD = I1.INCOME_M_CD ");
        stb.append("           AND T1.REQUEST_NO = I1.REQUEST_NO ");
        stb.append("           AND VALUE(I1.INCOME_APPROVAL, '0') = '1' ");
        stb.append("           AND VALUE(I1.INCOME_CANCEL, '0') = '0' ");
        stb.append("           AND I1.YEAR || SUBSTR(CAST(I1.INCOME_DATE AS VARCHAR(10)), 6, 2) BETWEEN '" + _param._yearMonthF + "' AND '" + _param._yearMonthT + "'  ");
        stb.append("     LEFT JOIN LEVY_L_MST L1 ON T1.YEAR = L1.YEAR ");
        stb.append("          AND T1.INCOME_L_CD = L1.LEVY_L_CD ");
        stb.append("     LEFT JOIN LEVY_M_MST L2 ON T1.YEAR = L2.YEAR ");
        stb.append("          AND T1.INCOME_L_CD = L2.LEVY_L_CD ");
        stb.append("          AND T1.INCOME_M_CD = L2.LEVY_M_CD ");
        stb.append("          AND L2.LEVY_IN_OUT_DIV = '1' ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("     AND VALUE(L2.KURIKOSI_FLG, '0') = '0' ");
        stb.append(" UNION ALL ");
        stb.append(" SELECT ");
        stb.append("     '2' AS INOUT_DIV, ");
        stb.append("     T1.YEAR, ");
        stb.append("     I1.OUTGO_DATE AS EXE_DATE, ");
        stb.append("     MONTH(I1.OUTGO_DATE) AS EXE_MONTH, ");
        stb.append("     T1.REQUEST_NO, ");
        stb.append("     T1.LINE_NO, ");
        stb.append("     T1.OUTGO_L_CD AS LCD, ");
        stb.append("     L1.LEVY_L_NAME AS LNAME, ");
        stb.append("     T1.OUTGO_M_CD AS MCD, ");
        stb.append("     L2.LEVY_M_NAME AS MNAME, ");
        stb.append("     L3.LEVY_S_NAME AS SNAME, ");
        stb.append("     VALUE(T1.TOTAL_PRICE, 0) + VALUE(I1.REQUEST_TESUURYOU, 0) AS TOTAL_PRICE ");
        stb.append(" FROM ");
        stb.append("     LEVY_REQUEST_OUTGO_MEISAI_DAT T1 ");
        stb.append("     INNER JOIN LEVY_REQUEST_OUTGO_DAT I1 ON T1.YEAR = I1.YEAR ");
        stb.append("           AND T1.OUTGO_L_CD = I1.OUTGO_L_CD ");
        stb.append("           AND T1.OUTGO_M_CD = I1.OUTGO_M_CD ");
        stb.append("           AND T1.REQUEST_NO = I1.REQUEST_NO ");
        stb.append("           AND VALUE(I1.OUTGO_APPROVAL, '0') = '1' ");
        stb.append("           AND VALUE(I1.OUTGO_CANCEL, '0') = '0' ");
        stb.append("           AND I1.YEAR || SUBSTR(CAST(I1.OUTGO_DATE AS VARCHAR(10)), 6, 2) BETWEEN '" + _param._yearMonthF + "' AND '" + _param._yearMonthT + "'  ");
        stb.append("     LEFT JOIN LEVY_L_MST L1 ON T1.YEAR = L1.YEAR ");
        stb.append("          AND T1.OUTGO_L_CD = L1.LEVY_L_CD ");
        stb.append("     LEFT JOIN LEVY_M_MST L2 ON T1.YEAR = L2.YEAR ");
        stb.append("          AND T1.OUTGO_L_CD = L2.LEVY_L_CD ");
        stb.append("          AND T1.OUTGO_M_CD = L2.LEVY_M_CD ");
        stb.append("          AND L2.LEVY_IN_OUT_DIV = '2' ");
        stb.append("     LEFT JOIN LEVY_S_MST L3 ON T1.YEAR = L3.YEAR ");
        stb.append("          AND T1.OUTGO_L_CD = L3.LEVY_L_CD ");
        stb.append("          AND T1.OUTGO_M_CD = L3.LEVY_M_CD ");
        stb.append("          AND T1.OUTGO_S_CD = L3.LEVY_S_CD ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._ctrlYear + "' ");
        stb.append(" ORDER BY ");
        stb.append("     LCD, ");
        stb.append("     EXE_MONTH, ");
        stb.append("     EXE_DATE, ");
        stb.append("     INOUT_DIV, ");
        stb.append("     REQUEST_NO, ");
        stb.append("     LINE_NO ");

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
            _month = month;
            _lineList = new ArrayList();
        }

        private String getMonth() {
            return Integer.parseInt(_month) > 9 ? _month : "0" + _month;
        }

        private void setLineListZenGetu(final DB2UDB db2, final String lCd) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = "4".equals(_month) ? getSimeDataSql(lCd) : getZenGetuDataSql(lCd);
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                int dataCnt = 0;
                final String setMonth = Integer.parseInt(_month) > 9 ? _month : "0" + _month;
                //前年度/前月繰越設定
                while (rs.next()) {
                    final String inoutDiv   = "1";
                    final String year       = rs.getString("YEAR");
                    final String exeDate    = _param._ctrlYear + "-" + setMonth + "-01";
                    final String exeMonth   = _month;
                    final String requestNo  = "";
                    final String lineNo     = "0";
                    final String setLCd     = lCd;
                    final String lName      = "";
                    final String mCd        = "";
                    final String mName      = "";
                    final String sName      = "4".equals(_month) ? "前年度繰越金" : "前月繰越金";
                    final String totalPrice = rs.getString("TOTAL_PRICE");

                    final LineData lineData = new LineData(inoutDiv, year, exeDate, exeMonth, requestNo, lineNo, setLCd, lName, mCd, mName, sName, totalPrice);
                    _lineList.add(lineData);
                    dataCnt++;
                }
                //前月繰越なしの場合。固定０円(４月は出力しない。)
                if (dataCnt == 0 && !"4".equals(_month)) {
                    final String inoutDiv   = "1";
                    final String year       = _param._ctrlYear;
                    final String exeDate    = _param._ctrlYear + "-" + setMonth + "-01";
                    final String exeMonth   = _month;
                    final String requestNo  = "";
                    final String lineNo     = "0";
                    final String setLCd     = lCd;
                    final String lName      = "";
                    final String mCd        = "";
                    final String mName      = "";
                    final String sName      = "4".equals(_month) ? "前年度繰越金" : "前月繰越金";
                    final String totalPrice = "0";

                    final LineData lineData = new LineData(inoutDiv, year, exeDate, exeMonth, requestNo, lineNo, setLCd, lName, mCd, mName, sName, totalPrice);
                    _lineList.add(lineData);
                }

            } catch (SQLException ex) {
                log.debug("Exception:", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }

        }

        private String getSimeDataSql(final String lCd) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.YEAR, ");
            stb.append("     SUM(VALUE(T1.TOTAL_PRICE, 0)) AS TOTAL_PRICE ");
            stb.append(" FROM ");
            stb.append("     LEVY_REQUEST_INCOME_MEISAI_DAT T1 ");
            stb.append("     INNER JOIN LEVY_REQUEST_INCOME_DAT I1 ON T1.YEAR = I1.YEAR ");
            stb.append("           AND T1.INCOME_L_CD = I1.INCOME_L_CD ");
            stb.append("           AND T1.INCOME_M_CD = I1.INCOME_M_CD ");
            stb.append("           AND T1.REQUEST_NO = I1.REQUEST_NO ");
            stb.append("           AND VALUE(I1.INCOME_APPROVAL, '0') = '1' ");
            stb.append("           AND VALUE(I1.INCOME_CANCEL, '0') = '0' ");
            stb.append("           AND MONTH(I1.INCOME_DATE) = " + _month + " ");
            stb.append("     LEFT JOIN LEVY_L_MST L1 ON T1.YEAR = L1.YEAR ");
            stb.append("          AND T1.INCOME_L_CD = L1.LEVY_L_CD ");
            stb.append("     INNER JOIN LEVY_M_MST L2 ON T1.YEAR = L2.YEAR ");
            stb.append("           AND T1.INCOME_L_CD = L2.LEVY_L_CD ");
            stb.append("           AND T1.INCOME_M_CD = L2.LEVY_M_CD ");
            stb.append("           AND L2.LEVY_IN_OUT_DIV = '1' ");
            stb.append("           AND L2.KURIKOSI_FLG = '1' ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _param._ctrlYear + "' ");
            stb.append("     AND T1.INCOME_L_CD = '" + lCd + "' ");
            stb.append(" GROUP BY ");
            stb.append("     T1.YEAR ");

            return stb.toString();
        }

        private String getZenGetuDataSql(final String lCd) {
            final int month = Integer.parseInt(_month);
            final String setMonth = month < 10 ? "0" + month : String.valueOf(month);
            final String yearMonth = month < 4 ? (Integer.parseInt(_param._ctrlYear) + 1) + setMonth : _param._ctrlYear + setMonth;
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH INCOME_DATA AS ( ");
            stb.append(" SELECT ");
            stb.append("     T1.YEAR, ");
            stb.append("     T1.INCOME_L_CD, ");
            stb.append("     SUM(VALUE(T1.REQUEST_GK, 0)) AS INCOME_TOTAL ");
            stb.append(" FROM ");
            stb.append("     LEVY_REQUEST_INCOME_DAT T1 ");
            stb.append("     LEFT JOIN LEVY_M_MST L1 ON L1.YEAR = T1.YEAR ");
            stb.append("                            AND L1.LEVY_L_CD = T1.INCOME_L_CD ");
            stb.append("                            AND L1.LEVY_M_CD = T1.INCOME_M_CD ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _param._ctrlYear + "' ");
            stb.append("     AND T1.YEAR || SUBSTR(CAST(T1.INCOME_DATE AS VARCHAR(10)), 6, 2) < '" + yearMonth + "'  ");
            stb.append("     AND T1.INCOME_APPROVAL = '1' ");
            stb.append("     AND VALUE(T1.INCOME_CANCEL, '0') = '0' ");
            stb.append(" GROUP BY ");
            stb.append("     T1.YEAR, ");
            stb.append("     T1.INCOME_L_CD ");
            stb.append(" ), OUTGO_DATA AS ( ");
            stb.append(" SELECT ");
            stb.append("     YEAR, ");
            stb.append("     OUTGO_L_CD, ");
            stb.append("     SUM(VALUE(REQUEST_GK, 0)) AS OUTGO_TOTAL ");
            stb.append(" FROM ");
            stb.append("     LEVY_REQUEST_OUTGO_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _param._ctrlYear + "' ");
            stb.append("     AND YEAR || SUBSTR(CAST(OUTGO_DATE AS VARCHAR(10)), 6, 2) < '" + yearMonth + "'  ");
            stb.append("     AND OUTGO_APPROVAL = '1' ");
            stb.append("     AND VALUE(OUTGO_CANCEL, '0') = '0' ");
            stb.append(" GROUP BY ");
            stb.append("     YEAR, ");
            stb.append("     OUTGO_L_CD ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     T1.YEAR, ");
            stb.append("     SUM(VALUE(T1.INCOME_TOTAL, 0)) AS INCOME_TOTAL, ");
            stb.append("     SUM(VALUE(T2.OUTGO_TOTAL, 0)) AS OUTGO_TOTAL, ");
            stb.append("     SUM(VALUE(T1.INCOME_TOTAL, 0) - VALUE(T2.OUTGO_TOTAL, 0)) AS TOTAL_PRICE ");
            stb.append(" FROM ");
            stb.append("     INCOME_DATA T1 ");
            stb.append("     LEFT JOIN OUTGO_DATA T2 ON T1.YEAR = T2.YEAR ");
            stb.append("                            AND T1.INCOME_L_CD = T2.OUTGO_L_CD ");
            stb.append(" GROUP BY ");
            stb.append("     T1.YEAR ");
            stb.append(" ORDER BY ");
            stb.append("     T1.YEAR ");

            return stb.toString();
        }
    }

    private class LineData {
        private final String _inoutDiv;
        private final String _year;
        private final String _exeDate;
        private final String _exeMonth;
        private final String _requestNo;
        private final String _lineNo;
        private final String _lCd;
        private final String _lName;
        private final String _mCd;
        private final String _mName;
        private final String _sName;
        private final String _totalPrice;

        public LineData(
                final String inoutDiv,
                final String year,
                final String exeDate,
                final String exeMonth,
                final String requestNo,
                final String lineNo,
                final String lCd,
                final String lName,
                final String mCd,
                final String mName,
                final String sName,
                final String totalPrice
        ) {
            _inoutDiv   = inoutDiv;
            _year       = year;
            _exeDate    = exeDate;
            _exeMonth   = exeMonth;
            _requestNo  = requestNo;
            _lineNo     = lineNo;
            _lCd        = lCd;
            _lName      = lName;
            _mCd        = mCd;
            _mName      = mName;
            _sName      = sName;
            _totalPrice = totalPrice;
        }

    }

    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 56595 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _monthF;
        private final String _monthT;
        private final String _prgid;
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final String _yearMonthF;
        private final String _yearMonthT;
        private final String _schoolName;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _monthF = request.getParameter("MONTH_F");
            _monthT = request.getParameter("MONTH_T");
            _prgid = request.getParameter("PRGID");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _yearMonthF = getYearMonth(Integer.parseInt(_ctrlYear), request.getParameter("MONTH_F"));
            _yearMonthT = getYearMonth(Integer.parseInt(_ctrlYear), request.getParameter("MONTH_T"));
            _schoolName = getSchoolName(db2, _ctrlYear);
        }

        private String getYearMonth(final int year, final String month) {
            String retStr = String.valueOf(year) + month;
            if (Integer.parseInt(month) < 4) {
                retStr = String.valueOf(year + 1) + month;
            }
            return retStr;
        }
        private String getSchoolName(final DB2UDB db2, final String year) {
            String retSchoolName = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement("SELECT SCHOOLNAME1 FROM SCHOOL_MST WHERE YEAR = '" + year + "' ");
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

    }
}

// eof

