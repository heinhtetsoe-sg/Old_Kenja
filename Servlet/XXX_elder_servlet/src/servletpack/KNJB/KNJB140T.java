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
 *					＜ＫＮＪＢ１４０Ｔ＞  教科書購入者一覧(東京都)
 *
 * 2006/06/09 m-yama   NO001 クラス名をKNJB140→KNJB140Tに変更
 * 2006/06/27 m-yama   NO002 年・組・番をHR_NAME＋ATTENDNOに変更
 *<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*/

public class KNJB140T extends HttpServlet {
	Vrw32alp svf = new Vrw32alp(); 	// PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
	DB2UDB db2;						// Databaseクラスを継承したクラス
	String dbname;
	boolean nonedata; 				// 該当データなしフラグ
	int ret;						// ＳＶＦ応答値

    private static final Log log = LogFactory.getLog(KNJB140T.class);

	/*----------------------------*
	 * HTTP Get リクエストの処理  *
	 *----------------------------*/
	public void svf_out(HttpServletRequest request, HttpServletResponse response)
	                 throws ServletException, IOException
	{


	// パラメータの取得
	    String param[] = new String[3];
		String textno[] = request.getParameterValues("CLASS_SELECTED");	//教科書データ

		try {
			dbname   = request.getParameter("DBNAME");      	// データベース名
	        param[0] = request.getParameter("YEAR");         	// 年度
	        param[1] = request.getParameter("GAKKI");         	// 学期

		} catch( Exception ex ) {
			System.out.println("parameter error!");
			System.out.println(ex);
		}

	// print設定
		PrintWriter out = new PrintWriter (response.getOutputStream());
		response.setContentType("application/pdf");
		OutputStream outstrm = response.getOutputStream();

	// svf設定
		ret = svf.VrInit();							//クラスの初期化
		ret = svf.VrSetSpoolFileStream(outstrm);   	//PDFファイル名の設定
		ret = svf.VrSetForm("KNJB140T.frm", 4);	   	//SuperVisualFormadeで設計したレイアウト定義態の設定

	// ＤＢ接続
		db2 = new DB2UDB(dbname, "db2inst1", "db2inst1", DB2UDB.TYPE2);
		try {
			db2.open();
		} catch( Exception ex ) {
			System.out.println("DB2 open error!");
		}

		for(int ia=0 ; ia<param.length ; ia++) System.out.println("param[" + ia + "]=" + param[ia]);

	//	ＳＶＦ作成処理
		PreparedStatement ps  = null;
		nonedata = false; 		// 該当データなしフラグ(MES001.frm出力用)
for(int i=0 ; i<3 ; i++) log.debug("param["+i+"]="+param[i]);
		//SQL作成
		try {
			ps  = db2.prepareStatement(preStat(param));
		} catch( Exception ex ) {
			log.error("DB2 prepareStatement set error!");
		}
		int i = 0;
		while(i < textno.length){
			set_detail(db2,svf,param,textno[i],ps);
			i++;
		}

	//	該当データ無し
		if(nonedata == false){
			ret = svf.VrSetForm("MES001.frm", 0);
			ret = svf.VrsOut("note" , "note");
			ret = svf.VrEndRecord();
			ret = svf.VrEndPage();
		}

	// 終了処理
		ret = svf.VrQuit();
		preStatClose(ps);	//preparestatementを閉じる
		db2.commit();
		db2.close();		// DBを閉じる
		outstrm.close();	// ストリームを閉じる 


	}//svf_outの括り

