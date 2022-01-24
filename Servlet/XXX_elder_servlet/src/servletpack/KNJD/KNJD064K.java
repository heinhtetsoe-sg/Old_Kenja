// kanji=漢字
/*
 * $Id: 9959d6fd863009788374de6ebc3af9cb8eb2290f $
 *
 * 作成日: 2006/03/30 14:27:31 - JST
 * 作成者: yamashiro
 *
 * Copyright(C) 2004-2009 ALP Okinawa Co.,Ltd. All rights reserved.
 */
/**
http://tokio/serv_ktest/KNJD?DBNAME=KINHDB&PRGID=KNJD064K&YEAR=2005&SEMESTER=9&CLASS_SELECTED=02K01&GRADE=02&DATE=2006/03/31&OUTPUT3=on&TESTKINDCD=0&SEME_FLG=3
 *
 *  学校教育システム 賢者 [成績管理] 前年度評価一覧
 *
 *  2006/03/30 yamashiro KNJD060K.javaを複写して作成
 *  成績一覧表KNJD060Kとの相違点(「生成一覧表（前年度）概略仕様」を踏まえた上で)
 *    ○出欠(皆勤者抽出も含めて)は、集計テーブルの前年度(4/01〜3/31)を集計して出力
 *    ○履修単位・修得単位は、生徒別に前年度の単位マスターから取得
 *    ○備考は出力桁数の制限上、留学・休学は日付を出力しない
 *    ○留年者は、学年成績、評定、欠課時数は、ブランク表示とする(仕様記載)
 *          => 成績を出力しない(集計も除外)
 *          => 留年者は、前年度の学年(出力指定学年から１を引く)をSQLの条件に入れることにより除外する
 *    ○科目単位数は前年度におけるMAX値を出力
 *
 *  2006/04/05 o-naka NO003 印刷日付を追加
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


public class KNJD064K{

    private static final Log log = LogFactory.getLog(KNJD064K.class);
    private String param[];
    private boolean nonedata;

    /**
      *
      *  KNJD.classから最初に起動されるクラス
      *
      **/
    public void svf_out(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        Vrw32alp svf = new Vrw32alp();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;              // Databaseクラスを継承したクラス

    // print svf設定
        setSvfInit(response, svf);

    // ＤＢ接続
        db2 = setDb(request);
        if( openDb(db2) ){
            log.error("db open error");
            return;
        }
        
        KNJD064K_BASE obj = new KNJD064K_BASE();
        nonedata = obj.printSvf(request, db2, svf);

    // 終了処理
        closeSvf(svf);
        closeDb(db2);

    }   //doGetの括り

    /** print設定 */
    private void setSvfInit(HttpServletResponse response ,Vrw32alp svf){
        response.setContentType("application/pdf");
        svf.VrInit();                                         //クラスの初期化
        try {
            svf.VrSetSpoolFileStream(response.getOutputStream());     //PDFファイル名の設定
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
            db2 = new DB2UDB(request.getParameter("DBNAME") , "db2inst1", "db2inst1", DB2UDB.TYPE2);    //Databaseクラスを継承したクラス
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
