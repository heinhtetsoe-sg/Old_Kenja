/*
 * $Id: 3ac95178045d00a35eb5ce972cea44d8e5999833 $
 *
 * 作成日: 2017/11/24
 * 作成者: tawada
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

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
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJL043Q {

    private static final Log log = LogFactory.getLog(KNJL043Q.class);

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
        if ("P".equals(_param._schoolKind)) {
            printOutP(db2, svf, printList);
        } else {
            printOutJH(svf, printList);
        }
    }

    private void printOutP(final DB2UDB db2, final Vrw32alp svf, final List printList) {
        svf.VrSetForm("KNJL043Q_2.frm", 1);
        for (Iterator iterator = printList.iterator(); iterator.hasNext();) {
            svf.VrsOut("TITLE", _param._testDivName + "のご案内");
            svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(db2, _param._loginDate));
            final InterviewClass printData = (InterviewClass) iterator.next();
            svf.VrsOut("EXAM_NO", printData._examNo);
            final String sizeNo = KNJ_EditEdit.getMS932ByteLength(printData._name) > 30 ? "3" : KNJ_EditEdit.getMS932ByteLength(printData._name) > 20 ? "2" : "1";
            svf.VrsOut("NAME" + sizeNo, printData._name);
            svf.VrsOut("EXAM_ROOM", printData._testRoom);
            svf.VrsOut("INTERVIEW_ROOM", printData._interviewRoom);
            svf.VrsOut("TIME", printData._interviewSetTime);

            svf.VrEndPage();
            _hasData = true;
        }
    }

    private void printOutJH(final Vrw32alp svf, final List printList) {
        svf.VrSetForm("KNJL043Q.frm", 1);
        final int maxLine = 4;
        int lineCnt = 1;
        for (Iterator iterator = printList.iterator(); iterator.hasNext();) {
            if (lineCnt > maxLine) {
                svf.VrEndPage();
                lineCnt = 1;
            }
            final InterviewClass printData = (InterviewClass) iterator.next();
            svf.VrsOutn("EXAM_NO", lineCnt, printData._examNo);
            final String sizeNo = KNJ_EditEdit.getMS932ByteLength(printData._name) > 30 ? "3" : KNJ_EditEdit.getMS932ByteLength(printData._name) > 20 ? "2" : "1";
            svf.VrsOutn("NAME" + sizeNo, lineCnt, printData._name);
            svf.VrsOutn("EXAM_ROOM", lineCnt, printData._testRoom);
            svf.VrsOutn("TIME", lineCnt, printData._interviewSetTime);
            //svf.VrsOutn("INTERVIEW_ANTER_ROOM", lineCnt, printData._interviewWatingRoom);
            svf.VrsOutn("INTERVIEW_ROOM", lineCnt, printData._interviewRoom);
            svf.VrsOutn("INTERVIEW_END_TIME", lineCnt, printData._interviewEndTime);
            svf.VrsOutn("SCHOOL_NAME", lineCnt, _param._schoolName);

            lineCnt++;
            _hasData = true;
        }
        if (_hasData) {
            svf.VrEndPage();
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

            while (rs.next()) {
                final String examNo = rs.getString("EXAMNO");
                final String name = rs.getString("NAME");
                final String testRoom = rs.getString("TEST_ROOM");
                final String interviewSetTime = rs.getString("INTERVIEW_SETTIME");
                final String interviewRoom = rs.getString("INTERVIEW_ROOM");
                final String interviewEndTime = rs.getString("INTERVIEW_ENDTIME");

                final InterviewClass interviewClass = new InterviewClass(examNo, name, testRoom, interviewSetTime, interviewRoom, interviewEndTime);
                retList.add(interviewClass);
            }

        } catch (SQLException ex) {
            log.error("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String getSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.EXAMNO, ");
        stb.append("     T1.NAME, ");
        if ("P".equals(_param._schoolKind)) {
            stb.append("     L050.NAME3 AS TEST_ROOM, ");
            stb.append("     L051.NAME3 AS INTERVIEW_SETTIME, ");
            stb.append("     CAST(NULL AS VARCHAR(1)) AS INTERVIEW_ENDTIME, ");
            stb.append("     L053.NAME3 AS INTERVIEW_ROOM ");
        } else {
            stb.append("     L050.NAME1 AS TEST_ROOM, ");
            stb.append("     L051A.NAME1 AS INTERVIEW_SETTIME, ");
            stb.append("     L051B.NAMESPARE2 AS INTERVIEW_ENDTIME, ");
            stb.append("     L052_I.NAME1 AS INTERVIEW_ROOM ");
        }
        stb.append(" FROM ");
        stb.append("     V_ENTEXAM_APPLICANTBASE_DAT T1 ");
        stb.append("     LEFT JOIN ENTEXAM_INTERVIEW_HALL_DAT T2 ");
        stb.append("              ON T1.ENTEXAMYEAR  = T2.ENTEXAMYEAR ");
        stb.append("             AND T1.APPLICANTDIV = T2.APPLICANTDIV ");
        stb.append("             AND T1.TESTDIV      = T2.TESTDIV ");
        stb.append("             AND T1.EXAMNO       = T2.EXAMNO ");
        stb.append("     LEFT JOIN V_NAME_MST L050 ");
        stb.append("              ON T1.ENTEXAMYEAR  = L050.YEAR ");
        stb.append("             AND L050.NAMECD1    = 'L050' ");
        stb.append("             AND T2.TEST_ROOM    = L050.NAMECD2 ");
        if ("P".equals(_param._schoolKind)) {
            stb.append("     LEFT JOIN V_NAME_MST L051 ");
            stb.append("              ON T1.ENTEXAMYEAR       = L051.YEAR ");
            stb.append("             AND L051.NAMECD1         = 'L051' ");
            stb.append("             AND T2.INTERVIEW_SETTIME = L051.NAMECD2 ");
            stb.append("     LEFT JOIN V_NAME_MST L053 ");
            stb.append("              ON T1.ENTEXAMYEAR    = L053.YEAR ");
            stb.append("             AND L053.NAMECD1      = 'L053' ");
            stb.append("             AND T2.INTERVIEW_ROOM = L053.NAMECD2 ");
        } else {
            stb.append("     LEFT JOIN V_NAME_MST L051A ");
            stb.append("              ON T1.ENTEXAMYEAR       = L051A.YEAR ");
            stb.append("             AND L051A.NAMECD1         = 'L051' ");
            stb.append("             AND T2.INTERVIEW_SETTIME = L051A.NAMECD2 ");
            stb.append("     LEFT JOIN V_NAME_MST L051B ");
            stb.append("              ON T1.ENTEXAMYEAR       = L051B.YEAR ");
            stb.append("             AND L051B.NAMECD1         = 'L051' ");
            stb.append("             AND T2.INTERVIEW_ENDTIME = L051B.NAMECD2 ");
            stb.append("     LEFT JOIN V_NAME_MST L052_I ");
            stb.append("              ON T1.ENTEXAMYEAR    = L052_I.YEAR ");
            stb.append("             AND L052_I.NAMECD1      = 'L052' ");
            stb.append("             AND T2.INTERVIEW_ROOM = L052_I.NAMECD2 ");
        }
        stb.append(" WHERE ");
        stb.append("         T1.ENTEXAMYEAR  = '"+ _param._entexamYear + "' ");
        stb.append("     AND T1.APPLICANTDIV = '"+ _param._applicantDiv + "' ");
        stb.append("     AND T1.TESTDIV      = '"+ _param._testDiv + "' ");
        stb.append("     AND VALUE(T1.JUDGEMENT, '0') <> '4' ");  //4:欠席は対象外
        stb.append(" ORDER BY ");
        stb.append("     T1.EXAMNO ");
        return stb.toString();
    }

    private class InterviewClass {
        final String _examNo;
        final String _name;
        final String _testRoom;
        final String _interviewSetTime;
        final String _interviewRoom;
        final String _interviewEndTime;
        public InterviewClass(
                final String examNo,
                final String name,
                final String testRoom,
                final String interviewSetTime,
                final String interviewRoom,
                final String interviewEndTime
        ) {
            _examNo              = examNo;
            _name                = name;
            _testRoom            = testRoom;
            _interviewSetTime    = interviewSetTime;
            _interviewRoom       = interviewRoom;
            _interviewEndTime    = interviewEndTime;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 71436 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _entexamYear;
        private final String _applicantDiv;
        private final String _testDiv;
        private final String _loginYear;
        private final String _loginSemester;
        private final String _loginDate;
        private final String _schoolKind;
        private final String _schoolName;
        private final String _testDivName;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamYear   = request.getParameter("ENTEXAMYEAR");
            _applicantDiv  = request.getParameter("APPLICANTDIV");
            _testDiv       = request.getParameter("TESTDIV");
            _loginYear     = request.getParameter("LOGIN_YEAR");
            _loginSemester = request.getParameter("LOGIN_SEMESTER");
            _loginDate     = request.getParameter("LOGIN_DATE");
            _schoolKind    = request.getParameter("SCHOOLKIND");
            _schoolName    = getSchoolName(db2, _loginYear);
            final String setNamecd1 = "P".equals(_schoolKind) ? "LP24" : "L024";
            _testDivName   = getNameMst(db2, setNamecd1, _testDiv);
        }

        private String getSchoolName(final DB2UDB db2, final String year) {
            String retSchoolName = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement("SELECT SCHOOLNAME1 FROM SCHOOL_MST WHERE YEAR = '" + year + "' AND SCHOOL_KIND = '"+ _schoolKind +"' ");
                rs = ps.executeQuery();
                if (rs.next()) {
                    retSchoolName = rs.getString("SCHOOLNAME1");
                }
            } catch (SQLException ex) {
                log.debug("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retSchoolName;
        }

        private String getNameMst(final DB2UDB db2, final String nameCd1, final String nameCd2) {
            String retName = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement("SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = '"+ nameCd1 +"' AND NAMECD2 = '"+ nameCd2 +"' ");
                rs = ps.executeQuery();
                if (rs.next()) {
                    retName = rs.getString("NAME1");
                }
            } catch (SQLException ex) {
                log.debug("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retName;
        }

    }
}

// eof
