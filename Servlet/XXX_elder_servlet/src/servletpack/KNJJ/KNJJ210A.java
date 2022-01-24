// kanji=漢字
/*
 * $Id: 9c1c061f3ded7a7857ea434aa535bde42bbb34a8 $
 *
 * 作成日: 2010/10/26 11:01:36 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2010 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJJ;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: 9c1c061f3ded7a7857ea434aa535bde42bbb34a8 $
 */
public class KNJJ210A {

    private static final Log log = LogFactory.getLog("KNJJ210A.class");

    private boolean _hasData;
    private static final String FORMNAME = "KNJJ210A.frm";

    Param _param;

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
            init(response, svf);

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
            closeDb(db2);
            svf.VrQuit();
        }

    }

    private void init(
            final HttpServletResponse response,
            final Vrw32alp svf
    ) throws IOException {
        response.setContentType("application/pdf");

        svf.VrInit();
        svf.VrSetSpoolFileStream(response.getOutputStream());
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        final List gradeHrList = getGradeHr(db2);
        final Map gradeNameMap = getGradeNameMap(db2);
        final List footerList = getFooter(db2);

        for (final Iterator iter = gradeHrList.iterator(); iter.hasNext();) {

            svf.VrSetForm(FORMNAME, 4);
            final GradeHrClass gradeHrClass = (GradeHrClass) iter.next();

            setHead(svf, gradeHrClass, gradeNameMap, footerList);

            for (final Iterator itStudent = gradeHrClass._students.iterator(); itStudent.hasNext();) {
                final Student student = (Student) itStudent.next();
                svf.VrsOut("ATTENDNO", student._attendNo);
                if (_param._isNameNasi && student.isGrdStudent()) {
                    svf.VrEndRecord();
                } else {
                    svf.VrsOut("GRP1", student._attendNo);
                    svf.VrsOut("GRP2", student._attendNo);
                    svf.VrsOut("NAME", student._name);
                    for (final Iterator itPrint = student._printDataList.iterator(); itPrint.hasNext();) {
                        final PrintData printData = (PrintData) itPrint.next();
                        final String yomikaeGrade = (String) _param._yomikaeGrade.get(printData._grade);
                        svf.VrsOut("NENDO" + yomikaeGrade, printData._year);
                        svf.VrsOut("COMM_NAME" + yomikaeGrade, printData._divName);
                        svf.VrsOut("CLASS" + yomikaeGrade, printData._hrClassName1);
                        svf.VrsOut("EXECUTIVE" + yomikaeGrade, printData._executive);
                    }
                    svf.VrEndRecord();
                    if (student._printDataList2.size() > 0) {
                        svf.VrsOut("GRP1", student._attendNo);
                        svf.VrsOut("GRP2", student._attendNo);
                        for (final Iterator itPrint = student._printDataList2.iterator(); itPrint.hasNext();) {
                            final PrintData printData = (PrintData) itPrint.next();
                            final String yomikaeGrade = (String) _param._yomikaeGrade.get(printData._grade);
                            svf.VrsOut("NENDO" + yomikaeGrade, printData._year);
                            svf.VrsOut("COMM_NAME" + yomikaeGrade, printData._divName);
                            svf.VrsOut("CLASS" + yomikaeGrade, printData._hrClassName1);
                            svf.VrsOut("EXECUTIVE" + yomikaeGrade, printData._executive);
                        }
                        svf.VrEndRecord();
                    }
                }
            }
            _hasData = true;
        }
    }

    private void setHead(final Vrw32alp svf, final GradeHrClass gradeHrClass, final Map gradeNameMap, final List footerList) {
        final String nendo = _param._ctrlYear + "年度";
        svf.VrsOut("NENDO", nendo);
        final String printDate = KNJ_EditDate.h_format_JP(_param._ctrlDate);
        svf.VrsOut("DATE", printDate);
        svf.VrsOut("HR_NAME", gradeHrClass._hrName);
        final String[] grade = new String[]{"01", "02", "03", "04", "05", "06"};
        for (int i = 0; i < grade.length; i++) {
            svf.VrsOut("CLASSNAME" + grade[i], (String) gradeNameMap.get(grade[i]));
        }
        for (int i = 0; i < footerList.size(); i++) {
            svf.VrsOut("NOTICE" + String.valueOf(i + 1), (String) footerList.get(i));
        }
    }

    private Map getGradeNameMap(final DB2UDB db2) throws SQLException {
        Map gradeNameMap = new HashMap();
        final String printDataSql = getGradeInfoSql();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(printDataSql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String grade = rs.getString("GRADE");
                final String gradeName1 = rs.getString("GRADE_NAME1");
                final String setKey = (String) _param._yomikaeGrade.get(grade);
                gradeNameMap.put(setKey, gradeName1);
            }
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return gradeNameMap;
    }

    private List getFooter(final DB2UDB db2) {
        List list = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement("SELECT NAME1, ABBV1 FROM NAME_MST WHERE NAMECD1 = 'J005' ORDER BY NAMECD2 ");
            rs = ps.executeQuery();
            while (rs.next()) {
                final String abbv1 = null == rs.getString("ABBV1") ? "" : rs.getString("ABBV1");
                final String name1 = null == rs.getString("NAME1") ? "" : rs.getString("NAME1");
                final String notice = abbv1 + "←" + name1;
                list.add(notice);
            }
        } catch (SQLException ex) {
            log.error("exception!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return list;
    }

    private String getGradeInfoSql() {
        final StringBuffer stb = new StringBuffer();

        stb.append(" SELECT ");
        stb.append("     * ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_GDAT ");
        stb.append(" WHERE ");
        stb.append("     YEAR = '" + _param._ctrlYear + "' ");
        stb.append("     AND SCHOOL_KIND = '" + _param._schoolKind + "' ");

        return stb.toString();
    }

    private List getGradeHr(DB2UDB db2) throws SQLException {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        for (int i = 0; i < _param._hrClass.length; i++) {
            final String gradeHr = _param._hrClass[i];
            final String gradeHrInfoSql = getGradeHrInfoSql(gradeHr);
            try {
                ps = db2.prepareStatement(gradeHrInfoSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String grade = rs.getString("GRADE");
                    final String hrClass = rs.getString("HR_CLASS");
                    final String hrName = rs.getString("HR_NAME");
                    final GradeHrClass gradeHrClass = new GradeHrClass(db2, grade, hrClass, hrName);
                    retList.add(gradeHrClass);
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
        return retList;
    }

    private String getGradeHrInfoSql(final String gradeHr) {
        final StringBuffer stb = new StringBuffer();

        stb.append(" SELECT ");
        stb.append("     * ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_HDAT ");
        stb.append(" WHERE ");
        stb.append("     YEAR = '" + _param._ctrlYear + "' ");
        stb.append("     AND SEMESTER = '" + _param._ctrlSemester + "' ");
        stb.append("     AND GRADE || HR_CLASS = '" + gradeHr + "' ");

        return stb.toString();
    }

    private class GradeHrClass {
        final String _grade;
        final String _hrClass;
        final String _hrName;
        final List _students;
        public GradeHrClass(final DB2UDB db2, final String grade, final String hrClass, final String hrName) throws SQLException {
            _grade = grade;
            _hrClass = hrClass;
            _hrName = hrName;
            _students = getStudentsList(db2, grade, hrClass);
        }

        private List getStudentsList(final DB2UDB db2, final String grade, final String hrClass) throws SQLException {
            final List retList = new ArrayList();
            final String studentSql = getStudentSql(grade, hrClass);
            PreparedStatement psStudent = null;
            ResultSet rsStudent = null;
            try {
                psStudent = db2.prepareStatement(studentSql);
                rsStudent = psStudent.executeQuery();
                while (rsStudent.next()) {
                    final String schregNo = rsStudent.getString("SCHREGNO");
                    final String attendNo = rsStudent.getString("ATTENDNO");
                    final String name = rsStudent.getString("NAME");
                    final String grdDiv = rsStudent.getString("GRD_DIV");
                    final Student student = new Student(db2, schregNo, attendNo, name, grdDiv);
                    student.setPrintData(db2);
                    retList.add(student);
                }
            } finally {
                DbUtils.closeQuietly(null, psStudent, rsStudent);
                db2.commit();
            }
            return retList;
        }

        private String getStudentSql(final String grade, final String hrClass) {
            final StringBuffer stb = new StringBuffer();

            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.ATTENDNO, ");
            stb.append("     L1.NAME, ");
            stb.append("     L1.GRD_DIV ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT T1 ");
            stb.append("     LEFT JOIN SCHREG_BASE_MST L1 ON T1.SCHREGNO = L1.SCHREGNO ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _param._ctrlYear + "' ");
            stb.append("     AND T1.SEMESTER = '" + _param._ctrlSemester + "' ");
            stb.append("     AND T1.GRADE = '" + grade + "' ");
            stb.append("     AND T1.HR_CLASS = '" + hrClass + "' ");
            stb.append(" ORDER BY ");
            stb.append("     T1.ATTENDNO ");

            return stb.toString();
        }
    }

    private class Student {
        final String _schregNo;
        final String _attendNo;
        final String _name;
        final String _grdDiv;
        final List _printDataList;
        final List _printDataList2;

        public Student(final DB2UDB db2, final String schregNo, final String attendNo, final String name, final String grdDiv) {
            _schregNo = schregNo;
            _attendNo = attendNo;
            _name = name;
            _grdDiv = grdDiv;
            _printDataList = new ArrayList();
            _printDataList2 = new ArrayList();
        }

        /**
         * 退学者・転学者・卒業生か？
         */
        private boolean isGrdStudent() {
            return "1".equals(_grdDiv) || "2".equals(_grdDiv) || "3".equals(_grdDiv);
        }

        public void setPrintData(final DB2UDB db2) throws SQLException {
            final String printDataSql = getPrintDataSql();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(printDataSql);
                rs = ps.executeQuery();
                String befGrade = "";
                while (rs.next()) {
                    final String year = rs.getString("YEAR");
                    final String grade = rs.getString("GRADE");
                    final String hrClassName1 = rs.getString("HR_CLASS_NAME1");
                    final String divName = rs.getString("DIVNAME");
                    final String executive = rs.getString("EXECUTIVE");
                    final PrintData printData = new PrintData(year, grade, hrClassName1, divName, executive);
                    if (!befGrade.equals(grade)) {
                        _printDataList.add(printData);
                    } else {
                        _printDataList2.add(printData);
                    }
                    befGrade = grade;
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        /**
         * @return
         */
        private String getPrintDataSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH REGD_YMAX AS ( ");
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.GRADE, ");
            stb.append("     MAX(T1.YEAR) AS YEAR ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.SCHREGNO = '" + _schregNo + "' ");
            stb.append(" GROUP BY ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.GRADE ");
            stb.append(" ), REGD_MMAX AS ( ");
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.GRADE, ");
            stb.append("     T1.YEAR, ");
            stb.append("     MAX(T1.SEMESTER) AS SEMESTER ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT T1, ");
            stb.append("     REGD_YMAX T2 ");
            stb.append(" WHERE ");
            stb.append("     T1.SCHREGNO = T2.SCHREGNO ");
            stb.append("     AND T1.YEAR = T2.YEAR ");
            stb.append(" GROUP BY ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.GRADE, ");
            stb.append("     T1.YEAR ");
            stb.append(" ), REGD_T AS ( ");
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.GRADE, ");
            stb.append("     T1.YEAR, ");
            stb.append("     T1.SEMESTER, ");
            stb.append("     L1.HR_CLASS_NAME1 ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT T1 ");
            stb.append("     LEFT JOIN SCHREG_REGD_HDAT L1 ON T1.YEAR = L1.YEAR ");
            stb.append("          AND T1.SEMESTER = L1.SEMESTER ");
            stb.append("          AND T1.GRADE = L1.GRADE ");
            stb.append("          AND T1.HR_CLASS = L1.HR_CLASS ");
            stb.append("     INNER JOIN SCHREG_REGD_GDAT REGD_G ON T1.YEAR = REGD_G.YEAR ");
            stb.append("          AND T1.GRADE = REGD_G.GRADE ");
            stb.append("          AND REGD_G.SCHOOL_KIND = '" + _param._schoolKind + "', ");
            stb.append("     REGD_MMAX T2 ");
            stb.append(" WHERE ");
            stb.append("     T1.SCHREGNO = T2.SCHREGNO ");
            stb.append("     AND T1.YEAR = T2.YEAR ");
            stb.append("     AND T1.SEMESTER = T2.SEMESTER ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     T1.YEAR, ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.GRADE, ");
            stb.append("     T1.HR_CLASS_NAME1, ");
            stb.append("     L1.DIV, ");
            stb.append("     L1.EXECUTIVECD, ");
            stb.append("     L2.NAME1 AS DIVNAME, ");
            stb.append("     L3.ABBV AS EXECUTIVE ");
            stb.append(" FROM ");
            stb.append("     REGD_T T1 ");
            stb.append("     INNER JOIN GUARDIAN_COMMITTEE_DAT L1 ON T1.YEAR = L1.YEAR ");
            stb.append("          AND T1.SCHREGNO = L1.SCHREGNO ");
            stb.append("     LEFT JOIN NAME_MST L2 ON L2.NAMECD1 = 'J006' ");
            stb.append("          AND L1.DIV = L2.NAMECD2 ");
            stb.append("     LEFT JOIN GUARDIAN_COMMITTEE_MST L3 ON L1.DIV = L3.DIV ");
            stb.append("          AND L1.EXECUTIVECD = L3.EXECUTIVECD ");
            stb.append(" ORDER BY ");
            stb.append("     T1.GRADE, ");
            stb.append("     L1.DIV ");

            return stb.toString();
        }
    }

    private class PrintData {
        final String _year;
        final String _grade;
        final String _hrClassName1;
        final String _divName;
        final String _executive;
        public PrintData(final String year, final String grade, final String hrClassName1, final String divName, final String executive) {
            _year = year;
            _grade = grade;
            _hrClassName1 = hrClassName1;
            _divName = divName;
            _executive = executive;
        }
    }

    private void closeDb(final DB2UDB db2) {
        if (null != db2) {
            db2.commit();
            db2.close();
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
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final String _schoolKind;
        private final String[] _hrClass;
        private final Map _yomikaeGrade;
        /** 退学者・転学者・卒業生は空欄にするか？ */
        private final boolean _isNameNasi;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _schoolKind = request.getParameter("SCHOOL_KIND");
            _hrClass = request.getParameterValues("CLASS_SELECTED");
            final String nameNasi = request.getParameter("NAME_NASI");
            _isNameNasi = "1".equals(nameNasi);
            _yomikaeGrade = getYomikaeGrade(db2);
        }

        private Map getYomikaeGrade(DB2UDB db2) throws SQLException {
            final Map retMap = new HashMap();
            final String sql = "SELECT * FROM NAME_MST WHERE NAMECD1 = 'A023' AND NAME1 = '" + _schoolKind + "' ";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                int startGrade = 0;
                int endGrade = 0;
                while (rs.next()) {
                    startGrade = rs.getInt("NAME2");
                    endGrade = rs.getInt("NAME3");
                }
                int setVal = 1;
                for (int i = startGrade; i <= endGrade; i++) {
                    final String setKey = i > 9 ? String.valueOf(i) : "0" + String.valueOf(i);
                    retMap.put(setKey, "0" + String.valueOf(setVal));
                    setVal++;
                }
            } catch (SQLException e) {
                log.debug("exception!", e);
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retMap;
        }

    }
}


// eof
