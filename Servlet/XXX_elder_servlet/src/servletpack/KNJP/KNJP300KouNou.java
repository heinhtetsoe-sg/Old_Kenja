/**
 *
 * $Id: fd4075eec58b8ba6ec7f3b5084bfbb223d6f77f6 $
 * 学校教育システム 賢者 [学籍管理]  校納金振込み用紙印刷
 *
 * 2005/06/01  作成 m-yama
 *
 */

package servletpack.KNJP;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJ_EditDate;

public class KNJP300KouNou {

    private static final Log log = LogFactory.getLog(KNJP300KouNou.class);
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
    private static final String HOKYOU_KAI = "0001";

    private Map hiraTokata = new HashMap(); //ひらがなからカタカナの変換用

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
            param[1] = request.getParameter("APPLICATION");     //費目コード
            param[2] = request.getParameter("DATE");            //納入期限日
            param[3] = request.getParameter("OUTPUT2");         //用紙指定
            param[4] = request.getParameter("OUTPUT");          //印刷指定
            param[5] = request.getParameter("SEMESTER");        //学期
            param[8] = request.getParameter("TAISYOSYA");       //対象者
            //学籍番号・学年・組・番号
            classcd = request.getParameterValues("category_selected");
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

        // FRM設定処理
        try {
            Set_Frm(db2,svf,param);                             //FRM設定のメソッド
        } catch( Exception ex ){
            log.error("Set_Frm error!" + ex);
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

        //マッピング設定
        setMapKana();   // NO001

        // 印刷処理
        for (int set = 0;set < schno.length;set++){
            svf.VrsOut("ERA_NAME", KNJ_EditDate.gengou(db2, Integer.parseInt(param[0])));
            SetBankData(db2,svf,schno[set],grade[set],hrclass[set],attend[set]);
            SetSchData(db2,svf,schno[set],grade[set],hrclass[set],attend[set]);
            if( nonedata )  svf.VrEndPage();
        }
        // 終了処理
        closeSvf(svf);
        closeDb(db2);

    }   //doGetの括り

