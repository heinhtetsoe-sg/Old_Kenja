/**
 *
 *    学校教育システム 賢者 [成績管理] 成績伝票（考査：１学期２回、２学期２回、３学期１回）
 *
 *    2015/02/19 o-naka KNJD124Fを元に新規作成
 */

package servletpack.KNJD;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJC.KNJDivideAttendDate;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_Get_Info;

public class KNJD124H {

    private static final Log log = LogFactory.getLog(KNJD124H.class);
    private DecimalFormat dmf1 = new DecimalFormat("00");
    /** 伝票明細件数ＭＡＸ */
    private static final int LINE_MAX = 50;
    private KNJSchoolMst _knjSchoolMst;

    /**
     * KNJD.classから最初に呼ばれる処理
     */
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws ServletException, IOException
    {
        Vrw32alp svf = new Vrw32alp();
        DB2UDB db2 = null;
        String param[] = new String[23];

        //パラメータの取得
        //String printname = request.getParameter("PRINTNAME");             //プリンタ名
        String printname = null;                                            //PDFで出力用！！
        try {
            log.fatal("$Revision: 56595 $"); // CVSキーワードの取り扱いに注意
            param[0]  = request.getParameter("YEAR");                       //年度
            param[1]  = request.getParameter("SEMESTER");                   //1-3:学期
            param[2]  = request.getParameter("CHAIRCD");                    //講座コード
            param[3]  = request.getParameter("SUBCLASSCD");                 //科目コード
            param[4]  = request.getParameter("STAFF");                      //ログイン者コード
            param[5]  = request.getParameter("TEST_DATE");                  //試験日
            param[9]  = request.getParameter("ATTENDSUBYEAR");              //累積現在日・年
            param[10] = request.getParameter("ATTENDSUBMONTH");             //累積現在日・月
            param[11] = request.getParameter("ATTENDSUBDAY");               //累積現在日・日
            param[12] = request.getParameter("COUNTFLG"); //テスト項目マスタ
            param[13] = request.getParameter("CHIKOKU_HYOUJI_FLG"); // 累積情報の遅刻・早退欄のフラグ(1なら遅刻・早退回数、1以外なら欠課換算後の余り)
            param[17] = request.getParameter("useCurriculumcd"); //プロパティ(教育課程コード)(1:教育課程対応)
            //1:評定、2:仮評
            param[18]  = request.getParameter("PROV");
            param[19] = request.getParameter("useVirus"); //プロパティ(ウイルス)(true:使用する)
            param[20] = request.getParameter("useKoudome"); //プロパティ(交止)(true:使用する)
            param[21] = request.getParameter("sort"); //ソート
            param[22] = request.getParameter("useAssessSubclassMst"); //プロパティ(科目別評定マスタ)
        } catch( Exception ex ) {
            log.error("parameter error!", ex);
        }

        //print設定-->printnameが存在する-->プリンターへ直接出力の場合
        PrintWriter outstrm = new PrintWriter (response.getOutputStream());
        if (printname != null)  response.setContentType("text/html");
        else                    response.setContentType("application/pdf");

        //svf設定-->printnameが存在する-->プリンターへ直接出力の場合
        int ret = svf.VrInit();
        if (printname != null) {
            ret = svf.VrSetPrinter("", printname);//プリンタ名の設定
            if( ret < 0 ) log.info("printname ret = " + ret);
        } else {
            ret = svf.VrSetSpoolFileStream(response.getOutputStream());//PDFファイル名の設定
        }

        //ＤＢ接続
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
        } catch( Exception ex ) {
            log.error("DB2 open error!", ex);
        }

        //ＳＶＦ作成処理
        boolean nonedata = false;
        setHead(db2,svf,param);
        if( printSvfMain(db2,svf,param) )nonedata = true;

        //該当データ無し-->printnameが存在する-->プリンターへ直接出力の場合
        if (printname != null) {
            if (!nonedata)  outstrm.println("<H1>対象データはありません。</h1>");
            else            outstrm.println("<H1>印刷しました。</h1>");
        } else if (!nonedata) {
            ret = svf.VrSetForm("MES001.frm", 0);
            ret = svf.VrsOut("note" , "note");
            ret = svf.VrEndPage();
        }

