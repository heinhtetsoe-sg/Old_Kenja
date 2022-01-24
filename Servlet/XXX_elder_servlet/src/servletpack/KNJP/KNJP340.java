// kanji=漢字
/*
 * $Id: 26aa538c960448e81783ba988410c6c743342dc6 $
 *
 * 作成日: 2005/11/21
 * 作成者: m-yama
 *
 * Copyright(C) 2005-2008 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJP;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_Get_Info;

/**
 *
 *  学校教育システム 賢者 [入試管理]
 *
 *                  ＜ＫＮＪＰ３４０＞  授業料軽減報告票
 *
 *  2005/11/21 m-yama 作成
 *  2005/11/24 m-yama NO001 抽出条件に軽減額ありのみを追加
 *  2005/11/27 m-yama NO002 出力項目を追加
 *  2005/11/27 m-yama NO003 抽出条件に全てを追加
 *  2005/11/27 m-yama NO004 抽出条件にBは除外を追加
 *  2006/01/21 m-yama NO005 合計欄を追加
 */

public class KNJP340 {


    private static final Log log = LogFactory.getLog(KNJP340.class);

    boolean _isKuniOnly;
    boolean _isFukenOnly;
    boolean _isRyohou;
    boolean _month3Flg;
    boolean _isFukenBurden;
    boolean _isRyohouBurden;

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws ServletException, IOException
    {

        Vrw32alp svf = new Vrw32alp();          //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                      //Databaseクラスを継承したクラス
        String param[] = new String[11];        //NO002

        String selected[] = request.getParameterValues("CLASS_SELECTED");   //印字対象

        //SQL作成時のIN文の作成
        StringBuffer sql = new StringBuffer();
        sql.append(" ( ");

        String comma = "";
        for (int i = 0; i < selected.length; i++) {
            sql.append(comma);
            sql.append("'").append(selected[i]).append("'");
            comma = ",";
        }
        sql.append(")");

        //パラメータの取得
        try {
            param[0]  = request.getParameter("YEAR");           //年度
            param[1]  = request.getParameter("SEMESTER");       //学期
            param[2]  = request.getParameter("OUTPUT");         //出力対象 1:軽減対象者,2:軽減決定者のみ,3:全て
            param[3]  = request.getParameter("PRINT_PREF");     //所得・都道府県 印字する。
            param[6]  = request.getParameter("GRD_DATE");       //移動日
            param[7]  = request.getParameter("OUT_DIV");        //帳票種別 1:国 2:府県 3:合計
            param[9]  = request.getParameter("PAID_YEARMONTH"); //相殺年月
            _isKuniOnly   = ("1".equals(param[7])) ? true : false;
            _isFukenOnly  = ("2".equals(param[7])) ? true : false;
            _isRyohou     = ("3".equals(param[7])) ? true : false;
            _month3Flg    = null != param[9] && "03".equals(param[9].substring(4, 6)) ? true : false;
            _isFukenBurden  = ("1".equals(request.getParameter("FUKEN_BURDEN"))) ? true : false;
            _isRyohouBurden = ("1".equals(request.getParameter("RYOHOU_BURDEN"))) ? true : false;
        } catch( Exception ex ) {
            log.warn("parameter error!",ex);
        }

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


    //  ＳＶＦ作成処理
        boolean nonedata = false;                               //該当データなしフラグ

        getHeaderData(db2,svf,param);                           //ヘッダーデータ抽出メソッド

for(int ia=0 ; ia<param.length ; ia++) log.debug("param["+ia+"]="+param[ia]);

        //SVF出力
log.debug("SVF syuturyoku!!!");
        if( printMain(db2,svf,param,sql) ) nonedata = true;

log.debug("nonedata="+nonedata);

    //  該当データ無し
        if( !nonedata ){
            svf.VrSetForm("MES001.frm", 0);
            svf.VrsOut("note" , "");
            svf.VrEndPage();
        }

    //  終了処理
        svf.VrQuit();
        db2.commit();
        db2.close();                //DBを閉じる
        outstrm.close();            //ストリームを閉じる

    }//doGetの括り


