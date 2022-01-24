/*
 * $Id$
 *
 * 作成日: 2021/05/07
 * 作成者: yogi
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJH;


import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
import servletpack.KNJZ.detail.KNJ_EditDate;

public class KNJH726{

    private static final Log log = LogFactory.getLog(KNJH726.class);

    private boolean _hasData;

    private static final String SUMSUBCLS = "1";
    private static final String SUMBUNRI = "2";

    private static final int PAGE_MAXCOL = 7;

    private static final int COL_INC = 22;
    private static final int COL_ABS = 23;
    private static final int COL_CNT = 24;
    private static final int COL_AVG = 25;
    private static final int COL_MAX = 26;
    private static final int COL_MIN = 27;
    private static final int COL_SDV = 28;

    private static final int COL_BUN = 1;
    private static final int COL_RI = 2;

    private static final String MERGE_CD1 = "12";
    private static final String MERGE_CD2 = "13";

    private static final String MERGED_CLASSCD = "12";
    private static final String MERGED_SUBCLASSCD = "120000";
    private Param _param;

    private static final int TTL_SUMBUNRI_MAXVAL = 800;
    private static final int TTL_SUMBUNRI_KOUSA = 40;
    private static final int TTL_MAXVAL = 200;
    private static final int TTL_KOUSA = 10;

    private static final int NML_SUMBUNRI_MAXVAL = 400;
    private static final int NML_SUMBUNRI_KOUSA = 20;
    private static final int NML_MAXVAL = 100;
    private static final int NML__KOUSA = 5;

    private static final String FREQ_ABSCD = "-1";
    private static final String FREQ_IGNORECD = "-99";

    private static final String NML_DELIMSTR = "-";
    private static final String FST_DELIMSTR = "@";
    private static final String SND_DELIMSTR = "#";

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
        final String fmtname = _param._totalChkFlg ? "KNJH726_2.frm" : "KNJH726.frm";
        svf.VrSetForm(fmtname, 1);

        final Map scoreMap1 = getScoreInfo(db2, false);
        final Map scoreMap2 = getScoreInfo(db2, true);
        if (scoreMap1.size() == 0) {
            return;
        }
        final Map totalSubclsMap = getTotalSubclsInfo(db2);
        final TotalBunriInfo totalBunriObj = getTotalBunriInfo(db2);
        final Map pageCutSubclsMap = pageCutSubcls(_param._subclsNameMap);
        if (pageCutSubclsMap == null) {
            return;
        }

        for (Iterator itp = pageCutSubclsMap.keySet().iterator();itp.hasNext();) {
            setTitle(svf);
            final Integer pageCnt = (Integer)itp.next();
            final Map subMap = (Map)pageCutSubclsMap.get(pageCnt);
            int subclsCnt = 0;
            for (Iterator ite = subMap.keySet().iterator();ite.hasNext();) {
                final String kStr = (String)ite.next();
                final subclsNameInf snObj = (subclsNameInf)_param._subclsNameMap.get(kStr);
                subclsCnt++;
                svf.VrsOut("SUBCLASS_NAME" + subclsCnt, snObj._subclassAbbv);
                putFrequency(svf, subclsCnt, SUMSUBCLS, kStr, scoreMap1, totalSubclsMap, totalBunriObj);
            }

            //文理の集計はページ関係なく出力
            Iterator itb = _param._bunriCdMap.keySet().iterator();
            if (_param._bunriCdMap.size() >= 1) {
                final String fstCd = (String)itb.next();
                putFrequency(svf, COL_BUN, SUMBUNRI, fstCd, scoreMap2, totalSubclsMap, totalBunriObj);
            }
            if (_param._bunriCdMap.size() >= 2) {
                final String sndCd = (String)itb.next();
                putFrequency(svf, COL_RI, SUMBUNRI, sndCd, scoreMap2, totalSubclsMap, totalBunriObj);
            }
            svf.VrEndPage();
            _hasData = true;
        }
    }

    private void setTitle(final Vrw32alp svf) {
        svf.VrsOut("TITLE", KNJ_EditDate.h_format_Seireki_N(_param._loginDate) + "度" + _param._testdivName + "集計結果表");
        svf.VrsOut("DATE", KNJ_EditDate.h_format_SeirekiJP(_param._loginDate));
        Iterator itb = _param._bunriCdMap.keySet().iterator();
        if (_param._bunriCdMap.size() > 0) {
            final String fstCd = (String)itb.next();
            final BunriInf putWk = (BunriInf)_param._bunriCdMap.get(fstCd);
            svf.VrsOut("COURSE_NAME1", putWk._name1);
        }
        if (_param._bunriCdMap.size() > 1) {
            final String sndCd = (String)itb.next();
            final BunriInf putWk = (BunriInf)_param._bunriCdMap.get(sndCd);
            svf.VrsOut("COURSE_NAME2", putWk._name1);
        }
    }

    private Map pageCutSubcls(final Map subclsNMap) {
        Map retMap = null;
        Map subMap = null;
        int subclsCnt = 0;
        int pageCnt = 1;

        for (Iterator ite = _param._subclsNameMap.keySet().iterator();ite.hasNext();) {
            final String kStr = (String)ite.next();
            final subclsNameInf snObj = (subclsNameInf)_param._subclsNameMap.get(kStr);
            if (subclsCnt >= PAGE_MAXCOL) {
                pageCnt++;
                subclsCnt = 0;
            }
            subclsCnt++;
            if (retMap == null) {
                retMap = new LinkedMap();
            }
            if (!retMap.containsKey(pageCnt)) {
                subMap = new LinkedMap();
                retMap.put(pageCnt, subMap);
            } else {
                subMap = (Map)retMap.get(pageCnt);
            }
            subMap.put(kStr, snObj);
        }
        return retMap;
    }

    private void putFrequency(final Vrw32alp svf, final int putCol, final String typeFlg, final String kStr, final Map scoreMap, final Map totalSubclsMap, final TotalBunriInfo totalBunriObj) {
        final String tblIdx;
        if (SUMSUBCLS.equals(typeFlg)) {
            tblIdx = "1";
        } else if (SUMBUNRI.equals(typeFlg)) {
            tblIdx = "2";
        } else {
            tblIdx = "";
            return;
        }
        final Map convMap = convertMap(typeFlg, kStr, scoreMap);
        int ttlCnt = 0;
        for (Iterator its = convMap.keySet().iterator();its.hasNext();) {
            final String idx = (String)its.next();
            final List detailList = (List)convMap.get(idx);
            if (!idx.equals(FREQ_IGNORECD)) {
                ttlCnt += detailList.size();
            }
        }
        int absCnt = 0;
        if (convMap.containsKey(FREQ_ABSCD)) {
            final List detailList = (List)convMap.get(FREQ_ABSCD);
            absCnt = detailList.size();
            svf.VrsOutn("NUM" + tblIdx + "_" + putCol, COL_ABS, String.valueOf(absCnt));  //欠席者数
        } else {
            svf.VrsOutn("NUM" + tblIdx + "_" + putCol, COL_ABS, "0");  //欠席者数
        }
        for (Iterator its = convMap.keySet().iterator();its.hasNext();) {
            final String idx = (String)its.next();
            if (idx.equals(FREQ_IGNORECD)) {
                continue;
            }
            final List detailList = (List)convMap.get(idx);
            final int idxVal = Integer.parseInt(idx);
            if (-1 < idxVal) {
                svf.VrsOutn("NUM" + tblIdx + "_" + putCol, idxVal + 1, String.valueOf(detailList.size())); //人数
                if (ttlCnt - absCnt > 0) {
                    BigDecimal perVal = new BigDecimal(detailList.size()).divide(new BigDecimal(ttlCnt - absCnt), 10, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).setScale(1, BigDecimal.ROUND_HALF_UP);
                    svf.VrsOutn("PER" + tblIdx + "_" + putCol, idxVal + 1, perVal.toString()); //%
                } else {
                    svf.VrsOutn("PER" + tblIdx + "_" + putCol, idxVal + 1, "0"); //%
                }
            }
        }

        if (SUMSUBCLS.equals(typeFlg)) {
            svf.VrsOutn("NUM1_" + putCol, COL_INC, String.valueOf(ttlCnt));  //申込者数
            svf.VrsOutn("NUM1_" + putCol, COL_CNT, String.valueOf(ttlCnt - absCnt));//対象者数
            if (totalSubclsMap.containsKey(kStr)) {
                final TotalSubclsInfo tsObj =(TotalSubclsInfo) totalSubclsMap.get(kStr);
                svf.VrsOutn("NUM1_" + putCol, COL_AVG, sisyago(tsObj._average, 1));//平均点
                svf.VrsOutn("NUM1_" + putCol, COL_MAX, sisyago(tsObj._max_Score, 1));//最高点
                svf.VrsOutn("NUM1_" + putCol, COL_MIN, sisyago(tsObj._min_Score, 1));//最低点
                svf.VrsOutn("NUM1_" + putCol, COL_SDV, sisyago(tsObj._stddev, 1));//標準偏差値
            }
        } else if (SUMBUNRI.equals(typeFlg)) {
            svf.VrsOutn("NUM2_" + putCol, COL_INC, "---");  //申込者数
            svf.VrsOutn("NUM2_" + putCol, COL_ABS, "---");  //欠席者数
            svf.VrsOutn("NUM2_" + putCol, COL_CNT, String.valueOf(ttlCnt - absCnt));//対象者数
            Iterator itb = _param._bunriCdMap.keySet().iterator();
            if (_param._bunriCdMap.size() > 0 && totalBunriObj != null) {
                final String fstCd = (String)itb.next();
                svf.VrsOutn("NUM2_" + putCol, COL_AVG, sisyago(kStr.equals(fstCd) ? totalBunriObj._bun_Average : totalBunriObj._ri_Average, 1));//平均点
                svf.VrsOutn("NUM2_" + putCol, COL_MAX, sisyago(kStr.equals(fstCd) ? totalBunriObj._max_Bun_Score : totalBunriObj._max_Ri_Score, 1));//最高点
                svf.VrsOutn("NUM2_" + putCol, COL_MIN, sisyago(kStr.equals(fstCd) ? totalBunriObj._min_Bun_Score : totalBunriObj._min_Ri_Score, 1));//最低点
                svf.VrsOutn("NUM2_" + putCol, COL_SDV, sisyago(kStr.equals(fstCd) ? totalBunriObj._bun_Stddev : totalBunriObj._ri_Stddev, 1));//標準偏差値
            }
        }
    }

    private String sisyago(final String valStr, final int scale) {
        if ("".equals(StringUtils.defaultString(valStr)) || !StringUtils.isNumeric(valStr)) {
            return "";
        }
        return new BigDecimal(valStr).setScale(scale, BigDecimal.ROUND_HALF_UP).toString();
    }

    private Map convertMap(final String typeFlg, final String kStr, final Map scoreMap) {
        Map retMap = new TreeMap();
        List detailList = new ArrayList();

        for (Iterator itt = scoreMap.keySet().iterator();itt.hasNext();) {
            final String fStr = (String)itt.next();
            if (SUMSUBCLS.equals(typeFlg)) {
                if (!fStr.endsWith(SND_DELIMSTR + kStr)) {
                    continue;
                }

            } else if (SUMBUNRI.equals(typeFlg)) {
                if (!fStr.startsWith(kStr + FST_DELIMSTR)) {
                    continue;
                }
            }
            final List subList = (List)scoreMap.get(fStr);
            for (Iterator itl = subList.iterator();itl.hasNext();) {
                final ScoreInfo sObj = (ScoreInfo)itl.next();
                final String sKey = chkKey(typeFlg, sObj);
                if (!retMap.containsKey(sKey)) {
                    detailList = new ArrayList();
                    retMap.put(sKey, detailList);
                } else {
                    detailList = (List)retMap.get(sKey);
                }
                detailList.add(sObj);
            }
        }
        return retMap;
    }

    //分布分けを行う。
    private String chkKey(final String typeFlg, final ScoreInfo sObj) {
        if (sObj == null) {
            return FREQ_IGNORECD;
        }
        if ("1".equals(sObj._absence_Flg_1)) {
            return FREQ_ABSCD;
        }
        if ("".equals(StringUtils.defaultString(sObj._score_1, "")) || !StringUtils.isNumeric(sObj._score_1)) {
            return FREQ_IGNORECD;
        }
        int maxVal = NML_MAXVAL;  //1回/2回毎の分布を標準とする
        int kousa = NML__KOUSA;
        if (_param._totalChkFlg) {
            if (SUMBUNRI.equals(typeFlg)) {
                maxVal = TTL_SUMBUNRI_MAXVAL;
                kousa = TTL_SUMBUNRI_KOUSA;
            } else {
                maxVal = TTL_MAXVAL;
                kousa = TTL_KOUSA;
            }
        } else {
            if (SUMBUNRI.equals(typeFlg)) {
                maxVal = NML_SUMBUNRI_MAXVAL;
                kousa = NML_SUMBUNRI_KOUSA;
            }
        }
        int cnt = 0;
        int sVal = Integer.parseInt(sObj._score_1);
        int cVal;
        for (cVal = maxVal;cVal >= 0;cVal = cVal - kousa) {
            if (cVal <= sVal) {
                break;
            }
            cnt++;
        }

        if (cVal < 0) {
            cnt = -1;
        }
        return String.valueOf(cnt);
    }

    private Map getScoreInfo(final DB2UDB db2, final boolean getClsCd) {
        final Map retMap = new LinkedMap();
        List subList = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final String sql = getScoreInfoSql(getClsCd);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String bunridiv = rs.getString("BUNRIDIV");
                final String schregno = rs.getString("SCHREGNO");
                final String score_1 = rs.getString("SCORE_1");
                final String absence_Flg_1 = rs.getString("ABSENCE_FLG_1");
                if (!getClsCd) {
                    final String classcd = rs.getString("CLASSCD");
                    final String subclasscd = rs.getString("SUBCLASSCD");
                    final String fKey = bunridiv + FST_DELIMSTR + schregno + SND_DELIMSTR + classcd + NML_DELIMSTR + subclasscd;
                    final ScoreInfo addWk = new ScoreInfo(bunridiv, classcd, subclasscd, schregno, score_1, absence_Flg_1);
                    if (!retMap.containsKey(fKey)) {
                        subList = new ArrayList();
                        retMap.put(fKey, subList);
                    } else {
                        subList = (List)retMap.get(fKey);
                    }
                    subList.add(addWk);
                } else {
                    final ScoreInfo addWk = new ScoreInfo(bunridiv, null, null, schregno, score_1, absence_Flg_1);
                    final String sKey = bunridiv + FST_DELIMSTR + schregno;
                    if (!retMap.containsKey(sKey)) {
                        subList = new ArrayList();
                        retMap.put(sKey, subList);
                    } else {
                        subList = (List)retMap.get(sKey);
                    }
                    subList.add(addWk);
                }
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
        return retMap;
    }

    private String getScoreInfoSql(final boolean ignoreSubcls) {
        final StringBuffer stb = new StringBuffer();
        //コード変換(CLASSCD=12,13のデータは、特定コードに変換する)
        if (!ignoreSubcls) {
            stb.append(" WITH CDFIX_1213 AS (");
            stb.append(" SELECT ");
            stb.append("   T1.BUNRIDIV, ");
            stb.append("   CASE WHEN T1.CLASSCD IS NOT NULL THEN '" + MERGED_CLASSCD + "' ELSE NULL END AS CLASSCD, ");
            stb.append("   CASE WHEN T1.CLASSCD IS NOT NULL THEN '" + MERGED_SUBCLASSCD + "' ELSE NULL END AS SUBCLASSCD, ");
            stb.append("   CASE WHEN T1.CLASSCD IS NOT NULL THEN T2.SCHREGNO ELSE NULL END AS SCHREGNO, ");
            stb.append("   T2.SCORE_1, ");
            stb.append("   T2.ABSENCE_FLG_1 ");
            stb.append(" FROM ");
            stb.append("   ACADEMICTEST_SUBCLASS_DAT T1 ");
            stb.append("   LEFT JOIN ACADEMICTEST_SCORE_DAT T2 ");
            stb.append("     ON T2.YEAR = T1.YEAR ");
            stb.append("    AND T2.CLASSCD = T1.CLASSCD ");
            stb.append("    AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("    AND T2.TESTDIV = T1.TESTDIV ");
            stb.append(" WHERE ");
            stb.append("   T1.YEAR = '" + _param._year + "' ");
            stb.append("   AND T1.CLASSCD IN ('" + MERGE_CD1 + "', '" + MERGE_CD2 + "') ");
            stb.append("   AND T2.SCHREGNO IS NOT NULL ");
            if (!_param._totalChkFlg) {
                stb.append("    AND T1.TESTDIV = '" + _param._testDiv + "' ");
            }
            stb.append(" ) ");
            //CLASSCD=12,13の複数レコードを集計
            stb.append(" SELECT ");
            stb.append("   T1.BUNRIDIV, ");
            stb.append("   T1.CLASSCD, ");
            stb.append("   T1.SUBCLASSCD, ");
            stb.append("   T1.SCHREGNO, ");
            stb.append("   SUM(T1.SCORE_1) AS SCORE_1, ");
            stb.append("   CASE WHEN SUM(CASE WHEN T1.ABSENCE_FLG_1 = '1' THEN 1 ELSE 0 END) > 0 THEN 1 ELSE 0 END AS ABSENCE_FLG_1 ");
            stb.append(" FROM ");
            stb.append("   CDFIX_1213 T1 ");
            stb.append(" GROUP BY ");
            stb.append("   T1.BUNRIDIV, ");
            stb.append("   T1.CLASSCD, ");
            stb.append("   T1.SUBCLASSCD, ");
            stb.append("   T1.SCHREGNO ");
            stb.append(" UNION ALL ");
        }
        //CLASSCD=12,13以外
        stb.append(" SELECT ");
        stb.append("   T1.BUNRIDIV, ");
        if (!ignoreSubcls) {
            stb.append("   T1.CLASSCD, ");
            stb.append("   T1.SUBCLASSCD, ");
        }
        stb.append("   T2.SCHREGNO, ");
        stb.append("   SUM(T2.SCORE_1) AS SCORE_1, ");
        stb.append("   CASE WHEN SUM(CASE WHEN T2.ABSENCE_FLG_1 = '1' THEN 1 ELSE 0 END) > 0 THEN 1 ELSE 0 END AS ABSENCE_FLG_1 ");
        stb.append(" FROM ");
        stb.append("   ACADEMICTEST_SUBCLASS_DAT T1 ");
        stb.append("   LEFT JOIN ACADEMICTEST_SCORE_DAT T2 ");
        stb.append("     ON T2.YEAR = T1.YEAR ");
        stb.append("    AND T2.CLASSCD = T1.CLASSCD ");
        stb.append("    AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("    AND T2.TESTDIV = T1.TESTDIV ");
        stb.append(" WHERE ");
        stb.append("   T1.YEAR = '" + _param._year + "' ");
        if (ignoreSubcls) {
            stb.append("   AND T1.CLASSCD NOT IN ('" + MERGE_CD1 + "', '" + MERGE_CD2 + "') ");
            stb.append("   AND T2.SCHREGNO IS NOT NULL ");
        }
        if (!_param._totalChkFlg) {
            stb.append("    AND T1.TESTDIV = '" + _param._testDiv + "' ");
        }
        stb.append(" GROUP BY ");
        stb.append("   T1.BUNRIDIV, ");
        if (!ignoreSubcls) {
            stb.append("   T1.CLASSCD, ");
            stb.append("   T1.SUBCLASSCD, ");
        }
        stb.append("   T2.SCHREGNO ");
        stb.append(" ORDER BY ");
        stb.append("   BUNRIDIV, ");
        if (!ignoreSubcls) {
            stb.append("   CLASSCD, ");
            stb.append("   SUBCLASSCD, ");
        }
        stb.append("   SCHREGNO ");
        return stb.toString();
    }

    private Map getTotalSubclsInfo(final DB2UDB db2) {
        final Map retMap = new LinkedMap();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final String sql = getTotalSubclsInfoSql();
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String classCd = rs.getString("CLASSCD");
                final String subclassCd = rs.getString("SUBCLASSCD");
                final String base_Cnt = rs.getString("BASE_CNT");
                final String abs_Cnt = rs.getString("ABS_CNT");
                final String average = rs.getString("AVERAGE");
                final String max_Score = rs.getString("MAX_SCORE");
                final String min_Score = rs.getString("MIN_SCORE");
                final String stddev = rs.getString("STDDEV");
                final String fKey = classCd + NML_DELIMSTR + subclassCd;
                final TotalSubclsInfo addWk = new TotalSubclsInfo(classCd, subclassCd, base_Cnt, abs_Cnt, average, max_Score, min_Score, stddev);
                retMap.put(fKey, addWk);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
        return retMap;
    }

    private String getTotalSubclsInfoSql() {
        final StringBuffer stb = new StringBuffer();
        //コード変換(CLASSCD=12,13のデータは、特定コードに変換する)
        stb.append(" WITH CDFIX_1213 AS (");
        stb.append(" SELECT ");
        stb.append("   T1.YEAR, ");
        stb.append("   CASE WHEN T1.CLASSCD IS NOT NULL THEN '" + MERGED_CLASSCD + "' ELSE NULL END AS CLASSCD, ");
        stb.append("   CASE WHEN T1.CLASSCD IS NOT NULL THEN '" + MERGED_SUBCLASSCD + "' ELSE NULL END AS SUBCLASSCD, ");
        stb.append("   T1.SCHREGNO, ");
        stb.append("   T2.ABSENCE_FLG_1 ");
        stb.append(" FROM ");
        stb.append("   SCHREG_ACADEMICTEST_DAT T1 ");
        stb.append("   LEFT JOIN ( ");
        stb.append("        SELECT DISTINCT ");
        stb.append("          TX2.YEAR, ");
        stb.append("          TX2.CLASSCD, ");
        stb.append("          TX2.SUBCLASSCD, ");
        stb.append("          TX2.SCHREGNO, ");
        stb.append("          TX2.ABSENCE_FLG_1 ");
        stb.append("        FROM ");
        stb.append("          ACADEMICTEST_SCORE_DAT TX2 ");
        stb.append("        WHERE ");
        stb.append("          TX2.YEAR = '" + _param._year + "' ");
        if (!_param._totalChkFlg) {
            stb.append("          AND TX2.TESTDIV = '" + _param._testDiv + "' ");
        }
        stb.append("   ) T2 ");
        stb.append("     ON T2.YEAR = T1.YEAR ");
        stb.append("    AND T2.CLASSCD = T1.CLASSCD ");
        stb.append("    AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("    AND T2.SCHREGNO = T1.SCHREGNO ");
        stb.append(" WHERE ");
        stb.append("   T1.YEAR = '" + _param._year + "' ");
        stb.append("   AND T1.DECLINE_FLG IS NULL ");
        stb.append("   AND T1.CLASSCD IN ('" + MERGE_CD1 + "', '" + MERGE_CD2 + "') ");
        stb.append(" ), SYUKKETU_TBL AS ( ");
        //CLASSCD=12,13の複数レコードを集計
        stb.append(" SELECT ");
        stb.append("   T0.YEAR, ");
        stb.append("   T0.CLASSCD, ");
        stb.append("   T0.SUBCLASSCD, ");
        stb.append("   COUNT(T0.SCHREGNO) AS BASE_CNT, ");
        stb.append("   COUNT(T0.ABSENCE_FLG_1) AS ABS_CNT ");
        stb.append(" FROM ");
        stb.append("   CDFIX_1213 T0 ");
        stb.append(" GROUP BY ");
        stb.append("   T0.YEAR, ");
        stb.append("   T0.CLASSCD, ");
        stb.append("   T0.SUBCLASSCD ");
        stb.append(" UNION ALL ");
        //CLASSCD=12,13以外
        stb.append(" SELECT ");
        stb.append("   T1.YEAR, ");
        stb.append("   T1.CLASSCD, ");
        stb.append("   T1.SUBCLASSCD, ");
        stb.append("   COUNT(T1.SCHREGNO) AS BASE_CNT, ");
        stb.append("   COUNT(T2.ABSENCE_FLG_1) AS ABS_CNT ");
        stb.append(" FROM ");
        stb.append("   SCHREG_ACADEMICTEST_DAT T1 ");
        stb.append("   LEFT JOIN ( ");
        stb.append("        SELECT DISTINCT ");
        stb.append("          TX2.YEAR, ");
        stb.append("          TX2.CLASSCD, ");
        stb.append("          TX2.SUBCLASSCD, ");
        stb.append("          TX2.SCHREGNO, ");
        stb.append("          TX2.ABSENCE_FLG_1 ");
        stb.append("        FROM ");
        stb.append("          ACADEMICTEST_SCORE_DAT TX2 ");
        stb.append("        WHERE ");
        stb.append("          TX2.YEAR = '" + _param._year + "' ");
        if (!_param._totalChkFlg) {
            stb.append("          AND TX2.TESTDIV = '" + _param._testDiv + "' ");
        }
        stb.append("   ) T2 ");
        stb.append("     ON T2.YEAR = T1.YEAR ");
        stb.append("    AND T2.CLASSCD = T1.CLASSCD ");
        stb.append("    AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("    AND T2.SCHREGNO = T1.SCHREGNO ");
        stb.append(" WHERE ");
        stb.append("   T1.YEAR = '" + _param._year + "' ");
        stb.append("   AND T1.DECLINE_FLG IS NULL ");
        stb.append("   AND T1.CLASSCD NOT IN ('" + MERGE_CD1 + "', '" + MERGE_CD2 + "') ");
        stb.append(" GROUP BY ");
        stb.append("   T1.YEAR, ");
        stb.append("   T1.CLASSCD, ");
        stb.append("   T1.SUBCLASSCD ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("   T1.CLASSCD, ");
        stb.append("   T1.SUBCLASSCD, ");
        stb.append("   T1.BASE_CNT, ");
        stb.append("   T1.ABS_CNT, ");
        stb.append("   T3.AVERAGE, ");
        stb.append("   T3.MAX_SCORE, ");
        stb.append("   T3.MIN_SCORE, ");
        stb.append("   T3.STDDEV ");
        stb.append(" FROM ");
        stb.append("   SYUKKETU_TBL T1 ");
        stb.append("   LEFT JOIN ACADEMICTEST_SUBCLASS_STAT_DAT T3 ");
        stb.append("     ON T3.YEAR = T1.YEAR ");
        stb.append("    AND T3.CLASSCD = T1.CLASSCD ");
        stb.append("    AND T3.SUBCLASSCD = T1.SUBCLASSCD ");
        if (!_param._totalChkFlg) {
            stb.append("    AND T3.TESTDIV = '" + _param._testDiv + "' ");
        }
        stb.append(" ORDER BY ");
        stb.append("   T1.CLASSCD, ");
        stb.append("   T1.SUBCLASSCD ");
        return stb.toString();
    }

    private TotalBunriInfo getTotalBunriInfo(final DB2UDB db2) {
        TotalBunriInfo retInfo = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final String sql = getTotalBunriInfoSql();
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            if (rs.next()) {
                final String bun_Average = rs.getString("BUN_AVERAGE");
                final String max_Bun_Score = rs.getString("MAX_BUN_SCORE");
                final String min_Bun_Score = rs.getString("MIN_BUN_SCORE");
                final String bun_Stddev = rs.getString("BUN_STDDEV");
                final String ri_Average = rs.getString("RI_AVERAGE");
                final String max_Ri_Score = rs.getString("MAX_RI_SCORE");
                final String min_Ri_Score = rs.getString("MIN_RI_SCORE");
                final String ri_Stddev = rs.getString("RI_STDDEV");
                retInfo = new TotalBunriInfo(bun_Average, max_Bun_Score, min_Bun_Score, bun_Stddev, ri_Average, max_Ri_Score, min_Ri_Score, ri_Stddev);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
        return retInfo;
    }

    private String getTotalBunriInfoSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("   SUM(BUN_AVERAGE) AS BUN_AVERAGE, ");
        stb.append("   SUM(MAX_BUN_SCORE) AS MAX_BUN_SCORE, ");
        stb.append("   SUM(MIN_BUN_SCORE) AS MIN_BUN_SCORE, ");
        stb.append("   SUM(BUN_STDDEV) AS BUN_STDDEV, ");
        stb.append("   SUM(RI_AVERAGE) AS RI_AVERAGE, ");
        stb.append("   SUM(MAX_RI_SCORE) AS MAX_RI_SCORE, ");
        stb.append("   SUM(MIN_RI_SCORE) AS MIN_RI_SCORE, ");
        stb.append("   SUM(RI_STDDEV) AS RI_STDDEV ");
        stb.append(" FROM ");
        stb.append("   ACADEMICTEST_TOTAL_STAT_DAT ");
        stb.append(" WHERE ");
        stb.append("   YEAR = '" + _param._year + "' ");
        if (!_param._totalChkFlg) {
            stb.append("    AND TESTDIV = '" + _param._testDiv + "' ");
        }
        return stb.toString();
    }

    private class ScoreInfo {
        final String _bunriDiv;
        final String _classCd;
        final String _subclassCd;
        final String _schregno;
        final String _score_1;
        final String _absence_Flg_1;
        public ScoreInfo (final String bunriDiv, final String classCd, final String subclassCd, final String schregno, final String score_1, final String absence_Flg_1)
        {
            _bunriDiv = bunriDiv;
            _classCd = classCd;
            _subclassCd = subclassCd;
            _schregno = schregno;
            _score_1 = score_1;
            _absence_Flg_1 = absence_Flg_1;
        }
    }

    private class TotalSubclsInfo {
        final String _classCd;
        final String _subclassCd;
        final String _base_Cnt;
        final String _abs_Cnt;
        final String _average;
        final String _max_Score;
        final String _min_Score;
        final String _stddev;
        public TotalSubclsInfo (final String classCd, final String subclassCd, final String base_Cnt, final String abs_Cnt, final String average, final String max_Score, final String min_Score, final String stddev)
        {
            _classCd = classCd;
            _subclassCd = subclassCd;
            _base_Cnt = base_Cnt;
            _abs_Cnt = abs_Cnt;
            _average = average;
            _max_Score = max_Score;
            _min_Score = min_Score;
            _stddev = stddev;
        }
    }

    private class TotalBunriInfo {
        final String _bun_Average;
        final String _max_Bun_Score;
        final String _min_Bun_Score;
        final String _bun_Stddev;
        final String _ri_Average;
        final String _max_Ri_Score;
        final String _min_Ri_Score;
        final String _ri_Stddev;
        public TotalBunriInfo (final String bun_Average, final String max_Bun_Score, final String min_Bun_Score, final String bun_Stddev, final String ri_Average, final String max_Ri_Score, final String min_Ri_Score, final String ri_Stddev)
        {
            _bun_Average = bun_Average;
            _max_Bun_Score = max_Bun_Score;
            _min_Bun_Score = min_Bun_Score;
            _bun_Stddev = bun_Stddev;
            _ri_Average = ri_Average;
            _max_Ri_Score = max_Ri_Score;
            _min_Ri_Score = min_Ri_Score;
            _ri_Stddev = ri_Stddev;
        }
    }

    private class subclsNameInf {
        final String _classCd;
        final String _subclassCd;
        final String _subclassName;
        final String _subclassAbbv;
        public subclsNameInf (final String classCd, final String subclassCd, final String subclassName, final String subclassAbbv)
        {
            _classCd = classCd;
            _subclassCd = subclassCd;
            _subclassName = subclassName;
            _subclassAbbv = subclassAbbv;
        }
    }
    private class BunriInf{
        final String _name1;
        final String _nameCd2;
        public BunriInf(final String nameCd2, final String name1) {
            _name1 = name1;
            _nameCd2 = nameCd2;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 71866 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _testDiv;
        private final String _loginDate;
        private final boolean _totalChkFlg;
        private final String _testdivName;
        private final Map _subclsNameMap;
        private final Map _bunriCdMap;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _testDiv = request.getParameter("TESTDIV");
            _loginDate = request.getParameter("LOGIN_DATE");
            _totalChkFlg = "1".equals(request.getParameter("TOTALCHKBOX"));
            _testdivName = _totalChkFlg ? "学力テスト合計" : getNameMst(db2, "NAME1", "H320", _testDiv);
            _subclsNameMap = getSubclsNameMap(db2);
            if (_subclsNameMap.size() < 1) {
                log.warn("SUBCLSCODE is not existed. you must set ACADEMICTEST_SUBCLASS_DAT.");
            }
            _bunriCdMap = getBunriInfMap(db2);
            if (_bunriCdMap.size() < 2) {
                log.warn("CODE is not existed. you must set V_NAME_MST H319.");
            }
        }

        private Map getBunriInfMap(final DB2UDB db2) {
            Map rtnMap = new LinkedMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                //名称M_H319の文理コードの配列格納インデックス(NAMECD2が、文→理の順で設定される想定)
                //コード自体が変化するので、担保されるのは設定コード順のみ、と想定して作成。
                ps = db2.prepareStatement(" SELECT NAMECD2, NAME1 FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'H319' ORDER BY NAMECD2");
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String name1 = rs.getString("NAME1");
                    final String nameCd2 = rs.getString("NAMECD2");
                    BunriInf addWk = new BunriInf(nameCd2, name1);
                    rtnMap.put(nameCd2, addWk);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return rtnMap;
        }

        private String getNameMst(final DB2UDB db2, final String field, final String namecd1, final String namecd2) {
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT " + field + " FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = '" + namecd1 + "' AND NAMECD2 = '" + namecd2 + "' ");
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString(field);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return rtn;
        }

        private Map getSubclsNameMap(final DB2UDB db2) {
            final Map retMap = new LinkedMap();
            final StringBuffer stb = new StringBuffer();
            //コード変換(CLASSCD=12,13のデータは、集約する)
            stb.append(" WITH CDFIX_1213 AS (");
            stb.append(" SELECT DISTINCT ");
            stb.append("   CASE WHEN T1.CLASSCD IS NOT NULL THEN '" + MERGED_CLASSCD + "' ELSE NULL END AS CLASSCD, ");
            stb.append("   CASE WHEN T1.CLASSCD IS NOT NULL THEN '" + MERGED_SUBCLASSCD + "' ELSE NULL END AS SUBCLASSCD, ");
            stb.append("   T1.SUBCLASSCD AS SORT_ORDERCD, ");
            stb.append("   T1.SUBCLASSNAME, ");
            stb.append("   T1.SUBCLASSABBV ");
            stb.append(" FROM ");
            stb.append("   ACADEMICTEST_SUBCLASS_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("   T1.YEAR = '" + _year + "' ");
            stb.append("   AND CLASSCD IN ('" + MERGE_CD1 + "', '" + MERGE_CD2 + "') ");
            if (!_totalChkFlg) {
                stb.append("    AND T1.TESTDIV = '" + _testDiv + "' ");
            }
            stb.append(" ) ");
            //CLASSCD=12,13のデータを集約する(CLASSCD=12,13の名称をまとめて1つにする。)
            stb.append(" SELECT ");
            stb.append("   T1.CLASSCD, ");
            stb.append("   T1.SUBCLASSCD, ");
            stb.append("   LISTAGG(T1.SUBCLASSNAME, '・') WITHIN GROUP(order BY T1.SORT_ORDERCD) AS SUBCLASSNAME, ");
            stb.append("   LISTAGG(T1.SUBCLASSABBV, '・') WITHIN GROUP(order BY T1.SORT_ORDERCD) AS SUBCLASSABBV ");
            stb.append(" FROM ");
            stb.append("   CDFIX_1213 T1 ");
            stb.append(" GROUP BY ");
            stb.append("   T1.CLASSCD, ");
            stb.append("   T1.SUBCLASSCD ");
            stb.append(" UNION ALL ");
            //CLASSCD=12,13以外
            stb.append(" SELECT DISTINCT ");
            stb.append("   T1.CLASSCD, ");
            stb.append("   T1.SUBCLASSCD, ");
            stb.append("   T1.SUBCLASSNAME, ");
            stb.append("   T1.SUBCLASSABBV ");
            stb.append(" FROM ");
            stb.append("   ACADEMICTEST_SUBCLASS_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("   T1.YEAR = '" + _year + "' ");
            stb.append("   AND CLASSCD NOT IN ('" + MERGE_CD1 + "', '" + MERGE_CD2 + "') ");
            if (!_totalChkFlg) {
                stb.append("    AND T1.TESTDIV = '" + _testDiv + "' ");
            }
            stb.append(" ORDER BY ");
            stb.append("   CLASSCD, ");
            stb.append("   SUBCLASSCD ");
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String classCd = rs.getString("CLASSCD");
                    final String subclassCd = rs.getString("SUBCLASSCD");
                    final String subclassName = rs.getString("SUBCLASSNAME");
                    final String subclassAbbv = rs.getString("SUBCLASSABBV");
                    final String fKey = classCd + NML_DELIMSTR + subclassCd;
                    subclsNameInf addWk = new subclsNameInf(classCd, subclassCd, subclassName, subclassAbbv);
                    //TESTDIV違いで重複したら、先頭のみ取得
                    if (!retMap.containsKey(fKey)) {
                        retMap.put(fKey, addWk);
                    }
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return retMap;
        }
    }
}

// eof

