/**
 *
 *	学校教育システム 賢者 [成績管理]
 *
 *					＜ＫＮＪＤ３２４＞  得点分布表（中学）（学年別・試験別・コース別）
 *
 *	2005/03/28 nakamoto 作成日
 *	2005/05/09 nakamoto 学年成績は、学期名称の表記はいらない。---NO001
 *	           nakamoto 今学期のパラメータを追加。---NO002
 *	           nakamoto 絶対評価と相対評価を追加。---NO003(未対応：後日対応予定)
 *	2005/05/12 nakamoto 素点(中間・期末)以外を出力する場合、公欠・欠席は見ない。---NO004
 *	2005/05/19 nakamoto 作成日を追加。---NO006
 *	           nakamoto 退学・転学について、異動日が3月31日だったら、3月31日は在籍とする。---NO007
 *	2005/06/16 nakamoto 科目コードでの条件を教科コードに変更---NO008
 ***********************************************************************************************
 *  2005/10/26 nakamoto 編入のデータ仕様変更および在籍異動条件に転入学を追加---NO025
 *	2005/12/13 nakamoto NO026:(各教科・５科目・９科目)は、テーブル(RECORD_CLASS_DAT)から参照するように修正
 **/

package servletpack.KNJD;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
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
import servletpack.KNJZ.detail.KNJ_Get_Info;


public class KNJD324K {


    private static final Log log = LogFactory.getLog(KNJD324K.class);

	/*----------------------------*
	 * HTTP Get リクエストの処理  *
	 *----------------------------*/
	public void svf_out(HttpServletRequest request, HttpServletResponse response)
	                 throws ServletException, IOException
	{

		Vrw32alp svf = new Vrw32alp(); 			//PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
		DB2UDB db2 = null;						//Databaseクラスを継承したクラス
		String param[] = new String[11];//NO026

	//	パラメータの取得
		try {
	        param[0] = request.getParameter("YEAR");         				//年度
			param[1] = request.getParameter("GAKKI");   					//学期 1,2,3,9:学年平均
			param[2] = request.getParameter("TESTKINDCD");   				//01:中間,02:期末,0:学期平均,9:学年平均
			param[5] = request.getParameter("GRADE");   					//学年
			String classcd[] = request.getParameterValues("COURSE_CD");   	//コース
			param[6] = "(";
			for( int ia=0 ; ia<classcd.length ; ia++ ){
				if(ia > 0) param[6] = param[6] + ",";
				param[6] = param[6] + "'" + classcd[ia] + "'";
			}
			param[6] = param[6] + ")";
			String idobi = request.getParameter("DATE");   					//異動対象日付
			param[7] = idobi.replace('/','-');
        	//学年末の場合、今学期をセット---NO002
			if (param[1].equals("9")) param[1] = request.getParameter("CTRL_SEME");
			param[10] = request.getParameter("useCurriculumcd");
		} catch( Exception ex ) {
			log.warn("parameter error!");
		}

	//	print設定
		PrintWriter out = new PrintWriter (response.getOutputStream());
		response.setContentType("application/pdf");
		OutputStream outstrm = response.getOutputStream();

	//	svf設定
		int ret = svf.VrInit();						   	//クラスの初期化
		ret = svf.VrSetSpoolFileStream(outstrm);   		//PDFファイル名の設定

	//	ＤＢ接続
		try {
			db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
			db2.open();
		} catch( Exception ex ) {
			log.error("DB2 open error!");
			return;
		}


	//	ＳＶＦ作成処理
		PreparedStatement ps  = null;
		PreparedStatement ps1 = null;
		PreparedStatement ps2 = null;
		PreparedStatement ps3 = null;
		boolean nonedata = false; 								//該当データなしフラグ
		printHeader(db2,svf,param);								//見出し出力のメソッド
		setParamAbsence(param);									//公欠・欠席パラメータのメソッド
		//SQL作成
		try {
			ps  = db2.prepareStatement(statementCourse(param));	//コース名取得preparestatement
			ps1 = db2.prepareStatement(statementScore(param));//科目別得点分布preparestatement
			ps2 = db2.prepareStatement(statementScore5(param));//５科目合計得点分布preparestatement
			ps3 = db2.prepareStatement(statementScore9(param));//９科目合計得点分布preparestatement
		} catch( Exception ex ) {
			log.warn("DB2 open error!");
		}
		//SVF出力
		printHeaderCourse(db2,svf,param,ps);					//見出し出力のメソッド
for(int ia=0 ; ia<param.length ; ia++) log.debug("param["+ia+"]="+param[ia]);
		if( printMain(db2,svf,param,ps1) ) {					//帳票出力のメソッド
			printScore5(db2,svf,param,ps2);
    		if( !param[2].equals("01") ) printScore9(db2,svf,param,ps3);
			ret = svf.VrEndPage();
			nonedata = true;
		}
log.debug("nonedata="+nonedata);
	//	該当データ無し
		if( !nonedata ){
			ret = svf.VrSetForm("MES001.frm", 0);
			ret = svf.VrsOut("note" , "");
			ret = svf.VrEndPage();
		}

	// 	終了処理
		ret = svf.VrQuit();
		statementClose(ps,ps1,ps2,ps3);		//preparestatementを閉じる
		db2.commit();
		db2.close();				//DBを閉じる
		outstrm.close();			//ストリームを閉じる 

    }//doGetの括り



