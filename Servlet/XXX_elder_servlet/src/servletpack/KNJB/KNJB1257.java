/*
 * $Id: c5048c823e5fcc6375f4cd59ab2cf2c6f6170956 $
 *
 * 作成日: 2014/09/18
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJB;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

public class KNJB1257 {

    private static final Log log = LogFactory.getLog(KNJB1257.class);

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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        final int maxColumn = 40;
        final int keta = 6;

        final List studentList = getStudentList(db2, _param);
        final List subclassCompSelectMstAllList = subclassCompSelectMstList(db2, _param, getCourses(studentList));
        setSubclassStdSelect(db2, _param, studentList);

        final List subclassPageList = getPageList(subclassCompSelectMstAllList, maxColumn);

        for (int pi = 0, maxPage = subclassPageList.size(); pi < maxPage; pi++) {
            final List subclassList = (List) subclassPageList.get(pi);

            svf.VrSetForm("KNJB0057.frm", 4);
            svf.VrsOut("year2", KenjaProperties.gengou(Integer.parseInt(_param._year)) + "年度"); // 年度
            svf.VrsOut("TITLE", "履修選択科目一覧"); // タイトル
            svf.VrsOut("ymd1", KNJ_EditDate.h_format_JP(_param._ctrlDate)); // 作成日

            for (int si = 0; si < studentList.size(); si++) {
                final Student student = (Student) studentList.get(si);
                final int line = si + 1;
                svf.VrsOutn("NUMBER", line, StringUtils.isNumeric(student._attendno) ? String.valueOf(Integer.parseInt(student._attendno))  : student._attendno); // 番号
                svf.VrsOutn("name1", line, student._name); // 氏名
                svf.VrsOut("HR_NAME", student._hrName); // クラス名
            }

            for (int subi = 0; subi < subclassList.size(); subi++) {
                final Subclass subclass = (Subclass) subclassList.get(subi);
                final List divideString = divideString(subclass._subclassabbv, keta);
                if (divideString.size() > 1) {
                    svf.VrsOut("SUBKECT2_1", (String) divideString.get(0)); // 科目
                    svf.VrsOut("SUBKECT2_2", (String) divideString.get(1)); // 科目
                } else if (divideString.size() > 0) {
                    svf.VrsOut("SUBKECT1", (String) divideString.get(0)); // 科目
                }
                final SubclassCompSelectMst group = subclass._group;
                svf.VrsOut("GROUP", getCenteredGroupName(group, subclassList, keta, subi)); // グループ
                svf.VrsOut("GROUP_CD", StringUtils.defaultString(group._groupcd).length() > 2 ? group._groupcd.substring(group._groupcd.length() - 2) : group._groupcd); // グループコード

                for (int si = 0; si < studentList.size(); si++) {
                    final Student student = (Student) studentList.get(si);
                    final int line = si + 1;
                    final boolean selectSubclass = student.isSelectSubclass(group._groupcd, subclass._subclasscd);
                    if (selectSubclass) {
                        svf.VrsOutn("CHECK", line, "レ"); // チェックマーク
                    }
                }
                svf.VrEndRecord();
                _hasData = true;
            }
        }
    }

    private String[] getCourses(final List studentList) {
        final Set set = new HashSet();
        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            set.add(student._course);
        }
        final String[] arr = new String[set.size()];
        int i = 0;
        for (final Iterator it = set.iterator(); it.hasNext();) {
            arr[i++] = (String) it.next();
        }
        return arr;
    }

    /**
     * センタリングされたグループ名の指定行の値を返す
     * @param group 対象グループ
     * @param subclassList 同じページの科目行（最大桁数計算用）
     * @param keta 1行あたりの桁数
     * @param subi 指定行
     * @return センタリングされたグループ名の指定行の値を返す
     */
    private String getCenteredGroupName(final SubclassCompSelectMst group, final List subclassList, final int keta, final int subi) {
        if (StringUtils.isBlank(group._name)) {
            return "";
        }
        int targetKeta = 0;
        try {
            targetKeta = group._name.getBytes("MS932").length;
        } catch (Exception e) {
            log.error("exception!", e);
        }
        boolean isSame = false;
        int sameGroupLine = 0; // 同一グループの行数
        int beforeSubclassCount = 0; // 同一グループの前のグループの行数
        for (final Iterator it = subclassList.iterator(); it.hasNext();) {
            final Subclass subclass = (Subclass) it.next();
            if (group == subclass._group) {
                isSame = true;
                sameGroupLine += 1;
            }
            if (!isSame) {
                beforeSubclassCount += 1;
            }
        }
        final int maxKeta = sameGroupLine * keta;
        if (maxKeta <= targetKeta) {
            return group._name;
        }
        final int preSpaceCount = (maxKeta - targetKeta) / 2;
        final List divideString = divideString(StringUtils.repeat(" ", preSpaceCount) + StringUtils.defaultString(group._name), keta);
        if (divideString.size() <= subi - beforeSubclassCount) {
            return "";
        }
        return StringUtils.replace((String) divideString.get(subi - beforeSubclassCount), "  ", "　"); // 半角スペース2つを全角スペース1つに変更
    }

    /**
     * 文字列を指定桁数で分割したリストを返す
     * @param name 文字列
     * @param keta 桁数
     * @return 文字列を指定桁数で分割したリスト
     */
    private List divideString(final String name, final int keta) {
        final List rtn = new ArrayList();
        if (StringUtils.isBlank(name)) {
            rtn.add(name);
        } else {
            int currentKeta = 0;
            StringBuffer current = new StringBuffer();
            for (int i = 0; i < name.length(); i++) {
                final String ch = String.valueOf(name.charAt(i));
                try {
                    final int mojiketa = ch.getBytes("MS932").length;
                    if (currentKeta + mojiketa > keta) {
                        rtn.add(current.toString());
                        currentKeta = 0;
                        current.delete(0, current.length());
                    }
                    currentKeta += mojiketa;
                    current.append(ch);
                } catch (Exception e) {
                    log.error("exception!", e);
                }
            }
            if (0 != currentKeta) {
                rtn.add(current.toString());
            }
        }
        return rtn;
    }

    private List getPageList(final List subclassCompSelectMstAllList, final int max) {
        final List pageList = new ArrayList();
        List current = null;
        for (final Iterator it = subclassCompSelectMstAllList.iterator(); it.hasNext();) {
            final SubclassCompSelectMst group = (SubclassCompSelectMst) it.next();
            for (final Iterator subit = group._subclassList.iterator(); subit.hasNext();) {
                final Subclass subclass = (Subclass) subit.next();
                if (null == current || current.size() >= max) {
                    current = new ArrayList();
                    pageList.add(current);
                }
                current.add(subclass);
            }
        }
        return pageList;
    }

    private static class Student {
        final String _grade;
        final String _hrClass;
        final String _hrName;
        final String _schregno;
        final String _name;
        final String _attendno;
        final String _course;
        final Map _groupSubclassMap;

        Student(
            final String grade,
            final String hrClass,
            final String hrName,
            final String schregno,
            final String name,
            final String attendno,
            final String course
        ) {
            _grade = grade;
            _hrClass = hrClass;
            _hrName = hrName;
            _schregno = schregno;
            _name = name;
            _attendno = attendno;
            _course = course;
            _groupSubclassMap = new HashMap();
        }

        private void addGroupSubclass(final String groupcd, final String subclasscd) {
            if (null == _groupSubclassMap.get(groupcd)) {
                _groupSubclassMap.put(groupcd, new ArrayList());
            }
            final List subclassList = (List) _groupSubclassMap.get(groupcd);
            subclassList.add(subclasscd);
        }

        public boolean isSelectSubclass(final String groupcd, final String subclasscd) {
            if (null == _groupSubclassMap.get(groupcd)) {
                return false;
            }
            final List subclassList = (List) _groupSubclassMap.get(groupcd);
            return subclassList.contains(subclasscd);
        }

        private static Student getStudent(final String schregno, final List list) {
            for (final Iterator it = list.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                if (student._schregno.equals(schregno)) {
                    return student;
                }
            }
            return null;
        }
    }

    private List getStudentList(final DB2UDB db2, final Param param) {
        final List list = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getStudentSql(param);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String grade = rs.getString("GRADE");
                final String hrClass = rs.getString("HR_CLASS");
                final String hrName = rs.getString("HR_NAME");
                final String schregno = rs.getString("SCHREGNO");
                final String name = rs.getString("NAME");
                final String attendno = rs.getString("ATTENDNO");
                final String course = rs.getString("COURSE");
                final Student student = new Student(grade, hrClass, hrName, schregno, name, attendno, course);
                list.add(student);
            }
        } catch (Exception ex) {
            log.fatal("exception!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return list;
    }

    private String getStudentSql(final Param param) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("    T1.GRADE, ");
        stb.append("    T1.HR_CLASS, ");
        stb.append("    T2.HR_NAME, ");
        stb.append("    T1.SCHREGNO, ");
        stb.append("    T3.NAME, ");
        stb.append("    T1.ATTENDNO, ");
        stb.append("    T1.GRADE || T1.COURSECD || T1.MAJORCD || T1.COURSECODE AS COURSE ");
        stb.append(" FROM SCHREG_REGD_DAT T1 ");
        stb.append(" INNER JOIN SCHREG_REGD_HDAT T2 ON T2.YEAR = T1.YEAR ");
        stb.append("     AND T2.SEMESTER = T1.SEMESTER ");
        stb.append("     AND T2.GRADE = T1.GRADE ");
        stb.append("     AND T2.HR_CLASS = T1.HR_CLASS ");
        stb.append(" INNER JOIN SCHREG_BASE_MST T3 ON T3.SCHREGNO = T1.SCHREGNO ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + param._year + "' ");
        stb.append("     AND T1.SEMESTER = '" + param._semester + "' ");
        stb.append("     AND T1.GRADE = '" + param._grade + "' ");
        stb.append("     AND T1.HR_CLASS = '" + param._hrclass + "' ");
        stb.append(" ORDER BY ");
        stb.append("    T1.GRADE, T1.HR_CLASS, T1.ATTENDNO ");
        return stb.toString();
    }

    private static class SubclassCompSelectMst {
        final String _groupcd;
        final String _name;
        final List _subclassList;

        SubclassCompSelectMst(
            final String groupcd,
            final String name
        ) {
            _groupcd = groupcd;
            _name = name;
            _subclassList = new ArrayList();
        }

        private static SubclassCompSelectMst getGroup(final String groupcd, final List groupList) {
            for (final Iterator it = groupList.iterator(); it.hasNext();) {
                final SubclassCompSelectMst group = (SubclassCompSelectMst) it.next();
                if (group._groupcd.equals(groupcd)) {
                    return group;
                }
            }
            return null;
        }
    }

    private List subclassCompSelectMstList(final DB2UDB db2, final Param param, final String[] courses) {
        final List list = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getSubclassCompSelectMstSql(param, courses);
            log.debug(" sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String groupcd = rs.getString("GROUPCD");
                if (null == SubclassCompSelectMst.getGroup(groupcd, list)) {
                    final String name = rs.getString("GROUP_NAME");
                    list.add(new SubclassCompSelectMst(groupcd, name));
                }
                final SubclassCompSelectMst group = SubclassCompSelectMst.getGroup(groupcd, list);

                final String subclasscd = rs.getString("SUBCLASSCD");
                final String subclassabbv = rs.getString("SUBCLASSABBV");
                final Subclass subclass = new Subclass(group, subclasscd, subclassabbv);
                group._subclassList.add(subclass);
            }
        } catch (Exception ex) {
            log.fatal("exception!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return list;
    }

    private String getSubclassCompSelectMstSql(final Param param, final String[] courses) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT  ");
        stb.append("     T1.GROUPCD, ");
        stb.append("     T1.NAME AS GROUP_NAME, ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append("    T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || ");
        }
        stb.append("     T2.SUBCLASSCD AS SUBCLASSCD, ");
        stb.append("     T3.SUBCLASSABBV ");
        stb.append(" FROM SUBCLASS_COMP_SELECT_MST T1 ");
        stb.append(" INNER JOIN SUBCLASS_COMP_SELECT_DAT T2 ON T2.YEAR = T1.YEAR ");
        stb.append("     AND T2.GRADE = T1.GRADE ");
        stb.append("     AND T2.COURSECD = T1.COURSECD ");
        stb.append("     AND T2.MAJORCD = T1.MAJORCD ");
        stb.append("     AND T2.COURSECODE = T1.COURSECODE ");
        stb.append("     AND T2.GROUPCD = T1.GROUPCD ");
        stb.append(" INNER JOIN SUBCLASS_MST T3 ON T3.SUBCLASSCD = T2.SUBCLASSCD ");
        stb.append("     AND T3.CLASSCD = T2.CLASSCD ");
        stb.append("     AND T3.SCHOOL_KIND = T2.SCHOOL_KIND ");
        stb.append("     AND T3.CURRICULUM_CD = T2.CURRICULUM_CD ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + param._year + "' ");
        stb.append("     AND T1.GRADE || T1.COURSECD || T1.MAJORCD || T1.COURSECODE IN " + SQLUtils.whereIn(true, courses) + " ");
        stb.append(" ORDER BY ");
        stb.append("     T1.GROUPCD, ");
        stb.append("     T1.NAME, ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append("    T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || ");
        }
        stb.append("     T2.SUBCLASSCD ");
        return stb.toString();
    }

    private static class Subclass {
        final SubclassCompSelectMst _group;
        final String _subclasscd;
        final String _subclassabbv;

        Subclass(
            final SubclassCompSelectMst group,
            final String subclasscd,
            final String subclassabbv
        ) {
            _group = group;
            _subclasscd = subclasscd;
            _subclassabbv = subclassabbv;
        }
    }

    private void setSubclassStdSelect(final DB2UDB db2, final Param param, final List studentList) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getSubclassStdSelectSql(param);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final Student student = Student.getStudent(rs.getString("SCHREGNO"), studentList);
                if (null == student) {
                    continue;
                }
                student.addGroupSubclass(rs.getString("GROUPCD"), rs.getString("SUBCLASSCD"));
            }
        } catch (Exception ex) {
            log.fatal("exception!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }

    private String getSubclassStdSelectSql(final Param param) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT  ");
        stb.append("    T1.GROUPCD, ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append("    T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
        }
        stb.append("    T1.SUBCLASSCD AS SUBCLASSCD, ");
        stb.append("    T1.SCHREGNO ");
        stb.append(" FROM SUBCLASS_STD_SELECT_RIREKI_DAT T1 ");
        stb.append(" INNER JOIN SCHREG_REGD_DAT T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("    AND T2.YEAR = T1.YEAR ");
        stb.append("    AND T2.SEMESTER = T1.SEMESTER ");
        stb.append("    AND T2.GRADE = '" + param._grade + "' ");
        stb.append("    AND T2.HR_CLASS = '" + param._hrclass + "' ");
        stb.append(" WHERE ");
        stb.append("    T1.YEAR = '" + param._year + "' ");
        stb.append("    AND T1.SEMESTER = '" + param._semester + "' ");
        stb.append("    AND T1.RIREKI_CODE = '" + _param._rirekiCode + "' ");
        return stb.toString();
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
        final String _ctrlYear;
        final String _ctrlSemester;
        final String _year;
        final String _semester;
        final String _grade;
        final String _hrclass;
        final String _useCurriculumcd;
        final String _ctrlDate;
        private final String _rirekiCode;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            final String gradeHrclass = request.getParameter("GRADE_CLASS");
            _grade = null != gradeHrclass && -1 != gradeHrclass.indexOf('_') ? StringUtils.split(gradeHrclass, "_")[0] : null;
            _hrclass = null != gradeHrclass && -1 != gradeHrclass.indexOf('_') ? StringUtils.split(gradeHrclass, "_")[1] : null;
            _rirekiCode = request.getParameter("RIREKI_CODE");
        }
    }
}

// eof

