// kanji=漢字
/*
 * $Id: 5e93e385c92dd1eb76a0d494e230cab454ea372a $
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
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: 5e93e385c92dd1eb76a0d494e230cab454ea372a $
 */
public class KNJM701D {

    private static final Log log = LogFactory.getLog("KNJM701D.class");

    private boolean _hasData;

    private static final String FORMNAME1 = "KNJM701D_1.frm";
    private static final String FORMNAME2 = "KNJM701D_2.frm";
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
            outPutData(db2, svf, FORMNAME1, student);
            if (_param._attendHisPrint) {
                outPutData(db2, svf, FORMNAME2, student);
            }
        }
    }

    private void outPutData(final DB2UDB db2, final Vrw32alp svf, final String formName, final Student student) {

        int lineCnt = 1;
        //データが空でも出力する
        _hasData = true;
        setPrintHead(svf, formName, student);
        final List dataList = FORMNAME1.equals(formName) ? student._printDataList : student._lastPrintDataList;
        for (final Iterator iter = dataList.iterator(); iter.hasNext();) {
            if (MAX_LINE < lineCnt) {
                svf.VrEndPage();
                setPrintHead(svf, formName, student);
                lineCnt = 1;
            }
            final PrintData printData = (PrintData) iter.next();
            String termDateStr = "";
            final String sDStr = StringUtils.defaultString(printData._special_Sdate, "");
            if (!"".equals(sDStr)) {
            	termDateStr = KNJ_EditDate.getAutoFormatDate(db2, sDStr);
            }
            final String sEStr = StringUtils.defaultString(printData._special_Edate, "");
            if (!"".equals(sEStr)) {
            	termDateStr += ("".equals(termDateStr) ? "" : "～")+ KNJ_EditDate.getAutoFormatDate(db2, sEStr);
            }
            svf.VrsOutn("PEROPD", lineCnt, termDateStr);  //期間

            String fieldName = printData._spact_Name.length() > 44 ? "3" : printData._spact_Name.length() > 30 ? "2" : "";
            svf.VrsOutn("SP_ACT" + fieldName, lineCnt, printData._spact_Name);  //特別活動内容

            final String setCre = null != printData._spact_Time ? printData._spact_Time.toString() : "";
            svf.VrsOutn("CREDIT_TIME", lineCnt, setCre);  //単位時間

            lineCnt++;
        }
        //ヘッダだけでも出力するよう、変更
        svf.VrEndPage();
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
                	final String special_Sdate = rs.getString("SPECIAL_SDATE");
                	final String special_Edate = rs.getString("SPECIAL_EDATE");
                	final String specialCd = rs.getString("SPECIALCD");
                	final String spact_Name = rs.getString("SPACT_NAME");
                    final BigDecimal spact_Time = rs.getBigDecimal("SPACT_TIME");
                    final PrintData printData = new PrintData(special_Sdate, special_Edate, specialCd, spact_Name, spact_Time);
                    if ("LAST".equals(yearDiv)) {
                        if (spact_Time != null) {
                            _totalCreditTimeLast = _totalCreditTimeLast.add(spact_Time);
                        }
                        _lastPrintDataList.add(printData);
                    } else {
                        if (spact_Time != null) {
                            _totalCreditTimeNow = _totalCreditTimeNow.add(spact_Time);
                        }
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
            stb.append("     T2.SPECIAL_SDATE, ");
            stb.append("     T2.SPECIAL_EDATE, ");
            stb.append("     T1.SPECIALCD, ");
            stb.append("     T2.SPECIALACTIVITYNAME AS SPACT_NAME, ");
            stb.append("     T2.SPECIALACTIVITYTIME AS SPACT_TIME ");
            stb.append(" FROM ");
            stb.append("     SPECIAL_ACTIVITY_DAT T1 ");
            stb.append("     LEFT JOIN SPECIAL_ACTIVITY_MST T2 ");
            stb.append("       ON T2.YEAR = T1.YEAR ");
            stb.append("      AND T2.SPECIALCD = T1.SPECIALCD ");
            stb.append(" WHERE ");
            if ("LAST".equals(yearDiv)) {
                stb.append("     T1.YEAR < '" + _param._ctrlYear + "' ");
            } else {
                stb.append("     T1.YEAR = '" + _param._ctrlYear + "' ");
            }
            stb.append("     AND T1.SCHREGNO = '" + _schregNo + "' ");
            stb.append("     AND T1.SPECIAL_FLG = '1' ");
            stb.append(" ORDER BY ");
            stb.append("     T2.SPECIAL_SDATE, ");
            stb.append("     T2.SPECIAL_EDATE, ");
            stb.append("     T1.SPECIALCD ");

            return stb.toString();
        }

    }

    private class PrintData {
        final String _special_Sdate;
        final String _special_Edate;
        final String _specialCd;
        final String _spact_Name;
        final BigDecimal _spact_Time;

        public PrintData (
        		  final String special_Sdate,
        		  final String special_Edate,
        		  final String specialCd,
        		  final String spact_Name,
        		  final BigDecimal spact_Time
        ) {
            _special_Sdate = special_Sdate;
            _special_Edate = special_Edate;
            _specialCd = specialCd;
            _spact_Name = spact_Name;
            _spact_Time = spact_Time;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 70583 $");
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
