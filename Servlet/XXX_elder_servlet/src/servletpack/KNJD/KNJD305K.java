package servletpack.KNJD;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_Get_Info;

/**
 *
 *    学校教育システム 賢者 [成績管理]
 *
 *                    ＜ＫＮＪＤ３０５＞  中間・期末試験欠課者名簿
 *
 *    2004/08/25 yamashiro・新規作成
 *    2004/10/01 yamashiro・DB2のSQLにおいてINT関数の不具合を修正
 *    2004/10/20 nakamoto 出欠はkin_record_datにもたすような仕様に変更。この対応でattend_dat,testscore_hdat部分を修正。
 *    2004/11/09 nakamoto 欠課している科目に○が表示されないバグを修正。
 *    2004/12/03 nakamoto 欠課状況の記号とコメントの文言を変更
 *    2004/12/06 nakamoto 公欠表示をカット。
 *  2005/02/18 yamashiro 異動者除外の処理追加
 *  2005/06/24 nakamoto 期末試験のみ実施する講座を欠課した場合は、黒●とする。(期末テストのみ実施テストとは、中間にkk、ksがなくて、中間素点に点数がない)
 *
 *  2005/10/14 nakamoto 2005/06/24の仕様変更による修正（期末試験のみ実施する講座をsch_chr_testで判断）---NO001
 *               nakamoto 編入のデータ仕様変更および在籍異動条件に転入学を追加---NO002
 **/

public class KNJD305K {

    private static final Log log = LogFactory.getLog(KNJD305K.class);

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws ServletException, IOException
    {
        Vrw32alp svf = new Vrw32alp();             //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                        //Databaseクラスを継承したクラス
        final Param param = new Param(request);        //05/02/18Modify yamashiro

        log.fatal("$Revision: 75659 $");
		KNJServletUtils.debugParam(request, log);

    //    パラメータの取得
        final String classcd[] = request.getParameterValues("GRADE");               //学年

    //    print設定
        response.setContentType("application/pdf");
        OutputStream outstrm = response.getOutputStream();

    //    svf設定
        svf.VrInit();                               //クラスの初期化
        svf.VrSetSpoolFileStream(outstrm);           //PDFファイル名の設定

    //    ＤＢ接続
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
        } catch( Exception ex ) {
            log.error("DB2 open error!");
            return;
        }

    //    ＳＶＦ作成処理
        PreparedStatement ps1 = null;
        PreparedStatement ps2 = null;
        PreparedStatement ps3 = null;
        boolean nonedata = false;                                 //該当データなしフラグ
        Set_Head(db2,svf,param);                                //見出し出力のメソッド
        //SQL作成
        try {
            ps1 = db2.prepareStatement(Pre_Stat1(param));        //生徒及び公欠・欠席者
            ps2 = db2.prepareStatement(Pre_Stat2(param));        //試験日欠課
            ps3 = db2.prepareStatement(Pre_Stat3(param));        //科目
        } catch( Exception ex ) {
            log.warn("DB2 open error!");
        }
        //SVF出力
        for( int ia=0 ; ia<classcd.length ; ia++ )
            if( Set_Detail_1(db2,svf,param,classcd[ia],ps1,ps2,ps3) )nonedata = true;

    //    該当データ無し
        if( !nonedata ){
            svf.VrSetForm("MES001.frm", 0);
            svf.VrsOut("note" , "");
            svf.VrEndPage();
        }

