// kanji=漢字
/*
 * $Id: 360bc414c4e618bab79ecc01cf9ae178a9917956 $
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
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 * 成績一覧表（明治中高）
 * @author maesiro
 * @version $Id: 360bc414c4e618bab79ecc01cf9ae178a9917956 $
 */
public class KNJD617A {
    private static final Log log = LogFactory.getLog(KNJD617A.class);

    private final String Gakunen = "000";

    private static final String _333333 = "333333";
    private static final String _555555 = "555555";
    private static final String _999999 = "999999";
    
    // 1:学年 2:コース 3:コースグループ
    private static final String GROUP_GRADE = "1";
    private static final String GROUP_COURSE = "2";
    private static final String GROUP_COURSEGROUP = "3";

    private static final String SORT_DIV_ATTENDNO = "1";
    private static final String SORT_DIV_RANK = "2";

    // 平均点一覧HR最大表示数
    private static final int HR_MAX = 6;
    
    private final int COUNT1 = 45;  // 1枚目45名
    private final int COUNT2 = 50; // 2枚目以降50名

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
            
            _param = createParam(request, db2);
            
            _hasData = false;
            
            final List groupList = Group.getGroupList(db2, _param);
            log.debug(" groupList = " + groupList);
            for (final Iterator it = groupList.iterator(); it.hasNext();) {
                final Group group = (Group) it.next();
                printMain(db2, svf, group);
            }
            
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
    
    static String nullBlank(final Object o) {
        return null == o ? null : o.toString();
    }
    
    private void printMain(final DB2UDB db2, final Vrw32alp svf, final Group group) {
        
        boolean hasData = false;
        
        final Map hrclassMap = getHrclass(db2, group._code);
        
        final List students = getStudentList(db2, group._code);
        if (students.isEmpty()) {
            return;
        }
        
        final Map courseAttendSubclassCdMap = getCourseAttendSubclassCdMap(db2, group._code);

        final List allsubclasscds = getSubclasscds(db2, group._code, students, courseAttendSubclassCdMap);
        
        final List pageSubclassListList = getPageSubclassListList(allsubclasscds, 20);

        final List pageStudentListList = getPageStudentListList(students);
        final int studentTotalPage = pageStudentListList.size();
        final int totalPage = studentTotalPage * pageSubclassListList.size();

        for (int spi = 0; spi < pageSubclassListList.size(); spi++) {
            final List subclasscds = (List) pageSubclassListList.get(spi);
            final boolean isLastSubclassPage = (spi == pageSubclassListList.size() - 1);
            svf.VrSetForm("KNJD617A_1.frm", 1);
            hasData = printAvg(svf, hrclassMap, subclasscds, isLastSubclassPage);
            printHeader(svf, subclasscds, spi + 1, totalPage);
            if (pageStudentListList.size() > 0) {
                final List pageStudentList = (List) pageStudentListList.get(0);
                printStudents(svf, hrclassMap, courseAttendSubclassCdMap, totalPage, subclasscds, isLastSubclassPage, pageStudentList);
            }
            svf.VrEndPage();
            
            hasData = true;
        }
        
        if (studentTotalPage > 1) {
            for (int pi = 1; pi < studentTotalPage; pi++) {
                for (int spi = 0; spi < pageSubclassListList.size(); spi++) {
                    final List subclasscds = (List) pageSubclassListList.get(spi);
                    final boolean isLastSubclassPage = (spi == pageSubclassListList.size() - 1);
                    svf.VrSetForm("KNJD617A_2.frm", 1);
                    printHeader(svf, subclasscds, pi * pageSubclassListList.size() + spi + 1, totalPage);
                    final List pageStudentList = (List) pageStudentListList.get(pi);
                    printStudents(svf, hrclassMap, courseAttendSubclassCdMap, totalPage, subclasscds, isLastSubclassPage, pageStudentList);
                    svf.VrEndPage();
                    
                    hasData = true;
                }
            }
        }
        
        _hasData = _hasData || hasData;
    }

    private void printStudents(final Vrw32alp svf,
            final Map hrclassMap,
            final Map courseAttendSubclassCdMap,
            final int totalPage,
            final List subclasscds,
            final boolean isLastSubclassPage,
            final List pageStudentList) {
        
        for (int si = 0; si < pageStudentList.size(); si++) {
            final Student student = (Student) pageStudentList.get(si);
            printStudent(svf, hrclassMap, subclasscds, si + 1, student, courseAttendSubclassCdMap, isLastSubclassPage);
        }
    }
    
