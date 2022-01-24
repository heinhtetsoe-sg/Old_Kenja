/**
 *
 *	学校教育システム 賢者 [成績管理]
 *
 *					＜ＫＮＪＤ３２５＞  成績一覧表（中学）（クラス別・個人別）
 *
 *	2005/03/02 nakamoto 作成日
 *	2005/05/09 nakamoto 学年成績は、学期名称の表記はいらない。---NO001
 *	           nakamoto 今学期と学籍処理日のパラメータを追加。---NO002
 *	           nakamoto 絶対評価と相対評価を追加。---NO003
 *	2005/05/12 nakamoto 素点(中間・期末)以外を出力する場合、公欠・欠席は見ない。---NO004
 *	           nakamoto 「学級合計行の９科平均」＝「学級合計行の９科合計」÷９・・・小数点２位以下切り捨て---NO005
 *	           nakamoto 朱点欄は中間・期末のみ印字。---NO006
 *	           nakamoto Ｆ表示有り／無しの２種類を出力可能とする。---NO007
 *	2005/05/17 nakamoto 時間(○時○分)を表示。---NO008
 *	2005/05/18 nakamoto 成績未入力の場合は「（　）」を表示。---NO009
 *	           nakamoto 合計・平均・席次を計算できない場合は「（　）」を表示。---NO010
 *	           nakamoto 中間試験時の４科目(音楽、美術、保体、技家)は、ブランク表示。---NO011
 *	           nakamoto 異動者については「（　）」表示は不要。---NO012
 *	2005/05/19 nakamoto 一括出力機能の追加。---NO014
 *	           nakamoto 退学・転学について、異動日が3月31日だったら、3月31日は在籍とする。---NO015
 *	2005/05/26 nakamoto (NO010)席次の不具合を修正。「9科/5科それぞれ、1科目でも成績がない場合は席次をつけない」---NO016
 *	           nakamoto NO009仕様変更。中間・期末では、成績未入力の場合「ブランク」を表示。---NO017
 *	2005/05/27 nakamoto 2J01クラスのコース平均点が違う不具合を修正。---NO018
 *
 *	2005/06/13 nakamoto コースグループ設定で設定したテーブル(COURSE_GROUP)を参照---NO019
 *	2005/06/16 nakamoto 科目コードでの条件を教科コードに変更---NO020
 *	2005/06/18 nakamoto 学年成績未入力の場合は「−」を表示。---NO021
 *	2005/07/06 nakamoto テーブル名変更による修正(COURSE_GROUP⇒COURSE_GROUP_DAT)---NO022
 *  2005/07/09 yamasiro 同一教科に複数成績データが存在し片方の成績がNULLの場合成績が出力されない不具合を修正---NO023
 *
 *************************
 *
 *	2005/10/13 nakamoto 席次は、テーブル(RECORD_RANK_DAT)から参照するように修正---NO024
 *	           nakamoto 編入のデータ仕様変更および在籍異動条件に転入学を追加---NO025
 *	2005/12/12 nakamoto NO026:(学級合計・学級平均・コース平均・朱点)は、テーブル(RECORD_CLASS_AVERAGE_DAT)から参照するように修正
 *	2006/02/26 nakamoto NO027:学期成績が2つ以下の場合学年成績'-'表示。中間期末共にKK又は、KSの場合1/2学期成績()表示。
 *	2006/07/10 nakamoto NO028:NO027の不具合。メール20060710（学期成績一覧表において、中間試験得点、期末試験得点の一方または両方を欠く生徒の得点が（）付き数字で表記されるケースがある）
 *	2006/07/11 nakamoto NO029:メール20060711の対応。「括弧付印字条件」をコメントし、納品後、コメントをはずした。
 *	2006/10/16 m-yama   NO030:NO027の1/2学期成績()表示は、遡及入力フラグ(1)の場合に変更。
 *	2007/05/30 nakamoto NO031:F表示の条件で指定している附属の出身学校コードを「0000193」から「0001010」に変更した。
 */

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


public class KNJD325K {


    private static final Log log = LogFactory.getLog(KNJD325K.class);

