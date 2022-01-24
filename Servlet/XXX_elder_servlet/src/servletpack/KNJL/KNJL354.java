/**
 * $Id: c8499b827d80eb6fe16e7bff9d4120ebe64ebf9b $
 */
package servletpack.KNJL;

import java.io.IOException;
import java.io.PrintWriter;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_Get_Info;

/**
 *
 *	学校教育システム 賢者 [入試管理]
 *
 *					＜ＫＮＪＬ３５４＞  補欠合格対象者一覧表
 *
 *	2020/10/16 s-shimoji 新規作成
 **/

public class KNJL354 {

    private static final Log log = LogFactory.getLog(KNJL354.class);

    Param _param;

	/*----------------------------*
	 * HTTP Get リクエストの処理  *
	 *----------------------------*/
	public void svf_out(HttpServletRequest request, HttpServletResponse response)
	                 throws ServletException, IOException
	{

		Vrw32alp svf = new Vrw32alp(); 			//PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
		DB2UDB db2 = null;						//Databaseクラスを継承したクラス

	//	print設定
		PrintWriter outstrm = new PrintWriter (response.getOutputStream());
		response.setContentType("application/pdf");

	//	svf設定
		svf.VrInit();						   		//クラスの初期化
		svf.VrSetSpoolFileStream(response.getOutputStream());  		//PDFファイル名の設定

	//	ＤＢ接続
		try {
			db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
			db2.open();
	        _param = createParam(db2, request);
		} catch( Exception ex ) {
			log.error("DB2 open error!");
			return;
		}


	//	ＳＶＦ作成処理
		PreparedStatement ps1 = null;
		PreparedStatement ps2 = null;
		boolean nonedata = false; 								//該当データなしフラグ
		setHeader(db2,svf);

		//SQL作成
		try {
			String ps1Sql = preStat1();
            log.debug("ps1 sql=" + ps1Sql);
			ps1 = db2.prepareStatement(ps1Sql);		//名簿一覧preparestatement

			String ps2Sql = preStat2();
            log.debug("ps2 sql=" + ps2Sql);
			ps2 = db2.prepareStatement(ps2Sql);		//総ページ数preparestatement
		} catch( Exception ex ) {
			log.error("DB2 prepareStatement set error!");
		}
		//SVF出力
		setTotalPage(db2,svf,ps2);							//総ページ数メソッド
		if( setSvfout(db2,svf,ps1) ){							//帳票出力のメソッド
			nonedata = true;
		}

        log.debug("nonedata="+nonedata);
	    // 該当データ無し
		if( !nonedata ){
			svf.VrSetForm("MES001.frm", 0);
			svf.VrsOut("note" , "note");
			svf.VrEndPage();
		}

	// 	終了処理
		svf.VrQuit();
		preStatClose(ps1,ps2);		//preparestatementを閉じる
		db2.commit();
		db2.close();				//DBを閉じる
		outstrm.close();			//ストリームを閉じる

    }//doGetの括り



	/** 事前処理 **/
	private void setHeader(
		DB2UDB db2,
		Vrw32alp svf
	) {
		KNJ_Get_Info getinfo = new KNJ_Get_Info();
		KNJ_Get_Info.ReturnVal returnval = null;
		svf.VrSetForm("KNJL354.frm", 4);	//繰上げ
		svf.VrsOut("NENDO"		,KenjaProperties.gengou(Integer.parseInt(_param._year)) + "年度　補欠合格対象者一覧");

        final String border = _param._output.equals("1") ? "２" : "";   //NO001
		svf.VrsOut("BORDERLINE"	,border + "合計の判定：" + _param._testscr + "点以上");

        svf.VrsOut("NUMBER_SUBJECT" , _param._output.equals("1") ? "２科計" : "合計"); //NO001

	//	作成日(現在処理日)の取得
		try {
			returnval = getinfo.Control(db2);
			svf.VrsOut("DATE"	,KNJ_EditDate.h_format_JP(returnval.val3));
		} catch( Exception e ){
			log.error("setHeader set error!");
		}

		getinfo = null;
		returnval = null;
	}



	/**総ページ数をセット**/
	private void setTotalPage(
		DB2UDB db2,
		Vrw32alp svf,
		PreparedStatement ps2
	) {
		try {
			ResultSet rs = ps2.executeQuery();

			while( rs.next() ){
				if (rs.getString("TOTAL_PAGE") != null)
					svf.VrsOut("TOTAL_PAGE"	,rs.getString("TOTAL_PAGE"));
			}
			rs.close();
			db2.commit();
		} catch( Exception ex ) {
			log.error("setTotalPage set error!");
		}

	}



