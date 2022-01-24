// kanji=漢字
/*
 * $Id: 226acb82da80da9b74d7af08fdba2b51f509bc2d $
 *
 * 作成日: 2004/12/22 11:25:40 - JST
 * 作成者: nakamoto
 *
 * Copyright(C) 2009-2009 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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

/**
 *
 *  学校教育システム 賢者 [入試管理]
 *
 *                  ＜ＫＮＪＬ３２７＞  
 *                  ＜１＞  合格通知書
 *                  ＜２＞  特待生合格通知書
 *                  ＜３＞  特待生合格証明書
 *                  ＜４＞  不合格通知書
 *                  ＜５＞  入学金振込用紙
 *                  ＜６＞  入学手続延期願
 *
 *  2004/12/22 nakamoto 作成日
 *  2005/01/07 nakamoto ふりがなをカタカナに変換 NO001
 *  2005/01/08 yamashiro 口座名義人カナを１２文字以内と超過で文字の大きさを変える
 *  2005/01/12 nakamoto 合格・振込・延期は、全員受験番号の指定で印刷可能にする NO002
 *             nakamoto コミットが入れられるところではコミットする  NO003
 *  2005/01/13 nakamoto 合格・振込・延期の条件に、合格者のみフラグ（パラメータ）を追加 NO004(NO002の変更)
 *  2005/12/29 m-yama   合格・延期・不合の条件に、合格者全員、受験者全員を追加 NO005
 *
 **/
public class KNJL327 {

    private static final Log log = LogFactory.getLog(KNJL327.class);

