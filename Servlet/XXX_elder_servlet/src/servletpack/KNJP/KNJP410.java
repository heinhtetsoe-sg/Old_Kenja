// kanji=漢字
/*
 * $Id: 11fd12b6c8eccb7a46d4588ecdecf6218518f65f $
 *
 * 作成日: 2006/03/30 11:43:43 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJP;

import java.io.IOException;
import java.io.PrintWriter;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_Get_Info;

/**
 *
 * 学校教育システム 賢者 [入試管理]
 *
 *                  ＜ＫＮＪＰ４１０＞  授業料納入台帳
 *
 *  2006/03/14 m-yama 作成
 */

public class KNJP410 {


    private static final Log log = LogFactory.getLog(KNJP410.class);

    int total_dabit1,total_patinto1 = 0;

    /**
     * KNJP.classから最初に呼ばれる処理。
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws ServletException, IOException
    {

        Vrw32alp svf = new Vrw32alp();
        DB2UDB db2 = null;
        String param[] = new String[11];

        String selected[] = request.getParameterValues("SELECT_SELECTED");

        // SQL作成時のIN文の作成
        StringBuffer sql = new StringBuffer();
        sql.append(" ( ");

        String comma = "";
        for (int i = 0; i < selected.length; i++) {
            sql.append(comma);
            sql.append("'").append(selected[i]).append("'");
            comma = ",";
        }
        sql.append(")");

        // パラメータの取得
        try {
            param[0]  = request.getParameter("YEAR");     //年度
            param[1]  = request.getParameter("SEMESTER"); //学期
            param[2]  = request.getParameter("OUTPUT");   //出力対象 1:学年,2,クラス
            param[3]  = request.getParameter("MONTH");    //印刷日
            param[4]  = request.getParameter("GRADE");    //学年
        } catch( Exception ex ) {
            log.warn("parameter error!",ex);
        }

        // print設定
        PrintWriter outstrm = new PrintWriter (response.getOutputStream());
        response.setContentType("application/pdf");

        // svf設定
        int ret = svf.VrInit();
        if (false && 0 != ret) { ret = 0; }
        ret = svf.VrSetSpoolFileStream(response.getOutputStream());

        // ＤＢ接続
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
        } catch( Exception ex ) {
            log.error("DB2 open error!",ex);
            return;
        }

        boolean nonedata = false;

        getHeaderData(db2,svf,param,sql);

for(int ia=0 ; ia<param.length ; ia++) log.debug("param["+ia+"]="+param[ia]);

        // SVF出力
        if( printMain(db2,svf,param,sql) ) nonedata = true;

        // 該当データ無し
        if( !nonedata ){
            ret = svf.VrSetForm("MES001.frm", 0);
            ret = svf.VrsOut("note" , "note");
            ret = svf.VrEndPage();
        }

        // 終了処理
        ret = svf.VrQuit();
        db2.commit();
        db2.close();
        outstrm.close();

    }//doGetの括り


    /**
     * ヘッダーデータを抽出。
     * @param db2
     * @param svf
     * @param param
     * @param sql
     */
    private void getHeaderData(DB2UDB db2,Vrw32alp svf,String param[],StringBuffer sql){

        // 作成日
        try {
            KNJ_Get_Info getinfo = new KNJ_Get_Info();
            KNJ_Get_Info.ReturnVal returnval = null;
            returnval = getinfo.Control(db2);
            param[6] = KNJ_EditDate.h_format_JP(returnval.val3);
            getinfo = null;
            returnval = null;
        } catch( Exception e ){
            log.warn("ctrl_date get error!",e);
        }
log.debug("heda OK!");
        // 年度
        try {
            param[5] = nao_package.KenjaProperties.gengou(Integer.parseInt(param[0])) + "年度";
        } catch( Exception e ){
            log.warn("jinendo get error!",e);
        }

        // 軽減額出力判定
        try {
            PreparedStatement ps = null;
            ps = db2.prepareStatement(getRedcCnt(param,sql));
            ResultSet rs = ps.executeQuery();

            while ( rs.next() ){
                param[7] = rs.getString("RED_CNT");
            }
            psrsClose(ps,rs);

        } catch( Exception e ){
            log.warn("jinendo get error!",e);
        }

        // 返金出力月取得
        try {
            PreparedStatement ps = null;
            ps = db2.prepareStatement(getExeMonth(param));
            ResultSet rs = ps.executeQuery();
            int setMonth  = 0;
            int paraMonth = Integer.parseInt(param[3]);
            while ( rs.next() ){
                setMonth = rs.getInt("EXEMONTH");
                param[9] = rs.getString("EXEDATE");
            }

            psrsClose(ps,rs);

            if (paraMonth < 4){
                paraMonth = paraMonth+12;
            }
            if (setMonth <= paraMonth){
                param[8] = String.valueOf(setMonth);
            }

        } catch( Exception e ){
            log.warn("jinendo get error!",e);
        }

    }//getHeaderData()の括り

