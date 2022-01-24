/*
 * $Id: 807355b7920b4f6f0ee936234cc86a5dce04d171 $
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

public class KNJL505J_4 {

    private static final Log log = LogFactory.getLog(KNJL505J_4.class);

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
        svf.VrSetForm("KNJL505J_4.frm", 1);
        final List printList = getList(db2);
        final int maxCnt = 6;
        int printLine = 1;
        int printCol = 1;

        for (Iterator iterator = printList.iterator(); iterator.hasNext();) {
            final PrintData printData = (PrintData) iterator.next();
            if (printLine > maxCnt) {
                svf.VrEndPage();
                svf.VrSetForm("KNJL505J_4.frm", 1);
                printCol = 1;
                printLine = 1;
            }

            //受験番号
            svf.VrsOutn("EXAM_NO" + String.valueOf(printCol), printLine, printData._receptNo);

            printLine = printCol == 2 ? printLine + 1 : printLine;
            printCol = printCol % 2 == 0 ? 1 : 2;
            _hasData = true;
        }

        svf.VrEndPage();
    }

    private List getList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        final String sql = sql();
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String receptNo = rs.getString("RECEPTNO");

                final PrintData printData = new PrintData(receptNo);
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

    private String sql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("  T1.RECEPTNO ");
        stb.append(" FROM ");
        stb.append("  ENTEXAM_RECEPT_DAT T1 ");
        stb.append("  INNER JOIN ENTEXAM_APPLICANTBASE_DAT T2 ");
        stb.append("    ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("   AND T2.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("   AND T2.EXAMNO = T1.EXAMNO ");
        stb.append(" WHERE ");
        stb.append("  T1.ENTEXAMYEAR = '"+_param._entexamyear+"' ");
        stb.append("  AND T1.APPLICANTDIV = '"+_param._applicantdiv+"' ");
        stb.append("  AND T1.TESTDIV = '"+_param._testdiv+"' ");
        if (!"".equals(_param._receptNoFrom)) {
            stb.append("  AND T1.RECEPTNO >= '"+_param._receptNoFrom+"' ");
        }
        if (!"".equals(_param._receptNoTo)) {
            stb.append("  AND T1.RECEPTNO <= '"+_param._receptNoTo+"' ");
        }
        if (!"".equals(_param._examtype)) {
            stb.append("  AND T1.EXAM_TYPE = '"+_param._examtype+"' ");
        }
        stb.append(" ORDER BY ");
        stb.append("     RECEPTNO ");
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
        log.fatal("$Revision: 66749 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _entexamyear;
        final String _applicantdiv;
        final String _testdiv;
        final String _examtype;
        final String _receptNoFrom;
        final String _receptNoTo;
        final String _loginDate;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _applicantdiv = request.getParameter("APPLICANTDIV");
            _testdiv = request.getParameter("TESTDIV");
            _examtype = StringUtils.defaultString(request.getParameter("EXAM_TYPE"), "");
            _receptNoFrom = !StringUtils.isBlank(request.getParameter("RECEPTNO_FROM")) ? request.getParameter("RECEPTNO_FROM") : "";
            _receptNoTo = !StringUtils.isBlank(request.getParameter("RECEPTNO_TO")) ? request.getParameter("RECEPTNO_TO") : "";
            _loginDate = request.getParameter("LOGIN_DATE");
        }
    }

}

// eof

