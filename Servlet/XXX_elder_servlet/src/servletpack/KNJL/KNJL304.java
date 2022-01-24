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
 *                  ＜ＫＮＪＬ３０４＞  出願者名簿
 *
 *  2004/12/21 nakamoto 作成日
 *  2004/12/27 nakamoto 総ページ数は入試区分毎に出力    NO001
 *  2005/01/06 nakamoto 入試区分をカット。受験番号範囲指定に変更    NO002
 *  2005/01/12 nakamoto 受験番号○○◇◇◇の、○○の部分が変わったら改ページする    NO004
 *             nakamoto コミットが入れられるところではコミットする  NO003
 **/

public class KNJL304 {

    private static final Log log = LogFactory.getLog(KNJL304.class);

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
            param[3] = request.getParameter("EXAMNO_ST");                   //開始受験番号 NO002
            param[4] = request.getParameter("EXAMNO_ED");                   //終了受験番号 NO002
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
        PreparedStatement ps1 = null;
        PreparedStatement ps2 = null;
        PreparedStatement psTestDiv = null;
        boolean nonedata = false;                               //該当データなしフラグ
        setHeader(db2,svf,param);
for(int i=0 ; i<param.length ; i++) log.debug("param["+i+"]="+param[i]);
        //SQL作成
        try {
            ps1 = db2.prepareStatement(preStat1(param));        //出願者一覧preparestatement
            ps2 = db2.prepareStatement(preStat2(param));        //総ページ数preparestatement
            psTestDiv = db2.prepareStatement(preTestDivMst(param));
        } catch( Exception ex ) {
            log.error("DB2 prepareStatement set error!");
        }
        //SVF出力
        setTotalPage(db2,svf,param,ps2);                            //総ページ数メソッド    /* NO002 */
        if( setSvfout(db2,svf,param,ps1,psTestDiv) ){                         //帳票出力のメソッド    /* NO002 */
            nonedata = true;
        }

    //  該当データ無し
        if( !nonedata ){
            ret = svf.VrSetForm("MES001.frm", 0);
            ret = svf.VrsOut("note" , "note");
            ret = svf.VrEndPage();
        }

    //  終了処理
        ret = svf.VrQuit();
        preStatClose(ps1,ps2,psTestDiv);      //preparestatementを閉じる  /* NO001 */
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
        ret = svf.VrSetForm("KNJL304.frm", 4);
        ret = svf.VrsOut("NENDO"    ,KenjaProperties.gengou(Integer.parseInt(param[0]))+"年度");

    //  ＳＶＦ属性変更--->改ページ
        //ret = svf.VrAttribute("TESTDIV","FF=1");  /* NO002 */

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

    /**総ページ数をセット**/
    private void setTotalPage(
        DB2UDB db2,
        Vrw32alp svf,
        String param[],
        PreparedStatement ps2
    ) {
        try {
            //ps2.setString( ++p, test_div );   /* NO001 */ /* NO002 */
            ResultSet rs = ps2.executeQuery();

            while( rs.next() ){
                if (rs.getString("TOTAL_PAGE") != null) 
                    svf.VrsOut("TOTAL_PAGE" ,rs.getString("TOTAL_PAGE"));
            }
            rs.close();
            db2.commit();   /* NO003 */
        } catch( Exception ex ) {
            log.error("setTotalPage set error!");
        }

    }