	/*----------------------------*
	 * HTTP Get リクエストの処理  *
	 *----------------------------*/
	public void svf_out(HttpServletRequest request, HttpServletResponse response)
	                 throws ServletException, IOException
	{

		Vrw32alp svf = new Vrw32alp(); 			//PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
		DB2UDB db2 = null;						//Databaseクラスを継承したクラス
		String param[] = new String[20];//---NO014 NO024

	//	パラメータの取得
		String classcd[] = request.getParameterValues("CLASS_SELECTED");    //学年・組---NO014
		try {
	        param[0] = request.getParameter("YEAR");         				//年度
			param[1] = request.getParameter("GAKKI");   					//学期 1,2,3,9
			param[2] = request.getParameter("TESTKINDCD");   				//01:中間,02:期末,0:学期成績,9:学年成績
			param[5] = "('010100','020100','030100','040100','050100')";
			param[9] = "('010100','020100','030100','040100','050100','060100','070100','080100','090100')";
			String idobi = request.getParameter("DATE");   					//異動対象日付
			param[12] = idobi.replace('/','-');
        	//学年末の場合、今学期をセット---NO002
			if (param[1].equals("9")) param[1] = request.getParameter("CTRL_SEME");
			param[13] = request.getParameter("CTRL_DAY");                   //学籍処理日
            //Ｆ表示有り／無し---NO007
			param[14] = request.getParameter("FINCD");                      //Ｆ表示有り／無し
			param[19] = request.getParameter("useCurriculumcd");
		} catch( Exception ex ) {
			log.warn("parameter error!",ex);
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
			log.error("DB2 open error!",ex);
			return;
		}


	//	ＳＶＦ作成処理
		boolean nonedata = false; 								//該当データなしフラグ

		setHeader(db2,svf,param);								//見出しセットのメソッド
		setParamAbsence(param);									//公欠・欠席パラメータのメソッド
for(int ia=0 ; ia<param.length ; ia++) log.debug("param["+ia+"]="+param[ia]);
		//SVF出力---NO014
		for( int ia=0 ; ia<classcd.length ; ia++ ){
log.debug("start! "+classcd[ia]);
    		printHeader(db2,svf,param);				//見出し出力---NO014
            param[8] = classcd[ia];                 //年組---NO014
			param[10] = param[8].substring(0,2);    //学年---NO014
            setParamCourse(db2,param);              //クラス別コース設定(param[6])---NO014
    		if( printScoreStudent(db2,svf,param) ){
    			printHeaderHrName(db2,svf,param);
    			printHeaderCourse(db2,svf,param);
    			printScoreSubject(db2,svf,param);
    			printScoreTotal(db2,svf,param);
    			printScoreAbsence(db2,svf,param);
    			printScoreTransfer(db2,svf,param);
    			if (param[14] != null) printFinschoolStudent(db2,svf,param);//Ｆ表示---NO007
    			ret = svf.VrEndPage();
    			nonedata = true;
    		}
log.debug("end! "+classcd[ia]);
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
		db2.commit();
		db2.close();				//DBを閉じる
		outstrm.close();			//ストリームを閉じる 

    }//doGetの括り


	/** 見出しセット */
	private void setHeader(DB2UDB db2,Vrw32alp svf,String param[]){

		KNJ_Get_Info getinfo = new KNJ_Get_Info();
		KNJ_Get_Info.ReturnVal returnval = null;
		int ret = 0;
		ret = svf.VrSetForm("KNJD325.frm", 1);
        param[15] = nao_package.KenjaProperties.gengou(Integer.parseInt(param[0])) + "年度";//---NO014
	//	学年末の場合、今学期をセット---NO002
		try {
//			returnval = getinfo.Control(db2);
//			if (param[1].equals("9")) param[1] = returnval.val2;
//			ret = svf.VrsOut("DATE",KNJ_EditDate.h_format_JP(returnval.val3));		//作成日
            //---↓---NO008---↓---//
			String sql = "VALUES RTRIM(CHAR(DATE(SYSDATE()))),RTRIM(CHAR(HOUR(SYSDATE()))),RTRIM(CHAR(MINUTE(SYSDATE())))";
			db2.query(sql);
			ResultSet rs = db2.getResultSet();
			String arr_ctrl_date[] = new String[3];
            int number = 0;
			while( rs.next() ){
    			arr_ctrl_date[number] = rs.getString(1);
                number++;
			}
			db2.commit();
            param[13] = KNJ_EditDate.h_format_JP(arr_ctrl_date[0])+"　"+arr_ctrl_date[1]+"時"+arr_ctrl_date[2]+"分";//---NO014
            //---↑---NO008---↑---//
//			ret = svf.VrsOut("DATE",KNJ_EditDate.h_format_JP(param[13]));		//作成日
		} catch( Exception e ){
			log.warn("ctrl_date get error!",e);
		}
	//	学期名称の取得
		try {
			returnval = getinfo.Semester(db2,param[0],param[1]);
			param[4] = returnval.val1;
            //NO025
			param[18] = returnval.val3;//学期終了日
		} catch( Exception e ){
			log.warn("Semester name get error!",e);
		}
	//	定期試験名の取得
		String stb = "";
		if (param[2].equals("01")) stb = "中間試験";
		if (param[2].equals("02")) stb = "期末試験";
		if (param[2].equals("02") && param[1].equals("3")) stb = "期末試験";
		if (param[2].equals("0"))  stb = "学期成績";
		if (param[2].equals("9"))  stb = "学年成績";
        //---NO003
		if (param[2].equals("90")) stb = "絶対評価(学年評定)";
		if (param[2].equals("91")) stb = "相対評価(５段階)";
		if (param[2].equals("92")) stb = "相対評価(１０段階)";
        //---NO001
		if (param[2].equals("01") || param[2].equals("02") || param[2].equals("0")) 
			param[4] = param[4] + stb;//---NO014
		else 
			param[4] = stb;//---NO014
	//	定期試験フィールド名の取得---NO024Add
		if (param[2].equals("01")) param[17] = "SEM"+param[1]+"_INTER_REC";
		if (param[2].equals("02")) param[17] = "SEM"+param[1]+"_TERM_REC";
		if (param[2].equals("0"))  param[17] = "SEM"+param[1]+"_REC";
		if (param[2].equals("9"))  param[17] = "GRADE_RECORD";
		if (param[2].equals("90")) param[17] = "GRADE_ASSESS";
		if (param[2].equals("91")) param[17] = "GRADE3_RELAASSESS_5STEP";
		if (param[2].equals("92")) param[17] = "GRADE3_RELAASSESS_10STEP";

		getinfo = null;
		returnval = null;

	//  朱点---NO006---中間・期末のみ印字
   		if ( param[2].equals("01") || param[2].equals("02") ) 
			param[16] = "朱　点（以下）";//---NO014

	}//setHeader()の括り


	/** 見出し出力---NO014 */
	private void printHeader(DB2UDB db2,Vrw32alp svf,String param[]){

		int ret = 0;
		try {
    		ret = svf.VrsOut("NENDO"    ,param[15]);    //年度
    		ret = svf.VrsOut("DATE"     ,param[13]);    //作成日時
    		ret = svf.VrsOut("TESTNAME" ,param[4]);     //定期試験名
    		ret = svf.VrsOut("FAILURE"  ,param[16]);    //朱点
		} catch( Exception e ){
			log.warn("printHeader error!",e);
		}

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
			param[7] = "CASE WHEN "+param[3]+" IS NULL AND "+param[3]+"_DI IN('KK','KS') THEN 'KK' "
					 + "     WHEN "+param[3]+" IS NOT NULL THEN RTRIM(CHAR("+param[3]+")) "
					 + "     ELSE NULL END";
			param[11] = "CASE WHEN "+param[3]+"_DI IN('KS') THEN '（欠）' "
					  + "     WHEN "+param[3]+"_DI IN('KK') THEN '（公）' "
					  + "     ELSE '' END";//---NO017
		//学期成績
		} else if (param[2].equals("0")) {
			//３学期
			if (2 < Integer.parseInt(param[1])) {
				param[7] = "CASE WHEN SEM3_REC IS NOT NULL THEN RTRIM(CHAR(SEM3_REC)) "
						 + "     ELSE NULL END";
				param[11] = "CASE WHEN SCORE IS NULL THEN '（　）' "
						  + "     ELSE '' END";
			//２学期
			} else if (1 < Integer.parseInt(param[1])) {
				param[7] = "CASE WHEN SEM2_REC IS NOT NULL THEN RTRIM(CHAR(SEM2_REC)) "
						 + "     ELSE NULL END";
				param[11] = "CASE WHEN SCORE IS NULL THEN '（　）' "
						  + "     ELSE '' END";
			//１学期
			} else {
				param[7] = "CASE WHEN SEM1_REC IS NOT NULL THEN RTRIM(CHAR(SEM1_REC)) "
						 + "     ELSE NULL END";
				param[11] = "CASE WHEN SCORE IS NULL THEN '（　）' "
						  + "     ELSE '' END";
			}
		//学年成績
		} else {
			param[7] = "CASE "+param[3]+" "//---NO003
					 + "     ELSE NULL END";
			param[11] = "CASE WHEN SCORE IS NULL AND (SEM1_REC_MARK+SEM2_REC_MARK+SEM3_REC_MARK) <= 1 THEN '　−　' "
					  + "     ELSE '' END";
		}

	}//setParamAbsence()の括り


	/** クラス別コース設定(コース平均計算単位の取得)---NO014---NO019--- */
	private void setParamCourse(DB2UDB db2,String param[]){

		try {
			PreparedStatement ps = db2.prepareStatement(statementCourseGroup(param));//コースを取得
			ResultSet rs = ps.executeQuery();

            int ia = 0;
			param[6] = "(";
			while( rs.next() ){
				if (ia > 0) param[6] = param[6] + ",";
				param[6] = param[6] + "'" + rs.getString("COURSECODE") + "'";
                ia++;
			}
			param[6] = param[6] + ")";
            if (ia == 0) param[6] = "('0000')";//対象データなし(コースグループ未設定)

			rs.close();
			ps.close();
			db2.commit();
		} catch( Exception ex ) {
			log.warn("setParamCourse error!",ex);
		}

	}//setParamCourse()の括り


	/** 組名称 */
	private void printHeaderHrName(DB2UDB db2,Vrw32alp svf,String param[]){

		int ret = 0;
		try {
			PreparedStatement ps9 = db2.prepareStatement(statementHrName(param));//組名称取得
			ResultSet rs = ps9.executeQuery();

			while( rs.next() ){
				ret = svf.VrsOut("HR_NAME" , rs.getString("HR_NAME") );
			}
			rs.close();
			ps9.close();
			db2.commit();
		} catch( Exception ex ) {
			log.warn("printHeaderHrName read error!",ex);
		}

	}//printHeaderHrName()の括り


