/*
 * $Id$
 *
 * 作成日: 2015/03/13
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJP;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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

public class KNJP965 {

    private static final Log log = LogFactory.getLog(KNJP965.class);

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
            lmst.setPaddingExeMonth();

            for (Iterator itMonth = lmst.getSortMonthList().iterator(); itMonth.hasNext();) {
                String monthStr = (String) itMonth.next();
                ExeMonth exeMonth = (ExeMonth)lmst._exeMonthMap.get(monthStr);
                exeMonth.setLineListZenGetu(db2);
                svf.VrSetForm("KNJP965.frm", 1);
                svf.VrsOut("PRINT_DATE", lmst._lName); //※フィールド名がPRINT_DATEだが科目名が入るので注意
                svf.VrsOut("BILL_NAME", exeMonth._year + "年" + exeMonth._month + "月分"); //※フィールド名がBILL_NAMEだが、出力年月が入るで注意

                lineCnt = 1;
                int zanDaka = 0;
                for (Iterator itLine = exeMonth._lineList.iterator(); itLine.hasNext();) {
                    if (MAXLINE < lineCnt) {
                        svf.VrEndPage();
                        lineCnt = 1;
                    }

                    LineData lineData = (LineData) itLine.next();
                    final boolean isIncomeData = INCOME.equals(lineData._inoutDiv);
                    if (isIncomeData) {
                        zanDaka = zanDaka + Integer.parseInt(lineData._totalPrice);
                    } else {
                        zanDaka = zanDaka - Integer.parseInt(lineData._totalPrice);
                    }
                    if (!"0".equals(lineData._lineNo)) { //前月/前年度繰越の行は収入・支出を出力しない
                        final String fieldName = isIncomeData ? "INCOME" : "EXPENSES" ;
                        svf.VrsOutn(fieldName, lineCnt, lineData._totalPrice);
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
                svf.VrsOutn("DATE", MAXLINE, exeMonth.getMonth() + "/" + lastDay);
                final String setTekiyou = Integer.parseInt(exeMonth._month) == 3 ? "次年度繰越" : "次月繰越";
                svf.VrsOutn("REMARK1", MAXLINE, setTekiyou);
                svf.VrsOutn("REST", MAXLINE, String.valueOf(zanDaka));
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
        List retList = new ArrayList();
        final Map lCdHistoryMap = new LinkedHashMap();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getPrintDataSql();
            log.debug(" sql=" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String inoutDiv   = rs.getString("INOUT_DIV");
                final String schoolKind = rs.getString("SCHOOL_KIND"); //前月繰越額の計算時に使用
                final String year       = rs.getString("YEAR");
                final String exeDate    = rs.getString("EXE_DATE");
                final String exeYear    = rs.getString("EXE_YEAR");
                final String exeMonth   = rs.getString("EXE_MONTH");
                final String requestNo  = rs.getString("REQUEST_NO");
                final String lineNo     = rs.getString("LINE_NO");
                final String lCd        = rs.getString("LCD");
                final String lName      = rs.getString("LNAME");
                final String mCd        = rs.getString("MCD");
                final String mName      = rs.getString("MNAME");
                final String sName      = rs.getString("SNAME");
                final String totalPrice = rs.getString("TOTAL_PRICE");

                //Lmst作成
                final String lcdKey = schoolKind + lCd;
                if (!lCdHistoryMap.containsKey(lcdKey)) {
                    final Lmst lmst = new Lmst(schoolKind, lCd, lName);
                    lCdHistoryMap.put(lcdKey, lmst);
                }
                final Lmst lmst = (Lmst)lCdHistoryMap.get(lcdKey);

                //ExeMonth作成
                if (!lmst._exeMonthMap.containsKey(exeYear + "-" + exeMonth)) {
                    new ExeMonth(lmst, exeYear, exeMonth);
                }
                final ExeMonth setExeMonth = (ExeMonth)lmst._exeMonthMap.get(exeYear + "-" + exeMonth);

                //LineData作成
                new LineData(setExeMonth, inoutDiv, year, exeDate, requestNo, lineNo, lCd, lName, mCd, mName, sName, totalPrice);
            }

            //Lmstのリストを取得
            retList = new ArrayList(lCdHistoryMap.values());

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
        stb.append("     T1.SCHOOL_KIND, ");
        stb.append("     T1.YEAR, ");
        stb.append("     I1.INCOME_DATE AS EXE_DATE, ");
        stb.append("     YEAR(I1.INCOME_DATE) AS EXE_YEAR, ");
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
        stb.append("     INNER JOIN LEVY_REQUEST_INCOME_DAT I1 ON T1.SCHOOLCD = I1.SCHOOLCD ");
        stb.append("           AND T1.SCHOOL_KIND = I1.SCHOOL_KIND ");
        stb.append("           AND T1.YEAR = I1.YEAR  ");
        stb.append("           AND T1.INCOME_L_CD = I1.INCOME_L_CD ");
        stb.append("           AND T1.INCOME_M_CD = I1.INCOME_M_CD ");
        stb.append("           AND T1.REQUEST_NO = I1.REQUEST_NO ");
        stb.append("           AND VALUE(I1.INCOME_APPROVAL, '0') = '1' ");
        stb.append("           AND VALUE(I1.INCOME_CANCEL, '0') = '0' ");
        stb.append("           AND I1.INCOME_DATE BETWEEN DATE('" + _param._yearMonthF + "-01" + "') AND last_day(DATE('" + _param._yearMonthT + "-01" + "'))  ");
        stb.append("     LEFT JOIN LEVY_L_MST L1 ON T1.SCHOOLCD = L1.SCHOOLCD");
        stb.append("          AND T1.SCHOOL_KIND = L1.SCHOOL_KIND ");
        stb.append("          AND T1.YEAR = L1.YEAR ");
        stb.append("          AND T1.INCOME_L_CD = L1.LEVY_L_CD ");
        stb.append("     LEFT JOIN LEVY_M_MST L2 ON T1.YEAR = L2.YEAR");
        stb.append("          AND T1.SCHOOLCD = L2.SCHOOLCD ");
        stb.append("          AND T1.SCHOOL_KIND = L2.SCHOOL_KIND ");
        stb.append("          AND T1.INCOME_L_CD = L2.LEVY_L_CD ");
        stb.append("          AND T1.INCOME_M_CD = L2.LEVY_M_CD ");
        stb.append("          AND L2.LEVY_IN_OUT_DIV = '1' ");
        stb.append(" WHERE ");
        stb.append("     T1.SCHOOLCD = '" + _param._schoolCd + "' ");
        if (!"".equals(_param._schoolKind)) {
            stb.append("     AND T1.SCHOOL_KIND = '" + _param._schoolKind + "' ");
        }
        stb.append("     AND T1.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("     AND I1.COLLECT_DIV = '1' ");
        stb.append(" UNION ALL ");
        stb.append(" SELECT ");
        stb.append("     '2' AS INOUT_DIV, ");
        stb.append("     T1.SCHOOL_KIND, ");
        stb.append("     T1.YEAR, ");
        stb.append("     I1.OUTGO_DATE AS EXE_DATE, ");
        stb.append("     YEAR(I1.OUTGO_DATE) AS EXE_YEAR, ");
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
        stb.append("     INNER JOIN LEVY_REQUEST_OUTGO_DAT I1 ON T1.SCHOOLCD = I1.SCHOOLCD ");
        stb.append("           AND T1.SCHOOL_KIND = I1.SCHOOL_KIND ");
        stb.append("           AND T1.YEAR = I1.YEAR  ");
        stb.append("           AND T1.OUTGO_L_CD = I1.OUTGO_L_CD ");
        stb.append("           AND T1.OUTGO_M_CD = I1.OUTGO_M_CD ");
        stb.append("           AND T1.REQUEST_NO = I1.REQUEST_NO ");
        stb.append("           AND VALUE(I1.OUTGO_APPROVAL, '0') = '1' ");
        stb.append("           AND VALUE(I1.OUTGO_CANCEL, '0') = '0' ");
        stb.append("           AND I1.OUTGO_DATE BETWEEN DATE('" + _param._yearMonthF + "-01" + "') AND last_day(DATE('" + _param._yearMonthT + "-01" + "'))  ");
        stb.append("     LEFT JOIN LEVY_L_MST L1 ON T1.SCHOOLCD = L1.SCHOOLCD");
        stb.append("          AND T1.SCHOOL_KIND = L1.SCHOOL_KIND ");
        stb.append("          AND T1.YEAR = L1.YEAR ");
        stb.append("          AND T1.OUTGO_L_CD = L1.LEVY_L_CD ");
        stb.append("     LEFT JOIN LEVY_M_MST L2 ON T1.YEAR = L2.YEAR");
        stb.append("          AND T1.SCHOOLCD = L2.SCHOOLCD ");
        stb.append("          AND T1.SCHOOL_KIND = L2.SCHOOL_KIND ");
        stb.append("          AND T1.OUTGO_L_CD = L2.LEVY_L_CD ");
        stb.append("          AND T1.OUTGO_M_CD = L2.LEVY_M_CD ");
        stb.append("          AND L2.LEVY_IN_OUT_DIV = '2' ");
        stb.append("     LEFT JOIN LEVY_S_MST L3 ON T1.SCHOOLCD = L3.SCHOOLCD");
        stb.append("          AND T1.SCHOOL_KIND = L3.SCHOOL_KIND ");
        stb.append("          AND T1.YEAR = L3.YEAR ");
        stb.append("          AND T1.OUTGO_L_CD = L3.LEVY_L_CD ");
        stb.append("          AND T1.OUTGO_M_CD = L3.LEVY_M_CD ");
        stb.append("          AND T1.OUTGO_S_CD = L3.LEVY_S_CD ");
        stb.append(" WHERE ");
        stb.append("     T1.SCHOOLCD 	= '" + _param._schoolCd + "' AND ");
        if (!"".equals(_param._schoolKind)) {
            stb.append("     T1.SCHOOL_KIND = '" + _param._schoolKind + "' AND ");
        }
        stb.append("     T1.YEAR 		= '" + _param._ctrlYear + "' ");
        stb.append(" ORDER BY ");
        stb.append("     SCHOOL_KIND, ");
        stb.append("     LCD, ");
        stb.append("     EXE_DATE, ");
        stb.append("     INOUT_DIV, ");
        stb.append("     REQUEST_NO, ");
        stb.append("     LINE_NO ");

        return stb.toString();
    }

    private class Lmst {
        private final String _schoolKind;
        private final String _lCd;
        private final String _lName;
        private final Map _exeMonthMap;

        public Lmst(
                final String schoolKind,
                final String lCd,
                final String lName
        ) {
            _schoolKind = schoolKind;
            _lCd = lCd;
            _lName = lName;
            _exeMonthMap = new HashMap();
        }

        private List getSortMonthList() {
            final List monthList = new ArrayList<String>(_exeMonthMap.keySet());

            Comparator<String> comparator = new Comparator<String>() {
                @Override
                public int compare(String yearMonth1, String yearMonth2) {
                    final Date date1 = convertStrToDate(yearMonth1);
                    final Date date2 = convertStrToDate(yearMonth2);
                    return date1.compareTo(date2);
                }
            };

            //月順ソート
            Collections.sort(monthList, comparator);
            return monthList;
        }

        //セットされていない月に空のExeMonthをセット
        private void setPaddingExeMonth() {
            try {
                final Date fromMonth = convertStrToDate(_param._yearMonthF);
                final Date toMonth   = convertStrToDate(_param._yearMonthT);
                final int diffMonth  = getDiffMonth(fromMonth, toMonth);

                Calendar cal = Calendar.getInstance();
                for (int i = 0; i <= diffMonth; i++) {
                    cal.setTime(fromMonth);
                    cal.add(Calendar.MONTH, i);
                    final String year  = String.valueOf(cal.get(Calendar.YEAR));
                    final String month = String.valueOf(cal.get(Calendar.MONTH) + 1);
                    if (!_exeMonthMap.containsKey(year + "-" + month)) {
                        new ExeMonth(this, year, month);
                    }
                }
            } catch(Exception ex) {
                log.debug("Exception:", ex);
            }
        }

        //年月文字列をDate型に変換
        private Date convertStrToDate(final String ymStr) {
            Date date = null;
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M");
                date = sdf.parse(ymStr);
            } catch (ParseException ex) {
                log.debug("Exception:", ex);
            }
            return date;
        }

        //2つの年月の差を取得
        private int getDiffMonth(Date date1, Date date2) {
            Calendar cal1 = Calendar.getInstance();
            cal1.setTime(date1);
            Calendar cal2 = Calendar.getInstance();
            cal2.setTime(date2);

            int count = 0;
            if (cal2.before(cal1)) {
                while (cal2.before(cal1)) {
                    cal2.add(Calendar.MONTH, 1);
                    count--;
                }
            } else {
                count--;
                while (!cal2.before(cal1)) {
                    cal2.add(Calendar.MONTH, -1);
                    count++;
                }
            }
            return count;
        }
    }

    private class ExeMonth {
        private Lmst _parentLmst;
        public String _hrName;
        private final String _year;
        private final String _month;
        private final List _lineList;
        public ExeMonth(
                final Lmst parentLMst,
                final String year,
                final String month
        ) {
            _parentLmst = parentLMst;
            _parentLmst._exeMonthMap.put(year + "-" + month, this);
            _year = year;
            _month = month;
            _lineList = new ArrayList();
        }

        private String getMonth() {
            return Integer.parseInt(_month) > 9 ? _month : "0" + _month;
        }

        //前年度/前月繰越行をリスト先頭に追加
        private void setLineListZenGetu(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = "4".equals(_month) ? getSimeDataSql(_parentLmst._lCd) : getZenGetuDataSql(_parentLmst._lCd);

                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                int dataCnt = 0;
                final String setMonth = Integer.parseInt(_month) > 9 ? _month : "0" + _month;
                //前年度/前月繰越設定
                while (rs.next()) {
                    final String inoutDiv   = "1";
                    final String year       = _param._ctrlYear;
                    final String exeDate    = _param._ctrlYear + "-" + setMonth + "-01";
                    final String requestNo  = "";
                    final String lineNo     = "0";
                    final String setLCd     = _parentLmst._lCd;
                    final String lName      = "";
                    final String mCd        = "";
                    final String mName      = "";
                    final String sName      = "4".equals(_month) ? "前年度繰越金" : "前月繰越金";
                    final String totalPrice = rs.getString("TOTAL_PRICE");

                    final LineData lineData = new LineData(this, inoutDiv, year, exeDate, requestNo, lineNo, setLCd, lName, mCd, mName, sName, totalPrice, false);
                    _lineList.add(0, lineData);
                    dataCnt++;
                }
                //前年度/前月繰越なしの場合。固定０円
                if (dataCnt == 0) {
                    final String inoutDiv   = "1";
                    final String year       = _param._ctrlYear;
                    final String exeDate    = _param._ctrlYear + "-" + setMonth + "-01";
                    final String requestNo  = "";
                    final String lineNo     = "0";
                    final String setLCd     = _parentLmst._lCd;
                    final String lName      = "";
                    final String mCd        = "";
                    final String mName      = "";
                    final String sName      = "4".equals(_month) ? "前年度繰越金" : "前月繰越金";
                    final String totalPrice = "0";

                    final LineData lineData = new LineData(this, inoutDiv, year, exeDate, requestNo, lineNo, setLCd, lName, mCd, mName, sName, totalPrice, false);
                    _lineList.add(0, lineData);
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
            stb.append("    SELECT ");
            stb.append("      YEAR, ");
            stb.append("      SUM(REQUEST_GK) AS TOTAL_PRICE ");
            stb.append("    FROM ");
            stb.append("      LEVY_REQUEST_INCOME_DAT ");
            stb.append("    WHERE ");
            stb.append("      SCHOOLCD = '" + _param._schoolCd + "' ");
            stb.append("      AND SCHOOL_KIND = '" + _parentLmst._schoolKind + "' ");
            stb.append("      AND YEAR = '" + _param._ctrlYear + "' ");
            stb.append("      AND COLLECT_DIV = '2' ");
            stb.append("      AND INCOME_L_CD = '" + lCd + "' ");
            stb.append("      AND INCOME_APPROVAL = '1' ");
            stb.append("      AND VALUE (INCOME_CANCEL, '0') = '0' ");
            stb.append("    GROUP BY ");
            stb.append("      YEAR ");

            return stb.toString();
        }

        private String getZenGetuDataSql(final String lCd) {
            final int month = Integer.parseInt(_month);
            final String setMonth = month < 10 ? "0" + month : String.valueOf(month);
            final String yearMonth = month < 4 ? (Integer.parseInt(_param._ctrlYear) + 1) + "-" + setMonth : _param._ctrlYear + "-" + setMonth;
            final StringBuffer stb = new StringBuffer();
            stb.append("    WITH INCOME_BASE AS ( ");
            stb.append("      SELECT ");
            stb.append("        * ");
            stb.append("      FROM ");
            stb.append("        LEVY_REQUEST_INCOME_DAT T1 ");
            stb.append("      WHERE ");
            stb.append("        T1.SCHOOLCD = '" + _param._schoolCd + "' ");
            stb.append("        AND T1.SCHOOL_KIND = '" + _parentLmst._schoolKind + "' ");
            stb.append("        AND T1.YEAR = '" + _param._ctrlYear + "' ");
            stb.append("        AND T1.INCOME_L_CD = '" + lCd + "' ");
            stb.append("        AND T1.INCOME_APPROVAL = '1' ");
            stb.append("        AND VALUE (T1.INCOME_CANCEL, '0') = '0' ");
            stb.append("    ), INCOME_ZENGETU AS ( ");
            stb.append("      SELECT ");
            stb.append("        YEAR, ");
            stb.append("        INCOME_L_CD, ");
            stb.append("        SUM(VALUE (REQUEST_GK, 0)) AS ZENGETU_MONEY ");
            stb.append("      FROM ");
            stb.append("        INCOME_BASE ");
            stb.append("      WHERE ");
            stb.append("        INCOME_DATE < DATE ('" + yearMonth + "-01" + "') ");
            stb.append("        AND COLLECT_DIV = '1' ");
            stb.append("      GROUP BY ");
            stb.append("        YEAR, ");
            stb.append("        INCOME_L_CD ");
            stb.append("    ), INCOME_KURIKOSI AS ( ");
            stb.append("      SELECT ");
            stb.append("        YEAR, ");
            stb.append("        INCOME_L_CD, ");
            stb.append("        SUM(VALUE (REQUEST_GK, 0)) AS KURIKOSI_MONEY ");
            stb.append("      FROM ");
            stb.append("        INCOME_BASE ");
            stb.append("      WHERE ");
            stb.append("        COLLECT_DIV = '2' ");
            stb.append("      GROUP BY ");
            stb.append("        YEAR, ");
            stb.append("        INCOME_L_CD ");
            stb.append("    ), OUTGO_DATA AS (  ");
            stb.append("      SELECT ");
            stb.append("        YEAR, ");
            stb.append("        OUTGO_L_CD, ");
            stb.append("        SUM(VALUE (REQUEST_GK, 0)) AS OUTGO_TOTAL ");
            stb.append("      FROM ");
            stb.append("        LEVY_REQUEST_OUTGO_DAT ");
            stb.append("      WHERE ");
            stb.append("        SCHOOLCD = '" + _param._schoolCd + "' ");
            stb.append("        AND SCHOOL_KIND = '" + _parentLmst._schoolKind + "' ");
            stb.append("        AND YEAR = '" + _param._ctrlYear + "' ");
            stb.append("        AND OUTGO_DATE < DATE ('" + yearMonth + "-01" + "') ");
            stb.append("        AND OUTGO_L_CD = '" + lCd + "' ");
            stb.append("        AND OUTGO_APPROVAL = '1' ");
            stb.append("        AND VALUE (OUTGO_CANCEL, '0') = '0' ");
            stb.append("      GROUP BY ");
            stb.append("        YEAR, ");
            stb.append("        OUTGO_L_CD ");
            stb.append("    ) ");
            stb.append("    SELECT ");
            stb.append("      VALUE (T1.ZENGETU_MONEY, 0) + VALUE (T2.KURIKOSI_MONEY, 0) - VALUE (T3.OUTGO_TOTAL, 0) AS TOTAL_PRICE ");
            stb.append("    FROM ");
            stb.append("      INCOME_ZENGETU AS T1 ");
            stb.append("      FULL OUTER JOIN INCOME_KURIKOSI AS T2 ");
            stb.append("        ON T2.YEAR = T1.YEAR ");
            stb.append("        AND T2.INCOME_L_CD = T1.INCOME_L_CD ");
            stb.append("      FULL OUTER JOIN OUTGO_DATA AS T3 ");
            stb.append("        ON T3.YEAR = T1.YEAR ");
            stb.append("        AND T3.OUTGO_L_CD = T1.INCOME_L_CD ");

            return stb.toString();
        }
    }

    private class LineData {
        private final ExeMonth _parentExeMonth;
        private final String _inoutDiv;
        private final String _year;
        private final String _exeDate;
        private final String _requestNo;
        private final String _lineNo;
        private final String _lCd;
        private final String _lName;
        private final String _mCd;
        private final String _mName;
        private final String _sName;
        private final String _totalPrice;

        public LineData(
                final ExeMonth parentExeMonth,
                final String inoutDiv,
                final String year,
                final String exeDate,
                final String requestNo,
                final String lineNo,
                final String lCd,
                final String lName,
                final String mCd,
                final String mName,
                final String sName,
                final String totalPrice,
                final boolean addListFlg
        ) {
            _parentExeMonth = parentExeMonth;
            if (addListFlg) {
                //フラグがtrueならインスタンス生成時にリストに追加する(未指定はtrue）
                _parentExeMonth._lineList.add(this);

            }
            _inoutDiv   = inoutDiv;
            _year       = year;
            _exeDate    = exeDate;
            _requestNo  = requestNo;
            _lineNo     = lineNo;
            _lCd        = lCd;
            _lName      = lName;
            _mCd        = mCd;
            _mName      = mName;
            _sName      = sName;
            _totalPrice = totalPrice;
        }

        public LineData(
                final ExeMonth parentExeMonth,
                final String inoutDiv,
                final String year,
                final String exeDate,
                final String requestNo,
                final String lineNo,
                final String lCd,
                final String lName,
                final String mCd,
                final String mName,
                final String sName,
                final String totalPrice
        ) {
            this(parentExeMonth, inoutDiv, year, exeDate, requestNo, lineNo, lCd, lName, mCd, mName, sName, totalPrice, true);
        }


        public String getExeDate() {
            return _parentExeMonth._month;
        }
    }

    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 72313 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _prgid;
        private final String _schoolCd;
        private final String _schoolKind;
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final String _yearMonthF;
        private final String _yearMonthT;
        private final String _schoolName;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _prgid = request.getParameter("PRGID");
            _schoolCd = request.getParameter("SCHOOLCD");
            _schoolKind = request.getParameter("SCHOOL_KIND");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _yearMonthF = request.getParameter("MONTH_F");
            _yearMonthT = request.getParameter("MONTH_T");
            _schoolName = getSchoolName(db2, _ctrlYear);
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

