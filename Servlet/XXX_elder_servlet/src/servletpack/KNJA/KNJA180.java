package servletpack.KNJA;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJ_Control;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_Semester;
import servletpack.KNJZ.detail.KNJ_V_Course;

/**
 *
 *	学校教育システム 賢者 [学籍管理]
 *
 *                     ＜ＫＮＪＡ１８０＞ 生徒基本データ(コース別)
 *
 * 2005/01/28 nakamoto 作成（東京都）
 * 2005/12/18 m-yama   NO001 SCHREG_BASE_DAT、SCHREG_ADDRESS_DAT修正に伴う修正
 **/

public class KNJA180 {

    private static final Log log = LogFactory.getLog("KNJA180.class");

	private Vrw32alp svf = new Vrw32alp(); 	//PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
	private DB2UDB db2;						// Databaseクラスを継承したクラス
	boolean nonedata; 				//該当データなしフラグ
	private String pyear,psemester,pgrade_hr_class,pschregno,date1,date2,pcourse_major,pcoursecode,tel_flg;

	/*----------------------------*
	 * HTTP Get リクエストの処理  *
	 *----------------------------*/
	public void svf_out(HttpServletRequest request, HttpServletResponse response)
	                 throws ServletException, IOException
	{
		// パラメータの取得
		try {
	        pyear = request.getParameter("YEAR");         				//年度
			psemester = request.getParameter("GAKKI");   				//学期
	        pcourse_major = request.getParameter("COURSE_MAJOR_NAME");  //課程学科
			pcoursecode = request.getParameter("COURSECODE");   		//コース
			tel_flg = request.getParameter("TEL");   		//電話番号出力フラグ

			//学年＋組
			String c_select[] = request.getParameterValues("CLASS_SELECTED");   //学年＋組
			pgrade_hr_class = "(";
			for(int ia=0 ; ia< c_select.length ; ia++){
				if(c_select[ia] == null ) break;
				if(ia > 0) pgrade_hr_class = pgrade_hr_class + ",";
				pgrade_hr_class = pgrade_hr_class + "'" + c_select[ia] + "'";
			}
			pgrade_hr_class = pgrade_hr_class + ")";

		} catch (Exception ex) {
			log.error("[KNJA180]parameter error!", ex);
		}
		log.debug("[KNJA180]pyear=" + pyear);
		log.debug("[KNJA180]psemester=" + psemester);
		log.debug("[KNJA180]pcourse_major=" + pcourse_major);
		log.debug("[KNJA180]pcoursecode=" + pcoursecode);
		log.debug("[KNJA180]pgrade_hr_class=" + pgrade_hr_class);


		// print設定
		response.setContentType("application/pdf");
		OutputStream outstrm = response.getOutputStream();

		// svf設定
		svf.VrInit();								//クラスの初期化
		svf.VrSetSpoolFileStream(outstrm);   		//PDFファイル名の設定
		svf.VrSetForm("KNJA180.frm", 4);	 	  	//SuperVisualFormadeで設計したレイアウト定義態の設定

		// ＤＢ接続
		db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
		try {
			db2.open();
		} catch (Exception ex) {
			log.error("[KNJA180]DB2 open error!", ex);
		}

		printHeader();
		setdate();
		setpschregno();

		printStudent();
		db2.close();		// DBを閉じる

		log.debug("[KNJA180]nonedata="+nonedata);
		/*該当データ無し*/
		if(nonedata == false){
			svf.VrSetForm("MES001.frm", 0);
			svf.VrsOut("note" , "note");
			svf.VrEndRecord();
			svf.VrEndPage();
		}

		svf.VrPrint();
		svf.VrQuit();

		outstrm.close();	// ストリームを閉じる
    }

	private void printHeader() {
		//	作成日(現在処理日)の取得
		try {
			KNJ_Control control = new KNJ_Control();							//クラスのインスタンス作成
			KNJ_Control.ReturnVal returnval = control.Control(db2);
			svf.VrsOut("YMD"	,KNJ_EditDate.h_format_JP(db2, returnval.val3));		//現在処理日
		} catch (Exception e) {
			log.error("[KNJA180]ctrl_date get error!", e);
		}

		//	課程学科名及びコース名の取得
		try {
			KNJ_V_Course v_course = new KNJ_V_Course();					//クラスのインスタンス作成
			KNJ_V_Course.ReturnVal returnval = v_course.V_CourseMajor(db2,pyear,pcourse_major);
			svf.VrsOut("COURSE_MAJOR"		,returnval.val1);		//課程学科名

			returnval = v_course.V_CourseCode(db2,pyear,pcoursecode);
			svf.VrsOut("COURSECODE"		,returnval.val1);		//コース名
			svf.VrsOut("nendo"			,KNJ_EditDate.gengou(db2, Integer.parseInt(pyear)) + "年度");
		} catch (Exception e) {
			log.error("[KNJA180]v_course error!", e);
		}
	}

