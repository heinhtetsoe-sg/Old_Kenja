package servletpack.KNJH;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import nao_package.svf.*;
import nao_package.db.*;
import java.sql.*;
import java.util.*;

/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*
 *
 *	学校教育システム 賢者 [指導情報管理]
 *
 *					＜ＫＮＪＨ０５０＞  生徒調査 学校要覧用（高等学校）
 *
 *		＊高校用と中学校用との相違個所	param[4]〜[7]
 *
 *	2003/04/15 yamashiro・LAA003より分離後、変更
 *  2006/06/10 m-yama    NO002 SCHREG_ENVIR_DATの変更に伴う修正 
 *  2006/07/05 m-yama    NO003 通学状況が出力されないバグを修正(地域コード)
 *<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*/

public class KNJH050 extends HttpServlet {
	Vrw32alp svf = new Vrw32alp(); //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
	DB2UDB    db2;		// Databaseクラスを継承したクラス
	String dbname = new String();
	int ret;      		// リターン値
	boolean nonedata; 	//該当データなしフラグ


	/*----------------------------*
	 * HTTP Get リクエストの処理  *
	 *----------------------------*/
	public void svf_out(HttpServletRequest request, HttpServletResponse response)
	                 throws ServletException, IOException
	{
			/*
				3	CLASS_SELECTED 	101 
				3	CLASS_SELECTED 	102 
				0	YEAR 			2002 
				1	GAKKI 			3  
			*/

	    String param[] = new String[8];

	// パラメータの取得
		try {
			param[4] = "高";			//	中			・高
//			param[5] = "J_CD";			//	E_CD		・J_CD
//			param[6] = "J_NAME";		//	E_NAME		・J_NAME
//			param[7] = "FINSCHOOL_MST";	//	ELEMENT_MST	・FINSCHOOL_MST

			dbname   = request.getParameter("DBNAME");      // データベース名
	        param[0] = request.getParameter("YEAR");        // 年度
	        param[1] = request.getParameter("GAKKI");       // 学期

			//出力対象学年・組の編集
			String pclass[] = request.getParameterValues("CLASS_SELECTED");
			param[3] = pclass[0];
			for(int ia=1 ; ia<pclass.length ; ia++){
				param[3] = param[3] + "," + pclass[ia];
			}
		} catch( Exception ex ) {
			System.out.println("[KNJH050]parameter error!");
			System.out.println(ex);
		}


	// print設定
		PrintWriter out = new PrintWriter (response.getOutputStream());
		response.setContentType("application/pdf");
		OutputStream outstrm = response.getOutputStream();

	// svf設定
		ret = svf.VrInit();						   	//クラスの初期化
		ret = svf.VrSetSpoolFileStream(outstrm);   	//PDFファイル名の設定
		ret = svf.VrSetForm("KNJH050.frm", 1);		//svf-form

	// ＤＢ接続
		db2 = new DB2UDB(dbname, "db2inst1", "db2inst1", DB2UDB.TYPE2);
		try {
			db2.open();
		} catch( Exception ex ) {
			System.out.println("[KNJH050]DB2 open error!");
			System.out.println(ex);
		}

		for(int ia=0 ; ia<param.length ; ia++) System.out.println("[KNJH050]param[" + ia + "]=" + param[ia]);

	/*-----------------------------------------------------------------------------
	    ＳＶＦ作成処理       
	  -----------------------------------------------------------------------------*/
		nonedata = false;

	//見出し等の出力
		String namecd_b020[][] = new String[4][2];		//地区コード・名称
		set_head(param,namecd_b020);					//地区コード・名称の取得

	//クラス毎の出力
		StringTokenizer sttoken = new StringTokenizer(param[3],",",false);	//学年・組
		while (sttoken.hasMoreTokens()) {
			param[2] = sttoken.nextToken();
			set_detail0(param);					//クラス・担任名等
			set_detail1(param,namecd_b020);		//通学状況
			set_detail2(param);					//兄弟姉妹調査
			set_detail3(param);					//住居調査
			set_detail4(param);					//保護者職業
			ret = svf.VrEndRecord();
			ret = svf.VrEndPage();
		}
/*
	//該当データ無し
		if(nonedata == false){
			ret = svf.VrSetForm("MES001.frm", 0);
			ret = svf.VrsOut("note" , "note");
			ret = svf.VrEndRecord();
			ret = svf.VrEndPage();
		}
*/
	// 終了処理
		db2.close();		// DBを閉じる
		ret = svf.VrQuit();
		outstrm.close();	// ストリームを閉じる 

    }    //doGetの括り



