//kanji=漢字
/*
 * $Id: 966bec8082de430fdf415918362ce72adfb5d21e $
 *
 * 作成日: 2008/05/19 15:38:24 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2004-2008 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJH;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJServletpacksvfANDdb2;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: 966bec8082de430fdf415918362ce72adfb5d21e $
 */
public class KNJH564F {

    private static final Log log = LogFactory.getLog(KNJH564F.class);

    private Param _param;

    private static final String SUBCLASS3 = "333333";
    private static final String SUBCLASS5 = "555555";
    private static final String SUBCLASSALL = "999999";
    private static final String RANK_DIV_GRADE = "01";
    private static final String RANK_DIV_HR = "02";
    private static final String RANK_DIV_COURSE = "03";
    private static final String RANK_DIV_MAJOR = "04";
    private static final String RANK_DIV_COURSEGROUP = "05";
    private static final String AVG_DIV_GRADE = "01";
    private static final String AVG_DIV_CLASS = "02";
    private static final String AVG_DIV_COURSE = "03";
    private static final String AVG_DIV_MAJOR = "04";
    private static final String AVG_DIV_COURSEGROUP = "05";
    private static final String AVG_DATA_SCORE = "1";

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

        } catch (final Exception e) {
            log.fatal("exception!", e);
        } finally {
            sd.closeSvf(svf, hasData);
            sd.closeDb(db2);
        }

    }// doGetの括り

    /**
     * @param db2
     */
    private boolean printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        boolean hasData = false;
        final List studentListAll = Student.getStudentList(db2, _param);
        final String form = "KNJH564F.frm";
        svf.VrSetForm(form, 1);
        final String nendo = KNJ_EditDate.getAutoFormatYear(db2, Integer.parseInt(_param._year));

        final List pageList = getPageList(studentListAll, 5);
        for (int pi = 0; pi < pageList.size(); pi++) {
            final List studentList = (List) pageList.get(pi);
            for (int i = 0; i < studentList.size(); i++) {
                final Student student = (Student) studentList.get(i);
                
                final int line = i + 1;
                
                svf.VrsOutn("NENDO", line, nendo);
                svf.VrsOutn("SEMESTER", line, _param._semesterName);
                svf.VrsOutn("TESTNAME", line, _param._testName);
                svf.VrsOutn("HR_NAME", line, student._hrName + "(" + student._attendNo + ")");
                svf.VrsOutn("NAME" + (KNJ_EditEdit.getMS932ByteLength(student._name) > 16 ? "2" : "1"), line, student._name);
                svf.VrsOutn("AVG_NAME1", line, "順位");

                int col = 1;
                for (final Iterator itSubclass = student._testSubclass.keySet().iterator(); itSubclass.hasNext();) {
                    final String subclassCd = (String) itSubclass.next();
                    final TestScore testScore = (TestScore) student._testSubclass.get(subclassCd);
                    svf.VrsOutn("SUBCLASS" + col, line, testScore._name);
                    svf.VrsOutn("SCORE" + col, line, testScore._score);
                    if ((null != testScore._rank || null != testScore._count)) {
                        svf.VrsOutn("RANK" + col, line, testScore._rank);
                        svf.VrsOutn("SLASH" + col, line, "/");
                        svf.VrsOutn("COUNT" + col, line, testScore._count);
                    }
                    col++;
                }

                if (student._hasScore) {
                	final TestScore testAll;
					if (student._testAll._isSet) {
						testAll = student._testAll;
                	} else {
                		//log.info("9教科の設定が無い場合、5教科を使用する : schregno = " + student._schregno);
                		testAll = student._subclass555555;
                	}
                    svf.VrsOutn("TOTAL_SCORE", line, testAll._score);
                    if ("1".equals(_param._juni) && (null != testAll._rank || null != testAll._count)) {
                        svf.VrsOutn("TOTAL_RANK", line, testAll._rank);
                        svf.VrsOutn("TOTAL_SLASH", line, "/");
                        svf.VrsOutn("TOTAL_COUNT", line, testAll._count);
                    } else if ("2".equals(_param._juni) && (null != testAll._rank2 || null != testAll._count2)) {
                        svf.VrsOutn("TOTAL_RANK", line, testAll._rank2);
                        svf.VrsOutn("TOTAL_SLASH", line, "/");
                        svf.VrsOutn("TOTAL_COUNT", line, testAll._count2);
                    }
                    svf.VrsOutn("AVERAGEL_SCORE", line, StringUtils.defaultString(student._averageScore));
                }

                hasData = true;
            }
            svf.VrEndPage();
        }
        return hasData;
    }
    
    private static List getPageList(final List studentList, final int max) {
        final List rtn = new ArrayList();
        List current = null;
        String befGradeClass = "";
        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            if (null == current || current.size() >= max || !befGradeClass.equals(student._grade + student._hrClass)) {
                current = new ArrayList();
                rtn.add(current);
            }
            current.add(student);
            befGradeClass = student._grade + student._hrClass;
        }
        return rtn;
    }

    private static List getMappedList(final Map map, final Object key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new ArrayList());
        }
        return (List) map.get(key1);
    }

    private static Map getMappedMap(final Map map, final Object key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new TreeMap());
        }
        return (Map) map.get(key1);
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
        final Map _testSubclass;
        private TestScore _testAll;
        private TestScore _subclass555555;
        private boolean _hasScore;
        /** 個人の平均点 */
        private String _averageScore;
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
            _testAll = new TestScore();
            _testAll._score = "";
            _subclass555555 = new TestScore();
        }

        public String toString() {
            return "学籍：" + _schregno + " 名前：" + _name;
        }

        public static List getStudentList(final DB2UDB db2, final Param param) throws SQLException  {
            final List rtnStudent = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getStudentInfoSql(param);
                log.debug(" sql = " + sql);
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
                                                  rs.getString("COURSECODENAME"));
                    rtnStudent.add(student);
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            ProficiencyRank.loadRank(db2, rtnStudent, param);
            return rtnStudent;
        }

        private static String getStudentInfoSql(final Param param) {
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
            stb.append("     INNER JOIN (SELECT DISTINCT SCHREGNO FROM PROFICIENCY_DAT I2 ");
            stb.append("                 WHERE I2.YEAR = '" + param._year + "' AND I2.SEMESTER = '" + param._semester + "' ");
            stb.append("                   AND I2.PROFICIENCYDIV = '" + param._proficiencydiv + "' ");
            stb.append("                   AND I2.PROFICIENCYCD = '" + param._proficiencycd + "' ");
            stb.append("                   AND I2.SCORE IS NOT NULL ");
            stb.append("                ) I2 ON I2.SCHREGNO = VSCH.SCHREGNO ");
            stb.append("     LEFT JOIN V_COURSE_MAJOR_MST L1 ON VSCH.YEAR = L1.YEAR ");
            stb.append("          AND VSCH.COURSECD = L1.COURSECD ");
            stb.append("          AND VSCH.MAJORCD = L1.MAJORCD ");
            stb.append("     LEFT JOIN V_COURSECODE_MST L2 ON VSCH.YEAR = L2.YEAR ");
            stb.append("          AND VSCH.COURSECODE = L2.COURSECODE ");
            stb.append("     LEFT JOIN SCHREG_NAME_SETUP_DAT L4 ON L4.SCHREGNO = VSCH.SCHREGNO AND L4.DIV = '04' ");
            stb.append("  ");
            stb.append(" WHERE ");
            stb.append("     VSCH.YEAR = '" + param._year + "' ");
            stb.append("     AND VSCH.SEMESTER = '" + param._semester + "' ");
            if (param.isClass()) {
                stb.append("     AND VSCH.GRADE = '" + param._grade + "' ");
                stb.append("     AND VSCH.HR_CLASS IN " + param._selectInstate + " ");
            } else {
                stb.append("     AND VSCH.GRADE = '" + param._grade + "' ");
                stb.append("     AND VSCH.HR_CLASS = '" + param._hrClass + "' ");
                stb.append("     AND VSCH.SCHREGNO IN " + param._selectInstate + " ");
            }
            stb.append(" ORDER BY ");
            stb.append("     VSCH.GRADE, ");
            stb.append("     VSCH.HR_CLASS, ");
            stb.append("     VSCH.ATTENDNO ");

            return stb.toString();
        }
    }

    private static class TestScore {
        String _name;
        String _score;
        String _avg;
        String _avg2;
        String _rank;
        String _count;
        String _rank2;
        String _count2;
        boolean _isSet;
//        String _passScore;

        public String toString() {
            return "科目：" + _name
                    + "得点：" + _score
                    + " 平均：" + _avg;
        }

        private static String getAvgString(final BigDecimal avg) {
            if (avg == null) {
                return null;
            }
            return avg.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
        }

    }

    private static class ProficiencyRank {

        public static void loadRank(final DB2UDB db2, final List studentList, final Param param) {
            final Map subclassnameMap = getProficiencySubclassNameMap(db2, param);

            final Map studentProficiencyScoreMap = getStudentProficiencyScoreMap(db2, param);
            log.info(" loadScore size = " + studentProficiencyScoreMap.size());
            
            final Map studentScoreMap = setSubclassGroupScore(db2, param, studentProficiencyScoreMap);
            
            log.info(" set subclassRankMap ");
            for (final Iterator it = studentList.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                
                boolean hasScore = false;
                final Map schregSubclassScoreMap = getMappedMap(studentScoreMap, student._schregno);
                //log.debug(" score schregno = " + student._schregno + ", subclass = " + schregSubclassScoreMap.keySet());
                for (final Iterator scoreIt = schregSubclassScoreMap.keySet().iterator(); scoreIt.hasNext();) {
                    String subclassCd = (String) scoreIt.next();

                    final BigDecimal score = (BigDecimal) schregSubclassScoreMap.get(subclassCd);
                    boolean isAvg = false;
                    if ((SUBCLASS3 + "_AVG").equals(subclassCd)) {
                        subclassCd = SUBCLASS3;
                        isAvg = true;
                    } else if ((SUBCLASS5 + "_AVG").equals(subclassCd)) {
                        subclassCd = SUBCLASS5;
                        isAvg = true;
                    } else if ((SUBCLASSALL + "_AVG").equals(subclassCd)) {
                        subclassCd = SUBCLASSALL;
                        isAvg = true;
                    }
                    if (SUBCLASS3.equals(subclassCd)) {
                        continue;
                    }
                    if (SUBCLASSALL.equals(subclassCd) || SUBCLASS5.equals(subclassCd)) {
                        TestScore testScore = null;
                        if (SUBCLASSALL.equals(subclassCd)) {
                        	testScore = student._testAll;
                            testScore._isSet = true;
                        } else if (SUBCLASS5.equals(subclassCd)) {
                        	testScore = student._subclass555555;
                        }
                        if (null != testScore && null != score) {
                            if (isAvg) {
                                student._averageScore = score.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                            } else {
                                testScore._score = score.toString();
                                hasScore = true;
                            }
                        }
                    } else {
                        if (null == student._testSubclass.get(subclassCd)) {
                            final TestScore testScore = new TestScore();
                            student._testSubclass.put(subclassCd, testScore);
                            testScore._name = (String) subclassnameMap.get(subclassCd);
                        }
                        final TestScore testScore = (TestScore) student._testSubclass.get(subclassCd);
                        if (null != score) {
                            if (isAvg) {
                                testScore._avg = score.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                            } else {
                                testScore._score = score.toString();
                                hasScore = true;
                            }
                        }
                    }
                }
                student._hasScore = hasScore;
            }

            final Map subclassRankMap = getSubclassRankMap(studentScoreMap);

            for (final Iterator it = studentList.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();

                final Map schregSubclassRankMap = getMappedMap(getMappedMap(subclassRankMap, "RANK"), student._schregno);
                final Map schregSubclassCountMap = getMappedMap(getMappedMap(subclassRankMap, "COUNT"), student._schregno);
                for (final Iterator scoreIt = schregSubclassRankMap.entrySet().iterator(); scoreIt.hasNext();) {
                    final Map.Entry e = (Map.Entry) scoreIt.next();
                    String subclassCd = (String) e.getKey();
                    final Integer rank = (Integer) e.getValue();
                    final Integer count = (Integer) schregSubclassCountMap.get(subclassCd);

                    boolean isAvg = false;
                    if ((SUBCLASS3 + "_AVG").equals(subclassCd)) {
                        subclassCd = SUBCLASS3;
                        isAvg = true;
                    } else if ((SUBCLASS5 + "_AVG").equals(subclassCd)) {
                        subclassCd = SUBCLASS5;
                        isAvg = true;
                    } else if ((SUBCLASSALL + "_AVG").equals(subclassCd)) {
                        subclassCd = SUBCLASSALL;
                        isAvg = true;
                    }
                    if (SUBCLASS3.equals(subclassCd)) {
                        continue;
                    }

                    final TestScore testScore;
                    if (SUBCLASSALL.equals(subclassCd)) {
                        testScore = student._testAll;
                    } else if (SUBCLASS5.equals(subclassCd)) {
                        testScore = student._subclass555555;
                    } else {
                        testScore = (TestScore) student._testSubclass.get(subclassCd);
                    }
                    if (null != testScore && null != rank) {
                        if (isAvg) {
                            testScore._rank2 = rank.toString();
                            if (null != count) {
                                testScore._count2 = count.toString();
                            }
                        } else {
                            testScore._rank = rank.toString();
                            if (null != count) {
                                testScore._count = count.toString();
                            }
                        }
                    }
                }
            }
        }

        private static Map setSubclassGroupScore(final DB2UDB db2, final Param param, final Map studentProficiencyScoreMap) {
            final Map subclassGroup = getProficiencySubclassGroupList(db2, param);
            log.info(" set total (subclassGroup size = " + subclassGroup.size() + ")");
            
            // 3科、5科、合計の算出
            final Map studentScoreMap = getMappedMap(studentProficiencyScoreMap, "SCORE");
            for (final Iterator it = studentScoreMap.keySet().iterator(); it.hasNext();) {
                final String schregno = (String) it.next();
                final String course = (String) getMappedMap(studentProficiencyScoreMap, "COURSE_KEY").get(schregno);
                
                for (final Iterator git = subclassGroup.keySet().iterator(); git.hasNext();) {
                    final String groupDiv = (String) git.next(); // 3 or 5 or 9
                    
                    final List subclasscdList = getMappedList(getMappedMap(subclassGroup, groupDiv), course);
                    boolean hasDi = false;
                    BigDecimal sum = new BigDecimal(0);
                    int count = 0;
                    for (final Iterator subcIt = subclasscdList.iterator(); subcIt.hasNext();) {
                        final String subclasscd = (String) subcIt.next();
                        if (getMappedList(getMappedMap(studentProficiencyScoreMap, "SCORE_DI"), schregno).contains(subclasscd)) {
                            hasDi = true;
                            break;
                        }
                        final BigDecimal score = (BigDecimal) getMappedMap(studentScoreMap, schregno).get(subclasscd);
                        if (null != score) {
                            sum = sum.add(score);
                            count += 1;
                        }
                    }
                    if (!hasDi && count > 0) {
                        final String subclasscdGokei = StringUtils.repeat(groupDiv, 6);
                        getMappedMap(studentScoreMap, schregno).put(subclasscdGokei, sum);
                        getMappedMap(studentScoreMap, schregno).put(subclasscdGokei + "_AVG", sum.divide(new BigDecimal(count), 5, BigDecimal.ROUND_HALF_UP));
                    }
                }
            }
            return studentScoreMap;
        }

        public static Map getSubclassRankMap(final Map studentScoreMap) {
            log.info(" set subclassScoreMap ");
            final Map subclassMap = new HashMap(); // 科目の得点をキーとする生徒のリスト
            for (final Iterator it = studentScoreMap.keySet().iterator(); it.hasNext();) {
                final String schregno = (String) it.next();

                for (final Iterator scit = getMappedMap(studentScoreMap, schregno).entrySet().iterator(); scit.hasNext();) {
                    final Map.Entry e = (Map.Entry) scit.next(); 
                    final String subclasscd = (String) e.getKey();
                    final Object score = e.getValue();
                    if (null != score) {
                        getMappedList(getMappedMap(subclassMap, subclasscd), score).add(schregno);
                    }
                }
            }
            
            log.info(" calc subclassRank ");
            final Map subclassRankMap = new HashMap(); // 生徒の科目をキーとする順位と母集団の数
            for (final Iterator it = subclassMap.keySet().iterator(); it.hasNext();) {
                final String subclasscd = (String) it.next();
                final Map scoreListSchregnoMap = getMappedMap(subclassMap, subclasscd);
                int count = 0; // 母集団の数
                final Set scoreSet = scoreListSchregnoMap.keySet();
                for (final ListIterator lit = new ArrayList(scoreSet).listIterator(scoreSet.size()); lit.hasPrevious();) {
                    final BigDecimal score = (BigDecimal) lit.previous();
                    final List schregnoList = getMappedList(scoreListSchregnoMap, score);
                    count += schregnoList.size();
                }
                int rank = 1;
                for (final ListIterator lit = new ArrayList(scoreSet).listIterator(scoreSet.size()); lit.hasPrevious();) {
                    final BigDecimal score = (BigDecimal) lit.previous();
                    final List schregnoList = getMappedList(scoreListSchregnoMap, score);
                    for (final Iterator schIt = schregnoList.iterator(); schIt.hasNext();) {
                        final String schregno = (String) schIt.next();
                        getMappedMap(getMappedMap(subclassRankMap, "RANK"), schregno).put(subclasscd, new Integer(rank));
                        getMappedMap(getMappedMap(subclassRankMap, "COUNT"), schregno).put(subclasscd, new Integer(count));
                    }
                    rank += schregnoList.size();
                }
            }
            return subclassRankMap;
        }

        public static Map getStudentProficiencyScoreMap(final DB2UDB db2, final Param param) {
            final Map map = new TreeMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = sqlScore(param);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String schregno = rs.getString("SCHREGNO");
                    final String subclasscd = rs.getString("PROFICIENCY_SUBCLASS_CD");
                    getMappedMap(getMappedMap(map, "SCORE"), schregno).put(subclasscd, rs.getBigDecimal("SCORE"));
                    getMappedMap(map, "COURSE_KEY").put(schregno, courseKey(rs.getString("GRADE"), rs.getString("COURSECD"), rs.getString("MAJORCD"), rs.getString("COURSECODE")));
                    if (null != rs.getString("SCORE_DI")) {
                        getMappedList(getMappedMap(map, "SCORE_DI"), schregno).add(subclasscd);
                    }
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return map;
        }

        private static String sqlScore(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT T1.SCHREGNO, T2.GRADE, T2.COURSECD, T2.MAJORCD, T2.COURSECODE, T1.PROFICIENCY_SUBCLASS_CD, T1.SCORE, T1.SCORE_DI ");
            stb.append(" FROM PROFICIENCY_DAT T1 ");
            stb.append(" INNER JOIN SCHREG_REGD_DAT T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("  AND T2.YEAR = T1.YEAR ");
            stb.append("  AND T2.SEMESTER = T1.SEMESTER ");
            stb.append(" WHERE ");
            stb.append("  T1.YEAR = '" + param._year + "' ");
            stb.append("  AND T1.SEMESTER = '" + param._semester + "' ");
            stb.append("  AND T1.PROFICIENCYDIV = '" + param._proficiencydiv + "' ");
            stb.append("  AND T1.PROFICIENCYCD = '" + param._proficiencycd + "' ");
            return stb.toString();
        }
        
        public static Map getProficiencySubclassNameMap(final DB2UDB db2, final Param param) {
            return KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, " SELECT * FROM PROFICIENCY_SUBCLASS_MST "), "PROFICIENCY_SUBCLASS_CD", "SUBCLASS_NAME");
        }

        private static String courseKey(final String grade, final String coursecd, final String majorcd, final String coursecode) {
            return grade + "-" + coursecd + "-" + majorcd + "-" + coursecode;
        }

        private static Map getProficiencySubclassGroupList(final DB2UDB db2, final Param param) {
            final Map list = new TreeMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = sqlSubclassGroup(param);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    getMappedList(getMappedMap(list, rs.getString("GROUP_DIV")), courseKey(rs.getString("GRADE"), rs.getString("COURSECD"), rs.getString("MAJORCD"), rs.getString("COURSECODE"))).add(rs.getString("PROFICIENCY_SUBCLASS_CD"));
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }

        private static String sqlSubclassGroup(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT  ");
            stb.append("  GROUP_DIV, ");
            stb.append("  GRADE, ");
            stb.append("  COURSECD, ");
            stb.append("  MAJORCD, ");
            stb.append("  COURSECODE, ");
            stb.append("  PROFICIENCY_SUBCLASS_CD ");
            stb.append(" FROM  ");
            stb.append("  DB2INST1.PROFICIENCY_SUBCLASS_GROUP_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("  T1.YEAR = '" + param._year + "' ");
            stb.append("  AND T1.SEMESTER = '" + param._semester + "' ");
            stb.append("  AND T1.PROFICIENCYDIV = '" + param._proficiencydiv + "' ");
            stb.append("  AND T1.PROFICIENCYCD = '" + param._proficiencycd + "' ");
            return stb.toString();
        }
    }
    
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 67353 $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    private static class Param {
        final String _year;
        final String _categoryIsClass;
        final String _proficiencydiv;
        final String _proficiencycd;
        final String _testName;
        final String _semester;
        final String _semesterName;
        final String _grade;
        final String _hrClass;
        final String _juni;
        final String[] _selectData;
        final String _selectInstate;
        final boolean _isdebug = true;

        Param(final DB2UDB db2, final HttpServletRequest request) throws Exception {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _categoryIsClass = request.getParameter("CATEGORY_IS_CLASS");
            _proficiencydiv = request.getParameter("PROFICIENCYDIV");
            _proficiencycd = request.getParameter("PROFICIENCYCD");
            _testName = getTestName(db2);
            _semesterName = getSemesterName(db2);
            _grade = request.getParameter("GRADE");
            _hrClass = request.getParameter("HR_CLASS");
            _juni = request.getParameter("JUNI");
            _selectData = request.getParameterValues("CATEGORY_SELECTED");  //学籍番号または学年-組
            _selectInstate = getInstate(_selectData);
        }

        private String getTestName(final DB2UDB db2) throws SQLException {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT PROFICIENCYNAME1 FROM PROFICIENCY_MST WHERE PROFICIENCYDIV = '" + _proficiencydiv + "' AND PROFICIENCYCD = '" + _proficiencycd + "' "));
        }

        private String getSemesterName(final DB2UDB db2) throws SQLException {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT SEMESTERNAME FROM SEMESTER_MST WHERE YEAR = '" + _year + "' AND SEMESTER = '" + _semester + "' "));
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

        private boolean isClass() {
            return _categoryIsClass.equals("1");
        }
    }
}

// eof