	private void printStudent() {
		// ＳＱＬ作成
		nonedata = false;

		String sql = "SELECT DISTINCT "
					 	+ "DB1.YEAR,"
						+ "DB1.GRADE,"
					  	+ "DB1.HR_CLASS,"
					  	+ "DB1.SEMESTER,"
					  	+ "DB1.ATTENDNO,"
					  	+ "DB1.SCHREGNO,"
					  	+ "DB7.HR_NAMEABBV,"
				  		+ "DB2.NAME AS SEITO_KANJI,"
				  		+ "VALUE(DB2.NAME_KANA,'') AS SEITO_KANA,"
				  		+ "VALUE(CHAR(DB2.BIRTHDAY),'') AS BIRTHDAY, "
				  		+ "VALUE(DB3.ZIPCD,'') AS ZIPCD1,"//郵便番号
				  		+ "VALUE(DB3.ADDR1,'') AS ADDRESS1,"
				  		+ "VALUE(DB3.ADDR2,'') AS ADDRESS2,"
				  		+ "VALUE(DB3.TELNO,'') AS TELNO1,"//電話番号
				  		+ "VALUE(DB4.GUARD_NAME,'') AS GUARD_NAME,"
//NO001
//			  		+ "VALUE(DB3.EMERGENCYTELNO,'') AS TELNO2,"//急用連絡先
				  		+ "VALUE(DB2.EMERGENCYTELNO,'') AS TELNO2,"//急用連絡先
				  		//+ "VALUE(DB4.GUARD_TELNO,'') AS TELNO2,"//急用連絡先
				  		+ "VALUE(DB6.FINSCHOOL_NAME,'')	AS J_NAME "
						+ "FROM  "
		  					+ "SCHREG_REGD_DAT 			   	DB1 "
							+ "INNER JOIN SCHREG_BASE_MST  	DB2 ON DB1.SCHREGNO = DB2.SCHREGNO "
							+ "INNER JOIN SCHREG_REGD_HDAT 	DB7 ON DB1.YEAR = DB7.YEAR "
																+ "AND DB1.SEMESTER = DB7.SEMESTER "
																+ "AND DB1.GRADE = DB7.GRADE "
																+ "AND DB1.HR_CLASS = DB7.HR_CLASS "
							+ "LEFT  JOIN GUARDIAN_DAT 	  	DB4 ON DB2.SCHREGNO = DB4.SCHREGNO "
							+ "LEFT  JOIN FINSCHOOL_MST     DB6 ON DB2.FINSCHOOLCD = DB6.FINSCHOOLCD "
							+ "LEFT JOIN ("
									+ "SELECT "
										+ "SCHREGNO,"
					  					+ "ZIPCD,"
					  					+ "TELNO,"
//NO001
//				  					+ "EMERGENCYTELNO,"
					  					+ "ADDR1,"
										+ "ADDR2 "
									+ "FROM "
										+ "SCHREG_ADDRESS_DAT W1 "
									+ "WHERE "
										+ "(W1.SCHREGNO,W1.ISSUEDATE) IN ( "
													 + "SELECT SCHREGNO,MAX(ISSUEDATE) "
													 + "FROM   SCHREG_ADDRESS_DAT W2 "
													 + "WHERE  W2.ISSUEDATE <= '" + date2 + "' "
															   	+ "AND (W2.EXPIREDATE IS NULL "
																+ "OR W2.EXPIREDATE >= '" + date1 + "') "
																+ "AND W2.SCHREGNO IN " + pschregno + " "
																+ "GROUP BY SCHREGNO ) "
							+ ")DB3 ON DB3.SCHREGNO = DB1.SCHREGNO "
						+ "WHERE  "
					     	+ "	   DB1.YEAR     = '" + pyear     + "' "
							+ "AND DB1.SEMESTER = '" + psemester + "' "
							+ "AND DB1.SCHREGNO IN " + pschregno + " "
					     	+ "AND DB1.COURSECD || DB1.MAJORCD = '" + pcourse_major + "' "
							+ "AND DB1.COURSECODE = '" + pcoursecode + "' "
						+" ORDER BY DB1.GRADE,DB1.HR_CLASS,DB1.ATTENDNO";

		log.debug("[KNJA180]sql="+sql);

		ResultSet rs = null;					// ＤＢの検索結果
		// 照会結果の取得およびＳＶＦへ出力
		try {
	        db2.query(sql);
			rs = db2.getResultSet();

			while (rs.next()) {
				// 学年が変わったら、改ページ
				svf.VrsOut("GRADE"			,rs.getString("GRADE"));
				// svfフォームのフィールドへ出力
				svf.VrsOut("CLASS",rs.getString("HR_NAMEABBV") + "-" + Integer.toString(rs.getInt("ATTENDNO")));
				svf.VrsOut("gakusekibango"	,rs.getString("SCHREGNO"));
				svf.VrsOut("seito_kana"		,rs.getString("SEITO_KANA"));
				svf.VrsOut("NAME"				,rs.getString("SEITO_KANJI"));
				svf.VrsOut("birthday1"		,KNJ_EditDate.h_format_JP_N(db2, rs.getString("BIRTHDAY")));
				svf.VrsOut("birthday2"		,KNJ_EditDate.h_format_JP_MD(rs.getString("BIRTHDAY")));
				svf.VrsOut("ZIPCD"			,rs.getString("ZIPCD1"));
				svf.VrsOut("seito_jyusho"		,rs.getString("ADDRESS1"));
				svf.VrsOut("seito_jyusho2"	,rs.getString("ADDRESS2"));
				//電話番号出力フラグ
				if (tel_flg != null) {
					svf.VrsOut("PHONE1"			,rs.getString("TELNO1"));
					svf.VrsOut("denwabango"		,rs.getString("TELNO2"));
				}
				svf.VrsOut("hogosha_kanji"	,rs.getString("GUARD_NAME"));
				svf.VrsOut("syussinko"		,rs.getString("J_NAME"));
		 		svf.VrEndRecord();
				nonedata  = true; //該当データなしフラグ
			}
		} catch (Exception e) {
			log.error("[KNJA180]DB2 query error!", e);
		} finally {
			DbUtils.closeQuietly(rs);
			db2.commit();
		}
	}

