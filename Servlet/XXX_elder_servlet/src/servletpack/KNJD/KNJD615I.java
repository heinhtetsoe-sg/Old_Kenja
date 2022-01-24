// kanji=漢字
/*
 * $Id: 379a5e78febd200fe381bc0f64826e9745dca279 $
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
import java.util.ArrayList;
import java.util.Collection;
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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 * 成績順一覧表（土佐塾）
 * @author maesiro
 * @version $Id: 379a5e78febd200fe381bc0f64826e9745dca279 $
 */
public class KNJD615I {
    private static final Log log = LogFactory.getLog(KNJD615I.class);

    private final String Gakunen = "000";

    private static final String _333333 = "333333";
    private static final String _555555 = "555555";
    private static final String _999999 = "999999";
    private static final String _99999A = "99999A";
    private static final String _99999B = "99999B";
    private static final String _999999AVG = "999999AVG";
    private static final String TESTCD_GAKUNENHYOTEI = "9990009";

    private static final String SORT_DIV_RANK = "1";
    private static final String SORT_DIV_ATTENDNO = "2";

    private Param _param;

    private boolean _hasData;

    private boolean _useForm_1_2;

    /**
     *  KNJD.classから最初に起動されます。
     */
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws ServletException, IOException {
        log.fatal("$Revision: 73586 $ $Date: 2020-04-08 19:20:02 +0900 (水, 08 4 2020) $"); // CVSキーワードの取り扱いに注意

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

            if ("2".equals(_param._printDiv)) {
                print1(db2, svf, "");
            } else {
                for (int i = 0; i < _param._categorySelected.length; i++) {
                    final String gradeHr = _param._categorySelected[i];
                    print1(db2, svf, gradeHr);
                }
            }
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

    private List getSubclasscds(final DB2UDB db2, final List students, final String gradeHr) {
        final Map courseSubclassCdMap = new HashMap(); // 表示する科目コード
        final String sql = getSubclassSql(_param, gradeHr);
        for (final Iterator it = KnjDbUtils.query(db2, sql).iterator(); it.hasNext();) {
            final Map row = (Map) it.next();
            if (null == courseSubclassCdMap.get(KnjDbUtils.getString(row, "COURSE"))) {
                courseSubclassCdMap.put(KnjDbUtils.getString(row, "COURSE"), new HashSet());
            }
            final Set subclassCdSet = (Set) courseSubclassCdMap.get(KnjDbUtils.getString(row, "COURSE"));
            subclassCdSet.add(KnjDbUtils.getString(row, "SUBCLASSCD"));
        }

        for (final Iterator itst = students.iterator(); itst.hasNext();) {
            final Student student = (Student) itst.next();
            if (null == courseSubclassCdMap.get(student._course)) {
                courseSubclassCdMap.put(student._course, new HashSet());
            }
            final Set subclassCdSet = (Set) courseSubclassCdMap.get(student._course);
            subclassCdSet.addAll(student._ranks.keySet());
        }

        Collection col = null;
        for (final Iterator it = courseSubclassCdMap.keySet().iterator(); it.hasNext();) {
            final String course = (String) it.next();
            final Set subclassCdSet = (Set) courseSubclassCdMap.get(course);
            if (null == col) {
                col = new HashSet(subclassCdSet);
            } else {
                col = CollectionUtils.union(col, subclassCdSet);
            }
        }
        if (null == col) {
            col = new HashSet();
        }
        for (final Iterator it = _param._notTargetSubclassCdList.iterator(); it.hasNext();) {
            final String subclasscd = (String) it.next();
            col.remove(subclasscd);
            log.debug("subclasscd="+subclasscd);
        }
        col.remove(_333333);
        col.remove(_555555);
        col.remove(_999999);
        col.remove(_99999A);
        col.remove(_99999B);
        final List rtn = new ArrayList(col);
        Collections.sort(rtn);
        return rtn;
    }

    private String getSubclassSql(final Param param, final String gradeHr) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT DISTINCT ");
        stb.append("     T2.COURSECD || T2.MAJORCD || T2.COURSECODE AS COURSE, ");
        stb.append("     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD ");
        stb.append(" FROM ");
        stb.append("     RECORD_SCORE_DAT T1 ");
        stb.append("     INNER JOIN SCHREG_REGD_DAT T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("         AND T2.YEAR = T1.YEAR ");
        stb.append("         AND T2.SEMESTER = '" + param._schregSemester + "' ");
        if ("1".equals(param._printDiv)) {
            stb.append("     AND T2.GRADE || '-' || T2.HR_CLASS = '" + gradeHr + "' ");
        }
        if ("1".equals(param._use_school_detail_gcm_dat)) {
            stb.append("     AND T2.COURSECD || '-' || T2.MAJORCD = '" + param._major + "' ");
        }
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '" + _param._testcd + "' ");
        stb.append("     AND T2.GRADE = '" + _param._grade +"' ");

        stb.append(" ORDER BY ");
        stb.append("     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD ");
        return stb.toString();
    }

    private final List getPageStudentListList(final List students, final int max) {
        return getPageList(students, max);
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
        final String _sp;
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
        public Student(final String schregno, final String sp, final String entDiv, final String name, final String grade, final String hrclass, final String hrClassName1, final String course, final String coursecode, final String coursecodeName, final String attendno, final String sex, final String inoutCd, final String residentcdAbbv2, final String residentcdNamespare1) {
            _schregno = schregno;
            _sp = sp;
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

        public static List getStudentList(final DB2UDB db2, final Param param, final String gradeHr) {

            final Map studentMap = new HashMap();
            final String sql = Student.sqlStudent(param, gradeHr);
            for (final Iterator it = KnjDbUtils.query(db2, sql).iterator(); it.hasNext();) {
                final Map row = (Map) it.next();

                final Student student = new Student(KnjDbUtils.getString(row, "SCHREGNO"), KnjDbUtils.getString(row, "SP"), KnjDbUtils.getString(row, "ENT_DIV"), KnjDbUtils.getString(row, "NAME"), KnjDbUtils.getString(row, "GRADE"), KnjDbUtils.getString(row, "HR_CLASS"), KnjDbUtils.getString(row, "HR_CLASS_NAME1"), KnjDbUtils.getString(row, "COURSE"), KnjDbUtils.getString(row, "COURSECODE"), KnjDbUtils.getString(row, "COURSECODENAME"), KnjDbUtils.getString(row, "ATTENDNO"), KnjDbUtils.getString(row, "SEX"), KnjDbUtils.getString(row, "INOUTCD"), KnjDbUtils.getString(row, "RESIDENTCD_ABBV2"), KnjDbUtils.getString(row, "RESIDENTCD_NAMESPARE1"));
                studentMap.put(student._schregno, student);
            }

            final String sqlRank = Rank.sqlRank(param, gradeHr);
            for (final Iterator it = KnjDbUtils.query(db2, sqlRank).iterator(); it.hasNext();) {
                final Map row = (Map) it.next();

                final Student student = (Student) studentMap.get(KnjDbUtils.getString(row, "SCHREGNO"));
                if (null == student) {
                    continue;
                }
                final String subclasscd;
                if (!(_333333.equals(KnjDbUtils.getString(row, "SUBCLASSCD")) || _555555.equals(KnjDbUtils.getString(row, "SUBCLASSCD")) || _999999.equals(KnjDbUtils.getString(row, "SUBCLASSCD")) || _99999A.equals(KnjDbUtils.getString(row, "SUBCLASSCD")) || _99999B.equals(KnjDbUtils.getString(row, "SUBCLASSCD")) || _999999AVG.equals(KnjDbUtils.getString(row, "SUBCLASSCD")))) {
                    subclasscd = KnjDbUtils.getString(row, "CLASSCD") + '-' + KnjDbUtils.getString(row, "SCHOOL_KIND") + '-' + KnjDbUtils.getString(row, "CURRICULUM_CD") + '-' + KnjDbUtils.getString(row, "SUBCLASSCD");
                } else {
                    subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");
                }
                final String valueDi = KnjDbUtils.getString(row, "VALUE_DI");
                final String score = null != valueDi ? valueDi : KnjDbUtils.getString(row, "SCORE");
                final String avg = KnjDbUtils.getString(row, "AVG");
                final Integer gradeRank = toInteger(KnjDbUtils.getString(row, "2".equals(param._outputKijun) ? "GRADE_AVG_RANK" : "GRADE_RANK"));
                final Integer classRank = toInteger(KnjDbUtils.getString(row, "2".equals(param._outputKijun) ? "CLASS_AVG_RANK" : "CLASS_RANK"));
                final Integer courseRank = toInteger(KnjDbUtils.getString(row, "2".equals(param._outputKijun) ? "COURSE_AVG_RANK" : "COURSE_RANK"));
                final Integer courseGroupRank = toInteger(KnjDbUtils.getString(row, "2".equals(param._outputKijun) ? "COURSEGROUP_AVG_RANK" : "COURSEGROUP_RANK"));
                student._ranks.put(subclasscd, new Rank(subclasscd, score, avg, gradeRank, classRank, courseRank, courseGroupRank));
            }

            final List rtn = new ArrayList();
            rtn.addAll(studentMap.values());

            Collections.sort(rtn, new StudentSorter(param));

            return rtn;
        }

        private static String sqlStudent(final Param param, final String gradeHr) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     REGD.SCHREGNO, ");
            stb.append("     BASE.NAME, ");
            stb.append("     CASE WHEN SCHOLARSHIP.SCHREGNO IS NOT NULL THEN '特' ELSE '' END AS SP, ");
            stb.append("     A002.ABBV1 AS ENT_DIV, ");
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
            stb.append("     LEFT JOIN NAME_MST A002 ON A002.NAMECD1 = 'A002' ");
            stb.append("         AND A002.NAMECD2 = ENTGRD.ENT_DIV ");

            stb.append("     LEFT JOIN ( ");
            stb.append("         SELECT SCHREGNO ");
            stb.append("         FROM SCHREG_SCHOLARSHIP_HIST_DAT SCHOLARSHIP ");
            stb.append("         LEFT JOIN SEMESTER_MST SEMES ON SEMES.YEAR = '" + param._year + "' ");
            stb.append("             AND SEMES.SEMESTER = '" + param._schregSemester + "' ");
            stb.append("         WHERE ( ");
            stb.append("             FROM_DATE BETWEEN SEMES.SDATE AND SEMES.EDATE ");
            stb.append("             OR SEMES.SDATE BETWEEN FROM_DATE AND VALUE(TO_DATE, '9999-12-31') ");
            stb.append("         ) ");
            stb.append("         GROUP BY SCHREGNO ");
            stb.append("     ) SCHOLARSHIP ON SCHOLARSHIP.SCHREGNO = REGD.SCHREGNO ");

            stb.append(" WHERE ");
            stb.append("     REGD.YEAR = '" + param._year + "' ");
            stb.append("     AND REGD.SEMESTER = '" + param._schregSemester + "' ");
            stb.append("     AND REGD.GRADE = '" + param._grade + "' ");
            if ("1".equals(param._printDiv)) {
                stb.append("     AND REGD.GRADE || '-' || REGD.HR_CLASS = '" + gradeHr + "' ");
            }
            if ("1".equals(param._use_school_detail_gcm_dat)) {
                stb.append("     AND REGD.COURSECD || '-' || REGD.MAJORCD = '" + param._major + "' ");
            }
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
            for (final Iterator it = _ranks.values().iterator(); it.hasNext();) {
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
            if (SORT_DIV_RANK.equals(_param._sortdiv)) {
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

    private static class Average {

        private static Average NULL = new Average(null, null, null, null, null, null, null);

        final String _subclasscd;
        final String _score;
        final String _highscore;
        final String _lowscore;
        final String _count;
        final String _avg;
        final String _stddev;
        public Average(final String subclasscd, final String score, final String highscore, final String lowscore, final String count, final String avg, final String stddev) {
            _subclasscd = subclasscd;
            _score = score;
            _highscore = highscore;
            _lowscore = lowscore;
            _count = count;
            _avg = avg;
            _stddev = stddev;
        }

        public static Average getAverage(final Map avgMap, final String avgKey) {
            Average average = (Average) avgMap.get(avgKey);
            if (null == average) {
                return NULL;
            }
            return average;
        }

        public static String avgKey(final String avgDiv, final Student student, final String subclasscd) {
            if ("1".equals(avgDiv)) {
                return avgKey(avgDiv, student._grade, "000", "00000000", subclasscd);
            } else if ("2".equals(avgDiv)) {
                return avgKey(avgDiv, student._grade, student._hrclass, "00000000", subclasscd);
            } else if ("3".equals(avgDiv)) {
                return avgKey(avgDiv, student._grade, "000", student._course, subclasscd);
            }
            return null;
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
            final String sql = Average.sqlAverage(param);
            for (final Iterator it = KnjDbUtils.query(db2, sql).iterator(); it.hasNext();) {
                final Map row = (Map) it.next();

                final String subclasscd;
                if (!(_333333.equals(KnjDbUtils.getString(row, "SUBCLASSCD")) || _555555.equals(KnjDbUtils.getString(row, "SUBCLASSCD")) || _999999.equals(KnjDbUtils.getString(row, "SUBCLASSCD")) || _99999A.equals(KnjDbUtils.getString(row, "SUBCLASSCD")) || _99999B.equals(KnjDbUtils.getString(row, "SUBCLASSCD")) || _999999AVG.equals(KnjDbUtils.getString(row, "SUBCLASSCD")))) {
                    subclasscd = KnjDbUtils.getString(row, "CLASSCD") + '-' + KnjDbUtils.getString(row, "SCHOOL_KIND") + '-' + KnjDbUtils.getString(row, "CURRICULUM_CD") + '-' + KnjDbUtils.getString(row, "SUBCLASSCD");
                } else {
                    subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");
                }
                final String grade = KnjDbUtils.getString(row, "GRADE");
                final String hrClass = KnjDbUtils.getString(row, "HR_CLASS");
                final String course = KnjDbUtils.getString(row, "COURSE");
                final String score = bdSetScale(KnjDbUtils.getString(row, "SCORE"));
                final String highscore = bdSetScale(KnjDbUtils.getString(row, "HIGHSCORE"));
                final String lowscore = bdSetScale(KnjDbUtils.getString(row, "LOWSCORE"));
                final String count = bdSetScale(KnjDbUtils.getString(row, "COUNT"));
                final String avg = KnjDbUtils.getString(row, "AVG");
                final String stddev = KnjDbUtils.getString(row, "STDDEV");
                final Average average = new Average(subclasscd, score, highscore, lowscore, count, avg, stddev);

                final String avgKey = avgKey(KnjDbUtils.getString(row, "AVG_DIV"), grade, hrClass, course, subclasscd);
                rtn.put(avgKey, average);
            }
            return rtn;
        }

        private static String sqlAverage(final Param param) {
            final StringBuffer stb = new StringBuffer();

            stb.append(" WITH RANK9 AS ( ");
            stb.append(" SELECT ");
            stb.append("     REGD.GRADE, REGD.HR_CLASS, REGD.COURSECD || REGD.MAJORCD || REGD.COURSECODE AS COURSE ");
            stb.append("     , CASE WHEN N1.NAMESPARE1 = '4' THEN '1' END AS RYOUSEI_FLG ");
            stb.append("     , T1.AVG ");
            stb.append(" FROM ");
            stb.append("     RECORD_RANK_SDIV_DAT T1 ");
            stb.append("     INNER JOIN (SELECT SCHREGNO, YEAR, GRADE, HR_CLASS, COURSECD, MAJORCD, COURSECODE ");
            stb.append("                 FROM SCHREG_REGD_DAT ");
            stb.append("                 WHERE YEAR = '" + param._year +"' ");
            stb.append("                 AND   SEMESTER = '" + param._schregSemester+ "' ");
            stb.append("                ) REGD ON REGD.YEAR = T1.YEAR AND REGD.SCHREGNO = T1.SCHREGNO");
            stb.append("        LEFT JOIN SCHREG_REGD_GDAT S3 ");
            stb.append("             ON S3.YEAR         = REGD.YEAR ");
            stb.append("            AND S3.GRADE        = REGD.GRADE ");
            stb.append("        LEFT JOIN SCHREG_BRANCH_DAT S4 ");
            stb.append("             ON S4.SCHOOLCD     = '000000000000' ");
            stb.append("            AND S4.SCHOOL_KIND  = S3.SCHOOL_KIND ");
            stb.append("            AND S4.YEAR         = REGD.YEAR ");
            stb.append("            AND S4.SCHREGNO     = REGD.SCHREGNO ");
            stb.append("        LEFT JOIN V_NAME_MST N1 ");
            stb.append("             ON N1.YEAR         = REGD.YEAR ");
            stb.append("            AND N1.NAMECD1      = 'J008' ");
            stb.append("            AND N1.NAMECD2      = S4.RESIDENTCD ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.SEMESTER = '" + param._semester + "' ");
            stb.append("     AND T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '" + param._testcd + "' ");
            stb.append("     AND T1.SUBCLASSCD = '" + _999999 + "' ");
            stb.append("     AND REGD.GRADE = '" + param._grade + "' ");
            stb.append(" ) ");

            stb.append(" SELECT ");
            stb.append("     T1.AVG_DIV, ");
            stb.append("     T1.GRADE, ");
            stb.append("     T1.HR_CLASS, ");
            stb.append("     T1.COURSECD || T1.MAJORCD || T1.COURSECODE AS COURSE, ");
            stb.append("     T1.CLASSCD, ");
            stb.append("     T1.SCHOOL_KIND, ");
            stb.append("     T1.CURRICULUM_CD, ");
            stb.append("     T1.SUBCLASSCD, ");
            stb.append("     T2.SUBCLASSCD AS SUBCLASS_MST_SUBCLSSCD, ");
            stb.append("     T1.SCORE, ");
            stb.append("     T1.HIGHSCORE, ");
            stb.append("     T1.LOWSCORE, ");
            stb.append("     T1.COUNT, ");
            stb.append("     T1.AVG, ");
            stb.append("     T1.STDDEV ");
            stb.append(" FROM ");
            stb.append("  RECORD_AVERAGE_SDIV_DAT T1 ");
            stb.append("     LEFT JOIN SUBCLASS_MST T2 ON T2.CLASSCD = T1.CLASSCD ");
            stb.append("         AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("         AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("         AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.SEMESTER = '" + param._semester + "' ");
            stb.append("     AND T1.GRADE = '" + param._grade + "' ");
            stb.append("     AND T1.AVG_DIV IN ('1', '2', '3', '7') "); // 学科、クラス、コース
            stb.append("     AND T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '" + param._testcd + "' ");

            // 学年
            stb.append(" UNION ALL ");
            stb.append(" SELECT ");
            stb.append("     '1' AS AVG_DIV, T1.GRADE, '000' AS HR_CLASS, '00000000' AS COURSE, ");
            stb.append("     CAST(NULL AS VARCHAR(1)) AS CLASSCD, ");
            stb.append("     CAST(NULL AS VARCHAR(1)) AS SCHOOL_KIND, ");
            stb.append("     CAST(NULL AS VARCHAR(1)) AS CURRICULUM_CD, ");
            stb.append("     '999999AVG' AS SUBCLASSCD, ");
            stb.append("     CAST(NULL AS VARCHAR(1)) AS SUBCLASS_MST_SUBCLSSCD, ");
            stb.append("     SUM(DOUBLE(AVG)) AS SCORE, ");
            stb.append("     MAX(DOUBLE(AVG)) AS HIGHSCORE, ");
            stb.append("     MIN(DOUBLE(AVG)) AS LOWSCORE, ");
            stb.append("     COUNT(DOUBLE(T1.AVG)) AS COUNT, ");
            stb.append("     AVG(DOUBLE(T1.AVG)) AS AVG, ");
            stb.append("     STDDEV(DOUBLE(T1.AVG)) AS STDDEV ");
            stb.append(" FROM ");
            stb.append("     RANK9 T1 ");
            stb.append(" GROUP BY ");
            stb.append("     T1.GRADE ");
            // 年組
            stb.append(" UNION ALL ");
            stb.append(" SELECT ");
            stb.append("     '2' AS AVG_DIV, T1.GRADE, T1.HR_CLASS, '00000000' AS COURSE, ");
            stb.append("     CAST(NULL AS VARCHAR(1)) AS CLASSCD, ");
            stb.append("     CAST(NULL AS VARCHAR(1)) AS SCHOOL_KIND, ");
            stb.append("     CAST(NULL AS VARCHAR(1)) AS CURRICULUM_CD, ");
            stb.append("     '999999AVG' AS SUBCLASSCD, ");
            stb.append("     CAST(NULL AS VARCHAR(1)) AS SUBCLASS_MST_SUBCLSSCD, ");
            stb.append("     SUM(DOUBLE(AVG)) AS SCORE, ");
            stb.append("     MAX(DOUBLE(AVG)) AS HIGHSCORE, ");
            stb.append("     MIN(DOUBLE(AVG)) AS LOWSCORE, ");
            stb.append("     COUNT(DOUBLE(T1.AVG)) AS COUNT, ");
            stb.append("     AVG(DOUBLE(T1.AVG)) AS AVG, ");
            stb.append("     STDDEV(DOUBLE(T1.AVG)) AS STDDEV ");
            stb.append(" FROM ");
            stb.append("     RANK9 T1 ");
            stb.append(" GROUP BY ");
            stb.append("     T1.GRADE, T1.HR_CLASS ");
            // コース
            stb.append(" UNION ALL ");
            stb.append(" SELECT ");
            stb.append("     '3' AS AVG_DIV, T1.GRADE, '000' AS HR_CLASS, T1.COURSE AS COURSE, ");
            stb.append("     CAST(NULL AS VARCHAR(1)) AS CLASSCD, ");
            stb.append("     CAST(NULL AS VARCHAR(1)) AS SCHOOL_KIND, ");
            stb.append("     CAST(NULL AS VARCHAR(1)) AS CURRICULUM_CD, ");
            stb.append("     '999999AVG' AS SUBCLASSCD, ");
            stb.append("     CAST(NULL AS VARCHAR(1)) AS SUBCLASS_MST_SUBCLSSCD, ");
            stb.append("     SUM(DOUBLE(AVG)) AS SCORE, ");
            stb.append("     MAX(DOUBLE(AVG)) AS HIGHSCORE, ");
            stb.append("     MIN(DOUBLE(AVG)) AS LOWSCORE, ");
            stb.append("     COUNT(DOUBLE(T1.AVG)) AS COUNT, ");
            stb.append("     AVG(DOUBLE(T1.AVG)) AS AVG, ");
            stb.append("     STDDEV(DOUBLE(T1.AVG)) AS STDDEV ");
            stb.append(" FROM ");
            stb.append("     RANK9 T1 ");
            stb.append(" GROUP BY ");
            stb.append("     T1.GRADE, T1.COURSE ");
            // 寮
            stb.append(" UNION ALL ");
            stb.append(" SELECT ");
            stb.append("     '7' AS AVG_DIV, T1.GRADE, '000' AS HR_CLASS, '00000000' AS COURSE, ");
            stb.append("     CAST(NULL AS VARCHAR(1)) AS CLASSCD, ");
            stb.append("     CAST(NULL AS VARCHAR(1)) AS SCHOOL_KIND, ");
            stb.append("     CAST(NULL AS VARCHAR(1)) AS CURRICULUM_CD, ");
            stb.append("     '999999AVG' AS SUBCLASSCD, ");
            stb.append("     CAST(NULL AS VARCHAR(1)) AS SUBCLASS_MST_SUBCLSSCD, ");
            stb.append("     SUM(DOUBLE(AVG)) AS SCORE, ");
            stb.append("     MAX(DOUBLE(AVG)) AS HIGHSCORE, ");
            stb.append("     MIN(DOUBLE(AVG)) AS LOWSCORE, ");
            stb.append("     COUNT(DOUBLE(T1.AVG)) AS COUNT, ");
            stb.append("     AVG(DOUBLE(T1.AVG)) AS AVG, ");
            stb.append("     STDDEV(DOUBLE(T1.AVG)) AS STDDEV ");
            stb.append(" FROM ");
            stb.append("     RANK9 T1 ");
            stb.append(" WHERE ");
            stb.append("     RYOUSEI_FLG = '1' ");
            stb.append(" GROUP BY ");
            stb.append("     T1.GRADE ");

            return stb.toString();
        }
    }

    private static class Rank {
        final String _subclasscd;
        final String _score;
        final String _avg;
        final Integer _gradeRank;
        final Integer _classRank;
        final Integer _courseRank;
        final Integer _courseGroupRank;
        public Rank(final String subclasscd, final String score, final String avg, final Integer gradeRank, final Integer classRank, final Integer courseRank, final Integer majorRank) {
            _subclasscd = subclasscd;
            _score = score;
            _avg = avg;
            _gradeRank = gradeRank;
            _classRank = classRank;
            _courseRank = courseRank;
            _courseGroupRank = majorRank;
        }

        private static String sqlRank(final Param param, final String gradeHr) {
            final StringBuffer stb = new StringBuffer();
            
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     TSCORE.CLASSCD, ");
            stb.append("     TSCORE.SCHOOL_KIND, ");
            stb.append("     TSCORE.CURRICULUM_CD, ");
            stb.append("     TSCORE.SUBCLASSCD, ");
            stb.append("     TSCORE.SCORE, ");
            stb.append("     TSCORE.VALUE_DI, ");
            stb.append("     CAST(NULL AS DOUBLE) AS AVG, ");
            stb.append("     CAST(NULL AS SMALLINT) AS GRADE_RANK, ");
            stb.append("     CAST(NULL AS SMALLINT) AS CLASS_RANK, ");
            stb.append("     CAST(NULL AS SMALLINT) AS COURSE_RANK, ");
            stb.append("     CAST(NULL AS SMALLINT) AS COURSEGROUP_RANK, ");
            stb.append("     CAST(NULL AS SMALLINT) AS GRADE_AVG_RANK, ");
            stb.append("     CAST(NULL AS SMALLINT) AS CLASS_AVG_RANK, ");
            stb.append("     CAST(NULL AS SMALLINT) AS COURSE_AVG_RANK, ");
            stb.append("     CAST(NULL AS SMALLINT) AS COURSEGROUP_AVG_RANK ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT T1 ");
            stb.append("     LEFT JOIN RECORD_SCORE_DAT TSCORE ON TSCORE.YEAR = T1.YEAR ");
            stb.append("         AND TSCORE.SEMESTER = '" + param._semester + "' ");
            stb.append("         AND TSCORE.SCHREGNO = T1.SCHREGNO ");
            stb.append(" WHERE ");
            stb.append("     T1.GRADE = '" + param._grade + "' ");
            stb.append("     AND T1.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.SEMESTER = '" + param._schregSemester + "' ");
            stb.append("     AND TSCORE.TESTKINDCD || TSCORE.TESTITEMCD || TSCORE.SCORE_DIV = '" + param._testcd + "' ");
            stb.append("     AND TSCORE.VALUE_DI IS NOT NULL ");
            if ("1".equals(param._printDiv)) {
                stb.append("     AND T1.GRADE || '-' || T1.HR_CLASS = '" + gradeHr + "' ");
            }
            if ("1".equals(param._use_school_detail_gcm_dat)) {
                stb.append("     AND T1.COURSECD || '-' || T1.MAJORCD = '" + param._major + "' ");
            }
            stb.append(" UNION ALL ");
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T2.CLASSCD, ");
            stb.append("     T2.SCHOOL_KIND, ");
            stb.append("     T2.CURRICULUM_CD, ");
            stb.append("     T2.SUBCLASSCD, ");
            stb.append("     T2.SCORE, ");
            stb.append("     CAST(NULL AS VARCHAR(1)) AS VALUE_DI, ");
            stb.append("     T2.AVG, ");
            stb.append("     T2.GRADE_RANK, ");
            stb.append("     T2.CLASS_RANK, ");
            stb.append("     T2.COURSE_RANK, ");
            stb.append("     T2.MAJOR_RANK AS COURSEGROUP_RANK, ");
            stb.append("     T2.GRADE_AVG_RANK, ");
            stb.append("     T2.CLASS_AVG_RANK, ");
            stb.append("     T2.COURSE_AVG_RANK, ");
            stb.append("     T2.MAJOR_AVG_RANK AS COURSEGROUP_AVG_RANK ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT T1 ");
            stb.append("     LEFT JOIN RECORD_RANK_SDIV_DAT T2 ON T2.YEAR = T1.YEAR ");
            stb.append("         AND T2.SEMESTER = '" + param._semester + "' ");
            stb.append("         AND T2.SCHREGNO = T1.SCHREGNO ");
            stb.append(" WHERE ");
            stb.append("     T1.GRADE = '" + param._grade + "' ");
            stb.append("     AND T1.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.SEMESTER = '" + param._schregSemester + "' ");
            stb.append("     AND T2.TESTKINDCD || T2.TESTITEMCD || T2.SCORE_DIV = '" + param._testcd + "' ");
            if ("1".equals(param._printDiv)) {
                stb.append("     AND T1.GRADE || '-' || T1.HR_CLASS = '" + gradeHr + "' ");
            }
            if ("1".equals(param._use_school_detail_gcm_dat)) {
                stb.append("     AND T1.COURSECD || '-' || T1.MAJORCD = '" + param._major + "' ");
            }
            stb.append(" ORDER BY ");
            stb.append("     SCHREGNO, ");
            stb.append("     CLASSCD, ");
            stb.append("     SCHOOL_KIND, ");
            stb.append("     CURRICULUM_CD, ");
            stb.append("     SUBCLASSCD ");
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
    
    private void print1(final DB2UDB db2, final Vrw32alp svf, final String gradeHr) {
        final String form = "KNJD615I_1.frm";
        final int maxColumn = 29;
        final int maxLine = 65;

        final Map avgMap = Average.getAverageMap(db2, _param);

        final List studentsAll = Student.getStudentList(db2, _param, gradeHr);
        final List students;
        if ("1".equals(_param._ryoOnly)) {
            // 寮生のみ
            students = new ArrayList();
            for (final Iterator it = studentsAll.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                if ("4".equals(student._residentcdNamespare1)) {
                    students.add(student);
                }
            }
        } else {
            students = studentsAll;
        }
        final List allsubclasscds = getSubclasscds(db2, students, gradeHr);

        final List pageSubclassListList = getPageList(allsubclasscds, maxColumn);

        final List pageStudentListList = getPageStudentListList(students, maxLine);
        int totalPage = pageStudentListList.size() * pageSubclassListList.size();
        if ("2".equals(_param._printDiv)) {
            // 平均点のページを含める
            totalPage += pageSubclassListList.size();
        }

        final String title = KNJ_EditDate.gengou(db2, Integer.parseInt(_param._year)) + "年度　" + StringUtils.defaultString(_param._gradename) + "　" + StringUtils.defaultString(_param._semestername) + "　" + StringUtils.defaultString(_param._testitemname);
        final String printDateStr = KNJ_EditDate.h_format_JP(db2, _param._loginDate);

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

            	if (stpi == pageStudentListList.size() - 1 && sbpi == pageSubclassListList.size() - 1) {
            		// 最後のページに赤点数計を印字
                    final Map akatenMap = new HashMap();
                    for (final Iterator it = students.iterator(); it.hasNext();) {
                        final Student student = (Student) it.next();

                        for (final Iterator akait = student.getAkatenList(_param).iterator(); akait.hasNext();) {
                            akait.next();
                            final String key = "2".equals(_param._printDiv) ? "TOTAL" : "HR_CLASS=" + student._grade + "-" + student._hrclass;
                            getMappedList(akatenMap, key).add(student._schregno);
                        }
                    }
                    final String key = "2".equals(_param._printDiv) ? "TOTAL" : "HR_CLASS=" + gradeHr;
                    final List akatenSchregnoList = getMappedList(akatenMap, key);
                    final Set akatenSchregnoSet = new HashSet(akatenSchregnoList);
                    svf.VrsOut("FOOTER", String.valueOf(akatenSchregnoList.size()) + "/" + String.valueOf(akatenSchregnoSet.size()) + "  赤点/人数");
                }

                svf.VrsOut("TITLE", title); // タイトル
                svf.VrsOut("PAGE1", String.valueOf(page)); // ページ
                svf.VrsOut("PAGE2", String.valueOf(totalPage)); // ページ
                svf.VrsOut("DATE", printDateStr); // 印刷日付

                for (int sti = 0; sti < studentList.size(); sti++) {
                    final Student student = (Student) studentList.get(sti);
                    final int line = sti + 1;
                    svf.VrsOutn("DOMITORY", line, student._residentcdAbbv2); // 寮生
                    svf.VrsOutn("SP", line, student._sp); // 特待
//                    String entDivName = "";
//                    if ("1".equals(student._entDiv)) {
//                        entDivName = "";
//                    } else if ("2".equals(student._entDiv)) {
//                        entDivName = "後";
//                    } else if ("3".equals(student._entDiv)) {
//                        entDivName = "転";
//                    }
                    svf.VrsOutn("DIV", line, student._entDiv); // 区分
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
                        svf.VrsOutn("HR_RANK", line, toStr(rank999999._classRank)); // クラス順位
                        svf.VrsOutn("COURSE_RANK", line, toStr(rank999999._courseRank)); // コース順位
                        svf.VrsOutn("GRADE_RANK", line, toStr(rank999999._gradeRank)); // 学年順位
                    }
                    final Param param = _param;
                    svf.VrsOutn("RED_POINT", line, String.valueOf(student.getAkatenList(param).size())); // 赤点
                }

                for (int subi = 0; subi < subclassList.size(); subi++) {

                    final String subclasscd = (String) subclassList.get(subi);
                    svf.VrsOut("GRPCD", getClasscd(subclasscd)); // グループコード
                    svf.VrsOut("CLASS_NAME", getColumnClassname(subclasscd, 4, classcdSubclasscdListMap)); // 教科名
                    final String subclassname = (String) _param._subclassnameMap.get(subclasscd);
                    svf.VrsOut("SUBCLASS_NAME" + (getMS932ByteCount(subclassname) <= 4 ? "1" : "2"), subclassname); // 科目名

                    for (int sti = 0; sti < studentList.size(); sti++) {
                        final Student student = (Student) studentList.get(sti);
                        final String line = String.valueOf(sti + 1);
                        final Rank rank = (Rank) student._ranks.get(subclasscd);
                        if (null != rank) {
                            svf.VrsOut("SCORE" + line, rank._score); // 素点
                            if (isAkaten(rank, _param)) {
                                svf.VrAttribute("SCORE" + line, "Palette=9,UnderLine=(0,1,1)"); // 素点
                            }
                        }
                    }
                    svf.VrEndRecord();
                }

                for (int i = subclassList.size(); i < maxColumn; i++) {
                    svf.VrEndRecord();
                }
                _hasData = true;
            }
        }
        if ("2".equals(_param._printDiv) && _hasData) {

            final Map akatenMap = new HashMap();
            final Map kessekiMap = new HashMap();
            final Map subclassScoreMap = new HashMap();
            for (final Iterator it = studentsAll.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();

                for (final Iterator akait = student.getAkatenList(_param).iterator(); akait.hasNext();) {
                    final String subclasscd = (String) akait.next();
                    getMappedList(akatenMap, "SUBCLASS=" + subclasscd + "|HR_CLASS=" + student._hrclass).add(student._schregno);
                    getMappedList(akatenMap, "SUBCLASS=" + subclasscd + "|HR_CLASS=TOTAL").add(student._schregno);
                    getMappedList(akatenMap, "SUBCLASS=TOTAL|HR_CLASS=" + student._hrclass).add(student._schregno);
                    getMappedList(akatenMap, "SUBCLASS=TOTAL|HR_CLASS=TOTAL").add(student._schregno);
                    if ("4".equals(student._residentcdNamespare1)) {
                        getMappedList(akatenMap, "SUBCLASS=" + subclasscd + "|HR_CLASS=RYO").add(student._schregno);
                        getMappedList(akatenMap, "SUBCLASS=TOTAL|HR_CLASS=RYO").add(student._schregno);
                    }
                }
                if (student.hasKesseki(_param)) {
                    getMappedList(kessekiMap, "HR_CLASS=" + student._hrclass).add(student._schregno);
                    getMappedList(kessekiMap, "HR_CLASS=TOTAL").add(student._schregno);
                }
                
                for (final Iterator subit = student._ranks.entrySet().iterator(); subit.hasNext();) {
                    final Map.Entry e = (Map.Entry) subit.next();
                    final String subclasscd = (String) e.getKey();
                    final Rank rank = (Rank) e.getValue();
                    if (null == rank || !NumberUtils.isNumber(rank._score)) {
                        continue;
                    }
                    getMappedList(subclassScoreMap, subclasscd).add(new BigDecimal(rank._score));
                    if (_999999.equals(subclasscd) && NumberUtils.isNumber(rank._avg)) {
                        getMappedList(subclassScoreMap, _999999AVG).add(new BigDecimal(rank._avg));
                    }
                }
            }

            final String form2 = "KNJD615I_2.frm";

            final List gradeAvgList = new ArrayList();
            gradeAvgList.add("GRADE");
            for (int i = 0; i < _param._courseList.size(); i++) {
                gradeAvgList.add("COURSE:" + _param._courseList.get(i));
            }
            gradeAvgList.add("MAX");
            gradeAvgList.add("MIN");
            gradeAvgList.add("MEDIAN");
            gradeAvgList.add("STDDEV");

            final Map ryoMap = new HashMap();
            ryoMap.put("HR_CLASS", "RYO");
            ryoMap.put("HR_CLASS_NAME1", "寮生");

            final Map totalMap = new HashMap();
            totalMap.put("HR_CLASS", "TOTAL");
            totalMap.put("HR_CLASS_NAME1", "");

            final List hrAvgHrclassList = new ArrayList();
            hrAvgHrclassList.addAll(_param._hrList);
            hrAvgHrclassList.add(ryoMap);

            final List akatenHrclassList = new ArrayList();
            akatenHrclassList.add(totalMap);
            akatenHrclassList.addAll(_param._hrList);
            akatenHrclassList.add(ryoMap);

            final List countHrclassList = new ArrayList();
            countHrclassList.add(totalMap);
            countHrclassList.addAll(_param._hrList);

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
                svf.VrsOut("DATE", printDateStr); // 印刷日付

                final int maxLineGradeAvg = 7;
                for (int j = 0; j < Math.min(gradeAvgList.size(), maxLineGradeAvg); j++) {
                    final int line = j + 1;
                    final String lineItem = (String) gradeAvgList.get(j);
                    if ("GRADE".equals(lineItem)) {
                        final Average averageGrade999999 = Average.getAverage(avgMap, Average.avgKey("1", _param._grade, "000", "00000000", _999999));
                        final Average averageGrade999999AVG = Average.getAverage(avgMap, Average.avgKey("1", _param._grade, "000", "00000000", _999999AVG));

                        svf.VrsOutn("AVE_NAME1", line, "学年平均"); // 平均名称
                        svf.VrsOutn("TOTAL1", line, sishagonyu(averageGrade999999._avg)); // 合計
                        svf.VrsOutn("AVE1", line, sishagonyu(averageGrade999999AVG._avg)); // 平均
                        svf.VrsOutn("HR_RANK", line, averageGrade999999._count); // クラス順位
                    } else if (lineItem.startsWith("COURSE:")) {
                        final String course = lineItem.substring("COURSE:".length());
                        final String coursecodename = StringUtils.defaultString((String) _param._coursecodenameMap.get(course));
                        svf.VrsOutn("AVE_NAME1", line, coursecodename); // 平均名称
                        svf.VrsOutn("TOTAL1", line, sishagonyu(Average.getAverage(avgMap, Average.avgKey("3", _param._grade,  "000", course, _999999))._avg)); // 合計
                        svf.VrsOutn("AVE1", line, sishagonyu(Average.getAverage(avgMap, Average.avgKey("3", _param._grade,  "000", course, _999999AVG))._avg)); // 平均
                        svf.VrsOutn("HR_RANK", line, Average.getAverage(avgMap, Average.avgKey("3", _param._grade,  "000", course, _999999))._count); // 人数
                    } else if ("MAX".equals(lineItem)) {
                        final Average averageGrade999999 = Average.getAverage(avgMap, Average.avgKey("1", _param._grade, "000", "00000000", _999999));
                        final Average averageGrade999999AVG = Average.getAverage(avgMap, Average.avgKey("1", _param._grade, "000", "00000000", _999999AVG));

                        svf.VrsOutn("AVE_NAME1", line, "最高点"); // 平均名称
                        svf.VrsOutn("TOTAL1", line, averageGrade999999._highscore); // 合計
                        svf.VrsOutn("AVE1", line, sishagonyu(averageGrade999999AVG._highscore)); // 平均
                    } else if ("MIN".equals(lineItem)) {
                        final Average averageGrade999999 = Average.getAverage(avgMap, Average.avgKey("1", _param._grade, "000", "00000000", _999999));
                        final Average averageGrade999999AVG = Average.getAverage(avgMap, Average.avgKey("1", _param._grade, "000", "00000000", _999999AVG));

                        svf.VrsOutn("AVE_NAME1", line, "最低点"); // 平均名称
                        svf.VrsOutn("TOTAL1", line, averageGrade999999._lowscore); // 合計
                        svf.VrsOutn("AVE1", line, sishagonyu(averageGrade999999AVG._lowscore)); // 平均
                    } else if ("MEDIAN".equals(lineItem)) {
                        final List scoreList999999 = getMappedList(subclassScoreMap, _999999);
                        final List scoreList999999Avg = getMappedList(subclassScoreMap, _999999AVG);
                        
                        svf.VrsOutn("AVE_NAME1", line, "中央値"); // 平均名称
                        svf.VrsOutn("TOTAL1", line, sishagonyu(median(scoreList999999))); // 合計
                        svf.VrsOutn("AVE1", line, sishagonyu(median(scoreList999999Avg))); // 平均
                    } else if ("STDDEV".equals(lineItem)) {
                        final Average averageGrade999999 = Average.getAverage(avgMap, Average.avgKey("1", _param._grade, "000", "00000000", _999999));
                        final Average averageGrade999999AVG = Average.getAverage(avgMap, Average.avgKey("1", _param._grade, "000", "00000000", _999999AVG));

                        svf.VrsOutn("AVE_NAME1", line, "標準偏差"); // 平均名称
                        svf.VrsOutn("TOTAL1", line, sishagonyu(averageGrade999999._stddev)); // 合計
                        svf.VrsOutn("AVE1", line, sishagonyu(averageGrade999999AVG._stddev)); // 平均
                    }
                }

                final int maxLineCourseAvg = 8;
                for (int j = 0; j < Math.min(hrAvgHrclassList.size(), maxLineCourseAvg); j++) {
                    final int line = j + 1;
                    final Map hr = (Map) hrAvgHrclassList.get(j);
                    final String hrClass = KnjDbUtils.getString(hr, "HR_CLASS");
                    svf.VrsOutn("AVE_NAME2", line, StringUtils.defaultString(KnjDbUtils.getString(hr, "HR_CLASS_NAME1")) + "平均"); // 平均名称
                    if ("RYO".equals(hrClass)) {
                        final Average averageRyo999999 = Average.getAverage(avgMap, Average.avgKey("7", _param._grade, "000", "00000000", _999999));
                        final Average averageRyo999999AVG = Average.getAverage(avgMap, Average.avgKey("7", _param._grade, "000", "00000000", _999999AVG));

                        svf.VrsOutn("TOTAL2", line, sishagonyu(averageRyo999999._avg)); // 合計
                        svf.VrsOutn("AVE2", line, sishagonyu(averageRyo999999AVG._avg)); // 平均
                    } else {
                        final Average averageHr999999 = Average.getAverage(avgMap, Average.avgKey("2", _param._grade, hrClass, "00000000", _999999));
                        final Average averageHr999999AVG = Average.getAverage(avgMap, Average.avgKey("2", _param._grade, hrClass, "00000000", _999999AVG));

                        svf.VrsOutn("TOTAL2", line, sishagonyu(averageHr999999._avg)); // 合計
                        svf.VrsOutn("AVE2", line, sishagonyu(averageHr999999AVG._avg)); // 平均
                    }
                }

                final int maxLineRed = 9;
                for (int j = 0; j < Math.min(akatenHrclassList.size(), maxLineRed); j++) {
                    final int line = j + 1;
                    final Map hr = (Map) akatenHrclassList.get(j);
                    final String hrClass = KnjDbUtils.getString(hr, "HR_CLASS");
                    svf.VrsOutn("RED_NAME", line, StringUtils.defaultString(KnjDbUtils.getString(hr, "HR_CLASS_NAME1")) + "赤点"); // 赤点名称
                    if ("TOTAL".equals(hrClass)) {
                        final List totalList = getMappedList(akatenMap, "SUBCLASS=TOTAL|HR_CLASS=TOTAL"); // のべSCHREGNO
                        final List schregnoList = new ArrayList(new HashSet(totalList)); // 重複をのぞいたSCHREGNO
                        svf.VrsOutn("TOTAL3", line, String.valueOf(totalList.size())); // 合計
                        svf.VrsOutn("AVE3", line, String.valueOf(schregnoList.size())); // 平均
                    } else if ("RYO".equals(hrClass)) {
                        svf.VrsOutn("TOTAL3", line, String.valueOf(getMappedList(akatenMap, "SUBCLASS=TOTAL|HR_CLASS=RYO").size())); // 合計
                    } else {
                        svf.VrsOutn("TOTAL3", line, String.valueOf(getMappedList(akatenMap, "SUBCLASS=TOTAL|HR_CLASS=" + hrClass).size())); // 合計
                    }
                }

                final int maxLineCount = 8;
                for (int j = 0; j < Math.min(countHrclassList.size(), maxLineCount); j++) {
                    final int line = j + 1;
                    final Map hr = (Map) countHrclassList.get(j);

                    if ("TOTAL".equals(KnjDbUtils.getString(hr, "HR_CLASS"))) {
                        final Average averageHr999999 = Average.getAverage(avgMap, Average.avgKey("1", _param._grade, "000", "00000000", _999999));

                        svf.VrsOutn("NUM_NAME", line, "総受験者数"); // 人数名称
                        svf.VrsOutn("TOTAL4", line, averageHr999999._count); // 合計
                        svf.VrsOutn("AVE4", line, String.valueOf(getMappedList(kessekiMap, "HR_CLASS=TOTAL").size())); // 平均
                    } else {
                        final String hrClass = KnjDbUtils.getString(hr, "HR_CLASS");
                        final Average averageHr999999 = Average.getAverage(avgMap, Average.avgKey("2", _param._grade, hrClass, "00000000", _999999));

                        svf.VrsOutn("NUM_NAME", line, StringUtils.defaultString(KnjDbUtils.getString(hr, "HR_CLASS_NAME1")) + "受験者数  " + StringUtils.defaultString(KnjDbUtils.getString(hr, "REGD_COUNT"))); // 人数名称
                        svf.VrsOutn("TOTAL4", line, averageHr999999._count); // 合計
                        svf.VrsOutn("AVE4", line, String.valueOf(getMappedList(kessekiMap, "HR_CLASS=" + hrClass).size())); // 平均
                    }
                }

                // 科目の列表示
                for (int i = 0; i < subclassList.size(); i++) {
                    final String subclasscd = (String) subclassList.get(i);
                    svf.VrsOut("GRPCD", getClasscd(subclasscd)); // グループコード
                    svf.VrsOut("CLASS_NAME", getColumnClassname(subclasscd, 4, classcdSubclasscdListMap)); // 教科名
                    final String subclassname = (String) _param._subclassnameMap.get(subclasscd);
                    svf.VrsOut("SUBCLASS_NAME" + (getMS932ByteCount(subclassname) <= 4 ? "1" : "2"), subclassname); // 科目名

                    for (int j = 0; j < Math.min(gradeAvgList.size(), maxLineGradeAvg); j++) {
                        final String line = String.valueOf(j + 1);

                        final String lineItem = (String) gradeAvgList.get(j);
                        if ("GRADE".equals(lineItem)) {
                            final Average averageGrade = Average.getAverage(avgMap, Average.avgKey("1", _param._grade, "000", "00000000", subclasscd));
                            svf.VrsOut("GRADE_AVE" + line, sishagonyu(averageGrade._avg)); // 平均
                        } else if (lineItem.startsWith("COURSE:")) {
                            final String course = lineItem.substring("COURSE:".length());
                            final Average averageCourse = Average.getAverage(avgMap, Average.avgKey("3", _param._grade, "000", course, subclasscd));
                            svf.VrsOut("GRADE_AVE" + line, sishagonyu(averageCourse._avg)); // 平均
                        } else if ("MAX".equals(lineItem)) {
                            final Average averageGrade = Average.getAverage(avgMap, Average.avgKey("1", _param._grade, "000", "00000000", subclasscd));
                            svf.VrsOut("GRADE_AVE" + line, averageGrade._highscore); // 平均
                        } else if ("MIN".equals(lineItem)) {
                            final Average averageGrade = Average.getAverage(avgMap, Average.avgKey("1", _param._grade, "000", "00000000", subclasscd));
                            svf.VrsOut("GRADE_AVE" + line, averageGrade._lowscore); // 平均
                        } else if ("MEDIAN".equals(lineItem)) {
                            svf.VrsOut("GRADE_AVE" + line, sishagonyu(median(getMappedList(subclassScoreMap, subclasscd)))); // 平均

                        } else if ("STDDEV".equals(lineItem)) {
                            final Average averageGrade = Average.getAverage(avgMap, Average.avgKey("1", _param._grade, "000", "00000000", subclasscd));
                            svf.VrsOut("GRADE_AVE" + line, sishagonyu(averageGrade._stddev)); // 平均
                        }
                    }
                    for (int j = 0; j < Math.min(hrAvgHrclassList.size(), maxLineCourseAvg); j++) {
                        final int line = j + 1;
                        final Map hr = (Map) hrAvgHrclassList.get(j);
                        final String hrClass = KnjDbUtils.getString(hr, "HR_CLASS");
                        if ("RYO".equals(hrClass)) {
                            final Average average = Average.getAverage(avgMap, Average.avgKey("7", _param._grade, "000", "00000000", subclasscd));
                            svf.VrsOut("COURSE_AVE" + line, sishagonyu(average._avg)); // 平均
                        } else {
                            final Average average = Average.getAverage(avgMap, Average.avgKey("2", _param._grade, hrClass, "00000000", subclasscd));
                            svf.VrsOut("COURSE_AVE" + line, sishagonyu(average._avg)); // 平均
                        }
                    }
                    for (int j = 0; j < Math.min(akatenHrclassList.size(), maxLineRed); j++) {
                        final int line = j + 1;
                        final Map hr = (Map) akatenHrclassList.get(j);
                        final String hrClass = KnjDbUtils.getString(hr, "HR_CLASS");
                        if ("TOTAL".equals(hrClass)) {
                             // のべSCHREGNO
                            svf.VrsOut("RED" + line, String.valueOf(getMappedList(akatenMap, "SUBCLASS=" + subclasscd + "|HR_CLASS=TOTAL").size())); // 赤点
                        } else if ("RYO".equals(hrClass)) {
                            svf.VrsOut("RED" + line, String.valueOf(getMappedList(akatenMap, "SUBCLASS=" + subclasscd + "|HR_CLASS=RYO").size())); // 赤点
                        } else {
                            svf.VrsOut("RED" + line, String.valueOf(getMappedList(akatenMap, "SUBCLASS=" + subclasscd + "|HR_CLASS=" + hrClass).size())); // 赤点
                        }
                    }
                    for (int j = 0; j < Math.min(countHrclassList.size(), maxLineCount); j++) {
                        final int line = j + 1;
                        final Map hr = (Map) countHrclassList.get(j);
                        if ("TOTAL".equals(KnjDbUtils.getString(hr, "HR_CLASS"))) {
                            final Average averageHr = Average.getAverage(avgMap, Average.avgKey("1", _param._grade, "000", "00000000", subclasscd));
                            svf.VrsOut("NUM" + line, averageHr._count); // 人数
                        } else {
                            final String hrClass = KnjDbUtils.getString(hr, "HR_CLASS");
                            final Average averageHr = Average.getAverage(avgMap, Average.avgKey("2", _param._grade, hrClass, "00000000", subclasscd));
                            svf.VrsOut("NUM" + line, averageHr._count); // 人数
                        }
                    }
                    svf.VrEndRecord();
                }
                for (int i = subclassList.size(); i < maxColumn; i++) {
                    svf.VrEndRecord();
                }
            }
        }
    }

    private static class Param {
        final String _year;
        final String _semester;
        final String _ctrlSeme;
        final String _loginDate;
        final String _grade;
        final String _testcd;
        final String _testcd2keta;
        final String _printDiv; // 印刷対象 1:クラス 2:学年
        final String _date;
        final String _ryoOnly;
        final Map _classnameMap;
        final Map _subclassnameMap;
        final Map _coursecodenameMap;
        final String _outputKijun; // 順位の基準点 1:総合点 2:平均点
        final String _sortdiv; // 成績順 or 年組番号順
        String _testitemname = "";
        String _semestername = "";
        String _gradename = "";
        final String _schoolKind;
        final String[] _categorySelected;
        final List _notTargetSubclassCdList;
        final String _major;
        final String _use_school_detail_gcm_dat;
        final String _useSchool_KindField;
        final String SCHOOLKIND;
        final String SCHOOLCD;
        final String _schregSemester;
        final List _hrList;
        final List _courseList;
        final String _use_prg_schoolkind;
        final String _selectSchoolKind;

        final int _akaten; // 指定点未満は赤字

        Param(final HttpServletRequest request, final DB2UDB db2) {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _ctrlSeme = request.getParameter("CTRL_SEME");
            _loginDate = request.getParameter("LOGIN_DATE");
            _grade = request.getParameter("GRADE");
            _testcd = request.getParameter("TESTCD");
            _testcd2keta = _testcd != null ? _testcd.substring(0, 2) : "";
            _printDiv = request.getParameter("PRINT_DIV");
            _date = request.getParameter("DATE");
            _ryoOnly = request.getParameter("RYO_ONLY");
            _classnameMap = getClassnameMap(db2);
            _subclassnameMap = getSubclassnameMap(db2);
            _major = request.getParameter("MAJOR");
            _use_school_detail_gcm_dat = request.getParameter("use_school_detail_gcm_dat");
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            SCHOOLCD = request.getParameter("SCHOOLCD");
            _use_prg_schoolkind = request.getParameter("use_prg_schoolkind");
            _selectSchoolKind = request.getParameter("selectSchoolKind");
            SCHOOLKIND = StringUtils.defaultString(getSchregRegdGdat(db2, "SCHOOL_KIND"));

            if ("9".equals(_semester)) {
                _schregSemester = _ctrlSeme;
            } else {
                _schregSemester = _semester;
            }
            _akaten = _testcd.endsWith("09") ? 2 : 30;

            setName(db2);
            _outputKijun = request.getParameter("OUTPUT_KIJUN");
            _sortdiv = request.getParameter("SORT_DIV");
            _schoolKind = getSchoolKind(db2);
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");  //PRINT_DIV='1' 学年-組
            _notTargetSubclassCdList = getNotTargetSubclassCdList(db2);
            _courseList = getCoursecodeList(db2);
            _hrList = getHrList(db2);
            _coursecodenameMap = getCoursecodenameMap(db2);
        }

        private String getSchregRegdGdat(final DB2UDB db2, final String field) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            String rtn = null;
            try{
                final StringBuffer stb = new StringBuffer();
                stb.append("SELECT  ");
                stb.append("        " + field + " ");
                stb.append("FROM    SCHREG_REGD_GDAT T1 ");
                stb.append("WHERE   T1.YEAR = '" + _year + "' ");
                stb.append(    "AND T1.GRADE = '" + _grade + "' ");
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    rtn = rs.getString(field);
                }
            } catch (Exception ex) {
                log.error("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
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
            sql += " WHERE T1.YEAR = '" + _year + "' AND T1.SEMESTER = '" + _schregSemester + "' AND T1.GRADE = '" + _grade + "' ";
            sql += " GROUP BY T1.GRADE, T1.HR_CLASS, T1.HR_CLASS_NAME1 ";
            sql += " ORDER BY T1.GRADE, T1.HR_CLASS ";
            return KnjDbUtils.query(db2, sql);
        }

        private List getCoursecodeList(final DB2UDB db2) {
            final String sql = "SELECT DISTINCT COURSECD || MAJORCD || COURSECODE AS COURSE FROM SCHREG_REGD_DAT WHERE YEAR = '" + _year + "' AND GRADE = '" + _grade + "' ORDER BY COURSECD || MAJORCD ||  COURSECODE ";
            return KnjDbUtils.getColumnDataList(KnjDbUtils.query(db2, sql), "COURSE");
        }

        private void setName(final DB2UDB db2) {
            String sql1 = "";
            if ("1".equals(_use_school_detail_gcm_dat)) {
                sql1 = "SELECT TESTITEMNAME FROM TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV WHERE YEAR = '" + _year + "'" +
                        " AND SEMESTER = '" + _semester + "' " +
                        " AND GRADE = '00' " +
                        " AND COURSECD || '-' || MAJORCD = '" + _major + "' " +
                        " AND TESTKINDCD || TESTITEMCD || SCORE_DIV = '" + _testcd + "' ";
                if ("1".equals(_use_prg_schoolkind)) {
                    sql1 += " AND SCHOOLCD = '" + SCHOOLCD + "' " +
                            " AND SCHOOL_KIND = '" + SCHOOLKIND + "' ";
                } else if ("1".equals(_useSchool_KindField) && !StringUtils.isBlank(SCHOOLKIND)) {
                    sql1 += " AND SCHOOLCD = '" + SCHOOLCD + "' " +
                           " AND SCHOOL_KIND = '" + SCHOOLKIND + "' ";
                }
            } else {
                sql1 += " SELECT TESTITEMNAME FROM TESTITEM_MST_COUNTFLG_NEW_SDIV ";
                sql1 += " WHERE YEAR = '" + _year + "' AND SEMESTER = '" + _semester + "' AND TESTKINDCD || TESTITEMCD || SCORE_DIV = '" + _testcd + "' ";
            }
            _testitemname = toStr(KnjDbUtils.getOne(KnjDbUtils.query(db2, sql1)));

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

        private Map getCoursecodenameMap(final DB2UDB db2) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT DISTINCT ");
            stb.append("     T2.COURSECD || T2.MAJORCD || T2.COURSECODE AS COURSE, ");
            stb.append("     T1.COURSECODENAME ");
            stb.append(" FROM ");
            stb.append("     COURSECODE_MST T1 ");
            stb.append("     INNER JOIN SCHREG_REGD_DAT T2 ON T2.YEAR = '" + _year + "' ");
            stb.append("         AND T2.GRADE = '" + _grade + "' ");
            stb.append("         AND T2.COURSECODE = T1.COURSECODE ");

            return KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, stb.toString()), "COURSE", "COURSECODENAME");
        }

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
            stb.append("     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("     VALUE(SUBCLASSABBV, SUBCLASSNAME) AS SUBCLASSNAME ");
            stb.append(" FROM ");
            stb.append("     SUBCLASS_MST ");

            return KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, stb.toString()), "SUBCLASSCD", "SUBCLASSNAME");
        }

        private List getNotTargetSubclassCdList(final DB2UDB db2) {
            final List rtnList = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {

                final StringBuffer stb = new StringBuffer();
                stb.append("   SELECT ");
                stb.append("       T1.ATTEND_CLASSCD || '-' || T1.ATTEND_SCHOOL_KIND || '-' || T1.ATTEND_CURRICULUM_CD || '-' || T1.ATTEND_SUBCLASSCD AS ATTEND_SUBCLASSCD, ");
                stb.append("       T1.COMBINED_CLASSCD || '-' || T1.COMBINED_SCHOOL_KIND || '-' || T1.COMBINED_CURRICULUM_CD || '-' || T1.COMBINED_SUBCLASSCD AS COMBINED_SUBCLASSCD ");
                stb.append("   FROM ");
                stb.append("       SUBCLASS_REPLACE_COMBINED_DAT T1 ");
                stb.append("   WHERE ");
                stb.append("       T1.YEAR = '" + _year + "' ");

                final String sql = stb.toString();
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    if ("99".equals(_testcd2keta)) {
                        // rtnList.add(rs.getString("ATTEND_SUBCLASSCD"));
                    } else {
                        if (rtnList.contains(rs.getString("COMBINED_SUBCLASSCD"))) {
                            continue;
                        }
                        rtnList.add(rs.getString("COMBINED_SUBCLASSCD"));
                    }
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtnList;
        }
    }
}
