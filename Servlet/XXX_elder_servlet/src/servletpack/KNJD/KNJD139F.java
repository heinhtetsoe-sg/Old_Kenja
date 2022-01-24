/*
 * $Id: 19929881afca4683e12e30c63c224efbbc63f0cc $
 *
 * 作成日: 2012/02/21
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

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
 * 学校教育システム 賢者 [成績管理] 面談資料
 */

public class KNJD139F {

    private static final Log log = LogFactory.getLog(KNJD139F.class);

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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {

        final List studentList = getStudentList(db2);

        svf.VrSetForm("KNJD139F.frm", 4);

        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            log.debug(" student = " + student._schregno + " : " + student._name);

            printSvfHeader(svf, student);

            printSvfStudent(svf, student);

            svf.VrEndRecord();
            _hasData = true;
        }
    }

    private void printSvfHeader(final Vrw32alp svf, final Student student) {
        svf.VrsOut("NENDO", KenjaProperties.gengou(Integer.parseInt(_param._year)) + "年度");
        svf.VrsOut("SEMESTER", _param._semesterName);
        svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._date));
        svf.VrsOut("SUBCLASS", _param._subclassName);
        svf.VrsOut("CHAIRNAME", _param._chairName);
    }

    /**
     * 『氏名』
     * @param svf
     * @param student
     */
    private void printSvfStudent(final Vrw32alp svf, final Student student) {
        svf.VrsOut("HR_NAME", student._hrName + "-" + student._attendno);
        svf.VrsOut("NAME", student._schregno + "　" + student._name);
        svf.VrsOut("VIEW_COMMENT1_1", student._remark);
    }

    private List getStudentList(final DB2UDB db2) {
        final List studentList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getStudentSql(_param);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String schregno = rs.getString("SCHREGNO");
                final String name = rs.getString("NAME");
                final String hrName = rs.getString("HR_NAME");
                final String remark = rs.getString("REMARK1");
                String attendno = rs.getString("ATTENDNO");
                attendno = null == attendno ? "" : (NumberUtils.isDigits(attendno)) ? (String.valueOf(Integer.parseInt(attendno)) + "番"): attendno;
                final Student student = new Student(schregno, name, hrName, attendno, remark);
                studentList.add(student);
            }

        } catch (SQLException e) {
            log.error("exception!!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return studentList;
    }

    private String getStudentSql(final Param param) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH SCHINFO AS ( ");
        stb.append("     SELECT ");
        stb.append("         T1.SCHREGNO, ");
        stb.append("         T1.GRADE, ");
        stb.append("         T1.HR_CLASS, ");
        stb.append("         T1.ATTENDNO, ");
        stb.append("         T2.NAME, ");
        stb.append("         T3.HR_NAME ");
        stb.append("     FROM ");
        stb.append("         SCHREG_REGD_FI_DAT T1, ");
        stb.append("         SCHREG_BASE_MST T2, ");
        stb.append("         SCHREG_REGD_FI_HDAT T3 ");
        stb.append("     WHERE ");
        stb.append("         T1.YEAR     = T3.YEAR AND ");
        stb.append("         T1.YEAR     = '" + _param._year + "' AND ");
        stb.append("         T1.SEMESTER = T3.SEMESTER AND ");
        stb.append("         T1.SEMESTER = '" + _param._ctrlSemester + "' AND ");
        stb.append("         T1.SCHREGNO = T2.SCHREGNO AND ");
        stb.append("         T1.GRADE    = T3.GRADE AND ");
        stb.append("         T1.GRADE    = '" + _param._grade + "' AND ");
        stb.append("         T1.HR_CLASS = T3.HR_CLASS ");
        stb.append(" ), CHAIR AS ( ");
        stb.append("     SELECT DISTINCT ");
        stb.append("         T2.SCHREGNO ");
        stb.append("     FROM ");
        stb.append("         CHAIR_DAT T1, ");
        stb.append("         CHAIR_STD_DAT T2 ");
        stb.append("     WHERE ");
        stb.append("         T1.YEAR     = T2.YEAR AND ");
        stb.append("         T1.YEAR     = '" + _param._year + "' AND ");
        stb.append("         T1.SEMESTER = T2.SEMESTER AND ");
        stb.append("         T1.SEMESTER = '" + _param._ctrlSemester + "' AND ");
        stb.append("         T1.CHAIRCD  = T2.CHAIRCD AND ");
        stb.append("         T1.CHAIRCD  = '" + _param._chairCd + "' ");
        stb.append(" ), REPORTREMARK AS ( ");
        stb.append("     SELECT ");
        stb.append("         SCHREGNO, ");
        stb.append("         REMARK1 ");
        stb.append("     FROM ");
        stb.append("         JVIEWSTAT_REPORTREMARK_DAT ");
        stb.append("     WHERE ");
        stb.append("         YEAR            = '" + _param._year + "' AND ");
        stb.append("         SEMESTER        = '" + _param._semester + "' AND ");
        stb.append("         CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '" + _param._subclasscd + "' ");
        stb.append(" ) ");
        stb.append(" SELECT DISTINCT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.HR_NAME, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     T1.NAME, ");
        stb.append("     T3.REMARK1 ");
        stb.append(" FROM ");
        stb.append("     SCHINFO T1 ");
        stb.append("     INNER JOIN CHAIR T2 ON T1.SCHREGNO = T2.SCHREGNO ");
        stb.append("     LEFT JOIN REPORTREMARK T3 ON T1.SCHREGNO = T3.SCHREGNO ");
        stb.append(" ORDER BY ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     T1.SCHREGNO ");

        return stb.toString();
    }

    private void VrsOutRenban(final Vrw32alp svf, final String field, final List list) {
        if (null != list) {
            for (int i = 0 ; i < list.size(); i++) {
                svf.VrsOut(field + (i + 1), (String) list.get(i));
            }
        }
    }

    private static int getMS932ByteCount(final String str) {
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

    private static String notZero(int n) {
        return String.valueOf(n);
    }

    private static class CharMS932 {
        final String _char;
        final int _len;
        public CharMS932(final String v, final byte[] b) {
            _char = v;
            _len = b.length;
        }
        public String toString() {
            return "[" + _char + " : " + _len + "]";
        }
    }
    private static List toCharMs932List(final String src) throws Exception {
        final List rtn = new ArrayList();
        for (int j = 0; j < src.length(); j++) {
            final String z = src.substring(j, j + 1);             //1文字を取り出す
            final CharMS932 c = new CharMS932(z, z.getBytes("MS932"));
            rtn.add(c);
        }
        return rtn;
    }

    protected static List retDividString(String targetsrc, final int dividlen, final int dividnum) {
        if (targetsrc == null) {
            return Collections.EMPTY_LIST;
        }
        final List lines = new ArrayList(dividnum);         //編集後文字列を格納する配列
        int len = 0;
        StringBuffer stb = new StringBuffer();

        try {
            if (!StringUtils.replace(targetsrc, "\r\n", "\n").equals(targetsrc)) {
//                log.fatal("改行コードが\\r\\n!:" + targetsrc);
                targetsrc = StringUtils.replace(targetsrc, "\r\n", "\n");
            }

            final List charMs932List = toCharMs932List(targetsrc);

            for (final Iterator it = charMs932List.iterator(); it.hasNext();) {
                final CharMS932 c = (CharMS932) it.next();
                //log.debug(" c = " + c);

                if (("\n".equals(c._char) || "\r".equals(c._char))) {
                    if (len <= dividlen) {
                        lines.add(stb.toString());
                        len = 0;
                        stb.delete(0, stb.length());
                    }
                } else {
                    if (len + c._len > dividlen) {
                        lines.add(stb.toString());
                        len = 0;
                        stb.delete(0, stb.length());
                    }
                    stb.append(c._char);
                    len += c._len;
                }
            }
            if (0 < len) {
                lines.add(stb.toString());
            }
        } catch (Exception ex) {
            log.error("retDividString error! ", ex);
        }
        if (lines.size() > dividnum) {
            return lines.subList(0, dividnum);
        }
        return lines;
    }

    private static class Student {
        final String _schregno;
        final String _name;
        final String _hrName;
        final String _attendno;
        final String _remark;

        public Student(final String schregno, final String name, final String hrName, final String attendno, final String remark) {
            _schregno = schregno;
            _name = name;
            _hrName = hrName;
            _attendno = attendno;
            _remark = remark;
        }

    }

    /**
     * 学習の記録（観点）
     */
    private static class ViewRecord {

        final String _semester;
        final String _viewcd;
        final String _status;
        final String _d029Namecd2;
        final String _d029Namespare1;
        final String _grade;
        final String _viewname;
        final String _classcd;
        final String _classMstShoworder;
        final String _showorder;
        ViewRecord(
                final String semester,
                final String viewcd,
                final String status,
                final String d029Namecd2,
                final String d029Namespare1,
                final String grade,
                final String viewname,
                final String classcd,
                final String classMstShoworder,
                final String showorder) {
            _semester = semester;
            _viewcd = viewcd;
            _status = status;
            _d029Namecd2 = d029Namecd2;
            _d029Namespare1 = d029Namespare1;
            _grade = grade;
            _viewname = viewname;
            _classcd = classcd;
            _classMstShoworder = classMstShoworder;
            _showorder = showorder;
        }

        public static List getViewRecordList(final DB2UDB db2, final Param param, final String schregno) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getViewRecordSql(param, schregno);
//                log.debug(" view record sql = "+  sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {

                    final String semester = rs.getString("SEMESTER");
                    final String viewcd = rs.getString("VIEWCD");
                    final String status = rs.getString("STATUS") != null ? rs.getString("STATUS") : "";
                    final String d029Namecd2 = rs.getString("D029NAMECD2");
                    final String d029Namespare1 = rs.getString("D029NAMESPARE1");
                    final String grade = rs.getString("GRADE");
                    final String viewname = rs.getString("VIEWNAME");
                    final String classcd = rs.getString("CLASSCD");
                    final String classMstShoworder = rs.getString("CLASS_MST_SHOWORDER");
                    final String showorder = rs.getString("SHOWORDER");

                    final ViewRecord viewRecord = new ViewRecord(semester, viewcd, status, d029Namecd2, d029Namespare1, grade, viewname, classcd, classMstShoworder, showorder);

                    list.add(viewRecord);
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }

        private static String getViewRecordSql(final Param param, final String schregno) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.GRADE   ");
            stb.append("     , T1.VIEWNAME ");
            stb.append("     , T1.VIEWCD ");
            stb.append("     , T3.YEAR ");
            stb.append("     , T3.SEMESTER ");
            stb.append("     , T3.SCHREGNO ");
            stb.append("     , T3.STATUS ");
            stb.append("     , T5.NAMECD2 AS D029NAMECD2 ");
            stb.append("     , T5.NAMESPARE1 AS D029NAMESPARE1 ");
            stb.append("     , T4.CLASSCD ");
            stb.append("     , T4.SHOWORDER AS CLASS_MST_SHOWORDER ");
            stb.append("     , T1.SHOWORDER ");
            stb.append(" FROM JVIEWNAME_GRADE_MST T1 ");
            stb.append("     INNER JOIN JVIEWNAME_GRADE_YDAT T2 ON T2.SUBCLASSCD = T1.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("         AND T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD = ");
                stb.append("             T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD ");
            }
            stb.append("         AND T2.VIEWCD = T1.VIEWCD ");
            stb.append("         AND T2.GRADE = T1.GRADE ");
            stb.append("     INNER JOIN JVIEWSTAT_RECORD_DAT T3 ON T3.SUBCLASSCD = T1.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("         AND T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD = ");
                stb.append("             T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD ");
            }
            stb.append("         AND T3.VIEWCD = T1.VIEWCD ");
            stb.append("         AND T3.YEAR = T2.YEAR ");
            stb.append("         AND T3.SEMESTER = '" + param._semester + "' ");
            stb.append("         AND T3.SCHREGNO = '" + schregno + "' ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     LEFT JOIN CLASS_MST T4 ON ");
                stb.append("             T4.CLASSCD || '-' || T4.SCHOOL_KIND = ");
                stb.append("             T1.CLASSCD || '-' || T1.SCHOOL_KIND ");
            } else {
                stb.append("     LEFT JOIN CLASS_MST T4 ON T4.CLASSCD = SUBSTR(T1.SUBCLASSCD, 1, 2) ");
            }
            stb.append("     LEFT JOIN NAME_MST T5 ON T5.NAMECD1 = 'D029' ");
            stb.append("         AND T5.ABBV1 = T3.STATUS ");
            stb.append(" WHERE ");
            stb.append("     T2.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.GRADE = '" + param._grade + "' ");
            stb.append("     AND ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("         T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
            }
            stb.append("         T1.SUBCLASSCD = '" + param._subclasscd + "' ");
            stb.append("     AND SUBSTR(T1.VIEWCD, 1, 2) = '" + param._classcd + "' ");
            stb.append(" ORDER BY ");
//            stb.append("     VALUE(T1.SHOWORDER, 0) ");
            stb.append("     T1.SHOWORDER ");
            stb.append("     , T1.VIEWCD ");
            return stb.toString();
        }
    }

    /**
     * 観点のコメント
     */
    private static class ViewstatReportRemark {

        final String _semester;
        final String _subclasscd;
        final String _remark1;
        ViewstatReportRemark(
                final String semester,
                final String subclasscd,
                final String remark1
                ) {
            _semester = semester;
            _subclasscd = subclasscd;
            _remark1 = remark1;
        }

        public static List getViewstatReportRemarkList(final DB2UDB db2, final Param param, final String schregno) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getViewstatReportRemarkSql(param, schregno);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {

                    final String semester = rs.getString("SEMESTER");
                    final String subclasscd = rs.getString("SUBCLASSCD");
                    final String remark1 = rs.getString("REMARK1");

                    final ViewstatReportRemark vrr = new ViewstatReportRemark(semester, subclasscd, remark1);

                    list.add(vrr);
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }

        private static String getViewstatReportRemarkSql(final Param param, final String schregno) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.YEAR ");
            stb.append("     , T1.SEMESTER ");
            stb.append("     , ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("         T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
            }
            stb.append("       T1.SUBCLASSCD AS SUBCLASSCD ");
            stb.append("     , T1.REMARK1 ");
            stb.append(" FROM JVIEWSTAT_REPORTREMARK_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.SEMESTER = '" + param._semester + "' ");
            stb.append("     AND T1.SCHREGNO = '" + schregno + "' ");
            stb.append("     AND ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("         T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
            }
            stb.append("         T1.SUBCLASSCD = '" + param._subclasscd + "' ");
            return stb.toString();
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 56595 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _year;
        final String _semester;
        final String _ctrlSemester;
        final String _date;
        final String _grade;
        final String _classcd;
        final String _subclasscd;
        final String _chairCd;
        final String _useCurriculumcd;
        final String _semesterName;
        final String _subclassName;
        final String _chairName;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("CTRL_YEAR");
            _semester = request.getParameter("SEMESTER");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _date = request.getParameter("CTRL_DATE").replace('/', '-');
            _grade = request.getParameter("GRADE");
            _subclasscd = request.getParameter("SUBCLASSCD");
            _classcd = _subclasscd.substring(0, 2);
            _chairCd = request.getParameter("CHAIRCD");
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _semesterName = getSemesterName(db2);
            _subclassName = getSubclassName(db2);
            _chairName = getChairName(db2);
        }

        private String getSemesterName(final DB2UDB db2) {
            String rtn = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = " SELECT SEMESTERNAME FROM SEMESTER_MST WHERE YEAR = '" + _year + "' AND SEMESTER = '" + _semester + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    if (null != rs.getString("SEMESTERNAME")) {
                        rtn = rs.getString("SEMESTERNAME");
                    }
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }

        private String getChairName(final DB2UDB db2) {
            String rtn = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql;
                sql = " SELECT CHAIRNAME FROM CHAIR_DAT WHERE YEAR = '" + _year + "' AND SEMESTER = '" + _semester + "' AND CHAIRCD = '" + _chairCd + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    if (null != rs.getString("CHAIRNAME")) {
                        rtn = rs.getString("CHAIRNAME");
                    }
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }

        private String getSubclassName(final DB2UDB db2) {
            String rtn = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql;
                sql = " SELECT SUBCLASSNAME FROM SUBCLASS_MST WHERE "
                    + "  CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || "
                    + "  SUBCLASSCD = '" + _subclasscd + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    if (null != rs.getString("SUBCLASSNAME")) {
                        rtn = rs.getString("SUBCLASSNAME");
                    }
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }
    }
}

// eof

