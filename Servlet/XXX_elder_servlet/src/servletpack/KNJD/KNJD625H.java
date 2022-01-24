// kanji=漢字
/*
 * $Id: 781d38d90d0973c00bedc88fdc529588dc919d45 $
 *
 * 作成日: 2007/05/14
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.math.BigDecimal;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJServletpacksvfANDdb2;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

/**
 * 得点分布表
 * 
 * @author nakamoto
 * @version $Id: 781d38d90d0973c00bedc88fdc529588dc919d45 $
 */
public class KNJD625H {
    private static final Log log = LogFactory.getLog(KNJD625H.class);

    private static final String SEME_ALL = "9";

    private static final String AVG_DIV_GRADE = "1";
    private static final String AVG_DIV_HR_CLASS = "2";
    private static final String AVG_DIV_COURSE = "3";
    private static final String AVERAGE_DAT_GRADE_CODE = "00000000000";

    private static final String SUBCLASSCD_ALL3 = "333333";
    private static final String SUBCLASSCD_ALL5 = "555555";
    private static final String SUBCLASSCD_ALL9 = "999999";
    
    private static final String TESTCD9900 = "9900";
    private static final String[] TESTCD_ARRAY = {"1010108", "1990008", "2010108", "2990008", "3990008", "9990008"};

    //実力テスト　教科列番号
    private static final int COL_ENG = 1; //英語
    private static final int COL_MATH = 2; //数学
    private static final int COL_LANG = 3; //国語
    private static final int COL_SCI_SOC = 4; //理科・社会
    private static final int COL_ALL = 5; //総合

    private Param _param;

    /**
     * KNJD.classから最初に起動されます。
     */
    public void svf_out(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
        final KNJServletpacksvfANDdb2 sd = new KNJServletpacksvfANDdb2(); // 帳票におけるＳＶＦおよびＤＢ２の設定

        DB2UDB db2 = null;
        Vrw32alp svf = null;
        boolean hasData = false;
        try {
            // ＤＢ接続
            db2 = sd.setDb(request);

            if (sd.openDb(db2)) {
                log.error("db open error! ");
                return;
            }
            // パラメータの取得
            _param = createParam(request);

            svf = new Vrw32alp(); // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
            sd.setSvfInit(request, response, svf);
                hasData = svfPrintMain(svf, db2, _param._grade);
        } catch (final Exception ex) {
            log.error("error! ", ex);
        } finally {
        	
            if (null != db2) {
                sd.closeDb(db2);
            }
            if (null != svf) {
                sd.closeSvf(svf, hasData);
            }
        }
        log.debug(" hasData = " + hasData);
    }

