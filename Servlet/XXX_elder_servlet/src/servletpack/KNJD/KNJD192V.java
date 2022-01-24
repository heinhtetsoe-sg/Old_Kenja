// kanji=漢字
/*
 * $Id: b963d02ba11e91e2453a91f45f90bc734f5d720d $
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
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
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
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJServletpacksvfANDdb2;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: b963d02ba11e91e2453a91f45f90bc734f5d720d $
 */
public class KNJD192V {

    private static final Log log = LogFactory.getLog("KNJD192V.class");

    private Param _param;

    private static final String SPECIAL_ALL = "999";

    private static final String SEMEALL = "9";

    private static final String SUBCLASS3 = "333333";
    private static final String SUBCLASS5 = "555555";
    private static final String SUBCLASS9 = "999999";

    private static final String SIDOU_INPUT_INF_MARK = "1";
    private static final String SIDOU_INPUT_INF_SCORE = "2";

    private static final String AMIKAKE_ATTR1 = "Paint=(1,60,1),Bold=1";
    private static final String AMIKAKE_ATTR2 = "Paint=(1,80,1),Bold=1";
    private static final String AMIKAKE_KETTEN = "Paint=(12,85,1),Bold=1"; // 青
    private static final String HYOTEI_TESTCD = "9990009";

