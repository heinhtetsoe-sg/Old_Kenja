package servletpack.KNJL;

import java.io.IOException;
import java.io.PrintWriter;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 *	学校教育システム 賢者 [入試管理]
 *
 *					＜ＫＮＪＬ３００＞  机上タックシール
 *
 *	2004/12/13 nakamoto 作成日
 *	2004/12/27 nakamoto 余りに文字は入れない	NO001
 **/

public class KNJL300 {

    private static final Log log = LogFactory.getLog(KNJL300.class);

	/*----------------------------*
	 * HTTP Get リクエストの処理  *
	 *----------------------------*/
	public void svf_out(HttpServletRequest request, HttpServletResponse response)
	                 throws ServletException, IOException
	{

		Vrw32alp svf = new Vrw32alp(); 			//PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
		DB2UDB db2 = null;						//Databaseクラスを継承したクラス
		String param[] = new String[14];

	//	パラメータの取得
		String classcd[] = request.getParameterValues("category_name");   	//試験会場コード + '-' + 開始受付番号（MIN）+ '-' + 終了受付番号（MAX）
		try {
	        param[0] = request.getParameter("YEAR");         				//年度
	        param[1] = request.getParameter("TESTDV");         				//入試区分
	        param[2] = request.getParameter("EXAM_TYPE");         			//受験型
	        param[3] = request.getParameter("noinf_st");         			//開始受付番号（画面入力）
	        param[4] = request.getParameter("noinf_ed");         			//終了受付番号（画面入力）
	        param[5] = request.getParameter("POROW");         				//開始位置（行）
	        param[6] = request.getParameter("POCOL");         				//開始位置（列）
	        param[12] = StringUtils.defaultString(request.getParameter("NO_TESTNAME"), "");   				//入試名なし
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
		boolean nonedata = false; 								//該当データなしフラグ
		setHeader(svf,param,classcd.length);
		//SQL作成
		try {
			ps1 = db2.prepareStatement(preStat1(param));		//入試区分・受験型preparestatement
		} catch( Exception ex ) {
			log.error("DB2 prepareStatement set error!");
		}
		//SVF出力
		for( int ia=0 ; ia<classcd.length ; ia++ ){
			setParamSplit(param,classcd[ia]);						//パラメータ分割メソッド
			setNameMst(db2,svf,param,ps1);							//入試区分・受験型取得メソッド
			if( setSvfout(svf,param) ){								//帳票出力のメソッド
				nonedata = true;
			}
		}

	//	該当データ無し
		if( !nonedata ){
			ret = svf.VrSetForm("MES001.frm", 0);
			ret = svf.VrsOut("note" , "note");
			ret = svf.VrEndPage();
		}

	// 	終了処理
		ret = svf.VrQuit();
		preStatClose(ps1);		//preparestatementを閉じる
		db2.commit();
		db2.close();				//DBを閉じる
		outstrm.close();			//ストリームを閉じる

    }//doGetの括り



	/** 事前処理 **/
	private void setHeader(
		Vrw32alp svf,
		String param[],
		int class_len
	) {
		svf.VrSetForm("KNJL300_1.frm", 1);
		try {
			//指示画面で試験会場コードが複数選択されている場合、行・列に１をセット
	        if (class_len > 1) {
				param[5] = "1";		//行番号
				param[6] = "1";		//列番号
			}
		} catch( Exception e ){
			log.error("setHeader set error!");
		}
	}



	/** パラメータの分割 **/
	private void setParamSplit(
		String param[],
		String classcd
	) {
		try {
			//連結されているパラメータの分割（試験会場コード + '-' + 開始受付番号（MIN）+ '-' + 終了受付番号（MAX））
	        param[7] = classcd.substring(0,4); 				//試験会場コード
	        param[8] = classcd.substring(5,9); 				//開始受付番号（MIN）
	        param[9] = classcd.substring(10); 				//終了受付番号（MAX）

			//指示画面で受付番号が入力されている場合、それをセット
	        if (param[3] != null && !param[3].equals("")) param[8] = param[3];		//開始受付番号（画面入力）
	        if (param[4] != null && !param[4].equals("")) param[9] = param[4];		//終了受付番号（画面入力）
		} catch( Exception e ){
			log.error("setParamSplit set error!");
		}
	}



	/**入試区分・受験型の名称を取得**/
	private void setNameMst(
		DB2UDB db2,
		Vrw32alp svf,
		String param[],
		PreparedStatement ps1
	) {
		int ret = 0;
        if (false && 0 != ret) { ret = 0; }
		try {
			int pp = 0;
			ps1.setString(++pp,param[7]);	//試験会場コード
			ResultSet rs = ps1.executeQuery();

			while( rs.next() ){
		        param[10] = rs.getString("TEST_NAME");         				//入試区分
		        param[11] = "入学試験( "+rs.getString("EXAM_NAME")+" )";	//受験型	/* NO001 */
			}
			rs.close();
		} catch( Exception ex ) {
			log.error("setNameMst set error!");
		}

	}



	/**帳票出力（受付番号をセット）**/
	private boolean setSvfout(
		Vrw32alp svf,
		String param[]
	) {
		boolean nonedata = false;
		int ret = 0;
        if (false && 0 != ret) { ret = 0; }
		try {
for(int i=0 ; i<param.length ; i++) log.debug("param["+i+"]="+param[i]);
            int gyoMax = 6;
            int lenMax = 3;
			int gyo = Integer.parseInt(param[5]);	//行番号
			int len = Integer.parseInt(param[6]);	//列番号
			int sno = Integer.parseInt(param[8]);	//開始受付番号
			int eno = Integer.parseInt(param[9]);	//終了受付番号
			while (sno <= eno) {
				//最終列
				if (len > lenMax) {
					len = 1;
					gyo++;
					//最終行
					if (gyo > gyoMax) {
						ret = svf.VrEndPage();//ページを出力
						gyo = 1;
					}
				}
				//タックシールに印字
				if (!"1".equals(param[12])) {
				    ret = svf.VrsOutn("TESTDIV"+String.valueOf(len) 	,gyo ,param[10]);			//入試区分
				    ret = svf.VrsOutn("EXAM_TYPE"+String.valueOf(len) 	,gyo ,param[11]);			//受験型
				} else {
					svf.VrsOutn("TESTDIV"+String.valueOf(len) 	,gyo , "座席番号");
				}
				ret = svf.VrsOutn("RECEPTNO"+String.valueOf(len) 	,gyo ,String.valueOf(sno));	//受付番号
				len++;
				sno++;
				nonedata = true;
			}
			//最終ページを出力
			if (nonedata) ret = svf.VrEndPage();
		} catch( Exception ex ) {
			log.error("setSvfout set error!");
		}
		return nonedata;
	}



	/**PrepareStatement作成**/
	private String preStat1(String param[])
	{
		StringBuffer stb = new StringBuffer();
	//	パラメータ（会場コード）
		try {
			stb.append("SELECT ");
			stb.append("    (SELECT NAME AS TEST_NAME FROM ENTEXAM_TESTDIV_MST WHERE ENTEXAMYEAR='"+param[0]+"' AND TESTDIV='"+param[1]+"'), ");
			stb.append("    (SELECT NAME1 AS EXAM_NAME FROM NAME_MST WHERE NAMECD1='L005' AND NAMECD2='"+param[2]+"'), ");
			stb.append("    S_RECEPTNO, E_RECEPTNO, EXAMHALL_NAME  ");
			stb.append("FROM ");
			stb.append("    ENTEXAM_HALL_DAT ");
			stb.append("WHERE ");
			stb.append("    TESTDIV='"+param[1]+"' AND ");
			stb.append("    EXAM_TYPE='"+param[2]+"' AND ");
			stb.append("    EXAMHALLCD=? ");
		} catch( Exception e ){
			log.error("preStat1 error!");
		}
		return stb.toString();

	}//preStat1()の括り



	/**PrepareStatement close**/
	private void preStatClose(
		PreparedStatement ps1
	) {
		try {
			ps1.close();
		} catch( Exception e ){
			log.error("preStatClose error!");
		}
	}//preStatClose()の括り



}//クラスの括り