	/** コース名出力 */
	private void printHeaderCourse(DB2UDB db2,Vrw32alp svf,String param[]){

		int ret = 0;
		try {
			PreparedStatement ps = db2.prepareStatement(statementCourseName(param));//コース名取得
			ResultSet rs = ps.executeQuery();

			String coursename = "";
			int reccnt = 0;
			while( rs.next() ){
				if (reccnt > 0) coursename = coursename + ", ";
				coursename = coursename + rs.getString("COURSECODENAME");
				reccnt++;
			}
			//コースが複数
			if (reccnt > 1) {
				ret = svf.VrsOut("COURSE" , "コース平均" );
				ret = svf.VrsOut("COURSE_NOTE" , "コース平均：" + coursename );
			} else if (reccnt > 0) {
				ret = svf.VrsOut("COURSE" , coursename + "平均" );
			}
			rs.close();
			ps.close();
			db2.commit();
		} catch( Exception ex ) {
			log.warn("printHeaderCourse read error!",ex);
		}

	}//printHeaderCourse()の括り



	/**生徒別（素点・５科・９科）*/
	private boolean printScoreStudent(DB2UDB db2,Vrw32alp svf,String param[])
	{
		boolean nonedata = false;
		int ret = 0;
		try {
			PreparedStatement ps1 = db2.prepareStatement(statementScoreStudent(param));	//生徒別（素点・５科・９科）
			ResultSet rs = ps1.executeQuery();
			int gyo = 0;
			while( rs.next() ){
				//行
				gyo = Integer.parseInt(rs.getString("ATTENDNO"));
				//出席番号・氏名
				ret = svf.VrsOutn("ATTENDNO" , gyo , (rs.getString("ATTENDNO")).substring(1) );//２桁表示
				ret = svf.VrsOutn("NAME"     , gyo , rs.getString("NAME") );
				//各科目---01:国語,02:社会,03:数学,04:理科,06:音楽,07:美術,08:保体,09:技家,05:英語
				for (int i = 1; i < 10; i++) {
                    if (param[2].equals("0") && 
                        Integer.parseInt(param[1]) < 3 && 
                        rs.getString("SUB"+String.valueOf(i)+"_ABS") != null && 
                        (rs.getString("SUB"+String.valueOf(i)+"_ABS")).equals("1")) {
                        ret = svf.VrsOutn("POINT"+String.valueOf(i) , gyo , "(" + rs.getString("SUB"+String.valueOf(i)) + ")" );
                    } else {
                        ret = svf.VrsOutn("POINT"+String.valueOf(i) , gyo , rs.getString("SUB"+String.valueOf(i)) );
                    }
					if (rs.getString("SUB"+String.valueOf(i)) != null) nonedata = true;//該当データなしフラグ
				}
				//５科（合計・平均・席次）
				ret = svf.VrsOutn("5TOTAL"   , gyo , (rs.getString("SUM5") != null) ? rs.getString("SUM5") : "（　）" );
				ret = svf.VrsOutn("5AVERAGE" , gyo , (rs.getString("AVG5") != null) ? rs.getString("AVG5") : "（　）" );
				ret = svf.VrsOutn("5RANK"    , gyo , (rs.getString("RNK5") != null) ? rs.getString("RNK5") : "（　）" );
				//９科（合計・平均・席次）（中間試験以外）
				if (!param[2].equals("01")) {
					ret = svf.VrsOutn("9TOTAL"   , gyo , (rs.getString("SUM9") != null) ? rs.getString("SUM9") : "（　）" );
					ret = svf.VrsOutn("9AVERAGE" , gyo , (rs.getString("AVG9") != null) ? rs.getString("AVG9") : "（　）" );
					ret = svf.VrsOutn("9RANK"    , gyo , (rs.getString("RNK9") != null) ? rs.getString("RNK9") : "（　）" );
				}
			}
			rs.close();
			ps1.close();
			db2.commit();
		} catch( Exception ex ) {
			log.warn("printScoreStudent read error!",ex);
		}
		return nonedata;

	}//printScoreStudent()の括り


	/**学級合計・学級平均・コース平均（科目別）*/
	private void printScoreSubject(DB2UDB db2,Vrw32alp svf,String param[])
	{
		int ret = 0;
		try {
			PreparedStatement ps2 = db2.prepareStatement(statementScoreSubject(param));	//科目別合計
			ResultSet rs = ps2.executeQuery();

			//各科目---01:国語,02:社会,03:数学,04:理科,06:音楽,07:美術,08:保体,09:技家,05:英語
//			String subclasscd[] = {"010100","020100","030100","040100","060100","070100","080100","090100","050100"};
			String subclasscd[] = {"01","02","03","04","06","07","08","09","05"};
			String retsu = "0";//列
			while( rs.next() ){
				//列
				for (int ia = 0; ia < subclasscd.length; ia++)
					if( rs.getString("SUBCLASSCD").equals(subclasscd[ia]) ) retsu = String.valueOf((ia+1));
				//学級合計・学級平均・コース平均
				for (int ib = 46; ib < 49; ib++) 
					ret = svf.VrsOutn("POINT"+retsu , ib , rs.getString("POINT"+String.valueOf(ib)) );
				//朱点（５科のみ）//---NO006---中間・期末のみ印字
        		if ( param[2].equals("01") || param[2].equals("02") ) 
    				if ( (Integer.parseInt(retsu) < 5) || retsu.equals("9") ) 
    					ret = svf.VrsOutn("POINT"+retsu , 49 , rs.getString("AKATEN") );
			}
			rs.close();
			ps2.close();
			db2.commit();
		} catch( Exception ex ) {
			log.warn("printScoreSubject read error!",ex);
		}

	}//printScoreSubject()の括り


	/**学級合計・学級平均・コース平均（５科・９科）*/
	private void printScoreTotal(DB2UDB db2,Vrw32alp svf,String param[])
	{
		int ret = 0;
		try {
			int pp = 0;
			PreparedStatement ps3 = db2.prepareStatement(statementScoreTotal(param));//５科・９科合計
			ResultSet rs = ps3.executeQuery();

			while( rs.next() ){
				//５科
				if ((rs.getString("KEY")).equals("5")) {
					for (int i = 46; i < 49; i++) {
						ret = svf.VrsOutn("5TOTAL"   , i , rs.getString("SUM"+String.valueOf(i)) );
						ret = svf.VrsOutn("5AVERAGE" , i , rs.getString("AVG"+String.valueOf(i)) );
					}
				}
				//９科（中間試験以外）
				if (!param[2].equals("01") && (rs.getString("KEY")).equals("9")) {
					for (int i = 46; i < 49; i++) {
						ret = svf.VrsOutn("9TOTAL"   , i , rs.getString("SUM"+String.valueOf(i)) );
						ret = svf.VrsOutn("9AVERAGE" , i , rs.getString("AVG"+String.valueOf(i)) );
					}
				}
			}
			rs.close();
			ps3.close();
			db2.commit();
		} catch( Exception ex ) {
			log.warn("printScoreTotal read error!",ex);
		}

	}//printScoreTotal()の括り


	/**公欠・欠席*/
	private void printScoreAbsence(DB2UDB db2,Vrw32alp svf,String param[])
	{
		int ret = 0;
		try {
			PreparedStatement ps4 = db2.prepareStatement(statementScoreAbsence(param));	//公欠・欠席
			ResultSet rs = ps4.executeQuery();
//log.debug("ps4="+ps4.toString());
			//各科目---01:国語,02:社会,03:数学,04:理科,06:音楽,07:美術,08:保体,09:技家,05:英語
//			String subclasscd[] = {"010100","020100","030100","040100","060100","070100","080100","090100","050100"};
			String subclasscd[] = {"01","02","03","04","06","07","08","09","05"};
			String retsu = "0";	//列
			int gyo = 0;		//行
			while( rs.next() ){
				//行
				gyo = Integer.parseInt(rs.getString("ATTENDNO"));
				//列
				for (int ia = 0; ia < subclasscd.length; ia++)
					if( rs.getString("SUBCLASSCD").equals(subclasscd[ia]) ) retsu = String.valueOf((ia+1));
				//公欠・欠席
				ret = svf.VrsOutn("POINT"+retsu , gyo , rs.getString("SCORE_DI") );
			}
			rs.close();
			ps4.close();
			db2.commit();
		} catch( Exception ex ) {
			log.warn("printScoreAbsence read error!",ex);
		}

	}//printScoreAbsence()の括り