    /**帳票出力（出願者一覧をセット）**/
    private boolean setSvfout(
        DB2UDB db2,
        Vrw32alp svf,
        String param[],
        PreparedStatement ps1,
        PreparedStatement psTestDiv
    ) {
        boolean nonedata = false;
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        ResultSet rsTestDiv = null;
        try {
            ResultSet rs = ps1.executeQuery();

            int reccnt_man      = 0;    //男レコード数カウント用
            int reccnt_woman    = 0;    //女レコード数カウント用
            int reccnt = 0;             //合計レコード数
            int pagecnt = 1;            //現在ページ数
            int gyo = 1;                //現在ページ数の判断用（行）

            while( rs.next() ){
                //レコードを出力
                if (reccnt > 0) ret = svf.VrEndRecord();
                //５０行超えたとき、ページ数カウント    /* NO002 */
                if (gyo > 50) { /* NO004 */
                    gyo = 1;
                    pagecnt++;
                }
                //ヘッダ
                ret = svf.VrsOut("PAGE"     ,String.valueOf(pagecnt));      //現在ページ数
                //明細
                ret = svf.VrsOut("EXAMNO"   ,rs.getString("EXAMNO"));       //受験番号
                ret = svf.VrsOut("NAME"     ,rs.getString("NAME"));         //名前
                ret = svf.VrsOut("KANA"     ,rs.getString("NAME_KANA"));    //ふりがな
                ret = svf.VrsOut("SEX"      ,rs.getString("SEX_NAME"));     //性別
                ret = svf.VrsOut("FINSCHOOL",rs.getString("FS_NAME"));      //出身学校名
                int num = 0;
                rsTestDiv = psTestDiv.executeQuery();
                while (rsTestDiv.next()) {
                    num++;
                    String testdiv = rsTestDiv.getString("TESTDIV"); 
                    String testdivVal = rs.getString("TESTDIV" + testdiv); 

                    if (testdivVal != null) {
                        ret = svf.VrsOut("TESTDIV" + num ,  String.valueOf(num));      //出願区分
                    }
                }
                rsTestDiv.close();
                ret = svf.VrsOut("ABSENCE"  ,rs.getString("ABSENCE_DAYS")); //欠席日数  /* NO002 */
                //レコード数カウント
                reccnt++;
                if (rs.getString("SEX") != null) {
                    if (rs.getString("SEX").equals("1")) reccnt_man++;
                    if (rs.getString("SEX").equals("2")) reccnt_woman++;
                }
                //現在ページ数判断用
                gyo++;

                nonedata = true;
            }
            //最終レコードを出力
            if (nonedata) {
                //最終ページに男女合計を出力
                ret = svf.VrsOut("NOTE" ,"男"+String.valueOf(reccnt_man)+"名,女"+String.valueOf(reccnt_woman)+"名,合計"+String.valueOf(reccnt)+"名");
                ret = svf.VrEndRecord();//レコードを出力
                //setSvfInt(svf);           //ブランクセット    /* NO001 */ /* NO002 */
            }
            rs.close();
            db2.commit();   /* NO003 */
        } catch( Exception ex ) {
            log.error("setSvfout set error!");
        }
        return nonedata;
    }

