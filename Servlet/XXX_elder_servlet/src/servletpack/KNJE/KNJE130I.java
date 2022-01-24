// kanji=漢字
/*
 * $Id: 63330d627debdb7f825b5575716b155efa6c89c1 $
 */
package servletpack.KNJE;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 * 学校教育システム 賢者 [進路情報システム]  評定人数集計表
 */
public class KNJE130I {

    private static final Log log = LogFactory.getLog(KNJE130I.class);

    private static final String FROM_TO_MARK = "\uFF5E";

    private boolean _hasData;
    
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws ServletException, IOException {
        Vrw32alp svf = null;
        try {
            svf = new Vrw32alp();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
            svf.VrInit();
            svf.VrSetSpoolFileStream(response.getOutputStream());

            response.setContentType("application/pdf");

            outputPdf(svf, request);

        } catch (final Exception ex) {
            log.error("error! ", ex);
        } finally {
            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note" , "note");
                svf.VrEndPage();
            }
            svf.VrQuit();
        }
    }
    
    public void outputPdf(
            final Vrw32alp svf,
            final HttpServletRequest request
    ) throws Exception {
        DB2UDB db2 = null;
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            // パラメータの取得
            Param param = createParam(request, db2);

            printMain(db2, svf, param);

        } catch (final Exception ex) {
            log.error("error! ", ex);
        } finally {
            try {
                db2.commit();
                db2.close();
            } catch (Exception ex) {
                log.error("db close error!", ex);
            }
        }
    }
    
    private void printMain(final DB2UDB db2, final Vrw32alp svf, final Param param) {
        
        final List<String> yearList = new ArrayList();
        for (int i = 0; i < 6; i++) { // 過去6年度分
            yearList.add(String.valueOf(Integer.parseInt(param._year) - i));
        }
        Map<String, List<SubclassMst>> yearSubclassListMap = new TreeMap<String, List<SubclassMst>>();
        Map<String, Map<SubclassMst, RecordRankSubclass>> yearSubclassMapMap = new TreeMap<String, Map<SubclassMst, RecordRankSubclass>>();
        Map<String, Map<String, Map<String, RecordAverageDat>>> yearAvgMap = new TreeMap<String, Map<String, Map<String, RecordAverageDat>>>();
        for (int ri = 0; ri < yearList.size(); ri++) {
            final String year = yearList.get(ri);
            
            final Map subclassMap = RecordRankSubclass.getRecordRankSubclassMap(db2, param, year, param._testcd);
            final Map<String, Map<String, RecordAverageDat>> avgMap = RecordAverageDat.getRecordAverageDatMap(db2, param, year, param._testcd);
            final List subclassList = new ArrayList(subclassMap.keySet());
            for (final Iterator it = subclassList.iterator(); it.hasNext();) {
                final SubclassMst mst = (SubclassMst) it.next();
                boolean remove = false;
                if (mst._isSaki && !param._isPrintSakiKamoku) {
                    if (param._isOutputDebug) {
                        log.info(" not print saki: " + mst);
                    }
                    remove = true;
                } else if (mst._isMoto && param._isNoPrintMoto) {
                    if (param._isOutputDebug) {
                        log.info(" not print moto: " + mst);
                    }
                    remove = true;
                } else if (param._d026List.contains(mst._subclasscd)) {
                    if (param._isOutputDebug) {
                        log.info(" not print d026: " + mst);
                    }
                    remove = true;
                }
                if (remove) {
                    it.remove();
                }
                
            }
            Collections.sort(subclassList);
            yearSubclassListMap.put(year, subclassList);
            yearSubclassMapMap.put(year, subclassMap);
            yearAvgMap.put(year, avgMap);
        }

        final int maxCol = 20;
        int pageMax = 0;
        for (int ri = 0; ri < yearList.size(); ri++) {
            final String year = yearList.get(ri);
            final List<SubclassMst> subclassList = yearSubclassListMap.get(year);
            pageMax = Math.max(pageMax, subclassList.size() / maxCol + (subclassList.size() % maxCol == 0 ? 0 : 1));
        }

        
        for (int pi = 0; pi < pageMax; pi++) {

            final String form = "KNJE130I.frm";
            svf.VrSetForm(form, 4);
            
            svf.VrsOut("TITLE", "評定人数集計表"); // タイトル
            svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(db2, param._loginDate)); // 日付
            svf.VrsOut("TESTNAME", StringUtils.defaultString(param._gradeName1) + " " + StringUtils.defaultString(param._testitemname) + " ６年間比較 " + ("1".equals(param._outputDiv) ? "％" : "人数")); // 考査名
            //svf.VrsOut("TIME", null); // 時間

            for (int ri = 0; ri < yearList.size(); ri++) {
                final String year = yearList.get(ri);
                
                final List<SubclassMst> subclassList = yearSubclassListMap.get(year);
                final Map<SubclassMst, RecordRankSubclass> subclassMap = yearSubclassMapMap.get(year);
                final Map<String, Map<String, RecordAverageDat>> avgMap = yearAvgMap.get(year);

                svf.VrsOut("YEAR_GRADE", spaceIfGradeName1Start(year, param._gradeName1)); // 年度・学年

                if (pi * maxCol < subclassList.size()) {
                	final List<SubclassMst> pageSubclassList = subclassList.subList(pi * maxCol, Math.min((pi + 1) * maxCol, subclassList.size()));
                	for (int j = 0; j < pageSubclassList.size(); j++) {
                		final SubclassMst mst = (SubclassMst) pageSubclassList.get(j);
                		final int line = j + 1;
                		if (param._isOutputDebug) {
                			log.info(" subclass " + j + " = " + mst);
                		}
                		
                		svf.VrsOutn("CLASSNAME", line, StringUtils.defaultString(mst._subclassabbv, mst._subclassname)); // 教科
                		
                		final RecordAverageDat gradeAvg = RecordAverageDat.getGradeAvg(avgMap, mst._subclasscd, param._grade);
                		if (null != gradeAvg) {
                			svf.VrsOutn("AVERAGE", line, sishaGonyu(gradeAvg._avg, 2)); // 評定平均値
                		}
                		final RecordRankSubclass subclass = (RecordRankSubclass) subclassMap.get(mst);
                		final List hyoteiAllList = new ArrayList();
                		for (int hi = 1; hi <= 5; hi++) {
                			final Integer hyotei = new Integer(hi);
                			hyoteiAllList.addAll(getMappedList(subclass._scoreSchregnoListMap, hyotei));
                		}
                		
                		for (int hi = 1; hi <= 5; hi++) {
                			final String ssi = String.valueOf(hi);
                			final Integer hyotei = new Integer(hi);
                			final int count = getMappedList(subclass._scoreSchregnoListMap, hyotei).size();
                			if ("1".equals(param._outputDiv)) {
                				final String percentage = percentage(count, hyoteiAllList.size());
                				if (null != percentage) {
                					svf.VrsOutn("PERCENT" + ssi, line, percentage + "%"); // 評定割合
                				}
                			} else if ("2".equals(param._outputDiv)) {
                				svf.VrsOutn("COUNT" + ssi, line, String.valueOf(count)); // 評定人数
                			}
                		}
                	}
                }

                svf.VrEndRecord();
                _hasData = true;
            }
        }
    }

    private static String spaceIfGradeName1Start(final String year, final String gradeName1) {
    	if (null != gradeName1 && 1 <= gradeName1.length() && Character.isDigit(gradeName1.charAt(0))) {
    		return year + " " + gradeName1;
    	}
		return year + StringUtils.defaultString(gradeName1);
	}

	private static String percentage(int bunshi, int bunbo) {
        if (bunbo == 0) {
            return null;
        }
        return new BigDecimal(bunshi).multiply(new BigDecimal(100)).divide(new BigDecimal(bunbo), 1, BigDecimal.ROUND_HALF_UP).toString();
    }

    private static Map getMappedMap(final Map map, final Object key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new TreeMap());
        }
        return (Map) map.get(key1);
    }

    private static List getMappedList(final Map map, final Object key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new ArrayList());
        }
        return (List) map.get(key1);
    }

    private static String sishaGonyu(final BigDecimal val, final int scale) {
        if (null == val) {
            return null;
        }
        return val.setScale(scale, BigDecimal.ROUND_HALF_UP).toString();
    }

    private static class TestItem {
        public String _testcd;
        public String _testitemname;
        public String _semester;
        public String _scoreDivName;
        public boolean _printScore;
        public String semester() {
            return _testcd.substring(0, 1);
        }
        public String toString() {
            return "TestItem(" + _testcd + ":" + _testitemname + ")";
        }
    }

    private static class SubclassMst implements Comparable {
        final String _classcd;
        final String _subclasscd;
        final String _classabbv;
        final String _classname;
        final String _subclassabbv;
        final String _subclassname;
        final Integer _classShoworder3;
        final Integer _subclassShoworder3;
        final boolean _isSaki;
        final boolean _isMoto;
        public SubclassMst(final String classcd, final String subclasscd, final String classabbv, final String classname, final String subclassabbv, final String subclassname,
                final int classShoworder3,
                final int subclassShoworder3,
                final boolean isSaki, final boolean isMoto) {
            _classcd = classcd;
            _subclasscd = subclasscd;
            _classabbv = classabbv;
            _classname = classname;
            _subclassabbv = subclassabbv;
            _subclassname = subclassname;
            _classShoworder3 = new Integer(classShoworder3);
            _subclassShoworder3 = new Integer(subclassShoworder3);
            _isSaki = isSaki;
            _isMoto = isMoto;
        }
        public int compareTo(final Object o) {
            final SubclassMst mst = (SubclassMst) o;
            int rtn;
            rtn = _classShoworder3.compareTo(mst._classShoworder3);
            if (0 != rtn) { return rtn; }
            if (null == _classcd && null == mst._classcd) {
                return 0;
            } else if (null == _classcd) {
                return 1;
            } else if (null == mst._classcd) {
                return -1;
            }
            rtn = _classcd.compareTo(mst._classcd);
            if (0 != rtn) { return rtn; }
            rtn = _subclassShoworder3.compareTo(mst._subclassShoworder3);
            if (0 != rtn) { return rtn; }
            if (null == _subclasscd && null == mst._subclasscd) {
                return 0;
            } else if (null == _subclasscd) {
                return 1;
            } else if (null == mst._subclasscd) {
                return -1;
            }
            return _subclasscd.compareTo(mst._subclasscd);
        }
        public String toString() {
            return "(" + _subclasscd + ":" + _subclassname + ")";
        }
    }
    
    private static class RecordRankSubclass {
        final SubclassMst _subclassMst;
        final Map _scoreSchregnoListMap = new TreeMap();

        RecordRankSubclass(final SubclassMst subclassMst) {
            _subclassMst = subclassMst;
        }
        
        public static Map getRecordRankSubclassMap(final DB2UDB db2, final Param param, final String year, final String semtestcd) {
            final Map map = new TreeMap();
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT  ");
            stb.append("  T1.CLASSCD, ");
            stb.append("  T1.SCHOOL_KIND, ");
            stb.append("  T1.CURRICULUM_CD, ");
            stb.append("  T1.SUBCLASSCD, ");
            stb.append("  T1.SCHREGNO, ");
            stb.append("  T1.SCORE ");
            stb.append(" FROM  ");
            stb.append("  RECORD_RANK_SDIV_DAT T1 ");
            stb.append("  INNER JOIN SUBCLASS_MST T2 ON T2.CLASSCD = T1.CLASSCD AND T2.SCHOOL_KIND = T1.SCHOOL_KIND AND T2.CURRICULUM_CD = T1.CURRICULUM_CD AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("  INNER JOIN SCHREG_REGD_DAT REGD ON REGD.SCHREGNO = T1.SCHREGNO  ");
            stb.append("      AND REGD.YEAR = T1.YEAR ");
            if ("9".equals(param._semester)) {
                stb.append("      AND REGD.SEMESTER = '" + param._ctrlSeme + "' ");
            } else {
                stb.append("      AND REGD.SEMESTER = '" + param._semester + "' ");
            }
            stb.append("      AND REGD.GRADE = '" + param._grade + "' ");
            stb.append(" WHERE  ");
            stb.append("  T1.YEAR = '" + year + "' ");
            stb.append("  AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '" + semtestcd + "' ");
            stb.append("  AND T1.SCORE IS NOT NULL ");

            final String sql = stb.toString();
            for (final Iterator it = KnjDbUtils.query(db2, sql).iterator(); it.hasNext();) {
                final Map row = (Map) it.next();
                final String subclasscd = KnjDbUtils.getString(row, "CLASSCD") + "-" + KnjDbUtils.getString(row, "SCHOOL_KIND") + "-" + KnjDbUtils.getString(row, "CURRICULUM_CD") + "-" + KnjDbUtils.getString(row, "SUBCLASSCD");
                final SubclassMst mst = param.getSubclassMst(subclasscd);
                final Integer score = Integer.valueOf(KnjDbUtils.getString(row, "SCORE"));
                if (null == map.get(mst)) {
                    map.put(mst, new RecordRankSubclass(mst));
                }
                final RecordRankSubclass subclass = (RecordRankSubclass) map.get(mst);
                getMappedList(subclass._scoreSchregnoListMap, score).add(KnjDbUtils.getString(row, "SCHREGNO"));
            }
            return map;
        }
    }
    
    private static class RecordAverageDat {
        final String _subclasscd;
        final String _avgDivKey;
        final String _score;
        final String _highscore;
        final String _lowscore;
        final String _count;
        final BigDecimal _avg;
        final BigDecimal _stddev;

        RecordAverageDat(
            final String subclasscd,
            final String avgDivKey,
            final String score,
            final String highscore,
            final String lowscore,
            final String count,
            final BigDecimal avg,
            final BigDecimal stddev
        ) {
            _subclasscd = subclasscd;
            _avgDivKey = avgDivKey;
            _score = score;
            _highscore = highscore;
            _lowscore = lowscore;
            _count = count;
            _avg = avg;
            _stddev = stddev;
        }
        
        public static RecordAverageDat getGradeAvg(final Map map, final String subclasscd, final String grade) {
            final String avgDivKey = "1" + "-" + grade + "-" + "000" + "-" + "00000000";
            return (RecordAverageDat) getMappedMap(map, subclasscd).get(avgDivKey);
        }
        
//        public static RecordAverageDat getHrAvg(final Map map, final String subclasscd, final String grade, final String hrClass) {
//            final String avgDivKey = "2" + "-" + grade + "-" + hrClass + "-" + "00000000";
//            return (RecordAverageDat) getMappedMap(map, subclasscd).get(avgDivKey);
//        }
//        
//        public static RecordAverageDat getCourseAvg(final Map map, final String subclasscd, final String grade, final String coursecd, final String majorcd, final String coursecode) {
//            final String avgDivKey = "3" + "-" + grade + "-" + "000" + "-" + coursecd + majorcd + coursecode;
//            return (RecordAverageDat) getMappedMap(map, subclasscd).get(avgDivKey);
//        }
//        
//        public static RecordAverageDat getCourseGroupAvg(final Map map, final String subclasscd, final String grade, final String coursegroupCd) {
//            final String avgDivKey = "5" + "-" + grade + "-" + "000" + "-" + "0" + coursegroupCd + "0000";
//            return (RecordAverageDat) getMappedMap(map, subclasscd).get(avgDivKey);
//        }

        public static Map<String, Map<String, RecordAverageDat>> getRecordAverageDatMap(final DB2UDB db2, final Param param, final String year, final String semtestcd) {
            final Map map = new HashMap();
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT  ");
            stb.append("  T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV AS SEMTESTCD, ");
            stb.append("  T1.CLASSCD, ");
            stb.append("  T1.SCHOOL_KIND, ");
            stb.append("  T1.CURRICULUM_CD, ");
            stb.append("  T1.SUBCLASSCD, ");
            stb.append("  AVG_DIV || '-' || GRADE || '-' || HR_CLASS || '-' || COURSECD || MAJORCD || COURSECODE AS AVG_DIV_KEY, ");
            stb.append("  SCORE, ");
            stb.append("  HIGHSCORE, ");
            stb.append("  LOWSCORE, ");
            stb.append("  COUNT, ");
            stb.append("  AVG, ");
            stb.append("  STDDEV ");
            stb.append(" FROM  ");
            stb.append("  RECORD_AVERAGE_SDIV_DAT T1 ");
            stb.append("  INNER JOIN SUBCLASS_MST T2 ON T2.CLASSCD = T1.CLASSCD AND T2.SCHOOL_KIND = T1.SCHOOL_KIND AND T2.CURRICULUM_CD = T1.CURRICULUM_CD AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append(" WHERE  ");
            stb.append("  T1.YEAR = '" + year + "' ");
            stb.append("  AND T1.SEMESTER = '" + semtestcd.substring(0, 1) + "' AND T1.TESTKINDCD = '" + semtestcd.substring(1, 3) + "' AND T1.TESTITEMCD  = '" + semtestcd.substring(3, 5) + "' AND T1.SCORE_DIV = '" + semtestcd.substring(5, 7) + "' ");

            final String sql = stb.toString();
            for (final Iterator it = KnjDbUtils.query(db2, sql).iterator(); it.hasNext();) {
                final Map row = (Map) it.next();
                final String subclasscd = KnjDbUtils.getString(row, "CLASSCD") + "-" + KnjDbUtils.getString(row, "SCHOOL_KIND") + "-" + KnjDbUtils.getString(row, "CURRICULUM_CD") + "-" + KnjDbUtils.getString(row, "SUBCLASSCD");
                final String avgDivKey = KnjDbUtils.getString(row, "AVG_DIV_KEY");
                final String score = KnjDbUtils.getString(row, "SCORE");
                final String highscore = KnjDbUtils.getString(row, "HIGHSCORE");
                final String lowscore = KnjDbUtils.getString(row, "LOWSCORE");
                final String count = KnjDbUtils.getString(row, "COUNT");
                final BigDecimal avg = KnjDbUtils.getBigDecimal(row, "AVG", null);
                final BigDecimal stddev = KnjDbUtils.getBigDecimal(row, "STDDEV", null);
                final RecordAverageDat recordaveragedat = new RecordAverageDat(subclasscd, avgDivKey, score, highscore, lowscore, count, avg, stddev);
                getMappedMap(map, subclasscd).put(avgDivKey, recordaveragedat);
            }
            return map;
        }
    }
    
    protected Param createParam(final HttpServletRequest request, final DB2UDB db2) {
        log.fatal("$Revision: 75605 $ $Date: 2020-07-22 15:03:54 +0900 (水, 22 7 2020) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(request, db2);
        return param;
    }

    protected static class Param {
        
        final String _year;
        final String _semester;
        final String _ctrlSeme;
        final String _grade;
        final String _testcd;
        final String _outputDiv; // 1:評定割合 2:人数
        final String _loginDate;

        final String _gradeName1;
        private Map _subclassMst;

        private boolean _isNoPrintMoto;
        private boolean _isPrintSakiKamoku;
        private List _d026List = new ArrayList();
        final String _testitemname;
        final boolean _isOutputDebug;
        
        Param(final HttpServletRequest request, final DB2UDB db2) {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _ctrlSeme = request.getParameter("CTRL_SEME");
            _grade = request.getParameter("GRADE");
            _testcd = request.getParameter("TESTCD");
            _outputDiv = request.getParameter("OUTPUT_DIV");
            _loginDate = KNJ_EditDate.H_Format_Haifun(request.getParameter("LOGIN_DATE"));

            _gradeName1 = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT GRADE_NAME1 FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND GRADE = '" + _grade + "' "));
            setSubclassMst(db2);
            loadNameMstD016(db2);
            setPrintSakiKamoku(db2);
            loadNameMstD026(db2);
            _testitemname = getTestitemname(db2);
            _isOutputDebug = "1".equals(getDbPrginfoProperties(db2, "outputDebug"));
        }
        
        private static String getDbPrginfoProperties(final DB2UDB db2, final String propName) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJE130I' AND NAME = '" + propName + "' "));
        }
        
        private void loadNameMstD016(final DB2UDB db2) {
            _isNoPrintMoto = false;
            final String sql = "SELECT NAMESPARE1 FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'D016' AND NAMECD2 = '01' ";
            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql));
            if ("Y".equals(KnjDbUtils.getString(row, "NAMESPARE1"))) _isNoPrintMoto = true;
            log.info("(名称マスタD016):元科目を表示しない = " + _isNoPrintMoto);
        }

        /**
         * 合併先科目を印刷するか
         */
        private void setPrintSakiKamoku(final DB2UDB db2) {
            // 初期値：印刷する
            _isPrintSakiKamoku = true;
            // 名称マスタ「D021」「01」から取得する
            if ("Y".equals(KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT NAMESPARE3 FROM V_NAME_MST WHERE YEAR='" + _year+ "' AND NAMECD1 = 'D021' AND NAMECD2 = '01' ")))) {
                _isPrintSakiKamoku = false;
            }
            log.debug("合併先科目を印刷するか：" + _isPrintSakiKamoku);
        }
        
        private void loadNameMstD026(final DB2UDB db2) {
            final StringBuffer sql = new StringBuffer();
            final String field = "9".equals(_semester) ? "NAMESPARE1" : "ABBV" + _semester;
            sql.append(" SELECT NAME1 AS SUBCLASSCD FROM V_NAME_MST ");
            sql.append(" WHERE YEAR = '" + _year + "' AND NAMECD1 = 'D026' AND " + field + " = '1'  ");
            
            _d026List.clear();
            _d026List.add(KnjDbUtils.getColumnDataList(KnjDbUtils.query(db2, sql.toString()), "SUBCLASSCD"));
            log.info("非表示科目:" + _d026List);
        }
        
        private SubclassMst getSubclassMst(final String subclasscd) {
            if (null == _subclassMst.get(subclasscd)) {
                String classcd = null;
                if (null != subclasscd) {
                    final String[] split = StringUtils.split(subclasscd, "-");
                    if (null != split && split.length > 2) {
                        classcd = split[0] + "-" + split[1];
                    }
                }
                return new SubclassMst(classcd, subclasscd, null, null, null, null, 9999, 9999, false, false);
            }
            return (SubclassMst) _subclassMst.get(subclasscd);
        }

        private void setSubclassMst(final DB2UDB db2) {
            _subclassMst = new HashMap();
            String sql = "";
            sql += " WITH REPL AS ( ";
            sql += " SELECT DISTINCT '1' AS DIV, COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || COMBINED_SUBCLASSCD AS SUBCLASSCD FROM SUBCLASS_REPLACE_COMBINED_DAT WHERE YEAR = '" + _year + "' ";
            sql += " UNION ";
            sql += " SELECT DISTINCT '2' AS DIV, ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ATTEND_SUBCLASSCD AS SUBCLASSCD FROM SUBCLASS_REPLACE_COMBINED_DAT WHERE YEAR = '" + _year + "' ";
            sql += " ) ";
            sql += " SELECT ";
            sql += " T1.CLASSCD || '-' || T1.SCHOOL_KIND AS CLASSCD, ";
            sql += " T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ";
            sql += " T1.SUBCLASSCD AS SUBCLASSCD, T2.CLASSABBV, T2.CLASSNAME, T1.SUBCLASSABBV, VALUE(T1.SUBCLASSORDERNAME2, T1.SUBCLASSNAME) AS SUBCLASSNAME, ";
            sql += " VALUE(T2.SHOWORDER3, 999999) AS CLASS_SHOWORDER3, ";
            sql += " VALUE(T1.SHOWORDER3, 999999) AS SUBCLASS_SHOWORDER3, ";
            sql += " CASE WHEN L1.SUBCLASSCD IS NOT NULL THEN 1 END AS IS_SAKI, ";
            sql += " CASE WHEN L2.SUBCLASSCD IS NOT NULL THEN 1 END AS IS_MOTO ";
            sql += " FROM SUBCLASS_MST T1 ";
            sql += " LEFT JOIN REPL L1 ON L1.DIV = '1' AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = L1.SUBCLASSCD ";
            sql += " LEFT JOIN REPL L2 ON L2.DIV = '2' AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = L2.SUBCLASSCD ";
            sql += " LEFT JOIN CLASS_MST T2 ON T2.CLASSCD = T1.CLASSCD AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
            for (final Iterator it = KnjDbUtils.query(db2, sql).iterator(); it.hasNext();) {
            	final Map row = (Map) it.next();
                final int classShoworder3 = Integer.parseInt(KnjDbUtils.getString(row, "CLASS_SHOWORDER3"));
                final int subclassShoworder3 = Integer.parseInt(KnjDbUtils.getString(row, "SUBCLASS_SHOWORDER3"));
                final boolean isSaki = "1".equals(KnjDbUtils.getString(row, "IS_SAKI"));
                final boolean isMoto = "1".equals(KnjDbUtils.getString(row, "IS_MOTO"));
                final SubclassMst mst = new SubclassMst(KnjDbUtils.getString(row, "CLASSCD"), KnjDbUtils.getString(row, "SUBCLASSCD"), KnjDbUtils.getString(row, "CLASSABBV"), KnjDbUtils.getString(row, "CLASSNAME"), KnjDbUtils.getString(row, "SUBCLASSABBV"), KnjDbUtils.getString(row, "SUBCLASSNAME"), classShoworder3, subclassShoworder3, isSaki, isMoto);
                _subclassMst.put(KnjDbUtils.getString(row, "SUBCLASSCD"), mst);
            }
        }
        
        protected String getTestitemname(final DB2UDB db2) {
            final String sql = "SELECT T1.TESTITEMNAME FROM TESTITEM_MST_COUNTFLG_NEW_SDIV T1 WHERE T1.YEAR = '" + _year + "' AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '" + _testcd + "' ";
            log.debug(" sql = " + sql);
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, sql));
        }
    }
}
