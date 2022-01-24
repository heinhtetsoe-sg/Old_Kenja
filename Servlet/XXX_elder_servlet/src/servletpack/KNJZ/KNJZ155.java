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
import java.sql.PreparedStatement;

/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*
 *
 *	学校教育システム 賢者 [マスタ管理]
 *
 *					＜ＫＮＪＺ１５５＞  教科書マスタ一覧表
 *
 *	2006/07/12 m-yama   NO001 東京都をベースに修正(ほぼ新規)
 *<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*/

public class KNJZ155 extends HttpServlet {
	Vrw32alp svf = new Vrw32alp(); 	// PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
	DB2UDB db2;						// Databaseクラスを継承したクラス
	String dbname;
	boolean nonedata; 				// 該当データなしフラグ
	int ret;						// ＳＶＦ応答値
    private static final Log log = LogFactory.getLog(KNJZ155.class);


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
			log.error("[KNJZ155]parameter error!");
			log.error(ex);
		}


	// print設定
		PrintWriter out = new PrintWriter (response.getOutputStream());
		response.setContentType("application/pdf");
		OutputStream outstrm = response.getOutputStream();

	// svf設定
		ret = svf.VrInit();							//クラスの初期化
		ret = svf.VrSetSpoolFileStream(outstrm);   	//PDFファイル名の設定
		ret = svf.VrSetForm("KNJZ155.frm", 4);	   	//SuperVisualFormadeで設計したレイアウト定義態の設定

	// ＤＢ接続
		db2 = new DB2UDB(dbname, "db2inst1", "db2inst1", DB2UDB.TYPE2);
		try {
			db2.open();
		} catch( Exception ex ) {
			log.error("[KNJZ155]DB2 open error!");
		}

	//	登録数カウント
		getDataCount(param);

		for(int ia=0 ; ia<param.length ; ia++) log.debug("[KNJZ155]param[" + ia + "]=" + param[ia]);

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

	/**
	 * 登録数取得
	 */
	public void getDataCount(String param[])
	{
		try {
			String sql = new String();

			sql = "SELECT COUNT(*) TOROKUSUU FROM textbook_ydat WHERE year='" + param[0] + "' ";

            log.debug("[KNJZ155]torokusuu sql="+sql);
            db2.query(sql);
			ResultSet rs = db2.getResultSet();

			while( rs.next() ){

				param[1] = rs.getString("TOROKUSUU");

			}
			db2.commit();
			log.debug("[KNJZ155]torokusuu read ok!");
		} catch( Exception ex ){
			log.error("[KNJZ155]torokusuu read error!");
			log.error( ex );
		}
	}

	/*----------------------------*
	 * ＳＶＦ出力
	 *----------------------------*/
	public void set_detail(String param[])
	{
		PreparedStatement ps = null;
		try {
			ps = db2.prepareStatement(meisaiSql(param));

			ResultSet rs = ps.executeQuery();

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
				ret = svf.VrsOut("TRADERNAME"  	+ fieldname	, rs.getString("CONTRACTORNAME"));		//業者
				ret = svf.VrsOut("NOTE"  		+ fieldname	, rs.getString("REMARK"));				//備考
				ret = svf.VrEndRecord();
				nonedata = true; //該当データなしフラグ
			}

			db2.commit();
			log.debug("[KNJZ155]set_detail read ok!");
		} catch( Exception ex ) {
			log.error("Set_Detail_1 read error!");
		}

	}//set_detailの括り

	/**
	 *	頁数を抽出
	 *
	 */
	private String meisaiSql(String param[])
	{
		StringBuffer stb = new StringBuffer();
		try {
			stb.append("SELECT ");
			stb.append("    t2.TEXTBOOKCD, ");
			stb.append("    t1.TEXTBOOKNAME, ");
			stb.append("    t1.TEXTBOOKDIV, ");
			stb.append("    t4.NAME1, ");
			stb.append("    t1.TEXTBOOKUNITPRICE, ");
			stb.append("    t1.CONTRACTORNAME, ");
			stb.append("    t1.REMARK ");
			stb.append("FROM ");
			stb.append("    TEXTBOOK_YDAT T2 LEFT JOIN TEXTBOOK_MST T1 ON T2.TEXTBOOKCD=T1.TEXTBOOKCD ");
			stb.append("    LEFT JOIN V_NAME_MST T4 ON T1.TEXTBOOKDIV = T4.NAMECD2 AND T4.YEAR = '"+param[0]+"' AND T4.NAMECD1 = 'M004' ");
			stb.append("WHERE ");
			stb.append("    t2.YEAR='" + param[0] + "' ");
			stb.append("ORDER BY ");
			stb.append("    t2.TEXTBOOKCD ");

//log.debug(stb);
		} catch( Exception e ){
			log.warn("meisaiSql error!",e);
		}
		return stb.toString();

	}//meisaiSql()の括り

}//クラスの括り
