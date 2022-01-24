// kanji=漢字
/*
 * $Id: 5acf47a031f4bb858e83ada0ab3748cfd9070e69 $
 *
 * 作成日: 2008/05/24 10:02:00 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.SpiderWebPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

/**
 * 個人成績票。
 * @author takaesu
 * @version $Id: 5acf47a031f4bb858e83ada0ab3748cfd9070e69 $
 */
public class KNJD105Q {
    /*pkg*/static final Log log = LogFactory.getLog(KNJD105Q.class);

    /** 3教科科目コード。 */
    private static final String ALL3 = "333333";
    /** 5教科科目コード。 */
    private static final String ALL5 = "555555";
    /** 9教科科目コード。 */
    private static final String ALL9 = "999999";

    private static final String SEMEALL = "9";

    private boolean _hasData;

    /** 成績表の最大科目数. */
    private final int TABLE_SUBCLASS_MAX = 20;

    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {

        final DB2UDB db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
        try {
            db2.open();
        } catch (final Exception ex) {
            log.error("db open error!", ex);
            return;
        }

        log.fatal("$Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        Param param = new Param(request, db2);

//        if ("csv".equals(param._cmd)) {
//            try {
//                CsvUtils.outputLines(log, response, param._thisExam._title + ".csv", getCsvLines(param, response, db2));
//            } catch (Exception e) {
//                log.fatal("exception!", e);
//            } finally {
//                if (null != db2) {
//                    db2.commit();
//                    db2.close();
//                }
//            }

//        } else {
            Vrw32alp svf = new Vrw32alp();
            if (svf.VrInit() < 0) {
                throw new IllegalStateException("svf初期化失敗");
            }
            svf.VrSetSpoolFileStream(response.getOutputStream());
            response.setContentType("application/pdf");

            try {
                if (!KNJServletUtils.isEnableGraph(log)) {
                    log.fatal("グラフを使える環境ではありません。");
                }

                printMain(param, svf, db2);

            } catch (Exception e) {
                log.fatal("exception!", e);
            } finally {
                if (!_hasData) {
                    svf.VrSetForm("MES001.frm", 0);
                    svf.VrsOut("note", "note");
                    svf.VrEndPage();
                }
                svf.VrQuit();

                if (null != db2) {
                    db2.commit();
                    db2.close();
                }

                if (null != param) {
                    param.delegeTmpImageFile();
                }
            }
//        }
    }

    private static Map getMappedMap(final Map map, final String key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new TreeMap());
        }
        return (Map) map.get(key1);
    }

    private static List getMappedList(final Map map, final String key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new ArrayList());
        }
        return (List) map.get(key1);
    }

    /**
     *  文字数を取得
     */
    private static int getMS932ByteLength(final String str) {
        int ret = 0;
        if (null != str) {
            try {
                ret = str.getBytes("MS932").length;                      //byte数を取得
            } catch (Exception ex) {
                log.error("retStringByteValue error!", ex);
            }
        }
        return ret;
    }

    private static String tostr(final Object o) {
        return null == o ? null : o.toString();
    }

    private static String sishagonyu(final BigDecimal bd, final int scale) {
        return null == bd ? null : bd.setScale(scale, BigDecimal.ROUND_HALF_UP).toString();
    }

    private static String add(String num1, String num2) {
        if (!NumberUtils.isNumber(num1)) return num2;
        if (!NumberUtils.isNumber(num2)) return num1;
        return new BigDecimal(num1).add(new BigDecimal(num2)).toString();
    }

//    private List newLine(final List lines) {
//        final List newLine = new ArrayList();
//        lines.add(newLine);
//        return newLine;
//    }
//    private List getCsvLines(final Param param, final HttpServletResponse response, final DB2UDB db2) throws SQLException {
//        // 対象の生徒たちを得る
//        final List students = Student.createStudents(db2, param);
//        _hasData = students.size() > 0;
//
//        // 成績のデータを読む
//        loadExam(db2, students, param._thisExam, param);
////        if (param._beforeExam.isValid()) {
////            loadExam(db2, students, param._beforeExam, param);
////        }
//
//        final List lines = new ArrayList();
//
//        // 印刷
//        for (final Iterator it = students.iterator(); it.hasNext();) {
//            final Student student = (Student) it.next();
//            log.debug(" student " + student._schregno + " , subclass count = " + getMappedMap(student._examSubclasses, param._thisExam.getExamKey()).size() + ", course key = " + student.courseKey());
////            log.debug("record this: " + getMappedMap(student._examRecord, param._thisExam.getExamKey()).values());
////            log.debug("record bef: " + getMappedMap(student._examRecord, param._beforeExam.getExamKey()).values());
//
//
//            final List line = newLine(lines);
//            line.add(StringUtils.defaultString(param._thisExam._title) + "　" + StringUtils.defaultString(student._hrName) + convNum(Integer.parseInt(student._attendNo)) + "番　" + StringUtils.defaultString(student._name));
//
//            outputTabCsv(lines, param, student, param._thisExam);
//            newLine(lines);
//        }
//        return lines;
//    }

