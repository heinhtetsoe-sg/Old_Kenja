// kanji=漢字
/*
 * $Id$
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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

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
import servletpack.KNJD.detail.RecordRankSdivSoutenDat;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.StaffInfo;

/**
 * 成績一覧表（明治中高）
 * @author maesiro
 * @version $Id$
 */
public class KNJD617V {
    private static final Log log = LogFactory.getLog(KNJD617V.class);

    private final String Gakunen = "000";

    private static final String _333333 = "333333";
    private static final String _555555 = "555555";
    private static final String _999999 = "999999";
    private static final String _99999A = "99999A";
    private static final String _99999B = "99999B";

    // 1:学年 2:コース 3:コースグループ
    private static final String GROUP_GRADE = "1";
    private static final String GROUP_COURSE = "2";
    private static final String GROUP_COURSEGROUP = "3";

    private static final String SORT_DIV_ATTENDNO = "1";
    private static final String SORT_DIV_RANK = "2";

    // 平均点一覧HR最大表示数
    private int HR_MAX = 6;
    private final int HR_MAX6 = 6;
    private final int HR_MAX11 = 11;

    private int COUNT1 = 45;  // 1枚目45名
    private final int COUNT2 = 50; // 2枚目以降50名
    private final int COUNT40 = 40;  // 1枚目40名
    private final int COUNT45 = 45;  // 1枚目45名

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
        log.fatal("$Revision: 75512 $ $Date: 2020-07-18 08:54:57 +0900 (土, 18 7 2020) $"); // CVSキーワードの取り扱いに注意

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

            if ("2".equals(_param._printDiv)) {
                final List<Group> groupList = Group.getGroupList(db2, _param, "");
                log.debug(" groupList = " + groupList);
                for (final Group group : groupList) {
                    printMain(db2, svf, group, "");
                }
            } else {
                for (int i = 0; i < _param._selectData.length; i++) {
                    final String gradeHr = _param._selectData[i];
                    final List<Group> groupList = Group.getGroupList(db2, _param, gradeHr);
                    log.debug(" groupList = " + groupList);
                    for (final Group group : groupList) {
                        printMain(db2, svf, group, gradeHr);
                    }
                }
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
        return KNJ_EditEdit.getMS932ByteLength(str);
    }

    static String nullBlank(final Object o) {
        return null == o ? null : o.toString();
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf, final Group group, final String cd) {

        boolean hasData = false;

        final Map<String, HrclassAvg> hrclassMap = getHrclass(db2, group._code, cd);

        final List<Student> students = getStudentList(db2, group._code, cd);
        if (students.isEmpty()) {
            return;
        }

        HR_MAX = _useForm_1_2 ? HR_MAX11 : HR_MAX6;
        COUNT1 = _useForm_1_2 ? COUNT40 : COUNT45;

        final List<String> allsubclasscds = getSubclasscds(db2, group._code, students, cd);
        log.info(" allsubclasscds (size = " + allsubclasscds.size() + ") = " + allsubclasscds);

        final List<List<String>> pageSubclassListList = getPageSubclassListList(allsubclasscds, 26);

        final List<List<Student>> pageStudentListList = getPageStudentListList(students);
        final int studentTotalPage = pageStudentListList.size();
        final int totalPage = studentTotalPage * pageSubclassListList.size();

        for (int spi = 0; spi < pageSubclassListList.size(); spi++) {
            final List<String> subclasscds = pageSubclassListList.get(spi);
            log.info(" subclasscds (size = " + subclasscds.size() + ") = " + subclasscds);
            final boolean isLastSubclassPage = (spi == pageSubclassListList.size() - 1);
            if ("2".equals(_param._printDiv)) {
                if ("1".equals(_param._useLc_Hrclass)) {
                    svf.VrSetForm(_useForm_1_2 ? "KNJD617V_4_2.frm" : "KNJD617V_4.frm", 1);
                } else {
                    svf.VrSetForm(_useForm_1_2 ? "KNJD617V_1_2.frm" : "KNJD617V_1.frm", 1);
                }
            } else {
                   if ("1".equals(_param._useLc_Hrclass)) {
                       svf.VrSetForm("KNJD617V_6.frm", 1);
                   } else {
                       svf.VrSetForm("KNJD617V_3.frm", 1);
                   }
            }
            hasData = printAvg(svf, hrclassMap, subclasscds, isLastSubclassPage);
            printHeader(db2, svf, subclasscds, spi + 1, totalPage);
            if (pageStudentListList.size() > 0) {
                final List<Student> pageStudentList = pageStudentListList.get(0);
                printStudents(svf, hrclassMap, totalPage, subclasscds, isLastSubclassPage, pageStudentList);
            }
            svf.VrEndPage();

            hasData = true;
        }

        if (studentTotalPage > 1) {
            for (int pi = 1; pi < studentTotalPage; pi++) {
                for (int spi = 0; spi < pageSubclassListList.size(); spi++) {
                    final List<String> subclasscds = pageSubclassListList.get(spi);
                    final boolean isLastSubclassPage = (spi == pageSubclassListList.size() - 1);
                    if ("2".equals(_param._printDiv)) {
                           if ("1".equals(_param._useLc_Hrclass)) {
                            svf.VrSetForm("KNJD617V_5.frm", 1);
                           } else {
                            svf.VrSetForm("KNJD617V_2.frm", 1);
                           }
                    } else {
                           if ("1".equals(_param._useLc_Hrclass)) {
                            svf.VrSetForm("KNJD617V_6.frm", 1);
                           } else {
                            svf.VrSetForm("KNJD617V_3.frm", 1);
                           }
                    }
                    printHeader(db2, svf, subclasscds, pi * pageSubclassListList.size() + spi + 1, totalPage);
                    final List<Student> pageStudentList = pageStudentListList.get(pi);
                    printStudents(svf, hrclassMap, totalPage, subclasscds, isLastSubclassPage, pageStudentList);
                    svf.VrEndPage();

                    hasData = true;
                }
            }
        }

        _hasData = _hasData || hasData;
    }

