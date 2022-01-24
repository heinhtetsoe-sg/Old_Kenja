// kanji=漢字
/*
 * $Id: 9bde686c82acbcb612180aa5feef847044d0d35e $
 */
package servletpack.KNJD;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 * 学校教育システム 賢者 [成績管理]  成績通知票
 */
public class KNJD624V {

    private static final Log log = LogFactory.getLog(KNJD624V.class);

    private boolean _hasData;
    private Param _param;

    private static final String AVG_GRADE = "1";
    private static final String AVG_HR = "2";
    private static final String AVG_COURSE = "3";
    private static final String AVG_MAJOR = "4";

    private static final String AVG_DIV_GRADE = "GRADE";
    private static final String AVG_DIV_HR_CLASS = "HR_CLASS";
    private static final String AVG_DIV_COURSE = "COURSE";
    private static final String AVG_DIV_MAJOR = "MAJOR";

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

            if ("2".equals(_param._radio)) {
                printMainHr(db2, svf);
            } else {
                for (int i = 0; i < _param._categorySelected.length; i++) {
                    printMain(db2, svf, _param._categorySelected[i]);
                }
            }

        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            db2.commit();
            db2.close();

            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
            svf.VrQuit();
        }
    }

    private static List getPageList(final Collection list, final int count) {
        final List rtn = new ArrayList();
        List current = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Object o = it.next();
            if (null == current || current.size() >= count) {
                current = new ArrayList();
                rtn.add(current);
            }
            current.add(o);
        }
        return rtn;
    }

    private static ScoreDistribution getMappedDistribution(final Map map, final String key) {
        if (null == map.get(key)) {
            map.put(key, new ScoreDistribution());
        }
        return (ScoreDistribution) map.get(key);
    }

    private static String objToString(final Object o) {
        return null == o ? "" : o.toString();
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf, final String subclassCd) {
        final String subclassName = (String) _param._subclassnameMap.get(subclassCd);
        final Subclass subclass = new Subclass(subclassCd, subclassName);
        // subclass.setPerfect(db2, _param);
        subclass._highPerfect = "100";
        subclass._lowPerfect = "100";

        final int FORM1_MAX_COL;
        final String FORM_FILE1;
        if ("1".equals(_param._knjd624vPage1Columns9)) {
        	FORM1_MAX_COL = 9;
            FORM_FILE1 = "1".equals(_param._knjd624vStudentLines55) ? "KNJD624V_6_2.frm" : "KNJD624V_6.frm"; // 9列
        } else {
        	FORM1_MAX_COL = 8;
            FORM_FILE1 = "1".equals(_param._knjd624vStudentLines55) ? "KNJD624V_1_2.frm" : "KNJD624V_1.frm"; // 8列
        }
        final List printHrListAll = HrClass.getPrintHrList(db2, _param, subclass);
        final List pageList = getPageList(printHrListAll, FORM1_MAX_COL);
        final String title = KNJ_EditDate.getAutoFormatYear(db2, Integer.parseInt(_param._year)) + "年度" +  _param._semesterName + "  " + _param._testName + "分布表";

        if ("1".equals(_param._knjd624vDistPageIsOther)) { // 分布は別フォームに出力する

            for (int pi = 0; pi < pageList.size(); pi++) {
            	final List printHrList = (List) pageList.get(pi);

            	final String form = FORM_FILE1;
            	svf.VrSetForm(form, 1);

            	printHeader(svf, subclass, title);

            	for (int hri = 0; hri < printHrList.size(); hri++) {
            		final int col = hri + 1;
            		final HrClass hrClass = (HrClass) printHrList.get(hri);
            		printHr(svf, subclass, col, hrClass, hrClass._hrClassName);
            	}

            	svf.VrEndPage();
            	_hasData = true;
            }

            final int FORM3_DIST_HR_COL_MAX = 27;

            final String FORM_FILE3; // 分布表のみ
            if (_param._is5dankai) {
            	FORM_FILE3 = "KNJD624V_5.frm";
            } else {
            	FORM_FILE3 = "KNJD624V_4.frm";
            }
    		final boolean isRecordType = true;
    		final List distPageList = getPageList(printHrListAll, FORM3_DIST_HR_COL_MAX);
    		for (int i = 0; i < distPageList.size(); i++) {
    			final List printDistHrList = (List) distPageList.get(i);
    			svf.VrSetForm(FORM_FILE3, isRecordType ? 4 : 1); // レコード型
            	printHeader(svf, subclass, title);
    			printLastPageDist(svf, subclass, printDistHrList, printHrListAll, FORM3_DIST_HR_COL_MAX, isRecordType);
    		}

        } else {

            final int FORM2_HR_COL_MAX = 6;
            final int FORM2_DIST_HR_COL_MAX = 8;
        	boolean setForm2 = false;
            final String FORM_FILE2; // 6列+分布表
            if (_param._is5dankai) {
                FORM_FILE2 = "1".equals(_param._knjd624vStudentLines55) ? "KNJD624V_3_2.frm" : "KNJD624V_3.frm";
            } else {
                FORM_FILE2 = "1".equals(_param._knjd624vStudentLines55) ? "KNJD624V_2_2.frm" : "KNJD624V_2.frm";
            }

            for (int pi = 0; pi < pageList.size(); pi++) {
            	final List printHrList = (List) pageList.get(pi);

            	final boolean isLastPage = pi == pageList.size() - 1;
            	final String form;
            	if (isLastPage && printHrList.size() <= FORM2_HR_COL_MAX) {
            		setForm2 = true;
            		form = FORM_FILE2;
            	} else {
            		form = FORM_FILE1;
            	}
            	svf.VrSetForm(form, 1);

            	printHeader(svf, subclass, title);

            	for (int hri = 0; hri < printHrList.size(); hri++) {
            		final int col = hri + 1;
            		final HrClass hrClass = (HrClass) printHrList.get(hri);
            		printHr(svf, subclass, col, hrClass, hrClass._hrClassName);
            	}

            	if (isLastPage) {
            		final boolean isRecordType = false;
            		final List distPageList = getPageList(printHrListAll, FORM2_DIST_HR_COL_MAX);
            		for (int i = 0; i < distPageList.size(); i++) {
            			final List printDistHrList = (List) distPageList.get(i);
            			if (i != 0 || !setForm2) {
            				svf.VrEndPage();
            				svf.VrSetForm(FORM_FILE2, isRecordType ? 4 : 1);

            				printHeader(svf, subclass, title);
            			}

            			printLastPageDist(svf, subclass, printDistHrList, printHrListAll, FORM2_DIST_HR_COL_MAX, isRecordType);
            		}
            	}

            	svf.VrEndPage();
            	_hasData = true;
            }
        }
    }

	private void printHeader(final Vrw32alp svf, final Subclass subclass, final String title) {
		svf.VrsOut("TITLE", title); // テスト名称
		svf.VrsOut("GRADE", _param._gradeName); // 学年
		svf.VrsOut("SUBJECT", subclass._subclassName); // 科目名称
		if (_param._is5dankai) {
			svf.VrAttribute("PERFECT", "X=10000"); // 非表示
		} else {
			svf.VrsOut("PERFECT", subclass.getPrintPerfect(_param)); // 満点
		}
	}

	private void printHr(final Vrw32alp svf, final Subclass subclass, final int col, final HrClass hrClass, final String title) {
		//基本部分印字
		svf.VrsOutn("CLASS1", col, title);
		final AvgDat hrAvgDat = (AvgDat) hrClass._avgDatMap.get(subclass._subclassCd + ":" + AVG_DIV_HR_CLASS);
		if (null != hrAvgDat) {
		    svf.VrsOutn("TOTAL_POINT1", col, hrAvgDat._score);
		    svf.VrsOutn("TOTAL_NUM1", col, hrAvgDat._cnt);
		    svf.VrsOutn("TOTAL_AVERAGE1", col, getAvg(hrAvgDat._avg, 1));
		}

		//分布の合計欄
		final AvgDat gradeAvgDat = (AvgDat) hrClass._avgDatMap.get(subclass._subclassCd + ":" + (_param._isPrintMajorAvg ? AVG_DIV_MAJOR : AVG_DIV_GRADE));
		if (null != gradeAvgDat) {
		    svf.VrsOut("GRADE_AVERAGE",  getAvg(gradeAvgDat._avg, 1));
		}

		if ("1".equals(_param._printOrderScore)) {
		    Collections.sort(hrClass._students, new ScoreComparator(subclass));
		}

		//生徒データ印字
		for (int j = 0; j < hrClass._students.size(); j++) {
		    final Student student = (Student) hrClass._students.get(j);
		    final int stline = j + 1;
		    svf.VrsOutn("ATTENDNO" + stline, col, student._attendNo);
		    svf.VrsOutn("NAME" + stline, col, student._name);
		    svf.VrsOutn("SCORE" + stline, col, (String) student._scoreMap.get(subclass._subclassCd));
		}
	}

    private void printMainHr(final DB2UDB db2, final Vrw32alp svf) {

        final int FORM1_MAX_COL;
        final String FORM_FILE1;
        if ("1".equals(_param._knjd624vPage1Columns9)) {
        	FORM1_MAX_COL = 9;
            FORM_FILE1 = "1".equals(_param._knjd624vStudentLines55) ? "KNJD624V_6_2.frm" : "KNJD624V_6.frm"; // 9列
        } else {
        	FORM1_MAX_COL = 8;
            FORM_FILE1 = "1".equals(_param._knjd624vStudentLines55) ? "KNJD624V_1_2.frm" : "KNJD624V_1.frm"; // 8列
        }
        final String nendo = KNJ_EditDate.getAutoFormatYear(db2, Integer.parseInt(_param._year)) + "年度";

        final List printHrListAll = HrClass.getPrintHrList(db2, _param, null);
        for (final Iterator it = printHrListAll.iterator(); it.hasNext();) {
            final HrClass hrClass = (HrClass) it.next();
            final Set subclasscdSet = new TreeSet();
            for (final Iterator sit = hrClass._students.iterator(); sit.hasNext();) {
                final Student student = (Student) sit.next();
                subclasscdSet.addAll(student._scoreMap.keySet());
            }

            final List pageList = getPageList(subclasscdSet, FORM1_MAX_COL);
            for (int pi = 0; pi < pageList.size(); pi++) {
                final List printSubclassList = (List) pageList.get(pi);

                final String form = FORM_FILE1;
                svf.VrSetForm(form, 1);

                svf.VrsOut("TITLE", nendo +  _param._semesterName + "  " + _param._testName + "分布表"); // テスト名称
                svf.VrsOut("GRADE", hrClass._hrName);

                svf.VrAttribute("SUBJECT", "X=10000");
                svf.VrAttribute("GRADE_AVERAGE", "X=10000");
                svf.VrAttribute("PERFECT", "X=10000");

                for (int subi = 0; subi < printSubclassList.size(); subi++) {
                    final String subclasscd = (String) printSubclassList.get(subi);

                    final String subclassName = (String) _param._subclassnameMap.get(subclasscd);
                    final Subclass subclass = new Subclass(subclasscd, subclassName);
                    subclass._highPerfect = "100";
                    subclass._lowPerfect = "100";

                    final int col = subi + 1;

                    printHr(svf, subclass, col, hrClass, subclassName);

                }
                svf.VrEndPage();
                _hasData = true;
            }
        }
    }

    private void printLastPageDist(final Vrw32alp svf, final Subclass subclass, final List printDistHrList, final List printHrListAll, final int hrMax, final boolean isRecordType) {

        final List rangeList = new ArrayList();
        final int max, kizami, min;
        if (_param._is5dankai) {
            // 5段階
            max = 5;
            kizami = 1;
            min = 1;
        } else {
            // 100段階 5点きざみ(95, 99), (90, 94), (85, 89), (80, 84), (70, 79),  ..., (0, 4)
            max = 100;
            kizami = 5;
            min = 0;
        }
        rangeList.add(new ScoreRange(max, 999));
        for (int i = max; i - kizami >= min; i -= kizami) {
            rangeList.add(new ScoreRange(i - kizami, i));
        }

        final int DANJO_DIST_DANSI_COL = 1;
        final int DANJO_DIST_JOSI_COL = 2;
        final int DANJO_DIST_GOKEI_COL = 3;
        svf.VrsOutn("CLASS3", DANJO_DIST_DANSI_COL, "男子");
        svf.VrsOutn("CLASS3", DANJO_DIST_JOSI_COL, "女子");
        svf.VrsOutn("CLASS3", DANJO_DIST_GOKEI_COL, "学年");

        AvgDat gradeAvgDat = null;
        if (printDistHrList.size() > 0) {
            final HrClass hrClass = (HrClass) printDistHrList.get(0);

            //分布の合計欄
            gradeAvgDat = (AvgDat) hrClass._avgDatMap.get(subclass._subclassCd + ":" + (_param._isPrintMajorAvg ? AVG_DIV_MAJOR : AVG_DIV_GRADE));
        }
        if (null != gradeAvgDat) {
        	svf.VrsOut("GRADE_AVERAGE",  getAvg(gradeAvgDat._avg, 1));

        	svf.VrsOutn("TOTAL_POINT3", DANJO_DIST_GOKEI_COL, gradeAvgDat._scoreKansan);
        	svf.VrsOutn("TOTAL_NUM3", DANJO_DIST_GOKEI_COL, gradeAvgDat._cnt);
        	svf.VrsOutn("TOTAL_AVERAGE3", DANJO_DIST_GOKEI_COL, getAvg(gradeAvgDat._avgKansan, 1));
        }

        //男女分布印字
        final Map hrDistMap = new HashMap();
        final ScoreDistribution total = new ScoreDistribution();
        for (final Iterator ith = printHrListAll.iterator(); ith.hasNext();) {
            final HrClass hrClass = (HrClass) ith.next();
            int distLine = 1;
            for (final Iterator itd = rangeList.iterator(); itd.hasNext();) {
                final ScoreRange sr = (ScoreRange) itd.next();
                final ScoreDistribution dist = getScoreDistribution(subclass, hrClass._students, sr);
                total.add(dist);
                final ScoreDistribution hrDist =  getMappedDistribution(hrDistMap,  String.valueOf(distLine));
                hrDist.add(dist);
                distLine++;
            }
        }
        for (final Iterator itd = hrDistMap.keySet().iterator(); itd.hasNext();) {
            final String distLine = (String) itd.next();
            final ScoreDistribution dist = (ScoreDistribution) hrDistMap.get(distLine);
            svf.VrsOutn("FIELD" + distLine, DANJO_DIST_DANSI_COL, String.valueOf(dist._dansiScore.size()));
            svf.VrsOutn("FIELD" + distLine, DANJO_DIST_JOSI_COL, String.valueOf(dist._jyosiScore.size()));
            svf.VrsOutn("FIELD" + distLine, DANJO_DIST_GOKEI_COL, String.valueOf(dist.totalCount()));
        }
        svf.VrsOutn("TOTAL_POINT3", DANJO_DIST_DANSI_COL, objToString(sum(total._dansiScore)));
        svf.VrsOutn("TOTAL_POINT3", DANJO_DIST_JOSI_COL, objToString(sum(total._jyosiScore)));
        svf.VrsOutn("TOTAL_NUM3", DANJO_DIST_DANSI_COL, String.valueOf(total._dansiScore.size()));
        svf.VrsOutn("TOTAL_NUM3", DANJO_DIST_JOSI_COL, String.valueOf(total._jyosiScore.size()));

        if (total._dansiScore.size() > 0) {
            final String avg = getBdAvgStr(total._dansiScore, 1);
            //log.info(" dansi total = " + total._dansiScore + " , count = " + total._dansiCnt + " = avg = " + avg);
            svf.VrsOutn("TOTAL_AVERAGE3", DANJO_DIST_DANSI_COL, avg);
        }
        if (total._jyosiScore.size() > 0) {
            final String avg = getBdAvgStr(total._jyosiScore, 1);
            //log.info(" jyosi total = " + total._jyosiScore + " , count = " + total._jyosiCnt + " = avg = " + avg);
            svf.VrsOutn("TOTAL_AVERAGE3", DANJO_DIST_JOSI_COL,  avg);
        }

        //クラス分布合計印字
        final Map hrDistMapCl = new LinkedMap();
        for (final Iterator ith = printHrListAll.iterator(); ith.hasNext();) {
            final HrClass hrClass = (HrClass) ith.next();
            for (int ri = 0; ri < rangeList.size(); ri++) {
                final ScoreRange sr = (ScoreRange) rangeList.get(ri);
                final int rangeLine = ri + 1;
                final ScoreDistribution dist = getScoreDistribution(subclass, hrClass._students, sr);
                getMappedDistribution(hrDistMapCl,  String.valueOf(rangeLine)).add(dist);
            }
        }

        if (isRecordType) {
            for (int hri = 0; hri < printDistHrList.size(); hri++) {
                final HrClass hrClass = (HrClass) printDistHrList.get(hri);
                //基本部分印字
                svf.VrsOut("CLASS1", hrClass._hrClassName);
                final AvgDat hrAvgDat = (AvgDat) hrClass._avgDatMap.get(subclass._subclassCd + ":" + AVG_DIV_HR_CLASS);
                if (null != hrAvgDat) {
                    svf.VrsOut("TOTAL_NUM2", hrAvgDat._cnt);
                    svf.VrsOut("TOTAL_AVERAGE2", getAvg(hrAvgDat._avgKansan, 0));
                }

                //クラス分布印字
                for (int ri = 0; ri < rangeList.size(); ri++) {
                    final ScoreRange sr = (ScoreRange) rangeList.get(ri);
                    final int rangeLine = ri + 1;
                    final ScoreDistribution dist = getScoreDistribution(subclass, hrClass._students, sr);
//                    log.info(" hrClass " + hrClass._grade + hrClass._hrClass + " | " + sr._lowInclusive + " ~ " + sr._highExclusive + " => " + dist._dansiScore + " , " + dist._jyosiScore + "( " + hrClass._students.size() + ")");
                    svf.VrsOutn("CLASS_NUM", rangeLine, String.valueOf(dist.totalCount()));
                }
                svf.VrEndRecord();
            }

            //分布の合計欄
            svf.VrsOut("CLASS1", "全体");
            if (null != gradeAvgDat) {
                svf.VrsOut("TOTAL_NUM2", gradeAvgDat._cnt);
                svf.VrsOut("TOTAL_AVERAGE2", getAvg(gradeAvgDat._avgKansan, 0));
            }

            for (final Iterator ith = hrDistMapCl.keySet().iterator(); ith.hasNext();) {
                final String rangeLine = (String) ith.next();
                final ScoreDistribution dist = getMappedDistribution(hrDistMapCl, rangeLine);
                svf.VrsOutn("CLASS_NUM", Integer.parseInt(rangeLine), String.valueOf(dist.totalCount()));
            }
            svf.VrEndRecord();

        } else {
            for (int hri = 0; hri < printDistHrList.size(); hri++) {
                final int hrcol = hri + 1;
                final HrClass hrClass = (HrClass) printDistHrList.get(hri);
                //基本部分印字
                svf.VrsOutn("CLASS2", hrcol, hrClass._hrClassName);
                final AvgDat hrAvgDat = (AvgDat) hrClass._avgDatMap.get(subclass._subclassCd + ":" + AVG_DIV_HR_CLASS);
                if (null != hrAvgDat) {
                    svf.VrsOutn("TOTAL_NUM2", hrcol, hrAvgDat._cnt);
                    svf.VrsOutn("TOTAL_AVERAGE2", hrcol, getAvg(hrAvgDat._avgKansan, 0));
                }

                //クラス分布印字
                for (int ri = 0; ri < rangeList.size(); ri++) {
                    final ScoreRange sr = (ScoreRange) rangeList.get(ri);
                    final int rangeLine = ri + 1;
                    final ScoreDistribution dist = getScoreDistribution(subclass, hrClass._students, sr);
//                    log.info(" hrClass " + hrClass._grade + hrClass._hrClass + " | " + sr._lowInclusive + " ~ " + sr._highExclusive + " => " + dist._dansiScore + " , " + dist._jyosiScore + "( " + hrClass._students.size() + ")");
                    svf.VrsOutn("CLASS_NUM" + rangeLine, hrcol, String.valueOf(dist.totalCount()));
                }
            }

            final int HR_DIST_LAST_COL = hrMax + 1;
            //分布の合計欄
            svf.VrsOutn("CLASS2", HR_DIST_LAST_COL, "全体");
            if (null != gradeAvgDat) {
                svf.VrsOutn("TOTAL_POINT2", HR_DIST_LAST_COL, gradeAvgDat._scoreKansan);
                svf.VrsOutn("TOTAL_NUM2", HR_DIST_LAST_COL, gradeAvgDat._cnt);
                svf.VrsOutn("TOTAL_AVERAGE2", HR_DIST_LAST_COL, getAvg(gradeAvgDat._avgKansan, 0));
            }

            for (final Iterator ith = hrDistMapCl.keySet().iterator(); ith.hasNext();) {
                final String rangeLine = (String) ith.next();
                final ScoreDistribution dist = getMappedDistribution(hrDistMapCl, rangeLine);
                svf.VrsOutn("CLASS_NUM" + rangeLine, HR_DIST_LAST_COL, String.valueOf(dist.totalCount()));
            }
        }
    }

    private static BigDecimal sum(final List bdList) {
        if (bdList.size() == 0) {
            return null;
        }
        BigDecimal sum = new BigDecimal(0);
        for (int i = 0; i < bdList.size(); i++) {
            final BigDecimal bd = (BigDecimal) bdList.get(i);
            sum = sum.add(bd);
        }
        return sum;
    }

    private static String getBdAvgStr(final List bdList, final int scale) {
        if (bdList.size() == 0) {
            return null;
        }
        final BigDecimal sum = sum(bdList);
        return sum.divide(new BigDecimal(bdList.size()), scale, BigDecimal.ROUND_HALF_UP).toString();
    }

    private static String getAvg(final String s, final int scale) {
        return null == s ? null : new BigDecimal(s).setScale(scale, BigDecimal.ROUND_HALF_UP).toString();
    }

    private static ScoreDistribution getScoreDistribution(final Subclass subclass, final List students, final ScoreRange range) {
        final ScoreDistribution sd = new ScoreDistribution();
        for (final Iterator itr = students.iterator(); itr.hasNext();) {
            final Student student = (Student) itr.next();
            if (null != student._scoreMap.get(subclass._subclassCd)) {
                final int score = Integer.parseInt((String) student._scoreMap.get(subclass._subclassCd));
                if (range._lowInclusive <= score && score < range._highExclusive) {
                    if ("1".equals(student._sex)) {
                        sd._dansiScore.add(new BigDecimal(score));
                    } else if ("2".equals(student._sex)) {
                        sd._jyosiScore.add(new BigDecimal(score));
                    } else {
                        sd._soreigaiScore.add(new BigDecimal(score));
                    }
                }
            }
        }
        return sd;
    }

    private static class HrClass {
        final String _grade;
        final String _hrClass;
        final String _hrName;
        final String _hrClassName;
        private List _students;
        private Map _avgDatMap;
        private String _course;

        public HrClass(
                final String grade,
                final String hrClass,
                final String hrName,
                final String hrClassName
        ) {
            _grade = grade;
            _hrClass = hrClass;
            _hrName = hrName;
            _hrClassName = hrClassName;
        }

        private static Map getAvgDat(
                final DB2UDB db2,
                final Param param,
                final String hrClass,
                final String course,
                final Subclass subclass
        ) {
            final Map retAvgMap = new HashMap();
            final String sql = getAvgSql(param, hrClass, course, subclass);
            // log.debug(" avg sql =" + sql);
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String score = rs.getString("SCORE");
                    final String scoreKansan = rs.getString("SCORE_KANSAN");
                    final String cnt = rs.getString("COUNT");
                    final String avg = rs.getString("AVG");
                    final String avgKansan = rs.getString("AVG_KANSAN");
                    final AvgDat avgDat = new AvgDat(score, scoreKansan, cnt, avg, avgKansan);
                    final String div = rs.getString("DIV");
                    retAvgMap.put(rs.getString("SUBCLASSCD") + ":" + div, avgDat);
                }
            } catch (Exception e) {
                log.fatal("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retAvgMap;
        }

        private static String getAvgSql(final Param param, final String hrClass, final String course, final Subclass subclass) {
        	final String testkindcd = param._testCd.substring(0, 2);
        	final String testitemcd = param._testCd.substring(2, 4);
        	final String scoreDiv = param._testCd.substring(4);
        	final String[] split = null == subclass ? new String[] {"", "", "", ""} : StringUtils.split(subclass._subclassCd, "-");
        	final String coursecd = null == course ? null : course.substring(0, 1);
        	final String majorcd = null == course ? null : course.substring(1, 4);
        	final String coursecode = null == course ? null : course.substring(4);

            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     '" + AVG_DIV_GRADE + "' AS DIV, ");
            stb.append("     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("     SCORE, ");
            stb.append("     SCORE AS SCORE_KANSAN, ");
            stb.append("     COUNT, ");
            stb.append("     AVG, ");
            stb.append("     AVG AS AVG_KANSAN ");
            stb.append(" FROM ");
//            stb.append("     RECORD_AVERAGE_CONV_DAT ");
            stb.append("     RECORD_AVERAGE_SDIV_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + param._year + "' ");
            stb.append("     AND SEMESTER = '" + param._semester + "' ");
            stb.append("     AND TESTKINDCD = '" + testkindcd + "' AND TESTITEMCD = '" + testitemcd + "' AND SCORE_DIV = '" + scoreDiv + "' ");
            if (null != subclass) {
                stb.append("     AND CLASSCD = '" + split[0] + "' AND SCHOOL_KIND = '" + split[1] + "' AND CURRICULUM_CD = '" + split[2] + "' AND SUBCLASSCD = '" + split[3] + "' ");
            }
            stb.append("     AND AVG_DIV = '" + AVG_GRADE + "' ");
            stb.append("     AND GRADE = '" + param._grade + "' ");
            stb.append(" UNION ");
            stb.append(" SELECT ");
            stb.append("     '" + AVG_DIV_HR_CLASS + "' AS DIV, ");
            stb.append("     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("     SCORE, ");
            stb.append("     SCORE AS SCORE_KANSAN, ");
            stb.append("     COUNT, ");
            stb.append("     AVG, ");
            stb.append("     AVG AS AVG_KANSAN ");
            stb.append(" FROM ");
//          stb.append("     RECORD_AVERAGE_CONV_DAT ");
            stb.append("     RECORD_AVERAGE_SDIV_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + param._year + "' ");
            stb.append("     AND SEMESTER = '" + param._semester + "' ");
            stb.append("     AND TESTKINDCD = '" + testkindcd + "' AND TESTITEMCD = '" + testitemcd + "' AND SCORE_DIV = '" + scoreDiv + "' ");
            if (null != subclass) {
                stb.append("     AND CLASSCD = '" + split[0] + "' AND SCHOOL_KIND = '" + split[1] + "' AND CURRICULUM_CD = '" + split[2] + "' AND SUBCLASSCD = '" + split[3] + "' ");
            }
            stb.append("     AND AVG_DIV = '" + AVG_HR + "' ");
            stb.append("     AND GRADE = '" + param._grade + "' ");
            stb.append("     AND HR_CLASS = '" + hrClass + "' ");
            stb.append(" UNION ");
            stb.append(" SELECT ");
            stb.append("     '" + AVG_DIV_COURSE + "' AS DIV, ");
            stb.append("     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("     SCORE, ");
            stb.append("     SCORE AS SCORE_KANSAN, ");
            stb.append("     COUNT, ");
            stb.append("     AVG, ");
            stb.append("     AVG AS AVG_KANSAN ");
            stb.append(" FROM ");
            stb.append("     RECORD_AVERAGE_SDIV_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + param._year + "' ");
            stb.append("     AND SEMESTER = '" + param._semester + "' ");
            stb.append("     AND TESTKINDCD = '" + testkindcd + "' AND TESTITEMCD = '" + testitemcd + "' AND SCORE_DIV = '" + scoreDiv + "' ");
            if (null != subclass) {
                stb.append("     AND CLASSCD = '" + split[0] + "' AND SCHOOL_KIND = '" + split[1] + "' AND CURRICULUM_CD = '" + split[2] + "' AND SUBCLASSCD = '" + split[3] + "' ");
            }
            stb.append("     AND AVG_DIV = '" + AVG_COURSE + "' ");
            stb.append("     AND GRADE = '" + param._grade + "' ");
            stb.append("     AND COURSECD = '" + coursecd + "' AND MAJORCD = '" + majorcd + "' AND COURSECODE = '" + coursecode + "' ");
            stb.append(" UNION ");
            stb.append(" SELECT ");
            stb.append("     '" + AVG_DIV_MAJOR + "' AS DIV, ");
            stb.append("     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("     SCORE, ");
            stb.append("     SCORE AS SCORE_KANSAN, ");
            stb.append("     COUNT, ");
            stb.append("     AVG, ");
            stb.append("     AVG AS AVG_KANSAN ");
            stb.append(" FROM ");
            stb.append("     RECORD_AVERAGE_SDIV_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + param._year + "' ");
            stb.append("     AND SEMESTER = '" + param._semester + "' ");
            stb.append("     AND TESTKINDCD = '" + testkindcd + "' AND TESTITEMCD = '" + testitemcd + "' AND SCORE_DIV = '" + scoreDiv + "' ");
            if (null != subclass) {
                stb.append("     AND CLASSCD = '" + split[0] + "' AND SCHOOL_KIND = '" + split[1] + "' AND CURRICULUM_CD = '" + split[2] + "' AND SUBCLASSCD = '" + split[3] + "' ");
            }
            stb.append("     AND AVG_DIV = '" + AVG_MAJOR + "' ");
            stb.append("     AND GRADE = '" + param._grade + "' ");
            stb.append("     AND COURSECD = '" + coursecd + "' AND MAJORCD = '" + majorcd + "' ");
            return stb.toString();
        }

        private static List getStudents(
                final DB2UDB db2,
                final Param param,
                final String grade,
                final String pHrClass,
                final Subclass subclass
        ) {
            final List studentList = new ArrayList();
            final String sql = getStudentsSql(param, grade, pHrClass, subclass);
//            log.info(" sql = " + sql);
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map studentMap = new HashMap();
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String schregno = rs.getString("SCHREGNO");
                    if (null == studentMap.get(schregno)) {
                        final String hrClass = rs.getString("HR_CLASS");
                        final String course = rs.getString("COURSE");
                        final String attendNo = rs.getString("ATTENDNO");
                        final String name = rs.getString("NAME");
                        final String sex = rs.getString("SEX");
                        final Student student = new Student(schregno, hrClass, course, attendNo, name, sex);
                        studentList.add(student);
                        studentMap.put(schregno, student);
                    }
                    if (null != rs.getString("SUBCLASSCD")) {
                        final Student student = (Student) studentMap.get(schregno);
                        student._scoreMap.put(rs.getString("SUBCLASSCD"), rs.getString("SCORE"));
                    }
                }
            } catch (Exception e) {
                log.fatal("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return studentList;
        }

        private static String getStudentsSql(final Param param, final String grade, final String hrClass, final Subclass subclass) {
        	final String testkindcd = param._testCd.substring(0, 2);
        	final String testitemcd = param._testCd.substring(2, 4);
        	final String scoreDiv = param._testCd.substring(4);

            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.HR_CLASS, ");
            stb.append("     T1.COURSECD || T1.MAJORCD || T1.COURSECODE AS COURSE, ");
            stb.append("     T1.ATTENDNO, ");
            stb.append("     BASE.NAME, ");
            stb.append("     BASE.SEX, ");
            stb.append("     L1.CLASSCD || '-' || L1.SCHOOL_KIND || '-' || L1.CURRICULUM_CD || '-' || L1.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("     L1.SCORE ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT T1 ");
            stb.append("     LEFT JOIN SCHREG_BASE_MST BASE ON T1.SCHREGNO = BASE.SCHREGNO ");
            stb.append("     LEFT JOIN RECORD_RANK_SDIV_DAT L1 ON T1.YEAR = L1.YEAR ");
            stb.append("          AND L1.SEMESTER = '" + param._semester + "' ");
            stb.append("          AND L1.TESTKINDCD = '" + testkindcd + "' AND L1.TESTITEMCD = '" + testitemcd + "' AND L1.SCORE_DIV = '" + scoreDiv + "' ");
            stb.append("          AND T1.SCHREGNO = L1.SCHREGNO ");
            stb.append("          AND L1.CLASSCD <= '90' ");
            if (null != subclass) {
            	final String[] split = null == subclass ? new String[] {"", "", "", ""} : StringUtils.split(subclass._subclassCd, "-");
                stb.append("     AND L1.CLASSCD = '" + split[0] + "' AND L1.SCHOOL_KIND = '" + split[1] + "' AND L1.CURRICULUM_CD = '" + split[2] + "' AND L1.SUBCLASSCD = '" + split[3] + "' ");
            }
            stb.append("          AND L1.SUBCLASSCD NOT IN ('333333', '555555', '999999') ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            if ("9".equals(param._semester)) {
                stb.append("     AND T1.SEMESTER = '" + param._ctrlSeme + "' ");
            } else {
                stb.append("     AND T1.SEMESTER = '" + param._semester + "' ");
            }
            if ("1".equals(param._use_school_detail_gcm_dat)) {
                stb.append("     AND T1.COURSECD || '-' || T1.MAJORCD = '" + param._major + "' ");
            }
            stb.append("     AND T1.GRADE = '" + grade + "' ");
            stb.append("     AND T1.HR_CLASS = '" + hrClass + "' ");
            stb.append(" ORDER BY ");
            stb.append("     T1.ATTENDNO ");

            return stb.toString();
        }

        private static List getPrintHrList(final DB2UDB db2, final Param param, final Subclass subclass) {
            final List rtn = new ArrayList();
            final String sql = getHrSql(param);
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final HrClass hrClass = new HrClass(rs.getString("GRADE"), rs.getString("HR_CLASS"), rs.getString("HR_NAME"), rs.getString("HR_CLASS_NAME1"));
                    rtn.add(hrClass);
                }
            } catch (Exception e) {
                log.fatal("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            for (final Iterator it = rtn.iterator(); it.hasNext();) {
                final HrClass hrClass = (HrClass) it.next();
                hrClass._students = HrClass.getStudents(db2, param, hrClass._grade, hrClass._hrClass, subclass);
                for (final Iterator stit = hrClass._students.iterator(); stit.hasNext();) {
                    final Student student = (Student) stit.next();
                    if (null != student._course) {
                        hrClass._course = student._course;
                        break;
                    }
                }
                if (null != subclass) {
                    boolean hasData = false;
                    for (final Iterator sit = hrClass._students.iterator(); sit.hasNext();) {
                        final Student student = (Student) sit.next();
                        if (student._scoreMap.get(subclass._subclassCd) != null) {
                            hasData = true;
                            break;
                        }
                    }
                    if (!hasData) {
                        it.remove();
                        continue;
                    }
                }
                hrClass._avgDatMap = HrClass.getAvgDat(db2, param, hrClass._hrClass, hrClass._course, subclass);
            }
            return rtn;
        }

        private static String getHrSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     GRADE, ");
            stb.append("     HR_CLASS, ");
            stb.append("     HR_NAME, ");
            stb.append("     HR_CLASS_NAME1 ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_HDAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + param._year + "' ");
            if ("9".equals(param._semester)) {
                stb.append("     AND SEMESTER = '" + param._ctrlSeme + "' ");
            } else {
                stb.append("     AND SEMESTER = '" + param._semester + "' ");
            }
            stb.append("     AND GRADE = '" + param._grade + "' ");
            if ("2".equals(param._radio)) {
                stb.append("     AND GRADE || HR_CLASS IN " + SQLUtils.whereIn(true, param._categorySelected) + " ");
            }
            stb.append(" ORDER BY ");
            stb.append("     GRADE, ");
            stb.append("     HR_CLASS ");

            return stb.toString();
        }
    }

    private static class AvgDat {
        final String _score;
        final String _scoreKansan;
        final String _cnt;
        final String _avg;
        final String _avgKansan;

        public AvgDat(
                final String score,
                final String scoreKansan,
                final String cnt,
                final String avg,
                final String avgKansan
        ) {
            _score = score;
            _scoreKansan = scoreKansan;
            _cnt = cnt;
            _avg = avg;
            _avgKansan = avgKansan;
        }
    }

    private static class Student {
        final String _schregNo;
        final String _hrClass;
        final String _course;
        final String _attendNo;
        final String _name;
        final String _sex;
        final Map _scoreMap = new HashMap();

        public Student (
                final String schregNo,
                final String hrClass,
                final String course,
                final String attendNo,
                final String name,
                final String sex
        ) {
            _schregNo = schregNo;
            _hrClass = hrClass;
            _course = course;
            _attendNo = attendNo;
            _name = name;
            _sex = sex;
        }
    }

    private static class ScoreRange {
        final int _lowInclusive;
        final int _highExclusive;
        public ScoreRange(final int low, final int high) {
            _lowInclusive = low;
            _highExclusive = high;
        }
    }

    private static class ScoreDistribution {
        final List _dansiScore = new ArrayList();
        final List _jyosiScore = new ArrayList();
        final List _soreigaiScore = new ArrayList();
        public void add(ScoreDistribution dist) {
            _dansiScore.addAll(dist._dansiScore);
            _jyosiScore.addAll(dist._jyosiScore);
            _soreigaiScore.addAll(dist._soreigaiScore);
        }
        public int totalCount() {
            return _dansiScore.size() + _jyosiScore.size() + _soreigaiScore.size();
        }
    }

    private static class Subclass {
        private final String _subclassCd;
        private final String _subclassName;
        private String _highPerfect;
        private String _lowPerfect;

        public Subclass(
                final String subclassCd,
                final String subclassName
        ) {
            _subclassCd = subclassCd;
            _subclassName = subclassName;
        }

        public String getPrintPerfect(final Param param) {
        	if (param._is5dankai) {
        		return "5";
        	}
            return _highPerfect.equals(_lowPerfect) ? _highPerfect : _lowPerfect + "\uFF5E" + _highPerfect;
        }

//        private void setPerfect(
//                final DB2UDB db2,
//                final Param param
//        ) {
//            final String sql = getPerfectSql(param, _subclassCd);
//            PreparedStatement ps = null;
//            ResultSet rs = null;
//            try {
//                ps = db2.prepareStatement(sql);
//                rs = ps.executeQuery();
//                while (rs.next()) {
//                    _highPerfect = rs.getString("MAX_PERFECT");
//                    _lowPerfect = rs.getString("MIN_PERFECT");
//                }
//            } catch (SQLException e) {
//                log.error("exception!", e);
//            } finally {
//                DbUtils.closeQuietly(null, ps, rs);
//                db2.commit();
//            }
//        }
//
//        private static String getPerfectSql(
//                final Param param,
//                final String subclassCd
//        ) {
//            final StringBuffer stb = new StringBuffer();
//            stb.append(" SELECT ");
//            stb.append("     VALUE(MAX(PERFECT), 100) AS MAX_PERFECT, ");
//            stb.append("     VALUE(MIN(PERFECT), 100) AS MIN_PERFECT ");
//            stb.append(" FROM ");
//            stb.append("     PERFECT_RECORD_DAT ");
//            stb.append(" WHERE ");
//            stb.append("     YEAR = '" + param._year + "' ");
//            stb.append("     AND SEMESTER = '" + param._semester + "' ");
//            stb.append("     AND TESTKINDCD || TESTITEMCD = '" + param._testCd + "' ");
//            stb.append("     AND ");
//            if ("1".equals(param._useCurriculumcd)) {
//                stb.append("         CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || ");
//            }
//            stb.append("         SUBCLASSCD = '" + subclassCd + "' ");
//            stb.append("     AND GRADE = CASE WHEN DIV = '01' ");
//            stb.append("                      THEN '00' ");
//            stb.append("                      ELSE '" + param._grade + "' ");
//            stb.append("                 END ");
//
//            return stb.toString();
//        }
    }

    private static class ScoreComparator implements Comparator {
        final Subclass _subclass;
        public ScoreComparator(final Subclass subclass) {
            _subclass = subclass;
        }
        public int compare(final Object o1, final Object o2) {
            final Student s1 = (Student) o1;
            final Student s2 = (Student) o2;
            final String score1 = (String) s1._scoreMap.get(_subclass._subclassCd);
            final String score2 = (String) s2._scoreMap.get(_subclass._subclassCd);
            if (null != score1 || null != score2) {
                if (null == score1) {
                    return 1;
                } else if (null == score2) {
                    return -1;
                }
                final Integer score1i = Integer.valueOf(score1);
                final Integer score2i = Integer.valueOf(score2);
                final int cmp = - score1i.compareTo(score2i); // 降順
                if (0 != cmp) {
                    return cmp;
                }
            }
            return (s1._hrClass + s1._attendNo).compareTo(s2._hrClass + s2._attendNo);
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 70325 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _year;
        final String _semester;
        final String _ctrlSeme;
        final String _semesterName;
        final String _ctrlDate;
        final String _testCd;
        final String _testName;
        final String _grade;
        final String _radio; // 1:科目ごとにすべてのHRを表示 2:HRごとにすべての科目を表示
        final String _gradeName;
        final String _useCurriculumcd;
        final String _printOrderScore;
        final String[] _categorySelected;
        final Map _subclassnameMap;
        final boolean _isPrintMajorAvg;
        final boolean _is5dankai;
        final String _major;
        final String _use_school_detail_gcm_dat;
        final String _useSchool_KindField;
        final String SCHOOLKIND;
        final String SCHOOLCD;
        final String _knjd624vDistPageIsOther; // プロパティーknjd624vDistPageIsOtherが1なら分布は別フォームに出力する
        final String _knjd624vPage1Columns9; // プロパティーknjd624vPage1Column9が1なら1ページ目は9列フォームに出力する
        final String _knjd624vStudentLines55; // プロパティーknjd624vStudentLines55が1なら生徒数55のフォームに出力する

        Param(final DB2UDB db2, final HttpServletRequest request) {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _ctrlSeme = request.getParameter("CTRL_SEME");
            _ctrlDate = request.getParameter("LOGIN_DATE");
            _testCd = request.getParameter("TESTCD");
            _grade = request.getParameter("GRADE");
            _radio = request.getParameter("RADIO");
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _printOrderScore = request.getParameter("PRINT_ORDER_SCORE");
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _major = request.getParameter("MAJOR");
            _use_school_detail_gcm_dat = request.getParameter("use_school_detail_gcm_dat");
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            SCHOOLKIND = request.getParameter("SCHOOLKIND");
            SCHOOLCD = request.getParameter("SCHOOLCD");
            _semesterName = getSemesterName(db2, _year, _semester);
            _testName = getTestName(db2, _year, _semester, _testCd);
            _gradeName = getGradeName(db2, _year, _grade);
            _subclassnameMap = getSubclassName(db2);

            final String z010 = setZ010Name1(db2);
            log.info(" z010 name1 = " + z010);
            final boolean _isRakunan = "rakunan".equals(z010);
            _is5dankai = "9".equals(_semester) && "990009".equals(_testCd) || "chiyoda".equals(z010) && "990008".equals(_testCd) || null != _testCd && _testCd.endsWith("09");
            _isPrintMajorAvg = "sundaikoufu".equals(z010);
            _knjd624vDistPageIsOther = request.getParameter("knjd624vDistPageIsOther");
            _knjd624vPage1Columns9 = request.getParameter("knjd624vPage1Columns9");
            _knjd624vStudentLines55 = _isRakunan ? "1" : request.getParameter("knjd624vStudentLines55");
        }

        /**
         * 名称マスタ NAMECD1='Z010' NAMECD2='00'読込
         */
        private String setZ010Name1(DB2UDB db2) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' "));
        }

        private String getSemesterName(final DB2UDB db2, final String year, final String semester) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT SEMESTERNAME FROM SEMESTER_MST WHERE YEAR = '" + year + "' AND SEMESTER = '" + semester + "'"));
        }

        private String getTestName(final DB2UDB db2, final String year, final String semester, final String testCd) {
            String sql = "";
            if ("1".equals(_use_school_detail_gcm_dat)) {
                sql = "SELECT TESTITEMNAME FROM TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV WHERE YEAR = '" + _year + "'" +
                        " AND SEMESTER = '" + _semester + "' " +
                        " AND GRADE = '00' " +
                        " AND COURSECD || '-' || MAJORCD = '" + _major + "' " +
                        " AND TESTKINDCD || TESTITEMCD || SCORE_DIV = '" + testCd + "' ";
                if ("1".equals(_useSchool_KindField) && !StringUtils.isBlank(SCHOOLKIND)) {
                    sql += " AND SCHOOLCD = '" + SCHOOLCD + "' " +
                           " AND SCHOOL_KIND = '" + SCHOOLKIND + "' ";
                }
            } else {
                sql += "SELECT TESTITEMNAME FROM TESTITEM_MST_COUNTFLG_NEW_SDIV ";
                sql += "WHERE YEAR = '" + year + "' AND SEMESTER = '" + semester + "' AND TESTKINDCD || TESTITEMCD || SCORE_DIV = '" + testCd + "'";
            }
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, sql));
        }

        private String getGradeName(final DB2UDB db2, final String year, final String grade) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT GRADE_NAME1 FROM SCHREG_REGD_GDAT WHERE YEAR = '" + year + "' AND GRADE = '" + grade + "'"));
        }

        private Map getSubclassName(final DB2UDB db2) {
            return KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, "SELECT CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS SUBCLASSCD, SUBCLASSNAME FROM SUBCLASS_MST "), "SUBCLASSCD", "SUBCLASSNAME");
        }

    }
}

// eof