	/*------------------------------------*
	 * 地区コード・名称取得	 			  *
	 *------------------------------------*/
	public void set_head(String param[],String namecd_b020[][])
	                 throws ServletException, IOException
	{
		try {
			String sql = new String();
			sql = "SELECT "
					+ "NAMECD1,"
					+ "NAMECD2,"
					+ "NAME1 "
				+ "FROM "
					+ "NAME_MST "
				+ "WHERE "
					+ "NAMECD1 = 'A020' "	//地区コード
				+ "ORDER BY "
					+ "NAMECD1,NAMECD2";

			db2.query(sql);
			ResultSet rs = db2.getResultSet();
			for(int ia=0 ; ia<namecd_b020.length ; ia++){
				if( !rs.next() )	break;
				namecd_b020[ia][0] = rs.getString("NAMECD2");
				namecd_b020[ia][1] = rs.getString("NAME1");
			}
			db2.commit();
		} catch( Exception e ){
			System.out.println("[KNJH050]set_head error!");
			System.out.println( e );
		}


	}//public void set_headの括り


	/*------------------------------------*
	 * クラス・担任名等		  			  *
	 *------------------------------------*/
	public void set_detail0(String param[])
	                 throws ServletException, IOException
	{
		try {
			String sql = new String();
			sql = "SELECT "
					+ "T2.GRADE,"
					+ "T2.HR_CLASS,"
					+ "T2.HR_NAME,"
					+ "T1.MEN_COUNT,"
					+ "T1.WOMEN_COUNT,"
					+ "T1.TOTAL_COUNT,"
					+ "T3.STAFFNAME AS STAFF_NAME "
				+ "FROM "
					+ "("
						+ "SELECT "
							+ "T1.YEAR,"
							+ "T1.GRADE,"
							+ "T1.HR_CLASS,"
							+ "T1.SEMESTER,"
							+ "SUM(CASE WHEN T2.SEX = '1' THEN 1 ELSE 0 END) AS MEN_COUNT,"
							+ "SUM(CASE WHEN T2.SEX = '2' THEN 1 ELSE 0 END) AS WOMEN_COUNT,"
							+ "COUNT(T1.SCHREGNO) AS TOTAL_COUNT "
						+ "FROM "
							+ "SCHREG_REGD_DAT T1,"
							+ "SCHREG_BASE_MST T2 "
						+ "WHERE "
								+ "YEAR = '" + param[0] + "' "
							+ "AND GRADE || HR_CLASS = '" + param[2] + "' "
							+ "AND SEMESTER = '" + param[1] + "' "
							+ "AND T1.SCHREGNO = T2.SCHREGNO "
						+ "GROUP BY "
							+ "T1.YEAR,"
							+ "T1.GRADE,"
							+ "T1.HR_CLASS,"
							+ "T1.SEMESTER "
					+ ")T1 "
					+ "INNER JOIN SCHREG_REGD_HDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE "
														+ "AND T2.HR_CLASS = T1.HR_CLASS AND T2.SEMESTER = T1.SEMESTER "
					+ "LEFT JOIN STAFF_MST T3 ON T3.STAFFCD = T2.TR_CD1";

            //System.out.println("[KNJH050]set_detail0 sql="+sql);
            db2.query(sql);
			ResultSet rs = db2.getResultSet();
			//System.out.println("[KNJH050]set_detail0 sql ok!");

	  	  	// SVF-formへデータを出力 
//			ret = svf.VrsOut("NENDO"  	, param[0]);
			ret = svf.VrsOut("NENDO"  	, nao_package.KenjaProperties.gengou(Integer.parseInt(param[0])) + "年度");
			if( rs.next() ){
				ret = svf.VrsOut("KUBUN" 		, param[4]);
//				ret = svf.VrsOut("GRADE" 		, rs.getString("GRADE"));
//				ret = svf.VrsOut("HR_CLASS" 	, rs.getString("HR_CLASS"));
				ret = svf.VrsOut("HR_NAME" 		, rs.getString("HR_NAME"));
				ret = svf.VrsOut("BOY" 			, rs.getString("MEN_COUNT"));
				ret = svf.VrsOut("GIRL" 		, rs.getString("WOMEN_COUNT"));
				ret = svf.VrsOut("TOTAL" 		, rs.getString("TOTAL_COUNT"));
				ret = svf.VrsOut("STAFFNAME" 	, rs.getString("STAFF_NAME"));
			}
			db2.commit();
    	    //System.out.println("[KNJH050]set_detail0 read ok!");
		} catch( Exception e ){
			System.out.println("[KNJH050]set_detail0 error!");
			System.out.println( e );
		}

	}//public void set_detai0の括り



