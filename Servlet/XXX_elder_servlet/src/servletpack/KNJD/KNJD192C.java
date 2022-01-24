// kanji=漢字
/*
 * $Id: ca1f41b55997d5d00d567837f4c5004ab0cb4225 $
 *
 * 作成日: 2011/04/27 17:21:20 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2011 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJD.detail.getReportCardInfoTottori;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletpacksvfANDdb2;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: ca1f41b55997d5d00d567837f4c5004ab0cb4225 $
 */
public class KNJD192C {

    private static final Log log = LogFactory.getLog("KNJD192C.class");

    private boolean _hasData;

    Param _param;

    private static final String SPECIAL_ALL = "999";
    private static final String SCORE = "SCORE";
    private static final String AVG = "AVG";
    private static final String RANK = "RANK";
    private static final String RANK2 = "RANK2";
    private static final String CREDIT = "CREDIT";
    private static final String KEKKA = "KEKKA";

    private static final String AVG_GRADE = "1";
    private static final String AVG_HR = "2";
    private static final String AVG_COURSE = "3";
    private static final String AVG_MAJOR = "4";

    private static final String SSEMESTER = "1";
    private static final String ATTEND_OBJ_SEM = "9";

    private static final String SUBCLASS3   = "333333";
    private static final String SUBCLASS5   = "555555";
    private static final String SUBCLASSALL = "999999";
    private KNJSchoolMst _knjSchoolMst;

