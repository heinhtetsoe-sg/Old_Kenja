// kanji=漢字
/*
 * $Id: e3b99dc10f3b11c4381b59dbffc46ca9d5df1124 $
 *
 * 作成日: 2009/02/13 14:42:08 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2014 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJM;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Calendar;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 *  学校教育システム 賢者 [学籍管理]  レポート提出票（通信制）
 *
 *  2004/04/26  作成 m-yama
 *
 */
public class KNJM330 {

    private static final Log log = LogFactory.getLog(KNJM330.class);
    private Calendar cal1 = Calendar.getInstance( );
    private DecimalFormat dmf = new DecimalFormat();
    private String param[];
    private boolean nonedata;
    private StringBuffer stb;
    private String str;
    private PreparedStatement ps1, ps2 ,ps3;
    private String z010;

    /**
     *  HTTP Get リクエストの処理
     */
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws ServletException, IOException
    {

        Vrw32alp svf = new Vrw32alp();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;              // Databaseクラスを継承したクラス

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

        log.fatal("$Revision: 56595 $");
    // 印刷処理
        printSvf(db2, svf);

    // 終了処理
        closeSvf(svf);
        closeDb(db2);

    }   //doGetの括り



    /* svf print 印刷処理 */
    private void printSvf(DB2UDB db2, Vrw32alp svf) {

        try {
            setNameMstZ010(db2);
            setHead(db2);
            ps1 = db2.prepareStatement( prestatementReportHead() );
            ps2 = db2.prepareStatement( prestatementReportDetail() );
            ps3 = db2.prepareStatement( prestatementReportMax() );
            printSvfReportHead(db2, svf);
        } catch( Exception ex ){
            log.error("prestatementReportHead error!" + ex);
        }
    }
    
    
    /**
     *  SQL-STATEMENT作成 レポート課題集
     *
     */
    private void setNameMstZ010(final DB2UDB db2)  {
        String sql = " SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' ";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            if (rs.next()) z010 = rs.getString("NAME1");
        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }

    /**
     *   レポート課題集印刷
     *
     */
    private void printSvfReportHead(DB2UDB db2, Vrw32alp svf) {

        ResultSet rs = null;
        try {
            rs = ps1.executeQuery();
            String subclasscode = "0";  //科目コードの保存
            int outkaisu = 0;           //出力した行数（回数）

            while (rs.next()) {
                //科目コードの変わり目
                if (!subclasscode.equals( rs.getString("SUBCLASSCD"))) {

                    if (param[1].equals("2") && param[3].equals("2")) {
                        subclasscode = rs.getString("SUBCLASSCD");
                        break;
                    }
                    if (!subclasscode.equals("0")) {
                        svf.VrPrint();
                        printSvfReportDetail(db2, svf, subclasscode);
                        outkaisu = 0;
                    }

                    printSvfReportHeadSetform(svf, rs.getString("SUBCLASSCD"));
                    subclasscode = rs.getString("SUBCLASSCD");
                    printSvfSubclassnameOut( svf, "SUBCLASS", rs.getString("SUBCLASSNAME") );   // 2004/12/08Modify
                    svf.VrsOut( "NENDO",       param[5] );
                    svf.VrsOut( "SCHOOLNAME",  param[6] );
                }

                if( rs.getInt("STANDARD_SEQ") <= outkaisu ) continue;
                //提出番号の編集
                if( stb == null ) stb = new StringBuffer();
                else              stb.delete( 0, stb.length() );
                stb.append( dmf.format(Integer.parseInt(rs.getString("STANDARD_SEQ"))) );
                if( rs.getInt("STANDARD_SEQ") < 12  &&  stb.length() < 3 ) stb.insert(1, " ");
                else if( rs.getInt("STANDARD_SEQ") == 12  &&  stb.length() < 4 ) stb.insert(1, " ");

                svf.VrsOut( "SEMESTER",    rs.getString("SEMESTERNAME") );   //学期
                svf.VrsOut( "NUMBER",    stb.toString() );   //提出番号
                //提出日の編集
                if( rs.getString("STANDARD_DATE") != null ){
                    cal1.setTime( rs.getDate("STANDARD_DATE") );
                    svf.VrsOut( "DEADLINE_M",  String.valueOf(cal1.get(Calendar.MONTH) + 1 ) + "月" );  //提出日
                    svf.VrsOut( "DEADLINE_D",  String.valueOf(cal1.get(Calendar.DATE) ) + "日" );  //提出日
                }
                svf.VrEndRecord();
            }
            outkaisu++;
            nonedata = true;
            if (!subclasscode.equals("0")) {
                svf.VrPrint();
                printSvfReportDetail(db2, svf, subclasscode);
            }
        } catch( Exception ex ){
            log.error("printSvfReportHead error!" + ex);
        } finally{
            if( rs != null )try{rs.close();}catch( SQLException ey ){log.error("rs.close error!" + ey );}
        }
    }


