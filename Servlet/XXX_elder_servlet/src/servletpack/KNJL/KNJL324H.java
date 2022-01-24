package servletpack.KNJL;

import java.io.IOException;
import java.io.PrintWriter;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJ_Get_Info;

/**
 *
 *  学校教育システム 賢者 [入試管理]
 *
 *                  ＜ＫＮＪＬ３２４Ｈ＞  合格者受験番号
 *
 *  2007/11/16 RTS 作成日
 **/

public class KNJL324H {

    private static final Log log = LogFactory.getLog(KNJL324H.class);

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws ServletException, IOException
    {

        Vrw32alp svf = new Vrw32alp();          //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                      //Databaseクラスを継承したクラス
        String param[] = new String[7];

        // パラメータの取得
        try {
            param[0] = request.getParameter("YEAR");                        //年度
            param[1] = request.getParameter("APDIV");                       //入試制度
            param[2] = request.getParameter("TESTDV");                      //入試区分
        } catch( Exception ex ) {
            log.error("parameter error!");
        }

        // print設定
        PrintWriter outstrm = new PrintWriter (response.getOutputStream());
        response.setContentType("application/pdf");

        // svf設定
        int ret = svf.VrInit();                             //クラスの初期化
        if (false && 0 != ret) { ret = 0; }
        ret = svf.VrSetSpoolFileStream(response.getOutputStream());         //PDFファイル名の設定

        // ＤＢ接続
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
        } catch( Exception ex ) {
            log.error("DB2 open error!");
            return;
        }


        // ＳＶＦ作成処理
        PreparedStatement ps = null;
        PreparedStatement ps1 = null;
        PreparedStatement ps2 = null;
        PreparedStatement ps3 = null;
        PreparedStatement ps4 = null;
        PreparedStatement ps5 = null;
        boolean nonedata = false;                               //該当データなしフラグ
        setHeader(db2,svf,param);
        for(int i=0 ; i<5 ; i++) log.debug("param["+i+"]="+param[i]);
        //SQL作成
        try {
            ps = db2.prepareStatement(preStat(param));          //入試区分preparestatement
            ps1 = db2.prepareStatement(preStat1(param));        //一覧(合否区分＝1(合格))preparestatement
            ps2 = db2.prepareStatement(preStat2(param));        //名称preparestatement
            ps3 = db2.prepareStatement(preStat3(param));        //一覧(合否区分＝3(繰上合格))preparestatement
            ps4 = db2.prepareStatement(preStat1_count(param));  //一覧(合否区分＝1(合格))preparestatement
            ps5 = db2.prepareStatement(preStat3_count(param));  //一覧(合否区分＝3(繰上合格))preparestatement
            
        } catch( Exception ex ) {
            log.error("DB2 prepareStatement set error!");
        }
        //SVF出力
        if (setSvfMain(db2,svf,param,ps,ps1,ps2,ps3,ps4,ps5)) nonedata = true;  //帳票出力のメソッド

        // 該当データ無し
        if( !nonedata ){
            ret = svf.VrSetForm("MES001.frm", 0);
            ret = svf.VrsOut("note" , "note");
            ret = svf.VrEndPage();
        }

        // 終了処理
        ret = svf.VrQuit();
        preStatClose(ps,ps1,ps2,ps3,ps4,ps5);   //preparestatementを閉じる
        db2.commit();
        db2.close();                    //DBを閉じる
        outstrm.close();                //ストリームを閉じる 

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
        
        if(param[1] != null){
            if(param[1].equals("1") ||param[1].equals("2")){
                // 入試制度が中学・高校(一般)の場合
                ret = svf.VrSetForm("KNJL324H_1.frm", 1);
            } else {
                // 入試制度が中学・高校(推薦)の場合
                ret = svf.VrSetForm("KNJL324H_2.frm", 1);
            }
        }
        String sNENDO = convZenkakuToHankaku(param[0]);
            param[3] = sNENDO + "年度";

        // ＳＶＦ属性変更--->改ページ
        ret = svf.VrAttribute("TESTDIV","FF=1");

