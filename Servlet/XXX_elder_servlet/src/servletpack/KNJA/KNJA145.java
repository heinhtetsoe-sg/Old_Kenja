package servletpack.KNJA;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJ_Get_Info;

/**
 *
 *    学校教育システム 賢者 [学籍管理]
 *
 *                    ＜ＫＮＪＡ１４５＞  身分証明書（法政）
 *
 *    2007/04/02 nakamoto 作成日
 *
 **/

public class KNJA145 {

    private static final Log log = LogFactory.getLog(KNJA145.class);
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    private String _useAddrField2;

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws ServletException, IOException
    {

        Vrw32alp svf = new Vrw32alp();            //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                        //Databaseクラスを継承したクラス
        String param[] = new String[19];

    //  パラメータの取得
        try {
            param[0] = request.getParameter("YEAR");                            //年度
            param[1] = request.getParameter("GAKKI");                           //学期
            param[2] = request.getParameter("GRADE_HR_CLASS");                  //学年＋組
            param[3] = request.getParameter("OUTPUT");                          //フォーム種別(1:Ａ４用紙(新入生), 2:カード(在籍), 3:Ａ４用紙(在籍))
            String sdate = request.getParameter("TERM_SDATE");                  //有効期限(開始)
            String edate = request.getParameter("TERM_EDATE");                  //有効期限(終了)
            param[5] = sdate.replace('/','-');
            param[6] = edate.replace('/','-');
            param[7] = request.getParameter("DOCUMENTROOT");                    // '/usr/local/deve_oomiya/src'
            // 学籍番号の指定
            param[14] = request.getParameter("DISP");                           //2:クラス,1:個人
            String classcd[] = request.getParameterValues("category_selected");
            param[4] = "(";
            for( int ia=0 ; ia<classcd.length ; ia++ ){
                if(ia > 0) param[4] = param[4] + ",";
                if (param[14].equals("2")) param[4] = param[4] + "'" + classcd[ia] + "'";
                if (param[14].equals("1")) param[4] = param[4] + "'" + (classcd[ia]).substring(0,(classcd[ia]).indexOf("-")) + "'";
            }
            param[4] = param[4] + ")";
            _useAddrField2 = request.getParameter("useAddrField2");
        } catch( Exception ex ) {
            log.error("parameter error!");
        }

    //  print設定
        PrintWriter outstrm = new PrintWriter (response.getOutputStream());
        response.setContentType("application/pdf");

    //  svf設定
        svf.VrInit();                                   //クラスの初期化
        svf.VrSetSpoolFileStream(response.getOutputStream());          //PDFファイル名の設定

    //  ＤＢ接続
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
        } catch( Exception ex ) {
            log.error("DB2 open error!");
            return;
        }

    //  ＳＶＦ作成処理
        PreparedStatement ps1 = null;
        boolean nonedata = false;                                 //該当データなしフラグ
        setHeader(db2,svf,param);
for(int i=0 ; i<param.length ; i++) log.debug("param["+i+"]="+param[i]);
        //SQL作成
        try {
            ps1 = db2.prepareStatement(preStat1(param));        //生徒情報
        } catch( Exception ex ) {
            log.error("DB2 prepareStatement set error!");
        }
        //SVF出力
        if( setSvfout(db2,svf,param,ps1) ) nonedata = true;    //帳票出力のメソッド

log.debug("nonedata = "+nonedata);

    //  該当データ無し
        if( !nonedata ){
            svf.VrSetForm("MES001.frm", 0);
            svf.VrsOut("note" , "");
            svf.VrEndPage();
        }