    /**ヘッダーデータを抽出*/
    private void getHeaderData(DB2UDB db2,Vrw32alp svf,String param[]){

        svf.VrSetForm("KNJP340.frm", 1);
    //  作成日
        try {
            KNJ_Get_Info getinfo = new KNJ_Get_Info();
            KNJ_Get_Info.ReturnVal returnval = null;
            returnval = getinfo.Control(db2);
            param[4] = KNJ_EditDate.h_format_JP(db2, returnval.val3);
            getinfo = null;
            returnval = null;
        } catch( Exception e ){
            log.warn("ctrl_date get error!",e);
        }
log.debug("heda OK!");
        //年度
        try {
            param[5] = KNJ_EditDate.gengou(db2, Integer.parseInt(param[0])) + "年度";
        } catch( Exception e ){
            log.warn("jinendo get error!",e);
        }

    }//getHeaderData()の括り


    /**印刷処理メイン*/
    private boolean printMain(DB2UDB db2,Vrw32alp svf,String param[],StringBuffer sql)
    {
        boolean nonedata = false;
        int gyo   = 1;
        int total = 0;
        int totalcnt  = 0;  //NO001
        String bifchangepage = "";
        try {
log.debug("printMain start!");

            db2.query(meisaiSql(param,sql));
            ResultSet rs = db2.getResultSet();

log.debug("printMain end!");

            while( rs.next() ){
                //１ページ印刷
                if (50 < gyo) {
                    svf.VrEndPage();
                    gyo = 1;
                }else if (gyo > 1 && !bifchangepage.equalsIgnoreCase(rs.getString("CHANGEPAGE"))){
                    svf.VrsOut("TOTALCNT"         , String.valueOf(totalcnt) );   //NO001
                    svf.VrsOut("TOTALMONEY"       , String.valueOf(total) );      //NO001
                    svf.VrEndPage();
                    total    = 0;
                    totalcnt = 0;   //NO001
                    gyo = 1;
                }

                if (_isFukenBurden || _isRyohouBurden) {
                    total = total + rs.getInt("OVER_MONEY");
                }
                //明細データをセット
                printMeisai(svf, param, rs, gyo);
                nonedata = true;

                gyo++;
                totalcnt++; //NO001
                total = total + rs.getInt("REDUCTIONMONEY");
                bifchangepage = rs.getString("CHANGEPAGE");

            }

            //最終ページ印刷
            if (nonedata) {
                if (50 < gyo) gyo = 1;
                svf.VrsOut("TOTALCNT"         , String.valueOf(totalcnt) );   //NO001
                svf.VrsOut("TOTALMONEY"       , String.valueOf(total) );      //NO001
                svf.VrEndPage();
            }

            rs.close();
            db2.commit();
        } catch( Exception ex ) {
            log.warn("printMain read error!",ex);
        }
        return nonedata;

    }//printMain()の括り