	/** SVF-FORM **/
	private void printHeader(DB2UDB db2,Vrw32alp svf,String param[]){

		KNJ_Get_Info getinfo = new KNJ_Get_Info();
		KNJ_Get_Info.ReturnVal returnval = null;
		int ret = 0;
		ret = svf.VrSetForm("KNJD324.frm", 1);
		ret = svf.VrsOut("NENDO",nao_package.KenjaProperties.gengou(Integer.parseInt(param[0])) + "年度");//年度

	//	学年末の場合、今学期をセット---NO002
//		try {
//			returnval = getinfo.Control(db2);
//			if (param[1].equals("9")) param[1] = returnval.val2;
//		} catch( Exception e ){
//			log.warn("ctrl_date get error!");
//		}
	//	作成日---NO006
		try {
			returnval = getinfo.Control(db2);
			ret = svf.VrsOut("DATE",KNJ_EditDate.h_format_JP(returnval.val3));
		} catch( Exception e ){
			log.warn("ctrl_date get error!");
		}
	//	学期名称の取得
		try {
			returnval = getinfo.Semester(db2,param[0],param[1]);
			param[4] = returnval.val1;
		} catch( Exception e ){
			log.warn("Semester name get error!");
		}
	//	定期試験名の取得
		String stb = "";
		if (param[2].equals("01")) stb = "中間試験";
		if (param[2].equals("02")) stb = "期末試験";
		if (param[2].equals("02") && param[1].equals("3")) stb = "期末試験";
		if (param[2].equals("0"))  stb = "学期成績";
		if (param[2].equals("9"))  stb = "学年成績";//---NO001
        //---NO003
		if (param[2].equals("90")) stb = "絶対評価(学年評定)";
		if (param[2].equals("91")) stb = "相対評価(５段階)";
		if (param[2].equals("92")) stb = "相対評価(１０段階)";
        //---NO001
		if (param[2].equals("01") || param[2].equals("02") || param[2].equals("0")) 
    		param[4] = param[4] + stb;
		else 
    		param[4] = stb;
	//	定期試験フィールド名の取得---NO026
		if (param[2].equals("01")) param[9] = "SEM"+param[1]+"_INTER_REC";
		if (param[2].equals("02")) param[9] = "SEM"+param[1]+"_TERM_REC";
		if (param[2].equals("0"))  param[9] = "SEM"+param[1]+"_REC";
		if (param[2].equals("9"))  param[9] = "GRADE_RECORD";
		if (param[2].equals("90")) param[9] = "GRADE_ASSESS";
		if (param[2].equals("91")) param[9] = "GRADE3_RELAASSESS_5STEP";
		if (param[2].equals("92")) param[9] = "GRADE3_RELAASSESS_10STEP";

		getinfo = null;
		returnval = null;

	}//printHeader()の括り