    //     終了処理
        svf.VrQuit();
        DbUtils.closeQuietly(ps1);
        DbUtils.closeQuietly(ps2);
        DbUtils.closeQuietly(ps3);
        db2.commit();
        db2.close();                //DBを閉じる
        outstrm.close();            //ストリームを閉じる 

    }//doGetの括り



    /** SVF-FORM **/
    private void Set_Head(DB2UDB db2,Vrw32alp svf,final Param param) {

        KNJ_Get_Info getinfo = new KNJ_Get_Info();
        KNJ_Get_Info.ReturnVal returnval = null;
        svf.VrSetForm("KNJD_KYOTU2.frm", 4);                //共通フォーム
        // 04/11/09Del svf.VrAttribute("ABSENT","Print=2");
        svf.VrAttribute("MARK1","Print=0");
        svf.VrAttribute("MARK2","Print=0");
        svf.VrsOut("NENDO2", KNJ_EditDate.gengou(db2, Integer.parseInt(param._0))+"年度");//年度
        if(param._2.equals("01")){                                //中間
            svf.VrsOut("PRGID","KNJD305_1");
            svf.VrsOut("TITLE2","中間試験欠課者名簿");
            svf.VrsOut("NOTE1","〇中間のみ欠席で受験していない場合");    // 04/12/03Modify
            svf.VrsOut("NOTE2","△中間のみ公欠で受験していない場合");    // 04/12/03Add
        } else{                                                    //期末
            svf.VrsOut("PRGID","KNJD305_2");
            svf.VrsOut("TITLE2","期末試験欠課者名簿");
            svf.VrsOut("NOTE1","〇期末のみ欠席で受験していない場合、但し中間でも欠課している場合は●です");// 04/12/03Modify
            svf.VrsOut("NOTE2","△期末のみ公欠で受験していない場合、但し中間でも欠課している場合は▲です");// 04/12/03Modify
        }

    //    ＳＶＦ属性変更--->改ページ
        svf.VrAttribute("GRADE","FF=1");

    //    作成日(現在処理日)の取得
        try {
            returnval = getinfo.Control(db2);
            svf.VrsOut("DATE",KNJ_EditDate.h_format_JP(db2, returnval.val3));        //作成日
        } catch( Exception e ){
            log.warn("ctrl_date get error!");
        }
    //    学期名称の取得
        try {
            returnval = getinfo.Semester(db2,param._0,param._1);
            svf.VrsOut("SEMESTER2"    ,returnval.val1);    //学期名称
            param._3 = returnval.val2;                        //学期開始日
            param._4 = returnval.val3;                        //学期終了日
        } catch( Exception e ){
            log.warn("Semester name get error!");
        }
        getinfo = null;
        returnval = null;

        final String[] arr = {param._0, param._1, param._2, param._3, param._4, param._5, param._6, param._7, param._8, param._9, param._10};
for(int ia=0 ; ia<arr.length ; ia++) log.debug("param["+ia+"]="+arr[ia]);

    }//Set_Head()の括り



    /**SVF-FORM**/
    private boolean Set_Detail_1(DB2UDB db2,Vrw32alp svf,final Param param,String classcd
                                        ,PreparedStatement ps1,PreparedStatement ps2,PreparedStatement ps3)
    {
        boolean nonedata = false;
        ResultSet rs = null;
        try {
            int pp = 0;
            ps1.setString(++pp,classcd);    //学年
            //ps1.setString(++pp,classcd);    //学年
            //ps1.setString(++pp,classcd);    //学年 04/10/20Modify
            rs = ps1.executeQuery();
            svf.VrsOut("GRADE",    classcd);                            //学年（改ページ用）

            Map hm1 = new HashMap();                                        //学籍番号と行番号の保管
            int schno = 0;
            while( rs.next() ){
                hm1.put(rs.getString("SCHREGNO"),new Integer(++schno));        //行番号に学籍番号を付ける
                Set_Detail_1_1(svf,rs,schno);                                //生徒名等出力のメソッド
                if( schno==1 )param._7 = rs.getString("ATTENDNO2");            //開始生徒
                param._8 = rs.getString("ATTENDNO2");                        //終了生徒
                if( schno==30 ){
                    if( Set_Detail_2(db2,svf,param,hm1,ps2,ps3,classcd) )nonedata = true;//科目、欠課出力のメソッド
                    hm1.clear();                                            //行番号情報を削除
                    schno = 0;
                    param._7 = null;                                        //開始生徒
                    param._8 = null;                                        //終了生徒
                }
            }
            if( schno>0 )
                if( Set_Detail_2(db2,svf,param,hm1,ps2,ps3,classcd) )nonedata = true;//科目、欠課出力のメソッド
        } catch( Exception ex ) {
            log.warn("Set_Detail_1 read error!");
        } finally {
        	DbUtils.closeQuietly(rs);
        	db2.commit();
        }
        return nonedata;

    }//boolean Set_Detail_1()の括り



    /** 生徒名等出力 **/
    private void Set_Detail_1_1(Vrw32alp svf,ResultSet rs,int ia){

        try {
            svf.VrsOutn("HR_CLASS",ia ,rs.getString("HR_NAMEABBV"));            //組略称
            svf.VrsOutn("ATTENDNO",ia ,rs.getString("ATTENDNO"));                //出席番号
            svf.VrsOutn("NAME"    ,ia ,rs.getString("NAME"));                    //生徒名
        } catch( SQLException ex ){
            log.warn("[KNJC051]Set_Detail_1_1 rs1 svf error!");
        }

    }//Set_Detail_1_1()の括り


    // 04/10/26 Modify add
    /** 科目、欠課内容出力 **/
    private boolean Set_Detail_2(DB2UDB db2,Vrw32alp svf,final Param param,Map hm1
                                    ,PreparedStatement ps2,PreparedStatement ps3,String classcd)
    {
        boolean nonedata = false;
        try {
            int pp = 0;
            ps3.setString(++pp,classcd);                //学年
            ResultSet rs3 = ps3.executeQuery();            //科目表のレコードセット
            pp = 0;
            ps2.setString(++pp,classcd);                //学年 04/10/26Modify
            ps2.setString(++pp,param._7);                //開始生徒
            ps2.setString(++pp,param._8);                //終了生徒
            ResultSet rs = ps2.executeQuery();            //欠課者表のレコードセット

            String subclass = "0";            //科目コードの保存
            String schno = "0";                //学籍番号の保存
            int lcount = 0;                    //列出力カウント]
            int testnum = 0;                //試験回数
            int absentnum = 0;                //欠課回数

            String di_name = "";            //公欠・欠席コードの保存（中間・期末） 04/12/03Add
            String di_name_inter = "";        //公欠・欠席コードの保存（中間） 04/12/03Add
            while( rs.next() ){
                //学籍番号のブレイク 04/11/09Modify
                if( !schno.equals(rs.getString("SCHREGNO")) || !subclass.equals(rs.getString("SUBCLASSCD")) ){
                    if( !schno.equals("0") )
                        Set_Detail_2_2(svf,schno,param,hm1,di_name,di_name_inter,testnum);    //欠課内容出力のメソッド
                    schno = rs.getString("SCHREGNO");
                    testnum = 0;
                    absentnum = 0;
                }
                //科目コードのブレイク
                if( !subclass.equals(rs.getString("SUBCLASSCD")) ){
                    if( !subclass.equals("0") ){
                        svf.VrEndRecord();
                        nonedata = true;
                        lcount++;
                    }
                    subclass = rs.getString("SUBCLASSCD");
                    //欠課者なしの科目列を出力
                    for( ; rs3.next() ; ){
                        Set_Detail_2_1(svf,rs3,param);                //科目出力のメソッド
                        if( rs3.getString("SUBCLASSCD").equals(subclass) )
                            break;
                        else{
                            svf.VrEndRecord();
                            nonedata = true;
                            lcount++;
                        }
                    }
                }
                //期末試験の場合-->中間試験の欠課と合わせてカウント
                if (param._2.equals("02") && !param._1.equals("3")) {
                    if (rs.getString(param._6) != null) absentnum++;
                    if (rs.getString("TEST02") != null) testnum++;//「期末試験のみ実施する講座」NO001Modify
                    //if (rs.getString(param._9) != null) testnum++;
                    di_name_inter = rs.getString(param._6);    //公欠・欠席コードの保存 04/12/03Add
                }
                di_name = rs.getString(param._5);            //公欠・欠席コードの保存 04/12/03Add
            }
            rs.close();        //欠課者表のレコードセットをクローズ
            db2.commit();   //05/02/18
            //最後の列を出力
            if( !schno.equals("0") ){
                Set_Detail_2_2(svf,schno,param,hm1,di_name,di_name_inter,testnum);    //欠課内容出力のメソッド
                svf.VrEndRecord();
                nonedata = true;
                lcount++;
            }
            if( nonedata ){
                //残りの科目列を出力-->欠課者なし
                for( ; rs3.next() ; ){
                    Set_Detail_2_1(svf,rs3,param);                //科目出力のメソッド
                        svf.VrEndRecord();
                        lcount++;
                }
                //空列の出力-->学年で改ページ
                for( ; lcount%17>0 ; lcount++ ) svf.VrEndRecord();
            }
            Svf_Int(svf);        //SVFフィールド初期化
            rs3.close();        //科目表のレコードセットをクローズ

        } catch( Exception ex ) {
            log.warn("Set_Detail_2 read error!");
        }

        return nonedata;

    }//boolean Set_Detail_2()の括り



    /** 科目名出力 **/
    private void Set_Detail_2_1(Vrw32alp svf,ResultSet rs,final Param param){

        try {
            boolean boo_elect = false;
            //科目マスタの選択区分＝１の時、科目名を網掛けにする。
            if( rs.getString("ELECTDIV")!=null )
                if( rs.getString("ELECTDIV").equals("1") )// 04/10/20Modify 2→1
                    boo_elect = true;
            if( boo_elect ) svf.VrAttribute("SUBCLASS1"     ,"Paint=(2,70,1),Bold=1");     //網掛け
            svf.VrsOut("SUBCLASS1"        ,rs.getString("SUBCLASSABBV"));                //科目
            if( boo_elect ) svf.VrAttribute("SUBCLASS1"     ,"Paint=(0,0,0),Bold=0");   //網掛けクリア
        } catch( SQLException ex ){
            log.warn("Set_Detail_2_1 svf error!", ex);
        }

    }//Set_Detail_2_1()の括り



    /** 欠課内容出力 04/12/03**/
    private void Set_Detail_2_2(
        Vrw32alp svf,
        String schno,
        final Param param,
        Map hm1,
        String di_name,
        String di_name_inter,
        int testnum
    ) {

        try {
        //    学籍番号（生徒）に対応した行に欠課内容をセットする。（丸：欠席、三角：公欠）
            Integer int1 = (Integer)hm1.get(schno);
            if( int1!=null ){
                svf.VrAttributen("POINT1",int1.intValue(),"Hensyu=3");
                if (param._2.equals("01")) {            //中間試験の時は、白
                    if (di_name.equals("KS")) svf.VrsOutn("POINT1", int1.intValue(), "〇");
                    if (di_name.equals("KK")) svf.VrsOutn("POINT1", int1.intValue(), "△");
                } else if (param._1.equals("3")) {        //３学期は、期末試験のみ実施のため、白→黒//2005.06.24
                    if (di_name.equals("KS")) svf.VrsOutn("POINT1", int1.intValue(), "●");
                    if (di_name.equals("KK")) svf.VrsOutn("POINT1", int1.intValue(), "▲");
                } else {                                //１・２学期の期末試験の時
                    if (di_name_inter != null) {        //期末・中間試験を両方欠課は、黒
                        if (di_name.equals("KS")) svf.VrsOutn("POINT1", int1.intValue(), "●");
                        if (di_name.equals("KK")) svf.VrsOutn("POINT1", int1.intValue(), "▲");
//                    } else if (testnum == 0) {            //期末試験のみ実施する講座を欠課した場合は、黒//2005.06.24
                    } else if (testnum > 0) {            //期末試験のみ実施する講座を欠課した場合は、黒//NO001Modify
                        if (di_name.equals("KS")) svf.VrsOutn("POINT1", int1.intValue(), "●");
                        if (di_name.equals("KK")) svf.VrsOutn("POINT1", int1.intValue(), "▲");
                    } else {                            //期末試験のみ欠課は、白
                        if (di_name.equals("KS")) svf.VrsOutn("POINT1", int1.intValue(), "〇");
                        if (di_name.equals("KK")) svf.VrsOutn("POINT1", int1.intValue(), "△");
                    }
                }
            }
        } catch( Exception ex ){
            log.warn("Set_Detail_2_2 svf error!", ex);
        }
    }//Set_Detail_2_2()の括り



    /**PrepareStatement作成**/
    private String Pre_Stat1(final Param param) {

    //    生徒及び公欠・欠席者データ
        StringBuffer stb = new StringBuffer();
        stb.append("WITH SCHNO AS(");
        stb.append(    "SELECT  SCHREGNO,GRADE,HR_CLASS,ATTENDNO ");
        stb.append(       "FROM    SCHREG_REGD_DAT W1,SEMESTER_MST T1 ");//NO002
        stb.append(       "WHERE   W1.YEAR = '" + param._0 + "' AND ");
        stb.append(                 "W1.SEMESTER = '" + param._1 + "' AND ");
        stb.append(            "W1.GRADE = ? AND ");
        stb.append(                 "W1.YEAR = T1.YEAR AND ");
        stb.append(                 "W1.SEMESTER = T1.SEMESTER AND ");
        stb.append(            "NOT EXISTS( SELECT  'X' FROM SCHREG_BASE_MST S1 ");
        stb.append(                        "WHERE   S1.SCHREGNO = W1.SCHREGNO AND ");
        stb.append(                              "((S1.GRD_DIV IN ('2','3') AND ");
        stb.append(                                "S1.GRD_DATE < CASE WHEN T1.EDATE < '"+param._10+"' THEN T1.EDATE ELSE '"+param._10+"' END) OR ");
        stb.append(                               "(S1.ENT_DIV IN ('4','5') AND ");
        stb.append(                                "S1.ENT_DATE > CASE WHEN T1.EDATE < '"+param._10+"' THEN T1.EDATE ELSE '"+param._10+"' END)) ) AND ");
        stb.append(            "NOT EXISTS( SELECT  'X' FROM SCHREG_TRANSFER_DAT S1 ");
        stb.append(                        "WHERE   S1.SCHREGNO = W1.SCHREGNO AND ");
        stb.append(                               "(S1.TRANSFERCD IN ('1','2') AND ");
        stb.append(                                "CASE WHEN T1.EDATE < '"+param._10+"' THEN T1.EDATE ELSE '"+param._10+"' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE)) ");
        stb.append(    "), ");

        stb.append("RECORD_REC AS(");
        stb.append(    "SELECT  SCHREGNO ");
        stb.append(    "FROM    KIN_RECORD_DAT W1 ");
        stb.append(    "WHERE   YEAR = '" + param._0 + "' AND ");
        stb.append(             param._5 + " IN ('KS','KK') ");
        stb.append(    "GROUP BY SCHREGNO ");
        stb.append(    ") ");

        stb.append("SELECT  HR_NAMEABBV, T2.GRADE, T2.HR_CLASS, T2.ATTENDNO, T4.NAME, ");
        stb.append(           "T2.SCHREGNO, T2.GRADE||T2.HR_CLASS||T2.ATTENDNO AS ATTENDNO2 ");
        stb.append("FROM    SCHNO T2, ");
        stb.append(           "SCHREG_REGD_HDAT T3, ");
        stb.append(           "SCHREG_BASE_MST T4 ");
        stb.append("WHERE   T3.YEAR = '" + param._0 + "' AND ");
        stb.append(        "T3.SEMESTER = '" + param._1 + "' AND ");
        stb.append(           "T3.GRADE = T2.GRADE AND ");
        stb.append(        "T3.HR_CLASS = T2.HR_CLASS AND ");
        stb.append(           "T4.SCHREGNO = T2.SCHREGNO AND ");
        stb.append(           "EXISTS(SELECT 'X' FROM RECORD_REC T1 WHERE T1.SCHREGNO = T2.SCHREGNO) ");
        stb.append("ORDER BY T2.GRADE, T2.HR_CLASS, T2.ATTENDNO ");
        return stb.toString();

    }//Pre_Stat1()の括り



    /** 試験日欠課者データPrepareStatement作成 **/
    private String Pre_Stat2(final Param param) {
        StringBuffer stb = new StringBuffer();
        stb.append("WITH SCHNO AS(");
        stb.append(    "SELECT  SCHREGNO,GRADE,HR_CLASS,ATTENDNO ");
        stb.append(       "FROM    SCHREG_REGD_DAT W1,SEMESTER_MST T1 ");//NO002
        stb.append(       "WHERE   W1.YEAR = '" + param._0 + "' AND ");
        stb.append(                 "W1.SEMESTER = '" + param._1 + "' AND ");
        stb.append(            "W1.GRADE = ? AND ");
        stb.append(            "W1.GRADE||W1.HR_CLASS||W1.ATTENDNO >= ? AND  ");
        stb.append(            "W1.GRADE||W1.HR_CLASS||W1.ATTENDNO <= ? AND  ");
        stb.append(                 "W1.YEAR = T1.YEAR AND ");
        stb.append(                 "W1.SEMESTER = T1.SEMESTER AND ");
        stb.append(            "NOT EXISTS( SELECT  'X' FROM SCHREG_BASE_MST S1 ");
        stb.append(                        "WHERE   S1.SCHREGNO = W1.SCHREGNO AND ");
        stb.append(                              "((S1.GRD_DIV IN ('2','3') AND ");
        stb.append(                                "S1.GRD_DATE < CASE WHEN T1.EDATE < '"+param._10+"' THEN T1.EDATE ELSE '"+param._10+"' END) OR ");
        stb.append(                               "(S1.ENT_DIV IN ('4','5') AND ");
        stb.append(                                "S1.ENT_DATE > CASE WHEN T1.EDATE < '"+param._10+"' THEN T1.EDATE ELSE '"+param._10+"' END)) ) AND ");
        stb.append(            "NOT EXISTS( SELECT  'X' FROM SCHREG_TRANSFER_DAT S1 ");
        stb.append(                        "WHERE   S1.SCHREGNO = W1.SCHREGNO AND ");
        stb.append(                               "(S1.TRANSFERCD IN ('1','2') AND ");
        stb.append(                                "CASE WHEN T1.EDATE < '"+param._10+"' THEN T1.EDATE ELSE '"+param._10+"' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE)) ");
        stb.append(    "), ");

        stb.append("RECORD_REC AS(");
        stb.append(    "SELECT  SUBCLASSCD, SCHREGNO, CHAIRCD,  ");
        stb.append(            "SEM1_INTER_REC, SEM1_TERM_REC, SEM1_INTER_REC_DI, SEM1_TERM_REC_DI, ");
        stb.append(            "SEM2_INTER_REC, SEM2_TERM_REC, SEM2_INTER_REC_DI, SEM2_TERM_REC_DI, ");
        stb.append(                            "SEM3_TERM_REC,                    SEM3_TERM_REC_DI ");
        stb.append(    "FROM    KIN_RECORD_DAT W1 ");
        stb.append(    "WHERE   YEAR = '" + param._0 + "' AND ");
        stb.append(             param._5 + " IN ('KS','KK') ");
        stb.append(    ") ");

        //「期末試験のみ実施する講座」を判断する表 NO001Add
        stb.append(",TEST AS ( ");
        stb.append("    SELECT SUBCLASSCD AS SUBCLASSCD_B, SCHREGNO AS SCHREGNO_B, ");
        stb.append("           MAX(CASE WHEN TESTKINDCD = '01' THEN TESTKINDCD END) AS TEST01, ");
        stb.append("           MAX(CASE WHEN TESTKINDCD = '02' THEN TESTKINDCD END) AS TEST02 ");
        stb.append("    FROM ");
        stb.append("        (SELECT W1.TESTKINDCD, W2.SUBCLASSCD, W3.SCHREGNO ");
        stb.append("        FROM   SCH_CHR_TEST W1, CHAIR_DAT W2, CHAIR_STD_DAT W3 ");
        stb.append("        WHERE  W1.YEAR = '" + param._0 + "' AND ");
        stb.append("               W1.SEMESTER = '" + param._1 + "' AND ");
        stb.append("               W1.YEAR = W2.YEAR AND ");
        stb.append("               W1.SEMESTER = W2.SEMESTER AND ");
        stb.append("               W1.CHAIRCD = W2.CHAIRCD AND ");
        stb.append("               W1.YEAR = W3.YEAR AND ");
        stb.append("               W1.SEMESTER = W3.SEMESTER AND ");
        stb.append("               W1.CHAIRCD = W3.CHAIRCD AND ");
        stb.append("               W1.EXECUTEDATE BETWEEN W3.APPDATE AND W3.APPENDDATE ");
        stb.append("        GROUP BY W1.TESTKINDCD, W2.SUBCLASSCD, W3.SCHREGNO) T1 ");
        stb.append("    GROUP BY SUBCLASSCD, SCHREGNO ) ");

        stb.append("SELECT  SUBCLASSCD, SCHREGNO, CHAIRCD, T2.TEST02, ");//NO001Add
        stb.append(        "SEM1_INTER_REC, SEM1_TERM_REC, SEM1_INTER_REC_DI, SEM1_TERM_REC_DI, ");
        stb.append(        "SEM2_INTER_REC, SEM2_TERM_REC, SEM2_INTER_REC_DI, SEM2_TERM_REC_DI, ");
        stb.append(                        "SEM3_TERM_REC,                    SEM3_TERM_REC_DI ");
        stb.append("FROM    RECORD_REC T1 ");
        stb.append("       LEFT JOIN TEST T2 ON T2.SUBCLASSCD_B = T1.SUBCLASSCD AND ");         //NO001Add
        stb.append("                            T2.SCHREGNO_B = T1.SCHREGNO AND ");             //NO001Add
        stb.append("                            T2.TEST01 IS NULL AND T2.TEST02 IS NOT NULL "); //NO001Add
        stb.append("WHERE   SCHREGNO IN (SELECT SCHREGNO FROM SCHNO) ");
        stb.append("ORDER BY SUBCLASSCD, SCHREGNO ");
        return stb.toString();

    }//Pre_Stat2()の括り


    /** 試験科目PrepareStatement作成 **/
    private String Pre_Stat3(final Param param) {
        StringBuffer stb = new StringBuffer();
        /* 2005/02/18Modify yamasihro 異動者を除外 */
        stb.append("WITH SCHNO AS(");
        stb.append(    "SELECT  SCHREGNO,GRADE,HR_CLASS,ATTENDNO ");
        stb.append(       "FROM    SCHREG_REGD_DAT W1,SEMESTER_MST T1 ");//NO002
        stb.append(       "WHERE   W1.YEAR = '" + param._0 + "' AND ");
        stb.append(                 "W1.SEMESTER = '" + param._1 + "' AND ");
        stb.append(            "W1.GRADE = ? AND ");
        //NO002Modify----------↓----------
        stb.append(                 "W1.YEAR = T1.YEAR AND ");
        stb.append(                 "W1.SEMESTER = T1.SEMESTER AND ");
        stb.append(            "NOT EXISTS( SELECT  'X' FROM SCHREG_BASE_MST S1 ");
        stb.append(                        "WHERE   S1.SCHREGNO = W1.SCHREGNO AND ");
        stb.append(                              "((S1.GRD_DIV IN ('2','3') AND ");
        stb.append(                                "S1.GRD_DATE < CASE WHEN T1.EDATE < '"+param._10+"' THEN T1.EDATE ELSE '"+param._10+"' END) OR ");
        stb.append(                               "(S1.ENT_DIV IN ('4','5') AND ");
        stb.append(                                "S1.ENT_DATE > CASE WHEN T1.EDATE < '"+param._10+"' THEN T1.EDATE ELSE '"+param._10+"' END)) ) AND ");
        stb.append(            "NOT EXISTS( SELECT  'X' FROM SCHREG_TRANSFER_DAT S1 ");
        stb.append(                        "WHERE   S1.SCHREGNO = W1.SCHREGNO AND ");
        stb.append(                               "(S1.TRANSFERCD IN ('1','2') AND ");
        stb.append(                                "CASE WHEN T1.EDATE < '"+param._10+"' THEN T1.EDATE ELSE '"+param._10+"' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE)) ");
//        stb.append(            "NOT EXISTS( SELECT  'X' FROM SCHREG_BASE_MST S1 ");
//        stb.append(                        "WHERE   S1.SCHREGNO = W1.SCHREGNO AND ");
//        stb.append(                                "S1.GRD_DIV IN ('2','3') AND ");
//        stb.append(                                "S1.GRD_DATE < '" + param._10 + "' ) AND ");//---NO011
//        stb.append(            "NOT EXISTS( SELECT  'X' FROM SCHREG_TRANSFER_DAT S1 ");
//        stb.append(                        "WHERE   S1.SCHREGNO = W1.SCHREGNO AND ");
//        stb.append(                                "((S1.TRANSFERCD IN ('1','2') AND ");  // 05/02/10停学を除外
//        stb.append(                                   "'" + param._10 + "' BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE) OR ");
//        stb.append(                                 "(S1.TRANSFERCD IN ('4') AND '" + param._10 + "' < S1.TRANSFER_SDATE)) ) ");
        //NO002Modify----------↑----------
        stb.append(    "), ");

        stb.append("RECORD_REC AS(");
        stb.append(    "SELECT  SUBCLASSCD, SCHREGNO ");
        stb.append(    "FROM    KIN_RECORD_DAT W1 ");
        stb.append(    "WHERE   YEAR = '" + param._0 + "' AND ");
        stb.append(             param._5 + " IN ('KS','KK') ");
        stb.append(    "GROUP BY SUBCLASSCD, SCHREGNO ");
        stb.append(    ") ");

        stb.append("SELECT  T1.SUBCLASSCD, MIN(T2.SUBCLASSABBV)AS SUBCLASSABBV, MIN(T2.ELECTDIV)AS ELECTDIV ");
        stb.append("FROM    RECORD_REC T1, SUBCLASS_MST T2 ");
        stb.append("WHERE   SCHREGNO IN (SELECT SCHREGNO FROM SCHNO) AND ");
        stb.append(        "T1.SUBCLASSCD = T2.SUBCLASSCD ");
        stb.append("GROUP BY T1.SUBCLASSCD ");
        stb.append("ORDER BY T1.SUBCLASSCD ");
        return stb.toString();

    }//Pre_Stat3()の括り

    /**SVF-FORM-FIELD-INZ**/
    private void Svf_Int(Vrw32alp svf){

        for (int j=1; j<31; j++){
            svf.VrsOutn("HR_CLASS"        ,j     , "" );
            svf.VrsOutn("ATTENDNO"        ,j     , "" );
            svf.VrsOutn("NAME"            ,j     , "" );
            svf.VrsOutn("ABSENT"            ,j     , "" );
        }

    }//Svf_Int()の括り

    private static class Param {
    	final String _0;
    	final String _1;
    	final String _2;
    	String _3;
    	String _4;
    	String _5;
    	String _6;
    	String _7;
    	String _8;
    	String _9;
    	final String _10;
    	
    	Param(final HttpServletRequest request) {
    		_0 = request.getParameter("YEAR");                                 //年度
    		_1 = request.getParameter("GAKKI");                               //学期
    		_2 = (request.getParameter("TESTKINDCD")).substring(0,2);         //0101:中間/0201:期末
    		_10 = KNJ_EditDate.H_Format_Haifun( request.getParameter("DATE") );//異動基準日 05/02/18Modify yamashiro
    		
            //各学期成績出欠情報 04/10/20Modify
            if(_2.equals("01")){                        //中間
                _5 = "SEM"+_1+"_INTER_REC_DI";
            } else {                                        //期末
                _5 = "SEM"+_1+"_TERM_REC_DI";
                _6 = "SEM"+_1+"_INTER_REC_DI";
                _9 = "SEM"+_1+"_INTER_REC";
            }
    	}
    }

}//クラスの括り