        //終了処理
        ret = svf.VrQuit();
        if (ret == 0) log.info("===> VrQuit():" + ret);
        db2.commit();
        db2.close();
        outstrm.close();

    }//doGetの括り


    /**
     * SVF-FORMセット＆見出し項目
     */
    private void setHead(DB2UDB db2,Vrw32alp svf,String param[]) {
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        ret = svf.VrSetForm("KNJD124H.frm", 4);
        log.info("===> VrSetForm():" + ret);
        KNJ_Get_Info getinfo = new KNJ_Get_Info();
        KNJ_Get_Info.ReturnVal returnval = null;
        ret = svf.VrsOut("NENDO",nao_package.KenjaProperties.gengou(Integer.parseInt(param[0]))+"年度");
        //作成日(現在処理日)
        try {
            returnval = getinfo.Control(db2);
            ret = svf.VrsOut("DATE",KNJ_EditDate.h_format_JP(returnval.val3));
        } catch( Exception ex ){
            log.error("ReturnVal setHead() get TODAY error!", ex );
        }
        //欠課数換算定数取得
        try {
            _knjSchoolMst = new KNJSchoolMst(db2, param[0]);
            log.debug("absentCov = " + _knjSchoolMst._absentCov + ", absentCovLate = " + _knjSchoolMst._absentCovLate);
        } catch( Exception ex ){
            log.warn("semesterdiv-get error!",ex);
        }
        //科目名
        String ChairCredit = "" ;
        ResultSet rs = null;
        try {
            db2.query( prestatementHeadSubclass(param) );
            rs = db2.getResultSet();
            if (rs.next()) {
                ChairCredit = rs.getString("SUBCLASSNAME");
                log.debug("kamokuset"+String.valueOf(ChairCredit));
            }
            rs.close();
        } catch( Exception ex ){
            log.error("setHead_ hrclass_staff error!", ex );
        }
        //単位数 
        try {
            db2.query( prestatementHeadCredit(param) );
            rs = db2.getResultSet();
            if (rs.next()) {
                if (rs.getString("MAXCREDITS").equalsIgnoreCase(rs.getString("MINCREDITS"))) {
                    ChairCredit = ChairCredit + "(" + rs.getString("MAXCREDITS") + "単位)";
                    log.debug("1kaime"+String.valueOf(ChairCredit));
                } else {
                    ChairCredit = ChairCredit + "(" + rs.getString("MINCREDITS") + " \uFF5E ";
                    ChairCredit = ChairCredit + rs.getString("MAXCREDITS") + "単位)";
                    log.debug("2kaime"+String.valueOf(ChairCredit));
                }
                ret = svf.VrsOut("SUBCLASS",String.valueOf(ChairCredit));
            } else {
                ret = svf.VrsOut("SUBCLASS",String.valueOf(ChairCredit) + "(　単位)");
            }
            rs.close();
        } catch( Exception ex ){
            log.error("setHead_ hrclass_staff error!", ex );
        }
        //講座名 
        try {
            db2.query( prestatementHeadChair(param) );
            rs = db2.getResultSet();
            if (rs.next()) {
                ret = svf.VrsOut("HR_CLASS",rs.getString("CHAIRNAME"));
            }
            rs.close();
        } catch( Exception ex ){
            log.error("setHead_ hrclass_staff error!", ex );
        }
        //担当者名
        String Staffcd = "";
        try {
            db2.query( prestatementHeadStaff(param) );
            rs = db2.getResultSet();
            if (rs.next()) {
                Staffcd = String.valueOf(param[4]);
                ret = svf.VrsOut("STAFFNAME1",rs.getString("STAFFNAME"));
            }
            rs.close();
        } catch( Exception ex ){
            log.error("setHead_ hrclass_staff error!", ex );
        }
        //担当者名2
        try {
            int staffcnt = 0 ;
            String Staffname = "" ;
            db2.query( prestatementHeadStaff2(param) );
            rs = db2.getResultSet();
            while (rs.next()) {
                log.debug(rs.getString("STAFFCD")+rs.getString("STAFFNAME"));
                if (staffcnt == 2) break;
                if (!Staffcd.equalsIgnoreCase(rs.getString("STAFFCD"))) {
                    if (staffcnt == 1) {
                        Staffname = Staffname + "," + rs.getString("STAFFNAME");
                    } else {
                        Staffname = rs.getString("STAFFNAME");
                    }
                    staffcnt++;
                }
            }
            ret = svf.VrsOut("STAFFNAME2",String.valueOf(Staffname));
            rs.close();
        } catch( Exception ex ){
            log.error("setHead_ hrclass_staff error!", ex );
        }
        //累積情報日付
        try {
            if (param[11] == null) param[11] = "";
            if (NumberUtils.isDigits(param[9]) && NumberUtils.isDigits(param[10]) && Integer.parseInt(param[10]) <= 3) { // 1月,2月,3月は年は年度+1
                param[9] = String.valueOf(Integer.parseInt(param[9]) + 1);
            }
            ret = svf.VrsOut("P_DATE",param[9]+"年"+param[10]+"月"+param[11]+ "日");
        } catch( Exception ex ){
            log.error("ReturnVal setHead() get P_DATE error!", ex );
        }
        getinfo = null;
        returnval = null;
        getParam2( db2, param );
        // 学期名・テスト項目名
        try {
            db2.query(getTestName(param));
            rs = db2.getResultSet();
            while (rs.next()) {
                String seme     = rs.getString("VALUE_SEME");
                String semeName = rs.getString("SEMESTERNAME");
                String test     = rs.getString("VALUE_TEST");
                String testName = rs.getString("TESTITEMNAME");

                if ("DUMMY".equals(getTestField(test))) continue;
                svf.VrsOut("SEMESTER" + seme  , semeName);
                svf.VrsOut(getTestField(test) , testName);
            }
            rs.close();
        } catch( Exception ex ){
            log.error("setHead() getTestName error!", ex );
        }
        // 評定項目名（1:評定、2:仮評）
        try {
            String provName = "2".equals(param[18]) ? "仮評" : "評定";
            svf.VrsOut("PROV_NAME"  , provName);
        } catch( Exception ex ){
            log.error("setHead() provName error!", ex );
        }
    }//setHead()の括り

    private String getTestField(String test) {
        if ("10101".equals(test)) return "TESTNAME1_1";
        if ("10201".equals(test)) return "TESTNAME1_2";
        if ("20101".equals(test)) return "TESTNAME2_1";
        if ("20201".equals(test)) return "TESTNAME2_2";
        if ("30201".equals(test)) return "TESTNAME3_2";
        return "DUMMY";
    }

    private String getTestName(String param[]) {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append(" SELECT ");
            stb.append("     T1.SEMESTERNAME, ");
            stb.append("     T1.SEMESTER as VALUE_SEME, ");
            stb.append("     rtrim(ltrim(substr(T2.TESTITEMNAME,1,15))) as TESTITEMNAME, "); //頭全角３文字
            stb.append("     T1.SEMESTER || T2.TESTKINDCD || T2.TESTITEMCD AS VALUE_TEST ");
            stb.append(" FROM ");
            stb.append("     SEMESTER_MST T1, ");
            stb.append("     " + param[12] + " T2 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = T2.YEAR AND ");
            stb.append("     T1.YEAR = '" + param[0] + "' ");
            if ("TESTITEM_MST_COUNTFLG_NEW".equals(param[12])) {
                stb.append("     AND T1.SEMESTER = T2.SEMESTER ");
            }
            stb.append(" ORDER BY ");
            stb.append("     VALUE_TEST ");
        } catch( Exception ex ){
            log.error("getTestName error!", ex );
        }
        return stb.toString();
    }

    /**
     * SVF-FORM メイン出力処理
     */
    private boolean printSvfMain(DB2UDB db2,Vrw32alp svf,String param[]) {
        for(int i = 0 ; i < param.length ; i++ )log.debug("param[" + i + "] = " + param[i] );
        //定義
        boolean nonedata = false;
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        ResultSet rs = null;
        int total[][] = {{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}};   //合計,平均,最高点,最低点
        //RecordSet作成
        try {
            db2.query( prestatementRecord(param,0) );            //生徒別成績データ
            rs = db2.getResultSet();
            int linex = 1;                                        //１ページ当り出力行数
            while( rs.next() ){
                if( printSvfOutMeisai( svf, rs, param, total, linex ) ){    //SVF-FORMへ出力
                    nonedata = true;
                    linex++;
                }
            }
            if( linex <= LINE_MAX ){                                    //明細行は５０行まで->足りない場合は空行を出力！
                for( ; linex <= LINE_MAX ; linex++ ){
                    String fieldNo = "";
                    if (linex % 5 == 0) {
                        fieldNo = "_5";
                    }
                    ret = svf.VrAttribute( "NAME" + fieldNo, "Meido=100" );
                    ret = svf.VrsOut( "NAME" + fieldNo, " . " );
                    ret = svf.VrEndRecord();
                }
            }
            printSvfOutTotal( svf, total );                        //SVF-FORMへ出力
        } catch( Exception ex ) { log.error("printSvfMain read error! ", ex);    }
        return nonedata;
    }//boolean printSvfMain()の括り


    /** 
     *   ＨＲ成績生徒別明細を出力 => VrEndRecord()
     *     printSvfOutDetal(Vrm32alp, svf-field-name, data1(data1がnullでないならdata1を右寄せで出力), 
     *                                               data2(data1がnullならdata2を中央割付で出力))
     */
    private boolean printSvfOutMeisai( Vrw32alp svf, ResultSet rs, String param[], int total[][], int linex ){

        boolean nonedata = false;
        try {
            String fieldNo = "";
            if (linex % 5 == 0) {
                fieldNo = "_5";
            }

            int ret = 0;
            ret = svf.VrsOut( "ATTENDNO" + fieldNo,  rs.getString("HR_NAMEABBV") + "-" + dmf1.format(rs.getInt("ATTENDNO")) );  //クラス−出席番号
            ret = svf.VrsOut( "NAME" + fieldNo,      rs.getString("NAME"));                                                     //名前

            printSvfOutDetal( svf, "POINT1_1" + fieldNo,   (rs.getString("SEM1_INTR_VALUE_DI") != null) ? rs.getString("SEM1_INTR_VALUE_DI") : rs.getString("SEM1_INTR_SCORE"),      null );
            printSvfOutDetal( svf, "POINT1_2" + fieldNo,   (rs.getString("SEM1_TERM_VALUE_DI") != null) ? rs.getString("SEM1_TERM_VALUE_DI") : rs.getString("SEM1_TERM_SCORE"),      null );
            printSvfOutDetal( svf, "AVERAGE1" + fieldNo,   rs.getString("SEM1_VALUE"),      null );
            printSvfOutDetal( svf, "PRE_AVERAGE1" + fieldNo,   rs.getString("SEM1_VALUE_KARI"),      null );

            printSvfOutDetal( svf, "POINT2_1" + fieldNo,   (rs.getString("SEM2_INTR_VALUE_DI") != null) ? rs.getString("SEM2_INTR_VALUE_DI") : rs.getString("SEM2_INTR_SCORE"),      null );
            printSvfOutDetal( svf, "POINT2_2" + fieldNo,   (rs.getString("SEM2_TERM_VALUE_DI") != null) ? rs.getString("SEM2_TERM_VALUE_DI") : rs.getString("SEM2_TERM_SCORE"),      null );
            printSvfOutDetal( svf, "AVERAGE2" + fieldNo,   rs.getString("SEM2_VALUE"),      null );
            printSvfOutDetal( svf, "PRE_AVERAGE2" + fieldNo,   rs.getString("SEM2_VALUE_KARI"),      null );

            printSvfOutDetal( svf, "POINT3_1" + fieldNo,   (rs.getString("SEM3_TERM_VALUE_DI") != null) ? rs.getString("SEM3_TERM_VALUE_DI") : rs.getString("SEM3_TERM_SCORE"),      null );

            printSvfOutDetal( svf, "POINT4" + fieldNo,     rs.getString("GRAD_SCORE_HEIJOU"),      null );//平常点
            printSvfOutDetal( svf, "TOTAL3" + fieldNo,     rs.getString("GRAD_SCORE"),      null );//総合計
            printSvfOutDetal( svf, "PRE_AVERAGE4" + fieldNo,   rs.getString("PROV_FLG"),      null );//仮評定フラグ
            printSvfOutDetal( svf, "AVERAGE4" + fieldNo,   rs.getString("GRAD_VALUE"),      null );//学年評定
            printSvfOutDetal( svf, "COMP_CREDIT" + fieldNo,  rs.getString("COMP_CREDIT"),     null );//履修単位
            printSvfOutDetal( svf, "GET_CREDIT" + fieldNo,   rs.getString("GET_CREDIT"),      null );//修得単位

            if( rs.getString("ABSENT") != null  &&  0 < Integer.parseInt( rs.getString("ABSENT") ) )
                printSvfOutDetal( svf, "TYPE_A" + fieldNo, null, rs.getString("ABSENT") );                         //欠時数

            if( rs.getString("LATE_EARLY") != null  &&  0 < Integer.parseInt( rs.getString("LATE_EARLY") ) )
                printSvfOutDetal( svf, "TYPE_B" + fieldNo, null, rs.getString("LATE_EARLY") );                       //遅刻数

            if( rs.getString("ABSENT2") != null  &&  !(rs.getString("ABSENT2")).equals("0")  &&  !(rs.getString("ABSENT2")).equals("0.0") )
                printSvfOutDetal( svf, "TYPE_C" + fieldNo, null, rs.getString("ABSENT2") );                        //欠課数

            //平均・最高点・最低点の累積および保存処理
            int i = 0;
            accumMeisai( total, i++, rs.getString("SEM1_INTR_SCORE") );
            accumMeisai( total, i++, rs.getString("SEM1_TERM_SCORE") );
            accumMeisai( total, i++, rs.getString("SEM1_VALUE") );
            accumMeisai( total, i++, rs.getString("SEM1_VALUE_KARI") );
            accumMeisai( total, i++, rs.getString("SEM2_INTR_SCORE") );
            accumMeisai( total, i++, rs.getString("SEM2_TERM_SCORE") );
            accumMeisai( total, i++, rs.getString("SEM2_VALUE") );
            accumMeisai( total, i++, rs.getString("SEM2_VALUE_KARI") );
            accumMeisai( total, i++, rs.getString("SEM3_TERM_SCORE") );
            accumMeisai( total, i++, rs.getString("GRAD_SCORE_HEIJOU") );
            accumMeisai( total, i++, rs.getString("GRAD_SCORE") );
            accumMeisai( total, i++, rs.getString("GRAD_VALUE") );

            ret = svf.VrEndRecord();
            if( ret == 0 )nonedata = true;
        } catch( SQLException ex ){
            log.error("printSvfOutMeisai error!", ex );
        }

        return nonedata;

    }//printSvfOutMeisai()の括り

    /** 
     *   平均・最高点・最高点の累積および保存処理
     */
    private void accumMeisai( int total[][], int i, String str )
    {
        try {
            if( str != null ){
                total[0][i] += Integer.parseInt( str );
                total[1][i] += 1;
                if( total[1][i] == 1  ||  Integer.parseInt( str ) < total[2][i] ) total[2][i] = Integer.parseInt( str );
                if( total[3][i] < Integer.parseInt( str ) ) total[3][i] = Integer.parseInt( str );
            }
        } catch( Exception ex ){
            log.error("error! ", ex );
        }

    }


    /** 
     *   ＨＲ成績合計・平均・最高点・最低点を出力 => VrEndRecord()
     *     printSvfOutDetal(Vrm32alp, svf-field-name, data1(data1がnullでないならdata1を右寄せで出力), 
     *                                               data2(data1がnullならdata2を中央割付で出力))
     */
    private void printSvfOutTotal( Vrw32alp svf, int total[][] )
    {
        try {
            dmf1.applyPattern("#.#");
            String fname[] = { "TOTAL1_1", 
                               "TOTAL1_2",
                               "TOTAL_AVERAGE1",
                               "TOTAL_PRE_AVERAGE1",
                               "TOTAL2_1", 
                               "TOTAL2_2",
                               "TOTAL_AVERAGE2",
                               "TOTAL_PRE_AVERAGE2",
                               "TOTAL3_1", 
                               "TOTAL4", 
                               "TOTAL_TOTAL3", "TOTAL_AVERAGE4" };

            printSvfOutTotalDetail( svf, total, fname, 0, "合計" );
            printSvfOutTotalDetailFloat( svf, total, fname, "平均点" );
            printSvfOutTotalDetail( svf, total, fname, 3, "最高点" );
            printSvfOutTotalDetail( svf, total, fname, 2, "最低点" );
        } catch( Exception ex ){
            log.error("error! ", ex );
        }
    }


    /** 
     *   合計・最高点・最低点を出力
     *     printSvfOutDetal(Vrm32alp, svf-field-name, data1(data1がnullでないならdata1を右寄せで出力), 
     *                                               data2(data1がnullならdata2を中央割付で出力))
     */
    private void printSvfOutTotalDetail( Vrw32alp svf, int total[][], String fname[], int ti, String title )
    {
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        ret = svf.VrsOut( "ITEM" , title );
        for( int i = 0 ; i < fname.length ; i++ ){
            if( 0 < total[1][i] )
                printSvfOutDetal( svf, fname[i], String.valueOf( total[ti][i] ), null);
        }
        ret = svf.VrEndRecord();
    }


    /** 
     *   平均点を出力
     *     printSvfOutDetal(Vrm32alp, svf-field-name, data1(data1がnullでないならdata1を右寄せで出力), 
     *                                               data2(data1がnullならdata2を中央割付で出力))
     */
    private void printSvfOutTotalDetailFloat( Vrw32alp svf, int total[][], String fname[], String title )
    {
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        ret = svf.VrsOut( "ITEM" , title );
        for( int i = 0 ; i < fname.length ; i++ ){
            if( 0 < total[1][i] )
                printSvfOutDetal( svf, fname[i], String.valueOf( dmf1.format( (float)Math.round( (float)total[0][i] / (float)total[1][i] * 10 ) / 10 )), null);
        }
        ret = svf.VrEndRecord();
    }


    /** 
     *   ＳＶＦＲｅｃｏｒｄ出力
     *     Strnig svffieldname => 出力SVFフィールド
     *     String data1 => data2がnullなら成績データが入っている
     *     String data2 => 成績データまたは出欠のデータが入っている
     *                     成績データと出欠のデータが入っている場合は、繋いで出力
     */
    private void printSvfOutDetal(Vrw32alp svf, String svffieldname, String data1, String data2) {
        try {
            int ret = 0;
            if (false && 0 != ret) { ret = 0; }
            if( data1 != null )
                ret = svf.VrAttribute(svffieldname , "Hensyu=1");        //右詰め
            else
                ret = svf.VrAttribute(svffieldname , "Hensyu=3");        //中央割付

            if( data1 != null  &&  data2 != null )
                ret = svf.VrsOut(svffieldname , data2 + ("   " + data1).substring( data1.length(), data1.length() + 3 ) );
            else if( data2 != null )
                ret = svf.VrsOut(svffieldname , data2);
            else if( data1 != null )
                ret = svf.VrsOut(svffieldname , data1);
        } catch( Exception ex ){
            log.error("printSvfOutDetal error!", ex );
        }
    }//printSvfOutDetal()の括り


    /** 
     *   ＳＶＦＲｅｃｏｒｄ出力  formatして小数点を出力
     *     Strnig svffieldname => 出力SVFフィールド
     *     String data1 => nullでないなら成績データが入っている
     *     String data2 => nullでないなら成績データが入っている
     */
    private void printSvfOutfloat(Vrw32alp svf, String svffieldname, String data1, String data2) {
        if( data1 == null  &&  data2 == null ) return;
        try {
            int ret = 0;
            if (false && 0 != ret) { ret = 0; }
            if( data1 != null  &&  data2 == null )
                ret = svf.VrAttribute(svffieldname , "Hensyu=1");        //右詰め
            else
                ret = svf.VrAttribute(svffieldname , "Hensyu=3");        //中央割付

            if( data2 != null )
                ret = svf.VrsOut( svffieldname , dmf1.format(Float.parseFloat(data2)) );
            else if( data1 != null )
                ret = svf.VrsOut(svffieldname , dmf1.format(Float.parseFloat(data1)) );
        } catch( Exception ex ){
            log.error("printSvfOutfloat error!", ex );
        }
    }//printSvfOutfloat()の括り


    /** 
     *      
     *   SQLStatement作成 成績データ
     *     int pdiv : 0=>生徒別  1=>合計 
     *      
     */
    String prestatementRecord(String param[],int pdiv) {
        StringBuffer stb = new StringBuffer();
        try {
            //講座の表
            stb.append("WITH CHAIR_A AS(");
            stb.append(    "SELECT  SCHREGNO ");
            stb.append(    "FROM    CHAIR_STD_DAT S1, ");
            stb.append(            "CHAIR_DAT S2 ");
            stb.append(    "WHERE   S1.YEAR = '" + param[0] + "' AND ");
//            stb.append(            "S1.SEMESTER <= '" + param[1] + "' AND ");
            stb.append(            "S1.CHAIRCD = '" + param[2] + "' AND ");
            stb.append(            "'" + param[5] + "' BETWEEN S1.APPDATE AND S1.APPENDDATE AND ");
            //教育課程対応
            if ("1".equals(param[17])) {
                stb.append(            "S2.CLASSCD || '-' || S2.SCHOOL_KIND || '-' || S2.CURRICULUM_CD || '-' || S2.SUBCLASSCD = '" + param[3] + "' AND ");
            } else {
                stb.append(            "S2.SUBCLASSCD = '" + param[3] + "' AND ");
            }
            stb.append(            "S2.YEAR = S1.YEAR AND ");
            stb.append(            "S2.SEMESTER = S1.SEMESTER AND ");
            stb.append(            "S2.CHAIRCD = S1.CHAIRCD ");
            stb.append(            "GROUP BY SCHREGNO ");
            stb.append(    ") ");

            //在籍の表
            stb.append(",SCHNO AS(");
            stb.append(    "SELECT  T2.SCHREGNO, T3.NAME, T4.HR_NAMEABBV, T2.GRADE, T2.HR_CLASS, T2.ATTENDNO, T2.YEAR, T2.COURSECD, T2.MAJORCD, T2.COURSECODE ");
            stb.append(    "FROM    SCHREG_REGD_DAT T2, ");
            stb.append(            "SCHREG_BASE_MST T3, ");
            stb.append(            "SCHREG_REGD_HDAT T4 ");
            stb.append(    "WHERE   T2.YEAR = '" + param[0] + "' AND ");
            stb.append(            "T2.SEMESTER = '" + param[1] + "' AND ");
            stb.append(            "T2.SCHREGNO = T3.SCHREGNO AND ");
            stb.append(            "T4.YEAR = T2.YEAR AND T4.SEMESTER = T2.SEMESTER AND ");
            stb.append(            "T4.GRADE = T2.GRADE AND T4.HR_CLASS = T2.HR_CLASS AND ");
            stb.append(            "EXISTS( SELECT'X' FROM CHAIR_A T5 WHERE T5.SCHREGNO = T2.SCHREGNO ) ");
            stb.append(       ") ");

            //成績の表
            stb.append(" ,RECORD_SCORE AS ( ");
            stb.append("     SELECT  SCHREGNO ");
            //素点
            stb.append("             ,min(CASE WHEN SEMESTER||TESTKINDCD||TESTITEMCD||SCORE_DIV = '1010101' THEN SCORE END) as SEM1_INTR_SCORE ");
            stb.append("             ,min(CASE WHEN SEMESTER||TESTKINDCD||TESTITEMCD||SCORE_DIV = '1020101' THEN SCORE END) as SEM1_TERM_SCORE ");
            stb.append("             ,min(CASE WHEN SEMESTER||TESTKINDCD||TESTITEMCD||SCORE_DIV = '2010101' THEN SCORE END) as SEM2_INTR_SCORE ");
            stb.append("             ,min(CASE WHEN SEMESTER||TESTKINDCD||TESTITEMCD||SCORE_DIV = '2020101' THEN SCORE END) as SEM2_TERM_SCORE ");
            stb.append("             ,min(CASE WHEN SEMESTER||TESTKINDCD||TESTITEMCD||SCORE_DIV = '3020101' THEN SCORE END) as SEM3_TERM_SCORE ");
            //仮評定
            stb.append("             ,min(CASE WHEN SEMESTER||TESTKINDCD||TESTITEMCD||SCORE_DIV = '1990000' THEN VALUE END) as SEM1_VALUE ");
            stb.append("             ,min(CASE WHEN SEMESTER||TESTKINDCD||TESTITEMCD||SCORE_DIV = '2990000' THEN VALUE END) as SEM2_VALUE ");
            //平常点
            stb.append("             ,min(CASE WHEN SEMESTER||TESTKINDCD||TESTITEMCD||SCORE_DIV = '9990201' THEN SCORE END) as GRAD_SCORE_HEIJOU ");
            //総合点
            stb.append("             ,min(CASE WHEN SEMESTER||TESTKINDCD||TESTITEMCD||SCORE_DIV = '9990100' THEN VALUE END) as GRAD_SCORE ");
            //学年評定
            stb.append("             ,min(CASE WHEN SEMESTER||TESTKINDCD||TESTITEMCD||SCORE_DIV = '9990000' THEN VALUE END) as GRAD_VALUE ");
            //単位
            stb.append("             ,min(CASE WHEN SEMESTER||TESTKINDCD||TESTITEMCD||SCORE_DIV = '9990000' THEN GET_CREDIT END) as GET_CREDIT ");
            stb.append("             ,min(CASE WHEN SEMESTER||TESTKINDCD||TESTITEMCD||SCORE_DIV = '9990000' THEN ADD_CREDIT END) as ADD_CREDIT ");
            stb.append("             ,min(CASE WHEN SEMESTER||TESTKINDCD||TESTITEMCD||SCORE_DIV = '9990000' THEN COMP_TAKESEMES END) as COMP_TAKESEMES ");
            stb.append("             ,min(CASE WHEN SEMESTER||TESTKINDCD||TESTITEMCD||SCORE_DIV = '9990000' THEN COMP_CREDIT END) as COMP_CREDIT ");
            //出欠
            stb.append("             ,min(CASE WHEN SEMESTER||TESTKINDCD||TESTITEMCD||SCORE_DIV = '1010101' THEN VALUE_DI END) as SEM1_INTR_VALUE_DI ");
            stb.append("             ,min(CASE WHEN SEMESTER||TESTKINDCD||TESTITEMCD||SCORE_DIV = '1020101' THEN VALUE_DI END) as SEM1_TERM_VALUE_DI ");
            stb.append("             ,min(CASE WHEN SEMESTER||TESTKINDCD||TESTITEMCD||SCORE_DIV = '2010101' THEN VALUE_DI END) as SEM2_INTR_VALUE_DI ");
            stb.append("             ,min(CASE WHEN SEMESTER||TESTKINDCD||TESTITEMCD||SCORE_DIV = '2020101' THEN VALUE_DI END) as SEM2_TERM_VALUE_DI ");
            stb.append("             ,min(CASE WHEN SEMESTER||TESTKINDCD||TESTITEMCD||SCORE_DIV = '3020101' THEN VALUE_DI END) as SEM3_TERM_VALUE_DI ");
            stb.append("     FROM   RECORD_SCORE_DAT ");
            stb.append("     WHERE  YEAR = '" + param[0] + "' ");
            //教育課程対応
            if ("1".equals(param[17])) {
                stb.append("       AND  CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '" + param[3] + "' ");
            } else {
                stb.append("       AND  SUBCLASSCD = '" + param[3] + "' ");
            }
            stb.append("     GROUP BY SCHREGNO ");
            stb.append("     ) ");
            stb.append(",RECORD AS(");
            stb.append(    "SELECT  T1.SCHREGNO, ");
            stb.append(            "SEM1_INTR_SCORE, ");
            stb.append(            "SEM1_TERM_SCORE, ");
            stb.append(            "SEM1_VALUE, ");
            stb.append(            "SEM2_INTR_SCORE, ");
            stb.append(            "SEM2_TERM_SCORE, ");
            stb.append(            "SEM2_VALUE, ");
            stb.append(            "SEM3_TERM_SCORE, ");
            stb.append(            "SEM1_INTR_VALUE_DI, SEM1_TERM_VALUE_DI, ");
            stb.append(            "SEM2_INTR_VALUE_DI, SEM2_TERM_VALUE_DI, ");
            stb.append(            "SEM3_TERM_VALUE_DI, ");
            stb.append(            "COMP_CREDIT, GET_CREDIT, ");
            stb.append(            "GRAD_SCORE_HEIJOU, GRAD_SCORE, GRAD_VALUE ");
            stb.append(    "FROM    RECORD_SCORE T1 ");
            stb.append(    ") ");

            //仮評定フラグの表
            stb.append(" ,RECORD_PROV_FLG AS ( ");
            stb.append("     SELECT  SCHREGNO, PROV_FLG ");
            stb.append("     FROM   RECORD_PROV_FLG_DAT ");
            stb.append("     WHERE  YEAR = '" + param[0] + "' ");
            //教育課程対応
            if ("1".equals(param[17])) {
                stb.append("       AND  CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '" + param[3] + "' ");
            } else {
                stb.append("       AND  SUBCLASSCD = '" + param[3] + "' ");
            }
            stb.append("     ) ");

            //学校マスタの各フラグを参照
            String strLesson = "VALUE(LESSON,0) - VALUE(OFFDAYS,0) - VALUE(ABROAD,0) - VALUE(SUSPEND,0) - VALUE(MOURNING,0)";
            if ("true".equals(param[19]))    strLesson += " - VALUE(VIRUS,0)";
            if ("true".equals(param[20]))    strLesson += " - VALUE(KOUDOME,0)";
            if ("1".equals(_knjSchoolMst._subOffDays))  strLesson += " + VALUE(OFFDAYS,0)";
            if ("1".equals(_knjSchoolMst._subSuspend))  strLesson += " + VALUE(SUSPEND,0)";
            if ("1".equals(_knjSchoolMst._subMourning)) strLesson += " + VALUE(MOURNING,0)";
            if ("1".equals(_knjSchoolMst._subVirus))    strLesson += " + VALUE(VIRUS,0)";
            if ("1".equals(_knjSchoolMst._subKoudome))  strLesson += " + VALUE(KOUDOME,0)";
            String absentStr = "VALUE(SICK,0) + VALUE(NOTICE,0) + VALUE(NONOTICE,0) + VALUE(NURSEOFF,0)";
            if ("1".equals(_knjSchoolMst._subOffDays))  absentStr += " + VALUE(OFFDAYS,0)";
            if ("1".equals(_knjSchoolMst._subAbsent))   absentStr += " + VALUE(ABSENT,0)";
            if ("1".equals(_knjSchoolMst._subSuspend))  absentStr += " + VALUE(SUSPEND,0)";
            if ("1".equals(_knjSchoolMst._subMourning)) absentStr += " + VALUE(MOURNING,0)";
            if ("1".equals(_knjSchoolMst._subVirus))    absentStr += " + VALUE(VIRUS,0)";
            if ("1".equals(_knjSchoolMst._subKoudome))  absentStr += " + VALUE(KOUDOME,0)";
            //出欠の表 明細
            stb.append(",ATTEND_A AS(");
            stb.append(    "SELECT  SCHREGNO, ");
            stb.append(            "SEMESTER, ");
            stb.append(            "SUM(" + strLesson + ") AS LESSON, ");
            stb.append(            "SUM(" + absentStr + ") AS ABSENT1, ");
            stb.append(            "SUM(VALUE(LATE,0) + VALUE(EARLY,0)) AS LATE_EARLY, ");
            stb.append(            "SUM(LATE) AS LATE, ");
            stb.append(            "SUM(EARLY) AS EARLY ");
            stb.append(    "FROM    ATTEND_SUBCLASS_DAT T1 ");
            stb.append(    "WHERE   YEAR = '" + param[0] + "' AND ");
            //教育課程対応
            if ("1".equals(param[17])) {
                stb.append(            "T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '" + param[3] + "' AND ");
            } else {
                stb.append(            "T1.SUBCLASSCD = '" + param[3] + "' AND ");
            }
            stb.append(            "EXISTS( SELECT 'X' FROM chair_std_dat T2 ");
            stb.append(                     "WHERE T1.YEAR = T2.YEAR AND ");
            stb.append(                           "T1.SCHREGNO = T2.SCHREGNO AND T2.CHAIRCD = '" + param[2] + "' ) ");
            stb.append(    "GROUP BY SCHREGNO ");
            stb.append(            ",SEMESTER ");
            stb.append(       ") ");

            //出欠集計データの表
            stb.append(",ATTEND_B AS(");
            if ("1".equals(_knjSchoolMst._absentCov) || "3".equals(_knjSchoolMst._absentCov)) {
                //学期でペナルティ欠課を算出する場合
                stb.append(    "SELECT  T1.SCHREGNO, ");
                stb.append(            "SUM(VALUE(LESSON,0)) AS LESSON, ");
                stb.append(            "VALUE(SUM(ABSENT),0) AS ABSENT, ");
                stb.append(            "VALUE(SUM(ABSENT2),0) AS ABSENT2, ");
                stb.append(            "VALUE(SUM(LATE_EARLY),0) AS LATE_EARLY ");
                stb.append(           ",VALUE(SUM(LATE_EARLY2),0) AS LATE_EARLY2 ");
                stb.append(    "FROM   ( ");
                stb.append(            "SELECT  T1.SCHREGNO, ");
                stb.append(                    "T1.SEMESTER, ");
                stb.append(                    "SUM(VALUE(LESSON,0)) AS LESSON, ");
                stb.append(                    "VALUE(SUM(ABSENT1),0) AS ABSENT, ");
                if( "3".equals(_knjSchoolMst._absentCov) ){
                    //学期でペナルティ欠課を算出（小数点あり）
                    stb.append(                "VALUE(SUM(ABSENT1),0) + DECIMAL(FLOAT(VALUE(SUM(LATE_EARLY),0)) / " + _knjSchoolMst._absentCovLate + ",4,1) AS ABSENT2, ");
                } else{
                    //学期でペナルティ欠課を算出
                    stb.append(                "VALUE(SUM(ABSENT1),0) + VALUE(SUM(LATE_EARLY),0) / " + _knjSchoolMst._absentCovLate + " AS ABSENT2, ");
                }
                stb.append(                    "MOD(VALUE(SUM(LATE_EARLY),0) , " + _knjSchoolMst._absentCovLate + ") AS LATE_EARLY ");
                stb.append(                   ",VALUE(SUM(LATE_EARLY),0) AS LATE_EARLY2 ");
                stb.append(            "FROM    ATTEND_A T1 ");
                stb.append(            "GROUP BY T1.SCHREGNO, T1.SEMESTER ");
                stb.append(           ")T1 ");
                stb.append(    "GROUP BY T1.SCHREGNO ");
            } else if ("5".equals(_knjSchoolMst._absentCov)) {
                //通年でペナルティ欠課を算出・・・例．余りを一捨二入
                stb.append(    "SELECT  T1.SCHREGNO, ");
                stb.append(            "SUM(VALUE(LESSON,0)) AS LESSON, ");
                stb.append(            "VALUE(SUM(ABSENT1),0) AS ABSENT, ");
                stb.append(            "VALUE(SUM(ABSENT1),0) + VALUE(SUM(LATE_EARLY),0) / " + _knjSchoolMst._absentCovLate);
                stb.append(                   "+ (CASE WHEN MOD(VALUE(SUM(LATE_EARLY),0) , " + _knjSchoolMst._absentCovLate + ") >= " + _knjSchoolMst._amariKuriage + " THEN 1 ELSE 0 END) AS ABSENT2, ");
                stb.append(            "CASE WHEN MOD(VALUE(SUM(LATE_EARLY),0) , " + _knjSchoolMst._absentCovLate + ") >= " + _knjSchoolMst._amariKuriage + " THEN 0 ");
                stb.append(                 "ELSE MOD(VALUE(SUM(LATE_EARLY),0) , " + _knjSchoolMst._absentCovLate + ") END AS LATE_EARLY ");
                stb.append(           ",VALUE(SUM(LATE_EARLY),0) AS LATE_EARLY2 ");
                stb.append(    "FROM    ATTEND_A T1 ");
                stb.append(    "GROUP BY T1.SCHREGNO ");
            } else{
                //ペナルティ欠課なしまたは通年でペナルティ欠課を算出する場合
                stb.append(    "SELECT  T1.SCHREGNO, ");
                stb.append(            "SUM(VALUE(LESSON,0)) AS LESSON, ");
                stb.append(            "VALUE(SUM(ABSENT1),0) AS ABSENT, ");
                if( "4".equals(_knjSchoolMst._absentCov) ){
                    //通年でペナルティ欠課を算出（小数点あり）
                    stb.append(        "VALUE(SUM(ABSENT1),0) + DECIMAL(FLOAT(VALUE(SUM(LATE_EARLY),0)) / " + _knjSchoolMst._absentCovLate + ",4,1) AS ABSENT2, ");
                    stb.append(        "MOD(VALUE(SUM(LATE_EARLY),0) , " + _knjSchoolMst._absentCovLate + ") AS LATE_EARLY ");
                } else if( "2".equals(_knjSchoolMst._absentCov) ){
                    //通年でペナルティ欠課を算出
                    stb.append(        "VALUE(SUM(ABSENT1),0) + VALUE(SUM(LATE_EARLY),0) / " + _knjSchoolMst._absentCovLate + " AS ABSENT2, ");
                    stb.append(        "MOD(VALUE(SUM(LATE_EARLY),0) , " + _knjSchoolMst._absentCovLate + ") AS LATE_EARLY ");
                } else{
                    //ペナルティ欠課なしの場合
                    stb.append(        "SUM(VALUE(ABSENT1,0)) AS ABSENT2, ");
                    stb.append(        "VALUE(SUM(LATE_EARLY),0) AS LATE_EARLY ");
                }
                stb.append(           ",VALUE(SUM(LATE_EARLY),0) AS LATE_EARLY2 ");
                stb.append(    "FROM    ATTEND_A T1 ");
                stb.append(    "GROUP BY T1.SCHREGNO ");
            }
            stb.append(       ") ");

            //メイン表
            stb.append("SELECT  T2.HR_NAMEABBV, ");
            stb.append(        "T2.ATTENDNO, ");
            stb.append(        "T2.NAME, ");
            stb.append(        "SEM1_INTR_SCORE, ");
            stb.append(        "SEM1_TERM_SCORE, ");
            stb.append(        "SEM1_VALUE, ");
            stb.append(        "SEM2_INTR_SCORE, ");
            stb.append(        "SEM2_TERM_SCORE, ");
            stb.append(        "SEM2_VALUE, ");
            stb.append(        "SEM3_TERM_SCORE, ");
            stb.append(        "SEM1_INTR_VALUE_DI, SEM1_TERM_VALUE_DI, ");
            stb.append(        "SEM2_INTR_VALUE_DI, SEM2_TERM_VALUE_DI, ");
            stb.append(        "SEM3_TERM_VALUE_DI, ");
            stb.append(        "GRAD_SCORE_HEIJOU, ");
            stb.append(        "GRAD_SCORE, ");
            stb.append(        "CASE WHEN P0.PROV_FLG = '1' THEN 'レ' END AS PROV_FLG, ");
            stb.append(        "T1.GRAD_VALUE, T1.COMP_CREDIT, T1.GET_CREDIT, ");
            if (param[3].substring(0,2).equals("90")) {
                stb.append(    "(SELECT ASSESSMARK FROM ASSESS_MST WHERE ASSESSCD = '3' AND ASSESSLEVEL = T1.GRAD_VALUE) AS ASSESSMARK, ");
            }
            stb.append(        "VALUE(LESSON,0) AS LESSON, ");
            stb.append(        "ABSENT, ABSENT2 ");
            // 累積情報の遅刻・早退欄のフラグ(1なら遅刻・早退回数、1以外なら欠課換算後の余り)
            if ("1".equals(param[13])) {
                stb.append(   ",VALUE(LATE_EARLY2,0) AS LATE_EARLY ");
            } else {
                stb.append(   ",VALUE(LATE_EARLY,0)  AS LATE_EARLY ");
            }
            // プロパティ(科目別評定マスタ)
            if ("1".equals(param[22])) {
                stb.append(   ",VALUE(S1.ASSESSLEVEL, A1.ASSESSLEVEL) AS SEM1_VALUE_KARI ");
                stb.append(   ",VALUE(S2.ASSESSLEVEL, A2.ASSESSLEVEL) AS SEM2_VALUE_KARI ");
            } else {
                stb.append(   ",A1.ASSESSLEVEL AS SEM1_VALUE_KARI ");
                stb.append(   ",A2.ASSESSLEVEL AS SEM2_VALUE_KARI ");
            }
            
            stb.append("FROM    SCHNO T2 ");
            stb.append(        "LEFT JOIN RECORD T1 ON T1.SCHREGNO = T2.SCHREGNO ");
            stb.append(        "LEFT JOIN RECORD_PROV_FLG P0 ON P0.SCHREGNO = T2.SCHREGNO ");
            stb.append(        "LEFT JOIN ATTEND_B T3 ON T3.SCHREGNO = T2.SCHREGNO ");

            //評定マスタ
            stb.append("     LEFT JOIN ASSESS_MST A1 ON A1.ASSESSCD = '3' ");
            stb.append("                            AND T1.SEM1_VALUE BETWEEN A1.ASSESSLOW AND A1.ASSESSHIGH ");
            stb.append("     LEFT JOIN ASSESS_MST A2 ON A2.ASSESSCD = '3' ");
            stb.append("                            AND T1.SEM2_VALUE BETWEEN A2.ASSESSLOW AND A2.ASSESSHIGH ");
            if ("1".equals(param[22])) {
                stb.append("     LEFT JOIN ASSESS_SUBCLASS_MST S1 ON S1.YEAR = T2.YEAR ");
                stb.append("         AND S1.GRADE = T2.GRADE ");
                stb.append("         AND S1.COURSECD = T2.COURSECD ");
                stb.append("         AND S1.MAJORCD = T2.MAJORCD ");
                stb.append("         AND S1.COURSECODE = T2.COURSECODE ");
                //教育課程対応
                if ("1".equals(param[17])) {
                    stb.append("     AND S1.CLASSCD || '-' || S1.SCHOOL_KIND || '-' || S1.CURRICULUM_CD || '-' || S1.SUBCLASSCD = '" + param[3] + "' ");
                } else {
                    stb.append("     AND S1.SUBCLASSCD    = '" + param[3] + "' ");
                }
                stb.append("         AND T1.SEM1_VALUE BETWEEN S1.ASSESSLOW AND S1.ASSESSHIGH ");
                stb.append("     LEFT JOIN ASSESS_SUBCLASS_MST S2 ON S2.YEAR = T2.YEAR ");
                stb.append("         AND S2.GRADE = T2.GRADE ");
                stb.append("         AND S2.COURSECD = T2.COURSECD ");
                stb.append("         AND S2.MAJORCD = T2.MAJORCD ");
                stb.append("         AND S2.COURSECODE = T2.COURSECODE ");
                //教育課程対応
                if ("1".equals(param[17])) {
                    stb.append("     AND S2.CLASSCD || '-' || S2.SCHOOL_KIND || '-' || S2.CURRICULUM_CD || '-' || S2.SUBCLASSCD = '" + param[3] + "' ");
                } else {
                    stb.append("     AND S2.SUBCLASSCD    = '" + param[3] + "' ");
                }
                stb.append("         AND T1.SEM2_VALUE BETWEEN S2.ASSESSLOW AND S2.ASSESSHIGH ");
            }

            //ソート
            stb.append(" ORDER BY ");
            stb.append("         T2.GRADE, T2.HR_CLASS, T2.ATTENDNO ");

        } catch( Exception ex ){
            log.error("prestatementRecord error!", ex );
        }
        return stb.toString();
    }//prestatementRecord()の括り


    /** 
     *  SQLStatement作成 科目名 
     **/
    String prestatementHeadSubclass(String param[]) {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("SELECT SUBCLASSNAME ");
            stb.append("FROM   SUBCLASS_MST ");
            //教育課程対応
            if ("1".equals(param[17])) {
                stb.append("WHERE  CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD ='"+param[3]+"' ");
            } else {
                stb.append("WHERE  SUBCLASSCD ='"+param[3]+"' ");
            }
        } catch( Exception ex ){
            log.error("prestatementHead error!", ex );
        }
        return stb.toString();
    }//prestatementHead()の括り


    /** 
     *  SQLStatement作成 講座名 
     **/
    String prestatementHeadChair(String param[]) {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("SELECT DISTINCT CHAIRNAME ");
            stb.append("FROM   CHAIR_DAT ");
            stb.append("WHERE  YEAR = '" + param[0] + "' AND ");
            stb.append(          "CHAIRCD = '" + param[2] + "' AND ");
            //教育課程対応
            if ("1".equals(param[17])) {
                stb.append(          "CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '" + param[3]+"' ");
            } else {
                stb.append(          "SUBCLASSCD = '" + param[3]+"' ");
            }
        } catch( Exception ex ){
            log.error("prestatementHead error!", ex );
        }
        return stb.toString();
    }//prestatementHead()の括り


    /** 
     *  SQLStatement作成 担当者名
     **/
    String prestatementHeadStaff(String param[]) {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("SELECT STAFFNAME ");
            stb.append("FROM   STAFF_MST ");
            stb.append("WHERE  STAFFCD = '" + param[4] + "' ");
        } catch( Exception ex ){
            log.error("prestatementHeadStaff error!", ex );
        }
        return stb.toString();
    }//prestatementHead()の括り


    /** 
     *  SQLStatement作成 担当者名
     **/
    String prestatementHeadStaff2(String param[]) {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("SELECT STAFFNAME,t1.STAFFCD ");
            stb.append("FROM   CHAIR_STF_DAT t1 inner join STAFF_MST t2 on t1.STAFFCD = t2.STAFFCD  ");
            stb.append("WHERE  t1.YEAR = '" + param[0] + "' AND ");
            stb.append("       t1.SEMESTER = '" + param[1] + "' AND ");
            stb.append("       t1.CHAIRCD = '" + param[2] + "' ");
            stb.append("ORDER BY t1.STAFFCD ");
        } catch( Exception ex ){
            log.error("prestatementHeadStaff error!", ex );
        }
        return stb.toString();
    }//prestatementHead()の括り


    /** 
     *  単位の取得
     */
    String prestatementHeadCredit(String param[])
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append(" WITH CHAIR_A AS(SELECT ");
            stb.append("     SCHREGNO ");
            stb.append(" FROM ");
            stb.append("     CHAIR_STD_DAT S1, ");
            stb.append("     CHAIR_DAT S2 ");
            stb.append(" WHERE ");
            stb.append("     S1.YEAR = '" + param[0] + "' AND ");
