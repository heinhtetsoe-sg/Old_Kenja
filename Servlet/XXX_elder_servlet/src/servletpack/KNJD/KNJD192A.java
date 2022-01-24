// kanji=漢字
/*
 * $Id: 21fe0e108b74e18d8e76e701453a5d884b4c4c1e $
 *
 * 作成日: 2009/07/22 17:54:00 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2009 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
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
import servletpack.KNJZ.detail.dao.AttendAccumulate;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: 21fe0e108b74e18d8e76e701453a5d884b4c4c1e $
 */
public class KNJD192A {

    private static final Log log = LogFactory.getLog("KNJD192A.class");

    private Param _param;

    private static final String SPECIAL_ALL = "999";

    private static final String ATTEND_OBJ_SEM = "9";

    private static final String SUBCLASS3   = "333333";
    private static final String SUBCLASS5   = "555555";
    private static final String SUBCLASSALL = "999999";
    private KNJSchoolMst _knjSchoolMst;

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

            _knjSchoolMst = new KNJSchoolMst(db2, _param._year);
            _param.load(db2);

            //SVF出力
            hasData = printMain(response, db2, svf);
        } catch (Exception e) {
            log.fatal("exception!", e);
        } finally {
            sd.closeSvf(svf, hasData);
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

            if (hasData && !befGradeClass.equals(student._grade + student._hrClass)) {
                cnt = pageChange(svf);
            }

            cnt++;
            setPrintOut(svf, student, cnt);

            if (cnt == 4) {
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

    /**
     *  文字数を取得
     */
    private static int getMS932ByteLength(final String str) {
        int ret = 0;
        if (null != str) {
            try {
                ret = str.getBytes("MS932").length;                      //byte数を取得
            } catch (Exception ex) {
                log.error("retStringByteValue error!", ex);
            }
        }
        return ret;
    }

    private void setPrintOut(final Vrw32alp svf, final Student student, final int fieldNo) {
        log.debug(student);
        svf.VrsOutn("NENDO", fieldNo, _param.changePrintYear());
        svf.VrsOutn("SEMESTER", fieldNo, _param._semesterName);
        svf.VrsOutn("TESTNAME", fieldNo, _param._testName);
        final String rankName = _param.getRankName();
        svf.VrsOutn("AVG_NAME", fieldNo, rankName);
        svf.VrsOutn("RANK_NAME", fieldNo, rankName);
        final String rankName2 = _param.getRankName2();
        svf.VrsOutn("CLS_AVG_NAME", fieldNo, rankName2);
        svf.VrsOutn("CLS_RANK_NAME", fieldNo, rankName2);
        final String hrName = student._hrName + "(" + student._attendNo + ")";
        svf.VrsOutn("HR_NAME", fieldNo, hrName);
        svf.VrsOutn("NAME", fieldNo, student._name);
        //注釈
        if (_param._isRuikei) {
            svf.VrsOutn("MARK1", fieldNo, "　");
            svf.VrsOutn("MARK2", fieldNo, "　");
            String comment = _param._useAbsenceWarn ? "注意" : "超過";
            svf.VrsOutn("NOTE1", fieldNo, "　：未履修" + comment + ",特活進級" + comment);
            svf.VrsOutn("NOTE2", fieldNo, "　：未修得" + comment);
            svf.VrAttributen("MARK1", fieldNo, "Paint=(1,40,1),Bold=1");
            svf.VrAttributen("MARK2", fieldNo, "Paint=(1,70,1),Bold=1");
        }

        int fieldCnt = 1;
        int kettenSubclassCount = 0;
        for (final Iterator itSubclass = student._testSubclass.keySet().iterator(); itSubclass.hasNext();) {
            final String subclassCd = (String) itSubclass.next();
            final TestScore testScore = (TestScore) student._testSubclass.get(subclassCd);
            final String credit = null != testScore._credit ? "(" + testScore._credit + ")" : "";
            final String subName = StringUtils.defaultString(testScore._name);
            if (getMS932ByteLength(subName + credit) > 9) {
                svf.VrsOutn("SUBCLASS" + fieldCnt + "_2", fieldNo, subName + credit);
            } else {
                svf.VrsOutn("SUBCLASS" + fieldCnt, fieldNo, subName + credit);
            }
            if (student.isPrintSubclassScoreAvg(subclassCd)) {
                if (_param._d065Name1List.contains(subclassCd)) {
                    svf.VrsOutn("SCORE" + fieldCnt, fieldNo, (String) _param._d001Abbv1Map.get(testScore._score));
                } else {
                    svf.VrsOutn("SCORE" + fieldCnt, fieldNo, testScore._score);
                    if (!"欠".equals(testScore._score) && !"".equals(testScore._score) && null != testScore._score) {
                        int score = Integer.parseInt(testScore._score);
                        if (testScore.isKetten(score)) {
                            svf.VrAttributen("SCORE" + fieldCnt, fieldNo, "Paint=(1,70,1),Bold=1");
                            kettenSubclassCount += 1;
                        }
                    } else if ("欠".equals(testScore._score) && _param.isCountKetsu()) {
                        kettenSubclassCount += 1;
                    }
                }
            }
            
            svf.VrsOutn("ABSENT" + fieldCnt, fieldNo, String.valueOf(testScore._kekka));
            if (_param._isRuikei) {
                if (isKekkaOver(String.valueOf(testScore._kekka), testScore._absenceHigh)) {
                    svf.VrAttributen("ABSENT" + fieldCnt, fieldNo, "Paint=(1,40,1),Bold=1");
                } else if (isKekkaOver(String.valueOf(testScore._kekka), testScore._getAbsenceHigh)) {
                    svf.VrAttributen("ABSENT" + fieldCnt, fieldNo, "Paint=(1,70,1),Bold=1");
                }
            }
            svf.VrsOutn("LESSON" + fieldCnt, fieldNo, String.valueOf(testScore._jisu));
            if (student.isPrintSubclassScoreAvg(subclassCd)) {
                if (!_param._d065Name1List.contains(subclassCd)) {
                    svf.VrsOutn("AVERAGE" + fieldCnt, fieldNo, testScore._avg);
                }
                svf.VrsOutn("RANK" + fieldCnt, fieldNo, testScore._rank);
                if (!_param._d065Name1List.contains(subclassCd)) {
                    svf.VrsOutn("CLASS_AVERAGE" + fieldCnt, fieldNo, (_param.isPrintChair()) ? testScore._avg3 : testScore._avg2);
                }
                svf.VrsOutn("CLASS_RANK" + fieldCnt, fieldNo, (_param.isPrintChair()) ? testScore._rank3 : testScore._rank2);
            }
            fieldCnt++;
//            log.debug(testScore);
        }

        if (0 < student._specialLesson) {
            svf.VrsOutn("SUBCLASS" + fieldCnt, fieldNo, "特別活動");
            svf.VrsOutn("ABSENT" + fieldCnt, fieldNo, String.valueOf(student._specialAbsent));
            svf.VrsOutn("LESSON" + fieldCnt, fieldNo, String.valueOf(student._specialLesson));
            if (_param._isRuikei) {
                if (isKekkaOver(String.valueOf(student._specialAbsent), student._spAbsenceHigh)) {
                    svf.VrAttributen("ABSENT" + fieldCnt, fieldNo, "Paint=(1,40,1),Bold=1");
                }
            }
        }

        svf.VrsOutn("TOTAL_AVERAGE", fieldNo, student._testAll._avg);
        if (!_param.isPrintChair()) {
            svf.VrsOutn("CLASS_AVERAGE", fieldNo, student._testAll._avg2);
        }

        if (student._hasScore) {
            svf.VrsOutn("TOTAL_SCORE", fieldNo, student._testAll._score);
            svf.VrsOutn("TOTAL_RANK", fieldNo, student._testAll._rank);
            if (!_param.isPrintChair()) {
                svf.VrsOutn("CLASS_RANK", fieldNo, student._testAll._rank2);
            }
            svf.VrsOutn("FAIL", fieldNo, String.valueOf(kettenSubclassCount));
//            log.debug("欠点科目数 = " + student._kettenSubclassCount);
//            svf.VrsOutn("AVERAGEL_SCORE", fieldNo, String.valueOf(student._averageScore));
//            log.debug("個人平均点 = " + student._averageScore);
            svf.VrsOutn("AVERAGEL_SCORE", fieldNo, student._testAll._average);
//            log.debug("個人平均点 = " + student._testAll._average);
            svf.VrsOutn("TOTAL_STUDENT", fieldNo, String.valueOf(student._testAll._cnt));
            if (!_param.isPrintChair()) {
                svf.VrsOutn("CLASS_STUDENT", fieldNo, String.valueOf(student._testAll._cnt2));
            }
        }
        //出欠情報
        svf.VrsOutn("MLESSON", fieldNo, student._mlesson);
        svf.VrsOutn("SICK", fieldNo, student._sick);
        svf.VrsOutn("LATE", fieldNo, student._late);
        svf.VrsOutn("EARLY", fieldNo, student._early);
//        log.debug("mlesson=" + student._mlesson + ", sick=" + student._sick + ", lateEarly=" + student._lateEarly);
    }

    private void setHead(final Vrw32alp svf) {
        final String form;
        if ("1".equals(_param._printOnedayAttend)) {
            form = _param._printForm15 ? "KNJD192A.frm" : "KNJD192A_2.frm";
        } else {
            form = _param._printForm15 ? "KNJD192A_3.frm" : "KNJD192A_4.frm";
        }
        svf.VrSetForm(form, 1);
    }

    private List getPrintData(final DB2UDB db2, final List studentList) throws SQLException, ParseException {
        List printDataList = new ArrayList();
        for (final Iterator iter = studentList.iterator(); iter.hasNext();) {
            final Student student = (Student) iter.next();

            setMapTestSubclassAvg(db2, student);
            if (_param.isCheckNoExamChair()) {
                setMapRecordChkfinSubclass(db2, student);
            }
            setChairStd(db2, student);
            setRank(db2, student);
            setRankChair(db2, student);
            setAvg(db2, student);
            setAvgChair(db2, student);
            if (!_param.isKetten()) {
                setKetten(db2, student);
            }
            setAbsenceHighSpecial(db2, student);
            printDataList.add(student);
        }
        setJisu(db2, studentList);
        setAttendSemes(db2, studentList);
        setKekka(db2, studentList);
        return printDataList;
    }
    
    private boolean isSubclassAll(final String subclassCd) {
        return SUBCLASS3.equals(subclassCd) || SUBCLASS5.equals(subclassCd) || SUBCLASSALL.equals(subclassCd);
    }

    private void setMapRecordChkfinSubclass(final DB2UDB db2, final Student student) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        // 考査の入力完了フラグが未完了の場合、考査は実施しないとみなす
        try {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH CHR_STD AS (");
            stb.append("   SELECT DISTINCT ");
            stb.append("       T1.YEAR, T1.SEMESTER, T1.CHAIRCD, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("       T2.CLASSCD, ");
                stb.append("       T2.SCHOOL_KIND, ");
                stb.append("       T2.CURRICULUM_CD, ");
            }
            stb.append("       T2.SUBCLASSCD ");
            stb.append("   FROM CHAIR_STD_DAT T1 ");
            stb.append("   INNER JOIN CHAIR_DAT T2 ON T2.YEAR = T1.YEAR ");
            stb.append("       AND T2.SEMESTER = T1.SEMESTER ");
            stb.append("       AND T2.CHAIRCD = T1.CHAIRCD ");
            stb.append("   WHERE ");
            stb.append("       T1.YEAR = '" + _param._year + "' ");
            stb.append("       AND T1.SEMESTER = '" + _param._semester + "' ");
            stb.append("       AND T1.SCHREGNO = '" + student._schregno + "' ");
            stb.append(" )");
            stb.append(" SELECT DISTINCT ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("     T1.CLASSCD, ");
                stb.append("     T1.SCHOOL_KIND, ");
                stb.append("     T1.CURRICULUM_CD, ");
            }
            stb.append("     T1.SUBCLASSCD, ");
            stb.append("     T2.EXECUTED ");
            stb.append(" FROM CHR_STD T1 ");
            stb.append(" INNER JOIN RECORD_CHKFIN_DAT T2 ON T2.YEAR = T1.YEAR ");
            stb.append("     AND T2.SEMESTER = T1.SEMESTER ");
            stb.append("     AND T2.CHAIRCD = T1.CHAIRCD ");
            stb.append("     AND T2.TESTKINDCD || T2.TESTITEMCD = '" + _param._kindItem + "' ");
            stb.append("     AND T2.RECORD_DIV = '1' ");
            
            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();
            while (rs.next()) {
                final String subclassCd;
                if ("1".equals(_param._useCurriculumcd) && !isSubclassAll(rs.getString("SUBCLASSCD"))) {
                    subclassCd = rs.getString("CLASSCD") + "-" + rs.getString("SCHOOL_KIND") + "-" + rs.getString("CURRICULUM_CD") + "-" + rs.getString("SUBCLASSCD");
                } else {
                    subclassCd = rs.getString("SUBCLASSCD");
                }
                final String executed = rs.getString("EXECUTED");
                student._recordChkfinSubclass.put(subclassCd, executed);
            }
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }

    private void setMapTestSubclassAvg(final DB2UDB db2, final Student student) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rankRs = null;
        // 学級の平均
        try {
            final String course = student._courseCd + student._majorCd + student._courseCode;
            final String rankSql = getReportCardInfoTottori.getRecordAverageTestAppointSql(_param._year, _param._semester, student._schregno, null, null, "2", student._grade, student._hrClass, course, _param._kindItem, _param._tableDiv);
            ps = db2.prepareStatement(rankSql);
            rankRs = ps.executeQuery();
            while (rankRs.next()) {
                final String subclassCd;
                if ("1".equals(_param._useCurriculumcd) && !isSubclassAll(rankRs.getString("SUBCLASSCD"))) {
                    subclassCd = rankRs.getString("CLASSCD") + "-" + rankRs.getString("SCHOOL_KIND") + "-" + rankRs.getString("CURRICULUM_CD") + "-" + rankRs.getString("SUBCLASSCD");
                } else {
                    subclassCd = rankRs.getString("SUBCLASSCD");
                }
                final String avg = rankRs.getString("AVG");
                student._testSubclassAvg.put(subclassCd, avg);
            }
        } finally {
            DbUtils.closeQuietly(null, ps, rankRs);
            db2.commit();
        }
    }

    private void setChairStd(final DB2UDB db2, final Student student) throws SQLException {
        ResultSet scoreRs = null;
        PreparedStatement psScore = null;
        try {
            String scoreSql = "";
            scoreSql = getChairStdSql(student);
            psScore = db2.prepareStatement(scoreSql);
            scoreRs = psScore.executeQuery();
            while (scoreRs.next()) {
                final String subclassCd;
                if ("1".equals(_param._useCurriculumcd) && !isSubclassAll(scoreRs.getString("SUBCLASSCD"))) {
                    subclassCd = scoreRs.getString("CLASSCD") + "-" + scoreRs.getString("SCHOOL_KIND") + "-" + scoreRs.getString("CURRICULUM_CD") + "-" + scoreRs.getString("SUBCLASSCD");
                } else {
                    subclassCd = scoreRs.getString("SUBCLASSCD");
                }
                String strKetu = "";
                if (student._testSubclassAvg.containsKey(subclassCd)) {
                    strKetu = "欠";
                }
                if (!_param._printTestOnly || _param._printTestOnly && student._testSubclassAvg.containsKey(subclassCd)) {
                    final TestScore testScore = new TestScore();
                    testScore._score = strKetu;
                    testScore._name = scoreRs.getString("SUBCLASSABBV");
                    student._testSubclass.put(subclassCd, testScore);
                }
            }
            DbUtils.closeQuietly(null, psScore, scoreRs);
            PreparedStatement psCredit = null;
            for (Iterator it = student._testSubclass.keySet().iterator(); it.hasNext();) {
                final String subclassCd = (String) it.next();
                TestScore testScore = (TestScore) student._testSubclass.get(subclassCd);
                //単位
                final String creditSql = getCredit(student, subclassCd.substring(0, 2), subclassCd);
                psCredit = db2.prepareStatement(creditSql);
                ResultSet creditRs = psCredit.executeQuery();
                while (creditRs.next()) {
                    testScore._credit = creditRs.getString("CREDITS");
                    if (_knjSchoolMst.isHoutei()) {
                        testScore._absenceHigh = creditRs.getString("ABSENCE_HIGH");
                        testScore._getAbsenceHigh = creditRs.getString("GET_ABSENCE_HIGH");
                    }
                }
                DbUtils.closeQuietly(null, psCredit, creditRs);
            }
            
            if (_knjSchoolMst.isJitu()) {
                PreparedStatement psAbsenceHigh = null;
                for (Iterator it = student._testSubclass.keySet().iterator(); it.hasNext();) {
                    final String subclassCd = (String) it.next();
                    TestScore testScore = (TestScore) student._testSubclass.get(subclassCd);
                    //実・欠課数上限値
                    final String absenceHighSql = getAbsenceHighSql(student, subclassCd);
                    psAbsenceHigh = db2.prepareStatement(absenceHighSql);
                    ResultSet absenceHighRs = psAbsenceHigh.executeQuery();
                    while (absenceHighRs.next()) {
                        testScore._absenceHigh = absenceHighRs.getString("COMP_ABSENCE_HIGH");
                        testScore._getAbsenceHigh = absenceHighRs.getString("GET_ABSENCE_HIGH");
                    }
                    DbUtils.closeQuietly(null, psAbsenceHigh, absenceHighRs);
                }
            }
            
            PreparedStatement psChaircd = null;
            final String chaircdSql = getChaircdSql(student);
            psChaircd = db2.prepareStatement(chaircdSql);
            for (Iterator it = student._testSubclass.keySet().iterator(); it.hasNext();) {
                final String subclassCd = (String) it.next();
                TestScore testScore = (TestScore) student._testSubclass.get(subclassCd);
                //講座
                psChaircd.setString(1, subclassCd);
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
            stb.append("     AND T1.CLASSCD = L1.CLASSCD ");
            stb.append("     AND T1.SCHOOL_KIND = L1.SCHOOL_KIND ");
            stb.append("     AND T1.CURRICULUM_CD = L1.CURRICULUM_CD ");
        }
        stb.append("     , CHAIR_STD_DAT T2 ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' AND ");
        stb.append("     T1.SEMESTER = '" + _param._schregSemester + "' AND ");
        stb.append("     substr(T1.SUBCLASSCD,1,2) < '90' AND ");
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

    private String getCredit(final Student student, final String classCd, final String subclassCd) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.CREDITS, ");
        stb.append("     VALUE(T1.ABSENCE_HIGH, 0) ");
        if (_param._useAbsenceWarn) {
            stb.append("    - VALUE(T1.ABSENCE_WARN_RISHU_SEM" + _param._warnSemester + ", 0) ");
        }
        stb.append("     AS ABSENCE_HIGH, ");
        stb.append("     VALUE(T1.GET_ABSENCE_HIGH, 0) ");
        if (_param._useAbsenceWarn) {
            stb.append("    - VALUE(T1.ABSENCE_WARN_SHUTOKU_SEM" + _param._warnSemester + ", 0) ");
        }
        stb.append("     AS GET_ABSENCE_HIGH ");
        stb.append(" FROM ");
        stb.append("     V_CREDIT_MST T1 ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.COURSECD = '" + student._courseCd + "' ");
        stb.append("     AND T1.MAJORCD = '" + student._majorCd + "' ");
        stb.append("     AND T1.GRADE = '" + student._grade + "' ");
        stb.append("     AND T1.COURSECODE = '" + student._courseCode + "' ");
        stb.append("     AND T1.CLASSCD = '" + classCd + "' ");
        stb.append("     AND ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
        }
        stb.append("         T1.SUBCLASSCD = '" + subclassCd + "' ");

        return stb.toString();
    }

    private String getAbsenceHighSql(final Student student, final String subclassCd) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     COMP_ABSENCE_HIGH, GET_ABSENCE_HIGH ");
        stb.append(" FROM ");
        stb.append("     SCHREG_ABSENCE_HIGH_DAT ");
        stb.append(" WHERE ");
        stb.append("     YEAR = '" + _param._year + "' AND ");
        stb.append("     DIV = '2' AND "); // 1:年間、2:随時
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || ");
        }
        stb.append("     SUBCLASSCD = '" + subclassCd + "' AND ");
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
            stb.append("     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
        }
        stb.append("     T1.SUBCLASSCD = ? AND ");
        stb.append("     T2.YEAR = T1.YEAR AND ");
        stb.append("     T2.SEMESTER = T1.SEMESTER AND ");
        stb.append("     T2.CHAIRCD = T1.CHAIRCD AND ");
        stb.append("     T2.SCHREGNO = '" + student._schregno + "' ");

        return stb.toString();
    }

    private void setRank(final DB2UDB db2, final Student student) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rankRs = null;
        try {
            final String rankSql = getReportCardInfoTottori.getRecordRankTestAppointSql(_param._year, _param._semester, student._schregno, null, null, _param._kindItem, _param._tableDiv);
//            log.debug(rankSql);
            ps = db2.prepareStatement(rankSql);
            rankRs = ps.executeQuery();
            int subclassCount = 0;
            boolean hasScore = false;
            double totalScore = 0;
            while (rankRs.next()) {
                final String subclassCd;
                if ("1".equals(_param._useCurriculumcd) && !isSubclassAll(rankRs.getString("SUBCLASSCD"))) {
                    subclassCd = rankRs.getString("CLASSCD") + "-" + rankRs.getString("SCHOOL_KIND") + "-" + rankRs.getString("CURRICULUM_CD") + "-" + rankRs.getString("SUBCLASSCD");
                } else {
                    subclassCd = rankRs.getString("SUBCLASSCD");
                }
                if (student._testSubclass.containsKey(subclassCd)) {
                    hasScore = true;
                    TestScore testScore = (TestScore) student._testSubclass.get(subclassCd);
                    testScore._score = rankRs.getString("SCORE");
                    testScore._rank = rankRs.getString(_param.getRankField() + _param.getRankAvgField() + "RANK");
                    testScore._rank2 = rankRs.getString("CLASS_" + _param.getRankAvgField() + "RANK");

                    if (!"欠".equals(testScore._score)) {
                        int score = Integer.parseInt(testScore._score);
                        totalScore += score;
                    }
                    subclassCount += 1;
                }
                if (subclassCd.equals(SUBCLASSALL)) {
                    student._testAll._score = rankRs.getString("SCORE");
                    student._testAll._rank = rankRs.getString(_param.getRankField() + _param.getRankAvgField() + "RANK");
                    student._testAll._rank2 = rankRs.getString("CLASS_" + _param.getRankAvgField() + "RANK");
                    student._testAll._average = new BigDecimal(rankRs.getDouble("AVG")).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                }
            }
            if (hasScore) {
            	student._hasScore = hasScore;
            }
            if (hasScore) {
                student._averageScore = new BigDecimal(totalScore / subclassCount).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
            }
            
        } finally {
            DbUtils.closeQuietly(null, ps, rankRs);
            db2.commit();
        }
    }

    private void setRankChair(final DB2UDB db2, final Student student) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rankRs = null;
        try {
            final String rankSql = getReportCardInfoTottori.getRecordRankChairTestAppointSql(_param._year, _param._semester, student._schregno, null, null, _param._kindItem, _param._tableDiv);
//            log.debug(rankSql);
            ps = db2.prepareStatement(rankSql);
            rankRs = ps.executeQuery();
            boolean hasScore = false;
            while (rankRs.next()) {
                final String subclassCd;
                if ("1".equals(_param._useCurriculumcd) && !isSubclassAll(rankRs.getString("SUBCLASSCD"))) {
                    subclassCd = rankRs.getString("CLASSCD") + "-" + rankRs.getString("SCHOOL_KIND") + "-" + rankRs.getString("CURRICULUM_CD") + "-" + rankRs.getString("SUBCLASSCD");
                } else {
                    subclassCd = rankRs.getString("SUBCLASSCD");
                }
                final String chairCd = rankRs.getString("CHAIRCD");
                if (student._testSubclass.containsKey(subclassCd)) {
                    TestScore testScore = (TestScore) student._testSubclass.get(subclassCd);

                    if (chairCd.equals(testScore._chaircd)) {
                        hasScore = true;
                        testScore._rank3 = rankRs.getString(_param.getRankField() + _param.getRankAvgField() + "RANK");
                    }
                }
            }
            if (hasScore) {
            	student._hasScore = hasScore;
            }
            
        } finally {
            DbUtils.closeQuietly(null, ps, rankRs);
            db2.commit();
        }
    }

    private void setAvg(final DB2UDB db2, final Student student) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rankRs = null;
        // 学年/コースの平均
        try {
            final String course = student._courseCd + student._majorCd + student._courseCode;
            final String rankSql = getReportCardInfoTottori.getRecordAverageTestAppointSql(_param._year, _param._semester, student._schregno, null, null, _param.getAvgDiv(), student._grade, student._hrClass, course, _param._kindItem, _param._tableDiv);
            ps = db2.prepareStatement(rankSql);
            rankRs = ps.executeQuery();
            int totalScore = 0;
            int totalCount = 0;
            while (rankRs.next()) {
                final String subclassCd;
                if ("1".equals(_param._useCurriculumcd) && !isSubclassAll(rankRs.getString("SUBCLASSCD"))) {
                    subclassCd = rankRs.getString("CLASSCD") + "-" + rankRs.getString("SCHOOL_KIND") + "-" + rankRs.getString("CURRICULUM_CD") + "-" + rankRs.getString("SUBCLASSCD");
                } else {
                    subclassCd = rankRs.getString("SUBCLASSCD");
                }
                if (student._testSubclass.containsKey(subclassCd)) {
                    TestScore testScore = (TestScore) student._testSubclass.get(subclassCd);
                    if (rankRs.getString("AVG") != null) {
                        BigDecimal avg = new BigDecimal(rankRs.getDouble("AVG"));
                        testScore._avg = avg.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                    }
                }
                if (subclassCd.equals(SUBCLASSALL)) {
                    student._testAll._cnt = rankRs.getInt("COUNT");
                }
                if (!subclassCd.equals(SUBCLASSALL) && !subclassCd.equals(SUBCLASS5) && !subclassCd.equals(SUBCLASS3)) {
                    totalScore += rankRs.getInt("SCORE");
                    totalCount += rankRs.getInt("COUNT");
                }
            }
            if (totalCount > 0) {
                BigDecimal avg = new BigDecimal((double)totalScore / totalCount);
                student._testAll._avg = avg.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
            }
        } finally {
            DbUtils.closeQuietly(null, ps, rankRs);
            db2.commit();
        }

        // 学級の平均
        try {
            final String rankSql = getReportCardInfoTottori.getRecordAverageTestAppointSql(_param._year, _param._semester, student._schregno, null, null, "2", student._grade, student._hrClass, null, _param._kindItem, _param._tableDiv);
            ps = db2.prepareStatement(rankSql);
            rankRs = ps.executeQuery();
            int totalScore = 0;
            int totalCount = 0;
            while (rankRs.next()) {
                final String subclassCd;
                if ("1".equals(_param._useCurriculumcd) && !isSubclassAll(rankRs.getString("SUBCLASSCD"))) {
                    subclassCd = rankRs.getString("CLASSCD") + "-" + rankRs.getString("SCHOOL_KIND") + "-" + rankRs.getString("CURRICULUM_CD") + "-" + rankRs.getString("SUBCLASSCD");
                } else {
                    subclassCd = rankRs.getString("SUBCLASSCD");
                }
                if (student._testSubclass.containsKey(subclassCd)) {
                    TestScore testScore = (TestScore) student._testSubclass.get(subclassCd);
                    if (rankRs.getString("AVG") != null) {
                        BigDecimal avg = new BigDecimal(rankRs.getDouble("AVG"));
                        testScore._avg2 = avg.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                    }
                }
                if (subclassCd.equals(SUBCLASSALL)) {
                    student._testAll._cnt2 = rankRs.getInt("COUNT");
                }
                if (!subclassCd.equals(SUBCLASSALL) && !subclassCd.equals(SUBCLASS5) && !subclassCd.equals(SUBCLASS3)) {
                    totalScore += rankRs.getInt("SCORE");
                    totalCount += rankRs.getInt("COUNT");
                }
            }
            if (totalCount > 0) {
                BigDecimal avg = new BigDecimal((double)totalScore / totalCount);
                student._testAll._avg2 = avg.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
            }
        } finally {
            DbUtils.closeQuietly(null, ps, rankRs);
            db2.commit();
        }
    }

    private void setAvgChair(final DB2UDB db2, final Student student) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rankRs = null;
        // 講座の平均
        try {
            final String rankSql = getReportCardInfoTottori.getRecordAverageChairTestAppointSql(_param._year, _param._semester, student._schregno, null, null, "1", student._grade, student._hrClass, null, _param._kindItem, _param._tableDiv);
            ps = db2.prepareStatement(rankSql);
            rankRs = ps.executeQuery();
            while (rankRs.next()) {
                final String subclassCd;
                if ("1".equals(_param._useCurriculumcd) && !isSubclassAll(rankRs.getString("SUBCLASSCD"))) {
                    subclassCd = rankRs.getString("CLASSCD") + "-" + rankRs.getString("SCHOOL_KIND") + "-" + rankRs.getString("CURRICULUM_CD") + "-" + rankRs.getString("SUBCLASSCD");
                } else {
                    subclassCd = rankRs.getString("SUBCLASSCD");
                }
                final String chairCd = rankRs.getString("CHAIRCD");
                if (student._testSubclass.containsKey(subclassCd)) {
                    TestScore testScore = (TestScore) student._testSubclass.get(subclassCd);

                    if (chairCd.equals(testScore._chaircd)) {
                        if (rankRs.getString("AVG") != null) {
                            BigDecimal avg = new BigDecimal(rankRs.getDouble("AVG"));
                            testScore._avg3 = avg.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                        }
                    }
                }
            }
        } finally {
            DbUtils.closeQuietly(null, ps, rankRs);
            db2.commit();
        }
    }

    private void setKetten(final DB2UDB db2, final Student student) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        // 欠点対象
        try {
            final String course = student._courseCd + student._majorCd + student._courseCode;
            final String sql = getKettenSql(student._schregno, student._grade, course);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String subclassCd;
                if ("1".equals(_param._useCurriculumcd) && !isSubclassAll(rs.getString("SUBCLASSCD"))) {
                    subclassCd = rs.getString("CLASSCD") + "-" + rs.getString("SCHOOL_KIND") + "-" + rs.getString("CURRICULUM_CD") + "-" + rs.getString("SUBCLASSCD");
                } else {
                    subclassCd = rs.getString("SUBCLASSCD");
                }
                if (student._testSubclass.containsKey(subclassCd)) {
                    TestScore testScore = (TestScore) student._testSubclass.get(subclassCd);
                    testScore._slump = rs.getString("SLUMP");
                    testScore._passScore = rs.getString("PASS_SCORE");
                }
            }
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
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
            stb.append("     TESTKINDCD || TESTITEMCD = '" + _param._kindItem + "' AND ");
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
                stb.append("     T1.CLASSCD, ");
                stb.append("     T1.SCHOOL_KIND, ");
                stb.append("     T1.CURRICULUM_CD, ");
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

    private void setKekka(final DB2UDB db2, final List studentList) throws SQLException, ParseException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            _param._attendParamMap.put("grade", _param._grade);
            _param._attendParamMap.put("schregno", "?");
            final String attendSql = AttendAccumulate.getAttendSubclassAbsenceSql(
                                                    _param._year,
                                                    _param._semester,
                                                    _param._dateS,
                                                    _param._date,
                                                    _param._attendParamMap);
            
            ps = db2.prepareStatement(attendSql);
            
            for (final Iterator sit = studentList.iterator(); sit.hasNext();) {
                
                final Student student = (Student) sit.next();
                
                ps.setString(1, student._schregno);

                rs = ps.executeQuery();
                while (rs.next()) {
                    final String subclassCd = rs.getString("SUBCLASSCD");
                    if (student._testSubclass.containsKey(subclassCd)) {
                        TestScore testScore = (TestScore) student._testSubclass.get(subclassCd);
                        if (ATTEND_OBJ_SEM.equals(rs.getString("SEMESTER"))) {
                            testScore._kekka = rs.getInt("ABSENT_SEM");
                        }
                    }
                    if (null != _param._attendSubclassSpecialMinutes && _param._attendSubclassSpecialMinutes.containsKey(subclassCd)) {
                        if (ATTEND_OBJ_SEM.equals(rs.getString("SEMESTER"))) {
                            String specialGroupCd = (String) _param._attendSubclassSpecialGroupCd.get(subclassCd);
                            Integer minutes = (Integer) _param._attendSubclassSpecialMinutes.get(subclassCd);
                            int minutes1 = rs.getInt("ABSENT_SEM") * minutes.intValue();

                            if (!student._spGroupAbsentMinutes.containsKey(specialGroupCd)) {
                                student._spGroupAbsentMinutes.put(specialGroupCd, new Integer(0));
                            }
                            int minutes2 = ((Integer) student._spGroupAbsentMinutes.get(specialGroupCd)).intValue();
                            student._spGroupAbsentMinutes.put(specialGroupCd, new Integer(minutes1 + minutes2));
                        }
                    }
                }

                int specialAbsent = 0;
                for (final Iterator it = student._spGroupAbsentMinutes.values().iterator(); it.hasNext();) {
                    final Integer groupAbsentMinutes = (Integer) it.next();
                    specialAbsent += getSpecialAttendExe(groupAbsentMinutes.intValue());
                }
                student._specialAbsent = specialAbsent;
                
                DbUtils.closeQuietly(rs);
            }
            
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
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

    private void setJisu(final DB2UDB db2, final List studentList) throws SQLException, ParseException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            _param._attendParamMap.put("schregno", "?");
            final String attendSql = AttendAccumulate.getAttendSubclassSql(
                                                    _param._year,
                                                    _param._semester,
                                                    _param._dateS,
                                                    _param._date,
                                                    _param._attendParamMap
                    );
