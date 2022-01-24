/**
 *
 *    学校教育システム 賢者 [成績管理]
 *
 *                    ＜ＫＮＪＤ３０７＞  中間・期末試験・学期成績の補点報告書
 *
 *    2004/08/27 yamashiro・新規作成
 *                        ・フォームは１ページ当り３０行(生徒)１７列(科目)とする
 *    2004/10/01 yamashiro・DB2のSQLにおいてINT関数の不具合を修正
 *    2004/10/29 nakamoto 出欠はkin_record_datにもたすような仕様に変更。この対応でattend_dat,testscore_hdat部分を修正。
 *                        文言を追加。（出欠者と異動情報者が同一の場合、異動情報を優先。その場合、○●を表示しない。）
 *    2004/11/01 nakamoto 文言の条件を変更。（学期内に異動しても文言を表示）
 *    2004/12/06 nakamoto 異動生徒の判定は、指示画面からのパラメータ（異動対象日付）を基準。（学期終了日は基準としない）
 *                        欠課状況の記号とコメントの文言を変更
 *                        公欠表示をカット。項目名を異動に変更
 *    2004/12/07 nakamoto 学期成績の場合、成績フラグが１の生徒を出力（仕様が決まっていないため、とりあえず）
 *    2004/12/16 nakamoto 学期成績の場合、
 *                        ・１学期指定の時、報告書なし（出力しない）
 *                        ・２学期指定の時、１学期中間・期末欠席者または２学期中間・期末欠席者が対象（１学期成績または２学期成績を出力）
 *                        ・３学期指定の時、３学期期末欠席者が対象（３学期成績を出力）
 *    2004/12/18 nakamoto 文言の日付を追加
 *    2005/01/24 nakamoto 指示画面で出力したい帳票（中間試験・期末試験・学期成績）を指定する方法に変更---NO001
 *    2005/02/05 nakamoto db2.commit追加。処理速度改善
 *    2005/02/14 nakamoto 出廷は、対象 NO002
 *    2005/02/18 nakamoto 異動の条件に「編入以外は、学期開始日<=異動開始日」「編入は、異動開始日<=学期終了日」を追加 NO003
 *                        文言の日付の表示位置を変更---NO004
 *    2005/03/09 nakamoto 学期成績で、「期末のみテストを実施する科目」が出力されない不具合を修正---NO005(とりあえず、以下の条件に変更した)
 *                        ・２学期指定の時、２学期中間または期末欠席者で、２学期成績フラグ'1'が対象（１学期成績または２学期成績を出力）
 *                        ・３学期指定の時、３学期期末欠席者で、３学期期末フラグ'1'が対象（３学期期末成績を出力）
 *  2005/06/24 nakamoto 期末試験のみ実施する講座を欠課した場合は、黒●とする。(期末テストのみ実施テストとは、中間にkk、ksがなくて、中間素点に点数がない)
 ***********************************************************************************************
 *  2005/10/20 nakamoto 編入のデータ仕様変更および在籍異動条件に転入学を追加---NO025
 *  2005/12/17 yamashiro ---NO006 学期成績における１学期の印刷を可とする
 *                                対象となるデータを、「中間・期末のフラグがNULLでも０でもなく、学期のフラグが１」の条件に変更
 **/

package servletpack.KNJD;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_Get_Info;


public class KNJD307K {

    private static final Log log = LogFactory.getLog(KNJD307K.class);

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws ServletException, IOException
    {
        Vrw32alp svf = new Vrw32alp();             //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                        //Databaseクラスを継承したクラス
        String param[] = new String[11];

        log.fatal("$Revision: 67196 $ $Date: 2019-04-26 00:14:31 +0900 (金, 26 4 2019) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);

    //    パラメータの取得
        String classcd[] = request.getParameterValues("GRADE");               //学年
        try {
            param[0] = request.getParameter("YEAR");                                 //年度
            param[1] = request.getParameter("GAKKI");                               //学期
            String idobi = request.getParameter("DATE");                               //異動対象日付 04/12/06Add
            param[4] = idobi.replace('/','-');
            param[9] = request.getParameter("OUTPUT");                                 //帳票種別 01:02:99---NO001
        } catch( Exception ex ) {
            log.warn("parameter error!");
        }

    //    print設定
        PrintWriter out = new PrintWriter (response.getOutputStream());
        response.setContentType("application/pdf");
        OutputStream outstrm = response.getOutputStream();

    //    svf設定
        int ret = svf.VrInit();                               //クラスの初期化
        ret = svf.VrSetSpoolFileStream(outstrm);           //PDFファイル名の設定

    //    ＤＢ接続
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
        } catch( Exception ex ) {
            log.error("DB2 open error!");
            return;
        }

    //    ＳＶＦ作成処理
        boolean nonedata = false;                                 //該当データなしフラグ
        Set_Head(db2,svf,param);                                //見出し出力のメソッド
for(int ia=0 ; ia<param.length ; ia++) log.debug("param["+ia+"]="+param[ia]);
        //SVF出力
        for( int ia=0 ; ia<classcd.length ; ia++ )
            if( Set_Detail_1(db2,svf,param,classcd[ia]) )nonedata = true;

    //    該当データ無し
        if( !nonedata ){
            ret = svf.VrSetForm("MES001.frm", 0);
            ret = svf.VrsOut("note" , "");
            ret = svf.VrEndPage();
        }

    //     終了処理
        ret = svf.VrQuit();
        db2.commit();
        db2.close();                //DBを閉じる
        outstrm.close();            //ストリームを閉じる 

    }//doGetの括り



    /** 見出し項目 **/
    private void Set_Head(DB2UDB db2,Vrw32alp svf,String param[]){

        KNJ_Get_Info getinfo = new KNJ_Get_Info();
        KNJ_Get_Info.ReturnVal returnval = null;
        int ret = 0;
        ret = svf.VrSetForm("KNJD_KYOTU.frm", 4);
        ret = svf.VrsOut("NENDO2",nao_package.KenjaProperties.gengou(Integer.parseInt(param[0]))+"年度");    //年度

    //    ＳＶＦ属性変更--->改ページ
        ret = svf.VrAttribute("GRADE","FF=1");

    //    作成日(現在処理日)の取得
        try {
            returnval = getinfo.Control(db2);
            ret = svf.VrsOut("DATE",KNJ_EditDate.h_format_JP(returnval.val3));        //作成日
        } catch( Exception e ){
            log.warn("ctrl_date get error!");
        }
    //    学期名称の取得
        try {
            returnval = getinfo.Semester(db2,param[0],param[1]);
            ret = svf.VrsOut("SEMESTER2",returnval.val1);    //学期名称
            param[3] = returnval.val2;                        //学期開始日
            param[10] = returnval.val3;                        //学期終了日---NO003
        } catch( Exception e ){
            log.warn("Semester name get error!");
        }
        getinfo = null;
        returnval = null;

    }//Set_Head()の括り



