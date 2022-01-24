// kanji=漢字
/*
 * $Id: 0ab514cf7abb543f95972b6a8c1f181450704eaf $
 *
 * 作成日: 2010/07/01 14:02:33 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2010 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: 0ab514cf7abb543f95972b6a8c1f181450704eaf $
 */
public class KNJD131P {

    private static final Log log = LogFactory.getLog("KNJD131P.class");

    private boolean _hasData;
    private static final String FORMNAME = "KNJD131P_1.frm";
    private static final String HREPORT_SEME = "9";
    private static final int MAXLINE = 50;

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
        final List hrClass = getHrClass(db2);
        for (final Iterator iter = hrClass.iterator(); iter.hasNext();) {
            final HrClass hrClassData = (HrClass) iter.next();
            setHeadData(svf, hrClassData);
            int lineCnt = 1;
            for (final Iterator itStudent = hrClassData._studentsList.iterator(); itStudent.hasNext();) {
                if (lineCnt > MAXLINE) {
                    svf.VrEndPage();
                    setHeadData(svf, hrClassData);
                    lineCnt = 1;
                }
                final Student student = (Student) itStudent.next();
                svf.VrsOutn("ATTENDNO", lineCnt, student._attendNo);
                svf.VrsOutn("NAME", lineCnt, student._name);
                svf.VrsOutn("SPECIALACTREMARK", lineCnt, student._specialActRemark);
                svf.VrsOutn("COMMUNICATION", lineCnt, student._communication);
                lineCnt++;
                _hasData = true;
            }
            if (lineCnt > 1) {
                svf.VrEndPage();
            }
        }
    }
    private void setHeadData(final Vrw32alp svf, final HrClass hrClassData) {
        svf.VrSetForm(FORMNAME, 1);
        svf.VrsOut("NENDO", _param._nendo);
        svf.VrsOut("SEMESTER", _param._semesterName);
        svf.VrsOut("HR_NAME", hrClassData._name);
        svf.VrsOut("STAFF_NAME", hrClassData._staffName);
        svf.VrsOut("DATE", _param._printDate);
    }

    private List getHrClass(final DB2UDB db2) throws SQLException {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        for (int i = 0; i < _param._gradeHrClasses.length; i++) {
            final String gradeHrClass = _param._gradeHrClasses[i];
            final String hrClassSql = getHrClassSql(gradeHrClass);
            try {
                ps = db2.prepareStatement(hrClassSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String grade = rs.getString("GRADE");
                    final String hrClass = rs.getString("HR_CLASS");
                    final String name = rs.getString("HR_NAME");
                    final String staffName = rs.getString("STAFFNAME");
                    final HrClass hrClassData = new HrClass(grade, hrClass, name, staffName);
                    hrClassData.setStudents(db2);
                    retList.add(hrClassData);
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
        return retList;
    }

    private String getHrClassSql(final String gradeHrClass) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.HR_NAME, ");
        stb.append("     L1.STAFFNAME ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_HDAT T1 ");
        stb.append("     LEFT JOIN STAFF_MST L1 ON T1.TR_CD1 = L1.STAFFCD ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param.getSemester() + "' ");
        stb.append("     AND T1.GRADE || T1.HR_CLASS = '" + gradeHrClass + "' ");

        return stb.toString();
    }

    private void closeDb(final DB2UDB db2) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
    }

    private class HrClass {
        private final String _grade;
        private final String _hrClass;
        private final String _name;
        private final String _staffName;
        private List _studentsList;

        public HrClass(
                final String grade,
                final String hrClass,
                final String name,
                final String staffName
        ) {
            _grade = grade;
            _name = name;
            _hrClass = hrClass;
            _staffName = staffName;
            _studentsList = new ArrayList();
        }

        private void setStudents(final DB2UDB db2) throws SQLException {
            final String studentSql = getStudentSql();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(studentSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String schregNo = rs.getString("SCHREGNO");
                    final String name = rs.getString("NAME");
                    final int attendNo = rs.getInt("ATTENDNO");
                    final String specialActRemark = rs.getString("SPECIALACTREMARK");
                    final String communication = rs.getString("COMMUNICATION");
                    final Student student = new Student(schregNo, name, attendNo, specialActRemark, communication);
                    _studentsList.add(student);
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private String getStudentSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.ATTENDNO, ");
            stb.append("     L1.NAME, ");
            stb.append("     L2.SPECIALACTREMARK, ");
            stb.append("     L2.COMMUNICATION ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT T1 ");
            stb.append("     LEFT JOIN SCHREG_BASE_MST L1 ON T1.SCHREGNO = L1.SCHREGNO ");
            stb.append("     LEFT JOIN HREPORTREMARK_DAT L2 ON T1.YEAR = L2.YEAR ");
            stb.append("          AND L2.SEMESTER = '" + HREPORT_SEME + "' ");
            stb.append("          AND T1.SCHREGNO = L2.SCHREGNO ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _param._ctrlYear + "' ");
            stb.append("     AND T1.SEMESTER = '" + _param.getSemester() + "' ");
            stb.append("     AND T1.GRADE = '" + _grade + "' ");
            stb.append("     AND T1.HR_CLASS = '" + _hrClass + "' ");
            stb.append(" ORDER BY ");
            stb.append("     T1.ATTENDNO ");

            return stb.toString();
        }
    }

    private class Student {
        private final String _schregNo;
        private final String _name;
        private final String _attendNo;
        private final String _specialActRemark;
        private final String _communication;

        public Student(
                final String schregNo,
                final String name,
                final int attendNo,
                final String specialActRemark,
                final String communication
        ) {
            _schregNo = schregNo;
            _name = name;
            _attendNo = attendNo < 10 ? "  " + attendNo : attendNo < 100 ? " " + attendNo : String.valueOf(attendNo);
            _specialActRemark = StringUtils.replace(specialActRemark, "\r\n", "");
            _communication = StringUtils.replace(communication, "\r\n", "");
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
        private final String _semester;
        private final String _grade;
        private final String[] _gradeHrClasses;
        private final String _nendo;
        private final String _semesterName;
        private final String _printDate;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _semester = request.getParameter("SEMESTER");
            _grade = request.getParameter("GRADE");
            _gradeHrClasses = request.getParameterValues("CATEGORY_SELECTED");
            _nendo = KNJ_EditDate.h_format_JP_N(_ctrlYear + "-01-01") + "度";
            _semesterName = getSemesterName(db2, _ctrlYear, _semester);
            _printDate = getPrintDate(db2);
        }

        private String getSemesterName(final DB2UDB db2, final String year, final String semester) throws SQLException {
            final String semeSql = "SELECT SEMESTERNAME FROM SEMESTER_MST WHERE YEAR = '" + year + "' AND SEMESTER = '" + semester + "' ";
            PreparedStatement ps = null;
            ResultSet rs = null;
            String semesterName = "";
            try {
                ps = db2.prepareStatement(semeSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    semesterName = rs.getString("SEMESTERNAME");
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return semesterName;
        }

        private String getPrintDate(final DB2UDB db2) throws SQLException {

            String retStr = "";
            try {
                String sql = "VALUES RTRIM(CHAR(DATE(SYSDATE()))),RTRIM(CHAR(HOUR(SYSDATE()))),RTRIM(CHAR(MINUTE(SYSDATE())))";
                db2.query(sql);
                ResultSet rs = db2.getResultSet();
                String arr_ctrl_date[] = new String[3];
                int number = 0;
                while( rs.next() ){
                    arr_ctrl_date[number] = rs.getString(1);
                    number++;
                }
                db2.commit();
                retStr = KNJ_EditDate.h_format_JP(arr_ctrl_date[0]) + arr_ctrl_date[1] + "時" + arr_ctrl_date[2] + "分";
            } finally {
                db2.commit();
            }
            return retStr;
        }

        private String getSemester () {
            return "9".equals(_semester) ? _ctrlSemester : _semester;
        }
    }
}

// eof