    /**明細データをセット*/
    private void printMeisai(Vrw32alp svf,String param[],ResultSet rs,int gyo)
    {
        try {
            if (_isKuniOnly) {
                svf.VrsOut("SUBTITLE"     , "国就学支援金" );
            } else if (_isFukenOnly) {
                svf.VrsOut("SUBTITLE"     , "府県補助金" );
            } else {
                svf.VrsOut("SUBTITLE"     , "国府県合計" );
            }

            svf.VrsOut("NENDO"        , param[5] );
            svf.VrsOut("DATE"         , param[4] );
            svf.VrsOut("HR_NAME"      , rs.getString("HR_NAME") );

            //NO002↓
            svf.VrsOutn("SLIPNO"      ,gyo    , "29" );
            svf.VrsOutn("SCHOOLCD"    ,gyo    , "0245" );
            svf.VrsOutn("DIVISION"    ,gyo    , "6" );
            svf.VrsOutn("MAJOR"       ,gyo    , rs.getString("BANK_MAJORCD") );
            svf.VrsOutn("GRADE"       ,gyo    , rs.getString("GRADE") );
            svf.VrsOutn("HR_CLASS"    ,gyo    , rs.getString("BANK_HR_CLASS") );
            //NO002↑

            svf.VrsOutn("SCHREGNO"    ,gyo    , rs.getString("SCHREGNO") );
            svf.VrsOutn("ATTENDNO"    ,gyo    , rs.getString("ATTENDNO") );
            svf.VrsOutn("NAME"        ,gyo    , rs.getString("NAME") );

            //NO002↓
            svf.VrsOutn("PROCESS"     ,gyo    , "2" );
            svf.VrsOutn("EXPENSE"     ,gyo    , "01" );
            svf.VrsOutn("REDU_WAY"    ,gyo    , "1" );
            String reduStart = "03";
//            String reduStart = _isFukenOnly ? "04" : "";
//            if (null != param[9]) {
//                String month = param[9].substring(4, 6);
//                if ("04".equals(month))
//                    reduStart = "01";
//                if ("09".equals(month))
//                    reduStart = "02";
//                if ("12".equals(month))
//                    reduStart = "04";
//                if ("03".equals(month))
//                    reduStart = "";
//            }
            svf.VrsOutn("REDU_START", gyo, reduStart);
            //NO002↑

            if (rs.getString("REDUC_DEC_FLG") != null && rs.getString("REDUC_DEC_FLG").equals("1")){
                svf.VrsOutn("MARK",gyo    , "*" );
            }

            //調整金があれば網掛け
            if (rs.getString("ADJUSTMENT_MONEY") != null && _isFukenOnly && "3".equals(param[2]) && "1".equals(rs.getString("OFFSET_LOCK_FLG"))) {
                svf.VrAttributen("AID"     ,gyo    , "Paint=(1,70,1),Bold=1" );
            }
            int reducMoney = rs.getInt("REDUCTIONMONEY");
            if (_isFukenBurden || _isRyohouBurden) {
                reducMoney = reducMoney + rs.getInt("OVER_MONEY");
            }
            svf.VrsOutn("AID", gyo, String.valueOf(reducMoney));
            if (rs.getString("ADJUSTMENT_MONEY") != null && _isFukenOnly && "3".equals(param[2]) && "1".equals(rs.getString("OFFSET_LOCK_FLG"))) {
                svf.VrAttributen("AID"     ,gyo    , "Paint=(0,0,0),Bold=0" );
            }

            if (param[3] != null) {
                svf.VrsOutn("PREFECTURE"  ,gyo    , rs.getString("PREF") );
            }

        } catch( Exception ex ) {
            log.warn("printMeisai read error!",ex);
        }

    }//printMeisai()の括り