	/**異動*/
	private void printScoreTransfer(DB2UDB db2,Vrw32alp svf,String param[])
	{
		int ret = 0;
		try {
			PreparedStatement ps5 = db2.prepareStatement(statementScoreTransfer(param));	//異動
			ResultSet rs = ps5.executeQuery();

			int gyo = 0;		//行
			while( rs.next() ){
				//行
				gyo = Integer.parseInt(rs.getString("ATTENDNO"));
				//異動日付と異動区分
				ret = svf.VrsOutn("NOTE" , gyo , KNJ_EditDate.h_format_JP(rs.getString("IDOU_DATE"))
												 + rs.getString("IDOU_NAME") );
				//５科・９科（合計・平均・席次）にブランクをセット---NO012
				ret = svf.VrsOutn("5TOTAL"   , gyo , "" );
				ret = svf.VrsOutn("5AVERAGE" , gyo , "" );
				ret = svf.VrsOutn("5RANK"    , gyo , "" );
				ret = svf.VrsOutn("9TOTAL"   , gyo , "" );
				ret = svf.VrsOutn("9AVERAGE" , gyo , "" );
				ret = svf.VrsOutn("9RANK"    , gyo , "" );
			}
			rs.close();
			ps5.close();
			db2.commit();
		} catch( Exception ex ) {
			log.warn("printScoreTransfer read error!",ex);
		}

	}//printScoreTransfer()の括り


	/**Ｆ表示---NO007*/
	private void printFinschoolStudent(DB2UDB db2,Vrw32alp svf,String param[])
	{
		int ret = 0;
		try {
			PreparedStatement ps6 = db2.prepareStatement(statementFinschoolStudent(param));	//Ｆ表示---NO007
			ResultSet rs = ps6.executeQuery();

			int gyo = 0;		//行
			while( rs.next() ){
				//行
				gyo = Integer.parseInt(rs.getString("ATTENDNO"));
				//附属小学校からの入学者
				ret = svf.VrsOutn("MARK" , gyo , rs.getString("FIN") );
			}
			rs.close();
			ps6.close();
			db2.commit();
		} catch( Exception ex ) {
			log.warn("printFinschoolStudent read error!",ex);
		}

	}//printFinschoolStudent()の括り


	/**組名称取得*/
	private String statementHrName(String param[])
	{
		StringBuffer stb = new StringBuffer();
		//パラメータ（なし）
		try {
			stb.append("SELECT HR_NAME ");
			stb.append("FROM   SCHREG_REGD_HDAT ");
			stb.append("WHERE  YEAR='"+param[0]+"' AND SEMESTER='"+param[1]+"' AND ");
			stb.append("       GRADE||HR_CLASS = '"+param[8]+"' ");
		} catch( Exception e ){
			log.warn("statementHrName error!",e);
		}
		return stb.toString();

	}//statementHrName()の括り


	/**コース名取得*/
	private String statementCourseName(String param[])
	{
		StringBuffer stb = new StringBuffer();
		//パラメータ（なし）
		try {
			stb.append("SELECT COURSECODE,COURSECODENAME ");
			stb.append("FROM   COURSECODE_MST ");
			stb.append("WHERE  COURSECODE IN "+param[6]+" ");
			stb.append("ORDER BY COURSECODE ");
		} catch( Exception e ){
			log.warn("statementCourseName error!",e);
		}
		return stb.toString();

	}//statementCourseName()の括り


