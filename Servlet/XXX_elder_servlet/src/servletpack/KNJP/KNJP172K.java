// kanji=漢字
/*
 * $Id: 8eaf32eed256bee5fb8ecb0f87d2b70b49da4e03 $
 *
 * 作成日: 2010/10/18 11:29:14 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2010 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJP;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: 8eaf32eed256bee5fb8ecb0f87d2b70b49da4e03 $
 */
public class KNJP172K {

    private static final Log log = LogFactory.getLog("KNJP172K.class");

    private static final String FORMID = "KNJP172K.frm";
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
        final Student student = new Student(db2);
        svf.VrSetForm(FORMID, 4);

        final String nendo = nao_package.KenjaProperties.gengou(Integer.parseInt(_param._year)) + "年度";
        svf.VrsOut("NENDO", nendo);
        svf.VrsOut("HR_NAME", student._hrNameAbbv + "-" + student._attendNo);
        svf.VrsOut("NAME", student._name + "(" + _param._schregNo + ")");
        svf.VrsOut("RECOG_NO", student._passNo);
        svf.VrsOut("RESHUFFLE1", student._idou1);
        svf.VrsOut("RESHUFFLE2", student._idou2);

        setTotal(svf, student._jugyouYotei, "1");
        setTotal(svf, student._jugyouNyukin, "2");
        setTotal(svf, student._baseYotei, "3");
        setTotal(svf, student._baseNyukin, "4");
        setTotal(svf, student._addYotei, "5");
        setTotal(svf, student._addNyukin, "6");
        setTotal(svf, student._hojokin, "7");

