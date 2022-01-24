/*
 * $Id: bbce1eae2a1c4b41d4e2ddf20d65ed9855ef66dc $
 *
 * 作成日: 2009/10/17
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJB;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 *  学校教育システム 賢者 [時間割・履修管理]
 *
 *                  ＜ＫＮＪＢ１００＞  開講科目一覧表
 */
public class KNJB1300 {

    private static final Log log = LogFactory.getLog(KNJB1300.class);

    private static final String CHAIRCD0000000 = "0000000"; // 講座クラスデータで群に属する講座の講座コード

    private static final String GUN0CD000 = "0000"; // 群に属しない講座の群コード

    private static final String GRADE_ALL = "99"; // 学年全て

    private final String SELECT_TABLE_SUBCLASS_STD_DAT = "2"; // 科目履修名簿を参照する

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

    private int getMS932ByteCount(final String str) {
        int ret = 0;
        if (null != str) {
            try {
                ret = str.getBytes("MS932").length;
            } catch (Exception e) {
            }
        }
        return ret;
    }

    private int getLength(final String str) {
        int ret = 0;
        if (null != str) {
            try {
                ret = str.length();
            } catch (Exception e) {
            }
        }
        return ret;
    }

    private static Set getMappedSet(final Map m, final Object key) {
        if (null == m.get(key)) {
            m.put(key, new HashSet());
        }
        return (Set) m.get(key);
    }

