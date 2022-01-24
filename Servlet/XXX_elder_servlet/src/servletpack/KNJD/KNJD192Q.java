// kanji=漢字
/*
 * $Id: f4371f46628c0488c65cd98ce36230dba7434327 $
 *
 * 作成日: 2009/07/22 17:54:00 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2009 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJServletpacksvfANDdb2;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: f4371f46628c0488c65cd98ce36230dba7434327 $
 */
public class KNJD192Q {

    private static final Log log = LogFactory.getLog("KNJD192V.class");

    private Param _param;

    private static final String SPECIAL_ALL = "999";

    private static final String SEMEALL = "9";

    private static final String SUBCLASS3 = "333333";
    private static final String SUBCLASS5 = "555555";
    private static final String SUBCLASS9 = "999999";

    private static final String SIDOU_INPUT_INF_MARK = "1";
    private static final String SIDOU_INPUT_INF_SCORE = "2";

    private static final String AMIKAKE_ATTR = "Paint=(1,80,1),Bold=1";
    private static final String HYOTEI_TESTCD = "9990009";

    public void svf_out(HttpServletRequest request, HttpServletResponse response) throws Exception {
        final KNJServletpacksvfANDdb2 sd = new KNJServletpacksvfANDdb2();  //帳票におけるＳＶＦおよびＤＢ２の設定
        DB2UDB db2 = null; //Databaseクラスを継承したクラス
        Vrw32alp svf = new Vrw32alp();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        boolean hasData = false;
        try {

            // print svf設定
            sd.setSvfInit(request, response, svf);
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);

            //SVF出力
            hasData = printMain(db2, svf);
        } catch (Exception e) {
            log.fatal("exception!", e);
        } finally {
            sd.closeSvf(svf, hasData);
            sd.closeDb(db2);
        }

    }// doGetの括り

    private List getPageList(final List studentList, final int maxLine) {
        final List rtn = new ArrayList();
        List current = null;
        String befGradeClass = "";
        for (final Iterator iter = studentList.iterator(); iter.hasNext();) {
            final Student student = (Student) iter.next();
            if (null == current || current.size() >= maxLine || !befGradeClass.equals(student._grade + student._hrClass)) {
                current = new ArrayList();
                rtn.add(current);
            }
            current.add(student);
            befGradeClass = student._grade + student._hrClass;
        }
        return rtn;
    }

    private static boolean isSubclassAll(final String subclassCd) {
        return SUBCLASS3.equals(subclassCd) || SUBCLASS5.equals(subclassCd) || SUBCLASS9.equals(subclassCd) || "99999A".equals(subclassCd) || "99999B".equals(subclassCd);
    }

    private static String getSubclassCd(final ResultSet rs, final Param param) throws SQLException {
        if (isSubclassAll(rs.getString("SUBCLASSCD"))) {
            return rs.getString("SUBCLASSCD");
        }
        final String subclassCd;
        subclassCd = rs.getString("CLASSCD") + "-" + rs.getString("SCHOOL_KIND") + "-" + rs.getString("CURRICULUM_CD") + "-" + rs.getString("SUBCLASSCD");
        return subclassCd;
    }

    private static String sishagonyu(final BigDecimal bd) {
        return null == bd ? null : bd.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
    }

    /*
     * 欠課数オーバー(授業時数の1/6以上)か
     */
    private static boolean isKekkaOver(final int kekka, final int jisu) {
        if (0 == kekka || 0 == jisu) { return false; }
        return new BigDecimal(kekka).compareTo(new BigDecimal(jisu).divide(new BigDecimal(6), 10, BigDecimal.ROUND_HALF_UP)) >= 0;
    }

    private List getStudentList(final DB2UDB db2) throws SQLException  {
        final List studentList = Student.getStudentList(db2, _param);

        Student.setMapTestSubclassAvg(db2, _param, studentList);
        TestScore.setChairStd(db2, _param, studentList);
        TestScore.setCreditMst(db2, _param, studentList);

        TestScore.setRank(db2, _param, studentList);
        TestScore.setAvg(db2, _param, studentList);
        if (!_param.isKetten()) {
            TestScore.setKetten(db2, _param, studentList);
        }
        Attendance.setAttendSubclass(db2, _param, studentList);
        Attendance.setAttendSemes(db2, _param, studentList);

        return studentList;
    }

    /**
     * @param db2
     */
    private boolean printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException, ParseException {
        boolean hasData = false;
        final List studentListAll = getStudentList(db2);
        log.debug(" studentList size = " + studentListAll.size());

        final String form;
        if ("1".equals(_param._printOnedayAttend)) {
            form = _param._printForm20 ? "KNJD192Q_2.frm" : "KNJD192Q.frm";
        } else {
            form = null;
        }

        final int maxLine = 4;
        final List pageList = getPageList(studentListAll, maxLine);
        for (final Iterator it = pageList.iterator(); it.hasNext();) {
            final List studentList = (List) it.next();

            svf.VrSetForm(form, 1);
            for (int gyo = 1; gyo <= maxLine; gyo++) {
                svf.VrsOutn("AVERAGE_TITLE1", gyo, "平均");
                svf.VrsOutn("CLS_AVG_NAME", gyo, _param.getRankName()); // 項目名（平均）
                if (!"1".equals(_param._notPrintRank)) {
                    svf.VrsOutn("CLS_RANK_NAME", gyo, _param.getRankName()); // 順位（項目名）
                }

                svf.VrsOutn("AVERAGE_TITLE1", gyo, "平均"); //
                svf.VrsOutn("KETTEN_TITLE1", gyo, "欠点");
                svf.VrsOutn("KETTEN_TITLE2", gyo, "科目数");
                svf.VrsOutn("CLS_AVG_NAME2_1", gyo, "コース"); // 平均
                svf.VrsOutn("CLS_AVG_NAME2_2", gyo, "平均"); // 平均
                svf.VrsOutn("CLS_RANK_NAME2_1", gyo, "コース"); // 順位
                svf.VrsOutn("CLS_RANK_NAME2_2", gyo, "順位"); // 順位
            }

            for (int gyo = 1; gyo <=  studentList.size(); gyo++) {
                final Student student = (Student) studentList.get(gyo - 1);

                setPrintOut(svf, student, gyo);

                hasData = true;
            }
            svf.VrEndPage();
        }
        return hasData;
    }

    private void setPrintOut(final Vrw32alp svf, final Student student, final int gyo) {
        svf.VrsOutn("NENDO", gyo, _param.changePrintYear());
        svf.VrsOutn("SEMESTER", gyo, _param._semesterName);
        svf.VrsOutn("TESTNAME", gyo, _param._testName);

        svf.VrsOutn("HR_NAME", gyo, student._hrName + "(" + student._attendNo + ")");
        svf.VrsOutn("NAME", gyo, student._name);
        if ("1".equals(_param._use_SchregNo_hyoji)) {
            svf.VrsOutn("SCHREGNO", gyo, student._schregno);
        }

        int kettenSubclassCount = 0;
        final List subclasscdList = new ArrayList(student._testSubclass.keySet());
        for (int j = 0; j < subclasscdList.size(); j++) {
            final String subclassCd = (String) subclasscdList.get(j);
            final TestScore testScore = (TestScore) student._testSubclass.get(subclassCd);
            final String col = String.valueOf(j + 1);

            final String credit = null != testScore._credit ? "(" + testScore._credit + ")" : "";
            if (getMS932ByteLength(testScore._subclassname) > 20) {
                final String[] token = KNJ_EditEdit.get_token(StringUtils.defaultString(testScore._subclassname) + credit, 10, 3);
                if (null != token) {
                    final String[] field = new String[] {"SUBCLASS" + col + "_2", "SUBCLASS" + col, "SUBCLASS" + col + "_3"};
                    for (int i = 0; i < Math.min(token.length,  field.length); i++) {
                        svf.VrsOutn(field[i], gyo, token[i]);
                    }
                }
            } else if (getMS932ByteLength(testScore._subclassname) > 10) {
                final String[] token = KNJ_EditEdit.get_token(testScore._subclassname, 10, 3);
                if (null != token) {
                    final String[] field = new String[] {"SUBCLASS" + col + "_2", "SUBCLASS" + col};
                    for (int i = 0; i < Math.min(token.length,  field.length); i++) {
                        svf.VrsOutn(field[i], gyo, token[i]);
                    }
                }
                svf.VrsOutn("SUBCLASS" + col + "_3", gyo, credit);
            } else {
                svf.VrsOutn("SUBCLASS" + col, gyo, testScore._subclassname);
                svf.VrsOutn("SUBCLASS" + col + "_3", gyo, credit);
            }
            if (student.isPrintSubclassScoreAvg(subclassCd, _param)) {

                svf.VrsOutn("SCORE" + col, gyo, testScore._score);
                if (!"欠".equals(testScore._score) && !"".equals(testScore._score) && null != testScore._score) {
                    final int score = Integer.parseInt(testScore._score);
                    if (testScore.isKetten(score, _param)) {
                        svf.VrAttributen("SCORE" + col, gyo, AMIKAKE_ATTR);
                        kettenSubclassCount += 1;
                    }
                }
            }

            svf.VrsOutn("ABSENT" + col, gyo, String.valueOf(testScore._kekka));
            if (isKekkaOver(testScore._kekka, testScore._jisu)) {
                svf.VrAttributen("ABSENT" + col, gyo, AMIKAKE_ATTR);
            }
            svf.VrsOutn("LESSON" + col, gyo, String.valueOf(testScore._jisu));

            if (student.isPrintSubclassScoreAvg(subclassCd, _param)) {
                svf.VrsOutn("CLASS_AVERAGE" + col, gyo, testScore._avg);
                if (!"1".equals(_param._notPrintRank)) {
                    svf.VrsOutn("CLASS_RANK" + col, gyo, testScore._rank);
                }
            }
        }

        if (student._hasScore) {
            svf.VrsOutn("TOTAL_SCORE", gyo, student._testAll._score); // 合計
            svf.VrsOutn("AVERAGEL_SCORE", gyo, student._testAll._average); // 平均
            svf.VrsOutn("FAIL", gyo, kettenSubclassCount == 0 ? "" : String.valueOf(kettenSubclassCount)); // 欠点科目数
            svf.VrsOutn("CLASS_AVERAGE", gyo, student._testAll._avg); // 学級平均
            if (!"1".equals(_param._notPrintRank)) {
                svf.VrsOutn("CLASS_RANK", gyo, String.valueOf(student._testAll._rank)); // 学級順位
                svf.VrsOutn("CLASS_STUDENT", gyo, String.valueOf(student._testAll._cnt)); // 学級人数
            }
        }

        //出欠情報
        svf.VrsOutn("LESSON", gyo, student._attendance._lesson); // 授業日数
        svf.VrsOutn("SUSPEND", gyo, student._attendance._suspend); // 出停日数
        svf.VrsOutn("MOURNING", gyo, student._attendance._mourning); // 忌引日数
        svf.VrsOutn("MLESSON", gyo, student._attendance._mlesson);
        svf.VrsOutn("SICK", gyo, student._attendance._sick);
        svf.VrsOutn("LATE", gyo, student._attendance._late);
        svf.VrsOutn("EARLY", gyo, student._attendance._early);

    }

    /**
     *  文字数を取得
     */
    private static int getMS932ByteLength(final String str) {
        int ret = 0;
        if (null != str) {
            try {
                ret = str.getBytes( "MS932").length;
            } catch (Exception ex) {
                log.error("retStringByteValue error!", ex);
            }
        }
        return ret;
    }

    private static class Attendance {
        private String _lesson;
        private String _suspend;
        private String _mourning;
        private String _mlesson;
        private String _sick;
        private String _late;
        private String _early;

        static void setAttendSemes(final DB2UDB db2, final Param _param, final List studentList) {

            final Map hrStudentListMap = new HashMap();
            for (final Iterator it = studentList.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                if (null == hrStudentListMap.get(student._grade + student._hrClass)) {
                    hrStudentListMap.put(student._grade + student._hrClass, new ArrayList());
                }
                final List hrStudentList = (List) hrStudentListMap.get(student._grade + student._hrClass);
                hrStudentList.add(student);
            }

            PreparedStatement ps = null;
            try {
                _param._attendParamMap.put("grade", "?");
                _param._attendParamMap.put("hrClass", "?");
                _param._attendParamMap.put("schregno", "?");
                _param._attendParamMap.put("groupByDiv", "SCHREGNO");
                final String sql = AttendAccumulate.getAttendSemesSql(
                                                        _param._year,
                                                        _param._semester,
                                                        _param._dateS,
                                                        _param._date,
                                                        _param._attendParamMap);

                ps = db2.prepareStatement(sql);

                for (final Iterator it = hrStudentListMap.keySet().iterator(); it.hasNext();) {
                    final String gradeHrclass = (String) it.next();

                    ps.setString(2, gradeHrclass.substring(0, 2));
                    ps.setString(3, gradeHrclass.substring(2));

                    final List hrStudentList = (List) hrStudentListMap.get(gradeHrclass);

                    for (final Iterator stit = hrStudentList.iterator(); stit.hasNext();) {

                        final Student student = (Student) stit.next();
                        ps.setString(1, student._schregno);

                        ResultSet rs = ps.executeQuery();
                        while (rs.next()) {
                            student._attendance._lesson = rs.getString("LESSON");
                            student._attendance._suspend = rs.getString("SUSPEND");
                            student._attendance._mourning = rs.getString("MOURNING");
                            student._attendance._mlesson = rs.getString("MLESSON");
                            student._attendance._sick = rs.getString("SICK");
                            student._attendance._late = String.valueOf(rs.getInt("LATE"));
                            student._attendance._early = String.valueOf(rs.getInt("EARLY"));
                        }
                        DbUtils.closeQuietly(rs);
                    }
                }
            } catch (Exception e) {
                log.fatal("exception!", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }

        private static void setAttendSubclass(final DB2UDB db2, final Param param, final List studentList) {

            final Map hrStudentListMap = new HashMap();
            for (final Iterator it = studentList.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                if (null == hrStudentListMap.get(student._grade + student._hrClass)) {
                    hrStudentListMap.put(student._grade + student._hrClass, new ArrayList());
                }
                final List hrStudentList = (List) hrStudentListMap.get(student._grade + student._hrClass);
                hrStudentList.add(student);
            }

            PreparedStatement ps = null;
            try {
                param._attendParamMap.put("grade", "?");
                param._attendParamMap.put("hrClass", "?");
                param._attendParamMap.put("schregno", "?");
                param._attendParamMap.put("useTestCountflg", "TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV");

                final String sql = AttendAccumulate.getAttendSubclassSql(
                        param._year,
                        param._semester,
                        param._dateS,
                        param._date,
                        param._attendParamMap);

                ps = db2.prepareStatement(sql);

                for (final Iterator hrit = hrStudentListMap.keySet().iterator(); hrit.hasNext();) {
                    final String gradeHrclass = (String) hrit.next();

                    ps.setString(2, gradeHrclass.substring(0, 2));
                    ps.setString(3, gradeHrclass.substring(2));

                    final List hrStudentList = (List) hrStudentListMap.get(gradeHrclass);

                    for (final Iterator stit = hrStudentList.iterator(); stit.hasNext();) {

                        final Student student = (Student) stit.next();
                        ps.setString(1, student._schregno);

                        ResultSet rs = ps.executeQuery();
                        while (rs.next()) {
                            if (!SEMEALL.equals(rs.getString("SEMESTER"))) {
                                continue;
                            }
                            final String subclassCd = rs.getString("SUBCLASSCD");
                            final int mlesson = rs.getInt("MLESSON");
                            final int kekka = rs.getInt("SICK2");
                            final int replacedKekka = rs.getInt("REPLACED_SICK");
                            if (student._testSubclass.containsKey(subclassCd)) {
                                final TestScore testScore = (TestScore) student._testSubclass.get(subclassCd);
                                testScore._jisu = mlesson;
                                if (null != testScore._combinedSubclasscd) {
                                    testScore._kekka = replacedKekka;
                                } else {
                                    testScore._kekka = kekka;
                                }
                            }
                        }

                        DbUtils.closeQuietly(rs);
                    }
                }

            } catch (Exception e) {
                log.fatal("exception!", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }

    }

    private static class Student {
        final String _schregno;
        final String _grade;
        final String _hrClass;
        final String _attendNo;
        final String _hrName;
        final String _hrNameAbbv;
        final String _name;
        final String _courseCd;
        final String _courseName;
        final String _majorCd;
        final String _majorName;
        final String _courseCode;
        final String _courseCodeName;
        final boolean _majorHas1Hr;
        final Map _testSubclass;
        final Map _testSubclassAvg;
        final Map _recordChkfinSubclass;
        final TestScore _testAll;
        boolean _hasScore;
        /** 個人の平均点 */
        String _averageScore;
        /** 出欠情報 */
        final Attendance _attendance = new Attendance();

        /**
         * コンストラクタ。
         */
        public Student(
                final String schregno,
                final String grade,
                final String hrClass,
                final String attendNo,
                final String hrName,
                final String hrNameAbbv,
                final String name,
                final String courseCd,
                final String courseName,
                final String majorCd,
                final String majorName,
                final String courseCode,
                final String courseCodeName,
                final boolean majorHas1Hr
                ) {
            _schregno = schregno;
            _grade = grade;
            _hrClass = hrClass;
            _attendNo = attendNo;
            _hrName = hrName;
            _hrNameAbbv = hrNameAbbv;
            _name = name;
            _courseCd = courseCd;
            _courseName = courseName;
            _majorCd = majorCd;
            _majorName = majorName;
            _courseCode = courseCode;
            _courseCodeName = courseCodeName;
            _majorHas1Hr = majorHas1Hr;
            _testSubclass = new TreeMap();
            _testSubclassAvg = new TreeMap();
            _recordChkfinSubclass = new TreeMap();
            _testAll = new TestScore();
            _testAll._score = "";
        }

        /** 指定科目コードの平均点を表示するか */
        public boolean isPrintSubclassScoreAvg(final String subclassCd, final Param param) {
            return true;
        }

        public String toString() {
            return "学籍：" + _schregno + " 名前：" + _name;
        }

        private static List getStudentList(final DB2UDB db2, final Param param) {
            final List studentList = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getStudentInfoSql(param);
                log.debug("getStudentInfoSql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final Student student = new Student(rs.getString("SCHREGNO"),
                                                  rs.getString("GRADE"),
                                                  rs.getString("HR_CLASS"),
                                                  rs.getString("ATTENDNO"),
                                                  rs.getString("HR_NAME"),
                                                  rs.getString("HR_NAMEABBV"),
                                                  "1".equals(rs.getString("USE_REAL_NAME")) ? rs.getString("REAL_NAME") : rs.getString("NAME"),
                                                  rs.getString("COURSECD"),
                                                  rs.getString("COURSENAME"),
                                                  rs.getString("MAJORCD"),
                                                  rs.getString("MAJORNAME"),
                                                  rs.getString("COURSECODE"),
                                                  rs.getString("COURSECODENAME"),
                                                  "1".equals(rs.getString("MAJOR_HAS_1_HR")));
                    studentList.add(student);
                }
            } catch (Exception e) {
                log.fatal("exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return studentList;
        }

        private static String getStudentInfoSql(final Param param) {
            final StringBuffer stb = new StringBuffer();

            stb.append(" WITH MAJOR_HR_COUNT AS ( ");
            stb.append(" SELECT ");
            stb.append("     VSCH.COURSECD, ");
            stb.append("     VSCH.MAJORCD, ");
            stb.append("     COUNT(DISTINCT HR_CLASS) AS MAJOR_HR_COUNT "); // 学科ごとのHR数
            stb.append(" FROM ");
            stb.append("     V_SCHREG_INFO VSCH ");
            stb.append(" WHERE ");
            stb.append("     VSCH.YEAR = '" + param._year + "' ");
            stb.append("     AND VSCH.SEMESTER = '" + param._schregSemester + "' ");
            stb.append("     AND VSCH.GRADE = '" + param._grade + "' ");
            stb.append(" GROUP BY ");
            stb.append("     VSCH.COURSECD, ");
            stb.append("     VSCH.MAJORCD ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     VSCH.SCHREGNO, ");
            stb.append("     VSCH.GRADE, ");
            stb.append("     VSCH.HR_CLASS, ");
            stb.append("     VSCH.ATTENDNO, ");
            stb.append("     VSCH.HR_NAME, ");
            stb.append("     VSCH.HR_NAMEABBV, ");
            stb.append("     VSCH.NAME, ");
            stb.append("     BASE.REAL_NAME, ");
            stb.append("     CASE WHEN L4.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END AS USE_REAL_NAME, ");
            stb.append("     VSCH.COURSECD, ");
            stb.append("     L1.COURSENAME, ");
            stb.append("     VSCH.MAJORCD, ");
            stb.append("     L1.MAJORNAME, ");
            stb.append("     VSCH.COURSECODE, ");
            stb.append("     L2.COURSECODENAME, ");
            stb.append("     CASE WHEN VALUE(MAJOR_HR_COUNT, 0) = 1 THEN '1' END AS MAJOR_HAS_1_HR ");
            stb.append(" FROM ");
            stb.append("     V_SCHREG_INFO VSCH ");
            stb.append("     INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = VSCH.SCHREGNO ");
            stb.append("     LEFT JOIN V_COURSE_MAJOR_MST L1 ON VSCH.YEAR = L1.YEAR ");
            stb.append("          AND VSCH.COURSECD = L1.COURSECD ");
            stb.append("          AND VSCH.MAJORCD = L1.MAJORCD ");
            stb.append("     LEFT JOIN V_COURSECODE_MST L2 ON VSCH.YEAR = L2.YEAR ");
            stb.append("          AND VSCH.COURSECODE = L2.COURSECODE ");
            stb.append("     LEFT JOIN GUARDIAN_DAT L3 ON VSCH.SCHREGNO = L3.SCHREGNO ");
            stb.append("     LEFT JOIN SCHREG_NAME_SETUP_DAT L4 ON L4.SCHREGNO = VSCH.SCHREGNO AND L4.DIV = '04' ");
            stb.append("     LEFT JOIN MAJOR_HR_COUNT L5 ON L5.COURSECD = VSCH.COURSECD AND L5.MAJORCD = VSCH.MAJORCD ");
            stb.append(" WHERE ");
            stb.append("     VSCH.YEAR = '" + param._year + "' ");
            stb.append("     AND VSCH.SEMESTER = '" + param._schregSemester + "' ");
            if ("1".equals(param._categoryIsClass)) {
                stb.append("     AND VSCH.GRADE = '" + param._grade + "' ");
                stb.append("     AND VSCH.HR_CLASS IN " + SQLUtils.whereIn(true, param._selectData) + " ");
            } else {
                stb.append("     AND VSCH.GRADE = '" + param._grade + "' ");
                stb.append("     AND VSCH.HR_CLASS = '" + param._hrClass + "' ");
                stb.append("     AND VSCH.SCHREGNO IN " + SQLUtils.whereIn(true, param._selectData) + " ");
            }
            stb.append(" ORDER BY ");
            stb.append("     VSCH.GRADE, ");
            stb.append("     VSCH.HR_CLASS, ");
            stb.append("     VSCH.ATTENDNO ");

            return stb.toString();
        }

        private static void setMapTestSubclassAvg(final DB2UDB db2, final Param param, final List studentList) throws SQLException {

            final Set grSet = new HashSet();
            for (final Iterator it = studentList.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                final String key = student._grade + student._hrClass;
                grSet.add(key);
            }

            final Map grAvgMap = new HashMap();

            final String sql = TestScore.getAverageSql(param._year, param._semester, "2", param._testcd);
            PreparedStatement ps = db2.prepareStatement(sql);

            // 学級の平均
            for (final Iterator it = grSet.iterator(); it.hasNext();) {
                final String key = (String) it.next();
                final Map avgMap = new HashMap();
                grAvgMap.put(key, avgMap);
                ResultSet rs = null;
                try {
                    ps.setString(1, key.substring(0, 2));
                    ps.setString(2, key.substring(2));
                    ps.setString(3, "00000000");
                    rs = ps.executeQuery();

                    while (rs.next()) {
                        final String subclassCd = getSubclassCd(rs, param);
                        final String avg = rs.getString("AVG");
                        avgMap.put(subclassCd, avg);
                    }
                } finally {
                    DbUtils.closeQuietly(rs);
                    db2.commit();
                }
            }
            DbUtils.closeQuietly(ps);

            for (final Iterator it = studentList.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                final String key = student._grade + student._hrClass;
                final Map avgMap = (Map) grAvgMap.get(key);
                if (null != avgMap) {
                    student._testSubclassAvg.putAll(avgMap);
                }
            }
        }
    }

    private static class TestScore {
        String _subclassname;
        String _combinedSubclasscd;
        String _score;
        String _avg;
        String _rank;
        String _credit;
        String _average; // 個人の平均点
        String _slumpScore;
        String _slumpMark;
        String _sidouinput;
        String _sidouinputinf;
        String _passScore;
        int _kekka;
        int _jisu;
        int _cnt;
        int _cntHr;

        private int getFailValue(final Param param) {
            if (param.isKetten() && NumberUtils.isDigits(param._ketten)) {
                return Integer.parseInt(param._ketten);
            }
            return -1;
        }

        private boolean isKetten(int score, final Param param) {
            if (param.isRecordSlump()) {
                if (null != param._testcd && param._testcd.endsWith("09")) { // 評定、仮評定は1の場合欠点
                    return 1 == score;
                }
                return "1".equals(_slumpScore);
            } else {
                return score <= getFailValue(param);
            }
        }

        public String toString() {
            return "科目：" + _subclassname
                    + "得点：" + _score
                    + " 平均：" + _avg
                    + " 席次：" + _rank;
        }

        private static void setRank(final DB2UDB db2, final Param param, final List studentList) throws SQLException {

            final String sql = getRecordRankTestAppointSql(param._year, param._semester, param._testcd);
            PreparedStatement ps = null;
            try {
//                log.debug(rankSql);
                ps = db2.prepareStatement(sql);

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    ps.setString(1, student._schregno);

                    ResultSet rs = ps.executeQuery();
                    int subclassCount = 0;
                    boolean hasScore = false;
                    int totalScore = 0;
                    while (rs.next()) {
                        final String subclassCd = getSubclassCd(rs, param);
                        if (student._testSubclass.containsKey(subclassCd)) {
                            hasScore = true;
                            final TestScore testScore = (TestScore) student._testSubclass.get(subclassCd);
                            testScore._score = rs.getString("SCORE");
                            testScore._rank = rs.getString(param.getRankField() + param.getRankAvgField() + "RANK");

                            if (!"欠".equals(testScore._score)) {
                                totalScore += Integer.parseInt(testScore._score);
                            }
                            subclassCount += 1;
                        }
                        if (subclassCd.equals(SUBCLASS9)) {
                            student._testAll._score = rs.getString("SCORE");
                            student._testAll._rank = rs.getString(param.getRankField() + param.getRankAvgField() + "RANK");
                            student._testAll._average = sishagonyu(rs.getBigDecimal("AVG"));
                        }
                    }
                    DbUtils.closeQuietly(rs);
                    student._hasScore = hasScore;
                    if (hasScore) {
                        student._averageScore = divide(totalScore, subclassCount);
                    }
                }

            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }

        /**
         * 席次データの取得
         * @return sql
         */
        public static String getRecordRankTestAppointSql(
                final String year,
                final String semester,
                final String testcd
        ) {
            final StringBuffer stb = new StringBuffer();

            stb.append(" SELECT ");
            stb.append("     T1.* ");
            stb.append(" FROM ");
            stb.append("     RECORD_RANK_SDIV_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + year + "' ");
            stb.append("     AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '" + testcd + "' ");
            stb.append("     AND T1.SCHREGNO = ? ");

            return stb.toString();
        }

        private static String getAverageParam(final int paramdiv, final String avgDiv, final Student student) {
            final String AVG_DIV_GRADE = "1";
            final String AVG_DIV_HR = "2";
            final String AVG_DIV_COURSE = "3";
            final String AVG_DIV_MAJOR = "4";
            String rtn = null;
            if (paramdiv == 2) { // HR
                if (AVG_DIV_GRADE.equals(avgDiv)) {
                    rtn = "000";
                } else if (AVG_DIV_HR.equals(avgDiv)) {
                    rtn = student._hrClass;
                } else if (AVG_DIV_COURSE.equals(avgDiv)) {
                    rtn = "000";
                } else if (AVG_DIV_MAJOR.equals(avgDiv)) {
                    rtn = "000";
                }
            } else if (paramdiv == 3) { // コース
                if (AVG_DIV_GRADE.equals(avgDiv)) {
                    rtn = "00000000";
                } else if (AVG_DIV_HR.equals(avgDiv)) {
                    rtn = "00000000";
                } else if (AVG_DIV_COURSE.equals(avgDiv)) {
                    rtn = student._courseCd + student._majorCd + student._courseCode;
                } else if (AVG_DIV_MAJOR.equals(avgDiv)) {
                    rtn = student._courseCd + student._majorCd + "0000";
                }
            }
            return rtn;
        }

        private static void setAvg(final DB2UDB db2, final Param param, final List studentList) throws SQLException {
            PreparedStatement ps = null;
            // 学年/コースの平均
            try {
                final String avgDiv = param.getAvgDiv();
                final String sql = getAverageSql(param._year, param._semester, avgDiv, param._testcd);
                ps = db2.prepareStatement(sql);

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    ps.setString(1, student._grade);
                    ps.setString(2, getAverageParam(2, avgDiv, student));
                    ps.setString(3, getAverageParam(3, avgDiv, student));

                    ResultSet rs = ps.executeQuery();
                    int totalScore = 0;
                    int totalCount = 0;

                    while (rs.next()) {
                        final String subclassCd = getSubclassCd(rs, param);
                        if (student._testSubclass.containsKey(subclassCd)) {
                            final TestScore testScore = (TestScore) student._testSubclass.get(subclassCd);
                            testScore._avg = sishagonyu(rs.getBigDecimal("AVG"));
                        }
                        if (SUBCLASS9.equals(subclassCd)) {
                            student._testAll._cnt = rs.getInt("COUNT");
                        }
                        if (!isSubclassAll(subclassCd)) {
                            totalScore += rs.getInt("SCORE");
                            totalCount += rs.getInt("COUNT");
                        }
                    }
                    DbUtils.closeQuietly(rs);
                    if (totalCount > 0) {
                        student._testAll._avg = divide(totalScore, totalCount);
                    }
                }
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }

        /**
         * 平均データの取得
         * @return sql
         */
        public static String getAverageSql(
                final String year,
                final String semester,
                final String avgDiv,
                final String testcd
        ) {
            final StringBuffer stb = new StringBuffer();

            stb.append(" SELECT ");
            stb.append("     T1.* ");
            stb.append(" FROM ");
            stb.append("     RECORD_AVERAGE_SDIV_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + year + "' ");
            stb.append("     AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '" + testcd + "' ");
            stb.append("     AND T1.AVG_DIV = '" + avgDiv + "' ");
            stb.append("     AND T1.GRADE = ? ");
            stb.append("     AND T1.HR_CLASS = ? ");
            stb.append("     AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE = ? ");

            return stb.toString();
        }

        private static void setKetten(final DB2UDB db2, final Param param, final List studentList) throws SQLException {
            PreparedStatement ps = null;
            // 欠点対象
            try {
                final String sql = getKettenSql(param);
                ps = db2.prepareStatement(sql);

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    ps.setString(1, student._schregno);

                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        final String subclassCd = getSubclassCd(rs, param);
                        if (student._testSubclass.containsKey(subclassCd)) {
                            final TestScore testScore = (TestScore) student._testSubclass.get(subclassCd);
                            testScore._sidouinput = rs.getString("SIDOU_INPUT");
                            testScore._sidouinputinf = rs.getString("SIDOU_INPUT_INF");
                            testScore._slumpScore = rs.getString("SLUMP_SCORE");
                        }
                    }
                    DbUtils.closeQuietly(rs);
                }
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }

        private static String getKettenSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            //成績不振科目データの表
            stb.append("   WITH REL_COUNT AS (");
            stb.append("   SELECT SUBCLASSCD");
            stb.append("     , CLASSCD ");
            stb.append("     , SCHOOL_KIND ");
            stb.append("     , CURRICULUM_CD ");
            stb.append("     , COUNT(*) AS COUNT ");
            stb.append("          FROM RELATIVEASSESS_MST ");
            stb.append("          WHERE GRADE = '" + param._grade + "' AND ASSESSCD = '3' ");
            stb.append("   GROUP BY SUBCLASSCD");
            stb.append("     , CLASSCD ");
            stb.append("     , SCHOOL_KIND ");
            stb.append("     , CURRICULUM_CD ");
            stb.append("   ) ");
            stb.append(" SELECT ");
            stb.append("     T1.CLASSCD, ");
            stb.append("     T1.SCHOOL_KIND, ");
            stb.append("     T1.CURRICULUM_CD, ");
            stb.append("     T1.SUBCLASSCD, ");
            stb.append("     cast(null as smallint) as PASS_SCORE, ");
            stb.append("     W1.SIDOU_INPUT, ");
            stb.append("     W1.SIDOU_INPUT_INF, ");
            stb.append("         CASE WHEN VALUE(T2.COUNT, 0) > 0 THEN ");
            stb.append("          (SELECT MAX(L3.ASSESSLEVEL) ");
            stb.append("           FROM RELATIVEASSESS_MST L3 ");
            stb.append("           WHERE L3.GRADE = '" + param._grade + "' AND L3.ASSESSCD = '3' ");
            stb.append("             AND T1.SCORE BETWEEN L3.ASSESSLOW AND L3.ASSESSHIGH  ");
            stb.append("             AND L3.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("     AND L3.CLASSCD = T1.CLASSCD ");
            stb.append("     AND L3.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("     AND L3.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("          ) ELSE ");
            stb.append("          (SELECT MAX(L3.ASSESSLEVEL) ");
            stb.append("           FROM ASSESS_MST L3 ");
            stb.append("           WHERE L3.ASSESSCD = '3' ");
            stb.append("             AND T1.SCORE BETWEEN L3.ASSESSLOW AND L3.ASSESSHIGH  ");
            stb.append("          ) ");
            stb.append("    END AS SLUMP_SCORE ");
            stb.append(" FROM ");
            stb.append("     RECORD_RANK_SDIV_DAT T1 ");
            stb.append("     INNER JOIN TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV W1 ON T1.YEAR = W1.YEAR ");
            stb.append("            AND T1.SEMESTER = W1.SEMESTER ");
            stb.append("            AND T1.TESTKINDCD = W1.TESTKINDCD ");
            stb.append("            AND T1.TESTITEMCD = W1.TESTITEMCD ");
            stb.append("            AND T1.SCORE_DIV = W1.SCORE_DIV ");
            stb.append("            AND GRADE = '00' ");
            stb.append("            AND COURSECD || '-' || MAJORCD = '" + param._major + "' ");
            stb.append("        LEFT JOIN REL_COUNT T2 ON T2.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("     AND T2.CLASSCD = T1.CLASSCD ");
            stb.append("     AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("     AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' AND ");
            stb.append("     T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '" + param._testcd + "' AND ");
            stb.append("     T1.SCHREGNO = ? ");
            return stb.toString();
        }

        private static String getChaircdSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     min(T1.CHAIRCD) as CHAIRCD ");
            stb.append(" FROM ");
            stb.append("     CHAIR_DAT T1, ");
            stb.append("     CHAIR_STD_DAT T2 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' AND ");
            stb.append("     T1.SEMESTER = '" + param._schregSemester + "' AND ");
            stb.append("     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
            stb.append("     T1.SUBCLASSCD = ? AND ");
            stb.append("     T2.YEAR = T1.YEAR AND ");
            stb.append("     T2.SEMESTER = T1.SEMESTER AND ");
            stb.append("     T2.CHAIRCD = T1.CHAIRCD AND ");
            stb.append("     T2.SCHREGNO = ? ");

            return stb.toString();
        }

        private static void setChairStd(final DB2UDB db2, final Param param, final List studentList) throws SQLException {
            ResultSet rs;
            PreparedStatement ps;
            String sql = getChairStdSql(param);
            ps = db2.prepareStatement(sql);

            for (final Iterator it = studentList.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();

                ps.setString(1, student._schregno);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String subclassCd = getSubclassCd(rs, param);
                    String strKetu = "";
                    if (student._testSubclassAvg.containsKey(subclassCd)) {
                    }
                    if (!param._printTestOnly || param._printTestOnly && student._testSubclassAvg.containsKey(subclassCd)) {
                        final TestScore testScore = new TestScore();
                        testScore._score = strKetu;
                        testScore._subclassname = rs.getString("SUBCLASSABBV");
                        testScore._combinedSubclasscd = rs.getString("COMBINED_SUBCLASSCD");
                        student._testSubclass.put(subclassCd, testScore);
                    }
                }
                DbUtils.closeQuietly(rs);
            }
            DbUtils.closeQuietly(ps);
        }

        private static String getChairStdSql(final Param param) {
            final StringBuffer stb = new StringBuffer();

            stb.append(" WITH COMBINED_SUBCLASS AS (");
            stb.append(" SELECT DISTINCT ");
            stb.append("     T1.COMBINED_CLASSCD, ");
            stb.append("     T1.COMBINED_SCHOOL_KIND, ");
            stb.append("     T1.COMBINED_CURRICULUM_CD, ");
            stb.append("     T1.COMBINED_SUBCLASSCD ");
            stb.append(" FROM ");
            stb.append("     SUBCLASS_REPLACE_COMBINED_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + param._year + "' ");
            stb.append(" )");

            stb.append(" SELECT DISTINCT ");
            stb.append("     T1.CLASSCD, ");
            stb.append("     T1.SCHOOL_KIND, ");
            stb.append("     T1.CURRICULUM_CD, ");
            stb.append("     T1.SUBCLASSCD, ");
            stb.append("     L2.COMBINED_SUBCLASSCD, ");
            stb.append("     L1.SUBCLASSABBV ");
            stb.append(" FROM ");
            stb.append("     CHAIR_DAT T1 ");
            stb.append("     LEFT JOIN SUBCLASS_MST L1 ON L1.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("     AND T1.CLASSCD = L1.CLASSCD ");
            stb.append("     AND T1.SCHOOL_KIND = L1.SCHOOL_KIND ");
            stb.append("     AND T1.CURRICULUM_CD = L1.CURRICULUM_CD ");
            stb.append("     LEFT JOIN COMBINED_SUBCLASS L2 ON L2.COMBINED_SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("     AND T1.CLASSCD = L2.COMBINED_CLASSCD ");
            stb.append("     AND T1.SCHOOL_KIND = L2.COMBINED_SCHOOL_KIND ");
            stb.append("     AND T1.CURRICULUM_CD = L2.COMBINED_CURRICULUM_CD ");
            stb.append("     , CHAIR_STD_DAT T2 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' AND ");
            stb.append("     T1.SEMESTER = '" + param._schregSemester + "' AND ");
            stb.append("     substr(T1.SUBCLASSCD,1,2) < '90' AND ");
            stb.append("     T2.YEAR = T1.YEAR AND ");
            stb.append("     T2.SEMESTER = T1.SEMESTER AND ");
            stb.append("     T2.CHAIRCD = T1.CHAIRCD AND ");
            stb.append("     T2.SCHREGNO = ? ");
            stb.append(" ORDER BY ");
            stb.append("     T1.CLASSCD, ");
            stb.append("     T1.SCHOOL_KIND, ");
            stb.append("     T1.CURRICULUM_CD, ");
            stb.append("     T1.SUBCLASSCD ");
            return stb.toString();
        }

        private static void setCreditMst(final DB2UDB db2, final Param param, final List studentList) throws SQLException {
            final String creditSql = getCredit(param);
            //単位
            PreparedStatement ps = db2.prepareStatement(creditSql);

            for (final Iterator stit = studentList.iterator(); stit.hasNext();) {
                final Student student = (Student) stit.next();

                ps.setString(1, student._courseCd);
                ps.setString(2, student._majorCd);
                ps.setString(3, student._grade);
                ps.setString(4, student._courseCode);

                for (final Iterator it = student._testSubclass.keySet().iterator(); it.hasNext();) {
                    final String subclassCd = (String) it.next();
                    final TestScore testScore = (TestScore) student._testSubclass.get(subclassCd);

                    ps.setString(5, subclassCd.substring(0, 2));
                    ps.setString(6, subclassCd);

                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        testScore._credit = rs.getString("CREDITS");
                    }
                    DbUtils.closeQuietly(rs);
                }
            }
            DbUtils.closeQuietly(ps);
        }

        private static String getCredit(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.CREDITS, ");
            stb.append("     VALUE(T1.ABSENCE_HIGH, 0) ");
            stb.append("     AS ABSENCE_HIGH, ");
            stb.append("     VALUE(T1.GET_ABSENCE_HIGH, 0) ");
            stb.append("     AS GET_ABSENCE_HIGH ");
            stb.append(" FROM ");
            stb.append("     V_CREDIT_MST T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.COURSECD = ? ");
            stb.append("     AND T1.MAJORCD = ? ");
            stb.append("     AND T1.GRADE = ? ");
            stb.append("     AND T1.COURSECODE = ? ");
            stb.append("     AND T1.CLASSCD = ? ");
            stb.append("     AND ");
            stb.append("     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
            stb.append("         T1.SUBCLASSCD = ? ");

            return stb.toString();
        }

        private static String divide(final int v1, final int v2) {
            return new BigDecimal(v1).divide(new BigDecimal(v2), 1, BigDecimal.ROUND_HALF_UP).toString();
        }
    }

    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    private static class Param {
        final String _year;
        final String _categoryIsClass;
        final String _testcd;
        final String _testName;
        final String _semester;
        final String _grade;
        final String _hrClass;
        final String _outputKijun;
        final String[] _selectData;
        final String _z010;
        final boolean _isSeireki;
        final String _ketten;
        final String _checkKettenDiv; //欠点プロパティ 1,2,設定無し(1,2以外)
        final String _countFlg;
        final String _scoreFlg;
        final String _schregSemester;
        final String _major;
        final String _use_school_detail_gcm_dat;
        final String _useSchool_KindField;
        final String SCHOOLKIND;
        final String SCHOOLCD;
        private String _semesterName;

        /** 出欠集計日付 */
        final String _dateS;
        final String _date;
        /** フォーム選択（最大科目数：１５or２０） */
        final boolean _printForm20;
        /** 試験科目のみ出力する */
        final boolean _printTestOnly;
        /** 順位を出力しない */
        final String _notPrintRank;
        /** 一日出席欄を出力する */
        final String _printOnedayAttend;
        /** 教育課程コードを使用するか */
        final String _useCurriculumcd;
        /** 注意週数学期 */
        final String _warnSemester;
        /** 「考査を実施しない講座は平均点を表示しない」を処理するか */
        final String _knjd192AcheckNoExamChair;
        /** 総合点の平均点を使用しない */
        final boolean _useTotalAverage;

        private KNJSchoolMst _knjSchoolMst;
        final Map _attendParamMap;

        /** 氏名欄に学籍番号の表示/非表示 1:表示 それ以外:非表示 */
        final String _use_SchregNo_hyoji;

        Param(final DB2UDB db2, final HttpServletRequest request) {
            _year = request.getParameter("YEAR");
            _categoryIsClass = request.getParameter("CATEGORY_IS_CLASS");
            _testcd = request.getParameter("SUB_TESTCD");
            _semester = request.getParameter("SEMESTER");
            _schregSemester = "9".equals(_semester) ? request.getParameter("CTRL_SEME") : _semester;
            _major = request.getParameter("MAJOR");
            _testName = getTestName(db2);
            setSemesterName(db2);
            _grade = request.getParameter("GRADE");
            _hrClass = request.getParameter("HR_CLASS");
            _outputKijun = request.getParameter("OUTPUT_KIJUN");
            _selectData = request.getParameterValues("CATEGORY_SELECTED");  //学籍番号または学年-組
            _use_school_detail_gcm_dat = request.getParameter("use_school_detail_gcm_dat");
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            SCHOOLKIND = request.getParameter("SCHOOLKIND");
            SCHOOLCD = request.getParameter("SCHOOLCD");

            _z010 = setNameMst(db2, "Z010", "00");
            _isSeireki = "2".equals(setNameMst(db2, "Z012", "01"));

            _ketten = request.getParameter("KETTEN");
            _checkKettenDiv = request.getParameter("checkKettenDiv");
            _countFlg = request.getParameter("COUNT_SURU");
            _scoreFlg = request.getParameter("SCORE_FLG");
            _dateS = request.getParameter("SDATE").replace('/', '-');
            _date = request.getParameter("DATE").replace('/', '-');
            _knjd192AcheckNoExamChair = request.getParameter("knjd192AcheckNoExamChair");
            _useTotalAverage = true;

            _printForm20 = true; // "2".equals(request.getParameter("SUBCLASS_MAX"));
            _printTestOnly = true; // null != request.getParameter("TEST_ONLY");
            _notPrintRank = request.getParameter("NOT_PRINT_RANK");

            _printOnedayAttend = "1"; //request.getParameter("ONEDAY_ATTEND");
            _useCurriculumcd = request.getParameter("useCurriculumcd");

            _use_SchregNo_hyoji = request.getParameter("use_SchregNo_hyoji");

            try {
                _knjSchoolMst = new KNJSchoolMst(db2, _year);
            } catch (Exception e) {
                log.fatal("exception!", e);
            }

            _warnSemester = setWarnSemester(db2);

            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
        }

        private String setWarnSemester(DB2UDB db2) {
            String _warnSemester = null;
            if ("9".equals(_semester)) {
                _warnSemester = _knjSchoolMst._semesterDiv;
            } else {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT T1.YEAR, T1.SEMESTER, T1.SDATE, T1.EDATE, T2.SEMESTER AS NEXT_SEMESTER, T2.SDATE AS NEXT_SDATE ");
                stb.append(" FROM V_SEMESTER_GRADE_MST T1 ");
                stb.append(" LEFT JOIN V_SEMESTER_GRADE_MST T2 ON T2.YEAR = T1.YEAR ");
                stb.append("     AND INT(T2.SEMESTER) = INT(T1.SEMESTER) + 1 ");
                stb.append("     AND T2.GRADE = T1.GRADE");
                stb.append(" WHERE T1.YEAR = '" + _year + "' ");
                stb.append("     AND T1.SEMESTER <> '9' ");
                stb.append("     AND (('" + _date + "' BETWEEN T1.SDATE AND T1.EDATE) ");
                stb.append("          OR (T1.EDATE < '" + _date + "' AND '" + _date + "' < VALUE(T2.SDATE, '9999-12-30'))) ");
                stb.append(" ORDER BY T1.SEMESTER ");

                PreparedStatement ps = null;
                ResultSet rs = null;
                try {
                    ps = db2.prepareStatement(stb.toString());
                    rs = ps.executeQuery();
                    if (rs.next()) {
                        _warnSemester = rs.getString("SEMESTER");
                    }
                } catch (SQLException e) {
                    log.error("exception!", e);
                } finally {
                    DbUtils.closeQuietly(null, ps, rs);
                }
            }
            return _warnSemester;
        }

        private String getTestName(final DB2UDB db2) {
            String rtn = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = "SELECT TESTITEMNAME FROM TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV WHERE YEAR = '" + _year + "'" +
                        " AND GRADE = '00' " +
                        " AND COURSECD || '-' || MAJORCD = '" + _major + "' " +
                        " AND SEMESTER || TESTKINDCD || TESTITEMCD || SCORE_DIV = '" + _testcd + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString("TESTITEMNAME");
                }
            } catch (Exception e) {
                log.fatal("exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtn;
        }

        private void setSemesterName(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = "SELECT SEMESTER, SEMESTERNAME, SDATE, EDATE FROM SEMESTER_MST WHERE YEAR = '" + _year + "' ORDER BY SEMESTER ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    String seme = rs.getString("SEMESTER");
                    if (_semester.equals(seme)) {
                        _semesterName = rs.getString("SEMESTERNAME");
                    }
                }
            } catch (Exception e) {
                log.fatal("exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        private String setNameMst(final DB2UDB db2, final String namecd1, final String namecd2) {
            String rtnSt = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(getNameMst(_year, namecd1, namecd2));
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtnSt = rs.getString("NAME1");
                }
            } catch (Exception e) {
                log.fatal("exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtnSt;
        }

        private String getNameMst(final String year, final String namecd1, final String namecd2) {
            final String rtnSql = " SELECT "
                                + "     * "
                                + " FROM "
                                + "     V_NAME_MST "
                                + " WHERE "
                                + "     YEAR = '" + year + "' "
                                + "     AND NAMECD1 = '" + namecd1 + "' "
                                + "     AND NAMECD2 = '" + namecd2 + "'";
            return rtnSql;
        }

        private String getRankField() {
            return "COURSE_";
        }

        private String getRankAvgField() {
            return "2".equals(_outputKijun) ? "AVG_" : "";
        }

        private String getRankName() {
            return "コース";
        }

        private String getAvgDiv() {
            return "3";
        }

        private boolean isPrintChair() {
            return false; // "3".equals(_groupDiv) || "4".equals(_groupDiv);
        }

        private String changePrintYear() {
            if (_isSeireki) {
                return _year + "年度";
            } else {
                return KenjaProperties.gengou(Integer.parseInt(_year)) + "年度";
            }
        }

        /** 欠点対象：RECORD_SLUMP_DATを参照して判断するか */
        private boolean isRecordSlump() {
            return false; //"1".equals(_checkKettenDiv) && (null == _ketten || "".equals(_ketten));
        }

        /** 欠点対象：指示画面の欠点を参照して判断するか */
        private boolean isKetten() {
            return !isRecordSlump();
        }
    }
}

// eof