    /** 学年ごとで中間・期末・学期の補てん報告書を印刷する **/
    private boolean Set_Detail_1(DB2UDB db2,Vrw32alp svf,String param[],String classcd)
    {
        boolean nonedata = false;

        //中間試験の補てん報告書印刷
        if( param[9].equals("01") ) //---NO001
            if( !param[1].equals("3") ) 
                if( Set_Detail_2(1,db2,svf,param,classcd) )nonedata = true;
        //期末試験の補てん報告書印刷
        if( param[9].equals("02") ) //---NO001
            if( Set_Detail_2(2,db2,svf,param,classcd) )nonedata = true;
        //学期成績の補てん報告書印刷
        if( param[9].equals("99") ) //---NO001
            //NO006 if( !param[1].equals("1") ) // 04/12/16Add
                if( Set_Detail_2(3,db2,svf,param,classcd) )nonedata = true;

        return nonedata;

    }//boolean Set_Detail_1()の括り



    /** 該当学年の補てん報告書を印刷する(int pdivにより中間・期末・学期を区別する) **/
    private boolean Set_Detail_2(int pdiv,DB2UDB db2,Vrw32alp svf,String param[],String classcd)
    {
        boolean nonedata = false;
        int ret = 0;
        if( pdiv==1 ){                                //中間
            ret = svf.VrsOut("PRGID","KNJD307_1");
            ret = svf.VrsOut("TITLE2","中間試験補点報告書");
            ret = svf.VrsOut("NOTE1","〇中間のみ欠席で受験していない場合");
            ret = svf.VrsOut("NOTE2","△中間のみ公欠で受験していない場合");

            param[5] = "SEM"+param[1]+"_INTER_REC";
        } else if( pdiv==2 ){                        //期末
            ret = svf.VrsOut("PRGID","KNJD307_2");
            ret = svf.VrsOut("TITLE2","期末試験補点報告書");
            ret = svf.VrsOut("NOTE1","〇期末のみ欠席で受験していない場合、但し中間でも欠課している場合は●です");
            ret = svf.VrsOut("NOTE2","△期末のみ公欠で受験していない場合、但し中間でも欠課している場合は▲です");

            param[5] = "SEM"+param[1]+"_TERM_REC";
        } else{
            ret = svf.VrsOut("PRGID","KNJD307_3");
            ret = svf.VrsOut("TITLE2","学期成績補点報告書");
            ret = svf.VrsOut("NOTE1","(1)１学期の補点          (2)２学期の補点          (3)３学期の補点");
            ret = svf.VrsOut("NOTE2","");
        }

        //SQL作成
        PreparedStatement ps1 = null;

        try {
            ps1 = db2.prepareStatement(Pre_Stat1(param,pdiv));        //生徒及び公欠・欠席者
            int pp = 0;
            ps1.setString(++pp,classcd);    //学年 04/10/29Modify
            ps1.setString(++pp,classcd);    //学年 04/10/29Modify
            ps1.setString(++pp,classcd);    //学年 04/10/29Modify
            ps1.setString(++pp,classcd);    //学年
            ps1.setString(++pp,classcd);    //学年
            //ps1.setString(++pp,classcd);    //学年 NO025
            ResultSet rs = ps1.executeQuery();
            ret = svf.VrsOut("GRADE",    classcd);                            //学年（改ページ用）

            Map hm1 = new HashMap();                                        //学籍番号と行番号の保管
            int schno = 0;
            String schregno = "";//学籍番号 04/10/29Modify
            while( rs.next() ){
                schregno = rs.getString("SCHREGNO");
                if (!rs.getString("DI_NAME").equals("KS"))
                    schregno = "00000000";//異動生徒（留学・休学・停学・編入）なら行番号に学籍番号"00000000"をセット

                hm1.put(schregno,new Integer(++schno));                        //行番号に学籍番号を付ける
                Set_Detail_2_1(svf,rs,schno);                                //生徒名等出力のメソッド
                if( schno==1 )param[7] = rs.getString("ATTENDNO2");            //開始生徒
                param[8] = rs.getString("ATTENDNO2");                        //終了生徒
                if( schno==30 ){
                    if( Set_Detail_3(pdiv,db2,svf,param,hm1,classcd) )nonedata = true;//科目、欠課出力のメソッド
                    hm1.clear();                                            //行番号情報を削除
                    schno = 0;
                    param[7] = null;                                        //開始生徒
                    param[8] = null;                                        //終了生徒
                }
            }
            rs.close();
            ps1.close();//SQL close
            db2.commit();//05.02.05
            if( schno>0 )
                if( Set_Detail_3(pdiv,db2,svf,param,hm1,classcd) )nonedata = true;//科目、欠課出力のメソッド
        } catch( Exception ex ) {
            log.warn("Set_Detail_2 read error!");
        }

        return nonedata;

    }//boolean Set_Detail_2()の括り



    /** 生徒名等出力 **/
    private void Set_Detail_2_1(Vrw32alp svf,ResultSet rs,int ia){

        try {
            int ret = 0;
            ret = svf.VrsOutn("HR_CLASS",ia ,rs.getString("HR_NAMEABBV"));            //組略称
            ret = svf.VrsOutn("ATTENDNO",ia ,rs.getString("ATTENDNO"));                //出席番号
            ret = svf.VrsOutn("NAME"    ,ia ,rs.getString("NAME"));                    //生徒名
            //公欠カット 04/12/06Modify
            String di_name= "";
            if (!rs.getString("DI_NAME").equals("KS")) 
                di_name= rs.getString("DI_NAME");//異動情報（留学・休学・停学・編入）
            ret = svf.VrsOutn("ABSENT"  ,ia ,di_name);//公欠区分
            //ret = svf.VrsOutn("ABSENT"  ,ia ,( rs.getInt("DI_NAME")==1 )?"公":"欠");//公欠区分

            //異動日付をセット 04/12/18Add
            String mongon= "";//---NO004
            if (!rs.getString("DI_NAME").equals("KS")) {
                String di_date = rs.getString("DI_DATE");                //2004-10-05
                String wareki  = KNJ_EditDate.h_format_JP_N(di_date);    //平成16年
                
				if (wareki.substring(2).startsWith("元")) {
					mongon  = wareki.substring(2,3) + di_date.substring(5,7) + di_date.substring(8);    //161005
				} else {
					mongon  = wareki.substring(2,4) + di_date.substring(5,7) + di_date.substring(8);    //161005
				}
                //ret = svf.VrsOutn("POINT1"    ,ia        ,mongon);        //異動日付
            }
            ret = svf.VrsOutn("TRANSFER",ia        ,mongon);            //異動日付---NO004
        } catch( SQLException ex ){
            log.warn("Set_Detail_2_1 rs1 svf error!");
            return;
        }

    }//Set_Detail_2_1()の括り