    /**
     *   レポート課題集 SVF-FORM設定
     *
     */
    private void printSvfReportHeadSetform(Vrw32alp svf, String subcls) {

        ResultSet rs = null;
        try{
log.debug("1");
            ps3.setString( 1, subcls ); //科目コード
log.debug("2");
            rs = ps3.executeQuery();
log.debug("3");
            rs.next();
log.debug("4");
            //フォームの設定
log.debug(param[1]+"  "+param[3]);
            if (!(param[1].equals("2") && param[3].equals("2"))){
log.debug("5");
                if( rs.getInt("STANDARD_SEQ") <= 6 )      svf.VrSetForm("KNJM330_2.frm", 4);
                else if( rs.getInt("STANDARD_SEQ") <= 9 ) svf.VrSetForm("KNJM330_3.frm", 4);
                else                  svf.VrSetForm("KNJM330_4.frm", 4);
                if ("kumamoto".equals(z010)) {
                    svf.VrsOut("EDBOARDNAME", "熊本県教育委員会認可");
                } else {
                    svf.VrsOut("EDBOARDNAME", "東京都教育委員会認可");
                }
            }
log.debug("6");
        } catch( Exception ex ){
            log.error("printSvfReportHead error!" + ex);
        } finally{
            if( rs != null )try{rs.close();}catch( SQLException ey ){log.error("rs.close error!" + ey );}
        }

        try{
            dmf.applyPattern("第#回");
        } catch( Exception ex ){
            log.error("printSvfReportHead error!" + ex);
        }
    }


    /**
     *   レポート提出票印刷
     *
     */
    private void printSvfReportDetail(DB2UDB db2, Vrw32alp svf, String subclasscode) {

        svf.VrSetForm("KNJM330_1.frm", 1);
        if( subclasscode == null ) return;
        dmf.applyPattern("00");
        ResultSet rs = null;

        try{
            ps2.setString( 1, subclasscode );   //科目コード
log.debug("kamoku"+String.valueOf(subclasscode));
            rs = ps2.executeQuery();
            while( rs.next() ){
                if ("kumamoto".equals(z010)) {
                    svf.VrsOut("EDBOARDNAME", "熊本県教育委員会認可");
                } else {
                    svf.VrsOut("EDBOARDNAME", "東京都教育委員会認可");
                }

                if (null != rs.getString("LINE_PRINT") && rs.getString("LINE_PRINT").equals("1")) {
                    svf.VrAttribute( "LINE", "Paint=(15,0,1)" );
                    svf.VrsOut("LINE", "a");
                    svf.VrAttribute( "LINE2", "Paint=(15,0,1)" );
                    svf.VrsOut("LINE2", "a");
                }
                svf.VrsOut( "TITLE",       "レポート提出票" );                           //帳票の種類

                if( param[4] != null  &&  0 < Integer.parseInt(param[4]) )
                    svf.VrsOut( "MAIN_NUMBER", param[4] );                               //提出回数

                if( rs.getString("SEMESTERNAME") != null ){
                    svf.VrsOut( "A_SEMESTER",  rs.getString("SEMESTERNAME") + "レポート" );   //学期
                    svf.VrAttribute( "D_SEMESTER", "Paint=(15,100,1)" );
                    svf.VrsOut( "D_SEMESTER",  "◆" + rs.getString("SEMESTERNAME") + "◆"  ); //学期
                }

                printSvfReportDetailOut(svf, rs, "A");
                printSvfReportDetailOut(svf, rs, "B");
                printSvfReportDetailOut(svf, rs, "C");
                printSvfReportDetailOut(svf, rs, "D");

                svf.VrEndPage();
            }
        } catch( Exception ex ){
            log.error("printSvfReportDetail error!" + ex);
        } finally{
            if( rs != null )try{rs.close();}catch( SQLException ey ){log.error("rs.close error!" + ey );}
        }
    }