    /**
     *   FRM設定
     *
     */
    private void Set_Frm(DB2UDB db2,Vrw32alp svf,String param[]){

        if (param[3].equals("1")) {
            svf.VrSetForm("KNJP300_1.frm", 1);
        } else if (HOKYOU_KAI.equals(param[1])) {
            svf.VrSetForm("KNJP300_4.frm", 1);
        } else {
            svf.VrSetForm("KNJP300_2.frm", 1);
        }
log.debug("フォーム種別" + String.valueOf(param[3]));
        //学校名称
        ResultSet rs  = null;
        try{
            db2.query("SELECT SCHOOLNAME1 FROM SCHOOL_MST WHERE YEAR= '" + param[0] + "'");
            rs = db2.getResultSet();
            if( rs.next() ) param[6] = rs.getString(1);
        } catch( Exception ex ){
            log.error("getThisYear error!" + ex);
        } finally{
            if( rs != null )try{rs.close();}catch( SQLException ey ){log.error("rs.close error!" + ey );}
        }

        if( param[6] == null ) param[6] = "";

        ResultSet rs2 = null;
        try{
            db2.query("SELECT SCHOOLNAME1 FROM SCHOOL_MST WHERE YEAR= '" + param[0] + "' AND SCHOOLNAME1 LIKE '%中学%'");
            rs2 = db2.getResultSet();
            if( rs2.next() ) jhighschool = true;
        } catch( Exception ex ){
            log.error("getThisYear error!" + ex);
        } finally{
            if( rs != null )try{rs.close();}catch( SQLException ey ){log.error("rs.close error!" + ey );}
        }

    }//Set_Frm()の括り

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
    /**
     *   銀行印刷
     *
     */
    private void SetBankData(
        DB2UDB db2,
        Vrw32alp svf,
        String schno,
        String grade,
        String hrclass,
        String attend ){

        try{
        // SQL作成処理
            try {
                ps2 = db2.prepareStatement( prestatementBankget() );
            } catch( Exception ex ){
                log.error("prestatementBankget error!" + ex);
            }
log.debug(String.valueOf(grade) + String.valueOf(hrclass) + String.valueOf(attend));
            ps2.setString( 1, grade + hrclass );    //年組
            ResultSet rs = ps2.executeQuery();
            while( rs.next() ){
                if (rs.getString("BANKNAME") != null && rs.getString("BRANCHNAME") != null ){
                    svf.VrsOut( "BANK1", rs.getString("BANKNAME") + "　" + rs.getString("BRANCHNAME") );
                    svf.VrsOut( "BANK3", rs.getString("BANKNAME") + "　" + rs.getString("BRANCHNAME") );
                }else if (rs.getString("BANKNAME") == null && rs.getString("BRANCHNAME") == null){
                    svf.VrsOut( "BANK1", "" );
                    svf.VrsOut( "BANK3", "" );
                }else if (rs.getString("BRANCHNAME") == null){
                    svf.VrsOut( "BANK1", rs.getString("BANKNAME") );
                    svf.VrsOut( "BANK3", rs.getString("BANKNAME") );
                }else {
                    svf.VrsOut( "BANK1", rs.getString("BRANCHNAME") );
                    svf.VrsOut( "BANK3", rs.getString("BRANCHNAME") );
                }

                if (!param[2].equals("")) {
                    svf.VrsOut( "DEADLINE",KNJ_EditDate.h_format_US_Y(param[2]) + "年" + KNJ_EditDate.h_format_JP_MD(param[2]) );
                }
                byte check_len[] = new byte[40];
                check_len = (rs.getString("ACCOUNTNAME")).getBytes("MS932");
                if (check_len.length > 40){
                    svf.VrsOut( "ACCOUNTNAME1_1", rs.getString("ACCOUNTNAME") );
                    svf.VrsOut( "ACCOUNTNAME2_1", rs.getString("ACCOUNTNAME") );
                    svf.VrsOut( "ACCOUNTNAME3_1", rs.getString("ACCOUNTNAME") );
                }else {
                    svf.VrsOut( "ACCOUNTNAME1", rs.getString("ACCOUNTNAME") );
                    svf.VrsOut( "ACCOUNTNAME2", rs.getString("ACCOUNTNAME") );
                    svf.VrsOut( "ACCOUNTNAME3", rs.getString("ACCOUNTNAME") );
                }
                svf.VrsOut( "ACCOUNTNO", rs.getString("ACCOUNTNO") );
                svf.VrsOut( "ITEM1", rs.getString("APPLICATIONNAME") );
                svf.VrsOut( "ITEM2", rs.getString("APPLICATIONNAME") );
                svf.VrsOut( "ITEM2_1", rs.getString("APPLICATIONNAME") );
                svf.VrsOut( "ITEM2_2", rs.getString("APPLICATIONNAME") );
                svf.VrsOut( "ITEM3", rs.getString("APPLICATIONNAME") + "振込依頼書" );
                svf.VrsOut( "DEPOSIT", rs.getString("NAME1") );
                svf.VrsOut( "REFERENCE", rs.getString("BANK_MAJORCD") + String.valueOf(grade) + rs.getString("BANK_HR_CLASS")  + attend.substring(1,3) );
                svf.VrsOut( "HANDLING_BANK", "(取扱銀行→" + rs.getString("BANKNAME") + "→" + String.valueOf(param[6]) + ")" );
                svf.VrsOut( "SCHOOLNAME", String.valueOf(param[6]) );

log.debug(rs.getString("APPLICATIONMONEY"));

                nonedata = true;
            }
            ps2.close();
        } catch( Exception ex ){
            log.error("SetBankData error!" + ex);
        }
    }
    /**
     *   生徒情報印刷
     *
     */
    private void SetSchData(
        DB2UDB db2,
        Vrw32alp svf,
        String schno,
        String grade,
        String hrclass,
        String attend ){

        try{
            String AdressSet;
        // SQL作成処理
            try {
                ps3 = db2.prepareStatement( prestatementSchinfget(schno) );
            } catch( Exception ex ){
                log.error("prestatementSchinfget error!" + ex);
            }
log.debug(String.valueOf(grade) + String.valueOf(hrclass) + String.valueOf(attend));
            ps3.setString( 1, grade + hrclass );    //年組
            ResultSet rs = ps3.executeQuery();
            while( rs.next() ){
                //郵便番号
                svf.VrsOut("ZIPCD", rs.getString("GUARD_ZIPCD"));

                if (rs.getString("GUARD_ADDR1") != null && rs.getString("GUARD_ADDR2") != null ){
                    AdressSet = rs.getString("GUARD_ADDR1") + "　" + rs.getString("GUARD_ADDR2");
                }else if (rs.getString("GUARD_ADDR1") == null && rs.getString("GUARD_ADDR2") == null){
                    AdressSet = "";
                }else if (rs.getString("GUARD_ADDR2") == null){
                    AdressSet = rs.getString("GUARD_ADDR1");
                }else {
                    AdressSet = rs.getString("GUARD_ADDR2");
                }
                byte check_len[] = new byte[40];
                check_len = (String.valueOf(AdressSet)).getBytes("MS932");
log.debug("レングス" + check_len.length);
                if (check_len.length > 40){
                    svf.VrsOut( "ADDRESS1_1", String.valueOf(AdressSet) );
                    svf.VrsOut( "ADDRESS2_1", String.valueOf(AdressSet) );
                    svf.VrsOut( "ADDRESS3_1", String.valueOf(AdressSet) );
                }else {
                    svf.VrsOut( "ADDRESS1", String.valueOf(AdressSet) );
                    svf.VrsOut( "ADDRESS2", String.valueOf(AdressSet) );
                    svf.VrsOut( "ADDRESS3", String.valueOf(AdressSet) );
                }
                String hr_nameabbv = rs.getString("HR_NAMEABBV");
                if (!jhighschool){
                    if (hr_nameabbv.substring(1,2).equals("J")){
                        hr_nameabbv = hr_nameabbv.substring(0,2) + hr_nameabbv.substring(3,4);
                    }else {
                        hr_nameabbv = hr_nameabbv.substring(1,4);
                    }
                }
log.debug(String.valueOf(hr_nameabbv));
                svf.VrsOut( "PHONE", rs.getString("GUARD_TELNO") );
                final String sama = param[3].equals("2") ? "　様" : "";
                final String setSama = null != rs.getString("GUARD_NAME") ? sama : "";
                svf.VrsOut( "GUARDNAME", rs.getString("GUARD_NAME") + setSama);
                svf.VrsOut( "GUARDNAME1", rs.getString("GUARD_NAME"));
                final String major = jhighschool ? "" : rs.getString("MAJORNAME");
                svf.VrsOut( "CLASS1", major + String.valueOf(Integer.parseInt(String.valueOf(grade))) + "年　" + String.valueOf(hr_nameabbv) + "組　" + String.valueOf(Integer.parseInt(String.valueOf(attend))) + "番" );
                svf.VrsOut( "CLASS2", major + String.valueOf(Integer.parseInt(String.valueOf(grade))) + "年　" + String.valueOf(hr_nameabbv) + "組　" + String.valueOf(Integer.parseInt(String.valueOf(attend))) + "番" );
                svf.VrsOut( "NAME1", rs.getString("NAME") + sama);
                svf.VrsOut( "NAME2", rs.getString("NAME") );
                svf.VrsOut( "NAME2_1", rs.getString("NAME") );
                svf.VrsOut( "NAME2_2", rs.getString("NAME") );
                svf.VrsOut( "NAME3", rs.getString("NAME") );
                if( rs.getString("NAME_KANA") != null ){
                    svf.VrsOut("KANA"     ,getConvertKana(rs.getString("NAME_KANA")) );           //ふりがな NO001 05/01/08Modify
                }
log.debug(rs.getString("GUARD_NAME"));
                svf.VrsOut( "MONEY1", rs.getString("APPLI_MONEY_DUE") );
                svf.VrsOut( "MONEY2", rs.getString("APPLI_MONEY_DUE") );
                svf.VrsOut( "MONEY3", rs.getString("APPLI_MONEY_DUE") );

                nonedata = true;
            }
            ps3.close();
        } catch( Exception ex ){
            log.error("SetBankData error!" + ex);
        }
    }

