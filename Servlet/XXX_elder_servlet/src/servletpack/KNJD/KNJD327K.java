package servletpack.KNJD;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJC.KNJDivideAttendDate;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_Get_Info;

/**
 *
 *  学校教育システム 賢者 [成績管理]
 *
 *                  ＜ＫＮＪＤ３２７＞ 皆勤者一覧
 *
 *  2005/07/02 nakamoto 作成日
 *
 *  2005/10/20 nakamoto 編入のデータ仕様変更および在籍異動条件に転入学を追加---NO025
 *  2005/11/30 nakamoto NO026:残作業No.241,No.164の対応---成績一覧表の皆勤者表示を参考に修正！
 **/

public class KNJD327K {


    private static final Log log = LogFactory.getLog(KNJD327K.class);

    private KNJ_Get_Info getinfo = new KNJ_Get_Info();      //各情報取得用のクラス     NO003
    private KNJ_Get_Info.ReturnVal returnval;               //各情報を返すためのクラス NO003
    private Calendar cal1 = Calendar.getInstance();         //                         NO003
    private Calendar cal2 = Calendar.getInstance();         //                         NO003
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");  //             NO003
    private int jnisu;                                      //授業日数                 NO003
    // 学校マスタ参照
    KNJSchoolMst _knjSchoolMst;
    private String _useCurriculumcd;
    private String _useVirus;
    private String _useKoudome;

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(HttpServletRequest request, HttpServletResponse response)
                     throws ServletException, IOException
    {
        log.fatal("$Revision: 56595 $"); // CVSキーワードの取り扱いに注意

        Vrw32alp svf = new Vrw32alp();          //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                      //Databaseクラスを継承したクラス
        String param[] = new String[7];

    //  パラメータの取得
        String printname = null;                                            //PDFで出力用！！
        String classcd[] = request.getParameterValues("GRADE");             //学年
        try {
            param[0] = request.getParameter("YEAR");                        //年度
            param[1] = request.getParameter("GAKKI");                       //学期
            param[2] = request.getParameter("SEMESTER_EDATE");              //学期終了日(基準日)
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _useVirus = request.getParameter("useVirus");
            _useKoudome = request.getParameter("useKoudome");
        } catch( Exception ex ) {
            log.warn("parameter error!");
        }

    //  print設定-->printnameが存在する-->プリンターへ直接出力の場合(存在しない-->PDF出力)
        PrintWriter outstrm = new PrintWriter (response.getOutputStream());
        if( printname!=null )   response.setContentType("text/html");
        else                    response.setContentType("application/pdf");

    //  svf設定-->printnameが存在する-->プリンターへ直接出力の場合(存在しない-->PDF出力)
        int ret = svf.VrInit();                             //クラスの初期化
        if( printname!=null ){
            ret = svf.VrSetPrinter("", printname);          //プリンタ名の設定
            if( ret < 0 ) log.warn("printname ret = " + ret);
        } else
            ret = svf.VrSetSpoolFileStream(response.getOutputStream());         //PDFファイル名の設定

    //  ＤＢ接続
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
        } catch( Exception ex ) {
            log.error("DB2 open error!");
            return;
        }


    //  ＳＶＦ作成処理
        PreparedStatement ps6 = null;
        boolean nonedata = false;                               //該当データなしフラグ
        setKnjSchoolMst(db2, param);
        Set_Head(db2,svf,param);                                //見出し取得のメソッド
        getDivideAttendDate( db2, param );                      //出欠用日付等取得 NO003
for(int ia=0 ; ia<param.length ; ia++) log.debug("param["+ia+"]="+param[ia]);
        //SQL作成
        try {
            ps6 = db2.prepareStatement(psAttendPerfect(param));     //出欠状況preparestatement
        } catch( Exception ex ) {
            log.warn("DB2 open error!");
        }
        //SVF出力
        for( int ia=0 ; ia<classcd.length ; ia++ ){
log.debug("start! "+classcd[ia]);
            if( printAttendPerfect(db2,svf,param,classcd[ia],ps6) ){
                nonedata = true;
                ret = svf.VrEndPage();
            }
        }

log.debug("nonedata = "+nonedata);

    //  該当データ無し-->printnameが存在する-->プリンターへ直接出力の場合(存在しない-->PDF出力)
        if( printname!=null ){
            outstrm.println("<HTML>");
            outstrm.println("<HEAD>");
            outstrm.println("<META http-equiv=\"Content-Type\" content=\"text/html; charset=euc-jp\">");
            outstrm.println("</HEAD>");
            outstrm.println("<BODY>");
            if( !nonedata ) outstrm.println("<H1>対象データはありません。</h1>");
            else            outstrm.println("<H1>印刷しました。</h1>");
            outstrm.println("</BODY>");
            outstrm.println("</HTML>");
        } else if( !nonedata ){
            ret = svf.VrSetForm("MES001.frm", 0);
            ret = svf.VrsOut("note" , "note");
            ret = svf.VrEndPage();
        }

