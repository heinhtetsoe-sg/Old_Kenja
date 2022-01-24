// kanji=漢字
/*
 * $Id: 4f06c32080c8d76f4e98452e544a47816b89334e $
 *
 * 作成日: 2007/05/14
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJServletpacksvfANDdb2;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.Vrw32alpWrap;

/**
 * 成績一覧表（成績判定会議用）を印刷します。
 * 成績処理改訂版。
 * 成績判定会議用をBASEとし、従来の成績一覧表の成績処理改訂版の印刷処理を行います。
 * @author nakamoto
 * @version $Id: 4f06c32080c8d76f4e98452e544a47816b89334e $
 */
public class KNJD653G {
    private static final Log log = LogFactory.getLog(KNJD653G.class);

//    private KNJDefineSchool _definecode;  //各学校における定数等設定のクラス
//    private KNJSchoolMst _knjSchoolMst;

    private final DecimalFormat DEC_FMT1 = new DecimalFormat("0.0");
    private final DecimalFormat DEC_FMT2 = new DecimalFormat("0");

    private static final String SPECIAL_ALL = "999";
    private static final String subclassCdAll3 = "333333";
    private static final String subclassCdAll5 = "555555";
    private static final String subclassCdAll9 = "999999";
    private static final String OUTPUT_RANK_CLASS = "1";
    private static final String OUTPUT_RANK_GRADE = "2";
    private static final String OUTPUT_RANK_COURSE = "3";
    private static final String OUTPUT_RANK_COURSEGROUP = "5";

    /** フォーム（５０名）*/
    private final int formMaxLine = 50;

    private Param _param;

    /**
     *  KNJD.classから最初に起動されます。
     */
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws ServletException, IOException {
        log.fatal("$Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $"); // CVSキーワードの取り扱いに注意
        final KNJServletpacksvfANDdb2 sd = new KNJServletpacksvfANDdb2();  //帳票におけるＳＶＦおよびＤＢ２の設定

        DB2UDB db2 = null;
        Vrw32alpWrap svf = null;
        boolean hasData = false;
        try {
            // ＤＢ接続
            db2 = sd.setDb(request);
            if (sd.openDb(db2)) {
                log.error("db open error! ");
                return;
            }
            // パラメータの取得
            _param = createParam(request, db2);

//            _definecode = new KNJDefineSchool();
//            _definecode.defineCode(db2, _param._year);         //各学校における定数等設定
//            log.debug("semesdiv=" + _definecode.semesdiv + "   absent_cov=" + _definecode.absent_cov + "   absent_cov_late=" + _definecode.absent_cov_late);

//            try {
//                _knjSchoolMst = new KNJSchoolMst(db2, _param._year);
//            } catch (SQLException e) {
//                log.warn("学校マスタ取得でエラー", e);
//            }

            svf = new Vrw32alpWrap();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
            svf.setKurikaeshiNum(_param._formMaxColumn);
            svf.setFieldNum(formMaxLine);
            sd.setSvfInit(request, response, svf);

            final String[] hrclass = request.getParameterValues("CLASS_SELECTED");  //印刷対象HR組
            for (int h = 0; h < hrclass.length; h++) {

                if (_param._outputCourse) {
                    final List courses = Course.createCourses(db2, hrclass[h], _param);
                    log.debug("コース数=" + courses.size());

                    for (final Iterator it = courses.iterator(); it.hasNext();) {
                        final Course course = (Course) it.next();
                        final HRInfo hrInfo = new HRInfo(hrclass[h]);
                        hrInfo.load(db2, course._coursecd);

                        // 印刷処理
                        if (printMain(svf, hrInfo)) {
                            hasData = true;
                        }
                    }
                } else {
                    final HRInfo hrInfo = new HRInfo(hrclass[h]);
                    hrInfo.load(db2, null);

                    // 印刷処理
                    if (printMain(svf, hrInfo)) {
                        hasData = true;
                    }
                }
            }
        } catch (final Exception ex) {
            log.error("error! ", ex);
        } finally {
            if (null != db2) {
                sd.closeDb(db2);
            }
            if (null != svf) {
                sd.closeSvf(svf, hasData);
            }
        }
    }

    private static class Course {
        private final String _grade;
        private final String _hrclass;
        private final String _coursecd;
        private final String _name;

        Course(
                final String grade,
                final String hrclass,
                final String coursecd,
                final String name
        ) {
            _grade = grade;
            _hrclass = hrclass;
            _coursecd = coursecd;
            _name = name;
        }

        public String toString() {
            return _coursecd + ":" + _name;
        }
        
        private static List createCourses(final DB2UDB db2, final String gradeHrclass, final Param param) throws SQLException {
            final List rtn = new ArrayList();
            final String sql = sqlCourses(gradeHrclass, param);

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String grade = rs.getString("GRADE");
                    final String hrclass = rs.getString("HR_CLASS");
                    final String coursecd = rs.getString("COURSECD");
                    final String name = rs.getString("COURSECODENAME");

                    final Course course = new Course(
                            grade,
                            hrclass,
                            coursecd,
                            name
                    );

                    rtn.add(course);
                }
            } catch (final Exception ex) {
                log.error("コースのロードでエラー:" + sql, ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtn;
        }

        private static String sqlCourses(final String hrclass, final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     W1.GRADE, ");
            stb.append("     W1.HR_CLASS, ");
            stb.append("     W1.COURSECD || W1.MAJORCD || W1.COURSECODE as COURSECD, ");
            stb.append("     L1.COURSECODENAME ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT W1 ");
            stb.append("     LEFT JOIN COURSECODE_MST L1 ON L1.COURSECODE=W1.COURSECODE ");
            stb.append(" WHERE ");
            stb.append("     W1.YEAR = '" + param._year + "' AND ");
            if (!"9".equals(param._semester)) {
                stb.append(" W1.SEMESTER = '" + param._semester + "' AND ");
            } else {
                stb.append(" W1.SEMESTER = '" + param._ctrlSemester + "' AND ");
            }
            stb.append("     W1.GRADE || W1.HR_CLASS = '" + hrclass + "' ");
            stb.append(" GROUP BY ");
            stb.append("     W1.GRADE, ");
            stb.append("     W1.HR_CLASS, ");
            stb.append("     W1.COURSECD, ");
            stb.append("     W1.MAJORCD, ");
            stb.append("     W1.COURSECODE, ");
            stb.append("     L1.COURSECODENAME ");
            return stb.toString();
        }
    }

    private ScoreValue getTargetValue(ScoreDetail d) {
        return isInterTerm() ? d._score : d._patternAssess;
    }

    private boolean isInterTerm() {
        return _param._testKindCd.startsWith("01") || _param._testKindCd.startsWith("02");
    }

    private boolean isGakunenMatu() {
        return !(isInterTerm()) && "9".equals(_param._semester);
    }

    private boolean enablePrintFlg() {
        return false;
    }

