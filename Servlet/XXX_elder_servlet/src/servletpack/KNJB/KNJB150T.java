package servletpack.KNJB;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*
 *
 *	学校教育システム 賢者 [時間割管理]
 *
 *					＜ＫＮＪＢ１５０＞  教科書購入表（東京都）
 *
 * 2005/02/01 nakamoto 作成
 * 2006/06/09 m-yama   NO001 クラス名をKNJB150→KNJB150Tに変更
 *<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*/

public class KNJB150T extends HttpServlet {

    private static final Log log = LogFactory.getLog(KNJB150T.class);

    private Vrw32alp svf = new Vrw32alp(); 	// PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
	private DB2UDB db2;						// Databaseクラスを継承したクラス
	private boolean nonedata; 				// 該当データなしフラグ


	/*----------------------------*
	 * HTTP Get リクエストの処理  *
	 *----------------------------*/
	public void svf_out(HttpServletRequest request, HttpServletResponse response)
	                 throws ServletException, IOException
	{


	// パラメータの取得
	    String param[] = new String[3];
		String schno[] = request.getParameterValues("category_selected");	//生徒データ

		try {
	        param[0] = request.getParameter("YEAR");         	// 年度
	        param[1] = request.getParameter("GAKKI");         	// 学期
			param[2] = request.getParameter("GRADE_HR_CLASS");	// 年組

		} catch( Exception ex ) {
			log.error("parameter error!", ex);
		}


	// print設定
		response.setContentType("application/pdf");
		OutputStream outstrm = response.getOutputStream();

	// svf設定
		svf.VrInit();							//クラスの初期化
		svf.VrSetSpoolFileStream(outstrm);   	//PDFファイル名の設定
		svf.VrSetForm("KNJB150T.frm", 4);	   	//SuperVisualFormadeで設計したレイアウト定義態の設定
//		svf.VrAttribute("SCHREGNO","FF=1");	//改ページキー

	// ＤＢ接続
		db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
		try {
			db2.open();
		} catch( Exception ex ) {
			log.error("DB2 open error!", ex);
		}


		for(int ia=0 ; ia<param.length ; ia++) log.debug("param[" + ia + "]=" + param[ia]);

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
		while(i < schno.length){
			set_detail(db2,svf,param,schno[i],ps);
			i++;
log.debug("flg"+nonedata);
		}

	//	該当データ無し
		if(nonedata == false){
			svf.VrSetForm("MES001.frm", 0);
			svf.VrsOut("note" , "note");
			svf.VrEndRecord();
			svf.VrEndPage();
		}

	// 終了処理
		svf.VrQuit();
		DbUtils.closeQuietly(ps);
		db2.commit();
		db2.close();		// DBを閉じる
		outstrm.close();	// ストリームを閉じる 


	}//svf_outの括り

    
    private static int byteCountMS932(final String str) {
        int count = 0;
        if (null != str) {
            try {
                count = str.getBytes("MS932").length;
            } catch (Exception e) {
                log.error("EncodingException!", e);
                count = str.length();
            }
        }
        return count;
    }