    /**
     *   ひらがな→カタカナ変換用マッピング
     *
     */
    private void setMapKana() {
        String obj1[] = {"あ","い","う","え","お","か","き","く","け","こ","さ","し","す","せ","そ",
                         "た","ち","つ","て","と","な","に","ぬ","ね","の","は","ひ","ふ","へ","ほ",
                         "ま","み","む","め","も","や","ゆ","よ","ら","り","る","れ","ろ","わ","を","ん",
                         "ぱ","ぴ","ぷ","ぺ","ぽ","っ","ゃ","ゅ","ょ","ぁ","ぃ","ぅ","ぇ","ぉ",
                         "が","ぎ","ぐ","げ","ご","ざ","じ","ず","ぜ","ぞ","だ","ぢ","づ","で","ど",
                         "ば","び","ぶ","べ","ぼ","　"};
        String obj2[] = {"ア","イ","ウ","エ","オ","カ","キ","ク","ケ","コ","サ","シ","ス","セ","ソ",
                         "タ","チ","ツ","テ","ト","ナ","ニ","ヌ","ネ","ノ","ハ","ヒ","フ","ヘ","ホ",
                         "マ","ミ","ム","メ","モ","ヤ","ユ","ヨ","ラ","リ","ル","レ","ロ","ワ","ヲ","ン",
                         "パ","ピ","プ","ペ","ポ","ッ","ャ","ュ","ョ","ァ","ィ","ゥ","ェ","ォ",
                         "ガ","ギ","グ","ゲ","ゴ","ザ","ジ","ズ","ゼ","ゾ","ダ","ヂ","ヅ","デ","ド",
                         "バ","ビ","ブ","ベ","ボ"," "};
        for( int i=0 ; i<obj1.length ; i++ )hiraTokata.put( obj1[i],obj2[i] );
    }