    public void svf_out(HttpServletRequest request, HttpServletResponse response) throws Exception {
        final KNJServletpacksvfANDdb2 sd = new KNJServletpacksvfANDdb2();  //帳票におけるＳＶＦおよびＤＢ２の設定
        DB2UDB db2 = null; //Databaseクラスを継承したクラス
        Vrw32alp svf = new Vrw32alp();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        boolean hasData = false;
        try {

            log.fatal("$Revision: 71405 $ $Date: 2019-12-24 15:47:54 +0900 (火, 24 12 2019) $"); // CVSキーワードの取り扱いに注意

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
        final List<List<Student>> rtn = new ArrayList<List<Student>>();
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

    private static List<List<Student>> getCuttingPageList(final List<Student> studentList, final int maxLine) {
        //アルゴリズムとして、先にクラス毎に何ページ出力するかを確認しつつ生徒データ格納用オブジェクトをページ数分用意して、
        //クラス内で何番目の生徒なのかで、出力NO % 作成ページ数 で何番目のページかを確定する。
        final List<List<Student>> rtn = new ArrayList<List<Student>>();
        List<Student> current = null;
        String befGradeClass = "";
        final ArrayList classcntlist = new ArrayList();
        StudentCnt objCnt = null;
        for (final Iterator iter = studentList.iterator(); iter.hasNext();) {
            final Student student = (Student) iter.next();
            if (!befGradeClass.equals(student._grade + student._hrClass)) {
                if (null != objCnt) {
                    setPageCnt(objCnt, maxLine);
                    for (int pcnt = 0;pcnt < objCnt._pagecnt;pcnt++) {
                        current = new ArrayList();
                        rtn.add(current);
                    }
                }
                objCnt = new StudentCnt();
                   objCnt._cnt = 0;
                classcntlist.add(objCnt);
            }
            objCnt._cnt++;
            befGradeClass = student._grade + student._hrClass;
            if (!iter.hasNext()) {
                setPageCnt(objCnt, maxLine);
                for (int pcnt = 0;pcnt < objCnt._pagecnt;pcnt++) {
                    current = new ArrayList();
                    rtn.add(current);
                }
            }
        }

        befGradeClass = "";
        int reccnt = 0;
        int totalPageCnt = 0;
        int classCnt = 0;
        objCnt = null;
        current = null;
        for (final Student student : studentList) {
            if (null == objCnt || !befGradeClass.equals(student._grade + student._hrClass)) {
                if (null != objCnt) {
                    totalPageCnt += objCnt._pagecnt;
                }
                objCnt = (StudentCnt)classcntlist.get(classCnt);
                classCnt++;
                reccnt = 0;
            }
            reccnt++;
            current = (ArrayList)rtn.get(totalPageCnt + getPageNo(objCnt, reccnt));
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

    private static String getSubclassCd(final ResultSet rs, final Param param) throws SQLException {
        final String subclassCd;
        if (!isSubclassAll(rs.getString("SUBCLASSCD"))) {
            subclassCd = rs.getString("CLASSCD") + "-" + rs.getString("SCHOOL_KIND") + "-" + rs.getString("CURRICULUM_CD") + "-" + rs.getString("SUBCLASSCD");
        } else {
            subclassCd = rs.getString("SUBCLASSCD");
        }
        return subclassCd;
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

    private static String roundHalfUp(final BigDecimal bd) {
        return null == bd ? null : bd.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
    }

    /**
     * 欠課数オーバーか
     * @param kekka 欠課数
     * @param absenceHigh 欠課数上限値（履修）
     * @return true or false
     */
    private static boolean isKekkaOver(final String kekka, final String absenceHigh) {
        if (null == kekka || Double.parseDouble(kekka) == 0) return false;
        if (null == absenceHigh) return false;
        return Double.parseDouble(kekka) > Double.parseDouble(absenceHigh);
    }

    private List<Student> getStudentList(final DB2UDB db2) throws SQLException  {
        final List<Student> studentList = Student.getStudentList(db2, _param);

        Student.setMapTestSubclass(db2, _param, studentList);
        TestScore.setChairStd(db2, _param, studentList);
        TestScore.setCreditMst(db2, _param, studentList);
        if (!_param.isHoutei()) {
            TestScore.setAbsenceHigh2(db2, _param, studentList);
        }

        TestScore.setChaircd(db2, _param, studentList);
        TestScore.setRank(db2, _param, studentList);
        TestScore.setRankChair(db2, _param, studentList);
        TestScore.setAvg(db2, _param, studentList);
        TestScore.setAvgChair(db2, _param, studentList);
        if (!_param.isKetten()) {
            TestScore.setKetten(db2, _param, studentList);
        }
        TestScore.setRankAvgAverage(db2, _param, studentList);
        Attendance.setAbsenceHighSpecial(db2, _param, studentList);
        Attendance.setAttendSubclass(db2, _param, studentList);
        Attendance.setAttendSemes(db2, _param, studentList);

        return studentList;
    }

    private static Map getMappedMap(final Map map, final Object key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new HashMap());
        }
        return (Map) map.get(key1);
    }

    /**
     * @param db2
     */
    private boolean printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException, ParseException {
        boolean hasData = false;
        final List<Student> studentListAll = getStudentList(db2);
        log.debug(" studentList size = " + studentListAll.size());

        final String form;
        if ("1".equals(_param._printOnedayAttend)) {
            form = _param._printForm20 ? "KNJD192V_2.frm" : "KNJD192V.frm";
        } else {
            form = null;
        }

        final int maxLine = 4;
        final List pageList = _param._sortCutting ? getCuttingPageList(studentListAll, maxLine) : getPageList(studentListAll, maxLine);
        for (final Iterator it = pageList.iterator(); it.hasNext();) {
            final List<Student> studentList = (List) it.next();

            svf.VrSetForm(form, 1);

            if ("1".equals(_param._notPrintKekka)) {
                for (int gyo = 1; gyo <= maxLine; gyo++) {
                    for (int i = 0; i < 20; i++) {
                        svf.VrAttributen("SLASH" + String.valueOf(i), gyo, "X=10000");
                    }
                }
            }
            for (int gyo = 1; gyo <=  studentList.size(); gyo++) {
                final Student student = studentList.get(gyo - 1);

                setPrintOut(svf, student, gyo);

                hasData = true;
            }
            svf.VrEndPage();
        }
        return hasData;
    }

    private void setPrintOut(final Vrw32alp svf, final Student student, final int gyo) {
        svf.VrsOutn("NENDO", gyo, _param._nendo);
        svf.VrsOutn("SEMESTER", gyo, _param._semesterName);
        svf.VrsOutn("TESTNAME", gyo, _param._testName);
        if (!"1".equals(_param._notPrintAvg) && _param._useTotalAverage) {
            svf.VrsOutn("AVERAGE_TITLE1", gyo, "平均");
        }
        if ("5".equals(_param._groupDiv) && student._majorHas1Hr) {
            // 表示しない
        } else {
            if (!"1".equals(_param._notPrintAvg)) {
                svf.VrsOutn("AVG_NAME", gyo, _param.getRankName());
            }
            if (!"1".equals(_param._notPrintAvg) && _param._useTotalAverage) {
                svf.VrsOutn("AVG_NAME2_1", gyo, _param.getRankName());
            }
            if (!"1".equals(_param._notPrintRank)) {
                svf.VrsOutn("RANK_NAME", gyo, _param.getRankName());
            }
        }
        if (!"1".equals(_param._notPrintKekka)) {
            svf.VrsOutn("KEKKA_TITLE", gyo, "欠課数／授業時数");
        }
        if (_param._useKetten && !_param._notUseKetten) {
            svf.VrsOutn("KETTEN_TITLE1", gyo, "欠点");
            svf.VrsOutn("KETTEN_TITLE2", gyo, "科目数");
        }
        if (!"1".equals(_param._notPrintAvg)) {
            svf.VrsOutn("CLS_AVG_NAME", gyo, _param.getRankName2());
        }
        if (!"1".equals(_param._notPrintAvg) && _param._useTotalAverage) {
            svf.VrsOutn("CLS_AVG_NAME2_1", gyo, _param.getRankName2());
        }
        if (!"1".equals(_param._notPrintRank)) {
            svf.VrsOutn("CLS_RANK_NAME", gyo, _param.getRankName2());
        }
        if (_param._isOutputDebug) {
        	log.info(" schregno = " + student._schregno);
        }
        svf.VrsOutn("HR_NAME", gyo, student._hrName + "(" + student._attendNo + ")");
        if ("1".equals(_param._use_SchregNo_hyoji)) {
            svf.VrsOutn("NAME2", gyo, student._name);
            svf.VrsOutn("SCHREGNO", gyo, student._schregno);
        } else  {
            svf.VrsOutn("NAME", gyo, student._name);
        }
        final List<String[]> remarkAttrs = new ArrayList<String[]>(3);
        if (!"1".equals(_param._notPrintKekka)) {
            //注釈
            if (_param._isRuikei) {
                final String comment = _param._useAbsenceWarn ? "注意" : "超過";
                remarkAttrs.add(new String[] {AMIKAKE_ATTR1, "　：未履修" + comment + ("1".equals(_param._notUseAttendSubclassSpecial) ? "" : ",特活進級" + comment)});
                remarkAttrs.add(new String[] {AMIKAKE_ATTR2, "　：未修得" + comment});
            }
        }
        remarkAttrs.add(new String[] {AMIKAKE_KETTEN, "　：欠点"});
        for (int i = 0; i < remarkAttrs.size(); i++) {
        	final String[] remarkAttr = remarkAttrs.get(i);
        	final String n = String.valueOf(i + 1);
            svf.VrsOutn("MARK" + n, gyo, "　");
            svf.VrAttributen("MARK" + n, gyo, remarkAttr[0]);
            svf.VrsOutn("NOTE" + n, gyo, remarkAttr[1]);
        }

        final List<TestScore> printScoreList = new ArrayList(student._testSubclass.values());
        Collections.sort(printScoreList);
        int j = 1;
        int kettenSubclassCount = 0;
        for (final TestScore testScore : printScoreList) {
            if (_param._isOutputDebug) {
            	log.info(" subclass = " + testScore._subclasscd + " " + testScore._name + " (electDiv = " + testScore._electdiv + ")");
            }
            final String credit = null != testScore._credit ? "(" + testScore._credit + ")" : "";
            svf.VrsOutn("SUBCLASS" + j, gyo, StringUtils.defaultString(testScore._name) + credit);
            if (student.isPrintSubclassScoreAvg(testScore._subclasscd, _param)) {
                svf.VrsOutn("SCORE" + j, gyo, testScore._score);
                if (NumberUtils.isDigits(testScore._score)) {
                    if (_param._useKetten && !_param._notUseKetten) {
                        if (testScore.isKetten(_param)) {
                            svf.VrAttributen("SCORE" + j, gyo, AMIKAKE_KETTEN);
                            kettenSubclassCount += 1;
                        }
                    }
                } else if ("欠".equals(testScore._score) && _param.isCountKetsu()) {
                    kettenSubclassCount += 1;
                }
            }

            if (!"1".equals(_param._notPrintKekka)) {
                svf.VrsOutn("ABSENT" + j + getKekkaField(), gyo, getKekkaString(testScore._kekka));
                if (_param._isRuikei) {
                    if (isKekkaOver(getKekkaString(testScore._kekka), testScore._absenceHigh)) {
                        svf.VrAttributen("ABSENT" + j + getKekkaField(), gyo, AMIKAKE_ATTR1);
                    } else if (isKekkaOver(getKekkaString(testScore._kekka), testScore._getAbsenceHigh)) {
                        svf.VrAttributen("ABSENT" + j + getKekkaField(), gyo, AMIKAKE_ATTR2);
                    }
                }
                svf.VrsOutn("LESSON" + j, gyo, String.valueOf(testScore._jisu));
            }
            if (student.isPrintSubclassScoreAvg(testScore._subclasscd, _param)) {
                if ("5".equals(_param._groupDiv) && student._majorHas1Hr) {
                    // 表示しない
                } else {
                    if (!"1".equals(_param._notPrintAvg)) {
                        svf.VrsOutn("AVERAGE" + j, gyo, testScore._avg);
                    }
                    if (!"1".equals(_param._notPrintRank)) {
                        svf.VrsOutn("RANK" + j, gyo, testScore._rank);
                    }
                }
                if (!"1".equals(_param._notPrintAvg)) {
                    svf.VrsOutn("CLASS_AVERAGE" + j, gyo, _param.isPrintChair() ? testScore._avgChair : testScore._avgHr);
                }
                if (!"1".equals(_param._notPrintRank)) {
                    svf.VrsOutn("CLASS_RANK" + j, gyo, _param.isPrintChair() ? testScore._rankChair : testScore._rankHr);
                }
            }
            j++;
        }

        if (!"1".equals(_param._notPrintKekka)) {
            if (0 < student._attendance._specialLesson && !"1".equals(_param._notUseAttendSubclassSpecial)) {
                svf.VrsOutn("SUBCLASS" + j, gyo, "特別活動");
                svf.VrsOutn("ABSENT" + j, gyo, String.valueOf(student._attendance._specialAbsent));
                svf.VrsOutn("LESSON" + j, gyo, String.valueOf(student._attendance._specialLesson));
                if (_param._isRuikei) {
                    if (isKekkaOver(String.valueOf(student._attendance._specialAbsent), student._attendance._spAbsenceHigh)) {
                        svf.VrAttributen("ABSENT" + j, gyo, AMIKAKE_ATTR1);
                    }
                }
            }
        }
        if (!"1".equals(_param._notPrintAvg) && _param._useTotalAverage) {
        	if (_param.isPrintChair()) {
        		// 合計点に講座順位はない
        	} else {
                svf.VrsOutn("CLASS_AVERAGE", gyo, student._testAll._avgHr);
            }
            if ("5".equals(_param._groupDiv) && student._majorHas1Hr) {
                // 表示しない
            } else {
                svf.VrsOutn("TOTAL_AVERAGE", gyo, student._testAll._avg);
            }
        }

        if (student._hasScore) {
            svf.VrsOutn("TOTAL_SCORE", gyo, student._testAll._score);
            if ("1".equals(_param._notPrintRank)) {
            	// 順位を表示しない
            } else {
            	if (_param.isPrintChair()) {
            		// 合計点に講座順位はない
            	} else {
                    svf.VrsOutn("CLASS_RANK", gyo, student._testAll._rankHr);
                }
                if ("5".equals(_param._groupDiv) && student._majorHas1Hr) {
                    // 表示しない
                } else {
                    svf.VrsOutn("TOTAL_RANK", gyo, student._testAll._rank);
                }
            }
            if (_param._useKetten && !_param._notUseKetten) {
                svf.VrsOutn("FAIL", gyo, String.valueOf(kettenSubclassCount));
            }
            if (!"1".equals(_param._notPrintAvg) && _param._useTotalAverage) {
                svf.VrsOutn("AVERAGEL_SCORE", gyo, student._testAll._average);
            }
            if ("1".equals(_param._notPrintRank)) {
            	// 順位を表示しない
            } else {
            	if (_param.isPrintChair()) {
            		// 合計点に講座順位はない
            	} else {
                    svf.VrsOutn("CLASS_STUDENT", gyo, String.valueOf(student._testAll._cntHr));
                }
                if ("5".equals(_param._groupDiv) && student._majorHas1Hr) {
                    // 表示しない
                } else {
                    svf.VrsOutn("TOTAL_STUDENT", gyo, String.valueOf(student._testAll._cnt));
                }
            }
        }
        //出欠情報
        svf.VrsOutn("MLESSON", gyo, student._attendance._mlesson);
        svf.VrsOutn("SICK", gyo, student._attendance._sick);
        svf.VrsOutn("LATE", gyo, student._attendance._late);
        svf.VrsOutn("EARLY", gyo, student._attendance._early);
    }

    private String getKekkaField() {
        return _param.isPrintKekkaFloat() ? "_2" : "";
    }

    private String getKekkaString(final Double absent) {
        if (_param.isPrintKekkaFloat()) {
            return null == absent ? "0.0" : _param.getAbsentFmt().format(absent.floatValue());
        } else {
            return null == absent ? "0" : String.valueOf(absent.intValue());
        }
    }

    private static class Attendance {
        private String _mlesson;
        private String _sick;
        private String _late;
        private String _early;

        /** 特活情報 */
        private Map _spGroupAbsentMinutes = new HashMap(); // 特活グループコード毎の欠課分数
        private int _specialAbsent; // 特活欠課時数
        private Map _spGroupLessonMinutes = new HashMap(); // 特活グループコード毎の授業分数
        private int _specialLesson; // 特活授業時数
        private String _spAbsenceHigh; // 特活履修上限値

        static void setAttendSemes(final DB2UDB db2, final Param _param, final List<Student> studentList) {

            final Map<String, List<Student>> hrStudentListMap = new HashMap<String, List<Student>>();
            for (final Student student : studentList) {
                if (null == hrStudentListMap.get(_param._grade + student._hrClass)) {
                    hrStudentListMap.put(_param._grade + student._hrClass, new ArrayList<Student>());
                }
                hrStudentListMap.get(_param._grade + student._hrClass).add(student);
            }

            PreparedStatement ps = null;
            try {
                _param._attendParamMap.put("grade", _param._grade);
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

                    ps.setString(2, gradeHrclass.substring(2));

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

        private static void setAbsenceHighSpecial(final DB2UDB db2, final Param param, final List<Student> studentList) throws SQLException {
            PreparedStatement ps = null;
            try {
                if (param.isHoutei()) {
                    final String sql = getCreditSpecialSql(param);
                    ps = db2.prepareStatement(sql);

                    for (final Iterator it = studentList.iterator(); it.hasNext();) {
                        final Student student = (Student) it.next();
                        ps.setString(1, student._courseCd);
                        ps.setString(2, student._majorCd);
                        ps.setString(3, student._courseCode);

                        ResultSet rs = ps.executeQuery();
                        while (rs.next()) {
                            student._attendance._spAbsenceHigh = rs.getString("ABSENCE_HIGH");
                        }
                        DbUtils.closeQuietly(rs);
                    }

                } else {
                    final String sql = getAbsenceHighSpecialSql(param);
                    ps = db2.prepareStatement(sql);

                    for (final Iterator it = studentList.iterator(); it.hasNext();) {
                        final Student student = (Student) it.next();
                        ps.setString(1, student._schregno);

                        ResultSet rs = ps.executeQuery();
                        while (rs.next()) {
                            student._attendance._spAbsenceHigh = rs.getString("ABSENCE_HIGH");
                        }
                        DbUtils.closeQuietly(rs);
                    }
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(ps);
            }
        }

        private static String getCreditSpecialSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     VALUE(T1.ABSENCE_HIGH, 0) ");
            if (param._useAbsenceWarn) {
                stb.append("      - VALUE(T1.ABSENCE_WARN_RISHU_SEM" + param._warnSemester + ", 0) ");
            }
            stb.append("       AS ABSENCE_HIGH, ");
            stb.append("     VALUE(T1.GET_ABSENCE_HIGH, 0) ");
            if (param._useAbsenceWarn) {
                stb.append("      - VALUE(T1.ABSENCE_WARN_SHUTOKU_SEM" + param._warnSemester + ", 0) ");
            }
            stb.append("       AS GET_ABSENCE_HIGH ");
            stb.append(" FROM ");
            stb.append("     V_CREDIT_SPECIAL_MST T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.COURSECD = ? ");
            stb.append("     AND T1.MAJORCD = ? ");
            stb.append("     AND T1.GRADE = '" + param._grade + "' ");
            stb.append("     AND T1.COURSECODE = ? ");
            stb.append("     AND T1.SPECIAL_GROUP_CD = '" + SPECIAL_ALL + "' ");
            return stb.toString();
        }

        private static String getAbsenceHighSpecialSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     COMP_ABSENCE_HIGH as ABSENCE_HIGH, GET_ABSENCE_HIGH ");
            stb.append(" FROM ");
            stb.append("     SCHREG_ABSENCE_HIGH_SPECIAL_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + param._year + "' AND ");
            stb.append("     DIV = '2' AND "); // 1:年間、2:随時
            stb.append("     SPECIAL_GROUP_CD = '" + SPECIAL_ALL + "' AND ");
            stb.append("     SCHREGNO = ? ");
            return stb.toString();
        }

        private static void setAttendSubclass(final DB2UDB db2, final Param param, final List<Student> studentList) {

            final Map<String, List<Student>> hrStudentListMap = new HashMap<String, List<Student>>();
            for (final Student student : studentList) {
                if (null == hrStudentListMap.get(param._grade + student._hrClass)) {
                    hrStudentListMap.put(param._grade + student._hrClass, new ArrayList<Student>());
                }
                hrStudentListMap.get(param._grade + student._hrClass).add(student);
            }

            PreparedStatement ps = null;
            try {
                param._attendParamMap.put("grade", param._grade);
                param._attendParamMap.put("hrClass", "?");
                param._attendParamMap.put("schregno", "?");
                param._attendParamMap.put("useTestCountflg", "TESTITEM_MST_COUNTFLG_NEW_SDIV");

                final String sql = AttendAccumulate.getAttendSubclassSql(
                        param._year,
                        param._semester,
                        param._dateS,
                        param._date,
                        param._attendParamMap);

                ps = db2.prepareStatement(sql);

                Integer zero = new Integer(0);

                for (final String gradeHrclass : hrStudentListMap.keySet()) {

                    ps.setString(2, gradeHrclass.substring(2));

                    final List<Student> hrStudentList = hrStudentListMap.get(gradeHrclass);

                    for (final Student student : hrStudentList) {

                        for (final Map rs : KnjDbUtils.query(db2,  ps, new Object[] {student._schregno})) {
                            if (!SEMEALL.equals(KnjDbUtils.getString(rs, "SEMESTER"))) {
                                continue;
                            }
                            final String subclassCd = KnjDbUtils.getString(rs, "SUBCLASSCD");
                            final int mlesson = KnjDbUtils.getInt(rs, "MLESSON", zero).intValue();
                            final Double kekka = null == KnjDbUtils.getString(rs, "SICK2") ? null : Double.valueOf(KnjDbUtils.getString(rs, "SICK2"));
                            final Double replacedKekka = null == KnjDbUtils.getString(rs, "REPLACED_SICK") ? null : Double.valueOf(KnjDbUtils.getString(rs, "REPLACED_SICK"));
                            if (student._testSubclass.containsKey(subclassCd)) {
                                final TestScore testScore = student._testSubclass.get(subclassCd);
                                testScore._jisu = mlesson;
                                if (null != testScore._combinedSubclasscd) {
                                    testScore._kekka = replacedKekka;
                                } else {
                                    testScore._kekka = kekka;
                                }
                            }
                            if (param._attendSubclassSpecialMinutes.containsKey(subclassCd)) {
                                final String specialGroupCd = (String) param._attendSubclassSpecialGroupCd.get(subclassCd);
                                final Integer minutesPerKoma = (Integer) param._attendSubclassSpecialMinutes.get(subclassCd);

                                addMinutes(specialGroupCd, minutesPerKoma, mlesson, student._attendance._spGroupLessonMinutes);
                                addMinutes(specialGroupCd, minutesPerKoma, null == kekka ? 0 : kekka.intValue(), student._attendance._spGroupAbsentMinutes);
                            }
                        }

                        student._attendance._specialLesson = minutesToKoma(param, student._attendance._spGroupLessonMinutes);
                        student._attendance._specialAbsent = minutesToKoma(param, student._attendance._spGroupAbsentMinutes);
                    }
                }

            } catch (Exception e) {
                log.fatal("exception!", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }

        private static int minutesToKoma(final Param param, final Map spGroupMinutes) {
            int totalSpecialAttendExe = 0;
            for (final Iterator it = spGroupMinutes.values().iterator(); it.hasNext();) {
                final Integer groupAbsentMinutes = (Integer) it.next();
                totalSpecialAttendExe += getSpecialAttendExe(param, groupAbsentMinutes.intValue());
            }
            return totalSpecialAttendExe;
        }

        /**
         * 欠課時分を欠課時数に換算した値を得る
         * @param kekka 欠課時分
         * @return 欠課時分を欠課時数に換算した値
         */
        private static int getSpecialAttendExe(final Param param, final int kekka) {
            final int jituJifun = Integer.parseInt(StringUtils.defaultString(param._knjSchoolMst._jituJifunSpecial, "50"));
            final BigDecimal bigKekka = new BigDecimal(kekka);
            final BigDecimal bigJitu = new BigDecimal(jituJifun);
            final String retSt = bigKekka.divide(bigJitu, 1, BigDecimal.ROUND_DOWN).toString();
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

        private static void addMinutes(final String specialGroupCd, final Integer komaMinutes, final int koma, final Map spGroupMinutes) {
            final int totalMinutes = koma * komaMinutes.intValue();
            if (!spGroupMinutes.containsKey(specialGroupCd)) {
                spGroupMinutes.put(specialGroupCd, new Integer(0));
            }
            final int storedMinutes = ((Integer) spGroupMinutes.get(specialGroupCd)).intValue();
            spGroupMinutes.put(specialGroupCd, new Integer(totalMinutes + storedMinutes));
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
        final Map<String, TestScore> _testSubclass;
        final Map _recordScoreSubclass;
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
        		final Param param,
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
            _recordScoreSubclass = new TreeMap();
            _recordChkfinSubclass = new TreeMap();
            _testAll = new TestScore(param, "999999");
            _testAll._score = "";
        }

        /** 指定科目コードの平均点を表示するか */
        public boolean isPrintSubclassScoreAvg(final String subclassCd, final Param param) {
            return true;
        }

        public String toString() {
            return "学籍：" + _schregno + " 名前：" + _name;
        }

        private static List<Student> getStudentList(final DB2UDB db2, final Param param) {
            final List<Student> studentList = new ArrayList<Student>();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getStudentInfoSql(param);
//                log.debug("getStudentInfoSql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final Student student = new Student(param,
                    		                      rs.getString("SCHREGNO"),
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

        private static void setMapTestSubclass(final DB2UDB db2, final Param param, final List<Student> studentList) throws SQLException {

            final String sql = TestScore.getRecordScoreSql(param);
            PreparedStatement ps = db2.prepareStatement(sql);

            for (final Iterator it = studentList.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                ResultSet rs = null;
                try {
                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();

                    while (rs.next()) {
                        student._recordScoreSubclass.put(getSubclassCd(rs, param), rs.getString("SCORE"));
                    }
                } finally {
                    DbUtils.closeQuietly(rs);
                    db2.commit();
                }
            }
            DbUtils.closeQuietly(ps);
        }
    }

    private static class TestScore implements Comparable {
    	final String _subclasscd;
        String _name;
        String _combinedSubclasscd;
        String _electdiv;
        String _score;
        String _avg;
        String _avgHr;
        String _avgChair;
        String _rank;
        String _rankHr;
        String _rankChair;
        String _credit;
        String _absenceHigh;
        String _getAbsenceHigh;
        String _average;
        String _chaircd;
        String _slumpScore;
        String _slumpMark;
        String _sidouinput;
        String _sidouinputinf;
        String _passScore;
        Double _kekka;
        int _jisu;
        int _cnt;
        int _cntHr;
        final Param _param;
        public TestScore(final Param param, final String subclasscd) {
        	_param = param;
        	_subclasscd = subclasscd;
        }

        private int getFailValue(final Param param) {
            if (param.isKetten() && null != param._ketten && !"".equals(param._ketten)) {
                return Integer.parseInt(param._ketten);
            }
            return -1;
        }

        private boolean isKetten(final Param param) {
//            if (param.isRecordSlump()) {
//                if (null != _sidouinput) {
//                    if (SIDOU_INPUT_INF_MARK.equals(_sidouinputinf)) { // 記号
//                        if (null != param._d054Namecd2Max && param._d054Namecd2Max.equals(_slumpMark)) {
//                            return true;
//                        }
//                    } else if (SIDOU_INPUT_INF_SCORE.equals(_sidouinputinf)) { // 得点
//                        return "1".equals(_slumpScore);
//                    }
//                }
                if (null != param._testcd && param._testcd.endsWith("09")) { // 評定、仮評定は1の場合欠点
                    return NumberUtils.isDigits(_score) && 1 == Integer.parseInt(_score);
                }
                if (NumberUtils.isDigits(_passScore)) {
                	return NumberUtils.isDigits(_score) && Integer.parseInt(_score) < Integer.parseInt(_passScore);
                }
                return "1".equals(_slumpScore);
//            } else {
//                return score <= getFailValue(param);
//            }
        }

        public String toString() {
            return "科目：" + _name
                    + "得点：" + _score
                    + " 平均：" + _avg
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
            if (param._isOutputDebug) {
            	log.info("rank sql = " + sql);
            }
            PreparedStatement ps = null;
            try {
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
                            testScore._rankHr = rs.getString("CLASS_" + param.getRankAvgField() + "RANK");
                            testScore._passScore = rs.getString("PASS_SCORE");

                            if (!"欠".equals(testScore._score)) {
                                totalScore += Integer.parseInt(testScore._score);
                            }
                            subclassCount += 1;
                        }
                        if (subclassCd.equals(SUBCLASS9)) {
                            student._testAll._score = rs.getString("SCORE");
                            student._testAll._rank = rs.getString(param.getRankField() + param.getRankAvgField() + "RANK");
                            student._testAll._rankHr = rs.getString("CLASS_" + param.getRankAvgField() + "RANK");
                            student._testAll._average = roundHalfUp(rs.getBigDecimal("AVG"));
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
        public static String getRecordRankTestAppointSql(final Param param) {
        	final String semester = param._testcd.substring(0, 1);
        	final String testkindcd = param._testcd.substring(1, 3);
        	final String testitemcd = param._testcd.substring(3, 5);
        	final String scoreDiv = param._testcd.substring(5, 7);

            final StringBuffer stb = new StringBuffer();

            stb.append(" SELECT ");
            stb.append("     T1.* ");
            stb.append("   , PERF.PASS_SCORE ");
            stb.append(" FROM ");
            stb.append("     RECORD_RANK_SDIV_DAT T1 ");
            stb.append(" LEFT JOIN SCHREG_REGD_DAT REGD ON REGD.SCHREGNO = T1.SCHREGNO ");
            stb.append("     AND REGD.YEAR = '" + param._year + "' ");
            stb.append("     AND REGD.SEMESTER = '" + param._schregSemester + "' ");
            stb.append(" LEFT JOIN PERFECT_RECORD_DAT PERF ON PERF.YEAR = T1.YEAR ");
            stb.append("     AND PERF.SEMESTER = T1.SEMESTER ");
            stb.append("     AND PERF.TESTKINDCD = T1.TESTKINDCD ");
            stb.append("     AND PERF.TESTITEMCD = T1.TESTITEMCD ");
            stb.append("     AND PERF.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("     AND PERF.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("     AND PERF.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("     AND (PERF.DIV =  '01' AND PERF.GRADE = '00' ");
            stb.append("       OR PERF.DIV <> '01' AND PERF.GRADE = REGD.GRADE ");
            stb.append("         ) ");
            stb.append("     AND (PERF.DIV     IN ('01', '02') AND PERF.COURSECD = '0'           AND PERF.MAJORCD = '000'        AND PERF.COURSECODE = '0000' ");
            stb.append("       OR PERF.DIV NOT IN ('01', '02') AND PERF.COURSECD = REGD.COURSECD AND PERF.MAJORCD = REGD.MAJORCD AND PERF.COURSECODE = REGD.COURSECODE ");
            stb.append("         ) ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.SEMESTER = '" + semester + "' AND T1.TESTKINDCD = '" + testkindcd + "' AND T1.TESTITEMCD = '" + testitemcd + "' AND T1.SCORE_DIV = '" + scoreDiv + "' ");
            stb.append("     AND T1.SCHREGNO = ? ");

            return stb.toString();
        }

        private static void setRankChair(final DB2UDB db2, final Param param, final List<Student> studentList) throws SQLException {
            final String sql = getRankChairSql(param);
            if (param._isOutputDebug) {
            	log.info("rank chair sql = " + sql);
            }
            PreparedStatement ps = null;
            try {
                ps = db2.prepareStatement(sql);

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    ps.setString(1, student._schregno);

                    ResultSet rs = ps.executeQuery();
                    boolean hasScore = false;
                    while (rs.next()) {
                        final String subclassCd = getSubclassCd(rs, param);
                        final String chairCd = rs.getString("CHAIRCD");
                        if (student._testSubclass.containsKey(subclassCd)) {
                            final TestScore testScore = student._testSubclass.get(subclassCd);

                            if (chairCd.equals(testScore._chaircd)) {
                                hasScore = true;
                                testScore._rankChair = rs.getString(param.getRankField() + param.getRankAvgField() + "RANK");
                            }
                        }
                    }
                    DbUtils.closeQuietly(rs);
                    student._hasScore = hasScore;
                }

            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }

        /**
         * 講座席次データの取得
         * @param tableDiv 1:DAT, 2:V_DAT
         * @return sql
         */
        public static String getRankChairSql(final Param param) {
        	final String semester = param._testcd.substring(0, 1);
        	final String testkindcd = param._testcd.substring(1, 3);
        	final String testitemcd = param._testcd.substring(3, 5);
        	final String scoreDiv = param._testcd.substring(5, 7);
        	
            final StringBuffer stb = new StringBuffer();

            stb.append(" SELECT ");
            stb.append("     T1.* ");
            stb.append(" FROM ");
            stb.append("     RECORD_RANK_CHAIR_SDIV_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.SEMESTER = '" + semester + "' AND T1.TESTKINDCD = '" + testkindcd + "' AND T1.TESTITEMCD = '" + testitemcd + "' AND T1.SCORE_DIV = '" + scoreDiv + "' ");
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

        private static void setAvg(final DB2UDB db2, final Param param, final List<Student> studentList) throws SQLException {
            PreparedStatement ps = null;
            // 学年/コースの平均
            try {
                final String avgDiv = param.getAvgDiv();
                final String sql = getAverageSql(param, avgDiv);
                if (param._isOutputDebug) {
                	log.info("avg sql = " + sql);
                }
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
                            testScore._avg = roundHalfUp(rs.getBigDecimal("AVG"));
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

            // 学級の平均
            try {
                final String avgDiv = "2";
                final String sql = getAverageSql(param, avgDiv);
                if (param._isOutputDebug) {
                	log.info("avg2 sql = " + sql);
                }
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
                            testScore._avgHr = roundHalfUp(rs.getBigDecimal("AVG"));
                        }
                        if (SUBCLASS9.equals(subclassCd)) {
                            student._testAll._cntHr = rs.getInt("COUNT");
                        }
                        if (!isSubclassAll(subclassCd)) {
                            totalScore += rs.getInt("SCORE");
                            totalCount += rs.getInt("COUNT");
                        }
                    }
                    DbUtils.closeQuietly(rs);
                    if (totalCount > 0) {
                    	student._testAll._avgHr = divide(totalScore, totalCount);
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
        public static String getAverageSql(final Param param, final String avgDiv) {
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
            stb.append("     AND T1.AVG_DIV = '" + avgDiv + "' ");
            stb.append("     AND T1.GRADE = ? ");
            stb.append("     AND T1.HR_CLASS = ? ");
            stb.append("     AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE = ? ");

            return stb.toString();
        }

        private static void setAvgChair(final DB2UDB db2, final Param param, final List<Student> studentList) throws SQLException {
            PreparedStatement ps = null;
            // 講座の平均
            try {
                final String avgDiv = "1";
                final String sql = getAverageChairSql(param, avgDiv);
                if (param._isOutputDebug) {
                	log.info("avg chair sql = " + sql);
                }
                ps = db2.prepareStatement(sql);

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    ps.setString(1, student._grade);
                    ps.setString(2, getAverageParam(2, avgDiv, student));
                    ps.setString(3, getAverageParam(3, avgDiv, student));

                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        final String subclassCd = getSubclassCd(rs, param);
                        final String chairCd = rs.getString("CHAIRCD");
                        if (student._testSubclass.containsKey(subclassCd)) {
                            final TestScore testScore = student._testSubclass.get(subclassCd);

                            if (chairCd.equals(testScore._chaircd)) {
                                testScore._avgChair = roundHalfUp(rs.getBigDecimal("AVG"));
                            }
                        }
                    }
                    DbUtils.closeQuietly(rs);
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
        public static String getAverageChairSql(final Param param, final String avgDiv) {
        	final String semester = param._testcd.substring(0, 1);
        	final String testkindcd = param._testcd.substring(1, 3);
        	final String testitemcd = param._testcd.substring(3, 5);
        	final String scoreDiv = param._testcd.substring(5, 7);

        	final StringBuffer stb = new StringBuffer();

            stb.append(" SELECT ");
            stb.append("     T1.* ");
            stb.append(" FROM ");
            stb.append("     RECORD_AVERAGE_CHAIR_SDIV_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.SEMESTER = '" + semester + "' AND T1.TESTKINDCD = '" + testkindcd + "' AND T1.TESTITEMCD = '" + testitemcd + "' AND T1.SCORE_DIV = '" + scoreDiv + "' ");
            stb.append("     AND T1.AVG_DIV = '" + avgDiv + "' ");
            stb.append("     AND T1.GRADE = ? ");
            stb.append("     AND T1.HR_CLASS = ? ");
            stb.append("     AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE = ? ");
            return stb.toString();
        }

        private static void setKetten(final DB2UDB db2, final Param param, final List<Student> studentList) throws SQLException {
            PreparedStatement ps = null;
            // 欠点対象
            try {
                final String sql = getKettenSql(param);
                if (param._isOutputDebug) {
                	log.info("ketten sql = " + sql);
                }
                ps = db2.prepareStatement(sql);

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    ps.setString(1, student._schregno);

                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        final String subclassCd = getSubclassCd(rs, param);
                        if (student._testSubclass.containsKey(subclassCd)) {
                            final TestScore testScore = student._testSubclass.get(subclassCd);
                            testScore._sidouinput = rs.getString("SIDOU_INPUT");
                            testScore._sidouinputinf = rs.getString("SIDOU_INPUT_INF");
                            testScore._slumpScore = rs.getString("SLUMP_SCORE");
//                            testScore._slumpMark = rs.getString("SLUMP_MARK");
//                            testScore._passScore = rs.getString("PASS_SCORE");
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
        	final String semester = param._testcd.substring(0, 1);
        	final String testkindcd = param._testcd.substring(1, 3);
        	final String testitemcd = param._testcd.substring(3, 5);
        	final String scoreDiv = param._testcd.substring(5, 7);

            final StringBuffer stb = new StringBuffer();
//            if (param.isRecordSlump()) {
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
//                stb.append("     T1.SLUMP, ");
                stb.append("     cast(null as smallint) as PASS_SCORE, ");
                stb.append("     W1.SIDOU_INPUT, ");
                stb.append("     W1.SIDOU_INPUT_INF, ");
//                stb.append("     CASE WHEN W1.SIDOU_INPUT = '1' AND W1.SIDOU_INPUT_INF = '1' THEN T1.MARK END AS SLUMP_MARK, ");
//                stb.append("     CASE WHEN W1.SIDOU_INPUT = '1' AND W1.SIDOU_INPUT_INF = '2' THEN ");
                stb.append("         CASE WHEN VALUE(T2.COUNT, 0) > 0 THEN ");
                stb.append("          (SELECT MAX(L3.ASSESSLEVEL) ");
                stb.append("           FROM RELATIVEASSESS_MST L3 ");
                stb.append("           WHERE L3.GRADE = '" + param._grade + "' AND L3.ASSESSCD = '3' ");
                stb.append("             AND T1.SCORE BETWEEN L3.ASSESSLOW AND L3.ASSESSHIGH  ");
                stb.append("             AND L3.SUBCLASSCD = T1.SUBCLASSCD ");
                stb.append("             AND L3.CLASSCD = T1.CLASSCD ");
                stb.append("             AND L3.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("             AND L3.CURRICULUM_CD = T1.CURRICULUM_CD ");
                stb.append("          ) ELSE ");
                stb.append("          (SELECT MAX(L3.ASSESSLEVEL) ");
                stb.append("           FROM ASSESS_MST L3 ");
                stb.append("           WHERE L3.ASSESSCD = '3' ");
                stb.append("             AND T1.SCORE BETWEEN L3.ASSESSLOW AND L3.ASSESSHIGH  ");
                stb.append("          ) ");
//                stb.append("         END ");
                stb.append("    END AS SLUMP_SCORE ");
                stb.append(" FROM ");
//                stb.append("     RECORD_SLUMP_SDIV_DAT T1 ");
                stb.append("     RECORD_RANK_SDIV_DAT T1 ");
                stb.append("     INNER JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV W1 ON T1.YEAR = W1.YEAR ");
                stb.append("            AND T1.SEMESTER = W1.SEMESTER ");
                stb.append("            AND T1.TESTKINDCD = W1.TESTKINDCD ");
                stb.append("            AND T1.TESTITEMCD = W1.TESTITEMCD ");
                stb.append("            AND T1.SCORE_DIV = W1.SCORE_DIV ");
                stb.append("     LEFT JOIN REL_COUNT T2 ON T2.SUBCLASSCD = T1.SUBCLASSCD ");
                stb.append("            AND T2.CLASSCD = T1.CLASSCD ");
                stb.append("            AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("            AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
                stb.append(" WHERE ");
                stb.append("     T1.YEAR = '" + param._year + "' AND ");
                stb.append("     T1.SEMESTER = '" + semester + "' AND T1.TESTKINDCD = '" + testkindcd + "' AND T1.TESTITEMCD = '" + testitemcd + "' AND T1.SCORE_DIV = '" + scoreDiv + "' AND ");
                stb.append("     T1.SCHREGNO = ? ");
                
//            }
            return stb.toString();
        }

        private static void setChaircd(final DB2UDB db2, final Param param, final List<Student> studentList) throws SQLException {

            final String sql = getChaircdSql(param);
            PreparedStatement ps = db2.prepareStatement(sql);

            for (final Student student : studentList) {

                ps.setString(1, student._schregno);
                
                for (final Map row : KnjDbUtils.query(db2, ps, new Object[] {student._schregno})) {
                	final String subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");
                	
                    final TestScore testScore = student._testSubclass.get(subclasscd);
                    if (null != testScore) {
                        testScore._chaircd = KnjDbUtils.getString(row, "CHAIRCD");
                    }
                }
            }
            DbUtils.closeQuietly(ps);
        }

        private static String getChaircdSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD ");
            stb.append("   , MIN(T1.CHAIRCD) as CHAIRCD ");
            stb.append(" FROM ");
            stb.append("     CHAIR_DAT T1, ");
            stb.append("     CHAIR_STD_DAT T2 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' AND ");
            stb.append("     T1.SEMESTER = '" + param._schregSemester + "' AND ");
            stb.append("     T2.YEAR = T1.YEAR AND ");
            stb.append("     T2.SEMESTER = T1.SEMESTER AND ");
            stb.append("     T2.CHAIRCD = T1.CHAIRCD AND ");
            stb.append("     T2.SCHREGNO = ? ");
            stb.append(" GROUP BY ");
            stb.append("     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD ");

            return stb.toString();
        }

        private static void setChairStd(final DB2UDB db2, final Param param, final List<Student> studentList) throws SQLException {
            PreparedStatement ps;
            String sql = getChairStdSql(param);
            if (param._isOutputDebug) {
            	log.info(" chair std sql = " + sql);
            }
            ps = db2.prepareStatement(sql);

            for (final Student student : studentList) {

                for (final Iterator stit = KnjDbUtils.query(db2, ps, new Object[] { student._schregno }).iterator(); stit.hasNext();) {
                	final Map row = (Map) stit.next();
                    final String subclassCd = getSubclassCd(row, param);
                    String strKetu = "";
                    if (student._recordScoreSubclass.containsKey(subclassCd)) {
                        strKetu = "欠";
                    }
                    if (!param._printTestOnly || param._printTestOnly && student._recordScoreSubclass.containsKey(subclassCd)) {
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
            if ("1".equals(param._knjd192vPrintClass90)) {
            	stb.append("     substr(T1.SUBCLASSCD,1,2) <= '90' AND ");
            } else {
            	stb.append("     substr(T1.SUBCLASSCD,1,2) <  '90' AND ");
            }
            stb.append("     T2.SCHREGNO = ? ");
            stb.append(" ORDER BY ");
            stb.append("     T1.CLASSCD, ");
            stb.append("     T1.SCHOOL_KIND, ");
            stb.append("     T1.CURRICULUM_CD, ");
            stb.append("     T1.SUBCLASSCD ");
            return stb.toString();
        }

        private static void setCreditMst(final DB2UDB db2, final Param param, final List<Student> studentList) throws SQLException {
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
                    final TestScore testScore = student._testSubclass.get(subclassCd);

                    ps.setString(5, subclassCd.substring(0, 2));
                    ps.setString(6, subclassCd);

                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        testScore._credit = rs.getString("CREDITS");
                        if (param.isHoutei()) {
                            testScore._absenceHigh = rs.getString("ABSENCE_HIGH");
                            testScore._getAbsenceHigh = rs.getString("GET_ABSENCE_HIGH");
                        }
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
            if (param._useAbsenceWarn) {
                stb.append("    - VALUE(T1.ABSENCE_WARN_RISHU_SEM" + param._warnSemester + ", 0) ");
            }
            stb.append("     AS ABSENCE_HIGH, ");
            stb.append("     VALUE(T1.GET_ABSENCE_HIGH, 0) ");
            if (param._useAbsenceWarn) {
                stb.append("    - VALUE(T1.ABSENCE_WARN_SHUTOKU_SEM" + param._warnSemester + ", 0) ");
            }
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
            stb.append("     AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = ? ");

            return stb.toString();
        }

        private static void setAbsenceHigh2(final DB2UDB db2, final Param param, final List<Student> studentList) throws SQLException {
            //実・欠課数上限値
            final String sql = getAbsenceHighSql(param);
            PreparedStatement ps = db2.prepareStatement(sql);

            for (final Iterator stit = studentList.iterator(); stit.hasNext();) {

                final Student student = (Student) stit.next();
                ps.setString(2, student._schregno);

                for (final Iterator it = student._testSubclass.keySet().iterator(); it.hasNext();) {
                    final String subclassCd = (String) it.next();
                    final TestScore testScore = student._testSubclass.get(subclassCd);

                    ps.setString(1, subclassCd);
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        testScore._absenceHigh = rs.getString("COMP_ABSENCE_HIGH");
                        testScore._getAbsenceHigh = rs.getString("GET_ABSENCE_HIGH");
                    }
                    DbUtils.closeQuietly(rs);
                }
            }
            DbUtils.closeQuietly(ps);
        }

        private static String getAbsenceHighSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     VALUE(T1.COMP_ABSENCE_HIGH, 0) ");
            if (param._useAbsenceWarn) {
                stb.append("    - VALUE(T2.ABSENCE_WARN_RISHU_SEM" + param._warnSemester + ", 0) ");
            }
            stb.append("     AS COMP_ABSENCE_HIGH, ");
            stb.append("     VALUE(T1.GET_ABSENCE_HIGH, 0) ");
            if (param._useAbsenceWarn) {
                stb.append("    - VALUE(T2.ABSENCE_WARN_SHUTOKU_SEM" + param._warnSemester + ", 0) ");
            }
            stb.append("     AS GET_ABSENCE_HIGH ");
            stb.append(" FROM ");
            stb.append("     SCHREG_ABSENCE_HIGH_DAT T1 ");
            stb.append("     LEFT JOIN SCHREG_REGD_DAT T3 ON T3.SCHREGNO = T1.SCHREGNO ");
            stb.append("         AND T3.YEAR = T1.YEAR ");
            stb.append("         AND T3.SEMESTER = '" + param._schregSemester + "' ");
            stb.append("     LEFT JOIN V_CREDIT_MST T2 ON T2.YEAR = T1.YEAR ");
            stb.append("         AND T2.COURSECD = T3.COURSECD ");
            stb.append("         AND T2.MAJORCD = T3.MAJORCD ");
            stb.append("         AND T2.GRADE = T3.GRADE ");
            stb.append("         AND T2.COURSECODE = T3.COURSECODE ");
            stb.append("         AND T2.CLASSCD = T1.CLASSCD ");
            stb.append("         AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("         AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("         AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' AND ");
            stb.append("     T1.DIV = '2' AND "); // 1:年間、2:随時
            stb.append("     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = ? AND ");
            stb.append("     T1.SCHREGNO = ? ");

            return stb.toString();
        }

        private static String divide(final int v1, final int v2) {
            return new BigDecimal(v1).divide(new BigDecimal(v2), 1, BigDecimal.ROUND_HALF_UP).toString();
        }

        private static void setRankAvgAverage(final DB2UDB db2, final Param param, final List<Student> studentList) throws SQLException {
        	final Map cache = new HashMap();

        	final String avgDivHr = "2";
            final String sql = sqlRankAvgAverage(param, avgDivHr);
            PreparedStatement ps = db2.prepareStatement(sql);
            for (final Iterator stit = studentList.iterator(); stit.hasNext();) {
                final Student student = (Student) stit.next();

            	final String cacheKey = student._hrClass;
                if (!getMappedMap(cache, avgDivHr).containsKey(cacheKey)) {

                    ps.setString(1, student._hrClass);

                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                    	getMappedMap(cache, avgDivHr).put(cacheKey, rs.getString("AVG_HR_AVERAGE"));
                    }
                    DbUtils.closeQuietly(rs);

                }
            	student._testAll._avgHr = (String) getMappedMap(cache, avgDivHr).get(cacheKey);

            }
            DbUtils.closeQuietly(ps);


            final String avgDiv = param.getAvgDiv();
            final String sql2 = sqlRankAvgAverage(param, avgDiv);
            PreparedStatement ps2 = db2.prepareStatement(sql2);
            for (final Iterator stit = studentList.iterator(); stit.hasNext();) {

            	final Student student = (Student) stit.next();
            	String cacheKey = null;

            	if ("1".equals(avgDiv)) {
            		cacheKey = student._grade;
                } else if ("3".equals(avgDiv)) {
            		cacheKey = student._courseCd + student._majorCd + student._courseCode;
                } else if ("4".equals(avgDiv)) {
            		cacheKey = student._courseCd + student._majorCd;
                }

                if (!getMappedMap(cache, avgDiv).containsKey(cacheKey)) {
                	log.info(" q " + cacheKey);
                	if ("1".equals(avgDiv)) {
                    } else if ("3".equals(avgDiv)) {
                        ps2.setString(1, student._courseCd);
                        ps2.setString(2, student._majorCd);
                        ps2.setString(3, student._courseCode);
                    } else if ("4".equals(avgDiv)) {
                        ps2.setString(1, student._courseCd);
                        ps2.setString(2, student._majorCd);
                    }

                	ResultSet rs = ps2.executeQuery();
                	while (rs.next()) {
                    	getMappedMap(cache, avgDiv).put(cacheKey, rs.getString("AVG_HR_AVERAGE"));
                	}
                	DbUtils.closeQuietly(rs);
                }
            	student._testAll._avg = (String) getMappedMap(cache, avgDiv).get(cacheKey);
            }
            DbUtils.closeQuietly(ps2);
        }

        /**
         * SQL 総合点・平均点の学級平均を取得するSQL
         */
        private static String sqlRankAvgAverage(final Param param, final String avgDiv) {
        	final String semester = param._testcd.substring(0, 1);
        	final String testkindcd = param._testcd.substring(1, 3);
        	final String testitemcd = param._testcd.substring(3, 5);
        	final String scoreDiv = param._testcd.substring(5, 7);

            final StringBuffer stb = new StringBuffer();

            stb.append("WITH ");

            //対象生徒の表 クラスの生徒
            stb.append(" SCHNO_A AS(");
            stb.append("     SELECT  W1.SCHREGNO ");
            stb.append("     FROM    SCHREG_REGD_DAT W1 ");
            stb.append("     INNER JOIN V_SEMESTER_GRADE_MST W2 ON W2.YEAR = W1.YEAR AND W2.SEMESTER = W1.SEMESTER AND W2.GRADE = W1.GRADE ");

            stb.append("     WHERE   W1.YEAR = '" + param._year + "' ");
            if (SEMEALL.equals(param._semester)) {
                stb.append("     AND W1.SEMESTER = '" + param._schregSemester + "' ");
            } else {
                stb.append("     AND W1.SEMESTER = '" + param._semester + "' ");
                stb.append("     AND NOT EXISTS(SELECT 'X'  FROM  SCHREG_BASE_MST S1");
                stb.append("                    WHERE S1.SCHREGNO = W1.SCHREGNO AND S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < W2.SDATE) ");
            }
            stb.append("         AND W1.GRADE = '" + param._grade + "' ");
            if ("2".equals(avgDiv)) {
                stb.append("            AND W1.HR_CLASS = ? ");
            } else if ("1".equals(avgDiv)) {
            } else if ("3".equals(avgDiv)) {
                stb.append("            AND W1.COURSECD = ? AND W1.MAJORCD = ? AND W1.COURSECODE = ? ");
            } else if ("4".equals(avgDiv)) {
                stb.append("            AND W1.COURSECD = ? AND W1.MAJORCD = ? ");
            }
            stb.append(") ");

            stb.append("SELECT ");
            stb.append("        DECIMAL(ROUND(AVG(FLOAT(W3.SCORE))*10,0)/10,5,1) AS AVG_HR_TOTAL ");
            stb.append("       ,DECIMAL(ROUND(AVG(FLOAT(W3.AVG))*10,0)/10,5,1) AS AVG_HR_AVERAGE ");
            stb.append("  FROM  RECORD_RANK_SDIV_DAT W3 ");
            stb.append("  INNER JOIN SCHNO_A T2 ON T2.SCHREGNO = W3.SCHREGNO ");
            stb.append(" WHERE  W3.YEAR = '" + param._year + "' ");
            stb.append("   AND  W3.SEMESTER = '" + semester + "' AND W3.TESTKINDCD = '" + testkindcd + "' AND W3.TESTITEMCD = '" + testitemcd + "' AND W3.SCORE_DIV = '" + scoreDiv + "' ");
            stb.append("   AND  W3.SUBCLASSCD = '" + SUBCLASS9 + "' ");

            return stb.toString();
        }

		public int compareTo(Object o) {
			if (o instanceof TestScore) {
				TestScore ts = (TestScore) o;
				int cmp;
				if ("1".equals(_param._orderUseElectdiv)) {
					cmp = StringUtils.defaultString(_electdiv, "0").compareTo(StringUtils.defaultString(ts._electdiv, "0"));
					if (0 != cmp) {
						return cmp;
					}
				}
				cmp = _subclasscd.compareTo(ts._subclasscd);
				return cmp;
			}
			return 0;
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
        final String _scorediv;
        final String _testName;
        final String _semester;
        final String _grade;
        final String _hrClass;
        final String _groupDiv;
        final String _outputKijun;
        final String[] _selectData;
        final String _z010;
        final String _ketten;
        final String _checkKettenDiv; //欠点プロパティ 1,2,設定無し(1,2以外)
        final String _countFlg;
        final String _scoreFlg;
        final String _schregSemester;
        final boolean _isRuikei;
        final String _notUseAttendSubclassSpecial;
        final String _orderUseElectdiv;
        final String _knjd192vPrintClass90;
        private String _semesterName;
        private String _d054Namecd2Max;

        /** 出欠集計日付 */
        final String _dateS;
        final String _date;
        /** フォーム選択（最大科目数：１５or２０） */
        final boolean _printForm20;
        /** 試験科目のみ出力する */
        final boolean _printTestOnly;
        /** 順位を出力しない */
        final String _notPrintRank;
        /** 平均点を出力しない */
        final String _notPrintAvg;
        /** 欠課を出力しない */
        final String _notPrintKekka;
        /** 注意 or 超過 */
        final boolean _useAbsenceWarn;
        /** 一日出席欄を出力する */
        final String _printOnedayAttend;
        /** 教育課程コードを使用するか */
        final String _useCurriculumcd;
        /** 注意週数学期 */
        final String _warnSemester;
        /** 「考査を実施しない講座は平均点を表示しない」を処理するか */
        final String _knjd192AcheckNoExamChair;
        /** 欠点を使用しない */
        final boolean _useKetten;
        /** 欠点を使用しない */
        final boolean _notUseKetten;
        /** 総合点の平均点を使用しない */
        final boolean _useTotalAverage;
        /** 裁断用にソートして出力する */
        final boolean _sortCutting;

        final boolean _isSapporo;

        /** 特別活動設定データのマップ */
        final Map _attendSubclassSpecialMinutes = new HashMap();
        final Map _attendSubclassSpecialGroupCd = new HashMap();

        private KNJSchoolMst _knjSchoolMst;
        final Map _attendParamMap;
        final boolean _isOutputDebug;

        /** 氏名欄に学籍番号の表示/非表示 1:表示 それ以外:非表示 */
        final String _use_SchregNo_hyoji;
        /** 講座名簿の終了日付が学期の終了日付と同一 */
        final String _printSubclassLastChairStd;

        final String _kekkaAsInt; //1:整数を表示する（absent_cov = 3 or 4 の時）
        final KNJDefineSchool _definecode;  //各学校における定数等設定のクラス

        final String _nendo;

        Param(final DB2UDB db2, final HttpServletRequest request) {
            _year = request.getParameter("YEAR");
            _categoryIsClass = request.getParameter("CATEGORY_IS_CLASS");
            _testcd = request.getParameter("SUB_TESTCD");
            _scorediv = _testcd.substring(_testcd.length() - 2);
            _semester = request.getParameter("SEMESTER");
             _schregSemester = "9".equals(_semester) ? request.getParameter("CTRL_SEME") : _semester;
            _testName = getTestName(db2);
            setSemesterName(db2);
            _grade = request.getParameter("GRADE");
            _hrClass = request.getParameter("HR_CLASS");
            _groupDiv = request.getParameter("GROUP_DIV");
            _outputKijun = request.getParameter("OUTPUT_KIJUN");
            _selectData = request.getParameterValues("CATEGORY_SELECTED");  //学籍番号または学年-組
            _notUseAttendSubclassSpecial = request.getParameter("notUseAttendSubclassSpecial");
            _orderUseElectdiv = request.getParameter("ORDER_USE_ELECTDIV");
            _knjd192vPrintClass90 = request.getParameter("knjd192vPrintClass90");

            _z010 = setNameMst(db2, "Z010", "00");
            _isSapporo = "sapporo".equals(_z010);

            _isRuikei = "1".equals(request.getParameter("DATE_DIV"));
            _ketten = request.getParameter("KETTEN");
            _checkKettenDiv = request.getParameter("checkKettenDiv");
            _countFlg = request.getParameter("COUNT_SURU");
            _scoreFlg = request.getParameter("SCORE_FLG");
            _dateS = request.getParameter("SDATE").replace('/', '-');
            _date = request.getParameter("DATE").replace('/', '-');
            _knjd192AcheckNoExamChair = request.getParameter("knjd192AcheckNoExamChair");
            _useKetten = !_isSapporo && !("1".equals(request.getParameter("useSchoolMstSemesAssesscd")) && "08".equals(_scorediv));
            _notUseKetten = "1".equals(request.getParameter("notUseKetten"));
            _useTotalAverage = !_isSapporo;

            _printForm20 = "2".equals(request.getParameter("SUBCLASS_MAX"));
            _printTestOnly = null != request.getParameter("TEST_ONLY");
            _notPrintRank = request.getParameter("NOT_PRINT_RANK");
            _notPrintAvg = request.getParameter("NOT_PRINT_AVG");
            _notPrintKekka = request.getParameter("NOT_PRINT_KEKKA");

            _useAbsenceWarn = "1".equals(request.getParameter("TYUI_TYOUKA"));
            _printOnedayAttend = "1"; //request.getParameter("ONEDAY_ATTEND");
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            setAttendSubclassSpecialMap(db2);

            _use_SchregNo_hyoji = request.getParameter("use_SchregNo_hyoji");
            _printSubclassLastChairStd = request.getParameter("printSubclassLastChairStd");

            _sortCutting = "1".equals(request.getParameter("SORT_CUTTING"));

            try {
                _knjSchoolMst = new KNJSchoolMst(db2, _year);
            } catch (Exception e) {
                log.fatal("exception!", e);
            }

            _warnSemester = setWarnSemester(db2);

            setD054Namecd2Max(db2);

            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);

            _isOutputDebug = "1".equals(getDbPrginfoProperties(db2, "outputDebug"));

            _kekkaAsInt = request.getParameter("KEKKA_AS_INT");
            _definecode = createDefineCode(db2);

            _nendo = KNJ_EditDate.getAutoFormatYear(db2, Integer.parseInt(_year)) + "年度";
        }

        private boolean isPrintKekkaFloat() {
            return !"1".equals(_kekkaAsInt) && (_definecode.absent_cov == 3 || _definecode.absent_cov == 4);
        }

        private DecimalFormat getAbsentFmt() {
            DecimalFormat absentFmt;
            switch (_definecode.absent_cov) {
            case 0:
            case 1:
            case 2:
                absentFmt = new DecimalFormat("0");
                break;
            default:
                absentFmt = new DecimalFormat("0.0");
            }
            return absentFmt;
        }

        /*
         *  クラス内で使用する定数設定
         */
        private KNJDefineSchool createDefineCode(final DB2UDB db2) {
            final KNJDefineSchool definecode = new KNJDefineSchool();
            definecode.defineCode(db2, _year);         //各学校における定数等設定
            log.debug("semesdiv=" + definecode.semesdiv + "   absent_cov=" + definecode.absent_cov + "   absent_cov_late=" + definecode.absent_cov_late);
            return definecode;
        }

        private static String getDbPrginfoProperties(final DB2UDB db2, final String propName) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJD192V' AND NAME = '" + propName + "' "));
        }

        private void setD054Namecd2Max(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                StringBuffer stb = new StringBuffer();
                stb.append(" SELECT NAMECD2, NAME1 FROM NAME_MST ");
                stb.append(" WHERE NAMECD1 = 'D054' AND NAMECD2 = (SELECT MAX(NAMECD2) AS NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'D054') ");
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    _d054Namecd2Max = rs.getString("NAMECD2");
                }
            } catch (SQLException ex) {
                log.debug("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
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

        private boolean isCountKetsu() {
            return null != _countFlg;
        }

        private String getTestName(final DB2UDB db2) {
        	return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT TESTITEMNAME FROM TESTITEM_MST_COUNTFLG_NEW_SDIV WHERE YEAR = '" + _year + "' AND SEMESTER || TESTKINDCD || TESTITEMCD || SCORE_DIV = '" + _testcd + "' "));
        }

        private void setSemesterName(final DB2UDB db2) {
        	_semesterName = KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT SEMESTERNAME FROM SEMESTER_MST WHERE YEAR = '" + _year + "' AND SEMESTER = '" + _semester + "' "));
        }

        private String setNameMst(final DB2UDB db2, final String namecd1, final String namecd2) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT NAME1 FROM V_NAME_MST WHERE  YEAR = '" + _year + "' AND NAMECD1 = '" + namecd1 + "' AND NAMECD2 = '" + namecd2 + "'"));
        }

        private String getRankField() {
            if ("1".equals(_groupDiv) || "3".equals(_groupDiv)) {
                return "GRADE_";
            } else if ("2".equals(_groupDiv) || "4".equals(_groupDiv)) {
                return "COURSE_";
            } else if ("5".equals(_groupDiv)) {
                return "MAJOR_";
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
            } else if ("5".equals(_groupDiv)) {
                return "学科";
            }
            return null;
        }

        private String getRankName2() {
            if ("1".equals(_groupDiv) || "2".equals(_groupDiv) || "5".equals(_groupDiv)) {
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
            } else if ("5".equals(_groupDiv)) {
                return "4";
            }
            return null;
        }

        private boolean isPrintChair() {
            return "3".equals(_groupDiv) || "4".equals(_groupDiv);
        }

        private void setAttendSubclassSpecialMap(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                String sql = "SELECT SPECIAL_GROUP_CD,";
                sql += " CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || ";
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
            } catch (Exception e) {
                log.fatal("exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        /** 欠点対象：RECORD_SLUMP_DATを参照して判断するか */
        private boolean isRecordSlump() {
            return true; //"1".equals(_checkKettenDiv) && (null == _ketten || "".equals(_ketten));
        }

        /** 欠点対象：指示画面の欠点を参照して判断するか */
        private boolean isKetten() {
            return !isRecordSlump();
        }

        private boolean isHoutei() {
            if ("9".equals(_semester) || null != _knjSchoolMst._semesterDiv && _knjSchoolMst._semesterDiv.equals(_semester)) {
                if ("3".equals(_knjSchoolMst._jugyouJisuFlg)) {
                    return true;
                }
            }
            return "1".equals(_knjSchoolMst._jugyouJisuFlg);
        }
    }
}

// eof
