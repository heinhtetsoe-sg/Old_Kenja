// kanji=漢字
/*
 * $Id: KNJL840H.java 77567 2020-11-18 04:06:31Z ishii $
 *
 * 作成日: 2020/10/08
 * 作成者: yogi
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
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 * <<クラスの説明>>。
 * @author nakamoto
 * @version $Id: KNJL840H.java 77567 2020-11-18 04:06:31Z ishii $
 */
public class KNJL840H {

    private static final Log log = LogFactory.getLog("KNJL840H.class");

    private boolean _hasData;

    private Param _param;

    private static final String SVF_CENTERING = "Hensyu=3";
    private static final String SVF_SETBGCOLOR = "Paint=(1,80,1)";

    private static final String PUDIV_FULL = "1";
    private static final String PUDIV_IGNORESUC = "2";

    private static final String SEX_NORM = "1";
    private static final String SEX_MALE = "2";
    private static final String SEX_FEMALE = "3";

    private static final String SEX_MAN = "1";
    private static final String SEX_WOMAN = "2";

    private static final String APP_FLG_FULL = "1";
    private static final String APP_FLG_ONE = "2";
    private static final String APP_FLG_SUM = "3";
    private static final String APP_FLG_CONC = "4";
    private static final String APP_FLG_MULTI = "5";

    private static final String SUMTYPE_POINT = "1";
    private static final String SUMTYPE_SUBCLS = "2";

    private static final boolean SEARCH_SCOREMAP = true;
    private static final boolean SEARCH_GLASSMAP = false;

    private static final String ATTENDCHK = "CHK_ATTEND";
    private static final String ABSENTCHK = "CHK_ABSENT";

	final static String ABSENT = "1";  //欠席

	final static String FULL_SCORECD = "99";

	final static String PRT_TBL1 = "PRT1";
	final static String PRT_TBL2 = "PRT2";

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

            _hasData = printMain(db2, svf);
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
    private boolean printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
    	boolean retbl = false;
    	final String form = !_param._isHschoolSuisen && SUMTYPE_POINT.equals(_param._totalDiv) ? "KNJL840H_2.frm" : "KNJL840H_1.frm";

    	final Map attendMap = new LinkedMap();
    	final Map applicantMap = getApplicantMap(db2, attendMap); //成績Map
    	final Map totalSInfo = getTotalScoreInfo(db2); //総合情報
    	final Map useTotalSInfo = totalSInfo.containsKey(_param.getUseSexCd()) ? (Map)totalSInfo.get(_param.getUseSexCd()) : null;

    	if(applicantMap.isEmpty() || totalSInfo.isEmpty()) {
        	return retbl;
        }

    	final int lineMax = 90;  //表1+表2の行数
    	final int lineHalf = lineMax / 2; //各表の行数