    //  終了処理
        svf.VrQuit();
        preStatClose(ps1);          //preparestatementを閉じる
        db2.commit();
        db2.close();                //DBを閉じる
        outstrm.close();            //ストリームを閉じる 

    }//doGetの括り



    /** 事前処理 **/
    private void setHeader(
        DB2UDB db2,
        Vrw32alp svf,
        String param[]
    ) {
        KNJ_Get_Info getinfo = new KNJ_Get_Info();
        KNJ_Get_Info.ReturnVal returnval = null;

        if (param[3].equals("2")) svf.VrSetForm("KNJA145_1.frm", 4);//カード
        else                      svf.VrSetForm("KNJA145_2.frm", 4);//Ａ４用紙

    //  写真データ
        try {
            returnval = getinfo.Control(db2);
            param[8] = returnval.val4;      //格納フォルダ
            param[9] = returnval.val5;      //拡張子
        } catch( Exception e ){
            log.error("setHeader set error!");
        }

    //  証明書学校データ
        try {
            String sql = "SELECT CERTIF_KINDCD,SCHOOL_NAME,JOB_NAME,PRINCIPAL_NAME,REMARK1 "
                       + "FROM CERTIF_SCHOOL_DAT "
                       + "WHERE YEAR='"+param[0]+"' AND CERTIF_KINDCD IN('101','102')";
            db2.query(sql);
            ResultSet rs = db2.getResultSet();
            while( rs.next() ){
                //高校
                if (rs.getString("CERTIF_KINDCD").equals("101")) {
                    param[10] = rs.getString("SCHOOL_NAME");    //学校名
                    param[11] = rs.getString("JOB_NAME");       //役職名
                    param[12] = rs.getString("PRINCIPAL_NAME"); //校長名
                    param[13] = rs.getString("REMARK1");        //学校住所
                //中学
                } else {
                    param[15] = rs.getString("SCHOOL_NAME");    //学校名
                    param[16] = rs.getString("JOB_NAME");       //役職名
                    param[17] = rs.getString("PRINCIPAL_NAME"); //校長名
                    param[18] = rs.getString("REMARK1");        //学校住所
                }
            }
            rs.close();
            db2.commit();
        } catch( Exception e ){
            log.error("setHeader set error!");
        }

        getinfo = null;
        returnval = null;
    }


    /**Ａ４用紙・カード**/
    private boolean setSvfout(
        DB2UDB db2,
        Vrw32alp svf,
        String param[],
        PreparedStatement ps1
    ) {
        boolean nonedata = false;
        try {
            ResultSet rs = ps1.executeQuery();

            //年齢--------------
            Calendar cals = Calendar.getInstance();
            Calendar calb = Calendar.getInstance();
            cals.setTime(sdf.parse( param[5] ));                //有効期限(開始)をCalendar calに変換
            int cals_year = cals.get(Calendar.YEAR);            //calsの年を取得
            int cals_day  = cals.get(Calendar.DAY_OF_YEAR);     //calsの年の何日目を取得
            String birth_age = "";                              //年齢
            String birthday = "";                               //生年月日

            while( rs.next() ){
                //年齢--------------
                //生年月日・年齢
                svf.VrsOut("BIRTHDAY" ,"" );
                if (rs.getString("BIRTHDAY") != null) {
                    calb.setTime(sdf.parse( rs.getString("BIRTHDAY") ));//生年月日をCalendar calに変換
                    int calb_year = calb.get(Calendar.YEAR);            //calbの年を取得
                    int calb_day  = calb.get(Calendar.DAY_OF_YEAR);     //calbの年の何日目を取得
                    if (calb_day <= cals_day) birth_age = "　(" + String.valueOf(cals_year - calb_year) + "才)";
                    else                      birth_age = "　(" + String.valueOf(cals_year - calb_year - 1) + "才)";
                    birthday = (rs.getString("BIRTHDAY")).substring(0,4) + "年"
                             + String.valueOf(Integer.parseInt((rs.getString("BIRTHDAY")).substring(5,7))) + "月"
                             + String.valueOf(Integer.parseInt((rs.getString("BIRTHDAY")).substring(8,10))) + "日";
                    svf.VrsOut("BIRTHDAY" ,birthday + birth_age );
                }
                //住所１または住所２が２０文字超えたら(50)にセット
                svf.VrsOut("ADDRESS1_3" ,"" );  //住所１(50)
                svf.VrsOut("ADDRESS2_3" ,"" );  //住所２(50)
                svf.VrsOut("ADDRESS1_2" ,"" );  //住所１(50)
                svf.VrsOut("ADDRESS2_2" ,"" );  //住所２(50)
                svf.VrsOut("ADDRESS1_1" ,"" );  //住所１(40)
                svf.VrsOut("ADDRESS2_1" ,"" );  //住所２(40)
                final String addr1 = rs.getString("ADDR1");
                final String addr2 = rs.getString("ADDR2");
                if ("1".equals(_useAddrField2) &&
                        ((addr1 != null && addr1.length() > 25) || 
                         (addr2 != null && addr2.length() > 25)))
                {
                    svf.VrsOut("ADDRESS1_3" ,addr1 );  //住所１(60)
                    svf.VrsOut("ADDRESS2_3" ,addr2 );  //住所２(60)
                } else if ((addr1 != null && addr1.length() > 20) || 
                            (addr2 != null && addr2.length() > 20))
                {
                    svf.VrsOut("ADDRESS1_2" ,addr1 );  //住所１(50)
                    svf.VrsOut("ADDRESS2_2" ,addr2 );  //住所２(50)
                } else {
                    svf.VrsOut("ADDRESS1_1" ,addr1 );  //住所１(40)
                    svf.VrsOut("ADDRESS2_1" ,addr2 );  //住所２(40)
                }
                //中学・高校判定--------------
                int grade = rs.getInt("GRADE");//学年
                String coursename = "";//課程名称
                String school = "";//JorH
                String schooldiv = "";//学校指定
                if (grade < 4) {
                    school = "_J";
                    schooldiv = "義務課程";
                    svf.VrsOut("SCHOOLNAME"   , param[15] );//学校名
                    svf.VrsOut("JOBNAME"      , param[16] );//役職名
                    svf.VrsOut("STAFFNAME"    , param[17] );//校長名
                    if (null != param[18]) {
                        if ("1".equals(_useAddrField2) && param[18].length() > 25) {
                            svf.VrsOut("SCHOOLADDRESS2", param[18] );//学校住所
                        } else {
                            svf.VrsOut("SCHOOLADDRESS", param[18] );//学校住所
                        }
                    }
                } else {
                    school = "_H";
                    schooldiv = "高等課程";
                    grade = grade - 3;
                    if (rs.getString("COURSENAME") != null) coursename = rs.getString("COURSENAME") + "課程　";
                    svf.VrsOut("SCHOOLNAME"   , param[10] );//学校名
                    svf.VrsOut("JOBNAME"      , param[11] );//役職名
                    svf.VrsOut("STAFFNAME"    , param[12] );//校長名
                    if (null != param[13]) {
                        if ("1".equals(_useAddrField2) && param[13].length() > 25) {
                            svf.VrsOut("SCHOOLADDRESS2", param[13] );//学校住所
                        } else {
                            svf.VrsOut("SCHOOLADDRESS", param[13] );//学校住所
                        }
                    }
                }
                //画像--------------
                String image_pass = param[7] + "/" + param[8] + "/";//イメージパス
                //カード
                if (param[3].equals("2")) {
                    String photo_check = image_pass + "P" + rs.getString("SCHREGNO") + "." + param[9];//顔写真
                    File f1 = new File(photo_check);
                    if (f1.exists()) svf.VrsOut("PHOTO"    , photo_check );
                }
                String stamp_check = image_pass + "SCHOOLSTAMP" + school + ".jpg";  //学校長印
                String badge_check = image_pass + "SCHOOLMARK" + school + ".jpg";   //校章
                String sname_check = image_pass + "SCHOOLNAME" + school + ".jpg";   //学校名
                String bar_check = image_pass + "BAR" + ".jpg";                     //学校名のバー
                String mark_check = image_pass + "MARK" + ".jpg";                   //学校ロゴ
                File f2 = new File(stamp_check);
                File f3 = new File(badge_check);
                File f4 = new File(sname_check);
                File f5 = new File(bar_check);
                File f6 = new File(mark_check);
                if (f2.exists()) svf.VrsOut("STAMP"           , stamp_check );
                if (f3.exists()) svf.VrsOut("BITMAP1"         , badge_check );
                if (f4.exists()) svf.VrsOut("BITMAP2"+ school , sname_check );
                if (f5.exists()) svf.VrsOut("BAR"             , bar_check );
                if (f6.exists()) svf.VrsOut("MARK"            , mark_check );
                //在籍--------------
                String attendno = (rs.getString("ATTENDNO") != null) ? String.valueOf(Integer.parseInt(rs.getString("ATTENDNO"))) : "";
                String grade_hr_class = "第 " + String.valueOf(grade) + " 学年 " + rs.getInt("HR_CLASS") + "組 " + attendno + "番";
                svf.VrsOut("CLASS" ,coursename + grade_hr_class );//年組
                //その他--------------
                svf.VrsOut("SCHOOLDIV"    ,schooldiv );                           //学校指定
                svf.VrsOut("BARCODE"      ,rs.getString("SCHREGNO").substring(1));//バーコード
                svf.VrsOut("SCHREGNO"     ,rs.getString("SCHREGNO") );            //学籍番号
                svf.VrsOut("NAME"         ,rs.getString("NAME") );                //氏名(漢字)
                String sdate = (param[5]).substring(0,4) + "年"
                             + String.valueOf(Integer.parseInt((param[5]).substring(5,7))) + "月"
                             + String.valueOf(Integer.parseInt((param[5]).substring(8,10))) + "日";
                String fdate = (param[6]).substring(0,4) + "年"
                             + String.valueOf(Integer.parseInt((param[6]).substring(5,7))) + "月"
                             + String.valueOf(Integer.parseInt((param[6]).substring(8,10))) + "日";
                svf.VrsOut("SDATE"        ,sdate );                               //有効期限(開始)
                svf.VrsOut("FDATE"        ,fdate );                               //有効期限(終了)

                svf.VrEndRecord();//１行出力
                nonedata = true;
            }
            rs.close();
            db2.commit();
        } catch( Exception ex ) {
            log.error("setSvfout set error!");
        }
        return nonedata;
    }


    /**生徒情報**/
    private String preStat1(String param[])
    {
        StringBuffer stb = new StringBuffer();
    //    パラメータ（学籍番号）
        try {
            //生徒情報
            stb.append("WITH SCHNO AS ( ");
            stb.append("    SELECT * ");
            //1:新入生データ,23:在籍データ
            if (param[3].equals("1")) 
                stb.append("FROM   CLASS_FORMATION_DAT ");
            else 
                stb.append("FROM   SCHREG_REGD_DAT ");
            stb.append("    WHERE  YEAR='"+param[0]+"' AND SEMESTER='"+param[1]+"' AND ");
            //1:個人,2:クラス
            if (param[14].equals("1")) 
                stb.append("       SCHREGNO IN "+param[4]+" ");
            if (param[14].equals("2")) 
                stb.append("       GRADE||HR_CLASS IN "+param[4]+" ");
            stb.append("    ) ");

            //住所
            stb.append(",SCH_ADDR1 AS ( ");
            stb.append("    SELECT SCHREGNO,MAX(ISSUEDATE) AS ISSUEDATE ");
            stb.append("    FROM   SCHREG_ADDRESS_DAT ");
            stb.append("    WHERE  SCHREGNO IN (SELECT SCHREGNO FROM SCHNO) ");
            stb.append("    GROUP BY SCHREGNO ) ");
            stb.append(",SCH_ADDR2 AS ( ");
            stb.append("    SELECT SCHREGNO,ISSUEDATE,ADDR1,ADDR2 ");
            stb.append("    FROM   SCHREG_ADDRESS_DAT ");
            stb.append("    WHERE  SCHREGNO IN (SELECT SCHREGNO FROM SCHNO) ) ");
            stb.append(",SCH_ADDR AS ( ");
            stb.append("    SELECT W2.SCHREGNO,W2.ADDR1,W2.ADDR2 ");
            stb.append("    FROM   SCH_ADDR1 W1,SCH_ADDR2 W2 ");
            stb.append("    WHERE  W1.SCHREGNO=W2.SCHREGNO AND ");
            stb.append("           W1.ISSUEDATE=W2.ISSUEDATE ) ");

            //メイン
            stb.append("SELECT T1.SCHREGNO, ");
            stb.append("       T1.GRADE, T1.HR_CLASS, ");
            stb.append("       VALUE(T1.ATTENDNO,'0') AS ATTENDNO, ");
            stb.append("       T5.COURSENAME, ");
            if (param[3].equals("1")) {//新入生
                stb.append("   CASE WHEN T1.GRADE = '01' AND VALUE(T1.REMAINGRADE_FLG,'0') = '0' ");
                stb.append("        THEN T0.NAME ELSE T2.NAME END AS NAME, "); //氏名
                stb.append("   CASE WHEN T1.GRADE = '01' AND VALUE(T1.REMAINGRADE_FLG,'0') = '0' ");
                stb.append("        THEN T0.BIRTHDAY ELSE T2.BIRTHDAY END AS BIRTHDAY, "); //生年月日
                stb.append("   CASE WHEN T1.GRADE = '01' AND VALUE(T1.REMAINGRADE_FLG,'0') = '0' ");
                stb.append("        THEN T0.ADDR1 ELSE T3.ADDR1 END AS ADDR1, "); //住所１
                stb.append("   CASE WHEN T1.GRADE = '01' AND VALUE(T1.REMAINGRADE_FLG,'0') = '0' ");
                stb.append("        THEN T0.ADDR2 ELSE T3.ADDR2 END AS ADDR2 "); //住所２
            }
            if (param[3].equals("2") || param[3].equals("3")) {//在籍
                stb.append("   T2.NAME, T2.BIRTHDAY, T3.ADDR1, T3.ADDR2 ");
            }
            stb.append("FROM   SCHNO T1 ");
            stb.append("       LEFT JOIN FRESHMAN_DAT T0 ON T0.ENTERYEAR=T1.YEAR AND T0.SCHREGNO=T1.SCHREGNO ");
            stb.append("       LEFT JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO=T1.SCHREGNO ");
            stb.append("       LEFT JOIN SCH_ADDR T3 ON T3.SCHREGNO=T1.SCHREGNO ");
            stb.append("       LEFT JOIN COURSE_MST T5 ON T5.COURSECD=T1.COURSECD ");
            stb.append("ORDER BY T1.GRADE, T1.HR_CLASS, T1.ATTENDNO ");
        } catch( Exception e ){
            log.error("preStat1 error!");
        }
        return stb.toString();

    }//preStat1()の括り


    /**PrepareStatement close**/
    private void preStatClose(
        PreparedStatement ps1
    ) {
        try {
            ps1.close();
        } catch( Exception e ){
            log.error("preStatClose error!");
        }
    }//preStatClose()の括り



}//クラスの括り