	/*------------------------------------*
	 * 通学状況		  			  		  *
	 *------------------------------------*/
	public void set_detail1(String param[],String namecd_b020[][])
	                 throws ServletException, IOException
	{
		try {
			String sql = new String();
			sql = "SELECT "
					+ "VALUE(NAMECD2,'XX') AS NAMECD,"
					+ "VALUE(NAME1,'TOTAL') AS NAME,"
					//地区コードごとの男女別集計
					//NO003↓(AREACD='X'→AREACD='0X'に修正)
					+ "SUM(CASE WHEN SEX = '1' AND AREACD = '01' THEN SCH_CNT ELSE 0 END) AS CNT_1_1,"
					+ "SUM(CASE WHEN SEX = '2' AND AREACD = '01' THEN SCH_CNT ELSE 0 END) AS CNT_1_2,"
					+ "SUM(CASE WHEN SEX = '1' AND AREACD = '02' THEN SCH_CNT ELSE 0 END) AS CNT_2_1,"
					+ "SUM(CASE WHEN SEX = '2' AND AREACD = '02' THEN SCH_CNT ELSE 0 END) AS CNT_2_2,"
					+ "SUM(CASE WHEN SEX = '1' AND AREACD = '03' THEN SCH_CNT ELSE 0 END) AS CNT_3_1,"
					+ "SUM(CASE WHEN SEX = '2' AND AREACD = '03' THEN SCH_CNT ELSE 0 END) AS CNT_3_2,"
					+ "SUM(CASE WHEN SEX = '1' AND AREACD = '04' THEN SCH_CNT ELSE 0 END) AS CNT_4_1,"
					+ "SUM(CASE WHEN SEX = '2' AND AREACD = '04' THEN SCH_CNT ELSE 0 END) AS CNT_4_2,"
					//全地区合計の男女別集計
					+ "SUM(CASE WHEN SEX = '1' AND AREACD IN ('01','02','03','04') THEN SCH_CNT ELSE 0 END) AS CNT_5_1,"
					+ "SUM(CASE WHEN SEX = '2' AND AREACD IN ('01','02','03','04') THEN SCH_CNT ELSE 0 END) AS CNT_5_2 "
					//NO003↑

				+ "FROM "
					+ "("
						+ "SELECT "
							+ "NAMECD2,"
							+ "NAME1 "
						+ "FROM "
							+ "NAME_MST "
						+ "WHERE "
							+ "NAMECD1 = 'H100' "
					+ ")T1 "
					+ "LEFT JOIN ("
						+ "SELECT "
							 + "COUNT(DISTINCT ST1.SCHREGNO) AS SCH_CNT,"
							 + "HOWTOCOMMUTECD,"
							 + "AREACD,"
							 + "SEX "
						+ "FROM "
							+ "SCHREG_REGD_DAT ST1 "
							+ "INNER JOIN SCHREG_BASE_MST ST2 ON ST1.SCHREGNO = ST2.SCHREGNO "
							+ "INNER JOIN SCHREG_ADDRESS_DAT ST3 ON ST1.SCHREGNO = ST3.SCHREGNO "
							+ "INNER JOIN ("
								+ "SELECT "
									+ "SCHREGNO,"
									+ "MAX(ISSUEDATE) AS ISSUEDATE "
								+ "FROM "
									+ "SCHREG_ADDRESS_DAT "
								+ "WHERE "
										+ "ISSUEDATE <= '" +  String.valueOf(Integer.parseInt(param[0])+1) + "-03-31' "
									+ "AND EXPIREDATE >= '" +  param[0] + "-04-01' "
								+ "GROUP BY "
									+ "SCHREGNO "
							+ ")ST5 ON ST3.SCHREGNO = ST5.SCHREGNO AND ST3.ISSUEDATE = ST5.ISSUEDATE "
//							+ "INNER JOIN STUD_ENVIR_DAT ST4 ON ST1.YEAR = ST4.RES_YEAR "
//NO002
//							+ "INNER JOIN SCHREG_ENVIR_DAT ST4 ON ST1.YEAR = ST4.YEAR "
//																 + "AND ST1.SCHREGNO = ST4.SCHREGNO "
							+ "INNER JOIN SCHREG_ENVIR_DAT ST4 ON ST1.SCHREGNO = ST4.SCHREGNO "
						+ "WHERE "
								+ "ST1.YEAR = '" +  param[0] + "' "
							+ "AND ST1.SEMESTER = '" +  param[1] + "' "
							+ "AND ST1.GRADE || ST1.HR_CLASS = '" + param[2] + "' "
						+ "GROUP BY "
							 + "HOWTOCOMMUTECD,"
							 + "AREACD,"
							+ " SEX "
					+ ") T2 ON T2.HOWTOCOMMUTECD = CASE WHEN NAMECD2='99' THEN '9' ELSE NAMECD2 END "
				+ "GROUP BY "
				+ "GROUPING SETS "
					+ "((NAMECD2,NAME1),()) "
				+ "ORDER BY "
					+ "NAMECD";

            //System.out.println("[KNJH050]set_detail1 sql="+sql);
            db2.query(sql);
			ResultSet rs = db2.getResultSet();
			//System.out.println("[KNJH050]set_detail1 sql ok!");

	  	  	// SVF-formへデータを出力 
			for(int ia=0 ; ia<namecd_b020.length ; ia++)
				ret = svf.VrsOut("NAME_"+(ia+1)	,namecd_b020[ia][1]);
			ret = svf.VrsOut("NAME_5"	,"計");
			int ia = 0;
			while( rs.next() ){
				if(rs.getString("NAMECD").equalsIgnoreCase("XX")){
					ia = 6;
					//合計
					ret = svf.VrsOutn("METHOD" 		,ia+1	,"合計");
					ret = svf.VrlOutn("NUMBER1_1" 	,ia+1	,rs.getInt("CNT_1_1"));
					ret = svf.VrlOutn("NUMBER1_2" 	,ia+1	,rs.getInt("CNT_1_2"));
					ret = svf.VrlOutn("NUMBER2_1" 	,ia+1	,rs.getInt("CNT_2_1"));
					ret = svf.VrlOutn("NUMBER2_2" 	,ia+1	,rs.getInt("CNT_2_2"));
					ret = svf.VrlOutn("NUMBER3_1" 	,ia+1	,rs.getInt("CNT_3_1"));
					ret = svf.VrlOutn("NUMBER3_2" 	,ia+1	,rs.getInt("CNT_3_2"));
					ret = svf.VrlOutn("NUMBER4_1" 	,ia+1	,rs.getInt("CNT_4_1"));
					ret = svf.VrlOutn("NUMBER4_2" 	,ia+1	,rs.getInt("CNT_4_2"));
					ret = svf.VrlOutn("NUMBER5_1" 	,ia+1	,rs.getInt("CNT_5_1"));
					ret = svf.VrlOutn("NUMBER5_2" 	,ia+1	,rs.getInt("CNT_5_2"));
				} else{
					if(ia < 6){
						ret = svf.VrsOutn("METHOD" 		,ia+1	,rs.getString("NAME"));
						ret = svf.VrlOutn("NUMBER1_1" 	,ia+1	,rs.getInt("CNT_1_1"));
						ret = svf.VrlOutn("NUMBER1_2" 	,ia+1	,rs.getInt("CNT_1_2"));
						ret = svf.VrlOutn("NUMBER2_1" 	,ia+1	,rs.getInt("CNT_2_1"));
						ret = svf.VrlOutn("NUMBER2_2" 	,ia+1	,rs.getInt("CNT_2_2"));
						ret = svf.VrlOutn("NUMBER3_1" 	,ia+1	,rs.getInt("CNT_3_1"));
						ret = svf.VrlOutn("NUMBER3_2" 	,ia+1	,rs.getInt("CNT_3_2"));
						ret = svf.VrlOutn("NUMBER4_1" 	,ia+1	,rs.getInt("CNT_4_1"));
						ret = svf.VrlOutn("NUMBER4_2" 	,ia+1	,rs.getInt("CNT_4_2"));
						ret = svf.VrlOutn("NUMBER5_1" 	,ia+1	,rs.getInt("CNT_5_1"));
						ret = svf.VrlOutn("NUMBER5_2" 	,ia+1	,rs.getInt("CNT_5_2"));
					}
					ia++;
				}
			}
			db2.commit();
    	    //System.out.println("[KNJH050]set_detail1 read ok!");
		} catch( Exception ex ) {
			System.out.println("[KNJH050]set_detail1 read error!");
			System.out.println(ex);
		}

	}//public void set_detailの括り


