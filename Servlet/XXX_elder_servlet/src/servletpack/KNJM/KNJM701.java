// kanji=漢字
/*
 * $Id: 68980b35aa027e0ba809eec68f8d1c49c1d03775 $
 *
 * 作成日: 2010/06/24 15:03:13 - JST
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
 * @version $Id: 68980b35aa027e0ba809eec68f8d1c49c1d03775 $
 */
public class KNJM701 {

    private static final Log log = LogFactory.getLog("KNJM701.class");

    private boolean _hasData;

    private static final String FORMNAME1 = "KNJM701_1.frm";
    private static final String FORMNAME2 = "KNJM701_2.frm";
    private static final int MAX_LINE = 45;

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
        for (final Iterator iter = studentList.iterator(); iter.hasNext();) {
            final Student student = (Student) iter.next();
            outPutData(svf, FORMNAME1, student);
            if (_param._attendHisPrint) {
                outPutData(svf, FORMNAME2, student);
            }
        }
    }

    private void outPutData(final Vrw32alp svf, final String formName, final Student student) {

        int lineCnt = 1;
        setPrintHead(svf, formName, student);
        final List dataList = FORMNAME1.equals(formName) ? student._printDataList : student._lastPrintDataList;
        for (final Iterator iter = dataList.iterator(); iter.hasNext();) {
            if (MAX_LINE < lineCnt) {
                svf.VrEndPage();
                setPrintHead(svf, formName, student);
                lineCnt = 1;
            }
            final PrintData printData = (PrintData) iter.next();

            svf.VrsOutn("ATTENDDATE", lineCnt, printData._attendDate.replace('-', '/'));
            String fieldName = printData._subclassName.length() > 22 ? "3" : printData._subclassName.length() > 15 ? "2" : "";
            svf.VrsOutn("SUBCLASS" + fieldName, lineCnt, printData._subclassName);
            final String periodPrint = printData._periodFName.equals(printData._periodTName) ? printData._periodFName : printData._periodFName + "\uFF5E" + printData._periodTName;
            svf.VrsOutn("PERIOD", lineCnt, periodPrint);
            final String setCre = null != printData._creditTime ? printData._creditTime.toString() : "";
            svf.VrsOutn("CREDIT_TIME", lineCnt, setCre);
            fieldName = printData._remark.length() > 20 ? "2" : "";
            svf.VrsOutn("REMARK" + fieldName, lineCnt, printData._remark);

            lineCnt++;
            _hasData = true;
        }
        if (!FORMNAME2.equals(formName) || dataList.size() > 0) {
            svf.VrEndPage();
        }
    }

    private void setPrintHead(final Vrw32alp svf, final String formName, final Student student) {
        svf.VrSetForm(formName, 1);
        svf.VrsOut("NENDO", _param._ctrlYear + "年度");
        svf.VrsOut("HR_NAME", student._hrName);
        svf.VrsOut("ATTENDNO", student._attendNo);
        svf.VrsOut("SCHREGNO", student._schregNo);
        svf.VrsOut("NAME", student._name);
        final String setLastCre = null != student._totalCreditTimeLast ? student._totalCreditTimeLast.toString() : "";
        svf.VrsOut("LAST_CREDIT_TIME", setLastCre);
        final String setThisCre = null != student._totalCreditTimeNow ? student._totalCreditTimeNow.toString() : "";
        svf.VrsOut("THIS_CREDIT_TIME", setThisCre);
        String setAddCre = "";
        if (null != student._totalCreditTimeNow && null != student._totalCreditTimeLast) {
            setAddCre = student._totalCreditTimeNow.add(student._totalCreditTimeLast).toString();
        } else if (null != student._totalCreditTimeNow) {
            setAddCre = student._totalCreditTimeNow.toString();
        } else if (null != student._totalCreditTimeLast) {
            setAddCre = student._totalCreditTimeLast.toString();
        }
        svf.VrsOut("TOTAL_CREDIT_TIME", setAddCre);
    }

    private List getStudents(DB2UDB db2) throws SQLException {

        final List retList = new ArrayList();
        final String studentSql = getStudentSql();
        PreparedStatement psStudent = null;
        ResultSet rsStudent = null;
        try {
            psStudent = db2.prepareStatement(studentSql);
            for (int i = 0; i < _param._selectDatas.length; i++) {
                final String schregNo = _param._selectDatas[i];
                psStudent.setString(1, schregNo);
                rsStudent = psStudent.executeQuery();
                while (rsStudent.next()) {
                    final String name = rsStudent.getString("NAME");
                    final int attendNo = rsStudent.getInt("ATTENDNO");
                    final String hrName = rsStudent.getString("HR_NAME");
                    final Student student = new Student(schregNo, name, attendNo, hrName);
                    student.setPrintData(db2, "THIS");
                    student.setPrintData(db2, "LAST");
                    retList.add(student);
                }
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
        stb.append("     T1.NAME, ");
        stb.append("     L1.ATTENDNO, ");
        stb.append("     L2.HR_NAME ");
        stb.append(" FROM ");
        stb.append("     SCHREG_BASE_MST T1 ");
        stb.append("     LEFT JOIN SCHREG_REGD_DAT L1 ON T1.SCHREGNO = L1.SCHREGNO ");
        stb.append("          AND L1.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("          AND L1.SEMESTER = '" + _param._ctrlSemester + "' ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT L2 ON L1.YEAR = L2.YEAR ");
        stb.append("          AND L1.SEMESTER = L2.SEMESTER ");
        stb.append("          AND L1.GRADE = L2.GRADE ");
        stb.append("          AND L1.HR_CLASS = L2.HR_CLASS ");
        stb.append(" WHERE ");
        stb.append("     T1.SCHREGNO = ? ");

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
        private final List _printDataList;
        private final List _lastPrintDataList;
        BigDecimal _totalCreditTimeNow = new BigDecimal(0.0);
        BigDecimal _totalCreditTimeLast = new BigDecimal(0.0);

        public Student(
                final String schregNo,
                final String name,
                final int attendNo,
                final String hrName
        ) {
            _schregNo = schregNo;
            _name = name;
            _attendNo = String.valueOf(attendNo);
            _hrName = hrName;
            _printDataList = new ArrayList();
            _lastPrintDataList = new ArrayList();
        }

        public void setPrintData(final DB2UDB db2, final String yearDiv) throws SQLException {
            final String printSql = getPrintSql(yearDiv);
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(printSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String attendDate = rs.getString("ATTENDDATE");
                    final String subclassName = rs.getString("SUBCLASSNAME");
                    final String periodFName = rs.getString("PERIODF_NAME");
                    final String periodTName = rs.getString("PERIODT_NAME");
                    final BigDecimal creditTime = rs.getBigDecimal("CREDIT_TIME");
                    final String remark = rs.getString("REMARK");
                    final PrintData printData = new PrintData(attendDate, subclassName, periodFName, periodTName, creditTime, remark);
                    if ("LAST".equals(yearDiv)) {
                        _totalCreditTimeLast = _totalCreditTimeLast.add(creditTime);
                        _lastPrintDataList.add(printData);
                    } else {
                        _totalCreditTimeNow = _totalCreditTimeNow.add(creditTime);
                        _printDataList.add(printData);
                    }
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private String getPrintSql(final String yearDiv) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.ATTENDDATE, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("     T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD AS SUBCLASSCD, ");
            } else {
                stb.append("     T1.SUBCLASSCD, ");
            }
            stb.append("     L1.SUBCLASSNAME, ");
            stb.append("     T1.PERIODF, ");
            stb.append("     L2.NAME1 AS PERIODF_NAME, ");
            stb.append("     T1.PERIODT, ");
            stb.append("     L3.NAME1 AS PERIODT_NAME, ");
            stb.append("     T1.CREDIT_TIME, ");
            stb.append("     T1.REMARK ");
            stb.append(" FROM ");
            stb.append("     SPECIALACT_ATTEND_DAT T1 ");
            stb.append("     LEFT JOIN SUBCLASS_MST L1 ON T1.SUBCLASSCD = L1.SUBCLASSCD ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("          AND T1.CLASSCD = L1.CLASSCD ");
                stb.append("          AND T1.SCHOOL_KIND = L1.SCHOOL_KIND ");
                stb.append("          AND T1.CURRICULUM_CD = L1.CURRICULUM_CD ");
            }
            stb.append("     LEFT JOIN NAME_MST L2 ON L2.NAMECD1 = 'B001' ");
            stb.append("          AND T1.PERIODF = L2.NAMECD2 ");
            stb.append("     LEFT JOIN NAME_MST L3 ON L3.NAMECD1 = 'B001' ");
            stb.append("          AND T1.PERIODT = L3.NAMECD2 ");
            stb.append(" WHERE ");
            if ("LAST".equals(yearDiv)) {
                stb.append("     T1.YEAR < '" + _param._ctrlYear + "' ");
            } else {
                stb.append("     T1.YEAR = '" + _param._ctrlYear + "' ");
            }
            stb.append("     AND T1.SCHREGNO = '" + _schregNo + "' ");
            stb.append(" ORDER BY ");
            stb.append("     T1.ATTENDDATE, ");
            stb.append("     T1.PERIODF, ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("     T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD ");
            } else {
                stb.append("     T1.SUBCLASSCD ");
            }

            return stb.toString();
        }

    }

    private class PrintData {
        final String _attendDate;
        final String _subclassName;
        final String _periodFName;
        final String _periodTName;
        final BigDecimal _creditTime;
        final String _remark;

        public PrintData(
                final String attendDate,
                final String subclassName,
                final String periodFName,
                final String periodTName,
                final BigDecimal creditTime,
                final String remark
        ) {
            _attendDate = attendDate;
            _subclassName = subclassName;
            _periodFName = periodFName;
            _periodTName = periodTName;
            _creditTime = creditTime;
            _remark = null != remark ? remark : "";
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
        private final String _gradeHrClass;
        private final String[] _selectDatas;
        private final boolean _attendHisPrint;
        private final String _useCurriculumcd;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _gradeHrClass = request.getParameter("GRADE_HR_CLASS");
            _selectDatas = request.getParameterValues("CATEGORY_SELECTED");
            _attendHisPrint = "1".equals(request.getParameter("ATTEND_HIST")) ? true : false;
            _useCurriculumcd = request.getParameter("useCurriculumcd");
        }

    }
}

// eof
