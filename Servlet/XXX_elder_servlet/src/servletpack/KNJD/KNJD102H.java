// kanji=漢字
/*
 * $Id: f0b11c5df8893bb630136efb01022c90bc9347d1 $
 *
 * 作成日: 2006/03/16 11:37:00 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2006 ALP Co.,Ltd. All rights reserved.
 */

package servletpack.KNJD;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.collections.comparators.ReverseComparator;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisState;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPosition;
import org.jfree.chart.axis.CategoryLabelWidthType;
import org.jfree.chart.axis.CategoryTick;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.Tick;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.text.TextBlock;
import org.jfree.text.TextFragment;
import org.jfree.text.TextLine;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;

import servletpack.KNJD.detail.KNJ_Testname;
import servletpack.KNJZ.detail.KNJDefineCode;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_Get_Info;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

/**
 *  学校教育システム 賢者 [成績管理]  個人成績表（テスト）
 *
 *  2005/04/12 yamashiro  新規作成
 *                      ・総合成績について、単位数および欠時数は合計を、得点および講座平均は平均を小数点第二位を四捨五入して出力（暫定）
 *                      ・欠時数について、出欠データと集計データを組み合わせて算出（暫定）
 *  2005/06/11 yamashiro・集計カウントフラグ参照を追加
 *  2005/06/20 yamashiro・ペナルティ欠課の算出式を修正
 *  2005/10/31 yamashiro・出欠の累積情報の日付を出力
 *                      ・出欠の累積情報の締日において「指定学期まで」の条件を除外
 *  2006/02/01 yamasihro・2学期制の場合、1月〜3月の出欠集計データのソート順による集計の不具合を修正 --
 * @version $Id: f0b11c5df8893bb630136efb01022c90bc9347d1 $
 */
public class KNJD102H {

    private static final Log log = LogFactory.getLog(KNJD102H.class);
    
    /** グラフにプロットする数 */
    private static final int NUMPLOT = 15;

    private boolean _hasData;

    private static final DecimalFormat _dmf1 = new DecimalFormat("0");
    private static final DecimalFormat _dmf2 = new DecimalFormat("0.0");
    
    private static final String RANK_DIV_GRADE = "2";
    private static final String RANK_BASE_DEVIATION = "2";
    private static final String SUM_SUBCLASSCD = "999999X";
    
    /**
     * KNJD.classから最初に起動されるクラス
     * @param request HTTPリクエスト
     * @param response HTTPレスポンス
     * @throws ServletException 例外
     * @throws IOException 例外
     */
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws ServletException, IOException {
        final Vrw32alp svf = new Vrw32alp();
        PrintWriter outStrm = null;
        String printName = null;

        try {
            svf.VrInit(); // クラスの初期化

            printName = request.getParameter("PRINTNAME");
            if (printName != null) {
                response.setContentType("text/html");
                outStrm = new PrintWriter(response.getOutputStream());
                int ret = svf.VrSetPrinter("", printName); // プリンタ名の設定
                if (ret < 0) {
                    log.info("printname ret = " + ret);
                }
            } else {
                response.setContentType("application/pdf");
                svf.VrSetSpoolFileStream(response.getOutputStream()); // PDFファイル名の設定
            }
        } catch (final IOException e) {
            log.error("IOException:", e);
            throw e;
        }

        Param param = null;
        DB2UDB db2 = null;
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            try {
                db2.open();
            } catch (final Exception ex) {
                log.error("db open error!", ex);
                return;
            }
            param = createParam(request, db2);
            
            printSvfMain(db2, svf, param);
            
        } catch (final ServletException e) {
            log.error("ServletException:", e);
            throw e;
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            if (null != param) {
                param.removeImageFiles();
            }
            // 終了処理
            if (printName != null) {
                outStrm.println("<HTML>");
                outStrm.println("<HEAD>");
                outStrm.println("<META http-equiv=\"Content-Type\" content=\"text/html; charset=euc-jp\">");
                outStrm.println("</HEAD>");
                outStrm.println("<BODY>");
                if (_hasData) {
                    outStrm.println("<H1>印刷しました。</h1>");
                } else {
                    outStrm.println("<H1>対象データはありません。</h1>");
                }
                outStrm.println("</BODY>");
                outStrm.println("</HTML>");
            } else if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }

            final int ret = svf.VrQuit();
            log.info("===> VrQuit():" + ret);