        // 作成日(現在処理日)の取得
        try {
            returnval = getinfo.Control(db2);
            param[4] = fomatSakuseiDate(returnval.val3);
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
     */
    private boolean setSvfMain(
        DB2UDB db2,
        Vrw32alp svf,
        String param[],
        PreparedStatement ps,
        PreparedStatement ps1,
        PreparedStatement ps2,
        PreparedStatement ps3,
        PreparedStatement ps4,
        PreparedStatement ps5
    ) {
        boolean nonedata = false;

        if( ! param[2].equals("0") ){
            setTestNameDate(db2,svf,param,ps2,param[2]);                    //名称メソッド
            for(int i=5 ; i<param.length ; i++) log.debug("param["+i+"]="+param[i]);
            if (setSvfout(db2,svf,param,ps1,ps3,ps4,ps5,param[2])) nonedata = true;         //帳票出力のメソッド
            return nonedata;
        }

        try {
            ResultSet rs = ps.executeQuery();

            while( rs.next() ){
                setTestNameDate(db2,svf,param,ps2, rs.getString("TESTDIV"));                    //名称メソッド
                for(int i=5 ; i<param.length ; i++) log.debug("param["+i+"]="+param[i]);
                if (setSvfout(db2,svf,param,ps1,ps3,ps4,ps5,rs.getString("TESTDIV"))) nonedata = true;      //帳票出力のメソッド
            }
            rs.close();
            db2.commit();
        } catch( Exception ex ) {
            log.error("setSvfMain set error!");
        }
        return nonedata;
    }

    /**名称をセット**/
    private void setTestNameDate(
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
            if(param[1].equals("1")){
                ps2.setString( ++p, test_div );
            } else {
                ps2.setString( ++p, param[1] );
            }
            ResultSet rs = ps2.executeQuery();

            while( rs.next() ){
                param[5] = rs.getString("TEST_NAME");   //入試区分
            }
            rs.close();
            db2.commit();
        } catch( Exception ex ) {
            log.error("setTestNameDate set error!");
        }
    }

