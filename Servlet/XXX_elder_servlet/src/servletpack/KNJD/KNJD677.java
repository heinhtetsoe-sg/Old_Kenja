/*
 * $Id: a6905f2c9ef0a3094e77912859dd2f8e6ba52ab3 $
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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 * 文京学園 評定平均分布表
 */
public class KNJD677 {

    private static final Log log = LogFactory.getLog(KNJD677.class);

    private boolean _hasData;
    
    private static String SEM_TESTCD_GAKUNENHYOTEI = "9990009";

    private static String SUBCLASSCD_777777 = "777777";
    private static String SUBCLASSCD_999999 = "999999";

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
    
    private static String sishaGonyu(final BigDecimal bd) {
        return null == bd ? null : bd.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        
        final int MAX_HR = 24;
        
		final List courseGroupList = CourseGroup.getCourseGroupList(db2, _param);
		final Map assessCourseMstListMap = AssessCourseMst.getAssessCourseMstListMap(db2, _param);
        final List distRangeListABCDE = DistRange.getListABCDE(assessCourseMstListMap, courseGroupList);
        final List distRangeList = DistRange.getList();
		final List printHrClassList = CourseGroup.getHrClassList(courseGroupList);
		
        final List studentAllList = new ArrayList();
        for (int cgi = 0; cgi < courseGroupList.size(); cgi++) {
            final CourseGroup cg = (CourseGroup) courseGroupList.get(cgi);
            studentAllList.addAll(cg.getStudentList());
        }

		final Map avgDatMap = RecordAverageDat.getRecordAverageDatMap(db2, _param);
		
		for (int i = 0; i < printHrClassList.size(); i++) {
		    final HrClass hr = (HrClass) printHrClassList.get(i);
		    
	        final String form = "KNJD677.frm";
	        svf.VrSetForm(form, 1);
	        
	        svf.VrsOut("TITLE", KenjaProperties.gengou(Integer.parseInt(_param._year)) + "年度　" + StringUtils.defaultString(_param._semestername) + "　評定平均分布表"); // タイトル
	        svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._loginDate)); // 作成日
	        svf.VrsOut("HR_NAME", hr._hrName); // 年組名
	        svf.VrsOut("STAFFNAME" + (getMS932ByteLength(hr._staffname) > 20 ? "_2" : ""), hr._staffname); // 担任名
		    
            for (int di = 0; di < distRangeListABCDE.size(); di++) {
                final DistRange dr = (DistRange) distRangeListABCDE.get(di);
                // マーク表示
                svf.VrsOut("LABEL_LEVEL_CNT" + String.valueOf(di + 1), dr._title); // 評定段階別人数

            }
	        for (int di = 0; di < distRangeList.size(); di++) {
	            final DistRange dr = (DistRange) distRangeList.get(di);
	            // マーク表示
                svf.VrsOut("LABEL_AVG_CNT" + String.valueOf(di + 1), dr._title); // 評定平均別人数
	        }

	        int line = 1;
	        for (int cgi = 0; cgi < courseGroupList.size(); cgi++) {
	            final CourseGroup cg = (CourseGroup) courseGroupList.get(cgi);

                for (int hri = 0; hri < cg._hrList.size(); hri++) {
                    final HrClass dhr = (HrClass) cg._hrList.get(hri);

                    printDistHr(svf, distRangeListABCDE, distRangeList, line, cg._groupName, dhr._hrClassName1, dhr._studentList);
                    line += 1;
                }
                
                printDistHr(svf, distRangeListABCDE, distRangeList, line, "　　　　　小　　計　", null, cg.getStudentList());
                line += 1;
	        }

            printDistHr(svf, distRangeListABCDE, distRangeList, MAX_HR, "　　　　　合　　計　", null, studentAllList);

