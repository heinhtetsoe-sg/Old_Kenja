// kanji=漢字
/*
 * $Id$
 */
package servletpack.KNJD;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_Control;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.dao.AttendAccumulate;
import servletpack.pdf.IPdf;
import servletpack.pdf.SvfPdf;

/**
 * 学校教育システム 賢者 [成績管理]  成績通知票
 */
public class KNJD187E {
    private static final Log log = LogFactory.getLog(KNJD187E.class);

    private static final String SEMEALL = "9";
    private static final String SUBCLASSCD555555 = "555555";
    private static final String SUBCLASSCD999999 = "999999";
    private static final String SUBCLASSCD999999AVG = "999999AVG";
    private static final String TESTCD_GAKUNEN_HYOKA = "9990008";
    private static final String TESTCD_GAKUNEN_HYOTEI = "9990009";
    private static final String TESTCD_GAKKI_HYOKA_NO_SEMES = "020108";
    private static final String TESTCD_MIDDLE_HYOKA_NO_SEMES = "010108";

    private boolean _hasData;

    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws ServletException, IOException {
        Vrw32alp svf = null;
        try {
            svf = new Vrw32alp();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
            svf.VrInit();
            svf.VrSetSpoolFileStream(response.getOutputStream());

            final IPdf ipdf = new SvfPdf(svf);

            response.setContentType("application/pdf");

            outputPdf(ipdf, request);

        } catch (final Exception ex) {
            log.error("error! ", ex);
        } finally {
            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note" , "note");
                svf.VrEndPage();
            }
            svf.VrQuit();
        }
    }

    public void outputPdf(
            final IPdf ipdf,
            final HttpServletRequest request
    ) throws Exception {
        DB2UDB db2 = null;
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            // パラメータの取得
            log.fatal("$Id$"); // CVSキーワードの取り扱いに注意
            KNJServletUtils.debugParam(request, log);
            final Param param = new Param(request, db2);

            printMain(db2, ipdf, param);

        } catch (final Exception ex) {
            log.error("error! ", ex);
        } finally {
            try {
                db2.commit();
                db2.close();
            } catch (Exception ex) {
                log.error("db close error!", ex);
            }
        }
    }

    protected void printMain(
            final DB2UDB db2,
            final IPdf ipdf,
            final Param param
    ) {
        final List studentList = Student.getStudentList(db2, param);
        if (studentList.isEmpty()) {
            return;
        }
        load(param, db2, studentList);

        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            log.info(" schregno = " + student._schregno);

            print2(db2, ipdf, param, student);
            _hasData = true;
        }
    }

    private void load(
            final Param param,
            final DB2UDB db2,
            final List studentList
    ) {

        final Map studentMap = new HashMap();
        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            studentMap.put(student._schregno, student);
        }

        //出席情報取得8※1/2/学年で取るように変更
        List getSemsList = new ArrayList();
        getSemsList.add("1");
        getSemsList.add("2");
        getSemsList.add("9");
        for (Iterator itq = getSemsList.iterator();itq.hasNext();) {
            String semeStr = (String)itq.next();
            final Semester semesInf = (Semester) param._semesterMap.get(semeStr);
            if (null != semesInf) {
                Attendance.load(db2, param, studentMap, semesInf._dateRange);
            }
        }
        //科目別の欠課情報を取得(student._tbl5InfoMapに取得データは格納)
        gethrAttendSemesInfo(db2, param, studentMap);

        //成績情報取得
        String testcdor = "";
        final StringBuffer stbtestcd = new StringBuffer();
        stbtestcd.append(" ( ");
        for (int i = 0; i < param._testcds.length; i++) {
            final String testcd = param._testcds[i];
            if (null == testcd) {
                continue;
            }
            final String seme = testcd.substring(0, 1);
            final String kind = testcd.substring(1, 3);
            final String item = testcd.substring(3, 5);
            final String sdiv = testcd.substring(5);
            if (seme.compareTo(param._semester) <= 0) {
                stbtestcd.append(testcdor);
                stbtestcd.append(" ( W3.SEMESTER <= '" + seme + "' AND W3.TESTKINDCD = '" + kind + "' AND W3.TESTITEMCD = '" + item + "' AND W3.SCORE_DIV = '" + sdiv + "' ) ");
                testcdor = " OR ";
            }
        }
        stbtestcd.append(" ) ");
        Score.load(db2, param, studentMap, stbtestcd);

        //委員会情報取得(指定学期まで)
        final String sqlCommittee = " SELECT T1.SEMESTER, T2.COMMITTEE_FLG, T2.COMMITTEENAME FROM SCHREG_COMMITTEE_HIST_DAT T1 "
                + " LEFT JOIN COMMITTEE_MST T2 ON T2.COMMITTEE_FLG = T1.COMMITTEE_FLG AND T2.COMMITTEECD = T1.COMMITTEECD "
                + " WHERE YEAR = '" + param._ctrlYear + "' AND SCHREGNO = ? AND SEMESTER <= '" + param._nouse9Semester + "' ORDER BY T1.SEMESTER, T2.COMMITTEE_FLG DESC, T2.COMMITTEECD ";
        //部活情報取得(指定学期に在籍した部活のみ)
        Semester semesInf = (Semester)param._semesterMap.get(param._nouse9Semester);
        final String sqlClub = " SELECT T1.CLUBCD, T3.CLUBNAME, MAX(T1.SDATE) AS SDATE FROM SCHREG_CLUB_HIST_DAT T1 "
                + " INNER JOIN SCHREG_REGD_GDAT T2 ON T2.SCHOOL_KIND = T1.SCHOOL_KIND AND T2.YEAR = '" + param._ctrlYear + "' AND T2.GRADE = '" + param._grade + "' "
                + " LEFT JOIN CLUB_MST T3 ON T3.SCHOOLCD = T1.SCHOOLCD AND T3.SCHOOL_KIND = T1.SCHOOL_KIND AND T3.CLUBCD = T1.CLUBCD "
                + " WHERE T1.SCHREGNO = ? "
                + " AND ( "
                + "  (T1.EDATE IS NULL AND T1.SDATE <= '" + semesInf._dateRange._edate + "') or (T1.SDATE <= '" + semesInf._dateRange._sdate + "' AND '" + semesInf._dateRange._sdate + "' <= T1.EDATE)"
                + "  or (T1.SDATE <= '" + semesInf._dateRange._edate + "' AND '" + semesInf._dateRange._edate + "' <= T1.EDATE) or ('" + semesInf._dateRange._sdate + "' <= T1.SDATE AND T1.EDATE <= '" + semesInf._dateRange._edate + "') "
                + " ) GROUP BY T1.CLUBCD, T3.CLUBNAME ORDER BY T1.CLUBCD ";

        //欠席理由
        final String sqlSemesRemark = " SELECT SEMESTER, MONTH, REMARK1 FROM ATTEND_SEMES_REMARK_DAT WHERE YEAR = '" + param._ctrlYear + "' AND SEMESTER <= '" + param._nouse9Semester + "' AND SCHREGNO = ? AND REMARK1 IS NOT NULL ORDER BY SEMESTER, MONTH ";

        //x年次評定(評定平均。学年末指定時以外は出さない。)
        StringBuffer strr = new StringBuffer();
        strr.append(" WITH IGNORE_RYUNENYEAR AS ( ");
        strr.append(" SELECT ");
        strr.append("     T2.SCHOOL_KIND, ");
        strr.append("     T2.GRADE, ");
        strr.append("     T1.SCHREGNO, ");
        strr.append("     MAX(T1.SEMESTER) AS SEMESTER, ");
        strr.append("     MAX(T1.YEAR) AS YEAR ");
        strr.append(" FROM ");
        strr.append("     SCHREG_REGD_DAT T1 ");
        strr.append("     INNER JOIN SCHREG_REGD_GDAT T2 ");
        strr.append("       ON T2.YEAR = T1.YEAR ");
        strr.append("      AND T2.GRADE = T1.GRADE ");
        strr.append("      AND T2.SCHOOL_KIND = ( ");
        strr.append("            SELECT ");
        strr.append("              TW.SCHOOL_KIND ");
        strr.append("            FROM ");
        strr.append("              SCHREG_REGD_GDAT TW ");
        strr.append("            WHERE ");
        strr.append("              TW.YEAR = '" + param._ctrlYear + "' ");
        strr.append("              AND TW.GRADE = '" + param._grade + "' ");
        strr.append("          ) ");
        strr.append(" WHERE ");
        strr.append("     T1.SCHREGNO = ? ");
        strr.append(" GROUP BY ");
        strr.append("     T2.SCHOOL_KIND, ");
        strr.append("     T2.GRADE, ");
        strr.append("     T1.SCHREGNO ");
        strr.append(" ORDER BY ");
        strr.append("     T2.SCHOOL_KIND, ");
        strr.append("     T2.GRADE ");
        strr.append(" ) ");
        strr.append(" SELECT ");
        strr.append("     T1.YEAR, ");
        strr.append("     COUNT(T1.VALUATION) AS CNT, ");
        strr.append("     SUM(T1.VALUATION) AS SCORE ");
        strr.append(" FROM ");
        strr.append("     SCHREG_STUDYREC_DAT T1 ");
        strr.append("     INNER JOIN IGNORE_RYUNENYEAR T3 ");
        strr.append("       ON T3.YEAR = T1.YEAR ");
        strr.append("      AND T3.SCHREGNO = T1.SCHREGNO ");
        strr.append(" WHERE ");
        strr.append("     T1.YEAR <= '" + param._ctrlYear + "' ");
        strr.append("     AND T1.SCHREGNO = ? ");
        strr.append("     AND T1.VALUATION > 0 ");
        strr.append(" GROUP BY ");
        strr.append("     T1.YEAR ");
        strr.append(" ORDER BY ");
        strr.append("     YEAR DESC ");
        final String sqlHyoutei = strr.toString();

        //コース平均
        StringBuffer stbb = new StringBuffer();
        stbb.append(" SELECT ");
        stbb.append("   T1.CLASSCD || '-'|| T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ");
        stbb.append("   T1.AVG ");
        stbb.append(" FROM ");
        stbb.append("   RECORD_AVERAGE_SDIV_DAT T1 ");
        stbb.append("   INNER JOIN SCHREG_REGD_DAT T2 ");
        stbb.append("     ON T2.YEAR = T1.YEAR ");
        stbb.append("    AND T2.SEMESTER = T1.SEMESTER ");
        stbb.append("    AND T2.GRADE = T1.GRADE ");
