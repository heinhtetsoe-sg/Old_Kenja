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
 *	学校教育システム 賢者 [入試管理]
 *
 *					＜ＫＮＪＬ３４０Ｈ＞  繰上げ・特別合格者名簿
 *
 *	2007/11/08 RTS 作成日
 **/

public class KNJL340H {

    private static final Log log = LogFactory.getLog(KNJL340H.class);

	/*----------------------------*
	 * HTTP Get リクエストの処理  *
	 *----------------------------*/
	public void svf_out(HttpServletRequest request, HttpServletResponse response)
	                 throws ServletException, IOException
	{

		Vrw32alp svf = new Vrw32alp(); 			//PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
		DB2UDB db2 = null;						//Databaseクラスを継承したクラス
		String param[] = new String[3];

	//	パラメータの取得
		try {
	        param[0] = request.getParameter("YEAR");         				//年度
	        param[1] = request.getParameter("APDIV");         				//入試制度
	        param[2] = request.getParameter("OUTPUT");         				//帳票種類 1:繰上げ,2:特別
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
		PreparedStatement ps2 = null;
		boolean nonedata = false; 								//該当データなしフラグ
		setHeader(db2,svf,param);
		for(int i=0 ; i<param.length ; i++) log.debug("param["+i+"]="+param[i]);
		//SQL作成
		try {
			ps1 = db2.prepareStatement(preStat1(param));		//名簿一覧preparestatement
			ps2 = db2.prepareStatement(preStat2(param));		//総ページ数preparestatement
		} catch( Exception ex ) {
			log.error("DB2 prepareStatement set error!");
		}
		//SVF出力
		setTotalPage(db2,svf,param,ps2);							//総ページ数メソッド
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
		preStatClose(ps1,ps2);		//preparestatementを閉じる
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
		KNJ_Get_Info getinfo = new KNJ_Get_Info();
		KNJ_Get_Info.ReturnVal returnval = null;
		int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        ret = svf.VrSetForm("KNJL340H.frm", 4);
		//表題の設定
		String sNENDO = convZenkakuToHankaku(param[0]);
		ret = svf.VrsOut("NENDO"	,sNENDO + "年度");
		if (param[2].equals("1")){
    		ret = svf.VrsOut("ITEM", "繰上");
		}
		if (param[2].equals("2")){
    		ret = svf.VrsOut("ITEM", "特別");
		}

		// 作成日(現在処理日)の取得
		try {
			returnval = getinfo.Control(db2);
			ret = svf.VrsOut("DATE"	,fomatSakuseiDate(returnval.val3));
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
		int ret = 0;
        if (false && 0 != ret) { ret = 0; }
		try {
			ResultSet rs = ps2.executeQuery();

			while( rs.next() ){
				if (rs.getString("TOTAL_PAGE") != null) 
					ret = svf.VrsOut("TOTAL_PAGE"	,rs.getString("TOTAL_PAGE"));
			}
			rs.close();
			db2.commit();
		} catch( Exception ex ) {
			log.error("setTotalPage set error!");
		}

	}



	/**帳票出力（名簿一覧をセット）**/
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

			int reccnt_man 		= 0;	//男レコード数カウント用
			int reccnt_woman 	= 0;	//女レコード数カウント用
			int reccnt = 0;				//合計レコード数
			int pagecnt = 1;			//現在ページ数
			int gyo = 1;				//現在ページ数の判断用（行）
			String honordiv = "";		//特待生記号
			String school_name = "";	//学校名称

			while( rs.next() ){
				//レコードを出力
				if (reccnt > 0) ret = svf.VrEndRecord();
				//５０行超えた場合、ページ数カウント
				if (gyo > 50) {
					gyo = 1;
					pagecnt++;
				}
				//ヘッダ
				ret = svf.VrsOut("PAGE"		,String.valueOf(pagecnt));			//現在ページ数
				//明細
				honordiv = ( rs.getString("HONORDIV").equals("1") ) ? "※" : "　";//特待生記号
				ret = svf.VrsOut("TESTDIV" 	,rs.getString("TEST_NAME"));		//入試制度
				ret = svf.VrsOut(setformatArea("NAME", 10, honordiv + rs.getString("NAME"))
						,honordiv + rs.getString("NAME"));	//名前
				ret = svf.VrsOut("EXAMNO" 	,rs.getString("EXAMNO"));			//受験番号
				ret = svf.VrsOut(setformatArea("KANA", 12, rs.getString("NAME_KANA"))
						,rs.getString("NAME_KANA"));		//ふりがな
				ret = svf.VrsOut("SEX" 		,rs.getString("SEX_NAME"));			//性別
				
				//出身学校名
				String ritu_name = nvlT(rs.getString("RITU_NAME"));
				String finschool_name = nvlT(rs.getString("FINSCHOOL_NAME"));
				if(ritu_name.equals("")){
					school_name =  finschool_name;
				} else {
					school_name =  ritu_name + "立" + finschool_name;
				}
				ret = svf.VrsOut(setformatArea("FINSCHOOL", 15, school_name)
						,school_name);			//出身学校名
				
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
				ret = svf.VrsOut("NOTE"	,"男"+String.valueOf(reccnt_man)+"名,女"+String.valueOf(reccnt_woman)+"名,合計"+String.valueOf(reccnt)+"名");
				ret = svf.VrEndRecord();//レコードを出力
			}
			rs.close();
			db2.commit();
		} catch( Exception ex ) {
			log.error("setSvfout set error!");
		}
		return nonedata;
	}



