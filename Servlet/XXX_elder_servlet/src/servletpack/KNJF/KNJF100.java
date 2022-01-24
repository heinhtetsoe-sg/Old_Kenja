package servletpack.KNJF;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import nao_package.svf.*;
import nao_package.db.*;
import java.sql.*;
import java.util.*;
import servletpack.KNJZ.detail.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 *	学校教育システム 賢者 [保健管理]
 *
 *					＜ＫＮＪＦ１０００＞  保健室利用一覧
 *
 *
 * 2003/11/12 nakamoto 和暦変換に対応
 * 2006/07/31 nakamoto NO001:来室日付に時間も表示する修正をした。
 */

public class KNJF100 {

    private static final Log log = LogFactory.getLog(KNJF100.class);

	Vrw32alp svf = new Vrw32alp(); //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
	DB2UDB    db2;		// Databaseクラスを継承したクラス
	String dbname = new String();
	int ret;      		// リターン値
	boolean nonedata; 	//該当データなしフラグ
// 2003/11/12
	KNJ_EditDate editdate = new KNJ_EditDate();		//和暦変換取得クラスのインスタンス作成


	/*----------------------------*
	 * HTTP Get リクエストの処理  *
	 *----------------------------*/
	public void svf_out(HttpServletRequest request, HttpServletResponse response)
	                 throws ServletException, IOException
	{

	    String param[] = new String[7];

	// パラメータの取得
		try {
			dbname   = request.getParameter("DBNAME");      // データベース名
	        param[0] = request.getParameter("YEAR");        // 年度
	        param[1] = request.getParameter("SEMESTER");    // 学期

			StringBuffer stbx = new StringBuffer();
			stbx.append(request.getParameter("DATE1"));         			// 日付範囲FROM
			stbx.replace(4,5,"-");
			stbx.replace(7,8,"-");
	        param[2] = stbx.toString();

			stbx = new StringBuffer();
			stbx.append(request.getParameter("DATE2"));         			// 日付範囲TO
			stbx.replace(4,5,"-");
			stbx.replace(7,8,"-");
	        param[3] = stbx.toString();

			String ghclass[] = request.getParameterValues("CLASS_SELECTED");	// 出力対象クラス
			int i = 0;
			param[4] = "(";
			while(i < ghclass.length){
				if(ghclass[i] == null ) break;
				if(i > 0) param[4] = param[4] + ",";
				param[4] = param[4] + "'" + ghclass[i] + "'";
				i++;
			}
			param[4] = param[4] + ")";

			param[5] = "off";
			if(request.getParameter("CHECK1") != null)
				if(request.getParameter("CHECK1").equalsIgnoreCase("on"))	param[5] = "on";//改ページ
		} catch( Exception ex ) {
			log.error("parameter error!" ,ex);
		}


	// print設定
		PrintWriter out = new PrintWriter (response.getOutputStream());
		response.setContentType("application/pdf");
		OutputStream outstrm = response.getOutputStream();

	// svf設定
		ret = svf.VrInit();						   //クラスの初期化
		ret = svf.VrSetSpoolFileStream(outstrm);   //PDFファイル名の設定

	// ＤＢ接続
		db2 = new DB2UDB(dbname, "db2inst1", "db2inst1", DB2UDB.TYPE2);
		try {
			db2.open();
		} catch( Exception ex ) {
			log.error("DB2 open error!" ,ex);
		}

	//	作成日(現在処理日)の取得
		try {
			KNJ_Control control = new KNJ_Control();							//クラスのインスタンス作成
			KNJ_Control.ReturnVal returnval = control.Control(db2);
			param[6] = returnval.val3;											//現在処理日
		} catch( Exception e ){
			log.error("ctrl_date get error!" ,e);
		}

		for(int ia=0 ; ia<param.length ; ia++) log.debug("param[" + ia + "]=" + param[ia]);

	/*-----------------------------------------------------------------------------
	    ＳＶＦ作成処理       
	  -----------------------------------------------------------------------------*/
		nonedata = false;


		set_detail1(param);		//SVF出力

		if(nonedata == false){
			//該当データ無し
			ret = svf.VrSetForm("MES001.frm", 0);
			ret = svf.VrsOut("note" , "note");
			ret = svf.VrEndRecord();
			ret = svf.VrEndPage();
		}

	// 終了処理
		db2.close();		// DBを閉じる
		ret = svf.VrQuit();
		outstrm.close();	// ストリームを閉じる 

    }    //doGetの括り



