/*
 * $Id: 5bf7182cfc7fffb7fe50590a3bb3b7563fe4aaef $
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KnjDbUtils;

public class KNJL306E {

    private static final Log log = LogFactory.getLog(KNJL306E.class);

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

    private static List getMappedList(final Map map, final Object key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new ArrayList());
        }
        return (List) map.get(key1);
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException {

    	final String sql = sql(_param);
    	if (_param._isOutputDebug) {
    		log.info(" sql = " + sql);
    	}

    	final List dataList = KnjDbUtils.query(db2, sql);

        final Map firstRow = KnjDbUtils.firstRow(dataList);
        final BigDecimal avg = sishagonyu(!NumberUtils.isNumber(KnjDbUtils.getString(firstRow, "AVG")) ? null : new BigDecimal(KnjDbUtils.getString(firstRow, "AVG")));
        final BigDecimal stddev = sishagonyu(!NumberUtils.isNumber(KnjDbUtils.getString(firstRow, "STDDEV")) ? null : new BigDecimal(KnjDbUtils.getString(firstRow, "STDDEV")));
        final Map scoreDataListMap = groupBy(dataList, "TOTAL4");

        final int maxScore = 300;
        final int minScore = 0;
        final int formMaxLine = 50;
        final int maxPage = maxScore / formMaxLine;

		final String title = KNJ_EditDate.getAutoFormatYear(db2, Integer.parseInt(_param._entexamyear)) + "年度　" + _param._testdivName + "　得点度数分布表";
		final String printDate = KNJ_EditDate.getAutoFormatDate(db2, _param._loginDate);

        int ruikei = 0; // 累計
        int suisenRuikei = 0; // 推薦累計
        for (int pagei = 0; pagei < maxPage; pagei++) {

            svf.VrSetForm("KNJL306E.frm", 4);

			svf.VrsOut("TITLE", title);
			svf.VrsOut("DATE", printDate);

            svf.VrsOut("PAGE", String.valueOf(pagei + 1)); // ページ
            svf.VrsOut("TOTAL_PAGE", String.valueOf(maxPage)); // 総ページ数
            svf.VrsOut("SUBTITLE", "得点範囲：" + String.valueOf(minScore) + "点 ～ " + String.valueOf(maxScore) + "点　　受験者数：" + String.valueOf(dataList.size()) + "　　平均点：" + (null == avg ? "" : avg.toString()) + "　　標準偏差：" + (null == stddev ? "" : stddev.toString()));

            final int startScore = maxScore - pagei * formMaxLine;
            final int endScore = pagei == maxPage - 1 ? minScore : maxScore - (pagei + 1) * formMaxLine + 1;

            for (int score = startScore; score >= endScore; score--) {
            	final List scoreDataList = getMappedList(scoreDataListMap, String.valueOf(score));

            	final int ninzu = scoreDataList.size();
            	ruikei += ninzu;
            	int suisen = 0;
            	for (final Iterator sit = scoreDataList.iterator(); sit.hasNext();) {
            		final Map row = (Map) sit.next();
            		if ("1".equals(KnjDbUtils.getString(row, "IS_SUISEN"))) {
            			suisen += 1;
            		}
            	}
            	suisenRuikei += suisen;
            	final String deviation = calcDeviation(score, avg, stddev);

                svf.VrsOut("SCORE", String.valueOf(score)); // 得点
				svf.VrsOut("COUNT", String.valueOf(ninzu)); // 人数
                svf.VrsOut("COUNT_ACCUM", String.valueOf(ruikei)); // 累計
                svf.VrsOut("COUNT_RECOM", String.valueOf(suisen)); // 推薦
                svf.VrsOut("COUNT_RECOM_ACCUM", String.valueOf(suisenRuikei)); // 推薦累計
                svf.VrsOut("DEVIATION", deviation); // 偏差値

            	svf.VrEndRecord();
            }
            _hasData = true;
        }
    }

	/**
     * 偏差値算出<br>
     * 偏差値 = 50 + 10 * (得点 - 平均点) / 標準偏差
     * @param score 得点
     * @param avg 平均点
     * @param stddev 標準偏差
     * @return 偏差値
     */
    private String calcDeviation(final int score, final BigDecimal avg, final BigDecimal stddev) {
    	if (null == avg || null == stddev) {
    		return null;
    	}
    	BigDecimal deviation = new BigDecimal(50);
    	if (stddev.doubleValue() > 0.0) {
    		deviation = deviation.add((new BigDecimal(10).multiply(new BigDecimal(score).subtract(avg))).divide(stddev, 10, BigDecimal.ROUND_HALF_UP));
    	}
    	return sishagonyu(deviation).toString();
	}

	private BigDecimal sishagonyu(final BigDecimal bd) {
    	// 3桁目を四捨五入して2桁まで表示
		return null == bd ? null : bd.setScale(2, BigDecimal.ROUND_HALF_UP);
	}

	private Map groupBy(final List dataList, final String field) {
    	final Map map = new HashMap();
    	for (final Iterator it = dataList.iterator(); it.hasNext();) {
    		final Map data = (Map) it.next();
    		final String val = KnjDbUtils.getString(data, field);
    		if (null == val) {
    			log.warn("no value in " + field + ", " + data);
    			continue;
    		}
    		getMappedList(map, val).add(data);
    	}
		return map;
	}

	public String sql(final Param param) {
	    final StringBuffer stb = new StringBuffer();
	    stb.append(" WITH SCORES AS ( ");
	    stb.append(" SELECT  ");
	    stb.append("    T1.EXAMNO ");
	    stb.append("  , L1.TOTAL4 ");
	    stb.append("  , MAX(CASE WHEN L004_SUISEN.NAMECD1 IS NOT NULL THEN '1' END) AS IS_SUISEN ");
	    stb.append(" FROM ENTEXAM_APPLICANTBASE_DAT T1 ");
	    stb.append(" LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT L2 ON ");
	    stb.append("      L2.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
	    stb.append("    AND L2.APPLICANTDIV = T1.APPLICANTDIV ");
	    stb.append("    AND L2.EXAMNO = T1.EXAMNO ");
	    stb.append("    AND L2.SEQ = '002' ");
	    stb.append(" LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT OTHERDETAIL ON ");
	    stb.append("      OTHERDETAIL.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
	    stb.append("    AND OTHERDETAIL.APPLICANTDIV = T1.APPLICANTDIV ");
	    stb.append("    AND OTHERDETAIL.EXAMNO <> T1.EXAMNO ");
	    stb.append("    AND OTHERDETAIL.SEQ = L2.SEQ ");
	    stb.append("    AND OTHERDETAIL.REMARK1 = L2.REMARK1 ");
	    stb.append(" LEFT JOIN ENTEXAM_APPLICANTBASE_DAT OTHERBASE ON ");
	    stb.append("      OTHERBASE.ENTEXAMYEAR = OTHERDETAIL.ENTEXAMYEAR ");
	    stb.append("    AND OTHERBASE.APPLICANTDIV = OTHERDETAIL.APPLICANTDIV ");
	    stb.append("    AND OTHERBASE.EXAMNO = OTHERDETAIL.EXAMNO ");
	    stb.append(" LEFT JOIN NAME_MST L004_SUISEN ON ");
	    stb.append("      L004_SUISEN.NAMECD1 = 'L004' ");
	    stb.append("    AND L004_SUISEN.NAMECD2 = OTHERBASE.TESTDIV ");
	    stb.append("    AND L004_SUISEN.NAMESPARE1 = '1' ");
	    stb.append(" INNER JOIN ENTEXAM_RECEPT_DAT L1 ON ");
	    stb.append("      L1.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
	    stb.append("    AND L1.APPLICANTDIV = T1.APPLICANTDIV ");
	    stb.append("    AND L1.EXAMNO = T1.EXAMNO ");
	    stb.append(" INNER JOIN V_NAME_MST L004_IN ON L004_IN.YEAR       = T1.ENTEXAMYEAR ");
	    stb.append("                              AND L004_IN.NAMECD1    = 'L004' ");
	    stb.append("                              AND L004_IN.NAMECD2    = T1.TESTDIV ");
	    stb.append("                              AND L004_IN.NAMESPARE1 = '" + param._testDiv + "' ");
	    stb.append(" LEFT JOIN V_NAME_MST L004 ON ");
	    stb.append("      L004.YEAR = T1.ENTEXAMYEAR ");
	    stb.append("  AND L004.NAMECD1 = 'L004' ");
	    stb.append("  AND L004.NAMECD2 = T1.TESTDIV ");
	    stb.append(" LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD33 ON ");
	    stb.append("      BD33.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
	    stb.append("  AND BD33.APPLICANTDIV = T1.APPLICANTDIV ");
	    stb.append("  AND BD33.EXAMNO = T1.EXAMNO ");
	    stb.append("  AND BD33.SEQ = '033' ");
	    stb.append(" LEFT JOIN V_NAME_MST L061 ON ");
	    stb.append("      L061.YEAR = T1.ENTEXAMYEAR ");
	    stb.append("  AND L061.NAMECD1 = 'L061' ");
	    stb.append("  AND L061.NAMECD2 = BD33.REMARK3 ");
	    stb.append(" WHERE ");
	    stb.append("      T1.ENTEXAMYEAR = '" + param._entexamyear + "' ");
	    stb.append("      AND T1.APPLICANTDIV = '" + param._applicantDiv + "' ");
	    stb.append("      AND VALUE(T1.JUDGEMENT, '') <> '3' "); // 欠席者を除く
	    stb.append("      AND L1.TOTAL4 IS NOT NULL ");
	    stb.append("      AND NOT (VALUE(L004.NAMESPARE3, '') = '2' AND VALUE(L061.NAMESPARE1, '') <> '1') "); // 2科目合計を除く
	    stb.append(" GROUP BY ");
	    stb.append("   T1.EXAMNO ");
	    stb.append("  ,L1.TOTAL4 ");
	    stb.append(" ), STATICS AS ( ");
	    stb.append("    SELECT ");
	    stb.append("       AVG(CAST(TOTAL4 AS DECIMAL(10, 5))) AS AVG ");
	    stb.append("     , STDDEV(TOTAL4) AS STDDEV ");
	    stb.append("    FROM SCORES ");
	    stb.append(" ) ");
	    stb.append(" SELECT ");
	    stb.append("     T1.EXAMNO ");
	    stb.append("   , T1.TOTAL4 ");
	    stb.append("   , T1.IS_SUISEN ");
	    stb.append("   , T2.AVG ");
	    stb.append("   , T2.STDDEV ");
	    stb.append("   , CASE WHEN T2.STDDEV > 0 THEN 10 * (T1.TOTAL4 - T2.AVG) / T2.STDDEV ELSE 0 END + 50 AS DEVIATION ");
	    stb.append(" FROM SCORES T1, STATICS T2 ");
	    stb.append(" ORDER BY ");
	    stb.append("   T1.TOTAL4 DESC, T1.EXAMNO ");
	    return stb.toString();

	}


    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 64251 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _applicantDiv;
        final String _testDiv;
        final String _entexamyear;
        final String _loginYear;
        final String _loginSemester;
        final String _loginDate;
        final String _prgid;
        final String _applicantName;
        final String _testdivName;

        final boolean _isOutputDebug;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _loginYear = request.getParameter("LOGIN_YEAR");
            _loginSemester = request.getParameter("LOGIN_SEMESTER");
            _loginDate = request.getParameter("LOGIN_DATE");
            _prgid = request.getParameter("PRGID");
            _applicantName = StringUtils.defaultString(getNameMst(db2, "NAME1", "L003", _applicantDiv));
            _testdivName = "2".equals(_testDiv) ? "Ａ日程": "Ｂ日程";
            _isOutputDebug = "1".equals(getDbPrginfoProperties(db2, "outputDebug"));
        }

        private static String getDbPrginfoProperties(final DB2UDB db2, final String propName) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJL306E' AND NAME = '" + propName + "' "));
        }

        private String getNameMst(final DB2UDB db2, final String field, final String namecd1, final String namecd2) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT " + field + " FROM NAME_MST WHERE NAMECD1 = '" + namecd1 + "' AND NAMECD2 = '" + namecd2 + "' "));
        }

    }
}

// eof