    /**出願者一覧を取得**/
    private String preStat1(String param[])
    {
        StringBuffer stb = new StringBuffer();
    //  パラメータ（なし）
        try {
            stb.append("SELECT ");
            stb.append("    CASE WHEN T1.TESTDIV0 IS NOT NULL THEN '1' END AS TESTDIV0, ");
            stb.append("    T4_0.NAME AS TEST_NAME0, ");
            stb.append("    CASE WHEN T1.TESTDIV1 IS NOT NULL THEN '2' END AS TESTDIV1, ");
            stb.append("    T4_1.NAME AS TEST_NAME1, ");
            stb.append("    CASE WHEN T1.TESTDIV2 IS NOT NULL THEN '3' END AS TESTDIV2, ");
            stb.append("    T4_2.NAME AS TEST_NAME2, ");
            stb.append("    CASE WHEN T1.TESTDIV3 IS NOT NULL THEN '4' END AS TESTDIV3, ");
            stb.append("    T4_3.NAME AS TEST_NAME3, ");
            stb.append("    CASE WHEN T1.TESTDIV4 IS NOT NULL THEN '5' END AS TESTDIV4, ");
            stb.append("    T4_4.NAME AS TEST_NAME4, ");
            stb.append("    CASE WHEN T1.TESTDIV5 IS NOT NULL THEN '6' END AS TESTDIV5, ");
            stb.append("    T4_5.NAME AS TEST_NAME5, ");
            stb.append("    CASE WHEN T1.TESTDIV6 IS NOT NULL THEN '7' END AS TESTDIV6, ");
            stb.append("    T4_6.NAME AS TEST_NAME6, ");
            stb.append("    T1.EXAMNO, ");
            stb.append("    SUBSTR(T1.EXAMNO,1,2) EXAMNO2, ");  /* NO004 */
            stb.append("    T1.NAME, ");
            stb.append("    T1.NAME_KANA, ");
            stb.append("    T1.SEX, ");
            stb.append("    T5.ABBV1 AS SEX_NAME, ");
            stb.append("    T1.FS_NAME,  ");
            stb.append("    T2.ABSENCE_DAYS  ");
            stb.append("FROM ");
            stb.append("    (SELECT TESTDIV0, TESTDIV1, TESTDIV2, TESTDIV3, TESTDIV4, TESTDIV5, TESTDIV6, EXAMNO, NAME, NAME_KANA, SEX, FS_NAME  ");
            stb.append("     FROM   ENTEXAM_APPLICANTBASE_DAT  ");
            stb.append("     WHERE  ENTEXAMYEAR='"+param[0]+"'  ");

        //  '全て'以外の場合（入試制度）
            if (!param[1].equals("0")) 
            stb.append("            AND APPLICANTDIV='"+param[1]+"' ");

            stb.append("            AND EXAMNO BETWEEN '"+param[3]+"' AND '"+param[4]+"' ");    /* NO002 */

            stb.append("            ) T1  ");
            stb.append("    LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT T2 ON T2.ENTEXAMYEAR='"+param[0]+"'  ");
            stb.append("                                             AND T2.EXAMNO=T1.EXAMNO  ");   /* NO002 */
            stb.append("    LEFT JOIN ENTEXAM_TESTDIV_MST T4_0 ON T4_0.ENTEXAMYEAR='"+param[0]+"' AND T4_0.TESTDIV=T1.TESTDIV0 ");
            stb.append("    LEFT JOIN ENTEXAM_TESTDIV_MST T4_1 ON T4_1.ENTEXAMYEAR='"+param[0]+"' AND T4_1.TESTDIV=T1.TESTDIV1 ");
            stb.append("    LEFT JOIN ENTEXAM_TESTDIV_MST T4_2 ON T4_2.ENTEXAMYEAR='"+param[0]+"' AND T4_2.TESTDIV=T1.TESTDIV2 ");
            stb.append("    LEFT JOIN ENTEXAM_TESTDIV_MST T4_3 ON T4_3.ENTEXAMYEAR='"+param[0]+"' AND T4_3.TESTDIV=T1.TESTDIV3 ");
            stb.append("    LEFT JOIN ENTEXAM_TESTDIV_MST T4_4 ON T4_4.ENTEXAMYEAR='"+param[0]+"' AND T4_4.TESTDIV=T1.TESTDIV4 ");
            stb.append("    LEFT JOIN ENTEXAM_TESTDIV_MST T4_5 ON T4_5.ENTEXAMYEAR='"+param[0]+"' AND T4_5.TESTDIV=T1.TESTDIV5 ");
            stb.append("    LEFT JOIN ENTEXAM_TESTDIV_MST T4_6 ON T4_6.ENTEXAMYEAR='"+param[0]+"' AND T4_6.TESTDIV=T1.TESTDIV6 ");
            stb.append("    LEFT JOIN NAME_MST T5 ON T5.NAMECD1='Z002' AND T5.NAMECD2=T1.SEX ");
            stb.append("ORDER BY  ");
            stb.append("    T1.EXAMNO ");   /* NO002 */
            //stb.append("    T1.TESTDIV, T1.EXAMNO ");
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
            stb.append("     FROM   ENTEXAM_APPLICANTBASE_DAT  ");
            stb.append("     WHERE  ENTEXAMYEAR='"+param[0]+"'  ");

        //  '全て'以外の場合（入試制度）
            if (!param[1].equals("0")) 
            stb.append("            AND APPLICANTDIV='"+param[1]+"' ");

            stb.append("            AND EXAMNO BETWEEN '"+param[3]+"' AND '"+param[4]+"' ");    /* NO002 */

            stb.append("     GROUP BY SUBSTR(EXAMNO,1,2) ) T1  ");  /* NO004 */
            //stb.append("            ) T1  ");
            //stb.append("     GROUP BY TESTDIV ) T1  ");   /* NO002 */
        } catch( Exception e ){
            log.error("preStat2 error!");
        }
        return stb.toString();

    }//preStat2()の括り



    private String preTestDivMst(String param[])
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("SELECT TESTDIV FROM ENTEXAM_TESTDIV_MST WHERE ENTEXAMYEAR = '"+param[0]+"' ORDER BY SHOWORDER, TESTDIV ");
        } catch( Exception e ){
            log.error("preStat1 error!", e);
        }
        return stb.toString();
    }

    /**PrepareStatement close**/
    private void preStatClose(
        PreparedStatement ps1,
        PreparedStatement ps2,
        PreparedStatement psTestDiv
    ) {
        try {
            ps1.close();
            ps2.close();
            psTestDiv.close();
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
