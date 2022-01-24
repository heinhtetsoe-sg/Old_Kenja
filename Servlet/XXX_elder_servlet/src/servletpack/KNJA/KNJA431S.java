/*
 * $Id: 5cc03ce6ad664a08b81a6ba29f47ecf9e1fce7c4 $
 *
 * 作成日: 2017/08/31
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJA;


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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

public class KNJA431S {

    private static final Log log = LogFactory.getLog(KNJA431S.class);

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

        String befGradeHr = "";
        final List studentList = getStudentList(db2);
        for (Iterator itStudent = studentList.iterator(); itStudent.hasNext();) {
            final Student student = (Student) itStudent.next();

            final String gradeHr = student._grade + student._hrClass;
            if (!befGradeHr.equals(gradeHr)) {
                svf.VrSetForm("KNJA431S.frm", 4);
                setTitle(svf);
            }
            svf.VrsOut("HR_NAME", student._hrName);
            svf.VrsOut("TR_NAME", student._staffName);

            for (Iterator itAtWk = student._attestOpinionsList.iterator(); itAtWk.hasNext();) {
                final Opinions opinions = (Opinions) itAtWk.next();
                svf.VrsOut("NO", student._attendno);
                svf.VrsOut("NAME", student._name);
                svf.VrsOut("YEAR", opinions._year);
                svf.VrsOut("CHAGE_OPI_SEQ", opinions._chageOpiSeq);
                svf.VrsOut("LAST_OPI_SEQ", opinions._lastOpiSeq);
                svf.VrEndRecord();
                _hasData = true;
            }
            befGradeHr = gradeHr;
        }
    }

    private void setTitle(final Vrw32alp svf) {
        svf.VrsOut("TITLE", KNJ_EditDate.h_format_JP_N(_param._ctrlYear + "-04-01") + "度" + "　署名状況一覧");
        svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._ctrlDate));
    }

    private List getStudentList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getStudentSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String schregno = rs.getString("SCHREGNO");
                final String grade = rs.getString("GRADE");
                final String hrClass = rs.getString("HR_CLASS");
                final String attendno = rs.getString("ATTENDNO");
                final String name = rs.getString("NAME");
                final String hrName = rs.getString("HR_NAME");
                final String hrAbbv = rs.getString("HR_NAMEABBV");
                final String staffName = rs.getString("STAFFNAME");
                final Student student = new Student(schregno, grade, hrClass, attendno, name, hrName, hrAbbv, staffName);
                student.setAttestOpinionsList(db2);
                retList.add(student);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String getStudentSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     REGD.SCHREGNO, ");
        stb.append("     REGD.GRADE, ");
        stb.append("     REGD.HR_CLASS, ");
        stb.append("     REGD.ATTENDNO, ");
        stb.append("     BASE.NAME, ");
        stb.append("     REGH.HR_NAME, ");
        stb.append("     REGH.HR_NAMEABBV, ");
        stb.append("     STAFF.STAFFNAME ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT REGD ");
        stb.append("     LEFT JOIN SCHREG_BASE_MST BASE ON REGD.SCHREGNO = BASE.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT REGH ON REGD.YEAR = REGH.YEAR ");
        stb.append("          AND REGD.SEMESTER = REGH.SEMESTER ");
        stb.append("          AND REGD.GRADE = REGH.GRADE ");
        stb.append("          AND REGD.HR_CLASS = REGH.HR_CLASS ");
        stb.append("     LEFT JOIN STAFF_MST STAFF ON REGH.TR_CD1 = STAFF.STAFFCD ");
        stb.append(" WHERE ");
        stb.append("     REGD.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("     AND REGD.SEMESTER = '" + _param._ctrlSemester + "' ");
        stb.append("     AND REGD.GRADE || REGD.HR_CLASS IN (" + _param._gradeClassInState + ") ");
        stb.append(" ORDER BY ");
        stb.append("     REGD.GRADE, ");
        stb.append("     REGD.HR_CLASS, ");
        stb.append("     REGD.ATTENDNO ");
        return stb.toString();
    }

    /** 生徒クラス */
    private class Student {
        final String _schregno;
        final String _grade;
        final String _hrClass;
        final String _attendno;
        final String _name;
        final String _hrName;
        final String _hrAbbv;
        final String _staffName;
        final List _attestOpinionsList = new ArrayList();
        public Student(
                final String schregno,
                final String grade,
                final String hrClass,
                final String attendno,
                final String name,
                final String hrName,
                final String hrAbbv,
                final String staffName
        ) {
            _schregno = schregno;
            _grade = grade;
            _hrClass = hrClass;
            _attendno = attendno;
            _name = name;
            _hrName = hrName;
            _hrAbbv = hrAbbv;
            _staffName = staffName;
        }

        public void setAttestOpinionsList(final DB2UDB db2) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = getOpinionsSql();
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    final String year = rs.getString("YEAR");
                    final String chageOpiSeq = null != rs.getString("CHAGE_OPI_SEQ") ? "レ" : "";
                    final String lastOpiSeq = null != rs.getString("LAST_OPI_SEQ") ? "レ" : "";

                    final Opinions opinions = new Opinions(year, chageOpiSeq, lastOpiSeq);
                    _attestOpinionsList.add(opinions);
                }

                db2.commit();
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        private String getOpinionsSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     REGD.YEAR, ");
            stb.append("     AT_WK.CHAGE_OPI_SEQ, ");
            stb.append("     AT_WK.LAST_OPI_SEQ ");
            stb.append(" FROM ");
            stb.append("     V_REGDYEAR_GRADE_DAT REGD ");
            stb.append("     LEFT JOIN ATTEST_OPINIONS_WK AT_WK ON REGD.YEAR = AT_WK.YEAR ");
            stb.append("          AND REGD.SCHREGNO = AT_WK.SCHREGNO ");
            stb.append(" WHERE ");
            stb.append("     REGD.SCHREGNO = '" + _schregno + "' ");
            stb.append(" ORDER BY ");
            stb.append("     REGD.YEAR ");

            return stb.toString();
        }
    }

    /** OPINIONSクラス */
    private class Opinions {
        final String _year;
        final String _chageOpiSeq;
        final String _lastOpiSeq;
        public Opinions(
                final String year,
                final String chageOpiSeq,
                final String lastOpiSeq
        ) {
            _year = year;
            _chageOpiSeq = chageOpiSeq;
            _lastOpiSeq = lastOpiSeq;
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
        final String[] _classSelected;
        final String _gradeClassInState;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _classSelected = request.getParameterValues("CLASS_SELECTED");
            String instate = "";
            String sep = "";
            for (int i = 0; i < _classSelected.length; i++) {
                instate += sep + "'" + _classSelected[i] + "'";
                sep = ",";
            }
            _gradeClassInState = instate;
        }

    }
}

// eof

