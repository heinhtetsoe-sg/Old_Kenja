/*
 * $Id: 4cb53344d462742a6c64a38afa63ccbc7f4d5d09 $
 *
 * 作成日: 2013/10/10
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 *  学校教育システム 賢者 [入試管理]
 *
 *                  ＜ＫＮＪＬ３１５Ｒ＞  受験コース別得点分布表
 **/
public class KNJL315R {

    private static final Log log = LogFactory.getLog(KNJL315R.class);

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
        final String form = "KNJL315R.frm";
        final int maxLine = 50;

        int totalPage = 0;
        final List entexamCourseList = EntexamCourse.load(db2, _param);
        for (int ci = 0; ci < entexamCourseList.size(); ci++) {
            final EntexamCourse course = (EntexamCourse) entexamCourseList.get(ci);

            final String sPerfect = (String) _param._coursePerfectMap.get(course._coursecd + course._majorcd + course._examcoursecd);
            final int perfect = 90 + (NumberUtils.isNumber(sPerfect) ? Integer.parseInt(sPerfect) : 0);

            final boolean[] naidaku = new boolean[] {true, false}; // 内諾あり・なし（左右）
            for (int i = 0; i < naidaku.length; i++) {
                course._scoreListArray[i] = ScoreDist.getScoreList(db2, _param, course._desirediv, naidaku[i], true);
                course._scoreDistListArray[i] = ScoreDist.getScoreDistList(perfect, course._scoreListArray[i]);
                course._pages[i] = course._scoreDistListArray[i].size() / maxLine + (course._scoreDistListArray[i].size() % maxLine != 0 ? 1 : 0);
                course._hiJukenShaSu[i] = ScoreDist.getScoreList(db2, _param, course._desirediv, naidaku[i], false).size();
            }
            course._coursePage = Math.max(1, Math.max(course._pages[0], course._pages[1]));
            totalPage += course._coursePage;
        }