	/*----------------------------*
	 * ＳＶＦ出力
	 *----------------------------*/
	public void set_detail(
		DB2UDB db2,
		Vrw32alp svf,
		String param[],
		String schno,
		PreparedStatement ps
	)
	{
		try {
  	  	   /** 照会結果の取得とsvf_formへ出力 **/

			//年度
//			svf.VrsOut("NENDO", nao_package.KenjaProperties.gengou(Integer.parseInt(param[0])) + "年度");
			svf.VrsOut("NENDO1", param[0] + "年度");

			int totalprice = 0;			//合計金額
			int syouprice  = 0;			//小計金額
			int syoukei    = 0;			//小計出力判定
			int gyo        = 0;			//小計出力判定
			String bfdata  ;			//前回コード
			String afdata  ;			//現在コード
			String titlenm ;			//タイトル名
			boolean bookdata; 			// 該当データなしフラグ

			bfdata  = String.valueOf("0");
			afdata  = String.valueOf("0");

			ps.setString( 1, schno);
			ps.setString( 2, schno);
			ps.setString( 3, schno);
log.debug(schno);
			ResultSet rs = ps.executeQuery();
			bookdata = false;
			while( rs.next() ){
				
log.debug("rsstart");

				bfdata = String.valueOf(afdata);
				afdata = rs.getString("TEXTBOOKDIV");
log.debug("rsstart2");
				//教科書区分が変われば、次の行
				if (!bfdata.equalsIgnoreCase(afdata) && gyo > 0) {
					syoukei = 1;
				}
				if (gyo == 50){
					gyo = 0;
				}
log.debug("rsstart3");
				//ヘッダ
				svf.VrsOut("HR_NAME"	, rs.getString("HR_NAME"));							//組名称
				svf.VrsOut("ATTENDNO"	, Integer.parseInt(rs.getString("ATTENDNO"))+"番");	//出席番号
				svf.VrsOut("NAME1"		, rs.getString("NAME_SHOW"));					//生徒氏名
log.debug("rsstart4");
				if (syoukei == 1){
					svf.VrsOut("kei"	 	, String.valueOf("小計"));		//小計
					svf.VrsOut("kingaku" 	, String.valueOf(syouprice));	//小計金額
					syouprice = 0;
					svf.VrEndRecord();
					gyo++;
log.debug("syoukei");
					if (gyo == 50){
						gyo = 0;
					}
					svf.VrsOut("SPACE1" 	, "空");
					svf.VrEndRecord();
					gyo++;
log.debug("space");
					if (gyo == 50){
						gyo = 0;
					}
				}
log.debug("rsstart5");
				if (gyo == 0 || syoukei == 1) {
					if (gyo > 47){
						for (int i = gyo ;i < 50 ;i++){
							svf.VrsOut("SPACE1" 	, "空");
							svf.VrEndRecord();
							gyo++;
log.debug("space2");
						}
						gyo = 0;
					}
					titlenm = String.valueOf("教科書名");
					if (afdata.equals("2")) titlenm = String.valueOf("学習書名"); 
					if (afdata.equals("3")) titlenm = String.valueOf("副教材名");
					if (afdata.equals("4")) titlenm = String.valueOf("地図");
					svf.VrsOut("KOTEI1" 	, String.valueOf("教科書"));
					svf.VrsOut("KOTEI2" 	, String.valueOf("コード"));
					svf.VrsOut("KOTEI3" 	, String.valueOf("発行社"));
					svf.VrsOut("KOTEI4" 	, String.valueOf("定価"));
					svf.VrsOut("KOTEI5" 	, String.valueOf("講座"));
					svf.VrsOut("KOTEI6" 	, String.valueOf("コード"));
					svf.VrsOut("KOTEI7" 	, String.valueOf("講座名"));
					svf.VrsOut("TEXT_TITLE" 	, String.valueOf(titlenm));

log.debug("Endrecordmae");
					svf.VrEndRecord();
log.debug("Endrecordato");
					gyo = gyo+2;
log.debug("title");
					if (gyo == 50){
						gyo = 0;
					}
					syoukei = 0;
				}
log.debug("rsstart6");
                //詳細
                final int checkCount = byteCountMS932(rs.getString("TEXTBOOKNAME"));
                String field1 = "1";
                String field2 = "";
                if (checkCount > 40) {
                    field1 = "2";
                    field2 = "2";
                }
                svf.VrsOut("TEXTCD"       + field1, rs.getString("TEXTBOOKCD"));       //教科書番号
                if (checkCount > 40) {
                    svf.VrsOut("TEXTNAME" + field1 + "_1", rs.getString("TEXTBOOKNAME"));     //教科書名
                } else {
                    svf.VrsOut("TEXTNAME" + field1, rs.getString("TEXTBOOKNAME"));     //教科書名
                }
                svf.VrsOut("ISSUECOMPANY" + field1, rs.getString("ISSUECOMPANYNAME")); //発行社
                svf.VrsOut("TEXTPRICE"    + field1, rs.getString("TEXTBOOKPRICE"));    //定価
                svf.VrsOut("CHAIRCD"      + field2, rs.getString("CHAIRCD"));          //講座コード
                svf.VrsOut("CHAIRNAME"    + field2, rs.getString("CHAIRNAME"));        //講座名
                svf.VrEndRecord();
                if (checkCount > 40) {
                    gyo += 2;
                } else {
                    gyo++;
                }
log.debug("rsstart7");
log.debug("meisai");
				if (gyo == 50){
					gyo = 0;
				}
				//合計金額を計算
				if( rs.getString("TEXTBOOKPRICE") != null ) {
				    syouprice  = syouprice  + Integer.parseInt(rs.getString("TEXTBOOKPRICE"));
				    totalprice = totalprice + Integer.parseInt(rs.getString("TEXTBOOKPRICE"));
				}
				nonedata = true; //該当データなしフラグ
				bookdata = true;
			}
			if(bookdata == true){
				svf.VrsOut("kei"	 	, String.valueOf("小計"));		//小計
				svf.VrsOut("kingaku" 	, String.valueOf(syouprice));	//小計金額
				svf.VrEndRecord();
				gyo++;
log.debug("syoukei2");
				if (gyo == 50){
					gyo = 0;
				}

				svf.VrsOut("kei" 	, String.valueOf("合計"));			//合計金額
				svf.VrsOut("kingaku" 	, String.valueOf(totalprice));	//合計金額
				svf.VrEndRecord();
				gyo++;
log.debug("goukei");
				if (gyo == 50){
					gyo = 0;
				}
				if (gyo > 0){
					for (int lastcnt = gyo ; lastcnt < 50 ; lastcnt++){
						svf.VrsOut("SPACE1" 	, "空");
						svf.VrEndRecord();
log.debug("space3");
					}
				}
			}
log.debug("syuuryou");
			db2.commit();
			log.debug("set_detail read ok!");
		} catch( Exception ex ){
			log.error("set_detail read error!", ex);
		}

	}//set_detailの括り

