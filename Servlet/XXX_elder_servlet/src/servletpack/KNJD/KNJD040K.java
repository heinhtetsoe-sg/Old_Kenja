/**
 *	学校教育システム 賢者 [成績管理]  テスト得点未入力講座一覧
 *    KIN_RECORD_DATを使用
 *
 *  2004/05/31 yamashiro・CHAIR_DATテーブルからGROUPSEQが削除されたことによる修正
 *	2004/07/29 yamashiro・成績データは'KIN_RECORD_DAT'を使用
 *						・取り敢えず、該当テスト種別の成績がNULLではなくかつフラグがNULLではないデータを取得し、
 *						その更新日付を処理日としている。
 *  2004/10/28 yamashiro・TESTSCORE_HDAT廃止により、講座を時間割データより取得する
 *  2004/12/02 yamashiro・印刷指示画面で、未入力のみおよび全件出力の選択を追加
 *  2005/10/12 yamashiro・テスト実施の講座のみ対象とする  <change specification of 残No.11>
 */

package servletpack.KNJD;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_Control;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_Semester;


public class KNJD040K {

    private static final Log log = LogFactory.getLog(KNJD040K.class);
    Vrw32alp svf = new Vrw32alp();  //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
    DB2UDB db2;                     //Databaseクラスを継承したクラス
    boolean nonedata;               //該当データなしフラグ
    PreparedStatement ps1,ps2;
    String param[];
    String _useCurriculumcd;


    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws ServletException, IOException
    {
        KNJServletUtils.debugParam(request, log);
        log.fatal("$Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $"); // CVSキーワードの取り扱いに注意
        
            /*
                1   GAKKI           2 
                2   TEST            0101 
                    CLASS_SELECTED  01 
                    CLASS_SELECTED  02 
                    PRGID           KNJD040 
                0   YEAR            2002 
            */

        param = new String[6];
        String classcd[] = request.getParameterValues("CLASS_SELECTED");  //教科
    //  パラメータの取得
        try {
            param[0] = request.getParameter("YEAR");            //年度
            param[1] = request.getParameter("GAKKI");           //学期
            param[4] = request.getParameter("TEST");            //テスト種別
            if( request.getParameter("OUTPUT") != null ) {
                param[5] = request.getParameter("OUTPUT");      // 1=>未入力のみ 2=>全て 04/12/02追加
            }
            _useCurriculumcd = request.getParameter("useCurriculumcd");
        } catch( Exception ex ) {
            log.error("[KNJD040]parameter error!", ex);
        }

    //  print設定
        response.setContentType("application/pdf");

    //  svf設定
        svf.VrInit();                        //クラスの初期化
        svf.VrSetSpoolFileStream( response.getOutputStream() );   //PDFファイル名の設定

    //  ＤＢ接続
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
        } catch( Exception ex ) {
            log.error("[KNJD040]DB2 open error!", ex);
        }


    //  ＳＶＦ作成処理
        nonedata = false; //該当データなしフラグ
        setHead();                                          //見出し出力のメソッド
        for(int ia=0 ; ia<param.length ; ia++) {
            log.debug("[KNJD040]param[" + ia + "]=" + param[ia]);
        }
        prestatementChair();                                        //preparestatement
        prestatementHrclassname();                                      //preparestatement
        for( int ia=0 ; ia<classcd.length ; ia++ ) {
            printsvfDetail(classcd[ia]);
        }

    //  該当データ無し
        if(nonedata == false){
            svf.VrSetForm("MES001.frm", 0);
            svf.VrsOut("note" , "");
            svf.VrEndPage();
        }

