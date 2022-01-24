// kanji=漢字
/*
 * $Id: d16ff6396cc034b787c0cff19c4ba2ca8dfe3072 $
 */
package servletpack.KNJD;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 * 学校教育システム 賢者 [成績管理]  成績通知票
 */
public class KNJD624H {

    private static final Log log = LogFactory.getLog(KNJD624H.class);

    private boolean _hasData;
    private Param _param;

    private static final String AVG_GRADE = "1";

    private static final String AVG_DIV_GRADE = "GRADE";

    private static final String[] HYOUKA_BUNPU = {"10","9","8","7","6","5","4","3","2","1"};
    private static final String[] HYOUKA_BUNPU_J = {"5","4","3","2","1","","","","",""};

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

            //生徒用の場合の出力チェック
            boolean flg = false;
            for (final Iterator it = _param._subclassnameMap.keySet().iterator(); it.hasNext();) {
                final String subclassCd = (String) it.next();
                final String subclassName = (String) _param._subclassnameMap.get(subclassCd);
                final String staffName = (String) _param._subclassStaffMap.get(subclassCd);
                final String total = (String) _param._subclassTotalMap.get(subclassCd);
                final Subclass subclass = new Subclass(subclassCd, subclassName, staffName, total);

                if("2".equals(_param._radio) && Integer.parseInt(subclass._total) < 15) {
                    //15人未満の科目は印字しない
                    continue;
                }
                flg = true;
                break;
            }

            if(flg == true) printMain(db2, svf);

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

    private static ScoreDistribution getMappedDistribution(final Map map, final String key) {
        if (null == map.get(key)) {
            map.put(key, new ScoreDistribution());
        }
        return (ScoreDistribution) map.get(key);
    }


    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        final int MAX_LINE = 30;