       	for (Iterator itt = attendMap.keySet().iterator();itt.hasNext();) {
       		svf.VrSetForm(form, 4);
    		final String testSubclsCd = (String)itt.next();
    		final TotalScoreInfo tsWk = useTotalSInfo.containsKey(testSubclsCd) ? (TotalScoreInfo)useTotalSInfo.get(testSubclsCd) : null;
    		setTitle(db2, svf, ("99".equals(testSubclsCd) || tsWk == null) ? "" : tsWk._testSubclassName);
    		setHeadTotalInfo(svf, tsWk, useTotalSInfo, attendMap);

       		int maxCnt1 = _param.getKizamiFullRateCnt(testSubclsCd, SEARCH_SCOREMAP);
        	int maxCnt2 = _param.getKizamiFullRateCnt(testSubclsCd, SEARCH_GLASSMAP);
        	Iterator ite1 = _param._scoreKizamiList.iterator();
        	Iterator ite2 = _param._glassKizamiList.iterator();
        	int lineCnt = 0;
        	int totalCnt1 = 0;
        	int totalCnt2 = 0;

    	    while (ite1.hasNext() || ite2.hasNext()) {
    	        if (lineCnt > lineMax) {
    	            lineCnt = 1;
    	        }
    	        if (lineCnt < lineHalf && ite1.hasNext()) {
    	            KizamiCls prt1 = (KizamiCls)ite1.next();
        	        if (maxCnt1 > 0 && prt1._studentInfo.containsKey(testSubclsCd)) {
        	            totalCnt1 += ((Map)prt1._studentInfo.get(testSubclsCd)).size();
        	        }
        	        printDetail(svf, testSubclsCd, prt1, totalCnt1, maxCnt1, PRT_TBL1);
        	    } else if (_param._isPrintGlass && (lineHalf <= lineCnt && lineCnt < lineMax) && ite2.hasNext()) {
        	        KizamiCls prt2 = (KizamiCls)ite2.next();
        	        if (maxCnt2 > 0 && prt2._studentInfo.containsKey(testSubclsCd)) {
        	            totalCnt2 += ((Map)prt2._studentInfo.get(testSubclsCd)).size();
        	        }
        	        printDetail(svf, testSubclsCd, prt2, totalCnt2, maxCnt2, PRT_TBL2);
        	    } else {
        	        //空行出力
        	        svf.VrsOut("BLANK", "a");
        	    }
        	    svf.VrEndRecord();
        	    lineCnt++;
        	    retbl = true;
        	}
    	    svf.VrEndPage();
    	}
       	return retbl;
    }

    private void printDetail(final Vrw32alp svf, final String testSubclsCd, final KizamiCls prt, final int totalCnt1, final int maxCnt, final String prtPattern) {
    	final BigDecimal graphScale = new BigDecimal(1.2);  //このグラフは右端が60%という表で、5%につき6個の"X"が付く。つまり1%=1.2個の"X" -> 比率1.2。
    	final int graphMaxRept = 63;    //このグラフは右端が60%という表で76(84byte-8byte(※%記載分))byte分の繰り返し幅があり、5%につき6個の"X"が付く。つまり60%で72個。63%より大なら64%(76個)として扱うため、63を判定値としている。

        String fIdxStr = "1";  //フォームのフィールド指定用。虫眼鏡の範囲の際に、背景色を変えるときに切替。
        //書式設定(特に背景色設定)
        if (_param._isPrintGlass) {
            final BigDecimal gS = new BigDecimal(_param._glass_S);
            final BigDecimal gE = new BigDecimal(_param._glass_E);

            if (gS != null && gE != null) {
                if ((gS.compareTo(prt._kS) <= 0 && prt._kS.compareTo(gE) <= 0 && PRT_TBL1.equals(prtPattern))
                	|| (gS.compareTo(prt._kE) <= 0 && prt._kE.compareTo(gE) <= 0 && PRT_TBL1.equals(prtPattern))) {
                    fIdxStr = "2";
                	final String r1Str = SVF_SETBGCOLOR + (prt.isSamePoint() ? "," + SVF_CENTERING : "");
                    svf.VrAttribute("RANGE"+ fIdxStr, r1Str);
                    svf.VrAttribute("RATE_TOTAL" + fIdxStr, SVF_SETBGCOLOR);
                    svf.VrAttribute("RATE" + fIdxStr, SVF_SETBGCOLOR);
                    svf.VrAttribute("PERCENT" + fIdxStr, SVF_SETBGCOLOR);
                } else {
                	if (prt.isSamePoint()) {
                		svf.VrAttribute("RANGE" + fIdxStr, SVF_CENTERING);
                	}
                }
            }
        } else {
        	if (prt.isSamePoint()) {
        		svf.VrAttribute("RANGE" + fIdxStr, SVF_CENTERING);
        	}
        }

        svf.VrsOut("RANGE" + fIdxStr, prt.getRangeStr());
        svf.VrsOut("RATE_TOTAL" + fIdxStr, String.valueOf(totalCnt1) );
        int rateCnt = 0;
        if (prt._studentInfo.containsKey(testSubclsCd)) {
        	rateCnt = ((Map)prt._studentInfo.get(testSubclsCd)).size();
        }
        svf.VrsOut("RATE" + fIdxStr, String.valueOf(rateCnt));
        final BigDecimal rate = new BigDecimal(rateCnt).multiply(new BigDecimal(100)).divide(new BigDecimal(maxCnt), 1, BigDecimal.ROUND_HALF_UP);
        final BigDecimal rateWk = rate.compareTo(new BigDecimal(graphMaxRept)) > 0 ? new BigDecimal(graphMaxRept + 1) : rate;

        final int reptCnt = rateWk.multiply(graphScale).setScale(0, BigDecimal.ROUND_CEILING).intValue();
        svf.VrsOut("PERCENT" + fIdxStr, (reptCnt == 0 ? "" : StringUtils.repeat("X", reptCnt)) + "(" + rate.toString() + ")");

        return;
    }

    private void setTitle(final DB2UDB db2, final Vrw32alp svf, final String subclsTestName) {
        //年度
    	svf.VrsOut("", KNJ_EditDate.gengou(db2, Integer.parseInt(_param._examYear))+ "年度　");
        //試験名
    	svf.VrsOut("EXAM_DIV", _param._testDivName);
    	if (!"".equals(subclsTestName)) {
    		//科目名称
    		svf.VrsOut("EXAM_SUBCLASS", subclsTestName);
    	}
    	//性別
		final String div = (SEX_NORM.equals(_param._sex) ? "共通" : SEX_MALE.equals(_param._sex) ? "男子" : "女子");
		svf.VrsOut("EXAM_HALL", div);
		//日付
    	final String date = _param._date != null ? StringUtils.replace(_param._date, "-", "/") : "";
    	svf.VrsOut("DATE", date);
    	//タイトル
    	final String topTitle = SUMTYPE_POINT.equals(_param._totalDiv) ? "合計点度数分布表" : "科目別度数分布表";
		svf.VrsOut("TITLE", "□ □ □　" + topTitle + "　□ □ □");

    }
    private void setHeadTotalInfo(final Vrw32alp svf, final TotalScoreInfo nowTsWk, final Map useTotalSInfo, final Map attendMap) {
    	if (nowTsWk == null) {
    		return;
    	}
        if (!_param._isHschoolSuisen && SUMTYPE_POINT.equals(_param._totalDiv)) {
            //合計点でのみ出力
        	int lineCnt = 0;
        	for (Iterator ite = useTotalSInfo.keySet().iterator();ite.hasNext();) {
        		final String tscd = (String)ite.next();
        		if (FULL_SCORECD.equals(tscd)) {
        			continue;
        		}
        		final TotalScoreInfo tsWk = (TotalScoreInfo)useTotalSInfo.get(tscd);
            	if (tsWk != null) {
            		lineCnt++;
            		svf.VrsOutn("CLASS_NAME", lineCnt, tsWk._testSubclassName);
            		svf.VrsOutn("CLASS_MAX", lineCnt, tsWk._highScore);
            		svf.VrsOutn("CLASS_MIN", lineCnt, tsWk._lowScore);
            		svf.VrsOutn("CLASS_AVE", lineCnt, tsWk._avg);
            	}
        	}
        }
    	//総合
    	svf.VrsOut("MAX", nowTsWk._highScore);
    	svf.VrsOut("MIN", nowTsWk._lowScore);
    	svf.VrsOut("AVE", nowTsWk._avg);

    	//出欠
    	Attend useAttObj;

        useAttObj = (Attend)attendMap.get(nowTsWk._testSubclassCd);
    	if (useAttObj != null) {
    		svf.VrsOut("ATTEND", String.valueOf(useAttObj._attend));
    		svf.VrsOut("ABSENT", String.valueOf(useAttObj._absent));
    		svf.VrsOut("TOTAL", String.valueOf(useAttObj._attend + useAttObj._absent));
    	}
    }

    private Map getTotalScoreInfo(final DB2UDB db2) throws SQLException {
    	Map retMap = new LinkedMap();
    	Map subMap = new LinkedMap();
    	PreparedStatement ps = null;
        ResultSet rs = null;

        final String sql = getTotalScoreInfoSql();
		log.debug(" getTotalScoreInfo sql =" + sql);

        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

			while (rs.next()) {
				final String sex = rs.getString("SEX");
	            final String testSubclsCd = rs.getString("TESTSUBCLASSCD");
	            final String testSubclsName = rs.getString("TESTSUBCLASS_Name");
				final String highScore = rs.getString("HIGHSCORE");
				final String lowScore = rs.getString("LOWSCORE");
				final String avg = rs.getString("AVG");
				final String count = rs.getString("COUNT");

				final TotalScoreInfo addwk = new TotalScoreInfo(sex, testSubclsCd, testSubclsName, highScore, lowScore, avg, count);

			    if(!retMap.containsKey(sex)) {
			    	subMap = new LinkedMap();
			    	retMap.put(sex, subMap);
			    }
			    subMap.put(testSubclsCd, addwk);
			}
		} catch (final SQLException e) {
			log.error("志願者の基本情報取得でエラー", e);
			throw e;
		} finally {
			db2.commit();
			DbUtils.closeQuietly(null, ps, rs);
		}

    	return retMap;
    }

    private String getTotalScoreInfoSql() {
        final StringBuffer stb = new StringBuffer();
        if (_param._isHschoolSuisen && SUMTYPE_POINT.equals(_param._totalDiv)) {
    		stb.append(" SELECT ");
    		stb.append("   CASE WHEN BASE.SEX IS NULL THEN '9' ELSE BASE.SEX END AS SEX, ");
    		stb.append("   '99' AS TESTSUBCLASSCD, ");
    		stb.append("   '' AS TESTSUBCLASS_NAME, ");
    		stb.append("   MAX(FLOAT(RD009.REMARK6)) AS HIGHSCORE, ");
    		stb.append("   MIN(FLOAT(RD009.REMARK6)) AS LOWSCORE, ");
    		stb.append("   DECIMAL(ROUND(AVG(FLOAT(RD009.REMARK6) * 1.0), 1), 4, 1) AS AVG, ");
    		stb.append("   COUNT(FLOAT(RD009.REMARK6)) AS COUNT ");
    		stb.append(" FROM ");
    		stb.append("   ENTEXAM_APPLICANTBASE_DAT BASE ");
    		stb.append(" LEFT JOIN ENTEXAM_RECEPT_DAT REC ");
    		stb.append("    ON REC.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
    		stb.append("   AND REC.APPLICANTDIV = BASE.APPLICANTDIV ");
    		stb.append("   AND REC.TESTDIV = BASE.TESTDIV ");
    		stb.append("   AND REC.EXAM_TYPE = '1' ");
    		stb.append("   AND REC.EXAMNO = BASE.EXAMNO ");
            stb.append(" LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT RD009 ");
            stb.append("    ON RD009.ENTEXAMYEAR = REC.ENTEXAMYEAR ");
            stb.append("   AND RD009.APPLICANTDIV = REC.APPLICANTDIV ");
            stb.append("   AND RD009.TESTDIV = REC.TESTDIV ");
            stb.append("   AND RD009.EXAM_TYPE = REC.EXAM_TYPE ");
            stb.append("   AND RD009.RECEPTNO = REC.RECEPTNO ");
            stb.append("   AND RD009.SEQ = '009' ");
    		stb.append(" WHERE ");
    		stb.append("   BASE.ENTEXAMYEAR = '" + _param._examYear + "' ");
    		stb.append("   AND BASE.APPLICANTDIV = '" + _param._applicantDiv + "' ");
    		stb.append("   AND BASE.TESTDIV = '" + _param._testDiv + "' ");
    		stb.append("   AND RD009.REMARK6 IS NOT NULL ");
    		stb.append(" GROUP BY ");
    		stb.append("   GROUPING SETS ((BASE.SEX), ()) ");
    		stb.append(" ORDER BY ");
    		stb.append("   SEX ");
        } else {
        	stb.append(" SELECT ");
        	stb.append("   T1.SEX, ");
        	//stb.append("   T1.COURSECODE, ");    //この学校は、1年の時には1コースしかないはずなので、複数のCOURSECODEは無い前提。
        	stb.append("   T1.TESTSUBCLASSCD, ");
        	stb.append("   T2.TESTSUBCLASS_NAME, ");
        	stb.append("   T1.HIGHSCORE, ");
        	stb.append("   T1.LOWSCORE, ");
        	stb.append("   T1.AVG, ");
        	stb.append("   T1.COUNT ");
        	stb.append(" FROM ");
        	stb.append("   ENTEXAM_TOKEI_HIGH_LOW_HISTORY_DAT T1 ");
        	stb.append("   LEFT JOIN ENTEXAM_TESTSUBCLASSCD_DAT T2 ");
        	stb.append("     ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        	stb.append("    AND T2.APPLICANTDIV = T1.APPLICANTDIV ");
        	stb.append("    AND T2.TESTDIV = T1.TESTDIV ");
        	stb.append("    AND T2.EXAM_TYPE = '1' ");
        	stb.append("    AND T2.TESTSUBCLASSCD = T1.TESTSUBCLASSCD ");
        	stb.append(" WHERE ");
        	stb.append("   T1.ENTEXAMYEAR = '" + _param._examYear + "' ");
        	stb.append("   AND T1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        	stb.append("   AND T1.TESTDIV = '" + _param._testDiv + "' ");
        	stb.append("   AND T1.COURSECODE = '0000' ");  //コースが1コースしかない学校なので、コードは固定で設定されている。
        	stb.append("   AND T1.SHDIV = '1' ");
        	stb.append(" ORDER BY ");
        	stb.append("   T1.SEX, ");
        	stb.append("   T1.TESTSUBCLASSCD ");
		}
    	return stb.toString();
    }

    // 受験者取得
    private Map getApplicantMap(final DB2UDB db2, final Map attendMap) throws SQLException {
    	Map retMap = new LinkedMap();
    	PreparedStatement ps = null;
        ResultSet rs = null;
        final String sql = getApplicantMapSql();
		log.debug(" getApplicantMap sql =" + sql);

		Attend attendObj = null;
		Map subMap = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

			while (rs.next()) {
				final String examno = rs.getString("EXAMNO");
				final String sex = rs.getString("SEX");
				final String judgement = rs.getString("JUDGEMENT");
	            final String testSubclsCd = rs.getString("TESTSUBCLASSCD");
				final String total4 = rs.getString("TOTAL4");
				final String attendFlg = rs.getString("ATTEND_FLG");
				final String concurrentAppFlg = rs.getString("CONCURRENT_APP_FLG");
				final String multiAppFlg = rs.getString("MULTI_APP_FLG");

				final Applicant applicant = new Applicant(examno, sex, judgement, testSubclsCd, total4, attendFlg, concurrentAppFlg, multiAppFlg);

				if (total4 != null || ABSENT.equals(attendFlg)) {
					////設定1:出欠&(メインループを回すための)試験種別
					if (!attendMap.containsKey(testSubclsCd)) {
						final SubclassMst testSubclsNameObj = _param._subclassMap.containsKey(testSubclsCd) ? (SubclassMst)_param._subclassMap.get(testSubclsCd) : null;
						final String testSubclsName = testSubclsNameObj == null ? "" : testSubclsNameObj._subclassName;
						attendObj = new Attend(testSubclsCd, testSubclsName);
						attendMap.put(testSubclsCd, attendObj);
					}
					attendObj = (Attend)attendMap.get(testSubclsCd);

					if (ABSENT.equals(attendFlg)) {
						attendObj._absent++;
					} else {
						attendObj._attend++;
						////設定2:メイン
						int putIdx1 = _param.getKizamiIndex(new BigDecimal(total4), SEARCH_SCOREMAP);
					    if (putIdx1 >= 0) {
					    	final KizamiCls putWk = (KizamiCls)_param._scoreKizamiList.get(putIdx1);
					    	if (!putWk._studentInfo.containsKey(testSubclsCd)) {
					    		subMap = new LinkedMap();
					    		putWk._studentInfo.put(testSubclsCd, subMap);
					    	} else {
					    		subMap = (Map)putWk._studentInfo.get(testSubclsCd);
					    	}
					    	subMap.put(examno, applicant);
					    }
					    int putIdx2 = _param.getKizamiIndex(new BigDecimal(total4), SEARCH_GLASSMAP);
					    if (putIdx2 >= 0) {
					    	final KizamiCls putWk = (KizamiCls)_param._glassKizamiList.get(putIdx2);
					    	if (!putWk._studentInfo.containsKey(testSubclsCd)) {
					    		subMap = new LinkedMap();
					    		putWk._studentInfo.put(testSubclsCd, subMap);
					    	} else {
					    		subMap = (Map)putWk._studentInfo.get(testSubclsCd);
					    	}
					    	subMap.put(examno, applicant);
					    }
					}
				}
			    if(!retMap.containsKey(examno)) {
			    	retMap.put(examno, applicant);
			    }
			}
		} catch (final SQLException e) {
			log.error("志願者の基本情報取得でエラー", e);
			throw e;
		} finally {
			db2.commit();
			DbUtils.closeQuietly(null, ps, rs);
		}

    	return retMap;
    }

    private String getApplicantMapSql() {
		final StringBuffer stb = new StringBuffer();
		stb.append(" SELECT ");
		stb.append("   BASE.EXAMNO, ");
		stb.append("   BASE.SEX, ");
		stb.append("   BASE.JUDGEMENT, ");
        if (SUMTYPE_SUBCLS.equals(_param._totalDiv)) {
        	stb.append("     ESD.TESTSUBCLASSCD, ");
            stb.append("     ESD.SCORE AS TOTAL4, ");
            stb.append("     ESD.ATTEND_FLG, ");
        } else {
        	stb.append("     '99' AS TESTSUBCLASSCD, ");
			stb.append("     FLOAT(RD009.REMARK6) AS TOTAL4, ");
            stb.append("     CASE WHEN REC.JUDGEDIV = '5' THEN 1 ELSE 0 END AS ATTEND_FLG, ");
		}
		stb.append("   DTL3.REMARK2 AS CONCURRENT_APP_FLG, ");
		stb.append("   DTL3.REMARK3 AS MULTI_APP_FLG ");
		stb.append(" FROM ");
		stb.append("   ENTEXAM_APPLICANTBASE_DAT BASE ");
		stb.append(" LEFT JOIN ");
		stb.append("   ENTEXAM_APPLICANTBASE_DETAIL_DAT DTL3 ");
		stb.append("   ON DTL3.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
		stb.append("  AND DTL3.APPLICANTDIV = BASE.APPLICANTDIV ");
		stb.append("  AND DTL3.EXAMNO = BASE.EXAMNO ");
		stb.append("  AND DTL3.SEQ = '005' ");
		stb.append(" LEFT JOIN ");
		stb.append("   ENTEXAM_RECEPT_DAT REC ");
		stb.append("   ON REC.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
		stb.append("  AND REC.APPLICANTDIV = BASE.APPLICANTDIV ");
		stb.append("  AND REC.TESTDIV = BASE.TESTDIV ");
		stb.append("  AND REC.EXAM_TYPE = '1' ");
		stb.append("  AND REC.EXAMNO = BASE.EXAMNO ");
		stb.append(" LEFT JOIN ");
		stb.append("   ENTEXAM_RECEPT_DETAIL_DAT RD009 ");
		stb.append("   ON RD009.ENTEXAMYEAR = REC.ENTEXAMYEAR ");
		stb.append("  AND RD009.APPLICANTDIV = REC.APPLICANTDIV ");
		stb.append("  AND RD009.TESTDIV = REC.TESTDIV ");
		stb.append("  AND RD009.EXAM_TYPE = REC.EXAM_TYPE ");
		stb.append("  AND RD009.RECEPTNO = REC.RECEPTNO ");
		stb.append("  AND RD009.SEQ = '009' ");
        if (SUMTYPE_SUBCLS.equals(_param._totalDiv)) {
		    stb.append(" LEFT JOIN ");
		    stb.append("   ENTEXAM_SCORE_DAT ESD ");
		    stb.append("   ON ESD.ENTEXAMYEAR = REC.ENTEXAMYEAR ");
		    stb.append("  AND ESD.APPLICANTDIV = REC.APPLICANTDIV ");
		    stb.append("  AND ESD.TESTDIV = REC.TESTDIV ");
		    stb.append("  AND ESD.EXAM_TYPE = '1' ");
		    stb.append("  AND ESD.RECEPTNO = REC.RECEPTNO ");
		    stb.append(" INNER JOIN ");
		    stb.append("   ENTEXAM_TESTSUBCLASSCD_DAT ETSD ");
		    stb.append("   ON ETSD.ENTEXAMYEAR = ESD.ENTEXAMYEAR ");
		    stb.append("  AND ETSD.APPLICANTDIV = ESD.APPLICANTDIV ");
		    stb.append("  AND ETSD.TESTDIV = ESD.TESTDIV ");
		    stb.append("  AND ETSD.EXAM_TYPE = '1' ");
		    stb.append("  AND ETSD.TESTSUBCLASSCD = ESD.TESTSUBCLASSCD ");
		    stb.append("  AND ETSD.TESTSUBCLASS_NAME IS NOT NULL ");
		    stb.append("  AND ETSD.REMARK2 IS NULL ");
        }
		stb.append(" WHERE ");
		stb.append("   BASE.ENTEXAMYEAR = '" + _param._examYear + "' ");
		stb.append("   AND BASE.APPLICANTDIV = '" + _param._applicantDiv + "' ");
		stb.append("   AND BASE.TESTDIV = '" + _param._testDiv + "' ");

		//合否区分
		if(!PUDIV_FULL.equals(_param._judgementDiv)) {
			if (PUDIV_IGNORESUC.equals(_param._judgementDiv)) {
			    stb.append("     AND BASE.JUDGEMENT <> '1' ");
			}
		}
		//性別
		if(!SEX_NORM.equals(_param._sex)) {
			if (SEX_MALE.equals(_param._sex)) {
				stb.append("     AND BASE.SEX = '1' ");
			} else if (SEX_FEMALE.equals(_param._sex)) {
				stb.append("     AND BASE.SEX = '2' ");
			}
		}
		//重複出願
		if (!APP_FLG_FULL.equals(_param._decision)) {
			//DTL3.REMARK2=CONCURRENT_APP_FLG(同時),REMARK3=MULTI_APP_FLG(複数),REMARK4=OverAge(年齢過多)
			if (APP_FLG_ONE.equals(_param._decision)) {
				stb.append("     AND VALUE(DTL3.REMARK2, '') = '' AND VALUE(DTL3.REMARK3, '') = '' ");
			} else {
			    if (APP_FLG_CONC.equals(_param._decision)) {
					stb.append("     AND VALUE(DTL3.REMARK3, '') = '1' ");
				} else if (APP_FLG_MULTI.equals(_param._decision)) {
					stb.append("     AND VALUE(DTL3.REMARK2, '') = '1' ");
				} else {
					stb.append("     AND VALUE(DTL3.REMARK2, '') = '1' OR VALUE(DTL3.REMARK3, '') = '1' ");
				}
			}
		}

		stb.append(" ORDER BY ");
        if (SUMTYPE_SUBCLS.equals(_param._totalDiv)) {
        	stb.append("     ESD.TESTSUBCLASSCD, ");
            stb.append("     ESD.SCORE DESC");
        } else {
			stb.append("     FLOAT(RD009.REMARK6) DESC ");
		}
        return stb.toString();
    }

    private class TotalScoreInfo {
    	final String _sex;
    	final String _testSubclassCd;
    	final String _testSubclassName;
    	final String _highScore;
    	final String _lowScore;
    	final String _avg;
    	final String _count;
    	TotalScoreInfo(final String sex, final String testSubclassCd, final String testSubclassName, final String highScore, final String lowScore, final String avg, final String count) {
    		_sex = sex;
        	_testSubclassCd = testSubclassCd;
        	_testSubclassName = testSubclassName;
        	_highScore = highScore;
        	_lowScore = lowScore;
        	_avg = avg;
        	_count = count;
    	}
    }

    private class SubclassMst {
    	final String _subclassCd;
    	final String _subclassName;
    	final String _remark2;

    	public SubclassMst(final String subclassCd, final String subclassName, final String remark2) {
    		_subclassCd = subclassCd;
    		_subclassName = subclassName;
    		_remark2 = remark2;
    	}
    }

    private class Attend {
    	final String _testSubclsCd;
    	final String _testSubclsName;
    	int _attend;
    	int _absent;
    	Attend(final String testSubclsCd, final String testSubclsName) {
    		_testSubclsCd = testSubclsCd;
    		_testSubclsName = testSubclsName;
    		_attend = 0;
    		_absent = 0;
    	}
    }

    private class Applicant {
        final String _examno;
        final String _sex;
        final String _judgement;
        final String _testSubclsCd;
        final String _total4; //得点
        final String _attendFlg; //欠席
        final String _concurrentAppFlg;
        final String _multiAppFlg;

		public Applicant(final String examno, final String sex, final String judgement, final String testSubclsCd, final String total4, final String attendFlg, final String concurrentAppFlg, final String multiAppFlg) {
		    _examno = examno;
		    _sex = sex;
		    _judgement = judgement;
		    _testSubclsCd = testSubclsCd;
		    _total4 = total4;
		    _attendFlg = attendFlg;
		    _concurrentAppFlg = concurrentAppFlg;
		    _multiAppFlg = multiAppFlg;
		}
    }

    private class KizamiCls {
    	private final BigDecimal _kE;
    	private final BigDecimal _kS;
    	private final Map _studentInfo;
    	KizamiCls(final BigDecimal kS, final BigDecimal kE) {
    		_kS = kS;
    		_kE = kE;
    		_studentInfo = new LinkedMap();
    	}
    	public String getRangeStr() {
    		return isSamePoint() ? _kE.toString() : insSpVal(_kE.toString(), 3) + "～" + insSpVal(_kS.toString(), 3);
    	}
    	private String insSpVal(final String valStr, final int maxLen) {
    		int intLen = 0;
    		if ("".equals(StringUtils.defaultString(valStr))) {
    			return "";
    		}
    		if (valStr.indexOf(".") >= 0) {
    			intLen = maxLen - valStr.indexOf(".");
    		} else {
    			intLen = maxLen - valStr.length();
    		}
    		return StringUtils.repeat(" ", intLen) + valStr;
    	}
    	public boolean isSamePoint() {
    		return _kS.equals(_kE) ? true : false;
    	}
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 77567 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _examYear;
        final String _applicantDiv; //入試制度
        final String _testDiv; //入試区分
        final String _testDivName; //入試区分名称
        final String _date;
        final Map _subclassMap; //試験教科名

        final String _judgementDiv;  //合否区分
        final String _sex;           //性別
        final String _decision;      //重複出願
        final String _totalDiv;      //集計区分

        final String _score_S;       //出力点下限値
        final String _score_E;       //出力点上限値
        final String _scoreKizami;   //出力点刻み
        final List _scoreKizamiList;    //刻みマップ

        final String _glass_S;       //虫眼鏡下限値
        final String _glass_E;       //虫眼鏡上限値
        final String _glassKizami;   //虫眼鏡刻み
        final List _glassKizamiList;    //刻みマップ
        final boolean _isPrintGlass;
        final boolean _isHschoolSuisen;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
        	_judgementDiv = request.getParameter("JUDGMENT_DIV");  //合否区分
        	_sex = request.getParameter("SEX");  //性別
        	_decision = request.getParameter("DECISION");  //重複出願
        	_totalDiv = request.getParameter("TOTAL_DIV");  //集計区分

        	_score_S = request.getParameter("SCORE_S");  //出力点範囲
        	_score_E = request.getParameter("SCORE_E");
        	_scoreKizami = request.getParameter("SCORE_KIZAMI");

        	_glass_S = request.getParameter("MUSHIMEGANE_S");  //虫眼鏡
        	_glass_E = request.getParameter("MUSHIMEGANE_E");
        	_glassKizami = request.getParameter("MUSHIMEGANE_KIZAMI");

       	    //虫眼鏡の終了と刻みが、0、0.0、空文字ならそもそも表として出せないので出力しない(これで虫眼鏡の表の出力制御とする)。
        	if ("0.0".equals(StringUtils.defaultString(_glassKizami, "0.0")) || ("".equals(_glassKizami) || "0".equals(_glassKizami))) {
                _isPrintGlass = false;
        	} else if ("0.0".equals(StringUtils.defaultString(_glass_E, "0.0")) || ("".equals(_glass_E) ||"0".equals(_glass_E))) {
                _isPrintGlass = false;
        	} else {
                _isPrintGlass = true;
        	}

        	_examYear = request.getParameter("ENTEXAMYEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
        	_isHschoolSuisen = "2".equals(_applicantDiv) && "01".equals(_testDiv) ? true : false;

          	_date = request.getParameter("LOGIN_DATE");
          	_testDivName = getTestDivAbbv(db2);
          	_subclassMap = getSubclassName(db2);
          	_scoreKizamiList = getKizamiList(_score_S, _score_E, _scoreKizami, SEARCH_SCOREMAP);
          	_glassKizamiList = getKizamiList(_glass_S, _glass_E, _glassKizami, SEARCH_GLASSMAP);
        }

        private String getUseSexCd() {
        	//ENTEXAM_TOKEI_HIGH_LOW_HISTORY_DATの性別が下記のコード体系となっていため、
        	//ここで指示画面指定の性別からテーブル指定のコードへ変換を行っている。
        	return SEX_NORM.equals(_sex) ? "9" : SEX_MALE.equals(_sex) ? "1" : "2";
        }

        private List getKizamiList(final String kizami_S, final String kizami_E, final String kizami, final boolean scoreFlg) {

        	final List retList = new ArrayList();
        	if (scoreFlg == SEARCH_GLASSMAP && !_isPrintGlass) return retList;

        	final BigDecimal kS = new BigDecimal(kizami_S);
        	final BigDecimal kE = new BigDecimal(kizami_E);
        	final BigDecimal kZ = new BigDecimal(kizami);

        	////// 帳票構成は下記。
        	////// 表A             表B
        	////// 100             100 - 70.0 ＊
        	////// 99.9 - 95.0     69.9 - 60.0
        	////// 94.9 - 90.0     59.9 - 50.0
        	////// ...             ...
        	////// ...             30.0 - 0.0 ＊
        	////// 4.9 - 0.0
        	//////
        	////// ここでは、各表毎に呼び出しを行っていて、その行毎(範囲毎)の格納先を作成している。
        	////// それぞれの表で上限、下限が設定されている。
        	////// 特に表B(=虫眼鏡)の場合、虫眼鏡の範囲外を上下、つまり＊の部分を始めと終わりに付ける。
        	////// 最上位の値についてはその値だけの行として、格納先を作成する。

        	//虫眼鏡なら、眼鏡の範囲外データ(下限)を入れる。
        	if (!scoreFlg && !_score_E.equals(kizami_E)) {
        		KizamiCls addWk = new KizamiCls(kE.setScale(1), new BigDecimal(_score_E).setScale(1));
        		retList.add(addWk);
        	} else {
    		    KizamiCls addWk = new KizamiCls(kE.setScale(1), kE.setScale(1));
    		    retList.add(addWk);
        	}

        	for (BigDecimal kzWk = kE;kzWk.compareTo(kS) >= 0;kzWk = kzWk.subtract(kZ)) {
        		final BigDecimal wE = kzWk.subtract(new BigDecimal("0.1"));
        		final BigDecimal wS = kzWk.subtract(kZ).compareTo(kS) < 0 ? kS : kzWk.subtract(kZ);
        		KizamiCls addWk = new KizamiCls(wS.setScale(1), wE);
        		retList.add(addWk);
        		if (wS.compareTo(kS) <= 0) {
        			break;
        		}
        	}

        	//虫眼鏡なら、眼鏡の範囲外データ(上限)を入れる。
        	if (!scoreFlg) {
        		if (!_score_S.equals(kizami_S)) {
        		    KizamiCls addWk = new KizamiCls(new BigDecimal(_score_S).setScale(1), kS.setScale(1));
        		    retList.add(addWk);
        		}
        	}

        	return retList;
        }

    	public int getKizamiIndex(final BigDecimal setVal, final boolean searchScoreMapFlg) {
    		final List searchMap;
    		if (searchScoreMapFlg == SEARCH_SCOREMAP) {
    			searchMap = _scoreKizamiList;
    		} else {
    			searchMap = _glassKizamiList;
    		}
    		int retVal = -1;
    		if (setVal == null || searchMap == null) return retVal;
    		int retIdx = 0;
    		for (Iterator ite = searchMap.iterator();ite.hasNext();) {
    			final KizamiCls srchObj = (KizamiCls)ite.next();
    			if (srchObj._kS.compareTo(setVal) <= 0 && setVal.compareTo(srchObj._kE) <= 0) {
    				retVal = retIdx;
    				break;
    			}
    			retIdx++;
    		}
    		return retVal;
    	}

    	public int getKizamiFullRateCnt(final String testSubclsCd, final boolean searchScoreMapFlg) {
    		return getKizamiFullRateCnt(testSubclsCd,searchScoreMapFlg,"");
    	}

    	public int getKizamiFullRateCnt(final String testSubclsCd, final boolean searchScoreMapFlg, final String chkInfoFlg) {
    		final List searchMap;
    		if (searchScoreMapFlg == SEARCH_SCOREMAP) {
    			searchMap = _scoreKizamiList;
    		} else {
    			searchMap = _glassKizamiList;
    		}
    		int retVal = 0;
    		if (searchMap == null) return retVal;
    		for (Iterator ite = searchMap.iterator();ite.hasNext();) {
    			final KizamiCls srchObj = (KizamiCls)ite.next();
    			if (!srchObj._studentInfo.containsKey(testSubclsCd)) {
    				continue;
    			}
    			final Map subMap = (Map)srchObj._studentInfo.get(testSubclsCd);
    			if (SEX_NORM.equals(_sex) && "".equals(chkInfoFlg)) {
        			retVal += subMap.size();
    			} else {
        			for (Iterator itr = subMap.keySet().iterator();itr.hasNext();) {
        				final String exno = (String)itr.next();
        				final Applicant datData = (Applicant)subMap.get(exno);
        				boolean chkOkFlg = true;
        				if (!"".equals(chkInfoFlg)) {
        					if (ATTENDCHK.equals(chkInfoFlg)) {
        						//出席をチェックしているので、欠席ならfalse
        						if (ABSENT.equals(datData._attendFlg)) {
        							chkOkFlg = false;
        						}
        					}
        					if (ABSENTCHK.equals(chkInfoFlg)) {
        						//欠席をチェックしているので、欠席でないならfalse
        						if (!ABSENT.equals(datData._attendFlg)) {
        							chkOkFlg = false;
        						}
        					}
        				}
        				if (chkOkFlg) {
            				if (SEX_MALE.equals(_sex) && SEX_MAN.equals(datData._sex)) {
                    			retVal++;
            				} else if (SEX_FEMALE.equals(_sex) && SEX_WOMAN.equals(datData._sex)) {
                    			retVal++;
            				} else {
                    			retVal++;
            				}
        				}
        			}
    			}
    		}
    		return retVal;
    	}

        private String getTestDivAbbv(final DB2UDB db2) {
        	return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT TESTDIV_NAME FROM ENTEXAM_TESTDIV_MST WHERE ENTEXAMYEAR = '" + _examYear + "' AND APPLICANTDIV = '" + _applicantDiv + "' AND TESTDIV = '" + _testDiv + "' "));
        }

        private Map getSubclassName(final DB2UDB db2) {
        	final Map retMap = new LinkedMap();
        	final StringBuffer stb = new StringBuffer();
        	stb.append(" SELECT ");
        	stb.append("     T1.TESTSUBCLASSCD, ");
        	stb.append("     T1.TESTSUBCLASS_NAME, ");
        	stb.append("     T1.REMARK2 ");
        	stb.append(" FROM ");
        	stb.append("     ENTEXAM_TESTSUBCLASSCD_DAT T1 ");
        	stb.append(" WHERE ");
        	stb.append("     T1.ENTEXAMYEAR = '" + _examYear + "' ");
        	stb.append("     AND T1.APPLICANTDIV = '" + _applicantDiv + "' ");
        	stb.append("     AND T1.TESTDIV = '" + _testDiv + "' ");
        	stb.append("     AND T1.EXAM_TYPE= '1' ");
        	stb.append(" ORDER BY ");
        	stb.append("     T1.TESTSUBCLASSCD ");

    		final List sqlWkLst = KnjDbUtils.query(db2, stb.toString());
    		for (Iterator ite = sqlWkLst.iterator();ite.hasNext();) {
    			final Map row = (Map)ite.next();
        		final String cd = KnjDbUtils.getString(row, "TESTSUBCLASSCD");
        		final String name = KnjDbUtils.getString(row, "TESTSUBCLASS_NAME");
        		final String remark2 = KnjDbUtils.getString(row, "REMARK2");
        		if(!retMap.containsKey(cd)) {
        			SubclassMst subclass = new SubclassMst(cd,name,remark2);
        			retMap.put(cd, subclass);
        		}
        	}
        	return retMap;
        }
    }


}

// eof