//    private void outputTabCsv(final List lines, final Param param, final Student student, final Exam exam) {
//
//        final int maxIdx = "1".equals(param._deviationPrint) || "2".equals(param._deviationPrint) ? 7 : 6;
//        final List[] lineScore = new List[maxIdx + 1];
//        for (int i = 0; i < lineScore.length; i++) {
//            lineScore[i] = newLine(lines);
//            lineScore[i].add("");
//        }
//
//        final int IDX_CLASS = 0;
//        final int IDX_SUBCLASS = 1;
//        final int IDX_SCORE = 2;
//        final int IDX_AVG = 3;
//        final int IDX_RANK = 4;
//        final int IDX_COUNT = 5;
//        final int IDX_MAX = 6;
//        final int IDX_HYOJUN = 7;
//
//        final String msg = param.getItemMsg();
//        lineScore[IDX_CLASS].add("教科名");
//        lineScore[IDX_SUBCLASS].add("科目名");
//        lineScore[IDX_SCORE].add("得点");
//        lineScore[IDX_AVG].add(msg + "平均");
//        lineScore[IDX_RANK].add(msg + "順位");
//        lineScore[IDX_COUNT].add("受験者数");
//        lineScore[IDX_MAX].add("最高得点");
//        if ("1".equals(param._deviationPrint)) {
//            lineScore[IDX_HYOJUN].add("偏差値");
//        } else if ("2".equals(param._deviationPrint)) {
//            lineScore[IDX_HYOJUN].add("標準偏差");
//        }
//
//        int i = 0;
//        String oldclassname = null;
//        for (final Iterator it = getMappedMap(student._examSubclasses, exam.getExamKey()).values().iterator(); it.hasNext();) {
//            final Subclass subclass = (Subclass) it.next();
//
//            if ("90".equals(subclass.getClassCd(param))) {
//                continue;
//            }
//
//            if (exam.getAttendSubclassCdList(student).contains(subclass._code)) {
//                continue;
//            }
//
//            final Record record = (Record) getMappedMap(student._examRecord, exam.getExamKey()).get(subclass._code);
//            final AverageDat avgDat = (AverageDat) exam._averages.get(averageKey(param, student, subclass._code));
//            final Class clazz = (Class) param._classes.get(subclass.getClassKey(param));
//            if (null == clazz) {
//                continue;
//            }
//
//            lineScore[IDX_CLASS].add(null != oldclassname && oldclassname.equals(clazz._name) ? "" : clazz._name);
//            lineScore[IDX_SUBCLASS].add(subclass._name);
//            i++;
//
//            if (null == record) {
//                for (int j = 2; j < lineScore.length; j++) {
//                    lineScore[j].add("");
//                }
//                continue;
//            }
//
//            lineScore[IDX_SCORE].add(tostr(record._score));
//            lineScore[IDX_RANK].add(tostr(record._rank));
//            if ("1".equals(param._deviationPrint)) {
//                lineScore[IDX_HYOJUN].add(sishagonyu(record._deviation, 1));
//            }
//
//            if (null == avgDat) {
//                lineScore[IDX_AVG].add(null);
//                lineScore[IDX_COUNT].add(null);
//                lineScore[IDX_MAX].add(null);
//                if ("2".equals(param._deviationPrint)) {
//                    lineScore[IDX_HYOJUN].add(null);
//                }
//            } else {
//                lineScore[IDX_AVG].add(sishagonyu(avgDat._avg, 1));
//                lineScore[IDX_COUNT].add(tostr(avgDat._count));
//                lineScore[IDX_MAX].add(tostr(avgDat._highScore));
//                if ("2".equals(param._deviationPrint)) {
//                    lineScore[IDX_HYOJUN].add(sishagonyu(avgDat._stdDev, 1));
//                }
//            }
//            oldclassname = clazz._name;
//        }
//
//        if (i > 0) {
//            lineScore[IDX_CLASS].add("全教科");
//            lineScore[IDX_SUBCLASS].add("全教科");
//
//            final Record rec9 = (Record) getMappedMap(student._examRecordOther, exam.getExamKey()).get(ALL9);
//            if (null == rec9) {
//                lineScore[IDX_SCORE].add("");
//                lineScore[IDX_RANK].add("");
//                if ("1".equals(param._deviationPrint)) {
//                    lineScore[IDX_HYOJUN].add("");
//                }
//            } else {
//                lineScore[IDX_SCORE].add(tostr(rec9._score));
//                lineScore[IDX_RANK].add(tostr(rec9._rank));
//                if ("1".equals(param._deviationPrint)) {
//                    lineScore[IDX_HYOJUN].add(sishagonyu(rec9._deviation, 1));
//                }
//            }
//            final AverageDat avg9 = (AverageDat) exam._averagesOther.get(averageKey(param, student, ALL9));
//            if (null == avg9) {
//                lineScore[IDX_AVG].add(null);
//                lineScore[IDX_COUNT].add(null);
//                lineScore[IDX_MAX].add(null);
//                if ("2".equals(param._deviationPrint)) {
//                    lineScore[IDX_HYOJUN].add(null);
//                }
//            } else {
//                lineScore[IDX_AVG].add(sishagonyu(avg9._avg, 1));
//                lineScore[IDX_COUNT].add(tostr(avg9._count));
//                lineScore[IDX_MAX].add(tostr(avg9._highScore));
//                if ("2".equals(param._deviationPrint)) {
//                    lineScore[IDX_HYOJUN].add(sishagonyu(avg9._stdDev, 1));
//                }
//            }
//        }
//        //log.debug("  exam = " + exam.getExamKey() + ", subclasses = " + subclasses.size());
//    }

    private void printMain(final Param param, final Vrw32alp svf, final DB2UDB db2) throws SQLException {
        // 対象の生徒たちを得る
        final List students = Student.createStudents(db2, param);
        _hasData = students.size() > 0;

        // 成績のデータを読む
        loadExam(db2, students, param._thisExam, param);
        setAttendance(db2, students, param);

        final String form2 = "KNJD105Q.frm";

        // 印刷
        for (final Iterator it = students.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            log.debug(" student " + student._schregno + " , subclass count = " + getMappedMap(student._examSubclasses, param._thisExam.getExamKey()).size() + ", course key = " + student.courseKey());
//            log.debug("record this: " + getMappedMap(student._examRecord, param._thisExam.getExamKey()).values());
//            log.debug("record bef: " + getMappedMap(student._examRecord, param._beforeExam.getExamKey()).values());

            svf.VrSetForm(form2, 4);
            printStatic(svf, param, student);
            // グラフ印字
            printBarGraph(svf, param, student, param._thisExam);
            printRadarGraph(svf, param, student, param._thisExam);
            //log.debug(" key = " + student._examRecord.keySet());
            // 成績
            printRecord(svf, param, student, param._thisExam);
        }
    }

    private void setAttendance(final DB2UDB db2, final List studentList, final Param param) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = AttendAccumulate.getAttendSemesSql(
                    param._year,
                    param._semester,
                    param._sdate,
                    param._edate,
                    param._attendParamMap
            );
            log.debug(" attend sql = " + sql);
            ps = db2.prepareStatement(sql);

            final String[] field = {"LESSON", "MLESSON", "PRESENT", "SUSPEND", "MOURNING", "SICK", "LATE", "EARLY"};
            for (final Iterator it = studentList.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();

                ps.setString(1, student._schregno);
                rs = ps.executeQuery();

                while (rs.next()) {
                    if (!SEMEALL.equals(rs.getString("SEMESTER"))) {
                        continue;
                    }

                    student._attendance = new HashMap();
                    for (int i = 0; i < field.length; i++) {
                        student._attendance.put(field[i], rs.getString(field[i]));
                    }
                    student._attendance.put("SUSPEND_MOURNING", add(rs.getString("SUSPEND"), rs.getString("MOURNING")));
                }
                DbUtils.closeQuietly(rs);
            }
        } catch (Exception e) {
            log.error("Exception", e);
        } finally {
            DbUtils.closeQuietly(ps);
            db2.commit();
        }


        try {
            final String sql = AttendAccumulate.getAttendSubclassSql(
                    param._year,
                    SEMEALL,
                    param._sdate,
                    param._edate,
                    param._attendParamMap
            );

            // log.debug(" attend subclass sql = " + sql);
            ps = db2.prepareStatement(sql);

            for (final Iterator it = studentList.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();

                ps.setString(1, student._schregno);
                rs = ps.executeQuery();

                while (rs.next()) {
                    if (!SEMEALL.equals(rs.getString("SEMESTER"))) {
                        continue;
                    }
                    final String subclasscd = rs.getString("SUBCLASSCD");

                    final int iclasscd = Integer.parseInt(subclasscd.substring(0, 2));
                    if ((Integer.parseInt(KNJDefineSchool.subject_D) <= iclasscd && iclasscd < Integer.parseInt(KNJDefineSchool.subject_U) || iclasscd == Integer.parseInt(KNJDefineSchool.subject_T))) {
//                        final BigDecimal lesson = rs.getBigDecimal("MLESSON");
                        // final BigDecimal rawSick = rs.getBigDecimal("SICK1");
//                        final BigDecimal sick = rs.getBigDecimal("SICK2");
                        // final BigDecimal rawReplacedSick = rs.getBigDecimal("RAW_REPLACED_SICK");
//                        final BigDecimal replacedSick = rs.getBigDecimal("REPLACED_SICK");

                        student._subclassAttendance.put(subclasscd, rs.getString("SICK2"));
                    }
                }
                DbUtils.closeQuietly(rs);
            }

        } catch (Exception e) {
            log.fatal("exception!", e);
        } finally {
            DbUtils.closeQuietly(ps);
            db2.commit();
        }
    }

    private static void loadExam(final DB2UDB db2, final List students, final Exam exam, final Param param) throws SQLException {
        log.error("成績関連の読込み " + exam._testCd);
        Student.loadSubClasses(db2, exam, students, param);
        Exam.loadAverageDat(db2, exam, param);
        Student.loadRecord(db2, exam, students, param);
    }

    private static String convNum(final int num) {
//        final Map m = new HashMap();
//        m.put("0", "０");
//        m.put("1", "１");
//        m.put("2", "２");
//        m.put("3", "３");
//        m.put("4", "４");
//        m.put("5", "５");
//        m.put("6", "６");
//        m.put("7", "７");
//        m.put("8", "８");
//        m.put("9", "９");
//
//        final String s = String.valueOf(num);
//        final StringBuffer stb = new StringBuffer();
//        for (int i = 0; i < s.length(); i++) {
//            final String c = String.valueOf(s.charAt(i));
//            stb.append(null == m.get(c) ? c : (String) m.get(c));
//        }
//        return stb.toString();
        return String.valueOf(num);
    }

    private void printStatic(final Vrw32alp svf, final Param param, final Student student) {
        svf.VrsOut("SCHOOL_NAME", param._schoolName);
//        svf.VrsOut("STAFFNAME", StringUtils.defaultString(param._remark2) + StringUtils.defaultString((String) param._staffs.get(student._grade + student._hrClass)));
        svf.VrsOut("NENDO", param._thisExam._title);
//      svf.VrsOut("SEMESTER", null); // 学期
//      svf.VrsOut("TESTNAME", null); // テスト名
//      svf.VrsOut("NENDO", null); // 年度
        svf.VrsOut("HR_NAME", StringUtils.defaultString(student._hrName) + convNum(Integer.parseInt(student._attendNo)) + "番");
        svf.VrsOut(getMS932ByteLength(student._name) > 20 ? "NAME_2" : "NAME", student._name);
        svf.VrsOut("DATE", StringUtils.defaultString(KNJ_EditDate.h_format_JP(param._jisshiDate)) + "実施");

//        // 個人評(期末の場合は注意事項)
//        KNJServletUtils.printDetail(svf, "PERSONAL_REMARK", student._remark, 45 * 2, 3);

//        svf.VrsOut("ITEM_TOTAL5", "全教科");

//        final String msg = param.getItemMsg();

//        svf.VrsOut("ITEM_AVG", msg + "平均");
//        svf.VrsOut("ITEM_RANK", msg + "順位");
//        svf.VrsOut("ITEM_RANK", msg + "順位");
//        if ("1".equals(param._deviationPrint)) {
//            svf.VrsOut("ITEM_DEVIATION", "偏差値");
//        } else if ("2".equals(param._deviationPrint)) {
//            svf.VrsOut("ITEM_DEVIATION", "標準偏差");
//        }

        if (null != student._attendance) {
            svf.VrsOut("LESSON", (String) student._attendance.get("LESSON")); // 授業日数
            svf.VrsOut("SUSPEND", (String) student._attendance.get("SUSPEND_MOURNING")); // 出停・忌引
            svf.VrsOut("ABSENT", (String) student._attendance.get("SICK")); // 欠席日数
            svf.VrsOut("PRESENT", (String) student._attendance.get("PRESENT")); // 出席日数
            svf.VrsOut("LATE", (String) student._attendance.get("LATE")); // 遅刻回数
            svf.VrsOut("EARLY", (String) student._attendance.get("EARLY")); // 早退回数
        }
    }

    private void printBarGraph(final Vrw32alp svf, final Param param, final Student student, final Exam exam) {

        /** 棒グラフの最大科目数. */
        final int BAR_GRAPH_MAX_ITEM = 11;
        // グラフ用のデータ作成
        final DefaultCategoryDataset scoreDataset = new DefaultCategoryDataset();
        final DefaultCategoryDataset avgDataset = new DefaultCategoryDataset();
        int i = 0;
        for (final Iterator it = getMappedMap(student._examRecord, exam.getExamKey()).values().iterator(); it.hasNext();) {
            final Record record = (Record) it.next();
            final Subclass subclass = (Subclass) param._subclasses.get(record._subclasscd);
            if (null == subclass) {
                continue;
            }

            if (subclass._isMoto) {
                continue;
            }

            if ("90".equals(record._classcd)) {
                continue;
            }

//            if (exam.getAttendSubclassCdList(student).contains(record._subclasscd)) {
//                continue;
//            }

            final String subclassKey = null != subclass._abbv && subclass._abbv.length() > 4 ? subclass._abbv.substring(0, 4) : subclass._abbv;
            scoreDataset.addValue(record._scorePercent, "本人得点", subclassKey);
            final AverageDat avgDat = (AverageDat) exam._averages.get(AverageDat.averageKey(param, student, record._subclasscd));
            final BigDecimal avgPercent = (null == avgDat) ? null : avgDat._avgPercent;

            avgDataset.addValue(avgPercent, param.getItemMsg() + "平均点", subclassKey);

            //log.info("棒グラフ⇒" + record._subclasscd + ":素点=" + record._score + ", 平均=" + avgPercent);
            if (i++ > BAR_GRAPH_MAX_ITEM) {
                break;
            }
        }
        log.debug("棒グラフ科目数: " + i);

        try {
            if (null != param._barLegendImage) {
                svf.VrsOut("BAR_LEGEND", param._barLegendImage);
            }
            svf.VrsOut("BAR_TITLE", "得点グラフ");

            // グラフの出力
            svf.VrsOut("BAR_LABEL", "得点");

            // グラフのファイルを生成
            final File outputFile = Chart.graphImageFile(Chart.createBarChart(scoreDataset, avgDataset), 1940, 930);
            if (outputFile.exists()) {
                svf.VrsOut("BAR", outputFile.toString());
                param._graphFiles.add(outputFile);
            }
        } catch (Throwable e) {
            log.error("exception or error!", e);
        }
    }

    private void printRadarGraph(final Vrw32alp svf, final Param param, final Student student, final Exam exam) {

//        final List attendSubclassCdList = exam.getAttendSubclassCdList(student);

        // データ作成
        final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (final Iterator it0 = param._classOrder.iterator(); it0.hasNext();) {
            final String classCd = (String) it0.next();

            for (final Iterator it = getMappedMap(student._examRecord, exam.getExamKey()).values().iterator(); it.hasNext();) {
                final Record record = (Record) it.next();
                if (!classCd.equals(record._classcd)) {
                    continue;
                }
//                if (attendSubclassCdList.contains(record._subclasscd)) {
//                    continue;
//                }
//                log.info("レーダーグラフ⇒" + record._subclasscd + ", " + record._deviation);
                final Class clazz = (Class) param._classes.get(record._classkey);
                final String name = (null == clazz) ? "" : clazz._abbv;
                dataset.addValue(record._deviation, "本人偏差値", name);// MAX80, MIN20

                dataset.addValue(50.0, "偏差値50", name);
            }
        }

        try {
            // チャート作成
            final JFreeChart chart = Chart.createRaderChart(dataset);

            // グラフのファイルを生成
            final File outputFile = Chart.graphImageFile(chart, 938, 784);
            param._graphFiles.add(outputFile);

            // 画像
            if (null != param._radarLegendImage) {
                svf.VrsOut("RADER_LEGEND", param._radarLegendImage);
            }

            svf.VrsOut("RADER_TITLE", "教科間バランス");

            // グラフの出力
            if (0 < dataset.getColumnCount() && outputFile.exists()) {
                svf.VrsOut("RADER", outputFile.toString());
            }
        } catch (Throwable e) {
            log.error("exception or error!", e);
        }
    }

    private void printRecord(final Vrw32alp svf, final Param param, final Student student, final Exam exam) {

        final boolean isKimatsu = "02".equals(exam.getKindCd());
        if (isKimatsu) {
            // 2行
            String[] subclasscds = { ALL5, ALL9 };
            for (int i = 0; i < subclasscds.length; i++) {
                final String si = String.valueOf(i + 1);
                final String subclasscd = subclasscds[i];
                // 得点
                final Record record = (Record) getMappedMap(student._examRecordOther, exam.getExamKey()).get(subclasscd);
                if (null == record) {
                    log.info(" k null rec exam key = " + exam.getExamKey() + " / subclasscd = " + subclasscd);
                } else {
                    svf.VrsOutn("TOTAL2_" + si, 1, tostr(record._score)); // 合計点
                    svf.VrsOutn("AVERAGE2_" + si, 1, sishagonyu(record._avg, 1)); // 平均
                    if ("1".equals(param._juniPrint)) {
                        svf.VrsOutn("RANK2_" + si, 1, tostr(record._rank)); // 席次
                    }
                }

                // 平均点
                final AverageDat avgDat = (AverageDat) exam._averagesOther.get(AverageDat.averageKey(param, student, subclasscd));
                if (null == avgDat) {
                    log.info(" k null avg key = " + AverageDat.averageKey(param, student, subclasscd) + " / subclasscd = " + subclasscd);
                } else {
                    svf.VrsOutn("TOTAL2_" + si, 2, sishagonyu(avgDat._avg, 1)); // 合計点
                    svf.VrsOutn("AVERAGE2_" + si, 2, sishagonyu(avgDat._avgAvg, 1)); // 平均
                }
            }

            svf.VrsOut("REMARK", "（注）上段5科、下段9科"); // 合計備考
        } else {
            // 中央
            final String subclasscd = ALL5;
            // 得点
            final Record record = (Record) getMappedMap(student._examRecordOther, exam.getExamKey()).get(subclasscd);
            if (null == record) {
                log.info(" null exam key = " + exam.getExamKey() + " / subclasscd = " + subclasscd);
            } else {
                svf.VrsOutn("TOTAL1", 1, tostr(record._score)); // 合計点
                svf.VrsOutn("AVERAGE1", 1, sishagonyu(record._avg, 1)); // 平均
                if ("1".equals(param._juniPrint)) {
                    svf.VrsOutn("RANK1", 1, tostr(record._rank)); // 席次
                }
            }

            // 平均点
            final AverageDat avgDat = (AverageDat) exam._averagesOther.get(AverageDat.averageKey(param, student, subclasscd));
            // 平均点
            if (null == avgDat) {
                log.info(" null avg key = " + AverageDat.averageKey(param, student, subclasscd) + " / subclasscd = " + subclasscd);
            } else {
                svf.VrsOutn("TOTAL1", 2, sishagonyu(avgDat._avg, 1)); // 合計点
                svf.VrsOutn("AVERAGE1", 2, sishagonyu(avgDat._avgAvg, 1)); // 平均
            }
        }

        int i = 0;
        final List subclassList = new ArrayList(getMappedMap(student._examSubclasses, exam.getExamKey()).values());
        Collections.sort(subclassList, new SubclassComparator(student, exam));
        for (final Iterator it = subclassList.iterator(); it.hasNext();) {
            final Subclass subclass = (Subclass) it.next();

            if ("90".equals(subclass.getClassCd(param))) {
                continue;
            }
            
//            log.info(" subclass cd = " + subclass._code + " | " + student._subclassGroup.get(subclass._code) + " / " + student._subclassGroup);
//            if (exam.getAttendSubclassCdList(student).contains(subclass._code)) {
//                continue;
//            }

            final Record record = (Record) getMappedMap(student._examRecord, exam.getExamKey()).get(subclass._code);
            final AverageDat avgDat = (AverageDat) exam._averages.get(AverageDat.averageKey(param, student, subclass._code));
            final Class clazz = (Class) param._classes.get(subclass.getClassKey(param));
            if (null == clazz) {
                continue;
            }
//            if (_fiveClassCd.contains(clazz._code)) {
//                amikake(getFClass(), clazz._name);
//            } else {
//                _svf.VrsOut(getFClass(), clazz._name);
//            }

            if (getMS932ByteLength(clazz._name) > 6) {
                svf.VrsOut("CLASS2_1", clazz._name); // 教科
            } else {
                svf.VrsOut("CLASS", clazz._name); // 教科
            }
//            svf.VrsOut("SUBCLASS", subclass._name); // 科目

            if (null != record) {
                
                final String score = null != record._scoreDi ? record._scoreDi : tostr(record._score);
//            svf.VrsOut("SCORE", param.isUnderScore(score) ? "(" + score + ")" : score);
                svf.VrsOut("SCORE", score);
                
//            svf.VrsOut("RANK", tostr(record._rank));
//            if ("1".equals(param._deviationPrint)) {
//                svf.VrsOut("DEVIATION", sishagonyu(record._deviation, 1));
//            }
            }

            if (null != avgDat) {
                svf.VrsOut("AVERAGE", sishagonyu(avgDat._avg, 1));
//                svf.VrsOut("MAX_SCORE", tostr(avgDat._highScore));
//                svf.VrsOut("EXAMINEE", tostr(avgDat._count));    // 受験者数
//                if ("2".equals(param._deviationPrint)) {
//                    svf.VrsOut("DEVIATION", sishagonyu(avgDat._stdDev, 1));
//                }
            }

            svf.VrsOut("ABSENT_NUM", StringUtils.defaultString((String) student._subclassAttendance.get(subclass._code), "0")); // 欠時

            svf.VrEndRecord();
            if (++i >= TABLE_SUBCLASS_MAX) {
                break;
            }
        }
        if (i == 0) {
            svf.VrsOut("CLASS", "\n");
            svf.VrEndRecord(); // データが無い時もこのコマンドを発行しないと印刷されないから
        }
        //log.debug("  exam = " + exam.getExamKey() + ", subclasses = " + subclasses.size());
    }

    private static class Exam {

        final String _year;
        final String _semester;
        final String _testCd;
        private String _grade;
        private String _examName = "";
        private String _semestername = "";
        private String _title = "";

        /** 成績平均データ。 */
        private Map _averages = new HashMap();
        /** 成績平均データ。3教科,5教科用 */
        private Map _averagesOther = new HashMap();
//        /** 前回の換算した得点、平均、最高点 */
//        private ConvertedScore _convertedScore;
//        /** 合併元科目コード */
//        private Map _attendSubclassCdMap = Collections.EMPTY_MAP;

        public Exam(final String year, final String semester, final String testCd) {
            _year = year;
            _semester = semester;
            _testCd = testCd;
        }

        public boolean isValid() {
            return null != _testCd;
        }

        public String getExamKey() {
            return _year + "-" + _semester + "-" + _testCd;
        }

        public String getKindCd() {
            return null == _testCd ? null : _testCd.substring(0, 2);
        }

        public String getItemCd() {
            return null == _testCd ? null : _testCd.substring(2, 4);
        }

        public String getScoreDiv() {
            return null == _testCd ? null : _testCd.substring(4);
        }

        private void load(final Param param, final DB2UDB db2) {
//            _convertedScore = null;
            loadExam(db2, param);
//            loadAttendSubclassCdMap(db2, param);
        }

//        private void loadAttendSubclassCdMap(final DB2UDB db2, final Param param) {
//            final Map map = new HashMap();
//            PreparedStatement ps = null;
//            ResultSet rs = null;
//            try {
//                final StringBuffer stb = new StringBuffer();
////                final String flg = "9900".equals(_testCd) ? "2" : "1";
////                stb.append("   SELECT ");
////                stb.append("       T1.GRADE || T1.COURSECD || T1.MAJORCD || T1.COURSECODE AS KEY, ");
////                if ("1".equals(param._useCurriculumcd)) {
////                    stb.append("       T1.ATTEND_CLASSCD || '-' || T1.ATTEND_SCHOOL_KIND || '-' || T1.ATTEND_CURRICULUM_CD || '-' || ");
////                }
////                stb.append("       T1.ATTEND_SUBCLASSCD AS ATTEND_SUBCLASSCD ");
////                stb.append("   FROM ");
////                stb.append("       SUBCLASS_WEIGHTING_COURSE_DAT T1 ");
////                stb.append("   WHERE ");
////                stb.append("       T1.YEAR = '" + _year + "' ");
////                stb.append("       AND T1.FLG = '" + flg + "' ");
////
////                ps = db2.prepareStatement(stb.toString());
////                rs = ps.executeQuery();
////                while (rs.next()) {
////                    getMappedList(map, rs.getString("KEY")).add(rs.getString("ATTEND_SUBCLASSCD"));
////                }
//
//                stb.append("   SELECT ");
//                if ("1".equals(param._useCurriculumcd)) {
//                    stb.append("       T1.ATTEND_CLASSCD || '-' || T1.ATTEND_SCHOOL_KIND || '-' || T1.ATTEND_CURRICULUM_CD || '-' || ");
//                }
//                stb.append("       T1.ATTEND_SUBCLASSCD AS ATTEND_SUBCLASSCD ");
//                stb.append("   FROM ");
//                stb.append("       SUBCLASS_REPLACE_COMBINED_DAT T1 ");
//                stb.append("   WHERE ");
//                stb.append("       T1.YEAR = '" + _year + "' ");
//
//                ps = db2.prepareStatement(stb.toString());
//                rs = ps.executeQuery();
//                while (rs.next()) {
//                    getMappedList(map, "0").add(rs.getString("ATTEND_SUBCLASSCD"));
//                }
//            } catch (Exception e) {
//                log.error("exception!", e);
//            } finally {
//                DbUtils.closeQuietly(null, ps, rs);
//                db2.commit();
//            }
//            _attendSubclassCdMap = map;
//        }

//        public List getAttendSubclassCdList(final Student student) {
////            return getMappedList(_attendSubclassCdMap, student.attendSubclassCdKey());
//            return getMappedList(_attendSubclassCdMap, "0");
//        }

        private void loadExam(final DB2UDB db2, final Param param) {
            if (null == _year) {
                _title = "";
                return;
            }
            PreparedStatement ps = null;
            ResultSet rs = null;
            _examName = "";
            try {
                String sql;
                if ("1".equals(param._use_school_detail_gcm_dat)) {
                    sql = "SELECT TESTITEMNAME FROM TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV WHERE YEAR = '" + _year + "'" +
                            " AND SEMESTER = '" + _semester + "' " +
                            " AND GRADE = '00' " +
                            " AND COURSECD || '-' || MAJORCD = '" + param._major + "' " +
                            " AND TESTKINDCD || TESTITEMCD || SCORE_DIV = '" + getKindCd() + getItemCd() + getScoreDiv() + "' ";
                    if ("1".equals(param._use_prg_schoolkind)) {
                        sql += " AND SCHOOLCD = '" + param.SCHOOLCD + "' " +
                                " AND SCHOOL_KIND = '" + param.SCHOOLKIND + "' ";
                    } else if ("1".equals(param._useSchool_KindField) && !StringUtils.isBlank(param.SCHOOLKIND)) {
                        sql += " AND SCHOOLCD = '" + param.SCHOOLCD + "' " +
                               " AND SCHOOL_KIND = '" + param.SCHOOLKIND + "' ";
                    }
                } else {
                    final StringBuffer stb = new StringBuffer();
                    stb.append("SELECT testitemname FROM testitem_mst_countflg_new_sdiv ");
                    stb.append(" WHERE year = '" + _year + "' ");
                    stb.append("   AND semester = '"  + _semester + "'");
                    stb.append("   AND testkindcd = '" + getKindCd() + "' ");
                    stb.append("   AND testitemcd = '" + getItemCd() + "'");
                    stb.append("   AND score_div = '" + getScoreDiv() + "'");
                    sql = stb.toString();
                }

                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    _examName = StringUtils.defaultString(rs.getString("testitemname"));
                }
            } catch (final SQLException e) {
                log.error("考査名取得エラー。", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            _semestername = "";
            try {
                final String sql = "SELECT semestername FROM semester_mst WHERE year = '" + _year + "' AND semester = '" + _semester + "'";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    _semestername = StringUtils.defaultString(rs.getString("semestername"));
                }
            } catch (final SQLException e) {
                log.error("学期名取得エラー。", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            _title = param._gradeName1 + "　" + _semestername + "　" + _examName;
        }

//        String nendo() {
//            return KenjaProperties.gengou(Integer.parseInt(_year)) + "(" + _year + ")年度";
//        }

        /**
         * 成績平均データを読込み、セットする。
         * @param db2 DB
         * @param kindCd テスト種別コード
         * @param itemCd テスト項目コード
         * @param outAvgDat 成績平均データの格納場所
         * @param outAvgDatOther 成績平均データ(3,5教科)の格納場所
         * @throws SQLException SQL例外
         */
        private static void loadAverageDat(
                final DB2UDB db2,
                final Exam exam,
                final Param param
        ) throws SQLException {

            String selectFrom;
            selectFrom = "WITH RANK_AVG_AVG AS ("
            + "SELECT"
            + "  t1.classcd,"
            + "  t1.school_kind,"
            + "  t1.curriculum_cd,"
            + "  t1.subclasscd,"
            + "  '3' as avg_div," // コースのみ
            + "  t2.grade,"
            + "  avg(t1.avg) as avg_avg"
            + " FROM"
            + "  record_rank_sdiv_dat t1 "
            + "  INNER JOIN schreg_regd_dat t2 on t2.schregno = t1.schregno "
            + "      AND t2.year = t1.year "
            + "      AND t2.semester = t1.semester "
            + " WHERE"
            + "    t1.year = '" + exam._year + "' AND"
            + "    t1.semester = '" + exam._semester + "' AND"
            + "    t1.testkindcd = '" + exam.getKindCd() + "' AND"
            + "    t1.testitemcd = '" + exam.getItemCd() + "' AND"
            + "    t1.score_div = '" + exam.getScoreDiv() + "' AND"
            + "    t2.grade='" + exam._grade + "' "
            + " GROUP BY "
            + "  t1.classcd,"
            + "  t1.school_kind,"
            + "  t1.curriculum_cd,"
            + "  t1.subclasscd,"
            + "  t2.grade "
            + ") ";

            selectFrom += "SELECT";
            selectFrom += "  case when t1.subclasscd in ('" + ALL3 + "', '" + ALL5 + "', '" + ALL9 + "') then t1.subclasscd  ";
            selectFrom += "   else t1.classcd || '-' || t1.school_kind || '-' || t1.curriculum_cd || '-' || t1.subclasscd ";
            selectFrom += "  end as subclasscd,";
            selectFrom += ""
                + "  t1.avg,"
                + "  t3.avg_avg,"
                + "  t1.stddev,"
                + "  t1.highscore,"
                + "  t1.count,"
                + "  t1.coursecd,"
                + "  t1.majorcd,"
                + "  t1.coursecode,"
                + "  value(t2.perfect, 100) as perfect"
                + " FROM"
                + "  record_average_sdiv_dat t1"
                + "  LEFT JOIN perfect_record_sdiv_dat t2 ON t2.year = t1.year AND "
                + "    t2.semester = t1.semester AND"
                + "    t2.testkindcd = t1.testkindcd AND"
                + "    t2.testitemcd = t1.testitemcd AND"
                + "    t2.score_div = t1.score_div AND";
            selectFrom += ""
                    + "    t2.classcd = t1.classcd AND"
                    + "    t2.school_kind = t1.school_kind AND"
                    + "    t2.curriculum_cd = t1.curriculum_cd AND";
            selectFrom += ""
                + "    t2.subclasscd = t1.subclasscd AND"
                + "    t2.grade = (case when t2.div = '01' then '00' else '" + exam._grade + "' end) AND"
                + "    t2.coursecd || t2.majorcd || t2.coursecode = "
                + "        (case when t2.div in ('01', '02') then '00000000' "
                + "              when t2.div = '04' then '0' || t1.majorcd || '0000' "
                + "              else t1.coursecd || t1.majorcd || t1.coursecode end) "
                ;
            selectFrom += " LEFT JOIN RANK_AVG_AVG t3 ON t3.avg_div = t1.avg_div AND ";
            selectFrom += "  (t1.subclasscd in ('" + ALL3 + "', '" + ALL5 + "', '" + ALL9 + "') and t3.subclasscd = t1.subclasscd  ";
            selectFrom += "   or "
                    + "    t3.classcd = t1.classcd AND"
                    + "    t3.school_kind = t1.school_kind AND"
                    + "    t3.curriculum_cd = t1.curriculum_cd AND"
                    + "    t3.subclasscd = t1.subclasscd) AND"
                    + "    t3.grade = t1.grade ";
            String where;
            where = " WHERE"
                    + "    t1.year = '" + exam._year + "' AND"
                    + "    t1.semester = '" + exam._semester + "' AND"
                    + "    t1.testkindcd = '" + exam.getKindCd() + "' AND"
                    + "    t1.testitemcd = '" + exam.getItemCd() + "' AND"
                    + "    t1.score_div = '" + exam.getScoreDiv() + "' AND";
            if ("1".equals(param._groupDiv)) {
                where += " t1.avg_div = '1' AND"
                    + "    t1.grade = '" + exam._grade + "' AND"
                    + "    t1.hr_class = '000' AND"
                    + "    t1.coursecd = '0' AND"
                    + "    t1.majorcd = '000' AND"
                    + "    t1.coursecode = '0000'"
                    ;
            } else if ("3".equals(param._groupDiv)) {
                where += " t1.avg_div = '5' AND"
                    + "    t1.grade = '" + exam._grade + "' AND"
                    + "    t1.hr_class = '000'"
                    ;
            } else {
                where += " t1.avg_div = '3' AND"
                    + "    t1.grade = '" + exam._grade + "' AND"
                    + "    t1.hr_class = '000'"
                    ;
            }

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = selectFrom + where;
                log.info(" avg sql = " + sql.toString());
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String subclasscd = rs.getString("subclasscd");
                    final Subclass subclass = (Subclass) param._subclasses.get(subclasscd);
                    if (null == subclass) {
                        if (!(ALL3.equals(subclasscd) || ALL5.equals(subclasscd) || ALL9.equals(subclasscd))) {
                            log.warn("成績平均データが科目マスタに無い!:" + subclasscd);
                        }
                    }
                    final BigDecimal avg = rs.getBigDecimal("avg");
                    BigDecimal avgPercent = null;
                    final int perfect = rs.getInt("PERFECT");
                    if (100 == perfect) {
                        avgPercent = avg;
                    } else if (null != avg){
                        avgPercent = avg.multiply(new BigDecimal(100)).divide(new BigDecimal(perfect), 1, BigDecimal.ROUND_HALF_UP);
                    }
                    final BigDecimal stdDev = rs.getBigDecimal("STDDEV");
                    final BigDecimal avgAvg = rs.getBigDecimal("AVG_AVG");
                    final Integer count = KNJServletUtils.getInteger(rs, "count");
                    final Integer highScore = KNJServletUtils.getInteger(rs, "highscore");
                    final String coursecd = rs.getString("coursecd");
                    final String majorcd = rs.getString("majorcd");
                    final String coursecode = rs.getString("coursecode");

                    final AverageDat avgDat = new AverageDat(subclasscd, avg, avgPercent, stdDev, avgAvg, highScore, count, coursecd, majorcd, coursecode);
                    final String key = AverageDat.averageKey(param, subclasscd, coursecd, majorcd, coursecode);
                    if (ALL3.equals(subclasscd) || ALL5.equals(subclasscd) || ALL9.equals(subclasscd)) {
                        exam._averagesOther.put(key, avgDat);
                    } else {
                        exam._averages.put(key, avgDat);
                    }
                }
            } catch (final SQLException e) {
                log.error("成績平均データの取得でエラー", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            log.debug("テストコード=" + exam.getExamKey() + " の成績平均データの件数=" + exam._averages.size());
        }
    }

    private static class Student {
        final String _schregno;
        final String _grade;
        final String _hrClass;
        final String _attendNo;
        final String _hrName;

        final String _courseCd;
        final String _majorCd;
        final String _courseCode;
        final String _coursegroupCd;

        final String _name;

        /** 成績所見データ. */
        final String _remark;

        /** 成績科目 */
        private final Map _examSubclasses = new TreeMap();

        /** 成績データ。 */
        private final Map _examRecord = new TreeMap();

        /** 成績データ。3教科,5教科用 */
        private final Map _examRecordOther = new HashMap();

        private Map _attendance = null;
        private Map _subclassAttendance = new HashMap();
        private Map _subclassGroup = new HashMap();

        private Student(
                final String schregno,
                final String grade,
                final String hrClass,
                final String attendNo,
                final String hrName,
                final String courseCd,
                final String majorCd,
                final String courseCode,
                final String coursegroupCd,
                final String name,
                final String remark
        ) {
            _schregno = schregno;
            _grade = grade;
            _hrClass = hrClass;
            _attendNo = attendNo;
            _hrName = hrName;
            _courseCd = courseCd;
            _majorCd = majorCd;
            _courseCode = courseCode;
            _coursegroupCd = coursegroupCd;
            _name = name;
            _remark = remark;
        }

        public String attendSubclassCdKey() {
            return _grade + courseKey();
        }

        public String courseKey() {
            return _courseCd + _majorCd + _courseCode;
        }

        public String courseGroupKey() {
            return "0" + _coursegroupCd + "0000";
        }

        public String toString() {
            return _schregno + "/" + _name;
        }

        private static List createStudents(final DB2UDB db2, final Param param) throws SQLException {
            final List rtn = new LinkedList();

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer sql = new StringBuffer();
                sql.append("SELECT");
                sql.append("  regd.schregno,");
                sql.append("  regd.grade,");
                sql.append("  regd.hr_class,");
                sql.append("  regd.attendno,");
                sql.append("  hdat.hr_name,");
                sql.append("  regd.coursecd,");
                sql.append("  regd.majorcd,");
                sql.append("  regd.coursecode,");
                sql.append("  base.name,");
                sql.append("  t3.remark1,");
                sql.append("  t4.group_cd as coursegroupCd");
                sql.append(" FROM");
                sql.append("  schreg_regd_dat regd ");
                sql.append("    INNER JOIN schreg_base_mst base ON base.schregno = regd.schregno");
                sql.append("    INNER JOIN schreg_regd_hdat hdat ON hdat.year = regd.year AND");
                sql.append("      hdat.semester = regd.semester AND");
                sql.append("      hdat.grade = regd.grade AND");
                sql.append("      hdat.hr_class = regd.hr_class ");
                sql.append("    LEFT JOIN hexam_record_remark_sdiv_dat t3 ON");
                sql.append("      regd.year = '" + param._thisExam._year + "' AND");
                sql.append("      regd.semester = '" + param._thisExam._semester + "' AND");
                sql.append("      t3.testkindcd = '" + param._thisExam.getKindCd() + "' AND");
                sql.append("      t3.testitemcd = '" + param._thisExam.getItemCd() + "' AND");
                sql.append("      t3.score_div = '" + param._thisExam.getScoreDiv() + "' AND");
                sql.append("      regd.schregno = t3.schregno AND");
                sql.append("      t3.remark_div = '2'"); // '2'固定
                sql.append("    LEFT JOIN course_group_cd_dat t4 ON regd.year = t4.year AND");
                sql.append("      regd.grade = t4.grade AND");
                sql.append("      regd.coursecd = t4.coursecd AND");
                sql.append("      regd.majorcd = t4.majorcd AND");
                sql.append("      regd.coursecode = t4.coursecode");
                sql.append(" WHERE");
                sql.append("  regd.year = '" + param._year + "' AND");
                sql.append("  regd.semester = '" + param._semester + "' AND");
                if ("1".equals(param._categoryIsClass)) {
                    sql.append("  regd.grade || '-' || regd.hr_class IN " + SQLUtils.whereIn(true, param._categorySelected));
                } else {
                    sql.append("  regd.schregno IN " + SQLUtils.whereIn(true, param._categorySelected));
                }
                sql.append(" ORDER BY regd.grade, regd.hr_class, regd.attendno ");

                //log.debug(" regd sql = " + sql);
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String schregno = rs.getString("schregno");
                    final String grade = rs.getString("grade");
                    final String hrclass = rs.getString("hr_class");
                    final String attendno = rs.getString("attendno");
                    final String hrName = rs.getString("hr_name");
                    final String coursecd = rs.getString("coursecd");
                    final String majorcd = rs.getString("majorcd");
                    final String coursecode = rs.getString("coursecode");
                    final String coursegroupCd = rs.getString("coursegroupCd");
                    final String name = rs.getString("name");
                    final String remark = rs.getString("remark1");

                    final Student student = new Student(
                            schregno,
                            grade,
                            hrclass,
                            attendno,
                            hrName,
                            coursecd,
                            majorcd,
                            coursecode,
                            coursegroupCd,
                            name,
                            remark
                    );
                    rtn.add(student);
                }
            } catch (final SQLException e) {
                log.error("生徒の基本情報取得でエラー", e);
                throw e;
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtn;
        }

        private static void loadSubClasses(final DB2UDB db2, final Exam exam, final List students, final Param param) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final StringBuffer sql = new StringBuffer();
                sql.append("select");
                if ("1".equals(param._useCurriculumcd)) {
                    sql.append("  distinct T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD");
                } else {
                    sql.append("  distinct T1.SUBCLASSCD");
                }
                sql.append(" from");
                sql.append("  RECORD_SCORE_DAT T1 ");
                sql.append(" where");
                sql.append("  T1.YEAR = '" + exam._year + "' and");
                sql.append("  T1.SEMESTER = '" + exam._semester + "' and");
                sql.append("  T1.TESTKINDCD = '" + exam.getKindCd() + "' and");
                sql.append("  T1.TESTITEMCD = '" + exam.getItemCd() + "' and");
                sql.append("  T1.SCORE_DIV = '" + exam.getScoreDiv() + "' and");
                sql.append("  T1.SCHREGNO = ? ");
                //log.debug("成績入力データの科目のSQL=" + sql);

                ps = db2.prepareStatement(sql.toString());
                for (final Iterator it = students.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();
                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        final String subclasscd = rs.getString("SUBCLASSCD");

                        final Subclass subClass = (Subclass) param._subclasses.get(subclasscd);
                        if (null != subClass) {
                            getMappedMap(student._examSubclasses, exam.getExamKey()).put(subclasscd, subClass);
                        }
                    }
                    DbUtils.closeQuietly(rs);
                }
            } catch (final SQLException e) {
                log.fatal("成績入力データの科目取得でエラー", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        /**
         * 生徒に成績データを関連付ける。
         * @param db2 DB
         * @param kindCd テスト種別コード
         * @param itemCd テスト項目コード
         * @param scoreDistributions 得点分布の格納場所
         * @param students 生徒たち
         */
        private static void loadRecord(
                final DB2UDB db2,
                final Exam exam,
                final List students,
                final Param param
        ) {
            final String gradeRank  = "2".equals(param._outputKijun) ? "grade_avg_rank" : "3".equals(param._outputKijun) ? "grade_deviation_rank" : "grade_rank";
            final String courseRank = "2".equals(param._outputKijun) ? "course_avg_rank" : "3".equals(param._outputKijun) ? "course_deviation_rank" : "course_rank";
            final String majorRank = "2".equals(param._outputKijun) ? "major_avg_rank" : "3".equals(param._outputKijun) ? "major_deviation_rank" : "major_rank";

            final StringBuffer sql = new StringBuffer();
            /* 通常の成績 */
            sql.append("SELECT");
            sql.append("  t1.classcd,");
            if ("1".equals(param._useCurriculumcd)) {
                sql.append("  t1.classcd || '-' || t1.school_kind as class_key,");
                sql.append("  t1.classcd || '-' || t1.school_kind || '-' || t1.curriculum_cd || '-' || t1.subclasscd as subclasscd,");
            } else {
                sql.append("  t1.classcd as class_key,");
                sql.append("  t1.subclasscd,");
            }
            sql.append("  t2.score,");
            sql.append("  t1.value_di as score_di,");
            sql.append("  t2." + gradeRank + " as grade_rank,");
            sql.append("  t2.grade_deviation,");
            sql.append("  t2." + courseRank + " as course_rank,");
            sql.append("  t2.course_deviation,");
            sql.append("  t2." + majorRank + " as major_rank,");
            sql.append("  t2.major_deviation,");
            sql.append("  t3.div,");
            sql.append("  case when tgrp3.year is not null or tgrp5.year is not null then '1' end as IS_GROUP ,");
            sql.append("  value(t3.perfect, 100) as perfect");
            sql.append(" FROM");
            sql.append("  record_score_dat t1 ");
            sql.append(" LEFT JOIN schreg_regd_dat regd on regd.schregno = t1.schregno AND ");
            sql.append("    t1.year = regd.year AND");
            sql.append("    regd.semester = '" + param._semester  + "' ");
            sql.append(" LEFT JOIN record_rank_sdiv_dat t2 ON t1.year = t2.year AND");
            sql.append("    t1.semester = t2.semester AND");
            sql.append("    t1.testkindcd = t2.testkindcd AND");
            sql.append("    t1.testitemcd = t2.testitemcd AND");
            sql.append("    t1.score_div = t2.score_div AND");
            if ("1".equals(param._useCurriculumcd)) {
                sql.append("    t1.classcd = t2.classcd AND");
                sql.append("    t1.school_kind = t2.school_kind AND");
                sql.append("    t1.curriculum_cd = t2.curriculum_cd AND");
            }
            sql.append("    t1.subclasscd = t2.subclasscd AND");
            sql.append("    t1.schregno = t2.schregno ");
            sql.append(" LEFT JOIN perfect_record_sdiv_dat t3 ON t1.year = t3.year AND");
            sql.append("    t1.semester = t3.semester AND");
            sql.append("    t1.testkindcd = t3.testkindcd AND");
            sql.append("    t1.testitemcd = t3.testitemcd AND");
            sql.append("    t1.score_div = t3.score_div AND");
            if ("1".equals(param._useCurriculumcd)) {
                sql.append("    t1.classcd = t3.classcd AND");
                sql.append("    t1.school_kind = t3.school_kind AND");
                sql.append("    t1.curriculum_cd = t3.curriculum_cd AND");
            }
            sql.append("    t1.subclasscd = t3.subclasscd AND");
            sql.append("    t3.grade = (case when t3.div = '01' then '00' else ? end) AND ");
            sql.append("    t3.coursecd || t3.majorcd || t3.coursecode = ");
            sql.append("        (case when t3.div in ('01', '02') then '00000000' ");
            sql.append("              when t3.div = '04' then '0' || ? || '0000' ");
            sql.append("              else ? end) ");
            sql.append(" LEFT JOIN rec_subclass_group_dat tgrp3 ON t1.year = tgrp3.year AND");
            sql.append("    '3' = tgrp3.group_div AND");
            sql.append("    regd.grade = tgrp3.grade AND");
            sql.append("    regd.coursecd = tgrp3.coursecd AND");
            sql.append("    regd.majorcd = tgrp3.majorcd AND");
            sql.append("    regd.coursecode = tgrp3.coursecode AND");
            sql.append("    t1.classcd = tgrp3.classcd AND");
            sql.append("    t1.school_kind = tgrp3.school_kind AND");
            sql.append("    t1.curriculum_cd = tgrp3.curriculum_cd AND");
            sql.append("    t1.subclasscd = tgrp3.subclasscd ");
            sql.append(" LEFT JOIN rec_subclass_group_dat tgrp5 ON t1.year = tgrp5.year AND");
            sql.append("    '5' = tgrp5.group_div AND");
            sql.append("    regd.grade = tgrp5.grade AND");
            sql.append("    regd.coursecd = tgrp5.coursecd AND");
            sql.append("    regd.majorcd = tgrp5.majorcd AND");
            sql.append("    regd.coursecode = tgrp5.coursecode AND");
            sql.append("    t1.classcd = tgrp5.classcd AND");
            sql.append("    t1.school_kind = tgrp5.school_kind AND");
            sql.append("    t1.curriculum_cd = tgrp5.curriculum_cd AND");
            sql.append("    t1.subclasscd = tgrp5.subclasscd ");
            sql.append(" WHERE");
            sql.append("  t1.year = '" + exam._year + "' AND");
            sql.append("  t1.semester = '" + exam._semester + "' AND");
            sql.append("  t1.testkindcd = '" + exam.getKindCd() + "' AND");
            sql.append("  t1.testitemcd = '" + exam.getItemCd() + "' AND");
            sql.append("  t1.score_div = '" + exam.getScoreDiv() + "' AND");
//            sql.append("  (t1.score IS NOT NULL or t1.value_di IS NOT NULL) AND");
            sql.append("  t1.schregno = ?");

            final StringBuffer sql1 = new StringBuffer();
            sql1.append("SELECT");
            sql1.append("  subclasscd,");
            sql1.append("  score,");
            sql1.append("  avg,");
            sql1.append("  " + gradeRank + " as grade_rank,");
            sql1.append("  grade_deviation,");
            sql1.append("  " + courseRank + " as course_rank,");
            sql1.append("  course_deviation,");
            sql1.append("  " + majorRank + " as major_rank,");
            sql1.append("  major_deviation");
            sql1.append(" FROM record_rank_sdiv_dat");
            sql1.append(" WHERE");
            sql1.append("  year = '" + exam._year + "' AND");
            sql1.append("  semester = '" + exam._semester + "' AND");
            sql1.append("  testkindcd = '" + exam.getKindCd() + "' AND");
            sql1.append("  testitemcd = '" + exam.getItemCd() + "' AND");
            sql1.append("  score_div = '" + exam.getScoreDiv() + "' AND");
            sql1.append("  subclasscd IN ('" + ALL3 + "', '" + ALL5 + "', '" + ALL9 + "') AND");
            sql1.append("  schregno= ?");

            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                ps = db2.prepareStatement(sql.toString());
                for (final Iterator it = students.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();
                    ps.setString(1, student._grade);
                    ps.setString(2, student._coursegroupCd);
                    ps.setString(3, student._courseCd + student._majorCd + student._courseCode);
                    ps.setString(4, student._schregno);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        final String subclasscd = rs.getString("subclasscd");
                        Subclass subClass = (Subclass) param._subclasses.get(subclasscd);
                        if (null == subClass) {
                            log.warn("対象成績データが科目マスタに無い!:" + subclasscd);
                            final String key;
                            if ("1".equals(param._useCurriculumcd)) {
                                final String[] split = StringUtils.split(subclasscd, "-");
                                key = split[0] + "-" + split[1];
                            } else {
                                key = subclasscd.substring(0, 2);
                            }
                            final Class clazz = (Class) param._classes.get(key);
                            if (null == clazz) {
                                continue;
                            }
                            subClass = new Subclass(subclasscd, clazz._name, clazz._abbv);
                        }

                        final Integer score = KNJServletUtils.getInteger(rs, "score");
                        Integer scorePercent = null;
                        final int perfect = rs.getInt("PERFECT");
                        if (100 == perfect) {
                            scorePercent = score;
                        } else if (null != score) {
                            scorePercent = new Integer(new BigDecimal(rs.getString("SCORE")).multiply(new BigDecimal(100)).divide(new BigDecimal(perfect), 0, BigDecimal.ROUND_HALF_UP).intValue());
                        }
                        final String scoreDi = rs.getString("score_di");
                        final Integer rank;
                        final BigDecimal deviation;
                        if ("1".equals(param._groupDiv)) {
                            rank = KNJServletUtils.getInteger(rs, "grade_rank");
                            deviation = rs.getBigDecimal("grade_deviation");
                        } else if ("3".equals(param._groupDiv)) {
                            rank = KNJServletUtils.getInteger(rs, "major_rank");
                            deviation = rs.getBigDecimal("major_deviation");
                        } else {
                            rank = KNJServletUtils.getInteger(rs, "course_rank");
                            deviation = rs.getBigDecimal("course_deviation");
                        }
                        student._subclassGroup.put(subclasscd, rs.getString("IS_GROUP"));

                        final Record rec = new Record(rs.getString("CLASSCD"), rs.getString("CLASS_KEY"), subclasscd, score, scoreDi, scorePercent, null, rank, deviation);
                        getMappedMap(student._examRecord, exam.getExamKey()).put(subclasscd, rec);
                    }
                    DbUtils.closeQuietly(rs);
                }

                DbUtils.closeQuietly(null, ps, rs);

                ps = db2.prepareStatement(sql1.toString());

                for (final Iterator it = students.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        final String subclasscd = rs.getString("subclasscd");
                        final Integer score = KNJServletUtils.getInteger(rs, "score");
                        final BigDecimal avg = rs.getBigDecimal("avg");
                        final Integer rank;
                        final BigDecimal deviation;
                        if ("1".equals(param._groupDiv)) {
                            rank = KNJServletUtils.getInteger(rs, "grade_rank");
                            deviation = rs.getBigDecimal("grade_deviation");
                        } else if ("3".equals(param._groupDiv)) {
                            rank = KNJServletUtils.getInteger(rs, "major_rank");
                            deviation = rs.getBigDecimal("major_deviation");
                        } else {
                            rank = KNJServletUtils.getInteger(rs, "course_rank");
                            deviation = rs.getBigDecimal("course_deviation");
                        }

                        final Record rec = new Record("99", "99", subclasscd, score, null, null, avg, rank, deviation);
                        getMappedMap(student._examRecordOther, exam.getExamKey()).put(subclasscd, rec);
                    }
                    DbUtils.closeQuietly(rs);
                }
            } catch (final SQLException e) {
                log.error("成績データの取得でエラー", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
    }

    /**
     * 教科。
     */
    private static class Class {
        private final String _code;
        private final String _name;
        private final String _abbv;

        public Class(final String code, final String name, final String abbv) {
            _code = code;
            _name = name;
            _abbv = abbv;
        }

        public String toString() {
            return _code + ":" + _abbv;
        }
    }

    /**
     * 科目。
     */
    private static class Subclass implements Comparable {
        private final String _code;
        private final String _name;
        private final String _abbv;

        /** 合併情報を持っているか */
        private boolean _hasCombined;
        /** 合併先か? */
        private boolean _isSaki;
        /** 合併元か? */
        private boolean _isMoto;

        public Subclass(final String code, final String name, final String abbv) {
            _code = code;
            _name = name;
            _abbv = abbv;
        }

        public Subclass(final String code) {
            this(code, "", "");
        }

        public String getClassKey(final Param param) {
            final String classCd;
            if ("1".equals(param._useCurriculumcd)) {
                final String[] split = StringUtils.split(_code, "-");
                classCd =  split[0] + "-" + split[1];
            } else {
                classCd = _code.substring(0, 2);
            }
            return classCd;
        }

        public String getClassCd(final Param param) {
            final String classCd;
            if ("1".equals(param._useCurriculumcd)) {
                final String[] split = StringUtils.split(_code, "-");
                classCd =  split[0];
            } else {
                classCd = _code.substring(0, 2);
            }
            return classCd;
        }

        public void setSaki() {
            _hasCombined = true;
            _isSaki = true;
        }

        public void setMoto() {
            _hasCombined = true;
            _isMoto = true;
        }

        public String toString() {
            return _code + ":" + _abbv;
        }

        public int compareTo(final Object o) {
            if (!(o instanceof Subclass)) {
                return -1;
            }
            final Subclass that = (Subclass) o;
            return this._code.compareTo(that._code);
        }
    }

    private static class SubclassComparator implements Comparator {
        final Student _student;
        final Exam _exam;
        public SubclassComparator(final Student student, final Exam exam) {
            _student = student;
            _exam = exam;
        }
        public int compare(final Object o1, final Object o2) {
            final Subclass sub1 = (Subclass) o1;
            final Subclass sub2 = (Subclass) o2;

            String isGroup1 = (String) _student._subclassGroup.get(sub1._code);
            String isGroup2 = (String) _student._subclassGroup.get(sub2._code);
            if (isGroup1 != null && isGroup2 == null) {
                return -1;
            } else if (isGroup1 == null && isGroup2 != null) {
                return 1;
            }
            return sub1._code.compareTo(sub2._code);
        }
    }

    private static class Record {
        final String _classcd;
        final String _classkey;
        final String _subclasscd;
        final Integer _score;
        final String _scoreDi;
        final Integer _scorePercent;
        final BigDecimal _avg;
        final Integer _rank;
        final BigDecimal _deviation;

        private Record(
                final String classcd,
                final String classkey,
                final String subclasscd,
                final Integer score,
                final String scoreDi,
                final Integer scorePercent,
                final BigDecimal avg,
                final Integer rank,
                final BigDecimal deviation
        ) {
            _classcd = classcd;
            _classkey = classkey;
            _subclasscd = subclasscd;
            _score = score;
            _scoreDi = scoreDi;
            _scorePercent = scorePercent;
            _avg = avg;
            _rank = rank;
            _deviation = deviation;
        }

        public String toString() {
            return _subclasscd + "/" + _score + "/" + _rank + "/" + _deviation;
        }
    }

    private static class AverageDat {
        final String _subclassCd;
        final BigDecimal _avg;
        final BigDecimal _avgPercent;
        final BigDecimal _stdDev;
        final BigDecimal _avgAvg;
        final Integer _highScore;
        final Integer _count;
        final String _coursecd;
        final String _majorcd;
        final String _coursecode;

        private AverageDat(
                final String subclassCd,
                final BigDecimal avg,
                final BigDecimal avgPercent,
                final BigDecimal stdDev,
                final BigDecimal avgAvg,
                final Integer highScore,
                final Integer count,
                final String coursecd,
                final String majorcd,
                final String coursecode
        ) {
            _subclassCd = subclassCd;
            _avg = avg;
            _avgPercent = avgPercent;
            _stdDev = stdDev;
            _avgAvg = avgAvg;
            _highScore = highScore;
            _count = count;
            _coursecd = coursecd;
            _majorcd = majorcd;
            _coursecode = coursecode;
        }

        private static String averageKey(final Param param, final String subclasscd, final String coursecd, final String majorcd, final String coursecode) {
            final String key;
            if ("1".equals(param._groupDiv)) {
                key = subclasscd;
            } else if ("3".equals(param._groupDiv)) {
                key = subclasscd + "0" + majorcd + "0000";
            } else {
                key = subclasscd + coursecd + majorcd + coursecode;
            }
            return key;
        }

        private static String averageKey(final Param param, final Student student, final String subclasscd) {
            final String key;
            if ("1".equals(param._groupDiv)) {
                key = subclasscd;
            } else if ("3".equals(param._groupDiv)) {
                key = subclasscd + student.courseGroupKey();
            } else {
                key = subclasscd + student.courseKey();
            }
            return key;
        }
    }

//    private static class ScoreDistribution {
//
//        public final static String[] _scoreKeys = new String[]{"0", "10", "20", "30", "40", "50", "60", "70", "80", "90", "100"};
//        public final Set _keySubClasses = new HashSet();
//        private final String _key;
//        private final Map _distributions = new HashMap();
//
//        private ScoreDistribution(final String key) {
//            _key = key;
//        }
//
//        private Map getSubclassDistributionMap(final String subClassCd) {
//            return getMappedMap(_distributions, subClassCd);
//        }
//
//        public void add(final Subclass subClass, final Integer score) {
//            int scoreKeyInd = (score.intValue() / 10);
//            if (scoreKeyInd <= _scoreKeys.length) {
//                _keySubClasses.add(subClass);
//                increment(subClass, _scoreKeys[scoreKeyInd]);
//            }
//        }
//
//        private void increment(final Subclass subClass, final String scoreKey) {
//            Integer count = getCount(subClass._code, scoreKey);
//            getSubclassDistributionMap(subClass._code).put(scoreKey, new Integer(count.intValue() + 1));
//        }
//
//        public Integer getCount(final String subClassCd, final String scoreKey) {
//            Map subclassScoreDist = getSubclassDistributionMap(subClassCd);
//            final Integer count;
//            if (subclassScoreDist.containsKey(scoreKey)) {
//                count = (Integer) subclassScoreDist.get(scoreKey);
//            } else {
//                count = Integer.valueOf("0");
//            }
//            return count;
//        }
//
//        private String distStr() {
//            StringBuffer stb = new StringBuffer();
//            String comma = "";
//            for (Iterator it = _keySubClasses.iterator(); it.hasNext();) {
//                Subclass subClass = (Subclass) it.next();
//                stb.append("[subClass=").append(subClass.toString());
//                for (int i = 0; i < _scoreKeys.length; i++) {
//                    String scoreKey = _scoreKeys[i];
//                    Integer count = getCount(subClass._code, scoreKey);
//                    stb.append(comma).append("\"").append(scoreKey).append("\"=").append(count);
//                    comma = ", ";
//                }
//                stb.append("] ");
//            }
//            return stb.toString();
//        }
//
//        public int getFieldIndex(final String scoreKey) {
//            int ind = -1;
//            for (int i = 0; i < _scoreKeys.length; i++) {
//                if (_scoreKeys[i].equals(scoreKey)) {
//                    ind = i;
//                    break;
//                }
//            }
//            if (ind == -1) {
//                return -1;
//            }
//            return 11 - ind;
//        }
//
//        public String toString() {
//            return " dist = (" + distStr() + ")";
//        }
//    }

//    /** 「100点に換算する」場合に表示するデータ */
//    private static class ConvertedScore {
//    }

private static class Chart {

        private static File graphImageFile(final JFreeChart chart, final int dotWidth, final int dotHeight) {
            final String tmpFileName = KNJServletUtils.createTmpFile(".png");
            //log.debug("\ttmp file name=" + tmpFileName);

            final File outputFile = new File(tmpFileName);
            try {
                ChartUtilities.saveChartAsPNG(outputFile, chart, dot2pixel(dotWidth), dot2pixel(dotHeight));
            } catch (final IOException ioEx) {
                log.error("グラフイメージをファイル化できません。", ioEx);
            }

            return outputFile;
        }

        private static int dot2pixel(final int dot) {
            final int pixel = dot / 4;   // おおよそ4分の1程度でしょう。

            /*
             * 少し大きめにする事でSVFに貼り付ける際の拡大を防ぐ。
             * 拡大すると粗くなってしまうから。
             */
            return (int) (pixel * 1.3);
        }

        private static JFreeChart createRaderChart(final DefaultCategoryDataset dataset) {
            final SpiderWebPlot plot = new SpiderWebPlot(dataset);
            plot.setWebFilled(false);
            plot.setMaxValue(80.0);

            // 実データ
            plot.setSeriesOutlineStroke(0, new BasicStroke(1.0f));
            plot.setSeriesOutlinePaint(0, Color.black);
            plot.setSeriesPaint(0, Color.black);

            // 偏差値50
            final BasicStroke stroke = new BasicStroke(1.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f, new float[] {4.0f, 4.0f}, 0.0f);
            plot.setSeriesOutlineStroke(1, stroke);
            plot.setSeriesOutlinePaint(1, Color.darkGray);
            plot.setSeriesPaint(1, Color.darkGray);

            plot.setOutlinePaint(Color.white);

            final JFreeChart chart = new JFreeChart(null, new Font("TimesRoman", Font.PLAIN, 15), plot, false);
            chart.setBackgroundPaint(Color.white);

            return chart;
        }

        private static JFreeChart createBarChart(
                final DefaultCategoryDataset scoreDataset,
                final DefaultCategoryDataset avgDataset
        ) {
            final JFreeChart chart = ChartFactory.createBarChart(null, null, null, scoreDataset, PlotOrientation.VERTICAL, false, false, false);
            final CategoryPlot plot = chart.getCategoryPlot();
            plot.setRangeGridlineStroke(new BasicStroke(2.0f)); // 目盛りの太さ
            plot.getDomainAxis().setTickLabelsVisible(true);
            plot.setRangeGridlinePaint(Color.black);
            plot.setRangeGridlineStroke(new BasicStroke(1.0f));

            // 追加する折れ線グラフの表示設定
            final LineAndShapeRenderer renderer2 = new LineAndShapeRenderer();
            renderer2.setItemLabelsVisible(true);
            renderer2.setPaint(Color.gray);
            plot.setDataset(1, avgDataset);
            plot.setRenderer(1, renderer2);
            plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);

            final NumberAxis numberAxis = new NumberAxis();
            numberAxis.setTickUnit(new NumberTickUnit(10));
            numberAxis.setTickLabelsVisible(true);
            numberAxis.setRange(0, 100.0);
            plot.setRangeAxis(numberAxis);

            final CategoryItemRenderer renderer = plot.getRenderer();
            renderer.setSeriesPaint(0, Color.darkGray);
            final Font itemLabelFont = new Font("TimesRoman", Font.PLAIN, 30);
            renderer.setItemLabelFont(itemLabelFont);
            renderer.setItemLabelsVisible(true);

            ((BarRenderer) renderer).setMaximumBarWidth(0.05);

            chart.setBackgroundPaint(Color.white);

            return chart;
        }
    }

    private static class Param {
        final String _year;
        final String _semester;
        final String _testCd;
        final String _grade;
        final String _printHogoshaKakuninran;
        final String _groupDiv; // 1:学年 3:コース

        final String _categoryIsClass; // [クラス指定 or 生徒指定]の値。
        final String[] _categorySelected; // クラス or 生徒。

        final String _loginDate;

        private String _schoolName; // 学校名
        private String _remark2;    // 担任職種名
        final String _deviationPrint; // 偏差値を印字するか?
        final String _outputKijun; // 基準点
        final String _cmd;
        final String _sdate;
        final String _edate;
        final String _juniPrint;
        final String _jisshiDate;
        final String _gradeCd;
        final String _gradeName1;

        final Map _staffs = new HashMap();

        /** 教科マスタ。 */
        final Map _classes;
        final String _imagePath;

        final String _useCurriculumcd;
        final String _useClassDetailDat;

        final List _classOrder;

        /** 中学か? false なら高校. */
        final boolean _isJunior;

//        /** 欠点 */
//        final int _borderScore;
        /** 科目マスタ。 */
        final Map _subclasses;

//        /** 100点満点に換算する */
//        private final boolean _isConvertScoreTo100;

        final Exam _thisExam;

        /** グラフイメージファイルの Set&lt;File&gt; */
        final Set _graphFiles = new HashSet();

        final String _barLegendImage;
        final String _radarLegendImage;

        final String _major;
        final String _use_school_detail_gcm_dat;
        final String _useSchool_KindField;
        final String SCHOOLKIND;
        final String SCHOOLCD;
        final String _use_prg_schoolkind;
        final String _selectSchoolKind;

        final Map _attendParamMap;

        public Param(final HttpServletRequest request, final DB2UDB db2) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _testCd = request.getParameter("TESTCD");
            _categoryIsClass = request.getParameter("CATEGORY_IS_CLASS");
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _loginDate = request.getParameter("LOGIN_DATE");
            _grade = request.getParameter("GRADE");
            _printHogoshaKakuninran = request.getParameter("PRINT_HOGOSHA_KAKUNINRAN");
            _groupDiv =  "2"; // request.getParameter("GROUP_DIV");// 1=学年, 2=コース
            _deviationPrint = request.getParameter("DEVIATION_PRINT");
            _cmd = request.getParameter("cmd");
            _sdate = null == request.getParameter("SDATE") ? request.getParameter("SDATE") : StringUtils.replace(request.getParameter("SDATE"), "/", "-");
            _edate = null == request.getParameter("EDATE") ? request.getParameter("EDATE") : StringUtils.replace(request.getParameter("EDATE"), "/", "-");

            _major = request.getParameter("MAJOR");
            _use_school_detail_gcm_dat = request.getParameter("use_school_detail_gcm_dat");
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            SCHOOLCD = request.getParameter("SCHOOLCD");
            _use_prg_schoolkind = request.getParameter("use_prg_schoolkind");
            _selectSchoolKind = request.getParameter("selectSchoolKind");

            _imagePath = request.getParameter("IMAGE_PATH");
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _useClassDetailDat = request.getParameter("useClassDetailDat");
            _outputKijun = request.getParameter("OUTPUT_KIJUN");
            log.debug(" record_rank_sdiv_dat outputKijun ? = " + _outputKijun);
            _juniPrint = request.getParameter("JUNI_PRINT");
            _jisshiDate = request.getParameter("JISSHI_DATE");

//            _borderScore = StringUtils.isBlank(request.getParameter("KETTEN")) ? 0 : Integer.parseInt(request.getParameter("KETTEN"));
//            _isPrintDistribution = "2".equals(request.getParameter("USE_GRAPH"));
//            _isPrintGuardianComment = "1".equals(request.getParameter("USE_HOGOSYA"));
//            _isConvertScoreTo100 = "1".equals(request.getParameter("KANSAN"));

            _thisExam = new Exam(_year, _semester, _testCd);
            _thisExam._grade = _grade;

            _isJunior = "J".equals(getSchregRegdGdat(db2, "SCHOOL_KIND"));
            SCHOOLKIND = StringUtils.defaultString(getSchregRegdGdat(db2, "SCHOOL_KIND"));
            _gradeCd = StringUtils.defaultString(getSchregRegdGdat(db2, "GRADE_CD"));
            _gradeName1 = StringUtils.defaultString(getSchregRegdGdat(db2, "GRADE_NAME1"));
            loadCertifSchool(db2);
            loadRegdHdat(db2);
            _thisExam.load(this, db2);
            log.info(" this Exam testcd = " + _thisExam.getExamKey());
            _classes = setClasses(db2);
            _subclasses = setSubClasses(db2);
            setCombinedOnSubClass(db2);

            _classOrder = loadClassOrder(db2);
            log.info(" class order = " + _classOrder);

            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
            _attendParamMap.put("grade", _grade);
            _attendParamMap.put("schregno", "?");
            _attendParamMap.put("sdate", _sdate);
            _attendParamMap.put("date", _edate);
            if ("1".equals(_use_school_detail_gcm_dat)) {
                _attendParamMap.put("useTestCountflg", "TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV");
            } else {
                _attendParamMap.put("useTestCountflg", "TESTITEM_MST_COUNTFLG_NEW_SDIV");
            }

            String barLegendImage;
            if ("1".equals(_groupDiv)) {
                /** 棒グラフ凡例画像1(学年). */
                barLegendImage = "BarChartLegendGrade.png";
            } else if ("3".equals(_groupDiv)) {
                /** 棒グラフ凡例画像3(コースグループ). */
                barLegendImage = "BarChartLegendCourseGroup.png";
            } else {
                /** 棒グラフ凡例画像2(コース). */
                barLegendImage = "BarChartLegendCourse.png";
            }
            _barLegendImage = checkImageFile(barLegendImage);
            /** レーダーチャート凡例画像. */
            _radarLegendImage = checkImageFile("RaderChartLegend.png");
        }

        public String checkImageFile(String image) {
            image = _imagePath + "/" + image;
            final boolean exists = new File(image).exists();
            log.fatal("画像ファイル exists?" + exists + " " + image);
            if (!exists) {
                image = null;
            }
            return image;
        }

        private String getItemMsg() {
            final String msg;
            if ("1".equals(_groupDiv)) {
                msg = "学年";
            } else if ("3".equals(_groupDiv)) {
                msg = "グループ";
            } else {
                msg = "コース";
            }
            return msg;
        }

        private void setCombinedOnSubClass(final DB2UDB db2) throws SQLException {
            final Set sakiSet = new HashSet();
            final Set motoSet = new HashSet();

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                String sql;
                sql = "select distinct";
                if ("1".equals(_useCurriculumcd)) {
                    sql +="  COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || ";
                }
                sql +="  COMBINED_SUBCLASSCD AS COMBINED_SUBCLASSCD,";
                if ("1".equals(_useCurriculumcd)) {
                    sql +="  ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ";
                }
                sql +="  ATTEND_SUBCLASSCD AS ATTEND_SUBCLASSCD"
                    + " from SUBCLASS_REPLACE_COMBINED_DAT"
                    + " where"
                    + "  YEAR = '" + _year + "'"
                    ;

                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String combined = rs.getString("COMBINED_SUBCLASSCD");
                    final String attend = rs.getString("ATTEND_SUBCLASSCD");
                    sakiSet.add(combined);
                    motoSet.add(attend);
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }

            // 合併先
            for (final Iterator it = sakiSet.iterator(); it.hasNext();) {
                final String saki = (String) it.next();
                final Subclass subClass = (Subclass) _subclasses.get(saki);
                if (null != subClass) {
                    subClass.setSaki();
                }
            }
            // 合併元
            for (final Iterator it = motoSet.iterator(); it.hasNext();) {
                final String moto = (String) it.next();
                final Subclass subClass = (Subclass) _subclasses.get(moto);
                if (null != subClass) {
                    subClass.setMoto();
                }
            }
        }

        private Map setSubClasses(final DB2UDB db2) throws SQLException {
            final Map rtn = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                String sql = "select";
                if  ("1".equals(_useCurriculumcd)) {
                    sql +="   CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS SUBCLASSCD,";
                } else {
                    sql +="   SUBCLASSCD,";
                }
                sql +="   coalesce(SUBCLASSORDERNAME2, SUBCLASSNAME) as NAME,"
                    + "   SUBCLASSABBV"
                    + " from V_SUBCLASS_MST"
                    + " where"
                    + "   YEAR = '" + _year + "'";

                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String code = rs.getString("SUBCLASSCD");
                    rtn.put(code, new Subclass(code, rs.getString("NAME"), rs.getString("SUBCLASSABBV")));
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            //log.debug("科目マスタ総数=" + rtn.size());
            return rtn;
        }

        private Map setClasses(final DB2UDB db2) throws SQLException {
            final Map rtn = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                String sql = "";
                sql += "select";
                if ("1".equals(_useCurriculumcd)) {
                    sql +="   CLASSCD || '-' || SCHOOL_KIND AS CLASSCD, ";
                } else {
                    sql +="   CLASSCD,";
                }
                sql +="   CLASSNAME,"
                    + "   CLASSABBV"
                    + " from V_CLASS_MST"
                    + " where"
                    + "   YEAR = '" + _year + "'";

                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String code = rs.getString("CLASSCD");
                    rtn.put(code, new Class(code, rs.getString("CLASSNAME"), rs.getString("CLASSABBV")));
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            //log.debug("教科マスタ=" + rtn);
            return rtn;
        }

        private void loadRegdHdat(final DB2UDB db2) {
            final String sql;
            sql = "SELECT t1.grade ||  t1.hr_class AS code, t2.staffname"
                + " FROM schreg_regd_hdat t1 INNER JOIN v_staff_mst t2 ON t1.year = t2.year AND t1.tr_cd1 = t2.staffcd"
                + " WHERE t1.year = '" + _year + "'"
                + " AND t1.semester = '" + _semester + "'";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    _staffs.put(rs.getString("code"), rs.getString("staffname"));
                }
            } catch (final SQLException e) {
                log.error("担任名の取得でエラー", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private void loadCertifSchool(final DB2UDB db2) throws SQLException {
            final String key = _isJunior ? "110" : "109";

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = "SELECT school_name, remark2 FROM certif_school_dat WHERE year = '" + _year + "' AND certif_kindcd = '" + key + "'";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    _schoolName = rs.getString("school_name");
                    _remark2 = rs.getString("remark2");
                }
            } catch (final SQLException e) {
                log.error("学校名取得エラー。");
                throw e;
            }
        }

        private List loadClassOrder(final DB2UDB db2) {
            final String sql;
            if ("1".equals(_useCurriculumcd) && "1".equals(_useClassDetailDat)) {
                sql = "SELECT classcd || '-' || school_kind AS classcd FROM class_detail_dat"
                    + " WHERE year = '" + _year + "' AND class_seq = '004' "
                    + " ORDER BY int(value(class_remark1, '99')), classcd || '-' || school_kind "
                    ;
            } else {
                final String field1 = _isJunior ? "name1" : "name2";
                final String field2 = _isJunior ? "namespare1" : "namespare2";
                sql = "SELECT " + field1 + " AS classcd FROM v_name_mst"
                    + " WHERE year = '" + _year + "' AND namecd1 = 'D009' AND " + field1 + " IS NOT NULL "
                    + " ORDER BY " + field2 + ", " + field1;
                    ;
            }

            final List classOrder = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    classOrder.add(rs.getString("classcd"));
                }
            } catch (final SQLException e) {
                log.error("教科表示順取得エラー。", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return classOrder;
            //log.debug("教科表示順 = " + _classOrder);
        }

//        private boolean isUnderScore(final String score) {
//            return NumberUtils.isDigits(score) && Integer.parseInt(score) < _borderScore;
//        }

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

        public void delegeTmpImageFile() {
            int count = 0;
            for (final Iterator it = _graphFiles.iterator(); it.hasNext();) {
                final File imageFile = (File) it.next();
                if (null == imageFile) {
                    continue;
                }
                boolean deleted = imageFile.delete();
                //log.fatal("グラフ画像ファイル削除:" + imageFile.getName() + " : " + imageFile.delete());
                if (deleted) {
                    count += 1;
                }
            }
            log.fatal("グラフ画像ファイル削除:" + count + "件");
        }
    }

} // KNJD105Q

// eof