	/**
	 *	生徒別（素点・５科・９科）
	 *
	 *　各個人の素点・合計・平均・席次を抽出。
	 *　席次は、各学年のコース（グループ）内・・・指示画面で指定したコースより抽出。
	 *　５科席次は、５科目全て受験した生徒が対象。９科席次も同様。---NO013
	 */
	private String statementScoreStudent(String param[])
	{
		StringBuffer stb = new StringBuffer();
		//パラメータ（なし）
		try {
			//在籍（クラス）
			stb.append("WITH SCHNO AS ( ");
			stb.append("    SELECT SCHREGNO ");
			stb.append("    FROM   SCHREG_REGD_DAT ");
			stb.append("    WHERE  YEAR='"+param[0]+"' AND SEMESTER='"+param[1]+"' AND ");
			stb.append("           GRADE||HR_CLASS='"+param[8]+"' ) ");
			//在籍（生徒名）
			stb.append(",SCHNO2 AS ( ");
			stb.append("    SELECT W1.SCHREGNO,ATTENDNO,NAME ");
			stb.append("    FROM   SCHREG_REGD_DAT W1,SCHREG_BASE_MST W2 ");
			stb.append("    WHERE  YEAR='"+param[0]+"' AND SEMESTER='"+param[1]+"' AND  ");
			stb.append("           GRADE||HR_CLASS='"+param[8]+"' AND W1.SCHREGNO=W2.SCHREGNO ) ");
			//在籍（コース）
			stb.append(",SCHNO3 AS ( ");
			stb.append("    SELECT SCHREGNO ");
			stb.append("    FROM   SCHREG_REGD_DAT ");
			stb.append("    WHERE  YEAR='"+param[0]+"' AND SEMESTER='"+param[1]+"' AND ");
			stb.append("           GRADE='"+param[10]+"' AND COURSECODE IN "+param[6]+" ) ");
			//異動 NO025Modify
			stb.append(statementTransfer(param));
			stb.append(",SCHNO_I AS ( ");
			stb.append("    SELECT SCHREGNO ");
			stb.append("    FROM   SCHNO ");
			stb.append("    WHERE  SCHREGNO NOT IN (SELECT SCHREGNO FROM IDOU1) AND ");
			stb.append("           SCHREGNO NOT IN (SELECT DISTINCT SCHREGNO FROM IDOU2) ) ");
			stb.append(",SCHNO3_I AS ( ");
			stb.append("    SELECT SCHREGNO ");
			stb.append("    FROM   SCHNO3 ");
			stb.append("    WHERE  SCHREGNO NOT IN (SELECT SCHREGNO FROM IDOU1) AND ");
			stb.append("           SCHREGNO NOT IN (SELECT DISTINCT SCHREGNO FROM IDOU2) ) ");
			//成績および公欠・欠席
			stb.append(",KIN_REC AS ( ");
			stb.append("    SELECT SUBCLASSCD,SCHREGNO, "+param[7]+" AS SCORE ");//---素点・KK・NULL
            if ("1".equals(param[19])) {
                stb.append("    , CLASSCD, SCHOOL_KIND, CURRICULUM_CD ");
            }
//NO027-->
			stb.append("           ,CHAIRCD ");
            if (param[1].equals("1")) 
    			stb.append("       ,SEM1_INTER_REC_DI AS INTER_REC_DI ,SEM1_TERM_REC_DI AS TERM_REC_DI,SEM1_REC_FLG AS REC_FLG ");  //NO030
            if (param[1].equals("2")) 
    			stb.append("       ,SEM2_INTER_REC_DI AS INTER_REC_DI ,SEM2_TERM_REC_DI AS TERM_REC_DI,SEM2_REC_FLG AS REC_FLG ");  //NO030
            if (param[1].equals("3")) 
    			stb.append("       ,'' AS INTER_REC_DI ,'' AS TERM_REC_DI,SEM3_REC_FLG AS REC_FLG ");  //NO030
//NO027<--
			stb.append("    FROM   KIN_RECORD_DAT ");
			stb.append("    WHERE  YEAR='"+param[0]+"' AND SUBSTR(SUBCLASSCD,1,2) <= '09' ) ");//---９科---NO020
			stb.append(",ABSENCE AS ( ");
			stb.append("    SELECT SCHREGNO, 'X5' AS SUBCLASSCD ");
			stb.append("    FROM   KIN_REC ");
			stb.append("    WHERE  SCORE IS NOT NULL AND SUBSTR(SUBCLASSCD,1,2) <= '05' ");
			stb.append("    GROUP BY SCHREGNO HAVING COUNT(*) < 5 ");
			stb.append("    UNION ");
			stb.append("    SELECT SCHREGNO, 'X5' AS SUBCLASSCD ");
			stb.append("    FROM   KIN_REC ");
			stb.append("    WHERE  SCORE IN ('KK') AND SUBSTR(SUBCLASSCD,1,2) <= '05' ");
			stb.append("    GROUP BY SCHREGNO ");
			stb.append("    UNION ");
			stb.append("    SELECT SCHREGNO, 'X9' AS SUBCLASSCD ");
			stb.append("    FROM   KIN_REC ");
			stb.append("    WHERE  SCORE IS NOT NULL AND SUBSTR(SUBCLASSCD,1,2) <= '09' ");
			stb.append("    GROUP BY SCHREGNO HAVING COUNT(*) < 9 ");
			stb.append("    UNION ");
			stb.append("    SELECT SCHREGNO, 'X9' AS SUBCLASSCD ");
			stb.append("    FROM   KIN_REC ");
			stb.append("    WHERE  SCORE IN ('KK') AND SUBSTR(SUBCLASSCD,1,2) <= '09' ");
			stb.append("    GROUP BY SCHREGNO ");
			stb.append("    ) ");
//NO027-->
			stb.append(",TEST_ABS AS ( ");
			stb.append("    SELECT CHAIRCD AS CHACD, ");
			stb.append("           SUM(DISTINCT SMALLINT(TESTKINDCD)) AS TESTSUM ");//2,3---NO028
//			stb.append("           SUM(SMALLINT(TESTKINDCD)) AS TESTSUM ");//2,3
			stb.append("    FROM   SCH_CHR_TEST ");
			stb.append("    WHERE  YEAR = '"+param[0]+"' AND ");
			stb.append("           SEMESTER = '"+param[1]+"' AND SEMESTER < '3' ");
			stb.append("    GROUP BY CHAIRCD ");
			stb.append("    ) ");
//NO027<--
			//素点
			stb.append(",SCORE AS ( ");
			stb.append("    SELECT W1.SUBCLASSCD,W1.SCHREGNO,W1.SCORE ");
//NO030-->
			stb.append("           ,SMALLINT(VALUE(REC_FLG,'0')) AS SCORE_ABS ");
//NO030<--
//NO027-->
//NO027<--
			stb.append("    FROM   SCHNO_I W2,KIN_REC W1 ");
			stb.append("           LEFT JOIN TEST_ABS W3 ON W3.CHACD=W1.CHAIRCD ");
			stb.append("    WHERE  W1.SCHREGNO=W2.SCHREGNO AND W1.SCORE IS NOT NULL ) ");
			//５科合計・平均
			stb.append(",SCH_AVG5 AS ( ");
			stb.append("    SELECT SCHREGNO, SUM(INT(SCORE)) AS SCORE_SUM, ");
			stb.append("           DECIMAL(ROUND(AVG(FLOAT(INT(SCORE)))*10,0)/10,5,1) AS SCORE_AVG ");
			stb.append("    FROM   SCORE ");
			stb.append("    WHERE  SCORE NOT IN ('KK') AND SUBSTR(SUBCLASSCD,1,2) <= '05' ");//---５科
			stb.append("    GROUP BY SCHREGNO ) ");
			//９科合計・平均
			stb.append(",SCH_AVG9 AS ( ");
			stb.append("    SELECT SCHREGNO, SUM(INT(SCORE)) AS SCORE_SUM, ");
			stb.append("           DECIMAL(ROUND(AVG(FLOAT(INT(SCORE)))*10,0)/10,5,1) AS SCORE_AVG ");
			stb.append("    FROM   SCORE ");
			stb.append("    WHERE  SCORE NOT IN ('KK') ");
			stb.append("    GROUP BY SCHREGNO ) ");
			//５科席次 NO024
			stb.append(",SCH_RANK5 AS ( ");
			stb.append("    SELECT SCHREGNO, "+param[17]+"_RANK AS SCORE_RNK ");
			stb.append("    FROM   RECORD_RANK_DAT ");
			stb.append("    WHERE  YEAR = '"+param[0]+"' AND RANK_DIV = '2' ) ");//５科
			//９科席次 NO024
			stb.append(",SCH_RANK9 AS ( ");
			stb.append("    SELECT SCHREGNO, "+param[17]+"_RANK AS SCORE_RNK ");
			stb.append("    FROM   RECORD_RANK_DAT ");
			stb.append("    WHERE  YEAR = '"+param[0]+"' AND RANK_DIV = '3' ) ");//９科
			/*---メイン---*/
			stb.append("SELECT T1.ATTENDNO,T1.NAME, ");	//---出席番号・氏名
//NO027-->
			stb.append("       SUM(CASE WHEN SUBSTR(T2.SUBCLASSCD,1,2) = '01'  ");//---国語
			stb.append("                THEN T2.SCORE_ABS  ");//0,1
			stb.append("                ELSE NULL END) AS SUB1_ABS, ");
			stb.append("       SUM(CASE WHEN SUBSTR(T2.SUBCLASSCD,1,2) = '02'  ");//---社会
			stb.append("                THEN T2.SCORE_ABS  ");//0,1
			stb.append("                ELSE NULL END) AS SUB2_ABS, ");
			stb.append("       SUM(CASE WHEN SUBSTR(T2.SUBCLASSCD,1,2) = '03'  ");//---数学
			stb.append("                THEN T2.SCORE_ABS  ");//0,1
			stb.append("                ELSE NULL END) AS SUB3_ABS, ");
			stb.append("       SUM(CASE WHEN SUBSTR(T2.SUBCLASSCD,1,2) = '04'  ");//---理科
			stb.append("                THEN T2.SCORE_ABS  ");//0,1
			stb.append("                ELSE NULL END) AS SUB4_ABS, ");
			stb.append("       SUM(CASE WHEN SUBSTR(T2.SUBCLASSCD,1,2) = '06'  ");//---音楽
			stb.append("                THEN T2.SCORE_ABS  ");//0,1
			stb.append("                ELSE NULL END) AS SUB5_ABS, ");
			stb.append("       SUM(CASE WHEN SUBSTR(T2.SUBCLASSCD,1,2) = '07'  ");//---美術
			stb.append("                THEN T2.SCORE_ABS  ");//0,1
			stb.append("                ELSE NULL END) AS SUB6_ABS, ");
			stb.append("       SUM(CASE WHEN SUBSTR(T2.SUBCLASSCD,1,2) = '08'  ");//---保体
			stb.append("                THEN T2.SCORE_ABS  ");//0,1
			stb.append("                ELSE NULL END) AS SUB7_ABS, ");
			stb.append("       SUM(CASE WHEN SUBSTR(T2.SUBCLASSCD,1,2) = '09'  ");//---技家
			stb.append("                THEN T2.SCORE_ABS  ");//0,1
			stb.append("                ELSE NULL END) AS SUB8_ABS, ");
			stb.append("       SUM(CASE WHEN SUBSTR(T2.SUBCLASSCD,1,2) = '05'  ");//---英語
			stb.append("                THEN T2.SCORE_ABS  ");//0,1
			stb.append("                ELSE NULL END) AS SUB9_ABS, ");
//NO027<--
			stb.append("       SUM(CASE WHEN SUBSTR(T2.SUBCLASSCD,1,2) = '01'  ");//---国語
			stb.append("                THEN (CASE WHEN T2.SCORE = 'KK' THEN NULL ELSE INT(T2.SCORE) END)  ");
			stb.append("                ELSE NULL END) AS SUB1, ");
			stb.append("       SUM(CASE WHEN SUBSTR(T2.SUBCLASSCD,1,2) = '02'  ");//---社会
			stb.append("                THEN (CASE WHEN T2.SCORE = 'KK' THEN NULL ELSE INT(T2.SCORE) END)  ");
			stb.append("                ELSE NULL END) AS SUB2, ");
			stb.append("       SUM(CASE WHEN SUBSTR(T2.SUBCLASSCD,1,2) = '03'  ");//---数学
			stb.append("                THEN (CASE WHEN T2.SCORE = 'KK' THEN NULL ELSE INT(T2.SCORE) END)  ");
			stb.append("                ELSE NULL END) AS SUB3, ");
			stb.append("       SUM(CASE WHEN SUBSTR(T2.SUBCLASSCD,1,2) = '04'  ");//---理科
			stb.append("                THEN (CASE WHEN T2.SCORE = 'KK' THEN NULL ELSE INT(T2.SCORE) END)  ");
			stb.append("                ELSE NULL END) AS SUB4, ");
			stb.append("       SUM(CASE WHEN SUBSTR(T2.SUBCLASSCD,1,2) = '06'  ");//---音楽
			stb.append("                THEN (CASE WHEN T2.SCORE = 'KK' THEN NULL ELSE INT(T2.SCORE) END)  ");
			stb.append("                ELSE NULL END) AS SUB5, ");
			stb.append("       SUM(CASE WHEN SUBSTR(T2.SUBCLASSCD,1,2) = '07'  ");//---美術
			stb.append("                THEN (CASE WHEN T2.SCORE = 'KK' THEN NULL ELSE INT(T2.SCORE) END)  ");
			stb.append("                ELSE NULL END) AS SUB6, ");
			stb.append("       SUM(CASE WHEN SUBSTR(T2.SUBCLASSCD,1,2) = '08'  ");//---保体
			stb.append("                THEN (CASE WHEN T2.SCORE = 'KK' THEN NULL ELSE INT(T2.SCORE) END)  ");
			stb.append("                ELSE NULL END) AS SUB7, ");
			stb.append("       SUM(CASE WHEN SUBSTR(T2.SUBCLASSCD,1,2) = '09'  ");//---技家
			stb.append("                THEN (CASE WHEN T2.SCORE = 'KK' THEN NULL ELSE INT(T2.SCORE) END)  ");
			stb.append("                ELSE NULL END) AS SUB8, ");
			stb.append("       SUM(CASE WHEN SUBSTR(T2.SUBCLASSCD,1,2) = '05'  ");//---英語
			stb.append("                THEN (CASE WHEN T2.SCORE = 'KK' THEN NULL ELSE INT(T2.SCORE) END)  ");
			stb.append("                ELSE NULL END) AS SUB9, ");
			stb.append("       MAX(T3.SCORE_SUM) AS SUM5, ");	//---５科合計
			stb.append("       MAX(T3.SCORE_AVG) AS AVG5, ");	//---５科平均
			stb.append("       MAX(T4.SCORE_RNK) AS RNK5, ");	//---５科席次
			stb.append("       MAX(T5.SCORE_SUM) AS SUM9, ");	//---９科合計
			stb.append("       MAX(T5.SCORE_AVG) AS AVG9, ");	//---９科平均
			stb.append("       MAX(T6.SCORE_RNK) AS RNK9 ");	//---９科席次
			stb.append("FROM   SCHNO2 T1 ");
			stb.append("       LEFT JOIN SCORE T2 ON T2.SCHREGNO=T1.SCHREGNO ");
			stb.append("       LEFT JOIN SCH_AVG5 T3 ON T3.SCHREGNO=T1.SCHREGNO ");
			stb.append("       LEFT JOIN SCH_RANK5 T4 ON T4.SCHREGNO=T1.SCHREGNO ");
			stb.append("       LEFT JOIN SCH_AVG9 T5 ON T5.SCHREGNO=T1.SCHREGNO ");
			stb.append("       LEFT JOIN SCH_RANK9 T6 ON T6.SCHREGNO=T1.SCHREGNO ");
			stb.append("GROUP BY T1.ATTENDNO,T1.NAME ");
//stb.append("having min(SUBSTR(T2.SUBCLASSCD,1,2)) = '08' ");
			stb.append("ORDER BY T1.ATTENDNO ");
//log.debug(stb.toString());
		} catch( Exception e ){
			log.warn("statementScoreStudent error!",e);
		}
		return stb.toString();

	}//statementScoreStudent()の括り


