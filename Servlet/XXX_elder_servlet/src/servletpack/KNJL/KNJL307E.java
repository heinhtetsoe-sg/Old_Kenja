/**     プロジェクト/プロパティ/javaコード・スタイル/コード・テンプレート/パターン       ***/

/*
 * $Id: 6e08ac121f7ef34a5982527dce42d0afea6b3357 $
 *
 * 作成日: 2019/01/11
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

public class KNJL307E {

    private static final Log log = LogFactory.getLog(KNJL307E.class);

    private boolean _hasData;
    private final String TOTALCD = "99";

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
        svf.VrSetForm("KNJL307E.frm", 1);
        svf.VrsOut("TITLE", KNJ_EditDate.getAutoFormatYear(db2, Integer.parseInt(_param._entExamYear)) + "年度　" + _param._applicantdivName + _param._testdivName + "　平均点");
        final Map examCntMap = getExamCntMap(db2);
        final Map scoreMap = getScoreMap(db2);
        final int maxLine = 5;
        int examCnt = 1;
        boolean hasOngakukaHeiganCnt = false;
        final Map totalMap = new HashMap();
        for (Iterator itDesire = _param._desireList.iterator(); itDesire.hasNext();) {
            final Desire desire = (Desire) itDesire.next();
            if (examCntMap.containsKey(desire._cd)) {
                final ExamCntTable examCntTable = (ExamCntTable) examCntMap.get(desire._cd);
                svf.VrsOutn("DESIREDIV", examCnt, desire._name);
                if (examCntTable._ongakukaHeiganCnt > 0) {
                    svf.VrsOutn("NUM2", examCnt, String.valueOf(examCntTable._totalCnt));
                    final String heiganCnt = String.valueOf(examCntTable._ongakukaHeiganCnt);
                    svf.VrsOutn("NUM3", examCnt, "(" + StringUtils.repeat(" ", 3 - heiganCnt.length()) + heiganCnt + ")");
                    hasOngakukaHeiganCnt = true;
                } else {
                    svf.VrsOutn("NUM", examCnt, String.valueOf(examCntTable._totalCnt));
                }
                svf.VrsOutn("NOTICE", examCnt, String.valueOf(examCntTable._kessekiCnt));
            } else {
                svf.VrsOutn("DESIREDIV", examCnt, desire._name);
                svf.VrsOutn("NUM", examCnt, "0");
                svf.VrsOutn("NOTICE", examCnt, "0");
            }

            if (scoreMap.containsKey(desire._cd)) {
                final ScoreTable scoreTable = (ScoreTable) scoreMap.get(desire._cd);
                printAvg(svf, examCnt, scoreTable, totalMap);
            } else {
                printAvg(svf, examCnt, new ScoreTable(), totalMap);
            }
            examCnt++;
            if (maxLine <= examCnt) {
            	break;
            }
        }

        examCnt = maxLine;
        svf.VrsOutn("DESIREDIV", examCnt, "平均点");
        final ExamCntTable totalExamCntTable = (ExamCntTable) examCntMap.get("TOTAL");
        if (null != totalExamCntTable) {
            if (totalExamCntTable._ongakukaHeiganCnt > 0) {
                svf.VrsOutn("NUM2", examCnt, String.valueOf(totalExamCntTable._totalCnt));
                final String heiganCnt = String.valueOf(totalExamCntTable._ongakukaHeiganCnt);
                svf.VrsOutn("NUM3", examCnt, "(" + StringUtils.repeat(" ", 3 - heiganCnt.length()) + heiganCnt + ")");
                hasOngakukaHeiganCnt = true;
            } else {
            	svf.VrsOutn("NUM", examCnt, String.valueOf(totalExamCntTable._totalCnt));
            }
            svf.VrsOutn("NOTICE", examCnt, String.valueOf(totalExamCntTable._kessekiCnt));
        } else {
            svf.VrsOutn("NUM", examCnt, "0");
            svf.VrsOutn("NOTICE", examCnt, "0");
        }
        for (Iterator itTotalMap = totalMap.keySet().iterator(); itTotalMap.hasNext();) {
            final String subclassCd = (String) itTotalMap.next();
            final ScoreCntTable scoreCntTable = (ScoreCntTable) totalMap.get(subclassCd);
            if (scoreCntTable._totalCnt > 0) {
            	final BigDecimal setVal = new BigDecimal(scoreCntTable._totalScore).divide(new BigDecimal(scoreCntTable._totalCnt), 1, BigDecimal.ROUND_HALF_UP);
            	if (TOTALCD.equals(subclassCd)) {
            		svf.VrsOutn("TOTAL4", examCnt, setVal.toString());
            	} else {
            		svf.VrsOutn("SCORE" + subclassCd, examCnt, setVal.toString());
            	}
            }
        }
        if (hasOngakukaHeiganCnt) {
    		svf.VrsOut("TEXT", "()内は音楽コースの併願者数です。");
        }
        svf.VrEndPage();
        _hasData = true;
    }

    private void printAvg(final Vrw32alp svf, final int examCnt, final ScoreTable scoreTable, Map totalMap) {
        for (Iterator itSubclass = _param._subclassList.iterator(); itSubclass.hasNext();) {

            final Subclass subclass = (Subclass) itSubclass.next();
            svf.VrsOut("SUBCLASS_NAME" + subclass._cd, subclass._name);

            //最終行出力の為に科目毎に加算していく
            if (!totalMap.containsKey(subclass._cd)) {
            	totalMap.put(subclass._cd, new ScoreCntTable(0, 0));
            }

            BigDecimal avgVal = scoreTable.avg(subclass._cd);
            if (null != avgVal) {
            	svf.VrsOutn("SCORE" + subclass._cd, examCnt, avgVal.toString());
            	ScoreCntTable scoreCntTable = (ScoreCntTable) totalMap.get(subclass._cd);
            	scoreCntTable._totalCnt += Integer.parseInt((String) scoreTable._countMap.get(subclass._cd));
            	scoreCntTable._totalScore += Integer.parseInt((String) scoreTable._scoreMap.get(subclass._cd));
            } else {
                svf.VrsOutn("SCORE" + subclass._cd, examCnt, "0.0");
            }
        }

        //3科目欄出力
        final String TOTAL4 = "TOTAL4";
        final BigDecimal setTotalAvg = scoreTable.avg(TOTAL4);
        if (null != setTotalAvg) {
        	svf.VrsOutn("TOTAL4", examCnt, setTotalAvg.toString());
        } else {
        	svf.VrsOutn("TOTAL4", examCnt, "0.0");
        }

        //最終行の3科目欄
        if (!totalMap.containsKey(TOTALCD)) {
        	totalMap.put(TOTALCD, new ScoreCntTable(0, 0));
        }
        if (null != setTotalAvg) {
        	ScoreCntTable scoreCntTable = (ScoreCntTable) totalMap.get(TOTALCD);
        	scoreCntTable._totalCnt += Integer.parseInt((String) scoreTable._countMap.get(TOTAL4));
        	scoreCntTable._totalScore += Integer.parseInt((String) scoreTable._scoreMap.get(TOTAL4));
        }
    }

    private Map getExamCntMap(final DB2UDB db2) {
        final Map retMap = (Map) new TreeMap();
        final String examCntSql = getExamCntSql();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            log.debug(" sql =" + examCntSql);
            ps = db2.prepareStatement(examCntSql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String desireDiv = rs.getString("DESIREDIV");
                final String desireName = rs.getString("DESIREDIVNAME");
                final int totalCnt = rs.getInt("TOTAL_CNT");
                final int kessekiCnt = rs.getInt("KESSEKI_CNT");
                final int ongakukaHeiganCnt = rs.getInt("ONGAKUKA_HEIGAN_CNT");
                final ExamCntTable examCntTbl = new ExamCntTable(desireName, totalCnt, kessekiCnt, ongakukaHeiganCnt);
                retMap.put(desireDiv, examCntTbl);
            }

        } catch (SQLException ex) {
            log.error("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }

        return retMap;
    }

    private String getExamCntSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     VALUE(VALUE(L061.NAMESPARE2, BASE.DESIREDIV), 'TOTAL') AS DESIREDIV, ");
        stb.append("     L058.NAME1 AS DESIREDIVNAME, ");
        stb.append("     SUM(CASE WHEN VALUE(BASE.JUDGEMENT, '0') <> '3' ");
        stb.append("              THEN 1 ");
        stb.append("              ELSE 0 ");
        stb.append("         END) AS TOTAL_CNT, ");
        stb.append("     SUM(CASE WHEN VALUE(BASE.JUDGEMENT, '0') = '3' ");
        stb.append("              THEN 1 ");
        stb.append("              ELSE 0 ");
        stb.append("         END) AS KESSEKI_CNT, ");
        stb.append("     SUM(CASE WHEN L061.NAMESPARE2 IS NOT NULL ");
        stb.append("              THEN 1 ");
        stb.append("              ELSE 0 ");
        stb.append("         END) AS ONGAKUKA_HEIGAN_CNT ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_APPLICANTBASE_DAT BASE ");
	    stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD33 ");
	    stb.append("            ON BD33.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
	    stb.append("           AND BD33.APPLICANTDIV = BASE.APPLICANTDIV ");
	    stb.append("           AND BD33.EXAMNO = BASE.EXAMNO ");
	    stb.append("           AND BD33.SEQ = '033' ");
	    stb.append("     LEFT JOIN V_NAME_MST L061 ");
	    stb.append("            ON L061.YEAR = BASE.ENTEXAMYEAR ");
	    stb.append("           AND L061.NAMECD1 = 'L061' ");
	    stb.append("           AND L061.NAMECD2 = BD33.REMARK3 ");
        stb.append("     LEFT JOIN NAME_MST L058 ");
        stb.append("          ON L058.NAMECD1 = 'L058' ");
        stb.append("         AND (L061.NAMESPARE2 IS NOT NULL AND L058.NAMECD2 = L061.NAMESPARE2 ");
        stb.append("           OR L061.NAMESPARE2 IS     NULL AND L058.NAMECD2 = BASE.DESIREDIV ");
        stb.append("             ) ");
        stb.append("     INNER JOIN V_NAME_MST L004 ");
        stb.append("            ON L004.YEAR       = BASE.ENTEXAMYEAR ");
        stb.append("           AND L004.NAMECD1    = 'L004' ");
        stb.append("           AND L004.NAMECD2    = BASE.TESTDIV ");
        stb.append("           AND L004.NAMESPARE1 = '" + _param._testDiv + "' ");
        stb.append(" WHERE ");
        stb.append("         BASE.ENTEXAMYEAR            = '" + _param._entExamYear + "' ");
        stb.append("     AND BASE.APPLICANTDIV           = '" + _param._applicantDiv + "' ");
        stb.append("     AND NOT (VALUE(L004.NAMESPARE3, '') = '2' AND L061.NAMESPARE1 IS NULL) "); // 音楽コース併願なしを除く
        stb.append(" GROUP BY ");
        stb.append("   GROUPING SETS ( ");
        stb.append("    (VALUE(L061.NAMESPARE2, BASE.DESIREDIV), ");
        stb.append("     L058.NAME1 ");
        stb.append("    ), ");
        stb.append("    ()) ");
        stb.append(" ORDER BY ");
        stb.append("     VALUE(L061.NAMESPARE2, BASE.DESIREDIV) ");

        return stb.toString();
    }

    private Map getScoreMap(final DB2UDB db2) {
        final Map retMap = (Map) new TreeMap();
        final String examCntSql = getScoreSql();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            log.debug(" sql =" + examCntSql);
            ps = db2.prepareStatement(examCntSql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String desireDiv = rs.getString("DESIREDIV");
                final String subclassCd = rs.getString("TESTSUBCLASSCD");
                final String totalScore = rs.getString("TOTAL_SCORE");
                final String totalCnt = rs.getString("TOTAL_CNT");

                if (!retMap.containsKey(desireDiv)) {
                	retMap.put(desireDiv, new ScoreTable());
                }
                ScoreTable scoreTable =(ScoreTable) retMap.get(desireDiv);
                scoreTable._scoreMap.put(subclassCd, totalScore);
                scoreTable._countMap.put(subclassCd, totalCnt);
            }

        } catch (SQLException ex) {
            log.error("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retMap;
    }

    private String getScoreSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     VALUE(L061.NAMESPARE2, BASE.DESIREDIV) AS DESIREDIV, ");
        stb.append("     SCORE.TESTSUBCLASSCD, ");
        stb.append("     SUM(SCORE.SCORE) AS TOTAL_SCORE, ");
        stb.append("     COUNT(SCORE.SCORE) AS TOTAL_CNT ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_APPLICANTBASE_DAT BASE ");
	    stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD33 ");
	    stb.append("            ON BD33.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
	    stb.append("           AND BD33.APPLICANTDIV = BASE.APPLICANTDIV ");
	    stb.append("           AND BD33.EXAMNO = BASE.EXAMNO ");
	    stb.append("           AND BD33.SEQ = '033' ");
	    stb.append("     LEFT JOIN V_NAME_MST L061 ");
	    stb.append("            ON L061.YEAR = BASE.ENTEXAMYEAR ");
	    stb.append("           AND L061.NAMECD1 = 'L061' ");
	    stb.append("           AND L061.NAMECD2 = BD33.REMARK3 ");
        stb.append("     INNER JOIN V_NAME_MST L004 ");
        stb.append("            ON L004.YEAR       = BASE.ENTEXAMYEAR ");
        stb.append("           AND L004.NAMECD1    = 'L004' ");
        stb.append("           AND L004.NAMECD2    = BASE.TESTDIV ");
        stb.append("           AND L004.NAMESPARE1 = '" + _param._testDiv + "' ");
        stb.append("     LEFT JOIN ENTEXAM_SCORE_DAT SCORE ");
        stb.append("          ON BASE.ENTEXAMYEAR      = SCORE.ENTEXAMYEAR ");
        stb.append("         AND BASE.APPLICANTDIV     = SCORE.APPLICANTDIV ");
        stb.append("         AND BASE.TESTDIV          = SCORE.TESTDIV ");
        stb.append("         AND SCORE.EXAM_TYPE       = '1' ");
        stb.append("         AND BASE.EXAMNO           = SCORE.RECEPTNO ");
        stb.append(" WHERE ");
        stb.append("         BASE.ENTEXAMYEAR            = '" + _param._entExamYear + "' ");
        stb.append("     AND BASE.APPLICANTDIV           = '" + _param._applicantDiv + "' ");
        stb.append("     AND VALUE(BASE.JUDGEMENT, '0') <> '3' ");
        stb.append("     AND NOT (VALUE(L004.NAMESPARE3, '') = '2' AND L061.NAMESPARE1 IS NULL) "); // 音楽コース併願なしを除く
        stb.append(" GROUP BY ");
        stb.append("     VALUE(L061.NAMESPARE2, BASE.DESIREDIV), ");
        stb.append("     SCORE.TESTSUBCLASSCD ");
        stb.append(" UNION ALL ");
        stb.append(" SELECT ");
        stb.append("     VALUE(L061.NAMESPARE2, BASE.DESIREDIV) AS DESIREDIV, ");
        stb.append("     'TOTAL4' AS TESTSUBCLASSCD, ");
        stb.append("     SUM(S2.TOTAL4) AS TOTAL_SCORE, ");
        stb.append("     COUNT(S2.TOTAL4) AS TOTAL_CNT ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_APPLICANTBASE_DAT BASE ");
	    stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD33 ");
	    stb.append("            ON BD33.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
	    stb.append("           AND BD33.APPLICANTDIV = BASE.APPLICANTDIV ");
	    stb.append("           AND BD33.EXAMNO = BASE.EXAMNO ");
	    stb.append("           AND BD33.SEQ = '033' ");
	    stb.append("     LEFT JOIN V_NAME_MST L061 ");
	    stb.append("            ON L061.YEAR = BASE.ENTEXAMYEAR ");
	    stb.append("           AND L061.NAMECD1 = 'L061' ");
	    stb.append("           AND L061.NAMECD2 = BD33.REMARK3 ");
        stb.append("     INNER JOIN V_NAME_MST L004 ");
        stb.append("            ON L004.YEAR       = BASE.ENTEXAMYEAR ");
        stb.append("           AND L004.NAMECD1    = 'L004' ");
        stb.append("           AND L004.NAMECD2    = BASE.TESTDIV ");
        stb.append("           AND L004.NAMESPARE1 = '" + _param._testDiv + "' ");
        stb.append("     INNER JOIN ENTEXAM_RECEPT_DAT S2 ");
        stb.append("            ON S2.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
        stb.append("            AND S2.APPLICANTDIV = BASE.APPLICANTDIV ");
        stb.append("            AND S2.TESTDIV = BASE.TESTDIV ");
        stb.append("            AND S2.EXAMNO = BASE.EXAMNO ");
        stb.append(" WHERE ");
        stb.append("         BASE.ENTEXAMYEAR            = '" + _param._entExamYear + "' ");
        stb.append("     AND BASE.APPLICANTDIV           = '" + _param._applicantDiv + "' ");
        stb.append("     AND VALUE(BASE.JUDGEMENT, '0') <> '3' ");
        stb.append("     AND NOT (VALUE(L004.NAMESPARE3, '') = '2' AND L061.NAMESPARE1 IS NULL) "); // 音楽コース併願なしを除く
        stb.append(" GROUP BY ");
        stb.append("     VALUE(L061.NAMESPARE2, BASE.DESIREDIV) ");
        stb.append(" ORDER BY ");
        stb.append("     DESIREDIV, ");
        stb.append("     TESTSUBCLASSCD ");
        return stb.toString();
    }

    private class ExamCntTable {
        private final String _desireName;
        private final int _totalCnt;
        private final int _kessekiCnt;
        private final int _ongakukaHeiganCnt;
        public ExamCntTable(
                final String desireName,
                final int totalCnt,
                final int kessekiCnt,
                final int ongakukaHeiganCnt
        ) {
            _desireName = desireName;
            _totalCnt = totalCnt;
            _kessekiCnt = kessekiCnt;
            _ongakukaHeiganCnt = ongakukaHeiganCnt;
        }
    }

    private class ScoreTable {
        private final Map _scoreMap;
        private final Map _countMap;
        public ScoreTable(
        ) {
            _scoreMap = new HashMap();
            _countMap = new HashMap();
        }

        public BigDecimal avg(final String subclasscd) {
        	final String totalScore = (String) _scoreMap.get(subclasscd);
        	final String totalCnt = (String) _countMap.get(subclasscd);
        	if (!NumberUtils.isNumber(totalScore) || !NumberUtils.isNumber(totalCnt)) {
        		return null;
        	}
            return new BigDecimal(totalScore).divide(new BigDecimal(totalCnt), 1, BigDecimal.ROUND_HALF_UP);
        }
    }

    private class ScoreCntTable {
        private int _totalCnt;
        private int _totalScore;
        public ScoreCntTable(
                final int totalCnt,
                final int totalScore
        ) {
            _totalCnt = totalCnt;
            _totalScore = totalScore;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 65795 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _loginYear;
        private final String _loginDate;
        private final String _applicantDiv;
        private final String _applicantdivName;
        private final String _testDiv;
        private final String _entExamYear;
        private final String _testdivName;
        private final List _subclassList;
        private final List _desireList;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _loginDate     = request.getParameter("LOGIN_DATE");
            _loginYear     = request.getParameter("LOGIN_YEAR");
            _entExamYear   = request.getParameter("ENTEXAMYEAR");
            _applicantDiv  = request.getParameter("APPLICANTDIV");
            _testDiv       = request.getParameter("TESTDIV");
            _desireList = getDesireList(db2);
            _subclassList = getSubclassList(db2);
            _applicantdivName = getNameMst(db2, "L003", _applicantDiv);
            _testdivName = getNameMst(db2, "L065", _testDiv);
        }

        private String getNameMst(final DB2UDB db2, final String namecd1, final String namecd2) {
            String retStr = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     NAME1 ");
                stb.append(" FROM ");
                stb.append("     V_NAME_MST ");
                stb.append(" WHERE ");
                stb.append("     YEAR    = '" + _entExamYear + "' ");
                stb.append("     AND NAMECD1 = '" + namecd1 + "' ");
                stb.append("     AND NAMECD2 = '" + namecd2 + "' ");
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    retStr = rs.getString("NAME1");
                }
            } catch (SQLException ex) {
                log.debug("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retStr;
        }

        private List getDesireList(final DB2UDB db2) {
            List retList = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     * ");
                stb.append(" FROM ");
                stb.append("     V_NAME_MST ");
                stb.append(" WHERE ");
                stb.append("     YEAR = '" + _entExamYear + "' ");
                stb.append("     AND NAMECD1 = 'L058' ");
                stb.append("     AND NAMECD2 LIKE '%0' ");
                stb.append(" ORDER BY ");
                stb.append("     NAMECD2 ");

                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String cd = rs.getString("NAMECD2");
                    final String name = rs.getString("NAME1");
                    final Desire subclass = new Desire(cd, name);
                    retList.add(subclass);
                }
            } catch (SQLException ex) {
                log.debug("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retList;
        }

        private List getSubclassList(final DB2UDB db2) {
            List retList = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     * ");
                stb.append(" FROM ");
                stb.append("     V_NAME_MST ");
                stb.append(" WHERE ");
                stb.append("     YEAR = '" + _entExamYear + "' ");
                stb.append("     AND NAMECD1 = 'L009' ");
                stb.append(" ORDER BY ");
                stb.append("     NAMECD2 ");

                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String cd = rs.getString("NAMECD2");
                    final String name = rs.getString("NAME1");
                    final Subclass subclass = new Subclass(cd, name);
                    retList.add(subclass);
                }
            } catch (SQLException ex) {
                log.debug("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retList;
        }
    }

    private class Subclass {
        private final String _cd;
        private final String _name;
        public Subclass(
            final String cd,
            final String name
        ) {
            _cd = cd;
            _name = name;
        }
    }

    private class Desire {
        private final String _cd;
        private final String _name;
        public Desire(
            final String cd,
            final String name
        ) {
            _cd = cd;
            _name = name;
        }
    }
}

// eof