    /**
     * 印刷処理メイン。
     * @param db2
     * @param svf
     * @param param
     * @param sql
     * @return 印字データ有りの場合true
     */
    private boolean printMain(DB2UDB db2,Vrw32alp svf,String param[],StringBuffer sql)
    {
        boolean nonedata = false;
        PreparedStatement ps = null;
        int ret   = 0;
        if (false && 0 != ret) { ret = 0; }
        int rscnt = 1;
        int gyo   = 1;
        String pagechange = "*";
        String attendchange = "*";

        try {
log.debug("printMain start!");
            ps = db2.prepareStatement(meisaiSql(param,sql));
            ResultSet rs = ps.executeQuery();
log.debug("printMain end!");

            ret = svf.VrSetForm("KNJP410.frm", 4);
            while( rs.next() ){
                if (!attendchange.equals("*") && !attendchange.equals(rs.getString("ATTENDNO"))){
                    rscnt = 1;
                }
                if (!pagechange.equals("*") && !pagechange.equalsIgnoreCase(rs.getString("PAGECHANGE"))){
                    if (gyo > 36){
                        gyo = 1;
                    }
                    for (;gyo < 33;gyo++){
                        ret = svf.VrsOut("NAME_MASK"        ,"999");
                        ret = svf.VrEndRecord();
                    }
                    printTotalSem(db2,svf,param,sql,pagechange);
                    gyo+=4;
                }
                if (gyo > 36){
                    gyo = 1;
                }
                rscnt = printMeisai(svf,param,rs,rscnt);

                attendchange = rs.getString("ATTENDNO");
                pagechange = rs.getString("PAGECHANGE");
                gyo++;
                nonedata = true;
            }

            printTotalSem(db2,svf,param,sql,pagechange);

            printTotalSem(db2,svf,param,sql,null);

            psrsClose(ps,rs);
            db2.commit();
        } catch( Exception ex ) {
            log.warn("printMain read error!",ex);
        }
        return nonedata;

    }//printMain()の括り

    /**
     * 明細データセット。
     * @param svf
     * @param param
     * @param rs
     * @param rscnt
     * @return 出力レコードデータのカウンタ
     */
    private int printMeisai(Vrw32alp svf,String param[],ResultSet rs,int rscnt)
    {
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        try {
            ret = svf.VrsOut("HR_CLASS"            , rs.getString("HR_NAME") );
            ret = svf.VrsOut("NENDO"            , param[5] );
            ret = svf.VrsOut("MONTH"            , param[3] );
            ret = svf.VrsOut("DATE"                , param[6] );

            ret = svf.VrsOut("ATTENDNO"            , rs.getString("ATTENDNO") );

            if (rscnt == 2){
                ret = svf.VrsOut("NAME"                , rs.getString("NAME") );
            }else if (rscnt == 3){
                ret = svf.VrsOut("SCHREGNO"            , rs.getString("SCHREGNO") );
            }
            ret = svf.VrsOut("NAME_MASK"        , rs.getString("ATTENDNO") );

            printMark(svf,rs.getString("EXPENSE_M_CD"));

            // 納入額
            ret = svf.VrsOut("NECESSARY"        , rs.getString("MONEY_DUE") );

            // 軽減額
            if (rs.getInt("REDUCTIONMONEY") > 0){
                ret = svf.VrsOut("REDUCTION"        , rs.getString("REDUCTIONMONEY") );
            }
            // 引落、振込、現金セット
            if (null != rs.getString("PAID_MONEY_DIV") && rs.getString("PAID_MONEY_DIV").equals("00")){
                printMeisaiTotal(svf,rs);
            }else {
                printMoney(svf,rs);
            }

            // 分納
            if (null != rs.getString("INST_PAID_DATE")){
                ret = svf.VrsOut("INSTALLMENT_DATE"        , rs.getString("INST_PAID_DATE").substring(5,7)+"/"+rs.getString("INST_PAID_DATE").substring(8) );
            }
            if (rs.getInt("BUNMONEY") > 0){
                ret = svf.VrsOut("INSTALLMENT_MONEY"    , rs.getString("BUNMONEY") );
            }

            // 完納
            if (rs.getInt("MONEY_DUE") == rs.getInt("PAID_MONEY")
                                          + rs.getInt("BUNMONEY")
                                          + rs.getInt("REDUCTIONMONEY")
                                          - rs.getInt("REPAY_MONEY")
                                          - rs.getInt("REPAY_MONEY2")
                                          - rs.getInt("INSTIGATION"))
            {
                ret = svf.VrsOut("PAYALL"            , "*" );
            }

            if (rs.getInt("REPAY_MONEY") > 0 || rs.getInt("REPAY_MONEY2") > 0){
                // 留学等返金日付
                if (null != rs.getString("REPAY_DATE")){
                    ret = svf.VrsOut("REPAYMENT_DATE1"        , rs.getString("REPAY_DATE").substring(5,7)+"/"+rs.getString("REPAY_DATE").substring(8) );
                }
                ret = svf.VrsOut("REPAYMENT_MONEY1"          , String.valueOf(rs.getInt("REPAY_MONEY") + rs.getInt("REPAY_MONEY2")) );
            }

            if (rs.getInt("INSTIGATION") > 0){
                // 軽減返金日付
                if (null != param[9]){
                    ret = svf.VrsOut("REPAYMENT_DATE2"        , param[9].substring(5,7)+"/"+param[9].substring(8) );
                }
                ret = svf.VrsOut("REPAYMENT_MONEY2"          , rs.getString("INSTIGATION") );
            }

            // 差引入金額
            if (null != rs.getString("PAID_MONEY_DIV") && rs.getString("PAID_MONEY_DIV").equals("00")){
                ret = svf.VrsOut("BALANCE", String.valueOf(total_dabit1+total_patinto1+rs.getInt("BUNMONEY")-(rs.getInt("REPAY_MONEY") + rs.getInt("REPAY_MONEY2"))-rs.getInt("INSTIGATION")));
                total_dabit1 = 0;
                total_patinto1 = 0;
            }else {
                ret = svf.VrsOut("BALANCE", String.valueOf(rs.getInt("PAID_MONEY")+rs.getInt("BUNMONEY")-(rs.getInt("REPAY_MONEY") + rs.getInt("REPAY_MONEY2"))-rs.getInt("INSTIGATION")));
            }

            ret = svf.VrsOut("REMARK_MASK"                , rs.getString("ATTENDNO") );

            ret = svf.VrEndRecord();
            rscnt++;
        } catch( Exception ex ) {
            log.warn("printMeisai read error!",ex);
        }

        return rscnt;
    }//printMeisai()の括り