	/**
	 *	学級合計・学級平均・コース平均（科目別）
	 *
	 *　各科目の学級合計・学級平均・コース平均・朱点を抽出。
	 *　コース平均は、各学年のコース（グループ）内・・・指示画面で指定したコースより抽出。
	 *　朱点は、コース平均の６０％（小数点切り捨て）。
	 */
	private String statementScoreSubject(String param[])
	{
		StringBuffer stb = new StringBuffer();
		//パラメータ（なし）
		try {
            //NO026
			stb.append("WITH CLASS_AVG AS ( ");
			stb.append("    SELECT CLASSCD, CALC_DIV, "+param[17]+" AS SCORE ");
            if ("1".equals(param[19])) {
                stb.append("    , SCHOOL_KIND ");
            }
			stb.append("    FROM   RECORD_CLASS_AVERAGE_DAT ");
			stb.append("    WHERE  YEAR = '"+param[0]+"' AND GRADE||HR_CLASS = '"+param[8]+"' AND CLASSCD <= '09' ");
			stb.append("    ) ");
			stb.append(" ");
			stb.append("SELECT CLASSCD AS SUBCLASSCD ");
			stb.append("       ,MIN(CASE WHEN CALC_DIV = '1' THEN INT(SCORE) END) AS POINT46 ");
			stb.append("       ,MIN(CASE WHEN CALC_DIV = '2' THEN SCORE END) AS POINT47 ");
			stb.append("       ,MIN(CASE WHEN CALC_DIV = '3' THEN SCORE END) AS POINT48 ");
			stb.append("       ,MIN(CASE WHEN CALC_DIV = '3' THEN INT(SCORE*(0.6)) END) AS AKATEN ");
			stb.append("FROM   CLASS_AVG ");
			stb.append("GROUP BY CLASSCD ");
            if ("1".equals(param[19])) {
                stb.append("    , SCHOOL_KIND ");
            }
		} catch( Exception e ){
			log.warn("statementScoreSubject error!",e);
		}
		return stb.toString();

	}//statementScoreSubject()の括り


