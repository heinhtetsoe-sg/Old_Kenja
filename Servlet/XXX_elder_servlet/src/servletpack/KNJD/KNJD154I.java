// kanji=漢字
/*
 * $Id: 5a5362aea5a438f73d2e2b6408c29973ad8cc24a $
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
import java.util.Collection;
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
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
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
import servletpack.KNJZ.detail.CsvUtils;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.SvfField;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

/**
 * 個人成績票。
 * @author takaesu
 * @version $Id: 5a5362aea5a438f73d2e2b6408c29973ad8cc24a $
 */
public class KNJD154I {
    /*pkg*/static final Log log = LogFactory.getLog(KNJD154I.class);

    /** 3教科科目コード。 */
    private static final String ALL3 = "333333";
    /** 5教科科目コード。 */
    private static final String ALL5 = "555555";
    /** 9教科科目コード。 */
    private static final String ALL9 = "999999";

    /** 合併元科目を除く */
    private static final String MOTO_FLG = "1";
    /** 合併先科目を除く */
    private static final String SAKI_FLG = "2";

    private static final String SEMEALL = "9";

    private static final String HYOTEI_TESTCD = "9990009";

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

        log.fatal("$Id: 5a5362aea5a438f73d2e2b6408c29973ad8cc24a $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        Param param = new Param(request, db2);

        if ("csv".equals(param._cmd)) {
            try {
                CsvUtils.outputLines(log, response, param._thisExam._title + ".csv", getCsvLines(param, response, db2));
            } catch (Exception e) {
                log.fatal("exception!", e);
            } finally {
                if (null != db2) {
                    db2.commit();
                    db2.close();
                }
            }

        } else {
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
                    int count = 0;
                    for (final Iterator it = param._graphFiles.iterator(); it.hasNext();) {
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
        }
    }

    private static <A, B, C> Map<B, C> getMappedMap(final Map<A, Map<B, C>> map, final A key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new TreeMap());
        }
        return map.get(key1);
    }

    private static <A, B> List<B> getMappedList(final Map<A, List<B>> map, final A key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new ArrayList());
        }
        return map.get(key1);
    }

    private static String tostr(final Object o) {
        return null == o ? null : o.toString();
    }

    private static String sishagonyu(final BigDecimal bd, final int scale) {
        return null == bd ? null : bd.setScale(scale, BigDecimal.ROUND_HALF_UP).toString();
    }

    private static String average(final Collection<String> scoreList, final int scale) {
        BigDecimal sum = null;
        int count = 0;
        for (final String score : scoreList) {
            if (NumberUtils.isNumber(score)) {
                if (null == sum) {
                    sum = new BigDecimal(0);
                }
                sum = sum.add(new BigDecimal(score));
                count += 1;
            }
        }
        final String avg = count == 0 || null == sum ? null : sum.divide(new BigDecimal(count), scale, BigDecimal.ROUND_HALF_UP).toString();
        log.info(" scoreList = " + scoreList + ", sum = " + sum + ", count = " + count + ", avg = " + avg);
        return avg;
    }

    private List newLine(final List lines) {
        final List newLine = new ArrayList();
        lines.add(newLine);
        return newLine;
    }

    private List getCsvLines(final Param param, final HttpServletResponse response, final DB2UDB db2) throws SQLException {
        // 対象の生徒たちを得る
        final List<Student> students = Student.createStudents(db2, param);
        _hasData = students.size() > 0;

        // 成績のデータを読む
        Exam.loadScore(db2, students, param._thisExam, param);
//      if (param._beforeExam.isValid()) {
//      Exam.loadExam(db2, students, param._beforeExam, param);
//      }

        final List lines = new ArrayList();

        // 印刷
        for (final Iterator it = students.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            log.debug(" student " + student._schregno + " , subclass count = " + getMappedMap(student._examSubclasses, param._thisExam.getExamKey()).size() + ", course key = " + student.courseKey());
            log.debug("record this: " + getMappedMap(student._examRecord, param._thisExam.getExamKey()).values());


            final List line = newLine(lines);
            line.add(StringUtils.defaultString(param._thisExam._title) + "　" + StringUtils.defaultString(student._hrName) + convNum(Integer.parseInt(student._attendNo)) + "番　" + StringUtils.defaultString(student._name));

            outputTabCsv(lines, param, student, param._thisExam);
            newLine(lines);
        }
        return lines;
    }

    private void outputTabCsv(final List lines, final Param param, final Student student, final Exam exam) {

        final int maxIdx = "1".equals(param._deviationPrint) || "2".equals(param._deviationPrint) ? 7 : 6;
        final List[] lineScore = new List[maxIdx + 1];
        for (int i = 0; i < lineScore.length; i++) {
            lineScore[i] = newLine(lines);
            lineScore[i].add("");
        }

        final int IDX_CLASS = 0;
        final int IDX_SUBCLASS = 1;
        final int IDX_SCORE = 2;
        final int IDX_AVG = 3;
        final int IDX_RANK = 4;
        final int IDX_COUNT = 5;
        final int IDX_MAX = 6;
        final int IDX_HYOJUN = 7;

        final String msg = param.getItemMsg();
        lineScore[IDX_CLASS].add("教科名");
        lineScore[IDX_SUBCLASS].add("科目名");
        lineScore[IDX_SCORE].add("得点");
        lineScore[IDX_AVG].add(msg + "平均");
        lineScore[IDX_RANK].add(msg + "順位");
        lineScore[IDX_COUNT].add("受験者数");
        lineScore[IDX_MAX].add("最高得点");
        if ("1".equals(param._deviationPrint)) {
            lineScore[IDX_HYOJUN].add("偏差値");
        } else if ("2".equals(param._deviationPrint)) {
            lineScore[IDX_HYOJUN].add("標準偏差");
        }

        int i = 0;
        String oldclassname = null;
        final List subclassList = new ArrayList(getMappedMap(student._examSubclasses, exam.getExamKey()).values());
        Collections.sort(subclassList);
        final List<String> deviationList = new ArrayList<String>();
        for (final Iterator it = subclassList.iterator(); it.hasNext();) {
            final Subclass subclass = (Subclass) it.next();

            if ("90".equals(subclass.getClassCd(param))) {
                continue;
            }

            //合併元科目を除く
            if (MOTO_FLG.equals(param._nameMstD070nameSpare1.get(exam._semester + exam._testCd))) {
                if (exam.getAttendSubclassCdList(student).contains(subclass._code)) {
                    continue;
                }

            //合併先科目を除く
            } else if (SAKI_FLG.equals(param._nameMstD070nameSpare1.get(exam._semester + exam._testCd))) {
                if (exam.getCombinedSubclassCdList(student).contains(subclass._code)) {
                    continue;
                }

            }

            final Record record = getMappedMap(student._examRecord, exam.getExamKey()).get(subclass._code);
            final AverageDat avgDat = (AverageDat) exam._averages.get(AverageDat.averageKey(param, student, subclass._code));
            final Class clazz = (Class) param._classes.get(subclass.getClassKey(param));
            if (null == clazz) {
                continue;
            }

            lineScore[IDX_CLASS].add(null != oldclassname && oldclassname.equals(clazz._name) ? "" : clazz._name);
            lineScore[IDX_SUBCLASS].add(subclass._name);
            i++;

            if (null == record) {
                for (int j = 2; j < lineScore.length; j++) {
                    lineScore[j].add("");
                }
                continue;
            }

            lineScore[IDX_SCORE].add(tostr(record._score));
            lineScore[IDX_RANK].add(tostr(record._courseRank));
            if ("1".equals(param._deviationPrint)) {
                lineScore[IDX_HYOJUN].add(sishagonyu(record._deviation, 1));
                deviationList.add(sishagonyu(record._deviation, 1));
            }

            if (null == avgDat) {
                lineScore[IDX_AVG].add(null);
                lineScore[IDX_COUNT].add(null);
                lineScore[IDX_MAX].add(null);
                if ("2".equals(param._deviationPrint)) {
                    lineScore[IDX_HYOJUN].add(null);
                }
            } else {
                lineScore[IDX_AVG].add(sishagonyu(avgDat._avg, 1));
                lineScore[IDX_COUNT].add(tostr(avgDat._count));
                lineScore[IDX_MAX].add(tostr(avgDat._highScore));
                if ("2".equals(param._deviationPrint)) {
                    lineScore[IDX_HYOJUN].add(sishagonyu(avgDat._stdDev, 1));
                }
            }
            oldclassname = clazz._name;
        }

        if (i > 0) {
            lineScore[IDX_CLASS].add("全教科");
            lineScore[IDX_SUBCLASS].add("全教科");

            final Record rec9 = getMappedMap(student._examRecordOther, exam.getExamKey()).get(ALL9);
            if (null == rec9) {
                lineScore[IDX_SCORE].add("");
                lineScore[IDX_RANK].add("");
            } else {
                lineScore[IDX_SCORE].add(tostr(rec9._score));
                lineScore[IDX_RANK].add(tostr(rec9._courseRank));
            }
            if ("1".equals(param._deviationPrint)) {
                lineScore[IDX_HYOJUN].add(average(deviationList, 1));
            }
            final AverageDat avg9 = (AverageDat) exam._averagesOther.get(AverageDat.averageKey(param, student, ALL9));
            if (null == avg9) {
                lineScore[IDX_AVG].add(null);
                lineScore[IDX_COUNT].add(null);
                lineScore[IDX_MAX].add(null);
                if ("2".equals(param._deviationPrint)) {
                    lineScore[IDX_HYOJUN].add(null);
                }
            } else {
                lineScore[IDX_AVG].add(sishagonyu(avg9._avg, 1));
                lineScore[IDX_COUNT].add(tostr(avg9._count));
                lineScore[IDX_MAX].add(tostr(avg9._highScore));
                if ("2".equals(param._deviationPrint)) {
                    lineScore[IDX_HYOJUN].add(sishagonyu(avg9._stdDev, 1));
                }
            }
        }
        //log.debug("  exam = " + exam.getExamKey() + ", subclasses = " + subclasses.size());
    }

    private void printMain(final Param param, final Vrw32alp svf, final DB2UDB db2) throws SQLException {
        // 対象の生徒たちを得る
        final List students = Student.createStudents(db2, param);
        _hasData = students.size() > 0;

        SubclassAttendance.load(db2, param, students);

        // 成績のデータを読む
        Exam.loadScore(db2, students, param._thisExam, param);

        //これまでの成績のデータを読む
        for (final String key : param._beforeExamMap.keySet()) {
            final Exam beforeExam = param._beforeExamMap.get(key);
            Exam.loadScore(db2, students, beforeExam, param);
        }

        final String form2 = "KNJD154I.frm";
        log.info(" form = " + form2);

        // 印刷
        for (final Iterator it = students.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            printStudent(db2, param, svf, form2, student);
        }
    }

    private void printStudent(final DB2UDB db2, final Param param, final Vrw32alp svf, final String form2, final Student student) {
        if (param._isOutptuDebug) {
            log.info(" student " + student._schregno + " , subclass count = " + getMappedMap(student._examSubclasses, param._thisExam.getExamKey()).size() + ", course key = " + student.courseKey());
            log.info("record this: " + getMappedMap(student._examRecord, param._thisExam.getExamKey()).values());
        } else {
            log.debug(" student " + student._schregno + " , subclass count = " + getMappedMap(student._examSubclasses, param._thisExam.getExamKey()).size() + ", course key = " + student.courseKey());
        }

        svf.VrSetForm(form2, 4);
        param._currentform = form2;
        if (!param._formnameFormFieldMap.containsKey(form2)) {
            param._formnameFormFieldMap.put(form2, SvfField.getSvfFormFieldInfoMapGroupByName(svf));
        }

        printStatic(svf, param, student);
        // グラフ印字
        printBarGraph(svf, param, student, param._thisExam);
//        printRadarGraph(svf, param, student, param._thisExam);
        //log.debug(" key = " + student._examRecord.keySet());

        //これまでの成績を取得
        int idx = 1;

        //現在成績とこれまでの成績の合計欄を出力
        printTotal(svf, param, student, param._thisExam, 0); //合計 現在成績
        for (final String key : param._beforeExamMap.keySet()) {
            final Exam beforeExam = param._beforeExamMap.get(key);
            svf.VrsOut("LAST_EXAM_TITLE", "これまでの成績");
            final String field  = "TEST_ITEM_NAME" + idx;
            final String[][] itemNameField = new String[][] {{field}};
            svfVrsOutWithCheckMojisu(svf, param, itemNameField, true, beforeExam._examName);
            svf.VrsOut("ITEM_NAME" + idx + "_1", "得点");
            svf.VrsOut("ITEM_NAME" + idx + "_2", "クラス順位");
            svf.VrsOut("ITEM_NAME" + idx + "_3", "コース順位");
            printTotal(svf, param, student, beforeExam, idx); //合計 これまでの成績
            idx++ ;
        }

        // 成績
        printTab(db2, svf, param, student, param._thisExam);

    }

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
        svf.VrsOut("STAFFNAME", StringUtils.defaultString(param._remark2) + StringUtils.defaultString((String) param._staffs.get(student._grade + student._hrClass)));
        svf.VrsOut("NENDO", param._nendo + " 試験成績個人票");
        svf.VrsOut("SEMESTER", param._semesterName);
        // 学科名
        String majorName = "";
        if("H".equals(param._schoolKind)) {
            majorName = param._schoolKindName + " " + student._majorName + " " + student._courseName;
        } else {
            majorName = param._schoolKindName;
        }
        svf.VrsOut("MAJOR_NAME", majorName);

        svf.VrsOut("HR_NAME", student._hrName + convNum(Integer.parseInt(student._attendNo)) + "番");

//        svfVrsOutWithCheckMojisu(svf, new String[][] {{"NAME"}, {"NAME_2"}}, false, student._name);
        svf.VrsOut("NAME", student._name);

        // 個人評(期末の場合は注意事項)
        KNJServletUtils.printDetail(svf, "PERSONAL_REMARK", student._remark, 45 * 2, 3);

        svf.VrsOut("ITEM_TOTAL5", "全教科");

//        final String msg = param.getItemMsg();
//        svf.VrsOut("ITEM_AVG", msg + "平均");
//        svf.VrsOut("ITEM_RANK", msg + "順位");
        svf.VrsOut("ITEM_AVG", "平均点");
        svf.VrsOut("ITEM_RANK1", "クラス順位");
        svf.VrsOut("ITEM_RANK2", "コース順位");
        if ("1".equals(param._deviationPrint)) {
            svf.VrsOut("ITEM_DEVIATION", "偏差値");
        } else if ("2".equals(param._deviationPrint)) {
            svf.VrsOut("ITEM_DEVIATION", "標準偏差");
        }

        // 画像
//        if (null != param._radarLegendImage) {
//            svf.VrsOut("RADER_LEGEND", param._radarLegendImage);
//        }
//        if (null != param._barLegendImage) {
//            svf.VrsOut("BAR_LEGEND", param._barLegendImage);
//        }

        //棒グラフの説明
        svf.VrAttribute("BAR_COLOR1", "PAINT=(0,10,2)"); // PAINT=(カラーパレットの番号,明度0～100,2)
        svf.VrAttribute("BAR_COLOR2", "PAINT=(0,60,2)"); // PAINT=(カラーパレットの番号,明度0～100,2)
        svf.VrsOut("BAR_NAME1", "得点");
        svf.VrsOut("BAR_NAME2", "平均");
        svf.VrsOut("BAR_NAME3", "偏差値");

        svf.VrsOut("BAR_TITLE", "得点グラフ");
