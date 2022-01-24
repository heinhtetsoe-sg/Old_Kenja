// kanji=漢字
/*
 * $Id: 6eec2300142db38f294b5e350ee41d12680b89fa $
 *
 * 作成日:
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJC;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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

import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJServletpacksvfANDdb2;
import servletpack.KNJZ.detail.KNJ_ClassCode;
import servletpack.KNJZ.detail.KNJ_Control;
import servletpack.KNJZ.detail.KNJ_EditDate;

/*
 *  学校教育システム 賢者 [出欠管理]     勤怠集計表 [個人資料]  講座別勤怠集計表
 *
 *  科目別勤怠集計表(KNJC110)を基に作成。
 *
 * $Id: 6eec2300142db38f294b5e350ee41d12680b89fa $
 */

public class KNJC110A implements KNJ_ClassCode{

    private static final Log log = LogFactory.getLog(KNJC110A.class);
    private boolean nonedata;               //該当データなしフラグ
    private Param param;
    private KNJDefineSchool definecode;       //各学校における定数等設定

    /**
     * HTTP Get リクエストの処理
     */
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws ServletException, IOException {
        final KNJServletpacksvfANDdb2 sd = new KNJServletpacksvfANDdb2();  //帳票におけるＳＶＦおよびＤＢ２の設定

        MyVrw32alp svf = new MyVrw32alp();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;              // Databaseクラスを継承したクラス

        // パラメータの取得
        KNJServletUtils.debugParam(request, log);
        log.fatal(" $Revision: 65440 $ $Date: 2019-01-31 16:08:07 +0900 (木, 31 1 2019) $");
        param = new Param(request);

        // print svf設定
        sd.setSvfInit(request, response, svf);

        // ＤＢ接続
        db2 = sd.setDb(request);
        if (sd.openDb(db2)) {
            log.error("db open error");
            return;
        }

        // 印刷処理
        String[] schno = request.getParameterValues("category_selected");  // 学籍番号
        printSvf(db2, svf, schno, param);

        // 終了処理
        sd.closeSvf(svf,nonedata);
        sd.closeDb(db2);
    }

    /*
     *  印刷処理
     */
    private void printSvf(
            final DB2UDB db2,
            final Vrw32alp svf,
            final String schno[],
            final Param param
    ) {
        printSvfHead(db2, svf);              //見出し出力のメソッド
        for(int i = 0; i < schno.length; i++) {
            final Student student = getStudent(db2, schno[i]);
            printSvfRegd(svf, student);             //学籍データ出力のメソッド
            printSvfAttendChair(db2, svf, student, param);      //講座別の校時出欠データ出力のメソッド svf.VrEndRecord()
            printSvfTotalChair(db2, svf, student);      //講座別の校時開講回数出力のメソッド svf.VrEndRecord()
        }
    }

    /*
     *  印刷処理 見出し出力
     */
    private void printSvfHead(
            final DB2UDB db2,
            final Vrw32alp svf
    ) {

        //  欠課数換算定数取得 => KNJDefineCodeImpを実装したオブジェクトを作成
        definecode = new KNJDefineSchool();
        definecode.defineCode (db2, param._year);         //各学校における定数等設定
        log.debug("semesdiv=" + definecode.semesdiv + "   absent_cov=" + definecode.absent_cov + "   absent_cov_late=" + definecode.absent_cov_late);

        svf.VrSetForm("KNJC110A.frm", 4);
        svf.VrAttribute("HR_NAME","FF=1");

        svf.VrsOut("PERIOD", KNJ_EditDate.h_format_JP(param._date1) + " \uFF5E " + KNJ_EditDate.h_format_JP(param._date2));  //集計期間

        //  作成日(現在処理日)の取得
        KNJ_Control control = new KNJ_Control();
        KNJ_Control.ReturnVal returnval = control.Control(db2);
        svf.VrsOut("ymd"  ,KNJ_EditDate.h_format_JP(returnval.val3));             //作成日
        control = null;
        returnval = null;
    }

