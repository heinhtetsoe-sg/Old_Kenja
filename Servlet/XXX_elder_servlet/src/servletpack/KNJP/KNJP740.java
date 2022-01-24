/**     プロジェクト/プロパティ/javaコード・スタイル/コード・テンプレート/パターン       ***/

/*
 * $Id: 0de6477fe80d6b1a67325d789103752781594dd7 $
 *
 * 作成日: 2018/06/28
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
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJP740 {

    private static final Log log = LogFactory.getLog(KNJP740.class);

    private static final String HIKIOTOSHI = "1";
    private static final String HENKIN      = "2";

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

            if (HIKIOTOSHI.equals(_param._output)) {
                if ("1".equals(_param._printType)) {
                    printMain(db2, svf); // 自動払込書
                } else {
                    printMain2(db2, svf); // 請求データ明細リスト
                }
            } else {
                if ("1".equals(_param._printType)) {
                    printMain3(db2, svf); // 振替払出書
                } else {
                    printMain4(db2, svf); // 返金データ明細リスト
                }
            }
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

    /** 自動払込書*/
    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrSetForm("KNJP740.frm", 1);
        svf.VrsOut("DATE", KNJ_EditDate.h_format_SeirekiJP(StringUtils.replace(_param._printDate, "/", "-")));
        svf.VrsOut("ACCOUNT_NO", "0" + _param._printkouzakigou + "0-" + _param._printkyokuban);
        svf.VrsOut("ACCOUNT_NAME", "千代田区立九段中等教育学校");
        svf.VrsOut("PAY_DATE1", _param._printlimitdate2.substring(0, 2) + "月" + _param._printlimitdate2.substring(2) + "日");
        svf.VrsOut("PAY_DATE2", _param._printsaihuri.substring(0, 2) + "月" + _param._printsaihuri.substring(2) + "日");
        svf.VrsOut("TOTAL_NUM", _param._printtotalcnt);
        svf.VrsOut("TOTAL_MONEY", _param._printtotalmoney);
        svf.VrsOut("KIND", _param._printsyubetsu);
        svf.VrsOut("COMPANY_NO", _param._printjigyounushi);

        svf.VrEndPage();
        _hasData = true;
    }

    /** 請求データ明細リスト*/
    private void printMain2(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrSetForm("KNJP740_2.frm", 4);
        final List printList = getList(db2);

        final int maxLine = 50;
        int line = 1;
        int page = 1;

        //ヘッダーセット
        svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(db2, _param._ctrl_date)); // 作成日付
        final String maxPage = String.valueOf((_param._dataCnt / maxLine) + ((_param._dataCnt % maxLine == 0) ? 0: 1));
        svf.VrsOut("TOTALPAGE", maxPage); // 最大ページ数

        svf.VrsOut("SYUBETSU", _param._printsyubetsu); // 契約種別コード
        svf.VrsOut("JC_CD", _param._printUkeireJcCd);  // 授入JCコード
        svf.VrsOut("JIGYOUSYU_NO", _param._printjigyounushi);       // 事業主番号
        svf.VrsOut("JIGYOUSYU_NAME", "ﾁﾖﾀﾞｸﾘﾂｸﾀﾞﾝﾁｭｳﾄｳｷｮｳｲｸｶﾞｯｺｳ"); // 事業主名
        svf.VrsOut("LIMIT_DATE", _param._printlimitdate2);       // 請求月日
        svf.VrsOut("RETRANSFER_DATE", _param._printsaihuri);    // 再振替日
        svf.VrsOut("SYUNOU_BRANCHCD", _param._printkouzakigou); // 収納口座記号
        svf.VrsOut("SYUNOU_ACCOUNTNO", _param._printkyokuban);  // 収納口座番号

        for (Iterator iterator = printList.iterator(); iterator.hasNext();) {
            final PrintData printData = (PrintData) iterator.next();
            if (line > maxLine) {
                line = 1;
                page++;
            }
            svf.VrsOut("PAGE", String.valueOf(page)); // ページ数

            svf.VrsOut("SCHREGNO",        printData._schregNo);    // 生徒番号
            svf.VrsOut("SCH_ACCOUNTNAME", printData._accountName); // 通帳名義
            svf.VrsOut("SCH_BRANCHCD",    printData._branchCd);    // 記号
            svf.VrsOut("SCH_ACCOUNTNO",   printData._accountNo);   // 番号
            svf.VrsOut("PLAN_MONEY",      printData._planMoney);   // 請求金額
            svf.VrsOut("LIMIT_DATE_YM",   printData._yearMonth);   // 請求年月

            line++;
            svf.VrEndRecord();
            _hasData = true;
        }
        for (int i = line; i <= maxLine; i++) {
            svf.VrEndRecord();
        }
    }

    /** 振替払出書*/
    private void printMain3(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrSetForm("KNJP740_3.frm", 1);

        //送付日
        svf.VrsOut("DATE", KNJ_EditDate.h_format_SeirekiJP(StringUtils.replace(_param._printDate, "/", "-")));

        //口座記号番号
        svf.VrsOut("ACCOUNT_NO", "0" + _param._printkouzakigou + "0-" + _param._printkyokuban);

        //口座加入者名
        svf.VrsOut("ACCOUNT_NAME", "千代田区立九段中等教育学校");

        //住所
        svf.VrsOut("ZIP_NO", "〒 " + (String) _param._schoolMap.get("SCHOOLZIPCD"));
        svf.VrsOut("TEL_NO", "TEL " + (String) _param._schoolMap.get("SCHOOLTELNO"));
        svf.VrsOut("ADDR",   (String) _param._schoolMap.get("SCHOOLADDR"));

        //送金日
        svf.VrsOut("PAY_DATE1", _param._printlimitdate.substring(0, 2) + "月" + _param._printlimitdate.substring(2) + "日");

        //合計件数
        svf.VrsOut("TOTAL_NUM", _param._printtotalcnt);

        //合計金額
        svf.VrsOut("TOTAL_MONEY", _param._printtotalmoney);

        //取扱局番
        svf.VrsOut("HANDLE_NO", _param._toriKyouName);

        //事業主番号
        svf.VrsOut("COMPANY_NO", _param._printjigyounushi);

        svf.VrEndPage();
        _hasData = true;
    }

    /** 返金データ明細リスト*/
    private void printMain4(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrSetForm("KNJP740_4.frm", 4);
        final List printList = getListHenkin(db2);

        final int maxLine = 25;
        int line = 1;
        int page = 1;
        int no   = 1;
        String setGradeHrClass = "";
        String befGradeHrClass = "";

        //ヘッダーセット
        svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(db2, _param._ctrl_date)); // 作成日付
        final String schoolName = "千代田区立九段中等教育学校";
        svf.VrsOut("SCHOOL_NAME", schoolName);
        final String maxPage = String.valueOf((_param._dataCnt / maxLine) + ((_param._dataCnt % maxLine == 0) ? 0: 1));
        svf.VrsOut("TOTALPAGE", maxPage); // 最大ページ数

        svf.VrsOut("SYUBETSU", _param._printsyubetsu); // 契約種別コード
        svf.VrsOut("JC_CD", _param._printUkeireJcCd);  // 授入JCコード
        svf.VrsOut("JIGYOUSYU_NO", _param._printjigyounushi);       // 事業主番号
        svf.VrsOut("JIGYOUSYU_NAME", "ﾁﾖﾀﾞｸﾘﾂｸﾀﾞﾝﾁｭｳﾄｳｷｮｳｲｸｶﾞｯｺｳ"); // 事業主名
        svf.VrsOut("LIMIT_DATE", _param._printlimitdate);       // 請求月日
        svf.VrsOut("RETRANSFER_DATE", _param._printsaihuri);    // 再振替日
        svf.VrsOut("SYUNOU_BRANCHCD", _param._printkouzakigou); // 収納口座記号
        svf.VrsOut("SYUNOU_ACCOUNTNO", _param._printkyokuban);  // 収納口座番号

        for (Iterator iterator = printList.iterator(); iterator.hasNext();) {
            final PrintDataHenkin printData = (PrintDataHenkin) iterator.next();
            if (line > maxLine) {
                line = 1;
                page++;
            }
            svf.VrsOut("PAGE", String.valueOf(page)); // ページ数

            svf.VrsOut("NO",                String.valueOf(no));       // 行番号
            if ("00-000".equals(printData._gradeHrClass)) {
                setGradeHrClass = "";
            } else if (!befGradeHrClass.equals(printData._gradeHrClass)) {
                setGradeHrClass = printData._gradeHrClass;
            } else {
                setGradeHrClass = "";
            }
            svf.VrsOut("HR_NAME",           setGradeHrClass);           // 学年-クラス
            svf.VrsOut("SEX",               printData._sex);            // 性別
            svf.VrsOut("SCHREGNO",          printData._schregNo);       // 生徒番号
            final String nameField = KNJ_EditEdit.getMS932ByteLength(printData._schName) > 30 ? "3":KNJ_EditEdit.getMS932ByteLength(printData._schName) > 20 ? "2": "1";
            svf.VrsOut("NAME" + nameField,  printData._schName);        // 児童/生徒氏名
            final String gNF = KNJ_EditEdit.getMS932ByteLength(printData._gName) > 30 ? "3":KNJ_EditEdit.getMS932ByteLength(printData._gName) > 20 ? "2": "1";
            svf.VrsOut("GUARD_NAME" +  gNF, printData._gName);          // 保護者氏名
            svf.VrsOut("SCH_ACCOUNTNAME",   printData._accountName);    // 通帳名義
            final String setBranchCd = "1" + printData._branchCd + "0-" + printData._accountNo + "1";
            svf.VrsOut("SCH_BRANCHCD",      setBranchCd);               // 通常貯金記号番号
            // 手数料を引く
            String setPlanMoney = String.valueOf(Integer.parseInt(printData._planMoney) - Integer.parseInt(printData._transferFee)) ;
            if ("1".equals(_param._useBenefit)) {
            	setPlanMoney = String.valueOf(Integer.parseInt(setPlanMoney) + Integer.parseInt(printData._kyufuMoney)) ;
            }
            svf.VrsOut("MONEY",             setPlanMoney);      // 金額

            befGradeHrClass = printData._gradeHrClass;
            line++;
            no++;
            svf.VrEndRecord();
            _hasData = true;
        }
        for (int i = line; i <= maxLine; i++) {
            svf.VrEndRecord();
        }
    }

    private List getList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getSql(_param);
            log.fatal(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String schregNo    = rs.getString("SCHREGNO");
                final String accountName = rs.getString("ACCOUNTNAME");
                final String branchCd    = rs.getString("BRANCHCD");
                final String accountNo   = rs.getString("ACCOUNTNO");
                final long pmCalcWk = rs.getLong("PLAN_MONEY") - (rs.getLong("COUNTRY_MONEY") + rs.getLong("COUNTRY_ADDMONEY") + rs.getLong("PREF_MONEY1") + rs.getLong("PREF_MONEY2") + rs.getLong("BURDEN_CHARGE1") + rs.getLong("BURDEN_CHARGE2") + rs.getLong("SCHOOL_1") + rs.getLong("SCHOOL_2"));
                final String planMoney   = String.valueOf(pmCalcWk);
                final String yearMonth   = rs.getString("PLAN_YEAR") + rs.getString("PLAN_MONTH");

                final PrintData printData = new PrintData(schregNo, accountName, branchCd, accountNo, planMoney, yearMonth);
                retList.add(printData);
                _param._dataCnt++;
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    /** 請求データ取得SQL **/
    private String getSql(final Param param) {
        String sep = "";
        String setMonthIn = "(";
        for (int i = 0; i < param._monthArray.length; i++) {
            setMonthIn += sep + "'" + param._monthArray[i] + "'";
            sep = ",";
            if (param._monthArray[i].equals(param._month)) break;
        }
        setMonthIn += ")";

        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH MAIN_DATA AS ( ");
        stb.append("     SELECT ");
        stb.append("         PLAN.SCHREGNO, ");
        stb.append("         REGD.GRADE, ");
        stb.append("         REGD.HR_CLASS, ");
        stb.append("         REGD.ATTENDNO, ");
        if (!("".equals(param._transfer_date) || null == param._transfer_date)) {
            stb.append("         '" + StringUtils.remove(param._transfer_date, "/").substring(4) + "' as LIMIT_DATE, ");
        } else {
            stb.append("         case ");
            stb.append("             when substr(cast(LMIT.PAID_LIMIT_DATE as varchar(10)), 6, 2) = '" + param._month + "' ");
            stb.append("             then substr(cast(LMIT.PAID_LIMIT_DATE as varchar(10)), 6, 5) ");
            stb.append("             else '" + param._setLimitDay + "' ");
            stb.append("         end as LIMIT_DATE, ");
        }
        stb.append("         SLIP.PAY_DIV, ");
        stb.append("         PLAN.PLAN_YEAR, ");
        stb.append("         PLAN.PLAN_MONTH, ");
        stb.append("         sum(PLAN.PLAN_MONEY) as PLAN_MONEY, ");
        stb.append("         max(VALUE(CASE WHEN REDUC_C.OFFSET_FLG = '1' THEN REDUC_C.DECISION_MONEY ELSE 0 END, 0)) AS COUNTRY_MONEY, ");
        stb.append("         max(VALUE(CASE WHEN REDUC_C.ADD_OFFSET_FLG = '1' THEN REDUC_C.ADD_DECISION_MONEY ELSE 0 END, 0)) AS COUNTRY_ADDMONEY, ");
        stb.append("         max(VALUE(CASE WHEN REDUC_D1.OFFSET_FLG = '1' THEN REDUC_D1.DECISION_MONEY ELSE 0 END, 0)) AS PREF_MONEY1, ");
        stb.append("         max(VALUE(CASE WHEN REDUC_D2.OFFSET_FLG = '1' THEN REDUC_D2.DECISION_MONEY ELSE 0 END, 0)) AS PREF_MONEY2, ");
        stb.append("         max(VALUE(BURDEN_1.BURDEN_CHARGE, 0)) AS BURDEN_CHARGE1, ");
        stb.append("         max(VALUE(BURDEN_2.BURDEN_CHARGE, 0)) AS BURDEN_CHARGE2, ");
        stb.append("         max(VALUE(SCHOOL_1.PLAN_MONEY, 0)) AS SCHOOL_1, ");
        stb.append("         max(VALUE(SCHOOL_2.PLAN_MONEY, 0)) AS SCHOOL_2, ");
        stb.append("         BANK.BRANCHCD, ");
        stb.append("         BANK.ACCOUNTNO, ");
        stb.append("         BANK.ACCOUNTNAME ");
        stb.append("     FROM ");
        stb.append("         COLLECT_SLIP_PLAN_M_DAT PLAN ");
        stb.append("         LEFT JOIN COLLECT_SLIP_DAT SLIP ON PLAN.SCHOOLCD    = SLIP.SCHOOLCD ");
        stb.append("                                        AND PLAN.SCHOOL_KIND = SLIP.SCHOOL_KIND ");
        stb.append("                                        AND PLAN.YEAR        = SLIP.YEAR ");
        stb.append("                                        AND PLAN.SLIP_NO     = SLIP.SLIP_NO ");
        stb.append("         LEFT JOIN COLLECT_SLIP_PLAN_LIMITDATE_DAT LMIT ON PLAN.SCHOOLCD    = LMIT.SCHOOLCD ");
        stb.append("                                                       AND PLAN.SCHOOL_KIND = LMIT.SCHOOL_KIND ");
        stb.append("                                                       AND PLAN.YEAR        = LMIT.YEAR ");
        stb.append("                                                       AND PLAN.SCHREGNO    = LMIT.SCHREGNO ");
        stb.append("                                                       AND PLAN.SLIP_NO     = LMIT.SLIP_NO ");
        stb.append("                                                       AND PLAN.PLAN_YEAR   = LMIT.PLAN_YEAR ");
        stb.append("                                                       AND PLAN.PLAN_MONTH  = LMIT.PLAN_MONTH ");
        stb.append("         LEFT JOIN REGISTBANK_DAT BANK ON PLAN.SCHOOLCD    = BANK.SCHOOLCD ");
        stb.append("                                      AND PLAN.SCHREGNO    = BANK.SCHREGNO ");
        stb.append("                                      AND SLIP.PAY_DIV     = BANK.SEQ ");
        stb.append("         LEFT JOIN SCHREG_REGD_DAT REGD ON REGD.SCHREGNO = PLAN.SCHREGNO ");
        stb.append("                                       AND REGD.YEAR     = PLAN.YEAR ");
        stb.append("                                       AND REGD.SEMESTER = '" + param._ctrl_semester + "' ");
        stb.append("     LEFT JOIN REDUCTION_COUNTRY_PLAN_DAT REDUC_C ON LMIT.SCHOOLCD         = REDUC_C.SCHOOLCD ");
        stb.append("                                                 AND LMIT.SCHOOL_KIND      = REDUC_C.SCHOOL_KIND ");
        stb.append("                                                 AND LMIT.YEAR             = REDUC_C.YEAR ");
        stb.append("                                                 AND LMIT.SLIP_NO          = REDUC_C.SLIP_NO ");
        stb.append("                                                 AND PLAN.PLAN_MONTH = REDUC_C.PLAN_MONTH ");
        stb.append("     LEFT JOIN REDUCTION_PLAN_DAT REDUC_D1 ON LMIT.SCHOOLCD         = REDUC_D1.SCHOOLCD ");
        stb.append("                                         AND LMIT.SCHOOL_KIND       = REDUC_D1.SCHOOL_KIND ");
        stb.append("                                         AND LMIT.YEAR              = REDUC_D1.YEAR ");
        stb.append("                                         AND REDUC_D1.REDUCTION_TARGET = '1' ");
        stb.append("                                         AND LMIT.SLIP_NO           = REDUC_D1.SLIP_NO ");
        stb.append("                                         AND PLAN.PLAN_MONTH  = REDUC_D1.PLAN_MONTH ");
        stb.append("     LEFT JOIN REDUCTION_PLAN_DAT REDUC_D2 ON LMIT.SCHOOLCD         = REDUC_D2.SCHOOLCD ");
        stb.append("                                         AND LMIT.SCHOOL_KIND       = REDUC_D2.SCHOOL_KIND ");
        stb.append("                                         AND LMIT.YEAR              = REDUC_D2.YEAR ");
        stb.append("                                         AND REDUC_D2.REDUCTION_TARGET = '2' ");
        stb.append("                                         AND LMIT.SLIP_NO           = REDUC_D2.SLIP_NO ");
        stb.append("                                         AND PLAN.PLAN_MONTH  = REDUC_D2.PLAN_MONTH ");
        stb.append("     LEFT JOIN REDUCTION_BURDEN_CHARGE_PLAN_DAT BURDEN_1 ON LMIT.SCHOOLCD        = BURDEN_1.SCHOOLCD ");
        stb.append("                                                      AND LMIT.SCHOOL_KIND       = BURDEN_1.SCHOOL_KIND ");
        stb.append("                                                      AND LMIT.YEAR              = BURDEN_1.YEAR ");
        stb.append("                                                      AND BURDEN_1.REDUCTION_TARGET = '1' ");
        stb.append("                                                      AND LMIT.SLIP_NO           = BURDEN_1.SLIP_NO ");
        stb.append("                                                      AND PLAN.PLAN_MONTH  = BURDEN_1.PLAN_MONTH ");
        stb.append("     LEFT JOIN REDUCTION_BURDEN_CHARGE_PLAN_DAT BURDEN_2 ON LMIT.SCHOOLCD        = BURDEN_2.SCHOOLCD ");
        stb.append("                                                      AND LMIT.SCHOOL_KIND       = BURDEN_2.SCHOOL_KIND ");
        stb.append("                                                      AND LMIT.YEAR              = BURDEN_2.YEAR ");
        stb.append("                                                      AND BURDEN_2.REDUCTION_TARGET = '2' ");
        stb.append("                                                      AND LMIT.SLIP_NO           = BURDEN_2.SLIP_NO ");
        stb.append("                                                      AND PLAN.PLAN_MONTH  = BURDEN_2.PLAN_MONTH ");
        stb.append("     LEFT JOIN REDUCTION_SCHOOL_PLAN_DAT SCHOOL_1 ON LMIT.SCHOOLCD          = SCHOOL_1.SCHOOLCD ");
        stb.append("                                                 AND LMIT.SCHOOL_KIND       = SCHOOL_1.SCHOOL_KIND ");
        stb.append("                                                 AND LMIT.YEAR              = SCHOOL_1.YEAR ");
        stb.append("                                                 AND SCHOOL_1.REDUCTION_TARGET = '1' ");
        stb.append("                                                 AND LMIT.SLIP_NO           = SCHOOL_1.SLIP_NO ");
        stb.append("                                                 AND PLAN.PLAN_MONTH        = SCHOOL_1.PLAN_MONTH ");
        stb.append("     LEFT JOIN REDUCTION_SCHOOL_PLAN_DAT SCHOOL_2 ON LMIT.SCHOOLCD          = SCHOOL_2.SCHOOLCD ");
        stb.append("                                                 AND LMIT.SCHOOL_KIND       = SCHOOL_2.SCHOOL_KIND ");
        stb.append("                                                 AND LMIT.YEAR              = SCHOOL_2.YEAR ");
        stb.append("                                                 AND SCHOOL_2.REDUCTION_TARGET = '2' ");
        stb.append("                                                 AND LMIT.SLIP_NO           = SCHOOL_2.SLIP_NO ");
        stb.append("                                                 AND PLAN.PLAN_MONTH        = SCHOOL_2.PLAN_MONTH ");
        stb.append("     WHERE ");
        stb.append("             PLAN.SCHOOLCD    = '" + param._schoolCd + "' ");
        if (!"99".equals(param._school_kind)) {
            stb.append("         AND PLAN.SCHOOL_KIND = '" + param._school_kind + "' ");
        }
        stb.append("         AND PLAN.YEAR        = '" + param._ctrl_year + "' ");
        stb.append("         AND PLAN.PLAN_MONTH  IN " + setMonthIn + " ");
        stb.append("         AND PLAN.PLAN_MONEY  <> 0 ");
        stb.append("         AND PLAN.PAID_MONEY  IS NULL ");
        stb.append("         AND SLIP.CANCEL_DATE IS NULL ");
        stb.append("         AND SLIP.PAY_DIV     IN ('1', '2') ");
        stb.append("         AND (LMIT.PAID_LIMIT_DATE IS NULL OR LMIT.PAID_LIMIT_MONTH IN " + setMonthIn + ") ");
        stb.append("     GROUP BY ");
        stb.append("         PLAN.SCHREGNO, ");
        stb.append("         REGD.GRADE, ");
        stb.append("         REGD.HR_CLASS, "); 
        stb.append("         REGD.ATTENDNO, ");
        stb.append("         SLIP.PAY_DIV, ");
        stb.append("         PLAN.PLAN_YEAR, ");
        stb.append("         PLAN.PLAN_MONTH, ");
        stb.append("         LMIT.PAID_LIMIT_DATE, ");
        stb.append("         BANK.BRANCHCD, ");
        stb.append("         BANK.ACCOUNTNO, ");
        stb.append("         BANK.ACCOUNTNAME ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     value(SCHREGNO, '') as SCHREGNO, ");
        stb.append("     GRADE, ");
        stb.append("     HR_CLASS, ");
        stb.append("     ATTENDNO, ");
        stb.append("     value(LIMIT_DATE, '') as LIMIT_DATE, ");
        stb.append("     PAY_DIV, ");
        stb.append("     substr(PLAN_YEAR, 3, 2) as PLAN_YEAR, ");
        stb.append("     PLAN_MONTH, ");
        stb.append("     sum(PLAN_MONEY) as PLAN_MONEY, ");
        stb.append("     sum(COUNTRY_MONEY) as COUNTRY_MONEY, ");
        stb.append("     sum(COUNTRY_ADDMONEY) as COUNTRY_ADDMONEY, ");
        stb.append("     sum(PREF_MONEY1) as PREF_MONEY1, ");
        stb.append("     sum(PREF_MONEY2) as PREF_MONEY2, ");
        stb.append("     sum(BURDEN_CHARGE1) as BURDEN_CHARGE1, ");
        stb.append("     sum(BURDEN_CHARGE2) as BURDEN_CHARGE2, ");
        stb.append("     sum(SCHOOL_1) as SCHOOL_1, ");
        stb.append("     sum(SCHOOL_2) as SCHOOL_2, ");
        stb.append("     value(BRANCHCD, '') as BRANCHCD, ");
        stb.append("     value(ACCOUNTNO, '') as ACCOUNTNO, ");
        stb.append("     value(ACCOUNTNAME, '') as ACCOUNTNAME ");
        stb.append(" FROM ");
        stb.append("     MAIN_DATA ");
        if (!"1".equals(_param._useOutputOverMonth)) {
	        stb.append(" WHERE ");
	        stb.append("     LIMIT_DATE = '" + _param._monthDate + "'  ");
        } else {
	        stb.append(" WHERE ");
	        stb.append("     LIMIT_DATE <= '" + _param._monthDate + "'  ");
        }
        stb.append(" GROUP BY ");
        stb.append("     SCHREGNO, ");
        stb.append("     GRADE, ");
        stb.append("     HR_CLASS, ");
        stb.append("     ATTENDNO, ");
        stb.append("     LIMIT_DATE, ");
        stb.append("     PAY_DIV, ");
        stb.append("     PLAN_YEAR, ");
        stb.append("     PLAN_MONTH, ");
        stb.append("     BRANCHCD, ");
        stb.append("     ACCOUNTNO, ");
        stb.append("     ACCOUNTNAME ");
        stb.append(" UNION ");
        stb.append(" SELECT ");
        stb.append("     STAFFCD as SCHREGNO, ");
        stb.append("     '99' as GRADE, ");
        stb.append("     '999' as HR_CLASS, ");
        stb.append("     STAFFCD as ATTENDNO, ");
        stb.append("     '99-99' as LIMIT_DATE, ");
        stb.append("     '' as PAY_DIV, ");
        stb.append("     substr(YEAR_MONTH, 1, 2) as PLAN_YEAR, ");
        stb.append("     substr(YEAR_MONTH, 3, 2) as PLAN_MONTH, ");
        stb.append("     int(PLAN_MONEY) as PLAN_MONEY, ");
        stb.append("     0 as COUNTRY_MONEY, ");
        stb.append("     0 as COUNTRY_ADDMONEY, ");
        stb.append("     0 as PREF_MONEY1, ");
        stb.append("     0 as PREF_MONEY2, ");
        stb.append("     0 as BURDEN_CHARGE1, ");
        stb.append("     0 as BURDEN_CHARGE2, ");
        stb.append("     0 as SCHOOL_1, ");
        stb.append("     0 as SCHOOL_2, ");
        stb.append("     BRANCHCD, ");
        stb.append("     ACCOUNTNO, ");
        stb.append("     ACCOUNTNAME ");
        stb.append(" FROM ");
        stb.append("     COLLECT_P740_STAFF_WORK_DAT ");
        stb.append(" ORDER BY ");
        stb.append("     LIMIT_DATE, ");
        stb.append("     PLAN_YEAR, ");
        stb.append("     PLAN_MONTH, ");
        stb.append("     GRADE, ");
        stb.append("     HR_CLASS, ");
        stb.append("     ATTENDNO ");

        return stb.toString();
    }

    private class PrintData {
        final String _schregNo;
        final String _accountName;
        final String _branchCd;
        final String _accountNo;
        final String _planMoney;
        final String _yearMonth;
        public PrintData(
                final String schregNo,
                final String accountName,
                final String branchCd,
                final String accountNo,
                final String planMoney,
                final String yearMonth
        ) {
            _schregNo    = schregNo;
            _accountName = accountName;
            _branchCd    = branchCd;
            _accountNo   = accountNo;
            _planMoney   = planMoney;
            _yearMonth   = yearMonth;
        }
    }

    private List getListHenkin(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getSqlHenkin(_param);
            log.info(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String gradeHrClass   = rs.getString("GRADE") + '-' + rs.getString("HR_CLASS");
                final String sex            = rs.getString("SEX");
                final String schregNo       = rs.getString("SCHREGNO");
                final String schName        = rs.getString("NAME");
                final String gName          = rs.getString("GNAME");
                final String accountName    = rs.getString("ACCOUNTNAME");
                final String branchCd       = rs.getString("BRANCHCD");
                final String accountNo      = rs.getString("ACCOUNTNO");
                final String transferFee    = rs.getString("BANK_TRANSFER_FEE");
                final String planMoney      = rs.getString("REQUEST_GK");
                final String kyufuMoney     = rs.getString("KYUFU_MONEY");

                final PrintDataHenkin printData = new PrintDataHenkin(gradeHrClass, sex, schregNo, schName, gName, accountName, branchCd, accountNo, transferFee, planMoney, kyufuMoney);
                retList.add(printData);
                _param._dataCnt++;
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    /** 請求データ取得SQL **/
    private String getSqlHenkin(final Param param) {

        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH MAX_SEM AS ( ");
        stb.append("     SELECT ");
        stb.append("         max(REGD.SEMESTER) as SEMESTER, ");
        stb.append("         REGD.SCHREGNO ");
        stb.append("     FROM ");
        stb.append("         SCHREG_REGD_DAT REGD ");
        stb.append("     WHERE ");
        stb.append("         REGD.YEAR     = '"+ param._ctrl_year +"' ");
        stb.append("     GROUP BY ");
        stb.append("         REGD.SCHREGNO ");
        stb.append(" ), REGD_INFO AS ( ");
        stb.append("     SELECT ");
        stb.append("         REGD.GRADE, ");
        stb.append("         REGD.HR_CLASS, ");
        stb.append("         REGD.ATTENDNO, ");
        stb.append("         REGD.SCHREGNO ");
        stb.append("     FROM ");
        stb.append("         SCHREG_REGD_DAT REGD, ");
        stb.append("         MAX_SEM ");
        stb.append("     WHERE ");
        stb.append("             REGD.YEAR     = '"+ param._ctrl_year +"' ");
        stb.append("         AND REGD.SEMESTER = MAX_SEM.SEMESTER ");
        stb.append("         AND REGD.SCHREGNO = MAX_SEM.SCHREGNO ");
        stb.append(" ), KYUHU_DATA AS (  ");
        stb.append("      SELECT ");
        stb.append("        OUTGO_SCH.SCHREGNO, ");
        stb.append("        SUM(VALUE (T1.REQUEST_GK, 0)) AS KYUFU_MONEY ");
        stb.append("      FROM ");
        stb.append("        LEVY_REQUEST_OUTGO_SCHREG_DAT OUTGO_SCH ");
        stb.append("        INNER JOIN LEVY_REQUEST_OUTGO_DAT T1 ");
        stb.append("          ON T1.YEAR = OUTGO_SCH.YEAR ");
        stb.append("          AND T1.SCHOOLCD = OUTGO_SCH.SCHOOLCD ");
        stb.append("          AND T1.SCHOOL_KIND = OUTGO_SCH.SCHOOL_KIND ");
        stb.append("          AND T1.OUTGO_L_CD = OUTGO_SCH.OUTGO_L_CD ");
        stb.append("          AND T1.OUTGO_M_CD = OUTGO_SCH.OUTGO_M_CD ");
        stb.append("          AND T1.REQUEST_NO = OUTGO_SCH.REQUEST_NO ");
        stb.append("      WHERE ");
        stb.append("        T1.YEAR = '"+ param._ctrl_year +"' ");
        stb.append("        AND T1.SCHOOLCD  = '"+ param._schoolCd + "' ");
        stb.append("        AND VALUE (T1.OUTGO_CANCEL, '0') = '0' ");
        stb.append("        AND T1.OUTGO_L_CD <> '99' ");
        stb.append("        AND T1.INCOME_L_CD = '98' ");
        stb.append("        AND T1.HENKIN_FLG = '1' ");
        stb.append("        AND VALUE (T1.HENKIN_APPROVAL, '0') = '0' ");
        stb.append("      GROUP BY ");
        stb.append("        OUTGO_SCH.SCHREGNO ");
        stb.append(" ) ");

        stb.append(" SELECT ");
        stb.append("     REGD.GRADE, ");
        stb.append("     REGD.HR_CLASS, ");
        stb.append("     REGD.ATTENDNO, ");
        stb.append("     Z002.NAME1 as SEX, ");
        stb.append("     SCHO.SCHREGNO, ");
        stb.append("     BASE.NAME, ");
        stb.append("     GARD.GUARD_NAME as GNAME, ");
        stb.append("     BANK.ACCOUNTNAME, ");
        stb.append("     value(BANK.BRANCHCD, '') as BRANCHCD, ");
        stb.append("     value(BANK.ACCOUNTNO, '') as ACCOUNTNO, ");
        stb.append("     SCHOOL_BANK.BANK_TRANSFER_FEE, ");
        stb.append("     sum(OUTG.REQUEST_GK) AS REQUEST_GK, ");
        stb.append("     value(KYUHU.KYUFU_MONEY, 0) AS KYUFU_MONEY");
        stb.append(" FROM ");
        stb.append("     LEVY_REQUEST_OUTGO_SCHREG_DAT SCHO ");
        stb.append("     INNER JOIN LEVY_REQUEST_OUTGO_DAT OUTG ON SCHO.SCHOOLCD    = OUTG.SCHOOLCD ");
        stb.append("                                           AND SCHO.SCHOOL_KIND = OUTG.SCHOOL_KIND ");
        stb.append("                                           AND SCHO.YEAR        = OUTG.YEAR ");
        stb.append("                                           AND SCHO.REQUEST_NO  = OUTG.REQUEST_NO ");
        stb.append("                                           AND SCHO.OUTGO_L_CD  = OUTG.OUTGO_L_CD ");
        stb.append("                                           AND SCHO.OUTGO_M_CD  = OUTG.OUTGO_M_CD ");
        stb.append("     LEFT JOIN REGISTBANK_DAT BANK ON BANK.SCHOOLCD    = SCHO.SCHOOLCD ");
        stb.append("                                  AND BANK.SCHREGNO    = SCHO.SCHREGNO ");
        stb.append("                                  AND BANK.SEQ         = OUTG.PAY_DIV ");
        stb.append("     LEFT JOIN REGD_INFO REGD ON SCHO.SCHREGNO = REGD.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_BASE_MST BASE ON SCHO.SCHREGNO = BASE.SCHREGNO ");
        stb.append("     LEFT JOIN GUARDIAN_DAT GARD ON SCHO.SCHREGNO = GARD.SCHREGNO ");
        stb.append("     LEFT JOIN NAME_MST Z002 ON Z002.NAMECD1 = 'Z002' ");
        stb.append("                            AND Z002.NAMECD2 = BASE.SEX ");
        stb.append("     LEFT JOIN COLLECT_SCHOOL_BANK_MST SCHOOL_BANK ON SCHO.SCHOOLCD          = SCHOOL_BANK.SCHOOLCD ");
        stb.append("                                                  AND SCHO.SCHOOL_KIND       = SCHOOL_BANK.SCHOOL_KIND ");
        stb.append("                                                  AND SCHO.YEAR              = SCHOOL_BANK.YEAR ");
        stb.append("                                                  AND SCHOOL_BANK.FORMAT_DIV = '2' ");
        stb.append("                                                  AND SCHOOL_BANK.SEQ        = '001' ");
        stb.append("     LEFT JOIN KYUHU_DATA KYUHU ON KYUHU.SCHREGNO = SCHO.SCHREGNO ");
        stb.append(" WHERE ");
        stb.append("         SCHO.SCHOOLCD        = '"+ param._schoolCd + "' ");
        if (!"99".equals(param._school_kind)) {
            stb.append("     AND SCHO.SCHOOL_KIND     = '"+ param._school_kind +"' ");
        }
        stb.append("     AND SCHO.YEAR            = '"+ param._ctrl_year +"' ");
        stb.append("     AND SCHO.LINE_NO         = 1 ");
        stb.append("     AND SCHO.OUTGO_L_CD      = '99' ");
        stb.append("     AND SCHO.OUTGO_M_CD      = '99' ");
        stb.append("     AND SCHO.OUTGO_S_CD      = '999' ");
        stb.append("     AND OUTG.HENKIN_FLG      = '1' ");
        stb.append("     AND value(OUTG.HENKIN_APPROVAL, '0') = '0' ");
        stb.append("     AND OUTG.OUTGO_CANCEL    IS NULL ");
        stb.append("     AND OUTG.HENKIN_DATE     IS NULL ");
        stb.append(" GROUP BY ");
        stb.append("     REGD.GRADE, ");
        stb.append("     REGD.HR_CLASS, ");
        stb.append("     REGD.ATTENDNO, ");
        stb.append("     Z002.NAME1, ");
        stb.append("     SCHO.SCHREGNO, ");
        stb.append("     BASE.NAME, ");
        stb.append("     GARD.GUARD_NAME, ");
        stb.append("     BANK.ACCOUNTNAME, ");
        stb.append("     BANK.BRANCHCD, ");
        stb.append("     BANK.ACCOUNTNO, ");
        stb.append("     SCHOOL_BANK.BANK_TRANSFER_FEE, ");
        stb.append("     KYUHU.KYUFU_MONEY  ");
        stb.append(" UNION ");
        stb.append(" SELECT ");
        stb.append("     '00' as GRADE, ");
        stb.append("     '000' as HR_CLASS, ");
        stb.append("     P740.STAFFCD as ATTENDNO, ");
        stb.append("     '教職員' as SEX, ");
        stb.append("     P740.STAFFCD as SCHREGNO, ");
        stb.append("     STAF.STAFFNAME as NAME, ");
        stb.append("     '' as GNAME, ");
        stb.append("     P740.ACCOUNTNAME, ");
        stb.append("     P740.BRANCHCD, ");
        stb.append("     P740.ACCOUNTNO, ");
        stb.append("     CAST(null AS smallint) AS BANK_TRANSFER_FEE, ");
        stb.append("     int(P740.PLAN_MONEY) as REQUEST_GK, ");
        stb.append("     0 as KYUFU_MONEY ");
        stb.append(" FROM ");
        stb.append("     COLLECT_P740_STAFF_WORK_DAT P740 ");
        stb.append("     LEFT JOIN STAFF_MST STAF ON P740.STAFFCD = STAF.STAFFCD ");
        stb.append(" ORDER BY ");
        stb.append("     GRADE, ");
        stb.append("     HR_CLASS, ");
        stb.append("     ATTENDNO ");

        return stb.toString();
    }

    private class PrintDataHenkin {
        final String _gradeHrClass;
        final String _sex;
        final String _schregNo;
        final String _schName;
        final String _gName;
        final String _accountName;
        final String _branchCd;
        final String _accountNo;
        final String _transferFee;
        final String _planMoney;
        final String _kyufuMoney;
        public PrintDataHenkin(
                final String gradeHrClass,
                final String sex,
                final String schregNo,
                final String schName,
                final String gName,
                final String accountName,
                final String branchCd,
                final String accountNo,
                final String transferFee,
                final String planMoney,
                final String kyufuMoney
        ) {
            _gradeHrClass   = gradeHrClass;
            _sex            = sex;
            _schregNo       = schregNo;
            _schName        = schName;
            _gName          = gName;
            _accountName    = accountName;
            _branchCd       = branchCd;
            _accountNo      = accountNo;
            _transferFee    = null != transferFee && !"".equals(transferFee) ? transferFee : _param._tesuRyo;
            _planMoney      = planMoney;
            _kyufuMoney		= kyufuMoney;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 75163 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _printType;
        private final String _printDate;
        private final String _school_kind;
        private final String _schoolCd;
        private final String _output;
        private final String _staff_lunch;
        private final String _max_file_size;
        private final String _month;
        private final String _transfer_date;
        private final String _retransfer_date;
        private final String _header;
        private final String _prgid;
        private final String _ctrl_year;
        private final String _ctrl_semester;
        private final String _ctrl_date;
        private final String _printkouzakigou;
        private final String _printkyokuban;
        private final String _printlimitdate;
        private final String _printlimitdate2;
        private final String _printsaihuri;
        private final String _printtotalcnt;
        private final String _printtotalmoney;
        private final String _printsyubetsu;
        private final String _printjigyounushi;
        private final String _printUkeireJcCd;
        private final String _setLimitDay;
        private final String _toriKyouName;
        private final String _tesuRyo;
        private final String _useBenefit;
        private final String _monthDate;
        private final String _useOutputOverMonth;
        private final Map _schoolMap;
        
        int _dataCnt = 0;

        private final String[] _monthArray = {"04", "05", "06", "07", "08", "09", "10", "11", "12", "01", "02", "03"};

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _printType           = request.getParameter("PRINT_TYPE"); // 1:自動払込書　2:請求データ明細リスト
            _printDate           = request.getParameter("PRINT_DATE");
            _school_kind         = request.getParameter("SCHOOL_KIND");
            _schoolCd            = request.getParameter("SCHOOLCD");
            _output              = request.getParameter("OUTPUT");
            _staff_lunch         = request.getParameter("STAFF_LUNCH");
            _max_file_size       = request.getParameter("MAX_FILE_SIZE");
            _month               = request.getParameter("MONTH");
            _monthDate           = request.getParameter("MONTH_DATE");
            _transfer_date       = request.getParameter("TRANSFER_DATE");
            _retransfer_date     = request.getParameter("RETRANSFER_DATE");
            _header              = request.getParameter("HEADER");
            _prgid               = request.getParameter("PRGID");
            _ctrl_year           = request.getParameter("CTRL_YEAR");
            _ctrl_semester       = request.getParameter("CTRL_SEMESTER");
            _ctrl_date           = request.getParameter("CTRL_DATE");
            _printkouzakigou     = request.getParameter("printKouzaKigou");
            _printkyokuban       = request.getParameter("printKyokuBan");
            _printlimitdate      = request.getParameter("printLimitDate");
            _printlimitdate2     = request.getParameter("printLimitDate2"); //引き落とし用日付
            _printsaihuri        = request.getParameter("printSaiHuri");
            _printtotalcnt       = request.getParameter("printTotalCnt");
            _printtotalmoney     = request.getParameter("printTotalMoney");
            _printsyubetsu       = request.getParameter("printSyubetsu");
            _printjigyounushi    = request.getParameter("printJigyounushi");
            _printUkeireJcCd     = request.getParameter("printUkeireJcCd");
            _setLimitDay         = request.getParameter("setLimitDay");
            _toriKyouName        = request.getParameter("toriKyouName");
            _tesuRyo             = "".equals(request.getParameter("tesuRyo")) ? "0" : request.getParameter("tesuRyo");
            _useBenefit          = request.getParameter("useBenefit");
            _useOutputOverMonth  = request.getParameter("useOutputOverMonth");

            _schoolMap = getSchoolInfo(db2, _ctrl_year);
        }

        private Map getSchoolInfo(final DB2UDB db2, final String year) {
            final Map retSchoolMap = new TreeMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {

                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     SCHOOLZIPCD, ");
                stb.append("     value(SCHOOLADDR1, '') || value(SCHOOLADDR2, '') as ADDR, ");
                stb.append("     SCHOOLTELNO ");
                stb.append(" FROM ");
                stb.append("     SCHOOL_BANK_MST ");
                stb.append(" WHERE ");
                stb.append("     BANKTRANSFERCD = (SELECT max(BANKTRANSFERCD) FROM SCHOOL_BANK_MST) ");
                stb.append(" FETCH FIRST ROW ONLY ");

                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    retSchoolMap.put("SCHOOLZIPCD", rs.getString("SCHOOLZIPCD"));
                    retSchoolMap.put("SCHOOLTELNO", rs.getString("SCHOOLTELNO"));
                    retSchoolMap.put("SCHOOLADDR",  rs.getString("ADDR"));
                }
            } catch (SQLException ex) {
                log.debug("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retSchoolMap;
        }

    }
}

// eof
