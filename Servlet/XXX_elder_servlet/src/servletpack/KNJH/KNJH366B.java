/*
 * $Id: 45731b901acdfdecf453db074ec7e581fcab9341 $
 *
 * 作成日: 2012/02/21
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJH;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 * 学校教育システム 賢者 [成績管理] 高校通知票
 */

public class KNJH366B {

    private static final Log log = LogFactory.getLog(KNJH366B.class);

    private boolean _hasData;

    private static final String SUBCLASSALL = "999999";
    private static final String GRADE99 = "99";
    private static final String SEMTESTCD99900 = "99900";
    private static final String SEMTESTCD9_99900 = "9_99900";

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

            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            if (null != db2) {
                db2.commit();
                db2.close();
            }
            svf.VrQuit();
        }
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {

        final List studentList = getStudentList(db2);

        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            log.debug(" student = " + student._schregno + " : " + student._name);
            student.load(db2, _param);
            // 学習のようす等
            printStudent(db2, svf, student);
        }
    }

    private List getStudentList(final DB2UDB db2) {
        final List studentList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getStudentSql(_param);
            log.debug(" student sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String schregno = rs.getString("SCHREGNO");
                final String name = rs.getString("NAME");
                final String hrName = rs.getString("HR_NAME");
                final String coursecodeName = rs.getString("COURSECODENAME");
                final String attendno = rs.getString("ATTENDNO");
                final String gradeCourse = rs.getString("COURSE");
                final Student student = new Student(schregno, name, hrName, coursecodeName, attendno, gradeCourse);
                studentList.add(student);
            }

        } catch (SQLException e) {
            log.error("exception!!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return studentList;
    }

    private String getStudentSql(final Param param) {
        final StringBuffer stb = new StringBuffer();
        // 異動者を除外した学籍の表 => 組平均において異動者の除外に使用
        stb.append("WITH SCHNO_A AS(");
        stb.append("  SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.GRADE || T1.COURSECD || T1.MAJORCD || T1.COURSECODE AS COURSE, ");
        stb.append("     T1.COURSECODE, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     T1.SEMESTER ");
        stb.append("  FROM    SCHREG_REGD_DAT T1 ");
        stb.append("          , SEMESTER_MST T2 ");
        stb.append("  WHERE ");
        stb.append("     T1.YEAR = '" + param._year + "' ");
        stb.append("     AND T1.SEMESTER = '"+ param._semester +"' ");
        stb.append("     AND T1.YEAR = T2.YEAR ");
        stb.append("     AND T1.SEMESTER = T2.SEMESTER ");
        stb.append("     AND T1.GRADE || T1.HR_CLASS = '" + param._gradeHrclass + "' ");
        stb.append("     AND T1.SCHREGNO IN " + SQLUtils.whereIn(true, param._categorySelected) + " ");
        //                      在籍チェック:転学(2)・退学(3)者は除外 但し異動日が学期終了日または異動基準日より小さい場合
        //                                   転入(4)・編入(5)者は除外 但し異動日が学期終了日または異動基準日より大きい場合
        stb.append("     AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST S1 ");
        stb.append("                     WHERE   S1.SCHREGNO = T1.SCHREGNO ");
        stb.append("                         AND ((S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < CASE WHEN T2.EDATE < '" + param._date + "' THEN T2.EDATE ELSE '" + param._date + "' END) ");
        stb.append("                           OR (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE > CASE WHEN T2.EDATE < '" + param._date + "' THEN T2.EDATE ELSE '" + param._date + "' END)) ) ");
        //                      異動者チェック：留学(1)・休学(2)者は除外 但し学期終了日または基準日が異動開始日と終了日内にある場合
        stb.append("     AND NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT S1 ");
        stb.append("                     WHERE   S1.SCHREGNO = T1.SCHREGNO ");
        stb.append("                         AND S1.TRANSFERCD IN ('1','2') AND CASE WHEN T2.EDATE < '" + param._date + "' THEN T2.EDATE ELSE '" + param._date + "' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE) ");
        stb.append(" ) ");
        //メイン表
        stb.append(" SELECT ");
        stb.append("    T1.SCHREGNO, ");
        stb.append("    T7.HR_NAME, ");
        stb.append("    T1.ATTENDNO, ");
        stb.append("    T1.COURSE, ");
        stb.append("    T8.COURSECODENAME, ");
        stb.append("    T5.NAME ");
        stb.append(" FROM ");
        stb.append("    SCHNO_A T1 ");
        stb.append("    INNER JOIN SCHREG_BASE_MST T5 ON T1.SCHREGNO = T5.SCHREGNO ");
        stb.append("    LEFT JOIN SCHREG_REGD_HDAT T7 ON T7.YEAR = '" + _param._year + "' ");
        stb.append("        AND T7.SEMESTER = '" + _param._semester + "' ");
        stb.append("        AND T7.GRADE || T7.HR_CLASS = T1.GRADE || T1.HR_CLASS ");
        stb.append("    LEFT JOIN COURSECODE_MST T8 ON T8.COURSECODE = T1.COURSECODE ");
        stb.append(" ORDER BY ATTENDNO");
        return stb.toString();
    }

    private int getMS932ByteLength(final String str) {
        int ret = 0;
        if (null != str) {
            try {
                ret = str.getBytes("MS932").length;
            } catch (Exception e) {
                log.error("exception!", e);
            }
        }
        return ret;
    }

    /**
     * 学習のようす等を印刷する
     * @param db2
     * @param svf
     * @param student
     * @param viewClassList
     */
    private void printStudent(
            final DB2UDB db2,
            final Vrw32alp svf,
            final Student student) {

        final String form = "KNJH366B.frm";
        svf.VrSetForm(form, 4);

        printSvfHeader(svf, student);

        final Set profCdSet = new TreeSet();
        final List subclassList = new ArrayList();
        final List subclassProfList = new ArrayList();
        ScoreSubclass subclass999999 = null;
        ScoreSubclass subclassProf999999 = null;
        for (final Iterator it = student._scoreSubclassList.iterator(); it.hasNext();) {
            final ScoreSubclass subclass = (ScoreSubclass) it.next();
            if (subclass._testdivname.equals(ScoreSubclass.KOUSA)) {
                if (subclass._subclasscd.equals(SUBCLASSALL)) {
                    subclass999999 = subclass;
                    continue;
                }
                subclassList.add(subclass);
            } else if (subclass._testdivname.equals(ScoreSubclass.JITURYOKU)) {
                for (final Iterator gradeScoreRankMapIt = subclass._scoreRankMap.values().iterator(); gradeScoreRankMapIt.hasNext();) {
                    final Map scoreRankMap = (Map) gradeScoreRankMapIt.next();
                    profCdSet.addAll(scoreRankMap.keySet());
                }
                if (subclass._subclasscd.equals(SUBCLASSALL)) {
                    subclassProf999999 = subclass;
                    continue;
                }
                subclassProfList.add(subclass);
            }
        }

        // 実力テスト印刷
        printSvfPrificiencyRank(svf, student, new ArrayList(profCdSet), subclassProfList, subclassProf999999);

        // 学期評価・学年評定印刷
        printSvfRecordRank(svf, student, subclassList, subclass999999);

        _hasData = true;
    }

    private void printSvfPrificiencyRank(final Vrw32alp svf, final Student student, final List profCdList, final List subclassProfList, final ScoreSubclass subclassProf999999) {
        for (int profcdi = 0; profcdi < profCdList.size() && profcdi < 10; profcdi++) {
            final String profCd = (String) profCdList.get(profCdList.size() - 1 - profcdi);
            final String profName = StringUtils.defaultString((String) _param._mockNameMap.get(profCd));

            Integer grade = null;
            for (int i = 0; i < subclassProfList.size() && i < 10; i++) {
                final ScoreSubclass subclass = (ScoreSubclass) subclassProfList.get(i);
                for (final Iterator it = subclass._scoreRankMap.values().iterator(); it.hasNext();) {
                    final Map profcdScorerankMap = (Map) it.next();
                    final ScoreRank scorerank = (ScoreRank) profcdScorerankMap.get(profCd);
                    if (null != scorerank && null != scorerank._grade && (null == grade || grade.intValue() < Integer.parseInt(scorerank._grade))) {
                        grade = Integer.valueOf(scorerank._grade);
                    }
                }
            }
            String gradename = "";
            final String[] suuji = {null, "１", "２", "３", "１", "２", "３"};
            if (null != grade && grade.intValue() < suuji.length) {
                gradename = "第" + suuji[grade.intValue()] + "学年　";
            }
            final int profline = profcdi + 1;
            svf.VrsOutn("MOCK_NAME", profline, gradename + profName); // 実力テスト名称

            for (int i = 0; i < subclassProfList.size() && i < 10; i++) {
                final ScoreSubclass subclass = (ScoreSubclass) subclassProfList.get(i);
                final String col = String.valueOf(i + 1);
                svf.VrsOut("CLASS_NAME" + col, subclass._classname); // 教科名
                svf.VrsOut("SUBCLASS_NAME" + col, subclass._subclassname); // 科目名

                for (final Iterator it = subclass._scoreRankMap.values().iterator(); it.hasNext();) {
                    final Map profcdScorerankMap = (Map) it.next();
                    final ScoreRank scorerank = (ScoreRank) profcdScorerankMap.get(profCd);
                    if (null != scorerank) {
                        svf.VrsOutn("MOCK_SCORE" + col, profline, scorerank._score); // 実力得点
                        svf.VrsOutn("MOCK_DEVI" + col, profline, scorerank._deviation); // 実力偏差値
                    }
                }
            }
            if (null != subclassProf999999) {
                for (final Iterator it = subclassProf999999._scoreRankMap.values().iterator(); it.hasNext();) {
                    final Map profcdScorerankMap = (Map) it.next();
                    final ScoreRank scorerank = (ScoreRank) profcdScorerankMap.get(profCd);
                    if (null != scorerank) {
                        svf.VrsOutn("MOCK_TOTAL", profline, scorerank._score); // 実力合計
                        svf.VrsOutn("MOCK_AVERAGE", profline, scorerank._avg); // 実力平均
                        svf.VrsOutn("MOCK_RANK", profline, scorerank._rank); // 実力順位
                    }
                }
            }
        }
    }

    private void printSvfRecordRank(final Vrw32alp svf, final Student student, final List subclassList, final ScoreSubclass subclass999999) {
        final int MAX_LINE = 56;
        final String[] grades = new String[] {"01", "02", "03"};

        printHyouka(svf, grades, subclassList, subclass999999);

        final Map classMap = new TreeMap();
        for (final Iterator it = subclassList.iterator(); it.hasNext();) {
            final ScoreSubclass subclass = (ScoreSubclass) it.next();
            if (null == classMap.get(subclass._classcd)) {
                classMap.put(subclass._classcd, new ScoreClass(subclass._classcd, subclass._classname));
            }
            final ScoreClass scoreclass = (ScoreClass) classMap.get(subclass._classcd);
            scoreclass._scoreSubclasses.add(subclass);
            final ScoreRank sr = subclass.getScoreRank(GRADE99, SEMTESTCD9_99900);
            if (null != sr) {
                scoreclass._avgGrades = sr._avg;
            }
        }

        int line = 0;
        for (final Iterator it = classMap.keySet().iterator(); it.hasNext();) {
            final String classcd = (String) it.next();
            final ScoreClass scoreclass = (ScoreClass) classMap.get(classcd);
            if (SUBCLASSALL.equals(classcd)) {
                continue;
            }
            svf.VrsOut("CLASS", scoreclass._classname); // 教科

            final Map gradevalueListMap = new HashMap();
            for (int gradei = 0; gradei < grades.length; gradei++) {
                final String grade = grades[gradei];
                gradevalueListMap.put(grade, new ArrayList());
            }
            for (final Iterator subit = scoreclass._scoreSubclasses.iterator(); subit.hasNext();) {
                final ScoreSubclass subclass = (ScoreSubclass) subit.next();
                svf.VrsOut("GRPCD", classcd); // グループコード
                printSubclass(svf, grades, gradevalueListMap, subclass);
                svf.VrEndRecord();
                line += 1;
            }

            svf.VrsOut("GRPCD", classcd); // グループコード
            printClassAvg(svf, grades, gradevalueListMap, scoreclass);
            svf.VrEndRecord();
            line += 1;
        }
        for (int l = line; l <= MAX_LINE; l++) {
            svf.VrEndRecord();
        }
    }

    private void printClassAvg(final Vrw32alp svf, final String[] grades, final Map gradevalueListMap, final ScoreClass scoreClass) {
        svf.VrsOut("SUBCLASS1", "教科平均"); // 科目
        for (int gradei = 0; gradei < grades.length; gradei++) {
            final String grade = grades[gradei];
            svf.VrsOut("VALUE" + (gradei + 1), getListAvg((List) gradevalueListMap.get(grade))); // 評定
        }
        svf.VrsOut("THURU_VALUE", "(通年)" + StringUtils.defaultString(toAvgString(scoreClass._avgGrades))); // 通年
    }

    private static String toHyoteiString(final String hyotei) {
        if (null == hyotei) {
            return null;
        }
        return new BigDecimal(hyotei).setScale(0, BigDecimal.ROUND_DOWN).toString();
    }

    private static String toAvgString(final String avg) {
        if (null == avg) {
            return null;
        }
        return new BigDecimal(avg).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
    }

    private void printSubclass(final Vrw32alp svf, final String[] grades, final Map gradevalueListMap, final ScoreSubclass subclass) {
        final int subclassnamelen = getMS932ByteLength(subclass._subclassname);
        svf.VrsOut("SUBCLASS" + (subclassnamelen > 20 ? "3" : subclassnamelen > 14 ? "2" : "1"), subclass._subclassname); // 科目

        for (int gradei = 0; gradei < grades.length; gradei++) {
            final String grade = grades[gradei];
            final ScoreRank scorerank = (ScoreRank) subclass.getScoreRank(grade, SEMTESTCD99900);
            if (null != scorerank) {
                svf.VrsOut("VALUE" + (gradei + 1), scorerank._score); // 評定
                ((List) gradevalueListMap.get(grade)).add(scorerank._score);
            }
        }
    }

    private void printHyouka(final Vrw32alp svf, final String[] grades, final List subclassList, final ScoreSubclass subclass999999) {
        String hyouteiHeikinVal = null;
        if (null != subclass999999) {
            final String[] semes = new String[] {"1", "2", "9"};
            for (int gradei = 0; gradei < grades.length; gradei++) {
                final String grade = grades[gradei];

                final Map semtestcdMap = (Map) subclass999999.getSemtestcdMap(grade);
                for (int semesi = 0; semesi < semes.length; semesi++) {
                    final String semtestcd = semes[semesi] + "9900";
                    final ScoreRank scorerank = (ScoreRank) semtestcdMap.get(semtestcd);
                    if (null != scorerank) {
                        final String fieldi = String.valueOf(Integer.parseInt(grade)) + "_" + String.valueOf(semesi + 1);
                        svf.VrsOut("RANK" + fieldi, scorerank._rank); // 評価順位
                        svf.VrsOut("DEVI" + fieldi, scorerank._avg); // 評価段階
                    }
                }
            }
            final ScoreRank sr = subclass999999.getScoreRank(GRADE99, SEMTESTCD9_99900);
            if (null != sr) {
                hyouteiHeikinVal = toAvgString(sr._avg);
            }
        }

        for (int gradei = 0; gradei < grades.length; gradei++) {
            final List valueList = new ArrayList();
            final String grade = grades[gradei];
            for (final Iterator it = subclassList.iterator(); it.hasNext();) {
                final ScoreSubclass subclass = (ScoreSubclass) it.next();
                final ScoreRank scorerank = subclass.getScoreRank(grade, SEMTESTCD99900);
                if (scorerank != null && NumberUtils.isNumber(scorerank._score)) {
                    valueList.add(scorerank._score);
                }
            }
            svf.VrsOut("TOTAL_VALUE" + String.valueOf(gradei + 1), getListAvg(valueList)); // 評定
        }
        final String hyouteiHeikin = StringUtils.defaultString(hyouteiHeikinVal);
        svf.VrsOut("TOTAL_DEVI", hyouteiHeikin); // 評価段階
        svf.VrsOut("TOTAL_RANK", null); // 評価順位 TODO: テーブルに値ない

        svf.VrsOut("TOTAL_THURU_VALUE", "(通年)" + hyouteiHeikin); // 通年
    }

    /**
     * 数値のリストの平均(四捨五入)を算出する
     * @param valueList リスト
     * @return 数値のリストの平均
     */
    private String getListAvg(final List valueList) {
        BigDecimal sum = new BigDecimal("0");
        int count = 0;
        for (final Iterator it = valueList.iterator(); it.hasNext();) {
            final String num = (String) it.next();
            sum = sum.add(new BigDecimal(num));
            count += 1;
        }
        if (0 == count) {
            return null;
        }
        return sum.divide(new BigDecimal(count), 1, BigDecimal.ROUND_HALF_UP).toString();
    }

    private void printSvfHeader(final Vrw32alp svf, final Student student) {

        svf.VrsOut("NENDO", _param._year + "年度"); // 年度
        svf.VrsOut("TITLE", "成績個人表"); // タイトル
        svf.VrsOut("DATE", String.valueOf(_param._date.substring(0, 4)) + "年" + KNJ_EditDate.h_format_JP_MD(_param._date)); // 作成日
        svf.VrsOut("COURSE", StringUtils.defaultString(student._coursecodeName) + StringUtils.defaultString(student._hrName, "")); // コース名・年組
        final String attendno = null == student._attendno ? "" : (NumberUtils.isDigits(student._attendno)) ? String.valueOf(Integer.parseInt(student._attendno)) + "番" : student._attendno;
        svf.VrsOut("NO", attendno); // 番号
        svf.VrsOut("NAME", student._name); // 生徒氏名
        if ("1".equals(_param._juni)) {
            svf.VrsOut("RANK_NAME1", "学年");
            svf.VrsOut("RANK_NAME2_1_1", "学年");
            svf.VrsOut("RANK_NAME2_1_2", "順位");
        } else if ("2".equals(_param._juni)) {
            svf.VrsOut("RANK_NAME1", "コース");
            svf.VrsOut("RANK_NAME2_2_1", "コー");
            svf.VrsOut("RANK_NAME2_2_2", "ス順");
            svf.VrsOut("RANK_NAME2_2_3", "位");
        } else {
            svf.VrsOut("RANK_NAME1", "グループ");
            svf.VrsOut("RANK_NAME2_2_1", "グル");
            svf.VrsOut("RANK_NAME2_2_2", "ープ");
            svf.VrsOut("RANK_NAME2_2_3", "順位");
        }

        for (int g = 1; g <= 3; g++) {
            svf.VrsOut("GRADE_NAME" + String.valueOf(g), "第" + String.valueOf(g) + "学年"); // 学年名称
            svf.VrsOut("GRADE_NAME" + String.valueOf(g + 3), "第" + String.valueOf(g) + "学年"); // 学年名称
            final String[] semes = new String[] {"1", "2", "9"};
            for (int si = 0; si < semes.length; si++) {
                svf.VrsOut("SEM" + String.valueOf(g) + "_" + String.valueOf(si + 1), (String) _param._semesterMap.get(semes[si])); // 学期
            }
        }
    }

    private static class Student {
        final String _schregno;
        final String _name;
        final String _hrName;
        final String _coursecodeName;
        final String _attendno;
        final String _gradeCourse;
        List _scoreSubclassList = Collections.EMPTY_LIST;
        List _tyousashoClassList = Collections.EMPTY_LIST;

        public Student(final String schregno, final String name, final String hrName, final String coursecodeName, final String attendno, final String gradeCourse) {
            _schregno = schregno;
            _name = name;
            _hrName = hrName;
            _coursecodeName = coursecodeName;
            _attendno = attendno;
            _gradeCourse = gradeCourse;
        }

        public void load(final DB2UDB db2, final Param param) {
            _scoreSubclassList = ScoreSubclass.getScoreSubclassList(db2, param, _schregno);
        }
    }

    private static class ScoreClass {
        final String _classcd;
        final String _classname;
        final List _scoreSubclasses = new ArrayList();
        String _avgGrades;
        ScoreClass(final String classcd, final String classname) {
            _classcd = classcd;
            _classname = classname;
        }
    }

    private static class ScoreSubclass {

        final static String KOUSA = "KOUSA";
        final static String JITURYOKU = "JITURYOKU";

        final String _testdivname;
        final String _classcd;
        final String _subclasscd;
        final String _classname;
        final String _subclassname;
        final Map _scoreRankMap = new TreeMap();
        public ScoreSubclass(final String testdivname, final String classcd, final String subclasscd, final String classname, final String subclassname) {
            _testdivname = testdivname;
            _classcd = classcd;
            _subclasscd = subclasscd;
            _classname = classname;
            _subclassname = subclassname;
        }

        private static ScoreSubclass getScoreSubclass(final List list, final String subclasscd, final String testdivname) {
            for (final Iterator it = list.iterator(); it.hasNext();) {
                final ScoreSubclass ss = (ScoreSubclass) it.next();
                if (subclasscd.equals(ss._subclasscd) && testdivname.equals(ss._testdivname)) {
                    return ss;
                }
            }
            return null;
        }

        private static String getGrade(final Param param, final String annualYear) {
            if ("1".equals(param._knjSchoolMst._schoolDiv)) {
                // 年度
                final int pyear = Integer.parseInt(param._year);
                final int pgrade = Integer.parseInt(param._grade);
                final int grade = pgrade + (Integer.parseInt(annualYear) - pyear);
                if (!(1 <= grade && grade <= 3)) {
                    return null;
                }
                final DecimalFormat df = new DecimalFormat("00");
                return df.format(grade);
            }
            // 年次
            return annualYear;
        }

        public static List getScoreSubclassList(final DB2UDB db2, final Param param, final String schregno) {
            final List list = new ArrayList();

            final Map m = new HashMap();
            m.put("CERTIFKIND", "008");
            m.put("useCurriculumcd", param._useCurriculumcd);
            m.put("useClassDetailDat", param._useClassDetailDat);

            final KNJDefineSchool definecode = new KNJDefineSchool();
            final KNJH366B.KNJE070_1 e70 = new KNJH366B.KNJE070_1(db2, definecode, schregno, param._year, param._semester, m);
            for (final Iterator it = e70._printData._studyrecData.iterator(); it.hasNext();) {
                final KNJH366B.KNJE070_1.StudyrecClass sc = (KNJH366B.KNJE070_1.StudyrecClass) it.next();

                if (KNJE070_1.SqlStudyrec.total.equals(sc._classname)) {
                    final String subclasscd = SUBCLASSALL;
                    ScoreSubclass ss = getScoreSubclass(list, subclasscd, KOUSA);
                    if (null == ss) {
                        ss = new ScoreSubclass(KOUSA, subclasscd, subclasscd, subclasscd, subclasscd);
                        list.add(ss);
                    }
                    final ScoreRank scoreRank = new ScoreRank(null, null, sc._avgGrades, null, null);
                    ss.addScore(GRADE99, SEMTESTCD9_99900, scoreRank);
                    continue;
                } else if (e70.isSuraClassName(sc._classname)) {
                    continue;
                }

                for (final Iterator itd = sc._studyrecDatList.iterator(); itd.hasNext();) {
                    final KNJH366B.KNJE070_1.StudyrecDat sd = (KNJH366B.KNJE070_1.StudyrecDat) itd.next();
                    final String subclasscd;
                    if ("1".equals(param._useCurriculumcd)) {
                        subclasscd = sd._classcd + "-" + sd._schoolKind + "-" + sd._curriculumCd + "-" + sd._subclasscd;
                    } else {
                        subclasscd = sd._subclasscd;
                    }
                    final String testdivname = KOUSA;
                    ScoreSubclass ss = getScoreSubclass(list, subclasscd, testdivname);
                    if (null == ss) {
                        ss = new ScoreSubclass(testdivname, sd._classcd, subclasscd, sd._classname, sd._subclassname);
                        list.add(ss);
                    }

                    for (final Iterator itg = sd._gradesList.iterator(); itg.hasNext();) {
                        final KNJH366B.KNJE070_1.Grades grades = (KNJH366B.KNJE070_1.Grades) itg.next();
                        final String grade = getGrade(param, grades._annualYear);
                        if (null == grade) {
                            continue;
                        }
                        final String score = toHyoteiString(grades._grades);
                        final ScoreRank scoreRank = new ScoreRank(grade, score, null, null, null);
                        ss.addScore(grade, SEMTESTCD99900, scoreRank);
                    }
                    final String avg = sc._avgGrades;
                    final ScoreRank scoreRank = new ScoreRank(null, null, avg, null, null);
                    ss.addScore(GRADE99, SEMTESTCD9_99900, scoreRank);
                }
            }

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getScoreSubclassSql(param, schregno);
              log.debug(" subclass sql = " + sql);
                ps = db2.prepareStatement(sql);

                rs = ps.executeQuery();
                while (rs.next()) {
                  final String subclasscd = rs.getString("SUBCLASSCD");
                  final String testdivname = rs.getString("TEST_DIV_NAME");
                  if (null == subclasscd || null == testdivname) {
                      continue;
                  }
                  if (null == rs.getString("GRADE")) {
                      log.debug(" grade = " + rs.getString("GRADE"));
                      continue;
                  }

                  ScoreSubclass ss = getScoreSubclass(list, subclasscd, testdivname);
                  if (null == ss) {
                      ss = new ScoreSubclass(testdivname, rs.getString("CLASSCD"), subclasscd, rs.getString("CLASSNAME"), rs.getString("SUBCLASSNAME"));
                      list.add(ss);
                  }
                  final String semtestcd = rs.getString("SEMTESTCD");
                  final String grade = rs.getString("GRADE");
                  final String score = rs.getString("SCORE");
                  final String avg = null == rs.getString("AVG") ? null : rs.getBigDecimal("AVG").setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                  final String rank = rs.getString("RANK");
                  final String deviation = rs.getString("DEVIATION");
                  final ScoreRank scoreRank = new ScoreRank(grade, score, avg, rank, deviation);
                  ss.addScore(grade, semtestcd, scoreRank);
              }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }

        private Map createMap(final String grade) {
            if (null == _scoreRankMap.get(grade)) {
                _scoreRankMap.put(grade, new HashMap());
            }
            return (Map) _scoreRankMap.get(grade);
        }

        private Map getSemtestcdMap(final String grade) {
            return createMap(grade);
        }

        private ScoreRank getScoreRank(final String grade, final String semtestcd) {
            return (ScoreRank) getSemtestcdMap(grade).get(semtestcd);
        }

        private void addScore(final String grade, final String semtestcd, final ScoreRank scoreRank) {
            getSemtestcdMap(grade).put(semtestcd, scoreRank);

        }

        private static String getScoreSubclassSql(final Param param, final String schregno) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH MOCK AS ( ");
            stb.append(" SELECT  ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     '" + JITURYOKU + "' AS TEST_DIV_NAME, ");
            stb.append("     T1.MOCKCD AS SEMTESTCD, ");
            stb.append("     T1.MOCK_SUBCLASS_CD, ");
            stb.append("     T1.YEAR, ");
            stb.append("     T1.MOCKDIV, ");
            stb.append("     T1.MOCKCD, ");
            stb.append("     T1.SCORE, ");
            stb.append("     T1.AVG, ");
            if ("1".equals(param._juni)) {
                stb.append("     T1.GRADE_RANK AS RANK, ");
                stb.append("     T1.GRADE_DEVIATION AS DEVIATION, ");
            } else if ("2".equals(param._juni)) {
                stb.append("     T1.COURSE_RANK AS RANK, ");
                stb.append("     T1.COURSE_DEVIATION AS DEVIATION, ");
            } else {
                stb.append("     T1.CLASS_RANK AS RANK, ");
                stb.append("     T1.CLASS_DEVIATION AS DEVIATION, ");
            }
            stb.append("     MAX(T3.GRADE) AS GRADE, ");
            stb.append("     T3.COURSECD, ");
            stb.append("     T3.MAJORCD, ");
            stb.append("     T3.COURSECODE, ");
            stb.append("     T4.GROUP_CD ");
            stb.append(" FROM  ");
            stb.append("     MOCK_RANK_DAT T1 ");
            stb.append(" LEFT JOIN SCHREG_REGD_DAT T3 ON  T3.SCHREGNO = T1.SCHREGNO AND T3.YEAR = T1.YEAR ");
            stb.append(" LEFT JOIN COURSE_GROUP_CD_DAT T4 ON T4.YEAR = T3.YEAR AND T4.GRADE = T3.GRADE AND  ");
            stb.append("  T4.COURSECD = T3.COURSECD AND T4.MAJORCD = T3.MAJORCD AND T4.COURSECODE = T3.COURSECODE ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR <= '" + param._year + "' ");
            stb.append("     AND T1.SCHREGNO = '" + schregno + "' ");
            if ("2".equals(param._kijunten)) {
                stb.append("     AND T1.MOCKDIV = '2' "); // 平均点
            } else {
                stb.append("     AND T1.MOCKDIV = '1' "); // 総合点
            }
            stb.append(" GROUP BY  ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.MOCKCD, ");
            stb.append("     T1.MOCK_SUBCLASS_CD, ");
            stb.append("     T1.YEAR, ");
            stb.append("     T1.MOCKDIV, ");
            stb.append("     T1.MOCKCD, ");
            stb.append("     T1.SCORE, ");
            stb.append("     T1.AVG, ");
            if ("1".equals(param._juni)) {
                stb.append("     T1.GRADE_RANK, ");
                stb.append("     T1.GRADE_DEVIATION, ");
            } else if ("2".equals(param._juni)) {
                stb.append("     T1.COURSE_RANK, ");
                stb.append("     T1.COURSE_DEVIATION, ");
            } else {
                stb.append("     T1.CLASS_RANK, ");
                stb.append("     T1.CLASS_DEVIATION, ");
            }
            stb.append("     T3.COURSECD, ");
            stb.append("     T3.MAJORCD, ");
            stb.append("     T3.COURSECODE, ");
            stb.append("     T4.GROUP_CD ");
            stb.append(") , MAIN AS ( ");
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     '" + KOUSA + "' AS TEST_DIV_NAME, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     T1.CLASSCD, ");
                stb.append("     T1.SCHOOL_KIND, ");
                stb.append("     T1.CURRICULUM_CD, ");
            } else {
                stb.append("     SUBSTR(T1.SUBCLASSCD, 1, 2) AS CLASSCD, ");
            }
            stb.append("     T1.SUBCLASSCD, ");
            stb.append("     '' AS SUBCLASSNAME, ");
            stb.append("     T1.YEAR, ");
            stb.append("     T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD AS SEMTESTCD, ");
            stb.append("     T1.SCORE, ");
            stb.append("     T1.AVG, ");
            if ("1".equals(param._juni)) {
                if ("2".equals(param._kijunten)) {
                    stb.append("     T1.GRADE_AVG_RANK AS RANK, ");
                } else {
                    stb.append("     T1.GRADE_RANK AS RANK, ");
                }
                stb.append("     T1.GRADE_DEVIATION AS DEVIATION, ");
            } else if ("2".equals(param._juni)) {
                if ("2".equals(param._kijunten)) {
                    stb.append("     T1.COURSE_AVG_RANK AS RANK, ");
                } else {
                    stb.append("     T1.COURSE_RANK AS RANK, ");
                }
                stb.append("     T1.COURSE_DEVIATION AS DEVIATION, ");
            } else {
                if ("2".equals(param._kijunten)) {
                    stb.append("     T1.MAJOR_AVG_RANK AS RANK, ");
                } else {
                    stb.append("     T1.MAJOR_RANK AS RANK, ");
                }
                stb.append("     T1.MAJOR_DEVIATION AS DEVIATION, ");
            }
            stb.append("     CAST(NULL AS VARCHAR(6)) AS MOCK_SUBCLASS_CD ");
            stb.append(" FROM ");
            stb.append("     RECORD_RANK_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("     (T1.YEAR < '" + param._year + "' OR T1.YEAR = '" + param._year + "' AND (T1.SEMESTER <= '" + param._semester + "' OR T1.SEMESTER = '9')) ");
            stb.append("     AND T1.SCHREGNO = '" + schregno + "' ");
            stb.append("     AND T1.TESTKINDCD = '99' AND T1.TESTITEMCD = '00' ");
            stb.append("     AND T1.SUBCLASSCD = '" + SUBCLASSALL + "' ");
            stb.append(" UNION ALL ");
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     '" + JITURYOKU + "' AS TEST_DIV_NAME, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     (CASE WHEN T1.MOCK_SUBCLASS_CD = '999999' THEN '00' ELSE SUBSTR(T3.CLASSCD, 1, 2) END) AS CLASSCD, ");
                stb.append("     (CASE WHEN T1.MOCK_SUBCLASS_CD = '999999' THEN '00' ELSE SUBSTR(T3.SCHOOL_KIND, 1, 2) END) AS SCHOOL_KIND, ");
                stb.append("     (CASE WHEN T1.MOCK_SUBCLASS_CD = '999999' THEN '00' ELSE SUBSTR(T3.CURRICULUM_CD, 1, 2) END) AS CURRICULUM_CD, ");
            } else {
                stb.append("     '00' AS CLASSCD, ");
            }
            stb.append("     (CASE WHEN T1.MOCK_SUBCLASS_CD = '999999' THEN '999999' ELSE T3.MOCK_SUBCLASS_CD END) AS SUBCLASSCD, ");
            stb.append("     T3.SUBCLASS_NAME AS SUBCLASSNAME, ");
            stb.append("     T1.YEAR, ");
            stb.append("     T1.SEMTESTCD, ");
            stb.append("     T1.SCORE, ");
            stb.append("     T1.AVG, ");
            stb.append("     T1.RANK, ");
            stb.append("     T1.DEVIATION, ");
            stb.append("     T1.MOCK_SUBCLASS_CD ");
            stb.append(" FROM ");
            stb.append("     MOCK T1 ");
            stb.append("     LEFT JOIN MOCK_SUBCLASS_MST T3 ON T3.MOCK_SUBCLASS_CD = T1.MOCK_SUBCLASS_CD ");
            stb.append(" ), ORDER AS ( ");
            stb.append(" SELECT DISTINCT ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     T1.CLASSCD, ");
                stb.append("     T1.SCHOOL_KIND, ");
                stb.append("     T1.CURRICULUM_CD, ");
            }
            stb.append("     T1.SUBCLASSCD, ");
            stb.append("     T4.CLASSNAME AS CLASSNAME, ");
            stb.append("     CASE WHEN TEST_DIV_NAME = '" + JITURYOKU + "' THEN T1.SUBCLASSNAME ELSE T3.SUBCLASSNAME END AS SUBCLASSNAME, ");
            stb.append("     99 AS ORDER1, ");
            stb.append("     99 AS ORDER2 ");
            stb.append(" FROM  ");
            stb.append("     MAIN T1 ");
            stb.append(" LEFT JOIN SUBCLASS_MST T3 ON T3.SUBCLASSCD = T1.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(" AND T3.CLASSCD = T1.CLASSCD AND T3.SCHOOL_KIND = T1.SCHOOL_KIND AND T3.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append(" LEFT JOIN CLASS_MST T4 ON ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(" T4.CLASSCD = T1.CLASSCD AND T4.SCHOOL_KIND = T1.SCHOOL_KIND ");
            } else {
                stb.append(" T4.CLASSCD = SUBSTR(T1.SUBCLASSCD, 1, 2) ");
            }
            stb.append(" ) ");
            stb.append(" SELECT  ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     T1.CLASSCD || '-' || T1.SCHOOL_KIND AS CLASSCD, ");
                stb.append("     (CASE WHEN '999999' = T1.SUBCLASSCD THEN T1.SUBCLASSCD ");
                stb.append("       ELSE T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD END ");
                stb.append("     ) AS SUBCLASSCD, ");
            } else {
                stb.append("     T1.CLASSCD, ");
                stb.append("     T1.SUBCLASSCD, ");
            }
            stb.append("     T2.CLASSNAME, ");
            stb.append("     T2.SUBCLASSNAME, ");
            stb.append("     T1.TEST_DIV_NAME, ");
            stb.append("     T3.GRADE, ");
            stb.append("     T1.SEMTESTCD, ");
            stb.append("     T1.SCORE, ");
            stb.append("     T1.AVG, ");
            stb.append("     T1.RANK, ");
            stb.append("     T1.DEVIATION, ");
            stb.append("     T1.MOCK_SUBCLASS_CD, ");
            stb.append("     VALUE(T2.ORDER1, 99) AS ORDER1, ");
            stb.append("     VALUE(T2.ORDER2, 99) AS ORDER2 ");
            stb.append(" FROM  ");
            stb.append("     MAIN T1 ");
            stb.append(" LEFT JOIN ORDER T2 ON T2.SUBCLASSCD = T1.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(" AND T2.CLASSCD = T1.CLASSCD ");
                stb.append(" AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append(" AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append(" LEFT JOIN (SELECT YEAR, SCHREGNO, MIN(GRADE) AS GRADE FROM SCHREG_REGD_DAT GROUP BY YEAR, SCHREGNO) T3 ON T3.YEAR = T1.YEAR ");
            stb.append("     AND T3.SCHREGNO = T1.SCHREGNO ");
            stb.append(" ORDER BY ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     VALUE(T2.ORDER1, 99), T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, ");
            } else {
                stb.append("     VALUE(T2.ORDER1, 99), SUBSTR(T1.SUBCLASSCD, 1, 2), ");
            }
            stb.append("     VALUE(T2.ORDER2, 99), T1.SUBCLASSCD, T1.SEMTESTCD ");
            return stb.toString();
        }

        public String toString() {
            return "ScoreSubclass(" + _testdivname + ":" + _subclasscd + ":" + _subclassname + ":" + _scoreRankMap + ")";
        }
    }

    private static class ScoreRank {
        final String _grade;
        final String _score;
        final String _avg;
        final String _rank;
        final String _deviation;

        public ScoreRank(
                final String grade,
                final String score,
                final String avg,
                final String rank,
                final String deviation) {
            _grade = grade;
            _score = score;
            _avg = avg;
            _rank = rank;
            _deviation = deviation;
        }
        public String toString() {
            return "[grade=" + _grade + ", score=" + _score + ", rank=" + _rank + "]";
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 56595 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _year;
        final String _semester;
        final String _date;
        final String _gradeHrclass;
        final String _grade;
        final String[] _categorySelected;
        final String _juni; // 1:学年 2:コース 3:講座グループ
        final String _kijunten;

        /** 教育課程コードを使用するか */
        final String _useCurriculumcd;
        final String _useClassDetailDat;
        final Map _semesterMap;
        final Map _mockNameMap;

       /** 各学校における定数等設定 */
        private KNJDefineSchool _definecode;
        private KNJSchoolMst _knjSchoolMst;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _date = request.getParameter("CTRL_DATE").replace('/', '-');
            _gradeHrclass = request.getParameter("GRADE_HR_CLASS");
            _grade = null != _gradeHrclass && _gradeHrclass.length() > 2 ? _gradeHrclass.substring(0, 2) : null;
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _juni = request.getParameter("JUNI");
            _kijunten = request.getParameter("KIJUNTEN");

            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _useClassDetailDat = request.getParameter("useClassDetailDat");

            _semesterMap  =getSemesterNameMap(db2, _year);

            try {
                _knjSchoolMst = new KNJSchoolMst(db2, _year);
            } catch (SQLException e) {
                log.warn("学校マスタ取得でエラー", e);
            }
            _mockNameMap = getMockName(db2);
        }

        private List getNameMstD052(final DB2UDB db2, final String year) {
            final List rtn = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                String sql;
                if ("1".equals(_useCurriculumcd) && "1".equals(_useClassDetailDat)) {
                    sql  = " SELECT CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS NAME1 ";
                    sql += " FROM SUBCLASS_DETAIL_DAT WHERE YEAR = '" + year + "' AND SUBCLASS_SEQ = '010' ";
                } else {
                    sql = " SELECT NAME1 FROM V_NAME_MST WHERE YEAR = '" + year + "' AND NAMECD1 = 'D052' ";
                }
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    if (null != rs.getString("NAME1")) {
                        rtn.add(rs.getString("NAME1"));
                    }
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }

        private Map getSemesterNameMap(final DB2UDB db2, final String year) {
            final Map rtn = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer sql = new StringBuffer();
                sql.append(" SELECT ");
                sql.append("    SEMESTER, SEMESTERNAME ");
                sql.append(" FROM SEMESTER_MST T1 ");
                sql.append(" WHERE ");
                sql.append("    T1.YEAR = '" + year + "' ");
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn.put(rs.getString("SEMESTER"), rs.getString("SEMESTERNAME"));
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }
        private String getNameMst(final DB2UDB db2, final String year, final String namecd1, final String namecd2, final String field) {
            String rtn = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer sql = new StringBuffer();
                sql.append(" SELECT ");
                sql.append("    " + field + " ");
                sql.append(" FROM NAME_MST T1 ");
                sql.append("    INNER JOIN NAME_YDAT T2 ON T2.YEAR = '" + year + "' AND T2.NAMECD1 = T1.NAMECD1 AND T2.NAMECD2 = T1.NAMECD2 ");
                sql.append(" WHERE ");
                sql.append("    T1.NAMECD1 = '" + namecd1 + "' ");
                sql.append("    AND T1.NAMECD2 = '" + namecd2 + "' ");
                sql.append("   ");
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    if (null != rs.getString(field)) {
                        rtn = rs.getString(field);
                    }
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }

        /**
         * 名称マスタ NAMECD1='Z010' NAMECD2='00'読込
         */
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

        private Map getMockName(final DB2UDB db2) {
            Map rtn = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer sql = new StringBuffer();
                sql.append(" SELECT ");
                sql.append("     T1.MOCKCD AS SEMTESTCD, ");
                sql.append("     T1.MOCKNAME1 ");
                sql.append(" FROM ");
                sql.append("     MOCK_MST T1 ");
                sql.append(" ORDER BY ");
                sql.append("     T1.MOCKCD ");

                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn.put(rs.getString("SEMTESTCD"), rs.getString("MOCKNAME1"));
                }
            } catch (SQLException ex) {
                log.debug("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }
    }

    private static class KNJE070_1 {

        protected final DB2UDB db2;                      //Databaseクラスを継承したクラス
        public boolean nonedata;
        protected PreparedStatement ps1;
        protected final KNJDefineSchool _definecode;  // 各学校における定数等設定
        protected boolean _hasCertifSchool;  // "CERTIF_SCHOOL_DAT"の存在チェック
        protected Param _param;

        final PrintData _printData;

        public KNJE070_1(
                final DB2UDB db2,
                final KNJDefineSchool definecode,
                final String schregno,
                final String year,
                final String semes,
                final Map paramap
        ) {
            this.db2 = db2;
            nonedata = false;
            _definecode = definecode;
            _hasCertifSchool = definecode.hasTableHasField(db2, "CERTIF_SCHOOL_DAT", null);
            _printData = new PrintData(schregno, year, semes, paramap);

            svf_int(_printData);
            DbUtils.closeQuietly(ps1);
        }

        /**
         * 学習の記録データ の java.sql.PreparedStatement オブジェクトをメンバ変数 ps1 にセットします。<br>
         * [年度/学年]列名の印字 および 有効[年度/学年]をメンバ変数 _gradeMap にセットするメソッドを呼んでいます。
         * @return
         */
        protected SqlStudyrec getPreStatementStudyrec(
                final String schregno,
                final String year,
                final Map paramap
        ) {
            final SqlStudyrec sqlStudyrec = new SqlStudyrec();
            sqlStudyrec.setStype(1);
            sqlStudyrec.setDaiken_div_code();
            sqlStudyrec.setZensekiSubclassCd();

            return sqlStudyrec;
        }

        protected static boolean useSonotaJuusyo(final Map paramap) {
            final boolean isGrd = "025".equals(paramap.get("CERTIFKIND")) || "026".equals(paramap.get("CERTIFKIND"));
            final boolean rtn = !isGrd && "on".equals(paramap.get("SONOTAJUUSYO"));
            log.debug(" certifkind = " + paramap.get("CERTIFKIND") + " sonotaJuusyo? " + rtn);
            return rtn;
        }

        protected static class Util {
            public static Map toMap(final ResultSet rs) throws SQLException {
                final Map m = new HashMap();
                final ResultSetMetaData meta = rs.getMetaData();
                for (int c = 1; c <= meta.getColumnCount(); c++) {
                    m.put(meta.getColumnName(c), rs.getString(meta.getColumnName(c)));
                }
                return m;
            }
        }

        /**
         *  SVF-FORM 学習の記録出力
         *  04/09/13 Modify 引数にmaxgradeを追加
         */
        public void study_out(
                final PrintData printData,
                final Vrw32alp svf
        ) {
            final int aline = 20;                  //各教科評定平均値出力行数

            try {
                int avg_line = 0;                       //各教科の評定平均値の列番目
                int ad_credit = 0;                      //加算単位
                final Map suramap = new HashMap();  // 大検・前籍項・総合・留学・教科総計の単位数格納用
                try {

                    for (final Iterator it = printData._studyrecData.iterator(); it.hasNext();) {
                        final StudyrecClass studyrecClass = (StudyrecClass) it.next();

                        final boolean isE014 = studyrecClass._isE014;
                        final boolean isSogo = SqlStudyrec.sogo.equals(studyrecClass._classname);

                        final String credit = studyrecClass.credit(isE014 || isSogo);
                        final String compCredit = studyrecClass.compCredit(isE014 || isSogo);

                        Integer scredit = study_out_sura_credit(printData, isE014, isSogo, studyrecClass, credit, suramap, Integer.valueOf(String.valueOf(ad_credit)), printData._paramap);  // 大検・前籍項・総合・留学・教科総計の単位の処理
                        if (scredit != null) { ad_credit = scredit.intValue(); }  // 単位数総計

                        //  教科総計の処理
                        if (SqlStudyrec.total.equals(studyrecClass._classname)) {
                            if (studyrecClass._avgGrades != null) {
                                svf.VrsOut("average",  studyrecClass._avgGrades);     //全体の評定平均値
                            }
                        }

                        // 教科以外は以降の処理をしない 名称マスタE014に登録された科目コードの科目は/欄に表示する
                        if (isSuraClassName(studyrecClass._classname) || isE014 || isSogo) {
                            continue;
                        }

                        avg_line++;
                        if (avg_line < aline + 1) {
                            final String field = "subject" + avg_line + (8 < StringUtils.defaultString(studyrecClass._classname).length() ? "_2" : "_1");
                            svf.VrsOut(field, StringUtils.defaultString(studyrecClass._classname));   //教科名
                            svf.VrsOut("average_" + avg_line, StringUtils.defaultString(studyrecClass._avgGrades));  //評定
                        }

                        for (final Iterator itd = studyrecClass._studyrecDatList.iterator(); itd.hasNext();) {

                            final StudyrecDat studyrecDat = (StudyrecDat) itd.next();

                            boolean hasGrades = false;
                            if (!hasGrades) { continue; }
                            // 未履修科目は出力しないの場合
                            if ("2".equals(printData._paramap.get("MIRISYU"))) {
                                String credit1 = compCredit;
                                if (credit1 == null || credit1.equals("0")) {
                                    continue;
                                }
                            }

                            svf.VrEndRecord();
                            nonedata = true;
                        }

                    }
                } catch (Exception e) {
                    log.error("exception!", e);
                }

                nonedata = true;

                // フォームの最終処理
                if (nonedata) {
                    svf.VrEndRecord(); // 最後のレコード出力
                } else {
                    //  学習情報がない場合の処理
                    svf.VrsOut("CLASSCD","A");  // 教科コード
                    svf.VrEndRecord();
                    nonedata = true;
                }
            } catch (Exception e) {
                log.error("[KNJE070_1]study_out error!", e);
            }
        }

        private List getStudyrecData(final DB2UDB db2, final PrintData printData) {
            final List studyrecData = new ArrayList();
            ResultSet rs = null;
            try {
                rs = ps1.executeQuery();
                while (rs.next()) {
                    final String classcd = rs.getString("CLASSCD");
                    final String subclasscd = rs.getString("SUBCLASSCD");
                    final String classname = rs.getString("CLASSNAME");
                    final String subclassname = rs.getString("SUBCLASSNAME");
                    final String annual = rs.getString("ANNUAL");
                    final String grades = rs.getString("GRADES");
                    final String avgGrades = rs.getString("AVG_GRADES");
                    final String credit = rs.getString("CREDIT");
                    final String compCredit = rs.getString("COMP_CREDIT");
                    final String assessLevel = rs.getString("ASSESS_LEVEL");
                    final String specialDiv = rs.getString("SPECIALDIV");
                    final String schoolKind;
                    final String curriculumCd;
                    if ("1".equals(_param._useCurriculumcd)) {
                        schoolKind = rs.getString("SCHOOL_KIND");
                        curriculumCd = rs.getString("CURRICULUM_CD");
                    } else {
                        schoolKind = null;
                        curriculumCd = null;
                    }

                    StudyrecClass studyrecClass = null;
                    for (final Iterator it = studyrecData.iterator(); it.hasNext();) {
                        final StudyrecClass src = (StudyrecClass) it.next();
                        if (_param.isSameClasscd(classcd, schoolKind, src._classcd, src._schoolKind)) {
                            studyrecClass = src;
                            break;
                        }
                    }

                    if (null == studyrecClass || printData._e014Subclasscd != null && printData._e014Subclasscd.equals(subclasscd)) {
                        studyrecClass = new StudyrecClass(classcd, schoolKind, classname, avgGrades, printData._e014Subclasscd != null && printData._e014Subclasscd.equals(subclasscd));
                        studyrecData.add(studyrecClass);
                    }

                    StudyrecDat studyrecDat = null;

                    for (final Iterator it = studyrecClass._studyrecDatList.iterator(); it.hasNext();) {
                        final StudyrecDat srd = (StudyrecDat) it.next();
                        if (_param.isSameClasscd(classcd, schoolKind, srd._classcd, srd._schoolKind) && subclasscd.equals(srd._subclasscd)) {
                            studyrecDat = srd;
                            break;
                        }
                    }

                    if (null == studyrecDat) {
                        studyrecDat = new StudyrecDat(classcd, schoolKind, curriculumCd, subclasscd, classname, subclassname, avgGrades, credit, compCredit, assessLevel, specialDiv);
                        studyrecClass._studyrecDatList.add(studyrecDat);
                    }

                    final Grades gradez = new Grades(annual, grades, credit, compCredit);
                    studyrecDat.addGrades(gradez);

                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(rs);
                db2.commit();
            }
            return studyrecData;
        }

        private static class StudyrecClass {
            final String _classcd;
            final String _schoolKind;
            final String _classname;
            final String _avgGrades;
            final boolean _isE014;
            final List _studyrecDatList;

            public StudyrecClass(
                    final String classcd,
                    final String schoolKind,
                    final String classname,
                    final String avgGrades,
                    final boolean isE014) {
                _classcd = classcd;
                _schoolKind = schoolKind;
                _classname = classname;
                _avgGrades = avgGrades;
                _isE014 = isE014;
                _studyrecDatList = new ArrayList();
            }

            public String credit(final boolean isSum) {
                final List list = new ArrayList();
                for (final Iterator itd = _studyrecDatList.iterator(); itd.hasNext();) {
                    final StudyrecDat srd = (StudyrecDat) itd.next();
                    for (final Iterator it = srd._gradesList.iterator(); it.hasNext();) {
                        final Grades g = (Grades) it.next();
                        if (NumberUtils.isDigits(g._credit)) {
                            list.add(g._credit);
                        }
                    }
                }
                return list.isEmpty() ? null : (isSum ? sum(list) : (String) list.get(0));
            }

            public String compCredit(final boolean isSum) {
                final List list = new ArrayList();
                for (final Iterator itd = _studyrecDatList.iterator(); itd.hasNext();) {
                    final StudyrecDat srd = (StudyrecDat) itd.next();
                    for (final Iterator it = srd._gradesList.iterator(); it.hasNext();) {
                        final Grades g = (Grades) it.next();
                        if (NumberUtils.isDigits(g._compCredit)) {
                            list.add(g._compCredit);
                        }
                    }
                }
                return list.isEmpty() ? null : (isSum ? sum(list) : (String) list.get(0));
            }

            private static String sum(final List list) {
                int sum = 0;
                for (final Iterator it = list.iterator(); it.hasNext();) {
                    final String v = (String) it.next();
                    sum += Integer.parseInt(v);
                }
                return String.valueOf(sum);
            }
        }

        private static class StudyrecDat {
            final String _classcd;
            final String _schoolKind;
            final String _curriculumCd;
            final String _subclasscd;
            final String _classname;
            final String _subclassname;
            final String _avgGrades;
            final String _assessLevel;
            final String _specialDiv;
            final List _gradesList;

            public StudyrecDat(
                    final String classcd,
                    final String schoolKind,
                    final String curriculumCd,
                    final String subclasscd,
                    final String classname,
                    final String subclassname,
                    final String avgGrades,
                    final String credit,
                    final String compCredit,
                    final String assessLevel,
                    final String specialDiv) {
                _classcd = classcd;
                _schoolKind = schoolKind;
                _curriculumCd = curriculumCd;
                _subclasscd = subclasscd;
                _classname = classname;
                _subclassname = subclassname;
                _avgGrades = avgGrades;
                _assessLevel = assessLevel;
                _specialDiv = specialDiv;
                _gradesList = new ArrayList();
            }

            public String keySubclasscd(final Param param) {
                if ("1".equals(param._useCurriculumcd)) {
                    return _classcd + "-" + _schoolKind + "-" + _curriculumCd + "-" + _subclasscd;
                }
                return _subclasscd;
            }

            public void addGrades(Grades grades) {
                _gradesList.add(grades);
            }

            public String toString() {
                return " classcd=" + _classcd +
                ", schoolKind=" + _schoolKind +
                ", curriculumCd=" + _curriculumCd +
                ", subclasscd=" + _subclasscd +
                ", classname=" + _classname +
                ", subclassname=" + _subclassname +
                ", avgGrades=" + _avgGrades +
                ", assessLevel=" + _assessLevel +
                ", specialDiv=" + _specialDiv +
                "";
            }
        }

        private class Grades {
            final String _annualYear;
            final String _grades;
            final String _credit;
            final String _compCredit;
            public Grades(
                    final String annual,
                    final String grades,
                    final String credit,
                    final String compCredit
            ) {
                _annualYear = annual;
                _grades = grades;
                _credit = credit;
                _compCredit = compCredit;
            }
            public String toString() {
                return "[年次=" + _annualYear + " 評定=" + _grades + " 単位=" + _credit + " 履修単位=" + _compCredit + "]";
            }
        }

        //--- 内部クラス -------------------------------------------------------

        public boolean isSuraClassName(final String classname) {
            if (null == classname) { return false; }
            if (SqlStudyrec.total.equals(classname) || SqlStudyrec.totalCredit.equals(classname)) { return true; }
            if (SqlStudyrec.abroad.equals(classname)) { return true; }
            if (SqlStudyrec.sogo.equals(classname)) { return true; }
            if (SqlStudyrec.zenseki.equals(classname)) { return true; }
            if (SqlStudyrec.daiken.equals(classname)) { return true; }
            if (SqlStudyrec.lhr.equals(classname)) { return true; }
            if (SqlStudyrec.tokiwahr.equals(classname)) { return true; }
            return false;
        }

        /*
         *  大検・前籍項・総合・留学・教科総計の単位数の処理
         */
        private Integer study_out_sura_credit(
                final PrintData printData,
                final boolean isE014,
                final boolean isSogo,
                final StudyrecClass studyrecClass,
                final String credit,
                final Map suramap,
                final Integer scredit,
                final Map paramap
        ) {
            String subclassname = null;
            for (final Iterator it = studyrecClass._studyrecDatList.iterator(); it.hasNext();) {
                final StudyrecDat srd = (StudyrecDat) it.next();
                subclassname = srd._subclassname;
            }
            final String classname = studyrecClass._classname;

            int ad_credit = scredit.intValue();
            if (SqlStudyrec.totalCredit.equals(classname)) {
                suramap.put(SqlStudyrec.totalCredit, credit);
                if (credit != null) ad_credit += Integer.parseInt(credit);
            } else if (SqlStudyrec.abroad.equals(classname)) {
                suramap.put(SqlStudyrec.abroad, credit);
                if (credit != null) ad_credit += Integer.parseInt(credit);
            } else if (SqlStudyrec.zenseki.equals(classname)) {
                suramap.put(SqlStudyrec.zenseki, credit);
                if (credit != null) ad_credit += Integer.parseInt(credit);
            } else if (SqlStudyrec.daiken.equals(classname)) {
                suramap.put(SqlStudyrec.daiken, credit);
                if (credit != null) ad_credit += Integer.parseInt(credit);
            } else if (SqlStudyrec.lhr.equals(classname)) {
                suramap.put(SqlStudyrec.lhr, credit);
                if (credit != null) ad_credit += Integer.parseInt(credit);
            } else if (SqlStudyrec.tokiwahr.equals(classname)) {
                suramap.put(SqlStudyrec.tokiwahr, credit);
                if (credit != null) ad_credit += Integer.parseInt(credit);
            } else if (isE014) {
                suramap.put("E014", credit);
                suramap.put("E014SUBCLASSNAME", subclassname);
                if (credit != null) ad_credit += Integer.parseInt(credit);
            } else {
                return null;
            }
            return Integer.valueOf(String.valueOf(ad_credit));
        }

        protected boolean isPrintGradeTitleGakunen(final PrintData printData) {
            return isGakunensei();
        }

        /**
         * ＳＶＦ−ＦＯＲＭフィールド初期化
         */
        public final void svf_int(
                final PrintData printData
        ) {
            _definecode.setSchoolCode(db2, printData._year);
            _param = new Param(db2, _definecode);
            _param._tyousasyoNotPrintAnotherStudyrec = (String) printData._paramap.get("tyousasyoNotPrintAnotherStudyrec");
            _param._notUseClassMstSpecialDiv = setNotUseClassMstSpecialDiv(db2, printData._year, (String) printData._paramap.get("CERTIFKIND"), printData._schregno);
            _param._useCurriculumcd = (String) printData._paramap.get("useCurriculumcd");
            _param._useClassDetailDat = (String) printData._paramap.get("useClassDetailDat");

            try {
                final SqlStudyrec sqlStudyrec = getSqlStudyrec(printData);
                sqlStudyrec._e014SubclassCd = sqlStudyrec.getStudentE014Subclass(db2, printData);

                final String sql = sqlStudyrec.pre_sql(printData);
                log.fatal("学習記録データSQL = " + sql.toString());
                ps1 = db2.prepareStatement(sql);

                printData._sqlStudyrec = sqlStudyrec;
                printData.preprocessStudyrecDat(ps1, _param);
                printData._studyrecData = getStudyrecData(db2, printData);

            } catch (Exception e1) {
                 log.error("Exception", e1);
            }
        }

        private SqlStudyrec getSqlStudyrec(final PrintData printData) {
            final Map paramap = printData._paramap;
            final SqlStudyrec sqlStudyrec = getPreStatementStudyrec(printData._schregno, printData._year, paramap);
            final String hyotei2 = null == paramap.get("HYOTEI") ? "off" : (String) paramap.get("HYOTEI");
            sqlStudyrec.setHyoutei(hyotei2);
            sqlStudyrec.setDefinecode(_definecode);
            final String risyu = (!paramap.containsKey("RISYU")) ? "1" : (String) paramap.get("RISYU") ;
            sqlStudyrec.setRisyu(risyu);
            final String mirisyu = (!paramap.containsKey("MIRISYU")) ? "2" : (String) paramap.get("MIRISYU") ;
            sqlStudyrec.setMirisyu(mirisyu);
            return sqlStudyrec;
        }

        /*
         * 教科マスタの専門区分を使用の設定
         * ・生徒の入学日付の年度が、証明書学校データのREMARK7の値（年度）以前の場合
         *  1) 成績欄データのソートに教科マスタの専門区分を使用しない。
         *  2) 成績欄に教科マスタの専門区分によるタイトルを表示しない。（名称マスタ「E015」設定に優先する。）
         *   ※証明書学校データのREMARK7の値（年度）が null の場合
         *    1) 専門区分をソートに使用する。
         *    2) タイトルの表示/非表示は名称マスタ「E015」の設定による。
         */
        private static String setNotUseClassMstSpecialDiv(final DB2UDB db2, final String year, final String certifKind, final String schregno) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            String ret = null;
            try {
                final StringBuffer sql = new StringBuffer();
                sql.append(" WITH T_SCHOOL_KIND AS ( ");
                sql.append("     SELECT DISTINCT T1.SCHREGNO, T1.YEAR, T2.SCHOOL_KIND ");
                sql.append("     FROM SCHREG_REGD_DAT T1 ");
                sql.append("     INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR ");
                sql.append("         AND T2.GRADE = T1.GRADE ");
                sql.append("     WHERE ");
                sql.append("         T1.SCHREGNO = '" + schregno + "' ");
                sql.append("         AND T2.YEAR = '" + year + "' ");
                sql.append(" ) ");
                sql.append(" SELECT ");
                sql.append("     T1.SCHREGNO, ");
                sql.append("     FISCALYEAR(T1.ENT_DATE) AS ENT_YEAR, ");
                sql.append("     T4.REMARK7, ");
                sql.append("     CASE WHEN FISCALYEAR(T1.ENT_DATE) <= T4.REMARK7 THEN 1 ELSE 0 END AS NOT_USE_CLASS_MST_SPECIALDIV ");
                sql.append(" FROM ");
                sql.append("     SCHREG_ENT_GRD_HIST_DAT T1 ");
                sql.append("     INNER JOIN T_SCHOOL_KIND T2 ON T2.SCHREGNO = T1.SCHREGNO AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
                sql.append("     , CERTIF_SCHOOL_DAT T4  ");
                sql.append(" WHERE ");
                sql.append("     T4.YEAR = T2.YEAR ");
                sql.append("     AND T4.CERTIF_KINDCD = '" + certifKind + "' ");

                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    ret = rs.getString("NOT_USE_CLASS_MST_SPECIALDIV");
                }

            } catch (Exception ex) {
                log.error("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return ret;
        }

        protected boolean isGakunensei() {
            return "0".equals(_definecode.schooldiv);
        }

        //--- 内部クラス -------------------------------------------------------
        /**
         * <<クラスの説明>>。
         * @author yamasiro
         * @version $Id: 45731b901acdfdecf453db074ec7e581fcab9341 $
         */
        public class SqlStudyrec {

            public static final String abroad = "abroad";
            public static final String lhr = "lhr";
            public static final String sogo = "sogo";
            public static final String zenseki = "zenseki";
            public static final String daiken = "daiken";
            public static final String total = "total";
            public static final String tokiwahr = "tokiwahr";
            public static final String totalCredit = "totalCredit";
            public static final String e014 = "e014";

            private String _hyoutei;  // 評定の読替え  １を２と評定
            private int _stype;  // 総合的な学習の時間、留学単位、修得単位の集計区分

            private KNJDefineSchool _definecode;  // 各学校における定数等設定
            private int _daiken_div_code;  // 大検の集計方法 0:合計 1:明細
            private String _zensekiSubclassCd;  // 前籍校の成績専用科目コード
            public String _e014SubclassCd;   // 生徒の名称マスタE014に登録された科目のレコードがあるか
            private boolean _isPrintRisyu;  // 履修のみ科目出力
            private boolean _isPrintMirisyu;  // 未履修科目出力

            /**
             * 履修のみ科目出力
             * @param risyu 1:する 2:しない
             */
            public void setRisyu(final String risyu) {
                _isPrintRisyu = "1".equals(risyu);
            }

            /**
             * 未履修科目出力
             * @param mirisyu 1:する 2:しない
             */
            public void setMirisyu(final String mirisyu) {
                _isPrintMirisyu = "1".equals(mirisyu);
            }

            /**
             * 前籍校の成績専用科目コードを設定します。<br>
             * 名称マスター'E011'のコード'01'のレコード予備１をセットします。。
             * @param zensekiSubclassCd 設定する zensekiSubclassCd。
             */
            public void setZensekiSubclassCd() {
                final String str = "SELECT NAMESPARE1 FROM NAME_MST WHERE NAMECD1 = 'E011' AND NAMECD2 = '01'";
                PreparedStatement ps = null;
                ResultSet rs = null;
                try {
                    ps = db2.prepareStatement(str);
                    rs = ps.executeQuery();
                    if (rs.next() && null != rs.getString("NAMESPARE1")) {
                        _zensekiSubclassCd = rs.getString("NAMESPARE1");
                    }
                } catch (SQLException e) {
                    log.error("SQLException", e);
                } finally {
                    DbUtils.closeQuietly(null, ps, rs);
                    db2.commit();
                }
            }

            /**
             * 高等学校卒業程度認定単位（大検）の印刷方法を設定します。<br>
             * 名称マスター'E011'のコード'02'のレコードが'Y'の場合は0を以外は1を設定します。
             * @param daiken_div_code 設定する daiken_div_code。
             */
            public void setDaiken_div_code() {
                int daiken = 1;
                final String str = "SELECT NAMESPARE1 FROM NAME_MST WHERE NAMECD1 = 'E011' AND NAMECD2 = '02'";
                PreparedStatement ps = null;
                ResultSet rs = null;
                try {
                    ps = db2.prepareStatement(str);
                    rs = ps.executeQuery();
                    if (rs.next() && null != rs.getString("NAMESPARE1") && "Y".equals(rs.getString("NAMESPARE1"))) {
                        daiken = 0;
                    }
                } catch (SQLException e) {
                    log.error("SQLException", e);
                } finally {
                    DbUtils.closeQuietly(null, ps, rs);
                    db2.commit();
                }
                _daiken_div_code = daiken;
            }

            public String getStudentE014Subclass(final DB2UDB db2, final PrintData printData) {
                String e014Subclasscd = null;
                final StringBuffer stb = new StringBuffer();
                PreparedStatement ps = null;
                ResultSet rs = null;
                try {
                    stb.append(getStudyrecSqlString(printData));
                    stb.append(" SELECT ");
                    stb.append("    T1.SUBCLASSCD ");
                    stb.append(" FROM ");
                    stb.append("    STUDYREC T1 ");
                    stb.append(" INNER JOIN NAME_MST L1 ON L1.NAMECD1 = 'E014' AND L1.NAME1 = T1.SUBCLASSCD ");

                    ps = db2.prepareStatement(stb.toString());
                    rs = ps.executeQuery();
                    //名称マスタE014：登録されている科目は、明細から除外する。
                    if (rs.next() && null != rs.getString("SUBCLASSCD")) {
                        e014Subclasscd = rs.getString("SUBCLASSCD");
                    }
                } catch (SQLException e) {
                    log.error("Exception!" + stb.toString(), e);
                } finally {
                    DbUtils.closeQuietly(null, ps, rs);
                    db2.commit();
                }
                return e014Subclasscd;
            }

            /**
             * @param daiken_div_code 設定する daiken_div_code。
             */
            public void setDaiken_div_code(final int daiken_div_code) {
                _daiken_div_code = daiken_div_code;
            }

            /**
             * @param _definecode 設定する _definecode。
             */
            public void setDefinecode(final KNJDefineSchool definecode) {
                _definecode = definecode;
            }

            /**
             * @param _hyoutei 設定する _hyoutei。
             */
            public void setHyoutei(final String hyoutei) {
                _hyoutei = hyoutei;
            }

            /**
             * @param _stype 設定する _stype。
             */
            public void setStype(final int stype) {
                _stype = stype;
            }

            /**
             * 学習記録データ(全て)の SQL SELECT 文を戻します。
             * @return
             */
            public String pre_sql(final PrintData printData) {

                final boolean useYear = !isPrintGradeTitleGakunen(printData);
                final StringBuffer stb = new StringBuffer();
                // 評定１を２と判定
                String gradesString = null;
                String creditMstCreditsString = null;
                if (_hyoutei.equals("on")){ //----->評定読み替えのON/OFF  評定１を２と読み替え
                    gradesString = "CASE VALUE(T1.GRADES,0) WHEN 1 THEN 2 ELSE T1.GRADES END ";

                    // 単位数は
                    //  GET_CREDIT が 0の時、
                    //  SCHREG_STUDYREC_DAT に登録された COMP_CREDIT（履修単位）の値が0以外は COMP_CREDIT（履修単位）を GET_CREDIT として取得、
                    //  0の時は単位マスタの CREDITS を GET_CREDIT として取得して、
                    //  ADD_CREDITを加算する。
                    StringBuffer stbCredit = new StringBuffer();
                    stbCredit.append(" (CASE WHEN GRADES = 1 AND GET_CREDIT = 0 THEN ");
                    stbCredit.append("  (CASE WHEN COMP_CREDIT <> 0 THEN COMP_CREDIT ELSE CREDIT_MST_CREDIT END) ");
                    stbCredit.append("  ELSE GET_CREDIT END) ");
                    stbCredit.append(" + ADD_CREDIT ");
                    creditMstCreditsString = stbCredit.toString();
                } else{
                    gradesString = "T1.GRADES ";
                    creditMstCreditsString = "CREDIT ";
                }

                // 該当生徒の成績データ表
                stb.append(getStudyrecSqlString(printData));  // 調査書仕様の学習記録データの抽出

                stb.append(" , MAIN_T AS ( ");
                // 該当生徒の科目評定、修得単位及び教科評定平均
                stb.append("SELECT ");
                if (useYear) {
                    stb.append("   T1.YEAR AS ANNUAL");
                } else {
                    stb.append("   T1.ANNUAL AS ANNUAL");
                }
                stb.append("     , T1.CLASSCD");
                stb.append("     , T1.CLASSNAME");
                if ("1".equals(_param._useCurriculumcd)) {
                    stb.append("   , T1.SCHOOL_KIND ");
                    stb.append("   , T1.CURRICULUM_CD ");
                }
                stb.append("     , T1.SUBCLASSCD");
                stb.append("     , T1.SUBCLASSNAME");
                if (_hyoutei.equals("hyde")) {
                    stb.append(" , 0 AS GRADES, 0 AS AVG_GRADES");
                    stb.append(" , '' AS ASSESS_LEVEL");
                } else {
                    stb.append(" , " +  gradesString + " AS GRADES");
                    stb.append(" , T5.AVG_GRADES");
                    stb.append(" , '' AS ASSESS_LEVEL");
                }
                stb.append("     , T1.CREDIT AS GRADE_CREDIT");
                stb.append("     , T4.CREDIT AS CREDIT ");
                stb.append("     , T4.COMP_CREDIT");
                stb.append("     , T1.SCHOOLCD");
                stb.append("     , T1.SHOWORDERCLASS");
                stb.append("     , T1.SHOWORDERSUBCLASS");
                stb.append(" FROM (");
                // 同一年度同一科目の場合単位は合計とします。
                //「0:平均」「1:重み付け」は「評定がNULL／ゼロ以外」
                String gradesCase = "case when 0 < GRADES then GRADES end";
                String creditCase = "case when 0 < GRADES then CREDIT end";
                stb.append("  SELECT CLASSCD ");
                if ("1".equals(_param._useCurriculumcd)) {
                    stb.append("   , SCHOOL_KIND ");
                    stb.append("   , CURRICULUM_CD ");
                }
                stb.append("    , SUBCLASSCD ");
                if (useYear) {
                    stb.append("  , YEAR ");
                } else {
                    stb.append("  , ANNUAL ");
                }
                stb.append("           , GVAL_CALC");
                stb.append("           , MIN(CLASSNAME) AS CLASSNAME");
                stb.append("           , MIN(SUBCLASSNAME) AS SUBCLASSNAME");
                stb.append("           , case when COUNT(*) = 1 then MAX(GRADES)");//１レコードの場合、評定はそのままの値。
                stb.append("                  when GVAL_CALC = '0' then ROUND(AVG(FLOAT("+gradesCase+")),0)");
                stb.append("                  when GVAL_CALC = '1' and 0 < SUM("+creditCase+") then ROUND(FLOAT(SUM(("+gradesCase+")*CREDIT))/SUM("+creditCase+"),0)");
                stb.append("                  else MAX(GRADES) end AS GRADES");
                stb.append("           , SUM(CREDIT) AS CREDIT");
                stb.append("           , MIN(SCHOOLCD) AS SCHOOLCD");
                stb.append("           , MIN(SHOWORDERCLASS) AS SHOWORDERCLASS");
                stb.append("           , MIN(SHOWORDERSUBCLASS) AS SHOWORDERSUBCLASS");
                stb.append("      FROM STUDYREC");
                stb.append("      WHERE ");
                stb.append("             NOT EXISTS( ");
                stb.append("              SELECT ");
                stb.append("                'x' ");
                stb.append("              FROM ");
                stb.append("                NAME_MST E1 ");
                stb.append("              WHERE ");
                stb.append("                E1.NAMECD1 = 'E014' ");
                stb.append("                AND E1.NAME1 = SUBCLASSCD) ");
                stb.append("  GROUP BY CLASSCD ");
                if ("1".equals(_param._useCurriculumcd)) {
                    stb.append("   , SCHOOL_KIND ");
                    stb.append("   , CURRICULUM_CD ");
                }
                stb.append("       ,SUBCLASSCD ");
                if (useYear) {
                    stb.append("  ,YEAR ");
                } else {
                    stb.append("  ,ANNUAL ");
                }
                stb.append("  , GVAL_CALC");
                stb.append("     ) T1");
                //  修得単位数の計
                stb.append(" INNER JOIN (");
                stb.append("    SELECT  CLASSCD ");
                if ("1".equals(_param._useCurriculumcd)) {
                    stb.append("   , SCHOOL_KIND ");
                    stb.append("   , CURRICULUM_CD ");
                }
                stb.append("            , SUBCLASSCD, SUM(" + creditMstCreditsString + ") AS CREDIT, SUM(T1.COMP_CREDIT) AS COMP_CREDIT, MAX(CREDIT_MST_CREDIT) AS CREDIT_MST_CREDIT ");
                stb.append("    FROM STUDYREC T1");
                stb.append("    WHERE CLASSCD BETWEEN '" + KNJDefineSchool.subject_D + "' AND '" + KNJDefineSchool.subject_U + "'");
                stb.append("    GROUP BY CLASSCD ");
                if ("1".equals(_param._useCurriculumcd)) {
                    stb.append("   , SCHOOL_KIND ");
                    stb.append("   , CURRICULUM_CD ");
                }
                stb.append("           ,SUBCLASSCD");
                stb.append(" ) T4 ON T4.SUBCLASSCD = T1.SUBCLASSCD AND T4.CLASSCD = T1.CLASSCD ");
                if ("1".equals(_param._useCurriculumcd)) {
                    stb.append("   AND T4.SCHOOL_KIND = T1.SCHOOL_KIND ");
                    stb.append("   AND T4.CURRICULUM_CD = T1.CURRICULUM_CD ");
                }

                if (!(_hyoutei.equals("hyde"))) {        //----->評定の出力有無
                    //  各教科の評定平均値
                    stb.append(" LEFT JOIN (");
                    stb.append("    SELECT  CLASSCD");
                    if ("1".equals(_param._useCurriculumcd)) {
                        stb.append("   , SCHOOL_KIND ");
                        stb.append("   , CURRICULUM_CD ");
                    }
                    stb.append("          , DECIMAL(ROUND(DECIMAL(AVG(FLOAT(" + gradesString + ")),5,2),1),5,1) AS AVG_GRADES");
                    stb.append(" FROM(");
                    // 同一年度同一科目の場合評価はどちらか一方とします。
                    stb.append("  SELECT CLASSCD, SUBCLASSCD ");
                    if ("1".equals(_param._useCurriculumcd)) {
                        stb.append("   , SCHOOL_KIND ");
                        stb.append("   , CURRICULUM_CD ");
                    }
                    if (useYear) {
                        stb.append("  , YEAR ");
                    } else {
                        stb.append("  , ANNUAL ");
                    }
                    stb.append("  , GVAL_CALC");
                    stb.append("           , case when COUNT(*) = 1 then MAX(GRADES)"); //１レコードの場合、評定はそのままの値。
                    stb.append("                  when GVAL_CALC = '0' then ROUND(AVG(FLOAT(" + gradesCase + ")),0)");
                    stb.append("                  when GVAL_CALC = '1' and 0 < SUM(" + creditCase + ") then ROUND(FLOAT(SUM((" + gradesCase + ") * CREDIT)) / SUM(" + creditCase + "),0)");
                    stb.append("                  else MAX(GRADES) end AS GRADES");
                    stb.append("      FROM STUDYREC");
                    stb.append("  GROUP BY CLASSCD,SUBCLASSCD");
                    if ("1".equals(_param._useCurriculumcd)) {
                        stb.append("   , SCHOOL_KIND ");
                        stb.append("   , CURRICULUM_CD ");
                    }
                    if (useYear) {
                        stb.append("  ,YEAR");
                    } else {
                        stb.append("  ,ANNUAL");
                    }
                    stb.append("  , GVAL_CALC");
                    stb.append("     ) T1");
                    stb.append("    WHERE   CLASSCD BETWEEN '" + KNJDefineSchool.subject_D + "' AND '"+KNJDefineSchool.subject_U + "'");
                    stb.append("        AND GRADES <> 0 ");
                    stb.append("        AND NOT EXISTS ( ");
                    if ("1".equals(_param._useCurriculumcd) && "1".equals(_param._useClassDetailDat)) {
                        stb.append("              SELECT ");
                        stb.append("                'x' ");
                        stb.append("              FROM ");
                        stb.append("                SUBCLASS_DETAIL_DAT E1 ");
                        stb.append("              WHERE ");
                        stb.append("                E1.YEAR = '" + printData._year + "' ");
                        stb.append("                AND E1.SUBCLASS_SEQ = '006' ");
                        stb.append("                AND E1.CLASSCD || '-' || E1.SCHOOL_KIND || '-' || E1.CURRICULUM_CD || '-' || E1.SUBCLASSCD = ");
                        stb.append("                    T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD ");
                    } else {
                        stb.append("              SELECT ");
                        stb.append("                'x' ");
                        stb.append("              FROM ");
                        stb.append("                NAME_MST E1 ");
                        stb.append("              WHERE ");
                        stb.append("                E1.NAMECD1 = 'D020' ");
                        stb.append("                AND E1.NAME1 = T1.SUBCLASSCD ");
                    }
                    stb.append("          ) ");
                    stb.append("        AND NOT EXISTS ( ");
                    stb.append("              SELECT ");
                    stb.append("                'x' ");
                    stb.append("              FROM ");
                    stb.append("                NAME_MST E1 ");
                    stb.append("              WHERE ");
                    stb.append("                E1.NAMECD1 = 'E014' ");
                    stb.append("                AND E1.NAME1 = T1.SUBCLASSCD ");
                    stb.append("          ) ");
                    stb.append("    GROUP BY CLASSCD");
                    if ("1".equals(_param._useCurriculumcd)) {
                        stb.append("   , SCHOOL_KIND ");
                        stb.append("   , CURRICULUM_CD ");
                    }
                    stb.append(" ) T5 ON T5.CLASSCD = T1.CLASSCD");
                    if ("1".equals(_param._useCurriculumcd)) {
                        stb.append("   AND T5.SCHOOL_KIND = T1.SCHOOL_KIND ");
                        stb.append("   AND T5.CURRICULUM_CD = T1.CURRICULUM_CD ");
                    }
                }
                stb.append(" WHERE  T1.CLASSCD BETWEEN '" + KNJDefineSchool.subject_D + "' AND '" + KNJDefineSchool.subject_U + "' ");

                // 総合学習の修得単位数（合計または学年別）
                stb.append(" UNION SELECT ");
                if (useYear) {
                    stb.append("    YEAR AS ANNUAL");
                } else {
                    stb.append("    ANNUAL AS ANNUAL");
                }
                stb.append("      , '" + KNJDefineSchool.subject_T + "' AS CLASSCD");
                stb.append("      , '" + sogo + "' AS CLASSNAME");
                if ("1".equals(_param._useCurriculumcd)) {
                    stb.append("  , '" + KNJDefineSchool.subject_T + "' AS SCHOOL_KIND");
                    stb.append("  , '" + KNJDefineSchool.subject_T + "' AS CURRICULUM_CD");
                }
                stb.append("      , '" + KNJDefineSchool.subject_T + "01' AS SUBCLASSCD");
                stb.append("      , '" + sogo + "' AS SUBCLASSNAME");
                stb.append("      , 0 AS GRADES");
                stb.append("      , 0 AS AVG_GRADES");
                stb.append("      , '' AS ASSESS_LEVEL");
                stb.append("      , 0 AS GRADE_CREDIT");
                stb.append("      , SUM(CREDIT) AS CREDIT");
                stb.append("      , SUM(COMP_CREDIT) AS COMP_CREDIT");
                stb.append("      , '0' AS SCHOOLCD ");
                stb.append("      , 0 AS SHOWORDERCLASS ");
                stb.append("      , 0 AS SHOWORDERSUBCLASS ");
                stb.append(" FROM   STUDYREC T1");
                stb.append(" WHERE  T1.CLASSCD = '"+KNJDefineSchool.subject_T+"'");
                if (null != _e014SubclassCd) {
                    stb.append(" AND T1.SUBCLASSCD <> '" + _e014SubclassCd + "'");
                }
                if (useYear) {
                    stb.append(" GROUP BY YEAR ");
                } else {
                    stb.append(" GROUP BY ANNUAL ");
                }

                // 留学中の修得単位数（学年別）
                stb.append(pre_sqlAbraod(printData));

                if (_param._isTokiwa) {
                    // 常盤ホームルーム(教科コード94)
                    if (1 < _stype) {
                        stb.append(" UNION SELECT ");
                        stb.append("     ANNUAL,'XX' AS CLASSCD,'" + tokiwahr + "' AS CLASSNAME, ");
                        if ("1".equals(_param._useCurriculumcd)) {
                            stb.append("  '" + tokiwahr + "' AS SCHOOL_KIND, ");
                            stb.append("  '" + tokiwahr + "' AS CURRICULUM_CD, ");
                        }
                        stb.append("      '" + tokiwahr + "' AS SUBCLASSCD,");
                        stb.append("      '" + tokiwahr + "' AS SUBCLASSNAME,");
                        stb.append("         0 AS GRADES,");
                        stb.append("         0 AS AVG_GRADES,");
                        stb.append("         '' AS ASSESS_LEVEL,");
                        stb.append("         0 AS GRADE_CREDIT,");
                        stb.append("         T1.CREDIT AS CREDIT ");
                        stb.append("       , T1.COMP_CREDIT AS COMP_CREDIT");
                        stb.append("       ,'0' AS SCHOOLCD ");
                        stb.append("       ,0 AS SHOWORDERCLASS ");  // 表示順教科
                        stb.append("       ,0 AS SHOWORDERSUBCLASS ");  // 表示順科目
                        stb.append(" FROM STUDYREC T1 ");
                        stb.append(" WHERE ");
                        stb.append("   CLASSCD = '94' ");
                    } else {
                        stb.append(" UNION SELECT ");
                        stb.append("     '0' AS ANNUAL,'XX' AS CLASSCD,'" + tokiwahr + "' AS CLASSNAME, ");
                        if ("1".equals(_param._useCurriculumcd)) {
                            stb.append("  '" + tokiwahr + "' AS SCHOOL_KIND, ");
                            stb.append("  '" + tokiwahr + "' AS CURRICULUM_CD, ");
                        }
                        stb.append("      '" + tokiwahr + "' AS SUBCLASSCD,");
                        stb.append("      '" + tokiwahr + "' AS SUBCLASSNAME,");
                        stb.append("         0 AS GRADES,");
                        stb.append("         0 AS AVG_GRADES,");
                        stb.append("         '' AS ASSESS_LEVEL,");
                        stb.append("         0 AS GRADE_CREDIT,");
                        stb.append("         SUM(T1.CREDIT) AS CREDIT ");
                        stb.append("       , SUM(T1.COMP_CREDIT) AS COMP_CREDIT");
                        stb.append("       ,'0' AS SCHOOLCD ");
                        stb.append("       ,0 AS SHOWORDERCLASS ");  // 表示順教科
                        stb.append("       ,0 AS SHOWORDERSUBCLASS ");  // 表示順科目
                        stb.append(" FROM STUDYREC T1 ");
                        stb.append(" WHERE ");
                        stb.append("   CLASSCD = '94' ");
                    }
                }
                if (1 == _stype) {
                    // 全体の評定平均値
                    stb.append(" UNION SELECT ");
                    stb.append("     '0' AS ANNUAL,'ZZ' AS CLASSCD,'" + total + "' AS CLASSNAME, ");
                    if ("1".equals(_param._useCurriculumcd)) {
                        stb.append("  '" + total + "' AS SCHOOL_KIND, ");
                        stb.append("  '" + total + "' AS CURRICULUM_CD, ");
                    }
                    stb.append("     'ZZZZ' AS SUBCLASSCD,");
                    stb.append("     '" + total + "' AS SUBCLASSNAME,");
                    stb.append("         0 AS GRADES,");

                    if ("hyde".equals(_hyoutei)) {           //----->評定の出力有無
                        stb.append("     0 AS AVG_GRADES,");
                        stb.append("     '' AS ASSESS_LEVEL,");
                    } else{
                        stb.append("     ROUND(DECIMAL(AVG(FLOAT(" + gradesString + ")),5,2),1) AS AVG_GRADES,");
                        stb.append("     (SELECT    ST2.ASSESSMARK ");
                        stb.append("      FROM      ASSESS_MST ST2 ");
                        stb.append("      WHERE     ST2.ASSESSCD='4' ");
                        stb.append("                 AND DECIMAL(ROUND(DECIMAL(AVG(FLOAT(" + gradesString + ")),5,2),1),5,1) ");
                        stb.append("                         BETWEEN ST2.ASSESSLOW AND ST2.ASSESSHIGH) AS ASSESS_LEVEL,");
                    }
                    stb.append("         0 AS GRADE_CREDIT,");
                    stb.append("         0 AS CREDIT ");
                    stb.append("      ,  0 AS COMP_CREDIT");
                    stb.append("       ,'0' AS SCHOOLCD ");
                    stb.append("        ,0 AS SHOWORDERCLASS ");  // 表示順教科
                    stb.append("        ,0 AS SHOWORDERSUBCLASS ");  // 表示順科目
                    stb.append(" FROM(");
                    stb.append("  SELECT SCHREGNO, CLASSCD ");
                    if ("1".equals(_param._useCurriculumcd)) {
                        stb.append("  , SCHOOL_KIND");
                        stb.append("  , CURRICULUM_CD");
                    }
                    stb.append("       , SUBCLASSCD ");
                    if (useYear) {
                        stb.append("  , YEAR ");
                    } else {
                        stb.append("  , ANNUAL ");
                    }
                    stb.append("           , GVAL_CALC");
                    stb.append("           , case when COUNT(*) = 1 then MAX(GRADES)");//１レコードの場合、評定はそのままの値。
                    stb.append("                  when GVAL_CALC = '0' then ROUND(AVG(FLOAT(" + gradesCase + ")),0)");
                    stb.append("                  when GVAL_CALC = '1' and 0 < SUM(" + creditCase + ") then ROUND(FLOAT(SUM((" + gradesCase + ") * CREDIT)) / SUM(" + creditCase + "), 0)");
                    stb.append("                  else MAX(GRADES) end AS GRADES");
                    stb.append("      FROM STUDYREC");
                    stb.append("      WHERE ");
                    stb.append("          GRADES <> 0 ");
                    stb.append("          AND NOT EXISTS ( ");
                    if ("1".equals(_param._useCurriculumcd) && "1".equals(_param._useClassDetailDat)) {
                        stb.append("              SELECT ");
                        stb.append("                'x' ");
                        stb.append("              FROM ");
                        stb.append("                SUBCLASS_DETAIL_DAT E1 ");
                        stb.append("              WHERE ");
                        stb.append("                E1.YEAR = '" + printData._year + "' ");
                        stb.append("                AND E1.SUBCLASS_SEQ = '006' ");
                        stb.append("                AND E1.CLASSCD || '-' || E1.SCHOOL_KIND || '-' || E1.CURRICULUM_CD || '-' || E1.SUBCLASSCD = ");
                        stb.append("                    STUDYREC.CLASSCD || '-' || STUDYREC.SCHOOL_KIND || '-' || STUDYREC.CURRICULUM_CD || '-' || STUDYREC.SUBCLASSCD ");
                    } else {
                        stb.append("              SELECT ");
                        stb.append("                'x' ");
                        stb.append("              FROM ");
                        stb.append("                NAME_MST E1 ");
                        stb.append("              WHERE ");
                        stb.append("                E1.NAMECD1 = 'D020' ");
                        stb.append("                AND E1.NAME1 = STUDYREC.SUBCLASSCD ");
                    }
                    stb.append("          ) ");
                    stb.append("          AND NOT EXISTS ( ");
                    stb.append("              SELECT ");
                    stb.append("                'x' ");
                    stb.append("              FROM ");
                    stb.append("                NAME_MST E1 ");
                    stb.append("              WHERE ");
                    stb.append("                E1.NAMECD1 = 'E014' ");
                    stb.append("                AND E1.NAME1 = STUDYREC.SUBCLASSCD ");
                    stb.append("          ) ");
                    stb.append("  GROUP BY SCHREGNO,CLASSCD ");
                    if ("1".equals(_param._useCurriculumcd)) {
                        stb.append("  , SCHOOL_KIND");
                        stb.append("  , CURRICULUM_CD");
                    }
                    stb.append("         , SUBCLASSCD ");
                    if (useYear) {
                        stb.append("  ,YEAR ");
                    } else {
                        stb.append("  ,ANNUAL ");
                    }
                    stb.append("  , GVAL_CALC");
                    stb.append("     ) T1");
                    stb.append(" WHERE CLASSCD BETWEEN '" + KNJDefineSchool.subject_D + "' AND '" + KNJDefineSchool.subject_U + "' ");

                    // 全体の修得単位数
                    stb.append(" UNION SELECT ");
                    stb.append("     '0' AS ANNUAL,'XX' AS CLASSCD,'" + totalCredit + "' AS CLASSNAME, ");
                    if ("1".equals(_param._useCurriculumcd)) {
                        stb.append("  '" + totalCredit + "' AS SCHOOL_KIND, ");
                        stb.append("  '" + totalCredit + "' AS CURRICULUM_CD, ");
                    }
                    stb.append("     'XXXX' AS SUBCLASSCD,");
                    stb.append("     '" + totalCredit + "' AS SUBCLASSNAME,");
                    stb.append("         0 AS GRADES,");
                    stb.append("         0 AS AVG_GRADES,");
                    stb.append("         '' AS ASSESS_LEVEL,");
                    stb.append("         0 AS GRADE_CREDIT,");
                    stb.append("         SUM(T1.CREDIT) AS CREDIT ");
                    stb.append("      , SUM(T1.COMP_CREDIT) AS COMP_CREDIT");
                    stb.append("       ,'0' AS SCHOOLCD ");
                    stb.append("        ,0 AS SHOWORDERCLASS ");  // 表示順教科
                    stb.append("        ,0 AS SHOWORDERSUBCLASS ");  // 表示順科目
                    stb.append(" FROM (");
                    stb.append("  SELECT SCHREGNO, CLASSCD ");
                    if ("1".equals(_param._useCurriculumcd)) {
                        stb.append("  , SCHOOL_KIND");
                        stb.append("  , CURRICULUM_CD");
                    }
                    stb.append("  , SUBCLASSCD ");
                    if (useYear) {
                        stb.append("  , YEAR");
                    } else {
                        stb.append("  , ANNUAL");
                    }
                    stb.append("  , GVAL_CALC");
                    stb.append("           , case when COUNT(*) = 1 then MAX(GRADES)");//１レコードの場合、評定はそのままの値。
                    stb.append("                  when GVAL_CALC = '0' then ROUND(AVG(FLOAT(" + gradesCase + ")),0)");
                    stb.append("                  when GVAL_CALC = '1' and 0 < SUM(" + creditCase + ") then ROUND(FLOAT(SUM((" + gradesCase + ") * CREDIT)) / SUM(" + creditCase + "), 0)");
                    stb.append("                  else MAX(GRADES) end AS GRADES");
                    stb.append("           , SUM(" + creditMstCreditsString + ") AS CREDIT");
                    stb.append("           , SUM(COMP_CREDIT) AS COMP_CREDIT");
                    stb.append("      FROM STUDYREC");
                    stb.append("      WHERE ");
                    stb.append("          NOT EXISTS( ");
                    if ("1".equals(_param._useCurriculumcd) && "1".equals(_param._useClassDetailDat)) {
                        stb.append("              SELECT ");
                        stb.append("                'x' ");
                        stb.append("              FROM ");
                        stb.append("                SUBCLASS_DETAIL_DAT E1 ");
                        stb.append("              WHERE ");
                        stb.append("                E1.YEAR = '" + printData._year + "' ");
                        stb.append("                AND E1.SUBCLASS_SEQ = '006' ");
                        stb.append("                AND E1.CLASSCD || '-' || E1.SCHOOL_KIND || '-' || E1.CURRICULUM_CD || '-' || E1.SUBCLASSCD = ");
                        stb.append("                    STUDYREC.CLASSCD || '-' || STUDYREC.SCHOOL_KIND || '-' || STUDYREC.CURRICULUM_CD || '-' || STUDYREC.SUBCLASSCD ");
                    } else {
                        stb.append("              SELECT ");
                        stb.append("                'x' ");
                        stb.append("              FROM ");
                        stb.append("                NAME_MST E1 ");
                        stb.append("              WHERE ");
                        stb.append("                E1.NAMECD1 = 'D020' ");
                        stb.append("                AND E1.NAME1 = STUDYREC.SUBCLASSCD ");
                    }
                    stb.append("          ) ");
                    stb.append("          AND NOT EXISTS( ");
                    stb.append("              SELECT ");
                    stb.append("                'x' ");
                    stb.append("              FROM ");
                    stb.append("                NAME_MST E1 ");
                    stb.append("              WHERE ");
                    stb.append("                E1.NAMECD1 = 'E014' ");
                    stb.append("                AND E1.NAME1 = STUDYREC.SUBCLASSCD ");
                    stb.append("          ) ");
                    stb.append("  GROUP BY SCHREGNO, CLASSCD ");
                    if ("1".equals(_param._useCurriculumcd)) {
                        stb.append("  , SCHOOL_KIND");
                        stb.append("  , CURRICULUM_CD");
                    }
                    stb.append("  ,SUBCLASSCD ");
                    if (useYear) {
                        stb.append("  ,YEAR ");
                    } else {
                        stb.append("  ,ANNUAL ");
                    }
                    stb.append("  , GVAL_CALC");
                    stb.append("     ) T1");
                    stb.append(" WHERE CLASSCD BETWEEN '" + KNJDefineSchool.subject_D + "' AND '" + KNJDefineSchool.subject_U + "' ");
                }

                // 前籍校における修得単位（レコードがある場合のみ）
                if (null != _zensekiSubclassCd) {
                    stb.append(" UNION SELECT");
                    stb.append("      '0' AS ANNUAL");
                    stb.append("    , 'ZB' AS CLASSCD");
                    stb.append("    , '" + zenseki + "' AS CLASSNAME");
                    if ("1".equals(_param._useCurriculumcd)) {
                        stb.append("  ,'" + zenseki + "' AS SCHOOL_KIND");
                        stb.append("  ,'" + zenseki + "' AS CURRICULUM_CD");
                    }
                    stb.append("    , 'ZZZB' AS SUBCLASSCD");
                    stb.append("    , '" + zenseki + "' AS SUBCLASSNAME");
                    stb.append("    , 0 AS GRADES");
                    stb.append("    , 0 AS AVG_GRADES");
                    stb.append("    , '' ASSESS_LEVEL");
                    stb.append("    , 0 AS GRADE_CREDIT");
                    stb.append("    , S1.CREDIT ");
                    stb.append("    , S1.COMP_CREDIT ");
                    stb.append("    , '1' AS SCHOOLCD ");
                    stb.append("    , 0 AS SHOWORDERCLASS ");  // 表示順教科
                    stb.append("    , 0 AS SHOWORDERSUBCLASS ");  // 表示順科目
                    stb.append(" FROM (");
                    stb.append("      SELECT SCHREGNO,SUM(T1.CREDIT ) AS CREDIT,SUM(T1.COMP_CREDIT ) AS COMP_CREDIT ");
                    stb.append("      FROM (");
                    stb.append("           SELECT T1.SCHREGNO, CREDIT, COMP_CREDIT");
                    stb.append("           FROM STUDYREC_DAT T1");
                    stb.append("           WHERE ((T1.SCHOOLCD = '1' OR T1.YEAR = '0') AND T1.SUBCLASSCD = '" + _zensekiSubclassCd + "')");
                    stb.append("      ) T1");
                    stb.append("      GROUP BY T1.SCHREGNO");
                    stb.append("      HAVING T1.SCHREGNO IS NOT NULL");
                    stb.append(" ) S1");
                }

                // 名称マスタE014が設定されている科目の修得単位（レコードがある場合のみ）
                if (null != _e014SubclassCd) {
                    stb.append(" UNION SELECT");
                    if (useYear) {
                        stb.append("    YEAR AS ANNUAL");
                    } else {
                        stb.append("    ANNUAL AS ANNUAL");
                    }
                    stb.append("      , '" + _e014SubclassCd + "' AS CLASSCD");
                    stb.append("      , '" + e014 + "' AS CLASSNAME");
                    if ("1".equals(_param._useCurriculumcd)) {
                        stb.append("  , '" + e014 + "' AS SCHOOL_KIND");
                        stb.append("  , '" + e014 + "' AS CURRICULUM_CD");
                    }
                    stb.append("      , '" + _e014SubclassCd + "' AS SUBCLASSCD");
                    stb.append("      , MAX(SUBCLASSNAME) AS SUBCLASSNAME");
                    stb.append("      , 0 AS GRADES");
                    stb.append("      , 0 AS AVG_GRADES");
                    stb.append("      , '' AS ASSESS_LEVEL");
                    stb.append("      , 0 AS GRADE_CREDIT");
                    stb.append("      , SUM(CREDIT) AS CREDIT");
                    stb.append("      , SUM(COMP_CREDIT) AS COMP_CREDIT");
                    stb.append("      , '0' AS SCHOOLCD ");
                    stb.append("      , 0 AS SHOWORDERCLASS ");
                    stb.append("      , 0 AS SHOWORDERSUBCLASS ");
                    stb.append(" FROM   STUDYREC T1");
                    stb.append(" WHERE  T1.SUBCLASSCD = '" + _e014SubclassCd + "'");
                    if (useYear) {
                        stb.append(" GROUP BY YEAR ");
                    } else {
                        stb.append(" GROUP BY ANNUAL ");
                    }
                }

                // 大検における認定単位（レコードがある場合のみ）
                if (0 == _daiken_div_code) {
                    stb.append(" UNION SELECT");
                    stb.append("      '0' AS ANNUAL");
                    stb.append("    , 'ZA' AS CLASSCD");
                    stb.append("    , '" + daiken + "' AS CLASSNAME");
                    if ("1".equals(_param._useCurriculumcd)) {
                        stb.append("  , '" + daiken + "' AS SCHOOL_KIND");
                        stb.append("  , '" + daiken + "' AS CURRICULUM_CD");
                    }
                    stb.append("    , 'ZZZA' AS SUBCLASSCD");
                    stb.append("    , '" + daiken + "' AS SUBCLASSNAME");
                    stb.append("    , 0 AS GRADES");
                    stb.append("    , 0 AS AVG_GRADES");
                    stb.append("    , '' ASSESS_LEVEL");
                    stb.append("    , 0 AS GRADE_CREDIT");
                    stb.append("    , S1.CREDIT ");
                    stb.append("    , S1.COMP_CREDIT ");
                    stb.append("    , ' 2' AS SCHOOLCD ");
                    stb.append("    , 0 AS SHOWORDERCLASS ");  // 表示順教科
                    stb.append("    , 0 AS SHOWORDERSUBCLASS ");  // 表示順科目
                    stb.append(" FROM (");
                    stb.append("      SELECT SCHREGNO,SUM(T1.CREDIT ) AS CREDIT,SUM(T1.COMP_CREDIT ) AS COMP_CREDIT ");
                    stb.append("      FROM (");
                    stb.append("           SELECT T1.SCHREGNO, CREDIT, COMP_CREDIT");
                    stb.append("           FROM STUDYREC_DAT T1");
                    stb.append("           WHERE T1.SCHOOLCD = '2'");
                    stb.append("      ) T1");
                    stb.append("      GROUP BY T1.SCHREGNO");
                    stb.append("      HAVING T1.SCHREGNO IS NOT NULL");
                    stb.append(" ) S1");
                }

                stb.append(" ) ");

                stb.append(" SELECT");
                stb.append("    T1.ANNUAL, ");
                stb.append("    T1.CLASSCD, ");
                stb.append("    T1.CLASSNAME, ");
                if ("1".equals(_param._useCurriculumcd)) {
                    stb.append("  T1.SCHOOL_KIND, ");
                    stb.append("  T1.CURRICULUM_CD, ");
                }
                stb.append("    T1.SUBCLASSCD, ");
                if ("1".equals(_param._useCurriculumcd) && "1".equals(_param._useClassDetailDat)) {
                    stb.append("    L1.CLASSCD || '-' || L1.SCHOOL_KIND || '-' || L1.CURRICULUM_CD || '-' || L1.SUBCLASSCD AS D020, ");
                } else {
                    stb.append("    L1.NAME1 AS D020, ");
                }
                stb.append("    L3.NAME1 AS E014, ");
                stb.append("    T1.SUBCLASSNAME, ");
                stb.append("    T1.GRADES, ");
                stb.append("    T1.AVG_GRADES, ");
                stb.append("    T1.ASSESS_LEVEL, ");
                stb.append("    T1.GRADE_CREDIT, ");
                stb.append("    T1.CREDIT, ");
                stb.append("    T1.COMP_CREDIT, ");
                stb.append("    T1.SCHOOLCD, ");
                stb.append("    T1.SHOWORDERCLASS, ");
                stb.append("    T1.SHOWORDERSUBCLASS, ");
                stb.append("    VALUE(L2.SPECIALDIV, '0') AS SPECIALDIV ");
                stb.append(" FROM ");
                stb.append("    MAIN_T T1 ");
                if ("1".equals(_param._useCurriculumcd) && "1".equals(_param._useClassDetailDat)) {
                    stb.append("    LEFT JOIN SUBCLASS_DETAIL_DAT L1 ON L1.YEAR = '" + printData._year + "' AND L1.SUBCLASS_SEQ = '006' ");
                    stb.append("         AND L1.CLASSCD || '-' || L1.SCHOOL_KIND || '-' || L1.CURRICULUM_CD || '-' || L1.SUBCLASSCD = ");
                    stb.append("             T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD ");
                } else {
                    stb.append("    LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'D020' ");
                    stb.append("         AND T1.SUBCLASSCD = L1.NAME1 ");
                }
                stb.append("    LEFT JOIN CLASS_MST L2 ON L2.CLASSCD = T1.CLASSCD ");
                if ("1".equals(_param._useCurriculumcd)) {
                    stb.append("  AND L2.SCHOOL_KIND = T1.SCHOOL_KIND ");
                }
                stb.append("    LEFT JOIN NAME_MST L3 ON L3.NAMECD1 = 'E014' ");
                stb.append("         AND T1.SUBCLASSCD = L3.NAME1 ");

                stb.append(" ORDER BY ");
                if (!"1".equals(_param._notUseClassMstSpecialDiv)) {
                    stb.append("    VALUE(L2.SPECIALDIV, '0'), ");
                }
                stb.append("    SHOWORDERCLASS, ");
                stb.append("    CLASSCD, ");
                stb.append("    SHOWORDERSUBCLASS, ");
                stb.append("    SUBCLASSCD, ");
                if ("1".equals(_param._useCurriculumcd)) {
                    stb.append("  SCHOOL_KIND, ");
                    stb.append("  CURRICULUM_CD, ");
                }
                stb.append("    ANNUAL");
                return stb.toString();
            }

            /**
             * 留学の SQL SELECT 文を戻します。
             * @return
             */
            private String pre_sqlAbraod(final PrintData printData) {
                final boolean useYear = !isPrintGradeTitleGakunen(printData);

                final StringBuffer stb = new StringBuffer();
                stb.append("         UNION SELECT ");
                if (1 == _stype) {
                    stb.append("          '0' AS ANNUAL");
                } else {
                    if (useYear) {
                        stb.append("      YEAR AS ANNUAL");
                    } else {
                        stb.append("      ANNUAL AS ANNUAL");
                    }
                }
                stb.append("            , 'AA' AS CLASSCD");
                stb.append("            , '" + abroad + "' AS CLASSNAME");
                if ("1".equals(_param._useCurriculumcd)) {
                    stb.append("  , '" + abroad + "' AS SCHOOL_KIND");
                    stb.append("  , '" + abroad + "' AS CURRICULUM_CD");
                }
                stb.append("            , 'AAAA' AS SUBCLASSCD");
                stb.append("            , '" + abroad + "' AS SUBCLASSNAME");
                stb.append("            , 0 AS GRADES,0 AS AVG_GRADES");
                stb.append("            , '' AS ASSESS_LEVEL");
                stb.append("            , 0 AS GRADE_CREDIT");
                stb.append("            , SUM(ABROAD_CREDITS) AS CREDIT ");
                stb.append("            , SUM(ABROAD_CREDITS) AS COMP_CREDIT ");
                stb.append("            , '0' AS SCHOOLCD ");
                stb.append("            , 0 AS SHOWORDERCLASS ");
                stb.append("            , 0 AS SHOWORDERSUBCLASS ");
                stb.append("         FROM(");
                stb.append("              SELECT ABROAD_CREDITS, INT(FISCALYEAR(TRANSFER_SDATE)) AS TRANSFER_YEAR ");
                stb.append("              FROM SCHREG_TRANSFER_DAT ");
                stb.append("              WHERE SCHREGNO = '" + printData._schregno + "' AND TRANSFERCD = '1' ");
                stb.append("         )ST1");
                if (1 == _stype) {
                    stb.append("     WHERE TRANSFER_YEAR <= " + printData._year + " ");
                } else {
                    stb.append("   , (");
                    if (useYear) {
                        stb.append("     SELECT YEAR ");
                    } else {
                        stb.append("     SELECT ANNUAL, MAX(YEAR) AS YEAR ");
                    }
                    stb.append("         FROM SCHREG_REGD_DAT ");
                    stb.append("         WHERE SCHREGNO = '" + printData._schregno + "' AND YEAR <= '" + printData._year + "' ");
                    if (useYear) {
                        stb.append("     GROUP BY YEAR ");
                    } else {
                        stb.append("     GROUP BY ANNUAL ");
                    }
                    stb.append("     )ST2 ");
                    stb.append("     WHERE ST1.TRANSFER_YEAR <= " + printData._year + " AND INTEGER(ST2.YEAR) = ST1.TRANSFER_YEAR ");
                    if (useYear) {
                        stb.append(" GROUP BY YEAR ");
                    } else {
                        stb.append(" GROUP BY ANNUAL ");
                    }
                }
                return stb.toString();
            }

            /**
             * 学習記録データの SQL SELECT 文を戻します。
             * @return
             */
            protected String getStudyrecSqlString(final PrintData printData) {
                final StringBuffer stb = new StringBuffer();
                stb.append(" WITH");
                if (isPrintGradeTitleGakunen(printData)) {
                    stb.append(" DROP_YEAR AS(");
                    stb.append("        SELECT DISTINCT YEAR");
                    stb.append("        FROM SCHREG_REGD_DAT T1");
                    stb.append("        WHERE SCHREGNO = '" + printData._schregno + "' ");
                    stb.append("        AND T1.YEAR NOT IN (SELECT MAX(YEAR) FROM SCHREG_REGD_DAT T2 WHERE SCHREGNO = '" + printData._schregno + "' AND YEAR <= '" + printData._year + "' GROUP BY GRADE)");
                    stb.append(" ),");
                }
                stb.append(" MAX_SEMESTER0 AS (SELECT YEAR, SCHREGNO, MAX(SEMESTER) AS SEMESTER ");
                stb.append("   FROM SCHREG_REGD_DAT WHERE SCHREGNO = '" + printData._schregno + "' ");
                stb.append("   GROUP BY YEAR, SCHREGNO ");
                stb.append(" ), ");

                stb.append(" STUDYREC_DAT AS(");
                stb.append("        SELECT  T1.SCHREGNO");
                stb.append("              , CASE WHEN INT(T1.YEAR) = 0 THEN '0' ELSE T1.YEAR END AS YEAR");
                stb.append("              , CASE WHEN INT(T1.ANNUAL) = 0 THEN '0' ELSE T1.ANNUAL END AS ANNUAL");
                stb.append("              , T1.SCHOOLCD");
                stb.append("              , VALUE(T1.VALUATION, 0) AS GRADES");
                stb.append("              , CASE WHEN T1.ADD_CREDIT IS NOT NULL THEN VALUE(T1.GET_CREDIT,0) + VALUE(T1.ADD_CREDIT,0)");
                stb.append("                     ELSE T1.GET_CREDIT END AS CREDIT");
                stb.append("              , VALUE(T1.COMP_CREDIT, 0) AS COMP_CREDIT");
                stb.append("              , VALUE(T1.GET_CREDIT, 0) AS GET_CREDIT");
                stb.append("              , VALUE(T1.ADD_CREDIT, 0) AS ADD_CREDIT");
                stb.append("              , T1.CLASSCD, T1.SUBCLASSCD");
                if ("1".equals(_param._useCurriculumcd)) {
                    stb.append("              , T1.SCHOOL_KIND");
                    stb.append("              , T1.CURRICULUM_CD");
                }
                stb.append("              , T1.CLASSNAME, T1.SUBCLASSNAME");
                stb.append("              , VALUE(T4.CREDITS, 0) AS CREDIT_MST_CREDIT ");
                stb.append("        FROM  SCHREG_STUDYREC_DAT T1");
                stb.append("        LEFT JOIN MAX_SEMESTER0 T2 ON T2.YEAR = T1.YEAR AND T2.SCHREGNO = T1.SCHREGNO ");
                stb.append("        LEFT JOIN SCHREG_REGD_DAT T3 ON T3.YEAR = T2.YEAR ");
                stb.append("           AND T3.SEMESTER = T2.SEMESTER ");
                stb.append("           AND T3.SCHREGNO = T2.SCHREGNO ");
                stb.append("        LEFT JOIN CREDIT_MST T4 ON T4.YEAR = T3.YEAR ");
                stb.append("           AND T4.CLASSCD = T1.CLASSCD ");
                if ("1".equals(_param._useCurriculumcd)) {
                    stb.append("           AND T4.SCHOOL_KIND = T1.SCHOOL_KIND ");
                    stb.append("           AND T4.CURRICULUM_CD = T1.CURRICULUM_CD ");
                }
                stb.append("           AND T4.SUBCLASSCD = T1.SUBCLASSCD ");
                stb.append("           AND T4.COURSECD = T3.COURSECD ");
                stb.append("           AND T4.MAJORCD = T3.MAJORCD ");
                stb.append("           AND T4.COURSECODE = T3.COURSECODE ");
                stb.append("           AND T4.GRADE = T3.GRADE ");
                stb.append("        WHERE  T1.SCHREGNO = '" + printData._schregno + "' ");
                stb.append("           AND T1.YEAR <= '" + printData._year + "' ");
                stb.append("       AND (T1.CLASSCD BETWEEN '" + KNJDefineSchool.subject_D + "' AND '" + KNJDefineSchool.subject_U+"' OR T1.CLASSCD = '" + KNJDefineSchool.subject_T + "' OR T1.CLASSCD = '94')");
                if ("1".equals(_param._tyousasyoNotPrintAnotherStudyrec)) {
                    stb.append("       AND T1.SCHOOLCD <> '1' ");
                }
                if (_param._isChuKouIkkan) {
                    stb.append("       AND 4 <= INT(T1.ANNUAL) ");
                }
                if (isPrintGradeTitleGakunen(printData)) {
                    stb.append("       AND T1.YEAR NOT IN(SELECT YEAR FROM DROP_YEAR)");
                }
                stb.append("           AND value(T1.PRINT_FLG, '0') NOT IN('1')");//印刷有無フラグが１のレコードは印刷しない。
                stb.append(" )");

                stb.append(",SUBCLASSGROUP AS(");
                stb.append("        SELECT ");
                if ("1".equals(_param._useCurriculumcd)) {
                    stb.append("                T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD,");
                }
                stb.append("                T1.SUBCLASSCD, T2.SUBCLASSCD2");
                stb.append("              , T4.CLASSNAME, T4.CLASSORDERNAME1");
                stb.append("              , T4.SHOWORDER2 AS SHOWORDERCLASS");
                stb.append("              , T3.SUBCLASSNAME, T3.SUBCLASSORDERNAME1");
                stb.append("              , T3.SHOWORDER2 AS SHOWORDERSUBCLASS");
                stb.append("        FROM ");
                if ("1".equals(_param._useCurriculumcd)) {
                    stb.append("        (SELECT CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD FROM STUDYREC_DAT GROUP BY CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD) T1");
                } else {
                    stb.append("        (SELECT SUBCLASSCD FROM STUDYREC_DAT GROUP BY SUBCLASSCD) T1");
                }
                stb.append("            INNER JOIN SUBCLASS_MST T2 ON T1.SUBCLASSCD = T2.SUBCLASSCD");
                if ("1".equals(_param._useCurriculumcd)) {
                    stb.append("           AND T1.SCHOOL_KIND = T2.SCHOOL_KIND");
                    stb.append("           AND T1.CURRICULUM_CD = T2.CURRICULUM_CD");
                    stb.append("           AND T1.CLASSCD = T2.CLASSCD");
                }
                stb.append("               AND T2.SUBCLASSCD2 IS NOT NULL");
                stb.append("            INNER JOIN SUBCLASS_MST T3 ON T2.SUBCLASSCD2 = T3.SUBCLASSCD ");
                if ("1".equals(_param._useCurriculumcd)) {
                    stb.append("           AND T2.SCHOOL_KIND = T3.SCHOOL_KIND");
                    stb.append("           AND T2.CURRICULUM_CD = T3.CURRICULUM_CD");
                    stb.append("           AND T2.CLASSCD = T3.CLASSCD");
                }
                stb.append("            INNER JOIN CLASS_MST T4 ON ");
                if ("1".equals(_param._useCurriculumcd)) {
                    stb.append("           T2.CLASSCD = T4.CLASSCD AND T2.SCHOOL_KIND = T4.SCHOOL_KIND");
                } else {
                    stb.append("           SUBSTR(T2.SUBCLASSCD,1,2) = T4.CLASSCD");
                }
                stb.append(" )");

                stb.append(",STUDYREC_SUBCLASSGROUP AS(");
                stb.append("        SELECT ");
                if ("1".equals(_param._useCurriculumcd)) {
                    stb.append("    T2.CLASSCD, T2.SCHOOL_KIND, T2.CURRICULUM_CD,");
                }
                stb.append("        T2.SUBCLASSCD");
                stb.append("              , MIN(T2.CLASSNAME) AS CLASSNAME");
                stb.append("              , MIN(T2.SUBCLASSNAME) AS SUBCLASSNAME");
                stb.append("        FROM  SUBCLASSGROUP T1, STUDYREC_DAT T2");
                stb.append("        WHERE  T1.SUBCLASSCD2 = T2.SUBCLASSCD");
                if ("1".equals(_param._useCurriculumcd)) {
                    stb.append("        AND T1.CLASSCD = T2.CLASSCD");
                    stb.append("        AND T1.SCHOOL_KIND = T2.SCHOOL_KIND");
                    stb.append("        AND T1.CURRICULUM_CD = T2.CURRICULUM_CD");
                }
                stb.append("           AND (T2.SUBCLASSNAME IS NOT NULL OR T2.CLASSNAME IS NOT NULL)");
                stb.append("        GROUP BY ");
                if ("1".equals(_param._useCurriculumcd)) {
                    stb.append("        T2.CLASSCD, T2.SCHOOL_KIND, T2.CURRICULUM_CD, ");
                }
                stb.append("        T2.SUBCLASSCD");
                stb.append(" )");
                stb.append(",STUDYREC AS(");
                stb.append("        SELECT  T1.SCHREGNO, T1.YEAR, T1.ANNUAL, T1.SCHOOLCD");
                stb.append("              , T1.GRADES, T1.CREDIT, T1.COMP_CREDIT, T1.GET_CREDIT, T1.ADD_CREDIT");
                stb.append("              , T1.CLASSCD");
                if ("1".equals(_param._useCurriculumcd)) {
                    stb.append("          , T1.SCHOOL_KIND, T1.CURRICULUM_CD ");
                }
                stb.append("              , T1.SUBCLASSCD AS STUDYREC_SUBCLASSCD ");
                stb.append("              , VALUE(T2.SUBCLASSCD2, T1.SUBCLASSCD) AS SUBCLASSCD");
                stb.append("              , CASE WHEN T1.SCHOOLCD = '1' THEN ");
                stb.append("                  VALUE( ");
                if (_param._hasAnotherClassMst) {
                    stb.append("                        ANT4.CLASSORDERNAME1, ");
                    stb.append("                        ANT4.CLASSNAME, ");
                }
                stb.append("                        T3.CLASSNAME ");
                stb.append("                       ,T2.CLASSORDERNAME1 ");
                stb.append("                       ,T2.CLASSNAME ");
                stb.append("                       ,T1.CLASSNAME ");
                stb.append("                       ,T4.CLASSORDERNAME1 ");
                stb.append("                       ,T4.CLASSNAME) ");
                stb.append("                ELSE ");
                stb.append("                  VALUE(T3.CLASSNAME ");
                stb.append("                       ,T2.CLASSORDERNAME1 ");
                stb.append("                       ,T2.CLASSNAME ");
                stb.append("                       ,T1.CLASSNAME ");
                stb.append("                       ,T4.CLASSORDERNAME1 ");
                stb.append("                       ,T4.CLASSNAME) ");
                stb.append("                END AS CLASSNAME");
                stb.append("              , CASE WHEN T1.SCHOOLCD = '1' THEN ");
                stb.append("                  VALUE( ");
                if (_param._hasAnotherSubclassMst) {
                    stb.append("                        ANT5.SUBCLASSORDERNAME1, ");
                    stb.append("                        ANT5.SUBCLASSNAME, ");
                }
                stb.append("                        T3.SUBCLASSNAME ");
                stb.append("                       ,T2.SUBCLASSORDERNAME1 ");
                stb.append("                       ,T2.SUBCLASSNAME ");
                stb.append("                       ,T1.SUBCLASSNAME ");
                stb.append("                       ,T5.SUBCLASSORDERNAME1 ");
                stb.append("                       ,T5.SUBCLASSNAME) ");
                stb.append("                ELSE ");
                stb.append("                  VALUE(T3.SUBCLASSNAME ");
                stb.append("                       ,T2.SUBCLASSORDERNAME1 ");
                stb.append("                       ,T2.SUBCLASSNAME ");
                stb.append("                       ,T1.SUBCLASSNAME ");
                stb.append("                       ,T5.SUBCLASSORDERNAME1 ");
                stb.append("                       ,T5.SUBCLASSNAME) ");
                stb.append("                END AS SUBCLASSNAME");
                stb.append("              , VALUE(T2.SHOWORDERCLASS ");
                stb.append("                     ,T4.SHOWORDER2 ");
                stb.append("                     ,999) AS SHOWORDERCLASS");
                stb.append("              , VALUE(T2.SHOWORDERSUBCLASS ");
                stb.append("                     ,T5.SHOWORDER2 ");
                stb.append("                     ,999) AS SHOWORDERSUBCLASS");
                stb.append("              , SC.GVAL_CALC");
                stb.append("              , T4.SPECIALDIV");
                stb.append("              , T1.CREDIT_MST_CREDIT");
                stb.append("        FROM  STUDYREC_DAT T1");
                stb.append("        LEFT JOIN SUBCLASSGROUP T2 ON T2.SUBCLASSCD = T1.SUBCLASSCD");
                if ("1".equals(_param._useCurriculumcd)) {
                    stb.append("          AND T2.CLASSCD = T1.CLASSCD ");
                    stb.append("          AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
                    stb.append("          AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
                }
                stb.append("        LEFT JOIN STUDYREC_SUBCLASSGROUP T3 ON T3.SUBCLASSCD = T2.SUBCLASSCD2");
                if ("1".equals(_param._useCurriculumcd)) {
                    stb.append("          AND T3.CLASSCD = T1.CLASSCD ");
                    stb.append("          AND T3.SCHOOL_KIND = T1.SCHOOL_KIND ");
                    stb.append("          AND T3.CURRICULUM_CD = T1.CURRICULUM_CD ");
                }
                stb.append("        LEFT JOIN CLASS_MST T4 ON T4.CLASSCD = T1.CLASSCD");
                if ("1".equals(_param._useCurriculumcd)) {
                    stb.append("          AND T4.SCHOOL_KIND = T1.SCHOOL_KIND ");
                }
                stb.append("        LEFT JOIN SUBCLASS_MST T5 ON ");
                if ("1".equals(_param._useCurriculumcd)) {
                    stb.append("          T5.CLASSCD = T1.CLASSCD AND ");
                    stb.append("          T5.SCHOOL_KIND = T1.SCHOOL_KIND AND ");
                    stb.append("          T5.CURRICULUM_CD = T1.CURRICULUM_CD AND ");
                }
                stb.append("              T5.SUBCLASSCD = T1.SUBCLASSCD");
                if (_param._hasAnotherClassMst) {
                    stb.append("        LEFT JOIN ANOTHER_CLASS_MST ANT4 ON ANT4.CLASSCD = T1.CLASSCD");
                    if ("1".equals(_param._useCurriculumcd)) {
                        stb.append("          AND ANT4.SCHOOL_KIND = T1.SCHOOL_KIND ");
                    }
                }
                if (_param._hasAnotherSubclassMst) {
                    stb.append("        LEFT JOIN ANOTHER_SUBCLASS_MST ANT5 ON ANT5.SUBCLASSCD = T1.SUBCLASSCD ");
                    if ("1".equals(_param._useCurriculumcd)) {
                        stb.append("          AND ANT5.CLASSCD = T1.CLASSCD ");
                        stb.append("          AND ANT5.SCHOOL_KIND = T1.SCHOOL_KIND ");
                        stb.append("          AND ANT5.CURRICULUM_CD = T1.CURRICULUM_CD ");
                    }
                }
                stb.append("        LEFT JOIN SCHOOL_MST SC ON SC.YEAR = T1.YEAR");
                stb.append("    WHERE  (");
                if (0 == _daiken_div_code) {
                    stb.append("        T1.SCHOOLCD = '0'");
                } else {
                    stb.append("        T1.SCHOOLCD = '0'");
                    stb.append("     OR (T1.SCHOOLCD = '2' AND T1.CREDIT IS NOT NULL)");
                }
                if (null != _zensekiSubclassCd) {
                    stb.append("     OR ((T1.SCHOOLCD = '1' OR T1.YEAR = '0') AND T1.SUBCLASSCD <> '" + _zensekiSubclassCd + "')");
                } else {
                    stb.append("     OR (T1.SCHOOLCD = '1' OR T1.YEAR = '0')");
                }
                stb.append(         ")");
                //履修のみ科目出力・・・「履修のみ」とは、「修得単位がゼロ　かつ　履修単位がゼロ以外」
                if (!_isPrintRisyu) {
                    if (_hyoutei.equals("on")){ //----->評定読み替えのON/OFF  評定１を２と読み替え
                        // 単位数は
                        //  GET_CREDIT が 0の時、
                        //  SCHREG_STUDYREC_DAT に登録された COMP_CREDIT（履修単位）の値が0以外は COMP_CREDIT（履修単位）を GET_CREDIT として取得、
                        //  0の時は単位マスタの CREDITS を GET_CREDIT として取得して、
                        //  ADD_CREDITを加算する。
                        stb.append("     AND ( ");
                        stb.append("                 ((CASE WHEN GRADES = 1 AND GET_CREDIT = 0 THEN ");
                        stb.append("                   (CASE WHEN COMP_CREDIT <> 0 THEN COMP_CREDIT ELSE CREDIT_MST_CREDIT END) ");
                        stb.append("                   ELSE GET_CREDIT END) + ADD_CREDIT ");
                        stb.append("                  ) <> 0) ");
                    } else {
                        //「修得単位がゼロ　かつ　履修単位がゼロ以外」のレコードは印刷しない。
                        stb.append("     AND (T1.CREDIT <> 0) ");
                    }
                }
                if (!_isPrintMirisyu) {
                    if (_param._isTokiwa) {
                        stb.append("       AND (T1.CLASSCD BETWEEN '" + KNJDefineSchool.subject_D + "' AND '" + KNJDefineSchool.subject_U+"' OR T1.CLASSCD = '" + KNJDefineSchool.subject_T + "' ");
                        stb.append("            AND T1.COMP_CREDIT <> 0 ");
                        stb.append("            OR T1.CLASSCD = '94') ");
                    } else {
                        stb.append("     AND T1.COMP_CREDIT <> 0 ");
                    }
                }
                stb.append(" )");
                return stb.toString();
            }

        }

        protected String[] getTitlenameArray(final PrintData printData, final Title title) {
            final String[] titlenameArray;
            if (0 == title._intKey) {
                titlenameArray = new String[]{"入", "学", "前"};
            } else if (isPrintGradeTitleGakunen(printData)) {
                titlenameArray = new String[]{"第", title._name.substring(1,2), "学年"};
            } else {
                final int s1 = 2, s2 = title._name.length() - 2;
                if (_param._isSeireki && NumberUtils.isDigits(title._name.substring(0, s1))) {
                    titlenameArray = new String[]{"", title._name.substring(0, s2), title._name.substring(s2)};
                } else {
                    titlenameArray = new String[]{title._name.substring(0, s1), title._name.substring(s1, s2), title._name.substring(s2)};
                }
            }
            return titlenameArray;
        }

        protected class Title {
            final String _key;
            final int _intKey;
            final String _year;
            final String _annual;
            final String _name;
            int _pos;
            public Title(final String key, final int intKey, final String year, final String annual, final PrintData printData) {
                _key = key;
                _intKey = intKey;
                _year = year;
                _annual = annual;
                _name = titlename(intKey, printData);
            }

            private final String titlename(final int intKey, final PrintData printData) {
                final String titlename;
                if (0 == intKey) {
                    if (isPrintGradeTitleGakunen(printData)) {
                        titlename = "入学前";
                    } else {
                        titlename = "入学前年度";
                    }
                } else if (isPrintGradeTitleGakunen(printData)) {
                    titlename = "第" + String.valueOf(intKey) + "学年";
                } else {
                    final String nendo = _param._isSeireki ? String.valueOf(intKey) : nao_package.KenjaProperties.gengou(intKey);
                    titlename = nendo + "年度";
                }
                return titlename;
            }

            public String toString() {
                return "Title(key = " + _key + ", year = " + _year + ", annual = " + _annual + ", name = " + _name + ")";
            }
        }

        /**
         * 生徒ごとの印刷データ
         */
        protected static class PrintData {
            final String _schregno;
            final String _year;
            final String _semes;
            final Map _paramap;

            private SqlStudyrec _sqlStudyrec;

            private List _studyrecData = Collections.EMPTY_LIST;;
            private Collection _ryunenYears = new HashSet();

            protected String schooldiv;
            protected String anname;                          //学年・年次名称

            private String _e014Subclasscd;
            private Map _d020Map;

            private boolean _printEachSubclassCreditSubstZenbu = false;

            public PrintData(
                    final String schregno,
                    final String year,
                    final String semes,
                    final Map paramap) {
                _schregno = schregno;
                _year = year;
                _semes = semes;
                _paramap = paramap;
            }

            private void preprocessStudyrecDat(final PreparedStatement ps1, final Param param) throws SQLException {
                //名称マスタD020：登録されている科目は、明細から除外する。
                ResultSet rsD020 = ps1.executeQuery();
                final Map d020Map0 = new HashMap();
                String e014Subclasscd0 = null;
                while (rsD020.next()) {
                    if (null != rsD020.getString("E014")) {
                        e014Subclasscd0 = rsD020.getString("E014");
                    }
                }
                DbUtils.closeQuietly(rsD020);
                _e014Subclasscd = e014Subclasscd0;
                _d020Map = d020Map0;
            }
        }

        protected static class Param {
            protected String _tyousasyoNotPrintAnotherStudyrec; // 前籍校の成績（SCHOOLCD='1'のSCHREG_STUDYREC_DAT）を表示しない
            protected String _notUseClassMstSpecialDiv; // 教科マスタの専門区分を使用しない
            protected String _useCurriculumcd;
            protected String _useClassDetailDat;
            protected Map _isNotPrintClassTitle;
            protected final String _e027Name1;
            protected final boolean _hasAnotherClassMst;
            protected final boolean _hasAnotherSubclassMst;
            protected boolean _isSeireki; // 西暦表示するならtrue
            protected boolean _isTsushin;   // 通信制:Z001.NAMESPARE3='1'
            protected boolean _isHesetuKou;   // 併設校:Z010.NAMESPARE2='1'
            protected boolean _isChuKouIkkan; // 中高一貫:Z010.NAMESPARE2='1' || '2'
            protected boolean _isKokubunji;  // 国分寺はTrue
            protected boolean _isTokyoto;  // 東京都はTrue
            protected boolean _isTokiwa;   // 常磐はTrue

            protected String _schooldivName;
            protected List _forms = Collections.EMPTY_LIST;
            protected final KNJDefineSchool _definecode;

            Param(final DB2UDB db2, final KNJDefineSchool definecode) {
                _definecode = definecode;
                _isSeireki = "2".equals(getNameMst(db2, "Z012", "00").get("NAME1"));
                setPrintClassTitle(db2);
                _e027Name1 = (String) getNameMst(db2, "E027", "1").get("NAME1");
                setJuniorHiSchool(db2, definecode);
                _hasAnotherClassMst = setTableColumnCheck(db2, "ANOTHER_CLASS_MST", null);
                _hasAnotherSubclassMst = setTableColumnCheck(db2, "ANOTHER_SUBCLASS_MST", null);
            }

            /**
             * @param isJuniorHiSchool 設定する isJuniorHiSchool。
             */
            private void setJuniorHiSchool(final DB2UDB db2, final KNJDefineSchool definecode) {
                final Map z001 = getNameMst(db2, "Z001", definecode.schooldiv);
                _schooldivName = (String) z001.get("NAME1");
                _isTsushin = "1".equals(z001.get("NAMESPARE3"));
                final Map z010 = getNameMst(db2, "Z010", "00");
                final String namespare2 = (String) z010.get("NAMESPARE2");
                final String name1 = (String) z010.get("NAME1");
                if (_isTsushin) {
                } else {
                    _isHesetuKou = "1".equals(namespare2);
                    _isChuKouIkkan = "1".equals(namespare2) || "2".equals(namespare2);
                    _isKokubunji = "kokubunji".equals(name1);
                    _isTokyoto = "tokyoto".equals(name1);
                    _isTokiwa = "tokiwa".equals(name1);
                }
//                log.debug(" isTsushin? = " + _isTsushin + ", name1 = " + name1);
            }

            private static boolean setTableColumnCheck(final DB2UDB db2, final String tabname, final String colname) {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT 1 FROM ");
                if (StringUtils.isBlank(colname)) {
                    stb.append("SYSCAT.TABLES");
                } else {
                    stb.append("SYSCAT.COLUMNS");
                }
                stb.append(" WHERE TABNAME = '" + tabname + "' ");
                if (!StringUtils.isBlank(colname)) {
                    stb.append(" AND COLNAME = '" + colname + "' ");
                }

                PreparedStatement ps = null;
                ResultSet rs = null;
                boolean hasTableColumn = false;
                try {
                    ps = db2.prepareStatement(stb.toString());
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        hasTableColumn = true;
                    }
                } catch (Exception ex) {
                    log.error("exception!", ex);
                } finally {
                    DbUtils.closeQuietly(null, ps, rs);
                    db2.commit();
                }
                log.fatal(" hasTableColumn " + tabname + "." + colname + " = " + hasTableColumn);
                return hasTableColumn;
            }

            public boolean isGakunensei() {
                return "0".equals(_definecode.schooldiv);
            }

            /**
             * 普通/専門教育に関する教科のタイトル表示設定
             */
            private void setPrintClassTitle(final DB2UDB db2) {
                _isNotPrintClassTitle = new HashMap();
                PreparedStatement ps = null;
                ResultSet rs = null;
                try {
                    final String sql = "SELECT NAMECD2, NAMESPARE1 FROM NAME_MST WHERE NAMECD1 = 'E015' ";
                    ps = db2.prepareStatement(sql);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        _isNotPrintClassTitle.put(rs.getString("NAMECD2"), rs.getString("NAMESPARE1"));
                    }
                } catch (Exception e) {
                    log.error("setPrintClassTitle Exception", e);
                } finally {
                    DbUtils.closeQuietly(null, ps, rs);
                    db2.commit();
                }
            }

            /**
             * 名称マスタ読み込み
             * @param db2
             * @param namecd1 名称コード1
             * @param namecd2 名称コード2
             * @return レコードのマップ
             */
            private Map getNameMst(final DB2UDB db2, final String namecd1, final String namecd2) {
                final Map nameMst = getNameMst(db2, namecd1);
                if (null == nameMst.get(namecd2)) {
                    return Collections.EMPTY_MAP;
                } else {
                    return (Map) nameMst.get(namecd2);
                }
            }

            /**
             * 名称マスタ読み込み
             * @param db2
             * @param map 名称マスタのマップ
             * @param key キー(名称コード2)
             * @return レコードのマップ
             */
            private static Map getMap(final Map map, final Object key) {
                if (null == map || null == map.get(key)) {
                    return Collections.EMPTY_MAP;
                }
                return (Map) map.get(key);
            }

            /**
             * 名称マスタ読み込み
             * @param db2
             * @param namecd1 名称コード1
             * @return 名称コード2をキーとするレコードのマップ
             */
            private Map getNameMst(final DB2UDB db2, final String namecd1) {
                final Map rtn = new HashMap();
                PreparedStatement ps = null;
                ResultSet rs = null;
                try {
                    final String sql = "SELECT * FROM NAME_MST WHERE NAMECD1 = '" + namecd1 + "' ";
                    ps = db2.prepareStatement(sql);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        rtn.put(rs.getString("NAMECD2"), Util.toMap(rs));
                    }
                } catch (Exception e) {
                    log.error("getNameMst Exception", e);
                } finally {
                    DbUtils.closeQuietly(null, ps, rs);
                    db2.commit();
                }
                return rtn;
            }

            /**
             * 普通/専門教育に関する教科のタイトルを表示するか
             * @param certifKindCd 証明書種別コード
             * @return 普通/専門教育に関する教科のタイトルを表示するか
             */
            protected boolean isPrintClassTitle(final String certifKindCd) {
                return !"1".equals(_isNotPrintClassTitle.get(certifKindCd));
            }

            public boolean isSameClasscd(final String classcd, final String schoolKind, final String s_classcd, final String s_schoolKind) {
                if ("1".equals(_useCurriculumcd)) {
                    return classcd.equals(s_classcd) && schoolKind.equals(s_schoolKind);
                }
                return classcd.equals(s_classcd);
            }
        }
    }
}

// eof