	/**帳票出力（名簿一覧をセット）**/
	private boolean setSvfout(
		DB2UDB db2,
		Vrw32alp svf,
		PreparedStatement ps1
	) {
		boolean nonedata = false;
		try {
			ResultSet rs = ps1.executeQuery();

			int reccnt_man = 0; // 男レコード数カウント用
			int reccnt_woman 	= 0; // 女レコード数カウント用
			int reccnt = 0; // 合計レコード数
			int pagecnt = 1; // 現在ページ数
			int gyo = 1; // 現在ページ数の判断用（行）

			while( rs.next() ) {
				//レコードを出力
				if (reccnt > 0) svf.VrEndRecord();
				//５０行超えた場合、ページ数カウント
				if (gyo > 50) {
					gyo = 1;
					pagecnt++;
				}
				//ヘッダ
				svf.VrsOut("PAGE",     String.valueOf(pagecnt));   //現在ページ数
				//明細
				svf.VrsOut("RANK",     rs.getString("TOTAL_RANK")); //順位
				svf.VrsOut("EXAMNO",   rs.getString("EXAMNO"));     //受験番号
				svf.VrsOut("NAME" 	,  rs.getString("NAME"));       //名前
				svf.VrsOut("FINSCHOOL",rs.getString("FS_NAME"));    //出身学校名
				svf.VrsOut("2TOTAL"	,  rs.getString("TOTAL_SCR"));  //２科計の合計
				String testTotal0 = rs.getString("TEST_TOTAL0");
				if (!"0".equals(testTotal0)) {
	                svf.VrsOut("CHECK1",   testTotal0);//トータル
				}
				String testTotal1 = rs.getString("TEST_TOTAL1");
				if (!"0".equals(testTotal1)) {
	                svf.VrsOut("CHECK2",   testTotal1);//トータル
				}
				String testTotal2 = rs.getString("TEST_TOTAL2");
				if (!"0".equals(testTotal2)) {
	                svf.VrsOut("CHECK3",   testTotal2);//トータル
				}
				svf.VrsOut("NOTE1_1",  rs.getString("REMARK1"));    //備考１

				//レコード数カウント
				reccnt++;
				if (rs.getString("SEX") != null) {
					if (rs.getString("SEX").equals("1")) reccnt_man++;
					if (rs.getString("SEX").equals("2")) reccnt_woman++;
				}
				//現在ページ数判断用
				gyo++;

				nonedata = true;
			}

			//最終レコードを出力
			if (nonedata) {
				//最終ページに男女合計を出力
				svf.VrsOut("NOTE"	,"男"+String.valueOf(reccnt_man)+"名,女"+String.valueOf(reccnt_woman)+"名,合計"+String.valueOf(reccnt)+"名");
				svf.VrEndRecord();//レコードを出力
			}
			rs.close();
			db2.commit();
		} catch( Exception ex ) {
			log.error("setSvfout set error!",ex);
		}
		return nonedata;
	}



