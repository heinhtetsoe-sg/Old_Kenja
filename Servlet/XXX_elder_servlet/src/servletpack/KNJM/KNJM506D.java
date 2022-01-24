/**     プロジェクト/プロパティ/javaコード・スタイル/コード・テンプレート/パターン       ***/

/*
 * $Id: 0f2f042c9a660ed59e3404698cb1c174829716d3 $
 *
 * 作成日: 2018/06/04
 * 作成者: yamashiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJM;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJM506D {

    private static final Log log = LogFactory.getLog(KNJM506D.class);

    private static final String TEST_HYOUTEI = "990009";
    private boolean _hasData;
    private int pageAllCnt = 0;

    private Param _param;

    /**
     * @param request リクエスト
     * @param response レスポンス
     */
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {

        final Vrw32alp svf = new Vrw32alp();
        DB2UDB db2 = null;
        try {
            response.setContentType("application/pdf");

            svf.VrInit();
            svf.VrSetSpoolFileStream(response.getOutputStream());

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);

            _hasData = false;

            printMain(db2, svf);
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {

            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }

            if (null != db2) {
                db2.commit();
                db2.close();
            }
            svf.VrQuit();
        }

    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        final List students = createStudents(db2, _param);

        String befGradeHr = "";
        int stdCnt = 1;
        int pageCnt = 0;
        for (final Iterator it = students.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            if (!befGradeHr.equals(student._grade + student._hrClass)) {
                svf.VrSetForm("KNJM506D.frm", 4);
                svf.VrsOut("TITLE", KNJ_EditDate.gengou(db2, Integer.parseInt(_param._ctrlYear)) + "年度　" + _param._semesterName + "単位認定表");
                svf.VrsOut("TUTOR", "チューター：" + student._hrName);
                stdCnt = 1;
                pageCnt++;
            } else if (stdCnt > 2) {
                svf.VrSetForm("KNJM506D.frm", 4);
                stdCnt = 1;
                pageCnt++;
            }

            String setField = "1";
            if (stdCnt == 2) {
                setField = "2";
            }

            svf.VrsOut("SCHREGNO" + setField, student._schregno);
            final String nameField = KNJ_EditEdit.getMS932ByteLength(student._name) > 20 ? "_3" : KNJ_EditEdit.getMS932ByteLength(student._name) > 14 ? "_2" : "_1";
            svf.VrsOut("NAME" + setField + nameField, student._name);
            final String kanaField = KNJ_EditEdit.getMS932ByteLength(student._kana) > 30 ? "_3" : KNJ_EditEdit.getMS932ByteLength(student._kana) > 20 ? "_2" : "_1";
            svf.VrsOut("KANA" + setField + kanaField, student._kana);
            svf.VrsOut("REGD" + setField, student._regdMonth);
            svf.VrsOut("COMP_CREDIT" + setField, String.valueOf(student._getCredit));
            svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(db2, _param._printDate));
            svf.VrsOut("PAGE", pageCnt + "/" + pageAllCnt + " ページ");

            int totalCredits = 0;
            for (Iterator itSubclass = student._subclassMap.keySet().iterator(); itSubclass.hasNext();) {
                final String subclassCd = (String) itSubclass.next();
                final Score score = (Score) student._subclassMap.get(subclassCd);
                svf.VrsOut("SUBCLASS_CD" + setField, score._classcd + "-" + score._schoolKind + "-" + score._curriculumCd + "-" + score._subclassCd);
                final String subNameField = KNJ_EditEdit.getMS932ByteLength(score._subclassName) > 30 ? "_2" : "_1";
                svf.VrsOut("SUBCLASS_NAME"  + setField + subNameField, score._subclassName);
                svf.VrsOut("TR_NAME" + setField, score._staffName);
                svf.VrsOut("CREDIT" + setField, score._credits);
                svf.VrsOut("SCHOOLING" + setField, score._schoolingLimitCnt);
                svf.VrsOut("INTERVIEW" + setField, score._schoolingCnt);
                svf.VrsOut("REPORT" + setField, score._repoLimitCnt);
                svf.VrsOut("CORRECT" + setField, score._reportCnt);
                svf.VrsOut("VALUE" + setField, score._value);
                svf.VrsOut("GET_CREDIT" + setField, score.getCredit(score._getCredit));
                if (!"".equals(score._getCredit)) {
                    totalCredits += Integer.parseInt(score._getCredit);
                }
                svf.VrEndRecord();
            }
            svf.VrsOut("TOTAL_GET_CREDIT_NAME" + setField, _param._semesterName + "修得単位数");
            svf.VrsOut("TOTAL_GET_CREDIT" + setField, String.valueOf(totalCredits));
            svf.VrEndRecord();

            befGradeHr = student._grade + student._hrClass;
            stdCnt++;

            _hasData = true;
        }
    }

    private List createStudents(final DB2UDB db2, final Param param) {

        final List students = Student.getStudenList(db2, param, this);
        if (students.size() == 0) {
            log.warn("対象の生徒がいません");
            return students;
        }
        Score.setScore(db2, students, param);

        return students;
    }

    private static class Student {

        private String _grade;
        private String _hrClass;
        private String _hrName;
        private String _regdMonth;
        private String _name;
        private String _kana;
        private String _staffName;
        private int _getCredit;
        final String _schregno;

        private int _totalCredit = 0;

        final Map _subclassMap = new HashMap();

        public Student(final String schregno) {
            _schregno = schregno;
        }

        public String toString() {
            return _schregno + ":" + _name;
        }

        static List getStudenList(final DB2UDB db2, final Param param, KNJM506D knjm506d) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final List students = new ArrayList();
            try {
                final String sql = sqlStudentData(param);
                log.debug("sqlRegdData = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                String befGradeHr = "";
                int hrStdCnt = 0;

                while (rs.next()) {

                    final Student student = new Student(rs.getString("SCHREGNO"));
                    students.add(student);
                    student._grade = rs.getString("GRADE");
                    student._hrClass = rs.getString("HR_CLASS");
                    student._hrName = StringUtils.defaultString(rs.getString("HR_NAME"));
                    student._name =rs.getString("NAME");
                    student._kana =rs.getString("NAME_KANA");
                    student._regdMonth = StringUtils.defaultString(rs.getString("CNT"));
                    student._staffName = StringUtils.defaultString(rs.getString("STAFFNAME"));
                    student._getCredit = rs.getInt("GET_CREDIT");

                    if (!"".equals(befGradeHr) && !befGradeHr.equals(student._grade + student._hrClass)) {
                        final int setCal1 = hrStdCnt / 2;
                        final int setCal2 = hrStdCnt % 2;
                        knjm506d.pageAllCnt += setCal1 + setCal2;
                        hrStdCnt = 0;
                    }

                    befGradeHr = student._grade + student._hrClass;
                    hrStdCnt++;
                    //log.debug("対象の生徒" + student);
                }
                final int setCal1 = hrStdCnt / 2;
                final int setCal2 = hrStdCnt % 2;
                knjm506d.pageAllCnt += setCal1 + setCal2;
            } catch (Exception ex) {
                log.error("printSvfMain read error! ", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return students;
        }

        private static String sqlStudentData(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH BASE_T AS ( ");
            stb.append(" SELECT ");
            stb.append("     SCHREGNO, ");
            stb.append("     ((YEAR('" + param._printDate + "') - YEAR(ENT_DATE)) * 12) ");
            stb.append("     + ");
            stb.append("     (MONTH('" + param._printDate + "') - MONTH(ENT_DATE) + 1) ");
            stb.append("     AS CNT ");
            stb.append(" FROM ");
            stb.append("     SCHREG_BASE_MST ");
            stb.append(" WHERE ");
            stb.append("     ENT_DATE <= '" + param._printDate + "' ");
            stb.append(" ), KYUUGAKU_T AS ( ");
            stb.append(" SELECT ");
            stb.append("     SCHREGNO, ");
            stb.append("     SUM( ");
            stb.append("         ((YEAR(CASE WHEN '" + param._printDate + "' < TRANSFER_EDATE THEN '" + param._printDate + "' ELSE TRANSFER_EDATE END) - YEAR(TRANSFER_SDATE)) * 12) ");
            stb.append("         + ");
            stb.append("         (MONTH(CASE WHEN '" + param._printDate + "' < TRANSFER_EDATE THEN '" + param._printDate + "' ELSE TRANSFER_EDATE END) - MONTH(TRANSFER_SDATE) + 1) ");
            stb.append("     ) AS CNT ");
            stb.append(" FROM ");
            stb.append("     SCHREG_TRANSFER_DAT ");
            stb.append(" WHERE ");
            stb.append("     TRANSFERCD = '2' ");
            stb.append("     AND TRANSFER_SDATE <= '" + param._printDate + "' ");
            stb.append(" GROUP BY ");
            stb.append("     SCHREGNO ");
            stb.append(" ), ANOTHER_T AS ( ");
            stb.append(" SELECT ");
            stb.append("     SCHREGNO, ");
            stb.append("     SUM(MONTH_CNT) AS CNT ");
            stb.append(" FROM ");
            stb.append("     ANOTHER_SCHOOL_HIST_DAT ");
            stb.append(" GROUP BY ");
            stb.append("     SCHREGNO ");
            stb.append(" ), REGD_MONTH AS ( ");
            stb.append(" SELECT ");
            stb.append("     BASE_T.SCHREGNO, ");
            stb.append("     VALUE(BASE_T.CNT, 0) - VALUE(KYUUGAKU_T.CNT, 0) + VALUE(ANOTHER_T.CNT, 0) AS CNT ");
            stb.append(" FROM ");
            stb.append("     BASE_T ");
            stb.append("     LEFT JOIN KYUUGAKU_T ON KYUUGAKU_T.SCHREGNO = BASE_T.SCHREGNO ");
            stb.append("     LEFT JOIN ANOTHER_T ON ANOTHER_T.SCHREGNO = BASE_T.SCHREGNO ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     REGD.GRADE, ");
            stb.append("     REGD.HR_CLASS, ");
            stb.append("     REGD.SCHREGNO, ");
            stb.append("     BASE.NAME, ");
            stb.append("     BASE.NAME_KANA, ");
            stb.append("     REGD_MONTH.CNT, ");
            stb.append("     REGDH.HR_NAME, ");
            stb.append("     STAFF.STAFFNAME, ");
            stb.append("     CASE WHEN STUDYREC.GET_CREDIT IS NOT NULL OR SCORE.GET_CREDIT IS NOT NULL THEN VALUE(STUDYREC.GET_CREDIT, 0) + VALUE(SCORE.GET_CREDIT, 0) END AS GET_CREDIT ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT REGD ");
            stb.append("     INNER JOIN SCHREG_BASE_MST BASE ON REGD.SCHREGNO = BASE.SCHREGNO ");
            stb.append("           AND BASE.ENT_DATE <= '" + param._printDate + "' ");
            stb.append("     LEFT JOIN SCHREG_REGD_HDAT REGDH ON REGD.YEAR = REGDH.YEAR ");
            stb.append("          AND REGD.SEMESTER = REGDH.SEMESTER ");
            stb.append("          AND REGD.GRADE = REGDH.GRADE ");
            stb.append("          AND REGD.HR_CLASS = REGDH.HR_CLASS ");
            stb.append("     LEFT JOIN STAFF_MST STAFF ON REGDH.TR_CD1 = STAFF.STAFFCD ");
            stb.append("     LEFT JOIN (SELECT SCHREGNO, SUM(GET_CREDIT) AS GET_CREDIT FROM SCHREG_STUDYREC_DAT WHERE YEAR < '" + param._ctrlYear + "' ");
            stb.append("          GROUP BY SCHREGNO) STUDYREC ON REGD.SCHREGNO = STUDYREC.SCHREGNO ");
            stb.append("     LEFT JOIN REGD_MONTH ON REGD.SCHREGNO = REGD_MONTH.SCHREGNO ");
            stb.append("     LEFT JOIN (SELECT SCHREGNO, SUM(GET_CREDIT) AS GET_CREDIT FROM V_RECORD_SCORE_HIST_DAT WHERE YEAR = '" + param._ctrlYear + "' ");
            stb.append("          AND SEMESTER < '" + param._semester + "' ");
            stb.append("          AND TESTKINDCD || TESTITEMCD || SCORE_DIV = '" + TEST_HYOUTEI + "' ");
            stb.append("          GROUP BY SCHREGNO) SCORE ON REGD.SCHREGNO = SCORE.SCHREGNO ");
            stb.append(" WHERE ");
            stb.append("     REGD.YEAR = '" + param._ctrlYear + "' ");
            stb.append("     AND REGD.SEMESTER = '" + param._semester + "' ");
            if ("1".equals(param._choice)) {
                stb.append("     AND REGD.GRADE || REGD.HR_CLASS IN " + SQLUtils.whereIn(true, param._categorySelected) + " ");
            } else {
                stb.append("     AND REGD.SCHREGNO IN " + SQLUtils.whereIn(true, param._categorySelected) + " ");
            }
            stb.append(" GROUP BY ");
            stb.append("     REGD.GRADE, ");
            stb.append("     REGD.HR_CLASS, ");
            stb.append("     REGD.SCHREGNO, ");
            stb.append("     BASE.NAME, ");
            stb.append("     BASE.NAME_KANA, ");
            stb.append("     REGD_MONTH.CNT, ");
            stb.append("     REGDH.HR_NAME, ");
            stb.append("     STAFF.STAFFNAME, ");
            stb.append("     STUDYREC.GET_CREDIT, ");
            stb.append("     SCORE.GET_CREDIT ");
            stb.append(" ORDER BY ");
            stb.append("     REGD.GRADE, ");
            stb.append("     REGD.HR_CLASS, ");
            stb.append("     REGD.SCHREGNO ");
            return stb.toString();
        }
    }

    private static class Subclass {
        final String _schregno;
        final String _subclassCd;

        final Map _testScoreMap = new HashMap();
        final Map _attendMap = new HashMap();

        public Subclass(
                final String schregno,
                final String subclassCd
        ) {
            _schregno = schregno;
            _subclassCd = subclassCd;
        }

        public String toString() {
            return _schregno + " : " + _subclassCd;
        }
    }

    private static class Score {

        String _classcd;
        String _schoolKind;
        String _curriculumCd;
        String _subclassCd;
        String _subclassName;
        String _staffName;
        String _credits;
        String _schoolingLimitCnt;
        String _schoolingCnt;
        String _repoLimitCnt;
        String _reportCnt;
        String _value;
        String _getCredit;


        public String toString() {
            return " Score " + " (" + _classcd + " , [" + _schoolingLimitCnt + " , " + _schoolingCnt + " , " + _repoLimitCnt + " , " + _reportCnt + "], val = [" + _value + ", " + _getCredit + "]) )";
        }

        public String getCredit(final String credit) {
            String retStr = credit;
            if ("".equals(credit)) {
                retStr = "****";
            }
            return retStr;
        }

        /**
         * 素点、平均をセットする。
         * @param db2
         * @param student
         * @param param
         */
        private static void setScore(final DB2UDB db2, final Collection students, final Param param) {

            final String sql = sqlRecordScoreHist(param);
            log.debug("setScoreValue sql = " + sql);

            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                ps = db2.prepareStatement(sql);

                for (final Iterator it = students.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        final String subclassCd = rs.getString("CLASSCD") + rs.getString("SCHOOL_KIND") + rs.getString("CURRICULUM_CD") + rs.getString("SUBCLASSCD");

                        if (!student._subclassMap.containsKey(subclassCd)) {
                            student._subclassMap.put(subclassCd, new Score());
                        }
                        final Score subScore = (Score) student._subclassMap.get(subclassCd);

                        subScore._classcd = rs.getString("CLASSCD");
                        subScore._schoolKind = rs.getString("SCHOOL_KIND");
                        subScore._curriculumCd = rs.getString("CURRICULUM_CD");
                        subScore._subclassCd = rs.getString("SUBCLASSCD");
                        subScore._subclassName = rs.getString("SUBCLASSNAME");
                        subScore._staffName = StringUtils.defaultString(rs.getString("STAFFNAME"));
                        subScore._credits = StringUtils.defaultString(rs.getString("CREDITS"));
                        subScore._schoolingLimitCnt = StringUtils.defaultString(rs.getString("SCHOOLING_LIMIT_CNT"));
                        subScore._schoolingCnt = StringUtils.defaultString(rs.getString("SCHOOLING_CNT"));
                        subScore._repoLimitCnt = StringUtils.defaultString(rs.getString("REPO_LIMIT_CNT"));
                        subScore._reportCnt = StringUtils.defaultString(rs.getString("REPORT_CNT"));
                        subScore._value = StringUtils.defaultString(rs.getString("VALUE"));
                        subScore._getCredit = StringUtils.defaultString(rs.getString("GET_CREDIT"));
                        student._getCredit += "".equals(subScore._getCredit) ? 0 : Integer.parseInt(subScore._getCredit);

                    }
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private static String sqlRecordScoreHist(final Param param) {

            final StringBuffer stb = new StringBuffer();

            stb.append(" WITH CHAIRT_STF AS ( ");
            stb.append(" SELECT ");
            stb.append("     CHAIRCD, ");
            stb.append("     MIN(STAFFCD) AS STAFFCD ");
            stb.append(" FROM ");
            stb.append("     CHAIR_STF_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + param._ctrlYear + "' ");
            stb.append("     AND SEMESTER = '" + param._semester + "' ");
            stb.append("     AND CHARGEDIV = 1 ");
            stb.append(" GROUP BY ");
            stb.append("     CHAIRCD ");
            stb.append(" ), CSTD AS ( ");
            stb.append(" SELECT ");
            stb.append("     CSTD.YEAR, ");
            stb.append("     CSTD.SEMESTER, ");
            stb.append("     CSTD.SCHREGNO, ");
            stb.append("     CSTD.CHAIRCD, ");
            stb.append("     CDAT.CLASSCD, ");
            stb.append("     CDAT.SCHOOL_KIND, ");
            stb.append("     CDAT.CURRICULUM_CD, ");
            stb.append("     CDAT.SUBCLASSCD ");
            stb.append(" FROM ");
            stb.append("     CHAIR_STD_DAT CSTD ");
            stb.append("     LEFT JOIN CHAIR_DAT CDAT ON CSTD.YEAR = CDAT.YEAR ");
            stb.append("          AND CSTD.SEMESTER = CDAT.SEMESTER ");
            stb.append("          AND CSTD.CHAIRCD = CDAT.CHAIRCD ");
            stb.append(" WHERE ");
            stb.append("     CSTD.YEAR = '" + param._ctrlYear + "' ");
            stb.append("     AND CSTD.SEMESTER = '" + param._semester + "' ");
            stb.append("     AND CSTD.SCHREGNO = ? ");
            stb.append(" GROUP BY ");
            stb.append("     CSTD.YEAR, ");
            stb.append("     CSTD.SEMESTER, ");
            stb.append("     CSTD.SCHREGNO, ");
            stb.append("     CSTD.CHAIRCD, ");
            stb.append("     CDAT.CLASSCD, ");
            stb.append("     CDAT.SCHOOL_KIND, ");
            stb.append("     CDAT.CURRICULUM_CD, ");
            stb.append("     CDAT.SUBCLASSCD ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     CSTD.CLASSCD, ");
            stb.append("     CSTD.SCHOOL_KIND, ");
            stb.append("     CSTD.CURRICULUM_CD, ");
            stb.append("     CSTD.SUBCLASSCD, ");
            stb.append("     SUBC.SUBCLASSNAME, ");
            stb.append("     STAFF.STAFFNAME, ");
            stb.append("     CRE.CREDITS, ");
            stb.append("     CORRES_SEM.SCHOOLING_LIMIT_CNT, ");
            stb.append("     ATTEND_SEM.SCHOOLING_CNT, ");
            stb.append("     CORRES_SEM.REPO_LIMIT_CNT, ");
            stb.append("     REPORT_SEM.REPORT_CNT, ");
            stb.append("     SCORE.VALUE, ");
            stb.append("     SCORE.GET_CREDIT ");
            stb.append(" FROM ");
            stb.append("     CSTD ");
            stb.append("     LEFT JOIN V_RECORD_SCORE_HIST_DAT SCORE ON CSTD.YEAR = SCORE.YEAR ");
            stb.append("          AND CSTD.SEMESTER = SCORE.SEMESTER ");
            stb.append("          AND SCORE.TESTKINDCD || SCORE.TESTITEMCD || SCORE.SCORE_DIV = '" + TEST_HYOUTEI + "' ");
            stb.append("          AND CSTD.CLASSCD = SCORE.CLASSCD ");
            stb.append("          AND CSTD.SCHOOL_KIND = SCORE.SCHOOL_KIND ");
            stb.append("          AND CSTD.CURRICULUM_CD = SCORE.CURRICULUM_CD ");
            stb.append("          AND CSTD.SUBCLASSCD = SCORE.SUBCLASSCD ");
            stb.append("          AND CSTD.SCHREGNO = SCORE.SCHREGNO ");
            stb.append("     LEFT JOIN SUBCLASS_MST SUBC ON CSTD.CLASSCD = SUBC.CLASSCD ");
            stb.append("          AND CSTD.SCHOOL_KIND = SUBC.SCHOOL_KIND ");
            stb.append("          AND CSTD.CURRICULUM_CD = SUBC.CURRICULUM_CD ");
            stb.append("          AND CSTD.SUBCLASSCD = SUBC.SUBCLASSCD ");
            stb.append("     LEFT JOIN CHAIRT_STF ON CSTD.CHAIRCD = CHAIRT_STF.CHAIRCD ");
            stb.append("     LEFT JOIN STAFF_MST STAFF ON CHAIRT_STF.STAFFCD = STAFF.STAFFCD ");
            stb.append("     LEFT JOIN SCHREG_REGD_DAT REGD ON CSTD.YEAR = REGD.YEAR ");
            stb.append("          AND CSTD.SEMESTER = REGD.SEMESTER ");
            stb.append("          AND CSTD.SCHREGNO = REGD.SCHREGNO ");
            stb.append("     LEFT JOIN CREDIT_MST CRE ON REGD.YEAR = CRE.YEAR ");
            stb.append("          AND REGD.COURSECD = CRE.COURSECD ");
            stb.append("          AND REGD.MAJORCD = CRE.MAJORCD ");
            stb.append("          AND REGD.GRADE = CRE.GRADE ");
            stb.append("          AND REGD.COURSECODE = CRE.COURSECODE ");
            stb.append("          AND CSTD.CLASSCD = CRE.CLASSCD ");
            stb.append("          AND CSTD.SCHOOL_KIND = CRE.SCHOOL_KIND ");
            stb.append("          AND CSTD.CURRICULUM_CD = CRE.CURRICULUM_CD ");
            stb.append("          AND CSTD.SUBCLASSCD = CRE.SUBCLASSCD ");
            stb.append("     LEFT JOIN CHAIR_CORRES_SEMES_DAT CORRES_SEM ON CSTD.YEAR = CORRES_SEM.YEAR ");
            stb.append("          AND CSTD.SEMESTER = CORRES_SEM.SEMESTER ");
            stb.append("          AND CSTD.CHAIRCD = CORRES_SEM.CHAIRCD ");
            stb.append("          AND CSTD.CLASSCD = CORRES_SEM.CLASSCD ");
            stb.append("          AND CSTD.SCHOOL_KIND = CORRES_SEM.SCHOOL_KIND ");
            stb.append("          AND CSTD.CURRICULUM_CD = CORRES_SEM.CURRICULUM_CD ");
            stb.append("          AND CSTD.SUBCLASSCD = CORRES_SEM.SUBCLASSCD ");
            stb.append("     LEFT JOIN SCH_ATTEND_SEMES_DAT ATTEND_SEM ON CSTD.YEAR = ATTEND_SEM.YEAR ");
            stb.append("          AND CSTD.SEMESTER = ATTEND_SEM.SEMESTER ");
            stb.append("          AND CSTD.SCHREGNO = ATTEND_SEM.SCHREGNO ");
            stb.append("          AND CSTD.CHAIRCD = ATTEND_SEM.CHAIRCD ");
            stb.append("     LEFT JOIN REP_PRESENT_SEMES_DAT REPORT_SEM ON CSTD.YEAR = REPORT_SEM.YEAR ");
            stb.append("          AND CSTD.SEMESTER = REPORT_SEM.SEMESTER ");
            stb.append("          AND CSTD.CLASSCD = REPORT_SEM.CLASSCD ");
            stb.append("          AND CSTD.SCHOOL_KIND = REPORT_SEM.SCHOOL_KIND ");
            stb.append("          AND CSTD.CURRICULUM_CD = REPORT_SEM.CURRICULUM_CD ");
            stb.append("          AND CSTD.SUBCLASSCD = REPORT_SEM.SUBCLASSCD ");
            stb.append("          AND CSTD.SCHREGNO = REPORT_SEM.SCHREGNO ");
            stb.append(" ORDER BY ");
            stb.append("     CSTD.CLASSCD, ");
            stb.append("     CSTD.SCHOOL_KIND, ");
            stb.append("     CSTD.CURRICULUM_CD, ");
            stb.append("     CSTD.SUBCLASSCD ");
            return stb.toString();
        }

    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 65892 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _semester;
        private final String _semesterName;
        private final String _choice;
        private final String _printDate;
        private final String[] _categorySelected;
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _semester = request.getParameter("SEMESTER");
            _choice = request.getParameter("CHOICE");
            _printDate = request.getParameter("PRINT_DATE").replace('/', '-');
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("LOGIN_DATE");
            _semesterName = getSemesterName(db2);
        }

        private String getSemesterName(final DB2UDB db2) throws SQLException {
            String retStr = "";
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = "SELECT SEMESTERNAME FROM SEMESTER_MST WHERE YEAR = '" + _ctrlYear + "' AND SEMESTER = '" + _semester + "'";
                ps = db2.prepareStatement(sql);

                rs = ps.executeQuery();
                while (rs.next()) {
                    retStr = rs.getString("SEMESTERNAME");
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retStr;
        }

    }
}

// eof
