// kanji=漢字
/*
 * 作成日: 2007/05/14
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJH;

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
import java.util.TreeSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

/**
 * 成績一覧表（明治中高）
 * @author maesiro
 */
public class KNJH565 {
    private static final Log log = LogFactory.getLog(KNJH565.class);

    private final String Gakunen = "000";

    private static final String _999999 = "999999";

    // 1:学年 2:コース 3:コースグループ
    private static final String GROUP_GRADE = "1";
    private static final String GROUP_HR = "2";
    private static final String GROUP_COURSE = "3";
    private static final String GROUP_MAJOR = "4";
    private static final String GROUP_COURSEGROUP = "5";

    private static final String AVG_DATA_SCORE = "1";
    private static final String AVG_DATA_KEISHA = "2";

    private static final String RANK_DATA_DIV_SCORE = "01";
    private static final String RANK_DATA_DIV_AVG = "02";
    private static final String RANK_DATA_DIV_DEVIATION = "03";
    private static final String RANK_DATA_DIV_KEISHA = "11";

    private static final String RANK_DIV_GRADE = "01";
    private static final String RANK_DIV_HRCLASS = "02";
    private static final String RANK_DIV_COURSE = "03";
    private static final String RANK_DIV_MAJOR = "04";
    private static final String RANK_DIV_COURSEGROUP = "05";

    private static final String AVG_DIV_GRADE = "01";
    private static final String AVG_DIV_HR_CLASS = "02";
    private static final String AVG_DIV_COURSE = "03";
    private static final String AVG_DIV_MAJOR = "04";
    private static final String AVG_DIV_COURSEGROUP = "05";

    private static final String SORT_DIV_ATTENDNO = "1";
    private static final String SORT_DIV_RANK = "2";

    // 平均点一覧HR最大表示数
    private static final int HR_MAX  = 8;
    private static final int HR_MAX2 = 46;

    private final int COUNT1 = 45;  // 1枚目45名
    private final int COUNT2 = 50; // 2枚目以降50名

    private Param _param;

    private boolean _hasData;

