/**
http://ktest/servlet_kin/KNJD?DBNAME=H051101&PRGID=KNJD060&YEAR=2005&SEMESTER=2&GRADE=03&CLASS_SELECTED=03D05&OUTPUT4=1&DATE=2005/10/30&OUTPUT3=on&TESTKINDCD=0
 *
 *	学校教育システム 賢者 [成績管理] 成績一覧
 *
 *	 2004/07/27 yamashiro KNJD060を継承して'kindai'用を作成
 *	 2004/08/18 yamashiro 講座クラスデータの同時展開の講座コードゼロに対応
 *	 2004/09/21 yamashiro 大幅仕様変更によりKNJD060から独立。
 *   2004/10/18 yamashiro 試験の公・欠のデータをKIN_RECORD_DATより出力
 *   2004/11/01 yamashiro 席次は、平均点を丸めない値で付ける
 *                        出欠集計テーブルATTEND_SEMES_DAT,_DATの変更に伴う修正
 *                        授業日数は、出欠月別累積データの同組の最大と最小を出力
 *   2004/11/03 yamashiro １科目でも欠席がある者と異動対象者は平均点と席次の対象外とし出力しない
 *                        最後の行に統計を出力する
 *   2004/11/04 yamashiro 学級の平均および個人の平均点は小数第一位を四捨五入し整数で印字
 *   2004/11/08 yamashiro ・成績の学級平均は、異動者を除いた平均を出力
 *                        ・成績の総合得点を追加
 *                        ・学級の平均および合計は、異動者を除いた平均および合計を出力
 *                        ・異動者は個人の成績は出力せず、備考にその旨を出力
 *   2004/11/09 yamashiro
 *   2004/11/10 yamashiro 総合点の平均を小数点第１位四捨五入後整数を出力する
 *   2004/11/28 yamashiro 「成績一覧表の各数値について」に伴う修正 => KNJD060K_INTER,KNJD060K_TERM,KNJD060K_GAKKI,KNJD060K_GRADEに分ける
 *   2005/02/02 yamashiro ・「欠課」と「出欠の記録」について、異動した生徒についても出力する
 *                        ・履修単位を生徒が履修した単位を合計して出力する。但し、出席の条件を満たしていること。
 *                        ・修得単位を、履修単位科目でかつ評定１以外を合計して出力する。
 *                        ・学年末において、評価読替え科目を出力する。
 *                        ・クラスの履修単位数を単位数の合計とする。
 *                        ・留学日数を出席すべき日数から除外する。休学は現時点で保留。
 *   2005/02/20 yamashiro 出欠の集計日および月の取得について KNJDivideAttendDate.classを使用
 *                        ３学期の中間試験は処理しない
 *   2005/05/30 yamashiro 一括出力対応
 *                        現在学期を指示画面より取得 <= SEME_FLG
 *
 */

package servletpack.KNJD;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class KNJD060K{

    private static final Log log = LogFactory.getLog(KNJD060K.class);
    private boolean nonedata;

	/**
	  *
	  *  KNJD.classから最初に起動されるクラス
	  *
	  **/
	public void svf_out(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		Vrw32alp svf = new Vrw32alp(); 	// PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
		DB2UDB db2 = null;				// Databaseクラスを継承したクラス

	// print svf設定
		setSvfInit(response, svf);

	// ＤＢ接続
		db2 = setDb(request);
		if( openDb(db2) ){
			log.error("db open error");
			return;
		}

        KNJD060K_BASE obj = new KNJD060K_BASE();
		nonedata = obj.printSvf(request, db2, svf);

	// 終了処理
		closeSvf(svf);
		closeDb(db2);

	}	//doGetの括り

	/** print設定 */
    private void setSvfInit(HttpServletResponse response ,Vrw32alp svf){
		response.setContentType("application/pdf");
		svf.VrInit();											//クラスの初期化
		try {
			svf.VrSetSpoolFileStream(response.getOutputStream());   	//PDFファイル名の設定
 		} catch( java.io.IOException ex ){
			log.error("db new error:" + ex);
		}
   }


	/** svf close */
    private void closeSvf(Vrw32alp svf){
		if( !nonedata ){
		 	svf.VrSetForm("MES001.frm", 0);
			svf.VrsOut("note" , "note");
			svf.VrEndPage();
		}
		svf.VrQuit();
    }


	/** DB set */
	private DB2UDB setDb(HttpServletRequest request)throws ServletException, IOException{
		DB2UDB db2 = null;
		try {
			db2 = new DB2UDB(request.getParameter("DBNAME")	, "db2inst1", "db2inst1", DB2UDB.TYPE2);	//Databaseクラスを継承したクラス
		} catch( Exception ex ){
			log.error("db new error:" + ex);
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
	}//private boolean Open_db()


	/** DB close */
	private void closeDb(DB2UDB db2){
		try {
			db2.commit();
			db2.close();
		} catch( Exception ex ){
			log.error("db close error!"+ex );
		}//try-cathの括り
	}//private Close_Db()


}//クラスの括り