	        // クラス評定平均
	        for (int sti = 0; sti < hr._studentList.size(); sti++) {
	            final Student student = (Student) hr._studentList.get(sti);
	            
	            AssessCourseMst.setAssessMark(assessCourseMstListMap, student);

                svf.VrsOutn("ATTENDNO", sti + 1, NumberUtils.isDigits(student._attendno) ? String.valueOf(Integer.parseInt(student._attendno)): student._attendno); // 出席番号
                svf.VrsOutn("NAME" + (getMS932ByteLength(student._name) > 20 ? "_2" : ""), sti + 1, student._name); // 氏名
                svf.VrsOutn("ASSESS_AVG", sti + 1, null == student._hyoteiHeikin ? null : student._hyoteiHeikin.toString()); // 評定平均
                svf.VrsOutn("LEVEL", sti + 1, student._assessMark); // ランク
	        }
	        
	     
	        final String subclasscd;
            if ("1".equals(_param._sentakuCheck)) {
                subclasscd = SUBCLASSCD_777777;
            } else {
                subclasscd = SUBCLASSCD_999999;
            }
	        final RecordAverageDat avgDat = RecordAverageDat.getHrAvg(avgDatMap, subclasscd, hr._grade, hr._hrClass);
	        if (null != avgDat) {
	            svf.VrsOutn("ASSESS_AVG", 51, sishaGonyu(avgDat._avg)); // 評定平均
	        }
	        
