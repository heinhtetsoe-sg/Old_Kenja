/**
 *
 *	学校教育システム 賢者 [入試処理]  予選考資料
 *
 *	2004/12/20
 *  2005/01/05 科目コードを社会:3 理科:4に変更
 *             入試区分の履歴の表記において、受付データの合否区分=1の時は◎を出力、を追加
 *             結果表の集計仕様を変更
 *             結果表の出力を追加
 *             ２科目成績一覧表の出力を追加
 *  2005/01/08 出願コース名は出力しない
 *  2005/01/19 KNJL321への組込みから復活
 *             受取パラメータについて  試験区分の全てを削除、受験型の選択を削除( => param[4]に代入していた )
 *
 */

package servletpack.KNJL;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class KNJL320 {

    private static final Log log = LogFactory.getLog(KNJL320.class);

    private StringBuffer stb = new StringBuffer();
	String param[];
    boolean nonedata;

	/**
	  *
	  *  KNJD.classから最初に起動されるクラス
	  *
	  **/
	public void svf_out(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		Vrw32alp svf = new Vrw32alp(); 	// PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
		DB2UDB db2 = null;				// Databaseクラスを継承したクラス
		param = new String[13];

	// パラメータの取得
	    getParam(request);

	// print svf設定
		setSvfInit(response, svf);

	// ＤＢ接続
		db2 = setDb(request);
		if( openDb(db2) ){
			System.out.println("db open error");
			return;
		}

	// 印刷処理
		printSvf(request, db2, svf);

	// 終了処理
		closeSvf(svf);
		closeDb(db2);

	}	//doGetの括り


    private void printSvf(HttpServletRequest request, DB2UDB db2, Vrw32alp svf){

        KNJL321BASE obj = null;
		try {
            obj = new KNJL321HYOUSI(db2, svf, param);
            obj.printSvf();
            if( obj.nonedata )nonedata = true;
            obj = new KNJL321KAMOKU2(db2, svf, param);
            obj.printSvf();
            if( obj.nonedata )nonedata = true;
		} catch( Exception e ){
			System.out.println("[KNJD]private Get_SemesterDiv error!"+e );
		}

    }


	/** get parameter doGet()パラメータ受け取り */
    private void getParam(HttpServletRequest request){
	    param = new String[14];
		try {
	        param[0] = request.getParameter("YEAR");         	//卒業年度
			param[1] = request.getParameter("APDIV");         	//入試制度
			param[2] = request.getParameter("TESTDV");         	//入試区分
            param[3] = request.getParameter("TESTSCR");         //２科目以上
            param[9] = "KNJL320";
		} catch( Exception ex ) {
			System.out.println("get parameter error!" + ex);
		}
for(int i=0 ; i<param.length ; i++) if( param[i] != null ) System.out.println("[KNJL320]param[" + i + "]=" + param[i]);
    }


	/** print設定 */
    private void setSvfInit(HttpServletResponse response ,Vrw32alp svf){
		response.setContentType("application/pdf");
		int ret = svf.VrInit();											//クラスの初期化
        if (false && 0 != ret) { ret = 0; }
		try {
			ret = svf.VrSetSpoolFileStream(response.getOutputStream());   	//PDFファイル名の設定
 		} catch( java.io.IOException ex ){
			System.out.println("db new error:" + ex);
		}
   }


	/** svf close */
    private void closeSvf(Vrw32alp svf){
		if( !nonedata ){
		 	int ret = svf.VrSetForm("MES001.frm", 0);
            if (false && 0 != ret) { ret = 0; }
			ret = svf.VrsOut("note" , "note");
			ret = svf.VrEndPage();
		}
		svf.VrQuit();
    }


	/** DB set */
	private DB2UDB setDb(HttpServletRequest request)throws ServletException, IOException{
		DB2UDB db2 = null;
		try {
			db2 = new DB2UDB(request.getParameter("DBNAME")	, "db2inst1", "db2inst1", DB2UDB.TYPE2);	//Databaseクラスを継承したクラス
		} catch( Exception ex ){
			System.out.println("db new error:" + ex);
			if( db2 != null)db2.close();
		}
		return db2;
	}


	/** DB open */
	private boolean openDb(DB2UDB db2){
		try {
			db2.open();
		} catch( Exception ex ){
			System.out.println("db open error!"+ex );
			return true;
		}//try-cathの括り
		return false;
	}//private boolean Open_db()


	/** DB close */
	private void closeDb(DB2UDB db2){
		try {
			db2.commit();
			db2.close();
		} catch( Exception ex ){
			System.out.println("db close error!"+ex );
		}//try-cathの括り
	}//private Close_Db()



}//クラスの括り
