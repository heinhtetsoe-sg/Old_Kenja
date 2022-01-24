package servletpack.KNJL;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;

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
 *					＜ＫＮＪＬ３２５＞  入試統計資料
 *
 *	2004/12/28 nakamoto 作成日
 *  2005/01/05 合格者数・最高点・最高点・最低点の２科と４科は志願者受付データの判定受験型で判別する
 *             最低点について得点を出力するかハイフンを出力するかの選択を追加( => 指示画面で選択 )
 *             合格者数・最高点・最高点・最低点について前回合格者を含めるか含めないかの選択を追加( => 指示画面で選択 )
 *	2005/01/07 nakamoto ４科計最高点は、受験型='2'の条件に変更
 *             平均点男女が０の場合、"−"を表示する
 *  2005/01/08 合格者数は志願者受付データの受験型で判別する => 01/05の変更を元に戻す
 *	2005/01/12 nakamoto コミットが入れられるところではコミットする	NO001
 *	2005/01/18 nakamoto ２科計４科計の最低点は、合格点マスタの合格最低点以上を表示する	NO002
 *	2005/02/02 nakamoto NO002のバグ修正	NO003
 *	2006/01/15 m-yama   前回含む場合の処理を修正	NO004
 **/

public class KNJL325 {

    private static final Log log = LogFactory.getLog(KNJL325.class);

    DecimalFormat df = new DecimalFormat("0.0");   // 05/01/10Modify