        int cp = 0;
        for (int ci = 0; ci < entexamCourseList.size(); ci++) {
            final EntexamCourse course = (EntexamCourse) entexamCourseList.get(ci);

            final int[] total = new int[2];

            for (int pi = 0; pi < course._coursePage; pi++) {
                svf.VrSetForm(form, 1);
                svf.VrsOut("TITLE", KenjaProperties.gengou(Integer.parseInt(_param._entexamyear)) + "年度　" + _param._testdivName + "得点分布表"); // タイトル
                svf.VrsOut("DATE", _param._dateStr); // 印刷日
                svf.VrsOut("PAGE1", String.valueOf(cp + pi + 1)); // ページ
                svf.VrsOut("PAGE2", String.valueOf(totalPage)); // ページ

                final String sPerfect = (String) _param._coursePerfectMap.get(course._coursecd + course._majorcd + course._examcoursecd);
                final int perfect = 90 + (NumberUtils.isNumber(sPerfect) ? Integer.parseInt(sPerfect) : 0);

                final boolean[] naidaku = new boolean[] {true, false}; // 内諾あり・なし（左右）
                for (int i = 0; i < naidaku.length; i++) {
                    final List scoreDistList = course.getScoreList(i, pi, maxLine);
                    final String k = String.valueOf(i + 1);
                    svf.VrsOut("HOPE_KIND" + k, course.getTitle(_param, naidaku[i])); // 専願区分
                    for (int j = 0; j < scoreDistList.size(); j++) {
                        final int line = j + 1;
                        final ScoreDist scoreDist = (ScoreDist) scoreDistList.get(j);
                        final int count = scoreDist._scoreList.size();

                        if (perfect == scoreDist._lower) {
                            svf.VrsOutn("HIGH" + k, line, String.valueOf(scoreDist._lower)); // 点数
                        } else {
                            svf.VrsOutn("HIGH" + k, line, String.valueOf(scoreDist._upper)); // 点数
                            svf.VrsOutn("FROM" + k, line, "〜"); // 〜
                            svf.VrsOutn("LOW" + k, line, String.valueOf(scoreDist._lower)); // 点数
                        }
                        svf.VrsOutn("NUM" + k, line, String.valueOf(count)); // 人数
                        total[i] += count;
                        svf.VrsOutn("SUBTOTAL" + k, line, String.valueOf(total[i])); // 累計
                    }
                    if (course.isLastP(i, pi, maxLine)) {
                        svf.VrsOut("JUKENSHA" + k, "(受験者数)");
                        svf.VrsOut("TOTAL" + k, String.valueOf(course._scoreListArray[i].size())); // 受験者数
                        svf.VrsOut("HIJUKENSHA" + k, "(未受験者数)");
                        svf.VrsOut("TOTAL" + k + "_2", String.valueOf(course._hiJukenShaSu[i])); // 未受験者数
                    }
                }
                svf.VrEndPage();
                _hasData = true;
            }

            cp += course._coursePage;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 63854 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    private static class ScoreDist {
        final int _upper;
        final int _lower;
        final List _scoreList;
        ScoreDist(final int upper, final int lower) {
            _upper = upper;
            _lower = lower;
            _scoreList = new ArrayList();
        }

        public static List getScoreDistList(final int perfect, final List scoreList) {
            final int[] maxmin = getMaxMin(perfect, scoreList);
            final int max = maxmin[0];
            final int min = maxmin[1];
            final List list = new ArrayList();
            if (max < 0 || min < 0) {
                return list;
            }
            final int kizami = 5;
            for (int s = max; s >= min; s -= kizami) {
                final int upper = s + 4;
                final int lower = s;
                ScoreDist dist = new ScoreDist(upper, lower);
                list.add(dist);
                for (final Iterator it = scoreList.iterator(); it.hasNext();) {
                    final String sscore = (String) it.next();
                    if (!NumberUtils.isDigits(sscore)) {
                        continue;
                    }
                    final Integer score = Integer.valueOf(sscore);
                    if (lower <= score.intValue() && score.intValue() <= upper) {
                        dist._scoreList.add(score);
                    }
                }
            }
            return list;
        }

        private static int[] getMaxMin(final int perfect, final List scoreList) {
            if (scoreList.isEmpty()) {
                return new int[] {-1, -1};
            }
            int max = 0;
            int min = perfect;
            for (final Iterator it = scoreList.iterator(); it.hasNext();) {
                final String sscore = (String) it.next();
                if (!NumberUtils.isDigits(sscore)) {
                    continue;
                }
                final Integer score = Integer.valueOf(sscore);
                max = Math.max(max, score.intValue());
                min = Math.min(min, score.intValue());
            }
            max = Math.min(max + 10, perfect);
            min = Math.max(min - 10, 0);
            max = max / 5 * 5;
            min = min / 5 * 5;
            return new int[] {max, min};
        }

        public static List getScoreList(final DB2UDB db2, final Param param, final String desirediv, final boolean naidaku, final boolean juken) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = sql(param, desirediv, naidaku, juken);
                // log.debug(" juken = " + juken + ", sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    list.add(rs.getString("TOTAL4"));
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }

        public static String sql(final Param param, final String desirediv, final boolean naidaku, final boolean juken) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.TOTAL4 ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_RECEPT_DAT T1 ");
            stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE ON BASE.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
            stb.append("         AND BASE.EXAMNO = T1.EXAMNO ");
            stb.append("         AND BASE.APPLICANTDIV = T1.APPLICANTDIV ");
            stb.append("         AND BASE.TESTDIV = T1.TESTDIV ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT T2 ON T2.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
            stb.append("         AND T2.APPLICANTDIV = BASE.APPLICANTDIV ");
            stb.append("         AND T2.EXAMNO       = BASE.EXAMNO ");
            stb.append("         AND T2.SEQ          = '002' ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANT_BEFORE_DAT BEF ON BEF.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
            stb.append("         AND BEF.APPLICANTDIV = BASE.APPLICANTDIV ");
            stb.append("         AND BEF.TESTDIV = BASE.TESTDIV ");
            stb.append("         AND BEF.BEFORE_PAGE = T2.REMARK1 ");
            stb.append("         AND BEF.BEFORE_SEQ = T2.REMARK2 ");
            stb.append("     LEFT JOIN ENTEXAM_COURSE_MST T14 ON T14.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
            stb.append("         AND T14.APPLICANTDIV = BASE.APPLICANTDIV ");
            stb.append("         AND T14.TESTDIV = BASE.TESTDIV ");
            stb.append("         AND T14.COURSECD = BEF.BEFORE_COURSECD ");
            stb.append("         AND T14.MAJORCD = BEF.BEFORE_MAJORCD ");
            stb.append("         AND T14.EXAMCOURSECD = BEF.BEFORE_EXAMCOURSECD ");
            stb.append(" WHERE ");
            stb.append("     T1.ENTEXAMYEAR = '" + param._entexamyear + "' ");
            stb.append("     AND T1.APPLICANTDIV = '" + param._applicantdiv + "' ");
            stb.append("     AND T1.TESTDIV = '" + param._testdiv + "' ");
            if (juken) {
                stb.append("     AND T1.TOTAL4 IS NOT NULL ");
            } else {
                stb.append("     AND T1.TOTAL4 IS NULL ");
            }
            stb.append("     AND BASE.DESIREDIV = '" + desirediv + "' ");
            if (naidaku) {
                stb.append("     AND BEF.BEFORE_EXAMCOURSECD IS NOT NULL ");
            } else {
                stb.append("     AND BEF.BEFORE_EXAMCOURSECD IS NULL ");
            }
            return stb.toString();
        }
    }

    private static class EntexamCourse {
        final String _desirediv;
        final String _coursecd;
        final String _majorcd;
        final String _examcoursecd;
        final List _examcourseMarkList;

        List[] _scoreListArray = new List[2];
        List[] _scoreDistListArray = new List[2];
        int[] _hiJukenShaSu = new int[2];
        int[] _pages = new int[2];
        int _coursePage;

        EntexamCourse(
            final String desirediv,
            final String coursecd,
            final String majorcd,
            final String examcoursecd
        ) {
            _desirediv = desirediv;
            _coursecd = coursecd;
            _majorcd = majorcd;
            _examcoursecd = examcoursecd;
            _examcourseMarkList = new ArrayList();
        }

        public boolean isLastP(int i, int pi, int maxLine) {
            final List list = _scoreDistListArray[i];
            if (list.size() == 0 && pi == 0) {
                return true;
            }
            if (pi * maxLine < list.size() && list.size() <= (pi + 1) * maxLine) {
                return true;
            }
            return false;
        }

        public List getScoreList(int i, int pi, int maxLine) {
            final List list = _scoreDistListArray[i];
            if (pi * maxLine > list.size()) {
                return Collections.EMPTY_LIST;
            }
            return list.subList(pi * maxLine, Math.min(list.size(), (pi + 1) * maxLine));
        }

        public String getTitle(final Param param, boolean naidaku) {
            final StringBuffer marks = new StringBuffer();
            String comma = "";
            for (final Iterator it = _examcourseMarkList.iterator(); it.hasNext();) {
                marks.append(comma).append(it.next());
                comma = ",";
            }
            if (naidaku) {
                final String last = (String) _examcourseMarkList.get(_examcourseMarkList.size() - 1);
                return param._testdivAbbv3 + "あり（" + last + "内諾" + marks.toString() + "受験）";
            } else {
                return param._testdivAbbv3 + "なし（" + marks.toString() + "受験）";
            }
        }

        static EntexamCourse getCourse(final List list, final String desirediv) {
            for (final Iterator it = list.iterator(); it.hasNext();) {
                final EntexamCourse c = (EntexamCourse) it.next();
                if (c._desirediv.equals(desirediv)) {
                    return c;
                }
            }
            return null;
        }

        public static List load(final DB2UDB db2, final Param param) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql(param));
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String desirediv = rs.getString("DESIREDIV");
                    if (null == getCourse(list, desirediv)) {
                        final String coursecd = rs.getString("COURSECD");
                        final String majorcd = rs.getString("MAJORCD");
                        final String examcoursecd = rs.getString("EXAMCOURSECD");
                        final EntexamCourse entexamcourse = new EntexamCourse(desirediv, coursecd, majorcd, examcoursecd);
                        list.add(entexamcourse);
                    }
                    getCourse(list, desirediv)._examcourseMarkList.add(rs.getString("EXAMCOURSE_MARK"));
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }

        public static String sql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.DESIREDIV, T1.COURSECD, T1.MAJORCD, T1.EXAMCOURSECD, T1.WISHNO, T2.EXAMCOURSE_MARK ");
            stb.append(" FROM ENTEXAM_WISHDIV_MST T1 ");
            stb.append(" LEFT JOIN ENTEXAM_COURSE_MST T2 ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
            stb.append("     AND T2.APPLICANTDIV = T1.APPLICANTDIV ");
            stb.append("     AND T2.TESTDIV = T1.TESTDIV ");
            stb.append("     AND T2.COURSECD = T1.COURSECD ");
            stb.append("     AND T2.MAJORCD = T1.MAJORCD ");
            stb.append("     AND T2.EXAMCOURSECD = T1.EXAMCOURSECD ");
            stb.append(" WHERE ");
            stb.append("     T1.ENTEXAMYEAR = '" + param._entexamyear + "' ");
            stb.append("     AND T1.APPLICANTDIV = '" + param._applicantdiv + "' ");
            stb.append("     AND T1.TESTDIV = '" + param._testdiv + "' ");
            stb.append(" ORDER BY ");
            stb.append("     T1.DESIREDIV, T1.COURSECD, T1.MAJORCD, T1.EXAMCOURSECD, T1.WISHNO ");
            return stb.toString();
        }
    }

