package servletpack.KNJB;

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
 *	学校教育システム 賢者 [時間割管理]
 *
 *					＜ＫＮＪＢ００４５Ｐ＞  時間割チェックリスト（印刷）
 *	１ページ：４６行
 *
 *	2004/11/16 nakamoto 新規作成
 *
 *	2004/11/19 nakamoto LIST1を見出しに印字する。さらにLIST1で改ページする。基本の場合は印字しない。
 *	2004/12/09 nakamoto 大項目に連番を付ける
 *	                    メッセージタイプ3,4が次ページに続けてまたがる場合、大項目（2）を表示
 *	2004/12/10 nakamoto エラー件数を見出しに印字する
 *<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*/

public class KNJB0045P {

	/* ロギング */
	private static final Log log = LogFactory.getLog(KNJB0045P.class);

	/*----------------------------*
	 * HTTP Get リクエストの処理  *
	 *----------------------------*/
	public void svf_out(HttpServletRequest request, HttpServletResponse response)
	                 throws ServletException, IOException
	{
		Vrw32alp svf = new Vrw32alp(); 			//PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
		DB2UDB db2 = null;						//Databaseクラスを継承したクラス
		String param[] = new String[2];

	//	パラメータ（エラー件数）04/12/10Add
		param[1] = request.getParameter("ERR_CNT");

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
			log.warn("[KNJB0045P]DB2 open error!");
		}

	//	ＳＶＦ作成処理
		PreparedStatement ps1 = null;
		PreparedStatement ps2 = null;
		PreparedStatement ps3 = null;
		PreparedStatement ps4 = null;
		boolean nonedata = false; 								//該当データなしフラグ
		Set_Head(db2,svf,param);								//フォーム設定のメソッド
		//SQL作成
		try {
			ps1 = db2.prepareStatement(Pre_Stat1());		//講座チェックリストヘッダデータ
			ps2 = db2.prepareStatement(Pre_Stat2());		//職員マスタ
			ps3 = db2.prepareStatement(Pre_Stat3());		//時間割パターンヘッダデータ
			ps4 = db2.prepareStatement(Pre_Stat4());		//講座チェックリストデータ
		} catch( Exception ex ) {
			log.warn("db2.prepareStatement error!");
		}
		//SVF出力
		if (Set_Detail_1(db2,svf,ps1,ps2,ps3,param)) nonedata = true;	//見出し出力のメソッド
		if (nonedata) {
			Set_Detail_4(db2,svf,ps4,param);							//メッセージ出力のメソッド
		}
		log.debug("nonedata="+nonedata);

	//	該当データ無し
		if( !nonedata ){
			ret = svf.VrSetForm("MES001.frm", 0);
			ret = svf.VrsOut("note" , "note");
			ret = svf.VrEndPage();
		}

