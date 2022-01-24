/**     プロジェクト/プロパティ/javaコード・スタイル/コード・テンプレート/パターン       ***/

/*
 * 作成日: 2020/03/04
 * 作成者: yogi
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.math.BigDecimal;
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

import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KnjDbUtils;

public class KNJD626D {

    private static final Log log = LogFactory.getLog(KNJD626D.class);

    /** 3教科科目コード */
    private static final String ALL3 = "333333";
    /** 5教科科目コード */
    private static final String ALL5 = "555555";
    /** 7教科科目コード */
    private static final String ALL7 = "777777";
    /** 9教科科目コード */
    private static final String ALL9 = "999999";
    /** 特殊科目コードA */
    private static final String ALL9A = "99999A";
    /** 特殊科目コードB */
    private static final String ALL9B = "99999B";
    /** 教科評定平均検索用コード */
    private static final String ALLZ = "ZZZZZZ";

    private static final String SEMEALL = "9";

    private static final int MAXCOL = 26;

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
        svf.VrSetForm("KNJD626D.frm", 1);
        Map gh_Map = getGHMapInfo(db2, "", null);
        getGHMapInfo(db2, "HISTGRAM", gh_Map);
        GradeHrCls histTotal = null;
        if (gh_Map.size() > 0) {
            histTotal = getHistTotalInfo(db2, gh_Map);
        }

        final int hrMax = 16;
        int hrCnt = 0;
        boolean ratePrtFlg = false;
        log.info("gh_Map = " + gh_Map.keySet());
        for (Iterator ite = gh_Map.keySet().iterator(); ite.hasNext();) {
            final String getKey = (String)ite.next();
            final GradeHrCls hrClSObj = (GradeHrCls)gh_Map.get(getKey);
            log.info("hr = " + hrClSObj._grade + "-" + hrClSObj._hrClass  + " histgram = " +hrClSObj._histgramMap.keySet()  );
            hrCnt++;
            if (hrCnt > hrMax) {
                svf.VrEndPage();
                hrCnt = 1;
            }
            if (hrCnt == 1) {
                setExceptSpread(db2, svf, hrClSObj);
            }

            //表1
            svf.VrsOutn("HR_NAME1", hrCnt, hrClSObj._hrAbbv); //組名称１
               for (int schidx = 0;schidx < _param._attendNoList.size();schidx++) {
                final String getSubKey = (String)_param._attendNoList.get(schidx);
                svf.VrsOutn("NO", schidx+1, String.valueOf(Integer.parseInt(getSubKey)));
                if (hrClSObj._schregMap.containsKey(getSubKey)) {
                    final Student student = (Student)hrClSObj._schregMap.get(getSubKey);
                    svf.VrsOutn("RATE"+hrCnt, schidx+1, student._valuation);
                    if(student._valMark != null) {
                        if(student._valMark.length() == 1) {
                            char c = student._valMark.charAt(0);
                            if ( 'A' <= c && c <= 'Z') {
                                c = (char)(c - 'A' + 'Ａ'); //全角変換
                            }
                            svf.VrsOutn("VALUE"+hrCnt, schidx+1, String.valueOf(c)); //評定平均
                        }
                    }
                }
            }
               printTable2(svf, hrCnt, hrClSObj, ratePrtFlg);
            ratePrtFlg = true;
            _hasData = true;
        }
        if (histTotal != null) {
               printTable2(svf, 17, histTotal, ratePrtFlg);
        }
        svf.VrEndPage();
    }

    private void printTable2(final Vrw32alp svf, final int hrCnt, final GradeHrCls hrClSObj, final boolean ratePrtFlg) {
        //表2
        svf.VrsOutn("HR_NAME2", hrCnt, hrClSObj._hrAbbv); //組名称１
        totalCalc totalSumWk = new totalCalc();
        int rankCnt = 1;
        for (Iterator its = _param._assessList.iterator();its.hasNext();) {
            AssessDat assInfo = (AssessDat)its.next();
            BigDecimal assHigh = new BigDecimal(assInfo._assesshigh);
            BigDecimal assLow = new BigDecimal(assInfo._assesslow);
            BigDecimal assMovVal = new BigDecimal(0.0).setScale(1, BigDecimal.ROUND_HALF_UP);
            int prtCnt = 1;
            int assCnt = assHigh.subtract(assLow).multiply(new BigDecimal(10.0)).intValue();
            totalCalc sumWk = new totalCalc();
            for (;assCnt >= 0;assCnt--) {
                //final String assVal = assHigh.subtract(assMovVal.multiply(new BigDecimal(prtCnt-1))).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                final String assVal = assHigh.subtract(assMovVal).toString();
                if (!ratePrtFlg) {
                    svf.VrsOutn("RATE_NUM" + rankCnt, prtCnt, assVal);
                }
                if (hrClSObj._histgramMap.containsKey(assVal)) {
                    HistgramDat prtwk = (HistgramDat)hrClSObj._histgramMap.get(assVal);
                    totalSumWk.add(prtwk._cnt);
                    sumWk.add(prtwk._cnt);
                    svf.VrsOutn("NUM" + rankCnt + "_" + hrCnt, prtCnt, prtwk._cnt);  //人数
                    svf.VrsOutn("SUBTOTAL" + rankCnt + "_"+hrCnt, prtCnt, String.valueOf(totalSumWk._totalVal));  //累計
                }
                assMovVal = assMovVal.add(new BigDecimal(0.1)).setScale(1, BigDecimal.ROUND_HALF_UP);
                prtCnt++;
            }
            if(sumWk._cnt > 0) {
                svf.VrsOut("TOTAL" + rankCnt + "_" + hrCnt, String.valueOf(sumWk._totalVal));  //人数
            }
            rankCnt++;
        }
        svf.VrsOut("ENROLL" + hrCnt, String.valueOf(hrClSObj._schregMap.size()));
    }