    /** パラメータクラス */
    private static class Param {
        final String _entexamyear;
        final String _applicantdiv;
        final String _testdiv;
        final String _date;

        final String _applicantdivName;
        final String _testdivName;
        final String _testdivAbbv3;
        final String _dateStr;
        final Map _coursePerfectMap;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _applicantdiv = request.getParameter("APPLICANTDIV");
            _testdiv = request.getParameter("TESTDIV");
            _date = request.getParameter("CTRL_DATE");
            _dateStr = getDateStr(_date);

            _applicantdivName = getNameMst(db2, "NAME1", "L003", _applicantdiv);
            _testdivName = getNameMst(db2, "NAME1", "L004", _testdiv);
            _testdivAbbv3 = getNameMst(db2, "ABBV3", "L004", _testdiv);
            _coursePerfectMap = getExamCoursecdPerfectMap(db2);
        }

        private String getDateStr(final String date) {
            final Calendar cal = Calendar.getInstance();
            final DecimalFormat df = new DecimalFormat("00");
            final int hour = cal.get(Calendar.HOUR_OF_DAY);
            final int minute = cal.get(Calendar.MINUTE);
            return KNJ_EditDate.h_format_JP(date) + "　" + df.format(hour) + "時" + df.format(minute) + "分現在";
        }