    private boolean svfPrintMain(final Vrw32alp svf, final DB2UDB db2, final String grade) {
        boolean hasData = false;
        _param.load(db2, grade);
        
        Map<String, List<SubclassRecord>> subclassListMap = getSubclassListMap(db2, _param, grade);
        
        Map<String, String> hrclassMap = getHrclassMap(db2, _param);
        Map<String, List<Student>> schregMap = getSchregMap(db2, _param);
        
        svf.VrSetForm("KNJD625H.frm" , 1);
        
        //クラス単位
        for (String hrclass : hrclassMap.keySet()) {
        	final String ghrclass = grade + hrclass;
        	final String hrname = hrclassMap.get(hrclass);
        	//生徒単位
        	for (Student student : schregMap.get(ghrclass)) {
        		final String schregno = student._schregno;
        		final String ghist = student._gradeHist;
                svf.VrsOut("TITLE" , _param._ctrlYear + "年度 " + _param._gradeCd.substring(1, 2) + "学年 " +_param._semesterName + " 成績資料");
                svf.VrsOut("SCHOOL_NAME" , _param._schoolname);
                svf.VrsOut("DATE" , _param._ctrlDate);                
                amikake(svf, "PAINT_SUBJECT_SAMPLE", "");

        		svf.VrsOut("HR_NAME", hrname + (student._attendno).replaceFirst("^0+", "") + "番" );
        		svf.VrsOut("NAME", "氏名  " + student._name );
        		svf.VrsOut("SCHREGNO",  schregno ); 
        		
        		List<String> suclassGroupList = getSubclassGroupList(db2, _param, schregno);

        		//1.学習成績の推移
        		int gradeHistRow = 1;
        		for (final String ghistEle : ghist.split(",")) {
        			final String[] tmp = ghistEle.split("-");
        			final String histYear = tmp[0];
        			final String histGrade = tmp[1];
        			final String histGradeCd = tmp[2];
        			
        			
        			svf.VrsOut("GRADE"+gradeHistRow, histGradeCd.substring(1, 2) + "年次");
        			
        			final String subListMapKey = hrclass + "-" + schregno + "-" + histYear;
        			final List<SubclassRecord> subclassList = subclassListMap.get(subListMapKey);
        			if (subclassList == null) {
        				gradeHistRow++;
        				continue;
        			}
        			
        			int subclassCol = 1;
	        		for (final SubclassRecord subRecord : subclassList) {
	        			final String subclasscd		= subRecord._subclasscd;

	        			boolean all5Flg = (_param._all5SubCd.equals(subclasscd)) ? true : false;
	        			boolean all9Flg = (_param._all9SubCd.equals(subclasscd)) ? true : false;
	        			
						if (!all5Flg && !all9Flg) {
							if (gradeHistRow == 1) {
								svf.VrsOutn("CLASS_NAME", subclassCol, subRecord._classAbbv);
							}
							final String subnameSuf = (KNJ_EditEdit.getMS932ByteLength(subRecord._subAbbv) <= 4) ? "1" : "2";
							
							if (suclassGroupList.contains(subclasscd) && gradeHistRow == 1) {
								amikakeN(svf, "SUBCLASS_NAME" + gradeHistRow + "_" + subnameSuf, subclassCol, subRecord._subAbbv);
							} else {
								svf.VrsOutn("SUBCLASS_NAME" + gradeHistRow + "_" + subnameSuf, subclassCol,	subRecord._subAbbv);
							}
						}     			
	        			
	        			//テスト単位
						int testcdRow = 1;
	        			for(final String testcd : TESTCD_ARRAY) {
	        				
	        				if (!subRecord._currentFlg && !"9990008".equals(testcd)) continue; //過年度学年では学年末評価のみ表示
	        				
	        				final String score 		= (String)subRecord._scoreMap.get(testcd);
	        				final String avg 		 		= (String)subRecord._avgMap.get(testcd);
	        				final String gradeAvg 	 	= (String)subRecord._gradeAvgMap.get(testcd);
	        				
	        				final String divFieldNameSuf = gradeHistRow == 1 ? "_" + testcdRow : "" ;
	        				final String divFieldname = "DIV" + gradeHistRow + divFieldNameSuf;
	        				
	        				final String avgFieldNameSuf = gradeHistRow == 1 ? "_" + testcdRow : "" ;
	        				final String avgFieldName = "AVE" + gradeHistRow + avgFieldNameSuf;

	        				if (all5Flg) {
	        					svf.VrsOutn(avgFieldName, 1, getRoundValue(avg, 1));
	        				} else if (all9Flg) {
	        					svf.VrsOutn(avgFieldName, 2, getRoundValue(avg, 1));
	        					svf.VrsOutn(avgFieldName, 3, getRoundValue(gradeAvg, 1));
	        				} else {
	        					svf.VrsOutn(divFieldname, subclassCol, score);
	        				}
	        				testcdRow++;
	        			}
	        			if (!all5Flg && !all9Flg) {
	        				subclassCol++; //通常科目の分だけカウントアップする(合計科目は無視)
	        			}
	        		}
	        		gradeHistRow++;
	        		hasData = true;
        		}

        		//2.校外実力テスト(新)
        		Map<String, List<String>> mockRankRangeMap = getMockRankRangeMap(db2, _param, student);

        		List<String> mockCdList = new ArrayList(student._mockCdMap.keySet());
        		Collections.sort(mockCdList);
        		Collections.reverse(mockCdList);

        		List<String> classCdList = new ArrayList(student._classCdMap.keySet());
        		Collections.sort(classCdList);

        		//年度+模試CD単位ループ(降順)
    			int mockRow = 1;
        		for (String yearAndMockCd : mockCdList) {
        			final String mockname = (String)student._mockCdMap.get(yearAndMockCd);
        			final String mockNameSuf = (KNJ_EditEdit.getMS932ByteLength(mockname) <= 24) ? "1" : "2";
        			svf.VrsOutn("MOCK_NAME1_" + mockNameSuf, mockRow, mockname);

        			//各教科列の模試科目を表示するフィールド番号(4列目は複数科目なので科目ループの外側で定義)
   	        		int mockSubCol1 = 0;
	        		int mockSubCol2 = 0;
	        		int mockSubCol3 = 0;
	        		int mockSubCol4 = 0;
	        		int mockSubCol5 = 0;
        			
        			for (String classCd : classCdList) {
        				//模試科目単位ループ

    					int mockSubCol   = 0;
    					int mockClassCol = 0;
 
    	        		if (!mockRankRangeMap.containsKey(yearAndMockCd + "-" + classCd)) continue;
    	        		
        				List<String> mockSubNameAndDevList = mockRankRangeMap.get(yearAndMockCd + "-" + classCd); //教科ごとの模試科目リスト(値は科目名_成績)
        				for (String mockSubNameAndDev : mockSubNameAndDevList) {
        					final String[] tmp = StringUtils.split(mockSubNameAndDev, "_"); //0:模試科目名, 1:模試科目DEVIATION
        					if (tmp.length != 2) continue;
        					if ("".equals(tmp[1])) continue;

        	        		final int classCdorder = getClassCdOrder(classCd);
        	        		String className = "";
        	        		if (classCdorder == -1) continue; //指定の教科以外は無視
        					if (classCdorder == COL_ENG) { //1.英語
        	        			mockClassCol = 1;
        	        			mockSubCol1++;
        	        			mockSubCol = mockSubCol1;
        	        			className = "英語";
        	        		} else if (classCdorder == COL_MATH) { //2.数学
        	        			mockClassCol = 2;
        	        			mockSubCol2++;
        	        			className = "数学";
        	        			mockSubCol = mockSubCol2;
        	        		} else if (classCdorder == COL_LANG) { //3.国語
        	        			mockClassCol = 3;
        	        			mockSubCol3++;
        	        			mockSubCol = mockSubCol3;
        	        			className = "国語";
        	        		} else if (classCdorder == COL_SCI_SOC) { //4.理科・社会
        	        			mockClassCol = 4;
        	        			mockSubCol4++;
        	        			mockSubCol = mockSubCol4;
        	        			className = "理科・社会";
        	        		} else if (classCdorder == COL_ALL) { //5.総合
        	        			mockClassCol = 5;
        	        			mockSubCol5++;
        	        			mockSubCol = mockSubCol5;
        	        			className = "総合";
        	        		}

                			svf.VrsOut("MOCK_CLASS_NAME" + mockClassCol, className);
        					svf.VrsOutn("MOCK_SUBCLASS_NAME" + mockClassCol + "_" + mockSubCol, mockRow, tmp[0]);
        					svf.VrsOutn("MOCK_SUBCLASS_DEVI" + mockClassCol + "_" + mockSubCol, mockRow, tmp[1]);
        					
        				}
    	        		hasData = true;
        			}
        			mockRow++;        			
        		}
                svf.VrEndPage();	
        	}
        }
        svf.VrEndPage();
        
        return hasData;
    }
   
