/*
 * $Id: d17e8b3004ec9bb728cefa0b64cc1418575a2da4 $
 *
 * 作成日: 2015/08/05
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;


import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

/**
 * 文京学園 累積科目別 得点分布表
 */
public class KNJD623B {

    private static final Log log = LogFactory.getLog(KNJD623B.class);

    private boolean _hasData;

    private Param _param;

    private static final String SEMEALL = "9";

    private static final int MAXDISP_SUBCLASS = 34;

    private static final int MAXDISP_COL = 2;
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
    	int colCnt = 0;
    	final Map hrClsMap = getHrClsGrpInfo(db2);  //hrClassGrp(M) - hrClassInfo(L) - student(M)

    	svf.VrSetForm("KNJD623B.frm", 4);
    	setTitle(db2, svf);
    	for (Iterator ite = hrClsMap.keySet().iterator();ite.hasNext();) {
    		final String kStr = (String)ite.next();
    		final CourseGroupInfo prtBaseObj = (CourseGroupInfo)hrClsMap.get(kStr);
            if (prtBaseObj._hrClassMap.size() == 0) {
            	continue;
            }
            //何列利用するか、チェックする。
            if (colCnt + prtBaseObj._hrClassMap.size() + 1 > MAXDISP_COL) {
            	svf.VrEndPage();
            	svf.VrSetForm("KNJD623B.frm", 4);
            	colCnt = 0;
            	setTitle(db2, svf);

            }
            colCnt += prtBaseObj._hrClassMap.size() + 1;
    		//一貫/特進(A/B)/進学
    		svf.VrsOut("COURSE_NAME", prtBaseObj._courseGrpName);
            svf.VrsOut("ENROLL_NUM_NAME", "在籍者数");
            svf.VrsOut("EXAM_NUM_NAME", "受験者数");
            svf.VrsOut("ATTEND_RATE_NAME", "出席率");
            svf.VrsOut("HR_AVE_NAME", "クラス平均");
            colCnt++;

    		//科目名称出力
    		int prtCnt = 0;
    		for (Iterator itscm = prtBaseObj._subclsMergeMap.keySet().iterator();itscm.hasNext();) {
    			final String scmStr = (String)itscm.next();
    			final SubClsInfo prtwk = (SubClsInfo)prtBaseObj._subclsMergeMap.get(scmStr);
    			int pWkCnt;
    			if (prtwk._dispLastFlg != null && "1".equals(prtwk._dispLastFlg)) {
    				pWkCnt = prtBaseObj.subclsLastDispIdx(prtwk);
    			} else {
    				pWkCnt = ++prtCnt;
    			}
    			if (pWkCnt > 0) {
    			    svf.VrsOutn("SUBCLASS_NAME", pWkCnt, StringUtils.defaultString(prtwk._subclassname, ""));
    			}
    		}
    		svf.VrEndRecord();

    		String bakHrClass = "";
    		int ketuPrtCnt = 0;
    		//各種データ
    		for (Iterator itr = prtBaseObj._hrClassMap.keySet().iterator();itr.hasNext();) {  //クラス
    			final String hrClsStr = (String)itr.next();
    			final HrClsGrpInfo prtWk = (HrClsGrpInfo)prtBaseObj._hrClassMap.get(hrClsStr);
		        if (!bakHrClass.equals(prtWk._hr_Class)) {
    			    //担任名1,2
    			    svf.VrsOut("TR_NAME1", StringUtils.substring(prtWk._hr_Class, 1) + " " + StringUtils.defaultString(prtWk._staff1, ""));
    			    if ("J".equals(_param._schoolKind)) {//中学だったら担任2も出力
    			        svf.VrsOut("TR_NAME2", "   " + prtWk._staff2);
    			    }
		        }
    			//在籍者数
    			svf.VrsOut("ENROLL_NUM", prtWk._zaisekisya);
    			//受験者数
    			svf.VrsOut("EXAM_NUM", prtWk._jyukensya);
    			if (prtWk._courseNameList != null) {
    		        for (Iterator its = prtWk._courseNameList.iterator();its.hasNext();) {
    		            final CourseNameInfo cnInfo = (CourseNameInfo)its.next();
    		            //文系/理系
    		            svf.VrsOut("SUBCOURSE_NAME", cnInfo._courseAbbv);
    		            if (prtWk._courseInfoMap.containsKey(cnInfo._coursecode)) {
    			        	final Map subclsMap = (Map)prtWk._courseInfoMap.get(cnInfo._coursecode);
    		        		boolean printFstFlg = false;
    			        	for (Iterator itt = subclsMap.keySet().iterator();itt.hasNext();) {
    			        		final String scStr = (String)itt.next();
    			        		final SubClsInfo subclsPrtWk = (SubClsInfo)subclsMap.get(scStr);
    			        		if (!printFstFlg) {
    	    			            //出席率
    	    			            svf.VrsOut("ATTEND_RATE", prtWk._att.calcAttRate().toString());
    	    			            printFstFlg = true;
    			        		}
    			        		if ("999999".equals(subclsPrtWk._subclasscd)) {
    	                            //クラス平均
    	                            svf.VrsOut("HR_AVE", subclsPrtWk._avg);
    			        	    } else {
    			        			//何行目に出力するかをチェック
    			        	    	int nCnt = 0;
    			        	    	boolean findFlg = false;
    			            		for (Iterator itscm = prtBaseObj._subclsMergeMap.keySet().iterator();itscm.hasNext();) {
    			            			final String scmStr = (String)itscm.next();
    			            			final SubClsInfo chkwk = (SubClsInfo)prtBaseObj._subclsMergeMap.get(scmStr);
    			            			++nCnt;
    			            			if (chkwk.getSubclsCd().equals(subclsPrtWk.getSubclsCd())) {
    			            				findFlg = true;
    			            				break;
    			            			}
    			            		}
    			        	    	//科目別クラス平均
    			            		if (findFlg) {
    			            			final BigDecimal bgPutStr = new BigDecimal(subclsPrtWk._avg).setScale(1, BigDecimal.ROUND_HALF_UP);
    			            			if (subclsPrtWk._dispLastFlg != null && "1".equals(subclsPrtWk._dispLastFlg)) {
    			            				final int putIdx = prtBaseObj.subclsLastDispIdx(subclsPrtWk);
    			            				if (putIdx > 0) {
    				    				        svf.VrsOutn("SUBCLASS_AVE", putIdx, bgPutStr.toString());
    			            				}
    			            			} else {
    				    				    svf.VrsOutn("SUBCLASS_AVE", nCnt, bgPutStr.toString());
    			            			}
    			            		}
    			        	    }
    			        	}
    		            }
    		        }
    			}
    			//上位5名の全科目平均
		        if (prtWk._avgDataList != null && prtWk._avgDataList.size() > 0) {
			        List top5List = getListTop5(prtWk._avgDataList);
	    			for (int aCnt = 0;aCnt < top5List.size();aCnt++) {
	    				ClsDetailInfo t5Wk = (ClsDetailInfo)top5List.get(aCnt);
	    				if (t5Wk != null && t5Wk._avg != null) {
	    				    BigDecimal t5avg = new BigDecimal(t5Wk._avg).setScale(1, BigDecimal.ROUND_HALF_UP);
	    				    svf.VrsOutn("RANK_AVE", aCnt+1, t5avg.toString());
	    				}
	    			}
	    			//下位5名の全科目平均
			        List last5List = getListLast5(prtWk._avgDataList);
	    			for (int aCnt = 0;aCnt < last5List.size();aCnt++) {
	    				ClsDetailInfo l5Wk = (ClsDetailInfo)top5List.get(aCnt);
	    				if (l5Wk != null && l5Wk._avg != null) {
	    					BigDecimal l5avg = new BigDecimal(l5Wk._avg).setScale(1, BigDecimal.ROUND_HALF_UP);
	    				    svf.VrsOutn("RANK_AVE", aCnt+6, l5avg.toString());
	    				}
	    			}
	    			//30点未満の人数
	    			svf.VrsOut("RANK_LOW_NUM", String.valueOf(getCntUnder30(prtWk._avgDataList)));
		        }
		        if (prtWk._testKetuList != null && prtWk._testKetuList.size() > 0) {
    			    //備考
    			    for (int aCnt = 0;aCnt < 5;aCnt++) {
    				    if (ketuPrtCnt < prtWk._testKetuList.size()) {
    			    	    KetuInfo kWk = (KetuInfo)prtWk._testKetuList.get(ketuPrtCnt);

    			            svf.VrsOutn("REMARK", aCnt, kWk.cutNameStr(4) + kWk._ketu_Cnt + "欠");
    				        ketuPrtCnt++;
    				    }
    			    }
    			}
		        bakHrClass = prtWk._hr_Class;
	            colCnt++;
    			_hasData = true;
        		svf.VrEndRecord();
    		}
    	}
    }

    private void setTitle(final DB2UDB db2, final Vrw32alp svf) {
    	svf.VrsOut("TITLE", _param._gradeName + _param._semestername + "中間成績資料");

    	String detDate = StringUtils.defaultString(KNJ_EditDate.getAutoFormatDate(db2, _param._loginDate), "");
    	if (!"".equals(detDate)) {
    	    Date date1 = new Date();
    	    SimpleDateFormat sdformat1 = new SimpleDateFormat("HH:mm");
    	    detDate += " " + sdformat1.format(date1);
    	}

    	svf.VrsOut("DATE", "作成日時:" + detDate);
    }
    private List getListTop5(final List chkList) {
    	List retList = new ArrayList();
    	for (Iterator ite = chkList.iterator();ite.hasNext();) {
    		ClsDetailInfo chkWk = (ClsDetailInfo)ite.next();
    		if (Integer.parseInt(chkWk._id) <= 5) {
    			retList.add(chkWk);
    		}
    	}
    	//5人以上(最初の5人を残すので、それ以外は外す)
    	if (retList.size() > 5) {
        	int nCnt = 0;
    		for (Iterator ite = retList.iterator();ite.hasNext();) {
        		ClsDetailInfo rmvWk = (ClsDetailInfo)ite.next();
        		nCnt++;
        		if (nCnt > 5) {
        			ite.remove();
        		}
        	}
    	}
    	return retList;
    }
    private List getListLast5(final List chkList) {
    	//一度5位以内を取得
    	List retList = new ArrayList();
    	for (Iterator ite = chkList.iterator();ite.hasNext();) {
    		ClsDetailInfo chkWk = (ClsDetailInfo)ite.next();
    		if (Integer.parseInt(chkWk._rev_Id) <= 5) {
    			retList.add(chkWk);
    		}
    	}
    	//5人以上(最後の5人を残すので、それ以外は外す)
    	if (retList.size() > 5) {
        	int nCnt = retList.size();
    		for (Iterator itw = retList.iterator();itw.hasNext();) {
    			ClsDetailInfo rmvWk = (ClsDetailInfo)itw.next();
        		if (nCnt > 5) {
        			itw.remove();
        		}
        		nCnt--;
        	}
    	}
    	return retList;
    }
    private int getCntUnder30(final List chkList) {
    	List retList = new ArrayList();
    	for (Iterator ite = chkList.iterator();ite.hasNext();) {
    		ClsDetailInfo chkWk = (ClsDetailInfo)ite.next();
    		if ("1".equals(chkWk._under_30)) {
    			retList.add(chkWk._attendno);
    		}
    	}
    	return retList.size();
    }
    private Map getHrClsGrpInfo(final DB2UDB db2) {
    	final Map clsDetailMap = getClsDetailInfo(db2);
    	final Map clsKetuMap = getClsKetuInfo(db2);
    	final Map clsAttendMap = getAttendSemes(db2);
    	final Map clscourseMap = getCourseMap(db2);
    	final Map retMap = new LinkedMap();

        final StringBuffer stb1 = new StringBuffer();
        stb1.append(" WITH SCHNO_A AS( ");
        stb1.append(" SELECT DISTINCT ");
        stb1.append("   T1.YEAR, T1.SEMESTER,T1.SCHREGNO, T1.GRADE, T1.HR_CLASS, T1.ATTENDNO, T1.COURSECD, T1.MAJORCD, T1.COURSECODE, CGRP.GROUP_CD, CGRPH.GROUP_NAME ");
        stb1.append(" FROM ");
        stb1.append("   SCHREG_REGD_DAT T1 ");
        stb1.append("   INNER JOIN SCHREG_REGD_HDAT TH ");
        stb1.append("     ON TH.YEAR = T1.YEAR ");
        stb1.append("    AND TH.SEMESTER = T1.SEMESTER ");
        stb1.append("    AND TH.GRADE = T1.GRADE ");
        stb1.append("    AND TH.HR_CLASS = T1.HR_CLASS ");
        stb1.append("   LEFT JOIN SEMESTER_MST T2  ");
        stb1.append("     ON T1.YEAR = T2.YEAR ");
        stb1.append("    AND T1.SEMESTER = T2.SEMESTER ");
        stb1.append("   INNER JOIN COURSE_GROUP_CD_DAT CGRP ON CGRP.YEAR = T1.YEAR ");
        stb1.append("    AND CGRP.GRADE = T1.GRADE ");
        stb1.append("    AND CGRP.COURSECD = T1.COURSECD ");
        stb1.append("    AND CGRP.MAJORCD = T1.MAJORCD ");
        stb1.append("    AND CGRP.COURSECODE = T1.COURSECODE ");
        stb1.append("   INNER JOIN COURSE_GROUP_CD_HDAT CGRPH ON CGRPH.YEAR = CGRP.YEAR ");
        stb1.append("    AND CGRPH.GRADE = CGRP.GRADE ");
        stb1.append("    AND CGRPH.GROUP_CD = CGRP.GROUP_CD ");
        stb1.append(" WHERE ");
        stb1.append("   T1.YEAR = '" + _param._year + "' ");
        stb1.append("   AND T1.SEMESTER = '" + _param._semester + "' ");
        stb1.append("   AND T1.GRADE = '" + _param._grade + "' ");
        stb1.append("   AND '" + _param._eDate + "' BETWEEN T2.SDATE AND T2.EDATE ");
        stb1.append("   AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST S1 ");
        stb1.append("                  WHERE S1.SCHREGNO = T1.SCHREGNO ");
        stb1.append("                    AND ((S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < CASE WHEN T2.EDATE < '" + _param._eDate + "' THEN T2.EDATE ELSE '"+_param._sDate+"' END) ");
        stb1.append("                    OR (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE > CASE WHEN T2.EDATE < '" + _param._sDate + "' THEN T2.EDATE ELSE '" + _param._sDate + "' END)) ) ");
        stb1.append("   AND NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT S1 ");
        stb1.append("                  WHERE S1.SCHREGNO = T1.SCHREGNO ");
        stb1.append("                    AND S1.TRANSFERCD IN ('1','2') ");
        stb1.append("                    AND CASE WHEN T2.EDATE < '" + _param._sDate + "' THEN T2.EDATE ELSE '" + _param._sDate + "' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE) ");
        stb1.append(" ), SCHREGCNT_DIST AS ( ");
        stb1.append(" select distinct ");
        stb1.append("  T1.GROUP_CD, ");
        stb1.append("  T1.GROUP_NAME, ");
        stb1.append("  T1.GRADE, ");
        stb1.append("  T4.GRADE_NAME1, ");
        stb1.append("  T1.HR_CLASS, ");
        stb1.append("  T2.HR_NAME, ");
        stb1.append("  T2.TR_CD1, ");
        stb1.append("  T2.TR_CD2, ");
        stb1.append("  T1.SCHREGNO ");
        stb1.append(" FROM ");
        stb1.append("  SCHNO_A T1 ");
        stb1.append("  LEFT JOIN SCHREG_REGD_HDAT T2 ");
        stb1.append("    ON T2.YEAR = T1.YEAR ");
        stb1.append("   AND T2.SEMESTER = T1.SEMESTER ");
        stb1.append("   AND T2.GRADE = T1.GRADE ");
        stb1.append("   AND T2.HR_CLASS = T1.HR_CLASS ");
        stb1.append("  LEFT JOIN SCHREG_REGD_GDAT T4 ");
        stb1.append("    ON T4.YEAR = T1.YEAR ");
        stb1.append("   AND T4.GRADE = T1.GRADE ");
        stb1.append(" ), TESTRESULT_DIST_BASE AS ( ");
        stb1.append(" SELECT ");
        stb1.append("  T1.GROUP_CD, ");
        stb1.append("  T1.GROUP_NAME, ");
        stb1.append("  T1.GRADE, ");
        stb1.append("  T4.GRADE_NAME1, ");
        stb1.append("  T1.HR_CLASS, ");
        stb1.append("  T2.HR_NAME, ");
        stb1.append("  T3.SCHREGNO, ");
        stb1.append("  T3.SCORE, ");
        stb1.append("  T3.VALUE_DI ");
        stb1.append(" FROM ");
        stb1.append("  SCHNO_A T1 ");
        stb1.append("  LEFT JOIN RECORD_SCORE_DAT T3 ");
        stb1.append("    ON T3.YEAR = T1.YEAR ");
        stb1.append("   AND T3.SEMESTER = T1.SEMESTER ");
        stb1.append("   AND T3.TESTKINDCD || T3.TESTITEMCD || T3.SCORE_DIV = '" + _param._testcd + "' ");
        stb1.append("   AND T3.SCHREGNO = T1.SCHREGNO ");
        stb1.append("  LEFT JOIN SCHREG_REGD_HDAT T2 ");
        stb1.append("    ON T2.YEAR = T1.YEAR ");
        stb1.append("   AND T2.SEMESTER = T1.SEMESTER ");
        stb1.append("   AND T2.GRADE = T1.GRADE ");
        stb1.append("   AND T2.HR_CLASS = T1.HR_CLASS ");
        stb1.append("  LEFT JOIN SCHREG_REGD_GDAT T4 ");
        stb1.append("    ON T4.YEAR = T1.YEAR ");
        stb1.append("   AND T4.GRADE = T1.GRADE ");
        stb1.append(" WHERE ");
        stb1.append("   T3.SCHREGNO IS NOT NULL ");
        stb1.append(" ), TESTRESULT_SCHREGSUMMARY AS ( ");
        stb1.append(" SELECT ");
        stb1.append("   T1.GROUP_CD, ");
        stb1.append("   T1.GROUP_NAME, ");
        stb1.append("   T1.GRADE, ");
        stb1.append("   T1.GRADE_NAME1, ");
        stb1.append("   T1.HR_CLASS, ");
        stb1.append("   T1.HR_NAME, ");
        stb1.append("   T1.SCHREGNO, ");
        stb1.append("   SUM(CASE WHEN T1.SCORE IS NULL AND T1.VALUE_DI IS NULL THEN NULL ");
        stb1.append("            WHEN T1.VALUE_DI = '*' OR T1.VALUE_DI = '**' THEN 1 ELSE 0 END) AS CNT_VALUE_DI ");
        stb1.append(" FROM ");
        stb1.append("   TESTRESULT_DIST_BASE T1 ");
        stb1.append(" GROUP BY ");
        stb1.append("   T1.GROUP_CD, ");
        stb1.append("   T1.GROUP_NAME, ");
        stb1.append("   T1.GRADE, ");
        stb1.append("   T1.GRADE_NAME1, ");
        stb1.append("   T1.HR_CLASS, ");
        stb1.append("   T1.HR_NAME, ");
        stb1.append("   T1.SCHREGNO ");
        stb1.append(" ) ");
        stb1.append(" SELECT ");
        stb1.append("  T1.GROUP_CD, ");
        stb1.append("  T1.GROUP_NAME, ");
        stb1.append("  T1.GRADE, ");
        stb1.append("  T1.GRADE_NAME1, ");
        stb1.append("  T1.HR_CLASS, ");
        stb1.append("  T1.HR_NAME, ");
        stb1.append("  T1.TR_CD1, ");
        stb1.append("  T1.TR_CD2, ");
        stb1.append("  T51.STAFFNAME_SHOW AS STAFF1, ");
        stb1.append("  T52.STAFFNAME_SHOW AS STAFF2, ");
        stb1.append("  SUM(CASE WHEN T1.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END) AS ZAISEKISYA, ");
        stb1.append("  SUM(CASE WHEN T2.CNT_VALUE_DI = 0 THEN 1 ELSE 0 END) AS JYUKENSYA ");
        stb1.append(" FROM ");
        stb1.append("  SCHREGCNT_DIST T1 ");
        stb1.append("  LEFT JOIN TESTRESULT_SCHREGSUMMARY T2 ");
        stb1.append("    ON T2.GRADE = T1.GRADE ");
        stb1.append("   AND T2.HR_CLASS = T1.HR_CLASS ");
        stb1.append("   AND T2.SCHREGNO = T1.SCHREGNO ");
        stb1.append("   AND T2.GROUP_CD = T1.GROUP_CD ");
        stb1.append("   AND T2.GROUP_NAME = T1.GROUP_NAME ");
        stb1.append("  LEFT JOIN STAFF_MST T51 ");
        stb1.append("    ON T51.STAFFCD = T1.TR_CD1 ");
        stb1.append("  LEFT JOIN STAFF_MST T52 ");
        stb1.append("    ON T52.STAFFCD = T1.TR_CD2 ");
        stb1.append(" GROUP BY ");
        stb1.append("  T1.GROUP_CD, ");
        stb1.append("  T1.GROUP_NAME, ");
        stb1.append("  T1.GRADE, ");
        stb1.append("  T1.GRADE_NAME1, ");
        stb1.append("  T1.HR_CLASS, ");
        stb1.append("  T1.HR_NAME, ");
        stb1.append("  T1.TR_CD1, ");
        stb1.append("  T1.TR_CD2, ");
        stb1.append("  T51.STAFFNAME_SHOW, ");
        stb1.append("  T52.STAFFNAME_SHOW ");
        stb1.append(" ORDER BY ");
        stb1.append("  T1.GROUP_CD, ");
        stb1.append("  T1.GRADE, ");
        stb1.append("  T1.HR_CLASS ");

        final StringBuffer stb2 = new StringBuffer();
        stb2.append(" SELECT ");
        stb2.append("   T1.CLASSCD, ");
        stb2.append("   T1.SCHOOL_KIND, ");
        stb2.append("   T1.CURRICULUM_CD, ");
        stb2.append("   T1.SUBCLASSCD, ");
        stb2.append("   T3.SUBCLASSNAME, ");
        stb2.append("   T3.SUBCLASSABBV, ");
        stb2.append("   CASE WHEN D017.NAME1 IS NOT NULL THEN 1 ELSE 0 END AS DISPLAST_FLG, ");
        stb2.append("   T1.COURSECD, ");
        stb2.append("   T1.MAJORCD, ");
        stb2.append("   T1.COURSECODE, ");
        stb2.append("   T2.COURSECODENAME, ");
        stb2.append("   T2.COURSECODEABBV1, ");
        stb2.append("   T1.SCORE, ");
        stb2.append("   T1.AVG, ");
        stb2.append("   T1.COUNT, ");
        stb2.append("   T1.STDDEV ");
        stb2.append(" FROM ");
        stb2.append("   RECORD_AVERAGE_SDIV_DAT T1 ");
        stb2.append("   LEFT JOIN COURSECODE_MST T2 ");
        stb2.append("     ON T2.COURSECODE = T1.COURSECODE ");
        stb2.append("   LEFT JOIN SUBCLASS_MST T3 ");
        stb2.append("     ON T3.CLASSCD = T1.CLASSCD ");
        stb2.append("    AND T3.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb2.append("    AND T3.CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb2.append("    AND T3.SUBCLASSCD = T1.SUBCLASSCD ");
        stb2.append("   LEFT JOIN NAME_MST D017 ");
        stb2.append("     ON D017.NAMECD1 = 'D017' ");
        stb2.append("    AND D017.NAME1 = T1.SUBCLASSCD ");
        stb2.append(" WHERE ");
        stb2.append("   T1.YEAR = '" + _param._year + "' ");
        stb2.append("   AND T1.SEMESTER = '" + _param._semester + "' ");
        stb2.append("   AND T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '" + _param._testcd + "' ");
        stb2.append("   AND T1.AVG_DIV = 'B' ");
        stb2.append("   AND T1.GRADE = '" + _param._grade + "' ");
        stb2.append("   AND T1.HR_CLASS = ? ");
        stb2.append("   AND (T1.CLASSCD < '90' OR T1.CLASSCD = '99') ");
        stb2.append("   AND T1.SUBCLASSCD NOT IN ('333333', '555555', '99999A', '99999B') ");
        stb2.append(" ORDER BY ");
        stb2.append("   T1.COURSECD, ");
        stb2.append("   T1.MAJORCD, ");
        stb2.append("   T1.COURSECODE, ");
        stb2.append("   D017.NAME2 IS NULL DESC, ");
        stb2.append("   D017.NAME2 ASC, ");
        stb2.append("   T1.CLASSCD, ");
        stb2.append("   T1.SCHOOL_KIND, ");
        stb2.append("   T1.CURRICULUM_CD, ");
        stb2.append("   T1.SUBCLASSCD, ");
        stb2.append("   T3.SUBCLASSNAME ");

        PreparedStatement ps1 = null;
        ResultSet rs1 = null;
        PreparedStatement ps2 = null;
        ResultSet rs2 = null;
        CourseGroupInfo cgInfo = null;
        Map subclsMap = null;
        try {
            ps1 = db2.prepareStatement(stb1.toString());
            rs1 = ps1.executeQuery();
            ps2 = db2.prepareStatement(stb2.toString());
            while (rs1.next()) {
            	final String group_Cd = rs1.getString("GROUP_CD");
            	final String group_Name = rs1.getString("GROUP_NAME");
            	final String grade = rs1.getString("GRADE");
            	final String grade_Name1 = rs1.getString("GRADE_NAME1");
            	final String hr_Class = rs1.getString("HR_CLASS");
            	final String hr_Name = rs1.getString("HR_NAME");
            	final String staff1 = rs1.getString("STAFF1");
            	final String staff2 = rs1.getString("STAFF2");
            	final String zaisekisya = rs1.getString("ZAISEKISYA");
            	final String jyukensya = rs1.getString("JYUKENSYA");
            	final HrClsGrpInfo addwk = new HrClsGrpInfo(group_Cd, group_Name, grade, grade_Name1, hr_Class, hr_Name, staff1, staff2, zaisekisya, jyukensya);
            	if (!retMap.containsKey(group_Cd)) {
            		cgInfo = new CourseGroupInfo(group_Name);
            		retMap.put(group_Cd, cgInfo);
            	}
            	cgInfo = (CourseGroupInfo)retMap.get(group_Cd);
            	cgInfo._hrClassMap.put(hr_Class, addwk);
            	if (clsDetailMap.containsKey(hr_Class)) {
            	    addwk._avgDataList = (List)clsDetailMap.get(hr_Class);
            	}
            	if (clsKetuMap != null && clsKetuMap.containsKey(hr_Class)) {
            	    addwk._testKetuList = (List)clsKetuMap.get(hr_Class);
            	}
            	if (clsAttendMap != null && clsAttendMap.containsKey(hr_Class)) {
            		addwk._att = (Attendance)clsAttendMap.get(hr_Class);
            	}
            	if (clsAttendMap != null && clscourseMap.containsKey(hr_Class)) {
            		addwk._courseNameList = (List)clscourseMap.get(hr_Class);
            	}
            	ps2.setString(1, hr_Class);
            	rs2 = ps2.executeQuery();
                while (rs2.next()) {
                	final String classcd = rs2.getString("CLASSCD");
                	final String school_Kind = rs2.getString("SCHOOL_KIND");
                	final String curriculum_Cd = rs2.getString("CURRICULUM_CD");
                	final String subclasscd = rs2.getString("SUBCLASSCD");
                	final String subclassname = rs2.getString("SUBCLASSNAME");
                	final String subclassabbv = rs2.getString("SUBCLASSABBV");
                	final String coursecd = rs2.getString("COURSECD");
                	final String majorcd = rs2.getString("MAJORCD");
                	final String coursecode = rs2.getString("COURSECODE");
                	final String coursecodename = rs2.getString("COURSECODENAME");
                	final String coursecodeabbv1 = rs2.getString("COURSECODEABBV1");
                	final String score = rs2.getString("SCORE");
                	final String avg = rs2.getString("AVG");
                	final String count = rs2.getString("COUNT");
                	final String stddev = rs2.getString("STDDEV");
                	final String dispLastFlg = rs2.getString("DISPLAST_FLG");
                	final SubClsInfo addWk2 = new SubClsInfo(classcd, school_Kind, curriculum_Cd, subclasscd, subclassname, subclassabbv, coursecd, majorcd, coursecode, coursecodename, coursecodeabbv1, score, avg, count, stddev, dispLastFlg);
                	if (!addwk._courseInfoMap.containsKey(coursecode)) {
                		subclsMap = new LinkedMap();
                		addwk._courseInfoMap.put(coursecode, subclsMap);
                	}
                	subclsMap = (Map)addwk._courseInfoMap.get(coursecode);
                	final String scKey = classcd + "-" + school_Kind + "-" + curriculum_Cd + "-" + subclasscd;
                	subclsMap.put(scKey, addWk2);
                	//科目集約にも登録
                	if (!cgInfo._subclsMergeMap.containsKey(scKey)) {
                		if (dispLastFlg != null && "1".equals(dispLastFlg)) {
                			cgInfo._subclsDispLastMap.put(scKey, addWk2);
                		}
                		cgInfo._subclsMergeMap.put(scKey, addWk2);  //科目名称を利用するために登録。ここに登録した物の数値は利用できないので、注意。利用するのは上の方。
                	}
                }

            }
        } catch (Exception ex) {
            log.fatal("exception!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps1, rs1);
            if (ps2 != null) {
                DbUtils.closeQuietly(null, ps2, rs2);
            }
            db2.commit();
        }

    	return retMap;
    }
    private Map getCourseMap(final DB2UDB db2) {
    	final Map retMap = new LinkedMap();
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH SCHNO_A AS( ");
        stb.append(" SELECT DISTINCT ");
        stb.append("   T1.YEAR, T1.SEMESTER,T1.SCHREGNO, T1.GRADE, T1.HR_CLASS, T1.ATTENDNO, T1.COURSECD, T1.MAJORCD, T1.COURSECODE, CGRP.GROUP_CD, CGRPH.GROUP_NAME ");
        stb.append(" FROM ");
        stb.append("   SCHREG_REGD_DAT T1 ");
        stb.append("   INNER JOIN SCHREG_REGD_HDAT TH ");
        stb.append("     ON TH.YEAR = T1.YEAR ");
        stb.append("    AND TH.SEMESTER = T1.SEMESTER ");
        stb.append("    AND TH.GRADE = T1.GRADE ");
        stb.append("    AND TH.HR_CLASS = T1.HR_CLASS ");
        stb.append("   LEFT JOIN SEMESTER_MST T2  ");
        stb.append("     ON T1.YEAR = T2.YEAR ");
        stb.append("    AND T1.SEMESTER = T2.SEMESTER ");
        stb.append("   INNER JOIN COURSE_GROUP_CD_DAT CGRP ON CGRP.YEAR = T1.YEAR ");
        stb.append("    AND CGRP.GRADE = T1.GRADE ");
        stb.append("    AND CGRP.COURSECD = T1.COURSECD ");
        stb.append("    AND CGRP.MAJORCD = T1.MAJORCD ");
        stb.append("    AND CGRP.COURSECODE = T1.COURSECODE ");
        stb.append("   INNER JOIN COURSE_GROUP_CD_HDAT CGRPH ON CGRPH.YEAR = CGRP.YEAR ");
        stb.append("    AND CGRPH.GRADE = CGRP.GRADE ");
        stb.append("    AND CGRPH.GROUP_CD = CGRP.GROUP_CD ");
        stb.append(" WHERE ");
        stb.append("   T1.YEAR = '" + _param._year + "' ");
        stb.append("   AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append("   AND T1.GRADE = '" + _param._grade + "' ");
        stb.append("   AND '" + _param._eDate + "' BETWEEN T2.SDATE AND T2.EDATE ");
        stb.append("   AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST S1 ");
        stb.append("                  WHERE S1.SCHREGNO = T1.SCHREGNO ");
        stb.append("                    AND ((S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < CASE WHEN T2.EDATE < '" + _param._eDate + "' THEN T2.EDATE ELSE '"+_param._sDate+"' END) ");
        stb.append("                    OR (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE > CASE WHEN T2.EDATE < '" + _param._sDate + "' THEN T2.EDATE ELSE '" + _param._sDate + "' END)) ) ");
        stb.append("   AND NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT S1 ");
        stb.append("                  WHERE S1.SCHREGNO = T1.SCHREGNO ");
        stb.append("                    AND S1.TRANSFERCD IN ('1','2') ");
        stb.append("                    AND CASE WHEN T2.EDATE < '" + _param._sDate + "' THEN T2.EDATE ELSE '" + _param._sDate + "' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE) ");
        stb.append(" ) ");
    	stb.append(" SELECT DISTINCT");
    	stb.append("   T1.GRADE, ");
    	stb.append("   T1.HR_CLASS, ");
        stb.append("   T1.COURSECODE, ");
        stb.append("   T2.COURSECODENAME, ");
        stb.append("   T2.COURSECODEABBV1 ");
    	stb.append(" FROM ");
    	stb.append("   SCHNO_A T1 ");
        stb.append("   LEFT JOIN COURSECODE_MST T2 ");
        stb.append("     ON T2.COURSECODE = T1.COURSECODE ");
        stb.append(" ORDER BY ");
    	stb.append("   T1.GRADE, ");
    	stb.append("   T1.HR_CLASS, ");
        stb.append("   T1.COURSECODE ");
        PreparedStatement ps = null;
        ResultSet rs = null;
        List subList = null;
        try {
            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();
            while (rs.next()) {
            	final String hr_Class = rs.getString("HR_CLASS");
            	final String coursecode = rs.getString("COURSECODE");
            	final String courseName = rs.getString("COURSECODENAME");
            	final String courseAbbv = rs.getString("COURSECODEABBV1");
            	CourseNameInfo addwk = new CourseNameInfo(hr_Class, coursecode, courseName, courseAbbv);
            	if (!retMap.containsKey(hr_Class)) {
            		subList = new ArrayList();
            		retMap.put(hr_Class, subList);
            	}
          		subList.add(addwk);
            }
        } catch (Exception ex) {
            log.fatal("exception!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }

    	return retMap;
    }
    private Map getClsKetuInfo(final DB2UDB db2) {
    	final Map retMap = new LinkedMap();
        final StringBuffer stb = new StringBuffer();
    	stb.append(" SELECT ");
    	stb.append("   T2.ATTENDNO, ");
    	stb.append("   T2.HR_CLASS, ");
    	stb.append("   T2.COURSECODE, ");
    	stb.append("   T1.SCHREGNO, ");
    	stb.append("   T3.NAME, ");
    	stb.append("   SUM(CASE WHEN T1.VALUE_DI = '*' OR T1.VALUE_DI = '**' THEN 1 ELSE 0 END) AS KETU_CNT ");
    	stb.append(" FROM ");
    	stb.append("   RECORD_SCORE_DAT T1 ");
    	stb.append("   INNER JOIN SCHREG_REGD_DAT T2 ");
    	stb.append("     ON T2.YEAR = T1.YEAR ");
    	stb.append("    AND T2.SEMESTER = T1.SEMESTER ");
    	stb.append("    AND T2.SCHREGNO = T1.SCHREGNO ");
    	stb.append("   INNER JOIN SCHREG_BASE_MST T3 ");
    	stb.append("     ON T3.SCHREGNO = T1.SCHREGNO ");
    	stb.append(" WHERE ");
    	stb.append("   T1.YEAR = '" + _param._year + "' ");
    	stb.append("   AND T1.SEMESTER = '" + _param._semester + "' ");
    	stb.append("   AND T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '010101' ");
    	stb.append("   AND T1.SCORE IS NULL AND T1.VALUE_DI IS NOT NULL ");
    	stb.append("   AND T2.GRADE = '" + _param._grade + "' ");
    	stb.append(" GROUP BY ");
    	stb.append("   T2.HR_CLASS, ");
    	stb.append("   T1.SCHREGNO, ");
    	stb.append("   T2.ATTENDNO, ");
    	stb.append("   T3.NAME, ");
    	stb.append("   T2.COURSECODE ");
        PreparedStatement ps = null;
        ResultSet rs = null;
        List subList = null;
        try {
            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();
            while (rs.next()) {
            	final String attendno = rs.getString("ATTENDNO");
            	final String hr_Class = rs.getString("HR_CLASS");
            	final String coursecode = rs.getString("COURSECODE");
            	final String schregno = rs.getString("SCHREGNO");
            	final String name = rs.getString("NAME");
            	final String ketu_Cnt = rs.getString("KETU_CNT");
            	KetuInfo addwk = new KetuInfo(attendno, hr_Class, coursecode, schregno, name, ketu_Cnt);
            	if (!retMap.containsKey(hr_Class)) {
            		subList = new ArrayList();
            		retMap.put(hr_Class, subList);
            	}
            	subList.add(addwk);
            }
        } catch (Exception ex) {
            log.fatal("exception!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }

    	return retMap;
    }
    private Map getClsDetailInfo(final DB2UDB db2) {
    	final Map retMap = new LinkedMap();
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     RANK() OVER(PARTITION BY T2.COURSECODE,T2.HR_CLASS ORDER BY T1.AVG DESC, T2.ATTENDNO ASC) AS ID, ");
        stb.append("     RANK() OVER(PARTITION BY T2.COURSECODE,T2.HR_CLASS ORDER BY T1.AVG ASC, T2.ATTENDNO DESC) AS REV_ID, ");
        stb.append("     T2.ATTENDNO, ");
        stb.append("     T2.HR_CLASS, ");
        stb.append("     T2.COURSECODE, ");
        stb.append("     T2.SCHREGNO, ");
        stb.append("     T1.AVG, ");
        stb.append("     CASE WHEN T1.AVG < 30 THEN 1 ELSE 0 END AS UNDER_30 ");
        stb.append(" FROM ");
        stb.append("     RECORD_RANK_SDIV_DAT T1 ");
        stb.append("     INNER JOIN SCHREG_REGD_DAT T2 ");
        stb.append("       ON T2.YEAR = T1.YEAR ");
        stb.append("      AND T2.SEMESTER = T1.SEMESTER ");
        stb.append("      AND T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("      AND T2.GRADE = '" + _param._grade + "' ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '990008' ");
        stb.append("     AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '99-H-99-999999' ");
        stb.append(" ORDER BY ");
        stb.append("   T2.COURSECODE, ");
        stb.append("   T2.HR_CLASS, ");
        stb.append("   T1.AVG DESC ");

        PreparedStatement ps = null;
        ResultSet rs = null;
        List subList = null;
        try {
            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();
            while (rs.next()) {
            	final String id = rs.getString("ID");
            	final String rev_Id = rs.getString("REV_ID");
            	final String attendno = rs.getString("ATTENDNO");
            	final String hr_Class = rs.getString("HR_CLASS");
            	final String coursecode = rs.getString("COURSECODE");
            	final String schregno = rs.getString("SCHREGNO");
            	final String avg = rs.getString("AVG");
            	final String under_30 = rs.getString("UNDER_30");
            	ClsDetailInfo addwk = new ClsDetailInfo(id, rev_Id, attendno, hr_Class, coursecode, schregno, avg, under_30);
            	if (!retMap.containsKey(hr_Class)) {
            		subList = new ArrayList();
            		retMap.put(hr_Class, subList);
            	}
            	subList.add(addwk);
            }
        } catch (Exception ex) {
            log.fatal("exception!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }

        return retMap;
    }

    private class CourseNameInfo{
    	final String _hr_Class;
    	final String _coursecode;
    	final String _courseName;
    	final String _courseAbbv;
    	CourseNameInfo(final String hr_Class, final String coursecode, final String courseName, final String courseAbbv) {
    		_hr_Class = hr_Class;
    		_coursecode = coursecode;
    		_courseName = courseName;
    		_courseAbbv = courseAbbv;
    	}
    }
    private class KetuInfo {
        final String _attendno;
        final String _hr_Class;
        final String _coursecode;
        final String _schregno;
        final String _name;
        final String _ketu_Cnt;
        public KetuInfo (final String attendno, final String hr_Class, final String coursecode, final String schregno, final String name, final String ketu_Cnt)
        {
            _attendno = attendno;
            _hr_Class = hr_Class;
            _coursecode = coursecode;
            _schregno = schregno;
            _name = name;
            _ketu_Cnt = ketu_Cnt;
        }
        private String cutNameStr(final int cutLen) {
        	if ("".equals(StringUtils.defaultString(_name, ""))) return "";
        	final int findkgr1 = _name.indexOf(" ");
        	final int findkgr2 = _name.indexOf("　");
            final int maxCutPtr = (findkgr1 > 0 && findkgr2 > 0) ? Math.min(findkgr1, findkgr2) : Math.max(findkgr1, findkgr2);

        	return maxCutPtr == -1 ? _name.substring(0, cutLen) : _name.substring(0, maxCutPtr-1);
        }
    }
    private class ClsDetailInfo {
        final String _id;
        final String _rev_Id;
        final String _attendno;
        final String _hr_Class;
        final String _coursecode;
        final String _schregno;
        final String _avg;
        final String _under_30;
        public ClsDetailInfo (final String id, final String rev_Id, final String attendno, final String hr_Class, final String coursecode, final String schregno, final String avg, final String under_30)
        {
            _id = id;
            _rev_Id = rev_Id;
            _attendno = attendno;
            _hr_Class = hr_Class;
            _coursecode = coursecode;
            _schregno = schregno;
            _avg = avg;
            _under_30 = under_30;
        }
    }
    private class CourseGroupInfo {
        final String _courseGrpName;
    	Map _hrClassMap;
    	Map  _subclsMergeMap;
    	Map _subclsDispLastMap;
    	public CourseGroupInfo(final String courseGrpName) {
    		_courseGrpName = courseGrpName;
    		_hrClassMap = new LinkedMap();
    		_subclsMergeMap = new LinkedMap();
    		_subclsDispLastMap = new LinkedMap();
    	}
    	private int subclsLastDispIdx(final SubClsInfo prtwk) {
    		int retIdx = 0;
    		boolean findFlg = false;
    		for (Iterator ite = _subclsDispLastMap.keySet().iterator();ite.hasNext();) {
    			final String subclsCd = (String)ite.next();
    			retIdx++;
    			if (prtwk.getSubclsCd().equals(subclsCd)) {
    				findFlg = true;
    				break;
    			}
    		}
    		return findFlg ? MAXDISP_SUBCLASS - _subclsDispLastMap.size() + retIdx : 0;
    	}
    }

    private class HrClsGrpInfo {
        final String _group_Cd;
        final String _group_Name;
        final String _grade;
        final String _grade_Name1;
        final String _hr_Class;
        final String _hr_Name;
        final String _staff1;
        final String _staff2;
        final String _zaisekisya;
        final String _jyukensya;
        final Map _courseInfoMap;
        List _courseNameList;
        List _avgDataList;
        List _testKetuList;
        Attendance _att;
        public HrClsGrpInfo (final String group_Cd, final String group_Name, final String grade, final String grade_Name1, final String hr_Class, final String hr_Name, final String staff1, final String staff2, final String zaisekisya, final String jyukensya)
        {
            _group_Cd = group_Cd;
            _group_Name = group_Name;
            _grade = grade;
            _grade_Name1 = grade_Name1;
            _hr_Class = hr_Class;
            _hr_Name = hr_Name;
            _staff1 = staff1;
            _staff2 = staff2;
            _zaisekisya = zaisekisya;
            _jyukensya = jyukensya;
            _courseInfoMap = new LinkedMap();
            _courseNameList = null;
            _avgDataList = null;
            _testKetuList = null;
            _att = null;
        }
    }

    private class SubClsInfo {
        final String _classcd;
        final String _school_Kind;
        final String _curriculum_Cd;
        final String _subclasscd;
        final String _subclassname;
        final String _subclassabbv;
        final String _coursecd;
        final String _majorcd;
        final String _coursecode;
        final String _coursecodename;
        final String _coursecodeabbv1;
        final String _score;
        final String _avg;
        final String _count;
        final String _stddev;
        final String _dispLastFlg;
        public SubClsInfo (final String classcd, final String school_Kind, final String curriculum_Cd, final String subclasscd, final String subclassname, final String subclassabbv, final String coursecd, final String majorcd,
        		            final String coursecode, final String coursecodename, final String coursecodeabbv1, final String score, final String avg, final String count, final String stddev, final String dispLastFlg)
        {
            _classcd = classcd;
            _school_Kind = school_Kind;
            _curriculum_Cd = curriculum_Cd;
            _subclasscd = subclasscd;
            _subclassname = subclassname;
            _subclassabbv = subclassabbv;
            _coursecd = coursecd;
            _majorcd = majorcd;
            _coursecode = coursecode;
            _coursecodename = coursecodename;
            _coursecodeabbv1 = coursecodeabbv1;
            _score = score;
            _avg = avg;
            _count = count;
            _stddev = stddev;
            _dispLastFlg = dispLastFlg;
        }
        private String getSubclsCd() {
        	return _classcd + "-" + _school_Kind + "-" + _curriculum_Cd + "-" + _subclasscd;
        }
    }

    private class Attendance {
        final String _semester;
        final int _lesson;
        final int _mLesson;
        final int _suspend;
        final int _mourning;
        final int _sick;
        final int _sickOnly;
        final int _noticeOnly;
        final int _nonoticeOnly;
        final int _present;
        final int _late;
        final int _early;
        final int _transDays;
        Attendance(
                final String semester,
                final int lesson,
                final int mLesson,
                final int suspend,
                final int mourning,
                final int sick,
                final int sickOnly,
                final int noticeOnly,
                final int nonoticeOnly,
                final int present,
                final int late,
                final int early,
                final int transDays
        ) {
            _semester = semester;
            _lesson = lesson;
            _mLesson = mLesson;
            _suspend = suspend;
            _mourning = mourning;
            _sick = sick;
            _sickOnly = sickOnly;
            _noticeOnly = noticeOnly;
            _nonoticeOnly = nonoticeOnly;
            _present = present;
            _late = late;
            _early = early;
            _transDays = transDays;
        }
        private BigDecimal calcAttRate() {
        	BigDecimal calcWk = new BigDecimal((double)((_mLesson - (_sickOnly + _noticeOnly + _nonoticeOnly)) / _mLesson)).setScale(1, BigDecimal.ROUND_HALF_UP);
        	return calcWk;
        }
    }

    private Map getAttendSemes(final DB2UDB db2) {
        if (null == _param._sDate || null == _param._eDate || _param._sDate.compareTo(_param._eDate) > 0) {
            return null;
        }
        Map retMap = new LinkedMap();
        PreparedStatement psAtSeme = null;
        ResultSet rsAtSeme = null;
        _param._attendParamMap.put("GRADE", _param._grade);
        _param._attendParamMap.put("groupByDiv", "HR_CLASS");

        final String sql = AttendAccumulate.getAttendSemesSql(
                _param._year,
                _param._semester,
                _param._sDate,
                _param._eDate,
                _param._attendParamMap
        );
        log.info("get getAttendSemes sql = " + sql);

        try {
            psAtSeme = db2.prepareStatement(sql);
            rsAtSeme = psAtSeme.executeQuery();
            while (rsAtSeme.next()) {
                final Attendance attendance = new Attendance(
                		SEMEALL, //期間の集計値なので、固定値で指定。
                		rsAtSeme.getInt("LESSON"),
                		rsAtSeme.getInt("MLESSON"),
                		rsAtSeme.getInt("SUSPEND"),
                		rsAtSeme.getInt("MOURNING"),
                		rsAtSeme.getInt("SICK"),
                		rsAtSeme.getInt("SICK_ONLY"),
                		rsAtSeme.getInt("NOTICE_ONLY"),
                		rsAtSeme.getInt("NONOTICE_ONLY"),
                		rsAtSeme.getInt("PRESENT"),
                		rsAtSeme.getInt("LATE"),
                		rsAtSeme.getInt("EARLY"),
                		rsAtSeme.getInt("TRANSFER_DATE")
                );
                retMap.put(rsAtSeme.getString("HR_CLASS"), attendance);
	    	}
        } catch (Exception ex) {
            log.fatal("exception!", ex);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(rsAtSeme);
        }
        return retMap;
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 74645 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _year;
        final String _semester;
//        final String _ctrlSemester;
        final String _grade;
        final String _loginDate;
        final String _testcd;
        final String _schoolKind;
        final String _semestername;
        final String _gradeName;
        final String _sDate;
        final String _eDate;
        private final Map _attendParamMap;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
//            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _grade = request.getParameter("GRADE");
            _loginDate = null == request.getParameter("LOGIN_DATE") ? request.getParameter("LOGIN_DATE") : StringUtils.replace(request.getParameter("LOGIN_DATE"), "/", "-");
            _testcd = request.getParameter("TESTKIND_ITEMCD");
            _schoolKind = getSchoolKind(db2);
            _semestername = getSemesterinfo(db2, "SEMESTERNAME");
            _sDate = request.getParameter("SDATE").replace('/', '-');
            _eDate = request.getParameter("EDATE").replace('/', '-');
            _gradeName = getGradename(db2);

            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
            _attendParamMap.put("grade", _grade);
            _attendParamMap.put("useTestCountflg", "TESTITEM_MST_COUNTFLG_NEW_SDIV");
            _attendParamMap.put("useCurriculumcd", "1");
        }

        private String getGradename(final DB2UDB db2) {
            String sql = "";
            sql += " SELECT GRADE_NAME1 FROM SCHREG_REGD_GDAT ";
            sql += " WHERE ";
            sql += "   YEAR = '" + _year + "' ";
            sql += "   AND GRADE = '" + _grade + "' ";
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString("GRADE_NAME1");
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            }
            return rtn;
        }

//        private String getTestitemname(final DB2UDB db2) {
//            String sql = "";
//            sql += " SELECT TESTITEMNAME FROM TESTITEM_MST_COUNTFLG_NEW_SDIV ";
//            sql += " WHERE ";
//            sql += "   YEAR = '" + _year + "' ";
//            sql += "   AND SEMESTER = '" + _semester + "' ";
//            sql += "   AND TESTKINDCD || TESTITEMCD || SCORE_DIV = '" + _testcd + "' ";
//            String rtn = null;
//            PreparedStatement ps = null;
//            ResultSet rs = null;
//            try {
//                ps = db2.prepareStatement(sql);
//                rs = ps.executeQuery();
//                while (rs.next()) {
//                    rtn = rs.getString("TESTITEMNAME");
//                }
//            } catch (SQLException e) {
//                log.error("exception!", e);
//            }
//            return rtn;
//        }

        private String getSemesterinfo(final DB2UDB db2, final String elmName) {
            String sql = "";
            sql += " SELECT " + elmName + " FROM SEMESTER_MST ";
            sql += " WHERE ";
            sql += "   YEAR = '" + _year + "' ";
            sql += "   AND SEMESTER = '" + _semester + "' ";
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString(elmName);
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            }
            return rtn;
        }

        private String getSchoolKind(final DB2UDB db2) {
            String sql = "";
            sql += " SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT ";
            sql += " WHERE ";
            sql += "   YEAR = '" + _year + "' ";
            sql += "   AND GRADE = '" + _grade + "' ";
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString("SCHOOL_KIND");
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            }
            return rtn;
        }
    }
}

// eof