	/*--------------------*
	 * 　SVF出力 		  *
	 *--------------------*/
	public void set_detail1(String param[])
	                 throws ServletException, IOException
	{
		try {
			String sql = new String();
			sql = "SELECT "
					+ "T1.SCHREGNO,"
					+ "SUBSTR(CHAR(T1.DATE),1,10) AS DATE,"
					+ "TIME(T1.DATE) AS DATE_TIME,"//NO001
					+ "T2.GRADE,"
					+ "T2.HR_CLASS,"
					+ "T2.ATTENDNO,"
					+ "T2H.HR_NAME,"
					+ "T3.NAME_SHOW AS NAME,"
					+ "T4A.NAME1 AS INTERNAL_NAME,"
					+ "T4B.NAME1 AS EXTERNAL_NAME,"
					+ "VALUE(T1.OCCURTIMECD,'0') AS OCCURTIMECD,"
					+ "T1.OCCURTIME,"
					+ "T1.BEDTIME,"
					+ "T1.RISINGTIME,"
					+ "T4C.NAME1 AS SLEEPING_NAME,"
					+ "T4D.NAME1 AS BREAKFAST_NAME,"
					+ "T4E.NAME1 AS NURSETREAT_NAME,"
					+ "T4F.NAME1 AS OCCURTIMECD_NAME,"
					+ "T1.REMARK "
				
				+ "FROM "
					+ "SCHREG_REGD_HDAT T2H, "
					+ "SCHREG_REGD_DAT T2 "
					+ "INNER JOIN NURSEOFFICE_DAT T1 ON T2.SCHREGNO = T1.SCHREGNO "
					+ "INNER JOIN SCHREG_BASE_MST T3 ON T1.SCHREGNO = T3.SCHREGNO "
					+ "LEFT JOIN NAME_MST T4A ON T4A.NAMECD2 = T1.VISIT_REASON 	AND T4A.NAMECD1 = 'F720' AND T1.TREATMENT_DIV = '01' "
					+ "LEFT JOIN NAME_MST T4B ON T4B.NAMECD2 = T1.VISIT_REASON 	AND T4B.NAMECD1 = 'F730' AND T1.TREATMENT_DIV = '02' "
					+ "LEFT JOIN NAME_MST T4C ON T4C.NAMECD2 = T1.SLEEPING 		AND T4C.NAMECD1 = 'F760' "
					+ "LEFT JOIN NAME_MST T4D ON T4D.NAMECD2 = T1.BREAKFAST 	AND T4D.NAMECD1 = 'F750' "
					+ "LEFT JOIN NAME_MST T4E ON T4E.NAMECD2 = T1.NURSETREAT 	AND T4E.NAMECD1 = 'F770' "
					+ "LEFT JOIN NAME_MST T4F ON T4F.NAMECD2 = T1.OCCURTIMECD 	AND T4F.NAMECD1 = 'F740' "
				
				+ "WHERE "
						+ "T2.YEAR 		= '" +  param[0] + "' "
					+ "AND T2.SEMESTER 	= '" +  param[1] + "' "
					+ "AND T2.GRADE || T2.HR_CLASS IN " +  param[4] + " "
					+ "AND T1.YEAR 		= T2.YEAR "
					+ "AND SUBSTR(CHAR(T1.DATE),1,10) BETWEEN '" +  param[2] + "' AND '" +  param[3] + "' "
					+ "AND T2H.YEAR 	= T2.YEAR "
					+ "AND T2H.SEMESTER = T2.SEMESTER "
					+ "AND T2H.GRADE 	= T2.GRADE "
					+ "AND T2H.HR_CLASS = T2.HR_CLASS "
				
				+ "ORDER BY "
					+ "T2.GRADE,"
					+ "T2.HR_CLASS,"
					+ "T2.ATTENDNO,"
					+ "T1.DATE";

            //log.debug("set_detai1 sql="+sql);
            db2.query(sql);
			ResultSet rs = db2.getResultSet();
			log.debug("set_detail1 sql ok!");

	  	  	// SVF-formへデータを出力
			ret = svf.VrSetForm("KNJF100.frm", 4);		//svf-form
			if(param[5].equalsIgnoreCase("on")){
				ret = svf.VrAttribute("HR_NAME" 	, "FF=1");
			}
// 2003/11/12
//			ret = svf.VrsOut("DATE1" 	, param[6]);	//作成日
			ret = svf.VrsOut("DATE1"	, editdate.h_format_JP(param[6]));

			while( rs.next() ){
				//明細出力
				ret = svf.VrsOut("SCHOOL_NO" 	, rs.getString("SCHREGNO"));		//学籍番号
// 2003/11/12
//				ret = svf.VrsOut("DATE2" 		, rs.getString("DATE"));			//来室日付
				ret = svf.VrsOut("DATE2"		, editdate.h_format_JP(rs.getString("DATE")));
				ret = svf.VrsOut("TIME" 		, rs.getString("DATE_TIME"));		//来室日付(時間)--NO001
				ret = svf.VrsOut("ATTENDNO" 	, rs.getString("ATTENDNO"));		//番号
				ret = svf.VrsOut("HR_NAME" 		, rs.getString("HR_NAME"));			//組名称
				ret = svf.VrsOut("NAME" 		, rs.getString("NAME"));			//氏名
				ret = svf.VrsOut("INTERNAL1" 	, rs.getString("INTERNAL_NAME"));	//来室の理由・内科
				ret = svf.VrsOut("SURGERY" 		, rs.getString("EXTERNAL_NAME"));	//来室の理由・外科
				ret = svf.VrsOut("CONDITION1" 	, rs.getString("OCCURTIMECD_NAME"));//症状のでた頃合い
				ret = svf.VrsOut("TIME1" 		, rs.getString("OCCURTIME"));		//症状のでた時間
				ret = svf.VrsOut("TIME2" 		, rs.getString("BEDTIME"));			//就寝時間
				ret = svf.VrsOut("TIME3" 		, rs.getString("RISINGTIME"));		//起床時間
				ret = svf.VrsOut("SLEEP1" 		, rs.getString("SLEEPING_NAME"));	//睡眠状況
				ret = svf.VrsOut("BREAKFAST1" 	, rs.getString("BREAKFAST_NAME"));	//朝食状況
				ret = svf.VrsOut("TREATMENT" 	, rs.getString("NURSETREAT_NAME"));	//保健室での処置
				ret = svf.VrsOut("CONSULT1" 	, rs.getString("REMARK"));			//相談したい事
				ret = svf.VrEndRecord();
				nonedata = true;
			}
			db2.commit();
    	    log.debug("set_detail1 read ok!");
		} catch( Exception ex ) {
			log.error("set_detail1 read error!" ,ex);
		}

   	    //log.debug("set_detail1 path!");

	}  //set_detail1の括り



}  //クラスの括り