        int line = 1;
        for (final Iterator it = _param._subclassnameMap.keySet().iterator(); it.hasNext();) {

            if(line == 1 || line > MAX_LINE) {
                if(line > MAX_LINE) svf.VrEndPage();
                //ヘッダー
                printHeader(db2, svf);
                line=1;
            }

            final String subclassCd = (String) it.next();
            final String subclassName = (String) _param._subclassnameMap.get(subclassCd);
            final String staffName = (String) _param._subclassStaffMap.get(subclassCd);
            final String total = (String) _param._subclassTotalMap.get(subclassCd);
            final Subclass subclass = new Subclass(subclassCd, subclassName, staffName, total);
            // subclass.setPerfect(db2, _param);
            subclass._highPerfect = "100";
            subclass._lowPerfect = "100";

            if("2".equals(_param._radio) && Integer.parseInt(subclass._total) < 15) {
                //生徒用の場合、15人未満の科目は印字しない
                continue;
            }

            //科目名称
            if(KNJ_EditEdit.getMS932ByteLength(subclass._subclassName) > 20) {
                final String[] val = KNJ_EditEdit.get_token(subclass._subclassName, 20, 2);
                svf.VrsOutn("SUBCLASS_NAME2_1", line, val[0]);
                svf.VrsOutn("SUBCLASS_NAME2_2", line, val[1]);
            } else {
                svf.VrsOutn("SUBCLASS_NAME1", line, subclass._subclassName);
            }

            //担当名
            final String trName = subclass._staffName;
            if(KNJ_EditEdit.getMS932ByteLength(trName) > 18) {
                final String[] val = KNJ_EditEdit.get_token(trName, 18, 2);
                svf.VrsOutn("TR_NAME2_1", line, val[0]);
                svf.VrsOutn("TR_NAME2_2", line, val[1]);
            } else {
                svf.VrsOutn("TR_NAME1", line, subclass._staffName);
            }
            svf.VrsOutn("PERFECT", line, subclass.getPrintPerfect(_param)); // 満点

            //分布表
            final List printHrListAll = Grade.getPrintHrList(db2, _param, subclass);
            printLastPageDist(svf, subclass, printHrListAll, printHrListAll, line);
            line++;
        }
        svf.VrEndPage();
    }


    private void printHeader(final DB2UDB db2, final Vrw32alp svf) {
        //ヘッダー
        final String form = (_param._testKbn) ? "KNJD624H_2.frm" : "KNJD624H_1.frm";
        final String title = KNJ_EditDate.getAutoFormatYear(db2, Integer.parseInt(_param._year)) + "年度 " + _param._testName + "得点分布 " + _param._gradeName;
        final String select = ("1".equals(_param._radio)) ? "教員用" : "生徒用";

        svf.VrSetForm(form, 1);
        svf.VrsOut("TITLE", title);
        svf.VrsOut("SELECT", select);
        svf.VrsOut("DATE", _param._ctrlDate.replace('-', '/'));

        final boolean hyoukaFlg = "08".equals(_param._testCd.substring(4)); //08:評価 09:評定 それ以外:得点
        final String distName = (!_param._testKbn) ? "得点分布" : hyoukaFlg ? "評価分布" : "評定分布";
        svf.VrsOut("DIST_NAME", distName); //分布名称

        String[] bunpuName = ("J".equals(_param._schoolKind)) ? HYOUKA_BUNPU_J : HYOUKA_BUNPU;
        if (_param._testKbn) {
            for(int idx = 0; idx < bunpuName.length; idx++) {
                final String field = "DIV_NAME"+(idx+1);
                svf.VrsOut(field, bunpuName[idx]);
            }
        }
    }

    private void printLastPageDist(final Vrw32alp svf, final Subclass subclass, final List printDistHrList, final List printHrListAll, final int line) {

        final List rangeList = new ArrayList();
        final int max, kizami, min;

        if (_param._testKbn) {
            //評価・評定
        	if("J".equals(_param._schoolKind)) {
                // 5段階
                max = 5;
                kizami = 1;
                min = 1;
        	} else {
                // 10段階
                max = 10;
                kizami = 1;
                min = 1;
        	}
        } else {
            //考査
            // 100段階 10点きざみ(100～90,89～80,,,,9～0)
            max = 100;
            kizami = 10;
            min = 0;
        }
        final int addMax = (_param._testKbn) ? max : max + 1 ;
        rangeList.add(new ScoreRange(addMax, 999));
        for (int i = max; i - kizami >= min; i -= kizami) {
            rangeList.add(new ScoreRange(i - kizami, i));
        }

        //'0'埋め
        for (int ri = 0; ri < rangeList.size(); ri++) {
            final int rangeLine = (_param._testKbn) ? ri + 1 : ri;
            svf.VrsOutn("NUM" + rangeLine, line, "0");
            svf.VrsOutn("AVE", line, "0");
        }

        //合計の作成
        final Map hrDistMap = new HashMap();
        final ScoreDistribution total = new ScoreDistribution();
        for (final Iterator ith = printHrListAll.iterator(); ith.hasNext();) {
            final Grade hrClass = (Grade) ith.next();
            int distLine = 1;
            for (final Iterator itd = rangeList.iterator(); itd.hasNext();) {
                final ScoreRange sr = (ScoreRange) itd.next();
                final ScoreDistribution dist = getScoreDistribution(_param, subclass, hrClass._students, sr);
                total.add(dist);
                final ScoreDistribution hrDist =  getMappedDistribution(hrDistMap,  String.valueOf(distLine));
                hrDist.add(dist);
                distLine++;
            }
        }

        //分布表
        int totalScore = 0;
        for (int hri = 0; hri < printDistHrList.size(); hri++) {
            final Grade grade = (Grade) printDistHrList.get(hri);

            for (int ri = 0; ri < rangeList.size(); ri++) {
                final ScoreRange sr = (ScoreRange) rangeList.get(ri);
                final int rangeLine = (_param._testKbn) ? ri + 1 : ri;
                final ScoreDistribution dist = getScoreDistribution(_param, subclass, grade._students, sr);
                svf.VrsOutn("NUM" + rangeLine, line, String.valueOf(dist.totalCount()));
                totalScore += dist.totalCount();
                _hasData = true;
            }
        }

        //平均
        if (total._totalScore.size() > 0) {
            final String avg = getBdAvgStr(total._totalScore, 1);
//            BigDecimal avg = new BigDecimal(totalScore / total._totalScore.size());
//            avg = avg.setScale(1, BigDecimal.ROUND_HALF_UP);
            svf.VrsOutn("AVE", line, avg.toString());
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

    private static ScoreDistribution getScoreDistribution(final Param param, final Subclass subclass, final List students, final ScoreRange range) {
        final ScoreDistribution sd = new ScoreDistribution();
        for (final Iterator itr = students.iterator(); itr.hasNext();) {
            final Student student = (Student) itr.next();
            if (null != student._scoreMap.get(subclass._subclassCd)) {
                final int score = Integer.parseInt((String) student._scoreMap.get(subclass._subclassCd));
                if(param._testKbn) {
                    if (range._lowInclusive == score) {
                        sd._totalScore.add(new BigDecimal(score));
                    }
                } else {
                	//100点も含める
                	final int high = (range._highExclusive == 100) ? range._highExclusive+1 : range._highExclusive;
                    if (range._lowInclusive <= score && score < high) {
                        sd._totalScore.add(new BigDecimal(score));
                    }
                }
            }
        }
        return sd;
    }

    private static class Grade {
        final String _grade;
        private List _students;
        private Map _avgDatMap;
        private String _course;

        public Grade(
                final String grade
        ) {
            _grade = grade;
        }

        private static Map getAvgDat(
                final DB2UDB db2,
                final Param param,
                final Subclass subclass
        ) {
            final Map retAvgMap = new HashMap();
            final String sql = getAvgSql(param, subclass);
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

        private static String getAvgSql(final Param param, final Subclass subclass) {
            final String testkindcd = param._testCd.substring(0, 2);
            final String testitemcd = param._testCd.substring(2, 4);
            final String scoreDiv = param._testCd.substring(4);
            final String[] split = null == subclass ? new String[] {"", "", "", ""} : StringUtils.split(subclass._subclassCd, "-");

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
            return stb.toString();
        }

        private static List getStudents(
                final DB2UDB db2,
                final Param param,
                final String grade,
                final Subclass subclass
        ) {
            final List studentList = new ArrayList();
            final String sql = getStudentsSql(param, grade, subclass);
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

        private static String getStudentsSql(final Param param, final String grade, final Subclass subclass) {
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
            stb.append("     AND T1.GRADE = '" + grade + "' ");
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
                    final Grade hrClass = new Grade(rs.getString("GRADE"));
                    rtn.add(hrClass);
                }
            } catch (Exception e) {
                log.fatal("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            for (final Iterator it = rtn.iterator(); it.hasNext();) {
                final Grade hrClass = (Grade) it.next();
                hrClass._students = Grade.getStudents(db2, param, hrClass._grade, subclass);
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
                hrClass._avgDatMap = Grade.getAvgDat(db2, param, subclass);
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
        final String _attendNo;
        final String _name;
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
        final List _totalScore = new ArrayList();
        public void add(ScoreDistribution dist) {
            _totalScore.addAll(dist._totalScore);
        }
        public int totalCount() {
//            return _dansiScore.size() + _jyosiScore.size() + _soreigaiScore.size();
              return _totalScore.size();
        }
    }

    private static class Subclass {
        private final String _subclassCd;
        private final String _subclassName;
        private final String _staffName;
        private final String _total;
        private String _highPerfect;
        private String _lowPerfect;

        public Subclass(
                final String subclassCd,
                final String subclassName,
                final String staffName,
                final String total
        ) {
            _subclassCd = subclassCd;
            _subclassName = subclassName;
            _staffName = staffName;
            _total = total;
        }

        public String getPrintPerfect(final Param param) {
            if (param._testKbn) {
                //評価・評定
            	if("J".equals(param._schoolKind)) {
                    return "5";
            	} else {
                    return "10";
            	}
            }
            //考査
            return _highPerfect.equals(_lowPerfect) ? _highPerfect : _lowPerfect + "\uFF5E" + _highPerfect;
        }
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
        log.fatal("$Revision: 74672 $");
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
        final boolean _testKbn; //TRUE:評価、評定 FALSE:考査
        final String _testName;
        final String _grade;
        final String _radio; // 1:科目ごとにすべてのHRを表示 2:HRごとにすべての科目を表示
        final String _gradeName;
        final String _schoolKind;
        final String _useCurriculumcd;
        Map _subclassnameMap = new TreeMap();
        Map _subclassStaffMap = new TreeMap();
        Map _subclassTotalMap = new TreeMap();
        final boolean _isPrintMajorAvg;
        final String _useSchool_KindField;
        final String SCHOOLKIND;
        final String SCHOOLCD;

        Param(final DB2UDB db2, final HttpServletRequest request) {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _ctrlSeme = request.getParameter("CTRL_SEME");
            _ctrlDate = request.getParameter("LOGIN_DATE");
            _testCd = request.getParameter("TESTCD");
            _testKbn = ("08".equals(_testCd.substring(4)) || "09".equals(_testCd.substring(4))); //08or09=評価、評定 それ以外=考査
            _grade = request.getParameter("GRADE");
            _radio = request.getParameter("RADIO");
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            SCHOOLKIND = request.getParameter("SCHOOLKIND");
            SCHOOLCD = request.getParameter("SCHOOLCD");
            _semesterName = getSemesterName(db2, _year, _semester);
            _testName = getTestName(db2, _year, _semester, _testCd);
            _gradeName = getGradeName(db2, _year, _grade);
            _schoolKind = getSchoolKind(db2);
            _subclassnameMap = getSubclassName(db2);
            _subclassStaffMap = getSubclassStaff(db2);
            _subclassTotalMap = getSubclassTotal(db2);

            final String z010 = setZ010Name1(db2);
            log.info(" z010 name1 = " + z010);
            _isPrintMajorAvg = "sundaikoufu".equals(z010);
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

        private String getSchoolKind(final DB2UDB db2) {
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND GRADE = '" + _grade + "' ");

            return KnjDbUtils.getOne(KnjDbUtils.query(db2, sql.toString()));
        }

        private Map getSubclassName(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map map = new TreeMap();
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT DISTINCT ");
                stb.append("   T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ");
                stb.append("   T1.SUBCLASSNAME ");
                stb.append(" FROM ");
                stb.append("   SUBCLASS_MST T1");
                stb.append("   INNER JOIN SUBCLASS_YDAT T2 ");
                stb.append("           ON T2.YEAR          = '"+ _year +"' ");
                stb.append("          AND T2.CLASSCD       = T1.CLASSCD ");
                stb.append("          AND T2.SCHOOL_KIND   = T1.SCHOOL_KIND ");
                stb.append("          AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
                stb.append("          AND T2.SUBCLASSCD    = T1.SUBCLASSCD ");
                stb.append("   INNER JOIN RECORD_RANK_SDIV_DAT T3 ");
                stb.append("           ON T3.YEAR          = '"+ _year +"' ");
                stb.append("          AND T3.SEMESTER      = '"+ _semester +"' ");
                stb.append("          AND T3.TESTKINDCD || T3.TESTITEMCD || T3.SCORE_DIV = '"+ _testCd +"' ");
                stb.append("          AND T3.CLASSCD       = T1.CLASSCD ");
                stb.append("          AND T3.SCHOOL_KIND   = T1.SCHOOL_KIND ");
                stb.append("          AND T3.CURRICULUM_CD = T1.CURRICULUM_CD ");
                stb.append("          AND T3.SUBCLASSCD    = T1.SUBCLASSCD ");
                stb.append("          AND T3.SCHREGNO     IN (SELECT REGD.SCHREGNO ");
                stb.append("                                    FROM SCHREG_REGD_DAT REGD ");
                stb.append("                                   WHERE REGD.YEAR     = '"+ _year +"' ");
                if ("9".equals(_semester)) {
                	stb.append("                                     AND REGD.SEMESTER = '"+ _ctrlSeme +"' ");
                } else {
                	stb.append("                                     AND REGD.SEMESTER = '"+ _semester +"' ");
                }
                stb.append("                                     AND REGD.GRADE    = '"+ _grade +"' ) ");
                stb.append("ORDER BY SUBCLASSCD ");
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String subclassCd  = StringUtils.defaultString(rs.getString("SUBCLASSCD"));
                    final String name = StringUtils.defaultString(rs.getString("SUBCLASSNAME"));
                    map.put(subclassCd, name);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
            return map;
        }

        private Map getSubclassStaff(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map map = new TreeMap();
            try {
                final StringBuffer sql = new StringBuffer();
                sql.append(" SELECT ");
                sql.append("   T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ");
                sql.append("   T1.SUBCLASSNAME, ");
                sql.append("   T2.CHAIRCD, ");
                sql.append("   T3.STAFFCD, ");
                sql.append("   T4.STAFFNAME_SHOW ");
                sql.append(" FROM ");
                sql.append("   SUBCLASS_MST T1 ");
                sql.append("   LEFT JOIN CHAIR_DAT T2 ");
                sql.append("           ON T2.YEAR     = '"+_year+"' ");
                if ("9".equals(_semester)) {
                	sql.append("          AND T2.SEMESTER = '"+ _ctrlSeme +"' ");
                } else {
                	sql.append("          AND T2.SEMESTER = '"+ _semester +"' ");
                }
                sql.append("          AND T2.CLASSCD       = T1.CLASSCD ");
                sql.append("          AND T2.SCHOOL_KIND   = T1.SCHOOL_KIND ");
                sql.append("          AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
                sql.append("          AND T2.SUBCLASSCD    = T1.SUBCLASSCD ");
                sql.append("   LEFT JOIN CHAIR_STF_DAT T3 ");
                sql.append("           ON T3.YEAR     = T2.YEAR ");
                sql.append("          AND T3.SEMESTER = T2.SEMESTER ");
                sql.append("          AND T3.CHAIRCD  = T2.CHAIRCD ");
                sql.append("   LEFT JOIN STAFF_MST T4 ");
                sql.append("           ON T4.STAFFCD = T3.STAFFCD ");
                sql.append("ORDER BY SUBCLASSCD ");
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                String defSubclassCd = "";
                final StringBuffer staffName = new StringBuffer();
                String cone = "";
                while (rs.next()) {
                    final String subclassCd  = StringUtils.defaultString(rs.getString("SUBCLASSCD"));
                    if(!"".equals(defSubclassCd ) && !subclassCd .equals(defSubclassCd )) {
                        if (!map.containsKey(defSubclassCd)) {
                            map.put(defSubclassCd, staffName.toString());
                            staffName.setLength(0); //担当名を初期化
                            cone = "";
                        }
                    }
                    //担当名
                    String name = StringUtils.defaultString(rs.getString("STAFFNAME_SHOW"));
                    if(!"".equals(name)) {
                        int idx = name.indexOf(" ");
                        if(idx > 0) {
                            //姓のみ
                            name = name.substring(0,idx);
                        }
                        staffName.append(cone);
                        staffName.append(name.toString());
                        cone = " ";
                    }

                    defSubclassCd  = subclassCd ;
                }
                //最終科目
                if (!map.containsKey(defSubclassCd)) map.put(defSubclassCd, staffName.toString());
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
            return map;
        }

        private Map getSubclassTotal(final DB2UDB db2) {
            final String testkindcd = _testCd.substring(0, 2);
            final String testitemcd = _testCd.substring(2, 4);
            final String scoreDiv = _testCd.substring(4);
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map map = new TreeMap();
            try {
                final StringBuffer sql = new StringBuffer();
                sql.append(" SELECT ");
                sql.append("   T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ");
                sql.append("   COUNT(T3.SCHREGNO) AS TOTAL_COUNT ");
                sql.append(" FROM ");
                sql.append("   SUBCLASS_MST T1 ");
                sql.append("   INNER JOIN SCHREG_REGD_DAT T2 ");
                sql.append("           ON T2.YEAR     = '"+_year+"' ");
                if ("9".equals(_semester)) {
                    sql.append("          AND T2.SEMESTER      = '"+ _ctrlSeme +"' ");
                } else {
                    sql.append("          AND T2.SEMESTER      = '"+ _semester +"' ");
                }
                sql.append("          AND T2.GRADE    = '"+_grade+"' ");
                sql.append("   INNER JOIN RECORD_RANK_SDIV_DAT T3 ");
                sql.append("            ON T3.YEAR = T2.YEAR ");
                sql.append("           AND T3.SEMESTER = '"+ _semester +"' ");
                sql.append("           AND T3.TESTKINDCD = '" + testkindcd + "' ");
                sql.append("           AND T3.TESTITEMCD = '" + testitemcd + "' ");
                sql.append("           AND T3.SCORE_DIV = '" + scoreDiv + "' ");
                sql.append("           AND T3.SCHREGNO = T2.SCHREGNO ");
                sql.append("           AND T3.CLASSCD <= '90' ");
                sql.append("           AND T3.CLASSCD       = T1.CLASSCD ");
                sql.append("           AND T3.SCHOOL_KIND   = T1.SCHOOL_KIND ");
                sql.append("           AND T3.CURRICULUM_CD = T1.CURRICULUM_CD ");
                sql.append("           AND T3.SUBCLASSCD    = T1.SUBCLASSCD ");
                sql.append(" GROUP BY ");
                sql.append("   T1.CLASSCD, ");
                sql.append("   T1.SCHOOL_KIND, ");
                sql.append("   T1.CURRICULUM_CD, ");
                sql.append("   T1.SUBCLASSCD ");
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String subclassCd  = StringUtils.defaultString(rs.getString("SUBCLASSCD"));
                    final String count = StringUtils.defaultString(rs.getString("TOTAL_COUNT"));
                    map.put(subclassCd, count);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
            return map;
        }



    }
}

// eof