//    private List reBuildSchMap(final Map srcMap, final int prtMax) {
//    	final List retList = new ArrayList();
//    	Map wkMap = new LinkedMap();
//    	int cnt = 0;
//    	for (Iterator ite = srcMap.keySet().iterator();ite.hasNext();) {
//    		final String kStr = (String)ite.next();
//    		final Student mvObj = (Student)srcMap.get(kStr);
//    		if (cnt >= prtMax) {
//    			retList.add(wkMap);
//    	    	wkMap = new LinkedMap();
//    			cnt = 0;
//    		}
//    		wkMap.put(kStr, mvObj);
//    	}
//    	if (wkMap.size() > 0) {
//			retList.add(wkMap);
//    	}
//    	return retList;
//    }
//
//    private List getClsNPrtIdx(final String classAbbv, final int subMapsize) {
//    	List retList = null;  //nullなら、1文字出力か、出力文字列が空文字orNULL
//    	if (subMapsize >= 1 && classAbbv.length() <= 2 * subMapsize) {
//    		//文字の出力開始位置の求め方
//    		//もし、入る文字数<出力文字数なら、半分のフォントサイズ(入る文字数は倍)にする。
//    		int useSize = 0;
//    		if (subMapsize < classAbbv.length()) {
//    			useSize = subMapsize * 2;
//    		} else {
//    			useSize = subMapsize;
//    		}
//    		//文字列の開始位置は、入る文字数の中間点(端数切り上げ)と出力文字の中間点(端数は調整。%の計算部分がそれ)の差を求めて、そこに1加算する。
//    		//計算上、端数は出ずに整数となる。
//    		BigDecimal calcWakuSize = new BigDecimal(Math.ceil(useSize/2.0));
//    		int calcwk = classAbbv.length() - ((useSize+1) % 2);
//    		BigDecimal calcStrLen = new BigDecimal(Math.floor(calcwk/2.0));
//    		int strtPt = calcWakuSize.subtract(calcStrLen).intValue();
//    		for (int cnt = 0;cnt < classAbbv.length();cnt++) {
//    			if (cnt < useSize) {
//    				if (retList == null ) retList = new ArrayList();
//    			    retList.add(String.valueOf(strtPt+cnt));
//    			}
//    		}
//    	}
//    	return retList;
//    }

    private void setExceptSpread(final DB2UDB db2, final Vrw32alp svf, final GradeHrCls hrClSObj) {
        //タイトル
        final String nendo = _param._year + "年度";
        svf.VrsOut("TITLE", nendo + " 評定平均値一覧表");

        //学年
        svf.VrsOut("GRADE", hrClSObj._gradeName);

        //日付
        svf.VrsOut("DATE", _param._ctrlDate);
    }

    private Map getGHMapInfo(final DB2UDB db2, final String getType, final Map setMap) throws SQLException {
        final String useSemester = "9".equals(_param._semester) ? _param._lastSemester : _param._semester;
        Map retMap = new LinkedMap();
        GradeHrCls ghCls = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH SYUUYAKU_WK_SUB AS ( ");  // 留年を加味したデータにする(留年した年のデータを抜くため、過年度学年の最後の年(最大の年)で集約)
            stb.append(" SELECT ");
            stb.append("     MAX(TW1.YEAR) AS YEAR, ");
            stb.append("     TW1.SEMESTER, ");
            stb.append("     TW2.GRADE_CD, ");
            stb.append("     TW1.SCHREGNO ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT TW1 ");
            stb.append("     LEFT JOIN SCHREG_REGD_GDAT TW2 ");
            stb.append("       ON TW2.YEAR = TW1.YEAR ");
            stb.append("      AND TW2.GRADE = TW1.GRADE ");
            stb.append(" WHERE ");
            stb.append("    TW1.YEAR <= '" + _param._year + "' ");
            stb.append(" GROUP BY ");
            stb.append("     TW1.SEMESTER, ");
            stb.append("     TW2.GRADE_CD, ");
            stb.append("     TW1.SCHREGNO ");
            stb.append(" ), SYUUYAKU_WK AS ( ");  // 上記データから、過年度分は最終学期、当年度分は指定学期のデータだけを抽出
            stb.append(" SELECT ");
            stb.append("    TK1.YEAR, ");
            stb.append("    MAX(TK1.SEMESTER) AS SEMESTER, ");
            stb.append("    TK1.GRADE_CD, ");
            stb.append("    TK1.SCHREGNO ");
            stb.append(" FROM ");
            stb.append("   SYUUYAKU_WK_SUB TK1 ");
            stb.append(" WHERE ");
            stb.append("    TK1.YEAR < '" + _param._year + "' ");
            stb.append(" GROUP BY ");
            stb.append("     TK1.YEAR, ");
            stb.append("     TK1.GRADE_CD, ");
            stb.append("     TK1.SCHREGNO ");
            stb.append(" UNION ALL ");
            stb.append(" SELECT ");
            stb.append("     TK2.YEAR, ");
            stb.append("     TK2.SEMESTER, ");
            stb.append("     TK2.GRADE_CD, ");
            stb.append("     TK2.SCHREGNO ");
            stb.append(" FROM ");
            stb.append("   SYUUYAKU_WK_SUB TK2 ");
            stb.append(" WHERE ");
            stb.append("    TK2.YEAR = '" + _param._year + "' ");
            stb.append("    AND TK2.SEMESTER = '" + useSemester + "' "); //(単学期の可能性からすると前学期の可能性もあるが)駒澤の科目は全て通年との事なので、指定学期で取得。
            stb.append(" ), SYUUYAKU_SCHREG AS ( ");  // SCHREG情報として、集約。
            stb.append(" SELECT ");
            stb.append("   T1.*, ");
            stb.append("   T2.SCHOOL_KIND ");
            stb.append(" FROM ");
            stb.append("   SCHREG_REGD_DAT T1 ");
            stb.append("   INNER JOIN SYUUYAKU_WK TW1 ");
            stb.append("     ON TW1.YEAR = T1.YEAR ");
            stb.append("    AND TW1.SEMESTER = T1.SEMESTER");
            stb.append("    AND TW1.SCHREGNO = T1.SCHREGNO ");
            stb.append("   LEFT JOIN SCHREG_REGD_GDAT T2 ");
            stb.append("     ON T2.YEAR = T1.YEAR ");
            stb.append("    AND T2.GRADE = T1.GRADE ");
            stb.append(" ), VALUATION_BASE AS ( ");
            stb.append(" select ");
            stb.append("   T2.YEAR, ");
            stb.append("   T1.GRADE, ");
            stb.append("   T2.SCHREGNO, ");
            stb.append("   T2.CLASSCD, ");
            stb.append("   T2.SCHOOL_KIND, ");
            stb.append("   T2.CURRICULUM_CD, ");
            stb.append("   T2.SUBCLASSCD, ");
            stb.append("   NULL AS PROV_FLG, ");
            stb.append("   T2.VALUATION AS SCORE ");
            stb.append(" FROM ");
            stb.append("   SYUUYAKU_SCHREG T1 ");
            stb.append("   LEFT JOIN SCHREG_STUDYREC_DAT T2 ");
            stb.append("     ON T2.YEAR = T1.YEAR ");
            stb.append("    AND T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("    AND T2.ANNUAL = T1.ANNUAL ");
            stb.append("    AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append(" WHERE ");
            stb.append("   T1.YEAR < '" + _param._year + "' ");
            stb.append("   AND T2.CLASSCD < 90 ");
            stb.append("   AND T2.YEAR IS NOT NULL ");
            stb.append(" UNION ALL ");
            // 当年度データ
            stb.append(" select ");
            stb.append("   T2.YEAR, ");
            stb.append("   T1.GRADE, ");
            stb.append("   T2.SCHREGNO, ");
            stb.append("   T2.CLASSCD, ");
            stb.append("   T2.SCHOOL_KIND, ");
            stb.append("   T2.CURRICULUM_CD, ");
            stb.append("   T2.SUBCLASSCD, ");
            if ("1".equals(_param._useProvFlg)) {
                stb.append("    P1.PROV_FLG, ");
                stb.append("    CASE WHEN T2.SCORE = 1 AND P1.PROV_FLG = '1' THEN 2 ELSE T2.SCORE END AS SCORE ");
            } else {
                stb.append("   NULL AS PROV_FLG, ");
                stb.append("   T2.SCORE ");
            }
            stb.append(" FROM ");
            stb.append("   SYUUYAKU_SCHREG T1 ");
            stb.append("   LEFT JOIN RECORD_RANK_SDIV_DAT T2 ");
            stb.append("     ON T2.YEAR = T1.YEAR ");
            stb.append("    AND T2.SEMESTER = '" + _param._semester + "' ");
            stb.append("    AND T2.SCHREGNO = T1.SCHREGNO ");
            if ("1".equals(_param._useProvFlg)) {
                stb.append("   LEFT JOIN RECORD_PROV_FLG_DAT P1 ");
                stb.append("     ON P1.YEAR = T2.YEAR ");
                stb.append("    AND P1.CLASSCD = T2.CLASSCD ");
                stb.append("    AND P1.SCHOOL_KIND = T2.SCHOOL_KIND ");
                stb.append("    AND P1.CURRICULUM_CD = T2.CURRICULUM_CD ");
                stb.append("    AND P1.SUBCLASSCD = T2.SUBCLASSCD ");
                stb.append("    AND P1.SCHREGNO = T2.SCHREGNO ");
            }
            stb.append(" WHERE ");
            stb.append("   T1.YEAR = '" + _param._year + "' ");
            stb.append("   AND T2.CLASSCD < 90 ");
            stb.append("   AND T1.SEMESTER = '" + useSemester + "' ");
            stb.append("   AND T2.TESTKINDCD || T2.TESTITEMCD || T2.SCORE_DIV = '" + _param._testcd + "' ");
            stb.append("   AND T2.YEAR IS NOT NULL ");
            stb.append("   AND T2.SUBCLASSCD NOT IN ('333333', '777777', '99999A', '99999B') ");
            stb.append("   AND T2.SUBCLASSCD NOT IN ('" + ALL9 + "', '" + ALL5 + "') ");
            stb.append(" ), KEKKA AS (");
            // 評定データ(過年度含む)
            stb.append(" SELECT ");
            stb.append("   TF1.YEAR, ");
            stb.append("   TF1.GRADE, ");
            stb.append("   TF1.SCHREGNO, ");
            stb.append("   TF1.CLASSCD, ");
            stb.append("   TF1.SCHOOL_KIND, ");
            stb.append("   TF1.CURRICULUM_CD, ");
            stb.append("   TF1.SUBCLASSCD, ");
            stb.append("   TF1.PROV_FLG, ");
            stb.append("   DECIMAL(CAST(TF1.SCORE AS double), 5, 1) AS SCORE ");
            stb.append(" FROM ");
            stb.append("   VALUATION_BASE TF1 ");
            stb.append(" ), KEKKA2 AS (");
            stb.append(" SELECT ");
            stb.append("   T1.SCHREGNO, ");
            stb.append("   CASE WHEN DECIMAL(ROUND(AVG(CAST(T1.SCORE AS double))*10.0, 0)/10.0, 5,1) < 1.9 THEN 1.9 ELSE DECIMAL(ROUND(AVG(CAST(T1.SCORE AS double))*10.0, 0)/10.0, 5,1) END AS VALUATION"); //下限値1.9にする
            stb.append(" FROM ");
            stb.append("   KEKKA T1 ");
            stb.append(" GROUP BY ");
            stb.append("   SCHREGNO ");

            stb.append(" ), SCHREG_BASE AS ( ");
            stb.append(" SELECT DISTINCT ");
            stb.append("  REGD.SCHREGNO, ");
            stb.append("  REGD.GRADE, ");
            stb.append("  GDAT.SCHOOL_KIND, ");
            stb.append("  A023.ABBV1 AS SKNAME, ");
            stb.append("  GDAT.GRADE_CD, ");
            stb.append("  GDAT.GRADE_NAME1, ");
            stb.append("  GDAT.GRADE_NAME2, ");
            stb.append("  REGD.HR_CLASS, ");
            stb.append("  HDAT.HR_NAME, ");
            stb.append("  HDAT.HR_CLASS_NAME1, ");
            stb.append("  SM.STAFFNAME, ");
            stb.append("  REGD.ATTENDNO, ");
            stb.append("  BASE.NAME, ");
            stb.append("  REGD.COURSECD, ");
            stb.append("  REGD.MAJORCD, ");
            stb.append("  REGD.COURSECODE, ");
            stb.append("  MAJR.MAJORNAME, ");
            stb.append("  CCODE.COURSECODENAME ");
            stb.append(" FROM ");
            stb.append("    SCHREG_REGD_DAT REGD ");
            stb.append("    INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ");
            stb.append("    INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = REGD.YEAR ");
            stb.append("          AND GDAT.GRADE = REGD.GRADE ");
            stb.append("    INNER JOIN SCHREG_REGD_HDAT HDAT ON HDAT.YEAR = REGD.YEAR ");
            stb.append("          AND HDAT.SEMESTER = REGD.SEMESTER ");
            stb.append("          AND HDAT.GRADE = REGD.GRADE ");
            stb.append("          AND HDAT.HR_CLASS = REGD.HR_CLASS ");
            stb.append("    LEFT JOIN MAJOR_MST MAJR ON REGD.COURSECD = MAJR.COURSECD ");
            stb.append("         AND REGD.MAJORCD = MAJR.MAJORCD ");
            stb.append("    LEFT JOIN COURSECODE_MST CCODE ON REGD.COURSECODE = CCODE.COURSECODE ");
            stb.append("    LEFT JOIN SEMESTER_MST SEM_MST ON SEM_MST.SEMESTER = REGD.SEMESTER AND SEM_MST.YEAR = REGD.YEAR ");
            stb.append("    LEFT JOIN STAFF_MST SM ON SM.STAFFCD = HDAT.TR_CD1 ");
            stb.append("    LEFT JOIN NAME_MST A023 ON A023.NAMECD1 = 'A023' AND A023.NAME1 = GDAT.SCHOOL_KIND ");
            stb.append(" WHERE ");
            stb.append("  REGD.YEAR = '" + _param._year + "' ");
            stb.append("  AND REGD.SEMESTER = '" + useSemester + "' ");
            stb.append("  AND REGD.GRADE = '" + _param._grade + "' ");
            // 在籍チェック:転学(2)・退学(3)者は除外
            stb.append("  AND ( BASE.GRD_DIV IS NULL OR BASE.GRD_DIV NOT IN('2','3') ) ");

            if (getType.equals("HISTGRAM")) {
                stb.append(" ), HISTGRAM_BASE AS ( ");
            } else {
                stb.append(" ) ");
            }
            stb.append(" SELECT ");
            stb.append("   T1.*, ");
            stb.append("   VDW.VALUATION, ");
            stb.append("   ASSMST.ASSESSMARK AS VAL_MARK ");
            stb.append(" FROM ");
            stb.append("   SCHREG_BASE T1 ");
            stb.append("   LEFT JOIN KEKKA2 VDW ON VDW.SCHREGNO = T1.SCHREGNO ");
            stb.append("   LEFT JOIN ASSESS_MST ASSMST ON ASSMST.ASSESSCD = '4' AND VALUE(VDW.VALUATION, 0.0) BETWEEN VALUE(ASSMST.ASSESSLOW, 0) AND VALUE(ASSMST.ASSESSHIGH, 0) ");

            if (!getType.equals("HISTGRAM")) {
                stb.append(" ORDER BY ");
                stb.append("    T1.GRADE, ");
                stb.append("    T1.HR_CLASS, ");
                stb.append("    T1.ATTENDNO  ");
            } else {
                stb.append(" ) ");
                stb.append(" SELECT ");
                stb.append("   T1.GRADE, ");
                stb.append("   T1.HR_CLASS, ");
                stb.append("   T1.HR_NAME, ");
                stb.append("   T1.HR_CLASS_NAME1, ");
                stb.append("   T1.VAL_MARK, ");
                stb.append("   T1.VALUATION, ");
                stb.append("   COUNT(*) AS CNT ");
                stb.append(" FROM ");
                stb.append("   HISTGRAM_BASE T1 ");
                stb.append(" WHERE ");
                stb.append("   T1.VALUATION IS NOT NULL ");
                stb.append(" GROUP BY ");
                stb.append("   T1.GRADE, ");
                stb.append("   T1.HR_CLASS, ");
                stb.append("   T1.HR_NAME, ");
                stb.append("   T1.HR_CLASS_NAME1, ");
                stb.append("   T1.VAL_MARK, ");
                stb.append("   T1.VALUATION ");
                stb.append(" ORDER BY ");
                stb.append("   T1.GRADE, ");
                stb.append("   T1.HR_CLASS, ");
                stb.append("   T1.VAL_MARK, ");
                stb.append("   T1.VALUATION DESC ");

            }

            log.debug(" regd sql = " + stb.toString());
            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();
            while (rs.next()) {
                if (getType.equals("HISTGRAM")) {
                    if (setMap != null) {
                        //基本的には、下のvaluationを設定後に実施する事。でないと、年組データが作成されていないので登録できない。
                        final String grade = rs.getString("GRADE");
                        final String hrClass = rs.getString("HR_CLASS");
                        final String hrName = rs.getString("HR_NAME");
                        final String hrAbbv = rs.getString("HR_CLASS_NAME1"); //組名称1
                        final String valMark = rs.getString("VAL_MARK");
                        final String valuation = rs.getString("VALUATION");
                        final String cnt = rs.getString("CNT");
                        final HistgramDat hist = new HistgramDat(grade, hrClass, hrName, hrAbbv, valMark, valuation, cnt);
                        final String rmKey = grade + "-" + hrClass;
                        if (setMap.containsKey(rmKey)) {
                            ghCls = (GradeHrCls)setMap.get(rmKey);
                            ghCls._histgramMap.put(valuation, hist);
                            log.info(grade + hrClass + " valuation = " + valuation + " cnt = " + cnt);
                        }
                    }
                } else {
                    final String schregno = rs.getString("SCHREGNO");
                    final String grade = rs.getString("GRADE");
                    final String schoolKind = rs.getString("SCHOOL_KIND");
                    final String skName = rs.getString("SKNAME");
                    final String gradeCd = rs.getString("GRADE_CD");
                    final String gradeName = rs.getString("GRADE_NAME1");
                    final String gradeName2 = rs.getString("GRADE_NAME2");
                    final String hrclass = rs.getString("HR_CLASS");
                    final String hrName = rs.getString("HR_NAME");
                    final String hrAbbv = rs.getString("HR_CLASS_NAME1"); //組名称1
                    final String staffName = rs.getString("STAFFNAME");
                    final String attendno = rs.getString("ATTENDNO");
                    final String name = rs.getString("NAME");
                    final String coursecd = rs.getString("COURSECD");
                    final String majorcd = rs.getString("MAJORCD");
                    final String coursecode = rs.getString("COURSECODE");
                    final String majorName = rs.getString("MAJORNAME");
                    final String courseCodeName = rs.getString("COURSECODENAME");
                    final String valuation = rs.getString("VALUATION");
                    final String valMark = rs.getString("VAL_MARK");

                    final Student student = new Student(
                            schregno,
                            grade,
                            hrclass,
                            hrName,
                            hrAbbv,
                            attendno,
                            name,
                            coursecd,
                            majorcd,
                            coursecode,
                            majorName,
                            courseCodeName,
                            valuation,
                            valMark
                    );
                    final String rmKey = grade + "-" + hrclass;
                    if (retMap.containsKey(rmKey)) {
                        ghCls = (GradeHrCls)retMap.get(rmKey);
                    } else {
                        ghCls = new GradeHrCls(grade, schoolKind, skName, gradeCd, gradeName, gradeName2, hrclass, hrName, hrAbbv, staffName, new LinkedMap());
                        //ghCls.setSubclassInfo(db2, grade + hrclass);
                        retMap.put(rmKey, ghCls);
                    }
                    //student.setSubclassValuation(db2, ghCls);
                    if (!ghCls._schregMap.containsKey(attendno)) {
                        ghCls._schregMap.put(attendno, student);
                    }
                }

            }
        } catch (final SQLException e) {
            log.error("生徒の基本情報取得でエラー", e);
            throw e;
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
        return retMap;
    }

    private GradeHrCls getHistTotalInfo(final DB2UDB db2, final Map setMap) throws SQLException {
        final String useSemester = "9".equals(_param._semester) ? _param._lastSemester : _param._semester;

        GradeHrCls ghCls = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH SYUUYAKU_WK_SUB AS ( ");  // 留年を加味したデータにする(留年した年のデータを抜くため、過年度学年の最後の年(最大の年)で集約)
            stb.append(" SELECT ");
            stb.append("     MAX(TW1.YEAR) AS YEAR, ");
            stb.append("     TW1.SEMESTER, ");
            stb.append("     TW2.GRADE_CD, ");
            stb.append("     TW1.SCHREGNO ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT TW1 ");
            stb.append("     LEFT JOIN SCHREG_REGD_GDAT TW2 ");
            stb.append("       ON TW2.YEAR = TW1.YEAR ");
            stb.append("      AND TW2.GRADE = TW1.GRADE ");
            stb.append(" WHERE ");
            stb.append("    TW1.YEAR <= '" + _param._year + "' ");
            stb.append(" GROUP BY ");
            stb.append("     TW1.SEMESTER, ");
            stb.append("     TW2.GRADE_CD, ");
            stb.append("     TW1.SCHREGNO ");
            stb.append(" ), SYUUYAKU_WK AS ( ");  // 上記データから、過年度分は最終学期、当年度分は指定学期のデータだけを抽出
            stb.append(" SELECT ");
            stb.append("    TK1.YEAR, ");
            stb.append("    MAX(TK1.SEMESTER) AS SEMESTER, ");
            stb.append("    TK1.GRADE_CD, ");
            stb.append("    TK1.SCHREGNO ");
            stb.append(" FROM ");
            stb.append("   SYUUYAKU_WK_SUB TK1 ");
            stb.append(" WHERE ");
            stb.append("    TK1.YEAR < '" + _param._year + "' ");
            stb.append(" GROUP BY ");
            stb.append("     TK1.YEAR, ");
            stb.append("     TK1.GRADE_CD, ");
            stb.append("     TK1.SCHREGNO ");
            stb.append(" UNION ALL ");
            stb.append(" SELECT ");
            stb.append("     TK2.YEAR, ");
            stb.append("     TK2.SEMESTER, ");
            stb.append("     TK2.GRADE_CD, ");
            stb.append("     TK2.SCHREGNO ");
            stb.append(" FROM ");
            stb.append("   SYUUYAKU_WK_SUB TK2 ");
            stb.append(" WHERE ");
            stb.append("    TK2.YEAR = '" + _param._year + "' ");
            stb.append("    AND TK2.SEMESTER = '" + useSemester + "' "); //(単学期の可能性からすると前学期の可能性もあるが)駒澤の科目は全て通年との事なので、指定学期で取得。
            stb.append(" ), SYUUYAKU_SCHREG AS ( ");  // SCHREG情報として、集約。
            stb.append(" SELECT ");
            stb.append("   T1.*, ");
            stb.append("   T2.SCHOOL_KIND ");
            stb.append(" FROM ");
            stb.append("   SCHREG_REGD_DAT T1 ");
            stb.append("   INNER JOIN SYUUYAKU_WK TW1 ");
            stb.append("     ON TW1.YEAR = T1.YEAR ");
            stb.append("    AND TW1.SEMESTER = T1.SEMESTER");
            stb.append("    AND TW1.SCHREGNO = T1.SCHREGNO ");
            stb.append("   LEFT JOIN SCHREG_REGD_GDAT T2 ");
            stb.append("     ON T2.YEAR = T1.YEAR ");
            stb.append("    AND T2.GRADE = T1.GRADE ");
            stb.append(" ), VALUATION_BASE AS ( ");
            stb.append(" SELECT ");
            stb.append("   T1.* ");
            stb.append(" FROM ");
            stb.append("   RECORD_RANK_SDIV_DAT T1 ");
            stb.append("   INNER JOIN SYUUYAKU_SCHREG T2 ");
            stb.append("     ON T2.YEAR = T1.YEAR ");
            stb.append("    AND T2.SCHREGNO = T1.SCHREGNO ");
            stb.append(" WHERE ");
            stb.append("   T1.YEAR < '" + _param._year + "' ");
            stb.append("   AND T1.SEMESTER = '9' ");
            stb.append("   AND T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '990009' ");
            stb.append("   AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '99-H-99-999999' ");
            stb.append(" UNION ALL ");
            stb.append(" SELECT ");
            stb.append("   T1.* ");
            stb.append(" FROM ");
            stb.append("   RECORD_RANK_SDIV_DAT T1 ");
            stb.append("   INNER JOIN SYUUYAKU_SCHREG T2 ");
            stb.append("     ON T2.YEAR = T1.YEAR ");
            stb.append("    AND T2.SCHREGNO = T1.SCHREGNO ");
            stb.append(" WHERE ");
            stb.append("   T1.YEAR = '" + _param._year + "' ");
            stb.append("   AND T1.SEMESTER = '" + _param._semester + "' ");
            stb.append("   AND T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '" + _param._testcd + "' ");
            stb.append("   AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '99-H-99-999999' ");
            stb.append(" ), VALUATION_DATA_WK AS ( ");
            stb.append(" SELECT ");
            stb.append("   SCHREGNO, ");
            stb.append("   CASE WHEN DECIMAL(ROUND(AVG(CAST(AVG AS double))*10.0, 0)/10.0, 5,1) < 1.9 THEN 1.9 ELSE DECIMAL(ROUND(AVG(CAST(AVG AS double))*10.0, 0)/10.0, 5,1) END AS VALUATION"); //下限値1.9にする。
            stb.append(" FROM ");
            stb.append("   VALUATION_BASE ");
            stb.append(" GROUP BY ");
            stb.append("   SCHREGNO ");
            stb.append(" ), SCHREG_BASE AS ( ");
            stb.append(" SELECT DISTINCT ");
            stb.append("  REGD.SCHREGNO, ");
            stb.append("  REGD.GRADE, ");
            stb.append("  GDAT.SCHOOL_KIND, ");
            stb.append("  A023.ABBV1 AS SKNAME, ");
            stb.append("  GDAT.GRADE_CD, ");
            stb.append("  GDAT.GRADE_NAME1, ");
            stb.append("  GDAT.GRADE_NAME2, ");
            stb.append("  REGD.HR_CLASS, ");
            stb.append("  HDAT.HR_NAME, ");
            stb.append("  HDAT.HR_NAMEABBV, ");
            stb.append("  SM.STAFFNAME, ");
            stb.append("  REGD.ATTENDNO, ");
            stb.append("  BASE.NAME, ");
            stb.append("  REGD.COURSECD, ");
            stb.append("  REGD.MAJORCD, ");
            stb.append("  REGD.COURSECODE, ");
            stb.append("  MAJR.MAJORNAME, ");
            stb.append("  CCODE.COURSECODENAME ");
            stb.append(" FROM ");
            stb.append("    SCHREG_REGD_DAT REGD ");
            stb.append("    INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ");
            stb.append("    INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = REGD.YEAR ");
            stb.append("          AND GDAT.GRADE = REGD.GRADE ");
            stb.append("    INNER JOIN SCHREG_REGD_HDAT HDAT ON HDAT.YEAR = REGD.YEAR ");
            stb.append("          AND HDAT.SEMESTER = REGD.SEMESTER ");
            stb.append("          AND HDAT.GRADE = REGD.GRADE ");
            stb.append("          AND HDAT.HR_CLASS = REGD.HR_CLASS ");
            stb.append("    LEFT JOIN MAJOR_MST MAJR ON REGD.COURSECD = MAJR.COURSECD ");
            stb.append("         AND REGD.MAJORCD = MAJR.MAJORCD ");
            stb.append("    LEFT JOIN COURSECODE_MST CCODE ON REGD.COURSECODE = CCODE.COURSECODE ");
            stb.append("    LEFT JOIN SEMESTER_MST SEM_MST ON SEM_MST.SEMESTER = REGD.SEMESTER AND SEM_MST.YEAR = REGD.YEAR ");
            stb.append("    LEFT JOIN STAFF_MST SM ON SM.STAFFCD = HDAT.TR_CD1 ");
            stb.append("    LEFT JOIN NAME_MST A023 ON A023.NAMECD1 = 'A023' AND A023.NAME1 = GDAT.SCHOOL_KIND ");
            stb.append(" WHERE ");
            stb.append("  REGD.YEAR = '" + _param._year + "' ");
            stb.append("  AND REGD.SEMESTER = '" + useSemester + "' ");
            stb.append("  AND REGD.GRADE = '" + _param._grade + "' ");
            // 在籍チェック:転学(2)・退学(3)者は除外
            stb.append("  AND ( BASE.GRD_DIV IS NULL OR BASE.GRD_DIV NOT IN('2','3') ) ");
            stb.append(" ), HISTGRAM_BASE AS ( ");
            stb.append(" SELECT ");
            stb.append("   T1.*, ");
            stb.append("   VDW.VALUATION, ");
            stb.append("   ASSMST.ASSESSMARK AS VAL_MARK ");
            stb.append(" FROM ");
            stb.append("   SCHREG_BASE T1 ");
            stb.append("   LEFT JOIN VALUATION_DATA_WK VDW ON VDW.SCHREGNO = T1.SCHREGNO ");
            stb.append("   LEFT JOIN ASSESS_MST ASSMST ON ASSMST.ASSESSCD = '4' AND VALUE(VDW.VALUATION, 0.0) BETWEEN VALUE(ASSMST.ASSESSLOW, 0) AND VALUE(ASSMST.ASSESSHIGH, 0) ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("   '99' AS GRADE, ");
            stb.append("   '999' AS HR_CLASS, ");
            stb.append("   '学年' AS HR_NAME, ");
            stb.append("   '学年' AS HR_NAMEABBV, ");
            stb.append("   T1.VAL_MARK, ");
            stb.append("   T1.VALUATION, ");
            stb.append("   COUNT(*) AS CNT ");
            stb.append(" FROM ");
            stb.append("   HISTGRAM_BASE T1 ");
            stb.append(" WHERE ");
            stb.append("   T1.VALUATION IS NOT NULL ");
            stb.append(" GROUP BY ");
            stb.append("   T1.VAL_MARK, ");
            stb.append("   T1.VALUATION ");
            stb.append(" ORDER BY ");
            stb.append("   T1.VAL_MARK, ");
            stb.append("   T1.VALUATION DESC ");

            //log.debug(" regd sql = " + stb.toString());
            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();
            while (rs.next()) {
                //基本的には、下のvaluationを設定後に実施する事。でないと、年組データが作成されていないので登録できない。
                final String grade = rs.getString("GRADE");
                final String hrClass = rs.getString("HR_CLASS");
                final String hrName = rs.getString("HR_NAME");
                final String hrAbbv = rs.getString("HR_NAMEABBV");
                final String valMark = rs.getString("VAL_MARK");
                final String valuation = rs.getString("VALUATION");
                final String cnt = rs.getString("CNT");
                final HistgramDat hist = new HistgramDat(grade, hrClass, hrName, hrAbbv, valMark, valuation, cnt);
                if (ghCls == null) {
                    ghCls = new GradeHrCls(grade, "H", "", "", "", "", hrClass, hrName, hrAbbv, "", new LinkedMap());
                }
                log.info("Total valuation = " + valuation + " cnt = " + cnt);
                ghCls._histgramMap.put(valuation, hist);
            }
            if (ghCls != null) {
                for (Iterator ith = setMap.keySet().iterator();ith.hasNext();) {
                    final String kStr = (String)ith.next();
                    final GradeHrCls cpobj = (GradeHrCls)setMap.get(kStr);
                    for (Iterator itNo = cpobj._schregMap.keySet().iterator();itNo.hasNext();) {
                        final String kkStr = (String)itNo.next();
                        final Student student = (Student)cpobj._schregMap.get(kkStr);
                        ghCls._schregMap.put(student._hrClass + itNo, student);
                    }
                }
            }
        } catch (final SQLException e) {
            log.error("生徒の基本情報取得でエラー", e);
            throw e;
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
        return ghCls;
    }

    private class GradeHrCls {
        final String _grade;
        final String _schoolKind;
        final String _skName;
        final String _gradeCd;
        final String _gradeName;
        final String _gradeName2;
        final String _hrClass;
        final String _hrName;
        final String _hrAbbv;
        final String _staffName;
        final Map _schregMap;
//        final Map _subclsInfoMap;
        final Map _histgramMap;
        GradeHrCls(
                final String grade,
                final String schoolKind,
                final String skName,
                final String gradeCd,
                final String gradeName,
                final String gradeName2,
                final String hrClass,
                final String hrName,
                final String hrAbbv,
                final String staffName,
                final Map schregMap
                ) {
            _grade = grade;
            _schoolKind = schoolKind;
            _skName = skName;
            _gradeCd = gradeCd;
            _gradeName = gradeName;
            _gradeName2 = gradeName2;
            _hrClass = hrClass;
            _hrName = hrName;
            _hrAbbv = hrAbbv;
            _staffName = staffName;
            _schregMap = schregMap;
//            _subclsInfoMap = new LinkedMap();
            _histgramMap = new LinkedMap();
        }

//        public void setSubclassInfo(final DB2UDB db2, final String ghrClass) throws SQLException {
//        	final String useSemester = "9".equals(_param._semester) ? _param._lastSemester : _param._semester;
//        	PreparedStatement ps = null;
//            ResultSet rs = null;
//            final String pskey = "setSubclassInfo";
//            if (!_param._psBuffer.containsKey(pskey)) {
//            	final StringBuffer stb = new StringBuffer();
//            	stb.append(" WITH SYUUYAKU_WK_SUB AS ( ");  // 留年を加味したデータにする(留年した年のデータを抜く)
//            	stb.append(" SELECT ");
//            	stb.append("     MAX(TW1.YEAR) AS YEAR, ");
//            	stb.append("     TW1.SEMESTER, ");
//            	stb.append("     TW2.SCHOOL_KIND, ");
//            	stb.append("     TW2.GRADE_CD, ");
//            	stb.append("     TW1.SCHREGNO ");
//            	stb.append(" FROM ");
//            	stb.append("     SCHREG_REGD_DAT TW1 ");
//            	stb.append("     LEFT JOIN SCHREG_REGD_GDAT TW2 ");
//            	stb.append("       ON TW2.YEAR = TW1.YEAR ");
//            	stb.append("      AND TW2.GRADE = TW1.GRADE ");
//            	stb.append(" WHERE ");
//            	stb.append("    TW1.YEAR <= '" + _param._year + "' ");
//            	stb.append(" GROUP BY ");
//            	stb.append("     TW2.SCHOOL_KIND, ");
//            	stb.append("     TW2.GRADE_CD, ");
//            	stb.append("     TW1.SEMESTER, ");
//            	stb.append("     TW1.SCHREGNO ");
//            	stb.append(" ), SYUUYAKU_WK AS ( ");  // 上記データから、紐づけるデータが重複しないようにするために、過年度分は最終学期、当年度分は指定学期までのデータだけを抽出
//            	stb.append(" SELECT ");
//            	stb.append("    TK1.YEAR, ");
//            	stb.append("    MAX(TK1.SEMESTER) AS SEMESTER, ");
//            	stb.append("    TK1.GRADE_CD, ");
//            	stb.append("    TK1.SCHREGNO ");
//            	stb.append(" FROM ");
//            	stb.append("   SYUUYAKU_WK_SUB TK1 ");
//            	stb.append(" WHERE ");
//            	stb.append("    TK1.YEAR < '" + _param._year + "' ");
//            	stb.append(" GROUP BY ");
//            	stb.append("     TK1.YEAR, ");
//            	stb.append("     TK1.GRADE_CD, ");
//            	stb.append("     TK1.SCHREGNO ");
//            	stb.append(" UNION ALL ");
//            	stb.append(" SELECT ");
//            	stb.append("     TK2.YEAR, ");
//            	stb.append("     TK2.SEMESTER, ");
//            	stb.append("     TK2.GRADE_CD, ");
//            	stb.append("     TK2.SCHREGNO ");
//            	stb.append(" FROM ");
//            	stb.append("   SYUUYAKU_WK_SUB TK2 ");
//            	stb.append(" WHERE ");
//            	stb.append("    TK2.YEAR = '" + _param._year + "' ");
//                stb.append("    AND TK2.SEMESTER = '" + useSemester + "' "); //(単学期の可能性からすると前学期の可能性もあるが)駒澤の科目は全て通年との事なので、指定学期で取得。
//            	stb.append(" ), SCHREG_FILTER AS ( ");  // 抽出条件として当年度の学籍番号と校種を取得
//            	stb.append(" SELECT DISTINCT ");
//            	stb.append("   T1.SCHREGNO, ");
//            	stb.append("   T2.SCHOOL_KIND ");
//            	stb.append(" FROM ");
//            	stb.append("   SCHREG_REGD_DAT T1 ");
//            	stb.append("   LEFT JOIN SCHREG_REGD_GDAT T2 ");
//            	stb.append("     ON T2.YEAR = T1.YEAR ");
//            	stb.append("    AND T2.GRADE = T1.GRADE ");
//            	stb.append(" WHERE ");
//            	stb.append("   T1.YEAR = '" + _param._year + "' ");
//            	stb.append("   AND T1.GRADE || T1.HR_CLASS = ? ");
//            	stb.append(" ), SYUUYAKU_SCHREG AS ( ");  // SCHREG情報として、集約。
//            	stb.append(" SELECT ");
//            	stb.append("   T1.*, ");
//            	stb.append("   T2.SCHOOL_KIND ");
//            	stb.append(" FROM ");
//            	stb.append("   SCHREG_REGD_DAT T1 ");
//            	stb.append("   INNER JOIN SYUUYAKU_WK TW1 ");
//            	stb.append("     ON TW1.YEAR = T1.YEAR ");
//            	stb.append("    AND TW1.SEMESTER = T1.SEMESTER");
//            	stb.append("    AND TW1.SCHREGNO = T1.SCHREGNO ");
//            	stb.append("   LEFT JOIN SCHREG_REGD_GDAT T2 ");
//            	stb.append("     ON T2.YEAR = T1.YEAR ");
//            	stb.append("    AND T2.GRADE = T1.GRADE ");
//            	stb.append(" WHERE ");
//            	stb.append("   T1.YEAR <= '" + _param._year + "' ");
//            	stb.append("   AND T2.SCHOOL_KIND IN (SELECT DISTINCT SCHOOL_KIND FROM SCHREG_FILTER) ");
//            	stb.append("   AND T1.SCHREGNO IN (SELECT SCHREGNO FROM SCHREG_FILTER) ");
//            	stb.append(" ), REPL AS ( ");  //元科目、先科目情報
//            	stb.append("  SELECT ");
//            	stb.append("   '1' AS DIV, ");
//            	stb.append("    COMBINED_CLASSCD AS CLASSCD, ");
//            	stb.append("    COMBINED_SCHOOL_KIND AS SCHOOL_KIND, ");
//            	stb.append("    COMBINED_CURRICULUM_CD AS CURRICULUM_CD, ");
//            	stb.append("    COMBINED_SUBCLASSCD AS SUBCLASSCD, ");
//            	stb.append("    CAST(NULL AS VARCHAR(1)) AS CALCULATE_CREDIT_FLG ");
//            	stb.append("  FROM ");
//            	stb.append("    SUBCLASS_REPLACE_COMBINED_DAT ");
//            	stb.append("  WHERE ");
//            	stb.append("    YEAR = '" + _param._year + "' ");
//            	stb.append("  GROUP BY ");
//            	stb.append("    COMBINED_CLASSCD, ");
//            	stb.append("    COMBINED_SCHOOL_KIND, ");
//            	stb.append("    COMBINED_CURRICULUM_CD, ");
//            	stb.append("    COMBINED_SUBCLASSCD ");
//            	stb.append("  UNION ");
//            	stb.append("  SELECT ");
//            	stb.append("    '2' AS DIV, ");
//            	stb.append("    ATTEND_CLASSCD AS CLASSCD, ");
//            	stb.append("    ATTEND_SCHOOL_KIND AS SCHOOL_KIND, ");
//            	stb.append("    ATTEND_CURRICULUM_CD AS CURRICULUM_CD, ");
//            	stb.append("    ATTEND_SUBCLASSCD AS SUBCLASSCD, ");
//            	stb.append("    MAX(CALCULATE_CREDIT_FLG) AS CALCULATE_CREDIT_FLG ");
//            	stb.append("  FROM ");
//            	stb.append("    SUBCLASS_REPLACE_COMBINED_DAT ");
//            	stb.append("  WHERE ");
//            	stb.append("    YEAR = '" + _param._year + "' ");
//            	stb.append("  GROUP BY ");
//            	stb.append("    ATTEND_CLASSCD, ");
//            	stb.append("    ATTEND_SCHOOL_KIND, ");
//            	stb.append("    ATTEND_CURRICULUM_CD, ");
//            	stb.append("    ATTEND_SUBCLASSCD ");
//            	stb.append(" ) ");
//            	stb.append(" SELECT DISTINCT ");
//            	stb.append("   T3.CLASSCD, ");
//            	stb.append("   T3.SCHOOL_KIND, ");
//            	stb.append("   T3.CURRICULUM_CD, ");
//            	stb.append("   T3.SUBCLASSCD, ");
//            	stb.append("   L1.CLASSNAME, ");
//            	stb.append("   L1.CLASSABBV, ");
//            	stb.append("   L2.SUBCLASSNAME, ");
//            	stb.append("   L2.SUBCLASSABBV, ");
//            	stb.append("   CASE WHEN L3.SUBCLASSCD IS NOT NULL THEN 1 END AS IS_SAKI, ");
//            	stb.append("   CASE WHEN L4.SUBCLASSCD IS NOT NULL THEN 1 END AS IS_MOTO ");
//            	stb.append(" FROM ");
//            	stb.append("   SYUUYAKU_SCHREG T1 ");
//            	stb.append("   LEFT JOIN CHAIR_STD_DAT T2 ");
//            	stb.append("     ON T2.YEAR = T1.YEAR ");
//            	stb.append("    AND T2.SEMESTER = T1.SEMESTER ");
//            	stb.append("    AND T2.SCHREGNO = T1.SCHREGNO ");
//            	stb.append("   LEFT JOIN CHAIR_DAT T3 ");
//            	stb.append("     ON T3.YEAR = T2.YEAR ");
//            	stb.append("    AND T3.SEMESTER = T2.SEMESTER ");
//            	stb.append("    AND T3.CHAIRCD = T2.CHAIRCD ");
//            	stb.append("    AND T3.SCHOOL_KIND = T1.SCHOOL_KIND ");  // 3年分の出力なので、校種で制限する
//            	stb.append("   LEFT JOIN CLASS_MST L1 ");
//            	stb.append("     ON L1.CLASSCD = T3.CLASSCD ");
//            	stb.append("    AND L1.SCHOOL_KIND = T3.SCHOOL_KIND ");
//            	stb.append("   LEFT JOIN SUBCLASS_MST L2 ");
//            	stb.append("     ON L2.CLASSCD = T3.CLASSCD ");
//            	stb.append("    AND L2.SCHOOL_KIND = T3.SCHOOL_KIND ");
//            	stb.append("    AND L2.CURRICULUM_CD = T3.CURRICULUM_CD ");
//            	stb.append("    AND L2.SUBCLASSCD = T3.SUBCLASSCD ");
//            	stb.append("   LEFT JOIN REPL L3 ");
//            	stb.append("     ON L3.DIV = '1' ");
//            	stb.append("    AND L3.CLASSCD = T3.CLASSCD ");
//            	stb.append("    AND L3.SCHOOL_KIND = T3.SCHOOL_KIND ");
//            	stb.append("    AND L3.CURRICULUM_CD = T3.CURRICULUM_CD ");
//            	stb.append("    AND L3.SUBCLASSCD = T3.SUBCLASSCD ");
//            	stb.append("   LEFT JOIN REPL L4 ");
//            	stb.append("     ON L4.DIV = '2' ");
//            	stb.append("    AND L4.CLASSCD = T3.CLASSCD ");
//            	stb.append("    AND L4.SCHOOL_KIND = T3.SCHOOL_KIND ");
//            	stb.append("    AND L4.CURRICULUM_CD = T3.CURRICULUM_CD ");
//            	stb.append("    AND L4.SUBCLASSCD = T3.SUBCLASSCD ");
//            	stb.append(" WHERE ");
//            	stb.append("   T3.CLASSCD IS NOT NULL ");
//            	stb.append(" ORDER BY ");
//            	stb.append("   T3.CLASSCD, ");
//            	stb.append("   T3.SCHOOL_KIND, ");
//            	stb.append("   T3.CURRICULUM_CD, ");
//            	stb.append("   T3.SUBCLASSCD ");
//                _param._psBuffer.put(pskey, stb.toString());
//            }
//        	final String sql = (String)_param._psBuffer.get(pskey);
//
//            Map subMap = null;
//            try {
//                ps = db2.prepareStatement(sql);
//                ps.setString(1, ghrClass);
//                rs = ps.executeQuery();
//                while (rs.next()) {
//                	final String classcd = rs.getString("CLASSCD");
//                	final String school_Kind = rs.getString("SCHOOL_KIND");
//                	final String curriculum_Cd = rs.getString("CURRICULUM_CD");
//                	final String subclasscd = rs.getString("SUBCLASSCD");
//                	final String classname = rs.getString("CLASSNAME");
//                	final String classabbv = rs.getString("CLASSABBV");
//                	final String subclassname = rs.getString("SUBCLASSNAME");
//                	final String subclassabbv = rs.getString("SUBCLASSABBV");
//                    final boolean isSaki = "1".equals(rs.getString("IS_SAKI"));
//                    final boolean isMoto = "1".equals(rs.getString("IS_MOTO"));
//                	SubclsInfo addwk = new SubclsInfo(classcd, school_Kind, curriculum_Cd, subclasscd, classname, classabbv, subclassname, subclassabbv, isSaki, isMoto);
//                	final String mKey = classcd + "-" + school_Kind;
//                	if (_subclsInfoMap.containsKey(mKey)) {
//                		subMap = (Map)_subclsInfoMap.get(mKey);
//                	} else {
//                		subMap = new LinkedMap();
//                		_subclsInfoMap.put(mKey, subMap);
//                	}
//                	final String subKey = classcd + "-" + school_Kind + "-" + curriculum_Cd  + "-" + subclasscd;
//                	subMap.put(subKey, addwk);
//                }
//            } catch (final SQLException e) {
//                log.error("生徒の基本情報取得でエラー", e);
//                throw e;
//            } finally {
//                db2.commit();
//                DbUtils.closeQuietly(null, ps, rs);
//            }
//        }
    }

    private class Student {
        final String _schregno;
        final String _grade;
        final String _hrClass;
        final String _hrName;
        final String _hrAbbv;
        final String _attendNo;
        final String _name;
        final String _courseCd;
        final String _majorCd;
        final String _courseCode;
        final String _majorName;
        final String _courseCodeName;
        final Map _subclassValMap;
        final Map _subclassRankMap;
        final String _valuation;
        final String _valMark;

        public Student(
                final String schregno,
                final String grade,
                final String hrClass,
                final String hrName,
                final String hrAbbv,
                final String attendNo,
                final String name,
                final String courseCd,
                final String majorCd,
                final String courseCode,
                final String majorName,
                final String courseCodeName,
                final String valuation,
                final String valMark
        ) {
            _schregno = schregno;
            _grade = grade;
            _hrClass = hrClass;
            _hrName = hrName;
            _hrAbbv = hrAbbv;
            _attendNo = attendNo;
            _name = name;
            _courseCd = courseCd;
            _majorCd = majorCd;
            _courseCode = courseCode;
            _majorName = majorName;
            _courseCodeName = courseCodeName;
            _subclassValMap = new  LinkedMap();
            _subclassRankMap = new LinkedMap();
            _valuation = valuation;
            _valMark = valMark;
        }


//        public void setSubclassValuation(final DB2UDB db2,final GradeHrCls ghCls) throws SQLException {
//        	final String useSemester = "9".equals(_param._semester) ? _param._lastSemester : _param._semester;
//            PreparedStatement ps = null;
//            ResultSet rs = null;
//            final String pskey = "setSubclassValuation";
//
//            try {
//                if (!_param._psBuffer.containsKey(pskey)) {
//                    final StringBuffer stb = new StringBuffer();
//                    stb.append(" WITH SYUUYAKU_WK_SUB AS ( ");  // 留年を加味したデータにする(留年した年のデータを抜くため、過年度学年の最後の年(最大の年)で集約)
//                    stb.append(" SELECT ");
//                    stb.append("     MAX(TW1.YEAR) AS YEAR, ");
//                    stb.append("     TW1.SEMESTER, ");
//                    stb.append("     TW2.GRADE_CD, ");
//                    stb.append("     TW1.SCHREGNO ");
//                    stb.append(" FROM ");
//                    stb.append("     SCHREG_REGD_DAT TW1 ");
//                    stb.append("     LEFT JOIN SCHREG_REGD_GDAT TW2 ");
//                    stb.append("       ON TW2.YEAR = TW1.YEAR ");
//                    stb.append("      AND TW2.GRADE = TW1.GRADE ");
//                    stb.append(" WHERE ");
//                    stb.append("    TW1.YEAR <= '" + _param._year + "' ");
//                    stb.append(" GROUP BY ");
//                    stb.append("     TW1.SEMESTER, ");
//                    stb.append("     TW2.GRADE_CD, ");
//                    stb.append("     TW1.SCHREGNO ");
//                    stb.append(" ), SYUUYAKU_WK AS ( ");  // 上記データから、過年度分は最終学期、当年度分は指定学期のデータだけを抽出
//                    stb.append(" SELECT ");
//                    stb.append("    TK1.YEAR, ");
//                    stb.append("    MAX(TK1.SEMESTER) AS SEMESTER, ");
//                    stb.append("    TK1.GRADE_CD, ");
//                    stb.append("    TK1.SCHREGNO ");
//                    stb.append(" FROM ");
//                    stb.append("   SYUUYAKU_WK_SUB TK1 ");
//                    stb.append(" WHERE ");
//                    stb.append("    TK1.YEAR < '" + _param._year + "' ");
//                    stb.append(" GROUP BY ");
//                    stb.append("     TK1.YEAR, ");
//                    stb.append("     TK1.GRADE_CD, ");
//                    stb.append("     TK1.SCHREGNO ");
//                    stb.append(" UNION ALL ");
//                    stb.append(" SELECT ");
//                    stb.append("     TK2.YEAR, ");
//                    stb.append("     TK2.SEMESTER, ");
//                    stb.append("     TK2.GRADE_CD, ");
//                    stb.append("     TK2.SCHREGNO ");
//                    stb.append(" FROM ");
//                    stb.append("   SYUUYAKU_WK_SUB TK2 ");
//                    stb.append(" WHERE ");
//                    stb.append("    TK2.YEAR = '" + _param._year + "' ");
//                    stb.append("    AND TK2.SEMESTER = '" + useSemester + "' "); //(単学期の可能性からすると前学期の可能性もあるが)駒澤の科目は全て通年との事なので、指定学期で取得。
//                    stb.append(" ), SCHREG_FILTER AS ( ");  // 抽出条件として当年度の学籍番号と校種を取得(過年度はクラスでは取得できないので、学籍番号で取得しないといけない)
//                    stb.append(" SELECT DISTINCT ");
//                    stb.append("   T1.SCHREGNO, ");
//                    stb.append("   T2.SCHOOL_KIND ");
//                    stb.append(" FROM ");
//                    stb.append("   SCHREG_REGD_DAT T1 ");
//                    stb.append("   LEFT JOIN SCHREG_REGD_GDAT T2 ");
//                    stb.append("     ON T2.YEAR = T1.YEAR ");
//                    stb.append("    AND T2.GRADE = T1.GRADE ");
//                    stb.append(" WHERE ");
//                    stb.append("   T1.YEAR = '" + _param._year + "' ");
//                    stb.append("   AND T1.GRADE || T1.HR_CLASS = ? ");
//                    stb.append(" ), SYUUYAKU_SCHREG AS ( ");  // SCHREG情報として、集約。
//                    stb.append(" SELECT ");
//                    stb.append("   T1.*, ");
//                    stb.append("   T2.SCHOOL_KIND ");
//                    stb.append(" FROM ");
//                    stb.append("   SCHREG_REGD_DAT T1 ");
//                    stb.append("   INNER JOIN SYUUYAKU_WK TW1 ");
//                    stb.append("     ON TW1.YEAR = T1.YEAR ");
//                    stb.append("    AND TW1.SEMESTER = T1.SEMESTER ");
//                    stb.append("    AND TW1.SCHREGNO = T1.SCHREGNO ");
//                    stb.append("   LEFT JOIN SCHREG_REGD_GDAT T2 ");
//                    stb.append("     ON T2.YEAR = T1.YEAR ");
//                    stb.append("    AND T2.GRADE = T1.GRADE ");
//                    stb.append(" WHERE ");
//                    stb.append("   T2.SCHOOL_KIND IN (SELECT DISTINCT SCHOOL_KIND FROM SCHREG_FILTER) ");
//                    stb.append("   AND T1.SCHREGNO = ? ");
//                    stb.append(" ), PRTBASE_DAT AS ( ");
//                    // 過年度評定データ
//                    stb.append(" select ");
//                    stb.append("   T2.YEAR, ");
//                    stb.append("   T1.GRADE, ");
//                    stb.append("   T2.SCHREGNO, ");
//                    stb.append("   T2.CLASSCD, ");
//                    stb.append("   T2.SCHOOL_KIND, ");
//                    stb.append("   T2.CURRICULUM_CD, ");
//                    stb.append("   T2.SUBCLASSCD, ");
//                    stb.append("   NULL AS PROV_FLG, ");
//                    stb.append("   T2.VALUATION AS SCORE ");
//                    stb.append(" FROM ");
//                    stb.append("   SYUUYAKU_SCHREG T1 ");
//                    stb.append("   LEFT JOIN SCHREG_STUDYREC_DAT T2 ");
//                    stb.append("     ON T2.YEAR = T1.YEAR ");
//                    stb.append("    AND T2.SCHREGNO = T1.SCHREGNO ");
//                    stb.append("    AND T2.ANNUAL = T1.ANNUAL ");
//                    stb.append("    AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
//                    stb.append(" WHERE ");
//                    stb.append("   T1.YEAR < '" + _param._year + "' ");
//                    stb.append("   AND T2.CLASSCD < 90 ");
//                    stb.append("   AND T2.YEAR IS NOT NULL ");
//                    stb.append(" UNION ALL ");
//                    // 当年度データ
//                    stb.append(" select ");
//                    stb.append("   T2.YEAR, ");
//                    stb.append("   T1.GRADE, ");
//                    stb.append("   T2.SCHREGNO, ");
//                    stb.append("   T2.CLASSCD, ");
//                    stb.append("   T2.SCHOOL_KIND, ");
//                    stb.append("   T2.CURRICULUM_CD, ");
//                    stb.append("   T2.SUBCLASSCD, ");
//                    if ("1".equals(_param._useProvFlg)) {
//                        stb.append("    P1.PROV_FLG, ");
//                        stb.append("    CASE WHEN T2.SCORE = 1 AND P1.PROV_FLG = '1' THEN 2 ELSE T2.SCORE END AS SCORE ");
//                    } else {
//                        stb.append("   NULL AS PROV_FLG, ");
//                        stb.append("   T2.SCORE ");
//                    }
//                    stb.append(" FROM ");
//                    stb.append("   SYUUYAKU_SCHREG T1 ");
//                    stb.append("   LEFT JOIN RECORD_RANK_SDIV_DAT T2 ");
//                    stb.append("     ON T2.YEAR = T1.YEAR ");
//                    stb.append("    AND T2.SEMESTER = '" + _param._semester + "' ");
//                    stb.append("    AND T2.SCHREGNO = T1.SCHREGNO ");
//                    if ("1".equals(_param._useProvFlg)) {
//                        stb.append("   LEFT JOIN RECORD_PROV_FLG_DAT P1 ");
//                        stb.append("     ON P1.YEAR = T2.YEAR ");
//                        stb.append("    AND P1.CLASSCD = T2.CLASSCD ");
//                        stb.append("    AND P1.SCHOOL_KIND = T2.SCHOOL_KIND ");
//                        stb.append("    AND P1.CURRICULUM_CD = T2.CURRICULUM_CD ");
//                        stb.append("    AND P1.SUBCLASSCD = T2.SUBCLASSCD ");
//                        stb.append("    AND P1.SCHREGNO = T2.SCHREGNO ");
//                    }
//                    stb.append(" WHERE ");
//                    stb.append("   T1.YEAR = '" + _param._year + "' ");
//                    stb.append("   AND T2.CLASSCD < 90 ");
//                    stb.append("   AND T1.SEMESTER = '" + useSemester + "' ");
//                    stb.append("   AND T2.TESTKINDCD || T2.TESTITEMCD || T2.SCORE_DIV = '" + _param._testcd + "' ");
//                    stb.append("   AND T2.YEAR IS NOT NULL ");
//                    stb.append("   AND T2.SUBCLASSCD NOT IN ('333333', '777777', '99999A', '99999B') ");
//                    stb.append("   AND T2.SUBCLASSCD NOT IN ('" + ALL9 + "', '" + ALL5 + "') ");
//                    stb.append(" ), KEKKA AS (");
//                    // 評定データ(過年度含む)
//                    stb.append(" SELECT ");
//                    stb.append("   TF1.YEAR, ");
//                    stb.append("   TF1.GRADE, ");
//                    stb.append("   TF1.SCHREGNO, ");
//                    stb.append("   TF1.CLASSCD, ");
//                    stb.append("   TF1.SCHOOL_KIND, ");
//                    stb.append("   TF1.CURRICULUM_CD, ");
//                    stb.append("   TF1.SUBCLASSCD, ");
//                    stb.append("   TF1.PROV_FLG, ");
//                    stb.append("   DECIMAL(CAST(TF1.SCORE AS double), 5, 1) AS SCORE ");
//                    stb.append(" FROM ");
//                    stb.append("   PRTBASE_DAT TF1 ");
//                    stb.append(" )");
//                    stb.append(" SELECT ");
//                    stb.append("   T1.SCHREGNO, ");
//                    stb.append("   CASE WHEN DECIMAL(ROUND(AVG(CAST(T1.SCORE AS double))*10.0, 0)/10.0, 5,1) < 1.9 THEN 1.9 ELSE DECIMAL(ROUND(AVG(CAST(T1.SCORE AS double))*10.0, 0)/10.0, 5,1) END AS VALUATION"); //下限値1.9にする
//                    stb.append(" FROM ");
//                    stb.append("   KEKKA T1 ");
//                	stb.append(" GROUP BY ");
//                	stb.append("   SCHREGNO ");
//
//                    _param._psBuffer.put(pskey, stb.toString());
//                }
//            	final String sql = (String)_param._psBuffer.get(pskey);
//                ps = db2.prepareStatement(sql);
//
//                ps.setString(1, _grade + _hrClass);
//                ps.setString(2, _schregno);
//                rs = ps.executeQuery();
//                while (rs.next()) {
//                	final String schregno = rs.getString("SCHREGNO");
//                	final String valuation = rs.getString("VALUATION");
//                }
//            } catch (final SQLException e) {
//                log.error("生徒の基本情報取得でエラー", e);
//                throw e;
//            } finally {
//                DbUtils.closeQuietly(null, ps, rs);
//                db2.commit();
//            }
//        }

//        public void setSubclassRank(final DB2UDB db2) throws SQLException {
//        	final String useSemester = "9".equals(_param._semester) ? _param._lastSemester : _param._semester;
//            PreparedStatement ps = null;
//            ResultSet rs = null;
//            final String pskey = "setSubclassRank";
//
//            try {
//                if (!_param._psBuffer.containsKey(pskey)) {
//                	final StringBuffer stb = new StringBuffer();
//                	stb.append(" WITH SYUUYAKU_WK_SUB AS ( ");  // 留年を加味したデータにする(留年した年のデータを抜くため、過年度学年の最後の年(最大の年)で集約)
//                	stb.append(" SELECT ");
//                	stb.append("     MAX(TW1.YEAR) AS YEAR, ");
//                	stb.append("     TW1.SEMESTER, ");
//                	stb.append("     TW2.GRADE_CD, ");
//                	stb.append("     TW1.SCHREGNO ");
//                	stb.append(" FROM ");
//                	stb.append("     SCHREG_REGD_DAT TW1 ");
//                	stb.append("     LEFT JOIN SCHREG_REGD_GDAT TW2 ");
//                	stb.append("       ON TW2.YEAR = TW1.YEAR ");
//                	stb.append("      AND TW2.GRADE = TW1.GRADE ");
//                	stb.append(" WHERE ");
//                	stb.append("    TW1.YEAR <= '" + _param._year + "' ");
//                	stb.append(" GROUP BY ");
//                	stb.append("     TW1.SEMESTER, ");
//                	stb.append("     TW2.GRADE_CD, ");
//                	stb.append("     TW1.SCHREGNO ");
//                    stb.append(" ), SYUUYAKU_WK AS ( ");  // 上記データから、過年度分は最終学期、当年度分は指定学期のデータだけを抽出
//                    stb.append(" SELECT ");
//                    stb.append("    TK1.YEAR, ");
//                    stb.append("    MAX(TK1.SEMESTER) AS SEMESTER, ");
//                    stb.append("    TK1.GRADE_CD, ");
//                    stb.append("    TK1.SCHREGNO ");
//                    stb.append(" FROM ");
//                    stb.append("   SYUUYAKU_WK_SUB TK1 ");
//                    stb.append(" WHERE ");
//                    stb.append("    TK1.YEAR < '" + _param._year + "' ");
//                    stb.append(" GROUP BY ");
//                    stb.append("     TK1.YEAR, ");
//                    stb.append("     TK1.GRADE_CD, ");
//                    stb.append("     TK1.SCHREGNO ");
//                    stb.append(" UNION ALL ");
//                    stb.append(" SELECT ");
//                    stb.append("     TK2.YEAR, ");
//                    stb.append("     TK2.SEMESTER, ");
//                    stb.append("     TK2.GRADE_CD, ");
//                    stb.append("     TK2.SCHREGNO ");
//                    stb.append(" FROM ");
//                    stb.append("   SYUUYAKU_WK_SUB TK2 ");
//                    stb.append(" WHERE ");
//                    stb.append("    TK2.YEAR = '" + _param._year + "' ");
//                    stb.append("    AND TK2.SEMESTER = '" + useSemester + "' "); //(単学期の可能性からすると前学期の可能性もあるが)駒澤の科目は全て通年との事なので、指定学期で取得。
//                	stb.append(" ), SCHREG_FILTER AS ( ");  // 抽出条件として当年度の学籍番号と校種を取得(過年度はクラスでは取得できないので、学籍番号で取得しないといけない)
//                	stb.append(" SELECT DISTINCT ");
//                	stb.append("   T1.SCHREGNO, ");
//                	stb.append("   T2.SCHOOL_KIND ");
//                	stb.append(" FROM ");
//                	stb.append("   SCHREG_REGD_DAT T1 ");
//                	stb.append("   LEFT JOIN SCHREG_REGD_GDAT T2 ");
//                	stb.append("     ON T2.YEAR = T1.YEAR ");
//                	stb.append("    AND T2.GRADE = T1.GRADE ");
//                	stb.append(" WHERE ");
//                	stb.append("   T1.YEAR = '" + _param._year + "' ");
//                	stb.append("   AND T1.GRADE || T1.HR_CLASS = ? ");
//                	stb.append(" ), SYUUYAKU_SCHREG AS ( ");  // SCHREG情報として、集約。
//                	stb.append(" SELECT ");
//                	stb.append("   T1.*, ");
//                	stb.append("   T2.SCHOOL_KIND ");
//                	stb.append(" FROM ");
//                	stb.append("   SCHREG_REGD_DAT T1 ");
//                	stb.append("   INNER JOIN SYUUYAKU_WK TW1 ");
//                	stb.append("     ON TW1.YEAR = T1.YEAR ");
//                	stb.append("    AND TW1.SEMESTER = T1.SEMESTER");
//                	stb.append("    AND TW1.SCHREGNO = T1.SCHREGNO ");
//                	stb.append("   LEFT JOIN SCHREG_REGD_GDAT T2 ");
//                	stb.append("     ON T2.YEAR = T1.YEAR ");
//                	stb.append("    AND T2.GRADE = T1.GRADE ");
//                	stb.append(" WHERE ");
//                	stb.append("   T2.SCHOOL_KIND IN (SELECT DISTINCT SCHOOL_KIND FROM SCHREG_FILTER) ");
//                	stb.append("   AND T1.SCHREGNO = ? ");
//                	stb.append(" ) ");
//                	//-- 過年度成績(評価)データ
//                	stb.append(" select ");
//                	stb.append("   T2.YEAR, ");
//                	stb.append("   T1.GRADE, ");
//                	stb.append("   T2.SCHREGNO, ");
//                	stb.append("   T2.CLASSCD, ");
//                	stb.append("   T2.SCHOOL_KIND, ");
//                	stb.append("   T2.CURRICULUM_CD, ");
//                	stb.append("   T2.SUBCLASSCD, ");
//                	stb.append("   T2.SCORE, ");
//                	stb.append("   DECIMAL(ROUND(T2.AVG*10.0, 0)/10.0, 5, 1) AS AVG, ");
//                	stb.append("   T2.CLASS_RANK, ");
//                	stb.append("   T2.GRADE_RANK ");
//                	stb.append(" FROM ");
//                	stb.append("   SYUUYAKU_SCHREG T1 ");
//                	stb.append("   LEFT JOIN RECORD_RANK_SDIV_DAT T2 ");
//                	stb.append("     ON T2.YEAR = T1.YEAR ");
//                	stb.append("    AND T2.SEMESTER = '9' ");
//                	stb.append("    AND T2.SCHREGNO = T1.SCHREGNO ");
//                	stb.append("    AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
//                	stb.append(" WHERE ");
//                	stb.append("   T1.YEAR < '" + _param._year + "' ");
//                	stb.append("   AND T2.YEAR IS NOT NULL ");
//                	stb.append("   AND T2.TESTKINDCD || T2.TESTITEMCD || T2.SCORE_DIV = '990008' ");  //過年度は年度末評価固定
//                	stb.append("   AND T2.SUBCLASSCD = '" + ALL9 + "' ");
//                	stb.append(" UNION ALL");
//                	//-- 当年度成績(評価)データ
//                	stb.append(" select ");
//                	stb.append("   T2.YEAR, ");
//                	stb.append("   T1.GRADE, ");
//                	stb.append("   T2.SCHREGNO, ");
//                	stb.append("   T2.CLASSCD, ");
//                	stb.append("   T2.SCHOOL_KIND, ");
//                	stb.append("   T2.CURRICULUM_CD, ");
//                	stb.append("   T2.SUBCLASSCD, ");
//                	stb.append("   T2.SCORE, ");
//                	stb.append("   DECIMAL(ROUND(T2.AVG*10.0, 0)/10.0, 5, 1) AS AVG, ");
//                	stb.append("   T2.CLASS_RANK, ");
//                	stb.append("   T2.GRADE_RANK ");
//                	stb.append(" FROM ");
//                	stb.append("   SYUUYAKU_SCHREG T1 ");
//                	stb.append("   LEFT JOIN RECORD_RANK_SDIV_DAT T2 ");
//                	stb.append("     ON T2.YEAR = T1.YEAR ");
//                	stb.append("    AND T2.SEMESTER = T1.SEMESTER ");
//                	stb.append("    AND T2.SCHREGNO = T1.SCHREGNO ");
//                	stb.append(" WHERE ");
//                	stb.append("   T1.YEAR = '" + _param._year + "' ");
//                	stb.append("   AND T1.SEMESTER = '" + useSemester + "' ");
//                	stb.append("   AND T2.TESTKINDCD || T2.TESTITEMCD || T2.SCORE_DIV = '" + _param._testcd.substring(0, 4) + "08" + "' ");
//                	stb.append("   AND T2.YEAR IS NOT NULL ");
//                	stb.append("   AND T2.SUBCLASSCD = '" + ALL9 + "' ");
//                    _param._psBuffer.put(pskey, stb.toString());
//                }
//            	final String sql = (String)_param._psBuffer.get(pskey);
//                ps = db2.prepareStatement(sql);
//
//                ps.setString(1, _grade + _hrClass);
//                ps.setString(2, _schregno);
//                rs = ps.executeQuery();
//                while (rs.next()) {
//                	final String year = rs.getString("YEAR");
//                	final String grade = rs.getString("GRADE");
//                	final String schregno = rs.getString("SCHREGNO");
//                	final String classcd = rs.getString("CLASSCD");
//                	final String schoolKind = rs.getString("SCHOOL_KIND");
//                	final String curriculumCd = rs.getString("CURRICULUM_CD");
//                	final String subclasscd = rs.getString("SUBCLASSCD");
//                	final String score = rs.getString("SCORE");
//                	final String avg = rs.getString("AVG");
//                	final String class_Rank = rs.getString("CLASS_RANK");
//                	final String grade_Rank = rs.getString("GRADE_RANK");
//
//                    final SubclassRank subclassRank = new SubclassRank(year, grade, schregno, classcd, schoolKind, curriculumCd, subclasscd, score, avg, class_Rank, grade_Rank);
//                    final String mKey = grade + "-" + classcd + "-" + schoolKind + "-" + curriculumCd + "-" + subclasscd;
//                    _subclassRankMap.put(mKey, subclassRank);
//                }
//            } catch (final SQLException e) {
//                log.error("生徒の基本情報取得でエラー", e);
//                throw e;
//            } finally {
//                DbUtils.closeQuietly(null, ps, rs);
//                db2.commit();
//            }
//        }

//        private void setValuation(final DB2UDB db2, final String schoolKind) throws SQLException {
//        	final String useSemester = "9".equals(_param._semester) ? _param._lastSemester : _param._semester;
//            PreparedStatement ps = null;
//            ResultSet rs = null;
//            final String pskey = "setValuation";
//
//            try {
//                if (!_param._psBuffer.containsKey(pskey)) {
//                	final StringBuffer stb = new StringBuffer();
//                	stb.append(" WITH SYUUYAKU_WK_SUB AS ( ");  // 留年を加味したデータにする(留年した年のデータを抜くため、過年度学年の最後の年(最大の年)で集約)
//                	stb.append(" SELECT ");
//                	stb.append("     MAX(TW1.YEAR) AS YEAR, ");
//                	stb.append("     TW1.SEMESTER, ");
//                	stb.append("     TW2.GRADE_CD, ");
//                	stb.append("     TW1.SCHREGNO ");
//                	stb.append(" FROM ");
//                	stb.append("     SCHREG_REGD_DAT TW1 ");
//                	stb.append("     LEFT JOIN SCHREG_REGD_GDAT TW2 ");
//                	stb.append("       ON TW2.YEAR = TW1.YEAR ");
//                	stb.append("      AND TW2.GRADE = TW1.GRADE ");
//                	stb.append(" WHERE ");
//                	stb.append("    TW1.YEAR <= '" + _param._year + "' ");
//                	stb.append(" GROUP BY ");
//                	stb.append("     TW1.SEMESTER, ");
//                	stb.append("     TW2.GRADE_CD, ");
//                	stb.append("     TW1.SCHREGNO ");
//                    stb.append(" ), SYUUYAKU_WK AS ( ");  // 上記データから、過年度分は最終学期、当年度分は指定学期のデータだけを抽出
//                    stb.append(" SELECT ");
//                    stb.append("    TK1.YEAR, ");
//                    stb.append("    MAX(TK1.SEMESTER) AS SEMESTER, ");
//                    stb.append("    TK1.GRADE_CD, ");
//                    stb.append("    TK1.SCHREGNO ");
//                    stb.append(" FROM ");
//                    stb.append("   SYUUYAKU_WK_SUB TK1 ");
//                    stb.append(" WHERE ");
//                    stb.append("    TK1.YEAR < '" + _param._year + "' ");
//                    stb.append(" GROUP BY ");
//                    stb.append("     TK1.YEAR, ");
//                    stb.append("     TK1.GRADE_CD, ");
//                    stb.append("     TK1.SCHREGNO ");
//                    stb.append(" UNION ALL ");
//                    stb.append(" SELECT ");
//                    stb.append("     TK2.YEAR, ");
//                    stb.append("     TK2.SEMESTER, ");
//                    stb.append("     TK2.GRADE_CD, ");
//                    stb.append("     TK2.SCHREGNO ");
//                    stb.append(" FROM ");
//                    stb.append("   SYUUYAKU_WK_SUB TK2 ");
//                    stb.append(" WHERE ");
//                    stb.append("    TK2.YEAR = '" + _param._year + "' ");
//                    stb.append("    AND TK2.SEMESTER = '" + useSemester + "' "); //(単学期の可能性からすると前学期の可能性もあるが)駒澤の科目は全て通年との事なので、指定学期で取得。
//                	stb.append(" ), SYUUYAKU_SCHREG AS ( ");  // SCHREG情報として、集約。
//                	stb.append(" SELECT ");
//                	stb.append("   T1.*, ");
//                	stb.append("   T2.SCHOOL_KIND ");
//                	stb.append(" FROM ");
//                	stb.append("   SCHREG_REGD_DAT T1 ");
//                	stb.append("   INNER JOIN SYUUYAKU_WK TW1 ");
//                	stb.append("     ON TW1.YEAR = T1.YEAR ");
//                	stb.append("    AND TW1.SEMESTER = T1.SEMESTER");
//                	stb.append("    AND TW1.SCHREGNO = T1.SCHREGNO ");
//                	stb.append("   LEFT JOIN SCHREG_REGD_GDAT T2 ");
//                	stb.append("     ON T2.YEAR = T1.YEAR ");
//                	stb.append("    AND T2.GRADE = T1.GRADE ");
//                	stb.append(" WHERE ");
//                	stb.append("   T1.SCHREGNO = ? ");
//                    stb.append(" ), VALUATION_BASE AS ( ");
//                	stb.append(" SELECT ");
//                	stb.append("   T1.* ");
//                	stb.append(" FROM ");
//                	stb.append("   RECORD_RANK_SDIV_DAT T1 ");
//                	stb.append("   INNER JOIN SYUUYAKU_SCHREG T2 ");
//                	stb.append("     ON T2.YEAR = T1.YEAR ");
//                	stb.append("    AND T2.SCHREGNO = T1.SCHREGNO ");
//                	stb.append(" WHERE ");
//                	stb.append("   T1.YEAR < '" + _param._year + "' ");
//                	stb.append("   AND T1.SEMESTER = '9' ");
//                	stb.append("   AND T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '990009' ");
//                	stb.append("   AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '99-" + schoolKind + "-99-999999' ");
//                	stb.append(" UNION ALL ");
//                	stb.append(" SELECT ");
//                	stb.append("   T1.* ");
//                	stb.append(" FROM ");
//                	stb.append("   RECORD_RANK_SDIV_DAT T1 ");
//                	stb.append("   INNER JOIN SYUUYAKU_SCHREG T2 ");
//                	stb.append("     ON T2.YEAR = T1.YEAR ");
//                	stb.append("    AND T2.SCHREGNO = T1.SCHREGNO ");
//                	stb.append(" WHERE ");
//                	stb.append("   T1.YEAR = '" + _param._year + "' ");
//                	stb.append("   AND T1.SEMESTER = '" + _param._semester + "' ");
//                	stb.append("   AND T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '" + _param._testcd + "' ");
//                	stb.append("   AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '99-" + schoolKind + "-99-999999' ");
//                	stb.append(" ) ");
//                	stb.append(" SELECT ");
//                	stb.append("   SCHREGNO, ");
//                	stb.append("   DECIMAL(ROUND(AVG(AVG)*10.0, 0)/10.0, 3,1) AS VALUATION");
//                	stb.append(" FROM ");
//                	stb.append("   VALUATION_BASE ");
//                	stb.append(" GROUP BY ");
//                	stb.append("   SCHREGNO ");
//                	stb.append(" ORDER BY ");
//                    stb.append("   SCHREGNO ");
//                    _param._psBuffer.put(pskey, stb.toString());
//                }
//            	final String sql = (String)_param._psBuffer.get(pskey);
//                ps = db2.prepareStatement(sql);
//
//                ps.setString(1, _schregno);
//                rs = ps.executeQuery();
//                if (rs.next()) {
//                	_totalValuation = StringUtils.defaultString(rs.getString("VALUATION"));
//                }
//            } catch (final SQLException e) {
//                log.error("生徒の基本情報取得でエラー", e);
//                throw e;
//            } finally {
//                DbUtils.closeQuietly(null, ps, rs);
//                db2.commit();
//            }
//        }
    }

    private class SubclassValuation {
        final String _year;
        final String _grade;
        final String _schregno;
        final String _classcd;
        final String _school_Kind;
        final String _curriculum_Cd;
        final String _subclasscd;
        final String _prov_Flg;
        final String _score;
        public SubclassValuation (final String year, final String grade, final String schregno, final String classcd, final String school_Kind, final String curriculum_Cd, final String subclasscd, final String prov_Flg, final String score)
        {
            _year = year;
            _grade = grade;
            _schregno = schregno;
            _classcd = classcd;
            _school_Kind = school_Kind;
            _curriculum_Cd = curriculum_Cd;
            _subclasscd = subclasscd;
            _prov_Flg = prov_Flg;
            _score = score;
        }
    }

//    private class SubclassRank {
//        final String _year;
//        final String _grade;
//        final String _schregno;
//        final String _classcd;
//        final String _schoolKind;
//        final String _curriculumCd;
//        final String _subclasscd;
//        final String _score;
//        final String _avg;
//        final String _class_Rank;
//        final String _grade_Rank;
//
//        public SubclassRank (final String year, final String grade, final String schregno, final String classcd, final String school_Kind, final String curriculum_Cd,
//        		              final String subclasscd, final String score, final String avg, final String class_Rank, final String grade_Rank)
//        {
//            _year = year;
//            _grade = grade;
//            _schregno = schregno;
//            _classcd = classcd;
//            _schoolKind = school_Kind;
//            _curriculumCd = curriculum_Cd;
//            _subclasscd = subclasscd;
//            _score = score;
//            _avg = avg;
//            _class_Rank = class_Rank;
//            _grade_Rank = grade_Rank;
//        }
//
//        public String getKey() {
//            return _classcd + _schoolKind + _curriculumCd + _subclasscd;
//        }
//    }

//    private class SubclsInfo {
//        final String _classCd;
//        final String _school_Kind;
//        final String _curriculum_Cd;
//        final String _subclassCd;
//        final String _className;
//        final String _classAbbv;
//        final String _subclassName;
//        final String _subclassAbbv;
//        final boolean _isSaki;
//        final boolean _isMoto;
//        public SubclsInfo (final String classCd, final String school_Kind, final String curriculum_Cd, final String subclassCd, final String className, final String classAbbv
//        		            , final String subclassName, final String subclassAbbv, final boolean isSaki, final boolean isMoto)
//        {
//            _classCd = classCd;
//            _school_Kind = school_Kind;
//            _curriculum_Cd = curriculum_Cd;
//            _subclassCd = subclassCd;
//            _className = className;
//            _classAbbv = classAbbv;
//            _subclassName = subclassName;
//            _subclassAbbv = subclassAbbv;
//            _isSaki = isSaki;
//            _isMoto = isMoto;
//        }
//    }

    private class Semester {
        final String _semester;
        final String _semestername;
        final DateRange _dateRange;
        public Semester(final String semester, final String semestername, final String sdate, final String edate) {
            _semester = semester;
            _semestername = semestername;
            _dateRange = new DateRange(_semester, _semestername, sdate, edate);
        }
    }

    private class DateRange {
        final String _key;
        final String _name;
        final String _sdate;
        final String _edate;
        public DateRange(final String key, final String name, final String sdate, final String edate) {
            _key = key;
            _name = name;
            _sdate = sdate;
            _edate = edate;
        }
        public String toString() {
            return "DateRange(" + _key + ", " + _sdate + ", " + _edate + ")";
        }
    }

    private class AssessDat {
        final String _assesslevel;
        final String _assessmark;
        final String _assesslow;
        final String _assesshigh;
        public AssessDat (final String assesslevel, final String assessmark, final String assesslow, final String assesshigh)
        {
            _assesslevel = assesslevel;
            _assessmark = assessmark;
            _assesslow = assesslow;
            _assesshigh = assesshigh;
        }
    }

    private class totalCalc {
        BigDecimal _totalVal;
        int _cnt;
        totalCalc() {
            _totalVal = new BigDecimal(0.0);
            _cnt = 0;
        }
        private void add(final String addwk) {
            if (!"".equals(StringUtils.defaultString(addwk, ""))) {
                _totalVal = _totalVal.add(new BigDecimal(addwk));
                _cnt++;
            }
        }
        private BigDecimal calc() {

            if (_cnt == 0) {
                return null;
            } else {
                return _totalVal.divide(new BigDecimal(_cnt), 1, BigDecimal.ROUND_HALF_UP);
            }
        }

    }
    private class HistgramDat {
        final String _grade;
        final String _hrClass;
        final String _hrName;
        final String _hrNameabbv;
        final String _valMark;
        final String _valuation;
        final String _cnt;
        public HistgramDat (final String grade, final String hrClass, final String hrName, final String hrNameabbv, final String valMark, final String valuation, final String cnt)
        {
            _grade = grade;
            _hrClass = hrClass;
            _hrName = hrName;
            _hrNameabbv = hrNameabbv;
            _valMark = valMark;
            _valuation = valuation;
            _cnt = cnt;
        }
    }


    private class ValuationLevel {
        final String _levCd;
        final String _name;
        final String _levelMark;
        final String _levelMin;
        final String _levelMax;
        private ValuationLevel (final String levCd, final String name, final String levelMark, final String levelMin, final String levelMax)
        {
            _levCd = levCd;
            _name = name;
            _levelMark = levelMark;
            _levelMin = levelMin;
            _levelMax = levelMax;
        }
    }



    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 75581 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _year;
        final String _ctrlDate;
//        final String _ctrlSeme;
        final String _semester;
//        final String _semesterName;
        final String _testcd;
        final String _grade;
        final String _lastSemester;
        final String _useProvFlg;

//        final Map _certifInfo;
        final Map _psBuffer;
        final Map _gradeMap;
        final List _assessList;
        final List _valuationLevList;
        private Map _semesterMap;
        private List _attendNoList;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _ctrlDate = request.getParameter("LOGIN_DATE");
//            _ctrlSeme = request.getParameter("CTRL_SEMESTER");
            _semester = request.getParameter("SEMESTER");
            _grade =  request.getParameter("GRADE");  //学期末のみが対象
            _testcd = request.getParameter("SUB_TESTCD");  //学期末のみが対象

            _useProvFlg = request.getParameter("useProvFlg");

            _psBuffer = new HashMap();

            _lastSemester = getLastSemester(db2);
//            _semesterName = getSemesterName(db2);
//            _certifInfo = getCertifInfo(db2);
            _gradeMap = getGradeMap(db2);
            _assessList = getAssessMst(db2);
            _semesterMap = loadSemester(db2, _year, _grade);
            _valuationLevList = getValuationLevel(db2, _year);
            _attendNoList = getAttendNoList(db2);
        }

        private String getLastSemester(final DB2UDB db2) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT MAX(SEMESTER) FROM V_SEMESTER_GRADE_MST WHERE YEAR='" + _year + "' AND GRADE='" + _grade + "' AND SEMESTER <> '9' "));

        }
        private Map loadSemester(final DB2UDB db2, final String year, final String grade) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map map = new HashMap();
            try {
                final String sql = "select"
                        + "   SEMESTER,"
                        + "   SEMESTERNAME,"
                        + "   SDATE,"
                        + "   EDATE"
                        + " from"
                        + "   V_SEMESTER_GRADE_MST"
                        + " where"
                        + "   YEAR='" + year + "'"
                        + "   AND GRADE='" + grade + "'"
                        + " order by SEMESTER"
                    ;

                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    map.put(rs.getString("SEMESTER"), new Semester(rs.getString("SEMESTER"), rs.getString("SEMESTERNAME"), rs.getString("SDATE"), rs.getString("EDATE")));
                }
            } catch (final Exception ex) {
                log.error("loadSemester exception!", ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return map;
        }

        private Map getGradeMap(final DB2UDB db2) {
            Map retMap = new LinkedMap();
            final String sql = "SELECT GRADE_CD, GRADE FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND SCHOOL_KIND = 'H' ORDER BY GRADE_CD, GRADE";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String gradeCd = rs.getString("GRADE_CD");
                    final String grade = rs.getString("GRADE");
                    if (!retMap.containsKey(gradeCd)) {
                        retMap.put(gradeCd, grade);
                    }
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retMap;
        }
        private List getAttendNoList(final DB2UDB db2) {
            List rtnList = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT DISTINCT ");
                stb.append("     REGD.ATTENDNO ");
                stb.append(" FROM ");
                stb.append("     SCHREG_REGD_DAT REGD ");
                stb.append("     INNER JOIN SCHREG_BASE_MST BASE ");
                stb.append("             ON BASE.SCHREGNO = REGD.SCHREGNO ");
                // 在籍チェック:転学(2)・退学(3)者は除外
                stb.append("            AND ( BASE.GRD_DIV IS NULL OR BASE.GRD_DIV NOT IN('2','3') ) ");
                stb.append(" WHERE REGD.YEAR  = '" + _year + "' ");
                stb.append("   AND REGD.GRADE = '" + _grade + "' ");
                stb.append(" ORDER BY ATTENDNO ");
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String attNo = rs.getString("ATTENDNO");
                    rtnList.add(attNo);
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtnList;
        }

        private List getValuationLevel(final DB2UDB db2, final String year) {
            List rtnList = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer sql = new StringBuffer();
                sql.append(" SELECT ");
                sql.append("    T1.NAMECD2 AS LEV_CD, ");
                sql.append("    T1.NAME1 AS NAME, ");
                sql.append("    T1.ABBV1 AS LEVEL_MARK, ");
                sql.append("    T1.NAMESPARE1 AS LEVEL_MIN, ");
                sql.append("    T1.NAMESPARE2 AS LEVEL_MAX ");
                sql.append(" FROM NAME_MST T1 ");
                sql.append("    INNER JOIN NAME_YDAT T2 ON T2.YEAR = '" + year + "' AND T2.NAMECD1 = T1.NAMECD1 AND T2.NAMECD2 = T1.NAMECD2 ");
                sql.append(" WHERE ");
                sql.append("    T1.NAMECD1 = 'D001' ");
                sql.append(" ORDER BY ");
                sql.append("    T1.NAMESPARE1 ");
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String levCd = rs.getString("LEV_CD");
                    final String name = rs.getString("NAME");
                    final String levelMark = rs.getString("LEVEL_MARK");
                    final String levelMin = rs.getString("LEVEL_MIN");
                    final String levelMax = rs.getString("LEVEL_MAX");
                    ValuationLevel addwk = new ValuationLevel(levCd, name, levelMark, levelMin, levelMax);
                    rtnList.add(addwk);
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtnList;
        }
//        private String findValuationLevel(final BigDecimal asVal) {
//        	String retStr = "";
//        	if (asVal != null && _valuationLevList.size() > 0) {
//        		ValuationLevel getFstObj = (ValuationLevel)_valuationLevList.get(0);
//        		retStr = getFstObj._levelMark;
//        	    for (Iterator ite = _valuationLevList.iterator();ite.hasNext();) {
//        	    	ValuationLevel getObj = (ValuationLevel)ite.next();
//        		    BigDecimal getLowVal = new BigDecimal(getObj._levelMin);
//        		    if (getLowVal.compareTo(asVal) <= 0) {
//        			    retStr = getObj._levelMark;
//        		    } else {
//        			    break;
//        		    }
//        	    }
//        	}
//        	return retStr;
//        }

        private String getSemesterName(final DB2UDB db2) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT SEMESTERNAME FROM SEMESTER_MST WHERE YEAR = '" + _year + "' AND SEMESTER = '" + _semester + "' "));
        }

//        private Map getCertifInfo(final DB2UDB db2) {
//            final Map retMap = new TreeMap();
//            PreparedStatement ps = null;
//            ResultSet rs = null;
//            try {
//                ps = db2.prepareStatement(" SELECT * from CERTIF_SCHOOL_DAT WHERE YEAR = '" + _year + "' AND CERTIF_KINDCD = '104' ");
//                rs = ps.executeQuery();
//                if (rs.next()) {  //1レコードのはずなので、先頭データだけ取得
//                	final String kStr1 = "SCHOOL_NAME";
//                	retMap.put(kStr1, rs.getString(kStr1));
//                	final String kStr2 = "JOB_NAME";
//                	retMap.put(kStr2, rs.getString(kStr2));
//                	final String kStr3 = "PRINCIPAL_NAME";
//                	retMap.put(kStr3, rs.getString(kStr3));
//                }
//            } catch (SQLException ex) {
//                log.debug("getCertifInfo exception!", ex);
//            } finally {
//                DbUtils.closeQuietly(null, ps, rs);
//                db2.commit();
//            }
//            return retMap;
//        }

        private List getAssessMst(final DB2UDB db2) {
            final List retList = new ArrayList();
            StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("   ASSESSLEVEL, ");
            stb.append("   ASSESSMARK, ");
            stb.append("   ASSESSLOW, ");
            stb.append("   ASSESSHIGH ");
            stb.append(" FROM ");
            stb.append("   ASSESS_MST ");
            stb.append(" WHERE ");
            stb.append("   ASSESSCD = '4' ");
            stb.append(" ORDER BY ");
            stb.append("   ASSESSHIGH DESC, ");
            stb.append("   ASSESSMARK ");

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {  //1レコードのはずなので、先頭データだけ取得
                    final String assesslevel = rs.getString("ASSESSLEVEL");
                    final String assessmark = rs.getString("ASSESSMARK");
                    final String assesslow = rs.getString("ASSESSLOW");
                    final String assesshigh = rs.getString("ASSESSHIGH");
                    AssessDat addwk = new AssessDat(assesslevel, assessmark, assesslow, assesshigh);
                    retList.add(addwk);
                    log.info("assesslevel = " + assesslevel + " assessmark = " + assessmark + " assesslow = " + assesslow + " assesshigh = " + assesshigh );
                }
            } catch (SQLException ex) {
                log.debug("getCertifInfo exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retList;
        }

        private String findAssessMst(final BigDecimal asVal) {
            String retStr = "";
            if (asVal != null && _assessList.size() > 0) {
                AssessDat getFstObj = (AssessDat)_assessList.get(0);
                retStr = getFstObj._assessmark;
                for (Iterator ite = _assessList.iterator();ite.hasNext();) {
                    AssessDat getObj = (AssessDat)ite.next();
                    BigDecimal getLowVal = new BigDecimal(getObj._assesslow);
                    if (getLowVal.compareTo(asVal) <= 0) {
                        retStr = getObj._assessmark;
                    } else {
                        break;
                    }
                }
            }
            return retStr;
        }

    }
}

// eof