	/** 公欠・欠席 */
	private void setParamAbsence(String param[]){

	//	各学期の中間・期末成績の取得
		if (param[2].equals("01")) param[3] = "SEM"+param[1]+"_INTER_REC";
		if (param[2].equals("02")) param[3] = "SEM"+param[1]+"_TERM_REC";
        //---NO003
		if (param[2].equals("9"))  param[3] = "WHEN GRADE_RECORD IS NOT NULL THEN RTRIM(CHAR(GRADE_RECORD))";
		if (param[2].equals("90")) param[3] = "WHEN GRADE_ASSESS IS NOT NULL THEN GRADE_ASSESS";
		if (param[2].equals("91")) param[3] = "WHEN GRADE3_RELAASSESS_5STEP IS NOT NULL THEN GRADE3_RELAASSESS_5STEP";
		if (param[2].equals("92")) param[3] = "WHEN GRADE3_RELAASSESS_10STEP IS NOT NULL THEN GRADE3_RELAASSESS_10STEP";
	//	公欠・欠席の取得
		//中間試験・期末試験
		if (param[2].equals("01") || param[2].equals("02")) {
			param[8] = "CASE WHEN "+param[3]+" IS NULL AND "+param[3]+"_DI IN('KK','KS') THEN 'KK' "
					 + "     WHEN "+param[3]+" IS NOT NULL THEN RTRIM(CHAR("+param[3]+")) "
					 + "     ELSE NULL END";
		//学期成績
		} else if (param[2].equals("0")) {
			//３学期
			if (2 < Integer.parseInt(param[1])) {
//---NO004
//				param[8] = "CASE WHEN (SEM3_TERM_REC IS NULL AND SEM3_TERM_REC_DI  IN('KK','KS')) AND "
//						 + "           VALUE(SEM3_REC_FLG,'0') = '0' THEN 'KK' "
//						 + "     WHEN SEM3_REC IS NOT NULL THEN RTRIM(CHAR(SEM3_REC)) "
//						 + "     ELSE NULL END";
				param[8] = "CASE WHEN SEM3_REC IS NOT NULL THEN RTRIM(CHAR(SEM3_REC)) "
						 + "     ELSE NULL END";
			//２学期
			} else if (1 < Integer.parseInt(param[1])) {
//---NO004
//				param[8] = "CASE WHEN ((SEM2_INTER_REC IS NULL AND SEM2_INTER_REC_DI IN('KK','KS')) OR "
//						 + "           (SEM2_TERM_REC  IS NULL AND SEM2_TERM_REC_DI  IN('KK','KS'))) AND "
//						 + "           VALUE(SEM2_REC_FLG,'0') = '0' THEN 'KK' "
//						 + "     WHEN SEM2_REC IS NOT NULL THEN RTRIM(CHAR(SEM2_REC)) "
//						 + "     ELSE NULL END";
				param[8] = "CASE WHEN SEM2_REC IS NOT NULL THEN RTRIM(CHAR(SEM2_REC)) "
						 + "     ELSE NULL END";
			//１学期
			} else {
//---NO004
//				param[8] = "CASE WHEN ((SEM1_INTER_REC IS NULL AND SEM1_INTER_REC_DI IN('KK','KS')) OR "
//						 + "           (SEM1_TERM_REC  IS NULL AND SEM1_TERM_REC_DI  IN('KK','KS'))) AND "
//						 + "           VALUE(SEM1_REC_FLG,'0') = '0' THEN 'KK' "
//						 + "     WHEN SEM1_REC IS NOT NULL THEN RTRIM(CHAR(SEM1_REC)) "
//						 + "     ELSE NULL END";
				param[8] = "CASE WHEN SEM1_REC IS NOT NULL THEN RTRIM(CHAR(SEM1_REC)) "
						 + "     ELSE NULL END";
			}
		//学年成績
		} else {
//---NO004
//			param[8] = "CASE WHEN ((SEM1_INTER_REC IS NULL AND SEM1_INTER_REC_DI IN('KK','KS')) OR "
//					 + "           (SEM1_TERM_REC  IS NULL AND SEM1_TERM_REC_DI  IN('KK','KS'))) AND "
//					 + "           VALUE(SEM1_REC_FLG,'0') = '0' THEN 'KK' "
//					 + "     WHEN ((SEM2_INTER_REC IS NULL AND SEM2_INTER_REC_DI IN('KK','KS')) OR "
//					 + "           (SEM2_TERM_REC  IS NULL AND SEM2_TERM_REC_DI  IN('KK','KS'))) AND "
//					 + "           VALUE(SEM2_REC_FLG,'0') = '0' THEN 'KK' "
//					 + "     WHEN (SEM3_TERM_REC IS NULL AND SEM3_TERM_REC_DI  IN('KK','KS')) AND "
//					 + "           VALUE(SEM3_REC_FLG,'0') = '0' THEN 'KK' "
//					 + "     "+param[3]+" "//---NO003
//					 + "     ELSE NULL END";
			param[8] = "CASE "+param[3]+" "//---NO003
					 + "     ELSE NULL END";
		}

	}//setParamAbsence()の括り