//            log.debug("授業時数のＳＱＬ＝"+attendSql);
            ps = db2.prepareStatement(attendSql);

            for (final Iterator it = studentList.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                
                ps.setString(1, student._schregno);
                rs = ps.executeQuery();
                
                while (rs.next()) {
                    final String subclassCd = rs.getString("SUBCLASSCD");
                    if (null != subclassCd && student._testSubclass.containsKey(subclassCd)) {
                        TestScore testScore = (TestScore) student._testSubclass.get(subclassCd);
                        if (ATTEND_OBJ_SEM.equals(rs.getString("SEMESTER"))) {
                            testScore._jisu = rs.getInt("MLESSON");
                        }
                    }
                    if (null != _param._attendSubclassSpecialMinutes && _param._attendSubclassSpecialMinutes.containsKey(subclassCd)) {
                        if (ATTEND_OBJ_SEM.equals(rs.getString("SEMESTER"))) {
                            String specialGroupCd = (String) _param._attendSubclassSpecialGroupCd.get(subclassCd);
                            Integer minutes = (Integer) _param._attendSubclassSpecialMinutes.get(subclassCd);
                            int minutes1 = rs.getInt("MLESSON") * minutes.intValue();
                            
                            if (!student._spGroupLessonMinutes.containsKey(specialGroupCd)) {
                                student._spGroupLessonMinutes.put(specialGroupCd, new Integer(0));
                            }
                            int minutes2 = ((Integer) student._spGroupLessonMinutes.get(specialGroupCd)).intValue();
                            student._spGroupLessonMinutes.put(specialGroupCd, new Integer(minutes1 + minutes2));
                        }
                    }
                }
                
                int specialLesson = 0;
                for (final Iterator itsp = student._spGroupLessonMinutes.values().iterator(); itsp.hasNext();) {
                    final Integer groupLessonMinutes = (Integer) itsp.next();
                    specialLesson += getSpecialAttendExe(groupLessonMinutes.intValue());
                }
                student._specialLesson = specialLesson;
            }
            DbUtils.closeQuietly(rs);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
    }

    private void setAbsenceHighSpecial(final DB2UDB db2, final Student student) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            String sql = "";
            if (_knjSchoolMst.isHoutei()) {
                sql = getCreditSpecialSql(student);
            } else {
                sql = getAbsenceHighSpecialSql(student);
            }
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                student._spAbsenceHigh = rs.getString("ABSENCE_HIGH");
            }
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
    }

    private String getCreditSpecialSql(final Student student) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     VALUE(T1.ABSENCE_HIGH, 0) ");
        if (_param._useAbsenceWarn) {
            stb.append("      - VALUE(T1.ABSENCE_WARN_RISHU_SEM" + _param._warnSemester + ", 0) ");
        }
        stb.append("       AS ABSENCE_HIGH, ");
        stb.append("     VALUE(T1.GET_ABSENCE_HIGH, 0) ");
        if (_param._useAbsenceWarn) {
            stb.append("      - VALUE(T1.ABSENCE_WARN_SHUTOKU_SEM" + _param._warnSemester + ", 0) ");
        }
        stb.append("       AS GET_ABSENCE_HIGH ");
        stb.append(" FROM ");
        stb.append("     V_CREDIT_SPECIAL_MST T1 ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.COURSECD = '" + student._courseCd + "' ");
        stb.append("     AND T1.MAJORCD = '" + student._majorCd + "' ");
        stb.append("     AND T1.GRADE = '" + student._grade + "' ");
        stb.append("     AND T1.COURSECODE = '" + student._courseCode + "' ");
        stb.append("     AND T1.SPECIAL_GROUP_CD = '" + SPECIAL_ALL + "' ");
        return stb.toString();
    }

    private String getAbsenceHighSpecialSql(final Student student) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     COMP_ABSENCE_HIGH as ABSENCE_HIGH, GET_ABSENCE_HIGH ");
        stb.append(" FROM ");
        stb.append("     SCHREG_ABSENCE_HIGH_SPECIAL_DAT ");
        stb.append(" WHERE ");
        stb.append("     YEAR = '" + _param._year + "' AND ");
        stb.append("     DIV = '2' AND "); // 1:年間、2:随時
        stb.append("     SPECIAL_GROUP_CD = '" + SPECIAL_ALL + "' AND ");
        stb.append("     SCHREGNO = '" + student._schregno + "' ");
        return stb.toString();
    }

    private void setAttendSemes(final DB2UDB db2, final List studentList) throws SQLException, ParseException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            _param._attendParamMap.put("groupByDiv", "SCHREGNO");
            _param._attendParamMap.put("schregno", "?");
            final String attendSql = AttendAccumulate.getAttendSemesSql(
                                                    _param._year,
                                                    _param._semester,
                                                    _param._dateS,
                                                    _param._date,
                                                    _param._attendParamMap);
            ps = db2.prepareStatement(attendSql);
            
            for (final Iterator it = studentList.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                
                ps.setString(1, student._schregno);
                rs = ps.executeQuery();
                
                while (rs.next()) {
                    student._mlesson = rs.getString("MLESSON");
                    student._sick = rs.getString("SICK");
                    student._late = String.valueOf(rs.getInt("LATE"));
                    student._early = String.valueOf(rs.getInt("EARLY"));
                }
                DbUtils.closeQuietly(rs);
            }
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
    }

    private String setNameMst(final DB2UDB db2, final String namecd1, final String namecd2) throws SQLException {
        String rtnSt = "";
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(getNameMst(namecd1, namecd2));
            rs = ps.executeQuery();
            while (rs.next()) {
                rtnSt = rs.getString("NAME1");
            }
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
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
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getStudentInfoSql();
//            log.debug("getStudentInfoSql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
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
            DbUtils.closeQuietly(null, ps, rs);
        }
        return rtnStudent;
    }

    
    /**
     * 欠課数オーバーか
     * @param kekka 欠課数 
     * @param absenceHigh 欠課数上限値（履修） 
     * @return true or false
     */
    private boolean isKekkaOver(String kekka, String absenceHigh) {
        if (null == kekka || Double.parseDouble(kekka) == 0) return false;
        if (null == absenceHigh) return true;
        return Double.parseDouble(kekka) > Double.parseDouble(absenceHigh);
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
        final Map _testSubclass;
        final Map _testSubclassAvg;
        final Map _recordChkfinSubclass;
        TestScore _testAll;
        boolean _hasScore;
        /** 個人の平均点 */
        String _averageScore;
        /** 出欠情報 */
        String _mlesson;
        String _sick;
        String _late;
        String _early;
        /** 特活情報 */
        private Map _spGroupAbsentMinutes = new HashMap(); // 特活グループコード毎の欠課分数
        private int _specialAbsent; // 特活欠課時数
        private Map _spGroupLessonMinutes = new HashMap(); // 特活グループコード毎の授業分数
        private int _specialLesson; // 特活授業時数
        private String _spAbsenceHigh; // 特活履修上限値

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
            _testSubclassAvg = new TreeMap();
            _recordChkfinSubclass = new TreeMap();
            _testAll = new TestScore();
            _testAll._score = "";
        }
        
        /** 指定科目コードの平均点を表示するか */
        public boolean isPrintSubclassScoreAvg(final String subclassCd) {
            if (_param.isCheckNoExamChair()) {
                if (!_recordChkfinSubclass.containsKey(subclassCd)) {
                    return true; // 設定無しは表示する
                }
                return "1".equals(_recordChkfinSubclass.get(subclassCd));  // 成績入力が完了していれば表示する
            }
            return true;
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
        String _avg;
        String _avg2;
        String _avg3;
        String _rank;
        String _rank2;
        String _rank3;
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
                    + " 平均：" + _avg
                    + " 席次：" + _rank;
        }
    }

    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        dumpParam(request, param);
        return param;
    }

    /** パラメータダンプ */
    private void dumpParam(final HttpServletRequest request, final Param param) {
        log.fatal("$Revision: 59216 $"); // CVSキーワードの取り扱いに注意
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
        final boolean _isRuikei;

        /** 出欠集計日付 */
        final String _dateS;
        final String _date;
        /** フォーム選択（最大科目数：１５or２０） */
        final boolean _printForm15;
        /** 試験科目のみ出力する */
        final boolean _printTestOnly;
        /** 注意 or 超過 */
        final boolean _useAbsenceWarn;
        /** 一日出席欄を出力する */
        final String _printOnedayAttend;
        /** 教育課程コードを使用するか */
        final String _useCurriculumcd;
        /** 注意週数学期 */
        String _warnSemester;
        /** 「考査を実施しない講座は平均点を表示しない」を処理するか */
        final String _knjd192AcheckNoExamChair;

        /** 特別活動設定データのマップ */
        private Map _attendSubclassSpecialMinutes = new HashMap();
        private Map _attendSubclassSpecialGroupCd = new HashMap();
        final Map _attendParamMap;
        final List _d065Name1List;
        final Map _d001Abbv1Map;

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
            _z012 = setNameMst(db2, "Z012", "01");
            _isSeireki = _z012.equals("2") ? true : false;

            _isRuikei = "1".equals(request.getParameter("DATE_DIV"));
            _ketten = request.getParameter("KETTEN");
            _checkKettenDiv = request.getParameter("checkKettenDiv");
            _countFlg = request.getParameter("COUNT_SURU");
            _scoreFlg = request.getParameter("SCORE_FLG");
            _dateS = request.getParameter("SDATE").replace('/', '-');
            _date = request.getParameter("DATE").replace('/', '-');
            setTableDiv();
            _knjd192AcheckNoExamChair = request.getParameter("knjd192AcheckNoExamChair");

            String formDiv = request.getParameter("SUBCLASS_MAX");
            _printForm15 = "1".equals(formDiv);
            String testOnly = request.getParameter("TEST_ONLY");
            _printTestOnly = null != testOnly;

            _useAbsenceWarn = "1".equals(request.getParameter("TYUI_TYOUKA"));
            _printOnedayAttend = request.getParameter("ONEDAY_ATTEND");
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            setAttendSubclassSpecialMap(db2);
            
            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
            
            _d065Name1List = getD065Name1List(db2);
            _d001Abbv1Map = getD001Abbv1Map(db2);
        }
        
        public void load(DB2UDB db2) {
            setWarnSemester(db2);
        }
        
        private void setWarnSemester(DB2UDB db2) {
            if ("9".equals(_semester)) {
                _warnSemester = _knjSchoolMst._semesterDiv;
            } else {
                final StringBuffer stb = new StringBuffer(); 
                stb.append(" SELECT T1.YEAR, T1.SEMESTER, T1.SDATE, T1.EDATE, T2.SEMESTER AS NEXT_SEMESTER, T2.SDATE AS NEXT_SDATE ");
                stb.append(" FROM V_SEMESTER_GRADE_MST T1 ");
                stb.append(" LEFT JOIN V_SEMESTER_GRADE_MST T2 ON T2.YEAR = T1.YEAR ");
                stb.append("     AND T2.GRADE = T1.GRADE ");
                stb.append("     AND INT(T2.SEMESTER) = INT(T1.SEMESTER) + 1 ");
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
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = "SELECT TESTITEMNAME FROM TESTITEM_MST_COUNTFLG_NEW WHERE YEAR = '" + _year + "'" +
                        " AND SEMESTER = '" + _semester + "' " +
                        " AND TESTKINDCD || TESTITEMCD = '" + _kindItem + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString("TESTITEMNAME");
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtn;
        }

        private void setSemesterName(final DB2UDB db2) throws SQLException {
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
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
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
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(getNameMst(_year, namecd1, namecd2));
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtnSt = rs.getString("NAME1");
                }
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

        private boolean isClass() {
            return _categoryIsClass.equals("1");
        }

        private String getRankField() {
            if ("1".equals(_groupDiv) || "3".equals(_groupDiv)) {
                return "GRADE_";
            } else if ("2".equals(_groupDiv) || "4".equals(_groupDiv)) {
                return "COURSE_";
            }
            return null;
        }

        private String getRankAvgField() {
            return "2".equals(_outputKijun) ? "AVG_" : "";
        }

        private String getRankName() {
            if ("1".equals(_groupDiv) || "3".equals(_groupDiv)) {
                return "学年";
            } else if ("2".equals(_groupDiv) || "4".equals(_groupDiv)) {
                return "コース";
            }
            return null;
        }

        private String getRankName2() {
            if ("1".equals(_groupDiv) || "2".equals(_groupDiv)) {
                return "学級";
            } else if ("3".equals(_groupDiv) || "4".equals(_groupDiv)) {
                return "講座";
            }
            return null;
        }

        private String getAvgDiv() {
            if ("1".equals(_groupDiv) || "3".equals(_groupDiv)) {
                return "1";
            } else if ("2".equals(_groupDiv) || "4".equals(_groupDiv)) {
                return "3";
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

        private void setAttendSubclassSpecialMap(final DB2UDB db2) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                String sql = "SELECT SPECIAL_GROUP_CD,";
                if ("1".equals(_useCurriculumcd)) {
                    sql += " CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || ";
                }
                sql += " SUBCLASSCD as SUBCLASSCD, smallint(MINUTES) as MINUTES FROM ATTEND_SUBCLASS_SPECIAL_DAT WHERE YEAR = '" + _year + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    String specialGroupCd = rs.getString("SPECIAL_GROUP_CD");
                    String subclasscd = rs.getString("SUBCLASSCD");
                    Integer minutes = (Integer) rs.getObject("MINUTES");
                    if (subclasscd != null && minutes != null) {
                        _attendSubclassSpecialMinutes.put(subclasscd, minutes);
                        _attendSubclassSpecialGroupCd.put(subclasscd, specialGroupCd);
                    }
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        /** 欠点対象：RECORD_SLUMP_DATを参照して判断するか */
        private boolean isRecordSlump() {
            return "1".equals(_checkKettenDiv) && (null == _ketten || "".equals(_ketten));
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
        
        /** 考査を実施しないかをチェックするか */
        private boolean isCheckNoExamChair() {
            return isKyoto() && "1".equals(_knjd192AcheckNoExamChair) && !"9".equals(_semester) && !_kindItem.startsWith("99");
        }
        
        private List getD065Name1List(final DB2UDB db2) {
            final List list = new ArrayList();
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT NAME1 FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'D065' AND NAME1 IS NOT NULL ");
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    list.add(rs.getString("NAME1"));
                }
            } catch (Exception ex) {
                log.error("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }
        
        private Map getD001Abbv1Map(final DB2UDB db2) {
            final Map list = new HashMap();
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT NAMECD2, ABBV1 FROM NAME_MST WHERE NAMECD1 = 'D001' AND ABBV1 IS NOT NULL ");
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    list.put(rs.getString("NAMECD2"), rs.getString("ABBV1"));
                }
            } catch (Exception ex) {
                log.error("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }
    }
}

// eof