	/**名簿一覧を取得**/
	private String preStat1()
	{
        final String field = ("1".equals(_param._output)) ? "TOTAL2" : "TOTAL4";  //NO001
		StringBuffer stb = new StringBuffer();
	//	パラメータ（なし）
		try {
			stb.append("SELECT RANK() OVER (ORDER BY TOTAL_SCR DESC) AS TOTAL_RANK, ");
			stb.append("       EXAMNO, NAME, FS_NAME, TOTAL_SCR, SEX, SEX_NAME, ");
			stb.append("       TEST_TOTAL0, TEST_TOTAL1, TEST_TOTAL2, REMARK1, TESTDIV_CNT ");
			stb.append("FROM ");
			stb.append("    (SELECT T1.EXAMNO, T2.NAME, T2.FS_NAME, T2.SEX, T5.ABBV1 AS SEX_NAME, ");
			stb.append("            T1.TEST_TOTAL0, T1.TEST_TOTAL1, T1.TEST_TOTAL2, T3.TELNO AS REMARK1, T1.TESTDIV_CNT, ");
			stb.append("            ( ");
			stb.append("            CASE WHEN T1.TEST_TOTAL0 > T1.TEST_TOTAL1 THEN ");
			stb.append("                     CASE WHEN T1.TEST_TOTAL1 > T1.TEST_TOTAL2 THEN ");
			stb.append("                         T1.TEST_TOTAL0 + T1.TEST_TOTAL1 "); // TEST_TOTAL2が最低点のため、他2つを合算する
			stb.append("                     ELSE ");
			stb.append("                         T1.TEST_TOTAL0 + T1.TEST_TOTAL2 "); // TEST_TOTAL1が最低点のため、他2つを合算する
			stb.append("                     END ");
			stb.append("                 ELSE ");
			stb.append("                     CASE WHEN T1.TEST_TOTAL0 > T1.TEST_TOTAL2 THEN ");
			stb.append("                         T1.TEST_TOTAL0 + T1.TEST_TOTAL1 "); // TEST_TOTAL2が最低点のため、他2つを合算する
			stb.append("                     ELSE ");
			stb.append("                         T1.TEST_TOTAL1 + T1.TEST_TOTAL2 "); // TEST_TOTAL0が最低点のため、他2つを合算する
			stb.append("                     END ");
			stb.append("                 END ");
			stb.append("           ) AS TOTAL_SCR ");
			stb.append("    FROM ");
			stb.append("        (SELECT ENTEXAMYEAR, ");
			stb.append("                EXAMNO, ");
			stb.append("                SUM(CASE WHEN TESTDIV='0' THEN " + field + " ELSE 0 END) AS TEST_TOTAL0, ");
			stb.append("                SUM(CASE WHEN TESTDIV='1' THEN " + field + " ELSE 0 END) AS TEST_TOTAL1, ");
			stb.append("                SUM(CASE WHEN TESTDIV='2' THEN " + field + " ELSE 0 END) AS TEST_TOTAL2, ");
			stb.append("                COUNT(*) AS TESTDIV_CNT ");
			stb.append("         FROM   ENTEXAM_RECEPT_DAT W1 ");
			stb.append("         WHERE  ENTEXAMYEAR='" + _param._year + "' AND ");
			stb.append("                APPLICANTDIV='" + _param._apdiv + "' AND ");
			stb.append("                JUDGEDIV='2' AND "); // 受付データ.合否区分 2:不合格
			                            // 合格者を除く
			stb.append("                EXAMNO NOT IN (SELECT DISTINCT EXAMNO ");
			stb.append("                               FROM   ENTEXAM_RECEPT_DAT ");
			stb.append("                               WHERE  ENTEXAMYEAR='" + _param._year + "' AND ");
			stb.append("                                      APPLICANTDIV='" + _param._apdiv + "' AND ");
			stb.append("                                      JUDGEDIV='1' ) AND ");
			                            // 未受験者を除く
			stb.append("                NOT EXISTS (SELECT 'X' ");
			stb.append("                            FROM   ENTEXAM_SCORE_DAT W2 ");
			stb.append("                            WHERE  W2.ENTEXAMYEAR=W1.ENTEXAMYEAR AND ");
			stb.append("                                   W2.APPLICANTDIV=W1.APPLICANTDIV AND ");
			stb.append("                                   W2.TESTDIV=W1.TESTDIV AND ");
			stb.append("                                   W2.EXAM_TYPE=W1.EXAM_TYPE AND ");
			stb.append("                                   W2.RECEPTNO=W1.RECEPTNO AND ");
			stb.append("                                   W2.TESTSUBCLASSCD in ('1','2') AND ");
			stb.append("                                   W2.ATTEND_FLG='0' ) "); // 得点データ.出欠フラグ 0:未受験
			stb.append("         GROUP BY ENTEXAMYEAR, ");
			stb.append("                  EXAMNO ");
			stb.append("        ) T1 ");
			stb.append("        LEFT JOIN ENTEXAM_APPLICANTBASE_DAT T2 ON T2.ENTEXAMYEAR=T1.ENTEXAMYEAR AND T2.EXAMNO=T1.EXAMNO ");
			stb.append("        LEFT JOIN NAME_MST T5 ON T5.NAMECD1='Z002' AND T5.NAMECD2=T2.SEX ");
			stb.append("        LEFT JOIN ENTEXAM_APPLICANTADDR_DAT T3 ON T3.ENTEXAMYEAR=T2.ENTEXAMYEAR AND T3.EXAMNO=T2.EXAMNO ");
			stb.append("    ) T6 ");
			stb.append("WHERE ");
			stb.append("    TOTAL_SCR>=" + _param._testscr + " ");
			stb.append("ORDER BY TOTAL_SCR DESC, ");
			stb.append("         EXAMNO ");
		} catch( Exception e ){
			log.error("preStat1 error!");
		}
		return stb.toString();

	}//preStat1()の括り



