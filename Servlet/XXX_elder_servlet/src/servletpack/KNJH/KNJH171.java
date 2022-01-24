// kanji=漢字
/*
 * $Id: c56fbfab19ca9cea5e52353c215db0f53c5f698e $
 *
 * 作成日: 2010/04/12 14:04:38 - JST
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2010 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJH;

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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;

public class KNJH171 {

    private static final Log log = LogFactory.getLog("KNJH171.class");

    private boolean _hasData;

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
        final List groupList = getGroupList(db2);
        svf.VrSetForm("KNJH171.frm", 4);
        svf.VrsOut("MARK1", "●");
        svf.VrsOut("MARK2", "○");

        for (final Iterator itg = groupList.iterator(); itg.hasNext();) {
            final Group group = (Group) itg.next();
            svf.VrsOut("GROUP_NAME", group._groupName);
            svf.VrsOut("MEETING_PLACE", group._meetingPlace);

            int cnt = 0;
            final List studentList = getStudentList(db2, group._groupNo);
            for (final Iterator its = studentList.iterator(); its.hasNext();) {
                final Student student = (Student) its.next();
                if (student._isGrd) {
                    continue;
                }
                cnt++;
                String no = (cnt % 3 == 0) ? "3" : String.valueOf(cnt % 3);

                svf.VrsOut("RESPONSIBILITY" + no, student.getMark());
                svf.VrsOut("ATTENDNO" + no, student._attendno);
                String len = (null != student._name && 10 < student._name.length()) ? "_2" : "_1";
                svf.VrsOut("NAME" + no + len, student._name);

                if (cnt % 3 == 0) svf.VrEndRecord();
                _hasData = true;
            }
            if (0 < cnt) svf.VrEndRecord();
        }
    }

    private List getGroupList(final DB2UDB db2) throws SQLException {
        final List rtnList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        final String sql = getGroupSql();
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String groupNo = rs.getString("GO_HOME_GROUP_NO");
                final String groupName = rs.getString("GO_HOME_GROUP_NAME");
                final String meetingPlace = rs.getString("MEETING_PLACE");
                final Group group = new Group(
                        groupNo,
                        groupName,
                        meetingPlace);
                rtnList.add(group);
            }
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return rtnList;
    }

    private String getGroupSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT DISTINCT ");
        stb.append("     T1.GO_HOME_GROUP_NO, ");
        stb.append("     T2.GO_HOME_GROUP_NAME, ");
        stb.append("     '［' || T2.MEETING_PLACE || '］' AS MEETING_PLACE ");
        stb.append(" FROM ");
        stb.append("     SCHREG_ENVIR_DAT T1 ");
        stb.append("     LEFT JOIN GO_HOME_GROUP_MST T2 ON T1.GO_HOME_GROUP_NO = T2.GO_HOME_GROUP_NO ");
        stb.append(" WHERE ");
        stb.append("     T1.GO_HOME_GROUP_NO IS NOT NULL AND ");
        stb.append("     T1.GO_HOME_GROUP_NO <> '00' AND ");
        stb.append("     T1.SCHREGNO IN (SELECT ");
        stb.append("                         SCHREGNO ");
        stb.append("                     FROM ");
        stb.append("                         SCHREG_REGD_DAT ");
        stb.append("                     WHERE ");
        stb.append("                         YEAR = '" + _param._ctrlYear + "' AND ");
        stb.append("                         SEMESTER = '" + _param._ctrlSemester + "' ");
        stb.append("                     ) ");
        stb.append(" ORDER BY ");
        stb.append("     T1.GO_HOME_GROUP_NO ");
        return stb.toString();
    }

    private class Group {
        final String _groupNo;
        final String _groupName;
        final String _meetingPlace;

        Group(final String groupNo,
                final String groupName,
                final String meetingPlace
        ) {
            _groupNo = groupNo;
            _groupName = groupName;
            _meetingPlace = meetingPlace;
        }
    }

    private List getStudentList(final DB2UDB db2, final String groupno) throws SQLException {
        final List rtnList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        final String sql = getStudentSql(groupno);
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String schregno = rs.getString("SCHREGNO");
                final String attendno = rs.getString("ATTENDNO");
                final String name = rs.getString("NAME");
                final String responsibility = rs.getString("RESPONSIBILITY");
                final boolean isGrd = "1".equals(rs.getString("IS_GRD"));
                final Student student = new Student(
                        schregno,
                        attendno,
                        name,
                        responsibility,
                        isGrd);
                rtnList.add(student);
            }
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return rtnList;
    }

    private String getStudentSql(final String groupno) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH SCH_INFO AS( ");
        stb.append("     SELECT ");
        stb.append("         T1.SCHREGNO, ");
        stb.append("         T1.GRADE, ");
        stb.append("         T1.HR_CLASS, ");
        stb.append("         T3.HR_NAMEABBV, ");
        stb.append("         substr(T1.ATTENDNO, 2) AS ATTENDNO, ");
        stb.append("         T2.NAME, ");
        stb.append("         CASE WHEN T2.GRD_DIV IS NOT NULL AND T2.GRD_DIV <> '4' AND VALUE(T2.GRD_DATE, '9999-12-31') < '" + _param._ctrlDate + "' THEN '1' END AS IS_GRD");
        stb.append("     FROM ");
        stb.append("         SCHREG_REGD_DAT T1 ");
        stb.append("         LEFT JOIN SCHREG_BASE_MST T2 ON T1.SCHREGNO = T2.SCHREGNO ");
        stb.append("         LEFT JOIN SCHREG_REGD_HDAT T3 ON T1.YEAR = T3.YEAR AND T1.SEMESTER = T3.SEMESTER AND ");
        stb.append("                                          T1.GRADE = T3.GRADE AND T1.HR_CLASS = T3.HR_CLASS ");
        stb.append("     WHERE ");
        stb.append("         T1.YEAR = '" + _param._ctrlYear + "' AND ");
        stb.append("         T1.SEMESTER = '" + _param._ctrlSemester + "' ");
        stb.append(" ) ");

        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.RESPONSIBILITY, ");
        stb.append("     T2.HR_NAMEABBV || T2.ATTENDNO AS ATTENDNO, ");
        stb.append("     T2.NAME, ");
        stb.append("     T2.IS_GRD ");
        stb.append(" FROM ");
        stb.append("     SCHREG_ENVIR_DAT T1,  ");
        stb.append("     SCH_INFO T2 ");
        stb.append(" WHERE ");
        stb.append("     T1.SCHREGNO = T2.SCHREGNO AND ");
        stb.append("     T1.GO_HOME_GROUP_NO = '" + groupno + "' ");
        stb.append(" ORDER BY ");
        stb.append("     T1.GO_HOME_GROUP_NO, ");
        stb.append("     T2.GRADE DESC, ");
        stb.append("     T2.HR_CLASS, ");
        stb.append("     T2.ATTENDNO ");
        return stb.toString();
    }

    private class Student {
        final String _schregno;
        final String _attendno;
        final String _name;
        final String _responsibility;
        final boolean _isGrd;

        Student(final String schregno,
                final String attendno,
                final String name,
                final String responsibility,
                final boolean isGrd
        ) {
            _schregno = schregno;
            _attendno = attendno;
            _name = name;
            _responsibility = responsibility;
            _isGrd = isGrd;
        }

        private String getMark() {
            if ("1".equals(_responsibility)) {
                return "●";
            } else if("2".equals(_responsibility)){
                return "○";
            }
            return "";
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

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
        }

    }
}

// eof