	/** コース名出力 */
	private void printHeaderCourse(DB2UDB db2,Vrw32alp svf,String param[],PreparedStatement ps){

		int ret = 0;
		try {
			int pp = 0;
			ResultSet rs = ps.executeQuery();

			String coursename = "";
			int reccnt = 0;
			while( rs.next() ){
				if (reccnt > 0) coursename = coursename + "・";
				coursename = coursename + rs.getString("COURSECODENAME");
				reccnt++;
			}
			ret = svf.VrsOut("COURSE" , "第"+String.valueOf(Integer.parseInt(param[5]))+"学年"+coursename+"　"+param[4] );
			rs.close();
			db2.commit();
		} catch( Exception ex ) {
			log.warn("printHeaderCourse read error!");
		}

	}//printHeaderCourse()の括り



	/**科目別得点分布**/
	private boolean printMain(DB2UDB db2,Vrw32alp svf,String param[],PreparedStatement ps1)
	{
		boolean nonedata = false;
		int ret = 0;
		try {
			int pp = 0;
			ResultSet rs = ps1.executeQuery();

			String retsu = "0";
			//---01:国語,02:社会,03:数学,04:理科,06:音楽,07:美術,08:保体,09:技家,05:英語
			while( rs.next() ){
				//各教科---NO008
				if( rs.getString("SUBCLASSCD").equals("01") ) retsu = "1";		//国語
				if( rs.getString("SUBCLASSCD").equals("02") ) retsu = "2";		//社会
				if( rs.getString("SUBCLASSCD").equals("03") ) retsu = "3";		//数学
				if( rs.getString("SUBCLASSCD").equals("04") ) retsu = "4";		//理科
				if( rs.getString("SUBCLASSCD").equals("06") ) retsu = "5";		//音楽
				if( rs.getString("SUBCLASSCD").equals("07") ) retsu = "6";		//美術
				if( rs.getString("SUBCLASSCD").equals("08") ) retsu = "7";		//保体
				if( rs.getString("SUBCLASSCD").equals("09") ) retsu = "8";		//技家
				if( rs.getString("SUBCLASSCD").equals("05") ) retsu = "9";		//英語
				//得点分布
				for (int i = 1; i < 12; i++) 
					ret = svf.VrsOutn("SUBCLASS"+retsu	, i	, rs.getString("SCORE"+String.valueOf(i)) );

				ret = svf.VrsOutn("SUBCLASS"+retsu	, 12	, rs.getString("JUKEN") );		//受験者数
				ret = svf.VrsOutn("SUBCLASS"+retsu	, 13	, rs.getString("AVG_SCORE") );	//平均点
				ret = svf.VrsOutn("SUBCLASS"+retsu	, 14	, rs.getString("MAX_SCORE") );	//最高点
				ret = svf.VrsOutn("SUBCLASS"+retsu	, 15	, rs.getString("MIN_SCORE") );	//最低点

				nonedata = true;
			}
			rs.close();
			db2.commit();
		} catch( Exception ex ) {
			log.warn("printMain read error!");
		}
		return nonedata;

	}//printMain()の括り


