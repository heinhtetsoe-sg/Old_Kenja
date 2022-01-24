package servletpack.KNJG;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import nao_package.svf.*;
import nao_package.db.*;
import java.sql.ResultSet;
import java.util.StringTokenizer;
import servletpack.KNJZ.detail.*;

/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*
 *
 *	学校教育システム 賢者 [事務管理]
 *
 *			＜ＫＮＪＧ０４０＞ 職員許可願い
 *
 *<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*/

public class KNJG040 extends HttpServlet {
	Vrw32alp svf = new Vrw32alp(); 	// PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
	DB2UDB db2;						// Databaseクラスを継承したクラス
	boolean nonedata; 				// 該当データなしフラグ
	int ret;						// ＳＶＦ応答値


	/*----------------------------*
	 * HTTP Get リクエストの処理  *
	 *----------------------------*/
	public void svf_out(HttpServletRequest request, HttpServletResponse response)
	                 throws ServletException, IOException
	{
/*
			0	APPLYDAY 		2003/01/06 
			1	APPLYCD 		1 
			2	STAFFCD 		999999 
			3	SDATE 			2003/01/07 
			3	HOUR1 			08 
			3	MINUTE1 		30 
			4	FDATE 			2003/01/08 
			4	HOUR2 			17 
			4	MINUTE2 		00 
*/

	// パラメータの取得
		String dbname = request.getParameter("DBNAME");      							//データベース名
	    String param[] = new String[6];
		try {
	        param[0] = request.getParameter("APPLYDAY");								//申請日
	        param[1] = request.getParameter("APPLYCD");     							//申請区分
	        param[2] = request.getParameter("STAFFCD");     							//職員コード
	        param[3] = request.getParameter("SDATE");									//期間開始
	        param[4] = request.getParameter("EDATE");									//期間終了
			param[5] = request.getParameter("NENDO");   	  							//処理年度
		} catch( Exception ex ) {
			System.out.println("[KNJG040]parameter error!");
			System.out.println(ex);
		}

	//	print設定
		PrintWriter out = new PrintWriter (response.getOutputStream());
		response.setContentType("application/pdf");
		OutputStream outstrm = response.getOutputStream();

	//	svf設定
		ret = svf.VrInit();							//クラスの初期化
		ret = svf.VrSetSpoolFileStream(outstrm);   	//PDFファイル名の設定

	//	ＤＢ接続
		db2 = new DB2UDB(dbname, "db2inst1", "db2inst1", DB2UDB.TYPE2);
		try {
			db2.open();
		} catch( Exception ex ) {
			System.out.println("[KNJG040]DB2 open error!");
		}

		for(int ia=0 ; ia<param.length ; ia++) System.out.println("[KNJG040]param[" + ia + "]=" + param[ia]);


	//	ＳＶＦ作成処理
		nonedata = false;
		if( param[1].equals("1") )	ret = svf.VrSetForm("KNJG040_1.frm", 1);	//年次休暇届
		if( param[1].equals("2") )	ret = svf.VrSetForm("KNJG040_2.frm", 1);	//病気休暇届
		if( param[1].equals("3") )	ret = svf.VrSetForm("KNJG040_3.frm", 1);	//出張許可願
		if( param[1].equals("4") )	ret = svf.VrSetForm("KNJG040_4.frm", 1);	//出張許可願(職専免)
		if( param[1].equals("5") )	ret = svf.VrSetForm("KNJG040_5.frm", 1);	//私事旅行（研修等）届
		set_detail1(param);


	//	該当データ無し
		if( !nonedata ){
			ret = svf.VrSetForm("MES001.frm", 0);
			ret = svf.VrsOut("note" , "note");
			ret = svf.VrEndPage();
		}

	//	終了処理
		db2.commit();
		db2.close();		// DBを閉じる
		ret = svf.VrQuit();
		outstrm.close();	// ストリームを閉じる 

	}//svf_outの括り



