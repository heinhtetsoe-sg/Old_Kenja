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
 *					＜ＫＮＪＬ３２４＞  合格者受験番号
 *
 *	2004/12/27 nakamoto 作成日
 *	2005/01/13 nakamoto コミットが入れられるところではコミットする	NO002
 **/

public class KNJL324 {

    private static final Log log = LogFactory.getLog(KNJL324.class);

	/*----------------------------*
	 * HTTP Get リクエストの処理  *
	 *----------------------------*/
	public void svf_out(HttpServletRequest request, HttpServletResponse response)
	                 throws ServletException, IOException
	{

		Vrw32alp svf = new Vrw32alp(); 			//PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
		DB2UDB db2 = null;						//Databaseクラスを継承したクラス
		String param[] = new String[7];

	//	パラメータの取得
		try {
	        param[0] = request.getParameter("YEAR");         				//年度
	        param[1] = request.getParameter("APDIV");         				//入試制度
	        param[2] = request.getParameter("TESTDV");         				//入試区分
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
		PreparedStatement ps = null;
		PreparedStatement ps1 = null;
		PreparedStatement ps2 = null;
        PreparedStatement ps3 = null;
        PreparedStatement ps4 = null;
        PreparedStatement ps6 = null;
		boolean nonedata = false; 								//該当データなしフラグ
		setHeader(db2,svf,param);
for(int i=0 ; i<5 ; i++) log.debug("param["+i+"]="+param[i]);
		//SQL作成
		try {
			ps = db2.prepareStatement(preStat(param));			//入試区分preparestatement
			ps1 = db2.prepareStatement(preStat1(param, null));		//一覧preparestatement
            ps3 = db2.prepareStatement(preStat1(param, "3"));     //一覧(アップ合格)preparestatement
            ps4 = db2.prepareStatement(preStat1(param, "4"));     //一覧(スライド合格)preparestatement
            ps6 = db2.prepareStatement(preStat1(param, "6"));     //一覧(非正規合格)preparestatement
			ps2 = db2.prepareStatement(preStat2(param));		//名称preparestatement
		} catch( Exception ex ) {
			log.error("DB2 prepareStatement set error!");
		}
		//SVF出力
		if (setSvfMain(db2,svf,param,ps,ps1,ps2,ps3,ps4,ps6)) nonedata = true;	//帳票出力のメソッド

	//	該当データ無し
		if( !nonedata ){
			ret = svf.VrSetForm("MES001.frm", 0);
			ret = svf.VrsOut("note" , "note");
			ret = svf.VrEndPage();
		}

	// 	終了処理
		ret = svf.VrQuit();
		preStatClose(ps,ps1,ps2,ps3,ps4,ps6);		//preparestatementを閉じる
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
		ret = svf.VrSetForm("KNJL324.frm", 1);
		param[3] = KenjaProperties.gengou(Integer.parseInt(param[0]))+"年度";

	//	ＳＶＦ属性変更--->改ページ
		ret = svf.VrAttribute("TESTDIV","FF=1");

	//	作成日(現在処理日)の取得
		try {
			returnval = getinfo.Control(db2);
			param[4] = KNJ_EditDate.h_format_JP(returnval.val3);
		} catch( Exception e ){
			log.error("setHeader set error!");
		}

		getinfo = null;
		returnval = null;
	}



	/**
     *  svf print 印刷処理
     *            入試区分が指定されていれば( => param[2] !== "0" )１回の処理
     *            入試区分が複数の場合は全ての入試区分を舐める
     */
	private boolean setSvfMain(
		DB2UDB db2,
		Vrw32alp svf,
		String param[],
		PreparedStatement ps,
		PreparedStatement ps1,
        PreparedStatement ps2,
        PreparedStatement ps3,
		PreparedStatement ps4,
        PreparedStatement ps6
	) {
		boolean nonedata = false;

        final String judgeClass46Igai = "通常合格";
        final String judgeClass3 = getJudgeClassName(db2, "3");
        final String judgeClass4 = getJudgeClassName(db2, "4");
        final String judgeClass6 = getJudgeClassName(db2, "6");
        if( ! param[2].equals("9") ){
			setTestNameDate(db2,svf,param,ps2,param[2]);						//名称メソッド
for(int i=5 ; i<param.length ; i++) log.debug("param["+i+"]="+param[i]);
			if (setSvfout(db2, svf, param, ps1, param[2], judgeClass46Igai)) nonedata = true;			//帳票出力のメソッド
            if (setSvfout(db2, svf, param, ps3, param[2], judgeClass3)) nonedata = nonedata || true;         //帳票出力のメソッド
            if (setSvfout(db2, svf, param, ps4, param[2], judgeClass4)) nonedata = nonedata || true;         //帳票出力のメソッド
            if (setSvfout(db2, svf, param, ps6, param[2], judgeClass6)) nonedata = nonedata || true;         //帳票出力のメソッド
			return nonedata;
        }

		try {
			ResultSet rs = ps.executeQuery();

			while( rs.next() ){
				setTestNameDate(db2,svf,param,ps2, rs.getString("TESTDIV"));					//名称メソッド
for(int i=5 ; i<param.length ; i++) log.debug("param["+i+"]="+param[i]);
				if (setSvfout(db2, svf, param, ps1, rs.getString("TESTDIV"), judgeClass46Igai)) nonedata = true;		//帳票出力のメソッド
                if (setSvfout(db2, svf, param, ps3, rs.getString("TESTDIV"), judgeClass3)) nonedata = nonedata || true;         //帳票出力のメソッド
                if (setSvfout(db2, svf, param, ps4, rs.getString("TESTDIV"), judgeClass4)) nonedata = nonedata || true;         //帳票出力のメソッド
                if (setSvfout(db2, svf, param, ps6, rs.getString("TESTDIV"), judgeClass6)) nonedata = nonedata || true;         //帳票出力のメソッド
			}
			rs.close();
			db2.commit();	/* NO002 */
		} catch( Exception ex ) {
			log.error("setSvfMain set error!");
		}
		return nonedata;
	}



	/**名称をセット**/
	private void setTestNameDate(
		DB2UDB db2,
		Vrw32alp svf,
		String param[],
		PreparedStatement ps2,
		String test_div
	) {
		int ret = 0;
        if (false && 0 != ret) { ret = 0; }
		try {
            int p = 0;
			ps2.setString( ++p, test_div );
			ResultSet rs = ps2.executeQuery();

			while( rs.next() ){
				param[5] = rs.getString("TEST_NAME");								//入試区分
				param[6] = KNJ_EditDate.h_format_JP_MD(rs.getString("TEST_DATE"));	//入試日付
			}
			rs.close();
			db2.commit();	/* NO002 */
		} catch( Exception ex ) {
			log.error("setTestNameDate set error!");
		}

	}



	/**帳票出力（一覧をセット）**/
	private boolean setSvfout(
		DB2UDB db2,
		Vrw32alp svf,
		String param[],
		PreparedStatement ps1,
		String test_div,
        final String judgeClassName
	) {
		boolean nonedata = false;
		int ret = 0;
        if (false && 0 != ret) { ret = 0; }
		try {
            int p = 0;
			ps1.setString( ++p, test_div );
			ResultSet rs = ps1.executeQuery();

			int gyo = 1;				//行
			int len = 1;				//列

			while( rs.next() ){
				//最終列
				if (len > 6) {
					len = 1;
					gyo++;
					//最終行
					if (gyo > 10) {
						ret = svf.VrEndPage();//ページを出力
						gyo = 1;
					}
				}
				//見出し
				ret = svf.VrsOut("NENDO"	,param[3]);		//年度
				ret = svf.VrsOut("DATE"		,param[4]);		//作成日
				ret = svf.VrsOut("TESTDIV"	,param[5]);		//入試区分
				ret = svf.VrsOut("EXAMDATE"	,param[6]);		//入試日付
                svf.VrsOut("SUBTAITOL", judgeClassName);
				//明細
				ret = svf.VrsOutn("EXAMNO"+String.valueOf(len) 	,gyo ,rs.getString("EXAMNO"));	//受験番号
				len++;
				nonedata = true;
			}
			//最終ページを出力
			if (nonedata) ret = svf.VrEndPage();
			rs.close();
			db2.commit();	/* NO002 */
		} catch( Exception ex ) {
			log.error("setSvfout set error!");
		}
		return nonedata;
	}



	/**入試区分を取得**/
	private String preStat(String param[])
	{
		StringBuffer stb = new StringBuffer();
		try {
			stb.append("SELECT DISTINCT TESTDIV ");
			stb.append("FROM   ENTEXAM_RECEPT_DAT ");
			stb.append("WHERE  ENTEXAMYEAR='"+param[0]+"' AND ");
			stb.append("       APPLICANTDIV='"+param[1]+"' AND ");
			stb.append("       JUDGEDIV='1' ");
			stb.append("ORDER BY TESTDIV ");
		} catch( Exception e ){
			log.error("preStat error!");
		}
		return stb.toString();

	}//preStat()の括り



	/**一覧を取得**/
	private String preStat1(String param[], String selectJudgeClass)
	{
		StringBuffer stb = new StringBuffer();
	//	パラメータ（入試区分）
		try {
			stb.append("SELECT EXAMNO FROM ENTEXAM_RECEPT_DAT ");
			stb.append("WHERE  ENTEXAMYEAR='"+param[0]+"' AND ");
			stb.append("       APPLICANTDIV='"+param[1]+"' AND ");
			stb.append("       TESTDIV=? AND ");
			stb.append("       JUDGEDIV='1' ");
            if (selectJudgeClass == null) {
                stb.append("       AND JUDGECLASS NOT IN ('4','6') ");
            } else {
                stb.append("       AND JUDGECLASS='"+selectJudgeClass+"' ");
            }
			stb.append("ORDER BY EXAMNO ");
		} catch( Exception e ){
			log.error("preStat1 error!");
		}
		return stb.toString();

	}//preStat1()の括り



	/**名称を取得**/
	private String preStat2(String param[])
	{
		StringBuffer stb = new StringBuffer();
	//	パラメータ（入試区分）
		try {
			stb.append("SELECT TESTDAY AS TEST_DATE, NAME AS TEST_NAME ");
			stb.append("FROM   ENTEXAM_TESTDIV_MST T4 ");
			stb.append("WHERE  ENTEXAMYEAR='"+param[0]+"' AND TESTDIV=? ");
		} catch( Exception e ){
			log.error("preStat2 error!");
		}
		return stb.toString();

	}//preStat2()の括り



	/**PrepareStatement close**/
	private void preStatClose(
		PreparedStatement ps,
		PreparedStatement ps1,
		PreparedStatement ps2,
        PreparedStatement ps3,
        PreparedStatement ps4,
        PreparedStatement ps6
	) {
		try {
			ps.close();
			ps1.close();
			ps2.close();
            ps3.close();
            ps4.close();
            ps6.close();
		} catch( Exception e ){
			log.error("preStatClose error!");
		}
	}//preStatClose()の括り

    /**合格クラス名称を取得**/
    private String getJudgeClassName(DB2UDB db2, String namecd2) {
        StringBuffer sql = new StringBuffer();
        sql.append(" SELECT NAME1 ");
        sql.append(" FROM NAME_MST ");
        sql.append(" WHERE NAMECD1 = 'L016' AND NAMECD2 = '"+namecd2+"' ");
        String name = null;
        try{
            PreparedStatement ps = db2.prepareStatement(sql.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
               name = rs.getString("NAME1");
            }
            rs.close();
            ps.close();
            db2.commit();
        } catch(Exception ex) {
            log.error("getJudgeClassName error!");
        }
        return name;
    }

}//クラスの括り