    private Integer getClassCdOrder (final String classcd) {
    	if ("J".equals(_param._schoolKind) && "19".equals(classcd) || "H".equals(_param._schoolKind) && "38".equals(classcd)) {
    		return COL_ENG;
    	} else if ("J".equals(_param._schoolKind) && "13".equals(classcd) || "H".equals(_param._schoolKind) && "34".equals(classcd)) {
    		return COL_MATH;
    	} else if ("J".equals(_param._schoolKind) && "11".equals(classcd) || "H".equals(_param._schoolKind) && "31".equals(classcd)) {
    		return COL_LANG;
    	} else if ("H".equals(_param._schoolKind) && "32".equals(classcd)
    			 || "H".equals(_param._schoolKind) && "33".equals(classcd)
    			 || "H".equals(_param._schoolKind) && "35".equals(classcd)) {
    		return COL_SCI_SOC;
    	} else if ("99".equals(classcd)) {
    		return COL_ALL;
    	}
    	return -1;    	
    }
    
    private String getRoundValue(final String value, int roundNum) {
        if (value == null) return null;
        
        BigDecimal bd = new BigDecimal(value);
        
        return bd.setScale(roundNum, BigDecimal.ROUND_HALF_UP).toString();
    }

    /**
     * 網掛けする。
     * @param field フィールド
     * @param value 値
     */
    void amikake(final Vrw32alp svf, final String field, final String value) {
    	svf.VrAttribute(field, "Paint=(2,70,2),Bold=1");
    	svf.VrsOut(field, value);
    	svf.VrAttribute(field, "Paint=(0,0,0),Bold=0");
    }
    
    void amikakeN(final Vrw32alp svf, final String field, final int line, final String value) {
    	svf.VrAttributen(field, line, "Paint=(2,70,2),Bold=1");
    	svf.VrsOutn(field, line, value);
    	svf.VrAttributen(field, line, "Paint=(0,0,0),Bold=0");
    }
    
