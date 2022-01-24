/*
 * $Id: dd5d6e5d8db70bac59b9ef1f6bfa38c06082708a $
 *
 * 作成日: 2017/10/31
 * 作成者: yamashiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.math.BigDecimal;
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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

public class KNJL317U {

    private static final Log log = LogFactory.getLog(KNJL317U.class);

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
        svf.VrSetForm("KNJL317U.frm", 1);
        svf.VrsOut("TITLE", KNJ_EditDate.h_format_JP_N(_param._entexamyear + "/04/01") + "度　" + _param._applicantName + "　" + _param._testdivName + "　科目別ヒストグラム");
        svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._loginDate));
        final List printList = getList(db2);
        int kamokuCnt = 1;
        for (Iterator iterator = printList.iterator(); iterator.hasNext();) {
            final TestKamoku testKamoku = (TestKamoku) iterator.next();
            svf.VrsOutn("CLASS_NAME", kamokuCnt, testKamoku._name);
            svf.VrsOutn("EXAM_NUM", kamokuCnt, testKamoku._count);
            if (NumberUtils.isNumber(testKamoku._calcAvg)) {
            	final BigDecimal setAvg = new BigDecimal(testKamoku._calcAvg).setScale(1, BigDecimal.ROUND_HALF_UP);
            	svf.VrsOutn("AVERAGE", kamokuCnt, setAvg.toString());
            }
            if (NumberUtils.isNumber(testKamoku._calcStddev)) {
            	final BigDecimal setStddev = new BigDecimal(testKamoku._calcStddev).setScale(2, BigDecimal.ROUND_HALF_UP);
            	svf.VrsOutn("VAL", kamokuCnt, setStddev.toString());
            }
            int lineCnt = 1;
            for (Iterator itRange = testKamoku._scoreRangeList.iterator(); itRange.hasNext();) {
                final ScoreRange scoreRange = (ScoreRange) itRange.next();
                svf.VrsOutn("NUM" + lineCnt, kamokuCnt, String.valueOf(scoreRange._cnt));
                if (scoreRange._cnt > 0) {
                    String setRange = "";
                    final BigDecimal divisionCnt = new BigDecimal(scoreRange._cnt).divide(testKamoku._rangeVal, 0, BigDecimal.ROUND_UP);
                    for (int i = 0; i < divisionCnt.intValue(); i++) {
                        setRange += "□";
                    }
                    svf.VrsOutn("TOTAL" + lineCnt, kamokuCnt, setRange);
                }
                lineCnt++;
            }
            kamokuCnt++;
            _hasData = true;
        }
        if (_hasData) {
            svf.VrEndPage();
        }
    }

    private List getList(final DB2UDB db2) {
        final List retList = new ArrayList();
        final String sql = getKamokuSql();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String cd = rs.getString("NAMECD2");
                final String name = rs.getString("NAME1");
                final String count = rs.getString("COUNT");
                final String calcStddev = rs.getString("CALC_STDDEV");
                final String calcAvg = rs.getString("CALC_AVG");
                final TestKamoku testKamoku = new TestKamoku(cd, name, count, calcStddev, calcAvg);
                testKamoku.setRangeScoreCnt(db2);
                retList.add(testKamoku);
            }
        } catch (Exception e) {
            log.error("exception!", e);
        }
        return retList;
    }

    private String getKamokuSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     L009.NAMECD2, ");
        stb.append("     L009.NAME1, ");
        stb.append("     J_AVG.COUNT, ");
        stb.append("     J_AVG.CALC_STDDEV, ");
        stb.append("     J_AVG.CALC_AVG ");
        stb.append(" FROM ");
        stb.append("     NAME_MST L009 ");
        stb.append("     LEFT JOIN ENTEXAM_JUDGE_AVARAGE_DAT J_AVG ON J_AVG.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("          AND J_AVG.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("          AND J_AVG.TESTDIV = '" + _param._testDiv + "' ");
        stb.append("          AND J_AVG.EXAM_TYPE = '1' ");
        stb.append("          AND L009.NAMECD2 = J_AVG.TESTSUBCLASSCD ");
        stb.append(" WHERE ");
        stb.append("     L009.NAMECD1 = 'L009' ");
        stb.append(" ORDER BY ");
        stb.append("     L009.NAMECD2 ");
        return stb.toString();
    }

    private class ScoreRange {
        final int _minScore;
        final int _maxScore;
        int _cnt;
        public ScoreRange(
                final int minScore,
                final int maxScore
        ) {
            _minScore = minScore;
            _maxScore = maxScore;
            _cnt = 0;
        }
    }

    private class TestKamoku {
        final String _cd;
        final String _name;
        final String _count;
        final String _calcStddev;
        final String _calcAvg;
        final List _scoreRangeList;
        int _maxCnt;
        BigDecimal _rangeVal;
        public TestKamoku(
                final String cd,
                final String name,
                final String count,
                final String calcStddev,
                final String calcAvg
        ) {
            _cd = cd;
            _name = name;
            _count = count;
            _calcStddev = calcStddev;
            _calcAvg = calcAvg;
            _scoreRangeList = setScoreRangeList();
            _maxCnt = 0;
        }

        private List setScoreRangeList() {
            final List retList = new ArrayList();
            retList.add(new ScoreRange(100, 100));
            final int maxCnt = 99;
            final int range = 5;
            int setMaxCnt = 99;
            int setMinCnt = 95;
            for (int i = range; i < maxCnt; i += range) {
                final ScoreRange scoreRange = new ScoreRange(setMinCnt, setMaxCnt);
                retList.add(scoreRange);
                setMaxCnt -= range;
                setMinCnt -= range;
            }
            retList.add(new ScoreRange(1, 4));
            retList.add(new ScoreRange(0, 0));
            return retList;
        }

        private void setRangeScoreCnt(final DB2UDB db2) {
            for (Iterator itRange = _scoreRangeList.iterator(); itRange.hasNext();) {
                ScoreRange scoreRange = (ScoreRange) itRange.next();
                final String sql = getScoreCntSql(scoreRange);
                PreparedStatement ps = null;
                ResultSet rs = null;
                try {
                    ps = db2.prepareStatement(sql);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        final int cnt = rs.getInt("CNT");
                        scoreRange._cnt = cnt;
                        if (_maxCnt < cnt) {
                            _maxCnt = cnt;
                        }
                    }
                } catch (Exception e) {
                    log.error("exception!", e);
                }
            }
            _rangeVal = new BigDecimal(_maxCnt).divide(new BigDecimal(70), 2, BigDecimal.ROUND_HALF_UP);
        }

        private String getScoreCntSql(final ScoreRange scoreRange) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     COUNT(*) AS CNT ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_SCORE_DAT E_SCORE ");
            stb.append(" WHERE ");
            stb.append("     E_SCORE.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
            stb.append("     AND E_SCORE.APPLICANTDIV = '" + _param._applicantDiv + "' ");
            stb.append("     AND E_SCORE.TESTDIV = '" + _param._testDiv + "' ");
            stb.append("     AND E_SCORE.EXAM_TYPE = '1' ");
            stb.append("     AND E_SCORE.TESTSUBCLASSCD = '" + _cd + "' ");
            stb.append("     AND E_SCORE.SCORE BETWEEN " + scoreRange._minScore + " AND " + scoreRange._maxScore + " ");

            return stb.toString();
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 58010 $");
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