    /**帳票出力（一覧をセット）**/
    private boolean setSvfout(
        DB2UDB db2,
        Vrw32alp svf,
        String param[],
        PreparedStatement ps1,
        PreparedStatement ps3,
        PreparedStatement ps4,
        PreparedStatement ps5,
        String test_div
    ) {
        boolean nonedata = false;
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        boolean makeEnd = true;
        int idx_ps1 = 0;
        int idx_ps2 = 0;
        int judge1_cnt = 0;
        int judge3_cnt = 0;
        boolean rs1End = false;
        boolean rs2End = false;
        boolean eofflg1 = false;
        boolean eofflg2 = false;
        ResultSet rs1 = null;
        ResultSet rs2 = null;
        ResultSet rs3 = null;
        ResultSet rs4 = null;
        try {
            int p = 0;
            // 一覧表示データ取得
            ps1.setString( ++p, test_div );
            rs1 = ps1.executeQuery();
            ps3.setString( p, test_div );
            rs2 = ps3.executeQuery();
            // 一覧表示件数取得
            ps4.setString( p, test_div );
            rs3 = ps4.executeQuery();
            ps5.setString( p, test_div );
            rs4 = ps5.executeQuery();
            
            //件数を取得
            if(rs3.next()){
                judge1_cnt = Integer.valueOf(rs3.getString("JUDGE1_CNT")).intValue();
                if(judge1_cnt == 0){
                    eofflg1 = true;
                    rs1End = true;
                }
            }
            //件数を取得
            if(rs4.next()){
                judge3_cnt = Integer.valueOf(rs4.getString("JUDGE3_CNT")).intValue();
                if(judge3_cnt == 0){
                    eofflg2 = true;
                    rs2End = true;
                }
            }
            // 入試制度が高校(推薦)の場合は繰上合格候補者一覧は表示しない
            if(param[1].equals("3")){
                eofflg2 = true;
                rs2End = true;
            }
            // 合否区分の1(合格)と3(繰上合格)のデータが存在しない場合
            if(eofflg1 == true && eofflg2 == true){
                makeEnd = false;
            }
            
            // 合格者用の『列・行』
            int len = 1;            // 列
            int gyo = 1;            // 行
            int total_count1 = 0;   // 合格者総数
            // 繰上合格候補者用の『列・行』
            int len2 = 1;   //列
            int gyo2 = 1;   //行
            int total_count2 = 0;   // 合格者総数
            
            while(makeEnd){
                // *---------------------*
                // * 改ページ判定        *
                // *---------------------*
                if(rs1End == true && rs2End == true){
                    // 合計欄の設定
                    ret = svf.VrsOut("COUNT1",  String.valueOf(total_count1));  // 合格者総数
                    ret = svf.VrsOut("COUNT2",  String.valueOf(total_count2));  // 繰上合格候補者総数
                    ret = svf.VrEndPage();//ページを出力
                    gyo  = 1;
                    gyo2 = 1;
                    len  = 1;
                    len2 = 1;
                    total_count1 = 0;
                    total_count2 = 0;
                    if(!eofflg1){
                        rs1End = true;
                    }
                    if(!eofflg2){
                        rs2End = true;
                    }
                }
                // *---------------------*
                // * 見出し編集          *
                // *---------------------*
                
                //表題(上)
                if(param[1].equals("1")){
                    ret = svf.VrsOut("SCHOOL"   ,"中学校");
                } else {
                    ret = svf.VrsOut("SCHOOL"   ,"高等学校");
                }
                //表題(下)
                ret = svf.VrsOut("NENDO"    ,param[3]); //年度
                ret = svf.VrsOut("TESTDIV"  ,param[5]); //入試区分
                if(param[1].equals("1") || param[1].equals("2")){
                    ret = svf.VrsOut("TITLE"    ,"および繰上合格候補者");  // タイトル
                }
                    
                // *---------------------*
                // *  合格者用の明細     *
                // *---------------------*
                if(!eofflg1){
                    while( rs1.next() ){
                        //改行判定
                        //明細編集
                        ret = svf.VrsOutn("EXAMNO"+String.valueOf(len)  ,gyo ,rs1.getString("EXAMNO")); //受験番号
                        nonedata = true;
                        gyo++;
                        total_count1++;
                        ++idx_ps1;
                        //最終行の場合列数をカウント
                        if (gyo > 40) {
                            len++;
                            if (len > 4) {
                                rs1End = true;
                                ++idx_ps1;
                                break;
                            }
                            gyo = 1;
                        }
                    }
                    // 全て読み終えた場合
                    if(judge1_cnt == idx_ps1){
                        rs1End = true;
                        eofflg1 = true;
                    }
                }
                // *--------------------------*
                // *  繰上合格候補者用の明細  *
                // *--------------------------*
                if(!eofflg2){
                    while( rs2.next() ){
                        ret = svf.VrsOutn("C_EXAMNO"+String.valueOf(len2)   ,gyo2 ,rs2.getString("EXAMNO"));    //受験番号
                        nonedata = true;
                        gyo2++;
                        total_count2++;
                        ++idx_ps2;
                        //最終行の場合列数をカウント
                        if (gyo2 > 40) {
                            len2++;
                            //改行判定
                            if (len2 > 3) {
                                rs2End = true;
                                ++idx_ps2;
                                break;
                            }
                            gyo2 = 1;
                        }
                    }
                    // 全て読み終えた場合
                    if(judge3_cnt == idx_ps2){
                        rs2End = true;
                        eofflg2 = true;
                    }
                }
                // 合格者欄および繰上合格候補者欄の出力が完了した場合
                if(eofflg1 == true && eofflg2 == true){
                    //最終ページを出力
                    ret = svf.VrsOut("COUNT1",  String.valueOf(total_count1));  // 合格者総数
                    ret = svf.VrsOut("COUNT2",  String.valueOf(total_count2));  // 繰上合格候補者総数
                    if (nonedata) ret = svf.VrEndPage();
                    //編集終了
                    makeEnd = false;
                    rs1.close();
                    rs2.close();
                    rs3.close();
                    rs4.close();
                    db2.commit();
                }
            }
        } catch( Exception ex ) {
            log.error("setSvfout set error!");
        }
        return nonedata;
    }


