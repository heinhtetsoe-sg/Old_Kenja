/**     プロジェクト/プロパティ/javaコード・スタイル/コード・テンプレート/パターン       ***/

/*
 * $Id: baa22920e4f8d74ada5e827ae87aecc7343debfc $
 *
 * 作成日: 2018/09/12
 * 作成者: yamashiro
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

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

public class KNJL044Q {

    private static final Log log = LogFactory.getLog(KNJL044Q.class);

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

        svf.VrSetForm("KNJL044Q.frm", 4);
        for (Iterator iterator = printList.iterator(); iterator.hasNext();) {
            svf.VrsOut("TITLE", KNJ_EditDate.gengou(db2, Integer.parseInt(_param._entexamYear)) + "年度　" + _param._testDivName);
            final InterviewClass printData = (InterviewClass) iterator.next();
            if (null != printData._interviewGroup) {
                svf.VrsOut("INTERVIEW_GROUP", printData._interviewGroup + printData._gNo);
            }
            svf.VrsOut("EXAM_NO", printData._examNo);
            svf.VrsOut("TEST_ROOM", printData._testRoom);
            svf.VrsOut("INTERVIEW_ROOM", printData._interviewRoom);

            svf.VrEndRecord();
            _hasData = true;
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
                final String examNo         = rs.getString("EXAMNO");
                final String name           = rs.getString("NAME");
                final String interviewGroup = rs.getString("INTERVIEW_GROUP");
                final String testRoom       = rs.getString("TEST_ROOM");
                final String interviewRoom  = rs.getString("INTERVIEW_ROOM");
                final String gNo            = rs.getString("GNO");

                final InterviewClass interviewClass = new InterviewClass(examNo, name, interviewGroup, testRoom, interviewRoom, gNo);
                retList.add(interviewClass);
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
        stb.append(" SELECT ");
        stb.append("     T1.EXAMNO, ");
        stb.append("     T1.NAME, ");
        stb.append("     L050.NAME3 AS TEST_ROOM, ");
        stb.append("     L053.NAME3 AS INTERVIEW_ROOM, ");
        stb.append("     L054.NAME3 AS INTERVIEW_GROUP, ");
        stb.append("     RANK() OVER(PARTITION BY T2.INTERVIEW_GROUP ORDER BY T1.EXAMNO) AS GNO ");
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
        stb.append("     LEFT JOIN V_NAME_MST L053 ");
        stb.append("              ON T1.ENTEXAMYEAR    = L053.YEAR ");
        stb.append("             AND L053.NAMECD1      = 'L053' ");
        stb.append("             AND T2.INTERVIEW_ROOM = L053.NAMECD2 ");
        stb.append("     LEFT JOIN V_NAME_MST L054 ");
        stb.append("              ON T1.ENTEXAMYEAR       = L054.YEAR ");
        stb.append("             AND L054.NAMECD1         = 'L054' ");
        stb.append("             AND T2.INTERVIEW_GROUP = L054.NAMECD2 ");
        stb.append(" WHERE ");
        stb.append("         T1.ENTEXAMYEAR  = '"+ _param._entexamYear + "' ");
        stb.append("     AND T1.APPLICANTDIV = '"+ _param._applicantDiv + "' ");
        stb.append("     AND T1.TESTDIV      = '"+ _param._testDiv + "' ");
        stb.append("     AND VALUE(T1.JUDGEMENT, '0') <> '4' ");  //4:欠席は対象外
        stb.append(" ORDER BY ");
        if ("2".equals(_param._sortDiv)) {
            stb.append("     T2.INTERVIEW_GROUP, ");
        }
        stb.append("     T1.EXAMNO ");
        return stb.toString();
    }

    private class InterviewClass {
        final String _examNo;
        final String _name;
        final String _interviewGroup;
        final String _testRoom;
        final String _interviewRoom;
        final String _gNo;
        public InterviewClass(
                final String examNo,
                final String name,
                final String interviewGroup,
                final String testRoom,
                final String interviewRoom,
                final String gNo
        ) {
            _examNo              = examNo;
            _name                = name;
            _interviewGroup      = interviewGroup;
            _testRoom            = testRoom;
            _interviewRoom       = interviewRoom;
            _gNo                 = gNo;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 63131 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _entexamYear;
        private final String _applicantDiv;
        private final String _testDiv;
        private final String _sortDiv;
        private final String _loginYear;
        private final String _loginSemester;
        private final String _loginDate;
        private final String _schoolKind;
        private final String _testDivName;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamYear   = request.getParameter("ENTEXAMYEAR");
            _applicantDiv  = request.getParameter("APPLICANTDIV");
            _testDiv       = request.getParameter("TESTDIV");
            _sortDiv       = request.getParameter("SORT_DIV");
            _loginYear     = request.getParameter("LOGIN_YEAR");
            _loginSemester = request.getParameter("LOGIN_SEMESTER");
            _loginDate     = request.getParameter("LOGIN_DATE");
            _schoolKind    = request.getParameter("SCHOOLKIND");
            final String setNamecd1 = "P".equals(_schoolKind) ? "LP24" : "L024";
            _testDivName   = getNameMst(db2, setNamecd1, _testDiv);
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