    /**
     *  クラス別軽減データを抽出
     *
     */
    private String meisaiSql(String param[],StringBuffer sql)
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append(" WITH T_SCHREGNO AS ( ");
            stb.append("SELECT ");
            stb.append("    t2.YEAR, ");
            stb.append("    t2.SEMESTER, ");
            stb.append("    t2.SCHREGNO, ");
            stb.append("    t2.GRADE, ");
            stb.append("    t2.HR_CLASS, ");
            stb.append("    t2.ATTENDNO ");
            stb.append("FROM ");
            stb.append("    SCHREG_REGD_DAT t2 ");
            stb.append("WHERE ");
            stb.append("        t2.YEAR = '"+param[0]+"' ");
            stb.append("    AND t2.SEMESTER = '"+param[1]+"' ");
            stb.append("    AND t2.GRADE || t2.HR_CLASS IN "+sql.toString()+" ");
            stb.append(" ), GRANT_T AS ( ");
            stb.append(" SELECT ");
            stb.append("     SCHREGNO, ");
            stb.append("     SUM(VALUE(GRANT_MONEY, 0)) AS GRANT_MONEY ");
            stb.append(" FROM ");
            stb.append("     SCHREG_GRANT_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + param[0] + "' ");
            stb.append(" GROUP BY ");
            stb.append("     SCHREGNO ");
            stb.append(" ), MONEY_DUE_T AS ( ");
            stb.append(" SELECT ");
            stb.append("     SCHREGNO, ");
            stb.append("     SUM(MONEY_DUE) AS MONEY_DUE ");
            stb.append(" FROM ");
            stb.append("     MONEY_DUE_M_DAT ");
            stb.append(" WHERE ");
            stb.append("    YEAR = '" + param[0] + "' ");
            stb.append("    AND EXPENSE_M_CD IN ('11', '12', '13') ");
            stb.append(" GROUP BY ");
            stb.append("     SCHREGNO ");
            stb.append(" ), REDUC_T AS ( ");
            stb.append(" SELECT ");
            stb.append("     SCHREGNO, ");
            stb.append("     SUM(CASE WHEN REDUC_DEC_FLG_1 = '1' ");
            stb.append("              THEN VALUE(REDUCTIONMONEY_1, 0) ");
            stb.append("              ELSE 0 ");
            stb.append("         END ");
            stb.append("         + ");
            stb.append("         CASE WHEN REDUC_DEC_FLG_2 = '1' ");
            stb.append("              THEN VALUE(REDUCTIONMONEY_2, 0) ");
            stb.append("              ELSE 0 ");
            stb.append("         END ");
            stb.append("     ) AS REDUCTIONMONEY ");
            stb.append(" FROM ");
            stb.append("     REDUCTION_DAT ");
            stb.append(" WHERE ");
            stb.append("    YEAR = '" + param[0] + "' ");
            stb.append("    AND (REDUC_DEC_FLG_1 = '1' ");
            stb.append("         OR ");
            stb.append("         REDUC_DEC_FLG_2 = '1') ");
            stb.append(" GROUP BY ");
            stb.append("     SCHREGNO ");
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
            stb.append("     ) AS REDUCTION_C_MONEY ");
            stb.append(" FROM ");
            stb.append("     REDUCTION_COUNTRY_PLAN_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("    T1.YEAR = '" + param[0] + "' ");
            stb.append("    AND (VALUE(T1.PLAN_CANCEL_FLG, '0') = '0' ");
            stb.append("         OR ");
            stb.append("         VALUE(T1.ADD_PLAN_CANCEL_FLG, '0') = '0') ");
            stb.append(" GROUP BY ");
            stb.append("     T1.SCHREGNO ");
            stb.append(" ) ");

            if (_isFukenOnly || _isRyohou) {
                stb.append(" , REDUCTION_DAT_SUM AS ( ");
                stb.append(" SELECT ");
                stb.append("     t1.SCHREGNO, ");
                stb.append("     CASE WHEN t1.REDUC_DEC_FLG_1 = '1' OR t1.REDUC_DEC_FLG_2 = '1' ");
                stb.append("          THEN '1' ");
                stb.append("          ELSE '0' ");
                stb.append("     END AS REDUC_DEC_FLG, ");
                stb.append("     CASE WHEN t1.OFFSET_FLG = '1' AND value(t1.LOCK_FLG,'0') = '0' ");
                stb.append("          THEN '1' ");
                stb.append("          ELSE '0' ");
                stb.append("     END AS OFFSET_LOCK_FLG, ");
                if (param[2].equals("2")) {
                    stb.append("     SUM(CASE WHEN t1.REDUC_DEC_FLG_1 = '1' AND t1.REDUCTIONMONEY_1 is not null ");
                    stb.append("              THEN t1.REDUCTIONMONEY_1 ");
                    stb.append("              ELSE 0 ");
                    stb.append("         END ");
                    stb.append("         + ");
                    stb.append("         CASE WHEN t1.REDUC_DEC_FLG_2 = '1' AND t1.REDUCTIONMONEY_2 is not null ");
                    stb.append("              THEN t1.REDUCTIONMONEY_2 ");
                    stb.append("              ELSE 0 ");
                    stb.append("         END) ");
                } else if (param[2].equals("3")) {
                    stb.append("     SUM(CASE WHEN t1.REDUC_DEC_FLG_1 = '1' AND t1.REDUCTIONMONEY_1 is not null ");
                    stb.append("               AND t1.OFFSET_FLG = '1' AND value(t1.LOCK_FLG,'0') = '0' ");
                    stb.append("              THEN t1.REDUCTIONMONEY_1 ");
                    stb.append("              ELSE 0 ");
                    stb.append("         END ");
                    stb.append("         + ");
                    stb.append("         CASE WHEN t1.REDUC_DEC_FLG_2 = '1' AND t1.REDUCTIONMONEY_2 is not null ");
                    stb.append("               AND t1.OFFSET_FLG = '1' AND value(t1.LOCK_FLG,'0') = '0' ");
                    stb.append("              THEN t1.REDUCTIONMONEY_2 ");
                    stb.append("              ELSE 0 ");
                    stb.append("         END) ");
                } else {
                    stb.append("     SUM(CASE WHEN t1.REDUCTIONMONEY_1 is not null OR t1.REDUCTIONMONEY_2 is not null ");
                    stb.append("              THEN VALUE(t1.REDUCTIONMONEY_1,0) + VALUE(t1.REDUCTIONMONEY_2,0) ");
                    stb.append("              ELSE NULL ");
                    stb.append("         END) ");
                }
                stb.append("     AS REDUCTIONMONEY ");
                stb.append(" FROM ");
                stb.append("     REDUCTION_DAT t1 ");
                stb.append(" WHERE ");
                stb.append("    t1.YEAR = '"+param[0]+"' ");
                if (param[2].equals("2")) {
                    stb.append("    AND (t1.REDUC_DEC_FLG_1 = '1' ");
                    stb.append("         OR ");
                    stb.append("         t1.REDUC_DEC_FLG_2 = '1') ");
                }
                if (param[2].equals("3")) {
                    stb.append("    AND t1.OFFSET_FLG = '1' AND value(t1.LOCK_FLG,'0') = '0' ");
                    stb.append("    AND (t1.REDUC_DEC_FLG_1 = '1' ");
                    stb.append("         OR ");
                    stb.append("         t1.REDUC_DEC_FLG_2 = '1') ");
                }
                if (!param[2].equals("4")) {
                    stb.append("    AND NOT EXISTS (SELECT ");
                    stb.append("                        'x' ");
                    stb.append("                    FROM ");
                    stb.append("                        REDUCTION_DAT E1 ");
                    stb.append("                    WHERE ");
                    stb.append("                    E1.YEAR = '"+param[0]+"' AND ");
                    stb.append("                    E1.REDUC_RARE_CASE_CD = 'B' AND ");
                    stb.append("                    t1.SCHREGNO = E1.SCHREGNO) ");
                }
                stb.append(" GROUP BY ");
                stb.append("     t1.SCHREGNO, ");
                stb.append("     CASE WHEN t1.REDUC_DEC_FLG_1 = '1' OR t1.REDUC_DEC_FLG_2 = '1' ");
                stb.append("          THEN '1' ");
                stb.append("          ELSE '0' ");
                stb.append("     END, ");
                stb.append("     CASE WHEN t1.OFFSET_FLG = '1' AND value(t1.LOCK_FLG,'0') = '0' ");
                stb.append("          THEN '1' ");
                stb.append("          ELSE '0' ");
                stb.append("     END ");
                stb.append(" ) ");
            }

            if (_isKuniOnly || _isRyohou) {
                stb.append(" , PAID_MONEY_SUM AS ( ");
                stb.append("     SELECT ");
                stb.append("         t1.SCHREGNO, ");
                stb.append("         SUM(CASE WHEN t1.PAID_YEARMONTH = '"+param[9]+"' AND VALUE(PLAN_CANCEL_FLG, '0') = '0' ");
                stb.append("                  THEN t1.PAID_MONEY ");
                stb.append("                  ELSE 0 ");
                stb.append("             END ");
                stb.append("             + ");
                stb.append("             CASE WHEN t1.ADD_PAID_YEARMONTH = '"+param[9]+"' AND VALUE(ADD_PLAN_CANCEL_FLG, '0') = '0' ");
                stb.append("                  THEN t1.ADD_PAID_MONEY ");
                stb.append("                  ELSE 0 ");
                stb.append("             END) AS PAID_MONEY ");
                stb.append("     FROM ");
                stb.append("         REDUCTION_COUNTRY_PLAN_DAT t1 ");
                stb.append("     WHERE ");
                stb.append("         t1.YEAR='"+param[0]+"' ");
                stb.append("         AND ((t1.PAID_YEARMONTH = '"+param[9]+"' AND VALUE(PLAN_CANCEL_FLG, '0') = '0' AND VALUE(PLAN_LOCK_FLG, '0') = '0') ");
                stb.append("           OR (t1.ADD_PAID_YEARMONTH = '"+param[9]+"' AND VALUE(ADD_PLAN_CANCEL_FLG, '0') = '0' AND VALUE(ADD_PLAN_LOCK_FLG, '0') = '0')) ");
                stb.append("         AND NOT EXISTS (SELECT ");
                stb.append("                            'x' ");
                stb.append("                         FROM ");
                stb.append("                            REDUCTION_COUNTRY_DAT E1 ");
                stb.append("                         WHERE ");
                stb.append("                            E1.YEAR = '"+param[0]+"' ");
                stb.append("                            AND t1.SCHREGNO = E1.SCHREGNO ");
                stb.append("                            AND VALUE(E1.OFFSET_FLG, '0') = '0') ");
                stb.append("     GROUP BY ");
                stb.append("         t1.SCHREGNO ");
                stb.append(" ) ");
            }

            stb.append(" , MAIN_T AS ( ");
            stb.append("SELECT ");
            stb.append("    t2.SCHREGNO, ");
            stb.append("    t6.HR_NAME, ");
            stb.append("    t2.ATTENDNO, ");
            stb.append("    t2.GRADE || t2.HR_CLASS || t2.ATTENDNO AS GRD_CLASS, ");
            stb.append("    t2.GRADE || t2.HR_CLASS AS CHANGEPAGE, ");
            stb.append("    t4.NAME, ");
            stb.append("    t8.PREF, ");
            if (_isRyohou) {
                stb.append("    t1.REDUC_DEC_FLG, ");
                if (param[2].equals("3")) {
                    stb.append("    t1.OFFSET_LOCK_FLG, ");
                    stb.append("    CASE WHEN t1.OFFSET_LOCK_FLG = '1' ");
                    stb.append("         THEN value(tt1.PAID_MONEY, 0) + value(t1.REDUCTIONMONEY, 0) + value(GRANT_T.GRANT_MONEY, 0) - value(AD.TOTAL_ADJUSTMENT_MONEY, 0) ");
                    stb.append("         ELSE value(tt1.PAID_MONEY, 0) + value(t1.REDUCTIONMONEY, 0) + value(GRANT_T.GRANT_MONEY, 0) ");
                    stb.append("         END AS REDUCTIONMONEY, ");
                } else {
                    stb.append("    '' AS OFFSET_LOCK_FLG, ");
                    stb.append("    value(tt1.PAID_MONEY, 0) + value(t1.REDUCTIONMONEY, 0) + value(GRANT_T.GRANT_MONEY, 0) AS REDUCTIONMONEY, ");
                }
            } else if (_isKuniOnly) {
                stb.append("    '' AS REDUC_DEC_FLG, ");
                stb.append("    '' AS OFFSET_LOCK_FLG, ");
                stb.append("    VALUE(tt1.PAID_MONEY, 0) + value(GRANT_T.GRANT_MONEY, 0) AS REDUCTIONMONEY, ");
            } else {
                stb.append("    t1.REDUC_DEC_FLG, ");
                if (param[2].equals("3")) {
                    stb.append("    t1.OFFSET_LOCK_FLG, ");
                    stb.append("    CASE WHEN t1.OFFSET_LOCK_FLG = '1' ");
                    stb.append("         THEN value(t1.REDUCTIONMONEY,0) + value(GRANT_T.GRANT_MONEY, 0) - value(AD.TOTAL_ADJUSTMENT_MONEY,0) ");
                    stb.append("         ELSE value(t1.REDUCTIONMONEY,0) + value(GRANT_T.GRANT_MONEY, 0) ");
                    stb.append("         END AS REDUCTIONMONEY, ");
                } else {
                    stb.append("    '' AS OFFSET_LOCK_FLG, ");
                    stb.append("    value(t1.REDUCTIONMONEY, 0) + value(GRANT_T.GRANT_MONEY, 0) AS REDUCTIONMONEY, ");
                }
            }
            stb.append("    t2.GRADE, ");
            stb.append("    t7.BANK_HR_CLASS, ");
            stb.append("    t7.BANK_MAJORCD, ");
            stb.append("    AD.TOTAL_ADJUSTMENT_MONEY AS ADJUSTMENT_MONEY, ");
            stb.append("    VALUE(OVER_MONEY.TOTAL_BURDEN_CHARGE, 0) AS OVER_MONEY ");
            stb.append("FROM ");
            stb.append("    T_SCHREGNO t2 ");
            stb.append("    LEFT JOIN REDUCTION_ADJUSTMENT_DAT AD ON AD.YEAR = t2.YEAR AND AD.SCHREGNO = t2.SCHREGNO ");
            stb.append("    LEFT JOIN REDUCTION_BURDEN_CHARGE_DAT OVER_MONEY ON OVER_MONEY.YEAR = '" + param[0] + "' ");
            stb.append("         AND OVER_MONEY.SCHREGNO = t2.SCHREGNO ");
            if (_isKuniOnly || _isRyohou) {
                stb.append("    LEFT JOIN PAID_MONEY_SUM tt1 ON tt1.SCHREGNO = t2.SCHREGNO ");
            }
            if (_isFukenOnly || _isRyohou) {
                stb.append("    LEFT JOIN REDUCTION_DAT_SUM t1 ON t1.SCHREGNO = t2.SCHREGNO ");
            }
            stb.append("    LEFT JOIN GRANT_T GRANT_T ON GRANT_T.SCHREGNO = t2.SCHREGNO ");
            stb.append("    LEFT JOIN SCHREG_BASE_MST t4 ON t4.SCHREGNO = t2.SCHREGNO ");
            stb.append("    LEFT JOIN GUARDIAN_DAT t9 ON t9.SCHREGNO = t2.SCHREGNO ");
            stb.append("    LEFT JOIN ( ");
            stb.append("        SELECT ");
            stb.append("            ZIP.NEW_ZIPCD, ");
            stb.append("            MAX(ZIP.PREF) AS PREF ");
            stb.append("        FROM ");
            stb.append("            ZIPCD_MST ZIP ");
            stb.append("        GROUP BY ZIP.NEW_ZIPCD ");
            stb.append("    ) t8 ON t9.GUARANTOR_ZIPCD = t8.NEW_ZIPCD ");
            stb.append("    LEFT JOIN SCHREG_REGD_HDAT t6 ON t6.GRADE || t6.HR_CLASS = t2.GRADE || t2.HR_CLASS ");
            stb.append("    AND t6.YEAR = t2.YEAR ");
            stb.append("    AND t6.SEMESTER = t2.SEMESTER ");
            //NO002↓
            stb.append("    LEFT JOIN BANK_CLASS_MST t7 ON t7.GRADE || t7.HR_CLASS = t2.GRADE || t2.HR_CLASS ");
            stb.append("    AND t7.YEAR = t2.YEAR ");
            //NO002↑
            stb.append("WHERE ");
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
            stb.append("                  AND BASE.GRD_DATE <= '"+ StringUtils.replace(param[6], "/", "-") +"') BASE_GRD, ");
            stb.append("             (SELECT ");
            stb.append("                  TRANSFER.SCHREGNO ");
            stb.append("                  FROM ");
            stb.append("                      SCHREG_TRANSFER_DAT TRANSFER ");
            stb.append("                  WHERE ");
            stb.append("                      TRANSFER.TRANSFERCD IN ('2') ");
            stb.append("                      AND TRANSFER.TRANSFER_SDATE >= '" + param[0] + "-04-01' ");
            stb.append("                      AND TRANSFER.TRANSFER_SDATE <= '"+ StringUtils.replace(param[6], "/", "-") +"' ");
            stb.append("                  ) SCH_TRANS ");
            stb.append("         WHERE ");
            stb.append("             E1.YEAR = '" + param[0] + "' ");
            stb.append("             AND (E1.SCHREGNO = BASE_GRD.SCHREGNO OR E1.SCHREGNO = SCH_TRANS.SCHREGNO) ");
            stb.append("             AND t2.SCHREGNO = E1.SCHREGNO ");
            stb.append("     ) ");
            stb.append("    AND ( ");
            stb.append("    GRANT_T.SCHREGNO = t2.SCHREGNO ");
            if (_isKuniOnly || _isRyohou) {
                stb.append("    OR t2.SCHREGNO = tt1.SCHREGNO ");
            }
            if (_isFukenOnly || _isRyohou) {
                stb.append("    OR t2.SCHREGNO = t1.SCHREGNO ");
            }
            if (_month3Flg) {
                stb.append("    t2.SCHREGNO = OVER_MONEY.SCHREGNO ");
            }
            stb.append("    ) ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     * ");
            stb.append(" FROM ");
            stb.append("     MAIN_T ");
            stb.append(" WHERE ");
            stb.append("     REDUCTIONMONEY + OVER_MONEY > 0 ");
            stb.append(" ORDER BY ");
            stb.append("     GRD_CLASS ");

log.debug(stb);
        } catch( Exception e ){
            log.warn("meisaiSql error!",e);
        }
        return stb.toString();

    }//meisaiSql()の括り

}//クラスの括り
