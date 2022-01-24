/*
 * $Id: 4898be80f0d35207270b6bd5b8eb683d2508c34a $
 *
 * 作成日: 2017/07/05
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;


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
import servletpack.KNJZ.detail.KNJ_EditDate;

public class KNJL321P {

    private static final Log log = LogFactory.getLog(KNJL321P.class);

    private static final String ZEN_JUKEN = "9";

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
        svf.VrSetForm("KNJL321P.frm", 4);
        setTitle(svf);

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final String sql = sql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            int lineCnt = 1;
            while (rs.next()) {
                if (ZEN_JUKEN.equals(rs.getString("SHDIV"))) {
                    svf.VrsOut("SUBTITLE", "合格ライン" + getTitleScore(rs.getString("BORDER_SCORE")) + "点以上　　合計偏差値" + getTitleScore(rs.getString("BORDER_DEVIATION")) + "以上");
                    svf.VrsOut("PLACE1", "全受験者");
                }
                svf.VrsOutn("NUM1", lineCnt, rs.getString("SYUTSUGAN"));
                svf.VrsOutn("NUM2", lineCnt, rs.getString("KESSEKI"));
                svf.VrsOutn("NUM3", lineCnt, rs.getString("GOUKAKU"));
                svf.VrsOutn("NUM4", lineCnt, rs.getString("FUGOUKAKU"));
                svf.VrsOutn("PASS1", lineCnt, rs.getString("GOUKAKU_RITSU"));
                lineCnt++;
                _hasData = true;
            }
            if (_hasData) {
                svf.VrEndRecord();
            }

            db2.commit();
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
    }

    private String getTitleScore(final String score) throws SQLException {
        String retStr = "    ";
        if (null != score) {
            retStr = score;
        }
        return retStr;
    }

    private void setTitle(final Vrw32alp svf) {
        svf.VrsOut("TITLE", KNJ_EditDate.h_format_JP_N(_param._entexamYear + "-04-01") + "度　" + _param._applicantDivName + "入試 " + _param._testDivName + "　判定資料");
        svf.VrsOut("INTERVIEW", "1".equals(_param._mensetsuDiv) ? "面接なし" : "面接あり");
    }

    private String sql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     PASSING.SHDIV, ");
        stb.append("     PASSING.BORDER_SCORE, ");
        stb.append("     PASSING.BORDER_DEVIATION, ");
        stb.append("     PASSING.SUCCESS_CNT_SPECIAL AS SYUTSUGAN, ");
        stb.append("     PASSING.SUCCESS_CNT_SPECIAL2 AS KESSEKI, ");
        stb.append("     PASSING.SUCCESS_CNT AS GOUKAKU, ");
        stb.append("     PASSING.SUCCESS_CNT_CANDI AS FUGOUKAKU, ");
        stb.append("     PASSING.SUCCESS_RATE AS GOUKAKU_RITSU ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_PASSINGMARK_MST PASSING ");
        stb.append(" WHERE ");
        stb.append("     PASSING.ENTEXAMYEAR = '" + _param._entexamYear + "' ");
        stb.append("     AND PASSING.APPLICANTDIV= '" + _param._applicantDiv + "' ");
        stb.append("     AND PASSING.TESTDIV = '" + _param._testDiv + "' ");
        stb.append("     AND PASSING.EXAM_TYPE = '" + _param._mensetsuDiv + "' ");
        stb.append("     AND PASSING.COURSECD = '0' ");
        stb.append("     AND PASSING.MAJORCD = '000' ");
        stb.append("     AND PASSING.EXAMCOURSECD = '0000' ");
        stb.append(" ORDER BY ");
        stb.append("     CASE WHEN PASSING.SHDIV = '9' ");
        stb.append("          THEN '0' ");
        stb.append("          ELSE PASSING.SHDIV ");
        stb.append("     END ");

        return stb.toString();
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
        final String _applicantDiv;
        final String _applicantDivName;
        final String _testDiv;
        final String _testDivName;
        final String _testDate;
        final String _mensetsuDiv;
        final String _entexamYear;
        final String _loginYear;
        final String _loginSemester;
        final String _loginDate;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _mensetsuDiv = request.getParameter("MENSETSUDIV");
            _entexamYear = request.getParameter("ENTEXAMYEAR");
            _loginYear = request.getParameter("LOGIN_YEAR");
            _loginSemester = request.getParameter("LOGIN_SEMESTER");
            _loginDate = request.getParameter("LOGIN_DATE");
            _applicantDivName = getNameMst(db2, "L003", _applicantDiv, "NAME1");
            final String testNameCd1 = "1".equals(_applicantDiv) ? "L024" : "L004";
            _testDivName = getNameMst(db2, testNameCd1, _testDiv, "NAME1");
            _testDate = getNameMst(db2, testNameCd1, _testDiv, "NAMESPARE1");
        }

        private String getNameMst(final DB2UDB db2, final String nameCd1, final String nameCd2, final String fieldName) throws SQLException {
            String retStr = "";
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = getNameMstSql(nameCd1, nameCd2);
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    retStr = rs.getString(fieldName);
                }

                db2.commit();
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retStr;
        }

        private String getNameMstSql(final String namecd1, final String namecd2) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     * ");
            stb.append(" FROM ");
            stb.append("     NAME_MST ");
            stb.append(" WHERE ");
            stb.append("     NAMECD1 = '" + namecd1 + "' ");
            stb.append("     AND NAMECD2 = '" + namecd2 + "' ");
            return stb.toString();
        }

    }
}

// eof