    /*
     *  印刷処理 生徒名出力
     */
    private Student getStudent(
            final DB2UDB db2,
            final String schregno
    ) {
        //SQL作成
        PreparedStatement ps = null;
        ResultSet rs = null;
        Student student = null;
        try {
            //学籍のSQL
            final String sql = prestatementRegd();
            ps = db2.prepareStatement(sql);
            ps.setString(1, schregno);
            rs = ps.executeQuery();
            if (rs.next()) {
                student = new Student(rs.getString("SCHREGNO"), rs.getString("NAME"), rs.getString("HR_NAME"), rs.getString("ATTENDNO"));
            }
        } catch (Exception ex) {
            log.error("[KNJC110]printSvfRegd error!", ex);
        } finally{
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return student;
    }

    /*
     *  印刷処理 生徒名出力
     */
    private void printSvfRegd(
            final Vrw32alp svf,
            final Student student
    ) {
        try {
            //学籍のSQL
            //組名称の編集
            if (student._hrname.lastIndexOf("組") > -1) {
                svf.VrsOut("HR_NAME", student._hrname + student._attendno + "番");        //組名称&出席番号
            } else {
                svf.VrsOut("HR_NAME", student._hrname + "-" + student._attendno + "番");  //組名称&出席番号
            }
            svf.VrsOut("name", student._name);     //氏名
        } catch (Exception ex) {
            log.error("[KNJC110]printSvfRegd error!", ex);
        }
    }

    private static class Student {
        final String _schregno;
        final String _name;
        final String _hrname;
        final String _attendno;
        public Student(final String schregno, final String name, final String hrname, final String attendno) {
            _schregno = schregno;
            _name = name;
            _hrname = hrname;
            _attendno = attendno;
        }
    }

    /*
     *  印刷処理 出欠データ出力
     *    校時（講座）の出欠状況を出力
     */
    private void printSvfAttendChair (
            final DB2UDB db2,
            final Vrw32alp svf,
            final Student student,
            final Param param
    ) {
        //講座別の遅刻・早退・欠課のSQL
        PreparedStatement ps2 = null;
        ResultSet rs = null;
        try {
            String sql = null;
            sql = prestatementChair_Common(student, param);
//            log.debug("prestatementChair sql="+sql);
            ps2 = db2.prepareStatement (sql);
            rs = ps2.executeQuery();
            String di_cd = null;
            String chaircd = null;
            int scount = 0;

            while (rs.next()) {
                //出欠種別のブレイク
                if (di_cd == null || !di_cd.equals(rs.getString("DI_CD"))) {
                    if (0 < scount) {
                        svf.VrEndRecord();
                        nonedata = true;
                        scount = 0;
                    }
                    di_cd = rs.getString("DI_CD");
                    chaircd = null;
                    final String kintai;
                    if ("1".equals(di_cd)) {
                        kintai = "遅刻";
                    } else if ("2".equals(di_cd)) {
                        kintai = "早退";
                    } else {
                        kintai = "欠課";
                    }
                    svf.VrsOut("KINTAI", kintai);
                    svf.VrsOut("total",  rs.getString("DI_TOTAL") );
                    svf.VrEndRecord();
                    nonedata = true;
                }
                //講座のブレイク
                if (chaircd == null  ||  ! chaircd.equals(rs.getString("CHAIRCD"))){
                    if (0 < scount) {
                        svf.VrEndRecord();
                        nonedata = true;
                        scount = 0;
                    }
                    chaircd = rs.getString("CHAIRCD");
                    svf.VrsOut("subject1", rs.getString("CHAIRNAME"));
                    if (rs.getString("CHAIR_TOTAL") != null) {
                        Integer chairTotal = Integer.valueOf(rs.getString("CHAIR_TOTAL"));
                        String strChairTotal = new DecimalFormat("000").format(chairTotal.intValue());
                        // 右詰処理
                        String hoge = "";
                        for(int i=0; i<3; i++) {
                            if(strChairTotal.charAt(i) != '0') {
                                hoge += strChairTotal.substring(i);
                                break;
                            }
                            hoge += " ";
                        }
                        strChairTotal = hoge;

                        log.debug("strChairTotal=" + strChairTotal);
                        svf.VrsOut("late_no", "( " + strChairTotal + "回 )");
                    } else
                        svf.VrsOut("late_no",  "回");
                }
                //出欠データ明細の出力
                if (5 <= scount) {
                    svf.VrEndRecord();
                    nonedata = true;
                    scount = 0;
                }
                scount++;
                if (rs.getString("EXECUTEDATE") != null) {
                    Calendar cal1 = Calendar.getInstance();
                    cal1.setTime(rs.getDate("EXECUTEDATE"));
                    final String tuki = String.valueOf(cal1.get(Calendar.MONTH) + 1);
                    final String hi = String.valueOf(cal1.get(Calendar.DATE));
                    final String youbi = "(" + KNJ_EditDate.h_format_W(rs.getString("EXECUTEDATE")) + ")";
                    svf.VrsOut("la" + scount, tuki + "/" + hi + youbi);//月日
                }
            }
            if (0 < scount) {
                svf.VrEndRecord();
                nonedata = true;
            }
        } catch( Exception ex ) {
            log.error("[KNJC110A]printSvfAttendChair error!", ex);
        } finally{
            db2.commit();
            DbUtils.closeQuietly(null, ps2, rs);
        }
    }


    /*
     *  印刷処理 出欠データ出力
     *    校時（講座）の開講状況を出力
     */
    private void printSvfTotalChair (
            final DB2UDB db2,
            final Vrw32alp svf,
            final Student student
    ) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            String sql = prestatementTotalChair(student);
//            log.debug("prestatementTotalChair sql="+sql);
            ps = db2.prepareStatement(sql);
            int p = 0;
            ps.setString(++p, student._schregno);
            rs = ps.executeQuery();
            String chaircd = null;
            String chairname = null;
            String strCount = null;
            int scount = 0;
            boolean printJisu = false;

            while ( rs.next() ){
                //出欠種別のブレイク
                if (printJisu == false) {
                    if (0 < scount) {
                        svf.VrEndRecord();
                        nonedata = true;
                        scount = 0;
                    }
                    chaircd = null;
                    nonedata = true;
                    svf.VrsOut("KINTAI", "授業時数");
                    svf.VrEndRecord();
                    printJisu = true;
                }
                //講座のブレイク
                if (chaircd == null || !chaircd.equals(rs.getString("CHAIRCD"))){
                    if (0 < scount) {
                        svf.VrEndRecord();
                        nonedata = true;
                        scount = 0;
                    }
                    chaircd = rs.getString("CHAIRCD");
                    chairname = rs.getString("CHAIRNAME");
                    svf.VrsOut("subject1", chairname);
                }
                //出欠データ明細の出力
                if (5 <= scount) {
                    svf.VrEndRecord();
                    nonedata = true;
                    scount = 0;
                }
                scount++;
                if (rs.getString("COUNT") != null) {
                    Integer count = Integer.valueOf(rs.getString("COUNT"));
                    strCount = new DecimalFormat("000").format(count.intValue());
                    // 右詰処理
                    String hoge = "";
                    for(int i=0; i<3; i++) {
                        if(strCount.charAt(i) != '0') {
                            hoge += strCount.substring(i);
                            break;
                        }
                        hoge += " ";
                    }
                    strCount = hoge;
                    svf.VrsOut("late_no", "( " + strCount + "回 )");
                }
                log.debug(" chairname=" + chairname + ",\t count=\"" + strCount + "\"");
            }
            if (0 < scount) {
                svf.VrEndRecord();
                nonedata = true;
            }
        } catch( Exception ex ) {
            log.error("[KNJC110A]printSvfTotalChair error!", ex);
        } finally{
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
    }

