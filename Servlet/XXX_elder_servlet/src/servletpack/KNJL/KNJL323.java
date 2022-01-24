package servletpack.KNJL;

import java.io.IOException;
import java.io.PrintWriter;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_Get_Info;

/**
 *
 *  学校教育システム 賢者 [入試管理]
 *
 *                  ＜ＫＮＪＬ３２３＞  特待生合格者名簿・不合格者名簿
 *
 *  2004/12/21 nakamoto 作成日
 *  2004/12/27 nakamoto 総ページ数は入試区分毎に出力    NO001
 *  2005/01/13 nakamoto 受験番号○○◇◇◇の、○○の部分が変わったら改ページする    NO004
 *             nakamoto コミットが入れられるところではコミットする  NO002
 *  2005/01/14 nakamoto NO004の修正は、不合格者名簿のみ対象とする。 NO005
 *  2006/01/10 m-yama   合格者名簿出力を追加    NO006
 *  2006/01/11 m-yama   合格者名簿に特別合格を含む  NO007
 *
 **/

public class KNJL323 {

    private static final Log log = LogFactory.getLog(KNJL323.class);

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws ServletException, IOException
    {

        Vrw32alp svf = new Vrw32alp();          //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                      //Databaseクラスを継承したクラス
        String param[] = new String[5];

    //  パラメータの取得
        try {
            param[0] = request.getParameter("YEAR");                        //年度
            param[1] = request.getParameter("APDIV");                       //入試制度
            param[2] = request.getParameter("TESTDV");                      //入試区分
            param[3] = request.getParameter("OUTPUT");                      //帳票種類1:特待生合格者名簿,2:不合格者名簿,3:合格者名簿 NO006
            param[4] = request.getParameter("SUCTYPE");                     //合格種別
        } catch( Exception ex ) {
            log.error("parameter error!");
        }

    //  print設定
        PrintWriter outstrm = new PrintWriter (response.getOutputStream());
        response.setContentType("application/pdf");

    //  svf設定
        int ret = svf.VrInit();                             //クラスの初期化
        if (false && 0 != ret) { ret = 0; }
        ret = svf.VrSetSpoolFileStream(response.getOutputStream());         //PDFファイル名の設定

    //  ＤＢ接続
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
        } catch( Exception ex ) {
            log.error("DB2 open error!");
            return;
        }


    //  ＳＶＦ作成処理
        PreparedStatement ps = null;    /* NO001 */
        PreparedStatement ps1 = null;
        PreparedStatement ps2 = null;
        boolean nonedata = false;                               //該当データなしフラグ
        setHeader(db2,svf,param);
for(int i=0 ; i<param.length ; i++) log.debug("param["+i+"]="+param[i]);
        //SQL作成
        try {
            ps = db2.prepareStatement(preStat(param));          //入試区分preparestatement  /* NO001 */
            ps1 = db2.prepareStatement(preStat1(param));        //合格者一覧preparestatement
            ps2 = db2.prepareStatement(preStat2(param));        //総ページ数preparestatement
        } catch( Exception ex ) {
            log.error("DB2 prepareStatement set error!");
        }
        //SVF出力
        if (setSvfMain(db2,svf,param,ps,ps1,ps2)) nonedata = true;  //帳票出力のメソッド    /* NO001 */
        /*
        setTotalPage(svf,param,ps2);                            //総ページ数メソッド
        if( setSvfout(svf,param,ps1) ){                         //帳票出力のメソッド
            nonedata = true;
        }
        */

    //  該当データ無し
        if( !nonedata ){
            ret = svf.VrSetForm("MES001.frm", 0);
            ret = svf.VrsOut("note" , "note");
            ret = svf.VrEndPage();
        }

