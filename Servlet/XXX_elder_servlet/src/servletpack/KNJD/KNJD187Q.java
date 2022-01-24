/**     プロジェクト/プロパティ/javaコード・スタイル/コード・テンプレート/パターン       ***/

/*
 * 作成日: 2018/09/21
 * 作成者: yamashiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditKinsoku;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

public class KNJD187Q {

    private static final Log log = LogFactory.getLog(KNJD186Q.class);

    private static final String SEMEALL = "9";

    private boolean _hasData;

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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        final List<Student> printList = Student.getList(db2, _param);

        final String title = KNJ_EditDate.gengou(db2, Integer.parseInt(_param._ctrlYear)) + "年度　" + _param._semesterName + "　成績通知票";
        for (final Student student : printList) {

            svf.VrSetForm("KNJD187Q.frm", 4);
            svf.VrsOut("TITLE", title);
            svf.VrsOut("SCHREGNO", student._schregNo);
            svf.VrsOut("HR_NAME", student._hrName);
            svf.VrsOut("NAME", student._name);
            svf.VrsOut("SCHOOL_NAME", _param._schoolName);
            svf.VrsOut("TR_NAME", student._trName);

            printSvfHReport(svf, student);

            final List<String> yearList = student.getYearList();
            printAttend(db2, svf, student, yearList);

            for (int yi = 0; yi < yearList.size(); yi++) {
                final String year = yearList.get(yi);
                svf.VrsOut("NENDO1_" + String.valueOf(yi + 1), KNJ_EditDate.gengou(db2, Integer.parseInt(year)) + "年度"); // 年度
            }

            String creditZenseki = null;
            for (final Map.Entry<String, String> e : student._creditMap.entrySet()) {
                creditZenseki = add(creditZenseki, e.getValue());
            }
            String creditHonkou = null;
            final Map<String, String> yearCreditHonkouMap = new HashMap<String, String>();
            for (final Subclass subclass : student._subclassList) {
                for (final Map.Entry<String, Score> e : subclass._yearScoreMap.entrySet()) {
                    final Score score = e.getValue();
                    creditHonkou = add(creditHonkou, score._credit);
                    yearCreditHonkouMap.put(e.getKey(), add(yearCreditHonkouMap.get(e.getKey()), score._credit));
                }
            }

            svf.VrsOutn("TOTAL_CREDIT_ALL", 1, creditZenseki); // 修得単位数
            svf.VrsOutn("TOTAL_CREDIT_ALL", 2, creditHonkou); // 修得単位数
            svf.VrsOutn("TOTAL_CREDIT_ALL", 3, add(creditZenseki, creditHonkou)); // 修得単位数

            final int max = 46;
            int printCount = 0;
            for (final Subclass subclass : student._subclassList) {
                svf.VrsOut("CLASS_NAME", subclass._className);
                svf.VrsOut("SUBCLASS_NAME", subclass._subclassName);

                for (int yi = 0; yi < yearList.size(); yi++) {
                    final String year = yearList.get(yi);
                    final Score score = subclass._yearScoreMap.get(year);

                    if (null != score) {
                        final String ssi = "1".equals(subclass._zenki) ? "1" : "2";
                        svf.VrsOut("VALUE" + String.valueOf(yi + 1) + "_" + ssi, score._score); // 評定
                        svf.VrsOut("CREDIT" + String.valueOf(yi + 1), score._credit); // 単位数
                    }
                }
                printCount += 1;
                svf.VrEndRecord();
            }

            for (int i = printCount; i < max - 1; i++) {
                svf.VrsOut("BLANK", "1"); // 空行用
                svf.VrEndRecord();
            }

            svf.VrsOut("TOTAL_VALUE_NAME", "計"); // 合計名称
            for (int yi = 0; yi < yearList.size(); yi++) {
                final String year = yearList.get(yi);
                svf.VrsOut("TOTAL_CREDIT" + String.valueOf(yi + 1), yearCreditHonkouMap.get(year)); // 合計単位数
            }
            svf.VrEndRecord();

            _hasData = true;
        }
    }

    private void printAttend(final DB2UDB db2, final Vrw32alp svf, final Student student, final List<String> yearList) {
        for (int i = 0; i < yearList.size(); i++) {
            final int line = i + 1;
            final String year = yearList.get(i);
            svf.VrsOutn("NENDO2", line, KNJ_EditDate.gengou(db2, Integer.parseInt(year)) + "年度"); // 年度
            final Attendance att = student._attendMap.get(year);
            if (null != att) {
                svf.VrsOutn("LESSON", line, String.valueOf(att._lesson)); // 授業日数
                svf.VrsOutn("SUSPEND", line, String.valueOf(att._suspendMourning)); // 出停忌引
                svf.VrsOutn("ABSENT", line, String.valueOf(att._absent)); // 欠席日数
                svf.VrsOutn("ATTEND", line, String.valueOf(att._present)); // 出席日数
            }
        }
    }

    /**
     * 『通信欄』を印字する
     * @param svf
     * @param student
     */
    private void printSvfHReport(final Vrw32alp svf, final Student student) {

        final int pcharsttCM = getParamSizeNum(_param._HREPORTREMARK_DAT_COMMUNICATION_SIZE, 0);
        final int plinesttCM = getParamSizeNum(_param._HREPORTREMARK_DAT_COMMUNICATION_SIZE, 1);
        final int charsttCM = (-1 == pcharsttCM || -1 == plinesttCM) ? 40 : pcharsttCM;
        final int linesttCM = (-1 == pcharsttCM || -1 == plinesttCM) ?  7 : plinesttCM;

        for (final HReportRemarkDat hReportRemarkDat : student._hReportRemarkDatList) {

            //通信欄
            VrsOutnRenban(svf, "COMM", KNJ_EditKinsoku.getTokenList(hReportRemarkDat._communication, charsttCM * 2, linesttCM));
        }
    }

    /**
     * "[w] * [h]"サイズタイプのパラメータのwもしくはhを整数で返す
     * @param param サイズタイプのパラメータ文字列
     * @param pos split後のインデクス (0:w, 1:h)
     * @return "[w] * [h]"サイズタイプのパラメータのwもしくはhの整数値
     */
    private static int getParamSizeNum(final String param, final int pos) {
        int num = -1;
        String[] nums = StringUtils.split(param, " * ");
        if (StringUtils.isBlank(param) || !(0 <= pos && pos < nums.length)) {
            num = -1;
        } else {
            try {
                num = Integer.valueOf(nums[pos]).intValue();
            } catch (Exception e) {
                log.error("Exception!", e);
            }
        }
        return num;
    }

    private void VrsOutnRenban(final Vrw32alp svf, final String field, final List<String> list) {
        for (int i = 0 ; i < list.size(); i++) {
            svf.VrsOutn(field, i + 1, list.get(i));
        }
    }

    private String add(final String v1, final String v2) {
        if (!NumberUtils.isDigits(v1)) { return v2; }
        if (!NumberUtils.isDigits(v2)) { return v1; }
        return String.valueOf(Integer.parseInt(v1) + Integer.parseInt(v2));
    }

    private static class Student {
        final String _schregNo;
        final String _hrName;
        final String _trName;
        final String _attendNo;
        final String _name;
        final List<Subclass> _subclassList;
        final List<HReportRemarkDat> _hReportRemarkDatList;
        final Map<String, String> _creditMap = new TreeMap<String, String>();
        final Map<String, Attendance> _attendMap = new TreeMap<String, Attendance>();
        public Student(final String schregNo, final String hrName, final String trName, final String attendNo, final String name) {
            _schregNo = schregNo;
            _hrName = hrName;
            _trName = trName;
            _attendNo = attendNo;
            _name = name;
            _subclassList = new ArrayList();
            _hReportRemarkDatList = new ArrayList();
        }

        public List<String> getYearList() {
            final Set<String> years = new TreeSet<String>();
            for (final Subclass s : _subclassList) {
                years.addAll(s._yearScoreMap.keySet());
            }
            years.addAll(_attendMap.keySet());
            List<String> list = new ArrayList<String>(years);
            final int max = 4;
            if (list.size() > max) {
                list = list.subList(list.size() - max, list.size());
            }
            return list;
        }

        private static List<Student> getList(final DB2UDB db2, final Param param) throws SQLException {
            final List<Student> retList = new ArrayList();
            final String sql = getStudentSql(param);

            for (final Map<String, String> row : KnjDbUtils.query(db2, sql)) {
                final String schregNo = KnjDbUtils.getString(row, "SCHREGNO");
                final String hrName = KnjDbUtils.getString(row, "HR_NAME");
                final String trName = KnjDbUtils.getString(row, "STAFFNAME");
                final String attendNo = KnjDbUtils.getString(row, "ATTENDNO");
                final String name = KnjDbUtils.getString(row, "NAME");

                final Student student = new Student(schregNo, hrName, trName, attendNo, name);
                retList.add(student);
            }

            setScore(db2, param, retList);
            setAttend(db2, param, retList);
            getHReportRemarkDatList(db2, param, retList);

            return retList;
        }

        private static String getStudentSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     REGD.SCHREGNO, ");
            stb.append("     REGDH.HR_NAME, ");
            stb.append("     STAFF.STAFFNAME, ");
            stb.append("     REGD.ATTENDNO, ");
            stb.append("     BASE.NAME ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT REGD ");
            stb.append("     LEFT JOIN SCHREG_REGD_HDAT REGDH ON REGD.YEAR = REGDH.YEAR ");
            stb.append("          AND REGD.SEMESTER = REGDH.SEMESTER ");
            stb.append("          AND REGD.GRADE = REGDH.GRADE ");
            stb.append("          AND REGD.HR_CLASS = REGDH.HR_CLASS ");
            stb.append("     LEFT JOIN STAFF_MST STAFF ON STAFF.STAFFCD = REGDH.TR_CD1 ");
            stb.append("     INNER JOIN SCHREG_BASE_MST BASE ON REGD.SCHREGNO = BASE.SCHREGNO ");
            stb.append(" WHERE ");
            stb.append("     REGD.YEAR = '" + param._ctrlYear + "' ");
            stb.append("     AND REGD.SEMESTER = '" + param._ctrlSemester + "' ");
            stb.append("     AND REGD.GRADE || REGD.HR_CLASS IN " + SQLUtils.whereIn(true, param._classSelected) + " ");
            stb.append("     AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST S1 ");
            stb.append("                    WHERE ");
            stb.append("                        S1.SCHREGNO = BASE.SCHREGNO ");
            stb.append("                        AND ((S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < '" + param._date + "') ");
            stb.append("                              OR ");
            stb.append("                             (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE > '" + param._date + "')) ");
            stb.append("                   ) ");
            stb.append("     AND NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT S1 ");
            stb.append("                    WHERE ");
            stb.append("                        S1.SCHREGNO = BASE.SCHREGNO ");
            stb.append("                        AND S1.TRANSFERCD IN ('2') ");
            stb.append("                        AND '" + param._date + "' BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE ");
            stb.append("                   ) ");
            stb.append(" ORDER BY ");
            stb.append("     REGD.GRADE, ");
            stb.append("     REGD.HR_CLASS, ");
            stb.append("     REGD.ATTENDNO ");

            return stb.toString();
        }

        private static void setScore(final DB2UDB db2, final Param param, final List<Student> studentList) throws SQLException {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH T_ZENKI AS ( ");
            stb.append("     SELECT ");
            stb.append("         CDAT.CLASSCD ");
            stb.append("       , CDAT.SCHOOL_KIND ");
            stb.append("       , CDAT.CURRICULUM_CD ");
            stb.append("       , CDAT.SUBCLASSCD ");
            stb.append("       , CASE WHEN MIN(VALUE(TAKESEMES, '0')) = '1' THEN '1' END AS ZENKI ");
            stb.append("     FROM CHAIR_DAT CDAT ");
            stb.append("     WHERE YEAR = '" + param._ctrlYear + "' ");
            stb.append("       AND SEMESTER <= '" + param._ctrlSemester + "' ");
            stb.append("     GROUP BY ");
            stb.append("         CDAT.CLASSCD ");
            stb.append("       , CDAT.SCHOOL_KIND ");
            stb.append("       , CDAT.CURRICULUM_CD ");
            stb.append("       , CDAT.SUBCLASSCD ");
            stb.append(" ), SUBM AS ( ");
            stb.append(" SELECT DISTINCT ");
            stb.append("     CSTD.YEAR, ");
            stb.append("     '0' AS SCHOOLCD, "); // 本校
            stb.append("     Z1.ZENKI, ");
            stb.append("     CSTD.SCHREGNO, ");
            stb.append("     CDAT.CLASSCD, ");
            stb.append("     CDAT.SCHOOL_KIND, ");
            stb.append("     CDAT.CURRICULUM_CD, ");
            stb.append("     CDAT.SUBCLASSCD, ");
            stb.append("     CLM.CLASSNAME, ");
            stb.append("     SUBM.SUBCLASSNAME, ");
            stb.append("     PROV.PROV_FLG ");
            stb.append(" FROM ");
            stb.append("     CHAIR_STD_DAT CSTD ");
            stb.append("     INNER JOIN CHAIR_DAT CDAT ON CSTD.YEAR = CDAT.YEAR ");
            stb.append("           AND CSTD.SEMESTER = CDAT.SEMESTER ");
            stb.append("           AND CSTD.CHAIRCD = CDAT.CHAIRCD ");
            stb.append("           AND CDAT.CLASSCD != '81' ");
            stb.append("           AND CDAT.CLASSCD <= '90' ");
            stb.append("     INNER JOIN T_ZENKI Z1 ON Z1.CLASSCD = CDAT.CLASSCD ");
            stb.append("          AND Z1.SCHOOL_KIND = CDAT.SCHOOL_KIND ");
            stb.append("          AND Z1.CURRICULUM_CD = CDAT.CURRICULUM_CD ");
            stb.append("          AND Z1.SUBCLASSCD = CDAT.SUBCLASSCD ");
            if ("1".equals(param._printZenki)) {
                stb.append("           AND Z1.ZENKI = '1' ");
            }
            if ("1".equals(param._printSubclassLastChairStd)) {
                stb.append("     INNER JOIN SEMESTER_MST STDSEME ON STDSEME.YEAR = CSTD.YEAR ");
                stb.append("       AND STDSEME.SEMESTER = CSTD.SEMESTER ");
                stb.append("       AND STDSEME.EDATE = CSTD.APPENDDATE ");
            }
            stb.append("     LEFT JOIN CLASS_MST CLM ON CDAT.CLASSCD = CLM.CLASSCD ");
            stb.append("          AND CDAT.SCHOOL_KIND = CLM.SCHOOL_KIND ");
            stb.append("     LEFT JOIN SUBCLASS_MST SUBM ON CDAT.CLASSCD = SUBM.CLASSCD ");
            stb.append("          AND CDAT.SCHOOL_KIND = SUBM.SCHOOL_KIND ");
            stb.append("          AND CDAT.CURRICULUM_CD = SUBM.CURRICULUM_CD ");
            stb.append("          AND CDAT.SUBCLASSCD = SUBM.SUBCLASSCD ");
            stb.append("     LEFT JOIN RECORD_PROV_FLG_DAT PROV ON CDAT.YEAR = PROV.YEAR ");
            stb.append("          AND CDAT.CLASSCD = PROV.CLASSCD ");
            stb.append("          AND CDAT.SCHOOL_KIND = PROV.SCHOOL_KIND ");
            stb.append("          AND CDAT.CURRICULUM_CD = PROV.CURRICULUM_CD ");
            stb.append("          AND CDAT.SUBCLASSCD = PROV.SUBCLASSCD ");
            stb.append("          AND CSTD.SCHREGNO = PROV.SCHREGNO ");
            stb.append(" WHERE ");
            stb.append("     CSTD.YEAR = '" + param._ctrlYear + "' ");
            stb.append("     AND CSTD.SEMESTER <= '" + param._ctrlSemester + "' ");
            stb.append("     AND CSTD.SCHREGNO = ? ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     SUBM.YEAR, ");
            stb.append("     SUBM.SCHOOLCD, ");
            stb.append("     SUBM.ZENKI, ");
            stb.append("     SUBM.CLASSCD || '-' || SUBM.SCHOOL_KIND || '-' || SUBM.CURRICULUM_CD || '-' || SUBM.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("     SUBM.CLASSNAME, ");
            stb.append("     SUBM.SUBCLASSNAME, ");
            stb.append("     SCORE.SCORE, ");
            stb.append("     SCORE.GET_CREDIT AS CREDIT, ");
            stb.append("     SUBM.PROV_FLG ");
            stb.append(" FROM ");
            stb.append("     SUBM ");
            stb.append("     LEFT JOIN RECORD_SCORE_DAT SCORE ON SCORE.YEAR = SUBM.YEAR ");
            stb.append("          AND SCORE.SEMESTER = '9' ");
            stb.append("          AND SCORE.TESTKINDCD = '99' ");
            stb.append("          AND SCORE.TESTITEMCD = '00' ");
            stb.append("          AND SCORE.SCORE_DIV = '09' ");
            stb.append("          AND SCORE.CLASSCD = SUBM.CLASSCD ");
            stb.append("          AND SCORE.SCHOOL_KIND = SUBM.SCHOOL_KIND ");
            stb.append("          AND SCORE.CURRICULUM_CD = SUBM.CURRICULUM_CD ");
            stb.append("          AND SCORE.SUBCLASSCD = SUBM.SUBCLASSCD ");
            stb.append("          AND SCORE.SCHREGNO = SUBM.SCHREGNO ");
            stb.append(" UNION ALL ");
            stb.append(" SELECT ");
            stb.append("     T1.YEAR, ");
            stb.append("     T1.SCHOOLCD, ");
            stb.append("     Z1.ZENKI, ");
            stb.append("     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("     VALUE(T1.CLASSNAME, CLM.CLASSORDERNAME1, CLM.CLASSNAME) AS CLASSNAME, ");
            stb.append("     VALUE(T1.SUBCLASSNAME, SUBM.SUBCLASSORDERNAME1, SUBM.SUBCLASSNAME) AS SUBCLASSNAME, ");
            stb.append("     T1.VALUATION AS SCORE, ");
            stb.append("     T1.GET_CREDIT AS CREDIT, ");
            stb.append("     CAST(NULL AS VARCHAR(1)) AS PROV_FLG ");
            stb.append(" FROM ");
            stb.append("     SCHREG_STUDYREC_DAT T1 ");
            stb.append("     LEFT JOIN T_ZENKI Z1 ON Z1.CLASSCD = T1.CLASSCD ");
            stb.append("          AND Z1.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("          AND Z1.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("          AND Z1.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("     LEFT JOIN CLASS_MST CLM ON T1.CLASSCD = CLM.CLASSCD ");
            stb.append("          AND T1.SCHOOL_KIND = CLM.SCHOOL_KIND ");
            stb.append("     LEFT JOIN SUBCLASS_MST SUBM ON T1.CLASSCD = SUBM.CLASSCD ");
            stb.append("          AND T1.SCHOOL_KIND = SUBM.SCHOOL_KIND ");
            stb.append("          AND T1.CURRICULUM_CD = SUBM.CURRICULUM_CD ");
            stb.append("          AND T1.SUBCLASSCD = SUBM.SUBCLASSCD ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR < '" + param._ctrlYear + "' ");
            stb.append("     AND (T1.SCHOOLCD = '0' OR T1.SCHOOLCD = '1') ");
            stb.append("     AND T1.SCHREGNO = ? ");
            stb.append(" ORDER BY ");
            stb.append("     SUBCLASSCD, ");
            stb.append("     YEAR DESC ");

            String sql = stb.toString();
            PreparedStatement ps = null;
            try {
                ps = db2.prepareStatement(sql);
                for (final Student student : studentList) {

                    final Map<String, Subclass> subclassMap = new HashMap();
                    for (final Map<String, String> row : KnjDbUtils.query(db2, ps, new Object[] {student._schregNo, student._schregNo})) {
                        final String schoolcd = KnjDbUtils.getString(row, "SCHOOLCD");
                        final String year = KnjDbUtils.getString(row, "YEAR");
                        final String subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");
                        final String credit = KnjDbUtils.getString(row, "CREDIT");
                        if ("1".equals(schoolcd)) {
                            student._creditMap.put(year + "-" + subclasscd, credit);
                            continue;
                        }
                        if (!subclassMap.containsKey(subclasscd)) {
                            final String className = KnjDbUtils.getString(row, "CLASSNAME");
                            final String subclassName = KnjDbUtils.getString(row, "SUBCLASSNAME");
                            final Subclass subclass = new Subclass(subclasscd, className, subclassName, KnjDbUtils.getString(row, "ZENKI"));
                            student._subclassList.add(subclass);
                            subclassMap.put(subclasscd, subclass);
                        }

                        subclassMap.get(subclasscd)._yearScoreMap.put(year, new Score(KnjDbUtils.getString(row, "SCORE"), credit, KnjDbUtils.getString(row, "PROV_FLG")));
                    }
                }
            } catch (SQLException ex) {
                log.error("exception! sql = " + sql, ex);
                throw ex;
            } finally {
                DbUtils.closeQuietly(ps);
            }
        }

        private static void setAttend(final DB2UDB db2, final Param param, final List<Student> studentList) throws SQLException {

            PreparedStatement ps = null;
            try {
                param._attendParamMap.put("schregno", "?");

                final String sql = AttendAccumulate.getAttendSemesSql(
                        param._ctrlYear,
                        param._ctrlSemester,
                        null,
                        param._date,
                        param._attendParamMap
                );
                log.debug(" attend sql = " + sql);

                ps = db2.prepareStatement(sql);
                for (final Student student : studentList) {

                    for (final Map<String, String> row : KnjDbUtils.query(db2, ps, new Object[] {student._schregNo})) {
                        if (!SEMEALL.equals(KnjDbUtils.getString(row, "SEMESTER"))) {
                            continue;
                        }

                        final Attendance attendance = new Attendance(
                                KnjDbUtils.getInt(row, "LESSON", 0),
                                KnjDbUtils.getInt(row, "SUSPEND", 0) + KnjDbUtils.getInt(row, "MOURNING", 0),
                                KnjDbUtils.getInt(row, "SICK", 0),
                                KnjDbUtils.getInt(row, "PRESENT", 0)
                        );
                        student._attendMap.put(param._ctrlYear, attendance);
                    }
                }
            } catch (SQLException ex) {
                throw ex;
            } finally {
                DbUtils.closeQuietly(ps);
            }

            try {
                final StringBuffer sql = new StringBuffer();
                sql.append(" SELECT ");
                sql.append("     T1.YEAR,");
                sql.append("     VALUE(CLASSDAYS,0) AS CLASSDAYS,");                           //授業日数
                sql.append("     CASE WHEN S1.SEM_OFFDAYS = '1' ");
                sql.append("          THEN VALUE(CLASSDAYS,0) ");
                sql.append("          ELSE VALUE(CLASSDAYS,0) - VALUE(OFFDAYS,0) ");
                sql.append("          END AS ATTEND_1,"); //授業日数-休学日数
                sql.append("     VALUE(SUSPEND,0) AS SUSPEND,");                               //出停
                sql.append("     VALUE(MOURNING,0) AS MOURNING,");                             //忌引
                sql.append("     VALUE(REQUIREPRESENT,0) + CASE WHEN S1.SEM_OFFDAYS = '1' ");
                sql.append("          THEN VALUE(OFFDAYS,0) ");
                sql.append("          ELSE 0 ");
                sql.append("          END AS REQUIREPRESENT,"); //要出席日数
                sql.append("     VALUE(SICK,0) + VALUE(ACCIDENTNOTICE,0) + VALUE(NOACCIDENTNOTICE,0) + CASE WHEN S1.SEM_OFFDAYS = '1' ");
                sql.append("          THEN VALUE(OFFDAYS,0) ");
                sql.append("          ELSE 0 ");
                sql.append("          END AS SICK,"); //病欠＋事故欠（届・無）
                sql.append("     VALUE(PRESENT,0) AS PRESENT ");  //出席日数
                sql.append(" FROM ");
                sql.append("     (");
                sql.append("         SELECT ");
                sql.append("             SCHREGNO,");
                sql.append("             MAX(SCHOOLCD) AS SCHOOLCD,");
                sql.append("             YEAR,");
                sql.append("             SUM(CLASSDAYS) AS CLASSDAYS,");
                sql.append("             SUM(OFFDAYS) AS OFFDAYS,");
                sql.append("             SUM(ABSENT) AS ABSENT,");
                sql.append("             SUM(SUSPEND) AS SUSPEND,");
                sql.append("             SUM(MOURNING) AS MOURNING,");
                sql.append("             SUM(REQUIREPRESENT) AS REQUIREPRESENT,");
                sql.append("             SUM(SICK) AS SICK,");
                sql.append("             SUM(ACCIDENTNOTICE) AS ACCIDENTNOTICE,");
                sql.append("             SUM(NOACCIDENTNOTICE) AS NOACCIDENTNOTICE,");
                sql.append("             SUM(PRESENT) AS PRESENT ");
                sql.append("         FROM ");
                sql.append("             SCHREG_ATTENDREC_DAT ");
                sql.append("         WHERE ");
                sql.append("                 SCHREGNO = ? ");
                sql.append("             AND YEAR < ? ");
                sql.append("             AND SCHOOLCD <> '1' ");
                sql.append("         GROUP BY ");
                sql.append("             SCHREGNO,");
                sql.append("             ANNUAL,");
                sql.append("             YEAR ");
                sql.append("    )T1 ");
                sql.append("    LEFT JOIN SCHOOL_MST S1 ON S1.YEAR = T1.YEAR ");
                sql.append("    AND S1.SCHOOL_KIND = 'H' ");
                sql.append(" ORDER BY ");
                sql.append("     T1.YEAR ");

                log.debug(" attend sql = " + sql);

                ps = db2.prepareStatement(sql.toString());
                for (final Student student : studentList) {

                    for (final Map<String, String> row : KnjDbUtils.query(db2, ps, new Object[] {student._schregNo, param._ctrlYear})) {

                        final Attendance attendance = new Attendance(
                                KnjDbUtils.getInt(row, "ATTEND_1", 0),
                                KnjDbUtils.getInt(row, "SUSPEND", 0) + KnjDbUtils.getInt(row, "MOURNING", 0),
                                KnjDbUtils.getInt(row, "SICK", 0),
                                KnjDbUtils.getInt(row, "PRESENT", 0)
                        );
                        student._attendMap.put(KnjDbUtils.getString(row, "YEAR"), attendance);
                    }
                }
            } catch (SQLException ex) {
                throw ex;
            } finally {
                DbUtils.closeQuietly(ps);
            }
        }

        private static void getHReportRemarkDatList(final DB2UDB db2, final Param param, final List<Student> studentList) throws SQLException {
            final String sql = getHReportRemarkSql(param);
            PreparedStatement ps = null;
            try {
                ps = db2.prepareStatement(sql);
                for (final Student student : studentList) {
                    for (final Map<String, String> row : KnjDbUtils.query(db2, ps, new Object[] {student._schregNo})) {
                        final String semester = KnjDbUtils.getString(row, "SEMESTER");
                        final String totalstudytime = KnjDbUtils.getString(row, "TOTALSTUDYTIME");
                        final String specialactremark = KnjDbUtils.getString(row, "SPECIALACTREMARK");
                        final String communication = KnjDbUtils.getString(row, "COMMUNICATION");
                        final String remark1 = KnjDbUtils.getString(row, "REMARK1");
                        final String remark2 = KnjDbUtils.getString(row, "REMARK2");
                        final String remark3 = KnjDbUtils.getString(row, "REMARK3");
                        final String attendrecRemark = KnjDbUtils.getString(row, "ATTENDREC_REMARK");

                        final HReportRemarkDat hReportRemarkDat = new HReportRemarkDat(semester, totalstudytime, specialactremark, communication,
                                remark1, remark2, remark3, attendrecRemark);
                        student._hReportRemarkDatList.add(hReportRemarkDat);
                    }
                }
            } catch (SQLException e) {
                throw e;
            } finally {
                DbUtils.closeQuietly(ps);
            }
        }

        private static String getHReportRemarkSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.* ");
            stb.append(" FROM ");
            stb.append("     HREPORTREMARK_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._ctrlYear + "' ");
            stb.append("     AND T1.SEMESTER = '" + param._ctrlSemester + "' ");
            stb.append("     AND T1.SCHREGNO = ? ");
            return stb.toString();
        }

    }

    private static class Subclass {
        final String _subclasscd;
        final String _className;
        final String _subclassName;
        final String _zenki;
        final Map<String, Score> _yearScoreMap = new TreeMap<String, Score>();
        public Subclass(
                final String subclasscd,
                final String className,
                final String subclassName,
                final String zenki
        ) {
            _subclasscd = subclasscd;
            _className = className;
            _subclassName = subclassName;
            _zenki = zenki;
        }
    }

    private static class Attendance {

        final int _lesson;
        final int _suspendMourning;
        final int _absent;
        final int _present;
        Attendance(
                final int lesson,
                final int suspendMourning,
                final int absent,
                final int present
        ) {
            _lesson = lesson;
            _suspendMourning = suspendMourning;
            _absent = absent;
            _present = present;
        }

        Attendance add(final Attendance att) {
            return new Attendance(
                    _lesson + att._lesson,
                    _suspendMourning + att._suspendMourning,
                    _absent + att._absent,
                    _present + att._present
                    );
        }

        public String toString() {
            return "Att(les=" + _lesson + ",susp" + _suspendMourning + ",absent=" + _absent + ",prese=" + _present + ")";
        }
    }

    private static class Score {
        final String _score;
        final String _credit;
        final String _provFlg;
        public Score(
                final String score,
                final String credit,
                final String provFlg
        ) {
            _score = score;
            _credit = StringUtils.defaultString(credit);
            _provFlg = provFlg;
        }
    }

    /**
     * 通知表所見
     */
    private static class HReportRemarkDat {
        final String _semester;
        final String _totalstudytime;     // 総合的な学習の時間
        final String _specialactremark;   // 特別活動の記録・その他
        final String _communication;      // 担任からの所見
        final String _remark1;            // 特別活動の記録・学級活動
        final String _remark2;            // 特別活動の記録・生徒会活動
        final String _remark3;            // 特別活動の記録・学校行事
        final String _attendrecRemark;    // 出欠備考

        public HReportRemarkDat(
                final String semester,
                final String totalstudytime,
                final String specialactremark,
                final String communication,
                final String remark1,
                final String remark2,
                final String remark3,
                final String attendrecRemark) {
            _semester = semester;
            _totalstudytime = totalstudytime;
            _specialactremark = specialactremark;
            _communication = communication;
            _remark1 = remark1;
            _remark2 = remark2;
            _remark3 = remark3;
            _attendrecRemark = attendrecRemark;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 77012 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String[] _classSelected;
        private final String _date;
        final String _ctrlDate;
        final String _ctrlSemester;
        final String _ctrlYear;
        final String _schoolName;
        final String _semesterName;
        final String _HREPORTREMARK_DAT_COMMUNICATION_SIZE;
        final String _printZenki; // 1: 前期のみ出力
        /** 講座名簿の終了日付が学期の終了日付と同一 */
        final String _printSubclassLastChairStd;
        final Map _attendParamMap;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _classSelected = request.getParameterValues("CLASS_SELECTED");
            _date = KNJ_EditDate.H_Format_Haifun( request.getParameter("DATE") );
            _ctrlDate = request.getParameter("CTRL_DATE");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _printZenki = request.getParameter("PRINT_ZENKI");
            _printSubclassLastChairStd = request.getParameter("printSubclassLastChairStd");
            _schoolName = getSchoolName(db2);
            _semesterName = getSemesterName(db2);
            _HREPORTREMARK_DAT_COMMUNICATION_SIZE = null == request.getParameter("HREPORTREMARK_DAT_COMMUNICATION_SIZE_H") ? "" : StringUtils.replace(request.getParameter("HREPORTREMARK_DAT_COMMUNICATION_SIZE_H"), "+", " ");                      //通信欄

            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
            _attendParamMap.put("useTestCountflg", "TESTITEM_MST_COUNTFLG_NEW_SDIV");
        }

        private String getSchoolName(final DB2UDB db2) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT SCHOOL_NAME FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _ctrlYear + "' AND CERTIF_KINDCD = '104' "));
        }

        private String getSemesterName(final DB2UDB db2) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT SEMESTERNAME FROM SEMESTER_MST WHERE YEAR = '" + _ctrlYear + "' AND SEMESTER = '" + _ctrlSemester + "'"));
        }

    }
}

// eof