    /*
     *  preparedStatement作成  学籍データ
     *    該当生徒の前学期のデータを降順で取得する => 最初のレコードだけ使用する
     */
    private String prestatementRegd () {
        final StringBuffer stb = new StringBuffer();
        stb.append("SELECT ");
        stb.append("    W1.SCHREGNO, W1.NAME, W3.HR_NAME, INT(W2.ATTENDNO)AS ATTENDNO ");
        stb.append("FROM ");
        stb.append("    SCHREG_BASE_MST W1");
        stb.append("    INNER JOIN SCHREG_REGD_DAT W2 ON W1.SCHREGNO = W2.SCHREGNO ");
        stb.append("    INNER JOIN SCHREG_REGD_HDAT W3 ON W2.YEAR = W3.YEAR AND W2.SEMESTER = W3.SEMESTER AND W2.GRADE = W3.GRADE AND W2.HR_CLASS = W3.HR_CLASS ");
        stb.append("WHERE ");
        stb.append("       W1.SCHREGNO = ? AND ");
        stb.append(       "W2.YEAR = '" + param._year + "' ");
        stb.append("ORDER BY W2.SEMESTER DESC");
        return stb.toString();
    }

    private String prestatementTotalChair(final Student student) {
        StringBuffer stb = new StringBuffer();
        stb.append(" WITH MK AS ( ");
        stb.append(" SELECT T1.YEAR, T1.SEMESTER, T1.CHAIRCD, L1.CHAIRNAME, T1.SCHREGNO, T1.APPDATE, T1.APPENDDATE");
        stb.append(" FROM CHAIR_STD_DAT T1");
        stb.append("     INNER JOIN CHAIR_DAT L1 ON");
        stb.append("         T1.YEAR = L1.YEAR AND");
        stb.append("         T1.SEMESTER = L1.SEMESTER AND");
        stb.append("         T1.CHAIRCD = L1.CHAIRCD");
        stb.append(" WHERE T1.SCHREGNO = ? AND T1.YEAR='" + param._year + "' ");
        stb.append(" ORDER BY T1.YEAR, T1.CHAIRCD, T1.SEMESTER");
        stb.append(" )");
        stb.append(" SELECT L1.CHAIRCD, MAX(T1.CHAIRNAME) AS CHAIRNAME, COUNT(L1.CHAIRCD) AS COUNT ");
        stb.append(" FROM MK T1 ");
        stb.append("     LEFT JOIN SCH_CHR_DAT L1 ON");
        stb.append("         L1.YEAR = T1.YEAR AND");
        stb.append("         L1.SEMESTER = T1.SEMESTER AND");
        stb.append("         L1.CHAIRCD = T1.CHAIRCD");
        stb.append("         AND L1.EXECUTEDATE BETWEEN T1.APPDATE AND T1.APPENDDATE");
        stb.append(" WHERE");
        stb.append("  L1.EXECUTEDATE BETWEEN '" + param._date1 + "' AND '" + param._date2 + "'");
        stb.append(" GROUP BY L1.CHAIRCD, T1.CHAIRCD");
        stb.append(" ORDER BY T1.CHAIRCD");
        return stb.toString();
    }
    /*
     *  preparedStatement作成(講座)  遅刻・早退・欠課
     *    時間割データにリンクした出欠データの表
     *    2008/10/08 講座コードを追加
     */
    private String prestatementChair_Common(final Student student, final Param param) {
        StringBuffer stb = new StringBuffer();
        stb.append(" WITH ");
        stb.append("  TEST_COUNTFLG AS ( ");
        stb.append("     SELECT ");
        stb.append("         T1.EXECUTEDATE, ");
        stb.append("         T1.PERIODCD, ");
        stb.append("         T1.CHAIRCD, ");
        stb.append("         '2' AS DATADIV ");
        stb.append("     FROM ");
        stb.append("         SCH_CHR_TEST T1, ");
        if ("TESTITEM_MST_COUNTFLG".equals(param._useTestCountflg)) {
            stb.append("         TESTITEM_MST_COUNTFLG T2 ");
            stb.append("     WHERE ");
            stb.append("         T2.YEAR       = T1.YEAR ");
        } else if ("TESTITEM_MST_COUNTFLG_NEW_SDIV".equals(param._useTestCountflg)) {
            stb.append("         TESTITEM_MST_COUNTFLG_NEW_SDIV T2 ");
            stb.append("     WHERE ");
            stb.append("         T2.YEAR       = T1.YEAR ");
            stb.append("         AND T2.SEMESTER   = T1.SEMESTER ");
            stb.append("         AND T2.SCORE_DIV  = '01' ");
        } else {
            stb.append("         TESTITEM_MST_COUNTFLG_NEW T2 ");
            stb.append("     WHERE ");
            stb.append("         T2.YEAR       = T1.YEAR ");
            stb.append("         AND T2.SEMESTER   = T1.SEMESTER ");
        }
        stb.append("         AND T2.TESTKINDCD = T1.TESTKINDCD ");
        stb.append("         AND T2.TESTITEMCD = T1.TESTITEMCD ");
        stb.append("         AND T2.COUNTFLG   = '0' ");
        stb.append(" ), ATTEND_DETAIL AS(");
        stb.append("   SELECT S1.SCHREGNO, S1.SEMESTER, S1.CHAIRCD, S1.EXECUTEDATE, S1.PERIODCD,");
        stb.append("          CASE (CASE WHEN L1.REP_DI_CD IN ('29','30','31') THEN VALUE(L1.ATSUB_REPL_DI_CD, L1.REP_DI_CD) ELSE L1.REP_DI_CD END) ");
        stb.append("                        WHEN '7'  THEN '1' ");
        stb.append("                        WHEN '15' THEN '1' ");
        stb.append("                        WHEN '23' THEN '1' ");
        stb.append("                        WHEN '24' THEN '1' ");
        stb.append("                        WHEN '16' THEN '2' ");
        stb.append("                        ELSE '3' END AS DI_CD, ");
        stb.append("          S2.DI_CD AS DI_CD0");
        stb.append("   FROM  (SELECT T2.SCHREGNO ,T1.EXECUTEDATE, T1.PERIODCD, T1.CHAIRCD, T1.SEMESTER");
        stb.append("          FROM   SCH_CHR_DAT T1");
        stb.append("          ,CHAIR_STD_DAT T2");
        stb.append("          ,CHAIR_DAT T3");
        stb.append("          WHERE  T1.YEAR = '" + param._year + "'");
        stb.append("             AND T1.EXECUTEDATE BETWEEN DATE('" + param._date1 + "') AND DATE('" + param._date2 + "')");
        stb.append("             AND T1.CHAIRCD = T2.CHAIRCD");
        stb.append("             AND T1.YEAR = T2.YEAR");
        stb.append("             AND T1.SEMESTER = T2.SEMESTER");
        stb.append("             AND T1.EXECUTEDATE BETWEEN T2.APPDATE AND T2.APPENDDATE");
        stb.append("             AND T1.CHAIRCD = T3.CHAIRCD");
        stb.append("             AND T1.YEAR = T3.YEAR");
        stb.append("             AND T1.SEMESTER = T3.SEMESTER");
        stb.append("             AND T2.SCHREGNO = '" + student._schregno + "' ");
        if( definecode.useschchrcountflg ){
            //                       COUNTFLGによる制限
            stb.append("         AND NOT EXISTS(SELECT 'X' FROM SCH_CHR_COUNTFLG T4");
            stb.append("                        WHERE  T4.EXECUTEDATE = T1.EXECUTEDATE");
            stb.append("                           AND T4.PERIODCD =T1.PERIODCD");
            stb.append("                           AND T4.CHAIRCD = T1.CHAIRCD");
            stb.append("                           AND VALUE(T1.DATADIV, '0') <> '2' ");
            stb.append("                           AND T4.GRADE||T4.HR_CLASS = '" + param._gradeHrClass + "'");
            stb.append("                           AND T4.COUNTFLG = '0')");
            stb.append("         AND NOT EXISTS(SELECT 'X' FROM TEST_COUNTFLG TEST ");
            stb.append("                       WHERE ");
            stb.append("                           TEST.EXECUTEDATE  = T1.EXECUTEDATE ");
            stb.append("                           AND TEST.PERIODCD = T1.PERIODCD ");
            stb.append("                           AND TEST.CHAIRCD  = T1.CHAIRCD ");
            stb.append("                           AND TEST.DATADIV  = T1.DATADIV) ");
        }
        //                           学籍異動による制限
        stb.append("             AND NOT EXISTS(SELECT 'X' FROM SCHREG_BASE_MST T4");
        stb.append("                            WHERE  T4.SCHREGNO = '" + student._schregno + "'");
        stb.append("                               AND ((ENT_DIV IN('4','5') AND EXECUTEDATE < ENT_DATE)");
        stb.append("                                 OR (GRD_DIV IN('2','3') AND EXECUTEDATE > GRD_DATE)))");
        //                           休学・留学による制限
        stb.append("             AND NOT EXISTS(SELECT 'X' FROM SCHREG_TRANSFER_DAT T4");
        stb.append("                            WHERE  T4.SCHREGNO = '" + student._schregno + "'");
        stb.append("                               AND (TRANSFERCD IN('1','2') AND EXECUTEDATE BETWEEN TRANSFER_SDATE AND TRANSFER_EDATE ))");
        stb.append("          GROUP BY T2.SCHREGNO,T1.EXECUTEDATE,T1.PERIODCD, T1.CHAIRCD, T1.SEMESTER ");
        stb.append("         )S1 ");
        stb.append(" INNER JOIN ATTEND_DAT S2 ON S2.YEAR = '" + param._year + "'");
        stb.append("                         AND S2.ATTENDDATE BETWEEN DATE('" + param._date1 + "') AND DATE('" + param._date2 + "')");
        stb.append("                         AND S2.ATTENDDATE = S1.EXECUTEDATE");
        stb.append("                         AND S2.PERIODCD = S1.PERIODCD");
        stb.append("                         AND S1.SCHREGNO = S2.SCHREGNO");
        stb.append("                         AND S2.DI_CD IN('4','5','6','7','11','12','13','14','15','16','23','24','29','30','31') ");
        stb.append(" LEFT JOIN ATTEND_DI_CD_DAT L1 ON L1.YEAR = '" + param._year + "' AND L1.DI_CD = S2.DI_CD ");
        stb.append(" ) ");
        stb.append("SELECT DISTINCT S1.SEMESTER, S1.SCHREGNO, S1.DI_CD, S1.CHAIRCD, S1.EXECUTEDATE, S4.CHAIRNAME, ");
        stb.append(       "S3.COUNT AS DI_TOTAL, S2.COUNT AS CHAIR_TOTAL ");
        stb.append("FROM(");
        stb.append(     "SELECT SEMESTER, SCHREGNO, T1.DI_CD, CHAIRCD, EXECUTEDATE ");
        stb.append(     "FROM   ATTEND_DETAIL T1 ");
        stb.append(    ")S1 ");
        stb.append(  "LEFT JOIN(");
        stb.append(     "SELECT SCHREGNO, T1.DI_CD, CHAIRCD, SUM(SMALLINT(VALUE(L1.MULTIPLY,'1'))) AS COUNT ");
        stb.append(     "FROM   ATTEND_DETAIL T1 ");
        stb.append(     "LEFT JOIN ATTEND_DI_CD_DAT L1 ON L1.YEAR = '" + param._year + "' AND L1.DI_CD = T1.DI_CD0 ");
        stb.append(     "GROUP BY SCHREGNO, CHAIRCD, T1.DI_CD ");
        stb.append(    ") S2 ON S1.SCHREGNO = S2.SCHREGNO AND S1.DI_CD = S2.DI_CD AND S1.CHAIRCD = S2.CHAIRCD ");
        stb.append(  "LEFT JOIN (");
        stb.append(     "SELECT SCHREGNO, T1.DI_CD, SUM(SMALLINT(VALUE(L1.MULTIPLY,'1')))  AS COUNT ");
        stb.append(     "FROM   ATTEND_DETAIL T1 ");
        stb.append(     "LEFT JOIN ATTEND_DI_CD_DAT L1 ON L1.YEAR = '" + param._year + "' AND L1.DI_CD = T1.DI_CD0 ");
        stb.append(     "GROUP BY SCHREGNO, T1.DI_CD ");
        stb.append(    ") S3 ON S1.SCHREGNO = S3.SCHREGNO AND S1.DI_CD = S3.DI_CD ");
        stb.append(  "LEFT JOIN CHAIR_DAT S4 ON S4.CHAIRCD = S1.CHAIRCD AND S4.YEAR = '"+param._year+"' AND S4.SEMESTER = S1.SEMESTER ");
        stb.append("ORDER BY S1.SCHREGNO, S1.DI_CD, S1.CHAIRCD, S1.EXECUTEDATE ");
        return stb.toString();
    }

    private static class Param {
        final String _year;
        final String _semester;
        final String _date1;
        final String _date2;
        final String _gradeHrClass;
        private final String _useTestCountflg;

        Param(final HttpServletRequest request) {
            _year  = request.getParameter("YEAR");           //年度
            _semester  = request.getParameter("SEMESTER");       //学期
            //日付型を変換
            KNJ_EditDate editdate = new KNJ_EditDate();                                 //クラスのインスタンス作成
            _date1 = editdate.h_format_sec(request.getParameter("DATE1"));            //印刷範囲開始
            _date2 = editdate.h_format_sec(request.getParameter("DATE2"));            //印刷範囲開始
            _gradeHrClass = request.getParameter("GRADE_HR_CLASS");  // 学年・組
            editdate = null;
            _useTestCountflg = request.getParameter("useTestCountflg");
        }
    }

    //--- 内部クラス -------------------------------------------------------
    private static class MyVrw32alp extends Vrw32alp {
        public int VrsOut(String field, String data) {
            if (null == field || null == data) return 0;
            return super.VrsOut(field, data);
        }

        public int VrsOutn(String field, int gyo, String data) {
            if (null == field || null == data) return 0;
            return super.VrsOutn(field, gyo, data);
        }
    }

}