	/**５科目合計得点分布**/
	private void printScore5(DB2UDB db2,Vrw32alp svf,String param[],PreparedStatement ps2)
	{
		int ret = 0;
		try {
			int pp = 0;
			ResultSet rs = ps2.executeQuery();

			while( rs.next() ){
				//得点分布
				for (int i = 1; i < 12; i++) 
					ret = svf.VrsOutn("NUMBER5_1"	, i	, rs.getString("SCORE"+String.valueOf(i)) );
				for (int i = 12; i < 16; i++) 
					ret = svf.VrsOutn("NUMBER5_2"	, i-11	, rs.getString("SCORE"+String.valueOf(i)) );

				ret = svf.VrsOut("EXAMINEES5"	, rs.getString("JUKEN") );		//受験者数
				ret = svf.VrsOut("AVERAGE5"		, rs.getString("AVG_SCORE") );	//平均点
				ret = svf.VrsOut("MAXMUM5"		, rs.getString("MAX_SCORE") );	//最高点
				ret = svf.VrsOut("MINIMUM5"		, rs.getString("MIN_SCORE") );	//最低点
			}
			rs.close();
			db2.commit();
		} catch( Exception ex ) {
			log.warn("printScore5 read error!");
		}

	}//printScore5()の括り


	/**９科目合計得点分布**/
	private void printScore9(DB2UDB db2,Vrw32alp svf,String param[],PreparedStatement ps3)
	{
		int ret = 0;
		try {
			int pp = 0;
			ResultSet rs = ps3.executeQuery();

			while( rs.next() ){
				//得点分布
				for (int i = 1; i < 12; i++) 
					ret = svf.VrsOutn("NUMBER9_1"	, i	, rs.getString("SCORE"+String.valueOf(i)) );
				for (int i = 12; i < 19; i++) 
					ret = svf.VrsOutn("NUMBER9_2"	, i-11	, rs.getString("SCORE"+String.valueOf(i)) );

				ret = svf.VrsOut("EXAMINEES9"	, rs.getString("JUKEN") );		//受験者数
				ret = svf.VrsOut("AVERAGE9"		, rs.getString("AVG_SCORE") );	//平均点
				ret = svf.VrsOut("MAXMUM9"		, rs.getString("MAX_SCORE") );	//最高点
				ret = svf.VrsOut("MINIMUM9"		, rs.getString("MIN_SCORE") );	//最低点
			}
			rs.close();
			db2.commit();
		} catch( Exception ex ) {
			log.warn("printScore9 read error!");
		}

	}//printScore9()の括り


	/**コース名取得**/
	private String statementCourse(String param[])
	{
		StringBuffer stb = new StringBuffer();
		//パラメータ（なし）
		try {
			stb.append("SELECT COURSECODE,COURSECODENAME ");
			stb.append("FROM   COURSECODE_MST ");
			stb.append("WHERE  COURSECODE IN "+param[6]+" ");
			stb.append("ORDER BY COURSECODE ");
		} catch( Exception e ){
			log.warn("statementCourse error!");
		}
		return stb.toString();

	}//statementCourse()の括り