    /**
     *  KNJH.classから最初に起動されます。
     */
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws ServletException, IOException {
        log.fatal("$Revision: 67345 $ $Date: 2019-05-07 18:09:11 +0900 (火, 07 5 2019) $"); // CVSキーワードの取り扱いに注意

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

            if (!GROUP_HR.equals(_param._formGroupdiv)) {
                final List groupList = Group.getGroupList(db2, _param);
                log.debug(" groupList = " + groupList);
                for (final Iterator it = groupList.iterator(); it.hasNext();) {
                    final Group group = (Group) it.next();
                    printMain(db2, svf, group);
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

    static String nullBlank(final Object o) {
        return null == o ? null : o.toString();
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf, final Group group) {

        boolean hasData = false;

        final Map hrclassMap = getHrclass(db2, group._code);
//        log.debug(" hrclassMap = " + hrclassMap);

        final Set attendSubclasses = getAttendSubclasses(db2, group._code);
        log.debug(" attendSubclasses = " + attendSubclasses);

        final List subclasscdsAll = getSubclasscds(db2, group._code);
//        log.debug(" subclasscdsAll = " + subclasscdsAll);
        subclasscdsAll.removeAll(attendSubclasses);

        final List students = getStudentList(db2, group._code);
        if (students.isEmpty()) {
            return;
        }

        final List pageStudentListList = getPageStudentListList(students);
        final int totalPage = pageStudentListList.size();
        final List pageSubclassListList = getSubclasscdPageList(subclasscdsAll, 10);

        for (int pi = 0; pi < totalPage; pi++) {

            final List pageStudentList = (List) pageStudentListList.get(pi);

            for (int spi = 0; spi < pageSubclassListList.size(); spi++) {
                final List subclasscds = (List) pageSubclassListList.get(spi);

                if (pi != 0) {
                    svf.VrSetForm("KNJH565_2.frm", 1);
                } else {
                    if (!_param._isMeiji) {
                        svf.VrSetForm("KNJH565_1_2.frm", 4);//明治以外は、一枚目はクラス別平均点一覧表のみ
                    } else {
                        svf.VrSetForm("KNJH565_1.frm", 4);
                    }
                }

                printHeader(db2, svf, _param, subclasscds, (pi * pageSubclassListList.size() + spi + 1), totalPage * pageSubclassListList.size(), group._name);

                for (int si = 0; si < pageStudentList.size(); si++) {
                    final Student student = (Student) pageStudentList.get(si);

                    printStudent(svf, hrclassMap, subclasscds, si + 1, student);
                }

                if (pi == 0) {
                    final HrclassAvg gakunen = (HrclassAvg) hrclassMap.get(Gakunen);

                    final List hrclasses = new ArrayList(hrclassMap.values());
                    for (int i = 0, max = hrclasses.size(); i < max; i++) {
                        final HrclassAvg hrclass = (HrclassAvg) hrclasses.get(i);
                        if (hrclass == gakunen) {
                            continue;
                        }
                        final int hrMax = (!_param._isMeiji) ? HR_MAX2: HR_MAX;
                        if (hrclass._idx > hrMax) {
                            break;
                        } else {
                            final String sfx = i == max - 1 ? "_2" : "";
                            printAvg(svf, sfx, hrclass, subclasscds, 0, null);
                            svf.VrEndRecord();
                        }
                    }

                    printAvg(svf, "", gakunen, subclasscds, 0, "平均点");
                    svf.VrEndRecord();
                    printAvg(svf, "", gakunen, subclasscds, 1, "標準偏差値");
                    svf.VrEndRecord();
                    printAvg(svf, "", gakunen, subclasscds, 2, "最高点");
                    svf.VrEndRecord();
                    printAvg(svf, "_2", gakunen, subclasscds, 3, "最低点");
                    svf.VrEndRecord();
                } else {
                    svf.VrEndPage();
                }

                hasData = true;
            }
        }

        _hasData = _hasData || hasData;
    }

    private void printHeader(final DB2UDB db2, final Vrw32alp svf, final Param param, final List subclasscds, final int page, final int totalPage, final String groupName) {

        final String nendo = KNJ_EditDate.getAutoFormatYear(db2, Integer.parseInt(_param._year)) + "年度";
        final String title = nendo + " " + _param._gradename + " " + _param._semestername + " " + _param._proficiencyname1 + "成績順一覧表";
        svf.VrsOut("TITLE", title);
        if (!GROUP_GRADE.equals(_param._formGroupdiv)) {
            svf.VrsOut("COURSE", null != groupName ? "（" + groupName + "）" : "");
        }
        svf.VrsOut("PAGE_HEADER", String.valueOf(page));
        svf.VrsOut("PAGE_FOOTER", String.valueOf(totalPage));
        svf.VrsOut("DATE", KNJ_EditDate.getAutoFormatDate(db2, _param._date));

        for (int si = 0; si < subclasscds.size(); si++) {
            final String subclasscd = (String) subclasscds.get(si);
            if (_999999.equals(subclasscd)) {
            } else {
                final String subclassname = (String) _param._subclassnames.get(subclasscd);
                svf.VrsOut("SUBCLASS_NAME" + (si + 1), subclassname);
                svf.VrsOut("SUBCLASS_NAME" + (si + 1) + "_2", subclassname);
            }
        }
        svf.VrsOut("TOTAL_NAME", "計");
        svf.VrsOut("AVERAGE_NAME", "平均");
        svf.VrsOut("REMARK_NAME", "");
        svf.VrsOut("TOTAL_NAME_2", "総点");
        svf.VrsOut("AVERAGE_NAME_2", "3".equals(param._juni) ? "偏差値" : "平均");
        svf.VrsOut("CLASS_RANK_NAME", "組順");
        svf.VrsOut("MALE_FEMALE_RANK_NAME", "男女順");
    }

    private List getSubclasscds(final DB2UDB db2, final String selected) {
        final Set subclasscdset = new TreeSet(); // 表示する科目コード
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getProficiencyDatSubclassSql(_param, selected);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                subclasscdset.add(rs.getString("PROFICIENCY_SUBCLASS_CD"));
            }
        } catch (final Exception e) {
            log.error("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        subclasscdset.add(_999999);
        final List subclasscds = new ArrayList(subclasscdset);
        return subclasscds;
    }

    private String getProficiencyDatSubclassSql(final Param param, final String selected) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT DISTINCT ");
        stb.append("     T1.PROFICIENCY_SUBCLASS_CD ");
        stb.append(" FROM ");
        stb.append("     PROFICIENCY_DAT T1 ");
        stb.append("     INNER JOIN SCHREG_REGD_DAT T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("         AND T2.YEAR = T1.YEAR ");
        stb.append("         AND T2.SEMESTER = T1.SEMESTER ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND T1.PROFICIENCYDIV = '" + _param._proficiencydiv + "' ");
        stb.append("     AND T1.PROFICIENCYCD = '" + _param._proficiencycd + "' ");
        stb.append("     AND T2.GRADE = '" + _param._grade +"' ");

        if (GROUP_HR.equals(param._formGroupdiv)) {
            stb.append("     AND T2.HR_CLASS = '" + selected + "' ");
        } else if (GROUP_COURSE.equals(param._formGroupdiv)) {
            stb.append("     AND T2.COURSECD || T2.MAJORCD || T2.COURSECODE = '" + selected + "' ");
        } else if (GROUP_MAJOR.equals(param._formGroupdiv)) {
            stb.append("     AND T2.COURSECD || T2.MAJORCD = '" + selected + "' ");
        } else if (GROUP_COURSEGROUP.equals(param._formGroupdiv)) {
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
        } else {
        }
        stb.append(" ORDER BY ");
        stb.append("     T1.PROFICIENCY_SUBCLASS_CD ");
        return stb.toString();
    }

    private Set getAttendSubclasses(final DB2UDB db2, final String selected) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        Set attendSubclass = new TreeSet();
        try {
            final String sql = getSubclassReplaceCmbSql(_param, selected);
//            log.debug(" replace_cmb sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                attendSubclass.add(rs.getString("ATTEND_SUBCLASSCD"));
            }
        } catch (final Exception e) {
            log.error("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return attendSubclass;
    }

    /** 合併科目取得SQL */
    private String getSubclassReplaceCmbSql(final Param param, final String selected) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT DISTINCT ");
        stb.append("     T1.ATTEND_SUBCLASSCD ");
        stb.append(" FROM ");
        stb.append("     PROFICIENCY_SUBCLASS_REPLACE_COMB_DAT T1 ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND T1.PROFICIENCYDIV = '" + _param._proficiencydiv + "' ");
        stb.append("     AND T1.PROFICIENCYCD = '" + _param._proficiencycd + "' ");
        stb.append("     AND T1.GRADE = '" + _param._grade +"' ");

        if (GROUP_HR.equals(param._formGroupdiv)) {
        } else if (GROUP_COURSE.equals(param._formGroupdiv)) {
            stb.append("     AND ((T1.DIV = '03' AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE = '" + selected + "') ");
            stb.append("      OR  (T1.DIV = '04' AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE IN ( ");
            stb.append("        SELECT ");
            stb.append("            '0' || T3.GROUP_CD || '0000' ");
            stb.append("        FROM ");
            stb.append("            COURSE_GROUP_CD_DAT T3 ");
            stb.append("        WHERE ");
            stb.append("            T3.YEAR = '" + _param._year + "' ");
            stb.append("            AND T3.GRADE = '" + _param._grade +"' ");
            stb.append("            AND T3.COURSECD || T3.MAJORCD || T3.COURSECODE = '" + selected + "' ");
            stb.append("     ))) ");
        } else if (GROUP_MAJOR.equals(param._formGroupdiv)) {
        } else if (GROUP_COURSEGROUP.equals(param._formGroupdiv)) {
            stb.append("     AND ((T1.DIV = '04' AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE = '0" + selected + "0000') ");
            stb.append("      OR  (T1.DIV = '03' AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE IN ( ");
            stb.append("        SELECT ");
            stb.append("            T3.COURSECD || T3.MAJORCD || T3.COURSECODE ");
            stb.append("        FROM ");
            stb.append("            COURSE_GROUP_CD_DAT T3 ");
            stb.append("        WHERE ");
            stb.append("            T3.YEAR = '" + _param._year + "' ");
            stb.append("            AND T3.GRADE = '" + _param._grade +"' ");
            stb.append("            AND T3.GROUP_CD = '" + selected + "' ");
            stb.append("     ))) ");
        } else {
        }
        stb.append(" ORDER BY ");
        stb.append("     T1.ATTEND_SUBCLASSCD ");
        return stb.toString();
    }

    private void printStudent(final Vrw32alp svf, final Map hrclassMap, final List subclasscds, final int si, final Student student) {
        final Integer gradeRank = student.getTotalRank(_param);
        svf.VrsOutn("RANK", si, null == gradeRank ? "" : gradeRank.toString());
        final String hrname = student._hrclass + "-" + (null == student._attendno ? "" : student._attendno);
        svf.VrsOutn("HR_CLASS_NO", si, hrname);
        svf.VrsOutn("IN_STUDENT", si, ("0".equals(student._inoutCd) ? "M" : ""));
        final int nameLength = KNJ_EditEdit.getMS932ByteLength(student._name);
        final String nameField = "NAME" + (nameLength > 30 ? "6" : nameLength > 24 ? "5" : nameLength > 20 ? "4" : nameLength > 16 ? "3" : nameLength > 12 ? "2" : "");
        svf.VrsOutn(nameField, si, student._name);

        for (int subi = 0; subi < subclasscds.size(); subi++) {
            final String subclasscd = (String) subclasscds.get(subi);
            final Rank rank = (Rank) student._ranks.get(subclasscd);
            if (null == rank) {
                continue;
            }
            if (_999999.equals(subclasscd)) {
                svf.VrsOutn("SUBCLASS_SCORE_TOTAL", si, rank._score);
                svf.VrsOutn("SUBCLASS_SCORE_TOTAL_AVERAGE", si, "3".equals(_param._juni) ? student.getDeviation(_param) : rank._avg);
                svf.VrsOutn("CLASS_RANK", si, nullBlank(rank._classRank));
                svf.VrsOutn("MALE_FEMALE_RANK", si, nullBlank(student._rankBySex));

            } else {
                svf.VrsOutn("SUBCLASS_SCORE" + (subi + 1), si, rank._score);
            }
        }
    }

    private final List getSubclasscdPageList(final List subclasscdlist, final int max) {
        final List rtn = new ArrayList();
        List current = null;
        String subclasscd999999 = null;
        for (final Iterator it = subclasscdlist.iterator(); it.hasNext();) {
            final String subclasscd = (String) it.next();
            if (_999999.equals(subclasscd)) {
                subclasscd999999 = subclasscd;
                continue;
            }
            if (null == current || current.size() >= max) {
                current = new ArrayList();
                rtn.add(current);
            }
            current.add(subclasscd);
        }
        if (null != subclasscd999999) {
            for (final Iterator it = rtn.iterator(); it.hasNext();) {
                final List subList = (List) it.next();
                subList.add(subclasscd999999);
            }
        }
        return rtn;
    }

    private final List getPageStudentListList(final List students) {
        final List list = new ArrayList();
        if (!_param._isMeiji) {
            final List blankList = new ArrayList();
            list.add(blankList);//１枚目は生徒を印刷しない
            if (students.size() < COUNT2) {
                list.add(students);
            } else {
                list.add(students.subList(0, COUNT2));

                List nokori = students.subList(COUNT2, students.size());
                while (nokori.size() > COUNT2) {
                    list.add(nokori.subList(0, COUNT2));
                    nokori = nokori.subList(COUNT2, nokori.size());
                }
                list.add(nokori);
            }
        } else {
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
                final Student student = new Student(rs.getString("SCHREGNO"), rs.getString("NAME"), rs.getString("HR_CLASS"), rs.getString("ATTENDNO"), rs.getString("SEX"), rs.getString("INOUTCD"));
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
//            log.debug(" rank sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {

                final Student student = (Student) studentMap.get(rs.getString("SCHREGNO"));
                if (null == student) {
                    continue;
                }
                final String subclasscd = rs.getString("SUBCLASSCD");
                final String score = "3".equals(_param._juni) ? rs.getString("SCORE0101") : rs.getString("SCORE");
                final String avg = avgString("3".equals(_param._juni) ? rs.getString("AVG0101") : rs.getString("AVG"));
                final Integer gradeRank = null == rs.getString("GRADE_RANK") ? null : Integer.valueOf(rs.getString("GRADE_RANK"));
                final Integer classRank = null == rs.getString("CLASS_RANK") ? null : Integer.valueOf(rs.getString("CLASS_RANK"));
                final Integer courseRank = null == rs.getString("COURSE_RANK") ? null : Integer.valueOf(rs.getString("COURSE_RANK"));
                final Integer majorRank = null == rs.getString("MAJOR_RANK") ? null : Integer.valueOf(rs.getString("MAJOR_RANK"));
                final Integer courseGroupRank = null == rs.getString("COURSEGROUP_RANK") ? null : Integer.valueOf(rs.getString("COURSEGROUP_RANK"));
                final String gradeDeviation = getAvgStr(rs.getString("GRADE_DEVIATION"));
                final String classDeviation = getAvgStr(rs.getString("CLASS_DEVIATION"));
                final String courseDeviation = getAvgStr(rs.getString("COURSE_DEVIATION"));
                final String majorDeviation = getAvgStr(rs.getString("MAJOR_DEVIATION"));
                final String courseGroupDeviation = getAvgStr(rs.getString("COURSEGROUP_DEVIATION"));
                student._ranks.put(subclasscd, new Rank(subclasscd, score, avg, gradeRank, classRank, courseRank, majorRank, courseGroupRank, gradeDeviation, classDeviation, courseDeviation, majorDeviation, courseGroupDeviation));
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

    public String avgString(final String rsAvg) {
        return null == rsAvg ? null : new BigDecimal(rsAvg).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
    }

    private static String getAvgStr(final String s) {
        if (!NumberUtils.isNumber(s)) {
            return null;
        }
        return new BigDecimal(s).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
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
                if (rank == max) {
                    continue;
                }
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


    private boolean printAvg(final Vrw32alp svf, final String sfx, final HrclassAvg hrclass, final List subclasscds0, final int div, final String head) {
        final List subclasscds = new ArrayList(subclasscds0);

        if (null != head) {
            svf.VrsOut("ST_NAME" + sfx, head);
        } else {
            svf.VrsOut("HR_NAME" + sfx, hrclass._hrname + " 平均点");
        }

        boolean hasData = false;
        for (int si = 0; si < subclasscds.size(); si++) {
            final String subclasscd = (String) subclasscds.get(si);

            final String field, s;
            if (_999999.equals(subclasscd)) {
                field = "SUBCLASS_TOTAL";
                s = "";
            } else {
                field = "SUBCLASS_AVERAGE";
                s = String.valueOf(si + 1);
            }

            final Average average = (Average) hrclass._subclassAvg.get(subclasscd);
            if (null != average) {
                final String tgt;
                if (div == 1) {
                    tgt = average._stddev;
                } else if (div == 2) {
                    tgt = average._highscore;
                } else if (div == 3) {
                    tgt = average._lowscore;
                } else {
                    tgt = average._avg;
                }
                svf.VrsOut(field + s + sfx, tgt);
            }

            hasData = true;
        }

        if (div == 0) {
            if ("J".equals(_param._schoolKind)) {
                    final List avgList = new ArrayList();
                    for (int si = 0; si < subclasscds.size(); si++) {
                        final String subclasscd = (String) subclasscds.get(si);
                        if (_999999.equals(subclasscd)) {
                            continue;
                        }
                        final Average average = (Average) hrclass._subclassAvg.get(subclasscd);
                        if (null != average && null != average._avgbd) {
                            avgList.add(average._avgbd);
                        }
                    }
                    svf.VrsOut("SUBCLASS_TOTAL_AVERAGE" + sfx, avg(avgList));
            }
        }
        return hasData;
    }

    private String avg(final List list) {
        int count = 0;
        BigDecimal sum = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final BigDecimal bd = (BigDecimal) it.next();
            if (null == sum) {
                sum = bd;
                count += 1;
            } else if (bd != null) {
                sum = sum.add(bd);
                count += 1;
            }
        }
        if (0 != count) {
            final String avg = sum.divide(new BigDecimal(count), 1, BigDecimal.ROUND_HALF_UP).toString();
            log.fatal(" list = " + list + ", count = " + count + ", avg = " + avg);
            return avg;
        }
        return null;
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
//            log.debug(sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {

                HrclassAvg hrclass = (HrclassAvg) rtn.get(rs.getString("HR_CLASS"));
                if (null == hrclass) {
                    continue;
                }

                final String subclasscd = rs.getString("SUBCLASSCD");
                final String score = rs.getString("SCORE");
                final String highscore = rs.getString("HIGHSCORE");
                final String lowscore = rs.getString("LOWSCORE");
                final String count = rs.getString("COUNT");
                final BigDecimal avgbd = rs.getBigDecimal("AVG");
                final String avg = null == rs.getString("AVG") ? null : new BigDecimal(rs.getString("AVG")).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                final String stddev = rs.getString("STDDEV");

                hrclass._subclassAvg.put(rs.getString("SUBCLASSCD"), new Average(subclasscd, score, highscore, lowscore, count, avg, avgbd, stddev));
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
            if (GROUP_COURSEGROUP.equals(param._formGroupdiv)) {
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
            if (GROUP_HR.equals(param._formGroupdiv)) {
                stb.append("     AND T2.HR_CLASS = '" + selected + "' ");
            } else if (GROUP_COURSE.equals(param._formGroupdiv)) {
                stb.append("     AND T2.COURSECD || T2.MAJORCD || T2.COURSECODE = '" + selected + "' ");
            } else if (GROUP_MAJOR.equals(param._formGroupdiv)) {
                stb.append("     AND T2.COURSECD || T2.MAJORCD = '" + selected + "' ");
            } else if (GROUP_COURSEGROUP.equals(param._formGroupdiv)) {
                stb.append("     AND T3.GROUP_CD = '" + selected + "' ");
            } else {
            }
            stb.append(" ORDER BY ");
            stb.append("     T1.GRADE, T1.HR_CLASS ");
            return stb.toString();
        }

        public String toString() {
            return "HrclassAvg(" + _hrclass + " : " + _subclassAvg + ")";
        }
    }

    private static class Student {
        final String _schregno;
        final String _hrclass;
        final String _attendno;
        final String _name;
        final String _sex;
        final String _inoutCd;
        Integer _rankBySex = null;
        final Map _ranks = new HashMap();
        public Student(final String schregno, final String name, final String hrclass, final String attendno, final String sex, final String inoutCd) {
            _schregno = schregno;
            _name = name;
            _hrclass = hrclass;
            _attendno = attendno;
            _sex = sex;
            _inoutCd = inoutCd;
        }
        public String getDeviation(final Param param) {
            final Rank rank = (Rank) _ranks.get(_999999);
            if (null == rank) {
                return null;
            }
            if (GROUP_GRADE.equals(param._formGroupdiv)) {
                return rank._gradeDeviation;
            } else if (GROUP_HR.equals(param._formGroupdiv)) {
                return rank._classDeviation;
            } else if (GROUP_COURSE.equals(param._formGroupdiv)) {
                return rank._courseDeviation;
            } else if (GROUP_MAJOR.equals(param._formGroupdiv)) {
                return rank._majorDeviation;
            } else if (GROUP_COURSEGROUP.equals(param._formGroupdiv)) {
                return rank._courseGroupDeviation;
            }
            return null;
        }
        /**
         * 総点の順位
         */
        public Integer getTotalRank(final Param param) {
            final Rank rank = (Rank) _ranks.get(_999999);
            if (null == rank) {
                return null;
            }
            if (GROUP_GRADE.equals(param._formGroupdiv)) {
                return rank._gradeRank;
            } else if (GROUP_HR.equals(param._formGroupdiv)) {
                return rank._classRank;
            } else if (GROUP_COURSE.equals(param._formGroupdiv)) {
                return rank._courseRank;
            } else if (GROUP_MAJOR.equals(param._formGroupdiv)) {
                return rank._majorRank;
            } else if (GROUP_COURSEGROUP.equals(param._formGroupdiv)) {
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
            stb.append("     T1.ATTENDNO, ");
            stb.append("     T2.SEX, ");
            stb.append("     T2.INOUTCD ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT T1 ");
            stb.append("     INNER JOIN  SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            if (GROUP_COURSEGROUP.equals(param._formGroupdiv)) {
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
            if (GROUP_HR.equals(param._formGroupdiv)) {
                stb.append("     AND T1.HR_CLASS = '" + selected + "' ");
            } else if (GROUP_COURSE.equals(param._formGroupdiv)) {
                stb.append("     AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE = '" + selected + "' ");
            } else if (GROUP_MAJOR.equals(param._formGroupdiv)) {
                stb.append("     AND T1.COURSECD || T1.MAJORCD = '" + selected + "' ");
            } else if (GROUP_COURSEGROUP.equals(param._formGroupdiv)) {
                stb.append("     AND T3.GROUP_CD = '" + selected + "' ");
            } else {
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
            stb.append("     T1.PROFICIENCY_SUBCLASS_CD AS SUBCLASSCD, ");
            stb.append("     T1.GRADE, ");
            stb.append("     T1.HR_CLASS, ");
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
            stb.append("     AND T1.PROFICIENCYDIV = '" + param._proficiencydiv + "' ");
            stb.append("     AND T1.PROFICIENCYCD = '" + param._proficiencycd + "' ");
            if ("4".equals(param._juni)) {
                stb.append("     AND T1.DATA_DIV = '" + AVG_DATA_KEISHA + "' ");
            } else {
                stb.append("     AND T1.DATA_DIV = '" + AVG_DATA_SCORE + "' ");
            }
            stb.append("     AND T1.GRADE = '" + param._grade +"' ");
            stb.append("     AND T1.AVG_DIV = '" + AVG_DIV_HR_CLASS + "' ");
            stb.append("     AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE = '00000000' ");
            stb.append(" UNION ");
            stb.append(" SELECT ");
            stb.append("     T1.AVG_DIV, ");
            stb.append("     T1.PROFICIENCY_SUBCLASS_CD AS SUBCLASSCD, ");
            stb.append("     T1.GRADE, ");
            stb.append("     T1.HR_CLASS, ");
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
            stb.append("     AND T1.PROFICIENCYDIV = '" + param._proficiencydiv + "' ");
            stb.append("     AND T1.PROFICIENCYCD = '" + param._proficiencycd + "' ");
            if ("4".equals(param._juni)) {
                stb.append("     AND T1.DATA_DIV = '" + AVG_DATA_KEISHA + "' ");
            } else {
                stb.append("     AND T1.DATA_DIV = '" + AVG_DATA_SCORE + "' ");
            }
            stb.append("     AND T1.GRADE = '" + param._grade +"' ");
            if (GROUP_HR.equals(param._formGroupdiv)) {
                stb.append("     AND T1.AVG_DIV = '" + AVG_DIV_HR_CLASS + "' ");
                stb.append("     AND T1.HR_CLASS = '" + selected + "' ");
            } else if (GROUP_COURSE.equals(param._formGroupdiv)) {
                stb.append("     AND T1.AVG_DIV = '" + AVG_DIV_COURSE + "' ");
                stb.append("     AND T1.HR_CLASS = '000' ");
                stb.append("     AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE = '" + selected + "' ");
            } else if (GROUP_MAJOR.equals(param._formGroupdiv)) {
                stb.append("     AND T1.AVG_DIV = '" + AVG_DIV_MAJOR + "' ");
                stb.append("     AND T1.HR_CLASS = '000' ");
                stb.append("     AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE = '" + selected + "' || '0000' ");
            } else if (GROUP_COURSEGROUP.equals(param._formGroupdiv)) {
                stb.append("     AND T1.AVG_DIV = '" + AVG_DIV_COURSEGROUP + "' ");
                stb.append("     AND T1.HR_CLASS = '000' ");
                stb.append("     AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE = '0' || '" + selected + "' || '0000' ");
            } else {
                stb.append("     AND T1.AVG_DIV = '" + AVG_DIV_GRADE + "' ");
                stb.append("     AND T1.HR_CLASS = '000' ");
                stb.append("     AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE = '00000000' ");
            }
            stb.append(" ORDER BY ");
            stb.append("     AVG_DIV, ");
            stb.append("     SUBCLASSCD, ");
            stb.append("     HR_CLASS ");
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
        final Integer _majorRank;
        final Integer _courseGroupRank;
        final String _gradeDeviation;
        final String _classDeviation;
        final String _courseDeviation;
        final String _majorDeviation;
        final String _courseGroupDeviation;
        public Rank(final String subclasscd, final String score, final String avg, final Integer gradeRank, final Integer classRank, final Integer courseRank, final Integer majorRank, final Integer courseGroupRank,
                final String gradeDeviation,
                final String classDeviation,
                final String courseDeviation,
                final String majorDeviation,
                final String courseGroupDeviation) {
            _subclasscd = subclasscd;
            _score = score;
            _avg = avg;
            _gradeRank = gradeRank;
            _classRank = classRank;
            _courseRank = courseRank;
            _majorRank = majorRank;
            _courseGroupRank = courseGroupRank;
            _gradeDeviation = gradeDeviation;
            _classDeviation = classDeviation;
            _courseDeviation = courseDeviation;
            _majorDeviation = majorDeviation;
            _courseGroupDeviation = courseGroupDeviation;
        }

        private static String sqlRank(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.PROFICIENCY_SUBCLASS_CD AS SUBCLASSCD, ");
            stb.append("     T1.SCORE, ");
            stb.append("     R0101.SCORE AS SCORE0101, ");
            stb.append("     T1.AVG, ");
            stb.append("     R0101.AVG AS AVG0101, ");
            stb.append("     T1.RANK AS GRADE_RANK, ");
            stb.append("     L2.RANK AS CLASS_RANK, ");
            stb.append("     L3.RANK AS COURSE_RANK, ");
            stb.append("     L4.RANK AS COURSEGROUP_RANK, ");
            stb.append("     L7.RANK AS MAJOR_RANK, ");
            stb.append("     T1.DEVIATION AS GRADE_DEVIATION, ");
            stb.append("     L2.DEVIATION AS CLASS_DEVIATION, ");
            stb.append("     L3.DEVIATION AS COURSE_DEVIATION, ");
            stb.append("     L4.DEVIATION AS COURSEGROUP_DEVIATION, ");
            stb.append("     L7.DEVIATION AS MAJOR_DEVIATION ");
            stb.append(" FROM PROFICIENCY_RANK_DAT T1 ");
            stb.append("     INNER JOIN SCHREG_REGD_DAT t3 on ");
            stb.append("         T1.YEAR = t3.YEAR and ");
            stb.append("         T1.SEMESTER = t3.SEMESTER and ");
            stb.append("         T1.SCHREGNO = t3.SCHREGNO ");
            stb.append("     LEFT JOIN PROFICIENCY_RANK_DAT L2 ON  L2.YEAR = t1.YEAR ");
            stb.append("          AND L2.SEMESTER = t1.SEMESTER ");
            stb.append("          AND L2.PROFICIENCYCD = t1.PROFICIENCYCD ");
            stb.append("          AND L2.PROFICIENCYDIV = t1.PROFICIENCYDIV ");
            stb.append("          AND L2.SCHREGNO = t1.SCHREGNO ");
            stb.append("          AND L2.PROFICIENCY_SUBCLASS_CD = t1.PROFICIENCY_SUBCLASS_CD ");
            stb.append("          AND L2.RANK_DATA_DIV = t1.RANK_DATA_DIV ");
            stb.append("          AND L2.RANK_DIV = '" + RANK_DIV_HRCLASS + "' ");
            stb.append("     LEFT JOIN PROFICIENCY_RANK_DAT L3 ON  L3.YEAR = t1.YEAR ");
            stb.append("          AND L3.SEMESTER = t1.SEMESTER ");
            stb.append("          AND L3.PROFICIENCYCD = t1.PROFICIENCYCD ");
            stb.append("          AND L3.PROFICIENCYDIV = t1.PROFICIENCYDIV ");
            stb.append("          AND L3.SCHREGNO = t1.SCHREGNO ");
            stb.append("          AND L3.PROFICIENCY_SUBCLASS_CD = t1.PROFICIENCY_SUBCLASS_CD ");
            stb.append("          AND L3.RANK_DATA_DIV = t1.RANK_DATA_DIV ");
            stb.append("          AND L3.RANK_DIV = '" + RANK_DIV_COURSE + "' ");
            stb.append("     LEFT JOIN PROFICIENCY_RANK_DAT L4 ON  L4.YEAR = t1.YEAR ");
            stb.append("          AND L4.SEMESTER = t1.SEMESTER ");
            stb.append("          AND L4.PROFICIENCYCD = t1.PROFICIENCYCD ");
            stb.append("          AND L4.PROFICIENCYDIV = t1.PROFICIENCYDIV ");
            stb.append("          AND L4.SCHREGNO = t1.SCHREGNO ");
            stb.append("          AND L4.PROFICIENCY_SUBCLASS_CD = t1.PROFICIENCY_SUBCLASS_CD ");
            stb.append("          AND L4.RANK_DATA_DIV = t1.RANK_DATA_DIV ");
            stb.append("          AND L4.RANK_DIV = '" + RANK_DIV_COURSEGROUP + "' ");
            stb.append("     LEFT JOIN PROFICIENCY_RANK_DAT L7 ON  L7.YEAR = t1.YEAR ");
            stb.append("          AND L7.SEMESTER = t1.SEMESTER ");
            stb.append("          AND L7.PROFICIENCYCD = t1.PROFICIENCYCD ");
            stb.append("          AND L7.PROFICIENCYDIV = t1.PROFICIENCYDIV ");
            stb.append("          AND L7.SCHREGNO = t1.SCHREGNO ");
            stb.append("          AND L7.PROFICIENCY_SUBCLASS_CD = t1.PROFICIENCY_SUBCLASS_CD ");
            stb.append("          AND L7.RANK_DATA_DIV = t1.RANK_DATA_DIV ");
            stb.append("          AND L7.RANK_DIV = '" + RANK_DIV_MAJOR + "' ");
            stb.append("     LEFT JOIN COURSE_GROUP_CD_DAT L5 ON  L5.YEAR = t3.YEAR ");
            stb.append("          AND L5.GRADE = t3.GRADE ");
            stb.append("          AND L5.COURSECD = t3.COURSECD ");
            stb.append("          AND L5.MAJORCD = t3.MAJORCD ");
            stb.append("          AND L5.COURSECODE = t3.COURSECODE ");
            stb.append("    LEFT JOIN COURSE_GROUP_CD_HDAT L6 ON  L6.YEAR = L5.YEAR ");
            stb.append("          AND L6.GRADE = L5.GRADE ");
            stb.append("          AND L6.GROUP_CD = L5.GROUP_CD ");
            stb.append("     LEFT JOIN PROFICIENCY_RANK_DAT R0101 ON  R0101.YEAR = t1.YEAR ");
            stb.append("          AND R0101.SEMESTER = t1.SEMESTER ");
            stb.append("          AND R0101.PROFICIENCYCD = t1.PROFICIENCYCD ");
            stb.append("          AND R0101.PROFICIENCYDIV = t1.PROFICIENCYDIV ");
            stb.append("          AND R0101.SCHREGNO = t1.SCHREGNO ");
            stb.append("          AND R0101.PROFICIENCY_SUBCLASS_CD = t1.PROFICIENCY_SUBCLASS_CD ");
            stb.append("          AND R0101.RANK_DATA_DIV = '01' ");
            stb.append("          AND R0101.RANK_DIV = '01' ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            if ("9".equals(param._semester)) {
                stb.append("     AND T1.SEMESTER = '" + param._ctrlSeme+ "' ");
            } else {
                stb.append("     AND T1.SEMESTER = '" + param._semester + "' ");
            }
            stb.append("     and T1.PROFICIENCYDIV = '" + param._proficiencydiv + "' ");
            stb.append("     and T1.PROFICIENCYCD = '" + param._proficiencycd + "' ");
            if ("2".equals(param._juni)) {
                stb.append("     and T1.RANK_DATA_DIV = '" + RANK_DATA_DIV_AVG + "' ");
            } else if ("3".equals(param._juni)) {
                stb.append("     and T1.RANK_DATA_DIV = '" + RANK_DATA_DIV_DEVIATION + "' ");
            } else if ("4".equals(param._juni)) {
                stb.append("     and T1.RANK_DATA_DIV = '" + RANK_DATA_DIV_KEISHA + "' ");
            } else {
                stb.append("     and T1.RANK_DATA_DIV = '" + RANK_DATA_DIV_SCORE + "' ");
            }
            stb.append("     and T1.RANK_DIV = '" + RANK_DIV_GRADE + "' ");
            stb.append("     and T3.GRADE = '" + param._grade + "' ");
            return stb.toString();
        }
    }

    private static class Group {
        final String _code;
        final String _name;
        Group(final String code, final String name) {
            _code = code;
            _name = name;
        }

        public String toString() {
            return "Group(" + _code + ":" + _name + ")";
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
                    final String name = rs.getString("NAME");
                    if (null == code) {
                        continue;
                    }
                    list.add(new Group(code, name));
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
            if (GROUP_HR.equals(param._formGroupdiv)) {
                stb.append("     T1.HR_CLASS AS GROUP ");
                stb.append("    ,L4.HR_NAME AS NAME ");
            } else if (GROUP_COURSE.equals(param._formGroupdiv)) {
                stb.append("     T1.COURSECD || T1.MAJORCD || T1.COURSECODE AS GROUP ");
                stb.append("    ,L3.MAJORNAME || L2.COURSECODENAME AS NAME ");
            } else if (GROUP_MAJOR.equals(param._formGroupdiv)) {
                stb.append("     T1.COURSECD || T1.MAJORCD AS GROUP ");
                stb.append("    ,L3.MAJORNAME AS NAME ");
            } else if (GROUP_COURSEGROUP.equals(param._formGroupdiv)) {
                stb.append("     T2.GROUP_CD AS GROUP ");
                stb.append("    ,L1.GROUP_NAME AS NAME ");
            } else {
                stb.append("     T1.GRADE AS GROUP ");
                stb.append("    ,'' AS NAME ");
            }
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT T1 ");
            stb.append("     LEFT JOIN COURSE_GROUP_CD_DAT T2 ON T2.YEAR = T1.YEAR AND ");
            stb.append("         T2.GRADE = T1.GRADE AND ");
            stb.append("         T2.COURSECD = T1.COURSECD AND ");
            stb.append("         T2.MAJORCD = T1.MAJORCD AND ");
            stb.append("         T2.COURSECODE = T1.COURSECODE ");
            stb.append("     LEFT JOIN COURSE_GROUP_CD_HDAT L1 ON L1.YEAR = T2.YEAR AND ");
            stb.append("         L1.GRADE = T2.GRADE AND ");
            stb.append("         L1.GROUP_CD = T2.GROUP_CD ");
            stb.append("     LEFT JOIN COURSECODE_MST L2 ON L2.COURSECODE = T1.COURSECODE ");
            stb.append("     LEFT JOIN MAJOR_MST L3 ON L3.COURSECD = T1.COURSECD AND L3.MAJORCD = T1.MAJORCD ");
            stb.append("     LEFT JOIN SCHREG_REGD_HDAT L4 ON L4.YEAR = T1.YEAR AND ");
            stb.append("         L4.SEMESTER = T1.SEMESTER AND ");
            stb.append("         L4.GRADE = T1.GRADE AND ");
            stb.append("         L4.HR_CLASS = T1.HR_CLASS ");
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
        final String _proficiencydiv;
        final String _proficiencycd;
        final String _juni;
        final String _date;
        final Map _subclassnames;
        final String _formGroupdiv;
        final String _sortdiv;
        String _proficiencyname1 = "";
        String _semestername = "";
        String _gradename = "";
        final String _schoolKind;
        final boolean _isMeiji;

        Param(final HttpServletRequest request, final DB2UDB db2) {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _ctrlSeme = request.getParameter("CTRL_SEME");
            _grade = request.getParameter("GRADE");
            _proficiencydiv = request.getParameter("PROFICIENCYDIV");
            _proficiencycd = request.getParameter("PROFICIENCYCD");
            final String juni = request.getParameter("JUNI");
            _juni = StringUtils.isBlank(request.getParameter("useKnjd106cJuni" + juni)) ? juni : request.getParameter("useKnjd106cJuni" + juni);
            _date = request.getParameter("DATE");
            _subclassnames = getSubclassname(db2);
            setName(db2);
            _formGroupdiv = request.getParameter("FORM_GROUP_DIV");
            _sortdiv = request.getParameter("SORT_DIV");
            _schoolKind = getSchoolKind(db2);
            _isMeiji = "meiji".equals(getNameMstZ010(db2));
        }

        private void setName(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT PROFICIENCYNAME1 FROM PROFICIENCY_MST WHERE PROFICIENCYDIV = '" + _proficiencydiv + "' AND PROFICIENCYCD = '" + _proficiencycd + "' ");
                rs = ps.executeQuery();
                if (rs.next()) {
                    _proficiencyname1 = nullBlank(rs.getString("PROFICIENCYNAME1"));
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
            stb.append("     PROFICIENCY_SUBCLASS_CD AS SUBCLASSCD, ");
            stb.append("     SUBCLASS_NAME AS SUBCLASSNAME ");
            stb.append(" FROM ");
            stb.append("     PROFICIENCY_SUBCLASS_MST ");
            return stb.toString();
        }

        private String getNameMstZ010(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            String rtn = "";

            try {
                final String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    rtn = rs.getString("NAME1");
                }
            } catch (Exception e) {
                log.error("Z010sqlError", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }
    }
}