    private List getPageSubclassListList(final List list, final int count) {
        final List rtn = new ArrayList();
        List current = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Object o = it.next();
            if (current == null || current.size() >= count) {
                current = new ArrayList();
                rtn.add(current);
            }
            current.add(o);
        }
        return rtn;
    }
    
    private Map getCourseAttendSubclassCdMap(final DB2UDB db2, final String selected) {
        final Map map = new HashMap();
        PreparedStatement ps = null;
        ResultSet rs = null;
        final String flg = "9900".equals(_param._testcd) ? "2" : "1";
        try {
            final StringBuffer stb = new StringBuffer();
            stb.append("   SELECT ");
            stb.append("       T1.COURSECD || T1.MAJORCD || T1.COURSECODE AS COURSE, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("       T1.ATTEND_CLASSCD || '-' || T1.ATTEND_SCHOOL_KIND || '-' || T1.ATTEND_CURRICULUM_CD || '-' || ");
            }
            stb.append("       T1.ATTEND_SUBCLASSCD AS ATTEND_SUBCLASSCD ");
            stb.append("   FROM ");
            stb.append("       SUBCLASS_WEIGHTING_COURSE_DAT T1 ");
            stb.append("   WHERE ");
            stb.append("       T1.YEAR = '" + _param._year + "' ");
            stb.append("       AND T1.FLG = '" + flg + "' ");
            stb.append("       AND T1.GRADE = '" + _param._grade + "' ");
            
            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();
            while (rs.next()) {
                if (null == map.get(rs.getString("COURSE"))) {
                    map.put(rs.getString("COURSE"), new ArrayList());
                }
                final List attendSubclassCdList = (List) map.get(rs.getString("COURSE"));
                attendSubclassCdList.add(rs.getString("ATTEND_SUBCLASSCD"));
            }
        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
//        for (final Iterator it = map.keySet().iterator(); it.hasNext();) {
//            final String course = (String) it.next();
//            log.fatal(" course = " + course + ", attend_subclass_cd = " + map.get(course));
//        }
        return map;
    }

    private void printHeader(final Vrw32alp svf, final List subclasscds, final int page, final int totalPage) {

        final String nendo = KenjaProperties.gengou(Integer.parseInt(_param._year)) + "年度";
        final String title = nendo + " " + _param._gradename + " " + _param._semestername + " " + _param._testitemname + "成績順一覧表";
        svf.VrsOut("TITLE", title);
        svf.VrsOut("PAGE_HEADER", String.valueOf(page));
        svf.VrsOut("PAGE_FOOTER", String.valueOf(totalPage));
        svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._date));

        for (int si = 0; si < subclasscds.size(); si++) {
            final String subclasscd = (String) subclasscds.get(si);
            final String subclassname = (String) _param._subclassnames.get(subclasscd);
            svf.VrsOut("SUBCLASS_NAME" + (si + 1), subclassname);
            svf.VrsOut("SUBCLASS_NAME" + (si + 1) + "_2", subclassname);
        }
        svf.VrsOut("TOTAL_NAME", "計");
        svf.VrsOut("AVERAGE_NAME", "平均");
        svf.VrsOut("REMARK_NAME", "");
        svf.VrsOut("TOTAL_NAME_2", "総点");
        svf.VrsOut("AVERAGE_NAME_2", "平均");
        svf.VrsOut("CLASS_RANK_NAME", "組順");
        svf.VrsOut("MALE_FEMALE_RANK_NAME", "男女順");
    }

    private List getSubclasscds(final DB2UDB db2, final String selected, final List students, final Map courseAttendSubclassCdMap) {
        final Map courseSubclassCdMap = new HashMap(); // 表示する科目コード
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getSubclassSql(_param, selected);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                if (null == courseSubclassCdMap.get(rs.getString("COURSE"))) {
                    courseSubclassCdMap.put(rs.getString("COURSE"), new HashSet());
                }
                final Set subclassCdSet = (Set) courseSubclassCdMap.get(rs.getString("COURSE"));
                subclassCdSet.add(rs.getString("SUBCLASSCD"));
            }
        } catch (final Exception e) {
            log.error("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }

        for (final Iterator itst = students.iterator(); itst.hasNext();) {
            final Student student = (Student) itst.next();
            if (null == courseSubclassCdMap.get(student._course)) {
                courseSubclassCdMap.put(student._course, new HashSet());
            }
            final Set subclassCdSet = (Set) courseSubclassCdMap.get(student._course);
            subclassCdSet.addAll(student._ranks.keySet());
        }
        
        for (final Iterator it = courseSubclassCdMap.keySet().iterator(); it.hasNext();) {
            final String course = (String) it.next();
            final Set subclassCdSet = (Set) courseSubclassCdMap.get(course);
            final List attendSubclasscdList = (List) courseAttendSubclassCdMap.get(course);
            if (null != attendSubclasscdList) {
//                final Collection debugcol = new TreeSet(subclassCdSet);
//                debugcol.removeAll(attendSubclasscdList);
//                log.fatal(" course = " + course + ", subclasscd = " + subclassCdSet + " => " + debugcol);
                subclassCdSet.removeAll(attendSubclasscdList);
            }
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
        col.remove(_333333);
        col.remove(_555555);
        col.remove(_999999);
        final List rtn = new ArrayList(col);
        Collections.sort(rtn);
        return rtn;
    }
    
    private String getSubclassSql(final Param param, final String selected) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT DISTINCT ");
        stb.append("     T2.COURSECD || T2.MAJORCD || T2.COURSECODE AS COURSE, ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append("     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
        }
        stb.append("     T1.SUBCLASSCD AS SUBCLASSCD ");
        stb.append(" FROM ");
        stb.append("     RECORD_SCORE_DAT T1 ");
        stb.append("     INNER JOIN SCHREG_REGD_DAT T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("         AND T2.YEAR = T1.YEAR ");
        if ("9".equals(param._semester)) {
            stb.append("     AND T2.SEMESTER = '" + param._ctrlSeme+ "' ");
        } else {
            stb.append("     AND T2.SEMESTER = T1.SEMESTER ");
        }
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND T1.TESTKINDCD || T1.TESTITEMCD = '" + _param._testcd + "' ");
        stb.append("     AND T2.GRADE = '" + _param._grade +"' ");
        
        if (GROUP_GRADE.equals(param._groupdiv)) {
        } else if (GROUP_COURSE.equals(param._groupdiv)) {
            stb.append("     AND T2.COURSECD || T2.MAJORCD || T2.COURSECODE = '" + selected + "' ");
        } else if (GROUP_COURSEGROUP.equals(param._groupdiv)) {
            stb.append("     AND T2.COURSECD || T2.MAJORCD || T2.COURSECODE IN ( ");
            stb.append("        SELECT ");
            stb.append("            T3.COURSECD || T3.MAJORCD || T3.COURSECODE ");
            stb.append("        FROM ");
            stb.append("            COURSE_GROUP_CD_DAT T3 ");
            stb.append("        WHERE ");
            stb.append("            T3.YEAR = '" + _param._year + "' ");
            stb.append("            AND T3.GRADE = '" + _param._grade +"' ");
            stb.append("            AND T3.GROUP_CD = '" + selected + "' ");
            stb.append("     ) ");
        }
        stb.append(" ORDER BY ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append("     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
        }
        stb.append("     T1.SUBCLASSCD ");
        return stb.toString();
    }

    private void printStudent(final Vrw32alp svf, final Map hrclassMap, final List subclasscds, final int si, final Student student,
            final Map courseAttendSubclassCdMap, final boolean isLastSubclassPage) {
        final Integer gradeRank = student.getTotalRank(_param);
        svf.VrsOutn("RANK", si, null == gradeRank ? "" : gradeRank.toString());
        final String hrname = student._hrclass + "-" + (null == student._attendno ? "" : student._attendno);
        svf.VrsOutn("HR_CLASS_NO", si, hrname);
        svf.VrsOutn("IN_STUDENT", si, ("0".equals(student._inoutCd) ? "M" : ""));
        final int nameLength = getMS932ByteCount(student._name);
        final String nameField = "NAME" + (nameLength > 16 ? "3" : nameLength > 12 ? "2" : "");
        svf.VrsOutn(nameField, si, student._name);
        
        for (int subi = 0; subi < subclasscds.size(); subi++) {
            final String subclasscd = (String) subclasscds.get(subi);
            final Rank rank = (Rank) student._ranks.get(subclasscd);
            if (null == rank) {
                continue;
            }
            final List attendSubclassCdList = (List) courseAttendSubclassCdMap.get(student._course);
            if (null != attendSubclassCdList && attendSubclassCdList.contains(rank._subclasscd)) {
                continue;
            }
            svf.VrsOutn("SUBCLASS_SCORE" + (subi + 1), si, rank._score);
        }
        if (isLastSubclassPage) {
            final Rank rank = (Rank) student._ranks.get(_999999);
            if (null != rank) {
                svf.VrsOutn("SUBCLASS_SCORE_TOTAL", si, rank._score);
                svf.VrsOutn("SUBCLASS_SCORE_TOTAL_AVERAGE", si, rank._avg);
                svf.VrsOutn("CLASS_RANK", si, nullBlank(rank._classRank));
                svf.VrsOutn("MALE_FEMALE_RANK", si, nullBlank(student._rankBySex));
            }
        }
    }
    
    private final List getPageStudentListList(final List students) {
        final List list = new ArrayList();
        if (students.size() < COUNT1) {
            list.add(students);
        } else {
            list.add(students.subList(0, COUNT1));
            
            List nokori = students.subList(COUNT1, students.size());
            while (nokori.size() > COUNT2) {
                list.add(nokori.subList(0, COUNT2));
                nokori = nokori.subList(COUNT2, nokori.size());
            }
            list.add(nokori);
        }
        return list;
    }
    
    private List getStudentList(DB2UDB db2, final String selected) {

        final Map studentMap = new HashMap();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = Student.sqlStudent(_param, selected);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final Student student = new Student(rs.getString("SCHREGNO"), rs.getString("NAME"), rs.getString("HR_CLASS"), rs.getString("COURSE"), rs.getString("ATTENDNO"), rs.getString("SEX"), rs.getString("INOUTCD"));
                studentMap.put(rs.getString("SCHREGNO"), student);
            }
            
        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        
        try {
            final String sql = Rank.sqlRank(_param);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                
                final Student student = (Student) studentMap.get(rs.getString("SCHREGNO"));
                if (null == student) {
                    continue;
                }
                final String subclasscd;
                if ("1".equals(_param._useCurriculumcd) && !(_333333.equals(rs.getString("SUBCLASSCD")) || _555555.equals(rs.getString("SUBCLASSCD")) || _999999.equals(rs.getString("SUBCLASSCD")))) {
                    subclasscd = rs.getString("CLASSCD") + '-' + rs.getString("SCHOOL_KIND") + '-' + rs.getString("CURRICULUM_CD") + '-' + rs.getString("SUBCLASSCD");
                } else {
                    subclasscd = rs.getString("SUBCLASSCD");
                }
                final String score = rs.getString("SCORE");
                final String avg = null == rs.getString("AVG") ? null : new BigDecimal(rs.getString("AVG")).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                final Integer gradeRank = null == rs.getString("GRADE_RANK") ? null : Integer.valueOf(rs.getString("GRADE_RANK"));
                final Integer classRank = null == rs.getString("CLASS_RANK") ? null : Integer.valueOf(rs.getString("CLASS_RANK"));
                final Integer courseRank = null == rs.getString("COURSE_RANK") ? null : Integer.valueOf(rs.getString("COURSE_RANK"));
                final Integer courseGroupRank = null == rs.getString("COURSEGROUP_RANK") ? null : Integer.valueOf(rs.getString("COURSEGROUP_RANK"));
                student._ranks.put(subclasscd, new Rank(subclasscd, score, avg, gradeRank, classRank, courseRank, courseGroupRank));
            }
            
        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        
        setRankBySex(studentMap.values());

        final List rtn = new ArrayList();
        rtn.addAll(studentMap.values());
        
        Collections.sort(rtn, new StudentSorter(_param));
        
        return rtn;
    }


    // 性別ごとに順位をセット
    private void setRankBySex(final Collection students) {
        final Set sexes = new HashSet();
        for (final Iterator its = students.iterator(); its.hasNext();) {
            final Student student = (Student) its.next();
            if (null != student._sex) {
                sexes.add(student._sex);
            }
        }
        
        // 性別ごとにリスト
        final Map sexStudentListMap = new HashMap(); // Map<sex, List<Student>>
        for (final Iterator it = students.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            if (null == student._sex) {
                continue;
            }
            if (null == sexStudentListMap.get(student._sex)) {
                sexStudentListMap.put(student._sex, new ArrayList());
            }
            final List studentGroup = (List) sexStudentListMap.get(student._sex);
            studentGroup.add(student);
        }
        
        for (final Iterator itx = sexStudentListMap.values().iterator(); itx.hasNext();) {
            
            final List studentGroup = (List) itx.next();
            
            final Map rankStudentMap = new TreeMap(); // Map<rank, List<Student>>
            final Integer max = new Integer(Integer.MAX_VALUE);

            // 総点順位ごとにリスト
            for (final Iterator its = studentGroup.iterator(); its.hasNext();) {
                final Student student = (Student) its.next();
                Integer rank = student.getTotalRank(_param);
                if (null == rank) {
                    rank = max;
                }
                if (null == rankStudentMap.get(rank)) {
                    rankStudentMap.put(rank, new ArrayList());
                }
                final List rankStudentList = (List) rankStudentMap.get(rank);
                rankStudentList.add(student);
            }
            
            int iRankBySex = 1; // 性別ごとの順位（1位から開始）
            for (final Iterator itr = rankStudentMap.keySet().iterator(); itr.hasNext();) {
                final Integer rank = (Integer) itr.next();
                final List rankStudentList = (List) rankStudentMap.get(rank);
                final Integer rankBySex = new Integer(iRankBySex);
                
                // 同一の総点順位の生徒に同一の性別ごとの順位をセット
                for (final Iterator its = rankStudentList.iterator(); its.hasNext();) {
                    final Student student = (Student) its.next();
                    student._rankBySex = rankBySex;
                }
                iRankBySex += rankStudentList.size();
            }
        }
    }


    private boolean printAvg(final Vrw32alp svf, final Map hrclassMap, final List subclasscds, final boolean isLastSubclassPage) {
        for (final Iterator ith = hrclassMap.keySet().iterator(); ith.hasNext();) {
            final String hrclasscd = (String) ith.next();
            final HrclassAvg hrclass = (HrclassAvg) hrclassMap.get(hrclasscd);
            if (Gakunen.equals(hrclass._hrclass) || hrclass._idx > HR_MAX) {
                continue;
            }
            svf.VrsOutn("HR_NAME", hrclass._idx, hrclass._hrname + " 平均点");
        }
        
        boolean hasData = false;
        for (int si = 0; si < subclasscds.size(); si++) {
            final String subclasscd = (String) subclasscds.get(si);
            
            printHrAverage(svf, hrclassMap, "SUBCLASS_AVERAGE", String.valueOf(si + 1), subclasscd);
            
            hasData = true;
        }
        
        if (isLastSubclassPage) {
            printHrAverage(svf, hrclassMap, "SUBCLASS_TOTAL", "", _999999);

            if ("J".equals(_param._schoolKind)) {
                for (final Iterator ith = hrclassMap.keySet().iterator(); ith.hasNext();) {
                    final String hrclasscd = (String) ith.next();
                    final HrclassAvg hrclass = (HrclassAvg) hrclassMap.get(hrclasscd);
                    if (!Gakunen.equals(hrclass._hrclass) && hrclass._idx > HR_MAX) {
                        continue;
                    }

                    BigDecimal avgtotal = new BigDecimal(0);
                    int subclassCount = 0;
                    for (int si = 0; si < subclasscds.size(); si++) {
                        final String subclasscd = (String) subclasscds.get(si);
                        
                        final Average average = (Average) hrclass._subclassAvg.get(subclasscd);
                        if (null != average && null != average._avgbd) {
                            avgtotal = avgtotal.add(average._avgbd);
                            subclassCount += 1;
                        }
                    }
                    
                    if (0 != subclassCount) {
                        final String avgavg = avgtotal.divide(new BigDecimal(subclassCount), 1, BigDecimal.ROUND_HALF_UP).toString();
                        svf.VrsOutn("SUBCLASS_TOTAL_AVERAGE", hrclass._idx, avgavg);
                    }
                }
            }
        }
        

        return hasData;
    }

    private void printHrAverage(final Vrw32alp svf, final Map hrclassMap, final String field, final String si, final String subclasscd) {
        for (final Iterator ith = hrclassMap.keySet().iterator(); ith.hasNext();) {
            final String hrclasscd = (String) ith.next();
            final HrclassAvg hrclass = (HrclassAvg) hrclassMap.get(hrclasscd);
            if (Gakunen.equals(hrclass._hrclass) || hrclass._idx > HR_MAX) {
                continue;
            }
            final Average average = (Average) hrclass._subclassAvg.get(subclasscd);
            if (null != average) {
                svf.VrsOutn(field + si, hrclass._idx, average._avg);
            }
        }
        final HrclassAvg gakunen = (HrclassAvg) hrclassMap.get(Gakunen);
        final Average averageGakunen = (Average) gakunen._subclassAvg.get(subclasscd);
        if (null != averageGakunen) {
            svf.VrsOutn(field + si, HR_MAX + 1, averageGakunen._avg);        // 平均点
            svf.VrsOutn(field + si, HR_MAX + 2, averageGakunen._stddev);     // 標準偏差
            svf.VrsOutn(field + si, HR_MAX + 3, averageGakunen._highscore);  // 最高点
            svf.VrsOutn(field + si, HR_MAX + 4, averageGakunen._lowscore);   // 最低点
        }
    }
    
    
    private Map getHrclass(final DB2UDB db2, final String selected) {
        final Map rtn = new TreeMap();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = HrclassAvg.sqlHrclass(_param, selected);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            int i = 1;
            while (rs.next()) {
                rtn.put(rs.getString("HR_CLASS"), new HrclassAvg(rs.getString("HR_CLASS"), rs.getString("HR_NAME"), i));
                i++;
            }
            
        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        final HrclassAvg gakunen = new HrclassAvg(Gakunen, "GAKUNEN", 7);
        rtn.put(gakunen._hrclass, gakunen);
        
        try {
            final String sql = Average.sqlAverage(_param, selected);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                
                HrclassAvg hrclass = null;
                final String avgdiv = rs.getString("AVG_DIV");
                if ("1".equals(avgdiv)) {
                    hrclass = gakunen;
//                } else if (GROUP_COURSEGROUP.equals(_param._groupdiv)) {
//                    if ("5".equals(avgdiv)) { // コースグループ順位
//                        hrclass = (HrclassAvg) rtn.get(rs.getString("HR_CLASS"));
//                    }
                } else {
                    if ("2".equals(avgdiv)) {
                        hrclass = (HrclassAvg) rtn.get(rs.getString("HR_CLASS"));
                    }
                }
                if (null == hrclass) {
                    continue;
                }
                
                final String subclasscd;
                if ("1".equals(_param._useCurriculumcd) && !(_333333.equals(rs.getString("SUBCLASSCD")) || _555555.equals(rs.getString("SUBCLASSCD")) || _999999.equals(rs.getString("SUBCLASSCD")))) {
                    subclasscd = rs.getString("CLASSCD") + '-' + rs.getString("SCHOOL_KIND") + '-' + rs.getString("CURRICULUM_CD") + '-' + rs.getString("SUBCLASSCD");
                } else {
                    subclasscd = rs.getString("SUBCLASSCD");
                }
                final String score = rs.getString("SCORE");
                final String highscore = rs.getString("HIGHSCORE");
                final String lowscore = rs.getString("LOWSCORE");
                final String count = rs.getString("COUNT");
                final BigDecimal avgbd = rs.getBigDecimal("AVG");
                final String avg = null == rs.getString("AVG") ? null : new BigDecimal(rs.getString("AVG")).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                final String stddev = rs.getString("STDDEV");

                hrclass._subclassAvg.put(subclasscd, new Average(subclasscd, score, highscore, lowscore, count, avg, avgbd, stddev));
            }
            
        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return rtn;
    }

    private static class HrclassAvg {
        final String _hrclass;
        final String _hrname;
        final int _idx;
        final Map _subclassAvg = new HashMap();
        HrclassAvg(final String hrclass, final String hrname, final int idx) {
            _hrclass = hrclass;
            _hrname = hrname;
            _idx = idx;
        }
        
        
        private static String sqlHrclass(final Param param, final String selected) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT DISTINCT ");
            stb.append("     T1.GRADE, ");
            stb.append("     T1.HR_CLASS, ");
            stb.append("     T1.HR_NAME ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_HDAT T1 ");
            stb.append("     INNER JOIN SCHREG_REGD_DAT T2 ON T2.YEAR = T1.YEAR ");
            stb.append("       AND T2.SEMESTER = T1.SEMESTER ");
            stb.append("       AND T2.GRADE = T1.GRADE ");
            stb.append("       AND T2.HR_CLASS = T1.HR_CLASS ");
            if (GROUP_COURSEGROUP.equals(param._groupdiv)) {
                stb.append("     INNER JOIN COURSE_GROUP_CD_DAT T3 ON T3.YEAR = T1.YEAR ");
                stb.append("       AND T3.GRADE = T1.GRADE ");
                stb.append("       AND T3.COURSECD || T3.MAJORCD || T3.COURSECODE = T2.COURSECD || T2.MAJORCD || T2.COURSECODE ");
            }
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            if ("9".equals(param._semester)) {
                stb.append("     AND T1.SEMESTER = '" + param._ctrlSeme+ "' ");
            } else {
                stb.append("     AND T1.SEMESTER = '" + param._semester + "' ");
            }
            stb.append("     AND T1.GRADE = '" + param._grade + "' ");
            if (GROUP_GRADE.equals(param._groupdiv)) {
            } else if (GROUP_COURSE.equals(param._groupdiv)) {
                stb.append("     AND T2.COURSECD || T2.MAJORCD || T2.COURSECODE = '" + selected + "' ");
            } else if (GROUP_COURSEGROUP.equals(param._groupdiv)) {
                stb.append("     AND T3.GROUP_CD = '" + selected + "' ");
            }
            stb.append(" ORDER BY ");
            stb.append("     T1.GRADE, T1.HR_CLASS ");
            return stb.toString();
        }
    }

    private static class Student {
        final String _schregno;
        final String _hrclass;
        final String _course;
        final String _attendno;
        final String _name;
        final String _sex;
        final String _inoutCd;
        Integer _rankBySex = null;
        final Map _ranks = new HashMap();
        public Student(final String schregno, final String name, final String hrclass, final String course, final String attendno, final String sex, final String inoutCd) {
            _schregno = schregno;
            _name = name;
            _hrclass = hrclass;
            _course = course;
            _attendno = attendno;
            _sex = sex;
            _inoutCd = inoutCd;
        }
        /**
         * 総点の順位
         */
        public Integer getTotalRank(final Param param) {
            final Rank rank = (Rank) _ranks.get(_999999);
            if (null == rank) {
                return null;
            }
            if (GROUP_GRADE.equals(param._groupdiv)) {
                return rank._gradeRank;
            } else if (GROUP_COURSE.equals(param._groupdiv)) {
                return rank._courseRank;
            } else if (GROUP_COURSEGROUP.equals(param._groupdiv)) {
                return rank._courseGroupRank;
            }
            return null;
        }
        
        private static String sqlStudent(final Param param, final String selected) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T2.NAME, ");
            stb.append("     T1.GRADE, ");
            stb.append("     T1.HR_CLASS, ");
            stb.append("     T1.COURSECD || T1.MAJORCD || T1.COURSECODE AS COURSE, ");
            stb.append("     T1.ATTENDNO, ");
            stb.append("     T2.SEX, ");
            stb.append("     T2.INOUTCD ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT T1 ");
            stb.append("     INNER JOIN  SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            if (GROUP_COURSEGROUP.equals(param._groupdiv)) {
                stb.append("     INNER JOIN COURSE_GROUP_CD_DAT T3 ON T3.YEAR = T1.YEAR ");
                stb.append("       AND T3.GRADE = T1.GRADE ");
                stb.append("       AND T3.COURSECD || T3.MAJORCD || T3.COURSECODE = T1.COURSECD || T1.MAJORCD || T1.COURSECODE ");
            }
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            if ("9".equals(param._semester)) {
                stb.append("     AND T1.SEMESTER = '" + param._ctrlSeme+ "' ");
            } else {
                stb.append("     AND T1.SEMESTER = '" + param._semester + "' ");
            }
            stb.append("     AND T1.GRADE = '" + param._grade + "' ");
            if (GROUP_GRADE.equals(param._groupdiv)) {
            } else if (GROUP_COURSE.equals(param._groupdiv)) {
                stb.append("     AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE = '" + selected + "' ");
            } else if (GROUP_COURSEGROUP.equals(param._groupdiv)) {
                stb.append("     AND T3.GROUP_CD = '" + selected + "' ");
            }
            return stb.toString();
        }
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
            return 0 == cmp || SORT_DIV_ATTENDNO.equals(_param._sortdiv) ? (s1._hrclass + s1._attendno).compareTo(s2._hrclass + s2._attendno) : cmp;
        }
    }
    
    private static class Average {
        final String _subclasscd;
        final String _score;
        final String _highscore;
        final String _lowscore;
        final String _count;
        final String _avg;
        final BigDecimal _avgbd;
        final String _stddev;
        public Average(final String subclasscd, final String score, final String highscore, final String lowscore, final String count, final String avg, final BigDecimal avgbd, final String stddev) {
            _subclasscd = subclasscd;
            _score = score;
            _highscore = highscore;
            _lowscore = lowscore;
            _count = count;
            _avg = avg;
            _avgbd = avgbd;
            _stddev = stddev;
        }
        
        private static String sqlAverage(final Param param, final String selected) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.AVG_DIV, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     T1.CLASSCD, ");
                stb.append("     T1.SCHOOL_KIND, ");
                stb.append("     T1.CURRICULUM_CD, ");
            }
            stb.append("     T1.SUBCLASSCD, ");
            stb.append("     T1.GRADE, ");
            stb.append("     T1.HR_CLASS, ");
            stb.append("     T1.SCORE, ");
            stb.append("     T1.HIGHSCORE, ");
            stb.append("     T1.LOWSCORE, ");
            stb.append("     T1.COUNT, ");
            stb.append("     T1.AVG, ");
            stb.append("     T1.STDDEV ");
            stb.append(" FROM ");
            stb.append("  RECORD_AVERAGE_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.SEMESTER = '" + param._semester + "' ");
            stb.append("     AND T1.GRADE = '" + param._grade + "' ");
            stb.append("     AND T1.AVG_DIV IN ('1', '2', '5') "); // 学科、クラス、コースグループ
            stb.append("     AND T1.TESTKINDCD || T1.TESTITEMCD = '" + param._testcd + "' ");
            stb.append(" ORDER BY ");
            stb.append("     T1.AVG_DIV, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     T1.CLASSCD, ");
                stb.append("     T1.SCHOOL_KIND, ");
                stb.append("     T1.CURRICULUM_CD, ");
            }
            stb.append("     T1.SUBCLASSCD, ");
            stb.append("     T1.HR_CLASS ");
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
        
        private static String sqlRank(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     T2.CLASSCD, ");
                stb.append("     T2.SCHOOL_KIND, ");
                stb.append("     T2.CURRICULUM_CD, ");
            }
            stb.append("     T2.SUBCLASSCD, ");
            stb.append("     T2.SCORE, ");
            stb.append("     T2.AVG, ");
            stb.append("     T2.GRADE_RANK, ");
            stb.append("     T2.CLASS_RANK, ");
            stb.append("     T2.COURSE_RANK, ");
            stb.append("     T2.MAJOR_RANK AS COURSEGROUP_RANK ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT T1 ");
            stb.append("     LEFT JOIN RECORD_RANK_DAT T2 ON T2.YEAR = T1.YEAR ");
            stb.append("         AND T2.SEMESTER = '" + param._semester + "' ");
            stb.append("         AND T2.SCHREGNO = T1.SCHREGNO ");
            stb.append(" WHERE ");
            stb.append("     T1.GRADE = '" + param._grade + "' ");
            stb.append("     AND T1.YEAR = '" + param._year + "' ");
            if ("9".equals(param._semester)) {
                stb.append("     AND T1.SEMESTER = '" + param._ctrlSeme+ "' ");
            } else {
                stb.append("     AND T1.SEMESTER = '" + param._semester + "' ");
            }
            stb.append("     AND T2.TESTKINDCD || T2.TESTITEMCD = '" + param._testcd + "' ");
            stb.append(" ORDER BY ");
            stb.append("     T1.SCHREGNO, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     T2.CLASSCD, ");
                stb.append("     T2.SCHOOL_KIND, ");
                stb.append("     T2.CURRICULUM_CD, ");
            }
            stb.append("     T2.SUBCLASSCD ");
            return stb.toString();
        }
    }
    
    private static class Group {
        final String _code;
        Group(final String code) {
            _code = code;
        }
        
        public String toString() {
            return "Group(" + _code + ")";
        }
        
        private static List getGroupList(final DB2UDB db2, final Param param) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = sqlGroup(param);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String code = rs.getString("GROUP");
                    if (null == code) {
                        continue;
                    }
                    list.add(new Group(code));
                }
                
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }
        
        private static String sqlGroup(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT DISTINCT ");
            if (GROUP_GRADE.equals(param._groupdiv)) {
                stb.append("     T1.GRADE AS GROUP ");
            } else if (GROUP_COURSE.equals(param._groupdiv)) {
                stb.append("     T1.COURSECD || T1.MAJORCD || T1.COURSECODE AS GROUP ");
            } else if (GROUP_COURSEGROUP.equals(param._groupdiv)) {
                stb.append("     T2.GROUP_CD AS GROUP ");
            }
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT T1 ");
            stb.append("     LEFT JOIN COURSE_GROUP_CD_DAT T2 ON T2.YEAR = T1.YEAR AND ");
            stb.append("         T2.GRADE = T1.GRADE AND ");
            stb.append("         T2.COURSECD = T1.COURSECD AND ");
            stb.append("         T2.MAJORCD = T1.MAJORCD AND ");
            stb.append("         T2.COURSECODE = T1.COURSECODE ");
            stb.append(" WHERE ");
            stb.append("     T1.GRADE = '" + param._grade + "' ");
            stb.append("     AND T1.YEAR = '" + param._year + "' ");
            if ("9".equals(param._semester)) {
                stb.append("     AND T1.SEMESTER = '" + param._ctrlSeme+ "' ");
            } else {
                stb.append("     AND T1.SEMESTER = '" + param._semester + "' ");
            }
            stb.append(" ORDER BY ");
            stb.append("     GROUP ");
            return stb.toString();
        }
    }
    
    private Param createParam(final HttpServletRequest request, final DB2UDB db2) {
        KNJServletUtils.debugParam(request, log);
        return new Param(request, db2);
    }

    private class Param {
        final String _year;
        final String _semester;
        final String _ctrlSeme;
        final String _grade;
        final String _testcd;
        final String _date;
        final Map _subclassnames;
        final String _groupdiv;
        final String _sortdiv;
        final String _useCurriculumcd;
        String _testitemname = "";
        String _semestername = "";
        String _gradename = "";
        final String _schoolKind;

        Param(final HttpServletRequest request, final DB2UDB db2) {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _ctrlSeme = request.getParameter("CTRL_SEME");
            _grade = request.getParameter("GRADE");
            _testcd = request.getParameter("TESTCD");
            _date = request.getParameter("DATE");
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _subclassnames = getSubclassname(db2);
            setName(db2);
            _groupdiv = request.getParameter("GROUP_DIV");
            _sortdiv = request.getParameter("SORT_DIV");
            _schoolKind = getSchoolKind(db2);
        }
        
        private void setName(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT TESTITEMNAME FROM TESTITEM_MST_COUNTFLG_NEW WHERE YEAR = '" + _year + "' AND SEMESTER = '" + _semester + "' AND TESTKINDCD || TESTITEMCD = '" + _testcd + "' ");
                rs = ps.executeQuery();
                if (rs.next()) {
                    _testitemname = nullBlank(rs.getString("TESTITEMNAME"));
                }
                
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }

            try {
                ps = db2.prepareStatement(" SELECT SEMESTERNAME FROM SEMESTER_MST WHERE SEMESTER = '" + _semester + "' ");
                rs = ps.executeQuery();
                if (rs.next()) {
                    _semestername = nullBlank(rs.getString("SEMESTERNAME"));
                }
                
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }

            try {
                ps = db2.prepareStatement(" SELECT GRADE_NAME1 FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND GRADE ='" + _grade + "' ");
                rs = ps.executeQuery();
                if (rs.next()) {
                    _gradename = nullBlank(rs.getString("GRADE_NAME1"));
                }
                
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
        
        private String getSchoolKind(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            String rtn = "";
            try {
                final String sql = "SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND GRADE = '" + _grade + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString("SCHOOL_KIND");
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }

        private Map getSubclassname(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map rtn = new HashMap();
            try {
                final String sql = sqlSubclass();
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn.put(rs.getString("SUBCLASSCD"), rs.getString("SUBCLASSNAME"));
                }
                
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }

        private String sqlSubclass() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append("     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || ");
            }
            stb.append("     SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("     SUBCLASSNAME ");
            stb.append(" FROM ");
            stb.append("     SUBCLASS_MST ");
            return stb.toString();
        }
    }
}
