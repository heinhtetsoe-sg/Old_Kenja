/**
http://ktest/servlet_kin/KNJC?DBNAME=H051101&PRGID=KNJC140&YEAR=2005&GAKKI=2&OUTPUT=2&OUTPUT3=on&GRADE=03
 *
 *	学校教育システム 賢者 [出欠管理] 欠席・欠課の要注意者・超過者リスト
 *
 *	2004/08/29 yamashiro・新規作成
 *  2004/10/27 yamashiro・科目別リストにおいて、欠課時間が２倍で出力される不具合を修正
 *  2005/02/04 yamashiro・集計出欠テーブルおよび出欠データの集計範囲取得処理を追加
 *  2005/12/11 yamashiro・getParamメソッドにおいてOUTPUT3が取得されていなかった不具合を修正(OUTPUT2と誤記述)
 *  
 * @version $Id: d6574caeebf8f96570b093c5e9f212ba06838fba $
 */

package servletpack.KNJC;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import servletpack.KNJZ.detail.KNJ_Get_Info;
import nao_package.svf.Vrw32alp;
import nao_package.db.DB2UDB;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class KNJC140K {

    private static final Log log = LogFactory.getLog(KNJC140K.class);
	private String param[];
    private String pselect[];
    private boolean nonedata;

	/**
	 *
	 *  KNJD.classから最初に起動されるクラス
	 *
	 */
	public void svf_out(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		Vrw32alp svf = new Vrw32alp(); 	// PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
		DB2UDB db2 = null;				// Databaseクラスを継承したクラス

        // パラメータの取得
	    getParam(request);

        // print svf設定
		setSvfInit(response, svf);

        // ＤＢ接続
		db2 = setDb(request);
		if( openDb(db2) ){
			log.error("db open error");
			return;
		}

        // 印刷処理
        getParam2( db2 );                   // 05/02/04
		printSvf(request, db2, svf);

        // 終了処理
		closeSvf(svf);
		closeDb(db2);

	}	//doGetの括り


	/**
	 *
	 *  印刷処理
	 *
	 */
    private void printSvf(HttpServletRequest request, DB2UDB db2, Vrw32alp svf){
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
		ret = svf.VrSetForm("KNJC140K.frm", 4);
		try {
            KNJC140K_BASE obj = null;
            if( param[2].equals("1") )
                obj = new KNJC140K_GRADE(db2, svf, param, pselect);
            else if( param[2].equals("2") )
                obj = new KNJC140K_SUBCLASS(db2, svf, param, pselect);
            else
                return;

            obj.printSvf();
            if( obj.nonedata ) nonedata = true;

		} catch( Exception ex ) {
			log.error("printSvf error!",ex);
		}
    }


	/** get parameter doGet()パラメータ受け取り */
    private void getParam(HttpServletRequest request){
	    param = new String[13];
		pselect = request.getParameterValues("GRADE");   			        //学年
		try {
	        param[0] = request.getParameter("YEAR");     					//年度
			param[1] = request.getParameter("GAKKI"); 						//学期
			param[2] = request.getParameter("OUTPUT");  					//1:学年別出力 2:科目別出力
			if( request.getParameter("RATIO")!=null )
				param[5] = request.getParameter("RATIO");					//学年別出力の場合、欠席日数算出の分母
			if( request.getParameter("OUTPUT3") != null )
				param[9] = request.getParameter("OUTPUT3");                 //遅刻・早退を欠課換算 05/12/11 Modify
			param[10] = request.getParameter("useCurriculumcd");
            param[11] = request.getParameter("useVirus");
            param[12] = request.getParameter("useKoudome");
		} catch( Exception ex ) {
            log.error("error! " , ex );
		}
		log.debug("$Id: d6574caeebf8f96570b093c5e9f212ba06838fba $");
        //for(int i=0 ; i<param.length ; i++) if( param[i] != null ) log.debug("[KNJC140K]param[" + i + "]=" + param[i]);
    }


	/** 
     *  パラメータセット 2005/02/04
     *      param[6]:attend_semes_datの最終集計日の翌日をセット
     *      param[7]:attend_semes_datの最終集計学期＋月をセット
     */
    private void getParam2( DB2UDB db2 )
    {
	    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	    Calendar cal1 = Calendar.getInstance();
	    Calendar cal2 = Calendar.getInstance();
		ResultSet rs = null;

        //	学期名称、範囲の取得
		try {
            KNJ_Get_Info getinfo = new KNJ_Get_Info();		//各情報取得用のクラス
            KNJ_Get_Info.ReturnVal returnval = getinfo.Semester(db2,param[0],param[1]);		//各情報を返すためのクラス
			param[8] = returnval.val3;									//学期期間TO
		} catch( Exception ex ){
			log.warn("term1 svf-out error!",ex);
        } finally {
            if( param[8] == null ) param[8] = ( Integer.parseInt(param[0]) + 1 ) + "-03-31";
		}

		try {
		    cal1.setTime( sdf.parse( param[8] ) );	//出欠集計範囲の終わり
 		} catch( Exception ex ){
			log.error("getParam2 error:" , ex);
		}

		try {
            db2.query("SELECT  SEMESTER || MONTH AS SM, "
            +                 "MAX((CASE WHEN MONTH BETWEEN '01' AND '03' THEN RTRIM(CHAR(INT(YEAR)+1)) ELSE YEAR END ) || '-' || MONTH || '-' || APPOINTED_DAY)"
            +         "FROM    ATTEND_SEMES_DAT "
            +         "WHERE   YEAR = '" + param[0] + "' AND "
            +                 "(CASE WHEN MONTH BETWEEN '01' AND '03' THEN RTRIM(CHAR(INT(YEAR)+1)) ELSE YEAR END ) || MONTH "
            +                       "<='"+ String.valueOf( cal1.get(Calendar.YEAR) ) + "" + String.valueOf( cal1.get(Calendar.MONTH) + 1 ) + "' "
            +         "GROUP BY SEMESTER, MONTH "
            +         "ORDER BY SEMESTER DESC, MONTH DESC ");
			rs = db2.getResultSet();
            while ( rs.next() ){
		        cal2.setTime( sdf.parse( rs.getString(2) ) );
                if( cal2.get( Calendar.DATE ) == 1 ){
                    cal2.add( Calendar.MONTH, 1 );
                }
                log.debug("cal2="+sdf.format(cal2.getTime()));
                if( cal2.after(cal1) ) continue;   //集計テーブルの集計締日が指示画面の締日より大きければ前の月を読む
                param[6] = sdf.format( cal2.getTime() );
                param[7] = rs.getString(1);
                break;
            }
		} catch( Exception ex ) {
			log.warn("ResultSet-read error!",ex);
		} finally{
			try {
				if( rs != null )rs.close();
                db2.commit();
			} catch( SQLException ex ) {
				log.warn("ResultSet-close error!",ex);
			}
		}

		try {
            //集計テーブルの集計締日の翌日をセット
            if( param[6] != null ){
                cal2.setTime( sdf.parse( param[6] ) );
                cal2.add( Calendar.DATE, 1 );
                param[6] = sdf.format( cal2.getTime() );
            }
            //集計テーブルのデータが存在しない場合は該当学期の開始日をセット
            if( param[6] == null ){
                try {
                    KNJ_Get_Info getinfo = new KNJ_Get_Info();
                    KNJ_Get_Info.ReturnVal returnval = getinfo.Semester( db2, param[0], ( param[1].equals("9") )? "1" : param[1] );
                    param[6] = returnval.val2;					//学期期間FROM
                } catch( Exception ex ){
                    log.warn("term1 svf-out error!",ex);
                } finally {
                    if( param[6] == null ) param[6] = param[0] + "-04-01";
                    param[7] = "103";
                }
            }
            //集計テーブル締月をセット
 		} catch( Exception ex ){
			log.error("getParam2 error:" , ex);
		}
        log.debug("param[6]="+param[6]);
        log.debug("param[7]="+param[7]);
    }


	/** print設定 */
    private void setSvfInit(HttpServletResponse response ,Vrw32alp svf){
		response.setContentType("application/pdf");
		int ret = svf.VrInit();											//クラスの初期化
        if (false && 0 != ret) { ret = 0; }
		try {
			ret = svf.VrSetSpoolFileStream(response.getOutputStream());   	//PDFファイル名の設定
 		} catch( java.io.IOException ex ){
			log.error("db new error:" , ex);
		}
    }

	/** svf close */
    private void closeSvf(Vrw32alp svf){
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
		if( !nonedata ){
		 	ret = svf.VrSetForm("MES001.frm", 0);
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
			log.error("db open error!"+ex );
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
			log.error("db close error!"+ex );
		}//try-cathの括り
	}
}//クラスの括り
