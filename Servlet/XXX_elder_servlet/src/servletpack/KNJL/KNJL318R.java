/**     プロジェクト/プロパティ/javaコード・スタイル/コード・テンプレート/パターン       ***/

/*
 * $Id: f5a9457d40bc00a8a3c99de10c418d83f787c2c3 $
 *
 * 作成日: 2019/01/09
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
import java.util.TreeSet;

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

public class KNJL318R {

    private static final Log log = LogFactory.getLog(KNJL318R.class);

    private boolean _hasData;
    private final String TOTAL_SUBCLASSCD = "99";
    private final String RIGHT_TOTAL_POSITION = "6";

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
    	final List consentList = new ArrayList();
    	if ("ALL".equals(_param._consent) && _param._isNaidakuPage) {
    		consentList.addAll(new TreeSet(_param._consentMap.keySet()));
    		consentList.remove("4");
    		consentList.add(null);
    	} else if ("4".equals(_param._consent)) {
    		consentList.add(null);
    	} else {
    		consentList.add(_param._consent);
    	}
    	
    	for (final Iterator it = consentList.iterator(); it.hasNext();) {
    		final String consent = (String) it.next();
    		svf.VrSetForm("KNJL318R.frm", 1);
    		svf.VrsOut("NENDO", KNJ_EditDate.gengou(db2, Integer.parseInt(_param._year)) + "年度");
    		svf.VrsOut("APPLICANTDIV", _param._applicantName);
    		svf.VrsOut("TESTDIV", _param._testName);
    		svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(db2, _param._loginDate));
    		
    		printLeftTable(db2, svf, consent);
    		printRightTable(db2, svf, consent);
    		
    		_hasData = true;
    		svf.VrEndPage();
    	}
    }

    private void printLeftTable(final DB2UDB db2, final Vrw32alp svf, final String consent) {
        final List leftList = getLeftList(db2, consent);
        int leftCnt = 1;
        for (Iterator iterator = leftList.iterator(); iterator.hasNext();) {
            final LeftTable leftTable = (LeftTable) iterator.next();
            svf.VrsOutn("COUNT", leftCnt++, leftTable._boyCnt);
            svf.VrsOutn("COUNT", leftCnt++, leftTable._girlCnt);
            svf.VrsOutn("COUNT", leftCnt++, leftTable._allCnt);
        }
    }

    private List getLeftList(final DB2UDB db2, final String consent) {
        final List retList = new ArrayList();
        final String shiganSql = getLeftSql(consent, 1);
        retList.add(getLeftTable(db2, shiganSql));
        final String jukenSql = getLeftSql(consent, 2);
        retList.add(getLeftTable(db2, jukenSql));
        final String goukakuSql = getLeftSql(consent, 3);
        retList.add(getLeftTable(db2, goukakuSql));
        return retList;
    }

    private LeftTable getLeftTable(final DB2UDB db2, final String sql) {
        LeftTable leftTable = new LeftTable("0", "0", "0");
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String boyCnt = rs.getString("BOY_CNT");
                final String girlCnt = rs.getString("GIRL_CNT");
                final String allCnt = rs.getString("ALL_CNT");
                leftTable = new LeftTable(boyCnt, girlCnt, allCnt);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return leftTable;
    }

    private String getLeftSql(final String consent, final int flg) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     SUM(CASE WHEN BASE.SEX = '1' ");
        stb.append("              THEN 1 ");
        stb.append("              ELSE 0 ");
        stb.append("         END) AS BOY_CNT, ");
        stb.append("     SUM(CASE WHEN BASE.SEX = '2' ");
        stb.append("              THEN 1 ");
        stb.append("              ELSE 0 ");
        stb.append("         END) AS GIRL_CNT, ");
        stb.append("     COUNT(*) AS ALL_CNT ");
        stb.append(" FROM ");
        if (flg == 1) {
            stb.append("     ENTEXAM_APPLICANTBASE_DAT BASE ");
            stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT BUN010 ON BASE.ENTEXAMYEAR = BUN010.ENTEXAMYEAR ");
            stb.append("           AND BASE.APPLICANTDIV = BUN010.APPLICANTDIV ");
            stb.append("           AND BASE.EXAMNO = BUN010.EXAMNO ");
            stb.append("           AND BUN010.SEQ = '010' ");
            stb.append("           AND BUN010.REMARK" + _param._testdiv + " IS NOT NULL ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT BUN014 ON BUN014.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
            stb.append("           AND BUN014.APPLICANTDIV = BASE.APPLICANTDIV ");
            stb.append("           AND BUN014.EXAMNO       = BASE.EXAMNO ");
            stb.append("           AND BUN014.SEQ          = '014' ");
            stb.append(" WHERE ");
            stb.append("     BASE.ENTEXAMYEAR = '" + _param._year + "' ");
            stb.append("     AND BASE.APPLICANTDIV = '" + _param._applicantdiv + "' ");
        } else if (flg == 2 || flg == 3) {
        	stb.append("     ENTEXAM_RECEPT_DAT RECEPT ");
        	stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE ON RECEPT.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
        	stb.append("           AND RECEPT.APPLICANTDIV = BASE.APPLICANTDIV ");
        	stb.append("           AND RECEPT.EXAMNO = BASE.EXAMNO ");
        	stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT BUN014 ON BUN014.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
        	stb.append("           AND BUN014.APPLICANTDIV = BASE.APPLICANTDIV ");
        	stb.append("           AND BUN014.EXAMNO       = BASE.EXAMNO ");
        	stb.append("           AND BUN014.SEQ          = '014' ");
        	stb.append(" WHERE ");
        	stb.append("     RECEPT.ENTEXAMYEAR = '" + _param._year + "' ");
        	stb.append("     AND RECEPT.APPLICANTDIV = '" + _param._applicantdiv + "' ");
        	stb.append("     AND RECEPT.TESTDIV = '" + _param._testdiv + "' ");
        	stb.append("     AND RECEPT.EXAM_TYPE = '1' ");
        	if (flg == 2) {
        		stb.append("     AND RECEPT.ATTEND_ALL_FLG = '1' ");
        	} else if (flg == 3) {
                stb.append("     AND RECEPT.JUDGEDIV IN ( ");
                stb.append("        SELECT ");
                stb.append("            NAMECD2 ");
                stb.append("        FROM ");
                stb.append("            V_NAME_MST ");
                stb.append("        WHERE ");
                stb.append("            YEAR = '" + _param._year + "' ");
                stb.append("            AND NAMECD1 = 'L013' ");
                stb.append("            AND NAMESPARE1 = '1' ");
                stb.append("        ) ");
        	}
        }
        if (!"ALL".equals(consent)) {
        	//指定内諾が全て以外
        	if("1".equals(_param._testdiv)) {
        		if (null == consent) {
            		stb.append("     AND BUN014.REMARK1 IS NULL ");
        		} else {
            		stb.append("     AND BUN014.REMARK1 = '" + consent + "'  ");
        		}
        	}else {
        		if (null == consent) {
        			stb.append("     AND BUN014.REMARK2 IS NULL ");
        		} else {
        			stb.append("     AND BUN014.REMARK2 = '" + consent + "'  ");
        		}
        	}
        }
        return stb.toString();
    }

    private void printRightTable(final DB2UDB db2, final Vrw32alp svf, final String consent) {
        final Map rightMap = getRightMap(db2, consent);
        int rightCnt = 1;
        final String setKaten = _param._isKasan ? "加点あり" : "加点なし";
        final String setNaidaku;
        if ("ALL".equals(consent)) {
            setNaidaku = "すべて";
        } else {
            setNaidaku = null == consent ? "内諾なし" : "内諾" + _param._consentMap.get(consent).toString();
        }
        final String txt;
        if ("1".equals(_param._target)) {
        	txt = "受験者";
        } else {
        	txt = "合格者";
        }
        svf.VrsOut("PASS_REMARK", txt + "（" + setKaten + "、" + setNaidaku + "）の最高点、最低点、平均点");
        for (Iterator itSubclass = _param._subclassList.iterator(); itSubclass.hasNext();) {
            final Subclass subclass = (Subclass) itSubclass.next();
            final RightTable rightTable = (RightTable) rightMap.get(subclass._cd);
            final String setField = KNJ_EditEdit.getMS932ByteLength(subclass._name) > 6 ? "2" : "";
            svf.VrsOutn("SUBCLASSNAME" + setField, rightCnt, subclass._name);
            svf.VrsOutn("AVERAGE1", rightCnt, null == rightTable._boyAvg ? null : rightTable._boyAvg.toString());
            svf.VrsOutn("AVERAGE2", rightCnt, null == rightTable._girlAvg ? null : rightTable._girlAvg.toString());
            svf.VrsOutn("AVERAGE3", rightCnt, null == rightTable._totalAvg ? null : rightTable._totalAvg.toString());
            svf.VrsOutn("HIGH", rightCnt, String.valueOf(rightTable._maxScore));
            svf.VrsOutn("LOW", rightCnt, String.valueOf(rightTable._minScore));
            rightCnt++;
        }
        //合計欄
        final RightTable rightTableTotal = (RightTable) rightMap.get(TOTAL_SUBCLASSCD);
        rightCnt = Integer.parseInt(RIGHT_TOTAL_POSITION);
        svf.VrsOutn("SUBCLASSNAME", rightCnt, rightTableTotal._subclassName);
        svf.VrsOutn("AVERAGE1", rightCnt, null == rightTableTotal._boyAvg ? null : rightTableTotal._boyAvg.toString());
        svf.VrsOutn("AVERAGE2", rightCnt, null == rightTableTotal._girlAvg ? null : rightTableTotal._girlAvg.toString());
        svf.VrsOutn("AVERAGE3", rightCnt, null == rightTableTotal._totalAvg ? null : rightTableTotal._totalAvg.toString());
        svf.VrsOutn("HIGH", rightCnt, String.valueOf(rightTableTotal._maxScore));
        svf.VrsOutn("LOW", rightCnt, String.valueOf(rightTableTotal._minScore));
    }

    private Map getRightMap(final DB2UDB db2, final String consent) {
        final Map retMap = (Map) new TreeMap();
        for (Iterator itSubclass = _param._subclassList.iterator(); itSubclass.hasNext();) {
            final Subclass subclass = (Subclass) itSubclass.next();
            final String scoreSql = getRightTableScoreSql(subclass._cd, consent, 1);
            retMap.put(subclass._cd, getRightTable(db2, scoreSql, subclass._name));
        }
        final String totalSql = getRightTableScoreSql(null, consent, 2);
        retMap.put(TOTAL_SUBCLASSCD, getRightTable(db2, totalSql, "合計"));

        return retMap;
    }

    private RightTable getRightTable(final DB2UDB db2, final String sql, final String subclassName) {
        RightTable rightTable = new RightTable(subclassName, null, null, null, 0, 0);
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final int boyScore = rs.getInt("BOY_SCORE");
                final int boyCnt = rs.getInt("BOY_CNT");
                final int girlScore = rs.getInt("GIRL_SCORE");
                final int girlCnt = rs.getInt("GIRL_CNT");
                final int totalScore = rs.getInt("TOTAL_SCORE");
                final int allCnt = rs.getInt("ALL_CNT");
                final int maxScore = rs.getInt("MAX_SCORE");
                final int minScore = rs.getInt("MIN_SCORE");
                final BigDecimal boyVal = boyCnt == 0 ? null : new BigDecimal(boyScore).divide(new BigDecimal(boyCnt), 1, BigDecimal.ROUND_HALF_UP);
                final BigDecimal girlVal = girlCnt == 0 ? null : new BigDecimal(girlScore).divide(new BigDecimal(girlCnt), 1, BigDecimal.ROUND_HALF_UP);
                final BigDecimal totalVal = allCnt == 0 ? null : new BigDecimal(totalScore).divide(new BigDecimal(allCnt), 1, BigDecimal.ROUND_HALF_UP);
                rightTable = new RightTable(subclassName, boyVal, girlVal, totalVal, maxScore, minScore);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return rightTable;
    }

    private String getRightTableScoreSql(final String subclassCd, final String consent, final int flg) {
        final StringBuffer stb = new StringBuffer();
        if (flg == 1) {
            stb.append(" SELECT ");
            stb.append("     SUM(CASE WHEN BASE.SEX = '1' ");
            stb.append("              THEN SCORE.SCORE ");
            stb.append("              ELSE 0 ");
            stb.append("         END) AS BOY_SCORE, ");
            stb.append("     SUM(CASE WHEN BASE.SEX = '1' ");
            stb.append("              THEN 1 ");
            stb.append("              ELSE 0 ");
            stb.append("         END) AS BOY_CNT, ");
            stb.append("     SUM(CASE WHEN BASE.SEX = '2' ");
            stb.append("              THEN SCORE.SCORE ");
            stb.append("              ELSE 0 ");
            stb.append("         END) AS GIRL_SCORE, ");
            stb.append("     SUM(CASE WHEN BASE.SEX = '2' ");
            stb.append("              THEN 1 ");
            stb.append("              ELSE 0 ");
            stb.append("         END) AS GIRL_CNT, ");
            stb.append("     SUM(SCORE.SCORE) AS TOTAL_SCORE, ");
            stb.append("     COUNT(*) AS ALL_CNT, ");
            stb.append("     VALUE(MAX(SCORE.SCORE), 0) AS MAX_SCORE, ");
            stb.append("     VALUE(MIN(SCORE.SCORE), 0) AS MIN_SCORE ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_RECEPT_DAT RECEPT ");
            stb.append("     LEFT JOIN ENTEXAM_SCORE_DAT SCORE ON RECEPT.ENTEXAMYEAR = SCORE.ENTEXAMYEAR ");
            stb.append("           AND RECEPT.APPLICANTDIV = SCORE.APPLICANTDIV ");
            stb.append("           AND RECEPT.TESTDIV = SCORE.TESTDIV ");
            stb.append("           AND RECEPT.EXAM_TYPE = SCORE.EXAM_TYPE ");
            stb.append("           AND RECEPT.RECEPTNO = SCORE.RECEPTNO ");
            stb.append("           AND SCORE.TESTSUBCLASSCD = '" + subclassCd + "' ");
        } else {
            String scoreField = "2";
            if (_param._isKasan) {
                if ("1".equals(_param._outkeisya)) {
                    scoreField = "3";
                } else {
                    scoreField = "1";
                }
            } else {
                if ("1".equals(_param._outkeisya)) {
                    scoreField = "4";
                } else {
                    scoreField = "2";
                }
            }
            stb.append(" SELECT ");
            stb.append("     SUM(CASE WHEN BASE.SEX = '1' ");
            stb.append("              THEN RECEPT.TOTAL" + scoreField + " ");
            stb.append("              ELSE 0 ");
            stb.append("         END) AS BOY_SCORE, ");
            stb.append("     SUM(CASE WHEN BASE.SEX = '1' ");
            stb.append("              THEN 1 ");
            stb.append("              ELSE 0 ");
            stb.append("         END) AS BOY_CNT, ");
            stb.append("     SUM(CASE WHEN BASE.SEX = '2' ");
            stb.append("              THEN RECEPT.TOTAL" + scoreField + " ");
            stb.append("              ELSE 0 ");
            stb.append("         END) AS GIRL_SCORE, ");
            stb.append("     SUM(CASE WHEN BASE.SEX = '2' ");
            stb.append("              THEN 1 ");
            stb.append("              ELSE 0 ");
            stb.append("         END) AS GIRL_CNT, ");
            stb.append("     SUM(RECEPT.TOTAL" + scoreField + ") AS TOTAL_SCORE, ");
            stb.append("     COUNT(*) AS ALL_CNT, ");
            stb.append("     VALUE(MAX(RECEPT.TOTAL" + scoreField + "), 0) AS MAX_SCORE, ");
            stb.append("     VALUE(MIN(RECEPT.TOTAL" + scoreField + "), 0) AS MIN_SCORE ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_RECEPT_DAT RECEPT ");
        }
        stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE ON RECEPT.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
        stb.append("           AND RECEPT.APPLICANTDIV = BASE.APPLICANTDIV ");
        stb.append("           AND RECEPT.EXAMNO = BASE.EXAMNO ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT BUN014 ON BUN014.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
        stb.append("           AND BUN014.APPLICANTDIV = BASE.APPLICANTDIV  ");
        stb.append("           AND BUN014.EXAMNO       = BASE.EXAMNO ");
        stb.append("           AND BUN014.SEQ          = '014' ");
        stb.append(" WHERE ");
        stb.append("     RECEPT.ENTEXAMYEAR = '" + _param._year + "' ");
        stb.append("     AND RECEPT.APPLICANTDIV = '" + _param._applicantdiv + "' ");
        stb.append("     AND RECEPT.TESTDIV = '" + _param._testdiv + "' ");
        stb.append("     AND RECEPT.EXAM_TYPE = '1' ");
        if ("2".equals(_param._target)) { // 合格者のみ
        	stb.append("     AND RECEPT.JUDGEDIV IN ( ");
        	stb.append("        SELECT ");
        	stb.append("            NAMECD2 ");
        	stb.append("        FROM ");
        	stb.append("            V_NAME_MST ");
        	stb.append("        WHERE ");
        	stb.append("            YEAR = '" + _param._year + "' ");
        	stb.append("            AND NAMECD1 = 'L013' ");
        	stb.append("            AND NAMESPARE1 = '1' ");
        	stb.append("        ) ");
        } else if ("1".equals(_param._target)) { // 受験者
        	stb.append("     AND VALUE(RECEPT.JUDGEDIV, '') NOT IN ('3', '4') ");
        }
        if (!"ALL".equals(consent)) {
        	//指定内諾が全て以外
        	if("1".equals(_param._testdiv)) {
        		if (null == consent) {
            		stb.append("     AND BUN014.REMARK1 IS NULL ");
        		} else {
            		stb.append("     AND BUN014.REMARK1 = '" + consent + "'  ");
        		}
        	}else {
        		if (null == consent) {
            		stb.append("     AND BUN014.REMARK2 IS NULL ");
        		} else {
            		stb.append("     AND BUN014.REMARK2 = '" + consent + "'  ");
        		}
        	}
        }
        return stb.toString();
    }

    private class LeftTable {
        private final String _boyCnt;
        private final String _girlCnt;
        private final String _allCnt;
        public LeftTable(
                final String boyCnt,
                final String girlCnt,
                final String allCnt
        ) {
            _boyCnt = boyCnt;
            _girlCnt = girlCnt;
            _allCnt = allCnt;
        }
    }

    private class RightTable {
        private final String _subclassName;
        private final BigDecimal _boyAvg;
        private final BigDecimal _girlAvg;
        private final BigDecimal _totalAvg;
        private final int _maxScore;
        private final int _minScore;
        public RightTable(
                final String subclassName,
                final BigDecimal boyAvg,
                final BigDecimal girlAvg,
                final BigDecimal totalAvg,
                final int maxScore,
                final int minScore
        ) {
            _subclassName = subclassName;
            _boyAvg = boyAvg;
            _girlAvg = girlAvg;
            _totalAvg = totalAvg;
            _maxScore = maxScore;
            _minScore = minScore;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 71700 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _applicantdiv;
        private final String _testdiv;
        final String _consent;      // 内諾
        final String _outkeisya;    // 傾斜配点出力   1:する 2:しない
        final boolean _isKasan;    // 加算点含む
        final String _target;    // 対象者 1:受験者 2:合格者
        final boolean _isNaidakuPage;    // 内諾区分ごと改ページ（_consentが"ALL"の場合のみ）
        private final String _year;
        private final String _semester;
        private final String _loginDate;
        private final String _applicantName;
        private final String _testName;
        private final List _subclassList;
        private final Map _consentMap;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _applicantdiv = request.getParameter("APPLICANTDIV");
            _testdiv = request.getParameter("TESTDIV");
            _consent = request.getParameter("CONSENT");
            _outkeisya = request.getParameter("OUTKEISYA");
            _target = request.getParameter("TARGET");
            _isNaidakuPage = "on".equals(request.getParameter("NAIDAKU_PAGE"));
            _isKasan = "on".equals(request.getParameter("INC_KASAN"));
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _loginDate = request.getParameter("LOGIN_DATE");
            _applicantName = getNameMst(db2, "L003", _applicantdiv);
            _testName = getNameMst(db2, "L024", _testdiv);
            _subclassList = getSubclassList(db2);
            _consentMap = getMapNameMst(db2, "NAME1", "L064");
        }

        private String getNameMst(final DB2UDB db2, final String nameCd1, final String nameCd2) {
            String retStr = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = '" + nameCd1 + "' AND NAMECD2 = '" + nameCd2 + "'");
                rs = ps.executeQuery();
                if (rs.next() && null != rs.getString("NAME1")) {
                  retStr = rs.getString("NAME1");
                }
            } catch (SQLException e) {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retStr;
        }

        private Map getMapNameMst(final DB2UDB db2, final String field, final String namecd1) {
            Map rtn = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT NAMECD2, " + field + " FROM NAME_MST WHERE NAMECD1 = '" + namecd1 + "' ");
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn.put(rs.getString("NAMECD2"), rs.getString(field));
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return rtn;
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
                stb.append("     YEAR = '" + _year + "' ");
                stb.append("     AND NAMECD1 = 'L009' ");
                stb.append("     AND NAME2 IS NOT NULL ");
                stb.append("     AND NAMESPARE1 = '" + _testdiv + "' ");
                stb.append(" ORDER BY ");
                stb.append("     NAMECD2 ");

                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String cd = rs.getString("NAMECD2");
                    final String name = rs.getString("NAME2");
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
}

// eof
