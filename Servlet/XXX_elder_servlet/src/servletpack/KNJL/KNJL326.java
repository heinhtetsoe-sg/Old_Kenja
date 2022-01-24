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
 *	学校教育システム 賢者 [入試管理]
 *
 *					＜ＫＮＪＬ３２６＞  特待生合格者成績順リスト
 *
 *	2004/12/24 nakamoto 作成日
 *	2004/12/27 nakamoto 総ページ数は入試区分毎に出力	NO001
 *  2005/01/05 科目コードを社会:3 理科:4に変更
 *	2005/01/13 nakamoto コミットが入れられるところではコミットする	NO002
 *
 **/

public class KNJL326 {

    private static final Log log = LogFactory.getLog(KNJL326.class);

	/*----------------------------*
	 * HTTP Get リクエストの処理  *
	 *----------------------------*/
	public void svf_out(HttpServletRequest request, HttpServletResponse response)
	                 throws ServletException, IOException
	{

		Vrw32alp svf = new Vrw32alp(); 			//PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
		DB2UDB db2 = null;						//Databaseクラスを継承したクラス
		String param[] = new String[4];

	//	パラメータの取得
		try {
	        param[0] = request.getParameter("YEAR");         				//年度
	        param[1] = request.getParameter("APDIV");         				//入試制度
	        param[2] = request.getParameter("TESTDV");         				//入試区分
            param[3] = request.getParameter("SORT");                        //表示順 1:成績順 2:受験番号順
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
		PreparedStatement ps = null;	/* NO001 */
		PreparedStatement ps1 = null;
		PreparedStatement ps2 = null;
		boolean nonedata = false; 								//該当データなしフラグ
		setHeader(db2,svf,param);
for(int i=0 ; i<param.length ; i++) log.debug("param["+i+"]="+param[i]);
		//SQL作成
		try {
			ps = db2.prepareStatement(preStat(param));			//入試区分preparestatement	/* NO001 */
			ps1 = db2.prepareStatement(preStat1(param));		//一覧preparestatement
			ps2 = db2.prepareStatement(preStat2(param));		//総ページ数preparestatement
		} catch( Exception ex ) {
			log.error("DB2 prepareStatement set error!");
		}
		//SVF出力
		if (setSvfMain(db2,svf,param,ps,ps1,ps2)) nonedata = true;	//帳票出力のメソッド	/* NO001 */
		/*
		setTotalPage(svf,param,ps2);							//総ページ数メソッド
		if( setSvfout(svf,param,ps1) ){							//帳票出力のメソッド
			nonedata = true;
		}
		*/
	//	該当データ無し
		if( !nonedata ){
			ret = svf.VrSetForm("MES001.frm", 0);
			ret = svf.VrsOut("note" , "note");
			ret = svf.VrEndPage();
		}

	// 	終了処理
		ret = svf.VrQuit();
		preStatClose(ps,ps1,ps2);		//preparestatementを閉じる	/* NO001 */
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
		ret = svf.VrSetForm("KNJL326.frm", 4);
		ret = svf.VrsOut("NENDO"	,KenjaProperties.gengou(Integer.parseInt(param[0]))+"年度");

	//	ＳＶＦ属性変更--->改ページ
		ret = svf.VrAttribute("TESTDIV","FF=1");

	//	作成日(現在処理日)の取得
		try {
			returnval = getinfo.Control(db2);
			ret = svf.VrsOut("DATE"	,KNJ_EditDate.h_format_JP(returnval.val3));
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
     */	/* NO001 */
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
			setTotalPage(db2,svf,param,ps2,param[2]);							//総ページ数メソッド
			if (setSvfout(db2,svf,param,ps1,param[2])) nonedata = true;			//帳票出力のメソッド
			return nonedata;
        }

		try {
			ResultSet rs = ps.executeQuery();

			while( rs.next() ){
				setTotalPage(db2,svf,param,ps2, rs.getString("TESTDIV"));						//総ページ数メソッド
				if (setSvfout(db2,svf,param,ps1, rs.getString("TESTDIV"))) nonedata = true;		//帳票出力のメソッド
			}
			rs.close();
			db2.commit();	/* NO002 */
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
			ps2.setString( ++p, test_div );	/* NO001 */
			ResultSet rs = ps2.executeQuery();

			while( rs.next() ){
				if (rs.getString("TOTAL_PAGE") != null) 
					ret = svf.VrsOut("TOTAL_PAGE"	,rs.getString("TOTAL_PAGE"));
			}
			rs.close();
			db2.commit();	/* NO002 */
		} catch( Exception ex ) {
			log.error("setTotalPage set error!");
		}

	}



	/**帳票出力（一覧をセット）**/
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
			ps1.setString( ++p, test_div );	/* NO001 */
			ResultSet rs = ps1.executeQuery();

			int reccnt_man 		= 0;	//男レコード数カウント用
			int reccnt_woman 	= 0;	//女レコード数カウント用
			int reccnt = 0;				//合計レコード数
			int pagecnt = 1;			//現在ページ数
			int gyo = 1;				//現在ページ数の判断用（行）
			String testdiv = "0";		//現在ページ数の判断用（入試区分）

			int rankcnt = 0;			//順位カウント用
			int rank = 0;				//順位
			String total4 = "0";		//４科計

			while( rs.next() ){
				//レコードを出力
				if (reccnt > 0) ret = svf.VrEndRecord();
				//５０行超えたとき、または、入試区分ブレイクの場合、ページ数カウント
				if ((gyo > 50) || (!testdiv.equals(rs.getString("TESTDIV")) && !testdiv.equals("0"))) {
					gyo = 1;
					pagecnt++;
				}
				//入試区分ブレイクの場合、順位カウントを初期化
				if (!testdiv.equals(rs.getString("TESTDIV")) && !testdiv.equals("0")) {
					rankcnt = 0;
					rank = 0;
					total4 = "0";
				}
				//順位カウント
				rankcnt++;
                if (!total4.equals(rs.getString("TOTAL4"))) {
                    rank = rankcnt;
                    total4 = rs.getString("TOTAL4");
                }
				//ヘッダ
				ret = svf.VrsOut("PAGE"		,String.valueOf(pagecnt));		//現在ページ数
				ret = svf.VrsOut("TESTDIV" 	,rs.getString("TEST_NAME"));	//入試区分
				//明細
				ret = svf.VrsOut("RANK" 	,String.valueOf(rank));			//順位
				ret = svf.VrsOut("EXAMNO" 	,rs.getString("EXAMNO"));		//受験番号
				ret = svf.VrsOut("NAME" 	,rs.getString("NAME"));			//名前
				ret = svf.VrsOut("SEX" 		,rs.getString("SEX_NAME"));		//性別
				ret = svf.VrsOut("POINT1"	,rs.getString("SCORE1"));		//得点（国語）
				ret = svf.VrsOut("POINT2"	,rs.getString("SCORE2"));		//得点（算数）
				ret = svf.VrsOut("POINT3"	,rs.getString("SCORE3"));		//得点（社会）05/01/05Modify
				ret = svf.VrsOut("POINT4"	,rs.getString("SCORE4"));		//得点（理科）05/01/05Modify
				if (!rs.getString("TOTAL4").equals("0")) 
					ret = svf.VrsOut("4TOTAL"	,rs.getString("TOTAL4"));	//４科計
				if (rs.getString("TOTAL4").equals("0")) 
					ret = svf.VrsOut("4TOTAL"	,"");						//４科計
				ret = svf.VrsOut("ABSENCE"	,rs.getString("ABSENCE_DAYS"));	//欠席数
				//備考(１０文字超えたかどうかで、どのフィールドにセットするかを決める)
				for (int i=1; i<3; i++) {
					//最初は両方にセット
					ret = svf.VrsOut("NOTE"+String.valueOf(i)+"_1"	,rs.getString("REMARK"+String.valueOf(i)));	//備考(20)
					ret = svf.VrsOut("NOTE"+String.valueOf(i)+"_2"	,rs.getString("REMARK"+String.valueOf(i)));	//備考(40)
					//セットしない片方をクリア
					if (rs.getString("REMARK"+String.valueOf(i)) != null) {
						if (rs.getString("REMARK"+String.valueOf(i)).length() > 10) {
							ret = svf.VrsOut("NOTE"+String.valueOf(i)+"_1"	,"");		//備考(20)
						} else {
							ret = svf.VrsOut("NOTE"+String.valueOf(i)+"_2"	,"");		//備考(40)
						}
					}
				}
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
				ret = svf.VrsOut("NOTE"	,"男"+String.valueOf(reccnt_man)+"名,女"+String.valueOf(reccnt_woman)+"名,合計"+String.valueOf(reccnt)+"名");
				ret = svf.VrEndRecord();//レコードを出力
				setSvfInt(svf);			//ブランクセット	/* NO001 */
			}
			rs.close();
			db2.commit();	/* NO002 */
		} catch( Exception ex ) {
			log.error("setSvfout set error!");
		}
		return nonedata;
	}



	/**入試区分を取得**/	/* NO001 */
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



	/**一覧を取得**/
	private String preStat1(String param[])
	{
		StringBuffer stb = new StringBuffer();
	//	パラメータ（入試区分）
		try {
		//	受付・得点
			stb.append("WITH SCORE AS ( ");
			stb.append("    SELECT W1.TESTDIV, W1.EXAMNO, W1.TOTAL2, W1.TOTAL4, ");
			stb.append("           SUM(CASE WHEN W3.TESTSUBCLASSCD='1' THEN W3.SCORE ELSE NULL END) AS SCORE1, ");
			stb.append("           SUM(CASE WHEN W3.TESTSUBCLASSCD='2' THEN W3.SCORE ELSE NULL END) AS SCORE2, ");
			stb.append("           SUM(CASE WHEN W3.TESTSUBCLASSCD='3' THEN W3.SCORE ELSE NULL END) AS SCORE3, ");
			stb.append("           SUM(CASE WHEN W3.TESTSUBCLASSCD='4' THEN W3.SCORE ELSE NULL END) AS SCORE4  ");
			stb.append("    FROM   ENTEXAM_RECEPT_DAT W1, ENTEXAM_SCORE_DAT W3  ");
			stb.append("    WHERE  W1.ENTEXAMYEAR='"+param[0]+"'  ");
			stb.append("           AND W1.ENTEXAMYEAR=W3.ENTEXAMYEAR  ");
			stb.append("           AND W1.APPLICANTDIV=W3.APPLICANTDIV  ");
			stb.append("           AND W1.TESTDIV=W3.TESTDIV  ");
			stb.append("           AND W1.EXAM_TYPE=W3.EXAM_TYPE  ");
			stb.append("           AND W1.RECEPTNO=W3.RECEPTNO  ");
			if (!param[1].equals("0")) //'全て'以外の場合（入試制度）
			stb.append("            AND W1.APPLICANTDIV='"+param[1]+"' ");
//			if (!param[2].equals("0")) //'全て'以外の場合（入試区分）
//			stb.append("            AND W1.TESTDIV='"+param[2]+"' ");
			stb.append("            AND W1.TESTDIV=? ");	/* NO001 */
			stb.append("            AND W1.JUDGEDIV='1'  ");
			stb.append("            AND W1.HONORDIV='1'  ");
			stb.append("    GROUP BY W1.TESTDIV, W1.EXAMNO, W1.TOTAL2, W1.TOTAL4 )  ");

		//	メイン
			stb.append("SELECT ");
			stb.append("    T1.TESTDIV, T4.NAME AS TEST_NAME, T1.EXAMNO, ");
			stb.append("    T1.NAME, T1.SEX, T5.ABBV1 AS SEX_NAME, ");
			stb.append("    T1.SCORE1, T1.SCORE2, T1.TOTAL2, ");
			stb.append("    T1.SCORE4, T1.SCORE3, VALUE(T1.TOTAL4,0) TOTAL4, ");
			stb.append("    T6.ABSENCE_DAYS, T1.REMARK1, T1.REMARK2  ");
			stb.append("FROM ");
			stb.append("    (SELECT W1.TESTDIV, W1.EXAMNO,  ");
			stb.append("            W1.SCORE1, W1.SCORE2, W1.SCORE3, W1.SCORE4, W1.TOTAL2, W1.TOTAL4,  ");
			stb.append("            W2.NAME, W2.SEX, W2.REMARK1, W2.REMARK2  ");
			stb.append("     FROM   SCORE W1, ENTEXAM_APPLICANTBASE_DAT W2  ");
			stb.append("     WHERE  W2.ENTEXAMYEAR='"+param[0]+"' AND W1.EXAMNO=W2.EXAMNO ) T1  ");

            stb.append("    LEFT JOIN ENTEXAM_TESTDIV_MST T4 ON T4.ENTEXAMYEAR='"+param[0]+"' AND T4.TESTDIV=T1.TESTDIV ");
			stb.append("    LEFT JOIN NAME_MST T5 ON T5.NAMECD1='Z002' AND T5.NAMECD2=T1.SEX ");
			stb.append("    LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT T6 ON T6.ENTEXAMYEAR='"+param[0]+"' AND T6.EXAMNO=T1.EXAMNO ");
			stb.append("ORDER BY  ");
			stb.append("    T1.TESTDIV ");
			//表示順 1:成績順 2:受験番号順
			if ("1".equals(param[3])) {
	            stb.append("    ,VALUE(T1.TOTAL4,0) DESC ");
			}
            stb.append("    ,T1.EXAMNO ");
		} catch( Exception e ){
			log.error("preStat1 error!");
		}
		return stb.toString();

	}//preStat1()の括り



	/**総ページ数を取得**/
	private String preStat2(String param[])
	{
		StringBuffer stb = new StringBuffer();
	//	パラメータ（入試区分）
		try {
		//	受付・得点
			stb.append("WITH SCORE AS ( ");
			stb.append("    SELECT W1.TESTDIV, W1.EXAMNO, W1.TOTAL2, W1.TOTAL4, ");
			stb.append("           SUM(CASE WHEN W3.TESTSUBCLASSCD='1' THEN W3.SCORE ELSE NULL END) AS SCORE1, ");
			stb.append("           SUM(CASE WHEN W3.TESTSUBCLASSCD='2' THEN W3.SCORE ELSE NULL END) AS SCORE2, ");
			stb.append("           SUM(CASE WHEN W3.TESTSUBCLASSCD='3' THEN W3.SCORE ELSE NULL END) AS SCORE3, ");
			stb.append("           SUM(CASE WHEN W3.TESTSUBCLASSCD='4' THEN W3.SCORE ELSE NULL END) AS SCORE4  ");
			stb.append("    FROM   ENTEXAM_RECEPT_DAT W1, ENTEXAM_SCORE_DAT W3  ");
			stb.append("    WHERE  W1.ENTEXAMYEAR='"+param[0]+"'  ");
			stb.append("           AND W1.ENTEXAMYEAR=W3.ENTEXAMYEAR  ");
			stb.append("           AND W1.APPLICANTDIV=W3.APPLICANTDIV  ");
			stb.append("           AND W1.TESTDIV=W3.TESTDIV  ");
			stb.append("           AND W1.EXAM_TYPE=W3.EXAM_TYPE  ");
			stb.append("           AND W1.RECEPTNO=W3.RECEPTNO  ");
			if (!param[1].equals("0")) //'全て'以外の場合（入試制度）
			stb.append("            AND W1.APPLICANTDIV='"+param[1]+"' ");
//			if (!param[2].equals("0")) //'全て'以外の場合（入試区分）
//			stb.append("            AND W1.TESTDIV='"+param[2]+"' ");
			stb.append("            AND W1.TESTDIV=? ");	/* NO001 */
			stb.append("            AND W1.JUDGEDIV='1'  ");
			stb.append("            AND W1.HONORDIV='1'  ");
			stb.append("    GROUP BY W1.TESTDIV, W1.EXAMNO, W1.TOTAL2, W1.TOTAL4 )  ");

		//	メイン
			stb.append("SELECT ");
			stb.append("    SUM(T1.TEST_CNT) TOTAL_PAGE  ");
			stb.append("FROM ");
			stb.append("    (SELECT CASE WHEN MOD(COUNT(*),50) > 0 THEN COUNT(*)/50 + 1 ELSE COUNT(*)/50 END TEST_CNT  ");
			stb.append("     FROM   SCORE W1, ENTEXAM_APPLICANTBASE_DAT W2  ");
			stb.append("     WHERE  W2.ENTEXAMYEAR='"+param[0]+"' AND W1.EXAMNO=W2.EXAMNO ");
			stb.append("     GROUP BY W1.TESTDIV ) T1  ");
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
			ps.close();	/* NO001 */
			ps1.close();
			ps2.close();
		} catch( Exception e ){
			log.error("preStatClose error!");
		}
	}//preStatClose()の括り



	/**ブランクをセット**/	/* NO001 */
	private void setSvfInt(
		Vrw32alp svf
	) {
		try {
			svf.VrsOut("NOTE"	,"");
		} catch( Exception ex ) {
			log.error("setSvfInt set error!");
		}
	}

}//クラスの括り
