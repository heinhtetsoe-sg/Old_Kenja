// kanji=漢字
/*
 * $Id: f5f659cccfc3c9edacccbcfdb41103ceaef37b9c $
 */
package servletpack.KNJD;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;

/**
 * 学校教育システム 賢者 [成績管理]  成績通知票
 */
public class KNJD624Q {

    private static final Log log = LogFactory.getLog(KNJD624Q.class);

    private boolean _hasData;
    private Param _param;

    private static final String SUBCLASSCD_555555 = "555555";
    private static final String SUBCLASSCD_999999 = "999999";
    final String NAI = "2"; // 内小 -> 内生
    final String GAI = "3"; // 内中 -> 外生

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

    private static List getRowList(final DB2UDB db2, final String sql) {
        final List list = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            log.info(" sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            ResultSetMetaData meta = rs.getMetaData();
            while (rs.next()) {
                final Map m = new HashMap();
                for (int i = 1; i <= meta.getColumnCount(); i++) {
                    m.put(meta.getColumnName(i), rs.getString(meta.getColumnName(i)));
                }
                list.add(m);
            }
        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return list;
    }

    private static String getString(final Map m, final String field) {
        if (null == m) {
            return null;
        }
        if (!m.containsKey(field)) {
            throw new IllegalArgumentException("not defined: " + field + " in " + m.keySet());
        }
        return (String) m.get(field);
    }

    private static BigDecimal sum(final List scoreList) {
        if (null == scoreList || scoreList.size() == 0) {
            return null;
        }
        BigDecimal bd = new BigDecimal(0);
        int count = 0;
        for (final Iterator it = scoreList.iterator(); it.hasNext();) {
            final String score = (String) it.next();
            if (NumberUtils.isNumber(score)) {
                bd = bd.add(new BigDecimal(score));
                count += 1;
            }
        }
        if (count == 0) {
            return null;
        }
        return bd;
    }

    private static String avg(final List scoreList) {
        final BigDecimal sum = sum(scoreList);
        if (null == sum) {
            return null;
        }
        final String rtn = sum.divide(new BigDecimal(scoreList.size()), 1, BigDecimal.ROUND_HALF_UP).toString();
        return rtn;
    }

    private static String stddev(final List scoreList) {
        final BigDecimal sum = sum(scoreList);
        if (null == sum) {
            return null;
        }
        final int count = scoreList.size();
        final BigDecimal avg = sum.divide(new BigDecimal(count), 5, BigDecimal.ROUND_HALF_UP);

        BigDecimal sigmaSum = new BigDecimal(0);
        for (final Iterator it = scoreList.iterator(); it.hasNext();) {
            final String score = (String) it.next();
            sigmaSum = sigmaSum.add(new BigDecimal(Math.pow(Double.parseDouble(score) - avg.doubleValue(), 2)));
        }
        final double sigma = Math.sqrt(sigmaSum.doubleValue() / count);
        return new BigDecimal(sigma).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {

        svf.VrSetForm("KNJD624Q.frm", 1);

        final int maxHr = 4;
        final int distLines = 12;
        final List subclassList = Subclass.getSubclassList(db2, _param);

        Subclass subclass9 = null;
        Subclass subclass9Avg = null;
        Subclass subclass5 = null;
        Subclass subclass5Avg = null;
        for (final Iterator it = subclassList.iterator(); it.hasNext();) {
            final Subclass subclass = (Subclass) it.next();
            if ((SUBCLASSCD_999999).equals(subclass._subclasscd)) {
                subclass9 = subclass;
                it.remove();
            } else if ((SUBCLASSCD_999999 + "AVG").equals(subclass._subclasscd)) {
                subclass9Avg = subclass;
                it.remove();
            } else if (SUBCLASSCD_555555.equals(subclass._subclasscd)) {
                subclass5 = subclass;
                it.remove();
            } else if ((SUBCLASSCD_555555 + "AVG").equals(subclass._subclasscd)) {
                subclass5Avg = subclass;
                it.remove();
            }
        }
        final int subclassCount = Math.min(subclassList.size(), 9);

        final List scoreRangeList = new ArrayList();
        final int max = 100;
        int rscore = max;
        scoreRangeList.add(new ScoreRange(max, max + 1)); // 100 <= score < 101
        for (; rscore > 0; rscore -= 10) {
            scoreRangeList.add(new ScoreRange(rscore - 10, rscore));
        }
        scoreRangeList.add(new ScoreRange(0, max + 1)); // 合計 0 <= score < 101
//        log.debug(" scoreRangeList size = " + scoreRangeList.size() + " / " + scoreRangeList);

        final String testname = StringUtils.defaultString(_param._gradeName) + "学年　" + StringUtils.defaultString(_param._semesterName) + StringUtils.defaultString(_param._testName);
        svf.VrsOut("TITLE", testname + "成績一覧分布表"); // タイトル

        for (int hri = 0; hri < Math.min(_param._hrclassList.size(), maxHr); hri++) {
            final Hrclass hr = (Hrclass) _param._hrclassList.get(hri);
            final String shri = String.valueOf(hri + 1);
            for (int sbi = 0; sbi < subclassCount; sbi++) {
                final String sbline = String.valueOf(sbi + 1);
                svf.VrsOut("HR_NAME" + sbline + "_" + shri, hr._hrClassName1); // クラス名
            }
            svf.VrsOut("HR_NAME10_" + shri, hr._hrClassName1); // クラス名
            for (int si = 0; si < _param._sexList.size(); si++) {
                final Map m = (Map) _param._sexList.get(si);
                final String hrnamesexname = StringUtils.defaultString(hr._hrClassName1) + StringUtils.defaultString(getString(m, "NAME"));
                if (si == 0) {
                    svf.VrsOut("HR_NAME10_" + shri + "FEMALE", hrnamesexname); // クラス名女子
                } else if (si == 1) {
                    svf.VrsOut("HR_NAME10_" + shri + "MALE", hrnamesexname); // クラス名男子
                }
            }
        }

        for (int sbi = 0; sbi < subclassCount; sbi++) {
            final Subclass subclass = (Subclass) subclassList.get(sbi);
            final String sbline = String.valueOf(sbi + 1);
            svf.VrsOut("CLASS1_" + sbline, subclass._subclassabbv); // 教科名
            svf.VrsOut("CLASS2_" + sbline, subclass._subclassabbv); // 教科名
        }

        for (int di = 0; di < distLines; di++) {
            final int line = di + 1;
            final ScoreRange r = (ScoreRange) scoreRangeList.get(di);
            for (int sbi = 0; sbi < subclassCount; sbi++) {
                final Subclass subclass = (Subclass) subclassList.get(sbi);
                final String sbline = String.valueOf(sbi + 1);

                final List gakunen = new ArrayList();
                for (int hri = 0; hri < Math.min(_param._hrclassList.size(), maxHr); hri++) {
                    final Hrclass hr = (Hrclass) _param._hrclassList.get(hri);
                    final List nenkumi = subclass.getTargetStudentList(hr._grade, hr._hrClass, null, null, r._low, r._high);
                    svf.VrsOutn("NUM" + sbline + "_" + String.valueOf(hri + 1), line, count(nenkumi)); // 得点人数
                    gakunen.addAll(nenkumi);
                }
                svf.VrsOutn("CLASS_NUM" + sbline, line, count(gakunen)); // 教科人数
            }
        }

        // 9教科目の右
        final String[] totals = {"9", "5"};
        for (int di = 0; di < distLines; di++) {
            final int line = di + 1;
            final ScoreRange r = (ScoreRange) scoreRangeList.get(di);
            for (int ti = 0; ti < totals.length; ti++) {
                Subclass subclass = null;
                if ("9".equals(totals[ti])) {
                    subclass = subclass9Avg;
                } else if ("5".equals(totals[ti])) {
                    subclass = subclass5Avg;
                }
                if (null != subclass) {
                    final List danshi = subclass.getTargetStudentList(null, null, "1", null, r._low, r._high);
                    final List jyoshi = subclass.getTargetStudentList(null, null, "2", null, r._low, r._high);
                    svf.VrsOutn(totals[ti] + "NUM1_1", line, count(danshi)); // 得点人数 男子
                    svf.VrsOutn(totals[ti] + "NUM1_2", line, count(jyoshi)); // 得点人数 女子
                    svf.VrsOutn(totals[ti] + "CLASS_NUM1", line, count(addAll(danshi, jyoshi))); // 教科人数
                }
            }
        }

        // クラス・教科別平均点
        for (int sbi = 0; sbi < subclassCount; sbi++) {
            final String sbline = String.valueOf(sbi + 1);
            final Subclass subclass = (Subclass) subclassList.get(sbi);
            printAvg(svf, subclass, maxHr, "AVE_SCORE" + sbline);
        }

        printAvg(svf, subclass9, maxHr, "9AVE_TOTAL1"); // 9科合計
        printAvg(svf, subclass9Avg, maxHr, "9AVE_TOTAL2"); // 9科平均
        printAvg(svf, subclass5, maxHr, "5AVE_TOTAL1"); // 5科合計
        printAvg(svf, subclass5Avg, maxHr, "5AVE_TOTAL2"); // 5科平均

        // 内外別平均点
        for (int di = 0; di < distLines; di++) {
            final ScoreRange r = (ScoreRange) scoreRangeList.get(di);
            final int line = di + 1;
            if (null != subclass9Avg) {
                final List naishin = subclass9Avg.getTargetStudentList(null, null, null, NAI, r._low, r._high);
                final List gaishin = subclass9Avg.getTargetStudentList(null, null, null, GAI, r._low, r._high);
                svf.VrsOutn("9SCORE2_1", line, count(addAll(naishin, gaishin))); // 得点人数
                svf.VrsOutn("9SCORE2_2", line, count(naishin)); // 得点人数 内進
                svf.VrsOutn("9SCORE2_3", line, count(gaishin)); // 得点人数 外進
            }
            if (null != subclass5Avg) {
                final List naishin = subclass5Avg.getTargetStudentList(null, null, null, NAI, r._low, r._high);
                final List gaishin = subclass5Avg.getTargetStudentList(null, null, null, GAI, r._low, r._high);
                svf.VrsOutn("5SCORE2_1", line, count(addAll(naishin, gaishin))); // 得点人数
                svf.VrsOutn("5SCORE2_2", line, count(naishin)); // 得点人数 内進
                svf.VrsOutn("5SCORE2_3", line, count(gaishin)); // 得点人数 外進
            }
        }
        _hasData = true;
        svf.VrEndPage();
    }

    public List addAll(final List list1, final List list2) {
        final List total = new ArrayList();
        total.addAll(list1);
        total.addAll(list2);
        return total;
    }

    private String count(final List list) {
        return String.valueOf(list.size());
    }

    public void printAvg(final Vrw32alp svf, final Subclass subclass, final int maxHr, final String field) {
        if (null == subclass) {
            return;
        }
        for (int hri = 0; hri < Math.min(_param._hrclassList.size(), maxHr); hri++) {
            final Hrclass hr = (Hrclass) _param._hrclassList.get(hri);
            final int hrline = hri * 3;

            final List nenkumiAll = subclass.getTargetStudentList(hr._grade, hr._hrClass, null, null, null, null);
            final List nenkumiDanshi = subclass.getTargetStudentList(hr._grade, hr._hrClass, "1", null, null, null);
            final List nenkumiJyoshi = subclass.getTargetStudentList(hr._grade, hr._hrClass, "2", null, null, null);

            //log.info(" " + subclass._subclasscd + "  hr (" + hr._grade + hr._hrClass + ") = " + nenkumiAll);

            svf.VrsOutn(field, hrline + 1, avg(nenkumiAll)); // 平均
            svf.VrsOutn(field, hrline + 2, avg(nenkumiDanshi)); // 平均 男子
            svf.VrsOutn(field, hrline + 3, avg(nenkumiJyoshi)); // 平均 女子
        }
        final List gakunenAll = subclass.getTargetStudentList(null, null, null, null, null, null);
        String val = "";
        for (int j = 0; j < 6; j++) {
            final int line = j + 1;
            switch (line) {
            case 1: // 全体
                val = avg(gakunenAll);
                break;
            case 2: // 男子
                val = avg(subclass.getTargetStudentList(null, null, "1", null, null, null));
                break;
            case 3: // 女子
                val = avg(subclass.getTargetStudentList(null, null, "2", null, null, null));
                break;
            case 4: // 内進
                val = avg(subclass.getTargetStudentList(null, null, null, NAI, null, null));
                break;
            case 5: // 外進
                val = avg(subclass.getTargetStudentList(null, null, null, GAI, null, null));
                break;
            case 6: // 全体 標準偏差
                if (subclass._subclasscd.endsWith("AVG")) {
                    // 平均点は標準偏差を表示しない
                    val = "";
                } else {
                    val = stddev(gakunenAll);
                }
                break;
            }
            svf.VrsOutn(field, maxHr * 3 + line, val); // 平均
        }
    }

    private static class Hrclass {
        final String _grade;
        final String _hrClass;
        final String _hrClassName1;
        Hrclass(
                final String grade,
                final String hrClass,
                final String hrClassName1
        ) {
            _grade = grade;
            _hrClass = hrClass;
            _hrClassName1 = hrClassName1;
        }
    }

    private static class Student {
        final String _schregno;
        final String _grade;
        final String _hrClass;
        final String _inoutcd;
        final String _sex;
        final Map _scoreMap = new HashMap();

        Student(
            final String schregno,
            final String grade,
            final String hrClass,
            final String inoutcd,
            final String sex
        ) {
            _schregno = schregno;
            _grade = grade;
            _hrClass = hrClass;
            _inoutcd = inoutcd;
            _sex = sex;
        }
    }

    private static class Subclass {
        final String _classcd;
        final String _subclasscd;
        String _subclassabbv;

        final List _studentList = new ArrayList();

        Subclass(final String classcd, final String subclasscd) {
            _classcd = classcd;
            _subclasscd = subclasscd;
        }

        /**
         * 指定対象の生徒を取得
         * @param grade 指定学年。指定しない場合はnull
         * @param hrClass 指定クラス。指定しない場合はnull
         * @param sex 指定性別。指定しない場合はnull
         * @param inoutcd 指定内外フラグ。指定しない場合はnull
         * @param scoreMin 得点範囲下限(値を含む)。指定しない場合はnull
         * @param scoreMax 得点範囲上限(値を含まない)。指定しない場合はnull
         * @return 指定対象の生徒を取得
         */
        public List getTargetStudentList(final String grade, final String hrClass, final String sex, final String inoutcd, final Integer scoreMin, final Integer scoreMax) {
            final List targetStudentList = new ArrayList();
            for (final Iterator it = _studentList.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                if (null != grade && !grade.equals(student._grade)) {
                    continue; // 指定の値と不一致なら対象外
                }
                if (null != hrClass && !hrClass.equals(student._hrClass)) {
                    continue; // 指定の値と不一致なら対象外
                }
                if (null != sex && !sex.equals(student._sex)) {
                    continue; // 指定の値と不一致なら対象外
                }
                if (null != inoutcd && !inoutcd.equals(student._inoutcd)) {
                    continue; // 指定の値と不一致なら対象外
                }
                final String scoreStr = (String) student._scoreMap.get(_subclasscd);
                if (!NumberUtils.isNumber(scoreStr)) {
                    continue;
                }
                final int score;
                if (NumberUtils.isDigits(scoreStr)) {
                    score = Integer.parseInt(scoreStr);
                } else {
                    score = new BigDecimal(scoreStr).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
                }
                if (null != scoreMin) {
                    if (scoreMin.intValue() > score) { // 境界値を含む
                        continue;
                    }
                }
                if (null != scoreMax) {
                    if (score >= scoreMax.intValue()) { // 境界値を含まない
                        continue;
                    }
                }
                targetStudentList.add(scoreStr);
            }
//            log.info(" subclasscd = " + _subclasscd + "| grade:hrclass:sex:inout = " + grade + ":" + hrClass + ":" + sex + ":" + inoutcd + " / (min, max) = (" + scoreMin + ", " + scoreMax + ") -> [" + targetStudentList.size() + " / " + _studentList.size() + "] { " + targetStudentList + "}");
            return targetStudentList;
        }

        public static List getSubclassList(final DB2UDB db2, final Param param) {
            final List subclassList = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final Map subclassMap = new HashMap();
                final Map studentMap = new HashMap();
                final String sql = sql(param);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {

                    final String classcd = rs.getString("CLASSCD");
                    final String subclasscd = rs.getString("SUBCLASSCD");
                    if (null == subclassMap.get(subclasscd)) {
                        final Subclass subclass = new Subclass(classcd, subclasscd);
                        subclass._subclassabbv = (String) param._subclassnameMap.get(subclasscd);
                        subclassMap.put(subclasscd, subclass);
                        subclassList.add(subclass);
                    }
                    final Subclass subclass = (Subclass) subclassMap.get(subclasscd);

                    final String schregno = rs.getString("SCHREGNO");
                    if (null == studentMap.get(schregno)) {
                        final String grade = rs.getString("GRADE");
                        final String hrClass = rs.getString("HR_CLASS");
                        final String inoutcd = rs.getString("INOUTCD");
                        final String sex = rs.getString("SEX");
                        final Student student = new Student(schregno, grade, hrClass, inoutcd, sex);
                        studentMap.put(schregno, student);
                    }
                    final Student student = (Student) studentMap.get(schregno);
                    subclass._studentList.add(student);

                    final String score = rs.getString("SCORE");
                    student._scoreMap.put(subclasscd, score);

                    if (SUBCLASSCD_555555.equals(subclasscd) || SUBCLASSCD_999999.equals(subclasscd)) {

                        final String classcdAvg = classcd + "AVG";
                        final String subclasscdAvg = subclasscd + "AVG";
                        if (null == subclassMap.get(subclasscdAvg)) {
                            final Subclass subclassAvg = new Subclass(classcdAvg, subclasscdAvg);
                            subclassMap.put(subclasscdAvg, subclassAvg);
                            subclassList.add(subclassAvg);
                        }
                        final Subclass subclassAvg = (Subclass) subclassMap.get(subclasscdAvg);

                        subclassAvg._studentList.add(student);

                        final String avg = rs.getString("AVG");
                        student._scoreMap.put(subclasscdAvg, avg);
                    }
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return subclassList;
        }

        public static String sql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("   T1.SCHREGNO ");
            stb.append("   , T1.CLASSCD || '-' || T1.SCHOOL_KIND AS CLASSCD ");
            stb.append("   , CASE WHEN T1.SUBCLASSCD IN ('555555', '999999') THEN ");
            stb.append("         T1.SUBCLASSCD ");
            stb.append("     ELSE ");
            stb.append("         T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD ");
            stb.append("     END AS SUBCLASSCD ");
            stb.append("   , T1.SCORE ");
            stb.append("   , T1.AVG ");
            stb.append("   , REGD.GRADE ");
            stb.append("   , REGD.HR_CLASS ");
            stb.append("   , T3.INOUTCD ");
            stb.append("   , T3.SEX ");
            stb.append(" FROM ");
            stb.append("     RECORD_RANK_SDIV_DAT T1 ");
            stb.append("     INNER JOIN SCHREG_REGD_DAT REGD ON REGD.SCHREGNO = T1.SCHREGNO ");
            stb.append("        AND REGD.YEAR = T1.YEAR ");
            stb.append("        AND REGD.SEMESTER = '" + ("9".equals(param._semester) ? param._ctrlSeme : param._semester) + "' ");
            stb.append("     INNER JOIN SCHREG_BASE_MST T3 ON T3.SCHREGNO = T1.SCHREGNO ");
            stb.append("     LEFT JOIN SUBCLASS_MST T4 ON T4.CLASSCD = T1.CLASSCD ");
            stb.append("         AND T4.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("         AND T4.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("         AND T4.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("     LEFT JOIN REC_SUBCLASS_GROUP_DAT TSUBG3 ON TSUBG3.YEAR = REGD.YEAR ");
            stb.append("         AND TSUBG3.GROUP_DIV = '3' ");
            stb.append("         AND TSUBG3.GRADE = REGD.GRADE ");
            stb.append("         AND TSUBG3.COURSECD = REGD.COURSECD ");
            stb.append("         AND TSUBG3.MAJORCD = REGD.MAJORCD ");
            stb.append("         AND TSUBG3.COURSECODE = REGD.COURSECODE ");
            stb.append("         AND TSUBG3.CLASSCD = T1.CLASSCD ");
            stb.append("         AND TSUBG3.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("         AND TSUBG3.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("         AND TSUBG3.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("     LEFT JOIN REC_SUBCLASS_GROUP_DAT TSUBG5 ON TSUBG5.YEAR = REGD.YEAR ");
            stb.append("         AND TSUBG5.GROUP_DIV = '5' ");
            stb.append("         AND TSUBG5.GRADE = REGD.GRADE ");
            stb.append("         AND TSUBG5.COURSECD = REGD.COURSECD ");
            stb.append("         AND TSUBG5.MAJORCD = REGD.MAJORCD ");
            stb.append("         AND TSUBG5.COURSECODE = REGD.COURSECODE ");
            stb.append("         AND TSUBG5.CLASSCD = T1.CLASSCD ");
            stb.append("         AND TSUBG5.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("         AND TSUBG5.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("         AND TSUBG5.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("    AND T1.SEMESTER = '" + param._semester + "' ");
            stb.append("    AND T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '" + param._testCd + "' ");
            stb.append("    AND REGD.GRADE = '" + param._grade + "' ");
            stb.append("    AND (T4.SUBCLASSCD IS NOT NULL OR T1.SUBCLASSCD = '555555' OR T1.SUBCLASSCD = '999999') ");
            stb.append("    AND T1.SCORE IS NOT NULL ");
            stb.append(" ORDER BY ");
            stb.append("    CASE WHEN TSUBG3.YEAR IS NOT NULL OR TSUBG5.YEAR IS NOT NULL THEN '0' ELSE '1' END ");
            stb.append("    , T4.SUBCLASSCD ");
            return stb.toString();
        }
    }

    private static class ScoreRange {
        final Integer _high;
        final Integer _low;
        public ScoreRange(final int lowInclusive, final int highExclusive) {
            _high = new Integer(highExclusive);
            _low = new Integer(lowInclusive);
        }
        public String toString() {
            return "ScoreRange(" + _low + " <= x < " + _high + ")";
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 57481 $");
        KNJServletUtils.debugParam(request, log);
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
        final String _gradeName;
        final List _hrclassList;
        final List _sexList;
        final Map _clazznameMap;
        final Map _subclassnameMap;
        final String _major;
        final String _use_school_detail_gcm_dat;
        final String _useSchool_KindField;
        final String SCHOOLKIND;
        final String SCHOOLCD;
        final String _use_prg_schoolkind;
        final String _selectSchoolKind;

        Param(final DB2UDB db2, final HttpServletRequest request) {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _ctrlSeme = request.getParameter("CTRL_SEME");
            _ctrlDate = request.getParameter("LOGIN_DATE");
            _testCd = request.getParameter("TESTCD");
            _grade = request.getParameter("GRADE");

            _major = request.getParameter("MAJOR");
            _use_school_detail_gcm_dat = request.getParameter("use_school_detail_gcm_dat");
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            SCHOOLCD = request.getParameter("SCHOOLCD");
            _use_prg_schoolkind = request.getParameter("use_prg_schoolkind");
            _selectSchoolKind = request.getParameter("selectSchoolKind");
            SCHOOLKIND = StringUtils.defaultString(getSchregRegdGdat(db2, "SCHOOL_KIND"));
            _semesterName = getSemesterName(db2, _year, _semester);
            _testName = getTestName(db2, _year, _semester, _testCd);
            _gradeName = getGradeName(db2, _year, _grade);
            _clazznameMap = getClazzNameMap(db2);
            _subclassnameMap = getSubclassNameMap(db2);

            final String regdSemester = "9".equals(_semester) ? _ctrlSeme : _semester;
            _hrclassList = getGradeHrclassList(db2, _year, regdSemester, _grade);
            _sexList = getSexList(db2);
        }

        /**
         * 名称マスタ NAMECD1='Z010' NAMECD2='00'読込
         */
        private String setZ010Name1(DB2UDB db2) {
            String name1 = null;
            final String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' ";
            for (final Iterator it = getRowList(db2, sql).iterator(); it.hasNext();) {
                final Map row = (Map) it.next();
                name1 = getString(row, "NAME1");
            }
            return name1;
        }

        private String getSemesterName(final DB2UDB db2, final String year, final String semester) {
            String rtn = null;
            final String sql = "SELECT SEMESTERNAME FROM SEMESTER_MST WHERE YEAR = '" + year + "' AND SEMESTER = '" + semester + "'";
            for (final Iterator it = getRowList(db2, sql).iterator(); it.hasNext();) {
                final Map row = (Map) it.next();
                rtn = getString(row, "SEMESTERNAME");
            }
            return rtn;
        }

        private String getSchregRegdGdat(final DB2UDB db2, final String field) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            String rtn = null;
            try{
                final StringBuffer stb = new StringBuffer();
                stb.append("SELECT  ");
                stb.append("        " + field + " ");
                stb.append("FROM    SCHREG_REGD_GDAT T1 ");
                stb.append("WHERE   T1.YEAR = '" + _year + "' ");
                stb.append(    "AND T1.GRADE = '" + _grade + "' ");
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    rtn = rs.getString(field);
                }
            } catch (Exception ex) {
                log.error("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }

        private String getTestName(final DB2UDB db2, final String year, final String semester, final String testCd) {
            String rtn = null;
            String sql = "";
            if ("1".equals(_use_school_detail_gcm_dat)) {
                sql = "SELECT TESTITEMNAME FROM TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV WHERE YEAR = '" + _year + "'" +
                        " AND SEMESTER = '" + _semester + "' " +
                        " AND GRADE = '00' " +
                        " AND COURSECD || '-' || MAJORCD = '" + _major + "' " +
                        " AND TESTKINDCD || TESTITEMCD || SCORE_DIV = '" + testCd + "' ";
                if ("1".equals(_use_prg_schoolkind)) {
                    sql += " AND SCHOOLCD = '" + SCHOOLCD + "' " +
                            " AND SCHOOL_KIND = '" + SCHOOLKIND + "' ";
                } else if ("1".equals(_useSchool_KindField) && !StringUtils.isBlank(SCHOOLKIND)) {
                    sql += " AND SCHOOLCD = '" + SCHOOLCD + "' " +
                           " AND SCHOOL_KIND = '" + SCHOOLKIND + "' ";
                }
            } else {
                sql += "SELECT TESTITEMNAME FROM TESTITEM_MST_COUNTFLG_NEW_SDIV ";
                sql += "WHERE YEAR = '" + year + "' AND SEMESTER = '" + semester + "' AND TESTKINDCD || TESTITEMCD || SCORE_DIV = '" + testCd + "'";
            }
            for (final Iterator it = getRowList(db2, sql).iterator(); it.hasNext();) {
                final Map row = (Map) it.next();
                rtn = getString(row, "TESTITEMNAME");
            }
            return rtn;
        }

        private String getGradeName(final DB2UDB db2, final String year, final String grade) {
            String rtn = null;
            final String sql = "SELECT GRADE_NAME1 FROM SCHREG_REGD_GDAT WHERE YEAR = '" + year + "' AND GRADE = '" + grade + "'";
            for (final Iterator it = getRowList(db2, sql).iterator(); it.hasNext();) {
                final Map row = (Map) it.next();
                rtn = getString(row, "GRADE_NAME1");
            }
            return rtn;
        }

        private List getGradeHrclassList(final DB2UDB db2, final String year, final String semester, final String grade) {
            List rtn = new ArrayList();
            final String sql = "SELECT GRADE, HR_CLASS, HR_CLASS_NAME1 FROM SCHREG_REGD_HDAT WHERE YEAR = '" + year + "' AND SEMESTER = '" + semester + "' AND GRADE = '" + grade + "' ORDER BY HR_CLASS ";
            for (final Iterator it = getRowList(db2, sql).iterator(); it.hasNext();) {
                final Map row = (Map) it.next();
                rtn.add(new Hrclass(getString(row, "GRADE"), getString(row, "HR_CLASS"), getString(row, "HR_CLASS_NAME1")));
            }
            return rtn;
        }

        private List getSexList(final DB2UDB db2) {
            final String sql = "SELECT NAMECD2 AS SEX, VALUE(ABBV1, '') || '子' AS NAME FROM NAME_MST WHERE NAMECD1 = 'Z002' ORDER BY NAMESPARE1, NAMECD2 ";
            return getRowList(db2, sql);
        }

//        private List getInoutcdList(final DB2UDB db2) {
//            final String sql = "SELECT NAMECD2 AS SEX, NAME1 AS NAME FROM NAME_MST WHERE NAMECD1 = 'A001' ORDER BY NAMECD2 ";
//            return getRowList(db2, sql);
//        }

        private Map getClazzNameMap(final DB2UDB db2) {
            Map rtn = new HashMap();
            final String sql = "SELECT CLASSCD || '-' || SCHOOL_KIND AS CLASSCD, CLASSNAME FROM CLASS_MST ";
            for (final Iterator it = getRowList(db2, sql).iterator(); it.hasNext();) {
                final Map row = (Map) it.next();
                rtn.put(getString(row, "CLASSCD"), getString(row, "CLASSNAME"));
            }
            return rtn;
        }

        private Map getSubclassNameMap(final DB2UDB db2) {
            Map rtn = new HashMap();
            final String sql = "SELECT T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, T1.SUBCLASSABBV FROM SUBCLASS_MST T1 ";
            for (final Iterator it = getRowList(db2, sql).iterator(); it.hasNext();) {
                final Map row = (Map) it.next();
                rtn.put(getString(row, "SUBCLASSCD"), getString(row, "SUBCLASSABBV"));
            }
            return rtn;
        }
    }
}

// eof