    /**
     * ギリシャ数字の１〜３と累計の出力。
     * @param svf
     * @param m_cd
     */
    private void printMark(Vrw32alp svf,String m_cd)
    {
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        try {
            if (null != m_cd && m_cd.equals("11")){
                // ギリシャ数字１
                ret = svf.VrsOut("PERIOD"    , "\u2160" );
            }else if (null != m_cd && m_cd.equals("12")){
                // ギリシャ数字２
                ret = svf.VrsOut("PERIOD"    , "\u2161" );
            }else if (null != m_cd && m_cd.equals("13")){
                // ギリシャ数字３
                ret = svf.VrsOut("PERIOD"    , "\u2162" );
            }else {
                // 累計
                ret = svf.VrsOut("PERIOD"    , "累計" );
            }
        } catch( Exception ex ){
            log.warn("printMark set error!",ex);
        }
    }

    /**
     * 金額データセット。
     * @param svf
     * @param rs
     */
    private void printMoney(Vrw32alp svf,ResultSet rs)
    {
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        try {
            if (rs.getString("PAID_MONEY_DIV") != null && rs.getString("PAID_MONEY_DIV").equals("01")){
                if (null != rs.getString("PAID_MONEY_DATE")){
                    ret = svf.VrsOut("DEBIT_DATE"        , rs.getString("PAID_MONEY_DATE").substring(5,7)+"/"+rs.getString("PAID_MONEY_DATE").substring(8) );
                }
                ret = svf.VrsOut("DABIT_MANEY"        , rs.getString("PAID_MONEY") );
                if (null != rs.getString("PAID_MONEY")){
                    total_dabit1 += rs.getInt("PAID_MONEY");
                }
            }else if (rs.getString("PAID_MONEY_DIV") != null && rs.getString("PAID_MONEY_DIV").equals("02")){
                if (null != rs.getString("PAID_MONEY_DATE")){
                    ret = svf.VrsOut("PAYINTO_DATE"        , rs.getString("PAID_MONEY_DATE").substring(5,7)+"/"+rs.getString("PAID_MONEY_DATE").substring(8) );
                }
                ret = svf.VrsOut("PATINTO_MONEY"    , rs.getString("PAID_MONEY") );
                if (null != rs.getString("PAID_MONEY")){
                    total_patinto1 += rs.getInt("PAID_MONEY");
                }
            }else if (rs.getString("PAID_MONEY_DIV") != null && rs.getInt("PAID_MONEY_DIV") > 2){
                if (null != rs.getString("PAID_MONEY_DATE")){
                    ret = svf.VrsOut("CASH_DATE"        , rs.getString("PAID_MONEY_DATE").substring(5,7)+"/"+rs.getString("PAID_MONEY_DATE").substring(8) );
                }
                ret = svf.VrsOut("PATINTO_MONEY"        , rs.getString("PAID_MONEY") );
                if (null != rs.getString("PAID_MONEY")){
                    total_patinto1 += rs.getInt("PAID_MONEY");
                }
            }
        } catch( Exception ex ) {
            log.warn("printMeisai read error!",ex);
        }

    }//printMeisai()の括り

    /**
     * 累計データセット。
     * @param svf
     * @param rs
     */
    private void printMeisaiTotal(Vrw32alp svf,ResultSet rs)
    {
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        try {
            if (total_dabit1 > 0){
                ret = svf.VrsOut("DABIT_MANEY"        , String.valueOf(total_dabit1) );
            }
            if (total_patinto1 > 0){
                ret = svf.VrsOut("PATINTO_MONEY"    , String.valueOf(total_patinto1) );
            }
        } catch( Exception ex ) {
            log.warn("printMeisai read error!",ex);
        }

    }//printMeisai()の括り

    /**
     * 期別合計データセット。
     * @param db2
     * @param svf
     * @param param
     * @param sql
     * @param pagechange
     */
    private void printTotalSem(DB2UDB db2,Vrw32alp svf,String param[],StringBuffer sql,String pagechange)
    {
        int ret  = 0;
        if (false && 0 != ret) { ret = 0; }
        int gcnt = 1;
        PreparedStatement ps = null;
        try {
            ps = db2.prepareStatement(totalSql(param,sql,pagechange));
            ResultSet rs = ps.executeQuery();
            while (rs.next()){

                printCommonObjection(svf,gcnt,pagechange);

                printMark(svf,rs.getString("EXPENSE_M_CD"));
                // 納入額
                ret = svf.VrsOut("NECESSARY"        , rs.getString("MONEY_DUE") );
                // 軽減額
                ret = svf.VrsOut("REDUCTION"        , rs.getString("REDUCTIONMONEY") );
                // 引落
                ret = svf.VrsOut("DABIT_MANEY"        , rs.getString("PAID_MONEY") );
                // 振込
                ret = svf.VrsOut("PATINTO_MONEY"    , String.valueOf(rs.getInt("PAID_FURI") + rs.getInt("PAID_CASH")) );
                // 分納
                ret = svf.VrsOut("INSTALLMENT_MONEY"    , rs.getString("BUNMONEY") );
                // 完納
                if (rs.getInt("MONEY_DUE") == rs.getInt("PAID_MONEY")
                                              + rs.getInt("PAID_FURI")
                                              + rs.getInt("PAID_CASH")
                                              + rs.getInt("BUNMONEY")
                                              + rs.getInt("REDUCTIONMONEY")
                                              - rs.getInt("REPAY_MONEY")
                                              - rs.getInt("INSTIGATION"))
                {
                    ret = svf.VrsOut("PAYALL"    , "*" );
                }
                // 留学等返金
                ret = svf.VrsOut("REPAYMENT_MONEY1"  , rs.getString("REPAY_MONEY") );
                // 軽減返金
                ret = svf.VrsOut("REPAYMENT_MONEY2"  , rs.getString("INSTIGATION") );
                // 差引入金額
                ret = svf.VrsOut("BALANCE", String.valueOf(rs.getInt("PAID_MONEY")+rs.getInt("PAID_FURI") + rs.getInt("PAID_CASH")+rs.getInt("BUNMONEY")-rs.getInt("REPAY_MONEY")-rs.getInt("INSTIGATION")));
                ret = svf.VrEndRecord();
                gcnt++;
            }
            psrsClose(ps,rs);

        } catch( Exception ex ) {
            log.warn("printTotalSem read error!",ex);
        }

    }//printTotalSem()の括り

