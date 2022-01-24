/*
 * $Id: 10ae85ee9ab9c0749fd6cd94cd6ddd1b968a0784 $
 *
 * 作成日: 2017/11/21
 * 作成者: yamashiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

public class KNJD231D {

    private static final Log log = LogFactory.getLog(KNJD231D.class);

    private static final String PRINT_WARNING = "warning";
    private static final String PRINT_LIST = "list";
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
        final List printList = getList(db2);
        if (PRINT_WARNING.equals(_param._formKind)) {
            printKeikoku(db2, svf, printList);
        } else {
            printList(db2, svf, printList);
        }
    }

    private void printKeikoku(final DB2UDB db2, final Vrw32alp svf, final List printList) {
    	final String formatDate = KNJ_EditDate.h_format_JP(db2, _param._date);
        svf.VrSetForm("KNJD231D_1.frm", 1);
        for (Iterator iterator = printList.iterator(); iterator.hasNext();) {
            final StudentData studentData = (StudentData) iterator.next();
			svf.VrsOut("DATE", formatDate);
            svf.VrsOut("GUARD_NAME", studentData._guardName + "　様");
            svf.VrsOut("SCHOOLNAME", _param._certifSchool._schoolName);
            svf.VrsOut("PRINCIPALNAME", _param._certifSchool._jobName + _param._certifSchool._principalName);
            final String title;
            final String setSentence1;
            if (_param.isGakkimatsu()) {
                title = _param._semestername + "成績についての連絡(警告)";
                setSentence1 = studentData._hrName + "　" + studentData._name + "さんは、" +  _param._semestername + "の成績において、下記の科目が不合格でした。この状況が改善されなければ、";
            } else {
                title = _param._semestername + _param._testitemname + "成績についての連絡(警告)";
                setSentence1 = studentData._hrName + "　" + studentData._name + "さんは、" +  _param._semestername + _param._testitemname + "の成績において、下記の科目が不合格でした。この状況が改善されなければ、";
            }
            svf.VrsOut("TITLE", title);
            final String setSentence2 = studentData._shinkyuDiv + "できない事態の起こることが憂慮されますことをお知らせいたします。";
            svf.VrsOut("SENTENCE1", setSentence1 + setSentence2);

            int lineCnt = 1;
            for (Iterator itPrint = studentData._printDataList.iterator(); itPrint.hasNext();) {
                final PrintData printData = (PrintData) itPrint.next();
                svf.VrsOutn("SUBCLASSNAME", lineCnt, printData._subclassname);
                svf.VrsOutn("SCORE", lineCnt, printData._score);
                svf.VrsOutn("CREDITS", lineCnt, printData._credits);
                lineCnt++;
            }

            svf.VrEndPage();
            _hasData = true;
        }
    }

    private void printList(final DB2UDB db2, final Vrw32alp svf, final List printList) {
        svf.VrSetForm("KNJD231D_2.frm", 4);
        svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(db2, _param._date));
        svf.VrsOut("NENDO", KNJ_EditDate.h_format_JP_N(db2, _param._ctrlYear + "-04-01"));
        svf.VrsOut("TESTNAME", "(" + _param._semestername + _param._testitemname + ")");
        int lineCnt = 1;
        for (Iterator iterator = printList.iterator(); iterator.hasNext();) {
            final StudentData studentData = (StudentData) iterator.next();
            svf.VrsOut("HR_NAME", studentData._hrName);
            svf.VrsOut("ATTENDNO", studentData._attendno);
            svf.VrsOut("NAME", studentData._name);
            svf.VrsOut("TOTAL_CREDITS", String.valueOf(studentData._totalCredit));
            for (Iterator itPrint = studentData._printDataList.iterator(); itPrint.hasNext();) {
                final PrintData printData = (PrintData) itPrint.next();
                svf.VrsOut("SUBCLASSNAME", printData._subclassname);
                svf.VrsOut("SCORE", printData._score);
                svf.VrsOut("CREDITS", printData._credits);
                svf.VrsOut("SHIRO1", String.valueOf(lineCnt));
                svf.VrsOut("SHIRO2", String.valueOf(lineCnt));
                svf.VrsOut("SHIRO3", String.valueOf(lineCnt));

                svf.VrEndRecord();
                _hasData = true;
            }
            lineCnt++;
        }
    }

    private List getList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            String befSchregNo = "";
            StudentData studentData = null;
            while (rs.next()) {
                final String schregno = rs.getString("SCHREGNO");
                final String grade = rs.getString("GRADE");
                final String hrClass = rs.getString("HR_CLASS");
                final String hrName = rs.getString("HR_NAME");
                final String attendno = rs.getString("ATTENDNO");
                final String nameShow = rs.getString("NAME_SHOW");
                final String name = rs.getString("NAME");
                final String shinkyuDiv = rs.getString("SHINKYU_DIV");
                final String guardName = rs.getString("GUARD_NAME");
                final String classcd = rs.getString("CLASSCD");
                final String schoolKind = rs.getString("SCHOOL_KIND");
                final String curriculumCd = rs.getString("CURRICULUM_CD");
                final String subclasscd = rs.getString("SUBCLASSCD");
                final String subclassname = rs.getString("SUBCLASSNAME");
                final String score = rs.getString("SCORE");
                final String credits = rs.getString("CREDITS");

                if (!befSchregNo.equals(schregno)) {
                    studentData = new StudentData(schregno, grade, hrClass, hrName, attendno, nameShow, name, shinkyuDiv, guardName);
                    retList.add(studentData);
                }
                studentData.setPrintDataList(classcd, schoolKind, curriculumCd, subclasscd, subclassname, score, credits);
                befSchregNo = schregno;
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String getSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH MAIN AS ( ");
        stb.append("     SELECT DISTINCT ");
        stb.append("         T1.SCHREGNO, ");
        stb.append("         S1.GRADE, ");
        stb.append("         S1.HR_CLASS, ");
        stb.append("         REGD_H.HR_NAME, ");
        stb.append("         S1.ATTENDNO, ");
        stb.append("         S2.NAME_SHOW, ");
        stb.append("         S2.NAME, ");
        stb.append("         CASE WHEN A023.NAMECD1 IS NOT NULL ");
        stb.append("              THEN '卒業' ");
        stb.append("              ELSE '進級' ");
        stb.append("         END AS SHINKYU_DIV, ");
        stb.append("         GUARDIAN.GUARD_NAME, ");
        stb.append("         L1.SEMESTERNAME, ");
        stb.append("         L2.TESTITEMNAME, ");
        stb.append("         T1.CLASSCD, ");
        stb.append("         T1.SCHOOL_KIND, ");
        stb.append("         T1.CURRICULUM_CD, ");
        stb.append("         T1.SUBCLASSCD, ");
        stb.append("         L3.SUBCLASSNAME, ");
        stb.append("         T1.SCORE, ");
        stb.append("         L4.CREDITS ");
        stb.append("     FROM ");
        stb.append("         RECORD_SCORE_DAT T1 ");
        stb.append("         INNER JOIN SCHREG_REGD_DAT S1 ");
        stb.append("              ON T1.YEAR         = S1.YEAR ");
        if ("9".equals(_param._semester)) {
            stb.append("             AND S1.SEMESTER     = '" + _param._ctrlSemester + "' ");
        } else {
            stb.append("             AND T1.SEMESTER     = S1.SEMESTER ");
        }
        stb.append("             AND T1.SCHREGNO     = S1.SCHREGNO ");
        stb.append("             AND S1.GRADE || '-' || S1.HR_CLASS = '" + _param._gradeHrClass + "' ");
        stb.append("         INNER JOIN SCHREG_REGD_HDAT REGD_H ");
        stb.append("              ON S1.YEAR         = REGD_H.YEAR ");
        stb.append("             AND S1.SEMESTER     = REGD_H.SEMESTER ");
        stb.append("             AND S1.GRADE = REGD_H.GRADE ");
        stb.append("             AND S1.HR_CLASS = REGD_H.HR_CLASS ");
        stb.append("         INNER JOIN SCHREG_REGD_GDAT GDAT ");
        stb.append("              ON GDAT.YEAR = S1.YEAR ");
        stb.append("             AND GDAT.GRADE = S1.GRADE ");
        stb.append("         LEFT JOIN NAME_MST A023 ON A023.NAMECD1 = 'A023' ");
        stb.append("             AND GDAT.SCHOOL_KIND     = A023.NAME1 ");
        stb.append("             AND GDAT.GRADE BETWEEN A023.NAMESPARE2 AND A023.NAMESPARE3 ");
        stb.append("         INNER JOIN SCHREG_BASE_MST S2 ON S2.SCHREGNO = T1.SCHREGNO ");
        stb.append("         LEFT JOIN GUARDIAN_DAT GUARDIAN ON GUARDIAN.SCHREGNO = T1.SCHREGNO ");
        stb.append("         LEFT JOIN SEMESTER_MST L1 ");
        stb.append("              ON T1.YEAR         = L1.YEAR ");
        stb.append("             AND T1.SEMESTER     = L1.SEMESTER ");
        stb.append("         LEFT JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV L2 ");
        stb.append("              ON T1.YEAR         = L2.YEAR ");
        stb.append("             AND T1.SEMESTER     = L2.SEMESTER ");
        stb.append("             AND T1.TESTKINDCD   = L2.TESTKINDCD ");
        stb.append("             AND T1.TESTITEMCD   = L2.TESTITEMCD ");
        stb.append("             AND T1.SCORE_DIV    = L2.SCORE_DIV ");
        stb.append("         LEFT JOIN SUBCLASS_MST L3 ");
        stb.append("              ON T1.CLASSCD          = L3.CLASSCD ");
        stb.append("             AND T1.SCHOOL_KIND      = L3.SCHOOL_KIND ");
        stb.append("             AND T1.CURRICULUM_CD    = L3.CURRICULUM_CD ");
        stb.append("             AND T1.SUBCLASSCD       = L3.SUBCLASSCD ");
        stb.append("         LEFT JOIN CREDIT_MST L4 ");
        stb.append("              ON T1.YEAR             = L4.YEAR ");
        stb.append("             AND S1.COURSECD         = L4.COURSECD ");
        stb.append("             AND S1.MAJORCD          = L4.MAJORCD ");
        stb.append("             AND S1.GRADE            = L4.GRADE ");
        stb.append("             AND S1.COURSECODE       = L4.COURSECODE ");
        stb.append("             AND T1.CLASSCD          = L4.CLASSCD ");
        stb.append("             AND T1.SCHOOL_KIND      = L4.SCHOOL_KIND ");
        stb.append("             AND T1.CURRICULUM_CD    = L4.CURRICULUM_CD ");
        stb.append("             AND T1.SUBCLASSCD       = L4.SUBCLASSCD ");
        stb.append("     WHERE ");
        stb.append("         T1.YEAR     = '" + _param._ctrlYear + "' AND ");
        stb.append("         T1.SEMESTER = '" + _param._semester + "' AND ");
        stb.append("         T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '" + _param._testcd + "' AND ");
        stb.append("         T1.SCORE   <= " + _param._ketten + " ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     * ");
        stb.append(" FROM ");
        stb.append("     MAIN ");
        if (PRINT_WARNING.equals(_param._formKind)) {
            stb.append(" WHERE ");
            stb.append("     SCHREGNO IN (" + _param._inState + ") ");
        }
        stb.append(" ORDER BY ");
        stb.append("     ATTENDNO, ");
        stb.append("     SCHREGNO, ");
        stb.append("     CLASSCD, ");
        stb.append("     SCHOOL_KIND, ");
        stb.append("     CURRICULUM_CD, ");
        stb.append("     SUBCLASSCD ");
        return stb.toString();
    }

    private class StudentData {
        final String _schregno;
        final String _grade;
        final String _hrClass;
        final String _hrName;
        final String _attendno;
        final String _nameShow;
        final String _name;
        final String _shinkyuDiv;
        final String _guardName;
        final List _printDataList;
        int _totalCredit;
        public StudentData(
                final String schregno,
                final String grade,
                final String hrClass,
                final String hrName,
                final String attendno,
                final String nameShow,
                final String name,
                final String shinkyuDiv,
                final String guardName
        ) {
            _schregno = schregno;
            _grade = grade;
            _hrClass = hrClass;
            _hrName = hrName;
            _attendno = attendno;
            _nameShow = nameShow;
            _name = name;
            _shinkyuDiv = shinkyuDiv;
            _guardName = guardName;
            _printDataList = new ArrayList();
            _totalCredit = 0;
        }

        private void setPrintDataList(
                final String classcd,
                final String schoolKind,
                final String curriculumCd,
                final String subclasscd,
                final String subclassname,
                final String score,
                final String credits
        ) {
            final PrintData printData = new PrintData(classcd, schoolKind, curriculumCd, subclasscd, subclassname, score, credits);
            _printDataList.add(printData);
            _totalCredit += "".equals(StringUtils.defaultString(credits)) ? 0 : Integer.parseInt(credits);
        }
    }

    private class PrintData {
        final String _classcd;
        final String _schoolKind;
        final String _curriculumCd;
        final String _subclasscd;
        final String _subclassname;
        final String _score;
        final String _credits;
        public PrintData(
                final String classcd,
                final String schoolKind,
                final String curriculumCd,
                final String subclasscd,
                final String subclassname,
                final String score,
                final String credits
        ) {
            _classcd = classcd;
            _schoolKind = schoolKind;
            _curriculumCd = curriculumCd;
            _subclasscd = subclasscd;
            _subclassname = subclassname;
            _score = score;
            _credits = credits;
        }
    }

    private class CertifSchool {
        final String _schoolName;
        final String _jobName;
        final String _principalName;
        public CertifSchool(
                final String schoolName,
                final String jobName,
                final String principalName
        ) {
            _schoolName = schoolName;
            _jobName = jobName;
            _principalName = principalName;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 64290 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _semester;
        final String _gradeHrClass;
        final String _testcd;
        final String _ketten;
        final String[] _checked;
        final String _inState;
        final String _date;
        final String _ctrlYear;
        final String _ctrlSemester;
        final String _ctrlDate;
        final String _prgid;
        final String _formKind;
        final String _keepKetten;
        final String _usePrgSchoolkind;
        final String _useschoolKindfield;
        final String _schoolkind;
        final String _schoolcd;
        final String _semestername;
        final String _testitemname;
        final CertifSchool _certifSchool;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _semester = request.getParameter("SEMESTER");
            _gradeHrClass = request.getParameter("GRADE_HR_CLASS");
            _testcd = request.getParameter("TESTCD");
            _ketten = request.getParameter("KETTEN");
            _formKind = request.getParameter("FORM_KIND");
            _checked = request.getParameterValues("CHECKED[]");
            _inState = getInState();
            _date = request.getParameter("DATE");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _prgid = request.getParameter("PRGID");
            _keepKetten = request.getParameter("KEEP_KETTEN");
            _usePrgSchoolkind = request.getParameter("use_prg_schoolkind");
            _useschoolKindfield = request.getParameter("useSchool_KindField");
            _schoolkind = request.getParameter("SCHOOLKIND");
            _schoolcd = request.getParameter("SCHOOLCD");
            _semestername = getSemesterName(db2);
            _testitemname = getTestName(db2);
            _certifSchool = getCertifSchool(db2);
        }
        
        public boolean isGakkimatsu() {
        	final String testitemcd = null != _testcd && _testcd.length() >= 4 ? _testcd.substring(2, 4) : null; 
        	return "00".equals(testitemcd);
        }

        private CertifSchool getCertifSchool(final DB2UDB db2) {
            CertifSchool retCertifSchool = new CertifSchool("", "", "");
            PreparedStatement ps = null;
            ResultSet rs = null;
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     SCHOOL_NAME, ");
            stb.append("     JOB_NAME, ");
            stb.append("     PRINCIPAL_NAME ");
            stb.append(" FROM ");
            stb.append("     CERTIF_SCHOOL_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR='" + _ctrlYear + "' ");
            stb.append("     AND CERTIF_KINDCD='142' ");
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    final String schoolName = rs.getString("SCHOOL_NAME");
                    final String jobName = rs.getString("JOB_NAME");
                    final String principalName = rs.getString("PRINCIPAL_NAME");
                    retCertifSchool = new CertifSchool(schoolName, jobName, principalName);
                }
            } catch (SQLException ex) {
                log.debug("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retCertifSchool;
        }

        private String getInState() {
            String retStr = "";
            if (PRINT_LIST.equals(_formKind)) {
                return retStr;
            }
            String sep = "";
            for (int i = 0; i < _checked.length; i++) {
                retStr += sep + "'" + _checked[i] + "'";
                sep = ",";
            }
            return retStr;
        }

        private String getSemesterName(final DB2UDB db2) {
            String retSemesterName = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            final StringBuffer stb = new StringBuffer();
            stb.append("     SELECT ");
            stb.append("         SEMESTERNAME ");
            stb.append("     FROM ");
            stb.append("         SEMESTER_MST ");
            stb.append("     WHERE ");
            stb.append("         YEAR = '" + _ctrlYear + "' ");
            stb.append("         AND SEMESTER = '" + _semester + "' ");
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    retSemesterName = rs.getString("SEMESTERNAME");
                }
            } catch (SQLException ex) {
                log.debug("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retSemesterName;
        }

        private String getTestName(final DB2UDB db2) {
            String retTestName = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            final StringBuffer stb = new StringBuffer();
            stb.append("     SELECT ");
            stb.append("         TESTITEMNAME ");
            stb.append("     FROM ");
            stb.append("         TESTITEM_MST_COUNTFLG_NEW_SDIV ");
            stb.append("     WHERE ");
            stb.append("         YEAR     = '" + _ctrlYear + "' ");
            stb.append("         AND SEMESTER = '" + _semester + "' ");
            stb.append("         AND TESTKINDCD || TESTITEMCD || SCORE_DIV = '" + _testcd + "' ");

            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    retTestName = rs.getString("TESTITEMNAME");
                }
            } catch (SQLException ex) {
                log.debug("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retTestName;
        }

    }
}

// eof