    private void printStudents(final Vrw32alp svf,
            final Map hrclassMap,
            final int totalPage,
            final List subclasscds,
            final boolean isLastSubclassPage,
            final List pageStudentList) {

        for (int si = 0; si < pageStudentList.size(); si++) {
            final Student student = (Student) pageStudentList.get(si);
            printStudent(svf, hrclassMap, subclasscds, si + 1, student, isLastSubclassPage);
        }
    }

    private <T> List<List<T>> getPageSubclassListList(final List<T> list, final int count) {
        final List<List<T>> rtn = new ArrayList<List<T>>();
        List<T> current = null;
        for (final T o : list) {
            if (current == null || current.size() >= count) {
                current = new ArrayList<T>();
                rtn.add(current);
            }
            current.add(o);
        }
        return rtn;
    }

    private void printHeader(final DB2UDB db2, final Vrw32alp svf, final List<String> subclasscds, final int page, final int totalPage) {

        final String title = KNJ_EditDate.getAutoFormatYear(db2, Integer.parseInt(_param._year)) + "年度" + " " + _param._gradename + " " + _param._semestername + " " + _param._testitemname + "成績順一覧表";
        svf.VrsOut("TITLE", title);
        svf.VrsOut("PAGE_HEADER", String.valueOf(page));
        svf.VrsOut("PAGE_FOOTER", String.valueOf(totalPage));
        svf.VrsOut("DATE", KNJ_EditDate.getAutoFormatDate(db2, _param._date));

        for (int si = 0; si < subclasscds.size(); si++) {
            final String subclasscd = subclasscds.get(si);
            final String subclassname = _param._subclassnames.get(subclasscd);
            svf.VrsOut("SUBCLASS_NAME" + (si + 1), subclassname);
            svf.VrsOut("SUBCLASS_NAME" + (si + 1) + "_2", subclassname);
        }
        svf.VrsOut("TOTAL_NAME", "計");
        svf.VrsOut("AVERAGE_NAME", "平均");
        svf.VrsOut("REMARK_NAME", "");
        svf.VrsOut("TOTAL_NAME_2", "総点");
        svf.VrsOut("AVERAGE_NAME_2", "平均");
        svf.VrsOut("CLASS_RANK_NAME", "組順");
        svf.VrsOut("COURSE_RANK_NAME", "コース順");
        svf.VrsOut("GRADE_RANK_NAME", "学年順");
    }

