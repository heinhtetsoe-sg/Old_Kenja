/**
 *
 *	学校教育システム 賢者 [成績管理]
 *
 *					＜ＫＮＪＤ３２３＞  学級別平均点一覧（学年別・試験別・クラス別）
 *
 *	2005/03/28 nakamoto 作成日
 *	2005/05/09 nakamoto 学年成績は、学期名称の表記はいらない。---NO001
 *	           nakamoto 今学期のパラメータを追加。---NO002
 *	           nakamoto 絶対評価と相対評価を追加。---NO003
 *	2005/05/12 nakamoto 素点(中間・期末)以外を出力する場合、公欠・欠席は見ない。---NO004
 *	2005/05/19 nakamoto 作成日を追加。---NO006
 *	           nakamoto 退学・転学について、異動日が3月31日だったら、3月31日は在籍とする。---NO007
 *	2005/05/27 nakamoto 2J01クラスのコース平均点が違う不具合を修正。---NO008
 *
 *	2005/06/13 nakamoto コースグループ設定で設定したテーブル(COURSE_GROUP)を参照---NO009
 *	           nakamoto 科目コードでの条件を教科コードに変更---NO010
 *	2005/06/14 nakamoto 科目表示順序は、国語、社会、数学、理科、英語、5教科の順とする---NO011
 *	2005/06/20 nakamoto 担当者欄には、その科目に複数講座がある場合は全担当教員を表示する(講座コードの若い順)---NO012
 *	2005/07/06 nakamoto テーブル名変更による修正(COURSE_GROUP⇒COURSE_GROUP_DAT)---NO022
 ***********************************************************************************************
 *  2005/10/26 nakamoto 編入のデータ仕様変更および在籍異動条件に転入学を追加---NO025
 *	2005/12/13 nakamoto NO026:(クラス平均・コース平均)は、テーブル(RECORD_CLASS_AVERAGE_DAT)から参照するように修正
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


public class KNJD323K {


    private static final Log log = LogFactory.getLog(KNJD323K.class);

	/*----------------------------*
	 * HTTP Get リクエストの処理  *
	 *----------------------------*/
	public void svf_out(HttpServletRequest request, HttpServletResponse response)
	                 throws ServletException, IOException
	{

		Vrw32alp svf = new Vrw32alp(); 			//PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
		DB2UDB db2 = null;						//Databaseクラスを継承したクラス
		String param[] = new String[8];//NO026

	//	パラメータの取得
		String classcd[] = request.getParameterValues("GRADE");   			//学年
		try {
	        param[0] = request.getParameter("YEAR");         				//年度
			param[1] = request.getParameter("GAKKI");   					//学期 1,2,3,9:学年平均
			param[2] = request.getParameter("TESTKINDCD");   				//01:中間,02:期末,0:学期平均,9:学年平均,90:絶対評価(学年評定),91:相対評価(５段階),92:相対評価(１０段階)
			String idobi = request.getParameter("DATE");   					//異動対象日付
			param[4] = idobi.replace('/','-');
        	//学年末の場合、今学期をセット---NO002
			if (param[1].equals("9")) param[1] = request.getParameter("CTRL_SEME");
			param[7] = request.getParameter("useCurriculumcd");
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
		PreparedStatement ps1 = null;
		boolean nonedata = false; 								//該当データなしフラグ
		printHeader(db2,svf,param);								//見出し出力のメソッド
		setParamAbsence(param);									//公欠・欠席パラメータのメソッド
for(int ia=0 ; ia<param.length ; ia++) log.debug("param["+ia+"]="+param[ia]);
		//SQL作成
		try {
			ps1 = db2.prepareStatement(statementAverage(param));		//平均点preparestatement
//log.debug("ps1 = "+statementAverage(param));
		} catch( Exception ex ) {
			log.warn("DB2 open error!");
		}
		//SVF出力
		for( int ia=0 ; ia<classcd.length ; ia++ ){
log.debug("start! "+classcd[ia]);
			if( printMain(db2,svf,param,classcd[ia],ps1) ) nonedata = true;		//帳票出力のメソッド
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
		statementClose(ps1);		//preparestatementを閉じる
		db2.commit();
		db2.close();				//DBを閉じる
		outstrm.close();			//ストリームを閉じる 

    }//doGetの括り



	/** SVF-FORM **/
	private void printHeader(DB2UDB db2,Vrw32alp svf,String param[]){

		KNJ_Get_Info getinfo = new KNJ_Get_Info();
		KNJ_Get_Info.ReturnVal returnval = null;
		int ret = 0;
		ret = svf.VrSetForm("KNJD323.frm", 4);
		ret = svf.VrsOut("NENDO",nao_package.KenjaProperties.gengou(Integer.parseInt(param[0])) + "年度");//年度

	//	ＳＶＦ属性変更--->学年毎に改ページ
		ret = svf.VrAttribute("GRADE","FF=1");

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
			if (param[2].equals("01") || param[2].equals("02") || param[2].equals("0")) 
                ret = svf.VrsOut("SEMESTER"	,returnval.val1);//---NO001
		} catch( Exception e ){
			log.warn("Semester name get error!");
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
		ret = svf.VrsOut("TESTNAME"	,stb);
	//	定期試験フィールド名の取得---NO026
		if (param[2].equals("01")) param[6] = "SEM"+param[1]+"_INTER_REC";
		if (param[2].equals("02")) param[6] = "SEM"+param[1]+"_TERM_REC";
		if (param[2].equals("0"))  param[6] = "SEM"+param[1]+"_REC";
		if (param[2].equals("9"))  param[6] = "GRADE_RECORD";
		if (param[2].equals("90")) param[6] = "GRADE_ASSESS";
		if (param[2].equals("91")) param[6] = "GRADE3_RELAASSESS_5STEP";
		if (param[2].equals("92")) param[6] = "GRADE3_RELAASSESS_10STEP";

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
			param[5] = "CASE WHEN "+param[3]+" IS NULL AND "+param[3]+"_DI IN('KK','KS') THEN 'KK' "
					 + "     WHEN "+param[3]+" IS NOT NULL THEN RTRIM(CHAR("+param[3]+")) "
					 + "     ELSE NULL END";
		//学期成績
		} else if (param[2].equals("0")) {
			//３学期
			if (2 < Integer.parseInt(param[1])) {
				param[5] = "CASE WHEN SEM3_REC IS NOT NULL THEN RTRIM(CHAR(SEM3_REC)) "
						 + "     ELSE NULL END";
			//２学期
			} else if (1 < Integer.parseInt(param[1])) {
				param[5] = "CASE WHEN SEM2_REC IS NOT NULL THEN RTRIM(CHAR(SEM2_REC)) "
						 + "     ELSE NULL END";
			//１学期
			} else {
				param[5] = "CASE WHEN SEM1_REC IS NOT NULL THEN RTRIM(CHAR(SEM1_REC)) "
						 + "     ELSE NULL END";
			}
		//学年成績
		} else {
			param[5] = "CASE "+param[3]+" "//---NO003
					 + "     ELSE NULL END";
		}

	}//setParamAbsence()の括り


	/**SVF-FORM**/
	private boolean printMain(DB2UDB db2,Vrw32alp svf,String param[],String classcd,PreparedStatement ps1)
	{

		boolean nonedata = false;
		int ret = 0;
		try {
			int pp = 0;
			ps1.setString(++pp,classcd);	//学年
			ps1.setString(++pp,classcd);	//学年---NO012
			ps1.setString(++pp,classcd);	//学年
			ps1.setString(++pp,classcd);	//学年---NO026
			ResultSet rs = ps1.executeQuery();

			ret = svf.VrsOut("GRADE" 	, "第"+String.valueOf(Integer.parseInt(classcd))+"学年" );//改ページ

			String hr_class = "0";
			String gyo = "0";
            //---NO012---↓---
            int staffno = 0;        //担当者欄の行数
			String subclasscd = "0";//教科
			String staffcd = "0";   //職員
			String staffname = "0"; //職員名
            //---NO012---↑---
			//---01:国語,02:社会,03:数学,04:理科,05:英語
			while( rs.next() ){
				//クラスのブレイク時、１列出力
				if( !hr_class.equals("0") && !hr_class.equals(rs.getString("HR_CLASS")) ) ret = svf.VrEndRecord();
				//クラス
				ret = svf.VrsOut("HR_CLASS" 	, rs.getString("HR_NAMEABBV") );
				//各教科---NO010---NO011
				if( rs.getString("SUBCLASSCD").equals("01") ) gyo = "1";		//国語
				if( rs.getString("SUBCLASSCD").equals("02") ) gyo = "2";		//社会
				if( rs.getString("SUBCLASSCD").equals("03") ) gyo = "3";		//数学
				if( rs.getString("SUBCLASSCD").equals("04") ) gyo = "4";		//理科
				if( rs.getString("SUBCLASSCD").equals("05") ) gyo = "5";		//英語

                //担当者欄(複数表示対応)---NO012---↓---
                //教科・クラスが同じで、職員が違う場合
                if( subclasscd.equals(rs.getString("SUBCLASSCD")) && 
                    hr_class.equals(rs.getString("HR_CLASS")) && 
                    !staffcd.equals(rs.getString("STAFFCD")) ){
                    staffno++;      //担当者カウント
                } else {
                    staffno = 1;    //初期値
                }
                //１名⇒２行目(真中)に表示。複数名⇒１行目から表示
                if( staffno > 3 ){
log.debug("staffno = "+staffno);
log.debug("staffname = "+staffname);
log.debug("STAFFNAME = "+rs.getString("STAFFNAME"));
                }
                if( staffno > 1 ){
                    if( staffno == 2 ) ret = svf.VrsOut("STAFFNAME" +gyo+"_1"	, staffname );
    				ret = svf.VrsOut("STAFFNAME" +gyo+"_"+String.valueOf(staffno)	, rs.getString("STAFFNAME") );
                } else {
    				ret = svf.VrsOut("STAFFNAME" +gyo+"_2"	, rs.getString("STAFFNAME") );
                }
//				ret = svf.VrsOut("STAFFNAME" +gyo	, rs.getString("STAFFNAME") );
                //---NO012---↑---

				ret = svf.VrsOut("AVE_CLASS" +gyo	, rs.getString("AVG_CLASS") );
				ret = svf.VrsOut("AVE_COURSE"+gyo	, rs.getString("AVG_COURSE") );
				ret = svf.VrsOut("COURSECD"  +gyo	, rs.getString("COURSECODE") );
				//５教科
				ret = svf.VrsOut("AVE_CLASS6" 		, rs.getString("AVG_CLASS5") );
				ret = svf.VrsOut("AVE_COURSE6" 		, rs.getString("AVG_COURSE5") );
				ret = svf.VrsOut("COURSECD6" 		, rs.getString("COURSECODE") );

				hr_class = rs.getString("HR_CLASS");
                //---NO012---↓---
				staffcd = rs.getString("STAFFCD");
				staffname = rs.getString("STAFFNAME");
                subclasscd = rs.getString("SUBCLASSCD");
                //---NO012---↑---
				nonedata = true;
			}
			//最終列出力
			if( nonedata ) ret = svf.VrEndRecord();
			rs.close();
			db2.commit();
		} catch( Exception ex ) {
			log.warn("printMain read error!", ex);
		}
		return nonedata;

	}//printMain()の括り



	/**PrepareStatement作成**/
	private String statementAverage(String param[])
	{
		StringBuffer stb = new StringBuffer();
		//パラメータ（学年２つ）
		try {
				//在籍
			stb.append("WITH SCHNO AS ( ");
			stb.append("    SELECT W1.SCHREGNO,W1.HR_CLASS,COURSE_SEQ AS COURSECODE ");
			stb.append("    FROM   SCHREG_REGD_DAT W1, COURSE_GROUP_DAT W2,SEMESTER_MST T1 ");//---NO022---NO025
			stb.append("    WHERE  W1.YEAR='"+param[0]+"' AND W1.SEMESTER='"+param[1]+"' AND W1.GRADE=? AND ");
			stb.append("           W1.YEAR=W2.YEAR AND W1.GRADE=W2.GRADE AND W1.COURSECODE=W2.COURSECODE AND ");
	        stb.append(	  	       "W1.YEAR = T1.YEAR AND ");
	        stb.append(	  	       "W1.SEMESTER = T1.SEMESTER AND ");
	        stb.append(            "NOT EXISTS( SELECT 'X' FROM SCHREG_BASE_MST S1 ");
	        stb.append(                        "WHERE  S1.SCHREGNO = W1.SCHREGNO AND ");
	        stb.append(                              "((S1.GRD_DIV IN ('2','3') AND ");
	        stb.append(                                "S1.GRD_DATE < CASE WHEN T1.EDATE < '"+param[4]+"' THEN T1.EDATE ELSE '"+param[4]+"' END) OR ");
	        stb.append(                               "(S1.ENT_DIV IN ('4','5') AND ");
	        stb.append(                                "S1.ENT_DATE > CASE WHEN T1.EDATE < '"+param[4]+"' THEN T1.EDATE ELSE '"+param[4]+"' END)) ) AND ");
	        stb.append(            "NOT EXISTS( SELECT 'X' FROM SCHREG_TRANSFER_DAT S1 ");
	        stb.append(                        "WHERE  S1.SCHREGNO = W1.SCHREGNO AND ");
	        stb.append(                               "(S1.TRANSFERCD IN ('1','2') AND ");
	        stb.append(                                "CASE WHEN T1.EDATE < '"+param[4]+"' THEN T1.EDATE ELSE '"+param[4]+"' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE)) ) ");
			//成績および公欠・欠席
			stb.append(",KIN_REC AS ( ");
			stb.append("    SELECT SUBSTR(SUBCLASSCD,1,2) AS SUBCLASSCD,SCHREGNO,CHAIRCD, "+param[5]+" AS SCORE ");//---素点・KK・NULL
			if ("1".equals(param[7])) {
	            stb.append(", CLASSCD, SCHOOL_KIND, CURRICULUM_CD ");
			}
			stb.append("           ,SUBCLASSCD AS SUBCLASSCD2 ");//---NO012Add
			stb.append("    FROM   KIN_RECORD_DAT ");
			stb.append("    WHERE  YEAR='"+param[0]+"' AND ");
			stb.append("           SUBSTR(SUBCLASSCD,1,2) <= '05' ) ");//---国語,社会,数学,理科,英語
			stb.append(",SCORE_I AS ( ");
			stb.append("    SELECT SUBCLASSCD,SCHREGNO,CHAIRCD, INT(SCORE) AS REC_AVG ");//---成績
			stb.append("           ,SUBCLASSCD2 ");//---NO012Add
            if ("1".equals(param[7])) {
                stb.append(", CLASSCD, SCHOOL_KIND, CURRICULUM_CD ");
            }
			stb.append("    FROM   KIN_REC ");
			stb.append("    WHERE  SCORE NOT IN ('KK') AND SCORE IS NOT NULL ) ");
			stb.append(",SCORE_AVG AS ( ");
			stb.append("    SELECT SCHREGNO, ");
			stb.append("           SUM(REC_AVG) AS REC_AVG ");//---個人合計---NO008
			//stb.append("           DECIMAL(ROUND(AVG(FLOAT(REC_AVG))*10,0)/10,4,1) AS REC_AVG ");//---個人平均
			stb.append("    FROM   SCORE_I ");
			stb.append("    GROUP BY SCHREGNO ");
			stb.append("    HAVING 5 <= COUNT(*) ) ");//---５科目全て受験---NO010
//			stb.append("    HAVING COUNT(*) = 5 ) ");//---５科目全て受験
				//担当者---NO012Add
			stb.append(",STAFF_LIST AS ( ");
			stb.append("    SELECT DISTINCT SUBSTR(W1.SUBCLASSCD,1,2) AS SUBCLASSCD,W1.CHAIRCD, ");
            if ("1".equals(param[7])) {
                stb.append(" CLASSCD, SCHOOL_KIND, CURRICULUM_CD, ");
            }
			stb.append("           W2.TRGTCLASS,W3.STAFFCD ");
			stb.append("    FROM   CHAIR_DAT W1,CHAIR_CLS_DAT W2,CHAIR_STF_DAT W3 ");
			stb.append("    WHERE  W1.YEAR='"+param[0]+"' AND W1.SEMESTER='"+param[1]+"' AND ");
			stb.append("           W1.SUBCLASSCD IN (SELECT DISTINCT SUBCLASSCD2 FROM SCORE_I) AND ");
			stb.append("           W2.YEAR=W1.YEAR AND W2.SEMESTER=W1.SEMESTER AND ");
			stb.append("           W2.GROUPCD=W1.GROUPCD AND ");
			stb.append("           (W2.CHAIRCD=W1.CHAIRCD OR W2.CHAIRCD='0000000') AND ");
			stb.append("           W2.TRGTGRADE=? AND ");
			stb.append("           W3.YEAR=W1.YEAR AND W3.SEMESTER=W1.SEMESTER AND ");
			stb.append("           W3.CHAIRCD=W1.CHAIRCD ) ");
			stb.append(",STAFF_NAME AS ( ");
			stb.append("    SELECT K1.CHAIRCD,K1.TRGTCLASS AS HR_CLASS,K1.SUBCLASSCD, ");
            if ("1".equals(param[7])) {
                stb.append(" CLASSCD, SCHOOL_KIND, CURRICULUM_CD, ");
            }
			stb.append("           K3.STAFFCD,K3.STAFFNAME_SHOW AS STAFFNAME ");
			stb.append("    FROM   STAFF_LIST K1,STAFF_MST K3 ");
			stb.append("    WHERE  K3.STAFFCD=K1.STAFFCD ) ");
				//クラス・コース
			stb.append(",CLS_COS AS ( ");
			stb.append("    SELECT DISTINCT W1.HR_CLASS,W1.COURSECODE,W2.HR_NAMEABBV ");
			stb.append("    FROM   SCHNO W1,SCHREG_REGD_HDAT W2 ");
			stb.append("    WHERE  W2.YEAR='"+param[0]+"' AND W2.SEMESTER='"+param[1]+"' AND W2.GRADE=? AND ");
			stb.append("           W1.HR_CLASS=W2.HR_CLASS ) ");
			stb.append(",CLASS_AVG AS ( ");
			stb.append("    SELECT HR_CLASS, CLASSCD, CALC_DIV, "+param[6]+" AS SCORE ");
            if ("1".equals(param[7])) {
                stb.append(", SCHOOL_KIND ");
            }
			stb.append("    FROM   RECORD_CLASS_AVERAGE_DAT ");
			stb.append("    WHERE  YEAR = '"+param[0]+"' AND GRADE = ? AND ");
			stb.append("           CLASSCD IN('01','02','03','04','05','5A') AND CALC_DIV IN('2','3') ");
			stb.append("           AND "+param[6]+" IS NOT NULL ) ");
			stb.append(",CLASS_AVG1 AS ( ");
			stb.append("    SELECT HR_CLASS, CLASSCD AS SUBCLASSCD ");
            if ("1".equals(param[7])) {
                stb.append(", SCHOOL_KIND ");
            }
			stb.append("           ,MIN(CASE WHEN CALC_DIV = '2' THEN SCORE END) AS AVG_CLASS ");   //各教科クラス平均
			stb.append("           ,MIN(CASE WHEN CALC_DIV = '3' THEN SCORE END) AS AVG_COURSE ");  //各教科コース平均
			stb.append("    FROM   CLASS_AVG ");
			stb.append("    WHERE  CLASSCD IN('01','02','03','04','05') ");
			stb.append("    GROUP BY HR_CLASS, CLASSCD ");
            if ("1".equals(param[7])) {
                stb.append(", SCHOOL_KIND ");
            }
			stb.append("          ) ");
			stb.append(",CLASS_AVG2 AS ( ");
			stb.append("    SELECT HR_CLASS, CLASSCD AS SUBCLASSCD ");
            if ("1".equals(param[7])) {
                stb.append(", SCHOOL_KIND ");
            }
			stb.append("           ,MIN(CASE WHEN CALC_DIV = '2' THEN SCORE END) AS AVG_CLASS5 ");  //５教科クラス平均
			stb.append("           ,MIN(CASE WHEN CALC_DIV = '3' THEN SCORE END) AS AVG_COURSE5 "); //５教科コース平均
			stb.append("    FROM   CLASS_AVG ");
			stb.append("    WHERE  CLASSCD IN('5A') ");
			stb.append("    GROUP BY HR_CLASS, CLASSCD ");
            if ("1".equals(param[7])) {
                stb.append(", SCHOOL_KIND ");
            }
			stb.append("          ) ");
			stb.append(",CLASS_AVG3 AS ( ");
			stb.append("    SELECT W1.HR_CLASS, W1.SUBCLASSCD, ");
            if ("1".equals(param[7])) {
                stb.append(" W1.SCHOOL_KIND, ");
            }
			stb.append("           W1.AVG_CLASS, W1.AVG_COURSE, W2.AVG_CLASS5, W2.AVG_COURSE5 ");
			stb.append("    FROM   CLASS_AVG1 W1, CLASS_AVG2 W2 ");
			stb.append("    WHERE  W1.HR_CLASS = W2.HR_CLASS ");
            if ("1".equals(param[7])) {
                stb.append(" AND W1.SCHOOL_KIND = W2.SCHOOL_KIND ");
            }
			stb.append("         ) ");

				//メイン
			stb.append("SELECT T1.HR_CLASS,T2.SUBCLASSCD,T1.COURSECODE,T1.HR_NAMEABBV,T6.STAFFNAME, ");
            if ("1".equals(param[7])) {
                stb.append(" T2.SCHOOL_KIND, ");
            }
			stb.append("       T6.CHAIRCD,T6.STAFFCD, ");//---NO012Add
			stb.append("       T2.AVG_CLASS, T2.AVG_COURSE, T2.AVG_CLASS5, T2.AVG_COURSE5 ");
			stb.append("FROM   CLS_COS T1, CLASS_AVG3 T2 ");
			stb.append("       LEFT JOIN STAFF_NAME T6 ON T2.HR_CLASS=T6.HR_CLASS AND  ");
			stb.append("                                  T2.SUBCLASSCD=T6.SUBCLASSCD ");
            if ("1".equals(param[7])) {
                stb.append(" AND T2.SCHOOL_KIND = T6.SCHOOL_KIND ");
            }
			stb.append("WHERE  T1.HR_CLASS=T2.HR_CLASS ");
			stb.append("ORDER BY T1.HR_CLASS,");
            if ("1".equals(param[7])) {
                stb.append(" T2.SCHOOL_KIND, ");
            }
			stb.append("         T2.SUBCLASSCD,T6.CHAIRCD ");
		} catch( Exception e ){
			log.warn("statementAverage error!");
		}
		return stb.toString();

	}//statementAverage()の括り



	/**PrepareStatement close**/
	private void statementClose(PreparedStatement ps1)
	{
		try {
			ps1.close();
		} catch( Exception e ){
			log.warn("statementClose error!");
		}
	}//statementClose()の括り



}//クラスの括り
