/**
 *
 *  学校教育システム 賢者 [学籍管理]  レポート再提出票（通信制）
 *
 *  2004/04/26 作成  m-yama
 *  2006/04/27 NO001 m-yama タイトルに再提出回数表示を追加。
 *
 */

package servletpack.KNJM;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Calendar;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class KNJM350 {

    private static final Log log = LogFactory.getLog(KNJM350.class);
    private Calendar cal1 = Calendar.getInstance( );
    private DecimalFormat dmf = new DecimalFormat();
    private String param[] = new String[11];
    private String maindata[];
    private String schno[];
    private String kai[];
    private String sai[];
    private String sem[];
    private String spdata[] = new String[7];
    private boolean nonedata;
    private StringBuffer stb;
    private String str;
    private int maincnt = 0;
    private PreparedStatement ps1;
    private String z010;

    /**
     *  HTTP Get リクエストの処理
     */
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws ServletException, IOException
    {

        Vrw32alp svf = new Vrw32alp();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;              // Databaseクラスを継承したクラス
    //  パラメータの取得
        try {
            param[0] = request.getParameter("YEAR");            //年度
            param[1] = request.getParameter("SELSUB");          //科目コード
            param[7] = request.getParameter("CNT");
            param[10] = request.getParameter("useCurriculumcd");                     //教育課程
            final String date = request.getParameter("DATE");
            if (!StringUtils.isEmpty(date)) {
                String repdate[] = StringUtils.split(date,"/");            //提出期限
                param[8] = repdate[1];            //提出期限月
                param[9] = repdate[2];            //提出期限日
            }
            maindata = new String[Integer.parseInt(param[7])];
            schno    = new String[Integer.parseInt(param[7])];
            kai      = new String[Integer.parseInt(param[7])];
            sai      = new String[Integer.parseInt(param[7])];
            sem      = new String[Integer.parseInt(param[7])];
            for (int i=0;i < Integer.parseInt(param[7]);i++){
                if (request.getParameterValues("DELCHK"+i) != null){
                    maindata[maincnt] = request.getParameter("DELCHK"+i);
                    String[] str = StringUtils.split(maindata[maincnt],":");
                    log.debug("str len = " + str.length);
                    schno[maincnt] = str[1];
                    kai[maincnt]   = str[2];
                    sai[maincnt]   = str[3];
                    sem[maincnt]   = str[5];
                    maincnt++;
                }
            }
        } catch( Exception ex ) {
            log.error("parameter error!");
        }

    // print svf設定
        setSvfInit(response, svf);

    // ＤＢ接続
        db2 = setDb(request);
        if( openDb(db2) ){
            log.error("db open error");
            return;
        }
        setNameMstZ010(db2);

    // 印刷処理
        try {
            ps1 = db2.prepareStatement( prestatementReportMax() );
        } catch( Exception ex ){
            log.error("prestatementReportHead error!" + ex);
        }
log.debug("param"+maincnt);
        for (int set = 0;set < maincnt;set++){
            printSvfReportDetail(db2, svf, schno[set], sem[set], kai[set],sai[set] );
        }
    // 終了処理
        closeSvf(svf);
        closeDb(db2);

    }   //doGetの括り
    
    
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
     *   レポート提出票印刷
     *
     */
    private void printSvfReportDetail(DB2UDB db2, Vrw32alp svf, String schno,String semestercd,String kaisuu,String saikaisuu) {

        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        ret = svf.VrSetForm("KNJM330_1.frm", 1);
        if( semestercd == null ) return;
        dmf.applyPattern("00");
        if ("kumamoto".equals(z010)) {
            svf.VrsOut("EDBOARDNAME", "熊本県教育委員会認可");
        } else {
            svf.VrsOut("EDBOARDNAME", "東京都教育委員会認可");
        }

        ResultSet rs = null;

        try{
            ps1.setString( 1, semestercd ); //科目コード
log.debug("kamoku"+String.valueOf(semestercd));
            rs = ps1.executeQuery();
            while( rs.next() ){
                ret = svf.VrsOut( "TITLE",       "レポート再提出票" + saikaisuu );          //帳票の種類 NO001
                //固定文字
                ret = svf.VrsOut( "KOTEIMONGON", "もとの提出票をはずさないで、その上にこの提出票をとめること。" );

                if ( param[4] != null  &&  0 < Integer.parseInt(param[4]) ) {
                    ret = svf.VrsOut( "MAIN_NUMBER", String.valueOf(kaisuu) );                               //提出回数
                }

                if( rs.getString("SEMESTERNAME") != null ){
                    ret = svf.VrsOut( "A_SEMESTER",  rs.getString("SEMESTERNAME") + "レポート" );   //学期
                    ret = svf.VrsOut( "D_SEMESTER",  "◆" + rs.getString("SEMESTERNAME") + "◆"  ); //学期
                }

                printSvfReportDetailOut(svf, rs, "A", kaisuu, saikaisuu);
                printSvfReportDetailOut(svf, rs, "B", kaisuu, saikaisuu);
                printSvfReportDetailOut(svf, rs, "C", kaisuu, saikaisuu);
                printSvfReportDetailOut(svf, rs, "D", kaisuu, saikaisuu);
                nonedata = true;
                ret = svf.VrEndPage();
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
    private void printSvfReportDetailOut(Vrw32alp svf, ResultSet rs, String fieldhead, String kaisuu, String saikaisuu) {

        int ret = 0;
        if (false && 0 != ret) { ret = 0; }

        try {
            printSvfSubclassnameOut( svf, fieldhead + "_SUBCLASS", rs.getString("SUBCLASSNAME") );   // 2004/12/08Modify
            ret = svf.VrsOut( fieldhead + "_NENDO",     param[0] + "年度" );                          //年度
            ret = svf.VrsOut( fieldhead + "_NUMBER",    "第" + String.valueOf(kaisuu) + "回" ); //提出回数

            if ( ! fieldhead.equals("A") ) {
                final String subclasscd = ("1".equals(param[10])) ? rs.getString("SUBCLASSCD_ONLY") : param[1];
                ret = svf.VrsOut( fieldhead + "_BARCODE",   param[0].substring(3,4) 
                                                      + String.valueOf(subclasscd)
                                                      + dmf.format(Integer.parseInt(kaisuu))
                                                      + String.valueOf(saikaisuu) );                                        //バーコード
            }
            if ( param[8] != null && param[9] != null && 
                 (fieldhead.equals("A") || fieldhead.equals("D")) ) {
                ret = svf.VrsOut(fieldhead + "_MONTH", String.valueOf(Integer.parseInt(param[8])));
                ret = svf.VrsOut(fieldhead + "_DAY", String.valueOf(Integer.parseInt(param[9])));
            }

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

        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        if( subclassname == null ) return;
        try {
            if ( subclassname.length() <= 6 ) {
                ret = svf.VrsOut( fieldname + "1",    subclassname );  //科目名
            } else if( subclassname.length() <= 15 ) {
                ret = svf.VrsOut( fieldname + "2",    subclassname );  //科目名
            } else {
                ret = svf.VrsOut( fieldname + "2",    subclassname.substring( 0, 16 ) );  //科目名
            }
        } catch( Exception ex ){
            log.error("printSvfReportDetailOut error!" + ex);
        }
    }


    /** print設定 */
    private void setSvfInit(HttpServletResponse response ,Vrw32alp svf) {

        response.setContentType("application/pdf");
        int ret = svf.VrInit();                                         //クラスの初期化
        if (false && 0 != ret) { ret = 0; }
        try {
            ret = svf.VrSetSpoolFileStream(response.getOutputStream()); //PDFファイル名の設定
        } catch( java.io.IOException ex ){
            log.error("db new error:" + ex);
        }
   }


    /** svf close */
    private void closeSvf(Vrw32alp svf) {

        int ret = 0;
        if( !nonedata ){
            ret = svf.VrSetForm("MES001.frm", 0);
            ret = svf.VrsOut("note" , "note");
            ret = svf.VrEndPage();
        }
        ret = svf.VrQuit();
        log.debug("VrQuit() = " + ret);
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
    private String prestatementReportMax()  {

        if ( stb == null ) {
            stb = new StringBuffer();
        } else {
            stb.delete( 0, stb.length() );
        }

        try{
            stb.append("SELECT t1.NAME1 as SEMESTERNAME,t2.SUBCLASSCD AS SUBCLASSCD_ONLY, t2.SUBCLASSNAME ");
            stb.append("FROM   V_NAME_MST t1,V_SUBCLASS_MST t2 ");
            stb.append("WHERE  t1.YEAR='" + param[0] + "' ");
            stb.append("       AND t1.NAMECD1 = 'M002' ");
            stb.append("       AND t1.NAMECD2 = ? ");
            stb.append("       AND t2.YEAR = '" + param[0] + "' ");
            if ("1".equals(param[10])) {
                stb.append("       AND t2.CLASSCD || '-' || t2.SCHOOL_KIND || '-' || t2.CURRICULUM_CD || '-' || t2.SUBCLASSCD = '" + param[1] + "' ");
            } else {
                stb.append("       AND t2.SUBCLASSCD = '" + param[1] + "' ");
            }
        } catch( Exception ex ){
            log.error("prestatementReportHead() error!"+ex );
        }
//log.debug("ps1"+stb);
        return stb.toString();
    }

}//クラスの括り
