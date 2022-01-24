/*
 * $Id: $
 *
 * 作成日: 2020/12/14
 * 作成者: ishimine
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

public class KNJL620I_SEISEKI {

    private static final Log log = LogFactory.getLog(KNJL620I_SEISEKI.class);

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
        final String nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_param._entexamyear)) + "年度 ";
        Map printMap = getSeiseki(db2); //類別、コース別成績データ
        if(printMap.isEmpty()) return;

        //類別成績表
        printRuibetsu(svf, nendo, printMap);
        //コース別成績表
        printCourse(svf, nendo, printMap);
    }

    //類別用
    private void printRuibetsu(final Vrw32alp svf, final String nendo, final Map printMap) {
        int line = 1; // 行数
        svf.VrSetForm("KNJL620I_2_1.frm", 1);
        svf.VrsOut("TITLE", nendo + " " + _param._testDivName + "　" + "類別成績表");
        int col = 1; //列
        //類別毎のループ
        for(Iterator iteRui = _param._ruibetsuMap.keySet().iterator(); iteRui.hasNext();) {
            final String keyRui = (String)iteRui.next();
            final String ruibetsuName = (String)_param._ruibetsuMap.get(keyRui);
            svf.VrsOutn("COURSE_NAME", line, ruibetsuName); //類別名称
            col = 1;

            final Tokei tokei = (Tokei)printMap.get(keyRui + "_9999");
            if(tokei != null) {
                for(Iterator iteScore = tokei._scoreMap.keySet().iterator(); iteScore.hasNext();) {
                    final String key2 = (String)iteScore.next();
                    final Score score = (Score)tokei._scoreMap.get(key2);
                    svf.VrsOut("CLASS_NAME1_" + col, score._subclassName); //科目名称
                    printRuibetsuScore(svf, score, col, line);
                    col++;
                }
            }
            line++;
        }

        //全体
        svf.VrsOutn("COURSE_NAME", line, "全体"); //類別名称
        final Tokei tokei = (Tokei)printMap.get("9_9999");
        if(tokei != null) {
            col = 1;
            for(Iterator iteScore = tokei._scoreMap.keySet().iterator(); iteScore.hasNext();) {
                final String key2 = (String)iteScore.next();
                final Score score = (Score)tokei._scoreMap.get(key2);
                printRuibetsuScore(svf, score, col, line);
                col++;
            }
        }
        svf.VrEndPage();
        _hasData = true;
    }

    //コース別用
    private void printCourse(final Vrw32alp svf, final String nendo, final Map printMap) {
        int line = 1; // 行数
        svf.VrSetForm("KNJL620I_2_2.frm", 1);
        svf.VrsOut("TITLE", nendo + " " + _param._testDivName + "　" + "類コース別成績表");
        int col = 1; //列
        int cnt = 1; //繰り返し回数
        //コース別毎のループ
        for(Iterator iteCourse = _param._courseMap.keySet().iterator(); iteCourse.hasNext();) {
            final String keyCourse = (String)iteCourse.next();
            final String courseAbbv = (String)_param._courseMap.get(keyCourse);
            final String field = cnt % 2 == 0 ? "2" : "";
            svf.VrsOutn("COURSE_NAME" + field, line, courseAbbv); //コース略称

            final Tokei tokei = (Tokei)printMap.get(keyCourse);
            if(tokei != null) {
                col = 1;
                for(Iterator iteScore = tokei._scoreMap.keySet().iterator(); iteScore.hasNext();) {
                    final String key2 = (String)iteScore.next();
                    final Score score = (Score)tokei._scoreMap.get(key2);
                    printCourseScore(svf, score, col, line, cnt);
                    col++;
                }
            }
            if(cnt++ % 2 == 0) {
                line++;
            }
        }

        //全体
        svf.VrsOutn("COURSE_NAME", 5, "全体"); //コース名称
        final Tokei tokei = (Tokei)printMap.get("9_9999");
        if(tokei != null) {
            col = 1;
            for(Iterator iteScore = tokei._scoreMap.keySet().iterator(); iteScore.hasNext();) {
                final String key2 = (String)iteScore.next();
                final Score score = (Score)tokei._scoreMap.get(key2);
                svf.VrsOut("CLASS_NAME1_" + col, score._subclassName); //科目名称
                svf.VrsOut("CLASS_NAME2_" + col, score._subclassName); //科目名称
                printCourseScore(svf, score, col, 5, 1);
                col++;
            }
        }
        svf.VrEndPage();
        _hasData = true;
    }

    private void printRuibetsuScore(final Vrw32alp svf, final Score score, final int col, final int line) {
        if(score != null) {
            svf.VrsOutn("AVE1_" + col, line, score._avg); //平均
            svf.VrsOutn("MAX1_" + col, line, score._max); //最高
            svf.VrsOutn("MIN1_" + col, line, score._min); //最低
           }
    }

    private void printCourseScore(final Vrw32alp svf, final Score score, final int col, final int line, final int cnt) {
        if(score != null) {
            final String field = cnt % 2 == 0 ? "2" : "1";
            svf.VrsOutn("AVE" + field + "_" + col, line, score._avg); //平均
            svf.VrsOutn("MAX" + field + "_" + col, line, score._max); //最高
            svf.VrsOutn("MIN" + field + "_" + col, line, score._min); //最低
           }
    }

    private Map<String, Tokei> getSeiseki(final DB2UDB db2) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        Map<String, Tokei> tokeiMap = new LinkedMap();
        Tokei tokei = null;

        try {
            final String sql = getSeisekiSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String ruibetsu = rs.getString("SHDIV");
                final String course = rs.getString("EXAMCOURSECD");
                final String testsubclasscd = rs.getString("TESTSUBCLASSCD");
                final String avg = rs.getString("AVG");
                final String max = rs.getString("HIGHSCORE");
                final String min = rs.getString("LOWSCORE");
                final String ruibetsuName = rs.getString("RUIBETSU");
                final String courseName = rs.getString("COURSE");
                final String subclassName = rs.getString("NAME1");
                final String key = ruibetsu + "_" + course;

                if(tokeiMap.containsKey(key)) {
                    tokei = tokeiMap.get(key);
                } else {
                    tokei = new Tokei(ruibetsu, course, ruibetsuName, courseName);
                    tokeiMap.put(key, tokei);
                }

                if(!tokei._scoreMap.containsKey(testsubclasscd)) {
                    final Score score = new Score(testsubclasscd, avg, max, min, subclassName);
                    tokei._scoreMap.put(testsubclasscd, score);
                }

            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }

        return tokeiMap;
    }

    private String getSeisekiSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     TOKEI.MAJORCD, ");
        stb.append("     TOKEI.TESTDIV, ");
        stb.append("     TOKEI.SHDIV, ");
        stb.append("     TOKEI.EXAMCOURSECD, ");
        stb.append("     TOKEI.TESTSUBCLASSCD, ");
        stb.append("     TOKEI.HIGHSCORE, ");
        stb.append("     TOKEI.LOWSCORE, ");
        stb.append("     TOKEI.AVG, ");
        stb.append("     GEN01.GENERAL_NAME AS RUIBETSU, ");
        stb.append("     GEN02.GENERAL_NAME AS COURSE, ");
        stb.append("     L009.NAME1 ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_TOKEI_HIGH_LOW_DAT TOKEI ");
        stb.append(" LEFT JOIN ");
        stb.append("     ENTEXAM_GENERAL_MST GEN01 ");
        stb.append("     ON GEN01.ENTEXAMYEAR = TOKEI.ENTEXAMYEAR ");
        stb.append("     AND GEN01.APPLICANTDIV = TOKEI.APPLICANTDIV ");
        stb.append("     AND GEN01.GENERAL_DIV = '01' ");
        stb.append("     AND GEN01.GENERAL_CD = TOKEI.SHDIV ");
        stb.append(" LEFT JOIN ");
        stb.append("     ENTEXAM_GENERAL_MST GEN02 ");
        stb.append("     ON GEN02.ENTEXAMYEAR = TOKEI.ENTEXAMYEAR ");
        stb.append("     AND GEN02.APPLICANTDIV = TOKEI.APPLICANTDIV ");
        stb.append("     AND GEN02.GENERAL_DIV = '02' ");
        stb.append("     AND GEN02.GENERAL_CD = TOKEI.EXAMCOURSECD ");
        stb.append(" LEFT JOIN ");
        stb.append("     ENTEXAM_SETTING_MST L009 ");
        stb.append("     ON L009.ENTEXAMYEAR = TOKEI.ENTEXAMYEAR ");
        stb.append("     AND L009.APPLICANTDIV = TOKEI.APPLICANTDIV ");
        stb.append("     AND L009.SETTING_CD = 'L009' ");
        stb.append("     AND L009.SEQ = TOKEI.TESTSUBCLASSCD ");
        stb.append(" WHERE ");
        stb.append("     TOKEI.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND TOKEI.TESTDIV = '" + _param._testDiv + "' ");
        stb.append(" ORDER BY ");
        stb.append("     TOKEI.MAJORCD, ");
        stb.append("     TOKEI.TESTDIV, ");
        stb.append("     TOKEI.SHDIV, ");
        stb.append("     TOKEI.EXAMCOURSECD, ");
        stb.append("     TOKEI.TESTSUBCLASSCD ");

        return stb.toString();
    }

    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Id$");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    private class Tokei {
        final String _shDiv;
        final String _examCourseCd;
        final String _ruibetsuName;
        final String _courseName;
        final Map _scoreMap;

        Tokei(final String shDiv, final String examCourseCd, final String ruibetsuName, final String courseName) {
            _shDiv = shDiv;
            _examCourseCd = examCourseCd;
            _ruibetsuName = ruibetsuName;
            _courseName = courseName;
            _scoreMap = new LinkedMap();
        }
    }

    private class Score {
        final String _testsubclasscd;
        final String _avg;
        final String _max;
        final String _min;
        final String _subclassName;

        Score(final String testsubclasscd, final String avg, final String max, final String min, final String subclassName) {
            _testsubclasscd = testsubclasscd;
            _avg = avg;
            _max = max;
            _min = min;
            _subclassName = subclassName;
        }
    }

    /** パラメータクラス */
    private static class Param {
        private final String _entexamyear;
        private final String _testDiv;
        private final String _majorCd;
        private final String _testDivName;
        private final Map _ruibetsuMap; //類別
        private final Map _courseMap; //コース別

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _testDiv = request.getParameter("TESTDIV");
            _majorCd = request.getParameter("MAJORCD");
            _testDivName = getTestDivName(db2);
            _ruibetsuMap = getRuibetsuMst(db2);
            _courseMap = getCourseMst(db2);
        }

        private String getTestDivName(final DB2UDB db2) {
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;

            String sql = " SELECT TESTDIV_NAME FROM ENTEXAM_TESTDIV_MST WHERE ENTEXAMYEAR='" + _entexamyear + "' AND APPLICANTDIV = '2' AND TESTDIV = '" + _testDiv + "' ";
            log.debug(" testdiv sql =" + sql);

            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString("TESTDIV_NAME");
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtn;
        }

        private Map getRuibetsuMst(final DB2UDB db2) {
            final Map rtnMap = new LinkedMap();
            PreparedStatement ps = null;
            ResultSet rs = null;

            String sql = " SELECT * FROM ENTEXAM_GENERAL_MST WHERE ENTEXAMYEAR = '" + _entexamyear + "' AND APPLICANTDIV = '2' AND TESTDIV = '0' AND GENERAL_DIV = '01' ORDER BY GENERAL_CD ";
            log.debug(" ruibetsu sql =" + sql);

            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String generalCd = rs.getString("GENERAL_CD");
                    final String generalName = rs.getString("GENERAL_NAME");
                    rtnMap.put(generalCd, generalName);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtnMap;
        }

        private Map getCourseMst(final DB2UDB db2) {
            final Map rtnMap = new LinkedMap();
            PreparedStatement ps = null;
            ResultSet rs = null;

            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("   * ");
            stb.append(" FROM ");
            stb.append("   ENTEXAM_GENERAL_MST DIV02 ");
            stb.append(" WHERE ");
            stb.append("   ENTEXAMYEAR = '" + _entexamyear + "' ");
            stb.append("   AND APPLICANTDIV = '2'  ");
            stb.append("   AND TESTDIV = '0' ");
            stb.append("   AND GENERAL_DIV = 02 ");
            stb.append(" ORDER BY");
            stb.append("   GENERAL_CD");

            log.debug(" course sql =" + stb.toString());

            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String ruibetsuCd = rs.getString("REMARK1");
                    final String generalCd = rs.getString("GENERAL_CD");
                    final String generalAbbv = rs.getString("GENERAL_ABBV");
                    rtnMap.put(ruibetsuCd + "_" + generalCd, generalAbbv);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtnMap;
        }
    }
}

// eof

