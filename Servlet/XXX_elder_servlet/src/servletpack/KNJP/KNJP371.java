// kanji=漢字
/*
 * $Id: 2562244c05616a8169d921bb51db5916de8abfa5 $
 *
 * 作成日: 2005/11/27 11:40:00 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJP;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 *
 *  学校教育システム 賢者 [入試管理]
 *
 *                  ＜ＫＮＪＰ３７０＞  振込依頼書
 *
 *  2005/11/27 m-yama 作成
 *  2005/11/27 m-yama NO001 パラメーター印刷対象外追加
 *  2006/03/23 m-yama NO002 プログラム追加によりクラス名変更KNJP370→KNJP371
 *  2006/03/29 m-yama NO003 生活行事費での出力も可
 *  @version $Id: 2562244c05616a8169d921bb51db5916de8abfa5 $
 */

public class KNJP371 {


    private static final Log log = LogFactory.getLog(KNJP371.class);
    private static final String MCD11 = "11";
    private static final String MCD12 = "12";
    private static final String MCD13 = "13";

    Param _param;

    /**
     * KNJP371.classから呼ばれる処理。
     * @param request リクエスト
     * @param response レスポンス
     * @throws Exception IO例外
     */
    //  CSOFF: ExecutableStatementCount
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {

        final Vrw32alp svf = new Vrw32alp();          //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        final PrintWriter outstrm = new PrintWriter(response.getOutputStream());
        DB2UDB db2 = null;                      //Databaseクラスを継承したクラス

        try {

            //  print設定
            response.setContentType("application/pdf");

        //  svf設定
            svf.VrInit();                         //クラスの初期化
            svf.VrSetSpoolFileStream(response.getOutputStream());         //PDFファイル名の設定

        //  ＤＢ接続
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            //パラメータの取得
            _param = createParam(db2, request);

        //  ＳＶＦ作成処理
            boolean nonedata = false;                               //該当データなしフラグ

        //SVF出力
            log.debug("SVF syuturyoku!!!");
            for (final Iterator iter = _param._pageInfo.iterator(); iter.hasNext();) {
                final PageInfo pageInfo = (PageInfo) iter.next();
                nonedata = printMain(db2, svf, pageInfo) ? true : nonedata;
            }

        //  該当データ無し
            if (!nonedata) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note" , "note");
                svf.VrEndPage();
            }
        } finally {
            if (null != svf) {
                svf.VrQuit();
            }
            if (null != db2) {
                db2.commit();
                db2.close();                //DBを閉じる
            }
            if (null != outstrm) {
                outstrm.close();            //ストリームを閉じる
            }
        }

    }
    //  CSON: ExecutableStatementCount

    /**印刷処理メイン NO001*/
    //  CSOFF: ExecutableStatementCount
    private boolean printMain(
            final DB2UDB db2,
            final Vrw32alp svf,
            final PageInfo pageInfo
    ) throws SQLException {
        boolean nonedata = false;
        int gyo  = 1;
        int pcnt = 1;
        int syoukei  = 0;
        int goukei   = 0;
        int totalgyo = 0;
        String bifchangepage = "";
        
        svf.VrSetForm(pageInfo._formName, 1);

        log.debug("printMain start!");

        db2.query(meisaiSql(pageInfo._isMinou));
        final ResultSet rs = db2.getResultSet();

        log.debug("printMain end!");

        while (rs.next()) {

            svf.VrsOut("TITLE", pageInfo._title);
            //合計印字 NO001
            if (!nonedata && pcnt == 1) {
                svf.VrsOut("TOTAL_NUMBER"     , pageInfo._totalCnt);
                svf.VrsOut("TOTAL"            , pageInfo._totalMoney);
            }
            //１ページ印刷
            if (15 < gyo) {
                svf.VrsOut("NUMBER"           , String.valueOf(gyo - 1));
                svf.VrsOut("SUBTOTAL"         , String.valueOf(syoukei));
                svf.VrEndPage();
                gyo = 1;
                pcnt++;
                syoukei = 0;
            } else if (gyo > 1 && !bifchangepage.equalsIgnoreCase(rs.getString("BANKNO"))) {
                svf.VrsOut("NUMBER"           , String.valueOf(gyo - 1));
                svf.VrsOut("SUBTOTAL"         , String.valueOf(syoukei));
                svf.VrEndPage();
                gyo = 1;
                pcnt++;
                syoukei = 0;
            }
            //明細データをセット
            printMeisai(svf, rs, gyo, String.valueOf(pcnt), pageInfo);
            nonedata = true;

            gyo++;
            totalgyo++;
            syoukei = syoukei + rs.getInt("MONEY");
            goukei  = goukei + rs.getInt("MONEY");
            bifchangepage = rs.getString("BANKNO");

        }

        //最終ページ印刷
        if (nonedata) {
            svf.VrsOut("TITLE", pageInfo._title);
            svf.VrsOut("NUMBER"           , String.valueOf(gyo - 1));
            svf.VrsOut("SUBTOTAL"         , String.valueOf(syoukei));
            svf.VrEndPage();
        }

        rs.close();
        db2.commit();
        return nonedata;

    }
    //  CSON: ExecutableStatementCount

    /**明細データをセット*/
    private void printMeisai(
            final Vrw32alp svf,
            final ResultSet rs,
            final int gyo,
            final String pcnt,
            final PageInfo pageInfo
    ) throws SQLException {
        //ヘッダ
        svf.VrsOut("PAGE"         , pageInfo._totalPage + "-" + pcnt);
        svf.VrsOut("BANK1"        , _param._bankName);
        svf.VrsOut("BRANCH1"      , _param._branchName);
        svf.VrsOut("SCHOOLNAME"   , _param._schoolName);
        svf.VrsOut("CHARGE"       , _param._staffName);
        svf.VrsOut("PHONE"        , _param._schoolTel);
        svf.VrsOut("DATE"         , _param._gengou);
        if (!pageInfo._isMinou) {
            svf.VrsOut("AQCCOUNT" , "当座　" + _param._aqccount);
        }
        //明細
        svf.VrsOutn("BANK2"       , gyo    , rs.getString("BANKNAME_KANA"));
        svf.VrsOutn("BRANCH2"     , gyo    , rs.getString("BRANCHNAME_KANA"));
        svf.VrsOutn("DEPOSIT_ITEM", gyo    , rs.getString("ABBV1"));
        svf.VrsOutn("ACCOUNTNO"   , gyo    , rs.getString("ACCOUNTNO"));
        svf.VrsOutn("NAME"        , gyo    , rs.getString("ACCOUNTNAME"));
        svf.VrsOutn("MONEY"       , gyo    , rs.getString("MONEY"));

    }

    /** クラス別軽減データを抽出 */
    //  CSOFF: ExecutableStatementCount|MethodLength
    private String meisaiSql(boolean isMinou) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH REDUCTION_DAT_SUM AS ( ");
        stb.append(" SELECT ");
        stb.append("     t1.YEAR, ");
        stb.append("     t1.SCHREGNO, ");
        stb.append("     SUM(CASE WHEN REDUC_DEC_FLG_1 = '1' ");
        stb.append("              THEN t1.REDUCTIONMONEY_1 ");
        stb.append("              ELSE 0 ");
        stb.append("         END ");
        stb.append("         + ");
        stb.append("         CASE WHEN REDUC_DEC_FLG_2 = '1' ");
        stb.append("              THEN t1.REDUCTIONMONEY_2 ");
        stb.append("              ELSE 0 ");
        stb.append("         END ");
        stb.append("     ) AS REDUCTIONMONEY ");
        stb.append(" FROM ");
        stb.append("     REDUCTION_DAT t1 ");
        stb.append(" WHERE ");
        stb.append("    t1.YEAR = '" + _param._year + "' ");
        stb.append("    AND t1.SCHREGNO " + _param._unprint.toString() + " ");
        stb.append("    AND (REDUC_DEC_FLG_1 = '1' ");
        stb.append("         OR ");
        stb.append("         REDUC_DEC_FLG_2 = '1') ");
        stb.append("    AND  VALUE(OFFSET_FLG, '0') = '1' AND VALUE(LOCK_FLG, '0') <> '1' ");
        stb.append(" GROUP BY ");
        stb.append("     t1.YEAR, ");
        stb.append("     t1.SCHREGNO ");
        stb.append(" ), REDUCTION_COUNTRY_PLAN_DAT_SUM AS ( ");
        stb.append(" SELECT  ");
        stb.append("     T1.YEAR, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     SUM((CASE WHEN VALUE(T1.PAID_YEARMONTH, '0') = '" + _param._yearMonth + "' AND VALUE(T1.PLAN_CANCEL_FLG, '0') = '0' AND VALUE(T1.PLAN_LOCK_FLG, '0') = '0' THEN T1.PLAN_MONEY ");
        stb.append("         ELSE 0 END) ");
        stb.append("     + (CASE WHEN VALUE(T1.ADD_PAID_YEARMONTH, '0') = '" + _param._yearMonth + "' AND VALUE(T1.ADD_PLAN_CANCEL_FLG, '0') = '0' AND VALUE(T1.ADD_PLAN_LOCK_FLG, '0') = '0' THEN T1.ADD_PLAN_MONEY ");
        stb.append("         ELSE 0 END)) AS PAID_PLAN_MONEY ");
        stb.append(" FROM ");
        stb.append("     REDUCTION_COUNTRY_PLAN_DAT T1 ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SCHREGNO " + _param._unprint.toString() + " ");
        stb.append("     AND (VALUE(T1.PAID_YEARMONTH, '0') = '" + _param._yearMonth + "' ");
        stb.append("          OR VALUE(T1.ADD_PAID_YEARMONTH, '0') = '" + _param._yearMonth + "') ");
        stb.append(" GROUP BY ");
        stb.append("     T1.YEAR, ");
        stb.append("     T1.SCHREGNO ");
        stb.append(" ), MONEY_SUM AS ( ");
        stb.append(" SELECT  ");
        stb.append("     '" + _param._year + "' AS YEAR, ");
        stb.append("     VALUE(T1.SCHREGNO, T2.SCHREGNO) AS SCHREGNO, ");
        stb.append("     VALUE(T1.REDUCTIONMONEY, 0) + VALUE(T2.PAID_PLAN_MONEY, 0) - VALUE(T3.TOTAL_ADJUSTMENT_MONEY,0) AS REDUCTIONMONEY ");
        stb.append(" FROM ");
        stb.append("     REDUCTION_DAT_SUM T1 ");
        stb.append("     FULL OUTER JOIN REDUCTION_COUNTRY_PLAN_DAT_SUM T2 ON T1.SCHREGNO = T2.SCHREGNO ");
        stb.append("         AND T1.YEAR = T2.YEAR ");
        stb.append("     LEFT JOIN REDUCTION_ADJUSTMENT_DAT T3 ON T3.YEAR = '" + _param._year + "' ");
        stb.append("         AND T3.SCHREGNO = T1.SCHREGNO ");

        if (_param._month3Flg) {
            stb.append(" ), MONEY_DUE_T AS ( ");
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     SUM(T1.MONEY_DUE) AS MONEY_DUE ");
            stb.append(" FROM ");
            stb.append("     MONEY_DUE_M_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("    T1.YEAR = '" + _param._year + "' ");
            stb.append("    AND T1.EXPENSE_M_CD IN ('" + MCD11 + "', '" + MCD12 + "', '" + MCD13 + "') ");
            stb.append(" GROUP BY ");
            stb.append("     T1.SCHREGNO ");
            stb.append(" ), REDUC_T AS ( ");
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     SUM(CASE WHEN T1.REDUC_DEC_FLG_1 = '1' ");
            stb.append("              THEN VALUE(T1.REDUCTIONMONEY_1, 0) ");
            stb.append("              ELSE 0 ");
            stb.append("         END ");
            stb.append("         + ");
            stb.append("         CASE WHEN T1.REDUC_DEC_FLG_2 = '1' ");
            stb.append("              THEN VALUE(T1.REDUCTIONMONEY_2, 0) ");
            stb.append("              ELSE 0 ");
            stb.append("         END ");
            stb.append("     ) AS REDUCTIONMONEY ");
            stb.append(" FROM ");
            stb.append("     REDUCTION_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("    T1.YEAR = '" + _param._year + "' ");
            stb.append("    AND (T1.REDUC_DEC_FLG_1 = '1' ");
            stb.append("         OR ");
            stb.append("         T1.REDUC_DEC_FLG_2 = '1') ");
            stb.append(" GROUP BY ");
            stb.append("     T1.SCHREGNO ");
            stb.append(" ), REDUC_COUNTRY_T AS ( ");
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     SUM(CASE WHEN VALUE(T1.PLAN_CANCEL_FLG, '0') = '0' ");
            stb.append("              THEN VALUE(T1.PLAN_MONEY, 0) ");
            stb.append("              ELSE 0 ");
            stb.append("         END ");
            stb.append("         + ");
            stb.append("         CASE WHEN VALUE(T1.ADD_PLAN_CANCEL_FLG, '0') = '0' ");
            stb.append("              THEN VALUE(T1.ADD_PLAN_MONEY, 0) ");
            stb.append("              ELSE 0 ");
            stb.append("         END ");
            stb.append("     ) AS REDUCTION_C_MONEY, ");
            stb.append("     SUM(CASE WHEN T1.PAID_YEARMONTH <= '" + _param._year + "12" + "' AND VALUE(T1.PLAN_CANCEL_FLG, '0') = '0' ");
            stb.append("              THEN VALUE(T1.PLAN_MONEY, 0) ");
            stb.append("              ELSE 0 ");
            stb.append("         END ");
            stb.append("         + ");
            stb.append("         CASE WHEN T1.ADD_PAID_YEARMONTH <= '" + _param._year + "12" + "' AND VALUE(T1.ADD_PLAN_CANCEL_FLG, '0') = '0' ");
            stb.append("              THEN VALUE(T1.ADD_PLAN_MONEY, 0) ");
            stb.append("              ELSE 0 ");
            stb.append("         END ");
            stb.append("     ) AS REDUCTION_C12_MONEY ");
            stb.append(" FROM ");
            stb.append("     REDUCTION_COUNTRY_PLAN_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("    T1.YEAR = '" + _param._year + "' ");
            stb.append("    AND (VALUE(T1.PLAN_CANCEL_FLG, '0') = '0' ");
            stb.append("         OR ");
            stb.append("         VALUE(T1.ADD_PLAN_CANCEL_FLG, '0') = '0') ");
            stb.append(" GROUP BY ");
            stb.append("     T1.SCHREGNO ");
            stb.append(" ), ADJUST AS ( ");
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     VALUE(T1.MONEY_DUE, 0) AS TOTAL_LESSON_MONEY, ");
            stb.append("     VALUE(L1.REDUCTIONMONEY, 0) AS REDUCTIONMONEY, ");
            stb.append("     VALUE(L2.REDUCTION_C_MONEY, 0) AS REDUCTION_COUNTRY_MONEY, ");
            stb.append("     VALUE(L2.REDUCTION_C12_MONEY, 0) AS REDUCTION_C12_MONEY ");
            stb.append(" FROM ");
            stb.append("     MONEY_DUE_T T1 ");
            stb.append("     LEFT JOIN REDUC_T L1 ON T1.SCHREGNO = L1.SCHREGNO ");
            stb.append("     LEFT JOIN REDUC_COUNTRY_T L2 ON T1.SCHREGNO = L2.SCHREGNO ");
            stb.append(" WHERE ");
            stb.append("     VALUE(L1.REDUCTIONMONEY, 0) + VALUE(L2.REDUCTION_C_MONEY, 0) < VALUE(T1.MONEY_DUE, 0) ");

            stb.append(" ), OVER_MONEY AS ( ");
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     VALUE(T1.TOTAL_BURDEN_CHARGE, 0) AS OVER_MONEY ");
            stb.append(" FROM ");
            stb.append("     REDUCTION_BURDEN_CHARGE_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _param._year + "' ");
            stb.append("     AND EXISTS( ");
            stb.append("          SELECT ");
            stb.append("              'x' ");
            stb.append("          FROM ");
            stb.append("              MONEY_SUM E1 ");
            stb.append("          WHERE ");
            stb.append("             T1.SCHREGNO = E1.SCHREGNO ");
            stb.append("     ) ");
        }
        stb.append(" ), SCHREG_DAT AS ( ");
        stb.append(" SELECT DISTINCT ");
        stb.append("     T1.SCHREGNO ");
        stb.append(" FROM ");
        stb.append("     MONEY_SUM T1 ");
        if (_param._month3Flg) {
            stb.append(" UNION ");
            stb.append(" SELECT DISTINCT ");
            stb.append("     T1.SCHREGNO ");
            stb.append(" FROM ");
            stb.append("     OVER_MONEY T1 ");
        }

        stb.append(" ), MINOU_SCH AS ( ");
        stb.append(" SELECT DISTINCT ");
        stb.append("     T1.SCHREGNO ");
        stb.append(" FROM ");
        stb.append("     MONEY_DUE_M_DAT T1 ");
        stb.append("     LEFT JOIN MONEY_PAID_M_DAT L1 ON T1.YEAR = L1.YEAR ");
        stb.append("          AND T1.SCHREGNO = L1.SCHREGNO ");
        stb.append("          AND T1.EXPENSE_M_CD = L1.EXPENSE_M_CD ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.EXPENSE_M_CD = '" + MCD11 + "' ");
        stb.append("     AND (L1.SCHREGNO IS NULL ");
        stb.append("          OR ");
        stb.append("          VALUE(L1.PAID_MONEY, 0) <= 0) ");
        stb.append(" UNION   ");
        stb.append(" SELECT DISTINCT ");
        stb.append("     T1.SCHREGNO ");
        stb.append(" FROM ");
        stb.append("     MONEY_DUE_M_DAT T1 ");
        stb.append("     LEFT JOIN MONEY_PAID_M_DAT L1 ON T1.YEAR = L1.YEAR ");
        stb.append("          AND T1.SCHREGNO = L1.SCHREGNO ");
        stb.append("          AND T1.EXPENSE_M_CD = L1.EXPENSE_M_CD ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.EXPENSE_M_CD = '" + MCD12 + "' ");
        stb.append("     AND (L1.SCHREGNO IS NULL ");
        stb.append("          OR ");
        stb.append("          VALUE(L1.PAID_MONEY, 0) <= 0) ");
        if (_param._month3Flg) {
            stb.append(" UNION   ");
            stb.append(" SELECT DISTINCT ");
            stb.append("     T1.SCHREGNO ");
            stb.append(" FROM ");
            stb.append("     ADJUST T1 ");
            stb.append("     LEFT JOIN (SELECT ");
            stb.append("                    LL1.SCHREGNO, ");
            stb.append("                    SUM(VALUE(LL1.PAID_MONEY, 0)) AS PAID_MONEY ");
            stb.append("                FROM ");
            stb.append("                    MONEY_PAID_M_DAT LL1 ");
            stb.append("                WHERE ");
            stb.append("                    LL1.YEAR = '" + _param._year + "' ");
            stb.append("                    AND LL1.EXPENSE_M_CD IN ('" + MCD11 + "', '" + MCD12 + "', '" + MCD13 + "') ");
            stb.append("                GROUP BY ");
            stb.append("                    LL1.SCHREGNO ");
            stb.append("                ) L1 ON T1.SCHREGNO = L1.SCHREGNO ");
            stb.append(" WHERE ");
            stb.append("     T1.TOTAL_LESSON_MONEY > L1.PAID_MONEY + T1.REDUCTIONMONEY + T1.REDUCTION_C12_MONEY ");
        }
        stb.append("), MONEYTBL AS ( ");
        stb.append("SELECT ");
        stb.append("    t2.GRADE || t2.HR_CLASS || ATTENDNO AS GRD_CLS_ATD, ");
        stb.append("    t3.BANKCD, ");
        stb.append("    t4.BANKNAME_KANA, ");
        stb.append("    t3.BRANCHCD, ");
        stb.append("    t4.BRANCHNAME_KANA, ");
        stb.append("    t3.DEPOSIT_ITEM, ");
        stb.append("    t5.ABBV1, ");
        stb.append("    t3.ACCOUNTNO, ");
        stb.append("    t3.ACCOUNTNAME, ");

        stb.append("    t1.REDUCTIONMONEY, ");
        if (!_param._month3Flg) {
            stb.append("    CASE WHEN t6.MONEY_DUE >= t1.REDUCTIONMONEY THEN '1' ELSE '0' END FRMID, ");
            stb.append("    CASE WHEN t6.MONEY_DUE >= t1.REDUCTIONMONEY ");
            stb.append("         THEN t6.MONEY_DUE - t1.REDUCTIONMONEY ");
            stb.append("         ELSE t1.REDUCTIONMONEY - t6.MONEY_DUE END MONEY ");
        } else {
            stb.append("    CASE WHEN CASE WHEN RS.REDUCTIONMONEY > 0 THEN t6.MONEY_DUE ELSE 0 END >= t1.REDUCTIONMONEY + VALUE(OVER_MONEY.OVER_MONEY, 0) THEN '1' ELSE '0' END FRMID, ");
            stb.append("    CASE WHEN CASE WHEN RS.REDUCTIONMONEY > 0 THEN t6.MONEY_DUE ELSE 0 END >= t1.REDUCTIONMONEY + VALUE(OVER_MONEY.OVER_MONEY, 0) ");
            stb.append("         THEN CASE WHEN RS.REDUCTIONMONEY > 0 THEN t6.MONEY_DUE ELSE 0 END - (t1.REDUCTIONMONEY + VALUE(OVER_MONEY.OVER_MONEY, 0)) ");
            stb.append("         ELSE (t1.REDUCTIONMONEY + VALUE(OVER_MONEY.OVER_MONEY, 0)) - CASE WHEN RS.REDUCTIONMONEY > 0 THEN t6.MONEY_DUE ELSE 0 END END MONEY ");
        }
        stb.append("FROM ");
        stb.append("    SCHREG_DAT ");
        stb.append("    LEFT JOIN MONEY_SUM t1 ON t1.SCHREGNO = SCHREG_DAT.SCHREGNO ");
        stb.append("    LEFT JOIN SCHREG_REGD_DAT t2 ON t2.SCHREGNO = SCHREG_DAT.SCHREGNO ");
        stb.append("    AND t2.YEAR = '" + _param._year + "' ");
        stb.append("    AND t2.SEMESTER = '" + _param._semester + "' ");
        stb.append("    LEFT JOIN REGISTBANK_DAT t3 ON t3.SCHREGNO = SCHREG_DAT.SCHREGNO ");
        stb.append("    LEFT JOIN BANK_MST t4 ON t4.BANKCD = t3.BANKCD ");
        stb.append("    AND t4.BRANCHCD = t3.BRANCHCD ");
        stb.append("    LEFT JOIN NAME_MST t5 ON t5.NAMECD1 = 'G203' ");
        stb.append("    AND t5.NAMECD2 = t3.DEPOSIT_ITEM ");
        stb.append("    LEFT JOIN MONEY_DUE_M_DAT t6 ON t6.YEAR = '" + _param._year + "' ");
        stb.append("    AND t6.SCHREGNO = SCHREG_DAT.SCHREGNO ");
        stb.append("    AND t6.EXPENSE_M_CD = '" + _param._taisyouMcd + "' ");
        if (_param._month3Flg) {
            stb.append("    LEFT JOIN OVER_MONEY ON OVER_MONEY.SCHREGNO = SCHREG_DAT.SCHREGNO ");
            stb.append("    LEFT JOIN REDUCTION_DAT_SUM RS ON RS.SCHREGNO = SCHREG_DAT.SCHREGNO ");
        }
        stb.append("    LEFT JOIN MINOU_SCH ON MINOU_SCH.SCHREGNO = SCHREG_DAT.SCHREGNO ");
        stb.append("WHERE ");
        if (isMinou) {
            stb.append("    MINOU_SCH.SCHREGNO IS NOT NULL ");
        } else {
            stb.append("    MINOU_SCH.SCHREGNO IS NULL ");
        }
        stb.append(") ");
        stb.append("SELECT ");
        stb.append("    '1' AS BANKNO, ");
        stb.append("    GRD_CLS_ATD, ");
        stb.append("    BANKCD, ");
        stb.append("    BANKNAME_KANA, ");
        stb.append("    BRANCHCD, ");
        stb.append("    BRANCHNAME_KANA, ");
        stb.append("    DEPOSIT_ITEM, ");
        stb.append("    ABBV1, ");
        stb.append("    ACCOUNTNO, ");
        stb.append("    ACCOUNTNAME, ");
        stb.append("    FRMID, ");
        stb.append("    MONEY ");
        stb.append("FROM ");
        stb.append("    MONEYTBL ");
        stb.append("WHERE ");
        stb.append("    FRMID = '0' ");
        if (!isMinou) {
            stb.append("    AND BANKCD = '" + _param._var1 + "' ");
            stb.append("UNION ALL ");
            stb.append("SELECT ");
            stb.append("    '2' AS BANKNO, ");
            stb.append("    GRD_CLS_ATD, ");
            stb.append("    BANKCD, ");
            stb.append("    BANKNAME_KANA, ");
            stb.append("    BRANCHCD, ");
            stb.append("    BRANCHNAME_KANA, ");
            stb.append("    DEPOSIT_ITEM, ");
            stb.append("    ABBV1, ");
            stb.append("    ACCOUNTNO, ");
            stb.append("    ACCOUNTNAME, ");
            stb.append("    FRMID, ");
            stb.append("    MONEY ");
            stb.append("FROM ");
            stb.append("    MONEYTBL ");
            stb.append("WHERE ");
            stb.append("    FRMID = '0' ");
            stb.append("    AND (BANKCD < '" + _param._var1 + "' OR BANKCD > '" + _param._var1 + "') ");
        }
        stb.append("ORDER BY ");
        stb.append("    BANKNO,GRD_CLS_ATD ");

        log.debug(stb);
        return stb.toString();

    }
    //  CSON: ExecutableStatementCount|MethodLength

    /**
     *  トータル金額を抽出 NO001
     *
     */
    private String totalMoneySql(
            final String year,
            final String yearMonth,
            final String unprint,
            final boolean isMinou,
            final boolean month3Flg,
            final String taisyouMcd
    ) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH REDUCTION_DAT_SUM AS ( ");
        stb.append(" SELECT ");
        stb.append("     t1.YEAR, ");
        stb.append("     t1.SCHREGNO, ");
        stb.append("     SUM(CASE WHEN REDUC_DEC_FLG_1 = '1' ");
        stb.append("              THEN t1.REDUCTIONMONEY_1 ");
        stb.append("              ELSE 0 ");
        stb.append("         END ");
        stb.append("         + ");
        stb.append("         CASE WHEN REDUC_DEC_FLG_2 = '1' ");
        stb.append("              THEN t1.REDUCTIONMONEY_2 ");
        stb.append("              ELSE 0 ");
        stb.append("         END ");
        stb.append("     ) AS REDUCTIONMONEY ");
        stb.append(" FROM ");
        stb.append("     REDUCTION_DAT t1 ");
        stb.append(" WHERE ");
        stb.append("    t1.YEAR = '" + year + "' ");
        stb.append("    AND t1.SCHREGNO " + unprint + " ");
        stb.append("    AND (REDUC_DEC_FLG_1 = '1' ");
        stb.append("         OR ");
        stb.append("         REDUC_DEC_FLG_2 = '1') ");
        stb.append("    AND  VALUE(OFFSET_FLG, '0') = '1' AND VALUE(LOCK_FLG, '0') <> '1' ");
        stb.append(" GROUP BY ");
        stb.append("     t1.YEAR, ");
        stb.append("     t1.SCHREGNO ");
        stb.append(" ), REDUCTION_COUNTRY_PLAN_DAT_SUM AS ( ");
        stb.append(" SELECT  ");
        stb.append("     T1.YEAR, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     SUM((CASE WHEN VALUE(T1.PAID_YEARMONTH, '0') = '" + yearMonth + "' AND VALUE(T1.PLAN_CANCEL_FLG, '0') = '0' AND VALUE(T1.PLAN_LOCK_FLG, '0') = '0' THEN T1.PLAN_MONEY ");
        stb.append("         ELSE 0 END) ");
        stb.append("     + (CASE WHEN VALUE(T1.ADD_PAID_YEARMONTH, '0') = '" + yearMonth + "' AND VALUE(T1.ADD_PLAN_CANCEL_FLG, '0') = '0' AND VALUE(T1.ADD_PLAN_LOCK_FLG, '0') = '0' THEN T1.ADD_PLAN_MONEY ");
        stb.append("         ELSE 0 END)) AS PAID_PLAN_MONEY ");
        stb.append(" FROM ");
        stb.append("     REDUCTION_COUNTRY_PLAN_DAT T1 ");
        stb.append(" WHERE ");
        stb.append("     (VALUE(T1.PAID_YEARMONTH, '0') = '" + yearMonth + "' ");
        stb.append("      OR VALUE(T1.ADD_PAID_YEARMONTH, '0') = '" + yearMonth + "') ");
        stb.append(" GROUP BY ");
        stb.append("     T1.YEAR, ");
        stb.append("     T1.SCHREGNO ");
        stb.append(" ), MONEY_SUM AS ( ");
        stb.append(" SELECT  ");
        stb.append("     '" + year + "' AS YEAR, ");
        stb.append("     VALUE(T1.SCHREGNO, T2.SCHREGNO) AS SCHREGNO, ");
        stb.append("     VALUE(T1.REDUCTIONMONEY, 0) + VALUE(T2.PAID_PLAN_MONEY, 0) - VALUE(T3.TOTAL_ADJUSTMENT_MONEY,0) AS REDUCTIONMONEY ");
        stb.append(" FROM ");
        stb.append("     REDUCTION_DAT_SUM T1 ");
        stb.append("     FULL OUTER JOIN REDUCTION_COUNTRY_PLAN_DAT_SUM T2 ON T1.SCHREGNO = T2.SCHREGNO ");
        stb.append("         AND T1.YEAR = T2.YEAR ");
        stb.append("     LEFT JOIN REDUCTION_ADJUSTMENT_DAT T3 ON T3.YEAR = '" + year + "' ");
        stb.append("         AND T3.SCHREGNO = T1.SCHREGNO ");

        if (month3Flg) {
            stb.append(" ), MONEY_DUE_T AS ( ");
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     SUM(T1.MONEY_DUE) AS MONEY_DUE ");
            stb.append(" FROM ");
            stb.append("     MONEY_DUE_M_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("    T1.YEAR = '" + year + "' ");
            stb.append("    AND T1.EXPENSE_M_CD IN ('" + MCD11 + "', '" + MCD12 + "', '" + MCD13 + "') ");
            stb.append(" GROUP BY ");
            stb.append("     T1.SCHREGNO ");
            stb.append(" ), REDUC_T AS ( ");
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     SUM(CASE WHEN T1.REDUC_DEC_FLG_1 = '1' ");
            stb.append("              THEN VALUE(T1.REDUCTIONMONEY_1, 0) ");
            stb.append("              ELSE 0 ");
            stb.append("         END ");
            stb.append("         + ");
            stb.append("         CASE WHEN T1.REDUC_DEC_FLG_2 = '1' ");
            stb.append("              THEN VALUE(T1.REDUCTIONMONEY_2, 0) ");
            stb.append("              ELSE 0 ");
            stb.append("         END ");
            stb.append("     ) AS REDUCTIONMONEY ");
            stb.append(" FROM ");
            stb.append("     REDUCTION_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("    T1.YEAR = '" + year + "' ");
            stb.append("    AND (T1.REDUC_DEC_FLG_1 = '1' ");
            stb.append("         OR ");
            stb.append("         T1.REDUC_DEC_FLG_2 = '1') ");
            stb.append(" GROUP BY ");
            stb.append("     T1.SCHREGNO ");
            stb.append(" ), REDUC_COUNTRY_T AS ( ");
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     SUM(CASE WHEN VALUE(T1.PLAN_CANCEL_FLG, '0') = '0' ");
            stb.append("              THEN VALUE(T1.PLAN_MONEY, 0) ");
            stb.append("              ELSE 0 ");
            stb.append("         END ");
            stb.append("         + ");
            stb.append("         CASE WHEN VALUE(T1.ADD_PLAN_CANCEL_FLG, '0') = '0' ");
            stb.append("              THEN VALUE(T1.ADD_PLAN_MONEY, 0) ");
            stb.append("              ELSE 0 ");
            stb.append("         END ");
            stb.append("     ) AS REDUCTION_C_MONEY, ");
            stb.append("     SUM(CASE WHEN T1.PAID_YEARMONTH <= '" + year + "12" + "' AND VALUE(T1.PLAN_CANCEL_FLG, '0') = '0' ");
            stb.append("              THEN VALUE(T1.PLAN_MONEY, 0) ");
            stb.append("              ELSE 0 ");
            stb.append("         END ");
            stb.append("         + ");
            stb.append("         CASE WHEN T1.ADD_PAID_YEARMONTH <= '" + year + "12" + "' AND VALUE(T1.ADD_PLAN_CANCEL_FLG, '0') = '0' ");
            stb.append("              THEN VALUE(T1.ADD_PLAN_MONEY, 0) ");
            stb.append("              ELSE 0 ");
            stb.append("         END ");
            stb.append("     ) AS REDUCTION_C12_MONEY ");
            stb.append(" FROM ");
            stb.append("     REDUCTION_COUNTRY_PLAN_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("    T1.YEAR = '" + year + "' ");
            stb.append("    AND (VALUE(T1.PLAN_CANCEL_FLG, '0') = '0' ");
            stb.append("         OR ");
            stb.append("         VALUE(T1.ADD_PLAN_CANCEL_FLG, '0') = '0') ");
            stb.append(" GROUP BY ");
            stb.append("     T1.SCHREGNO ");
            stb.append(" ), ADJUST AS ( ");
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     VALUE(T1.MONEY_DUE, 0) AS TOTAL_LESSON_MONEY, ");
            stb.append("     VALUE(L1.REDUCTIONMONEY, 0) AS REDUCTIONMONEY, ");
            stb.append("     VALUE(L2.REDUCTION_C_MONEY, 0) AS REDUCTION_COUNTRY_MONEY, ");
            stb.append("     VALUE(L2.REDUCTION_C12_MONEY, 0) AS REDUCTION_C12_MONEY ");
            stb.append(" FROM ");
            stb.append("     MONEY_DUE_T T1 ");
            stb.append("     LEFT JOIN REDUC_T L1 ON T1.SCHREGNO = L1.SCHREGNO ");
            stb.append("     LEFT JOIN REDUC_COUNTRY_T L2 ON T1.SCHREGNO = L2.SCHREGNO ");
            stb.append(" WHERE ");
            stb.append("     VALUE(L1.REDUCTIONMONEY, 0) + VALUE(L2.REDUCTION_C_MONEY, 0) < VALUE(T1.MONEY_DUE, 0) ");

            stb.append(" ), OVER_MONEY AS ( ");
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     VALUE(T1.TOTAL_BURDEN_CHARGE, 0) AS OVER_MONEY ");
            stb.append(" FROM ");
            stb.append("     REDUCTION_BURDEN_CHARGE_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + year + "' ");
            stb.append("     AND EXISTS( ");
            stb.append("          SELECT ");
            stb.append("              'x' ");
            stb.append("          FROM ");
            stb.append("              MONEY_SUM E1 ");
            stb.append("          WHERE ");
            stb.append("             T1.SCHREGNO = E1.SCHREGNO ");
            stb.append("     ) ");
        }
        stb.append(" ), SCHREG_DAT AS ( ");
        stb.append(" SELECT DISTINCT ");
        stb.append("     T1.SCHREGNO ");
        stb.append(" FROM ");
        stb.append("     MONEY_SUM T1 ");
        if (month3Flg) {
            stb.append(" UNION ");
            stb.append(" SELECT DISTINCT ");
            stb.append("     T1.SCHREGNO ");
            stb.append(" FROM ");
            stb.append("     OVER_MONEY T1 ");
        }

        stb.append(" ), MINOU_SCH AS ( ");
        stb.append(" SELECT DISTINCT ");
        stb.append("     T1.SCHREGNO ");
        stb.append(" FROM ");
        stb.append("     MONEY_DUE_M_DAT T1 ");
        stb.append("     LEFT JOIN MONEY_PAID_M_DAT L1 ON T1.YEAR = L1.YEAR ");
        stb.append("          AND T1.SCHREGNO = L1.SCHREGNO ");
        stb.append("          AND T1.EXPENSE_M_CD = L1.EXPENSE_M_CD ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + year + "' ");
        stb.append("     AND T1.EXPENSE_M_CD = '" + MCD11 + "' ");
        stb.append("     AND (L1.SCHREGNO IS NULL ");
        stb.append("          OR ");
        stb.append("          VALUE(L1.PAID_MONEY, 0) <= 0) ");
        stb.append(" UNION   ");
        stb.append(" SELECT DISTINCT ");
        stb.append("     T1.SCHREGNO ");
        stb.append(" FROM ");
        stb.append("     MONEY_DUE_M_DAT T1 ");
        stb.append("     LEFT JOIN MONEY_PAID_M_DAT L1 ON T1.YEAR = L1.YEAR ");
        stb.append("          AND T1.SCHREGNO = L1.SCHREGNO ");
        stb.append("          AND T1.EXPENSE_M_CD = L1.EXPENSE_M_CD ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + year + "' ");
        stb.append("     AND T1.EXPENSE_M_CD = '" + MCD12 + "' ");
        stb.append("     AND (L1.SCHREGNO IS NULL ");
        stb.append("          OR ");
        stb.append("          VALUE(L1.PAID_MONEY, 0) <= 0) ");
        if (month3Flg) {
            stb.append(" UNION   ");
            stb.append(" SELECT DISTINCT ");
            stb.append("     T1.SCHREGNO ");
            stb.append(" FROM ");
            stb.append("     ADJUST T1 ");
            stb.append("     LEFT JOIN (SELECT ");
            stb.append("                    LL1.SCHREGNO, ");
            stb.append("                    SUM(VALUE(LL1.PAID_MONEY, 0)) AS PAID_MONEY ");
            stb.append("                FROM ");
            stb.append("                    MONEY_PAID_M_DAT LL1 ");
            stb.append("                WHERE ");
            stb.append("                    LL1.YEAR = '" + year + "' ");
            stb.append("                    AND LL1.EXPENSE_M_CD IN ('" + MCD11 + "', '" + MCD12 + "', '" + MCD13 + "') ");
            stb.append("                GROUP BY ");
            stb.append("                    LL1.SCHREGNO ");
            stb.append("                ) L1 ON T1.SCHREGNO = L1.SCHREGNO ");
            stb.append(" WHERE ");
            stb.append("     T1.TOTAL_LESSON_MONEY > L1.PAID_MONEY + T1.REDUCTIONMONEY + T1.REDUCTION_C12_MONEY ");
        }
        stb.append("), MONEYTBL AS (SELECT ");
        if (!month3Flg) {
            stb.append("    CASE WHEN t2.MONEY_DUE >= t1.REDUCTIONMONEY THEN '1' ELSE '0' END FRMID, ");
            stb.append("    CASE WHEN t2.MONEY_DUE >= t1.REDUCTIONMONEY ");
            stb.append("         THEN t2.MONEY_DUE - t1.REDUCTIONMONEY ");
            stb.append("         ELSE t1.REDUCTIONMONEY - t2.MONEY_DUE END MONEY ");
        } else {
            stb.append("    CASE WHEN CASE WHEN RS.REDUCTIONMONEY > 0 THEN t2.MONEY_DUE ELSE 0 END >= t1.REDUCTIONMONEY + VALUE(OVER_MONEY.OVER_MONEY, 0) THEN '1' ELSE '0' END FRMID, ");
            stb.append("    CASE WHEN CASE WHEN RS.REDUCTIONMONEY > 0 THEN t2.MONEY_DUE ELSE 0 END >= t1.REDUCTIONMONEY + VALUE(OVER_MONEY.OVER_MONEY, 0) ");
            stb.append("         THEN CASE WHEN RS.REDUCTIONMONEY > 0 THEN t2.MONEY_DUE ELSE 0 END - (t1.REDUCTIONMONEY + VALUE(OVER_MONEY.OVER_MONEY, 0)) ");
            stb.append("         ELSE (t1.REDUCTIONMONEY + VALUE(OVER_MONEY.OVER_MONEY, 0)) - CASE WHEN RS.REDUCTIONMONEY > 0 THEN t2.MONEY_DUE ELSE 0 END END MONEY ");
        }
        stb.append("FROM ");
        stb.append("    SCHREG_DAT ");
        stb.append("    LEFT JOIN MONEY_SUM t1 ON t1.SCHREGNO = SCHREG_DAT.SCHREGNO ");
        stb.append("    LEFT JOIN MONEY_DUE_M_DAT t2 ON t2.YEAR = '" + year + "' ");
        stb.append("    AND t2.SCHREGNO = SCHREG_DAT.SCHREGNO ");
        stb.append("    AND t2.EXPENSE_M_CD = '" + taisyouMcd + "' ");
        if (month3Flg) {
            stb.append("    LEFT JOIN OVER_MONEY ON OVER_MONEY.SCHREGNO = SCHREG_DAT.SCHREGNO ");
            stb.append("    LEFT JOIN REDUCTION_DAT_SUM RS ON RS.SCHREGNO = SCHREG_DAT.SCHREGNO ");
        }
        stb.append("    LEFT JOIN MINOU_SCH ON MINOU_SCH.SCHREGNO = SCHREG_DAT.SCHREGNO ");
        stb.append("WHERE ");
        if (isMinou) {
            stb.append("    MINOU_SCH.SCHREGNO IS NOT NULL ");
        } else {
            stb.append("    MINOU_SCH.SCHREGNO IS NULL ");
        }
        stb.append(") ");
        stb.append("SELECT ");
        stb.append("    FRMID, ");
        stb.append("    COUNT(*) AS TOTALCNT, ");
        stb.append("    SUM(MONEY) AS TOTALMONEY ");
        stb.append("FROM ");
        stb.append("    MONEYTBL ");
        stb.append("WHERE ");
        stb.append("    FRMID = '0' ");
        stb.append("GROUP BY ");
        stb.append("    FRMID ");

//      log.debug(stb);
        return stb.toString();

    }

    /** 総ページ数を抽出 */
    //  CSOFF: ExecutableStatementCount
    private String totalpageSql(
            final String year,
            final String yearMonth,
            final String semester,
            final String var1,
            final String unprint,
            final boolean isMinou,
            final boolean month3Flg,
            final String taisyouMcd
    ) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH REDUCTION_DAT_SUM AS ( ");
        stb.append(" SELECT ");
        stb.append("     t1.YEAR, ");
        stb.append("     t1.SCHREGNO, ");
        stb.append("     SUM(CASE WHEN REDUC_DEC_FLG_1 = '1' ");
        stb.append("              THEN t1.REDUCTIONMONEY_1 ");
        stb.append("              ELSE 0 ");
        stb.append("         END ");
        stb.append("         + ");
        stb.append("         CASE WHEN REDUC_DEC_FLG_2 = '1' ");
        stb.append("              THEN t1.REDUCTIONMONEY_2 ");
        stb.append("              ELSE 0 ");
        stb.append("         END ");
        stb.append("     ) AS REDUCTIONMONEY ");
        stb.append(" FROM ");
        stb.append("     REDUCTION_DAT t1 ");
        stb.append(" WHERE ");
        stb.append("    t1.YEAR = '" + year + "' ");
        stb.append("    AND t1.SCHREGNO " + unprint + " ");
        stb.append("    AND (REDUC_DEC_FLG_1 = '1' ");
        stb.append("         OR ");
        stb.append("         REDUC_DEC_FLG_2 = '1') ");
        stb.append("    AND  VALUE(OFFSET_FLG, '0') = '1' AND VALUE(LOCK_FLG, '0') <> '1' ");
        stb.append(" GROUP BY ");
        stb.append("     t1.YEAR, ");
        stb.append("     t1.SCHREGNO ");
        stb.append(" ), REDUCTION_COUNTRY_PLAN_DAT_SUM AS ( ");
        stb.append(" SELECT  ");
        stb.append("     T1.YEAR, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     SUM((CASE WHEN VALUE(T1.PAID_YEARMONTH, '0') = '" + yearMonth + "' AND VALUE(T1.PLAN_CANCEL_FLG, '0') = '0' AND VALUE(T1.PLAN_LOCK_FLG, '0') = '0' THEN T1.PLAN_MONEY ");
        stb.append("         ELSE 0 END) ");
        stb.append("     + (CASE WHEN VALUE(T1.ADD_PAID_YEARMONTH, '0') = '" + yearMonth + "' AND VALUE(T1.ADD_PLAN_CANCEL_FLG, '0') = '0' AND VALUE(T1.ADD_PLAN_LOCK_FLG, '0') = '0' THEN T1.ADD_PLAN_MONEY ");
        stb.append("         ELSE 0 END)) AS PAID_PLAN_MONEY ");
        stb.append(" FROM ");
        stb.append("     REDUCTION_COUNTRY_PLAN_DAT T1 ");
        stb.append(" WHERE ");
        stb.append("     (VALUE(T1.PAID_YEARMONTH, '0') = '" + yearMonth + "' ");
        stb.append("      OR VALUE(T1.ADD_PAID_YEARMONTH, '0') = '" + yearMonth + "') ");
        stb.append(" GROUP BY ");
        stb.append("     T1.YEAR, ");
        stb.append("     T1.SCHREGNO ");
        stb.append(" ), MONEY_SUM AS ( ");
        stb.append(" SELECT  ");
        stb.append("     '" + year + "' AS YEAR, ");
        stb.append("     VALUE(T1.SCHREGNO, T2.SCHREGNO) AS SCHREGNO, ");
        stb.append("     VALUE(T1.REDUCTIONMONEY, 0) + VALUE(T2.PAID_PLAN_MONEY, 0) - VALUE(T3.TOTAL_ADJUSTMENT_MONEY,0) AS REDUCTIONMONEY ");
        stb.append(" FROM ");
        stb.append("     REDUCTION_DAT_SUM T1 ");
        stb.append("     FULL OUTER JOIN REDUCTION_COUNTRY_PLAN_DAT_SUM T2 ON T1.SCHREGNO = T2.SCHREGNO ");
        stb.append("         AND T1.YEAR = T2.YEAR ");
        stb.append("     LEFT JOIN REDUCTION_ADJUSTMENT_DAT T3 ON T3.YEAR = '" + year + "' ");
        stb.append("         AND T3.SCHREGNO = T1.SCHREGNO ");

        if (month3Flg) {
            stb.append(" ), MONEY_DUE_T AS ( ");
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     SUM(T1.MONEY_DUE) AS MONEY_DUE ");
            stb.append(" FROM ");
            stb.append("     MONEY_DUE_M_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("    T1.YEAR = '" + year + "' ");
            stb.append("    AND T1.EXPENSE_M_CD IN ('" + MCD11 + "', '" + MCD12 + "', '" + MCD13 + "') ");
            stb.append(" GROUP BY ");
            stb.append("     T1.SCHREGNO ");
            stb.append(" ), REDUC_T AS ( ");
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     SUM(CASE WHEN T1.REDUC_DEC_FLG_1 = '1' ");
            stb.append("              THEN VALUE(T1.REDUCTIONMONEY_1, 0) ");
            stb.append("              ELSE 0 ");
            stb.append("         END ");
            stb.append("         + ");
            stb.append("         CASE WHEN T1.REDUC_DEC_FLG_2 = '1' ");
            stb.append("              THEN VALUE(T1.REDUCTIONMONEY_2, 0) ");
            stb.append("              ELSE 0 ");
            stb.append("         END ");
            stb.append("     ) AS REDUCTIONMONEY ");
            stb.append(" FROM ");
            stb.append("     REDUCTION_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("    T1.YEAR = '" + year + "' ");
            stb.append("    AND (T1.REDUC_DEC_FLG_1 = '1' ");
            stb.append("         OR ");
            stb.append("         T1.REDUC_DEC_FLG_2 = '1') ");
            stb.append(" GROUP BY ");
            stb.append("     T1.SCHREGNO ");
            stb.append(" ), REDUC_COUNTRY_T AS ( ");
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     SUM(CASE WHEN VALUE(T1.PLAN_CANCEL_FLG, '0') = '0' ");
            stb.append("              THEN VALUE(T1.PLAN_MONEY, 0) ");
            stb.append("              ELSE 0 ");
            stb.append("         END ");
            stb.append("         + ");
            stb.append("         CASE WHEN VALUE(T1.ADD_PLAN_CANCEL_FLG, '0') = '0' ");
            stb.append("              THEN VALUE(T1.ADD_PLAN_MONEY, 0) ");
            stb.append("              ELSE 0 ");
            stb.append("         END ");
            stb.append("     ) AS REDUCTION_C_MONEY, ");
            stb.append("     SUM(CASE WHEN T1.PAID_YEARMONTH <= '" + year + "12" + "' AND VALUE(T1.PLAN_CANCEL_FLG, '0') = '0' ");
            stb.append("              THEN VALUE(T1.PLAN_MONEY, 0) ");
            stb.append("              ELSE 0 ");
            stb.append("         END ");
            stb.append("         + ");
            stb.append("         CASE WHEN T1.ADD_PAID_YEARMONTH <= '" + year + "12" + "' AND VALUE(T1.ADD_PLAN_CANCEL_FLG, '0') = '0' ");
            stb.append("              THEN VALUE(T1.ADD_PLAN_MONEY, 0) ");
            stb.append("              ELSE 0 ");
            stb.append("         END ");
            stb.append("     ) AS REDUCTION_C12_MONEY ");
            stb.append(" FROM ");
            stb.append("     REDUCTION_COUNTRY_PLAN_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("    T1.YEAR = '" + year + "' ");
            stb.append("    AND (VALUE(T1.PLAN_CANCEL_FLG, '0') = '0' ");
            stb.append("         OR ");
            stb.append("         VALUE(T1.ADD_PLAN_CANCEL_FLG, '0') = '0') ");
            stb.append(" GROUP BY ");
            stb.append("     T1.SCHREGNO ");
            stb.append(" ), ADJUST AS ( ");
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     VALUE(T1.MONEY_DUE, 0) AS TOTAL_LESSON_MONEY, ");
            stb.append("     VALUE(L1.REDUCTIONMONEY, 0) AS REDUCTIONMONEY, ");
            stb.append("     VALUE(L2.REDUCTION_C_MONEY, 0) AS REDUCTION_COUNTRY_MONEY, ");
            stb.append("     VALUE(L2.REDUCTION_C12_MONEY, 0) AS REDUCTION_C12_MONEY ");
            stb.append(" FROM ");
            stb.append("     MONEY_DUE_T T1 ");
            stb.append("     LEFT JOIN REDUC_T L1 ON T1.SCHREGNO = L1.SCHREGNO ");
            stb.append("     LEFT JOIN REDUC_COUNTRY_T L2 ON T1.SCHREGNO = L2.SCHREGNO ");
            stb.append(" WHERE ");
            stb.append("     VALUE(L1.REDUCTIONMONEY, 0) + VALUE(L2.REDUCTION_C_MONEY, 0) < VALUE(T1.MONEY_DUE, 0) ");

            stb.append(" ), OVER_MONEY AS ( ");
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     VALUE(T1.TOTAL_BURDEN_CHARGE, 0) AS OVER_MONEY ");
            stb.append(" FROM ");
            stb.append("     REDUCTION_BURDEN_CHARGE_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + year + "' ");
            stb.append("     AND EXISTS( ");
            stb.append("          SELECT ");
            stb.append("              'x' ");
            stb.append("          FROM ");
            stb.append("              MONEY_SUM E1 ");
            stb.append("          WHERE ");
            stb.append("             T1.SCHREGNO = E1.SCHREGNO ");
            stb.append("     ) ");
        }
        stb.append(" ), SCHREG_DAT AS ( ");
        stb.append(" SELECT DISTINCT ");
        stb.append("     T1.SCHREGNO ");
        stb.append(" FROM ");
        stb.append("     MONEY_SUM T1 ");
        if (month3Flg) {
            stb.append(" UNION ");
            stb.append(" SELECT DISTINCT ");
            stb.append("     T1.SCHREGNO ");
            stb.append(" FROM ");
            stb.append("     OVER_MONEY T1 ");
        }

        stb.append(" ), MINOU_SCH AS ( ");
        stb.append(" SELECT DISTINCT ");
        stb.append("     T1.SCHREGNO ");
        stb.append(" FROM ");
        stb.append("     MONEY_DUE_M_DAT T1 ");
        stb.append("     LEFT JOIN MONEY_PAID_M_DAT L1 ON T1.YEAR = L1.YEAR ");
        stb.append("          AND T1.SCHREGNO = L1.SCHREGNO ");
        stb.append("          AND T1.EXPENSE_M_CD = L1.EXPENSE_M_CD ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + year + "' ");
        stb.append("     AND T1.EXPENSE_M_CD = '" + MCD11 + "' ");
        stb.append("     AND (L1.SCHREGNO IS NULL ");
        stb.append("          OR ");
        stb.append("          VALUE(L1.PAID_MONEY, 0) <= 0) ");
        stb.append(" UNION   ");
        stb.append(" SELECT DISTINCT ");
        stb.append("     T1.SCHREGNO ");
        stb.append(" FROM ");
        stb.append("     MONEY_DUE_M_DAT T1 ");
        stb.append("     LEFT JOIN MONEY_PAID_M_DAT L1 ON T1.YEAR = L1.YEAR ");
        stb.append("          AND T1.SCHREGNO = L1.SCHREGNO ");
        stb.append("          AND T1.EXPENSE_M_CD = L1.EXPENSE_M_CD ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + year + "' ");
        stb.append("     AND T1.EXPENSE_M_CD = '" + MCD12 + "' ");
        stb.append("     AND (L1.SCHREGNO IS NULL ");
        stb.append("          OR ");
        stb.append("          VALUE(L1.PAID_MONEY, 0) <= 0) ");
        if (month3Flg) {
            stb.append(" UNION   ");
            stb.append(" SELECT DISTINCT ");
            stb.append("     T1.SCHREGNO ");
            stb.append(" FROM ");
            stb.append("     ADJUST T1 ");
            stb.append("     LEFT JOIN (SELECT ");
            stb.append("                    LL1.SCHREGNO, ");
            stb.append("                    SUM(VALUE(LL1.PAID_MONEY, 0)) AS PAID_MONEY ");
            stb.append("                FROM ");
            stb.append("                    MONEY_PAID_M_DAT LL1 ");
            stb.append("                WHERE ");
            stb.append("                    LL1.YEAR = '" + year + "' ");
            stb.append("                    AND LL1.EXPENSE_M_CD IN ('" + MCD11 + "', '" + MCD12 + "', '" + MCD13 + "') ");
            stb.append("                GROUP BY ");
            stb.append("                    LL1.SCHREGNO ");
            stb.append("                ) L1 ON T1.SCHREGNO = L1.SCHREGNO ");
            stb.append(" WHERE ");
            stb.append("     T1.TOTAL_LESSON_MONEY > L1.PAID_MONEY + T1.REDUCTIONMONEY + T1.REDUCTION_C12_MONEY ");
        }
        stb.append("), MONEYTBL AS ( ");
        stb.append("SELECT ");
        stb.append("    t2.GRADE || t2.HR_CLASS || ATTENDNO AS GRD_CLS_ATD, ");
        stb.append("    t3.BANKCD, ");
        stb.append("    t4.BANKNAME_KANA, ");
        stb.append("    t3.BRANCHCD, ");
        stb.append("    t4.BRANCHNAME_KANA, ");
        stb.append("    t3.DEPOSIT_ITEM, ");
        stb.append("    t5.ABBV1, ");
        stb.append("    t3.ACCOUNTNO, ");
        stb.append("    t3.ACCOUNTNAME, ");

        stb.append("    t1.REDUCTIONMONEY, ");
        if (!month3Flg) {
            stb.append("    CASE WHEN t6.MONEY_DUE >= t1.REDUCTIONMONEY THEN '1' ELSE '0' END FRMID, ");
            stb.append("    CASE WHEN t6.MONEY_DUE >= t1.REDUCTIONMONEY ");
            stb.append("         THEN t6.MONEY_DUE - t1.REDUCTIONMONEY ");
            stb.append("         ELSE t1.REDUCTIONMONEY - t6.MONEY_DUE END MONEY ");
        } else {
            stb.append("    CASE WHEN CASE WHEN RS.REDUCTIONMONEY > 0 THEN t6.MONEY_DUE ELSE 0 END >= t1.REDUCTIONMONEY + VALUE(OVER_MONEY.OVER_MONEY, 0) THEN '1' ELSE '0' END FRMID, ");
            stb.append("    CASE WHEN CASE WHEN RS.REDUCTIONMONEY > 0 THEN t6.MONEY_DUE ELSE 0 END >= t1.REDUCTIONMONEY + VALUE(OVER_MONEY.OVER_MONEY, 0) ");
            stb.append("         THEN CASE WHEN RS.REDUCTIONMONEY > 0 THEN t6.MONEY_DUE ELSE 0 END - (t1.REDUCTIONMONEY + VALUE(OVER_MONEY.OVER_MONEY, 0)) ");
            stb.append("         ELSE (t1.REDUCTIONMONEY + VALUE(OVER_MONEY.OVER_MONEY, 0)) - CASE WHEN RS.REDUCTIONMONEY > 0 THEN t6.MONEY_DUE ELSE 0 END END MONEY ");
        }
        stb.append("FROM ");
        stb.append("    SCHREG_DAT ");
        stb.append("    LEFT JOIN MONEY_SUM t1 ON t1.SCHREGNO = SCHREG_DAT.SCHREGNO ");
        stb.append("    LEFT JOIN SCHREG_REGD_DAT t2 ON t2.SCHREGNO = SCHREG_DAT.SCHREGNO ");
        stb.append("    AND t2.YEAR = '" + year + "' ");
        stb.append("    AND t2.SEMESTER = '" + semester + "' ");
        stb.append("    LEFT JOIN REGISTBANK_DAT t3 ON t3.SCHREGNO = SCHREG_DAT.SCHREGNO ");
        stb.append("    LEFT JOIN BANK_MST t4 ON t4.BANKCD = t3.BANKCD ");
        stb.append("    AND t4.BRANCHCD = t3.BRANCHCD ");
        stb.append("    LEFT JOIN NAME_MST t5 ON t5.NAMECD1 = 'G203' ");
        stb.append("    AND t5.NAMECD2 = t3.DEPOSIT_ITEM ");
        stb.append("    LEFT JOIN MONEY_DUE_M_DAT t6 ON t6.YEAR = '" + year + "' ");
        stb.append("    AND t6.SCHREGNO = SCHREG_DAT.SCHREGNO ");
        stb.append("    AND t6.EXPENSE_M_CD = '" + taisyouMcd + "' ");
        if (month3Flg) {
            stb.append("    LEFT JOIN OVER_MONEY ON OVER_MONEY.SCHREGNO = SCHREG_DAT.SCHREGNO ");
            stb.append("    LEFT JOIN REDUCTION_DAT_SUM RS ON RS.SCHREGNO = SCHREG_DAT.SCHREGNO ");
        }
        stb.append("    LEFT JOIN MINOU_SCH ON MINOU_SCH.SCHREGNO = SCHREG_DAT.SCHREGNO ");
        stb.append("WHERE ");
        if (isMinou) {
            stb.append("    MINOU_SCH.SCHREGNO IS NOT NULL ");
        } else {
            stb.append("    MINOU_SCH.SCHREGNO IS NULL ");
        }
        stb.append(") ");
        stb.append("SELECT ");
        stb.append("    CASE WHEN 0 < MOD(COUNT(*),15) THEN COUNT(*)/15 + 1 ELSE COUNT(*)/15 END TOTAL_PAGE ");
        stb.append("FROM ");
        stb.append("    MONEYTBL ");
        stb.append("WHERE ");
        stb.append("    FRMID = '0' ");
        if (!isMinou) {
            stb.append("    AND BANKCD = '" + var1 + "' ");
            stb.append("UNION ALL ");
            stb.append("SELECT ");
            stb.append("    CASE WHEN 0 < MOD(COUNT(*),15) THEN COUNT(*)/15 + 1 ELSE COUNT(*)/15 END TOTAL_PAGE ");
            stb.append("FROM ");
            stb.append("    MONEYTBL ");
            stb.append("WHERE ");
            stb.append("    FRMID = '0' ");
            stb.append("    AND (BANKCD < '" + var1 + "' OR BANKCD > '" + var1 + "') ");
        }
//      log.debug(stb);
        return stb.toString();

    }
    //  CSON: ExecutableStatementCount

    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        dumpParam(request, param);
        return param;
    }

    /** パラメータダンプ */
    private void dumpParam(final HttpServletRequest request, final Param param) {
        log.fatal("$Revision: 56595 $"); // CVSキーワードの取り扱いに注意
        if (log.isDebugEnabled()) {
            final Enumeration enums = request.getParameterNames();
            while (enums.hasMoreElements()) {
                final String name = (String) enums.nextElement();
                final String[] values = request.getParameterValues(name);
                log.debug("parameter:name=" + name + ", value=[" + StringUtils.join(values, ',') + "]");
            }
        }
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _semester;
        private final String _staffCd;
        private final String _staffName;
        private final String _date;
        private final String _gengou;
        private final StringBuffer _unprint = new StringBuffer();
        private final String _schoolName;
        private final String _schoolTel;
        private final String _bankName;
        private final String _branchName;
        private final String _var1;
        private final List _pageInfo = new ArrayList();
        private final String _yearMonth;
        private final boolean _month3Flg;
        private final String _taisyouMcd;
        private final String _aqccount;

        Param(final DB2UDB db2, final HttpServletRequest request) throws Exception {

            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _staffCd = request.getParameter("STAFF");
            _date  = request.getParameter("DATE");
            _yearMonth = request.getParameter("YEAR_MONTH");
            _month3Flg = "03".equals(_yearMonth.substring(4)) ? true : false;
            _taisyouMcd = "09".equals(_yearMonth.substring(4)) ? MCD12 : MCD13;

            String[] schreg;
            String[] checksch;
            checksch = request.getParameterValues("category_selected");
            if (null == checksch) {
                _unprint.append("NOT IN ('')");
            } else {
                schreg = request.getParameterValues("category_selected");    //印刷対象外
                _unprint.append("NOT IN (");
                String com = "";
                for (int sccnt = 0; sccnt < schreg.length; sccnt++) {
                    _unprint.append(com + "'" + schreg[sccnt].substring(0, schreg[sccnt].indexOf(":")) + "'");
                    com = ",";
                }
                _unprint.append(")");
            }

            // DBより取得
            //担当者の取得
            final String staffSql = "SELECT STAFFNAME FROM STAFF_MST WHERE STAFFCD = '" + _staffCd + "' ";
            db2.query(staffSql);
            final ResultSet staffRs = db2.getResultSet();
            String staff = "";
            while (staffRs.next()) {
                staff = staffRs.getString("STAFFNAME");
            }
            _staffName = staff;
            staffRs.close();
            db2.commit();

            //作成日
            _gengou = KNJ_EditDate.h_format_JP(_date);

            //学校名の取得
            final String schoolSql = "SELECT SCHOOLNAME1,SCHOOLTELNO FROM SCHOOL_MST WHERE YEAR = '" + _year + "' ";
            db2.query(schoolSql);
            final ResultSet schoolRs = db2.getResultSet();
            String schoolName = "";
            String schoolTel = "";
            while (schoolRs.next()) {
                schoolName = schoolRs.getString("SCHOOLNAME1");
                schoolTel = schoolRs.getString("SCHOOLTELNO");
            }
            _schoolName = schoolName;
            _schoolTel = schoolTel;
            schoolRs.close();
            db2.commit();

            //銀行名の取得
            final String bankSql = "SELECT VAR1,VAR2,BANKNAME,BRANCHNAME FROM SCHOOL_EXPENSES_SYS_INI "
                                + "LEFT JOIN BANK_MST ON BANKCD = VAR1 AND BRANCHCD = VAR2 WHERE PROGRAMID = 'BANK' AND DIV = '0001' ";
            db2.query(bankSql);
            final ResultSet bankRs = db2.getResultSet();
            String bankName = "";
            String branchName = "";
            String var1 = "";
            while (bankRs.next()) {
                bankName = bankRs.getString("BANKNAME");
                branchName = bankRs.getString("BRANCHNAME");
                var1 = bankRs.getString("VAR1");
            }
            _bankName = bankName;
            _branchName = branchName;
            _var1 = var1;
            bankRs.close();
            db2.commit();

            final String aqccountSql = "SELECT VAR1 FROM SCHOOL_EXPENSES_SYS_INI WHERE PROGRAMID = 'BANK' AND DIV = '0002' ";
            db2.query(aqccountSql);
            final ResultSet aqccountRs = db2.getResultSet();
            String aqccount = "";
            while (aqccountRs.next()) {
                aqccount = aqccountRs.getString("VAR1");
            }
            _aqccount = aqccount;
            aqccountRs.close();
            db2.commit();

            _pageInfo.add(new PageInfo(db2, _year, _yearMonth, _semester, _var1, _unprint.toString(), false, _month3Flg, _taisyouMcd));
            _pageInfo.add(new PageInfo(db2, _year, _yearMonth, _semester, _var1, _unprint.toString(), true, _month3Flg, _taisyouMcd));
        }
    }

    /** ページ情報 */
    private class PageInfo {
        private final boolean _isMinou;
        private final int _totalPage;
        private final String _totalCnt;
        private final String _totalMoney;
        private final String _title;
        private final String _formName;

        public PageInfo(
                final DB2UDB db2,
                final String year,
                final String yearMonth,
                final String semester,
                final String var1,
                final String unprint,
                final boolean isMinou,
                final boolean month3Flg,
                final String taisyouMcd
        ) throws Exception  {

            _isMinou = isMinou;
            if (_isMinou) {
                _title = "授業料未納者";
                _formName = "KNJP370.frm";
            } else {
                _title = "振込依頼書(連記式)";
                _formName = "KNJP370_3.frm";
            }
            //総頁の取得
            db2.query(totalpageSql(year, yearMonth, semester, var1, unprint, isMinou, month3Flg, taisyouMcd));
            final ResultSet totalPageRs = db2.getResultSet();
            int totalp = 0;
            while (totalPageRs.next()) {
                totalp = totalp + totalPageRs.getInt("TOTAL_PAGE");
            }
            _totalPage = totalp;
            totalPageRs.close();
            db2.commit();

            //合計の取得
            db2.query(totalMoneySql(year, yearMonth, unprint.toString(), isMinou, month3Flg, taisyouMcd));
            final ResultSet totalRs = db2.getResultSet();
            String totalCnt = "";
            String totalMoney = "";
            while (totalRs.next()) {
                totalCnt = totalRs.getString("TOTALCNT");
                totalMoney = totalRs.getString("TOTALMONEY");
            }
            _totalCnt = totalCnt;
            _totalMoney = totalMoney;
            totalRs.close();
            db2.commit();
        }

    }
}
