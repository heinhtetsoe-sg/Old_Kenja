/**
 *
 *    学校教育システム 賢者 [成績管理] 成績点票（智辯）
 *
 *    2009/05/29 o-naka 新規作成 KNJD124Bを土台に作成
 */

package servletpack.KNJD;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import nao_package.svf.Vrw32alp;
import nao_package.db.DB2UDB;
import servletpack.KNJZ.detail.KNJ_Get_Info;
import servletpack.KNJZ.detail.KNJ_EditDate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class KNJD124C {

    private static final Log log = LogFactory.getLog(KNJD124C.class);
    private DecimalFormat dmf1 = new DecimalFormat("00");
    private static final int MAX_LINE = 45;

    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws ServletException, IOException
    {
        Vrw32alp svf = new Vrw32alp();
        DB2UDB db2 = null;
        String param[] = new String[19];

        String printname = null;
        try {
            log.fatal("$Revision: 56595 $"); // CVSキーワードの取り扱いに注意
            param[0]  = request.getParameter("YEAR");                        //年度
            param[1]  = request.getParameter("SEMESTER");                    //1-3:学期
            param[2]  = request.getParameter("CHAIRCD");                     //講座コード
            param[3]  = request.getParameter("SUBCLASSCD");                  //科目コード
            param[4]  = request.getParameter("STAFF");                       //ログイン者コード
            param[5]  = request.getParameter("TEST_DATE");                   //試験日
            param[18] = request.getParameter("useCurriculumcd"); //プロパティ(教育課程コード)(1:教育課程対応)
        } catch( Exception ex ) {
            log.error("parameter error!", ex);
        }

        //    print設定-->printnameが存在する-->プリンターへ直接出力の場合
        PrintWriter outstrm = new PrintWriter (response.getOutputStream());
        if (printname != null)  response.setContentType("text/html");
        else                    response.setContentType("application/pdf");

        //    svf設定-->printnameが存在する-->プリンターへ直接出力の場合
        int ret = svf.VrInit();
        if (printname != null) {
            ret = svf.VrSetPrinter("", printname);
            if (ret < 0) log.info("printname ret = " + ret);
        } else {
            ret = svf.VrSetSpoolFileStream(response.getOutputStream());
        }

        //    ＤＢ接続
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
        } catch( Exception ex ) {
            log.error("DB2 open error!", ex);
        }

        //    ＳＶＦ作成処理
        boolean nonedata = false;
        setHead(db2,svf,param);
        if (printSvfMain(db2,svf,param)) nonedata = true;

        //    該当データ無し-->printnameが存在する-->プリンターへ直接出力の場合
        if (printname != null) {
            if (!nonedata)  outstrm.println("<H1>対象データはありません。</h1>");
            else            outstrm.println("<H1>印刷しました。</h1>");
        } else if (!nonedata) {
            ret = svf.VrSetForm("MES001.frm", 0);
            ret = svf.VrsOut("note" , "note");
            ret = svf.VrEndPage();
        }

        //     終了処理
        ret = svf.VrQuit();
        if (ret == 0) log.info("===> VrQuit():" + ret);
        db2.commit();
        db2.close();
        outstrm.close();
    }

    /** SVF-FORMセット＆見出し項目 **/
    private void setHead(DB2UDB db2,Vrw32alp svf,String param[]) {
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        ret = svf.VrSetForm("KNJD124C.frm", 4);
        KNJ_Get_Info getinfo = new KNJ_Get_Info();
        KNJ_Get_Info.ReturnVal returnval = null;
        ret = svf.VrsOut("NENDO",nao_package.KenjaProperties.gengou(Integer.parseInt(param[0]))+"年度");

        //    作成日(現在処理日)
        try {
            returnval = getinfo.Control(db2);
            ret = svf.VrsOut("DATE",KNJ_EditDate.h_format_JP(returnval.val3));
        } catch( Exception ex ){
            log.error("ReturnVal setHead() get TODAY error!", ex );
        }

        //    科目名
        ResultSet rs = null;
        try {
            db2.query( prestatementHeadSubclass(param) );
            rs = db2.getResultSet();
            if (rs.next()) {
                ret = svf.VrsOut("SUBCLASS",rs.getString("SUBCLASSNAME"));
            }
            rs.close();
        } catch( Exception ex ){
            log.error("setHead_ hrclass_staff error!", ex );
        }

        //    講座名 
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

        //  担当者名---ログイン
        try {
            db2.query(sqlStaffNameLogin(param));
            rs = db2.getResultSet();
            if (rs.next()) {
                ret = svf.VrsOut("STAFFNAME1",rs.getString("STAFFNAME"));
            }
            rs.close();
        } catch( Exception ex ){
            log.error("StaffNameLogin error!", ex );
        }

        //  担当者名2---講座
        try {
            int staffcnt = 0 ;
            String Staffname = "" ;
            db2.query(sqlStaffNameChair(param));
            rs = db2.getResultSet();
            while( rs.next() ){
                log.debug(rs.getString("STAFFCD")+rs.getString("STAFFNAME"));
                if (staffcnt == 2) break;
                if (!(param[4]).equals(rs.getString("STAFFCD"))) {
                    if (staffcnt == 1) {
                        Staffname = Staffname + "," + rs.getString("STAFFNAME");
                    } else {
                        Staffname = rs.getString("STAFFNAME");
                    }
                    staffcnt++;
                }
            }
            ret = svf.VrsOut("STAFFNAME2", Staffname);
            rs.close();
        } catch( Exception ex ){
            log.error("StaffNameChair error!", ex );
        }

        getinfo = null;
        returnval = null;
    }

    /** SVF-FORM メイン出力処理 **/
    private boolean printSvfMain(DB2UDB db2,Vrw32alp svf,String param[]) {
        for (int i = 0; i < param.length; i++) log.debug("param[" + i + "] = " + param[i] );
        //定義
        boolean nonedata = false;
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        ResultSet rs = null;
        int total[][] = {{0,0,0,0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0,0,0,0},{0,0,0,0,0,0,0,0,0,0,0,0,0,0}};   //合計

        try {
            int linex = 0; //１ページ当り出力行数
            db2.query(prestatementRecord(param,0)); //生徒別成績データ
            rs = db2.getResultSet();
            while (rs.next()) {
                if (printSvfOutMeisai(svf, rs, param, total)) {
                    nonedata = true;
                    linex++;
                }
            }
            //明細行はMAX_LINE行まで->足りない場合は空行を出力！
            if (linex < MAX_LINE) {
                for ( ; linex < MAX_LINE ; linex++ ) {
                    ret = svf.VrAttribute( "NAME1", "Meido=100" );
                    ret = svf.VrsOut( "NAME1", " . " );
                    ret = svf.VrEndRecord();
                }
            }
            printSvfOutTotal(svf, total);
        } catch( Exception ex ) { log.error("printSvfMain read error! ", ex);    }

        return nonedata;
    }

    /** 
     *   ＨＲ成績生徒別明細を出力
     *     printSvfOutDetal(Vrm32alp, svf-field-name, data1(data1がnullでないならdata1を右寄せで出力), 
     *                                               data2(data1がnullならdata2を中央割付で出力))
     */
    private boolean printSvfOutMeisai(Vrw32alp svf, ResultSet rs, String param[], int total[][]) {
        boolean nonedata = false;
        try {
            int ret = 0;
            ret = svf.VrsOut( "ATTENDNO",  rs.getString("HR_NAMEABBV") + "-" + dmf1.format(rs.getInt("ATTENDNO")) );  //クラス−出席番号
            ret = svf.VrsOut( "NAME1",      rs.getString("NAME"));                                                     //名前
            ret = svf.VrsOut( "NAME2",      rs.getString("NAME_ENG"));                                                     //名前

            printSvfOutDetal( svf, "POINT1",   rs.getString("SEM1_SCORE"),  rs.getString("SEM1_SCORE_DI") );//１学期評定

            printSvfOutDetal( svf, "POINT2",   rs.getString("SEM2_SCORE"),  rs.getString("SEM2_SCORE_DI") );//２学期評価

            printSvfOutDetal( svf, "POINT3",   rs.getString("SEM3_SCORE"),  rs.getString("SEM3_SCORE_DI") );//３学期評価

            printSvfOutDetal( svf, "POINT4",   rs.getString("GRAD_SCORE"),  rs.getString("GRAD_SCORE_DI") );//学年評定(５段階)

            //平均・最高点・最高点の累積および保存処理
            int i = 0;
            accumMeisai( total, i++, rs.getString("SEM1_SCORE") );
            accumMeisai( total, i++, rs.getString("SEM2_SCORE") );
            accumMeisai( total, i++, rs.getString("SEM3_SCORE") );
            accumMeisai( total, i++, rs.getString("GRAD_SCORE") );

            ret = svf.VrEndRecord();
            if( ret == 0 )nonedata = true;
        } catch( SQLException ex ){
            log.error("printSvfOutMeisai error!", ex );
        }

        return nonedata;
    }

    /** 
     *   平均・最高点・最高点の累積および保存処理
     */
    private void accumMeisai(int total[][], int i, String str) {
        try {
            if (str != null) {
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
     *   ＨＲ成績合計・平均・最高点・最低点を出力
     *     printSvfOutDetal(Vrm32alp, svf-field-name, data1(data1がnullでないならdata1を右寄せで出力), 
     *                                               data2(data1がnullならdata2を中央割付で出力))
     */
    private void printSvfOutTotal(Vrw32alp svf, int total[][]) {
        try {
            dmf1.applyPattern("#.#");
            String fname[] = { "TOTAL1",
                               "TOTAL2",
                               "TOTAL3",
                               "TOTAL4" };
            printSvfOutTotalDetail( svf, total, fname, 0, "合計" );
            printSvfOutTotalDetail( svf, total, fname, 1, "人数" );
            printSvfOutTotalDetailFloat( svf, total, fname, "平均" );
            //printSvfOutTotalDetail( svf, total, fname, 3, "最高点" );
            //printSvfOutTotalDetail( svf, total, fname, 2, "最低点" );
        } catch( Exception ex ){
            log.error("error! ", ex );
        }
    }

    /** 
     *   合計・最高点・最低点を出力
     *     printSvfOutDetal(Vrm32alp, svf-field-name, data1(data1がnullでないならdata1を右寄せで出力), 
     *                                               data2(data1がnullならdata2を中央割付で出力))
     */
    private void printSvfOutTotalDetail(Vrw32alp svf, int total[][], String fname[], int ti, String title) {
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        ret = svf.VrsOut( "ITEM" , title );
        for (int i = 0 ; i < fname.length ; i++) {
            if (0 < total[1][i]) printSvfOutDetal(svf, fname[i], String.valueOf( total[ti][i] ), null);
        }
        ret = svf.VrEndRecord();
    }

    /** 
     *   平均点を出力
     *     printSvfOutDetal(Vrm32alp, svf-field-name, data1(data1がnullでないならdata1を右寄せで出力), 
     *                                               data2(data1がnullならdata2を中央割付で出力))
     */
    private void printSvfOutTotalDetailFloat(Vrw32alp svf, int total[][], String fname[], String title) {
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        ret = svf.VrsOut( "ITEM" , title );
        for (int i = 0 ; i < fname.length ; i++) {
            if (0 < total[1][i]) printSvfOutDetal(svf, fname[i], String.valueOf(dmf1.format((float)Math.round((float)total[0][i] / (float)total[1][i] * 10) / 10)), null);
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
            if (data1 != null || data2 != null)
                ret = svf.VrAttribute(svffieldname , "Hensyu=1");        //右詰め
            else
                ret = svf.VrAttribute(svffieldname , "Hensyu=3");        //中央割付

            if (data2 != null && "*".equals(data2))
                ret = svf.VrsOut(svffieldname , data2); //未入力「*」
            else if (data2 != null)
                ret = svf.VrsOut(svffieldname , "[" + data2 + "]"); //見込点「(6)」
            else if (data1 != null)
                ret = svf.VrsOut(svffieldname , data1);
        } catch( Exception ex ){
            log.error("printSvfOutDetal error!", ex );
        }
    }

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
            stb.append(    "SELECT  SCHREGNO,S1.SEMESTER ");
            stb.append(    "FROM    CHAIR_STD_DAT S1, ");
            stb.append(            "CHAIR_DAT S2 ");
            stb.append(    "WHERE   S1.YEAR = '" + param[0] + "' AND ");
            stb.append(            "S1.CHAIRCD = '" + param[2] + "' AND ");
            stb.append(            "'" + param[5] + "' BETWEEN S1.APPDATE AND S1.APPENDDATE AND ");
            //教育課程対応
            if ("1".equals(param[18])) {
                stb.append(            "S2.CLASSCD || '-' || S2.SCHOOL_KIND || '-' || S2.CURRICULUM_CD || '-' || S2.SUBCLASSCD = '" + param[3] + "' AND ");
            } else {
                stb.append(            "S2.SUBCLASSCD = '" + param[3] + "' AND ");
            }
            stb.append(            "S2.YEAR = S1.YEAR AND ");
            stb.append(            "S2.SEMESTER = S1.SEMESTER AND ");
            stb.append(            "S2.CHAIRCD = S1.CHAIRCD ");
            stb.append(            "GROUP BY SCHREGNO,S1.SEMESTER ");
            stb.append(    ") ");
            
            //在籍の表
            stb.append(",SCHNO AS(");
            stb.append(    "SELECT  T2.SCHREGNO, T3.NAME, T3.NAME_ENG, T4.HR_NAMEABBV, T2.GRADE, T2.HR_CLASS, T2.ATTENDNO ");
            stb.append(    "FROM    SCHREG_REGD_DAT T2, ");
            stb.append(            "SCHREG_BASE_MST T3, ");
            stb.append(            "SCHREG_REGD_HDAT T4 ");
            stb.append(    "WHERE   T2.YEAR = '" + param[0] + "' AND ");
            stb.append(            "T2.SEMESTER = (SELECT  MAX(SEMESTER) ");
            stb.append(                           "FROM    SCHREG_REGD_DAT W2 ");
            stb.append(                           "WHERE   W2.YEAR = '" + param[0] + "' AND ");
            stb.append(                                   "W2.SEMESTER <= '" + param[1] + "' AND ");
            stb.append(                                   "W2.SCHREGNO = T2.SCHREGNO) AND ");
            stb.append(            "T2.SCHREGNO = T3.SCHREGNO AND T4.YEAR = '" + param[0] + "' AND ");
            stb.append(            "T4.SEMESTER = T2.SEMESTER AND T4.GRADE = T2.GRADE AND ");
            stb.append(            "T4.HR_CLASS = T2.HR_CLASS AND ");
            stb.append(            "EXISTS( SELECT'X' FROM CHAIR_A T5 WHERE T5.SCHREGNO = T2.SCHREGNO ) ");
            stb.append(       ") ");

            //成績の表
            stb.append(" ,RECORD_SCORE AS ( ");
            stb.append("     SELECT  SCHREGNO ");
            //成績
            stb.append("             ,min(CASE WHEN SEMESTER||TESTKINDCD||TESTITEMCD||SCORE_DIV = '1990000' THEN SCORE END) as SEM1_SCORE ");
            stb.append("             ,min(CASE WHEN SEMESTER||TESTKINDCD||TESTITEMCD||SCORE_DIV = '2990000' THEN SCORE END) as SEM2_SCORE ");
            stb.append("             ,min(CASE WHEN SEMESTER||TESTKINDCD||TESTITEMCD||SCORE_DIV = '3990000' THEN SCORE END) as SEM3_SCORE ");
            stb.append("             ,min(CASE WHEN SEMESTER||TESTKINDCD||TESTITEMCD||SCORE_DIV = '9990000' THEN SCORE END) as GRAD_SCORE ");
            //未入力・見込点
            stb.append("             ,min(CASE WHEN SEMESTER||TESTKINDCD||TESTITEMCD||SCORE_DIV = '1990000' THEN VALUE_DI END) as SEM1_SCORE_DI ");
            stb.append("             ,min(CASE WHEN SEMESTER||TESTKINDCD||TESTITEMCD||SCORE_DIV = '2990000' THEN VALUE_DI END) as SEM2_SCORE_DI ");
            stb.append("             ,min(CASE WHEN SEMESTER||TESTKINDCD||TESTITEMCD||SCORE_DIV = '3990000' THEN VALUE_DI END) as SEM3_SCORE_DI ");
            stb.append("             ,min(CASE WHEN SEMESTER||TESTKINDCD||TESTITEMCD||SCORE_DIV = '9990000' THEN VALUE_DI END) as GRAD_SCORE_DI ");
            stb.append("     FROM   RECORD_SCORE_DAT ");
            stb.append("     WHERE  YEAR = '" + param[0] + "' ");
            //教育課程対応
            if ("1".equals(param[18])) {
                stb.append("       AND  CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '" + param[3] + "' ");
            } else {
                stb.append("       AND  SUBCLASSCD = '" + param[3] + "' ");
            }
            stb.append("     GROUP BY SCHREGNO ");
            stb.append("     ) ");
            

            //メイン表
            stb.append("SELECT  T2.HR_NAMEABBV, ");
            stb.append(        "T2.ATTENDNO, ");
            stb.append(        "T2.NAME, ");
            stb.append(        "T2.NAME_ENG, ");
            stb.append(        "T1.SEM1_SCORE, ");
            stb.append(        "T1.SEM2_SCORE, ");
            stb.append(        "T1.SEM3_SCORE, ");
            stb.append(        "T1.SEM1_SCORE_DI, ");
            stb.append(        "T1.SEM2_SCORE_DI, ");
            stb.append(        "T1.SEM3_SCORE_DI, ");
            stb.append(        "T1.GRAD_SCORE_DI, ");
            stb.append(        "T1.GRAD_SCORE ");
            stb.append("FROM    SCHNO T2 ");
            stb.append(        "LEFT JOIN RECORD_SCORE T1 ON T1.SCHREGNO = T2.SCHREGNO ");
            stb.append("ORDER BY T2.GRADE, T2.HR_CLASS, T2.ATTENDNO");
        } catch( Exception ex ){
            log.error("prestatementRecord error!", ex );
        }
        return stb.toString();
    }

    /** 
     *  SQLStatement作成 科目名 
     **/
    String prestatementHeadSubclass(String param[]) {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("SELECT SUBCLASSNAME ");
            stb.append("FROM   SUBCLASS_MST ");
            //教育課程対応
            if ("1".equals(param[18])) {
                stb.append("WHERE  CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD ='"+param[3]+"' ");
            } else {
                stb.append("WHERE  SUBCLASSCD ='"+param[3]+"' ");
            }
        } catch( Exception ex ){
            log.error("prestatementHeadSubclass error!", ex );
        }
        return stb.toString();
    }

    /** 
     *  SQLStatement作成 講座名 
     **/
    String prestatementHeadChair(String param[]) {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("SELECT DISTINCT CHAIRNAME ");
            stb.append("FROM   CHAIR_DAT ");
            stb.append("WHERE  YEAR = '" + param[0] + "' AND ");
            stb.append(       "CHAIRCD = '" + param[2] + "' AND ");
            //教育課程対応
            if ("1".equals(param[18])) {
                stb.append(          "CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '" + param[3]+"' ");
            } else {
                stb.append(          "SUBCLASSCD = '" + param[3]+"' ");
            }
        } catch( Exception ex ){
            log.error("prestatementHeadChair error!", ex );
        }
        return stb.toString();
    }

    /** 
     *  担当者名---ログイン
     */
    String sqlStaffNameLogin(String param[]) {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("SELECT STAFFNAME ");
            stb.append("FROM   STAFF_MST ");
            stb.append("WHERE  STAFFCD = '" + param[4] + "' ");
        } catch( Exception ex ){
            log.error("sqlStaffNameLogin error!", ex );
        }
        return stb.toString();
    }

    /** 
     *  担当者名2---講座
     */
    String sqlStaffNameChair(String param[]) {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("SELECT STAFFNAME,t1.STAFFCD ");
            stb.append("FROM   CHAIR_STF_DAT t1 left join STAFF_MST t2 on t1.STAFFCD = t2.STAFFCD  ");
            stb.append("WHERE  t1.YEAR = '" + param[0] + "' AND ");
            stb.append("       t1.SEMESTER = '" + param[1] + "' AND ");
            stb.append("       t1.CHAIRCD = '" + param[2] + "' ");
            stb.append("ORDER BY t1.STAFFCD ");
        } catch( Exception ex ){
            log.error("sqlStaffNameChair error!", ex );
        }
        return stb.toString();
    }

}
