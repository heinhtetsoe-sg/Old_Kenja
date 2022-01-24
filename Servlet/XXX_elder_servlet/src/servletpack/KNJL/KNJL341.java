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

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 *
 *	学校教育システム 賢者 [入試管理]
 *
 *					＜ＫＮＪＬ３４１＞  
 *					＜１＞  繰上合格通知書
 *					＜２＞  特別合格通知書
 *					＜３＞  入学金振込用紙
 *
 *	2005/01/24 nakamoto 作成日
 *	2005/02/01 nakamoto 志願者基礎データのテーブル変更。SPECIAL_MEASURES（特別設置区分）1:繰上合格,2:特別合格 NO001
 *
 **/

public class KNJL341 {

    private static final Log log = LogFactory.getLog(KNJL341.class);

	private Map hmm = new HashMap();	//ひらがなからカタカナの変換用 NO001

	/*----------------------------*
	 * HTTP Get リクエストの処理  *
	 *----------------------------*/
	public void svf_out(HttpServletRequest request, HttpServletResponse response)
	                 throws ServletException, IOException
	{

		Vrw32alp svf = new Vrw32alp(); 			//PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
		DB2UDB db2 = null;						//Databaseクラスを継承したクラス
		String param[] = new String[12];

	//	パラメータの取得
		try {
	        param[0] = request.getParameter("YEAR");         				//年度
	        param[1] = request.getParameter("APDIV");         				//入試制度
	        param[3] = request.getParameter("NOTICEDAY");         			//通知日付

	        param[4] = request.getParameter("OUTPUT");         				//帳票種類 1,2,3
            param[11] = request.getParameter("TESTDIV");                    //入試区分 9:全体 9以外

		if (param[4].equals("1")) {
	        param[5] = request.getParameter("OUTPUTA");         			//受験者(1) 1:全員2:指定
	        param[6] = request.getParameter("EXAMNOA");         			//受験番号
		}
		if (param[4].equals("2")) {
	        param[5] = request.getParameter("OUTPUTB");         			//受験者(2) 1:全員2:指定
	        param[6] = request.getParameter("EXAMNOB");         			//受験番号
		}
		if (param[4].equals("3")) {
	        param[5] = request.getParameter("OUTPUTC");         			//受験者(3) 1:全員2:指定
	        param[6] = request.getParameter("EXAMNOC");         			//受験番号
		}
        if (param[4].equals("4")) {
            param[5] = request.getParameter("OUTPUTD");                     //受験者(1) 1:全員2:指定
            param[6] = request.getParameter("EXAMNOD");                     //受験番号
        }

		} catch( Exception ex ) {
			log.error("parameter error!");
		}

	//	print設定
		PrintWriter outstrm = new PrintWriter (response.getOutputStream());
		response.setContentType("application/pdf");

	//	svf設定
		int ret = svf.VrInit();						   		//クラスの初期化
        if (false && 0 != ret) { ret = 0; }
		ret = svf.VrSetSpoolFileStream(response.getOutputStream());  		//PDFファイル名の設定

	//	ＤＢ接続
		try {
			db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
			db2.open();
		} catch( Exception ex ) {
			log.error("DB2 open error!");
			return;
		}


	//	ＳＶＦ作成処理
		PreparedStatement ps1 = null;
		boolean nonedata = false; 								//該当データなしフラグ
		setHeader(db2,svf,param);
		setMapKana();
for(int i=0 ; i<param.length ; i++) log.debug("param["+i+"]="+param[i]);
		//SQL作成
		try {
			ps1 = db2.prepareStatement(preStat1(param));		//各通知書preparestatement
		} catch( Exception ex ) {
			log.error("DB2 prepareStatement set error!");
		}
		//SVF出力
		if( setSvfout(db2,svf,param,ps1) ){							//帳票出力のメソッド
			nonedata = true;
		}

	//	該当データ無し
		if( !nonedata ){
			ret = svf.VrSetForm("MES001.frm", 0);
			ret = svf.VrsOut("note" , "note");
			ret = svf.VrEndPage();
		}

	// 	終了処理
		ret = svf.VrQuit();
		preStatClose(ps1);		//preparestatementを閉じる
		db2.commit();
		db2.close();				//DBを閉じる
		outstrm.close();			//ストリームを閉じる 

    }//doGetの括り