    /** 科目、欠課内容出力(int pdivにより中間・期末・学期を区別する) **/
    private boolean Set_Detail_3(int pdiv,DB2UDB db2,Vrw32alp svf,String param[],Map hm1,String classcd)
    {
        boolean nonedata = false;
        int ret = 0;

        //SQL作成
        PreparedStatement ps2 = null;
        PreparedStatement ps3 = null;

        try {
log.debug("Set_Detail_3 param[7] ="+param[7]+"  param[8]="+param[8]);
            int pp = 0;
            ps3 = db2.prepareStatement(Pre_Stat3(param,pdiv));        //科目
            //ps3.setString(++pp,classcd);            //学年 04/12/18Del
            ResultSet rs3 = ps3.executeQuery();        //科目表のレコードセット
            pp = 0;
            ps2 = db2.prepareStatement(Pre_Stat2(param,pdiv));        //試験日欠課
            ps2.setString(++pp,classcd);            //学年 04/10/29Modify  04/12/18Modify
            //ps2.setString(++pp,param[7]);            //開始生徒 04/12/18Del
            //ps2.setString(++pp,param[8]);            //終了生徒 04/12/18Del
            ResultSet rs = ps2.executeQuery();        //欠課者表のレコードセット

            String subclass = "0";            //科目コードの保存
            String schno = "0";                //学籍番号の保存
            int lcount = 0;                    //列出力カウント]
            int testnum = 0;                //テスト種別回数 (または学期成績フラグとする testnum==100)
            int absentnum = 0;                //欠席回数
            int hoten = -2;                    //補点-->初期値は-2とする

            String di_name = "";            //公欠・欠席コードの保存（中間・期末） 04/12/06Add
            String di_name_inter = "";        //公欠・欠席コードの保存（中間） 04/12/06Add
            while( rs.next() ){
                //学籍番号のブレイク
                if( !schno.equals(rs.getString("SCHREGNO")) || !subclass.equals(rs.getString("SUBCLASSCD")) ){
                    if( !schno.equals("0") )
                        Set_Detail_3_2(svf,schno,param,hm1,di_name,di_name_inter,testnum,hoten,absentnum,pdiv);    //補点&欠課内容セットのメソッド
                    schno = rs.getString("SCHREGNO");            //学籍番号の保存
                    testnum = 0; absentnum = 0;    hoten = -2;        //初期値にセット
                }
                //科目コードのブレイク
                if( !subclass.equals(rs.getString("SUBCLASSCD")) ){
                    if( !subclass.equals("0") ){
                        ret = svf.VrEndRecord();
                        nonedata = true;
                        lcount++;                                //出力列カウント
                    }
                    subclass = rs.getString("SUBCLASSCD");
                    //欠課者なしの科目列を出力
                    for( ; rs3.next() ; ){
                        //Set_Detail_3_1(svf,rs3,param);                //科目出力のメソッド
                        if( rs3.getString("SUBCLASSCD").equals(subclass) ){
                            Set_Detail_3_1(svf,rs3,param);                //科目出力のメソッド
                            break;
                        //else{
                            // 04/12/18Del
                            //ret = svf.VrEndRecord();
                            //nonedata = true;
                            //lcount++;
                        }
                    }
/*                    //科目名をセット
                    for( ; subclass2<subclass ; ){
                        if( stop++>50 )break;
                        if( rs3.next() ){
                            subclass2 = rs3.getInt("SUBCLASSCD");                    //科目名出力用の科目コードの保存
                            subclassname = rs3.getString("SUBCLASSABBV");            //科目名の保存
                            if( rs3.getString("ELECTDIV")!=null )    electdiv = rs3.getInt("ELECTDIV");
                            else                                    electdiv = 0;
                            Set_Detail_3_1(svf,param,subclassname,electdiv);        //科目名セットのメソッド
                            if( subclass2<subclass ){        //データ出力用にない科目列を出力する
                                ret = svf.VrEndRecord(); 
                                nonedata = true; 
                                lcount++;
                            }
                            if( subclass2==subclass )break;//科目名出力用とデータ出力用の科目コードが一致なら抜ける
                        }
                    }    */
                }
                if( pdiv==3 ){            //学期成績 04/12/16Modify
                    if (param[1].equals("3")) {
                            if (rs.getString("SEM"+param[1]+"_TERM_REC_FLG")!=null 
                                    && rs.getInt("SEM"+param[1]+"_TERM_REC_FLG")==1 ){
                                hoten = rs.getInt("SEM"+param[1]+"_TERM_REC");
                                absentnum = Integer.parseInt(param[1]);        //学期
                            }
                    } else {
                        for( int ia=Integer.parseInt(param[1]) ; ia>0 ; ia-- ){        //学期を降順に処理する
                            if( rs.getString("SEM"+String.valueOf(ia)+"_REC_FLG")!=null 
                                    && rs.getInt("SEM"+String.valueOf(ia)+"_REC_FLG")==1 ){
                                hoten = rs.getInt("SEM"+String.valueOf(ia)+"_REC");
                                absentnum = ia;        //学期
                                break;
                            }
                        }
                    }
                    /*
                    if( testnum==0 ){    //学期成績は中間・期末のデータが存在するので１回だけ処理する testnum
                        for( int ia=Integer.parseInt(param[1]) ; ia>0 ; ia-- ){        //学期を降順に処理する
                            if( rs.getString("SEM"+param[1]+"_REC_FLG")!=null 
                                    && rs.getInt("SEM"+param[1]+"_REC_FLG")==1 ){
                                hoten = rs.getInt("SEM"+param[1]+"_REC");
                                absentnum = ia;        //学期
                                testnum = 100;        //testnum==100は処理済み
                                break;
                            }
                        }
                    }
                    */
                } else{                    //中間・期末成績
                    //期末試験の場合-->中間試験の欠課と合わせてカウント 04/12/06Add
                    if (pdiv == 2 && !param[1].equals("3")) {
                        di_name_inter = rs.getString("SEM"+param[1]+"_INTER_REC_DI");
                        if (rs.getString("SEM"+param[1]+"_INTER_REC") != null) testnum++;//2005.06.24
                    }
                    di_name = rs.getString(param[5]+"_DI");
                    if (rs.getInt(param[5]+"_FLG") == 1) hoten = rs.getInt(param[5]);    //補充 04/12/06
                    /*
                    //中間試験の場合
                    if (pdiv == 1) {
                        if (rs.getInt("SEM"+param[1]+"_INTER_REC_FLG") == 1)
                            hoten = rs.getInt("SEM"+param[1]+"_INTER_REC");                        //補点
                        if (rs.getString("SEM"+param[1]+"_TERM_REC_DI") != null) absentnum++;    //期末出欠
                        if (rs.getString("SEM"+param[1]+"_TERM_REC") != null) testnum++;        //期末成績
                    }
                    //期末試験の場合
                    if (pdiv == 2) {
                        if (rs.getInt("SEM"+param[1]+"_TERM_REC_FLG") == 1)
                            hoten = rs.getInt("SEM"+param[1]+"_TERM_REC");                        //補点
                        if (pdiv == 2 && !param[1].equals("3")) {
                            if (rs.getString("SEM"+param[1]+"_INTER_REC_DI") != null) absentnum++;    //中間出欠
                            if (rs.getString("SEM"+param[1]+"_INTER_REC") != null) testnum++;        //中間成績
                        }
                    }
                    */
                }
            }
            rs.close();        //欠課者表のレコードセットをクローズ
            //最後の列を出力
            if( !schno.equals("0") ){
                Set_Detail_3_2(svf,schno,param,hm1,di_name,di_name_inter,testnum,hoten,absentnum,pdiv);    //補点&欠課内容セットのメソッド
                ret = svf.VrEndRecord(); nonedata = true; lcount++;
            }
            if( nonedata ){
                //残りの科目列を出力-->欠課者なし 04/12/18Del
                //for( ; rs3.next() ; ){
                //    Set_Detail_3_1(svf,rs3,param);            //科目名セットのメソッド
                //    ret = svf.VrEndRecord(); nonedata = true; lcount++;
                //}
                //空列の出力-->学年で改ページ
                for( ; lcount%17>0 ; lcount++ )ret = svf.VrEndRecord();
            }
            Svf_Int(svf);        //SVFフィールド初期化
            rs3.close();        //科目表のレコードセットをクローズ
            ps2.close();//SQL close
            ps3.close();//SQL close
            db2.commit();//05.02.05

        } catch( Exception ex ) {
            log.warn("Set_Detail_3 read error!");
        }

        return nonedata;

    }//boolean Set_Detail_3()の括り



