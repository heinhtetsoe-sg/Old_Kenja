package servletpack.KNJE;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import nao_package.svf.Vrw32alp;
import nao_package.db.DB2UDB;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.SQLException;
import java.sql.*;
import servletpack.KNJZ.detail.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 *	学校教育システム 賢者 [近大]
 *
 *					＜ＫＮＪＥ１４０＞  成績段階別人数一覧表
 *
 *	2005/07/11 m-yama 作成日
 **/

public class KNJE140 {

    private static final Log log = LogFactory.getLog(KNJE140.class);
	private boolean jhighschool = false;

	/*----------------------------*
	 * HTTP Get リクエストの処理  *
	 *----------------------------*/
	public void svf_out(HttpServletRequest request, HttpServletResponse response)
	                 throws ServletException, IOException
	{

		Vrw32alp svf = new Vrw32alp(); 			//PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
		DB2UDB db2 = null;						//Databaseクラスを継承したクラス
		String param[] = new String[10];
		//	パラメータの取得
		try {
	        param[0] = request.getParameter("YEAR");			//年度
			param[1] = request.getParameter("GAKKI");			//学期
			param[2] = request.getParameter("GRADE");			//学年
		} catch( Exception ex ) {
			log.error("parameter error!");
		}

	//	print設定
		PrintWriter outstrm = new PrintWriter (response.getOutputStream());
		response.setContentType("application/pdf");

	//	svf設定
        int ret = 0;
        if (ret != 0) ret = 0;
		ret = svf.VrInit();						   		//クラスの初期化
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
		boolean nonedata = false; 								//該当データなしフラグ
for(int i=0 ; i<4 ; i++) log.debug("param["+i+"]="+param[i]);
		//SQL作成
		try {
			ps  = db2.prepareStatement(preStat(param));
		} catch( Exception ex ) {
			log.error("DB2 prepareStatement set error!");
		}
		//ヘッダ作成
		setHeader(db2,svf,param);

		if (setSvfMain(db2,svf,param,ps)) nonedata = true;	//帳票出力のメソッド

	//	該当データ無し
		if( !nonedata ){
			ret = svf.VrSetForm("MES001.frm", 0);
			ret = svf.VrsOut("note" , "note");
			ret = svf.VrEndPage();
		}

	// 	終了処理
		ret = svf.VrQuit();
		preStatClose(ps);			//preparestatementを閉じる
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
		param[3] = param[0]+"年度";

	//	作成日(現在処理日)の取得
		try {
			returnval = getinfo.Control(db2);
			param[4] = KNJ_EditDate.h_format_thi(returnval.val3,0);
		} catch( Exception ex ){
			log.error("setHeader set error!");
		}

		getinfo = null;
		returnval = null;
	}

