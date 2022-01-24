/*
 * $Id: 820eb279540e5f360030b88c6e627c9efb44c0cd $
 *
 * 作成日: 2017/10/30
 * 作成者: tawada
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJL325A {

    private static final Log log = LogFactory.getLog(KNJL325A.class);

    private boolean _hasData;

    private Param _param;

	private String bithdayField;

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
        svf.VrSetForm("KNJL325A.frm", 1);
        final List printList = getList(db2);
        final int maxCnt = 27;
        final int maxoneline = 9;
        int printCnt = 1;
        int printLine = 1;
        int printCol = 1;

        setTitle(db2, svf);//ヘッダ
        printCnt = 1;
        String subfield = "";
        for (Iterator iterator = printList.iterator(); iterator.hasNext();) {
            final PrintData printData = (PrintData) iterator.next();
            if (printCnt > maxCnt) {
                svf.VrEndPage();
                setTitle(db2, svf);//ヘッダ
            	printCol = 1;
                printCnt = 1;
            }
            if (printCnt != 1 && printCnt % 9 == 1) {
            	printCol++;
            }
            printLine = printCnt % 9 == 0 ? 9 : printCnt % 9;

            //受験番号
            svf.VrsOutn("EXAMNO" + String.valueOf(printCol), printLine, printData._receptNo);

            printCnt++;
            _hasData = true;
        }
        if (_hasData) {
            svf.VrEndPage();
        }
    }

    private void setTitle(final DB2UDB db2, final Vrw32alp svf) {
//        String setYear = KNJ_EditDate.h_format_JP_N(db2, _param._entExamYear + "/04/01");
//        final Calendar cal = Calendar.getInstance();
//        final String printDateTime = KNJ_EditDate.h_format_thi(_param._loginDate, 0);// + "　" + cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE);
//        svf.VrsOut("DATE", printDateTime);
//        svf.VrsOut("TITLE", setYear + "度　" + "　" + _param._testdivName + "　受験者確認名簿");
        svf.VrsOut("TITLE", _param._appDivName + _param._testDivName);
        svf.VrsOut("SUBTITLE", _param._passCourseName + "コース合格者");
        //svf.VrsOut("REMARK1", data); //空白とする
        //svf.VrsOut("REMARK2", data); //空白とする
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

// dbg int testexno = 1001;
// dbg int maxdbgcnt = 30;
// dbg int cnt = 0;
// dbg while (cnt < maxdbgcnt) {
// dbg     final PrintData printData = new PrintData(String.valueOf((testexno + cnt)));
// dbg     retList.add(printData);
// dbg     cnt++;
            while (rs.next()) {
                final String examNo = rs.getString("RECEPTNO");

                final PrintData printData = new PrintData(examNo);
                retList.add(printData);
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
        stb.append("     T1.RECEPTNO ");
        stb.append(" FROM ");
        stb.append("  ENTEXAM_RECEPT_DAT T1 ");
        stb.append("  LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT T3 ");
        stb.append("      ON T3.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("     AND T3.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("     AND T3.TESTDIV = T1.TESTDIV ");
        stb.append("     AND T3.EXAM_TYPE = T1.EXAM_TYPE ");
        stb.append("     AND T3.RECEPTNO = T1.RECEPTNO ");
        stb.append("     AND T3.SEQ = '006' ");
        stb.append(" WHERE ");
        stb.append("     T1.ENTEXAMYEAR = '" + _param._entExamYear + "' ");
        stb.append("     AND T1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND T1.TESTDIV = '" + _param._testDiv + "' ");
        stb.append("     AND (T3.REMARK8 = '" + _param._passCourse + "' OR T3.REMARK9 = '" + _param._passCourse + "') ");
        stb.append(" ORDER BY ");
        stb.append("     T1.RECEPTNO ");

        return stb.toString();
    }

    private class PrintData {
        final String _receptNo;

        public PrintData(
                final String receptNo
        ) {
        	_receptNo = receptNo;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 61733 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _loginYear;
        private final String _loginSemester;
        private final String _loginDate;
        private final String _entExamYear;
        private final String _applicantDiv;
        private final String _appDivName;
        private final String _testDiv;
        private final String _testDivName;
        private final String _passCourse;
        private final String _passCourseName;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _loginYear      = request.getParameter("LOGIN_YEAR");
            _loginSemester  = request.getParameter("LOGIN_SEMESTER");
            _loginDate      = request.getParameter("LOGIN_DATE");
            _entExamYear    = request.getParameter("ENTEXAMYEAR");
            _applicantDiv   = request.getParameter("APPLICANTDIV");
            _appDivName     = StringUtils.defaultString(getNameMst(db2, "NAME1", "L003", _applicantDiv));
            _testDiv        = request.getParameter("TESTDIV");
            _testDivName    = StringUtils.defaultString(getTestdivMst(db2, "TESTDIV_NAME", _testDiv));
            _passCourse     = request.getParameter("PASSCOURSE");
            final String appdivstr = _applicantDiv.equals("1") ? "J" : "H";
            _passCourseName = StringUtils.defaultString(getNameMst(db2, "NAME1", "L"+appdivstr+"13", _passCourse));
        }

        private String getNameMst(final DB2UDB db2, final String field, final String namecd1, final String namecd2) {
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT " + field + " FROM NAME_MST WHERE NAMECD1 = '" + namecd1 + "' AND NAMECD2 = '" + namecd2 + "' ");
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString(field);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return rtn;
        }
        private String getTestdivMst(final DB2UDB db2, final String field, final String testdiv) {
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT " + field + " FROM ENTEXAM_TESTDIV_MST WHERE ENTEXAMYEAR = '" + _entExamYear + "' AND APPLICANTDIV = '" + _applicantDiv + "' AND TESTDIV = '" + testdiv + "' ");
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString(field);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return rtn;
        }
    }
}

// eof
