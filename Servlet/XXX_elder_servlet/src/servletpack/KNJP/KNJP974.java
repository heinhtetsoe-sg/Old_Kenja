/**     プロジェクト/プロパティ/javaコード・スタイル/コード・テンプレート/パターン       ***/

/*
 * $Id: 275ba0fd942c77d1888ef81bd136534c38722a3c $
 *
 * 作成日: 2019/02/27
 * 作成者: yamashiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJP;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
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

public class KNJP974 {

    private static final Log log = LogFactory.getLog(KNJP974.class);

    private boolean _hasData;
    private final String REPAY_LMS = "9999999";

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

        final List printGradeList = getGradeList(db2);
        for (Iterator itGrade = printGradeList.iterator(); itGrade.hasNext();) {
            final Gdat gdat = (Gdat) itGrade.next();
            for (Iterator itLmst = gdat._lmstPrintDataMap.keySet().iterator(); itLmst.hasNext();) {
                svf.VrSetForm("KNJP974.frm", 4);
                final String lCd = (String) itLmst.next();
                final Ldata ldata = (Ldata) gdat._lmstPrintDataMap.get(lCd);

                final String nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_param._year)) + "年度";
                final String setTitle = nendo + _param._schoolName + gdat._gradeName1 + ldata._lName + "会計報告書";
                final int titleLen = KNJ_EditEdit.getMS932ByteLength(setTitle);
                final String titleField = titleLen > 60 ? "3" : titleLen > 50 ? "2" : "1";
                svf.VrsOut("TITLE" + titleField, setTitle);

                //収入
                svf.VrsOut("KIND", "(収　入)");
                svf.VrsOut("UNIT", "(単位：円)");
                svf.VrEndRecord();

                int lineCnt = 1;
                int totalIncomeMoney = 0;
                for (Iterator itIncome = ldata._incomeList.iterator(); itIncome.hasNext();) {
                    final IncomeData incomeData = (IncomeData) itIncome.next();
                    svf.VrsOut("NO1", String.valueOf(lineCnt));
                    final String setItem1 = incomeData._mName + "（" + nfNum.format(incomeData._incomeMoney) + "円 × " + String.valueOf(incomeData._schCount) + "人）";
                    svf.VrsOut("ITEM1", setItem1);
                    final int setPrice1 = incomeData._incomeMoney * incomeData._schCount;
                    svf.VrsOut("PRICE1", String.valueOf(setPrice1));
                    totalIncomeMoney += setPrice1;
                    lineCnt++;
                    svf.VrEndRecord();
                }
                svf.VrsOut("TOTAL_NAME", "合計");
                svf.VrsOut("PRICE3", String.valueOf(totalIncomeMoney));
                svf.VrEndRecord();
                _hasData = true;

                //空行
                printBlankLine(svf);

                //支出
                svf.VrsOut("KIND", "(支　出)");
                svf.VrEndRecord();

                lineCnt = 1;
                int totalOutGoMoney = 0;
                int totalRepay = 0;
                int mNameLen = 0;
                int mCount = 0;
                int sIdx = 0;
                String befMcd = "";
                for (Iterator itIncome = ldata._outgoList.iterator(); itIncome.hasNext();) {
                    final OutoGoData outoGoData = (OutoGoData) itIncome.next();
                    if ("1".equals(outoGoData._henkinFlg)) {
                        totalRepay += outoGoData._outGoMoney;
                        continue;
                    }
                    if (!befMcd.equals(outoGoData._mCd)) {
                        //項目名を印字開始する行数をセット
                        final double tmp = Integer.parseInt((String) _param._sCdCntMap.get(gdat._grade + lCd + outoGoData._mCd)) - outoGoData._mNameArrayCnt + 1;
                        if (0 < tmp) {
                            sIdx = (int) Math.ceil(tmp / 2);
                        } else {
                            sIdx = 1;
                        }
                        mNameLen = 0;
                        mCount = 0;
                    }
                    svf.VrsOut("NO2", String.valueOf(lineCnt));
                    svf.VrsOut("GRPCD", outoGoData._mCd);
                    mCount++;
                    if (sIdx <= mCount) {
                        if (outoGoData._mNameArray[mNameLen] != null) {
                            svf.VrsOut("ITEM2", outoGoData._mNameArray[mNameLen]);
                            mNameLen++;
                        }
                    }
                    final String itemField = KNJ_EditEdit.getMS932ByteLength(outoGoData._sName) > 70 ? "2" : "1";
                    svf.VrsOut("ITEM3_" + itemField, outoGoData._sName);
                    svf.VrsOut("PRICE2", String.valueOf(outoGoData._outGoMoney));
                    totalOutGoMoney += outoGoData._outGoMoney;
                    lineCnt++;
                    befMcd = outoGoData._mCd;
                    svf.VrEndRecord();
                }
                svf.VrsOut("TOTAL_NAME", "合計");
                svf.VrsOut("PRICE3", String.valueOf(totalOutGoMoney));
                svf.VrEndRecord();

                //空行
                printBlankLine(svf);

                //差引
                svf.VrsOut("KIND", "(差　引)");
                svf.VrEndRecord();

                final int diffMoney = totalIncomeMoney - totalOutGoMoney;
                svf.VrsOut("PRICE4", String.valueOf(diffMoney));
                svf.VrEndRecord();

                //空行
                printBlankLine(svf);

                //返金額
                svf.VrsOut("KIND", "(返金額)");
                svf.VrEndRecord();

                svf.VrsOut("ITEM4", "監査後返金予定");
                svf.VrsOut("PRICE4", String.valueOf(totalRepay));
                svf.VrEndRecord();

                //空行
                printBlankLine(svf);

                //残額
                svf.VrsOut("KIND", "(残　額)");
                svf.VrEndRecord();

                svf.VrsOut("ITEM4", ldata._lName);
                final int balanceMoney = diffMoney - totalRepay;
                svf.VrsOut("PRICE4", String.valueOf(balanceMoney));
                svf.VrEndRecord();

                svf.VrsOut("UNIT", "翌年度への繰越");
                svf.VrEndRecord();

                //通帳残額
                svf.VrsOut("KIND", "(通帳残額)");
                svf.VrEndRecord();

                svf.VrsOut("ITEM5", "未返金につき差引額を相当");
                svf.VrsOut("PRICE5", String.valueOf(totalRepay + balanceMoney));
                svf.VrEndRecord();

                _hasData = true;
            }

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
                final String grade = rs.getString("GRADE");
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
            _schoolKind = schoolKind;
            _grade = grade;
            _gradeName1 = gradeName1;
            _lmstPrintDataMap = getLmstMap(db2, _grade);
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
                    final String lCd = rsIncome.getString("INCOME_L_CD");
                    final String mCd = rsIncome.getString("INCOME_M_CD");
                    final String lName = rsIncome.getString("LEVY_L_NAME");
                    final String mName = rsIncome.getString("LEVY_M_NAME");
                    final int incomeMoney = rsIncome.getInt("INCOME_MONEY");
                    final int schCount    = rsIncome.getInt("SCH_COUNT");
                    Ldata lData = null;
                    if (retMap.containsKey(lCd)) {
                        lData = (Ldata) retMap.get(lCd);
                    } else {
                        lData = new Ldata(lCd, lName);
                        retMap.put(lCd, lData);
                    }
                    final IncomeData incomeData = new IncomeData(mCd, mName, incomeMoney, schCount);
                    lData._incomeList.add(incomeData);
                }
            } catch (SQLException ex) {
                log.debug("getGradeList exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, psIncome, rsIncome);
                db2.commit();
            }

            //支出
            PreparedStatement psOutGo = null;
            ResultSet rsOutGo = null;
            int sCdCnt = 0;
            try {
                final String outGoSql = getOutGoSql();
                psOutGo = db2.prepareStatement(outGoSql);
                rsOutGo = psOutGo.executeQuery();
                while (rsOutGo.next()) {
                    final String lCd = rsOutGo.getString("OUTGO_L_CD");
                    final String mCd = rsOutGo.getString("OUTGO_M_CD");
                    final String sCd = rsOutGo.getString("OUTGO_S_CD");
                    final String lName = rsOutGo.getString("LEVY_L_NAME");
                    final String mName = rsOutGo.getString("LEVY_M_NAME");
                    final String sName = rsOutGo.getString("LEVY_S_NAME");
                    final int outGoMoney = rsOutGo.getInt("TOTAL_PRICE");
                    final String henkinFlg = rsOutGo.getString("HENKIN_FLG");
                    final String henkinApproval = rsOutGo.getString("HENKIN_APPROVAL");
                    Ldata lData = null;
                    if (retMap.containsKey(lCd)) {
                        lData = (Ldata) retMap.get(lCd);
                    } else {
                        lData = new Ldata(lCd, lName);
                        retMap.put(lCd, lData);
                    }
                    if (!_param._sCdCntMap.containsKey(grade + lCd + mCd)) {
                        sCdCnt = 0;
                    }
                    sCdCnt++;
                    _param._sCdCntMap.put(grade + lCd + mCd, String.valueOf(sCdCnt));
                    final OutoGoData outoGoData = new OutoGoData(mCd, sCd, mName, sName, outGoMoney, henkinFlg, henkinApproval);
                    lData._outgoList.add(outoGoData);
                }
            } catch (SQLException ex) {
                log.debug("getGradeList exception!", ex);
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
            stb.append("         L_MST.LEVY_L_NAME, ");
            stb.append("         M_MST.LEVY_M_NAME, ");
            stb.append("         INC_S.SCHREGNO, ");
            stb.append("         sum(INC_S.INCOME_MONEY) AS INCOME_MONEY ");
            stb.append("     FROM ");
            stb.append("         LEVY_REQUEST_INCOME_SCHREG_DAT INC_S ");
            stb.append("         LEFT JOIN LEVY_REQUEST_INCOME_DAT INC_D ON INC_D.SCHOOLCD    = INC_S.SCHOOLCD ");
            stb.append("              AND INC_D.SCHOOL_KIND = INC_S.SCHOOL_KIND ");
            stb.append("              AND INC_D.YEAR        = INC_S.YEAR ");
            stb.append("              AND INC_D.INCOME_L_CD = INC_S.INCOME_L_CD ");
            stb.append("              AND INC_D.INCOME_M_CD = INC_S.INCOME_M_CD ");
            stb.append("              AND INC_D.REQUEST_NO  = INC_S.REQUEST_NO ");
            stb.append("         	  AND INC_D.INCOME_DATE BETWEEN '" + _param._fromDate + "' AND '" + _param._toDate + "' ");
            stb.append("         LEFT JOIN LEVY_L_MST L_MST ON INC_D.SCHOOLCD    = L_MST.SCHOOLCD ");
            stb.append("                                   AND INC_D.SCHOOL_KIND = L_MST.SCHOOL_KIND ");
            stb.append("                                   AND INC_D.YEAR        = L_MST.YEAR ");
            stb.append("                                   AND INC_D.INCOME_L_CD = L_MST.LEVY_L_CD ");
            stb.append("         LEFT JOIN LEVY_M_MST M_MST ON INC_D.SCHOOLCD    = M_MST.SCHOOLCD ");
            stb.append("                                   AND INC_D.SCHOOL_KIND = M_MST.SCHOOL_KIND ");
            stb.append("                                   AND INC_D.YEAR        = M_MST.YEAR ");
            stb.append("                                   AND INC_D.INCOME_L_CD = M_MST.LEVY_L_CD ");
            stb.append("                                   AND INC_D.INCOME_M_CD = M_MST.LEVY_M_CD ");
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
            stb.append("         L_MST.LEVY_L_NAME, ");
            stb.append("         M_MST.LEVY_M_NAME, ");
            stb.append("         INC_S.SCHREGNO ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     INCOME_L_CD, ");
            stb.append("     INCOME_M_CD, ");
            stb.append("     LEVY_L_NAME, ");
            stb.append("     LEVY_M_NAME, ");
            stb.append("     INCOME_MONEY, ");
            stb.append("     count(*) AS SCH_COUNT ");
            stb.append(" FROM ");
            stb.append("     SCH_INCOME_DATA ");
            stb.append(" GROUP BY ");
            stb.append("     INCOME_L_CD, ");
            stb.append("     INCOME_M_CD, ");
            stb.append("     LEVY_L_NAME, ");
            stb.append("     LEVY_M_NAME, ");
            stb.append("     INCOME_MONEY ");
            stb.append(" ORDER BY ");
            stb.append("     INCOME_L_CD, ");
            stb.append("     INCOME_M_CD, ");
            stb.append("     INCOME_MONEY desc ");

            return stb.toString();
        }

        private String getOutGoSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     OUT_MEISAI.OUTGO_L_CD, ");
            stb.append("     OUT_MEISAI.OUTGO_M_CD, ");
            stb.append("     OUT_MEISAI.OUTGO_S_CD, ");
            stb.append("     L_MST.LEVY_L_NAME, ");
            stb.append("     M_MST.LEVY_M_NAME, ");
            stb.append("     S_MST.LEVY_S_NAME, ");
            stb.append("     VALUE(OUT_D.HENKIN_FLG, '0') AS HENKIN_FLG, ");
            stb.append("     VALUE(OUT_D.HENKIN_APPROVAL, '0') AS HENKIN_APPROVAL, ");
            stb.append("     SUM(OUT_MEISAI.TOTAL_PRICE) AS TOTAL_PRICE ");
            stb.append(" FROM ");
            stb.append("     LEVY_REQUEST_OUTGO_MEISAI_DAT OUT_MEISAI ");
            stb.append("     INNER JOIN LEVY_REQUEST_OUTGO_DAT OUT_D ON OUT_MEISAI.SCHOOLCD = OUT_D.SCHOOLCD ");
            stb.append("           AND OUT_MEISAI.SCHOOL_KIND = OUT_D.SCHOOL_KIND ");
            stb.append("           AND OUT_MEISAI.YEAR = OUT_D.YEAR ");
            stb.append("           AND OUT_MEISAI.OUTGO_L_CD = OUT_D.OUTGO_L_CD ");
            stb.append("           AND OUT_MEISAI.OUTGO_M_CD = OUT_D.OUTGO_M_CD ");
            stb.append("           AND OUT_MEISAI.REQUEST_NO = OUT_D.REQUEST_NO ");
            stb.append("           AND VALUE(OUT_D.OUTGO_APPROVAL, '0') = '1' ");
            stb.append("           AND VALUE(OUT_D.OUTGO_CANCEL, '0') != '1' ");
            stb.append("           AND OUT_D.OUTGO_DATE BETWEEN '" + _param._fromDate + "' AND '" + _param._toDate + "' ");
            stb.append("     LEFT JOIN LEVY_L_MST L_MST ON OUT_MEISAI.SCHOOLCD = L_MST.SCHOOLCD ");
            stb.append("          AND OUT_MEISAI.SCHOOL_KIND = L_MST.SCHOOL_KIND ");
            stb.append("          AND OUT_MEISAI.YEAR = L_MST.YEAR ");
            stb.append("          AND OUT_MEISAI.OUTGO_L_CD = L_MST.LEVY_L_CD ");
            stb.append("     LEFT JOIN LEVY_M_MST M_MST ON OUT_MEISAI.SCHOOLCD = M_MST.SCHOOLCD ");
            stb.append("          AND OUT_MEISAI.SCHOOL_KIND = M_MST.SCHOOL_KIND ");
            stb.append("          AND OUT_MEISAI.YEAR = M_MST.YEAR ");
            stb.append("          AND OUT_MEISAI.OUTGO_L_CD = M_MST.LEVY_L_CD ");
            stb.append("          AND OUT_MEISAI.OUTGO_M_CD = M_MST.LEVY_M_CD ");
            stb.append("     LEFT JOIN LEVY_S_MST S_MST ON OUT_MEISAI.SCHOOLCD = S_MST.SCHOOLCD ");
            stb.append("          AND OUT_MEISAI.SCHOOL_KIND = S_MST.SCHOOL_KIND ");
            stb.append("          AND OUT_MEISAI.YEAR = S_MST.YEAR ");
            stb.append("          AND OUT_MEISAI.OUTGO_L_CD = S_MST.LEVY_L_CD ");
            stb.append("          AND OUT_MEISAI.OUTGO_M_CD = S_MST.LEVY_M_CD ");
            stb.append("          AND OUT_MEISAI.OUTGO_S_CD = S_MST.LEVY_S_CD ");
            stb.append(" WHERE ");
            stb.append("     OUT_MEISAI.SCHOOLCD = '" + _param._schoolCd + "' ");
            stb.append("     AND OUT_MEISAI.SCHOOL_KIND = '" + _schoolKind + "' ");
            stb.append("     AND OUT_MEISAI.YEAR = '" + _param._year + "' ");
            stb.append("     AND OUT_MEISAI.REQUEST_NO IN ( ");
            stb.append("         SELECT ");
            stb.append("             OUT_SCH.REQUEST_NO ");
            stb.append("         FROM ");
            stb.append("             LEVY_REQUEST_OUTGO_SCHREG_DAT OUT_SCH ");
            stb.append("         WHERE ");
            stb.append("             OUT_SCH.SCHOOLCD = '" + _param._schoolCd + "' ");
            stb.append("             AND OUT_SCH.SCHOOL_KIND = '" + _schoolKind + "' ");
            stb.append("             AND OUT_SCH.YEAR = '" + _param._year + "' ");
            stb.append("             AND EXISTS( ");
            stb.append("                 SELECT ");
            stb.append("                     'X' ");
            stb.append("                 FROM ");
            stb.append("                     SCHREG_REGD_DAT REGD ");
            stb.append("                 WHERE ");
            stb.append("                     REGD.YEAR = OUT_SCH.YEAR ");
            stb.append("                     AND REGD.SCHREGNO = OUT_SCH.SCHREGNO ");
            stb.append("                     AND REGD.GRADE = '" + _grade + "' ");
            stb.append("             ) ");
            stb.append("         GROUP BY ");
            stb.append("             OUT_SCH.REQUEST_NO ");
            stb.append(" ) ");
            stb.append(" GROUP BY ");
            stb.append("     OUT_MEISAI.OUTGO_L_CD, ");
            stb.append("     OUT_MEISAI.OUTGO_M_CD, ");
            stb.append("     OUT_MEISAI.OUTGO_S_CD, ");
            stb.append("     L_MST.LEVY_L_NAME, ");
            stb.append("     M_MST.LEVY_M_NAME, ");
            stb.append("     S_MST.LEVY_S_NAME, ");
            stb.append("     VALUE(OUT_D.HENKIN_FLG, '0'), ");
            stb.append("     VALUE(OUT_D.HENKIN_APPROVAL, '0') ");
            stb.append(" ORDER BY ");
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
            _lCd = lCd;
            _lName = lName;
            _incomeList = new ArrayList();
            _outgoList = new ArrayList();
        }
    }

    private class IncomeData {
        final String _mCd;
        final String _mName;
        final int _incomeMoney;
        final int _schCount;
        public IncomeData(
                final String mCd,
                final String mName,
                final int incomeMoney,
                final int schCount
        ) {
            _mCd = mCd;
            _mName = mName;
            _incomeMoney = incomeMoney;
            _schCount = schCount;
        }
    }

    private class OutoGoData {
        final String _mCd;
        final String _sCd;
        final String _mName;
        final String[] _mNameArray;
        final int _mNameArrayCnt;
        final String _sName;
        final int _outGoMoney;
        final String _henkinFlg;
        final String _henkinApproval;
        public OutoGoData(
                final String mCd,
                final String sCd,
                final String mName,
                final String sName,
                final int outGoMoney,
                final String henkinFlg,
                final String henkinApproval
        ) {
            _mCd = mCd;
            _sCd = sCd;
            _mName = mName;
            _mNameArray = KNJ_EditEdit.get_token_1(mName, 6, 10);
            _mNameArrayCnt = getCount(_mNameArray);
            _sName = sName;
            _outGoMoney = outGoMoney;
            _henkinFlg = henkinFlg;
            _henkinApproval = henkinApproval;
        }

        private int getCount(final String[] nameArray) {
            int retInt = 0;
            if (nameArray == null) return retInt;
            for (int i = 0; i < nameArray.length; i++) {
                String string = nameArray[i];
                if (string != null) {
                    retInt++;
                }
            }

            return retInt;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 73844 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _grade;
        
        private final String _month;
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final String _schoolKind;
        private final String _schoolCd;
        private final String _useSchoolKindField;
        private final String _fromDate;
        private final String _toDate;
        private final String _usePrgSchoolkind;
        private final String _selectSchoolKind;
        private final String _schoolName;
        private final Map _sCdCntMap = new HashMap();

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _grade = request.getParameter("GRADE");
            _month = request.getParameter("MONTH");
            _fromDate			= request.getParameter("FROM_DATE").replace("/", "-");
            _toDate				= request.getParameter("TO_DATE").replace("/", "-");;
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _schoolKind = request.getParameter("SCHOOL_KIND");
            _schoolCd = request.getParameter("SCHOOLCD");
            _useSchoolKindField = request.getParameter("useSchool_KindField");
            _usePrgSchoolkind = request.getParameter("use_prg_schoolkind");
            _selectSchoolKind = request.getParameter("selectSchoolKind");
            _schoolName = getSchoolName(db2, _year);
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
