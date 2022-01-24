/*
 * $Id: c519fdca6c8b293771d3cd51a833e466ca32844f $
 *
 * 作成日: 2017/09/01
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJC;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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

public class KNJC190A {

    private static final Log log = LogFactory.getLog(KNJC190A.class);

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

            if ("1".equals(_param._disp)) {
                printMainHr(db2, svf);
            } else {
                printMainChair(db2, svf);
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

    private void setTitle(final Vrw32alp svf) {
        svf.VrsOut("TITLE", KNJ_EditDate.h_format_JP_N(_param._ctrlYear + "-04-01") + "度" + "　出欠未入力一覧表");
        svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._ctrlDate));
        int semeCnt = 1;
        for (Iterator itSeme = _param._semesterMonthList.iterator(); itSeme.hasNext();) {
            final SemesterMonthMst semesterMonthMst = (SemesterMonthMst) itSeme.next();
            final int intMonth = Integer.parseInt(semesterMonthMst._month);
            svf.VrsOutn("MONTH1", semeCnt, String.valueOf(intMonth) + "月");
            svf.VrsOutn("SEMESTER1", semeCnt, semesterMonthMst._semesterName);
            semeCnt++;
        }
    }

    private void printMainHr(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrSetForm("KNJC190A_1.frm", 4);
        setTitle(svf);
        final List regdList = getRegdList(db2);
        for (int line = 0; line < regdList.size(); line++) {
            final RegdHdat regdHdat = (RegdHdat) regdList.get(line);
            if ("1".equals(_param._output) && _param._semesterMonthList.size() == regdHdat._zumiCnt) {
                continue;
            }
            svf.VrsOut("HR_NAME", regdHdat._hrName);
            svf.VrsOut("TEACHER_NAME", regdHdat._staff);
            int semeCnt = 1;
            for (Iterator itSeme = _param._semesterMonthList.iterator(); itSeme.hasNext();) {
                final SemesterMonthMst semesterMonthMst = (SemesterMonthMst) itSeme.next();
                final String getAttend = (String) regdHdat._attendMap.get(semesterMonthMst._semester + semesterMonthMst._month);
                final String setAttendd = null == getAttend ? "未" : getAttend;
                svf.VrsOut("ATTEND" + semeCnt, setAttendd);
                semeCnt++;
            }
            svf.VrEndRecord();
            _hasData = true;
        }

    }

    private List getRegdList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getRegdSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String grade = rs.getString("GRADE");
                final String hrClass = rs.getString("HR_CLASS");
                final String hrName = rs.getString("HR_NAME");
                final String staffName = rs.getString("STAFFNAME");
                final RegdHdat regdHdat = new RegdHdat(grade, hrClass, hrName, staffName);
                regdHdat.setAttendMap(db2);
                retList.add(regdHdat);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String getRegdSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     REGDH.GRADE, ");
        stb.append("     REGDH.HR_CLASS, ");
        stb.append("     REGDH.HR_NAME, ");
        stb.append("     STAFF.STAFFNAME ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_HDAT REGDH ");
        stb.append("     LEFT JOIN STAFF_MST STAFF ON REGDH.TR_CD1 = STAFF.STAFFCD ");
        stb.append(" WHERE ");
        stb.append("     REGDH.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("     AND REGDH.SEMESTER = '" + _param._ctrlSemester + "' ");
        stb.append("     AND REGDH.GRADE || REGDH.HR_CLASS IN (" + _param._sqlInState + ") ");
        stb.append(" ORDER BY ");
        stb.append("     REGDH.GRADE, ");
        stb.append("     REGDH.HR_CLASS ");
        return stb.toString();
    }

    private void printMainChair(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrSetForm("KNJC190A_2.frm", 4);
        setTitle(svf);
        final List subclassList = getSubclassList(db2);
        for (int line = 0; line < subclassList.size(); line++) {
            final Subclass subclass = (Subclass) subclassList.get(line);
            for (Iterator itChair = subclass._chairList.iterator(); itChair.hasNext();) {
                final Chair chair = (Chair) itChair.next();
                if ("1".equals(_param._output) && _param._semesterMonthList.size() == chair._zumiCnt) {
                    continue;
                }
                int semeCnt = 1;
                for (Iterator itSeme = _param._semesterMonthList.iterator(); itSeme.hasNext();) {
                    final SemesterMonthMst semesterMonthMst = (SemesterMonthMst) itSeme.next();
                    final String getAttend = (String) chair._attendMap.get(semesterMonthMst._semester + semesterMonthMst._month);
                    final String setAttendd = null == getAttend ? "未" : getAttend;
                    svf.VrsOut("ATTEND" + semeCnt, setAttendd);
                    semeCnt++;
                }
                for (Iterator itStaff = chair._staffList.iterator(); itStaff.hasNext();) {
                    String staff = (String) itStaff.next();
                    svf.VrsOut("SUBCLASS_NAME", subclass._subclassAbbv);
                    svf.VrsOut("CHAIR_NAME", chair._chairName);
                    svf.VrsOut("TEACHER_NAME", staff);
                    for (int grpSeq = 1; grpSeq < 15; grpSeq++) {
                        svf.VrsOut("GRP" + grpSeq, String.valueOf(chair._chairCd));
                    }
                    svf.VrEndRecord();
                }
                if (chair._staffList.size() == 0) {
                    svf.VrsOut("SUBCLASS_NAME", subclass._subclassAbbv);
                    svf.VrsOut("CHAIR_NAME", chair._chairName);
                    svf.VrEndRecord();
                }
            }
            _hasData = true;
        }
    }

    private List getSubclassList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getSubclassSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String classCd = rs.getString("CLASSCD");
                final String schoolKind = rs.getString("SCHOOL_KIND");
                final String curriculumCd = rs.getString("CURRICULUM_CD");
                final String subclassCd = rs.getString("SUBCLASSCD");
                final String subclassName = rs.getString("SUBCLASSNAME");
                final String subclassAbbv = rs.getString("SUBCLASSNAME");
                final Subclass subclass = new Subclass(classCd, schoolKind, curriculumCd, subclassCd, subclassName, subclassAbbv);
                subclass.setChairList(db2);
                retList.add(subclass);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String getSubclassSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     SUBCLASS.CLASSCD, ");
        stb.append("     SUBCLASS.SCHOOL_KIND, ");
        stb.append("     SUBCLASS.CURRICULUM_CD, ");
        stb.append("     SUBCLASS.SUBCLASSCD, ");
        stb.append("     SUBCLASS.SUBCLASSNAME, ");
        stb.append("     SUBCLASS.SUBCLASSABBV ");
        stb.append(" FROM ");
        stb.append("     SUBCLASS_MST SUBCLASS ");
        stb.append(" WHERE ");
        stb.append("     SUBCLASS.CLASSCD || '-' || SUBCLASS.SCHOOL_KIND  || '-' || SUBCLASS.CURRICULUM_CD || '-' || SUBCLASS.SUBCLASSCD IN (" + _param._sqlInState + ") ");
        stb.append(" ORDER BY ");
        stb.append("     SUBCLASS.CLASSCD, ");
        stb.append("     SUBCLASS.SCHOOL_KIND, ");
        stb.append("     SUBCLASS.CURRICULUM_CD, ");
        stb.append("     SUBCLASS.SUBCLASSCD ");
        return stb.toString();
    }

    private class RegdHdat {
        final String _grade;
        final String _hrClass;
        final String _hrName;
        final String _staff;
        Map _attendMap = new HashMap();
        int _zumiCnt = 0;
        public RegdHdat(
                final String grade,
                final String hrClass,
                final String hrName,
                final String staff
        ) throws SQLException {
            _grade = grade;
            _hrClass = hrClass;
            _hrName = hrName;
            _staff = staff;
        }

        private void setAttendMap(final DB2UDB db2) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = getAttendSql();
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    final String semester = rs.getString("SEMESTER");
                    final String month = rs.getString("MONTH");
                    final String executed = rs.getString("EXECUTED");
                    _attendMap.put(semester + month, executed);
                    if (!"未".equals(executed)) {
                        _zumiCnt++;
                    }
                }

                db2.commit();
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        private String getAttendSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     CHKFIN.MONTH, ");
            stb.append("     CHKFIN.SEMESTER, ");
            stb.append("     CASE WHEN VALUE(CHKFIN.EXECUTED, '0') = '1' ");
            stb.append("          THEN '' ");
            stb.append("          ELSE '未' ");
            stb.append("     END AS EXECUTED ");
            stb.append(" FROM ");
            stb.append("     ATTEND_CHKFIN_DAT CHKFIN ");
            stb.append(" WHERE ");
            stb.append("     CHKFIN.YEAR = '" + _param._ctrlYear + "' ");
            stb.append("     AND CHKFIN.ATTEND_DIV = '1' ");
            stb.append("     AND CHKFIN.GRADE = '" + _grade + "' ");
            stb.append("     AND CHKFIN.HR_CLASS = '" + _hrClass + "' ");
            stb.append(" ORDER BY ");
            stb.append("     CHKFIN.MONTH, ");
            stb.append("     CHKFIN.SEMESTER ");
            return stb.toString();
        }
    }

    private class Subclass {
        final String _classCd;
        final String _schoolKind;
        final String _curriculumCd;
        final String _subclassCd;
        final String _subclassName;
        final String _subclassAbbv;
        List _chairList = new ArrayList();
        public Subclass(
                final String classCd,
                final String schoolKind,
                final String curriculumCd,
                final String subclassCd,
                final String subclassName,
                final String subclassAbbv
                ) throws SQLException {
            _classCd = classCd;
            _schoolKind = schoolKind;
            _curriculumCd = curriculumCd;
            _subclassCd = subclassCd;
            _subclassName = subclassName;
            _subclassAbbv = subclassAbbv;
        }

        private void setChairList(final DB2UDB db2) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = getChairSql();
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    final String chairCd = rs.getString("CHAIRCD");
                    final String chairName = rs.getString("CHAIRNAME");
                    final Chair chair = new Chair(chairCd, chairName);
                    chair.setStaffList(db2);
                    chair.setAttendMap(db2);
                    _chairList.add(chair);
                }

                db2.commit();
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        private String getChairSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     CHAIR.CHAIRCD, ");
            stb.append("     CHAIR.CHAIRNAME ");
            stb.append(" FROM ");
            stb.append("     CHAIR_DAT CHAIR ");
            stb.append(" WHERE ");
            stb.append("     CHAIR.YEAR = '" + _param._ctrlYear + "' ");
            stb.append("     AND CHAIR.SEMESTER = '" + _param._ctrlSemester + "' ");
            stb.append("     AND CHAIR.CLASSCD = '" + _classCd + "' ");
            stb.append("     AND CHAIR.SCHOOL_KIND = '" + _schoolKind + "' ");
            stb.append("     AND CHAIR.CURRICULUM_CD = '" + _curriculumCd + "' ");
            stb.append("     AND CHAIR.SUBCLASSCD = '" + _subclassCd + "' ");
            stb.append(" ORDER BY ");
            stb.append("     CHAIRCD ");
            return stb.toString();
        }

    }

    private class Chair {
        final String _chairCd;
        final String _chairName;
        List _staffList = new ArrayList();
        Map _attendMap = new HashMap();
        int _zumiCnt = 0;
        public Chair(
                final String chairCd,
                final String chairName
                ) throws SQLException {
            _chairCd = chairCd;
            _chairName = chairName;
        }

        private void setAttendMap(final DB2UDB db2) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = getAttendSql();
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    final String semester = rs.getString("SEMESTER");
                    final String month = rs.getString("MONTH");
                    final String executed = rs.getString("EXECUTED");
                    _attendMap.put(semester + month, executed);
                    if (!"未".equals(executed)) {
                        _zumiCnt++;
                    }
                }

                db2.commit();
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        private String getAttendSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     CHKFIN.MONTH, ");
            stb.append("     CHKFIN.SEMESTER, ");
            stb.append("     CASE WHEN VALUE(CHKFIN.EXECUTED, '0') = '1' ");
            stb.append("          THEN '' ");
            stb.append("          ELSE '未' ");
            stb.append("     END AS EXECUTED ");
            stb.append(" FROM ");
            stb.append("     ATTEND_CHKFIN_DAT CHKFIN ");
            stb.append(" WHERE ");
            stb.append("     CHKFIN.YEAR = '" + _param._ctrlYear + "' ");
            stb.append("     AND CHKFIN.ATTEND_DIV = '2' ");
            stb.append("     AND CHKFIN.CHAIRCD = '" + _chairCd + "' ");
            stb.append(" ORDER BY ");
            stb.append("     CHKFIN.MONTH, ");
            stb.append("     CHKFIN.SEMESTER ");
            return stb.toString();
        }

        private void setStaffList(final DB2UDB db2) throws SQLException {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH STAFF_T AS ( ");
            stb.append(" SELECT ");
            stb.append("     T1.STAFFCD ");
            stb.append(" FROM ");
            stb.append("     CHAIR_STF_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _param._ctrlYear + "' ");
            stb.append("     AND T1.SEMESTER = '" + _param._ctrlSemester + "' ");
            stb.append("     AND T1.CHAIRCD = '" + _chairCd + "' ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     T1.STAFFNAME ");
            stb.append(" FROM ");
            stb.append("     STAFF_MST T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.STAFFCD IN (SELECT STAFFCD FROM STAFF_T) ");
            stb.append(" ORDER BY ");
            stb.append("     T1.STAFFCD ");

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    _staffList.add(rs.getString("STAFFNAME"));
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }
    }

    private class SemesterMst {
        final String _semester;
        final String _naem;
        final String _sDate;
        final String _eDate;
        final String _sMonth;
        final String _eMonth;
        public SemesterMst(
                final String semester,
                final String naem,
                final String sDate,
                final String eDate
        ) throws SQLException {
            _semester = semester;
            _naem = naem;
            _sDate = sDate;
            _eDate = eDate;
            final String[] sDateArray = StringUtils.split(_sDate, "-");
            _sMonth = sDateArray[1];
            final String[] eDateArray = StringUtils.split(_eDate, "-");
            _eMonth = eDateArray[1];
        }

    }

    private class SemesterMonthMst {
        final String _semester;
        final String _semesterName;
        final String _month;
        public SemesterMonthMst(
                final String semester,
                final String semesterName,
                final String month
        ) {
            _semester = semester;
            _semesterName = semesterName;
            _month = month;
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
        final String[] _categorySelected;
        final String _ctrlYear;
        final String _ctrlSemester;
        final String _ctrlDate;
        final String _disp;
        final String _output;
        final List _semesterMstList;
        final List _semesterMonthList;
        final String _sqlInState;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _disp = request.getParameter("DISP");
            _output = request.getParameter("OUTPUT");
            String instate = "";
            String sep = "";
            for (int i = 0; i < _categorySelected.length; i++) {
                instate += sep + "'" + _categorySelected[i] + "'";
                sep = ",";
            }
            _sqlInState = instate;
            _semesterMstList = getSemeMstList(db2);
            _semesterMonthList = getSemeMonthList();
        }

        private List getSemeMstList(final DB2UDB db2) throws SQLException {
            final List retList = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = getSemeMonthSql();
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    final String semester = rs.getString("SEMESTER");
                    final String name = rs.getString("SEMESTERNAME");
                    final String sDate = rs.getString("SDATE");
                    final String eDate = rs.getString("EDATE");

                    final SemesterMst semesterMst = new SemesterMst(semester, name, sDate, eDate);
                    retList.add(semesterMst);
                }

                db2.commit();
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retList;
        }
        private String getSemeMonthSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     SEMESTER, ");
            stb.append("     SEMESTERNAME, ");
            stb.append("     SDATE, ");
            stb.append("     EDATE ");
            stb.append(" FROM ");
            stb.append("     SEMESTER_MST ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _ctrlYear + "' ");
            stb.append("     AND SEMESTER < '9' ");
            stb.append(" ORDER BY ");
            stb.append("     SEMESTER ");
            return stb.toString();
        }

        private List getSemeMonthList() {
            final List retList = new ArrayList();
            for (Iterator itSeme = _semesterMstList.iterator(); itSeme.hasNext();) {
                final SemesterMst semesterMst = (SemesterMst) itSeme.next();
                final int sMonth = Integer.parseInt(semesterMst._sMonth) >= 4 ? Integer.parseInt(semesterMst._sMonth) : Integer.parseInt(semesterMst._sMonth) + 12;
                final int eMonth = Integer.parseInt(semesterMst._eMonth) >= 4 ? Integer.parseInt(semesterMst._eMonth) : Integer.parseInt(semesterMst._eMonth) + 12;
                for (int month = sMonth; month <= eMonth; month++) {
                    if (month < 10) {
                        final SemesterMonthMst semesterMonthMst = new SemesterMonthMst(semesterMst._semester, semesterMst._naem, "0" + String.valueOf(month));
                        retList.add(semesterMonthMst);
                    } else if (month < 13) {
                        final SemesterMonthMst semesterMonthMst = new SemesterMonthMst(semesterMst._semester, semesterMst._naem, String.valueOf(month));
                        retList.add(semesterMonthMst);
                    } else {
                        final SemesterMonthMst semesterMonthMst = new SemesterMonthMst(semesterMst._semester, semesterMst._naem, "0" + String.valueOf(month - 12));
                        retList.add(semesterMonthMst);
                    }
                }
            }
            return retList;
        }
    }
}

// eof