    /** 科目名出力 **/
    private void Set_Detail_3_1(Vrw32alp svf,ResultSet rs,String param[]){

        try {
            int ret = 0;
            boolean boo_elect = false;
            //科目マスタの選択区分＝１の時、科目名を網掛けにする。
            if( rs.getString("ELECTDIV")!=null )
                if( rs.getString("ELECTDIV").equals("1") )// 04/10/20Modify 2→1
                    boo_elect = true;
            if( boo_elect )ret = svf.VrAttribute("SUBCLASS1"     ,"Paint=(2,60,1),Bold=1");     //網掛け
            ret = svf.VrsOut("SUBCLASS1"        ,rs.getString("SUBCLASSABBV"));                //科目
            if( boo_elect )ret = svf.VrAttribute("SUBCLASS1"     ,"Paint=(0,0,0),Bold=0");   //網掛けクリア
        } catch( Exception ex ){
            log.warn("Set_Detail_3_1 svf error!");
        }

    }//Set_Detail_3_1()の括り



    /** 補点＆欠課内容出力 **/
    private void Set_Detail_3_2(
        Vrw32alp svf,
        String schno,
        String param[],
        Map hm1,
        String di_name,
        String di_name_inter,
        int testnum,
        int hoten,
        int absentnum,
        int pdiv
    ) {
        try {
            int ret = 0;
        //    学籍番号（生徒）に対応した行に欠課内容をセットする。（丸：欠席、三角：公欠）
            Integer int1 = (Integer)hm1.get(schno);
            if( int1==null )return;
            ret = svf.VrAttributen("POINT1",int1.intValue(),"Hensyu=3");
                //--->中間OR期末試験の補充
                String stry = "";
                if (hoten > -2) {
                    String strx = "   "+String.valueOf(hoten);
                    stry = strx.substring(strx.length()-3,strx.length());
                }
                //学期末
                if ((pdiv == 3) && (absentnum > 0)) {
                    ret = svf.VrsOutn("POINT1", int1.intValue()    , "("+String.valueOf(absentnum)+")"+stry);    // 04/12/16Modify
                    return;
                }
                //中間試験の時は、白
                if (pdiv == 1) {
                    if (di_name.equals("KS")) ret = svf.VrsOutn("POINT1", int1.intValue(), "〇"+stry);
                    if (di_name.equals("KK")) ret = svf.VrsOutn("POINT1", int1.intValue(), "△"+stry);
                //３学期は、期末試験のみ実施のため、白→黒//2005.06.24
                } else if (param[1].equals("3")) {
                    if (di_name.equals("KS")) ret = svf.VrsOutn("POINT1", int1.intValue(), "●"+stry);
                    if (di_name.equals("KK")) ret = svf.VrsOutn("POINT1", int1.intValue(), "▲"+stry);
                //１・２学期の期末試験の時
                } else {
                    //期末・中間試験を両方欠課は、黒
                    if (di_name_inter != null) {
                        if (di_name.equals("KS")) ret = svf.VrsOutn("POINT1", int1.intValue(), "●"+stry);
                        if (di_name.equals("KK")) ret = svf.VrsOutn("POINT1", int1.intValue(), "▲"+stry);
                    //期末試験のみ実施する講座を欠課した場合は、黒//2005.06.24
                    } else if (testnum == 0) {
                        if (di_name.equals("KS")) ret = svf.VrsOutn("POINT1", int1.intValue(), "●"+stry);
                        if (di_name.equals("KK")) ret = svf.VrsOutn("POINT1", int1.intValue(), "▲"+stry);
                    //期末試験のみ欠課は、白
                    } else {
                        if (di_name.equals("KS")) ret = svf.VrsOutn("POINT1", int1.intValue(), "〇"+stry);
                        if (di_name.equals("KK")) ret = svf.VrsOutn("POINT1", int1.intValue(), "△"+stry);
                    }
                }
/*
            String strmaru = "";//欠課内容
            if (param[1].equals("3")) {
                strmaru = "●";
            } else {
                if( pdiv==1 ){                    //中間
                    if (absentnum > 0) {                //中間・期末とも欠課
                        strmaru = "●";
                    } else {                            //中間試験のみ欠課
                        strmaru = "〇";
                    }
                } else if( pdiv==2 ) {                                        //期末
                    if (absentnum > 0) {                //中間・期末とも欠課
                        strmaru = "●";
                    } else if (testnum > 0) {            //期末試験のみ欠課（中間は受験）
                        strmaru = "〇";
                    } else {                            //期末試験のみ欠課
                        strmaru = "●";
                    }
                }
            }
            if( pdiv==3 ){
                if( testnum==100 ){                                                    //--->学期末の補点
                    ret = svf.VrAttributen("POINT1",int1.intValue(),"Hensyu=0");
                    String strx = "   "+String.valueOf(hoten);
                    ret = svf.VrsOutn("POINT1", int1.intValue()
                                        , "("+absentnum+")"+strx.substring(strx.length()-3,strx.length()));
                }
            } else if (hoten > -2) {                                            //--->中間OR期末試験の補点
                String strx = "   "+String.valueOf(hoten);
                ret = svf.VrsOutn("POINT1"    , int1.intValue()
                                            , strmaru + strx.substring(strx.length()-3,strx.length()));
            } else {
                ret = svf.VrsOutn("POINT1", int1.intValue(), strmaru);        //欠課内容
            }
*/
        } catch( Exception ex ){
            log.warn("Set_Detail_3_2 svf error!");
        }

    }//Set_Detail_3_2()の括り