    public void svf_out(HttpServletRequest request, HttpServletResponse response) throws Exception {
        final KNJServletpacksvfANDdb2 sd = new KNJServletpacksvfANDdb2();  //帳票におけるＳＶＦおよびＤＢ２の設定
        DB2UDB db2 = null; //Databaseクラスを継承したクラス
        Vrw32alp svf = new Vrw32alp();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        try {

            // print svf設定
            sd.setSvfInit(request, response, svf);
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);

            _knjSchoolMst = new KNJSchoolMst(db2, _param._year);

            //SVF出力
            _hasData = printMain(response, db2, svf);

        } finally {
            sd.closeSvf(svf, _hasData);
            sd.closeDb(db2);
        }

    }// doGetの括り

    /**
     * @param response
     * @param db2
     */
    private boolean printMain(final HttpServletResponse response, final DB2UDB db2, final Vrw32alp svf) throws SQLException, ParseException {
        boolean hasData = false;
        final List studentList = getStudentList(db2);
        final List printDataList = getPrintData(db2, studentList);
        setHead(svf);
        int cnt = 0;
        String befGradeClass = "";
        for (final Iterator iter = printDataList.iterator(); iter.hasNext();) {
            final Student student = (Student) iter.next();

            if (hasData && !befGradeClass.equals(student._grade + student._hrClass) && 0 != cnt) {
                cnt = pageChange(svf);
            }

            cnt++;
            setPrintOut(svf, student, cnt);

            if (cnt == 3) {
                cnt = pageChange(svf);
            }

            befGradeClass = student._grade + student._hrClass;
            hasData = true;
        }
        if (cnt > 0) {
            svf.VrEndPage();
        }
        return hasData;
    }

    private int pageChange(final Vrw32alp svf) {
        svf.VrEndPage();
        return 0;
    }

    private void setPrintOut(final Vrw32alp svf, final Student student, final int fieldNo) {
        log.debug(student);
        svf.VrsOutn("NENDO", fieldNo, _param.changePrintYear());
        svf.VrsOutn("SEMESTER", fieldNo, _param._semesterName);
        svf.VrsOutn("TESTNAME", fieldNo, _param._testName);
        final String rankName = _param.getRankName();
        svf.VrsOutn("CLS_AVG_NAME", fieldNo, rankName);
        final String rankName2 = _param.getRankName2();
        svf.VrsOutn("COURSE_NUM_NAME", fieldNo, rankName2);
        final String rankName3 = _param.getRankName3();
        svf.VrsOutn("MAJOR_NUM_NAME", fieldNo, rankName3);
        final String hrName = student._hrName + "(" + student._attendNo + ")";
        svf.VrsOutn("HR_NAME", fieldNo, hrName);
        svf.VrsOutn("NAME", fieldNo, student._name);

        final Map testSub = student._testSubclass;
        int fieldCnt = 1;
        int kettenSubclassCount = 0;
        if (null != testSub) {
            for (final Iterator itSubclass = testSub.keySet().iterator(); itSubclass.hasNext();) {
                final String subclassCd = (String) itSubclass.next();
                final TestScore testScore = (TestScore) testSub.get(subclassCd);
                final String credit = null != testScore._credit ? "(" + testScore._credit + ")" : "";
                final String subName = null != testScore._name ? testScore._name : "";
                svf.VrsOutn("SUBCLASS" + fieldCnt, fieldNo, subName + credit);
                svf.VrsOutn("SCORE" + fieldCnt, fieldNo, testScore._score);
                svf.VrsOutn("CLASS_AVERAGE" + fieldCnt, fieldNo, testScore._hrAvg);
                svf.VrsOutn("COURSE_NUM" + fieldCnt, fieldNo, testScore._courseCnt);
                svf.VrsOutn("COURSE_AVERAGE" + fieldCnt, fieldNo, testScore._courseAvg);
                svf.VrsOutn("COURSE_MAX" + fieldCnt, fieldNo, testScore._courseHighScore);
                svf.VrsOutn("COURSE_MIN" + fieldCnt, fieldNo, testScore._courseLowScore);
                svf.VrsOutn("MAJOR_NUM" + fieldCnt, fieldNo, testScore._majorCnt);
                svf.VrsOutn("MAJOR_AVERAGE" + fieldCnt, fieldNo, testScore._majorAvg);
                svf.VrsOutn("MAJOR_MAX" + fieldCnt, fieldNo, testScore._majorHighScore);
                svf.VrsOutn("MAJOR_MIN" + fieldCnt, fieldNo, testScore._majorLowScore);
                if (!"欠".equals(testScore._score) && !"".equals(testScore._score) && null != testScore._score) {
                    int score = Integer.parseInt(testScore._score);
                    if (testScore.isKetten(score)) {
                        svf.VrAttributen("SCORE" + fieldCnt, fieldNo, "Paint=(1,70,1),Bold=1");
                        kettenSubclassCount += 1;
                    }
                } else if ("欠".equals(testScore._score) && _param.isCountKetsu()) {
                    kettenSubclassCount += 1;
                }

//                svf.VrsOutn("AVERAGE" + fieldCnt, fieldNo, testScore._hrAvg);
//                svf.VrsOutn("RANK" + fieldCnt, fieldNo, testScore._hrRank);
//                svf.VrsOutn("CLASS_AVERAGE" + fieldCnt, fieldNo, (_param.isPrintChair()) ? testScore._majorAvg : testScore._courseAvg);
//                svf.VrsOutn("CLASS_RANK" + fieldCnt, fieldNo, (_param.isPrintChair()) ? testScore._majorRank : testScore._courseRank);
                fieldCnt++;
//                log.debug(testScore);
            }
        }

//        svf.VrsOutn("CLASS_AVERAGE", fieldNo, student._testAll._hrAvg);
//        svf.VrsOutn("TOTAL_AVERAGE", fieldNo, student._testAll._courseAvg);

        if (student._hasScore) {
            svf.VrsOutn("TOTAL_SCORE", fieldNo, student._testAll._score);
            svf.VrsOutn("AVERAGEL_SCORE", fieldNo, student._testAll._average);

            svf.VrsOutn("CLASS_AVERAGE", fieldNo, student._testAll._hrAvg);
            svf.VrsOutn("CLASS_RANK", fieldNo, student._testAll._hrRank);
            svf.VrsOutn("CLASS_STUDENT", fieldNo, student._testAll._hrCnt);

            svf.VrsOutn("TOTAL_AVERAGE", fieldNo, student._testAll._courseAvg);
            svf.VrsOutn("TOTAL_RANK", fieldNo, student._testAll._courseRank);
            svf.VrsOutn("TOTAL_STUDENT", fieldNo, student._testAll._courseCnt);

//            if (!_param.isPrintChair()) {
//                svf.VrsOutn("CLASS_RANK", fieldNo, student._testAll._chairHrRank);
//            }
            svf.VrsOutn("FAIL", fieldNo, String.valueOf(kettenSubclassCount));
        }
    }

    private void setHead(final Vrw32alp svf) {
        final String form = _param._printForm15 ? "KNJD192C.frm" : "KNJD192C_2.frm";
        svf.VrSetForm(form, 1);
    }
    
    private String getSubclasscd(final ResultSet rs) throws SQLException {
        final String subclassCd = rs.getString("SUBCLASSCD");
        if (SUBCLASSALL.equals(subclassCd) || SUBCLASS3.equals(subclassCd) || SUBCLASS5.equals(subclassCd)) {
            return subclassCd;
        }
        if ("1".equals(_param._useCurriculumcd)) {
            return rs.getString("CLASSCD") + "-" + rs.getString("SCHOOL_KIND") + "-" + rs.getString("CURRICULUM_CD") + "-" + subclassCd;
        }
        return subclassCd;
    }

    private List getPrintData(final DB2UDB db2, final List studentList) throws SQLException, ParseException {
        List printDataList = new ArrayList();
        for (final Iterator iter = studentList.iterator(); iter.hasNext();) {
            final Student student = (Student) iter.next();

            setChairStd(db2, student);
            if (!_param._printTestOnly) {
                setScore(db2, student);//初期値："欠"をセット
            }
            setRank(db2, student);
            setRankChair(db2, student);

            //RECORD_AVERAGE_DAT
            final String gradeSql = getReportCardInfoTottori.getRecordAverageTestAppointSql(_param._year, _param._semester, student._schregno, null, null, AVG_GRADE, student._grade, student._hrClass, null, _param._kindItem, _param._tableDiv);
            setAvg(db2, student, gradeSql, AVG_GRADE);

            //RECORD_AVERAGE_DAT
            final String hrSql = getReportCardInfoTottori.getRecordAverageTestAppointSql(_param._year, _param._semester, student._schregno, null, null, AVG_HR, student._grade, student._hrClass, null, _param._kindItem, _param._tableDiv);
            setAvg(db2, student, hrSql, AVG_HR);

            //RECORD_AVERAGE_DAT
            final String course = student._courseCd + student._majorCd + student._courseCode;
            final String courseSql = getReportCardInfoTottori.getRecordAverageTestAppointSql(_param._year, _param._semester, student._schregno, null, null, AVG_COURSE, student._grade, student._hrClass, course, _param._kindItem, _param._tableDiv);
            setAvg(db2, student, courseSql, AVG_COURSE);

            //RECORD_AVERAGE_DAT
            final String major = student._courseCd + student._majorCd;
            final String majorSql = getReportCardInfoTottori.getRecordAverageTestAppointSql(_param._year, _param._semester, student._schregno, null, null, AVG_MAJOR, student._grade, student._hrClass, major, _param._kindItem, _param._tableDiv);
            setAvg(db2, student, majorSql, AVG_MAJOR);

//            setAvgChair(db2, student);
            if (!_param.isKetten()) {
                setKetten(db2, student);
            }
            printDataList.add(student);
        }
        return printDataList;
    }

    private void setChairStd(final DB2UDB db2, final Student student) throws SQLException {
        ResultSet scoreRs = null;
        PreparedStatement psScore = null;
        try {
            String strKetu = "";
            String scoreSql = "";
            if (_param._printTestOnly) {
                strKetu = "欠";
                scoreSql = getReportCardInfoTottori.getRecordScoreTestAppointSql(_param._year, _param._semester, student._schregno, null, null, _param._scoreDiv, _param._kindItem);
            } else {
                scoreSql = getChairStdSql(student);
            }
            psScore = db2.prepareStatement(scoreSql);
            final String creditSql = getCredit(student);
            scoreRs = psScore.executeQuery();
            while (scoreRs.next()) {
                final String subclassCd = getSubclasscd(scoreRs);
                final TestScore testScore = new TestScore(strKetu, SCORE);
                testScore._name = scoreRs.getString("SUBCLASSABBV");
                student._testSubclass.put(subclassCd, testScore);
            }
            DbUtils.closeQuietly(null, psScore, scoreRs);
            PreparedStatement psCredit = null;
            psCredit = db2.prepareStatement(creditSql);
            PreparedStatement psAbsenceHigh = null;
            final String absenceHighSql = getAbsenceHighSql(student);
            psAbsenceHigh = db2.prepareStatement(absenceHighSql);
            PreparedStatement psChaircd = null;
            final String chaircdSql = getChaircdSql(student);
            psChaircd = db2.prepareStatement(chaircdSql);
            
            for (Iterator it = student._testSubclass.keySet().iterator(); it.hasNext();) {
                final String subclassCd = (String) it.next();
                TestScore testScore = (TestScore) student._testSubclass.get(subclassCd);
                int creInt = 1;
                //単位
                if ("1".equals(_param._useCurriculumcd)) {
                    String classcd = null;
                    String schoolKind = null;
                    String curriculumCd = null;
                    String rawSubclassCd = subclassCd;
                    if (StringUtils.split(subclassCd, "-").length == 4) {
                        String[] arr = StringUtils.split(subclassCd, "-");
                        classcd = arr[0];
                        schoolKind = arr[1];
                        curriculumCd = arr[2];
                        rawSubclassCd = arr[3];
                    }
                    psCredit.setString(creInt++, classcd);
                    psCredit.setString(creInt++, schoolKind);
                    psCredit.setString(creInt++, curriculumCd);
                    psCredit.setString(creInt++, rawSubclassCd);
                } else {
                    psCredit.setString(creInt++, subclassCd.substring(0, 2));
                    psCredit.setString(creInt++, subclassCd);
                }
                ResultSet creditRs = psCredit.executeQuery();
                while (creditRs.next()) {
                    testScore._credit = creditRs.getString("CREDITS");
                    if (_knjSchoolMst.isHoutei()) {
                        testScore._absenceHigh = creditRs.getString("ABSENCE_HIGH");
                        testScore._getAbsenceHigh = creditRs.getString("GET_ABSENCE_HIGH");
                    }
                }
                DbUtils.closeQuietly(creditRs);
            }
            DbUtils.closeQuietly(psCredit);
            
            if (_knjSchoolMst.isJitu()) {
                for (Iterator it = student._testSubclass.keySet().iterator(); it.hasNext();) {
                    final String subclassCd = (String) it.next();
                    TestScore testScore = (TestScore) student._testSubclass.get(subclassCd);
                    //実・欠課数上限値
                    if ("1".equals(_param._useCurriculumcd)) {
                        String classcd = null;
                        String schoolKind = null;
                        String curriculumCd = null;
                        String rawSubclassCd = subclassCd;
                        if (StringUtils.split(subclassCd, "-").length == 4) {
                            String[] arr = StringUtils.split(subclassCd, "-");
                            classcd = arr[0];
                            schoolKind = arr[1];
                            curriculumCd = arr[2];
                            rawSubclassCd = arr[3];
                        }
                        int i = 1;
                        psAbsenceHigh.setString(i++, classcd);
                        psAbsenceHigh.setString(i++, schoolKind);
                        psAbsenceHigh.setString(i++, curriculumCd);
                        psAbsenceHigh.setString(i++, rawSubclassCd);
                    } else {
                        psAbsenceHigh.setString(1, subclassCd);
                    }
                    ResultSet absenceHighRs = psAbsenceHigh.executeQuery();
                    while (absenceHighRs.next()) {
                        testScore._absenceHigh = absenceHighRs.getString("COMP_ABSENCE_HIGH");
                        testScore._getAbsenceHigh = absenceHighRs.getString("GET_ABSENCE_HIGH");
                    }
                    DbUtils.closeQuietly(absenceHighRs);
                }
                DbUtils.closeQuietly(psAbsenceHigh);
            }
            
            for (Iterator it = student._testSubclass.keySet().iterator(); it.hasNext();) {
                final String subclassCd = (String) it.next();
                TestScore testScore = (TestScore) student._testSubclass.get(subclassCd);
                //講座
                if ("1".equals(_param._useCurriculumcd)) {
                    String classcd = null;
                    String schoolKind = null;
                    String curriculumCd = null;
                    String rawSubclassCd = subclassCd;
                    if ("1".equals(_param._useCurriculumcd) && StringUtils.split(subclassCd, "-").length == 4) {
                        String[] arr = StringUtils.split(subclassCd, "-");
                        classcd = arr[0];
                        schoolKind = arr[1];
                        curriculumCd = arr[2];
                        rawSubclassCd = arr[3];
                    }
                    int i = 1;
                    psChaircd.setString(i++, classcd);
                    psChaircd.setString(i++, schoolKind);
                    psChaircd.setString(i++, curriculumCd);
                    psChaircd.setString(i++, rawSubclassCd);
                } else {
                    psChaircd.setString(1, subclassCd);
                }
                ResultSet chaircdRs = null;
                chaircdRs = psChaircd.executeQuery();
                while (chaircdRs.next()) {
                    testScore._chaircd = chaircdRs.getString("CHAIRCD");
                }
                DbUtils.closeQuietly(chaircdRs);
            }
            DbUtils.closeQuietly(psChaircd);
        } finally {
            db2.commit();
        }
    }

    private String getChairStdSql(final Student student) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT DISTINCT ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     T1.CLASSCD, ");
            stb.append("     T1.SCHOOL_KIND, ");
            stb.append("     T1.CURRICULUM_CD, ");
        }
        stb.append("     T1.SUBCLASSCD, ");
        stb.append("     L1.SUBCLASSABBV ");
        stb.append(" FROM ");
        stb.append("     CHAIR_DAT T1 ");
        stb.append("     LEFT JOIN SUBCLASS_MST L1 ON L1.SUBCLASSCD = T1.SUBCLASSCD ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     AND L1.CLASSCD = T1.CLASSCD ");
            stb.append("     AND L1.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("     AND L1.CURRICULUM_CD = T1.CURRICULUM_CD ");
        }
        stb.append("     , CHAIR_STD_DAT T2 ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' AND ");
        stb.append("     T1.SEMESTER = '" + _param._schregSemester + "' AND ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     T1.CLASSCD < '90' AND ");
        } else {
            stb.append("     substr(T1.SUBCLASSCD,1,2) < '90' AND ");
        }
        stb.append("     T2.YEAR = T1.YEAR AND ");
        stb.append("     T2.SEMESTER = T1.SEMESTER AND ");
        stb.append("     T2.CHAIRCD = T1.CHAIRCD AND ");
        stb.append("     T2.SCHREGNO = '" + student._schregno + "' ");
        stb.append(" ORDER BY ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     T1.CLASSCD, ");
            stb.append("     T1.SCHOOL_KIND, ");
            stb.append("     T1.CURRICULUM_CD, ");
        }
        stb.append("     T1.SUBCLASSCD ");

        return stb.toString();
    }

    private String getCredit(final Student student) {
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
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.COURSECD = '" + student._courseCd + "' ");
        stb.append("     AND T1.MAJORCD = '" + student._majorCd + "' ");
        stb.append("     AND T1.GRADE = '" + student._grade + "' ");
        stb.append("     AND T1.COURSECODE = '" + student._courseCode + "' ");
        stb.append("     AND T1.CLASSCD = ? ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     AND T1.SCHOOL_KIND = ? ");
            stb.append("     AND T1.CURRICULUM_CD = ? ");
        }
        stb.append("     AND T1.SUBCLASSCD = ? ");

        return stb.toString();
    }

    private String getAbsenceHighSql(final Student student) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     COMP_ABSENCE_HIGH, GET_ABSENCE_HIGH ");
        stb.append(" FROM ");
        stb.append("     SCHREG_ABSENCE_HIGH_DAT T1 ");
        stb.append(" WHERE ");
        stb.append("     YEAR = '" + _param._year + "' AND ");
        stb.append("     DIV = '2' AND "); // 1:年間、2:随時
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     T1.CLASSCD = ? AND ");
            stb.append("     T1.SCHOOL_KIND = ? AND ");
            stb.append("     T1.CURRICULUM_CD = ? AND ");
        }
        stb.append("     SUBCLASSCD = ? AND ");
        stb.append("     SCHREGNO = '" + student._schregno + "' ");

        return stb.toString();
    }

    private String getChaircdSql(final Student student) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     min(T1.CHAIRCD) as CHAIRCD ");
        stb.append(" FROM ");
        stb.append("     CHAIR_DAT T1, ");
        stb.append("     CHAIR_STD_DAT T2 ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' AND ");
        stb.append("     T1.SEMESTER = '" + _param._schregSemester + "' AND ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     T1.CLASSCD = ? AND ");
            stb.append("     T1.SCHOOL_KIND = ? AND ");
            stb.append("     T1.CURRICULUM_CD = ? AND ");
        }
        stb.append("     T1.SUBCLASSCD = ? AND ");
        stb.append("     T2.YEAR = T1.YEAR AND ");
        stb.append("     T2.SEMESTER = T1.SEMESTER AND ");
        stb.append("     T2.CHAIRCD = T1.CHAIRCD AND ");
        stb.append("     T2.SCHREGNO = '" + student._schregno + "' ");

        return stb.toString();
    }

    private void setScore(final DB2UDB db2, final Student student) throws SQLException {
        ResultSet scoreRs = null;
        PreparedStatement psScore = null;
        try {
            final String scoreSql = getReportCardInfoTottori.getRecordScoreTestAppointSql(_param._year, _param._semester, student._schregno, null, null, _param._scoreDiv, _param._kindItem);
            psScore = db2.prepareStatement(scoreSql);
            scoreRs = psScore.executeQuery();
            while (scoreRs.next()) {
                final String subclassCd = getSubclasscd(scoreRs);
                if (null != student._testSubclass && student._testSubclass.containsKey(subclassCd)) {
                    TestScore testScore = (TestScore) student._testSubclass.get(subclassCd);
                    testScore._score = "欠";
                }
            }
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, psScore, scoreRs);
        }
    }

    private void setRank(final DB2UDB db2, final Student student) throws SQLException {
        ResultSet rankRs = null;
        try {
            final String rankSql = getReportCardInfoTottori.getRecordRankTestAppointSql(_param._year, _param._semester, student._schregno, null, null, _param._kindItem, _param._tableDiv);
//            log.debug(rankSql);
            db2.query(rankSql);
            rankRs = db2.getResultSet();
            boolean hasScore = false;
            while (rankRs.next()) {
                final String subclassCd = getSubclasscd(rankRs);
                TestScore testScore = null;
                if (null != student._testSubclass && student._testSubclass.containsKey(subclassCd)) {
                    hasScore = true;
                    testScore = (TestScore) student._testSubclass.get(subclassCd);
                } else if (subclassCd.equals(SUBCLASSALL)) {
                    testScore = student._testAll;
                    testScore._average = null == rankRs.getString("AVG") ? null : new BigDecimal(rankRs.getDouble("AVG")).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                }
                if (null != testScore) {
                    testScore._score = rankRs.getString("SCORE");
                    testScore._gradeRank = rankRs.getString("GRADE_" + _param.getRankAvgField() + "RANK");
                    testScore._hrRank = rankRs.getString("CLASS_" + _param.getRankAvgField() + "RANK");
                    testScore._courseRank = rankRs.getString("COURSE_" + _param.getRankAvgField() + "RANK");
                    testScore._majorRank = rankRs.getString("MAJOR_" + _param.getRankAvgField() + "RANK");
                }
            }
            student._hasScore = student._hasScore || hasScore;
        } finally {
            db2.commit();
        }
    }

    private void setRankChair(final DB2UDB db2, final Student student) throws SQLException {
        ResultSet rankRs = null;
        try {
            final String rankSql = getReportCardInfoTottori.getRecordRankChairTestAppointSql(_param._year, _param._semester, student._schregno, null, null, _param._kindItem, _param._tableDiv);
//            log.debug(rankSql);
            db2.query(rankSql);
            rankRs = db2.getResultSet();
            boolean hasScore = false;
            while (rankRs.next()) {
                final String subclassCd = getSubclasscd(rankRs);
                final String chairCd = rankRs.getString("CHAIRCD");
                if (null != student._testSubclass && student._testSubclass.containsKey(subclassCd)) {
                    TestScore testScore = (TestScore) student._testSubclass.get(subclassCd);

                    if (chairCd.equals(testScore._chaircd)) {
                        hasScore = true;
                        testScore._chairGradeRank = rankRs.getString("GRADE_" + _param.getRankAvgField() + "RANK");
                        testScore._chairHrRank = rankRs.getString("CLASS_" + _param.getRankAvgField() + "RANK");
                        testScore._chairCourseRank = rankRs.getString("COURSE_" + _param.getRankAvgField() + "RANK");
                        testScore._chairMajorRank = rankRs.getString("MAJOR_" + _param.getRankAvgField() + "RANK");
                    }
                }
            }
            student._hasScore = student._hasScore || hasScore;
            
        } finally {
            db2.commit();
        }
    }

    private void setAvg(final DB2UDB db2, final Student student, final String sql, final String avgDiv) throws SQLException {
        ResultSet rankRs = null;
        // 学年/コースの平均
        try {
            db2.query(sql);
            rankRs = db2.getResultSet();
            while (rankRs.next()) {
                final String subclassCd = getSubclasscd(rankRs);
                final String avg = null == rankRs.getString("AVG") ? null : new BigDecimal(rankRs.getString("AVG")).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                final String count = rankRs.getString("COUNT");
                if (null != student._testSubclass && student._testSubclass.containsKey(subclassCd)) {
                    TestScore testScore = (TestScore) student._testSubclass.get(subclassCd);
                    if (AVG_GRADE.equals(avgDiv)) {
                        testScore._gradeCnt = count;
                        testScore._gradeAvg = avg;
                        testScore._gradeHighScore = rankRs.getString("HIGHSCORE");
                        testScore._gradeLowScore = rankRs.getString("LOWSCORE");
                    } else if (AVG_HR.equals(avgDiv)) {
                        testScore._hrCnt = count;
                        testScore._hrAvg = avg;
                        testScore._hrHighScore = rankRs.getString("HIGHSCORE");
                        testScore._hrLowScore = rankRs.getString("LOWSCORE");
                    } else if (AVG_COURSE.equals(avgDiv)) {
                        testScore._courseCnt = count;
                        testScore._courseAvg = avg;
                        testScore._courseHighScore = rankRs.getString("HIGHSCORE");
                        testScore._courseLowScore = rankRs.getString("LOWSCORE");
                    } else if (AVG_MAJOR.equals(avgDiv)) {
                        testScore._majorCnt = count;
                        testScore._majorAvg = avg;
                        testScore._majorHighScore = rankRs.getString("HIGHSCORE");
                        testScore._majorLowScore = rankRs.getString("LOWSCORE");
                    }
                }
                if (subclassCd.equals(SUBCLASSALL)) {
                    if (AVG_GRADE.equals(avgDiv)) {
                        student._testAll._gradeAvg = avg;
                        student._testAll._gradeCnt = count;
                    } else if (AVG_HR.equals(avgDiv)) {
                        student._testAll._hrAvg = avg;
                        student._testAll._hrCnt = count;
                    } else if (AVG_COURSE.equals(avgDiv)) {
                        student._testAll._courseAvg = avg;
                        student._testAll._courseCnt = count;
                    } else if (AVG_MAJOR.equals(avgDiv)) {
                        student._testAll._majorAvg = avg;
                        student._testAll._majorCnt = count;
                    }
                }
            }
        } finally {
            db2.commit();
        }

    }

    private void setAvgChair(final DB2UDB db2, final Student student) throws SQLException {
        ResultSet rankRs = null;
        // 講座の平均
        try {
            final String rankSql = getReportCardInfoTottori.getRecordAverageChairTestAppointSql(_param._year, _param._semester, student._schregno, null, null, "1", student._grade, student._hrClass, null, _param._kindItem, _param._tableDiv);
            db2.query(rankSql);
            rankRs = db2.getResultSet();
            while (rankRs.next()) {
                final String subclassCd = getSubclasscd(rankRs);
                final String chairCd = rankRs.getString("CHAIRCD");
                if (null != student._testSubclass && student._testSubclass.containsKey(subclassCd)) {
                    TestScore testScore = (TestScore) student._testSubclass.get(subclassCd);

                    if (chairCd.equals(testScore._chaircd)) {
                        if (rankRs.getString("AVG") != null) {
                            BigDecimal avg = new BigDecimal(rankRs.getDouble("AVG"));
                            testScore._majorAvg = avg.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                        }
                    }
                }
            }
        } finally {
            db2.commit();
        }
    }

    private void setKetten(final DB2UDB db2, final Student student) throws SQLException {
        ResultSet rs = null;
        // 欠点対象
        try {
            final String course = student._courseCd + student._majorCd + student._courseCode;
            final String sql = getKettenSql(student._schregno, student._grade, course);
            db2.query(sql);
            rs = db2.getResultSet();
            while (rs.next()) {
                final String subclassCd = getSubclasscd(rs);
                if (null != student._testSubclass && student._testSubclass.containsKey(subclassCd)) {
                    TestScore testScore = (TestScore) student._testSubclass.get(subclassCd);
                    testScore._slump = rs.getString("SLUMP");
                    testScore._passScore = rs.getString("PASS_SCORE");
                }
            }
        } finally {
            db2.commit();
        }
    }

    private String getKettenSql(final String schregno, final String grade, final String course) {
        final StringBuffer stb = new StringBuffer();
        if (_param.isRecordSlump()) {
            //成績不振科目データの表
            stb.append(" SELECT ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("     CLASSCD, ");
                stb.append("     SCHOOL_KIND, ");
                stb.append("     CURRICULUM_CD, ");
            }
            stb.append("     SUBCLASSCD, ");
            stb.append("     SLUMP, ");
            stb.append("     cast(null as smallint) as PASS_SCORE ");
            stb.append(" FROM ");
            stb.append("     RECORD_SLUMP_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _param._year + "' AND ");
            stb.append("     SEMESTER = '" + _param._semester + "' AND ");
            stb.append("     TESTKINDCD || TESTITEMCD = '" + _param.getRecordSlumpTestcd() + "' AND ");
            stb.append("     SCHREGNO = '" + schregno + "' ");
        }
        if (_param.isPerfectRecord()) {
            //満点マスタの表
            stb.append(" WITH PERFECT_T AS ( ");
            stb.append(" SELECT ");
            stb.append("     YEAR, ");
            stb.append("     SEMESTER, ");
            stb.append("     TESTKINDCD || TESTITEMCD AS TESTCD, ");
            stb.append("     CLASSCD, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("     SCHOOL_KIND, ");
                stb.append("     CURRICULUM_CD, ");
            }
            stb.append("     SUBCLASSCD, ");
            stb.append("     MIN(DIV) AS DIV ");
            stb.append(" FROM ");
            stb.append("     PERFECT_RECORD_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _param._year + "' ");
            stb.append("     AND SEMESTER = '" + _param._semester + "' ");
            stb.append("     AND TESTKINDCD || TESTITEMCD = '" + _param._kindItem + "' ");
            stb.append(" GROUP BY ");
            stb.append("     YEAR, ");
            stb.append("     SEMESTER, ");
            stb.append("     TESTKINDCD || TESTITEMCD, ");
            stb.append("     CLASSCD, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("     SCHOOL_KIND, ");
                stb.append("     CURRICULUM_CD, ");
            }
            stb.append("     SUBCLASSCD ");
            stb.append(" ) ");

            stb.append(" SELECT DISTINCT ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("     CLASSCD, ");
                stb.append("     SCHOOL_KIND, ");
                stb.append("     CURRICULUM_CD, ");
            }
            stb.append("     T1.SUBCLASSCD, ");
            stb.append("     cast(null as varchar(1)) as SLUMP, ");
            stb.append("     T1.PASS_SCORE ");
            stb.append(" FROM ");
            stb.append("     PERFECT_RECORD_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("     EXISTS( ");
            stb.append("         SELECT ");
            stb.append("             'x' ");
            stb.append("         FROM ");
            stb.append("             PERFECT_T E1 ");
            stb.append("         WHERE ");
            stb.append("             E1.YEAR = T1.YEAR ");
            stb.append("             AND E1.SEMESTER = T1.SEMESTER ");
            stb.append("             AND E1.TESTCD = T1.TESTKINDCD || T1.TESTITEMCD ");
            stb.append("             AND E1.CLASSCD = T1.CLASSCD ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("             AND E1.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("             AND E1.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append("             AND E1.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("             AND E1.DIV = T1.DIV ");
            stb.append("     ) ");
            stb.append("     AND T1.GRADE = CASE WHEN T1.DIV = '01' THEN '00' ELSE '" + grade + "' END ");
            stb.append("     AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE = CASE WHEN T1.DIV IN ('01','02') THEN '00000000' ELSE '" + course + "' END ");
        }
        return stb.toString();
    }

    /**
     * 欠課時分を欠課時数に換算した値を得る
     * @param kekka 欠課時分
     * @return 欠課時分を欠課時数に換算した値
     */
    private int getSpecialAttendExe(final int kekka) {
        final int jituJifun = (_knjSchoolMst._jituJifunSpecial == null) ? 50 : Integer.parseInt(_knjSchoolMst._jituJifunSpecial);
        final BigDecimal bigKekka = new BigDecimal(kekka);
        final BigDecimal bigJitu = new BigDecimal(jituJifun);
        BigDecimal bigD = bigKekka.divide(bigJitu, 1, BigDecimal.ROUND_DOWN);
        String retSt = bigD.toString();
        final int retIndex = retSt.indexOf(".");
        int seisu = 0;
        if (retIndex > 0) {
            seisu = Integer.parseInt(retSt.substring(0, retIndex));
            final int hasu = Integer.parseInt(retSt.substring(retIndex + 1, retIndex + 2));
            seisu = hasu < 5 ? seisu : seisu + 1;
        } else {
            seisu = Integer.parseInt(retSt);
        }
        return seisu;
    }

    private String setNameMst(final DB2UDB db2, final String namecd1, final String namecd2) throws SQLException {
        String rtnSt = "";
        db2.query(getNameMst( namecd1, namecd2));
        ResultSet rs = db2.getResultSet();
        try {
            while (rs.next()) {
                rtnSt = rs.getString("NAME1");
            }
        } finally {
            db2.commit();
            DbUtils.closeQuietly(rs);
        }
        return rtnSt;
    }

    private String getNameMst(final String namecd1, final String namecd2) {
        final String rtnSql = " SELECT "
                            + "     * "
                            + " FROM "
                            + "     V_NAME_MST "
                            + " WHERE "
                            + "     YEAR = '" + _param._year + "' "
                            + "     AND NAMECD1 = '" + namecd1 + "' "
                            + "     AND NAMECD2 = '" + namecd2 + "'";
        return rtnSql;
    }

    private List getStudentList(final DB2UDB db2) throws SQLException  {
        final List rtnStudent = new ArrayList();
        ResultSet rs = null;
        try {
            final String sql = getStudentInfoSql();
//            log.debug("getStudentInfoSql = " + sql);
            db2.query(sql);
            rs = db2.getResultSet();
            while (rs.next()) {
                Student student = new Student(rs.getString("SCHREGNO"),
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
                                              rs.getString("COURSECODENAME"));
                rtnStudent.add(student);
            }
        } finally {
            db2.commit();
            DbUtils.closeQuietly(rs);
        }
        return rtnStudent;
    }

    class Student {
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
        Map _testSubclass;
        TestScore _testAll;
        boolean _hasScore;

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
                final String courseCodeName
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
            _testSubclass = new TreeMap();
            _testAll = new TestScore("", SCORE);
        }

        public String toString() {
            return "学籍：" + _schregno + " 名前：" + _name;
        }
    }

    private String getStudentInfoSql() {
        final StringBuffer stb = new StringBuffer();
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
        stb.append("     L2.COURSECODENAME ");
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
        stb.append("  ");
        stb.append(" WHERE ");
        stb.append("     VSCH.YEAR = '" + _param._year + "' ");
        stb.append("     AND VSCH.SEMESTER = '" + _param._schregSemester + "' ");
        if (_param.isClass()) {
            stb.append("     AND VSCH.GRADE = '" + _param._grade + "' ");
            stb.append("     AND VSCH.HR_CLASS IN " + _param._selectInstate + " ");
        } else {
            stb.append("     AND VSCH.GRADE = '" + _param._grade + "' ");
            stb.append("     AND VSCH.HR_CLASS = '" + _param._hrClass + "' ");
            stb.append("     AND VSCH.SCHREGNO IN " + _param._selectInstate + " ");
        }
        stb.append(" ORDER BY ");
        stb.append("     VSCH.GRADE, ");
        stb.append("     VSCH.HR_CLASS, ");
        stb.append("     VSCH.ATTENDNO ");

        return stb.toString();
    }
 
    class TestScore {
        String _name;
        String _score;
        String _gradeAvg;
        String _gradeRank;
        String _gradeCnt;
        String _gradeHighScore;
        String _gradeLowScore;
        String _hrAvg;
        String _hrRank;
        String _hrCnt;
        String _hrHighScore;
        String _hrLowScore;
        String _courseAvg;
        String _courseRank;
        String _courseCnt;
        String _courseHighScore;
        String _courseLowScore;
        String _majorAvg;
        String _majorRank;
        String _majorCnt;
        String _majorHighScore;
        String _majorLowScore;
        String _chairGradeRank;
        String _chairHrRank;
        String _chairMajorRank;
        String _chairCourseRank;
        String _credit;
        String _absenceHigh;
        String _getAbsenceHigh;
        String _average;
        String _chaircd;
        String _slump;
        String _passScore;
        int _kekka;
        int _jisu;
        int _cnt;
        int _cnt2;

        public TestScore(String score, String scoreDiv) {
            setScore(score, scoreDiv);
        }

        private void setScore(final String score, final String scoreDiv) {
            if (scoreDiv.equals(SCORE)) {
                _score = score;
            } else if (scoreDiv.equals(AVG)) {
                _hrAvg = score;
            } else if (scoreDiv.equals(RANK)) {
                _hrRank = score;
            } else if (scoreDiv.equals(RANK2)) {
                _courseRank = score;
            } else if (scoreDiv.equals(CREDIT)) {
                _credit = score;
            } else if (scoreDiv.equals(KEKKA)) {
                _kekka = Integer.parseInt(score);
            }
        }

        private int getFailValue() {
            if (_param.isPerfectRecord() && null != _passScore) {
                return Integer.parseInt(_passScore);
            } else if (_param.isKetten() && null != _param._ketten && !"".equals(_param._ketten)) {
                return Integer.parseInt(_param._ketten);
            }
            return -1;
        }
        
        private boolean isKetten(int score) {
            if (_param.isRecordSlump()) {
                return "1".equals(_slump);
            } else if (_param.isPerfectRecord()) {
                return score < getFailValue();
            } else {
                return score <= getFailValue();
            }
        }

        public String toString() {
            return "科目：" + _name
                    + "得点：" + _score
                    + " 平均：" + _hrAvg
                    + " 席次：" + _hrRank;
        }
    }

    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        dumpParam(request, param);
        return param;
    }

    /** パラメータダンプ */
    private void dumpParam(final HttpServletRequest request, final Param param) {
        log.fatal("$Revision: 56595 $"); // CVSキーワードの取り扱いに注意
        if (log.isDebugEnabled()) {
            final Enumeration enums = request.getParameterNames();
            while (enums.hasMoreElements()) {
                final String name = (String) enums.nextElement();
                final String[] values = request.getParameterValues(name);
                log.debug("parameter:name=" + name + ", value=[" + StringUtils.join(values, ',') + "]");
            }
        }
    }

    private class Param {
        final String _year;
        final String _categoryIsClass;
        final String _kindItem;
        final String _scoreDiv;
        final String _testName;
        final String _semester;
        String _semesterName;
        final String _grade;
        final String _hrClass;
        final String _groupDiv;
        final String _outputKijun;
        final String[] _selectData;
        final String _selectInstate;
        String _z010 = "";
        String _z012 = "";
        final boolean _isSeireki;
        final String _ketten;
        final String _checkKettenDiv; //欠点プロパティ 1,2,設定無し(1,2以外)
        final String _countFlg;
        final String _scoreFlg;
        private String _tableDiv;
        final String _schregSemester;

        /** フォーム選択（最大科目数：１５or２０） */
        final boolean _printForm15;
        /** 試験科目のみ出力する */
        final boolean _printTestOnly;
        /** 注意 or 超過 */
        final boolean _useAbsenceWarn;
        /** 教育課程コード */
        final String _useCurriculumcd;

        Param(final DB2UDB db2, final HttpServletRequest request) throws Exception {
            _year = request.getParameter("YEAR");
            _categoryIsClass = request.getParameter("CATEGORY_IS_CLASS");
            _kindItem = request.getParameter("TESTCD");
            _scoreDiv = _kindItem.equals("9900") ? "00" : "01";
            _semester = request.getParameter("SEMESTER");
             String ctrlSemester = request.getParameter("CTRL_SEME");
            _schregSemester = "9".equals(_semester) ? ctrlSemester : _semester;
            _testName = getTestName(db2);
            setSemesterName(db2);
            _grade = request.getParameter("GRADE");
            _hrClass = request.getParameter("HR_CLASS");
            _groupDiv = request.getParameter("GROUP_DIV");
            _outputKijun = request.getParameter("OUTPUT_KIJUN");
            _selectData = request.getParameterValues("CATEGORY_SELECTED");  //学籍番号または学年-組
            _selectInstate = getInstate(_selectData);

            _z010 = setNameMst(db2, "Z010", "00");
            _z012 = setNameMst(db2, "Z012", "00");
            _isSeireki = _z012.equals("2") ? true : false;

            _ketten = request.getParameter("KETTEN");
            _checkKettenDiv = request.getParameter("checkKettenDiv");
            _countFlg = request.getParameter("COUNT_SURU");
            _scoreFlg = request.getParameter("SCORE_FLG");
            setTableDiv();

            String formDiv = request.getParameter("SUBCLASS_MAX");
            _printForm15 = "1".equals(formDiv);
            String testOnly = request.getParameter("TEST_ONLY");
            _printTestOnly = null != testOnly;

            _useAbsenceWarn = "1".equals(request.getParameter("TYUI_TYOUKA"));
            _useCurriculumcd = request.getParameter("useCurriculumcd");
        }
        
        /**
         * 席次テーブル
         * @return 1:DAT, 2:V_DAT 
         */
        public void setTableDiv() {
            if (isTottori() && (("9900".equals(_kindItem) && "1".equals(_scoreFlg)) || (!"9900".equals(_kindItem) && "2".equals(_scoreFlg)))) {
                _tableDiv = "2";
            } else {
                _tableDiv = "1";
            }
            log.fatal("序列テーブル：" + ("2".equals(_tableDiv) ? "V_DAT" : "DAT"));
        }

        private boolean isCountKetsu() {
            return null != _countFlg;
        }

        private String getTestName(final DB2UDB db2) throws SQLException {
            String rtn = "";
            ResultSet rs = null;
            try {
                final String sql = "SELECT TESTITEMNAME FROM TESTITEM_MST_COUNTFLG_NEW WHERE YEAR = '" + _year + "'" +
                        " AND SEMESTER = '" + _semester + "' " +
                        " AND TESTKINDCD || TESTITEMCD = '" + _kindItem + "' ";
                db2.query(sql);
                rs = db2.getResultSet();
                while (rs.next()) {
                    rtn = rs.getString("TESTITEMNAME");
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(rs);
            }
            return rtn;
        }

        private void setSemesterName(final DB2UDB db2) throws SQLException {
            ResultSet rs = null;
            try {
                final String sql = "SELECT SEMESTER, SEMESTERNAME, SDATE, EDATE FROM SEMESTER_MST WHERE YEAR = '" + _year + "' ORDER BY SEMESTER ";
                db2.query(sql);
                rs = db2.getResultSet();
                while (rs.next()) {
                    String seme = rs.getString("SEMESTER");
                    if (_semester.equals(seme)) {
                        _semesterName = rs.getString("SEMESTERNAME");
                    }
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(rs);
            }
        }

        /**
         * @param selectData
         * @return
         */
        private String getInstate(final String[] selectData) {
            String sep = "";
            final StringBuffer stb = new StringBuffer();
            stb.append("('");
            for (int i = 0; i < selectData.length; i++) {
                stb.append(sep + selectData[i]);
                sep = "','";
            }
            stb.append("')");

            return stb.toString();
        }

        private String setNameMst(final DB2UDB db2, final String namecd1, final String namecd2) throws SQLException {
            String rtnSt = "";
            db2.query(getNameMst(_year, namecd1, namecd2));
            ResultSet rs = db2.getResultSet();
            try {
                while (rs.next()) {
                    rtnSt = rs.getString("NAME1");
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(rs);
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

        private boolean isClass() {
            return _categoryIsClass.equals("1");
        }

        private String getRankAvgField() {
            return "2".equals(_outputKijun) ? "AVG_" : "";
        }

        private String getRankName() {
            if ("1".equals(_groupDiv) || "3".equals(_groupDiv)) {
                return "学年";
            } else if ("2".equals(_groupDiv) || "4".equals(_groupDiv)) {
                return "学級";
            }
            return null;
        }

        private String getRankName2() {
            if ("1".equals(_groupDiv) || "2".equals(_groupDiv)) {
                return "コース";
            } else if ("3".equals(_groupDiv) || "4".equals(_groupDiv)) {
                return "講座";
            }
            return null;
        }

        private String getRankName3() {
            if ("1".equals(_groupDiv) || "2".equals(_groupDiv)) {
                return "学科";
            } else if ("3".equals(_groupDiv) || "4".equals(_groupDiv)) {
                return "講座";
            }
            return null;
        }

        private String getAvgDiv() {
            if ("1".equals(_groupDiv) || "3".equals(_groupDiv)) {
                return AVG_GRADE;
            } else if ("2".equals(_groupDiv) || "4".equals(_groupDiv)) {
                return AVG_COURSE;
            }
            return null;
        }

        private boolean isPrintChair() {
            return "3".equals(_groupDiv) || "4".equals(_groupDiv);
        }

        private String changePrintYear() {
            if (_isSeireki) {
                return _param._year + "年度";
            } else {
                return nao_package.KenjaProperties.gengou(Integer.parseInt(_param._year)) + "年度";
            }
        }

        /** RECORD_SLUMP_DATを参照するテストコード */
        private String getRecordSlumpTestcd() {
            if (isKyoto()) {
                if ( "1".equals(_semester) && "9901".equals(_kindItem)) return "0201";
                if ( "2".equals(_semester) && "9901".equals(_kindItem)) return "0102";
                if (!"9".equals(_semester) && "9900".equals(_kindItem)) return "0201";
            }
            return _kindItem;
        }
        
        /** 欠点対象：RECORD_SLUMP_DATを参照して判断するか */
        private boolean isRecordSlump() {
            return "1".equals(_checkKettenDiv) && !"9".equals(_semester);
        }

        /** 欠点対象：満点マスタ(PERFECT_RECORD_DAT)の合格点(PASS_SCORE)を参照して判断するか */
        private boolean isPerfectRecord() {
            return "2".equals(_checkKettenDiv);
        }

        /** 欠点対象：指示画面の欠点を参照して判断するか */
        private boolean isKetten() {
            return !isRecordSlump() && !isPerfectRecord();
        }
        
        private boolean isKyoto() {
            return "kyoto".equals(_z010);
        }
        
        private boolean isTottori() {
            return "tottori".equals(_z010);
        }
    }
}

// eof
