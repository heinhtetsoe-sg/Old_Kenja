// kanji=漢字
/*
 * $Id: d315669e2c9fe35885a2eaa4ba71be415693838f $
 */
package servletpack.KNJD;

import static servletpack.KNJZ.detail.KNJ_EditEdit.getMS932ByteLength;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

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

/**
 * 学校教育システム 賢者 [成績管理]  度数分布表
 */
public class KNJD625B {

    private static final Log log = LogFactory.getLog(KNJD625B.class);

    private static final String SEMEALL = "9";
    private static final String SUBCLASSCD_999999 = "999999";

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

    private static <T> List<List<T>> getPageList(final Collection<T> list, final int count) {
        final List<List<T>> rtn = new ArrayList();
        List<T> current = null;
        for (final T t : list) {
            if (null == current || current.size() >= count) {
                current = new ArrayList<T>();
                rtn.add(current);
            }
            current.add(t);
        }
        return rtn;
    }

    private static ScoreDistribution getMappedDistribution(final Map<String, ScoreDistribution> map, final String key) {
        if (null == map.get(key)) {
            map.put(key, new ScoreDistribution());
        }
        return map.get(key);
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {

        final String form = "KNJD625B.frm";
        final String title = _param._nendo + " 度数分布表";
        final String testname = StringUtils.defaultString(_param._gradeName) + " " + StringUtils.defaultString(_param._semesterName) + " " + StringUtils.defaultString(_param._testName);
        final List<ScoreRange> rangeList = getRangeList();
        final int sishagonyuKetaSoten = 1;
        final int sishagonyuKetaHyoka = 2;
        final int histgramKeta = 20;

        final List<Course> courseList = Course.getCourseList(db2, _param);
        for (final Course course : courseList) {

        	final Set<String> subclasscdSet = new TreeSet<String>();
        	for (final Student student : course._students) {
        		subclasscdSet.addAll(student._scoreMap.keySet());
        	}
        	if (subclasscdSet.contains(SUBCLASSCD_999999)) {
        		subclasscdSet.remove(SUBCLASSCD_999999);
        	}
        	
        	final int max = 19;
        	for (final List<String> subclasscdList : getPageList(new ArrayList<String>(subclasscdSet), max)) {
        		svf.VrSetForm(form, 4);
        		
        		log.info(" print page : cdsize = " + subclasscdList);
        		
        		svf.VrsOut("TITLE", title); // タイトル
        		svf.VrsOut("COURSE", testname + " " + StringUtils.defaultString(course._name)); // 学年
        		
        		for (int i = 0; i < subclasscdList.size(); i++) {
        			final String subclasscd = subclasscdList.get(i);
        			if (i == 0 || i == 10) {
            			printHeader(svf);
            			svf.VrEndRecord();
        			}
        			
        			svf.VrsOut("SUBCLASSNAME", _param._subclassnameMap.get(subclasscd)); // 科目名
        			svf.VrsOut("CREDITS", course._creditMap.get(subclasscd)); // 単位名
        			AvgDat avgDat = course._avgDatMap.get(subclasscd);
        			if (null != avgDat) {
        				svf.VrsOut("SCORE_AVG", sishagonyu(avgDat._scoreAvg, sishagonyuKetaSoten)); // 得点平均
        				svf.VrsOut("VALUE_AVG", sishagonyu(avgDat._avg, sishagonyuKetaHyoka)); // 評価平均
        			}
        			final List<String> staffnameList = course.getStaffname(subclasscd, 10);
        			for (int stfi = 0; stfi < Math.min(3, staffnameList.size()); stfi++) {
        				final int line = stfi + 1;
        				svf.VrsOutn("STAFFNAME1", line, staffnameList.get(stfi)); // 職員名
        			}
        			for (int ri = 0; ri < rangeList.size(); ri++) {
        				final int line = ri + 1;
            			final ScoreDistribution dist = ScoreDistribution.getScoreDistribution(subclasscd, course._students, rangeList.get(ri));
        				final int keta = dist.getHistgramKeta(subclasscd, course._students, rangeList, histgramKeta);
        				if (keta > 0) {
        					svf.VrAttributen("HISTGRAM", line, "Paint=(0,70,1),Keta=" + String.valueOf(keta)); // ヒストグラム
        				}
        				svf.VrsOutn("COUNT", line, String.valueOf(dist.getCount())); // 人数
        			}
        			svf.VrEndRecord();
        		}
        		for (int i = subclasscdList.size(); i < max; i++) {
        			if (i == 0 || i == 10) {
            			printHeader(svf);
            			svf.VrEndRecord();
        			}
        			svf.VrsOut("SUBCLASSNAME", "DUMMY1"); // 科目名
        			svf.VrAttribute("SUBCLASSNAME", "X=10000");
        			svf.VrEndRecord();
        		}
        		{
        			final String subclasscd = SUBCLASSCD_999999;
        			svf.VrsOut("SUBCLASSNAME", "総合"); // 科目名
        			AvgDat avgDat = course._avgDatMap.get(subclasscd);
        			if (null != avgDat) {
        				svf.VrsOut("SCORE_AVG", sishagonyu(avgDat._scoreAvg, sishagonyuKetaSoten)); // 得点平均 下1桁
        				svf.VrsOut("VALUE_AVG", sishagonyu(avgDat._avg, sishagonyuKetaHyoka)); // 評価平均 下2桁
        			}
        			for (int ri = 0; ri < rangeList.size(); ri++) {
        				final int line = ri + 1;
            			final ScoreDistribution dist = ScoreDistribution.getScoreDistribution(subclasscd, course._students, rangeList.get(ri));
        				final int keta = dist.getHistgramKeta(subclasscd, course._students, rangeList, histgramKeta);
        				if (keta > 0) {
        					svf.VrAttributen("HISTGRAM", line, "Paint=(0,70,1),Keta=" + String.valueOf(keta)); // ヒストグラム
        				}
        				svf.VrsOutn("COUNT", line, String.valueOf(dist.getCount())); // 人数
        			}
        			svf.VrEndRecord();
        		}
        		_hasData = true;
        	}
        }
    }

	private void printHeader(final Vrw32alp svf) {
		svf.VrsOut("SUBCLASSNAME", "科目名"); // 科目名
		svf.VrAttribute("SUBCLASSNAME", "Hensyu=0"); // 無編集
		svf.VrsOut("CREDITS", "単位数"); // 単位名
		svf.VrAttribute("CREDITS", "Hensyu=0"); // 無編集
		svf.VrsOut("SCORE_AVG", "得点平均"); // 得点平均
		svf.VrAttribute("SCORE_AVG", "Hensyu=0"); // 無編集
		svf.VrsOut("VALUE_AVG", "評価平均"); // 評価平均
		svf.VrAttribute("VALUE_AVG", "Hensyu=0"); // 無編集
		svf.VrsOutn("STAFFNAME1", 2, "担当者"); // 単位名
		svf.VrAttributen("STAFFNAME1", 2, "Hensyu=0"); // 無編集
		final int maxLine3 = 10;
		for (int j = 0; j < maxLine3; j++) {
			final int line = j + 1;
			svf.VrsOutn("COUNT", line, String.valueOf(maxLine3 - j)); // 人数
			svf.VrAttributen("COUNT", line, "Hensyu=0,Size=8.0"); // 無編集
		}
	}
    
	private List<ScoreRange> getRangeList() {
		final List<ScoreRange> rangeList = new ArrayList<ScoreRange>();
        final int max, kizami;
        final boolean is10dankai = true;
        if (is10dankai) {
        	// 10段階 1点きざみ、降順
            max = 10;
            kizami = 1;
        } else {
        	// 100段階 10点きざみ、降順
            max = 100;
            kizami = 10;
        }
        final int min = 0;
        rangeList.add(new ScoreRange(max, 9999));
        for (int i = max; i - kizami >= min; i -= kizami) {
            rangeList.add(new ScoreRange(i - kizami, i));
        }
		return rangeList;
	}

    private static BigDecimal sum(final List<BigDecimal> bdList) {
        if (bdList.size() == 0) {
            return null;
        }
        BigDecimal sum = new BigDecimal(0);
        for (int i = 0; i < bdList.size(); i++) {
            final BigDecimal bd = bdList.get(i);
            sum = sum.add(bd);
        }
        return sum;
    }

    private static String getBdAvgStr(final List<BigDecimal> bdList, final int scale) {
        if (bdList.size() == 0) {
            return null;
        }
        final BigDecimal sum = sum(bdList);
        return sum.divide(new BigDecimal(bdList.size()), scale, BigDecimal.ROUND_HALF_UP).toString();
    }

    private static String sishagonyu(final String s, final int scale) {
        return null == s ? null : new BigDecimal(s).setScale(scale, BigDecimal.ROUND_HALF_UP).toString();
    }

    private static class Course {
        final String _grade;
        final String _coursecd;
        final String _majorcd;
        final String _coursecode;
        final String _name;
        private List<Student> _students;
        private Map<String, AvgDat> _avgDatMap;
        final Map<String, String> _creditMap = new HashMap();
        Map<String, Map<String, String>> _staffMap = new HashMap();
        private String _course;

        public Course(final String grade, final String coursecd, final String majorcd, final String coursecode, final String name) {
            _grade = grade;
            _coursecd = coursecd;
            _majorcd = majorcd;
            _coursecode = coursecode;
            _name = name;
        }
        
        public List<String> getStaffname(final String subclasscd, final int keta) {
        	final List<String> staffnamelist = new ArrayList<String>();
        	if (_staffMap.containsKey(subclasscd)) {
            	for (final Map.Entry<String, String> e : _staffMap.get(subclasscd).entrySet()) {
            		String name = e.getValue();
            		int spcIdx = name.indexOf(' ');
            		if (-1 == spcIdx) {
            			spcIdx = name.indexOf('　');
            		}
            		if (-1 == spcIdx) {
            			spcIdx = name.indexOf('・');
            		}
            		if (0 < spcIdx) {
            			name = name.substring(0, spcIdx);
            		}
            		if (!staffnamelist.contains(name)) {
            			staffnamelist.add(name);
            		}
            	}
        	}
        	final List<String> printLines = new ArrayList();
        	for (final String staffname : staffnamelist) {
        		if (printLines.isEmpty()) {
        			if (!StringUtils.isBlank(staffname)) {
        				printLines.add(staffname);
        			}
        		} else {
        			final String lastLine = printLines.get(printLines.size() - 1);
        			final String attempt = lastLine + "," + StringUtils.defaultString(staffname);
        			if (getMS932ByteLength(attempt) <= keta) {
        				printLines.set(printLines.size() - 1, attempt);
        			} else {
        				printLines.set(printLines.size() - 1, lastLine + ",");
        				printLines.add(StringUtils.defaultString(staffname));
        			}
        		}
        	}
        	return printLines;
        }

        private static Map<String, AvgDat> getAvgDatMap(final DB2UDB db2, final Param param, final Course course) {
            final Map<String, AvgDat> retAvgMap = new HashMap();
            final String sql = getAvgSql(param, course);
            // log.debug(" avg sql =" + sql);
            for (final Map<String, String> row : KnjDbUtils.query(db2, sql)) {
                String subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");
                if (!retAvgMap.containsKey(subclasscd)) {
                	final String cnt = KnjDbUtils.getString(row, "COUNT");
    				retAvgMap.put(subclasscd, new AvgDat(cnt));
                }
                final AvgDat avgDat = retAvgMap.get(subclasscd);
            	final String testCd = KnjDbUtils.getString(row, "TEST_CD");
                final String avg = KnjDbUtils.getString(row, "AVG");
				if (param._scoreTestCd.equals(testCd)) {
                	avgDat._scoreAvg = avg;
                } else {
                    avgDat._avg = avg;
                }
            }
            return retAvgMap;
        }

        private static String getAvgSql(final Param param, final Course course) {
        	final String testkindcd = param._testCd.substring(0, 2);
        	final String testitemcd = param._testCd.substring(2, 4);
        	final String scoreDiv = param._testCd.substring(4);
        	
        	final String scoreTestkindcd = param._scoreTestCd.substring(0, 2);
        	final String scoreTestitemcd = param._scoreTestCd.substring(2, 4);
        	final String scoreScoreDiv = param._scoreTestCd.substring(4);
        	
        	final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("     TESTKINDCD || TESTITEMCD || SCORE_DIV AS TEST_CD, ");
            stb.append("     COUNT, ");
            stb.append("     AVG ");
            stb.append(" FROM ");
            stb.append("     RECORD_AVERAGE_SDIV_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + param._year + "' ");
            stb.append("     AND SEMESTER = '" + param._semester + "' ");
            stb.append("     AND (TESTKINDCD = '" + testkindcd + "' AND TESTITEMCD = '" + testitemcd + "' AND SCORE_DIV = '" + scoreDiv + "' ");
            stb.append("       OR TESTKINDCD = '" + scoreTestkindcd + "' AND TESTITEMCD = '" + scoreTestitemcd + "' AND SCORE_DIV = '" + scoreScoreDiv + "' ) ");
            stb.append("     AND AVG_DIV = '3' "); // 区分 3:コース
            stb.append("     AND GRADE = '" + param._grade + "' ");
            stb.append("     AND COURSECD = '" + course._coursecd + "' AND MAJORCD = '" + course._majorcd + "' AND COURSECODE = '" + course._coursecode + "' ");
            stb.append(" UNION ALL ");
            stb.append(" SELECT ");
            stb.append("     SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("     TESTKINDCD || TESTITEMCD || SCORE_DIV AS TEST_CD, ");
            stb.append("     COUNT(T1.AVG) AS COUNT, ");
            stb.append("     AVG(AVG) AS AVG ");
            stb.append(" FROM ");
            stb.append("     RECORD_RANK_SDIV_DAT T1 ");
            stb.append("     INNER JOIN SCHREG_REGD_DAT REGD ON REGD.SCHREGNO = T1.SCHREGNO ");
            stb.append("         AND REGD.YEAR = '" + param._year + "' ");
            if (SEMEALL.equals(param._semester)) {
                stb.append("     AND T1.SEMESTER = '" + param._ctrlSeme + "' ");
            } else {
                stb.append("     AND T1.SEMESTER = '" + param._semester + "' ");
            }
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.SEMESTER = '" + param._semester + "' ");
            stb.append("     AND (T1.TESTKINDCD = '" + testkindcd + "' AND T1.TESTITEMCD = '" + testitemcd + "' AND T1.SCORE_DIV = '" + scoreDiv + "' ");
            stb.append("       OR T1.TESTKINDCD = '" + scoreTestkindcd + "' AND T1.TESTITEMCD = '" + scoreTestitemcd + "' AND T1.SCORE_DIV = '" + scoreScoreDiv + "' ) ");
            stb.append("     AND REGD.GRADE = '" + param._grade + "' ");
            stb.append("     AND REGD.COURSECD = '" + course._coursecd + "' AND REGD.MAJORCD = '" + course._majorcd + "' AND REGD.COURSECODE = '" + course._coursecode + "' ");
            stb.append("     AND T1.SUBCLASSCD = '" + SUBCLASSCD_999999 + "' ");
            stb.append(" GROUP BY ");
            stb.append("     T1.SUBCLASSCD ");
            stb.append("   , T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV ");
            return stb.toString();
        }

        private static List<Student> getStudentScore(final DB2UDB db2, final Param param, final Course course) {
            final List<Student> studentList = new ArrayList();
            final String sql = getStudentScoreSql(param, course);
//            log.info(" sql = " + sql);
            final Map<String, Student> studentMap = new HashMap();
            for (final Map<String, String> row : KnjDbUtils.query(db2, sql)) {
                final String schregno = KnjDbUtils.getString(row, "SCHREGNO");
                if (null == studentMap.get(schregno)) {
                    final String hrClass = KnjDbUtils.getString(row, "HR_CLASS");
                    final String attendNo = KnjDbUtils.getString(row, "ATTENDNO");
                    final String name = KnjDbUtils.getString(row, "NAME");
                    final Student student = new Student(schregno, hrClass, attendNo, name);
                    studentList.add(student);
                    studentMap.put(schregno, student);
                }
                final String subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");
				if (null != subclasscd) {
	                final String credits = KnjDbUtils.getString(row, "CREDITS");
	                if (NumberUtils.isDigits(credits)) {
	                	course._creditMap.put(subclasscd, credits);
	                }

                    final Student student = studentMap.get(schregno);
                    student._scoreMap.put(subclasscd, sishagonyu(KnjDbUtils.getString(row, "SCORE"), 0));
                }
            }
            return studentList;
        }

        private static String getStudentScoreSql(final Param param, final Course course) {
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
            stb.append("     L1.CLASSCD || '-' || L1.SCHOOL_KIND || '-' || L1.CURRICULUM_CD || '-' || L1.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("     CREM.CREDITS, ");
            stb.append("     L1.SCORE ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT T1 ");
            stb.append("     INNER JOIN SCHREG_BASE_MST BASE ON T1.SCHREGNO = BASE.SCHREGNO ");
            stb.append("     INNER JOIN RECORD_RANK_SDIV_DAT L1 ON T1.YEAR = L1.YEAR ");
            stb.append("          AND L1.SEMESTER = '" + param._semester + "' ");
            stb.append("          AND L1.TESTKINDCD = '" + testkindcd + "' AND L1.TESTITEMCD = '" + testitemcd + "' AND L1.SCORE_DIV = '" + scoreDiv + "' "); // 評価
            stb.append("          AND T1.SCHREGNO = L1.SCHREGNO ");
            stb.append("     LEFT JOIN SUBCLASS_MST SUBM ON SUBM.CLASSCD = L1.CLASSCD ");
            stb.append("         AND SUBM.SCHOOL_KIND = L1.SCHOOL_KIND ");
            stb.append("         AND SUBM.CURRICULUM_CD = L1.CURRICULUM_CD ");
            stb.append("         AND SUBM.SUBCLASSCD = L1.SUBCLASSCD ");
            stb.append("     LEFT JOIN CREDIT_MST CREM ON CREM.YEAR = T1.YEAR ");
            stb.append("         AND CREM.COURSECD = T1.COURSECD ");
            stb.append("         AND CREM.GRADE = T1.GRADE ");
            stb.append("         AND CREM.MAJORCD = T1.MAJORCD ");
            stb.append("         AND CREM.COURSECODE = T1.COURSECODE ");
            stb.append("         AND CREM.CLASSCD = L1.CLASSCD ");
            stb.append("         AND CREM.SCHOOL_KIND = L1.SCHOOL_KIND ");
            stb.append("         AND CREM.CURRICULUM_CD = L1.CURRICULUM_CD ");
            stb.append("         AND CREM.SUBCLASSCD = L1.SUBCLASSCD ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            if (SEMEALL.equals(param._semester)) {
                stb.append("     AND T1.SEMESTER = '" + param._ctrlSeme + "' ");
            } else {
                stb.append("     AND T1.SEMESTER = '" + param._semester + "' ");
            }
            stb.append("     AND T1.GRADE = '" + course._grade + "' ");
            stb.append("     AND T1.COURSECD = '" + course._coursecd + "' ");
            stb.append("     AND T1.MAJORCD = '" + course._majorcd + "' ");
            stb.append("     AND T1.COURSECODE = '" + course._coursecode + "' ");
            stb.append("     AND L1.CLASSCD <= '90' AND SUBM.SUBCLASSCD IS NOT NULL ");
            stb.append(" UNION ALL ");
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.HR_CLASS, ");
            stb.append("     T1.COURSECD || T1.MAJORCD || T1.COURSECODE AS COURSE, ");
            stb.append("     T1.ATTENDNO, ");
            stb.append("     BASE.NAME, ");
            stb.append("     L1.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("     CAST(NULL AS SMALLINT) AS CREDITS, ");
            stb.append("     L1.AVG AS SCORE ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT T1 ");
            stb.append("     INNER JOIN SCHREG_BASE_MST BASE ON T1.SCHREGNO = BASE.SCHREGNO ");
            stb.append("     INNER JOIN RECORD_RANK_SDIV_DAT L1 ON T1.YEAR = L1.YEAR ");
            stb.append("          AND L1.SEMESTER = '" + param._semester + "' ");
            stb.append("          AND L1.TESTKINDCD = '" + testkindcd + "' AND L1.TESTITEMCD = '" + testitemcd + "' AND L1.SCORE_DIV = '" + scoreDiv + "' "); // 評価
            stb.append("          AND T1.SCHREGNO = L1.SCHREGNO ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            if (SEMEALL.equals(param._semester)) {
                stb.append("     AND T1.SEMESTER = '" + param._ctrlSeme + "' ");
            } else {
                stb.append("     AND T1.SEMESTER = '" + param._semester + "' ");
            }
            stb.append("     AND T1.GRADE = '" + course._grade + "' ");
            stb.append("     AND T1.COURSECD = '" + course._coursecd + "' ");
            stb.append("     AND T1.MAJORCD = '" + course._majorcd + "' ");
            stb.append("     AND T1.COURSECODE = '" + course._coursecode + "' ");
            stb.append("     AND L1.SUBCLASSCD = '999999' ");

            return stb.toString();
        }

        private static Map<String, Map<String, String>> getStaffMap(final DB2UDB db2, final Param param, final Course course) {
            final Map<String, Map<String, String>> staffMap = new HashMap();
            final String sql = getStaffSql(param, course);
            // log.debug(" avg sql =" + sql);
            for (final Map<String, String> row : KnjDbUtils.query(db2, sql)) {
                final String subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");
                if (!staffMap.containsKey(subclasscd)) {
    				staffMap.put(subclasscd, new TreeMap<String, String>());
                }
                final String chargedivOrder = "1".equals(KnjDbUtils.getString(row, "CHARGEDIV")) ? "0" : "9";
                final String cd = chargedivOrder + KnjDbUtils.getString(row, "STAFFCD");
                staffMap.get(subclasscd).put(cd, KnjDbUtils.getString(row, "STAFFNAME"));
            }
            return staffMap;
        }

        private static String getStaffSql(final Param param, final Course course) {
        	final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT DISTINCT ");
            stb.append("     T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("     T1.CHARGEDIV, ");
            stb.append("     T1.STAFFCD, ");
            stb.append("     STFM.STAFFNAME ");
            stb.append(" FROM ");
            stb.append("     CHAIR_STF_DAT T1 ");
            stb.append("     INNER JOIN CHAIR_DAT T2 ON T2.YEAR = T1.YEAR ");
            stb.append("         AND T2.SEMESTER = T1.SEMESTER ");
            stb.append("         AND T2.CHAIRCD = T1.CHAIRCD ");
            stb.append("     INNER JOIN CHAIR_STD_DAT T3 ON T3.YEAR = T1.YEAR ");
            stb.append("         AND T3.SEMESTER = T1.SEMESTER ");
            stb.append("         AND T3.CHAIRCD = T1.CHAIRCD ");
            stb.append("     INNER JOIN SCHREG_REGD_DAT REGD ON REGD.SCHREGNO = T3.SCHREGNO ");
            stb.append("         AND REGD.YEAR = T1.YEAR ");
            stb.append("         AND REGD.SEMESTER = T1.SEMESTER ");
            stb.append("         AND REGD.GRADE = '" + course._grade + "' ");
            stb.append("         AND REGD.COURSECD = '" + course._coursecd + "' ");
            stb.append("         AND REGD.MAJORCD = '" + course._majorcd + "' ");
            stb.append("         AND REGD.COURSECODE = '" + course._coursecode + "' ");
            stb.append("     INNER JOIN STAFF_MST STFM ON STFM.STAFFCD = T1.STAFFCD ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            if (!SEMEALL.equals(param._semester)) {
                stb.append("     AND T1.SEMESTER = '" + param._semester + "' ");
            }
            return stb.toString();
        }

        private static List<Course> getCourseList(final DB2UDB db2, final Param param) {
            final List<Course> rtn = new ArrayList<Course>();
            final String sql = getCourseSql(param);
            for (final Map<String, String> row : KnjDbUtils.query(db2, sql)) {
                final Course course = new Course(KnjDbUtils.getString(row, "GRADE"), KnjDbUtils.getString(row, "COURSECD"), KnjDbUtils.getString(row, "MAJORCD"), KnjDbUtils.getString(row, "COURSECODE"), KnjDbUtils.getString(row, "NAME"));
                rtn.add(course);
            }
            for (final Course course : rtn) {
                course._students = Course.getStudentScore(db2, param, course);
                course._avgDatMap = Course.getAvgDatMap(db2, param, course);
                course._staffMap = Course.getStaffMap(db2, param, course);
            }
            return rtn;
        }

        private static String getCourseSql(final Param param) {
        	final String coursecd = param._course.substring(0, 1);
        	final String majorcd = param._course.substring(1, 4);
        	final String coursecode = param._course.substring(4);
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT DISTINCT ");
            stb.append("     T1.GRADE, ");
            stb.append("     T1.COURSECD, ");
            stb.append("     T1.MAJORCD, ");
            stb.append("     T1.COURSECODE, ");
            stb.append("     VALUE(L2.MAJORNAME, '') || VALUE(L3.COURSECODENAME, '') AS NAME ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT T1 ");
            stb.append("     INNER JOIN COURSE_MST L1 ON L1.COURSECD = T1.COURSECD ");
            stb.append("     INNER JOIN MAJOR_MST L2 ON L2.COURSECD = T1.COURSECD AND L2.MAJORCD = T1.MAJORCD ");
            stb.append("     INNER JOIN COURSECODE_MST L3 ON L3.COURSECODE = T1.COURSECODE ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            if ("9".equals(param._semester)) {
                stb.append("     AND T1.SEMESTER = '" + param._ctrlSeme + "' ");
            } else {
                stb.append("     AND T1.SEMESTER = '" + param._semester + "' ");
            }
            stb.append("     AND T1.GRADE = '" + param._grade + "' ");
            stb.append("     AND T1.COURSECD = '" + coursecd + "' ");
            stb.append("     AND T1.MAJORCD = '" + majorcd + "' ");
            stb.append("     AND T1.COURSECODE = '" + coursecode + "' ");
            stb.append(" ORDER BY ");
            stb.append("     T1.GRADE ");
            stb.append("   , T1.COURSECD ");
            stb.append("   , T1.MAJORCD ");
            stb.append("   , T1.COURSECODE ");

            return stb.toString();
        }
    }

    private static class AvgDat {
        final String _count;
        String _avg;
        String _scoreAvg;

        public AvgDat(final String count) {
            _count = count;
        }
    }

    private static class Student {
        final String _schregNo;
        final String _hrClass;
        final String _attendNo;
        final String _name;
        final Map<String, String> _scoreMap = new HashMap();
        final Map<String, String> _testScoreMap = new HashMap();

        public Student (
                final String schregNo,
                final String hrClass,
                final String attendNo,
                final String name
        ) {
            _schregNo = schregNo;
            _hrClass = hrClass;
            _attendNo = attendNo;
            _name = name;
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
        final List<String> _scoreList = new ArrayList();
        public int getCount() {
            return _scoreList.size();
        }
		public int getHistgramKeta(final String subclasscd, final List<Student> students, final List<ScoreRange> scoreRangeList, final int maxKeta) {
			int maxNinzu = 0;
			for (final ScoreRange range : scoreRangeList) {
				final ScoreDistribution sd = getScoreDistribution(subclasscd, students, range);
				maxNinzu = Math.max(maxNinzu, sd.getCount());
			}
			final BigDecimal rate = new BigDecimal(getCount()).divide(new BigDecimal(maxNinzu), 2, BigDecimal.ROUND_DOWN);
			int keta = rate.multiply(new BigDecimal(maxKeta)).setScale(0, BigDecimal.ROUND_DOWN).intValue();
			if (keta == 0 && rate.doubleValue() > 0.0) {
				keta = 1;
			}
			if (keta > 0) {
				log.info(" count : " + getCount() + ", max : " + maxNinzu + ", rate : " + rate + ", keta : " + keta);
			}
			return keta;
		}
	    private static ScoreDistribution getScoreDistribution(final String subclasscd, final List<Student> students, final ScoreRange range) {
	        final ScoreDistribution sd = new ScoreDistribution();
	        for (final Student student : students) {
	            final String score = student._scoreMap.get(subclasscd);
				if (NumberUtils.isNumber(score)) {
	                final int iscore = Integer.parseInt(score);
	                if (range._lowInclusive <= iscore && iscore < range._highExclusive) {
	                	sd._scoreList.add(score);
	                }
	            }
	        }
	        return sd;
	    }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 76103 $");
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
        final String _scoreTestCd;
        final String _testName;
        final String _grade;
        final String _course;
        final String _gradeName;
        final String _useCurriculumcd;
        final Map<String, String> _subclassnameMap;
        final String _useSchool_KindField;
        final String _nendo;
        final String SCHOOLKIND;
        final String SCHOOLCD;

        Param(final DB2UDB db2, final HttpServletRequest request) {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _ctrlSeme = request.getParameter("CTRL_SEME");
            _ctrlDate = request.getParameter("LOGIN_DATE");
            _testCd = request.getParameter("TESTCD");
            if ("1990008".equals(_semester + _testCd)) {
                _scoreTestCd = "020101";
            } else {
                _scoreTestCd = _testCd.substring(0, 4) + "01";
            }
            _grade = request.getParameter("GRADE");
            _course = request.getParameter("COURSE");
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            _nendo = KNJ_EditDate.getAutoFormatYear(db2, Integer.parseInt(_year)) + "年度";
            SCHOOLKIND = request.getParameter("SCHOOLKIND");
            SCHOOLCD = request.getParameter("SCHOOLCD");
            _semesterName = getSemesterName(db2, _year, _semester);
            _testName = getTestName(db2, _year, _semester, _testCd);
            _gradeName = getGradeName(db2, _year, _grade);
            _subclassnameMap = getSubclassName(db2);
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
            sql += "SELECT TESTITEMNAME FROM TESTITEM_MST_COUNTFLG_NEW_SDIV ";
            sql += "WHERE YEAR = '" + year + "' AND SEMESTER = '" + semester + "' AND TESTKINDCD || TESTITEMCD || SCORE_DIV = '" + testCd + "'";
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, sql));
        }

        private String getGradeName(final DB2UDB db2, final String year, final String grade) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT GRADE_NAME1 FROM SCHREG_REGD_GDAT WHERE YEAR = '" + year + "' AND GRADE = '" + grade + "'"));
        }

        private Map getSubclassName(final DB2UDB db2) {
            return KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, "SELECT CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS SUBCLASSCD, VALUE(SUBCLASSABBV, SUBCLASSNAME) AS SUBCLASSNAME FROM SUBCLASS_MST "), "SUBCLASSCD", "SUBCLASSNAME");
        }

    }
}

// eof
