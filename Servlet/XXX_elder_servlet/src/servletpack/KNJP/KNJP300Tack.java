/**
 *
 * $Id: 35a1575175e665043e131246c9dfdac3e2ed1563 $
 * 学校教育システム 賢者 [学籍管理]  タックシール印刷
 *
 * 2005/06/01  作成 m-yama
 *
 * 2007/05/14  m-yama   NO001 frmID変更 KNJA190K→KNJP300_3
 *                      NO002 上記修正を破棄し、KNJA190Kと同様に修正
 */

package servletpack.KNJP;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.Calendar;

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

public class KNJP300Tack {

    private static final Log log = LogFactory.getLog(KNJP300Tack.class);
    private Calendar cal1 = Calendar.getInstance( );
    private DecimalFormat dmf = new DecimalFormat();
    private String param[] = new String[9];
    private String maindata[];
    private String schno[];
    private String grade[];
    private String hrclass[];
    private String attend[];
    private String arrangementno[];
    private String classcd[];
    private String spdata[] = new String[7];
    private boolean nonedata;
    private boolean jhighschool = false;
    private StringBuffer stb;
    private String str;
    private PreparedStatement ps1;
    private PreparedStatement ps2;
    private PreparedStatement ps3;

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
            param[1] = request.getParameter("SEMESTER");        //学期
            param[5] = request.getParameter("APPLICATION");     //学期
            param[4] = request.getParameter("OUTPUT");          //印刷指定
            param[6] = "様";                                    //  様
            param[8] = request.getParameter("TAISYOSYA");       //対象者
            //学籍番号・学年・組・番号
            classcd = request.getParameterValues("category_selected");
        } catch( Exception ex ) {
            log.error("parameter error!" + ex);
        }

        // print svf設定
        setSvfInit(response, svf);

        // ＤＢ接続
        db2 = setDb(request);
        if( openDb(db2) ){
            log.error("db open error");
            return;
        }

        // param項目分解処理
        try {
            if (param[4].equals("1")){
                Set_Param_Data1(request);
            }else {
                Set_Param_Data2(request,db2);
            }
        } catch( Exception ex ){
            log.error("Set_Param_Data error!" + ex);
        }

        try {
            StringBuffer sbx = new StringBuffer();
            sbx.append("(");
            for(int ia=0 ; ia<schno.length ; ia++){
                if(schno[ia] == null)   break;
                if(ia>0)    sbx.append(",");
                sbx.append("'");
                sbx.append(schno[ia]);
                sbx.append("'");
            }
            sbx.append(")");
            param[2] = sbx.toString();
        } catch( Exception ex ){
            log.error("Set_Param_Data error!" + ex);
        }

        // 印刷処理
        Set_Detail(db2,svf,param);  //印刷処理

        // 終了処理
        closeSvf(svf);
        closeDb(db2);

    }   //doGetの括り

    /**
     *   PARAM設定1
     *
     */
    private void Set_Param_Data1(HttpServletRequest request){
        //サイズ確定
        schno         = new String[classcd.length];
        grade         = new String[classcd.length];
        hrclass       = new String[classcd.length];
        attend        = new String[classcd.length];
        arrangementno = new String[classcd.length];
        for (int i=0;i < classcd.length;i++){
            String[] str = StringUtils.split(classcd[i],":");
            schno[i]         = str[0];
            grade[i]         = str[1];
            hrclass[i]       = str[2];
            attend[i]        = str[3];
        }
    }

    /**
     *   PARAM設定2
     *
     */
    private void Set_Param_Data2(HttpServletRequest request,DB2UDB db2){

        try{
            String sqlclass = "(";
            for (int i=0;i < classcd.length;i++){
                if (i > 0)sqlclass += ",";
                sqlclass += "'" + classcd[i] + "'";
            }
            sqlclass += ")";
            
        // SQL作成処理
            try {
                ps1 = db2.prepareStatement( prestatementSchget(sqlclass) );
            } catch( Exception ex ){
                log.error("prestatementReportHead error!" + ex);
            }
            ResultSet rs = ps1.executeQuery();
            int sccnt = 0;
            while( rs.next() ){
                sccnt++;
            }
            ps1.close();

            //サイズ確定
            schno         = new String[sccnt];
            grade         = new String[sccnt];
            hrclass       = new String[sccnt];
            attend        = new String[sccnt];
            arrangementno = new String[sccnt];

        // SQL作成処理
            try {
                ps1 = db2.prepareStatement( prestatementSchget(sqlclass) );
            } catch( Exception ex ){
                log.error("prestatementReportHead error!" + ex);
            }
            ResultSet rs1 = ps1.executeQuery();
            sccnt = 0;
            while( rs1.next() ){
                schno[sccnt]         = rs1.getString("SCHREGNO");
                grade[sccnt]         = rs1.getString("GRADE");
                hrclass[sccnt]       = rs1.getString("HR_CLASS");
                attend[sccnt]        = rs1.getString("ATTENDNO");
                sccnt++;
            }
            ps1.close();

        } catch( Exception ex ){
            log.error("Set_Param_Data2 error!" + ex);
        }
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

    /** 印刷処理 **/
    private void Set_Detail(DB2UDB db2,Vrw32alp svf,String param[]){

        try {
            db2.query( Set_Stat1(param));
            ResultSet rs = db2.getResultSet();

            svf.VrSetForm("KNJA190K.frm", 4); //NO001
            int ib = 0; //列
log.debug("1111111111");
            while( rs.next() ){
log.debug("2222222222");
                if( ib==3 ){
                    svf.VrEndRecord();
                    nonedata = true;
                    ib = 0;
                    for( int ic=1 ; ic<4 ; ic++ ){
                        svf.VrsOut("ZIPCODE"+ic    ,"");  //郵便番号
                        svf.VrsOut("ADDRESS1_"+ic  ,"");  //住所
                        svf.VrsOut("SCHOOLNAME"+ic ,"");  //氏名
                        svf.VrsOut("STUDENT"+ic    ,"");  //学級名称＋生徒名
                    }
log.debug("3333333333");
                }
                ib++;
                svf.VrsOut("ZIPCODE"+ib    ,"〒" + rs.getString("ZIPCD"));        //郵便番号
                printAddress(svf, rs.getString("ADDR1"), rs.getString("ADDR2"), ib);
                svf.VrsOut("SCHOOLNAME"+ib ,h_finschoolname(rs.getString("NAME"),param[6]));  //氏名
log.debug("4444444444");
                svf.VrsOut("STUDENT"+ib    ,"("+rs.getString("HRCLASS_NAME")+"  "
                                                +rs.getString("SCH_NAME")+")" );    //学級名称＋生徒名
            }
            db2.commit();
            rs.close();
            if( ib>0){
                svf.VrEndRecord();
                nonedata = true;
            }
        } catch( Exception ex ){
            log.error("[KNJP300Tack]boolean Set_Detail() read error!" + ex);
        }

        if( nonedata ) svf.VrPrint();

    }//boolean Set_Detail()の括り

    /**
     * 住所を印字します。
     * @param svf
     * @param addr1 住所1
     * @param addr2 住所2
     * @param ib 列名（1〜3）
     */
    private void printAddress(
            final Vrw32alp svf,
            String addr1, 
            final String addr2,
            final int ib
    ) {
        if (null != addr1 && 3 <= addr1.length()) {
            if ("大阪府".equals(addr1.substring(0,3))) {
                addr1 = addr1.substring(3);
            }
        }
        boolean overAddr1Len = isOverStringLength(addr1, 40);
        boolean overAddr2len = isOverStringLength(addr2, 40);
        String addressFieldDiv = (overAddr1Len || overAddr2len)? "2": "1";
        if (null != addr1) {
            svf.VrsOut("ADDRESS1_" + ib + "_" + addressFieldDiv, addr1);
        }
        if (null != addr2) {
            svf.VrsOut("ADDRESS2_" + ib + "_" + addressFieldDiv, addr2);
        }
    }

    
   /**
    * @param str
    * @param i
    * @return String str の長さ(byte)が int i を超えた場合Trueを戻します。
    */
    public static boolean isOverStringLength(
           final String str, 
           final int i
    ) {
        if (null == str) { return false; }
        if (0 == str.length()) { return false; }
        byte arrbyte[] = new byte[i + 2];
        try {
            arrbyte = str.getBytes( "MS932" );
        } catch (UnsupportedEncodingException e) {
            log.error("UnsupportedEncodingException", e);
        }
       return (i < arrbyte.length);
    }

    /** 氏名の編集 **/
    public String h_finschoolname(String finschoolname1,String finschoolname2){

        StringBuffer finschoolname = new StringBuffer();
        try {
            if(finschoolname1!=null){
                finschoolname.append(finschoolname1);
                byte SendB[] = new byte[50];
                SendB=finschoolname1.getBytes();

                int j=0;
                if(SendB.length>18) j=2;

                for(int i=SendB.length ; i<(22*j-2) ; i++) {
                    finschoolname.append(" ");
                }

                if(j == 0){
                    finschoolname.append(" ");
                    finschoolname.append(" ");
                }
                if(finschoolname2!=null)    finschoolname.append(finschoolname2);
            }
            if(finschoolname==null) finschoolname.append(" ");
        } catch( Exception ex ) {
            log.error("[KNJA190K]h_finschoolname error!" + ex);
        }
        return finschoolname.toString();
    }//h_finschoolnameの括り

    /** svf close */
    private void closeSvf(Vrw32alp svf) {

        if( !nonedata ){
            svf.VrSetForm("MES001.frm", 0);
            svf.VrsOut("note" , "note");
            svf.VrEndPage();
        }
        int ret = svf.VrQuit();
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
     *  SQL-STATEMENT作成 生徒情報
     *
     */
    private String prestatementSchget(String sqlclass)  {

        if( stb == null ) stb = new StringBuffer();
        else              stb.delete( 0, stb.length() );

        try{
            stb.append(" WITH ATABLE AS ( ");
            stb.append(" SELECT ");
            stb.append("     SCHREGNO ");
            stb.append(" FROM ");
            stb.append("     APPLICATION_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + param[0] + "' AND ");
            stb.append("     APPLICATIONCD = '" + param[5] + "' ");
            if ("2".equals(param[8])) {
                stb.append("     AND VALUE(INT(APPLI_PAID_MONEY), 0) <= 0 ");
            }
            stb.append(" GROUP BY ");
            stb.append("     SCHREGNO ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     t2.SCHREGNO,t2.GRADE,t2.HR_CLASS,t2.ATTENDNO ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT t2 ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + param[0] + "' AND ");
            stb.append("     GRADE || HR_CLASS in "+sqlclass+" AND ");
            stb.append("     EXISTS (SELECT 'X' ");
            stb.append("             FROM ATABLE t1 ");
            stb.append("             WHERE t2.SCHREGNO = t1.SCHREGNO) ");
            stb.append(" GROUP BY ");
            stb.append("     t2.SCHREGNO,t2.GRADE,t2.HR_CLASS,t2.ATTENDNO ");
            stb.append(" ORDER BY ");
            stb.append("     t2.GRADE,t2.HR_CLASS,t2.ATTENDNO ");

        } catch( Exception ex ){
            log.error("prestatementReportHead() error!"+ex );
        }
//log.debug("ps1"+stb);
        return stb.toString();
    }

    /** 保護者出力用ＳＱＬ **/
    private String Set_Stat1(String param[]){

        StringBuffer stb = new StringBuffer();
        try {
            stb.append("SELECT VALUE(GUARD_ZIPCD,'')AS ZIPCD,");
            stb.append(       "GUARD_ADDR1 AS ADDR1,GUARD_ADDR2 AS ADDR2,");
            stb.append(       "GUARD_NAME AS NAME,W3.NAME AS SCH_NAME,W4.HR_NAME AS HRCLASS_NAME ");
            stb.append("FROM   SCHREG_REGD_DAT W1,GUARDIAN_DAT W2,SCHREG_BASE_MST W3,SCHREG_REGD_HDAT W4 ");
            stb.append("WHERE  W1.SCHREGNO IN"+param[2]+"AND ");
            stb.append(       "W1.YEAR='"+param[0]+"'AND W1.SEMESTER='"+param[1]+"'AND W1.SCHREGNO=W2.SCHREGNO AND ");
            stb.append(       "W1.SCHREGNO=W3.SCHREGNO AND W4.YEAR='"+param[0]+"'AND W4.SEMESTER='"+param[1]+"'AND ");
            stb.append(       "W4.YEAR=W1.YEAR AND W4.SEMESTER=W1.SEMESTER AND W4.GRADE=W1.GRADE AND W4.HR_CLASS=W1.HR_CLASS ");
            stb.append("ORDER BY W1.GRADE,W1.HR_CLASS,W1.ATTENDNO");
        } catch( Exception ex ){
            log.error("[KNJP300Tack]String Set_Stat1() error!" + ex);
        }

        return stb.toString();

    }//String Set_Stat1()の括り

}//クラスの括り