	// 	終了処理
		ret = svf.VrQuit();
		Pre_Stat_f(ps1,ps2,ps3,ps4);//preparestatementを閉じる
		db2.commit();
		db2.close();				//DBを閉じる
		outstrm.close();			//ストリームを閉じる 

    }//doGetの括り



	/** フォーム設定 **/
	private void Set_Head(DB2UDB db2,Vrw32alp svf,String param[]){

		int ret = 0;
		ret = svf.VrSetForm("KNJB0045.frm", 4);

	//	ＳＶＦ属性変更--->改ページ 04/11/19Add
		ret = svf.VrAttribute("LIST1","FF=1");

	//	パラメータ（エラー件数）04/12/10Add
		ret = svf.VrsOut("ERRCNT",param[1] + "件");

	}//Set_Head()の括り



	/** 見出し出力のメソッド **/
	private boolean Set_Detail_1(
		DB2UDB db2,
		Vrw32alp svf,
		PreparedStatement ps1,
		PreparedStatement ps2,
		PreparedStatement ps3,
		String param[]
	) {
		boolean nonedata = false;
		int ret = 0;

		try {
			ResultSet rs = ps1.executeQuery();

			while( rs.next() ){
				param[0] = rs.getString("RADIO");					//時間割種別 04/11/19Add Set_Detail_4で使用！
				Set_Detail_1_1(svf, rs);							//年度等出力のメソッド
				Set_Detail_2(db2,svf,ps2, rs.getString("STAFFCD"));	//作成者出力のメソッド
				Set_Detail_3(db2,svf,ps3, rs);						//サブタイトル出力のメソッド
				nonedata = true;
			}
			rs.close();
		} catch( Exception ex ) {
			log.warn("Set_Detail_1 error!");
		}
		return nonedata;

	}//boolean Set_Detail_1()の括り



	/** 年度等出力 **/
	private void Set_Detail_1_1(Vrw32alp svf,ResultSet rs) {

		try {
			int ret = 0;
			ret = svf.VrsOut("NENDO"	,nao_package.KenjaProperties.gengou(Integer.parseInt(rs.getString("YEAR")))+"年度");
			ret = svf.VrsOut("SEMESTER"	,rs.getString("SEMESTERNAME"));							//学期
			ret = svf.VrsOut("OPERATION",rs.getString("OPERATION"));							//教師稼動数
			ret = svf.VrsOut("DATE"		,KNJ_EditDate.h_format_JP(rs.getString("CREATEDATE")));	//作成日
			if (rs.getString("RADIO").equals("1")) ret = svf.VrsOut("TITLE1","基本時間割チェックリスト");
			if (rs.getString("RADIO").equals("2")) ret = svf.VrsOut("TITLE1","通常時間割チェックリスト");
		} catch( Exception ex ){
			log.warn("Set_Detail_1_1 error!");
		}

	}//Set_Detail_1_1()の括り



	/** 作成者出力のメソッド **/
	private void Set_Detail_2(
		DB2UDB db2,
		Vrw32alp svf,
		PreparedStatement ps2,
		String staffcd
	) {
		int ret = 0;
		try {
			int pp = 0;
			ps2.setString(++pp,staffcd);			//職員コード
			ResultSet rs = ps2.executeQuery();

			while( rs.next() ){
				ret = svf.VrsOut("STAFFNAME",rs.getString("STAFFNAME"));		//作成者
			}
			rs.close();
		} catch( Exception ex ) {
			log.warn("Set_Detail_2 error!");
		}

	}//Set_Detail_2()の括り



	/** サブタイトル出力のメソッド **/
	private void Set_Detail_3(
		DB2UDB db2,
		Vrw32alp svf,
		PreparedStatement ps3,
		ResultSet rs1
	) {
		int ret = 0;
		try {
			if (rs1.getString("RADIO").equals("1")) {//基本
				int bscseq = rs1.getInt("BSCSEQ");
				int pp = 0;
				ps3.setString(++pp,rs1.getString("YEAR"));		//年度
				ps3.setString(++pp,rs1.getString("SEMESTER"));	//学期
				ps3.setInt(++pp,bscseq);						//ＳＥＱ
				ResultSet rs = ps3.executeQuery();

				while( rs.next() ){
					ret = svf.VrsOut("TITLE2","（SEQ"+String.valueOf(bscseq)+"："+rs.getString("TITLE")+"）");	//サブタイトル
				}
				rs.close();
			}
			if (rs1.getString("RADIO").equals("2")) {//通常
				String sdate = KNJ_EditDate.h_format_JP(rs1.getString("DATE_FROM"));
				String edate = KNJ_EditDate.h_format_JP(rs1.getString("DATE_TO"));
				ret = svf.VrsOut("TITLE2","（"+sdate+" \uFF5E "+edate+"）");	//サブタイトル
			}
		} catch( Exception ex ) {
			log.warn("Set_Detail_2 error!");
		}

	}//Set_Detail_3()の括り



	/** 作成者出力のメソッド **/
	private void Set_Detail_4(
		DB2UDB db2,
		Vrw32alp svf,
		PreparedStatement ps4,
		String param[]
	) {
		int ret = 0;
		try {
			ResultSet rs = ps4.executeQuery();

			int renban = 0;				//大項目に連番を付ける 04/12/09Add
			int gyocnt = 1;				//行数カウント用 04/12/09Add
			String list2name = "";		//大項目保管用	 04/12/09Add
			while( rs.next() ){
				int list = rs.getInt("MSGTYPE");//メッセージタイプ(1,2,3,4)
				//メッセージタイプ3,4が次ページに続けてまたがる場合、大項目（2）を表示 04/12/09Add
				if (gyocnt > 46) {
					gyocnt = 1;
					if (list > 2) {
						ret = svf.VrsOut("LIST2"	,list2name + "(続き)");
						ret = svf.VrEndRecord();
						gyocnt++;
					}
				}
				//メッセージタイプが1,2ならMSG、3,4ならITEM,MSGを印字する
				//メッセージタイプ1は、通常時間割(param[0]=2)の場合のみ印字する
				if (list == 1) {
					if (param[0].equals("2")) 
						ret = svf.VrsOut("LIST"+String.valueOf(list)	,rs.getString("MSG"));
				} else if (list == 2) {	
					list2name = String.valueOf(++renban)+"　"+rs.getString("MSG");	// 04/12/09Add
					ret = svf.VrsOut("LIST"+String.valueOf(list)	,list2name);	// 04/12/09Modify
				} else {
					ret = svf.VrsOut("LIST"+String.valueOf(list)	,rs.getString("ITEM")+"　"+rs.getString("MSG"));
				}

				ret = svf.VrEndRecord();
				if (list > 1) gyocnt++;	// 04/12/09Add
			}
			rs.close();
		} catch( Exception ex ) {
			log.warn("Set_Detail_4 error!");
		}

	}//Set_Detail_4()の括り



	/* 講座チェックリストヘッダデータ
	 * 抽出条件：動作状態='OK'
	 * 学期マスタより学期名を取得
	 */
	private String Pre_Stat1()
	{
		StringBuffer stb = new StringBuffer();
		try {
            stb.append("SELECT w1.status, w1.radio, w1.year, w1.semester, w1.bscseq, w1.date_from, w1.date_to, ");
            stb.append("       w1.operation, w1.registercd staffcd, date(w1.updated) createdate, w2.semestername ");
            stb.append("FROM   sch_checklist_hdat w1, semester_mst w2 ");
            stb.append("WHERE  w1.status='OK' AND w1.year=w2.year AND w1.semester=w2.semester ");
		} catch( Exception e ){
			log.warn("Pre_Stat1 error!");
		}
		return stb.toString();

	}//Pre_Stat1()の括り


	/* 職員マスタ */
	private String Pre_Stat2()
	{
		StringBuffer stb = new StringBuffer();
	//	職員コードをパラメータとする
		try {
            stb.append("SELECT staffcd, staffname ");
            stb.append("FROM   staff_mst ");
            stb.append("WHERE  staffcd=? ");
		} catch( Exception e ){
			log.warn("Pre_Stat2 error!");
		}
		return stb.toString();

	}//Pre_Stat2()の括り


	/* 時間割パターンヘッダデータ */
	private String Pre_Stat3()
	{
		StringBuffer stb = new StringBuffer();
	//	年度・学期・ＳＥＱ（パターン）をパラメータとする
		try {
            stb.append("SELECT year, semester, bscseq, title ");
            stb.append("FROM   sch_ptrn_hdat ");
            stb.append("WHERE  year=? AND semester=? AND bscseq=? ");
		} catch( Exception e ){
			log.warn("Pre_Stat3 error!");
		}
		return stb.toString();

	}//Pre_Stat3()の括り


	/* 講座チェックリストデータ */
	private String Pre_Stat4()
	{
		StringBuffer stb = new StringBuffer();
	//	KEY1,KEY2,KEY3,KEY4でソートする
		try {
            stb.append("SELECT value(key1,0) key1, value(key2,0) key2, ");
            stb.append("       value(key3,0) key3, value(key4,0) key4, ");
            stb.append("       msgtype, item, msg ");
            stb.append("FROM   sch_checklist_dat ");
            stb.append("ORDER BY key1,key2,key3,key4 ");
		} catch( Exception e ){
			log.warn("Pre_Stat4 error!");
		}
		return stb.toString();

	}//Pre_Stat4()の括り


	/**PrepareStatement close**/
	private void Pre_Stat_f(PreparedStatement ps1,PreparedStatement ps2,PreparedStatement ps3,PreparedStatement ps4)
	{
		try {
			ps1.close();
			ps2.close();
			ps3.close();
			ps4.close();
		} catch( Exception e ){
			log.warn("Preparedstatement-close error!");
		}
	}//Pre_Stat_f()の括り



}//クラスの括り