	/*------------------------------------*
	 * 兄弟姉妹調査	  			  		  *
	 *------------------------------------*/
	public void set_detail2(String param[])
	                 throws ServletException, IOException
	{
		try {
			String sql = new String();
			sql = "SELECT "
					+ "VALUE(NAMECD2,'XX') AS NAMECD,"
					+ "VALUE(NAME1,'TOTAL') AS NAME,"
					+ "SUM(CASE WHEN SEX = '1' THEN SCH_CNT ELSE 0 END) AS CNT_1,"
					+ "SUM(CASE WHEN SEX = '2' THEN SCH_CNT ELSE 0 END) AS CNT_2,"
					+ "SUM(CASE WHEN SEX IN ('1','2') THEN SCH_CNT ELSE 0 END) AS CNT_3 "
				+ "FROM "
					+ "("
						+ "SELECT "
							+ "NAMECD2,"
							+ "NAME1 "
						+ "FROM "
							+ "NAME_MST "
						+ "WHERE "
							+ "NAMECD1 = 'H107' "
					+ ")T1 "
					+ "LEFT JOIN ("
						+ "SELECT "
							 + "COUNT(DISTINCT ST1.SCHREGNO) AS SCH_CNT,"
							 + "BRO_SISCD,"
							 + "SEX "
						+ "FROM "
							+ "SCHREG_REGD_DAT ST1 "
							+ "INNER JOIN SCHREG_BASE_MST ST2 ON ST1.SCHREGNO = ST2.SCHREGNO "
//							+ "INNER JOIN STUD_ENVIR_DAT ST4 ON ST1.YEAR = ST4.RES_YEAR "
//NO002
//							+ "INNER JOIN SCHREG_ENVIR_DAT ST4 ON ST1.YEAR = ST4.YEAR "
//																 + "AND ST1.SCHREGNO = ST4.SCHREGNO "
							+ "INNER JOIN SCHREG_ENVIR_DAT ST4 ON ST1.SCHREGNO = ST4.SCHREGNO "
						+ "WHERE "
								+ "ST1.YEAR = '" +  param[0] + "' "
							+ "AND ST1.SEMESTER = '" +  param[1] + "' "
							+ "AND ST1.GRADE || ST1.HR_CLASS = '" + param[2] + "' "
						+ "GROUP BY "
							 + "BRO_SISCD,"
							+ " SEX "
					+ ") T2 ON INT(T2.BRO_SISCD) = INT(CASE WHEN NAMECD2='99' THEN '9' ELSE NAMECD2 END) "
				+ "GROUP BY "
				+ "GROUPING SETS "
					+ "((NAMECD2,NAME1),()) "
				+ "ORDER BY "
					+ "NAMECD";

            //System.out.println("[KNJH050]set_detail2 sql="+sql);
            db2.query(sql);
			ResultSet rs = db2.getResultSet();
			//System.out.println("[KNJH050]set_detail2 sql ok!");

	  	  	// SVF-formへデータを出力 
			int ia = 0;
			while( rs.next() ){
				if(rs.getString("NAMECD").equalsIgnoreCase("XX")){
					ia = 6;
					//合計
					ret = svf.VrsOutn("ITEM1" 		,ia+1	,"合計");
					ret = svf.VrlOutn("BROTHER1" 	,ia+1	,rs.getInt("CNT_1"));
					ret = svf.VrlOutn("BROTHER2" 	,ia+1	,rs.getInt("CNT_2"));
					ret = svf.VrlOutn("BROTHER3" 	,ia+1	,rs.getInt("CNT_3"));
				} else{
					if(ia < 6){
						ret = svf.VrsOutn("ITEM1" 		,ia+1	,rs.getString("NAME"));
						ret = svf.VrlOutn("BROTHER1" 	,ia+1	,rs.getInt("CNT_1"));
						ret = svf.VrlOutn("BROTHER2" 	,ia+1	,rs.getInt("CNT_2"));
						ret = svf.VrlOutn("BROTHER3" 	,ia+1	,rs.getInt("CNT_3"));
					}
					ia++;
				}
			}
			db2.commit();
    	    //System.out.println("[KNJH050]set_detail2 read ok!");
		} catch( Exception ex ) {
			System.out.println("[KNJH050]set_detail2 read error!");
			System.out.println(ex);
		}

	}//public void set_detai2の括り


