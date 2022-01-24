/**
 *
 *	学校教育システム 賢者 [成績管理]
 *
 *					＜ＫＮＪＤ３２４＿２＞  得点分布表（中学：評価）（クラス別・試験別）
 *
 *	2005/05/13 nakamoto 作成日
 *	2005/05/19 nakamoto 作成日を追加。---NO006
 *	           nakamoto 退学・転学について、異動日が3月31日だったら、3月31日は在籍とする。---NO007
 *	2005/06/16 nakamoto 科目コードでの条件を教科コードに変更---NO008
 ***********************************************************************************************
 *  2005/10/26 nakamoto 編入のデータ仕様変更および在籍異動条件に転入学を追加---NO025
 *  2005/10/31 nakamoto 残作業No.29に対応：印刷指定に[学年選択]を追加。印刷指定が[コース選択]の場合、テスト種別に[絶対評価(学年評定)][相対評価(５段階)][相対評価(１０段階)]を追加。
 *  2005/11/08 nakamoto 絶対評価(学年評定)・相対評価(５段階)の場合、５段階用フォーム(KNJD324_3.frm)
 *	                    相対評価(１０段階)の場合、１０段階用フォーム(KNJD324_2.frm)を使用するように変更。---NO026
 *	2005/12/13 nakamoto NO027:(各教科)は、テーブル(RECORD_CLASS_DAT)から参照するように修正
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


public class KNJD324_2K {


    private static final Log log = LogFactory.getLog(KNJD324_2K.class);

	/*----------------------------*
	 * HTTP Get リクエストの処理  *
	 *----------------------------*/
	public void svf_out(HttpServletRequest request, HttpServletResponse response)
	                 throws ServletException, IOException
	{

		Vrw32alp svf = new Vrw32alp(); 			//PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
		DB2UDB db2 = null;						//Databaseクラスを継承したクラス
		String param[] = new String[12];//---NO006

	//	パラメータの取得
		String classcd1[] = request.getParameterValues("COURSE_CD");   	    //コースリスト
		String classcd2[] = request.getParameterValues("CLASS_SELECTED");   //クラスリスト
		String classcd3[] = request.getParameterValues("GRADELIST");   	    //学年リスト
		try {
	        param[0] = request.getParameter("YEAR");         				//年度
			param[1] = request.getParameter("CTRL_SEME");   				//今学期
			param[2] = request.getParameter("TESTKINDCD");   				//90:絶対評価(学年評定),91:相対評価(５段階),92:相対評価(１０段階)
			String idobi = request.getParameter("DATE");   					//異動対象日付
			param[7] = idobi.replace('/','-');

			param[3] = request.getParameter("OUTPUT");   				    //印刷指定 1:コース,2:クラス,3:学年
	        param[9] = request.getParameter("GRADE");         				//学年 1:コースで使用する
	        param[11] = request.getParameter("useCurriculumcd");
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
		boolean nonedata = false; 								//該当データなしフラグ
		setHeader(db2,svf,param);								//見出し出力のメソッド
for(int ia=0 ; ia<param.length ; ia++) log.debug("param["+ia+"]="+param[ia]);

		//1:コース
        if( param[3].equals("1") ) 
    		if( printSelect1(db2,svf,param,classcd1) ) nonedata = true;
		//2:クラス
        if( param[3].equals("2") ) 
    		if( printSelect23(db2,svf,param,classcd2) ) nonedata = true;
		//3:学年
        if( param[3].equals("3") ) 
    		if( printSelect23(db2,svf,param,classcd3) ) nonedata = true;

	//	該当データ無し
		if( !nonedata ){
			ret = svf.VrSetForm("MES001.frm", 0);
			ret = svf.VrsOut("note" , "");
			ret = svf.VrEndPage();
		}
log.debug("nonedata="+nonedata);

	// 	終了処理
		ret = svf.VrQuit();
		db2.commit();
		db2.close();				//DBを閉じる
		outstrm.close();			//ストリームを閉じる 

    }//doGetの括り



	/** SVF-FORM **/
	private void setHeader(DB2UDB db2,Vrw32alp svf,String param[]){

		int ret = 0;
    //  フォーム---NO026
        //テスト種別---90:絶対評価(学年評定),91:相対評価(５段階),92:相対評価(１０段階)
		if (param[2].equals("92"))  ret = svf.VrSetForm("KNJD324_2.frm", 1);//１０段階用フォーム
		else                        ret = svf.VrSetForm("KNJD324_3.frm", 1);//５段階用フォーム

	//	年度の取得
		param[5] = nao_package.KenjaProperties.gengou(Integer.parseInt(param[0])) + "年度";

	//	定期試験名の取得
		if (param[2].equals("90")) param[4] = "絶対評価(学年評定)";
		if (param[2].equals("91")) param[4] = "相対評価(５段階)";
		if (param[2].equals("92")) param[4] = "相対評価(１０段階)";

	//	作成日---NO006
		KNJ_Get_Info getinfo = new KNJ_Get_Info();
		KNJ_Get_Info.ReturnVal returnval = null;
		try {
			returnval = getinfo.Control(db2);
    		param[8] = KNJ_EditDate.h_format_JP(returnval.val3);
		} catch( Exception e ){
			log.warn("ctrl_date get error!");
		}
		getinfo = null;
		returnval = null;

	//	公欠・欠席の取得
//		if (param[2].equals("90")) param[6] = "CASE WHEN GRADE_ASSESS IS NOT NULL THEN GRADE_ASSESS ELSE NULL END";
//		if (param[2].equals("91")) param[6] = "CASE WHEN GRADE3_RELAASSESS_5STEP IS NOT NULL THEN GRADE3_RELAASSESS_5STEP ELSE NULL END";
//		if (param[2].equals("92")) param[6] = "CASE WHEN GRADE3_RELAASSESS_10STEP IS NOT NULL THEN GRADE3_RELAASSESS_10STEP ELSE NULL END";
        //NO027
		if (param[2].equals("90")) param[6] = "GRADE_ASSESS";
		if (param[2].equals("91")) param[6] = "GRADE3_RELAASSESS_5STEP";
		if (param[2].equals("92")) param[6] = "GRADE3_RELAASSESS_10STEP";

	}//setHeader()の括り


	/**印刷処理(選択1)**/
	private boolean printSelect1(DB2UDB db2,Vrw32alp svf,String param[],String classcd[])
	{
		boolean nonedata = false;
		int ret = 0;
		try {
       		//パラメータをセット
			param[10] = "(";
			for( int ia=0 ; ia<classcd.length ; ia++ ){
				if(ia > 0) param[10] = param[10] + ",";
				param[10] = param[10] + "'" + classcd[ia] + "'";
			}
			param[10] = param[10] + ")";
log.debug("start! "+param[10]);
       		//印刷
       		if( printMain(db2,svf,param) ) {
       			printHeader(svf,param);
                printCourseName(db2,svf,param);
       			ret = svf.VrEndPage();
       			nonedata = true;
       		}
		} catch( Exception ex ) {
			log.warn("printSelect1 read error!");
		}
		return nonedata;

	}//printSelect1()の括り


	/**印刷処理(選択23)**/
	private boolean printSelect23(DB2UDB db2,Vrw32alp svf,String param[],String classcd[])
	{
		boolean nonedata = false;
		int ret = 0;
		try {
    		for( int ia=0 ; ia<classcd.length ; ia++ ){
                param[10] = classcd[ia];
log.debug("start! "+param[10]);
        		if( printMain(db2,svf,param) ) {
        			printHeader(svf,param);
            		//2:クラス
                    if( param[3].equals("2") ) printHrName(db2,svf,param);
            		//3:学年
                    if( param[3].equals("3") ) printGradeName(svf,param);
        			ret = svf.VrEndPage();
        			nonedata = true;
        		}
    		}
		} catch( Exception ex ) {
			log.warn("printSelect23 read error!");
		}
		return nonedata;

	}//printSelect23()の括り


	/** タイトル */
	private void printHeader(Vrw32alp svf,String param[])
    {
		int ret = 0;
		try {
    		ret = svf.VrsOut("NENDO"    ,param[5]); //年度
    		ret = svf.VrsOut("COURSE"   ,param[4]); //定期試験名
			ret = svf.VrsOut("DATE"     ,param[8]); //作成日---NO006
		} catch( Exception ex ) {
			log.warn("printHeader read error!");
		}

	}//printHeader()の括り


	/** コース名称 */
	private void printCourseName(DB2UDB db2,Vrw32alp svf,String param[])
    {
		int ret = 0;
		try {
			PreparedStatement ps = db2.prepareStatement(statementCourse(param));    //コース名称取得
			ResultSet rs = ps.executeQuery();

			String coursename = "";
			String seq = "";
			while( rs.next() ){
				coursename = coursename + seq + rs.getString("COURSECODENAME");
				seq = "・";
			}
			ret = svf.VrsOut("HR_NAME" , "第"+String.valueOf(Integer.parseInt(param[9])) + "学年" + coursename );

			rs.close();
			ps.close();
			db2.commit();
		} catch( Exception ex ) {
			log.warn("printCourseName read error!");
		}

	}//printCourseName()の括り


	/** 組名称 */
	private void printHrName(DB2UDB db2,Vrw32alp svf,String param[])
    {
		int ret = 0;
		try {
			PreparedStatement ps = db2.prepareStatement(statementHrName(param));    //組名称取得
			ResultSet rs = ps.executeQuery();

			while( rs.next() ){
				ret = svf.VrsOut("HR_NAME" , rs.getString("HR_NAME") );
			}
			rs.close();
			ps.close();
			db2.commit();
		} catch( Exception ex ) {
			log.warn("printHrName read error!");
		}

	}//printHrName()の括り


	/** 学年 */
	private void printGradeName(Vrw32alp svf,String param[])
    {
		int ret = 0;
		try {
			ret = svf.VrsOut("HR_NAME" , "第"+String.valueOf(Integer.parseInt(param[10])) + "学年" );
		} catch( Exception ex ) {
			log.warn("printGradeName read error!");
		}

	}//printGradeName()の括り


	/**科目別得点分布**/
	private boolean printMain(DB2UDB db2,Vrw32alp svf,String param[])
	{
		boolean nonedata = false;
		int ret = 0;
		try {
			PreparedStatement ps = db2.prepareStatement(statementScore(param));    //科目別得点分布取得
			ResultSet rs = ps.executeQuery();

			int gyo = 5;//NO026
       		if (param[2].equals("92")) gyo = 10;//NO026
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
				for (int i = 1; i < (gyo+1); i++) {
               		if (param[2].equals("92")) 
    					ret = svf.VrsOutn("SUBCLASS"+retsu	, i	, rs.getString("SCORE"+String.valueOf(i)) );
                    else 
					    ret = svf.VrsOutn("SUBCLASS"+retsu	, i	, rs.getString("SCORE"+String.valueOf(i+gyo)) );
                }
				ret = svf.VrsOutn("SUBCLASS"+retsu	, gyo+1	, rs.getString("JUKEN") );		//受験者数
				ret = svf.VrsOutn("SUBCLASS"+retsu	, gyo+2	, rs.getString("AVG_SCORE") );	//平均点
				ret = svf.VrsOutn("SUBCLASS"+retsu	, gyo+3	, rs.getString("MAX_SCORE") );	//最高点
				ret = svf.VrsOutn("SUBCLASS"+retsu	, gyo+4	, rs.getString("MIN_SCORE") );	//最低点
/*****NO026
				//得点分布
				for (int i = 1; i < 11; i++) 
					ret = svf.VrsOutn("SUBCLASS"+retsu	, i	, rs.getString("SCORE"+String.valueOf(i)) );

				ret = svf.VrsOutn("SUBCLASS"+retsu	, 11	, rs.getString("JUKEN") );		//受験者数
				ret = svf.VrsOutn("SUBCLASS"+retsu	, 12	, rs.getString("AVG_SCORE") );	//平均点
				ret = svf.VrsOutn("SUBCLASS"+retsu	, 13	, rs.getString("MAX_SCORE") );	//最高点
				ret = svf.VrsOutn("SUBCLASS"+retsu	, 14	, rs.getString("MIN_SCORE") );	//最低点
*****/

				nonedata = true;
			}
			rs.close();
			ps.close();
			db2.commit();
		} catch( Exception ex ) {
			log.warn("printMain read error!");
		}
		return nonedata;

	}//printMain()の括り


	/**組名称取得*/
	private String statementHrName(String param[])
	{
		StringBuffer stb = new StringBuffer();
		//パラメータ（１つ）
		try {
			stb.append("SELECT HR_NAME ");
			stb.append("FROM   SCHREG_REGD_HDAT ");
			stb.append("WHERE  YEAR='"+param[0]+"' AND SEMESTER='"+param[1]+"' AND ");
			stb.append("       GRADE||HR_CLASS = '"+param[10]+"' ");
		} catch( Exception e ){
			log.warn("statementHrName error!");
		}
		return stb.toString();

	}//statementHrName()の括り


	/**コース名取得**/
	private String statementCourse(String param[])
	{
		StringBuffer stb = new StringBuffer();
		//パラメータ（なし）
		try {
			stb.append("SELECT COURSECODE,COURSECODENAME ");
			stb.append("FROM   COURSECODE_MST ");
			stb.append("WHERE  COURSECODE IN "+param[10]+" ");
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
		//パラメータ（１つ）
		try {
			//在籍（学年・組）---NO027
			stb.append("WITH SCHNO AS ( ");
			stb.append("    SELECT SCHREGNO ");
			stb.append("    FROM   SCHREG_REGD_DAT ");
			stb.append("    WHERE  YEAR='"+param[0]+"' AND SEMESTER='"+param[1]+"' AND ");
    		//1:コース
            if( param[3].equals("1") ) 
    			stb.append("       GRADE = '"+param[9]+"' AND COURSECODE IN "+param[10]+" ");
    		//2:クラス
            if( param[3].equals("2") ) 
    			stb.append("       GRADE||HR_CLASS = '"+param[10]+"' ");
    		//3:学年
            if( param[3].equals("3") ) 
    			stb.append("       GRADE = '"+param[10]+"' ");
			stb.append("    ) ");
			//成績---NO027
			stb.append(",SCORE_I AS ( ");
			stb.append("    SELECT CLASSCD AS SUBCLASSCD, ");
			if ("1".equals(param[11])) {
	            stb.append("    SCHOOL_KIND, ");
			}
			stb.append("           SCHREGNO, "+param[6]+" AS SCORE ");
			stb.append("    FROM   RECORD_CLASS_DAT ");
			stb.append("    WHERE  YEAR='"+param[0]+"' AND CLASSCD <= '09' AND ");
			stb.append("           "+param[6]+" IS NOT NULL ) ");

				//メイン
			stb.append("SELECT W1.SUBCLASSCD, ");
            if ("1".equals(param[11])) {
                stb.append("    W1.SCHOOL_KIND, ");
            }
			stb.append("       SUM(CASE WHEN SCORE = 10 THEN 1 ELSE 0 END) AS SCORE1, ");	//10
			stb.append("       SUM(CASE WHEN SCORE = 9  THEN 1 ELSE 0 END) AS SCORE2, ");	//9
			stb.append("       SUM(CASE WHEN SCORE = 8  THEN 1 ELSE 0 END) AS SCORE3, ");	//8
			stb.append("       SUM(CASE WHEN SCORE = 7  THEN 1 ELSE 0 END) AS SCORE4, ");	//7
			stb.append("       SUM(CASE WHEN SCORE = 6  THEN 1 ELSE 0 END) AS SCORE5, ");	//6
			stb.append("       SUM(CASE WHEN SCORE = 5  THEN 1 ELSE 0 END) AS SCORE6, ");	//5
			stb.append("       SUM(CASE WHEN SCORE = 4  THEN 1 ELSE 0 END) AS SCORE7, ");	//4
			stb.append("       SUM(CASE WHEN SCORE = 3  THEN 1 ELSE 0 END) AS SCORE8, ");	//3
			stb.append("       SUM(CASE WHEN SCORE = 2  THEN 1 ELSE 0 END) AS SCORE9, ");	//2
			stb.append("       SUM(CASE WHEN SCORE = 1  THEN 1 ELSE 0 END) AS SCORE10, ");	//1
			stb.append("       DECIMAL(ROUND(AVG(FLOAT(SCORE))*10,0)/10,4,1) AS AVG_SCORE, ");//平均点
			stb.append("       MAX(SCORE) AS MAX_SCORE, ");//最高点
			stb.append("       MIN(SCORE) AS MIN_SCORE, ");//最低点
			stb.append("       COUNT(*) AS JUKEN ");//受験者数
			stb.append("FROM   SCORE_I W1,SCHNO W2 ");//NO025Modify
			//stb.append("FROM   SCORE_I W1,SCHNO_I W2 ");
			stb.append("WHERE  W1.SCHREGNO=W2.SCHREGNO ");
			stb.append("GROUP BY W1.SUBCLASSCD ");
            if ("1".equals(param[11])) {
                stb.append("    , W1.SCHOOL_KIND ");
            }
		} catch( Exception e ){
			log.warn("statementScore error!");
		}
		return stb.toString();

	}//statementScore()の括り



}//クラスの括り
