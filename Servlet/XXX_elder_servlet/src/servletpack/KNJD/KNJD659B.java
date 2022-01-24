// kanji=漢字
/*
 * $Id: 2dca34ff6f749ae29e1aeecf91405507a78a89a6 $
 */
package servletpack.KNJD;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 * 学校教育システム 賢者 [成績管理]  成績資料
 */
public class KNJD659B {

    private static final Log log = LogFactory.getLog(KNJD659B.class);

    private static final String SEMEALL = "9";
    private static final String SUBCLASSCD999999 = "999999";

    private static final String STATUS = "STATUS";
    private static final String VIEWFLG = "VIEWFLG";
    private static final String ASSESS_SHOW1 = "ASSESS_SHOW1";

    private static final int DistriGrpRange[][] = {{100,95},{94,90},{89,85},{84,80},{79,75},{74,70},{69,65},{64,60},{59,55},{54,50},{49,0}};
    private boolean _hasData;

    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws ServletException, IOException {
        Vrw32alp svf = null;
        DB2UDB db2 = null;
        try {
            svf = new Vrw32alp();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
            svf.VrInit();
            svf.VrSetSpoolFileStream(response.getOutputStream());

            response.setContentType("application/pdf");

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            // パラメータの取得
            Param param = createParam(request, db2);

            printMain(db2, svf, param);

        } catch (final Exception ex) {
            log.error("error! ", ex);
        } finally {
            if (null != db2) {
                try {
                    db2.commit();
                    db2.close();
                } catch (Exception ex) {
                    log.error("db close error!", ex);
                }
            }
            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note" , "note");
                svf.VrEndPage();
            }
            svf.VrQuit();
        }
    }

    private void printMain(
            final DB2UDB db2,
            final Vrw32alp svf,
            final Param param
    ) {
        final List studentList = Student.getStudentList(db2, param);
        if (studentList.isEmpty()) {
            return;
        }

        load(param, db2, studentList);

        String formname = "";
        List<EvalTtl> evaluationTitleList = null;

        if (param._is5nenOr6nen) {
        	formname = "KNJD659B_2.frm";
        	evaluationTitleList = Student.setEvaluationTitle(db2, param);
        } else {
        	formname = "KNJD659B_1.frm";
        }
        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            log.info(" schregno = " + student._schregno);
            svf.VrSetForm(formname, 4);
            setTitle(svf, student, param);
            if (param._is5nenOr6nen) {
            	printHyoutei(svf, student, param, evaluationTitleList);
            }
        	printRecord(svf, student, param);
            svf.VrEndPage();
            _hasData = true;
        }
    }

    private void load(final Param param, final DB2UDB db2, final List studentList) {
        ViewClass.setViewClassList(db2, param, studentList);
        Student.setRecordDat(db2, param, studentList);
        if (param._is5nenOr6nen) {
            setDistributtionDat(db2, param, studentList);
            Student.setEvaluationDat(db2, param, studentList);
        }
    }

    private void setTitle(final Vrw32alp svf, final Student student, final Param param) {
    	svf.VrsOut("TITLE", toZenkaku(param._nendo + " " + StringUtils.defaultString(student._gradeName1) + StringUtils.defaultString((String)param._semesternameMap.get(param._semester)) + "　成績資料"));
    	svf.VrsOut("HR_NAME", toZenkaku(StringUtils.defaultString(student._hrName) + StringUtils.defaultString(student._attendno) + "番"));
    	int namelen = KNJ_EditEdit.getMS932ByteLength(student._name);
    	String namefield = namelen > 30 ? "3" : namelen > 20 ? "2" : "1";
    	svf.VrsOut("NAME" + namefield, student._name);
    	return;
    }

	private String toZenkaku(final String s) {
		final StringBuffer stb = new StringBuffer();
		if (null != s) {
			for (int i = 0; i < s.length(); i++) {
				char ch = s.charAt(i);
				if ('0' <= ch && ch <= '9') {
					ch = (char) (ch - '0' + '０');
				}
				stb.append(ch);
			}
		}
		return stb.toString();
	}

	private void printHyoutei(final Vrw32alp svf, final Student student, final Param param, final List<EvalTtl> evaluationTitleList) {
		final int maxCol = 9;
    	String lastyear = String.valueOf(Integer.parseInt(param._year) - 1);

        //評価
        String bkkcode = "";
        final List scoreList = new ArrayList();
        for (int jj = 0; jj < evaluationTitleList.size();jj++) {
        	final EvalTtl ettl = evaluationTitleList.get(jj);
        	if (jj >= maxCol) {
        		continue;
        	}
    	    //科目名称
    		svf.VrsOutn("CLASS_NAME2", jj+1, ettl._classname);
	        for (int ii = 0; ii < student._evaluationDat.size();ii++) {
	        	final EvalDat edat = student._evaluationDat.get(ii);
        		if (edat._classcd.equals(ettl._classcd)) {
		        	if (!bkkcode.equals(edat.getSubClsCd())) {
		        	    //科目名称
		        		svf.VrsOutn("CLASS_NAME2", jj+1, edat._classname);
		        	}
		        	//指定学年が5年なら5年のみ出力、それ以外(指定学年が6年)なら「昨年度のデータは5年、当年度なら6年」
		        	if ("05".equals(param._gradeCd) || lastyear.equals(edat._year)) {
		        		svf.VrsOutn("VALUE1", jj+1, edat._score);
		        	} else {
		        		svf.VrsOutn("VALUE2", jj+1, edat._score);
		        	}
//	        		if (param._year.equals(edat._year)) {
	        			if (NumberUtils.isNumber(edat._score)) {
	        				scoreList.add(new BigDecimal(edat._score));
	        			}
//	        		}
        		}
	        	bkkcode = edat.getSubClsCd();
	        }
        }

        final String average = getAverage(scoreList);
        if (null != average) {
    		svf.VrsOut("AVERAGE", average);
        }

//        for (int ii = 0; ii < student._evaluationDat.size();ii++) {
//            final EvalDat edat = (EvalDat)student._evaluationDat.get(ii);
//
//            //SUBCLASS=999999は評価平均
//            if (SUBCLASSCD999999.equals(edat._subclasscd)) {
//                //当年度分のみ出力。
//                if (param._year.equals(edat._year)) {
//                    svf.VrsOut("AVERAGE", edat._score);
//                }
//            } else {
//                if (colCnt > maxCol) {
//                    continue;
//                }
//                //次レコード判定
//                if (!bkkcode.equals(edat.getSubClsCd())) {
//                    //出力位置を次列にスライド
//                    colCnt++;
//                    if (colCnt > maxCol) {
//                        continue;
//                    }
//                    //科目名称
//                    svf.VrsOutn("CLASS_NAME2", colCnt, edat._classname);
//                }
//                //指定学年が5年なら5年のみ出力、それ以外(指定学年が6年)なら「昨年度のデータは5年、当年度なら6年」
//                if ("05".equals(param._gradeCd) || lastyear.equals(edat._year)) {
//                    svf.VrsOutn("VALUE1", colCnt, edat._score);
//                } else {
//                    svf.VrsOutn("VALUE2", colCnt, edat._score);
//                }
//            }
//            bkkcode = edat.getSubClsCd();
//        }
	}

	private String getAverage(final List<BigDecimal> scoreList) {
		if (scoreList.isEmpty()) {
			return null;
		}
		BigDecimal sum = new BigDecimal(0);
		for (int i = 0; i < scoreList.size(); i++) {
			final BigDecimal n = scoreList.get(i);
			sum = sum.add(n);
		}
		return sum.divide(new BigDecimal(scoreList.size()), 1, BigDecimal.ROUND_HALF_UP).toString();
	}

	private void printRecord(final Vrw32alp svf, final Student student, final Param param) {

		if (param._isOutputDebug) {
			log.info(" ViewClass size " + student._viewClassList.size());
		}

		final String printMarkPattern = "[0-9]{3}3"; // ABC表記する観点コードパターン = 4桁で下1桁が3
        String bakclasscd = "";
        int viewcnt = 0;
		int count = 0;
		printView:
		for (int i = 0; i < student._viewClassList.size(); i++) {
			final ViewClass vc = student._viewClassList.get(i);

			final List<String> classnameCharList = getCenterizedCharList(vc._classname, vc.getViewSize());
			if (param._isOutputDebug) {
				log.info(" " + vc + ", classnameCharList = " + classnameCharList);
			}
			for (int vi = 0; vi < vc.getViewSize(); vi++) {
    	        final String viewcd = vc.getViewCd(vi);
				final String viewname = vc.getViewName(vi);
    			String recorddiv = null;

		        if ("".equals(bakclasscd) || !bakclasscd.equals(vc._classcd)) {
		        	viewcnt++;
		        	svf.VrsOut("GRPCD", String.valueOf(viewcnt));
		        } else {
		        	svf.VrsOut("GRPCD", String.valueOf(viewcnt));
		        }
	    		recorddiv = "1";
    	    	if (vi < classnameCharList.size()) {
    	    		svf.VrsOut("CLASS_NAME", classnameCharList.get(vi)); // 教科名
    	    	}
    			if (param._isOutputDebug) {
    				log.info(" view " + vi + " recorddiv " + recorddiv);
    			}
    			if (KNJ_EditEdit.getMS932ByteLength(viewname) > 30) {
    		        if (param._is5nenOr6nen) {
                        svf.VrsOut("ITEM_NAME2", viewname); // 学習の記録観点
    		        } else {
        				svf.VrsOut("ITEM_NAME", viewname); // 学習の記録観点
    		        }
    			} else {
    				svf.VrsOut("ITEM_NAME", viewname); // 学習の記録観点
    			}
    			final boolean isPrintMark = Pattern.matches(printMarkPattern, viewcd);
    	        //SUBCLASSCD(結合型)とVIEWCDで成績データと紐づけ
    	        RecDat rdat = findRecDat(student, vc._subclasscd, viewcd);
    	        if (rdat != null) {
    	        	if (isPrintMark) {
      	                svf.VrsOut("GRADE_AVE", "※");
//      	                final int iShowDat = NumberUtils.isNumber(rdat._showdat) ? (int) Double.parseDouble(rdat._showdat) : -1;
//      	                String showdatMark = "";
//      	                if (param._is1nenOr2nen) {
//      	                	// １年・２年
//      	                	if (90 <= iShowDat) {
//      	                		showdatMark = "Ａ";
//      	                	} else if (70 <= iShowDat) {
//      	                		showdatMark = "Ｂ";
//      	                	} else if (0 <= iShowDat) {
//      	                		showdatMark = "Ｃ";
//      	                	}
//      	                } else {
//      	                	// ３年～６年
//      	                	if (85 <= iShowDat) {
//      	                		showdatMark = "Ａ";
//      	                	} else if (65 <= iShowDat) {
//      	                		showdatMark = "Ｂ";
//      	                	} else if (0 <= iShowDat) {
//      	                		showdatMark = "Ｃ";
//      	                	}
//      	                }
//      	                svf.VrsOut("SCORE", showdatMark);
                        svf.VrsOut("SCORE", rdat._status);
    	        	} else {
      	                svf.VrsOut("GRADE_AVE", rdat.getAvgStr());
                        svf.VrsOut("SCORE", rdat._showdat);
    	        	}
    	        }
    	        if (param._is5nenOr6nen) {
    	        	DistriDat ddat = findDistributionData(param, vc._subclasscd, viewcd);
        	        if (ddat != null) {
        	        	if (isPrintMark) {
        	        		// ABC印字
    	        			for (int ii = 1;ii <= 11;ii++) {
    	        				svf.VrsOut("KOME"+ii, "※");
    	        			}
        	        	} else if ("1".equals(ddat._mantenflg)) {
        	        		for (int ii = 0;ii < 11;ii++) {
        	        			int chkrng[] = DistriGrpRange[ii];
        	        	        if (rdat != null) {
        	        	        	if (NumberUtils.isDigits(rdat._showdat)) {
        	        	        		if (chkrng[1] <= Integer.parseInt(rdat._showdat) && Integer.parseInt(rdat._showdat) <= chkrng[0] ) {
        	        	        			svf.VrsOut("HERE"+(ii+1), "〇");
        	        	        		}
        	        	        	}
        	        	        }
                                svf.VrsOut("NUM"+(ii+1), ddat.getRangeN(ii+1));
        	        		}
        	        	} else {
        	        		boolean isAnyCount = false;
        	        		for (int ii = 0;ii < 11;ii++) {
        	        			if (NumberUtils.isDigits(ddat.getRangeN(ii+1)) && Integer.parseInt(ddat.getRangeN(ii+1)) > 0) {
        	        				isAnyCount = true;
        	        				break;
        	        			}
        	        		}
        	        		if (isAnyCount) {
        	        			// 得点がはいっている観点に※を印字する
        	        			for (int ii = 1;ii <= 11;ii++) {
        	        				svf.VrsOut("KOME"+ii, "※");
        	        			}
        	        		}
        	        	}
        	        }
    	        }
    	        bakclasscd = vc._classcd;
    			svf.VrEndRecord();
    			count += 1;
			}
		}
	}

	private RecDat findRecDat(final Student student, final String vcSubclasscd, final String vcViewCd) {
		RecDat retdat = null;
		for (int ii = 0;ii < student._recordDat.size();ii++) {
			RecDat wkdat = student._recordDat.get(ii);
			if (wkdat.getSubClsCd().equals(vcSubclasscd) && wkdat._viewcd.equals(vcViewCd)) {
				retdat = wkdat;
				break;
			}
		}
		return retdat;
	}

	private static DistriDat findDistributionData(final Param param, final String vcSubclasscd, final String vcViewCd) {
		DistriDat retdat = null;
		for (int ii = 0;ii < param._distributionDat.size();ii++) {
			DistriDat wkdat = param._distributionDat.get(ii);
			if (wkdat.getSubClsCd().equals(vcSubclasscd) && wkdat._viewcd.equals(vcViewCd)) {
				retdat = wkdat;
				break;
			}
		}
		return retdat;
	}

    private static String hankakuToZenkaku(final String str) {
        if (null == str) {
            return null;
        }
        final String[] nums = new String[]{"０", "１", "２", "３", "４", "５", "６", "７", "８", "９"};
        final StringBuffer stb = new StringBuffer();
        for (int i = 0; i < str.length(); i++) {
            final String s = String.valueOf(str.charAt(i));
            if (NumberUtils.isDigits(s)) {
                final int j = Integer.parseInt(s);
                stb.append(nums[j]);
            } else {
                stb.append(s);
            }
        }
        return stb.toString();
    }

    private static Map getMappedMap(final Map map, final String key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new TreeMap());
        }
        return (Map) map.get(key1);
    }

	private static List<String> getCenterizedCharList(final String name, final int count) {
		final StringBuffer stb = new StringBuffer();
		if (null != name) {
			final int spc1 = (count - name.length()) / 2;
			stb.append(StringUtils.repeat(" ", spc1));
			stb.append(name);
			stb.append(StringUtils.repeat(" ", count - stb.length()));
		}
		final List<String> list = new ArrayList();
		for (int i = 0; i < stb.length(); i++) {
			list.add(String.valueOf(stb.charAt(i)));
		}
		return list;
	}

    public static void setDistributtionDat(final DB2UDB db2, final Param param, final List studentList) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final StringBuffer stb = new StringBuffer();
            //まず、100点満点のデータコードを抽出するために、グループ化(集計)する。
            stb.append(" WITH DATCNT AS ( ");
            stb.append(" select ");
            stb.append("  YEAR, SCHOOL_KIND, PATTERN_CD,COUNT(*) AS CNT, MIN(ASSESSLOW) AS ASSESMIN, MAX(ASSESSHIGH) AS ASSESMAX ");
            stb.append(" from ");
            stb.append("  JVIEWSTAT_LEVEL_PATTERN_DAT ");
            stb.append(" GROUP BY ");
            stb.append("  YEAR, SCHOOL_KIND, PATTERN_CD ");
            //上記から、100点満点のデータコードを抽出する
            stb.append(" ), MANTENTBL AS ( ");
            stb.append(" SELECT ");
            stb.append("  * ");
            stb.append(" FROM ");
            stb.append("  DATCNT ");
            stb.append(" WHERE ");
            stb.append("  ASSESMIN = 0 AND ASSESMAX = 100 ");
            //分布図を作成する。集計は、SCOREベースで集計する。'※'はSQLではなく処理側で対応する。
            stb.append(" ) ");
            stb.append(" select ");
            stb.append("  JVSRD.CLASSCD, ");
            stb.append("  JVSRD.SCHOOL_KIND, ");
            stb.append("  JVSRD.CURRICULUM_CD, ");
            stb.append("  JVSRD.SUBCLASSCD, ");
            stb.append("  JVSRD.VIEWCD, ");
            stb.append("  MTBL.PATTERN_CD, ");
            stb.append("  CASE WHEN MTBL.CNT IN (3, 5) THEN '1' ELSE '0' END AS MANTENFLG, ");
            stb.append("  sum(CASE WHEN JVSRD.SCORE BETWEEN 95 AND 100 THEN 1 ELSE 0 END) AS RANGE1, ");
            stb.append("  sum(CASE WHEN JVSRD.SCORE BETWEEN 90 AND 94 THEN 1 ELSE 0 END) AS RANGE2, ");
            stb.append("  sum(CASE WHEN JVSRD.SCORE BETWEEN 85 AND 89 THEN 1 ELSE 0 END) AS RANGE3, ");
            stb.append("  sum(CASE WHEN JVSRD.SCORE BETWEEN 80 AND 84 THEN 1 ELSE 0 END) AS RANGE4, ");
            stb.append("  sum(CASE WHEN JVSRD.SCORE BETWEEN 75 AND 79 THEN 1 ELSE 0 END) AS RANGE5, ");
            stb.append("  sum(CASE WHEN JVSRD.SCORE BETWEEN 70 AND 74 THEN 1 ELSE 0 END) AS RANGE6, ");
            stb.append("  sum(CASE WHEN JVSRD.SCORE BETWEEN 65 AND 69 THEN 1 ELSE 0 END) AS RANGE7, ");
            stb.append("  sum(CASE WHEN JVSRD.SCORE BETWEEN 60 AND 64 THEN 1 ELSE 0 END) AS RANGE8, ");
            stb.append("  sum(CASE WHEN JVSRD.SCORE BETWEEN 55 AND 59 THEN 1 ELSE 0 END) AS RANGE9, ");
            stb.append("  sum(CASE WHEN JVSRD.SCORE BETWEEN 50 AND 54 THEN 1 ELSE 0 END) AS RANGE10, ");
            stb.append("  sum(CASE WHEN JVSRD.SCORE BETWEEN 0 AND 49 THEN 1 ELSE 0 END) AS RANGE11 ");
            stb.append(" FROM ");
            stb.append("  JVIEWSTAT_RECORD_DAT JVSRD ");
            stb.append("  LEFT JOIN JVIEWSTAT_SUBCLASS_PATTERN_DAT JVSSPD ");
            stb.append("     ON JVSSPD.YEAR = JVSRD.YEAR ");
            stb.append("    AND JVSSPD.GRADE = '" + param._grade + "' ");
            stb.append("    AND JVSSPD.CLASSCD = JVSRD.CLASSCD ");
            stb.append("    AND JVSSPD.SCHOOL_KIND = JVSRD.SCHOOL_KIND ");
            stb.append("    AND JVSSPD.CURRICULUM_CD = JVSRD.CURRICULUM_CD ");
            stb.append("    AND JVSSPD.SUBCLASSCD = JVSRD.SUBCLASSCD ");
            stb.append("    AND JVSSPD.VIEWCD = JVSRD.VIEWCD ");
            stb.append("  LEFT JOIN JVIEWSTAT_LEVEL_PATTERN_DAT JVSLPD ");
            stb.append("     ON JVSLPD.YEAR = JVSRD.YEAR ");
            stb.append("    AND JVSLPD.SCHOOL_KIND = JVSRD.SCHOOL_KIND ");
            stb.append("    AND JVSLPD.PATTERN_CD = JVSSPD.PATTERN_CD ");
            stb.append("    AND JVSRD.SCORE BETWEEN JVSLPD.ASSESSLOW AND JVSLPD.ASSESSHIGH ");
            stb.append("  LEFT JOIN MANTENTBL MTBL ");
            stb.append("     ON MTBL.YEAR = JVSLPD.YEAR ");
            stb.append("    AND MTBL.SCHOOL_KIND = JVSLPD.SCHOOL_KIND ");
            stb.append("    AND MTBL.PATTERN_CD = JVSLPD.PATTERN_CD ");
            stb.append("  LEFT JOIN SCHREG_REGD_DAT SRD ");
            stb.append("     ON SRD.SCHREGNO = JVSRD.SCHREGNO ");
            stb.append("    AND SRD.YEAR = JVSRD.YEAR ");
            if (SEMEALL.equals(param._semester)) {
                stb.append("     AND SRD.SEMESTER = '" + param._ctrlSemester + "' ");
            } else {
                stb.append("     AND SRD.SEMESTER = '" + param._semester + "' ");
            }
            stb.append(" WHERE ");
            stb.append("  JVSRD.YEAR = '" + param._year + "' ");
            stb.append("  AND JVSRD.SEMESTER = '" + param._semester + "' ");
            stb.append("  AND SRD.GRADE = '" + param._grade + "' ");
            stb.append(" GROUP BY ");
            stb.append("  JVSRD.CLASSCD, ");
            stb.append("  JVSRD.SCHOOL_KIND, ");
            stb.append("  JVSRD.CURRICULUM_CD, ");
            stb.append("  JVSRD.SUBCLASSCD, ");
            stb.append("  JVSRD.VIEWCD, ");
            stb.append("  JVSSPD.PATTERN_CD, ");
            stb.append("  MTBL.PATTERN_CD, ");
            stb.append("  MTBL.CNT ");
            stb.append(" ORDER BY ");
            stb.append("  JVSRD.CLASSCD, JVSRD.SCHOOL_KIND, JVSRD.CURRICULUM_CD, JVSRD.SUBCLASSCD, JVSRD.VIEWCD, MTBL.PATTERN_CD ");

            final String sql = stb.toString();
            log.debug("sql = ");
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            param._distributionDat.clear();
            while (rs.next()) {
                param._distributionDat.add(new DistriDat(rs.getString("CLASSCD"), rs.getString("SCHOOL_KIND"), rs.getString("CURRICULUM_CD"),
            	                                          rs.getString("SUBCLASSCD"), rs.getString("VIEWCD"), rs.getString("PATTERN_CD"),
                                                          rs.getString("MANTENFLG"),
                                                          rs.getString("RANGE1"), rs.getString("RANGE2"), rs.getString("RANGE3"),
                                                          rs.getString("RANGE4"), rs.getString("RANGE5"), rs.getString("RANGE6"),
                                                          rs.getString("RANGE7"), rs.getString("RANGE8"), rs.getString("RANGE9"),
                                                          rs.getString("RANGE10"), rs.getString("RANGE11") ));
            }
        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
        	DbUtils.closeQuietly(null, ps, rs);
        	db2.commit();
        }
    }

    /**
     * 生徒
     */
    private static class Student {
        final String _schregno;
        String _name;
        final String _gradeCd;
        final String _gradeName1;
        final String _hrName;
        final String _grade;
        final String _attendno;
        final String _hrClassName1;
        final String _entyear;

        final List<RecDat> _recordDat = new ArrayList();       //RecDat
        final List<EvalDat> _evaluationDat = new ArrayList();   //EvalDat
        final List<ViewClass> _viewClassList = new ArrayList();   //ViewClass

        Student(final String schregno, final String gradeCd, final String gradeName1, final String hrName, final String staffName, final String attendno, final String grade, final String coursecd, final String majorcd, final String course, final String majorname, final String hrClassName1, final String entyear) {
            _schregno = schregno;
            _gradeCd = gradeCd;
            _gradeName1 = gradeName1;
            _hrName = hrName;
            _attendno = attendno;
            _grade = grade;
            _hrClassName1 = hrClassName1;
            _entyear = entyear;
        }

        /**
         * 生徒を取得
         */
        private static List getStudentList(final DB2UDB db2, final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append("     SELECT  REGD.SCHREGNO");
            stb.append("            ,REGD.SEMESTER ");
            stb.append("            ,BASE.NAME ");
            stb.append("            ,BASE.REAL_NAME ");
            stb.append("            ,NAMESD.SCHREGNO AS NAME_SETUP ");
            stb.append("            ,REGDH.HR_NAME ");
            stb.append("            ,STFM.STAFFNAME ");
            stb.append("            ,REGD.ATTENDNO ");
            stb.append("            ,REGD.GRADE ");
            stb.append("            ,REGD.COURSECD ");
            stb.append("            ,REGD.MAJORCD ");
            stb.append("            ,REGD.COURSECD || REGD.MAJORCD || REGD.COURSECODE AS COURSE");
            stb.append("            ,W9.MAJORNAME ");
            stb.append("            ,REGDG.GRADE_CD ");
            stb.append("            ,REGDG.GRADE_NAME1 ");
            stb.append("            ,REGDH.HR_CLASS_NAME1 ");
            stb.append("            ,FISCALYEAR(BASE.ENT_DATE) AS ENT_YEAR ");
            stb.append("            ,CASE WHEN W3.SCHREGNO IS NOT NULL THEN 1 ");
            stb.append("                  WHEN W4.SCHREGNO IS NOT NULL THEN 1 ");
            stb.append("                  WHEN TRANSF.SCHREGNO IS NOT NULL THEN 1 ");
            stb.append("                  ELSE 0 END AS LEAVE ");
            stb.append("            ,BASE.BIRTHDAY ");
            stb.append("     FROM    SCHREG_REGD_DAT REGD ");
            stb.append("     INNER JOIN V_SEMESTER_GRADE_MST W2 ON W2.YEAR = '" + param._year + "' AND W2.SEMESTER = REGD.SEMESTER AND W2.GRADE = '" + param._grade + "' ");
            //               転学・退学者で、異動日が[学期終了日または出欠集計日の小さい日付]より小さい日付の場合
            stb.append("     LEFT JOIN SCHREG_BASE_MST W3 ON W3.SCHREGNO = REGD.SCHREGNO ");
            stb.append("                  AND W3.GRD_DIV IN('2','3') ");
            stb.append("                  AND W3.GRD_DATE < CASE WHEN W2.EDATE < '" + param._date + "' THEN W2.EDATE ELSE '" + param._date + "' END ");
            //               転入・編入者で、異動日が[学期終了日または出欠集計日の小さい日付]より大きい日付の場合
            stb.append("     LEFT JOIN SCHREG_BASE_MST W4 ON W4.SCHREGNO = REGD.SCHREGNO ");
            stb.append("                  AND W4.ENT_DIV IN('4','5') ");
            stb.append("                  AND W4.ENT_DATE > CASE WHEN W2.EDATE < '" + param._date + "' THEN W2.EDATE ELSE '" + param._date + "' END ");
            //               留学・休学者で、[学期終了日または出欠集計日の小さい日付]が留学・休学期間内にある場合
            stb.append("     LEFT JOIN SCHREG_TRANSFER_DAT TRANSF ON TRANSF.SCHREGNO = REGD.SCHREGNO ");
            stb.append("                  AND TRANSF.TRANSFERCD IN ('1','2') ");
            stb.append("                  AND CASE WHEN W2.EDATE < '" + param._date + "' THEN W2.EDATE ELSE '" + param._date + "' END BETWEEN TRANSF.TRANSFER_SDATE AND TRANSF.TRANSFER_EDATE ");
            stb.append("     LEFT JOIN SCHREG_REGD_HDAT REGDH ON REGDH.YEAR = REGD.YEAR ");
            stb.append("                  AND REGDH.SEMESTER = REGD.SEMESTER ");
            stb.append("                  AND REGDH.GRADE = REGD.GRADE ");
            stb.append("                  AND REGDH.HR_CLASS = REGD.HR_CLASS ");
            stb.append("     LEFT JOIN SCHREG_REGD_GDAT REGDG ON REGDG.YEAR = REGD.YEAR AND REGDG.GRADE = REGD.GRADE ");
            stb.append("     LEFT JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ");
            stb.append("     LEFT JOIN SCHREG_NAME_SETUP_DAT NAMESD ON NAMESD.SCHREGNO = REGD.SCHREGNO AND NAMESD.DIV = '03' ");
            stb.append("     LEFT JOIN STAFF_MST STFM ON STFM.STAFFCD = REGDH.TR_CD1 ");
            stb.append("     LEFT JOIN MAJOR_MST W9 ON W9.COURSECD = REGD.COURSECD ");
            stb.append("                  AND W9.MAJORCD = REGD.MAJORCD ");
            stb.append("     WHERE   REGD.YEAR = '" + param._year + "' ");
            if (SEMEALL.equals(param._semester)) {
                stb.append("     AND REGD.SEMESTER = '" + param._ctrlSemester + "' ");
            } else {
                stb.append("     AND REGD.SEMESTER = '" + param._semester + "' ");
                stb.append("     AND NOT EXISTS(SELECT 'X'  FROM  SCHREG_BASE_MST S1");
                stb.append("                    WHERE S1.SCHREGNO = REGD.SCHREGNO AND S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < W2.SDATE) ");
            }
            stb.append("         AND REGD.GRADE || REGD.HR_CLASS IN " + whereIn(true, param._categorySelected) + " ");
            stb.append("     ORDER BY ");
            stb.append("         REGD.GRADE ");
            stb.append("       , REGD.HR_CLASS ");
            stb.append("       , REGD.ATTENDNO ");
            final String sql = stb.toString();
            if (param._isOutputDebug) {
            	log.info(" student sql = " + sql);
            }

            final List<Student> students = new ArrayList();

            for (final Iterator it = KnjDbUtils.query(db2, sql).iterator(); it.hasNext();) {
            	final Map row = (Map) it.next();
                final String attendno = NumberUtils.isDigits(KnjDbUtils.getString(row, "ATTENDNO")) ? String.valueOf(Integer.parseInt(KnjDbUtils.getString(row, "ATTENDNO"))) : KnjDbUtils.getString(row, "ATTENDNO");
                final String staffname = StringUtils.defaultString(KnjDbUtils.getString(row, "STAFFNAME"));
                final Student student = new Student(KnjDbUtils.getString(row, "SCHREGNO"), KnjDbUtils.getString(row, "GRADE_CD"), KnjDbUtils.getString(row, "GRADE_NAME1"), KnjDbUtils.getString(row, "HR_NAME"), staffname, attendno, KnjDbUtils.getString(row, "GRADE"), KnjDbUtils.getString(row, "COURSECD"), KnjDbUtils.getString(row, "MAJORCD"), KnjDbUtils.getString(row, "COURSE"), KnjDbUtils.getString(row, "MAJORNAME"), KnjDbUtils.getString(row, "HR_CLASS_NAME1"), KnjDbUtils.getString(row, "ENT_YEAR"));
                student._name = null != KnjDbUtils.getString(row, "NAME_SETUP") ? KnjDbUtils.getString(row, "REAL_NAME") : KnjDbUtils.getString(row, "NAME");
                students.add(student);
            }
            return students;
        }

        /**
         * 文字列の配列を、SQL文where節のin句で使える文字列に変換する。
         * 例:<br/>
         * <pre>
         * whereIn(*, null)                         = null
         * whereIn(*, [])                           = null
         * whereIn(false, [null])                   = "(null)"
         * whereIn(true, [null])                    = null
         * whereIn(*, ["can't"])                    = "('can''t')"
         * whereIn(*, ["abc", "don't"])             = "('abc', 'don''t')"
         * whereIn(false, ["abc", null, "xyz"])     = "('abc', null, 'xyz')"
         * whereIn(true, ["abc", null, "xyz"])      = "('abc', 'xyz')"
         * </pre>
         * @param skipNull nullをスキップするか否か。<code>true</code>ならスキップする
         * @param array 文字列の配列
         * @return 変換後の文字列
         */
        public static String whereIn(final boolean skipNull, final String[] array) {
            if (null == array || 0 == array.length) {
                return null;
            }

            final StringBuffer sb = new StringBuffer();
            int n = 0;
            for (int i = 0; i < array.length; i++) {
                if (null == array[i] && skipNull) {
                    continue;
                }

                if (0 == n) { sb.append("("); }
                if (0 != n) { sb.append(", "); }

                if (null == array[i]) {
                    sb.append(String.valueOf(array[i])); // "null"
                } else {
                    sb.append('\'');
                    sb.append(StringEscapeUtils.escapeSql(array[i]));
                    sb.append('\'');
                }
                //--
                n++;
            }

            if (0 == n) {
                return null;
            }

            sb.append(")");
            return sb.toString();
        }

        private static List<String> getSchregnoList(final List<Student> studentList) {
        	List<String> retlist = new ArrayList();
        	for (int ii = 0;ii < studentList.size();ii++) {
        		Student wkptr = studentList.get(ii);
        		retlist.add(wkptr._schregno);
        	}
        	return retlist;
        }

        public static void setRecordDat(final DB2UDB db2, final Param param, final List<Student> studentList) {
            try {
                final StringBuffer stb = new StringBuffer();
                //得点の出力には、100点満点の得点と、そうではない評価的な出力(良い/普通や、A/B/C等)がある。
                //100点満点5段階についてはJVIEWSTAT_RECORD_DAT.SCOREを、それ以外のデータについてはJVIEWSTAT_LEVEL_PATTERN_DAT.ASSESS_SHOW1を成績とする。
                //まず、100点満点のデータコードを抽出するために、グループ化(集計)する。
                stb.append(" WITH DATCNT AS ( ");
                stb.append(" select ");
                stb.append("  YEAR, SCHOOL_KIND, PATTERN_CD,COUNT(*) AS CNT, MIN(ASSESSLOW) AS ASSESMIN, MAX(ASSESSHIGH) AS ASSESMAX ");
                stb.append(" from ");
                stb.append("  JVIEWSTAT_LEVEL_PATTERN_DAT ");
                stb.append(" GROUP BY ");
                stb.append("  YEAR, SCHOOL_KIND, PATTERN_CD ");
                //上記から、100点満点のデータコードを抽出する①
                stb.append(" ), MANTENTBL AS ( ");
                stb.append(" SELECT ");
                stb.append("  * ");
                stb.append(" FROM ");
                stb.append("  DATCNT ");
                //学年平均を抽出する②(※この時点では"※"は設定していない。SQLではなく、処理側で設定する)
                stb.append(" ), GRADEAVG AS ( ");
                stb.append(" select ");
                stb.append("  JVSRD.CLASSCD, ");
                stb.append("  JVSRD.SCHOOL_KIND, ");
                stb.append("  JVSRD.CURRICULUM_CD, ");
                stb.append("  JVSRD.SUBCLASSCD, ");
                stb.append("  JVSRD.VIEWCD, ");
                stb.append("  MTBL.PATTERN_CD, ");
                stb.append("  CASE WHEN MTBL.ASSESMIN = 0 AND MTBL.ASSESMAX = 100 AND MTBL.CNT IN (3, 5) THEN CHAR(DECIMAL(ROUND(AVG(DOUBLE(JVSRD.SCORE)), 1), 5, 1)) ELSE '' END AS SHOWDAT ");
                stb.append(" FROM ");
                stb.append("  JVIEWSTAT_RECORD_DAT JVSRD ");
                stb.append("  INNER JOIN JVIEWNAME_GRADE_MST JVNGM ON JVNGM.GRADE = '" + param._grade +"'  ");
                stb.append("      AND JVNGM.CLASSCD = JVSRD.CLASSCD ");
                stb.append("      AND JVNGM.SCHOOL_KIND = JVSRD.SCHOOL_KIND ");
                stb.append("      AND JVNGM.CURRICULUM_CD = JVSRD.CURRICULUM_CD ");
                stb.append("      AND JVNGM.SUBCLASSCD = JVSRD.SUBCLASSCD ");
                stb.append("      AND JVNGM.VIEWCD = JVSRD.VIEWCD ");
                stb.append("  INNER JOIN SCHREG_REGD_DAT SRD ");
                stb.append("     ON SRD.YEAR = JVSRD.YEAR ");
                if (SEMEALL.equals(param._semester)) {
                    stb.append("    AND SRD.SEMESTER = '" + param._ctrlSemester + "' ");
                } else {
                    stb.append("    AND SRD.SEMESTER = '" + param._semester + "' ");
                }
                stb.append("    AND SRD.SCHREGNO = JVSRD.SCHREGNO ");
                stb.append("    AND SRD.GRADE = '" + param._grade + "' ");
                stb.append("  LEFT JOIN JVIEWSTAT_SUBCLASS_PATTERN_DAT JVSSPD ");
                stb.append("     ON JVSSPD.YEAR = JVSRD.YEAR ");
                stb.append("    AND JVSSPD.GRADE = '" + param._grade + "' ");
                stb.append("    AND JVSSPD.CLASSCD = JVSRD.CLASSCD ");
                stb.append("    AND JVSSPD.SCHOOL_KIND = JVSRD.SCHOOL_KIND ");
                stb.append("    AND JVSSPD.CURRICULUM_CD = JVSRD.CURRICULUM_CD ");
                stb.append("    AND JVSSPD.SUBCLASSCD = JVSRD.SUBCLASSCD ");
                stb.append("    AND JVSSPD.VIEWCD = JVSRD.VIEWCD ");
                stb.append("  LEFT JOIN JVIEWSTAT_LEVEL_PATTERN_DAT JVSLPD ");
                stb.append("     ON JVSLPD.YEAR = JVSRD.YEAR ");
                stb.append("    AND JVSLPD.SCHOOL_KIND = JVSRD.SCHOOL_KIND ");
                stb.append("    AND JVSLPD.PATTERN_CD = JVSSPD.PATTERN_CD ");
                stb.append("    AND JVSRD.SCORE BETWEEN JVSLPD.ASSESSLOW AND JVSLPD.ASSESSHIGH ");
                stb.append("  LEFT JOIN MANTENTBL MTBL ");
                stb.append("     ON MTBL.YEAR = JVSLPD.YEAR ");
                stb.append("    AND MTBL.SCHOOL_KIND = JVSLPD.SCHOOL_KIND ");
                stb.append("    AND MTBL.PATTERN_CD = JVSLPD.PATTERN_CD ");
                stb.append(" WHERE ");
                stb.append("  JVSRD.YEAR = '" + param._year + "' ");
                stb.append("  AND JVSRD.SEMESTER = '" + param._semester + "' ");
                stb.append("  AND JVSRD.SCORE IS NOT NULL ");
                stb.append(" GROUP BY ");
                stb.append("  JVSRD.CLASSCD, ");
                stb.append("  JVSRD.SCHOOL_KIND, ");
                stb.append("  JVSRD.CURRICULUM_CD, ");
                stb.append("  JVSRD.SUBCLASSCD, ");
                stb.append("  JVSRD.VIEWCD, ");
                stb.append("  JVSSPD.PATTERN_CD, ");
                stb.append("  MTBL.PATTERN_CD, ");
                stb.append("  MTBL.ASSESMIN, ");
                stb.append("  MTBL.ASSESMAX, ");
                stb.append("  MTBL.CNT ");
                stb.append(" ORDER BY ");
                stb.append("  JVSRD.CLASSCD, JVSRD.SCHOOL_KIND, JVSRD.CURRICULUM_CD, JVSRD.SUBCLASSCD, JVSRD.VIEWCD, MTBL.PATTERN_CD ");
                stb.append(" ) ");
                //①、②を利用して、結果をまとめる
                stb.append(" select ");
                stb.append("  JVSRD.CLASSCD, ");
                stb.append("  JVSRD.SCHOOL_KIND, ");
                stb.append("  JVSRD.CURRICULUM_CD, ");
                stb.append("  JVSRD.SUBCLASSCD, ");
                stb.append("  JVSRD.VIEWCD, ");
                stb.append("  JVSSPD.PATTERN_CD, ");
                stb.append("  JVSRD.SCHREGNO, ");
                stb.append("  CASE WHEN MTBL.ASSESMIN = 0 AND MTBL.ASSESMAX = 100 AND MTBL.CNT IN (3, 5) THEN '1' ELSE '0' END AS MANTENFLG, ");
                stb.append("  CASE WHEN MTBL.ASSESMIN = 0 AND MTBL.ASSESMAX = 100 AND MTBL.CNT IN (3, 5) THEN RTRIM(CHAR(JVSRD.SCORE)) ELSE JVSLPD.ASSESS_SHOW1 END AS SHOWDAT, ");
                stb.append("  GAVG.SHOWDAT AS SHOWAVG, ");
                stb.append("  JVSRD.STATUS ");
                stb.append(" FROM ");
                stb.append("  JVIEWSTAT_RECORD_DAT JVSRD ");
                stb.append("  INNER JOIN JVIEWNAME_GRADE_MST JVNGM ON JVNGM.GRADE = '" + param._grade +"'  ");
                stb.append("      AND JVNGM.CLASSCD = JVSRD.CLASSCD ");
                stb.append("      AND JVNGM.SCHOOL_KIND = JVSRD.SCHOOL_KIND ");
                stb.append("      AND JVNGM.CURRICULUM_CD = JVSRD.CURRICULUM_CD ");
                stb.append("      AND JVNGM.SUBCLASSCD = JVSRD.SUBCLASSCD ");
                stb.append("      AND JVNGM.VIEWCD = JVSRD.VIEWCD ");
                stb.append("  LEFT JOIN SCHREG_REGD_DAT SRD ");
                stb.append("     ON SRD.YEAR = JVSRD.YEAR ");
                if (SEMEALL.equals(param._semester)) {
                    stb.append("     AND SRD.SEMESTER = '" + param._ctrlSemester + "' ");
                } else {
                    stb.append("     AND SRD.SEMESTER = '" + param._semester + "' ");
                }
                stb.append("    AND SRD.SCHREGNO = JVSRD.SCHREGNO ");
                stb.append("  LEFT JOIN JVIEWSTAT_SUBCLASS_PATTERN_DAT JVSSPD ");
                stb.append("     ON JVSSPD.YEAR = JVSRD.YEAR ");
                stb.append("    AND JVSSPD.GRADE = '" + param._grade + "' ");
                stb.append("    AND JVSSPD.CLASSCD = JVSRD.CLASSCD ");
                stb.append("    AND JVSSPD.SCHOOL_KIND = JVSRD.SCHOOL_KIND ");
                stb.append("    AND JVSSPD.CURRICULUM_CD = JVSRD.CURRICULUM_CD ");
                stb.append("    AND JVSSPD.SUBCLASSCD = JVSRD.SUBCLASSCD ");
                stb.append("    AND JVSSPD.VIEWCD = JVSRD.VIEWCD ");
                stb.append("  LEFT JOIN JVIEWSTAT_LEVEL_PATTERN_DAT JVSLPD ");
                stb.append("     ON JVSLPD.YEAR = JVSRD.YEAR ");
                stb.append("    AND JVSLPD.SCHOOL_KIND = JVSRD.SCHOOL_KIND ");
                stb.append("    AND JVSLPD.PATTERN_CD = JVSSPD.PATTERN_CD ");
                stb.append("    AND JVSRD.SCORE BETWEEN JVSLPD.ASSESSLOW AND JVSLPD.ASSESSHIGH ");
                stb.append("  LEFT JOIN MANTENTBL MTBL ");
                stb.append("     ON MTBL.YEAR = JVSLPD.YEAR ");
                stb.append("    AND MTBL.SCHOOL_KIND = JVSLPD.SCHOOL_KIND ");
                stb.append("    AND MTBL.PATTERN_CD = JVSLPD.PATTERN_CD ");
                stb.append("  LEFT JOIN GRADEAVG GAVG ");
                stb.append("     ON GAVG.CLASSCD = JVSRD.CLASSCD ");
                stb.append("    AND GAVG.SCHOOL_KIND = JVSRD.SCHOOL_KIND ");
                stb.append("    AND GAVG.CURRICULUM_CD = JVSRD.CURRICULUM_CD ");
                stb.append("    AND GAVG.SUBCLASSCD = JVSRD.SUBCLASSCD ");
                stb.append("    AND GAVG.VIEWCD = JVSRD.VIEWCD ");
                stb.append("    AND GAVG.PATTERN_CD = JVSSPD.PATTERN_CD ");
                stb.append(" WHERE ");
                stb.append("  JVSRD.YEAR = '" + param._year + "' ");
                stb.append("  AND JVSRD.SEMESTER = '" + param._semester + "' ");
                stb.append("  AND SRD.GRADE = '" + param._grade + "' ");
                List wklist = getSchregnoList(studentList);
                String[] wkstrarry = (String[])wklist.toArray(new String[wklist.size()]);
                stb.append("  AND SRD.SCHREGNO IN " + whereIn(true, wkstrarry) + " ");
                stb.append(" ORDER BY ");
                stb.append("  JVSRD.CLASSCD, ");
                stb.append("  JVSRD.SCHOOL_KIND, ");
                stb.append("  JVSRD.CURRICULUM_CD, ");
                stb.append("  JVSRD.SUBCLASSCD, ");
                stb.append("  JVSRD.VIEWCD, ");
                stb.append("  JVSSPD.PATTERN_CD ");

                final Map<String, Student> studentMap = new HashMap();
                for (final Student student : studentList) {
                	studentMap.put(student._schregno, student);
                }

                final String sql = stb.toString();
                log.info(" record sql = " + sql);
                for (final Iterator it = KnjDbUtils.query(db2, sql).iterator(); it.hasNext();) {
                	final Map row = (Map) it.next();
                	final Student studentPtr = studentMap.get(KnjDbUtils.getString(row, "SCHREGNO"));
                	if (studentPtr != null) {
                		studentPtr._recordDat.add(new RecDat(KnjDbUtils.getString(row, "CLASSCD"), KnjDbUtils.getString(row, "SCHOOL_KIND"), KnjDbUtils.getString(row, "CURRICULUM_CD"),
                				                             KnjDbUtils.getString(row, "SUBCLASSCD"), KnjDbUtils.getString(row, "VIEWCD"), KnjDbUtils.getString(row, "PATTERN_CD"),
                				                             KnjDbUtils.getString(row, "SCHREGNO"), KnjDbUtils.getString(row, "MANTENFLG"), KnjDbUtils.getString(row, "SHOWDAT"),
                				                             KnjDbUtils.getString(row, "SHOWAVG"), KnjDbUtils.getString(row, "STATUS")));
                	}
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
        }

        public static void setEvaluationDat(final DB2UDB db2, final Param param, final List studentList) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final Map studentMap = new HashMap();
                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                	final Student student = (Student) it.next();
                	studentMap.put(student._schregno, student);
                }

                final String sql = getEvaluationSql(param);
                log.debug("evalsql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                	final Student studentPtr = (Student) studentMap.get(rs.getString("SCHREGNO"));
                	if (studentPtr != null) {
                		studentPtr._evaluationDat.add(new EvalDat(rs.getString("YEAR"), rs.getString("CLASSCD"), rs.getString("CLASSNAME"), rs.getString("SCHOOL_KIND")
                				                                   ,rs.getString("CURRICULUM_CD"), rs.getString("SUBCLASSCD"), rs.getString("SCHREGNO")
                				                                   ,rs.getString("SCORE")));
                	}
                }

            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(ps);
            }
        }

        public static List<EvalTtl> setEvaluationTitle(final DB2UDB db2, final Param param) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            List<EvalTtl> retList = new ArrayList();
            try {
                final String sql = getEvaluationTitleSql(param);
                log.debug("evalttlsql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                	retList.add(new EvalTtl(rs.getString("CLASSCD"), rs.getString("CLASSNAME"), rs.getString("SCHOOL_KIND")));
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(ps);
            }
            return retList;
        }
        private static String getEvaluationTitleSql(Param param) {
        	StringBuffer stb = new StringBuffer();
            //評価で出力する教科名称を取得(CLASSCD='99'を独自に設定)
        	stb.append(" WITH JVIEW_SEME AS ( ");
        	stb.append(" SELECT ");
        	stb.append("     T1.GRADE, ");
        	stb.append("     T1.CLASSCD, ");
        	stb.append("     T1.SCHOOL_KIND, ");
        	stb.append("     T1.CURRICULUM_CD, ");
        	stb.append("     T1.SUBCLASSCD, ");
        	stb.append("     T1.VIEWCD, ");
        	stb.append("     SEME.SEMESTER ");
        	stb.append(" FROM ");
        	stb.append("     JVIEWNAME_GRADE_MST T1 ");
        	stb.append("     INNER JOIN JVIEWNAME_GRADE_YDAT T2 ON T2.YEAR = '" + param._year + "' ");
        	stb.append("         AND T2.GRADE = T1.GRADE ");
        	stb.append("         AND T2.CLASSCD = T1.CLASSCD  ");
        	stb.append("         AND T2.SCHOOL_KIND = T1.SCHOOL_KIND  ");
        	stb.append("         AND T2.CURRICULUM_CD = T1.CURRICULUM_CD  ");
        	stb.append("         AND T2.SUBCLASSCD = T1.SUBCLASSCD  ");
        	stb.append("         AND T2.VIEWCD = T1.VIEWCD ");
        	stb.append("     INNER JOIN SEMESTER_MST SEME ON SEME.YEAR = '" + param._year + "' ");
        	stb.append(" WHERE ");
        	stb.append("     T1.GRADE = '" + param._grade + "' ");
        	stb.append(" ) ");
//            //まず、100点満点の5段階のデータコードを抽出するために、グループ化(集計)する。
//            stb.append(" , DATCNT AS ( ");
//            stb.append(" select ");
//            stb.append("  YEAR, SCHOOL_KIND, PATTERN_CD,COUNT(*) AS CNT, MIN(ASSESSLOW) AS ASSESMIN, MAX(ASSESSHIGH) AS ASSESMAX ");
//            stb.append(" from ");
//            stb.append("  JVIEWSTAT_LEVEL_PATTERN_DAT ");
//            stb.append(" GROUP BY ");
//            stb.append("  YEAR, SCHOOL_KIND, PATTERN_CD ");
////            //上記から、100点満点の5段階のデータコードを抽出する①
//            stb.append(" ), MANTENTBL AS ( ");
//            stb.append(" SELECT ");
//            stb.append("  * ");
//            stb.append(" FROM ");
//            stb.append("  DATCNT ");
////            stb.append(" WHERE ");
////            stb.append("  CNT >= 5 AND ASSESMIN = 0 AND ASSESMAX = 100 ");
//        	stb.append(" ) ");
        	stb.append(" , JVIEWDAT AS ( ");
        	stb.append(" SELECT ");
        	stb.append("    CLM.CLASSCD ");
        	stb.append("  , VALUE(CLM.CLASSORDERNAME2, CLM.CLASSNAME) AS CLASSNAME ");
        	stb.append("  , CLM.SCHOOL_KIND ");
        	stb.append("  , CLM.SHOWORDER3 ");
        	stb.append(" FROM ");
        	stb.append("     JVIEWNAME_GRADE_MST T1 ");
        	stb.append("     INNER JOIN JVIEWNAME_GRADE_YDAT T2 ON T2.YEAR = '" + param._year + "' ");
        	stb.append("         AND T2.GRADE = T1.GRADE ");
        	stb.append("         AND T2.CLASSCD = T1.CLASSCD  ");
        	stb.append("         AND T2.SCHOOL_KIND = T1.SCHOOL_KIND  ");
        	stb.append("         AND T2.CURRICULUM_CD = T1.CURRICULUM_CD  ");
        	stb.append("         AND T2.SUBCLASSCD = T1.SUBCLASSCD  ");
        	stb.append("         AND T2.VIEWCD = T1.VIEWCD ");
        	stb.append("     INNER JOIN CLASS_MST CLM ON CLM.CLASSCD = T1.CLASSCD ");
        	stb.append("         AND CLM.SCHOOL_KIND = T1.SCHOOL_KIND  ");
        	stb.append("     LEFT JOIN JVIEW_SEME L1 ON L1.CLASSCD = T1.CLASSCD ");
        	stb.append("         AND L1.SCHOOL_KIND = T1.SCHOOL_KIND ");
        	stb.append("         AND L1.CURRICULUM_CD = T1.CURRICULUM_CD ");
        	stb.append("         AND L1.SUBCLASSCD = T1.SUBCLASSCD ");
        	stb.append("         AND L1.VIEWCD = T1.VIEWCD ");
        	stb.append("         AND L1.GRADE = T1.GRADE ");
            stb.append("     INNER JOIN JVIEWSTAT_SUBCLASS_PATTERN_DAT JVSSPD ");
            stb.append("        ON JVSSPD.YEAR = T2.YEAR ");
            stb.append("         AND JVSSPD.GRADE = '" + param._grade + "' ");
            stb.append("         AND JVSSPD.CLASSCD = T1.CLASSCD ");
            stb.append("         AND JVSSPD.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("         AND JVSSPD.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("         AND JVSSPD.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("         AND JVSSPD.VIEWCD = T1.VIEWCD ");
            stb.append("     INNER JOIN JVIEWSTAT_LEVEL_PATTERN_DAT JVSLPD ");
            stb.append("        ON JVSLPD.YEAR = JVSSPD.YEAR ");
            stb.append("         AND JVSLPD.SCHOOL_KIND = JVSSPD.SCHOOL_KIND ");
            stb.append("         AND JVSLPD.PATTERN_CD = JVSSPD.PATTERN_CD ");
//            stb.append("     INNER JOIN MANTENTBL MTBL ");
//            stb.append("        ON MTBL.YEAR = JVSLPD.YEAR ");
//            stb.append("         AND MTBL.SCHOOL_KIND = JVSLPD.SCHOOL_KIND ");
//            stb.append("         AND MTBL.PATTERN_CD = JVSLPD.PATTERN_CD ");
            stb.append(" WHERE ");
        	stb.append("     T1.GRADE = '" + param._grade + "' ");
        	stb.append("     AND T1.SCHOOL_KIND = '" + param._schoolKind + "'  ");
        	stb.append("     AND L1.SEMESTER = '" + param._semester + "' ");
        	stb.append(" GROUP BY ");
            stb.append("   CLM.CLASSCD ");
        	stb.append("  , CLM.SCHOOL_KIND ");
            stb.append("   ,CLM.CLASSORDERNAME2 ");
            stb.append("   ,CLM.CLASSNAME ");
            stb.append("   ,CLM.SHOWORDER3 ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("   CLASSCD, ");
            stb.append("   CLASSNAME, ");
            stb.append("   SCHOOL_KIND, ");
            stb.append("   SHOWORDER3 ");
            stb.append(" FROM ");
            stb.append("   JVIEWDAT ");
        	stb.append(" ORDER BY ");
        	stb.append("   VALUE(SHOWORDER3, -1), ");
        	stb.append("   CLASSCD ");

        	return stb.toString();
        }

        private static String getEvaluationSql(Param param) {
        	StringBuffer stb = new StringBuffer();
        	String lastyear = String.valueOf(Integer.parseInt(param._year) - 1);
            //成績データを取得
        	stb.append(" SELECT ");
        	stb.append("  T1.YEAR, ");
        	stb.append("  T1.CLASSCD, ");
        	stb.append("  VALUE(CM.CLASSORDERNAME2, CM.CLASSNAME) AS CLASSNAME, ");
        	stb.append("  T1.SCHOOL_KIND, ");
        	stb.append("  T1.CURRICULUM_CD, ");
        	stb.append("  T1.SUBCLASSCD, ");
        	stb.append("  T1.SCHREGNO, ");
        	stb.append("  T1.SCORE ");
        	stb.append(" FROM ");
        	stb.append("  RECORD_SCORE_DAT T1 ");
        	stb.append("  LEFT JOIN CLASS_MST CM ");
        	stb.append("    ON CM.CLASSCD = T1.CLASSCD ");
        	stb.append("   AND CM.SCHOOL_KIND = T1.SCHOOL_KIND ");
        	stb.append("  LEFT JOIN SCHREG_REGD_DAT T2 ");
        	stb.append("    ON T2.YEAR = T1.YEAR ");
            if (SEMEALL.equals(param._semester)) {
                stb.append("     AND T2.SEMESTER = '" + param._ctrlSemester + "' ");
            } else {
                stb.append("     AND T2.SEMESTER = '" + param._semester + "' ");
            }
        	stb.append("   AND T2.SCHREGNO = T1.SCHREGNO ");
        	stb.append(" WHERE ");
        	stb.append("  T1.YEAR = '" + param._year + "' ");
        	stb.append("  AND T1.SEMESTER = '" + param._semester + "' ");
        	stb.append("  AND T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '990009' ");
        	stb.append("  AND T2.GRADE || T2.HR_CLASS IN "+ SQLUtils.whereIn(true, param._categorySelected) +" ");
        	if ("06".equals(param._gradeCd)) {
        	    stb.append(" UNION ");
            	stb.append(" SELECT ");
            	stb.append("  T1.YEAR, ");
            	stb.append("  T1.CLASSCD, ");
            	stb.append("  VALUE(CM.CLASSORDERNAME2, CM.CLASSNAME) AS CLASSNAME, ");
            	stb.append("  T1.SCHOOL_KIND, ");
            	stb.append("  T1.CURRICULUM_CD, ");
            	stb.append("  T1.SUBCLASSCD, ");
            	stb.append("  T1.SCHREGNO, ");
            	stb.append("  T1.VALUATION AS SCORE ");
            	stb.append(" FROM ");
            	stb.append("  SCHREG_STUDYREC_DAT T1 ");
            	stb.append("  LEFT JOIN CLASS_MST CM ");
            	stb.append("    ON CM.CLASSCD = T1.CLASSCD ");
            	stb.append("   AND CM.SCHOOL_KIND = T1.SCHOOL_KIND ");
            	stb.append(" WHERE ");
            	stb.append("  T1.YEAR = '" + lastyear + "' ");
            	stb.append("  AND T1.SCHREGNO IN (SELECT T2.SCHREGNO FROM SCHREG_REGD_DAT T2 ");
            	stb.append("              WHERE ");
            	stb.append("               T2.YEAR = '" + param._year + "' ");
                if (SEMEALL.equals(param._semester)) {
                    stb.append("     AND T2.SEMESTER = '" + param._ctrlSemester + "' ");
                } else {
                    stb.append("     AND T2.SEMESTER = '" + param._semester + "' ");
                }
            	stb.append("               AND T2.GRADE || T2.HR_CLASS IN " + SQLUtils.whereIn(true, param._categorySelected) + " ");
            	stb.append("     ) ");
        	}

        	return stb.toString();
        }
    }

    private static class EvalDat {
    	final String _year;
    	final String _classcd;
    	final String _classname;
    	final String _schoolkind;
    	final String _curriculumcd;
    	final String _subclasscd;
    	final String _schregno;
    	final String _score;
    	EvalDat(
                final String year,
                final String classcd,
                final String classname,
                final String schoolkind,
                final String curriculumcd,
                final String subclasscd,
                final String schregno,
                final String score
                ) {
            _year = year;
            _classcd = classcd;
            _classname = classname;
            _schoolkind = schoolkind;
            _curriculumcd = curriculumcd;
            _subclasscd = subclasscd;
            _schregno = schregno;
            _score = score;
    	}
    	String getSubClsCd() {
    		return _classcd + "-" + _schoolkind + "-" + _curriculumcd + "-" + _subclasscd;
    	}
    }

    private static class EvalTtl {
    	final String _classcd;
    	final String _classname;
    	final String _schoolkind;
    	EvalTtl(final String classcd, final String classname, final String schoolkind) {
    		_classcd = classcd;
    		_classname = classname;
    		_schoolkind = schoolkind;
    	}
    }
    private static class RecDat {
        final String _classcd;
        final String _schoolkind;
        final String _curriculumcd;
        final String _subclasscd;
        final String _viewcd;
        final String _patterncd;
        final String _schregno;
        final String _mantenflg;
        final String _showdat;
        final String _showavg;
        final String _status;

    	RecDat(final String classcd, final String schoolkind, final String curriculumcd, final String subclasscd,
               final String viewcd, final String patterncd, final String schregno, final String mantenflg,
               final String showdat, final String showavg, final String status) {
    		_classcd = classcd;
    		_schoolkind = schoolkind;
    		_curriculumcd = curriculumcd;
    		_subclasscd = subclasscd;
    		_viewcd = viewcd;
    		_patterncd = patterncd;
    		_schregno = schregno;
    		_mantenflg = mantenflg;
    		_showdat = showdat;
    		_showavg = showavg;
    		_status = status;

    	}
    	String getSubClsCd() {
    		return _classcd + "-" + _schoolkind + "-" + _curriculumcd + "-" + _subclasscd;
    	}
    	String getAvgStr() {
    		final String retstr;
    		if ("1".equals(_mantenflg)) {
    			retstr = _showavg;
    		} else if (StringUtils.isBlank(_showdat)) {
    			retstr = "";
    		} else {
    			retstr = "※";
    		}
    		return retstr;
    	}
    }

    private static class DistriDat {
        final String _classcd;
        final String _schoolkind;
        final String _curriculumcd;
        final String _subclasscd;
        final String _viewcd;
        final String _patterncd;
        final String _mantenflg;
        final String _range1;
        final String _range2;
        final String _range3;
        final String _range4;
        final String _range5;
        final String _range6;
        final String _range7;
        final String _range8;
        final String _range9;
        final String _range10;
        final String _range11;

        DistriDat(final String classcd, final String schoolkind, final String curriculumcd, final String subclasscd,
               final String viewcd, final String patterncd, final String mantenflg,
               final String range1, final String range2, final String range3, final String range4,
               final String range5, final String range6, final String range7, final String range8,
               final String range9, final String range10, final String range11) {
    		_classcd = classcd;
    		_schoolkind = schoolkind;
    		_curriculumcd = curriculumcd;
    		_subclasscd = subclasscd;
    		_viewcd = viewcd;
    		_patterncd = patterncd;
    		_mantenflg = mantenflg;
    		_range1 = range1;
    		_range2 = range2;
    		_range3 = range3;
    		_range4 = range4;
    		_range5 = range5;
    		_range6 = range6;
    		_range7 = range7;
    		_range8 = range8;
    		_range9 = range9;
    		_range10 = range10;
    		_range11 = range11;

    	}
    	String getSubClsCd() {
    		return _classcd + "-" + _schoolkind + "-" + _curriculumcd + "-" + _subclasscd;
    	}
    	String getRangeN(final int index) {
    		String retstr = "";
    		if (index == 1) {
    			retstr = _range1;
    		} else if (index == 2) {
    			retstr = _range2;
    		} else if (index == 3) {
    			retstr = _range3;
    		} else if (index == 4) {
    			retstr = _range4;
    		} else if (index == 5) {
    			retstr = _range5;
    		} else if (index == 6) {
    			retstr = _range6;
    		} else if (index == 7) {
    			retstr = _range7;
    		} else if (index == 8) {
    			retstr = _range8;
    		} else if (index == 9) {
    			retstr = _range9;
    		} else if (index == 10) {
    			retstr = _range10;
    		} else if (index == 11) {
    			retstr = _range11;
    		}
    		return retstr;
    	}
    }

    /**
     * 観点の教科
     */
    private static class ViewClass {
        final String _classcd;
        final String _subclasscd;
        final String _electDiv;
        final String _classname;
        final String _subclassname;
        final List _viewList;

        final Map _viewcdSemesterStatDatMap = new HashMap();

        ViewClass(
                final String classcd,
                final String subclasscd,
                final String electDiv,
                final String classname,
                final String subclassname) {
            _classcd = classcd;
            _subclasscd = subclasscd;
            _electDiv = electDiv;
            _classname = classname;
            _subclassname = subclassname;
            _viewList = new ArrayList();
        }

        public void addView(final String viewcd, final String viewname) {
            _viewList.add(new String[]{viewcd, viewname});
        }

        public boolean hasViewCd(final String viewcd) {
        	for (final Iterator it = _viewList.iterator(); it.hasNext();) {
        		final String[] arr = (String[]) it.next();
        		if (arr[0].equals(viewcd)) {
        			return true;
        		}
        	}
        	return false;
        }

        public String getViewCd(final int i) {
            return ((String[]) _viewList.get(i))[0];
        }

        public String getViewName(final int i) {
            String viewname = ((String[]) _viewList.get(i))[1];
            viewname = StringUtils.replace(viewname, " ", "");
            viewname = StringUtils.replace(viewname, "　", "");
            return viewname;
        }

        public int getViewSize() {
            return _viewList.size();
        }

        public String toString() {
        	return "ViewClass(" + _subclasscd + ", " + _classname + ")";
        }

        public static void setViewClassList(final DB2UDB db2, final Param param, final List studentList) {
            final String sql = getViewClassSql(param);
            if (param._isOutputDebug) {
            	log.info(" view class sql = " + sql);
            }

            PreparedStatement ps = null;
            try {
                ps = db2.prepareStatement(sql);

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                	final Student student = (Student) it.next();

                	student._viewClassList.clear();

                	for (final Iterator rit = KnjDbUtils.query(db2, ps, new Object[] {student._schregno}).iterator(); rit.hasNext();) {
                		final Map row = (Map) rit.next();

                		final String classcd = KnjDbUtils.getString(row, "CLASSCD");
                		final String subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");
                		final String viewcd = KnjDbUtils.getString(row, "VIEWCD");

                		ViewClass viewClass = null;
                		for (final Iterator<ViewClass> vit = student._viewClassList.iterator(); vit.hasNext();) {
                			final ViewClass viewClass0 = vit.next();
                			if (viewClass0._subclasscd.equals(subclasscd)) {
                				viewClass = viewClass0;
                				break;
                			}
                		}

                		if (null == viewClass) {
                			final String electDiv = KnjDbUtils.getString(row, "ELECTDIV");
                			final String classname = KnjDbUtils.getString(row, "CLASSNAME");
                			final String subclassname = KnjDbUtils.getString(row, "SUBCLASSNAME");

                			viewClass = new ViewClass(classcd, subclasscd, electDiv, classname, subclassname);
                			student._viewClassList.add(viewClass);
                		}

                		if (!viewClass.hasViewCd(viewcd)) {
                			final String viewname = KnjDbUtils.getString(row, "VIEWNAME");
                			viewClass.addView(viewcd, viewname);
                		}

                		final String semester = KnjDbUtils.getString(row, "SEMESTER");
                		if (null == semester) {
                			continue;
                		}
                		final Map stat = getMappedMap(getMappedMap(viewClass._viewcdSemesterStatDatMap, viewcd), semester);
                		stat.put(STATUS, KnjDbUtils.getString(row, STATUS));
                		stat.put(VIEWFLG, KnjDbUtils.getString(row, VIEWFLG));
                		String assess = KnjDbUtils.getString(row, ASSESS_SHOW1);
                		if (null == assess) {
                			if (Integer.parseInt(semester) <= Integer.parseInt(param._semester)) {
                				if ("1".equals(KnjDbUtils.getString(row, VIEWFLG))) {
                					assess = "※";
                				}
                			}
                		}
						stat.put(ASSESS_SHOW1, assess);
                	}
                }

            } catch (Exception e) {
            	log.error("exception!", e);
            } finally {
            	DbUtils.closeQuietly(ps);
            }
        }

        private static String getViewClassSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            //grade-semester-教科,科目等のコード のリストを作成する
//            //ここでは得点を取得しないので、100点満点については制限しない。
            stb.append(" WITH JVIEW_SEME AS ( ");
            stb.append(" SELECT ");
            stb.append("     T1.GRADE ");
            stb.append("   , T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD ");
            stb.append("   , T1.VIEWCD ");
            stb.append("   , SEME.SEMESTER ");
            stb.append(" FROM ");
            stb.append("     JVIEWNAME_GRADE_MST T1 ");
            stb.append("     INNER JOIN JVIEWNAME_GRADE_YDAT T2 ON T2.YEAR = '" + param._year + "' ");
            stb.append("         AND T2.GRADE = T1.GRADE ");
            stb.append("         AND T2.CLASSCD = T1.CLASSCD  ");
            stb.append("         AND T2.SCHOOL_KIND = T1.SCHOOL_KIND  ");
            stb.append("         AND T2.CURRICULUM_CD = T1.CURRICULUM_CD  ");
            stb.append("         AND T2.SUBCLASSCD = T1.SUBCLASSCD  ");
            stb.append("         AND T2.VIEWCD = T1.VIEWCD ");
            stb.append("     INNER JOIN SEMESTER_MST SEME ON SEME.YEAR = '" + param._year + "' ");
            stb.append("     WHERE ");
            stb.append("         T1.GRADE = '" + param._grade + "' ");
            stb.append(" ) ");
//            //100点満点の5段階のパターンコードを抽出するために、グループ化(集計)する。
//            stb.append(" , DATCNT AS ( ");
//            stb.append(" select ");
//            stb.append("  YEAR, SCHOOL_KIND, PATTERN_CD,COUNT(*) AS CNT, MIN(ASSESSLOW) AS ASSESMIN, MAX(ASSESSHIGH) AS ASSESMAX ");
//            stb.append(" from ");
//            stb.append("  JVIEWSTAT_LEVEL_PATTERN_DAT ");
//            stb.append(" GROUP BY ");
//            stb.append("  YEAR, SCHOOL_KIND, PATTERN_CD ");
//            //上記から、100点満点のパターンコードを抽出する
//            stb.append(" ) ");
//            stb.append(" , MANTENTBL AS ( ");
//            stb.append(" SELECT ");
//            stb.append("  * ");
//            stb.append(" FROM ");
//            stb.append("  DATCNT ");
//            stb.append(" WHERE ");
//            stb.append("  ASSESMIN = 0 AND ASSESMAX = 100 ");
//            //満点フラグを作成する(フラグが立つものだけを抽出し、後で存在チェックデータとして利用)
//            //満点パターン(5段階100点満点)のデータを抽出する。
//            stb.append(" ), MANTENFLGTBL AS ( ");
//            stb.append(" select ");
//            stb.append("  JVSSPD.CLASSCD, ");
//            stb.append("  JVSSPD.SCHOOL_KIND, ");
//            stb.append("  JVSSPD.CURRICULUM_CD, ");
//            stb.append("  JVSSPD.SUBCLASSCD, ");
//            stb.append("  CASE WHEN MTBL.PATTERN_CD IS NOT NULL THEN '1' ELSE '0' END AS SHOWDAT ");
//            stb.append(" FROM ");
//            stb.append("  JVIEWSTAT_SUBCLASS_PATTERN_DAT JVSSPD ");
//            stb.append("  LEFT JOIN JVIEWSTAT_LEVEL_PATTERN_DAT JVSLPD ");
//            stb.append("     ON JVSLPD.YEAR = JVSSPD.YEAR ");
//            stb.append("    AND JVSLPD.SCHOOL_KIND = JVSSPD.SCHOOL_KIND ");
//            stb.append("    AND JVSLPD.PATTERN_CD = JVSSPD.PATTERN_CD ");
//            stb.append("  LEFT JOIN MANTENTBL MTBL ");
//            stb.append("     ON MTBL.YEAR = JVSLPD.YEAR ");
//            stb.append("    AND MTBL.SCHOOL_KIND = JVSLPD.SCHOOL_KIND ");
//            stb.append("    AND MTBL.PATTERN_CD = JVSLPD.PATTERN_CD ");
//            stb.append(" WHERE ");
//            stb.append("  JVSSPD.YEAR = '" + param._year + "' ");
//            stb.append("  AND JVSSPD.GRADE = '" + param._grade + "' ");
//            stb.append("  AND MTBL.PATTERN_CD IS NOT NULL ");
//            stb.append(" GROUP BY ");
//            stb.append("  JVSSPD.CLASSCD, ");
//            stb.append("  JVSSPD.SCHOOL_KIND, ");
//            stb.append("  JVSSPD.CURRICULUM_CD, ");
//            stb.append("  JVSSPD.SUBCLASSCD, ");
//            stb.append("  MTBL.PATTERN_CD ");
//            stb.append(" ORDER BY ");
//            stb.append("  JVSSPD.CLASSCD, JVSSPD.SCHOOL_KIND, JVSSPD.CURRICULUM_CD, JVSSPD.SUBCLASSCD, MTBL.PATTERN_CD ");
//            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     T1.GRADE ");
            stb.append("   , CLM.CLASSCD ");
            stb.append("   , VALUE(CLM.CLASSORDERNAME2, CLM.CLASSNAME) AS CLASSNAME ");
            stb.append("   , VALUE(SCLM.SUBCLASSORDERNAME2, SCLM.SUBCLASSNAME) AS SUBCLASSNAME ");
            stb.append("   , T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD ");
            stb.append("   , VALUE(SCLM.ELECTDIV, '0') AS ELECTDIV ");
            stb.append("   , T1.VIEWCD ");
            stb.append("   , T1.VIEWNAME ");
            stb.append("   , L1.SEMESTER ");
            stb.append("   , REC.SCHREGNO ");
            stb.append("   , REC.STATUS ");
            stb.append("   , INP.VIEWFLG ");
            stb.append("   , NM_D029.NAME1 AS STATUS_NAME1 ");
            stb.append("   , CASE WHEN NM_D038.NAMECD2 IS NOT NULL THEN ");
            stb.append("         CASE REC.STATUS WHEN NM_D038.ABBV1 THEN NM_D038.NAMESPARE1 ");
            stb.append("                         WHEN NM_D038.ABBV2 THEN NM_D038.NAMESPARE2 ");
            stb.append("         END ");
            stb.append("     ELSE NM_D029.NAME1 ");
            stb.append("     END AS ASSESS_SHOW1 ");
            stb.append(" FROM ");
            stb.append("     JVIEWNAME_GRADE_MST T1 ");
            stb.append("     INNER JOIN JVIEWNAME_GRADE_YDAT T2 ON T2.YEAR = '" + param._year + "' ");
            stb.append("         AND T2.GRADE = T1.GRADE ");
            stb.append("         AND T2.CLASSCD = T1.CLASSCD  ");
            stb.append("         AND T2.SCHOOL_KIND = T1.SCHOOL_KIND  ");
            stb.append("         AND T2.CURRICULUM_CD = T1.CURRICULUM_CD  ");
            stb.append("         AND T2.SUBCLASSCD = T1.SUBCLASSCD  ");
            stb.append("         AND T2.VIEWCD = T1.VIEWCD ");
            stb.append("     INNER JOIN CLASS_MST CLM ON CLM.CLASSCD = T1.CLASSCD ");
            stb.append("         AND CLM.SCHOOL_KIND = T1.SCHOOL_KIND  ");
            stb.append("     LEFT JOIN SUBCLASS_MST SCLM ON SCLM.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("         AND SCLM.CLASSCD = T1.CLASSCD  ");
            stb.append("         AND SCLM.SCHOOL_KIND = T1.SCHOOL_KIND  ");
            stb.append("         AND SCLM.CURRICULUM_CD = T1.CURRICULUM_CD  ");
            stb.append("     LEFT JOIN JVIEW_SEME L1 ON L1.CLASSCD = T1.CLASSCD ");
            stb.append("         AND L1.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("         AND L1.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("         AND L1.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("         AND L1.VIEWCD = T1.VIEWCD ");
            stb.append("         AND L1.GRADE = T1.GRADE ");
            stb.append("     LEFT JOIN JVIEWSTAT_INPUTSEQ_DAT INP ON INP.YEAR = T2.YEAR ");
            stb.append("         AND INP.CLASSCD = T2.CLASSCD  ");
            stb.append("         AND INP.SCHOOL_KIND = T2.SCHOOL_KIND  ");
            stb.append("         AND INP.CURRICULUM_CD = T2.CURRICULUM_CD  ");
            stb.append("         AND INP.SUBCLASSCD = T2.SUBCLASSCD  ");
            stb.append("         AND INP.VIEWCD = T2.VIEWCD ");
            stb.append("         AND INP.SEMESTER = L1.SEMESTER ");
            stb.append("         AND INP.GRADE = T2.GRADE ");
            stb.append("     LEFT JOIN JVIEWSTAT_RECORD_DAT REC ON REC.SUBCLASSCD = T2.SUBCLASSCD ");
            stb.append("         AND REC.CLASSCD = T2.CLASSCD  ");
            stb.append("         AND REC.SCHOOL_KIND = T2.SCHOOL_KIND  ");
            stb.append("         AND REC.CURRICULUM_CD = T2.CURRICULUM_CD  ");
            stb.append("         AND REC.VIEWCD = T2.VIEWCD ");
            stb.append("         AND REC.YEAR = T2.YEAR ");
            stb.append("         AND REC.SEMESTER = L1.SEMESTER ");
            stb.append("         AND REC.SCHREGNO = ? ");
            if (param._is5nenOr6nen) {
                stb.append("     LEFT JOIN NAME_MST NM_D029 ON NM_D029.NAMECD1 = 'DP29' ");
                stb.append("         AND NM_D029.ABBV1 = REC.STATUS ");
            } else {
                stb.append("     LEFT JOIN NAME_MST NM_D029 ON NM_D029.NAMECD1 = 'D029' ");
                stb.append("         AND NM_D029.ABBV1 = REC.STATUS ");
            }
            stb.append("     LEFT JOIN NAME_MST NM_D038 ON NM_D038.NAMECD1 = 'D038' ");
            stb.append("         AND NM_D038.NAMECD2 = T1.VIEWCD ");
            stb.append(" WHERE ");
            stb.append("     T1.GRADE = '" + param._grade + "' ");
            if ("1".equals(param._use_prg_schoolkind)) {
                stb.append("     AND T1.SCHOOL_KIND = '" + param._schoolKind + "' ");
            } else if ("1".equals(param._useSchool_KindField) && !StringUtils.isBlank(param._schoolKind)) {
                stb.append("     AND T1.SCHOOL_KIND = '" + param._schoolKind + "' ");
            }
//            stb.append("     AND EXISTS(SELECT 'X' "); //満点パターンに存在するか、チェック
//            stb.append("                FROM MANTENFLGTBL MFLGTBL ");
//            stb.append("                WHERE ");
//            stb.append("                  MFLGTBL.CLASSCD = T1.CLASSCD");
//            stb.append("                  AND MFLGTBL.SCHOOL_KIND = T1.SCHOOL_KIND");
//            stb.append("                  AND MFLGTBL.CURRICULUM_CD = T1.CURRICULUM_CD");
//            stb.append("                  AND MFLGTBL.SUBCLASSCD = T1.SUBCLASSCD");
//            stb.append("                  AND MFLGTBL.SHOWDAT = '1' ");
//            stb.append("                ) ");
            stb.append("     AND T1.CLASSCD IN ('01', '02', '03', '04') "); // 固定
            stb.append(" ORDER BY ");
            stb.append("     VALUE(SCLM.ELECTDIV, '0'), ");
            stb.append("     VALUE(CLM.SHOWORDER3, -1), ");
            stb.append("     CLM.CLASSCD, ");
            stb.append("     VALUE(SCLM.SHOWORDER3, -1), ");
            stb.append("     SCLM.CLASSCD, ");
            stb.append("     SCLM.SCHOOL_KIND, ");
            stb.append("     SCLM.CURRICULUM_CD, ");
            stb.append("     SCLM.SUBCLASSCD, ");
            stb.append("     VALUE(T1.SHOWORDER, -1), ");
            stb.append("     T1.VIEWCD, ");
            stb.append("     L1.SEMESTER ");
            log.debug("sql = "+stb.toString());
            return stb.toString();
        }
    }

    protected Param createParam(final HttpServletRequest request, final DB2UDB db2) {
        log.fatal("$Revision: 76861 $ $Date: 2020-09-15 11:23:55 +0900 (火, 15 9 2020) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(request, db2);
        return param;
    }

    private static class Param {
        final String _year;
        final String _semester;
        final String _ctrlSemester;

        final String _grade;
        final String _gradeCd;
        final String _gradeName;
        final String[] _categorySelected;
        final String _date;
        final String _nendo;
        final Map _semesternameMap;

        final String _useSchool_KindField;
        final String _schoolKind;
        final String _use_prg_schoolkind;

        final boolean _isOutputDebug;
        final List<DistriDat> _distributionDat;
        final boolean _is5nenOr6nen;
        final boolean _is1nenOr2nen;

        Param(final HttpServletRequest request, final DB2UDB db2) {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
                _grade = request.getParameter("GRADE");
                _categorySelected = request.getParameterValues("CLASS_SELECTED");
                _date = StringUtils.replace(request.getParameter("CTRL_DATE"), "/", "-");
            _nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_year)) + "年度";

            _useSchool_KindField = request.getParameter("useSchool_KindField");
            _use_prg_schoolkind = request.getParameter("use_prg_schoolkind");
            _schoolKind = StringUtils.defaultString(getSchregRegdGdat(db2, "SCHOOL_KIND"));

            _isOutputDebug = "1".equals(KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJD659B' AND NAME = 'outputDebug' ")));
            _gradeCd = getSchregRegdGdat(db2, "GRADE_CD");
            _gradeName = getSchregRegdGdat(db2, "GRADE_NAME1");
            _semesternameMap = getSemesternameMap(db2);
            _distributionDat = new ArrayList();
            _is5nenOr6nen = "05".equals(_gradeCd) || "06".equals(_gradeCd);
            _is1nenOr2nen = "01".equals(_gradeCd) || "02".equals(_gradeCd);
        }

        private String getSchregRegdGdat(final DB2UDB db2, final String field) {
            final StringBuffer stb = new StringBuffer();
            stb.append("SELECT  ");
            stb.append("        " + field + " ");
            stb.append("FROM    SCHREG_REGD_GDAT T1 ");
            stb.append("WHERE   T1.YEAR = '" + _year + "' ");
            stb.append(    "AND T1.GRADE = '" + _grade + "' ");
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, stb.toString()));
        }

        private Map getSemesternameMap(final DB2UDB db2) {
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT * FROM V_SEMESTER_GRADE_MST WHERE YEAR = '" + _year + "' AND GRADE = '" + _grade + "' ");
            Map rtn = KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, sql.toString()), "SEMESTER", "SEMESTERNAME");
        	if ("2020".equals(_year)) {
        		rtn.put("1", "前期");
        		rtn.put("2", "後期");
        		rtn.put("3", "");
        		return rtn;
        	}
			return rtn;
        }
    }
}