    //  終了処理
        ret = svf.VrQuit();
        if( ret == 0 )log.debug("===> VrQuit():" + ret);
        Pre_Stat_f(ps6);        //preparestatementを閉じる
        db2.commit();
        db2.close();                //DBを閉じる
        outstrm.close();            //ストリームを閉じる

    }//doGetの括り


    /**
     *  学校マスタ取得
     */
    void setKnjSchoolMst(DB2UDB db2, String param[])
    {
        try {
            _knjSchoolMst = new KNJSchoolMst(db2, param[0]);
        } catch (SQLException e) {
            log.warn("学校マスタ取得でエラー", e);
        }
    }


    /** SVF-FORM **/
    private void Set_Head(DB2UDB db2,Vrw32alp svf,String param[]){

        if( getinfo == null ) getinfo = new KNJ_Get_Info();

        svf.VrSetForm("KNJD327.frm", 1);    //KIN01

    //  作成日(現在処理日)の取得
        try {
            returnval = getinfo.Control(db2);
            param[3] = KNJ_EditDate.h_format_JP(returnval.val3);
        } catch( Exception e ){
            log.warn("ctrl_date get error!");
        }

    }//Set_Head()の括り



    /**
     *   皆勤者一覧を出力するメソッド
     */
    private boolean printAttendPerfect(
        DB2UDB db2,
        Vrw32alp svf,
        String param[],
        String classcd,
        PreparedStatement ps6
    ) {
        boolean nonedata = false;
        try {
            int pp = 0;
            ps6.setString(++pp,classcd);    //学年
            ps6.setString(++pp,classcd);    //学年---2005.07.06
            ps6.setString(++pp,classcd);    //学年
            ResultSet rs = ps6.executeQuery();
log.debug("executeQuery read ok!");

            int gyo = 0;            //データカウント
            while( rs.next() ){
                if (gyo == 100) {
                    gyo = 0;                //初期化
                    svf.VrEndPage();  //１ページ印字
                }
                if (gyo < 50) { //左表
                    svf.VrsOutn( "class1"     , gyo + 1       , rs.getString("HR_NAMEABBV") );
                    svf.VrsOutn( "nomber1"    , gyo + 1       , String.valueOf(rs.getInt("ATTENDNO")) );
                    svf.VrsOutn( "name1"      , gyo + 1       , rs.getString("NAME") );
                } else {        //右表
                    svf.VrsOutn( "class2"     , gyo + 1 - 50  , rs.getString("HR_NAMEABBV") );
                    svf.VrsOutn( "nomber2"    , gyo + 1 - 50  , String.valueOf(rs.getInt("ATTENDNO")) );
                    svf.VrsOutn( "name2"      , gyo + 1 - 50  , rs.getString("NAME") );
                }
                svf.VrsOut( "maketime"  ,param[3] ); //作成日

                gyo++;
                nonedata = true;
            }
            rs.close();
            db2.commit();
        } catch( Exception ex ) {
            log.warn("printAttendPerfect read error!", ex);
        }
        return nonedata;

    }//printAttendPerfect()の括り



    /**
      *  出欠状況の明細及び合計
      *
      *  2005/11/30 NO026:残作業No.241,No.164の対応---成績一覧表の皆勤者表示を参考に修正！
      */
    private String psAttendPerfect(String param[])
    {
        StringBuffer stb = new StringBuffer();
        try {
            //対象生徒
            stb.append("WITH SCHNO AS(");
            stb.append(     "SELECT  W1.SCHREGNO, W1.ATTENDNO, W1.GRADE, W1.HR_CLASS ");
            stb.append(            ",CASE WHEN W4.SCHREGNO IS NOT NULL THEN 1 WHEN W5.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END AS LEAVE ");
            stb.append(     "FROM    SCHREG_REGD_DAT W1 ");
            stb.append(     "INNER   JOIN SEMESTER_MST W2 ON W2.SEMESTER = W1.SEMESTER AND W2.YEAR = '" + param[0] + "' ");

            stb.append(     "LEFT    JOIN SCHREG_BASE_MST W4 ON W4.SCHREGNO = W1.SCHREGNO ");
            stb.append(                                    "AND ((W4.GRD_DIV IN('2','3') AND W4.GRD_DATE < CASE WHEN W2.EDATE < '" + param[2] + "' THEN W2.EDATE ELSE '" + param[2] + "' END) ");
            stb.append(                                      "OR (W4.ENT_DIV IN('4','5') AND W4.ENT_DATE > CASE WHEN W2.EDATE < '" + param[2] + "' THEN W2.EDATE ELSE '" + param[2] + "' END)) ");

            stb.append(     "LEFT    JOIN SCHREG_TRANSFER_DAT W5 ON W5.SCHREGNO = W1.SCHREGNO ");
            stb.append(                                        "AND (W5.TRANSFERCD IN ('1','2') AND CASE WHEN W2.EDATE < '" + param[2] + "' THEN W2.EDATE ELSE '" + param[2] + "' END BETWEEN W5.TRANSFER_SDATE AND W5.TRANSFER_EDATE) ");

            stb.append(     "WHERE   W1.YEAR = '" + param[0] + "' AND ");
            stb.append(             "W1.SEMESTER = '" + param[1] + "' AND ");
            stb.append(             "W1.GRADE = ? ");   //---1:指定学年
            stb.append(     "), ");

            //対象講座の表
            stb.append("CHAIR_A AS(");
            stb.append(     "SELECT  K2.CHAIRCD,");
            if ("1".equals(_useCurriculumcd)) {
                stb.append(             "K2.CLASSCD || '-' || K2.SCHOOL_KIND || '-' || K2.CURRICULUM_CD || '-' || ");
            }
            stb.append(             "K2.SUBCLASSCD AS SUBCLASSCD,K2.SEMESTER ");
            stb.append(     "FROM    CHAIR_CLS_DAT K1, CHAIR_DAT K2 ");
            stb.append(     "WHERE   K1.YEAR = '" + param[0] + "' ");
            stb.append(         "AND K2.YEAR = '" + param[0] + "' ");
            stb.append(         "AND K1.SEMESTER <= '" + param[1] + "' ");
            stb.append(         "AND K2.SEMESTER <= '" + param[1] + "' ");
            stb.append(         "AND K1.SEMESTER = K2.SEMESTER ");
            stb.append(         "AND (K1.CHAIRCD = '0000000' OR K1.CHAIRCD = K2.CHAIRCD) ");
            stb.append(         "AND K1.TRGTGRADE = ? ");   //---2:指定学年
            stb.append(         "AND K1.GROUPCD = K2.GROUPCD ");
            stb.append(     "GROUP BY K2.CHAIRCD, ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append(             "K2.CLASSCD || '-' || K2.SCHOOL_KIND || '-' || K2.CURRICULUM_CD || '-' || ");
            }
            stb.append(              "K2.SUBCLASSCD, K2.SEMESTER ");
            stb.append(     "),");

            // テスト項目マスタの集計フラグ
            stb.append(" TEST_COUNTFLG AS ( ");
            stb.append("     SELECT ");
            stb.append("         T1.EXECUTEDATE, ");
            stb.append("         T1.PERIODCD, ");
            stb.append("         T1.CHAIRCD, ");
            stb.append("         '2' AS DATADIV ");
            stb.append("     FROM ");
            stb.append("         SCH_CHR_TEST T1, ");
            stb.append("         TESTITEM_MST_COUNTFLG T2 ");
            stb.append("     WHERE ");
            stb.append("         T2.YEAR       = T1.YEAR ");
            stb.append("         AND T2.TESTKINDCD = T1.TESTKINDCD ");
            stb.append("         AND T2.TESTITEMCD = T1.TESTITEMCD ");
            stb.append("         AND T2.COUNTFLG   = '0' ");
            stb.append(" ), ");

            //時間割の表
            stb.append("SCHEDULE_A AS(");
            stb.append(    "SELECT  T1.CHAIRCD, T1.SEMESTER, T1.EXECUTEDATE, T1.PERIODCD, T1.DATADIV ");
            stb.append(    "FROM    SCH_CHR_DAT T1 ");
            stb.append(    "WHERE   T1.YEAR = '" + param[0] + "' ");
            stb.append(        "AND T1.EXECUTEDATE BETWEEN '" + param[5] + "' AND '" + param[2] + "' ");
            stb.append(        "AND EXISTS(SELECT 'X' FROM CHAIR_A T2 WHERE T2.CHAIRCD = T1.CHAIRCD) ");
            stb.append(     "),");

            stb.append("SCHEDULE_R AS(");
            stb.append(                 "SELECT T2.SCHREGNO, SUBCLASSCD, T1.SEMESTER, EXECUTEDATE, PERIODCD ");
            stb.append(                 "FROM   SCHEDULE_A T1,");
            stb.append(                        "CHAIR_STD_DAT T2, ");
            stb.append(                        "CHAIR_A T3 ");
            stb.append(                 "WHERE  T1.SEMESTER = T3.SEMESTER AND ");
            stb.append(                        "T1.CHAIRCD = T3.CHAIRCD AND ");
            stb.append(                        "T1.EXECUTEDATE BETWEEN T2.APPDATE AND T2.APPENDDATE AND ");
            stb.append(                        "T2.YEAR = '" + param[0] + "' AND ");
            stb.append(                        "T2.SEMESTER = T3.SEMESTER AND ");
            stb.append(                        "T2.CHAIRCD = T3.CHAIRCD AND ");
            stb.append(                        "T1.SEMESTER = T2.SEMESTER AND ");
            stb.append(                        "T1.CHAIRCD = T2.CHAIRCD ");
            stb.append(                    "AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST T4 ");
            stb.append(                                   "WHERE   T4.SCHREGNO = T2.SCHREGNO ");
            stb.append(                                       "AND (( ENT_DIV IN('4','5') AND EXECUTEDATE < ENT_DATE ) ");
            stb.append(                                         "OR ( GRD_DIV IN('2','3') AND EXECUTEDATE > GRD_DATE )) ) ");
            stb.append("    AND NOT EXISTS(SELECT ");
            stb.append("                       'X' ");
            stb.append("                   FROM ");
            stb.append("                       ATTEND_DAT T4 ");
            stb.append("                       LEFT JOIN ATTEND_DI_CD_DAT ATDD ON ATDD.YEAR = '" + param[0] + "' AND ATDD.DI_CD = T4.DI_CD ");
            stb.append("                   WHERE ");
            stb.append("                       T4.SCHREGNO = T2.SCHREGNO ");
            stb.append("                       AND T4.ATTENDDATE = T1.EXECUTEDATE ");
            stb.append("                       AND T4.PERIODCD = T1.PERIODCD ");
            stb.append("                       AND ATDD.REP_DI_CD = '27' "); // 勤怠コードが'27'の入力されている日付は時間割にカウントしない
            stb.append("                  ) ");
            // 勤怠コード'28'は時間割にカウントしない
            stb.append("    AND NOT EXISTS(SELECT ");
            stb.append("                       'X' ");
            stb.append("                   FROM ");
            stb.append("                       ATTEND_DAT T4 ");
            stb.append("                   WHERE ");
            stb.append("                       T4.SCHREGNO = T2.SCHREGNO ");
            stb.append("                       AND T4.ATTENDDATE = T1.EXECUTEDATE ");
            stb.append("                       AND T4.PERIODCD = T1.PERIODCD ");
            stb.append("                       AND T4.DI_CD = '28' ");
            stb.append("                  ) ");
                stb.append(                "AND NOT EXISTS(SELECT  'X' FROM SCH_CHR_COUNTFLG T4 ");
                stb.append(                               "WHERE   T4.EXECUTEDATE = T1.EXECUTEDATE ");
                stb.append(                                   "AND T4.PERIODCD = T1.PERIODCD ");
                stb.append(                                   "AND T4.CHAIRCD = T1.CHAIRCD ");
                stb.append(                                   "AND T1.DATADIV IN ('0', '1') ");
                stb.append(                                   "AND T4.GRADE = ? "); //---3:指定学年
                stb.append(                                   "AND T4.COUNTFLG = '0')  ");
                stb.append(                "AND NOT EXISTS(SELECT  'X' FROM TEST_COUNTFLG TEST ");
                stb.append(                               "WHERE ");
                stb.append(                                       "TEST.EXECUTEDATE  = T1.EXECUTEDATE ");
                stb.append(                                       "AND TEST.PERIODCD = T1.PERIODCD ");
                stb.append(                                       "AND TEST.CHAIRCD  = T1.CHAIRCD ");
                stb.append(                                       "AND TEST.DATADIV  = T1.DATADIV) ");
            stb.append(                 "GROUP BY T2.SCHREGNO, SUBCLASSCD, T1.SEMESTER, EXECUTEDATE, PERIODCD ");
            stb.append(     "),");

            stb.append("SCHEDULE_MAIN AS(");
            stb.append(     "SELECT  T1.* ");
            stb.append(     "FROM    SCHEDULE_R T1 ");
            stb.append(     "WHERE   NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT T4 ");
            stb.append(                        "WHERE   T4.SCHREGNO = T1.SCHREGNO ");
            stb.append(                            "AND T4.TRANSFERCD IN('1','2') AND EXECUTEDATE BETWEEN T4.TRANSFER_SDATE AND T4.TRANSFER_EDATE ) ");
            stb.append(     "),");

            stb.append("SCH_TRANSFER AS(");
            stb.append(" SELECT ");
            stb.append("     T1.SUBCLASSCD, ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.EXECUTEDATE, ");
            stb.append("     T1.PERIODCD, ");
            stb.append("     T1.SEMESTER, ");
            stb.append("     '00' AS DI_CD ");
            stb.append(" FROM ");
            stb.append("     SCHEDULE_R T1, ");
            stb.append("     SCHREG_TRANSFER_DAT T2 ");
            stb.append(" WHERE ");
            stb.append("     T2.TRANSFERCD IN('2') ");
            stb.append("     AND T1.SCHREGNO = T2.SCHREGNO ");
            stb.append("     AND T1.EXECUTEDATE BETWEEN T2.TRANSFER_SDATE AND T2.TRANSFER_EDATE ");
            stb.append(" GROUP BY ");
            stb.append("     T1.SUBCLASSCD, ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.EXECUTEDATE, ");
            stb.append("     T1.PERIODCD, ");
            stb.append("     T1.SEMESTER ");
            stb.append(     "),");

            //欠課数の表
            stb.append("SUBCLASS_ATTEND_A AS(");
            stb.append(     "SELECT SCHREGNO, SUBCLASSCD, ");
            stb.append(            "SUM(LATE_EARLY) AS LATE_EARLY, ");
                stb.append(        "SUM( VALUE(ABSENT1,0) + VALUE(LATE_EARLY,0)/3 ) AS ABSENT1 ");
                    //出欠データより集計
            stb.append(     "FROM ( ");
            stb.append(          "SELECT S1.SCHREGNO,SUBCLASSCD,");
            stb.append(                 "SUM(CASE WHEN (CASE WHEN ATDD.ATSUB_REPL_DI_CD IS NOT NULL THEN ATDD.ATSUB_REPL_DI_CD ELSE ATDD.REP_DI_CD END) IN( ");
            if ("1".equals(_knjSchoolMst._subSuspend)) {
                stb.append(                 " '2', '9', ");
            }
            if ("1".equals(_knjSchoolMst._subVirus)) {
                stb.append(                 " '19', '20', ");
            }
            if ("1".equals(_knjSchoolMst._subKoudome)) {
                stb.append(                 " '25', '26', ");
            }
            if ("1".equals(_knjSchoolMst._subMourning)) {
                stb.append(                 " '3', '10', ");
            }
            if ("1".equals(_knjSchoolMst._subAbsent)) {
                stb.append(                 " '1', '8', ");
            }
            stb.append(                 "'4','5','6','14','11','12','13') THEN 1 ELSE 0 END)AS ABSENT1, ");
            stb.append(                 "SUM(CASE WHEN (CASE WHEN ATDD.ATSUB_REPL_DI_CD IS NOT NULL THEN ATDD.ATSUB_REPL_DI_CD ELSE ATDD.REP_DI_CD END) IN('15','16','23','24') THEN SMALLINT(VALUE(ATDD.MULTIPLY, '1')) ELSE 0 END)AS LATE_EARLY ");
            stb.append(          "FROM SCHEDULE_MAIN S1 ");
            stb.append(          "LEFT JOIN ATTEND_DAT S2 ON S2.YEAR = '" + param[0] + "' AND ");
            stb.append(                                      "S2.ATTENDDATE = S1.EXECUTEDATE AND ");
            stb.append(                                      "S2.PERIODCD = S1.PERIODCD AND ");
            stb.append(                                      "S1.SCHREGNO = S2.SCHREGNO ");
            stb.append(          "INNER JOIN ATTEND_DI_CD_DAT ATDD ON ATDD.YEAR = '" + param[0] + "' AND ATDD.DI_CD = S2.DI_CD ");
            stb.append(                                      "AND (CASE WHEN ATDD.ATSUB_REPL_DI_CD IS NOT NULL THEN ATDD.ATSUB_REPL_DI_CD ELSE ATDD.REP_DI_CD END) IN( ");
            if ("1".equals(_knjSchoolMst._subSuspend)) {
                stb.append(                 " '2', '9', ");
            }
            if ("1".equals(_knjSchoolMst._subVirus)) {
                stb.append(                 " '19', '20', ");
            }
            if ("1".equals(_knjSchoolMst._subKoudome)) {
                stb.append(                 " '25', '26', ");
            }
            if ("1".equals(_knjSchoolMst._subMourning)) {
                stb.append(                 " '3', '10', ");
            }
            if ("1".equals(_knjSchoolMst._subAbsent)) {
                stb.append(                 " '1', '8', ");
            }
            stb.append(                                      "'4','5','6','14','15','16','11','12','13','23','24') ");
            stb.append(          "GROUP BY S1.SCHREGNO, SUBCLASSCD, SEMESTER ");

            if ("1".equals(_knjSchoolMst._subOffDays)) {
                stb.append("UNION ALL ");
                stb.append("SELECT ");
                stb.append("    SCHREGNO, ");
                stb.append("    SUBCLASSCD, ");
                stb.append("    COUNT(*) AS ABSENT1, ");
                stb.append("    0 AS LATE_EARLY ");
                stb.append("FROM SCH_TRANSFER ");
                stb.append("GROUP BY ");
                stb.append("    SCHREGNO, ");
                stb.append("    SUBCLASSCD, ");
                stb.append("    SEMESTER ");
            }

                    //月別科目別出欠集計データより欠課を取得
            stb.append(          "UNION ALL ");
            stb.append(          "SELECT  W1.SCHREGNO, ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append(             "W1.CLASSCD || '-' || W1.SCHOOL_KIND || '-' || W1.CURRICULUM_CD || '-' || ");
            }
            stb.append(                  "W1.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append(                  "SUM(VALUE(SICK,0) + VALUE(NOTICE,0) + VALUE(NONOTICE,0) + VALUE(NURSEOFF,0) ");
            if ("1".equals(_knjSchoolMst._subOffDays)) {
                stb.append(              "+ VALUE(OFFDAYS,0) ");
            }
            if ("1".equals(_knjSchoolMst._subAbsent)) {
                stb.append(              "+ VALUE(ABSENT,0) ");
            }
            if ("1".equals(_knjSchoolMst._subSuspend)) {
                stb.append(              "+ VALUE(SUSPEND,0) ");
            }
            if ("1".equals(_knjSchoolMst._subMourning)) {
                stb.append(              "+ VALUE(MOURNING,0) ");
            }
            if ("1".equals(_knjSchoolMst._subVirus)) {
                stb.append(              "+ VALUE(VIRUS,0) ");
            }
            if ("1".equals(_knjSchoolMst._subKoudome)) {
                stb.append(              "+ VALUE(KOUDOME,0) ");
            }
            stb.append(                  ") AS ABSENT1, ");
            stb.append(                  "SUM(VALUE(LATE,0) + VALUE(EARLY,0)) AS LATE_EARLY ");
            stb.append(          "FROM    ATTEND_SUBCLASS_DAT W1, SCHNO W2 ");
            stb.append(          "WHERE   W1.YEAR = '" + param[0] + "' AND ");
            stb.append(                  "W1.SEMESTER <= '" + param[1] + "' AND ");
            stb.append(                  "W1.SEMESTER||W1.MONTH <= '" + param[6] + "' AND ");
            stb.append(                  "W1.SCHREGNO = W2.SCHREGNO ");
            stb.append(          "GROUP BY W1.SCHREGNO, ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append(             "W1.CLASSCD || '-' || W1.SCHOOL_KIND || '-' || W1.CURRICULUM_CD || '-' || ");
            }
            stb.append(                   "W1.SUBCLASSCD, W1.SEMESTER ");
            stb.append(          ")S1 ");
            stb.append(     "GROUP BY S1.SCHREGNO, SUBCLASSCD ");
            stb.append(     "), ");

            stb.append("SUBCLASS_ATTEND_B AS(");
            stb.append(     "SELECT  SCHREGNO, SUM(ABSENT1) AS ABSENT1, SUM(LATE_EARLY) AS LATE_EARLY ");
            stb.append(     "FROM    SUBCLASS_ATTEND_A ");
            stb.append(     "GROUP BY SCHREGNO ");
            stb.append(     "HAVING 0 < SUM(ABSENT1) OR 0 < SUM(LATE_EARLY) ");
            stb.append(     "), ");

            //対象生徒の時間割データ
            stb.append("SCHEDULE_SCHREG_R AS(");
            stb.append(     "SELECT  T2.SCHREGNO,T1.EXECUTEDATE,T1.PERIODCD ");
            stb.append(     "FROM    SCHEDULE_A T1, CHAIR_STD_DAT T2 ");
            stb.append(     "WHERE   T2.YEAR = '" + param[0] + "' ");
            stb.append(         "AND T1.EXECUTEDATE BETWEEN T2.APPDATE AND T2.APPENDDATE ");
            stb.append(         "AND T1.SEMESTER = T2.SEMESTER ");
            stb.append(         "AND T1.CHAIRCD = T2.CHAIRCD ");
            stb.append(         "AND T2.SCHREGNO IN(SELECT SCHREGNO FROM SCHNO) ");
            stb.append(         "AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST T3 ");
            stb.append(                        "WHERE   T3.SCHREGNO = T2.SCHREGNO ");
            stb.append(                            "AND (( ENT_DIV IN('4','5') AND EXECUTEDATE < ENT_DATE ) ");
            stb.append(                              "OR ( GRD_DIV IN('2','3') AND EXECUTEDATE > GRD_DATE )) ) ");
            stb.append("    AND NOT EXISTS(SELECT ");
            stb.append("                       'X' ");
            stb.append("                   FROM ");
            stb.append("                       ATTEND_DAT T4 ");
            stb.append("                       LEFT JOIN ATTEND_DI_CD_DAT ATDD ON ATDD.YEAR = '" + param[0] + "' AND ATDD.DI_CD = T4.DI_CD ");
            stb.append("                   WHERE ");
            stb.append("                       T4.SCHREGNO = T2.SCHREGNO ");
            stb.append("                       AND T4.ATTENDDATE = T1.EXECUTEDATE ");
            stb.append("                       AND ATDD.REP_DI_CD = '27' "); // 勤怠コードが'27'の入力されている日付は時間割にカウントしない
            stb.append("                  ) ");
            stb.append(     "GROUP BY T2.SCHREGNO,T1.EXECUTEDATE,T1.PERIODCD ");
            stb.append(     "), ");

            stb.append("SCHEDULE_SCHREG AS(");
            stb.append(     "SELECT  T1.SCHREGNO,T1.EXECUTEDATE,T1.PERIODCD ");
            stb.append(     "FROM    SCHEDULE_SCHREG_R T1 ");
            stb.append(     "WHERE   NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT T4 ");
            stb.append(                        "WHERE   T4.SCHREGNO = T1.SCHREGNO ");
            stb.append(                            "AND T4.TRANSFERCD IN('1','2') AND EXECUTEDATE BETWEEN T4.TRANSFER_SDATE AND T4.TRANSFER_EDATE ) ");
            stb.append(     "), ");

            //対象生徒の出欠データ
            stb.append("T_ATTEND_DAT AS(");
            stb.append(     "SELECT  T0.SCHREGNO, T0.ATTENDDATE, T0.PERIODCD, ");
            stb.append("             T0.DI_CD AS YOMIKAEMAE, ");
            stb.append("             CASE WHEN ATDD.ATSUB_REPL_DI_CD IS NOT NULL THEN ATDD.ATSUB_REPL_DI_CD ELSE ATDD.REP_DI_CD END AS DI_CD ");
            stb.append(     "FROM    ATTEND_DAT T0 ");
            stb.append("             LEFT JOIN ATTEND_DI_CD_DAT ATDD ON ATDD.YEAR = '" + param[0] + "' AND ATDD.DI_CD = T0.DI_CD, ");
            stb.append(             "SCHEDULE_SCHREG T1 ");
            stb.append(     "WHERE   T0.YEAR = '" + param[0] + "' AND ");
            stb.append(             "T0.ATTENDDATE BETWEEN '" + param[5] + "' AND '" + param[2] + "' AND ");
            stb.append(             "T0.SCHREGNO = T1.SCHREGNO AND ");
            stb.append(             "T0.ATTENDDATE = T1.EXECUTEDATE AND ");
            stb.append(             "T0.PERIODCD = T1.PERIODCD ");
            stb.append(     "), ");

            //対象生徒の出欠データ（忌引・出停した日）
            stb.append("T_ATTEND_DAT_B AS(");
            stb.append(     "SELECT  T0.SCHREGNO, T0.ATTENDDATE ");
            stb.append(     "FROM    T_ATTEND_DAT T0 ");
            stb.append(     "WHERE   DI_CD IN('2','3','9','10') ");
            stb.append(     "GROUP BY T0.SCHREGNO, T0.ATTENDDATE ");
            stb.append(     "), ");

            //対象生徒の日単位の最小校時・最大校時・校時数
            stb.append("T_PERIOD_CNT AS(");
            stb.append(     "SELECT  T1.SCHREGNO, T1.EXECUTEDATE, ");
            stb.append(             "MIN(T1.PERIODCD) AS FIRST_PERIOD, ");
            stb.append(             "MAX(T1.PERIODCD) AS LAST_PERIOD, ");
            stb.append(             "COUNT(DISTINCT T1.PERIODCD) AS PERIOD_CNT ");
            stb.append(     "FROM    SCHEDULE_SCHREG T1 ");
            stb.append(     "GROUP BY T1.SCHREGNO, T1.EXECUTEDATE ");
            stb.append(     "), ");

            //留学日数を算出 休学を含める
            stb.append("TRANSFER_SCHREG AS(");
            stb.append(     "SELECT  T3.SCHREGNO, T3.TRANSFERCD, COUNT(DISTINCT T1.EXECUTEDATE)AS TRANSFER_DATE ");
            stb.append(     "FROM    SCHREG_TRANSFER_DAT T3, SCHEDULE_SCHREG_R T1 ");
            stb.append(     "WHERE   T3.SCHREGNO = T1.SCHREGNO ");
            stb.append(         "AND T3.TRANSFERCD IN('1','2') ");
            stb.append(         "AND T1.EXECUTEDATE BETWEEN T3.TRANSFER_SDATE AND T3.TRANSFER_EDATE ");
            stb.append(     "GROUP BY T3.SCHREGNO, T3.TRANSFERCD ");
            stb.append(     ") ");


            //メイン表
            stb.append("SELECT TBL1.SCHREGNO,HR_NAMEABBV,ATTENDNO,NAME ");
            stb.append("FROM ( ");

            stb.append("   SELECT  TT0.SCHREGNO, TT0.ATTENDNO, TT0.GRADE, TT0.HR_CLASS,  ");
            stb.append(           "TT0.LEAVE, ");
                                    //出席すべき日数
            stb.append(           "( VALUE(TT1.LESSON,0) - VALUE(TT3.SUSPEND,0) - VALUE(TT4.MOURNING,0) ) ");
            if ("1".equals(_knjSchoolMst._semOffDays)) {
                stb.append(                 " + VALUE(TT9.TRANSFER_DATE,0) ");
            }
            if ("true".equals(_useVirus)) {
                stb.append(           " - VALUE(TT3_1.VIRUS,0) - VALUE(TT7.VIRUS,0) ");
            }
            if ("true".equals(_useKoudome)) {
                stb.append(           " - VALUE(TT3_2.KOUDOME,0) - VALUE(TT7.KOUDOME,0) ");
            }
            stb.append(            " + ( VALUE(TT7.LESSON,0) - VALUE(TT7.SUSPEND,0) - VALUE(TT7.MOURNING,0) ) AS MLESSON, ");
                                    //各種欠席日数
            stb.append(           "VALUE(TT3.SUSPEND,0) + VALUE(TT7.SUSPEND,0) AS SUSPEND, ");
            stb.append(           "VALUE(TT3_1.VIRUS,0) + VALUE(TT7.VIRUS,0) AS VIRUS, ");
            stb.append(           "VALUE(TT3_2.KOUDOME,0) + VALUE(TT7.KOUDOME,0) AS KOUDOME, ");
            stb.append(           "VALUE(TT4.MOURNING,0) + VALUE(TT7.MOURNING,0) AS MOURNING, ");
            stb.append(           "VALUE(TT5.SICK,0) + VALUE(TT5.NOTICE,0) + VALUE(TT5.NONOTICE,0) + VALUE(TT7.ABSENT,0) ");
            if ("1".equals(_knjSchoolMst._semOffDays)) {
                stb.append(                 " + VALUE(TT9.TRANSFER_DATE,0) ");
            }
            stb.append(           " AS ABSENT, ");
                                    //遅刻・早退
            stb.append(           "VALUE(TT6.LATE,0) + VALUE(TT10.LATE,0) + VALUE(TT7.LATE,0) AS LATE, ");
            stb.append(           "VALUE(TT6.EARLY,0) + VALUE(TT11.EARLY,0) + VALUE(TT7.EARLY,0) AS EARLY, ");
                                    //留学日数
            stb.append(           "VALUE(TT8.TRANSFER_DATE,0) + VALUE(TT7.ABROAD,0) AS RTRANSFER_DATE, ");
                                    //休学日数
            stb.append(           "VALUE(TT9.TRANSFER_DATE,0) + VALUE(TT7.OFFDAYS,0) AS KTRANSFER_DATE, ");
                                    //欠課および授業の遅刻・早退
            stb.append(           "VALUE(TT13.ABSENT1,0) AS ABSENT1, VALUE(TT13.LATE_EARLY,0) AS LATE_EARLY ");
            stb.append(   "FROM    SCHNO TT0 ");

            //個人別授業日数
            stb.append(   "LEFT OUTER JOIN(");
            stb.append(      "SELECT  SCHREGNO, COUNT(DISTINCT EXECUTEDATE) AS LESSON ");
            stb.append(      "FROM    T_PERIOD_CNT ");
            stb.append(      "GROUP BY SCHREGNO ");
            stb.append(      ") TT1 ON TT0.SCHREGNO = TT1.SCHREGNO ");
            //個人別出停日数
            stb.append(   "LEFT OUTER JOIN(");
            stb.append(      "SELECT SCHREGNO, COUNT(DISTINCT ATTENDDATE) AS SUSPEND ");
            stb.append(      "FROM   T_ATTEND_DAT ");
            stb.append(      "WHERE  DI_CD IN ('2','9') ");
            stb.append(      "GROUP BY SCHREGNO ");
            stb.append(      ") TT3 ON TT0.SCHREGNO = TT3.SCHREGNO ");
            //個人別出停伝染病日数
            stb.append(   "LEFT OUTER JOIN(");
            stb.append(      "SELECT SCHREGNO, COUNT(DISTINCT ATTENDDATE) AS VIRUS ");
            stb.append(      "FROM   T_ATTEND_DAT ");
            stb.append(      "WHERE  DI_CD IN ('19','20') ");
            stb.append(      "GROUP BY SCHREGNO ");
            stb.append(      ") TT3_1 ON TT0.SCHREGNO = TT3_1.SCHREGNO ");
            //個人別出停交止日数
            stb.append(   "LEFT OUTER JOIN(");
            stb.append(      "SELECT SCHREGNO, COUNT(DISTINCT ATTENDDATE) AS KOUDOME ");
            stb.append(      "FROM   T_ATTEND_DAT ");
            stb.append(      "WHERE  DI_CD IN ('25','26') ");
            stb.append(      "GROUP BY SCHREGNO ");
            stb.append(      ") TT3_2 ON TT0.SCHREGNO = TT3_2.SCHREGNO ");
            //個人別忌引日数
            stb.append(   "LEFT OUTER JOIN(");
            stb.append(      "SELECT SCHREGNO, COUNT(DISTINCT ATTENDDATE) AS MOURNING ");
            stb.append(      "FROM   T_ATTEND_DAT ");
            stb.append(      "WHERE  DI_CD IN ('3','10') ");
            stb.append(      "GROUP BY SCHREGNO ");
            stb.append(      ") TT4 ON TT0.SCHREGNO = TT4.SCHREGNO ");

            //個人別欠席日数
            stb.append(   "LEFT OUTER JOIN(");
            stb.append(      "SELECT  W0.SCHREGNO, ");
            stb.append(              "SUM(CASE W0.DI_CD WHEN '4' THEN 1 WHEN '11' THEN 1 ELSE 0 END) AS SICK, ");
            stb.append(              "SUM(CASE W0.DI_CD WHEN '5' THEN 1 WHEN '12' THEN 1 ELSE 0 END) AS NOTICE, ");
            stb.append(              "SUM(CASE W0.DI_CD WHEN '6' THEN 1 WHEN '13' THEN 1 ELSE 0 END) AS NONOTICE ");
            stb.append(      "FROM    ATTEND_DAT W0, ");
            stb.append(         "(");
            stb.append(         "SELECT  T0.SCHREGNO, T0.EXECUTEDATE, T0.FIRST_PERIOD ");
            stb.append(         "FROM    T_PERIOD_CNT T0, ");
            stb.append(            "(");
            stb.append(            "SELECT  W1.SCHREGNO, W1.ATTENDDATE, ");
            stb.append(                    "MIN(W1.PERIODCD) AS FIRST_PERIOD, ");
            stb.append(                    "COUNT(W1.PERIODCD) AS PERIOD_CNT ");
            stb.append(            "FROM    T_ATTEND_DAT W1 ");
            stb.append(            "WHERE   W1.YOMIKAEMAE IN ('4','5','6','11','12','13') ");
            stb.append(            "GROUP BY W1.SCHREGNO, W1.ATTENDDATE ");
            stb.append(            ") T1 ");
            stb.append(         "WHERE   T0.SCHREGNO = T1.SCHREGNO AND ");
            stb.append(                 "T0.EXECUTEDATE = T1.ATTENDDATE AND ");
            stb.append(                 "T0.FIRST_PERIOD = T1.FIRST_PERIOD AND ");
            stb.append(                 "T0.PERIOD_CNT = T1.PERIOD_CNT ");
            stb.append(         ") W1 ");
            stb.append(      "WHERE   W0.SCHREGNO = W1.SCHREGNO AND ");
            stb.append(              "W0.ATTENDDATE = W1.EXECUTEDATE AND ");
            stb.append(              "W0.PERIODCD = W1.FIRST_PERIOD ");
            stb.append(      "GROUP BY W0.SCHREGNO ");
            stb.append(      ")TT5 ON TT0.SCHREGNO = TT5.SCHREGNO ");

            //個人別遅刻・早退回数
            stb.append(   "LEFT OUTER JOIN(");
            stb.append(      "SELECT  T0.SCHREGNO, COUNT(T2.ATTENDDATE) AS LATE, COUNT(T3.ATTENDDATE) AS EARLY ");
            stb.append(      "FROM    T_PERIOD_CNT T0 ");
            stb.append(      "INNER JOIN(");
            stb.append(         "SELECT  W1.SCHREGNO, W1.ATTENDDATE, COUNT(W1.PERIODCD) AS PERIOD_CNT ");
            stb.append(         "FROM    T_ATTEND_DAT W1 ");
            stb.append(         "WHERE   W1.YOMIKAEMAE NOT IN ('0','14','15','16','23','24','29','30','31','32') ");
            stb.append(             "AND NOT EXISTS(SELECT 'X' FROM T_ATTEND_DAT_B W2 ");
            stb.append(                            "WHERE W2.SCHREGNO = W1.SCHREGNO AND W2.ATTENDDATE = W1.ATTENDDATE) ");
            stb.append(         "GROUP BY W1.SCHREGNO, W1.ATTENDDATE ");
            stb.append(         ")T1 ON T0.SCHREGNO = T1.SCHREGNO AND T0.EXECUTEDATE = T1.ATTENDDATE AND ");
            stb.append(                                              "T0.PERIOD_CNT != T1.PERIOD_CNT ");
            stb.append(      "LEFT OUTER JOIN(");
            stb.append(         "SELECT  SCHREGNO ,ATTENDDATE ,PERIODCD ");
            stb.append(         "FROM    T_ATTEND_DAT ");
            stb.append(         "WHERE   YOMIKAEMAE IN ('4','5','6','11','12','13') ");
            stb.append(         ")T2 ON T0.SCHREGNO = T2.SCHREGNO AND T0.EXECUTEDATE  = T2.ATTENDDATE AND ");
            stb.append(                                              "T0.FIRST_PERIOD = T2.PERIODCD ");
            stb.append(      "LEFT OUTER JOIN(");
            stb.append(         "SELECT  SCHREGNO ,ATTENDDATE ,PERIODCD ");
            stb.append(         "FROM    T_ATTEND_DAT ");
            stb.append(         "WHERE   YOMIKAEMAE IN ('4','5','6') ");
            stb.append(         ")T3 ON T0.SCHREGNO= T3.SCHREGNO AND T0.EXECUTEDATE = T3.ATTENDDATE AND ");
            stb.append(                                             "T0.LAST_PERIOD = T3.PERIODCD ");
            stb.append(      "GROUP BY T0.SCHREGNO ");
            stb.append(      ")TT6 ON TT0.SCHREGNO = TT6.SCHREGNO ");

            //個人別遅刻回数
            stb.append(   "LEFT OUTER JOIN(");
            stb.append(      "SELECT  T0.SCHREGNO, COUNT(T2.ATTENDDATE) AS LATE ");
            stb.append(      "FROM    T_PERIOD_CNT T0 ");
            stb.append(      "INNER JOIN(");
            stb.append(         "SELECT  SCHREGNO ,ATTENDDATE ,PERIODCD ");
            stb.append(         "FROM    T_ATTEND_DAT W1 ");
            stb.append(         "WHERE   YOMIKAEMAE IN ('15','23','24','29','31','32') ");
            stb.append(             "AND NOT EXISTS(SELECT 'X' FROM T_ATTEND_DAT_B W2 ");
            stb.append(                            "WHERE W2.SCHREGNO = W1.SCHREGNO AND W2.ATTENDDATE = W1.ATTENDDATE) ");
            stb.append(         ")T2 ON T0.SCHREGNO = T2.SCHREGNO AND T0.EXECUTEDATE  = T2.ATTENDDATE AND ");
            stb.append(                                              "T0.FIRST_PERIOD = T2.PERIODCD ");
            stb.append(      "GROUP BY T0.SCHREGNO ");
            stb.append(      ")TT10 ON TT0.SCHREGNO = TT10.SCHREGNO ");

            //個人別早退回数
            stb.append(   "LEFT OUTER JOIN(");
            stb.append(      "SELECT  T0.SCHREGNO, COUNT(T2.ATTENDDATE) AS EARLY ");
            stb.append(      "FROM    T_PERIOD_CNT T0 ");
            stb.append(      "INNER JOIN(");
            stb.append(         "SELECT  SCHREGNO ,ATTENDDATE ,PERIODCD ");
            stb.append(         "FROM    T_ATTEND_DAT W1 ");
            stb.append(         "WHERE   YOMIKAEMAE IN ('16','30','31','32') ");
            stb.append(             "AND NOT EXISTS(SELECT 'X' FROM T_ATTEND_DAT_B W2 ");
            stb.append(                            "WHERE W2.SCHREGNO = W1.SCHREGNO AND W2.ATTENDDATE = W1.ATTENDDATE) ");
            stb.append(         ")T2 ON T0.SCHREGNO = T2.SCHREGNO AND T0.EXECUTEDATE  = T2.ATTENDDATE AND ");
            stb.append(                                              "T0.LAST_PERIOD = T2.PERIODCD ");
            stb.append(      "GROUP BY T0.SCHREGNO ");
            stb.append(      ")TT11 ON TT0.SCHREGNO = TT11.SCHREGNO ");

            //月別集計データから集計した表
            stb.append(   "LEFT JOIN(");
            stb.append(      "SELECT SCHREGNO, ");
            stb.append(                   "VALUE(SUM(LESSON),0) - VALUE(SUM(OFFDAYS),0) - VALUE(SUM(ABROAD),0) ");
            if ("1".equals(_knjSchoolMst._semOffDays)) {
                stb.append(                   "+ VALUE(SUM(OFFDAYS),0) ");
            }
            stb.append("                  AS LESSON, ");
            stb.append(                   "SUM(MOURNING) AS MOURNING, ");
            stb.append(                   "SUM(SUSPEND) AS SUSPEND, ");
            if ("true".equals(_useVirus)) {
                stb.append(                   "SUM(VIRUS) AS VIRUS, ");
            } else {
                stb.append(                   "0 AS VIRUS, ");
            }
            if ("true".equals(_useKoudome)) {
                stb.append(                   "SUM(KOUDOME) AS KOUDOME, ");
            } else {
                stb.append(                   "0 AS KOUDOME, ");
            }
            stb.append(                   "SUM( VALUE(SICK,0) + VALUE(NOTICE,0) + VALUE(NONOTICE,0) ");
            if ("1".equals(_knjSchoolMst._semOffDays)) {
                stb.append(                   "+ VALUE(OFFDAYS,0) ");
            }
            stb.append(                   ") AS ABSENT, ");
            stb.append(                   "SUM(LATE) AS LATE, ");
            stb.append(                   "SUM(EARLY) AS EARLY, ");
            stb.append(                   "SUM(ABROAD) AS ABROAD, ");
            stb.append(                   "SUM(OFFDAYS) AS OFFDAYS ");
            stb.append(            "FROM   ATTEND_SEMES_DAT W1 ");
            stb.append(            "WHERE  YEAR = '" + param[0] + "' AND ");
            stb.append(                   "SEMESTER <= '" + param[1] + "' AND ");
            stb.append(                   "SEMESTER||MONTH <= '" + param[6] + "' AND ");
            stb.append(                   "EXISTS(");
            stb.append(                       "SELECT  'X' ");
            stb.append(                       "FROM    SCHNO W2 ");
            stb.append(                       "WHERE   W1.SCHREGNO = W2.SCHREGNO)");
            stb.append(            "GROUP BY SCHREGNO ");
            stb.append(      ")TT7 ON TT0.SCHREGNO = TT7.SCHREGNO ");

            //留学日数の表
            stb.append(   "LEFT JOIN(");
            stb.append(      "SELECT SCHREGNO, SUM(TRANSFER_DATE) AS TRANSFER_DATE ");
            stb.append(      "FROM   TRANSFER_SCHREG ");
            stb.append(      "WHERE  TRANSFERCD = '1' ");
            stb.append(      "GROUP BY SCHREGNO ");
            stb.append(   ")TT8 ON TT8.SCHREGNO = TT0.SCHREGNO ");

            //休学日数の表
            stb.append(   "LEFT JOIN(");
            stb.append(      "SELECT SCHREGNO, SUM(TRANSFER_DATE) AS TRANSFER_DATE ");
            stb.append(      "FROM   TRANSFER_SCHREG ");
            stb.append(      "WHERE  TRANSFERCD = '2' ");
            stb.append(      "GROUP BY SCHREGNO ");
            stb.append(   ")TT9 ON TT9.SCHREGNO = TT0.SCHREGNO ");

            //欠課の表
            stb.append(   "LEFT JOIN SUBCLASS_ATTEND_B TT13 ON TT13.SCHREGNO = TT0.SCHREGNO ");

            stb.append(") TBL1 ");

            stb.append("LEFT JOIN SCHREG_REGD_HDAT TBL2 ON TBL2.YEAR = '" + param[0] + "' AND ");
            stb.append("                                   TBL2.SEMESTER = '" + param[1] + "' AND ");
            stb.append("                                   TBL2.GRADE = TBL1.GRADE AND ");
            stb.append("                                   TBL2.HR_CLASS = TBL1.HR_CLASS ");

            stb.append("LEFT JOIN SCHREG_BASE_MST TBL3 ON TBL3.SCHREGNO = TBL1.SCHREGNO ");

            stb.append("WHERE TBL1.ABSENT = 0 AND ");
            stb.append("      TBL1.LATE = 0 AND ");
            stb.append("      TBL1.EARLY = 0 AND ");
            stb.append("      TBL1.KTRANSFER_DATE = 0 AND ");
            stb.append("      TBL1.ABSENT1 = 0 AND ");
            stb.append("      TBL1.LATE_EARLY = 0 AND ");
            stb.append("      TBL1.LEAVE = 0 ");

            stb.append("ORDER BY TBL1.GRADE, TBL1.HR_CLASS, ATTENDNO ");
        } catch( Exception ex ){
            log.warn("error! ",ex);
        }
        return stb.toString();

    }//psAttendPerfect()の括り


    /**PrepareStatement close**/
    private void Pre_Stat_f(PreparedStatement ps6) {

        try {
            if( ps6 != null )ps6.close();
        } catch( Exception e ){
            log.warn("Pre_Stat_f error!");
        }

    }//Pre_Stat_f()の括り


    /**
     *  出欠集計テーブルをみる最終月と出欠データをみる開始日を取得する
     */
    private void getDivideAttendDate( DB2UDB db2, String param[] )
    {
        KNJDivideAttendDate obj = new KNJDivideAttendDate();
        try {
            obj.getDivideAttendDate( db2, param[0], param[1], param[2] );
            param[5] = obj.date;
            param[6] = obj.month;
        } catch( Exception ex ){
            log.error("error! ",ex);
        }
    }



}//クラスの括り