	/*-------------*
	 * ＳＶＦ出力  *
	 *-------------*/
	public void set_detail1(String param[])	{

	//	学校情報取得
		try {
			KNJ_Schoolinfo schoolinfo = new KNJ_Schoolinfo(param[5]);
			KNJ_Schoolinfo.ReturnVal returnval = schoolinfo.get_info(db2);
			ret = svf.VrsOut("schoolname1" 	, returnval.SCHOOL_NAME1);			//学校名
			ret = svf.VrsOut("schoolname2" 	, returnval.SCHOOL_NAME2);			//学校名
			ret = svf.VrsOut("post" 		, returnval.PRINCIPAL_JOBNAME);		//役職
			ret = svf.VrsOut("staff1" 		, returnval.PRINCIPAL_NAME);		//校長名
			System.out.println("[KNJG040]schoolinfo get ok!");
		} catch( Exception e ){
			System.out.println("[KNJG040]schoolinfo get error!");
			System.out.println( e );
		}

	//	許可願い出力
		boolean nonedata2 = false;
		int appcd = Integer.parseInt(param[1]);	//申請区分
		
		try {
			String sql = new String();
			sql = "SELECT "
					+ "T1.APPLYDAY,"
					+ "SUBSTR(CHAR(T1.SDATE),1,10) AS SDATE_DAY,"
					+ "SUBSTR(CHAR(T1.SDATE),12,2) AS SDATE_HH,"
					+ "SUBSTR(CHAR(T1.SDATE),15,2) AS SDATE_MM,"
					+ "SUBSTR(CHAR(T1.EDATE),1,10) AS FDATE_DAY,"
					+ "SUBSTR(CHAR(T1.EDATE),12,2) AS FDATE_HH,"
					+ "SUBSTR(CHAR(T1.EDATE),15,2) AS FDATE_MM,"
					+ "VALUE(T1.HOURS,'00') AS HOURS,"
					+ "VALUE(T1.MINUTES,'00') AS MINUTES,"
					+ "T1.VACATION,"
					+ "T1.VACATIONREASON,"
					+ "T1.GUIDE,"
					+ "T1.GUIDE_NUM,"
					+ "T1.BUSINESSTRIP,"
					+ "T1.REMARK,"
					+ "T1.CALL_NAME,"
					+ "T1.CALL_TELNO,"
					+ "T2.STAFFNAME,"
					+ "T2.SECTIONNAME,"
					+ "T2.JOBNAME "
				+ "FROM "
					+ "PERMREQUEST_DAT T1 "
					+ "INNER JOIN ("
						+ "SELECT "
							+ "ST1.STAFFCD,"
							+ "ST1.STAFFNAME,"
							+ "ST2.SECTIONNAME,"
							+ "ST3.JOBNAME "
						+ "FROM "
							+ "V_STAFF_MST ST1 "
							+ "LEFT JOIN SECTION_MST ST2 ON ST1.SECTIONCD = ST2.SECTIONCD "
							+ "LEFT JOIN JOB_MST ST3 ON ST1.JOBCD = ST3.JOBCD "
						+ "WHERE "
								+ "ST1.YEAR = '" +  param[5] + "' "
							+ "AND ST1.STAFFCD = '" +  param[2] + "' "
					+ ")T2 ON  T2.STAFFCD = T1.STAFFCD "
				
				+ "WHERE "
						+ "T1.APPLYDAY = '" +  param[0] + "' "
					+ "AND T1.APPLYCD = '" +  param[1] + "' "
					+ "AND T1.STAFFCD = '" +  param[2] + "' "
					+ "AND T1.SDATE = '" +  param[3] + "' "
					+ "AND T1.EDATE = '" +  param[4] + "'";

            //System.out.println("[KNJG040]set_detai1 sql="+sql);
            db2.query(sql);
			ResultSet rs = db2.getResultSet();
			System.out.println("[KNJG040]set_detail1 sql ok!");

			if( rs.next() ){
				//全帳票共通
				ret = svf.VrsOut("ymd",KNJ_EditDate.h_format_JP(rs.getString("APPLYDAY")));			//申請日
				ret = svf.VrsOut("belong",rs.getString("SECTIONNAME"));				//所属
				ret = svf.VrsOut("position",rs.getString("JOBNAME"));				//職名
				ret = svf.VrsOut("staff2",rs.getString("STAFFNAME"));				//氏名

				ret = svf.VrsOut("date1",KNJ_EditDate.h_format_JP_MD(rs.getString("SDATE_DAY")));	//開始日付
				ret = svf.VrsOut("week1",KNJ_EditDate.h_format_W(rs.getString("SDATE_DAY"))+"曜日");//開始曜日
				ret = svf.VrsOut("date2",KNJ_EditDate.h_format_JP_MD(rs.getString("FDATE_DAY")));	//終了日付
				ret = svf.VrsOut("week2",KNJ_EditDate.h_format_W(rs.getString("FDATE_DAY"))+"曜日");//終了曜日
				//旅行届以外共通
				if( appcd!=5 ){
					ret = svf.VrsOut("hour1",rs.getString("SDATE_HH")+"時");		//開始時
					ret = svf.VrsOut("minute1",rs.getString("SDATE_MM")+"分");		//開始分
					ret = svf.VrsOut("hour2",rs.getString("FDATE_HH")+"時");		//終了時
					ret = svf.VrsOut("minute2",rs.getString("FDATE_MM")+"分");		//終了分
					//所要時間の編集と出力
					StringBuffer sbx = new StringBuffer();
					String strx = new String();
					try{
						strx = String.valueOf(Integer.parseInt(rs.getString("HOURS")));
					} catch( Exception ex ) {
						strx = "  ";
					}
					sbx.append(strx);
					sbx.append("時間");
					strx = " ";
					if( rs.getString("MINUTES")!=null )		strx = rs.getString("MINUTES");
					sbx.append(strx);
					sbx.append("分");
					ret = svf.VrsOut("time",sbx.toString());							//時間：時
				}
				//全帳票共通-->休暇届の休暇地　出張願の目的地　旅行届の旅行先
				KNJ_EditSvf esvf = new KNJ_EditSvf(svf);
				esvf.set_toknizer(rs.getString("VACATION"),"destination",5);			//目的地
				//休暇届・旅行届のみ
				if( appcd!=3 )
					esvf.set_toknizer(rs.getString("VACATIONREASON"),"purpose",5);		//理由
				//出張許可願
				if( appcd==3 || appcd==4 ){
					esvf.set_toknizer(rs.getString("GUIDE"),"lead",3);					//用件：引率
					ret = svf.VrsOut("student1", rs.getString("GUIDE_NUM"));			//生徒数
					esvf.set_toknizer(rs.getString("BUSINESSTRIP"),"official_trip",3);	//用件：出張
					esvf.set_toknizer(rs.getString("REMARK"),"note",3);					//備考
				}
				//私事旅行（研修等）届のみ
				if( appcd==5 ){
					ret = svf.VrsOut("connect1",rs.getString("CALL_NAME"));				//連絡先
					ret = svf.VrsOut("phone",rs.getString("CALL_TELNO"));				//連絡先電話
				}
				ret = svf.VrEndPage();
				nonedata = true;
			}
			db2.commit();
    	    //System.out.println("[KNJG040]set_detail1 read ok!");
		} catch( Exception ex ) {
			System.out.println("[KNJG040]set_detail1 read error!");
			System.out.println(ex);
		}

	}//set_detail1の括り


}//クラスの括り