//        svf.VrsOut("RADER_TITLE", "教科間バランス");
    }

    private void svfVrsOutWithCheckMojisu(final Vrw32alp svf, final Param param, final String[][] fields, final boolean tategaki, final String data) {
        svfVrsOutWithCheckMojisu(svf, param, getUseField(svf, param, tategaki, fields, data), tategaki, data);
    }

    private void printBarGraph(final Vrw32alp svf, final Param param, final Student student, final Exam exam) {

        /** 棒グラフの最大科目数. */
        final int BAR_GRAPH_MAX_ITEM = 15;
        // グラフ用のデータ作成
        final DefaultCategoryDataset scoreDataset = new DefaultCategoryDataset();
//        final DefaultCategoryDataset avgDataset = new DefaultCategoryDataset();
        final DefaultCategoryDataset deviationDataset = new DefaultCategoryDataset();
        int i = 0;
        final List recordList = new ArrayList<Record>(getMappedMap(student._examRecord, exam.getExamKey()).values());
        Collections.sort(recordList);
        for (final Iterator it = recordList.iterator(); it.hasNext();) {
            final Record record = (Record) it.next();
            final Subclass subclass = (Subclass) param._subclasses.get(record._subclasscd);
            if (null == subclass) {
                continue;
            }

            if ("90".equals(record._classcd)) {
                continue;
            }

            //合併元科目を除く
            if (MOTO_FLG.equals(param._nameMstD070nameSpare1.get(exam._semester + exam._testCd))) {
                if (exam.getAttendSubclassCdList(student).contains(subclass._code)) {
                    continue;
                }

            //合併先科目を除く
            } else if (SAKI_FLG.equals(param._nameMstD070nameSpare1.get(exam._semester + exam._testCd))) {
                if (exam.getCombinedSubclassCdList(student).contains(subclass._code)) {
                    continue;
                }

            }

            final String subclassKey = null != subclass._abbv && subclass._abbv.length() > 4 ? subclass._abbv.substring(0, 4) : subclass._abbv;
            scoreDataset.addValue(record._scorePercent, "得点", subclassKey);
            final AverageDat avgDat = (AverageDat) exam._averages.get(AverageDat.averageKey(param, student, record._subclasscd));
            final BigDecimal avgPercent = (null == avgDat) ? null : avgDat._avgPercent;
            scoreDataset.addValue(avgPercent, "平均", subclassKey);

//            final AverageDat avgDat = (AverageDat) exam._averages.get(AverageDat.averageKey(param, student, record._subclasscd));
//            final BigDecimal avgPercent = (null == avgDat) ? null : avgDat._avgPercent;
//            avgDataset.addValue(avgPercent, param.getItemMsg() + "平均点", subclassKey);

            deviationDataset.addValue(record._deviation, "偏差値", subclassKey);


            //log.info(i + " 棒グラフ⇒" + record._subclasscd + ":素点=" + record._score + ", 平均=" + avgPercent + " / " + (i > BAR_GRAPH_MAX_ITEM));
            i += 1;
            if (i >= BAR_GRAPH_MAX_ITEM) {
                break;
            }
        }
        if (param._isOutptuDebug) {
            log.info("棒グラフ科目数: " + i);
        }

        try {
            // チャート作成
//            final JFreeChart chart = createBarChart(scoreDataset, avgDataset);
            final JFreeChart chart = createBarChart(scoreDataset, deviationDataset);

            // グラフのファイルを生成
            final File outputFile = graphImageFile(chart, 1940, 650);
            param._graphFiles.add(outputFile);

            // グラフの出力
            svf.VrsOut("BAR_LABEL", "個人成績分布");

            if (outputFile.exists()) {
                svf.VrsOut("BAR", outputFile.toString());
            }
        } catch (Throwable e) {
            log.error("exception or error!", e);
        }
    }

    private JFreeChart createBarChart(
            final DefaultCategoryDataset scoreDataset,
            final DefaultCategoryDataset deviationDataset
    ) {
        final JFreeChart chart = ChartFactory.createBarChart(null, null, null, scoreDataset, PlotOrientation.VERTICAL, false, false, false);
        final CategoryPlot plot = chart.getCategoryPlot();
        plot.setRangeGridlineStroke(new BasicStroke(2.0f)); // 目盛りの太さ
        plot.setRangeGridlinePaint(Color.black);
        plot.setRangeGridlineStroke(new BasicStroke(1.0f));

        // 追加する折れ線グラフの表示設定
        final LineAndShapeRenderer renderer2 = new LineAndShapeRenderer();
        renderer2.setItemLabelsVisible(true);
        renderer2.setPaint(Color.black);
        plot.setDataset(1, deviationDataset);
        plot.setRenderer(1, renderer2);
        plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);

        final NumberAxis numberAxis = new NumberAxis();
        numberAxis.setTickUnit(new NumberTickUnit(10));
        numberAxis.setTickLabelsVisible(true);
        numberAxis.setRange(0, 100.0);
        plot.setRangeAxis(numberAxis);

        final CategoryItemRenderer renderer = plot.getRenderer();
        renderer.setSeriesPaint(0, Color.black);
        renderer.setSeriesPaint(1, Color.gray);
        final Font itemLabelFont = new Font("TimesRoman", Font.PLAIN, 11);
        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setTickLabelsVisible(true);
        domainAxis.setTickLabelFont(itemLabelFont);
        log.info(" domainAxis.getTickLabelInsets() = " + domainAxis.getTickLabelInsets());

        BarRenderer barRenderer = (BarRenderer) renderer;
        barRenderer.setMaximumBarWidth(0.05);

        chart.setBackgroundPaint(Color.white);

        return chart;
    }

    private void printRadarGraph(final Vrw32alp svf, final Param param, final Student student, final Exam exam) {

        final List attendSubclassCdList   = exam.getAttendSubclassCdList(student);
        final List combinedSubclassCdList = exam.getCombinedSubclassCdList(student);

        // データ作成
        final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        final Collection<Record> records = new ArrayList<Record>(getMappedMap(student._examRecord, exam.getExamKey()).values());
        for (final Iterator it = param._classOrder.iterator(); it.hasNext();) {
            final String classCd = (String) it.next();

            final List checkRecordList = new ArrayList();
            for (final Iterator rit = records.iterator(); rit.hasNext();) {
                final Record record = (Record) rit.next();
                if (classCd.equals(record._classkey)) {
                    checkRecordList.add(record);
                    rit.remove();
                }
            }
            if (param._isOutptuDebug) {
                log.info(" check radar classcd " + classCd + " / " + checkRecordList);
            }

            for (final Iterator rit = checkRecordList.iterator(); rit.hasNext();) {
                final Record record = (Record) rit.next();

                //合併元科目を除く
                if (MOTO_FLG.equals(param._nameMstD070nameSpare1.get(exam._semester + exam._testCd))) {
                    if (attendSubclassCdList.contains(record._subclasscd)) {
                        continue;
                    }

                //合併先科目を除く
                } else if (SAKI_FLG.equals(param._nameMstD070nameSpare1.get(exam._semester + exam._testCd))) {
                    if (combinedSubclassCdList.contains(record._subclasscd)) {
                        continue;
                    }

                }

                if (param._isOutptuDebug) {
                    log.info("レーダーグラフ⇒" + record._subclasscd + ", " + record._deviation);
                }
                final Class clazz = (Class) param._classes.get(record._classkey);
                final String name = (null == clazz) ? "" : clazz._abbv;
                final String rowKey = "本人偏差値";
                final String columnKey = name;
                final boolean hasValue = dataset.getColumnKeys().contains(columnKey) && null != dataset.getValue(rowKey, columnKey);
                if (hasValue) {
                    if (param._isOutptuDebug) {
                        log.info(" key " + columnKey + " hasValue " + dataset.getValue(rowKey, columnKey) + " / " + classCd + " : " + record._subclasscd);
                    }
                } else {
                    dataset.addValue(record._deviation, rowKey, columnKey);// MAX80, MIN20
                    dataset.addValue(50.0, "偏差値50", columnKey);
                }
            }
        }

        try {
            // チャート作成
            final JFreeChart chart = createRaderChart(dataset);

            // グラフのファイルを生成
            final File outputFile = graphImageFile(chart, 938, 784);
            param._graphFiles.add(outputFile);

            // グラフの出力
            if (0 < dataset.getColumnCount() && outputFile.exists()) {
                svf.VrsOut("RADER", outputFile.toString());
            }
        } catch (Throwable e) {
            log.error("exception or error!", e);
        }
    }

    private JFreeChart createRaderChart(final DefaultCategoryDataset dataset) {
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

    private void printTotal(final Vrw32alp svf, final Param param, final Student student, final Exam exam, final int idx) {

        //idx = 0以外:これまでの成績
        final boolean isBefore = idx != 0 ? true : false;

        String deviationAvg = null; // 各科目の偏差値の平均
        if (!isBefore) {
            final List<Subclass> subclassList = new ArrayList(getMappedMap(student._examSubclasses, exam.getExamKey()).values());
            Collections.sort(subclassList);
            final List<Subclass> printSubclassList = getPrintSubclassList(param, exam, student, subclassList);
            final Map<String, String> subclasscdDeviationMap = new TreeMap<String, String>();
            for (final Subclass subclass : printSubclassList) {

                final Record rec = getMappedMap(student._examRecord, exam.getExamKey()).get(subclass._code);
                if (null != rec) {
                    subclasscdDeviationMap.put(subclass._code, sishagonyu(rec._deviation, 1)); // 偏差値
                }
            }
            deviationAvg = average(subclasscdDeviationMap.values(), 1);
        }

        boolean kekkaFlg = false;

        final Record rec9 = getMappedMap(student._examRecordOther, exam.getExamKey()).get(ALL9);
        if (null != rec9) {
            svf.VrsOut(isBefore ? "TOTAL_LAST_SCORE" + idx : "TOTAL_SCORE", tostr(rec9._score)); //得点
            svf.VrsOut(isBefore ? "TOTAL_LAST_RANK" + idx + "_1": "TOTAL_RANK1", tostr(rec9._classRank)); //クラス順位
            if(isBefore) svf.VrsOut("TOTAL_LAST_RANK" + idx + "_2", tostr(rec9._classRankCount)); //クラス順位(分母)
            svf.VrsOut(isBefore ? "TOTAL_LAST_RANK" + idx + "_3": "TOTAL_RANK2", tostr(rec9._courseRank)); //コース順位
            kekkaFlg = true;
        }
        if (!isBefore) {
            if ("1".equals(param._deviationPrint)) {
                svf.VrsOut("TOTAL_DEVIATION", deviationAvg); //偏差値
            }
        }
        final String key;
        if ("1".equals(param._groupDiv)) {
            key = ALL9;
        } else if ("2".equals(param._groupDiv)) {
            key = ALL9 + student.courseKey();
        } else if ("3".equals(param._groupDiv)) {
            key = ALL9 + student._hrClass;
        } else {
            key = "";
        }
        final AverageDat avg9 = (AverageDat) exam._averagesOther.get(key);
        if (null != avg9) {
            if(isBefore) {
                svf.VrsOut("TOTAL_LAST_RANK" + idx + "_4", tostr(avg9._courseCount));
                svf.VrsOut("TOTAL_LAST_RANK_SLASH" + idx + "_1", "/");
                svf.VrsOut("TOTAL_LAST_RANK_SLASH" + idx + "_2", "/");
                kekkaFlg = true;
            } else {
                svf.VrsOut("TOTAL_AVERAGE", sishagonyu(avg9._avg, 1)); //平均
                svf.VrsOut("TOTAL_EXAMINEE", tostr(avg9._count)); //受験者数
            }
        }

        //欠課
        if(!isBefore) {
            final List subclassList = new ArrayList(getMappedMap(student._examSubclasses, exam.getExamKey()).values());
            Collections.sort(subclassList);
            for (final Iterator it = subclassList.iterator(); it.hasNext();) {
                final Subclass subclass = (Subclass) it.next();
                if ("90".equals(subclass.getClassCd(param))) {
                    continue;
                }
                //合計を設定
                if (student._attendSubClassMap.containsKey(subclass._code)) {
                    final Map<String, SubclassAttendance> attendSubMap = student._attendSubClassMap.get(subclass._code);
                    for (final Iterator itAttendSub = param._semesterMap.keySet().iterator(); itAttendSub.hasNext();) {
                        BigDecimal sick = BigDecimal.ZERO;
                        final String semester = (String) itAttendSub.next();
                        if(!param._semester.equals(semester)) continue;
                        if (attendSubMap.containsKey(semester)) {
                            final SubclassAttendance attendance= attendSubMap.get(semester);
                            if(attendance._sick != null) {
                                sick = attendance._sick;
                            }
                        }
                        //合計を設定
                        if (!student._attendSubClassMap.containsKey(ALL9)) {
                            //ALL9無し
                            student._attendSubClassMap.put(ALL9, new TreeMap());
                        }
                        Map<String, SubclassAttendance> setSubAttendMap = student._attendSubClassMap.get(ALL9);
                        if (setSubAttendMap.containsKey(semester)) {
                            //学期一致
                            final SubclassAttendance attendance = setSubAttendMap.get(semester);
                            if(sick != null) sick = sick.add(attendance._sick);
                        }
                        setSubAttendMap.put(semester, new SubclassAttendance(null, null, sick, null, null));
                    }
                }
            }

            if (student._attendSubClassMap.containsKey(ALL9)) {
                final Map<String, SubclassAttendance> attendSubMap = student._attendSubClassMap.get(ALL9);
                for (final Iterator itAttendSub = param._semesterMap.keySet().iterator(); itAttendSub.hasNext();) {
                    BigDecimal sick = BigDecimal.ZERO;
                    final String semester = (String) itAttendSub.next();
                    if(!param._semester.equals(semester)) continue;

                    if (attendSubMap.containsKey(semester)) {
                        final SubclassAttendance attendance= attendSubMap.get(semester);
                        if(attendance._sick != null) {
                            sick = attendance._sick;
                        }
                    }
                    svf.VrsOut("TOTAL_KEKKA", sick.toString()); //欠課
                }
            } else {
                //科目が存在しない場合、'0'を印字
                if(kekkaFlg) svf.VrsOut("TOTAL_KEKKA", "0"); //欠課
            }
        }
    }

    private List<Subclass> getPrintSubclassList(final Param param, final Exam exam, final Student student, final List<Subclass> subclassList) {
        final List<Subclass> rtn = new ArrayList<Subclass>();
        for (final Subclass subclass : subclassList) {
            if (printChkBefore(param, student, exam, subclass)) {
                rtn.add(subclass);
            }
        }
        return rtn;
    }

    //表の出力処理。dispWhiteSpaceと処理を合わせているので注意(チェックでcontinueする、とかループを抜ける、とか。dispWhiteSpace()で「これまでの成績」を非表示にする判定をしているため。svfのrecord処理をした後だと表示されない。)
    private void printTab(final DB2UDB db2, final Vrw32alp svf, final Param param, final Student student, final Exam exam) {
        int i = 0;
        final List<Subclass> subclassList = new ArrayList(getMappedMap(student._examSubclasses, exam.getExamKey()).values());
        Collections.sort(subclassList);
        final List<Subclass> printSubclassList = getPrintSubclassList(param, exam, student, subclassList);
        dispWhiteSpace(svf, param, student, printSubclassList);

        //対象生徒と同一なコースの生徒の得点を取得
        final Map<String, Map<String, String>> subclassCdResultMap = getCourseScoreMap(db2, param, student, exam);

        for (final Subclass subclass : printSubclassList) {

            final Record record = getMappedMap(student._examRecord, exam.getExamKey()).get(subclass._code);
            final AverageDat avgDat = (AverageDat) exam._averages.get(AverageDat.averageKey(param, student, subclass._code));

            svfVrsOutWithCheckMojisu(svf, param, new String[][] {{"SUBCLASS"}}, true, subclass._abbv);

            final String score = tostr(record._score);
            svf.VrsOut("SCORE", score); //得点
            svf.VrsOut("RANK1", tostr(record._classRank)); //クラス順位
            svf.VrsOut("RANK2", tostr(record._courseRank)); //コース順位
            if ("1".equals(param._deviationPrint)) {
                svf.VrsOut("DEVIATION", sishagonyu(record._deviation, 1)); //偏差値
            }

            if (null != avgDat) {
                svf.VrsOut("AVERAGE", sishagonyu(avgDat._avg, 1)); //平均点
//                svf.VrsOut(isBefore ? "PRE_MAX_SCORE" : "MAX_SCORE", tostr(avgDat._highScore)); //最高得点
                svf.VrsOut("EXAMINEE", tostr(avgDat._count));    // 受験者数
            }

            //欠課
            if (student._attendSubClassMap.containsKey(subclass._code)) {
                final Map<String, SubclassAttendance> attendSubMap = student._attendSubClassMap.get(subclass._code);
                for (final Iterator itAttendSub = param._semesterMap.keySet().iterator(); itAttendSub.hasNext();) {
                    BigDecimal sick = BigDecimal.ZERO;
                    final String semester = (String) itAttendSub.next();
                    if(!param._semester.equals(semester)) continue;
                    if (attendSubMap.containsKey(semester)) {
                        final SubclassAttendance attendance= attendSubMap.get(semester);
                        if(attendance._sick != null) {
                            sick = attendance._sick;
                        }
                    }
                    svf.VrsOut("KEKKA", sick.toString()); //欠課
                }
            } else {
                //科目が存在しない場合、'0'を印字
                svf.VrsOut("KEKKA", "0"); //欠課
            }

            //分布表
            printBunpu(svf, param, student, param._thisExam, getMappedMap(subclassCdResultMap, subclass._code));


            //これまでの成績
            int idx = 1;
            for (final String key : param._beforeExamMap.keySet()) {
                boolean printFlg = false;
                final Exam beforeExam = (Exam)param._beforeExamMap.get(key);
                printFlg = printBefore(svf, param, student, beforeExam, subclass, idx);
                if (printFlg) {
                    idx++ ;
                } else {
                    idx++ ;
                }
            }

            svf.VrEndRecord();
            if (++i >= TABLE_SUBCLASS_MAX) {
                break;
            }
        }
        if (i == 0) {
            svf.VrEndRecord(); // データが無い時もこのコマンドを発行しないと印刷されないから
        }
        //log.debug("  exam = " + exam.getExamKey() + ", subclasses = " + subclasses.size());
    }

    //「これまでの成績」が出力不要なのかを判定。printTabと処理を合わせているので注意(チェックでcontinueする、とかループを抜ける、とか。)
    private void dispWhiteSpace(final Vrw32alp svf, final Param param, final Student student, final List<Subclass> printSubclassList) {
        int i = 0;
        int beforePrtCnt = 0;
        for (final Subclass subclass : printSubclassList) {

            //これまでの成績(dCntは呼び出し元のidxとは異なるので注意。)
            int dCnt = 0;
            for (final String key : param._beforeExamMap.keySet()) {
                boolean printFlg = false;
                final Exam beforeExam = param._beforeExamMap.get(key);
                printFlg = printChkBefore(param, student, beforeExam, subclass);
                if (printFlg) {
                    dCnt++ ;
                }
            }
            beforePrtCnt += dCnt;

            if (++i >= TABLE_SUBCLASS_MAX) {
                break;
            }
        }
        if (beforePrtCnt == 0) {
            //これまでの成績で何も出力しなかった場合、表を隠す。
            svf.VrsOut("BLANK", param._whiteSpaceImagePath);
        }
    }

    private boolean printChkBefore(final Param param, final Student student, final Exam exam, final Subclass subclass) {
        if ("90".equals(subclass.getClassCd(param))) {
            return false;
        }

        //合併元科目を除く
        if (MOTO_FLG.equals(param._nameMstD070nameSpare1.get(exam._semester + exam._testCd))) {
            if (exam.getAttendSubclassCdList(student).contains(subclass._code)) {
                return false;
            }

        //合併先科目を除く
        } else if (SAKI_FLG.equals(param._nameMstD070nameSpare1.get(exam._semester + exam._testCd))) {
            if (exam.getCombinedSubclassCdList(student).contains(subclass._code)) {
                return false;
            }
        }

        final Record record = getMappedMap(student._examRecord, exam.getExamKey()).get(subclass._code);
        //成績が無い場合、科目名称を出力せずに処理終了。
        if (null == record) {
            return false;
        }

        final Class clazz = (Class) param._classes.get(subclass.getClassKey(param));
        if (null == clazz) {
            return false;
        }
        return true;
    }

    private boolean printBefore(final Vrw32alp svf, final Param param, final Student student, final Exam exam, final Subclass subclass, final int idx) {
        if (!printChkBefore(param, student, exam, subclass)) {
            return false;
        }

        final Record record = getMappedMap(student._examRecord, exam.getExamKey()).get(subclass._code);

        svfVrsOutWithCheckMojisu(svf, param, new String[][] {{"LAST_SUBCLASS"}}, true, subclass._name);

        final String score = tostr(record._score);
//        svf.VrsOut(isBefore ? "PRE_SCORE" : "SCORE", param.isUnderScore(score) ? "(" + score + ")" : score);
        svf.VrsOut("LAST_SCORE" + idx, score); //得点

        svf.VrsOut("LAST_RANK" + idx + "_1", tostr(record._classRank)); //クラス順位
        svf.VrsOut("LAST_RANK" + idx + "_2", tostr(record._classRankCount)); //クラス順位(分母)
        svf.VrsOut("LAST_RANK" + idx + "_3", tostr(record._courseRank)); //コース順位

        final AverageDat avgDat = exam._averages.get(AverageDat.averageKey(param, student, subclass._code));
        if (null != avgDat) {
            svf.VrsOut("LAST_RANK" + idx + "_4", tostr(avgDat._courseCount));
            svf.VrsOut("LAST_RANK_SLASH" + idx + "_1", "/");
            svf.VrsOut("LAST_RANK_SLASH" + idx + "_2", "/");
        }
        return true;
    }
    private void svfVrsOutWithCheckMojisu(final Vrw32alp svf, final Param param, final String[] useField, final boolean tategaki, final String data) {

        if (null != useField) {
            final int fieldKeta = getFieldKeta(param, useField[0], 0) / (tategaki ? 2 : 1); // 縦書きなので / 2
            final List tokenList = getTokenList(data, tategaki, fieldKeta);
            for (int i = 0; i < Math.min(useField.length,  tokenList.size()); i++) {
                svf.VrsOut(useField[i], (String) tokenList.get(i));
            }
        }
    }

    private String[] getUseField(final Vrw32alp svf, final Param param, final boolean tategaki, final String[][] fields, final String data) {
        if (null == data) {
            return null;
        }
        if (null == fields || fields.length == 0) {
            throw new IllegalArgumentException("フィールド名指定不正");
        }
        final int dataKeta = tategaki ? data.length() : KNJ_EditEdit.getMS932ByteLength(data);
        String[] lastField = null;
        String[] firstValidField = null;
        for (int i = 0; i < fields.length; i++) {
            final int fieldKeta = getFieldKeta(param, fields[i][0], 0) / (tategaki ? 2 : 1); // 縦書きなので / 2
            if (fieldKeta > 0) {
                if (null == firstValidField && dataKeta <= fieldKeta) {
                    firstValidField = fields[i];
                }
                lastField = fields[i];
            }
        }
        final String[] useField;
        if (null != firstValidField) {
            useField = firstValidField;
        } else if (null != lastField) {
            useField = lastField;
        } else {
            log.warn("no such field:" + ArrayUtils.toString(fields));
            useField = null;
        }
        return useField;
    }

    private static List getTokenList(final String data, final boolean tategaki, final int fieldKeta) {
        final List tokenList = new ArrayList();
        if (StringUtils.isBlank(data) || fieldKeta < 1) {
            return tokenList;
        }
        if (tategaki) {
            final StringBuffer buf = new StringBuffer(data);
            while (buf.length() > 0) {
                int width = Math.min(buf.length(), fieldKeta);
                tokenList.add(buf.substring(0, width));
                buf.delete(0, width);
            }
        } else {
            final String[] tokens = KNJ_EditEdit.get_token_1(data, fieldKeta, 99);
            if (null != tokens) {
                int lastNotNull = -1;
                for (int i = tokens.length - 1; i >= 0; i--) {
                    if (null != tokens[i]) {
                        lastNotNull = i + 1;
                        break;
                    }
                }
                for (int i = 0; i < lastNotNull; i++) {
                    tokenList.add(tokens[i]);
                }
            }
        }
        return tokenList;
    }

    private static int getFieldKeta(final Param param, final String fieldname, final int defaultKeta) {
        try {
            SvfField field = param._formnameFormFieldMap.get(param._currentform).get(fieldname);
            if (null != field) {
//                log.info(" field " + fieldname + " = " + field._fieldLength);
                return field._fieldLength;
            }
        } catch (Throwable t) {
            log.warn(t);
        }
        return defaultKeta;
    }

    private void printBunpu(final Vrw32alp svf, final Param param, final Student student, final Exam exam, final Map<String, String> resultMap) {

        // 100段階 10点きざみ(100,99～90,89～80,,,,9～0)
        final List<ScoreRange> rangeList = new ArrayList();
        final int max = 100;
        final int kizami = 10;
        final int min = 0;

        final int addMax = max;
        rangeList.add(new ScoreRange(addMax, 999));
        for (int i = max; i - kizami >= min; i -= kizami) {
            rangeList.add(new ScoreRange(i - kizami, i));
        }

        final Map<String, String> copy = new HashMap(resultMap);
        //分布表
        for (int ri = 0; ri < rangeList.size(); ri++) {
            final ScoreRange sr = rangeList.get(ri);
            final ScoreDistribution dist = new ScoreDistribution();
            for (final Iterator<String> it = copy.keySet().iterator(); it.hasNext();) {
                final String schno = it.next();
                final String sscore = resultMap.get(schno);
                final int score = Integer.parseInt(sscore);

                final int high = sr._highExclusive;
                if (sr._lowInclusive <= score && score < high) {
                    dist._totalScore.add(new BigDecimal(score));
                    it.remove();
                }
            }
            final int field = ri+1;
            svf.VrsOut("HIST" + field, String.valueOf(dist.totalCount()));
        }
    }

    private Map<String, Map<String, String>> getCourseScoreMap (final DB2UDB db2, final Param param, final Student student, final Exam exam) {

        final String cacheKey = exam._semester + exam._testCd + "|" + student._grade + "-" + student._courseCd + student._majorCd + student._courseCode; // テストごと、コースごと
        if (!param._distributionCacheMap.containsKey(cacheKey)) {
            Map<String, Map<String, String>> resultMap = new TreeMap<String, Map<String, String>>();
            log.info(" fetch distribution : " + cacheKey);

            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH SCHNO AS ( ");
            stb.append(" SELECT DISTINCT  ");
            stb.append("   T1.* ");
            stb.append(" FROM  ");
            stb.append("   SCHREG_REGD_DAT T1 ");
            stb.append("   INNER JOIN SCHREG_REGD_GDAT T2 ");
            stb.append("           ON T2.YEAR  = T1.YEAR ");
            stb.append("          AND T2.GRADE = T1.GRADE ");
            stb.append("          AND T2.SCHOOL_KIND = '"+ param._schoolKind +"' ");
            stb.append(" WHERE  ");
            stb.append("       T1.YEAR       = '"+ param._year +"' ");
            stb.append("   AND T1.SEMESTER   = '"+ param._semester +"' ");
            stb.append("   AND T1.GRADE      = '"+ param._grade +"' ");
            stb.append("   AND T1.COURSECD   = '"+ student._courseCd +"' ");
            stb.append("   AND T1.MAJORCD    = '"+ student._majorCd +"' ");
            stb.append("   AND T1.COURSECODE = '"+ student._courseCode +"' ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("   T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("   T1.SCHREGNO, ");
            stb.append("   T2.SCORE ");
            stb.append(" FROM ");
            stb.append("   SCHNO T1 ");
            stb.append("   INNER JOIN RECORD_RANK_SDIV_DAT T2  ");
            stb.append("           ON T2.YEAR     = T1.YEAR ");
            stb.append("          AND T2.SEMESTER = T1.SEMESTER ");
            stb.append("          AND T2.TESTKINDCD = '" + exam.getKindCd() + "' AND T2.TESTITEMCD = '" + exam.getItemCd() + "' AND T2.SCORE_DIV = '"+ exam.getScoreDiv() +"' ");
            stb.append("          AND T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("          AND T2.SCORE IS NOT NULL ");
            final String sql =  stb.toString();

            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                log.debug(sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    getMappedMap(resultMap, rs.getString("SUBCLASSCD")).put(rs.getString("SCHREGNO"), StringUtils.defaultString(rs.getString("SCORE")));
                }
            } catch (SQLException ex) {
                log.debug("Exception:", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            param._distributionCacheMap.put(cacheKey, resultMap);
        }
        return param._distributionCacheMap.get(cacheKey);
    }

    private static class ScoreRange {
        final int _lowInclusive;
        final int _highExclusive;
        public ScoreRange(final int low, final int high) {
            _lowInclusive = low;
            _highExclusive = high;
        }
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
        private Map<String, AverageDat> _averages = new HashMap();
        /** 成績平均データ。3教科,5教科用 */
        private Map _averagesOther = new HashMap();
//        /** 前回の換算した得点、平均、最高点 */
//        private ConvertedScore _convertedScore;
        /** 合併元科目コード */
        private Map _attendSubclassCdMap = Collections.EMPTY_MAP;
        /** 合併先科目コード */
        private Map _combinedSubclassCdMap = Collections.EMPTY_MAP;

        public Exam(final String year, final String semester, final String testCd) {
            _year = year;
            _semester = semester;
            _testCd = testCd;
        }

        private static void loadScore(final DB2UDB db2, final List<Student> students, final Exam exam, final Param param) throws SQLException {
            log.error("成績関連の読込み " + exam._semester + exam._testCd);
            Student.loadSubClasses(db2, exam, students, param);
            Exam.loadAverageDat(db2, exam, param);
            Student.loadRecord(db2, exam, students, param);
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
            setName(db2, param);
            loadAttendSubclassCdMap(db2, param);
            loadCombinedSubclassCdMap(db2, param);
        }

        private void loadAttendSubclassCdMap(final DB2UDB db2, final Param param) {
            final StringBuffer stb = new StringBuffer();
//          final String flg = "9900".equals(_testCd) ? "2" : "1";
//          stb.append("   SELECT ");
//          stb.append("       T1.GRADE || T1.COURSECD || T1.MAJORCD || T1.COURSECODE AS KEY, ");
//          if ("1".equals(param._useCurriculumcd)) {
//              stb.append("       T1.ATTEND_CLASSCD || '-' || T1.ATTEND_SCHOOL_KIND || '-' || T1.ATTEND_CURRICULUM_CD || '-' || ");
//          }
//          stb.append("       T1.ATTEND_SUBCLASSCD AS ATTEND_SUBCLASSCD ");
//          stb.append("   FROM ");
//          stb.append("       SUBCLASS_WEIGHTING_COURSE_DAT T1 ");
//          stb.append("   WHERE ");
//          stb.append("       T1.YEAR = '" + _year + "' ");
//          stb.append("       AND T1.FLG = '" + flg + "' ");
//
//          ps = db2.prepareStatement(stb.toString());
//          rs = ps.executeQuery();
//          while (rs.next()) {
//              getMappedList(map, rs.getString("KEY")).add(rs.getString("ATTEND_SUBCLASSCD"));
//          }

            stb.append("   SELECT ");
            stb.append("       T1.ATTEND_CLASSCD || '-' || T1.ATTEND_SCHOOL_KIND || '-' || T1.ATTEND_CURRICULUM_CD || '-' || T1.ATTEND_SUBCLASSCD AS ATTEND_SUBCLASSCD ");
            stb.append("   FROM ");
            stb.append("       SUBCLASS_REPLACE_COMBINED_DAT T1 ");
            stb.append("   WHERE ");
            stb.append("       T1.YEAR = '" + _year + "' ");

            final Map map = new HashMap();
            final List attList = KnjDbUtils.getColumnDataList(KnjDbUtils.query(db2, stb.toString()), "ATTEND_SUBCLASSCD");
            if (!attList.isEmpty()) {
                getMappedList(map, "0").addAll(attList);
            }
            _attendSubclassCdMap = map;
        }

        public List getAttendSubclassCdList(final Student student) {
//            return getMappedList(_attendSubclassCdMap, student.attendSubclassCdKey());
            return getMappedList(_attendSubclassCdMap, "0");
        }

        private void loadCombinedSubclassCdMap(final DB2UDB db2, final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append("   SELECT ");
            stb.append("       T1.COMBINED_CLASSCD || '-' || T1.COMBINED_SCHOOL_KIND || '-' || T1.COMBINED_CURRICULUM_CD || '-' || T1.COMBINED_SUBCLASSCD AS COMBINED_SUBCLASSCD ");
            stb.append("   FROM ");
            stb.append("       SUBCLASS_REPLACE_COMBINED_DAT T1 ");
            stb.append("   WHERE ");
            stb.append("       T1.YEAR = '" + _year + "' ");

            final Map map = new HashMap();
            final List combList = KnjDbUtils.getColumnDataList(KnjDbUtils.query(db2, stb.toString()), "COMBINED_SUBCLASSCD");
            if (!combList.isEmpty()) {
                getMappedList(map, "0").addAll(combList);
            }
            _combinedSubclassCdMap = map;
        }

        public List getCombinedSubclassCdList(final Student student) {
            return getMappedList(_combinedSubclassCdMap, "0");
        }

        private void setName(final DB2UDB db2, final Param param) {
            if (null == _year) {
                _title = "";
                return;
            }
            final StringBuffer sql = new StringBuffer();
            sql.append("SELECT testitemname FROM testitem_mst_countflg_new_sdiv ");
            sql.append(" WHERE year = '" + _year + "' ");
            sql.append("   AND semester = '"  + _semester + "'");
            sql.append("   AND testkindcd = '" + getKindCd() + "' ");
            sql.append("   AND testitemcd = '" + getItemCd() + "'");
            sql.append("   AND score_div = '" + getScoreDiv() + "'");

            _examName = StringUtils.defaultString(KnjDbUtils.getOne(KnjDbUtils.query(db2, sql.toString())));

            PreparedStatement ps = null;
            ResultSet rs = null;
            _semestername = "";
            try {
                final String semesSql = "SELECT semestername FROM semester_mst WHERE year = '" + _year + "' AND semester = '" + _semester + "'";
                ps = db2.prepareStatement(semesSql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    _semestername = StringUtils.defaultString(rs.getString("semestername")) + "　";
                }
            } catch (final SQLException e) {
                log.error("学期名取得エラー。", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            _title = param._nendo + "　" + _semestername + _examName;

        }

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
            selectFrom = "SELECT";
            selectFrom += "  case when t1.subclasscd in ('" + ALL3 + "', '" + ALL5 + "', '" + ALL9 + "') then t1.subclasscd  ";
            selectFrom += "   else t1.classcd || '-' || t1.school_kind || '-' || t1.curriculum_cd || '-' || t1.subclasscd ";
            selectFrom += "  end as subclasscd,";
            selectFrom += ""
                + "  t1.avg,"
                + "  t1.stddev,"
                + "  t1.highscore,"
                + "  t1.count,"
                + "  COURSE_AVG.COUNT AS COURSE_COUNT,"
                + "  t1.coursecd,"
                + "  t1.majorcd,"
                + "  t1.coursecode,"
                + "  value(t2.perfect, 100) as perfect,"
                + "  t1.hr_class"
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
            selectFrom += ""

                    + "  LEFT JOIN RECORD_AVERAGE_SDIV_DAT COURSE_AVG "
                    + "         ON COURSE_AVG.YEAR          = T1.YEAR "
                    + "        AND COURSE_AVG.SEMESTER      = T1.SEMESTER "
                    + "        AND COURSE_AVG.TESTKINDCD    = T1.TESTKINDCD "
                    + "        AND COURSE_AVG.TESTITEMCD    = T1.TESTITEMCD "
                    + "        AND COURSE_AVG.SCORE_DIV     = T1.SCORE_DIV "
                    + "        AND COURSE_AVG.CLASSCD       = T1.CLASSCD "
                    + "        AND COURSE_AVG.SCHOOL_KIND   = T1.SCHOOL_KIND "
                    + "        AND COURSE_AVG.CURRICULUM_CD = T1.CURRICULUM_CD "
                    + "        AND COURSE_AVG.SUBCLASSCD    = T1.SUBCLASSCD "
                    + "        AND COURSE_AVG.AVG_DIV       = '3' "
                    + "        AND COURSE_AVG.GRADE         = T1.GRADE "
                    + "        AND COURSE_AVG.HR_CLASS      = '000' "
                    + "        AND COURSE_AVG.COURSECD      = T1.COURSECD "
                    + "        AND COURSE_AVG.MAJORCD       = T1.MAJORCD "
                    + "        AND COURSE_AVG.COURSECODE    = T1.COURSECODE ";
            String where;
            where = " WHERE"
                    + "    t1.year = '" + exam._year + "' AND"
                    + "    t1.semester = '" + exam._semester + "' AND"
                    + "    t1.testkindcd = '" + exam.getKindCd() + "' AND"
                    + "    t1.testitemcd = '" + exam.getItemCd() + "' AND"
                    + "    t1.score_div = '" + exam.getScoreDiv() + "' ";
            if ("1".equals(param._groupDiv)) {
                where += " AND "
                    + " t1.avg_div = '1' AND"
                    + "    t1.grade = '" + exam._grade + "' AND"
                    + "    t1.hr_class = '000' AND"
                    + "    t1.coursecd = '0' AND"
                    + "    t1.majorcd = '000' AND"
                    + "    t1.coursecode = '0000'"
                    ;
            } else if ("2".equals(param._groupDiv)) {
                where += " AND "
                    + " t1.avg_div = '3' AND"
                    + "    t1.grade = '" + exam._grade + "' AND"
                    + "    t1.hr_class = '000'"
                    ;
            } else if ("3".equals(param._groupDiv)) {
                where += " AND "
                        + " t1.avg_div = '2' AND"
                        + "    t1.grade = '" + exam._grade + "' AND"
                        + "    t1.coursecd = '0' AND"
                        + "    t1.majorcd = '000' AND"
                        + "    t1.coursecode = '0000'"
                        ;
            }

            PreparedStatement ps = null;
            try {
                final String sql = selectFrom + where;
                ps = db2.prepareStatement(sql);
                for (final Iterator it = KnjDbUtils.query(db2, sql).iterator(); it.hasNext();) {
                    final Map row = (Map) it.next();
                    final String subclasscd = KnjDbUtils.getString(row, "subclasscd");
                    final Subclass subclass = (Subclass) param._subclasses.get(subclasscd);
                    if (null == subclass) {
                        if (!(ALL3.equals(subclasscd) || ALL5.equals(subclasscd) || ALL9.equals(subclasscd))) {
                            log.warn("成績平均データが科目マスタに無い!:" + subclasscd);
                        }
                    }
                    final BigDecimal avg = KnjDbUtils.getBigDecimal(row, "avg", null);
                    BigDecimal avgPercent = null;
                    final int perfect = KnjDbUtils.getInt(row, "PERFECT", new Integer(100)).intValue();
                    if (100 == perfect) {
                        avgPercent = avg;
                    } else if (null != avg){
                        avgPercent = avg.multiply(new BigDecimal(100)).divide(new BigDecimal(perfect), 1, BigDecimal.ROUND_HALF_UP);
                    }
                    final BigDecimal stdDev = KnjDbUtils.getBigDecimal(row, "STDDEV", null);
                    final Integer count = KnjDbUtils.getInt(row, "count", null);
                    final Integer courseCount = KnjDbUtils.getInt(row, "COURSE_COUNT", null);
                    final Integer highScore = KnjDbUtils.getInt(row, "highscore", null);
                    final String coursecd = KnjDbUtils.getString(row, "coursecd");
                    final String majorcd = KnjDbUtils.getString(row, "majorcd");
                    final String coursecode = KnjDbUtils.getString(row, "coursecode");
                    final String hrClass = KnjDbUtils.getString(row, "hr_class");


                    final AverageDat avgDat = new AverageDat(subclasscd, avg, avgPercent, stdDev, highScore, count, courseCount, coursecd, majorcd, coursecode);
                    if (ALL3.equals(subclasscd) || ALL5.equals(subclasscd) || ALL9.equals(subclasscd)) {
                        exam._averagesOther.put(AverageDat.averageKey(param, subclasscd, coursecd, majorcd, coursecode, hrClass), avgDat);
                    } else {
                        exam._averages.put(AverageDat.averageKey(param, subclasscd, coursecd, majorcd, coursecode, hrClass), avgDat);
                    }
                }
            } catch (final SQLException e) {
                log.warn("成績平均データの取得でエラー", e);
                throw e;
            } finally {
                DbUtils.closeQuietly(ps);
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
        final String _majorName;
        final String _courseName;

        final String _name;

        /** 成績所見データ. */
        final String _remark;

        /** 成績科目 */
        private final Map _examSubclasses = new TreeMap();

        /** 成績データ。 */
        private final Map<String, Map<String, Record>> _examRecord = new TreeMap();

        /** 成績データ。3教科,5教科用 */
        private final Map<String, Map<String, Record>> _examRecordOther = new HashMap();

        final Map _attendMap = new TreeMap();
        final Map _printSubclassMap = new TreeMap();
        final Map<String, Map<String, SubclassAttendance>> _attendSubClassMap = new HashMap();

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
                final String majorName,
                final String courseName,
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
            _majorName = majorName;
            _courseName = courseName;
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

        private static List<Student> createStudents(final DB2UDB db2, final Param param) throws SQLException {
            final List<Student> rtn = new LinkedList();

            final StringBuffer sql = new StringBuffer();
            sql.append("SELECT");
            sql.append("  regd.SCHREGNO,");
            sql.append("  regd.GRADE,");
            sql.append("  regd.HR_CLASS,");
            sql.append("  regd.ATTENDNO,");
            sql.append("  hdat.HR_NAME,");
            sql.append("  regd.COURSECD,");
            sql.append("  regd.MAJORCD,");
            sql.append("  regd.COURSECODE,");
            sql.append("  MAJR.MAJORNAME,");
            sql.append("  COURSE.COURSECODENAME,");
            sql.append("  base.NAME,");
            sql.append("  t3.REMARK1,");
            sql.append("  t4.GROUP_CD ");
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
            sql.append("    LEFT JOIN V_MAJOR_MST MAJR ON REGD.YEAR     = MAJR.YEAR ");
            sql.append("                              AND REGD.COURSECD = MAJR.COURSECD");
            sql.append("                              AND REGD.MAJORCD = MAJR.MAJORCD");
            sql.append("    LEFT JOIN COURSECODE_MST COURSE ON REGD.COURSECODE = COURSE.COURSECODE ");
            sql.append(" WHERE");
            sql.append("  regd.year = '" + param._year + "' AND");
            sql.append("  regd.semester = '" + param._semester + "' AND");
            if ("1".equals(param._categoryIsClass)) {
                sql.append("  regd.grade || '-' || regd.hr_class IN " + SQLUtils.whereIn(true, param._categorySelected));
            } else {
                sql.append("  regd.schregno IN " + SQLUtils.whereIn(true, param._categorySelected));
            }
            sql.append(" ORDER BY regd.grade, regd.hr_class, regd.attendno ");

            for (final Iterator it = KnjDbUtils.query(db2, sql.toString()).iterator(); it.hasNext();) {
                final Map row = (Map) it.next();
                final String schregno = KnjDbUtils.getString(row, "SCHREGNO");
                final String grade = KnjDbUtils.getString(row, "GRADE");
                final String hrclass = KnjDbUtils.getString(row, "HR_CLASS");
                final String attendno = KnjDbUtils.getString(row, "ATTENDNO");
                final String hrName = KnjDbUtils.getString(row, "HR_NAME");
                final String coursecd = KnjDbUtils.getString(row, "COURSECD");
                final String majorcd = KnjDbUtils.getString(row, "MAJORCD");
                final String coursecode = KnjDbUtils.getString(row, "COURSECODE");
                final String coursegroupCd = KnjDbUtils.getString(row, "GROUP_CD");
                final String majorName = KnjDbUtils.getString(row, "MAJORNAME");
                final String courseName = KnjDbUtils.getString(row, "COURSECODENAME");
                final String name = KnjDbUtils.getString(row, "NAME");
                final String remark = KnjDbUtils.getString(row, "REMARK1");

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
                        majorName,
                        courseName,
                        name,
                        remark
                );
                rtn.add(student);
            }
            return rtn;
        }

        private static void loadSubClasses(final DB2UDB db2, final Exam exam, final List<Student> students, final Param param) throws SQLException {
            PreparedStatement ps = null;

            final StringBuffer sql = new StringBuffer();
            sql.append("select");
            sql.append("  distinct T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD");
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
            try {
                ps = db2.prepareStatement(sql.toString());
                for (final Iterator it = students.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    for (final Iterator rit = KnjDbUtils.query(db2, ps, new Object[] {student._schregno}).iterator(); rit.hasNext();) {
                        final Map row = (Map) rit.next();
                        final String subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");

                        final Subclass subClass = (Subclass) param._subclasses.get(subclasscd);
                        if (null != subClass) {
                            getMappedMap(student._examSubclasses, exam.getExamKey()).put(subclasscd, subClass);
                        }
                    }
                }
            } finally {
                DbUtils.closeQuietly(ps);
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
                final List<Student> students,
                final Param param
        ) {
            final String gradeRank  = "2".equals(param._outputKijun) ? "grade_avg_rank" : "3".equals(param._outputKijun) ? "grade_deviation_rank" : "grade_rank";
            final String courseRank = "2".equals(param._outputKijun) ? "course_avg_rank" : "3".equals(param._outputKijun) ? "course_deviation_rank" : "course_rank";
            final String majorRank = "2".equals(param._outputKijun) ? "major_avg_rank" : "3".equals(param._outputKijun) ? "major_deviation_rank" : "major_rank";
            final String classRank = "2".equals(param._outputKijun) ? "class_avg_rank" : "3".equals(param._outputKijun) ? "class_deviation_rank" : "class_rank";

            final StringBuffer sql = new StringBuffer();
            /* 通常の成績 */
            sql.append("SELECT");
            sql.append("  t1.CLASSCD,");
            sql.append("  t1.classcd || '-' || t1.school_kind as CLASS_KEY,");
            sql.append("  t1.classcd || '-' || t1.school_kind || '-' || t1.curriculum_cd || '-' || t1.subclasscd as SUBCLASSCD,");
            sql.append("  t2.SCORE,");
            sql.append("  t2." + gradeRank + " as GRADE_RANK,");
            sql.append("  t2.GRADE_DEVIATION,");
            sql.append("  t2." + courseRank + " as COURSE_RANK,");
            sql.append("  t2.COURSE_DEVIATION,");
            sql.append("  t2." + majorRank + " as MAJOR_RANK,");
            sql.append("  t2.MAJOR_DEVIATION,");
            sql.append("  t2." + classRank + " as CLASS_RANK,");
            sql.append("  CLASS_AVG.COUNT AS CLASS_COUNT,");
            sql.append("  t2.CLASS_DEVIATION,");
            sql.append("  t3.DIV,");
            sql.append("  value(t3.perfect, 100) as PERFECT");
            sql.append(" FROM");
            sql.append("  record_score_dat t1 ");
            sql.append(" LEFT JOIN record_rank_sdiv_dat t2 ON t1.year = t2.year AND");
            sql.append("    t1.semester = t2.semester AND");
            sql.append("    t1.testkindcd = t2.testkindcd AND");
            sql.append("    t1.testitemcd = t2.testitemcd AND");
            sql.append("    t1.score_div = t2.score_div AND");
            sql.append("    t1.classcd = t2.classcd AND");
            sql.append("    t1.school_kind = t2.school_kind AND");
            sql.append("    t1.curriculum_cd = t2.curriculum_cd AND");
            sql.append("    t1.subclasscd = t2.subclasscd AND");
            sql.append("    t1.schregno = t2.schregno ");
            sql.append(" LEFT JOIN perfect_record_sdiv_dat t3 ON t1.year = t3.year AND");
            sql.append("    t1.semester = t3.semester AND");
            sql.append("    t1.testkindcd = t3.testkindcd AND");
            sql.append("    t1.testitemcd = t3.testitemcd AND");
            sql.append("    t1.score_div = t3.score_div AND");
            sql.append("    t1.classcd = t3.classcd AND");
            sql.append("    t1.school_kind = t3.school_kind AND");
            sql.append("    t1.curriculum_cd = t3.curriculum_cd AND");
            sql.append("    t1.subclasscd = t3.subclasscd AND");
            sql.append("    t3.grade = (case when t3.div = '01' then '00' else ? end) AND ");
            sql.append("    t3.coursecd || t3.majorcd || t3.coursecode = ");
            sql.append("        (case when t3.div in ('01', '02') then '00000000' ");
            sql.append("              when t3.div = '04' then '0' || ? || '0000' ");
            sql.append("              else ? end) ");
            sql.append("  LEFT JOIN SCHREG_REGD_DAT REGD ");
            sql.append("         ON REGD.YEAR     = T1.YEAR ");
            sql.append("        AND REGD.SEMESTER = T1.SEMESTER ");
            sql.append("        AND REGD.SCHREGNO = T1.SCHREGNO ");
            sql.append("  LEFT JOIN RECORD_AVERAGE_SDIV_DAT CLASS_AVG ");
            sql.append("         ON CLASS_AVG.YEAR          = T1.YEAR ");
            sql.append("        AND CLASS_AVG.SEMESTER      = T1.SEMESTER ");
            sql.append("        AND CLASS_AVG.TESTKINDCD    = T1.TESTKINDCD ");
            sql.append("        AND CLASS_AVG.TESTITEMCD    = T1.TESTITEMCD ");
            sql.append("        AND CLASS_AVG.SCORE_DIV     = T1.SCORE_DIV ");
            sql.append("        AND CLASS_AVG.CLASSCD       = T1.CLASSCD ");
            sql.append("        AND CLASS_AVG.SCHOOL_KIND   = T1.SCHOOL_KIND ");
            sql.append("        AND CLASS_AVG.CURRICULUM_CD = T1.CURRICULUM_CD ");
            sql.append("        AND CLASS_AVG.SUBCLASSCD    = T1.SUBCLASSCD ");
            sql.append("        AND CLASS_AVG.AVG_DIV       = '2' ");
            sql.append("        AND CLASS_AVG.GRADE         = REGD.GRADE ");
            sql.append("        AND CLASS_AVG.HR_CLASS      = REGD.HR_CLASS ");
            sql.append("        AND CLASS_AVG.COURSECD      = '0' ");
            sql.append("        AND CLASS_AVG.MAJORCD       = '000' ");
            sql.append("        AND CLASS_AVG.COURSECODE    = '0000' ");
            sql.append(" WHERE");
            sql.append("  t1.year = '" + exam._year + "' AND");
            sql.append("  t1.semester = '" + exam._semester + "' AND");
            sql.append("  t1.testkindcd = '" + exam.getKindCd() + "' AND");
            sql.append("  t1.testitemcd = '" + exam.getItemCd() + "' AND");
            sql.append("  t1.score_div = '" + exam.getScoreDiv() + "' AND");
            sql.append("  t1.schregno = ?");


            PreparedStatement ps = null;

            try {
                ps = db2.prepareStatement(sql.toString());
                for (final Iterator it = students.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();
                    final Object[] qparam = new Object[] {
                            student._grade,
                            student._coursegroupCd,
                            student._courseCd + student._majorCd + student._courseCode,
                            student._schregno};
                    for (final Iterator rit = KnjDbUtils.query(db2, ps, qparam).iterator(); rit.hasNext();) {
                        final Map row = (Map) rit.next();
                        final String subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");
                        Subclass subClass = (Subclass) param._subclasses.get(subclasscd);
                        if (null == subClass) {
                            log.warn("対象成績データが科目マスタに無い!:" + subclasscd);
                            final String key;
                            final String[] split = StringUtils.split(subclasscd, "-");
                            key = split[0] + "-" + split[1];
                            final Class clazz = (Class) param._classes.get(key);
                            if (null == clazz) {
                                continue;
                            }
                            subClass = new Subclass(param, subclasscd, clazz._name, clazz._abbv);
                        }

                        final Integer score = KnjDbUtils.getInt(row, "SCORE", null);
                        Integer scorePercent = null;
                        final int perfect = KnjDbUtils.getInt(row, "PERFECT", new Integer(100)).intValue();
                        if (100 == perfect) {
                            scorePercent = score;
                        } else if (null != score) {
                            scorePercent = new Integer(new BigDecimal(KnjDbUtils.getString(row, "SCORE")).multiply(new BigDecimal(100)).divide(new BigDecimal(perfect), 0, BigDecimal.ROUND_HALF_UP).intValue());
                        }
                        final Integer rank;
                        BigDecimal deviation;
                        if ("1".equals(param._groupDiv)) {
                            rank = KnjDbUtils.getInt(row, "GRADE_RANK", null);
                            deviation = KnjDbUtils.getBigDecimal(row, "GRADE_DEVIATION", null);
                        } else if ("2".equals(param._groupDiv)) {
                            rank = KnjDbUtils.getInt(row, "COURSE_RANK", null);
                            deviation = KnjDbUtils.getBigDecimal(row, "COURSE_DEVIATION", null);
                        } else if ("3".equals(param._groupDiv)) {
                            rank = KnjDbUtils.getInt(row, "CLASS_RANK", null);
                            deviation = KnjDbUtils.getBigDecimal(row, "CLASS_DEVIATION", null);
                        }else {
                            rank = null;
                            deviation = null;
                        }
                        if (null == deviation && null != rank) {
                            final AverageDat averageDat = exam._averages.get(AverageDat.averageKey(param, student, subclasscd));
                            if (null != averageDat && null != averageDat._count && averageDat._count.intValue() == 1 && null != averageDat._stdDev && averageDat._stdDev.doubleValue() == 0.0) {
                                deviation = new BigDecimal("50.0");
                            }
                        }
                        final Integer getClassRank = KnjDbUtils.getInt(row, "CLASS_RANK", null);
                        final Integer getClassCount = KnjDbUtils.getInt(row, "CLASS_COUNT", null);
                        final Integer getCourseRank = KnjDbUtils.getInt(row, "COURSE_RANK", null);

                        final Record rec = new Record(param, KnjDbUtils.getString(row, "CLASSCD"), KnjDbUtils.getString(row, "CLASS_KEY"), subclasscd, score, scorePercent, null, rank, getClassRank, getClassCount, getCourseRank, deviation);
                        getMappedMap(student._examRecord, exam.getExamKey()).put(subclasscd, rec);
                    }
                }

            } catch (final SQLException e) {
                log.warn("成績データの取得でエラー", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }

            final StringBuffer sql1 = new StringBuffer();
            sql1.append("SELECT");
            sql1.append("  T1.SUBCLASSCD,");
            sql1.append("  T1.SCORE,");
            sql1.append("  T1.AVG,");
            sql1.append("  T1." + gradeRank + " as GRADE_RANK,");
            sql1.append("  T1.GRADE_DEVIATION,");
            sql1.append("  T1." + courseRank + " as COURSE_RANK,");
            sql1.append("  T1.COURSE_DEVIATION,");
            sql1.append("  T1." + majorRank + " as MAJOR_RANK,");
            sql1.append("  T1.MAJOR_DEVIATION, ");
            sql1.append("  T1." + classRank + " as CLASS_RANK,");
            sql1.append("  CLASS_AVG.COUNT AS CLASS_COUNT,");
            sql1.append("  T1.CLASS_DEVIATION ");
            sql1.append(" FROM record_rank_sdiv_dat T1 ");
            sql1.append("  LEFT JOIN SCHREG_REGD_DAT REGD ");
            sql1.append("         ON REGD.YEAR     = T1.YEAR ");
            sql1.append("        AND REGD.SEMESTER = T1.SEMESTER ");
            sql1.append("        AND REGD.SCHREGNO = T1.SCHREGNO ");
            sql1.append("  LEFT JOIN RECORD_AVERAGE_SDIV_DAT CLASS_AVG ");
            sql1.append("         ON CLASS_AVG.YEAR          = T1.YEAR ");
            sql1.append("        AND CLASS_AVG.SEMESTER      = T1.SEMESTER ");
            sql1.append("        AND CLASS_AVG.TESTKINDCD    = T1.TESTKINDCD ");
            sql1.append("        AND CLASS_AVG.TESTITEMCD    = T1.TESTITEMCD ");
            sql1.append("        AND CLASS_AVG.SCORE_DIV     = T1.SCORE_DIV ");
            sql1.append("        AND CLASS_AVG.CLASSCD       = T1.CLASSCD ");
            sql1.append("        AND CLASS_AVG.SCHOOL_KIND   = T1.SCHOOL_KIND ");
            sql1.append("        AND CLASS_AVG.CURRICULUM_CD = T1.CURRICULUM_CD ");
            sql1.append("        AND CLASS_AVG.SUBCLASSCD    = T1.SUBCLASSCD ");
            sql1.append("        AND CLASS_AVG.AVG_DIV       = '2' ");
            sql1.append("        AND CLASS_AVG.GRADE         = REGD.GRADE ");
            sql1.append("        AND CLASS_AVG.HR_CLASS      = REGD.HR_CLASS ");
            sql1.append("        AND CLASS_AVG.COURSECD      = '0' ");
            sql1.append("        AND CLASS_AVG.MAJORCD       = '000' ");
            sql1.append("        AND CLASS_AVG.COURSECODE    = '0000' ");
            sql1.append(" WHERE");
            sql1.append("  T1.year = '" + exam._year + "' AND");
            sql1.append("  T1.semester = '" + exam._semester + "' AND");
            sql1.append("  T1.testkindcd = '" + exam.getKindCd() + "' AND");
            sql1.append("  T1.testitemcd = '" + exam.getItemCd() + "' AND");
            sql1.append("  T1.score_div = '" + exam.getScoreDiv() + "' AND");
            sql1.append("  T1.subclasscd IN ('" + ALL3 + "', '" + ALL5 + "', '" + ALL9 + "') AND");
            sql1.append("  T1.schregno= ?");
            try {
                ps = db2.prepareStatement(sql1.toString());
                for (final Iterator it = students.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    for (final Iterator rit = KnjDbUtils.query(db2, ps, new Object[] {student._schregno}).iterator(); rit.hasNext();) {
                        final Map row = (Map) rit.next();

                        final String subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");
                        final Integer score = KnjDbUtils.getInt(row, "SCORE", null);
                        final BigDecimal avg = KnjDbUtils.getBigDecimal(row, "AVG", null);
                        final Integer rank;
                        final BigDecimal deviation;
                        if ("1".equals(param._groupDiv)) {
                            rank = KnjDbUtils.getInt(row, "GRADE_RANK", null);
                            deviation = KnjDbUtils.getBigDecimal(row, "GRADE_DEVIATION", null);
                        } else if ("2".equals(param._groupDiv)) {
                            rank = KnjDbUtils.getInt(row, "COURSE_RANK", null);
                            deviation = KnjDbUtils.getBigDecimal(row, "COURSE_DEVIATION", null);
                        } else if ("3".equals(param._groupDiv)) {
                            rank = KnjDbUtils.getInt(row, "CLASS_RANK", null);
                            deviation = KnjDbUtils.getBigDecimal(row, "CLASS_DEVIATION", null);
                        } else {
                            rank = null;
                            deviation = null;
                        }

                        final Integer getClassRank = KnjDbUtils.getInt(row, "CLASS_RANK", null);
                        final Integer getClassCount = KnjDbUtils.getInt(row, "CLASS_COUNT", null);
                        final Integer getCourseRank = KnjDbUtils.getInt(row, "COURSE_RANK", null);
                        final Record rec = new Record(param, "99", "99", subclasscd, score, null, avg, rank, getClassRank, getClassCount, getCourseRank, deviation);
                        getMappedMap(student._examRecordOther, exam.getExamKey()).put(subclasscd, rec);
                    }
                }

            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(ps);
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

    private static class SubclassComparator implements Comparator {
        final Param _param;
        SubclassComparator(final Param param) {
            _param = param;
        }
        public int compare(final Object o1, final Object o2) {
            final Subclass s1 = (Subclass) o1;
            final Subclass s2 = (Subclass) o2;
            final int d105Order1 = _param._nameMstD105Name1List.indexOf(s1._code);
            final int d105Order2 = _param._nameMstD105Name1List.indexOf(s2._code);
            if (d105Order1 != d105Order2) {
                if (d105Order1 == -1) {
                    return 1;
                } else if (d105Order2 == -1) {
                    return -1;
                }
                return d105Order1 - d105Order2;
            }
            return s1._code.compareTo(s2._code);
        }
    }

    /**
     * 科目。
     */
    private static class Subclass implements Comparable {
        final Param _param;
        final String _code;
        final String _name;
        final String _abbv;

        /** 合併情報を持っているか */
        private boolean _hasCombined;
        /** 合併先か? */
        private boolean _isSaki;
        /** 合併元か? */
        private boolean _isMoto;

        public Subclass(final Param param, final String code, final String name, final String abbv) {
            _param = param;
            _code = code;
            _name = name;
            _abbv = abbv;
        }

        public Subclass(final Param param, final String code) {
            this(param, code, "", "");
        }

        public String getClassKey(final Param param) {
            final String classCd;
            final String[] split = StringUtils.split(_code, "-");
            classCd =  split[0] + "-" + split[1];
            return classCd;
        }

        public String getClassCd(final Param param) {
            final String classCd;
            final String[] split = StringUtils.split(_code, "-");
            classCd =  split[0];
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

        public int compareTo(final Object o) {
            if (!(o instanceof Subclass)) {
                return -1;
            }
            final Subclass that = (Subclass) o;
            return _param._subclassComparator.compare(this, that);
        }

        public String toString() {
            return _code + ":" + _abbv;
        }
    }

    private static class Record implements Comparable {
        final Param _param;
        final String _classcd;
        final String _classkey;
        final String _subclasscd;
        final Integer _score;
        final Integer _scorePercent;
        final BigDecimal _avg;
        final Integer _rank;
        final Integer _classRank;
        final Integer _classRankCount;
        final Integer _courseRank;
        final BigDecimal _deviation;

        private Record(
                final Param param,
                final String classcd,
                final String classkey,
                final String subclasscd,
                final Integer score,
                final Integer scorePercent,
                final BigDecimal avg,
                final Integer rank,
                final Integer classRank,
                final Integer classRankCount,
                final Integer courseRank,
                final BigDecimal deviation
        ) {
            _param = param;
            _classcd = classcd;
            _classkey = classkey;
            _subclasscd = subclasscd;
            _score = score;
            _scorePercent = scorePercent;
            _avg = avg;
            _rank = rank;
            _classRank = classRank;
            _classRankCount = classRankCount;
            _courseRank = courseRank;
            _deviation = deviation;
        }

        public int compareTo(final Object o) {
            if (!(o instanceof Record)) {
                return -1;
            }
            Record that = (Record) o;
            final int d105Order1 = _param._nameMstD105Name1List.indexOf(_subclasscd);
            final int d105Order2 = _param._nameMstD105Name1List.indexOf(that._subclasscd);
            if (d105Order1 != d105Order2) {
                if (d105Order1 == -1) {
                    return 1;
                } else if (d105Order2 == -1) {
                    return -1;
                }
                return d105Order1 - d105Order2;
            }
            return _subclasscd.compareTo(that._subclasscd);
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
        final Integer _highScore;
        final Integer _count;
        final Integer _courseCount;
        final String _coursecd;
        final String _majorcd;
        final String _coursecode;

        private AverageDat(
                final String subclassCd,
                final BigDecimal avg,
                final BigDecimal avgPercent,
                final BigDecimal stdDev,
                final Integer highScore,
                final Integer count,
                final Integer courseCount,
                final String coursecd,
                final String majorcd,
                final String coursecode
        ) {
            _subclassCd = subclassCd;
            _avg = avg;
            _avgPercent = avgPercent;
            _stdDev = stdDev;
            _highScore = highScore;
            _count = count;
            _courseCount = courseCount;
            _coursecd = coursecd;
            _majorcd = majorcd;
            _coursecode = coursecode;
        }

        private static String averageKey(final Param param, final String subclasscd, final String coursecd, final String majorcd, final String coursecode, final String hrClass) {
            final String key;
            if ("1".equals(param._groupDiv)) {
                key = subclasscd;
            } else if ("2".equals(param._groupDiv)) {
                key = subclasscd + coursecd + majorcd + coursecode;
            } else if ("3".equals(param._groupDiv)) {
                key = subclasscd + hrClass;
            }else {
                key = "";
            }
            return key;
        }

        private static String averageKey(final Param param, final Student student, final String subclasscd) {
            final String key;
            if ("1".equals(param._groupDiv)) {
                key = subclasscd;
            } else if ("2".equals(param._groupDiv)) {
                key = subclasscd + student.courseKey();
            } else if ("3".equals(param._groupDiv)) {
                key = subclasscd + student._hrClass;
            }else {
                key = "";
            }
            return key;
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

//    /** 「100点に換算する」場合に表示するデータ */
//    private static class ConvertedScore {
//    }

    private static class SubclassMst implements Comparable<SubclassMst> {
        final String _specialDiv;
        final String _classcd;
        final String _subclasscd;
        final String _classabbv;
        final String _classname;
        final String _subclassname;
        final Integer _classShoworder3;
        final Integer _subclassShoworder3;
        final String _calculateCreditFlg;
        SubclassMst _combined = null;
        List<SubclassMst> _attendSubclassList = new ArrayList();
        public SubclassMst(final String specialDiv, final String classcd, final String subclasscd, final String classabbv, final String classname, final String subclassabbv, final String subclassname,
                final Integer classShoworder3,
                final Integer subclassShoworder3,
                final String calculateCreditFlg) {
            _specialDiv = specialDiv;
            _classcd = classcd;
            _subclasscd = subclasscd;
            _classabbv = classabbv;
            _classname = classname;
            _subclassname = subclassname;
            _classShoworder3 = classShoworder3;
            _subclassShoworder3 = subclassShoworder3;
            _calculateCreditFlg = calculateCreditFlg;
        }
        public boolean isMoto() {
            return null != _combined;
        }
        public boolean isSaki() {
            return !_attendSubclassList.isEmpty();
        }
        public int compareTo(final SubclassMst mst) {
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
            return "SubclassMst(subclasscd = " + _subclasscd + ")";
        }
    }

    private static class DateRange {
        final String _key;
        final String _name;
        final String _sdate;
        final String _edate;
        public DateRange(final String key, final String name, final String sdate, final String edate) {
            _key = key;
            _name = name;
            _sdate = sdate;
            _edate = edate;
        }
        public String toString() {
            return "DateRange(" + _key + ", " + _sdate + ", " + _edate + ")";
        }
    }

    private static class Semester {
        final String _semester;
        final String _semestername;
        final DateRange _dateRange;
        final List _testItemList;
        final List _semesterDetailList;
        public Semester(final String semester, final String semestername, final String sdate, final String edate) {
            _semester = semester;
            _semestername = semestername;
            _dateRange = new DateRange(_semester, _semestername, sdate, edate);
            _testItemList = new ArrayList();
            _semesterDetailList = new ArrayList();
        }
        public int getTestItemIdx(final TestItem testItem) {
            return _testItemList.indexOf(testItem);
        }
        public int getSemesterDetailIdx(final SemesterDetail semesterDetail) {
//          log.debug(" semesterDetail = " + semesterDetail + " , " + _semesterDetailList.indexOf(semesterDetail));
          return _semesterDetailList.indexOf(semesterDetail);
        }

        public int compareTo(final Object o) {
            if (!(o instanceof Semester)) {
                return 0;
            }
            Semester s = (Semester) o;
            return _semester.compareTo(s._semester);
        }
    }

    private static class SemesterDetail implements Comparable<SemesterDetail> {
        final Semester _semester;
        final String _cdSemesterDetail;
        final String _semestername;
        final String _sdate;
        final String _edate;
        public SemesterDetail(Semester semester, String cdSemesterDetail, String semestername, final String sdate, final String edate) {
            _semester = semester;
            _cdSemesterDetail = cdSemesterDetail;
            _semestername = StringUtils.defaultString(semestername);
            _sdate = sdate;
            _edate = edate;
        }
        public int compareTo(final SemesterDetail sd) {
            int rtn;
            rtn = _semester.compareTo(sd._semester);
            if (rtn != 0) {
                return rtn;
            }
            rtn = _cdSemesterDetail.compareTo(sd._cdSemesterDetail);
            return rtn;
        }
        public String toString() {
            return "SemesterDetail(" + _semester._semester + ", " + _cdSemesterDetail + ", " + _semestername + ")";
        }
    }

    private static class TestItem {
        final String _year;
        final Semester _semester;
        final String _testkindcd;
        final String _testitemcd;
        final String _scoreDiv;
        final String _testitemname;
        final String _testitemabbv1;
        final String _sidouInput;
        final String _sidouInputInf;
        final SemesterDetail _semesterDetail;
        boolean _isGakunenKariHyotei;
        int _printKettenFlg; // -1: 表示しない（仮評定）、1: 値が1をカウント（学年評定）、2:換算した値が1をカウント（評価等）
        public TestItem(final String year, final Semester semester, final String testkindcd, final String testitemcd, final String scoreDiv,
                final String testitemname, final String testitemabbv1, final String sidouInput, final String sidouInputInf,
                final SemesterDetail semesterDetail) {
            _year = year;
            _semester = semester;
            _testkindcd = testkindcd;
            _testitemcd = testitemcd;
            _scoreDiv = scoreDiv;
            _testitemname = testitemname;
            _testitemabbv1 = testitemabbv1;
            _sidouInput = sidouInput;
            _sidouInputInf = sidouInputInf;
            _semesterDetail = semesterDetail;
        }
        public String getTestcd() {
            return _semester._semester +_testkindcd +_testitemcd + _scoreDiv;
        }
        public String toString() {
            return "TestItem(" + _semester._semester + _testkindcd + _testitemcd + "(" + _scoreDiv + "))";
        }
    }

    private static class SubclassAttendance {
        final BigDecimal _lesson;
        final BigDecimal _attend;
        final BigDecimal _sick;
        final BigDecimal _late;
        final BigDecimal _early;
        boolean _isOver;

        public SubclassAttendance(final BigDecimal lesson, final BigDecimal attend, final BigDecimal sick, final BigDecimal late, final BigDecimal early) {
            _lesson = lesson;
            _attend = attend;
            _sick = sick;
            _late = late;
            _early = early;
        }

        public String toString() {
            return "SubclassAttendance(" + _sick == null ? null : sishaGonyu(_sick.toString())  + "/" + _lesson + ")";
        }

        private static void load(final DB2UDB db2,
                final Param param,
                final List studentList) {
            if (null == param._dateFrom || null == param._dateTo || param._dateFrom.compareTo(param._dateTo) > 0) {
                return;
            }

            for (final Iterator it2 = param._attendSemesterDetailList.iterator(); it2.hasNext();) {
                final SemesterDetail semesDetail = (SemesterDetail) it2.next();
                if (null == semesDetail) {
                    continue;
                }

                PreparedStatement ps = null;
                ResultSet rs = null;
                try {
                    param._attendParamMap.put("schregno", "?");

                    final String sql = AttendAccumulate.getAttendSubclassSql(
                            param._year,
                            param._semester,
                            param._dateFrom,
                            param._dateTo,
                            param._attendParamMap
                    );

                    ps = db2.prepareStatement(sql);

                    for (final Iterator it3 = studentList.iterator(); it3.hasNext();) {
                        final Student student = (Student) it3.next();

                        ps.setString(1, student._schregno);
                        rs = ps.executeQuery();

                        while (rs.next()) {
                            if (!SEMEALL.equals(rs.getString("SEMESTER"))) {
                                continue;
                            }
                            final String subclasscd = rs.getString("SUBCLASSCD");

                            final SubclassMst mst = (SubclassMst) param._subclassMstMap.get(subclasscd);
                            if (null == mst) {
                                log.warn("no subclass : " + subclasscd);
                                continue;
                            }
                            final int iclasscd = Integer.parseInt(subclasscd.substring(0, 2));
                            //if ((Integer.parseInt(KNJDefineSchool.subject_D) <= iclasscd && iclasscd <= Integer.parseInt(KNJDefineSchool.subject_U) || iclasscd == Integer.parseInt(KNJDefineSchool.subject_T))) {
                            if (Integer.parseInt(KNJDefineSchool.subject_D) <= iclasscd && rs.getBigDecimal("MLESSON").intValue() > 0) {

                                final BigDecimal lesson = rs.getBigDecimal("MLESSON");
                                final BigDecimal rawSick = rs.getBigDecimal("SICK1");
                                final BigDecimal sick = rs.getBigDecimal("SICK2");
                                final BigDecimal rawReplacedSick = rs.getBigDecimal("RAW_REPLACED_SICK");
                                final BigDecimal replacedSick = rs.getBigDecimal("REPLACED_SICK");
                                final BigDecimal late = rs.getBigDecimal("LATE");
                                final BigDecimal early = rs.getBigDecimal("EARLY");

                                final BigDecimal sick1 = mst.isSaki() ? rawReplacedSick : rawSick;
                                final BigDecimal attend = lesson.subtract(null == sick1 ? BigDecimal.valueOf(0) : sick1);
                                final BigDecimal sick2 = mst.isSaki() ? replacedSick : sick;

                                final BigDecimal absenceHigh = rs.getBigDecimal("ABSENCE_HIGH");

                                final SubclassAttendance subclassAttendance = new SubclassAttendance(lesson, attend, sick2, late, early);

                                //欠課時数上限
                                final Double absent = Double.valueOf(mst.isSaki() ? rs.getString("REPLACED_SICK"): rs.getString("SICK2"));
                                subclassAttendance._isOver = subclassAttendance.judgeOver(absent, absenceHigh);

                                Map setSubAttendMap = null;

                                if (student._attendSubClassMap.containsKey(subclasscd)) {
                                    setSubAttendMap = (Map) student._attendSubClassMap.get(subclasscd);
                                } else {
                                    setSubAttendMap = new TreeMap();
                                }

//                                setMap.put(semesDetail._cdSemesterDetail, subclassAttendance);

                                setSubAttendMap.put(param._semester, subclassAttendance);

                                student._attendSubClassMap.put(subclasscd, setSubAttendMap);
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
        }

        /**
         * 欠課時数超過ならTrueを戻します。
         * @param absent 欠課時数
         * @param absenceHigh 超過対象欠課時数（CREDIT_MST）
         * @return
         */
        private boolean judgeOver(final Double absent, final BigDecimal absenceHigh) {
            if (null == absent || null == absenceHigh) {
                return false;
            }
            if (0.1 > absent.floatValue() || 0.0 == absenceHigh.doubleValue()) {
                return false;
            }
            if (absenceHigh.doubleValue() < absent.doubleValue()) {
                return true;
            }
            return false;
        }
    }

    private static String sishaGonyu(final String val) {
        if (!NumberUtils.isNumber(val)) {
            return null;
        }
        return new BigDecimal(val).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
    }

   private static class Param {
        final String _year;
        final String _semester;
        final String _testCd;
        final String _grade;
        final String _groupDiv; // 1:学年 2:コース 3:クラス

        final String _categoryIsClass; // [クラス指定 or 生徒指定]の値。
        final String[] _categorySelected; // クラス or 生徒。

        final String _loginDate;

        final String _nendo;
        final String _semesterName;
        private String _schoolName; // 学校名
        private String _remark2;    // 担任職種名
        final String _schoolKind;
        final String _schoolKindName;
        final String _deviationPrint; // 偏差値を印字するか?
        final String _outputKijun; // 基準点
        final String _dateFrom;
        final String _dateTo;
        final String _cmd;

        final boolean _semes2Flg;
        final boolean _semes3Flg;
        final boolean _semes9Flg;

        private final Map _attendParamMap;

        private final List _semesterList;
        private Map _semesterMap;
        private final Map _semesterDetailMap;
        private List _d026List = new ArrayList();
        private Map _attendRanges;
        private Map<String, SubclassMst> _subclassMstMap;
        private String _currentform;
        private Map<String, Map<String, SvfField>> _formnameFormFieldMap = new HashMap<String, Map<String, SvfField>>();
        private final List _attendSemesterDetailList;
        private final List _attendTestKindItemList;

        final Map<String, String> _nameMstD070nameSpare1;       // 考査種別と合算科目の母集団から除くフラグのマップ 1:合併元除く 2:合併先除く
        final boolean _MajorPrintFlg;             // 同一校種内で、学科コードが一つの時は、学科名は印字しない
        final List _nameMstD105Name1List;

        final Map<String, Map<String, Map<String, String>>> _distributionCacheMap = new HashMap<String, Map<String, Map<String, String>>>();
        final Map _staffs = new HashMap();

        /** 教科マスタ。 */
        final Map _classes;
        final String _imagePath;

        final String _useCurriculumcd;
        final String _useClassDetailDat;

        final List _classOrder;

        /** 中学か? false なら高校. */
        final boolean _isJunior;

        /** 欠点 */
        final int _borderScore;
        /** 科目マスタ。 */
        final Map _subclasses;

        final SubclassComparator _subclassComparator;
//        /** 100点満点に換算する */
//        private final boolean _isConvertScoreTo100;

        final Exam _thisExam;
        private final Map<String, Exam> _beforeExamMap;

        /** グラフイメージファイルの Set&lt;File&gt; */
        final Set _graphFiles = new HashSet();

        final String _barLegendImage;
        final String _radarLegendImage;

        final boolean _isOutptuDebug;

        final String _whiteSpaceImagePath;

        public Param(final HttpServletRequest request, final DB2UDB db2) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _testCd = request.getParameter("TESTCD");
            _categoryIsClass = request.getParameter("CATEGORY_IS_CLASS");
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _loginDate = request.getParameter("LOGIN_DATE");
            _grade = request.getParameter("GRADE");
//            _groupDiv =  request.getParameter("GROUP_DIV");// 1=学年, 2=コース
            _groupDiv =  "2"; //コース
//            _deviationPrint = request.getParameter("DEVIATION_PRINT");
            _deviationPrint = "1"; //偏差値を出力する
            _cmd = request.getParameter("cmd");

            _nameMstD070nameSpare1 = getNameMstD070nameSpare1(db2);
            _MajorPrintFlg         = getMajorPrintFlg(db2, _year, _semester, getSchoolKind(db2));

//            _nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_year)) + "(" + _year + ")年度";
            _nendo = _year + "年度";
            _semesterName = getSemesterMst(db2, "SEMESTERNAME", _semester);
            _imagePath = request.getParameter("IMAGE_PATH");
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _useClassDetailDat = request.getParameter("useClassDetailDat");
//            _outputKijun = request.getParameter("OUTPUT_KIJUN");
            _outputKijun = "1"; //順位の基準点：総合点
            log.debug(" record_rank_sdiv_dat outputKijun ? = " + _outputKijun);

//            _borderScore = StringUtils.isBlank(request.getParameter("KETTEN")) ? 0 : Integer.parseInt(request.getParameter("KETTEN"));
            _borderScore = 0;

            _dateFrom = request.getParameter("DATE_FROM").replace('/', '-');
            _dateTo = request.getParameter("DATE_TO").replace('/', '-');

            _semes2Flg = "2".equals(_semester) || "3".equals(_semester) || "9".equals(_semester) ? true : false;
            _semes3Flg = "3".equals(_semester) || "9".equals(_semester) ? true : false;
            _semes9Flg = "9".equals(_semester) ? true : false;
            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
            _attendParamMap.put("grade", _grade);
            _attendParamMap.put("useTestCountflg", "TESTITEM_MST_COUNTFLG_NEW_SDIV");
            _attendParamMap.put("useCurriculumcd", "1");

            _semesterList = getSemesterList(db2);
            _semesterMap = loadSemester(db2);
            _semesterDetailMap = new HashMap();
            _attendRanges = new HashMap();
            for (final Iterator it = _semesterMap.keySet().iterator(); it.hasNext();) {
                final String semester = (String) it.next();
                final Semester oSemester = (Semester) _semesterMap.get(semester);
                _attendRanges.put(semester, oSemester._dateRange);
            }
            setSubclassMst(db2);
            _attendTestKindItemList = getTestKindItemList(db2, false, false);
            _attendSemesterDetailList = getAttendSemesterDetailList();

            _thisExam = new Exam(_year, _semester, _testCd);
            _thisExam._grade = _grade;

            _isJunior = "J".equals(getSchoolKind(db2));
            loadCertifSchool(db2);
            _schoolKind = getSchoolKind(db2);
            _schoolKindName = getSchoolKindName(db2);
            loadRegdHdat(db2);
            _thisExam.load(this, db2);
            log.info(" this Exam testcd = " + _thisExam.getExamKey());

            //これまでの成績のデータを読む
            _beforeExamMap = getBeforTestMap(db2);

            _classes = setClasses(db2);
            _subclasses = setSubClasses(db2);
            _subclassComparator = new SubclassComparator(this);
            setCombinedOnSubClass(db2);
            _nameMstD105Name1List = KnjDbUtils.getColumnDataList(KnjDbUtils.query(db2, "SELECT NAME1 FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'D105' AND NAME1 IS NOT NULL ORDER BY INT(VALUE(NAME2, '9999')), NAMECD2 "), "NAME1");

            _classOrder = loadClassOrder(db2);
            log.info(" class order = " + _classOrder);

            String barLegendImage;
            if ("1".equals(_groupDiv)) {
                /** 棒グラフ凡例画像1(学年). */
                barLegendImage = "BarChartLegendGrade.png";
            } else if ("2".equals(_groupDiv)) {
                /** 棒グラフ凡例画像2(コース). */
                barLegendImage = "BarChartLegendCourse.png";
            } else if ("3".equals(_groupDiv)) {
                /** 棒グラフ凡例画像3(クラス). */
                barLegendImage = "BarChartLegendClass.png";
            } else {
                barLegendImage = "";
            }
            _barLegendImage = checkImageFile(barLegendImage);
            /** レーダーチャート凡例画像. */
            _radarLegendImage = checkImageFile("RaderChartLegend.png");

            _isOutptuDebug = "1".equals(getDbPrginfoProperties(db2, "outputDebug"));
            _whiteSpaceImagePath = checkImageFile("whitespace.png");

        }

        private static String getDbPrginfoProperties(final DB2UDB db2, final String propName) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJD154I' AND NAME = '" + propName + "' "));
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
            } else if ("2".equals(_groupDiv)) {
                msg = "コース";
            } else if ("3".equals(_groupDiv)) {
                msg = "クラス";
            } else {
                msg = "";
            }
            return msg;
        }

        private void setCombinedOnSubClass(final DB2UDB db2) throws SQLException {
            final Set sakiSet = new HashSet();
            final Set motoSet = new HashSet();

            String sql;
            sql = "select distinct";
            sql +="  COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || ";
            sql +="  COMBINED_SUBCLASSCD AS COMBINED_SUBCLASSCD,";
            sql +="  ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ";
            sql +="  ATTEND_SUBCLASSCD AS ATTEND_SUBCLASSCD"
                + " from SUBCLASS_REPLACE_COMBINED_DAT"
                + " where"
                + "  YEAR = '" + _year + "'"
                ;

            for (final Iterator it = KnjDbUtils.query(db2, sql).iterator(); it.hasNext();) {
                final Map row = (Map) it.next();
                final String combined = KnjDbUtils.getString(row, "COMBINED_SUBCLASSCD");
                final String attend = KnjDbUtils.getString(row, "ATTEND_SUBCLASSCD");
                sakiSet.add(combined);
                motoSet.add(attend);
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
            String sql = "select";
            sql +="   CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS SUBCLASSCD,";
            sql +="   coalesce(SUBCLASSORDERNAME2, SUBCLASSNAME) as NAME,"
                + "   SUBCLASSABBV"
                + " from V_SUBCLASS_MST"
                + " where"
                + "   YEAR = '" + _year + "'";

            for (final Iterator it = KnjDbUtils.query(db2, sql).iterator(); it.hasNext();) {
                final Map row = (Map) it.next();
                final String code = KnjDbUtils.getString(row, "SUBCLASSCD");
                rtn.put(code, new Subclass(this, code, KnjDbUtils.getString(row, "NAME"), KnjDbUtils.getString(row, "SUBCLASSABBV")));
            }
            //log.debug("科目マスタ総数=" + rtn.size());
            return rtn;
        }

        private Map setClasses(final DB2UDB db2) throws SQLException {
            final Map rtn = new HashMap();
            String sql = "";
            sql += "select";
            sql +="   CLASSCD || '-' || SCHOOL_KIND AS CLASSCD, ";
            sql +="   CLASSNAME,"
                + "   CLASSABBV"
                + " from V_CLASS_MST"
                + " where"
                + "   YEAR = '" + _year + "'";

            for (final Iterator it = KnjDbUtils.query(db2, sql).iterator(); it.hasNext();) {
                final Map row = (Map) it.next();
                final String code = KnjDbUtils.getString(row, "CLASSCD");
                rtn.put(code, new Class(code, KnjDbUtils.getString(row, "CLASSNAME"), KnjDbUtils.getString(row, "CLASSABBV")));
            }
            //log.debug("教科マスタ=" + rtn);
            return rtn;
        }

        private Map<String, String> getNameMstD070nameSpare1(final DB2UDB db2) {
            return KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, "SELECT NAME1, NAMESPARE1 FROM NAME_MST WHERE NAMECD1 = 'D070' "), "NAME1", "NAMESPARE1");
        }

        private boolean getMajorPrintFlg(final DB2UDB db2, final String year, final String semester, final String schoolKind) {
            boolean retFlg = false;
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     REGD.MAJORCD ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT REGD ");
            stb.append("     LEFT JOIN SCHREG_REGD_GDAT GDAT ON REGD.YEAR  = GDAT.YEAR ");
            stb.append("                                    AND REGD.GRADE = GDAT.GRADE ");
            stb.append(" WHERE ");
            stb.append("         REGD.YEAR        = '" + year + "' ");
            stb.append("     AND REGD.SEMESTER    = '" + semester + "' ");
            stb.append("     AND GDAT.SCHOOL_KIND = '" + schoolKind + "'");
            stb.append(" GROUP BY ");
            stb.append("     REGD.MAJORCD ");

            final int majorCnt = KnjDbUtils.query(db2, stb.toString()).size();
            if (majorCnt > 1) retFlg = true;

            return retFlg;
        }

        private void loadRegdHdat(final DB2UDB db2) {
            final String sql;
            sql = "SELECT t1.grade ||  t1.hr_class AS CODE, t2.STAFFNAME"
                + " FROM schreg_regd_hdat t1 INNER JOIN v_staff_mst t2 ON t1.year = t2.year AND t1.tr_cd1 = t2.staffcd"
                + " WHERE t1.year = '" + _year + "'"
                + " AND t1.semester = '" + _semester + "'";
            _staffs.putAll(KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, sql), "CODE", "STAFFNAME"));
        }

        private void loadCertifSchool(final DB2UDB db2) throws SQLException {
            final String key = _isJunior ? "110" : "109";

            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, "SELECT SCHOOL_NAME, REMARK2 FROM certif_school_dat WHERE year = '" + _year + "' AND certif_kindcd = '" + key + "'"));
            _schoolName = KnjDbUtils.getString(row, "SCHOOL_NAME");
            _remark2 = KnjDbUtils.getString(row, "REMARK2");
        }

        private List loadClassOrder(final DB2UDB db2) {
            final String sql;
            if ("1".equals(_useClassDetailDat)) {
                sql = "SELECT classcd || '-' || school_kind AS CLASSCD FROM class_detail_dat"
                    + " WHERE year = '" + _year + "' AND class_seq = '004' "
                    + " ORDER BY int(value(class_remark1, '99')), classcd || '-' || school_kind "
                    ;
            } else {
                final String field1 = _isJunior ? "name1" : "name2";
                final String field2 = _isJunior ? "namespare1" : "namespare2";
                sql = "SELECT " + field1 + " AS CLASSCD FROM v_name_mst"
                    + " WHERE year = '" + _year + "' AND namecd1 = 'D009' AND " + field1 + " IS NOT NULL "
                    + " ORDER BY int(value(" + field2 + ", '99'))"
                    ;
            }

            final List classOrder = KnjDbUtils.getColumnDataList(KnjDbUtils.query(db2, sql), "CLASSCD");
            return classOrder;
            //log.debug("教科表示順 = " + _classOrder);
        }

        private boolean isUnderScore(final String score) {
            return NumberUtils.isDigits(score) && Integer.parseInt(score) < _borderScore;
        }

        private String getSchoolKind(final DB2UDB db2) {
            final StringBuffer stb = new StringBuffer();
            stb.append("SELECT  ");
            stb.append("        SCHOOL_KIND ");
            stb.append("FROM    SCHREG_REGD_GDAT T1 ");
            stb.append("WHERE   T1.YEAR = '" + _year + "' ");
            stb.append(    "AND T1.GRADE = '" + _grade + "' ");
            String schoolKind = KnjDbUtils.getOne(KnjDbUtils.query(db2, stb.toString()));
            return schoolKind;
        }

        private String getSchoolKindName(final DB2UDB db2) {
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT ABBV1 FROM NAME_MST WHERE NAMECD1 = 'A023' AND NAME1 = '" + _schoolKind + "' ");
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, sql.toString()));
        }

        private String getSemesterMst(final DB2UDB db2, final String field, final String semester) {
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT " + field + " FROM SEMESTER_MST WHERE YEAR = '" + _year + "' AND SEMESTER = '" + semester + "' ");
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = StringUtils.defaultString(rs.getString(field));
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return rtn;
        }

        private List getSemesterList(DB2UDB db2) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = " SELECT * FROM SEMESTER_MST WHERE YEAR = '" + _year + "' ORDER BY SEMESTER ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String semester = rs.getString("SEMESTER");
                    final String semestername = rs.getString("SEMESTERNAME");
                    final String sdate = rs.getString("SDATE");
                    final String edate = rs.getString("EDATE");
                    Semester semes = new Semester(semester, semestername, sdate, edate);
                    list.add(semes);
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }

        /**
         * 年度の開始日を取得する
         */
        private Map loadSemester(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map map = new TreeMap();
            try {
                final String sql = "select"
                        + "   SEMESTER,"
                        + "   SEMESTERNAME,"
                        + "   SDATE,"
                        + "   EDATE"
                        + " from"
                        + "   V_SEMESTER_GRADE_MST"
                        + " where"
                        + "   YEAR='" + _year + "'"
                        + "   AND GRADE='" + _grade + "'"
                        + " order by SEMESTER"
                    ;

                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    map.put(rs.getString("SEMESTER"), new Semester(rs.getString("SEMESTER"), rs.getString("SEMESTERNAME"), rs.getString("SDATE"), rs.getString("EDATE")));
                }
            } catch (final Exception ex) {
                log.error("テスト項目のロードでエラー", ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return map;
        }


        private void setSubclassMst(
                final DB2UDB db2
        ) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            _subclassMstMap = new HashMap();
            try {
                String sql = "";
                sql += " SELECT ";
                sql += " VALUE(T2.SPECIALDIV, '0') AS SPECIALDIV, ";
                sql += " T1.CLASSCD, ";
                sql += " T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ";
                sql += " T2.CLASSABBV, VALUE(T2.CLASSORDERNAME2, T2.CLASSNAME) AS CLASSNAME, T1.SUBCLASSABBV, VALUE(T1.SUBCLASSORDERNAME2, T1.SUBCLASSNAME) AS SUBCLASSNAME, ";
                sql += " COMB1.CALCULATE_CREDIT_FLG, ";
                sql += " VALUE(T2.SHOWORDER3, 999) AS CLASS_SHOWORDER3, ";
                sql += " VALUE(T1.SHOWORDER3, 999) AS SUBCLASS_SHOWORDER3, ";
                sql += " ATT1.COMBINED_CLASSCD || '-' || ATT1.COMBINED_SCHOOL_KIND || '-' || ATT1.COMBINED_CURRICULUM_CD || '-' || ATT1.COMBINED_SUBCLASSCD AS COMBINED_SUBCLASSCD, ";
                sql += " COMB1.ATTEND_CLASSCD || '-' || COMB1.ATTEND_SCHOOL_KIND || '-' || COMB1.ATTEND_CURRICULUM_CD || '-' || COMB1.ATTEND_SUBCLASSCD AS ATTEND_SUBCLASSCD ";
                sql += " FROM SUBCLASS_MST T1 ";
                sql += " LEFT JOIN CLASS_MST T2 ON T2.CLASSCD = T1.CLASSCD AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
                sql += " LEFT JOIN SUBCLASS_REPLACE_COMBINED_DAT ATT1 ON ATT1.YEAR = '" + _year + "' AND ATT1.ATTEND_CLASSCD = T1.CLASSCD AND ATT1.ATTEND_SCHOOL_KIND = T1.SCHOOL_KIND AND ATT1.ATTEND_CURRICULUM_CD = T1.CURRICULUM_CD AND ATT1.ATTEND_SUBCLASSCD = T1.SUBCLASSCD ";
                sql += " LEFT JOIN SUBCLASS_REPLACE_COMBINED_DAT COMB1 ON COMB1.YEAR = '" + _year + "' AND COMB1.COMBINED_CLASSCD = T1.CLASSCD AND COMB1.COMBINED_SCHOOL_KIND = T1.SCHOOL_KIND AND COMB1.COMBINED_CURRICULUM_CD = T1.CURRICULUM_CD AND COMB1.COMBINED_SUBCLASSCD = T1.SUBCLASSCD ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    if (!_subclassMstMap.containsKey(rs.getString("SUBCLASSCD"))) {
                        final SubclassMst mst = new SubclassMst(rs.getString("SPECIALDIV"), rs.getString("CLASSCD"), rs.getString("SUBCLASSCD"), rs.getString("CLASSABBV"), rs.getString("CLASSNAME"), rs.getString("SUBCLASSABBV"), rs.getString("SUBCLASSNAME"), new Integer(rs.getInt("CLASS_SHOWORDER3")), new Integer(rs.getInt("SUBCLASS_SHOWORDER3")), rs.getString("CALCULATE_CREDIT_FLG"));
                        _subclassMstMap.put(rs.getString("SUBCLASSCD"), mst);
                    }
                    final SubclassMst combined = _subclassMstMap.get(rs.getString("COMBINED_SUBCLASSCD"));
                    if (null != combined) {
                        final SubclassMst mst = _subclassMstMap.get(rs.getString("SUBCLASSCD"));
                        mst._combined = combined;
                    }
                    final SubclassMst attend = _subclassMstMap.get(rs.getString("ATTEND_SUBCLASSCD"));
                    if (null != attend) {
                        final SubclassMst mst = _subclassMstMap.get(rs.getString("SUBCLASSCD"));
                        mst._attendSubclassList.add(attend);
                    }
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private List getTestKindItemList(DB2UDB db2, final boolean useSubclassControl, final boolean addSemester) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final Map semesterMap = new HashMap();
                for (final Iterator it = _semesterMap.keySet().iterator(); it.hasNext();) {
                    final String seme = (String) it.next();
                    if(!_semesterMap.containsKey(seme)) continue;
                    final Semester semester = (Semester) _semesterMap.get(seme);
                    semesterMap.put(semester._semester, semester);
                }
                final StringBuffer stb = new StringBuffer();
                stb.append(" WITH ADMIN_CONTROL_SDIV_SUBCLASSCD AS (");
                if (useSubclassControl) {
                    stb.append("   SELECT DISTINCT ");
                    stb.append("       T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD ");
                    stb.append("   FROM ADMIN_CONTROL_SDIV_DAT T1 ");
                    stb.append("   WHERE T1.YEAR = '" + _year + "' ");
                    stb.append("   UNION ALL ");
                }
                stb.append("   SELECT DISTINCT ");
                stb.append("       T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD ");
                stb.append("   FROM ADMIN_CONTROL_SDIV_DAT T1 ");
                stb.append("   WHERE T1.YEAR = '" + _year + "' ");
                stb.append("     AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '00-00-00-000000' ");
                stb.append("   UNION ALL ");
                stb.append("   SELECT DISTINCT ");
                stb.append("       T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD ");
                stb.append("   FROM ADMIN_CONTROL_SDIV_DAT T1 ");
                stb.append("   WHERE T1.YEAR = '" + _year + "' ");
                stb.append("     AND T1.CLASSCD = '00' ");
                stb.append("     AND T1.CURRICULUM_CD = '00' ");
                stb.append("     AND T1.SUBCLASSCD = '000000' ");
                stb.append(" ) ");
                stb.append(" SELECT ");
                stb.append("     T1.YEAR, ");
                stb.append("     T1.SEMESTER, ");
                stb.append("     T1.TESTKINDCD, ");
                stb.append("     T1.TESTITEMCD, ");
                stb.append("     T1.SCORE_DIV, ");
                stb.append("     T1.TESTITEMNAME, ");
                stb.append("     T1.TESTITEMABBV1, ");
                stb.append("     T1.SIDOU_INPUT, ");
                stb.append("     T1.SIDOU_INPUT_INF, ");
                stb.append("     T11.CLASSCD || '-' || T11.SCHOOL_KIND || '-' || T11.CURRICULUM_CD || '-' || T11.SUBCLASSCD AS SUBCLASSCD, ");
                stb.append("     T2.SEMESTER_DETAIL, ");
                stb.append("     T2.SEMESTER AS SEMESTER_DETAIL_SEMESTER, ");
                stb.append("     T2.SEMESTERNAME AS SEMESTERDETAILNAME, ");
                stb.append("     T2.SDATE, ");
                stb.append("     T2.EDATE ");
                stb.append(" FROM TESTITEM_MST_COUNTFLG_NEW_SDIV T1 ");
                stb.append(" INNER JOIN ADMIN_CONTROL_SDIV_DAT T11 ON T11.YEAR = T1.YEAR ");
                stb.append("    AND T11.SEMESTER = T1.SEMESTER ");
                stb.append("    AND T11.TESTKINDCD = T1.TESTKINDCD ");
                stb.append("    AND T11.TESTITEMCD = T1.TESTITEMCD ");
                stb.append("    AND T11.SCORE_DIV = T1.SCORE_DIV ");
                stb.append(" LEFT JOIN SEMESTER_DETAIL_MST T2 ON T2.YEAR = T1.YEAR ");
                stb.append("    AND T2.SEMESTER = T1.SEMESTER ");
                stb.append("    AND T2.SEMESTER_DETAIL = T1.SEMESTER_DETAIL ");
                stb.append(" WHERE T1.YEAR = '" + _year + "' ");
                stb.append("   AND T11.CLASSCD || '-' || T11.SCHOOL_KIND || '-' || T11.CURRICULUM_CD || '-' || T11.SUBCLASSCD IN "); // 指定の科目が登録されていれば登録された科目、登録されていなければ00-00-00-000000を使用する
                stb.append("    (SELECT MAX(SUBCLASSCD) FROM ADMIN_CONTROL_SDIV_SUBCLASSCD) ");
                stb.append(" ORDER BY T1.SEMESTER, T1.TESTKINDCD, T1.TESTITEMCD, T1.SCORE_DIV ");

                log.debug(" testitem sql ="  + stb.toString());
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();

                String adminSdivSubclasscd = null;
                while (rs.next()) {
                    adminSdivSubclasscd = rs.getString("SUBCLASSCD");
                    final String year = rs.getString("YEAR");
                    final String testkindcd = rs.getString("TESTKINDCD");
                    final String testitemcd = rs.getString("TESTITEMCD");
                    final String scoreDiv = rs.getString("SCORE_DIV");
                    final String sidouInput = rs.getString("SIDOU_INPUT");
                    final String sidouInputInf = rs.getString("SIDOU_INPUT_INF");
                    Semester semester = (Semester) semesterMap.get(rs.getString("SEMESTER"));
                    if (null == semester) {
                        continue;
                    }
                    final String testitemname = rs.getString("TESTITEMNAME");
                    final String testitemabbv1 = rs.getString("TESTITEMABBV1");
                    SemesterDetail semesterDetail = null;
                    final String cdsemesterDetail = rs.getString("SEMESTER_DETAIL");
                    if (null != cdsemesterDetail) {
                        if (_semesterDetailMap.containsKey(cdsemesterDetail)) {
                            semesterDetail = (SemesterDetail) _semesterDetailMap.get(cdsemesterDetail);
                        } else {
                            final String semesterdetailname = rs.getString("SEMESTERDETAILNAME");
                            final String sdate = rs.getString("SDATE");
                            final String edate = rs.getString("EDATE");
                            semesterDetail = new SemesterDetail(semester, cdsemesterDetail, semesterdetailname, sdate, edate);
                            Semester semesDetailSemester = (Semester) semesterMap.get(rs.getString("SEMESTER_DETAIL_SEMESTER"));
                            if (null != semesDetailSemester) {
                                semesDetailSemester._semesterDetailList.add(semesterDetail);
                            }
                            _semesterDetailMap.put(semesterDetail._cdSemesterDetail, semesterDetail);
                        }
                    }
                    final TestItem testItem = new TestItem(
                            year, semester, testkindcd, testitemcd, scoreDiv, testitemname, testitemabbv1, sidouInput, sidouInputInf,
                            semesterDetail);
                    final boolean isGakunenHyotei = HYOTEI_TESTCD.equals(testItem.getTestcd());
                    if (isGakunenHyotei) {
                        testItem._printKettenFlg = 1;
                    } else if (!SEMEALL.equals(semester._semester) && "09".equals(scoreDiv)) { // 9学期以外の09=9学期以外の仮評定
                        testItem._printKettenFlg = -1;
                    } else {
                        testItem._printKettenFlg = 2;
                    }
                    if (isGakunenHyotei) {
                        final TestItem testItemKari = new TestItem(
                                year, semester, testkindcd, testitemcd, scoreDiv, "仮評定", "仮評定", sidouInput, sidouInputInf,
                                semesterDetail);
                        testItemKari._printKettenFlg = -1;
                        testItemKari._isGakunenKariHyotei = true;
                        if (addSemester) {
                            semester._testItemList.add(testItemKari);
                        }
                        list.add(testItemKari);
                    }
                    if (addSemester) {
                        semester._testItemList.add(testItem);
                    }
                    list.add(testItem);
                }
                log.debug(" testitem admin_control_sdiv_dat subclasscd = " + adminSdivSubclasscd);

            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            log.debug(" testcd = " + list);
            return list;
        }

        private List getAttendSemesterDetailList() {
            final Map rtn = new TreeMap();
            for (final Iterator it = _attendTestKindItemList.iterator(); it.hasNext();) {
                final TestItem item = (TestItem) it.next();
                if (null != item._semesterDetail && null != item._semesterDetail._cdSemesterDetail) {
                    rtn.put(item._semesterDetail._cdSemesterDetail, item._semesterDetail);
                }
            }
            Semester semester9 = null;
            for (final Iterator it = _semesterList.iterator(); it.hasNext();) {
                final Semester semester = (Semester) it.next();
                if (semester._semester.equals(SEMEALL)) {
                    semester9 = semester;
                    final SemesterDetail semesterDetail9 = new SemesterDetail(semester9, SEMEALL, "学年", semester9._dateRange._sdate, semester9._dateRange._edate);
                    semester9._semesterDetailList.add(semesterDetail9);
                    rtn.put(SEMEALL, semesterDetail9);
                    break;
                }
            }
            return new ArrayList(rtn.values());
        }

        private Map<String, Exam> getBeforTestMap (final DB2UDB db2) {

            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT DISTINCT  ");
            stb.append("   T1.* ");
            stb.append(" , VALUE(L2.SCORE_COUNT, L2.VALUE_DI_COUNT, 0) AS SCORE_COUNT ");
            stb.append(" FROM  ");
            stb.append("   TESTITEM_MST_COUNTFLG_NEW_SDIV T1 ");
            stb.append(" LEFT JOIN ( ");
            stb.append("     SELECT ");
            stb.append("         D1.YEAR ");
            stb.append("       , D1.SEMESTER ");
            stb.append("       , D1.TESTKINDCD ");
            stb.append("       , D1.TESTITEMCD ");
            stb.append("       , D1.SCORE_DIV ");
            stb.append("       , COUNT(D1.SCORE) AS SCORE_COUNT ");
            stb.append("       , COUNT(D1.VALUE_DI) AS VALUE_DI_COUNT ");
            stb.append("     FROM RECORD_SCORE_DAT D1 ");
            stb.append("     INNER JOIN SCHREG_REGD_DAT REGD ON REGD.SCHREGNO = D1.SCHREGNO ");
            stb.append("         AND REGD.YEAR = '" + _year + "' ");
            stb.append("         AND REGD.SEMESTER = '" + _semester + "' ");
            stb.append("         AND REGD.GRADE = '" + _grade + "' ");
            stb.append("     WHERE ");
            stb.append("         D1.YEAR = '" + _year + "' ");
            stb.append("     GROUP BY ");
            stb.append("         D1.YEAR ");
            stb.append("       , D1.SEMESTER ");
            stb.append("       , D1.TESTKINDCD ");
            stb.append("       , D1.TESTITEMCD ");
            stb.append("       , D1.SCORE_DIV ");
            stb.append("   ) L2 ON L2.YEAR = T1.YEAR ");
            stb.append("       AND L2.SEMESTER = T1.SEMESTER ");
            stb.append("       AND L2.TESTKINDCD = T1.TESTKINDCD ");
            stb.append("       AND L2.TESTITEMCD = T1.TESTITEMCD ");
            stb.append("       AND L2.SCORE_DIV = T1.SCORE_DIV ");
            stb.append(" WHERE  ");
            stb.append("       T1.YEAR = '"+ _year +"' ");
            stb.append("   AND T1.SEMESTER <= '" + _semester + "' ");
            stb.append("   AND T1.SCORE_DIV = '01' ");
            stb.append("   AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV < '" + _thisExam._semester + _thisExam._testCd + "' ");
            final String sql = stb.toString();

            Map<String, Exam> resultMap = new TreeMap();

            log.debug(sql);
            for (final Map<String, String> row : KnjDbUtils.query(db2, sql)) {
                final String semester = StringUtils.defaultString(KnjDbUtils.getString(row, "SEMESTER"));
                final String testkindcd = StringUtils.defaultString(KnjDbUtils.getString(row, "TESTKINDCD"));
                final String testitemcd = StringUtils.defaultString(KnjDbUtils.getString(row, "TESTITEMCD"));
                final String scoreDiv = StringUtils.defaultString(KnjDbUtils.getString(row, "SCORE_DIV"));
                final int scoreCount = KnjDbUtils.getInt(row, "SCORE_COUNT", 0);
                final String key = semester + testkindcd + testitemcd + scoreDiv;
                if (scoreCount == 0) {
                    log.info(" test no score : " + key);
                    continue;
                }

                final String testcd = testkindcd + testitemcd + scoreDiv;
                final Exam exam = new Exam(_year, semester, testcd);
                exam._grade = _grade;
                exam.load(this, db2);

                resultMap.put(key, exam);
            }
            return resultMap;
        }
    }

}

// eof
