package servletpack.KNJL;

import java.io.IOException;
import java.io.PrintWriter;

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
 *					＜ＫＮＪＬ３５１＞  入試試験成績資料(総合)
 *
 *	2005/02/23 m-yama    作成日
 *  2006/01/05 yamashiro NO001 備考欄に「辞退」「特待」「繰上」「特別」を出力する。仕様はKNJL328と同様とする
 *  2006/01/15 m-yama    NO002 各回毎の欠席者は、▲表示にする。
 *  2007/06/13 m-yama    NO003 パラメータ追加に伴う修正。
 **/

public class KNJL351 {

    private static final Log log = LogFactory.getLog(KNJL351.class);

	/*----------------------------*
	 * HTTP Get リクエストの処理  *
	 *----------------------------*/
	public void svf_out(HttpServletRequest request, HttpServletResponse response)
	                 throws ServletException, IOException
	{

		Vrw32alp svf = new Vrw32alp(); 			//PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
		DB2UDB db2 = null;						//Databaseクラスを継承したクラス
		String param[] = new String[9];

	//	パラメータの取得
		try {
	        param[0] = request.getParameter("YEAR");         //年度
	        param[3] = request.getParameter("OUTPUT");       //ソート順序指定(1:番号順,2:成績順) NO003
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
		PreparedStatement ps  = null;
		PreparedStatement ps1 = null;
        PreparedStatement psTestDiv = null;
		boolean nonedata = false; 								//該当データなしフラグ
		setHeader(db2,svf,param);
for(int i=0 ; i<1 ; i++) log.debug("param["+i+"]="+param[i]);
		//SQL作成
		try {
			ps  = db2.prepareStatement(preStat(param));			//出願者数preparestatement
			ps1 = db2.prepareStatement(preStat1(param));		//トータル頁数preparestatement
            psTestDiv = db2.prepareStatement(preTestDivMst(param));
		} catch( Exception ex ) {
			log.error("DB2 prepareStatement set error!");
		}
		//SVF出力
		setTotalPage(db2,svf,param,ps1);					//総ページ数メソッド
		if (setSvfMain(db2,svf,param,ps,psTestDiv)) nonedata = true;	//帳票出力のメソッド

	//	該当データ無し
		if( !nonedata ){
			ret = svf.VrSetForm("MES001.frm", 0);
			ret = svf.VrsOut("note" , "note");
			ret = svf.VrEndPage();
		}

	// 	終了処理
		ret = svf.VrQuit();
		preStatClose(ps,ps1,psTestDiv);		//preparestatementを閉じる
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
		param[1] = KenjaProperties.gengou(Integer.parseInt(param[0]))+"年度";

	//	作成日(現在処理日)の取得
		try {
			returnval = getinfo.Control(db2);
			param[2] = KNJ_EditDate.h_format_JP(returnval.val3);
		} catch( Exception e ){
			log.error("setHeader set error!");
		}

		getinfo = null;
		returnval = null;
	}

	/**
     *  svf print 印刷処理
     */
	private boolean setSvfMain(
		DB2UDB db2,
		Vrw32alp svf,
		String param[],
		PreparedStatement ps,
        PreparedStatement psTestDiv
	) {
		boolean nonedata = false;
		int ret = 0;
        if (false && 0 != ret) { ret = 0; }
		int reccnt = 0;				//合計レコード数
		int pagecnt = 1;			//現在ページ数
		int gyo = 1;				//現在ページ数の判断用（行）
        ResultSet rsTestDiv = null;
		try {
			setTitle(svf,param);		//見出しメソッド
			ResultSet rs = ps.executeQuery();
			while( rs.next() ){
				//レコードを出力
				if (reccnt > 0) ret = svf.VrEndRecord();
				//５０行超えたとき、ページ数カウント
				if (gyo > 50) {
					gyo = 1;
					pagecnt++;
				}
				//ヘッダ
				ret = svf.VrsOut("PAGE"		,String.valueOf(pagecnt));		//現在ページ数
				//明細
				ret = svf.VrsOut("EXAMNO" 	,rs.getString("EXAMNO"));				//受験番号
                String name = rs.getString("NAME");
                String nameField = (name != null && 12 < name.length()) ? "NAME2" : "NAME";
				ret = svf.VrsOut(nameField 	,name);					                //名前
				ret = svf.VrsOut("SEX" 	,rs.getString("SEX"));						//性別
				ret = svf.VrsOut("SCHOOLNAME" 	,rs.getString("FS_NAME"));			//出身学校名

                int num = 0;
                rsTestDiv = psTestDiv.executeQuery();
                while (rsTestDiv.next()) {
                    num++;
                    String testdiv = rsTestDiv.getString("TESTDIV"); 

                    ret = svf.VrsOut(num + "GOUHI", rs.getString("GOUHI"+ testdiv));    //X回目合否
                    ret = svf.VrsOut(num + "4KEI",  rs.getString("FOUR" + testdiv));    //X回目4科計
                }
                rsTestDiv.close();

				ret = svf.VrsOut("2AVG" 	,rs.getString("REC_AVG"));				//成績(2科平均)
				ret = svf.VrsOut("EXAM" 		,rs.getString("RECEPT"));			//受験
                ret = svf.VrsOut("KATEN",( rs.getString("KATEN") != null )? rs.getString("KATEN") : "");           //加点
				ret = svf.VrsOut("GOUHI",rs.getString("GOUHI"));					//合否
				ret = svf.VrsOut("ADJOURNMENT" 	,rs.getString("SPECIAL_MEASURES3"));//特別アップ
				ret = svf.VrsOut("PROSEDURE" 		,rs.getString("PROCEDUREDIV"));	//手続
				ret = svf.VrsOut("ENTRANCE",rs.getString("ENTDIV"));				//入学
				//NO001 ret = svf.VrsOut("BIKOU",rs.getString("REMARK1"));					//備考
				ret = svf.VrsOut("BIKOU", getBiko( rs ) );					//備考 NO001

				//レコード数カウント
				reccnt++;
				//現在ページ数判断用
				gyo++;

				nonedata = true;
			}
			if (nonedata) ret = svf.VrEndRecord();
			rs.close();
			db2.commit();
		} catch( Exception ex ) {
			log.error("setSvfMain set error!", ex);
		}
		return nonedata;
	}

	/**総ページ数をセット**/
	private void setTotalPage(
		DB2UDB db2,
		Vrw32alp svf,
		String param[],
		PreparedStatement ps1
	) {
		int ret = 0;
        if (false && 0 != ret) { ret = 0; }
		try {
			ret = svf.VrSetForm("KNJL351.frm", 4);	//セットフォーム
			ResultSet rs = ps1.executeQuery();

			while( rs.next() ){
				if (rs.getString("TEST_CNT") != null)
					ret = svf.VrsOut("TOTAL_PAGE"	,rs.getString("TEST_CNT"));
			}
			rs.close();
			db2.commit();
		} catch( Exception ex ) {
			log.error("setTotalPage set error!");
		}

	}

	/**見出し項目をセット**/
	private void setTitle(
		Vrw32alp svf,
		String param[]
	) {
		int ret = 0;
        if (false && 0 != ret) { ret = 0; }
		try {
				ret = svf.VrsOut("NENDO"	 ,param[1]);		//年度
				ret = svf.VrsOut("FIELD1"	 ,param[2]);		//作成日
				ret = svf.VrsOut("NUMBER_SUBJECT" ,param[3].equals("2") ? "(2科計平均)" : "(合計平均)");   //NO003
		} catch( Exception ex ) {
			log.error("setTitle set error!");
		}

	}
	/**データ　取得**/
	private String preStat(String param[])
	{
		StringBuffer stb = new StringBuffer();
		try {

            final String field = (param[3].equals("2")) ? "TOTAL2" : "TOTAL4";  //NO003

            stb.append("with test as ");
            stb.append("(select EXAMNO,TOTAL2,TOTAL4,JUDGEDIV,HONORDIV,ADJOURNMENTDIV,TESTDIV, JUDGECLASS, KATEN ");
            stb.append(" from ENTEXAM_RECEPT_DAT ");
            stb.append(" where ENTEXAMYEAR = '"+param[0]+"'), ");
            stb.append("test0 as (select * from test where TESTDIV = '0'), ");
			stb.append("test1 as (select * from test where TESTDIV = '1'), ");
            stb.append("test2 as (select * from test where TESTDIV = '2'), ");
            stb.append("test3 as (select * from test where TESTDIV = '3'), ");
            stb.append("test4 as (select * from test where TESTDIV = '4'), ");
            stb.append("test5 as (select * from test where TESTDIV = '5'), ");
            stb.append("test6 as (select * from test where TESTDIV = '6'), ");
            stb.append("test_all(EXAMNO,TOTAL_AVG,TOTAL_RANK) as ");
			stb.append(" (SELECT EXAMNO, DECIMAL(ROUND(AVG(FLOAT(" + field + "))*10,0)/10,4,1) AS TOTAL_AVG, ");
			stb.append("    RANK() OVER(ORDER BY (value(ROUND(AVG(FLOAT(" + field + "))*10,0)/10,-1)) DESC) AS TOTAL_RANK ");
			stb.append("FROM   ENTEXAM_RECEPT_DAT W1 ");
			stb.append("WHERE  ENTEXAMYEAR='"+param[0]+"' AND ");
			stb.append("APPLICANTDIV='1' AND ");
			stb.append("NOT EXISTS (SELECT 'X' ");
			stb.append("            FROM   ENTEXAM_SCORE_DAT W2 ");
			stb.append("            WHERE  W2.ENTEXAMYEAR=W1.ENTEXAMYEAR AND ");
			stb.append("                   W2.APPLICANTDIV=W1.APPLICANTDIV AND ");
			stb.append("                   W2.TESTDIV=W1.TESTDIV AND ");
			stb.append("                   W2.EXAM_TYPE=W1.EXAM_TYPE AND ");
			stb.append("                   W2.RECEPTNO=W1.RECEPTNO AND ");
			stb.append("                   W2.TESTSUBCLASSCD in ('1','2') AND ");
			stb.append("                   W2.ATTEND_FLG='0' ) ");
			stb.append("GROUP BY EXAMNO) ");
            //合否判定（ライン入力）を実行したかどうかを判断
            stb.append(", T_PASSINGMARK AS ( ");
            stb.append("    SELECT DISTINCT ");
            stb.append("        ENTEXAMYEAR, ");
            stb.append("        TESTDIV ");
            stb.append("    FROM ");
            stb.append("        ENTEXAM_PASSINGMARK_MST ");
            stb.append("    WHERE ");
            stb.append("        ENTEXAMYEAR='"+param[0]+"' ");
            stb.append(") ");
			stb.append("SELECT ");
			stb.append("    w1.EXAMNO, ");
			stb.append("    w1.NAME, ");
			stb.append("    Meisyou_Get(w1.SEX,'Z002',2) SEX, ");
			stb.append("    w1.FS_NAME, ");
            stb.append("    w0.TOTAL4 FOUR0, ");
            stb.append("    case when w0.HONORDIV ='1' then '☆' when w0.JUDGECLASS = '3' then '◎' when w0.JUDGECLASS = '4' then '○' when w0.JUDGECLASS = '6' then '○' when w0.JUDGEDIV = '1' and w0.JUDGECLASS = '2' then '◎' when w0.JUDGEDIV = '1' and w0.JUDGECLASS = '1' then '○' when d0.APPLICANTDIV = '1' and d0.EXAMINEE_DIV = '2' then '▲' when w0.TOTAL2 is null and w0.TOTAL4 is null  then '' when p0.TESTDIV is null then '＊' else '×' end GOUHI0, ");
			stb.append("    w2.TOTAL4 FOUR1, ");
			stb.append("    case when w2.HONORDIV ='1' then '☆' when w2.JUDGECLASS = '3' then '◎' when w2.JUDGECLASS = '4' then '○' when w2.JUDGECLASS = '6' then '○' when w2.JUDGEDIV = '1' and w2.JUDGECLASS = '2' then '◎' when w2.JUDGEDIV = '1' and w2.JUDGECLASS = '1' then '○' when w6.APPLICANTDIV = '1' and w6.EXAMINEE_DIV = '2' then '▲' when w2.TOTAL2 is null and w2.TOTAL4 is null  then '' when p1.TESTDIV is null then '＊' else '×' end GOUHI1, ");
			stb.append("    w3.TOTAL4 FOUR2, ");
			stb.append("    case when w3.HONORDIV ='1' then '☆' when w3.JUDGECLASS = '3' then '◎' when w3.JUDGECLASS = '4' then '○' when w3.JUDGECLASS = '6' then '○' when w3.JUDGEDIV = '1' and w3.JUDGECLASS = '2' then '◎' when w3.JUDGEDIV = '1' and w3.JUDGECLASS = '1' then '○' when w7.APPLICANTDIV = '1' and w7.EXAMINEE_DIV = '2' then '▲' when w3.TOTAL2 is null and w3.TOTAL4 is null  then '' when p2.TESTDIV is null then '＊' else '×' end GOUHI2, ");
			stb.append("    w4.TOTAL4 FOUR3, ");
			stb.append("    case when w4.HONORDIV ='1' then '☆' when w4.JUDGECLASS = '3' then '◎' when w4.JUDGECLASS = '4' then '○' when w4.JUDGECLASS = '6' then '○' when w4.JUDGEDIV = '1' and w4.JUDGECLASS = '2' then '◎' when w4.JUDGEDIV = '1' and w4.JUDGECLASS = '1' then '○' when w8.APPLICANTDIV = '1' and w8.EXAMINEE_DIV = '2' then '▲' when w4.TOTAL2 is null and w4.TOTAL4 is null  then '' when p3.TESTDIV is null then '＊' else '×' end GOUHI3, ");
            stb.append("    w11.TOTAL4 FOUR4, ");
            stb.append("    case when w11.HONORDIV ='1' then '☆' when w11.JUDGECLASS = '3' then '◎' when w11.JUDGECLASS = '4' then '○' when w11.JUDGECLASS = '6' then '○' when w11.JUDGEDIV = '1' and w11.JUDGECLASS = '2' then '◎' when w11.JUDGEDIV = '1' and w11.JUDGECLASS = '1' then '○' when w9.APPLICANTDIV = '1' and w9.EXAMINEE_DIV = '2' then '▲' when w11.TOTAL2 is null and w11.TOTAL4 is null  then '' when p4.TESTDIV is null then '＊' else '×' end GOUHI4, ");
            stb.append("    w12.TOTAL4 FOUR5, ");
            stb.append("    case when w12.HONORDIV ='1' then '☆' when w12.JUDGECLASS = '3' then '◎' when w12.JUDGECLASS = '4' then '○' when w12.JUDGECLASS = '6' then '○' when w12.JUDGEDIV = '1' and w12.JUDGECLASS = '2' then '◎' when w12.JUDGEDIV = '1' and w12.JUDGECLASS = '1' then '○' when w10.APPLICANTDIV = '1' and w10.EXAMINEE_DIV = '2' then '▲' when w12.TOTAL2 is null and w12.TOTAL4 is null  then '' when p5.TESTDIV is null then '＊' else '×' end GOUHI5, ");
            stb.append("    w13.TOTAL4 FOUR6, ");
            stb.append("    case when w13.HONORDIV ='1' then '☆' when w13.JUDGECLASS = '3' then '◎' when w13.JUDGECLASS = '4' then '○' when w13.JUDGECLASS = '6' then '○' when w13.JUDGEDIV = '1' and w13.JUDGECLASS = '2' then '◎' when w13.JUDGEDIV = '1' and w13.JUDGECLASS = '1' then '○' when w16.APPLICANTDIV = '1' and w16.EXAMINEE_DIV = '2' then '▲' when w13.TOTAL2 is null and w13.TOTAL4 is null  then '' when p6.TESTDIV is null then '＊' else '×' end GOUHI6, ");
			stb.append("    w5.TOTAL_AVG REC_AVG, ");
			stb.append("    RANK() OVER(ORDER BY (value(w5.TOTAL_AVG,-1)) DESC) AS TOTAL_DESC, ");
			stb.append("    case when w0.EXAMNO is not null then '○' when w2.EXAMNO is not null then '○' when w3.EXAMNO is not null then '○' when w4.EXAMNO is not null then '○' when w11.EXAMNO is not null then '○' when w12.EXAMNO is not null then '○' when w13.EXAMNO is not null then '○' else '▲' end RECEPT, ");
            stb.append("    case when w0.KATEN is not null then w0.KATEN when w2.KATEN is not null then w2.KATEN when w3.KATEN is not null then w3.KATEN when w4.KATEN is not null then w4.KATEN when w11.KATEN is not null then w11.KATEN when w12.KATEN is not null then w12.KATEN when w13.KATEN is not null then w13.KATEN end KATEN, ");
			stb.append("    case when w0.HONORDIV ='1' or w2.HONORDIV ='1' or w3.HONORDIV ='1' or w4.HONORDIV ='1' or w11.HONORDIV ='1' or w12.HONORDIV ='1' or w13.HONORDIV ='1' then '☆' when w1.JUDGEMENT = '1' and w1.ENTCLASS = '2' then '◎' when w1.JUDGEMENT = '1' and w1.ENTCLASS = '1' then '○' when w0.EXAMNO is null and w2.EXAMNO is null and w3.EXAMNO is null and w4.EXAMNO is null and w11.EXAMNO is null and w12.EXAMNO is null and w13.EXAMNO is null then '' else '×' end GOUHI, ");
			stb.append("    case when w0.ADJOURNMENTDIV = '1' then '○' when w2.ADJOURNMENTDIV = '1' then '○' when w3.ADJOURNMENTDIV = '1' then '○' when w4.ADJOURNMENTDIV = '1' then '○' when w11.ADJOURNMENTDIV = '1' then '○' when w12.ADJOURNMENTDIV = '1' then '○' when w13.ADJOURNMENTDIV = '1' then '○' else '' end ADJOUR, ");
            stb.append("    case w1.SPECIAL_MEASURES when '3' then '○' else '' end SPECIAL_MEASURES3, ");
			stb.append("    case w1.PROCEDUREDIV when '1' then '○' else '' end PROCEDUREDIV, ");
			stb.append("    case w1.ENTDIV when '1' then '○' else '' end ENTDIV, ");
			stb.append("    w1.REMARK1 ");
            stb.append(   ",W1.ENTDIV ");            //NO001
            stb.append(   ",W1.SPECIAL_MEASURES ");  //NO001
            stb.append(   ",VALUE(W13.HONORDIV, VALUE(W12.HONORDIV, VALUE(W11.HONORDIV, VALUE(W4.HONORDIV, VALUE(W3.HONORDIV, VALUE(W2.HONORDIV, W0.HONORDIV)))))) AS HONORDIV ");  //NO001
			stb.append("FROM ");
			stb.append("    ENTEXAM_APPLICANTBASE_DAT w1 ");
            stb.append("    left join test0 w0 on w1.examno = w0.examno ");
            stb.append("    left join test1 w2 on w1.examno = w2.examno ");
            stb.append("    left join test2 w3 on w1.examno = w3.examno ");
            stb.append("    left join test3 w4 on w1.examno = w4.examno ");
            stb.append("    left join test4 w11 on w1.examno = w11.examno ");
            stb.append("    left join test5 w12 on w1.examno = w12.examno ");
            stb.append("    left join test6 w13 on w1.examno = w13.examno ");
            stb.append("    left join test_all w5 on w1.examno = w5.examno ");
//NO002↓
            stb.append("    LEFT JOIN ENTEXAM_DESIRE_DAT d0 ON d0.ENTEXAMYEAR = w1.ENTEXAMYEAR ");
            stb.append("        AND d0.APPLICANTDIV = '1' ");
            stb.append("        AND d0.TESTDIV = '0' ");
            stb.append("        AND d0.examno = w1.examno ");
			stb.append("    LEFT JOIN ENTEXAM_DESIRE_DAT w6 ON w6.ENTEXAMYEAR = w1.ENTEXAMYEAR ");
			stb.append("        AND w6.APPLICANTDIV = '1' ");
			stb.append("        AND w6.TESTDIV = '1' ");
			stb.append("        AND w6.examno = w1.examno ");
			stb.append("    LEFT JOIN ENTEXAM_DESIRE_DAT w7 ON w7.ENTEXAMYEAR = w1.ENTEXAMYEAR ");
			stb.append("        AND w7.APPLICANTDIV = '1' ");
			stb.append("        AND w7.TESTDIV = '2' ");
			stb.append("        AND w7.examno = w1.examno ");
			stb.append("    LEFT JOIN ENTEXAM_DESIRE_DAT w8 ON w8.ENTEXAMYEAR = w1.ENTEXAMYEAR ");
			stb.append("        AND w8.APPLICANTDIV = '1' ");
			stb.append("        AND w8.TESTDIV = '3' ");
			stb.append("        AND w8.examno = w1.examno ");
            stb.append("    LEFT JOIN ENTEXAM_DESIRE_DAT w9 ON w9.ENTEXAMYEAR = w1.ENTEXAMYEAR ");
            stb.append("        AND w9.APPLICANTDIV = '1' ");
            stb.append("        AND w9.TESTDIV = '4' ");
            stb.append("        AND w9.examno = w1.examno ");
            stb.append("    LEFT JOIN ENTEXAM_DESIRE_DAT w10 ON w10.ENTEXAMYEAR = w1.ENTEXAMYEAR ");
            stb.append("        AND w10.APPLICANTDIV = '1' ");
            stb.append("        AND w10.TESTDIV = '5' ");
            stb.append("        AND w10.examno = w1.examno ");
            stb.append("    LEFT JOIN ENTEXAM_DESIRE_DAT w16 ON w16.ENTEXAMYEAR = w1.ENTEXAMYEAR ");
            stb.append("        AND w16.APPLICANTDIV = '1' ");
            stb.append("        AND w16.TESTDIV = '6' ");
            stb.append("        AND w16.examno = w1.examno ");
//NO002↑
            stb.append("    LEFT JOIN T_PASSINGMARK p0 ON p0.ENTEXAMYEAR = w1.ENTEXAMYEAR AND p0.TESTDIV = '0' ");
            stb.append("    LEFT JOIN T_PASSINGMARK p1 ON p1.ENTEXAMYEAR = w1.ENTEXAMYEAR AND p1.TESTDIV = '1' ");
            stb.append("    LEFT JOIN T_PASSINGMARK p2 ON p2.ENTEXAMYEAR = w1.ENTEXAMYEAR AND p2.TESTDIV = '2' ");
            stb.append("    LEFT JOIN T_PASSINGMARK p3 ON p3.ENTEXAMYEAR = w1.ENTEXAMYEAR AND p3.TESTDIV = '3' ");
            stb.append("    LEFT JOIN T_PASSINGMARK p4 ON p4.ENTEXAMYEAR = w1.ENTEXAMYEAR AND p4.TESTDIV = '4' ");
            stb.append("    LEFT JOIN T_PASSINGMARK p5 ON p5.ENTEXAMYEAR = w1.ENTEXAMYEAR AND p5.TESTDIV = '5' ");
            stb.append("    LEFT JOIN T_PASSINGMARK p6 ON p6.ENTEXAMYEAR = w1.ENTEXAMYEAR AND p6.TESTDIV = '6' ");

            stb.append("WHERE w1.ENTEXAMYEAR = '"+param[0]+"' ");
            //NO003
			if (param[3].equals("1")){
				stb.append("ORDER BY w1.examno ");
			}else {
				stb.append("ORDER BY TOTAL_DESC ");
			}
		} catch( Exception e ){
			log.error("preStat error!", e);
		}
//log.debug(stb);
		return stb.toString();

	}//preStat()の括り

	/**総ページ数を取得**/
	private String preStat1(String param[])
	{
		StringBuffer stb = new StringBuffer();
	//	パラメータ（なし）
		try {
			stb.append("SELECT CASE WHEN MOD(COUNT(*),50) > 0 THEN COUNT(*)/50 + 1 ELSE COUNT(*)/50 END TEST_CNT ");
			stb.append("     FROM   ENTEXAM_APPLICANTBASE_DAT ");
			stb.append("     WHERE  ENTEXAMYEAR='"+param[0]+"' ");
		} catch( Exception e ){
			log.error("preStat1 error!", e);
		}
		return stb.toString();

	}//preStat1()の括り

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
		PreparedStatement ps,
		PreparedStatement ps1,
        PreparedStatement psTestDiv
	) {
		try {
			ps.close();
			ps1.close();
            psTestDiv.close();
		} catch( Exception e ){
			log.error("preStatClose error!");
		}
	}//preStatClose()の括り