	/**
	 *	学級合計・学級平均・コース平均（５科・９科）
	 *
	 *　５科・９科の学級合計・学級平均・コース平均を抽出。
	 *　５科は、５科目全て受験した生徒が対象。９科も同様。
	 *　平均の平均は、各個人の平均の合計と人数で算出。
	 */
	private String statementScoreTotal(String param[])
	{
		StringBuffer stb = new StringBuffer();
		//パラメータ（なし）
		try {
            //NO026
			stb.append("WITH CLASS_AVG AS ( ");
			stb.append("    SELECT CLASSCD, CALC_DIV, "+param[17]+" AS SCORE ");
			if ("1".equals(param[19])) {
	            stb.append("    , SCHOOL_KIND ");
			}
			stb.append("    FROM   RECORD_CLASS_AVERAGE_DAT ");
			stb.append("    WHERE  YEAR = '"+param[0]+"' AND GRADE||HR_CLASS = '"+param[8]+"' AND CLASSCD IN('5T','5A','9T','9A') ");
			stb.append("    ) ");
			stb.append(" ");
			stb.append("SELECT SUBSTR(CLASSCD,1,1) AS KEY ");
            if ("1".equals(param[19])) {
                stb.append("    , SCHOOL_KIND ");
            }
			stb.append("       ,MIN(CASE WHEN CLASSCD IN('5T','9T') AND CALC_DIV = '1' THEN INT(SCORE) END) AS SUM46 ");
			stb.append("       ,MIN(CASE WHEN CLASSCD IN('5T','9T') AND CALC_DIV = '2' THEN SCORE END) AS SUM47 ");
			stb.append("       ,MIN(CASE WHEN CLASSCD IN('5T','9T') AND CALC_DIV = '3' THEN SCORE END) AS SUM48 ");
			stb.append("       ,MIN(CASE WHEN CLASSCD IN('5A','9A') AND CALC_DIV = '1' THEN SCORE END) AS AVG46 ");
			stb.append("       ,MIN(CASE WHEN CLASSCD IN('5A','9A') AND CALC_DIV = '2' THEN SCORE END) AS AVG47 ");
			stb.append("       ,MIN(CASE WHEN CLASSCD IN('5A','9A') AND CALC_DIV = '3' THEN SCORE END) AS AVG48 ");
			stb.append("FROM   CLASS_AVG ");
			stb.append("GROUP BY SUBSTR(CLASSCD,1,1) ");
            if ("1".equals(param[19])) {
                stb.append("    , SCHOOL_KIND ");
            }

		} catch( Exception e ){
			log.warn("statementScoreTotal error!",e);
		}
		return stb.toString();

	}//statementScoreTotal()の括り


	/**
	 *	公欠・欠席
	 *
	 *　各個人・各科目の公欠(KK)・欠席(KS)を抽出。
	 *　中間・期末の場合、欠席(KS)なら（欠）、公欠(KK)なら（公）と表示。
	 *　学期成績・学年成績の場合、中間ＯＲ期末＝欠席(KS)なら（欠）、それ以外なら（公）と表示。
	 */
	private String statementScoreAbsence(String param[])
	{
		StringBuffer stb = new StringBuffer();
		//パラメータ（なし）
		try {
			//在籍（クラス）
			stb.append("WITH SCHNO AS ( ");
			stb.append("    SELECT SCHREGNO,ATTENDNO ");
			stb.append("    FROM   SCHREG_REGD_DAT ");
			stb.append("    WHERE  YEAR='"+param[0]+"' AND SEMESTER='"+param[1]+"' AND ");
			stb.append("           GRADE||HR_CLASS='"+param[8]+"' ) ");
			//異動 NO025Modify
            stb.append(statementTransfer(param));
			stb.append(",SCHNO_I AS ( ");
			stb.append("    SELECT SCHREGNO,ATTENDNO ");
			stb.append("    FROM   SCHNO ");
			stb.append("    WHERE  SCHREGNO NOT IN (SELECT SCHREGNO FROM IDOU1) AND ");
			stb.append("           SCHREGNO NOT IN (SELECT DISTINCT SCHREGNO FROM IDOU2) ) ");
			//成績および公欠・欠席
			stb.append(",KIN_REC AS ( ");
			stb.append("    SELECT SUBSTR(SUBCLASSCD,1,2) AS SUBCLASSCD, ");
            if ("1".equals(param[19])) {
                stb.append("    SCHOOL_KIND, ");
            }
			stb.append("           SCHREGNO, "+param[7]+" AS SCORE ");//---素点・KK・NULL
			stb.append("           ,CASE WHEN SEM1_REC IS NULL THEN 0 ELSE 1 END AS SEM1_REC_MARK ");	//NO027
			stb.append("           ,CASE WHEN SEM2_REC IS NULL THEN 0 ELSE 1 END AS SEM2_REC_MARK ");	//NO027
			stb.append("           ,CASE WHEN SEM3_REC IS NULL THEN 0 ELSE 1 END AS SEM3_REC_MARK ");	//NO027
			stb.append("           ,SEM1_TERM_REC_DI,SEM1_INTER_REC_DI ");
			stb.append("           ,SEM2_TERM_REC_DI,SEM2_INTER_REC_DI ");
			stb.append("           ,SEM3_TERM_REC_DI ");
			stb.append("    FROM   KIN_RECORD_DAT ");
			stb.append("    WHERE  YEAR='"+param[0]+"' ");
			if (param[2].equals("01")) stb.append("    AND SUBSTR(SUBCLASSCD,1,2) <= '05' ) ");//---５科---NO011
			else                       stb.append("    AND SUBSTR(SUBCLASSCD,1,2) <= '09' ) ");//---９科---NO011
			stb.append(",ABSENCE AS ( ");
			stb.append("    SELECT SUBCLASSCD,SCHREGNO, "+param[11]+" AS SCORE_DI ");//---（公）（欠）（　）
            if ("1".equals(param[19])) {
                stb.append("    , SCHOOL_KIND ");
            }
			stb.append("    FROM   KIN_REC T1 ");
            // NO023 -->
			stb.append("    WHERE  SCORE IN ('KK') OR ");
			stb.append("           (SCORE IS NULL AND ");
			stb.append("            NOT EXISTS(SELECT  'X' FROM KIN_REC T2 ");
			stb.append("                       WHERE   T2.SCHREGNO = T1.SCHREGNO AND ");
			stb.append("                               SUBSTR(T2.SUBCLASSCD,1,2) = SUBSTR(T1.SUBCLASSCD,1,2) AND ");
            if ("1".equals(param[19])) {
                stb.append("    T1.SCHOOL_KIND = T1.SCHOOL_KIND AND ");
            }
            stb.append("                               SCORE IS NOT NULL))) ");
			// <-- NO023

			/*---メイン---*/
			stb.append("SELECT T2.ATTENDNO,T1.SUBCLASSCD,SCORE_DI,T1.SCHREGNO ");
            if ("1".equals(param[19])) {
                stb.append("    , T1.SCHOOL_KIND ");
            }
			stb.append("FROM   ABSENCE T1,SCHNO_I T2 ");
			stb.append("WHERE  T1.SCHREGNO=T2.SCHREGNO ");
			stb.append("ORDER BY T2.ATTENDNO ");
		} catch( Exception e ){
			log.warn("statementScoreAbsence error!",e);
		}
		return stb.toString();

	}//statementScoreAbsence()の括り


