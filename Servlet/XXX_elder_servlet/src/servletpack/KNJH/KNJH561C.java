// kanji=漢字
/*
 * $Id: e343bad170c9df6cbcd764cb6d1b0185c298b067 $
 *
 * 作成日: 2007/05/14
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJH;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 * 成績順一覧表（土佐塾）
 * @author maesiro
 * @version $Id: e343bad170c9df6cbcd764cb6d1b0185c298b067 $
 */
public class KNJH561C {
    private static final Log log = LogFactory.getLog(KNJH561C.class);

    private final String Gakunen = "000";

    private static final String RANK_KANSAN = "03";
    private static final String RANK_GOUKEI = "01";
    private static final String RANK_GRADE = "01";

    private static final String RANK_DATA_DIV_SCORE = "01";
    private static final String RANK_DATA_DIV_AVG = "02";
    private static final String RANK_DATA_DIV_DEVIATION = "03";

    private static final String RANK_DIV_GRADE = "01";
    private static final String RANK_DIV_HR = "02";

    private static final String _999999 = "999999";
    private static final String _999999AVG = "999999AVG";

    private static final String AVG_DIV_GRADE = "01";
    private static final String AVG_DIV_HR_CLASS = "02";
    private static final String AVG_DIV_COURSE = "03";

    private static final String AVG_DATA_SCORE = "1";
    
    private static final String SORT_DIV_RANK = "1";
    private static final String SORT_DIV_ATTENDNO = "2";

    private Param _param;

    private boolean _hasData;