	/*----------------------------*
	 * ＳＶＦ出力
	 *----------------------------*/
	public void set_detail(
		DB2UDB db2,
		Vrw32alp svf,
		String param[],
		String textno,
		PreparedStatement ps
	)
	{
		try {
  	  	   /** 照会結果の取得とsvf_formへ出力 **/

			//年度
//			ret = svf.VrsOut("NENDO", nao_package.KenjaProperties.gengou(Integer.parseInt(param[0])) + "年度");
			ret = svf.VrsOut("NENDO1", param[0] + "年度");

			String schregno = "0";		//前レコードの学籍番号
			int totalprice = 0;			//合計件数
			int syouprice  = 0;			//小計件数
			int syoukei    = 0;			//小計出力判定
			int gyo        = 0;			//小計出力判定
			String bfdata  ;			//前回コード
			String afdata  ;			//現在コード
			String titlenm ;			//タイトル名
			String attendsp ;			//NO002
			boolean bookdata; 			// 該当データなしフラグ

			bfdata  = String.valueOf("0");
			afdata  = String.valueOf("0");

			ps.setString( 1, textno);
log.debug(textno);
			ResultSet rs = ps.executeQuery();
			bookdata = false;
			while( rs.next() ){
				
				bfdata = String.valueOf(afdata);
				afdata = rs.getString("chaircd");

				//講座が変われば、次の行
				if (!bfdata.equalsIgnoreCase(afdata) && gyo > 0) {
					syoukei = 1;
				}
				if (gyo == 48){
					gyo = 0;
				}
				//小計
				if (syoukei == 1){
					if (gyo < 48){
						for (int umeji = gyo ; umeji < 48 ; umeji++){
							ret = svf.VrAttribute("NAME1","Meido=100");
							ret = svf.VrsOut("NAME1" 	," . ");
							ret = svf.VrEndRecord();
//							gyo++;
						}
					}
					ret = svf.VrsOut("kei"	 	, String.valueOf("小計"));		//小計
					ret = svf.VrsOut("kensu" 	, String.valueOf(syouprice));	//小計金額
					syouprice = 0;
					ret = svf.VrEndRecord();
					//合計欄空行
					ret = svf.VrsOut("SPACE1" 	," . ");
					ret = svf.VrEndRecord();
					gyo = 0;
					syoukei = 0;
				}

				//ヘッダ
				ret = svf.VrsOut("TEXTCD1"	, rs.getString("TEXTBOOKCD"));					//教科書番号
				ret = svf.VrsOut("CHAIRCD" 	, rs.getString("CHAIRCD"));						//講座コード
				ret = svf.VrsOut("CHAIRNAME" 	, rs.getString("CHAIRNAME"));				//講座名
				ret = svf.VrsOut("TEXTNAME1", rs.getString("TEXTBOOKNAME"));				//教科書名
				ret = svf.VrsOut("ISSUECOMPANY1"		, rs.getString("ISSUECOMPANYNAME"));//発行社
				//詳細 NO002
//				titlenm  = String.valueOf(Integer.parseInt(rs.getString("GRADE")) + "年");
//				titlenm += rs.getString("HR_CLASS")+"組";
//				titlenm += String.valueOf(Integer.parseInt(rs.getString("ATTENDNO"))+"番");
				attendsp = String.valueOf("  " + rs.getInt("ATTENDNO"));
				titlenm  = rs.getString("HR_NAME") + attendsp.substring(attendsp.length()-2) +"番";
				ret = svf.VrsOut("class"	, String.valueOf(titlenm));			//組名称
				ret = svf.VrsOut("NAME1"		, rs.getString("NAME_SHOW"));					//生徒氏名
				gyo++;
				ret = svf.VrEndRecord();
				if (gyo == 48){
					gyo = 0;
				}
				syouprice++;
				totalprice++;
				nonedata = true; //該当データなしフラグ
				bookdata = true;
			}
			if(bookdata == true){
				if (gyo < 48){
					for (int lastume = gyo ; lastume < 48 ; lastume++){
						ret = svf.VrAttribute("NAME1","Meido=100");
						ret = svf.VrsOut("NAME1" 	," . ");
						ret = svf.VrEndRecord();
						gyo++;
					}
				}
				ret = svf.VrsOut("kei"	 	, String.valueOf("小計"));		//小計
				ret = svf.VrsOut("kensu" 	, String.valueOf(syouprice));	//小計件数
				ret = svf.VrEndRecord();
//				gyo++;
//				if (gyo == 50){
//					gyo = 0;
//				}
//				if (gyo == 0){
//					for (int totalume = 0 ; totalume < 47 ; totalume++){
//						ret = svf.VrAttribute("NAME1","Meido=100");
//						ret = svf.VrsOut("NAME1" 	," . ");
//						ret = svf.VrEndRecord();
//					}
//				}
				ret = svf.VrsOut("kei" 	, String.valueOf("合計"));			//合計
				ret = svf.VrsOut("kensu" 	, String.valueOf(totalprice));	//合計件数
				ret = svf.VrEndRecord();
//				gyo++;
			}
			db2.commit();
			System.out.println("set_detail read ok!");
		} catch( Exception ex ){
			System.out.println("set_detail read error!");
			System.out.println( ex );
		}

	}//set_detailの括り