	/**総ページ数を取得**/
	private String preStat2()
	{
        final String field = ("1".equals(_param._output)) ? "TOTAL2" : "TOTAL4";  //NO001
		StringBuffer stb = new StringBuffer();
	//	パラメータ（なし）
		try {
			stb.append("SELECT CASE WHEN MOD(COUNT(*),50) > 0 THEN COUNT(*)/50 + 1 ELSE COUNT(*)/50 END TOTAL_PAGE ");
			stb.append("FROM ");
			stb.append("    (SELECT T1.EXAMNO, ");
			stb.append("           ( ");
			stb.append("           CASE WHEN T1.TEST_TOTAL0 > T1.TEST_TOTAL1 THEN ");
			stb.append("                     CASE WHEN T1.TEST_TOTAL1 > T1.TEST_TOTAL2 THEN ");
			stb.append("                         T1.TEST_TOTAL0 + T1.TEST_TOTAL1 ");
			stb.append("                     ELSE ");
			stb.append("                         T1.TEST_TOTAL0 + T1.TEST_TOTAL2 ");
			stb.append("                     END ");
			stb.append("                ELSE ");
			stb.append("                     CASE WHEN T1.TEST_TOTAL0 > T1.TEST_TOTAL2 THEN ");
			stb.append("                         T1.TEST_TOTAL0 + T1.TEST_TOTAL1 ");
			stb.append("                     ELSE ");
			stb.append("                         T1.TEST_TOTAL1 + T1.TEST_TOTAL2 ");
			stb.append("                     END ");
			stb.append("                END ");
			stb.append("           ) AS TOTAL_SCR ");
			stb.append("    FROM ");
			stb.append("        (SELECT ENTEXAMYEAR, ");
			stb.append("                EXAMNO, ");
			stb.append("                SUM(CASE WHEN TESTDIV='0' THEN " + field + " ELSE 0 END) AS TEST_TOTAL0, ");
			stb.append("                SUM(CASE WHEN TESTDIV='1' THEN " + field + " ELSE 0 END) AS TEST_TOTAL1, ");
			stb.append("                SUM(CASE WHEN TESTDIV='2' THEN " + field + " ELSE 0 END) AS TEST_TOTAL2, ");
			stb.append("                COUNT(*) AS TESTDIV_CNT ");
			stb.append("         FROM   ENTEXAM_RECEPT_DAT W1 ");
			stb.append("         WHERE  ENTEXAMYEAR='" + _param._year + "' AND ");
			stb.append("                APPLICANTDIV='" + _param._apdiv + "' AND ");
			stb.append("                JUDGEDIV='2' AND "); // 受付データ.合否区分 2:不合格
			                            // 合格者を除く
			stb.append("                EXAMNO NOT IN (SELECT DISTINCT EXAMNO ");
			stb.append("                               FROM   ENTEXAM_RECEPT_DAT ");
			stb.append("                               WHERE  ENTEXAMYEAR='" + _param._year + "' AND ");
			stb.append("                                      APPLICANTDIV='" + _param._apdiv + "' AND ");
			stb.append("                                      JUDGEDIV='1' ) AND ");
			                            // 未受験者を除く
			stb.append("                NOT EXISTS (SELECT 'X' ");
			stb.append("                            FROM   ENTEXAM_SCORE_DAT W2 ");
			stb.append("                            WHERE  W2.ENTEXAMYEAR=W1.ENTEXAMYEAR AND ");
			stb.append("                                   W2.APPLICANTDIV=W1.APPLICANTDIV AND ");
			stb.append("                                   W2.TESTDIV=W1.TESTDIV AND ");
			stb.append("                                   W2.EXAM_TYPE=W1.EXAM_TYPE AND ");
			stb.append("                                   W2.RECEPTNO=W1.RECEPTNO AND ");
			stb.append("                                   W2.TESTSUBCLASSCD in ('1','2') AND ");
			stb.append("                                   W2.ATTEND_FLG='0' ) "); // 得点データ.出欠フラグ 0:未受験
			stb.append("         GROUP BY ENTEXAMYEAR, ");
			stb.append("                  EXAMNO ");
			stb.append("        ) T1 ");
			stb.append("    ) T6 ");
			stb.append("WHERE ");
			stb.append("    TOTAL_SCR>=" + _param._testscr + " ");
		} catch( Exception e ){
			log.error("preStat2 error!");
		}
		return stb.toString();

	}//preStat2()の括り

	/**PrepareStatement close**/
	private void preStatClose(
		PreparedStatement ps1,
		PreparedStatement ps2
	) {
		try {
			ps1.close();
			ps2.close();
		} catch( Exception e ){
			log.error("preStatClose error!");
		}
	}//preStatClose()の括り

    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 77472 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        private final String _year; // 年度
        private final String _apdiv; // 入試制度
        private final String _testscr; // ２科計の判定点：何点以上
        private final String _output; // 1:2科 2:4科 NO001

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _apdiv = request.getParameter("APDIV");
            _testscr = request.getParameter("TESTSCR");
            _output = request.getParameter("OUTPUT");
        }
    }

}//クラスの括り