    /** 生徒及び公欠・欠席者データPrepareStatement作成 **/
    private String Pre_Stat1(String param[],int pdiv)
    {
        StringBuffer stb = new StringBuffer();
    //    学年、テスト種別をパラメータとする
        try {
            stb.append("SELECT TBL2.HR_NAMEABBV,TBL2.GRADE,TBL2.HR_CLASS,TBL2.ATTENDNO,TBL5.NAME,TBL1.DI_NAME,");
            stb.append(       "TBL1.DI_DATE,TBL2.SCHREGNO,TBL2.GRADE||TBL2.HR_CLASS||TBL2.ATTENDNO AS ATTENDNO2 ");
                        //学期内の異動情報 04/10/29Modify
            stb.append("FROM ( ");
            stb.append("       SELECT w3.schregno,w3.transfer_sdate di_date, ");        // 04/12/18Modify
            stb.append("             (select name1 from name_mst ");
            stb.append("              where namecd1='A004' and namecd2=w3.transfercd) di_name ");
            stb.append("       FROM   schreg_regd_dat w1, schreg_transfer_dat w3 ,SEMESTER_MST T1 ");//NO025
            stb.append("       WHERE  w1.year='"+param[0]+"' AND w1.semester='"+param[1]+"' AND  ");
            stb.append("              w1.grade=? and w3.schregno=w1.schregno and  ");
            //NO025Modify----------↓----------
            stb.append(                 "W1.YEAR = T1.YEAR AND ");
            stb.append(                 "W1.SEMESTER = T1.SEMESTER AND ");
            stb.append(            "(W3.TRANSFERCD IN ('1','2') AND ");
            stb.append("              '"+param[3]+"' <= w3.transfer_sdate and  ");//---NO003
            stb.append(            "CASE WHEN T1.EDATE < '"+param[4]+"' THEN T1.EDATE ELSE '"+param[4]+"' END BETWEEN W3.TRANSFER_SDATE AND W3.TRANSFER_EDATE) ");
            /***** NO025
            stb.append("            ((w3.transfercd in ('1','2') and  ");//---NO002 '3'をカット
            stb.append("              '"+param[3]+"' <= w3.transfer_sdate and  ");//---NO003
            stb.append("              w3.transfer_sdate <= '"+param[4]+"' and '"+param[4]+"' <= w3.transfer_edate) or ");
            stb.append("             (w3.transfercd in ('4') and '"+param[4]+"' < w3.transfer_sdate and ");
            stb.append("              w3.transfer_sdate <= '"+param[10]+"' )) ");//---NO003
            *****/
            stb.append("       union ");
            stb.append("       SELECT w3.schregno,w3.grd_date di_date, ");                // 04/12/18Modify
            stb.append("             (select name1 from name_mst ");
            stb.append("              where namecd1='A003' and namecd2=w3.grd_div) di_name ");
            stb.append("       FROM   schreg_regd_dat w1, schreg_base_mst w3 ,SEMESTER_MST T1 ");//NO025
            stb.append("       WHERE  w1.year='"+param[0]+"' AND w1.semester='"+param[1]+"' AND  ");
            stb.append("              w1.grade=? and w3.schregno=w1.schregno and  ");
            stb.append(                 "W1.YEAR = T1.YEAR AND ");
            stb.append(                 "W1.SEMESTER = T1.SEMESTER AND ");
            stb.append(            "(W3.GRD_DIV IN ('2','3') AND ");
            stb.append("              '"+param[3]+"' <= w3.grd_date and  ");//---NO003
            stb.append(             "W3.GRD_DATE < CASE WHEN T1.EDATE < '"+param[4]+"' THEN T1.EDATE ELSE '"+param[4]+"' END) ");
            /***** NO025
            stb.append("              '"+param[3]+"' <= w3.grd_date and  ");//---NO003
            stb.append("              w3.grd_div in ('2','3') and w3.grd_date < '"+param[4]+"'  ");//---NO011
            *****/
            stb.append("       union ");
            stb.append("       SELECT w3.schregno,w3.ENT_DATE di_date, ");                // 04/12/18Modify
            stb.append("             (select name1 from name_mst ");
            stb.append("              where namecd1='A002' and namecd2=w3.ENT_DIV) di_name ");
            stb.append("       FROM   schreg_regd_dat w1, schreg_base_mst w3 ,SEMESTER_MST T1 ");//NO025
            stb.append("       WHERE  w1.year='"+param[0]+"' AND w1.semester='"+param[1]+"' AND  ");
            stb.append("              w1.grade=? and w3.schregno=w1.schregno and  ");
            stb.append(                 "W1.YEAR = T1.YEAR AND ");
            stb.append(                 "W1.SEMESTER = T1.SEMESTER AND ");
            stb.append(            "(W3.ENT_DIV IN ('4','5') AND ");
            stb.append("              w3.ENT_DATE <= '"+param[10]+"' and  ");//---NO003
            stb.append(             "W3.ENT_DATE > CASE WHEN T1.EDATE < '"+param[4]+"' THEN T1.EDATE ELSE '"+param[4]+"' END) ");
            //NO025Modify----------↑----------
                        //試験日の出欠情報（異動生徒は省く） 04/10/29Modify
            //以下SQL修正 04/12/18Modify 処理速度改善
            stb.append("       union ");
            stb.append("       SELECT W1.schregno,'1900-10-10' di_date,'KS' DI_NAME ");    // 04/12/18Modify
            stb.append("       FROM ");
            stb.append("        (SELECT schregno ");
            stb.append("         FROM   kin_record_dat ");
            stb.append("         WHERE  year='"+param[0]+"' AND ");
        if (pdiv < 3) {
            stb.append("                "+param[5]+"_di in ('KS','KK') AND ");
            stb.append("               ("+param[5]+"_FLG='0' OR "+param[5]+"_FLG='1' OR "+param[5]+"_FLG IS NULL) ");
        } else {
            if (param[1].equals("2")) {
//---NO005        stb.append("  ((SEM1_INTER_REC_di in ('KS','KK') and SEM1_TERM_REC_di in ('KS','KK')) OR  ");
//---NO005        stb.append("   (SEM2_INTER_REC_di in ('KS','KK') and SEM2_TERM_REC_di in ('KS','KK'))) ");
//---NO006        stb.append("  (SEM2_INTER_REC_di in ('KS','KK') OR SEM2_TERM_REC_di in ('KS','KK')) AND ");
//---NO006        stb.append(      "SEM2_REC_FLG='1' ");
                stb.append(   "VALUE(SEM2_INTER_REC_FLG,'0') = '0' AND VALUE(SEM2_TERM_REC_FLG,'0') = '0' AND SEM2_REC_FLG='1' ");//---NO006
            } else if (param[1].equals("3")) {
//---NO006        stb.append(      "SEM3_TERM_REC_DI in ('KS','KK') AND SEM3_TERM_REC_FLG='1' ");//---NO005
                stb.append(   "VALUE(SEM3_TERM_REC_FLG,'0') = '0' AND SEM3_REC_FLG='1' ");//---NO006
            }
            else {  //{  }NO006 Add
                stb.append(   "VALUE(SEM1_INTER_REC_FLG,'0') = '0' AND VALUE(SEM1_TERM_REC_FLG,'0') = '0' AND SEM1_REC_FLG='1' ");//---NO006
            }
        }
            stb.append("         GROUP BY schregno ) W1, ");
            //NO025Modify----------↓----------
            stb.append(       "(SELECT W1.schregno ");
            stb.append(        "FROM   SCHREG_REGD_DAT W1,SEMESTER_MST T1 ");
            stb.append(        "WHERE  W1.YEAR = '" + param[0] + "' AND ");
            stb.append(                 "W1.SEMESTER = '" + param[1] + "' AND ");
            stb.append(            "W1.GRADE = ? AND ");
            stb.append(                 "W1.YEAR = T1.YEAR AND ");
            stb.append(                 "W1.SEMESTER = T1.SEMESTER AND ");
            stb.append(            "NOT EXISTS( SELECT 'X' FROM SCHREG_BASE_MST S1 ");
            stb.append(                        "WHERE  S1.SCHREGNO = W1.SCHREGNO AND ");
            stb.append(                             "((S1.GRD_DIV IN ('2','3') AND ");
            stb.append(                               "S1.GRD_DATE < CASE WHEN T1.EDATE < '"+param[4]+"' THEN T1.EDATE ELSE '"+param[4]+"' END) OR ");
            stb.append(                              "(S1.ENT_DIV IN ('4','5') AND ");
            stb.append(                               "S1.ENT_DATE > CASE WHEN T1.EDATE < '"+param[4]+"' THEN T1.EDATE ELSE '"+param[4]+"' END)) ) AND ");
            stb.append(            "NOT EXISTS( SELECT 'X' FROM SCHREG_TRANSFER_DAT S1 ");
            stb.append(                        "WHERE  S1.SCHREGNO = W1.SCHREGNO AND ");
            stb.append(                              "(S1.TRANSFERCD IN ('1','2') AND ");
            stb.append(                               "CASE WHEN T1.EDATE < '"+param[4]+"' THEN T1.EDATE ELSE '"+param[4]+"' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE)) ");
            stb.append(     " ) W2 ");
            stb.append("       WHERE W1.SCHREGNO=W2.SCHREGNO ");
            /*****NO025
            stb.append("        (SELECT schregno ");
            stb.append("         FROM   SCHREG_REGD_DAT ");
            stb.append("         WHERE  year='"+param[0]+"' AND SEMESTER='"+param[1]+"' AND GRADE=? ) W2 ");
            stb.append("       WHERE W1.SCHREGNO=W2.SCHREGNO AND ");
            stb.append("              w2.schregno not in ( ");
            stb.append("                    SELECT w3.schregno ");
            stb.append("                    FROM   schreg_regd_dat w1, schreg_transfer_dat w3  ");
            stb.append("                    WHERE  w1.year='"+param[0]+"' AND w1.semester='"+param[1]+"' AND  ");
            stb.append("                           w1.grade=? and w3.schregno=w1.schregno and  ");
            stb.append("                         ((w3.transfercd in ('1','2') and  ");//---NO002 '3'をカット
            stb.append("                           w3.transfer_sdate <= '"+param[4]+"' and  ");
            stb.append("                           '"+param[4]+"' <= w3.transfer_edate) or ");
            stb.append("                          (w3.transfercd in ('4') and  ");
            stb.append("                           '"+param[4]+"' < w3.transfer_sdate )) ");
            stb.append("                    union ");
            stb.append("                    SELECT w3.schregno ");
            stb.append("                    FROM   schreg_regd_dat w1, schreg_base_mst w3  ");
            stb.append("                    WHERE  w1.year='"+param[0]+"' AND w1.semester='"+param[1]+"' AND  ");
            stb.append("                           w1.grade=? and w3.schregno=w1.schregno and  ");
            stb.append("                           w3.grd_div in ('2','3') and w3.grd_date < '"+param[4]+"') ");//---NO011
            *****/
            //NO025Modify----------↑----------
            stb.append(       " )TBL1, ");
                        //学籍情報
            stb.append(     "( SELECT W1.SCHREGNO,W1.GRADE,W1.HR_CLASS,W1.ATTENDNO,W2.HR_NAMEABBV ");
            stb.append(       "FROM   SCHREG_REGD_DAT W1,SCHREG_REGD_HDAT W2 ");
            stb.append(       "WHERE  W2.YEAR='"+param[0]+"' AND W2.SEMESTER='"+param[1]+"' AND ");
            stb.append(              "W2.GRADE =? AND ");
            stb.append(              "W1.YEAR=W2.YEAR AND W1.SEMESTER=W2.SEMESTER AND ");
            stb.append(              "W1.GRADE = W2.GRADE AND W1.HR_CLASS = W2.HR_CLASS )TBL2, ");
            stb.append(     "SCHREG_BASE_MST TBL5 ");

            stb.append("WHERE TBL1.SCHREGNO=TBL2.SCHREGNO AND TBL1.SCHREGNO=TBL5.SCHREGNO ");
            stb.append("ORDER BY TBL2.GRADE,TBL2.HR_CLASS,TBL2.ATTENDNO ");
        } catch( Exception e ){
            log.warn("Pre_Stat1 error!");
        }
        return stb.toString();

    }//Pre_Stat1()の括り