	/*------------------------------------*
	 * 住居調査		  			  		  *
	 *------------------------------------*/
	public void set_detail3(String param[])
	                 throws ServletException, IOException
	{
		try {
			String sql = new String();
			sql = "SELECT "
					+ "VALUE(NAMECD2,'XX') AS NAMECD,"
					+ "VALUE(NAME1,'TOTAL') AS NAME,"
					+ "SUM(CASE WHEN SEX = '1' THEN SCH_CNT ELSE 0 END) AS CNT_1,"
					+ "SUM(CASE WHEN SEX = '2' THEN SCH_CNT ELSE 0 END) AS CNT_2,"
					+ "SUM(CASE WHEN SEX IN ('1','2') THEN SCH_CNT ELSE 0 END) AS CNT_3 "
				+ "FROM "
					+ "("
						+ "SELECT "
							+ "NAMECD2,"
							+ "NAME1 "
						+ "FROM "
							+ "NAME_MST "
						+ "WHERE "
							+ "NAMECD1 = 'H108' "
					+ ")T1 "
					+ "LEFT JOIN ("
						+ "SELECT "
							 + "COUNT(DISTINCT ST1.SCHREGNO) AS SCH_CNT,"
							 + "RESIDENTCD,"
							 + "SEX "
						+ "FROM "
							+ "SCHREG_REGD_DAT ST1 "
							+ "INNER JOIN SCHREG_BASE_MST ST2 ON ST1.SCHREGNO = ST2.SCHREGNO "
//							+ "INNER JOIN STUD_ENVIR_DAT ST4 ON ST1.YEAR = ST4.RES_YEAR "
//NO002
//							+ "INNER JOIN SCHREG_ENVIR_DAT ST4 ON ST1.YEAR = ST4.YEAR "
//																 + "AND ST1.SCHREGNO = ST4.SCHREGNO "
							+ "INNER JOIN SCHREG_ENVIR_DAT ST4 ON ST1.SCHREGNO = ST4.SCHREGNO "
						+ "WHERE "
								+ "ST1.YEAR = '" +  param[0] + "' "
							+ "AND ST1.SEMESTER = '" +  param[1] + "' "
							+ "AND ST1.GRADE || ST1.HR_CLASS = '" + param[2] + "' "
						+ "GROUP BY "
							 + "RESIDENTCD,"
							+ " SEX "
					+ ") T2 ON INT(T2.RESIDENTCD) = INT(CASE WHEN NAMECD2='99' THEN '9' ELSE NAMECD2 END) "
				+ "GROUP BY "
				+ "GROUPING SETS "
					+ "((NAMECD2,NAME1),()) "
				+ "ORDER BY "
					+ "NAMECD";

            //System.out.println("[KNJH050]set_detail3 sql="+sql);
            db2.query(sql);
			ResultSet rs = db2.getResultSet();
			//System.out.println("[KNJH050]set_detail3 sql ok!");

	  	  	// SVF-formへデータを出力 
			int ia = 0;
			while( rs.next() ){
				if(rs.getString("NAMECD").equalsIgnoreCase("XX")){
					ia = 4;
					//合計
					ret = svf.VrsOutn("ITEM2" 	,ia+1	,"合計");
					ret = svf.VrlOutn("HOUSE1" 	,ia+1	,rs.getInt("CNT_1"));
					ret = svf.VrlOutn("HOUSE2" 	,ia+1	,rs.getInt("CNT_2"));
					ret = svf.VrlOutn("HOUSE3" 	,ia+1	,rs.getInt("CNT_3"));
				} else{
					if(ia < 4){
						ret = svf.VrsOutn("ITEM2" 	,ia+1	,rs.getString("NAME"));
						ret = svf.VrlOutn("HOUSE1" 	,ia+1	,rs.getInt("CNT_1"));
						ret = svf.VrlOutn("HOUSE2" 	,ia+1	,rs.getInt("CNT_2"));
						ret = svf.VrlOutn("HOUSE3" 	,ia+1	,rs.getInt("CNT_3"));
					}
					ia++;
				}
			}
			db2.commit();
    	    //System.out.println("[KNJH050]set_detail3 read ok!");
		} catch( Exception ex ) {
			System.out.println("[KNJH050]set_detail3 read error!");
			System.out.println(ex);
		}

	}//public void set_detai3の括り


