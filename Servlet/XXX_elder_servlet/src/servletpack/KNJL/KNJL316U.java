/*
 * $Id: 50eb7f751abe62e832c36f79ce344956339c3b62 $
 *
 * 作成日: 2017/11/01
 * 作成者: yamashiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

public class KNJL316U {

    private static final Log log = LogFactory.getLog(KNJL316U.class);

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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        svf.VrSetForm("KNJL316U.frm", 1);
        svf.VrsOut("TITLE", KNJ_EditDate.h_format_JP_N(_param._entexamyear + "/04/01") + "度　" + _param._applicantName + "　" + _param._testdivName + "　4科目合計点の度数分布表");
        svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._loginDate));

        final List printRangeList = getScoreRangeList(db2);
        final Map printRangeDataMap = getScoreRangeMap(db2);

        final int maxLine = 100;
        int lineCnt = 1;
        int colCnt = 1;
        int ruikeiCnt = 0;
        for (Iterator iterator = printRangeList.iterator(); iterator.hasNext();) {
            final ScoreRange scoreRange = (ScoreRange) iterator.next();
            if (lineCnt > maxLine) {
                lineCnt = 1;
                colCnt++;
            }
            int setCnt = 0;
            if (printRangeDataMap.containsKey(String.valueOf(scoreRange._maxScore))) {
                final String cnt = (String) printRangeDataMap.get(String.valueOf(scoreRange._maxScore));
                setCnt = Integer.parseInt(cnt);
            }
            ruikeiCnt += setCnt;
            svf.VrsOutn("SCORE" + colCnt, lineCnt, String.valueOf(scoreRange._maxScore));
            svf.VrsOutn("NUM" + colCnt, lineCnt, String.valueOf(setCnt));
            svf.VrsOutn("TOTAL" + colCnt, lineCnt, String.valueOf(ruikeiCnt));

            lineCnt++;
            _hasData = true;
        }
        if (_hasData) {
            svf.VrEndPage();
        }
    }

    private List getScoreRangeList(final DB2UDB db2) throws SQLException {
        final List retList = new ArrayList();
        int perfect = 0;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getPerfectSql();
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                perfect = rs.getInt("PERFECT");
            }
        } catch (Exception e) {
            log.error("exception!", e);
        }

        for (int range = perfect; range >= 0; range--) {
            final ScoreRange scoreRange = new ScoreRange(range, range);
            retList.add(scoreRange);
        }
        return retList;
    }

    private String getPerfectSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     SUM(PERFECT) AS PERFECT ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_PERFECT_MST ");
        stb.append(" WHERE ");
        stb.append("     ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND TESTDIV = '" + _param._testDiv + "' ");
        return stb.toString();
    }

    private Map getScoreRangeMap(final DB2UDB db2) throws SQLException {
        final Map retMap = new HashMap();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getScoreSql();
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String total4 = rs.getString("TOTAL4");
                final String cnt = rs.getString("CNT");
                retMap.put(total4, cnt);
            }
        } catch (Exception e) {
            log.error("exception!", e);
        }

        return retMap;
    }

    private String getScoreSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     TOTAL4, ");
        stb.append("     COUNT(*) AS CNT ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_RECEPT_DAT ");
        stb.append(" WHERE ");
        stb.append("     ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND TESTDIV = '" + _param._testDiv + "' ");
        stb.append("     AND TOTAL4 IS NOT NULL ");
        stb.append(" GROUP BY ");
        stb.append("     TOTAL4 ");
        return stb.toString();
    }

    private class ScoreRange {
        final int _minScore;
        final int _maxScore;
        public ScoreRange(
                final int minScore,
                final int maxScore
        ) {
            _minScore = minScore;
            _maxScore = maxScore;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 56953 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _applicantDiv;
        final String _testDiv;
        final String _entexamyear;
        final String _loginYear;
        final String _loginSemester;
        final String _loginDate;
        final String _prgid;
        final String _applicantName;
        final String _testdivName;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _loginYear = request.getParameter("LOGIN_YEAR");
            _loginSemester = request.getParameter("LOGIN_SEMESTER");
            _loginDate = request.getParameter("LOGIN_DATE");
            _prgid = request.getParameter("PRGID");
            _applicantName = StringUtils.defaultString(getNameMst(db2, "NAME1", "L003", _applicantDiv));
            _testdivName = StringUtils.defaultString(getNameMst(db2, "NAME1", "L004", _testDiv));
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

    }
}

// eof