    /**
     *   ひらがな→カタカナ変換
     *
     */
    private String getConvertKana(String kana)
    {
        StringBuffer stb = new StringBuffer();
        if( kana != null ){
            for( int i=0 ; i<kana.length() ; i++ ){
                if( hiraTokata.get(kana.substring(i,i+1)) == null ){
                    stb.append( kana.substring(i,i+1) );
log.info("kana = "+kana.substring(i,i+1));
                } else {
                    stb.append( (hiraTokata.get(kana.substring(i,i+1))) );
                }
            }
        }
        stb.append("");
        return stb.toString();
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
            stb.append("     APPLICATIONCD = '" + param[1] + "' ");
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

    /**
     *  SQL-STATEMENT作成 銀行情報
     *
     */
    private String prestatementBankget()    {

        if( stb == null ) stb = new StringBuffer();
        else              stb.delete( 0, stb.length() );

        try{
            stb.append(" SELECT ");
            stb.append("     t1.APPLICATIONNAME,t1.APPLICATIONMONEY,t1.ACCOUNTNO,t1.ACCOUNTNAME, ");
            stb.append("     t2.BANKNAME,t2.BRANCHNAME, ");
            stb.append("     t3.NAME1, ");
            stb.append("     t4.BANK_MAJORCD,t4.GRADE,t4.BANK_HR_CLASS ");
            stb.append(" FROM APPLICATION_MST t1 ");
            stb.append("     LEFT JOIN BANK_MST t2 ON t1.BANKCD = t2.BANKCD ");
            stb.append("     AND t1.BRANCHCD = t2.BRANCHCD ");
            stb.append("     LEFT JOIN NAME_MST t3 ON t1.DEPOSIT_ITEM = t3.NAMECD2 ");
            stb.append("     AND t3.NAMECD1 = 'G203', ");
            stb.append("     BANK_CLASS_MST t4 ");
            stb.append(" WHERE ");
            stb.append("     t1.YEAR = '" + param[0] + "' AND ");
            stb.append("     t1.APPLICATIONCD = '" + param[1] + "' AND ");
            stb.append("     t4.YEAR = '" + param[0] + "' AND ");
            stb.append("     t4.GRADE || t4.HR_CLASS = ? ");

        } catch( Exception ex ){
            log.error("prestatementReportHead() error!"+ex );
        }
//log.debug("ps1"+stb);
        return stb.toString();
    }

    /**
     *  SQL-STATEMENT作成 生徒個人情報
     *
     */
    private String prestatementSchinfget(String schno)  {

        if( stb == null ) stb = new StringBuffer();
        else              stb.delete( 0, stb.length() );

        try{
            stb.append(" WITH ATABLE AS ( ");
            stb.append(" SELECT ");
            stb.append("     SCHREGNO,MAX(ISSUEDATE) AS ISSUEDATE ");
            stb.append(" FROM ");
            stb.append("     SCHREG_ADDRESS_DAT ");
            stb.append(" WHERE ");
            stb.append("     SCHREGNO = '" + schno + "' ");
            stb.append(" GROUP BY ");
            stb.append("     SCHREGNO ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     t2.NAME,t2.NAME_KANA, ");
            stb.append("     t3.MAJORNAME, ");
            stb.append("     t4.ADDR1,t4.ADDR2,t4.TELNO, ");
            stb.append("     t6.GUARD_ADDR1,t6.GUARD_ADDR2,t6.GUARD_NAME,t6.GUARD_TELNO, t6.GUARD_ZIPCD, ");
            stb.append("     t7.HR_NAME,t7.HR_NAMEABBV,t8.APPLI_MONEY_DUE ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT t1 ");
            stb.append("     LEFT JOIN V_MAJOR_MST t3 ON t1.COURSECD = t3.COURSECD ");
            stb.append("     AND t1.MAJORCD = t3.MAJORCD ");
            stb.append("     AND t3.YEAR = '" + param[0] + "', ");
            stb.append("     SCHREG_BASE_MST t2 ");
            stb.append("     LEFT JOIN APPLICATION_DAT t8 ON t2.SCHREGNO = t8.SCHREGNO ");
            stb.append("     AND t8.YEAR = '" + param[0] + "' ");
            stb.append("     AND t8.APPLICATIONCD = '" + param[1] + "', ");
            stb.append("     SCHREG_ADDRESS_DAT t4, ");
            stb.append("     ATABLE t5, ");
            stb.append("     GUARDIAN_DAT t6, ");
            stb.append("     SCHREG_REGD_HDAT t7 ");
            stb.append(" WHERE ");
            stb.append("     t1.YEAR = '" + param[0] + "' AND ");
            stb.append("     t1.SEMESTER = '" + param[5] + "' AND ");
            stb.append("     t1.SCHREGNO = '" + schno + "' AND ");
            stb.append("     t2.SCHREGNO = '" + schno + "' AND ");
            stb.append("     t4.SCHREGNO = '" + schno + "' AND ");
            stb.append("     t4.ISSUEDATE = t5.ISSUEDATE AND ");
            stb.append("     t6.SCHREGNO = '" + schno + "' AND ");
            stb.append("     t7.YEAR = '" + param[0] + "' AND ");
            stb.append("     t7.SEMESTER = '" + param[5] + "' AND ");
            stb.append("     t7.GRADE || t7.HR_CLASS = ? ");


log.debug(stb);
        } catch( Exception ex ){
            log.error("prestatementReportHead() error!"+ex );
        }
//log.debug("ps1"+stb);
        return stb.toString();
    }

}//クラスの括り