    private List<String> getSubclasscds(final DB2UDB db2, final String selected, final List<Student> students, final String gradeHr) {
        final Map<String, Set<String>> courseSubclassCdMap = new HashMap<String, Set<String>>(); // 表示する科目コード
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getSubclassSql(_param, selected, gradeHr);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                if (null == courseSubclassCdMap.get(rs.getString("COURSE"))) {
                    courseSubclassCdMap.put(rs.getString("COURSE"), new HashSet<String>());
                }
                courseSubclassCdMap.get(rs.getString("COURSE")).add(rs.getString("SUBCLASSCD"));
            }
        } catch (final Exception e) {
            log.error("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }

        for (final Student student : students) {
            if (null == courseSubclassCdMap.get(student._course)) {
                courseSubclassCdMap.put(student._course, new HashSet<String>());
            }
            courseSubclassCdMap.get(student._course).addAll(student._ranks.keySet());
        }

        Collection<String> col = null;
        for (final String course : courseSubclassCdMap.keySet()) {
            final Set<String> subclassCdSet = courseSubclassCdMap.get(course);
            if (null == col) {
                col = new HashSet(subclassCdSet);
            } else {
                col = CollectionUtils.union(col, subclassCdSet);
            }
        }
        if (null == col) {
            col = new HashSet<String>();
        }
        for (final String subclasscd : _param._notTargetSubclassCdList) {
            col.remove(subclasscd);
            log.debug("subclasscd="+subclasscd);
        }
        col.remove(_333333);
        col.remove(_555555);
        col.remove(_999999);
        col.remove(_99999A);
        col.remove(_99999B);
        final List<String> rtn = new ArrayList(col);
        Collections.sort(rtn);
        return rtn;
    }

    private String getSubclassSql(final Param param, final String selected, final String gradeHr) {
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
        if ("1".equals(param._printDiv)) {
            stb.append("     AND T2.GRADE || '-' || T2.HR_CLASS = '" + gradeHr + "' ");
        }
        if ("1".equals(param._use_school_detail_gcm_dat)) {
            stb.append("     AND T2.COURSECD || '-' || T2.MAJORCD = '" + param._major + "' ");
        }
        if ("3".equals(param._subclassGroupDiv) || "5".equals(param._subclassGroupDiv)) {
            stb.append("     INNER JOIN REC_SUBCLASS_GROUP_DAT R1 ON R1.YEAR = T1.YEAR ");
            stb.append("         AND R1.GROUP_DIV = '" + param._subclassGroupDiv + "' ");
            stb.append("         AND R1.GRADE = T2.GRADE ");
            stb.append("         AND R1.COURSECD = T2.COURSECD ");
            stb.append("         AND R1.MAJORCD = T2.MAJORCD ");
            stb.append("         AND R1.COURSECODE = T2.COURSECODE ");
            stb.append("         AND R1.CLASSCD = T1.CLASSCD ");
            stb.append("         AND R1.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("         AND R1.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("         AND R1.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("     INNER JOIN REC_SUBCLASS_GROUP_MST R2 ON R2.YEAR = R1.YEAR ");
            stb.append("         AND R2.GROUP_DIV = R1.GROUP_DIV ");
            stb.append("         AND R2.GRADE = R1.GRADE ");
            stb.append("         AND R2.COURSECD = R1.COURSECD ");
            stb.append("         AND R2.MAJORCD = R1.MAJORCD ");
            stb.append("         AND R2.COURSECODE = R1.COURSECODE ");
        }
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '" + _param._testcd + "' ");
        stb.append("     AND T2.GRADE = '" + _param._grade +"' ");
        stb.append("     AND substr(T1.SUBCLASSCD, 1, 2) < '90' ");

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

    private void printStudent(final Vrw32alp svf, final Map hrclassMap, final List<String> subclasscds, final int si, final Student student,
            final boolean isLastSubclassPage) {

        final Rank stRank = student._ranks.get(_param.getTotalSubclasscd());

        //log.info(" stRank = " + stRank + " (" + student._schregno + ")");

        if (null != stRank) {
            final String grankField = "1".equals(_param._printDiv) ? "GRADE_RANK" : "RANK";
            Integer rank = stRank._gradeRank;
            if ("1".equals(_param._use_school_detail_gcm_dat)) {
                rank = stRank._majorRank;
            }
            svf.VrsOutn(grankField, si, null == rank ? "" : rank.toString());
            svf.VrsOutn("CLASS_RANK", si, null == stRank._classRank ? "" : stRank._classRank.toString());
            svf.VrsOutn("COURSE_RANK", si, null == stRank._courseRank ? "" : stRank._courseRank.toString());
        }

        final String hrname;
        if (_param._isSundaikoufu) {
            hrname = StringUtils.defaultString(student._hrNameabbv) + " " + (NumberUtils.isDigits(student._attendno) ? StringUtils.repeat(" ", 2 - String.valueOf(Integer.parseInt(student._attendno)).length()) + String.valueOf(Integer.parseInt(student._attendno)) : StringUtils.defaultString(student._attendno));
        } else {
            hrname = student._hrclass + "-" + (null == student._attendno ? "" : student._attendno);
        }
        svf.VrsOutn("HR_CLASS_NO", si, hrname);
        svf.VrsOutn("IN_STUDENT", si, ("0".equals(student._inoutCd) ? "M" : ""));
        if ("1".equals(_param._useLc_Hrclass)) {
            svf.VrsOutn("LC_CLASS", si, student._lcClassAbbv);
        }

        String showName = _param._staffInfo.getStrEngOrJp(student._name, student._nameEng);
        final String name = StringUtils.defaultString(showName) + StringUtils.defaultString(student._scholarshipAbbv1);
        final int nameLength = getMS932ByteCount(name);
        final String nameField;
        final int fieldKeta;
        if (nameLength > 16) {
            nameField = "NAME3";
            fieldKeta = 20;
        } else if (nameLength > 12) {
            nameField = "NAME2";
            fieldKeta = 16;
        } else {
            nameField = "NAME";
            fieldKeta = 12;
        }

        svf.VrsOutn(nameField, si, StringUtils.defaultString(showName) + StringUtils.repeat(" ", fieldKeta - nameLength)  + StringUtils.defaultString(student._scholarshipAbbv1));

        for (int subi = 0; subi < subclasscds.size(); subi++) {
            final String subclasscd = subclasscds.get(subi);
            final Rank rank = student._ranks.get(subclasscd);
            if (null == rank) {
                continue;
            }
            svf.VrsOutn("SUBCLASS_SCORE" + (subi + 1), si, rank._score);
        }
        if (isLastSubclassPage) {
            final Rank rank = student._ranks.get(_param.getTotalSubclasscd());
            if (null != rank) {
                svf.VrsOutn("SUBCLASS_SCORE_TOTAL", si, rank._score);
                svf.VrsOutn("SUBCLASS_SCORE_TOTAL_AVERAGE", si, rank._avg);
                svf.VrsOutn("CLASS_RANK", si, nullBlank(rank._classRank));
                svf.VrsOutn("MALE_FEMALE_RANK", si, nullBlank(student._rankBySex));
            }
        }
    }

    private final List<List<Student>> getPageStudentListList(final List<Student> students) {
        final List<List<Student>> list = new ArrayList<List<Student>>();
        if (students.size() < COUNT1) {
            list.add(students);
        } else {
            list.add(students.subList(0, COUNT1));

            List<Student> nokori = students.subList(COUNT1, students.size());
            while (nokori.size() > COUNT2) {
                list.add(nokori.subList(0, COUNT2));
                nokori = nokori.subList(COUNT2, nokori.size());
            }
            list.add(nokori);
        }
        return list;
    }

    private List<Student> getStudentList(DB2UDB db2, final String selected, final String gradeHr) {

        final Map<String, Student> studentMap = new HashMap();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = Student.sqlStudent(_param, selected, gradeHr);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String scholarshipAbbv1 = _param._isTokiwagi && null != rs.getString("SCHOLARSHIP_ABBV1") ? "(" + rs.getString("SCHOLARSHIP_ABBV1") + ")" : null;
                final String lcAbbv = "1".equals(_param._useLc_Hrclass) ? StringUtils.defaultString(rs.getString("LC_NAMEABBV"), "") : "";
                final Student student = new Student(rs.getString("SCHREGNO"), rs.getString("NAME"), rs.getString("NAME_ENG"), rs.getString("HR_CLASS"), rs.getString("HR_NAMEABBV"), rs.getString("HR_CLASS_NAME1"), rs.getString("COURSE"), rs.getString("ATTENDNO"), rs.getString("SEX"), rs.getString("INOUTCD"), scholarshipAbbv1, lcAbbv);
                studentMap.put(rs.getString("SCHREGNO"), student);
            }

        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }

        try {
            final String sql = Rank.sqlRank(_param, gradeHr);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {

                final Student student = studentMap.get(rs.getString("SCHREGNO"));
                if (null == student) {
                    continue;
                }
                final String subclasscd;
                if ("1".equals(_param._useCurriculumcd) && !(_333333.equals(rs.getString("SUBCLASSCD")) || _555555.equals(rs.getString("SUBCLASSCD")) || _999999.equals(rs.getString("SUBCLASSCD")) || _99999A.equals(rs.getString("SUBCLASSCD")) || _99999B.equals(rs.getString("SUBCLASSCD")))) {
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

        if (_param._isKwansei && "H".equals(_param._schoolKind)) {
            // 関西学院高校は合計点の代わりに総点を表示する
            final String totalSubclasscd = _param.getTotalSubclasscd();
            for (final Student student : studentMap.values()) {
                student._ranks.remove(totalSubclasscd);
            }
            final RecordRankSdivSoutenDat.Store store = RecordRankSdivSoutenDat.Store.load(db2, _param._year, _param._semester + _param._testcd, "9".equals(_param._semester) ? _param._ctrlSeme : _param._semester, _param._grade);
            for (final Student student : studentMap.values()) {
                final RecordRankSdivSoutenDat recordRankSdivSoutenDat = store.getRecordRankSdivSoutenDat(student._schregno, totalSubclasscd);
                if (null == recordRankSdivSoutenDat) {
                    continue;
                }
                final String score = nullBlank(recordRankSdivSoutenDat._totalPoint); // 総点
                final String avg = recordRankSdivSoutenDat.getTotalAvg(1); // 総点 / 単位数合計平均
                final Integer gradeRank = recordRankSdivSoutenDat._gradeRank; // 総点による学年順位
                final Integer classRank = recordRankSdivSoutenDat._classRank; // 総点によるクラス順位
                final Integer courseRank = recordRankSdivSoutenDat._courseRank; // 総点によるコース順位
                final Integer courseGroupRank = null;
                student._ranks.put(totalSubclasscd, new Rank(totalSubclasscd, score, avg, gradeRank, classRank, courseRank, courseGroupRank));
            }
        }

        setRankBySex(studentMap.values());

        final List<Student> rtn = new ArrayList<Student>();
        rtn.addAll(studentMap.values());

        Collections.sort(rtn, new StudentSorter(_param));

        return rtn;
    }


    // 性別ごとに順位をセット
    private void setRankBySex(final Collection<Student> students) {
        final Set<String> sexes = new HashSet<String>();
        for (final Student student : students) {
            if (null != student._sex) {
                sexes.add(student._sex);
            }
        }

        // 性別ごとにリスト
        final Map<String, List<Student>> sexStudentListMap = new HashMap<String, List<Student>>(); // Map<sex, List<Student>>
        for (final Student student : students) {
            if (null == student._sex) {
                continue;
            }
            if (null == sexStudentListMap.get(student._sex)) {
                sexStudentListMap.put(student._sex, new ArrayList());
            }
            final List<Student> studentGroup = sexStudentListMap.get(student._sex);
            studentGroup.add(student);
        }

        for (final List<Student> studentGroup : sexStudentListMap.values()) {

            final Map<Integer, List<Student>> rankStudentMap = new TreeMap<Integer, List<Student>>(); // Map<rank, List<Student>>
            final Integer max = new Integer(Integer.MAX_VALUE);

            // 総点順位ごとにリスト
            for (final Student student : studentGroup) {
                Integer rank = student.getTotalRank(_param);
                if (null == rank) {
                    rank = max;
                }
                if (null == rankStudentMap.get(rank)) {
                    rankStudentMap.put(rank, new ArrayList());
                }
                final List<Student> rankStudentList = rankStudentMap.get(rank);
                rankStudentList.add(student);
            }

            int iRankBySex = 1; // 性別ごとの順位（1位から開始）
            for (final Integer rank : rankStudentMap.keySet()) {
                final List<Student> rankStudentList = rankStudentMap.get(rank);
                final Integer rankBySex = new Integer(iRankBySex);

                // 同一の総点順位の生徒に同一の性別ごとの順位をセット
                for (final Student student : rankStudentList) {
                    student._rankBySex = rankBySex;
                }
                iRankBySex += rankStudentList.size();
            }
        }
    }


    private boolean printAvg(final Vrw32alp svf, final Map<String, HrclassAvg> hrclassMap, final List<String> subclasscds, final boolean isLastSubclassPage) {
        for (final String hrclasscd : hrclassMap.keySet()) {
            final HrclassAvg hrclass = hrclassMap.get(hrclasscd);
            if (Gakunen.equals(hrclass._hrclass) || hrclass._idx > HR_MAX) {
                continue;
            }
            svf.VrsOutn("HR_NAME", hrclass._idx, hrclass._hrname + " 平均点");
        }

        boolean hasData = false;
        for (int si = 0; si < subclasscds.size(); si++) {
            final String subclasscd = subclasscds.get(si);

            printHrAverage(svf, hrclassMap, "SUBCLASS_AVERAGE", String.valueOf(si + 1), subclasscd);

            hasData = true;
        }

        if (isLastSubclassPage) {
            printHrAverage(svf, hrclassMap, "SUBCLASS_TOTAL", "", _param.getTotalSubclasscd());

//            if ("J".equals(_param._schoolKind)) {
                for (final String hrclasscd : hrclassMap.keySet()) {
                    final HrclassAvg hrclass = hrclassMap.get(hrclasscd);
                    if (!Gakunen.equals(hrclass._hrclass) && hrclass._idx > HR_MAX) {
                        continue;
                    }

                    BigDecimal avgtotal = new BigDecimal(0);
                    int subclassCount = 0;
                    for (int si = 0; si < subclasscds.size(); si++) {
                        final String subclasscd = subclasscds.get(si);

                        final Average average = hrclass._subclassAvg.get(subclasscd);
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
//            }
        }


        return hasData;
    }

    private void printHrAverage(final Vrw32alp svf, final Map<String, HrclassAvg> hrclassMap, final String field, final String si, final String subclasscd) {
        for (final String hrclasscd : hrclassMap.keySet()) {
            final HrclassAvg hrclass = hrclassMap.get(hrclasscd);
            if (Gakunen.equals(hrclass._hrclass) || hrclass._idx > HR_MAX) {
                continue;
            }
            final Average average = hrclass._subclassAvg.get(subclasscd);
            if (null != average) {
                svf.VrsOutn(field + si, hrclass._idx, average._avg);
            }
        }
        final HrclassAvg gakunen = hrclassMap.get(Gakunen);
        final Average averageGakunen = gakunen._subclassAvg.get(subclasscd);
        if (null != averageGakunen) {
            svf.VrsOutn(field + si, HR_MAX + 1, averageGakunen._avg);        // 平均点
            svf.VrsOutn(field + si, HR_MAX + 2, averageGakunen._stddev);     // 標準偏差
            svf.VrsOutn(field + si, HR_MAX + 3, averageGakunen._highscore);  // 最高点
            svf.VrsOutn(field + si, HR_MAX + 4, averageGakunen._lowscore);   // 最低点
        }
    }


    private Map<String, HrclassAvg> getHrclass(final DB2UDB db2, final String selected, final String gradeHr) {
        final Map<String, HrclassAvg> rtn = new TreeMap();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = HrclassAvg.sqlHrclass(_param, selected, gradeHr);
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
        _useForm_1_2 = "2".equals(_param._printDiv) && rtn.size() > HR_MAX6 ? true : false;
        final int gakunen_idx = _useForm_1_2 ? HR_MAX11 + 1 : HR_MAX6 + 1;
        final HrclassAvg gakunen = new HrclassAvg(Gakunen, "GAKUNEN", gakunen_idx);
        rtn.put(gakunen._hrclass, gakunen);

        try {
            final String sql = Average.sqlAverage(_param, selected);
            log.info(" average sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {

                HrclassAvg hrclass = null;
                final String avgdiv = rs.getString("AVG_DIV");
                if ("1".equals(avgdiv) || "1".equals(_param._use_school_detail_gcm_dat) && "4".equals(avgdiv)) {
                    hrclass = gakunen;
//                    } else if (GROUP_COURSEGROUP.equals(_param._groupdiv)) {
//                        if ("5".equals(avgdiv)) { // コースグループ順位
//                            hrclass = (HrclassAvg) rtn.get(rs.getString("HR_CLASS"));
//                        }
                } else {
                    if ("2".equals(avgdiv)) {
                        hrclass = rtn.get(rs.getString("HR_CLASS"));
                    }
                }
                if (null == hrclass) {
                    continue;
                }

                final String subclasscd;
                if ("1".equals(_param._useCurriculumcd) && !(_333333.equals(rs.getString("SUBCLASSCD")) || _555555.equals(rs.getString("SUBCLASSCD")) || _999999.equals(rs.getString("SUBCLASSCD")) || _99999A.equals(rs.getString("SUBCLASSCD")) || _99999B.equals(rs.getString("SUBCLASSCD")))) {
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
        final Map<String, Average> _subclassAvg = new HashMap<String, Average>();
        HrclassAvg(final String hrclass, final String hrname, final int idx) {
            _hrclass = hrclass;
            _hrname = hrname;
            _idx = idx;
        }


        private static String sqlHrclass(final Param param, final String selected, final String gradeHr) {
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
            if ("1".equals(param._printDiv)) {
                stb.append("     AND T1.GRADE || '-' || T1.HR_CLASS = '" + gradeHr + "' ");
            }
            if ("1".equals(param._use_school_detail_gcm_dat)) {
                stb.append("     AND T2.COURSECD || '-' || T2.MAJORCD = '" + param._major + "' ");
            }
            stb.append(" ORDER BY ");
            stb.append("     T1.GRADE, T1.HR_CLASS ");
            return stb.toString();
        }
    }

    private static class Student {
        final String _schregno;
        final String _hrclass;
        final String _hrNameabbv;
        final String _hrclassName1;
        final String _course;
        final String _attendno;
        final String _name;
        final String _nameEng;
        final String _sex;
        final String _scholarshipAbbv1;
        final String _inoutCd;
        final String _lcClassAbbv;
        Integer _rankBySex = null;
        final Map<String, Rank> _ranks = new HashMap();
        public Student(final String schregno, final String name, final String nameEng, final String hrclass, final String hrNameabbv, final String hrclassName1, final String course, final String attendno, final String sex, final String inoutCd, final String scholarshipAbbv1, final String lcClassAbbv) {
            _schregno = schregno;
            _name = name;
            _nameEng = nameEng;
            _hrclass = hrclass;
            _hrNameabbv = hrNameabbv;
            _hrclassName1 = hrclassName1;
            _course = course;
            _attendno = attendno;
            _sex = sex;
            _inoutCd = inoutCd;
            _scholarshipAbbv1 = scholarshipAbbv1;
            _lcClassAbbv = lcClassAbbv;
        }
        /**
         * 総点の順位
         */
        public Integer getTotalRank(final Param param) {
            final Rank rank = _ranks.get(param.getTotalSubclasscd());
            if (null == rank) {
                return null;
            }
            if (GROUP_GRADE.equals(param._groupdiv)) {
                if ("1".equals(param._use_school_detail_gcm_dat)) {
                    return rank._majorRank;
                }
                return rank._gradeRank;
            } else if (GROUP_COURSE.equals(param._groupdiv)) {
                return rank._courseRank;
            } else if (GROUP_COURSEGROUP.equals(param._groupdiv)) {
                return rank._courseGroupRank;
            }
            return null;
        }

        private static String sqlStudent(final Param param, final String selected, final String gradeHr) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T2.NAME, ");
            stb.append("     T2.NAME_ENG, ");
            stb.append("     T1.GRADE, ");
            stb.append("     T1.HR_CLASS, ");
            stb.append("     REGDH.HR_NAMEABBV, ");
            stb.append("     REGDH.HR_CLASS_NAME1, ");
            stb.append("     T1.COURSECD || T1.MAJORCD || T1.COURSECODE AS COURSE, ");
            stb.append("     T1.ATTENDNO, ");
            stb.append("     T2.SEX, ");
            stb.append("     T2.INOUTCD ");
            if (param._isTokiwagi) {
                stb.append("     , NMA044.ABBV1 AS SCHOLARSHIP_ABBV1 ");
            }
            if ("1".equals(param._useLc_Hrclass)) {
                stb.append("     , SRLH.LC_NAMEABBV AS LC_NAMEABBV ");
            }
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT T1 ");
            stb.append("     INNER JOIN  SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            if (GROUP_COURSEGROUP.equals(param._groupdiv)) {
                stb.append("     INNER JOIN COURSE_GROUP_CD_DAT T3 ON T3.YEAR = T1.YEAR ");
                stb.append("       AND T3.GRADE = T1.GRADE ");
                stb.append("       AND T3.COURSECD || T3.MAJORCD || T3.COURSECODE = T1.COURSECD || T1.MAJORCD || T1.COURSECODE ");
            }
            stb.append("     INNER JOIN SCHREG_REGD_HDAT REGDH ON REGDH.YEAR = T1.YEAR ");
            stb.append("       AND REGDH.SEMESTER = T1.SEMESTER ");
            stb.append("       AND REGDH.GRADE = T1.GRADE ");
            stb.append("       AND REGDH.HR_CLASS = T1.HR_CLASS ");
            if (param._isTokiwagi) {
                stb.append("     LEFT JOIN SEMESTER_MST SEME ON SEME.YEAR = T1.YEAR AND SEME.SEMESTER = T1.SEMESTER ");
                stb.append("     LEFT JOIN SCHREG_SCHOLARSHIP_HIST_DAT TSCHOL ON TSCHOL.SCHREGNO = T1.SCHREGNO ");
                stb.append("          AND ( ");
                stb.append("              TSCHOL.FROM_DATE BETWEEN SEME.SDATE AND SEME.EDATE ");
                stb.append("           OR TSCHOL.TO_DATE BETWEEN SEME.SDATE AND SEME.EDATE ");
                stb.append("           OR SEME.SDATE BETWEEN TSCHOL.FROM_DATE AND VALUE(TSCHOL.TO_DATE, '9999-12-31') ");
                stb.append("           OR SEME.EDATE BETWEEN TSCHOL.FROM_DATE AND VALUE(TSCHOL.TO_DATE, '9999-12-31') ");
                stb.append("              ) ");
                stb.append("          AND TSCHOL.SCHOLARSHIP IS NOT NULL ");
                stb.append("     LEFT JOIN V_NAME_MST NMA044 ON NMA044.YEAR = T1.YEAR ");
                stb.append("          AND NMA044.NAMECD1 = 'A044' ");
                stb.append("          AND NMA044.NAMECD2 = TSCHOL.SCHOLARSHIP ");
            }
            if ("1".equals(param._useLc_Hrclass)) {
                stb.append("     LEFT JOIN SCHREG_REGD_LC_DAT SRLD ON SRLD.SCHREGNO = T1.SCHREGNO ");
                stb.append("          AND SRLD.YEAR = T1.YEAR ");
                stb.append("          AND SRLD.SEMESTER = T1.SEMESTER ");
                stb.append("     LEFT JOIN SCHREG_REGD_LC_HDAT SRLH ON SRLH.YEAR = SRLD.YEAR ");
                stb.append("          AND SRLH.SEMESTER = SRLD.SEMESTER ");
                stb.append("          AND SRLH.GRADE = SRLD.GRADE ");
                stb.append("          AND SRLH.LC_CLASS = SRLD.LC_CLASS ");
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
            if ("1".equals(param._printDiv)) {
                stb.append("     AND T1.GRADE || '-' || T1.HR_CLASS = '" + gradeHr + "' ");
            }
            if ("1".equals(param._use_school_detail_gcm_dat)) {
                stb.append("     AND T1.COURSECD || '-' || T1.MAJORCD = '" + param._major + "' ");
            }
            return stb.toString();
        }
    }


    private static class StudentSorter implements Comparator<Student> {
        // 科目コード999999の順位でソート。順位がなければHR出欠番号順。
        final Param _param;
        StudentSorter(final Param param) {
            _param = param;
        }

        public int compare(final Student s1, final Student s2) {
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
            stb.append("  RECORD_AVERAGE_SDIV_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.SEMESTER = '" + param._semester + "' ");
            stb.append("     AND T1.GRADE = '" + param._grade + "' ");
            stb.append("     AND T1.AVG_DIV IN ('1', '2', '4', '5') "); // 学科、クラス、コースグループ
            stb.append("     AND T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '" + param._testcd + "' ");
            if ("3".equals(param._subclassGroupDiv)) {
                stb.append("     AND (T1.SUBCLASSCD = '333333' OR substr(T1.SUBCLASSCD, 1, 2) < '90') ");
            } else if ("5".equals(param._subclassGroupDiv)) {
                stb.append("     AND (T1.SUBCLASSCD = '555555' OR substr(T1.SUBCLASSCD, 1, 2) < '90') ");
            } else {
                stb.append("     AND (T1.SUBCLASSCD = '999999' OR substr(T1.SUBCLASSCD, 1, 2) < '90') ");
            }
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
        final Integer _majorRank;
        public Rank(final String subclasscd, final String score, final String avg, final Integer gradeRank, final Integer classRank, final Integer courseRank, final Integer majorRank) {
            _subclasscd = subclasscd;
            _score = score;
            _avg = avg;
            _gradeRank = gradeRank;
            _classRank = classRank;
            _courseRank = courseRank;
            _courseGroupRank = majorRank;
            _majorRank = majorRank;
        }

        public String toString() {
            return "Rank(" + _subclasscd + ", " + _score + ", " + _avg + ", " + _gradeRank + ", " + _classRank + ", " + _courseRank + ", " + _courseGroupRank + ", " + _majorRank + ")";
        }

        private static String sqlRank(final Param param, final String gradeHr) {
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
            if ("2".equals(param._outputKijun)) {
                stb.append("     T2.GRADE_AVG_RANK AS GRADE_RANK, ");
                stb.append("     T2.CLASS_AVG_RANK AS CLASS_RANK, ");
                stb.append("     T2.COURSE_AVG_RANK AS COURSE_RANK, ");
                stb.append("     T2.MAJOR_AVG_RANK AS COURSEGROUP_RANK ");
            } else {
                stb.append("     T2.GRADE_RANK, ");
                stb.append("     T2.CLASS_RANK, ");
                stb.append("     T2.COURSE_RANK, ");
                stb.append("     T2.MAJOR_RANK AS COURSEGROUP_RANK ");
            }
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT T1 ");
            stb.append("     LEFT JOIN RECORD_RANK_SDIV_DAT T2 ON T2.YEAR = T1.YEAR ");
            stb.append("         AND T2.SEMESTER = '" + param._semester + "' ");
            stb.append("         AND T2.SCHREGNO = T1.SCHREGNO ");
            if ("3".equals(param._subclassGroupDiv) || "5".equals(param._subclassGroupDiv)) {
                stb.append("     LEFT JOIN REC_SUBCLASS_GROUP_DAT R1 ON R1.YEAR = T1.YEAR ");
                stb.append("         AND R1.GROUP_DIV = '" + param._subclassGroupDiv + "' ");
                stb.append("         AND R1.GRADE = T1.GRADE ");
                stb.append("         AND R1.COURSECD = T1.COURSECD ");
                stb.append("         AND R1.MAJORCD = T1.MAJORCD ");
                stb.append("         AND R1.COURSECODE = T1.COURSECODE ");
                stb.append("         AND R1.CLASSCD = T2.CLASSCD ");
                stb.append("         AND R1.SCHOOL_KIND = T2.SCHOOL_KIND ");
                stb.append("         AND R1.CURRICULUM_CD = T2.CURRICULUM_CD ");
                stb.append("         AND R1.SUBCLASSCD = T2.SUBCLASSCD ");
                stb.append("     LEFT JOIN REC_SUBCLASS_GROUP_MST R2 ON R2.YEAR = R1.YEAR ");
                stb.append("         AND R2.GROUP_DIV = R1.GROUP_DIV ");
                stb.append("         AND R2.GRADE = R1.GRADE ");
                stb.append("         AND R2.COURSECD = R1.COURSECD ");
                stb.append("         AND R2.MAJORCD = R1.MAJORCD ");
                stb.append("         AND R2.COURSECODE = R1.COURSECODE ");
            }
            stb.append(" WHERE ");
            stb.append("     T1.GRADE = '" + param._grade + "' ");
            stb.append("     AND T1.YEAR = '" + param._year + "' ");
            if ("9".equals(param._semester)) {
                stb.append("     AND T1.SEMESTER = '" + param._ctrlSeme+ "' ");
            } else {
                stb.append("     AND T1.SEMESTER = '" + param._semester + "' ");
            }
            stb.append("     AND T2.TESTKINDCD || T2.TESTITEMCD || T2.SCORE_DIV = '" + param._testcd + "' ");
            if ("1".equals(param._printDiv)) {
                stb.append("     AND T1.GRADE || '-' || T1.HR_CLASS = '" + gradeHr + "' ");
            }
            if ("1".equals(param._use_school_detail_gcm_dat)) {
                stb.append("     AND T1.COURSECD || '-' || T1.MAJORCD = '" + param._major + "' ");
            }
            if ("3".equals(param._subclassGroupDiv)) {
                stb.append("     AND (T2.SUBCLASSCD = '333333' OR substr(T2.SUBCLASSCD, 1, 2) < '90' AND R1.SUBCLASSCD IS NOT NULL) ");
            } else if ("5".equals(param._subclassGroupDiv)) {
                stb.append("     AND (T2.SUBCLASSCD = '555555' OR substr(T2.SUBCLASSCD, 1, 2) < '90' AND R1.SUBCLASSCD IS NOT NULL) ");
            } else {
                stb.append("     AND (T2.SUBCLASSCD = '999999' OR substr(T2.SUBCLASSCD, 1, 2) < '90') ");
            }
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

        private static List<Group> getGroupList(final DB2UDB db2, final Param param, final String cd) {
            final List<Group> list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = sqlGroup(param, cd);
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

        private static String sqlGroup(final Param param, final String cd) {
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
            if ("1".equals(param._printDiv)) {
                stb.append("     AND T1.GRADE || '-' || T1.HR_CLASS = '" + cd + "' ");
            } else if ("3".equals(param._printDiv)) {
                stb.append("     AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE = '" + cd + "' ");
            }
            if ("1".equals(param._use_school_detail_gcm_dat)) {
                stb.append("     AND T1.COURSECD || '-' || T1.MAJORCD = '" + param._major + "' ");
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
        final String _testcd2keta;
        final String _printDiv;
        final String _date;
        final Map<String, String> _subclassnames;
        final String _outputKijun;
        final String _groupdiv; // 1:学年 2:コース
        final String _sortdiv;
        final String _useCurriculumcd;
        final String _subclassGroupDiv;
        String _testitemname = "";
        String _semestername = "";
        String _gradename = "";
        final String _schoolKind;
        final String[] _selectData;
        final List<String> _notTargetSubclassCdList;
        final String _major;
        final String _use_school_detail_gcm_dat;
        final String _useSchool_KindField;
        final String SCHOOLKIND;
        final String SCHOOLCD;
        final boolean _isSundaikoufu;
        final boolean _isTokiwagi;
        final String _useLc_Hrclass;
        final String _staffCd;
        final StaffInfo _staffInfo;
        final boolean _isKwansei;

        Param(final HttpServletRequest request, final DB2UDB db2) {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _ctrlSeme = request.getParameter("CTRL_SEME");
            _grade = request.getParameter("GRADE");
            _testcd = request.getParameter("TESTCD");
            _testcd2keta = _testcd != null ? _testcd.substring(0, 2) : "";
            _printDiv = request.getParameter("PRINT_DIV");
            _date = request.getParameter("DATE");
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _subclassGroupDiv = request.getParameter("SUBCLASS_GROUP_DIV");
            _subclassnames = getSubclassname(db2);
            _major = request.getParameter("MAJOR");
            _use_school_detail_gcm_dat = request.getParameter("use_school_detail_gcm_dat");
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            SCHOOLKIND = request.getParameter("SCHOOLKIND");
            SCHOOLCD = request.getParameter("SCHOOLCD");
            setName(db2);
            _outputKijun = request.getParameter("OUTPUT_KIJUN");
            _groupdiv = request.getParameter("GROUP_DIV");
            _sortdiv = request.getParameter("SORT_DIV");
            _schoolKind = getSchoolKind(db2);
            final String z010name1 = setZ010Name1(db2);
            _isSundaikoufu = "sundaikoufu".equals(z010name1);
            _isTokiwagi = "tokiwagi".equals(z010name1);
            _selectData = request.getParameterValues("CATEGORY_SELECTED");  //PRINT_DIV='1' 学年-組、PRINT_DIV='3' コース
            _notTargetSubclassCdList = getNotTargetSubclassCdList(db2);
            _useLc_Hrclass = request.getParameter("useLc_Hrclass");
            _staffCd = request.getParameter("PRINT_LOG_STAFFCD");
            _staffInfo = new StaffInfo(db2, _staffCd);
            final String z010 = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' "));
            _isKwansei = "kwansei".equals(z010);
        }

        private String getTotalSubclasscd() {
            if ("3".equals(_subclassGroupDiv)) {
                return _333333;
            } else if ("5".equals(_subclassGroupDiv)) {
                return _555555;
            }
            return _999999;
        }

        /**
         * 名称マスタ NAMECD1='Z010' NAMECD2='00'読込
         */
        private String setZ010Name1(DB2UDB db2) {
            String name1 = KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' "));
            return name1;
        }

        private void setName(final DB2UDB db2) {
            String sql = "";
            if ("1".equals(_use_school_detail_gcm_dat)) {
                sql = "SELECT TESTITEMNAME FROM TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV WHERE YEAR = '" + _year + "'" +
                        " AND SEMESTER = '" + _semester + "' " +
                        " AND GRADE = '00' " +
                        " AND COURSECD || '-' || MAJORCD = '" + _major + "' " +
                        " AND TESTKINDCD || TESTITEMCD || SCORE_DIV = '" + _testcd + "' ";
                if ("1".equals(_useSchool_KindField) && !StringUtils.isBlank(SCHOOLKIND)) {
                    sql += " AND SCHOOLCD = '" + SCHOOLCD + "' " +
                           " AND SCHOOL_KIND = '" + SCHOOLKIND + "' ";
                }
            } else {
                sql += " SELECT TESTITEMNAME FROM TESTITEM_MST_COUNTFLG_NEW_SDIV ";
                sql += " WHERE YEAR = '" + _year + "' AND SEMESTER = '" + _semester + "' AND TESTKINDCD || TESTITEMCD || SCORE_DIV = '" + _testcd + "' ";
            }
            _testitemname = nullBlank(KnjDbUtils.getOne(KnjDbUtils.query(db2, sql)));

            _semestername = nullBlank(KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT SEMESTERNAME FROM SEMESTER_MST WHERE YEAR = '" + _year + "' AND SEMESTER = '" + _semester + "' ")));

            _gradename = nullBlank(KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT GRADE_NAME1 FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND GRADE ='" + _grade + "' ")));
        }

        private String getSchoolKind(final DB2UDB db2) {
            String rtn = nullBlank(KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND GRADE = '" + _grade + "' ")));
            return rtn;
        }

        private Map getSubclassname(final DB2UDB db2) {
            final Map rtn = KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, sqlSubclass()), "SUBCLASSCD", "SUBCLASSNAME");
            return rtn;
        }

        private String sqlSubclass() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append("     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || ");
            }
            stb.append("     SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("     VALUE(SUBCLASSABBV, SUBCLASSNAME) AS SUBCLASSNAME ");
            stb.append(" FROM ");
            stb.append("     SUBCLASS_MST ");
            return stb.toString();
        }

        private List<String> getNotTargetSubclassCdList(final DB2UDB db2) {
            final List<String> rtnList = new ArrayList<String>();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = sqlNotTargetSubclassCd();
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
            log.info(" notTargetSubclassCdList = " + rtnList);
            return rtnList;
        }

        private String sqlNotTargetSubclassCd() {
            final StringBuffer stb = new StringBuffer();
            stb.append("   SELECT ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append("       T1.ATTEND_CLASSCD || '-' || T1.ATTEND_SCHOOL_KIND || '-' || T1.ATTEND_CURRICULUM_CD || '-' || ");
            }
            stb.append("       T1.ATTEND_SUBCLASSCD AS ATTEND_SUBCLASSCD, ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append("       T1.COMBINED_CLASSCD || '-' || T1.COMBINED_SCHOOL_KIND || '-' || T1.COMBINED_CURRICULUM_CD || '-' || ");
            }
            stb.append("       T1.COMBINED_SUBCLASSCD AS COMBINED_SUBCLASSCD ");
            stb.append("   FROM ");
            stb.append("       SUBCLASS_REPLACE_COMBINED_DAT T1 ");
            stb.append("   WHERE ");
            stb.append("       T1.YEAR = '" + _year + "' ");
            return stb.toString();
        }
    }
}