    //  終了処理
        ret = svf.VrQuit();
        preStatClose(ps,ps1,ps2);       //preparestatementを閉じる  /* NO001 */
        db2.commit();
        db2.close();                //DBを閉じる
        outstrm.close();            //ストリームを閉じる

    }//doGetの括り



    /** 事前処理 **/
    private void setHeader(
        DB2UDB db2,
        Vrw32alp svf,
        String param[]
    ) {
        KNJ_Get_Info getinfo = new KNJ_Get_Info();
        KNJ_Get_Info.ReturnVal returnval = null;
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        if (param[3].equals("1")) ret = svf.VrSetForm("KNJL323_1.frm", 4);
        //NO006
        if (param[3].equals("2")){
            ret = svf.VrSetForm("KNJL323_2.frm", 4);
            ret = svf.VrsOut("TITLE", "不合格");
        }
        //NO006
        if (param[3].equals("3")){
            ret = svf.VrSetForm("KNJL323_2.frm", 4);
            ret = svf.VrsOut("TITLE", "合格");
        }
        if (param[3].equals("4")){
            ret = svf.VrSetForm("KNJL323_2.frm", 4);
            ret = svf.VrsOut("TITLE", "アップ合格");
        }
        if (param[3].equals("5")){
            ret = svf.VrSetForm("KNJL323_2.frm", 4);
            ret = svf.VrsOut("TITLE", "スライド合格");
        }
        if (param[3].equals("6")){
            ret = svf.VrSetForm("KNJL323_2.frm", 4);
            ret = svf.VrsOut("TITLE", "非正規合格");
        }
        if (param[4].equals("1")) {
            ret = svf.VrsOut("SUBTITLE", "(Ｔ合格)");
        }
        if (param[4].equals("2")) {
            ret = svf.VrsOut("SUBTITLE", "(Ｓ合格)");
        }
        ret = svf.VrsOut("NENDO"    ,KenjaProperties.gengou(Integer.parseInt(param[0]))+"年度");

    //  ＳＶＦ属性変更--->改ページ
        ret = svf.VrAttribute("TESTDIV","FF=1");

    //  作成日(現在処理日)の取得
        try {
            returnval = getinfo.Control(db2);
            ret = svf.VrsOut("DATE" ,KNJ_EditDate.h_format_JP(returnval.val3));
        } catch( Exception e ){
            log.error("setHeader set error!");
        }

        getinfo = null;
        returnval = null;
    }



    /**
     *  svf print 印刷処理
     *            入試区分が指定されていれば( => param[2] !== "0" )１回の処理
     *            入試区分が複数の場合は全ての入試区分を舐める
     */ /* NO001 */
    private boolean setSvfMain(
        DB2UDB db2,
        Vrw32alp svf,
        String param[],
        PreparedStatement ps,
        PreparedStatement ps1,
        PreparedStatement ps2
    ) {
        boolean nonedata = false;

        if( ! param[2].equals("9") ){
            setTotalPage(db2,svf,param,ps2,param[2]);                           //総ページ数メソッド
            if (setSvfout(db2,svf,param,ps1,param[2])) nonedata = true;         //帳票出力のメソッド
            return nonedata;
        }

        try {
            ResultSet rs = ps.executeQuery();

            while( rs.next() ){
                setTotalPage(db2,svf,param,ps2, rs.getString("TESTDIV"));                       //総ページ数メソッド
                if (setSvfout(db2,svf,param,ps1, rs.getString("TESTDIV"))) nonedata = true;     //帳票出力のメソッド
            }
            rs.close();
            db2.commit();   /* NO002 */
        } catch( Exception ex ) {
            log.error("setSvfMain set error!");
        }
        return nonedata;
    }



    /**総ページ数をセット**/
    private void setTotalPage(
        DB2UDB db2,
        Vrw32alp svf,
        String param[],
        PreparedStatement ps2,
        String test_div
    ) {
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        try {
            int p = 0;
            ps2.setString( ++p, test_div ); /* NO001 */
            ResultSet rs = ps2.executeQuery();

            while( rs.next() ){
                if (rs.getString("TOTAL_PAGE") != null)
                    ret = svf.VrsOut("TOTAL_PAGE"   ,rs.getString("TOTAL_PAGE"));
            }
            rs.close();
            db2.commit();   /* NO002 */
        } catch( Exception ex ) {
            log.error("setTotalPage set error!");
        }

    }



    /**帳票出力（合格者一覧をセット）**/
    private boolean setSvfout(
        DB2UDB db2,
        Vrw32alp svf,
        String param[],
        PreparedStatement ps1,
        String test_div
    ) {
        boolean nonedata = false;
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        try {
            int p = 0;
            ps1.setString( ++p, test_div ); /* NO001 */
            ResultSet rs = ps1.executeQuery();

            int reccnt_man      = 0;    //男レコード数カウント用
            int reccnt_woman    = 0;    //女レコード数カウント用
            int reccnt = 0;             //合計レコード数
            int pagecnt = 1;            //現在ページ数
            int gyo = 1;                //現在ページ数の判断用（行）
            String testdiv = "0";       //現在ページ数の判断用（入試区分）

            while( rs.next() ){
                //レコードを出力
                if (reccnt > 0) ret = svf.VrEndRecord();
                //５０行超えたとき、または、入試区分ブレイクの場合、ページ数カウント
                if ((gyo > 50) || (!testdiv.equals(rs.getString("TESTDIV")) && !testdiv.equals("0"))) { /* NO004 */
                    gyo = 1;
                    pagecnt++;
                }
                //ヘッダ
                ret = svf.VrsOut("PAGE"     ,String.valueOf(pagecnt));      //現在ページ数
                ret = svf.VrsOut("TESTDIV"  ,rs.getString("TEST_NAME"));    //入試区分
                //明細
                ret = svf.VrsOut("EXAMNO"   ,rs.getString("EXAMNO"));       //受験番号
                ret = svf.VrsOut("NAME"     ,rs.getString("NAME"));         //名前
                ret = svf.VrsOut("KANA"     ,rs.getString("NAME_KANA"));    //ふりがな
                ret = svf.VrsOut("SEX"      ,rs.getString("SEX_NAME"));     //性別
                ret = svf.VrsOut("FINSCHOOL",rs.getString("FS_NAME"));      //出身学校名
                //レコード数カウント
                reccnt++;
                if (rs.getString("SEX") != null) {
                    if (rs.getString("SEX").equals("1")) reccnt_man++;
                    if (rs.getString("SEX").equals("2")) reccnt_woman++;
                }
                //現在ページ数判断用
                gyo++;
                testdiv = rs.getString("TESTDIV");

                nonedata = true;
            }
            //最終レコードを出力
            if (nonedata) {
                //最終ページに男女合計を出力
                ret = svf.VrsOut("NOTE" ,"男"+String.valueOf(reccnt_man)+"名,女"+String.valueOf(reccnt_woman)+"名,合計"+String.valueOf(reccnt)+"名");
                ret = svf.VrEndRecord();//レコードを出力
                setSvfInt(svf);         //ブランクセット    /* NO001 */
            }
            rs.close();
            db2.commit();   /* NO002 */
        } catch( Exception ex ) {
            log.error("setSvfout set error!");
        }
        return nonedata;
    }



    /**入試区分を取得**/    /* NO001 */
    private String preStat(String param[])
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("SELECT DISTINCT TESTDIV ");
            stb.append("FROM   ENTEXAM_RECEPT_DAT ");
            stb.append("WHERE  ENTEXAMYEAR='"+param[0]+"'  ");
            stb.append("ORDER BY TESTDIV ");
        } catch( Exception e ){
            log.error("preStat error!");
        }
        return stb.toString();

    }//preStat()の括り



    /**合格者一覧を取得**/
    private String preStat1(String param[])
    {
        StringBuffer stb = new StringBuffer();
    //  パラメータ（なし）
        try {
            stb.append("SELECT ");
            stb.append("    T1.TESTDIV, ");
            stb.append("    T4.NAME AS TEST_NAME, ");
            stb.append("    T1.EXAMNO, ");
            stb.append("    SUBSTR(T1.EXAMNO,1,2) EXAMNO2, ");  /* NO004 */
            stb.append("    T1.NAME, ");
            stb.append("    T1.NAME_KANA, ");
            stb.append("    T1.SEX, ");
            stb.append("    T5.ABBV1 AS SEX_NAME, ");
            stb.append("    T1.FS_NAME  ");
            stb.append("FROM ");
            stb.append("    (SELECT W1.TESTDIV, W1.EXAMNO, W2.NAME, W2.NAME_KANA, W2.SEX, W2.FS_NAME  ");
            stb.append("     FROM   ENTEXAM_RECEPT_DAT W1, ENTEXAM_APPLICANTBASE_DAT W2  ");
            stb.append("     WHERE  W1.ENTEXAMYEAR='"+param[0]+"'  ");
            stb.append("            AND W1.ENTEXAMYEAR=W2.ENTEXAMYEAR  ");
            stb.append("            AND W1.EXAMNO=W2.EXAMNO  ");

        //  '全て'以外の場合（入試制度）
            if (!param[1].equals("0"))
            stb.append("            AND W1.APPLICANTDIV='"+param[1]+"' ");

        //  '全て'以外の場合（入試区分）
//          if (!param[2].equals("0"))
//          stb.append("            AND W1.TESTDIV='"+param[2]+"' ");
            stb.append("            AND W1.TESTDIV=? ");    /* NO001 */

        //  特待生合格者名簿
            if (param[3].equals("1")) {
            stb.append("            AND W1.JUDGEDIV='1'  ");
            stb.append("            AND W1.HONORDIV='1'  ");
            }

        //  不合格者名簿
            if (param[3].equals("2"))
            stb.append("            AND W1.JUDGEDIV='2'  ");

        //  合格者名簿 NO006
            if (param[3].equals("3")) {
                stb.append("            AND W1.JUDGEDIV='1'  ");
//NO007         stb.append("            AND value(W1.HONORDIV,'0')<>'1'  ");
                if (param[4].equals("1")) {
                    //Ｔ合格
                    stb.append("            AND W1.JUDGECLASS IN ('2', '3')  ");
                }
                if (param[4].equals("2")) {
                    //Ｓ合格
                    stb.append("            AND W1.JUDGECLASS IN ('1', '4', '6')  ");
                }
            }

            //  アップ合格者名簿
            if (param[3].equals("4")) {
                stb.append("            AND W1.JUDGEDIV='1'  ");
                stb.append("            AND W1.JUDGECLASS='3'  ");
            }

            //  スライド合格者名簿
            if (param[3].equals("5")) {
                stb.append("            AND W1.JUDGEDIV='1'  ");
                stb.append("            AND W1.JUDGECLASS='4'  ");
            }

            //  非正規合格者名簿
            if (param[3].equals("6")) {
                stb.append("            AND W1.JUDGEDIV='1'  ");
                stb.append("            AND W1.JUDGECLASS='6'  ");
            }
            stb.append("            ) T1  ");
            stb.append("    LEFT JOIN ENTEXAM_TESTDIV_MST T4 ON T4.ENTEXAMYEAR='"+param[0]+"' AND T4.TESTDIV=T1.TESTDIV ");
            stb.append("    LEFT JOIN NAME_MST T5 ON T5.NAMECD1='Z002' AND T5.NAMECD2=T1.SEX ");
            stb.append("ORDER BY  ");
            stb.append("    T1.TESTDIV, T1.EXAMNO ");
        } catch( Exception e ){
            log.error("preStat1 error!");
        }
        return stb.toString();

    }//preStat1()の括り



    /**総ページ数を取得**/
    private String preStat2(String param[])
    {
        StringBuffer stb = new StringBuffer();
    //  パラメータ（なし）
        try {
            stb.append("SELECT ");
            stb.append("    SUM(T1.TEST_CNT) TOTAL_PAGE  ");
            stb.append("FROM ");
            stb.append("    (SELECT CASE WHEN MOD(COUNT(*),50) > 0 THEN COUNT(*)/50 + 1 ELSE COUNT(*)/50 END TEST_CNT  ");
            stb.append("     FROM   ENTEXAM_RECEPT_DAT W1, ENTEXAM_APPLICANTBASE_DAT W2  ");
            stb.append("     WHERE  W1.ENTEXAMYEAR='"+param[0]+"'  ");
            stb.append("            AND W1.ENTEXAMYEAR=W2.ENTEXAMYEAR  ");
            stb.append("            AND W1.EXAMNO=W2.EXAMNO  ");

        //  '全て'以外の場合（入試制度）
            if (!param[1].equals("0"))
            stb.append("            AND W1.APPLICANTDIV='"+param[1]+"' ");

        //  '全て'以外の場合（入試区分）
//          if (!param[2].equals("0"))
//          stb.append("            AND W1.TESTDIV='"+param[2]+"' ");
            stb.append("            AND W1.TESTDIV=? ");    /* NO001 */

        //  特待生合格者名簿
            if (param[3].equals("1")) {
            stb.append("            AND W1.JUDGEDIV='1'  ");
            stb.append("            AND W1.HONORDIV='1'  ");
            stb.append("     GROUP BY W1.TESTDIV ) T1  ");  /* NO005 */
            }

        //  不合格者名簿
            if (param[3].equals("2")) {
            stb.append("            AND W1.JUDGEDIV='2'  ");
            stb.append("     GROUP BY W1.TESTDIV, SUBSTR(W1.EXAMNO,1,2) ) T1  ");   /* NO004 */
            }
        //  合格者名簿 NO006
            if (param[3].equals("3")) {
                stb.append("            AND W1.JUDGEDIV='1'  ");
//NO007         stb.append("            AND value(W1.HONORDIV,'0')<>'1'  ");
                stb.append("     GROUP BY W1.TESTDIV, SUBSTR(W1.EXAMNO,1,2) ) T1  ");   /* NO004 */
            }
            //  アップ合格者名簿
            if (param[3].equals("4")) {
                stb.append("            AND W1.JUDGEDIV='1'  ");
                stb.append("            AND W1.JUDGECLASS='3'  ");
                stb.append("     GROUP BY W1.TESTDIV ) T1  ");
            }
            //  スライド合格者名簿
            if (param[3].equals("5")) {
                stb.append("            AND W1.JUDGEDIV='1'  ");
                stb.append("            AND W1.JUDGECLASS='4'  ");
                stb.append("     GROUP BY W1.TESTDIV ) T1  ");
            }
            //  非正規合格者名簿
            if (param[3].equals("6")) {
                stb.append("            AND W1.JUDGEDIV='1'  ");
                stb.append("            AND W1.JUDGECLASS='6'  ");
                stb.append("     GROUP BY W1.TESTDIV ) T1  ");
            }
       } catch( Exception e ){
            log.error("preStat2 error!");
        }
        return stb.toString();

    }//preStat2()の括り



    /**PrepareStatement close**/
    private void preStatClose(
        PreparedStatement ps,
        PreparedStatement ps1,
        PreparedStatement ps2
    ) {
        try {
            ps.close(); /* NO001 */
            ps1.close();
            ps2.close();
        } catch( Exception e ){
            log.error("preStatClose error!");
        }
    }//preStatClose()の括り



    /**ブランクをセット**/  /* NO001 */
    private void setSvfInt(
        Vrw32alp svf
    ) {
        try {
            svf.VrsOut("NOTE"   ,"");
        } catch( Exception ex ) {
            log.error("setSvfInt set error!");
        }

    }



}//クラスの括り
