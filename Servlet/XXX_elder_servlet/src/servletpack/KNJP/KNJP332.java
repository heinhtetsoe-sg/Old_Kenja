// kanji=漢字
/*
 * $Id: 9a67c886a77afd9c2dfab2bde4e244468a0bd5c9 $
 *
 * 作成日: 2010/10/04
 * 作成者: m-yama
 *
 * Copyright(C) 2005-2008 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJP;

import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
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

/**
 *
 *  学校教育システム 賢者 [入試管理]
 *
 *                  ＜ＫＮＪＰ３３０＞  授業料軽減決定通知書
 *
 *  2005/11/10 m-yama 作成
 *  2005/11/24 m-yama NO001 抽出条件に軽減額ありのみを追加
 *  2005/11/24 m-yama NO002 フォーム変更に伴う修正 項目追加
 *  2005/11/25 m-yama NO003 フォーム変更に伴う修正 フォームを共通化
 *  2005/11/25 m-yama NO004 差引額０の場合の出力変更
 */

public class KNJP332 {


    private static final Log log = LogFactory.getLog(KNJP332.class);

    Param _param;
    private boolean _hasData;
    private String FORM_TYPE9 = "1";

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws Exception
    {

        Vrw32alp svf = new Vrw32alp();          //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                      //Databaseクラスを継承したクラス

    //  print設定
        PrintWriter outstrm = new PrintWriter (response.getOutputStream());
        response.setContentType("application/pdf");

    //  svf設定
        svf.VrInit();                         //クラスの初期化
        svf.VrSetSpoolFileStream(response.getOutputStream());         //PDFファイル名の設定

    //  ＤＢ接続
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
        } catch( Exception ex ) {
            log.error("DB2 open error!",ex);
            return;
        }

        _param = createParam(db2, request);

        _hasData = false;

        //SVF出力
        printMain(db2, svf);

    //  該当データ無し
        if (!_hasData) {
            svf.VrSetForm("MES001.frm", 0);
            svf.VrsOut("note" , "note");
            svf.VrEndPage();
        }

