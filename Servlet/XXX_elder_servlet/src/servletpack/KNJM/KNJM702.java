// kanji=漢字
/*
 * $Id: d2a683bc92ee9f0bd243ae7542a59b9873996860 $
 *
 * 作成日: 2010/06/24 15:17:06 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2010 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJM;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
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

import servletpack.KNJZ.detail.CsvUtils;
import servletpack.KNJZ.detail.KNJServletUtils;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: d2a683bc92ee9f0bd243ae7542a59b9873996860 $
 */
public class KNJM702 {

    private static final Log log = LogFactory.getLog("KNJM702.class");

    private boolean _hasData;

    private static final String FORMNAME = "KNJM702.frm";
    private static final int MAX_LINE = 50;

    private Param _param;

    /**
     * @param request リクエスト
     * @param response レスポンス
     */
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {
        
        Vrw32alp svf = null;
        DB2UDB db2 = null;
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);

            if ("csv".equals(_param._cmd)) {
                outputCsv(db2, response);
            } else {
                svf = new Vrw32alp();
                        
                init(response, svf);
                
                _hasData = false;
                printMain(db2, svf);
                if (!_hasData) {
                    svf.VrSetForm("MES001.frm", 0);
                    svf.VrsOut("note", "note");
                    svf.VrEndPage();
                }
            }
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            closeDb(db2);
            if (null != svf) {
                svf.VrQuit();
            }
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
        final List hrClassList = getHrClass(db2);
        for (final Iterator iter = hrClassList.iterator(); iter.hasNext();) {
            final HrClass hrClass = (HrClass) iter.next();
            outPutData(svf, hrClass);
        }
    }

    private void outPutData(final Vrw32alp svf, final HrClass hrClass) {

        int lineCnt = 1;
        setPrintHead(svf, hrClass);
        for (final Iterator iter = hrClass._studentList.iterator(); iter.hasNext();) {
            if (MAX_LINE < lineCnt) {
                svf.VrEndPage();
                setPrintHead(svf, hrClass);
                lineCnt = 1;
            }
            final Student student = (Student) iter.next();

            svf.VrsOutn("ATTENDNO", lineCnt, student._attendNo);
            svf.VrsOutn("SCHREGNO", lineCnt, student._schregNo);
            svf.VrsOutn("NAME", lineCnt, student._name);
            final String setCreditBorder = null != student._creditBorder ? student._creditBorder.toString() : "";
            svf.VrsOutn("SP_TIME", lineCnt, setCreditBorder);
            final String setThisCre = null != student._totalCreditTimeNow ? student._totalCreditTimeNow.toString() : "";
            svf.VrsOutn("THIS_CREDIT_TIME", lineCnt, setThisCre);
            final String setLastCre = null != student._totalCreditTimeLast ? student._totalCreditTimeLast.toString() : "";
            svf.VrsOutn("LAST_CREDIT_TIME", lineCnt, setLastCre);
            String setAddCre = "";
            if (null != student._totalCreditTimeNow && null != student._totalCreditTimeLast) {
                setAddCre = student._totalCreditTimeNow.add(student._totalCreditTimeLast).toString();
            } else if (null != student._totalCreditTimeNow) {
                setAddCre = student._totalCreditTimeNow.toString();
            } else if (null != student._totalCreditTimeLast) {
                setAddCre = student._totalCreditTimeLast.toString();
            }
            svf.VrsOutn("TOTAL_CREDIT_TIME", lineCnt, setAddCre);

            final String setShortageCre = null != student._fusokuCre ? String.valueOf(student._fusokuCre.doubleValue()) : "";
            svf.VrsOutn("SHORTAGE_TIME", lineCnt, "0.0".equals(setShortageCre) ? "" : setShortageCre);

            lineCnt++;
            _hasData = true;
        }
        if (lineCnt > 1) {
            svf.VrEndPage();
        }
    }

    private void setPrintHead(final Vrw32alp svf, final HrClass hrClass) {
        svf.VrSetForm(FORMNAME, 1);
        svf.VrsOut("NENDO", _param._ctrlYear + "年度");
        svf.VrsOut("HR_NAME", hrClass._hrName);
    }
    
    private void outputCsv(final DB2UDB db2, final HttpServletResponse response) throws SQLException {
        
        final List lines = new ArrayList();
        
        final String title = _param._ctrlYear + "年度" + "　特別活動出席履歴（クラス別）";
        CsvUtils.newLine(lines).add(title);
        CsvUtils.newLine(lines);

        final List hrClassList = getHrClass(db2);
        for (final Iterator iter = hrClassList.iterator(); iter.hasNext();) {
            final HrClass hrClass = (HrClass) iter.next();
            outPutDataCsv(lines, hrClass);
        }
        
        CsvUtils.outputLines(log, response, title + ".csv", lines);
    }


    private void outPutDataCsv(final List lines, final HrClass hrClass) {

        CsvUtils.newLine(lines).add(hrClass._hrName);
        CsvUtils.newLine(lines).addAll(Arrays.asList(new String[] {"出席番号", "学籍番号", "氏名", "特活時数", "今年度単位時間合計", "前年度までの単位時間合計", "単位時間総合計", "不足時間"}));
        for (final Iterator iter = hrClass._studentList.iterator(); iter.hasNext();) {

            final Student student = (Student) iter.next();
            
            final List studentLine = CsvUtils.newLine(lines);

            studentLine.add(student._attendNo);
            studentLine.add(student._schregNo);
            studentLine.add(student._name);
            final String setCreditBorder = null != student._creditBorder ? student._creditBorder.toString() : "";
            studentLine.add(setCreditBorder);
            final String setThisCre = null != student._totalCreditTimeNow ? student._totalCreditTimeNow.toString() : "";
            studentLine.add(setThisCre);
            final String setLastCre = null != student._totalCreditTimeLast ? student._totalCreditTimeLast.toString() : "";
            studentLine.add(setLastCre);
            String setAddCre = "";
            if (null != student._totalCreditTimeNow && null != student._totalCreditTimeLast) {
                setAddCre = student._totalCreditTimeNow.add(student._totalCreditTimeLast).toString();
            } else if (null != student._totalCreditTimeNow) {
                setAddCre = student._totalCreditTimeNow.toString();
            } else if (null != student._totalCreditTimeLast) {
                setAddCre = student._totalCreditTimeLast.toString();
            }
            studentLine.add(setAddCre);

            final String setShortageCre = null != student._fusokuCre ? String.valueOf(student._fusokuCre.doubleValue()) : "";
            studentLine.add("0.0".equals(setShortageCre) ? "" : setShortageCre);

            _hasData = true;
        }
        
        CsvUtils.newLine(lines); // 空行
        CsvUtils.newLine(lines); // 空行
    }

    private List getHrClass(DB2UDB db2) throws SQLException {

        final List retList = new ArrayList();
        final String hrClassSql = getHrClass();
        PreparedStatement psHrClass = null;
        ResultSet rsHrClass = null;
        try {
            psHrClass = db2.prepareStatement(hrClassSql);
            for (int i = 0; i < _param._selectDatas.length; i++) {
                final String gradeHr = _param._selectDatas[i];
                psHrClass.setString(1, gradeHr);
                rsHrClass = psHrClass.executeQuery();
                while (rsHrClass.next()) {
                    final String grade = rsHrClass.getString("GRADE");
                    final String hrClassCd = rsHrClass.getString("HR_CLASS");
                    final String hrName = rsHrClass.getString("HR_NAME");
                    final HrClass hrClass = new HrClass(grade, hrClassCd, hrName);
                    hrClass.setStudent(db2);
                    retList.add(hrClass);
                }
            }
        } finally {
            DbUtils.closeQuietly(null, psHrClass, rsHrClass);
            db2.commit();
        }
        return retList;
    }

    private String getHrClass() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     GRADE, ");
        stb.append("     HR_CLASS, ");
        stb.append("     HR_NAME ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_HDAT ");
        stb.append(" WHERE ");
        stb.append("     YEAR = '" + _param._ctrlYear + "' ");
        stb.append("     AND SEMESTER = '" + _param._ctrlSemester + "' ");
        stb.append("     AND GRADE || HR_CLASS = ? ");

        return stb.toString();
    }

    private class HrClass {
        private final String _grade;
        private final String _hrClass;
        private final String _hrName;
        private final List _studentList;

        public HrClass(
                final String grade,
                final String hrClass,
                final String hrName
        ) {
            _grade = grade;
            _hrClass = hrClass;
            _hrName = hrName;
            _studentList = new ArrayList();
        }

        public void setStudent(final DB2UDB db2) throws SQLException {
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
                    final Student student = new Student(schregNo, name, attendNo);
                    student.setPrintData(db2);
                    _studentList.add(student);
                }
            } finally {
                DbUtils.closeQuietly(null, psStudent, rsStudent);
                db2.commit();
            }
        }

        private String getStudentSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     L1.NAME, ");
            stb.append("     T1.ATTENDNO ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT T1 ");
            stb.append("     LEFT JOIN SCHREG_BASE_MST L1 ON T1.SCHREGNO = L1.SCHREGNO ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _param._ctrlYear + "' ");
            stb.append("     AND T1.SEMESTER = '" + _param._ctrlSemester + "' ");
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
        BigDecimal _totalCreditTimeNow = new BigDecimal(0.0);
        BigDecimal _totalCreditTimeLast = new BigDecimal(0.0);
        BigDecimal _totalCredit = new BigDecimal(0.0);
        BigDecimal _fusokuCre = new BigDecimal(0.0);
        BigDecimal _creditBorder = null;
        
        public Student(
                final String schregNo,
                final String name,
                final int attendNo
        ) {
            _schregNo = schregNo;
            _name = name;
            _attendNo = String.valueOf(attendNo);
        }

        public void setPrintData(final DB2UDB db2) throws SQLException {
            setNowCredit(db2);
            setLastCredit(db2);
            setCreditBorder(db2);
            if (null != _creditBorder && _creditBorder.doubleValue() > 0.0 && _creditBorder.compareTo(_totalCredit) > 0) {
                _fusokuCre = _creditBorder.subtract(_totalCredit);
            }
        }

        public void setNowCredit(final DB2UDB db2) throws SQLException {
            final String nowCreditSql = getNowCredit();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(nowCreditSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    _totalCreditTimeNow = rs.getBigDecimal("CREDIT_TIME");
                    _totalCredit = null != _totalCreditTimeNow ? _totalCredit.add(_totalCreditTimeNow) : _totalCredit;
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private String getNowCredit() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     SUM(T1.CREDIT_TIME) AS CREDIT_TIME ");
            stb.append(" FROM ");
            stb.append("     SPECIALACT_ATTEND_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _param._ctrlYear + "' ");
            stb.append("     AND T1.SCHREGNO = '" + _schregNo + "' ");

            return stb.toString();
        }

        public void setLastCredit(final DB2UDB db2) throws SQLException {
            final String lastCreditSql = getLastCredit();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(lastCreditSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    _totalCreditTimeLast = rs.getBigDecimal("CREDIT_TIME");
                    _totalCredit = null != _totalCreditTimeLast ? _totalCredit.add(_totalCreditTimeLast) : _totalCredit;
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private String getLastCredit() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     SUM(T1.CREDIT_TIME) AS CREDIT_TIME ");
            stb.append(" FROM ");
            stb.append("     SPECIALACT_ATTEND_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR < '" + _param._ctrlYear + "' ");
            stb.append("     AND T1.SCHREGNO = '" + _schregNo + "' ");

            return stb.toString();
        }

        public void setCreditBorder(final DB2UDB db2) throws SQLException {
            final String creditBorderSql = getCreditBorder();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(creditBorderSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    _creditBorder = rs.getBigDecimal("CREDIT_BORDER");
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        public String getCreditBorder() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T2.NAME1 AS CREDIT_BORDER ");
            stb.append(" FROM ");
            stb.append("     SCHREG_BASE_DETAIL_MST T1 ");
            stb.append("     INNER JOIN NAME_MST T2 ON T2.NAMECD1 = 'M013' ");
            stb.append("         AND T2.NAMECD2 = BASE_REMARK2 ");
            stb.append(" WHERE ");
            stb.append("     T1.SCHREGNO = '" + _schregNo + "' ");
            stb.append("     AND T1.BASE_SEQ = '004' ");
            stb.append("     AND T2.NAME1 IS NOT NULL ");
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
        private final String _year;
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final String[] _selectDatas;
        private final String _cmd;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _selectDatas = request.getParameterValues("CATEGORY_SELECTED");
            _cmd = request.getParameter("cmd");
        }

    }
}

// eof