    //  終了処理
        svf.VrQuit();
        preparedstatemenClose();        //preparestatement
        db2.commit();
        db2.close();        //DBを閉じる

    }//doGetの括り



    /** SVF-FORM **/
    private void setHead(){

        svf.VrSetForm( "KNJD040.frm",  4 );
        svf.VrsOut( "NENDO", nao_package.KenjaProperties.gengou(Integer.parseInt(param[0])) + "年度" ); //年度

    //  ＳＶＦ属性変更--->出力形式がクラス別の場合クラス毎に改ページ
        //svf.VrAttribute("PAGE","FF=1");

        svf.VrsOut( "TITLE",        "テスト得点未入力講座一覧");
        svf.VrsOut( "ENFORCEMENT",  "実施日");
    //  作成日(現在処理日)の取得
        try {
            KNJ_Control control = new KNJ_Control();
            KNJ_Control.ReturnVal returnval = control.Control(db2);
            svf.VrsOut( "DATE",   KNJ_EditDate.h_format_JP(returnval.val3) ); //作成日
        } catch( Exception ex ){
            log.error("[KNJD040]ctrl_date get error!", ex );
        }
    //  学期名称の取得
        try {
            KNJ_Semester semester = new KNJ_Semester();
            KNJ_Semester.ReturnVal returnval = semester.Semester(db2,param[0],param[1]);
            svf.VrsOut( "TERM",   returnval.val1 ); //学期名称
            param[2] = returnval.val2;
            param[3] = returnval.val3;
        } catch( Exception ex ){
            log.error("[KNJD040]Semester name get error!", ex );
        }
    //  テスト名称出力
        try {
            String itemname = TestName(db2,param[4]);
            svf.VrsOut( "TESTNAME",   itemname );
        } catch( Exception ex ){
            log.error("[KNJD040]testkindname error!", ex );
        }

    }//setHead()の括り
    
    public String TestName(DB2UDB db2,String itemcd){

        String itemname = null;

        try {
            String sql = null;
            sql = "SELECT "
                    + "TESTITEMNAME "
                + "FROM "
                    + "TESTITEM_MST W1 "
                + "WHERE "
                    + "W1.TESTKINDCD || W1.TESTITEMCD = '" + itemcd + "'";
            db2.query(sql);
            ResultSet rs = db2.getResultSet();
            if( rs.next() ) {
                itemname = rs.getString("TESTITEMNAME");
            }
            DbUtils.closeQuietly(rs);
        } catch( Exception e ){
            log.error("[KNJ_Testname]TestITEMname error!", e);
        }

        return itemname;
    }
    
    /**SVF-FORM**/
    private void printsvfDetail(String classcd)
    {
        try {
            int pp = 0;
            ps1.setString( ++pp, classcd ); //教科コード
            ps1.setString( ++pp, classcd ); //教科コード
            ResultSet rs = ps1.executeQuery();
            StringBuffer stb = new StringBuffer();

            while( rs.next() ){
                if( rs.getString("CLASSNAME")    != null )svf.VrsOut( "COURSE",      rs.getString("CLASSNAME")    );
                if( rs.getString("STAFFCD")      != null )svf.VrsOut( "STAFFCD",     rs.getString("STAFFCD")      );
                if( rs.getString("STAFFNAME")    != null )svf.VrsOut( "STAFFNAME",   rs.getString("STAFFNAME")    );
                if( rs.getString("GROUPABBV")    != null )svf.VrsOut( "OPTIONAL",    rs.getString("GROUPABBV")    );
                if( rs.getString("SUBCLASSNAME") != null )svf.VrsOut( "SUBJECT",     rs.getString("SUBCLASSNAME") );
                //群ＳＥＱは数値として処理する 2004/02/12
                //if( rs.getInt("GROUPSEQ")!= 0 ) svf.VrsOut("GROUP_SEQ", String.valueOf(rs.getInt("GROUPSEQ")));
                if( rs.getString("OPERATION_DATE") != null ) {
                    svf.VrsOut( "PERIOD1",  KNJ_EditDate.h_format_thi(rs.getString("OPERATION_DATE"), 1) );
                }
                //処理日を編集して出力
                if( rs.getString("UP_DATE") != null ){
                    svf.VrsOut( "RECORD_DATE",  KNJ_EditDate.h_format_thi( rs.getString("UP_DATE"), 1 ) );
                    stb = new StringBuffer();
                    stb.append( rs.getString("UP_TIME") );
                    stb.setCharAt( 2, ':' );
                    stb.setCharAt( 5, ':' );
                    svf.VrsOut( "RECORD_TIME",   stb.toString() );
                } else {
                    svf.VrsOut( "RECORD_DATE",   "未入力" );
                }
                //クラス編集
                printsvfHclassname( rs.getString("CHAIRCD") );
                svf.VrEndRecord();
                nonedata = true;
                //svffieldClear();                                          //SVFフィールド初期化
            }
            DbUtils.closeQuietly(rs);
        } catch( Exception ex ) {
            log.error("[KNJD040]printsvfDetail() read error!", ex);
        }

    }  //printsvfDetail()の括り



    /**
     *   SVF-FORM 印刷処理 受講ＨＲクラス名を編集して出力
     *
     */
    private void printsvfHclassname(String chaircd)
    {
        try {
            int pp = 0;
            ps2.setString( ++pp, chaircd ); //講座コード
            ResultSet rs = ps2.executeQuery();
            String strx = null;
            boolean first = false;

            while( rs.next() ){
                if( ! first ){
                    strx = rs.getString("HR_NAME");
                    first = true;
                } else {
                    strx = strx + "," + rs.getString("HR_NAME");
                }
            }
            DbUtils.closeQuietly(rs);
            if( first  &&  strx != null ) svf.VrsOut("CLASS"  ,strx);
        } catch( Exception ex ) {
            log.error("[KNJD040]printsvfHclassname() read error!", ex);
        }

    }  //printsvfHclassname()の括り

    /** 
     *   PrepareStatement作成 テスト得点未入力講座の表
     *     KIN_RECORD_DATを使用
     *     テスト処理日 04/10/28Modify TESTSCORE_HDAT廃止による変更
     *       TESTSCORE_HDATは使用しない => 未処理はKIN_RECORD_DATのUPDATEで判断
     **/
    void prestatementChair(){

    //  成績データの対象となるフィールドを設定
        StringBuffer stb = new StringBuffer();
        String t_field = null;
        String t_field_flg = null;
        try {
            stb.append("SEM");
            stb.append(param[1]);
            stb.append("_");
            stb.append( ((param[4].substring(0,2)).equals("01") )? "INTER_REC" : "TERM_REC" );  //中間・期末
            t_field = stb.toString();
            stb.append("_FLG");
            t_field_flg = stb.toString();
            log.debug("[KNJD040K]prestatementChair t_field="+t_field);
        } catch( Exception ex ){
            log.debug("[KNJD040K]get t_field error!", ex );
        }

    //  テスト得点講座の表
        stb = new StringBuffer();
        try {
            //05/10/12 Modify change specification of 残No.11
            stb.append("SELECT  T1.CHAIRCD, ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append(        "T1.CLASSCD || '-' || T1.SCHOOL_KIND AS CLASSCD, ");
            } else {
                stb.append(        "SUBSTR(T1.SUBCLASSCD,1,2) AS CLASSCD,");
            }
            stb.append(        "T2.CLASSNAME, ");
            stb.append(        "T1.SUBCLASSCD,T3.SUBCLASSNAME, ");
            stb.append(        "T1.GROUPCD,T4.GROUPABBV, ");
            stb.append(        "T1.OPERATION_DATE,T1.EXECUTED, ");
            stb.append(        "T5.STAFFCD,T6.STAFFNAME, ");
            stb.append(        "CASE WHEN VALUE(T1.EXECUTED,'0') = '1' THEN  DATE(T7.UPDATED) ELSE NULL END AS UP_DATE, ");
            stb.append(        "CASE WHEN VALUE(T1.EXECUTED,'0') = '1' THEN  TIME(T7.UPDATED) ELSE NULL END AS UP_TIME ");

            stb.append("FROM(");
            stb.append(  "SELECT ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append(      "CLASSCD, SCHOOL_KIND, CURRICULUM_CD,");
            }
            stb.append(          "SUBCLASSCD,");
            stb.append(          "W1.CHAIRCD,GROUPCD, ");
            stb.append(          "MIN(EXECUTEDATE) AS OPERATION_DATE,MAX(EXECUTED) AS EXECUTED ");
            stb.append(  "FROM    SCH_CHR_TEST W1 ");
            stb.append(  "INNER JOIN CHAIR_DAT W2 ON W2.YEAR = '" + param[0] + "' AND W2.SEMESTER = '" + param[1] + "' ");
            stb.append(                         "AND W2.CHAIRCD = W1.CHAIRCD ");
            stb.append(  "WHERE   W1.YEAR = '" + param[0] + "' AND W1.SEMESTER = '" + param[1] + "' ");
            stb.append(      "AND W1.TESTKINDCD||W1.TESTITEMCD = '" + param[4] + "' ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append(      "AND CLASSCD || '-' || SCHOOL_KIND = ? ");
            } else {
                stb.append(      "AND SUBSTR(W2.SUBCLASSCD,1,2) = ? ");
            }
            if( param[5].equals("1") )
                stb.append(  "AND VALUE(W1.EXECUTED,'0') = '0' ");
            stb.append("GROUP BY ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append(     "CLASSCD, SCHOOL_KIND, CURRICULUM_CD,");
            }
            stb.append(         "SUBCLASSCD,W1.CHAIRCD,GROUPCD ");
            stb.append(")T1 ");

            if ("1".equals(_useCurriculumcd)) {
                stb.append("LEFT JOIN CLASS_MST T2 ON T2.CLASSCD || '-' || T2.SCHOOL_KIND = ");
                stb.append("                          T1.CLASSCD || '-' || T1.SCHOOL_KIND ");
                stb.append("LEFT JOIN SUBCLASS_MST T3 ON T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD = ");
                stb.append("                             T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD ");
            } else {
                stb.append("LEFT JOIN CLASS_MST T2 ON T2.CLASSCD = SUBSTR(T1.SUBCLASSCD,1,2) ");
                stb.append("LEFT JOIN SUBCLASS_MST T3 ON T3.SUBCLASSCD = T1.SUBCLASSCD ");
            }
            stb.append("LEFT JOIN ELECTCLASS_MST T4 ON T4.GROUPCD = T1.GROUPCD ");
            stb.append("LEFT JOIN CHAIR_STF_DAT T5 ON T5.YEAR = '" + param[0] + "' ");
            stb.append(                          "AND T5.SEMESTER = '" + param[1] + "' ");
            stb.append(                          "AND T5.CHAIRCD = T1.CHAIRCD ");
            stb.append("LEFT JOIN STAFF_MST T6 ON T6.STAFFCD = T5.STAFFCD ");

            stb.append("LEFT JOIN (");
            stb.append(  "SELECT  ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append(      "CLASSCD, SCHOOL_KIND, CURRICULUM_CD,");
            }
            stb.append(          "SUBCLASSCD, CHAIRCD, MAX(UPDATED) AS UPDATED ");
            stb.append(  "FROM    KIN_RECORD_DAT ");
            stb.append(  "WHERE   YEAR = '" + param[0] + "' ");
            stb.append(      "AND " + t_field + " IS NOT NULL ");
            stb.append(      "AND " + t_field_flg + " IS NOT NULL ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append(      "AND CLASSCD || '-' || SCHOOL_KIND = ? ");
            } else {
                stb.append(      "AND SUBSTR(SUBCLASSCD,1,2) = ? ");
            }
            stb.append(  "GROUP BY ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append(          "CLASSCD, SCHOOL_KIND, CURRICULUM_CD,");
            }
            stb.append(          "SUBCLASSCD,CHAIRCD ");
            stb.append(")T7 ON ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append(      " T7.CLASSCD || '-' || T7.SCHOOL_KIND || '-' || T7.CURRICULUM_CD || '-' || ");
            }
            stb.append(" T7.SUBCLASSCD = ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append(      " T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
            }
            stb.append(" T1.SUBCLASSCD AND T7.CHAIRCD = T1.CHAIRCD ");

            stb.append("ORDER BY ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append(          " T7.CLASSCD || '-' || T7.SCHOOL_KIND || '-' || T7.CURRICULUM_CD, ");
            } else {
                stb.append(          "SUBSTR(T1.SUBCLASSCD,1,2), ");
            }
            stb.append(          "T5.STAFFCD,T1.GROUPCD,T1.SUBCLASSCD,T1.CHAIRCD ");

            ps1 = db2.prepareStatement(stb.toString());
        } catch( Exception ex ){
            log.debug("[KNJD040K]prestatementChair error!", ex );
        }

    }//prestatementChairの括り

    /**
     *   PrepareStatement作成
     *     講座クラスデータより対象クラスの照会
     *     04/08/18 Modify 講座クラスデータの同時展開の講座コードゼロに対応
     *
     */
    private void prestatementHrclassname() {

        try {
            String sql = 
                  "SELECT CHAIRCD,TRGTGRADE,TRGTCLASS,HR_NAME "
                + "FROM ( SELECT K2.CHAIRCD,K1.TRGTGRADE,K1.TRGTCLASS "
                       + "FROM   CHAIR_CLS_DAT K1,CHAIR_DAT K2 "
                       + "WHERE  K1.YEAR='"+param[0]+"' AND K1.SEMESTER='"+param[1]+"' AND "
                              + "K2.YEAR='"+param[0]+"' AND K2.SEMESTER='"+param[1]+"' AND "
                              + "(K1.CHAIRCD='0000000' OR K1.CHAIRCD=K2.CHAIRCD) AND "
                              + "K1.GROUPCD=K2.GROUPCD)W1,"
                       + "SCHREG_REGD_HDAT W2 "
                + "WHERE  W1.CHAIRCD=? AND W1.TRGTGRADE=W2.GRADE AND W1.TRGTCLASS=W2.HR_CLASS AND "
                       + "W2.YEAR='"+param[0]+"' AND W2.SEMESTER='"+param[1]+"' "
                + "ORDER BY TRGTGRADE,TRGTCLASS";

            ps2 = db2.prepareStatement(sql);
        } catch( Exception ex ){
            log.error("[KNJD040]prestatementHrclassname error!", ex );
        }

    }//prestatementHrclassnameの括り


    /**PrepareStatement close**/
    private void preparedstatemenClose() {
        DbUtils.closeQuietly(ps1);
        DbUtils.closeQuietly(ps2);
    }

}
