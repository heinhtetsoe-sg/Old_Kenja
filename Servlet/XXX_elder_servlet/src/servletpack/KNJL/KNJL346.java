package servletpack.KNJL;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_Get_Info;

/**
 *
 *	学校教育システム 賢者 [入試管理]
 *
 *					＜ＫＮＪＬ３４６＞  不合格者成績順一覧表（繰上判定用）
 *
 *	2005/01/24 nakamoto 作成日
 *  2007/06/13 m-yama   NO001 パラメータ追加に伴う修正。
 **/

public class KNJL346 {

    private static final Log log = LogFactory.getLog(KNJL346.class);

    DecimalFormat df = new DecimalFormat("0.0");

	/*----------------------------*
	 * HTTP Get リクエストの処理  *
	 *----------------------------*/
	public void svf_out(HttpServletRequest request, HttpServletResponse response)
	                 throws ServletException, IOException
	{

		Vrw32alp svf = new Vrw32alp(); 			//PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
		DB2UDB db2 = null;						//Databaseクラスを継承したクラス
		String param[] = new String[4];

	//	パラメータの取得
		try {
	        param[0] = request.getParameter("YEAR");         				//年度
	        param[1] = request.getParameter("APDIV");         				//入試制度
	        param[2] = request.getParameter("TESTSCR");         			//２科計の平均：何点以上
	        param[3] = request.getParameter("OUTPUT");         				//1:2科 2:4科 NO001
		} catch( Exception ex ) {
			log.error("parameter error!");
		}

	//	print設定
		PrintWriter outstrm = new PrintWriter (response.getOutputStream());
		response.setContentType("application/pdf");

	//	svf設定
		int ret = svf.VrInit();						   		//クラスの初期化
        if (false && 0 != ret) { ret = 0; }
		ret = svf.VrSetSpoolFileStream(response.getOutputStream());  		//PDFファイル名の設定

	//	ＤＢ接続
		try {
			db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
			db2.open();
		} catch( Exception ex ) {
			log.error("DB2 open error!");
			return;
		}


	//	ＳＶＦ作成処理
		PreparedStatement ps1 = null;
		PreparedStatement ps2 = null;
        PreparedStatement psTestDiv = null;
		boolean nonedata = false; 								//該当データなしフラグ
		setHeader(db2,svf,param);
for(int i=0 ; i<param.length ; i++) log.debug("param["+i+"]="+param[i]);
		//SQL作成
		try {
			ps1 = db2.prepareStatement(preStat1(param));		//名簿一覧preparestatement
            log.debug("ps1 sql=" + preStat1(param));
			ps2 = db2.prepareStatement(preStat2(param));		//総ページ数preparestatement
            psTestDiv = db2.prepareStatement(preTestDivMst(param));
		} catch( Exception ex ) {
			log.error("DB2 prepareStatement set error!");
		}
		//SVF出力
		setTotalPage(db2,svf,param,ps2);							//総ページ数メソッド
		if( setSvfout(db2,svf,param,ps1,psTestDiv) ){							//帳票出力のメソッド
			nonedata = true;
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
		preStatClose(ps1,ps2,psTestDiv);		//preparestatementを閉じる
		db2.commit();
		db2.close();				//DBを閉じる
		outstrm.close();			//ストリームを閉じる 

    }//doGetの括り



	/** 事前処理 **/
	private void setHeader(
		DB2UDB db2,
		Vrw32alp svf,
		String param[]
	) {
		KNJ_Get_Info getinfo = new KNJ_Get_Info();
		KNJ_Get_Info.ReturnVal returnval = null;
		int ret = 0;
        if (false && 0 != ret) { ret = 0; }
		ret = svf.VrSetForm("KNJL346.frm", 4);	//繰上げ
		ret = svf.VrsOut("NENDO"		,KenjaProperties.gengou(Integer.parseInt(param[0]))+"年度");

        final String border = param[3].equals("1") ? "２" : "";   //NO001
		ret = svf.VrsOut("BORDERLINE"	,border + "合計の平均："+param[2]+"点以上");

        ret = svf.VrsOut("NUMBER_SUBJECT" ,param[3].equals("1") ? "２科計" : "合計"); //NO001

	//	作成日(現在処理日)の取得
		try {
			returnval = getinfo.Control(db2);
			ret = svf.VrsOut("DATE"	,KNJ_EditDate.h_format_JP(returnval.val3));
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
		String param[],
		PreparedStatement ps2
	) {
		int ret = 0;
        if (false && 0 != ret) { ret = 0; }
		try {
			ResultSet rs = ps2.executeQuery();

			while( rs.next() ){
				if (rs.getString("TOTAL_PAGE") != null) 
					ret = svf.VrsOut("TOTAL_PAGE"	,rs.getString("TOTAL_PAGE"));
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
		String param[],
		PreparedStatement ps1,
        PreparedStatement psTestDiv
	) {
		boolean nonedata = false;
		int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        ResultSet rsTestDiv = null;
		try {
			ResultSet rs = ps1.executeQuery();

			int reccnt_man 		= 0;	//男レコード数カウント用
			int reccnt_woman 	= 0;	//女レコード数カウント用
			int reccnt = 0;				//合計レコード数
			int pagecnt = 1;			//現在ページ数
			int gyo = 1;				//現在ページ数の判断用（行）

			while( rs.next() ){
				//レコードを出力
				if (reccnt > 0) ret = svf.VrEndRecord();
				//５０行超えた場合、ページ数カウント
				if (gyo > 50) {
					gyo = 1;
					pagecnt++;
				}
				//ヘッダ
				ret = svf.VrsOut("PAGE"		,String.valueOf(pagecnt));			//現在ページ数
				//明細
				ret = svf.VrsOut("RANK"		,rs.getString("TOTAL_RANK"));	//順位
				ret = svf.VrsOut("EXAMNO" 	,rs.getString("EXAMNO"));			//受験番号
				ret = svf.VrsOut("NAME" 	,rs.getString("NAME"));				//名前
				ret = svf.VrsOut("FINSCHOOL",rs.getString("FS_NAME"));			//出身学校名
				ret = svf.VrsOut("2TOTAL"	,String.valueOf(df.format(rs.getFloat("TOTAL_AVG"))));//２科計の平均

                int num = 0;
                rsTestDiv = psTestDiv.executeQuery();
                while (rsTestDiv.next()) {
                    num++;
                    String testdiv = rsTestDiv.getString("TESTDIV"); 

                    ret = svf.VrsOut("CHECK" + num   ,(rs.getString("TESTDIV" + testdiv).equals("1")) ? rs.getString("TEST_TOTAL" + testdiv) : "");    //トータル
                }
                rsTestDiv.close();

                //備考１(１０文字超えたかどうかで、どのフィールドにセットするかを決める)
				//最初は両方をクリア
				ret = svf.VrsOut("NOTE1_1"	,"");		//備考１(20)
				ret = svf.VrsOut("NOTE1_2"	,"");		//備考１(40)
				//備考１をセット
				if (rs.getString("REMARK1") != null) {
					if (rs.getString("REMARK1").length() > 10) {
						ret = svf.VrsOut("NOTE1_2"	,rs.getString("REMARK1"));	//備考１(40)
					} else {
						ret = svf.VrsOut("NOTE1_1"	,rs.getString("REMARK1"));	//備考１(20)
					}
				}
				//ret = svf.VrsOut("SEX" 		,rs.getString("SEX_NAME"));			//性別
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
				ret = svf.VrsOut("NOTE"	,"男"+String.valueOf(reccnt_man)+"名,女"+String.valueOf(reccnt_woman)+"名,合計"+String.valueOf(reccnt)+"名");
				ret = svf.VrEndRecord();//レコードを出力
			}
			rs.close();
			db2.commit();
		} catch( Exception ex ) {
			log.error("setSvfout set error!",ex);
		}
		return nonedata;
	}



	/**名簿一覧を取得**/
	private String preStat1(String param[])
	{
        final String field = (param[3].equals("1")) ? "TOTAL2" : "TOTAL4";  //NO001
		StringBuffer stb = new StringBuffer();
	//	パラメータ（なし）
		try {
			stb.append("SELECT T1.TOTAL_RANK, T1.EXAMNO, T2.NAME, T2.FS_NAME, T1.TOTAL_AVG, T2.SEX, T5.ABBV1 AS SEX_NAME, ");
			stb.append("       T1.TESTDIV0, T1.TESTDIV1, T1.TESTDIV2, T1.TESTDIV3, T1.TESTDIV4, T1.TESTDIV5, T1.TESTDIV6, T1.TEST_TOTAL0, T1.TEST_TOTAL1, T1.TEST_TOTAL2, T1.TEST_TOTAL3, T1.TEST_TOTAL4, T1.TEST_TOTAL5, T1.TEST_TOTAL6, T2.REMARK1, T1.TESTDIV_CNT ");
			stb.append("FROM ");
			stb.append("    (SELECT EXAMNO, ROUND(AVG(FLOAT(" + field + "))*10,0)/10 AS TOTAL_AVG, ");
			stb.append("            RANK() OVER (ORDER BY ROUND(AVG(FLOAT(" + field + "))*10,0)/10 DESC) AS TOTAL_RANK, ");
            stb.append("            SUM(CASE WHEN TESTDIV='0' THEN 1 ELSE 0 END) AS TESTDIV0, ");
			stb.append("            SUM(CASE WHEN TESTDIV='1' THEN 1 ELSE 0 END) AS TESTDIV1, ");
			stb.append("            SUM(CASE WHEN TESTDIV='2' THEN 1 ELSE 0 END) AS TESTDIV2, ");
			stb.append("            SUM(CASE WHEN TESTDIV='3' THEN 1 ELSE 0 END) AS TESTDIV3, ");
            stb.append("            SUM(CASE WHEN TESTDIV='4' THEN 1 ELSE 0 END) AS TESTDIV4, ");
            stb.append("            SUM(CASE WHEN TESTDIV='5' THEN 1 ELSE 0 END) AS TESTDIV5, ");
            stb.append("            SUM(CASE WHEN TESTDIV='6' THEN 1 ELSE 0 END) AS TESTDIV6, ");
            stb.append("            SUM(CASE WHEN TESTDIV='0' THEN " + field + " ELSE 0 END) AS TEST_TOTAL0, ");
			stb.append("            SUM(CASE WHEN TESTDIV='1' THEN " + field + " ELSE 0 END) AS TEST_TOTAL1, ");
			stb.append("            SUM(CASE WHEN TESTDIV='2' THEN " + field + " ELSE 0 END) AS TEST_TOTAL2, ");
			stb.append("            SUM(CASE WHEN TESTDIV='3' THEN " + field + " ELSE 0 END) AS TEST_TOTAL3, ");
            stb.append("            SUM(CASE WHEN TESTDIV='4' THEN " + field + " ELSE 0 END) AS TEST_TOTAL4, ");
            stb.append("            SUM(CASE WHEN TESTDIV='5' THEN " + field + " ELSE 0 END) AS TEST_TOTAL5, ");
            stb.append("            SUM(CASE WHEN TESTDIV='6' THEN " + field + " ELSE 0 END) AS TEST_TOTAL6, ");
			stb.append("            COUNT(*) TESTDIV_CNT ");
			stb.append("     FROM   ENTEXAM_RECEPT_DAT W1 ");
			stb.append("     WHERE  ENTEXAMYEAR='"+param[0]+"' AND ");
			stb.append("            APPLICANTDIV='"+param[1]+"' AND ");
			stb.append("            JUDGEDIV='2' AND ");						//受付データ.合否区分 2:不合格
									//合格者を除く
			stb.append("            EXAMNO NOT IN (SELECT DISTINCT EXAMNO ");
			stb.append("                           FROM   ENTEXAM_RECEPT_DAT ");
			stb.append("                           WHERE  ENTEXAMYEAR='"+param[0]+"' AND ");
			stb.append("                                  APPLICANTDIV='"+param[1]+"' AND ");
			stb.append("                                  JUDGEDIV='1' ) AND ");
									//未受験者を除く
			stb.append("            NOT EXISTS (SELECT 'X' ");
			stb.append("                        FROM   ENTEXAM_SCORE_DAT W2 ");
			stb.append("                        WHERE  W2.ENTEXAMYEAR=W1.ENTEXAMYEAR AND ");
			stb.append("                               W2.APPLICANTDIV=W1.APPLICANTDIV AND ");
			stb.append("                               W2.TESTDIV=W1.TESTDIV AND ");
			stb.append("                               W2.EXAM_TYPE=W1.EXAM_TYPE AND ");
			stb.append("                               W2.RECEPTNO=W1.RECEPTNO AND ");
			stb.append("                               W2.TESTSUBCLASSCD in ('1','2') AND ");
			stb.append("                               W2.ATTEND_FLG='0' ) ");	//得点データ.出欠フラグ 0:未受験
			stb.append("     GROUP BY EXAMNO ");
			stb.append("     HAVING ROUND(AVG(FLOAT(" + field + "))*10,0)/10 >= "+param[2]+" ");
			stb.append("    ) T1 ");
			stb.append("    LEFT JOIN ENTEXAM_APPLICANTBASE_DAT T2 ON T2.ENTEXAMYEAR='"+param[0]+"' AND T2.EXAMNO=T1.EXAMNO ");
			stb.append("    LEFT JOIN NAME_MST T5 ON T5.NAMECD1='Z002' AND T5.NAMECD2=T2.SEX ");
			stb.append("ORDER BY T1.TOTAL_AVG DESC ");
		} catch( Exception e ){
			log.error("preStat1 error!");
		}
		return stb.toString();

	}//preStat1()の括り



	/**総ページ数を取得**/
	private String preStat2(String param[])
	{
        final String field = (param[3].equals("1")) ? "TOTAL2" : "TOTAL4";  //NO001
		StringBuffer stb = new StringBuffer();
	//	パラメータ（なし）
		try {
			stb.append("SELECT ");
			stb.append("    CASE WHEN MOD(COUNT(*),50) > 0 THEN COUNT(*)/50 + 1 ELSE COUNT(*)/50 END TOTAL_PAGE  ");
			stb.append("FROM ");
			stb.append("    (SELECT EXAMNO, ROUND(AVG(FLOAT(" + field + "))*10,0)/10 AS TOTAL_AVG, ");
            stb.append("            SUM(CASE WHEN TESTDIV='0' THEN " + field + " ELSE 0 END) AS TESTDIV0, ");
			stb.append("            SUM(CASE WHEN TESTDIV='1' THEN " + field + " ELSE 0 END) AS TESTDIV1, ");
			stb.append("            SUM(CASE WHEN TESTDIV='2' THEN " + field + " ELSE 0 END) AS TESTDIV2, ");
			stb.append("            SUM(CASE WHEN TESTDIV='3' THEN " + field + " ELSE 0 END) AS TESTDIV3, ");
            stb.append("            SUM(CASE WHEN TESTDIV='4' THEN " + field + " ELSE 0 END) AS TESTDIV4, ");
            stb.append("            SUM(CASE WHEN TESTDIV='5' THEN " + field + " ELSE 0 END) AS TESTDIV5, ");
			stb.append("            COUNT(*) TESTDIV_CNT ");
			stb.append("     FROM   ENTEXAM_RECEPT_DAT W1 ");
			stb.append("     WHERE  ENTEXAMYEAR='"+param[0]+"' AND ");
			stb.append("            APPLICANTDIV='"+param[1]+"' AND ");
			stb.append("            JUDGEDIV='2' AND ");						//受付データ.合否区分 2:不合格
									//合格者を除く
			stb.append("            EXAMNO NOT IN (SELECT DISTINCT EXAMNO ");
			stb.append("                           FROM   ENTEXAM_RECEPT_DAT ");
			stb.append("                           WHERE  ENTEXAMYEAR='"+param[0]+"' AND ");
			stb.append("                                  APPLICANTDIV='"+param[1]+"' AND ");
			stb.append("                                  JUDGEDIV='1' ) AND ");
									//未受験者を除く
			stb.append("            NOT EXISTS (SELECT 'X' ");
			stb.append("                        FROM   ENTEXAM_SCORE_DAT W2 ");
			stb.append("                        WHERE  W2.ENTEXAMYEAR=W1.ENTEXAMYEAR AND ");
			stb.append("                               W2.APPLICANTDIV=W1.APPLICANTDIV AND ");
			stb.append("                               W2.TESTDIV=W1.TESTDIV AND ");
			stb.append("                               W2.EXAM_TYPE=W1.EXAM_TYPE AND ");
			stb.append("                               W2.RECEPTNO=W1.RECEPTNO AND ");
			stb.append("                               W2.TESTSUBCLASSCD in ('1','2') AND ");
			stb.append("                               W2.ATTEND_FLG='0' ) ");	//得点データ.出欠フラグ 0:未受験
			stb.append("     GROUP BY EXAMNO ");
			stb.append("     HAVING ROUND(AVG(FLOAT(" + field + "))*10,0)/10 >= "+param[2]+" ");
			stb.append("    ) T1 ");
		} catch( Exception e ){
			log.error("preStat2 error!");
		}
		return stb.toString();

	}//preStat2()の括り

    private String preTestDivMst(String param[])
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("SELECT TESTDIV FROM ENTEXAM_TESTDIV_MST WHERE ENTEXAMYEAR = '"+param[0]+"' ORDER BY SHOWORDER, TESTDIV ");
        } catch( Exception e ){
            log.error("preStat1 error!", e);
        }
        return stb.toString();
    }



	/**PrepareStatement close**/
	private void preStatClose(
		PreparedStatement ps1,
		PreparedStatement ps2,
        PreparedStatement psTestDiv
	) {
		try {
			ps1.close();
			ps2.close();
            psTestDiv.close();
		} catch( Exception e ){
			log.error("preStatClose error!");
		}
	}//preStatClose()の括り



}//クラスの括り