//            stb.append("     S1.SEMESTER <= '" + param[1] + "' AND ");
            stb.append("     S1.CHAIRCD = '" + param[2] + "' AND ");
            stb.append(     "'" + param[5] + "' BETWEEN S1.APPDATE AND S1.APPENDDATE AND ");
            //教育課程対応
            if ("1".equals(param[17])) {
                stb.append("     S2.CLASSCD || '-' || S2.SCHOOL_KIND || '-' || S2.CURRICULUM_CD || '-' || S2.SUBCLASSCD = '" + param[3]+"' AND ");
            } else {
                stb.append("     S2.SUBCLASSCD = '" + param[3]+"' AND ");
            }
            stb.append("     S2.YEAR = S1.YEAR AND ");
            stb.append("     S2.SEMESTER = S1.SEMESTER AND ");
            stb.append("     S2.CHAIRCD = S1.CHAIRCD ");
            stb.append(" GROUP BY ");
            stb.append("     SCHREGNO ");
            stb.append(" ), ");
            stb.append(" btable (GRADE,COURSECD,YEAR,MAJORCD,COURSECODE) as ( ");
            stb.append(" SELECT   ");
            stb.append("     T2.GRADE,T2.COURSECD,T2.YEAR,T2.MAJORCD,T2.COURSECODE ");
            stb.append(" FROM     ");
            stb.append("     SCHREG_REGD_DAT T2,  ");
            stb.append("     SCHREG_BASE_MST T3,  ");
            stb.append("     SCHREG_REGD_HDAT T4  ");
            stb.append(" WHERE    ");
            stb.append("     T2.YEAR = '" + param[0] + "' AND  ");
            stb.append("     T2.SEMESTER = (SELECT ");
            stb.append("                        MAX(SEMESTER) ");
            stb.append("                    FROM ");
            stb.append("                        SCHREG_REGD_DAT W2 ");
            stb.append("                    WHERE ");
            stb.append("                        W2.YEAR = '" + param[0] + "' AND ");
            stb.append("                        W2.SEMESTER <= '" + param[1] + "' AND ");
            stb.append("                        W2.SCHREGNO = T2.SCHREGNO ) AND  ");
            stb.append("     T2.SCHREGNO = T3.SCHREGNO AND T4.YEAR = '" + param[0] + "' AND  ");
            stb.append("     T4.SEMESTER = T2.SEMESTER AND T4.GRADE = T2.GRADE AND  ");
            stb.append("     T4.HR_CLASS = T2.HR_CLASS AND ");
            stb.append("     EXISTS(SELECT ");
            stb.append("                'X' ");
            stb.append("            FROM ");
            stb.append("                 CHAIR_A T5 ");
            stb.append("             WHERE ");
            stb.append("                 T5.SCHREGNO = T2.SCHREGNO ) ) ");
            stb.append(" SELECT  ");
            stb.append("     T1.YEAR,MAX(CREDITS) as MAXCREDITS,MIN(CREDITS) as MINCREDITS ");
            stb.append(" FROM  ");
            stb.append("     btable T5,CREDIT_MST T1 ");
            stb.append(" WHERE  ");
            stb.append("     T1.YEAR = '" + param[0] + "' AND T1.GRADE = T5.GRADE AND  ");
            stb.append("     T1.COURSECD = T5.COURSECD AND T1.MAJORCD = T5.MAJORCD AND ");
            stb.append("     T1.GRADE = T5.GRADE AND T1.COURSECODE = T5.COURSECODE AND ");
            //教育課程対応
            if ("1".equals(param[17])) {
                stb.append("     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '" + param[3]+"' ");
            } else {
                stb.append("     T1.CLASSCD = '"+param[3].substring(0,2)+"' AND T1.SUBCLASSCD = '" + param[3]+"' ");
            }
            stb.append(" GROUP BY ");
            stb.append("     T1.YEAR ");
        } catch( Exception ex ){
            log.error("prestatementHeadCredit error!", ex );
        }
        return stb.toString();
    }


    /** 
     *  パラメータセット
     *      param[6]:attend_semes_datの最終集計日の翌日をセット
     *      param[7]:attend_semes_datの最終集計学期＋月をセット
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

//    public class Vrw32alp extends nao_package.svf.Vrw32alp {
//
//        /**
//         * {@inheritDoc}
//         */
//        public int VrsOut(String field, String data) {
//            int ret = super.VrsOut(field, data);
//            if (ret != 0) log.fatal("*******A_VrsOut:"+ret);
//            return super.VrsOut(field, data);
//        }
//
//        /**
//         * {@inheritDoc}
//         */
//        public int VrsOutn(String field, int gyo, String data) {
//            int ret = super.VrsOutn(field, gyo, data);
//            if (ret != 0) log.fatal("*******B_VrsOutn:"+ret);
//            return super.VrsOutn(field, gyo, data);
//        }
//        
//    }
}//クラスの括り