	/**科目別得点分布**/
	private String statementScore(String param[])
	{
		StringBuffer stb = new StringBuffer();
		//パラメータ（なし）
		try {
			//在籍（学年・コース）
			stb.append("WITH SCHNO AS ( ");

			stb.append(statementTransfer(param));
            stb.append("    ) ");

			//成績---NO026
			stb.append(",SCORE_I AS ( ");
			stb.append("    SELECT CLASSCD AS SUBCLASSCD, ");
			if ("1".equals(param[10])) {
	            stb.append("    SCHOOL_KIND, ");
			}
			stb.append("           SCHREGNO, "+param[9]+" AS SCORE ");
			stb.append("    FROM   RECORD_CLASS_DAT ");
			stb.append("    WHERE  YEAR='"+param[0]+"' AND CLASSCD <= '09' AND ");
			stb.append("           "+param[9]+" IS NOT NULL ) ");

				//メイン
			stb.append("SELECT W1.SUBCLASSCD, ");
            if ("1".equals(param[10])) {
                stb.append("    W1.SCHOOL_KIND, ");
            }
			stb.append("       SUM(CASE WHEN 99 < SCORE THEN 1 ELSE 0 END) AS SCORE1, ");					// 100
			stb.append("       SUM(CASE WHEN 89 < SCORE AND SCORE < 100 THEN 1 ELSE 0 END) AS SCORE2, ");	//90-99
			stb.append("       SUM(CASE WHEN 79 < SCORE AND SCORE <  90 THEN 1 ELSE 0 END) AS SCORE3, ");	//80-89
			stb.append("       SUM(CASE WHEN 69 < SCORE AND SCORE <  80 THEN 1 ELSE 0 END) AS SCORE4, ");	//70-79
			stb.append("       SUM(CASE WHEN 59 < SCORE AND SCORE <  70 THEN 1 ELSE 0 END) AS SCORE5, ");	//60-69
			stb.append("       SUM(CASE WHEN 49 < SCORE AND SCORE <  60 THEN 1 ELSE 0 END) AS SCORE6, ");	//50-59
			stb.append("       SUM(CASE WHEN 39 < SCORE AND SCORE <  50 THEN 1 ELSE 0 END) AS SCORE7, ");	//40-49
			stb.append("       SUM(CASE WHEN 29 < SCORE AND SCORE <  40 THEN 1 ELSE 0 END) AS SCORE8, ");	//30-39
			stb.append("       SUM(CASE WHEN 19 < SCORE AND SCORE <  30 THEN 1 ELSE 0 END) AS SCORE9, ");	//20-29
			stb.append("       SUM(CASE WHEN  9 < SCORE AND SCORE <  20 THEN 1 ELSE 0 END) AS SCORE10, ");	//10-19
			stb.append("       SUM(CASE WHEN SCORE < 10 THEN 1 ELSE 0 END) AS SCORE11, ");					// 0-9
			stb.append("       DECIMAL(ROUND(AVG(FLOAT(SCORE))*10,0)/10,4,1) AS AVG_SCORE, ");//平均点
			stb.append("       MAX(SCORE) AS MAX_SCORE, ");//最高点
			stb.append("       MIN(SCORE) AS MIN_SCORE, ");//最低点
			stb.append("       COUNT(*) AS JUKEN ");//受験者数
			stb.append("FROM   SCORE_I W1,SCHNO W2 ");//NO025
			stb.append("WHERE  W1.SCHREGNO=W2.SCHREGNO ");
			stb.append("GROUP BY W1.SUBCLASSCD ");
            if ("1".equals(param[10])) {
                stb.append("    ,W1.SCHOOL_KIND ");
            }
		} catch( Exception e ){
			log.warn("statementScore error!");
		}
		return stb.toString();

	}//statementScore()の括り