	/*------------------------------------*
	 * 保護者の職業調査			  		  *
	 *------------------------------------*/
	public void set_detail4(String param[])
	                 throws ServletException, IOException
	{
		try {
			String sql = new String();
			sql = "SELECT "
					+ "VALUE(T1.NAMECD2,'XX') AS NAMECD,"
					+ "VALUE(T1.NAME1,'TOTAL') AS NAME,"
					+ "VALUE(SUM(SCH_CNT),0) AS SCH_CNT "
				+ "FROM "
					+ "("
						+ "SELECT "
							+ "NAMECD2,"
							+ "NAME1 "
						+ "FROM "
							+ "NAME_MST "
						+ "WHERE "
							+ "NAMECD1 = 'H202' "
					+ ")T1 "
					+ "LEFT JOIN ("
						+ "SELECT "
							+ "COUNT(DISTINCT ST1.SCHREGNO) AS SCH_CNT,"
							+ "ST4.GUARD_JOBCD "
						+ "FROM "
							+ "SCHREG_REGD_DAT ST1,"
							+ "SCHREG_BASE_MST ST2,"
							+ "GUARDIAN_DAT ST4 "
						+ "WHERE "
								+ "ST1.YEAR = '" + param[0] + "' "
							+ "AND ST1.SEMESTER = '" + param[1] + "' "
							+ "AND ST1.GRADE || ST1.HR_CLASS = '" + param[2] + "' "
							+ "AND ST1.SCHREGNO = ST2.SCHREGNO "
							+ "AND ST1.SCHREGNO = ST4.SCHREGNO "
							+ "AND VALUE(ST4.GUARD_JOBCD,'00')>='01' "
						+ "GROUP BY "
							+ "GUARD_JOBCD "
					+ ") T2 ON T2.GUARD_JOBCD = T1.NAMECD2 "
				+ "GROUP BY "
				+ "GROUPING SETS "
					+ "((NAMECD2,NAME1),()) "
				+ "ORDER BY "
					+ "NAMECD";

            //System.out.println("[KNJH050]set_detail4 sql="+sql);
            db2.query(sql);
			ResultSet rs = db2.getResultSet();
			//System.out.println("[KNJH050]set_detail4 sql ok!");

	  	  	// SVF-formへデータを出力 
			int ia = 0;
			int sonota = 0;
			while( rs.next() ){
				if(rs.getString("NAMECD").equalsIgnoreCase("XX")){
					//その他
					ret = svf.VrsOutn("BUSINESS" 	,27	,"その他");
					ret = svf.VrlOutn("COUNT" 		,27	,sonota);		//人数
					//合計
					ret = svf.VrsOutn("BUSINESS" 	,28	,"合計");
					ret = svf.VrlOutn("COUNT" 		,28	,rs.getInt("SCH_CNT"));	//人数
				} else{
					if(ia < 26){
						ret = svf.VrsOutn("BUSINESS" 	,ia+1	,rs.getString("NAME"));		//職業名称
						ret = svf.VrlOutn("COUNT" 		,ia+1	,rs.getInt("SCH_CNT"));		//人数
					} else{
						sonota = sonota + rs.getInt("SCH_CNT");
					}
					ia++;
				}
			}
			db2.commit();
    	    //System.out.println("[KNJH050]set_detail4 read ok!");
		} catch( Exception ex ) {
			System.out.println("[KNJH050]set_detail4 read error!");
			System.out.println(ex);
		}

	}  //public void set_detai4の括り


}  //クラスの括り