        int allTotal = 0;
        for (final Iterator iter = _param._monthList.iterator(); iter.hasNext();) {
            final String month = (String) iter.next();
            svf.VrsOut("MONTH", Integer.parseInt(month) + "月");
            svf.VrsOut("NECESSITY", student.getMoney(student._jugyouYotei, month, ""));
            svf.VrsOut("PAYMENT", student.getMoney(student._jugyouNyukin, month, ""));
            svf.VrsOut("BASIC_SCHEDULE", student.getMoney(student._baseYotei, month, ""));
            svf.VrsOut("BASIC_DECISION", student.getMoney(student._baseNyukin, month, ""));
            svf.VrsOut("ADD_SCHEDULE", student.getMoney(student._addYotei, month, ""));
            svf.VrsOut("ADD_DECISION", student.getMoney(student._addNyukin, month, ""));

            final String hojoTotal = student.getMoney(student._hojokin, month, "");
            if (null != hojoTotal && !hojoTotal.equals("")) {
                svf.VrsOut("LKAKKO1", "(");
                svf.VrsOut("LKAKKO2", "(");
                svf.VrsOut("RKAKKO1", ")");
                svf.VrsOut("RKAKKO2", ")");
                svf.VrsOut("CLASS_SCHEDULE1", student.getMoney(student._hojokin, month, "1"));
                svf.VrsOut("CLASS_SCHEDULE2", student.getMoney(student._hojokin, month, "2"));
            }
            svf.VrsOut("CLASS_SCHEDULE3", student.getMoney(student._hojokin, month, ""));
            final int totalMoney = student.getTotalMoney(month);
            svf.VrsOut("TOTAL", String.valueOf(totalMoney));
            allTotal += totalMoney;
            svf.VrsOut("TOTAL10", String.valueOf(allTotal));
            svf.VrEndRecord();
        }
        _hasData = true;
    }

    private void setTotal(final Vrw32alp svf, final Map dataMap, final String field) {
        svf.VrsOut("PREF" + field, getFuken(dataMap));
        svf.VrsOut("REAMARK" + field, getBikou(dataMap));
        svf.VrsOut("TOTAL" + field, getTotalMoney(dataMap));
    }

    private String getFuken(final Map dataMap) {
        String retVal = "";
        for (final Iterator iter = dataMap.keySet().iterator(); iter.hasNext();) {
            final String month = (String) iter.next();
            final SetData setData = (SetData) dataMap.get(month);
            retVal = null != setData._fuken && !setData._fuken.equals("") ? setData._fuken : retVal;
        }
        return retVal;
    }

    private String getBikou(final Map dataMap) {
        String retVal = "";
        for (final Iterator iter = dataMap.keySet().iterator(); iter.hasNext();) {
            final String month = (String) iter.next();
            final SetData setData = (SetData) dataMap.get(month);
            retVal = null != setData._remark && !setData._remark.equals("") ? setData._remark : retVal;
        }
        return retVal;
    }

    private String getTotalMoney(Map dataMap) {
        int retVal = 0;
        for (final Iterator iter = dataMap.keySet().iterator(); iter.hasNext();) {
            final String month = (String) iter.next();
            final SetData setData = (SetData) dataMap.get(month);
            retVal += null != setData._setMoney && !setData._setMoney.equals("") ? Integer.parseInt(setData._setMoney) : 0;
        }
        return String.valueOf(retVal);
    }

    private void closeDb(final DB2UDB db2) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
    }

    private class Student {
        private final String _name;
        private final String _hrNameAbbv;
        private final String _attendNo;
        private final String _passNo;
        private String _idou1 = "";
        private String _idou2 = "";
        private final Map _jugyouYotei;
        private final Map _jugyouNyukin;
        private final Map _baseYotei;
        private final Map _baseNyukin;
        private final Map _addYotei;
        private final Map _addNyukin;
        private final Map _hojokin;
        private final Map _total;

        public Student(final DB2UDB db2) throws SQLException {
            final String infoSql = getStudentInfoSql();
            String name = "";
            String hrNameAbbv = "";
            String attendNo = "";
            String passNo = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(infoSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    name = rs.getString("NAME");
                    hrNameAbbv = rs.getString("HR_NAMEABBV");
                    attendNo = rs.getString("ATTENDNO");
                    passNo = rs.getString("PASSNO");
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            _name = name;
            _hrNameAbbv = hrNameAbbv;
            _attendNo = attendNo;
            _passNo = passNo;

            //異動情報
            int infoNo = 0;
            final String transferSql = getTransferInfo();
            PreparedStatement psTrans = null;
            ResultSet rsTrans = null;
            try {
                psTrans = db2.prepareStatement(transferSql);
                rsTrans = psTrans.executeQuery();
                while (rsTrans.next()) {
                    infoNo++;
                    final String sdate = rsTrans.getString("SDATE").replace('-', '/');
                    if (rsTrans.getString("SORT").equals("2")) {
                        final String edate = rsTrans.getString("EDATE").replace('-', '/');
                        final String fukusuu = (1 < rsTrans.getInt("CNT")) ? "：[複]" : "";
                        if (_idou1.length() > 0) {
                            _idou2 = rsTrans.getString("DIV_NAME") + "：" + sdate + "〜" + edate + fukusuu;
                        } else {
                            _idou1 = rsTrans.getString("DIV_NAME") + "：" + sdate + "〜" + edate + fukusuu;
                        }
                    } else {
                        if (_idou1.length() > 0) {
                            _idou2 = rsTrans.getString("DIV_NAME") + "：" + sdate;
                        } else {
                            _idou1 = rsTrans.getString("DIV_NAME") + "：" + sdate;
                        }
                    }
                }
            } finally {
                DbUtils.closeQuietly(null, psTrans, rsTrans);
                db2.commit();
            }

            final String jugyouYoteiSql = getSelectMoneyDue();
            _jugyouYotei = setData(db2, jugyouYoteiSql, "");
            final String jugyouNyukinSql = getSelectMoneyPaid();
            _jugyouNyukin = setData(db2, jugyouNyukinSql, "");
            final String baseYoteiSql = getSelectCountry("DUE");
            _baseYotei = setData(db2, baseYoteiSql, "DUE");
            final String baseNyukinSql = getSelectCountry("DUE_PAY");
            _baseNyukin = setData(db2, baseNyukinSql, "DUE_PAY");
            final String addYoteiSql = getSelectCountry("ADD");
            _addYotei = setData(db2, addYoteiSql, "ADD");
            final String addNyukinSql = getSelectCountry("ADD_PAY");
            _addNyukin = setData(db2, addNyukinSql, "ADD_PAY");
            final String hojokinSql = getReductionDat();
            _hojokin = setData(db2, hojokinSql, "");
            _total = setTotal();
        }

        public String getMoney(final Map dataMap, final String month, final String div) {
            final SetData setData = (SetData) dataMap.get(month);
            if (div.equals("1")) {
                return setData._setMoney1;
            } else if (div.equals("2")) {
                return setData._setMoney2;
            } else {
                return setData._setMoney;
            }
        }

        public int getTotalMoney(final String month) {
            int totalMoney = 0;

            SetData data = (SetData) _baseNyukin.get(month);
            totalMoney += getIntMoney(data);

            data = (SetData) _addNyukin.get(month);
            totalMoney += getIntMoney(data);

            data = (SetData) _hojokin.get(month);
            totalMoney += getIntMoney(data);

            return totalMoney;
        }

        public int getIntMoney(final SetData data) {
            int retVal = 0;
            if (null != data && null != data._setMoney && data._setMoney.length() > 0) {
                return Integer.parseInt(data._setMoney);
            }
            return retVal;
        }

        private String getStudentInfoSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("   T1.SCHREGNO, ");
            stb.append("   T1.NAME, ");
            stb.append("   L2.HR_NAMEABBV, ");
            stb.append("   L1.ATTENDNO, ");
            stb.append("   L3.PASSNO ");
            stb.append(" FROM ");
            stb.append("     SCHREG_BASE_MST T1 ");
            stb.append("     LEFT JOIN SCHREG_REGD_DAT L1 ON L1.YEAR = '" + _param._year + "' ");
            stb.append("          AND T1.SCHREGNO = L1.SCHREGNO ");
            stb.append("     LEFT JOIN SCHREG_REGD_HDAT L2 ON L1.YEAR = L2.YEAR ");
            stb.append("       AND L1.SEMESTER = L2.SEMESTER ");
            stb.append("       AND L1.GRADE = L2.GRADE ");
            stb.append("       AND L1.HR_CLASS = L2.HR_CLASS ");
            stb.append("     LEFT JOIN REDUCTION_AUTHORIZE_DAT L3 ON T1.SCHREGNO = L3.SCHREGNO ");
            stb.append("          AND L3.DATA_DIV = '1' ");
            stb.append("          AND L3.DATA_DIV_SUB = '1' ");
            stb.append(" WHERE ");
            stb.append("   T1.SCHREGNO = '" + _param._schregNo + "' ");
            stb.append(" ORDER BY ");
            stb.append("   L1.SEMESTER ");

            return stb.toString();
        }

        private String getTransferInfo() {
            final StringBuffer stb = new StringBuffer();
            final String sdate = _param._year + "-04-01";
            final String edate = String.valueOf(Integer.parseInt(_param._year) + 1) + "-03-31";
            stb.append(" WITH T_TRANSFER AS ( ");
            stb.append("     SELECT ");
            stb.append("         SCHREGNO, ");
            stb.append("         TRANSFERCD, ");
            stb.append("         MAX(TRANSFER_SDATE) AS TRANSFER_SDATE, ");
            stb.append("         COUNT(*) AS CNT ");
            stb.append("     FROM ");
            stb.append("         SCHREG_TRANSFER_DAT ");
            stb.append("     WHERE ");
            stb.append("         SCHREGNO = '" + _param._schregNo + "' ");
            stb.append("         AND TRANSFERCD IN ('1','2') "); //1:留学、2:休学
            stb.append("         AND ((TRANSFER_SDATE BETWEEN DATE('" + sdate + "') AND DATE('" + edate + "')) ");
            stb.append("          OR  (TRANSFER_EDATE BETWEEN DATE('" + sdate + "') AND DATE('" + edate + "'))) ");
            stb.append("     GROUP BY ");
            stb.append("         SCHREGNO, ");
            stb.append("         TRANSFERCD ");
            stb.append("     ) ");
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.TRANSFERCD AS DIV, ");
            stb.append("     L1.NAME1 AS DIV_NAME,  ");
            stb.append("     '2' AS SORT, ");
            stb.append("     T1.TRANSFER_SDATE AS SDATE, ");
            stb.append("     T1.TRANSFER_EDATE AS EDATE, ");
            stb.append("     T2.CNT ");
            stb.append(" FROM ");
            stb.append("     SCHREG_TRANSFER_DAT T1 ");
            stb.append("     INNER JOIN T_TRANSFER T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("                             AND T2.TRANSFERCD = T1.TRANSFERCD ");
            stb.append("                             AND T2.TRANSFER_SDATE = T1.TRANSFER_SDATE ");
            stb.append("     LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'A004' ");
            stb.append("                          AND L1.NAMECD2 = T1.TRANSFERCD ");
            stb.append(" UNION ALL ");
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.GRD_DIV AS DIV, ");
            stb.append("     L1.NAME1 AS DIV_NAME, ");
            stb.append("     '1' AS SORT, ");
            stb.append("     T1.GRD_DATE AS SDATE, ");
            stb.append("     CAST(NULL AS DATE) AS EDATE, ");
            stb.append("     1 AS CNT ");
            stb.append(" FROM ");
            stb.append("     SCHREG_BASE_MST T1 ");
            stb.append("     LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'A003' ");
            stb.append("                          AND L1.NAMECD2 = T1.GRD_DIV ");
            stb.append(" WHERE ");
            stb.append("     T1.SCHREGNO = '" + _param._schregNo + "' ");
            stb.append("     AND T1.GRD_DIV IN ('2','3') "); //2:退学、3:転学
            stb.append("     AND T1.GRD_DATE BETWEEN DATE('" + sdate + "') AND DATE('" + edate + "') ");
            stb.append(" UNION ALL ");
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.ENT_DIV AS DIV, ");
            stb.append("     L1.NAME1 AS DIV_NAME, ");
            stb.append("     '3' AS SORT, ");
            stb.append("     T1.ENT_DATE AS SDATE, ");
            stb.append("     CAST(NULL AS DATE) AS EDATE, ");
            stb.append("     1 AS CNT ");
            stb.append(" FROM ");
            stb.append("     SCHREG_BASE_MST T1 ");
            stb.append("     LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'A002' ");
            stb.append("                          AND L1.NAMECD2 = T1.ENT_DIV ");
            stb.append(" WHERE ");
            stb.append("     T1.SCHREGNO = '" + _param._schregNo + "' ");
            stb.append("     AND T1.ENT_DIV IN ('4','5') "); //4:転入学、5:編入学
            stb.append("     AND T1.ENT_DATE BETWEEN DATE('" + sdate + "') AND DATE('" + edate + "') ");
            stb.append(" ORDER BY ");
            stb.append("     SORT, ");
            stb.append("     SDATE DESC ");
            return stb.toString();
        }

        private Map setTotal() {
            return null;
        }

        private Map setData(final DB2UDB db2, final String sql, final String div) throws SQLException {
            final Map retMap = new HashMap();
            final String setCancelSoeji = div.equals("ADD") ? "ADD_PLAN_CANCEL_FLG" : "PLAN_CANCEL_FLG";
            final String setLockSoeji = div.equals("ADD") || div.equals("ADD_PAY") ? "ADD_PLAN_LOCK_FLG" : "PLAN_LOCK_FLG";
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String setMonth = rs.getString("SET_MONTH").length() == 1 ? "0" + rs.getString("SET_MONTH") : rs.getString("SET_MONTH");
                    String setMoney = rs.getString("SET_MONEY");
                    String setMoney1 = rs.getString("SET_MONEY1");
                    String setMoney2 = rs.getString("SET_MONEY2");
                    final String fukenCd = rs.getString("FUKEN_CD");
                    final String fuken = rs.getString("FUKEN");
                    final String remark = rs.getString("REDUC_REMARK");
                    final boolean cancelFlg = null != rs.getString(setCancelSoeji) && rs.getString(setCancelSoeji).equals("1") ? true : false;
                    if (!cancelFlg) {
                        if (null != fukenCd && fukenCd.equals("27") && null != setMoney && !setMoney.equals("")) { //27:大阪府
                            setMoney1 = null != setMoney1 && !setMoney1.equals("") ? setMoney1 : "";
                            setMoney2 = null != setMoney2 && !setMoney2.equals("") ? setMoney2 : "";
                        }
                        setMoney = null != setMoney && !setMoney.equals("") ? setMoney : "";
                    } else {
                        setMoney = "";
                    }

                    //色
                    String amikake = "";
                    if (div.equals("DUE_PAY") || div.equals("ADD_PAY")) {
                        if (null != rs.getString(setLockSoeji) && rs.getString(setLockSoeji).equals("1")) {
                            amikake = "red";
                        } else if (null != rs.getString("PAID_YEARMONTH") && !rs.getString("PAID_YEARMONTH").equals("")) {
                            amikake = "yellow";
                        } else if (null != rs.getString("SET_MONEY") && !rs.getString("SET_MONEY").equals("")) {
                            amikake = "blue";
                        }
                    }

                    final SetData setData = new SetData(setMoney, setMoney1, setMoney2, fuken, remark, amikake);
                    retMap.put(setMonth, setData);
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }

            for (final Iterator iter = _param._monthList.iterator(); iter.hasNext();) {
                final String month = (String) iter.next();
                if (!retMap.containsKey(month)) {
                    final SetData setData = new SetData();
                    retMap.put(month, setData);
                }
            }
            return retMap;
        }

        private String getSelectMoneyDue() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     CASE EXPENSE_M_CD WHEN '11' THEN '4' WHEN '12' THEN '9' ELSE '12' END AS SET_MONTH, ");
            stb.append("     MONEY_DUE AS SET_MONEY, ");
            stb.append("     '' AS FUKEN_CD, ");
            stb.append("     '' AS FUKEN, ");
            stb.append("     '' AS SET_MONEY1, ");
            stb.append("     '' AS SET_MONEY2, ");
            stb.append("     '' AS PLAN_CANCEL_FLG, ");
            stb.append("     '' AS PLAN_LOCK_FLG, ");
            stb.append("     '' AS PAID_YEARMONTH, ");
            stb.append("     '' AS REDUC_REMARK ");
            stb.append(" FROM ");
            stb.append("     MONEY_DUE_M_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _param._year + "' ");
            stb.append("     AND SCHREGNO = '" + _param._schregNo + "' ");
            stb.append("     AND EXPENSE_M_CD IN ('11', '12', '13') ");

            return stb.toString();
        }

        private String getSelectMoneyPaid() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     MONTH(PAID_MONEY_DATE) AS SET_MONTH, ");
            stb.append("     SUM(VALUE(PAID_MONEY, 0)) AS SET_MONEY, ");
            stb.append("     '' AS FUKEN_CD, ");
            stb.append("     '' AS FUKEN, ");
            stb.append("     '' AS SET_MONEY1, ");
            stb.append("     '' AS SET_MONEY2, ");
            stb.append("     '' AS PLAN_CANCEL_FLG, ");
            stb.append("     '' AS PLAN_LOCK_FLG, ");
            stb.append("     '' AS PAID_YEARMONTH, ");
            stb.append("     '' AS REDUC_REMARK ");
            stb.append(" FROM ");
            stb.append("     MONEY_PAID_M_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _param._year + "' ");
            stb.append("     AND SCHREGNO = '" + _param._schregNo + "' ");
            stb.append("     AND EXPENSE_M_CD IN ('11', '12', '13') ");
            stb.append(" GROUP BY ");
            stb.append("     MONTH(PAID_MONEY_DATE) ");

            return stb.toString();
        }

        private String getSelectCountry(final String div) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.PLAN_MONTH AS SET_MONTH, ");
            stb.append("     T1.PLAN_CANCEL_FLG, ");
            stb.append("     T1.ADD_PLAN_CANCEL_FLG, ");
            stb.append("     T1.PLAN_LOCK_FLG, ");
            stb.append("     T1.ADD_PLAN_LOCK_FLG, ");
            stb.append("     '' AS FUKEN_CD, ");
            stb.append("     '' AS FUKEN, ");
            stb.append("     '' AS SET_MONEY1, ");
            stb.append("     '' AS SET_MONEY2, ");
            if (div.equals("DUE")) {
                stb.append("     T1.PLAN_MONEY AS SET_MONEY, ");
                stb.append("     T1.PAID_YEARMONTH AS PAID_YEARMONTH, ");
                stb.append("     L1.REDUC_REMARK ");
            } else if (div.equals("DUE_PAY")) {
                stb.append("     T1.PAID_MONEY AS SET_MONEY, ");
                stb.append("     T1.PAID_YEARMONTH AS PAID_YEARMONTH, ");
                stb.append("     '' AS REDUC_REMARK ");
            } else if (div.equals("ADD")) {
                stb.append("     T1.ADD_PLAN_MONEY AS SET_MONEY, ");
                stb.append("     T1.ADD_PAID_YEARMONTH AS PAID_YEARMONTH, ");
                stb.append("     '' AS REDUC_REMARK ");
            } else {
                stb.append("     T1.ADD_PAID_MONEY AS SET_MONEY, ");
                stb.append("     T1.ADD_PAID_YEARMONTH AS PAID_YEARMONTH, ");
                stb.append("     '' AS REDUC_REMARK ");
            }
            stb.append(" FROM ");
            stb.append("     REDUCTION_COUNTRY_PLAN_DAT T1 ");
            stb.append("     LEFT JOIN REDUCTION_COUNTRY_DAT L1 ON T1.YEAR = L1.YEAR ");
            stb.append("     AND T1.SCHREGNO = L1.SCHREGNO ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _param._year + "' ");
            stb.append("     AND T1.SCHREGNO = '" + _param._schregNo + "' ");

            return stb.toString();
        }


        private String getReductionDat() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     CASE WHEN T1.REDUC_DEC_FLG_1 = '1' OR T1.REDUC_DEC_FLG_2 = '1' ");
            stb.append("          THEN CASE WHEN T1.REDUC_DEC_FLG_1 = '1' ");
            stb.append("                    THEN T1.REDUCTIONMONEY_1 ");
            stb.append("                    ELSE 0 ");
            stb.append("               END ");
            stb.append("               + ");
            stb.append("               CASE WHEN T1.REDUC_DEC_FLG_2 = '1' ");
            stb.append("                    THEN T1.REDUCTIONMONEY_2 ");
            stb.append("                    ELSE 0 ");
            stb.append("               END ");
            stb.append("          ELSE NULL ");
            stb.append("     END AS SET_MONEY, ");
            stb.append("     CASE WHEN T1.REDUC_DEC_FLG_1 = '1' ");
            stb.append("          THEN T1.REDUCTIONMONEY_1 ");
            stb.append("          ELSE NULL ");
            stb.append("     END AS SET_MONEY1, ");
            stb.append("     CASE WHEN T1.REDUC_DEC_FLG_2 = '1' ");
            stb.append("          THEN T1.REDUCTIONMONEY_2 ");
            stb.append("          ELSE NULL ");
            stb.append("     END AS SET_MONEY2, ");
            stb.append("     '12' AS SET_MONTH, ");
            stb.append("     L1.PREF_CD AS FUKEN_CD, ");
            stb.append("     L1.PREF_NAME AS FUKEN, ");
            stb.append("     '' AS PLAN_CANCEL_FLG, ");
            stb.append("     '' AS PLAN_LOCK_FLG, ");
            stb.append("     '' AS PAID_YEARMONTH, ");
            stb.append("     T1.REDUC_REMARK ");
            stb.append(" FROM ");
            stb.append("     REDUCTION_DAT T1 ");
            stb.append("     LEFT JOIN PREF_MST L1 ON T1.PREFECTURESCD = L1.PREF_CD ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _param._year + "' ");
            stb.append("     AND T1.SCHREGNO = '" + _param._schregNo + "' ");

            return stb.toString();
        }

    }

    private class SetData {
        final String _setMoney;
        final String _setMoney1;
        final String _setMoney2;
        final String _fuken;
        final String _remark;
        final String _amikake;

        public SetData() {
            _setMoney = "";
            _setMoney1 = "";
            _setMoney2 = "";
            _fuken = "";
            _remark = "";
            _amikake = "";
        }

        public SetData(
                final String setMoney,
                final String setMoney1,
                final String setMoney2,
                final String fuken,
                final String remark,
                final String amikake
        ) {
            _setMoney = setMoney;
            _setMoney1 = setMoney1;
            _setMoney2 = setMoney2;
            _fuken = fuken;
            _remark = remark;
            _amikake = amikake;
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
        private final String _schregNo;
        private final List _monthList;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _schregNo = request.getParameter("schregNo");
            _monthList = new ArrayList();
            _monthList.add("04");
            _monthList.add("05");
            _monthList.add("06");
            _monthList.add("07");
            _monthList.add("08");
            _monthList.add("09");
            _monthList.add("10");
            _monthList.add("11");
            _monthList.add("12");
            _monthList.add("01");
            _monthList.add("02");
            _monthList.add("03");
        }

    }
}

// eof