    /**
     *   レポート提出票印刷
     *
     */
    private void printSvfReportDetailOut(Vrw32alp svf, ResultSet rs, String fieldhead) {


        try {
            if( fieldhead.equals("A")  ||  fieldhead.equals("D") )
                if( rs.getDate("STANDARD_DATE") != null ){
                    cal1.setTime( rs.getDate("STANDARD_DATE") );
                    svf.VrsOut( fieldhead + "_MONTH",  String.valueOf(cal1.get(Calendar.MONTH) + 1 ) );  //提出期限
                    svf.VrsOut( fieldhead + "_DAY",    String.valueOf(cal1.get(Calendar.DATE) ) );       //提出期限
                }

            //if( rs.getString("SUBCLASSNAME") != null )
            //    svf.VrsOut( fieldhead + "_SUBCLASS",  rs.getString("SUBCLASSNAME") );           //科目名
            printSvfSubclassnameOut( svf, fieldhead + "_SUBCLASS", rs.getString("SUBCLASSNAME") );   // 2004/12/08Modify
            svf.VrsOut( fieldhead + "_NENDO",     param[0] + "年度" );                          //年度
            svf.VrsOut( fieldhead + "_NUMBER",    "第" + rs.getString("STANDARD_SEQ") + "回" ); //提出回数

            if( ! fieldhead.equals("A") ) {
                final String subclasscd = "1".equals(param[7]) ? rs.getString("SUBCLASSCD_ONLY") : rs.getString("SUBCLASSCD");
                svf.VrsOut( fieldhead + "_BARCODE",   param[0].substring(3,4) 
                                                      + subclasscd
                                                      + dmf.format(rs.getInt("STANDARD_SEQ"))
                                                      + "0" );
            }                                        //バーコード
        } catch( Exception ex ){
            log.error("printSvfReportDetailOut error!" + ex);
        }
    }


    /**
     *   科目名印刷処理
     *     2004/12/08 正式名称を、６文字以内かどうかで文字の大きさを変更( SVF-FORMで設定 )、１０文字超え分をカット
     *
     */
    private void printSvfSubclassnameOut(Vrw32alp svf, String fieldname, String subclassname ) {

        if( subclassname == null ) return;
        try {
            if( subclassname.length() <= 6 )
                svf.VrsOut( fieldname + "1",    subclassname );  //科目名
            else
                svf.VrsOut( fieldname + "2",    subclassname );  //科目名
        } catch( Exception ex ){
            log.error("printSvfReportDetailOut error!" + ex);
        }
    }



    /**
     *
     *   年度取得（編集後）
     *
     */
    private void setHead(DB2UDB db2) {


        //年度
        try{
            param[5] = nao_package.KenjaProperties.gengou(Integer.parseInt(param[0])) + "年度 (" + param[0] + ")";
        } catch( Exception ex ){
            log.error("setHead error!" + ex);
        }

        //学校名称
        ResultSet rs = null;
        try{
            db2.query("SELECT SCHOOLNAME1 FROM SCHOOL_MST WHERE YEAR= '" + param[0] + "'");
            rs = db2.getResultSet();
            if( rs.next() ) param[6] = rs.getString(1);
        } catch( Exception ex ){
            log.error("getThisYear error!" + ex);
        } finally{
            if( rs != null )try{rs.close();}catch( SQLException ey ){log.error("rs.close error!" + ey );}
        }

        if( param[5] == null ) param[5] = "";
        if( param[6] == null ) param[6] = "";
    }


    /** get parameter doGet()パラメータ受け取り */
    private void getParam(HttpServletRequest request) {

        param = new String[9];
        try {
            param[0] = request.getParameter("YEAR");    //年度
            param[1] = request.getParameter("OUTPUT");  //印刷パターン  初回=1,再提出=2
            param[7] = request.getParameter("useCurriculumcd");                     //教育課程
            //再提出のみ有効
            if( param[1].equals("2") ){
                param[2] = request.getParameter("KAMOKU");  //科目コード
                if (request.getParameter("HYOUSI") != null){
                    param[3] = request.getParameter("HYOUSI");  //表紙出力
                }else {
                    param[3] = "2";  //表紙出力
                }
                param[4] = request.getParameter("TKAISU");  //提出回数
            }else {
                param[3] = "2";  //表紙出力無し
            }
log.debug("param3="+param[3]);
        } catch( Exception ex ) {
            log.error("get parameter error!" + ex);
        }
log.debug("tkaisu="+request.getParameter("TKAISU"));
for( int i=0 ; i<param.length ; i++ )log.debug("param["+i+"]="+param[i]);
    }


