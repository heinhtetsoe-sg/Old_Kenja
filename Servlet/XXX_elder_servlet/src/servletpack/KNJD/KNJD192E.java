// kanji=漢字
/*
 * $Id: 89444e91cf1b9a2556d53dcab7c347a28848ddcb $
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJServletpacksvfANDdb2;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: 89444e91cf1b9a2556d53dcab7c347a28848ddcb $
 */
public class KNJD192E {

    private static final Log log = LogFactory.getLog("KNJD192E.class");

    private Param _param;

    private static final String SEMEALL = "9";

    private static final String AVG_DIV_1_GRADE = "1";
    private static final String AVG_DIV_2_HR = "2";
    private static final String AVG_DIV_3_COURSE = "3";
    private static final String AVG_DIV_B_HR_COURSE = "B";
    private static final String AVG_DIV_C_HR_COURSE_KOUNYU = "C";
    private static final String AVG_DIV_D_HR_COURSE_IKKAN = "D";
    private static final String AVG_DIV_E_COURSE_KOUNYU = "E";
    private static final String AVG_DIV_F_COURSE_IKKAN = "F";

    private static final String GROUP_DIV_CLASS_GRADE = "1";
    private static final String GROUP_DIV_CLASS_COURSE = "2";

    private static final String ENT_TYPE_KOUNYU = "0"; // 高入
    private static final String ENT_TYPE_IKKAN = "1"; // 一貫

    private static final String SUBCLASS3 = "333333";
    private static final String SUBCLASS5 = "555555";
    private static final String SUBCLASS9 = "999999";

//    private static final String AMIKAKE_ATTR1 = "Paint=(1,60,1),Bold=1";
//    private static final String AMIKAKE_ATTR2 = "Paint=(1,80,1),Bold=1";
//    private static final String HYOTEI_TESTCD = "9990009";

    public void svf_out(HttpServletRequest request, HttpServletResponse response) throws Exception {
        final KNJServletpacksvfANDdb2 sd = new KNJServletpacksvfANDdb2();  //帳票におけるＳＶＦおよびＤＢ２の設定
        DB2UDB db2 = null; //Databaseクラスを継承したクラス
        Vrw32alp svf = new Vrw32alp();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        boolean hasData = false;
        try {

            log.fatal("$Revision: 74539 $ $Date: 2020-05-27 09:10:12 +0900 (水, 27 5 2020) $"); // CVSキーワードの取り扱いに注意

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

    private List<List<Student>> getPageList(final List<Student> studentList, final int maxLine) {
        final List<List<Student>> rtn = new ArrayList();
        List<Student> current = null;
        String befGradeClass = "";
        for (final Student student : studentList) {
            if (null == current || current.size() >= maxLine || !befGradeClass.equals(student._grade + student._hrClass)) {
                current = new ArrayList();
                rtn.add(current);
            }
            current.add(student);
            befGradeClass = student._grade + student._hrClass;
        }
        return rtn;
    }

    private static class StudentCnt {
        int _cnt;
        int _pagecnt;
    }
    private static void setPageCnt(StudentCnt sobj, final int maxLine) {
        final BigDecimal bigCnt = new BigDecimal(sobj._cnt);
        final BigDecimal bigMaxLine = new BigDecimal(maxLine);
        sobj._pagecnt = bigCnt.divide(bigMaxLine, BigDecimal.ROUND_CEILING).intValue();
    }
    private static int getPageNo(StudentCnt sobj,final int studentno) {
        int retpageno = 0;
        retpageno = ((studentno - 1) % sobj._pagecnt);
        return retpageno;
    }

    private static boolean isSubclassAll(final String subclassCd) {
        return SUBCLASS3.equals(subclassCd) || SUBCLASS5.equals(subclassCd) || SUBCLASS9.equals(subclassCd) || "99999A".equals(subclassCd) || "99999B".equals(subclassCd);
    }

    private static String getSubclassCd(final Map row, final Param param) {
        final String subclassCd;
        if (!isSubclassAll(KnjDbUtils.getString(row, "SUBCLASSCD"))) {
            subclassCd = KnjDbUtils.getString(row, "CLASSCD") + "-" + KnjDbUtils.getString(row, "SCHOOL_KIND") + "-" + KnjDbUtils.getString(row, "CURRICULUM_CD") + "-" + KnjDbUtils.getString(row, "SUBCLASSCD");
        } else {
            subclassCd = KnjDbUtils.getString(row, "SUBCLASSCD");
        }
        return subclassCd;
    }

    private static String roundHalfUp(final String num) {
        return !NumberUtils.isNumber(num) ? null : new BigDecimal(num).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
    }

    private static String roundHalfUp(final BigDecimal bd) {
        return null == bd ? null : bd.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
    }

//    /**
//     * 欠課数オーバーか
//     * @param kekka 欠課数
//     * @param absenceHigh 欠課数上限値（履修）
//     * @return true or false
//     */
//    private static boolean isKekkaOver(final String kekka, final String absenceHigh) {
//        if (null == kekka || Double.parseDouble(kekka) == 0) return false;
//        if (null == absenceHigh) return false;
//        return Double.parseDouble(kekka) > Double.parseDouble(absenceHigh);
//    }

    private List<Student> getStudentList(final DB2UDB db2) throws SQLException  {
    final List<Student> studentList = Student.getStudentList(db2, _param);

        Student.setMapTestSubclass(db2, _param, studentList);
        TestScore.setChairStd(db2, _param, studentList);

        TestScore.setRank(db2, _param, studentList);
        TestScore.setRankDetail(db2, _param, studentList);
        _param._recordAverageSdivDatMap = TestScore.setAvg(db2, _param);
//        Attendance.setAttendSubclass(db2, _param, studentList);
        Attendance.setAttendSemes(db2, _param, studentList);

        return studentList;
    }

    private static <A, B, C> Map<B, C> getMappedMap(final Map<A, Map<B, C>> map, final A key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new HashMap());
        }
        return map.get(key1);
    }

//    //V_RECORD_RANK_SDIV_DATの取得
//    private String getVRecordRankSdiv(final DB2UDB db2, final Param param, final String subclasscd, final String schregno, final String field) {
//    String rtnStr = "";
//    final String semester = param._testcd.substring(0, 1);
//    final String testkindcd = param._testcd.substring(1, 3);
//    final String testitemcd = param._testcd.substring(3, 5);
//    final String scoreDiv = param._testcd.substring(5, 7);
//        final StringBuffer stb = new StringBuffer();
//        stb.append(" SELECT  ");
//        stb.append("   * ");
//        stb.append(" FROM  ");
//        stb.append("   V_RECORD_RANK_SDIV_DAT ");
//        stb.append(" WHERE YEAR       = '"+ param._year +"' ");
//        stb.append("   AND SEMESTER   = '" + semester + "' ");
//        stb.append("   AND TESTKINDCD = '" + testkindcd + "' ");
//        stb.append("   AND TESTITEMCD = '" + testitemcd + "' ");
//        stb.append("   AND SCORE_DIV  = '" + scoreDiv + "' ");
//        if(SUBCLASS9.equals(subclasscd)) {
//            stb.append("   AND SUBCLASSCD = '"+ subclasscd +"' ");
//        } else {
//        stb.append("   AND CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD = '"+ subclasscd +"' ");
//        }
//        stb.append("   AND SCHREGNO   = '"+ schregno +"' ");
//        final String sql =  stb.toString();
//        PreparedStatement ps = null;
//        ResultSet rs = null;
//
//        try {
//        log.debug(sql);
//            ps = db2.prepareStatement(sql);
//            rs = ps.executeQuery();
//            while (rs.next()) {
//            rtnStr = StringUtils.defaultString(rs.getString(field));
//            }
//        } catch (SQLException ex) {
//            log.error("Exception:", ex);
//        } finally {
//            DbUtils.closeQuietly(null, ps, rs);
//            db2.commit();
//        }
//        return rtnStr.toString();
//    }