        private String getExamCourseName(final DB2UDB db2, final String field, final String examcoursecd) {
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                StringBuffer sql = new StringBuffer();
                sql.append(" SELECT " + field + " ");
                sql.append(" FROM ENTEXAM_COURSE_MST ");
                sql.append(" WHERE ENTEXAMYEAR = '" + _entexamyear + "' ");
                sql.append("   AND APPLICANTDIV = '" + _applicantdiv + "' ");
                sql.append("   AND TESTDIV = '" + _testdiv + "' ");
                sql.append("   AND EXAMCOURSECD = '" + examcoursecd + "' ");
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString(field);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return rtn;
        }

        private Map getExamCoursecdPerfectMap(final DB2UDB db2) {
            final Map rtn = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                StringBuffer sql = new StringBuffer();
                sql.append(" SELECT ");
                sql.append("     T2.COURSECD, T2.MAJORCD, T2.EXAMCOURSECD, SUM(T2.PERFECT) AS PERFECT ");
                sql.append(" FROM V_NAME_MST T1 ");
                sql.append(" INNER JOIN ENTEXAM_PERFECT_MST T2 ON T2.ENTEXAMYEAR = T1.YEAR ");
                sql.append("     AND T2.APPLICANTDIV = '" + _applicantdiv + "' ");
                sql.append("     AND T2.TESTDIV = '" + _testdiv + "' ");
                sql.append("     AND T1.NAMECD2 = T2.TESTSUBCLASSCD ");
                sql.append(" WHERE ");
                sql.append("     T1.YEAR = '" + _entexamyear + "' ");
                sql.append("     AND T1.NAMECD1 = 'L009' ");
                if ("1".equals(_testdiv)) {
                    sql.append("     AND NAMESPARE2 = '1' ");
                } else if ("2".equals(_testdiv)) {
                    sql.append("     AND NAMESPARE3 = '1' ");
                }
                sql.append(" GROUP BY ");
                sql.append("     T2.COURSECD, T2.MAJORCD, T2.EXAMCOURSECD ");
                sql.append(" ORDER BY ");
                sql.append("     T2.COURSECD, T2.MAJORCD, T2.EXAMCOURSECD ");
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn.put(rs.getString("COURSECD") + rs.getString("MAJORCD") + rs.getString("EXAMCOURSECD"), rs.getString("PERFECT"));
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return rtn;
        }

        private static String getNameMst(final DB2UDB db2, final String field, final String namecd1, final String namecd2) {
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT " + field + " FROM NAME_MST WHERE NAMECD1 = '" + namecd1 + "' AND NAMECD2 = '" + namecd2 + "' ");
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString(field);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return rtn;
        }
    }
}

// eof