    //  終了処理
        svf.VrQuit();
        db2.commit();
        db2.close();                //DBを閉じる
        outstrm.close();            //ストリームを閉じる

    }//doGetの括り

    /**印刷処理メイン*/
    private void printMain(final DB2UDB db2, final Vrw32alp svf)
    {
        try {
log.debug("printMain start!");
            if (_param._output.equals("1")){
                db2.query(todoufukenMeisai());
            }else if(_param._output.equals("2")){
                db2.query(classMeisai());
            }else {
                db2.query(kojinMeisai());
            }
            ResultSet rs = db2.getResultSet();
log.debug("printMain end!");

            while( rs.next() ){

                //明細データをセット
                printMeisai(svf, rs);
                svf.VrEndPage();

                _hasData = true;
            }

            rs.close();
            db2.commit();
        } catch( Exception ex ) {
            log.warn("printMain read error!",ex);
        }

    }//printMain()の括り

    /**明細データをセット*/
    private void printMeisai(final Vrw32alp svf, final ResultSet rs)
    {
        try {
            final int keigenMoney = rs.getInt("REDUCTIONMONEY") + rs.getInt("B_PAID_MONEY") + rs.getInt("A_PAID_MONEY");
            int grantTotal = 0;
            final Map grantMap = new TreeMap();
            int grantCnt2 = 0;
            for (Iterator iterator = _param._G212Map.keySet().iterator(); iterator.hasNext();) {
                final String grantCd = (String) iterator.next();
                if (null == rs.getString("GRANTNAME" + grantCd) || "".equals(rs.getString("GRANTNAME" + grantCd))) {
                    continue;
                }
                grantTotal += rs.getInt("GRANT_MONEY" + grantCd);
                final Map grantVal = new HashMap();
                grantVal.put("GRANT_NAME", rs.getString("GRANTNAME" + grantCd));
                grantVal.put("GRANT_MONEY", rs.getString("GRANT_MONEY" + grantCd));
                grantMap.put(grantCd, grantVal);
                grantCnt2++;
            }
            grantCnt2 = grantCnt2 > 2 ? 2 : grantCnt2;
            String paternCd = "";
            String frmName = "";
            if (keigenMoney > 0) {
                if (grantTotal > 0) {
                    frmName = "KNJP332_B.frm";
                    paternCd = "B";
                } else if (rs.getInt("MONEY_DUE") < 600000) {
                    frmName = "KNJP332_F.frm";
                    paternCd = "F";
                } else {
                    frmName = "KNJP332_A.frm";
                    paternCd = "A";
                }
            } else {
                if (grantTotal > 0) {
                    if (rs.getInt("MONEY_DUE") > grantTotal) {
                        if (grantCnt2 == 1) {
                            frmName = "KNJP332_D.frm";
                            paternCd = "D";
                        } else {
                            frmName = "KNJP332_D_2.frm";
                            paternCd = "D";
                        }
                    } else {
                        if (grantCnt2 == 1) {
                            frmName = "KNJP332_E.frm";
                            paternCd = "E";
                        } else {
                            frmName = "KNJP332_E_2.frm";
                            paternCd = "E";
                        }
                    }
                } else {
                    frmName = "KNJP332_C.frm";
                    paternCd = "C";
                }
            }
            log.fatal(paternCd);

            final int totalReducMoney = rs.getInt("REDUCTIONMONEY") + rs.getInt("B_PAID_MONEY") + rs.getInt("A_PAID_MONEY") + rs.getInt("ADJUSTMENT_MONEY");
            final int totalReducCountryMoney = rs.getInt("B_PAID_MONEY") + rs.getInt("A_PAID_MONEY");
            if ("A".equals(paternCd)) {
                int totalMoney = rs.getInt("MONEY_DUE") - (totalReducCountryMoney + rs.getInt("REDUCTIONMONEY") + rs.getInt("ADJUSTMENT_MONEY"));
                if (totalMoney > 0) {
                    if (rs.getInt("ADJUSTMENT_MONEY") > 0) {
                        frmName = "KNJP332_A_2.frm";
                    } else {
                        frmName = "KNJP332_A_3.frm";
                    }
                    totalMoney = totalMoney + rs.getInt("FEE");
                } else {
                    if (rs.getInt("ADJUSTMENT_MONEY") <= 0) {
                        frmName = "KNJP332_F.frm";
                        paternCd = "F";
                    }
                }
                if ("A".equals(paternCd)) {
                    svf.VrSetForm(frmName, 1);
                    svf.VrsOut("MONEY1"            , String.valueOf(totalReducMoney));
                    svf.VrsOut("MONEY2"            , String.valueOf(totalMoney));
                    svf.VrsOut("MONEY3_1"            , rs.getString("MONEY_DUE"));
                    svf.VrsOut("MONEY3_2"            , String.valueOf(totalReducCountryMoney));
                    svf.VrsOut("MONEY3_3"            , rs.getString("REDUCTIONMONEY"));
                    if ("KNJP332_A_3.frm".equals(frmName)) {
                        svf.VrsOut("MONEY3_4"            , rs.getString("FEE"));
                    } else {
                        svf.VrsOut("MONEY3_4"            , rs.getString("ADJUSTMENT_MONEY"));
                        svf.VrsOut("MONEY3_5"            , rs.getString("FEE"));
                    }
                    svf.VrsOut("TOTAL"            , String.valueOf(totalMoney));
                }
            }
            int moneyLine = 1;
            String gnameLine = "";
            if ("B".equals(paternCd)) {
                int totalMoney = rs.getInt("MONEY_DUE") - (grantTotal + totalReducCountryMoney + rs.getInt("REDUCTIONMONEY") + rs.getInt("ADJUSTMENT_MONEY"));
                if (totalMoney > 0) {
                    if (rs.getInt("ADJUSTMENT_MONEY") > 0) {
                        frmName = "KNJP332_B_3.frm";
                    } else {
                        frmName = "KNJP332_B_2.frm";
                    }
                    totalMoney = totalMoney + rs.getInt("FEE");
                } else {
                    if (rs.getInt("ADJUSTMENT_MONEY") > 0) {
                        frmName = "KNJP332_B_4.frm";
                    } else {
                        frmName = "KNJP332_B.frm";
                    }
                }
                log.fatal(paternCd);
                svf.VrSetForm(frmName, 1);
                svf.VrsOut("MONEY1"            , String.valueOf(totalReducMoney));
                svf.VrsOut("MONEY2"            , String.valueOf(totalMoney));
                svf.VrsOut("MONEY3_" + moneyLine++            , rs.getString("MONEY_DUE"));
                int grantCnt = 1;
                for (Iterator iterator = grantMap.keySet().iterator(); iterator.hasNext();) {
                    final String grantCd = (String) iterator.next();
                    final Map printGrant = (Map) grantMap.get(grantCd);
                    if (null != (String) printGrant.get("GRANT_NAME")) {
                        svf.VrsOut("GRANT_NAME" + gnameLine, (String) printGrant.get("GRANT_NAME") + "減免額");
                        svf.VrsOut("MONEY3_" + moneyLine++, (String) printGrant.get("GRANT_MONEY"));
                        gnameLine = "2";
                    }
                    grantCnt++;
                    if (grantCnt > grantCnt2) {
                        break;
                    }
                }
                svf.VrsOut("MONEY3_" + moneyLine++            , String.valueOf(totalReducCountryMoney));
                svf.VrsOut("MONEY3_" + moneyLine++            , rs.getString("REDUCTIONMONEY"));
                if ("KNJP332_B_3.frm".equals(frmName) || "KNJP332_B_4.frm".equals(frmName)) {
                    svf.VrsOut("MONEY3_" + moneyLine++, rs.getString("ADJUSTMENT_MONEY"));
                }
                svf.VrsOut("MONEY3_" + moneyLine++            , rs.getString("FEE"));
                svf.VrsOut("TOTAL"            , String.valueOf(totalMoney));
            }
            if ("C".equals(paternCd)) {
                final int totalMoney = rs.getInt("MONEY_DUE") + rs.getInt("FEE");
                svf.VrSetForm(frmName, 1);
                svf.VrsOut("MONEY1"            , String.valueOf(totalMoney));
                svf.VrsOut("MONEY2_1"            , rs.getString("MONEY_DUE"));
                svf.VrsOut("MONEY2_2"            , rs.getString("FEE"));
                svf.VrsOut("TOTAL"            , String.valueOf(totalMoney));
            }
            if ("D".equals(paternCd)) {
                final int totalMoney = rs.getInt("MONEY_DUE") - grantTotal + rs.getInt("FEE");
                svf.VrSetForm(frmName, 1);
                svf.VrsOut("MONEY1"            , String.valueOf(totalMoney));
                svf.VrsOut("MONEY2_" + moneyLine++            , rs.getString("MONEY_DUE"));
                int grantCnt = 1;
                for (Iterator iterator = grantMap.keySet().iterator(); iterator.hasNext();) {
                    final String grantCd = (String) iterator.next();
                    final Map printGrant = (Map) grantMap.get(grantCd);
                    if (null != (String) printGrant.get("GRANT_NAME")) {
                        svf.VrsOut("GRANT_NAME" + gnameLine, (String) printGrant.get("GRANT_NAME") + "減免額");
                        svf.VrsOut("MONEY2_" + moneyLine++, (String) printGrant.get("GRANT_MONEY"));
                        gnameLine = "2";
                    }
                    grantCnt++;
                    if (grantCnt > grantCnt2) {
                        break;
                    }
                }
                svf.VrsOut("MONEY2_" + moneyLine++            , rs.getString("FEE"));
                svf.VrsOut("TOTAL"            , String.valueOf(totalMoney));
            }
            if ("E".equals(paternCd)) {
                final int totalMoney = rs.getInt("MONEY_DUE") - grantTotal;
                svf.VrSetForm(frmName, 1);
                svf.VrsOut("MONEY1"            , String.valueOf(totalMoney));
                svf.VrsOut("MONEY2_" + moneyLine++            , rs.getString("MONEY_DUE"));
                int grantCnt = 1;
                for (Iterator iterator = grantMap.keySet().iterator(); iterator.hasNext();) {
                    final String grantCd = (String) iterator.next();
                    final Map printGrant = (Map) grantMap.get(grantCd);
                    if (null != (String) printGrant.get("GRANT_NAME")) {
                        svf.VrsOut("GRANT_NAME" + gnameLine, (String) printGrant.get("GRANT_NAME") + "減免額");
                        svf.VrsOut("MONEY2_" + moneyLine++, (String) printGrant.get("GRANT_MONEY"));
                        gnameLine = "2";
                    }
                    grantCnt++;
                    if (grantCnt > grantCnt2) {
                        break;
                    }
                }
                svf.VrsOut("TOTAL"            , String.valueOf(totalMoney));
            }
            if ("F".equals(paternCd)) {
                int totalMoney = rs.getInt("MONEY_DUE") - (totalReducCountryMoney + rs.getInt("REDUCTIONMONEY"));
                if (totalMoney > 0) {
                    frmName = "KNJP332_F_2.frm";
                    totalMoney = totalMoney + rs.getInt("FEE");
                }
                svf.VrSetForm(frmName, 1);
                svf.VrsOut("MONEY1"           , String.valueOf(totalReducMoney));
                svf.VrsOut("MONEY2"           , String.valueOf(totalMoney));
                svf.VrsOut("MONEY3_1"         , rs.getString("MONEY_DUE"));
                svf.VrsOut("MONEY3_2"         , String.valueOf(totalReducCountryMoney));
                svf.VrsOut("MONEY3_3"         , rs.getString("REDUCTIONMONEY"));
                svf.VrsOut("MONEY3_4"         , rs.getString("FEE"));
                svf.VrsOut("TOTAL"            , String.valueOf(totalMoney));
            }
            svf.VrsOut("DATE"             , _param._printDate);
            svf.VrsOut("GUARDIANNAME"     , rs.getString("GUARANTOR_NAME"));
            svf.VrsOut("HR_NAME"          , rs.getString("HR_NAME"));
            svf.VrsOut("ATTENDNO"         , rs.getString("ATTENDNO"));
            svf.VrsOut("NAME"             , rs.getString("NAME"));
            svf.VrsOut("NENDO"            , _param._nendo);
            svf.VrsOut("BANKDATE"         , _param._printDate2);
        } catch( Exception ex ) {
            log.warn("printMeisai read error!",ex);
        }

    }//printMeisai()の括り

    /**
     *  都道府県別軽減データを抽出
     *
     */
    private String todoufukenMeisai()
    {
        StringBuffer stb = new StringBuffer();
        try {

            stb.append(" WITH REDUCTION_DAT_SUM AS ( ");
            stb.append(" SELECT ");
            stb.append("     t1.YEAR, ");
            stb.append("     t1.SCHREGNO, ");
            stb.append("     t1.PREFECTURESCD, ");
            if (FORM_TYPE9.equals(_param._yousiki)) {
                stb.append("     0 AS REDUCTIONMONEY ");
            } else {
                stb.append("     SUM(CASE WHEN REDUC_DEC_FLG_1 = '1' AND REDUCTIONMONEY_1 is not null ");
                stb.append("              THEN t1.REDUCTIONMONEY_1 ");
                stb.append("              ELSE 0 ");
                stb.append("         END ");
                stb.append("         + ");
                stb.append("         CASE WHEN REDUC_DEC_FLG_2 = '1' AND REDUCTIONMONEY_2 is not null ");
                stb.append("              THEN t1.REDUCTIONMONEY_2 ");
                stb.append("              ELSE 0 ");
                stb.append("         END ");
                stb.append("     ) AS REDUCTIONMONEY ");
            }
            stb.append(" FROM ");
            stb.append("     REDUCTION_DAT t1 ");
            stb.append(" WHERE ");
            stb.append("    t1.YEAR = '"+ _param._year +"' ");
            stb.append("    AND ((REDUC_DEC_FLG_1 = '1' AND REDUCTIONMONEY_1 is not null) ");
            stb.append("          OR ");
            stb.append("         (REDUC_DEC_FLG_2 = '1' AND REDUCTIONMONEY_2 is not null)) ");
            stb.append(" GROUP BY ");
            stb.append("     t1.YEAR, ");
            stb.append("     t1.SCHREGNO, ");
            stb.append("     t1.PREFECTURESCD ");
            stb.append(" ), BASE_MONEY AS ( ");
            stb.append(" SELECT ");
            stb.append("     YEAR, ");
            stb.append("     SCHREGNO, ");
            stb.append("     MIN(PLAN_YEAR || PLAN_MONTH) AS B_MIN, ");
            stb.append("     MAX(PLAN_YEAR || PLAN_MONTH) AS B_MAX, ");
            stb.append("     VALUE(SUM(PAID_MONEY), 0) AS B_PAID_MONEY ");
            stb.append(" FROM ");
            stb.append("     REDUCTION_COUNTRY_PLAN_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '"+ _param._year +"' ");
            stb.append("     AND VALUE(PLAN_CANCEL_FLG, '0') = '0' ");
            stb.append("     AND VALUE(PLAN_LOCK_FLG, '0') = '0' ");
            stb.append("     AND PAID_YEARMONTH IS NOT NULL ");
            stb.append(" GROUP BY ");
            stb.append("     YEAR, ");
            stb.append("     SCHREGNO ");
            stb.append(" ), ADD_MONEY AS ( ");
            stb.append(" SELECT ");
            stb.append("     YEAR, ");
            stb.append("     SCHREGNO, ");
            stb.append("     MIN(PLAN_YEAR || PLAN_MONTH) AS A_MIN, ");
            stb.append("     MAX(PLAN_YEAR || PLAN_MONTH) AS A_MAX, ");
            stb.append("     VALUE(SUM(ADD_PAID_MONEY), 0) AS A_PAID_MONEY ");
            stb.append(" FROM ");
            stb.append("     REDUCTION_COUNTRY_PLAN_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '"+ _param._year +"' ");
            stb.append("     AND VALUE(ADD_PLAN_CANCEL_FLG, '0') = '0' ");
            stb.append("     AND VALUE(ADD_PLAN_LOCK_FLG, '0') = '0' ");
            stb.append("     AND ADD_PAID_YEARMONTH IS NOT NULL ");
            stb.append(" GROUP BY ");
            stb.append("     YEAR, ");
            stb.append("     SCHREGNO ");
            for (Iterator iterator = _param._G212Map.keySet().iterator(); iterator.hasNext();) {
                final String grantCd = (String) iterator.next();
                stb.append(" ), GRANT_T" + grantCd + " AS ( ");
                stb.append(" SELECT ");
                stb.append("     T1.*, ");
                stb.append("     N1.NAME1 AS GRANTNAME ");
                stb.append(" FROM ");
                stb.append("     SCHREG_GRANT_DAT T1 ");
                stb.append("     INNER JOIN NAME_MST N1 ON N1.NAMECD1 = 'G212' ");
                stb.append("           AND N1.NAMECD2 = T1.GRANTCD ");
                stb.append("           AND VALUE(N1.NAMESPARE1, '') != '1' ");
                stb.append(" WHERE ");
                stb.append("     T1.YEAR = '"+ _param._year +"' ");
                stb.append("     AND T1.GRANTCD = '" + grantCd + "' ");
            }
            stb.append(" ), SCH_T AS ( ");
            stb.append(" SELECT DISTINCT ");
            stb.append("     REGD.YEAR, ");
            stb.append("     REGD.SCHREGNO ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT REGD ");
            stb.append("     INNER JOIN GUARDIAN_DAT t5 ON t5.SCHREGNO = REGD.SCHREGNO ");
            stb.append("     INNER JOIN ZIPCD_MST L2 ON t5.GUARANTOR_ZIPCD = L2.NEW_ZIPCD ");
            stb.append("           AND SUBSTR(L2.CITYCD, 1, 2)  IN "+ _param._inState.toString() +" ");
            stb.append(" WHERE ");
            stb.append("     REGD.YEAR = '"+ _param._year +"' ");
            stb.append("     AND REGD.SEMESTER = '"+ _param._semester +"' ");
            stb.append(" UNION ");
            stb.append(" SELECT DISTINCT ");
            stb.append("     YEAR, ");
            stb.append("     SCHREGNO ");
            stb.append(" FROM ");
            stb.append("     REDUCTION_DAT_SUM ");
            stb.append(" UNION ");
            stb.append(" SELECT DISTINCT ");
            stb.append("     YEAR, ");
            stb.append("     SCHREGNO ");
            stb.append(" FROM ");
            stb.append("     BASE_MONEY ");
            stb.append(" UNION ");
            stb.append(" SELECT DISTINCT ");
            stb.append("     YEAR, ");
            stb.append("     SCHREGNO ");
            stb.append(" FROM ");
            stb.append("     ADD_MONEY ");
            for (Iterator iterator = _param._G212Map.keySet().iterator(); iterator.hasNext();) {
                final String grantCd = (String) iterator.next();
                stb.append(" UNION ");
                stb.append(" SELECT DISTINCT ");
                stb.append("     YEAR, ");
                stb.append("     SCHREGNO ");
                stb.append(" FROM ");
                stb.append("     GRANT_T" + grantCd + " ");
            }
            stb.append(" ), MONEYTBL AS ( ");
            stb.append("SELECT DISTINCT ");
            stb.append("    SCH_T.SCHREGNO, ");
            stb.append("    t5.GUARANTOR_NAME, ");
            stb.append("    t3.HR_NAME, ");
            stb.append("    t2.ATTENDNO, ");
            stb.append("    t2.GRADE || t2.HR_CLASS || t2.ATTENDNO AS GRD_CLASS, ");
            stb.append("    t4.NAME, ");
            stb.append("    L3.NAME1, ");
            stb.append("    t7.MONEY_DUE, ");
            stb.append("    VALUE(t1.REDUCTIONMONEY, 0) - VALUE(ADJUST.TOTAL_ADJUSTMENT_MONEY, 0) AS REDUCTIONMONEY, ");
            stb.append("    VALUE(BASE_M.B_PAID_MONEY, 0) AS B_PAID_MONEY, ");
            stb.append("    VALUE(SUBSTR(BASE_M.B_MIN, 5), '') AS B_MIN, ");
            stb.append("    VALUE(SUBSTR(BASE_M.B_MAX, 5), '') AS B_MAX, ");
            stb.append("    VALUE(ADD_M.A_PAID_MONEY, 0) AS A_PAID_MONEY, ");
            stb.append("    VALUE(SUBSTR(ADD_M.A_MIN, 5), '') AS A_MIN, ");
            stb.append("    VALUE(SUBSTR(ADD_M.A_MAX, 5), '') AS A_MAX, ");
            stb.append("    '165' AS FEE, ");
            stb.append("    VALUE(BURDEN.TOTAL_BURDEN_CHARGE, 0) AS ADJUSTMENT_MONEY, ");
            for (Iterator iterator = _param._G212Map.keySet().iterator(); iterator.hasNext();) {
                final String grantCd = (String) iterator.next();
                stb.append("    VALUE(GRANT_T" + grantCd + ".GRANTNAME, '') AS GRANTNAME" + grantCd + ", ");
                stb.append("    VALUE(GRANT_T" + grantCd + ".GRANT_MONEY, 0) AS GRANT_MONEY" + grantCd + ", ");
            }
            stb.append("    CASE WHEN t7.MONEY_DUE >= VALUE(t1.REDUCTIONMONEY, 0) + VALUE(BURDEN.TOTAL_BURDEN_CHARGE, 0) + VALUE(BASE_M.B_PAID_MONEY, 0) + VALUE(ADD_M.A_PAID_MONEY, 0) - VALUE(ADJUST.TOTAL_ADJUSTMENT_MONEY, 0) THEN '1' ELSE '0' END FRMID, ");
            stb.append("    CASE WHEN t7.MONEY_DUE >= VALUE(t1.REDUCTIONMONEY, 0) + VALUE(BURDEN.TOTAL_BURDEN_CHARGE, 0) + VALUE(BASE_M.B_PAID_MONEY, 0) + VALUE(ADD_M.A_PAID_MONEY, 0) - VALUE(ADJUST.TOTAL_ADJUSTMENT_MONEY, 0)  THEN t7.MONEY_DUE - (VALUE(t1.REDUCTIONMONEY, 0) + VALUE(BURDEN.TOTAL_BURDEN_CHARGE, 0) + VALUE(BASE_M.B_PAID_MONEY, 0) + VALUE(ADD_M.A_PAID_MONEY, 0) - VALUE(ADJUST.TOTAL_ADJUSTMENT_MONEY, 0)) ELSE (VALUE(t1.REDUCTIONMONEY, 0) + VALUE(BURDEN.TOTAL_BURDEN_CHARGE, 0) + VALUE(BASE_M.B_PAID_MONEY, 0) + VALUE(ADD_M.A_PAID_MONEY, 0) - VALUE(ADJUST.TOTAL_ADJUSTMENT_MONEY, 0)) - t7.MONEY_DUE END MONEY, ");
            stb.append("    CASE WHEN t7.MONEY_DUE >= VALUE(t1.REDUCTIONMONEY, 0) + VALUE(BURDEN.TOTAL_BURDEN_CHARGE, 0) + VALUE(BASE_M.B_PAID_MONEY, 0) + VALUE(ADD_M.A_PAID_MONEY, 0) - VALUE(ADJUST.TOTAL_ADJUSTMENT_MONEY, 0) THEN t7.MONEY_DUE - (VALUE(t1.REDUCTIONMONEY, 0) + VALUE(BURDEN.TOTAL_BURDEN_CHARGE, 0) + VALUE(BASE_M.B_PAID_MONEY, 0) + VALUE(ADD_M.A_PAID_MONEY, 0) - VALUE(ADJUST.TOTAL_ADJUSTMENT_MONEY, 0)) + 165 ELSE (VALUE(t1.REDUCTIONMONEY, 0) + VALUE(BURDEN.TOTAL_BURDEN_CHARGE, 0) + VALUE(BASE_M.B_PAID_MONEY, 0) + VALUE(ADD_M.A_PAID_MONEY, 0) - VALUE(ADJUST.TOTAL_ADJUSTMENT_MONEY, 0)) - t7.MONEY_DUE END TOTAL_MONEY ");
            stb.append(" FROM ");
            stb.append("     SCH_T ");
            stb.append("     LEFT JOIN REDUCTION_DAT_SUM t1 ON SCH_T.SCHREGNO = t1.SCHREGNO ");
            stb.append("     LEFT JOIN BASE_MONEY BASE_M ON SCH_T.SCHREGNO = BASE_M.SCHREGNO ");
            stb.append("     LEFT JOIN ADD_MONEY ADD_M ON SCH_T.SCHREGNO = ADD_M.SCHREGNO ");
            stb.append("     LEFT JOIN SCHREG_REGD_DAT t2 ON t2.SCHREGNO = SCH_T.SCHREGNO ");
            stb.append("          AND t2.YEAR = SCH_T.YEAR ");
            stb.append("          AND t2.SEMESTER = '"+ _param._semester +"' ");
            stb.append("     LEFT JOIN SCHREG_REGD_HDAT t3 ON t3.GRADE || t3.HR_CLASS = t2.GRADE || t2.HR_CLASS ");
            stb.append("          AND t3.YEAR = SCH_T.YEAR ");
            stb.append("          AND t3.SEMESTER = '"+ _param._semester +"' ");
            stb.append("     LEFT JOIN SCHREG_BASE_MST t4 ON t4.SCHREGNO = SCH_T.SCHREGNO ");
            stb.append("     INNER JOIN GUARDIAN_DAT t5 ON t5.SCHREGNO = SCH_T.SCHREGNO ");
            stb.append("     INNER JOIN ZIPCD_MST L2 ON t5.GUARANTOR_ZIPCD = L2.NEW_ZIPCD ");
            stb.append("           AND SUBSTR(L2.CITYCD, 1, 2)  IN "+ _param._inState.toString() +" ");
            stb.append("     LEFT JOIN NAME_MST L3 ON L3.NAMECD1 = 'G202' ");
            stb.append("          AND SUBSTR(L2.CITYCD, 1, 2) = L3.NAMECD2 ");
            stb.append("     LEFT JOIN MONEY_DUE_M_DAT t7 ON t7.YEAR = SCH_T.YEAR ");
            stb.append("          AND t7.SCHREGNO = SCH_T.SCHREGNO ");
            if (FORM_TYPE9.equals(_param._yousiki)) {
                stb.append("          AND t7.EXPENSE_M_CD = '12' ");
            } else {
                stb.append("          AND t7.EXPENSE_M_CD = '13' ");
            }
            if (FORM_TYPE9.equals(_param._yousiki)) {
                stb.append("     LEFT JOIN (SELECT L_ADJUST.YEAR, L_ADJUST.SCHREGNO, 0 AS TOTAL_ADJUSTMENT_MONEY FROM REDUCTION_ADJUSTMENT_DAT L_ADJUST) ADJUST ON SCH_T.YEAR = ADJUST.YEAR ");
                stb.append("          AND SCH_T.SCHREGNO = ADJUST.SCHREGNO ");
            } else {
                stb.append("     LEFT JOIN REDUCTION_ADJUSTMENT_DAT ADJUST ON SCH_T.YEAR = ADJUST.YEAR ");
                stb.append("          AND SCH_T.SCHREGNO = ADJUST.SCHREGNO ");
            }
            stb.append("     LEFT JOIN REDUCTION_BURDEN_CHARGE_DAT BURDEN ON BURDEN.YEAR = SCH_T.YEAR ");
            stb.append("          AND BURDEN.SCHREGNO = SCH_T.SCHREGNO ");
            for (Iterator iterator = _param._G212Map.keySet().iterator(); iterator.hasNext();) {
                final String grantCd = (String) iterator.next();
                stb.append("     LEFT JOIN GRANT_T" + grantCd + " ON GRANT_T" + grantCd + ".YEAR = SCH_T.YEAR ");
                stb.append("          AND GRANT_T" + grantCd + ".SCHREGNO = SCH_T.SCHREGNO ");
            }
            stb.append(") ");
            stb.append(" SELECT ");
            stb.append("     * ");
            stb.append(" FROM ");
            stb.append("     MONEYTBL ");
            stb.append(" WHERE ");
            stb.append("     NOT EXISTS( ");
            stb.append("         SELECT ");
            stb.append("             'x' ");
            stb.append("         FROM ");
            stb.append("             SCHREG_REGD_DAT E1, ");
            stb.append("             (SELECT ");
            stb.append("                  BASE.SCHREGNO ");
            stb.append("              FROM ");
            stb.append("                  SCHREG_BASE_MST BASE ");
            stb.append("              WHERE ");
            stb.append("                  BASE.GRD_DIV IN ('2', '3') ");
            stb.append("                  AND BASE.GRD_DATE <= '"+ StringUtils.replace(_param._grdDate, "/", "-") +"') BASE_GRD, ");
            stb.append("             (SELECT ");
            stb.append("                  TRANSFER.SCHREGNO ");
            stb.append("                  FROM ");
            stb.append("                      SCHREG_TRANSFER_DAT TRANSFER ");
            stb.append("                  WHERE ");
            stb.append("                      TRANSFER.TRANSFERCD IN ('2') ");
            stb.append("                      AND TRANSFER.TRANSFER_SDATE >= '" + _param._year + "-04-01' ");
            stb.append("                      AND TRANSFER.TRANSFER_SDATE <= '"+ StringUtils.replace(_param._grdDate, "/", "-") +"' ");
            stb.append("             ) SCH_TRANS ");
            stb.append("         WHERE ");
            stb.append("             E1.YEAR = '"+ _param._year +"' ");
            stb.append("             AND ((E1.SCHREGNO = BASE_GRD.SCHREGNO) OR (E1.SCHREGNO = SCH_TRANS.SCHREGNO AND VALUE(MONEYTBL.MONEY_DUE, 0) = 0)) ");
            stb.append("             AND MONEYTBL.SCHREGNO = E1.SCHREGNO ");
            stb.append("     ) ");
            stb.append("ORDER BY ");
            stb.append("    GRD_CLASS ");

log.debug(stb);
        } catch( Exception e ){
            log.warn("todoufukenMeisai error!",e);
        }
        return stb.toString();

    }//todoufukenMeisai()の括り

    /**
     *  クラス別軽減データを抽出
     *
     */
    private String classMeisai()
    {
        StringBuffer stb = new StringBuffer();
        try {

            stb.append(" WITH REDUCTION_DAT_SUM AS ( ");
            stb.append(" SELECT ");
            stb.append("     t1.YEAR, ");
            stb.append("     t1.SCHREGNO, ");
            stb.append("     t1.PREFECTURESCD, ");
            if (FORM_TYPE9.equals(_param._yousiki)) {
                stb.append("     0 AS REDUCTIONMONEY ");
            } else {
                stb.append("     SUM(CASE WHEN REDUC_DEC_FLG_1 = '1' AND REDUCTIONMONEY_1 is not null ");
                stb.append("              THEN t1.REDUCTIONMONEY_1 ");
                stb.append("              ELSE 0 ");
                stb.append("         END ");
                stb.append("         + ");
                stb.append("         CASE WHEN REDUC_DEC_FLG_2 = '1' AND REDUCTIONMONEY_2 is not null ");
                stb.append("              THEN t1.REDUCTIONMONEY_2 ");
                stb.append("              ELSE 0 ");
                stb.append("         END ");
                stb.append("     ) AS REDUCTIONMONEY ");
            }
            stb.append(" FROM ");
            stb.append("     REDUCTION_DAT t1 ");
            stb.append(" WHERE ");
            stb.append("    t1.YEAR = '"+ _param._year +"' ");
            stb.append("    AND ((REDUC_DEC_FLG_1 = '1' AND REDUCTIONMONEY_1 is not null) ");
            stb.append("          OR ");
            stb.append("         (REDUC_DEC_FLG_2 = '1' AND REDUCTIONMONEY_2 is not null)) ");
            stb.append(" GROUP BY ");
            stb.append("     t1.YEAR, ");
            stb.append("     t1.SCHREGNO, ");
            stb.append("     t1.PREFECTURESCD ");
            stb.append(" ), BASE_MONEY AS ( ");
            stb.append(" SELECT ");
            stb.append("     YEAR, ");
            stb.append("     SCHREGNO, ");
            stb.append("     MIN(PLAN_YEAR || PLAN_MONTH) AS B_MIN, ");
            stb.append("     MAX(PLAN_YEAR || PLAN_MONTH) AS B_MAX, ");
            stb.append("     VALUE(SUM(PAID_MONEY), 0) AS B_PAID_MONEY ");
            stb.append(" FROM ");
            stb.append("     REDUCTION_COUNTRY_PLAN_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '"+ _param._year +"' ");
            stb.append("     AND VALUE(PLAN_CANCEL_FLG, '0') = '0' ");
            stb.append("     AND VALUE(PLAN_LOCK_FLG, '0') = '0' ");
            stb.append("     AND PAID_YEARMONTH IS NOT NULL ");
            stb.append(" GROUP BY ");
            stb.append("     YEAR, ");
            stb.append("     SCHREGNO ");
            stb.append(" ), ADD_MONEY AS ( ");
            stb.append(" SELECT ");
            stb.append("     YEAR, ");
            stb.append("     SCHREGNO, ");
            stb.append("     MIN(PLAN_YEAR || PLAN_MONTH) AS A_MIN, ");
            stb.append("     MAX(PLAN_YEAR || PLAN_MONTH) AS A_MAX, ");
            stb.append("     VALUE(SUM(ADD_PAID_MONEY), 0) AS A_PAID_MONEY ");
            stb.append(" FROM ");
            stb.append("     REDUCTION_COUNTRY_PLAN_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '"+ _param._year +"' ");
            stb.append("     AND VALUE(ADD_PLAN_CANCEL_FLG, '0') = '0' ");
            stb.append("     AND VALUE(ADD_PLAN_LOCK_FLG, '0') = '0' ");
            stb.append("     AND ADD_PAID_YEARMONTH IS NOT NULL ");
            stb.append(" GROUP BY ");
            stb.append("     YEAR, ");
            stb.append("     SCHREGNO ");
            for (Iterator iterator = _param._G212Map.keySet().iterator(); iterator.hasNext();) {
                final String grantCd = (String) iterator.next();
                stb.append(" ), GRANT_T" + grantCd + " AS ( ");
                stb.append(" SELECT ");
                stb.append("     T1.*, ");
                stb.append("     N1.NAME1 AS GRANTNAME ");
                stb.append(" FROM ");
                stb.append("     SCHREG_GRANT_DAT T1 ");
                stb.append("     INNER JOIN NAME_MST N1 ON N1.NAMECD1 = 'G212' ");
                stb.append("           AND N1.NAMECD2 = T1.GRANTCD ");
                stb.append("           AND VALUE(N1.NAMESPARE1, '') != '1' ");
                stb.append(" WHERE ");
                stb.append("     T1.YEAR = '"+ _param._year +"' ");
                stb.append("     AND T1.GRANTCD = '" + grantCd + "' ");
            }
            stb.append(" ), SCH_T AS ( ");
            stb.append(" SELECT DISTINCT ");
            stb.append("     YEAR, ");
            stb.append("     SCHREGNO ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '"+ _param._year +"' ");
            stb.append("     AND SEMESTER = '"+ _param._semester +"' ");
            stb.append("     AND GRADE || HR_CLASS IN "+ _param._inState.toString() +" ");
            stb.append(" UNION ");
            stb.append(" SELECT DISTINCT ");
            stb.append("     YEAR, ");
            stb.append("     SCHREGNO ");
            stb.append(" FROM ");
            stb.append("     REDUCTION_DAT_SUM ");
            stb.append(" UNION ");
            stb.append(" SELECT DISTINCT ");
            stb.append("     YEAR, ");
            stb.append("     SCHREGNO ");
            stb.append(" FROM ");
            stb.append("     BASE_MONEY ");
            stb.append(" UNION ");
            stb.append(" SELECT DISTINCT ");
            stb.append("     YEAR, ");
            stb.append("     SCHREGNO ");
            stb.append(" FROM ");
            stb.append("     ADD_MONEY ");
            for (Iterator iterator = _param._G212Map.keySet().iterator(); iterator.hasNext();) {
                final String grantCd = (String) iterator.next();
                stb.append(" UNION ");
                stb.append(" SELECT DISTINCT ");
                stb.append("     YEAR, ");
                stb.append("     SCHREGNO ");
                stb.append(" FROM ");
                stb.append("     GRANT_T" + grantCd + " ");
            }
            stb.append(" ), MONEYTBL AS ( ");
            stb.append(" SELECT DISTINCT ");
            stb.append("     SCH_T.SCHREGNO, ");
            stb.append("     t5.GUARANTOR_NAME, ");
            stb.append("     t3.HR_NAME, ");
            stb.append("     t2.ATTENDNO, ");
            stb.append("     t2.GRADE || t2.HR_CLASS || t2.ATTENDNO AS GRD_CLASS, ");
            stb.append("     t4.NAME, ");
            stb.append("     L3.NAME1, ");
            stb.append("     t7.MONEY_DUE, ");
            stb.append("     VALUE(t1.REDUCTIONMONEY, 0) - VALUE(ADJUST.TOTAL_ADJUSTMENT_MONEY, 0) AS REDUCTIONMONEY, ");
            stb.append("     VALUE(BASE_M.B_PAID_MONEY, 0) AS B_PAID_MONEY, ");
            stb.append("     VALUE(SUBSTR(BASE_M.B_MIN, 5), '') AS B_MIN, ");
            stb.append("     VALUE(SUBSTR(BASE_M.B_MAX, 5), '') AS B_MAX, ");
            stb.append("     VALUE(ADD_M.A_PAID_MONEY, 0) AS A_PAID_MONEY, ");
            stb.append("     VALUE(SUBSTR(ADD_M.A_MIN, 5), '') AS A_MIN, ");
            stb.append("     VALUE(SUBSTR(ADD_M.A_MAX, 5), '') AS A_MAX, ");
            stb.append("     '165' AS FEE, ");
            stb.append("     VALUE(BURDEN.TOTAL_BURDEN_CHARGE, 0) AS ADJUSTMENT_MONEY, ");
            for (Iterator iterator = _param._G212Map.keySet().iterator(); iterator.hasNext();) {
                final String grantCd = (String) iterator.next();
                stb.append("    VALUE(GRANT_T" + grantCd + ".GRANTNAME, '') AS GRANTNAME" + grantCd + ", ");
                stb.append("    VALUE(GRANT_T" + grantCd + ".GRANT_MONEY, 0) AS GRANT_MONEY" + grantCd + ", ");
            }
            stb.append("     CASE WHEN t7.MONEY_DUE >= VALUE(t1.REDUCTIONMONEY, 0) + VALUE(BURDEN.TOTAL_BURDEN_CHARGE, 0) + VALUE(BASE_M.B_PAID_MONEY, 0) + VALUE(ADD_M.A_PAID_MONEY, 0) - VALUE(ADJUST.TOTAL_ADJUSTMENT_MONEY, 0) THEN '1' ELSE '0' END FRMID, ");
            stb.append("     CASE WHEN t7.MONEY_DUE >= VALUE(t1.REDUCTIONMONEY, 0) + VALUE(BURDEN.TOTAL_BURDEN_CHARGE, 0) + VALUE(BASE_M.B_PAID_MONEY, 0) + VALUE(ADD_M.A_PAID_MONEY, 0) - VALUE(ADJUST.TOTAL_ADJUSTMENT_MONEY, 0) THEN t7.MONEY_DUE - (VALUE(t1.REDUCTIONMONEY, 0) + VALUE(BURDEN.TOTAL_BURDEN_CHARGE, 0) + VALUE(BASE_M.B_PAID_MONEY, 0) + VALUE(ADD_M.A_PAID_MONEY, 0) - VALUE(ADJUST.TOTAL_ADJUSTMENT_MONEY, 0)) ELSE (VALUE(t1.REDUCTIONMONEY, 0) + VALUE(BURDEN.TOTAL_BURDEN_CHARGE, 0) + VALUE(BASE_M.B_PAID_MONEY, 0) + VALUE(ADD_M.A_PAID_MONEY, 0) - VALUE(ADJUST.TOTAL_ADJUSTMENT_MONEY, 0)) - t7.MONEY_DUE END MONEY, ");
            stb.append("     CASE WHEN t7.MONEY_DUE >= VALUE(t1.REDUCTIONMONEY, 0) + VALUE(BURDEN.TOTAL_BURDEN_CHARGE, 0) + VALUE(BASE_M.B_PAID_MONEY, 0) + VALUE(ADD_M.A_PAID_MONEY, 0) - VALUE(ADJUST.TOTAL_ADJUSTMENT_MONEY, 0) THEN t7.MONEY_DUE - (VALUE(t1.REDUCTIONMONEY, 0) + VALUE(BURDEN.TOTAL_BURDEN_CHARGE, 0) + VALUE(BASE_M.B_PAID_MONEY, 0) + VALUE(ADD_M.A_PAID_MONEY, 0) - VALUE(ADJUST.TOTAL_ADJUSTMENT_MONEY, 0)) + 165 ELSE (VALUE(t1.REDUCTIONMONEY, 0) + VALUE(BURDEN.TOTAL_BURDEN_CHARGE, 0) + VALUE(BASE_M.B_PAID_MONEY, 0) + VALUE(ADD_M.A_PAID_MONEY, 0) - VALUE(ADJUST.TOTAL_ADJUSTMENT_MONEY, 0)) - t7.MONEY_DUE END TOTAL_MONEY ");
            stb.append(" FROM ");
            stb.append("     SCH_T ");
            stb.append("     LEFT JOIN REDUCTION_DAT_SUM t1 ON SCH_T.SCHREGNO = t1.SCHREGNO ");
            stb.append("     LEFT JOIN BASE_MONEY BASE_M ON SCH_T.SCHREGNO = BASE_M.SCHREGNO ");
            stb.append("     LEFT JOIN ADD_MONEY ADD_M ON SCH_T.SCHREGNO = ADD_M.SCHREGNO ");
            stb.append("     INNER JOIN SCHREG_REGD_DAT t2 ON t2.SCHREGNO = SCH_T.SCHREGNO ");
            stb.append("           AND t2.YEAR = SCH_T.YEAR ");
            stb.append("           AND t2.SEMESTER = '"+ _param._semester +"' ");
            stb.append("           AND t2.GRADE || t2.HR_CLASS IN "+ _param._inState.toString() +" ");
            stb.append("           AND t2.YEAR = SCH_T.YEAR ");
            stb.append("           AND t2.SEMESTER = '"+ _param._semester +"' ");
            stb.append("     LEFT JOIN SCHREG_REGD_HDAT t3 ON t3.GRADE || t3.HR_CLASS = t2.GRADE || t2.HR_CLASS ");
            stb.append("          AND t3.YEAR = SCH_T.YEAR ");
            stb.append("          AND t3.SEMESTER = '"+ _param._semester +"' ");
            stb.append("     LEFT JOIN SCHREG_BASE_MST t4 ON t4.SCHREGNO = SCH_T.SCHREGNO ");
            stb.append("     INNER JOIN GUARDIAN_DAT t5 ON t5.SCHREGNO = SCH_T.SCHREGNO ");
            stb.append("     INNER JOIN ZIPCD_MST L2 ON t5.GUARANTOR_ZIPCD = L2.NEW_ZIPCD ");
            if (!_param._todouFuken.equals("99")) {
                stb.append("           AND SUBSTR(L2.CITYCD, 1, 2) = '"+ _param._todouFuken +"' ");
            }
            stb.append("     INNER JOIN NAME_MST L3 ON L3.NAMECD1 = 'G202' ");
            stb.append("           AND SUBSTR(L2.CITYCD, 1, 2) = L3.NAMECD2 ");
            stb.append("     LEFT JOIN MONEY_DUE_M_DAT t7 ON t7.YEAR = SCH_T.YEAR ");
            stb.append("          AND t7.SCHREGNO = SCH_T.SCHREGNO ");
            if (FORM_TYPE9.equals(_param._yousiki)) {
                stb.append("          AND t7.EXPENSE_M_CD = '12' ");
            } else {
                stb.append("          AND t7.EXPENSE_M_CD = '13' ");
            }
            if (FORM_TYPE9.equals(_param._yousiki)) {
                stb.append("     LEFT JOIN (SELECT L_ADJUST.YEAR, L_ADJUST.SCHREGNO, 0 AS TOTAL_ADJUSTMENT_MONEY FROM REDUCTION_ADJUSTMENT_DAT L_ADJUST) ADJUST ON SCH_T.YEAR = ADJUST.YEAR ");
                stb.append("          AND SCH_T.SCHREGNO = ADJUST.SCHREGNO ");
            } else {
                stb.append("     LEFT JOIN REDUCTION_ADJUSTMENT_DAT ADJUST ON SCH_T.YEAR = ADJUST.YEAR ");
                stb.append("          AND SCH_T.SCHREGNO = ADJUST.SCHREGNO ");
            }
            stb.append("     LEFT JOIN REDUCTION_BURDEN_CHARGE_DAT BURDEN ON BURDEN.YEAR = SCH_T.YEAR ");
            stb.append("          AND BURDEN.SCHREGNO = SCH_T.SCHREGNO ");
            for (Iterator iterator = _param._G212Map.keySet().iterator(); iterator.hasNext();) {
                final String grantCd = (String) iterator.next();
                stb.append("     LEFT JOIN GRANT_T" + grantCd + " ON GRANT_T" + grantCd + ".YEAR = SCH_T.YEAR ");
                stb.append("          AND GRANT_T" + grantCd + ".SCHREGNO = SCH_T.SCHREGNO ");
            }
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     * ");
            stb.append(" FROM ");
            stb.append("     MONEYTBL ");
            stb.append(" WHERE ");
            stb.append("     NOT EXISTS( ");
            stb.append("         SELECT ");
            stb.append("             'x' ");
            stb.append("         FROM ");
            stb.append("             SCHREG_REGD_DAT E1, ");
            stb.append("             (SELECT ");
            stb.append("                  BASE.SCHREGNO ");
            stb.append("              FROM ");
            stb.append("                  SCHREG_BASE_MST BASE ");
            stb.append("              WHERE ");
            stb.append("                  BASE.GRD_DIV IN ('2', '3') ");
            stb.append("                  AND BASE.GRD_DATE <= '"+ StringUtils.replace(_param._grdDate, "/", "-") +"') BASE_GRD, ");
            stb.append("             (SELECT ");
            stb.append("                  TRANSFER.SCHREGNO ");
            stb.append("                  FROM ");
            stb.append("                      SCHREG_TRANSFER_DAT TRANSFER ");
            stb.append("                  WHERE ");
            stb.append("                      TRANSFER.TRANSFERCD IN ('2') ");
            stb.append("                      AND TRANSFER.TRANSFER_SDATE >= '" + _param._year + "-04-01' ");
            stb.append("                      AND TRANSFER.TRANSFER_SDATE <= '"+ StringUtils.replace(_param._grdDate, "/", "-") +"' ");
            stb.append("             ) SCH_TRANS ");
            stb.append("         WHERE ");
            stb.append("             E1.YEAR = '"+ _param._year +"' ");
            stb.append("             AND ((E1.SCHREGNO = BASE_GRD.SCHREGNO) OR (E1.SCHREGNO = SCH_TRANS.SCHREGNO AND VALUE(MONEYTBL.MONEY_DUE, 0) = 0)) ");
            stb.append("             AND MONEYTBL.SCHREGNO = E1.SCHREGNO ");
            stb.append("     ) ");
            stb.append("ORDER BY ");
            stb.append("    GRD_CLASS ");

log.debug(stb);
        } catch( Exception e ){
            log.warn("classMeisai error!",e);
        }
        return stb.toString();

    }//classMeisai()の括り

    /**
     *  個人別軽減データを抽出
     *
     */
    private String kojinMeisai()
    {
        StringBuffer stb = new StringBuffer();
        try {

            stb.append(" WITH REDUCTION_DAT_SUM AS ( ");
            stb.append(" SELECT ");
            stb.append("     t1.YEAR, ");
            stb.append("     t1.SCHREGNO, ");
            stb.append("     t1.PREFECTURESCD, ");
            if (FORM_TYPE9.equals(_param._yousiki)) {
                stb.append("     0 AS REDUCTIONMONEY ");
            } else {
                stb.append("     SUM(CASE WHEN REDUC_DEC_FLG_1 = '1' AND REDUCTIONMONEY_1 is not null ");
                stb.append("              THEN t1.REDUCTIONMONEY_1 ");
                stb.append("              ELSE 0 ");
                stb.append("         END ");
                stb.append("         + ");
                stb.append("         CASE WHEN REDUC_DEC_FLG_2 = '1' AND REDUCTIONMONEY_2 is not null ");
                stb.append("              THEN t1.REDUCTIONMONEY_2 ");
                stb.append("              ELSE 0 ");
                stb.append("         END ");
                stb.append("     ) AS REDUCTIONMONEY ");
            }
            stb.append(" FROM ");
            stb.append("     REDUCTION_DAT t1 ");
            stb.append(" WHERE ");
            stb.append("    t1.YEAR = '"+ _param._year +"' ");
            stb.append("    AND t1.SCHREGNO IN "+ _param._inState.toString() +" ");
            stb.append("    AND ((REDUC_DEC_FLG_1 = '1' AND REDUCTIONMONEY_1 is not null) ");
            stb.append("          OR ");
            stb.append("         (REDUC_DEC_FLG_2 = '1' AND REDUCTIONMONEY_2 is not null)) ");
            stb.append(" GROUP BY ");
            stb.append("     t1.YEAR, ");
            stb.append("     t1.SCHREGNO, ");
            stb.append("     t1.PREFECTURESCD ");
            stb.append(" ), BASE_MONEY AS ( ");
            stb.append(" SELECT ");
            stb.append("     YEAR, ");
            stb.append("     SCHREGNO, ");
            stb.append("     MIN(PLAN_YEAR || PLAN_MONTH) AS B_MIN, ");
            stb.append("     MAX(PLAN_YEAR || PLAN_MONTH) AS B_MAX, ");
            stb.append("     VALUE(SUM(PAID_MONEY), 0) AS B_PAID_MONEY ");
            stb.append(" FROM ");
            stb.append("     REDUCTION_COUNTRY_PLAN_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '"+ _param._year +"' ");
            stb.append("     AND SCHREGNO IN "+ _param._inState.toString() +" ");
            stb.append("     AND VALUE(PLAN_CANCEL_FLG, '0') = '0' ");
            stb.append("     AND VALUE(PLAN_LOCK_FLG, '0') = '0' ");
            stb.append("     AND PAID_YEARMONTH IS NOT NULL ");
            stb.append(" GROUP BY ");
            stb.append("     YEAR, ");
            stb.append("     SCHREGNO ");
            stb.append(" ), ADD_MONEY AS ( ");
            stb.append(" SELECT ");
            stb.append("     YEAR, ");
            stb.append("     SCHREGNO, ");
            stb.append("     MIN(PLAN_YEAR || PLAN_MONTH) AS A_MIN, ");
            stb.append("     MAX(PLAN_YEAR || PLAN_MONTH) AS A_MAX, ");
            stb.append("     VALUE(SUM(ADD_PAID_MONEY), 0) AS A_PAID_MONEY ");
            stb.append(" FROM ");
            stb.append("     REDUCTION_COUNTRY_PLAN_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '"+ _param._year +"' ");
            stb.append("     AND SCHREGNO IN "+ _param._inState.toString() +" ");
            stb.append("     AND VALUE(ADD_PLAN_CANCEL_FLG, '0') = '0' ");
            stb.append("     AND VALUE(ADD_PLAN_LOCK_FLG, '0') = '0' ");
            stb.append("     AND ADD_PAID_YEARMONTH IS NOT NULL ");
            stb.append(" GROUP BY ");
            stb.append("     YEAR, ");
            stb.append("     SCHREGNO ");
            for (Iterator iterator = _param._G212Map.keySet().iterator(); iterator.hasNext();) {
                final String grantCd = (String) iterator.next();
                stb.append(" ), GRANT_T" + grantCd + " AS ( ");
                stb.append(" SELECT ");
                stb.append("     T1.*, ");
                stb.append("     N1.NAME1 AS GRANTNAME ");
                stb.append(" FROM ");
                stb.append("     SCHREG_GRANT_DAT T1 ");
                stb.append("     INNER JOIN NAME_MST N1 ON N1.NAMECD1 = 'G212' ");
                stb.append("           AND N1.NAMECD2 = T1.GRANTCD ");
                stb.append("           AND VALUE(N1.NAMESPARE1, '') != '1' ");
                stb.append(" WHERE ");
                stb.append("     T1.YEAR = '"+ _param._year +"' ");
                stb.append("     AND T1.GRANTCD = '" + grantCd + "' ");
                stb.append("     AND T1.SCHREGNO IN "+ _param._inState.toString() +" ");
            }
            stb.append(" ), SCH_T AS ( ");
            stb.append(" SELECT DISTINCT ");
            stb.append("     YEAR, ");
            stb.append("     SCHREGNO ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '"+ _param._year +"' ");
            stb.append("     AND SEMESTER = '"+ _param._semester +"' ");
            stb.append("     AND SCHREGNO IN "+ _param._inState.toString() +" ");
            stb.append(" UNION ");
            stb.append(" SELECT DISTINCT ");
            stb.append("     YEAR, ");
            stb.append("     SCHREGNO ");
            stb.append(" FROM ");
            stb.append("     REDUCTION_DAT_SUM ");
            stb.append(" UNION ");
            stb.append(" SELECT DISTINCT ");
            stb.append("     YEAR, ");
            stb.append("     SCHREGNO ");
            stb.append(" FROM ");
            stb.append("     BASE_MONEY ");
            stb.append(" UNION ");
            stb.append(" SELECT DISTINCT ");
            stb.append("     YEAR, ");
            stb.append("     SCHREGNO ");
            stb.append(" FROM ");
            stb.append("     ADD_MONEY ");
            for (Iterator iterator = _param._G212Map.keySet().iterator(); iterator.hasNext();) {
                final String grantCd = (String) iterator.next();
                stb.append(" UNION ");
                stb.append(" SELECT DISTINCT ");
                stb.append("     YEAR, ");
                stb.append("     SCHREGNO ");
                stb.append(" FROM ");
                stb.append("     GRANT_T" + grantCd + " ");
            }
            stb.append(" ), MONEYTBL AS ( ");
            stb.append(" SELECT DISTINCT ");
            stb.append("     SCH_T.SCHREGNO, ");
            stb.append("     t5.GUARANTOR_NAME, ");
            stb.append("     t3.HR_NAME, ");
            stb.append("     t2.ATTENDNO, ");
            stb.append("     t2.GRADE || t2.HR_CLASS || t2.ATTENDNO AS GRD_CLASS, ");
            stb.append("     t4.NAME, ");
            stb.append("     L3.NAME1, ");
            stb.append("     t7.MONEY_DUE, ");
            stb.append("     VALUE(t1.REDUCTIONMONEY, 0) - VALUE(ADJUST.TOTAL_ADJUSTMENT_MONEY, 0) AS REDUCTIONMONEY, ");
            stb.append("     VALUE(BASE_M.B_PAID_MONEY, 0) AS B_PAID_MONEY, ");
            stb.append("     VALUE(SUBSTR(BASE_M.B_MIN, 5), '') AS B_MIN, ");
            stb.append("     VALUE(SUBSTR(BASE_M.B_MAX, 5), '') AS B_MAX, ");
            stb.append("     VALUE(ADD_M.A_PAID_MONEY, 0) AS A_PAID_MONEY, ");
            stb.append("     VALUE(SUBSTR(ADD_M.A_MIN, 5), '') AS A_MIN, ");
            stb.append("     VALUE(SUBSTR(ADD_M.A_MAX, 5), '') AS A_MAX, ");
            stb.append("     '165' AS FEE, ");
            stb.append("     VALUE(BURDEN.TOTAL_BURDEN_CHARGE, 0) AS ADJUSTMENT_MONEY, ");
            for (Iterator iterator = _param._G212Map.keySet().iterator(); iterator.hasNext();) {
                final String grantCd = (String) iterator.next();
                stb.append("    VALUE(GRANT_T" + grantCd + ".GRANTNAME, '') AS GRANTNAME" + grantCd + ", ");
                stb.append("    VALUE(GRANT_T" + grantCd + ".GRANT_MONEY, 0) AS GRANT_MONEY" + grantCd + ", ");
            }
            stb.append("     CASE WHEN t7.MONEY_DUE >= VALUE(t1.REDUCTIONMONEY, 0) + VALUE(BURDEN.TOTAL_BURDEN_CHARGE, 0) + VALUE(BASE_M.B_PAID_MONEY, 0) + VALUE(ADD_M.A_PAID_MONEY, 0) - VALUE(ADJUST.TOTAL_ADJUSTMENT_MONEY, 0) THEN '1' ELSE '0' END FRMID, ");
            stb.append("     CASE WHEN t7.MONEY_DUE >= VALUE(t1.REDUCTIONMONEY, 0) + VALUE(BURDEN.TOTAL_BURDEN_CHARGE, 0) + VALUE(BASE_M.B_PAID_MONEY, 0) + VALUE(ADD_M.A_PAID_MONEY, 0) - VALUE(ADJUST.TOTAL_ADJUSTMENT_MONEY, 0) THEN t7.MONEY_DUE - (VALUE(t1.REDUCTIONMONEY, 0) + VALUE(BURDEN.TOTAL_BURDEN_CHARGE, 0) + VALUE(BASE_M.B_PAID_MONEY, 0) + VALUE(ADD_M.A_PAID_MONEY, 0) - VALUE(ADJUST.TOTAL_ADJUSTMENT_MONEY, 0)) ELSE (VALUE(t1.REDUCTIONMONEY, 0) + VALUE(BURDEN.TOTAL_BURDEN_CHARGE, 0) + VALUE(BASE_M.B_PAID_MONEY, 0) + VALUE(ADD_M.A_PAID_MONEY, 0) - VALUE(ADJUST.TOTAL_ADJUSTMENT_MONEY, 0)) - t7.MONEY_DUE END MONEY, ");
            stb.append("     CASE WHEN t7.MONEY_DUE >= VALUE(t1.REDUCTIONMONEY, 0) + VALUE(BURDEN.TOTAL_BURDEN_CHARGE, 0) + VALUE(BASE_M.B_PAID_MONEY, 0) + VALUE(ADD_M.A_PAID_MONEY, 0) - VALUE(ADJUST.TOTAL_ADJUSTMENT_MONEY, 0) THEN t7.MONEY_DUE - (VALUE(t1.REDUCTIONMONEY, 0) + VALUE(BURDEN.TOTAL_BURDEN_CHARGE, 0) + VALUE(BASE_M.B_PAID_MONEY, 0) + VALUE(ADD_M.A_PAID_MONEY, 0) - VALUE(ADJUST.TOTAL_ADJUSTMENT_MONEY, 0)) + 165 ELSE (VALUE(t1.REDUCTIONMONEY, 0) + VALUE(BURDEN.TOTAL_BURDEN_CHARGE, 0) + VALUE(BASE_M.B_PAID_MONEY, 0) + VALUE(ADD_M.A_PAID_MONEY, 0) - VALUE(ADJUST.TOTAL_ADJUSTMENT_MONEY, 0)) - t7.MONEY_DUE END TOTAL_MONEY ");
            stb.append(" FROM ");
            stb.append("     SCH_T ");
            stb.append("     LEFT JOIN REDUCTION_DAT_SUM t1 ON SCH_T.SCHREGNO = t1.SCHREGNO ");
            stb.append("     LEFT JOIN BASE_MONEY BASE_M ON SCH_T.SCHREGNO = BASE_M.SCHREGNO ");
            stb.append("     LEFT JOIN ADD_MONEY ADD_M ON SCH_T.SCHREGNO = ADD_M.SCHREGNO ");
            stb.append("     LEFT JOIN SCHREG_REGD_DAT t2 ON t2.SCHREGNO = SCH_T.SCHREGNO ");
            stb.append("          AND t2.YEAR = SCH_T.YEAR ");
            stb.append("          AND t2.SEMESTER = '"+ _param._semester +"' ");
            stb.append("     LEFT JOIN SCHREG_REGD_HDAT t3 ON t3.GRADE || t3.HR_CLASS = t2.GRADE || t2.HR_CLASS ");
            stb.append("          AND t3.YEAR = SCH_T.YEAR ");
            stb.append("          AND t3.SEMESTER = '"+ _param._semester +"' ");
            stb.append("     LEFT JOIN SCHREG_BASE_MST t4 ON t4.SCHREGNO = SCH_T.SCHREGNO ");
            stb.append("     INNER JOIN GUARDIAN_DAT t5 ON t5.SCHREGNO = SCH_T.SCHREGNO ");
            stb.append("     INNER JOIN ZIPCD_MST L2 ON t5.GUARANTOR_ZIPCD = L2.NEW_ZIPCD ");
            stb.append("     INNER JOIN NAME_MST L3 ON L3.NAMECD1 = 'G202' ");
            stb.append("           AND SUBSTR(L2.CITYCD, 1, 2) = L3.NAMECD2 ");
            stb.append("     LEFT JOIN MONEY_DUE_M_DAT t7 ON t7.YEAR = SCH_T.YEAR ");
            stb.append("          AND t7.SCHREGNO = SCH_T.SCHREGNO ");
            if (FORM_TYPE9.equals(_param._yousiki)) {
                stb.append("          AND t7.EXPENSE_M_CD = '12' ");
            } else {
                stb.append("          AND t7.EXPENSE_M_CD = '13' ");
            }
            if (FORM_TYPE9.equals(_param._yousiki)) {
                stb.append("     LEFT JOIN (SELECT L_ADJUST.YEAR, L_ADJUST.SCHREGNO, 0 AS TOTAL_ADJUSTMENT_MONEY FROM REDUCTION_ADJUSTMENT_DAT L_ADJUST) ADJUST ON SCH_T.YEAR = ADJUST.YEAR ");
                stb.append("          AND SCH_T.SCHREGNO = ADJUST.SCHREGNO ");
            } else {
                stb.append("     LEFT JOIN REDUCTION_ADJUSTMENT_DAT ADJUST ON SCH_T.YEAR = ADJUST.YEAR ");
                stb.append("          AND SCH_T.SCHREGNO = ADJUST.SCHREGNO ");
            }
            stb.append("     LEFT JOIN REDUCTION_BURDEN_CHARGE_DAT BURDEN ON BURDEN.YEAR = SCH_T.YEAR ");
            stb.append("          AND BURDEN.SCHREGNO = SCH_T.SCHREGNO ");
            for (Iterator iterator = _param._G212Map.keySet().iterator(); iterator.hasNext();) {
                final String grantCd = (String) iterator.next();
                stb.append("     LEFT JOIN GRANT_T" + grantCd + " ON GRANT_T" + grantCd + ".YEAR = SCH_T.YEAR ");
                stb.append("          AND GRANT_T" + grantCd + ".SCHREGNO = SCH_T.SCHREGNO ");
            }
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     * ");
            stb.append(" FROM ");
            stb.append("     MONEYTBL ");
            stb.append(" WHERE ");
            stb.append("     NOT EXISTS( ");
            stb.append("         SELECT ");
            stb.append("             'x' ");
            stb.append("         FROM ");
            stb.append("             SCHREG_REGD_DAT E1, ");
            stb.append("             (SELECT ");
            stb.append("                  BASE.SCHREGNO ");
            stb.append("              FROM ");
            stb.append("                  SCHREG_BASE_MST BASE ");
            stb.append("              WHERE ");
            stb.append("                  BASE.GRD_DIV IN ('2', '3') ");
            stb.append("                  AND BASE.GRD_DATE <= '"+ StringUtils.replace(_param._grdDate, "/", "-") +"') BASE_GRD, ");
            stb.append("             (SELECT ");
            stb.append("                  TRANSFER.SCHREGNO ");
            stb.append("                  FROM ");
            stb.append("                      SCHREG_TRANSFER_DAT TRANSFER ");
            stb.append("                  WHERE ");
            stb.append("                      TRANSFER.TRANSFERCD IN ('2') ");
            stb.append("                      AND TRANSFER.TRANSFER_SDATE >= '" + _param._year + "-04-01' ");
            stb.append("                      AND TRANSFER.TRANSFER_SDATE <= '"+ StringUtils.replace(_param._grdDate, "/", "-") +"' ");
            stb.append("             ) SCH_TRANS ");
            stb.append("         WHERE ");
            stb.append("             E1.YEAR = '"+ _param._year +"' ");
            stb.append("             AND ((E1.SCHREGNO = BASE_GRD.SCHREGNO) OR (E1.SCHREGNO = SCH_TRANS.SCHREGNO AND VALUE(MONEYTBL.MONEY_DUE, 0) = 0)) ");
            stb.append("             AND MONEYTBL.SCHREGNO = E1.SCHREGNO ");
            stb.append("     ) ");
            stb.append("ORDER BY ");
            stb.append("    GRD_CLASS ");

log.debug(stb);
        } catch( Exception e ){
            log.warn("kojinMeisai error!",e);
        }
        return stb.toString();

    }//kojinMeisai()の括り

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 70677+ $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _nendo;
        private final String _semester;
        private final String _date1;
        private final String _printDate;
        private final String _date2;
        private final String _grdDate;
        private final String _printWeek;
        private final String _printDate2;
        private final String _output;
        private final String _yousiki;
        private final String _todouFuken;
        private String _inState;
        private final Map _G212Map;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_year)) + "年度";
            _semester = request.getParameter("SEMESTER");
            _date1 = request.getParameter("DATE1");          //印刷日
            _date2 = request.getParameter("DATE2");          //引落日
            _grdDate = request.getParameter("GRD_DATE");     //異動日付
            _output = request.getParameter("OUTPUT");
            _yousiki = request.getParameter("YOUSIKI");
            _todouFuken = request.getParameter("TODOUFUKEN");     //都道府県
            final String[] selected = request.getParameterValues("SELECT_SELECTED");
            String sep = "";
            _inState = "(";
            for (int i = 0; i < selected.length; i++) {
                _inState += sep + "'" + selected[i] + "'";
                sep = ",";
            }
            _inState += ")";
            _printDate = KNJ_EditDate.h_format_JP(db2, _date1);
            _printWeek = KNJ_EditDate.h_format_W(_date2);
            _printDate2 = KNJ_EditDate.h_format_JP(db2, _date2);
            _G212Map = getG212Map(db2);
        }

        private Map getG212Map(DB2UDB db2) throws SQLException {
            final Map retMap = new TreeMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            final String g212Sql = getG212Sql();
            try {
                ps = db2.prepareStatement(g212Sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    retMap.put(rs.getString("NAMECD2"), rs.getString("NAME1"));
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retMap;
        }

        private String getG212Sql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     * ");
            stb.append(" FROM ");
            stb.append("     V_NAME_MST ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _year + "' ");
            stb.append("     AND NAMECD1 = 'G212' ");
            stb.append(" ORDER BY ");
            stb.append("     NAMECD2 ");

            return stb.toString();
        }

    }

}//クラスの括り
