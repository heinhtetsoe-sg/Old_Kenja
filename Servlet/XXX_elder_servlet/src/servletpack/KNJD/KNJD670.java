/*
 * $Id: 7089940ecbce416d16cd4cfc1a8e379161b62e82 $
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
import java.sql.ResultSetMetaData;
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
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 * 文京学園 累積科目別 得点分布表
 */
public class KNJD670 {

    private static final Log log = LogFactory.getLog(KNJD670.class);

    private boolean _hasData;
    
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
    
    private static String sishaGonyu(final BigDecimal bd) {
        return null == bd ? null : bd.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        final int MAX_LINE = 50;
        final int MAX_COL = 5;
        final BigDecimal _100 = new BigDecimal(100);

        final List courseGroupList = CourseGroup.getCourseGroupList(db2, _param);

//      final Map avgDatMap = RecordAverageRuikeiDat.getRecordAverageRuikeiDatMap(db2, _param);
        
        for (int cgi = 0; cgi < courseGroupList.size(); cgi++) {
            final CourseGroup cg = (CourseGroup) courseGroupList.get(cgi);
            
            final List subclassList = getSubclassList(cg._studentList);
            Collections.sort(subclassList);

            for (int si = 0; si < subclassList.size(); si++) {
                
                final Subclass subclass = (Subclass) subclassList.get(si);

                int perfect = Perfect.getPerfect(db2, _param, subclass._subclasscd, cg._groupCd);
                if ("J".equals(_param._schoolKind)) {
                    perfect = 100;
                }
                final int kessekisha = _param.getKessekisha(db2, subclass._subclasscd, cg._groupCd);
                final List subclassStudentListAll = getSubclassScoreStudentList(cg._studentList, subclass._subclasscd);
                final BigDecimal subclassStudentCount = new BigDecimal(subclassStudentListAll.size());
                final List subclassStudentList = new ArrayList(subclassStudentListAll);
                
                final List distRangeListAll = DistRange.getList(perfect);
                int accum = 0;

                final List pageList = getGroupList(distRangeListAll, MAX_LINE * MAX_COL);
                for (int pi = 0; pi < pageList.size(); pi++) {
                    final List distRangeList = (List) pageList.get(pi); 

                    final String form = "KNJD670.frm";
                    svf.VrSetForm(form, 1);
                    
                    svf.VrsOut("GRADE_NAME", cg._gradeName1); // 学年名
                    svf.VrsOut("GROUP_NAME", cg._groupName); // コースグループ名
                    svf.VrsOut("TITLE", KenjaProperties.gengou(Integer.parseInt(_param._year)) + "年度　" + StringUtils.defaultString(_param._semestername) + "　" + StringUtils.defaultString(_param._testitemname) + "　科目別累積分布表"); // タイトル
                    svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._loginDate)); // 作成日
                    //svf.VrsOut("PAGE", null); // ページ
                    svf.VrsOut("SUBCLASS", subclass._subclassname); // 科目
                    svf.VrsOut("SELECT_MARK", "3".equals(subclass._requireFlg) ? "＊" : ""); // 選択科目

                    if (pi == pageList.size() - 1) {
                        // 科目ごとの最後のページ
                        svf.VrsOut("LABEL_TOTAL_CNT", "人数"); // 人数
                        svf.VrsOut("LABEL_MAX_SCORE", "最高点"); // 最高点
                        svf.VrsOut("LABEL_MIN_SCORE", "最低点"); // 最低点
                        svf.VrsOut("LABEL_AVG_SCORE", "平均点"); // 平均点
                        svf.VrsOut("LABEL_KESSEKI_CNT", "欠席者数"); // 欠席者数
                        
//                        final RecordAverageRuikeiDat avgDat = RecordAverageRuikeiDat.getCourseGroupAvg(avgDatMap, subclass._subclasscd, _param._grade, cg._groupCd);
//                        if (null != avgDat) {
//                            svf.VrsOut("TOTAL_CNT", avgDat._count); // 人数
//                            svf.VrsOut("MAX_SCORE", avgDat._highscore); // 最高点
//                            svf.VrsOut("MIN_SCORE", avgDat._lowscore); // 最低点
//                            svf.VrsOut("AVG_SCORE", sishaGonyu(avgDat._avg)); // 平均点
//                            svf.VrsOut("KESSEKI_CNT", String.valueOf(kessekisha)); // 欠席者数
//                        }

                        // 帳票で算出する
                        BigDecimal max = null;
                        BigDecimal min = null;
                        final List bdList = new ArrayList();
                        for (int i = 0; i < subclassStudentListAll.size(); i++) {
                            final Student student = (Student) subclassStudentListAll.get(i);
                            if (student.isTengakuTaigaku(_param._loginDate)) {
                                log.info(" tengaku: " + student._schregno + " grdDate = " + student._grdDate + " grdDiv = " + student._grdDiv);
                                continue;
                            }
                            final BigDecimal bd = student.score(subclass._subclasscd);
                            if (null != bd) {
                                max = null == max ? bd : max.compareTo(bd) > 0 ? max : bd;
                                min = null == min ? bd : min.compareTo(bd) < 0 ? min : bd;
                                bdList.add(bd);
                            }
                        }
                        log.info(" subclass " + subclass._subclasscd + ":" + subclass._subclassabbv  + ", avg: " + bdList.size() + ", " + max + ", " + min + ", student size = " + subclassStudentListAll.size());
                        
                        if (bdList.size() > 0) {
                            svf.VrsOut("TOTAL_CNT", String.valueOf(bdList.size())); // 人数
                            svf.VrsOut("MAX_SCORE", null == max ? "" : max.toString()); // 最高点
                            svf.VrsOut("MIN_SCORE", null == min ? "" : min.toString()); // 最低点
                            svf.VrsOut("AVG_SCORE", sishaGonyu(getAvg(bdList))); // 平均点
                            svf.VrsOut("KESSEKI_CNT", String.valueOf(kessekisha)); // 欠席者数
                        }

                    }
                    
                    final List columnList = getGroupList(distRangeList, MAX_LINE);
                    for (int coli = 0; coli < columnList.size(); coli++) {
                        final List lineList = (List) columnList.get(coli);
                        
                        for (int di = 0; di < lineList.size(); di++) {
                            final DistRange dr = (DistRange) lineList.get(di);
                            final String scoli = String.valueOf(coli + 1);
                            final int line = di + 1;
                            svf.VrsOutn("SCORE" + scoli, line, dr._title); // 得点
                            final int count = dr.getInRangeStudentList(subclassStudentList, subclass._subclasscd).size();
                            if (0 != count) {
                                svf.VrsOutn("SCH_CNT" + scoli, line, String.valueOf(count)); // 人数
                                
                                accum += count;
                                svf.VrsOutn("RUIKEI_CNT" + scoli, line, String.valueOf(accum)); // 累計・人数
                                if (subclassStudentCount.intValue() != 0) {
                                    final String accumRate = new BigDecimal(accum).multiply(_100).divide(subclassStudentCount, 1, BigDecimal.ROUND_DOWN).toString();
                                    svf.VrsOutn("RATIO" + scoli, line, accumRate); // 比率
                                }
                            }
                        }
                    }
                    
                    svf.VrEndPage();
                    _hasData = true;
                }
            }
        }
    }
    
    private static BigDecimal getAvg(final List bdList) {
        if (null == bdList || bdList.size() == 0) {
            return null;
        }
        BigDecimal sum = new BigDecimal(0);
        for (int i = 0; i < bdList.size(); i++) {
            final BigDecimal bd = (BigDecimal) bdList.get(i);
            sum = sum.add(bd);
        }
        return sum.divide(new BigDecimal(bdList.size()), 5, BigDecimal.ROUND_HALF_UP);
    }

    private static List getGroupList(final List list, final int max) {
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
    
    private List getSubclassScoreStudentList(final List studentList, final String subclasscd) {
        final List rtn = new ArrayList();
        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            if (student.isTengakuTaigaku(_param._loginDate)) {
                continue;
            }
            if (null == student._scoreMap.get(subclasscd)) {
                continue;
            }
            rtn.add(student);
        }
        return rtn;
    }

    private List getSubclassList(final List studentList) {
        final Map subclassMap = new TreeMap();
        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            subclassMap.putAll(student._subclassMap);
        }
        return new ArrayList(subclassMap.values());
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
        final BigDecimal _upper;
        final BigDecimal _lower;
        final boolean _inclusive;
        private DistRange(final String title, final BigDecimal lower, final BigDecimal upper, final boolean inclusive) {
            _title = title;
            _upper = upper;
            _lower = lower;
            _inclusive = inclusive;
        }

        public static List getList(final int perfect) {
            
            final List rangeList = new ArrayList();
            for (int i = perfect; i >= 0; i--) {
                final BigDecimal upper = new BigDecimal(i + 1); 
                final BigDecimal lower = new BigDecimal(i); 
                final DistRange drange = new DistRange(lower.toString(), lower, upper, false);
                rangeList.add(drange);
            }
            return rangeList;
        }
        
        public List getInRangeStudentList(final List studentList, final String subclasscd) {
            final List inrange = new ArrayList();
            for (final Iterator it = studentList.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                if (between(student.score(subclasscd))) {
                    inrange.add(student);
                    it.remove();
                }
            }
            return inrange;
        }

        public boolean between(final BigDecimal val) {
            if (null != val && null != _upper && null != _lower) {
                final boolean inRange = _lower.compareTo(val) <= 0 && (_inclusive && val.compareTo(_upper) <= 0 || !_inclusive && val.compareTo(_upper) < 0);
                //log.debug(" in range (" + _lower + ", " + _upper + ", " + _inclusive + ") , " + val + " = " + inRange);
                return inRange;
            }
            return false;
        }
    }
    
    private static class Subclass implements Comparable {
        final String _subclasscd;
        final String _subclassname;
        final String _subclassabbv;
        final String _requireFlg;
        public Subclass(final String subclasscd, final String subclassname, final String subclassabbv, final String requireFlg) {
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
    }
    
    private static class Student implements Comparable {
        final String _grade;
        final String _hrClass;
        final String _attendno;
        final String _schregno;
        final String _name;
        final String _course;
        String _grdDiv;
        String _grdDate;
        final Map _subclassMap = new HashMap();
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
        
        public boolean isTengakuTaigaku(final String kijunbi) {
            if (null != kijunbi && null != _grdDate && ("2".equals(_grdDiv) || "3".equals(_grdDiv))) {
                return kijunbi.compareTo(_grdDate) > 0;
            }
            return false;
        }
        
        public BigDecimal score(final String subclasscd) {
            return (BigDecimal) _scoreMap.get(subclasscd);
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

    private static class CourseGroup {
        final String _gradeName1;
        final String _groupCd;
        final String _groupName;
        final List _studentList;

        CourseGroup(
            final String gradeName1,
            final String groupCd,
            final String groupName
        ) {
            _gradeName1 = gradeName1;
            _groupCd = groupCd;
            _groupName = groupName;
            _studentList = new ArrayList();
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
                stb.append("       HRSTF.STAFFNAME, ");
                stb.append("       T1.ATTENDNO, ");
                stb.append("       T1.SCHREGNO, ");
                stb.append("       T1.COURSECD, ");
                stb.append("       T1.MAJORCD, ");
                stb.append("       T1.COURSECODE, ");
                stb.append("       BASE.NAME, ");
                stb.append("       ENTGRD.GRD_DIV, ");
                stb.append("       ENTGRD.GRD_DATE ");
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
                stb.append("     LEFT JOIN SCHREG_ENT_GRD_HIST_DAT ENTGRD ON ENTGRD.SCHREGNO = T1.SCHREGNO AND ENTGRD.SCHOOL_KIND = GDAT.SCHOOL_KIND ");
                stb.append("     WHERE ");
                stb.append("         T1.YEAR = '" + param._year + "' ");
                stb.append("         AND T1.SEMESTER = '" + param.getRegdSemester() + "' ");
                stb.append("         AND T1.GRADE = '" + param._grade + "' ");
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
                stb.append("   T1.GRD_DIV, ");
                stb.append("   T1.GRD_DATE, ");
                stb.append("   T1.COURSECD || T1.MAJORCD || T1.COURSECODE AS COURSE, ");
                stb.append("   T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD AS SUBCLASSCD, ");
                stb.append("   T3.SUBCLASSCD AS SUBCLASSCD_ONLY, ");
                stb.append("   SUBM.SUBCLASSNAME, ");
                stb.append("   SUBM.SUBCLASSABBV, ");
                stb.append("   CRE.REQUIRE_FLG, ");
                stb.append("   TRANK.SEMESTER, ");
                stb.append("   TRANK.TESTKINDCD || TRANK.TESTITEMCD || TRANK.SCORE_DIV AS TESTCD, ");
                stb.append("   TRANK.SCORE ");
                stb.append(" FROM REGD T1 ");
                stb.append(" LEFT JOIN CHAIR_STD_DAT T2 ON T2.YEAR = T1.YEAR ");
                stb.append("     AND T2.SEMESTER <= T1.SEMESTER ");
                stb.append("     AND T2.SCHREGNO = T1.SCHREGNO ");
                stb.append(" LEFT JOIN CHAIR_DAT T3 ON T3.YEAR = T2.YEAR ");
                stb.append("     AND T3.SEMESTER = T2.SEMESTER ");
                stb.append("     AND T3.CHAIRCD = T2.CHAIRCD ");
                stb.append("     AND T3.CLASSCD <= '90' ");
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
                if ("2".equals(param._output)) {
                    stb.append(" LEFT JOIN RECORD_RANK_RUIKEI_SDIV_DAT TRANK ON TRANK.YEAR = T3.YEAR ");
                } else {
                    stb.append(" LEFT JOIN RECORD_RANK_SDIV_DAT TRANK ON TRANK.YEAR = T3.YEAR ");
                }
                stb.append("     AND TRANK.SEMESTER = '" + param._semester + "' AND TRANK.TESTKINDCD || TRANK.TESTITEMCD || TRANK.SCORE_DIV = '" + param._testcd + "' ");
                stb.append("     AND TRANK.CLASSCD = T3.CLASSCD ");
                stb.append("     AND TRANK.SCHOOL_KIND = T3.SCHOOL_KIND ");
                stb.append("     AND TRANK.CURRICULUM_CD = T3.CURRICULUM_CD ");
                stb.append("     AND TRANK.SUBCLASSCD = T3.SUBCLASSCD ");
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

                    final String schregno = rs.getString("SCHREGNO");
                    if (null == Student.getStudent(schregno, coursegroup._studentList)) {
                        final String hrName = rs.getString("HR_NAME");
                        final String staffname = rs.getString("STAFFNAME");
                        final String attendno = rs.getString("ATTENDNO");
                        final String name = rs.getString("NAME");
                        final String course = rs.getString("COURSE");
                        
                        final Student student = new Student(grade, hrClass, hrName, staffname, attendno, schregno, name, course);
                        student._grdDiv = rs.getString("GRD_DIV");
                        student._grdDate = rs.getString("GRD_DATE");
                        coursegroup._studentList.add(student);
                    }
                    final String subclasscdOnly = rs.getString("SUBCLASSCD_ONLY");
                    if ("333333".equals(subclasscdOnly) || "555555".equals(subclasscdOnly) || "999999".equals(subclasscdOnly) || "777777".equals(subclasscdOnly) || "888888".equals(subclasscdOnly)) {
                        continue;
                    }
                    final String subclasscd = rs.getString("SUBCLASSCD");
                    if (null == subclasscd) {
                        continue;
                    }
                    
                    final Student student = Student.getStudent(schregno, coursegroup._studentList);
                    student._subclassMap.put(subclasscd, new Subclass(subclasscd, rs.getString("SUBCLASSNAME"), rs.getString("SUBCLASSABBV"), rs.getString("REQUIRE_FLG")));
                    
                    if (null != rs.getBigDecimal("SCORE")) {
                        student._scoreMap.put(subclasscd, rs.getBigDecimal("SCORE"));
                        log.debug(" student = " + student._schregno + ", subclasscd = " + subclasscd + ", score = " + rs.getString("SCORE"));
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
    
    private static class RecordAverageRuikeiDat {
        final String _subclasscd;
        final String _avgDivKey;
        final String _score;
        final String _highscore;
        final String _lowscore;
        final String _count;
        final BigDecimal _avg;
        final BigDecimal _stddev;

        RecordAverageRuikeiDat(
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
        
        public static RecordAverageRuikeiDat getGradeAvg(final Map map, final String subclasscd, final String grade) {
            final String avgDivKey = "1" + "-" + grade + "-" + "000" + "-" + "00000000";
            return (RecordAverageRuikeiDat) getMappedMap(map, subclasscd).get(avgDivKey);
        }
        
        public static RecordAverageRuikeiDat getHrAvg(final Map map, final String subclasscd, final String grade, final String hrClass) {
            final String avgDivKey = "2" + "-" + grade + "-" + hrClass + "-" + "00000000";
            return (RecordAverageRuikeiDat) getMappedMap(map, subclasscd).get(avgDivKey);
        }
        
        public static RecordAverageRuikeiDat getCourseAvg(final Map map, final String subclasscd, final String grade, final String coursecd, final String majorcd, final String coursecode) {
            final String avgDivKey = "3" + "-" + grade + "-" + "000" + "-" + coursecd + majorcd + coursecode;
            return (RecordAverageRuikeiDat) getMappedMap(map, subclasscd).get(avgDivKey);
        }
        
        public static RecordAverageRuikeiDat getCourseGroupAvg(final Map map, final String subclasscd, final String grade, final String coursegroupCd) {
            final String avgDivKey = "5" + "-" + grade + "-" + "000" + "-" + "0" + coursegroupCd + "0000";
            return (RecordAverageRuikeiDat) getMappedMap(map, subclasscd).get(avgDivKey);
        }

        public static Map getRecordAverageRuikeiDatMap(final DB2UDB db2, final Param param) {
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
                    if ("333333".equals(rs.getString("SUBCLASSCD")) || "555555".equals(rs.getString("SUBCLASSCD")) || "999999".equals(rs.getString("SUBCLASSCD")) || "777777".equals(rs.getString("SUBCLASSCD")) || "888888".equals(rs.getString("SUBCLASSCD")) ) {
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
                    final RecordAverageRuikeiDat recordaveragedat = new RecordAverageRuikeiDat(subclasscd, avgDivKey, score, highscore, lowscore, count, avg, stddev);
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
    
    private static class Perfect {
        private static int getPerfect(final DB2UDB db2, final Param param, final String subclasscd, final String groupCd) {
            
            final int testCnt = getTestCnt(db2, param, groupCd, subclasscd);

            final Map perfect = getRow(db2, getPerfectSql(param, groupCd, subclasscd));
            log.debug(" query perfect = " + perfect);
            int perfectSumPerfect = parseInt((String) perfect.get("SUM_PERFECT"), 0);
            final int perfectCnt = parseInt((String) perfect.get("CNT"), 0);
            if (perfectCnt == 0 && testCnt == 0) {
                return 0;
            }

            if (perfectCnt != testCnt) {
                perfectSumPerfect += (testCnt - perfectCnt) * 100;
            }
            //log.debug(" sumPerfect = " + perfectSumPerfect);
            return perfectSumPerfect;
        }
        
        private static int parseInt(final String s, final int def) {
           return NumberUtils.isDigits(s) ? Integer.parseInt(s) : def;
        }

        public static int getTestCnt(final DB2UDB db2, final Param param, final String groupCd, final String subclasscd) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     COUNT(DISTINCT SEMESTER || TESTKINDCD || TESTITEMCD) AS COUNT ");
            stb.append(" FROM ");
            stb.append("     ADMIN_CONTROL_TESTITEM_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.GRADE = '" + param._grade + "' ");
            stb.append("     AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '" + subclasscd + "' ");
            stb.append("     AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE ");
            stb.append("         IN (SELECT ");
            stb.append("                 I1.COURSECD || I1.MAJORCD || I1.COURSECODE ");
            stb.append("             FROM ");
            stb.append("                 COURSE_GROUP_CD_DAT I1 ");
            stb.append("             WHERE ");
            stb.append("                 I1.YEAR    = '" + param._year + "' ");
            stb.append("                 AND I1.GRADE   = '" + param._grade + "' ");
            stb.append("                 AND I1.GROUP_CD   = '" + groupCd + "' ");
            stb.append("         ) ");

            //log.debug(" testCnt sql = " + stb.toString());
            final int testSubclassCnt = getOneInt(db2, stb.toString());
            stb.delete(0, stb.length());
            final String testSubclasscd = testSubclassCnt > 0 ? subclasscd : "00" + "-" + param._schoolKind + "-" + "00" + "-" + "000000";
            
            final StringBuffer sstb = new StringBuffer();
            sstb.append(" SELECT ");
            sstb.append("     COUNT(DISTINCT SEMESTER || TESTKINDCD || TESTITEMCD) AS COUNT ");
            sstb.append(" FROM ");
            sstb.append("     ADMIN_CONTROL_TESTITEM_DAT T1 ");
            sstb.append(" WHERE ");
            sstb.append("     T1.YEAR = '" + param._year + "' ");
            if (!param._isRuiseki) {
                sstb.append("     AND T1.SEMESTER = '" + param._semester + "' ");
            }
            if (param._isRuiseki || !param._isRuiseki && "99".equals(param._testcd.substring(0, 2))) {
                sstb.append("     AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD <= '" + param._semester + param._testcd.substring(0, 4) + "' ");
            } else {
                sstb.append("     AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD = '" + param._semester + param._testcd.substring(0, 4) + "' ");
            }
            sstb.append("     AND T1.GRADE = '" + param._grade + "' ");
            sstb.append("     AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '" + testSubclasscd + "' ");
            sstb.append("     AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE ");
            sstb.append("         IN (SELECT ");
            sstb.append("                 I1.COURSECD || I1.MAJORCD || I1.COURSECODE ");
            sstb.append("             FROM ");
            sstb.append("                 COURSE_GROUP_CD_DAT I1 ");
            sstb.append("             WHERE ");
            sstb.append("                 I1.YEAR    = '" + param._year + "' ");
            sstb.append("                 AND I1.GRADE   = '" + param._grade + "' ");
            sstb.append("                 AND I1.GROUP_CD   = '" + groupCd + "' ");
            sstb.append("         ) ");
            //log.debug(" testCnt sql = " + sstb.toString());
            final int testCnt = getOneInt(db2, sstb.toString());
            stb.delete(0, sstb.length());
            log.debug(" testSubclasscd = " + testSubclasscd + ", testCnt = " + testCnt);
            return testCnt;
        }

        //満点取得
        private static String getPerfectSql(final Param param, final String groupCd, final String subclasscd) {
            final StringBuffer stb2 = new StringBuffer();
            stb2.append(" SELECT ");
            stb2.append("     SUM(T1.PERFECT) AS SUM_PERFECT, ");
            stb2.append("     COUNT(*) AS CNT ");
            stb2.append(" FROM ");
            stb2.append("     PERFECT_RECORD_SDIV_DAT T1 ");
            stb2.append(" WHERE ");
            stb2.append("     T1.YEAR = '" + param._year + "' ");
            if (!param._isRuiseki) {
                stb2.append("     AND T1.SEMESTER = '" + param._semester + "' ");
            }
            if (param._isRuiseki || !param._isRuiseki && "99".equals(param._testcd.substring(0, 2))) {
                stb2.append("     AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD <= '" + param._semester + param._testcd.substring(0, 4) + "' ");
            } else {
                stb2.append("     AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD = '" + param._semester + param._testcd.substring(0, 4) + "' ");
            }
            stb2.append("     AND T1.TESTKINDCD IN ('01', '02') ");
            stb2.append("     AND T1.SCORE_DIV = '01' ");
            stb2.append("     AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '" + subclasscd + "' ");
            stb2.append("     AND T1.GRADE = CASE WHEN T1.DIV = '01' THEN '00' ELSE '" + param._grade + "' END ");
            stb2.append("     AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE = ");
            stb2.append("         CASE WHEN T1.DIV IN ('01','02') ");
            stb2.append("              THEN '00000000' ");
            stb2.append("              ELSE '0' || '" + groupCd + "' || '0000' END ");

            return stb2.toString();
        }
        
        private static Map getRow(final DB2UDB db2, final String sql) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map m = new HashMap();
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                final ResultSetMetaData meta = rs.getMetaData();
                if (rs.next()) {
                    for (int i = 1; i <= meta.getColumnCount(); i++) {
                        m.put(meta.getColumnName(i), rs.getString(meta.getColumnName(i)));
                    }
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return m;
        }
        
        private static int getOneInt(final DB2UDB db2, final String sql) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            int rtn = 0;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    rtn = rs.getInt(1);
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
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
        final String _groupCd;
        final String _loginDate;
        final String _testcd;
        final String _output;
        final String _schoolKind;
        final String _testitemname;
        final String _semestername;
        final boolean _isRuiseki = true;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _grade = request.getParameter("GRADE");
            _groupCd = request.getParameter("GROUP_CD");
            _loginDate = null == request.getParameter("LOGIN_DATE") ? request.getParameter("LOGIN_DATE") : StringUtils.replace(request.getParameter("LOGIN_DATE"), "/", "-");
            _testcd = request.getParameter("TESTKIND_ITEMCD");
            _schoolKind = getSchoolKind(db2);
            _output = "2";
            _testitemname = getTestitemname(db2);
            _semestername = getSemestername(db2);
        }

        public String getRegdSemester() {
            return "9".equals(_semester) ? _ctrlSemester : _semester;
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

        private String getSchoolKind(final DB2UDB db2) {
            String sql = "";
            sql += " SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT ";
            sql += " WHERE ";
            sql += "   YEAR = '" + _year + "' ";
            sql += "   AND GRADE = '" + _grade + "' ";
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString("SCHOOL_KIND");
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            }
            return rtn;
        }
        
        public int getKessekisha(final DB2UDB db2, final String subclasscd, final String groupCd) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH ADMIN_CONTROL_SDIV AS ( ");
            stb.append(" SELECT DISTINCT ");
            stb.append("   YEAR, ");
            stb.append("   SEMESTER, ");
            stb.append("   TESTKINDCD, ");
            stb.append("   TESTITEMCD, ");
            stb.append("   SCORE_DIV ");
            stb.append(" FROM ADMIN_CONTROL_SDIV_DAT ");
            stb.append(" WHERE ");
            stb.append(" YEAR = '" + _year + "' ");
            stb.append(" AND (TESTKINDCD = '01' OR TESTKINDCD = '02') ");
            stb.append(" AND SCORE_DIV = '01' "); // SCORE_DIVは'01'
            stb.append(" AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '00-" + _schoolKind + "-00-000000' ");
            stb.append(" AND SEMESTER || TESTKINDCD || TESTITEMCD || SCORE_DIV <= '" + _semester + _testcd + "' ");
            stb.append(" ) ");
            stb.append(" SELECT  ");
            stb.append(" COUNT(DISTINCT T2.SCHREGNO) AS KESSEKI_TOTAL ");
            stb.append(" FROM ADMIN_CONTROL_SDIV T1 ");
            stb.append(" INNER JOIN RECORD_SCORE_DAT T2 ON T2.YEAR = T1.YEAR ");
            stb.append("     AND T2.SEMESTER = T1.SEMESTER ");
            stb.append("     AND T2.TESTKINDCD = T1.TESTKINDCD ");
            stb.append("     AND T2.TESTITEMCD = T1.TESTITEMCD ");
            stb.append("     AND T2.SCORE_DIV = T1.SCORE_DIV ");
            stb.append("     AND T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD = '" + subclasscd + "' ");
            stb.append("     AND T2.VALUE_DI = '*' ");
            stb.append(" INNER JOIN SCHREG_REGD_DAT T3 ON T3.SCHREGNO = T2.SCHREGNO ");
            stb.append("     AND T3.YEAR = T2.YEAR ");
            stb.append("     AND T3.SEMESTER = T2.SEMESTER ");
            stb.append("     AND T3.GRADE = '" + _grade + "' ");
            stb.append(" INNER JOIN COURSE_GROUP_CD_DAT T4 ON T4.YEAR = T3.YEAR ");
            stb.append("     AND T4.GRADE = T3.GRADE ");
            stb.append("     AND T4.GROUP_Cd = '" + groupCd + "' ");
            stb.append("     AND T4.COURSECD = T3.COURSECD ");
            stb.append("     AND T4.MAJORCD = T3.MAJORCD ");
            stb.append("     AND T4.COURSECODE = T3.COURSECODE ");
            
            int rtn = 0;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getInt("KESSEKI_TOTAL");
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            }
            return rtn;
        }
    }
}

// eof