	/**名簿一覧を取得**/
	private String preStat1(String param[])
	{
		StringBuffer stb = new StringBuffer();
	//	パラメータ（なし）
		try {
			stb.append("SELECT ");
			stb.append("    T1.APPLICANTDIV, ");
			stb.append("    T1.EXAMNO, ");
			stb.append("    VALUE(T1.HONORDIV,'0') AS HONORDIV, ");
			stb.append("    T1.NAME, ");
			stb.append("    T1.NAME_KANA, ");
			stb.append("    T1.SEX, ");
			stb.append("    T4.ABBV1 AS TEST_NAME, ");
			stb.append("    T5.ABBV1 AS SEX_NAME, ");
			stb.append("    T7.NAME1 AS RITU_NAME, ");
			stb.append("    T6.FINSCHOOL_NAME, ");
			stb.append("    T1.FS_NAME  ");
			stb.append("FROM ");
			stb.append("    (SELECT W2.APPLICANTDIV, W2.EXAMNO, W2.NAME, W2.NAME_KANA, W2.SEX, W2.FS_NAME, W2.FS_CD, W2.HONORDIV ");
			stb.append("     FROM   ENTEXAM_APPLICANTBASE_DAT W2  ");
			stb.append("     WHERE  W2.ENTEXAMYEAR='"+param[0]+"'  ");
			stb.append("            AND W2.JUDGEMENT='1' ");
			if (param[2].equals("1")) 
			stb.append("            AND W2.SPECIAL_MEASURES='1' ");
			if (param[2].equals("2")) 
			stb.append("            AND W2.SPECIAL_MEASURES='2' ");

			stb.append("            AND W2.APPLICANTDIV='"+param[1]+"' ) T1 ");
			stb.append("    LEFT JOIN NAME_MST T4 ON T4.NAMECD1='L003' AND T4.NAMECD2=T1.APPLICANTDIV ");
			stb.append("    LEFT JOIN NAME_MST T5 ON T5.NAMECD1='Z002' AND T5.NAMECD2=T1.SEX ");
            stb.append("    LEFT JOIN FINSCHOOL_MST T6 ON  ");
			stb.append("              T6.FINSCHOOLCD = T1.FS_CD  ");
			stb.append("    LEFT JOIN NAME_MST T7 ON  ");
			stb.append("              T7.NAMECD1 = 'L001'  ");
			stb.append("          AND T7.NAMECD2 = T6.FINSCHOOL_DISTCD ");
			stb.append("ORDER BY  ");
			stb.append("    T1.EXAMNO ");
		} catch( Exception e ){
			log.error("preStat1 error!");
		}
		return stb.toString();

	}//preStat1()の括り



	/**総ページ数を取得**/
	private String preStat2(String param[])
	{
		StringBuffer stb = new StringBuffer();
	//	パラメータ（なし）
		try {
			stb.append("SELECT ");
			stb.append("    SUM(T1.TEST_CNT) TOTAL_PAGE  ");
			stb.append("FROM ");
			stb.append("    (SELECT CASE WHEN MOD(COUNT(*),50) > 0 THEN COUNT(*)/50 + 1 ELSE COUNT(*)/50 END TEST_CNT  ");
			stb.append("     FROM   ENTEXAM_APPLICANTBASE_DAT W2  ");
			stb.append("     WHERE  W2.ENTEXAMYEAR='"+param[0]+"'  ");

			if (param[2].equals("1")) 
			stb.append("            AND W2.SPECIAL_MEASURES='1' ");	//繰上げ
			if (param[2].equals("2")) 
			stb.append("            AND W2.SPECIAL_MEASURES='2' ");	//特別

			stb.append("            AND W2.APPLICANTDIV='"+param[1]+"' ) T1 ");
		} catch( Exception e ){
			log.error("preStat2 error!");
		}
		return stb.toString();

	}//preStat2()の括り



	/**PrepareStatement close**/
	private void preStatClose(
		PreparedStatement ps1,
		PreparedStatement ps2
	) {
		try {
			ps1.close();
			ps2.close();
		} catch( Exception e ){
			log.error("preStatClose error!");
		}
	}//preStatClose()の括り

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
     * 帳票に設定する文字数が制限文字超の場合
     * 帳票設定エリアの変更を行う
     * @param area_name	帳票出力エリア
     * @param area_len		制限文字数
     * @param sval			値
     * @return
     */
    private String setformatArea(String area_name, int area_len, String sval) {

    	String retAreaName = "";
		// 値がnullの場合はnullが返される
    	if (sval == null) {
			return null;
		}
    	// 設定値が制限文字超の場合、帳票設定エリアの変更を行う
    	if(area_len >= sval.length()){
   			retAreaName = area_name + "1";
    	} else {
   			retAreaName = area_name + "2";
    	}
        return retAreaName;
    }

    /**
	 * NULL値を""として返す。
	 */
	private String nvlT(String val) {

		if (val == null) {
			return "";
		} else {
			return val;
		}
	}

}//クラスの括り