	/**
	 *	異動
	 *
	 *　退学・転学・休学・留学・編入を抽出。
	 */
	private String statementScoreTransfer(String param[])
	{
		StringBuffer stb = new StringBuffer();
		//パラメータ（なし）
		try {
			//在籍（クラス）
			stb.append("WITH SCHNO AS ( ");
			stb.append("    SELECT SCHREGNO,ATTENDNO ");
			stb.append("    FROM   SCHREG_REGD_DAT ");
			stb.append("    WHERE  YEAR='"+param[0]+"' AND SEMESTER='"+param[1]+"' AND ");
			stb.append("           GRADE||HR_CLASS='"+param[8]+"' ) ");
			//異動 NO025Modify
			stb.append(",IDOU1 AS ( ");
			stb.append("    SELECT SCHREGNO,GRD_DATE AS IDOU_DATE, ");
			stb.append("           (SELECT NAME1 FROM NAME_MST ");
			stb.append("            WHERE  NAMECD1='A003' AND NAMECD2=GRD_DIV) AS IDOU_NAME ");
			stb.append("    FROM   SCHREG_BASE_MST ");
			stb.append("    WHERE  GRD_DIV IN ('2','3') AND GRD_DATE < ");//転学(2)・退学(3)
			stb.append("           CASE WHEN '"+param[18]+"' < '"+param[12]+"' THEN '"+param[18]+"' ");
			stb.append("                ELSE '"+param[12]+"' END ");
			stb.append("    UNION ");
			stb.append("    SELECT SCHREGNO,ENT_DATE AS IDOU_DATE, ");
			stb.append("           (SELECT NAME1 FROM NAME_MST ");
			stb.append("            WHERE  NAMECD1='A002' AND NAMECD2=ENT_DIV) AS IDOU_NAME ");
			stb.append("    FROM   SCHREG_BASE_MST ");
			stb.append("    WHERE  ENT_DIV IN ('4','5') AND ENT_DATE > ");//転入(4)・編入(5)
			stb.append("           CASE WHEN '"+param[18]+"' < '"+param[12]+"' THEN '"+param[18]+"' ");
			stb.append("                ELSE '"+param[12]+"' END ) ");
			stb.append(",IDOU2 AS ( ");
			stb.append("    SELECT SCHREGNO,TRANSFER_SDATE AS IDOU_DATE, ");
			stb.append("           (SELECT NAME1 FROM NAME_MST ");
			stb.append("            WHERE  NAMECD1='A004' AND NAMECD2=TRANSFERCD) AS IDOU_NAME ");
			stb.append("    FROM   SCHREG_TRANSFER_DAT ");
			stb.append("    WHERE  TRANSFERCD IN ('1','2') AND  ");//留学(1)・休学(2)
			stb.append("           CASE WHEN '"+param[18]+"' < '"+param[12]+"' THEN '"+param[18]+"' ");
			stb.append("                ELSE '"+param[12]+"' END ");
			stb.append("           BETWEEN TRANSFER_SDATE AND TRANSFER_EDATE AND ");
			stb.append("           SCHREGNO NOT IN (SELECT SCHREGNO FROM IDOU1) ) ");
			/*---メイン---*/
			stb.append("SELECT T1.SCHREGNO,ATTENDNO,IDOU_DATE,IDOU_NAME ");//NO025
			stb.append("FROM   SCHNO T1,IDOU1 T2 ");
			stb.append("WHERE  T1.SCHREGNO=T2.SCHREGNO ");
			stb.append("UNION ALL ");
			stb.append("SELECT T1.SCHREGNO,ATTENDNO,IDOU_DATE,IDOU_NAME ");//NO025
			stb.append("FROM   SCHNO T1,IDOU2 T2 ");
			stb.append("WHERE  T1.SCHREGNO=T2.SCHREGNO ");
			stb.append("ORDER BY ATTENDNO ");
		} catch( Exception e ){
			log.warn("statementScoreTransfer error!",e);
		}
		return stb.toString();

	}//statementScoreTransfer()の括り


	/**
	 *	Ｆ表示有り／無し
	 *
	 *　Ｆ表示：附属小学校からの入学者---NO007
	 */
	private String statementFinschoolStudent(String param[])
	{
		StringBuffer stb = new StringBuffer();
		//パラメータ（なし）
		try {
			//在籍（附属）
			stb.append("WITH SCHNO_FIN AS ( ");
			stb.append("    SELECT W1.SCHREGNO,ATTENDNO,NAME,FINSCHOOLCD ");
			stb.append("    FROM   SCHREG_REGD_DAT W1,SCHREG_BASE_MST W2 ");
			stb.append("    WHERE  YEAR='"+param[0]+"' AND SEMESTER='"+param[1]+"' AND ");
			stb.append("           FINSCHOOLCD='0001010' AND ");//附属小学校---NO031
			stb.append("           GRADE||HR_CLASS='"+param[8]+"' AND W1.SCHREGNO=W2.SCHREGNO ) ");
			/*---メイン---*/
			stb.append("SELECT ATTENDNO,'F' AS FIN ");
			stb.append("FROM   SCHNO_FIN ");
			stb.append("ORDER BY ATTENDNO ");
		} catch( Exception e ){
			log.warn("statementFinschoolStudent error!",e);
		}
		return stb.toString();

	}//statementFinschoolStudent()の括り


	/**
	 *	コースグループ
	 *
	 *　コースグループからコースを取得---NO019
	 */
	private String statementCourseGroup(String param[])
	{
		StringBuffer stb = new StringBuffer();
		try {
			//在籍（クラス）
			stb.append("WITH SCHNO AS ( ");
			stb.append("    SELECT DISTINCT COURSECODE FROM SCHREG_REGD_DAT ");
			stb.append("    WHERE  YEAR='"+param[0]+"' AND SEMESTER='"+param[1]+"' AND GRADE||HR_CLASS='"+param[8]+"' ) ");
			//コースグループ
			stb.append(",CRS_GRP AS ( ");
			stb.append("    SELECT COURSECODE,COURSE_SEQ FROM COURSE_GROUP_DAT ");//---NO022
			stb.append("    WHERE  YEAR='"+param[0]+"' AND GRADE='"+param[10]+"' ) ");
			//メイン
			stb.append("SELECT COURSECODE FROM CRS_GRP ");
			stb.append("WHERE  COURSE_SEQ IN ( SELECT COURSE_SEQ FROM CRS_GRP ");
			stb.append("                       WHERE  COURSECODE IN (SELECT COURSECODE FROM SCHNO) ) ");
		} catch( Exception e ){
			log.warn("statementCourseGroup error!",e);
		}
		return stb.toString();

	}//statementCourseGroup()の括り


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
			//異動 NO025Modify
			stb.append(",IDOU1 AS ( ");
			stb.append("    SELECT SCHREGNO ");
			stb.append("    FROM   SCHREG_BASE_MST ");
			stb.append("    WHERE  GRD_DIV IN ('2','3') AND GRD_DATE < ");
			stb.append("           CASE WHEN '"+param[18]+"' < '"+param[12]+"' THEN '"+param[18]+"' ");
			stb.append("                ELSE '"+param[12]+"' END ");
			stb.append("    UNION ");
			stb.append("    SELECT SCHREGNO ");
			stb.append("    FROM   SCHREG_BASE_MST ");
			stb.append("    WHERE  ENT_DIV IN ('4','5') AND ENT_DATE > ");
			stb.append("           CASE WHEN '"+param[18]+"' < '"+param[12]+"' THEN '"+param[18]+"' ");
			stb.append("                ELSE '"+param[12]+"' END ) ");
			stb.append(",IDOU2 AS ( ");
			stb.append("    SELECT SCHREGNO ");
			stb.append("    FROM   SCHREG_TRANSFER_DAT ");
			stb.append("    WHERE  TRANSFERCD IN ('1','2') AND  ");
			stb.append("           CASE WHEN '"+param[18]+"' < '"+param[12]+"' THEN '"+param[18]+"' ");
			stb.append("                ELSE '"+param[12]+"' END ");
			stb.append("           BETWEEN TRANSFER_SDATE AND TRANSFER_EDATE AND ");
			stb.append("           SCHREGNO NOT IN (SELECT SCHREGNO FROM IDOU1) ) ");
		} catch( Exception e ){
			log.warn("statementTransfer error!",e);
		}
		return stb.toString();

	}//statementTransfer()の括り



}//クラスの括り