    private static Map getMappedMap(final Map map, final String key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new TreeMap());
        }
        return (Map) map.get(key1);
    }

    private static List getMappedList(final Map map, final String key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new ArrayList());
        }
        return (List) map.get(key1);
    }

    /**
     * 学級ごとの印刷。
     * @param svf
     * @param hrInfo
     * @return
     */
    private boolean printMain(
            final Vrw32alpWrap svf,
            final HRInfo hrInfo
    ) {
        boolean hasData = false;
        boolean pageFlg = false;

        int first = -1;  // 生徒リストのインデックス
        int last = 0;  // 生徒リストのインデックス
        int page = 0;  // ページ
        for (final Iterator it = hrInfo._students.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();

            if (-1 == first) {
                first = hrInfo._students.indexOf(student);
                pageFlg = true;
            }
            last = hrInfo._students.indexOf(student);

            int nowpage = (int)Math.floor((double)student._gnum / (double) formMaxLine);
            if (page != nowpage) {
                page = nowpage;
                List list = hrInfo._students.subList(first, last + 1);
                first = last;
                pageFlg = false;

                if (printSub(svf, hrInfo, list)) {
                    hasData = true;
                }
            }
        }
        if (pageFlg && 0 < last) {
            List list = hrInfo._students.subList(first, last + 1);

            if (printSub(svf, hrInfo, list)) {
                hasData = true;
            }
        }

        return hasData;
    }

    /**
     * 学級ごと、生徒MAX行ごとの印刷。
     * @param svf
     * @param hrInfo：年組
     * @param stulist：List hrInfo._studentsのsublist
     * @return
     */
    private boolean printSub(
            final Vrw32alpWrap svf,
            final HRInfo hrInfo,
            final List students
    ) {
        boolean hasData = false;

        int line = 0;  // 科目の列番号
        int subclassesnum = 0;

        final List subClasses = new ArrayList(hrInfo._subclasses.values());

        Collections.sort(subClasses);
        for (int i = 0; i < subClasses.size(); i++) {
            log.debug(" " + i + " , " + subClasses.get(i));
        }

        for (final Iterator it = subClasses.iterator(); it.hasNext();) {
            final SubClass subclass = (SubClass) it.next();
            final boolean notOutputColumn = "90".equals(subclass._classcode) && _param._notOutputSougou;
            if (notOutputColumn || hrInfo.isNotTargetSubclassCd(subclass)) {
                continue;
            }
            subclassesnum += 1;
        }

        for (final Iterator it = subClasses.iterator(); it.hasNext();) {
            if (0 == line % _param._formMaxColumn) {
                printHeader(svf, hrInfo, students);
            }

            final SubClass subclass = (SubClass) it.next();
            final boolean notOutputColumn = "90".equals(subclass._classcode) && _param._notOutputSougou;
            if (notOutputColumn || hrInfo.isNotTargetSubclassCd(subclass)) {
                continue;
            }
            if (subclassesnum == line + 1) {
                for (final Iterator it1 = students.iterator(); it1.hasNext();) {
                    final Student student = (Student) it1.next();
                    if (!student.hasKesshi(hrInfo, null)) {
                        printTotal(svf, student);
                    }
                }
                printHRInfo(svf, hrInfo);
            }  // 生徒別総合成績および出欠を印字

            printSubclass(svf, subclass, line, hrInfo, students);
            svf.VrEndRecord();
            line += 1;
            hasData = true;
        }

        return hasData;
    }

    private void printHeader(final Vrw32alpWrap svf, final HRInfo hrInfo, final List students) {
        svf.VrSetForm(_param.getFormFile(), 4);  //SVF-FORM設定
        svf.VrsOut("YEAR2", _param.getNendo());
        svf.VrsOut("ymd1", KNJ_EditDate.h_format_JP(_param._ctrlDate));

        svf.VrsOut("TEACHER", hrInfo.getStaffName());  //担任名
        svf.VrsOut("HR_NAME", hrInfo._hrName);  //組名称

        svf.VrsOut("TITLE" , _param._semesterName + " " + _param._testItemName + " 成績一覧表");

        final String[] rankNameR = _param.getRankNameR();
        if (rankNameR.length > 1) {
            svf.VrsOut("ORDER2_11" , rankNameR[0]);
            svf.VrsOut("ORDER2_12" , rankNameR[1]);
            svf.VrsOut("ORDER2_13" , "順位");
        } else {
            svf.VrsOut("ORDER2"   , rankNameR[0]);
            svf.VrsOut("ORDER2_2" , "順位");
        }
        final String[] rankNameL = _param.getRankNameL();
        if (rankNameL.length > 1) {
            svf.VrsOut("ORDER12" , rankNameL[0]);
            svf.VrsOut("ORDER12_12" , rankNameL[1]);
            svf.VrsOut("ORDER12_13" , "順位");
        } else {
            svf.VrsOut("ORDER12" , rankNameL[0]);
            svf.VrsOut("ORDER12_2" , "順位");
        }
        for (final Iterator it1 = students.iterator(); it1.hasNext();) {
            final Student student = (Student) it1.next();
            svf.VrsOutn("name1", student._gnum, student._name);    // 氏名
            svf.VrsOutn("NUMBER", student._gnum, DEC_FMT2.format(Integer.parseInt(student._attendNo)));  // 出席番号
            svf.VrsOutn("sex", student._gnum, student._sexName);    // 性別
            svf.VrsOutn("COURSE", student._gnum, student._courseCodeName);    // コース
            svf.VrsOutn("DORMITORY", student._gnum, student.getResidentMark());    // 寮
        }  // 生徒名等を印字
    }

    /**
     * 学級データの印字処理(学級平均)
     * @param svf
     */
    void printHRInfo(
            final Vrw32alpWrap svf,
            final HRInfo hrInfo
    ) {
        final int line = formMaxLine + 3;
        if (null != hrInfo._hrAverageScore3) {
            final String idx = "1";
            svf.VrsOutn("TOTAL" + idx, line, hrInfo._hrAverageScore3);  //総合点
            svf.VrsOutn("AVERAGE" + idx, line, hrInfo._hrAverageAvg3);  //平均点
            log.debug("SCORE3 = " + hrInfo._hrAverageScore3 + ", AVERAGE3 = " + hrInfo._hrAverageAvg3);
        }
        if (null != hrInfo._hrAverageScore5) {
            final String idx = "2";
            svf.VrsOutn("TOTAL" + idx, line, hrInfo._hrAverageScore5);  //総合点
            svf.VrsOutn("AVERAGE" + idx, line, hrInfo._hrAverageAvg5);  //平均点
            log.debug("SCORE5 = " + hrInfo._hrAverageScore5 + ", AVERAGE5 = " + hrInfo._hrAverageAvg5);
        }
        if (null != hrInfo._hrAverageScore9) {
            final String idx = "3";
            svf.VrsOutn("TOTAL" + idx, line, hrInfo._hrAverageScore9);  //総合点
            svf.VrsOutn("AVERAGE" + idx, line, hrInfo._hrAverageAvg9);  //平均点
            log.debug("SCORE9 = " + hrInfo._hrAverageScore9 + ", AVERAGE9 = " + hrInfo._hrAverageAvg9);
        }
    }

    /**
     * 生徒別総合点・平均点・順位を印刷します。
     * @param svf
     * @param gnum 行番号(印字位置)
     */
    void printTotal(
            final Vrw32alpWrap svf,
            final Student student
    ) {
        final int gnum = student._gnum;
        if (student._recordRankDat.get(subclassCdAll3) != null) {
            final RecordRankDat rrd = (RecordRankDat) student._recordRankDat.get(subclassCdAll3);
            final String idx = "1";
            svf.VrsOutn("TOTAL" + idx, gnum, rrd._score);  //総合点
            svf.VrsOutn("AVERAGE" + idx, gnum, rrd._avg);  //平均点
            svf.VrsOutn("RANK" + idx + "_1", gnum, rrd.getRank1());  //クラス順位
            svf.VrsOutn("RANK" + idx + "_2", gnum, rrd.getRank2());  //順位
        }
        if (student._recordRankDat.get(subclassCdAll5) != null) {
            final RecordRankDat rrd = (RecordRankDat) student._recordRankDat.get(subclassCdAll5);
            final String idx = "2";
            svf.VrsOutn("TOTAL" + idx, gnum, rrd._score);  //総合点
            svf.VrsOutn("AVERAGE" + idx, gnum, rrd._avg);  //平均点
            svf.VrsOutn("RANK" + idx + "_1", gnum, rrd.getRank1());  //クラス順位
            svf.VrsOutn("RANK" + idx + "_2", gnum, rrd.getRank2());  //順位
        }
        if (student._recordRankDat.get(subclassCdAll9) != null) {
            final RecordRankDat rrd = (RecordRankDat) student._recordRankDat.get(subclassCdAll9);
            final String idx = "3";
            svf.VrsOutn("TOTAL" + idx, gnum, rrd._score);  //総合点
            svf.VrsOutn("AVERAGE" + idx, gnum, rrd._avg);  //平均点
            svf.VrsOutn("RANK" + idx + "_1", gnum, rrd.getRank1());  //クラス順位
            svf.VrsOutn("RANK" + idx + "_2", gnum, rrd.getRank2());  //順位
        }
    }

    /**
     * 該当科目名および科目別成績等を印字する処理
     * @param svf
     * @param subclass
     * @param line：科目の列番号
     * @param stulist：List hrInfo._studentsのsublist
     * @return
     */
    private void printSubclass(
            final Vrw32alpWrap svf,
            final SubClass subclass,
            final int line,
            final HRInfo hrInfo,
            final List stulist
    ) {
        printSubclass(svf, subclass, line);  // 該当科目名等を印字
        for (final Iterator it = stulist.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            if (getMappedList(hrInfo._courseNotTargetSubclassCdMap, student.getCourse()).contains(subclass.getSubclassCd())) {
                continue;
            }

            if (student.hasKesshi(hrInfo, subclass.getSubclassCd())) {
                continue;
            }

            if (student._scoreDetails.containsKey(subclass._subclasscode)) {
              final ScoreDetail detail = (ScoreDetail) student._scoreDetails.get(subclass._subclasscode);
              final ScoreValue score = getTargetValue(detail);

              if (null != score) {
                  if (_param._creditDrop && isGakunenMatu() && score.hasIntValue() && score.getScoreAsInt() == 1) {
                      svf.VrsOutn("SCORE", student._gnum,  "*" + score.getScore());
                  } else {
                      svf.VrsOutn("SCORE", student._gnum,  score.getScore());
                  }
              }
            }
        }  // 生徒別該当科目成績を印字する処理


        final String key = subclass._subclasscode.substring(1) + "2" + hrInfo._hrclassCd + "0" + "000" + "0000";
        final RecordAverageDat rad = (RecordAverageDat) hrInfo._recordAverageDat.get(key);
        if (rad != null) {
            svf.VrsOutn("SCORE", formMaxLine + 1, rad._count);
            svf.VrsOutn("SCORE", formMaxLine + 2, rad._score);
            svf.VrsOutn("SCORE", formMaxLine + 3, rad._avg);
        }
    }

    /**
     * 科目項目(教科名・科目名・)を印刷します。
     * @param svf
     * @param line 科目の列番
     */
    private void printSubclass(
            final Vrw32alpWrap svf,
            final SubClass subclass,
            int line
    ) {
        int i = ((line + 1) % _param._formMaxColumn == 0)? _param._formMaxColumn: (line + 1) % _param._formMaxColumn;
        if (log.isDebugEnabled()) {
            log.debug("subclassname=" + subclass._subclasscode + " " + subclass._subclassabbv + "   line=" + line + " i="+i);
        }
        //教科名
        svf.VrsOut("course1", subclass._classabbv);
        //科目名
        if (subclass._electdiv || subclass._saki) {
            svf.VrAttribute("subject1", "Paint=(2,70,2),Bold=1");
        }
        if (subclass._subclassabbv != null) {
            final String field;
            if (subclass._subclassabbv.length() > 4) {
                field = "SUBJECT3_1";
            } else if (subclass._subclassabbv.length() > 2) {
                field = "SUBJECT2_1";
            } else {
                field = "SUBJECT1";
            }
            svf.VrsOut(field, subclass._subclassabbv);
        }
        if (subclass._electdiv) {
            svf.VrAttribute("subject1", "Paint=(0,0,0),Bold=0");
        }

    }

    private static double round10(final int a, final int b) {
        return Math.round(a * 10.0 / b) / 10.0;
    }

    private ScoreDetail createScoreDetail(
            final ResultSet rs,
            Map subclasses
    ) throws SQLException {

        final String subclassCd;
        if ("1".equals(_param._useCurriculumcd)) {
            subclassCd = rs.getString("SUBCLASSCD").substring(0, 1) + rs.getString("CLASSCD") + "-" + rs.getString("SCHOOL_KIND") + "-" + rs.getString("CURRICULUM_CD") + "-" + rs.getString("SUBCLASSCD").substring(1);
        } else {
            subclassCd = rs.getString("SUBCLASSCD");
        }
        SubClass subclass = null;

        if (!subclasses.containsKey(subclassCd)) {
            String classabbv = rs.getString("CLASSNAME");
            Integer classmstShoworder4 = Integer.valueOf(rs.getString("SHOWORDER4"));
            String subclassabbv = rs.getString("SUBCLASSNAME");
            boolean electdiv = "1".equals(rs.getString("ELECTDIV"));
            boolean saki = false;
            try {
                saki = "-1".equals(rs.getString("REPLACEMOTO"));
            } catch (SQLException e) {
                 log.error("SQLException", e);
            }

            subclass = new SubClass(subclassCd, classabbv, classmstShoworder4, subclassabbv, electdiv, saki);
            subclasses.put(subclassCd, subclass);
        }
        subclass = (SubClass) subclasses.get(subclassCd);

        ScoreValue score = null;
        ScoreValue patternAssess = null;
        if (!(null == rs.getString("SCORE") || KNJDefineSchool.subject_T.equals(subclass._classcode))) {
            score = new ScoreValue(rs.getString("SCORE"));
        }
        if (!(null == rs.getString("PATTERN_ASSESS") || KNJDefineSchool.subject_T.equals(subclass._classcode))) {
            patternAssess = new ScoreValue(rs.getString("PATTERN_ASSESS"));
        }

        final ScoreDetail detail = new ScoreDetail(
                subclass,
                score,
                patternAssess,
                (Integer) rs.getObject("REPLACEMOTO"),
                (String) rs.getObject("PRINT_FLG"),
                rs.getString("VALUE_DI")
        );
        return detail;
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * <<学級のクラス>>。
     */
    private class HRInfo implements Comparable {
        private final String _hrclassCd;
        private String _staffName1;
        private String _staffName2;
        private String _staffName3;
        private String _hrName;
        private final List _students = new LinkedList();
        private final Map _subclasses = new TreeMap();
        private String _hrAverageScore3;
        private String _hrAverageAvg3;
        private String _hrAverageScore5;
        private String _hrAverageAvg5;
        private String _hrAverageScore9;
        private String _hrAverageAvg9;
        private final Map _recordAverageDat = new HashMap();
        private Map _courseNotTargetSubclassCdMap;

        HRInfo(final String hrclassCd) {
            _hrclassCd = hrclassCd;
        }

        String getCode() { return _hrclassCd; }

        void load(
                final DB2UDB db2,
                final String courseCd
        ) throws Exception {
            loadStudents(db2, courseCd);
            loadStudentsInfo(db2);
            loadScoreDetail(db2);
            loadHrStaff(db2);
            setRecordRankDat(db2);
            setRecordAverageDat(db2);
            setHrAverage();
            _courseNotTargetSubclassCdMap = getCourseNotTargetSubclassCdMap(db2, courseCd);
        }

        private Map getCourseNotTargetSubclassCdMap(final DB2UDB db2, final String courseCd) {
            final Map courseNotTargetSubclassCdListMap = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            final String flg = "9900".equals(_param._testKindCd) ? "2" : "1";
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append("   SELECT ");
                stb.append("     T1.COURSECD || T1.MAJORCD || T1.COURSECODE AS COURSE, ");
                if ("1".equals(_param._useCurriculumcd)) {
                    stb.append("       T1.ATTEND_CLASSCD || '-' || T1.ATTEND_SCHOOL_KIND || '-' || T1.ATTEND_CURRICULUM_CD || '-' || ");
                }
                stb.append("       T1.ATTEND_SUBCLASSCD AS ATTEND_SUBCLASSCD, ");
                if ("1".equals(_param._useCurriculumcd)) {
                    stb.append("       T1.COMBINED_CLASSCD || '-' || T1.COMBINED_SCHOOL_KIND || '-' || T1.COMBINED_CURRICULUM_CD || '-' || ");
                }
                stb.append("       T1.COMBINED_SUBCLASSCD AS COMBINED_SUBCLASSCD ");
                stb.append("   FROM ");
                stb.append("       SUBCLASS_WEIGHTING_COURSE_DAT T1 ");
                stb.append("   WHERE ");
                stb.append("       T1.YEAR = '" + _param._year + "' ");
                stb.append("       AND T1.FLG = '" + flg + "' ");
                stb.append("       AND T1.GRADE = '" + _param._grade + "' ");

                if (null != courseCd) {
                    stb.append("     AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE = '" + courseCd + "' ");
                } else {
                    stb.append("     AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE IN ( ");
                    stb.append("        SELECT DISTINCT ");
                    stb.append("            T3.COURSECD || T3.MAJORCD || T3.COURSECODE ");
                    stb.append("        FROM ");
                    stb.append("            SCHREG_REGD_DAT T3 ");
                    stb.append("        WHERE ");
                    stb.append("            T3.YEAR = '" + _param._year + "' ");
                    if (!"9".equals(_param._semester)) {
                        stb.append(" AND T3.SEMESTER = '" + _param._semester + "' ");
                    } else {
                        stb.append(" AND T3.SEMESTER = '" + _param._ctrlSemester + "' ");
                    }
                    stb.append("            AND T3.GRADE || T3.HR_CLASS = '" + _hrclassCd + "' ");
                    stb.append("     ) ");
                }

                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    if ("9900".equals(_param._testKindCd)) {
                        // getMappedList(courseNotTargetSubclassCdListMap, rs.getString("COURSE")).add(rs.getString("ATTEND_SUBCLASSCD"));
                    } else {
                        // getMappedList(courseNotTargetSubclassCdListMap, rs.getString("COURSE")).add(rs.getString("COMBINED_SUBCLASSCD"));
                    }
                    final List list = getMappedList(getMappedMap(getMappedMap(courseNotTargetSubclassCdListMap, "PAIR"), rs.getString("COURSE")), rs.getString("COMBINED_SUBCLASSCD"));
                    if (!list.contains(rs.getString("ATTEND_SUBCLASSCD"))) {
                        list.add(rs.getString("ATTEND_SUBCLASSCD"));
                    }
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return courseNotTargetSubclassCdListMap;
        }

        private boolean isNotTargetSubclassCd(final SubClass subclass) {
            if (null == subclass || null == subclass.getSubclassCd()) {
                return false;
            }
            final Set courses = new HashSet();
            for (final Iterator it = _students.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                if (null == student.getCourse()) {
                    continue;
                }
                for (final Iterator its = student._scoreDetails.values().iterator(); its.hasNext();) {
                    final ScoreDetail sd = (ScoreDetail) its.next();
                    if (sd._subClass == subclass) {
                        courses.add(student.getCourse());
                    }
                }
            }
            boolean isNotTarget = true;
            for (final Iterator itc = courses.iterator(); itc.hasNext();) {
                final String course = (String) itc.next();
                if ("PAIR".equals(course)) {
                    continue;
                }
                List notSubclassCdList = (List) _courseNotTargetSubclassCdMap.get(course);
                if (null == notSubclassCdList) {
                    notSubclassCdList = Collections.EMPTY_LIST;
                }
                if (!notSubclassCdList.contains(subclass.getSubclassCd())) {
                    isNotTarget = false;
                    break;
                }
            }
            return isNotTarget;
        }
        
        private Map getCombinedAttendSubclassMap(final String course) {
            return getMappedMap(getMappedMap(_courseNotTargetSubclassCdMap, "PAIR"), course);
        }

        private List getAttendSubclassList(final String course, final String combiendSubclasscd) {
            return getMappedList(getCombinedAttendSubclassMap(course), combiendSubclasscd);
        }

        private List getNotTargetSubclassList(final String course) {
            return getMappedList(_courseNotTargetSubclassCdMap, course);
        }
        
        /**
         * SQL HR組の学籍番号を取得するSQL
         */
        private String sqlHrclassStdList(final String hrClass, final String courseCd) {
            final StringBuffer stb = new StringBuffer();

            stb.append("SELECT  W1.SCHREGNO,W1.ATTENDNO,W1.COURSECD,W1.MAJORCD,W1.GRADE,W1.COURSECODE ");
            stb.append("FROM    SCHREG_REGD_DAT W1 ");
            stb.append("INNER  JOIN SCHREG_BASE_MST W3 ON W3.SCHREGNO = W1.SCHREGNO ");
            stb.append("WHERE   W1.YEAR = '" + _param._year + "' ");
            if (!"9".equals(_param._semester)) {
                stb.append(    "AND W1.SEMESTER = '" + _param._semester + "' ");
            } else {
                stb.append(    "AND W1.SEMESTER = '" + _param._ctrlSemester + "' ");
            }
            stb.append(    "AND W1.GRADE||W1.HR_CLASS = '" + hrClass + "' ");
            if (_param._outputCourse) {
                stb.append(         "AND W1.COURSECD || W1.MAJORCD || W1.COURSECODE = '" + courseCd + "' ");
            }
            stb.append("ORDER BY W1.ATTENDNO");

            return stb.toString();
        }

        private void loadStudents(
                final DB2UDB db2,
                final String courseCd
        ) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = sqlHrclassStdList(_hrclassCd, courseCd);
                ps = db2.prepareStatement(sql);

                rs = ps.executeQuery();
                int gnum = 0;
                while (rs.next()) {
                    if (_param._noKetuban) {
                        gnum++;
                    } else {
                        gnum = rs.getInt("ATTENDNO");
                    }
                    final Student student = new Student(rs.getString("SCHREGNO"), this, rs.getString("COURSECD"), rs.getString("MAJORCD"), rs.getString("GRADE"), rs.getString("COURSECODE"), gnum);
                    _students.add(student);
                }
            } catch (SQLException e) {
                log.error("SQLException", e);
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        /**
         * SQL 任意の生徒の学籍情報を取得するSQL
         *   SEMESTER_MSTは'指示画面指定学期'で検索 => 学年末'9'有り
         *   SCHREG_REGD_DATは'指示画面指定学期'で検索 => 学年末はSCHREG_REGD_HDATの最大学期
         */
        private String sqlStdNameInfo(String hrclass) {
            final StringBuffer stb = new StringBuffer();
            stb.append("SELECT  W1.SCHREGNO, W1.ATTENDNO, W3.NAME, W6.HR_NAME, W9.RESIDENTCD, ");
            stb.append(        "(CASE WHEN W3.SEX = '1' THEN '' ELSE '　' END) || W7.NAME2 AS SEX, ");
            stb.append(        "W8.COURSECODENAME, ");
            stb.append(        "CASE WHEN W4.GRD_DATE IS NOT NULL THEN W4.GRD_DATE ELSE W4.ENT_DATE END AS KBN_DATE1, ");
            stb.append(        "CASE WHEN W4.GRD_DATE IS NOT NULL THEN (SELECT NAME1 FROM NAME_MST S1 WHERE S1.NAMECD1 = 'A003' AND S1.NAMECD2 = W4.GRD_DIV) ");
            stb.append(             "ELSE (SELECT NAME1 FROM NAME_MST S1 WHERE S1.NAMECD1 = 'A002' AND S1.NAMECD2 = W4.ENT_DIV) END AS KBN_NAME1, ");
            stb.append(        "W5.TRANSFER_SDATE AS KBN_DATE2,");
            stb.append(        "(SELECT NAME1 FROM NAME_MST S1 WHERE S1.NAMECD1='A004' AND S1.NAMECD2 = W5.TRANSFERCD) AS KBN_NAME2 ");
            stb.append("FROM    SCHREG_REGD_DAT W1 ");
            stb.append("INNER  JOIN SCHREG_REGD_HDAT  W6 ON W6.YEAR = '" + _param._year + "' AND W6.SEMESTER = W1.SEMESTER AND W6.GRADE = W1.GRADE AND W6.HR_CLASS = W1.HR_CLASS ");
            stb.append("INNER  JOIN SEMESTER_MST    W2 ON W2.YEAR = '" + _param._year + "' AND W2.SEMESTER = '" + _param._semester + "' ");
            stb.append("INNER  JOIN SCHREG_BASE_MST W3 ON W3.SCHREGNO = W1.SCHREGNO ");
            stb.append("LEFT   JOIN SCHREG_BASE_MST W4 ON W4.SCHREGNO = W1.SCHREGNO ");
            stb.append(                              "AND ((W4.GRD_DIV IN('2','3') AND W4.GRD_DATE < W2.EDATE) ");
            stb.append(                                "OR (W4.ENT_DIV IN('4','5') AND W4.ENT_DATE > W2.SDATE)) ");
            stb.append("LEFT   JOIN SCHREG_TRANSFER_DAT W5 ON W5.SCHREGNO = W1.SCHREGNO ");
            stb.append(                                  "AND (W5.TRANSFERCD IN ('1','2') AND W2.EDATE BETWEEN W5.TRANSFER_SDATE AND W5.TRANSFER_EDATE) ");
            stb.append("LEFT   JOIN NAME_MST W7 ON W7.NAMECD1 = 'Z002' AND W7.NAMECD2 = W3.SEX ");
            stb.append("LEFT   JOIN COURSECODE_MST W8 ON W8.COURSECODE = W1.COURSECODE ");
            stb.append("LEFT   JOIN SCHREG_ENVIR_DAT W9 ON W9.SCHREGNO = W1.SCHREGNO ");
            stb.append("WHERE   W1.YEAR = '" + _param._year + "' ");
            if (!_param._semester.equals("9")) {
                stb.append("AND W1.SEMESTER = '" + _param._semester + "' ");
            } else {
                stb.append("AND W1.SEMESTER = '" + _param._ctrlSemester + "' ");
            }
            stb.append("AND W1.GRADE || W1.HR_CLASS = '" + hrclass + "' ");
            return stb.toString();
        }

        private void loadStudentsInfo(
                final DB2UDB db2
        ) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = sqlStdNameInfo(getCode());
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {

                    String schregno = rs.getString("SCHREGNO");
                    Student student = getStudent(schregno);
                    if (student == null) {
                        continue;
                    }
                    student._attendNo = rs.getString("ATTENDNO");
                    student._name = rs.getString("NAME");
                    student._sexName = rs.getString("SEX");
                    student._courseCodeName = rs.getString("COURSECODENAME");
                    student._residentCd = rs.getString("RESIDENTCD");
                }

            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }

        private Student getStudent(String schregno) {
            if (schregno == null) {
                return null;
            }
            for (final Iterator it = _students.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                if (schregno.equals(student._code)) {
                    return student;
                }
            }
            return null;
        }

        /**
         *  PrepareStatement作成 --> 成績・評定・欠課データの表
         */
        private String sqlStdSubclassDetail(String hrclassCd) {
            final StringBuffer stb = new StringBuffer();

            stb.append("WITH ");

            //対象生徒の表 クラスの生徒
            stb.append(" SCHNO_A AS(");
            stb.append("     SELECT  W1.SCHREGNO,W1.SEMESTER ");
            stb.append("            ,W1.GRADE,W1.COURSECD, W1.MAJORCD, W1.COURSECODE ");
            stb.append("            ,CASE WHEN W3.SCHREGNO IS NOT NULL THEN 1 ");
            stb.append("                  WHEN W4.SCHREGNO IS NOT NULL THEN 1 ");
            stb.append("                  WHEN W5.SCHREGNO IS NOT NULL THEN 1 ");
            stb.append("                  ELSE 0 END AS LEAVE ");
            stb.append("     FROM    SCHREG_REGD_DAT W1 ");
            stb.append("     INNER JOIN SEMESTER_MST W2 ON W2.YEAR = '" + _param._year + "' AND W2.SEMESTER = W1.SEMESTER ");
            //               転学・退学者で、異動日が[学期終了日または出欠集計日の小さい日付]より小さい日付の場合
            stb.append("     LEFT JOIN SCHREG_BASE_MST W3 ON W3.SCHREGNO = W1.SCHREGNO ");
            stb.append("                  AND W3.GRD_DIV IN('2','3') ");
            stb.append("                  AND W3.GRD_DATE < CASE WHEN W2.EDATE < '" + _param._ctrlDate + "' THEN W2.EDATE ELSE '" + _param._ctrlDate + "' END ");
            //               転入・編入者で、異動日が[学期終了日または出欠集計日の小さい日付]より大きい日付の場合
            stb.append("     LEFT JOIN SCHREG_BASE_MST W4 ON W4.SCHREGNO = W1.SCHREGNO ");
            stb.append("                  AND W4.ENT_DIV IN('4','5') ");
            stb.append("                  AND W4.ENT_DATE > CASE WHEN W2.EDATE < '" + _param._ctrlDate + "' THEN W2.EDATE ELSE '" + _param._ctrlDate + "' END ");
            //               留学・休学者で、[学期終了日または出欠集計日の小さい日付]が留学・休学期間内にある場合
            stb.append("     LEFT JOIN SCHREG_TRANSFER_DAT W5 ON W5.SCHREGNO = W1.SCHREGNO ");
            stb.append("                  AND W5.TRANSFERCD IN ('1','2') ");
            stb.append("                  AND CASE WHEN W2.EDATE < '" + _param._ctrlDate + "' THEN W2.EDATE ELSE '" + _param._ctrlDate + "' END BETWEEN W5.TRANSFER_SDATE AND W5.TRANSFER_EDATE ");

            stb.append("     WHERE   W1.YEAR = '" + _param._year + "' ");
            if ("9".equals(_param._semester)) {
                stb.append( "    AND W1.SEMESTER = '" + _param._ctrlSemester + "' ");
            } else {
                stb.append("     AND W1.SEMESTER = '" + _param._semester + "' ");
                stb.append("     AND NOT EXISTS(SELECT 'X'  FROM  SCHREG_BASE_MST S1");
                stb.append("                    WHERE S1.SCHREGNO = W1.SCHREGNO AND S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < W2.SDATE) ");
            }
            stb.append("         AND W1.GRADE || W1.HR_CLASS = '" + hrclassCd + "' ");
            stb.append(") ");

            //対象講座の表
            stb.append(",CHAIR_A AS(");
            stb.append(     "SELECT W1.SCHREGNO, W2.CHAIRCD, W2.SUBCLASSCD, W2.SEMESTER, W1.APPDATE, W1.APPENDDATE ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(     " , W2.CLASSCD, W2.SCHOOL_KIND, W2.CURRICULUM_CD ");
            }
            stb.append(     "FROM   CHAIR_STD_DAT W1, CHAIR_DAT W2 ");
            stb.append(     "WHERE  W1.YEAR = '" + _param._year + "' ");
            stb.append(        "AND W2.YEAR = '" + _param._year + "' ");
            stb.append(        "AND W1.SEMESTER = W2.SEMESTER ");
            stb.append(        "AND W1.SEMESTER <= '" + _param._semester + "' ");
            stb.append(        "AND W2.SEMESTER <= '" + _param._semester + "' ");
            stb.append(        "AND W1.CHAIRCD = W2.CHAIRCD ");
            stb.append(        "AND EXISTS(SELECT 'X' FROM SCHNO_A W3 WHERE W3.SCHREGNO = W1.SCHREGNO)");
            stb.append(     ")");

            stb.append(",CREDITS_A AS(");
            stb.append(    "SELECT  SCHREGNO, SUBCLASSCD, CREDITS ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(     " , CLASSCD, SCHOOL_KIND, CURRICULUM_CD ");
            }
            stb.append(    "FROM    CREDIT_MST T1, SCHNO_A T2 ");
            stb.append(    "WHERE   T1.YEAR = '" + _param._year + "' ");
            stb.append(        "AND T1.GRADE = T2.GRADE ");
            stb.append(        "AND T1.COURSECD = T2.COURSECD ");
            stb.append(        "AND T1.MAJORCD = T2.MAJORCD ");
            stb.append(        "AND T1.COURSECODE = T2.COURSECODE ");
            stb.append(        "AND EXISTS(SELECT 'X' FROM CHAIR_A T3 WHERE T3.SUBCLASSCD = T1.SUBCLASSCD AND T3.SCHREGNO = T2.SCHREGNO)");
            stb.append(") ");

            // 単位数の表
            stb.append(",CREDITS_B AS(");
            stb.append("    SELECT  T1.SCHREGNO, T1.SUBCLASSCD, T1.CREDITS");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(     " , CLASSCD, SCHOOL_KIND, CURRICULUM_CD ");
            }
            stb.append("    FROM    CREDITS_A T1");
            stb.append("    WHERE   NOT EXISTS(SELECT 'X' FROM SUBCLASS_REPLACE_COMBINED_DAT T2 ");
            stb.append("                       WHERE  T2.YEAR = '" + _param._year + "' ");
            stb.append("                          AND T2.CALCULATE_CREDIT_FLG = '2'");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("                          AND T2.COMBINED_CLASSCD = T1.CLASSCD ");
                stb.append("                          AND T2.COMBINED_SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("                          AND T2.COMBINED_CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append("                          AND T2.COMBINED_SUBCLASSCD = T1.SUBCLASSCD)");
            stb.append("    UNION SELECT T1.SCHREGNO, COMBINED_SUBCLASSCD AS SUBCLASSCD, SUM(T1.CREDITS) AS CREDITS");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(     " , COMBINED_CLASSCD AS CLASSCD, COMBINED_SCHOOL_KIND AS SCHOOL_KIND, COMBINED_CURRICULUM_CD AS CURRICULUM_CD ");
            }
            stb.append("    FROM    CREDITS_A T1, SUBCLASS_REPLACE_COMBINED_DAT T2 ");
            stb.append("    WHERE   T2.YEAR = '" + _param._year + "' ");
            stb.append("        AND T2.CALCULATE_CREDIT_FLG = '2'");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("                          AND T2.ATTEND_CLASSCD = T1.CLASSCD ");
                stb.append("                          AND T2.ATTEND_SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("                          AND T2.ATTEND_CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append("        AND T2.ATTEND_SUBCLASSCD = T1.SUBCLASSCD");
            stb.append("    GROUP BY SCHREGNO, COMBINED_SUBCLASSCD");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(     " , COMBINED_CLASSCD, COMBINED_SCHOOL_KIND, COMBINED_CURRICULUM_CD ");
            }
            stb.append(") ");

            //成績データの表（通常科目）
            stb.append(",RECORD_REC AS(");
            stb.append(    "SELECT  W3.SCHREGNO, W3.SUBCLASSCD ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(     " , W3.CLASSCD, W3.SCHOOL_KIND, W3.CURRICULUM_CD ");
            }
            if (_param._testKindCd.equals("0101") || _param._testKindCd.equals("0201") || _param._testKindCd.equals("0202")) {
                //中間・期末成績
                stb.append(       ",CASE WHEN SCORE IS NOT NULL THEN RTRIM(CHAR(SCORE)) ");
                stb.append(             "ELSE NULL END AS SCORE ");
                stb.append(       ",'' AS PATTERN_ASSESS ");
            } else {
                //学期・学年成績
                stb.append(       ",'' AS SCORE ");
                stb.append(       ",CASE WHEN SCORE IS NOT NULL THEN RTRIM(CHAR(SCORE)) ");
                stb.append(             "ELSE NULL END AS PATTERN_ASSESS ");
            }
            stb.append(    "FROM    " + _param._rankTable + " W3 ");
            stb.append(    "WHERE   W3.YEAR = '" + _param._year + "' AND ");
            stb.append("            W3.SEMESTER = '" + _param._semester + "' AND ");
            stb.append("            W3.TESTKINDCD || W3.TESTITEMCD = '" + _param._testKindCd + "' AND ");
            stb.append(            "EXISTS(SELECT 'X' FROM SCHNO_A W1 ");
            stb.append(                   "WHERE W3.SCHREGNO = W1.SCHREGNO AND W1.LEAVE = 0) ");
            stb.append(     ") ");

            //成績データの表（通常科目）
            stb.append(",RECORD_SCORE AS(");
            stb.append(    "SELECT  W3.SCHREGNO, W3.SUBCLASSCD ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(     " , W3.CLASSCD, W3.SCHOOL_KIND, W3.CURRICULUM_CD ");
            }
            stb.append(           ",W3.COMP_CREDIT ,W3.GET_CREDIT, W3.VALUE_DI ");
            stb.append(           ",CASE WHEN W3.VALUE IS NOT NULL THEN RTRIM(CHAR(W3.VALUE)) ELSE NULL END AS PATTERN_ASSESS ");
            stb.append(    "FROM    RECORD_SCORE_DAT W3 ");
            stb.append(    "WHERE   W3.YEAR = '" + _param._year + "' AND ");
            stb.append("            W3.SEMESTER = '" + _param._semester + "' AND ");
            stb.append("            W3.TESTKINDCD || W3.TESTITEMCD = '" + _param._testKindCd + "' AND ");
            stb.append("            W3.SCORE_DIV = '00' AND ");
            stb.append(            "EXISTS(SELECT 'X' FROM SCHNO_A W1 ");
            stb.append(                   "WHERE W3.SCHREGNO = W1.SCHREGNO AND W1.LEAVE = 0) ");
            stb.append(     ") ");

            //メイン表
            stb.append(" SELECT  value(T7.ELECTDIV,'0') || T1.SUBCLASSCD AS SUBCLASSCD,T1.SCHREGNO ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(     " , T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD ");
            }
            stb.append("        ,T3.SCORE AS SCORE ");
            stb.append("        ,T3.PATTERN_ASSESS ");
            stb.append("        ,T33.COMP_CREDIT ");
            stb.append("        ,T33.GET_CREDIT ");
            stb.append("        ,T33.VALUE_DI ");
            stb.append("        ,T11.CREDITS ");
            stb.append("        ,T7.SUBCLASSABBV AS SUBCLASSNAME ");
            stb.append("        ,T8.CLASSABBV AS CLASSNAME ");
            stb.append("        ,VALUE(T8.SHOWORDER4, 999) AS SHOWORDER4 ");
            stb.append("        ,T7.ELECTDIV ");
            stb.append("        ,CASE WHEN T9.SUBCLASSCD IS NOT NULL THEN -1");
            stb.append("              WHEN T10.SUBCLASSCD IS NOT NULL THEN 1 ELSE 0 END AS REPLACEMOTO ");
            stb.append("        ,VALUE(T10.PRINT_FLG,'0') AS PRINT_FLG");
            //対象生徒・講座の表
            stb.append(" FROM(");
            stb.append("     SELECT  W2.SCHREGNO,W2.SUBCLASSCD");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(     " , W2.CLASSCD, W2.SCHOOL_KIND, W2.CURRICULUM_CD ");
            }
            stb.append("     FROM    CHAIR_A W2");
            if (!"9".equals(_param._semester)) {
                stb.append(" WHERE   W2.SEMESTER = '" + _param._semester + "'");
            }
            stb.append("     GROUP BY W2.SCHREGNO,W2.SUBCLASSCD");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(     " , W2.CLASSCD, W2.SCHOOL_KIND, W2.CURRICULUM_CD ");
            }
            stb.append(" )T1 ");
            //成績の表
            stb.append(" LEFT JOIN(");
            stb.append("   SELECT W3.SCHREGNO,W3.SUBCLASSCD,W3.SCORE,W3.PATTERN_ASSESS");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(     " , W3.CLASSCD, W3.SCHOOL_KIND, W3.CURRICULUM_CD ");
            }
            stb.append("   FROM   RECORD_REC W3");
            stb.append(" )T3 ON T3.SUBCLASSCD = T1.SUBCLASSCD AND T3.SCHREGNO = T1.SCHREGNO");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(     " AND T3.CLASSCD = T1.CLASSCD AND T3.SCHOOL_KIND = T1.SCHOOL_KIND AND T3.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append(" LEFT JOIN(");
            stb.append("   SELECT W3.SCHREGNO,W3.SUBCLASSCD,COMP_CREDIT,GET_CREDIT,PATTERN_ASSESS,VALUE_DI");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(     " , W3.CLASSCD, W3.SCHOOL_KIND, W3.CURRICULUM_CD ");
            }
            stb.append("   FROM   RECORD_SCORE W3");
            stb.append(" )T33 ON T33.SUBCLASSCD = T1.SUBCLASSCD AND T33.SCHREGNO = T1.SCHREGNO");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(     " AND T33.CLASSCD = T1.CLASSCD AND T33.SCHOOL_KIND = T1.SCHOOL_KIND AND T33.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            //合併先科目の表
            stb.append("  LEFT JOIN(");
            stb.append("    SELECT COMBINED_SUBCLASSCD AS SUBCLASSCD");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(     " , COMBINED_CLASSCD AS CLASSCD, COMBINED_SCHOOL_KIND AS SCHOOL_KIND, COMBINED_CURRICULUM_CD AS CURRICULUM_CD ");
            }
            stb.append("    FROM   SUBCLASS_REPLACE_COMBINED_DAT");
            stb.append("    WHERE  YEAR = '" + _param._year + "'");
            stb.append("    GROUP BY COMBINED_SUBCLASSCD");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(     " , COMBINED_CLASSCD, COMBINED_SCHOOL_KIND, COMBINED_CURRICULUM_CD ");
            }
            stb.append(" )T9 ON T9.SUBCLASSCD = T1.SUBCLASSCD");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(     " AND T9.CLASSCD = T1.CLASSCD AND T9.SCHOOL_KIND = T1.SCHOOL_KIND AND T9.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            //合併元科目の表
            stb.append("  LEFT JOIN(");
            stb.append("    SELECT ATTEND_SUBCLASSCD AS SUBCLASSCD, MAX(PRINT_FLG2) AS PRINT_FLG");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(     " , ATTEND_CLASSCD AS CLASSCD, ATTEND_SCHOOL_KIND AS SCHOOL_KIND, ATTEND_CURRICULUM_CD AS CURRICULUM_CD ");
            }
            stb.append("    FROM   SUBCLASS_REPLACE_COMBINED_DAT");
            stb.append("    WHERE  YEAR = '" + _param._year + "'");
            stb.append("    GROUP BY ATTEND_SUBCLASSCD");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(     " , ATTEND_CLASSCD, ATTEND_SCHOOL_KIND, ATTEND_CURRICULUM_CD ");
            }
            stb.append(" )T10 ON T10.SUBCLASSCD = T1.SUBCLASSCD");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(     " AND T10.CLASSCD = T1.CLASSCD AND T10.SCHOOL_KIND = T1.SCHOOL_KIND AND T10.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }

            stb.append(" LEFT JOIN CREDITS_A T6 ON T6.SCHREGNO = T1.SCHREGNO AND T6.SUBCLASSCD = T1.SUBCLASSCD");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(     " AND T6.CLASSCD = T1.CLASSCD AND T6.SCHOOL_KIND = T1.SCHOOL_KIND AND T6.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append(" LEFT JOIN CREDITS_B T11 ON T11.SUBCLASSCD = T1.SUBCLASSCD AND T11.SCHREGNO = T1.SCHREGNO");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(     " AND T11.CLASSCD = T1.CLASSCD AND T11.SCHOOL_KIND = T1.SCHOOL_KIND AND T11.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append(" LEFT JOIN SUBCLASS_MST T7 ON T7.SUBCLASSCD = T1.SUBCLASSCD");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(     " AND T7.CLASSCD = T1.CLASSCD AND T7.SCHOOL_KIND = T1.SCHOOL_KIND AND T7.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append(" LEFT JOIN CLASS_MST T8 ON T8.CLASSCD = LEFT(T1.SUBCLASSCD,2)");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(     " AND T8.SCHOOL_KIND = T1.SCHOOL_KIND ");
            }

//            stb.append(" WHERE ");
//            stb.append("     EXISTS ( ");
//            stb.append("        SELECT ");
//            stb.append("            'x' ");
//            stb.append("        FROM ");
//            stb.append("            RECORD_AVERAGE_DAT E1 ");
//            stb.append("        WHERE ");
//            stb.append("            E1.YEAR = '" + _param._year + "' ");
//            stb.append("            AND E1.SEMESTER = '" + _param._semester + "' ");
//            stb.append("            AND E1.TESTKINDCD || E1.TESTITEMCD = '" + _param._testKindCd + "' ");
//            if ("1".equals(_param._useCurriculumcd)) {
//                stb.append("            AND E1.CLASSCD || '-' || E1.SCHOOL_KIND || '-' || E1.CURRICULUM_CD || '-' || E1.SUBCLASSCD = T1.SUBCLASSCD");
//            } else {
//                stb.append("            AND E1.SUBCLASSCD = T1.SUBCLASSCD");
//            }
//            stb.append("            AND E1.AVG_DIV = '1' ");
//            stb.append("            AND E1.GRADE = '" + _param._grade + "' ");
//            stb.append("     ) ");
//            if (!"9900".equals(_param._testKindCd)) {
//                stb.append("     AND T9.SUBCLASSCD IS NULL ");
//            }

            stb.append(" ORDER BY T1.SUBCLASSCD, T1.SCHREGNO");

            return stb.toString();
        }

        private void loadScoreDetail(
                final DB2UDB db2
        ) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = sqlStdSubclassDetail(getCode());
                log.error(" subclassDetail = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    if (enablePrintFlg() && "1".equals(rs.getString("PRINT_FLG"))) {
                        continue;
                    }
                    final Student student = getStudent(rs.getString("SCHREGNO"));
                    if (student == null) {
                        continue;
                    }

                    final String subclassCd = rs.getString("SUBCLASSCD") != null && rs.getString("SUBCLASSCD").length() > 1 ? rs.getString("SUBCLASSCD").substring(1) : "";

                    final String classCd = subclassCd == null || "".equals(subclassCd) ? "" : subclassCd.substring(0, 2);
                    if (classCd.compareTo(KNJDefineSchool.subject_U) <= 0 || classCd.equals(KNJDefineSchool.subject_T)) {

                        if (rs.getString("SCORE") == null) {
                            continue;
                        }

                        final ScoreDetail scoreDetail = createScoreDetail(rs,_subclasses);
                        student._scoreDetails.put(scoreDetail._subClass._subclasscode, scoreDetail);
                    }
                }
            } catch (SQLException e) {
                log.error("SQLException", e);
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        /** ＤＢより組名称及び担任名を取得するメソッド **/
        private void loadHrStaff(DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try{
                String sql;
                sql = "SELECT "
                        + "HR_NAME,"
                        + "HR_NAMEABBV,"
                        + "W1.STAFFNAME AS STAFFNAME1,"
                        + "W3.STAFFNAME AS STAFFNAME2,"
                        + "W4.STAFFNAME AS STAFFNAME3,"
                        + "CLASSWEEKS,"
                        + "CLASSDAYS "
                    + "FROM "
                        + "SCHREG_REGD_HDAT W2 "
                        + "LEFT JOIN STAFF_MST W1 ON W1.STAFFCD=W2.TR_CD1 "
                        + "LEFT JOIN STAFF_MST W3 ON W3.STAFFCD=W2.TR_CD2 "
                        + "LEFT JOIN STAFF_MST W4 ON W4.STAFFCD=W2.TR_CD3 "
                    + "WHERE "
                            + "YEAR = '" + _param._year + "' "
                        + "AND GRADE || HR_CLASS = '" + _hrclassCd + "' ";
                if( !"9".equals(_param._semester)) sql = sql               //学期指定の場合
                        + "AND SEMESTER = '" + _param._semester + "'";
                else                        sql = sql               //学年指定の場合
                        + "AND SEMESTER = '" + _param._ctrlSemester + "'";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if( rs.next() ){
                    _hrName = rs.getString("HR_NAME");
                    _staffName1 = rs.getString("STAFFNAME1");
                    _staffName2 = rs.getString("STAFFNAME2");
                    _staffName3 = rs.getString("STAFFNAME3");
                }

            } catch( Exception ex ){
                log.error("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        /**
         * @param db2
         * @throws SQLException
         */
        private void setHrAverage() {
            int count3 = 0;
            int score3 = 0;
            double avg3 = 0;
            int count5 = 0;
            int score5 = 0;
            double avg5 = 0;
            int count9 = 0;
            int score9 = 0;
            double avg9 = 0;
            for (Iterator it = _students.iterator(); it.hasNext();) {
                Student student = (Student) it.next();
                if (student._recordRankDat.get(subclassCdAll3) != null) {
                    final RecordRankDat rrd = (RecordRankDat) student._recordRankDat.get(subclassCdAll3);
                    count3++;
                    score3 += Integer.parseInt(rrd._score);
                    avg3 += Double.parseDouble(rrd._avg2);
                }
                if (student._recordRankDat.get(subclassCdAll5) != null) {
                    final RecordRankDat rrd = (RecordRankDat) student._recordRankDat.get(subclassCdAll5);
                    count5++;
                    score5 += Integer.parseInt(rrd._score);
                    avg5 += Double.parseDouble(rrd._avg2);
                }
                if (student._recordRankDat.get(subclassCdAll9) != null) {
                    final RecordRankDat rrd = (RecordRankDat) student._recordRankDat.get(subclassCdAll9);
                    count9++;
                    score9 += Integer.parseInt(rrd._score);
                    avg9 += Double.parseDouble(rrd._avg2);
                }
            }
            if (0 < count3) {
                final double doubleScore = (double) score3 / count3;
                BigDecimal decimalScore = new BigDecimal(doubleScore);
                _hrAverageScore3 = decimalScore.setScale(1, BigDecimal.ROUND_HALF_UP).toString();

                final double doubleAvg = avg3 / count3;
                BigDecimal decimalAvg = new BigDecimal(doubleAvg);
                _hrAverageAvg3 = decimalAvg.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
            }
            if (0 < count5) {
                final double doubleScore = (double) score5 / count5;
                BigDecimal decimalScore = new BigDecimal(doubleScore);
                _hrAverageScore5 = decimalScore.setScale(1, BigDecimal.ROUND_HALF_UP).toString();

                final double doubleAvg = avg5 / count5;
                BigDecimal decimalAvg = new BigDecimal(doubleAvg);
                _hrAverageAvg5 = decimalAvg.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
            }
            if (0 < count9) {
                final double doubleScore = (double) score9 / count9;
                BigDecimal decimalScore = new BigDecimal(doubleScore);
                _hrAverageScore9 = decimalScore.setScale(1, BigDecimal.ROUND_HALF_UP).toString();

                final double doubleAvg = avg9 / count9;
                BigDecimal decimalAvg = new BigDecimal(doubleAvg);
                _hrAverageAvg9 = decimalAvg.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
            }
        }

        private String sqlRecordRankDat() {
            StringBuffer stb = new StringBuffer();
            stb.append("SELECT ");
            stb.append("    * ");
            stb.append("FROM ");
            stb.append("    " + _param._rankTable + " T1 ");
            stb.append("WHERE ");
            stb.append("    T1.YEAR = '" + _param._year + "'");
            stb.append("    AND T1.SEMESTER = '" + _param._semester + "' ");
            stb.append("    AND T1.TESTKINDCD || T1.TESTITEMCD = '" + _param._testKindCd + "'");
            stb.append("    AND T1.SUBCLASSCD IN ('" + subclassCdAll3 + "', '" + subclassCdAll5 + "', '" + subclassCdAll9 + "') ");
            stb.append("    AND T1.SCHREGNO = ? ");
            return stb.toString();
        }

        /**
         * @param db2
         * @throws SQLException
         */
        private void setRecordRankDat(DB2UDB db2) {
            PreparedStatement ps = null;
            final String sql = sqlRecordRankDat();
            try {
                ps = db2.prepareStatement(sql);

                for (Iterator it = _students.iterator(); it.hasNext();) {
                    Student student = (Student) it.next();
                    student._recordRankDat.clear();
                    ps.setString(1, student._code);
                    final ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        String subclassCd;
                        if ("1".equals(_param._useCurriculumcd) && !("333333".equals(rs.getString("SUBCLASSCD")) || "555555".equals(rs.getString("SUBCLASSCD")) || "999999".equals(rs.getString("SUBCLASSCD")))) {
                            subclassCd = rs.getString("CLASSCD") + "-" + rs.getString("SCHOOL_KIND") + "-" + rs.getString("CURRICULUM_CD") + "-" + rs.getString("SUBCLASSCD");
                        } else {
                            subclassCd = rs.getString("SUBCLASSCD");
                        }
                        String score = rs.getString("SCORE");
                        String avg = rs.getString("AVG") == null ? "" : rs.getBigDecimal("AVG").setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                        String avg2 = rs.getString("AVG");
                        String gradeRank = rs.getString("GRADE_RANK");
                        String gradeAvgRank = rs.getString("GRADE_AVG_RANK");
                        String courseRank = rs.getString("COURSE_RANK");
                        String courseAvgRank = rs.getString("COURSE_AVG_RANK");
                        String classRank = rs.getString("CLASS_RANK");
                        String classAvgRank = rs.getString("CLASS_AVG_RANK");
                        String majorRank = rs.getString("MAJOR_RANK");
                        String majorAvgRank = rs.getString("MAJOR_AVG_RANK");

                        RecordRankDat recordRankDat = new RecordRankDat(student._code, subclassCd, score, avg, avg2, gradeRank, gradeAvgRank, courseRank, courseAvgRank, classRank, classAvgRank, majorRank, majorAvgRank);
                        student._recordRankDat.put(subclassCd, recordRankDat);
                    }
                    rs.close();
                }

            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(ps);
            }
        }

        private String sqlRecordAverageDat() {
            StringBuffer stb = new StringBuffer();
            stb.append("SELECT ");
            stb.append("    * ");
            stb.append("FROM ");
            stb.append("    " + _param._avgTable + " T1 ");
            stb.append("WHERE ");
            stb.append("    T1.YEAR = '" + _param._year + "'");
            stb.append("    AND T1.SEMESTER = '" + _param._semester + "' ");
            stb.append("    AND T1.TESTKINDCD || T1.TESTITEMCD = '" + _param._testKindCd + "'");
            stb.append("    AND T1.AVG_DIV = '2' ");
            return stb.toString();
        }

        /**
         * @param db2
         * @throws SQLException
         */
        private void setRecordAverageDat(DB2UDB db2) {
            PreparedStatement ps = null;
            final String sql = sqlRecordAverageDat();
            try {
                ps = db2.prepareStatement(sql);
                final ResultSet rs = ps.executeQuery();
                _recordAverageDat.clear();
                while (rs.next()) {
                    String subclassCd;
                    if ("1".equals(_param._useCurriculumcd) && !("333333".equals(rs.getString("SUBCLASSCD")) || "555555".equals(rs.getString("SUBCLASSCD")) || "999999".equals(rs.getString("SUBCLASSCD")))) {
                        subclassCd = rs.getString("CLASSCD") + "-" + rs.getString("SCHOOL_KIND") + "-" + rs.getString("CURRICULUM_CD") + "-" + rs.getString("SUBCLASSCD");
                    } else {
                        subclassCd = rs.getString("SUBCLASSCD");
                    }
                    final String avgDiv = rs.getString("AVG_DIV");
                    final String avg = rs.getString("AVG") == null ? "" : rs.getBigDecimal("AVG").setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                    final String score = rs.getString("SCORE");
                    final String count = rs.getString("COUNT");
                    final String grade = rs.getString("GRADE");
                    final String hrclass = rs.getString("HR_CLASS");
                    final String courseCd = rs.getString("COURSECD");
                    final String majorCd = rs.getString("MAJORCD");
                    final String courseCode = rs.getString("COURSECODE");

                    final RecordAverageDat recordAverageDat = new RecordAverageDat(subclassCd, avgDiv, grade, hrclass, courseCd, majorCd, courseCode, score, count, avg);
                    final String key = subclassCd + avgDiv + grade + hrclass + courseCd + majorCd + courseCode;
                    _recordAverageDat.put(key, recordAverageDat);
                }
                rs.close();

            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(ps);
            }
        }

        String getStaffName() {
            StringBuffer stb = new StringBuffer();
            String comma = "";
            if (_staffName1 != null) {
                stb.append(comma).append(_staffName1);
                comma = "、";
            }
            if (_staffName2 != null) {
                stb.append(comma).append(_staffName2);
                comma = "、";
            }
            if (_staffName3 != null) {
                stb.append(comma).append(_staffName3);
                comma = "、";
            }
            return stb.toString();
        }

        public int compareTo(final Object o) {
            if (!(o instanceof HRInfo)) return -1;
            final HRInfo that = (HRInfo) o;
            return _hrclassCd.compareTo(that._hrclassCd);
        }

        public String toString() {
            return _hrName + "[" + getStaffName() + "]";
        }
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * <<生徒のクラス>>。
     */
    private class Student implements Comparable {
        private final int _gnum;  // 行番号
        private final String _code;  // 学籍番号
        private final String _courseCd;
        private final String _majorCd;
        private final String _grade;
        private final String _courseCode;
        private final HRInfo _hrInfo;
        private String _attendNo;
        private String _name;
        private String _sexName;
        private String _courseCodeName;
        private String _residentCd;
        private final Map _scoreDetails = new HashMap();
        private Total _total;
        private final Map _recordRankDat = new HashMap();

        Student(final String code, final HRInfo hrInfo, final String courseCd, final String majorCd, final String grade, final String courseCode, final int gnum) {
            _gnum = gnum;
            _code = code;
            _hrInfo = hrInfo;
            _courseCd = courseCd;
            _majorCd = majorCd;
            _grade = grade;
            _courseCode = courseCode;
        }

        String getCourse() {
            return _courseCd + _majorCd + _courseCode;
        }

        String getResidentMark() { return "4".equals(_residentCd) ? "○" : ""; }

        ScoreDetail getScoreDetail(String subclassCd) {
            if (subclassCd == null) {
                return null;
            }
            for (Iterator it = _scoreDetails.keySet().iterator(); it.hasNext();) {
                String keySubClassCd = (String) it.next();
                if (keySubClassCd.substring(1).equals(subclassCd)) {
                    return (ScoreDetail) _scoreDetails.get(keySubClassCd);
                }
            }
            return null;
        }
        
        public boolean hasKesshi(final HRInfo hrInfo, final String subclasscd) {
            boolean hasAttendSubclasscdKesshi = false;
            final String course = getCourse();
            if (null == subclasscd) {
                for (final Iterator cait = hrInfo.getCombinedAttendSubclassMap(course).entrySet().iterator(); cait.hasNext();) {
                    final Map.Entry e = (Map.Entry) cait.next();
                    final String combinedSubclasscd = (String) e.getKey();
                    final List attendSubclasscdList = hrInfo.getAttendSubclassList(course, combinedSubclasscd);
                    for (final Iterator ait = attendSubclasscdList.iterator(); ait.hasNext();) {
                        final String attendSubclasscd = (String) ait.next();
                        final ScoreDetail scoreAtt = getScoreDetail(attendSubclasscd);
                        if (null == scoreAtt || null == scoreAtt._score || null != scoreAtt._valueDi) {
                            hasAttendSubclasscdKesshi = true;
                            break;
                        }
                    }
                }
            } else {
                final List attendSubclasscdList = hrInfo.getAttendSubclassList(course, subclasscd);
                for (final Iterator ait = attendSubclasscdList.iterator(); ait.hasNext();) {
                    final String attendSubclasscd = (String) ait.next();
                    final ScoreDetail scoreAtt = getScoreDetail(attendSubclasscd);
                    if (null == scoreAtt || null == scoreAtt._score || null != scoreAtt._valueDi) {
                        hasAttendSubclasscdKesshi = true;
                        break;
                    }
                }
            }
            return hasAttendSubclasscdKesshi;
        }

        /**
         * 出席番号順にソートします。
         * {@inheritDoc}
         */
        public int compareTo(final Object o) {
            if (!(o instanceof Student)) return -1;
            final Student that = (Student) o;
            int rtn;
            rtn = _hrInfo.compareTo(that._hrInfo);
            if (0 == rtn) {
                rtn = _attendNo.compareTo(that._attendNo);
            }
            return rtn;
        }

        public String toString() {
            return _attendNo + ":" + _name;
        }
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * <<科目のクラスです>>。
     */
    private class SubClass implements Comparable {
        private final String _classabbv;
        private final String _classcode;
        private final Integer _classmstShoworder4;
        private final String _subclasscode;
        private final String _subclassabbv;
        private final boolean _electdiv; // 選択科目
        private final boolean _saki; // 元科目

        SubClass(
                final String subclasscode,
                final String classabbv,
                final Integer classmstShoworder4,
                final String subclassabbv,
                final boolean electdiv,
                final boolean saki
        ) {
            _classabbv = classabbv;
            // (subclasscodeは頭1桁+科目コードなので教科コードは2文字目から2桁)
            _classcode = subclasscode.substring(1, 3);
            _classmstShoworder4 = classmstShoworder4;
            _subclasscode = subclasscode;
            _subclassabbv = subclassabbv;
            _electdiv = electdiv;
            _saki = saki;
        }

        public boolean equals(final Object obj) {
            if (!(obj instanceof SubClass)) return false;
            final SubClass that = (SubClass) obj;
            return _subclasscode.equals(that._subclasscode);
        }

        public String getSubclassCd() {
            return _subclasscode.substring(1);
        }

        public int hashCode() {
            return _subclasscode.hashCode();
        }

        public int compareTo(Object o) {
            if (!(o instanceof SubClass)) {
                return -1;
            }
            SubClass other = (SubClass) o;
            int rtn = _classmstShoworder4.compareTo(other._classmstShoworder4);
            if (rtn == 0) {
                rtn = _subclasscode.compareTo(other._subclasscode);
            }
            return rtn;
        }

        public String toString() {
            return "["+_classabbv + " , " +_subclasscode + " , " +_classmstShoworder4 + " , " +_subclassabbv + " , " +_electdiv + " ]";
        }
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * <<素点・評定データのクラスです>>。
     */
    private static class ScoreValue {
        private final String _strScore;
        private int _val;

        ScoreValue(final String strScore) {
            _strScore = strScore;
        }

        String getScore() { return _strScore; }
        boolean hasIntValue() { return !StringUtils.isEmpty(_strScore) && StringUtils.isNumeric(_strScore); }
        int getScoreAsInt() { return Integer.parseInt(_strScore); }

        public String toString() {
            return _strScore;
        }
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * <<生徒別科目別データのクラスです>>。
     */
    private class ScoreDetail {
        final SubClass _subClass;
        final ScoreValue _score; // 素点
        final ScoreValue _patternAssess; // 成績
        final Integer _replacemoto;
        final String _print_flg;
        final String _valueDi;

        ScoreDetail(
                final SubClass subClass,
                final ScoreValue score,
                final ScoreValue patternAssess,
                final Integer replacemoto,
                final String print_flg,
                final String valueDi
        ) {
            _subClass = subClass;
            _score = score;
            _patternAssess = patternAssess;
            _replacemoto = replacemoto;
            _print_flg = print_flg;
            _valueDi = valueDi;
        }

        /**
         * @return 合併元科目はFalseを、以外はTrueを戻します。
         */
        boolean enableCredit() {
            if (null != _replacemoto && _replacemoto.intValue() >= 1) {
                return false;
            }
            return true;
        }

        public String toString() {
            return (_subClass + " , " + _score + " , " + _patternAssess + " , " + _replacemoto + " , "
                    + _print_flg);
        }
    }

    private class RecordRankDat {
        final String _schregno;
        final String _subclassCd;
        final String _score;
        final String _avg;
        final String _avg2;
        final String _gradeRank;
        final String _gradeAvgRank;
        final String _courseRank;
        final String _courseAvgRank;
        final String _classRank;
        final String _classAvgRank;
        final String _majorRank;
        final String _majorAvgRank;

        public RecordRankDat(
                final String schregno,
                final String subclassCd,
                final String score,
                final String avg,
                final String avg2,
                final String gradeRank,
                final String gradeAvgRank,
                final String courseRank,
                final String courseAvgRank,
                final String classRank,
                final String classAvgRank,
                final String majorRank,
                final String majorAvgRank) {
            _schregno = schregno;
            _subclassCd = subclassCd;
            _score = score;
            _avg = avg;
            _avg2 = avg2;
            _gradeRank = gradeRank;
            _gradeAvgRank = gradeAvgRank;
            _courseRank = courseRank;
            _courseAvgRank = courseAvgRank;
            _classRank = classRank;
            _classAvgRank = classAvgRank;
            _majorRank = majorRank;
            _majorAvgRank = majorAvgRank;
        }

        public String getRank1() {
            if (OUTPUT_RANK_CLASS.equals(_param._totalRankL)) {
                return _param._useAverageAsKijunten ? _classAvgRank : _classRank;
            } else if (OUTPUT_RANK_GRADE.equals(_param._totalRankL)) {
                return _param._useAverageAsKijunten ? _gradeAvgRank : _gradeRank;
            } else if (OUTPUT_RANK_COURSE.equals(_param._totalRankL)) {
                return _param._useAverageAsKijunten ? _courseAvgRank : _courseRank;
            } else if (OUTPUT_RANK_COURSEGROUP.equals(_param._totalRankL)) {
                return _param._useAverageAsKijunten ? _majorAvgRank : _majorRank;
            } else {
                return "";
            }
        }

        public String getRank2() {
            if (OUTPUT_RANK_CLASS.equals(_param._totalRankR)) {
                return _param._useAverageAsKijunten ? _classAvgRank : _classRank;
            } else if (OUTPUT_RANK_GRADE.equals(_param._totalRankR)) {
                return _param._useAverageAsKijunten ? _gradeAvgRank : _gradeRank;
            } else if (OUTPUT_RANK_COURSE.equals(_param._totalRankR)) {
                return _param._useAverageAsKijunten ? _courseAvgRank : _courseRank;
            } else if (OUTPUT_RANK_COURSEGROUP.equals(_param._totalRankR)) {
                return _param._useAverageAsKijunten ? _majorAvgRank : _majorRank;
            } else {
                return "";
            }
        }

        public String toString() {
            return _subclassCd + " : (" + _gradeRank + " , " + _gradeAvgRank + ") (" + _classRank + " , " + _classAvgRank + ") (" + _courseRank + " , " + _courseAvgRank + ") (" + _majorRank + " , " + _majorAvgRank + ")";
        }
    }

    private class RecordAverageDat {
        final String _subclassCd;
        final String _avgDiv;
        final String _grade;
        final String _hrclass;
        final String _courseCd;
        final String _majorCd;
        final String _courseCode;
        final String _score;
        final String _count;
        final String _avg;

        public RecordAverageDat(
                final String subclassCd,
                final String avgDiv,
                final String grade,
                final String hrclass,
                final String courseCd,
                final String majorCd,
                final String courseCode,
                final String score,
                final String count,
                final String avg) {
            _subclassCd = subclassCd;
            _avgDiv = avgDiv;
            _grade = grade;
            _hrclass = hrclass;
            _courseCd = courseCd;
            _majorCd = majorCd;
            _courseCode = courseCode;
            _score = score;
            _count = count;
            _avg = avg;
        }


        public String toString() {
            return _subclassCd + "-" + _avgDiv + "-" + _grade + "-" + _hrclass + "-" + _courseCd + _majorCd + _courseCode + " : (" + _score + " , " + _count + " , " + _avg + ") ";
        }
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * <<生徒別総合成績データのクラスです>>。
     */
    private class Total implements Comparable {
        private Student _student;
        private int _total;  // 総合点
        private int _count;  // 件数（成績）
        private BigDecimal _avgBigDecimal;  // 平均点
        private int _countFail;  //欠点科目数

        /**
         * コンストラクタ。
         * @param student
         */
        Total(final Student student) {
            _student = student;
            compute();
        }

        /**
         * 生徒別総合点・件数・履修単位数・修得単位数・特別活動欠課時数を算出します。
         */
        private void compute() {
            int total = 0;
            int count = 0;

            int countFail = 0;

            for (final Iterator it = _student._scoreDetails.values().iterator(); it.hasNext();) {
                final ScoreDetail detail = (ScoreDetail) it.next();

                final ScoreValue scoreValue = getTargetValue(detail);
                if (isAddTotal(scoreValue, detail._replacemoto)) {
                    total += scoreValue.getScoreAsInt();
                    count++;
                }
            }

            _total = total;
            _count = count;
            if (0 < count) {
                final double avg = (float) total / (float) count;
                _avgBigDecimal = new BigDecimal(avg);
            }
            _countFail = countFail;
        }


        /**
         * @param scoreValue
         * @param replacemoto
         * @param knjdObj
         * @return 成績総合計に組み入れる場合Trueを戻します。
         */
        private boolean isAddTotal(
                final ScoreValue scoreValue,
                final Integer replacemoto
        ) {
            if (null == scoreValue || !scoreValue.hasIntValue()) { return false; }
            if (isGakunenMatu() && null != replacemoto && 0 < replacemoto.intValue()) { return false; }
            return true;
        }

        /**
         * {@inheritDoc}
         */
        public int compareTo(final Object o) {
            if (!(o instanceof Total)) return -1;
            final Total that = (Total) o;

            return that._avgBigDecimal.compareTo(this._avgBigDecimal);
        }

        /**
         * {@inheritDoc}
         */
        public boolean equals(final Object o) {
            if (!(o instanceof Total)) return false;
            final Total that = (Total) o;
            return that._avgBigDecimal.equals(this._avgBigDecimal);
        }

        /**
         * {@inheritDoc}
         */
        public String toString() {
            return _avgBigDecimal.toString();
        }
    }

    private Param createParam(final HttpServletRequest request, final DB2UDB db2) throws ServletException {
        KNJServletUtils.debugParam(request, log);
        return new Param(request, db2);
    }

    private class Param {
        /** 年度 */
        final String _year;
        /** 学期 */
        final String _semester;
        /** LOG-IN時の学期（現在学期）*/
        final String _ctrlSemester;

        /** 学年 */
        final String _grade;

        final String[] _hrclasses;

        final String _ctrlDate;

        final String _testKindCd;

        private String _ketten;
        private String _rankTable = "RECORD_RANK_DAT";
        private String _avgTable = "RECORD_AVERAGE_DAT";

        /** 総合順位出力 1:学級 2:学年 3:コース 5:コースグループ */
        final boolean _outputRankClass;
        final boolean _outputRankCourse;
        final boolean _outputRankGrade;
        final boolean _outputRankCourseGroup;
        String _totalRankL;
        String _totalRankR;

        /** 基準点としてRECORD_RANK_DATで*_AVG_RANKを使用するか */
        final boolean _useAverageAsKijunten;

        /** 起動元のプログラムＩＤ */
        final String _prgId;

        /** 科目数　（26科目) */
        final int _formMaxColumn;

        /** 単位保留 */
        final boolean _creditDrop;

        /** 欠番を詰める */
        final boolean _noKetuban;

        /** 同一クラスでのコース毎に改頁あり */
        final boolean _outputCourse;

        /** "総合的な学習の時間"を表示しない */
        final boolean _notOutputSougou;

        private String _semesterName;
        private String _testItemName;

        private static final String FROM_TO_MARK = "\uFF5E";

        private final String _useCurriculumcd;

        Param(final HttpServletRequest request, final DB2UDB db2) throws ServletException {

            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _grade = request.getParameter("GRADE");
            _hrclasses = request.getParameterValues("CLASS_SELECTED");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _testKindCd = request.getParameter("TESTKINDCD");  //テスト・成績種別

            _outputRankClass = request.getParameter("OUTPUT_RANK1") != null;
            _outputRankGrade = request.getParameter("OUTPUT_RANK2") != null;
            _outputRankCourse = request.getParameter("OUTPUT_RANK3") != null;
            _outputRankCourseGroup = request.getParameter("OUTPUT_RANK5") != null;
            setTotalRankLR();

            _ketten = request.getParameter("KETTEN");
            _creditDrop = request.getParameter("OUTPUT4") != null;
            _noKetuban = request.getParameter("OUTPUT5") != null;
            _outputCourse = request.getParameter("OUTPUT_COURSE") != null;
            _notOutputSougou = "1".equals(request.getParameter("OUTPUT_SOUGOU"));

            _prgId = request.getParameter("PRGID");

            _useAverageAsKijunten = "2".equals(request.getParameter("OUTPUT_KIJUN"));

            _formMaxColumn = 26;

            _useCurriculumcd = request.getParameter("useCurriculumcd");
            
            setTestName(db2);
            setSemesterName(db2);
        }

        public String getFormFile() {
            return "KNJD653G.frm";
        }

        /** 年度 */
        public String getNendo() {
            return KenjaProperties.gengou(Integer.parseInt(_year)) + "年度";
        }

        /** 総合順位出力(0 or 1 or 2個選択可) */
        private void setTotalRankLR() {
            if (_outputRankClass) {
                _totalRankL = OUTPUT_RANK_CLASS;
                if (_outputRankCourse) {
                    _totalRankR = OUTPUT_RANK_COURSE;
                } else if (_outputRankGrade) {
                    _totalRankR = OUTPUT_RANK_GRADE;
                } else if (_outputRankCourseGroup) {
                    _totalRankR = OUTPUT_RANK_COURSEGROUP;
                } else {
                    _totalRankR = "";
                }
            } else if (_outputRankCourse) {
                _totalRankL = OUTPUT_RANK_COURSE;
                if (_outputRankGrade) {
                    _totalRankR = OUTPUT_RANK_GRADE;
                } else if (_outputRankCourseGroup) {
                    _totalRankR = OUTPUT_RANK_COURSEGROUP;
                } else {
                    _totalRankR = "";
                }
            } else if (_outputRankGrade) {
                _totalRankL = OUTPUT_RANK_GRADE;
                if (_outputRankCourseGroup) {
                    _totalRankR = OUTPUT_RANK_COURSEGROUP;
                } else {
                    _totalRankR = "";
                }
            } else if (_outputRankCourseGroup) {
                _totalRankL = OUTPUT_RANK_COURSEGROUP;
                _totalRankR = "";
            } else {
                _totalRankL = "";
                _totalRankR = "";
            }
        }

        /** 総合順位出力の順位欄項目名(左) */
        private String[] getRankNameL() {
            return getRankName(_totalRankL);
        }

        /** 総合順位出力の順位欄項目名(右) */
        private String[] getRankNameR() {
            return getRankName(_totalRankR);
        }

        /** 総合順位出力の順位欄項目名 */
        private String[] getRankName(final String totalRank) {
            if (OUTPUT_RANK_CLASS.equals(totalRank)) {
                return new String[]{"クラス"};
            } else if (OUTPUT_RANK_GRADE.equals(totalRank)) {
                return new String[]{"学年"};
            } else if (OUTPUT_RANK_COURSE.equals(totalRank)) {
                return new String[]{"コース"};
            } else if (OUTPUT_RANK_COURSEGROUP.equals(totalRank)) {
                return new String[]{"コース", "グループ"};
            } else {
                return new String[]{};
            }
        }

        private void setTestName(final DB2UDB db2) {
            _testItemName = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = "SELECT TESTITEMNAME "
                                 +   "FROM TESTITEM_MST_COUNTFLG_NEW "
                                 +  "WHERE YEAR = '" + _year + "' "
                                 +    "AND SEMESTER = '" + _semester + "' "
                                 +    "AND TESTKINDCD || TESTITEMCD = '" + _testKindCd + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    _testItemName = rs.getString("TESTITEMNAME");
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private void setSemesterName(DB2UDB db2){
            PreparedStatement ps = null;
            ResultSet rs = null;
            try{
                String sql = "SELECT * FROM SEMESTER_MST WHERE YEAR = '" + _year + "' AND SEMESTER = '" + _semester + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    _semesterName = rs.getString("SEMESTERNAME");
                }
            } catch( Exception ex ){
                log.error("exception!", ex );
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
    }
}