    /**
     * 累計共通文言セット。
     * @param svf
     * @param gcnt
     * @param pagechange
     */
    private void printCommonObjection(Vrw32alp svf,int gcnt,String pagechange)
    {
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        try {

            if (gcnt == 2){
                if (null != pagechange){
                    ret = svf.VrsOut("NAME"                , "期別合計" );
                }else {
                    ret = svf.VrsOut("NAME"                , "期別総計" );
                }
            }else if (gcnt == 4){
                if (null != pagechange){
                    ret = svf.VrsOut("NAME"                , "クラス合計" );
                }else {
                    ret = svf.VrsOut("NAME"                , "クラス総計" );
                }
                ret = svf.VrsOut("NAME_MASK"        , "000" );
            }

            if (null != pagechange){
                ret = svf.VrsOut("REMARK_MASK"    , "888" );
            }else {
                ret = svf.VrsOut("REMARK_MASK"    , "999" );
            }

        } catch( Exception ex ){
            log.warn("printTotalSub set error!",ex);
        }
    }

    private void psrsClose(PreparedStatement ps,ResultSet rs){
        try {
            if (null != rs) { rs.close(); }
        } catch (final SQLException e) {
            log.error("rsclose error", e);
        }
        try {
            if (null != ps) { ps.close(); }
        } catch (final SQLException e) {
            log.error("psclose error", e);
        }
    }