    /**
     *  KNJD.classから最初に起動されます。
     */
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws ServletException, IOException {
        log.fatal("$Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $"); // CVSキーワードの取り扱いに注意

        final Vrw32alp svf = new Vrw32alp();
        DB2UDB db2 = null;
        try {
            response.setContentType("application/pdf");

            svf.VrInit();
            svf.VrSetSpoolFileStream(response.getOutputStream());

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            KNJServletUtils.debugParam(request, log);
            _param = new Param(request, db2);

            _hasData = false;

            printMain(svf, db2);
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

    private void printMain(final Vrw32alp svf, DB2UDB db2) {
        
        final Map avgMap = ProficiencyAverage.getAverageMap(db2, _param);

        final List studentsAll = Student.getStudentList(db2, _param);

        if ("3".equals(_param._selectDiv)) {
            print1(db2, svf, null, avgMap, studentsAll);
        } else {
            for (int i = 0; i < _param._categorySelected.length; i++) {
                final String gradeHr = _param._categorySelected[i];
                print1(db2, svf, gradeHr, avgMap, studentsAll);
            }
        }
    }

    private int getMS932ByteCount(final String str) {
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

    private static String toStr(final Object o) {
        return null == o ? null : o.toString();
    }

    private static String sishagonyu(final BigDecimal bd) {
        return null == bd ? null : bd.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
    }
    
    private static BigDecimal median(final List bdList) {
        if (null == bdList || bdList.isEmpty()) {
            return null;
        }
        final List copy = new ArrayList(bdList);
        Collections.sort(copy);
        final BigDecimal median;
        if (copy.size() == 1) {
            median = (BigDecimal) copy.get(0);
        } else if (copy.size() % 2 == 1) {
            median = (BigDecimal) copy.get(copy.size() / 2);
        } else { // (copy.size() % 2 == 0) {
            final BigDecimal median1 = (BigDecimal) copy.get(copy.size() / 2);
            final BigDecimal median2 = (BigDecimal) copy.get(copy.size() / 2 - 1);
            median = (median1.add(median2)).divide(new BigDecimal(2), BigDecimal.ROUND_HALF_UP);
        }
        return median;
    }

    private static String sishagonyu(final String s) {
        return NumberUtils.isNumber(s) ? sishagonyu(new BigDecimal(s)) : null;
    }
    
    private Set getMappedHashSet(final Map map, final Object key) {
        if (null == map.get(key)) {
            map.put(key, new HashSet());
        }
        return (Set) map.get(key);
    }

    private List getSubclasscds(final DB2UDB db2, final List students, final String gradeHr) {
        final String sql = getSubclassSql(_param, gradeHr);
        return KnjDbUtils.getColumnDataList(KnjDbUtils.query(db2, sql), "PROFICIENCY_SUBCLASS_CD");
    }

    private String getSubclassSql(final Param param, final String gradeHr) {
        final StringBuffer stb = new StringBuffer();
        
        stb.append(" SELECT DISTINCT ");
        stb.append("     T1.PROFICIENCY_SUBCLASS_CD ");
        stb.append(" FROM ");
        stb.append("     PROFICIENCY_DAT T1 ");
        stb.append("     INNER JOIN SCHREG_REGD_DAT T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("         AND T2.YEAR = T1.YEAR ");
        stb.append("         AND T2.SEMESTER = T1.SEMESTER ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + param._year + "' ");
        stb.append("     AND T1.SEMESTER = '" + param._semester + "' ");
        stb.append("     AND T1.PROFICIENCYDIV = '" + param._proficiencyDiv + "' ");
        stb.append("     AND T1.PROFICIENCYCD = '" + param._proficiencyCd + "' ");
        stb.append("     AND T2.GRADE = '" + param._grade +"' ");
        if (null != gradeHr) {
            stb.append("     AND T2.GRADE || T2.HR_CLASS = '" + gradeHr + "' ");
        }
        stb.append(" ORDER BY ");
        stb.append("     T1.PROFICIENCY_SUBCLASS_CD ");
        return stb.toString();
    }

    /**
     * listを最大数ごとにグループ化したリストを得る
     * @param list
     * @param max 最大数
     * @return listを最大数ごとにグループ化したリスト
     */
    private static List getPageList(final List list, final int max) {
        final List rtn = new ArrayList();
        List current = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Object o = it.next();
            if (null == current || current.size() >= max) {
                current = new ArrayList();
                rtn.add(current);
            }
            current.add(o);
        }
        return rtn;
    }

    private static Integer toInteger(final String intString) {
        if (NumberUtils.isDigits(intString)) {
            return Integer.valueOf(intString);
        }
        return null;
    }

    private static class Student {
        final String _schregno;
        final String _entDiv;
        final String _grade;
        final String _hrclass;
        final String _hrClassName1;
        final String _course;
        final String _coursecode;
        final String _coursecodeName;
        final String _attendno;
        final String _name;
        final String _sex;
        final String _inoutCd;
        final String _residentcdAbbv2;
        final String _residentcdNamespare1;
        final Map _ranks = new HashMap();
        public Student(final String schregno, final String entDiv, final String name, final String grade, final String hrclass, final String hrClassName1, final String course, final String coursecode, final String coursecodeName, final String attendno, final String sex, final String inoutCd, final String residentcdAbbv2, final String residentcdNamespare1) {
            _schregno = schregno;
            _entDiv = entDiv;
            _name = name;
            _grade = grade;
            _hrclass = hrclass;
            _hrClassName1 = hrClassName1;
            _course = course;
            _coursecode = coursecode;
            _coursecodeName = coursecodeName;
            _attendno = attendno;
            _sex = sex;
            _inoutCd = inoutCd;
            _residentcdAbbv2 = residentcdAbbv2;
            _residentcdNamespare1 = residentcdNamespare1;
        }

        public static List getStudentList(final DB2UDB db2, final Param param) {

            final Map studentMap = new HashMap();
            final String sql = Student.sqlStudent(param);
//            log.info(" student sql = " + sql);
            for (final Iterator it = KnjDbUtils.query(db2, sql).iterator(); it.hasNext();) {
                final Map row = (Map) it.next();

                final Student student = new Student(KnjDbUtils.getString(row, "SCHREGNO"), KnjDbUtils.getString(row, "ENT_DIV"), KnjDbUtils.getString(row, "NAME"), KnjDbUtils.getString(row, "GRADE"), KnjDbUtils.getString(row, "HR_CLASS"), KnjDbUtils.getString(row, "HR_CLASS_NAME1"), KnjDbUtils.getString(row, "COURSE"), KnjDbUtils.getString(row, "COURSECODE"), KnjDbUtils.getString(row, "COURSECODENAME"), KnjDbUtils.getString(row, "ATTENDNO"), KnjDbUtils.getString(row, "SEX"), KnjDbUtils.getString(row, "INOUTCD"), KnjDbUtils.getString(row, "RESIDENTCD_ABBV2"), KnjDbUtils.getString(row, "RESIDENTCD_NAMESPARE1"));
                studentMap.put(student._schregno, student);
            }

            final String sqlRank = Rank.sqlRank(param);
            for (final Iterator it = KnjDbUtils.query(db2, sqlRank).iterator(); it.hasNext();) {
                final Map row = (Map) it.next();

                final Student student = (Student) studentMap.get(KnjDbUtils.getString(row, "SCHREGNO"));
                if (null == student) {
                    continue;
                }
                final String subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");
                final String valueDi = KnjDbUtils.getString(row, "SCORE_DI");
                final String score = null != valueDi ? valueDi : KnjDbUtils.getString(row, "SCORE");
                final String avg = KnjDbUtils.getString(row, "AVG");
                final Integer gradeRank = toInteger(KnjDbUtils.getString(row, "GRADE_RANK"));
                final Integer hrRank = toInteger(KnjDbUtils.getString(row, "HR_RANK"));
                student._ranks.put(subclasscd, new Rank(subclasscd, score, avg, gradeRank, hrRank));
            }

            final List rtn = new ArrayList();
            rtn.addAll(studentMap.values());

            Collections.sort(rtn, new StudentSorter(param));

            return rtn;
        }

        private static String sqlStudent(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     REGD.SCHREGNO, ");
            stb.append("     BASE.NAME, ");
            stb.append("     ENTGRD.ENT_DIV, ");
            stb.append("     REGD.GRADE, ");
            stb.append("     REGD.HR_CLASS, ");
            stb.append("     REGDH.HR_CLASS_NAME1, ");
            stb.append("     REGD.COURSECD || REGD.MAJORCD || REGD.COURSECODE AS COURSE, ");
            stb.append("     REGD.COURSECODE, ");
            stb.append("     CCM.COURSECODENAME, ");
            stb.append("     REGD.ATTENDNO, ");
            stb.append("     BASE.SEX, ");
            stb.append("     BASE.INOUTCD, ");
            stb.append("     BRANCH.RESIDENTCD, ");
            stb.append("     J008.ABBV2 AS RESIDENTCD_ABBV2, ");
            stb.append("     J008.NAMESPARE1 AS RESIDENTCD_NAMESPARE1 ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT REGD ");
            stb.append("     INNER JOIN  SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ");
            stb.append("     LEFT JOIN SCHREG_REGD_HDAT REGDH ON REGDH.YEAR = REGD.YEAR ");
            stb.append("         AND REGDH.SEMESTER = REGD.SEMESTER ");
            stb.append("         AND REGDH.GRADE = REGD.GRADE ");
            stb.append("         AND REGDH.HR_CLASS = REGD.HR_CLASS ");
            stb.append("     LEFT JOIN COURSECODE_MST CCM ON CCM.COURSECODE = REGD.COURSECODE ");
            stb.append("     LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = REGD.YEAR ");
            stb.append("         AND GDAT.GRADE = REGD.GRADE ");
            stb.append("     LEFT JOIN SCHREG_BRANCH_DAT BRANCH ON BRANCH.SCHOOLCD = '000000000000' ");
            stb.append("         AND BRANCH.SCHOOL_KIND = GDAT.SCHOOL_KIND ");
            stb.append("         AND BRANCH.YEAR = REGD.YEAR ");
            stb.append("         AND BRANCH.SCHREGNO = REGD.SCHREGNO ");
            stb.append("     LEFT JOIN NAME_MST J008 ON J008.NAMECD1 = 'J008' ");
            stb.append("         AND J008.NAMECD2 = BRANCH.RESIDENTCD ");
            stb.append("     LEFT JOIN SCHREG_ENT_GRD_HIST_DAT ENTGRD ON ENTGRD.SCHREGNO = REGD.SCHREGNO ");
            stb.append("         AND ENTGRD.SCHOOL_KIND = ENTGRD.SCHOOL_KIND ");
            stb.append(" WHERE ");
            stb.append("     REGD.YEAR = '" + param._year + "' ");
            stb.append("     AND REGD.SEMESTER = '" + param._schregSemester + "' ");
            stb.append("     AND REGD.GRADE = '" + param._grade + "' ");
            stb.append(" ORDER BY ");
            stb.append("     REGD.GRADE, REGD.HR_CLASS, REGD.ATTENDNO ");
            return stb.toString();
        }

        /**
         * 総点の順位
         */
        public Integer getTotalRank(final Param param) {
            final Rank rank = (Rank) _ranks.get(_999999);
            if (null == rank) {
                return null;
            }
//            if ("1".equals(param._printDiv)) {
//                return rank._classRank;
//            }
            return rank._gradeRank;
        }

        public List getAkatenList(final Param param) {
            final List akatenList = new ArrayList();
            for (final Iterator it = getSubclassList().iterator(); it.hasNext();) {
                final Rank rank = (Rank) it.next();
                if (isAkaten(rank, param)) {
                    akatenList.add(rank._subclasscd);
                }
            }
//          if (akatenList.size() > 0) {
//          log.info(" " + _schregno + " akaten (" + akatenList.size() + ") subclasscd = " + akatenList);
//      }
            return akatenList;
        }

        public boolean hasKesseki(final Param param) {
            for (final Iterator it = _ranks.values().iterator(); it.hasNext();) {
                final Rank rank = (Rank) it.next();
                if ("*".equals(rank._score)) {
                    return true;
                }
            }
            return false;
        }
        
        public String toString() {
            return "Student(" + _schregno + ")";
        }

        public List getSubclassList() {
            final List subclassList = new ArrayList();
            for (final Iterator it = _ranks.values().iterator(); it.hasNext();) {
                final Rank rank = (Rank) it.next();
                if (_999999.equals(rank._subclasscd) || _999999AVG.equals(rank._subclasscd)) {
                    continue;
                }
                subclassList.add(rank);
            }
            return subclassList;
        }
    }

    private static boolean isAkaten(final Rank rank, final Param param) {
        if (NumberUtils.isNumber(rank._score) && Double.parseDouble(rank._score)  < param._akaten) {
            return true;
        }
        return false;
    }

    private static class StudentSorter implements Comparator {
        // 科目コード999999の順位でソート。順位がなければHR出欠番号順。
        final Param _param;
        StudentSorter(final Param param) {
            _param = param;
        }

        public int compare(final Object o1, final Object o2) {
            final Student s1 = (Student) o1;
            final Student s2 = (Student) o2;
            if (SORT_DIV_RANK.equals(_param._sort)) {
                final Integer s1rank = s1.getTotalRank(_param);
                final Integer s2rank = s2.getTotalRank(_param);
                int cmp = 0;
                if (s1rank != null && s2rank != null) {
                    cmp = s1rank.compareTo(s2rank);
                } else if (null == s1rank && null != s2rank) {
                    cmp = 1;
                } else if (null != s1rank && null == s2rank) {
                    cmp = -1;
                }
                if (cmp != 0) {
                    return cmp;
                }
            }
            return (s1._hrclass + s1._attendno).compareTo(s2._hrclass + s2._attendno);
        }
    }

    private static class ProficiencyAverage {

        private static ProficiencyAverage NULL = new ProficiencyAverage(null, null, null, null, null, null, null);

        final String _subclasscd;
        final String _score;
        final String _highscore;
        final String _lowscore;
        final String _count;
        final String _avg;
        final String _stddev;
        public ProficiencyAverage(final String subclasscd, final String score, final String highscore, final String lowscore, final String count, final String avg, final String stddev) {
            _subclasscd = subclasscd;
            _score = score;
            _highscore = highscore;
            _lowscore = lowscore;
            _count = count;
            _avg = avg;
            _stddev = stddev;
        }
        
        public String toString() {
            return "ProfAvg(subclasscd = " + _subclasscd + ", max = " + _highscore + ", min = " + _lowscore + ", count = " + _count + ", avg = " + _avg + ")";
        }

        public static ProficiencyAverage getAverageGrade(final Map avgMap, final String grade, final String subclasscd) {
            return getAverage(avgMap, avgKey(AVG_DIV_GRADE, grade, "000", "00000000", subclasscd));
        }

        public static ProficiencyAverage getAverageHr(final Map avgMap, final String grade, final String hrclass, final String subclasscd) {
            return getAverage(avgMap, avgKey(AVG_DIV_HR_CLASS, grade, hrclass, "00000000", subclasscd));
        }

        public static ProficiencyAverage getAverageCourse(final Map avgMap, final String grade, final String course, final String subclasscd) {
            return getAverage(avgMap, avgKey(AVG_DIV_COURSE, grade, "000", course, subclasscd));
        }

        public static ProficiencyAverage getAverage(final Map avgMap, final String avgKey) {
            ProficiencyAverage average = (ProficiencyAverage) avgMap.get(avgKey);
            if (null == average) {
                return NULL;
            }
            return average;
        }

        public static String avgKey(final String avgDiv, final String grade, final String hrClass, final String course, final String subclasscd) {
            return avgDiv + "-" + grade + "-" + hrClass + "-" + course + ":" + subclasscd;
        }

        private static String bdSetScale(final String val) {
            if (!NumberUtils.isNumber(val)) {
                return val;
            }
            final BigDecimal kirisage = new BigDecimal(val).setScale(0, BigDecimal.ROUND_FLOOR);
            if (new BigDecimal(val).compareTo(kirisage) == 0) {
                return kirisage.toString();
            }
            return val;
        }

        private static Map getAverageMap(final DB2UDB db2, final Param param) {
            final Map rtn = new HashMap();
            final String sql = ProficiencyAverage.sqlAverage(param);
            for (final Iterator it = KnjDbUtils.query(db2, sql).iterator(); it.hasNext();) {
                final Map row = (Map) it.next();

                final String subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");
                final String grade = KnjDbUtils.getString(row, "GRADE");
                final String hrClass = KnjDbUtils.getString(row, "HR_CLASS");
                final String course = KnjDbUtils.getString(row, "COURSE");
                final String score = bdSetScale(KnjDbUtils.getString(row, "SCORE"));
                final String highscore = bdSetScale(KnjDbUtils.getString(row, "HIGHSCORE"));
                final String lowscore = bdSetScale(KnjDbUtils.getString(row, "LOWSCORE"));
                final String count = bdSetScale(KnjDbUtils.getString(row, "COUNT"));
                final String avg = KnjDbUtils.getString(row, "AVG");
                final String stddev = KnjDbUtils.getString(row, "STDDEV");
                final ProficiencyAverage average = new ProficiencyAverage(subclasscd, score, highscore, lowscore, count, avg, stddev);

                final String avgKey = avgKey(KnjDbUtils.getString(row, "AVG_DIV"), grade, hrClass, course, subclasscd);
                rtn.put(avgKey, average);
            }
            log.info(" average dat size = " + rtn.size());
            return rtn;
        }

        private static String sqlAverage(final Param param) {
            final StringBuffer stb = new StringBuffer();

            stb.append(" WITH RANK9 AS ( ");
            stb.append(" SELECT ");
            stb.append("     REGD.GRADE, REGD.HR_CLASS, REGD.COURSECD || REGD.MAJORCD || REGD.COURSECODE AS COURSE ");
            stb.append("     , T1.AVG ");
            stb.append(" FROM ");
            stb.append("     PROFICIENCY_RANK_DAT T1 ");
            stb.append("     INNER JOIN (SELECT SCHREGNO, YEAR, GRADE, HR_CLASS, COURSECD, MAJORCD, COURSECODE ");
            stb.append("                 FROM SCHREG_REGD_DAT ");
            stb.append("                 WHERE YEAR = '" + param._year +"' ");
            stb.append("                 AND   SEMESTER = '" + param._schregSemester+ "' ");
            stb.append("                ) REGD ON REGD.YEAR = T1.YEAR AND REGD.SCHREGNO = T1.SCHREGNO");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.SEMESTER = '" + param._semester + "' ");
            stb.append("     AND T1.PROFICIENCYDIV = '" + param._proficiencyDiv + "' ");
            stb.append("     AND T1.PROFICIENCYCD = '" + param._proficiencyCd + "' ");
            stb.append("     AND T1.RANK_DATA_DIV = '" + RANK_DATA_DIV_SCORE + "' ");
            stb.append("     AND T1.RANK_DIV = '" + RANK_DIV_GRADE + "' ");
            stb.append(" ) ");

            stb.append(" SELECT ");
            stb.append("     T1.AVG_DIV, ");
            stb.append("     T1.GRADE, ");
            stb.append("     T1.HR_CLASS, ");
            stb.append("     T1.COURSECD || T1.MAJORCD || T1.COURSECODE AS COURSE, ");
            stb.append("     T1.PROFICIENCY_SUBCLASS_CD AS SUBCLASSCD, ");
            stb.append("     T1.SCORE, ");
            stb.append("     T1.HIGHSCORE, ");
            stb.append("     T1.LOWSCORE, ");
            stb.append("     T1.COUNT, ");
            stb.append("     T1.AVG, ");
            stb.append("     T1.STDDEV ");
            stb.append(" FROM ");
            stb.append("     PROFICIENCY_AVERAGE_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.SEMESTER = '" + param._semester + "' ");
            stb.append("     AND T1.PROFICIENCYDIV = '" + param._proficiencyDiv + "' ");
            stb.append("     AND T1.PROFICIENCYCD = '" + param._proficiencyCd + "' ");
            stb.append("     AND T1.DATA_DIV = '" + AVG_DATA_SCORE + "' ");
            stb.append("     AND T1.GRADE = '" + param._grade +"' ");
            
            // 学年
            stb.append(" UNION ALL ");
            stb.append(" SELECT ");
            stb.append("     '" + AVG_DIV_GRADE +  "' AS AVG_DIV, T1.GRADE, '000' AS HR_CLASS, '00000000' AS COURSE, ");
            stb.append("     '999999AVG' AS SUBCLASSCD, ");
            stb.append("     SUM(AVG) AS SCORE, ");
            stb.append("     MAX(AVG) AS HIGHSCORE, ");
            stb.append("     MIN(AVG) AS LOWSCORE, ");
            stb.append("     COUNT(T1.AVG) AS COUNT, ");
            stb.append("     AVG(T1.AVG) AS AVG, ");
            stb.append("     STDDEV(T1.AVG) AS STDDEV ");
            stb.append(" FROM ");
            stb.append("     RANK9 T1 ");
            stb.append(" GROUP BY ");
            stb.append("     T1.GRADE ");
            // 年組
            stb.append(" UNION ALL ");
            stb.append(" SELECT ");
            stb.append("     '" + AVG_DIV_HR_CLASS +  "' AS AVG_DIV, T1.GRADE, T1.HR_CLASS, '00000000' AS COURSE, ");
            stb.append("     '999999AVG' AS SUBCLASSCD, ");
            stb.append("     SUM(AVG) AS SCORE, ");
            stb.append("     MAX(AVG) AS HIGHSCORE, ");
            stb.append("     MIN(AVG) AS LOWSCORE, ");
            stb.append("     COUNT(T1.AVG) AS COUNT, ");
            stb.append("     AVG(T1.AVG) AS AVG, ");
            stb.append("     STDDEV(T1.AVG) AS STDDEV ");
            stb.append(" FROM ");
            stb.append("     RANK9 T1 ");
            stb.append(" GROUP BY ");
            stb.append("     T1.GRADE, T1.HR_CLASS ");

            return stb.toString();
        }
    }

    private static class Rank {
        final String _subclasscd;
        final String _score;
        final String _avg;
        final Integer _gradeRank;
        final Integer _hrRank;
        public Rank(final String subclasscd, final String score, final String avg, final Integer gradeRank, final Integer hrRank) {
            _subclasscd = subclasscd;
            _score = score;
            _avg = avg;
            _gradeRank = gradeRank;
            _hrRank = hrRank;
        }

        private static String sqlRank(final Param param) {
            final StringBuffer stb = new StringBuffer();
            
            stb.append("SELECT");
            stb.append("  T1.SCHREGNO,");
            stb.append("  T1.proficiency_subclass_cd as subclasscd,");
            stb.append("  T1.SCORE,");
            stb.append("  CAST(NULL AS DOUBLE) AS AVG,");
            stb.append("  T1.SCORE_DI,");
            stb.append("  CAST(NULL AS INT) as GRADE_RANK,");
            stb.append("  CAST(NULL AS INT) as HR_RANK ");
            stb.append(" FROM proficiency_dat T1 ");
            stb.append(" WHERE SCORE_DI IS NOT NULL AND ");
            stb.append("  T1.year = '" + param._year + "' AND");
            stb.append("  T1.semester = '" + param._semester + "' AND");
            stb.append("  T1.proficiencydiv = '" + param._proficiencyDiv + "' AND");
            stb.append("  T1.proficiencycd = '" + param._proficiencyCd + "' AND");
            stb.append(" EXISTS (SELECT 'X' FROM SCHREG_REGD_DAT REGD ");
            stb.append("     WHERE ");
            stb.append("         REGD.YEAR = '" + param._year + "' ");
            stb.append("         AND REGD.SEMESTER = '" + param._schregSemester + "' ");
            stb.append("         AND REGD.GRADE = '" + param._grade + "' ");
            stb.append(" ) ");

            stb.append(" UNION ALL ");

            stb.append("SELECT");
            stb.append("  T1.SCHREGNO,");
            stb.append("  T1.proficiency_subclass_cd as subclasscd,");
            stb.append("  T1.SCORE,");
            stb.append("  T1.AVG,");
            stb.append("  CAST(NULL AS VARCHAR(1)) as SCORE_DI,");
            stb.append("  T1.rank as GRADE_RANK,");
            stb.append("  T7.rank as HR_RANK ");
            stb.append(" FROM proficiency_rank_dat T1 ");
            stb.append(" LEFT JOIN proficiency_subclass_mst T3 ON T3.proficiency_subclass_cd = T1.proficiency_subclass_cd ");
            stb.append(" LEFT JOIN proficiency_rank_dat T7 ON ");
            stb.append("     T7.year = T1.year AND ");
            stb.append("     T7.semester = T1.semester AND");
            stb.append("     T7.proficiencydiv = T1.proficiencydiv AND");
            stb.append("     T7.proficiencycd = T1.proficiencycd AND");
            stb.append("     T7.proficiency_subclass_cd = T1.proficiency_subclass_cd AND");
            stb.append("     T7.schregno = T1.schregno AND");
            stb.append("     T7.rank_data_div = t1.rank_data_div AND");
            stb.append("     T7.rank_div = '" + RANK_DIV_HR + "' ");
            stb.append(" WHERE");
            stb.append("  T1.year = '" + param._year + "' AND");
            stb.append("  T1.semester = '" + param._semester + "' AND");
            stb.append("  T1.proficiencydiv = '" + param._proficiencyDiv + "' AND");
            stb.append("  T1.proficiencycd = '" + param._proficiencyCd + "' AND");
            stb.append("  (T3.proficiency_subclass_cd IS NOT NULL OR T1.proficiency_subclass_cd IN ('" + _999999 + "')) AND");
            if ("2".equals(param._juni)) {
                stb.append("  T1.rank_data_div = '" + RANK_DATA_DIV_AVG + "' AND");
            } else {
                stb.append("  T1.rank_data_div = '" + RANK_DATA_DIV_SCORE + "' AND");
            }
            stb.append("  T1.rank_div = '" + RANK_DIV_GRADE + "' AND");
            
            stb.append(" EXISTS (SELECT 'X' FROM SCHREG_REGD_DAT REGD ");
            stb.append("     WHERE ");
            stb.append("         REGD.YEAR = '" + param._year + "' ");
            stb.append("         AND REGD.SEMESTER = '" + param._schregSemester + "' ");
            stb.append("         AND REGD.GRADE = '" + param._grade + "' ");
            stb.append(" ) ");
            return stb.toString();
        }
    }

    private static List getMappedList(final Map map, final Object key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new ArrayList());
        }
        return (List) map.get(key1);
    }

    private static String getClasscd(final String subclasscd) {
        final String[] split = StringUtils.split(subclasscd, "-");
        if (null == split || split.length < 2) {
            return null;
        }
        return split[0] + "-" + split[1];
    }

    /**
     * 科目列ごとの教科名を得る
     * @param subclasscd 列の科目コード
     * @param keta 1列の教科名の桁数
     * @param classcdSubclasscdListMap 教科コードをキーとする科目コードリストのマップ
     * @return 科目列ごとの教科名
     */
    private String getColumnClassname(final String subclasscd, final int keta, final Map classcdSubclasscdListMap) {
        final String classcd = getClasscd(subclasscd);
        final String classname = (String) _param._classnameMap.get(classcd);
        if (null == classname) {
            return "";
        }
        final List subclasscdList = getMappedList(classcdSubclasscdListMap, classcd);
        final int idx = subclasscdList.indexOf(subclasscd);
        if (-1 == idx) {
            return "";
        }
        final int totalKeta = keta * subclasscdList.size();
        String rtn = "";
        rtn += StringUtils.repeat("　", (totalKeta - getMS932ByteCount(classname)) / 2 / 2);
        rtn += classname;
        rtn += StringUtils.repeat(" ", totalKeta - getMS932ByteCount(rtn));
        rtn = StringUtils.replace(rtn, "  ", "　"); // 半角スペース2つを全角に置換
        final String[] token = KNJ_EditEdit.get_token(rtn, 4, 999);
        if (null == token || idx >= token.length) {
            return "";
        }
        return token[idx];
    }
    
    private void print1(final DB2UDB db2, final Vrw32alp svf, final String gradeHr, final Map avgMap, final List studentsAll) {
        final String form;
        final int maxColumn = 13;
        final int maxLine;
        final boolean isPrintAvgBetuForm;
        final List students;
        
        final Stat stat = new Stat(studentsAll, _param);

        if (null != gradeHr) {
            maxLine = 45;
            form = "KNJH561C_3.frm";
            isPrintAvgBetuForm = false;
            
            // 指定年組のみ
            students = new ArrayList();
            for (final Iterator it = studentsAll.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                if (gradeHr.equals(student._grade + student._hrclass)) {
                    students.add(student);
                }
            }

        } else {
            maxLine = 60;
            form = "KNJH561C_1.frm";
            isPrintAvgBetuForm = true;

            students = studentsAll;
        }
        if ("1".equals(_param._ryoOnly)) {
            for (final Iterator it = students.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                if (!"4".equals(student._residentcdNamespare1)) {
                    it.remove();
                }
            }
        }

        final List allsubclasscds = getSubclasscds(db2, students, gradeHr); // PROFICIENCY_DAT = 999999をふくまない

        final List pageSubclassListList = getPageList(allsubclasscds, maxColumn);

        final List pageStudentListList = getPageList(students, maxLine);
        int totalPage = pageStudentListList.size() * pageSubclassListList.size();
        if (isPrintAvgBetuForm) {
            // 平均点のページを含める
            totalPage += pageSubclassListList.size();
        }

        final String title = KenjaProperties.gengou(Integer.parseInt(_param._year)) + "年度　" + StringUtils.defaultString(_param._semestername) + "　" + StringUtils.defaultString(_param._testitemname);

        for (int stpi = 0; stpi < pageStudentListList.size(); stpi++) {

            final List studentList = (List) pageStudentListList.get(stpi);

            for (int sbpi = 0; sbpi < pageSubclassListList.size(); sbpi++) {

                final List subclassList = (List) pageSubclassListList.get(sbpi);
                final int page = stpi * pageSubclassListList.size() + sbpi + 1;

                final Map classcdSubclasscdListMap = new HashMap();
                for (int subi = 0; subi < subclassList.size(); subi++) {
                    final String subclasscd = (String) subclassList.get(subi);
                    getMappedList(classcdSubclasscdListMap, getClasscd(subclasscd)).add(subclasscd);
                }
                svf.VrSetForm(form, 4);

                svf.VrsOut("TITLE", title); // タイトル
                svf.VrsOut("PAGE1", String.valueOf(page)); // ページ
                svf.VrsOut("PAGE2", String.valueOf(totalPage)); // ページ
                svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._loginDate)); // 印刷日付

                for (int sti = 0; sti < studentList.size(); sti++) {
                    final Student student = (Student) studentList.get(sti);
                    final int line = sti + 1;
                    svf.VrsOutn("DOMITORY", line, student._residentcdAbbv2); // 寮生
                    svf.VrsOutn("SP", line, null); // 特待 TODO
                    String entDivName = "";
                    if ("1".equals(student._entDiv)) {
                        entDivName = "";
                    } else if ("2".equals(student._entDiv)) {
                        entDivName = "後";
                    } else if ("3".equals(student._entDiv)) {
                        entDivName = "転";
                    }
                    svf.VrsOutn("DIV", line, entDivName); // 区分 TODO
                    svf.VrsOutn("HR_NAME", line, student._hrClassName1); // クラス名
                    svf.VrsOutn("NO", line, NumberUtils.isDigits(student._attendno) ? String.valueOf(Integer.parseInt(student._attendno)) : StringUtils.defaultString(student._attendno)); // 番号

                    final int nameKeta = getMS932ByteCount(student._name);
                    svf.VrsOutn("NAME" + (nameKeta <= 20 ? "1" : nameKeta <= 30 ? "2" : "3"), line, student._name); // 氏名

                    if (!"0000".equals(student._coursecode)) {
                        svf.VrsOutn("COURSE_NAME", line, student._coursecodeName); // コース名
                    }

                    final Rank rank999999 = (Rank) student._ranks.get(_999999);
                    if (null != rank999999) {
                        svf.VrsOutn("TOTAL", line, rank999999._score); // 合計
                        svf.VrsOutn("AVE", line, sishagonyu(rank999999._avg)); // 平均
                        svf.VrsOutn("HR_RANK", line, toStr(rank999999._hrRank)); // クラス順位
                        svf.VrsOutn("GRADE_RANK", line, toStr(rank999999._gradeRank)); // 学年順位
                    }
                    final ProficiencyAverage avgGrade999999 = ProficiencyAverage.getAverageGrade(avgMap, student._grade, _999999);
                    if (null != avgGrade999999) {
                        svf.VrsOutn("GRADE_NUM", line, avgGrade999999._count); // 学年人数
                    }
                    final ProficiencyAverage avgHr999999 = ProficiencyAverage.getAverageHr(avgMap, student._grade, student._hrclass, _999999);
                    if (null != avgHr999999) {
                        svf.VrsOutn("HR_NUM", line, avgHr999999._count); // クラス人数
                    }
                    svf.VrsOutn("RED_POINT", line, String.valueOf(student.getAkatenList(_param).size())); // 赤点
                    
                    svf.VrsOutn("SUBCLASS_NUM", line, String.valueOf(student.getSubclassList().size())); // 科目数
                }
                
                if (!isPrintAvgBetuForm) {
                    printStatHeader(svf, avgMap, stat);
                }

                for (int subi = 0; subi < subclassList.size(); subi++) {

                    final String subclasscd = (String) subclassList.get(subi);
                    final String subclassname = (String) _param._subclassnameMap.get(subclasscd);
                    if (null != subclassname) {
                        svf.VrsOut("CLASS_NAME1", subclassname.length() > 4 ? subclassname.substring(0, 4) : subclassname); // 教科名
                    }

                    for (int sti = 0; sti < studentList.size(); sti++) {
                        final Student student = (Student) studentList.get(sti);
                        final String line = String.valueOf(sti + 1);
                        final Rank rank = (Rank) student._ranks.get(subclasscd);
                        if (null != rank) {
                            svf.VrsOut("SCORE1_" + line, rank._score); // 素点
                            if (isAkaten(rank, _param)) {
                                svf.VrAttribute("SCORE1_" + line, "Palette=9,UnderLine=(0,1,1)"); // 素点
                            }
                        }
                    }
                    
                    if (!isPrintAvgBetuForm) {
                        printSubclassStat(svf, avgMap, stat, subclasscd);
                    }
                    
                    svf.VrEndRecord();
                }

                for (int i = subclassList.size(); i < maxColumn; i++) {
                    svf.VrsOut("CLASS_NAME1", "DUMMY"); // 素点
                    svf.VrAttribute("CLASS_NAME1", "X=10000"); // 素点
                    svf.VrEndRecord();
                }
                _hasData = true;
            }
        }
        if (isPrintAvgBetuForm) {

            final String form2 = "KNJH561C_2.frm";
            
            final int printedPage = pageStudentListList.size() * pageSubclassListList.size();
            for (int sbpi = 0; sbpi < pageSubclassListList.size(); sbpi++) {

                svf.VrSetForm(form2, 4);

                final List subclassList = (List) pageSubclassListList.get(sbpi);

                final Map classcdSubclasscdListMap = new HashMap();
                for (int subi = 0; subi < subclassList.size(); subi++) {
                    final String subclasscd = (String) subclassList.get(subi);
                    getMappedList(classcdSubclasscdListMap, getClasscd(subclasscd)).add(subclasscd);
                }

                svf.VrsOut("TITLE", title); // タイトル
                svf.VrsOut("PAGE1", String.valueOf(printedPage + sbpi + 1)); // ページ
                svf.VrsOut("PAGE2", String.valueOf(totalPage)); // ページ
                svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._loginDate)); // 印刷日付

                printStatHeader(svf, avgMap, stat);

                // 科目の列表示
                for (int i = 0; i < subclassList.size(); i++) {
                    final String subclasscd = (String) subclassList.get(i);
                    final String subclassname = (String) _param._subclassnameMap.get(subclasscd);
                    if (null != subclassname) {
                        svf.VrsOut("CLASS_NAME1", subclassname.length() > 4 ? subclassname.substring(0, 4) : subclassname); // 教科名
                    }

                    printSubclassStat(svf, avgMap, stat, subclasscd);
                    svf.VrEndRecord();
                }
                for (int i = subclassList.size(); i < maxColumn; i++) {
                    svf.VrsOut("CLASS_NAME1", "DUMMY"); // 素点
                    svf.VrAttribute("CLASS_NAME1", "X=10000"); // 素点
                    svf.VrEndRecord();
                }
            }
        }
    }

    // 統計ヘッダ印字
    private void printStatHeader(final Vrw32alp svf, final Map avgMap, final Stat stat) {
        
        for (final Iterator stit = stat._statMapList.iterator(); stit.hasNext();) {
            final Map keyValueMap = (Map) stit.next();
            for (final Iterator kvit = keyValueMap.entrySet().iterator(); kvit.hasNext();) {
                final Map.Entry e = (Map.Entry) kvit.next();
                final String key = (String) e.getKey();
                final List itemList = (List) e.getValue();
                if (Stat.MAX_MIN.equals(key)) {
                    
                    for (int j = 0; j < itemList.size(); j++) {
                        final int line = j + 1;
                        final String lineItem = (String) itemList.get(j);
                        if ("MAX".equals(lineItem)) {
//                            final ProficiencyAverage averageGrade999999 = ProficiencyAverage.getAverage(avgMap, ProficiencyAverage.avgKey("1", _param._grade, "000", "00000000", _999999));
//                            final ProficiencyAverage averageGrade999999AVG = ProficiencyAverage.getAverage(avgMap, ProficiencyAverage.avgKey("1", _param._grade, "000", "00000000", _999999AVG));

                            svf.VrsOutn("AVE_NAME4", line, "最高点"); // 平均名称
//                            svf.VrsOutn("TOTAL2", line, averageGrade999999._highscore); // 合計
//                            svf.VrsOutn("AVE2", line, sishagonyu(averageGrade999999AVG._highscore)); // 平均
                        } else if ("MIN".equals(lineItem)) {
//                            final ProficiencyAverage averageGrade999999 = ProficiencyAverage.getAverage(avgMap, ProficiencyAverage.avgKey("1", _param._grade, "000", "00000000", _999999));
//                            final ProficiencyAverage averageGrade999999AVG = ProficiencyAverage.getAverage(avgMap, ProficiencyAverage.avgKey("1", _param._grade, "000", "00000000", _999999AVG));

                            svf.VrsOutn("AVE_NAME4", line, "最低点"); // 平均名称
//                            svf.VrsOutn("TOTAL2", line, averageGrade999999._lowscore); // 合計
//                            svf.VrsOutn("AVE2", line, sishagonyu(averageGrade999999AVG._lowscore)); // 平均
                        }
                    }
                    
                } else if (Stat.HR_AVG.equals(key)) {
                    for (int j = 0; j < Math.min(itemList.size(), stat.maxLineHrAvg); j++) {
                        final int line = j + 1;
                        final Map hr = (Map) itemList.get(j);
                        final String hrClass = KnjDbUtils.getString(hr, "HR_CLASS");
                        
//                        log.info(" hr avg hrclass - " + hrClass);
                        
                        if (Stat.GRADE.equals(hrClass)) {
                            svf.VrsOutn("AVE_NAME1", line, "平　　均"); // 平均名称
                            final ProficiencyAverage averageHr999999 = ProficiencyAverage.getAverageGrade(avgMap, _param._grade, _999999);
                            final List score999999AvgList = getMappedList(stat._subclassScoreMap, Stat.statKey(_999999AVG, Stat.GRADE));

                            svf.VrsOutn("TOTAL2", line, sishagonyu(averageHr999999._avg)); // 合計
                            svf.VrsOutn("AVE2", line, sishagonyu(calcAverage(score999999AvgList))); // 平均

                        } else if (Stat.RYO.equals(hrClass)) {
                            svf.VrsOutn("AVE_NAME1", line, "寮生平均"); // 平均名称
                            final List score999999List = getMappedList(stat._subclassScoreMap, Stat.statKey(_999999, Stat.RYO));
                            final List score999999AvgList = getMappedList(stat._subclassScoreMap, Stat.statKey(_999999AVG, Stat.RYO));

                            svf.VrsOutn("TOTAL2", line, sishagonyu(calcAverage(score999999List))); // 合計
                            svf.VrsOutn("AVE2", line, sishagonyu(calcAverage(score999999AvgList))); // 平均
                        } else {
                            svf.VrsOutn("AVE_NAME1", line, StringUtils.defaultString(KnjDbUtils.getString(hr, "HR_CLASS_NAME1")) + "　平均"); // 平均名称
                            final ProficiencyAverage averageHr999999 = ProficiencyAverage.getAverageHr(avgMap, _param._grade, hrClass, _999999);
                            final List score999999AvgList = getMappedList(stat._subclassScoreMap, Stat.statKey(_999999AVG, hrClass));

                            svf.VrsOutn("TOTAL2", line, sishagonyu(averageHr999999._avg)); // 合計
                            svf.VrsOutn("AVE2", line, sishagonyu(calcAverage(score999999AvgList))); // 平均
                        }
                    }

                } else if (Stat.AKATEN.equals(key)) {
                    for (int j = 0; j < Math.min(itemList.size(), stat.maxLineRed); j++) {
                        final int line = j + 1;
                        final Map hr = (Map) itemList.get(j);
                        final String hrClass = KnjDbUtils.getString(hr, "HR_CLASS");
                        if (Stat.TOTAL.equals(hrClass)) {
                            svf.VrsOutn("AVE_NAME2", line, "赤点合計"); // 赤点名称

                            final List totalList = getMappedList(stat._akatenMap, Stat.statKey("TOTAL", "TOTAL")); // のべSCHREGNO
                            final List schregnoList = new ArrayList(new HashSet(totalList)); // 重複をのぞいたSCHREGNO
                            svf.VrsOut("RED_TOTAL", String.valueOf(totalList.size())); // 合計
                            svf.VrsOut("RED_NUM", String.valueOf(schregnoList.size())); // 平均
//                        } else if (Stat.RYO.equals(hrClass)) {
//                            svf.VrsOut("RED1_" + String.valueOf(line), String.valueOf(getMappedList(stat._akatenMap, Stat.statKey("TOTAL", Stat.RYO)).size())); // 合計
                        } else {
                            svf.VrsOutn("AVE_NAME2", line, StringUtils.defaultString(KnjDbUtils.getString(hr, "HR_CLASS_NAME1")) + "　合計"); // 赤点名称
                            svf.VrsOut("RED1_" + String.valueOf(line), String.valueOf(getMappedList(stat._akatenMap, Stat.statKey("TOTAL", hrClass)).size())); // 合計
                        }
                    }

                } else if (Stat.COUNT.equals(key)) {
                    for (int j = 0; j < Math.min(itemList.size(), stat.maxLineCount); j++) {
                        final int line = j + 1;
                        final Map hr = (Map) itemList.get(j);

                        final ProficiencyAverage averageHr999999;
                        if (Stat.TOTAL.equals(KnjDbUtils.getString(hr, "HR_CLASS"))) {
                            averageHr999999 = ProficiencyAverage.getAverageGrade(avgMap, _param._grade, _999999);

                            svf.VrsOutn("AVE_NAME3", line, "受験者数"); // 人数名称
                        } else {
                            final String hrClass = KnjDbUtils.getString(hr, "HR_CLASS");
                            averageHr999999 = ProficiencyAverage.getAverageHr(avgMap, _param._grade, hrClass, _999999);

                            svf.VrsOutn("AVE_NAME3", line, StringUtils.defaultString(KnjDbUtils.getString(hr, "HR_CLASS_NAME1")) + "  ( " + StringUtils.defaultString(KnjDbUtils.getString(hr, "REGD_COUNT")) + " )"); // 人数名称
                        }
                        svf.VrsOut("EXAM_NUM1_" + String.valueOf(line), averageHr999999._count); // 合計
//                            svf.VrsOutn("AVE4", line, String.valueOf(getMappedList(stat._kessekiMap, "HR_CLASS=" + hrClass).size())); // 平均

                    }
                }
            }
        }
    }

    // 統計データ印字 科目ごと
    private void printSubclassStat(final Vrw32alp svf,
            final Map avgMap,
            final Stat stat,
            final String subclasscd) {

        for (final Iterator stit = stat._statMapList.iterator(); stit.hasNext();) {
            final Map keyValueMap = (Map) stit.next();
            for (final Iterator kvit = keyValueMap.entrySet().iterator(); kvit.hasNext();) {
                final Map.Entry e = (Map.Entry) kvit.next();
                final String key = (String) e.getKey();
                final List itemList = (List) e.getValue();
                
                if (Stat.MAX_MIN.equals(key)) {
                    for (int j = 0; j < Math.min(itemList.size(), stat.maxLineMaxMin); j++) {
                        //final String line = String.valueOf(j + 1);
                        final String lineItem = (String) itemList.get(j);
                        final ProficiencyAverage averageGrade = ProficiencyAverage.getAverageGrade(avgMap, _param._grade, subclasscd);
                        if ("MAX".equals(lineItem)) {
                            svf.VrsOut("NAX1", averageGrade._highscore); // 平均
                        } else if ("MIN".equals(lineItem)) {
                            svf.VrsOut("MIN1", averageGrade._lowscore); // 平均
                        }
                    }
                } else if (Stat.HR_AVG.equals(key)) {
                    for (int j = 0; j < Math.min(itemList.size(), stat.maxLineHrAvg); j++) {
                        final int line = j + 1;
                        final Map hr = (Map) itemList.get(j);
                        final String hrClass = KnjDbUtils.getString(hr, "HR_CLASS");
                        if (Stat.GRADE.equals(hrClass)) {
                            svf.VrsOut("AVE1_" + line, sishagonyu(ProficiencyAverage.getAverageGrade(avgMap, _param._grade, subclasscd)._avg)); // 平均
                        } else if (Stat.RYO.equals(hrClass)) {
                            final List scoreList = getMappedList(stat._subclassScoreMap, Stat.statKey(subclasscd, Stat.RYO));
//                            log.info(" ryo scoreList (subclasscdcd = " + subclasscd + ") = " + scoreList);
                            svf.VrsOut("AVE1_" + line, sishagonyu(calcAverage(scoreList))); // 平均
                        } else {
                            svf.VrsOut("AVE1_" + line, sishagonyu(ProficiencyAverage.getAverageHr(avgMap, _param._grade, hrClass, subclasscd)._avg)); // 平均
                        }
                    }
                } else if (Stat.AKATEN.equals(key)) {
                    for (int j = 0; j < Math.min(itemList.size(), stat.maxLineRed); j++) {
                        final int line = j + 1;
                        final Map hr = (Map) itemList.get(j);
                        final String hrClass = KnjDbUtils.getString(hr, "HR_CLASS");
                        if (Stat.TOTAL.equals(hrClass)) {
                             // のべSCHREGNO
                            svf.VrsOut("RED1_" + line, String.valueOf(getMappedList(stat._akatenMap, Stat.statKey(subclasscd, "TOTAL")).size())); // 赤点
                        } else if (Stat.RYO.equals(hrClass)) {
                            svf.VrsOut("RED1_" + line, String.valueOf(getMappedList(stat._akatenMap, Stat.statKey(subclasscd, Stat.RYO)).size())); // 赤点
                        } else {
                            svf.VrsOut("RED1_" + line, String.valueOf(getMappedList(stat._akatenMap, Stat.statKey(subclasscd, hrClass)).size())); // 赤点
                        }
                    }
                } else if (Stat.COUNT.equals(key)) {
                    for (int j = 0; j < Math.min(itemList.size(), stat.maxLineCount); j++) {
                        final int line = j + 1;
                        final Map hr = (Map) itemList.get(j);
                        final String hrClass = KnjDbUtils.getString(hr, "HR_CLASS");
                        final ProficiencyAverage averageHr;
                        if (Stat.TOTAL.equals(hrClass)) {
                            averageHr = ProficiencyAverage.getAverageGrade(avgMap, _param._grade, subclasscd);
                        } else {
                            averageHr = ProficiencyAverage.getAverageHr(avgMap, _param._grade, hrClass, subclasscd);
                        }
                        svf.VrsOut("EXAM_NUM1_" + line, averageHr._count); // 人数
                    }
                }
            }
        }
    }

    /**
     * 得点のリストから平均を算出する
     * @param scoreList 得点のリスト。nullが含まれればNullPointerException
     * @return 平均
     */
    private static BigDecimal calcAverage(final List scoreList) {
        if (null == scoreList || scoreList.isEmpty()) {
            return null;
        }
        //log.info(" scoreList = " + scoreList);
        BigDecimal bd = new BigDecimal(0);
        for (int i = 0; i < scoreList.size(); i++) {
            final BigDecimal num = (BigDecimal) scoreList.get(i);
            bd = bd.add(num);
        }
        return bd.divide(new BigDecimal(scoreList.size()), 10, BigDecimal.ROUND_HALF_UP);
    }

    private static class Stat {
        
        private static final String MAX_MIN = "MAX_MIN";
        private static final String HR_AVG = "HR_AVG";
        private static final String AKATEN = "AKATEN";
        private static final String COUNT = "COUNT";
        private static final String TOTAL = "TOTAL";
        private static final String RYO = "RYO";
        private static final String GRADE = "GRADE";
        
        final int maxLineMaxMin = 2;
        final int maxLineHrAvg = 7;
        final int maxLineRed = 6;
        final int maxLineCount = 6;

        final Map _akatenMap = new HashMap();
        final Map _kessekiMap = new HashMap();
        final Map _subclassScoreMap = new HashMap();
        
        final List _statMapList;

        public Stat(final List students, final Param param) {
            for (final Iterator it = students.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();

                final boolean isRyo = "4".equals(student._residentcdNamespare1);
                for (final Iterator akait = student.getAkatenList(param).iterator(); akait.hasNext();) {
                    final String subclasscd = (String) akait.next();
                    getMappedList(_akatenMap, statKey(subclasscd, student._hrclass)).add(student._schregno);
                    getMappedList(_akatenMap, statKey(subclasscd, "TOTAL")).add(student._schregno);
                    getMappedList(_akatenMap, statKey("TOTAL", student._hrclass)).add(student._schregno);
                    getMappedList(_akatenMap, statKey("TOTAL", "TOTAL")).add(student._schregno);
                    if (isRyo) {
                        getMappedList(_akatenMap, statKey(subclasscd, RYO)).add(student._schregno);
                        getMappedList(_akatenMap, statKey("TOTAL", RYO)).add(student._schregno);
                    }
                }
                if (student.hasKesseki(param)) {
                    getMappedList(_kessekiMap, "HR_CLASS=" + student._hrclass).add(student._schregno);
                    getMappedList(_kessekiMap, "HR_CLASS=TOTAL").add(student._schregno);
                }
                
                for (final Iterator subit = student._ranks.entrySet().iterator(); subit.hasNext();) {
                    final Map.Entry e = (Map.Entry) subit.next();
                    final String subclasscd = (String) e.getKey();
                    final Rank rank = (Rank) e.getValue();
                    if (null == rank || !NumberUtils.isNumber(rank._score)) {
                        continue;
                    }
                    getMappedList(_subclassScoreMap, subclasscd).add(new BigDecimal(rank._score));
                    if (isRyo) {
                        getMappedList(_subclassScoreMap, statKey(subclasscd, RYO)).add(new BigDecimal(rank._score));
                    }
                    if (_999999.equals(subclasscd) && NumberUtils.isNumber(rank._avg)) {
                        getMappedList(_subclassScoreMap, statKey(_999999AVG, GRADE)).add(new BigDecimal(rank._avg));
                        getMappedList(_subclassScoreMap, statKey(_999999AVG, student._hrclass)).add(new BigDecimal(rank._avg));
                        if (isRyo) {
                            getMappedList(_subclassScoreMap, statKey(_999999AVG, RYO)).add(new BigDecimal(rank._avg));
                        }
                    }
                }
            }
            
            _statMapList = getStatMapList(param);
        }
        
        private static String statKey(final String subclasscd, final String hrClass) {
            return "SUBCLASS=" + subclasscd + "|HR_CLASS=" + hrClass;
        }
        
        private List getStatMapList(final Param param) {
            final List statMapList = new ArrayList();
            
            final Map ryoMap = new HashMap();
            ryoMap.put("HR_CLASS", RYO);
            ryoMap.put("HR_CLASS_NAME1", "寮生");

            final Map gradeMap = new HashMap();
            gradeMap.put("HR_CLASS", GRADE);
            gradeMap.put("HR_CLASS_NAME1", "");

            final Map totalMap = new HashMap();
            totalMap.put("HR_CLASS", TOTAL);
            totalMap.put("HR_CLASS_NAME1", "");

            final List hrAvgHrclassList = new ArrayList();
            hrAvgHrclassList.add(gradeMap);
            hrAvgHrclassList.addAll(param._hrList.size() <= 5 ? param._hrList : param._hrList.subList(0, 5));
            hrAvgHrclassList.add(ryoMap);
            statMapList.add(getKeyValueMap(HR_AVG, hrAvgHrclassList));


            final List akatenHrclassList = new ArrayList();
            akatenHrclassList.add(totalMap);
            akatenHrclassList.addAll(param._hrList);
//            akatenHrclassList.add(ryoMap);
            statMapList.add(getKeyValueMap(AKATEN, akatenHrclassList));

            final List countHrclassList = new ArrayList();
            countHrclassList.add(totalMap);
            countHrclassList.addAll(param._hrList);
            statMapList.add(getKeyValueMap(COUNT, countHrclassList));

            final List gradeAvgList = new ArrayList();
            gradeAvgList.add("MAX");
            gradeAvgList.add("MIN");
            statMapList.add(getKeyValueMap(MAX_MIN, gradeAvgList));

            return statMapList;
        }

        private Map getKeyValueMap(String key, Object val) {
            final Map keyValueMap = new HashMap();
            keyValueMap.put(key, val);
            return keyValueMap;
        }
    }

    private static class Param {
        final String _year;
        final String _semester;
        final String _ctrlSeme;
        final String _loginDate;
        final String _grade;
        final String _proficiencyDiv;
        final String _proficiencyCd;
        final String _selectDiv; // 印刷対象 1:クラス 2:学年
        final String _date;
        final String _ryoOnly;
        final Map _classnameMap;
        final Map _subclassnameMap;
//        final Map _coursecodenameMap;
        final String _juni; // 順位の基準点 1:総合点 2:平均点
        final String _sort; // 成績順 or 年組番号順
        String _testitemname = "";
        String _semestername = "";
        String _gradename = "";
        final String _schoolKind;
        final String[] _categorySelected;
        final String _schregSemester;
        final List _hrList;

        final int _akaten = 30;

        Param(final HttpServletRequest request, final DB2UDB db2) {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _ctrlSeme = request.getParameter("CTRL_SEME");
            _loginDate = request.getParameter("LOGIN_DATE");
            _grade = request.getParameter("GRADE");
            _proficiencyDiv = request.getParameter("PROFICIENCYDIV");
            _proficiencyCd = request.getParameter("PROFICIENCYCD");
            _selectDiv = request.getParameter("SELECT_DIV");
            _date = request.getParameter("DATE");
            _ryoOnly = request.getParameter("RYO_ONLY");
            _classnameMap = getClassnameMap(db2);
            _subclassnameMap = getSubclassnameMap(db2);
            _schregSemester = "9".equals(_semester) ? _ctrlSeme : _semester;
            setName(db2);
            _juni = request.getParameter("JUNI");
            _sort = request.getParameter("SORT");
            _schoolKind = getSchoolKind(db2);
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _hrList = getHrList(db2);
//            _coursecodenameMap = getCoursecodenameMap(db2);
        }

        private String getSchregRegdGdat(final DB2UDB db2, final String field) {
            final String sql = "SELECT " + field + " FROM    SCHREG_REGD_GDAT T1 WHERE   T1.YEAR = '" + _year + "' AND T1.GRADE = '" + _grade + "' ";
            final String rtn = KnjDbUtils.getOne(KnjDbUtils.query(db2, sql));
            return rtn;
        }

        private List getHrList(final DB2UDB db2) {
            String sql = "";
            sql += " SELECT T1.GRADE, T1.HR_CLASS, T1.HR_CLASS_NAME1, COUNT(REGD.SCHREGNO) AS REGD_COUNT ";
            sql += " FROM SCHREG_REGD_HDAT T1 ";
            sql += " INNER JOIN SCHREG_REGD_DAT REGD ON REGD.YEAR = T1.YEAR ";
            sql += "     AND REGD.SEMESTER = T1.SEMESTER ";
            sql += "     AND REGD.GRADE = T1.GRADE ";
            sql += "     AND REGD.HR_CLASS = T1.HR_CLASS ";
            sql += " WHERE T1.YEAR = '" + _year + "' AND T1.SEMESTER = '" + _semester + "' AND T1.GRADE = '" + _grade + "' ";
            sql += " GROUP BY T1.GRADE, T1.HR_CLASS, T1.HR_CLASS_NAME1 ";
            sql += " ORDER BY T1.GRADE, T1.HR_CLASS ";
            return KnjDbUtils.query(db2, sql);
        }

//        private List getCoursecodeList(final DB2UDB db2) {
//            final String sql = "SELECT DISTINCT COURSECD || MAJORCD || COURSECODE AS COURSE FROM SCHREG_REGD_DAT WHERE YEAR = '" + _year + "' AND GRADE = '" + _grade + "' ORDER BY COURSECD || MAJORCD ||  COURSECODE ";
//            return KnjDbUtils.getColumnDataList(KnjDbUtils.query(db2, sql), "COURSE");
//        }

        private void setName(final DB2UDB db2) {
            final String proficiencySql = "SELECT PROFICIENCYNAME1 FROM PROFICIENCY_MST WHERE PROFICIENCYDIV = '" + _proficiencyDiv + "' AND PROFICIENCYCD = '" + _proficiencyCd + "'";
            _testitemname = toStr(KnjDbUtils.getOne(KnjDbUtils.query(db2, proficiencySql)));

            final String sqlSemester = " SELECT SEMESTERNAME FROM SEMESTER_MST WHERE YEAR = '" + _year + "' AND SEMESTER = '" + _semester + "' ";
            _semestername = toStr(KnjDbUtils.getOne(KnjDbUtils.query(db2, sqlSemester)));

            final String sqlGrade = " SELECT GRADE_NAME1 FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND GRADE ='" + _grade + "' ";
            _gradename = toStr(KnjDbUtils.getOne(KnjDbUtils.query(db2, sqlGrade)));
        }

        private String getSchoolKind(final DB2UDB db2) {
            final String sql = "SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND GRADE = '" + _grade + "' ";
            final String rtn = KnjDbUtils.getOne(KnjDbUtils.query(db2, sql));
            return rtn;
        }

//        private Map getCoursecodenameMap(final DB2UDB db2) {
//            final StringBuffer stb = new StringBuffer();
//            stb.append(" SELECT DISTINCT ");
//            stb.append("     T2.COURSECD || T2.MAJORCD || T2.COURSECODE AS COURSE, ");
//            stb.append("     T1.COURSECODENAME ");
//            stb.append(" FROM ");
//            stb.append("     COURSECODE_MST T1 ");
//            stb.append("     INNER JOIN SCHREG_REGD_DAT T2 ON T2.YEAR = '" + _year + "' ");
//            stb.append("         AND T2.GRADE = '" + _grade + "' ");
//            stb.append("         AND T2.COURSECODE = T1.COURSECODE ");
//
//            return KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, stb.toString()), "COURSE", "COURSECODENAME");
//        }

        private Map getClassnameMap(final DB2UDB db2) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     CLASSCD || '-' || SCHOOL_KIND AS CLASSCD, ");
            stb.append("     VALUE(CLASSABBV, CLASSNAME) AS CLASSNAME ");
            stb.append(" FROM ");
            stb.append("     CLASS_MST ");

            return KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, stb.toString()), "CLASSCD", "CLASSNAME");
        }

        private Map getSubclassnameMap(final DB2UDB db2) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     PROFICIENCY_SUBCLASS_CD, ");
            stb.append("     VALUE(SUBCLASS_ABBV, SUBCLASS_NAME) AS SUBCLASS_NAME ");
            stb.append(" FROM ");
            stb.append("     PROFICIENCY_SUBCLASS_MST ");

            return KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, stb.toString()), "PROFICIENCY_SUBCLASS_CD", "SUBCLASS_NAME");
        }
    }
}