	/**
     *  備考編集 
     *  2006/01/05 Build NO001 KNJL328の「条件」と同様
     */
	private String getBiko( ResultSet rs )
    {
        String sret = null;
		try {
            if( rs.getString("ENTDIV") != null  &&  rs.getString("ENTDIV").equals("2") )
                sret = "辞退";
            else if( rs.getString("HONORDIV") != null  &&  rs.getString("HONORDIV").equals("1") ){
                    sret = "特待";      //条件
            } else if( rs.getString("SPECIAL_MEASURES") != null ){// NO001 (JUDGEMENT→SPECIAL_MEASURES)
                if( rs.getString("SPECIAL_MEASURES").equals("1") )// NO001 (JUDGEMENT→SPECIAL_MEASURES,2→1)
                    sret = "繰上";      //条件
                else if( rs.getString("SPECIAL_MEASURES").equals("2") )// NO001 (JUDGEMENT→SPECIAL_MEASURES,3→2)
                    sret = "特別";      //条件
            }
            if( rs.getString("REMARK1") != null ){
                if( sret != null )
                    sret +=  (", " + rs.getString("REMARK1") );
                else
                    sret = rs.getString("REMARK1");
            }
		} catch( Exception ex ) {
			log.error("getBiko error!", ex);
		} finally{
            if( sret == null )sret = "";
        }
		return sret;
	}


}//クラスの括り