	/**
     *  svf print 印刷処理全印刷
     */
	private boolean setSvfMain(
		DB2UDB db2,
		Vrw32alp svf,
		String param[],
		PreparedStatement ps
	) {
		boolean nonedata  = false;
		int gyo = 1;
		int allcntA = 0;
		int allcntB = 0;
		int allcntC = 0;
		int allcntD = 0;
		int allcntE = 0;
		int allcnt = 0;
		try {
			svf.VrSetForm("KNJE140.frm", 4);			//セットフォーム
			ResultSet rs = ps.executeQuery();
			while( rs.next() ){
				if (gyo > 20){
					gyo = 1;
				}
				//ヘッダ
				svf.VrsOut("NENDO"			,param[3]);			//年度
				svf.VrsOut("DATE"				,param[4]);			//作成日
				svf.VrsOut("GRADE"			,String.valueOf(Integer.parseInt(param[2])));

				//明細
				svf.VrsOut("GROUPNAME"	,rs.getString("GROUP_NAME"));	//グループ名
				svf.VrsOut("A_MEMBER"		,rs.getString("A_SUM"));		//Ａ合計
				svf.VrsOut("B_MEMBER"		,rs.getString("B_SUM"));		//Ｂ合計
				svf.VrsOut("C_MEMBER"		,rs.getString("C_SUM"));		//Ｃ合計
				svf.VrsOut("D_MEMBER"		,rs.getString("D_SUM"));		//Ｄ合計
				svf.VrsOut("E_MEMBER"		,rs.getString("E_SUM"));		//Ｅ合計
				svf.VrsOut("TOTAL_MEMBER"	,rs.getString("ALL_SUM"));		//合計

				allcntA = allcntA+Integer.parseInt(rs.getString("A_SUM"));
				allcntB = allcntB+Integer.parseInt(rs.getString("B_SUM"));
				allcntC = allcntC+Integer.parseInt(rs.getString("C_SUM"));
				allcntD = allcntD+Integer.parseInt(rs.getString("D_SUM"));
				allcntE = allcntE+Integer.parseInt(rs.getString("E_SUM"));
				allcnt  = allcnt +Integer.parseInt(rs.getString("ALL_SUM"));

				gyo++;
				svf.VrEndRecord();
				nonedata  = true ;
			}
			rs.close();
			db2.commit();
			if (nonedata){
				if (gyo > 20) gyo = 1;
				for (;gyo < 20;gyo++){
					svf.VrEndRecord();
				}
				//合計
				svf.VrsOut("GROUPNAME"	,"合計");	//グループ名
				svf.VrsOut("A_MEMBER"		,String.valueOf(allcntA));		//Ａ合計
				svf.VrsOut("B_MEMBER"		,String.valueOf(allcntB));		//Ｂ合計
				svf.VrsOut("C_MEMBER"		,String.valueOf(allcntC));		//Ｃ合計
				svf.VrsOut("D_MEMBER"		,String.valueOf(allcntD));		//Ｄ合計
				svf.VrsOut("E_MEMBER"		,String.valueOf(allcntE));		//Ｅ合計
				svf.VrsOut("TOTAL_MEMBER"	,String.valueOf(allcnt));		//合計
				svf.VrEndRecord();
			}
			return nonedata;
		} catch( Exception ex ) {
			log.error("setSvfMain set error!");
		}
		return nonedata;
	}

	/**データ　取得**/
	private String preStat(String param[])
	{
		StringBuffer stb = new StringBuffer();
		try {
			stb.append("WITH COURSET AS ( ");
			stb.append("SELECT ");
			stb.append("    COURSE_SEQ,GROUP_NAME ");
			stb.append("FROM ");
			stb.append("    COURSE_GROUP_DAT ");
			stb.append("WHERE ");
			stb.append("    YEAR = '"+param[0]+"' ");
			stb.append("    AND GRADE = '"+param[2]+"' ");
			stb.append("GROUP BY ");
			stb.append("    COURSE_SEQ,GROUP_NAME ");
			stb.append(") ");
			stb.append("SELECT ");
			stb.append("    COURSECODE,GROUP_NAME, ");
			stb.append("    SUM(A_MEMBER) AS A_SUM, ");
			stb.append("    SUM(B_MEMBER) AS B_SUM, ");
			stb.append("    SUM(C_MEMBER) AS C_SUM, ");
			stb.append("    SUM(D_MEMBER) AS D_SUM, ");
			stb.append("    SUM(E_MEMBER) AS E_SUM, ");
			stb.append("    SUM(A_MEMBER)+SUM(B_MEMBER)+SUM(C_MEMBER)+SUM(D_MEMBER)+SUM(E_MEMBER) AS ALL_SUM ");
			stb.append("FROM ");
			stb.append("    GENEVIEWMBR_DAT ");
			stb.append("    LEFT JOIN COURSET ON COURSE_SEQ = COURSECODE ");
			stb.append("WHERE ");
			stb.append("    YEAR = '"+param[0]+"' ");
			stb.append("    AND COURSECD = '0' ");
			stb.append("    AND MAJORCD = '000' ");
			stb.append("    AND GRADE = '"+param[2]+"' ");
			stb.append("GROUP BY ");
			stb.append("    COURSECODE,GROUP_NAME ");

log.debug(stb);
		} catch( Exception ex ){
			log.error("preStat error!");
		}
		return stb.toString();

	}//preStat()の括り

	/**PrepareStatement close**/
	private void preStatClose(
		PreparedStatement ps
	) {
		try {
			ps.close();
		} catch( Exception ex ){
			log.error("preStatClose error!");
		}
	}//preStatClose()の括り

}//クラスの括り