    private Map hmm = new HashMap();    //ひらがなからカタカナの変換用 NO001

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws ServletException, IOException
    {

        Vrw32alp svf = new Vrw32alp();          //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                      //Databaseクラスを継承したクラス
        String param[] = new String[15];

    //  パラメータの取得
        try {
            param[0] = request.getParameter("YEAR");                        //年度
            param[1] = request.getParameter("APDIV");                       //入試制度
            param[2] = request.getParameter("TESTDV");                      //入試区分
            param[3] = request.getParameter("NOTICEDAY");                   //通知日付

            param[4] = request.getParameter("OUTPUT");                      //帳票種類 1,2,3,4,5,6,7,8

        if (param[4].equals("1") || param[4].equals("5")) {
            param[5] = request.getParameter("OUTPUTA");                     //受験者(1,5) 1:全員2:指定
            param[6] = request.getParameter("EXAMNOA");                     //受験番号
            param[14] = request.getParameter("CHKBOXA");                    //合格者のみフラグ(1,null) NO004
        }
        if (param[4].equals("6")) {
            param[5] = request.getParameter("OUTPUTB");                     //受験者(6) 1:全員2:指定
            param[6] = request.getParameter("EXAMNOB");                     //受験番号
            param[14] = request.getParameter("CHKBOXB");                    //合格者のみフラグ(1,null) NO004
        }
        if (param[4].equals("2") || param[4].equals("3")) {
            param[5] = request.getParameter("OUTPUTC");                     //受験者(2,3) 1:全員2:指定
            param[6] = request.getParameter("EXAMNOC");                     //受験番号
        }
        if (param[4].equals("4")) {
            param[5] = request.getParameter("OUTPUTD");                     //受験者(4) 1:全員2:指定
            param[6] = request.getParameter("EXAMNOD");                     //受験番号
        }

            param[7] = request.getParameter("DEADLINE");                    //提出期限日
            param[8] = request.getParameter("TIMEUPH");                     //提出期限(時)
            param[9] = request.getParameter("TIMEUPM");                     //提出期限(分)
            
        if (param[4].equals("7")) {
            param[5] = request.getParameter("OUTPUTE");                     //受験者(7) 1:全員2:指定
            param[6] = request.getParameter("EXAMNOE");                     //受験番号
        }
        if (param[4].equals("8")) {
            param[5] = request.getParameter("OUTPUTE");                     //受験者(8) 1:全員2:指定
            param[6] = request.getParameter("EXAMNOE");                     //受験番号
        }
            
            
            
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
        boolean nonedata = false;                               //該当データなしフラグ
        setHeader(db2,svf,param);
        setMapKana();   // NO001
for(int i=0 ; i<param.length ; i++) log.debug("param["+i+"]="+param[i]);
        //SQL作成
        try {
            ps1 = db2.prepareStatement(preStat1(param));        //各通知書preparestatement
        } catch( Exception ex ) {
            log.error("DB2 prepareStatement set error!");
        }
        //SVF出力
        if( setSvfout(db2,svf,param,ps1) ){                         //帳票出力のメソッド
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
        preStatClose(ps1);      //preparestatementを閉じる
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
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
    //  帳票フォーム
//        if (param[4].equals("1")) ret = svf.VrSetForm("KNJL327_1.frm", 4);
//        if (param[4].equals("2") && (param[2].equals("0") || param[2].equals("2") || param[2].equals("5"))) ret = svf.VrSetForm("KNJL327_2.frm", 4);
//        if (param[4].equals("2") && (param[2].equals("1") || param[2].equals("3") || param[2].equals("4"))) ret = svf.VrSetForm("KNJL327_9.frm", 4);
        if (param[4].equals("3")) ret = svf.VrSetForm("KNJL327_3.frm", 4);
        if (param[4].equals("4")) ret = svf.VrSetForm("KNJL327_4.frm", 4);
        if (param[4].equals("5")) ret = svf.VrSetForm("KNJL327_5.frm", 4);
        if (param[4].equals("6")) ret = svf.VrSetForm("KNJL327_6.frm", 4);
//        if (param[4].equals("7")) ret = svf.VrSetForm("KNJL327_7.frm", 4);
//        if (param[4].equals("8")) ret = svf.VrSetForm("KNJL327_8.frm", 4);

        try {
            //通知日付(1〜4)
                param[10] = KNJ_EditDate.h_format_JP(param[3]);
            //年度(6)
                param[11] = KenjaProperties.gengou(Integer.parseInt(param[0]))+"年";
            //提出期限日(6)
                param[12] = KNJ_EditDate.h_format_JP(param[7]);
            //提出時間(6)
                param[13] = param[8]+":"+param[9];
        } catch( Exception e ){
            log.error("setHeader set error!");
        }
    }



    private List getTextList(final ResultSet rs) throws Exception {
        final List textList = new ArrayList();
        final String testName = rs.getString("TEST_NAME");
        if ("1".equals(rs.getString("HONORDIV"))) {
            if ("3".equals(rs.getString("JUDGECLASS"))) {
                //D.アップ＋特待
                textList.add(testName + "、合格おめでとうございます。");
                textList.add("特別進学クラス、英数特科クラスともに合格です。");
                textList.add("また、特待生としても合格です。");
            } else if ("5".equals(rs.getString("JUDGECLASS"))) {
                //A.正規
                textList.add(testName + "、合格おめでとうございます。");
                textList.add("特待生として合格です。");
            } else {
                //C.特待
                textList.add(testName + "、合格おめでとうございます。");
                textList.add("特待生として合格です。");
            }
        } else if ("3".equals(rs.getString("JUDGECLASS"))) {
                //F.アップ
                textList.add(testName + "、合格おめでとうございます。");
                textList.add("特別進学クラス、英数特科クラスともに合格です。");
        } else if ("4".equals(rs.getString("JUDGECLASS"))) {
                //G.スライド
                textList.add("英数特科クラスではありませんが、");
                textList.add("特別進学クラスに合格です。");
        } else if ("6".equals(rs.getString("JUDGECLASS"))) {
                //B.非正規
                textList.add("特待は付きませんが、");
                textList.add("特別進学クラスに合格です。");
        } else {
                //E.通常
                textList.add(testName + "、合格おめでとうございます。");
        }
                textList.add("あなたの夢を、本校で大きく育んでみませんか。");
                textList.add("応援します。");
        return textList;
    }

    /**帳票出力（各通知書をセット）**/
    private boolean setSvfout(
        DB2UDB db2,
        Vrw32alp svf,
        String param[],
        PreparedStatement ps1
    ) {
        boolean nonedata = false;
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        try {
            ResultSet rs = ps1.executeQuery();

            while( rs.next() ){
                if (param[4].equals("1") || param[4].equals("2") || param[4].equals("7") || param[4].equals("8")) {
                    ret = svf.VrSetForm("KNJL327_12.frm",4);
                    final List textList = getTextList(rs);
                    int gyo = 0;
                    for (final Iterator it = textList.iterator(); it.hasNext();) {
                        final String text = (String) it.next();
                        gyo++;
                        ret = svf.VrsOut("TEXT" + gyo, text);
                    }
                }
                //(1〜4)
                ret = svf.VrsOut("EXAMNO"   ,rs.getString("EXAMNO"));                       //受験番号
                ret = svf.VrsOut("NAME"     ,rs.getString("NAME"));             //名前
                if( rs.getString("NAME") != null  &&  12 < rs.getString("NAME").length() )
                    ret = svf.VrsOut("NAME2"    ,rs.getString("NAME"));                     //名前 05/01/08Modify
                else
                    ret = svf.VrsOut("NAME1"    ,rs.getString("NAME"));                     //名前 05/01/08Modify
                ret = svf.VrsOut("TESTDIV"  ,rs.getString("TEST_NAME"));                    //入試区分
                ret = svf.VrsOut("DATE"     ,param[10]);                                    //通知日付
                //(5)

                ret = svf.VrsOut("EXAMNO1"  ,rs.getString("EXAMNO"));                       //受験番号
                if( rs.getString("NAME") != null  &&  10 < rs.getString("NAME").length() )
                    ret = svf.VrsOut("NAME1_2"  ,rs.getString("NAME"));                     //名前 05/01/08Modify
                else
                    ret = svf.VrsOut("NAME1_1"  ,rs.getString("NAME"));                     //名前 05/01/08Modify
                ret = svf.VrsOut("EXAMNO2"  ,rs.getString("EXAMNO"));                       //受験番号
                if( rs.getString("NAME") != null  &&  10 < rs.getString("NAME").length() )
                    ret = svf.VrsOut("NAME2_2"  ,rs.getString("NAME"));                     //名前 05/01/08Modify
                else
                    ret = svf.VrsOut("NAME2_1"  ,rs.getString("NAME"));                     //名前 05/01/08Modify
                ret = svf.VrsOut("EXAMNO3"  ,rs.getString("EXAMNO"));                       //受験番号
                if( rs.getString("NAME") != null  &&  12 < rs.getString("NAME").length() )
                    ret = svf.VrsOut("NAME3_2"  ,rs.getString("NAME"));                     //名前 05/01/08Modify
                else
                    ret = svf.VrsOut("NAME3_1"  ,rs.getString("NAME"));                     //名前 05/01/08Modify
                if( rs.getString("NAME_KANA") != null  &&  12 < rs.getString("NAME_KANA").length() )
                    ret = svf.VrsOut("KANA3_2"  ,getConvertKana(rs.getString("NAME_KANA")) );           //ふりがな NO001 05/01/08Modify
                else
                    ret = svf.VrsOut("KANA3_1"  ,getConvertKana(rs.getString("NAME_KANA")) );           //ふりがな NO001 05/01/08Modify
                
                //(6)
                ret = svf.VrsOut("TESTDIV1" ,rs.getString("TEST_NAME"));    //入試区分(上)
                ret = svf.VrsOut("TESTDIV2" ,rs.getString("TEST_NAME"));    //入試区分(下)
                ret = svf.VrsOut("YEAR"     ,param[11]);                    //年度
                ret = svf.VrsOut("DEADLINE1",param[12]);                    //提出期限日
                ret = svf.VrsOut("DEADLINE2",param[13]);                    //提出時間

                //(7〜8)
                ret = svf.VrsOut("ENTCLASS", rs.getString("CLASS_NAME"));   //クラス名
                
                ret = svf.VrEndRecord();//レコードを出力
                nonedata = true;
            }
            rs.close();
            db2.commit();   /* NO003 */
        } catch( Exception ex ) {
            log.error("setSvfout set error!");
        }
        return nonedata;
    }



    /**
     *  文字数チェック 2005/01/08 yamashiro
     */
    private int getMojisu( String moji ) {

        int mojisu = 0;

        try{
            byte bymoji[] = moji.getBytes("MS932");
            mojisu = bymoji.length;
        } catch( Exception e ){
            log.error("getMojisu error!", e );
        }
        return mojisu;
    }



    /**各通知書を取得**/
    private String preStat1(String param[])
    {
        StringBuffer stb = new StringBuffer();
    //  パラメータ（なし）
        try {
            stb.append("SELECT ");
            stb.append("    T1.TESTDIV, ");
            stb.append("    T4.NAME AS TEST_NAME, ");
            stb.append("    T1.EXAMNO, ");
            stb.append("    T1.NAME, ");
            stb.append("    T1.NAME_KANA, ");
            stb.append("    T5.NAME2 AS CLASS_NAME, ");
            stb.append("    T1.JUDGECLASS, ");
            stb.append("    T1.HONORDIV ");
            stb.append("FROM ");
            stb.append("    (SELECT W1.TESTDIV, W1.EXAMNO, W2.NAME, W2.NAME_KANA, W2.ENTCLASS, W1.JUDGECLASS, W1.HONORDIV  ");
            stb.append("     FROM   ENTEXAM_RECEPT_DAT W1, ENTEXAM_APPLICANTBASE_DAT W2  ");
            stb.append("     WHERE  W1.ENTEXAMYEAR='"+param[0]+"'  ");
            stb.append("            AND W1.ENTEXAMYEAR=W2.ENTEXAMYEAR  ");
            stb.append("            AND W1.EXAMNO=W2.EXAMNO  ");

        //  '全て'以外の場合（入試制度）
            if (!param[1].equals("0")) 
            stb.append("            AND W1.APPLICANTDIV='"+param[1]+"' ");
            stb.append("            AND W1.TESTDIV='"+param[2]+"' ");

            //受験者指定
            if (param[5].equals("2")) {
                stb.append("        AND W1.EXAMNO = '"+param[6]+"' ");
                //合格通知書・入学金振込用紙
                if (param[4].equals("1") || param[4].equals("5")) {
                    if (param[14] != null) {    /* NO004 */
                        stb.append("        AND W1.JUDGEDIV =  '1' ");
                        stb.append("        AND (W1.HONORDIV <> '1' OR W1.HONORDIV IS NULL) ");
                    }
                }
                //入学手続延期願
                if (param[4].equals("6") && param[14] != null)  /* NO004 */
                    stb.append("        AND W1.JUDGEDIV = '1' ");
            }

            //特待生合格通知書・特待生合格証明書
            if (param[4].equals("2") || param[4].equals("3")) {
                stb.append("        AND W1.JUDGEDIV = '1' ");
                stb.append("        AND W1.HONORDIV = '1' ");
            }
            //不合格通知書  NO005
            if (param[4].equals("4") && param[5].equals("1")) {
                stb.append("        AND W1.JUDGEDIV = '2' ");
            }
            //合格通知書    NO005
            if ((param[4].equals("1") && param[5].equals("3")) ||
                (param[4].equals("5") && param[5].equals("3")) ||
                (param[4].equals("6") && param[5].equals("3"))) {
                stb.append("        AND W1.JUDGEDIV = '1' ");
            }
            //アップ合格
            if (param[4].equals("7")) {
                stb.append("        AND W1.JUDGECLASS = '3' ");
            }
            //スライド合格
            if (param[4].equals("8")) {
                stb.append("        AND W1.JUDGECLASS = '4' ");
            }
            stb.append("            ) T1  ");
            stb.append("    LEFT JOIN ENTEXAM_TESTDIV_MST T4 ON T4.ENTEXAMYEAR='"+param[0]+"' AND T4.TESTDIV=T1.TESTDIV ");
            stb.append("    LEFT JOIN NAME_MST T5 ON T5.NAMECD1='L016' AND T5.NAMECD2=T1.JUDGECLASS ");
            stb.append("ORDER BY  ");
            stb.append("    T1.TESTDIV, T1.EXAMNO ");
        } catch( Exception e ){
            log.error("preStat1 error!");
        }
log.debug(stb);
        return stb.toString();

    }//preStat1()の括り



    /**PrepareStatement close**/
    private void preStatClose(
        PreparedStatement ps1
    ) {
        try {
            ps1.close();
        } catch( Exception e ){
            log.error("preStatClose error!");
        }
    }//preStatClose()の括り



    /** ひらがなカタカナ変換用をセット NO001 */
    private void setMapKana() {
        String obj1[] = {"あ","い","う","え","お","か","き","く","け","こ","さ","し","す","せ","そ",
                         "た","ち","つ","て","と","な","に","ぬ","ね","の","は","ひ","ふ","へ","ほ",
                         "ま","み","む","め","も","や","ゆ","よ","ら","り","る","れ","ろ","わ","を","ん",
                         "ぱ","ぴ","ぷ","ぺ","ぽ","っ","ゃ","ゅ","ょ","ぁ","ぃ","ぅ","ぇ","ぉ",
                         "が","ぎ","ぐ","げ","ご","ざ","じ","ず","ぜ","ぞ","だ","ぢ","づ","で","ど",
                         "ば","び","ぶ","べ","ぼ","　"};
        String obj2[] = {"ア","イ","ウ","エ","オ","カ","キ","ク","ケ","コ","サ","シ","ス","セ","ソ",
                         "タ","チ","ツ","テ","ト","ナ","ニ","ヌ","ネ","ノ","ハ","ヒ","フ","ヘ","ホ",
                         "マ","ミ","ム","メ","モ","ヤ","ユ","ヨ","ラ","リ","ル","レ","ロ","ワ","ヲ","ン",
                         "パ","ピ","プ","ペ","ポ","ッ","ャ","ュ","ョ","ァ","ィ","ゥ","ェ","ォ",
                         "ガ","ギ","グ","ゲ","ゴ","ザ","ジ","ズ","ゼ","ゾ","ダ","ヂ","ヅ","デ","ド",
                         "バ","ビ","ブ","ベ","ボ","　"};
        for( int i=0 ; i<obj1.length ; i++ )hmm.put( obj1[i],obj2[i] );
    }



    /** ひらがなをカタカナに変換 NO001 */
    private String getConvertKana(String kana)
    {
        StringBuffer stb = new StringBuffer();
        if( kana != null ){
            for( int i=0 ; i<kana.length() ; i++ ){
                if( hmm.get(kana.substring(i,i+1)) == null ){
                    stb.append( kana.substring(i,i+1) );
log.info("kana = "+kana.substring(i,i+1));
                } else {
                    stb.append( (hmm.get(kana.substring(i,i+1))) );
                }
            }
        }
        stb.append("");
        return stb.toString();
    }



}//クラスの括り