	/**データ　取得**/
	private String preStat(String[] param)
	{
		final StringBuffer stb = new StringBuffer();
        stb.append("SELECT ");
        stb.append("    t5.HR_NAME, ");
        stb.append("    t3.NAME_SHOW, ");
        stb.append("    t4.ATTENDNO, ");
        stb.append("    t2.TEXTBOOKDIV, ");
        stb.append("    t1.TEXTBOOKCD, ");
        stb.append("    t2.TEXTBOOKNAME, ");
        stb.append("    t2.ISSUECOMPANYCD, ");
        stb.append("    w2.ISSUECOMPANYNAME, ");
        stb.append("    t2.TEXTBOOKPRICE, ");
        stb.append("    t1.CHAIRCD, ");
        stb.append("    w1.CHAIRNAME ");
        stb.append("FROM ");
        stb.append("    SCHREG_TEXTBOOK_DAT t1 LEFT JOIN CHAIR_DAT w1 ON t1.CHAIRCD = w1.CHAIRCD AND w1.YEAR = '"+param[0]+"' AND w1.SEMESTER = '"+param[1]+"', ");
        stb.append("    TEXTBOOK_MST t2 LEFT JOIN ISSUECOMPANY_MST w2 ON t2.ISSUECOMPANYCD = w2.ISSUECOMPANYCD, ");
        stb.append("    SCHREG_BASE_MST t3,SCHREG_REGD_DAT t4,SCHREG_REGD_HDAT t5 ");
        stb.append("WHERE ");
        stb.append("    t1.year = '"+param[0]+"' AND ");
        stb.append("    t1.semester = '"+param[1]+"' AND ");
        stb.append("    t1.schregno = ? AND ");
        stb.append("    t1.TEXTBOOKCD = t2.TEXTBOOKCD AND ");
        stb.append("    t3.schregno = ? AND ");
        stb.append("    t4.schregno = ? AND ");
        stb.append("    t4.YEAR = '"+param[0]+"' AND ");
        stb.append("    t4.SEMESTER = '"+param[1]+"' AND ");
        stb.append("    t5.YEAR = '"+param[0]+"' AND ");
        stb.append("    t5.SEMESTER = '"+param[1]+"' AND ");
        stb.append("    t5.GRADE || t5.HR_CLASS = '"+param[2]+"' ");
        stb.append("ORDER BY ");
        stb.append("    TEXTBOOKDIV,TEXTBOOKCD ");
		return stb.toString();

	}//preStat()の括り

}//クラスの括り
