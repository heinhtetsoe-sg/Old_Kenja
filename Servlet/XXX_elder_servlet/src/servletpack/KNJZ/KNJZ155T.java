package servletpack.KNJZ;

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

/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*
 *
 *	学校教育システム 賢者 [マスタ管理]
 *
 *					＜ＫＮＪＺ１５５＞  教科書マスタ一覧表
 *
 *<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*/

public class KNJZ155T extends HttpServlet {
	Vrw32alp svf = new Vrw32alp(); 	// PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
	DB2UDB db2;						// Databaseクラスを継承したクラス
	String dbname;
	boolean nonedata; 				// 該当データなしフラグ
	int ret;						// ＳＶＦ応答値
    private static final Log log = LogFactory.getLog(KNJZ155T.class);


	/*----------------------------*
	 * HTTP Get リクエストの処理  *
	 *----------------------------*/
	public void svf_out(HttpServletRequest request, HttpServletResponse response)
	                 throws ServletException, IOException
	{


	// パラメータの取得
	    String param[] = new String[3];
		try {
			dbname   = request.getParameter("DBNAME");      	// データベース名
	        param[0] = request.getParameter("YEAR");         	// 年度

		} catch( Exception ex ) {
			System.out.println("[KNJZ155T]parameter error!");
			System.out.println(ex);
		}


	// print設定
		PrintWriter out = new PrintWriter (response.getOutputStream());
		response.setContentType("application/pdf");
		OutputStream outstrm = response.getOutputStream();

	// svf設定
		ret = svf.VrInit();							//クラスの初期化
		ret = svf.VrSetSpoolFileStream(outstrm);   	//PDFファイル名の設定
		ret = svf.VrSetForm("KNJZ155T.frm", 4);	   	//SuperVisualFormadeで設計したレイアウト定義態の設定

	// ＤＢ接続
		db2 = new DB2UDB(dbname, "db2inst1", "db2inst1", DB2UDB.TYPE2);
		try {
			db2.open();
		} catch( Exception ex ) {
			System.out.println("[KNJZ155T]DB2 open error!");
		}

	//	登録数カウント
		try {
			String sql = new String();

			sql = "SELECT "
				+ "    COUNT(*) TOROKUSUU "
				+ "FROM "
				+ "    textbook_ydat t2 left join textbook_mst t1 on (t2.textbookcd=t1.textbookcd) "
				+ "WHERE "
				+ "    t2.year='" + param[0] + "' ";
log.debug(sql);

            System.out.println("[KNJZ155T]torokusuu sql="+sql);
            db2.query(sql);
			ResultSet rs = db2.getResultSet();
			System.out.println("[KNJZ155T]torokusuu sql ok!");

  	  	   /** 照会結果の取得とsvf_formへ出力 **/

			while( rs.next() ){

				param[1] = rs.getString("TOROKUSUU");			//登録数

			}
			db2.commit();
			System.out.println("[KNJZ155T]torokusuu read ok!");
		} catch( Exception ex ){
			System.out.println("[KNJZ155T]torokusuu read error!");
			System.out.println( ex );
		}

		for(int ia=0 ; ia<param.length ; ia++) System.out.println("[KNJZ155T]param[" + ia + "]=" + param[ia]);

	//	ＳＶＦ作成処理
		nonedata = false; 		// 該当データなしフラグ(MES001.frm出力用)

		set_detail(param);
log.debug("flg"+nonedata);

	//	該当データ無し
		if(nonedata == false){
			ret = svf.VrSetForm("MES001.frm", 0);
			ret = svf.VrsOut("note" , "note");
			ret = svf.VrEndRecord();
			ret = svf.VrEndPage();
		}

	// 終了処理
		ret = svf.VrQuit();
		db2.close();		// DBを閉じる
		outstrm.close();	// ストリームを閉じる 


	}//svf_outの括り


	/*----------------------------*
	 * ＳＶＦ出力
	 *----------------------------*/
	public void set_detail(String param[])
	{
		try {
			String sql = new String();
log.debug("kita"+param[1]);
			sql = "SELECT "
				+ "    t2.textbookcd, "
				+ "    t1.textbookname, "
				+ "    t1.textbookdiv, "
				+ "    t4.name1, "
				+ "    t1.textbookunitprice, "
				+ "    t3.issuecompanyname, "
				+ "    t1.remark "
				+ "FROM "
				+ "    textbook_ydat t2 left join textbook_mst t1 on t2.textbookcd=t1.textbookcd "
				+ "    left join issuecompany_mst t3 on t1.issuecompanycd = t3.issuecompanycd "
				+ "    left join v_name_mst t4 on t1.textbookdiv = t4.namecd2 and t4.year = '"+param[0]+"' and t4.namecd1 = 'M004'"
				+ "WHERE "
				+ "    t2.year='" + param[0] + "' "
				+ "ORDER BY "
				+ "    1 ";
log.debug(sql);
//            System.out.println("[KNJZ155T]set_detail sql="+sql);
            db2.query(sql);
log.debug("resultstart");
			ResultSet rs = db2.getResultSet();
log.debug("resultend");
//			System.out.println("[KNJZ155T]set_detail sql ok!");
  	  	   /** 照会結果の取得とsvf_formへ出力 **/

			String textname = new String();	//書籍名
			String note = new String();	//備考
			String fieldname = new String();		//svfフィールド名称の区分
			byte SendA[] = new byte[40];
			byte SendB[] = new byte[40];

			while( rs.next() ){
				textname = rs.getString("TEXTBOOKNAME");
				note = rs.getString("REMARK");
				fieldname = "1";
				if(textname != null){
					SendA = textname.getBytes();
					if(SendA.length > 30)	fieldname = "2";
				}
				if(note != null){
					SendB = note.getBytes();
					if(SendB.length > 20)	fieldname = "2";
				}
				ret = svf.VrsOut("NENDO"	, nao_package.KenjaProperties.gengou(Integer.parseInt(param[0])) + "年度");
				ret = svf.VrsOut("REGISTRATION" , param[1]);		//登録数
				ret = svf.VrsOut("TEXTCD"		+ fieldname , rs.getString("TEXTBOOKCD"));			//教科書番号
				ret = svf.VrsOut("TEXTNAME"  	+ fieldname	, rs.getString("TEXTBOOKNAME"));		//書籍名
				ret = svf.VrsOut("TEXTDIV"  	+ fieldname	, rs.getString("TEXTBOOKDIV"));			//区分
				ret = svf.VrsOut("DIVNAME"  	+ fieldname	, rs.getString("NAME1"));				//区分名称
				ret = svf.VrsOut("TEXTUNITPRICE"+ fieldname	, rs.getString("TEXTBOOKUNITPRICE"));	//単価
				ret = svf.VrsOut("TRADERNAME"  	+ fieldname	, rs.getString("ISSUECOMPANYNAME"));	//発行社
				ret = svf.VrsOut("NOTE"  		+ fieldname	, rs.getString("REMARK"));				//備考
				ret = svf.VrEndRecord();
				nonedata = true; //該当データなしフラグ
			}

			db2.commit();
			System.out.println("[KNJZ155T]set_detail read ok!");
		} catch( Exception ex ) {
			log.error("Set_Detail_1 read error!");
		}

	}//set_detailの括り


}//クラスの括り
