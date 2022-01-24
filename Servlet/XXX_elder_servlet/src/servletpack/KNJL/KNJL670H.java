/*
 * $Id: 956fc909cc383ebb8f68ef4a18c16e2c1f707e9c $
 *
 * 作成日: 2020/10/27
 * 作成者: yogi
 *
 * Copyright(C) 2020 SATT Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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

/**
 *  学校教育システム 賢者 [入試管理]
 *
 *                  ＜ＫＮＪＬ６７０Ｈ＞  合格基準点種別合格者数一覧表
 **/
public class KNJL670H {

    private static final Log log = LogFactory.getLog(KNJL670H.class);

    private static final String HEIGAN = "H";  //併願
    private static final String NRML = "F";    //一般

	private static final String COMMITFLG = "1";
    private static final String TEST_SUBJ = "02";   //科目試験あり
    private static final String KAKUYAKU = "99";

    private static final int KAKUYAKU_PRTCNT = 7;

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
            if (_hasData) {
            	svf.VrEndPage();
            }
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
    	if (!TEST_SUBJ.equals(_param._testdiv)) {  //小論文のみ(点数の入らない試験)は、出力しない。
    		return;
    	}
    	Date dateObj = new Date();
    	SimpleDateFormat format = new SimpleDateFormat( "HH:mm:ss" );
    	// 日時情報を指定フォーマットの文字列で取得
    	String dispTime = format.format( dateObj );
    	svf.VrSetForm("KNJL670H.frm", 1);
        Map retMap = new LinkedMap();
        final boolean prtJdgFlg = getSqlData(db2, retMap);
        if (!prtJdgFlg) {  //シミュレーションの元となる、併願、一般の人数が全て0の場合は出力しない。
        	return;
        }
        int lineMax = 50;
        int lineCnt = 1;
        int pageCnt = 1;
    	setTitle(db2, svf, pageCnt, dispTime);
        for (Iterator ite = retMap.keySet().iterator();ite.hasNext();) {
            final String asikiri = (String)ite.next();
            final Map subMap = (Map)retMap.get(asikiri);
            if (lineMax < lineCnt) {
            	svf.VrEndPage();
            	lineCnt = 1;
            	pageCnt++;
            	setTitle(db2, svf, pageCnt, dispTime);
            }

    		//log.info("------ Record ------" + asikiri);
    		svf.VrsOutn("PASS_MIN_SCORE", lineCnt, asikiri);

    		BigDecimal fBase = new BigDecimal(0.0);
            for (Iterator its = subMap.keySet().iterator();its.hasNext();) {
                final String classify_Cd = (String)its.next();
                final PrintData prtWk = (PrintData)subMap.get(classify_Cd);
                final Map row = (Map)_param._classifyMap.get(classify_Cd);
                //log.info("------ Record_Mark ------" + KnjDbUtils.getString(row, "MARK"));
                if (!HEIGAN.equals(KnjDbUtils.getString(row, "MARK")) && !NRML.equals(KnjDbUtils.getString(row, "MARK"))) {
                    if (KAKUYAKU.equals(classify_Cd)) {  //※ソース内部利用コード
                        //確約(確約フラグが立っている物の合計)
                        svf.VrsOutn("COMMIT_TOTAL", lineCnt, defStr(prtWk._s_Cnt, ""));
                        if (prtWk._s_Cnt != null) {
                            fBase = fBase.add(new BigDecimal(prtWk._s_Cnt));
                        }
                    } else {
                        int prtFstIdx = 1;
                        boolean findFlg = false;
                        for (Iterator itc = _param._classifyMap.keySet().iterator();itc.hasNext();) {
                            final String kStr = (String)itc.next();
                            final Map rWk = (Map)_param._classifyMap.get(kStr);
                            if (HEIGAN.equals(KnjDbUtils.getString(rWk, "MARK")) && NRML.equals(KnjDbUtils.getString(rWk, "MARK"))) {
                            	continue;
                            }
                            if (kStr.equals(classify_Cd)) {
                                findFlg = true;
                                break;
                            }
                            prtFstIdx++;
                        }
                        if (findFlg) {
                            svf.VrsOutn("COMMIT_NUM" + prtFstIdx, lineCnt, defStr(prtWk._s_Cnt, ""));
                        }
                    }
            	} else if (HEIGAN.equals(KnjDbUtils.getString(row, "MARK"))) {
            		//併願類別
            		svf.VrsOutn("CON_APPLI_NUM1", lineCnt, defStr(prtWk._h_Cnt, ""));
            		svf.VrsOutn("CON_APPLI_NUM2", lineCnt, defStr(prtWk._h1_Cnt, ""));
            		svf.VrsOutn("CON_APPLI_NUM3", lineCnt, defStr(prtWk._h2_Cnt, ""));
            		svf.VrsOutn("CON_APPLI_NUM4", lineCnt, defStr(prtWk._h1_Per, ""));
            		svf.VrsOutn("CON_APPLI_NUM5", lineCnt, defStr(prtWk._h2_Per, ""));
            		svf.VrsOutn("CON_APPLI_NUM6", lineCnt, defStr(prtWk._h12_Per, ""));
            		if (prtWk._h12_Per != null) {
            		    fBase = fBase.add(new BigDecimal(prtWk._h12_Per));
            		}
            	} else if (NRML.equals(KnjDbUtils.getString(row, "MARK"))) {
            		//一般
            		svf.VrsOutn("GENERAL_NUM1", lineCnt, defStr(prtWk._f_Cnt, ""));      //F
            		svf.VrsOutn("GENERAL_NUM2", lineCnt, defStr(prtWk._f1_Cnt, ""));     //F1
            		svf.VrsOutn("GENERAL_NUM3", lineCnt, defStr(prtWk._f2_Cnt, ""));     //F2
            		svf.VrsOutn("GENERAL_NUM4", lineCnt, defStr(prtWk._f1A_Per, ""));    //F1A%
            		svf.VrsOutn("GENERAL_NUM5", lineCnt, defStr(prtWk._f2A1_Per, ""));   //F2A1%
            		svf.VrsOutn("GENERAL_NUM6", lineCnt, defStr(prtWk._f2A2_Per, ""));   //F2A2%
            		svf.VrsOutn("GENERAL_NUM7", lineCnt, defStr(prtWk._f2A3_Per, ""));   //F2A3%
            		svf.VrsOutn("GENERAL_NUM8", lineCnt, fBase.add(new BigDecimal(defStr(prtWk._f1A_Per, "0"))).add(new BigDecimal(defStr(prtWk._f2A1_Per, "0"))).toString());  //28
            		svf.VrsOutn("GENERAL_NUM9", lineCnt, fBase.add(new BigDecimal(defStr(prtWk._f1A_Per, "0"))).add(new BigDecimal(defStr(prtWk._f2A2_Per, "0"))).toString());  //29
            		svf.VrsOutn("GENERAL_NUM10", lineCnt, fBase.add(new BigDecimal(defStr(prtWk._f1A_Per, "0"))).add(new BigDecimal(defStr(prtWk._f2A3_Per, "0"))).toString()); //30
            		svf.VrsOutn("GENERAL_NUM11", lineCnt, defStr(prtWk._f1B_Per, ""));   //F1B%
            		svf.VrsOutn("GENERAL_NUM12", lineCnt, fBase.add(new BigDecimal(defStr(prtWk._f1B_Per, "0"))).add(new BigDecimal(defStr(prtWk._f2A1_Per, "0"))).toString()); //32
            		svf.VrsOutn("GENERAL_NUM13", lineCnt, fBase.add(new BigDecimal(defStr(prtWk._f1B_Per, "0"))).add(new BigDecimal(defStr(prtWk._f2A2_Per, "0"))).toString()); //33
            		svf.VrsOutn("GENERAL_NUM14", lineCnt, fBase.add(new BigDecimal(defStr(prtWk._f1B_Per, "0"))).add(new BigDecimal(defStr(prtWk._f2A3_Per, "0"))).toString()); //34
            	}
            	_hasData = true;
            }
            lineCnt++;
        }
    }
    private String defStr(final String str, final String defs) {
    	return StringUtils.defaultString(str, defs);
    }

    private void setTitle(final DB2UDB db2, final Vrw32alp svf, final int pageCnt, final String dispTime) {
    	svf.VrsOut("TITLE", _param._entexamYear + "年度 合格基準点種別合格者数一覧表 " + _param._testdivName);
    	svf.VrsOut("PAGE", String.valueOf(pageCnt));
    	svf.VrsOut("DATE", _param._ctrlDate + " " + dispTime);
    	int cnt = 1;
    	String hStr = HEIGAN;
    	String fStr = NRML;
    	for (Iterator ite = _param._classifyMap.keySet().iterator();ite.hasNext();) {
    		final String ccd = (String)ite.next();
    		final Map row = (Map)_param._classifyMap.get(ccd);
    		final String mark = KnjDbUtils.getString(row, "CLASSIFY_NAME");
    		if (!HEIGAN.equals(mark) && !NRML.equals(mark)) {
    	        svf.VrsOut("COMMIT_NAME" + cnt, KnjDbUtils.getString(row, "CLASSIFY_NAME"));
        	    cnt++;
    		}
    	}
    	svf.VrsOut("CON_APPLI_NAME1", hStr);
    	svf.VrsOut("CON_APPLI_NAME2", hStr+"1");
    	svf.VrsOut("CON_APPLI_NAME3", hStr+"2");
    	svf.VrsOut("CON_APPLI_NAME4", hStr+"1 " + _param._heiganP1 + "%");
    	svf.VrsOut("CON_APPLI_NAME5", hStr+"2 " + _param._heiganP2 + "%");
    	svf.VrsOut("CON_APPLI_NAME6", hStr+"12");

    	svf.VrsOut("GENERAL_NAME1", fStr);
    	svf.VrsOut("GENERAL_NAME2", fStr + "1無");
    	svf.VrsOut("GENERAL_NAME3", fStr + "2延");
    	svf.VrsOut("GENERAL_NAME4", fStr + "1 " + _param._nrmlP1 + "%");
    	svf.VrsOut("GENERAL_NAME5", fStr + "2 " + _param._ennouP1 + "%");
    	svf.VrsOut("GENERAL_NAME6", fStr + "2 " + _param._ennouP2 + "%");
    	svf.VrsOut("GENERAL_NAME7", fStr + "2 " + _param._ennouP3 + "%");
    	svf.VrsOut("GENERAL_NAME8", _param._ennouP1 + "%計");
    	svf.VrsOut("GENERAL_NAME9", _param._ennouP2 + "%計");
    	svf.VrsOut("GENERAL_NAME10", _param._ennouP3 + "%計");
    	svf.VrsOut("GENERAL_NAME11", fStr + "1 " + _param._nrmlP2 + "%");
    	svf.VrsOut("GENERAL_NAME12", _param._ennouP1 + "%計");
    	svf.VrsOut("GENERAL_NAME13", _param._ennouP2 + "%計");
    	svf.VrsOut("GENERAL_NAME14", _param._ennouP3 + "%計");
    }

    private boolean getSqlData(final DB2UDB db2, final Map retMap) {
        List asikiriList = new ArrayList();
        BigDecimal fullCntWk = new BigDecimal("0");
        for (int ii = _param._strtLine;ii <= _param._endLine;ii++ ) {
        	if (asikiriList.size() == 10) {
        		final Map subMap = getSql10Data(db2, asikiriList);
        		for (Iterator ite = subMap.keySet().iterator();ite.hasNext();) {
        			final String kStr = (String)ite.next();
        			final Map wkMap = (Map)subMap.get(kStr);
        			for (Iterator itr = wkMap.keySet().iterator();itr.hasNext();) {
        				final String skStr = (String)itr.next();
        				final PrintData cntObj = (PrintData)wkMap.get(skStr);
            			if (cntObj._f_Cnt != null) {
            				fullCntWk = fullCntWk.add(new BigDecimal(cntObj._f_Cnt));
            			}
            			if (cntObj._h_Cnt != null) {
            				fullCntWk = fullCntWk.add(new BigDecimal(cntObj._h_Cnt));
            			}
            			if (cntObj._s_Cnt != null) {
            				fullCntWk = fullCntWk.add(new BigDecimal(cntObj._s_Cnt));
            			}
        			}
        		}
        		retMap.putAll(subMap);
        		//log.info("------ cnt ------" + retMap.size());
        		asikiriList = new ArrayList();
        	}
        	asikiriList.add(String.valueOf(ii));
        }
        if (asikiriList.size() > 0) {
    		final Map subMap = getSql10Data(db2, asikiriList);
    		for (Iterator ite = subMap.keySet().iterator();ite.hasNext();) {
    			final String kStr = (String)ite.next();
    			final Map wkMap = (Map)subMap.get(kStr);
    			for (Iterator itr = wkMap.keySet().iterator();itr.hasNext();) {
    				final String skStr = (String)itr.next();
    				final PrintData cntObj = (PrintData)wkMap.get(skStr);
        			if (cntObj._f_Cnt != null) {
        				fullCntWk = fullCntWk.add(new BigDecimal(cntObj._f_Cnt));
        			}
        			if (cntObj._h_Cnt != null) {
        				fullCntWk = fullCntWk.add(new BigDecimal(cntObj._h_Cnt));
        			}
        			if (cntObj._s_Cnt != null) {
        				fullCntWk = fullCntWk.add(new BigDecimal(cntObj._s_Cnt));
        			}
    			}
    		}
    		retMap.putAll(subMap);
        }
		//log.info("------ last cnt ------" + retMap.size());
        return fullCntWk.compareTo(new BigDecimal("0")) > 0;
    }
    private Map getSql10Data(final DB2UDB db2, final List asikiriList) {
        Map retMap = new LinkedMap();
        Map wkMap = new LinkedMap();

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
        	final String sql = getSql(asikiriList);
            //log.info(" sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
            	final String asikiri_Score = rs.getString("ASIKIRI_SCORE");
            	final String classify_Cd = rs.getString("CLASSIFY_CD");
            	final String s_Cnt = rs.getString("S_CNT");
            	final String h_Cnt = rs.getString("H_CNT");
            	final String h1_Cnt = rs.getString("H1_CNT");
            	final String h2_Cnt = rs.getString("H2_CNT");
            	final String h1_Per = rs.getString("H1_PER");
            	final String h2_Per = rs.getString("H2_PER");
            	final String h12_Per = rs.getString("H12_PER");
            	final String f_Cnt = rs.getString("F_CNT");
            	final String f1_Cnt = rs.getString("F1_CNT");
            	final String f2_Cnt = rs.getString("F2_CNT");
            	final String f1A_Per = rs.getString("F1A_PER");
            	final String f2A1_Per = rs.getString("F2A1_PER");
            	final String f2A2_Per = rs.getString("F2A2_PER");
            	final String f2A3_Per = rs.getString("F2A3_PER");
            	final String f1B_Per = rs.getString("F1B_PER");
            	PrintData addwk = new PrintData(asikiri_Score, classify_Cd, s_Cnt, h_Cnt, h1_Cnt, h2_Cnt, h1_Per, h2_Per, h12_Per, f_Cnt, f1_Cnt, f2_Cnt, f1A_Per, f2A1_Per, f2A2_Per, f2A3_Per, f1B_Per);
            	if (!retMap.containsKey(asikiri_Score)) {
            		wkMap = new LinkedMap();
            		retMap.put(asikiri_Score, wkMap);
            	} else {
            		wkMap = (Map)retMap.get(asikiri_Score);
            	}
            	wkMap.put(classify_Cd, addwk);
            }
        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retMap;
    }

    private String getSql(final List sqlprmList) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH destinations (cnt) AS ( ");
        for (java.util.Iterator ite = sqlprmList.iterator();ite.hasNext();) {
        	final String sval = (String)ite.next();
            stb.append(" VALUES("+sval+") ");
            if (ite.hasNext()) {
                stb.append(" UNION ");
            }
        }
        // 類別マスタテーブルを登録順で9つ(7(確約)-2(H/F))まで抽出
        stb.append(" ), ECM_TBL1 as ( ");
        stb.append(" SELECT ");
        stb.append("   '3' AS M_ORDER, ");
        stb.append("   * ");
        stb.append(" FROM ");
        stb.append("   ENTEXAM_CLASSIFY_MST T1 ");
        stb.append(" WHERE ");
        stb.append("   T1.ENTEXAMYEAR = '" + _param._entexamYear + "' ");
        stb.append("   AND T1.APPLICANTDIV = '" + _param._applicantdiv + "' ");
        stb.append("   AND T1.MARK NOT IN ('" + HEIGAN + "', '" + NRML + "') ");
        stb.append(" ORDER BY ");
        stb.append("    M_ORDER, ");
        stb.append("    ORDER, ");
        stb.append("    CLASSIFY_CD ");
        stb.append(" FETCH FIRST " + KAKUYAKU_PRTCNT + " ROWS ONLY ");
        stb.append(" ), ECM_TBL2 as ( ");
        stb.append(" SELECT ");
        stb.append("   '2' AS M_ORDER, ");
        stb.append("   * ");
        stb.append(" FROM ");
        stb.append("   ENTEXAM_CLASSIFY_MST T1 ");
        stb.append(" WHERE ");
        stb.append("   T1.ENTEXAMYEAR = '" + _param._entexamYear + "' ");
        stb.append("   AND T1.APPLICANTDIV = '" + _param._applicantdiv + "' ");
        stb.append("   AND T1.MARK = '" + HEIGAN + "' ");
        stb.append(" ), ECM_TBL3 as ( ");
        stb.append(" SELECT ");
        stb.append("   '1' AS M_ORDER, ");
        stb.append("   * ");
        stb.append(" FROM ");
        stb.append("   ENTEXAM_CLASSIFY_MST T1 ");
        stb.append(" WHERE ");
        stb.append("   T1.ENTEXAMYEAR = '" + _param._entexamYear + "' ");
        stb.append("   AND T1.APPLICANTDIV = '" + _param._applicantdiv + "' ");
        stb.append("   AND T1.MARK ='" + NRML + "' ");
        stb.append(" ), ECM_TBL AS ( ");
        stb.append(" SELECT * FROM ECM_TBL1 ");
        stb.append(" UNION ALL ");
        stb.append(" SELECT * FROM ECM_TBL2 ");
        stb.append(" UNION ALL ");
        stb.append(" SELECT * FROM ECM_TBL3 ");
        stb.append(" ORDER BY ");
        stb.append("    M_ORDER, ");
        stb.append("    ORDER, ");
        stb.append("    CLASSIFY_CD ");
        // 類別マスタ(確定フラグONの物と併願、一般)とクロステーブルを作成
        stb.append(" ), B_TBL AS ( ");
        stb.append(" SELECT ");
        stb.append("   T1.cnt AS ASIKIRI_SCORE, ");
        stb.append("   T2.M_ORDER, ");
        stb.append("   T2.ORDER, ");
        stb.append("   T2.CLASSIFY_CD, ");
        stb.append("   T2.MARK, ");
        stb.append("   T2.CALC_FLG, ");
        stb.append("   T2.COMMITMENT_FLG ");
        stb.append(" FROM ");
        stb.append("   destinations T1 ");
        stb.append("   FULL OUTER JOIN ECM_TBL T2 ");
        stb.append("     ON T2.ENTEXAMYEAR = '" + _param._entexamYear + "' ");
        stb.append("     AND T2.APPLICANTDIV = '" + _param._applicantdiv + "' ");
        stb.append(" WHERE ");
        stb.append("   T1.cnt IS NOT NULL ");
        stb.append(" ORDER BY ");
        stb.append("   T1.cnt, ");
        stb.append("   T2.M_ORDER, ");
        stb.append("   T2.ORDER, ");
        stb.append("   T2.classify_cd ");
        //科目欠席チェック用集計
        stb.append(" ), MERGE_SCORE AS (");
        stb.append(" SELECT ");
        stb.append("   ENTEXAMYEAR, ");
        stb.append("   APPLICANTDIV, ");
        stb.append("   TESTDIV, ");
        stb.append("   EXAM_TYPE, ");
        stb.append("   RECEPTNO, ");
        stb.append("   SUM(CASE WHEN (ATTEND_FLG = '1' OR ATTEND_FLG IS NULL) THEN 0 ELSE 1 END) AS ABSENT_CNT ");
        stb.append(" FROM ");
        stb.append("   ENTEXAM_SCORE_DAT ");
        stb.append(" GROUP BY ");
        stb.append("   ENTEXAMYEAR, ");
        stb.append("   APPLICANTDIV, ");
        stb.append("   TESTDIV, ");
        stb.append("   EXAM_TYPE, ");
        stb.append("   RECEPTNO ");
        // 各足切り点数ごとに生徒の到達/未達を算出。他に出身の判別や併願の判別を行う。
        stb.append(" ), ASIKIRI_RESULT_TBL AS ( ");
        stb.append(" SELECT ");
        stb.append("   B1.CNT AS ASIKIRI_SCORE, ");
        stb.append("   T1.EXAMNO, ");
        stb.append("   T1.TESTDIV1, ");
        stb.append("   T2.TOTAL4, ");
        stb.append("   CASE WHEN (VALUE(L027.NAMESPARE1, '') <> '1' AND VALUE(T5.ATTEND_FLG, '') <> '1') THEN 0 ELSE 1 END AS COMMITMENT_FAIL_FLG, ");
        stb.append("   CASE WHEN (B1.CNT <= VALUE(T2.TOTAL4, 0) AND VALUE(L027.NAMESPARE1, '') <> '1' AND VALUE(T5.ATTEND_FLG, '') <> '1')  THEN 1 ELSE 0 END AS JUDGE, ");
        stb.append("   CASE WHEN T3.FINSCHOOL_PREF_CD = '13' THEN 1 ELSE 0 END AS IS_TOKYO, ");  // 併願類別(H)の時のみ利用(13:東京地区)
        stb.append("   CASE WHEN VALUE(T4.REMARK9, '') <> '' THEN 1 ELSE 0 END AS IS_HEIGAN ");  // 一般(F)の時のみ利用
        stb.append(" FROM ");
        stb.append(" destinations B1 ");
        stb.append(" FULL OUTER JOIN ENTEXAM_APPLICANTBASE_DAT T1 ");
        stb.append("   ON T1.ENTEXAMYEAR = '" + _param._entexamYear + "' ");
        stb.append("  AND T1.APPLICANTDIV = '" + _param._applicantdiv + "' ");
        stb.append("  AND T1.TESTDIV = '" + _param._testdiv + "' ");
        stb.append(" LEFT JOIN ENTEXAM_RECEPT_DAT T2 ");
        stb.append("   ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("  AND T2.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("  AND T2.TESTDIV = T1.TESTDIV ");
        stb.append("  AND T2.EXAM_TYPE = '1' ");
        stb.append("  AND T2.EXAMNO = T1.EXAMNO ");
        stb.append(" LEFT JOIN FINSCHOOL_MST T3 ");
        stb.append("   ON T3.FINSCHOOLCD = T1.FS_CD ");
        stb.append(" LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT T4 ");
        stb.append("   ON T4.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("  AND T4.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("  AND T4.EXAMNO = T1.EXAMNO ");
        stb.append("  AND T4.SEQ = '031' ");
        stb.append(" LEFT JOIN ENTEXAM_INTERVIEW_DAT T5 ");
        stb.append("   ON T5.ENTEXAMYEAR = T2.ENTEXAMYEAR ");
        stb.append("  AND T5.APPLICANTDIV = T2.APPLICANTDIV ");
        stb.append("  AND T5.TESTDIV = T2.TESTDIV ");
        stb.append("  AND T5.EXAMNO = T2.EXAMNO ");
        stb.append(" LEFT JOIN MERGE_SCORE T6 ");
        stb.append("   ON T6.ENTEXAMYEAR = T2.ENTEXAMYEAR ");
        stb.append("  AND T6.APPLICANTDIV = T2.APPLICANTDIV ");
        stb.append("  AND T6.TESTDIV = T2.TESTDIV ");
        stb.append("  AND T6.EXAM_TYPE = T2.EXAM_TYPE ");
        stb.append("  AND T6.RECEPTNO = T2.RECEPTNO ");
        stb.append(" LEFT JOIN ENTEXAM_SETTING_MST L027 ");
        stb.append("   ON L027.ENTEXAMYEAR = T5.ENTEXAMYEAR ");
        stb.append("  AND L027.APPLICANTDIV = T5.APPLICANTDIV ");
        stb.append("  AND L027.SETTING_CD = 'L027' ");
        stb.append("  AND L027.SEQ = T5.INTERVIEW_A ");
        stb.append(" WHERE ");
        stb.append("  B1.CNT IS NOT NULL ");
        stb.append("  AND T1.JUDGEMENT <> '3' ");  //欠席は除く
        stb.append("  AND T6.ABSENT_CNT = 0 ");    //科目欠席０のみ(科目欠席は除く)
        // 各類別コード毎に集計(無条件合格と足切り突破)
        stb.append(" ), GRP_CRS_TBL AS ( ");
        stb.append(" SELECT ");
        stb.append("   B1.ASIKIRI_SCORE, ");
        stb.append("   B1.M_ORDER, ");
        stb.append("   B1.ORDER, ");
        stb.append("   B1.CLASSIFY_CD, ");
        ////集計方法：H/Fの場合とそれ以外で合否判定が違う。 1.H/Fなら合否判定する。2.H/F以外では、確約フラグは面談D合格、それ以外は点数判定する。
        stb.append("   SUM( ");
        stb.append("     CASE WHEN B1.MARK IN ('" + HEIGAN + "', '" + NRML + "') ");
        stb.append("          THEN (CASE WHEN (T1.EXAMNO IS NOT NULL AND T1.TOTAL4 IS NOT NULL) THEN T1.JUDGE ELSE 0 END) ");
        stb.append("          ELSE (CASE WHEN B1.COMMITMENT_FLG = '1' AND T1.COMMITMENT_FAIL_FLG <> 1 THEN 1  ");
        stb.append("                     ELSE (CASE WHEN (B1.CALC_FLG = '1' AND T1.EXAMNO IS NOT NULL AND T1.TOTAL4 IS NULL) THEN T1.JUDGE ELSE 0 END) ");
        stb.append("                     END ) ");
        stb.append("          END ) AS S_CNT, ");
        stb.append("   CASE WHEN B1.MARK = '" + HEIGAN + "' THEN SUM(CASE WHEN T1.JUDGE = 1 THEN T1.IS_TOKYO ELSE 0 END) ELSE NULL END AS H1_CNT, ");                                    //★"併願:H"として記載
        stb.append("   CASE WHEN B1.MARK = '" + HEIGAN + "' THEN SUM(CASE WHEN T1.JUDGE = 1 THEN (CASE WHEN T1.IS_TOKYO = 1 THEN 0 ELSE 1 END) ELSE 0 END) ELSE NULL END AS H2_CNT, ");  //★"併願:H"として記載
        stb.append("   CASE WHEN B1.MARK = '" + NRML + "' THEN SUM(CASE WHEN T1.JUDGE = 1 THEN (CASE WHEN T1.IS_HEIGAN = 1 THEN 0 ELSE 1 END) ELSE 0 END) ELSE NULL END AS F1_CNT, ");   //★"一般:F"として記載
        stb.append("   CASE WHEN B1.MARK = '" + NRML + "' THEN SUM(CASE WHEN T1.JUDGE = 1 THEN T1.IS_HEIGAN ELSE 0 END) ELSE NULL END AS F2_CNT ");                                      //★"一般:F"として記載
        stb.append(" FROM ");
        stb.append("   B_TBL B1 ");
        stb.append("   LEFT JOIN ASIKIRI_RESULT_TBL T1 ");
        stb.append("     ON T1.ASIKIRI_SCORE = B1.ASIKIRI_SCORE ");
        stb.append("    AND T1.TESTDIV1 = B1.CLASSIFY_CD ");
        stb.append(" GROUP BY ");
        stb.append("   B1.ASIKIRI_SCORE, ");
        stb.append("   B1.M_ORDER, ");
        stb.append("   B1.ORDER, ");
        stb.append("   B1.CLASSIFY_CD, ");
        stb.append("   B1.MARK ");
        // 各類別コード(H,F以外)毎に集計(無条件合格と足切り突破)
        stb.append(" ), GRP_CRS_NOTHF_TBL AS ( ");
        stb.append(" SELECT ");
        stb.append("   B1.ASIKIRI_SCORE, ");
        stb.append("   B1.M_ORDER, ");
        stb.append("   B1.ORDER, ");
        stb.append("   B1.CLASSIFY_CD, ");
        ////H/F以外では、確約フラグは合格、それ以外は点数判定する。
        stb.append("   SUM(CASE WHEN B1.COMMITMENT_FLG = '1' AND T1.COMMITMENT_FAIL_FLG <> 1 THEN 1 ");
        stb.append("            ELSE (CASE WHEN (B1.CALC_FLG = '1' AND T1.EXAMNO IS NOT NULL AND T1.TOTAL4 IS NULL) THEN T1.JUDGE ELSE 0 END) END) AS S_CNT, ");
        stb.append("   NULL AS H1_CNT, ");  //確約はH/F不要なのでNULL
        stb.append("   NULL AS H2_CNT, ");  //確約はH/F不要なのでNULL
        stb.append("   NULL AS F1_CNT, ");  //確約はH/F不要なのでNULL
        stb.append("   NULL AS F2_CNT ");   //確約はH/F不要なのでNULL
        stb.append(" FROM ");
        stb.append("   B_TBL B1 ");
        stb.append("   LEFT JOIN ASIKIRI_RESULT_TBL T1 ");
        stb.append("     ON T1.ASIKIRI_SCORE = B1.ASIKIRI_SCORE ");
        stb.append("    AND T1.TESTDIV1 = B1.CLASSIFY_CD ");
        stb.append(" WHERE ");
        stb.append("   B1.MARK NOT IN ('" + HEIGAN + "', '" + NRML + "')");
        stb.append(" GROUP BY ");
        stb.append("   B1.ASIKIRI_SCORE, ");
        stb.append("   B1.M_ORDER, ");
        stb.append("   B1.ORDER, ");
        stb.append("   B1.CLASSIFY_CD, ");
        stb.append("   B1.MARK ");
        // 確約列(合格者計)を作成
        stb.append(" ), SAMM_TBL AS ( ");  //個別列
        stb.append(" SELECT ");
        stb.append("   ASIKIRI_SCORE, ");
        stb.append("   M_ORDER, ");
        stb.append("   ORDER, ");
        stb.append("   CLASSIFY_CD, ");
        stb.append("   SUM(VALUE(S_CNT, 0)) AS S_CNT, ");
        stb.append("   SUM(VALUE(H1_CNT, 0)+VALUE(H2_CNT, 0)) AS H_CNT, ");
        stb.append("   SUM(H1_CNT) AS H1_CNT, ");
        stb.append("   SUM(H2_CNT) AS H2_CNT, ");
        stb.append("   SUM(VALUE(F1_CNT, 0)+VALUE(F2_CNT, 0)) AS F_CNT, ");
        stb.append("   SUM(F1_CNT) AS F1_CNT, ");
        stb.append("   SUM(F2_CNT) AS F2_CNT ");
        stb.append(" FROM ");
        stb.append("   GRP_CRS_TBL ");
        stb.append(" GROUP BY ");
        stb.append("   ASIKIRI_SCORE, ");
        stb.append("   M_ORDER, ");
        stb.append("   ORDER, ");
        stb.append("   CLASSIFY_CD ");
        stb.append(" UNION ALL ");  //確約列
        stb.append(" SELECT ");
        stb.append("   ASIKIRI_SCORE, ");
        stb.append("   '99' AS M_ORDER, ");
        stb.append("   '99' AS ORDER, ");
        stb.append("   '" + KAKUYAKU + "' AS CLASSIFY_CD, ");
        stb.append("   SUM(S_CNT) AS S_CNT, ");
        stb.append("   NULL AS H_CNT, ");
        stb.append("   NULL AS H1_CNT, ");
        stb.append("   NULL AS H2_CNT, ");
        stb.append("   NULL AS F_CNT, ");
        stb.append("   NULL AS F1_CNT, ");
        stb.append("   NULL AS F2_CNT ");
        stb.append(" FROM ");
        stb.append("   GRP_CRS_NOTHF_TBL ");
        stb.append(" GROUP BY ");
        stb.append("   ASIKIRI_SCORE ");
        stb.append(" ORDER BY ");
        stb.append("   ASIKIRI_SCORE, ");
        stb.append("   M_ORDER, ");
        stb.append("   ORDER, ");
        stb.append("   classify_cd ");
        stb.append(" ) ");
        // 他の項目を作成
        stb.append(" SELECT ");
        stb.append("   T1.ASIKIRI_SCORE, ");
        stb.append("   T1.M_ORDER, ");
        stb.append("   T1.ORDER, ");
        stb.append("   T1.CLASSIFY_CD, ");
        stb.append("   T1.S_CNT, ");
        stb.append("   T1.H_CNT, ");
        stb.append("   T1.H1_CNT, ");
        stb.append("   T1.H2_CNT, ");
        stb.append("   DECIMAL(T1.H1_CNT * " + _param._heiganP1 + " / 100.0, 6,2) AS H1_PER, ");
        stb.append("   DECIMAL(T1.H2_CNT * " + _param._heiganP2 + " / 100.0, 6,2) AS H2_PER, ");
        stb.append("   DECIMAL((T1.H1_CNT * " + _param._heiganP1 + " + T1.H2_CNT * " + _param._heiganP2 + " ) / 100.0, 6,2) AS H12_PER, ");
        stb.append("   T1.F_CNT, ");
        stb.append("   T1.F1_CNT, ");
        stb.append("   T1.F2_CNT, ");
        stb.append("   DECIMAL(T1.F1_CNT * " + _param._nrmlP1 + " / 100.0, 6,2) AS F1A_PER, ");
        stb.append("   DECIMAL(T1.F2_CNT * " + _param._ennouP1 + " / 100.0, 6,2) AS F2A1_PER, ");
        stb.append("   DECIMAL(T1.F2_CNT * " + _param._ennouP2 + " / 100.0, 6,2) AS F2A2_PER, ");
        stb.append("   DECIMAL(T1.F2_CNT * " + _param._ennouP3 + " / 100.0, 6,2) AS F2A3_PER, ");
        stb.append("   DECIMAL(T1.F1_CNT * " + _param._nrmlP2 + " / 100.0, 6,2) AS F1B_PER ");
        stb.append(" FROM ");
        stb.append("   SAMM_TBL T1 ");
        stb.append(" ORDER BY ");
        stb.append("   T1.ASIKIRI_SCORE, ");
        stb.append("   T1.M_ORDER DESC, ");
        stb.append("   T1.ORDER, ");
        stb.append("   T1.classify_cd ");
        return stb.toString();
    }

    private class PrintData {
        final String _asikiri_Score;
        final String _classify_Cd;
        final String _s_Cnt;
        final String _h_Cnt;
        final String _h1_Cnt;
        final String _h2_Cnt;
        final String _h1_Per;
        final String _h2_Per;
        final String _h12_Per;
        final String _f_Cnt;
        final String _f1_Cnt;
        final String _f2_Cnt;
        final String _f1A_Per;
        final String _f2A1_Per;
        final String _f2A2_Per;
        final String _f2A3_Per;
        final String _f1B_Per;
        public PrintData (final String asikiri_Score, final String classify_Cd, final String s_Cnt, final String h_Cnt, final String h1_Cnt, final String h2_Cnt, final String h1_Per, final String h2_Per, final String h12_Per, final String f_Cnt, final String f1_Cnt, final String f2_Cnt, final String f1A_Per, final String f2A1_Per, final String f2A2_Per, final String f2A3_Per, final String f1B_Per)
        {
            _asikiri_Score = asikiri_Score;
            _classify_Cd = classify_Cd;
            _s_Cnt = s_Cnt;
            _h_Cnt = h_Cnt;
            _h1_Cnt = h1_Cnt;
            _h2_Cnt = h2_Cnt;
            _h1_Per = h1_Per;
            _h2_Per = h2_Per;
            _h12_Per = h12_Per;
            _f_Cnt = f_Cnt;
            _f1_Cnt = f1_Cnt;
            _f2_Cnt = f2_Cnt;
            _f1A_Per = f1A_Per;
            _f2A1_Per = f2A1_Per;
            _f2A2_Per = f2A2_Per;
            _f2A3_Per = f2A3_Per;
            _f1B_Per = f1B_Per;
        }
    }
    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 56595 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
    	final String _ctrlDate;
        final String _entexamYear;
        final String _applicantdiv;
        final String _testdiv;
        final String _testdivName;
        final int _strtLine;
        final int _endLine;
        final String _heiganP1;
        final String _heiganP2;
        final String _nrmlP1;
        final String _nrmlP2;
        final String _ennouP1;
        final String _ennouP2;
        final String _ennouP3;
        final Map _classifyMap;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
        	_ctrlDate = request.getParameter("LOGIN_DATE");
            _entexamYear = request.getParameter("ENTEXAMYEAR");
            _applicantdiv = "2";  //request.getParameter("APPLICANTDIV");
            _testdiv = request.getParameter("TESTDIV");
            _strtLine = Integer.parseInt(request.getParameter("SIM_START"));
            _endLine = Integer.parseInt(request.getParameter("SIM_END"));
            _heiganP1 = request.getParameter("TOKIO_PROBABILITY");
            _heiganP2 = request.getParameter("OTHER_PROBABILITY");
            _nrmlP1 = request.getParameter("GENERAL_PROBABILITY1");
            _nrmlP2 = request.getParameter("GENERAL_PROBABILITY2");
            _ennouP1 = request.getParameter("POSTPONE_PROBABILITY1");
            _ennouP2 = request.getParameter("POSTPONE_PROBABILITY2");
            _ennouP3 = request.getParameter("POSTPONE_PROBABILITY3");
            _classifyMap = getClassifyMap(db2);
            _testdivName = getTestdivName(db2);
        }
        private String getTestdivName(final DB2UDB db2) {
        	String retStr = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT TESTDIV_NAME FROM ENTEXAM_TESTDIV_MST WHERE ENTEXAMYEAR = '" + _entexamYear + "' AND APPLICANTDIV = '" + _applicantdiv + "' AND TESTDIV = '" + _testdiv + "' "));
        	retStr = StringUtils.defaultString(retStr, "");
        	return retStr;
        }
        private Map getClassifyMap(final DB2UDB db2) {
        	final Map retMap = new LinkedMap();
            StringBuffer stb = new StringBuffer();
            //getSql()と合わせているので、修正の際は注意。
            stb.append(" WITH ECM_TBL1 as ( ");
            stb.append(" SELECT ");
            stb.append("   '1' AS M_ORDER, ");
            stb.append("   * ");
            stb.append(" FROM ");
            stb.append("   ENTEXAM_CLASSIFY_MST T1 ");
            stb.append(" WHERE ");
            stb.append("   T1.ENTEXAMYEAR = '" + _entexamYear + "' ");
            stb.append("   AND T1.APPLICANTDIV = '" + _applicantdiv + "' ");
            stb.append("   AND T1.MARK NOT IN ('" + HEIGAN + "', '" + NRML + "') ");
            stb.append(" ORDER BY ");
            stb.append("    ORDER, ");
            stb.append("    CLASSIFY_CD ");
            stb.append(" FETCH FIRST " + KAKUYAKU_PRTCNT + " ROWS ONLY ");
            stb.append(" ), ECM_TBL3 as ( ");
            stb.append(" SELECT ");
            stb.append("   '3' AS M_ORDER, ");
            stb.append("   * ");
            stb.append(" FROM ");
            stb.append("   ENTEXAM_CLASSIFY_MST T1 ");
            stb.append(" WHERE ");
            stb.append("   T1.ENTEXAMYEAR = '" + _entexamYear + "' ");
            stb.append("   AND T1.APPLICANTDIV = '" + _applicantdiv + "' ");
            stb.append("   AND T1.MARK IN ('" + HEIGAN + "', '" + NRML + "') ");
            stb.append(" FETCH FIRST 2 ROWS ONLY ");
            stb.append(" ) ");
            stb.append(" SELECT * FROM ECM_TBL1 ");
            stb.append(" UNION ALL ");
            stb.append(" SELECT * FROM ECM_TBL3 ");
            stb.append(" ORDER BY ");
            stb.append("    M_ORDER, ");
            stb.append("    ORDER, ");
            stb.append("    CLASSIFY_CD ");

            //log.info("getClassifyMap:"+stb.toString());
        	for (final Iterator it = KnjDbUtils.query(db2, stb.toString()).iterator(); it.hasNext();) {
        		final Map row = (Map) it.next();
        		final String kStr = KnjDbUtils.getString(row, "CLASSIFY_CD");
        		retMap.put(kStr, row);
        	}
        	return retMap;
        }
    }
}

// eof