    private static Map getMappedMap(final Map m, final Object key) {
        if (null == m.get(key)) {
            m.put(key, new HashMap());
        }
        return (Map) m.get(key);
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        Clazz.load(db2, _param, _param.clazzInstances, _param.subclassInstances);
        Student.load(db2, _param, _param.studentInstances);
        Student.setCourse(_param.studentInstances, _param.courseInstances);
        Course.loadMajorNames(db2, _param.majorNames);
        CreditMst.load(db2, _param, _param.courseInstances, _param.subclassInstances, _param.creditMstInstances, _param.creditMstRequireNames);
        Chair.load(db2, _param, _param.chairInstances, _param.subclassInstances, _param.studentInstances);
        if (SELECT_TABLE_SUBCLASS_STD_DAT.equals(_param._selectTable)) {
            SubclassCompSelectMst.load(db2, _param, _param.subclassCompSelectMstInstances, _param.studentInstances, _param.subclassInstances);
        }

        int ret;
        String title;
        ret = svf.VrSetForm("KNJB1300.frm", 4);
        title = "開講科目・" + (SELECT_TABLE_SUBCLASS_STD_DAT.equals(_param._selectTable) ? "履修" : "講座") + "生徒集計一覧表";
        if (ret == -30) {
            svf.VrSetForm("KNJB100.frm", 4);
            title = "";
        }
        svf.VrsOut("NENDO", KNJ_EditDate.h_format_JP_N(_param._year + "-01-01") + "度　" + title);
        svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._loginDate));
        svf.VrsOut("TIME", _param._loginHour + "時" + _param._loginMinutes + "分");
        svf.VrsOut("GRADE", _param._gradeName);
        svf.VrsOut("STAFFNAME_SHOW", _param._loginStaffName);
        svf.VrsOut("SCHOOLNAME", _param._schoolName);
        svf.VrsOut("NOTICE", _param._requireInfo);
        svf.VrsOut("SELECT", SELECT_TABLE_SUBCLASS_STD_DAT.equals(_param._selectTable) ? "履修登録名簿" : "講座名簿");

        log.fatal(" table = " + _param._selectTable);
        final List printLines;
        if (SELECT_TABLE_SUBCLASS_STD_DAT.equals(_param._selectTable)) {
            // 履修登録名簿
            printLines = getPrintLineSubclassStdSelectDat();
        } else {
            // 講座名簿
            printLines = getPrintLineChairStdDat();
        }
        final DecimalFormat df = new DecimalFormat("00");
        int grp = 0;
        for (final Iterator it = PrintLine.groupBySubclass(printLines).iterator(); it.hasNext();) {
            final List subclassPrintLines = (List) it.next();

            final int printSubclasscdLine = 0; // Math.max(0, subclassPrintLines.size() / 2 - 1);
            final int printSubclassnameLine = subclassPrintLines.size() > 1 ? 1 : 0; // subclassPrintLines.size() / 2;
            grp += 1;
            final String subGrp = df.format(grp);

            final List groupByCourseList = PrintLine.groupByCourse(subclassPrintLines);
            int k = 0;
            int cgrp = 0;
            for (int j = 0; j < groupByCourseList.size(); j++) {
                final List subclassCoursePrintLines = (List) groupByCourseList.get(j);
                cgrp += 1;
                final String courseGrp = df.format(cgrp);
                for (int i = 0, max = subclassCoursePrintLines.size(); i < max; i++) {
                    final PrintLine pl = (PrintLine) subclassCoursePrintLines.get(i);
                    final boolean isPrintCourseName = i == 0;
                    printLine(svf, subGrp, courseGrp, printSubclasscdLine == k, printSubclassnameLine == k, isPrintCourseName, pl, db2);
                    svf.VrEndRecord();
                    k += 1;
                }
            }
            _hasData = true;
        }
    }

    /**
     * 1行表示する
     * @param svf
     * @param pl
     */
    public void printLine(final Vrw32alp svf, final String subGrp, final String courseGrp, final boolean isPrintSubclasscd, final boolean isPrintSubclassname, final boolean isPrintCourseName, final PrintLine pl, final DB2UDB db2) throws SQLException {

        if (getMS932ByteCount(pl._subclass.clazz._classname) > 16) {
            svf.VrsOut("CLASS_NAME_2", pl._subclass.clazz._classname);
            svfVrAttribute(svf, "CLASS_NAME_1", pl._subclass.clazz._classname.substring(5));
        } else {
            svf.VrsOut("CLASS_NAME_1", pl._subclass.clazz._classname);
        }

        svf.VrsOut("SUB_GRP", subGrp);

        if (isPrintSubclasscd && isPrintSubclassname) {
            svf.VrsOut("SUBCLASS_CD", pl._subclass._subclasscd);
            if (getMS932ByteCount(pl._subclass._subclassname) > 20) {
                svf.VrsOut("SUBCLASS_3_2", pl._subclass._subclassname);
                svfVrAttribute(svf, "SUBCLASS_3_1", pl._subclass._subclassname.substring(5));
            } else {
                svf.VrsOut("SUBCLASS_3_1", pl._subclass._subclassname);
            }
        } else if (isPrintSubclasscd) {
            svf.VrsOut("SUBCLASS_2", pl._subclass._subclasscd);
        } else if (isPrintSubclassname) {
            if (getMS932ByteCount(pl._subclass._subclassname) > 20) {
                svf.VrsOut("SUBCLASS_2", pl._subclass._subclassname);
                svfVrAttribute(svf, "SUBCLASS_1", pl._subclass._subclassname.substring(5));
            } else {
                svf.VrsOut("SUBCLASS_1", pl._subclass._subclassname);
            }
        }

        if (null == pl._stdCount || pl._stdCount.intValue() <= 0) {
            svf.VrsOut("ENROLL_MALE", null);
            svf.VrsOut("ENROLL_FEMALE", null);
            svf.VrsOut("ENROLL_NUM", null);
        } else {
            final Map groupBySex = Student.groupBySex(pl._subClassStudent);
            svf.VrsOut("ENROLL_MALE", getCollectionString(getMappedSet(groupBySex, "1"), null));
            svf.VrsOut("ENROLL_FEMALE", getCollectionString(getMappedSet(groupBySex, "2"), null));
            svf.VrsOut("ENROLL_NUM", String.valueOf(pl._subClassStudent.size()));
        }

        final CreditMst creditMst = CreditMst.getCreditMst(_param.creditMstInstances, pl._subclass, pl._course);
        if (null == creditMst) {
            svfVrAttribute(svf, "CREDIT", null);
            svfVrAttribute(svf, "SEL_SUBCLASS", null);
        } else {
            svf.VrsOut("CREDIT", creditMst._credit);
            svf.VrsOut("SEL_SUBCLASS", _param.requireName(creditMst._requireFlg));
        }
        svf.VrsOut("COURSE_GRP", courseGrp);

//        log.debug(" course = " + pl._course + " courseGrp = " + courseGrp + ", chair = " + pl._chair);
        if (isPrintCourseName) {
            if (null == pl._course) {
                svfVrAttribute(svf, "COURSE_NAME_2", null);
                svfVrAttribute(svf, "COURSE_NAME_1", null);
            } else {
                final String majorName = _param.majorName(pl._course._coursecd,  pl._course._majorcd);
                svf.VrsOut(getLength(majorName) > 6 ? "COURSE_NAME_2" : "COURSE_NAME_1", majorName);
            }
        }
        if (null == pl._chair) {
            svf.VrsOut("CHAIR_NAME1", null);
            svf.VrsOut("CHAIR_NAME2", null);
        } else {
            final String staffSql = getStaffSql(_param, pl._chair._chaircd);
            PreparedStatement ps = null;
            ResultSet rs = null;
            String setStaffName1 = "";
            String setStaffName2 = "";
            int staffCnt = 1;
            boolean isPutCnt2 = false;
            try {
                ps = db2.prepareStatement(staffSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String setStaffName = rs.getString("STAFFNAME");
                    if (null != setStaffName) {
                        String name[] = StringUtils.split(setStaffName, "　");
                        if (null != name && name.length > 0 && staffCnt == 1 && null != name[0]) {
                            setStaffName1 = name[0];
                        }
                        if (null != name && name.length > 0 && staffCnt == 2 && null != name[0]) {
                            setStaffName2 = name[0];
                        }
                    }
                    if (staffCnt == 2) {
                        isPutCnt2 = true;
                        break;
                    }
                    staffCnt++;
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            svf.VrsOut("CHAIR_CD", pl._chair._chaircd);
            svf.VrsOut(getLength(pl._chair._chairname) > 10 ? "CHAIR_NAME2" : "CHAIR_NAME1", pl._chair._chairname);
            if (isPutCnt2) {
                svf.VrsOut("TEACHER2_1", setStaffName1);
                svf.VrsOut("TEACHER2_2", setStaffName2);
            } else {
                svf.VrsOut("TEACHER1", setStaffName1);
            }
        }
        if (null == pl._chairstudent || pl._chairstudent.size() <= 0) {
            svf.VrsOut("CHAIR_MALE", null);
            svf.VrsOut("CHAIR_FEMALE", null);
            svf.VrsOut("CHAIR_NUM", null);
        } else {
            final Map groupBySex = Student.groupBySex(pl._chairstudent);
            svf.VrsOut("CHAIR_MALE", getCollectionString(getMappedSet(groupBySex, "1"), null));
            svf.VrsOut("CHAIR_FEMALE", getCollectionString(getMappedSet(groupBySex, "2"), null));
            svf.VrsOut("CHAIR_NUM", String.valueOf(pl._chairstudent.size()));
        }
    }

    private static String getCollectionString(final Collection col, final String alt) {
        return (null == col || col.size() == 0) ? alt : String.valueOf(col.size());
    }

    /**
     * @return
     */
    private static String getStaffSql(final Param param, final String chairCd) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.STAFFCD, ");
        stb.append("     L1.STAFFNAME ");
        stb.append(" FROM ");
        stb.append("     CHAIR_STF_DAT T1 ");
        stb.append("     LEFT JOIN STAFF_MST L1 ON T1.STAFFCD = L1.STAFFCD ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + param._year + "' ");
        stb.append("     AND T1.SEMESTER = '" + param._semester + "' ");
        stb.append("     AND T1.CHAIRCD = '" + chairCd + "' ");
        stb.append(" GROUP BY ");
        stb.append("     T1.STAFFCD, ");
        stb.append("     L1.STAFFNAME ");
        stb.append(" ORDER BY ");
        stb.append("     T1.STAFFCD ");

        return stb.toString();
    }

    private static void svfVrAttribute(final Vrw32alp svf, final String field, final String data) {
        svf.VrsOut(field, null == data ? "DUMMY" : data);
        svf.VrAttribute(field, "X=10000");
    }

    private List getPrintLineSubclassStdSelectDat() {
        final List line = new ArrayList();
        for (final Iterator itscsm = _param.subclassCompSelectMstInstances.iterator(); itscsm.hasNext();) {
            final SubclassCompSelectMst scsm = (SubclassCompSelectMst) itscsm.next();

            if (scsm._selectDatList.size() == 0) { // 科目の履修登録名簿が未設定（履修生徒が空）
                if (0 == scsm._subclass._chairs.size()) {
                    line.add(new PrintLine(scsm._subclass, null, null, null, null, null));
                } else {
                    for (final Iterator itc = scsm._subclass._chairs.iterator(); itc.hasNext();) {
                        final Chair chair = (Chair) itc.next();
                        line.add(new PrintLine(scsm._subclass, null, null, chair, null, null));
                    }
                }
            } else if (scsm._subclass.allChairStudents().isEmpty()) { // 科目の講座名簿が未設定（講座名簿が空）
                if (0 == scsm._subclass._chairs.size()) {
                    // 講座の数が0の場合（講座が作成されていない場合）、教科、科目、履修登録人数を１行表示
                    line.add(new PrintLine(scsm._subclass, toInt(scsm.totalStudentSize()), null, null, null, scsm.totalStudent()));
                } else {
                    // 講座の数が0でない場合（講座が作成されている場合）、教科、科目、履修登録人数、講座を講座の数行表示
                    for (final Iterator itc = scsm._subclass._chairs.iterator(); itc.hasNext();) {
                        final Chair chair = (Chair) itc.next();
                        line.add(new PrintLine(scsm._subclass, toInt(scsm.totalStudentSize()), null, chair, null, scsm.totalStudent()));
                    }
                }

            } else {
                // 科目の講座生徒名簿が空ではない
                for (final Iterator itscsd = scsm._selectDatList.iterator(); itscsd.hasNext();) {
                    final SubclassCompSelectDat scsd = (SubclassCompSelectDat) itscsd.next();

                    final Collection allChairs = new TreeSet(scsm._subclass._chairs);
                    log.fatal(" !! 1 = " + allChairs + " , 2 = " + scsm._subclass._chairs);
                    final Collection students = new TreeSet(scsd._students);

//                    // コースの履修生徒と講座名簿のリストが一致する複数講座がある場合先に表示する
//                    final Map scsdCourseStudentMap = KNJB100.getCourseStudentMap(scsd._students);
//                    for (final Iterator itcr = scsdCourseStudentMap.keySet().iterator(); itcr.hasNext();) {
//                        final Course stcourse = (Course) itcr.next();
//                        final Collection costudents = (Collection) scsdCourseStudentMap.get(stcourse);
//
//                        if (SubclassCompSelectDat.allStudentsAreChairStudent(costudents, allChairs)) { // コースの履修生徒と講座名簿のリストが一致する複数講座がある
//                            final Collection displayChairs = SubclassCompSelectDat.getDisplayChair(costudents, scsm._subclass._chairs);
//                            for (final Iterator itc = displayChairs.iterator(); itc.hasNext();) {
//                                final Chair chair = (Chair) itc.next();
//
//                                for (final Iterator itr = chair.getStudentCourses().iterator(); itr.hasNext();) {
//                                    final Course course = (Course) itr.next();
//                                    line.add(new PrintLine(scsm._subclass, toInt(scsd._students.size()), course, chair, toInt(chair.getStudent(course).size())));
//                                }
//                            }
//                            allChairs.removeAll(displayChairs); // 一致した講座をのこりの表示対象から除く
////                          for (final Iterator it = displayChairs.iterator(); it.hasNext();) {
////                              final Chair chair = (Chair) it.next();
////                              students.removeAll(chair._students);
////                          }
//                            displayChairs.clear();
//                        }
//                    }

                    final Map courseStudentMap = Student.groupByCourse(students);

                    // 履修生徒のうち講座名簿に完全には一致しないコースの生徒
                    for (final Iterator itc = courseStudentMap.keySet().iterator(); itc.hasNext();) {
                        final Course course = (Course) itc.next();
                        final Collection courseStudents = (Collection) courseStudentMap.get(course);

                        for (final Iterator itch = allChairs.iterator(); itch.hasNext();) {
                            final Chair chair = (Chair) itch.next();
                            final Collection chairStudent = chair.getStudent(course);
                            if (0 != chairStudent.size()) {
                                line.add(new PrintLine(scsm._subclass, toInt(courseStudents.size()), course, chair, chairStudent, courseStudents));
                            }
                        }
                    }
                }
            }
        }
        return line;
    }

    private Integer toInt(final int i) {
        return new Integer(i);
    }

    private List getPrintLineChairStdDat() {
        final List line = new ArrayList();
        // 講座ごとに表示
        for (final Iterator itch = _param.chairInstances.iterator(); itch.hasNext();) {
            final Chair chair = (Chair) itch.next();
            
            final Set totalStudent = new HashSet(); // 科目全体の生徒
            final Map subclassChairStudent = new HashMap(); // 科目全体のコースごとの生徒
            for (final Iterator its = chair._subclass._chairs.iterator(); its.hasNext();) {
                final Chair subchair = (Chair) its.next();
                // 講座名簿の生徒数が0ではない場合、講座名簿の生徒のコースごとに表示
                for (final Iterator itcr = subchair.getStudentCourses().iterator(); itcr.hasNext();) {
                    final Course course = (Course) itcr.next();
                    getMappedSet(subclassChairStudent, course).addAll(subchair.getStudent(course));
                }
                totalStudent.addAll(chair._students);
            }
            if (0 == chair._students.size()) {
                // 講座名簿の生徒数が0の場合、講座のみ1行表示
                line.add(new PrintLine(chair._subclass, toInt(totalStudent.size()), null, chair, chair._students, null));
            } else {
                // 講座名簿の生徒数が0ではない場合、講座名簿の生徒のコースごとに表示
                for (final Iterator itcr = chair.getStudentCourses().iterator(); itcr.hasNext();) {
                    final Course course = (Course) itcr.next();
                    final Set courseChairStudent = (Set) subclassChairStudent.get(course);
                    line.add(new PrintLine(chair._subclass, toInt(null == courseChairStudent ? 0 : courseChairStudent.size()), course, chair, chair.getStudent(course), courseChairStudent));
                }
            }
        }
        return line;
    }

    private static List querySql(final DB2UDB db2, final String sql) {
        final List rtn = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
//            log.debug(" sql = " + sql);
            rs = ps.executeQuery();
            final ResultSetMetaData meta = rs.getMetaData();
            while (rs.next()) {
                final Map m = new HashMap();
                for (int i = 1; i <= meta.getColumnCount(); i++) {
                    final String field = meta.getColumnName(i);
                    final String value = rs.getString(field);
                    m.put(field, value);
                }
                rtn.add(m);
            }
        } catch (SQLException e) {
            log.error("exception! sql = " + sql, e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return rtn;
    }

    private static String getClasscd(final Map m, final Param param) {
        final String classCd;
        if ("1".equals(param._useCurriculumcd)) {
            classCd = getMapValue(m, "CLASSCD") + "-" + getMapValue(m, "SCHOOL_KIND");
        } else {
            classCd = getMapValue(m, "CLASSCD");
        }
        return classCd;
    }

    private static String getSubclasscd(final Map m, final Param param) {
        final String subclassCd;
        if ("1".equals(param._useCurriculumcd)) {
            subclassCd = getMapValue(m, "CLASSCD") + "-" + getMapValue(m, "SCHOOL_KIND") + "-" + getMapValue(m, "CURRICULUM_CD") + "-" + getMapValue(m, "SUBCLASSCD");
        } else {
            subclassCd = getMapValue(m, "SUBCLASSCD");
        }
        return subclassCd;
    }

    private static String getMapValue(final Map m, final String key) {
        if (null == m || !m.containsKey(key)) {
            throw new RuntimeException(" !! not contained field '" + key + "' in resultmap " + m + ".");
        }
        return (String) m.get(key);
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 66186 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    private static class PrintLine {
        final Subclass _subclass;
        final Integer _stdCount;
        final Course _course;
        final Chair _chair;
        final Collection _chairstudent;
        final Collection _subClassStudent;

        public PrintLine(final Subclass subclass, final Integer stdcount,
        final Course course, final Chair chair, final Collection chairstudent, final Collection subclassStudent) {
            _subclass = subclass;
            _stdCount = stdcount;
            _course = course;
            _chair = chair;
            _chairstudent = chairstudent;
            _subClassStudent = subclassStudent;
        }

        private static List groupBySubclass(final List printLines) {
            final List rtn = new ArrayList();
            List current = null;
            String currentSubclasscd = null;
            for (final Iterator it = printLines.iterator(); it.hasNext();) {
                final PrintLine pl = (PrintLine) it.next();
                if (null == currentSubclasscd || !currentSubclasscd.equals(pl._subclass._subclasscd)) {
                    current = new ArrayList();
                    rtn.add(current);
                    currentSubclasscd = pl._subclass._subclasscd;
                }
                current.add(pl);
            }
            return rtn;
        }

        private static List groupByCourse(final List printLines) {
            final List rtn = new ArrayList();
            List current = null;
            String currentCourse = null;
            for (final Iterator it = printLines.iterator(); it.hasNext();) {
                final PrintLine pl = (PrintLine) it.next();
                if (null == currentCourse || ((pl._course == null && !currentCourse.equals("(null)")) || pl._course != null && !currentCourse.equals(pl._course.getMajorCode()))) {
                    current = new ArrayList();
                    rtn.add(current);
                    if (null == pl._course) {
                        currentCourse = "(null)";
                    } else {
                        currentCourse = pl._course.getMajorCode();
                    }
                }
                current.add(pl);
            }
            return rtn;
        }
    }

    private static class Student implements Comparable {
        final String _schregno;
        final String _sex;
        final String _coursecd;
        final String _majorcd;
        final String _coursecode;
        final String _grade;
        Course _course;
        public Student(final String schregno, final String sex, final String coursecd, final String majorcd, final String coursecode, final String grade) {
            _schregno = schregno;
            _sex = sex;
            _coursecd = coursecd;
            _majorcd = majorcd;
            _coursecode = coursecode;
            _grade = grade;
        }
        public static void setCourse(final Collection students, final Collection courses) {
            for (final Iterator it = students.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                final Course course = Course.mapTo(courses, student._coursecd, student._majorcd, student._coursecode, student._grade);
                student._course = course;
            }
        }
        public int compareTo(final Object o) {
            if (null == o || !(o instanceof Student)) {
                return -1;
            }
            final Student other = (Student) o;
            return _schregno.compareTo(other._schregno);
        }
        /**
         * 在籍している生徒とコースを作成する
         * @param db2
         * @param param
         */
        public static void load(final DB2UDB db2, final Param param, final Collection students) {
            students.clear();
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT ");
            sql.append("   T1.SCHREGNO, T2.SEX, T1.GRADE, T1.COURSECD, T1.MAJORCD, T1.COURSECODE ");
            sql.append(" FROM SCHREG_REGD_DAT T1 ");
            if ("1".equals(param.use_prg_schoolkind)) {
                if (!StringUtils.isBlank(param.selectSchoolKind)) {
                    sql.append("  INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR AND GDAT.GRADE = T1.GRADE AND SCHOOL_KIND IN (" + param.selectSchoolKindIn + ") ");
                }
            } else if ("1".equals(param._useSchool_KindField) && !StringUtils.isBlank(param._SCHOOL_KIND) && !StringUtils.isBlank(param._SCHOOLCD)) {
                sql.append("  INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR AND GDAT.GRADE = T1.GRADE AND SCHOOL_KIND = '" + param._SCHOOL_KIND + "' ");
            }
            sql.append(" INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            sql.append(" WHERE T1.YEAR = '" + param._year + "' ");
            sql.append("   AND T1.SEMESTER = '" + param._semester + "'");
            if (!GRADE_ALL.equals(param._grade)) {
                sql.append(" AND T1.GRADE = '" + param._grade + "' ");
            }
            final List resultList = querySql(db2, sql.toString());

            for (final Iterator it = resultList.iterator(); it.hasNext();) {
                final Map m = (Map) it.next();

                final String schregno = getMapValue(m, "SCHREGNO");
                final String sex = getMapValue(m, "SEX");
                final String coursecd = getMapValue(m, "COURSECD");
                final String majorcd = getMapValue(m, "MAJORCD");
                final String coursecode = getMapValue(m, "COURSECODE");
                final String grade = getMapValue(m, "GRADE");
                final Student student = new Student(schregno, sex, coursecd, majorcd, coursecode, grade);
                students.add(student);
            }
        }
        public static Student getStudent(final Collection students, final String schregno) {
            Student student = null;
            for (final Iterator it = students.iterator(); it.hasNext();) {
                final Student s = (Student) it.next();
                if (s._schregno.equals(schregno)) {
                    student = s;
                    break;
                }
            }
            return student;
        }
        /**
         * コースと生徒のSetのマップを得る
         */
        public static Map groupByCourse(final Collection students) {
            final Map map = new HashMap();
            for (final Iterator it = students.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                getMappedSet(map, student._course).add(student);
            }
            return map;
        }
        public static Map groupBySex(final Collection students) {
            final Map map = new HashMap();
            for (final Iterator it = students.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                getMappedSet(map, student._sex).add(student);
            }
            return map;
        }
        public String toString() {
            return "[" + _schregno + "]";
        }
    }

    private static class Subclass implements Comparable {
        Clazz clazz;
        final String _subclasscd;
        final String _subclassname;
        final List _creditMsts;
        final Collection _chairs;
        public Subclass(final String subclasscd, final String subclassname) {
            _subclasscd = subclasscd;
            _subclassname = subclassname;
            _creditMsts = new ArrayList();
            _chairs = new TreeSet();
        }
        /**
         * この科目に属する全ての講座の講座名簿が空か
         * @return 空ならtrue、そうでなければfalse
         */
        public Set allChairStudents() {
            final Set allStudents = new TreeSet();
            for (final Iterator it = _chairs.iterator(); it.hasNext();) {
                final Chair chair = (Chair) it.next();
                allStudents.addAll(chair._students);
            }
            return allStudents;
        }
        public static Subclass getSubclass(final Collection col, final String subclasscd) {
            Subclass subc = null;
            for (final Iterator it = col.iterator(); it.hasNext();) {
                final Subclass subclass = (Subclass) it.next();
                if (subclass._subclasscd.equals(subclasscd)) {
                    subc = subclass;
                    break;
                }
            }
            return subc;
        }
        public int compareTo(final Object o) {
            if (null == o || !(o instanceof Subclass)) {
                return -1;
            }
            final Subclass other = (Subclass) o;
            return _subclasscd.compareTo(other._subclasscd);
        }
        public int hashCode() {
            return _subclasscd.hashCode() + (null == _subclassname ? 0 : _subclassname.hashCode());
        }
        public String toString() {
            return "Subclass(" + _subclasscd + ":" + _subclassname + ")";
        }
    }

    private static class Clazz implements Comparable {
        final String _classcd;
        final String _classname;
        final Set _subclasses;
        public Clazz(final String classcd, final String classname) {
            _classcd = classcd;
            _classname = classname;
            _subclasses = new TreeSet();
        }
        /**
         * 教科マスタ・科目マスタから教科と科目を作成する
         * @param db2
         * @return
         */
        public static List load(final DB2UDB db2, final Param param, final Collection clazzes, final Collection subclasses) {
            final List rtn = new ArrayList();
            String sql = "";
            if ("1".equals(param._useCurriculumcd)) {
                sql += " SELECT CM.CLASSCD, CM.SCHOOL_KIND, SM.CURRICULUM_CD, CM.CLASSNAME, ";
                sql += "        SM.SUBCLASSCD AS SUBCLASSCD, SM.SUBCLASSNAME ";
                sql += "  FROM CLASS_MST CM, SUBCLASS_MST SM ";
                sql += " WHERE CM.CLASSCD || '-' || CM.SCHOOL_KIND = ";
                sql += "       SM.CLASSCD || '-' || SM.SCHOOL_KIND ";
                if ("1".equals(param.use_prg_schoolkind)) {
                    if (!StringUtils.isBlank(param.selectSchoolKind)) {
                        sql += " AND CM.SCHOOL_KIND IN (" + param.selectSchoolKindIn + ") ";
                    }
                } else if ("1".equals(param._useSchool_KindField) && !StringUtils.isBlank(param._SCHOOL_KIND) && !StringUtils.isBlank(param._SCHOOLCD)) {
                    sql += " AND CM.SCHOOL_KIND = '" + param._SCHOOL_KIND + "' ";
                    sql += " AND SM.SCHOOL_KIND = '" + param._SCHOOL_KIND + "' ";
                }
            } else {
                sql += " SELECT CM.CLASSCD, CM.CLASSNAME, SM.SUBCLASSCD, SM.SUBCLASSNAME ";
                sql += "  FROM CLASS_MST CM, SUBCLASS_MST SM ";
                sql += " WHERE SUBSTR(SM.SUBCLASSCD, 1, 2) = CM.CLASSCD ";
            }

            final List resultList = querySql(db2, sql);
            for (final Iterator it = resultList.iterator(); it.hasNext();) {
                final Map m = (Map) it.next();

                final String classcd = getClasscd(m, param);
                final String classname = getMapValue(m, "CLASSNAME");
                Clazz clazz = new Clazz(classcd, classname);
                if (rtn.contains(clazz)) {
                    clazz = (Clazz) rtn.get(rtn.indexOf(clazz));
                } else {
                    rtn.add(clazz);
                    clazzes.add(clazz);
                }

                final String subclasscd = getSubclasscd(m, param);

                if (null == Subclass.getSubclass(clazz._subclasses, subclasscd)) {
                    final String subclassname = getMapValue(m, "SUBCLASSNAME");
                    final Subclass dsubclass = new Subclass(subclasscd, subclassname);
                    clazz._subclasses.add(dsubclass);
                    dsubclass.clazz = clazz;
                    subclasses.add(dsubclass);
                }
            }
            return rtn;
        }
        public int compareTo(final Object o) {
            if (null == o || !(o instanceof Clazz)) {
                return -1;
            }
            final Clazz other = (Clazz) o;
            return _classcd.compareTo(other._classcd);
        }
        public int hashCode() {
            return _classcd.hashCode() + _classname.hashCode();
        }
    }

    private static class Course implements Comparable {
        final String _grade;
        final String _coursecd;
        final String _majorcd;
        final String _coursecode;
        public Course(final String grade, final String coursecd, final String majorcd, final String coursecode) {
            _grade = grade;
            _coursecd = coursecd;
            _majorcd = majorcd;
            _coursecode = coursecode;
        }
        public String getMajorCode() {
            return _grade + _coursecd + _majorcd;
        }
        /**
         * 学科名をロードする。
         * @param db2
         */
        public static void loadMajorNames(final DB2UDB db2, final Map majorNames) {
            majorNames.clear();
            final List resultList = querySql(db2, "SELECT * FROM MAJOR_MST");
            for (final Iterator it = resultList.iterator();it.hasNext();) {
                final Map m = (Map) it.next();
                mapToMajorName(majorNames, m);
            }
        }
        public int compareTo(Object o) {
            if (null == o || !(o instanceof Course)) {
                return -1;
            }
            final Course other = (Course) o;
            int ret = 0;
            if (_grade == null) {
                return 1;
            } else if (other._grade == null) {
                return -1;
            } else {
                ret = _grade.compareTo(other._grade);
                if (0 != ret) {
                    return ret;
                }
            }
            if (_coursecd == null) {
                return 1;
            } else if (other._coursecd == null) {
                return -1;
            } else {
                ret = _coursecd.compareTo(other._coursecd);
                if (0 != ret) {
                    return ret;
                }
            }
            if (_majorcd == null) {
                return 1;
            } else if (other._majorcd == null) {
                return -1;
            } else {
                ret = _majorcd.compareTo(other._majorcd);
                if (0 != ret) {
                    return ret;
                }
            }
            if (_coursecode == null) {
                return 1;
            } else if (other._coursecode == null) {
                return -1;
            } else {
                ret = _coursecode.compareTo(other._coursecode);
                if (0 != ret) {
                    return ret;
                }
            }
            return ret;
        }
        public String toString() {
            return "Course[" + _grade + ":" + _coursecd + ":" + _majorcd + ":" + _coursecode + "]";
        }
        public static void mapToMajorName(final Map majorNames, final Map m) {
            final String coursecd = getMapValue(m, "COURSECD");
            final String majorcd = getMapValue(m, "MAJORCD");
            final String majorname = getMapValue(m, "MAJORNAME");
            getMappedMap(majorNames, coursecd).put(majorcd, majorname);
        }
        public static Course mapTo(final Collection courses, final String coursecd, final String majorcd, final String coursecode, final String grade) {
            Course c = getCourse(courses, grade, coursecd, majorcd, coursecode);
            if (null == c) {
                c = new Course(grade, coursecd, majorcd, coursecode);
                courses.add(c);
            }
            return c;
        }
        public static Course getCourse(final Collection courses, final String grade, final String coursecd, final String majorcd, final String coursecode) {
            Course c = null;
            for (final Iterator it = courses.iterator(); it.hasNext();) {
                final Course course = (Course) it.next();
                if (course._grade.equals(grade) && course._coursecd.equals(coursecd) && course._majorcd.equals(majorcd) && course._coursecode.equals(coursecode)) {
                    c = course;
                    break;
                }
            }
            return c;
        }
    }

    private static class Chair implements Comparable {
        final String _chaircd;
        final String _groupcd;
        final String _chairname;
        final Subclass _subclass;
        final Set _trgtgrades;
        final Set _students; // 講座名簿の生徒
        public Chair(final String chaircd, final String groupcd, final String chairname, final Subclass subclass) {
            _chaircd = chaircd;
            _groupcd = groupcd;
            _chairname = chairname;
            _subclass = subclass;
            _trgtgrades = new HashSet();
            _students = new HashSet();
        }
        public Set getStudentCourses() {
            return new TreeSet(Student.groupByCourse(_students).keySet());
        }
        public Collection getStudent(final Course course) {
            final Collection col = (Collection) Student.groupByCourse(_students).get(course);
            return null == col ? Collections.EMPTY_SET : col;
        }
        public static Collection filterChairsByGrade(final String grade, final Collection chairs, final Collection subclasses) {
            final Collection col = new TreeSet();
            if (GRADE_ALL.equals(grade)) {
                col.addAll(chairs);
            } else {
                final Collection nottarget = new TreeSet();
                for (final Iterator it = chairs.iterator(); it.hasNext();) {
                    final Chair chair = (Chair) it.next();
                    if (!chair._trgtgrades.contains(grade)) {
                        nottarget.add(chair);
                    }
                }
                for (final Iterator it = subclasses.iterator(); it.hasNext();) {
                    final Subclass subclass = (Subclass) it.next();
                    subclass._chairs.removeAll(nottarget);
                }
                chairs.removeAll(nottarget);
            }
            return col;
        }

        public static void load(final DB2UDB db2, final Param param, final Collection chairs, final Collection subclasses, final Collection students) {
            chairs.clear();
            List resultSet;
            final String year = param._year;
            final String semester = param._semester;

            String sql = "SELECT * FROM CHAIR_DAT WHERE YEAR = '" + year + "' AND SEMESTER = '" + semester + "' ";
            if ("1".equals(param.use_prg_schoolkind)) {
                if (!StringUtils.isBlank(param.selectSchoolKind)) {
                    sql += "  AND SCHOOL_KIND IN (" + param.selectSchoolKindIn + ") ";
                }
            } else if ("1".equals(param._useSchool_KindField) && !StringUtils.isBlank(param._SCHOOL_KIND) && !StringUtils.isBlank(param._SCHOOLCD)) {
                sql += " AND SCHOOL_KIND = '" + param._SCHOOL_KIND + "' ";
            }
            sql += " ORDER BY CHAIRCD ";
            resultSet= querySql(db2, sql);
            for (final Iterator it = resultSet.iterator(); it.hasNext();) {
                final Map m = (Map) it.next();

                final String chaircd = getMapValue(m, "CHAIRCD");
                final String groupcd = getMapValue(m, "GROUPCD");
                final String chairname = getMapValue(m, "CHAIRNAME");
                final String subclassCd = getSubclasscd(m, param);
                final Subclass subclass = Subclass.getSubclass(subclasses, subclassCd);
                Chair chair = null;
                if (null != subclass) {
                    chair = new Chair(chaircd, groupcd, chairname, subclass);
                    chairs.add(chair);
                    chair._subclass._chairs.add(chair);
                }
            }
            String sql2 = "SELECT * FROM CHAIR_CLS_DAT T1 ";
            if ("1".equals(param.use_prg_schoolkind)) {
                if (!StringUtils.isBlank(param.selectSchoolKind)) {
                    sql2 += "  INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.TRGTGRADE AND T2.SCHOOL_KIND IN (" + param.selectSchoolKindIn + ") ";
                }
            } else if ("1".equals(param._useSchool_KindField) && !StringUtils.isBlank(param._SCHOOL_KIND) && !StringUtils.isBlank(param._SCHOOLCD)) {
                sql2 += " INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.TRGTGRADE ";
                sql2 += " AND T2.SCHOOL_KIND = '" + param._SCHOOL_KIND + "' ";
            }
            sql2 += " WHERE T1.YEAR = '" + year + "' AND SEMESTER = '" + semester + "' " + (GRADE_ALL.equals(param._grade) ? "" : "AND TRGTGRADE = '" + param._grade + "' ");
            resultSet = querySql(db2, sql2);
            for (final Iterator it = resultSet.iterator(); it.hasNext();) {
                final Map m = (Map) it.next();
                // 講座クラスデータの学年をセットする
                final String chaircd = getMapValue(m, "CHAIRCD");
                final String groupcd = getMapValue(m, "GROUPCD");
                final String grade = getMapValue(m, "TRGTGRADE");
                if (null != grade) {
                    for (final Iterator cit = getChairs(chairs, chaircd, groupcd).iterator(); cit.hasNext();) {
                        final Chair chair = (Chair) cit.next();
                        chair._trgtgrades.add(grade);
                    }
                }
            }
            filterChairsByGrade(param._grade, chairs, subclasses);
            String sql3 = "SELECT * FROM CHAIR_STD_DAT T1 ";
            if ("1".equals(param.use_prg_schoolkind)) {
                if (!StringUtils.isBlank(param.selectSchoolKind)) {
                    sql3 += " INNER JOIN SCHREG_REGD_DAT T3 ON T3.YEAR = T1.YEAR AND T3.SEMESTER = T1.SEMESTER AND T3.SCHREGNO = T1.SCHREGNO ";
                    sql3 += " INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T3.GRADE AND T2.SCHOOL_KIND IN (" + param.selectSchoolKindIn + ") ";
                }
            } else if ("1".equals(param._useSchool_KindField) && !StringUtils.isBlank(param._SCHOOL_KIND) && !StringUtils.isBlank(param._SCHOOLCD)) {
                sql3 += " INNER JOIN SCHREG_REGD_DAT T3 ON T3.YEAR = T1.YEAR AND T3.SEMESTER = T1.SEMESTER AND T3.SCHREGNO = T1.SCHREGNO ";
                sql3 += " INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T3.GRADE ";
                sql3 += " AND T2.SCHOOL_KIND = '" + param._SCHOOL_KIND + "' ";
            }
            sql3 += " WHERE T1.YEAR = '" + year + "' AND T1.SEMESTER = '" + semester + "' AND '" + param._appDate + "' BETWEEN APPDATE AND APPENDDATE ";
            resultSet = querySql(db2, sql3);
            for (final Iterator it = resultSet.iterator(); it.hasNext();) {
                final Map m = (Map) it.next();
                final Student student = Student.getStudent(students, getMapValue(m, "SCHREGNO"));
                final Chair chair = getChair(chairs, getMapValue(m, "CHAIRCD"));
                if (null != chair && null != student) {
                    chair._students.add(student);
                }
            }
        }
        public static List getChairs(final Collection chairInstances, final String chaircd, final String groupcd) {
            final List list = new ArrayList();
            for (final Iterator it = chairInstances.iterator(); it.hasNext();) {
                final Chair c = (Chair) it.next();
                if (!c._chaircd.equals(CHAIRCD0000000) && c._chaircd.equals(chaircd) || !GUN0CD000.equals(c._groupcd) && groupcd.equals(c._groupcd)) {
                    list.add(c);
                }
            }
            return list;
        }
        public static Chair getChair(final Collection chairInstances, final String chaircd) {
            Chair chair = null;
            for (final Iterator it = chairInstances.iterator(); it.hasNext();) {
                final Chair c = (Chair) it.next();
                if (c._chaircd.equals(chaircd)) {
                    chair = c;
                    break;
                }
            }
            return chair;
        }
        public int compareTo(Object o) {
            if (null == o || !(o instanceof Chair)) {
                return -1;
            }
            Chair other = (Chair) o;
            int ret = 0;
            if (null != _subclass) {
                if (null == other._subclass) {
                    ret = -1;
                } else {
                    ret = _subclass.compareTo(other._subclass);
                }
            }
            if (0 != ret) return ret;
            return _chaircd.compareTo(other._chaircd);
        }
        public int hashCode() {
            return _chaircd.hashCode();
        }
        public String toString() {
            return "Chair(" + _chaircd + ":" + _chairname + ")";
        }
    }

    private static class CreditMst {
        final Subclass _subclass;
        final Course _course;
        final String _credit;
        final String _requireFlg;
        public CreditMst(final Subclass subclass, final Course course, final String credit, final String requireFlg) {
            _subclass = subclass;
            _course = course;
            _credit = credit;
            _requireFlg = requireFlg;
        }
        /**
         * 単位マスタをロードする
         * @param db2
         * @param param
         * @param courses
         */
        public static void load(final DB2UDB db2, final Param param, final Collection courses, final Collection subclasses, final Collection creditMsts, final Map creditMstRequireNames) {
            creditMsts.clear();
            for (final Iterator it = courses.iterator(); it.hasNext();) {
                final Course course = (Course) it.next();
                final List resultSet = querySql(db2, " SELECT * FROM CREDIT_MST CM WHERE CM.YEAR = '" + param._year + "' AND CM.GRADE = '" + course._grade + "' AND CM.COURSECD = '" + course._coursecd + "' AND CM.MAJORCD = '" + course._majorcd + "' AND CM.COURSECODE = '" + course._coursecode + "' ");
                for (final Iterator i = resultSet.iterator(); i.hasNext();) {
                    final Map m = (Map) i.next();

                    final Subclass subclass = Subclass.getSubclass(subclasses, getSubclasscd(m, param));
                    if (null != course && null != subclass) {
                        final String credit = getMapValue(m, "CREDITS");
                        final String requireFlg = getMapValue(m, "REQUIRE_FLG");
                        final CreditMst cm = new CreditMst(subclass, course, credit ,requireFlg);
                        creditMsts.add(cm);
                    }
                }
            }
            // 必履修区分名称をロードする。
            creditMstRequireNames.clear();
            final List resultSet = querySql(db2, " SELECT * FROM NAME_MST WHERE NAMECD1 = 'Z011' ");
            for (final Iterator it = resultSet.iterator(); it.hasNext();) {
                final Map m = (Map) it.next();

                final String cd = getMapValue(m, "NAMECD2");
                final String name = getMapValue(m, "NAMESPARE1");
                creditMstRequireNames.put(cd, name);
            }
        }

        public static CreditMst getCreditMst(final Collection creditMstInstances, final Subclass subclass, final Course course) {
            if (null == subclass || null == course) {
                return null;
            }
            CreditMst cm = null;
            for (final Iterator it = creditMstInstances.iterator(); it.hasNext();) {
                final CreditMst c = (CreditMst) it.next();
                if (c._subclass.equals(subclass) && c._course.equals(course)) {
                    cm = c;
                    break;
                }
            }
            return cm;
        }
        public int hashCode() {
            return (null == _course ? 0 : _course.hashCode()) + (null == _subclass ? 0 : _subclass.hashCode());
        }
        public String toString() {
            return "CreditMst(" + _subclass + ":" + _course + ":credit=" + _credit + ":requireflg=" + _requireFlg + ")";
        }
    }

    /**
     * 科目履修選択マスタ
     */
    private static class SubclassCompSelectMst implements Comparable {
        final Subclass _subclass;
        final Collection _selectDatList;
        public SubclassCompSelectMst(final Subclass subclass) {
            _subclass = subclass;
            _selectDatList = new TreeSet();
        }

        public static void load(final DB2UDB db2, final Param param, final Collection subclassCompSelectMstInstances, final Collection studentInstances, final Collection subclassInstances) {
            subclassCompSelectMstInstances.clear();
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT DISTINCT ");
            if ("1".equals(param._useCurriculumcd)) {
                sql.append("   SCSD.CLASSCD, SCSD.SCHOOL_KIND, SCSD.CURRICULUM_CD, ");
            }
            sql.append("   SCSD.SUBCLASSCD, ");
            sql.append("   SSSD.SCHREGNO ");
            sql.append(" FROM ");
            sql.append("   SUBCLASS_COMP_SELECT_DAT SCSD ");
            if ("1".equals(param.use_prg_schoolkind)) {
                if (!StringUtils.isBlank(param.selectSchoolKind)) {
                    sql.append("  INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = SCSD.YEAR AND GDAT.GRADE = SCSD.GRADE AND GDAT.SCHOOL_KIND IN (" + param.selectSchoolKindIn + ") ");
                }
            } else if ("1".equals(param._useSchool_KindField) && !StringUtils.isBlank(param._SCHOOL_KIND) && !StringUtils.isBlank(param._SCHOOLCD)) {
                sql.append(" INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = SCSD.YEAR AND GDAT.GRADE = SCSD.GRADE AND GDAT.SCHOOL_KIND = '" + param._SCHOOL_KIND + "' ");
            }
            sql.append("   LEFT JOIN SUBCLASS_STD_SELECT_RIREKI_DAT SSSD ON SSSD.YEAR = SCSD.YEAR ");
            sql.append("     AND SSSD.SEMESTER = '" + param._semester + "' ");
            sql.append("     AND SSSD.RIREKI_CODE = '" + param._rirekiCode + "' ");
            if ("1".equals(param._useCurriculumcd)) {
                sql.append("     AND SSSD.CLASSCD = SCSD.CLASSCD ");
                sql.append("     AND SSSD.SCHOOL_KIND = SCSD.SCHOOL_KIND ");
                sql.append("     AND SSSD.CURRICULUM_CD = SCSD.CURRICULUM_CD ");
            } else {
                sql.append("     AND SSSD.CURRICULUM_CD = '2' ");
            }
            sql.append("     AND SSSD.SUBCLASSCD = SCSD.SUBCLASSCD ");
            sql.append(" WHERE ");
            sql.append("     SCSD.YEAR = '" + param._year + "'");
            if (!GRADE_ALL.equals(param._grade)) {
                sql.append("     AND SCSD.GRADE = '" + param._grade + "'");
            }
            if ("1".equals(param._useSchool_KindField) && !StringUtils.isBlank(param._SCHOOL_KIND) && !StringUtils.isBlank(param._SCHOOLCD)) {
                sql.append(" AND SCSD.SCHOOL_KIND = '" + param._SCHOOL_KIND +"' ");
            }

            final List resultlist = querySql(db2, sql.toString());
            for (final Iterator it = resultlist.iterator(); it.hasNext();) {
                final Map m = (Map) it.next();
                final SubclassCompSelectMst scsm = mapTo(subclassCompSelectMstInstances, m, subclassInstances, param);
                final Student student = Student.getStudent(studentInstances, getMapValue(m, "SCHREGNO"));
                if (null != student) {
                    final SubclassCompSelectDat scsd = SubclassCompSelectDat.mapTo(scsm._selectDatList, student);
                    scsd._students.add(student);
                }
            }
        }

        public static SubclassCompSelectMst mapTo(final Collection col, final Map m, final Collection subclassInstances, final Param param) {
            final Subclass subclass = Subclass.getSubclass(subclassInstances, getSubclasscd(m, param));
            SubclassCompSelectMst scsm = null;
            for (final Iterator it = col.iterator(); it.hasNext() && scsm == null;) {
                SubclassCompSelectMst s = (SubclassCompSelectMst) it.next();
                if (s._subclass.equals(subclass)) {
                    scsm = s;
                }
            }
            if (null == scsm) {
                scsm = new SubclassCompSelectMst(subclass);
                col.add(scsm);
            }
            return scsm;
        }
        public int compareTo(Object o) {
            if (null == o || !(o instanceof SubclassCompSelectMst)) {
                return -1;
            }
            final SubclassCompSelectMst scsm = (SubclassCompSelectMst) o;
            return scsm._subclass == null ? -1 : _subclass.compareTo(scsm._subclass);
        }
        public Collection totalStudent() {
            final Set students = new TreeSet();
            for (final Iterator it = _selectDatList.iterator(); it.hasNext();) {
                final SubclassCompSelectDat scsd = (SubclassCompSelectDat) it.next();
                students.addAll(scsd._students);
            }
            return students;
        }
        public int totalStudentSize() {
            final Set students = new TreeSet();
            for (final Iterator it = _selectDatList.iterator(); it.hasNext();) {
                final SubclassCompSelectDat scsd = (SubclassCompSelectDat) it.next();
                students.addAll(scsd._students);
            }
            return students.size();
        }
        public int hashCode() {
            return (_subclass == null) ? 0 : _subclass.hashCode();
        }
        public String toString() {
            return "SubclassCompSelectMst(" + _subclass + ":" + _subclass._chairs + ":" + _selectDatList + ")";
        }
    }

    /**
     * 科目履修選択データ
     */
    private static class SubclassCompSelectDat implements Comparable {
        final String _grade;
        final Set _students;
        public SubclassCompSelectDat(final String grade) {
            _grade = grade;
            _students = new TreeSet();
        }

        public static boolean allStudentsAreChairStudent(final Collection courseStudents, final Collection chairs) {
            return getDisplayChair(courseStudents, chairs) != Collections.EMPTY_SET;
        }

        /**
         *
         * @param students0 指定の生徒
         * @param chairs 指定の複数の講座
         * @return chairs1 = 指定の複数の講座に含まれる講座。 指定の生徒が、chairs1の名簿に設定されており、かつchairs1の名簿の生徒数の合計が指定の生徒数と一致するならaを得る。それ以外は空の Collectionを得る。
         */
        public static Collection getDisplayChair(final Collection students0, final Collection chairs) {
            final Collection students = new TreeSet(students0); // 講座名簿に設定されていない生徒
            final Set chairsIncludesStudent = new TreeSet();
            final Set includedStudent = new TreeSet();
            for (final Iterator itstudent = students.iterator(); itstudent.hasNext();) {
                final Student student = (Student) itstudent.next();
                for (final Iterator itc = chairs.iterator(); itc.hasNext();) {
                    final Chair chair = (Chair) itc.next();
                    if (chair._students.contains(student)) { // 講座名簿に設定されている
                        chairsIncludesStudent.add(chair);
                        includedStudent.add(student);
                    }
                }
            }
            students.removeAll(includedStudent); // 講座名簿に設定されていない生徒から省く
            if (chairsIncludesStudent.isEmpty() || !students.isEmpty()) {
                // 指定の複数の講座名簿に指定の生徒が一人も設定されていない もしくは どの講座名簿にも設定されていない生徒がいる場合
                return Collections.EMPTY_SET;
            }

            // 以下は、指定の複数の講座名簿に指定の生徒が全て設定されている場合
            int chairStudentTotal = 0; // 指定の生徒がひとりでも名簿に設定されている講座の名簿の生徒数
            for (final Iterator itc = chairsIncludesStudent.iterator(); itc.hasNext();) {
                final Chair chair = (Chair) itc.next();
                chairStudentTotal += chair._students.size();
            }
            // 指定の生徒の数と chairStudentTotal が一致するならその講座を得る
            return students0.size() == chairStudentTotal ? chairsIncludesStudent : Collections.EMPTY_SET;
        }

        public static SubclassCompSelectDat mapTo(final Collection col, final Student student) {
            final String grade = student._course._grade;
            SubclassCompSelectDat scsd = null;
            for (final Iterator it = col.iterator(); it.hasNext();) {
                final SubclassCompSelectDat s = (SubclassCompSelectDat) it.next();
                if (s._grade.equals(grade)) {
                    scsd = s;
                    break;
                }
            }
            if (null == scsd) {
                scsd = new SubclassCompSelectDat(grade);
                col.add(scsd);
            }
            return scsd;
        }

        public int compareTo(Object o) {
            if (null == o || !(o instanceof SubclassCompSelectDat)) {
                return -1;
            }
            if (null == _grade) {
                return 1;
            }
            final SubclassCompSelectDat other = (SubclassCompSelectDat) o;
            return _grade.compareTo(other._grade);
        }
        public int hashCode() {
            return _grade.hashCode() * 29;
        }
        public String toString() {
            return "SelectDat(" + _grade + ":" + _students.size() + ")";
        }
    }

    /** パラメータクラス */
    private static class Param {
        final String _year;
        final String _semester;
        final String _loginDate;
        final String _appDate;

        /** ログイン時間 */
        final String _loginHour;
        final String _loginMinutes;

        final String _loginStaffCd;
        final String _selectTable;
        final String _grade;

        final String _useCurriculumcd;
        private final String _rirekiCode;
        private final String _SCHOOLCD;
        private final String _SCHOOL_KIND;
        private final String _useSchool_KindField;
        private final String use_prg_schoolkind;
        private final String selectSchoolKind;
        private String selectSchoolKindIn;

        String _gradeName = null;
        String _schoolName = null;
        String _loginStaffName = null;
        String _requireInfo = null;

        private List clazzInstances = new ArrayList();
        private Collection subclassInstances = new TreeSet(); // 全てのインスタンス
        final List studentInstances = new ArrayList();
        final Collection chairInstances = new TreeSet();
        final Map majorNames = new HashMap(); // MappedMap<courseCd, majorCd, majorName>
        final Set courseInstances = new TreeSet();
        final Map creditMstRequireNames = new HashMap();
        final Collection creditMstInstances = new HashSet();
        final Collection subclassCompSelectMstInstances = new TreeSet();

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _loginDate = request.getParameter("LOGIN_DATE");
            _appDate = request.getParameter("DATE").replace('/', '-'); // 講座適用日付
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            _SCHOOLCD= request.getParameter("SCHOOLCD");
            _SCHOOL_KIND = request.getParameter("SCHOOL_KIND");
            use_prg_schoolkind = request.getParameter("use_prg_schoolkind");
            selectSchoolKind = request.getParameter("selectSchoolKind");
            if (!StringUtils.isBlank(selectSchoolKind)) {
                String[] split = StringUtils.split(selectSchoolKind, ":");
                String comma = "";
                selectSchoolKindIn = "";
                for (int i = 0; i < split.length; i++) {
                    selectSchoolKindIn += comma + "'" + split[i] + "'";
                    comma = ",";
                }
                log.info(" selectSchoolKindIn = " + selectSchoolKindIn);
            }

            final Calendar cal = Calendar.getInstance();
            _loginHour = String.valueOf(cal.get(Calendar.HOUR_OF_DAY));
            _loginMinutes = String.valueOf(cal.get(Calendar.MINUTE));

            _loginStaffCd = request.getParameter("STAFFCD");
            _selectTable = request.getParameter("OUTPUT");
            _grade = request.getParameter("GRADE");

            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _rirekiCode = request.getParameter("RIREKI_CODE");

            setSchoolName(db2);
            setGradeName(db2);
            setLoginStaffName(db2);
            setRequireInfo(db2);
        }

        /** スタッフ名取得 */
        private void setLoginStaffName(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            _loginStaffName = null;
            try {
                final String sql = "SELECT STAFFNAME FROM STAFF_MST WHERE STAFFCD = '" + _loginStaffCd + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    _loginStaffName = rs.getString("STAFFNAME");
                }
            } catch (SQLException e) {
                log.error("Exception:", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        /** 学年名取得 */
        private void setGradeName(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            _gradeName = null;
            try {
                String sql = "SELECT GRADE_NAME1 FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND GRADE = '" + _grade + "' ";
                if ("1".equals(_useSchool_KindField) && !StringUtils.isBlank(_SCHOOL_KIND) && !StringUtils.isBlank(_SCHOOLCD)) {
                    sql += ("   AND SCHOOL_KIND = '" + _SCHOOL_KIND + "' ");
                }
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    _gradeName = rs.getString("GRADE_NAME1") != null ? rs.getString("GRADE_NAME1") : "";
                }
                if (GRADE_ALL.equals(_grade)) {
                    _gradeName = "全学年";
                }
            } catch (SQLException e) {
                log.error("Exception:", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        /** 学校名取得 */
        private void setSchoolName(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            _schoolName = null;
            try {
                String sql = "SELECT SCHOOLNAME1 FROM SCHOOL_MST WHERE YEAR = '" + _year + "' ";
                if ("1".equals(_useSchool_KindField) && !StringUtils.isBlank(_SCHOOL_KIND) && !StringUtils.isBlank(_SCHOOLCD)) {
                    sql += ("   AND SCHOOL_KIND = '" + _SCHOOL_KIND + "' ");
                }
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    _schoolName = rs.getString("SCHOOLNAME1");
                }
            } catch (SQLException e) {
                log.error("Exception:", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        /** 必履修備考取得 */
        private void setRequireInfo(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            _requireInfo = null;
            final StringBuffer info = new StringBuffer();
            try {
                final String sql = "SELECT VALUE(NAMESPARE1,'') || ':' || VALUE(NAME1,'') AS NOTE FROM NAME_MST WHERE NAMECD1 = 'Z011' ORDER BY NAMECD2 ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                info.append("※必履修区分…");
                String noteSep = "";
                while (rs.next()) {
                    info.append(noteSep + rs.getString("NOTE"));
                    noteSep = "、";
                }
            } catch (SQLException e) {
                log.error("Exception:", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            _requireInfo = info.toString();
        }

        public String requireName(final String requireFlg) {
            return (String) creditMstRequireNames.get(requireFlg);
        }

        public String majorName(final String coursecd, final String majorcd) {
            return (String) getMappedMap(majorNames, coursecd).get(majorcd);
        }
    }
}

// eof

