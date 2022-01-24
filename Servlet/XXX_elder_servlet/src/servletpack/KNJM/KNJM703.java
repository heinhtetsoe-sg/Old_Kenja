// kanji=漢字
/*
 * $Id: 545d02170c69eca311e4bad83ea16462adb54fb3 $
 *
 * 作成日: 2010/06/24 15:17:24 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2010 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJM;

import java.io.IOException;
import java.math.BigDecimal;
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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: 545d02170c69eca311e4bad83ea16462adb54fb3 $
 */
public class KNJM703 {

    private static final Log log = LogFactory.getLog("KNJM703.class");

    private boolean _hasData;

    private static final String FORMNAME = "KNJM703.frm";
    private static final int MAX_LINE = 50;

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
        final List studentList = getStudents(db2);
        outPutData(svf, studentList);
    }

    private void outPutData(final Vrw32alp svf, final List studentList) {

        int lineCnt = 1;
        String befChairCd = "";
        String befAttendDate = "";
        setPrintHead(svf);
        for (final Iterator iter = studentList.iterator(); iter.hasNext();) {
            final Student student = (Student) iter.next();

            if (MAX_LINE < lineCnt ||
                (!"".equals(befChairCd) && !befChairCd.equals(student._chairCd)) ||
                (!"".equals(befAttendDate) && !befAttendDate.equals(student._attendDate))
            ) {
                svf.VrEndPage();
                setPrintHead(svf);
                lineCnt = 1;
            }

            svf.VrsOutn("HR_NAME_ATTENDNO",lineCnt,  student._hrName + student._attendNo + "番");
            svf.VrsOutn("SCHREGNO",lineCnt,  student._schregNo);
            svf.VrsOutn("NAME",lineCnt,  student._name);
            svf.VrsOutn("CHAIRNAME", lineCnt, student._chairName);
            svf.VrsOutn("ATTENDDATE", lineCnt, student._attendDate.replace('-', '/'));
            final String setPeriod = null != student._periodF && student._periodF.equals(student._periodT) ? student._periodF : student._periodF + "\uFF5E" + student._periodT;
            svf.VrsOutn("PERIOD", lineCnt, setPeriod);
            final String setCre = null != student._creditTime ? student._creditTime.toString() : "";
            svf.VrsOutn("CREDIT_TIME", lineCnt, setCre);

            lineCnt++;
            befChairCd = student._chairCd;
            befAttendDate = student._attendDate;
            _hasData = true;
        }
        if (lineCnt > 1) {
            svf.VrEndPage();
        }
    }

    private void setPrintHead(final Vrw32alp svf) {
        svf.VrSetForm(FORMNAME, 1);
        svf.VrsOut("NENDO", _param._ctrlYear + "年度");
        svf.VrsOut("CND_SUBCLASS", _param._subClassName);
        svf.VrsOut("CND_CHAIRNAME", _param._chairName);
        svf.VrsOut("CND_ATTENDDATE", _param._printAttendDate);
        svf.VrsOut("CND_PERIOD", _param._periodName);
    }

    private List getStudents(DB2UDB db2) throws SQLException {

        final List retList = new ArrayList();
        final String studentSql = getStudentSql();
        PreparedStatement psStudent = null;
        ResultSet rsStudent = null;
        try {
            psStudent = db2.prepareStatement(studentSql);
            rsStudent = psStudent.executeQuery();
            while (rsStudent.next()) {
                final String schregNo = rsStudent.getString("SCHREGNO");
                final String name = rsStudent.getString("NAME");
                final int attendNo = rsStudent.getInt("ATTENDNO");
                final String hrName = rsStudent.getString("HR_NAME");
                final String attendDate = rsStudent.getString("ATTENDDATE");
                final String chairCd = rsStudent.getString("CHAIRCD");
                final String chairName = rsStudent.getString("CHAIRNAME");
                final String periodF = rsStudent.getString("PERIODF_NAME");
                final String periodT = rsStudent.getString("PERIODT_NAME");
                final BigDecimal creditTime = rsStudent.getBigDecimal("CREDIT_TIME");
                final Student student = new Student(schregNo, name, hrName, attendNo, attendDate, chairCd, chairName, periodF, periodT, creditTime);
                retList.add(student);
            }
        } finally {
            DbUtils.closeQuietly(null, psStudent, rsStudent);
            db2.commit();
        }
        return retList;
    }

    private String getStudentSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     BASE.NAME, ");
        stb.append("     REGD_D.ATTENDNO, ");
        stb.append("     REGD_H.HR_NAME, ");
        stb.append("     T1.ATTENDDATE, ");
        stb.append("     T1.CHAIRCD, ");
        stb.append("     L1.CHAIRNAME, ");
        stb.append("     T1.PERIODF, ");
        stb.append("     L2.NAME1 AS PERIODF_NAME, ");
        stb.append("     T1.PERIODT, ");
        stb.append("     L3.NAME1 AS PERIODT_NAME, ");
        stb.append("     T1.CREDIT_TIME, ");
        stb.append("     T1.REMARK ");
        stb.append(" FROM ");
        stb.append("     SPECIALACT_ATTEND_DAT T1 ");
        stb.append("     LEFT JOIN CHAIR_DAT L1 ON T1.YEAR = L1.YEAR ");
        stb.append("          AND L1.SEMESTER = '" + _param._ctrlSemester + "' ");
        stb.append("          AND T1.CHAIRCD = L1.CHAIRCD ");
        stb.append("     LEFT JOIN NAME_MST L2 ON L2.NAMECD1 = 'B001' ");
        stb.append("          AND T1.PERIODF = L2.NAMECD2 ");
        stb.append("     LEFT JOIN NAME_MST L3 ON L3.NAMECD1 = 'B001' ");
        stb.append("          AND T1.PERIODT = L3.NAMECD2 ");
        stb.append("     LEFT JOIN SCHREG_BASE_MST BASE ON T1.SCHREGNO = BASE.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_REGD_DAT REGD_D ON T1.SCHREGNO = REGD_D.SCHREGNO ");
        stb.append("          AND REGD_D.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("          AND REGD_D.SEMESTER = '" + _param._ctrlSemester + "' ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT REGD_H ON REGD_D.YEAR = REGD_H.YEAR ");
        stb.append("          AND REGD_D.SEMESTER = REGD_H.SEMESTER ");
        stb.append("          AND REGD_D.GRADE = REGD_H.GRADE ");
        stb.append("          AND REGD_D.HR_CLASS = REGD_H.HR_CLASS ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._ctrlYear + "' ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '" + _param._subClassCd + "' ");
        } else {
            stb.append("     AND T1.SUBCLASSCD = '" + _param._subClassCd + "' ");
        }
        if (null != _param._chairCd && _param._chairCd.length() > 0) {
            stb.append("     AND T1.CHAIRCD = '" + _param._chairCd + "' ");
        }
        if (null != _param._attendDate && _param._attendDate.length() > 0) {
            stb.append("     AND T1.ATTENDDATE = '" + _param._attendDate.replace('/', '-') + "' ");
        }
        if (null != _param._periodCd && _param._periodCd.length() > 0) {
            stb.append("     AND T1.PERIODF = '" + _param._periodCd + "' ");
        }
        stb.append(" ORDER BY ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     T1.CLASSCD, ");
            stb.append("     T1.SCHOOL_KIND, ");
            stb.append("     T1.CURRICULUM_CD, ");
        }
        stb.append("     T1.SUBCLASSCD, ");
        stb.append("     T1.CHAIRCD, ");
        stb.append("     T1.ATTENDDATE, ");
        stb.append("     T1.PERIODF, ");
        stb.append("     REGD_D.GRADE, ");
        stb.append("     REGD_D.HR_CLASS, ");
        stb.append("     REGD_D.ATTENDNO ");

        return stb.toString();
    }

    private void closeDb(final DB2UDB db2) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
    }

    private class Student {
        private final String _schregNo;
        private final String _name;
        private final String _hrName;
        private final String _attendNo;
        private final String _attendDate;
        private final String _chairCd;
        private final String _chairName;
        private final String _periodF;
        private final String _periodT;
        private final BigDecimal _creditTime;

        public Student(
                final String schregNo,
                final String name,
                final String hrName,
                final int attendNo,
                final String attendDate,
                final String chairCd,
                final String chairName,
                final String periodF,
                final String periodT, final BigDecimal creditTime
        ) {
            _schregNo = schregNo;
            _name = name;
            _attendNo = attendNo < 10 ? "  " + attendNo : attendNo < 100 ? " " + attendNo : String.valueOf(attendNo);
            _hrName = hrName;
            _attendDate = attendDate;
            _chairCd = chairCd;
            _chairName = chairName;
            _periodF = periodF;
            _periodT = periodT;
            _creditTime = creditTime;
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
        private final String _classCd;
        private final String _subClassCd;
        private final String _subClassName;
        private final String _chairCd;
        private final String _chairName;
        private final String _attendDate;
        private final String _printAttendDate;
        private final String _periodCd;
        private final String _periodName;
        private final String _useCurriculumcd;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _classCd = request.getParameter("CLASSCD");
            _subClassCd = request.getParameter("SUBCLASSCD");
            _subClassName = getSubclassName(db2, _subClassCd, _useCurriculumcd);
            _chairCd = request.getParameter("CHAIRCD");
            _chairName = getChairName(db2, _ctrlYear, _ctrlSemester, _chairCd);
            _attendDate = request.getParameter("ATTENDDATE");
            _printAttendDate = null != _attendDate && _attendDate.length() == 0 ? "全て" : _attendDate;
            _periodCd = request.getParameter("PERIODCD");
            _periodName = getPeriodName(db2, _periodCd);
        }

        private String getSubclassName(final DB2UDB db2, final String subClassCd, final String useCurriculumcd) throws SQLException {
            String retName = "";
            String subcalssSql = "SELECT SUBCLASSNAME FROM SUBCLASS_MST WHERE SUBCLASSCD = '" + subClassCd + "'";
            if ("1".equals(useCurriculumcd)) {
                subcalssSql = "SELECT SUBCLASSNAME FROM SUBCLASS_MST WHERE CLASSCD || '-' || SCHOOL_KIND || '-' ||  CURRICULUM_CD || '-' ||  SUBCLASSCD = '" + subClassCd + "'";
            }
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(subcalssSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    retName = rs.getString("SUBCLASSNAME");
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retName;
        }

        private String getChairName(final DB2UDB db2, final String year, final String semester, final String chairCd) throws SQLException {
            String retName = null != chairCd && chairCd.length() == 0 ? "全て" : "";
            final String chairSql = "SELECT CHAIRNAME FROM CHAIR_DAT WHERE YEAR = '" + year + "' AND SEMESTER = '" + semester + "' AND CHAIRCD = '" + chairCd + "'";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(chairSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    retName = rs.getString("CHAIRNAME");
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retName;
        }

        private String getPeriodName(final DB2UDB db2, final String periodCd) throws SQLException {
            String retName = null != periodCd && periodCd.length() == 0 ? "全て" : "";
            final String periodSql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'B001' AND NAMECD2 = '" + periodCd + "'";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(periodSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    retName = rs.getString("NAME1");
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retName;
        }

    }
}

// eof
