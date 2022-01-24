/*
 * 作成日: 2021/02/08
 * 作成者: shimoji
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;

public class KNJD301B {

    private static final Log log = LogFactory.getLog(KNJD301B.class);

    private boolean _hasData;

    private Param _param;

    private static final int LINE_MAX = 49;
    private static final int GAKUNENBETSU_HEIKINTEN_LINE_MAX = 51;
    private static final int BUKATSUDOUBETSU_SEISEKI_ICHIRANHYO_LINE_MAX = 21;
    private static final String SCORE_DIV_RAW_SCORE = "01"; // 01:素点
    private static final String SCORE_DIV_RESULTS = "08"; // 08:成績
    private static final String SCORE_DIV_RATING = "09"; // 09:評定
    private static final String SEME_ALL = "9"; // 9:学年末
    private static final String SCORE_DIV_KIMATSU_RESULTS = "9900" + SCORE_DIV_RESULTS; // 990008:期末成績
    private static final String SCORE_DIV_KIMATSU_RATING = "9900" + SCORE_DIV_RATING; // 990009:期末評定
    /**
     * 成績一覧表（_reportKind = 3）以外が選択された場合、もしくは、
     * 学年末以外が選択されており、かつ、テスト種別の大分類（_testKindの先頭２桁）が 01 の場合。
     * 指定されたテスト種別の素点を出力する。
     */
    private static final String P1_PRTTEST_SPECIFY = "1";
    /**
     * 成績一覧表（_reportKind = 3）が選択されており、
     * かつ、学年末（_semester = 9）以外が選択、
     * かつ、テスト種別の大分類（_testKind の先頭２桁）が 02 の場合は、
     * 指定されたテスト種別の素点と、期末成績、期末評価を出力する。
     */
    private static final String P2_PRTTEST_ALL = "2";
    /**
     * 成績一覧表（_reportKind = 3）が選択されており、
     * 学年末が選択された場合は、期末成績と期末評価を出力する。
     */
    private static final String P3_PRTTEST_KIMATUONLY = "3";
    private static final String ASSESS1_SVF_ATTRIBUTE = "Paint=(9,00,2),Bold=1,Meido=100";

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
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/M/d");
        String outputDate = sdf.format(new Date());

        if ("1".equals(_param._reportKind)) {
            printGakunenbetsuHeikinten(db2, svf, outputDate);
        } else if ("2".equals(_param._reportKind)) {
            printDosuBunpuhyou(db2, svf, outputDate);
        } else if ("3".equals(_param._reportKind)) {
            printBukatsudoubetsuSeisekiIchiranhyo(db2, svf, outputDate);
        }
    }

    private void printGakunenbetsuHeikinten(final DB2UDB db2, final Vrw32alp svf, String outputDate) {
        svf.VrSetForm("KNJD301B_1.frm", 4);

        Map<String, String> clubMapMst = getClubMap(db2);
        Map<String, String> gradeMapMst = getGradeMap(db2);
        Map<String, Map<String, GradeScore>> clubMap = getGakunenbetsuHeikinten(db2, gradeMapMst);

        printTitle(svf, outputDate);

        int lineCnt = 1;
        for (String clubcd : clubMapMst.keySet()) {
            if (GAKUNENBETSU_HEIKINTEN_LINE_MAX <= lineCnt) {
                /**
                 * 改ページ前に総計欄を出力する
                 */
                Map<String, GradeScore> gradeMap = clubMap.get(null);
                printFootter(svf, gradeMapMst, gradeMap);
                svf.VrEndRecord();
                svf.VrEndPage();

                svf.VrSetForm("KNJD301B_1.frm", 4);
                printTitle(svf, outputDate);
                lineCnt = 1;
            }

            String clubName = clubMapMst.get(clubcd);
            svf.VrsOut("CLUB_CD",   clubcd);
            svf.VrsOut("CLUB_NAME", clubName);

            if (clubMap.containsKey(clubcd)) {
                Map<String, GradeScore> gradeMap = clubMap.get(clubcd);
                int totalScore = 0;
                int totalCount = 0;

                /**
                 * 該当クラブの１～３年生の平均を印字する。
                 */
                boolean printFlg = false;
                for (GradeScore greadeScore : gradeMap.values()) {
                    int colCnt = 1;
                    for (String grade : gradeMapMst.keySet()) {
                        if (grade.equals(greadeScore._grade)) {
                            break;
                        }
                        colCnt++;
                    }
                    if (3 < colCnt) {
                        // 不正データ（SCHREG_CLUB_HIST_DATには存在する人がSCHREG_REGD_DATにはいないなど）、
                        // もしくは、部員がいない部活動などはスキップする。
                        continue;
                    }

                    totalScore += greadeScore._score;
                    totalCount += greadeScore._count;

                    svf.VrsOut("AVE" + colCnt, greadeScore.avg().toString());
                    printFlg = true;
                    _hasData = true;
                }

                if (printFlg) {
                    /**
                     * いずれかの学年に部員がいる場合のみ、
                     * 該当クラブの全学年平均を印字する。
                     */
                    GradeScore totalGrade = new GradeScore(null, totalScore, totalCount);
                    svf.VrsOut("AVE4", totalGrade.avg().toString());
                }
            }

            lineCnt++;
            svf.VrEndRecord();
        }

        Map<String, GradeScore> gradeMap = clubMap.get(null);
        printFootter(svf, gradeMapMst, gradeMap);
        svf.VrEndRecord();
    }

    private void printTitle(final Vrw32alp svf, String outputDate) {
        svf.VrsOut("TITLE", _param._warekiNen + "年度　" + _param._testKindName + "　学年別平均点（部活動別）");
        svf.VrsOut("PRINT_DATE", "出力日：" + outputDate);
    }

    private void printFootter(final Vrw32alp svf, Map<String, String> gradeMapMst, Map<String, GradeScore> gradeMap) {
        int cnt = 1;
        int totalScore = 0;
        int totalCount = 0;
        for (String grade : gradeMapMst.keySet()) {
            if (3 < cnt) {
                break;
            }
            if (gradeMap.containsKey(grade)) {
                GradeScore gradeScore  = gradeMap.get(grade);
                svf.VrsOut("TOTAL_AVE" + cnt, gradeScore.avg().toString());
                totalScore += gradeScore._score;
                totalCount += gradeScore._count;
            }
            cnt++;
        }
        GradeScore totalGrade = new GradeScore(null, totalScore, totalCount);
        svf.VrsOut("TOTAL_AVE4", totalGrade.avg().toString());
    }

    private void printDosuBunpuhyou(final DB2UDB db2, final Vrw32alp svf, String outputDate) {
        final List<TickWidth> tickWidthList = new ArrayList<TickWidth>();
        String formName = null;
        if (SCORE_DIV_RATING.equals(_param._testKind.substring(4))) {
            for (int i = 5, id = 1; i > 0; i--, id++) {
                tickWidthList.add(new TickWidth(id, i, i));
            }
            formName = "KNJD301B_3.frm";
        } else {
            for (int i = 100, id = 1; i > 0; i -= 5, id++) {
                int low = id == 1 ? i - 5 : i -4;
                tickWidthList.add(new TickWidth(id, i, low));
                if (id == 1) {
                    i--;
                }
            }
            formName = "KNJD301B_2.frm";
        }

        Map<String, String> gradeMapMst = getGradeMap(db2);
        Map<String, String> clubMapMst = getClubMap(db2);
        Map<String, Subclass> subclassMapMst = getSubclassMap(db2);
        DosuBunpuhyo dosuBunpuhyo = getDosuBunpuhyo(db2, tickWidthList);
        Map<String, Map<String, Map<String, Aggregate>>> dosuBunpuhyoStatistics = getDosuBunpuhyoStatistics(db2);

        for (String grade : dosuBunpuhyo._dosuBunpuMap.keySet()) {
            String gradeCd = String.valueOf(Integer.parseInt(gradeMapMst.get(grade)));

            Map<String, Map<String, Map<TickWidth, Integer>>> clubMap = dosuBunpuhyo._dosuBunpuMap.get(grade);
            for (String clubcd : clubMapMst.keySet()) {
                String clubName = clubMapMst.get(clubcd);

                svf.VrSetForm(formName, 4);
                svf.VrsOut("TITLE", gradeCd + "年生　部活動別　度数分布（" + _param._testKindName + "）");
                svf.VrsOut("CLUB_NAME", clubcd + "：" + clubName);
                svf.VrsOut("PRINT_DATE", "出力日：" + outputDate);

                /**
                 * 指示画面で選択された部活動に成績データがあれば表示する。
                 * ※部員が誰もいないなど、成績データが登録されていなければタイトル以外何も表示しない。
                 */
                if (clubMap.containsKey(clubcd)) {
                    Map<String, Map<TickWidth, Integer>> subclassMap = clubMap.get(clubcd);
                    for (String subclasscd : subclassMapMst.keySet()) {
                        // 科目名を表示
                        Subclass subclass = subclassMapMst.get(subclasscd);
                        int subclassNameByte = KNJ_EditEdit.getMS932ByteLength(subclass._subclassName);
                        String subclassNameFieldName = subclassNameByte > 16 ? "3" : subclassNameByte > 12 ? "2" : "1";
                        svf.VrsOut("SUBCLASS_NAME" + subclassNameFieldName, subclass._subclassName);

                        if (subclassMap.containsKey(subclasscd)) {
                            // 科目（１列）の成績を表示する
                            Map<TickWidth, Integer> tickWidthMap = subclassMap.get(subclasscd);
                            for (TickWidth tickWidth : dosuBunpuhyo._tickWidthList) {
                                if (tickWidthMap.containsKey(tickWidth)) {
                                    int score = tickWidthMap.get(tickWidth);
                                    svf.VriOut("NUM" + tickWidth._id, score);
                                } else {
                                    // 表示する刻み幅の成績がない場合は、空欄表示する。
                                    svf.VrsOut("NUM" + tickWidth._id, "");
                                }
                            }

                            _hasData = true;
                        } else {
                            // 表示する成績がない科目は、科目の列を全て空欄表示する。
                            for (TickWidth tickWidth : dosuBunpuhyo._tickWidthList) {
                                svf.VrsOut("NUM" + tickWidth._id, "");
                            }
                        }

                        boolean subclassExsitsFlg = false;
                        if (dosuBunpuhyoStatistics.containsKey(grade)) {
                            Map<String, Map<String, Aggregate>> clubStatisticsMap = dosuBunpuhyoStatistics.get(grade);
                            if (clubStatisticsMap.containsKey(clubcd)) {
                                Map<String, Aggregate> subclassStatisticsMap = clubStatisticsMap.get(clubcd);
                                if (subclassStatisticsMap.containsKey(subclasscd)) {
                                    Aggregate aggregate = subclassStatisticsMap.get(subclasscd);
                                    svf.VriOut("NO_TAKEN",    aggregate._valueDiCount);
                                    svf.VriOut("EXAM_NUM",    aggregate._count);
                                    svf.VriOut("TOTAK_SCORE", aggregate._score);
                                    svf.VrsOut("CLUB_AVE",    aggregate._avg.toString());
                                    svf.VrsOut("DEVI",        aggregate._stddev.toString());
                                    svf.VriOut("MAX",         aggregate._max);
                                    svf.VriOut("MIN",         aggregate._min);

                                    subclassExsitsFlg = true;
                                }
                            }
                        }

                        if (!subclassExsitsFlg) {
                            svf.VrsOut("NO_TAKEN",    "");
                            svf.VrsOut("EXAM_NUM",    "");
                            svf.VrsOut("TOTAK_SCORE", "");
                            svf.VrsOut("CLUB_AVE",    "");
                            svf.VrsOut("DEVI",        "");
                            svf.VrsOut("MAX",         "");
                            svf.VrsOut("MIN",         "");
                        }

                        svf.VrEndRecord();
                    }
                }

                svf.VrEndPage();
            }
        }
    }

    private void printBukatsudoubetsuSeisekiIchiranhyo(final DB2UDB db2, final Vrw32alp svf, String outputDate) {
        Map<String, String> gradeMapMst = getGradeMap(db2);
        Map<String, String> clubMapMst = getClubMap(db2);
        Map<String, Subclass> subclassMapMst = getSubclassMap(db2);
        Map<String, Map<String, List<Map<String, Student>>>> gradeMap = getBukatsudoubetsuSeisekiIchiranhyoList(db2);
        int assess = getAssess(db2);

        for (String grade : gradeMap.keySet()) {
            String gradeCd = String.valueOf(Integer.parseInt(gradeMapMst.get(grade)));

            int no = 1;
            Map<String, List<Map<String, Student>>> clubMap = gradeMap.get(grade);
            for (String clubcd : clubMapMst.keySet()) {
                String clubName = clubMapMst.get(clubcd);

                svf.VrSetForm("KNJD301B_4.frm", 4);
                final String testName = SEME_ALL.equals(_param._semester) ? "学年末" : _param._testKindAbbv;
                svf.VrsOut("TITLE", _param._warekiNen + "年度　" + gradeCd + "年生　" + testName + "　部活動別成績一覧表");
                svf.VrsOut("CLUB_NAME", clubcd + "：" + clubName);
                svf.VrsOut("PRINT_DATE", "出力日：" + outputDate);

                /**
                 * 指示画面で選択された部活動に成績データがあれば表示する。
                 * ※部員が誰もいないなど、成績データが登録されていなければタイトル以外何も表示しない。
                 */
                if (clubMap.containsKey(clubcd)) {
                    List<Map<String, Student>> schregList = clubMap.get(clubcd);

                    for (Map<String, Student> schregMap : schregList) {

                        /**
                         * 名前などを印字する。
                         */
                        int lineCnt = 1;
                        for (Student student : schregMap.values()) {
                            svf.VriOutn("NO",          lineCnt, no);
                            svf.VrsOutn("HR_NAME",     lineCnt, student._hrClassName);
                            svf.VrsOutn("SCHREG_NO",   lineCnt, student._schregno);
                            svf.VrsOutn("NAME1",       lineCnt, student._name);
                            if (P1_PRTTEST_SPECIFY.equals(_param._printPattern)) {
                                svf.VrsOutn("TOTAL_SCORE", lineCnt, student._rawScoreTotal.toString());
                                svf.VrsOutn("AVE_SCORE",   lineCnt, student.rawScoreAvg().toString());
                            } else if (P2_PRTTEST_ALL.equals(_param._printPattern)) {
                                svf.VrsOutn("TOTAL_SCORE", lineCnt, student._rawScoreTotal.toString());
                                svf.VrsOutn("AVE_SCORE",   lineCnt, student.rawScoreAvg().toString());
                                svf.VrsOutn("TOTAL_DIV1",  lineCnt, student._resultsTotal.toString());
                                svf.VrsOutn("AVE_DIV1",    lineCnt, student.resultsAvg().toString());
                                svf.VrsOutn("TOTAL_DIV2",  lineCnt, ""); // 評価の合計は印字しない
                                svf.VrsOutn("AVE_DIV2",    lineCnt, student.ratingAvg().toString());
                            } else if (P3_PRTTEST_KIMATUONLY.equals(_param._printPattern)) {
                                svf.VrsOutn("TOTAL_DIV1",  lineCnt, student._resultsTotal.toString());
                                svf.VrsOutn("AVE_DIV1",    lineCnt, student.resultsAvg().toString());
                                svf.VrsOutn("TOTAL_DIV2",  lineCnt, ""); // 評価の合計は印字しない
                                svf.VrsOutn("AVE_DIV2",    lineCnt, student.ratingAvg().toString());
                            }

                            no++;
                            lineCnt++;
                            _hasData = true;
                        }

                        for (String subclasscd : subclassMapMst.keySet()) {
                            Subclass subclass = subclassMapMst.get(subclasscd);
                            svf.VrsOut("SUBCLASS_NAME1", subclass._subclassAbbv);

                            /**
                             * 点数を印字する。
                             */
                            int colCnt = 1;
                            for (Student student : schregMap.values()) {
                                if (P1_PRTTEST_SPECIFY.equals(_param._printPattern)) {
                                    this.printScore(svf, "SCORE", colCnt, student._rawScoreMap, subclasscd, assess);
                                } else if (P2_PRTTEST_ALL.equals(_param._printPattern)) {
                                    this.printScore(svf, "SCORE", colCnt, student._rawScoreMap, subclasscd, assess);
                                    this.printScore(svf, "DIV1", colCnt, student._resultsMap, subclasscd, assess);
                                    this.printScore(svf, "DIV2", colCnt, student._ratingMap, subclasscd, assess);
                                } else if (P3_PRTTEST_KIMATUONLY.equals(_param._printPattern)) {
                                    this.printScore(svf, "DIV1", colCnt, student._resultsMap, subclasscd, assess);
                                    this.printScore(svf, "DIV2", colCnt, student._ratingMap, subclasscd, assess);
                                }

                                colCnt++;
                            }

                            svf.VrEndRecord();
                        }

                        svf.VrEndPage();
                    }
                }

            }
        }
    }

    private void printScore (final Vrw32alp svf, final String fieldName, final int colCnt, final Map<String, BigDecimal> scoreMap, final String subclasscd, final int assess) {
        String scoreStr = null;
        if (scoreMap.containsKey(subclasscd)) {
            BigDecimal score = scoreMap.get(subclasscd);
            if (score == null) {
                scoreStr = "";
            } else {
                /**
                 * 印字する項目が「評価」以外（「素点」か「成績」）の場合、
                 * かつ、評定１の判定の場合は、背景を赤とし白文字（太字）にする。
                 */
                if (!"DIV2".equals(fieldName) && score.intValue() <= assess) {
                    svf.VrAttributen(fieldName, colCnt, ASSESS1_SVF_ATTRIBUTE);
                }

                scoreStr = score.toString();
            }
        }
        svf.VrsOutn(fieldName, colCnt, scoreStr);
    }

    private Map<String, Map<String, GradeScore>> getGakunenbetsuHeikinten(final DB2UDB db2, Map<String, String> gradeMapMst) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        Map<String, Map<String, GradeScore>> clubMap = new LinkedHashMap<String, Map<String, GradeScore>>();
        Map<String, GradeScore> gradeMap = null;

        Map<String, GradeScore> totalGradeScore = new LinkedHashMap<String, GradeScore>();
        for (String grade : gradeMapMst.keySet()) {
            GradeScore gradeScore = new GradeScore(grade, 0, 0);
            totalGradeScore.put(grade, gradeScore);
        }

        final String gakunenbetsuHeikintenSql = getGakunenbetsuHeikintenSql();
        log.debug(" gakunenbetsu heikinten sql =" + gakunenbetsuHeikintenSql);

        try {
            ps = db2.prepareStatement(gakunenbetsuHeikintenSql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String clubcd = rs.getString("CLUBCD");
                final String grade = rs.getString("GRADE");
                final int score = rs.getInt("SCORE");
                final int count = rs.getInt("COUNT");

                if (clubMap.containsKey(clubcd)) {
                    gradeMap = clubMap.get(clubcd);
                } else {
                    gradeMap = new LinkedHashMap<String, GradeScore>();
                    clubMap.put(clubcd, gradeMap);
                }
                GradeScore gradeScore = new GradeScore(grade, score, count);
                gradeMap.put(grade, gradeScore);

                GradeScore total = totalGradeScore.get(grade);

                if (total != null) {
                    total._score += score;
                    total._count += count;
                }
            }

            /**
             * 最後の要素に各学年の集計値を格納する
             */
            clubMap.put(null, totalGradeScore);
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }

        return clubMap;
    }

    private DosuBunpuhyo getDosuBunpuhyo(final DB2UDB db2, List<TickWidth> tickWidthList) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        DosuBunpuhyo dosuBunpuhyo = new DosuBunpuhyo(tickWidthList);
        final String dosuBunpuhyoSql = getDosuBunpuhyoSql();
        log.debug(" dosu bunpuhyo sql =" + dosuBunpuhyoSql);

        try {
            ps = db2.prepareStatement(dosuBunpuhyoSql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String grade = rs.getString("GRADE");
                final String clubcd = rs.getString("CLUBCD");
                final String subclasscd = rs.getString("SUBCLASSCD");
                final boolean valueDi = rs.getBoolean("VALUE_DI");
                final int score = rs.getInt("SCORE");

                dosuBunpuhyo.put(grade, clubcd, subclasscd, valueDi, score);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }

        return dosuBunpuhyo;
    }

    private Map<String, Map<String, Map<String, Aggregate>>> getDosuBunpuhyoStatistics(final DB2UDB db2) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        Map<String, Map<String, Map<String, Aggregate>>> dosuBunpuhyoStatistics = new LinkedHashMap<String, Map<String, Map<String, Aggregate>>>();
        Map<String, Map<String, Aggregate>> clubMap = null;
        Map<String, Aggregate> subclassMap = null;

        final String dosuBunpuhyoStatisticsSql = getDosuBunpuhyoStatisticsSql();
        log.debug(" dosu bunpuhyo statistics sql =" + dosuBunpuhyoStatisticsSql);

        try {
            ps = db2.prepareStatement(dosuBunpuhyoStatisticsSql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String grade = rs.getString("GRADE");
                final String clubcd = rs.getString("CLUBCD");
                final String subclasscd = rs.getString("SUBCLASSCD");
                final int count = rs.getInt("COUNT");
                final int valueDiCount = rs.getInt("VALUE_DI_COUNT");
                final BigDecimal avg = rs.getBigDecimal("AVG");
                final BigDecimal stddev = rs.getBigDecimal("STDDEV");
                final int score = rs.getInt("SCORE");
                final int max = rs.getInt("MAX");
                final int min = rs.getInt("MIN");

                if (dosuBunpuhyoStatistics.containsKey(grade)) {
                    clubMap = dosuBunpuhyoStatistics.get(grade);
                } else {
                    clubMap = new LinkedHashMap<String, Map<String, Aggregate>>();
                    dosuBunpuhyoStatistics.put(grade, clubMap);
                }

                if (clubMap.containsKey(clubcd)) {
                    subclassMap = clubMap.get(clubcd);
                } else {
                    subclassMap = new LinkedHashMap<String, Aggregate>();
                    clubMap.put(clubcd, subclassMap);
                }

                Aggregate aggregate = new Aggregate(count, valueDiCount, avg, stddev, score, max, min);
                subclassMap.put(subclasscd, aggregate);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }

        return dosuBunpuhyoStatistics;
    }

    private Map<String, String> getClubMap(final DB2UDB db2) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        Map<String, String> clubMap = new LinkedHashMap<String, String>();

        final String clubSql = getClubSql();
        log.debug(" club sql =" + clubSql);

        try {
            ps = db2.prepareStatement(clubSql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String clubcd = rs.getString("CLUBCD");
                final String clubName = rs.getString("CLUBNAME");

                clubMap.put(clubcd, clubName);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }

        return clubMap;
    }

    private Map<String, Subclass> getSubclassMap(final DB2UDB db2) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        Map<String, Subclass> subclassMap = new LinkedHashMap<String, Subclass>();

        final String subclassSql = getSubclassSql();
        log.debug(" subclass sql =" + subclassSql);

        try {
            ps = db2.prepareStatement(subclassSql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String subclasscd = rs.getString("SUBCLASSCD");
                final String subclassName = rs.getString("SUBCLASSNAME");
                final String subclassAbbv = rs.getString("SUBCLASSABBV");

                Subclass subclass = new Subclass(subclasscd, subclassName, subclassAbbv);
                subclassMap.put(subclasscd, subclass);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }

        return subclassMap;
    }

    private Map<String, String> getGradeMap(final DB2UDB db2) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        Map<String, String> gradeMap = new LinkedHashMap<String, String>();

        final String gradeSql = getGradeSql();
        log.debug(" grade sql =" + gradeSql);

        try {
            ps = db2.prepareStatement(gradeSql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String grade = rs.getString("GRADE");
                final String gradeCd = rs.getString("GRADE_CD");

                gradeMap.put(grade, gradeCd);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }

        return gradeMap;
    }

    private Map<String, Map<String, List<Map<String, Student>>>> getBukatsudoubetsuSeisekiIchiranhyoList(final DB2UDB db2) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        Map<String, Map<String, List<Map<String, Student>>>> gradeMap = new LinkedHashMap<String, Map<String, List<Map<String, Student>>>>();
        Map<String, List<Map<String, Student>>> clubMap = null;
        List<Map<String, Student>> schregnoList = null;
        Map<String, Student> schregnoMap = null;
        Student student = null;

        final String bukatsudoubetsuSeisekiIchiranhyoSql = getBukatsudoubetsuSeisekiIchiranhyoSql();
        log.debug(" bukatsudoubetsu seiseki ichiranhyo sql =" + bukatsudoubetsuSeisekiIchiranhyoSql);

        try {
            ps = db2.prepareStatement(bukatsudoubetsuSeisekiIchiranhyoSql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String grade = rs.getString("GRADE");
                final String clubcd = rs.getString("CLUBCD");
                final String subclasscd = rs.getString("SUBCLASSCD");
                final String hrNameAbbv = rs.getString("HR_NAMEABBV");
                final String schregno = rs.getString("SCHREGNO");
                final String name = rs.getString("NAME");
                final BigDecimal rawScore = rs.getBigDecimal("RAW_SCORE");
                final BigDecimal results = rs.getBigDecimal("RESULTS");
                final BigDecimal rating = rs.getBigDecimal("RATING");

                if (gradeMap.containsKey(grade)) {
                    clubMap = gradeMap.get(grade);
                } else {
                    clubMap = new LinkedHashMap<String, List<Map<String, Student>>>();
                    gradeMap.put(grade, clubMap);
                }

                if (clubMap.containsKey(clubcd)) {
                    schregnoList = clubMap.get(clubcd);
                } else {
                    schregnoList = new ArrayList<Map<String, Student>>();
                    clubMap.put(clubcd, schregnoList);
                }

                if (schregnoList.size() != 0) {
                    schregnoMap = schregnoList.get(schregnoList.size() - 1);
                } else {
                    schregnoMap = new LinkedHashMap<String, Student>();
                    schregnoList.add(schregnoMap);
                }

                if (schregnoMap.containsKey(schregno)) {
                    student = schregnoMap.get(schregno);
                    student.put(subclasscd, rawScore, results, rating);
                } else {
                    student =  new Student(subclasscd, schregno, hrNameAbbv, name, rawScore, results, rating);
                    if (BUKATSUDOUBETSU_SEISEKI_ICHIRANHYO_LINE_MAX <= schregnoMap.size()) {
                        schregnoMap = new LinkedHashMap<String, Student>();
                        schregnoList.add(schregnoMap);
                    }
                    schregnoMap.put(schregno, student);
                }
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }

        return gradeMap;
    }

    private int getAssess(final DB2UDB db2) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        int assess = 0;

        final String assessSql = getAssessSql();
        log.debug(" assess sql =" + assessSql);

        try {
            ps = db2.prepareStatement(assessSql);
            rs = ps.executeQuery();

            while (rs.next()) {
                assess = rs.getInt("ASSESSHIGH");
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }

        return assess;
    }

    private class Student {
        private final String _schregno;
        private final String _hrClassName;
        private final String _name;
        private final Map<String, BigDecimal> _rawScoreMap;
        private final Map<String, BigDecimal> _resultsMap;
        private final Map<String, BigDecimal> _ratingMap;

        /**
         * 集計用
         */
        private BigDecimal _rawScoreTotal = new BigDecimal(0);
        private BigDecimal _resultsTotal  = new BigDecimal(0);
        private BigDecimal _ratingTotal   = new BigDecimal(0);
        private int _rawScoreCnt = 0;
        private int _resultsCnt  = 0;
        private int _ratingCnt   = 0;

        private Student (
            final String  subclasscd,
            final String  schregno,
            final String  hrClassName,
            final String  name,
            final BigDecimal rawScore,
            final BigDecimal results,
            final BigDecimal rating) {

            _schregno    = schregno;
            _hrClassName = hrClassName;
            _name        = name;

            _rawScoreMap = new LinkedHashMap<String, BigDecimal>();
            _resultsMap  = new LinkedHashMap<String, BigDecimal>();
            _ratingMap   = new LinkedHashMap<String, BigDecimal>();

            if (rawScore != null) {
                _rawScoreMap.put(subclasscd, rawScore);
                _rawScoreTotal = _rawScoreTotal.add(rawScore);
                _rawScoreCnt++;
            }

            if (results != null) {
                _resultsMap.put(subclasscd, results);
                _resultsTotal = _resultsTotal.add(results);
                _resultsCnt++;
            }

            if (rating != null) {
                _ratingMap.put(subclasscd, rating);
                _ratingTotal = _ratingTotal.add(rating);
                _ratingCnt++;
            }
        }

        private void put(
            final String  subclasscd,
            final BigDecimal rawScore,
            final BigDecimal results,
            final BigDecimal rating) {

            if (rawScore != null) {
                _rawScoreMap.put(subclasscd, rawScore);
                _rawScoreTotal = _rawScoreTotal.add(rawScore);
                _rawScoreCnt++;
            }

            if (results != null) {
                _resultsMap.put(subclasscd, results);
                _resultsTotal = _resultsTotal.add(results);
                _resultsCnt++;
            }

            if (rating != null) {
                _ratingMap.put(subclasscd, rating);
                _ratingTotal = _ratingTotal.add(rating);
                _ratingCnt++;
            }
        }

        private BigDecimal rawScoreAvg() {
            if (this._rawScoreCnt == 0) {
                return new BigDecimal(0);
            } else {
                double avg = (double)this._rawScoreTotal.intValue() / (double)this._rawScoreCnt;
                return new BigDecimal(avg).setScale(1, BigDecimal.ROUND_HALF_UP);
            }
        }

        private BigDecimal resultsAvg() {
            if (this._resultsCnt == 0) {
                return new BigDecimal(0);
            } else {
                double avg = (double)this._resultsTotal.intValue() / (double)this._resultsCnt;
                return new BigDecimal(avg).setScale(1, BigDecimal.ROUND_HALF_UP);
            }
        }

        private BigDecimal ratingAvg() {
            if (this._ratingCnt == 0) {
                return new BigDecimal(0);
            } else {
                double avg = (double)this._ratingTotal.intValue() / (double)this._ratingCnt;
                return new BigDecimal(avg).setScale(1, BigDecimal.ROUND_HALF_UP);
            }
        }
    }

    private String getGakunenbetsuHeikintenSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     CLUB.CLUBCD, ");
        stb.append("     DAT.GRADE, ");
        stb.append("     SUM(RANK.SCORE) AS SCORE, ");
        stb.append("     COUNT(RANK.SCHREGNO) AS COUNT ");

        stb.append(" FROM ");
        stb.append("     CLUB_MST CLUB ");

        stb.append("     INNER JOIN NAME_MST A023 ON ");
        stb.append("                NAMECD1 = 'A023' ");
        stb.append("            AND NAME1   = CLUB.SCHOOL_KIND ");

        stb.append("     INNER JOIN SCHREG_CLUB_HIST_DAT HIST ON ");
        stb.append("                HIST.SCHOOLCD    = CLUB.SCHOOLCD ");
        stb.append("            AND HIST.SCHOOL_KIND = CLUB.SCHOOL_KIND ");
        stb.append("            AND HIST.CLUBCD      = CLUB.CLUBCD ");
        stb.append("            AND (HIST.EDATE IS NULL OR (DATE('" + _param._loginDate + "') BETWEEN HIST.SDATE AND HIST.EDATE)) ");

        stb.append("     INNER JOIN SCHREG_REGD_DAT DAT ON ");
        stb.append("                DAT.YEAR     = '" + _param._year     + "' ");
        if (SEME_ALL.equals(_param._semester)) {
            stb.append("            AND DAT.SEMESTER = '" + _param._maxSemester + "' ");
        } else {
            stb.append("            AND DAT.SEMESTER = '" + _param._semester + "' ");
        }
        stb.append("            AND DAT.SCHREGNO = HIST.SCHREGNO ");
        stb.append("            AND (DAT.GRADE BETWEEN A023.NAME2 AND A023.NAME3) ");

        stb.append("     INNER JOIN SCHREG_REGD_HDAT HDAT ON ");
        stb.append("                HDAT.YEAR     = DAT.YEAR ");
        stb.append("            AND HDAT.SEMESTER = DAT.SEMESTER ");
        stb.append("            AND HDAT.GRADE    = DAT.GRADE ");
        stb.append("            AND HDAT.HR_CLASS = DAT.HR_CLASS ");

        stb.append("     INNER JOIN SCHREG_BASE_MST BASE ON ");
        stb.append("                BASE.SCHREGNO = DAT.SCHREGNO ");

        stb.append("     INNER JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV TESTITEM ON ");
        stb.append("                TESTITEM.YEAR       = DAT.YEAR ");
        stb.append("            AND TESTITEM.SEMESTER   = '" + _param._semester                 + "' ");
        stb.append("            AND TESTITEM.TESTKINDCD = '" + _param._testKind.substring(0, 2) + "' ");
        stb.append("            AND TESTITEM.TESTITEMCD = '" + _param._testKind.substring(2, 4) + "' ");
        stb.append("            AND TESTITEM.SCORE_DIV  = '" + _param._testKind.substring(4)    + "' ");

        stb.append("     INNER JOIN RECORD_RANK_SDIV_DAT RANK ON ");
        stb.append("                RANK.YEAR           = TESTITEM.YEAR ");
        stb.append("            AND RANK.SEMESTER       = TESTITEM.SEMESTER ");
        stb.append("            AND RANK.TESTKINDCD     = TESTITEM.TESTKINDCD ");
        stb.append("            AND RANK.TESTITEMCD     = TESTITEM.TESTITEMCD ");
        stb.append("            AND RANK.SCORE_DIV      = TESTITEM.SCORE_DIV ");
        stb.append("            AND RANK.SCHOOL_KIND    = CLUB.SCHOOL_KIND ");
        stb.append("            AND RANK.SCHREGNO       = DAT.SCHREGNO ");
        stb.append("            AND RANK.CLASSCD       <= '90' ");
        stb.append("            AND RANK.CURRICULUM_CD <> '99' ");

        stb.append(" WHERE ");
        stb.append("     CLUB.SCHOOLCD    = '" + _param._schoolcd   + "' AND ");
        stb.append("     CLUB.SCHOOL_KIND = '" + _param._schoolKind + "' AND ");
        stb.append(SQLUtils.whereIn(true, "CLUB.CLUBCD", _param._clubcds));

        stb.append(" GROUP BY ");
        stb.append("     CLUB.CLUBCD, ");
        stb.append("     DAT.GRADE ");

        stb.append(" ORDER BY ");
        stb.append("     CLUB.CLUBCD, ");
        stb.append("     DAT.GRADE ");
        return stb.toString();
    }

    private String getDosuBunpuhyoSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     DAT.GRADE, ");
        stb.append("     CLUB.CLUBCD, ");
        stb.append("     SCORE.CLASSCD || '-' || SCORE.SCHOOL_KIND || '-' || SCORE.CURRICULUM_CD || '-' || SCORE.SUBCLASSCD AS SUBCLASSCD, ");
        stb.append("     RANK.SCHREGNO, ");
        stb.append("     CASE SCORE.VALUE_DI WHEN '*' THEN 1 ELSE 0 END AS VALUE_DI, ");
        stb.append("     RANK.SCORE ");
        stb.append(" FROM ");
        stb.append("     CLUB_MST CLUB ");

        stb.append("     INNER JOIN NAME_MST A023 ON ");
        stb.append("                NAMECD1 = 'A023' ");
        stb.append("            AND NAME1   = CLUB.SCHOOL_KIND ");

        stb.append("     INNER JOIN SCHREG_CLUB_HIST_DAT HIST ON ");
        stb.append("                HIST.SCHOOLCD    = CLUB.SCHOOLCD ");
        stb.append("            AND HIST.SCHOOL_KIND = CLUB.SCHOOL_KIND ");
        stb.append("            AND HIST.CLUBCD      = CLUB.CLUBCD ");
        stb.append("            AND (HIST.EDATE IS NULL OR (DATE('" + _param._loginDate + "') BETWEEN HIST.SDATE AND HIST.EDATE)) ");

        stb.append("     INNER JOIN SCHREG_REGD_DAT DAT ON ");
        stb.append("                DAT.YEAR     = '" + _param._year + "' ");
        if (SEME_ALL.equals(_param._semester)) {
            stb.append("            AND DAT.SEMESTER = '" + _param._maxSemester + "' ");
        } else {
            stb.append("            AND DAT.SEMESTER = '" + _param._semester + "' ");
        }
        stb.append("            AND DAT.SCHREGNO = HIST.SCHREGNO ");
        stb.append("            AND (DAT.GRADE BETWEEN A023.NAME2 AND A023.NAME3) ");

        stb.append("     INNER JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV TESTITEM ON ");
        stb.append("                TESTITEM.YEAR       = DAT.YEAR ");
        stb.append("            AND TESTITEM.SEMESTER   = '" + _param._semester                 + "' ");
        stb.append("            AND TESTITEM.TESTKINDCD = '" + _param._testKind.substring(0, 2) + "' ");
        stb.append("            AND TESTITEM.TESTITEMCD = '" + _param._testKind.substring(2, 4) + "' ");
        stb.append("            AND TESTITEM.SCORE_DIV  = '" + _param._testKind.substring(4)    + "' ");

        stb.append("     INNER JOIN RECORD_SCORE_DAT SCORE ON ");
        stb.append("                SCORE.YEAR          = TESTITEM.YEAR ");
        stb.append("            AND SCORE.SEMESTER      = TESTITEM.SEMESTER ");
        stb.append("            AND SCORE.TESTKINDCD    = TESTITEM.TESTKINDCD ");
        stb.append("            AND SCORE.TESTITEMCD    = TESTITEM.TESTITEMCD ");
        stb.append("            AND SCORE.SCORE_DIV     = TESTITEM.SCORE_DIV ");
        stb.append("            AND SCORE.SCHREGNO      = DAT.SCHREGNO ");

        stb.append("     LEFT JOIN RECORD_RANK_SDIV_DAT RANK ON ");
        stb.append("                RANK.YEAR          = SCORE.YEAR ");
        stb.append("            AND RANK.SEMESTER      = SCORE.SEMESTER ");
        stb.append("            AND RANK.TESTKINDCD    = SCORE.TESTKINDCD ");
        stb.append("            AND RANK.TESTITEMCD    = SCORE.TESTITEMCD ");
        stb.append("            AND RANK.SCORE_DIV     = SCORE.SCORE_DIV ");
        stb.append("            AND RANK.CLASSCD       = SCORE.CLASSCD ");
        stb.append("            AND RANK.SCHOOL_KIND   = SCORE.SCHOOL_KIND ");
        stb.append("            AND RANK.CURRICULUM_CD = SCORE.CURRICULUM_CD ");
        stb.append("            AND RANK.SUBCLASSCD    = SCORE.SUBCLASSCD ");
        stb.append("            AND RANK.SCHREGNO      = SCORE.SCHREGNO ");
        stb.append("            AND RANK.CLASSCD       <= '90' ");
        stb.append("            AND RANK.CURRICULUM_CD <> '99' ");

        stb.append(" WHERE ");
        stb.append("     CLUB.SCHOOLCD    = '" + _param._schoolcd   + "' AND ");
        stb.append("     CLUB.SCHOOL_KIND = '" + _param._schoolKind + "' AND ");
        stb.append(SQLUtils.whereIn(true, "CLUB.CLUBCD", _param._clubcds));

        stb.append(" ORDER BY ");
        stb.append("     DAT.GRADE, ");
        stb.append("     CLUB.CLUBCD, ");
        stb.append("     SCORE.CLASSCD, ");
        stb.append("     SCORE.SCHOOL_KIND, ");
        stb.append("     SCORE.CURRICULUM_CD, ");
        stb.append("     SCORE.SUBCLASSCD ");
        return stb.toString();
    }

    private String getDosuBunpuhyoStatisticsSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH STATISTICS AS ( ");
        stb.append(getDosuBunpuhyoSql());
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     GRADE, ");
        stb.append("     CLUBCD, ");
        stb.append("     SUBCLASSCD, ");
        stb.append("     COUNT(SCHREGNO) AS COUNT, ");
        stb.append("     SUM(VALUE_DI) AS VALUE_DI_COUNT, ");
        stb.append("     SUM(SCORE) AS SCORE, ");
        stb.append("     DECIMAL(INT((AVG(FLOAT(SCORE)) * 100) + 0.5) / 100.0, 5, 2) AS AVG, ");
        stb.append("     DECIMAL(INT((STDDEV(FLOAT(SCORE)) * 100) + 0.5) / 100.0, 5, 2) AS STDDEV, ");
        stb.append("     MAX(SCORE) AS MAX, ");
        stb.append("     MIN(SCORE) AS MIN ");

        stb.append(" FROM ");
        stb.append("     STATISTICS ");

        stb.append(" GROUP BY ");
        stb.append("     GRADE, ");
        stb.append("     CLUBCD, ");
        stb.append("     SUBCLASSCD ");

        stb.append(" ORDER BY ");
        stb.append("     GRADE, ");
        stb.append("     CLUBCD, ");
        stb.append("     SUBCLASSCD ");
        return stb.toString();
    }

    private String getAssessSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     ASSESSHIGH ");
        stb.append(" FROM ");
        stb.append("     ASSESS_MST ");
        stb.append(" WHERE ");
        stb.append("     ASSESSCD    = '3' ");
        stb.append(" AND ASSESSLEVEL = '1' "); // 1:評定段階１
        return stb.toString();
    }

    private String getBukatsudoubetsuSeisekiIchiranhyoSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     DAT.GRADE, ");
        stb.append("     CLUB.CLUBCD, ");
        stb.append("     RANK.CLASSCD || '-' || RANK.SCHOOL_KIND || '-' || RANK.CURRICULUM_CD || '-' || RANK.SUBCLASSCD AS SUBCLASSCD, ");
        stb.append("     HDAT.HR_NAMEABBV, ");
        stb.append("     HIST.SCHREGNO, ");
        stb.append("     BASE.NAME, ");
        stb.append("     SUM(CASE TESTITEM.SCORE_DIV WHEN '" + SCORE_DIV_RAW_SCORE + "' THEN RANK.SCORE ELSE NULL END) AS RAW_SCORE, ");
        stb.append("     SUM(CASE TESTITEM.SCORE_DIV WHEN '" + SCORE_DIV_RESULTS   + "' THEN RANK.SCORE ELSE NULL END) AS RESULTS, ");
        stb.append("     SUM(CASE TESTITEM.SCORE_DIV WHEN '" + SCORE_DIV_RATING    + "' THEN RANK.SCORE ELSE NULL END) AS RATING ");
        stb.append(" FROM ");
        stb.append("     CLUB_MST CLUB ");

        stb.append("     INNER JOIN NAME_MST A023 ON ");
        stb.append("                NAMECD1 = 'A023' ");
        stb.append("            AND NAME1   = CLUB.SCHOOL_KIND ");

        stb.append("     INNER JOIN SCHREG_CLUB_HIST_DAT HIST ON ");
        stb.append("                HIST.SCHOOLCD    = CLUB.SCHOOLCD ");
        stb.append("            AND HIST.SCHOOL_KIND = CLUB.SCHOOL_KIND ");
        stb.append("            AND HIST.CLUBCD      = CLUB.CLUBCD ");
        stb.append("            AND (HIST.EDATE IS NULL OR (DATE('" + _param._loginDate + "') BETWEEN HIST.SDATE AND HIST.EDATE)) ");

        stb.append("     INNER JOIN SCHREG_REGD_DAT DAT ON ");
        stb.append("                DAT.YEAR     = '" + _param._year + "' ");
        if (SEME_ALL.equals(_param._semester)) {
            stb.append("            AND DAT.SEMESTER = '" + _param._maxSemester + "' ");
        } else {
            stb.append("            AND DAT.SEMESTER = '" + _param._semester + "' ");
        }
        stb.append("            AND DAT.SCHREGNO = HIST.SCHREGNO ");
        stb.append("            AND (DAT.GRADE BETWEEN A023.NAME2 AND A023.NAME3) ");

        stb.append("     INNER JOIN SCHREG_REGD_HDAT HDAT ON ");
        stb.append("                HDAT.YEAR     = DAT.YEAR ");
        stb.append("            AND HDAT.SEMESTER = DAT.SEMESTER ");
        stb.append("            AND HDAT.GRADE    = DAT.GRADE ");
        stb.append("            AND HDAT.HR_CLASS = DAT.HR_CLASS ");

        stb.append("     INNER JOIN SCHREG_BASE_MST BASE ON ");
        stb.append("                BASE.SCHREGNO = DAT.SCHREGNO ");

        stb.append("     INNER JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV TESTITEM ON ");
        stb.append("                TESTITEM.YEAR       = DAT.YEAR ");
        stb.append("            AND TESTITEM.SEMESTER   = '" + _param._semester + "' ");
        if (P1_PRTTEST_SPECIFY.equals(_param._printPattern)) {
            stb.append("            AND ( ");
            stb.append("                     TESTITEM.TESTKINDCD || TESTITEM.TESTITEMCD || TESTITEM.SCORE_DIV = '" + _param._testKind + "' ");
            stb.append("                ) ");
        } else if (P2_PRTTEST_ALL.equals(_param._printPattern)) {
            stb.append("            AND ( ");
            stb.append("                     TESTITEM.TESTKINDCD || TESTITEM.TESTITEMCD || TESTITEM.SCORE_DIV = '" + _param._testKind + "' ");
            stb.append("                  OR TESTITEM.TESTKINDCD || TESTITEM.TESTITEMCD || TESTITEM.SCORE_DIV = '" + SCORE_DIV_KIMATSU_RESULTS + "' ");
            stb.append("                  OR TESTITEM.TESTKINDCD || TESTITEM.TESTITEMCD || TESTITEM.SCORE_DIV = '" + SCORE_DIV_KIMATSU_RATING + "' ");
            stb.append("                ) ");
        } else if (P3_PRTTEST_KIMATUONLY.equals(_param._printPattern)) {
            stb.append("            AND ( ");
            stb.append("                     TESTITEM.TESTKINDCD || TESTITEM.TESTITEMCD || TESTITEM.SCORE_DIV = '" + SCORE_DIV_KIMATSU_RESULTS + "' ");
            stb.append("                  OR TESTITEM.TESTKINDCD || TESTITEM.TESTITEMCD || TESTITEM.SCORE_DIV = '" + SCORE_DIV_KIMATSU_RATING + "' ");
            stb.append("                ) ");
        }

        stb.append("     INNER JOIN RECORD_RANK_SDIV_DAT RANK ON ");
        stb.append("                RANK.YEAR           = TESTITEM.YEAR ");
        stb.append("            AND RANK.SEMESTER       = TESTITEM.SEMESTER ");
        stb.append("            AND RANK.TESTKINDCD     = TESTITEM.TESTKINDCD ");
        stb.append("            AND RANK.TESTITEMCD     = TESTITEM.TESTITEMCD ");
        stb.append("            AND RANK.SCORE_DIV      = TESTITEM.SCORE_DIV ");
        stb.append("            AND RANK.SCHOOL_KIND    = CLUB.SCHOOL_KIND ");
        stb.append("            AND RANK.SCHREGNO       = DAT.SCHREGNO ");
        stb.append("            AND RANK.CLASSCD       <= '90' ");
        stb.append("            AND RANK.CURRICULUM_CD <> '99' ");

        stb.append(" WHERE ");
        stb.append("     CLUB.SCHOOLCD    = '" + _param._schoolcd   + "' AND ");
        stb.append("     CLUB.SCHOOL_KIND = '" + _param._schoolKind + "' AND ");
        stb.append(SQLUtils.whereIn(true, "CLUB.CLUBCD", _param._clubcds));

        stb.append(" GROUP BY ");
        stb.append("     DAT.GRADE, ");
        stb.append("     CLUB.CLUBCD, ");
        stb.append("     RANK.CLASSCD, ");
        stb.append("     RANK.SCHOOL_KIND, ");
        stb.append("     RANK.CURRICULUM_CD, ");
        stb.append("     RANK.SUBCLASSCD, ");
        stb.append("     HDAT.HR_CLASS, ");
        stb.append("     HDAT.HR_NAMEABBV, ");
        stb.append("     HIST.SCHREGNO, ");
        stb.append("     BASE.NAME ");

        stb.append(" ORDER BY ");
        stb.append("     DAT.GRADE, ");
        stb.append("     CLUB.CLUBCD, ");
        stb.append("     RANK.CLASSCD, ");
        stb.append("     RANK.SCHOOL_KIND, ");
        stb.append("     RANK.CURRICULUM_CD, ");
        stb.append("     RANK.SUBCLASSCD, ");
        stb.append("     HDAT.HR_CLASS, ");
        stb.append("     HIST.SCHREGNO ");
        return stb.toString();
    }

    private String getClubSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     CLUBCD, ");
        stb.append("     CLUBNAME ");

        stb.append(" FROM ");
        stb.append("     CLUB_MST ");

        stb.append(" WHERE ");
        stb.append("     SCHOOLCD    = '" + _param._schoolcd   + "' AND ");
        stb.append("     SCHOOL_KIND = '" + _param._schoolKind + "' AND ");
        stb.append(SQLUtils.whereIn(true, "CLUBCD", _param._clubcds));

        stb.append(" ORDER BY ");
        stb.append("     CLUBCD ");
        return stb.toString();
    }

    private String getSubclassSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     SUB.CLASSCD || '-' || SUB.SCHOOL_KIND || '-' || SUB.CURRICULUM_CD || '-' || SUB.SUBCLASSCD AS SUBCLASSCD, ");
        stb.append("     SUB.SUBCLASSNAME, ");
        stb.append("     SUB.SUBCLASSABBV ");
        stb.append(" FROM ");
        stb.append("     CLUB_MST CLUB ");

        stb.append("     INNER JOIN NAME_MST A023 ON ");
        stb.append("                NAMECD1 = 'A023' ");
        stb.append("            AND NAME1   = CLUB.SCHOOL_KIND ");

        stb.append("     INNER JOIN SCHREG_CLUB_HIST_DAT HIST ON ");
        stb.append("                HIST.SCHOOLCD    = CLUB.SCHOOLCD ");
        stb.append("            AND HIST.SCHOOL_KIND = CLUB.SCHOOL_KIND ");
        stb.append("            AND HIST.CLUBCD      = CLUB.CLUBCD ");
        stb.append("            AND (HIST.EDATE IS NULL OR (DATE('" + _param._loginDate + "') BETWEEN HIST.SDATE AND HIST.EDATE)) ");

        stb.append("     INNER JOIN SCHREG_REGD_DAT DAT ON ");
        stb.append("                DAT.YEAR     = '" + _param._year + "' ");
        if (SEME_ALL.equals(_param._semester)) {
            stb.append("            AND DAT.SEMESTER = '" + _param._maxSemester + "' ");
        } else {
            stb.append("            AND DAT.SEMESTER = '" + _param._semester + "' ");
        }
        stb.append("            AND DAT.SCHREGNO = HIST.SCHREGNO ");
        stb.append("            AND (DAT.GRADE BETWEEN A023.NAME2 AND A023.NAME3) ");

        stb.append("     INNER JOIN SCHREG_REGD_HDAT HDAT ON ");
        stb.append("                HDAT.YEAR     = DAT.YEAR ");
        stb.append("            AND HDAT.SEMESTER = DAT.SEMESTER ");
        stb.append("            AND HDAT.GRADE    = DAT.GRADE ");
        stb.append("            AND HDAT.HR_CLASS = DAT.HR_CLASS ");

        stb.append("     INNER JOIN SCHREG_BASE_MST BASE ON ");
        stb.append("                BASE.SCHREGNO = DAT.SCHREGNO ");

        stb.append("     INNER JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV TESTITEM ON ");
        stb.append("                TESTITEM.YEAR       = DAT.YEAR ");
        stb.append("            AND TESTITEM.SEMESTER   = '"+ _param._semester + "' ");
        if (P1_PRTTEST_SPECIFY.equals(_param._printPattern)) {
            stb.append("            AND TESTITEM.TESTKINDCD || TESTITEM.TESTITEMCD || TESTITEM.SCORE_DIV = '" + _param._testKind + "' ");
        } else if (P2_PRTTEST_ALL.equals(_param._printPattern)) {
            stb.append("            AND ( ");
            stb.append("                     TESTITEM.TESTKINDCD || TESTITEM.TESTITEMCD || TESTITEM.SCORE_DIV = '" + _param._testKind + "' ");
            stb.append("                  OR TESTITEM.TESTKINDCD || TESTITEM.TESTITEMCD || TESTITEM.SCORE_DIV = '" + SCORE_DIV_KIMATSU_RESULTS + "' ");
            stb.append("                  OR TESTITEM.TESTKINDCD || TESTITEM.TESTITEMCD || TESTITEM.SCORE_DIV = '" + SCORE_DIV_KIMATSU_RATING + "' ");
            stb.append("                ) ");
        } else if (P3_PRTTEST_KIMATUONLY.equals(_param._printPattern)) {
            stb.append("            AND ( ");
            stb.append("                     TESTITEM.TESTKINDCD || TESTITEM.TESTITEMCD || TESTITEM.SCORE_DIV = '" + SCORE_DIV_KIMATSU_RESULTS + "' ");
            stb.append("                  OR TESTITEM.TESTKINDCD || TESTITEM.TESTITEMCD || TESTITEM.SCORE_DIV = '" + SCORE_DIV_KIMATSU_RATING + "' ");
            stb.append("                ) ");
        }

        stb.append("     INNER JOIN RECORD_RANK_SDIV_DAT RANK ON ");
        stb.append("                RANK.YEAR           = TESTITEM.YEAR ");
        stb.append("            AND RANK.SEMESTER       = TESTITEM.SEMESTER ");
        stb.append("            AND RANK.TESTKINDCD     = TESTITEM.TESTKINDCD ");
        stb.append("            AND RANK.TESTITEMCD     = TESTITEM.TESTITEMCD ");
        stb.append("            AND RANK.SCORE_DIV      = TESTITEM.SCORE_DIV ");
        stb.append("            AND RANK.SCHOOL_KIND    = CLUB.SCHOOL_KIND ");
        stb.append("            AND RANK.SCHREGNO       = DAT.SCHREGNO ");
        stb.append("            AND RANK.CLASSCD       <= '90' ");
        stb.append("            AND RANK.CURRICULUM_CD <> '99' ");

        stb.append("     INNER JOIN SUBCLASS_MST SUB ON ");
        stb.append("                SUB.CLASSCD       = RANK.CLASSCD ");
        stb.append("            AND SUB.SCHOOL_KIND   = RANK.SCHOOL_KIND ");
        stb.append("            AND SUB.CURRICULUM_CD = RANK.CURRICULUM_CD ");
        stb.append("            AND SUB.SUBCLASSCD    = RANK.SUBCLASSCD ");

        stb.append(" WHERE ");
        stb.append("     CLUB.SCHOOLCD    = '" + _param._schoolcd   + "' AND ");
        stb.append("     CLUB.SCHOOL_KIND = '" + _param._schoolKind + "' AND ");
        stb.append(SQLUtils.whereIn(true, "CLUB.CLUBCD", _param._clubcds));

        stb.append(" GROUP BY ");
        stb.append("     SUB.CLASSCD, ");
        stb.append("     SUB.SCHOOL_KIND, ");
        stb.append("     SUB.CURRICULUM_CD, ");
        stb.append("     SUB.SUBCLASSCD, ");
        stb.append("     SUB.SUBCLASSNAME, ");
        stb.append("     SUB.SUBCLASSABBV ");

        stb.append(" ORDER BY ");
        stb.append("     SUB.CLASSCD, ");
        stb.append("     SUB.SCHOOL_KIND, ");
        stb.append("     SUB.CURRICULUM_CD, ");
        stb.append("     SUB.SUBCLASSCD ");
        return stb.toString();
    }

    private String getGradeSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     GRADE, ");
        stb.append("     GRADE_CD ");

        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_GDAT ");

        stb.append(" WHERE ");
        stb.append("     YEAR = '" + _param._year + "' AND ");
        stb.append("     SCHOOL_KIND = '" + _param._schoolKind + "' ");

        stb.append(" ORDER BY ");
        stb.append("     GRADE ");
        return stb.toString();
    }

    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    private static class TickWidth {
        private final int _id;
        private final int _high;
        private final int _low;

        TickWidth (final int id, final int high, final int low) {
            _id   = id;
            _high = high;
            _low  = low;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + _id;
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }

            if (obj == null) {
                return false;
            }

            if (getClass() != obj.getClass()) {
                return false;
            }

            TickWidth other = (TickWidth) obj;
            if (_id != other._id) {
                return false;
            }

            return true;
        }
    }

    private class GradeScore {
        private final String _grade;
        private int _score;
        private int _count;

        private GradeScore (
            final String grade,
            final int score,
            final int count) {

            _grade    = grade;
            _score    = score;
            _count    = count;
        }

        private BigDecimal avg() {
            if (this._count == 0) {
                return new BigDecimal(0);
            } else {
                double avg = (double)this._score / (double)this._count;
                return new BigDecimal(avg).setScale(2, BigDecimal.ROUND_HALF_UP);
            }
        }
    }

    private class DosuBunpuhyo {
        private final List<TickWidth> _tickWidthList;
        private final Map<String, Map<String, Map<String, Map<TickWidth, Integer>>>>  _dosuBunpuMap;

        private DosuBunpuhyo (final List<TickWidth> tickWidthList) {
            _tickWidthList = tickWidthList;
            _dosuBunpuMap  = new LinkedHashMap<String, Map<String, Map<String, Map<TickWidth, Integer>>>>();
        }

        public void put(final String grade, final String clubcd, final String subclasscd, final boolean valueDi, final int score) {
            // 受験者のみ処理する
            if (!valueDi) {
                Map<String, Map<String, Map<TickWidth, Integer>>> clubMap = null;
                if (_dosuBunpuMap.containsKey(grade)) {
                    clubMap = _dosuBunpuMap.get(grade);
                } else {
                    clubMap = new LinkedHashMap<String, Map<String, Map<TickWidth, Integer>>>();
                    _dosuBunpuMap.put(grade, clubMap);
                }

                Map<String, Map<TickWidth, Integer>> subclassMap = null;
                if (clubMap.containsKey(clubcd)) {
                    subclassMap = clubMap.get(clubcd);
                } else {
                    subclassMap = new LinkedHashMap<String, Map<TickWidth, Integer>>();
                    clubMap.put(clubcd, subclassMap);
                }

                Map<TickWidth, Integer> tickWidthMap = null;
                if (subclassMap.containsKey(subclasscd)) {
                    tickWidthMap = subclassMap.get(subclasscd);
                } else {
                    tickWidthMap = new LinkedHashMap<TickWidth, Integer>();
                    subclassMap.put(subclasscd, tickWidthMap);
                }

                int scoreCnt = 0;
                for (TickWidth tickWidth : _tickWidthList) {
                    if (tickWidth._low <= score && score <= tickWidth._high) {
                        if (tickWidthMap.containsKey(tickWidth)) {
                            scoreCnt = tickWidthMap.get(tickWidth);
                            scoreCnt++;
                        } else {
                            scoreCnt = 1;
                        }
                        tickWidthMap.put(tickWidth, scoreCnt);
                        break;
                    }
                }
            }
        }
    }

    private class Subclass {
        private final String _subclasscd;
        private final String _subclassName;
        private final String _subclassAbbv;

        private Subclass(
            final String subclasscd,
            final String subclassName,
            final String subclassAbbv
        ) {
            _subclasscd = subclasscd;
            _subclassName = subclassName;
            _subclassAbbv = subclassAbbv;
        }
    }

    private class Aggregate {
        final int _count;
        final int _valueDiCount;
        final BigDecimal _avg;
        final BigDecimal _stddev;
        final int _score;
        final int _max;
        final int _min;

        private Aggregate(
            final int count,
            final int valueDiCount,
            final BigDecimal avg,
            final BigDecimal stddev,
            final int score,
            final int max,
            final int min) {
            _count        = count;
            _valueDiCount = valueDiCount;
            _avg          = avg;
            _stddev       = stddev;
            _score        = score;
            _max          = max;
            _min          = min;
        }
    }

    /** パラメータクラス */
    private static class Param {
        private final String   _year;
        private final String   _semester;
        private final String   _schoolcd;
        private final String   _schoolKind;
        private final String   _loginDate;
        private final String   _testKind;
        private final String   _reportKind;
        private final String[] _clubcds;
        private final String   _maxSemester;
        private final String   _warekiNen;
        private final String   _testKindName;
        private final String   _printPattern;
        private final String   _testKindAbbv;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year          = request.getParameter("HID_YEAR");
            _semester      = request.getParameter("SEMESTER");
            _schoolcd      = request.getParameter("SCHOOLCD");
            _schoolKind    = request.getParameter("SCHOOL_KIND");
            _loginDate     = request.getParameter("LOGIN_DATE");
            _testKind      = request.getParameter("TESTKIND");
            _reportKind    = request.getParameter("REPORT_KIND");
            _clubcds       = request.getParameterValues("CLUBS_SELECTED");
            _maxSemester   = getMaxSemester(db2);
            _warekiNen     = getWarekiNen(db2);
            _testKindName  = getTestKindName(db2);
            _printPattern  = getPrintPattern();
            _testKindAbbv = KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT TESTITEMABBV1 FROM TESTITEM_MST_COUNTFLG_NEW_SDIV WHERE YEAR = '" + _year + "' AND SEMESTER = '" + _semester + "' AND TESTKINDCD || TESTITEMCD || SCORE_DIV = '" + _testKind + "'"));
        }

        private String getMaxSemester(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            String semester = null;

            final String sql = "SELECT MAX(SEMESTER) AS SEMESTER FROM SEMESTER_MST WHERE YEAR = '" + _year + "' AND SEMESTER <> '" + SEME_ALL + "' ";
            log.debug(" max semester sql =" + sql);

            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    semester = StringUtils.defaultString(rs.getString("SEMESTER"));
                }
            } catch (SQLException ex) {
                log.debug("Exception:", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }

            return semester;
        }

        private String getWarekiNen(final DB2UDB db2) throws SQLException {
            String warekiNen = KNJ_EditDate.gengou(db2, Integer.parseInt(_year));

            return warekiNen;
        }

        private String getTestKindName(final DB2UDB db2) {
            if (_testKind == null) {
                return "";
            }

            PreparedStatement ps = null;
            ResultSet rs = null;

            String name = null;

            final String sql = "SELECT TESTITEMNAME FROM TESTITEM_MST_COUNTFLG_NEW_SDIV WHERE YEAR = '" + _year + "' AND SEMESTER = '" + _semester + "' AND TESTKINDCD || TESTITEMCD || SCORE_DIV = '" + _testKind + "'";
            log.debug(" test item name sql =" + sql);

            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    name = StringUtils.defaultString(rs.getString("TESTITEMNAME"));
                }
            } catch (SQLException ex) {
                log.debug("Exception:", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }

            return name;
        }

        private String getPrintPattern() {
            String printPattern = null;

            if ("3".equals(_reportKind)) {
                if (SEME_ALL.equals(_semester)) {
                    printPattern = P3_PRTTEST_KIMATUONLY;
                } else {
                    if ("01".equals(_testKind.substring(0, 2))) {
                        printPattern = P1_PRTTEST_SPECIFY;
                    } else if ("02".equals(_testKind.substring(0, 2))) {
                        printPattern = P2_PRTTEST_ALL;
                    } else {
                        printPattern = "";
                    }
                }
            } else {
                printPattern = "1";
            }

            return printPattern;
        }
    }
}

// eof