    /**入試区分を取得**/
    private String preStat(String param[])
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("SELECT DISTINCT TESTDIV ");
            stb.append("FROM   ENTEXAM_RECEPT_DAT ");
            stb.append("WHERE  ENTEXAMYEAR='"+param[0]+"' AND ");
            stb.append("       APPLICANTDIV='"+param[1]+"' AND ");
            stb.append("       JUDGEDIV IN ('1','3') ");
            stb.append("ORDER BY TESTDIV ");
        } catch( Exception e ){
            log.error("preStat error!");
        }
        return stb.toString();

    }//preStat()の括り


    /**一覧件数を取得**/
    private String preStat1_count(String param[])
    {
        StringBuffer stb = new StringBuffer();
    //  パラメータ（入試区分）
        try {
            stb.append("SELECT ");
            stb.append("COUNT(*) AS JUDGE1_CNT ");
            stb.append("FROM ENTEXAM_RECEPT_DAT ");
            stb.append("WHERE  ENTEXAMYEAR='"+param[0]+"' AND ");
            stb.append("       APPLICANTDIV='"+param[1]+"' AND ");
            stb.append("       TESTDIV=? AND ");
            stb.append("       JUDGEDIV='1' ");
        } catch( Exception e ){
            log.error("preStat1 error!");
        }
        return stb.toString();

    }//preStat1()の括り

    /**一覧件数を取得**/
    private String preStat3_count(String param[])
    {
        StringBuffer stb = new StringBuffer();
    //  パラメータ（入試区分）
        try {
            stb.append("SELECT ");
            stb.append("COUNT(*) AS JUDGE3_CNT ");
            stb.append("FROM ENTEXAM_RECEPT_DAT ");
            stb.append("WHERE  ENTEXAMYEAR='"+param[0]+"' AND ");
            stb.append("       APPLICANTDIV='"+param[1]+"' AND ");
            stb.append("       TESTDIV=? AND ");
            stb.append("       JUDGEDIV='3' ");
        } catch( Exception e ){
            log.error("preStat1 error!");
        }
        return stb.toString();

    }//preStat1()の括り

    /**一覧を取得**/
    private String preStat1(String param[])
    {
        StringBuffer stb = new StringBuffer();
    //  パラメータ（入試区分）
        try {
            stb.append("SELECT ");
            stb.append("EXAMNO, ");
            stb.append("JUDGEDIV  ");
            stb.append("FROM ENTEXAM_RECEPT_DAT ");
            stb.append("WHERE  ENTEXAMYEAR='"+param[0]+"' AND ");
            stb.append("       APPLICANTDIV='"+param[1]+"' AND ");
            stb.append("       TESTDIV=? AND ");
            stb.append("       JUDGEDIV='1' ");
            stb.append("ORDER BY EXAMNO ");
        } catch( Exception e ){
            log.error("preStat1 error!");
        }
        return stb.toString();

    }//preStat1()の括り

    /**一覧を取得**/
    private String preStat3(String param[])
    {
        StringBuffer stb = new StringBuffer();
    //  パラメータ（入試区分）
        try {
            stb.append("SELECT ");
            stb.append("EXAMNO, ");
            stb.append("JUDGEDIV  ");
            stb.append("FROM ENTEXAM_RECEPT_DAT ");
            stb.append("WHERE  ENTEXAMYEAR='"+param[0]+"' AND ");
            stb.append("       APPLICANTDIV='"+param[1]+"' AND ");
            stb.append("       TESTDIV=? AND ");
            stb.append("       JUDGEDIV='3' ");
            stb.append("ORDER BY EXAMNO ");
        } catch( Exception e ){
            log.error("preStat1 error!");
        }
        return stb.toString();

    }//preStat1()の括り


    /**名称を取得**/
    private String preStat2(String param[])
    {
        StringBuffer stb = new StringBuffer();
        // パラメータ（入試区分）
        try {
            if (param[1].equals("1")){
                stb.append("SELECT NAME1 AS TEST_NAME FROM NAME_MST ");
            } else {
                stb.append("SELECT ABBV1 AS TEST_NAME FROM NAME_MST ");
            }

            if (param[1].equals("1")){
                stb.append("WHERE  NAMECD1='L004' AND NAMECD2=? ");
            } else {
                stb.append("WHERE  NAMECD1='L003' AND NAMECD2=? ");
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
        PreparedStatement ps2,
        PreparedStatement ps3,
        PreparedStatement ps4,
        PreparedStatement ps5
    ) {
        try {
            ps.close();
            ps1.close();
            ps2.close();
            ps3.close();
            ps4.close();
            ps5.close();
        } catch( Exception e ){
            log.error("preStatClose error!");
        }
    }//preStatClose()の括り

    /**
     * 日付をフォーマットYYYY年MM月DD日に設定する
     * @param s
     * @return
     */
    private String fomatSakuseiDate(String cnvDate) {

        String retDate = "";
        try {
            DateFormat foramt = new SimpleDateFormat("yyyy-MM-dd"); 
            //文字列よりDate型へ変換
            Date date1 = foramt.parse(cnvDate); 
            // 年月日のフォーマットを指定
            SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy'年'MM'月'dd'日'");
            // Date型より文字列へ変換
            retDate = sdf1.format(date1);
        } catch( Exception e ){
            log.error("setHeader set error!");
        }
        return retDate;
    }

    /**
     * 半角数字を全角数字に変換する
     * @param s
     * @return
     */
    private String convZenkakuToHankaku(String s) {
        StringBuffer sb = new StringBuffer(s);
        for (int i = 0; i < s.length(); i++) {
          char c = s.charAt(i);
          if (c >= '0' && c <= '9') {
            sb.setCharAt(i, (char) (c - '0' + 0xff10));
          }
        }
        return sb.toString();
    }


}//クラスの括り