	/**５科目合計得点分布**/
	private String statementScore5(String param[])
	{
		StringBuffer stb = new StringBuffer();
		//パラメータ（なし）
		try {
			//在籍（学年・コース）
			stb.append("WITH SCHNO AS ( ");
			stb.append(statementTransfer(param));
			stb.append("    ) ");
			//成績---NO026
			stb.append(",KIN_REC AS ( ");
			stb.append("    SELECT CLASSCD, ");
            if ("1".equals(param[10])) {
                stb.append("    SCHOOL_KIND, ");
            }
			stb.append("           SCHREGNO, "+param[9]+" AS SCORE ");
			stb.append("    FROM   RECORD_CLASS_DAT ");
			stb.append("    WHERE  YEAR='"+param[0]+"' AND CLASSCD <= '05' AND ");
			stb.append("           "+param[9]+" IS NOT NULL ) ");
			stb.append(",SCORE_SUM AS ( ");
			stb.append("    SELECT SCHREGNO,SUM(SCORE) AS SCORE ");
			stb.append("    FROM   KIN_REC ");
			stb.append("    GROUP BY SCHREGNO ");
			stb.append("    HAVING COUNT(*) = 5 ) ");

				//メイン
			stb.append("SELECT  ");
			stb.append("       SUM(CASE WHEN 459 < SCORE THEN 1 ELSE 0 END) AS SCORE1, ");					//460-500
			stb.append("       SUM(CASE WHEN 439 < SCORE AND SCORE < 460 THEN 1 ELSE 0 END) AS SCORE2, "); 	//440-459
			stb.append("       SUM(CASE WHEN 419 < SCORE AND SCORE < 440 THEN 1 ELSE 0 END) AS SCORE3, "); 	//420-439
			stb.append("       SUM(CASE WHEN 399 < SCORE AND SCORE < 420 THEN 1 ELSE 0 END) AS SCORE4, "); 	//400-419
			stb.append("       SUM(CASE WHEN 379 < SCORE AND SCORE < 400 THEN 1 ELSE 0 END) AS SCORE5, "); 	//380-399
			stb.append("       SUM(CASE WHEN 359 < SCORE AND SCORE < 380 THEN 1 ELSE 0 END) AS SCORE6, "); 	//360-379
			stb.append("       SUM(CASE WHEN 339 < SCORE AND SCORE < 360 THEN 1 ELSE 0 END) AS SCORE7, "); 	//340-359
			stb.append("       SUM(CASE WHEN 319 < SCORE AND SCORE < 340 THEN 1 ELSE 0 END) AS SCORE8, "); 	//320-339
			stb.append("       SUM(CASE WHEN 299 < SCORE AND SCORE < 320 THEN 1 ELSE 0 END) AS SCORE9, "); 	//300-319
			stb.append("       SUM(CASE WHEN 279 < SCORE AND SCORE < 300 THEN 1 ELSE 0 END) AS SCORE10, ");	//280-299
			stb.append("       SUM(CASE WHEN 259 < SCORE AND SCORE < 280 THEN 1 ELSE 0 END) AS SCORE11, ");	//260-279
			stb.append("       SUM(CASE WHEN 239 < SCORE AND SCORE < 260 THEN 1 ELSE 0 END) AS SCORE12, ");	//240-259
			stb.append("       SUM(CASE WHEN 219 < SCORE AND SCORE < 240 THEN 1 ELSE 0 END) AS SCORE13, ");	//220-239
			stb.append("       SUM(CASE WHEN 199 < SCORE AND SCORE < 220 THEN 1 ELSE 0 END) AS SCORE14, ");	//200-219
			stb.append("       SUM(CASE WHEN SCORE < 200 THEN 1 ELSE 0 END) AS SCORE15, ");					//  0-199
			stb.append("       DECIMAL(ROUND(AVG(FLOAT(SCORE))*10,0)/10,4,1) AS AVG_SCORE, ");//平均点
			stb.append("       MAX(SCORE) AS MAX_SCORE, ");//最高点
			stb.append("       MIN(SCORE) AS MIN_SCORE, ");//最低点
			stb.append("       COUNT(*) AS JUKEN ");//受験者数
			stb.append("FROM   SCORE_SUM W1,SCHNO W2 ");//NO025
			stb.append("WHERE  W1.SCHREGNO=W2.SCHREGNO ");
		} catch( Exception e ){
			log.warn("statementScore5 error!");
		}
		return stb.toString();

	}//statementScore5()の括り


