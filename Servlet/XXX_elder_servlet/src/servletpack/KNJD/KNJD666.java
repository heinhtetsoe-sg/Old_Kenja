/*
 * $Id: 6d16ee97b588b71407ee5546e5f5b1cc5990d4d7 $
 *
 * 作成日: 2015/08/05
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;


import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 * 文京学園 成績一覧表
 */
public class KNJD666 {

    private static final Log log = LogFactory.getLog(KNJD666.class);

    private boolean _hasData;
    
    private static String SUBCLASSCD_333333 = "333333";
    private static String SUBCLASSCD_555555 = "555555";
    private static String SUBCLASSCD_777777 = "777777";
    private static String SUBCLASSCD_888888 = "888888";
    private static String SUBCLASSCD_999999 = "999999";

    private static int SORT_SCORE = 1;
    private static int SORT_AVG = 2;

    private Param _param;
    
    private static String PRGID_KNJD678 = "KNJD678";

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
    
    private static String toString(final Object o) {
        return null == o ? null : o.toString();
    }
    
    private static String sishaGonyu(final BigDecimal bd) {
        return null == bd ? null : bd.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        
        final int maxSubclass = 25;
        
        final Map avgDatMap = RecordAverageDat.getRecordAverageDatMap(db2, _param);
        final String[] subclasscdTotal = {SUBCLASSCD_777777, SUBCLASSCD_888888, SUBCLASSCD_999999};

        final List courseGroupList = CourseGroup.getCourseGroupList(db2, _param);
        for (int cgi = 0; cgi < courseGroupList.size(); cgi++) {
            final CourseGroup cg = (CourseGroup) courseGroupList.get(cgi);
            
            for (final Iterator it = cg._hrList.iterator(); it.hasNext();) {
                final HrClass hr = (HrClass) it.next();
                
                final Collection courses = courses(hr._studentList);
                log.info(" hr = " + hr._grade + hr._hrClass + " (courses = " + courses + ")");
                
                final String form = "KNJD666.frm"; 
                svf.VrSetForm(form, 4);

                final String title;
                if (PRGID_KNJD678.equals(_param._prgid)) {
                    if ("9990009".equals(_param._semester + _param._testcd)) {
                        title = KenjaProperties.gengou(Integer.parseInt(_param._year)) + "年度　クラス別評定一覧表（" + ("2".equals(_param._ranking) ? "成績順" : "番号順") + "）"; // タイトル
                    } else {
                        title = KenjaProperties.gengou(Integer.parseInt(_param._year)) + "年度　クラス別" + StringUtils.defaultString(_param._testitemname) + "一覧表（" + ("2".equals(_param._ranking) ? "成績順" : "番号順") + "）"; // タイトル
                    }
                } else {
                    title = KenjaProperties.gengou(Integer.parseInt(_param._year)) + "年度　" + StringUtils.defaultString(_param._testitemname) + "　クラス別成績順位表（" + ("2".equals(_param._ranking) ? "成績順" : "番号順") + "）"; // タイトル
                }
                svf.VrsOut("TITLE", title); // タイトル
                svf.VrsOut("SUB_TITLE", "2".equals(_param._output) ? "（累積別）" : PRGID_KNJD678.equals(_param._prgid) ? "" : "（考査別）"); // サブタイトル
                svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._loginDate)); // 
                svf.VrsOut("GRADE_COURSE", cg._groupName); // 学年コース名
                for (int i = 1; i <= 3; i++) {
                    svf.VrsOut("SUBTOTAL_NAME" + i, "総合点"); // 合計名称
                    svf.VrsOut("AVERAGE_NAME" + i, "平均点"); // 平均名称
                    svf.VrsOut("RANK_NAME1_" + i, "クラス席次"); // 年組
                    svf.VrsOut("RANK_NAME2_" + i, "J".equals(_param._schoolKind) ? "学年席次" : "コース席次"); // 担任名
                }
                svf.VrsOut("HR_NAME", hr._hrName); // 年組
                svf.VrsOut("TEACHER_NAME", hr._staffname); // 担任名
                
                final List printStudentList = new ArrayList(hr._studentList);
                Collections.sort(printStudentList, new Student.Comparator(_param, SORT_AVG));
                
                for (int i = 0; i < printStudentList.size(); i++) {
                    final Student student = (Student) printStudentList.get(i);
                    final int line = i + 1;
                    svf.VrsOutn("NO", line, NumberUtils.isDigits(student._attendno) ? String.valueOf(Integer.parseInt(student._attendno)) : student._attendno); // 番号
                    svf.VrsOutn("NAME", line, student._name); // 氏名
                    
                    for (int j = 0; j < 3; j++) {
                        final String si = String.valueOf(j + 1);
                        final Score score = (Score) student._scoreMap.get(subclasscdTotal[j]);
                        if (null != score) {
                            svf.VrsOutn("SUBTOTAL" + si, line, score._score); // 合計
                            svf.VrsOutn("AVERAGE" + si, line, sishaGonyu(score._avg)); // 平均
                            svf.VrsOutn("CLASS_RANK" + si, line, toString(score._classAvgRank)); // クラス席次
                            svf.VrsOutn("COURSE_RANK" + si, line, "J".equals(_param._schoolKind) ? toString(score._gradeAvgRank) : toString(score._courseGroupAvgRank)); // コース席次
                        }
                    }
                }
                
                 for (int si = 0; si < subclasscdTotal.length; si++) {
                     final String subclasscd = subclasscdTotal[si];
                     final String ssi = String.valueOf(si + 1);
                     
                     final RecordAverageDat avgHr = RecordAverageDat.get(avgDatMap, RecordAverageDat.getHrAvgDivKey(hr._grade, hr._hrClass), subclasscd);
                     if (null != avgHr) {
                         final int avglen = null == avgHr._avg ? 0 : sishaGonyu(avgHr._avg).length();
                         svf.VrsOut("AVE_SUBTOTAL" + ssi + (avglen <= 4 ? "" : avglen == 5 ? "_2" : "_3" ), sishaGonyu(avgHr._avg)); // 合計

                     }
                     final BigDecimal avgAvg = getAvgAvg(printStudentList, subclasscd, hr._subclassMap);
                     final int avgavglen = null == avgAvg ? 0 : sishaGonyu(avgAvg).length();
                     svf.VrsOut("AVE_AVERAGE" + ssi + (avgavglen <= 5 ? "" : "_2"), sishaGonyu(avgAvg)); // 平均
                     //svf.VrsOut("AVE_CLASS_RANK" + si, null); // クラス席次
                     //svf.VrsOut("AVE_COURSE_RANK" + si, null); // コース席次
                 }

                
                 final Map subclassMap = new HashMap(hr._subclassMap);
                 subclassMap.remove(SUBCLASSCD_333333);
                 subclassMap.remove(SUBCLASSCD_555555);
                 subclassMap.remove(SUBCLASSCD_777777);
                 subclassMap.remove(SUBCLASSCD_888888);
                 subclassMap.remove(SUBCLASSCD_999999);
                 setPrintSubclass(subclassMap, courses, _param);
                 final List subclassList = new ArrayList(subclassMap.values());
                 Collections.sort(subclassList);
                 
                 for (int j = 0; j < subclassList.size(); j++) {
                     final Subclass subclass = (Subclass) subclassList.get(j);
                     
                     svf.VrsOut("SELECT_MARK", "3".equals(subclass._requireFlg) ? "＊" : ""); // 選択科目表示
                     svf.VrsOut("SUBCLASS_NAME", null != subclass._subclassabbv && subclass._subclassabbv.length() > 3 ? subclass._subclassabbv.substring(0, 3) : subclass._subclassabbv); // 科目名
                     
                     for (int i = 0; i < printStudentList.size(); i++) {
                         final Student student = (Student) printStudentList.get(i);
                         final int line = i + 1;
                         final Score score = (Score) student._scoreMap.get(subclass._subclasscd);
                         if (null != score && null != score._score) {
                             final int scorelen = null == score._score ? 0 : score._score.length();
                             if (scorelen <= 2) {
                                 svf.VrsOutn("VALUE", line, score._score); // 評定
                             } else {
                                 svf.VrsOutn("SCORE", line, score._score); // 素点
                             }
                         }
                     }
                     
                     final RecordAverageDat avgHr = RecordAverageDat.get(avgDatMap, RecordAverageDat.getHrAvgDivKey(hr._grade, hr._hrClass), subclass._subclasscd);
                     if (null != avgHr) {
                         final String avg = sishaGonyu(avgHr._avg);
                         final int avglen = null == avg ? 0 : avg.length();
                         svf.VrsOut("SUBCLASS_TOTAL" + (avglen <= 4 ? "" : "_2"), avg); // 科目合計
                     }
                     
                     svf.VrEndRecord();
                 }
                 
                 for (int i = subclassList.size(); i < maxSubclass; i++) {
                     svf.VrEndRecord();
                 }
                 _hasData = true;
            }
        }
    }
    
    private static Collection courses(final List studentList) {
        final Set courses = new HashSet();
        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            courses.add(student._course);
        }
        return courses;
    }
    
    private static void setPrintSubclass(final Map subclassMap, final Collection courses, final Param param) {
        final String paramKindItem = param._testcd.substring(0, 4);
        if (paramKindItem.startsWith("99")) {
            return;
        }
        final String paramSemKindItem = param._semester + paramKindItem;
        for (final Iterator it = subclassMap.entrySet().iterator(); it.hasNext();) {
            final Map.Entry e = (Map.Entry) it.next();
            final String subclasscd = (String) e.getKey();
            final Subclass subclass = (Subclass) e.getValue();
            
            boolean removeFlg = true; // この科目が、対象生徒の全て（のコース）にテストがないなら、表示しないようにするため除く
            
            for (final Iterator itc = courses.iterator(); itc.hasNext();) {
                final String course = (String) itc.next();
                
                final Collection semKindItem = getMappedList(getMappedMap(param._adminControlTestitemDat, course), subclasscd);
                if (semKindItem.size() == 0 || semKindItem.contains(paramSemKindItem)) {
                    // 設定なしもしくはテストがある
                    removeFlg = false;
                } else {
                    // ADMIN_CONTROL_TESTITEM_DATにテスト種別が1個以上設定されていてかつ指定テスト種別が含まれている -> この科目にテストがない
                }
            }
            if (removeFlg) {
                it.remove();
                log.info(" remove subclass : " + subclasscd + " (" + subclass + ")");
            }
        }
    }

    private static BigDecimal getAvgAvg(final List printStudentList, final String totalSubclasscd, final Map subclassMap) {
        BigDecimal total = new BigDecimal(0);
        int count = 0;
        for (final Iterator it = printStudentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            
            BigDecimal studentTotal = null;
            int studentCount = 0;
            studentSubclass:
            for (final Iterator sit = student._scoreMap.entrySet().iterator(); sit.hasNext();) {
                final Map.Entry e = (Map.Entry) sit.next();
                final String subclasscd = (String) e.getKey();
                if (SUBCLASSCD_777777.equals(subclasscd) || SUBCLASSCD_888888.equals(subclasscd) || SUBCLASSCD_999999.equals(subclasscd)) {
                    continue;
                }
                final Subclass subclass = (Subclass) subclassMap.get(subclasscd);
                if (null == subclass) {
                    continue;
                }
                if (SUBCLASSCD_777777.equals(totalSubclasscd) && !"3".equals(subclass._requireFlg)
                 || SUBCLASSCD_888888.equals(totalSubclasscd) && "3".equals(subclass._requireFlg)
                 || SUBCLASSCD_999999.equals(totalSubclasscd)) {

                    // 対象の科目
                    final Score score = (Score) student._scoreMap.get(subclasscd);
                    if (null != score) {
                        if (NumberUtils.isDigits(score._score)) {
                            studentTotal = (null == studentTotal ? new BigDecimal(0) : studentTotal).add(new BigDecimal(score._score));
                            studentCount += 1;
                        } else if ("*".equals(score._score)) {
                            studentTotal = null;
                            studentCount = 0;
                            break studentSubclass;
                        }
                    }
                }
            }
            if (null != studentTotal) {
                total = total.add(studentTotal);
                count += studentCount;
            }
        }
        if (count == 0) {
            return null;
        }
        BigDecimal avg = total.divide(new BigDecimal(count), 1, BigDecimal.ROUND_HALF_UP);
        log.info(" " + totalSubclasscd + " : " + total + " / " + count + " = " + avg);
        return avg;
    }

    private static List getMappedList(final Map map, final String key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new ArrayList());
        }
        return (List) map.get(key1);
    }

    private static Map getMappedMap(final Map map, final String key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new TreeMap());
        }
        return (Map) map.get(key1);
    }

    private static class Score {
        final String _chaircd;
        final String _subclasscd;
        String _score;
        final BigDecimal _avg;
        final Integer _gradeRank;
        final Integer _gradeAvgRank;
        final Integer _classRank;
        final Integer _classAvgRank;
        final Integer _courseRank;
        final Integer _courseAvgRank;
        final Integer _courseGroupRank;
        final Integer _courseGroupAvgRank;

        Score(
                final String chaircd,
                final String subclasscd,
                final String score,
                final BigDecimal avg,
                final Integer gradeRank,
                final Integer gradeAvgRank,
                final Integer classRank,
                final Integer classAvgRank,
                final Integer courseRank,
                final Integer courseAvgRank,
                final Integer courseGroupRank,
                final Integer courseGroupAvgRank) {
            _chaircd = chaircd;
            _subclasscd = subclasscd;
            _score = score;
            _avg = avg;
            _gradeRank = gradeRank;
            _gradeAvgRank = gradeAvgRank;
            _classRank = classRank;
            _classAvgRank = classAvgRank;
            _courseRank = courseRank;
            _courseAvgRank = courseAvgRank;
            _courseGroupRank = courseGroupRank;
            _courseGroupAvgRank = courseGroupAvgRank;
        }
    }
    
    private static class Subclass implements Comparable {
        final String _subclasscd;
        final String _subclassname;
        final String _subclassabbv;
        final String _requireFlg;

        Subclass(
                final String subclasscd,
                final String subclassname,
                final String subclassabbv,
                final String requireFlg) {
            _subclasscd = subclasscd;
            _subclassname = subclassname;
            _subclassabbv = subclassabbv;
            _requireFlg = requireFlg;
        }
        
        public int compareTo(Object o) {
            final Subclass subclass = (Subclass) o;
            String requireFlg1 = StringUtils.defaultString(_requireFlg, "0");
            String requireFlg2 = StringUtils.defaultString(subclass._requireFlg, "0");
            if (!"3".equals(requireFlg1) && "3".equals(requireFlg2)) {
                return -1;
            } else if ("3".equals(requireFlg1) && !"3".equals(requireFlg2)) { // 選択科目は後
                return 1;
            }
            return _subclasscd.compareTo(subclass._subclasscd);
        }
        public String toString() {
            return "Subclass(" + _subclasscd + " / " + _subclassabbv + ")";
        }
    }

    private static class Student {
        final String _grade;
        final String _hrClass;
        final String _attendno;
        final String _schregno;
        final String _name;
        final String _course;
        final Map _scoreMap = new HashMap();

        Student(
            final String grade,
            final String hrClass,
            final String hrName,
            final String staffname,
            final String attendno,
            final String schregno,
            final String name,
            final String course) {
            _grade = grade;
            _hrClass = hrClass;
            _attendno = attendno;
            _schregno = schregno;
            _name = name;
            _course = course;
        }
        
        private static Student getStudent(final String schregno, final List studentList) {
            for (final Iterator it = studentList.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                if (schregno.equals(student._schregno)) {
                    return student;
                }
            }
            return null;
        }
        
        public Integer rankForCompare(int sortDiv) {
            final Score score999999 = (Score) _scoreMap.get(SUBCLASSCD_999999);
            if (null == score999999) {
                return null;
            }
            
            if (SORT_AVG == sortDiv) {
                return score999999._courseGroupAvgRank;
            }
            return score999999._courseGroupRank;
        }
        
        private static class Comparator implements java.util.Comparator {
            final Param _param;
            final int _div;
            Comparator(final Param param, final int div) {
                _param = param;
                _div = div;
            }
            public int compare(final Object o1, final Object o2) {
                int cmp = 0;
                final Student s1 = (Student) o1;
                final Student s2 = (Student) o2;
                if ("2".equals(_param._ranking)) {
                    // 成績順
                    final Integer rank1 = null == s1.rankForCompare(_div) ? new Integer(999999) : s1.rankForCompare(_div);
                    final Integer rank2 = null == s2.rankForCompare(_div) ? new Integer(999999) : s2.rankForCompare(_div);
                    cmp = rank1.compareTo(rank2);
                    if (0 != cmp) return cmp;
                }
                if (null == s2._attendno) return -1;
                if (null == s1._attendno) return 1;
                return s1._attendno.compareTo(s2._attendno);
            }
        }
    }

    private static class HrClass {
        final String _grade;
        final String _hrClass;
        final String _hrName;
        final String _staffname;
        final List _studentList;

        final Map _subclassMap = new HashMap();

        HrClass(
                final String grade,
                final String hrClass,
                final String hrName,
                final String staffname) {
            _grade = grade;
            _hrClass = hrClass;
            _hrName = hrName;
            _staffname = staffname;
            _studentList = new ArrayList();
        }
        
        
        private static HrClass getHrClass(final String gradeHrclass, final List hrList) {
            for (final Iterator it = hrList.iterator(); it.hasNext();) {
                final HrClass hr = (HrClass) it.next();
                if (gradeHrclass.equals(hr._grade + hr._hrClass)) {
                    return hr;
                }
            }
            return null;
        }
    }

    private static class CourseGroup {
        final String _gradeName1;
        final String _groupCd;
        final String _groupName;
        final List _hrList;

        CourseGroup(
            final String gradeName1,
            final String groupCd,
            final String groupName
        ) {
            _gradeName1 = gradeName1;
            _groupCd = groupCd;
            _groupName = groupName;
            _hrList = new ArrayList();
        }

        public static List getCourseGroupList(final DB2UDB db2, final Param param) {
            final List courseGroupList = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map schregMap = new HashMap();
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" WITH REGD AS ( ");
                stb.append("     SELECT ");
                stb.append("       T1.YEAR, ");
                stb.append("       T1.SEMESTER, ");
                stb.append("       GDAT.GRADE_NAME1, ");
                stb.append("       CGRP.GROUP_CD, ");
                stb.append("       CGRPH.GROUP_NAME, ");
                stb.append("       T1.GRADE, ");
                stb.append("       T1.HR_CLASS, ");
                stb.append("       HDAT.HR_NAME, ");
                stb.append("       HRSTF.STAFFNAME, ");
                stb.append("       T1.ATTENDNO, ");
                stb.append("       T1.SCHREGNO, ");
                stb.append("       T1.COURSECD, ");
                stb.append("       T1.MAJORCD, ");
                stb.append("       T1.COURSECODE, ");
                stb.append("       T1.COURSECD || T1.MAJORCD || T1.COURSECODE AS COURSE, ");
                stb.append("       BASE.NAME ");
                stb.append("     FROM SCHREG_REGD_DAT T1 ");
                stb.append("     INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = T1.SCHREGNO ");
                stb.append("     INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR ");
                stb.append("         AND GDAT.GRADE = T1.GRADE ");
                stb.append("     INNER JOIN SCHREG_REGD_HDAT HDAT ON HDAT.YEAR = T1.YEAR ");
                stb.append("         AND HDAT.SEMESTER = T1.SEMESTER ");
                stb.append("         AND HDAT.GRADE = T1.GRADE ");
                stb.append("         AND HDAT.HR_CLASS = T1.HR_CLASS ");
                stb.append("     INNER JOIN COURSE_GROUP_CD_DAT CGRP ON CGRP.YEAR = T1.YEAR ");
                stb.append("         AND CGRP.GRADE = T1.GRADE ");
                stb.append("         AND CGRP.COURSECD = T1.COURSECD ");
                stb.append("         AND CGRP.MAJORCD = T1.MAJORCD ");
                stb.append("         AND CGRP.COURSECODE = T1.COURSECODE ");
                stb.append("     INNER JOIN COURSE_GROUP_CD_HDAT CGRPH ON CGRPH.YEAR = CGRP.YEAR ");
                stb.append("         AND CGRPH.GRADE = CGRP.GRADE ");
                stb.append("         AND CGRPH.GROUP_CD = CGRP.GROUP_CD ");
                stb.append("     LEFT JOIN STAFF_MST HRSTF ON HRSTF.STAFFCD = HDAT.TR_CD1 ");
                stb.append("     WHERE ");
                stb.append("         T1.YEAR = '" + param._year + "' ");
                stb.append("         AND T1.SEMESTER = '" + param.getRegdSemester()  + "' ");
                stb.append("         AND T1.GRADE = '" + param._grade + "' ");
                if (!"00000".equals(param._gradeHrclass)) {
                    stb.append("         AND T1.GRADE || T1.HR_CLASS = '" + param._gradeHrclass + "' ");
                }
                stb.append("         AND CGRP.GROUP_CD = '" + param._groupCd + "' ");
                stb.append(" ) ");
                stb.append(" SELECT ");
                stb.append("   T1.GRADE_NAME1, ");
                stb.append("   T1.GROUP_CD, ");
                stb.append("   T1.GROUP_NAME, ");
                stb.append("   T1.GRADE, ");
                stb.append("   T1.HR_CLASS, ");
                stb.append("   T1.HR_NAME, ");
                stb.append("   T1.STAFFNAME, ");
                stb.append("   T1.ATTENDNO, ");
                stb.append("   T1.SCHREGNO, ");
                stb.append("   T1.NAME, ");
                stb.append("   T1.COURSE, ");
                stb.append("   T2.CHAIRCD, ");
                stb.append("   T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD AS SUBCLASSCD, ");
                stb.append("   SUBM.SUBCLASSNAME, ");
                stb.append("   SUBM.SUBCLASSABBV, ");
                stb.append("   CRE.REQUIRE_FLG, ");
                stb.append("   TREC.SEMESTER, ");
                stb.append("   TREC.TESTKINDCD || TREC.TESTITEMCD || TREC.SCORE_DIV AS TESTCD, ");
                stb.append("   TREC.VALUE_DI, ");
                stb.append("   TRANK.SCORE, ");
                stb.append("   ASLV.ASSESSLEVEL AS ASSESSLEVEL, ");
                stb.append("   TRANK.AVG, ");
                stb.append("   TRANK.GRADE_RANK, ");
                stb.append("   TRANK.GRADE_AVG_RANK, ");
                stb.append("   TRANK.CLASS_RANK, ");
                stb.append("   TRANK.CLASS_AVG_RANK, ");
                stb.append("   TRANK.COURSE_RANK, ");
                stb.append("   TRANK.COURSE_AVG_RANK, ");
                stb.append("   TRANK.MAJOR_RANK, ");
                stb.append("   TRANK.MAJOR_AVG_RANK ");
                stb.append(" FROM REGD T1 ");
                stb.append(" LEFT JOIN CHAIR_STD_DAT T2 ON T2.YEAR = T1.YEAR ");
                stb.append("     AND T2.SEMESTER = T1.SEMESTER ");
                stb.append("     AND T2.SCHREGNO = T1.SCHREGNO ");
                stb.append(" LEFT JOIN CHAIR_DAT T3 ON T3.YEAR = T2.YEAR ");
                stb.append("     AND T3.SEMESTER = T2.SEMESTER ");
                stb.append("     AND T3.CHAIRCD = T2.CHAIRCD ");
                stb.append("     AND T3.CLASSCD < '90' ");
                stb.append(" LEFT JOIN SUBCLASS_MST SUBM ON SUBM.CLASSCD = T3.CLASSCD ");
                stb.append("     AND SUBM.SCHOOL_KIND = T3.SCHOOL_KIND ");
                stb.append("     AND SUBM.CURRICULUM_CD = T3.CURRICULUM_CD ");
                stb.append("     AND SUBM.SUBCLASSCD = T3.SUBCLASSCD ");
                stb.append(" LEFT JOIN CREDIT_MST CRE ON CRE.YEAR = T1.YEAR ");
                stb.append("     AND CRE.COURSECD = T1.COURSECD ");
                stb.append("     AND CRE.MAJORCD = T1.MAJORCD ");
                stb.append("     AND CRE.GRADE = T1.GRADE ");
                stb.append("     AND CRE.COURSECODE = T1.COURSECODE ");
                stb.append("     AND CRE.CLASSCD = T3.CLASSCD ");
                stb.append("     AND CRE.SCHOOL_KIND = T3.SCHOOL_KIND ");
                stb.append("     AND CRE.CURRICULUM_CD = T3.CURRICULUM_CD ");
                stb.append("     AND CRE.SUBCLASSCD = T3.SUBCLASSCD ");
                stb.append(" LEFT JOIN RECORD_SCORE_DAT TREC ON TREC.YEAR = T3.YEAR ");
                stb.append("     AND TREC.SEMESTER = '" + param._semester + "' AND TREC.TESTKINDCD || TREC.TESTITEMCD || TREC.SCORE_DIV = '" + param._testcd + "' ");
                stb.append("     AND TREC.CLASSCD = T3.CLASSCD ");
                stb.append("     AND TREC.SCHOOL_KIND = T3.SCHOOL_KIND ");
                stb.append("     AND TREC.CURRICULUM_CD = T3.CURRICULUM_CD ");
                stb.append("     AND TREC.SUBCLASSCD = T3.SUBCLASSCD ");
                stb.append("     AND TREC.SCHREGNO = T1.SCHREGNO ");
                stb.append(" LEFT JOIN ");
                if ("2".equals(param._output)) {
                    stb.append("  RECORD_RANK_RUIKEI_SDIV_DAT  ");
                } else {
                    stb.append("  RECORD_RANK_SDIV_DAT  ");
                }
                stb.append(" TRANK ON TRANK.YEAR = T3.YEAR ");
                stb.append("     AND TRANK.SEMESTER = '" + param._semester + "' AND TRANK.TESTKINDCD || TRANK.TESTITEMCD || TRANK.SCORE_DIV = '" + param._testcd + "' ");
                stb.append("     AND TRANK.CLASSCD = T3.CLASSCD ");
                stb.append("     AND TRANK.SCHOOL_KIND = T3.SCHOOL_KIND ");
                stb.append("     AND TRANK.CURRICULUM_CD = T3.CURRICULUM_CD ");
                stb.append("     AND TRANK.SUBCLASSCD = T3.SUBCLASSCD ");
                stb.append("     AND TRANK.SCHREGNO = T1.SCHREGNO ");
                stb.append(" LEFT JOIN ASSESS_LEVEL_SDIV_MST ASLV ON ASLV.YEAR = T3.YEAR ");
                stb.append("     AND ASLV.SEMESTER = '" + param._semester + "' ");
                stb.append("     AND ASLV.TESTKINDCD || ASLV.TESTITEMCD || ASLV.SCORE_DIV = '" + param._testcd + "' ");
                if ("2".equals(param._output)) {
//                    if ("9".equals(param._semester)) {
//                        stb.append("     AND ASLV.RUISEKI_DIV = '3' ");
//                    } else {
//                        stb.append("     AND ASLV.RUISEKI_DIV = '2' ");
//                    }
                    stb.append("     AND ASLV.RUISEKI_DIV = '3' ");
                } else {
                    stb.append("     AND ASLV.RUISEKI_DIV = '1' ");
                }
                stb.append("     AND ASLV.CLASSCD = T3.CLASSCD ");
                stb.append("     AND ASLV.SCHOOL_KIND = T3.SCHOOL_KIND ");
                stb.append("     AND ASLV.CURRICULUM_CD = T3.CURRICULUM_CD ");
                stb.append("     AND ASLV.SUBCLASSCD = T3.SUBCLASSCD ");
                stb.append("     AND ASLV.DIV = '5' ");
                stb.append("     AND ASLV.GRADE = T1.GRADE ");
                stb.append("     AND ASLV.HR_CLASS = '000' ");
                stb.append("     AND ASLV.COURSECD = '0' ");
                stb.append("     AND ASLV.MAJORCD = T1.GROUP_CD ");
                stb.append("     AND ASLV.COURSECODE = '0000' ");
                stb.append("     AND TRANK.SCORE BETWEEN ASLV.ASSESSLOW AND ASLV.ASSESSHIGH ");
                stb.append(" UNION ALL ");
                stb.append(" SELECT ");
                stb.append("   T1.GRADE_NAME1, ");
                stb.append("   T1.GROUP_CD, ");
                stb.append("   T1.GROUP_NAME, ");
                stb.append("   T1.GRADE, ");
                stb.append("   T1.HR_CLASS, ");
                stb.append("   T1.HR_NAME, ");
                stb.append("   T1.STAFFNAME, ");
                stb.append("   T1.ATTENDNO, ");
                stb.append("   T1.SCHREGNO, ");
                stb.append("   T1.NAME, ");
                stb.append("   T1.COURSE, ");
                stb.append("   CAST(NULL AS VARCHAR(1)) AS CHAIRCD, ");
                stb.append("   TRANK.SUBCLASSCD AS SUBCLASSCD, ");
                stb.append("   CAST(NULL AS VARCHAR(1)) AS SUBCLASSNAME, ");
                stb.append("   CAST(NULL AS VARCHAR(1)) AS SUBCLASSABBV, ");
                stb.append("   CAST(NULL AS VARCHAR(1)) AS REQUIRE_FLG, ");
                stb.append("   TRANK.SEMESTER, ");
                stb.append("   TRANK.TESTKINDCD || TRANK.TESTITEMCD || TRANK.SCORE_DIV AS TESTCD, ");
                stb.append("   CAST(NULL AS VARCHAR(1)) AS VALUE_DI, ");
                stb.append("   TRANK.SCORE, ");
                stb.append("   CAST(NULL AS SMALLINT) AS ASSESSLEVEL, ");
                stb.append("   TRANK.AVG, ");
                stb.append("   TRANK.GRADE_RANK, ");
                stb.append("   TRANK.GRADE_AVG_RANK, ");
                stb.append("   TRANK.CLASS_RANK, ");
                stb.append("   TRANK.CLASS_AVG_RANK, ");
                stb.append("   TRANK.COURSE_RANK, ");
                stb.append("   TRANK.COURSE_AVG_RANK, ");
                stb.append("   TRANK.MAJOR_RANK, ");
                stb.append("   TRANK.MAJOR_AVG_RANK ");
                stb.append(" FROM REGD T1 ");
                stb.append(" INNER JOIN ");
                if ("2".equals(param._output)) {
                    stb.append("  RECORD_RANK_RUIKEI_SDIV_DAT  ");
                } else {
                    stb.append("  RECORD_RANK_SDIV_DAT  ");
                }
                stb.append("   TRANK ON TRANK.YEAR = T1.YEAR ");
                stb.append("     AND TRANK.SEMESTER = '" + param._semester + "' AND TRANK.TESTKINDCD || TRANK.TESTITEMCD || TRANK.SCORE_DIV = '" + param._testcd + "' ");
                stb.append("     AND TRANK.SUBCLASSCD IN ('" + SUBCLASSCD_777777 + "', '" + SUBCLASSCD_888888 + "', '" + SUBCLASSCD_999999 + "') ");
                stb.append("     AND TRANK.SCHREGNO = T1.SCHREGNO ");
                stb.append(" ORDER BY ");
                stb.append("     GROUP_CD, ");
                stb.append("     GRADE, ");
                stb.append("     HR_CLASS, ");
                stb.append("     ATTENDNO ");

                final String sql = stb.toString();
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                final Map courseGroupMap = new HashMap();
                while (rs.next()) {
                    final String groupCd = rs.getString("GROUP_CD");
                    if (null == courseGroupMap.get(groupCd)) {
                        final String gradeName1 = rs.getString("GRADE_NAME1");
                        final String groupName = rs.getString("GROUP_NAME");
                        
                        final CourseGroup coursegroup = new CourseGroup(gradeName1, groupCd, groupName);
                        courseGroupList.add(coursegroup);
                        courseGroupMap.put(groupCd, coursegroup);
                    }
                    final CourseGroup coursegroup = (CourseGroup) courseGroupMap.get(groupCd);
                    
                    final String grade = rs.getString("GRADE");
                    final String hrClass = rs.getString("HR_CLASS");
                    if (null == HrClass.getHrClass(grade + hrClass, coursegroup._hrList)) {
                        final String hrName = rs.getString("HR_NAME");
                        final String staffname = rs.getString("STAFFNAME");
                        
                        final HrClass hr = new HrClass(grade, hrClass, hrName, staffname);
                        coursegroup._hrList.add(hr);
                    }

                    final HrClass hr = HrClass.getHrClass(grade + hrClass, coursegroup._hrList);
                    final String schregno = rs.getString("SCHREGNO");
                    if (null == Student.getStudent(schregno, hr._studentList)) {
                        final String hrName = rs.getString("HR_NAME");
                        final String staffname = rs.getString("STAFFNAME");
                        final String attendno = rs.getString("ATTENDNO");
                        final String name = rs.getString("NAME");
                        final String course = rs.getString("COURSE");
                        
                        final Student student = new Student(grade, hrClass, hrName, staffname, attendno, schregno, name, course);
                        schregMap.put(schregno, student);
                        hr._studentList.add(student);
                    }
                    
                    final String subclasscd = rs.getString("SUBCLASSCD");
                    if (null != subclasscd) {
                        final Student student = Student.getStudent(schregno, hr._studentList);
                        final String chaircd = rs.getString("CHAIRCD");
                        
                        final String subclassname = rs.getString("SUBCLASSNAME");
                        final String subclassabbv = rs.getString("SUBCLASSABBV");
                        final String requireFlg = rs.getString("REQUIRE_FLG");
                        
                        final Subclass subclass = new Subclass(subclasscd, subclassname, subclassabbv, requireFlg);
                        hr._subclassMap.put(subclasscd, subclass);

                        final boolean isTotal = (SUBCLASSCD_777777.equals(subclasscd) || SUBCLASSCD_888888.equals(subclasscd) || SUBCLASSCD_999999.equals(subclasscd));

                        final String score;
                        if (null != rs.getString("VALUE_DI")) {
                            score = rs.getString("VALUE_DI");
                        } else if (PRGID_KNJD678.equals(param._prgid) && !isTotal && "2".equals(param._output)) {
                            score = rs.getString("ASSESSLEVEL");
                        } else {
                            score = rs.getString("SCORE");
                        }
                        final Score s = new Score(chaircd, subclasscd, score, rs.getBigDecimal("AVG")
                                , toInteger(rs.getString("GRADE_RANK")), toInteger(rs.getString("GRADE_AVG_RANK"))
                                , toInteger(rs.getString("CLASS_RANK")), toInteger(rs.getString("CLASS_AVG_RANK"))
                                , toInteger(rs.getString("COURSE_RANK")), toInteger(rs.getString("COURSE_AVG_RANK"))
                                , toInteger(rs.getString("MAJOR_RANK")), toInteger(rs.getString("MAJOR_AVG_RANK")));
                        student._scoreMap.put(subclasscd, s);
                    }
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            if (!PRGID_KNJD678.equals(param._prgid) && !"2".equals(param._output)) {
                try {
                    final StringBuffer stb = new StringBuffer();
                    stb.append(" SELECT ");
                    stb.append("   T2.SCHREGNO, ");
                    stb.append("   T2.CHAIRCD, ");
                    stb.append("   T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD AS SUBCLASSCD, ");
                    stb.append("   TREC.SEMESTER, ");
                    stb.append("   TREC.TESTKINDCD || TREC.TESTITEMCD || TREC.SCORE_DIV AS TESTCD, ");
                    stb.append("   TREC.VALUE_DI ");
                    stb.append(" FROM CHAIR_STD_DAT T2 ");
                    stb.append(" INNER JOIN CHAIR_DAT T3 ON T3.YEAR = T2.YEAR ");
                    stb.append("     AND T3.SEMESTER = T2.SEMESTER ");
                    stb.append("     AND T3.CHAIRCD = T2.CHAIRCD ");
                    stb.append("     AND T3.CLASSCD <= '90' ");
                    stb.append(" INNER JOIN RECORD_SCORE_DAT TREC ON TREC.YEAR = T3.YEAR ");
                    if ("2".equals(param._output)) {
                        stb.append("     AND TREC.SEMESTER = '" + param._semester + "' AND TREC.TESTKINDCD || TREC.TESTITEMCD || TREC.SCORE_DIV <= '" + param._testcd + "' ");
                    } else {
                        stb.append("     AND TREC.SEMESTER = '" + param._semester + "' AND TREC.TESTKINDCD || TREC.TESTITEMCD = '" + param._testcd.substring(0, 4) + "' ");
                    }
                    stb.append("     AND TREC.SCORE_DIV = '01' ");
                    stb.append("     AND TREC.CLASSCD = T3.CLASSCD ");
                    stb.append("     AND TREC.SCHOOL_KIND = T3.SCHOOL_KIND ");
                    stb.append("     AND TREC.CURRICULUM_CD = T3.CURRICULUM_CD ");
                    stb.append("     AND TREC.SUBCLASSCD = T3.SUBCLASSCD ");
                    stb.append("     AND TREC.SCHREGNO = T2.SCHREGNO ");
                    stb.append("     AND TREC.VALUE_DI = '*' ");
                    stb.append(" WHERE ");
                    stb.append("     T2.YEAR = '" + param._year + "' ");

                    final String sql = stb.toString();
                    ps = db2.prepareStatement(sql);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        final Student student = (Student) schregMap.get(rs.getString("SCHREGNO"));
                        if (null == student) {
                            continue;
                        }
                        final Score s = (Score) student._scoreMap.get(rs.getString("SUBCLASSCD"));
                        if (null == s) {
                            continue;
                        }
                        s._score = "*"; // 1個でも欠試があれば"*"
                        log.info(" schregno = " + student._schregno + " , " + s._subclasscd + ", " + s._score);
                    }
                } catch (Exception ex) {
                    log.fatal("exception!", ex);
                } finally {
                    DbUtils.closeQuietly(null, ps, rs);
                    db2.commit();
                }
            }
            return courseGroupList;
        }
        
        private static Integer toInteger(final String s) {
            if (NumberUtils.isDigits(s)) {
                return Integer.valueOf(s);
            }
            return null;
        }
    }
    
    private static class RecordAverageDat {
        final String _subclasscd;
        final String _avgDivKey;
        final String _score;
        final String _highscore;
        final String _lowscore;
        final String _count;
        final BigDecimal _avg;
        final BigDecimal _stddev;

        RecordAverageDat(
            final String subclasscd,
            final String avgDivKey,
            final String score,
            final String highscore,
            final String lowscore,
            final String count,
            final BigDecimal avg,
            final BigDecimal stddev
        ) {
            _subclasscd = subclasscd;
            _avgDivKey = avgDivKey;
            _score = score;
            _highscore = highscore;
            _lowscore = lowscore;
            _count = count;
            _avg = avg;
            _stddev = stddev;
        }
        
        public static Map getSubclassMap(final Map avgDatMap, final String avgDivKey) {
            return getMappedMap(avgDatMap, avgDivKey);
        }
        
        public static RecordAverageDat get(final Map avgDatMap, final String avgDivKey, final String subclasscd) {
            return (RecordAverageDat) getSubclassMap(avgDatMap, avgDivKey).get(subclasscd);
        }
        
        public static String getGradeAvgDivKey(final String grade) {
            return "1" + "-" + grade + "-" + "000" + "-" + "00000000";
        }
        
        public static String getHrAvgDivKey(final String grade, final String hrClass) {
            return "2" + "-" + grade + "-" + hrClass + "-" + "00000000";
        }
        
        public static String getCourseAvgDivKey(final String grade, final String coursecd, final String majorcd, final String coursecode) {
            return "3" + "-" + grade + "-" + "000" + "-" + coursecd + majorcd + coursecode;
        }
        
        public static String getCourseGroupAvgDivKey(final String grade, final String coursegroupCd) {
            return "5" + "-" + grade + "-" + "000" + "-" + "0" + coursegroupCd + "0000";
        }

        public static Map getRecordAverageDatMap(final DB2UDB db2, final Param param) {
            final Map map = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT  ");
                stb.append("  CLASSCD, ");
                stb.append("  SCHOOL_KIND, ");
                stb.append("  CURRICULUM_CD, ");
                stb.append("  SUBCLASSCD, ");
                stb.append("  AVG_DIV || '-' || GRADE || '-' || HR_CLASS || '-' || COURSECD || MAJORCD || COURSECODE AS AVG_DIV_KEY, ");
                stb.append("  SCORE, ");
                stb.append("  HIGHSCORE, ");
                stb.append("  LOWSCORE, ");
                stb.append("  COUNT, ");
                stb.append("  AVG, ");
                stb.append("  STDDEV ");
                stb.append(" FROM  ");
                if ("2".equals(param._output)) {
                    stb.append("  RECORD_AVERAGE_RUIKEI_SDIV_DAT  ");
                } else {
                    stb.append("  RECORD_AVERAGE_SDIV_DAT  ");
                }
                stb.append(" WHERE  ");
                stb.append("  YEAR = '" + param._year + "' ");
                stb.append("  AND SEMESTER || TESTKINDCD || TESTITEMCD || SCORE_DIV= '" + param._semester + param._testcd + "' ");

                final String sql = stb.toString();
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String subclasscd;
                    if ("333333".equals(rs.getString("SUBCLASSCD")) || "555555".equals(rs.getString("SUBCLASSCD")) || SUBCLASSCD_777777.equals(rs.getString("SUBCLASSCD")) || SUBCLASSCD_888888.equals(rs.getString("SUBCLASSCD")) || SUBCLASSCD_999999.equals(rs.getString("SUBCLASSCD"))) {
                        subclasscd = rs.getString("SUBCLASSCD");
                    } else {
                        subclasscd = rs.getString("CLASSCD") + "-" + rs.getString("SCHOOL_KIND") + "-" + rs.getString("CURRICULUM_CD") + "-" + rs.getString("SUBCLASSCD");
                    }
                    final String avgDivKey = rs.getString("AVG_DIV_KEY");
                    final String score = rs.getString("SCORE");
                    final String highscore = rs.getString("HIGHSCORE");
                    final String lowscore = rs.getString("LOWSCORE");
                    final String count = rs.getString("COUNT");
                    final BigDecimal avg = rs.getBigDecimal("AVG");
                    final BigDecimal stddev = rs.getBigDecimal("STDDEV");
                    final RecordAverageDat recordaveragedat = new RecordAverageDat(subclasscd, avgDivKey, score, highscore, lowscore, count, avg, stddev);
                    getMappedMap(map, avgDivKey).put(subclasscd, recordaveragedat);
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return map;
        }
    }
    
    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 56595 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _year;
        final String _semester;
        final String _ctrlSemester;
        final String _grade;
        final String _gradeHrclass;
        final String _testcd;
        final String _groupCd;
        final String _loginDate;
        final String _output; // 帳票 1:考査順 2:累積別
        final String _ranking; // 順位 1:番号順 2:成績順
        final String _prgid;
        final Map _adminControlTestitemDat;

        final String _testitemname;
        final String _semestername;
        final String _schoolKind;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _prgid = request.getParameter("PRGID");
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _testcd = request.getParameter("TESTKIND_ITEMCD");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _grade = request.getParameter("GRADE");
            _gradeHrclass = request.getParameter("GRADE_HR_CLASS");
            _groupCd = request.getParameter("GROUP_CD");
            _loginDate = request.getParameter("LOGIN_DATE");
            _output = PRGID_KNJD678.equals(_prgid) ? "1" : request.getParameter("OUTPUT");
            _ranking = request.getParameter("RANKING");

            _adminControlTestitemDat = getAdminControlTestitemDat(db2);
            _testitemname = getTestitemname(db2);
            _semestername = getSemestername(db2);
            _schoolKind = getSchoolKind(db2);
        }
        
        /** 作成日 */
        public String getNow() {
            final StringBuffer stb = new StringBuffer();
            final Date date = new Date();
            final SimpleDateFormat sdfY = new SimpleDateFormat("yyyy");
            stb.append(nao_package.KenjaProperties.gengou(Integer.parseInt(sdfY.format(date))));
            final SimpleDateFormat sdf = new SimpleDateFormat("年M月d日H時m分");
            stb.append(sdf.format(date));
            return stb.toString();
        }

        public String getRegdSemester() {
            return "9".equals(_semester) ? _ctrlSemester : _semester;
        }

        private Map getAdminControlTestitemDat(final DB2UDB db2) {
            String sql = "";
            sql += " SELECT ";
            sql += "   COURSECD || MAJORCD || COURSECODE AS COURSE, ";
            sql += "   CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS SUBCLASSCD, ";
            sql += "   SEMESTER || TESTKINDCD || TESTITEMCD AS SEM_KIND_ITEM ";
            sql += " FROM ADMIN_CONTROL_TESTITEM_DAT T1 ";
            sql += " WHERE ";
            sql += "     T1.YEAR = '" + _year + "' ";
            sql += "     AND GRADE = '" + _grade + "' ";
            sql += "     AND SUBCLASSCD <> '000000' ";
            Map rtn = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    getMappedList(getMappedMap(rtn, rs.getString("COURSE")), rs.getString("SUBCLASSCD")).add(rs.getString("SEM_KIND_ITEM"));
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            }
            return rtn;
        }

        private String getTestitemname(final DB2UDB db2) {
            String sql = "";
            sql += " SELECT TESTITEMNAME FROM TESTITEM_MST_COUNTFLG_NEW_SDIV ";
            sql += " WHERE ";
            sql += "   YEAR = '" + _year + "' ";
            sql += "   AND SEMESTER = '" + _semester + "' ";
            sql += "   AND TESTKINDCD || TESTITEMCD || SCORE_DIV = '" + _testcd + "' ";
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString("TESTITEMNAME");
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            }
            return rtn;
        }
        
        private String getSemestername(final DB2UDB db2) {
            String sql = "";
            sql += " SELECT SEMESTERNAME FROM SEMESTER_MST ";
            sql += " WHERE ";
            sql += "   YEAR = '" + _year + "' ";
            sql += "   AND SEMESTER = '" + _semester + "' ";
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString("SEMESTERNAME");
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            }
            return rtn;
        }
        
        private String getSchoolKind(final DB2UDB db2) throws SQLException {
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = "SELECT school_kind FROM schreg_regd_gdat WHERE year = '" + _year + "' AND grade = '" + _grade + "'";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    rtn = rs.getString("school_Kind");
                }
            } catch (final SQLException e) {
                log.error("学校種別取得エラー。");
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }
    }
}

// eof