	/** 事前処理 **/
	private void setHeader(
		DB2UDB db2,
		Vrw32alp svf,
		String param[]
	) {
		int ret = 0;
        if (false && 0 != ret) { ret = 0; }
	//	帳票フォーム
		if (param[4].equals("1")) ret = svf.VrSetForm("KNJL327_12.frm", 4);
		if (param[4].equals("2")) ret = svf.VrSetForm("KNJL341.frm", 4);
		if (param[4].equals("3")) ret = svf.VrSetForm("KNJL327_5.frm", 4);
        if (param[4].equals("4")) ret = svf.VrSetForm("KNJL327_12.frm", 4); //特別アップ合格通知書

		try {
			//通知日付(1,2)
			param[10] = KNJ_EditDate.h_format_JP(param[3]);
		} catch( Exception e ){
			log.error("setHeader set error!");
		}
	}



    private List getTextList(String param[], final ResultSet rs) throws Exception {
        final List textList = new ArrayList();
        final String entclassName = rs.getString("ENTCLASS_NAME");

        if (param[4].equals("4")) {
            //H.特別アップ
            textList.add("合格おめでとうございます。");
            textList.add("あなたは、特別進学クラスに３回合格されましたので、");
            textList.add("英数特科クラスにも合格です。");
        } else {
            //I.繰上
            textList.add("おめでとうございます。");
            textList.add(entclassName + "に合格です。");
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
				//繰上・特別・特別アップ合格通知書
				if (param[4].equals("1") || param[4].equals("2") || param[4].equals("4")) {
					ret = svf.VrsOut("EXAMNO" 	,rs.getString("EXAMNO"));		            //受験番号
	                if( rs.getString("NAME") != null  &&  12 < rs.getString("NAME").length() )
	                    ret = svf.VrsOut("NAME2" 	,rs.getString("NAME"));			        //名前：１３文字以上
	                else
	                    ret = svf.VrsOut("NAME1" 	,rs.getString("NAME"));			        //名前：１３文字未満
                    if (param[4].equals("4")) {
                        ret = svf.VrsOut("TESTCLASS" , "特別進学クラス");
                    }
                    ret = svf.VrsOut("ENTCLASS" , rs.getString("ENTCLASS_NAME"));
                    ret = svf.VrsOut("DATE"		,param[10]);					            //通知日付
                    //繰上・特別アップ合格通知書
                    if (param[4].equals("1") || param[4].equals("4")) {
                        final List textList = getTextList(param, rs);
                        int gyo = 0;
                        for (final Iterator it = textList.iterator(); it.hasNext();) {
                            final String text = (String) it.next();
                            gyo++;
                            ret = svf.VrsOut("TEXT" + gyo, text);
                        }
                    }
				}
				//入学金振込用紙
				if (param[4].equals("3")) {
					ret = svf.VrsOut("EXAMNO1" 	,rs.getString("EXAMNO"));		            //受験番号
	                if( rs.getString("NAME") != null  &&  10 < rs.getString("NAME").length() )
	                    ret = svf.VrsOut("NAME1_2" 	,rs.getString("NAME"));			        //名前：１１文字以上
	                else
	                    ret = svf.VrsOut("NAME1_1" 	,rs.getString("NAME"));			        //名前：１１文字未満
					ret = svf.VrsOut("EXAMNO2" 	,rs.getString("EXAMNO"));		            //受験番号
	                if( rs.getString("NAME") != null  &&  10 < rs.getString("NAME").length() )
	                    ret = svf.VrsOut("NAME2_2" 	,rs.getString("NAME"));			        //名前：１１文字以上
	                else
	                    ret = svf.VrsOut("NAME2_1" 	,rs.getString("NAME"));			        //名前：１１文字未満
					ret = svf.VrsOut("EXAMNO3" 	,rs.getString("EXAMNO"));		            //受験番号
	                if( rs.getString("NAME") != null  &&  12 < rs.getString("NAME").length() )
	                    ret = svf.VrsOut("NAME3_2" 	,rs.getString("NAME"));			        //名前：１３文字以上
	                else
	                    ret = svf.VrsOut("NAME3_1" 	,rs.getString("NAME"));			        //名前：１３文字未満
	                if( rs.getString("NAME_KANA") != null  &&  12 < rs.getString("NAME_KANA").length() )
					    ret = svf.VrsOut("KANA3_2" 	,getConvertKana(rs.getString("NAME_KANA")) );//ふりがな：１３文字以上
	                else
					    ret = svf.VrsOut("KANA3_1" 	,getConvertKana(rs.getString("NAME_KANA")) );//ふりがな：１３文字未満
				}
				ret = svf.VrEndRecord();//レコードを出力
				nonedata = true;
			}//while
			rs.close();
			db2.commit();
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
	//	パラメータ（なし）
		try {
			stb.append("SELECT ");
			stb.append("    T1.EXAMNO, ");
			stb.append("    T1.NAME, ");
			stb.append("    T1.NAME_KANA, ");
            stb.append("    T1.ENTCLASS_NAME ");
			stb.append("FROM ");
			stb.append("    (SELECT EXAMNO, NAME, NAME_KANA, L1.NAME1 AS ENTCLASS_NAME  ");
			stb.append("     FROM   ENTEXAM_APPLICANTBASE_DAT ");
            stb.append("     LEFT JOIN NAME_MST L1 ON ");
            stb.append("            L1.NAMECD1='L017' AND L1.NAMECD2=ENTCLASS ");
			stb.append("     WHERE  ENTEXAMYEAR='"+param[0]+"'  ");
			stb.append("            AND APPLICANTDIV='"+param[1]+"' ");

			//繰上合格通知書
			if (param[4].equals("1")) 
				stb.append("        AND SPECIAL_MEASURES = '1' ");// NO001
				//stb.append("        AND JUDGEMENT = '2' ");// NO001
			//特別合格通知書
			if (param[4].equals("2")) 
				stb.append("        AND SPECIAL_MEASURES = '2' ");// NO001
				//stb.append("        AND JUDGEMENT = '3' ");// NO001
			//入学金振込用紙
			if (param[4].equals("3")) 
				stb.append("        AND SPECIAL_MEASURES IN ('1','2','3') ");// NO001
				//stb.append("        AND JUDGEMENT IN ('2','3') ");// NO001
            //特別アップ合格通知書
            if (param[4].equals("4")) 
                stb.append("        AND SPECIAL_MEASURES = '3' ");
			//受験者指定
			if (param[5].equals("2")) 
				stb.append("        AND EXAMNO = '"+param[6]+"' ");

			stb.append("            ) T1  ");
            //特別アップ合格通知書
            if (param[4].equals("4") && !"9".equals(param[11]) && null != param[11]) {
                stb.append("  INNER JOIN ENTEXAM_RECEPT_DAT T3 ON  T3.ENTEXAMYEAR = '"+param[0]+"' ");
                stb.append("                                   AND T3.APPLICANTDIV = '"+param[1]+"' ");
                stb.append("                                   AND T3.TESTDIV = '"+param[11]+"' ");
                stb.append("                                   AND T3.EXAMNO = T1.EXAMNO ");
            }
			stb.append("ORDER BY  ");
			stb.append("    T1.EXAMNO ");
		} catch( Exception e ){
			log.error("preStat1 error!");
		}
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