//        stbb.append("    AND T2.HR_CLASS = T1.HR_CLASS ");  //where句で指定
        stbb.append("    AND T2.COURSECD = T1.COURSECD ");
        stbb.append("    AND T2.MAJORCD = T1.MAJORCD ");
        stbb.append("    AND T2.COURSECODE = T1.COURSECODE ");
        stbb.append("    AND T2.SCHREGNO = ? ");
        stbb.append(" WHERE ");
        stbb.append("   T1.YEAR = '" + param._ctrlYear + "' ");
        stbb.append("   AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV IN ( '" + param._schregSemester + TESTCD_MIDDLE_HYOKA_NO_SEMES + "', '" + param._schregSemester + TESTCD_GAKKI_HYOKA_NO_SEMES + "')  ");
        stbb.append("   AND AVG_DIV = '3' ");
        stbb.append("   AND T1.HR_CLASS = '000' "); //学年ベースで取得
        stbb.append(" ORDER BY ");
        stbb.append("   T1.TESTKINDCD, T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD ");
        final String sqlCourseAvg = stbb.toString();

        StringBuffer stbr = new StringBuffer();
        //該当生徒の合併科目のみの単位を取得(集計)する
        stbr.append(" WITH COMB_CREDITDAT AS ( ");
        stbr.append(" select ");
        stbr.append("   T3.COMBINED_CLASSCD || '-' || T3.COMBINED_SCHOOL_KIND || '-' || T3.COMBINED_CURRICULUM_CD || '-' || T3.COMBINED_SUBCLASSCD AS SUBCLASSCD, ");
        stbr.append("   SUM(T1.CREDITS) AS CREDITS ");
        stbr.append(" FROM ");
        stbr.append("   SCHREG_REGD_DAT T2 ");
        stbr.append("   LEFT JOIN CREDIT_MST T1 ");
        stbr.append("     ON T1.YEAR = T2.YEAR ");
        stbr.append("    AND T1.COURSECD = T2.COURSECD ");
        stbr.append("    AND T1.MAJORCD = T2.MAJORCD ");
        stbr.append("    AND T1.COURSECODE = T2.COURSECODE ");
        stbr.append("    AND T1.GRADE = T2.GRADE ");
        stbr.append("   LEFT JOIN SUBCLASS_REPLACE_COMBINED_DAT T3 ");
        stbr.append("     ON T3.YEAR = T1.YEAR ");
        stbr.append("    AND T3.ATTEND_CLASSCD || '-' || T3.ATTEND_SCHOOL_KIND || '-' || T3.ATTEND_CURRICULUM_CD || '-' || T3.ATTEND_SUBCLASSCD ");
        stbr.append("         = T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD ");
        stbr.append(" WHERE ");
        stbr.append("   T2.YEAR = '" + param._ctrlYear + "' ");
        stbr.append("   AND T2.SEMESTER = '" + param._ctrlSemester + "' ");
        stbr.append("   AND T2.SCHREGNO = ? ");
        stbr.append("   AND T3.COMBINED_CLASSCD || '-' || T3.COMBINED_SCHOOL_KIND || '-' || T3.COMBINED_CURRICULUM_CD || '-' || T3.COMBINED_SUBCLASSCD IS NOT NULL ");
        stbr.append(" GROUP BY ");
        stbr.append("     T3.COMBINED_CLASSCD, ");
        stbr.append("     T3.COMBINED_SCHOOL_KIND, ");
        stbr.append("     T3.COMBINED_CURRICULUM_CD, ");
        stbr.append("     T3.COMBINED_SUBCLASSCD ");
        stbr.append(" ) ");
        stbr.append(" ( ");
        //該当生徒の合併科目以外の単位を取得する
        stbr.append(" SELECT ");
        stbr.append("   T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ");
        stbr.append("   SUM(T1.CREDITS) AS CREDITS ");
        stbr.append(" FROM ");
        stbr.append("   SCHREG_REGD_DAT T2 ");
        stbr.append("   LEFT JOIN CREDIT_MST T1 ");
        stbr.append("     ON T1.YEAR = T2.YEAR ");
        stbr.append("    AND T1.COURSECD = T2.COURSECD ");
        stbr.append("    AND T1.MAJORCD = T2.MAJORCD ");
        stbr.append("    AND T1.COURSECODE = T2.COURSECODE ");
        stbr.append("    AND T1.GRADE = T2.GRADE ");
        stbr.append(" WHERE ");
        stbr.append("   T2.YEAR = '" + param._ctrlYear + "' ");
        stbr.append("   AND T2.SEMESTER = '" + param._ctrlSemester + "' ");
        stbr.append("   AND T2.SCHREGNO = ? ");
        //該当生徒の合併科目に関連する科目を除外
        stbr.append("   AND NOT EXISTS(SELECT ");
        stbr.append("                    'X' ");
        stbr.append("                  FROM ");
        stbr.append("                    COMB_CREDITDAT TW  ");
        stbr.append("                    LEFT JOIN SUBCLASS_REPLACE_COMBINED_DAT TS ");
        stbr.append("                      ON TW.SUBCLASSCD = TS.COMBINED_CLASSCD || '-' || TS.COMBINED_SCHOOL_KIND || '-' || TS.COMBINED_CURRICULUM_CD || '-' || TS.COMBINED_SUBCLASSCD ");
        stbr.append("                     AND TS.YEAR = T1.YEAR ");
        stbr.append("                  WHERE  ");
        stbr.append("                     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD  ");
        stbr.append("                     = TS.ATTEND_CLASSCD || '-' || TS.ATTEND_SCHOOL_KIND || '-' || TS.ATTEND_CURRICULUM_CD || '-' || TS.ATTEND_SUBCLASSCD ");
        stbr.append("       ) ");
        //該当生徒の合併科目を除外
        stbr.append("   AND NOT EXISTS(SELECT ");
        stbr.append("                    'X' ");
        stbr.append("                  FROM ");
        stbr.append("                    COMB_CREDITDAT TW ");
        stbr.append("                  WHERE  ");
        stbr.append("                     TW.SUBCLASSCD = T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD  ");
        stbr.append("       ) ");
        stbr.append(" GROUP BY ");
        stbr.append("     T1.CLASSCD, ");
        stbr.append("     T1.SCHOOL_KIND, ");
        stbr.append("     T1.CURRICULUM_CD, ");
        stbr.append("     T1.SUBCLASSCD ");
        stbr.append(" ) ");
        //該当生徒の合併科目を結合
        stbr.append(" UNION ");
        stbr.append(" SELECT ");
        stbr.append("   * ");
        stbr.append(" FROM ");
        stbr.append("   COMB_CREDITDAT ");

        final String sqlCombCredit = stbr.toString();

        for (final Iterator it = studentMap.values().iterator(); it.hasNext();) {
            final Student student = (Student) it.next();

            //委員会情報取得(指定学期のみ)
            PreparedStatement ps1 = null;
            try {
                ps1 = db2.prepareStatement(sqlCommittee);
                String delim = "";
                String concatStr = "";
                for (final Iterator ite = KnjDbUtils.query(db2, ps1, new Object[] {student._schregno}).iterator();ite.hasNext();) {
                    final Map row = (Map) ite.next();
                    final String semester = KnjDbUtils.getString(row, "SEMESTER");
                    //final String committeeFlg = KnjDbUtils.getString(row, "COMMITTEE_FLG");
                    final String committeeName = KnjDbUtils.getString(row, "COMMITTEENAME");
                    final String kStr = semester;
                    if (!student._committeeMap.containsKey(kStr)) {
                        concatStr = new String();
                        delim = "";
                    } else {
                        concatStr = (String)student._committeeMap.get(kStr);
                    }
                    if (!"".equals(StringUtils.defaultString(committeeName, ""))) {
                        concatStr += delim + committeeName;
                        student._committeeMap.put(kStr, concatStr);
                        delim = "・";
                    }
                }
                DbUtils.closeQuietly(ps1);
                db2.commit();

            } catch (Exception e){
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(ps1);
                db2.commit();
            }

            //部活情報取得(指定学期に在籍した部活のみ)
            PreparedStatement ps2 = null;
            try {
                ps2 = db2.prepareStatement(sqlClub);
                String concatStr = "";
                String delim = "";
                concatStr = "";
                for (final Iterator ite = KnjDbUtils.query(db2, ps2, new Object[] {student._schregno}).iterator();ite.hasNext();) {
                    final Map row = (Map) ite.next();
                    final String clubName = KnjDbUtils.getString(row, "CLUBNAME");
                    if (!"".equals(StringUtils.defaultString(clubName, ""))) {
                        concatStr += delim + clubName;
                        delim = "・";
                    }
                }
                student._someClubName = concatStr;
            } catch (Exception e){
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(ps2);
                db2.commit();
            }

            //欠席理由
            PreparedStatement ps3 = null;
            try {
                ps3 = db2.prepareStatement(sqlSemesRemark);
                final Map remarkMap = new LinkedMap();
                for (final Iterator rit = KnjDbUtils.query(db2, ps3, new Object[] {student._schregno}).iterator(); rit.hasNext();) {
                    final Map row = (Map) rit.next();
                    final String semester = KnjDbUtils.getString(row, "SEMESTER");
                    final String remark1 = KnjDbUtils.getString(row, "REMARK1");

                    String remark = (String) remarkMap.get(semester);
                    if (null == remark) {
                        remark = "";
                    } else if (!StringUtils.isBlank(remark)) {
                        remark += "  ";
                    }
                    remarkMap.put(semester, remark + remark1);
                }

                student._semesRemarkMap = new LinkedMap(remarkMap);
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(ps3);
                db2.commit();
            }

            //x年次評定(評定平均。学年末指定時以外は出さない。)
            PreparedStatement ps4 = null;
            try {
                if (param._isOutputDebug) {
                    log.info(" sqlHyoutei = " + sqlHyoutei);
                }
                  ps4 = db2.prepareStatement(sqlHyoutei);
                for (final Iterator rit = KnjDbUtils.query(db2, ps4, new Object[] {student._schregno, student._schregno}).iterator(); rit.hasNext();) {
                    final Map row = (Map) rit.next();
                    final Integer score = KnjDbUtils.getInt(row, "SCORE", new Integer(0));
                    final Integer cnt = KnjDbUtils.getInt(row, "CNT", new Integer(0));
                    final String yy = KnjDbUtils.getString(row, "YEAR");
                    student._avgMap.put(yy, new LatestHyoutei(yy, score.intValue(), cnt.intValue()));
                }
               } catch (Exception e) {
                log.error("exception!", e);
               } finally {
                DbUtils.closeQuietly(ps4);
                db2.commit();
               }

            //コース平均
            PreparedStatement ps5 = null;
            try {
                  ps5 = db2.prepareStatement(sqlCourseAvg);
                for (final Iterator rit = KnjDbUtils.query(db2, ps5, new Object[] {student._schregno}).iterator(); rit.hasNext();) {
                    final Map row = (Map) rit.next();
                    final String avg = KnjDbUtils.getString(row, "AVG");
                    final String subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");
                    student._courseAvgMap.put(subclasscd, avg);
                }
               } catch (Exception e) {
                log.error("exception!", e);
               } finally {
                DbUtils.closeQuietly(ps5);
                db2.commit();
               }
            //コース平均
            PreparedStatement ps6 = null;
            try {
                  ps6 = db2.prepareStatement(sqlCombCredit);
                for (final Iterator rit = KnjDbUtils.query(db2, ps6, new Object[] {student._schregno, student._schregno}).iterator(); rit.hasNext();) {
                    final Map row = (Map) rit.next();
                    final String credits = KnjDbUtils.getString(row, "CREDITS");
                    final String subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");
                    student._subclsCreditMap.put(subclasscd, credits);
                }
               } catch (Exception e) {
                log.error("exception!", e);
               } finally {
                DbUtils.closeQuietly(ps6);
                db2.commit();
               }
        }
    }

    private static String sishaGonyu(final String val) {
        if (!NumberUtils.isNumber(val)) {
            return null;
        }
        return new BigDecimal(val).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
    }

    /**
     * 生徒
     */
    private static class Student {
        final String _schregno;
        final String _name;
        final String _hrName;
        final String _staffName;
        final String _grade;
        final String _gradeName1;
        final String _gradeName2;
        final String _hrClass;
        final String _coursecd;
        final String _majorcd;
        final String _coursecode;
        final String _course;
        final String _majorname;
        final String _attendno;
        final String _hrClassName1;
        final Map _attendMap;
        final Map _subclassMap;
        private String _coursecodeAbbv;
        private String _coursecodeName;
        private Map _semesterHreportremarkDatMap;
        private Map _semesRemarkMap;
        private Map _committeeMap;
        private String _someClubName;
        private Map _avgMap;
        private Map _tbl5InfoMap;
        private Map _courseAvgMap;
        private Map _subclsCreditMap;

        Student(final String schregno, final String name, final String hrName, final String staffName, final String attendno, final String grade, final String gradeName1,
                final String gradeName2, final String hrClass, final String coursecd, final String majorcd, final String coursecode, final String course, final String majorname,
                final String hrClassName1
                ) {
            _schregno = schregno;
            _name = name;
            _hrName = hrName;
            _staffName = staffName;
            _attendno = attendno;
            _grade = grade;
            _gradeName1 = gradeName1;
            _gradeName2 = gradeName2;
            _hrClass = hrClass;
            _coursecd = coursecd;
            _majorcd = majorcd;
            _coursecode = coursecode;
            _course = course;
            _majorname = majorname;
            _hrClassName1 = hrClassName1;
            _attendMap = new TreeMap();
            _subclassMap = new TreeMap();
            _semesterHreportremarkDatMap = new HashMap();
            _semesRemarkMap = new LinkedMap();
            _committeeMap = new LinkedMap();
            _someClubName = "";
            _avgMap = new LinkedMap();
            _tbl5InfoMap = new LinkedMap();
            _courseAvgMap = new LinkedMap();
            _subclsCreditMap = new LinkedMap();
        }

        Subclass getSubClass(final String subclasscd) {
            if (null == _subclassMap.get(subclasscd)) {
                String classcd = null;
                if (null != subclasscd) {
                    final String[] split = StringUtils.split(subclasscd, "-");
                    if (null != split && split.length > 2) {
                        classcd = split[0] + "-" + split[1];
                    } else {
                        classcd = subclasscd;
                    }
                }
                return new Subclass(new SubclassMst(classcd, subclasscd, null, null, null, null, new Integer(99999), new Integer(99999)));
            }
            return (Subclass) _subclassMap.get(subclasscd);
        }

        /**
         * 生徒を取得
         */
        private static List getStudentList(final DB2UDB db2, final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append("     SELECT  REGD.SCHREGNO");
            stb.append("            ,REGD.SEMESTER ");
            stb.append("            ,BASE.NAME ");
            stb.append("            ,REGDH.HR_NAME ");
            stb.append("            ,W8.STAFFNAME ");
            stb.append("            ,REGD.ATTENDNO ");
            stb.append("            ,REGD.GRADE ");
            stb.append("            ,GDAT.GRADE_NAME1");
            stb.append("            ,GDAT.GRADE_NAME2");
            stb.append("            ,REGD.HR_CLASS ");
            stb.append("            ,REGD.COURSECD ");
            stb.append("            ,REGD.MAJORCD ");
            stb.append("            ,REGD.COURSECODE ");
            stb.append("            ,REGD.COURSECD || REGD.MAJORCD || REGD.COURSECODE AS COURSE");
            stb.append("            ,W9.MAJORNAME ");
            stb.append("            ,W10.COURSECODENAME ");
            stb.append("            ,W10.COURSECODEABBV1 ");
            stb.append("            ,REGDH.HR_CLASS_NAME1 ");
            stb.append("            ,CASE WHEN W3.SCHREGNO IS NOT NULL THEN 1 ");
            stb.append("                  WHEN W4.SCHREGNO IS NOT NULL THEN 1 ");
            stb.append("                  WHEN W5.SCHREGNO IS NOT NULL THEN 1 ");
            stb.append("                  ELSE 0 END AS LEAVE ");
            stb.append("     FROM    SCHREG_REGD_DAT REGD ");
            stb.append("     INNER JOIN V_SEMESTER_GRADE_MST W2 ON W2.YEAR = '" + param._ctrlYear + "' AND W2.SEMESTER = REGD.SEMESTER AND W2.GRADE = '" + param._grade + "' ");
            //               転学・退学者で、異動日が[学期終了日または出欠集計日の小さい日付]より小さい日付の場合
            stb.append("     LEFT JOIN SCHREG_BASE_MST W3 ON W3.SCHREGNO = REGD.SCHREGNO ");
            stb.append("                  AND W3.GRD_DIV IN('2','3') ");
            stb.append("                  AND W3.GRD_DATE < CASE WHEN W2.EDATE < '" + param._date + "' THEN W2.EDATE ELSE '" + param._date + "' END ");
            //               転入・編入者で、異動日が[学期終了日または出欠集計日の小さい日付]より大きい日付の場合
            stb.append("     LEFT JOIN SCHREG_BASE_MST W4 ON W4.SCHREGNO = REGD.SCHREGNO ");
            stb.append("                  AND W4.ENT_DIV IN('4','5') ");
            stb.append("                  AND W4.ENT_DATE > CASE WHEN W2.EDATE < '" + param._date + "' THEN W2.EDATE ELSE '" + param._date + "' END ");
            //               留学・休学者で、[学期終了日または出欠集計日の小さい日付]が留学・休学期間内にある場合
            stb.append("     LEFT JOIN SCHREG_TRANSFER_DAT W5 ON W5.SCHREGNO = REGD.SCHREGNO ");
            stb.append("                  AND W5.TRANSFERCD IN ('1','2') ");
            stb.append("                  AND CASE WHEN W2.EDATE < '" + param._date + "' THEN W2.EDATE ELSE '" + param._date + "' END BETWEEN W5.TRANSFER_SDATE AND W5.TRANSFER_EDATE ");
            stb.append("     LEFT JOIN SCHREG_REGD_HDAT REGDH ON REGDH.YEAR = REGD.YEAR ");
            stb.append("                  AND REGDH.SEMESTER = REGD.SEMESTER ");
            stb.append("                  AND REGDH.GRADE = REGD.GRADE ");
            stb.append("                  AND REGDH.HR_CLASS = REGD.HR_CLASS ");
            stb.append("     LEFT JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ");
            stb.append("     LEFT JOIN STAFF_MST W8 ON W8.STAFFCD = REGDH.TR_CD1 ");
            stb.append("     LEFT JOIN MAJOR_MST W9 ON W9.COURSECD = REGD.COURSECD AND W9.MAJORCD = REGD.MAJORCD ");
            stb.append("     LEFT JOIN COURSECODE_MST W10 ON W10.COURSECODE = REGD.COURSECODE ");
            stb.append("     LEFT JOIN SCHREG_REGD_GDAT GDAT ");
            stb.append("            ON GDAT.YEAR  = REGD.YEAR ");
            stb.append("           AND GDAT.GRADE = REGD.GRADE ");
            stb.append("     WHERE   REGD.YEAR = '" + param._ctrlYear + "' ");
            if (SEMEALL.equals(param._semester)) {
                stb.append("     AND REGD.SEMESTER = '" + param._ctrlSemester + "' ");
            } else {
                stb.append("     AND REGD.SEMESTER = '" + param._semester + "' ");
                stb.append("     AND NOT EXISTS(SELECT 'X'  FROM  SCHREG_BASE_MST S1");
                stb.append("                    WHERE S1.SCHREGNO = REGD.SCHREGNO AND S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < W2.SDATE) ");
            }
            if ("1".equals(param._disp)) {
                stb.append("         AND REGD.GRADE || REGD.HR_CLASS IN " + SQLUtils.whereIn(true, param._categorySelected));
            } else {
                stb.append("         AND REGD.GRADE || REGD.HR_CLASS = '" + param._gradeHrclass + "' ");
                stb.append("         AND REGD.SCHREGNO IN " + SQLUtils.whereIn(true, param._categorySelected));
            }
            stb.append("     ORDER BY ");
            stb.append("         REGD.GRADE, ");
            stb.append("         REGD.HR_CLASS, ");
            stb.append("         REGD.ATTENDNO ");
            final String sql = stb.toString();
            log.debug(" student sql = " + sql);

            final List students = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    //final String attendno = NumberUtils.isDigits(rs.getString("ATTENDNO")) ? String.valueOf(Integer.parseInt(rs.getString("ATTENDNO"))) : rs.getString("ATTENDNO");
                    final String staffname = StringUtils.defaultString(rs.getString("STAFFNAME"));
                    final Student student = new Student(rs.getString("SCHREGNO"), rs.getString("NAME"), rs.getString("HR_NAME"), staffname, rs.getString("ATTENDNO"), rs.getString("GRADE"), rs.getString("GRADE_NAME1"), rs.getString("GRADE_NAME2"), rs.getString("HR_CLASS"), rs.getString("COURSECD"), rs.getString("MAJORCD"), rs.getString("COURSECODE"), rs.getString("COURSE"), rs.getString("MAJORNAME"), rs.getString("HR_CLASS_NAME1"));
                    student._coursecodeAbbv = rs.getString("COURSECODEABBV1");
                    student._coursecodeName = rs.getString("COURSECODENAME");

                    students.add(student);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }

            return students;
        }

        private String getAvgOneYear(Param param) {
            String retStr = "";
            if (_avgMap.size() > 0) {
                LatestHyoutei avgwk = (LatestHyoutei)_avgMap.get(param._ctrlYear);
                if (avgwk != null) {
                    BigDecimal bg = new BigDecimal(avgwk._score);
                    retStr = bg.divide(new BigDecimal(avgwk._cnt), 1, BigDecimal.ROUND_HALF_UP).toString();
                }
            }
            if (param._isOutputDebug) {
                log.info(" student " + _schregno + ", year = " + param._ctrlYear + ", avgwk = " + _avgMap.get(param._ctrlYear) + ", avg = " + retStr);
            }
            return retStr;
        }
        private String getAvgAllYear(Param param) {
            String retStr = "";
            int _score = 0;
            int _cnt = 0;

            if (_avgMap.size() > 0) {
                for (Iterator ite = _avgMap.keySet().iterator();ite.hasNext();) {
                    String kStr = (String)ite.next();
                    final LatestHyoutei avgwk = (LatestHyoutei)_avgMap.get(kStr);
                    if (avgwk != null) {
                        _score += avgwk._score;
                        _cnt += avgwk._cnt;
                    }
                }
                BigDecimal bg = new BigDecimal(_score);
                retStr = bg.divide(new BigDecimal(_cnt), 1, BigDecimal.ROUND_HALF_UP).toString();
            }
            if (param._isOutputDebug) {
                log.info(" student " + _schregno + ", avgMap = " + _avgMap + " score = " + _score + ", count = " + _cnt + ", avg = " + retStr);
            }

            return retStr;
        }
    }

    private static class Attendance {

        final int _lesson;
        final int _mLesson;
        final int _suspend;
        final int _mourning;
        final int _absent;
        final int _present;
        final int _late;
        final int _early;
        final int _kekkaDCnt;
        Attendance(
                final int lesson,
                final int mLesson,
                final int suspend,
                final int mourning,
                final int absent,
                final int present,
                final int late,
                final int early,
                final int kekkaDCnt
        ) {
            _lesson = lesson;
            _mLesson = mLesson;
            _suspend = suspend;
            _mourning = mourning;
            _absent = absent;
            _present = present;
            _late = late;
            _early = early;
            _kekkaDCnt = kekkaDCnt;
        }

        private static void load(
                final DB2UDB db2,
                final Param param,
                final Map studentMap,
                final DateRange dateRange
        ) {
            log.info(" attendance = " + dateRange);
            if (null == dateRange || null == dateRange._sdate || null == dateRange._edate || dateRange._sdate.compareTo(param._date) > 0) {
                return;
            }
            final String edate = dateRange._edate.compareTo(param._date) > 0 ? param._date : dateRange._edate;

            //対象生徒の欠課日数について、(生徒別学期別に)事前に取得しておく。
            Map absentSemesMap = getAbsentSemes(db2, param, studentMap, dateRange);

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                param._attendParamMap.put("schregno", "?");

                final String sql = AttendAccumulate.getAttendSemesSql(
                        param._ctrlYear,
                        param._semester,
                        dateRange._sdate,
                        edate,
                        param._attendParamMap
                );
                log.debug(" attend sql = " + sql);
                ps = db2.prepareStatement(sql);

                for (final Iterator it = studentMap.values().iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();

                    while (rs.next()) {
                        String kekkaDCnt = "0";
                        if (student._schregno != null) {
                            Map absSemeMap = (Map)absentSemesMap.get(student._schregno);
                            if (absSemeMap != null && absSemeMap.size() > 0) {
                                final String kekkaDCntWk = (String)absSemeMap.get(rs.getString("SEMESTER"));
                                if (kekkaDCntWk != null) {
                                    kekkaDCnt = kekkaDCntWk;
                                }
                            }
                        }
                        final Attendance attendance = new Attendance(
                                rs.getInt("LESSON"),
                                rs.getInt("MLESSON"),
                                rs.getInt("SUSPEND"),
                                rs.getInt("MOURNING"),
                                rs.getInt("SICK"),
                                rs.getInt("PRESENT"),
                                rs.getInt("LATE"),
                                rs.getInt("EARLY"),
                                Integer.parseInt(kekkaDCnt)
                        );
                        student._attendMap.put(rs.getString("SEMESTER"), attendance);
                    }
                    DbUtils.closeQuietly(rs);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }
        private static Map getAbsentSemes(
                final DB2UDB db2,
                final Param param,
                final Map studentMap,
                final DateRange dateRange
        ) {
            Map retMap = new HashMap();

            if (studentMap.size() == 0) return retMap;
            final String sql = getAbsentSemesSql(param, studentMap);
            log.debug(" absencesemes sql = " + sql);
            PreparedStatement ps = null;
            ResultSet rs = null;
            Map addMap = new HashMap();;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    final String keyStr = rs.getString("SCHREGNO");
                    if (!retMap.containsKey(keyStr)) {
                        addMap = new HashMap();
                        retMap.put(keyStr, addMap);
                    }
                    addMap.put(rs.getString("SEMESTER"), rs.getString("KEKKA_DCNT"));
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
            return retMap;
        }
        private static String getAbsentSemesSql(
            final Param param,
            final Map studentMap
        ) {
            String schregInstate = "";
            String delim = "";
            for (Iterator its = studentMap.keySet().iterator();its.hasNext();) {
                final String schregno = (String)its.next();
                schregInstate += delim + "'" + schregno + "'";
                delim = ", ";
            }
            StringBuffer stb = new StringBuffer();
            stb.append(" WITH GETABSENTSEMES_BASE AS ( ");
            stb.append(" SELECT ");
            stb.append("  SCHREGNO, ");
            stb.append("  SEMESTER, ");
            stb.append("  SUM(CNT) AS KEKKA_DCNT ");
            stb.append(" FROM ");
            stb.append("  ATTEND_SEMES_DETAIL_DAT ");
            stb.append(" WHERE ");
            stb.append("  COPYCD = '0' ");
            stb.append("  AND YEAR = '" + param._ctrlYear + "' ");
            stb.append("  AND SEMESTER <= '" + param._semester + "' ");
            stb.append("  AND SCHREGNO IN (" + schregInstate + ") ");
            String[] cutDate = StringUtils.split(param._date, '-');
            if (cutDate.length > 2) {
                if (cutDate[0] == param._ctrlYear) {
                    stb.append("  AND MONTH BETWEEN '04' AND '" + cutDate[1] + "' ");
                } else if (Integer.parseInt(cutDate[0]) == Integer.parseInt(param._ctrlYear) + 1) {
                    stb.append("  AND ('04' <= MONTH OR MONTH <= '" + cutDate[1] + "') ");
                }
            }
            stb.append("  AND SEQ = '102' ");
            stb.append(" GROUP BY ");
            stb.append("  SCHREGNO, ");
            stb.append("  SEMESTER ");
            stb.append(" ORDER BY ");
            stb.append("  SCHREGNO, ");
            stb.append("  SEMESTER ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("   * ");
            stb.append(" FROM ");
            stb.append("   GETABSENTSEMES_BASE ");
            stb.append(" UNION ");
            stb.append(" SELECT ");
            stb.append("   SCHREGNO, ");
            stb.append("   '9' AS SEMESTER, ");
            stb.append("   SUM(KEKKA_DCNT) AS KEKKA_DCNT ");
            stb.append(" FROM ");
            stb.append("   GETABSENTSEMES_BASE ");
            stb.append(" GROUP BY ");
            stb.append("   SCHREGNO ");
            return stb.toString();
        }
    }

    /**
     * 科目別欠課時数
     */
    private void gethrAttendSemesInfo(final DB2UDB db2, final Param param, final Map studentMap) {
        List addLst = new ArrayList();
        for (final Iterator rit = param._attendRanges.values().iterator(); rit.hasNext();) {
            final DateRange range = (DateRange) rit.next();
            param._attendParamMap.put("schregno", "?");
            param._attendParamMap.put("grade", "?");
            if (!"".equals(param._gradeHrclass)) {
                param._attendParamMap.put("hrClass", "?");
            }
            final String edate = range._edate.compareTo(param._date) > 0 ? param._date : range._edate;

            PreparedStatement psAtSeme = null;
            ResultSet rsAtSeme = null;
            try {
                final String sql = AttendAccumulate.getAttendSubclassSql(
                        param._ctrlYear,
                        range._key,
                        range._sdate,
                        edate,
                        param._attendParamMap
                );
                psAtSeme = db2.prepareStatement(sql);

                for (final Iterator it = studentMap.keySet().iterator(); it.hasNext();) {
                    final String schregNo = (String)it.next();
                    final Student student = (Student)studentMap.get(schregNo);

                    psAtSeme.setString(1, schregNo);
                    psAtSeme.setString(2, param._grade);
                    psAtSeme.setString(3, student._hrClass);
                    rsAtSeme = psAtSeme.executeQuery();

                    while (rsAtSeme.next()) {
                        if (!SEMEALL.equals(rsAtSeme.getString("SEMESTER"))) {
                            continue;
                        }
                        final String subclasscd = rsAtSeme.getString("SUBCLASSCD");
                        final BigDecimal lesson = rsAtSeme.getBigDecimal("MLESSON");
                        final BigDecimal rawReplacedSick = rsAtSeme.getBigDecimal("RAW_REPLACED_SICK");
                        final BigDecimal rawSick = rsAtSeme.getBigDecimal("SICK1");
                        final BigDecimal sick1 = "1".equals(rsAtSeme.getString("IS_COMBINED_SUBCLASS")) ? rawReplacedSick : rawSick;
                        final BigDecimal attend = lesson.subtract(null == sick1 ? BigDecimal.valueOf(0) : sick1);
                        final BigDecimal sick = rsAtSeme.getBigDecimal("SICK2");
                        final BigDecimal replacedSick = rsAtSeme.getBigDecimal("REPLACED_SICK");
                        final BigDecimal sick2 = "1".equals(rsAtSeme.getString("IS_COMBINED_SUBCLASS")) ? replacedSick : sick;  //欠課時数
                        final BigDecimal late = rsAtSeme.getBigDecimal("LATE");
                        final BigDecimal early = rsAtSeme.getBigDecimal("EARLY");
                        final BigDecimal credits = rsAtSeme.getBigDecimal("CREDITS");
                        //MAX学期指定以外なら(単位数/3)<欠課時数、つまり1/3学期分として判定。MAX学期(or"9"学期)なら(単位数/1)<欠課時数で判定
                        final String name = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT NAME FROM SCHREG_BASE_MST WHERE SCHREGNO = '" + schregNo + "'"));
                        final String subclsNameAbbv = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT SUBCLASSABBV FROM SUBCLASS_MST WHERE CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '" + subclasscd + "' "));
                        final String useSemester = range._key;
                        final String courseCode = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT COURSECODE FROM SCHREG_REGD_DAT WHERE YEAR = '" + param._ctrlYear + "' AND SEMESTER = '" + useSemester + "' AND SCHREGNO = '" + schregNo + "' "));
                        final String courseNameAbbv = !"".equals(StringUtils.defaultString(courseCode, "")) ? KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT COURSECODEABBV1 FROM COURSECODE_MST WHERE COURSECODE = '" + courseCode + "' ")) : "";
                        final tbl5Info addwk = new tbl5Info(useSemester, schregNo, name, subclasscd, StringUtils.defaultString(subclsNameAbbv, ""), StringUtils.defaultString(courseNameAbbv, ""), lesson, attend, sick2, late, early, credits);
                        if (!student._tbl5InfoMap.containsKey(subclasscd)) {
                            addLst = new ArrayList();
                            student._tbl5InfoMap.put(subclasscd, addLst);
                        } else {
                            addLst = (List)student._tbl5InfoMap.get(subclasscd);
                        }
                        addLst.add(addwk);
                     }
                }
                DbUtils.closeQuietly(rsAtSeme);
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(psAtSeme);
                db2.commit();
            }
        }
    }

    private static class tbl5Info {
        final String _semester;
        final String _schregNo;
        final String _name;
        final String _subclassCd;
        final String _subclassNameAbbv;
        final String _courseNameAbbv;
        final BigDecimal _lesson;
        final BigDecimal _attend;
        final BigDecimal _sick;
        final BigDecimal _late;
        final BigDecimal _early;
        final BigDecimal _credits;

        public tbl5Info(
                         final String semester,
                         final String schregNo,
                         final String name,
                         final String subclassCd,
                         final String subclassNameAbbv,
                         final String courseNameAbbv,
                         final BigDecimal lesson,
                         final BigDecimal attend,
                         final BigDecimal sick,
                         final BigDecimal late,
                         final BigDecimal early,
                         final BigDecimal credits
                         ) {
            _semester = semester;
            _schregNo = schregNo;
            _name = name;
            _subclassCd = subclassCd;
            _subclassNameAbbv = subclassNameAbbv;
            _courseNameAbbv = courseNameAbbv;
            _lesson = lesson;
            _attend = attend;
            _sick = sick;
            _late = late;
            _early = early;
            _credits = credits;
        }
    }

    /**
     * 科目
     */
    private static class Subclass implements Comparable {
        final SubclassMst _mst;
        final Map _scoreMap;
        Subclass _sakiSubclass;

        Subclass(
                final SubclassMst mst
        ) {
            _mst = mst;
            _scoreMap = new TreeMap();
        }

        public Score getScore(final String testcd) {
            if (null == testcd || _scoreMap == null|| null == (Score)_scoreMap.get(testcd)) {
                return Score.nullScore;
            }
            return (Score) _scoreMap.get(testcd);
        }

        public int compareTo(final Object o) {
            final Subclass subclass = (Subclass) o;
            return _mst.compareTo(subclass._mst);
        }
    }

    /**
     * 成績
     */
    private static class Score {

        static final Rank nullRank = new Rank(null, null, null, null, null);
        static final Score nullScore = new Score(null, null, null, nullRank, nullRank, nullRank, nullRank, null, null, null, null, null, null);

        final String _score;
        final String _avg;
        final Rank _gradeRank;
        final Rank _hrRank;
        final Rank _courseRank;
        final Rank _majorRank;
        final String _karihyotei;
        final String _replacemoto;
        final String _credits;
        final String _stdScore;

        Score(
                final String score,
                final String assessLevel,
                final String avg,
                final Rank gradeRank,
                final Rank hrRank,
                final Rank courseRank,
                final Rank majorRank,
                final String karihyotei,
                final String replacemoto,
                final String compCredit,
                final String getCredit,
                final String stdScore,
                final String credits
        ) {
            _score = score;
            _avg = avg;
            _gradeRank = gradeRank;
            _hrRank = hrRank;
            _courseRank = courseRank;
            _majorRank = majorRank;
            _replacemoto = replacemoto;
            _karihyotei = karihyotei;
            _credits = credits;
            _stdScore = stdScore;
        }

//        /**
//         * @return 合併元科目はFalseを、以外はTrueを戻します。
//         */
//        private boolean enableCredit() {
//            if (NumberUtils.isDigits(_replacemoto) && Integer.parseInt(_replacemoto) >= 1) {
//                return false;
//            }
//            return true;
//        }


        public String toString() {
            return "Score(" + _score + ")";
        }

        private static void load(
                final DB2UDB db2,
                final Param param,
                final Map studentMap,
                final StringBuffer stbtestcd
        ) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = sqlScore(param, stbtestcd);
                log.debug(" subclass query start. sql:"+sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                log.debug(" subclass query end.");

                while (rs.next()) {
                    final Student student = (Student) studentMap.get(rs.getString("SCHREGNO"));
                    if (null == student) {
                        continue;
                    }
                    final String testcd = StringUtils.defaultString(rs.getString("TESTCD"), "");

                    final Rank gradeRank = new Rank(rs.getString("GRADE_RANK"), rs.getString("GRADE_AVG_RANK"), rs.getString("GRADE_COUNT"), rs.getString("GRADE_AVG"), rs.getString("GRADE_HIGHSCORE"));
                    final Rank hrRank = new Rank(rs.getString("CLASS_RANK"), rs.getString("CLASS_AVG_RANK"), rs.getString("HR_COUNT"), rs.getString("HR_AVG"), rs.getString("HR_HIGHSCORE"));
                    final Rank courseRank = new Rank(rs.getString("COURSE_RANK"), rs.getString("COURSE_AVG_RANK"), rs.getString("COURSE_COUNT"), rs.getString("COURSE_AVG"), rs.getString("COURSE_HIGHSCORE"));
                    final Rank majorRank = new Rank(rs.getString("MAJOR_RANK"), rs.getString("MAJOR_AVG_RANK"), rs.getString("MAJOR_COUNT"), rs.getString("MAJOR_AVG"), rs.getString("MAJOR_HIGHSCORE"));

                    String scoreString = rs.getString("SCORE");
                    if ("*".equals(rs.getString("VALUE_DI"))) {
                        if (!("99".equals(rs.getString("TESTKINDCD")) && "00".equals(rs.getString("TESTITEMCD")))) {
                            scoreString = "欠";
                        }
                    }

                    final Score score = new Score(
                            scoreString,
                            null, // rs.getString("ASSESS_LEVEL"),
                            rs.getString("AVG"),
                            gradeRank,
                            hrRank,
                            courseRank,
                            majorRank,
                            null, // rs.getString("KARI_HYOUTEI"),
                            rs.getString("REPLACEMOTO"),
                            rs.getString("COMP_CREDIT"),
                            rs.getString("GET_CREDIT"),
                            rs.getString("STANDARD_SCORE"),
                            rs.getString("CREDITS")
                    );

                    final String subclasscd;
                    if (SUBCLASSCD999999.equals(StringUtils.split(rs.getString("SUBCLASSCD"), "-")[3])) {
                        subclasscd = SUBCLASSCD999999;
                    } else {
                        subclasscd = rs.getString("SUBCLASSCD");
                    }
                    if (null == student._subclassMap.get(subclasscd)) {
                        final Subclass subClass = new Subclass(param.getSubclassMst(subclasscd));
                        student._subclassMap.put(subclasscd, subClass);
                    }
//                    if (null == testcd) {
//                        continue;
//                    }
                    // log.debug(" schregno = " + student._schregno + " : " + testcd + " : " + rs.getString("SUBCLASSCD") + " = " + rs.getString("SCORE"));
                    final Subclass subClass = student.getSubClass(subclasscd);
                    subClass._scoreMap.put(testcd, score);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private static String sqlScore(final Param param, final StringBuffer stbtestcd) {
            final StringBuffer stb = new StringBuffer();
            stb.append("WITH ");

            //対象生徒の表 クラスの生徒
            stb.append(" SCHNO_A AS(");
            stb.append("     SELECT  W1.SCHREGNO,W1.YEAR,W1.SEMESTER ");
            stb.append("            ,W1.GRADE, W1.HR_CLASS, W1.COURSECD, W1.MAJORCD, W1.COURSECODE ");
            stb.append("     FROM    SCHREG_REGD_DAT W1 ");

            stb.append("     WHERE   W1.YEAR = '" + param._ctrlYear + "' ");
            if (SEMEALL.equals(param._semester)) {
                stb.append("     AND W1.SEMESTER = '" + param._ctrlSemester + "' ");
            } else {
                stb.append("     AND W1.SEMESTER = '" + param._semester + "' ");
            }

            if ("1".equals(param._disp)) {
                stb.append("         AND W1.GRADE || W1.HR_CLASS IN " + SQLUtils.whereIn(true, param._categorySelected));
            } else {
                stb.append("         AND W1.GRADE || W1.HR_CLASS = '" + param._gradeHrclass + "' ");
            }
            stb.append(") ");

            //成績データの表（通常科目）
            stb.append(",RECORD_REC AS(");
            stb.append("    SELECT ");
            stb.append("     W3.SCHREGNO ");
            stb.append("    ,W3.SEMESTER ");
            stb.append("    ,W3.TESTKINDCD ");
            stb.append("    ,W3.TESTITEMCD ");
            stb.append("    ,W3.SCORE_DIV ");
            stb.append("    ,W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || W3.SUBCLASSCD AS SUBCLASSCD ");
            stb.append("    ,W3.SCORE ");
            stb.append("    ,W3.AVG ");
            stb.append("    ,W3.GRADE_RANK ");
            stb.append("    ,W3.GRADE_AVG_RANK ");
            stb.append("    ,W3.CLASS_RANK ");
            stb.append("    ,W3.CLASS_AVG_RANK ");
            stb.append("    ,W3.COURSE_RANK ");
            stb.append("    ,W3.COURSE_AVG_RANK ");
            stb.append("    ,W3.MAJOR_RANK ");
            stb.append("    ,W3.MAJOR_AVG_RANK ");
            stb.append("    ,T_AVG1.AVG AS GRADE_AVG ");
            stb.append("    ,T_AVG1.COUNT AS GRADE_COUNT ");
            stb.append("    ,T_AVG1.HIGHSCORE AS GRADE_HIGHSCORE ");
            stb.append("    ,T_AVG2.AVG AS HR_AVG ");
            stb.append("    ,T_AVG2.COUNT AS HR_COUNT ");
            stb.append("    ,T_AVG2.HIGHSCORE AS HR_HIGHSCORE ");
            stb.append("    ,T_AVG3.AVG AS COURSE_AVG ");
            stb.append("    ,T_AVG3.COUNT AS COURSE_COUNT ");
            stb.append("    ,T_AVG3.HIGHSCORE AS COURSE_HIGHSCORE ");
            stb.append("    ,T_AVG4.AVG AS MAJOR_AVG ");
            stb.append("    ,T_AVG4.COUNT AS MAJOR_COUNT ");
            stb.append("    ,T_AVG4.HIGHSCORE AS MAJOR_HIGHSCORE ");
            stb.append("    FROM ");
            stb.append("      RECORD_RANK_SDIV_DAT W3 ");
            stb.append("      INNER JOIN SCHNO_A W1 ON W3.SCHREGNO = W1.SCHREGNO ");
            stb.append("      LEFT JOIN RECORD_AVERAGE_SDIV_DAT T_AVG1 ON T_AVG1.YEAR = W3.YEAR AND T_AVG1.SEMESTER = W3.SEMESTER AND T_AVG1.TESTKINDCD = W3.TESTKINDCD AND T_AVG1.TESTITEMCD = W3.TESTITEMCD AND T_AVG1.SCORE_DIV = W3.SCORE_DIV AND T_AVG1.GRADE = '" + param._grade + "' AND T_AVG1.CLASSCD = W3.CLASSCD AND T_AVG1.SCHOOL_KIND = W3.SCHOOL_KIND AND T_AVG1.CURRICULUM_CD = W3.CURRICULUM_CD AND T_AVG1.SUBCLASSCD = W3.SUBCLASSCD ");
            stb.append("          AND T_AVG1.AVG_DIV = '1' ");
            stb.append("      LEFT JOIN RECORD_AVERAGE_SDIV_DAT T_AVG2 ON T_AVG2.YEAR = W3.YEAR AND T_AVG2.SEMESTER = W3.SEMESTER AND T_AVG2.TESTKINDCD = W3.TESTKINDCD AND T_AVG2.TESTITEMCD = W3.TESTITEMCD AND T_AVG2.SCORE_DIV = W3.SCORE_DIV AND T_AVG2.GRADE = '" + param._grade + "' AND T_AVG2.CLASSCD = W3.CLASSCD AND T_AVG2.SCHOOL_KIND = W3.SCHOOL_KIND AND T_AVG2.CURRICULUM_CD = W3.CURRICULUM_CD AND T_AVG2.SUBCLASSCD = W3.SUBCLASSCD ");
            stb.append("          AND T_AVG2.AVG_DIV = '2' AND T_AVG2.HR_CLASS = W1.HR_CLASS ");
            stb.append("      LEFT JOIN RECORD_AVERAGE_SDIV_DAT T_AVG3 ON T_AVG3.YEAR = W3.YEAR AND T_AVG3.SEMESTER = W3.SEMESTER AND T_AVG3.TESTKINDCD = W3.TESTKINDCD AND T_AVG3.TESTITEMCD = W3.TESTITEMCD AND T_AVG3.SCORE_DIV = W3.SCORE_DIV AND T_AVG3.GRADE = '" + param._grade + "' AND T_AVG3.CLASSCD = W3.CLASSCD AND T_AVG3.SCHOOL_KIND = W3.SCHOOL_KIND AND T_AVG3.CURRICULUM_CD = W3.CURRICULUM_CD AND T_AVG3.SUBCLASSCD = W3.SUBCLASSCD ");
            stb.append("          AND T_AVG3.AVG_DIV = '3' AND T_AVG3.COURSECD = W1.COURSECD  AND T_AVG3.MAJORCD = W1.MAJORCD AND T_AVG3.COURSECODE = W1.COURSECODE ");
            stb.append("      LEFT JOIN RECORD_AVERAGE_SDIV_DAT T_AVG4 ON T_AVG4.YEAR = W3.YEAR AND T_AVG4.SEMESTER = W3.SEMESTER AND T_AVG4.TESTKINDCD = W3.TESTKINDCD AND T_AVG4.TESTITEMCD = W3.TESTITEMCD AND T_AVG4.SCORE_DIV = W3.SCORE_DIV AND T_AVG4.GRADE = '" + param._grade + "' AND T_AVG4.CLASSCD = W3.CLASSCD AND T_AVG4.SCHOOL_KIND = W3.SCHOOL_KIND AND T_AVG4.CURRICULUM_CD = W3.CURRICULUM_CD AND T_AVG4.SUBCLASSCD = W3.SUBCLASSCD ");
            stb.append("          AND T_AVG4.AVG_DIV = '4' AND T_AVG4.COURSECD = W1.COURSECD AND T_AVG4.MAJORCD = W1.MAJORCD AND T_AVG4.COURSECODE = '0000' ");
            stb.append("    WHERE ");
            stb.append("      W3.YEAR = '" + param._ctrlYear + "' ");
            stb.append("      AND     W3.SUBCLASSCD NOT IN ('333333', '555555', '99999A', '99999B') ");
            stb.append("      AND  (W3.SUBCLASSCD = '999999' ");
            stb.append("      OR " + stbtestcd.toString());
            stb.append("          ) ");
            stb.append("     ) ");

            //成績データの表（通常科目）
            stb.append(",RECORD_SCORE AS(");
            stb.append("    SELECT ");
            stb.append("      CSD.SCHREGNO ");
            stb.append("      ,CSD.SEMESTER ");
            stb.append("      ,W3.TESTKINDCD ");
            stb.append("      ,W3.TESTITEMCD ");
            stb.append("      ,W3.SCORE_DIV ");
            stb.append("      ,CD.CLASSCD || '-' || CD.SCHOOL_KIND || '-' || CD.CURRICULUM_CD || '-' || ");
            stb.append("         CD.SUBCLASSCD AS SUBCLASSCD ");
            stb.append("      ,W3.VALUE_DI ");
            stb.append("      ,W3.COMP_CREDIT ");
            stb.append("      ,W3.GET_CREDIT ");
            stb.append("      ,T11.STANDARD_SCORE ");
            stb.append("      ,WC.CREDITS ");
            stb.append("    FROM ");
            stb.append("      CHAIR_STD_DAT CSD ");
            stb.append("      LEFT JOIN CHAIR_DAT CD ");
            stb.append("        ON CD.YEAR = CSD.YEAR ");
            stb.append("       AND CD.SEMESTER = CSD.SEMESTER ");
            stb.append("       AND CD.CHAIRCD = CSD.CHAIRCD ");
            stb.append("      LEFT JOIN RECORD_SCORE_DAT W3 ");
            stb.append("        ON W3.YEAR = CD.YEAR ");
            stb.append("       AND W3.SEMESTER = CD.SEMESTER ");
            stb.append("       AND W3.CLASSCD = CD.CLASSCD ");
            stb.append("       AND W3.SCHOOL_KIND = CD.SCHOOL_KIND ");
            stb.append("       AND W3.CURRICULUM_CD = CD.CURRICULUM_CD ");
            stb.append("       AND W3.SUBCLASSCD = CD.SUBCLASSCD ");
            stb.append("       AND W3.SCHREGNO = CSD.SCHREGNO ");
            stb.append("       AND ( " + stbtestcd.toString() + " OR ( W3.SCHREGNO IS NOT NULL AND W3.TESTKINDCD IS NULL AND W3.TESTITEMCD IS NULL ) ) ");
            stb.append("      LEFT JOIN SCHREG_REGD_DAT WS ");
            stb.append("        ON WS.YEAR = W3.YEAR ");
            stb.append("       AND WS.SEMESTER = W3.SEMESTER ");
            stb.append("       AND WS.SCHREGNO = W3.SCHREGNO ");
            stb.append("      LEFT JOIN CREDIT_MST WC ");
            stb.append("        ON WC.YEAR = W3.YEAR ");
            stb.append("       AND WC.CLASSCD = W3.CLASSCD ");
            stb.append("       AND WC.SCHOOL_KIND = W3.SCHOOL_KIND ");
            stb.append("       AND WC.CURRICULUM_CD = W3.CURRICULUM_CD ");
            stb.append("       AND WC.SUBCLASSCD = W3.SUBCLASSCD");
            stb.append("       AND WC.COURSECD = WS.COURSECD ");
            stb.append("       AND WC.MAJORCD = WS.MAJORCD ");
            stb.append("       AND WC.COURSECODE = WS.COURSECODE ");
            stb.append("       AND WC.GRADE = WS.GRADE ");
            //成績不振科目データの表
            stb.append("      LEFT JOIN STANDARD_SCORE_DAT T11 ");
            stb.append("        ON T11.YEAR = W3.YEAR ");
            stb.append("       AND T11.SEMESTER = W3.SEMESTER ");
            stb.append("       AND T11.TESTKINDCD = W3.TESTKINDCD");
            stb.append("       AND T11.TESTITEMCD = W3.TESTITEMCD");
            stb.append("       AND T11.SCORE_DIV = W3.SCORE_DIV ");
            stb.append("       AND T11.CLASSCD = W3.CLASSCD ");
            stb.append("       AND T11.SCHOOL_KIND = W3.SCHOOL_KIND ");
            stb.append("       AND T11.CURRICULUM_CD = W3.CURRICULUM_CD ");
            stb.append("       AND T11.SUBCLASSCD = W3.SUBCLASSCD ");
            stb.append("       AND T11.COURSECD = WS.COURSECD ");
            stb.append("       AND T11.MAJORCD = WS.MAJORCD ");
            stb.append("       AND T11.COURSECODE = WS.COURSECODE ");
            stb.append("       AND T11.GRADE = WS.GRADE ");
            stb.append("    WHERE ");
            stb.append("       CSD.YEAR = '" + param._ctrlYear + "' ");
            if (SEMEALL.equals(param._semester)) {
                stb.append("     AND CSD.SEMESTER <= '" + param._ctrlSemester + "' ");
            } else {
                stb.append("     AND CSD.SEMESTER <= '" + param._semester + "' ");
            }
            stb.append("       AND EXISTS(SELECT 'X' FROM SCHNO_A W1 WHERE W1.SCHREGNO = CSD.SCHREGNO) ");
            stb.append("     ) ");

            stb.append(" ,COMBINED_SUBCLASS AS ( ");
            stb.append("    SELECT ");
            stb.append("            COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || ");
            stb.append("           COMBINED_SUBCLASSCD AS SUBCLASSCD");
            stb.append("    FROM   SUBCLASS_REPLACE_COMBINED_DAT");
            stb.append("    WHERE  YEAR = '" + param._ctrlYear + "'");
            stb.append("    GROUP BY ");
            stb.append("            COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || ");
            stb.append("           COMBINED_SUBCLASSCD");
            stb.append(" ) ");

            stb.append(" ,ATTEND_SUBCLASS AS ( ");
            stb.append("    SELECT ");
            stb.append("            ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ");
            stb.append("           ATTEND_SUBCLASSCD AS SUBCLASSCD, MAX(PRINT_FLG2) AS PRINT_FLG");
            stb.append("    FROM   SUBCLASS_REPLACE_COMBINED_DAT");
            stb.append("    WHERE  YEAR = '" + param._ctrlYear + "'");
            stb.append("    GROUP BY ");
            stb.append("            ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ");
            stb.append("           ATTEND_SUBCLASSCD");
            stb.append(" ) ");

            stb.append(" ,T_SUBCLASSCD_SUB AS ( ");
            stb.append("    SELECT SCHREGNO, SUBCLASSCD FROM RECORD_REC ");
            stb.append("    UNION ");
            stb.append("      ( SELECT ");
            stb.append("	      T1.SCHREGNO, ");
            stb.append("	      T1.SUBCLASSCD ");
            stb.append("        FROM ");
            stb.append("	      RECORD_SCORE T1 ");
//            stb.append("	  WHERE ");
//            stb.append("	      T1.VALUE_DI = '*' ");
//            stb.append("	      OR ");
//            stb.append("	      EXISTS (SELECT ");
//            stb.append("	                'X' ");
//            stb.append("	              FROM ");
//            stb.append("	                RECORD_REC T2 ");
//            stb.append("	              WHERE ");
//            stb.append("	                T2.SCHREGNO = T1.SCHREGNO ");
//            stb.append("	                AND T2.SEMESTER = T1.SEMESTER ");
//            stb.append("	                AND T2.TESTKINDCD = T1.TESTKINDCD ");
//            stb.append("	                AND T2.TESTITEMCD = T1.TESTITEMCD ");
//            stb.append("	                AND T2.SCORE_DIV = T1.SCORE_DIV ");
//            stb.append("	                AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
//            stb.append("	             ) ");
            stb.append("	 ) ");

            stb.append(" ) ");
            stb.append(" ,T_SUBCLASSCD AS ( ");
            stb.append("   SELECT DISTINCT * FROM T_SUBCLASSCD_SUB ");
            stb.append(" ) ");

            stb.append(" ,T_TESTCD AS ( ");
            stb.append("    SELECT SCHREGNO, SUBCLASSCD, SEMESTER, TESTKINDCD, TESTITEMCD, SCORE_DIV FROM RECORD_REC ");
            stb.append("    UNION ");
            stb.append("    SELECT SCHREGNO, SUBCLASSCD, SEMESTER, TESTKINDCD, TESTITEMCD, SCORE_DIV FROM RECORD_SCORE ");
            stb.append(" ) ");

            //メイン表
            stb.append(" SELECT  T1.SUBCLASSCD ");
            stb.append("        ,T1.SCHREGNO ");
            stb.append("        ,T2.SEMESTER ");
            stb.append("        ,T2.TESTKINDCD ");
            stb.append("        ,T2.TESTITEMCD ");
            stb.append("        ,T2.SCORE_DIV ");
            stb.append("        ,T2.SEMESTER || T2.TESTKINDCD || T2.TESTITEMCD || T2.SCORE_DIV AS TESTCD ");
            stb.append("        ,T3.SCORE ");
            stb.append("        ,T3.AVG ");
            stb.append("        ,T3.GRADE_RANK ");
            stb.append("        ,T3.GRADE_AVG_RANK ");
            stb.append("        ,T3.CLASS_RANK ");
            stb.append("        ,T3.CLASS_AVG_RANK ");
            stb.append("        ,T3.COURSE_RANK ");
            stb.append("        ,T3.COURSE_AVG_RANK ");
            stb.append("        ,T3.MAJOR_RANK ");
            stb.append("        ,T3.MAJOR_AVG_RANK ");
            stb.append("        ,T3.GRADE_AVG ");
            stb.append("        ,T3.GRADE_COUNT ");
            stb.append("        ,T3.GRADE_HIGHSCORE ");
            stb.append("        ,T3.HR_AVG ");
            stb.append("        ,T3.HR_COUNT ");
            stb.append("        ,T3.HR_HIGHSCORE ");
            stb.append("        ,T3.COURSE_AVG ");
            stb.append("        ,T3.COURSE_COUNT ");
            stb.append("        ,T3.COURSE_HIGHSCORE ");
            stb.append("        ,T3.MAJOR_AVG ");
            stb.append("        ,T3.MAJOR_COUNT ");
            stb.append("        ,T3.MAJOR_HIGHSCORE ");
            stb.append("        ,T33.COMP_CREDIT ");
            stb.append("        ,T33.GET_CREDIT ");
            stb.append("        ,T33.VALUE_DI ");
            stb.append("        ,CASE WHEN T9.SUBCLASSCD IS NOT NULL THEN -1");
            stb.append("              WHEN T10.SUBCLASSCD IS NOT NULL THEN 1 ELSE 0 END AS REPLACEMOTO ");
            stb.append("        ,VALUE(T10.PRINT_FLG,'0') AS PRINT_FLG");
            stb.append("        ,T33.STANDARD_SCORE ");
            stb.append("        ,T33.CREDITS ");

            //対象生徒・講座の表
            stb.append(" FROM T_SUBCLASSCD T1 ");
            //成績の表
            stb.append(" LEFT JOIN T_TESTCD T2 ON T2.SCHREGNO = T1.SCHREGNO AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append(" LEFT JOIN RECORD_REC T3 ON T3.SUBCLASSCD = T2.SUBCLASSCD AND T3.SCHREGNO = T2.SCHREGNO AND T3.SEMESTER = T2.SEMESTER AND T3.TESTKINDCD = T2.TESTKINDCD AND T3.TESTITEMCD = T2.TESTITEMCD AND T3.SCORE_DIV = T2.SCORE_DIV ");
            stb.append(" LEFT JOIN RECORD_SCORE T33 ON T33.SUBCLASSCD = T2.SUBCLASSCD AND T33.SCHREGNO = T2.SCHREGNO  AND T33.SEMESTER = T2.SEMESTER AND T33.TESTKINDCD = T2.TESTKINDCD AND T33.TESTITEMCD = T2.TESTITEMCD AND T33.SCORE_DIV = T2.SCORE_DIV ");
            stb.append("       AND (T33.VALUE_DI IS NOT NULL OR T33.CREDITS IS NOT NULL) ");
            //合併先科目の表
            stb.append("  LEFT JOIN COMBINED_SUBCLASS T9 ON T9.SUBCLASSCD = T1.SUBCLASSCD");
            //合併元科目の表
            stb.append("  LEFT JOIN ATTEND_SUBCLASS T10 ON T10.SUBCLASSCD = T1.SUBCLASSCD");
//            stb.append(" WHERE ");
//            stb.append("     SUBSTR(T1.SUBCLASSCD, 1, 2) < '99' OR T1.SUBCLASSCD like '%" + SUBCLASSCD999999 + "'");
            stb.append(" ORDER BY T1.SCHREGNO, T2.SEMESTER, T2.TESTKINDCD, T2.TESTITEMCD, T2.SCORE_DIV, T1.SUBCLASSCD");

            return stb.toString();
        }
    }

    private static class Rank {
        final String _rank;
        final String _avgRank;
        final String _count;
        final String _avg;
        final String _highscore;
        public Rank(final String rank, final String avgRank, final String count, final String avg, final String highscore) {
            _rank = rank;
            _avgRank = avgRank;
            _count = count;
            _avg = avg;
            _highscore = highscore;
        }
        public String getRank(final Param param) {
            return "2".equals(param._rankDiv) ? _avgRank : _rank;
        }
    }

    private static class Semester {
        final String _semester;
        final String _semestername;
        final DateRange _dateRange;
        public Semester(final String semester, final String semestername, final String sdate, final String edate) {
            _semester = semester;
            _semestername = semestername;
            _dateRange = new DateRange(_semester, _semestername, sdate, edate);
        }
    }

    private static class DateRange {
        final String _key;
        final String _name;
        final String _sdate;
        final String _edate;
        public DateRange(final String key, final String name, final String sdate, final String edate) {
            _key = key;
            _name = name;
            _sdate = sdate;
            _edate = edate;
        }
        public String toString() {
            return "DateRange(" + _key + ", " + _sdate + ", " + _edate + ")";
        }
    }

    private static class SubclassMst implements Comparable {
        final String _classcd;
        final String _subclasscd;
        final String _classabbv;
        final String _classname;
        final String _subclassabbv;
        final String _subclassname;
        final Integer _classShoworder3;
        final Integer _subclassShoworder3;
        boolean _isSaki;
        SubclassMst _combinedSubclassMst;
        public SubclassMst(final String classcd, final String subclasscd, final String classabbv, final String classname, final String subclassabbv, final String subclassname,
                final Integer classShoworder3, final Integer subclassShoworder3) {
            _classcd = classcd;
            _subclasscd = subclasscd;
            _classabbv = classabbv;
            _classname = classname;
            _subclassabbv = subclassabbv;
            _subclassname = subclassname;
            _classShoworder3 = classShoworder3;
            _subclassShoworder3 = subclassShoworder3;
        }
        public int compareTo(final Object o) {
            final SubclassMst mst = (SubclassMst) o;
            int cmp = 0;
            if (0 != cmp) return cmp;
            cmp = _classShoworder3.compareTo(mst._classShoworder3);
            if (0 != cmp) return cmp;
            if (null != _classcd && null != mst._classcd) {
                cmp = _classcd.compareTo(mst._classcd);
                if (0 != cmp) return cmp;
            }
            cmp = _subclassShoworder3.compareTo(mst._subclassShoworder3);
            if (0 != cmp) return cmp;
            if (null != _subclasscd && null != mst._subclasscd) {
                cmp = _subclasscd.compareTo(mst._subclasscd);
            }
            return cmp;
        }
    }

    private class LatestHyoutei {
        final String _year;
        final int _score;
        final int _cnt;
        LatestHyoutei(final String year, final int score, final int cnt) {
            _year = year;
            _score = score;
            _cnt = cnt;
        }
        public String toString() {
            return "LatestHyoutei(year = " + _year + ", score = " + _score + ", count = " + _cnt + ")";
        }
    }

    private void print2(final DB2UDB db2, final IPdf ipdf, final Param param, final Student student) {
        final String form = "KNJD187E.frm";
        ipdf.VrSetForm(form, 4);

        printHeader(db2, ipdf, param, student);
        printAttendance(ipdf, param, student);
        printCommitteeClub(ipdf, param, student);
        printScore(ipdf, param, student);
    }

    private void printCommitteeClub(final IPdf ipdf, final Param param, final Student student) {
        //委員会
        String committeeStr1 = (String)student._committeeMap.get("1");
         if (!"".equals(StringUtils.defaultString(committeeStr1, ""))) {
              ipdf.VrsOut("COMMITTEE1", committeeStr1);
          }

        String committeeStr2 = (String)student._committeeMap.get("2");
           if (!"".equals(StringUtils.defaultString(committeeStr2, ""))) {
               ipdf.VrsOut("COMMITTEE2", committeeStr2);
           }
           //部活
           if (!"".equals(StringUtils.defaultString(student._someClubName, ""))) {
               String[] putwk = KNJ_EditEdit.get_token(student._someClubName, 40, 3);
               for (int cnt = 0;cnt < putwk.length;cnt++) {
                   if (!"".equals(StringUtils.defaultString(putwk[cnt], ""))) {
                       ipdf.VrsOutn("CLUB", cnt + 1, putwk[cnt]);
                   }
               }
           }

    }

    private void printHeader(final DB2UDB db2, final IPdf ipdf, final Param param, final Student student) {
        ipdf.VrsOut("TITLE", KNJ_EditDate.getAutoFormatYear(db2, Integer.parseInt(param._ctrlYear)) + "年度 " + student._gradeName1 + student._coursecodeName + " 成績通知表");
        ipdf.VrsOut("GRADE", student._gradeName1);
        ipdf.VrsOut("SCHREGNO", student._hrName + student._attendno.substring(1));
        final int nlen = KNJ_EditEdit.getMS932ByteLength(student._name);
        final String nfield = nlen > 30 ? "2" : "1";
        ipdf.VrsOut("NAME" + nfield, student._name);
        final int trlen = KNJ_EditEdit.getMS932ByteLength(student._staffName);
        final String trfield = trlen > 30 ? "2" : "1";
        ipdf.VrsOut("TR_NAME" + trfield, student._staffName);
        ipdf.VrsOut("GRADE_RANK_VALUE_NAME", Integer.parseInt(param._gradeCd) + "年次評定");

        for (Iterator ite = param._semesterMap.keySet().iterator();ite.hasNext();) {
            String kStr = (String)ite.next();
            Semester putwk = (Semester)param._semesterMap.get(kStr);
            if ("9".equals(putwk._semester)) {
                ipdf.VrsOut("SEMESTER9", putwk._semestername);
            } else {
                ipdf.VrsOut("SEMESTER1_" + putwk._semester, putwk._semestername);
            }
            //中間/期末の行の出力については、成績の出力で対応。
        }
    }

    private void printAttendance(final IPdf ipdf, final Param param, final Student student) {
        for (int line = 1; line <= 3; line++) {
            final String semester;
            if (line > Integer.parseInt(param._semester)) continue;
            if (line == 3) {
                semester = SEMEALL;
            } else {
                semester = String.valueOf(line);
            }
            final Attendance att = (Attendance) student._attendMap.get(semester);
            if (null != att) {
                Semester semeswk = (Semester)param._semesterMap.get(semester);
                int putChkWk1 = Integer.parseInt(semeswk._semester) >= 3 ? 9 : Integer.parseInt(semeswk._semester);
                int putChkWk2 = Integer.parseInt(semester) >= 3 ? 9 : Integer.parseInt(semester);
                if (putChkWk1 <= putChkWk2) {
                    ipdf.VrsOutn("SEMESTER2", line, semeswk._semestername);               //学期
                    ipdf.VrsOutn("LESSON", line, (att._lesson > 0 ? String.valueOf(att._lesson) : "")); // 授業日数
                    ipdf.VrsOutn("SUSPEND", line, String.valueOf(att._suspend));          // 出停
                    ipdf.VrsOutn("MOURNING", line, String.valueOf(att._mourning));        // 忌引
                    ipdf.VrsOutn("ABSENCE", line, String.valueOf(att._absent));           // 欠席日数
                    ipdf.VrsOutn("PRESENT", line, chkZeroVal(att._lesson, att._present)); // 出席日数
                    ipdf.VrsOutn("LATE", line, chkZeroVal(att._lesson, att._late));       // 遅刻回数
                    ipdf.VrsOutn("EARLY", line, chkZeroVal(att._lesson, att._early));     // 早退回数
                }
            }
        }
        String attendRemark = "";
        String delim = "";
        for (Iterator ity = student._semesRemarkMap.keySet().iterator();ity.hasNext();) {
            String kStr = (String)ity.next();
            attendRemark += delim + (String)student._semesRemarkMap.get(kStr);
            delim = "  ";
        }

        String[] putwk = KNJ_EditEdit.get_token(attendRemark, 26, 6);
        if (putwk != null) {
            for (int cnt=0;cnt < putwk.length;cnt++) {
                if (putwk[cnt] != null) {
                    ipdf.VrsOutn("ABSENCE_RESAON", cnt + 1, putwk[cnt]); // 出欠等の理由
                }
            }
        }
    }
    private String chkZeroVal(final int lesson, final int chkVal) {
        if (lesson > 0) {
            return String.valueOf(chkVal);
        } else if (chkVal > 0) {
              return String.valueOf(chkVal);
        }
        return "";
    }

    private void printScore(final IPdf ipdf, final Param param, final Student student) {

        //合計欄
        //合計は科目だけで良い。欠課は不要。
        final int[][] idxlist = {{1, 2, 3}, {4, 5, 6}, {7, 8}, {9, 10}}; //学期毎で利用indexを分類
        for (int ti = 0; ti < param._testcds.length; ti++) {
            final String testcd = param._testcds[ti];
            final Score score999999 = student.getSubClass(SUBCLASSCD999999).getScore(testcd);
            if (null == score999999) {
                continue;
            }

            final String semesStr = testcd.substring(0, 1);
            final String testKindCd = testcd.substring(1, 3);
            if ("3".equals(semesStr) || "9".equals(semesStr)) {
                ipdf.VrsOut("TEST_ITEM" + semesStr, "学年末");
            } else {
                if ("01".equals(testKindCd)) {
                    ipdf.VrsOut("TEST_ITEM" + semesStr + "_1", "中間");
                } else {
                    ipdf.VrsOut("TEST_ITEM" + semesStr + "_2", "期末");
                }
            }
            if (Integer.parseInt(testKindCd) < 3 && Integer.parseInt(semesStr) <= Integer.parseInt(param._semester)) {  //中間/期末の2種類しか無いはず。
                int idx = "3".equals(semesStr) ? 7 : idxlist[Integer.parseInt(semesStr)-1][Integer.parseInt(testKindCd)-1];  //indexは0ベースなので-1。
                ipdf.VrsOutn("NUM"           , idx, score999999._score); // 得点
                ipdf.VrsOutn("AVERAGE"       , idx, sishaGonyu(score999999._avg)); // 平均
                ipdf.VrsOutn("CLASS_RANK"    , idx, score999999._hrRank.getRank(param)); // クラス順位
                ipdf.VrsOutn("COURSE_RANK"   , idx, score999999._courseRank.getRank(param)); // コース順位
            }
        }

        //年度末指定時のみ出力
        if (SEMEALL.equals(param._semester)) {
            //x年次評定
            ipdf.VrsOut("RANK_VALUE1", student.getAvgOneYear(param));
            //1～3年 評定
            ipdf.VrsOut("RANK_VALUE2", student.getAvgAllYear(param));
        }

        final int maxRecord = 18;

        final List subclassList = new ArrayList(student._subclassMap.values());
        final List sakiSubclassList = new ArrayList();
        for (final Iterator it = subclassList.iterator(); it.hasNext();) {
            final Subclass subclass = (Subclass) it.next();
            if (subclass._mst._isSaki) {
                sakiSubclassList.add(subclass);
            }
            if (SUBCLASSCD999999.equals(subclass._mst._subclasscd)) {
                it.remove();
            }
        }
        final List removeSakiSubclassList = new ArrayList();
        for (final Iterator it = subclassList.iterator(); it.hasNext();) {
            final Subclass subclass = (Subclass) it.next();
            if (null != subclass._mst._combinedSubclassMst) {
                for (final Iterator cit = sakiSubclassList.iterator(); cit.hasNext();) {
                    final Subclass sakiSubclass = (Subclass) cit.next();
                    if (sakiSubclass._mst == subclass._mst._combinedSubclassMst) {
                        subclass._sakiSubclass = sakiSubclass;
                        removeSakiSubclassList.add(sakiSubclass);
                    }
                }
            }
        }
        if (param._isOutputDebug) {
            for (final Iterator it = sakiSubclassList.iterator(); it.hasNext();) {
                final Subclass sakiSubclass = (Subclass) it.next();
                log.info(" | saki subclass = " + sakiSubclass._mst._subclasscd + " " + sakiSubclass._mst._subclassname + " / score = " + sakiSubclass._scoreMap);
            }
        }
        subclassList.removeAll(removeSakiSubclassList);
        Collections.sort(subclassList);
        final int printCount = subclassList.size();  //Math.min(subclassList.size(),  maxRecord);
        String defClassCd = "";
        String subClassCdBk = "";
        int subclsCnt = 1;
        for (int i = 0; i < printCount; i++) {
            final Subclass subclass = (Subclass) subclassList.get(i);
//            log.info(" subclasscd = " + subclass._mst._subclasscd);

            final boolean isPrintSakikamoku = subclass._sakiSubclass != null;

            final SubclassMst subclsObj = (!subclass._mst._isSaki && subclass._mst._combinedSubclassMst != null) ? subclass._mst._combinedSubclassMst : subclass._mst;
            if (!"".equals(subClassCdBk) && !subClassCdBk.equals(subclsObj._subclasscd)) {
                subclsCnt++;
            }

            ipdf.VrsOut("GRPCD1", subclsObj._classcd); // 教科コード
            if(!defClassCd.equals(subclsObj._classcd)) ipdf.VrsOut("CLASS_NAME" + (getMS932ByteLength(subclsObj._classname) > 4 ? "2" : "1"), subclsObj._classname); // 教科名
            ipdf.VrsOut("GRPCD2", String.valueOf(subclsCnt)); // 科目コード
            if (!subClassCdBk.equals(subclsObj._subclasscd)) {
                ipdf.VrsOut("SUBCLASS_NAME" + (getMS932ByteLength(subclsObj._subclassname) > 16 ? "2" : "1"), subclsObj._subclassname); // 科目名
            }
            final String creditsVal = (String)student._subclsCreditMap.get(subclsObj._subclasscd);
            ipdf.VrsOut("CREDIT", creditsVal);
            defClassCd = subclass._mst._classcd;

            if (param._isOutputDebug) {
                log.info(" subclass = " + subclass._mst._subclasscd + " " + subclass._mst._subclassname + " / score = " + subclass._scoreMap + " " + (subclass._mst._isSaki ? "[saki]" : "") + ", saki = " + (isPrintSakikamoku ? subclass._sakiSubclass._mst._subclassname : "null"));
            }

            // ★★★ ここから下はRECORD処理。ここまでに他の出力処理を終わらせておくこと。★★★
            //欠課の出力(他の内容はテスト項目の有無に違いがある為、別口で処理)
            List kekkaList = (List)student._tbl5InfoMap.get(subclass._mst._subclasscd);
            for (Iterator ite = param._semesterMap.keySet().iterator();ite.hasNext();) {
                String semester = (String)ite.next();
                if (Integer.parseInt(semester) > Integer.parseInt(param._semester)) continue; //指定学期以降のデータ処理なら、continue
                if (kekkaList != null) {
                    BigDecimal kekkaCnt = new BigDecimal("0");
                    for (int cnt=0;cnt < kekkaList.size();cnt++) {
                        final tbl5Info kekkainfo = (tbl5Info)kekkaList.get(cnt);
                        if (kekkainfo._semester.equals(semester)) {
                            kekkaCnt = kekkaCnt.add(kekkainfo._sick);
                        }
                    }
                    ipdf.VrsOut("KEKKA" + semester, kekkaCnt.toString());
                }
                final String clsCd = subclass._mst._classcd.length() > 2 ? subclass._mst._classcd.substring(0,2) : subclass._mst._classcd;
                if (Integer.parseInt(clsCd) >= 90) {
                    if (!"9".equals(semester)) {
                        ipdf.VrsOut("SLASH_SCORE" + semester + "_1", param._shashImgPath);
                        ipdf.VrsOut("SLASH_SCORE" + semester + "_2", param._shashImgPath);
                    } else {
                        ipdf.VrsOut("SLASH_SCORE9" + semester, param._shashImgPath);
                        ipdf.VrsOut("SLASH_AVERAGE" + semester, param._shashImgPath);
                    }
                }
            }

            String locSemesBak = "";
            int scoresum = 0;
            int stdscoresum = 0;
            int maxSemScoresum = 0;
            int maxSemStdscoresum = 0;
            List chkfieldList = new ArrayList();
            boolean chkfieldNuriFlg = false;
            for (int ti = 0; ti < param._testcds.length; ti++) {
                final String testcd = param._testcds[ti];
                final String sfx = param._testcdsField[ti];
                final String locSemester = testcd.substring(0,1);
                final String locTestKindCd = testcd.substring(1,3);
                if (!locSemesBak.equals(locSemester)) {
                    scoresum = 0;
                    stdscoresum = 0;
                    chkfieldList = new ArrayList();
                    chkfieldNuriFlg = false;
                }
                if ("".equals(StringUtils.defaultString(subclass._mst._classcd, ""))) continue;  //教科コードが入ってないなら、continue
                if (Integer.parseInt(locSemester) > Integer.parseInt(param._semester)) continue; //指定学期以降のデータ処理なら、continue

                final Score score = student.getSubClass(subclass._mst._subclasscd).getScore(testcd);
                if(score == null) continue;
                if(score._score == null) continue;
                final String clsCd = subclass._mst._classcd.length() > 2 ? subclass._mst._classcd.substring(0,2) : subclass._mst._classcd;
                if (Integer.parseInt(clsCd) < 90) {
                    if (!"9".equals(locSemester)) {
                        ipdf.VrsOut("SCORE" + sfx, score._score); // 素点
                        String field;
                        field = "ASTER" + sfx;
                        if ("欠".equals(score._score)) {
                            chkfieldNuriFlg = true;
                        }
                        scoresum = sum_intValWithChk(scoresum, score._score);
                        stdscoresum = sum_intValWithChk(stdscoresum, param._stdScore);
                        maxSemScoresum = sum_intValWithChk(maxSemScoresum, score._score);
                        maxSemStdscoresum = sum_intValWithChk(maxSemStdscoresum, param._stdScore);
                        chkfieldList.add(field);
                        if (isAkaten(score, param)) {
                            ipdf.VrsOut("ASTER" + sfx, "*");
                        }
                        if ("3".equals(locSemester) ||  !"01".equals(locTestKindCd)) {
                            if("3".equals(locSemester)) { //3学期はこれまでの考査の合計で判断する
                                if (maxSemScoresum < maxSemStdscoresum || chkfieldNuriFlg) {
                                    for (int cnt = 0;cnt < chkfieldList.size();cnt++) {
                                        final String nurifield = (String)chkfieldList.get(cnt);
                                        ipdf.VrsOut(nurifield, "*");
                                    }
                                }
                            } else {
                                if (scoresum < stdscoresum || chkfieldNuriFlg) {
                                    for (int cnt = 0;cnt < chkfieldList.size();cnt++) {
                                        final String nurifield = (String)chkfieldList.get(cnt);
                                        ipdf.VrsOut(nurifield, "*");
                                    }
                                }
                            }
                        }
                    } else {
                    }
                }
                locSemesBak = locSemester;

                if (TESTCD_GAKUNEN_HYOTEI.equals(testcd)) {
                    final Score useScore;
                    if (isPrintSakikamoku) {
                        useScore = subclass._sakiSubclass.getScore(testcd);
                    } else {
                        useScore = score;
                    }
                    String field = "SCORE9";
                    if (null == useScore._score) {
                        ipdf.VrsOut(field, "DUMMY" + removeSakiSubclassList.indexOf(subclass._sakiSubclass)); // 素点
                        ipdf.VrAttribute(field, "X=10000"); // 素点
                    } else {
                        ipdf.VrsOut(field, useScore._score); // 素点
                    }
                }
            }
              final String avgStr = (String)student._courseAvgMap.get(subclass._mst._subclasscd);
               if (!"".equals(StringUtils.defaultString(avgStr, ""))) {
                   final BigDecimal setAvg = new BigDecimal(avgStr).setScale(1, BigDecimal.ROUND_HALF_UP);
                   ipdf.VrsOut("SUBCLASS_COURSE_AVERAGE", setAvg.toString());
               }
            subClassCdBk = subclsObj._subclasscd;
            ipdf.VrEndRecord();
        }
        // 残の行には空行を出力
        for (int i = printCount; i < maxRecord; i++) {
            final boolean isPrintSakikamoku = false;
            final String div = isPrintSakikamoku ? "1" : "2";
            ipdf.VrsOut("GRP" + div, String.valueOf(i)); // 科目名
            ipdf.VrsOut("SUBCLASS_NAME" + div, "DUMMY"); // 科目名
            ipdf.VrAttribute("SUBCLASS_NAME" + div, "X=10000");
            ipdf.VrEndRecord();
        }
    }

    private static int sum_intValWithChk(final int sum_base, final String sum_val) {
        int retVal = sum_base;
        if (sum_val != null && NumberUtils.isNumber(sum_val)) {
            retVal += Integer.parseInt(sum_val);
        }
        return retVal;
    }
    private static boolean isAkaten(final Score score, final Param param) {

        if (param._stdScore != null && NumberUtils.isNumber(param._stdScore)) {
            if (score._score != null && NumberUtils.isNumber(score._score) &&  Integer.parseInt(score._score) < Integer.parseInt(param._stdScore)) {
                return true;
            }
        }
        return false;
    }

    private static int getMS932ByteLength(final String s) {
        if (null != s) {
            try {
                return s.getBytes("MS932").length;
            } catch (Exception e) {
                log.error("exception!", e);
            }
        }
        return 0;
    }

    private static class Param {
        final String _ctrlYear;
        final String _ctrlDate;
        final String _semester;
        final String _ctrlSemester;
        final String _schregSemester;
        final String _nouse9Semester;

        final String _disp;
        final String _grade;
        final String _gradeHrclass;
        final String[] _categorySelected;
        /** 出欠集計日付 */
        final String _date;

        final String _rankDiv = "2"; // 順位の基準点 1:総合点 2:平均点
        final String _gradeCd;
        final String _schoolKind;
        final String _schoolCd;
        final String _documentroot;
        final boolean _isOutputDebug;
        final String _imagePath;
        final String _extension;
        final String _shashImgPath;
        final String _stdScore;

        /** 端数計算共通メソッド引数 */
        private Map _semesterMap;
        private Map _subclassMstMap;

        private KNJSchoolMst _knjSchoolMst;

        private String _schoolName;
        final String[] _testcds;
        final String[] _testcdsField;
        final Map _attendParamMap;
        private Map _attendRanges;


        Param(final HttpServletRequest request, final DB2UDB db2) {
            _ctrlYear = request.getParameter("LOGIN_YEAR");
            _ctrlDate = request.getParameter("LOGIN_DATE");
            _ctrlSemester = request.getParameter("LOGIN_SEMESTER");
            _disp = request.getParameter("DISP");
            _gradeHrclass = request.getParameter("GRADE_HR_CLASS");
            if ("1".equals(_disp)) {
                _grade = _gradeHrclass;
            } else {
                _grade = _gradeHrclass.substring(0, 2);
            }
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _date = KNJ_EditDate.H_Format_Haifun(request.getParameter("DATE"));
            _documentroot = request.getParameter("DOCUMENTROOT");
            _schoolCd = request.getParameter("SCHOOLCD");

            try {
                _knjSchoolMst = new KNJSchoolMst(db2, _ctrlYear);
            } catch (SQLException e) {
                log.warn("学校マスタ取得でエラー", e);
            }
            _semester = _knjSchoolMst._semesterDiv.equals(request.getParameter("SEMESTER")) ? SEMEALL : request.getParameter("SEMESTER");
            _schregSemester = request.getParameter("SEMESTER");
            _semesterMap = loadSemester(db2, _ctrlYear, _grade);
            _nouse9Semester = "9".equals(_schregSemester) ? _ctrlSemester : _schregSemester;
            _gradeCd = getGradeCd(db2);
            _schoolKind = getSchoolKind(db2);
            setCertifSchoolDat(db2);

            _stdScore = request.getParameter("SCORELINE");
            setSubclassMst(db2);

            _isOutputDebug = "1".equals(getDbPrginfoProperties(db2, "outputDebug"));

            final KNJ_Control.ReturnVal returnval = getImagepath(db2);
            _imagePath = null == returnval ? null : returnval.val4;
            _extension = null == returnval ? null : returnval.val5; // 写真データの拡張子
            _shashImgPath = getSlashPath();

            final String z010Name1 = setZ010Name1(db2);
            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
            _attendParamMap.put("grade", _grade);
            if ("2".equals(_disp)) _attendParamMap.put("hrClass", _gradeHrclass.substring(2));  //複数クラスの場合どうする？
            _attendParamMap.put("useTestCountflg", "TESTITEM_MST_COUNTFLG_NEW_SDIV");
            _attendParamMap.put("useCurriculumcd", "1");

            _testcds = new String[] {
                    "1010108",        //1学期中間
//                    "1020101",
                    "1020108",        //1学期期末
                    "2010108",        //2学期中間
//                    "2020101",
                    "2020108",        //2学期期末
//                    "3020101",
                    "3020108",        //3学期末
                    TESTCD_GAKUNEN_HYOKA,  //学年評価
                    TESTCD_GAKUNEN_HYOTEI, //学年評定
            };

            _testcdsField = new String[] {
                    "1_1",
                    "1_2",
//                    "1_3",
                    "2_1",
                    "2_2",
//                    "2_3",
                    "3_1",
//                    "3_2",
                    "4_1",
                    null,
            };

            _attendRanges = new HashMap();
            for (final Iterator it = _semesterMap.keySet().iterator(); it.hasNext();) {
                final String semester = (String) it.next();
                final Semester oSemester = (Semester) _semesterMap.get(semester);
                _attendRanges.put(semester, oSemester._dateRange);
            }
        }

        private String setZ010Name1(DB2UDB db2) {
            String name1 = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement("SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' ");
                rs = ps.executeQuery();
                if (rs.next()) {
                    name1 = rs.getString("NAME1");
                }
            } catch (SQLException ex) {
                log.debug("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return name1;
        }

        private static String getDbPrginfoProperties(final DB2UDB db2, final String propName) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJD187E' AND NAME = '" + propName + "' "));
        }

        /**
         * 写真データ格納フォルダの取得 --NO001
         */
        private KNJ_Control.ReturnVal getImagepath(final DB2UDB db2) {
            KNJ_Control.ReturnVal returnval = null;
            try {
                KNJ_Control imagepath_extension = new KNJ_Control(); // 取得クラスのインスタンス作成
                returnval = imagepath_extension.Control(db2);
            } catch (Exception ex) {
                log.error("getDocumentroot error!", ex);
            }
            return returnval;
        }

        public String getSlashPath() {
            final String path = _documentroot + "/" + (null == _imagePath || "".equals(_imagePath) ? "" : _imagePath + "/") + "slash.jpg";
            final boolean exists = new java.io.File(path).exists();
            log.info(" image path " + path + " exists? " + exists);
            if (exists) {
                return path;
            }
            return null;
        }

        /**
         * 年度の開始日を取得する
         */
        private Map loadSemester(final DB2UDB db2, final String year, final String grade) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map map = new HashMap();
            try {
                final String sql = "select"
                        + "   SEMESTER,"
                        + "   SEMESTERNAME,"
                        + "   SDATE,"
                        + "   EDATE"
                        + " from"
                        + "   V_SEMESTER_GRADE_MST"
                        + " where"
                        + "   YEAR='" + year + "'"
                        + "   AND GRADE='" + grade + "'"
                        + " order by SEMESTER"
                    ;

                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    map.put(rs.getString("SEMESTER"), new Semester(rs.getString("SEMESTER"), rs.getString("SEMESTERNAME"), rs.getString("SDATE"), rs.getString("EDATE")));
                }
            } catch (final Exception ex) {
                log.error("テスト項目のロードでエラー", ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return map;
        }

        private String getGradeCd(final DB2UDB db2) {
            StringBuffer sql = new StringBuffer();
            sql.append(" SELECT GRADE_CD FROM SCHREG_REGD_GDAT ");
            sql.append(" WHERE YEAR = '" + _ctrlYear + "' AND GRADE = '" + _grade + "' ");
            log.debug("gdat sql = " + sql.toString());

            return KnjDbUtils.getOne(KnjDbUtils.query(db2, sql.toString()));
        }

        private String getSchoolKind(final DB2UDB db2) {
            StringBuffer sql = new StringBuffer();
            sql.append(" SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT ");
            sql.append(" WHERE YEAR = '" + _ctrlYear + "' AND GRADE = '" + _grade + "' ");

            return KnjDbUtils.getOne(KnjDbUtils.query(db2, sql.toString()));
        }


        private void setCertifSchoolDat(final DB2UDB db2) {
            final String kindCd = "J".equals(_schoolKind) ? "103" : "104";
            StringBuffer sql = new StringBuffer();
            sql.append(" SELECT SCHOOL_NAME, JOB_NAME, PRINCIPAL_NAME, REMARK2, REMARK4, REMARK5, REMARK6 FROM CERTIF_SCHOOL_DAT ");
            sql.append(" WHERE YEAR = '" + _ctrlYear + "' AND CERTIF_KINDCD = '" + kindCd + "' ");
            log.debug("certif_school_dat sql = " + sql.toString());

            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql.toString()));

            _schoolName = KnjDbUtils.getString(row, "SCHOOL_NAME");

            _schoolName = StringUtils.defaultString(_schoolName);
        }

        private SubclassMst getSubclassMst(final String subclasscd) {
            if (null == _subclassMstMap.get(subclasscd)) {
                String classcd = null;
                if (null != subclasscd) {
                    final String[] split = StringUtils.split(subclasscd, "-");
                    if (null != split && split.length > 2) {
                        classcd = split[0] + "-" + split[1];
                    } else {
                        classcd = subclasscd;
                    }
                }
                return new SubclassMst(classcd, subclasscd, null, null, null, null, new Integer(99999), new Integer(99999));
            }
            return (SubclassMst) _subclassMstMap.get(subclasscd);
        }

        private void setSubclassMst(
                final DB2UDB db2
        ) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            _subclassMstMap = new HashMap();
            try {
                String sql = "";
                sql += " SELECT ";
                sql += " T1.CLASSCD || '-' || T1.SCHOOL_KIND AS CLASSCD, ";
                sql += " T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ";
                sql += " T1.SUBCLASSCD AS SUBCLASSCD, T2.CLASSABBV, T2.CLASSNAME, T1.SUBCLASSABBV, VALUE(T1.SUBCLASSORDERNAME2, T1.SUBCLASSNAME) AS SUBCLASSNAME, ";
                sql += " VALUE(T1.SHOWORDER3, 999) AS SUBCLASS_SHOWORDER3, ";
                sql += " VALUE(T2.SHOWORDER3, 999) AS CLASS_SHOWORDER3 ";
                sql += " FROM SUBCLASS_MST T1 ";
                sql += " LEFT JOIN CLASS_MST T2 ON T2.CLASSCD = T1.CLASSCD AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final Integer classShoworder3 = Integer.valueOf(rs.getString("CLASS_SHOWORDER3"));
                    final Integer subclassShoworder3 = Integer.valueOf(rs.getString("SUBCLASS_SHOWORDER3"));
                    final SubclassMst mst = new SubclassMst(rs.getString("CLASSCD"), rs.getString("SUBCLASSCD"), rs.getString("CLASSABBV"), rs.getString("CLASSNAME"), rs.getString("SUBCLASSABBV"), rs.getString("SUBCLASSNAME"), classShoworder3, subclassShoworder3);
                    _subclassMstMap.put(rs.getString("SUBCLASSCD"), mst);
                }

            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }

            String sql1 = "";
            sql1 += " SELECT ";
            sql1 += "    COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || COMBINED_SUBCLASSCD AS COMBINED_SUBCLASSCD, ";
            sql1 += "    ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ATTEND_SUBCLASSCD AS ATTEND_SUBCLASSCD ";
            sql1 += " FROM SUBCLASS_REPLACE_COMBINED_DAT WHERE YEAR = '" + _ctrlYear + "' ";

            for (final Iterator it = KnjDbUtils.query(db2, sql1).iterator(); it.hasNext();) {
                final Map row = (Map) it.next();
                final String combiendSubclasscd = KnjDbUtils.getString(row, "COMBINED_SUBCLASSCD");
                final String attendSubclasscd = KnjDbUtils.getString(row, "ATTEND_SUBCLASSCD");

                SubclassMst combinedSubclassMst = (SubclassMst) _subclassMstMap.get(combiendSubclasscd);
                SubclassMst attendSubclassMst = (SubclassMst) _subclassMstMap.get(attendSubclasscd);
                if (null != combinedSubclassMst && null != attendSubclassMst) {
                    attendSubclassMst._combinedSubclassMst = combinedSubclassMst;
                    combinedSubclassMst._isSaki = true;
                }
            }
        }

        private Map getYearDate(final DB2UDB db2, final String year) {
            Map rtn = new HashMap();
            final String sql = " SELECT "
                    + "     SDATE, EDATE "
                    + " FROM "
                    + "     SEMESTER_MST "
                    + " WHERE "
                    + "     YEAR = '" + year + "' "
                    + "     AND SEMESTER = '" + SEMEALL + "' ";

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn.put("SDATE", rs.getString("SDATE"));
                    rtn.put("EDATE", rs.getString("EDATE"));
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtn;
        }
    }
}