	/**９科目合計得点分布**/
	private String statementScore9(String param[])
	{
		StringBuffer stb = new StringBuffer();
		//パラメータ（なし）
		try {
			//在籍（学年・コース）
			stb.append("WITH SCHNO AS ( ");
			stb.append(statementTransfer(param));
			stb.append("    ) ");
			//成績---NO026
			stb.append(",KIN_REC AS ( ");
			stb.append("    SELECT CLASSCD, ");
            if ("1".equals(param[10])) {
                stb.append("    SCHOOL_KIND, ");
            }
			stb.append("           SCHREGNO, "+param[9]+" AS SCORE ");
			stb.append("    FROM   RECORD_CLASS_DAT ");
			stb.append("    WHERE  YEAR='"+param[0]+"' AND CLASSCD <= '09' AND ");
			stb.append("           "+param[9]+" IS NOT NULL ) ");
			stb.append(",SCORE_SUM AS ( ");
			stb.append("    SELECT SCHREGNO,SUM(SCORE) AS SCORE ");
			stb.append("    FROM   KIN_REC ");
			stb.append("    GROUP BY SCHREGNO ");
			stb.append("    HAVING COUNT(*) = 9 ) ");

				//メイン
			stb.append("SELECT  ");
			stb.append("       SUM(CASE WHEN 849 < SCORE THEN 1 ELSE 0 END) AS SCORE1, ");					//850-900
			stb.append("       SUM(CASE WHEN 819 < SCORE AND SCORE < 850 THEN 1 ELSE 0 END) AS SCORE2, ");	//820-849
			stb.append("       SUM(CASE WHEN 789 < SCORE AND SCORE < 820 THEN 1 ELSE 0 END) AS SCORE3, ");	//790-819
			stb.append("       SUM(CASE WHEN 759 < SCORE AND SCORE < 790 THEN 1 ELSE 0 END) AS SCORE4, ");	//760-789
			stb.append("       SUM(CASE WHEN 729 < SCORE AND SCORE < 760 THEN 1 ELSE 0 END) AS SCORE5, ");	//730-759
			stb.append("       SUM(CASE WHEN 699 < SCORE AND SCORE < 730 THEN 1 ELSE 0 END) AS SCORE6, ");	//700-729
			stb.append("       SUM(CASE WHEN 669 < SCORE AND SCORE < 700 THEN 1 ELSE 0 END) AS SCORE7, ");	//670-699
			stb.append("       SUM(CASE WHEN 639 < SCORE AND SCORE < 670 THEN 1 ELSE 0 END) AS SCORE8, ");	//640-669
			stb.append("       SUM(CASE WHEN 609 < SCORE AND SCORE < 640 THEN 1 ELSE 0 END) AS SCORE9, ");	//610-639
			stb.append("       SUM(CASE WHEN 579 < SCORE AND SCORE < 610 THEN 1 ELSE 0 END) AS SCORE10, ");	//580-609
			stb.append("       SUM(CASE WHEN 549 < SCORE AND SCORE < 580 THEN 1 ELSE 0 END) AS SCORE11, ");	//550-579
			stb.append("       SUM(CASE WHEN 519 < SCORE AND SCORE < 550 THEN 1 ELSE 0 END) AS SCORE12, ");	//520-549
			stb.append("       SUM(CASE WHEN 489 < SCORE AND SCORE < 520 THEN 1 ELSE 0 END) AS SCORE13, ");	//490-519
			stb.append("       SUM(CASE WHEN 459 < SCORE AND SCORE < 490 THEN 1 ELSE 0 END) AS SCORE14, ");	//460-489
			stb.append("       SUM(CASE WHEN 429 < SCORE AND SCORE < 460 THEN 1 ELSE 0 END) AS SCORE15, ");	//430-459
			stb.append("       SUM(CASE WHEN 399 < SCORE AND SCORE < 430 THEN 1 ELSE 0 END) AS SCORE16, ");	//400-429
			stb.append("       SUM(CASE WHEN 369 < SCORE AND SCORE < 400 THEN 1 ELSE 0 END) AS SCORE17, ");	//370-399
			stb.append("       SUM(CASE WHEN SCORE < 370 THEN 1 ELSE 0 END) AS SCORE18, ");					//  0-369
			stb.append("       DECIMAL(ROUND(AVG(FLOAT(SCORE))*10,0)/10,4,1) AS AVG_SCORE, ");//平均点
			stb.append("       MAX(SCORE) AS MAX_SCORE, ");//最高点
			stb.append("       MIN(SCORE) AS MIN_SCORE, ");//最低点
			stb.append("       COUNT(*) AS JUKEN ");//受験者数
			stb.append("FROM   SCORE_SUM W1,SCHNO W2 ");//NO025
			stb.append("WHERE  W1.SCHREGNO=W2.SCHREGNO ");
		} catch( Exception e ){
			log.warn("statementScore9 error!");
		}
		return stb.toString();

	}//statementScore9()の括り


	/**PrepareStatement close**/
	private void statementClose(
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
			log.warn("statementClose error!");
		}
	}//statementClose()の括り


	/**
	 *	異動(共通SQL)
	 *
	 *　転学(2)・退学(3)者 但し異動日が学期終了日または異動基準日より小さい場合
	 *　転入(4)・編入(5)者 但し異動日が学期終了日または異動基準日より大きい場合
	 *　留学(1)・休学(2)者
	 */
	private String statementTransfer(String param[])
	{
		StringBuffer stb = new StringBuffer();
		try {
			//在籍 異動は見ない NO026Modify
			stb.append("    SELECT W1.SCHREGNO ");
			stb.append("    FROM   SCHREG_REGD_DAT W1 ");
			stb.append("    WHERE  W1.YEAR='"+param[0]+"' AND W1.SEMESTER='"+param[1]+"' AND ");
			stb.append("           W1.GRADE='"+param[5]+"' AND W1.COURSECODE IN "+param[6]+" ");//---学年・コース
		} catch( Exception e ){
			log.warn("statementTransfer error!",e);
		}
		return stb.toString();

	}//statementTransfer()の括り


}//クラスの括り