	private void setdate() {
		//	学期期間の取得(住所取得用)
		try {
			KNJ_Semester semester = new KNJ_Semester();						//クラスのインスタンス作成
			KNJ_Semester.ReturnVal returnval =
						semester.Semester(db2, pyear, psemester);
			date1 = returnval.val2;											//学期開始日
			date2 = returnval.val3;											//学期終了日
		} catch (Exception e) {
			log.error("[KNJA180]Semester sdate get error!", e);
		}
	}

	private void setpschregno() {
		//	学籍番号の取得
		try {
            String sql = "SELECT "
                        + "T1.SCHREGNO,T1.GRADE,T1.HR_CLASS,T1.ATTENDNO "
                    + "FROM "
		                + "SCHREG_BASE_MST T3,"
		                + "SCHREG_REGD_DAT T1,"
		                + "SCHREG_REGD_HDAT T2 "
                    + "WHERE "
		                    + "T2.SEMESTER   			= '" +  psemester + "' "
                        + "AND T2.YEAR       			= '" +  pyear + "' "
                        + "AND T2.GRADE || T2.HR_CLASS  IN " +  pgrade_hr_class + " "
                        + "AND T1.YEAR       			= T2.YEAR "
                        + "AND T1.SEMESTER   			= T2.SEMESTER "
                        + "AND T1.GRADE      			= T2.GRADE "
                        + "AND T1.HR_CLASS   			= T2.HR_CLASS "
                        + "AND T1.SCHREGNO   			= T3.SCHREGNO "
                    + "ORDER BY 2, 3, 4 ";

			db2.query(sql);
			ResultSet rs = db2.getResultSet();

			int i = 0;
			pschregno = "(";
			while( rs.next() ){
				if(i > 0) pschregno = pschregno + ",";
				pschregno = pschregno + "'" + rs.getString("SCHREGNO") + "'";
				i++;
			}
			pschregno = pschregno + ")";

			db2.commit();
			log.debug("[KNJA180]pschregno ok!");
		} catch (Exception e) {
			log.error("[KNJA180]pschregno error!", e);
		}
	}
}