    /** 試験日欠課者データPrepareStatement作成 **/
    String Pre_Stat2(String param[],int pdiv)
    {
        //学年、出力生徒範囲をパラメータとする
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("WITH KIN_REC2 AS ( ");
            stb.append("    SELECT SCHREGNO ");
            stb.append("    FROM   KIN_RECORD_DAT ");
            stb.append("    WHERE  YEAR='"+param[0]+"' AND ");
            if (pdiv < 3) {
                stb.append("       "+param[5]+"_DI in ('KS','KK') AND ");
                stb.append("       ("+param[5]+"_FLG='0' OR "+param[5]+"_FLG='1' OR "+param[5]+"_FLG IS NULL) ");
            } else {
                if (param[1].equals("2")) {
//---NO005            stb.append("  ((SEM1_INTER_REC_di in ('KS','KK') and SEM1_TERM_REC_di in ('KS','KK')) OR  ");
//---NO005            stb.append("   (SEM2_INTER_REC_di in ('KS','KK') and SEM2_TERM_REC_di in ('KS','KK'))) ");
//---NO006            stb.append("  (SEM2_INTER_REC_di in ('KS','KK') OR SEM2_TERM_REC_di in ('KS','KK')) AND ");
//---NO006            stb.append(      "SEM2_REC_FLG='1' ");
                    stb.append(   "VALUE(SEM2_INTER_REC_FLG,'0') = '0' AND VALUE(SEM2_TERM_REC_FLG,'0') = '0' AND SEM2_REC_FLG='1' ");//---NO006
                } else if (param[1].equals("3")) {
//---NO006             stb.append(      "SEM3_TERM_REC_DI in ('KS','KK') AND SEM3_TERM_REC_FLG='1' ");//---NO005
                    stb.append(   "VALUE(SEM3_TERM_REC_FLG,'0') = '0' AND SEM3_REC_FLG='1' ");//---NO006
                }
                else {  //{  }NO006 Add
                    stb.append(   "VALUE(SEM1_INTER_REC_FLG,'0') = '0' AND VALUE(SEM1_TERM_REC_FLG,'0') = '0' AND SEM1_REC_FLG='1' ");//---NO006
                }
            }
            stb.append("    GROUP BY SCHREGNO ");
            stb.append("    ), ");
            stb.append("SCHNO AS ( ");
            stb.append("    SELECT K2.SCHREGNO ");
            stb.append("    FROM   SCHREG_REGD_DAT K1,KIN_REC2 K2 ");
            stb.append("    WHERE  K2.SCHREGNO=K1.SCHREGNO AND YEAR='"+param[0]+"' AND ");
            stb.append("           SEMESTER='"+param[1]+"' AND GRADE=? ), ");
            stb.append("KIN_REC AS ( ");
            stb.append("    SELECT * ");
            stb.append("    FROM   KIN_RECORD_DAT ");
            stb.append("    WHERE  YEAR='"+param[0]+"' AND ");
            if (pdiv < 3) {
                stb.append("       "+param[5]+"_DI in ('KS','KK') AND ");
                stb.append("       ("+param[5]+"_FLG='0' OR "+param[5]+"_FLG='1' OR "+param[5]+"_FLG IS NULL) ");
            } else {
                if (param[1].equals("2")) {
//---NO005            stb.append("  ((SEM1_INTER_REC_di in ('KS','KK') and SEM1_TERM_REC_di in ('KS','KK')) OR  ");
//---NO005            stb.append("   (SEM2_INTER_REC_di in ('KS','KK') and SEM2_TERM_REC_di in ('KS','KK'))) ");
//---NO006            stb.append("  (SEM2_INTER_REC_di in ('KS','KK') OR SEM2_TERM_REC_di in ('KS','KK')) AND ");
//---NO006            stb.append(      "SEM2_REC_FLG='1' ");
                    stb.append(   "VALUE(SEM2_INTER_REC_FLG,'0') = '0' AND VALUE(SEM2_TERM_REC_FLG,'0') = '0' AND SEM2_REC_FLG='1' ");//---NO006
                } else if (param[1].equals("3")) {
//---NO006             stb.append(      "SEM3_TERM_REC_DI in ('KS','KK') AND SEM3_TERM_REC_FLG='1' ");//---NO005
                    stb.append(   "VALUE(SEM3_TERM_REC_FLG,'0') = '0' AND SEM3_REC_FLG='1' ");//---NO006
                }
                else {  //{  }NO006 Add
                    stb.append(   "VALUE(SEM1_INTER_REC_FLG,'0') = '0' AND VALUE(SEM1_TERM_REC_FLG,'0') = '0' AND SEM1_REC_FLG='1' ");//---NO006
                }
            }
            stb.append("    ) ");
            stb.append("SELECT ");
            stb.append("    w1.subclasscd, w1.schregno, w1.chaircd,  ");
            stb.append("    w1.sem1_inter_rec, w1.sem1_term_rec, w1.sem1_inter_rec_di, w1.sem1_term_rec_di, ");
            stb.append("    w1.sem2_inter_rec, w1.sem2_term_rec, w1.sem2_inter_rec_di, w1.sem2_term_rec_di, ");
            stb.append("    w1.sem3_term_rec, w1.sem3_term_rec_di, ");
            stb.append("    w1.sem1_inter_rec_flg, w1.sem1_term_rec_flg, ");
            stb.append("    w1.sem2_inter_rec_flg, w1.sem2_term_rec_flg, ");
            stb.append("    w1.sem3_term_rec_flg, ");
               stb.append("    SEM1_REC, SEM1_REC_FLG, SEM2_REC, SEM2_REC_FLG ");
            stb.append("FROM   KIN_REC W1, SCHNO W2 ");
            stb.append("WHERE  W2.SCHREGNO=W1.SCHREGNO ");
            stb.append("ORDER BY W1.SUBCLASSCD,W1.SCHREGNO ");
/* 05/02/05Modify
            stb.append("SELECT ");
            stb.append("    w1.subclasscd, w1.schregno, w1.chaircd,  ");
            stb.append("    w1.sem1_inter_rec, w1.sem1_term_rec, w1.sem1_inter_rec_di, w1.sem1_term_rec_di, ");
            stb.append("    w1.sem2_inter_rec, w1.sem2_term_rec, w1.sem2_inter_rec_di, w1.sem2_term_rec_di, ");
            stb.append("    w1.sem3_term_rec, w1.sem3_term_rec_di, ");
            stb.append("    w1.sem1_inter_rec_flg, w1.sem1_term_rec_flg, ");
            stb.append("    w1.sem2_inter_rec_flg, w1.sem2_term_rec_flg, ");
            stb.append("    w1.sem3_term_rec_flg, ");
               stb.append(       "SEM1_REC, SEM1_REC_FLG, SEM2_REC, SEM2_REC_FLG ");
            stb.append("FROM ");
            stb.append("    kin_record_dat w1, schreg_regd_dat w2 ");
            stb.append("WHERE ");
            stb.append("    w2.year='"+param[0]+"' AND w2.semester='"+param[1]+"' AND GRADE=? AND ");// 04/12/18
            //stb.append("    w2.GRADE||w2.HR_CLASS||w2.ATTENDNO >=? AND  ");
            //stb.append("    w2.GRADE||w2.HR_CLASS||w2.ATTENDNO <=? AND  ");
            stb.append("    w2.year=w1.year AND w2.schregno=w1.schregno AND  ");
        // 04/12/07Modify
        if (pdiv < 3) {
            stb.append(              param[5]+"_di in ('KS','KK') and ");//check
            stb.append("    ("+param[5]+"_FLG='0' OR "+param[5]+"_FLG='1' OR "+param[5]+"_FLG IS NULL) ");//FLG
        //学期成績のフラグ１の生徒 04/12/07Modify
        } else {
            if (param[1].equals("2")) {
                stb.append("  ((SEM1_INTER_REC_di in ('KS','KK') and SEM1_TERM_REC_di in ('KS','KK')) OR  ");
                stb.append("   (SEM2_INTER_REC_di in ('KS','KK') and SEM2_TERM_REC_di in ('KS','KK'))) ");
            } else if (param[1].equals("3")) {
                stb.append(      "SEM3_TERM_REC_DI in ('KS','KK') ");
            }
        }
            stb.append("order by w1.subclasscd, w1.schregno ");
*/
        } catch( Exception e ){
            log.warn("Pre_Stat2 error!");
        }
        return stb.toString();

    }//Pre_Stat2()の括り


    /** 試験科目PrepareStatement作成 **/
    String Pre_Stat3(String param[],int pdiv)
    {
        StringBuffer stb = new StringBuffer();
        //学年、試験種別をパラメータとする
        try {
            //以下SQL修正 04/12/18Modify 処理速度改善
            stb.append("SELECT t1.subclasscd, t2.subclassabbv, t2.electdiv  ");
            stb.append("FROM  ");
            stb.append("    (SELECT subclasscd ");
            stb.append("     FROM   kin_record_dat ");
            stb.append("     WHERE  year='"+param[0]+"' AND ");
            //stb.append("            schregno in (SELECT schregno ");
            //stb.append("                         FROM   SCHREG_REGD_DAT ");
            //stb.append("                         WHERE  year='"+param[0]+"' AND SEMESTER='"+param[1]+"' AND GRADE=? ) AND  ");
        if (pdiv < 3) {
            stb.append("            "+param[5]+"_di in ('KS','KK') AND  ");
            stb.append("           ("+param[5]+"_FLG='0' OR "+param[5]+"_FLG='1' OR "+param[5]+"_FLG IS NULL) ");
        } else {
            if (param[1].equals("2")) {
//---NO005        stb.append("  ((SEM1_INTER_REC_di in ('KS','KK') and SEM1_TERM_REC_di in ('KS','KK')) OR  ");
//---NO005        stb.append("   (SEM2_INTER_REC_di in ('KS','KK') and SEM2_TERM_REC_di in ('KS','KK'))) ");
//---NO006      stb.append("  (SEM2_INTER_REC_di in ('KS','KK') OR SEM2_TERM_REC_di in ('KS','KK')) AND ");
//---NO006        stb.append(      "SEM2_REC_FLG='1' ");
                stb.append(   "VALUE(SEM2_INTER_REC_FLG,'0') = '0' AND VALUE(SEM2_TERM_REC_FLG,'0') = '0' AND SEM2_REC_FLG='1' ");//---NO006
            } else if (param[1].equals("3")) {
//---NO006        stb.append(      "SEM3_TERM_REC_DI in ('KS','KK') AND SEM3_TERM_REC_FLG='1' ");//---NO005
                stb.append(   "VALUE(SEM3_TERM_REC_FLG,'0') = '0' AND SEM3_REC_FLG='1' ");//---NO006
            }
            else {  //{  }NO006 Add
                stb.append(   "VALUE(SEM1_INTER_REC_FLG,'0') = '0' AND VALUE(SEM1_TERM_REC_FLG,'0') = '0' AND SEM1_REC_FLG='1' ");//---NO006
            }
        }
            stb.append("     GROUP BY subclasscd ) T1, ");
            stb.append("     subclass_mst T2  ");
            stb.append("WHERE t1.subclasscd=t2.subclasscd  ");
            stb.append("ORDER BY t1.subclasscd  ");
            /* 04/12/18
            stb.append("SELECT t1.subclasscd, t2.subclassabbv, t2.electdiv ");
            stb.append("FROM ");
            stb.append("    (SELECT w1.subclasscd ");
            stb.append("     FROM   kin_record_dat w1, schreg_regd_dat w2 ");
            stb.append("     WHERE  w2.year='"+param[0]+"' AND w2.semester='"+param[1]+"' AND w2.grade =? AND  ");
            stb.append("            w2.year=w1.year AND w2.schregno=w1.schregno AND ");
        // 04/12/07Modify
        if (pdiv < 3) {
            stb.append(              param[5]+"_di in ('KS','KK') and ");//check
            stb.append("    ("+param[5]+"_FLG='0' OR "+param[5]+"_FLG='1' OR "+param[5]+"_FLG IS NULL) ");//FLG
        //学期成績のフラグ１の生徒 04/12/07Modify
        } else {
            if (param[1].equals("2")) {
                stb.append("  ((SEM1_INTER_REC_di in ('KS','KK') and SEM1_TERM_REC_di in ('KS','KK')) OR  ");
                stb.append("   (SEM2_INTER_REC_di in ('KS','KK') and SEM2_TERM_REC_di in ('KS','KK'))) ");
            } else if (param[1].equals("3")) {
                stb.append(      "SEM3_TERM_REC_DI in ('KS','KK') ");
            }
        }
            stb.append("     GROUP BY w1.subclasscd)t1, ");
            stb.append("    subclass_mst t2 ");
            stb.append("WHERE t1.subclasscd=t2.subclasscd ");
            stb.append("ORDER BY t1.subclasscd ");
            */
        } catch( Exception e ){
            log.warn("Pre_Stat3 error!");
        }
        return stb.toString();

    }//Pre_Stat3()の括り



    /**SVF-FORM-FIELD-INZ**/
    private void Svf_Int(Vrw32alp svf){

        int ret = 0;
        for (int j=1; j<31; j++){
            ret = svf.VrsOutn("HR_CLASS"        ,j     , "" );
            ret = svf.VrsOutn("ATTENDNO"        ,j     , "" );
            ret = svf.VrsOutn("NAME"            ,j     , "" );
            ret = svf.VrsOutn("ABSENT"            ,j     , "" );
            ret = svf.VrsOutn("TRANSFER"        ,j     , "" );//---NO004
        }

    }//Svf_Int()の括り



}//クラスの括り
