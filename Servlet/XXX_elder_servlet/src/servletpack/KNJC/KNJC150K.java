/**
 *
 *	学校教育システム 賢者 [出欠管理] 長欠者要注意者リスト
 *
 *	2004/08/29 yamashiro
 *  2004/12/15 yamashiro 月の集計日付範囲の仕様変更
 *  2005/03/03 yamashiro 欠課時数要注意者リストを追加 <= KNJC140から移動
 *  2005/12/11 yamashiro・getParamメソッドにおいてOUTPUT3が取得されていなかった不具合を修正(OUTPUT2と誤記述)
 */

package servletpack.KNJC;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import nao_package.svf.Vrw32alp;
import nao_package.db.DB2UDB;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class KNJC150K {

    private static final Log log = LogFactory.getLog(KNJC150K.class);

    private boolean nonedata;

	/**
	 *
	 *  KNJC.classから最初に起動されるクラス
	 *
	 */
	public void svf_out(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		Vrw32alp svf = new Vrw32alp(); 	// PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
		DB2UDB db2 = null;				// Databaseクラスを継承したクラス
		String param[] = new String[13];
		String pselect[] = request.getParameterValues("GRADE");   			        //学年

        // パラメータの取得
	    getParam( request, param, pselect );
        // print svf設定
		setSvfInit( response, svf );
        // ＤＢ接続
		db2 = setDb( request );
		if( openDb(db2) ){
			log.error("db open error");
			return;
		}
        // 印刷処理
		printSvf( db2, svf, param, pselect );
        // 終了処理
		closeSvf( svf );
		closeDb( db2 );
	}


	/**
	 *
	 *  印刷処理
	 *
	 */
    private void printSvf( DB2UDB db2, Vrw32alp svf, String param[], String pselect[] ){

        int ret = 0;
        if (false || ret == 0) ret = 0;
		ret = svf.VrSetForm("KNJC140K.frm", 4);

		try {
            if( param[2].equals("1") ){
				getParam2( db2, param );    //05/09/06Build
                KNJC150K_CHOKETU obj = new KNJC150K_CHOKETU();
	            obj.printSvf( db2, svf, param, pselect );
	            if( obj.nonedata ) nonedata = true;
            } else if( param[2].equals("2") ){
				getParam2( db2, param );
                KNJC150K_SUBCLASS obj = new KNJC150K_SUBCLASS( db2, svf, param, pselect );
	            obj.printSvf();
	            if( obj.nonedata ) nonedata = true;
            } else
                return;

		} catch( Exception ex ) {
			log.error("printSvf error!",ex);
		}
    }


	/** 
     *  get parameter doGet()パラメータ受け取り 
     *			YEAR           年度 2004 
	 *			GAKKI          学期 3 
	 *			GRADE          対象学年の配列 01,02,03
	 *			OUTPUT         1:長欠者要注意者リスト  2:欠課時数要注意者リスト
	 *			DAYS           月XX以上 10
	 *			NENGETSU_FROM  印刷範囲from 2005-01 
	 *			NENGETSU_TO    印刷範囲to   2005-03 
     */
    private void getParam( HttpServletRequest request, String param[], String pselect[] ){
        
        log.debug("$Id: de6640b9dd507238b152e22c35b984d17341a7d6 $");

	    //param = new String[8];
		pselect = request.getParameterValues("GRADE");   			        //学年
		try {
	        param[0] = request.getParameter("YEAR");     					//年度
			param[1] = request.getParameter("GAKKI"); 						//学期
			param[3] = request.getParameter("NENGETSU_FROM");  				//印刷範囲年月FROM
			param[4] = request.getParameter("NENGETSU_TO");  				//印刷範囲年月TO
			param[5] = request.getParameter("DAYS");						//対象日数
			param[2] = request.getParameter("OUTPUT");  					//1:月別長欠者 2:欠課時数要注意者
			if( request.getParameter("OUTPUT2") != null )
				param[9] = request.getParameter("OUTPUT3");                 //遅刻・早退を欠課換算
			param[10] = request.getParameter("useCurriculumcd");
            param[11] = request.getParameter("useVirus");
            param[12] = request.getParameter("useKoudome");
		} catch( Exception ex ) {
            log.error("error! " , ex );
		}

        for(int i=0 ; i<param.length ; i++) 
            if( param[i] != null ) log.debug("param[" + i + "]=" + param[i]);
    }


	/** 
     *  パラメータセット 2005/01/29
     *      param[6]:attend_semes_datの最終集計日の翌日をセット
     *      param[7]:attend_semes_datの最終集計学期＋月をセット
     *      param[8]:集計日をセット NULLの場合は学期終了日
     */
    private void getParam2( DB2UDB db2, String param[] )
    {
		KNJDivideAttendDate obj = new KNJDivideAttendDate();
		try {
			obj.getDivideAttendDate( db2, param[0], param[1], null );
			param[6] = obj.date;
			param[7] = obj.month;
			param[8] = obj.enddate;
		} catch( Exception ex ){
			log.error("error! ",ex);
		}
	}


	/** print設定 */
    private void setSvfInit(HttpServletResponse response ,Vrw32alp svf){
		response.setContentType("application/pdf");
		int ret = svf.VrInit();											//クラスの初期化
        if (false || ret == 0) ret = 0;
		try {
			ret = svf.VrSetSpoolFileStream(response.getOutputStream());   	//PDFファイル名の設定
 		} catch( java.io.IOException ex ){
			log.error("db new error:" , ex);
		}
   }


	/** svf close */
    private void closeSvf(Vrw32alp svf){
		if( !nonedata ){
		 	int ret = svf.VrSetForm("MES001.frm", 0);
            if (false || ret == 0) ret = 0;
			ret = svf.VrsOut("note" , "note");
			ret = svf.VrEndPage();
		}
		int ret = svf.VrQuit();
        if (false || ret == 0) ret = 0;
    }


	/** DB set */
	private DB2UDB setDb(HttpServletRequest request)throws ServletException, IOException{
		DB2UDB db2 = null;
		try {
			db2 = new DB2UDB(request.getParameter("DBNAME")	, "db2inst1", "db2inst1", DB2UDB.TYPE2);	//Databaseクラスを継承したクラス
		} catch( Exception ex ){
			log.error("db new error:" , ex);
			if( db2 != null)db2.close();
		}
		return db2;
	}


	/** DB open */
	private boolean openDb(DB2UDB db2){
		try {
			db2.open();
		} catch( Exception ex ){
			log.error("db open error!",ex );
			return true;
		}//try-cathの括り
		return false;
	}


	/** DB close */
	private void closeDb(DB2UDB db2){
		try {
			db2.commit();
			db2.close();
		} catch( Exception ex ){
			log.error("db close error!",ex );
		}//try-cathの括り
	}


}//クラスの括り