	/**データ　取得**/
	private String preStat(String param[])
	{
		StringBuffer stb = new StringBuffer();
		try {
			stb.append("SELECT ");
			stb.append("    t2.textbookcd, ");
			stb.append("    t2.textbookname, ");
			stb.append("    t5.ISSUECOMPANYNAME, ");
			stb.append("    t4.chaircd, ");
			stb.append("    t4.chairname, ");
			stb.append("    t3.schregno, ");
			stb.append("    t7.GRADE, ");
			stb.append("    t7.HR_CLASS, ");
			stb.append("    t7.ATTENDNO, ");
			stb.append("    t6.NAME_SHOW, ");
			stb.append("    t8.HR_NAME ");	//NO002
			stb.append("FROM ");
			stb.append("    chair_textbook_dat t1, ");
			stb.append("    chair_std_dat t3, ");
			stb.append("    chair_dat t4, ");
			stb.append("    textbook_mst t2 ");
			stb.append("    left join ISSUECOMPANY_MST t5 on t2.ISSUECOMPANYCD = t5.ISSUECOMPANYCD, ");
			stb.append("    SCHREG_BASE_MST t6, ");
			stb.append("    SCHREG_REGD_DAT t7 ");
			//NO002↓
			stb.append("    left join SCHREG_REGD_HDAT t8 on t7.year = t8.year ");
			stb.append("              and t7.semester = t8.semester ");
			stb.append("              and t7.grade = t8.grade ");
			stb.append("              and t7.hr_class = t8.hr_class ");
			//NO002↑
			stb.append("WHERE ");
			stb.append("    t1.year = '"+param[0]+"' AND ");
			stb.append("    t1.semester = '"+param[1]+"' AND ");
			stb.append("    t1.textbookcd = ? AND ");
			stb.append("    t3.year = '"+param[0]+"' AND ");
			stb.append("    t3.semester = '"+param[1]+"' AND ");
			stb.append("    t1.chaircd = t3.chaircd AND ");
			stb.append("    t4.year = '"+param[0]+"' AND ");
			stb.append("    t4.semester = '"+param[1]+"' AND ");
			stb.append("    t1.chaircd = t4.chaircd AND ");
			stb.append("    t1.textbookcd = t2.textbookcd AND ");
			stb.append("    t3.SCHREGNO = t6.SCHREGNO AND ");
			stb.append("    t3.SCHREGNO = t7.SCHREGNO AND ");
			stb.append("    t7.year = '"+param[0]+"' AND ");
			stb.append("    t7.semester = '"+param[1]+"' ");
			stb.append("ORDER BY ");
			stb.append("    chaircd,GRADE,HR_CLASS,ATTENDNO ");
log.debug(stb);
		} catch( Exception ex ){
			log.error("preStat error!");
		}
		return stb.toString();

	}//preStat()の括り

	/**PrepareStatement close**/
	private void preStatClose(PreparedStatement ps)
	{
		try {
			ps.close();
		} catch( Exception e ){
			log.warn("preStatClose error!");
		}
	}//preStatClose()の括り

}//クラスの括り