    /**
     * 明細データを抽出。
     * @param param
     * @param sql
     * @return 明細データを抽出するSQLの文字列
     */
    private String meisaiSql(String param[],StringBuffer sql)
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append(" WITH REDUCTION_DAT_SUM AS ( ");
            stb.append(" SELECT ");
            stb.append("     t1.YEAR, ");
            stb.append("     t1.SCHREGNO, ");
            stb.append("     CASE WHEN REDUC_DEC_FLG_1 = '1' OR REDUC_DEC_FLG_2 = '1' ");
            stb.append("          THEN '1' ");
            stb.append("          ELSE '0' ");
            stb.append("     END AS REDUC_DEC_FLG, ");
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
            stb.append("    t1.YEAR = '"+param[0]+"' ");
            stb.append("    AND (REDUC_DEC_FLG_1 = '1' ");
            stb.append("         OR ");
            stb.append("         REDUC_DEC_FLG_2 = '1') ");
            stb.append(" GROUP BY ");
            stb.append("     t1.YEAR, ");
            stb.append("     t1.SCHREGNO, ");
            stb.append("     CASE WHEN REDUC_DEC_FLG_1 = '1' OR REDUC_DEC_FLG_2 = '1' ");
            stb.append("          THEN '1' ");
            stb.append("          ELSE '0' ");
            stb.append("     END ");
            stb.append("), MAIN_T AS ( ");
            stb.append("SELECT ");
            stb.append("    T1.GRADE || T1.HR_CLASS AS PAGECHANGE, ");
            stb.append("    T2.HR_NAME, ");
            stb.append("    T1.GRADE, ");
            stb.append("    T1.HR_CLASS, ");
            stb.append("    T1.ATTENDNO, ");
            stb.append("    T7.NAME, ");
            stb.append("    T1.SCHREGNO, ");
            stb.append("    T3.EXPENSE_M_CD, ");
            stb.append("    VALUE(T3.MONEY_DUE,0) AS MONEY_DUE, ");
            stb.append("    CASE WHEN '"+param[7]+"' = '0' THEN 0 ELSE SUM(VALUE(T8.REDUCTIONMONEY,0)) END AS REDUCTIONMONEY, ");
            stb.append("    T4.PAID_MONEY_DIV, ");
            stb.append("    T4.PAID_MONEY_DATE, ");
            stb.append("    VALUE(T4.PAID_MONEY,0) AS PAID_MONEY, ");
            stb.append("    T3.INST_CD, ");
            stb.append("    MAX(T5.PAID_MONEY_DATE) AS INST_PAID_DATE, ");
            stb.append("    SUM(VALUE(T5.PAID_MONEY,0)) AS BUNMONEY, ");
            stb.append("    MAX(T4.REPAY_DATE) AS REPAY_DATE, ");
            stb.append("    CASE WHEN '"+param[7]+"' > '0' AND VALUE(T3.MONEY_DUE,0) < VALUE(T8.REDUCTIONMONEY,0) ");
            if (null != param[8]){
                stb.append("    THEN VALUE(T8.REDUCTIONMONEY,0) - VALUE(T3.MONEY_DUE,0) ");
            }else {
                stb.append("    THEN 0 ");
            }
            stb.append("    ELSE 0 END AS INSTIGATION, ");
            stb.append("    VALUE(T4.REPAY_MONEY,0) AS REPAY_MONEY, ");
            stb.append("    MAX(T5.REPAY_DATE) AS REPAY_DATE2, ");
            stb.append("    SUM(VALUE(T5.REPAY_MONEY,0)) AS REPAY_MONEY2 ");
            stb.append("FROM ");
            stb.append("    SCHREG_REGD_DAT T1 ");
            stb.append("    LEFT JOIN SCHREG_REGD_HDAT T2 ON T2.YEAR = T1.YEAR ");
            stb.append("    AND T2.SEMESTER = T1.SEMESTER ");
            stb.append("    AND T2.GRADE || T2.HR_CLASS = T1.GRADE || T1.HR_CLASS ");
            stb.append("    LEFT JOIN MONEY_DUE_M_DAT T3 ON T3.YEAR = T1.YEAR ");
            stb.append("    AND T3.SCHREGNO = T1.SCHREGNO ");
            stb.append("    AND EXPENSE_M_CD < '20' ");
            stb.append("    LEFT JOIN MONEY_PAID_M_DAT T4 ON T4.YEAR = T1.YEAR ");
            stb.append("    AND T4.SCHREGNO = T1.SCHREGNO ");
            stb.append("    AND T4.EXPENSE_M_CD = T3.EXPENSE_M_CD ");
            if (Integer.parseInt(param[3]) < 4){
                stb.append("    AND (MONTH(T4.PAID_MONEY_DATE) <= "+Integer.parseInt(param[3])+" OR MONTH(T4.PAID_MONEY_DATE) > 3) ");
                stb.append("    AND YEAR(T4.PAID_MONEY_DATE) <= "+Integer.parseInt(param[0])+"+1 ");
            }else {
                stb.append("    AND MONTH(T4.PAID_MONEY_DATE) <= "+Integer.parseInt(param[3])+" ");
                stb.append("    AND MONTH(T4.PAID_MONEY_DATE) > 3 ");
            }
            stb.append("    LEFT JOIN INSTALLMENT_DAT T5 ON T5.YEAR = T1.YEAR ");
            stb.append("    AND T5.INST_CD = T3.INST_CD ");
            stb.append("    AND T5.SCHREGNO = T3.SCHREGNO ");
            if (Integer.parseInt(param[3]) < 4){
                stb.append("    AND (MONTH(T5.PAID_MONEY_DATE) <= "+Integer.parseInt(param[3])+" OR MONTH(T5.PAID_MONEY_DATE) > 3) ");
                stb.append("    AND YEAR(T5.PAID_MONEY_DATE) <= "+Integer.parseInt(param[0])+"+1 ");
            }else {
                stb.append("    AND MONTH(T5.PAID_MONEY_DATE) <= "+Integer.parseInt(param[3])+" ");
                stb.append("    AND MONTH(T5.PAID_MONEY_DATE) > 3 ");
            }
            stb.append("    LEFT JOIN SCHREG_BASE_MST T7 ON T7.SCHREGNO = T1.SCHREGNO ");
            stb.append("    LEFT JOIN REDUCTION_DAT_SUM T8 ON T8.YEAR = T1.YEAR ");
            stb.append("    AND T8.SCHREGNO = T1.SCHREGNO ");
            stb.append("    AND T8.REDUC_DEC_FLG = '1' ");
            stb.append("    AND T3.EXPENSE_M_CD = '13' ");
            stb.append("WHERE ");
            stb.append("    T1.YEAR = '"+param[0]+"' ");
            stb.append("    AND T1.SEMESTER = '"+param[1]+"' ");
            if (param[2].equals("2")){
                stb.append("    AND T1.GRADE || T1.HR_CLASS IN "+sql+" ");
            }else {
                stb.append("    AND T1.GRADE IN "+sql+" ");
            }
            stb.append("GROUP BY ");
            stb.append("    T1.GRADE, ");
            stb.append("    T1.HR_CLASS, ");
            stb.append("    T1.ATTENDNO, ");
            stb.append("    T1.GRADE || T1.HR_CLASS, ");
            stb.append("    T2.HR_NAME, ");
            stb.append("    T7.NAME, ");
            stb.append("    T1.SCHREGNO, ");
            stb.append("    T3.EXPENSE_M_CD, ");
            stb.append("    T3.MONEY_DUE, ");
            stb.append("    T8.REDUCTIONMONEY, ");
            stb.append("    T4.PAID_INPUT_FLG, ");
            stb.append("    T4.PAID_MONEY_DATE, ");
            stb.append("    T4.PAID_MONEY, ");
            stb.append("    T4.PAID_MONEY_DIV, ");
            stb.append("    T3.INST_CD, ");
            stb.append("    T4.REPAY_MONEY ");
            stb.append("), SUB_T AS ( ");
            stb.append("SELECT ");
            stb.append("    PAGECHANGE, ");
            stb.append("    HR_NAME, ");
            stb.append("    GRADE, ");
            stb.append("    HR_CLASS, ");
            stb.append("    ATTENDNO, ");
            stb.append("    NAME, ");
            stb.append("    SCHREGNO, ");
            stb.append("    '14' AS EXPENSE_M_CD, ");
            stb.append("    SUM(VALUE(MONEY_DUE,0)) AS MONEY_DUE, ");
            stb.append("    SUM(REDUCTIONMONEY) AS REDUCTIONMONEY, ");
            stb.append("    '00' AS PAID_MONEY_DIV, ");
            stb.append("    CAST(NULL AS DATE) AS PAID_MONEY_DATE, ");
            stb.append("    SUM(PAID_MONEY) AS PAID_MONEY, ");
            stb.append("    '' AS INST_CD, ");
            stb.append("    MAX(INST_PAID_DATE) AS INST_PAID_DATE, ");
            stb.append("    SUM(BUNMONEY) AS BUNMONEY, ");
            stb.append("    MAX(REPAY_DATE) AS REPAY_DATE, ");
            stb.append("    SUM(INSTIGATION) AS INSTIGATION, ");
            stb.append("    SUM(REPAY_MONEY) AS REPAY_MONEY, ");
            stb.append("    MAX(REPAY_DATE2) AS REPAY_DATE2, ");
            stb.append("    SUM(REPAY_MONEY2) AS REPAY_MONEY2 ");
            stb.append("FROM ");
            stb.append("    MAIN_T ");
            stb.append("GROUP BY ");
            stb.append("    GRADE, ");
            stb.append("    HR_CLASS, ");
            stb.append("    ATTENDNO, ");
            stb.append("    PAGECHANGE, ");
            stb.append("    HR_NAME, ");
            stb.append("    NAME, ");
            stb.append("    SCHREGNO ");
            stb.append(") ");
            stb.append("SELECT ");
            stb.append("    * ");
            stb.append("FROM ");
            stb.append("    MAIN_T ");
            stb.append("UNION ");
            stb.append("SELECT ");
            stb.append("    * ");
            stb.append("FROM ");
            stb.append("    SUB_T ");
            stb.append("ORDER BY ");
            stb.append("    GRADE, ");
            stb.append("    HR_CLASS, ");
            stb.append("    ATTENDNO, ");
            stb.append("    EXPENSE_M_CD ");

//log.debug(stb);
        } catch( Exception e ){
            log.warn("meisaiSql error!",e);
        }
        return stb.toString();

    }//meisaiSql()の括り

    /**
     * 累計データ抽出。
     * @param param
     * @param sql
     * @param pagechange
     * @return 累計データを抽出するSQLの文字列
     */
    private String totalSql(String param[],StringBuffer sql,String pagechange)
    {
        StringBuffer stb = new StringBuffer();
        try {

            stb.append("WITH SCH_T AS ( ");
            stb.append("SELECT ");
            stb.append("    SCHREGNO ");
            stb.append("FROM ");
            stb.append("    SCHREG_REGD_DAT ");
            stb.append("WHERE ");
            stb.append("    YEAR = '"+param[0]+"' ");
            stb.append("    AND SEMESTER = '"+param[1]+"' ");
            if (param[2].equals("2")){
                stb.append("    AND GRADE || HR_CLASS IN "+sql+" ");
            }else {
                stb.append("    AND GRADE IN "+sql+" ");
            }
            if (null != pagechange){
                stb.append("    AND GRADE || HR_CLASS = '"+pagechange+"' ");
            }
            stb.append(" ), REDUCTION_DAT_SUM AS ( ");
            stb.append(" SELECT ");
            stb.append("     t1.YEAR, ");
            stb.append("     t1.SCHREGNO, ");
            stb.append("     CASE WHEN REDUC_DEC_FLG_1 = '1' OR REDUC_DEC_FLG_2 = '1' ");
            stb.append("          THEN '1' ");
            stb.append("          ELSE '0' ");
            stb.append("     END AS REDUC_DEC_FLG, ");
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
            stb.append("    t1.YEAR = '"+param[0]+"' ");
            stb.append("    AND (REDUC_DEC_FLG_1 = '1' ");
            stb.append("         OR ");
            stb.append("         REDUC_DEC_FLG_2 = '1') ");
            stb.append(" GROUP BY ");
            stb.append("     t1.YEAR, ");
            stb.append("     t1.SCHREGNO, ");
            stb.append("     CASE WHEN REDUC_DEC_FLG_1 = '1' OR REDUC_DEC_FLG_2 = '1' ");
            stb.append("          THEN '1' ");
            stb.append("          ELSE '0' ");
            stb.append("     END ");
            stb.append("),MONEY_DUE_SCH AS ( ");
            stb.append("SELECT ");
            stb.append("    T1.SCHREGNO, ");
            stb.append("    T2.EXPENSE_M_CD, ");
            stb.append("    SUM(VALUE(T2.MONEY_DUE,0)) AS MONEY_DUE, ");
            stb.append("    CASE WHEN '"+param[7]+"' = '0' THEN 0 ELSE SUM(VALUE(T4.REDUCTIONMONEY,0)) END AS REDUCTIONMONEY, ");
            stb.append("    SUM(VALUE(T3.PAID_MONEY,0)) AS BUNMONEY, ");
            stb.append("    SUM(VALUE(T3.REPAY_MONEY,0)) AS REPAY_MONEY2, ");
            stb.append("    CASE WHEN '"+param[7]+"' > '0' AND VALUE(T2.MONEY_DUE,0) < VALUE(T4.REDUCTIONMONEY,0) ");
            if (null != param[8]){
                stb.append("    THEN VALUE(T4.REDUCTIONMONEY,0) - VALUE(T2.MONEY_DUE,0) ");
            }else {
                stb.append("    THEN 0 ");
            }
            stb.append("    ELSE 0 END AS INSTIGATION, ");
            stb.append("    SUM(VALUE(T3.REPAY_MONEY,0)) AS REPAY_MONEY ");
            stb.append("FROM ");
            stb.append("    SCH_T T1 ");
            stb.append("    LEFT JOIN MONEY_DUE_M_DAT T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("    AND T2.YEAR = '"+param[0]+"' ");
            stb.append("    AND T2.EXPENSE_M_CD < '20' ");
            stb.append("    LEFT JOIN INSTALLMENT_DAT T3 ON T3.YEAR = T2.YEAR ");
            stb.append("    AND T3.INST_CD = T2.INST_CD ");
            stb.append("    AND T3.SCHREGNO = T2.SCHREGNO ");
            if (Integer.parseInt(param[3]) < 4){
                stb.append("    AND (MONTH(T3.PAID_MONEY_DATE) <= "+Integer.parseInt(param[3])+" OR MONTH(T3.PAID_MONEY_DATE) > 3) ");
                stb.append("    AND YEAR(T3.PAID_MONEY_DATE) <= "+Integer.parseInt(param[0])+"+1 ");
            }else {
                stb.append("    AND MONTH(T3.PAID_MONEY_DATE) <= "+Integer.parseInt(param[3])+" ");
                stb.append("    AND MONTH(T3.PAID_MONEY_DATE) > 3 ");
            }
            stb.append("    LEFT JOIN REDUCTION_DAT_SUM T4 ON T4.YEAR = T2.YEAR ");
            stb.append("    AND T4.SCHREGNO = T1.SCHREGNO ");
            stb.append("    AND T4.REDUC_DEC_FLG = '1' ");
            stb.append("    AND T2.EXPENSE_M_CD = '13' ");
            stb.append("GROUP BY ");
            stb.append("    T1.SCHREGNO, ");
            stb.append("    T2.EXPENSE_M_CD, ");
            stb.append("    T2.MONEY_DUE, ");
            stb.append("    T4.REDUCTIONMONEY, ");
            stb.append("    T3.REPAY_MONEY ");
            stb.append("),MONEY_DUE_T AS ( ");
            stb.append("SELECT ");
            stb.append("    '1' AS TOTALCD, ");
            stb.append("    EXPENSE_M_CD, ");
            stb.append("    SUM(VALUE(MONEY_DUE,0)) AS MONEY_DUE, ");
            stb.append("    CASE WHEN '"+param[7]+"' = '0' THEN 0 ELSE SUM(VALUE(REDUCTIONMONEY,0)) END AS REDUCTIONMONEY, ");
            stb.append("    SUM(VALUE(BUNMONEY,0)) AS BUNMONEY, ");
            stb.append("    SUM(VALUE(INSTIGATION,0)) AS INSTIGATION, ");
            stb.append("    SUM(VALUE(REPAY_MONEY,0)) AS REPAY_MONEY2 ");
            stb.append("FROM ");
            stb.append("    MONEY_DUE_SCH ");
            stb.append("GROUP BY ");
            stb.append("    EXPENSE_M_CD ");
            stb.append("),PAID_T AS ( ");
            stb.append("SELECT ");
            stb.append("    T2.EXPENSE_M_CD, ");
            stb.append("    T2.PAID_MONEY_DIV, ");
            stb.append("    SUM(VALUE(T2.PAID_MONEY,0)) AS PAID_MONEY, ");
            stb.append("    SUM(VALUE(T2.REPAY_MONEY,0)) AS REPAY_MONEY ");
            stb.append("FROM ");
            stb.append("    SCH_T T1 ");
            stb.append("    LEFT JOIN MONEY_PAID_M_DAT T2 ON T2.YEAR = '"+param[0]+"' ");
            stb.append("    AND T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("    AND T2.EXPENSE_M_CD < '20' ");
            if (Integer.parseInt(param[3]) < 4){
                stb.append("    AND (MONTH(T2.PAID_MONEY_DATE) <= "+Integer.parseInt(param[3])+" OR MONTH(T2.PAID_MONEY_DATE) > 3) ");
                stb.append("    AND YEAR(T2.PAID_MONEY_DATE) <= "+Integer.parseInt(param[0])+"+1 ");
            }else {
                stb.append("    AND MONTH(T2.PAID_MONEY_DATE) <= "+Integer.parseInt(param[3])+" ");
                stb.append("    AND MONTH(T2.PAID_MONEY_DATE) > 3 ");
            }
            stb.append("GROUP BY ");
            stb.append("    T2.EXPENSE_M_CD, ");
            stb.append("    T2.PAID_MONEY_DIV ");
            stb.append(") ");
            stb.append("SELECT ");
            stb.append("    T1.TOTALCD, ");
            stb.append("    T1.EXPENSE_M_CD, ");
            stb.append("    VALUE(T1.MONEY_DUE,0) AS MONEY_DUE, ");
            stb.append("    VALUE(T1.REDUCTIONMONEY,0) AS REDUCTIONMONEY, ");
            stb.append("    VALUE(P1.PAID_MONEY,0) AS PAID_MONEY, ");
            stb.append("    VALUE(P2.PAID_MONEY,0) AS PAID_FURI, ");
            stb.append("    VALUE(P3.PAID_MONEY,0) AS PAID_CASH, ");
            stb.append("    VALUE(T1.BUNMONEY,0) AS BUNMONEY, ");
            stb.append("    VALUE(T1.INSTIGATION,0) AS INSTIGATION, ");
            stb.append("    VALUE(T1.REPAY_MONEY2,0) ");
            stb.append("    + VALUE(P1.REPAY_MONEY,0) ");
            stb.append("    + VALUE(P2.REPAY_MONEY,0) ");
            stb.append("    + VALUE(P3.REPAY_MONEY,0) AS REPAY_MONEY ");
            stb.append("FROM ");
            stb.append("    MONEY_DUE_T T1 ");
            stb.append("    LEFT JOIN PAID_T P1 ON P1.EXPENSE_M_CD = T1.EXPENSE_M_CD ");
            stb.append("    AND P1.PAID_MONEY_DIV = '01' ");
            stb.append("    LEFT JOIN PAID_T P2 ON P2.EXPENSE_M_CD = T1.EXPENSE_M_CD ");
            stb.append("    AND P2.PAID_MONEY_DIV = '02' ");
            stb.append("    LEFT JOIN PAID_T P3 ON P3.EXPENSE_M_CD = T1.EXPENSE_M_CD ");
            stb.append("    AND P3.PAID_MONEY_DIV > '02' ");
            stb.append("UNION ");
            stb.append("SELECT ");
            stb.append("    T1.TOTALCD, ");
            stb.append("    '14' AS EXPENSE_M_CD, ");
            stb.append("    SUM(VALUE(T1.MONEY_DUE,0)) AS MONEY_DUE, ");
            stb.append("    SUM(VALUE(T1.REDUCTIONMONEY,0)) AS REDUCTIONMONEY, ");
            stb.append("    SUM(VALUE(P1.PAID_MONEY,0)) AS PAID_MONEY, ");
            stb.append("    SUM(VALUE(P2.PAID_MONEY,0)) AS PAID_FURI, ");
            stb.append("    SUM(VALUE(P3.PAID_MONEY,0)) AS PAID_CASH, ");
            stb.append("    SUM(VALUE(T1.BUNMONEY,0)) AS BUNMONEY, ");
            stb.append("    SUM(VALUE(T1.INSTIGATION,0)) AS INSTIGATION, ");
            stb.append("    SUM(VALUE(T1.REPAY_MONEY2,0) ");
            stb.append("        + VALUE(P1.REPAY_MONEY,0) ");
            stb.append("        + VALUE(P2.REPAY_MONEY,0) ");
            stb.append("        + VALUE(P3.REPAY_MONEY,0)) AS REPAY_MONEY ");
            stb.append("FROM ");
            stb.append("    MONEY_DUE_T T1 ");
            stb.append("    LEFT JOIN PAID_T P1 ON P1.EXPENSE_M_CD = T1.EXPENSE_M_CD ");
            stb.append("    AND P1.PAID_MONEY_DIV = '01' ");
            stb.append("    LEFT JOIN PAID_T P2 ON P2.EXPENSE_M_CD = T1.EXPENSE_M_CD ");
            stb.append("    AND P2.PAID_MONEY_DIV = '02' ");
            stb.append("    LEFT JOIN PAID_T P3 ON P3.EXPENSE_M_CD = T1.EXPENSE_M_CD ");
            stb.append("    AND P3.PAID_MONEY_DIV > '02' ");
            stb.append("GROUP BY ");
            stb.append("    T1.TOTALCD ");
            stb.append("ORDER BY ");
            stb.append("    EXPENSE_M_CD ");

//log.debug(stb);
        } catch( Exception e ){
            log.warn("totalSql error!",e);
        }
        return stb.toString();

    }//totalSql()の括り

    /**
     * 軽減額出力判定SQL。
     * @param param
     * @param sql
     * @return 軽減額出力判定用SQLの文字列
     */
    private String getRedcCnt(String param[],StringBuffer sql)
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("WITH SCH_T AS ( ");
            stb.append("SELECT ");
            stb.append("    SCHREGNO ");
            stb.append("FROM ");
            stb.append("    SCHREG_REGD_DAT ");
            stb.append("WHERE ");
            stb.append("    YEAR = '"+param[0]+"' ");
            stb.append("    AND SEMESTER = '"+param[1]+"' ");
            if (param[2].equals("2")){
                stb.append("    AND GRADE || HR_CLASS IN "+sql+" ");
            }else {
                stb.append("    AND GRADE IN "+sql+" ");
            }
            stb.append(") ");
            stb.append("SELECT ");
            stb.append("    COUNT(*) AS RED_CNT ");
            stb.append("FROM ");
            stb.append("    MONEY_PAID_M_DAT ");
            stb.append("WHERE ");
            stb.append("    YEAR = '"+param[0]+"' ");
            stb.append("    AND SCHREGNO IN (SELECT SCHREGNO FROM SCH_T) ");
            stb.append("    AND EXPENSE_M_CD = '13' ");
            if (Integer.parseInt(param[3]) < 4){
                stb.append("    AND (MONTH(PAID_MONEY_DATE) <= "+Integer.parseInt(param[3])+" OR MONTH(PAID_MONEY_DATE) > 3) ");
                stb.append("    AND YEAR(PAID_MONEY_DATE) <= "+Integer.parseInt(param[0])+"+1 ");
            }else {
                stb.append("    AND MONTH(PAID_MONEY_DATE) <= "+Integer.parseInt(param[3])+" ");
                stb.append("    AND MONTH(PAID_MONEY_DATE) > 3 ");
            }

//log.debug(stb);
        } catch( Exception e ){
            log.warn("getRedcCnt error!" ,e);
        }
        return stb.toString();
    }

    /**
     * 返金出力月取得。
     * @param param
     * @return 返金出力月データを抽出する、SQLの文字列
     */
    private String getExeMonth(String param[])
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("SELECT ");
            stb.append("    DATE1 AS EXEDATE, ");
            stb.append("    CASE WHEN MONTH(DATE1) < 4 ");
            stb.append("    THEN MONTH(DATE1) + 12 ");
            stb.append("    ELSE MONTH(DATE1) END AS EXEMONTH ");
            stb.append("FROM ");
            stb.append("    SCHOOL_EXPENSES_SYS_INI ");
            stb.append("WHERE ");
            stb.append("    PROGRAMID = 'KNJP371' ");
            stb.append("    AND DIV = '"+param[0]+"' ");
        } catch(Exception e){
            log.warn("getExeDate error!" ,e);
        }
        return stb.toString();
    }

}//クラスの括り