    /**
     * @param db2
     */
    private boolean printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        boolean hasData = false;
        final List<Student> studentListAll = getStudentList(db2);
        log.debug(" studentList size = " + studentListAll.size());

        final String form = "KNJD192E.frm";

        final int maxLine = 2;
        final List<List<Student>> pageList = getPageList(studentListAll, maxLine);
        for (final List<Student> studentList : pageList) {

            svf.VrSetForm(form, 1);

            for (int gyo = 1; gyo <=  studentList.size(); gyo++) {
                final Student student = studentList.get(gyo - 1);

                setPrintOut(db2, svf, student, gyo);

                hasData = true;
            }
            svf.VrEndPage();
        }
        return hasData;
    }

    private void setPrintOut(final DB2UDB db2, final Vrw32alp svf, final Student student, final int gyo) {
    	//ヘッダ
        svf.VrsOut("TITLE" + gyo, _param._nendo + _param._testName + "　個人成績表（短冊）");
        svf.VrsOut("HR_NAME" + gyo, student._hrName + "(" + student._attendNo + ")");
        String nameField = KNJ_EditEdit.getMS932ByteLength(student._name) > 30 ? "2" : "1";
        svf.VrsOut("NAME" + gyo + "_" + nameField, student._name);
        final String entType = ENT_TYPE_IKKAN.equals(student._entType) ? "　一貫" : "　高入";
        svf.VrsOut("COURSE_NAME" + gyo, student._courseName + entType);

        //出欠情報
        svf.VrsOut("MUST" + gyo, student._attendance._mlesson);
        svf.VrsOut("ABSENCE" + gyo, student._attendance._sick);
        svf.VrsOut("LATE" + gyo, student._attendance._late);
        svf.VrsOut("EARLY" + gyo, student._attendance._early);

        //明細タイトル
        svf.VrsOut("RANK_NAME" + gyo + "_1", "クラス順位");
        svf.VrsOut("RANK_NAME" + gyo + "_2", _param.getRankName() + "順位");
        svf.VrsOut("AVE_NAME" + gyo + "_1", "クラス平均");
        svf.VrsOut("AVE_NAME" + gyo + "_2", _param.getRankName() + "平均");
        svf.VrsOut("MAX_NAME" + gyo + "_1", "クラス最高点");
        svf.VrsOut("MIN_NAME" + gyo + "_1", "クラス最低点");
        svf.VrsOut("MAX_NAME" + gyo + "_2", _param.getRankName() + "最高点");
        svf.VrsOut("MIN_NAME" + gyo + "_2", _param.getRankName() + "最低点");
        svf.VrsOut("EXAM_NUM_NAME" + gyo + "_1", "クラス受験者数");
        svf.VrsOut("EXAM_NUM_NAME" + gyo + "_2", _param.getRankName() + "受験者数");

        //明細
        final List<TestScore> printScoreList = new ArrayList(student._testSubclass.values());
        Collections.sort(printScoreList);
        int j = 1;
        for (final TestScore testScore : printScoreList) {
            svf.VrsOutn("SUBCLASS_NAME" + gyo , j, StringUtils.defaultString(testScore._name));
            final String subclasscd = testScore._subclasscd;
            final int kubun = RecordAverageSdivDat.KUBUN_SCORE;
            if (student.isPrintSubclassScoreAvg(subclasscd, _param)) {
            	final String avgDivGradeOrCourse = GROUP_DIV_CLASS_COURSE.equals(_param._groupDiv) ? AVG_DIV_3_COURSE : AVG_DIV_1_GRADE;
                svf.VrsOutn("SCORE" + gyo, j, testScore._score);
                svf.VrsOutn("RANK" + gyo + "_1", j, testScore._rankHr);
                svf.VrsOutn("RANK" + gyo + "_2", j, testScore._rank);
                svf.VrsOutn("AVE" + gyo + "_1", j, roundHalfUp(getRecordAverageSdivDat(AVG_DIV_2_HR, student, subclasscd, kubun)._avg));
                svf.VrsOutn("AVE" + gyo + "_2", j, roundHalfUp(getRecordAverageSdivDat(avgDivGradeOrCourse, student, subclasscd, kubun)._avg));
                svf.VrsOutn("MAX" + gyo + "_1", j, getRecordAverageSdivDat(AVG_DIV_2_HR, student, subclasscd, kubun)._highScore);
                svf.VrsOutn("MIN" + gyo + "_1", j, getRecordAverageSdivDat(AVG_DIV_2_HR, student, subclasscd, kubun)._lowScore);
                svf.VrsOutn("MAX" + gyo + "_2", j, getRecordAverageSdivDat(avgDivGradeOrCourse, student, subclasscd, kubun)._highScore);
                svf.VrsOutn("MIN" + gyo + "_2", j, getRecordAverageSdivDat(avgDivGradeOrCourse, student, subclasscd, kubun)._lowScore);
                svf.VrsOutn("EXAM_NUM" + gyo + "_1", j, getRecordAverageSdivDat(AVG_DIV_2_HR, student, subclasscd, kubun)._count);
                svf.VrsOutn("EXAM_NUM" + gyo + "_2", j, getRecordAverageSdivDat(avgDivGradeOrCourse, student, subclasscd, kubun)._count);
            }
            j++;
        }

        //明細(平均点)

        if (student._hasScore) {
            final String rankDivHrCourse = "B";
            final String rankDivCourse = "3";
            final String avgDivHrCourse = AVG_DIV_B_HR_COURSE;
            final String avgDivCourse = AVG_DIV_3_COURSE;
            final int kubun = RecordAverageSdivDat.KUBUN_AVG;
            svf.VrsOutn("SUBCLASS_NAME" + gyo , 21, "平均点");
            svf.VrsOutn("SCORE" + gyo, 21, student._testAllClassCourse._average);
            svf.VrsOutn("RANK" + gyo + "_1", 21, student.rankDetailRank(SUBCLASS9, rankDivHrCourse));
            svf.VrsOutn("RANK" + gyo + "_2", 21, student.rankDetailRank(SUBCLASS9, rankDivCourse));
            svf.VrsOutn("AVE" + gyo + "_1", 21, roundHalfUp(getRecordAverageSdivDat(avgDivHrCourse, student, SUBCLASS9, kubun)._avg));
            svf.VrsOutn("AVE" + gyo + "_2", 21, roundHalfUp(getRecordAverageSdivDat(avgDivCourse, student, SUBCLASS9, kubun)._avg));
            svf.VrsOutn("MAX" + gyo + "_1", 21, roundHalfUp(getRecordAverageSdivDat(avgDivHrCourse, student, SUBCLASS9, kubun)._highScore));
            svf.VrsOutn("MIN" + gyo + "_1", 21, roundHalfUp(getRecordAverageSdivDat(avgDivHrCourse, student, SUBCLASS9, kubun)._lowScore));
            svf.VrsOutn("MAX" + gyo + "_2", 21, roundHalfUp(getRecordAverageSdivDat(avgDivCourse, student, SUBCLASS9, kubun)._highScore));
            svf.VrsOutn("MIN" + gyo + "_2", 21, roundHalfUp(getRecordAverageSdivDat(avgDivCourse, student, SUBCLASS9, kubun)._lowScore));
            svf.VrsOutn("EXAM_NUM" + gyo + "_1", 21, getRecordAverageSdivDat(avgDivHrCourse, student, SUBCLASS9, kubun)._count);
            svf.VrsOutn("EXAM_NUM" + gyo + "_2", 21, getRecordAverageSdivDat(avgDivCourse, student, SUBCLASS9, kubun)._count);
        }

        //明細(合計点)
        if (student._hasScore) {
            final String rankDivHrCourse;
            final String rankDivCourse;
            final String avgDivHrCourse;
            final String avgDivCourse;
            if (ENT_TYPE_IKKAN.equals(student._entType)) {
                rankDivHrCourse = "D";
                rankDivCourse = "F";
                avgDivHrCourse = AVG_DIV_D_HR_COURSE_IKKAN;
                avgDivCourse = AVG_DIV_F_COURSE_IKKAN;
            } else { // ENT_TYPE_KOUNYU.equals(student._entType)
                rankDivHrCourse = "C";
                rankDivCourse = "E";
                avgDivHrCourse = AVG_DIV_C_HR_COURSE_KOUNYU;
                avgDivCourse = AVG_DIV_E_COURSE_KOUNYU;
            }

            final int kubun = RecordAverageSdivDat.KUBUN_SCORE;
            svf.VrsOutn("SUBCLASS_NAME" + gyo , 22, "合計点");
            svf.VrsOutn("SCORE" + gyo, 22, student._testAllClassCourseEnt._score);
            svf.VrsOutn("RANK" + gyo + "_1", 22, student.rankDetailRank(SUBCLASS9, rankDivHrCourse));
            svf.VrsOutn("RANK" + gyo + "_2", 22, student.rankDetailRank(SUBCLASS9, rankDivCourse));
            svf.VrsOutn("AVE" + gyo + "_1", 22, roundHalfUp(getRecordAverageSdivDat(avgDivHrCourse, student, SUBCLASS9, kubun)._avg));
            svf.VrsOutn("AVE" + gyo + "_2", 22, roundHalfUp(getRecordAverageSdivDat(avgDivCourse, student, SUBCLASS9, kubun)._avg));
            svf.VrsOutn("MAX" + gyo + "_1", 22, getRecordAverageSdivDat(avgDivHrCourse, student, SUBCLASS9, kubun)._highScore);
            svf.VrsOutn("MIN" + gyo + "_1", 22, getRecordAverageSdivDat(avgDivHrCourse, student, SUBCLASS9, kubun)._lowScore);
            svf.VrsOutn("MAX" + gyo + "_2", 22, getRecordAverageSdivDat(avgDivCourse, student, SUBCLASS9, kubun)._highScore);
            svf.VrsOutn("MIN" + gyo + "_2", 22, getRecordAverageSdivDat(avgDivCourse, student, SUBCLASS9, kubun)._lowScore);
            svf.VrsOutn("EXAM_NUM" + gyo + "_1", 22, getRecordAverageSdivDat(avgDivHrCourse, student, SUBCLASS9, kubun)._count);
            svf.VrsOutn("EXAM_NUM" + gyo + "_2", 22, getRecordAverageSdivDat(avgDivCourse, student, SUBCLASS9, kubun)._count);
        }

    }

    private static class Attendance {
        private String _mlesson;
        private String _sick;
        private String _late;
        private String _early;

        static void setAttendSemes(final DB2UDB db2, final Param _param, final List<Student> studentList) {

            final Map<String, List<Student>> hrStudentListMap = new HashMap();
            for (final Student student : studentList) {
                if (null == hrStudentListMap.get(student._grade + student._hrClass)) {
                    hrStudentListMap.put(student._grade + student._hrClass, new ArrayList());
                }
                hrStudentListMap.get(student._grade + student._hrClass).add(student);
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

                for (final String gradeHrclass : hrStudentListMap.keySet()) {

                    ps.setString(2, gradeHrclass.substring(0, 2));
                    ps.setString(3, gradeHrclass.substring(2));

                    final List<Student> hrStudentList = hrStudentListMap.get(gradeHrclass);

                    for (final Student student : hrStudentList) {

                        ps.setString(1, student._schregno);

                        ResultSet rs = ps.executeQuery();
                        while (rs.next()) {
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

    }

    private static class Student {
        final String _schregno;
        final String _grade;
        final String _hrClass;
        final String _attendNo;
        final String _entType;
        final String _hrName;
        final String _hrNameAbbv;
        final String _name;
        final String _courseCd;
        final String _courseName;
        final String _majorCd;
        final String _majorName;
        final String _courseCode;
        final String _courseCodeName;
        final String _courseGroupCd;
        final boolean _majorHas1Hr;
        final Map<String, Map<String, String>> _rankDivRecordRankSdivDetailDatRankMap = new HashMap<String, Map<String, String>>();
        final Map<String, TestScore> _testSubclass;
        final Map<String, String> _recordScoreSubclass;
        final Map<String, String> _ketsuSubclass;
        final Map _recordChkfinSubclass;
        final TestScore _testAllClassCourse;
        final TestScore _testAllClassCourseEnt;
        boolean _hasScore;
        /** 出欠情報 */
        final Attendance _attendance = new Attendance();

        /**
         * コンストラクタ。
         */
        public Student(
        final Param param,
                final String schregno,
                final String grade,
                final String hrClass,
                final String attendNo,
                final String entType,
                final String hrName,
                final String hrNameAbbv,
                final String name,
                final String courseCd,
                final String courseName,
                final String majorCd,
                final String majorName,
                final String courseCode,
                final String courseCodeName,
                final String courseGroupCd,
                final boolean majorHas1Hr
                ) {
            _schregno = schregno;
            _grade = grade;
            _hrClass = hrClass;
            _attendNo = attendNo;
            _entType = entType;
            _hrName = hrName;
            _hrNameAbbv = hrNameAbbv;
            _name = name;
            _courseCd = courseCd;
            _courseName = courseName;
            _majorCd = majorCd;
            _majorName = majorName;
            _courseCode = courseCode;
            _courseCodeName = courseCodeName;
            _courseGroupCd = courseGroupCd;
            _majorHas1Hr = majorHas1Hr;
            _testSubclass = new TreeMap();
            _recordScoreSubclass = new TreeMap();
            _ketsuSubclass = new TreeMap();
            _recordChkfinSubclass = new TreeMap();
            _testAllClassCourse = new TestScore(param, "999999");
            _testAllClassCourse._score = "";
            _testAllClassCourseEnt = new TestScore(param, "999999");
            _testAllClassCourseEnt._score = "";
        }

        /** 指定科目コードの平均点を表示するか */
        public boolean isPrintSubclassScoreAvg(final String subclassCd, final Param param) {
            return true;
        }

        public String rankDetailRank(final String subclasscd, final String rankDiv) {
        return getMappedMap(_rankDivRecordRankSdivDetailDatRankMap, subclasscd).get(rankDiv);
        }

        public String toString() {
            return "学籍：" + _schregno + " 名前：" + _name;
        }

        private static List getStudentList(final DB2UDB db2, final Param param) {
        final List<Student> studentList = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getStudentInfoSql(param);
//                log.debug("getStudentInfoSql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final Student student = new Student(param,
                                          StringUtils.defaultString(rs.getString("SCHREGNO")),
                                          StringUtils.defaultString(rs.getString("GRADE")),
                                          StringUtils.defaultString(rs.getString("HR_CLASS")),
                                          StringUtils.defaultString(rs.getString("ATTENDNO")),
                                          StringUtils.defaultString(rs.getString("ENT_TYPE")),
                                          StringUtils.defaultString(rs.getString("HR_NAME")),
                                          StringUtils.defaultString(rs.getString("HR_NAMEABBV")),
                                          "1".equals(rs.getString("USE_REAL_NAME")) ? StringUtils.defaultString(rs.getString("REAL_NAME")) : StringUtils.defaultString(rs.getString("NAME")),
                                          StringUtils.defaultString(rs.getString("COURSECD")),
                                          StringUtils.defaultString(rs.getString("COURSENAME")),
                                          StringUtils.defaultString(rs.getString("MAJORCD")),
                                          StringUtils.defaultString(rs.getString("MAJORNAME")),
                                          StringUtils.defaultString(rs.getString("COURSECODE")),
                                          StringUtils.defaultString(rs.getString("COURSECODENAME")),
                                          StringUtils.defaultString(rs.getString("COURSE_GROUP_CD")),
                                          "1".equals(StringUtils.defaultString(rs.getString("MAJOR_HAS_1_HR"))));
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
            stb.append(" , COURSE_GROUP_CD AS ( ");
            stb.append(" SELECT ");
            stb.append("     VSCH.YEAR, ");
            stb.append("     VSCH.GRADE, ");
            stb.append("     VSCH.COURSECD, ");
            stb.append("     VSCH.MAJORCD, ");
            stb.append("     VSCH.COURSECODE, ");
            stb.append("     MAX(GROUP_CD) AS COURSE_GROUP_CD "); // コースグループ・・・複数はない前提だが、念のため、複数ある場合の対応
            stb.append(" FROM ");
            stb.append("     COURSE_GROUP_CD_DAT VSCH ");
            stb.append(" WHERE ");
            stb.append("     VSCH.YEAR = '" + param._year + "' ");
            stb.append("     AND VSCH.GRADE = '" + param._grade + "' ");
            stb.append(" GROUP BY ");
            stb.append("     VSCH.YEAR, ");
            stb.append("     VSCH.GRADE, ");
            stb.append("     VSCH.COURSECD, ");
            stb.append("     VSCH.MAJORCD, ");
            stb.append("     VSCH.COURSECODE ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     VSCH.SCHREGNO, ");
            stb.append("     VSCH.GRADE, ");
            stb.append("     VSCH.HR_CLASS, ");
            stb.append("     VSCH.ATTENDNO, ");
            stb.append("     CASE WHEN L6.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END AS ENT_TYPE, "); //1:一貫 0:高入
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
            stb.append("     VALUE(L7.COURSE_GROUP_CD, '000') AS COURSE_GROUP_CD, ");
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
            stb.append("     LEFT JOIN SCHREG_ENT_GRD_HIST_DAT L6 ON L6.SCHREGNO = VSCH.SCHREGNO AND L6.SCHOOL_KIND = 'J' ");
            stb.append("     LEFT JOIN COURSE_GROUP_CD L7 ON VSCH.YEAR = L7.YEAR ");
            stb.append("          AND VSCH.GRADE = L7.GRADE ");
            stb.append("          AND VSCH.COURSECD = L7.COURSECD ");
            stb.append("          AND VSCH.MAJORCD = L7.MAJORCD ");
            stb.append("          AND VSCH.COURSECODE = L7.COURSECODE ");
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

        private static void setMapTestSubclass(final DB2UDB db2, final Param param, final List<Student> studentList) throws SQLException {

            final String sql = TestScore.getRecordScoreSql(param);

            for (final Student student : studentList) {
                for (final Map<String, String> rs : KnjDbUtils.query(db2, sql, new Object[] { student._schregno })) {
                    student._recordScoreSubclass.put(getSubclassCd(rs, param), StringUtils.defaultString(KnjDbUtils.getString(rs, "SCORE")));
                    student._ketsuSubclass.put(getSubclassCd(rs, param), StringUtils.defaultString(KnjDbUtils.getString(rs, "VALUE_DI")));
                }
            }
        }
    }

    private RecordAverageSdivDat getRecordAverageSdivDat(final String avgKey, final Student student, final String subclasscd, final int scoreOrAvgKubun) {
    	RecordAverageSdivDat rtn = _param._recordAverageSdivDatMap.get(RecordAverageSdivDat.averageKey(avgKey, student, subclasscd, scoreOrAvgKubun));
    	if (null == rtn) {
    	return new RecordAverageSdivDat(null, null, null, null);
    	}
    	return rtn;
    }

    private static class RecordAverageSdivDat {

    	static final int KUBUN_SCORE = 1;
    	static final int KUBUN_AVG = 2;

    	/**
    	 *
    	 * @param avgKey
    	 * @param hrClass
    	 * @param coursecd
    	 * @param majorcd
    	 * @param coursecode
    	 * @param subclasscd
    	 * @param scoreOrAvgKubun 1:得点 2:平均
    	 * @return
    	 */
        public static String averageKey(final String avgKey, final String hrClass, final String coursecd, final String majorcd, final String coursecode, final String subclasscd, final int scoreOrAvgKubun) {
            return avgKey + ":" + hrClass + "-" + coursecd + majorcd + coursecode + "-" + subclasscd + "-" + String.valueOf(scoreOrAvgKubun);
        }

        public static String averageKey(final String avgKey, final Student student, final String subclasscd, final int scoreOrAvgKubun) {
            String hrClass = "000";
            String coursecd = "0";
            String majorcd = "000";
            String coursecode = "0000";

            if (AVG_DIV_1_GRADE.equals(avgKey)) {
            } else if (AVG_DIV_2_HR.equals(avgKey)) {
                hrClass = student._hrClass;
            } else if (AVG_DIV_3_COURSE.equals(avgKey)) {
                coursecd = student._courseCd;
                majorcd = student._majorCd;
                coursecode = student._courseCode;
            } else if (AVG_DIV_B_HR_COURSE.equals(avgKey)) {
                hrClass = student._hrClass;
                coursecd = student._courseCd;
                majorcd = student._majorCd;
                coursecode = student._courseCode;
            } else if (AVG_DIV_C_HR_COURSE_KOUNYU.equals(avgKey)) {
                hrClass = student._hrClass;
                coursecd = student._courseCd;
                majorcd = student._majorCd;
                coursecode = student._courseCode;
            } else if (AVG_DIV_D_HR_COURSE_IKKAN.equals(avgKey)) {
                hrClass = student._hrClass;
                coursecd = student._courseCd;
                majorcd = student._majorCd;
                coursecode = student._courseCode;
            } else if (AVG_DIV_E_COURSE_KOUNYU.equals(avgKey)) {
                coursecd = student._courseCd;
                majorcd = student._majorCd;
                coursecode = student._courseCode;
            } else if (AVG_DIV_F_COURSE_IKKAN.equals(avgKey)) {
                coursecd = student._courseCd;
                majorcd = student._majorCd;
                coursecode = student._courseCode;
            }
            return averageKey(avgKey, hrClass, coursecd, majorcd, coursecode, subclasscd, scoreOrAvgKubun);
        }

        final BigDecimal _avg;
        final String _count;
        final String _highScore;
        final String _lowScore;
        RecordAverageSdivDat(final BigDecimal avg, final String count, final String highScore, final String lowScore) {
            _avg = avg;
            _count = count;
            _highScore = highScore;
            _lowScore = lowScore;
        }
    }

    private static class TestScore implements Comparable<TestScore> {
    	final String _subclasscd;
        String _name;
        String _combinedSubclasscd;
        String _electdiv;
        String _score;
        String _rank;
        String _rankHr;
        String _credit;
        String _average;
        String _slumpMark;
        Double _kekka;
        int _jisu;
        final Param _param;
        public TestScore(final Param param, final String subclasscd) {
        	_param = param;
        	_subclasscd = subclasscd;
        }

        public String toString() {
            return "科目：" + _name
                    + "得点：" + _score
                    + " 平均：" + _average
                    + " 席次：" + _rank;
        }

        private static String getRecordScoreSql(final Param param) {
            final String semester = param._testcd.substring(0, 1);
            final String testkindcd = param._testcd.substring(1, 3);
            final String testitemcd = param._testcd.substring(3, 5);
            final String scoreDiv = param._testcd.substring(5, 7);

            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.* ");
            stb.append(" FROM ");
            stb.append("     RECORD_SCORE_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.SEMESTER = '" + semester + "' AND T1.TESTKINDCD = '" + testkindcd + "' AND T1.TESTITEMCD = '" + testitemcd + "' AND T1.SCORE_DIV = '" + scoreDiv + "' ");
            stb.append("     AND T1.SCHREGNO = ? ");

            return stb.toString();
        }

        private static void setRank(final DB2UDB db2, final Param param, final List<Student> studentList) throws SQLException {

            final String sql = getRecordRankTestAppointSql(param);

            for (final Student student : studentList) {

                boolean hasScore = false;
                for (final Map<String, String> rs : KnjDbUtils.query(db2, sql, new Object[] { student._schregno })) {
                    final String subclassCd = getSubclassCd(rs, param);
                    if (subclassCd.equals(SUBCLASS9)) {
                        student._testAllClassCourse._score = KnjDbUtils.getString(rs, "SCORE");
                        student._testAllClassCourse._average = roundHalfUp(KnjDbUtils.getBigDecimal(rs, "AVG", null));

                        student._testAllClassCourseEnt._score = KnjDbUtils.getString(rs, "SCORE");
                        student._testAllClassCourseEnt._average = roundHalfUp(KnjDbUtils.getBigDecimal(rs, "AVG", null));
                    } else if (student._testSubclass.containsKey(subclassCd)) {
                        hasScore = true;
                        final TestScore testScore = student._testSubclass.get(subclassCd);
                        testScore._score = KnjDbUtils.getString(rs, "SCORE");
                        final String avgField = param.getRankAvgField(); //順位の基準点 == 総合点："" , 平均点:"AVG_"
                        testScore._rankHr = KnjDbUtils.getString(rs, "CLASS_" + avgField + "RANK");
                        testScore._rank = GROUP_DIV_CLASS_GRADE.equals(param._groupDiv) ? KnjDbUtils.getString(rs, "GRADE_" + avgField + "RANK") : KnjDbUtils.getString(rs, "COURSE_" + avgField + "RANK");
                    }
                }
                student._hasScore = hasScore;
            }
        }

        /**
         * 席次データの取得
         * @return sql
         */
        public static String getRecordRankTestAppointSql(final Param param) {
        final String semester = param._testcd.substring(0, 1);
        final String testkindcd = param._testcd.substring(1, 3);
        final String testitemcd = param._testcd.substring(3, 5);
        final String scoreDiv = param._testcd.substring(5, 7);

            final StringBuffer stb = new StringBuffer();

            stb.append(" SELECT ");
            stb.append("     T1.* ");
            stb.append(" FROM ");
            stb.append("     RECORD_RANK_SDIV_DAT T1 ");
            stb.append(" LEFT JOIN SCHREG_REGD_DAT REGD ON REGD.SCHREGNO = T1.SCHREGNO ");
            stb.append("     AND REGD.YEAR = '" + param._year + "' ");
            stb.append("     AND REGD.SEMESTER = '" + param._schregSemester + "' ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.SEMESTER = '" + semester + "' AND T1.TESTKINDCD = '" + testkindcd + "' AND T1.TESTITEMCD = '" + testitemcd + "' AND T1.SCORE_DIV = '" + scoreDiv + "' ");
            stb.append("     AND T1.SCHREGNO = ? ");

            return stb.toString();
        }


        private static void setRankDetail(final DB2UDB db2, final Param param, final List<Student> studentList) throws SQLException {
            final String semester = param._testcd.substring(0, 1);
            final String testkindcd = param._testcd.substring(1, 3);
            final String testitemcd = param._testcd.substring(3, 5);
            final String scoreDiv = param._testcd.substring(5, 7);

            final StringBuffer stb = new StringBuffer();

            stb.append(" SELECT ");
            stb.append("     T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, '1' AS RANK_DIV, GRADE_RANK AS RANK, GRADE_AVG_RANK AS AVG_RANK ");
            stb.append(" FROM ");
            stb.append("     RECORD_RANK_SDIV_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.SEMESTER = '" + semester + "' AND T1.TESTKINDCD = '" + testkindcd + "' AND T1.TESTITEMCD = '" + testitemcd + "' AND T1.SCORE_DIV = '" + scoreDiv + "' ");
            stb.append("     AND T1.SCHREGNO = ? ");
            stb.append(" UNION ALL ");
            stb.append(" SELECT ");
            stb.append("     T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, '2' AS RANK_DIV, CLASS_RANK AS RANK, CLASS_AVG_RANK AS AVG_RANK ");
            stb.append(" FROM ");
            stb.append("     RECORD_RANK_SDIV_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.SEMESTER = '" + semester + "' AND T1.TESTKINDCD = '" + testkindcd + "' AND T1.TESTITEMCD = '" + testitemcd + "' AND T1.SCORE_DIV = '" + scoreDiv + "' ");
            stb.append("     AND T1.SCHREGNO = ? ");
            stb.append(" UNION ALL ");
            stb.append(" SELECT ");
            stb.append("     T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, '3' AS RANK_DIV, COURSE_RANK AS RANK, COURSE_AVG_RANK AS AVG_RANK ");
            stb.append(" FROM ");
            stb.append("     RECORD_RANK_SDIV_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.SEMESTER = '" + semester + "' AND T1.TESTKINDCD = '" + testkindcd + "' AND T1.TESTITEMCD = '" + testitemcd + "' AND T1.SCORE_DIV = '" + scoreDiv + "' ");
            stb.append("     AND T1.SCHREGNO = ? ");
            stb.append(" UNION ALL ");
            stb.append(" SELECT ");
            stb.append("     T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, '4' AS RANK_DIV, MAJOR_RANK AS RANK, MAJOR_AVG_RANK AS AVG_RANK ");
            stb.append(" FROM ");
            stb.append("     RECORD_RANK_SDIV_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.SEMESTER = '" + semester + "' AND T1.TESTKINDCD = '" + testkindcd + "' AND T1.TESTITEMCD = '" + testitemcd + "' AND T1.SCORE_DIV = '" + scoreDiv + "' ");
            stb.append("     AND T1.SCHREGNO = ? ");
            stb.append(" UNION ALL ");
            stb.append(" SELECT ");
            stb.append("     T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, '5' AS RANK_DIV, COURSE_GROUP_RANK AS RANK, COURSE_GROUP_AVG_RANK AS AVG_RANK ");
            stb.append(" FROM ");
            stb.append("     RECORD_RANK_SDIV_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.SEMESTER = '" + semester + "' AND T1.TESTKINDCD = '" + testkindcd + "' AND T1.TESTITEMCD = '" + testitemcd + "' AND T1.SCORE_DIV = '" + scoreDiv + "' ");
            stb.append("     AND T1.SCHREGNO = ? ");
            stb.append(" UNION ALL ");
            stb.append(" SELECT ");
            stb.append("     T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, RANK_DIV, RANK, AVG_RANK ");
            stb.append(" FROM ");
            stb.append("     RECORD_RANK_DETAIL_SDIV_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.SEMESTER = '" + semester + "' AND T1.TESTKINDCD = '" + testkindcd + "' AND T1.TESTITEMCD = '" + testitemcd + "' AND T1.SCORE_DIV = '" + scoreDiv + "' ");
            stb.append("     AND T1.SCHREGNO = ? ");

            final String sql = stb.toString();

            for (final Student student : studentList) {
                for (final Map<String, String> row : KnjDbUtils.query(db2, sql,
                        new Object[] { student._schregno, student._schregno, student._schregno, student._schregno,
                                student._schregno, student._schregno })) {
                    final String subclassCd = getSubclassCd(row, param);
                    final String rankDiv = KnjDbUtils.getString(row, "RANK_DIV");
                    final String avgField = param.getRankAvgField(); // 順位の基準点 == 総合点："" , 平均点:"AVG_"

                    getMappedMap(student._rankDivRecordRankSdivDetailDatRankMap, subclassCd).put(rankDiv, KnjDbUtils.getString(row, avgField + "RANK"));
                }
            }
        }

        private static Map<String, RecordAverageSdivDat> setAvg(final DB2UDB db2, final Param param) throws SQLException {
            final Map<String, RecordAverageSdivDat> rtn = new HashMap();
            final String semester = param._testcd.substring(0, 1);
            final String testkindcd = param._testcd.substring(1, 3);
            final String testitemcd = param._testcd.substring(3, 5);
            final String scoreDiv = param._testcd.substring(5, 7);

            final StringBuffer stb = new StringBuffer();

            stb.append(" SELECT ");
            stb.append("     T1.* ");
            stb.append(" FROM ");
            stb.append("     RECORD_AVERAGE_SDIV_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.SEMESTER = '" + semester + "' AND T1.TESTKINDCD = '" + testkindcd + "' AND T1.TESTITEMCD = '" + testitemcd + "' AND T1.SCORE_DIV = '" + scoreDiv + "' ");
            stb.append("     AND T1.GRADE = '" + param._grade + "' ");

            final String sql = stb.toString();
            for (final Map<String, String> row : KnjDbUtils.query(db2, sql)) {
                final String subclassCd = getSubclassCd(row, param);
                final String avgDiv = KnjDbUtils.getString(row, "AVG_DIV");
                final String hrClass = KnjDbUtils.getString(row, "HR_CLASS");
                final String coursecd = KnjDbUtils.getString(row, "COURSECD");
                final String majorcd = KnjDbUtils.getString(row, "MAJORCD");
                final String coursecode = KnjDbUtils.getString(row, "COURSECODE");
                final String averageKey = RecordAverageSdivDat.averageKey(avgDiv, hrClass, coursecd, majorcd, coursecode, subclassCd, RecordAverageSdivDat.KUBUN_SCORE);
                rtn.put(averageKey, new RecordAverageSdivDat(KnjDbUtils.getBigDecimal(row, "AVG", null), KnjDbUtils.getString(row, "COUNT"), KnjDbUtils.getString(row, "HIGHSCORE"), KnjDbUtils.getString(row, "LOWSCORE")));
            }

            for (final String avgDiv : new String[] {AVG_DIV_1_GRADE, AVG_DIV_2_HR, AVG_DIV_3_COURSE, AVG_DIV_B_HR_COURSE, AVG_DIV_C_HR_COURSE_KOUNYU, AVG_DIV_D_HR_COURSE_IKKAN, AVG_DIV_E_COURSE_KOUNYU, AVG_DIV_F_COURSE_IKKAN}) {

            	final StringBuffer stb2 = new StringBuffer();
                stb2.append(" SELECT ");
                stb2.append("     T2.GRADE ");
                if (AVG_DIV_1_GRADE.equals(avgDiv)) {
                	stb2.append("   , '000' AS HR_CLASS, '0' AS COURSECD, '000' AS MAJORCD, '0000' AS COURSECODE ");
                } else if (AVG_DIV_2_HR.equals(avgDiv)) {
                    stb2.append("   , T2.HR_CLASS, '0' AS COURSECD, '000' AS MAJORCD, '0000' AS COURSECODE ");
                } else if (AVG_DIV_3_COURSE.equals(avgDiv)) {
                    stb2.append("   , '000' AS HR_CLASS, T2.COURSECD, T2.MAJORCD, T2.COURSECODE ");
                } else if (AVG_DIV_B_HR_COURSE.equals(avgDiv) || AVG_DIV_C_HR_COURSE_KOUNYU.equals(avgDiv) || AVG_DIV_D_HR_COURSE_IKKAN.equals(avgDiv)) {
                    stb2.append("   , T2.HR_CLASS, T2.COURSECD, T2.MAJORCD, T2.COURSECODE ");
                } else if (AVG_DIV_E_COURSE_KOUNYU.equals(avgDiv) || AVG_DIV_F_COURSE_IKKAN.equals(avgDiv)) {
                	stb2.append("   , '000' AS HR_CLASS, T2.COURSECD, T2.MAJORCD, T2.COURSECODE ");
                }
                stb2.append("   , AVG(T1.AVG) AS AVG ");
                stb2.append("   , COUNT(T1.AVG) AS COUNT ");
                stb2.append("   , MAX(T1.AVG) AS HIGHSCORE ");
                stb2.append("   , MIN(T1.AVG) AS LOWSCORE ");
                stb2.append(" FROM ");
                stb2.append("     RECORD_RANK_SDIV_DAT T1 ");
                stb2.append("     INNER JOIN SCHREG_REGD_DAT T2 ON T2.SCHREGNO = T1.SCHREGNO ");
                stb2.append("         AND T2.YEAR = T1.YEAR ");
                stb2.append("         AND T2.SEMESTER = '" + param._schregSemester + "' ");
                stb2.append("     LEFT JOIN SCHREG_ENT_GRD_HIST_DAT EGHISTJ ON EGHISTJ.SCHREGNO = T1.SCHREGNO ");
                stb2.append("                                    AND EGHISTJ.SCHOOL_KIND = 'J' ");
                stb2.append(" WHERE ");
                stb2.append("     T1.YEAR = '" + param._year + "' ");
                stb2.append("     AND T1.SEMESTER = '" + semester + "' AND T1.TESTKINDCD = '" + testkindcd + "' AND T1.TESTITEMCD = '" + testitemcd + "' AND T1.SCORE_DIV = '" + scoreDiv + "' ");
                stb2.append("     AND T1.SUBCLASSCD = '" + SUBCLASS9 + "' ");
                stb2.append("     AND T2.GRADE = '" + param._grade + "' ");
                if (AVG_DIV_C_HR_COURSE_KOUNYU.equals(avgDiv) || AVG_DIV_E_COURSE_KOUNYU.equals(avgDiv)) {
                    stb2.append("     AND EGHISTJ.SCHREGNO IS NULL ");
                } else if (AVG_DIV_D_HR_COURSE_IKKAN.equals(avgDiv) || AVG_DIV_F_COURSE_IKKAN.equals(avgDiv)) {
                	stb2.append("     AND EGHISTJ.SCHREGNO IS NOT NULL ");
                }
                stb2.append(" GROUP BY ");
                stb2.append("     T2.GRADE ");
                if (AVG_DIV_1_GRADE.equals(avgDiv)) {
                } else if (AVG_DIV_2_HR.equals(avgDiv)) {
                    stb2.append("     , T2.HR_CLASS ");
                } else if (AVG_DIV_3_COURSE.equals(avgDiv)) {
                    stb2.append("     , T2.COURSECD, T2.MAJORCD, T2.COURSECODE ");
                } else if (AVG_DIV_B_HR_COURSE.equals(avgDiv) || AVG_DIV_C_HR_COURSE_KOUNYU.equals(avgDiv) || AVG_DIV_D_HR_COURSE_IKKAN.equals(avgDiv)) {
                    stb2.append("     , T2.HR_CLASS, T2.COURSECD, T2.MAJORCD, T2.COURSECODE ");
                } else if (AVG_DIV_E_COURSE_KOUNYU.equals(avgDiv) || AVG_DIV_F_COURSE_IKKAN.equals(avgDiv)) {
                    stb2.append("     , T2.COURSECD, T2.MAJORCD, T2.COURSECODE ");
                }

                final String sql2 = stb2.toString();
                log.info(" sql2 " + avgDiv + " = " + sql2);
                for (final Map<String, String> row : KnjDbUtils.query(db2, sql2)) {
                    final String subclassCd = SUBCLASS9;
                    final String hrClass = KnjDbUtils.getString(row, "HR_CLASS");
                    final String coursecd = KnjDbUtils.getString(row, "COURSECD");
                    final String majorcd = KnjDbUtils.getString(row, "MAJORCD");
                    final String coursecode = KnjDbUtils.getString(row, "COURSECODE");
                    final String averageKey = RecordAverageSdivDat.averageKey(avgDiv, hrClass, coursecd, majorcd, coursecode, subclassCd, RecordAverageSdivDat.KUBUN_AVG);
                    rtn.put(averageKey, new RecordAverageSdivDat(KnjDbUtils.getBigDecimal(row, "AVG", null), KnjDbUtils.getString(row, "COUNT"), KnjDbUtils.getString(row, "HIGHSCORE"), KnjDbUtils.getString(row, "LOWSCORE")));
                }

            }
            return rtn;
        }

        private static void setChairStd(final DB2UDB db2, final Param param, final List<Student> studentList) throws SQLException {
            PreparedStatement ps;
            String sql = getChairStdSql(param);
            ps = db2.prepareStatement(sql);

            for (final Student student : studentList) {

                for (final Map row : KnjDbUtils.query(db2, ps, new Object[] { student._schregno })) {
                    final String subclassCd = getSubclassCd(row, param);
                    String strKetu = "";
                    if (student._ketsuSubclass.containsKey(subclassCd)) {
                        final String value = (String) student._ketsuSubclass.get(subclassCd);
                        if("*".equals(value)) strKetu = "欠";
                    }
                    if (!param._printTestOnly || param._printTestOnly && student._recordScoreSubclass.containsKey(subclassCd)) {
                        //TODO 試験科目のみ出力する場合、student._recordScoreSubclassにある科目のみが設定される
                        final String score = (String)student._recordScoreSubclass.get(subclassCd);
                        if("".equals(strKetu)) {
                            if("".equals(score)) {
                                continue;
                            } else {
                                strKetu = score;
                            }
                        }
                        final TestScore testScore = new TestScore(param, subclassCd);
                        testScore._score = strKetu;
                        testScore._electdiv = KnjDbUtils.getString(row, "ELECTDIV");
                        testScore._name = KnjDbUtils.getString(row, "SUBCLASSABBV");
                        testScore._combinedSubclasscd = KnjDbUtils.getString(row, "COMBINED_SUBCLASSCD");
                        student._testSubclass.put(subclassCd, testScore);
                    }
                }
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
            stb.append("     L1.ELECTDIV, ");
            stb.append("     L2.COMBINED_SUBCLASSCD, ");
            stb.append("     L1.SUBCLASSABBV ");
            stb.append(" FROM ");
            stb.append("     CHAIR_DAT T1 ");
            stb.append("     LEFT JOIN SUBCLASS_MST L1 ON L1.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("       AND T1.CLASSCD = L1.CLASSCD ");
            stb.append("       AND T1.SCHOOL_KIND = L1.SCHOOL_KIND ");
            stb.append("       AND T1.CURRICULUM_CD = L1.CURRICULUM_CD ");
            stb.append("     LEFT JOIN COMBINED_SUBCLASS L2 ON L2.COMBINED_SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("       AND T1.CLASSCD = L2.COMBINED_CLASSCD ");
            stb.append("       AND T1.SCHOOL_KIND = L2.COMBINED_SCHOOL_KIND ");
            stb.append("       AND T1.CURRICULUM_CD = L2.COMBINED_CURRICULUM_CD ");
            stb.append("     INNER JOIN CHAIR_STD_DAT T2 ON T2.YEAR = T1.YEAR ");
            stb.append("       AND T2.SEMESTER = T1.SEMESTER ");
            stb.append("       AND T2.CHAIRCD = T1.CHAIRCD ");
            stb.append("     INNER JOIN SCHREG_REGD_DAT REGD ON REGD.SCHREGNO = T2.SCHREGNO ");
            stb.append("       AND REGD.YEAR = T1.YEAR ");
            stb.append("       AND REGD.SEMESTER = T1.SEMESTER ");
            if ("1".equals(param._printSubclassLastChairStd)) {
                stb.append("     INNER JOIN SEMESTER_MST STDSEME ON STDSEME.YEAR = T2.YEAR ");
                stb.append("       AND STDSEME.SEMESTER = T2.SEMESTER ");
                stb.append("       AND STDSEME.EDATE = T2.APPENDDATE ");
            }
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' AND ");
            stb.append("     T1.SEMESTER = '" + param._schregSemester + "' AND ");
            stb.append("     substr(T1.SUBCLASSCD,1,2) < '90' AND ");
            stb.append("     T2.SCHREGNO = ? ");
            stb.append(" ORDER BY ");
            stb.append("     T1.CLASSCD, ");
            stb.append("     T1.SCHOOL_KIND, ");
            stb.append("     T1.CURRICULUM_CD, ");
            stb.append("     T1.SUBCLASSCD ");
            return stb.toString();
        }

        public int compareTo(TestScore ts) {
            final int cmp = _subclasscd.compareTo(ts._subclasscd);
            return cmp;
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
        final String _groupDiv;
        final String _outputKijun;
        final String[] _selectData;
        final String _schregSemester;
        private String _semesterName;
        final String _schoolKind;

        /** 出欠集計日付 */
        final String _dateS;
        final String _date;
        /** 試験科目のみ出力する */
        final boolean _printTestOnly;
        /** 注意週数学期 */
        final String _warnSemester;

//        /** 特別活動設定データのマップ */
//        final Map _attendSubclassSpecialMinutes = new HashMap();
//        final Map _attendSubclassSpecialGroupCd = new HashMap();

        private KNJSchoolMst _knjSchoolMst;
        final Map _attendParamMap;

        /** 講座名簿の終了日付が学期の終了日付と同一 */
        final String _printSubclassLastChairStd;

        Map<String, RecordAverageSdivDat> _recordAverageSdivDatMap;
        final String _nendo;

        Param(final DB2UDB db2, final HttpServletRequest request) {
            _year = request.getParameter("YEAR");
            _categoryIsClass = request.getParameter("CATEGORY_IS_CLASS");
            _testcd = request.getParameter("SUB_TESTCD");
            _semester = request.getParameter("SEMESTER");
            _schregSemester = "9".equals(_semester) ? request.getParameter("CTRL_SEME") : _semester;
            _testName = getTestName(db2);
            setSemesterName(db2);
            _grade = request.getParameter("GRADE");
            _hrClass = request.getParameter("HR_CLASS");
            _groupDiv = request.getParameter("GROUP_DIV");
            _outputKijun = request.getParameter("OUTPUT_KIJUN");
            _selectData = request.getParameterValues("CATEGORY_SELECTED");  //学籍番号または学年-組
            _schoolKind = getSchoolKind(db2);

            _dateS = request.getParameter("SDATE").replace('/', '-');
            _date = request.getParameter("DATE").replace('/', '-');
            _printTestOnly = true; //試験科目のみ出力する

//            setAttendSubclassSpecialMap(db2);

            _printSubclassLastChairStd = request.getParameter("printSubclassLastChairStd");

            try {
                _knjSchoolMst = new KNJSchoolMst(db2, _year);
            } catch (Exception e) {
                log.fatal("exception!", e);
            }

            _warnSemester = setWarnSemester(db2);

            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);

            _nendo = KNJ_EditDate.getAutoFormatYear(db2, Integer.parseInt(_year)) + "年度";
        }

        private static String getDbPrginfoProperties(final DB2UDB db2, final String propName) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJD192E' AND NAME = '" + propName + "' "));
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

                _warnSemester = KnjDbUtils.getString(KnjDbUtils.firstRow(KnjDbUtils.query(db2, stb.toString())), "SEMESTER");
            }
            if (null == _warnSemester) {
                _warnSemester = _knjSchoolMst._semesterDiv;
            }
            if (NumberUtils.isDigits(_warnSemester) && Integer.parseInt(_warnSemester) > 3) {
                _warnSemester = "3";
            }
            return _warnSemester;
        }

        private String getTestName(final DB2UDB db2) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT TESTITEMNAME FROM TESTITEM_MST_COUNTFLG_NEW_SDIV WHERE YEAR = '" + _year + "' AND SEMESTER || TESTKINDCD || TESTITEMCD || SCORE_DIV = '" + _testcd + "' "));
        }

        private void setSemesterName(final DB2UDB db2) {
            _semesterName = KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT SEMESTERNAME FROM SEMESTER_MST WHERE YEAR = '" + _year + "' AND SEMESTER = '" + _semester + "' "));
        }

        private String getRankAvgField() {
            return "2".equals(_outputKijun) ? "AVG_" : "";
        }

        private String getRankName() {
            if (GROUP_DIV_CLASS_GRADE.equals(_groupDiv)) {
                return "学年";
            } else if (GROUP_DIV_CLASS_COURSE.equals(_groupDiv)) {
                return "コース";
            }
            return null;
        }

        private String getSchoolKind(final DB2UDB db2) {
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND GRADE = '" + _grade + "' ");

            return KnjDbUtils.getOne(KnjDbUtils.query(db2, sql.toString()));
        }
    }
}

// eof