    private static Map getHrclassMap(final DB2UDB db2, final Param param) {
    	final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("   HR_CLASS, HR_NAME ");
        stb.append(" FROM ");
        stb.append("   SCHREG_REGD_HDAT ");
        stb.append(" WHERE ");
        stb.append("   GRADE || HR_CLASS IN ('" + param._selectdata + "') ");
        stb.append(" ORDER BY ");
        stb.append("   HR_CLASS ");

        Map hrclassMap = new LinkedHashMap();
        
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();

            while (rs.next()) {
            	final String hrclass    = rs.getString("HR_CLASS");
            	final String hrname     = rs.getString("HR_NAME");
            	hrclassMap.put(hrclass, hrname);
            }
                        
        } catch (SQLException e) {
            log.debug("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        
        return hrclassMap;           
    }

    private static Map getSchregMap(final DB2UDB db2, final Param param) {
    	final StringBuffer stb = new StringBuffer();
        stb.append(" WITH REGD_BASE AS ( ");
        stb.append("   SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     T2.YEAR, ");
        stb.append("     T2.GRADE AS GRADE2 ");
        stb.append("   FROM ");
        stb.append("     SCHREG_REGD_DAT T1 ");
        stb.append("     INNER JOIN SCHREG_REGD_DAT T2 ");
        stb.append("       ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("       AND T2.YEAR <= T1.YEAR ");
        stb.append("     INNER JOIN SCHREG_REGD_GDAT T3 ");
        stb.append("       ON T3.YEAR = T2.YEAR ");
        stb.append("       AND T3.GRADE = T2.GRADE ");
        stb.append("       AND T3.SCHOOL_KIND = '" + param._schoolKind + "' ");
        stb.append("   WHERE ");
        stb.append("     T1.YEAR = '" + param._ctrlYear + "' ");
        if (SEME_ALL.equals(param._semester)) {
            stb.append("    AND T1.SEMESTER = '" + param._ctrlSemester + "' ");
        } else {
            stb.append("    AND T1.SEMESTER = '" + param._semester + "' ");
        }
        stb.append("     AND T1.GRADE || T1.HR_CLASS IN ('" + param._selectdata + "') ");
        stb.append("   GROUP BY ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     T2.YEAR, ");
        stb.append("     T2.GRADE ");
        stb.append(" ) ");
        stb.append(" , REGD_BASE2 AS ( ");
        stb.append("   SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     LISTAGG(T1.YEAR  || '-' ||T1.GRADE2 || '-' || T2.GRADE_CD, ',') WITHIN ");
        stb.append("   GROUP (ORDER BY CASE WHEN T1.GRADE = T1.GRADE2 THEN '1' ELSE '2' END , T1.GRADE2) AS GRADE_HIST ");
        stb.append("   FROM ");
        stb.append("     REGD_BASE T1 ");
        stb.append("   LEFT JOIN ");
        stb.append("   	 SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR ");
        stb.append("   	 					AND T2.GRADE = T1.GRADE2 ");
        stb.append("   GROUP BY ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("   T1.SCHREGNO, ");
        stb.append("   T1.GRADE, ");
        stb.append("   T1.HR_CLASS, ");
        stb.append("   T1.ATTENDNO, ");
        stb.append("   T1.GRADE_HIST, ");
        stb.append("   T2.NAME ");
        stb.append(" FROM ");
        stb.append("   REGD_BASE2 T1 ");
        stb.append("   LEFT JOIN SCHREG_BASE_MST T2 ");
        stb.append("   ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append(" ORDER BY ");
        stb.append("   T1.GRADE, ");
        stb.append("   T1.HR_CLASS, ");
        stb.append("   T1.ATTENDNO ");

        Map schregMap = new LinkedHashMap();
        
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();

            List list = null;
            while (rs.next()) {
            	Student student = new Student();
            	student._schregno   = rs.getString("SCHREGNO");
            	student._grade     	= rs.getString("GRADE");
            	student._hrclass    = rs.getString("HR_CLASS");
            	student._attendno   = rs.getString("ATTENDNO");
            	student._gradeHist  = rs.getString("GRADE_HIST");
            	student._name     	= rs.getString("NAME");
            	
            	final String mapkey = student._grade + student._hrclass;
            	if (!schregMap.containsKey(mapkey)) {
            		list = new ArrayList();
            		schregMap.put(mapkey, list);
            	}
        		((List)schregMap.get(mapkey)).add(student);
            }
        } catch (SQLException e) {
            log.debug("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        
        return schregMap;           
    }

    private static String sqlSchregRecordRankDat(final Param param, final String grade) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH REGD_DATA AS ( ");
        stb.append("   SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T2.YEAR, ");
        stb.append("     T2.GRADE AS GRADE2 ");
        stb.append("   FROM ");
        stb.append("     SCHREG_REGD_DAT T1 ");
        stb.append("     INNER JOIN SCHREG_REGD_DAT T2 ");
        stb.append("       ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("       AND T2.YEAR <= T1.YEAR ");
        stb.append("     INNER JOIN SCHREG_REGD_GDAT T3 ");
        stb.append("       ON T3.YEAR = T2.YEAR ");
        stb.append("       AND T3.GRADE = T2.GRADE ");
        stb.append("       AND T3.SCHOOL_KIND = '" + param._schoolKind + "' ");
        stb.append("   WHERE ");
        stb.append("     T1.YEAR = '" + param._ctrlYear + "' ");
        if (SEME_ALL.equals(param._semester)) {
            stb.append("    AND T1.SEMESTER = '" + param._ctrlSemester + "' ");
        } else {
            stb.append("    AND T1.SEMESTER = '" + param._semester + "' ");
        }
        stb.append("     AND T1.GRADE = '" + grade + "' ");
        stb.append("     AND T1.GRADE || T1.HR_CLASS IN ('" + param._selectdata + "') ");
        stb.append("   GROUP BY ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T2.YEAR, ");
        stb.append("     T2.GRADE ");
        stb.append(" ), REGD_DATA2 AS ( "); //REGD_DATAは選択した組だけ抽出した集団で、REGD_DATA2は指定した学年全体の集団。(学年平均を求めるために作成)
        stb.append("   SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T2.YEAR, ");
        stb.append("     T2.GRADE AS GRADE2 ");
        stb.append("   FROM ");
        stb.append("     SCHREG_REGD_DAT T1 ");
        stb.append("     INNER JOIN SCHREG_REGD_DAT T2 ");
        stb.append("       ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("       AND T2.YEAR <= T1.YEAR ");
        stb.append("     INNER JOIN SCHREG_REGD_GDAT T3 ");
        stb.append("       ON T3.YEAR = T2.YEAR ");
        stb.append("       AND T3.GRADE = T2.GRADE ");
        stb.append("       AND T3.SCHOOL_KIND = '" + param._schoolKind + "' ");
        stb.append("   WHERE ");
        stb.append("     T1.YEAR = '" + param._ctrlYear + "' ");
        if (SEME_ALL.equals(param._semester)) {
            stb.append("    AND T1.SEMESTER = '" + param._ctrlSemester + "' ");
        } else {
            stb.append("    AND T1.SEMESTER = '" + param._semester + "' ");
        }
        stb.append("     AND T1.GRADE = '" + grade + "' ");
        stb.append("   GROUP BY ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T2.YEAR, ");
        stb.append("     T2.GRADE ");
        stb.append(" ), RANK_DATA AS ( ");
        stb.append("   SELECT ");
        stb.append("     T2.GRADE, ");
        stb.append("     T2.GRADE2, ");
        stb.append("     T2.HR_CLASS, ");
        stb.append("     T2.SCHREGNO, ");
        stb.append("     T2.YEAR, ");
        stb.append("     T1.SEMESTER, ");
        stb.append("     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ");
        stb.append("     T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV AS TESTCD, ");
        stb.append("     T1.SCORE, ");
        stb.append("     T1.AVG ");
        stb.append("   FROM ");
        stb.append("     RECORD_RANK_SDIV_DAT T1 ");
        stb.append("     INNER JOIN REGD_DATA T2 ");
        stb.append("       ON T2.YEAR = T1.YEAR ");
        stb.append("       AND T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("   WHERE ");
        stb.append("       T1.SCHOOL_KIND = '" + param._schoolKind + "' ");
        stb.append("       AND T1.YEAR || T1.SEMESTER <= '" + param._ctrlYear + param._semester + "' ");
        stb.append("       AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV IN ('1010108','1990008','2010108','2990008','3990008','9990008') "); //不要なテスト種別を除く
        stb.append("       AND T1.SUBCLASSCD NOT IN ('333333', '777777', '99999A', '99999B') "); //不要な合計科目を除く
        stb.append("       AND NOT EXISTS ( ");
        stb.append("                             SELECT ");
        stb.append("                               'X' ");
        stb.append("                             FROM ");
        stb.append("                               SUBCLASS_REPLACE_COMBINED_DAT T2 ");
        stb.append("                             WHERE ");
        stb.append("                               T2.YEAR = T1.YEAR ");
        stb.append("                               AND T2.ATTEND_CLASSCD 		= T1.CLASSCD ");
        stb.append("                               AND T2.ATTEND_SCHOOL_KIND 	= T1.SCHOOL_KIND ");
        stb.append("                               AND T2.ATTEND_CURRICULUM_CD 	= T1.CURRICULUM_CD ");
        stb.append("                               AND T2.ATTEND_SUBCLASSCD 	= T1.SUBCLASSCD ");
        stb.append("       					) ");
        stb.append(" ), GRADE_AVG_DATA AS (");
        stb.append("   SELECT ");
        stb.append("     T1.YEAR, ");
        stb.append("     T2.GRADE, ");
        stb.append("     SUM(CASE WHEN T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '1010108' THEN T1.AVG ELSE NULL END) ");
        stb.append("         / MAX(CASE WHEN T2.SEMESTER || T2.TESTKINDCD || T2.TESTITEMCD || T2.SCORE_DIV = '1010108' THEN T2.COUNT ELSE NULL END) AS GRADE_AVG_1010108, ");
        stb.append("     SUM(CASE WHEN T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '1990008' THEN T1.AVG ELSE NULL END) ");
        stb.append("         / MAX(CASE WHEN T2.SEMESTER || T2.TESTKINDCD || T2.TESTITEMCD || T2.SCORE_DIV = '1990008' THEN T2.COUNT ELSE NULL END) AS GRADE_AVG_1990008, ");
        stb.append("     SUM(CASE WHEN T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '2010108' THEN T1.AVG ELSE NULL END) ");
        stb.append("         / MAX(CASE WHEN T2.SEMESTER || T2.TESTKINDCD || T2.TESTITEMCD || T2.SCORE_DIV = '2010108' THEN T2.COUNT ELSE NULL END) AS GRADE_AVG_2010108, ");
        stb.append("     SUM(CASE WHEN T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '2990008' THEN T1.AVG ELSE NULL END) ");
        stb.append("         / MAX(CASE WHEN T2.SEMESTER || T2.TESTKINDCD || T2.TESTITEMCD || T2.SCORE_DIV = '2990008' THEN T2.COUNT ELSE NULL END) AS GRADE_AVG_2990008, ");
        stb.append("     SUM(CASE WHEN T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '3990008' THEN T1.AVG ELSE NULL END) ");
        stb.append("         / MAX(CASE WHEN T2.SEMESTER || T2.TESTKINDCD || T2.TESTITEMCD || T2.SCORE_DIV = '3990008' THEN T2.COUNT ELSE NULL END) AS GRADE_AVG_3990008, ");
        stb.append("     SUM(CASE WHEN T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '9990008' THEN T1.AVG ELSE NULL END) ");
        stb.append("         / MAX(CASE WHEN T2.SEMESTER || T2.TESTKINDCD || T2.TESTITEMCD || T2.SCORE_DIV = '9990008' THEN T2.COUNT ELSE NULL END) AS GRADE_AVG_9990008 ");
        stb.append("   FROM ");
        stb.append("     RECORD_RANK_SDIV_DAT T1 ");
        stb.append("     INNER JOIN REGD_DATA2 REGD ");
        stb.append("       ON  REGD.YEAR = T1.YEAR ");
        stb.append("       AND REGD.SCHREGNO = T1.SCHREGNO ");
        stb.append("     INNER JOIN RECORD_AVERAGE_SDIV_DAT T2 ");
        stb.append("       ON  T2.YEAR = T1.YEAR ");
        stb.append("       AND T2.SEMESTER = T1.SEMESTER ");
        stb.append("       AND T2.TESTKINDCD = T1.TESTKINDCD ");
        stb.append("       AND T2.TESTITEMCD = T1.TESTITEMCD ");
        stb.append("       AND T2.SCORE_DIV = T1.SCORE_DIV ");
        stb.append("       AND T2.CLASSCD = T1.CLASSCD ");
        stb.append("       AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
        stb.append("       AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("       AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("       AND T2.AVG_DIV = '1' ");
        stb.append("   	   AND T2.GRADE = REGD.GRADE2 ");
        stb.append("       AND T2.HR_CLASS = '000' ");
        stb.append("       AND T2.COURSECD = '0' ");
        stb.append("       AND T2.MAJORCD = '000' ");
        stb.append("       AND T2.COURSECODE = '0000' ");
        stb.append("   WHERE ");
        stb.append("     T1.YEAR || T1.SEMESTER <= '"+ param._ctrlYear + param._semester + "'  ");
        stb.append("     AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD  = '" + param._all9SubCd +"' ");
        stb.append("   GROUP BY ");
        stb.append("     T1.YEAR, ");
        stb.append("     T2.GRADE ");
        stb.append(" ) ");

        stb.append(" SELECT ");
        stb.append("   CASE WHEN T1.GRADE = T1.GRADE2 THEN '1' ELSE '0' END AS CURRENT_FLG, ");
        stb.append("   T1.HR_CLASS, ");
        stb.append("   T1.SCHREGNO, ");
        stb.append("   T1.YEAR, ");
        stb.append("   T1.SUBCLASSCD, ");
        stb.append("   T2.CLASSABBV, ");
        stb.append("   T3.SUBCLASSABBV, ");
        stb.append("   SUM(CASE WHEN T1.TESTCD = '1010108' THEN T1.SCORE ELSE NULL END) AS SCORE_1010108, ");
        stb.append("   SUM(CASE WHEN T1.TESTCD = '1990008' THEN T1.SCORE ELSE NULL END) AS SCORE_1990008, ");
        stb.append("   SUM(CASE WHEN T1.TESTCD = '2010108' THEN T1.SCORE ELSE NULL END) AS SCORE_2010108, ");
        stb.append("   SUM(CASE WHEN T1.TESTCD = '2990008' THEN T1.SCORE ELSE NULL END) AS SCORE_2990008, ");
        stb.append("   SUM(CASE WHEN T1.TESTCD = '3990008' THEN T1.SCORE ELSE NULL END) AS SCORE_3990008, ");
        stb.append("   SUM(CASE WHEN T1.TESTCD = '9990008' THEN T1.SCORE ELSE NULL END) AS SCORE_9990008, ");
        stb.append("   SUM(CASE WHEN T1.TESTCD = '1010108' THEN T1.AVG   ELSE NULL END) AS AVG_1010108, ");
        stb.append("   SUM(CASE WHEN T1.TESTCD = '1990008' THEN T1.AVG   ELSE NULL END) AS AVG_1990008, ");
        stb.append("   SUM(CASE WHEN T1.TESTCD = '2010108' THEN T1.AVG   ELSE NULL END) AS AVG_2010108, ");
        stb.append("   SUM(CASE WHEN T1.TESTCD = '2990008' THEN T1.AVG   ELSE NULL END) AS AVG_2990008, ");
        stb.append("   SUM(CASE WHEN T1.TESTCD = '3990008' THEN T1.AVG   ELSE NULL END) AS AVG_3990008, ");
        stb.append("   SUM(CASE WHEN T1.TESTCD = '9990008' THEN T1.AVG   ELSE NULL END) AS AVG_9990008, ");
        stb.append("   GRADE_AVG.GRADE_AVG_1010108, ");
        stb.append("   GRADE_AVG.GRADE_AVG_1990008, ");
        stb.append("   GRADE_AVG.GRADE_AVG_2010108, ");
        stb.append("   GRADE_AVG.GRADE_AVG_2990008, ");
        stb.append("   GRADE_AVG.GRADE_AVG_3990008, ");
        stb.append("   GRADE_AVG.GRADE_AVG_9990008 ");
        stb.append("   FROM RANK_DATA T1 ");
        stb.append("   LEFT JOIN GRADE_AVG_DATA GRADE_AVG ");
        stb.append("   	 ON  GRADE_AVG.YEAR  = T1.YEAR ");
        stb.append("   	 AND GRADE_AVG.GRADE = T1.GRADE2 ");
        stb.append("   LEFT JOIN CLASS_MST T2 ");
        stb.append("   	 ON T2.CLASSCD = SUBSTR(T1.SUBCLASSCD, 1, 2) ");
        if ("1".equals(param._useCurriculumcd)) {
        	stb.append("   	AND T2.SCHOOL_KIND = '" + param._schoolKind + "' ");
        }
        stb.append("   LEFT JOIN SUBCLASS_MST T3 ");
        if ("1".equals(param._useCurriculumcd)) {
        	stb.append("   	ON T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD = T1.SUBCLASSCD ");
        } else {
        	stb.append("   	ON T3.SUBCLASSCD = T1.SUBCLASSCD  ");
        }
        stb.append("   WHERE T1.YEAR || T1.SEMESTER <= '"+ param._ctrlYear + param._semester + "' ");
        stb.append("   GROUP BY T1.HR_CLASS, T1.SCHREGNO, T1.GRADE, T1.GRADE2, T1.YEAR, T1.SUBCLASSCD, T2.CLASSABBV, T3.SUBCLASSABBV, GRADE_AVG_1010108, GRADE_AVG_1990008, GRADE_AVG_2010108, GRADE_AVG_2990008, GRADE_AVG_3990008, GRADE_AVG_9990008 ");
        stb.append("   ORDER BY T1.HR_CLASS, T1.SCHREGNO, CASE WHEN CURRENT_FLG THEN '1' ELSE '2' END, T1.YEAR, T1.SUBCLASSCD ");
        
        return stb.toString();
    }
   
    private static List getSubclassGroupList(final DB2UDB db2, final Param param, final String schregno) {
    	final StringBuffer stb = new StringBuffer();
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        stb.append(" WITH SCHREG_DATA AS ( ");
        stb.append("   SELECT ");
        stb.append("     YEAR ");
        stb.append("     , GRADE ");
        stb.append("     , COURSECD ");
        stb.append("     , MAJORCD ");
        stb.append("     , COURSECODE ");
        stb.append("   FROM ");
        stb.append("     SCHREG_REGD_DAT ");
        stb.append("   WHERE ");
        stb.append("     YEAR = '" + param._ctrlYear + "' ");
        stb.append("     AND SEMESTER = '" + param._ctrlSemester + "' ");
        stb.append("     AND SCHREGNO = '" + schregno + "' ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("   T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD");
        stb.append(" FROM ");
        stb.append("   REC_SUBCLASS_GROUP_DAT T1 ");
        stb.append("   INNER JOIN SCHREG_DATA T2 ");
        stb.append("     ON T2.YEAR = T1.YEAR ");
        stb.append("     AND T2.GRADE = T1.GRADE ");
        stb.append("     AND T2.COURSECD = T1.COURSECD ");
        stb.append("     AND T2.MAJORCD = T1.MAJORCD ");
        stb.append("     AND T2.COURSECODE = T1.COURSECODE ");
        stb.append(" WHERE GROUP_DIV = '5' ");

        
        List subclassGroupList = new ArrayList();
        try {
            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();

            while (rs.next()) {
            	final String subclasscd    	= rs.getString("SUBCLASSCD");
            	subclassGroupList.add(subclasscd);
            }
            
        } catch (SQLException e) {
            log.fatal("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        
        return subclassGroupList;           
    }
    
    private static Map getSubclassListMap(final DB2UDB db2, final Param param, final String grade) {
    	final String sql = sqlSchregRecordRankDat(param, grade);
    	log.debug("record sql"+sql);
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        Map subclassListMap = new LinkedHashMap();
        try {
            ps = db2.prepareStatement(sql.toString());
            rs = ps.executeQuery();

            while (rs.next()) {
            	final String hrclass    	= rs.getString("HR_CLASS");
            	final String schregno   	= rs.getString("SCHREGNO");
            	final String year          = rs.getString("YEAR");
            	final String subclasscd    = rs.getString("SUBCLASSCD");
            	final String classAbbv 	    = StringUtils.defaultString(rs.getString("CLASSABBV"));
            	final String subAbbv	    = StringUtils.defaultString(rs.getString("SUBCLASSABBV"));
            	final String currentFlgStr = rs.getString("CURRENT_FLG");
             	final boolean currentFlg  = "1".equals(currentFlgStr) ? true : false;
            	final Map scoreMap 			= new LinkedHashMap();
            	final Map avgMap 			= new LinkedHashMap();
            	final Map gradeAvgMap 		= new LinkedHashMap();
            	
            	
            	for(String testcd: TESTCD_ARRAY) {
            		if (!currentFlg && !"9990008".equals(testcd)) continue;
            		scoreMap.put(testcd, rs.getString("SCORE_" + testcd));
            		avgMap.put(testcd, rs.getString("AVG_" + testcd));
            		gradeAvgMap.put(testcd, rs.getString("GRADE_AVG_" + testcd));
            	}
            	
            	final String mapkey = hrclass+"-"+schregno+"-"+year;
            	if (!subclassListMap.containsKey(mapkey)) {
            		subclassListMap.put(mapkey, new ArrayList());
            	}
        		List list = (List)subclassListMap.get(mapkey);
        		SubclassRecord subRecord = new SubclassRecord(subclasscd, classAbbv, subAbbv, scoreMap, avgMap, gradeAvgMap);
    			subRecord.setCurrentFlg(currentFlg);
        		list.add(subRecord);
            }
            
        } catch (SQLException e) {
            log.fatal("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        
        return subclassListMap;           
    }

    /**
     * 生徒
     */
    private static class Student {
    	private String _schregno;
    	private String _grade;
    	private String _hrclass;
    	private String _attendno;
    	private String _gradeHist; //対象生徒の過年度を含めた学年（カンマ区切り）
    	private String _name;
    	private List _yearList = new ArrayList();
    	private Map _mockCdMap = new LinkedHashMap();
    	private Map _classCdMap = new LinkedHashMap();
    	private Map _totalMap = new LinkedHashMap();
    	
    }
    
    private static class SubclassRecord {
    	String _subclasscd;
    	String _classAbbv;
    	String _subAbbv;
    	Map _scoreMap;
    	Map _avgMap;
    	Map _gradeAvgMap;
    	boolean _currentFlg = false; //最新の学年かどうかのフラグ
    	SubclassRecord(final String subclasscd, final String classAbbv, final String subAbbv, final Map scoreMap, final Map avgMap, final Map gradeAvgMap) {
    		_subclasscd 	= subclasscd;
    		_classAbbv  	= classAbbv;
    		_subAbbv    	= subAbbv;
    		_scoreMap 		= scoreMap;
    		_avgMap 		= avgMap;
    		_gradeAvgMap 	= gradeAvgMap;
        }        
    	private void setCurrentFlg(final boolean currentFlg) {
    		_currentFlg = currentFlg;
    	}
    	
    	private boolean getCurrentFlg() {
    		return _currentFlg;
    	}
    }

    /*
     * 模試用
     */
    
    private static class MockClass {
    	final String _classcd;
    	final String _classAbbv;
    	
    	MockClass(String classcd, final String classAbbv) {
    		_classcd = classcd;
    		_classAbbv = classAbbv;
    	}
    }
  
    private Map getMockRankRangeMap(final DB2UDB db2, final Param param, final Student student) {
    	
    	final String[] gradeAndYearArray = student._gradeHist.split(",");
    	String yearHist = "('";
    	String sep = "";
    	for (final String gradeAndYear : gradeAndYearArray) {
    		yearHist += sep + gradeAndYear.split("-")[0];
    		sep = "','";
    	}
    	yearHist += "')";
    	
    	final StringBuffer stb = new StringBuffer();

    	stb.append("  WITH SCH_SEME AS ( ");
        stb.append("  SELECT ");
        stb.append("    YEAR, ");
        stb.append("    MAX(SEMESTER) AS SEMESTER, ");
        stb.append("    SCHREGNO ");
        stb.append("  FROM ");
        stb.append("    SCHREG_REGD_DAT ");
        stb.append("  WHERE ");
        stb.append("   YEAR || SEMESTER <= '" + _param._ctrlYear + _param._semester + "' ");
        stb.append("   AND YEAR IN " + yearHist + " ");
        stb.append("   AND SCHREGNO = '" + student._schregno + "' ");
        stb.append("  GROUP BY ");
        stb.append("    YEAR,SCHREGNO ");

        stb.append(" ), MOCK_TOTAL AS ( ");
        stb.append("  SELECT ");
        stb.append("    T1.YEAR, ");
        stb.append("    T1.MOCKCD, ");
        stb.append("    T1.MOCK_SUBCLASS_CD ");
        stb.append("  FROM ");
        stb.append("    MOCK_TOTAL_SUBCLASS_DAT T1");
        stb.append("    INNER JOIN  ");
        stb.append("    SCHREG_REGD_DAT T2");
        stb.append("    ON T2.YEAR = T1.YEAR");
        stb.append("    AND T2.COURSECD = T1.COURSECD");
        stb.append("    AND T2.MAJORCD = T1.MAJORCD");
        stb.append("    AND T2.COURSECODE = T1.COURSECODE");
        stb.append("    INNER JOIN  ");
        stb.append("    SCH_SEME T3");
        stb.append("    ON T3.YEAR = T2.YEAR");
        stb.append("    AND T3.SEMESTER = T2.SEMESTER ");
        stb.append("    AND T3.SCHREGNO = T2.SCHREGNO");
        stb.append(" ) ");

        stb.append(" SELECT ");
        stb.append("   T1.YEAR, ");
        stb.append("   T1.MOCKCD, ");
        stb.append("   CASE WHEN T5.MOCK_SUBCLASS_CD IS NOT NULL THEN '99' ELSE T3.CLASSCD END AS CLASSCD, ");
        stb.append("   T1.MOCK_SUBCLASS_CD, ");
        stb.append("   T3.SUBCLASS_ABBV, ");
        stb.append("   T1.DEVIATION, ");
        stb.append("   T2.MOCKNAME1, ");
        stb.append("   T4.CLASSABBV ");
        stb.append(" FROM ");
        stb.append("   MOCK_RANK_RANGE_DAT T1 ");
        stb.append("   LEFT JOIN MOCK_MST T2 ON T2.MOCKCD = T1.MOCKCD ");
        stb.append("   LEFT JOIN MOCK_SUBCLASS_MST T3 ON T3.MOCK_SUBCLASS_CD = T1.MOCK_SUBCLASS_CD ");
        stb.append("   LEFT JOIN CLASS_MST T4 ON T4.CLASSCD = T3.CLASSCD ");
        stb.append("                         AND T4.SCHOOL_KIND = T3.SCHOOL_KIND ");
        //MOCK_RANK_RANGE_DATの模試科目が総合科目かをチェックするために結合
        stb.append("   LEFT JOIN MOCK_TOTAL T5 ON T5.YEAR = T1.YEAR ");
        stb.append("                           AND T5.MOCKCD = T1.MOCKCD ");
        stb.append("                           AND T5.MOCK_SUBCLASS_CD = T1.MOCK_SUBCLASS_CD ");
        stb.append(" WHERE ");
        stb.append("   T1.YEAR IN " + yearHist + " ");
        stb.append("   AND T1.SCHREGNO = '" + student._schregno + "' ");
        stb.append("   AND T1.RANK_RANGE = '1' ");
        stb.append("   AND T1.RANK_DIV = '02' ");
        stb.append("   AND T1.MOCKDIV = '1' ");
        stb.append("   AND T1.DEVIATION IS NOT NULL ");
        stb.append(" ORDER BY ");
        stb.append("   T2.MOSI_DATE DESC, ");
        stb.append("   T1.MOCKCD DESC, ");
        stb.append("   T3.CLASSCD, ");
        stb.append("   T3.MOCK_SUBCLASS_CD ");

    	
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        Map mockRankRangeMap = new LinkedHashMap();

        try {
            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();
            
            while (rs.next()) {
            	final String year	  	 	 = rs.getString("YEAR");
            	final String mockcd		     = rs.getString("MOCKCD");
            	final String mockname       = rs.getString("MOCKNAME1");
            	final String classcd	     = rs.getString("CLASSCD");
            	final String classAbbv	     = rs.getString("CLASSABBV");
            	final String mockSubcd 	 	 = rs.getString("MOCK_SUBCLASS_CD");
            	final String subAbbv        = rs.getString("SUBCLASS_ABBV");
            	final String deviation	 	 = rs.getString("DEVIATION");

            	if (classcd == null) continue; //MOCK_SUBCLASS_MSTでCLASSCDを未設定の場合は帳票に出さない
            	
            	//生徒ごとの校外実力テストの縦横項目を取得
            	student._mockCdMap.put(year + "-" + mockcd, mockname);
            	student._classCdMap.put(classcd, classAbbv);
            	final String mapkey = year + "-" + mockcd + "-" + classcd;

            	if (!mockRankRangeMap.containsKey(mapkey)) {
            		mockRankRangeMap.put(mapkey, new ArrayList());
            	}
        		List list = (List)mockRankRangeMap.get(mapkey);
        		list.add(subAbbv + "_" + deviation);
            }
        } catch (SQLException e) {
            log.fatal("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        
        return mockRankRangeMap;           
    }
    
    private static class MockRankRange {
    	final String _mockcd;
    	final String _mockSubcd;
    	final String _subclassAbbv;
    	final String _deviation;
    	final String _mockname;
    	
    	MockRankRange(final String mockcd, final String mockSubcd, final String subclassAbbv, final String deviation, final String mockname) {
    		_mockcd = mockcd;
    		_mockSubcd = mockSubcd;
    		_subclassAbbv = subclassAbbv;
    		_deviation = deviation;
    		_mockname = mockname;
    	}
    }
   
    /*
     * パラメータ
     */
    
    private Param createParam(HttpServletRequest request) throws Exception {
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(request);
        return param;
    }

    private static class Param {

        /** 年度 */
        final String _ctrlYear;
        /** 学期 */
        final String _semester;
        /** LOG-IN時の学期（現在学期） */
        final String _ctrlSemester;
        final String _ctrlDate;
        //final String _testcd;
        final String _grade;
        // final String _form;
        final String _group;
        final String _formSelect; // 1:18科目 2:25科目
        final String _useCurriculumcd;
        private String _semesterName;
        private String _testItemName;
        private String _gradeCd;
        private String _gradeName;
        private String _schoolKind;
        private String _schoolname;
        private String _schoolcd;
        private String _hrstaffname;
        private String _selectdata;
        private String _all5SubCd;
        private String _all9SubCd;
        private static final String FROM_TO_MARK = "\uFF5E";

        Param(final HttpServletRequest request) throws ServletException {
            _ctrlYear = request.getParameter("YEAR");
            _schoolcd = request.getParameter("SCHOOLCD");
            _semester = request.getParameter("SEMESTER");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _grade = request.getParameter("GRADE");
            _group = request.getParameter("GROUP"); // 1:クラス別 2:コース別
            _formSelect = request.getParameter("FORM_SELECT");
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _selectdata = request.getParameter("selectdata").replace(",", "','");
        }

        public void load(final DB2UDB db2, final String grade) {
            setGradeName(db2, grade);
            setSemesterName(db2);
            setSchoolName(db2);
            _all5SubCd = "55-" + _schoolKind + "-99-555555";
            _all9SubCd = "99-" + _schoolKind + "-99-999999";
        }
        
        private void setGradeName(final DB2UDB db2, final String grade) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = "SELECT SCHOOL_KIND, GRADE_CD, GRADE_NAME1 FROM SCHREG_REGD_GDAT " 
                        + "WHERE YEAR = '" + _ctrlYear + "' AND GRADE = '" + grade  +"' ";
                // log.debug(" gradeName sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                _gradeName = "";
                _schoolKind = "";
                if (rs.next()) {
                	_gradeCd = rs.getString("GRADE_CD");
                    _gradeName = rs.getString("GRADE_NAME1");
                    _schoolKind = rs.getString("SCHOOL_KIND");
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private void setSemesterName(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                String sql = "SELECT * FROM SEMESTER_MST WHERE YEAR = '" + _ctrlYear + "' AND SEMESTER = '" + _semester + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    _semesterName = rs.getString("SEMESTERNAME");
                }
            } catch (Exception ex) {
                log.error("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private void setSchoolName(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                String sql = " SELECT SCHOOLNAME1 FROM SCHOOL_MST WHERE YEAR = '" + _ctrlYear + "' AND SCHOOLCD = '" + _schoolcd + "' AND SCHOOL_KIND = '" + _schoolKind + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    _schoolname = rs.getString("SCHOOLNAME1");
                }
            } catch (Exception ex) {
                log.error("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
    }
}
