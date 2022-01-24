/*
 * $Id: c11badc7233dee39d31cb6e90b1bbb34a72a97e5 $
 *
 * 作成日: 2020/03/06
 * 作成者: tawada
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJP;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJP974A {

    private static final Log log = LogFactory.getLog(KNJP974A.class);

    private boolean _hasData;

    private final String FORM_974A = "KNJP974A.frm";
    private final String INCOME    = "INCOME";
    private final String OUTGO     = "OUTGO";

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

        NumberFormat nfNum = NumberFormat.getNumberInstance();    //カンマ区切り形式

        //学年毎
        final List printGradeList = getGradeList(db2);
        for (Iterator itGrade = printGradeList.iterator(); itGrade.hasNext();) {
            final Gdat gdat = (Gdat) itGrade.next();

            //科目コード毎
            for (Iterator itLmst = gdat._lmstPrintDataMap.keySet().iterator(); itLmst.hasNext();) {
                svf.VrSetForm(FORM_974A, 4);
                final String lCd = (String) itLmst.next();
                final Ldata ldata = (Ldata) gdat._lmstPrintDataMap.get(lCd);

                int maruIdx = 1;
                final Map taiouMap = new TreeMap();

                //タイトル
                final String nendo      = KNJ_EditDate.gengou(db2, Integer.parseInt(_param._year)) + "年度";
                final String schoolName = (String) _param._schoolNameMap.get(gdat._schoolKind);
                final String setTitle   = nendo + schoolName + "（" + gdat._gradeName1 + "）" + ldata._lName + "会計報告書";
                final int titleLen     = KNJ_EditEdit.getMS932ByteLength(setTitle);
                final String titleField = titleLen > 60 ? "3" : titleLen > 50 ? "2" : "1";
                svf.VrsOut("TITLE" + titleField, setTitle);

                /**********/
                /** 収入 **/
                /**********/
                svf.VrsOut("KIND", "(収　入)");
                svf.VrsOut("UNIT", "(単位：円)");
                svf.VrEndRecord();

                int lineCnt = 1;
                int totalIncomeMoney = 0;
                int totalIncMcdMoney = 0;
                String befIncLMcd = "";
                String incmName = "";
                for (Iterator itIncome = ldata._incomeList.iterator(); itIncome.hasNext();) {
                    final IncomeData incomeData = (IncomeData) itIncome.next();

                    //小合計セット
                    final String incomeLMcd = incomeData._lCd + incomeData._mCd;
                    if (!"".equals(befIncLMcd) && !befIncLMcd.equals(incomeLMcd)) {
                        final String[] befmNameArray = KNJ_EditEdit.get_token(incmName, 6, 10);
                        setmName(svf, befmNameArray, lineCnt);
                        mcdTotal(svf, INCOME, maruIdx, befIncLMcd, incmName, totalIncMcdMoney, taiouMap);

                        totalIncMcdMoney = 0;
                        lineCnt = 1;
                        maruIdx++;
                    }

                    //連番
                    svf.VrsOut("NO2", String.valueOf(lineCnt));
                    svf.VrsOut("GRPCD", incomeLMcd);

                    //項目名１
                    setmName(svf, incomeData._mNameArray, lineCnt);

                    //項目名
                    String setItem1 = "";
                    if ("1".equals(incomeData._sort)) {
                        setItem1 = incomeData._mName + " 前年度繰り越し";
                    } else {
                        //例）"研修費収入（10,000円×150人）"
                        setItem1 = incomeData._mName + "（" + nfNum.format(incomeData._incomeMoney) + "円 × " + String.valueOf(incomeData._schCount) + "人）";
                    }
                    final String fieldNum = KNJ_EditEdit.getMS932ByteLength(setItem1) > 60 ? "_2": "_1";
                    svf.VrsOut("ITEM3" + fieldNum, setItem1);

                    //決済日付
                    final String setMonth = StringUtils.split(incomeData._incomeDate, "-")[1];
                    final String setDay   = StringUtils.split(incomeData._incomeDate, "-")[2];
                    final String setDate  = setMonth + "月" + setDay + "日";
                    svf.VrsOut("DATE", setDate);

                    //金額
                    final int setPrice1 = incomeData._incomeMoney * incomeData._schCount;
                    svf.VrsOut("PRICE2", String.valueOf(setPrice1));

                    befIncLMcd = incomeLMcd;
                    incmName   = incomeData._mName;
                    totalIncMcdMoney += setPrice1;
                    totalIncomeMoney += setPrice1;
                    lineCnt++;
                    svf.VrEndRecord();
                }

                //小合計セット
                if (!"".equals(befIncLMcd)) {
                    final String[] mNameArray = KNJ_EditEdit.get_token(incmName, 6, 10);
                    setmName(svf, mNameArray, lineCnt);
                    mcdTotal(svf, INCOME, maruIdx, befIncLMcd, incmName, totalIncMcdMoney, taiouMap);
                    maruIdx++;
                }

                //収入総合計
                final String totalKey = ldata._lCd + "ALL";
                mcdTotal(svf, INCOME, maruIdx, totalKey, ldata._lName, totalIncomeMoney, taiouMap);
                maruIdx++;

                /**********/
                /** 支出 **/
                /**********/
                svf.VrsOut("KIND", "(支　出)");
                svf.VrEndRecord();

                lineCnt = 1;
                int totalOutGoMoney  = 0;
                int totalOutMcdMoney = 0;
                String outmName = "";
                befIncLMcd = "";
                for (Iterator itOutgo = ldata._outgoList.iterator(); itOutgo.hasNext();) {
                    final OutoGoData outoGoData = (OutoGoData) itOutgo.next();

                    //小合計セット
                    final String incomeLMcd = outoGoData._incomLCd + outoGoData._incomMCd;
                    if (!"".equals(befIncLMcd) && !befIncLMcd.equals(incomeLMcd)) {
                        final String[] befmNameArray = KNJ_EditEdit.get_token(outmName, 6, 10);
                        setmName(svf, befmNameArray, lineCnt);
                        mcdTotal(svf, OUTGO, maruIdx, befIncLMcd, outmName, totalOutMcdMoney, taiouMap);

                        totalOutMcdMoney = 0;
                        lineCnt = 1;
                        maruIdx++;
                    }

                    //連番
                    svf.VrsOut("NO2", String.valueOf(lineCnt));
                    svf.VrsOut("GRPCD", incomeLMcd);

                    //項目名
                    setmName(svf, outoGoData._mNameArray, lineCnt);

                    //細目名
                    final String itemField = KNJ_EditEdit.getMS932ByteLength(outoGoData._sName) > 60 ? "2" : "1";
                    svf.VrsOut("ITEM3_" + itemField, outoGoData._sName);

                    //決済日付
                    final String setMonth = StringUtils.split(outoGoData._outgoDate, "-")[1];
                    final String setDay   = StringUtils.split(outoGoData._outgoDate, "-")[2];
                    final String setDate  = setMonth + "月" + setDay + "日";
                    svf.VrsOut("DATE", setDate);

                    //金額
                    svf.VrsOut("PRICE2", String.valueOf(outoGoData._outGoMoney));

                    totalOutMcdMoney += outoGoData._outGoMoney;
                    totalOutGoMoney  += outoGoData._outGoMoney;
                    lineCnt++;
                    befIncLMcd = incomeLMcd;
                    outmName   = outoGoData._mName;
                    svf.VrEndRecord();
                }

                //小合計セット
                if (!"".equals(befIncLMcd)) {
                    final String[] mNameArray = KNJ_EditEdit.get_token(outmName, 6, 10);
                    setmName(svf, mNameArray, lineCnt);
                    mcdTotal(svf, OUTGO, maruIdx, befIncLMcd, outmName, totalOutMcdMoney, taiouMap);

                    totalOutMcdMoney = 0;
                    maruIdx++;
                }

                //支出総合計
                mcdTotal(svf, OUTGO, maruIdx, totalKey, ldata._lName, totalOutGoMoney, taiouMap);
                maruIdx++;

                /************/
                /** 最終欄 **/
                /************/
                int sumTital = 0;
                for (Iterator itTotal = taiouMap.keySet().iterator(); itTotal.hasNext();) {
                    final String mCd = (String) itTotal.next();
                    final TotalData td = (TotalData) taiouMap.get(mCd);

                    //合計名称
                    final String setName = td._incomeTitle + " ― " + td._outgoTitle;
                    final String fieldNum = KNJ_EditEdit.getMS932ByteLength(setName) > 60 ? "_2": "";
                    svf.VrsOut("ITEM5" + fieldNum, setName);

                    //合計金額
                    final int setMoney = td._incomeTotalMoney - td._outgoTotalMoney;
                    svf.VrsOut("PRICE5", String.valueOf(setMoney));

                    svf.VrEndRecord();
                    sumTital++;
                }

                _hasData = true;
            }

        }
    }

    private class TotalData {
        String _incomeTitle;
        int _incomeTotalMoney;
        String _outgoTitle;
        int _outgoTotalMoney;
        public TotalData (
                final String incomeTitle,
                final int incomeTotal,
                final String outgoTitle,
                final int outgoTotal
                ) {
            _incomeTitle        = incomeTitle;
            _incomeTotalMoney   = incomeTotal;
            _outgoTitle         = outgoTitle;
            _outgoTotalMoney    = outgoTotal;
        }
    }

    /** 項目名 */
    private void setmName(final Vrw32alp svf, final String[] str, final int lineCnt) {
        //項目名
        if (lineCnt <= 10 && str[lineCnt - 1] != null) {
            svf.VrsOut("ITEM2", str[lineCnt - 1]);
        }
    }

    /**
     * 小計セット
     * @param svf
     * @param div 収入:"INCOME"、支出:"OUTGO"
     * @param incomeLMcd
     * @param idx
     * @param mName
     * @param totalMoney
     * @param taiouMap
     */
    private void mcdTotal(final Vrw32alp svf, final String div, final int idx, final String incomeLMcd, final String mName, int totalMoney, Map taiouMap) {
        
        //項目名
        String koumokuFiledName = "";
        String moneyFiledName = "";
        String fieldNum = "";
        final String sou = (incomeLMcd.contains("ALL")) ? "総": "";
        final String subTitle = (INCOME.equals(div)) ? "（収入）": "（支出）";
        final String setItem1 =  mName + subTitle + sou + "合計";
        if (incomeLMcd.contains("ALL")) {
            koumokuFiledName = "ITEM5";
            moneyFiledName   = "PRICE5";
            fieldNum = KNJ_EditEdit.getMS932ByteLength(setItem1) > 60 ? "_2": "";
        } else {
            koumokuFiledName = "ITEM3";
            moneyFiledName   = "PRICE2";
            fieldNum = KNJ_EditEdit.getMS932ByteLength(setItem1) > 60 ? "_2": "_1";

            svf.VrsOut("GRPCD", incomeLMcd);
        }
        svf.VrsOut(koumokuFiledName + fieldNum, setItem1);

        //金額
        svf.VrsOut(moneyFiledName, String.valueOf(totalMoney));
        svf.VrEndRecord();

        //空行
        printBlankLine(svf);

        //最後の欄で使用
        if (taiouMap.containsKey(incomeLMcd)) {
            final TotalData td = (TotalData) taiouMap.get(incomeLMcd);
            if (INCOME.equals(div)) {
                td._incomeTitle      = setItem1;
                td._incomeTotalMoney = totalMoney;
            } else {
                td._outgoTitle      = setItem1;
                td._outgoTotalMoney = totalMoney;
            }
        } else {
            TotalData td2 = new TotalData("", 0, "", 0);
            if (INCOME.equals(div)) {
                td2 = (TotalData) new TotalData(setItem1, totalMoney, "", 0);
            } else {
                td2 = (TotalData) new TotalData("", 0, setItem1, totalMoney);
            }
            taiouMap.put(incomeLMcd, td2);
        }
    }

    private void printBlankLine(final Vrw32alp svf) {
        svf.VrsOut("BLANK", "空");
        svf.VrEndRecord();
    }

    private List getGradeList(final DB2UDB db2) {
        final List retList = new ArrayList();

        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     SCHOOL_KIND, ");
        stb.append("     GRADE, ");
        stb.append("     GRADE_NAME1 ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_GDAT ");
        stb.append(" WHERE ");
        stb.append("     YEAR = '" + _param._year + "' ");
        if ("00".equals(_param._grade)) {
            if ("1".equals(_param._usePrgSchoolkind)) {
                if (null != _param._selectSchoolKind && !"".equals(_param._selectSchoolKind)) {
                    final String[] schoolKinds = StringUtils.split(_param._selectSchoolKind, ":");
                    stb.append("     AND SCHOOL_KIND IN " + SQLUtils.whereIn(true, schoolKinds) + " ");
                }
            } else if ("1".equals(_param._useSchoolKindField)) {
                stb.append("     AND SCHOOL_KIND = '" + _param._schoolKind + "' ");
            }
        } else {
            stb.append("     AND GRADE = '" + _param._grade + "' ");
        }
        stb.append(" ORDER BY ");
        stb.append("     GRADE ");

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();
            while (rs.next()) {
                final String schoolKind = rs.getString("SCHOOL_KIND");
                final String grade      = rs.getString("GRADE");
                final String gradeName1 = rs.getString("GRADE_NAME1");

                final Gdat gdat = new Gdat(db2, schoolKind, grade, gradeName1);
                retList.add(gdat);
            }
        } catch (SQLException ex) {
            log.debug("getGradeList exception!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private class Gdat {
        final String _schoolKind;
        final String _grade;
        final String _gradeName1;
        final Map _lmstPrintDataMap;
        public Gdat(
                final DB2UDB db2,
                final String schoolKind,
                final String grade,
                final String gradeName1
        ) {
            _schoolKind         = schoolKind;
            _grade              = grade;
            _gradeName1         = gradeName1;
            _lmstPrintDataMap   = getLmstMap(db2, _grade);
        }

        private Map getLmstMap(final DB2UDB db2, final String grade) {
            final Map retMap = new TreeMap();

            //収入
            PreparedStatement psIncome = null;
            ResultSet rsIncome = null;
            try {
                final String incomeSql = getIncomeSql();
                psIncome = db2.prepareStatement(incomeSql);
                rsIncome = psIncome.executeQuery();
                while (rsIncome.next()) {
                    final String incomeLCd  = rsIncome.getString("INCOME_L_CD");
                    final String incomeMCd  = rsIncome.getString("INCOME_M_CD");
                    final String incomeDate = rsIncome.getString("INCOME_DATE");
                    final int incomeMoney   = rsIncome.getInt("INCOME_MONEY");
                    final int schCount      = rsIncome.getInt("SCH_COUNT");
                    final String sort       = rsIncome.getString("sort");

                    Ldata lData = null;
                    if (retMap.containsKey(incomeLCd)) {
                        lData = (Ldata) retMap.get(incomeLCd);
                    } else {
                        final String lName = (String) _param._levyLMstMap.get(_schoolKind + incomeLCd);
                        lData = new Ldata(incomeLCd, lName);
                        retMap.put(incomeLCd, lData);
                    }
                    final String mName = (String) _param._levyMMstMap.get(_schoolKind + incomeLCd + incomeMCd);
                    final IncomeData incomeData = new IncomeData(incomeLCd, incomeMCd, mName, incomeDate, incomeMoney, schCount, sort);
                    lData._incomeList.add(incomeData);
                }
            } catch (SQLException ex) {
                log.debug("getIncomeDat exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, psIncome, rsIncome);
                db2.commit();
            }

            //支出
            PreparedStatement psOutGo = null;
            ResultSet rsOutGo = null;
            try {
                final String outGoSql = getOutGoSql();
                psOutGo = db2.prepareStatement(outGoSql);
                rsOutGo = psOutGo.executeQuery();
                while (rsOutGo.next()) {
                    final String incomeLcd  = rsOutGo.getString("INCOME_L_CD");
                    final String incomeMcd  = rsOutGo.getString("INCOME_M_CD");
                    final String outgoDate  = rsOutGo.getString("OUTGO_DATE");
                    final String outgoLCd   = rsOutGo.getString("OUTGO_L_CD");
                    final String outgoMCd   = rsOutGo.getString("OUTGO_M_CD");
                    final String outgoSCd   = rsOutGo.getString("OUTGO_S_CD");
                    final int outGoMoney    = rsOutGo.getInt("TOTAL_PRICE");

                    Ldata lData = null;
                    if (retMap.containsKey(incomeLcd)) {
                        lData = (Ldata) retMap.get(incomeLcd);
                    } else {
                        final String lName = (String) _param._levyLMstMap.get(_schoolKind + incomeLcd);
                        lData = new Ldata(incomeLcd, lName);
                        retMap.put(incomeLcd, lData);
                    }

                    final String mName = (String) _param._levyMMstMap.get(_schoolKind + outgoLCd + outgoMCd);
                    final String sName = (String) _param._levySMstMap.get(_schoolKind + outgoLCd + outgoMCd + outgoSCd);

                    final OutoGoData outoGoData = new OutoGoData(incomeLcd, incomeMcd, outgoDate, mName, sName, outGoMoney);
                    lData._outgoList.add(outoGoData);
                }
            } catch (SQLException ex) {
                log.debug("getOutgoDat exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, psOutGo, rsOutGo);
                db2.commit();
            }
            return retMap;
        }

        private String getIncomeSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH SCH_INCOME_DATA AS ( ");
            stb.append("     SELECT ");
            stb.append("         INC_S.INCOME_L_CD, ");
            stb.append("         INC_S.INCOME_M_CD, ");
            stb.append("         INC_D.COLLECT_DIV, ");
            stb.append("         INC_D.INCOME_DATE, ");
            stb.append("         INC_S.SCHREGNO, ");
            stb.append("         sum(INC_S.INCOME_MONEY) AS INCOME_MONEY ");
            stb.append("     FROM ");
            stb.append("         LEVY_REQUEST_INCOME_SCHREG_DAT INC_S ");
            stb.append("         LEFT JOIN LEVY_REQUEST_INCOME_DAT INC_D ");
            stb.append("             ON INC_D.SCHOOLCD    = INC_S.SCHOOLCD ");
            stb.append("            AND INC_D.SCHOOL_KIND = INC_S.SCHOOL_KIND ");
            stb.append("            AND INC_D.YEAR        = INC_S.YEAR ");
            stb.append("            AND INC_D.INCOME_L_CD = INC_S.INCOME_L_CD ");
            stb.append("            AND INC_D.INCOME_M_CD = INC_S.INCOME_M_CD ");
            stb.append("            AND INC_D.REQUEST_NO  = INC_S.REQUEST_NO ");
            stb.append("         	AND INC_D.INCOME_DATE BETWEEN '" + _param._fromDate + "' AND '" + _param._toDate + "' ");
            stb.append("     WHERE ");
            stb.append("             INC_S.SCHOOLCD    = '" + _param._schoolCd + "' ");
            stb.append("         AND INC_S.SCHOOL_KIND = '" + _schoolKind + "' ");
            stb.append("         AND INC_S.YEAR        = '" + _param._year + "' ");
            stb.append("         AND VALUE(INC_D.INCOME_APPROVAL, '0') = '1' ");
            stb.append("         AND VALUE(INC_D.INCOME_CANCEL, '0')  != '1' ");
            stb.append("         AND INC_S.SCHREGNO IN ( SELECT ");
            stb.append("                                     REGD.SCHREGNO ");
            stb.append("                                 FROM ");
            stb.append("                                     SCHREG_REGD_DAT REGD ");
            stb.append("                                 WHERE ");
            stb.append("                                         REGD.YEAR     = INC_S.YEAR ");
            stb.append("                                     AND REGD.GRADE    = '" + _grade + "' ");
            stb.append("                                ) ");
            stb.append("     GROUP BY ");
            stb.append("         INC_S.INCOME_L_CD, ");
            stb.append("         INC_S.INCOME_M_CD, ");
            stb.append("         INC_D.COLLECT_DIV, ");
            stb.append("         INC_D.INCOME_DATE, ");
            stb.append("         INC_S.SCHREGNO ");
            stb.append(" ) ");
            //前年度繰越から作成した収入伺
            stb.append(" SELECT ");
            stb.append("     INCOME_L_CD, ");
            stb.append("     INCOME_M_CD, ");
            stb.append("     INCOME_DATE, ");
            stb.append("     sum(INCOME_MONEY) AS INCOME_MONEY, ");
            stb.append("     1 as SCH_COUNT, ");
            stb.append("     '1' as sort ");
            stb.append(" FROM ");
            stb.append("     SCH_INCOME_DATA ");
            stb.append(" WHERE ");
            stb.append("     VALUE(COLLECT_DIV, '0') = '2' ");
            stb.append(" GROUP BY ");
            stb.append("     INCOME_L_CD, ");
            stb.append("     INCOME_M_CD, ");
            stb.append("     INCOME_DATE ");
            stb.append(" UNION ALL ");
            //それ以外の収入伺
            stb.append(" SELECT ");
            stb.append("     INCOME_L_CD, ");
            stb.append("     INCOME_M_CD, ");
            stb.append("     INCOME_DATE, ");
            stb.append("     INCOME_MONEY, ");
            stb.append("     count(*) AS SCH_COUNT, ");
            stb.append("     '2' as sort ");
            stb.append(" FROM ");
            stb.append("     SCH_INCOME_DATA ");
            stb.append(" WHERE ");
            stb.append("     VALUE(COLLECT_DIV, '0') != '2' ");
            stb.append(" GROUP BY ");
            stb.append("     INCOME_L_CD, ");
            stb.append("     INCOME_M_CD, ");
            stb.append("     INCOME_DATE, ");
            stb.append("     INCOME_MONEY ");
            stb.append(" ORDER BY ");
            stb.append("     INCOME_L_CD, ");
            stb.append("     INCOME_M_CD, ");
            stb.append("     sort, ");
            stb.append("     INCOME_DATE, ");
            stb.append("     INCOME_MONEY desc ");

            return stb.toString();
        }


        private String getOutGoSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     OUT_D.INCOME_L_CD, ");
            stb.append("     OUT_D.INCOME_M_CD, ");
            stb.append("     OUT_D.OUTGO_DATE, ");
            stb.append("     OUT_MEISAI.OUTGO_L_CD, ");
            stb.append("     OUT_MEISAI.OUTGO_M_CD, ");
            stb.append("     OUT_MEISAI.OUTGO_S_CD, ");
            stb.append("     SUM(OUT_MEISAI.TOTAL_PRICE) AS TOTAL_PRICE ");
            stb.append(" FROM ");
            stb.append("     LEVY_REQUEST_OUTGO_MEISAI_DAT OUT_MEISAI ");
            stb.append("     INNER JOIN LEVY_REQUEST_OUTGO_DAT OUT_D ");
            stb.append("          ON OUT_MEISAI.SCHOOLCD    = OUT_D.SCHOOLCD ");
            stb.append("         AND OUT_MEISAI.SCHOOL_KIND = OUT_D.SCHOOL_KIND ");
            stb.append("         AND OUT_MEISAI.YEAR        = OUT_D.YEAR ");
            stb.append("         AND OUT_MEISAI.OUTGO_L_CD  = OUT_D.OUTGO_L_CD ");
            stb.append("         AND OUT_MEISAI.OUTGO_M_CD  = OUT_D.OUTGO_M_CD ");
            stb.append("         AND OUT_MEISAI.REQUEST_NO  = OUT_D.REQUEST_NO ");
            stb.append("         AND VALUE(OUT_D.OUTGO_APPROVAL, '0') = '1' ");
            stb.append("         AND VALUE(OUT_D.OUTGO_CANCEL, '0')  != '1' ");
            stb.append("         AND OUT_D.OUTGO_DATE BETWEEN '" + _param._fromDate + "' AND '" + _param._toDate + "' ");
            stb.append(" WHERE ");
            stb.append("         OUT_MEISAI.SCHOOLCD    = '" + _param._schoolCd + "' ");
            stb.append("     AND OUT_MEISAI.SCHOOL_KIND = '" + _schoolKind + "' ");
            stb.append("     AND OUT_MEISAI.YEAR        = '" + _param._year + "' ");
            stb.append("     AND OUT_D.OUTGO_L_CD      != '99' "); //返金伝票除く
            stb.append("     AND OUT_D.INCOME_L_CD     != '98' "); //給付伝票除く
            stb.append("     AND OUT_MEISAI.REQUEST_NO IN ( ");
            stb.append("         SELECT ");
            stb.append("             OUT_SCH.REQUEST_NO ");
            stb.append("         FROM ");
            stb.append("             LEVY_REQUEST_OUTGO_SCHREG_DAT OUT_SCH ");
            stb.append("         WHERE ");
            stb.append("                 OUT_SCH.SCHOOLCD    = '" + _param._schoolCd + "' ");
            stb.append("             AND OUT_SCH.SCHOOL_KIND = '" + _schoolKind + "' ");
            stb.append("             AND OUT_SCH.YEAR        = '" + _param._year + "' ");
            stb.append("             AND EXISTS( ");
            stb.append("                 SELECT ");
            stb.append("                     'X' ");
            stb.append("                 FROM ");
            stb.append("                     SCHREG_REGD_DAT REGD ");
            stb.append("                 WHERE ");
            stb.append("                         REGD.YEAR     = OUT_SCH.YEAR ");
            stb.append("                     AND REGD.SCHREGNO = OUT_SCH.SCHREGNO ");
            stb.append("                     AND REGD.GRADE    = '" + _grade + "' ");
            stb.append("             ) ");
            stb.append("         GROUP BY ");
            stb.append("             OUT_SCH.REQUEST_NO ");
            stb.append("         ) ");
            stb.append(" GROUP BY ");
            stb.append("     OUT_D.INCOME_L_CD, ");
            stb.append("     OUT_D.INCOME_M_CD, ");
            stb.append("     OUT_D.OUTGO_DATE, ");
            stb.append("     OUT_MEISAI.OUTGO_L_CD, ");
            stb.append("     OUT_MEISAI.OUTGO_M_CD, ");
            stb.append("     OUT_MEISAI.OUTGO_S_CD ");
            stb.append(" ORDER BY ");
            stb.append("     OUT_D.INCOME_L_CD, ");
            stb.append("     OUT_D.INCOME_M_CD, ");
            stb.append("     OUT_D.OUTGO_DATE, ");
            stb.append("     OUT_MEISAI.OUTGO_L_CD, ");
            stb.append("     OUT_MEISAI.OUTGO_M_CD, ");
            stb.append("     OUT_MEISAI.OUTGO_S_CD ");

            return stb.toString();
        }
    }

    private class Ldata {
        final String _lCd;
        final String _lName;
        final List _incomeList;
        final List _outgoList;
        public Ldata(
                final String lCd,
                final String lName
        ) {
            _lCd        = lCd;
            _lName      = lName;
            _incomeList = new ArrayList();
            _outgoList  = new ArrayList();
        }
    }

    private class IncomeData {
        final String _lCd;
        final String _mCd;
        final String _mName;
        final String _incomeDate;
        final int _incomeMoney;
        final int _schCount;
        final String _sort;
        final String[] _mNameArray;
        public IncomeData(
                final String lCd,
                final String mCd,
                final String mName,
                final String incomeDate,
                final int incomeMoney,
                final int schCount,
                final String sort
        ) {
            _lCd            = lCd;
            _mCd            = mCd;
            _mName          = mName;
            _incomeDate     = incomeDate;
            _incomeMoney    = incomeMoney;
            _schCount       = schCount;
            _sort           = sort;
            _mNameArray     = KNJ_EditEdit.get_token(mName, 6, 10);
        }
    }

    private class OutoGoData {
        final String _incomLCd;
        final String _incomMCd;
        final String _outgoDate;
        final String _mName;
        final String _sName;
        final String[] _mNameArray;
        final int _outGoMoney;
        public OutoGoData(
                final String incomLCd,
                final String incomMCd,
                final String outgoDate,
                final String mName,
                final String sName,
                final int outGoMoney
        ) {
            _incomLCd       = incomLCd;
            _incomMCd       = incomMCd;
            _outgoDate      = outgoDate;
            _mName          = mName;
            _mNameArray     = KNJ_EditEdit.get_token(mName, 6, 10);
            _sName          = sName;
            _outGoMoney     = outGoMoney;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 73675 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _grade;
        private final String _month;
        private final String _schoolKind;
        private final String _schoolCd;
        private final String _useSchoolKindField;
        private final String _usePrgSchoolkind;
        private final String _selectSchoolKind;
        private final String _fromDate;
        private final String _toDate;
        private final Map _schoolNameMap;
        private final Map _levyLMstMap;
        private final Map _levyMMstMap;
        private final Map _levySMstMap;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year               = request.getParameter("YEAR");
            _grade              = request.getParameter("GRADE");
            _month              = request.getParameter("MONTH");
            _fromDate			= request.getParameter("FROM_DATE").replace("/", "-");
            _toDate				= request.getParameter("TO_DATE").replace("/", "-");;
            _schoolKind         = request.getParameter("SCHOOL_KIND");
            _schoolCd           = request.getParameter("SCHOOLCD");
            _useSchoolKindField = request.getParameter("useSchool_KindField");
            _usePrgSchoolkind   = request.getParameter("use_prg_schoolkind");
            _selectSchoolKind   = request.getParameter("selectSchoolKind");
            _schoolNameMap      = getSchoolName(db2, _year);
            _levyLMstMap        = getLevyLMst(db2);
            _levyMMstMap        = getLevyMMst(db2);
            _levySMstMap        = getLevySMst(db2);
        }

        /** LEVY_L_MST */
        private Map getLevyLMst(final DB2UDB db2) {
            Map retMap = new TreeMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     SCHOOL_KIND, ");
                stb.append("     LEVY_L_CD, ");
                stb.append("     LEVY_L_NAME ");
                stb.append(" FROM ");
                stb.append("     LEVY_L_MST ");
                stb.append(" WHERE ");
                stb.append("         SCHOOLCD    = '" + _schoolCd + "' ");
                stb.append("     AND YEAR        = '" + _year + "' ");

                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String schoolKind = rs.getString("SCHOOL_KIND");
                    final String lCd        = rs.getString("LEVY_L_CD");
                    final String lName      = rs.getString("LEVY_L_NAME");

                    retMap.put(schoolKind + lCd, lName);
                }
            } catch (SQLException ex) {
                log.debug("getLevyLMst exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retMap;
        }

        /** LEVY_M_MST */
        private Map getLevyMMst(final DB2UDB db2) {
            Map retMap = new TreeMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     SCHOOL_KIND, ");
                stb.append("     LEVY_L_CD, ");
                stb.append("     LEVY_M_CD, ");
                stb.append("     LEVY_M_NAME ");
                stb.append(" FROM ");
                stb.append("     LEVY_M_MST ");
                stb.append(" WHERE ");
                stb.append("         SCHOOLCD    = '" + _schoolCd + "' ");
                stb.append("     AND YEAR        = '" + _year + "' ");

                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String schoolKind = rs.getString("SCHOOL_KIND");
                    final String lCd        = rs.getString("LEVY_L_CD");
                    final String mCd        = rs.getString("LEVY_M_CD");
                    final String mName      = rs.getString("LEVY_M_NAME");

                    retMap.put(schoolKind + lCd + mCd, mName);
                }
            } catch (SQLException ex) {
                log.debug("getLevyMMst exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retMap;
        }

        /** LEVY_S_MST */
        private Map getLevySMst(final DB2UDB db2) {
            Map retMap = new TreeMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     SCHOOL_KIND, ");
                stb.append("     LEVY_L_CD, ");
                stb.append("     LEVY_M_CD, ");
                stb.append("     LEVY_S_CD, ");
                stb.append("     LEVY_S_NAME ");
                stb.append(" FROM ");
                stb.append("     LEVY_S_MST ");
                stb.append(" WHERE ");
                stb.append("         SCHOOLCD    = '" + _schoolCd + "' ");
                stb.append("     AND YEAR        = '" + _year + "' ");

                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String schoolKind = rs.getString("SCHOOL_KIND");
                    final String lCd        = rs.getString("LEVY_L_CD");
                    final String mCd        = rs.getString("LEVY_M_CD");
                    final String sCd        = rs.getString("LEVY_S_CD");
                    final String sName      = rs.getString("LEVY_S_NAME");

                    retMap.put(schoolKind + lCd + mCd + sCd, sName);
                }
            } catch (SQLException ex) {
                log.debug("getLevySMst exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retMap;
        }

        private Map getSchoolName(final DB2UDB db2, final String year) {
            Map retMap = new TreeMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("    SCHOOL_KIND, ");
                stb.append("    SCHOOLNAME1 ");
                stb.append(" FROM ");
                stb.append("    SCHOOL_MST ");
                stb.append(" WHERE ");
                stb.append("    YEAR = '" + year + "' ");

                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String schoolKind = rs.getString("SCHOOL_KIND");
                    final String schoolName = rs.getString("SCHOOLNAME1");

                    retMap.put(schoolKind, schoolName);
                }
            } catch (SQLException ex) {
                log.debug("getSCHOOL_MST exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retMap;
        }

    }
}
// eof