	        _hasData = true;
            svf.VrEndPage();
		}
    }

    public void printDistHr(final Vrw32alp svf, final List distRangeListABCDE, final List distRangeList, int line, final String groupName, final String kumi, final List studentList) {
        svf.VrsOutn("GROUP_NAME1", line, groupName); // コースグループ名
        svf.VrsOutn("GROUP_NAME2", line, groupName); // コースグループ名
        svf.VrsOutn("KUMI1", line, kumi); // 組
        svf.VrsOutn("KUMI2", line, kumi); // 組

        for (int di = 0; di < distRangeListABCDE.size(); di++) {
            final DistRange dr = (DistRange) distRangeListABCDE.get(di);
            final int count = dr.getInRangeStudentList(studentList).size();
            svf.VrsOutn("LEVEL_CNT" + String.valueOf(di + 1), line, count == 0 ? null : String.valueOf(count)); // 評定段階別人数
        }
        for (int di = 0; di < distRangeList.size(); di++) {
            final DistRange dr = (DistRange) distRangeList.get(di);
            final int count = dr.getInRangeStudentList(studentList).size();
            svf.VrsOutn("AVG_CNT" + String.valueOf(di + 1), line, count == 0 ? null : String.valueOf(count)); // 評定段階別人数
        }
        svf.VrsOutn("KESSHI", line, null); // 欠試
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
    
    private static class DistRange {
        final String _title;
        final BigDecimal _lower;
        final BigDecimal _upper;
        final boolean _inclusive;
        private DistRange(final String title, final BigDecimal lower, final BigDecimal upper, final boolean inclusive) {
            _title = title;
            _lower = lower;
            _upper = upper;
            _inclusive = inclusive;
        }

        public static List getListABCDE(final Map assessCourseMstMap, final List courseGroupList) {
            List assessCourseMstList = Collections.EMPTY_LIST;
            setAssessCourseMstList:
            for (final Iterator it = courseGroupList.iterator(); it.hasNext();) {
                final CourseGroup cg = (CourseGroup) it.next();
                for (final Iterator hit = cg._hrList.iterator(); hit.hasNext();) {
                    final HrClass hr = (HrClass) hit.next();
                    for (final Iterator stit = hr._studentList.iterator(); stit.hasNext();) {
                        final Student student = (Student) stit.next();
                        if (null != student._course) {
                            assessCourseMstList = AssessCourseMst.getAssessMstListOfCourse(assessCourseMstMap, student._course);
                            if (assessCourseMstList.size() > 0) {
                                break setAssessCourseMstList;
                            }
                        }
                    }
                }
            }
            final List rangeList = new ArrayList();
            for (final Iterator it = assessCourseMstList.iterator(); it.hasNext();) {
                AssessCourseMst acm = (AssessCourseMst) it.next();
                DistRange drange = new DistRange(acm._assessmark, acm._assesslow, acm._assesshigh, true);
                rangeList.add(drange);
            }
            //log.debug(" rangeList = " + rangeList);
            return rangeList;
        }

        public static List getList() {
            final List rangeList = new ArrayList();
            final BigDecimal _10 = new BigDecimal(10);
            for (int i = 50; i >= 11; i--) {
                final BigDecimal upper = new BigDecimal(i + 1).divide(_10, 1, BigDecimal.ROUND_DOWN); 
                final BigDecimal lower = new BigDecimal(i).divide(_10, 1, BigDecimal.ROUND_DOWN); 
                final DistRange drange = new DistRange(lower.toString(), lower, upper, false);
                rangeList.add(drange);
            }
            return rangeList;
        }
        
        public List getInRangeStudentList(final List studentList) {
            final List inrange = new ArrayList();
            for (final Iterator it = studentList.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                if (between(student._hyoteiHeikin)) {
                    inrange.add(student);
                }
            }
            return inrange;
        }

        public boolean between(final BigDecimal val) {
            if (null != val && null != _upper && null != _lower) {
                return _lower.compareTo(val) <= 0 && (_inclusive && val.compareTo(_upper) <= 0 || !_inclusive && val.compareTo(_upper) < 0);
            }
            return false;
        }
        
        public String toString() {
            return "{ title : " + _title + ", lower : " + _lower + ", upper : " + _upper + "}";
        }
    }

    private static class Student implements Comparable {
        final String _grade;
        final String _hrClass;
        final String _attendno;
        final String _schregno;
        final String _name;
        final String _course;
        BigDecimal _hyoteiHeikin;
        String _assessMark;

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
        
        public int compareTo(Object o) {
            final Student student = (Student) o;
            return (_grade + _hrClass + _attendno).compareTo(student._grade + student._hrClass + student._attendno);
        }
    }

    private static class HrClass implements Comparable {
        final String _grade;
        final String _hrClass;
        final String _hrName;
        final String _hrClassName1;
        final String _staffname;
        final List _studentList;

        HrClass(
                final String grade,
                final String hrClass,
                final String hrName,
                final String hrClassName1,
                final String staffname) {
            _grade = grade;
            _hrClass = hrClass;
            _hrName = hrName;
            _hrClassName1 = hrClassName1;
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

        public int compareTo(Object o) {
            final HrClass hrClass = (HrClass) o;
            return (_grade + _hrClass).compareTo(hrClass._grade + hrClass._hrClass);
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

        public static List getHrClassList(final List courseGroupList) {
            final List hrList = new ArrayList();
            for (final Iterator it = courseGroupList.iterator(); it.hasNext();) {
                final CourseGroup cg = (CourseGroup) it.next();
                for (final Iterator hit = cg._hrList.iterator(); hit.hasNext();) {
                    final HrClass hr = (HrClass) hit.next();
                    if (null == HrClass.getHrClass(hr._grade + hr._hrClass, hrList)) {
                        hrList.add(new HrClass(hr._grade, hr._hrClass, hr._hrName, hr._hrClassName1, hr._staffname));
                    }
                    final HrClass nhr = HrClass.getHrClass(hr._grade + hr._hrClass, hrList);
                    nhr._studentList.addAll(hr._studentList);
                }
            }
            for (final Iterator hit = hrList.iterator(); hit.hasNext();) {
                final HrClass hr = (HrClass) hit.next();
                Collections.sort(hr._studentList);
            }
            Collections.sort(hrList);
            return hrList;
        }

        public List getStudentList() {
            final List studentList = new ArrayList();
            for (final Iterator it = _hrList.iterator(); it.hasNext();) {
                final HrClass hrClass = (HrClass) it.next();
                studentList.addAll(hrClass._studentList);
            }
            return studentList;
        }

        public static List getCourseGroupList(final DB2UDB db2, final Param param) {
            final List courseGroupList = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
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
                stb.append("       HDAT.HR_CLASS_NAME1, ");
                stb.append("       HRSTF.STAFFNAME, ");
                stb.append("       T1.ATTENDNO, ");
                stb.append("       T1.SCHREGNO, ");
                stb.append("       T1.COURSECD, ");
                stb.append("       T1.MAJORCD, ");
                stb.append("       T1.COURSECODE, ");
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
                stb.append("         AND T1.SEMESTER = '" + param.getRegdSemester() + "' ");
                stb.append("         AND T1.GRADE = '" + param._grade + "' ");
                stb.append(" ) ");
                stb.append(" SELECT ");
                stb.append("   T1.GRADE_NAME1, ");
                stb.append("   T1.GROUP_CD, ");
                stb.append("   T1.GROUP_NAME, ");
                stb.append("   T1.GRADE, ");
                stb.append("   T1.HR_CLASS, ");
                stb.append("   T1.HR_NAME, ");
                stb.append("   T1.HR_CLASS_NAME1, ");
                stb.append("   T1.STAFFNAME, ");
                stb.append("   T1.ATTENDNO, ");
                stb.append("   T1.SCHREGNO, ");
                stb.append("   T1.NAME, ");
                stb.append("   T1.COURSECD || T1.MAJORCD || T1.COURSECODE AS COURSE, ");
                stb.append("   TRANK.SUBCLASSCD AS SUBCLASSCD, ");
                stb.append("   TRANK.SEMESTER, ");
                stb.append("   TRANK.TESTKINDCD || TRANK.TESTITEMCD || TRANK.SCORE_DIV AS TESTCD, ");
                stb.append("   TRANK.SCORE, ");
                stb.append("   TRANK.AVG ");
                stb.append(" FROM REGD T1 ");
                stb.append(" LEFT JOIN RECORD_RANK_SDIV_DAT TRANK ON TRANK.YEAR = T1.YEAR ");
                stb.append("     AND TRANK.SEMESTER = '9' AND TRANK.TESTKINDCD || TRANK.TESTITEMCD || TRANK.SCORE_DIV = '990009' ");
                if ("1".equals(param._sentakuCheck)) {
                    stb.append("     AND TRANK.SUBCLASSCD = '" + SUBCLASSCD_777777 + "' ");
                } else {
                    stb.append("     AND TRANK.SUBCLASSCD = '" + SUBCLASSCD_999999 + "' ");
                }
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
                        final String hrClassName1 = rs.getString("HR_CLASS_NAME1");
                        final String staffname = rs.getString("STAFFNAME");
                        
                        final HrClass hr = new HrClass(grade, hrClass, hrName, hrClassName1, staffname);
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
                        hr._studentList.add(student);
                    }
                    
                    if (null != rs.getBigDecimal("AVG")) {
                        final Student student = Student.getStudent(schregno, hr._studentList);
                        student._hyoteiHeikin = rs.getBigDecimal("AVG").setScale(1, BigDecimal.ROUND_HALF_UP);
                    }
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return courseGroupList;
        }

    }
    
    private static class AssessCourseMst {
        final String _course;
        final String _assesslevel;
        final String _assessmark;
        final BigDecimal _assesslow;
        final BigDecimal _assesshigh;

        AssessCourseMst(
            final String course,
            final String assesslevel,
            final String assessmark,
            final BigDecimal assesslow,
            final BigDecimal assesshigh
        ) {
            _course = course;
            _assesslevel = assesslevel;
            _assessmark = assessmark;
            _assesslow = assesslow;
            _assesshigh = assesshigh;
        }
        
        public String toString() {
            return "{ course : " + _course + ", level : " + _assesslevel + ", mark : " + _assessmark + ", low : " + _assesslow + ", high : " + _assesshigh + "}";
        }
        
        public static void setAssessMark(final Map assessCourseMstMap, final Student student) {
            if (null == student._hyoteiHeikin) {
                return;
            }
            //log.debug(" course = " + student._course + " / " + assessCourseMstMap.get(student._course));
            final List assessCourseMstList = getAssessMstListOfCourse(assessCourseMstMap, student._course);
            for (final Iterator it = assessCourseMstList.iterator(); it.hasNext();) {
                AssessCourseMst mst = (AssessCourseMst) it.next();
                if (new DistRange(null, mst._assesslow, mst._assesshigh, true).between(student._hyoteiHeikin)) {
                    student._assessMark = mst._assessmark;
                    break;
                }
            }
        }

        public static List getAssessMstListOfCourse(final Map assessCourseMstMap, final String course) {
            return getMappedList(assessCourseMstMap, course);
        }

        public static Map getAssessCourseMstListMap(final DB2UDB db2, final Param param) {
            final Map map = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT  ");
                stb.append("  COURSECD || MAJORCD || COURSECODE AS COURSE, ");
                stb.append("  ASSESSLEVEL, ");
                stb.append("  ASSESSMARK, ");
                stb.append("  ASSESSLOW, ");
                stb.append("  ASSESSHIGH ");
                stb.append(" FROM  ");
                stb.append("  ASSESS_COURSE_MST  ");
                stb.append(" WHERE  ");
                stb.append(" ASSESSCD = '4'  ");
                stb.append(" ORDER BY  ");
                stb.append(" ASSESSLEVEL DESC ");

                final String sql = stb.toString();
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String course = rs.getString("COURSE");
                    final String assesslevel = rs.getString("ASSESSLEVEL");
                    final String assessmark = rs.getString("ASSESSMARK");
                    final BigDecimal assesslow = rs.getBigDecimal("ASSESSLOW");
                    final BigDecimal assesshigh = rs.getBigDecimal("ASSESSHIGH");
                    final AssessCourseMst assesscoursemst = new AssessCourseMst(course, assesslevel, assessmark, assesslow, assesshigh);
                    
                    getMappedList(map, course).add(assesscoursemst);
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
        
        public static RecordAverageDat getGradeAvg(final Map map, final String subclasscd, final String grade) {
            final String avgDivKey = "1" + "-" + grade + "-" + "000" + "-" + "00000000";
            return (RecordAverageDat) getMappedMap(map, subclasscd).get(avgDivKey);
        }
        
        public static RecordAverageDat getHrAvg(final Map map, final String subclasscd, final String grade, final String hrClass) {
            final String avgDivKey = "2" + "-" + grade + "-" + hrClass + "-" + "00000000";
            return (RecordAverageDat) getMappedMap(map, subclasscd).get(avgDivKey);
        }
        
        public static RecordAverageDat getCourseAvg(final Map map, final String subclasscd, final String grade, final String coursecd, final String majorcd, final String coursecode) {
            final String avgDivKey = "3" + "-" + grade + "-" + "000" + "-" + coursecd + majorcd + coursecode;
            return (RecordAverageDat) getMappedMap(map, subclasscd).get(avgDivKey);
        }
        
        public static RecordAverageDat getCourseGroupAvg(final Map map, final String subclasscd, final String grade, final String coursegroupCd) {
            final String avgDivKey = "5" + "-" + grade + "-" + "000" + "-" + "0" + coursegroupCd + "0000";
            return (RecordAverageDat) getMappedMap(map, subclasscd).get(avgDivKey);
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
                stb.append("  RECORD_AVERAGE_SDIV_DAT  ");
                stb.append(" WHERE  ");
                stb.append("  YEAR = '" + param._year + "' ");
                stb.append("  AND SEMESTER || TESTKINDCD || TESTITEMCD || SCORE_DIV= '" + SEM_TESTCD_GAKUNENHYOTEI + "' ");

                final String sql = stb.toString();
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String subclasscd;
                    if ("333333".equals(rs.getString("SUBCLASSCD")) || "555555".equals(rs.getString("SUBCLASSCD")) || SUBCLASSCD_999999.equals(rs.getString("SUBCLASSCD")) || SUBCLASSCD_777777.equals(rs.getString("SUBCLASSCD")) || "888888".equals(rs.getString("SUBCLASSCD")) ) {
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
                    getMappedMap(map, subclasscd).put(avgDivKey, recordaveragedat);
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
        final String _loginDate;
        final String _semestername;
        final String _sentakuCheck; // 1は「選択科目を除く」

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _grade = request.getParameter("GRADE");
            _loginDate = request.getParameter("LOGIN_DATE");
            _sentakuCheck = request.getParameter("SENTAKU_CHECK");
            _semestername = getSemestername(db2);
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
    }
}

// eof