    /** print設定 */
    private void setSvfInit(HttpServletResponse response ,Vrw32alp svf) {

        response.setContentType("application/pdf");
        svf.VrInit();                                         //クラスの初期化
        try {
            svf.VrSetSpoolFileStream(response.getOutputStream()); //PDFファイル名の設定
        } catch( java.io.IOException ex ){
            log.error("db new error:" + ex);
        }
   }


    /** svf close */
    private void closeSvf(Vrw32alp svf) {

        if( !nonedata ){
            svf.VrSetForm("MES001.frm", 0);
            svf.VrsOut("note" , "");
            svf.VrEndPage();
        }
        svf.VrQuit();
    }


    /** DB set */
    private DB2UDB setDb(HttpServletRequest request)throws ServletException, IOException {

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
    private boolean openDb(DB2UDB db2) {

        try {
            db2.open();
        } catch( Exception ex ){
            log.error("db open error!"+ex );
            return true;
        }//try-cathの括り

        return false;

    }//private boolean Open_db()


    /** DB close */
    private void closeDb(DB2UDB db2) {

        try {
            db2.commit();
            db2.close();
        } catch( Exception ex ){
            log.error("db close error!"+ex );
        }//try-cathの括り
    }//private Close_Db()


    /**
     *  SQL-STATEMENT作成 レポート課題集
     *
     */
    private String prestatementReportHead() {

        if( stb == null ) stb = new StringBuffer();
        else              stb.delete( 0, stb.length() );

        try{
            stb.append("SELECT ");
            if ("1".equals(param[7])) {
                stb.append("     W1.CLASSCD || W1.SCHOOL_KIND || W1.CURRICULUM_CD || W1.SUBCLASSCD AS SUBCLASSCD, ");
            } else {
                stb.append("     W1.SUBCLASSCD, ");
            }
            stb.append("       W3.SUBCLASSNAME, W1.STANDARD_SEQ, ");
            stb.append("       W1.STANDARD_DATE,case when N1.NAME1 = '最終回' then '後期' else N1.NAME1 end AS SEMESTERNAME ");
            stb.append("FROM   REP_STANDARDDATE_DAT W1 ");
            stb.append("       LEFT JOIN V_SUBCLASS_MST W3 ON W1.SUBCLASSCD = W3.SUBCLASSCD AND W3.YEAR = '" + param[0] + "' ");
            if ("1".equals(param[7])) {
                stb.append("       AND W1.CLASSCD = W3.CLASSCD ");
                stb.append("       AND W1.SCHOOL_KIND = W3.SCHOOL_KIND ");
                stb.append("       AND W1.CURRICULUM_CD = W3.CURRICULUM_CD ");
            }
            stb.append("       LEFT JOIN V_NAME_MST N1 ON W1.REPORTDIV = N1.NAMECD2 ");
            stb.append("       AND N1.YEAR = '" + param[0] + "' ");
            stb.append("       AND N1.NAMECD1 = 'M002' ");
            stb.append("WHERE  W1.YEAR='" + param[0] + "' ");
            if (param[1].equals("2")){
                if ("1".equals(param[7])) {
                    stb.append("     AND W1.CLASSCD || '-' || W1.SCHOOL_KIND || '-' || W1.CURRICULUM_CD || '-' || W1.SUBCLASSCD ='" + param[2] + "' ");
                } else {
                    stb.append("     AND W1.SUBCLASSCD='" + param[2] + "' ");
                }
            }
            stb.append("ORDER BY ");
            if ("1".equals(param[7])) {
                stb.append("       W1.CLASSCD, ");
                stb.append("       W1.SCHOOL_KIND, ");
                stb.append("       W1.CURRICULUM_CD, ");
            }
            stb.append("    W1.SUBCLASSCD, W1.STANDARD_SEQ ");
        } catch( Exception ex ){
            log.error("prestatementReportHead() error!"+ex );
        }
//log.debug("ps1"+stb);
        return stb.toString();
    }


    /**
     *  SQL-STATEMENT作成 レポート提出票
     *
     */
    private String prestatementReportDetail()   {

        if( stb == null ) stb = new StringBuffer();
        else              stb.delete( 0, stb.length() );

        try{
            stb.append("SELECT ");
            if ("1".equals(param[7])) {
                stb.append("     W1.SUBCLASSCD AS SUBCLASSCD_ONLY, ");
                stb.append("     W1.CLASSCD || W1.SCHOOL_KIND || W1.CURRICULUM_CD || W1.SUBCLASSCD AS SUBCLASSCD, ");
            } else {
                stb.append("     W1.SUBCLASSCD, ");
            }
            stb.append("    W2.SUBCLASSNAME, ");
            stb.append("    W1.STANDARD_SEQ, ");
            stb.append("    W1.STANDARD_DATE, ");
            stb.append("    W1.REPORTDIV, ");
            stb.append("    W3.NAME1 AS SEMESTERNAME, ");
            stb.append("    VALUE(W3.NAMESPARE1, '0') AS LINE_PRINT ");
            stb.append("FROM ");
            stb.append("    REP_STANDARDDATE_DAT W1 ");
            stb.append("    LEFT JOIN V_SUBCLASS_MST W2 ON W1.SUBCLASSCD = W2.SUBCLASSCD  ");
            if ("1".equals(param[7])) {
                stb.append("       AND W1.CLASSCD = W2.CLASSCD ");
                stb.append("       AND W1.SCHOOL_KIND = W2.SCHOOL_KIND ");
                stb.append("       AND W1.CURRICULUM_CD = W2.CURRICULUM_CD ");
            }
            stb.append("    AND W2.YEAR = '" + param[0] + "' ");
            stb.append("    LEFT JOIN V_NAME_MST W3 ON W1.REPORTDIV = W3.NAMECD2 ");
            stb.append("    AND W3.YEAR = '" + param[0] + "' ");
            stb.append("    AND W3.NAMECD1 = 'M002' ");
            stb.append("WHERE ");
            stb.append("    W1.YEAR = '" + param[0] + "' ");
            if ("1".equals(param[7])) {
                stb.append("     AND W1.CLASSCD || W1.SCHOOL_KIND || W1.CURRICULUM_CD || W1.SUBCLASSCD = ? ");
            } else {
                stb.append("     AND W1.SUBCLASSCD = ? ");
            }
            if (param[1].equals("2")){
                stb.append("    AND W1.STANDARD_SEQ = " + param[4] + " ");
            }
            stb.append("ORDER BY ");
            if ("1".equals(param[7])) {
                stb.append("       W1.CLASSCD, ");
                stb.append("       W1.SCHOOL_KIND, ");
                stb.append("       W1.CURRICULUM_CD, ");
            }
            stb.append("    W1.SUBCLASSCD, ");
            stb.append("    W1.STANDARD_SEQ ");
//log.debug(stb);
        } catch( Exception ex ){
            log.error("prestatementReportDetail() error!"+ex );
        }
        return stb.toString();
    }


    /**
     *  SQL-STATEMENT作成 レポート課題集
     *
     */
    private String prestatementReportMax()  {

        if( stb == null ) stb = new StringBuffer();
        else              stb.delete( 0, stb.length() );

        try{
            stb.append("SELECT ");
            if ("1".equals(param[7])) {
                stb.append("     CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD AS SUBCLASSCD, ");
            } else {
                stb.append("     SUBCLASSCD, ");
            }
            stb.append("    MAX(STANDARD_SEQ) AS STANDARD_SEQ ");
            stb.append("FROM   REP_STANDARDDATE_DAT ");
            stb.append("WHERE  YEAR='" + param[0] + "' ");
            if ("1".equals(param[7])) {
                stb.append("     AND CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD = ? ");
            } else {
                stb.append("     AND SUBCLASSCD = ? ");
            }
            stb.append("GROUP BY ");
            if ("1".equals(param[7])) {
                stb.append("       CLASSCD, ");
                stb.append("       SCHOOL_KIND, ");
                stb.append("       CURRICULUM_CD, ");
            }
            stb.append("    SUBCLASSCD ");
            stb.append("ORDER BY ");
            if ("1".equals(param[7])) {
                stb.append("       CLASSCD, ");
                stb.append("       SCHOOL_KIND, ");
                stb.append("       CURRICULUM_CD, ");
            }
            stb.append("    SUBCLASSCD ");
        } catch( Exception ex ){
            log.error("prestatementReportHead() error!"+ex );
        }
//log.debug("ps1"+stb);
        return stb.toString();
    }

}//クラスの括り