            if (null != outStrm) {
                outStrm.close(); // ストリームを閉じる
            }
            if (null != db2) {
                db2.commit();
                db2.close();
            }
        }
    }

    /**
     * SVF-OUT 印刷処理
     */
    private void printSvfMain(
            final DB2UDB db2,
            final Vrw32alp svf,
            final Param param
    ) throws Exception {

        final Map students = Student.createStudents(db2, param); // 生徒の一覧を得る。

        final List sortedStudents = Student.loadStdData(db2, param, students);

        final PointSet pointSet1 = new PointSet(1);   // 学級/講座の平均と順位
        PointSet.setPointSetMap(pointSet1, param, db2, students.values());
        final PointSet pointSet2 = new PointSet(2);  // 学年/コースの平均と順位
        PointSet.setPointSetMap(pointSet2, param, db2, students.values());

        /** フォームファイル */
        final String form = "KNJD102_3.frm";

        svf.VrSetForm(form, 1);

        if (param._enableGraph && !"1".equals(param._useJFreeChart)) {
            /** MS-Excel2000 のファイル。*/
            final String EXCEL_FILE = "KNJD102H.xls";

            /** データが入っているExcel シート名。*/
            final String DATA_SHEET = "data";

            int ret = 0;
            ret = svf.VrComout("/{EXCL " + EXCEL_FILE + "}/");
            log.debug("Excel-File=" + EXCEL_FILE + ", ret=" + ret);

            ret = svf.VrComout("/{SSHT " + DATA_SHEET + "}/");
            log.debug("Excel-DataSheet=" + DATA_SHEET + ", ret=" + ret);
        }

        // ＳＶＦ属性変更--->改ページ
        svf.VrAttribute("NAME", "FF=1"); // 改ページ

        //== 印刷
        for (final Iterator itS = sortedStudents.iterator(); itS.hasNext();) {
            final Student student = (Student) itS.next();
            log.debug(student);
            Form.printSvfHead(svf, student, param);

            int gno = 0;
            for (final Iterator itC = student._subClassList.iterator(); itC.hasNext();) {
                final SubClass subClass = (SubClass) itC.next();

                Form.printSvfSubclass(svf, param, student, subClass, ++gno, pointSet1, pointSet2);
            }

            Form.printTotal(svf, student, param, pointSet1.getPoint(param, student, null), pointSet2.getPoint(param, student, null));

            _hasData = true;
            
            if (param._enableGraph) {
                if ("1".equals(param._useJFreeChart)) {
                    // チャート作成
                    final Map scoreDataMap = new HashMap();
                    final JFreeChart chart = Form.createBarChart(createDataSet(param, student, scoreDataMap, pointSet1), scoreDataMap);
                    
                    // グラフのファイルを生成
                    final File outputFile = Form.getGraphOutputFile(chart);
                    param._graphFiles.add(outputFile);

                    // グラフの出力
                    // svf.VrsOut("BAR_LABEL", KEY_TOKUTEN);
                    svf.VrsOut("BITMAP", outputFile.toString());

                } else {
                    Form.printSvfGraphExcel(svf, param, student, pointSet1);
                }
            }
            int ret = svf.VrEndPage();
            log.debug("VrEndPage() = " + ret);
        }
    }
    
    private static DefaultCategoryDataset createDataSet(final Param param, final Student student, final Map scoreDataMap, final PointSet pointSet1) {
        final String KEY_TOKUTEN = "得点";
        final String KEY_HEIKINTEN = "平均点";
        // グラフ用のデータ作成
        final Number none = new Integer(-1);
        final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        int i = 0;
        
        final Map tokutenMap = new HashMap();
        final Map heikintenMap = new HashMap();
        scoreDataMap.put(KEY_TOKUTEN, tokutenMap);
        scoreDataMap.put(KEY_HEIKINTEN, heikintenMap);
        for (final Iterator it = student._subClassList.iterator(); it.hasNext();) {
            final SubClass subClass = (SubClass) it.next();

            final BigDecimal divBd;
            if (NumberUtils.isDigits(subClass._perfect) && 0 < Integer.parseInt(subClass._perfect) && 100 != Integer.parseInt(subClass._perfect)) {
                divBd = new BigDecimal(subClass._perfect).divide(new BigDecimal(100), BigDecimal.ROUND_HALF_UP);
            } else {
                divBd = new BigDecimal(1);
            }
            final Number scoreBd;
            if (!NumberUtils.isDigits(subClass._score)) {
                scoreBd = none;
                tokutenMap.put(subClass._abbv, scoreBd);
            } else {
                scoreBd = new BigDecimal(subClass._score).divide(divBd, 0, BigDecimal.ROUND_DOWN);
                tokutenMap.put(subClass._abbv, new BigDecimal(subClass._score));
            }
            dataset.addValue(scoreBd, KEY_TOKUTEN, subClass._abbv);

            final Point point = pointSet1.getPoint(param, student, subClass._subclassChairCd);
            final BigDecimal averageBd = getAverage((List) point._rankMap.get(getDataKey(param, point, subClass)));
            final Number average;
            if (null == averageBd) {
                average = none;
                heikintenMap.put(subClass._abbv, average);
            } else {
                average = averageBd.divide(divBd, 0, BigDecimal.ROUND_DOWN);
                heikintenMap.put(subClass._abbv, averageBd);
            }
            dataset.addValue(average, KEY_HEIKINTEN, subClass._abbv);

            log.info("棒グラフ⇒" + subClass + ":素点=" + scoreBd + ", 平均=" + average);
            if (i++ > NUMPLOT) {
                break;
            }
        }

        for (; i < NUMPLOT; i++) {
            final String dummyAbbv = StringUtils.repeat(" ", i + 1);
            dataset.addValue(none, KEY_TOKUTEN, dummyAbbv);
            dataset.addValue(none, KEY_HEIKINTEN, dummyAbbv);
        }
        return dataset;
    }

    // 講座平均 or 学級平均、コース平均 or 学年平均
    private static BigDecimal getAverage(final List list) {
        final Float avg = getAvg(list);
        if (null == avg) {
            return null;
        }
        return new BigDecimal(avg.floatValue()).setScale(1, BigDecimal.ROUND_HALF_UP);
    }

    private static class Form {

        private static String getRankStr(final Map dataMap, final String key, final Number num, final Point point) {
            if (null == num || null == point) {
                return "";
            }
            final int ranking = point.rank(dataMap, key, num);
            return Integer.toString(ranking) + "/" + point.rankSize(dataMap, key);
        }
        
        /**
         * SVF-FORM 数値編集
         */
        private static String scoreFormat(
                final DecimalFormat format,
                final Object score
        ) {
            if (null == score || !NumberUtils.isNumber(score.toString())) {
                return "";
            }
            return String.valueOf(format.format(Float.parseFloat(score.toString())));
        }

        /**
         * SVF-FORM-OUT 学籍データ等印刷
         */
        private static void printSvfHead(
                final Vrw32alp svf,
                final Student student,
                final Param param
        ) throws NumberFormatException {
            svf.VrsOut("TEST", param._testName); // テスト名称
            svf.VrsOut("NENDO", nao_package.KenjaProperties.gengou(Integer.parseInt(param._year)) + "年度"); // 年度
            svf.VrsOut("SEMESTER", param._gakkiName); // 学期
            svf.VrsOut("DATE", param._jpDate); // 作成日
            svf.VrsOut("STAFFNAME", param._tannin); // 学級担任

            final String ban = Integer.parseInt(student._no) + "番   ";
            final String nenkumi = student._homeRoom._name;
            svf.VrsOut("NAME", nenkumi + " " + ban + student._name);

            svf.VrsOut("P_DATE", KNJ_EditDate.h_format_JP(param._date));

            final String avgHead;
            final String avgHead2;
            final String avgFoot;
            if (RANK_BASE_DEVIATION.equals(param._rankBase)) {
                avgFoot = "偏差値";
            } else {
                avgFoot = "平均";
            }
            if (param.isSelectHomeroom()) {
                avgHead = "学級";
            } else {
                avgHead = "講座";
            }
            if (RANK_DIV_GRADE.equals(param._rankDiv)) {
                avgHead2 = "学年";
            } else {
                avgHead2 = "コース";
            }
            svf.VrsOut("ITEM_AVERAGE1", avgHead + avgFoot);
            svf.VrsOut("ITEM_AVERAGE2", avgHead + avgFoot);

            svf.VrsOut("ITEM_RANKING1", avgHead + "順位");
            svf.VrsOut("ITEM_RANKING2", avgHead + "順位");
            
            svf.VrsOut("ITEM_C_DEV1", avgHead2 + avgFoot);
            svf.VrsOut("ITEM_C_DEV2", avgHead2 + avgFoot);

            svf.VrsOut("ITEM_C_RANKING1", avgHead2 + "順位");
            svf.VrsOut("ITEM_C_RANKING2", avgHead2 + "順位");
        }

        /**
         * SVF-FORM-OUT 成績データ等印刷
         */
        private static void printSvfSubclass(
                final Vrw32alp svf,
                final Param param,
                final Student student,
                final SubClass subClass,
                final int gno,
                final PointSet pointSet1,
                final PointSet pointSet2
        ) {
            /** 1段目の欄の数 */
            final int NUMFIELD = 12;

            final Point point1 = pointSet1.getPoint(param, student, subClass._subclassChairCd);
            final Point point2 = pointSet2.getPoint(param, student, subClass._subclassChairCd);

            final int f = (gno <= NUMFIELD) ? 1 : 2;
            final int g = (gno <= NUMFIELD) ? gno : gno - NUMFIELD;
            final String pScore = scoreFormat(_dmf1, subClass._score);
            
            String pAbsent = "";
            if (subClass._absent != null && 0 < Float.parseFloat(subClass._absent)) {
                if (param._defineCode.absent_cov == 3 || param._defineCode.absent_cov == 4) {
                    pAbsent = scoreFormat(_dmf2, subClass._absent);
                } else {
                    pAbsent = scoreFormat(_dmf1, subClass._absent);
                }
            }
            
            svf.VrsOutn("SUBCLASS" + f, g, subClass._abbv); // 科目名
            svf.VrsOutn("CREDIT" + f, g, scoreFormat(_dmf1, subClass._credits)); // 単位
            svf.VrsOutn("KEKKA" + f, g, pAbsent); // 欠時数
            svf.VrsOutn("POINT" + f, g, pScore); // 得点
            if (RANK_BASE_DEVIATION.equals(param._rankBase)) {
                if (NumberUtils.isDigits(subClass._score)) {
                    svf.VrsOutn("AVERAGE" + f, g, point1.getStdDev(Integer.valueOf(subClass._score), getDataKey(param, point1, subClass))); // 講座偏差値/ホームルーム偏差値
                    svf.VrsOutn("C_DEV"   + f, g, point2.getStdDev(Integer.valueOf(subClass._score), subClass._subclassCd)); // コース偏差値/学年偏差値
                }
            } else {
                svf.VrsOutn("AVERAGE" + f, g, getPrintAverage(point1, getDataKey(param, point1, subClass))); // 講座平均/ホームルーム平均
                svf.VrsOutn("C_DEV"   + f, g, getPrintAverage(point2, subClass._subclassCd)); // コース平均/学年平均
            }

            if (NumberUtils.isDigits(pScore)) {
                final Integer iScore = Integer.valueOf(pScore);
                // 講座順位/ホームルーム順位
                svf.VrsOutn("RANKING" + f, g, getRankStr(point1._rankMap, getDataKey(param, point1, subClass), iScore, point1));

                // コース順位/学年順位
                if (null != point2) {
                    svf.VrsOutn("C_RANKING" + f, g, getRankStr(point2._rankMap, subClass._subclassCd, iScore, point2));
                }
            }
        }
        
        private static void printTotal(final Vrw32alp svf, final Student student, final Param param, final Point point1, final Point point2) {
            svf.VrsOut("TOTAL_CREDIT", _dmf1.format(sum(student._creditList))); // 単位
            svf.VrsOut("TOTAL_KEKKA", sum(student._kekkaList) <= 0 ? "" : _dmf1.format(sum(student._kekkaList)));   // 欠課
            svf.VrsOut("TOTAL_AVERAGE", 0 == student._scoreList.size() ? "" : _dmf2.format((float) Math.round(sum(student._scoreList) / student._scoreList.size() * 10) / 10)); // 平均
            svf.VrsOut("TOTAL_POINT", student.getTotalPoint());   // 得点

            final String key = SUM_SUBCLASSCD;
            final Number avg = (Number) getMappedMap(point2._avgMap, key).get(student._schregno);
            if (RANK_BASE_DEVIATION.equals(param._rankBase)) {
                if (null != avg) {
//                    svf.VrsOut("TOTAL_AVERAGE", point1.getStdDev(avg, key)); // 講座偏差値/ホームルーム偏差値
                    svf.VrsOut("TOTAL_C_DEV", point2.getStdDev(avg, key)); // コース偏差値/学年偏差値
                }
            } else {
//                svf.VrsOut("TOTAL_AVERAGE", getPrintAverage(point1, key)); // 講座平均/ホームルーム平均
                svf.VrsOut("TOTAL_C_DEV", getPrintAverage(point2, key)); // コース平均/学年平均
            }

            final Number num;
            if (RANK_BASE_DEVIATION.equals(param._rankBase)) {
                num = (Number) getMappedMap(point2._stdDevMap, key).get(avg);
            } else {
                num = avg;
            }
            if (null != point1) {
                final Map rankMap1;
                if (RANK_BASE_DEVIATION.equals(param._rankBase)) {
                    rankMap1 = point1._stdDevRankMap;
                } else {
                    rankMap1 = point1._avgRankMap;
                }
                // 講座順位/ホームルーム順位
                svf.VrsOut("TOTAL_RANKING", getRankStr(rankMap1, key, num, point1));
            }
            if (null != point2) {
                final Map rankMap2;
                if (RANK_BASE_DEVIATION.equals(param._rankBase)) {
                    rankMap2 = point2._stdDevRankMap;
                } else {
                    rankMap2 = point2._avgRankMap;
                }
                // コース順位/学年順位
                svf.VrsOut("TOTAL_C_RANKING", getRankStr(rankMap2, key, num, point2));
            }
        }

        public static String getPrintAverage(final Point point, final String dataKey) {
            return scoreFormat(_dmf2, getAverage((List) point._rankMap.get(dataKey)));
        }
        
        private static void printSvfGraphSubClassExcel(
                final Vrw32alp svf,
                final Param param,
                final Student student,
                final SubClass subClass,
                final int gno,
                final PointSet pointSet
        ) {
            /*
             *  |A     |B     |C     |...|P|
             * -+------+------+------+
             * 1|      |現代文|古典  |
             * 2|得点  |45    |63    |
             * 3|平均点|50.1  |51.6  |
             * ----
             * ret = svf.VrComout("/{SDAT 2,1,1000}/");
             * -- 2=行(1行=0,2行=1...)
             * -- 1=列(A列=0,B列=1...)
             */
            final String retuNO = String.valueOf(gno + 1); // B列始まり

            /*
             * ★
             * グラフの出力にて、VrComout に渡す値に null や 空文字列("") を渡すと、
             * 内容は保証されない(前回印字した内容が印字されたりする)。
             * 
             * 捕捉: データラベル(MS-Excel用語)でゼロは印字されない。すなわち、「ない」と「ゼロ」を区別できない。
             */
            if (null == subClass) {
                svf.VrComout("/{SDAT " + "0" + "," + retuNO + "," + " " + "}/");
                svf.VrComout("/{SDAT " + "1" + "," + retuNO + "," + "0" + "}/");  // ★
                svf.VrComout("/{SDAT " + "2" + "," + retuNO + "," + "0" + "}/");  // ★
            } else {
                svf.VrComout("/{SDAT " + "0" + "," + retuNO + "," + subClass._abbv + "}/");

                if (!StringUtils.isEmpty(scoreFormat(_dmf1, subClass._score))) {
                    svf.VrComout("/{SDAT " + "1" + "," + retuNO + "," + scoreFormat(_dmf1, subClass._score) + "}/");
                } else {
                    svf.VrComout("/{SDAT " + "1" + "," + retuNO + "," + "0" + "}/");  // ★
                }

                final Point point = pointSet.getPoint(param, student, subClass._subclassChairCd);
                final String printAverage1 = scoreFormat(_dmf2, getAverage((List) point._rankMap.get(getDataKey(param, point, subClass))));
                if (!StringUtils.isEmpty(printAverage1)) {
                    svf.VrComout("/{SDAT " + "2" + "," + retuNO + "," + printAverage1 + "}/");
                } else {
                    svf.VrComout("/{SDAT " + "2" + "," + retuNO + "," + "0" + "}/");  // ★
                }
            }
        }
        
        private static void printSvfGraphExcel(final Vrw32alp svf, final Param param, final Student student, final PointSet pointSet1) {
            int ret;
            int gno;
            gno = 0;
            for (final Iterator itC = student._subClassList.iterator(); itC.hasNext();) {
                final SubClass subClass = (SubClass) itC.next();

                printSvfGraphSubClassExcel(svf, param, student, subClass, gno, pointSet1);
                gno++;
                if (NUMPLOT <= gno) {
                    break;
                }
            }

            for (; gno < NUMPLOT; gno++) {
                printSvfGraphSubClassExcel(svf, param, student, null, gno, null);
            }
            /** MS-Excel2000 のファイルのグラフが作成されているシート名。*/
            final String GRAPH_SHEET = "Graph";
            /** フォームファイル中のビットマップフィールド名 */
            final String BITMAP = "BITMAP";
            ret = svf.VrComout("/{CGRP " + BITMAP + "," + GRAPH_SHEET + "}/");
            log.debug("Form-File-Bitmap-Area=" + BITMAP + ", Excel-GraphSheet=" + GRAPH_SHEET + ", ret=" + ret);
        }

        private static JFreeChart createBarChart(
                final DefaultCategoryDataset scoreDataset,
                final Map scoreDataMap
        ) {
            final Color colorbar1 = new Color(255, 200, 3);
            final Color colorbar2 = new Color(193, 193, 193);
            final Color colorbg = new Color(255, 255, 153);

            final String fontName = "Sazanami Gothic Regular";
            final Font legendfont = new Font(fontName, Font.BOLD, 12);
            final Font tickLabelfont = new Font(fontName, Font.PLAIN, 9);
            final Font itemLabelFont = new Font(fontName, Font.PLAIN, 13);
            final Font itemLabelFont2 = new Font(fontName, Font.ITALIC, 13);
            final Font labelFont = new Font(fontName, Font.BOLD, 14);

            final JFreeChart chart = ChartFactory.createBarChart(null, null, null, scoreDataset, PlotOrientation.VERTICAL, true, false, false);
            final RectangleInsets chartMargin = chart.getPadding();
            chart.setPadding(new RectangleInsets(chartMargin.getTop() + 10, chartMargin.getLeft() + 10, chartMargin.getBottom() + 80, chartMargin.getRight() + 10));
            chart.setAntiAlias(true);
            chart.setBorderVisible(true);
            chart.setBorderStroke(new BasicStroke(2.0f));
            
            final CategoryPlot plot = chart.getCategoryPlot();
            plot.setRangeGridlineStroke(new BasicStroke(2.0f)); // 目盛りの太さ
            plot.setRangeGridlinePaint(Color.black);
            plot.setRangeGridlineStroke(new BasicStroke(1.0f));
            plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
            plot.setBackgroundPaint(colorbg);
            
            final LegendTitle legend = chart.getLegend();
            legend.setPosition(RectangleEdge.RIGHT);
            legend.setItemFont(legendfont);
            legend.setBorder(BlockBorder.NONE);
            final RectangleInsets legendMargin = legend.getMargin();
            legend.setMargin(legendMargin.getTop(), legendMargin.getLeft() + 10, legendMargin.getBottom(), legendMargin.getRight() + 10);
            
            final BarRenderer renderer = (BarRenderer) plot.getRenderer();
            renderer.setSeriesPaint(0, colorbar1);
            renderer.setSeriesPaint(1, colorbar2);
            renderer.setSeriesItemLabelFont(0, itemLabelFont);
            renderer.setSeriesItemLabelFont(1, itemLabelFont2);
            renderer.setBaseItemLabelsVisible(true);
            renderer.setMaximumBarWidth(0.05);
            renderer.setBaseItemLabelGenerator(new KNJD102HStandardCategoryItemLabelGenerator(scoreDataMap));
            renderer.setOutlinePaint(Color.black);

            final CategoryAxis domainAxis = new KNJD102HCategoryAxis();
            domainAxis.setTickLabelsVisible(true);
            domainAxis.setTickLabelFont(tickLabelfont);
            domainAxis.setLabel("科目");
            domainAxis.setLabelFont(labelFont);
            final RectangleInsets domainLabelInsets = domainAxis.getLabelInsets();
            domainAxis.setLabelInsets(new RectangleInsets(domainLabelInsets.getTop() + 60, domainLabelInsets.getLeft(), domainLabelInsets.getBottom(), domainLabelInsets.getRight()));
            plot.setDomainAxis(domainAxis);

            final NumberAxis numberAxis = new NumberAxis();
            numberAxis.setTickUnit(new NumberTickUnit(10));
            numberAxis.setTickLabelsVisible(true);
            numberAxis.setTickLabelFont(tickLabelfont);
            numberAxis.setRange(0, 100.0);
            numberAxis.setLabel("得点");
            numberAxis.setLabelAngle(Math.PI / 2);
            numberAxis.setLabelFont(labelFont);
            plot.setRangeAxis(numberAxis);

            chart.setBackgroundPaint(Color.white);

            return chart;
        }

        private static File getGraphOutputFile(final JFreeChart chart) {
            final int w = 2930;
            final int h = 1942;
            final String tmpFileName = KNJServletUtils.createTmpFile(".png");
            log.debug("\ttmp file name=" + tmpFileName);

            final File outputFile = new File(tmpFileName);
            try {
                ChartUtilities.saveChartAsPNG(outputFile, chart, Form.dot2pixel(w), Form.dot2pixel(h));
            } catch (final IOException ioEx) {
                log.error("グラフイメージをファイル化できません。", ioEx);
            }
            return outputFile;
        }

        private static int dot2pixel(final int dot) {
            // final int pixel = dot / 4;   // おおよそ4分の1程度でしょう。

            /*
             * 少し大きめにする事でSVFに貼り付ける際の拡大を防ぐ。
             * 拡大すると粗くなってしまうから。
             */
            return (int) (1.3 * dot / 4);
        }
    }

    private static List getMappedList(final Map _map, final String key) {
        if (!_map.containsKey(key)) {
            _map.put(key, new ArrayList());
        }
        return (List) _map.get(key);
    }

    private static Map getMappedMap(final Map _map, final String key) {
        if (!_map.containsKey(key)) {
            _map.put(key, new HashMap());
        }
        return (Map) _map.get(key);
    }

    private static float sum(final List floatList) {
        float total = 0;
        for (final Iterator it = floatList.iterator(); it.hasNext();) {
            final Number point = (Number) it.next();
            total += point.floatValue();
        }
        return total;
    }

    /**
     * キーの得点リストの平均点を得る
     * @param key キー
     * @return キーの得点リストの平均点
     */
    private static Float getAvg(final List list) {
        if (null == list) {
            return null;
        }
        final float avg = (float) sum(list) / list.size();
        return new Float(avg);
    }

    // =======================================================================

    /*pkg*/static class Student {
        
        final String _schregno;
        final String _no;
        final String _name;

        private List _creditList = new ArrayList();
        private List _kekkaList = new ArrayList();
        private List _scoreList = new ArrayList();

        final List _subClassList = new LinkedList();

        final Course _course;
        final HomeRoom _homeRoom;

        /* pkg */Student(
                final String schregno,
                final String attendNo,
                final String name,
                final Course course,
                final HomeRoom homeRoom
        ) {
            _schregno = schregno;
            _no = attendNo;
            _name = name;
            _course = course;
            _homeRoom = homeRoom;
        }

        /** {@inheritDoc} */
        public boolean equals(final Object obj) {
            if (obj instanceof String) {
                return _schregno.equals(obj);
            }
            if (obj instanceof Student) {
                final Student std = (Student) obj;
                return _schregno.equals(std._schregno);
            }
            return false;
        }

        /** {@inheritDoc} */
        public int hashCode() {
            return _schregno.hashCode();
        }

        /** {@inheritDoc} */
        public String toString() {
            return _schregno + "," + _name + ", " + _homeRoom._name + "年組, " + _no + "番";
        }

        public String getTotalPoint() {
            // TAKAESU: 本来の仕様は「詳細な得点が全て空っぽの時は空っぽを印字」だ。すなわちゼロと空っぽの明確な定義
            int result = 0;
            for (final Iterator it = _subClassList.iterator(); it.hasNext();) {
                final SubClass subClass = (SubClass) it.next();
                if (NumberUtils.isDigits(subClass._score)) {
                    result += Integer.parseInt(subClass._score);
                }
            }
            return String.valueOf(result);
        }

        private static List loadStdData(
                final DB2UDB db2,
                final Param param,
                final Map courseStudents
        ) throws Exception {
            final List students = new ArrayList();

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                String sql;
                sql = sqlStdData(param);
                ps = db2.prepareStatement(sql);
                
                for (int i = 0; i < param._schregs.length; i++) {
                    final String schregno = param._schregs[i];
                    ps.setString(1, schregno);
                    ps.setString(2, schregno);
                    rs = ps.executeQuery();
                    
                    while (rs.next()) {
                        final String schno = rs.getString("SCHREGNO");
                        final Student student = (Student) courseStudents.get(schno);

                        final String code = rs.getString("SUBCLASSCD");
                        final String subclassChairCd = rs.getString("SUBCLASS_CHAIR_CD");
                        if (null == subclassChairCd) {
                            log.warn(param._chaircdField + "がnull:" + student + ", 科目=" + code);
                            continue;
                        }
                        final String abbv = rs.getString("SUBCLASSABBV");
                        final String credits = rs.getString("CREDITS");
                        final String score = rs.getString("SCORE");
                        final String perfect = rs.getString("PERFECT");

                        final SubClass subClass = new SubClass(code, subclassChairCd, abbv, credits, score, perfect);

                        student._subClassList.add(subClass);
                        if (!students.contains(student)) {
                            students.add(student);
                        }
                        
                        if (null != subClass._credits) {
                            student._creditList.add(new Float(Float.parseFloat(subClass._credits)));
                        }

                        if (null != subClass._score) {
                            student._scoreList.add(new Float(Float.parseFloat(subClass._score)));
                        }
                    }
                    db2.commit();
                    DbUtils.closeQuietly(rs);
                }
                
                // 出欠の情報
                final KNJDefineSchool _definecode = new KNJDefineSchool();       //各学校における定数等設定
                _definecode.defineCode (db2, param._year);
                final KNJDefineCode definecode0 = param.setClasscode0(db2);
                final String z010Name1 = param.setZ010Name1(db2);
                final String sdate = param._sdate;
                final String SSEMESTER = "1";
                final String ESEMESTER = "9";
                final String periodInState = AttendAccumulate.getPeiodValue(db2, definecode0, param._year, SSEMESTER, ESEMESTER);
                final Map attendSemesMap = AttendAccumulate.getAttendSemesMap(db2, z010Name1, param._year);
                final Map hasuuMap = AttendAccumulate.getHasuuMap(attendSemesMap,  sdate, param._date);
                log.debug(" hasuuMap = " + hasuuMap);
                
                final boolean semesFlg = ((Boolean) hasuuMap.get("semesFlg")).booleanValue();
                final KNJDefineSchool defineSchool = new KNJDefineSchool();
                defineSchool.defineCode(db2, param._year);
                
                final KNJSchoolMst _knjSchoolMst = new KNJSchoolMst(db2, param._year);
                log.debug(" KNJSchoolMst.lesson_flg = " + _knjSchoolMst._jugyouJisuFlg);

                final Map paramMap = new HashMap();
                paramMap.put("useCurriculumcd", param._useCurriculumcd);
                paramMap.put("useVirus", param._useVirus);
                paramMap.put("useKoudome", param._useKoudome);
                paramMap.put("DB2UDB", db2);

                // 時数単位
                sql = AttendAccumulate.getAttendSubclassSql(
                        semesFlg,
                        defineSchool,
                        _knjSchoolMst,
                        param._year,
                        SSEMESTER,
                        ESEMESTER,
                        (String) hasuuMap.get("attendSemesInState"),
                        periodInState,
                        (String) hasuuMap.get("befDayFrom"),
                        (String) hasuuMap.get("befDayTo"),
                        (String) hasuuMap.get("aftDayFrom"),
                        (String) hasuuMap.get("aftDayTo"),
                        null,
                        null,
                        "?",
                        paramMap
                        );
                ps = db2.prepareStatement(sql);
                
                for (int i = 0; i < param._schregs.length; i++) {
                    final String schregno = param._schregs[i];

                    ps.setString(1, schregno);
                    rs = ps.executeQuery();
                    
                    while (rs.next()) {
                        if (!"9".equals(rs.getString("SEMESTER"))) {
                            continue;
                        }
                        final String schno = rs.getString("SCHREGNO");
                        final Student student = (Student) courseStudents.get(schno);

                        final String code = rs.getString("SUBCLASSCD");
                        final String absent = rs.getString("SICK2");

                        final SubClass subClass = SubClass.getSubclass(code, student._subClassList);
                        if (null != subClass) {
                            subClass._absent = absent;
                            if (null != subClass._absent) {
                                student._kekkaList.add(new Float((float) Math.floor(Float.parseFloat(subClass._absent))));
                            }
                        }
                    }
                    db2.commit();
                    DbUtils.closeQuietly(rs);
                }

            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return students;
        }
        
        /**
         * 生徒別明細SQL作成
         * 2005/06/20 yamashiro・ペナルティ欠課の算出式を修正
         */
        private static String sqlStdData(final Param param) {
            final StringBuffer sb = new StringBuffer();

            // 在籍の表
            sb.append("WITH SCHNO_A AS(");
            sb.append(    "SELECT  T2.SCHREGNO, T3.NAME, T4.HR_NAME, T2.GRADE, T2.HR_CLASS, T2.ATTENDNO, ");
            sb.append(            "T2.COURSECD, T2.MAJORCD, T2.COURSECODE ");
            sb.append(    "FROM    SCHREG_REGD_DAT T2, ");
            sb.append(            "SCHREG_BASE_MST T3, ");
            sb.append(            "SCHREG_REGD_HDAT T4 ");
            sb.append(    "WHERE   T2.YEAR = '" + param._year + "' AND ");
            sb.append(            "T2.SEMESTER = '" + param._gakki + "' AND ");
            sb.append(            "T2.SCHREGNO = T3.SCHREGNO AND T4.YEAR = '" + param._year + "' AND ");
            sb.append(            "T4.SEMESTER = T2.SEMESTER AND T4.GRADE = T2.GRADE AND ");
            sb.append(            "T4.HR_CLASS = T2.HR_CLASS AND ");
            sb.append(            "T2.SCHREGNO = ? ");
            sb.append(    ") ");

            // 成績の表
            sb.append(",RECORD_A AS(");
            sb.append(    "SELECT  T1.SCHREGNO, ");
            if ("1".equals(param._useCurriculumcd)) {
                sb.append("    T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
            }
            sb.append(           "T1.SUBCLASSCD AS SUBCLASSCD, " + param._chaircdField + ", ");
            sb.append(             param._scoreField + " AS SCORE, ");
            sb.append(           "VALUE(T4.PERFECT, T3.PERFECT, T2.PERFECT, 100) AS PERFECT ");
            sb.append(    "FROM    RECORD_DAT T1 ");
            sb.append(    "INNER JOIN SCHNO_A TA ON TA.SCHREGNO = T1.SCHREGNO ");
            sb.append(    "LEFT JOIN PERFECT_RECORD_DAT T2 ON T2.YEAR = T1.YEAR ");
            sb.append(    "    AND T2.SEMESTER = '" + param._gakki + "' ");
            sb.append(    "    AND T2.TESTKINDCD || T2.TESTITEMCD = '" + param._testKind + "' ");
            if ("1".equals(param._useCurriculumcd)) {
                sb.append(    "    AND T2.CLASSCD = T1.CLASSCD AND T2.SCHOOL_KIND = T1.SCHOOL_KIND AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            sb.append(    "    AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
            sb.append(    "    AND T2.DIV = '01' AND T2.GRADE = '00' AND T2.COURSECD = '0' AND T2.MAJORCD = '000' AND T2.COURSECODE = '0000' ");
            sb.append(    "LEFT JOIN PERFECT_RECORD_DAT T3 ON T3.YEAR = T1.YEAR ");
            sb.append(    "    AND T3.SEMESTER = '" + param._gakki + "' ");
            sb.append(    "    AND T3.TESTKINDCD || T3.TESTITEMCD = '" + param._testKind + "' ");
            if ("1".equals(param._useCurriculumcd)) {
                sb.append(    "    AND T3.CLASSCD = T1.CLASSCD AND T3.SCHOOL_KIND = T1.SCHOOL_KIND AND T3.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            sb.append(    "    AND T3.SUBCLASSCD = T1.SUBCLASSCD ");
            sb.append(    "    AND T3.DIV = '02' AND T3.GRADE = TA.GRADE AND T3.COURSECD = '0' AND T3.MAJORCD = '000' AND T3.COURSECODE = '0000' ");
            sb.append(    "LEFT JOIN PERFECT_RECORD_DAT T4 ON T4.YEAR = T1.YEAR ");
            sb.append(    "    AND T4.SEMESTER = '" + param._gakki + "' ");
            sb.append(    "    AND T4.TESTKINDCD || T4.TESTITEMCD = '" + param._testKind + "' ");
            if ("1".equals(param._useCurriculumcd)) {
                sb.append(    "    AND T4.CLASSCD = T1.CLASSCD AND T4.SCHOOL_KIND = T1.SCHOOL_KIND AND T4.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            sb.append(    "    AND T4.SUBCLASSCD = T1.SUBCLASSCD ");
            sb.append(    "    AND T4.DIV = '03' AND T4.GRADE = TA.GRADE AND T4.COURSECD = TA.COURSECD AND T4.MAJORCD = TA.MAJORCD AND T4.COURSECODE = TA.COURSECODE ");
            sb.append(    "WHERE   T1.YEAR='" + param._year + "' AND ");
            sb.append(            "T1.SCHREGNO = ? AND ");
            sb.append(            "(" + param._scoreField + " IS NOT NULL OR " + param._chaircdField + " IS NOT NULL) ");
            sb.append(    ") ");

            // メイン表
            sb.append("SELECT  T1.HR_NAME, ");
            sb.append(        "T1.SCHREGNO, ");
            sb.append(        "T1.ATTENDNO, ");
            sb.append(        "T1.NAME, ");
            sb.append(        "T2.SCORE, ");
            sb.append(        "T2.PERFECT, ");
            sb.append(        "T4.CREDITS, ");
            sb.append(        "T2.SUBCLASSCD, ");
            sb.append(        "T2.SUBCLASSCD || T2." + param._chaircdField + " AS SUBCLASS_CHAIR_CD, ");
            sb.append(        "T5.SUBCLASSABBV ");
            sb.append("FROM    SCHNO_A T1 ");
            sb.append(        "LEFT JOIN RECORD_A T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            sb.append(        "LEFT JOIN CREDIT_MST T4 ON T4.YEAR= '" + param._year + "' AND ");
            sb.append(                                   "T4.GRADE = T1.GRADE AND ");
            sb.append(                                   "T4.COURSECD = T1.COURSECD AND ");
            sb.append(                                   "T4.MAJORCD = T1.MAJORCD AND ");
            sb.append(                                   "T4.COURSECODE = T1.COURSECODE AND ");
            if ("1".equals(param._useCurriculumcd)) {
                sb.append("    T4.CLASSCD || '-' || T4.SCHOOL_KIND || '-' || T4.CURRICULUM_CD || '-' || ");
            }
            sb.append(                                   "T4.SUBCLASSCD = T2.SUBCLASSCD ");
            sb.append(        "LEFT JOIN SUBCLASS_MST T5 ON ");
            if ("1".equals(param._useCurriculumcd)) {
                sb.append("    T5.CLASSCD || '-' || T5.SCHOOL_KIND || '-' || T5.CURRICULUM_CD || '-' || ");
            }
            sb.append(                                   "T5.SUBCLASSCD = T2.SUBCLASSCD ");
            sb.append("ORDER BY T1.GRADE, T1.HR_CLASS, T1.ATTENDNO, T2.SUBCLASSCD ");

            return sb.toString();
        }
        
        private static Map createStudents(final DB2UDB db2, final Param param) {
            final Map rtn = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sqlStudents(param));
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String schregno = rs.getString("SCHREGNO");
                    final String hrName = rs.getString("HR_NAME");
                    final String attendNo = rs.getString("ATTENDNO");
                    final String name = rs.getString("NAME");

                    final String courseCd = rs.getString("COURSECD");
                    final String majorcd = rs.getString("MAJORCD");
                    final String courseCode = rs.getString("COURSECODE");
                    final String grade = rs.getString("GRADE");
                    final String hrClass = rs.getString("HR_CLASS");
                    final Course course = new Course(courseCd, majorcd, courseCode, grade);
                    final HomeRoom homeRoom = new HomeRoom(grade, hrClass, hrName);

                    final Student student = new Student(schregno, attendNo, name, course, homeRoom);
                    rtn.put(schregno, student);
                }
            } catch (final Exception e) {
                log.error("生徒の一覧取得にてエラー", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtn;
        }
        
        private static String sqlStudents(final Param param) {
            final String sql;
            sql = "SELECT"
                + "    T1.SCHREGNO,"
                + "    T1.ATTENDNO,"
                + "    T1.COURSECD,"
                + "    T1.MAJORCD,"
                + "    T1.COURSECODE,"
                + "    T1.GRADE,"
                + "    T1.HR_CLASS,"
                + "    T2.HR_NAME,"
                + "    T3.NAME"
                + "  FROM"
                + "    SCHREG_REGD_DAT T1,"
                + "    SCHREG_REGD_HDAT T2,"
                + "    SCHREG_BASE_MST T3"
                + "  WHERE"
                + "    T1.YEAR = T2.YEAR AND"
                + "    T1.SEMESTER = T2.SEMESTER AND"
                + "    T1.GRADE = T2.GRADE AND"
                + "    T1.HR_CLASS = T2.HR_CLASS AND"
                + "    T1.SCHREGNO = T3.SCHREGNO AND"
                + "    T1.year ='" + param._year + "' AND"
                + "    T2.semester ='" + param._gakki + "' AND"
                + "    T1.schregno in " + SQLUtils.whereIn(true, param._schregs)
                + "  ORDER BY"
                + "    T1.GRADE, T1.HR_CLASS, T1.ATTENDNO"
                ;
            return sql;
        }
    } // Student
    
    // =======================================================================

    private static class PointSet {
        final int _kind;
        private Map _pointMap;
        PointSet(final int kind) {
            _kind = kind;
        }
        private Object getKey(final Param param, final Student student, final String subClassChairCd) {
            final Object key;
            if (_kind == 1) {
                if (param.isSelectHomeroom()) {
                    key = student._homeRoom;
                } else {
                    key = subClassChairCd;
                }
                return key;
            } else if (_kind == 2) {
                if (RANK_DIV_GRADE.equals(param._rankDiv)) {
                    key = student._homeRoom._grade;
                } else {
                    key = student._course;
                }
                return key;
            }
            throw new IllegalArgumentException(" kind should be 1 or 2 : " + _kind);
        }

        Point getPoint(final Param param, final Student student, final String subClassChairCd) {
            return (Point) _pointMap.get(getKey(param, student, subClassChairCd));
        }
        
        private static void setPointSetMap(final PointSet pointSet, final Param param, final DB2UDB db2, final Collection students) {
            if (pointSet._kind == 1) {
                pointSet._pointMap = new HashMap();
                if (param.isSelectHomeroom()) {
                    final Set groups = new HashSet();
                    for (final Iterator it = students.iterator(); it.hasNext();) {
                        final Student student = (Student) it.next();
                        groups.add(student._homeRoom);
                    }
                    log.debug("年組のSet=" + groups);
                    for (final Iterator it = groups.iterator(); it.hasNext();) {
                        final HomeRoom homeRoom = (HomeRoom) it.next();
                        final Point point = new Point(pointSet._kind, "学級");
                        
                        final StringBuffer sql = new StringBuffer();
                        sql.append("SELECT");
                        if ("1".equals(param._useCurriculumcd)) {
                            sql.append("    t1.classcd || '-' || t1.school_kind || '-' || t1.curriculum_cd || '-' || ");
                        }
                        sql.append("    t1.subclasscd AS subclasscd,");
                        sql.append("    t1.schregno,");
                        sql.append("    t1." + param._scoreField + " AS score");
                        sql.append(" FROM");
                        sql.append("    record_dat t1,");
                        sql.append("    schreg_regd_dat t2");
                        sql.append(" WHERE");
                        sql.append("    t1.year = t2.year AND");
                        sql.append("    t1.schregno = t2.schregno AND");
                        sql.append("    t1.year = '" + param._year + "' AND");
                        sql.append("    t2.semester = '" + param._gakki + "' AND");
                        sql.append("    t2.grade = '" + homeRoom._grade + "' AND");
                        sql.append("    t2.hr_class = '" + homeRoom._room + "' AND");
                        sql.append("    t1." + param._scoreField + " is not null");
                        sql.append(" ORDER BY");
                        sql.append("    subclasscd");

                        loadPoint(db2, point, param, sql.toString(), "SUBCLASSCD");
                        pointSet._pointMap.put(homeRoom, point);
                    }
                } else {
                    final Set groups = new HashSet();
                    for (final Iterator it = students.iterator(); it.hasNext();) {
                        final Student student = (Student) it.next();
                        for (final Iterator its = student._subClassList.iterator(); its.hasNext();) {
                            final SubClass subClass = (SubClass) its.next();
                            groups.add(subClass._subclassChairCd);
                        }
                    }
                    log.debug("講座のSet=" + groups);
                    for (final Iterator it = groups.iterator(); it.hasNext();) {
                        final String subclassChairCd = (String) it.next();
                        final Point point = new Point(pointSet._kind, "科目講座");
                        
                        final StringBuffer sql = new StringBuffer();
                        sql.append("select");
                        sql.append("   SCHREGNO, ");
                        if ("1".equals(param._useCurriculumcd)) {
                            sql.append("    CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || ");
                        }
                        sql.append("   SUBCLASSCD || " + param._chaircdField + " as SUBCLASS_CHAIR_CD,");
                        sql.append("   " + param._scoreField + " as SCORE");
                        sql.append(" from RECORD_DAT");
                        sql.append(" where");
                        sql.append("   YEAR='" + param._year + "' and");
                        sql.append("   " + param._chaircdField + " IS NOT NULL and");
                        sql.append("   " + param._scoreField + " IS NOT NULL");
                        if (null != subclassChairCd) {
                            sql.append("   and ");
                            if ("1".equals(param._useCurriculumcd)) {
                                sql.append("    CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || ");
                            }
                            sql.append("       SUBCLASSCD || " + param._chaircdField + " = '" + subclassChairCd + "' ");
                        }

                        loadPoint(db2, point, param, sql.toString(), "SUBCLASS_CHAIR_CD");
                        pointSet._pointMap.put(subclassChairCd, point);
                    }
                }
                return;
            } else if (pointSet._kind == 2) {
                pointSet._pointMap = new HashMap();
                if (RANK_DIV_GRADE.equals(param._rankDiv)) {
                    final Set groupSet = new HashSet();
                    for (final Iterator it = students.iterator(); it.hasNext();) {
                        final Student student = (Student) it.next();
                        groupSet.add(student._homeRoom._grade);
                    }
                    log.debug("{学年}の全種類=" + groupSet + "/" + groupSet.size() + "個");
                    for (final Iterator it = groupSet.iterator(); it.hasNext();) {
                        final String grade = (String) it.next();
                        final Point point = new Point(pointSet._kind, "学年");
                        
                        final StringBuffer sql = new StringBuffer();
                        sql.append("SELECT");
                        if ("1".equals(param._useCurriculumcd)) {
                            sql.append("    t1.classcd || '-' || t1.school_kind || '-' || t1.curriculum_cd || '-' || ");
                        }
                        sql.append("    t1.subclasscd AS subclasscd,");
                        sql.append("    t1.schregno,");
                        sql.append("    t1." + param._scoreField + " AS score");
                        sql.append(" FROM");
                        sql.append("    record_dat t1,");
                        sql.append("    schreg_regd_dat t2");
                        sql.append(" WHERE");
                        sql.append("    t1.year = t2.year AND");
                        sql.append("    t1.schregno = t2.schregno AND");
                        sql.append("    t1.year = '" + param._year + "' AND");
                        sql.append("    t2.semester = '" + param._gakki + "' AND");
                        sql.append("    t2.grade = '" + grade + "' AND");
                        sql.append("    t1." + param._scoreField + " is not null");
                        sql.append(" ORDER BY");
                        sql.append("    subclasscd");

                        loadPoint(db2, point, param, sql.toString(), "SUBCLASSCD");
                        pointSet._pointMap.put(grade, point);
                    }
                } else {
                    final Set groupSet = new HashSet();
                    for (final Iterator it = students.iterator(); it.hasNext();) {
                        final Student student = (Student) it.next();
                        groupSet.add(student._course);
                    }
                    log.debug("{課程・学科・コース・学年}の全種類=" + groupSet + "/" + groupSet.size() + "個");
                    for (final Iterator it = groupSet.iterator(); it.hasNext();) {
                        final Course course = (Course) it.next();
                        final Point point = new Point(pointSet._kind, "コース");
                        
                        final StringBuffer sql = new StringBuffer();
                        sql.append("select");
                        sql.append("    T1.SCHREGNO, ");
                        if ("1".equals(param._useCurriculumcd)) {
                            sql.append("    T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
                        }
                        sql.append("    T1.SUBCLASSCD AS SUBCLASSCD,");
                        sql.append("    " + param._scoreField + " as SCORE");
                        sql.append(" from");
                        sql.append("    RECORD_DAT T1, SCHREG_REGD_DAT T2");
                        sql.append(" where");
                        sql.append("    T1.SCHREGNO = T2.SCHREGNO and");
                        sql.append("    T1.YEAR = T2.YEAR and");
                        sql.append("   " + param._scoreField + " IS NOT NULL and");
                        sql.append("    T1.YEAR='" + param._year + "' and");
                        sql.append("    T2.SEMESTER='" + param._gakki + "' and");
                        sql.append("    T2.COURSECD = '" + course._courseCd + "' and");
                        sql.append("    T2.MAJORCD = '" + course._majorcd + "' and");
                        sql.append("    T2.COURSECODE = '" + course._courseCode + "' and");
                        sql.append("    T2.GRADE = '" + course._grade + "'");

                        loadPoint(db2, point, param, sql.toString(), "SUBCLASSCD");
                        pointSet._pointMap.put(course, point);
                    }
                }
                return;
            }
            throw new IllegalArgumentException(" kind should be 1 or 2 : " + pointSet._kind);
        }
        
        private static void loadPoint(final DB2UDB db2, final Point point, final Param param, final String sql, final String keyField) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                //log.debug(" loadPoint sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String key = rs.getString(keyField);
                    final String schregno = rs.getString("SCHREGNO");
                    final Integer score = KNJServletUtils.getInteger(rs, "SCORE");

                    getMappedMap(point._dataMap, key).put(schregno, score);
                }
            } catch (final Exception ex) {
                log.error(point._title + "平均の取得に失敗", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
            if ("SUBCLASSCD".equals(keyField)) {
                final String key = SUM_SUBCLASSCD; // 合計の科目コード
                final Map allStudentScoreMap = new HashMap();
                for (final Iterator it = point._dataMap.keySet().iterator(); it.hasNext();) {
                    final String subclasscd = (String) it.next();
                    final Map subclassScoreMap = (Map) point._dataMap.get(subclasscd);
                    for (final Iterator sit = subclassScoreMap.keySet().iterator(); sit.hasNext();) {
                        final String schregno = (String) sit.next();
                        final Integer score = (Integer) subclassScoreMap.get(schregno);
                        if (null != score) {
                            getMappedList(allStudentScoreMap, schregno).add(score);
                        }
                    }
                }
                for (final Iterator it = allStudentScoreMap.keySet().iterator(); it.hasNext();) {
                    final String schregno = (String) it.next();
                    final List scoreList = (List) allStudentScoreMap.get(schregno);
                    final float sum = sum(scoreList);
                    getMappedMap(point._dataMap, key).put(schregno, new Integer(Math.round(sum)));
                    getMappedMap(point._avgMap, key).put(schregno, getAverage(scoreList));
                }
            }

            final Comparator c = new ReverseComparator();
            for (final Iterator itk = point._dataMap.keySet().iterator(); itk.hasNext();) {
                final String key = (String) itk.next();
                final List list = new ArrayList(((Map) point._dataMap.get(key)).values());
                point._rankMap.put(key, list);
                Collections.sort(list, c);
                log.debug(point._title + "順位:" + key + ", 得点順:" + list);
                if (SUM_SUBCLASSCD.equals(key)) {
                    final List avglist = new ArrayList(((Map) point._avgMap.get(key)).values());
                    point._avgRankMap.put(key, avglist);
                    Collections.sort(avglist, c);
                    log.debug(point._title + "順位:" + key + ", 平均点得点順:" + avglist);
                    
                    if (RANK_BASE_DEVIATION.equals(param._rankBase)) {
                        final Map totalStdDevMap = createStdDevMap(avglist);
                        point._stdDevMap.put(key, totalStdDevMap);
                        final List stddevlist = new ArrayList();
                        for (final Iterator ait = avglist.iterator(); ait.hasNext();) {
                            final BigDecimal avg = (BigDecimal) ait.next();
                            stddevlist.add(totalStdDevMap.get(avg));
                        }
                        point._stdDevRankMap.put(key, stddevlist);
                        Collections.sort(stddevlist, c);
                        log.debug(point._title + "平均点偏差値" + ":" + key + ", 平均の偏差値順:" + stddevlist);
                    }
                } else {
                    if (RANK_BASE_DEVIATION.equals(param._rankBase)) {
                        // 各キーの得点リストの偏差値を算出する
                        final Map stdDevMap = createStdDevMap(list);
                        point._stdDevMap.put(key, stdDevMap);
                        log.debug(point._title + "偏差値" + ":" + key + ", 偏差値:" + stdDevMap);
                    }
                }
            }
        }
        public static Map createStdDevMap(final List list) {
            final Map stdDevMap = new HashMap();
            final Float avg = getAvg(list);
            double sigmaSum = 0.0;
            for (final Iterator it = list.iterator(); it.hasNext();) {
                final Number score = (Number) it.next();
                sigmaSum += Math.pow(score.doubleValue() - avg.doubleValue(), 2.0);
            }
            final double sigma = Math.sqrt(sigmaSum / list.size());
            for (final Iterator it = list.iterator(); it.hasNext();) {
                final Number score = (Number) it.next();
                if (!stdDevMap.containsKey(score)) {
                    final double stdDiv = (0.0 == sigma ? 0.0 : 10 * (score.doubleValue() - avg.doubleValue()) / sigma) + 50;
                    final Number val = new BigDecimal(stdDiv).setScale(1, BigDecimal.ROUND_HALF_UP);
                    stdDevMap.put(score, val);
                }
            }
            return stdDevMap;
        }
    }
    
    // =======================================================================

    /**
     * 生徒の科目ごとのデータ
     */
    private static class SubClass {
        private final String _subclassCd;    // 科目コード
        private final String _subclassChairCd;
        private final String _abbv;

        private final String _credits;
        private final String _score;
        private final String _perfect;

        private String _absent;

        public SubClass(
                final String subclassCd,
                final String subclassChairCd,
                final String abbv,
                final String credits,
                final String score,
                final String perfect
        ) {
            _subclassCd = subclassCd;
            _subclassChairCd = subclassChairCd;
            _abbv = abbv;
            _credits = credits;
            _score = score;
            _perfect = perfect;
        }

        public static SubClass getSubclass(final String code, final List subClassList) {
            for (final Iterator it = subClassList.iterator(); it.hasNext();) {
                final SubClass subClass = (SubClass) it.next();
                if (subClass._subclassCd.equals(code)) {
                    return subClass;
                }
            }
            return null;
        }

        public String toString() {
            return _subclassChairCd + ":" + _abbv;
        }
    } // SubClass

    // =======================================================================

    private static class HomeRoom {
        private final String _grade;
        private final String _room;
        private final String _name;
        HomeRoom(
                final String grade,
                final String room,
                final String name
        ) {
            _grade = grade;
            _room = room;
            _name = name;
        }

        /** {@inheritDoc} */
        public boolean equals(final Object obj) {
            if (obj instanceof HomeRoom) {
                final HomeRoom that = (HomeRoom) obj;
                if (!this._grade.equals(that._grade)) {
                    return false;
                }
                if (!this._room.equals(that._room)) {
                    return false;
                }
                if (this._name.equals(that._name)) {
                    
                }
                return this._grade.equals(that._grade);
            }
            return false;
        }

        /** {@inheritDoc} */
        public int hashCode() {
            return toString().hashCode();
        }

        public String toString() {
            return _grade + "-" + _room + ":" + _name;
        }
    } // HomeRoom

    // =======================================================================

    private static class Course {
        private final String _courseCd;
        private final String _majorcd;
        private final String _courseCode;
        private final String _grade;

        /**
         * コンストラクタ。
         * @param courseCd 課程
         * @param majorcd 学科
         * @param courseCode コース
         * @param grade 学年
         */
        /* pkg */Course(
                final String courseCd,
                final String majorcd,
                final String courseCode,
                final String grade
        ) {
            _courseCd = courseCd;
            _majorcd = majorcd;
            _courseCode = courseCode;
            _grade = grade;
        }

        /** {@inheritDoc} */
        public boolean equals(final Object obj) {
            if (obj instanceof Course) {
                final Course that = (Course) obj;
                if (!this._courseCd.equals(that._courseCd)) {
                    return false;
                }
                if (!this._majorcd.equals(that._majorcd)) {
                    return false;
                }
                if (this._courseCode.equals(that._courseCode)) {
                    
                }
                return this._grade.equals(that._grade);
            }
            return false;
        }

        /** {@inheritDoc} */
        public int hashCode() {
            return toString().hashCode();
        }

        /** {@inheritDoc} */
        public String toString() {
            return _courseCd + "_" + _majorcd + "_" + _courseCode + "_" + _grade;
        }
    } // Course

    // =======================================================================
    private static String getDataKey(final Param param, final Point point, final SubClass subClass) {
        if (point._kind == 1 && !param.isSelectHomeroom()) {
            return subClass._subclassChairCd;
        }
        return subClass._subclassCd;
    }

    private static class Point {
        final int _kind;
        final String _title;
        private final Map _dataMap = new HashMap(); // <key, Map<Schregno, Score> // デバッグ用
        private final Map _rankMap = new HashMap(); // <キー,得点のリスト>のマップ
        private final Map _avgMap = new HashMap(); // <key, Map<Schregno, Score> // デバッグ用
        private final Map _avgRankMap = new HashMap(); // <key, 平均のリスト>のマップ
        private final Map _stdDevMap = new HashMap(); // <科目コードもしくは講座コード,偏差値のリスト>のマップ
        private final Map _stdDevRankMap = new HashMap(); // <key, 平均のリスト>のマップ

        Point(final int kind, final String title) {
            _kind = kind;
            _title = title;
        }

        /**
         * キーの得点リストのサイズを得る
         * @param key キー
         * @return キーの得点リストのサイズ
         */
        public int rankSize(final Map rankMap, final String key) {
            final List rtn = (List) rankMap.get(key);
            if (null == rtn) {  // TODO: null の場合は、どういう状態か?仕様も含めて調査せよ
                return -1;
            }
            return rtn.size();
        }

        /**
         * キーの得点リストのvalの順位を得る
         * @param key キー
         * @param val 得点
         * @return キーの得点リストの得点の順位（得点リストが無いもしくは得点が無い場合は-1）
         */
        public int rank(final Map rankMap, final String key, final Number val) {
            final List rtn = (List) rankMap.get(key);
            if (null == rtn || !rtn.contains(val)) {
                return -1;
            }
            return 1 + rtn.indexOf(val);
        }

        /**
         * キーの偏差値リストのvalの偏差値を得る
         * @param val 得点
         * @param key キー
         * @return キーの偏差値リストの得点の偏差値（偏差値リストが無い場合は-1）
         */
        public String getStdDev(final Number val, final String key) {
            final Map map = (Map) _stdDevMap.get(key);
            if (map == null || null == map.get(val)) {
                return null;
            }
            return map.get(val).toString();
        }

        public String toString() {
            return _dataMap.keySet().isEmpty() ? "[]" : _dataMap.toString();
        }
    } // Point
    
    public static class KNJD102HCategoryAxis extends CategoryAxis {

        /**
         * Creates a temporary list of ticks that can be used when drawing the axis.
         *
         * @param g2  the graphics device (used to get font measurements).
         * @param state  the axis state.
         * @param dataArea  the area inside the axes.
         * @param edge  the location of the axis.
         * 
         * @return A list of ticks.
         */
        public List refreshTicks(Graphics2D g2, 
                                 AxisState state,
                                 Rectangle2D dataArea,
                                 RectangleEdge edge) {

            List ticks = new java.util.ArrayList();
            
            // sanity check for data area...
            if (dataArea.getHeight() <= 0.0 || dataArea.getWidth() < 0.0) {
                return ticks;
            }

            CategoryPlot plot = (CategoryPlot) getPlot();
            List categories = plot.getCategoriesForAxis(this);
            double max = 0.0;
                    
            if (categories != null) {
                CategoryLabelPosition position = this.getCategoryLabelPositions().getLabelPosition(edge);
                float r = this.getMaximumCategoryLabelWidthRatio();
                if (r <= 0.0) {
                    r = position.getWidthRatio();   
                }
                      
                float l = 0.0f;
                if (position.getWidthType() == CategoryLabelWidthType.CATEGORY) {
                    l = (float) calculateCategorySize(categories.size(), dataArea, edge);  
                } else {
                    if (RectangleEdge.isLeftOrRight(edge)) {
                        l = (float) dataArea.getWidth();   
                    } else {
                        l = (float) dataArea.getHeight();   
                    }
                }
                int categoryIndex = 0;
                Iterator iterator = categories.iterator();
                while (iterator.hasNext()) {
                    Comparable category = (Comparable) iterator.next();
                    TextBlock label = createLabel(category, l * r, edge, g2);
                    if (edge == RectangleEdge.TOP || edge == RectangleEdge.BOTTOM) {
                        max = Math.max(max, calculateTextBlockHeight(label, position, g2));
                    } else if (edge == RectangleEdge.LEFT || edge == RectangleEdge.RIGHT) {
                        max = Math.max(max, calculateTextBlockWidth(label, position, g2));
                    }
                    Tick tick = new CategoryTick(category, label, position.getLabelAnchor(), position.getRotationAnchor(), position.getAngle());
                    ticks.add(tick);
                    categoryIndex = categoryIndex + 1;
                }
            }
            state.setMax(max);
            // return ticks;
            
            // 以下を追加
            final List newticks = new java.util.ArrayList();
            for (final Iterator it = ticks.iterator(); it.hasNext();) {
                final CategoryTick tick = (CategoryTick) it.next();
                final TextBlock tb = tick.getLabel();
                final TextBlock ntb = new TextBlock();
                for (final Iterator it2 = tb.getLines().iterator(); it2.hasNext();) {
                    final TextLine tl = (TextLine) it2.next();
                    final TextFragment fr = tl.getFirstTextFragment();
                    final String s = fr.getText();
                    if ("...".equals(StringUtils.trim(s))) {
                        continue;
                    }
                    for (int i = 0, len = s.length(); i < len; i++) {
                        final String ch = String.valueOf(s.charAt(i));
                        TextLine ntl = new TextLine();
                        ntl.addFragment(new TextFragment(ch));
                        ntb.addLine(ntl);
                    }
                }
                
                final Tick newtick = new CategoryTick(tick.getCategory(), ntb, tick.getLabelAnchor(), tick.getRotationAnchor(), tick.getAngle());
                newticks.add(newtick);
            }
            
            return newticks;
            
        }
    }

    public static class KNJD102HStandardCategoryItemLabelGenerator extends StandardCategoryItemLabelGenerator {
        final Map _scoreDataMap;
        public KNJD102HStandardCategoryItemLabelGenerator(final Map scoreDataMap) {
            _scoreDataMap = scoreDataMap;
        }
        /**
         * Generates an item label.
         * 
         * @param dataset  the dataset.
         * @param series  the series index.
         * @param category  the category index.
         * 
         * @return the label.
         */
        public String generateLabel(final CategoryDataset dataset, final int series, final int category) {
            final int row = series;
            final int column = category;
            final Comparable rowKey = dataset.getRowKey(row);
            final Comparable columnKey = dataset.getColumnKey(column);
            // log.debug(" row = " + rowKey + ", columnKey = " + columnKey);
            // final Number value = dataset.getValue(row, column);
            final Map subclassScoreMap = (Map) _scoreDataMap.get(rowKey);
            if (null != subclassScoreMap) {
                final Number bd = (Number) subclassScoreMap.get(columnKey);
                if (null != bd) {
                    return bd.toString();
                }
            }
            return "";
        }
    }
    
    private Param createParam(final HttpServletRequest request, final DB2UDB db2) {
        log.fatal("$Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        return new Param(request, db2);
    }

    private static class Param {
        final String _year;         // YEAR
        final String _gakki;        // GAKKI
        final String _hr;           // GradeHomeRoom
        final String _testKind;     // TESTKINDCD
        final String _date;         // 日付(2006-01-12)
        final String[] _schregs;    // 学籍番号の列挙
        final String _scoreField;         // 得点のRECORD_DATのフィールド名(SEM1_INTR_SCOREなど)
        final String _chaircdField; // RECORD_DATのフィールド名設定
        final String _countFlgTable;// テスト名称テーブル
        final String _rankBase;     // 順位の基準（平均点/偏差値）
        final String _rankDiv;      // 総合順位出力（コース/学年）
        final String _useCurriculumcd; // 教育課程コード使用フラグ
        final String _useVirus;
        final String _useKoudome;
        final String _useJFreeChart;
        private String _gakkiName;  // 学期名称
        private String _gakkiFrom;  // 学期期間FROM
        private String _gakkiTo;    // 学期期間TO
        private String _tannin;     // 学級担任名
        private String _jpDate;     // 和暦作成日(平成14年10月27日)
        private String _testName;

        private final KNJDefineSchool _defineCode = new KNJDefineSchool();    // 各学校における定数等設定

        private KNJSchoolMst _knjSchoolMst;
        private String _sdate;
        
        final boolean _enableGraph;
        final Set _graphFiles = new HashSet();


        /* pkg */Param(final HttpServletRequest request, final DB2UDB db2) {
            _enableGraph = isEnableGraph();
            _year = request.getParameter("YEAR"); // 年度
            _gakki = request.getParameter("GAKKI"); // 1-3:学期
            _hr = request.getParameter("GRADE_HR_CLASS"); // 学年・組
            _testKind = request.getParameter("TESTKINDCD"); // 中間:01,期末:99
            _countFlgTable = request.getParameter("COUNTFLG");
            _date = KNJ_EditDate.H_Format_Haifun(request.getParameter("DATE")); // 異動基準日
            _schregs = request.getParameterValues("category_selected");
            _rankBase = request.getParameter("RANK_BASE");
            _rankDiv = request.getParameter("RANK_DIV");

            _scoreField = getItem() + "_SCORE";  // Ex) SEM1_TERM_SCORE
            _chaircdField = getItem() + "_CHAIRCD";    // Ex) SEM1_TERM_CHAIRCD
//            log.debug("   scoreField = " + _scoreField);
//            log.debug(" chaircdField = " + _chaircdField);
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _useVirus = request.getParameter("useVirus");
            _useKoudome = request.getParameter("useKoudome");
            _useJFreeChart = request.getParameter("useJFreeChart");
            
            try {
                _defineCode.defineCode(db2, _year); // 各学校における定数等設定
                log.debug("semesdiv=" + _defineCode.semesdiv + "   absent_cov=" + _defineCode.absent_cov + "   absent_cov_late=" + _defineCode.absent_cov_late);
                log.debug("学校区分(schoolDiv)=" + _defineCode.schooldiv + ", 学年制か？=" + isGakunenSei());
                _knjSchoolMst = new KNJSchoolMst(db2, _year);
                
                setTestName(db2);
                setHead(db2);
                loadSemester(db2);
            } catch (SQLException e) {
                log.error("exception!", e);
            }
        }

        private String getItem() {
            final String testKind;
            if ("0101".equals(_testKind)) {
                testKind = "INTR";
            } else {
                testKind = ("0201".equals(_testKind)) ? "TERM" : "TERM2";
            }

            return "SEM" + _gakki + "_" + testKind ;
        }

        private boolean isSelectHomeroom() {
            return isGakunenSei();
        }

        private boolean isGakunenSei() {
            return "0".equals(_defineCode.schooldiv);
        }

        private void setTestName(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = KNJ_Testname.getTestNameSql(_countFlgTable, _year, _gakki, _testKind);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    _testName = rs.getString("TESTITEMNAME");
                }
            } catch (final SQLException e) {
                log.error("テスト名称の読込みでエラー", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        private void removeImageFiles() {
            for (final Iterator it = _graphFiles.iterator(); it.hasNext();) {
                final File imageFile = (File) it.next();
                if (null == imageFile) {
                    continue;
                }
                log.fatal("グラフ画像ファイル削除:" + imageFile.getName() + " : " + imageFile.delete());
            }
        }
        
        private KNJDefineCode setClasscode0(final DB2UDB db2) {
            KNJDefineCode definecode = null;
            try {
                definecode = new KNJDefineCode();
                definecode.defineCode(db2, _year);         //各学校における定数等設定
            } catch (Exception ex) {
                log.warn("semesterdiv-get error!", ex);
            }
            return definecode;
        }

        /**
         * 名称マスタ NAMECD1='Z010' NAMECD2='00'読込
         */
        private String setZ010Name1(final DB2UDB db2) {
            String name1 = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement("SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' ");
                rs = ps.executeQuery();
                if (rs.next()) {
                    name1 = rs.getString("NAME1");
                }
            } catch (SQLException ex) {
                log.debug("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return name1;
        }
        
        /**
         * SVF-FORMセット＆見出し項目
         */
        private void setHead(
                final DB2UDB db2
        ) {
            KNJ_Get_Info getinfo = new KNJ_Get_Info();
            KNJ_Get_Info.ReturnVal returnval = null;

            try {
                returnval = getinfo.Semester(db2, _year, _gakki);
                _gakkiName = returnval.val1; // 学期名称
                _gakkiFrom = returnval.val2; // 学期期間FROM
                _gakkiTo = returnval.val3; // 学期期間TO
            } catch (final Exception ex) {
                log.error("error! ", ex);
            } finally {
                if (_gakkiName == null) _gakkiName = "  学期";
                if (_gakkiFrom == null) _gakkiFrom = _year + "-04-01";
                if (_gakkiTo == null)   _gakkiTo = (Integer.parseInt(_year) + 1) + "-03-31";
            }

            returnval = getinfo.Staff_name(db2, _year, _gakki, _hr, "");
            _tannin = returnval.val1; // 学級担任名

            returnval = getinfo.Control(db2);
            _jpDate = KNJ_EditDate.h_format_JP(returnval.val3); // 作成日

            getinfo = null;
            returnval = null;
        }
        
        /**
         * 学期マスタ (SEMESTER_MST) をロードする
         * @param db2
         */
        private void loadSemester(final DB2UDB db2) {
            
            final String sql = "SELECT SEMESTER, SEMESTERNAME, SDATE, EDATE FROM SEMESTER_MST "
                + " WHERE YEAR = '" + _year + "' AND SEMESTER <> '9' ORDER BY SEMESTER";
            //log.debug(" semester sql = " + sql);
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                boolean first = true;
                while (rs.next()) {
                    String sdate = rs.getString("SDATE");
                    if (first) {
                        _sdate = sdate;
                        first = false;
                    }
                }
                
            } catch (Exception ex) {
                log.error("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
        }
        
        /*
         * グラフが使える環境か調べる。
         * ----
         * 以下の条件に該当する場合、Error が発生する。Error を catch し、グラフ機能を無効にする。
         * 
         * ○賢者Base1:
         *      X-Windowsが起動していない。かつ DISPLAY が設定されていない。
         *      以下の Error が発生する。
         *          java.lang.NoClassDefFoundError: sun/awt/X11GraphicsEnvironment
         *
         * ○賢者Base2:
         *      WAS に「java -Djava.awt.headless=true」の設定が施されていない。
         *      以下の Error が発生する。(尚、InternalError は初回起動時のみで、以降は NoClassDefFoundError が発生する)
         *          java.lang.InternalError: Can't connect to X11 window server using ':0.0' as the value of the DISPLAY variable.
         */
        private static boolean isEnableGraph() {
            try {
                GraphicsEnvironment.getLocalGraphicsEnvironment();
            } catch (final NoClassDefFoundError e) {
                // グラフを使用できない
                log.error("グラフを使用できません。: " + e);
                return false;
            } catch (final InternalError e) {
                // グラフを使用できない
                log.error("グラフを使用できません。: " + e);
                return false;
            } catch (final Throwable e) {
                // グラフを使用できない
                log.fatal("想定外の例外が発生: " + e);
                return false;
            }

            // グラフを使用できる
            log.fatal("グラフを使用できる環境です。");
            return true;
        }
    } // Param
} // KNJD102H