	/*----------------------------*
	 * HTTP Get リクエストの処理  *
	 *----------------------------*/
	public void svf_out(HttpServletRequest request, HttpServletResponse response)
	                 throws ServletException, IOException
	{

		Vrw32alp svf = new Vrw32alp(); 			//PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
		DB2UDB db2 = null;						//Databaseクラスを継承したクラス
		String param[] = new String[9];

	//	パラメータの取得
		try {
	        param[0] = request.getParameter("YEAR");         //年度
	        param[1] = request.getParameter("APDIV");        //入試制度
	        param[2] = request.getParameter("TESTDV");       //入試区分
            if( request.getParameter("CHECK1") != null ) param[7] = request.getParameter("CHECK1");  //前回までの合格者対象者に含める 05/01/05
	        if( request.getParameter("CHECK2") != null ) param[8] = request.getParameter("CHECK2");  //最低点の表示なし 05/01/05
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
		PreparedStatement ps = null;
		PreparedStatement ps1 = null;
		PreparedStatement ps2 = null;
		PreparedStatement ps3 = null;
		boolean nonedata = false; 								//該当データなしフラグ
		setHeader(db2,svf,param);
for(int i=0 ; i<5 ; i++) log.debug("param["+i+"]="+param[i]);
		//SQL作成
		try {
			ps = db2.prepareStatement(preStat(param));			//入試区分preparestatement
			ps1 = db2.prepareStatement(preStat1(param));		//速報データ（人数）preparestatement
			ps2 = db2.prepareStatement(preStat2(param));		//名称preparestatement
			ps3 = db2.prepareStatement(getJudgeAvarage(param));		//速報データ（得点）preparestatement
		} catch( Exception ex ) {
			log.error("DB2 prepareStatement set error!");
		}
		//SVF出力
		if (setSvfMain(db2,svf,param,ps,ps1,ps2,ps3)) nonedata = true;	//帳票出力のメソッド

	//	該当データ無し
		if( !nonedata ){
			ret = svf.VrSetForm("MES001.frm", 0);
			ret = svf.VrsOut("note" , "note");
			ret = svf.VrEndPage();
		}

	// 	終了処理
		ret = svf.VrQuit();
		preStatClose(ps,ps1,ps2,ps3);		//preparestatementを閉じる
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
		param[3] = KenjaProperties.gengou(Integer.parseInt(param[0]))+"年度";

	//	ＳＶＦ属性変更--->改ページ
		//ret = svf.VrAttribute("TESTDIV","FF=1");

	//	作成日(現在処理日)の取得
		try {
			returnval = getinfo.Control(db2);
			param[4] = KNJ_EditDate.h_format_JP(returnval.val3);
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
		PreparedStatement ps3
	) {
		boolean nonedata = false;

        if( ! param[2].equals("9") ){
			setTestNameDate(db2,svf,param,ps2,param[2]);						//名称メソッド
for(int i=5 ; i<param.length ; i++) log.debug("param["+i+"]="+param[i]);
			if (setSvfout1(db2,svf,param,ps1,param[2])) nonedata = true;			//速報データ（人数）出力のメソッド
			if (setSvfout2(db2,svf,param,ps3,param[2])) nonedata = true;			//速報データ（得点）出力のメソッド
			return nonedata;
        }

		try {
			ResultSet rs = ps.executeQuery();

			while( rs.next() ){
				setTestNameDate(db2,svf,param,ps2, rs.getString("TESTDIV"));					//名称メソッド
for(int i=5 ; i<param.length ; i++) log.debug("param["+i+"]="+param[i]);
				if (setSvfout1(db2,svf,param,ps1, rs.getString("TESTDIV"))) nonedata = true;	//速報データ（人数）のメソッド
				if (setSvfout2(db2,svf,param,ps3, rs.getString("TESTDIV"))) nonedata = true;	//速報データ（得点）のメソッド
			}
			rs.close();
			db2.commit();	/* NO001 */
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
			ps2.setString( ++p, test_div );
			ResultSet rs = ps2.executeQuery();

			while( rs.next() ){
				param[5] = rs.getString("TEST_NAME");								//入試区分
				param[6] = KNJ_EditDate.h_format_JP_MD(rs.getString("TEST_DATE"));	//入試日付
			}
			rs.close();
			db2.commit();	/* NO001 */
		} catch( Exception ex ) {
			log.error("setTestNameDate set error!");
		}

	}



	/**帳票出力（速報データ（人数）をセット）**/
	private boolean setSvfout1(
		DB2UDB db2,
		Vrw32alp svf,
		String param[],
		PreparedStatement ps1,
		String test_div
	) {
		boolean nonedata = false;
		int ret = 0;
        if (false && 0 != ret) { ret = 0; }
		ret = svf.VrSetForm("KNJL325_1.frm", 1);	//セットフォーム
		try {
			setTitle(svf,param);		//見出しメソッド

            int p = 0;
			ps1.setString( ++p, test_div );
			ps1.setString( ++p, test_div );
			ps1.setString( ++p, test_div );
            if( param[7] == null )ps1.setString( ++p, test_div ); //前回までの合格者を対象に含めない場合 05/01/05
			ResultSet rs = ps1.executeQuery();

			int gyo = 0;				//行

			while( rs.next() ){
				//明細
				if (rs.getString("TYPE_CNT").equals("A")) gyo = 1;		//志願者開始行
				if (rs.getString("TYPE_CNT").equals("B")) gyo = 4;		//受験者開始行
				if (rs.getString("TYPE_CNT").equals("C")) gyo = 7;		//合格者開始行
				for (int i=1; i<4; i++) {
					ret = svf.VrsOutn("EXAM_TYPE1" 	,gyo 	,rs.getString("TYPE2_"+String.valueOf(i)));	//２科目型
					ret = svf.VrsOutn("EXAM_TYPE2" 	,gyo 	,rs.getString("TYPE4_"+String.valueOf(i)));	//４科目型
					ret = svf.VrsOutn("TOTAL" 		,gyo 	,rs.getString("TOTAL" +String.valueOf(i)));	//計
					gyo++;
				}
				nonedata = true;
			}
			//出力
			if (nonedata) ret = svf.VrEndPage();
			rs.close();
			db2.commit();	/* NO001 */
		} catch( Exception ex ) {
			log.error("setSvfout1 set error!");
		}
		return nonedata;
	}



	/**帳票出力（速報データ（得点）をセット）**/
	private boolean setSvfout2(
		DB2UDB db2,
		Vrw32alp svf,
		String param[],
		PreparedStatement ps3,
		String test_div
	) {
		boolean nonedata = false;
		int ret = 0;
        if (false && 0 != ret) { ret = 0; }
		ret = svf.VrSetForm("KNJL325_2.frm", 1);	//セットフォーム
		try {
			setTitle(svf,param);		//見出しメソッド

            int p = 0;
			ps3.setString( ++p, test_div );
			ResultSet rs = ps3.executeQuery();

			String subclass 	= "";		//列フィールド名
			String boyaverage 	= "";	//平均点（男子）
			String grlaverage 	= "";	//平均点（女子）
			while( rs.next() ){
				//明細
				if (rs.getString("TESTSUBCLASSCD").equals("1")) subclass = "SUBCLASS1_";	//国語
				if (rs.getString("TESTSUBCLASSCD").equals("2")) subclass = "SUBCLASS2_";	//算数
				if (rs.getString("TESTSUBCLASSCD").equals("3")) subclass = "SUBCLASS3_";	//社会 05/01/05Modify
				if (rs.getString("TESTSUBCLASSCD").equals("4")) subclass = "SUBCLASS4_";	//理科 05/01/05Modify
				if (rs.getString("TESTSUBCLASSCD").equals("A")) subclass = "4TOTAL";		//４科計
				//明細
				boyaverage 	= ( rs.getString("AVARAGE_MEN") == null ) ? "−" : String.valueOf(df.format(rs.getFloat("AVARAGE_MEN")));
				grlaverage 	= ( rs.getString("AVARAGE_WOMEN") == null ) ? "−" : String.valueOf(df.format(rs.getFloat("AVARAGE_WOMEN")));
				ret = svf.VrsOut(subclass+"1" 	,boyaverage );	//平均点（男子）
				ret = svf.VrsOut(subclass+"2" 	,grlaverage );	//平均点（女子）
				ret = svf.VrsOut(subclass+"3" 	,String.valueOf(df.format(rs.getFloat("AVARAGE_TOTAL"))) );	//平均点（合計）
				ret = svf.VrsOut(subclass+"4" 	,rs.getString("MAX_SCORE"));								//最高点

                if( 6 < subclass.length() )
				    ret = svf.VrsOut(subclass+"5" 	,( param[8] != null )? "−" : rs.getString("MIN_SCORE"));//最低点 05/01/05Modify
                else
				    ret = svf.VrsOut(subclass+"5" 	,rs.getString("MIN_SCORE"));								//最低点

				nonedata = true;
			}
			//出力
			if (nonedata) ret = svf.VrEndPage();
			rs.close();
			db2.commit();	/* NO001 */
		} catch( Exception ex ) {
			log.error("setSvfout2 set error!");
		}
		return nonedata;
	}



	/**見出し項目をセット**/
	private void setTitle(
		Vrw32alp svf,
		String param[]
	) {
		int ret = 0;
        if (false && 0 != ret) { ret = 0; }

		try {
				ret = svf.VrsOut("NENDO"	,param[3]);		//年度
				ret = svf.VrsOut("DATE2"	,param[4]);		//作成日
				ret = svf.VrsOut("TESTDIV"	,param[5]);		//入試区分
				ret = svf.VrsOut("EXAMDATE"	,param[6]);		//入試日付
		} catch( Exception ex ) {
			log.error("setTitle set error!");
		}

	}



	/**入試区分を取得**/
	private String preStat(String param[])
	{
		StringBuffer stb = new StringBuffer();
		try {
			stb.append("SELECT DISTINCT TESTDIV ");
			stb.append("FROM   ENTEXAM_RECEPT_DAT ");
			stb.append("WHERE  ENTEXAMYEAR='"+param[0]+"' AND ");
			stb.append("       APPLICANTDIV='"+param[1]+"' ");
			stb.append("ORDER BY TESTDIV ");
		} catch( Exception e ){
			log.error("preStat error!");
		}
		return stb.toString();

	}//preStat()の括り



	/**速報データ（人数）を取得**/
	private String preStat1(String param[])
	{
		StringBuffer stb = new StringBuffer();
	//	パラメータ（入試区分２つ）
		try {
			//志願者
			stb.append("SELECT  'A' TYPE_CNT, ");
			stb.append("        SUM(CASE WHEN W2.EXAMNO BETWEEN '20000' AND '29999' AND W3.SEX='1' THEN 1 ELSE 0 END) AS TYPE2_1,  ");
			stb.append("        SUM(CASE WHEN W2.EXAMNO BETWEEN '40000' AND '49999' AND W3.SEX='1' THEN 1 ELSE 0 END) AS TYPE4_1,  ");
			stb.append("        SUM(CASE WHEN W3.SEX='1' THEN 1 ELSE 0 END) AS TOTAL1,  ");
			stb.append("        SUM(CASE WHEN W2.EXAMNO BETWEEN '20000' AND '29999' AND W3.SEX='2' THEN 1 ELSE 0 END) AS TYPE2_2,  ");
			stb.append("        SUM(CASE WHEN W2.EXAMNO BETWEEN '40000' AND '49999' AND W3.SEX='2' THEN 1 ELSE 0 END) AS TYPE4_2,  ");
			stb.append("        SUM(CASE WHEN W3.SEX='2' THEN 1 ELSE 0 END) AS TOTAL2,  ");
			stb.append("        SUM(CASE WHEN W2.EXAMNO BETWEEN '20000' AND '29999' THEN 1 ELSE 0 END) AS TYPE2_3,  ");
			stb.append("        SUM(CASE WHEN W2.EXAMNO BETWEEN '40000' AND '49999' THEN 1 ELSE 0 END) AS TYPE4_3,  ");
			stb.append("        COUNT(*) AS TOTAL3  ");
			stb.append("FROM    ENTEXAM_DESIRE_DAT W2  ");
			stb.append("        INNER JOIN ENTEXAM_APPLICANTBASE_DAT W3 ON W3.ENTEXAMYEAR = W2.ENTEXAMYEAR AND  ");
			stb.append("                                                   W3.EXAMNO = W2.EXAMNO  ");
			stb.append("WHERE   W2.ENTEXAMYEAR = '"+param[0]+"' AND  ");
			stb.append("        W2.APPLICANTDIV = '"+param[1]+"' AND  ");   //志願者データ：入試制度
			stb.append("        W2.TESTDIV = ? AND  ");                     //志願者データ：入試区分
			stb.append("        W2.APPLICANT_DIV = '1'  ");                 //志願者データ：志願者区分
			stb.append("GROUP BY W2.TESTDIV  ");
			//受験者
			stb.append("UNION ALL  ");
			stb.append("SELECT  'B' TYPE_CNT, ");
			stb.append("        SUM(CASE WHEN W1.EXAM_TYPE='1' AND W3.SEX='1' THEN 1 ELSE 0 END) AS TYPE2_1,  ");
			stb.append("        SUM(CASE WHEN W1.EXAM_TYPE='2' AND W3.SEX='1' THEN 1 ELSE 0 END) AS TYPE4_1,  ");
			stb.append("        SUM(CASE WHEN W3.SEX='1' THEN 1 ELSE 0 END) AS TOTAL1,  ");
			stb.append("        SUM(CASE WHEN W1.EXAM_TYPE='1' AND W3.SEX='2' THEN 1 ELSE 0 END) AS TYPE2_2,  ");
			stb.append("        SUM(CASE WHEN W1.EXAM_TYPE='2' AND W3.SEX='2' THEN 1 ELSE 0 END) AS TYPE4_2,  ");
			stb.append("        SUM(CASE WHEN W3.SEX='2' THEN 1 ELSE 0 END) AS TOTAL2,  ");
			stb.append("        SUM(CASE WHEN W1.EXAM_TYPE='1' THEN 1 ELSE 0 END) AS TYPE2_3,  ");
			stb.append("        SUM(CASE WHEN W1.EXAM_TYPE='2' THEN 1 ELSE 0 END) AS TYPE4_3,  ");
			stb.append("        SUM(CASE WHEN W1.EXAM_TYPE='1' OR W1.EXAM_TYPE='2' THEN 1 ELSE 0 END) AS TOTAL3  ");
			stb.append("FROM    ENTEXAM_RECEPT_DAT W1  ");
			stb.append("        INNER JOIN ENTEXAM_DESIRE_DAT W2 ON W2.ENTEXAMYEAR = W1.ENTEXAMYEAR AND  ");
			stb.append("                                            W2.APPLICANTDIV = W1.APPLICANTDIV AND  ");
			stb.append("                                            W2.TESTDIV = W1.TESTDIV AND  ");
			stb.append("                                            W2.EXAMNO = W1.EXAMNO  ");
			stb.append("        INNER JOIN ENTEXAM_APPLICANTBASE_DAT W3 ON W3.ENTEXAMYEAR = W1.ENTEXAMYEAR AND  ");
			stb.append("                                                   W3.EXAMNO = W1.EXAMNO  ");
			stb.append("WHERE   W1.ENTEXAMYEAR = '"+param[0]+"' AND  ");
			stb.append("        W1.APPLICANTDIV = '"+param[1]+"' AND  ");   //志願者受付データ：入試制度
			stb.append("        W1.TESTDIV = ? AND  ");                     //志願者受付データ：入試区分
			stb.append("        W2.EXAMINEE_DIV = '1' AND  ");				//志願者データ：受験者区分
			stb.append("        W2.APPLICANT_DIV = '1'  ");					//志願者データ：志願者区分
			stb.append("GROUP BY W1.TESTDIV  ");

			//合格者
			stb.append("UNION ALL  ");
			stb.append("SELECT  'C' TYPE_CNT, ");
			stb.append("        SUM(CASE WHEN W1.EXAM_TYPE='1' AND W3.SEX='1' THEN 1 ELSE 0 END) AS TYPE2_1,  ");
			stb.append("        SUM(CASE WHEN W1.EXAM_TYPE='2' AND W3.SEX='1' THEN 1 ELSE 0 END) AS TYPE4_1,  ");
			// 05/01/08 stb.append("        SUM(CASE WHEN W1.JUDGE_EXAM_TYPE='1' AND W3.SEX='1' THEN 1 ELSE 0 END) AS TYPE2_1,  "); // 05/01/05Modify
			// 05/01/08 stb.append("        SUM(CASE WHEN W1.JUDGE_EXAM_TYPE='2' AND W3.SEX='1' THEN 1 ELSE 0 END) AS TYPE4_1,  "); // 05/01/05Modify
			stb.append("        SUM(CASE WHEN W3.SEX='1' THEN 1 ELSE 0 END) AS TOTAL1,  ");
			stb.append("        SUM(CASE WHEN W1.EXAM_TYPE='1' AND W3.SEX='2' THEN 1 ELSE 0 END) AS TYPE2_2,  ");
			stb.append("        SUM(CASE WHEN W1.EXAM_TYPE='2' AND W3.SEX='2' THEN 1 ELSE 0 END) AS TYPE4_2,  ");
			// 05/01/08 stb.append("        SUM(CASE WHEN W1.JUDGE_EXAM_TYPE='1' AND W3.SEX='2' THEN 1 ELSE 0 END) AS TYPE2_2,  "); // 05/01/05Modify
			// 05/01/08 stb.append("        SUM(CASE WHEN W1.JUDGE_EXAM_TYPE='2' AND W3.SEX='2' THEN 1 ELSE 0 END) AS TYPE4_2,  "); // 05/01/05Modify
			stb.append("        SUM(CASE WHEN W3.SEX='2' THEN 1 ELSE 0 END) AS TOTAL2,  ");
			stb.append("        SUM(CASE WHEN W1.EXAM_TYPE='1' THEN 1 ELSE 0 END) AS TYPE2_3,  ");
			stb.append("        SUM(CASE WHEN W1.EXAM_TYPE='2' THEN 1 ELSE 0 END) AS TYPE4_3,  ");
			stb.append("        SUM(CASE WHEN W1.EXAM_TYPE='1' OR W1.EXAM_TYPE='2' THEN 1 ELSE 0 END) AS TOTAL3  ");
			// 05/01/08 stb.append("        SUM(CASE WHEN W1.JUDGE_EXAM_TYPE='1' THEN 1 ELSE 0 END) AS TYPE2_3,  ");                // 05/01/05Modify
			// 05/01/08 stb.append("        SUM(CASE WHEN W1.JUDGE_EXAM_TYPE='2' THEN 1 ELSE 0 END) AS TYPE4_3,  ");                // 05/01/05Modify
			// 05/01/08 stb.append("        SUM(CASE WHEN W1.JUDGE_EXAM_TYPE='1' OR W1.JUDGE_EXAM_TYPE='2' THEN 1 ELSE 0 END) AS TOTAL3  "); // 05/01/05Modify
			stb.append("FROM    ENTEXAM_RECEPT_DAT W1  ");
			stb.append("        INNER JOIN ENTEXAM_APPLICANTBASE_DAT W3 ON W3.ENTEXAMYEAR = W1.ENTEXAMYEAR AND  ");
			stb.append("                                                   W3.EXAMNO = W1.EXAMNO  ");
			stb.append("WHERE   W1.ENTEXAMYEAR = '"+param[0]+"' AND  ");
			stb.append("        W1.APPLICANTDIV = '"+param[1]+"' AND  ");   //志願者受付データ：入試制度
            stb.append("        W1.TESTDIV = ? AND  ");                     //志願者受付データ：入試区分
			stb.append("        W1.JUDGEDIV = '1' AND W1.JUDGECLASS not in ('4','6') ");						//志願者受付データ：合否区分

            //前回までの合格者を対象に含めない場合 05/01/05
            if( param[7] == null ){
                stb.append(    "AND W1.EXAMNO NOT IN(");
                stb.append(             "SELECT  DISTINCT EXAMNO ");
                stb.append(             "FROM    ENTEXAM_RECEPT_DAT W1  ");
                stb.append(             "WHERE   W1.ENTEXAMYEAR = '"+param[0]+"' AND  ");
                stb.append(                     "W1.APPLICANTDIV = '"+param[1]+"' AND  ");   //志願者受付データ：入試制度
                stb.append(                     "W1.TESTDIV < ? AND  ");                     //志願者受付データ：入試区分
                stb.append(                     "W1.JUDGEDIV = '1' AND W1.JUDGECLASS not in ('4','6') ");				         //志願者受付データ：合否区分
                stb.append(             ")");
            }

            stb.append("GROUP BY W1.TESTDIV  ");
			stb.append("ORDER BY TYPE_CNT ");
//log.debug(stb);
		} catch( Exception e ){
			log.error("preStat1 error!");
		}
		return stb.toString();

	}//preStat1()の括り



	/**名称を取得**/
	private String preStat2(String param[])
	{
		StringBuffer stb = new StringBuffer();
	//	パラメータ（入試区分）
		try {
            stb.append("SELECT TESTDAY AS TEST_DATE, NAME AS TEST_NAME ");
            stb.append("FROM   ENTEXAM_TESTDIV_MST T4 ");
            stb.append("WHERE  ENTEXAMYEAR='"+param[0]+"' AND TESTDIV=? ");
		} catch( Exception e ){
			log.error("preStat2 error!");
		}
		return stb.toString();

	}//preStat2()の括り



	/**名称を取得**/
	private String preStat3(String param[])
	{
		StringBuffer stb = new StringBuffer();
	//	パラメータ（入試区分）
		try {
			//志願者受付・基礎データ
			stb.append("WITH RECEPT AS ( ");
			stb.append("    SELECT  W3.SEX, W1.*  ");
			stb.append("    FROM    ENTEXAM_RECEPT_DAT W1,ENTEXAM_APPLICANTBASE_DAT W3  ");
			stb.append("    WHERE   W1.ENTEXAMYEAR = '"+param[0]+"' AND  ");
			stb.append("            W1.APPLICANTDIV = '"+param[1]+"' AND  ");
			stb.append("            W1.TESTDIV = ? AND  ");
			stb.append("            W3.ENTEXAMYEAR = W1.ENTEXAMYEAR AND  ");
			stb.append("            W3.EXAMNO = W1.EXAMNO AND  ");
            //前回までの合格者を対象に含めない場合 05/01/05
            if( param[7] == null ){
                stb.append(        "W1.EXAMNO NOT IN(");
                //stb.append(             "SELECT  DISTINCT EXAMNO ");//Modify 05/01/10
                stb.append(             "SELECT  EXAMNO ");
                stb.append(             "FROM    ENTEXAM_RECEPT_DAT W1  ");
                stb.append(             "WHERE   W1.ENTEXAMYEAR = '"+param[0]+"' AND  ");
                stb.append(                     "W1.APPLICANTDIV = '"+param[1]+"' AND  ");   //志願者受付データ：入試制度
                stb.append(                     "W1.TESTDIV < ? AND  ");                     //志願者受付データ：入試区分
                stb.append(                     "W1.JUDGEDIV = '1' AND W1.JUDGECLASS not in ('4','6') ");				         //志願者受付データ：合否区分
                stb.append(             "GROUP BY EXAMNO ");//Add 05/01/10
                stb.append(             ") AND ");
            }
			stb.append("            W1.JUDGEDIV = '1' AND W1.JUDGECLASS not in ('4','6') ),  ");

			//科目別	平均点（合計）最高点・最低点
			stb.append("SUB_AVG AS ( ");
			stb.append("    SELECT  TESTSUBCLASSCD AS SUBCLASSCD,  ");
			stb.append("            MAX(SCORE) AS MAXSCORE,  ");
			stb.append("            MIN(SCORE) AS MINSCORE,  ");
			stb.append("            ROUND(AVG(FLOAT(SCORE))*10,0)/10 AS AVERAGE  ");
			stb.append("    FROM    RECEPT W1  ");
			stb.append("            INNER JOIN ENTEXAM_SCORE_DAT W4 ON W4.ENTEXAMYEAR = W1.ENTEXAMYEAR AND  ");
			stb.append("                                               W4.APPLICANTDIV = W1.APPLICANTDIV AND  ");
			stb.append("                                               W4.EXAM_TYPE = W1.EXAM_TYPE AND  ");
			stb.append("                                               W4.TESTDIV = W1.TESTDIV AND  ");
			stb.append("                                               W4.RECEPTNO = W1.RECEPTNO  ");
			stb.append("    GROUP BY W1.TESTDIV,TESTSUBCLASSCD ),  ");
					//平均点（男子）
			stb.append("SUB_B_AVG AS ( ");
			stb.append("    SELECT  TESTSUBCLASSCD AS SUBCLASSCD,  ");
			stb.append("            ROUND(AVG(FLOAT(SCORE))*10,0)/10 AS AVERAGE  ");
			stb.append("    FROM    RECEPT W1  ");
			stb.append("            INNER JOIN ENTEXAM_SCORE_DAT W4 ON W4.ENTEXAMYEAR = W1.ENTEXAMYEAR AND  ");
			stb.append("                                               W4.APPLICANTDIV = W1.APPLICANTDIV AND  ");
			stb.append("                                               W4.EXAM_TYPE = W1.EXAM_TYPE AND  ");
			stb.append("                                               W4.TESTDIV = W1.TESTDIV AND  ");
			stb.append("                                               W4.RECEPTNO = W1.RECEPTNO  ");
			stb.append("    WHERE   W1.SEX = '1'  ");
			stb.append("    GROUP BY W1.TESTDIV,TESTSUBCLASSCD ),  ");
					//平均点（女子）
			stb.append("SUB_G_AVG AS ( ");
			stb.append("    SELECT  TESTSUBCLASSCD AS SUBCLASSCD,  ");
			stb.append("            ROUND(AVG(FLOAT(SCORE))*10,0)/10 AS AVERAGE  ");
			stb.append("    FROM    RECEPT W1  ");
			stb.append("            INNER JOIN ENTEXAM_SCORE_DAT W4 ON W4.ENTEXAMYEAR = W1.ENTEXAMYEAR AND  ");
			stb.append("                                               W4.APPLICANTDIV = W1.APPLICANTDIV AND  ");
			stb.append("                                               W4.EXAM_TYPE = W1.EXAM_TYPE AND  ");
			stb.append("                                               W4.TESTDIV = W1.TESTDIV AND  ");
			stb.append("                                               W4.RECEPTNO = W1.RECEPTNO  ");
			stb.append("    WHERE   W1.SEX = '2'  ");
			stb.append("    GROUP BY W1.TESTDIV,TESTSUBCLASSCD ),  ");

			//２科計	平均点（合計）最高点
			stb.append("TYPE2_AVG AS ( ");
			stb.append("    SELECT  'A' AS SUBCLASSCD,  ");
			stb.append("            MAX(TOTAL2) AS MAXSCORE,  ");
			//stb.append("            MIN(TOTAL2) AS MINSCORE,  ");---NO002
			stb.append("            ROUND(AVG(FLOAT(TOTAL2))*10,0)/10 AS AVERAGE  ");
			stb.append("    FROM    RECEPT W1  ");
            stb.append("    WHERE   VALUE(W1.JUDGE_EXAM_TYPE,'0') = '1' ");    // 05/01/05
			stb.append("    GROUP BY W1.TESTDIV ),  ");
					//最低点---NO002
			stb.append("TYPE2_MIN AS ( ");
			stb.append("    SELECT  'A' AS SUBCLASSCD,  ");
			stb.append("            MIN(TOTAL2) AS MINSCORE  ");
			stb.append("    FROM    RECEPT W1  ");
			stb.append("            INNER JOIN ENTEXAM_PASSINGMARK_MST W4 ON W4.ENTEXAMYEAR = W1.ENTEXAMYEAR AND  ");
			stb.append("                                               W4.APPLICANTDIV = W1.APPLICANTDIV AND  ");
			stb.append("                                               W4.TESTDIV = W1.TESTDIV AND  ");
			stb.append("                                               W4.EXAM_TYPE = W1.JUDGE_EXAM_TYPE AND  ");//NO003---W1.EXAM_TYPE→W1.JUDGE_EXAM_TYPE
			stb.append("                                               W4.BORDER_SCORE <= W1.TOTAL2  ");
            stb.append("    WHERE   VALUE(W1.JUDGE_EXAM_TYPE,'0') = '1' ");
			stb.append("    GROUP BY W1.TESTDIV ),  ");
					//平均点（男子）
			stb.append("TYPE2_B_AVG AS ( ");
			stb.append("    SELECT  'A' AS SUBCLASSCD,  ");
			stb.append("            ROUND(AVG(FLOAT(TOTAL2))*10,0)/10 AS AVERAGE  ");
			stb.append("    FROM    RECEPT W1  ");
			stb.append("    WHERE   W1.SEX = '1' AND ");
            stb.append("            VALUE(W1.JUDGE_EXAM_TYPE,'0') = '1' ");    // 05/01/05
			stb.append("    GROUP BY W1.TESTDIV ),  ");
					//平均点（女子）
			stb.append("TYPE2_G_AVG AS ( ");
			stb.append("    SELECT  'A' AS SUBCLASSCD,  ");
			stb.append("            ROUND(AVG(FLOAT(TOTAL2))*10,0)/10 AS AVERAGE  ");
			stb.append("    FROM    RECEPT W1  ");
			stb.append("    WHERE   W1.SEX = '2' AND ");
            stb.append("            VALUE(W1.JUDGE_EXAM_TYPE,'0') = '1' ");    // 05/01/05
			stb.append("    GROUP BY W1.TESTDIV ),  ");

			//４科計	平均点（合計）最高点・最低点
			stb.append("TYPE4_AVG AS ( ");
			stb.append("    SELECT  'B' AS SUBCLASSCD,  ");
			//stb.append("            MAX(TOTAL4) AS MAXSCORE,  ");    // 05/01/07
			stb.append("            MIN(TOTAL4) AS MINSCORE,  ");
			stb.append("            ROUND(AVG(FLOAT(TOTAL4))*10,0)/10 AS AVERAGE  ");
			stb.append("    FROM    RECEPT W1  ");
			//stb.append("    WHERE   W1.EXAM_TYPE = '2' AND ");
            stb.append("    WHERE   VALUE(W1.JUDGE_EXAM_TYPE,'0') = '2' ");    // 05/01/05
			stb.append("    GROUP BY W1.TESTDIV ),  ");
					//最高点 05/01/07Add
			stb.append("TYPE4_MAX AS ( ");
			stb.append("    SELECT  'B' AS SUBCLASSCD,  ");
			stb.append("            MAX(TOTAL4) AS MAXSCORE  ");
			stb.append("    FROM    RECEPT W1  ");
			stb.append("    WHERE   W1.EXAM_TYPE = '2' ");
			stb.append("    GROUP BY W1.TESTDIV ),  ");
					//最低点---NO002
			stb.append("TYPE4_MIN AS ( ");
			stb.append("    SELECT  'B' AS SUBCLASSCD,  ");
			stb.append("            MIN(TOTAL4) AS MINSCORE  ");
			stb.append("    FROM    RECEPT W1  ");
			stb.append("            INNER JOIN ENTEXAM_PASSINGMARK_MST W4 ON W4.ENTEXAMYEAR = W1.ENTEXAMYEAR AND  ");
			stb.append("                                               W4.APPLICANTDIV = W1.APPLICANTDIV AND  ");
			stb.append("                                               W4.TESTDIV = W1.TESTDIV AND  ");
			stb.append("                                               W4.EXAM_TYPE = W1.JUDGE_EXAM_TYPE AND  ");//NO003---W1.EXAM_TYPE→W1.JUDGE_EXAM_TYPE
			stb.append("                                               W4.BORDER_SCORE <= W1.TOTAL4  ");
			stb.append("    WHERE   VALUE(W1.JUDGE_EXAM_TYPE,'0') = '2' ");
			stb.append("    GROUP BY W1.TESTDIV ),  ");
					//平均点（男子）
			stb.append("TYPE4_B_AVG AS ( ");
			stb.append("    SELECT  'B' AS SUBCLASSCD,  ");
			stb.append("            ROUND(AVG(FLOAT(TOTAL4))*10,0)/10 AS AVERAGE  ");
			stb.append("    FROM    RECEPT W1  ");
			//stb.append("    WHERE   W1.EXAM_TYPE = '2' AND W1.SEX = '1'  ");
			stb.append("    WHERE   W1.SEX = '1' AND ");
            stb.append("            VALUE(W1.JUDGE_EXAM_TYPE,'0') = '2' ");    // 05/01/05
			stb.append("    GROUP BY W1.TESTDIV ),  ");
					//平均点（女子）
			stb.append("TYPE4_G_AVG AS ( ");
			stb.append("    SELECT  'B' AS SUBCLASSCD,  ");
			stb.append("            ROUND(AVG(FLOAT(TOTAL4))*10,0)/10 AS AVERAGE  ");
			stb.append("    FROM    RECEPT W1  ");
			//stb.append("    WHERE   W1.EXAM_TYPE = '2' AND W1.SEX = '2'  ");
			stb.append("    WHERE   W1.SEX = '2' AND ");
            stb.append("            VALUE(W1.JUDGE_EXAM_TYPE,'0') = '2' ");    // 05/01/05
			stb.append("    GROUP BY W1.TESTDIV )  ");

			//メイン
			//科目別
			stb.append("SELECT  T1.SUBCLASSCD, T2.AVERAGE AS BOYAVERAGE, T3.AVERAGE AS GRLAVERAGE,  ");
			stb.append("        T1.AVERAGE, T1.MAXSCORE, T1.MINSCORE  ");
			stb.append("FROM    SUB_AVG T1  ");
			stb.append("        LEFT JOIN SUB_B_AVG T2 ON T1.SUBCLASSCD=T2.SUBCLASSCD ");
			stb.append("        LEFT JOIN SUB_G_AVG T3 ON T1.SUBCLASSCD=T3.SUBCLASSCD ");

			//２科計
			stb.append("UNION ALL  ");
			stb.append("SELECT  T1.SUBCLASSCD, T2.AVERAGE AS BOYAVERAGE, T3.AVERAGE AS GRLAVERAGE,  ");
			stb.append("        T1.AVERAGE, T1.MAXSCORE, T5.MINSCORE  ");//---NO002 T1.MINSCORE→T5.MINSCORE
			stb.append("FROM    TYPE2_AVG T1  ");
			stb.append("        LEFT JOIN TYPE2_B_AVG T2 ON T1.SUBCLASSCD=T2.SUBCLASSCD ");
			stb.append("        LEFT JOIN TYPE2_G_AVG T3 ON T1.SUBCLASSCD=T3.SUBCLASSCD ");
			stb.append("        LEFT JOIN TYPE2_MIN T5 ON T1.SUBCLASSCD=T5.SUBCLASSCD ");//---NO002

			//４科計
			stb.append("UNION ALL  ");
			stb.append("SELECT  T1.SUBCLASSCD, T2.AVERAGE AS BOYAVERAGE, T3.AVERAGE AS GRLAVERAGE,  ");
			stb.append("        T1.AVERAGE, T4.MAXSCORE, T5.MINSCORE  ");// 05/01/07 T1.MAXSCORE→T4.MAXSCORE---NO002 T1.MINSCORE→T5.MINSCORE
			stb.append("FROM    TYPE4_AVG T1  ");
			stb.append("        LEFT JOIN TYPE4_B_AVG T2 ON T1.SUBCLASSCD=T2.SUBCLASSCD ");
			stb.append("        LEFT JOIN TYPE4_G_AVG T3 ON T1.SUBCLASSCD=T3.SUBCLASSCD ");
			stb.append("        LEFT JOIN TYPE4_MAX T4 ON T1.SUBCLASSCD=T4.SUBCLASSCD ");// 05/01/07
			stb.append("        LEFT JOIN TYPE4_MIN T5 ON T1.SUBCLASSCD=T5.SUBCLASSCD ");//---NO002
			stb.append("ORDER BY SUBCLASSCD ");
		} catch( Exception e ){
			log.error("preStat3 error!");
		}
		return stb.toString();

	}//preStat3()の括り


    /**速報データ（得点）KNJL110Oで作成したデータ**/
    private String getJudgeAvarage(String param[]) {
        StringBuffer stb = new StringBuffer();

        try {
            stb.append(" SELECT ");
            stb.append("     TESTSUBCLASSCD, ");
            stb.append("     AVARAGE_MEN, ");
            stb.append("     AVARAGE_WOMEN, ");
            stb.append("     AVARAGE_TOTAL, ");
            stb.append("     MAX_SCORE, ");
            stb.append("     MIN_SCORE ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_JUDGE_AVARAGE_DAT ");
            stb.append(" WHERE ");
            stb.append("     ENTEXAMYEAR = '"+param[0]+"' ");
            stb.append("     AND APPLICANTDIV = '"+param[1]+"' ");
            stb.append("     AND TESTDIV = ? ");
            stb.append("     AND EXAM_TYPE = '2' ");
            stb.append("     AND (TESTSUBCLASSCD <= '4' OR TESTSUBCLASSCD = 'A') ");
            stb.append(" ORDER BY ");
            stb.append("     TESTSUBCLASSCD ");
        } catch( Exception e ){
            log.error("preStat3 error!");
        }

        return stb.toString();
    }


	/**PrepareStatement close**/
	private void preStatClose(
		PreparedStatement ps,
		PreparedStatement ps1,
		PreparedStatement ps2,
		PreparedStatement ps3
	) {
		try {
			ps.close();
			ps1.close();
			ps2.close();
			ps3.close();
		} catch( Exception e ){
			log.error("preStatClose error!");
		}
	}//preStatClose()の括り



}//クラスの括り
