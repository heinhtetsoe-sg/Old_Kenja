/**     プロジェクト/プロパティ/javaコード・スタイル/コード・テンプレート/パターン       ***/

/*
 *
 * 作成日: 2021/04/02
 * 作成者: ishimine
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.awt.BasicStroke;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.RectangleEdge;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KnjDbUtils;

public class KNJL431M {

    private static final Log log = LogFactory.getLog(KNJL431M.class);

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

            if ("01".equals(_param._examCd)) {
                printKikoku(db2, svf); //帰国生入試
            } else {
                printIppan(db2, svf); //一般入試
            }

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

            if (null != _param && "02".equals(_param._examCd)) {
                int count = 0;
                for (final Iterator it = _param._graphFiles.iterator(); it.hasNext();) {
                    final File imageFile = (File) it.next();
                    if (null == imageFile) {
                        continue;
                    }
                    boolean deleted = imageFile.delete();
                    if (deleted) {
                        count += 1;
                    }
                }
                log.fatal("グラフ画像ファイル削除:" + count + "件");
            }
            svf.VrQuit();
        }
    }

    //帰国生入試帳票
    private void printKikoku(final DB2UDB db2, final Vrw32alp svf) {
        final Map<String, Kikoku> printKikokuMap = getPrintKikokuMap(db2);
        if (printKikokuMap.isEmpty()) return;

        final Map<String, Total> kikokuTotalMap = getKikokuTotalMap(db2);

        int kikokuCnt = printKikokuMap.size();
        int cnt = 1;
        int field = 1;
        final int MAX_LINE  = 70; //最大行
        final int LAST_LINE = 60; //最大行（最後のページ）
        boolean lastPage = false;
        int hanteiLine = MAX_LINE; //判定用のページ最大行

        if (kikokuCnt <= LAST_LINE) {
            svf.VrSetForm("KNJL431M_2.frm", 4);
            lastPage = true;
            hanteiLine = LAST_LINE;
            printKikokuTotal(svf, kikokuTotalMap);
        } else {
            svf.VrSetForm("KNJL431M_1.frm", 4);
        }
        printTitle(svf, lastPage);

        for (Iterator<Kikoku> ite = printKikokuMap.values().iterator(); ite.hasNext();) {
            final Kikoku kikoku = ite.next();
            if (cnt > hanteiLine) {
                svf.VrEndPage();
                if (kikokuCnt <= (LAST_LINE)) {
                    svf.VrSetForm("KNJL431M_2.frm", 4);
                    lastPage = true;
                    hanteiLine = LAST_LINE;
                    printKikokuTotal(svf, kikokuTotalMap);

                } else {
                    svf.VrSetForm("KNJL431M_1.frm", 4);
                }
                printTitle(svf, lastPage);
                field = 1;
                cnt = 1;
            }

            svf.VrsOut("EXAM_NO" + field, kikoku._receptno);

            int subclassCnt = 1;
            int interviewCnt = 1;

            //面接、科目別得点
            for (Iterator<Score> scoreIte = kikoku.scoreMap.values().iterator(); scoreIte.hasNext();) {
                final Score score = scoreIte.next();
                if (_param._interviewCdList.contains(score._exam_Subclass)) {
                    svf.VrsOut("INTERVIEW1_" + interviewCnt, score._label);
                    interviewCnt++;
                } else {
                    if (!"1".equals(score._absenceFlg)) {
                        if (score._exam_Score != null) {
                        	final BigDecimal bd1 = new BigDecimal(score._exam_Score);
                        	svf.VrsOut("SCORE1_" + subclassCnt, bd1.setScale(0, RoundingMode.DOWN).toString());
                        }
                    } else {
                        svf.VrsOut("SCORE1_" + subclassCnt, "欠");
                    }
                    subclassCnt++;
                }
            }

            if (kikoku._total != null) {
                final BigDecimal bd1 = new BigDecimal(kikoku._total);
                svf.VrsOut("TOTAL_SCORE1", bd1.setScale(0, RoundingMode.DOWN).toString());
            }
            if (kikoku._rank != null) {
                final BigDecimal bd1 = new BigDecimal(kikoku._rank);
                svf.VrsOut("RANK1", bd1.setScale(0, RoundingMode.DOWN).toString());
            }

            svf.VrsOut("INTERVIEW_ROOM1", kikoku._placeId);
            svf.VrsOut("REMARK1", kikoku._remark);
            _hasData = true;
            svf.VrEndRecord();
            cnt++;
            kikokuCnt--;
        }

        if (!lastPage) {
            svf.VrEndPage();
            svf.VrSetForm("KNJL431M_2.frm", 4);
            printTitle(svf, true);
            printKikokuTotal(svf, kikokuTotalMap);
            svf.VrsOut("DUMMY", "dummy");
            svf.VrEndRecord();
        }
        svf.VrEndPage();
    }

    private void printKikokuTotal(final Vrw32alp svf, final Map<String, Total> totalMap) {
        int cnt = 1;
        for (Iterator<String> ite = totalMap.keySet().iterator(); ite.hasNext();) {
            final String cd = ite.next();
            final Total total = totalMap.get(cd);
            if (total != null) {
                svf.VrsOutn("TOTAL1_" + cnt, 1, getPrintScore(total._avg));
                svf.VrsOutn("TOTAL1_" + cnt, 2, getPrintScore(total._max));
                svf.VrsOutn("TOTAL1_" + cnt, 3, getPrintScore(total._min));
            }
            cnt++;
        }
    }

    private String getPrintScore(final String str) {
        String retStr = "";
        if (str != null) {
            final BigDecimal bd1 = new BigDecimal(str);
            retStr = bd1.setScale(1, BigDecimal.ROUND_UP).toString();
        }
        return retStr;
    }

    //一般入試帳票
    private void printIppan(final DB2UDB db2, final Vrw32alp svf) {
        final Map<String, Ippan> ippanMap = getIppanMap(db2, false);
        if (ippanMap.isEmpty()) return;

        final int maxCnt; //1ページの最大印字数
        final String sort = "1".equals(_param._sortDiv) ? "受験番号順" : "合計点順";
        if (_param._subclassMap.size() <= 2) {
            svf.VrSetForm("KNJL431M_3.frm", 4);
            svf.VrsOut("SUBTITLE", "選考資料　（２科目" + sort + "）");
            maxCnt = 120;
        } else {
            svf.VrSetForm("KNJL431M_4.frm", 4);
            svf.VrsOut("SUBTITLE", "選考資料　（４科目" + sort + "）");
            maxCnt = 90;
        }

        final String kousyu = "H".equals(_param._schoolKind) ? "高等学校" : "中学校";
        svf.VrsOut("TITLE", _param._nendo + " 東京女学館 " + kousyu + " 入学試験（一般学級）");

        //科目名
        int subCnt = 1;
        for (Iterator<Subclass> subIte = _param._subclassMap.values().iterator(); subIte.hasNext();) {
            final Subclass subclass = subIte.next();
            svf.VrsOut("SUBCLASS_NAME1_" + subCnt, subclass._subclassName);
            svf.VrsOut("SUBCLASS_NAME2_" + subCnt, subclass._subclassName);
            svf.VrsOut("SUBCLASS_NAME3_" + subCnt, subclass._subclassName);
            subCnt++;
        }

        int cnt = 1;
        int page = 1;
        svf.VrsOut("PAGE", "Page　" + String.valueOf(page)); //ページ数
        for (Iterator<Ippan> ite = ippanMap.values().iterator(); ite.hasNext();) {
            if (cnt > maxCnt) {
                svf.VrsOut("PAGE", "Page　" + String.valueOf(++page)); //ページ数
                cnt = 1;
            }

            final Ippan ippan = ite.next();
            svf.VrsOut("EXAM_NO1", ippan._receptno); //番号
            if (ippan._total != null) {
                final BigDecimal bd1 = new BigDecimal(ippan._total);
                svf.VrsOut("TOTAL_SCORE1", bd1.setScale(0, RoundingMode.DOWN).toString()); //合計
            }
            if (ippan._rank != null) {
                final BigDecimal bd1 = new BigDecimal(ippan._rank);
                svf.VrsOut("RANK1", bd1.setScale(0, RoundingMode.DOWN).toString()); //順位
            }
            svf.VrsOut("INTERVIEW1_1", ippan._remark1); //備考1
            svf.VrsOut("INTERVIEW1_2", ippan._remark2); //備考2

            subCnt = 1;
            for (Iterator<Subclass> subIte = _param._subclassMap.values().iterator(); subIte.hasNext();) {
                final Subclass subclass = subIte.next();
                final Score score = ippan.scoreMap.get(subclass._cd);
                if (score != null) {
                    if (!"1".equals(score._absenceFlg)) {
                        svf.VrsOut("SCORE1_" + subCnt, score._exam_Score); //点数
                    } else {
                        svf.VrsOut("SCORE1_" + subCnt, "欠"); //点数
                    }
                }
                subCnt++;
            }
            svf.VrEndRecord();
            _hasData = true;
            cnt++;

        }
        svf.VrEndPage();

        printBunpu(db2, svf);
    }

    //得点分布表
    private void printBunpu(final DB2UDB db2, final Vrw32alp svf) {
        final Map<String, Bunpu> bunpuMap = getIppanMap(db2, true); //分布表取得
        final Map<String, Total> totalMap = getIppanTotalMap(db2); //統計取得

        svf.VrSetForm("KNJL431M_5.frm", 1);

        int cnt = 1;
        String graph1Name = "";
        String graph2Name = "";
        //科目名 統計
        BigDecimal totalBd = new BigDecimal("0.00");
        for (Iterator<Subclass> subIte = _param._subclassMap.values().iterator(); subIte.hasNext();) {
            final Subclass subclass = subIte.next();
            svf.VrsOut("SUBCLASS_NAME" + subclass._field, subclass._subclassName);
            svf.VrsOut("SUBCLASS_NAME2_" + subclass._field, subclass._subclassName);
            if(cnt <= 2) {
                graph1Name += "".equals(graph1Name) ? subclass._subclassName : "・" + subclass._subclassName;
            } else {
                graph2Name += "".equals(graph2Name) ? subclass._subclassName : "・" + subclass._subclassName;
            }

            int line = 1;
            final Total total = totalMap.get(subclass._cd);
            if (total != null) {
                log.info(total._shigan);
                svf.VrsOutn("SCORE" + subclass._field, line++, total._shigan);
                svf.VrsOutn("SCORE" + subclass._field, line++, total._juken);
                svf.VrsOutn("SCORE" + subclass._field, line++, total._kesseki);
                svf.VrsOutn("SCORE" + subclass._field, line++, total._max);
                svf.VrsOutn("SCORE" + subclass._field, line++, total._min);
                svf.VrsOutn("SCORE" + subclass._field, line++, total._avg);
                svf.VrsOutn("SCORE" + subclass._field, line++, total._stddev);
                if (total._avg != null) {
                    totalBd = totalBd.add(new BigDecimal(String.valueOf(total._avg)));
                }
            }
            cnt++;
            if (cnt > 4) break; //科目は4教科まで
        }
        if (!"0.00".equals(totalBd.toPlainString())) {
            svf.VrsOutn("TOTAL_SCORE", 6, totalBd.toPlainString()); //平均合計
        }

        //グラフ作成
        DefaultCategoryDataset scoreDataset1 = new DefaultCategoryDataset();
        DefaultCategoryDataset scoreDataset2 = new DefaultCategoryDataset();
        for (Iterator<Bunpu> ite = bunpuMap.values().iterator(); ite.hasNext();) {
            final Bunpu bunpu = ite.next();
            final Subclass subclass = (Subclass)_param._subclassMap.get(bunpu._testSubclassCd);
            if (subclass == null) {
            	continue;
            }

            if (subclass._field <= 2) { //1つのオブジェクトにグラフデータは2教科設定
                scoreDataset1 = barDataset(scoreDataset1, bunpu);
            } else {
                scoreDataset2 = barDataset(scoreDataset2, bunpu);
            }

            //分布表
            int line = 1;
            svf.VrsOutn("FREQ" + subclass._field, line++, bunpu._bunpu21);
            svf.VrsOutn("FREQ" + subclass._field, line++, bunpu._bunpu20);
            svf.VrsOutn("FREQ" + subclass._field, line++, bunpu._bunpu19);
            svf.VrsOutn("FREQ" + subclass._field, line++, bunpu._bunpu18);
            svf.VrsOutn("FREQ" + subclass._field, line++, bunpu._bunpu17);
            svf.VrsOutn("FREQ" + subclass._field, line++, bunpu._bunpu16);
            svf.VrsOutn("FREQ" + subclass._field, line++, bunpu._bunpu15);
            svf.VrsOutn("FREQ" + subclass._field, line++, bunpu._bunpu14);
            svf.VrsOutn("FREQ" + subclass._field, line++, bunpu._bunpu13);
            svf.VrsOutn("FREQ" + subclass._field, line++, bunpu._bunpu12);
            svf.VrsOutn("FREQ" + subclass._field, line++, bunpu._bunpu11);
            if ("100".equals(subclass._value)) { //満点マスタで100なら
                svf.VrsOutn("FREQ" + subclass._field, line++, bunpu._bunpu10);
                svf.VrsOutn("FREQ" + subclass._field, line++, bunpu._bunpu09);
                svf.VrsOutn("FREQ" + subclass._field, line++, bunpu._bunpu08);
                svf.VrsOutn("FREQ" + subclass._field, line++, bunpu._bunpu07);
                svf.VrsOutn("FREQ" + subclass._field, line++, bunpu._bunpu06);
                svf.VrsOutn("FREQ" + subclass._field, line++, bunpu._bunpu05);
                svf.VrsOutn("FREQ" + subclass._field, line++, bunpu._bunpu04);
                svf.VrsOutn("FREQ" + subclass._field, line++, bunpu._bunpu03);
                svf.VrsOutn("FREQ" + subclass._field, line++, bunpu._bunpu02);
                svf.VrsOutn("FREQ" + subclass._field, line++, bunpu._bunpu01);
            } else {
                svf.VrsOutn("FREQ" + subclass._field, line++, "-----");
                svf.VrsOutn("FREQ" + subclass._field, line++, "-----");
                svf.VrsOutn("FREQ" + subclass._field, line++, "-----");
                svf.VrsOutn("FREQ" + subclass._field, line++, "-----");
                svf.VrsOutn("FREQ" + subclass._field, line++, "-----");
                svf.VrsOutn("FREQ" + subclass._field, line++, "-----");
                svf.VrsOutn("FREQ" + subclass._field, line++, "-----");
                svf.VrsOutn("FREQ" + subclass._field, line++, "-----");
                svf.VrsOutn("FREQ" + subclass._field, line++, "-----");
                svf.VrsOutn("FREQ" + subclass._field, line++, "-----");
            }
        }

        //分布毎のループ
        printChart(svf, scoreDataset1, "GRAPH2", "1", graph1Name);
        if (cnt > 3) printChart(svf, scoreDataset2, "GRAPH1", "2", graph2Name);
        svf.VrEndPage();
    }

    //グラフデータ登録
    private DefaultCategoryDataset barDataset(final DefaultCategoryDataset scoreDataset, final Bunpu bunpu) {
        final Subclass subclass = (Subclass)_param._subclassMap.get(bunpu._testSubclassCd);

        scoreDataset.addValue(Integer.parseInt(bunpu._bunpu21), subclass._subclassName, "0~4");
        scoreDataset.addValue(Integer.parseInt(bunpu._bunpu20), subclass._subclassName, "5~9");
        scoreDataset.addValue(Integer.parseInt(bunpu._bunpu19), subclass._subclassName, "10~14");
        scoreDataset.addValue(Integer.parseInt(bunpu._bunpu18), subclass._subclassName, "15~19");
        scoreDataset.addValue(Integer.parseInt(bunpu._bunpu17), subclass._subclassName, "20~24");
        scoreDataset.addValue(Integer.parseInt(bunpu._bunpu16), subclass._subclassName, "25~29");
        scoreDataset.addValue(Integer.parseInt(bunpu._bunpu15), subclass._subclassName, "30~34");
        scoreDataset.addValue(Integer.parseInt(bunpu._bunpu14), subclass._subclassName, "35~39");
        scoreDataset.addValue(Integer.parseInt(bunpu._bunpu13), subclass._subclassName, "40~44");
        scoreDataset.addValue(Integer.parseInt(bunpu._bunpu12), subclass._subclassName, "45~49");
        scoreDataset.addValue(Integer.parseInt(bunpu._bunpu11), subclass._subclassName, "50~54");
        if ("100".equals(subclass._value)) { //満点マスタで100なら
            scoreDataset.addValue(Integer.parseInt(bunpu._bunpu10), subclass._subclassName, "55~59");
            scoreDataset.addValue(Integer.parseInt(bunpu._bunpu09), subclass._subclassName, "60~64");
            scoreDataset.addValue(Integer.parseInt(bunpu._bunpu08), subclass._subclassName, "65~69");
            scoreDataset.addValue(Integer.parseInt(bunpu._bunpu07), subclass._subclassName, "70~74");
            scoreDataset.addValue(Integer.parseInt(bunpu._bunpu06), subclass._subclassName, "75~79");
            scoreDataset.addValue(Integer.parseInt(bunpu._bunpu05), subclass._subclassName, "80~84");
            scoreDataset.addValue(Integer.parseInt(bunpu._bunpu04), subclass._subclassName, "85~89");
            scoreDataset.addValue(Integer.parseInt(bunpu._bunpu03), subclass._subclassName, "90~94");
            scoreDataset.addValue(Integer.parseInt(bunpu._bunpu02), subclass._subclassName, "95~99");
            scoreDataset.addValue(Integer.parseInt(bunpu._bunpu01), subclass._subclassName, "100点");
        }
        return scoreDataset;
    }

    private JFreeChart createBarChart(
            final DefaultCategoryDataset scoreDataset,
            final String cnt,
            final String graphName
    ) {
        final JFreeChart chart = ChartFactory.createBarChart(graphName + "得点分布表", null, null, scoreDataset, PlotOrientation.VERTICAL, true, false, false);

        final CategoryPlot plot = chart.getCategoryPlot();
        plot.setRangeGridlineStroke(new BasicStroke(2.0f)); //目盛りの太さ
        plot.getDomainAxis().setTickLabelsVisible(true); //横軸ラベル表示
        plot.setRangeGridlinePaint(Color.black); //線の色
        plot.setBackgroundPaint(Color.white); //背景色

        BarRenderer barRenderer = new BarRenderer();
        barRenderer.setItemMargin(0.0); // カテゴリ内のバーの間隔
        plot.setRenderer(barRenderer);

        final NumberAxis numberAxis = new NumberAxis();
        numberAxis.setLabel("人数"); //Y軸ラベル
        numberAxis.setLabelAngle(1.58); //ラベル向き
        if ("1".equals(cnt)) {
            numberAxis.setTickUnit(new NumberTickUnit(2)); //Y軸目盛り間隔
            numberAxis.setRange(0, 16); //Y軸範囲
        } else {
            numberAxis.setTickUnit(new NumberTickUnit(5));
            numberAxis.setRange(0, 25);
        }
        numberAxis.setTickLabelsVisible(true); //縦軸ラベル表示
        plot.setRangeAxis(numberAxis);


        final CategoryItemRenderer renderer = plot.getRenderer();
        renderer.setSeriesPaint(0, Color.darkGray);

        ((BarRenderer) renderer).setMaximumBarWidth(0.05);

        chart.setBackgroundPaint(Color.white);
        chart.getLegend().setPosition(RectangleEdge.RIGHT); // 凡例の位置

        return chart;
    }

    private static File graphImageFile(final JFreeChart chart, final int dotWidth, final int dotHeight) {
        final String tmpFileName = KNJServletUtils.createTmpFile(".png");

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
        return (int) (pixel * 1.1);
    }

    private void printChart(final Vrw32alp svf, final DefaultCategoryDataset scoreDataset, final String field, final String cnt, final String graphName) {
        try {
            // チャート作成
            final JFreeChart chart = createBarChart(scoreDataset, cnt, graphName);

            // グラフのファイルを生成
            final File outputFile;
            if ("1".equals(cnt)) {
                outputFile = graphImageFile(chart, 5000, 2000);
            } else {
                outputFile = graphImageFile(chart, 3000, 1700);
            }

            _param._graphFiles.add(outputFile);

            // グラフの出力
            if (outputFile.exists()) {
                svf.VrsOut(field, outputFile.toString());
            }
        } catch (Throwable e) {
            log.error("exception or error!", e);
        }
    }

    private void printTitle(final Vrw32alp svf, final boolean lastFlg) {
        if ("01".equals(_param._examCd)) {
            final String kousyu = "H".equals(_param._schoolKind) ? "高等学校" : "中学校";
            svf.VrsOut("TITLE", _param._nendo + " " + kousyu + "・" + _param._examName + "判定会議資料");
            int cnt = 1;
            for (Iterator<Subclass> ite = _param._subclassMap.values().iterator(); ite.hasNext();) {
                final Subclass subclass = ite.next();
                if (subclass._value != null) { //面接以外の科目
                    svf.VrsOut("SUBCLASS_NAME1_" + cnt, subclass._subclassName + "(" + subclass._value + ")");
                    svf.VrsOut("SUBCLASS_NAME2_" + cnt, subclass._subclassName + "(" + subclass._value + ")");
                    if (lastFlg) {
                        svf.VrsOut("SUBCLASS_NAME1_" + (cnt + 2), subclass._subclassName);
                    }
                    cnt++;
                    if (cnt > 2) break;
                }
            }
        }
    }

    private Map getPrintKikokuMap(final DB2UDB db2) {
        final Map<String, Kikoku> retMap = new LinkedHashMap();
        PreparedStatement ps = null;
        ResultSet rs = null;
        final String sql = getKikokuSql();
        log.debug(" sql =" + sql);
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {

                final String receptno = rs.getString("RECEPTNO");

                if (!retMap.containsKey(receptno)) {
                    final String year = rs.getString("YEAR");
                    final String examno = rs.getString("EXAMNO");
                    final String exam_School_Kind = rs.getString("EXAM_SCHOOL_KIND");
                    final String applicant_Div = rs.getString("APPLICANT_DIV");
                    final String course_Div = rs.getString("COURSE_DIV");
                    final String frequency = rs.getString("FREQUENCY");
                    final String name = rs.getString("NAME");
                    final String remark = rs.getString("REMARK3");
                    final String rank = rs.getString("RANK");
                    final String total = rs.getString("TOTAL");
                    final String placeId = rs.getString("PLACE_ID");

                    final Kikoku kikoku = new Kikoku(year, examno, exam_School_Kind, applicant_Div, course_Div, frequency, receptno, name, rank, total, remark, placeId);
                    retMap.put(receptno, kikoku);
                }

                final Kikoku kikoku = retMap.get(receptno);
                final String exam_Subclass = rs.getString("EXAM_SUBCLASS");

                if (!kikoku.scoreMap.containsKey(exam_Subclass)) {
                    final String label = rs.getString("LABEL");
                    final String value = rs.getString("VALUE");
                    final String exam_Score = rs.getString("EXAM_SCORE");
                    final String absenceFlg = rs.getString("ABSENCE_FLG");

                    final Score score = new Score(exam_Subclass, label, value, exam_Score, absenceFlg);
                    kikoku.scoreMap.put(exam_Subclass, score);
                }
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retMap;
    }

    private Map getKikokuTotalMap(final DB2UDB db2) {
        final Map<String, Total> retMap = new LinkedHashMap();
        PreparedStatement ps = null;
        ResultSet rs = null;
        final String sql = getKikokuTotalSql();
        log.debug(" sql =" + sql);
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String cd = rs.getString("EXAM_SUBCLASS");

                if (!retMap.containsKey(cd)) {
                    final String max = rs.getString("MAX");
                    final String min = rs.getString("MIN");
                    final String avg = rs.getString("AVG");
                    final Total total = new Total(max, min, avg, null, null, null, null);
                    retMap.put(cd, total);
                }
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retMap;
    }

    //帰国生帳票用SQL
    private String getKikokuSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.YEAR, ");
        stb.append("     T1.EXAMNO, ");
        stb.append("     T1.EXAM_SCHOOL_KIND, ");
        stb.append("     T1.APPLICANT_DIV, ");
        stb.append("     T1.COURSE_DIV, ");
        stb.append("     T1.FREQUENCY, ");
        stb.append("     T1.RECEPTNO, ");
        stb.append("     T0.NAME, ");
        stb.append("     T0.REMARK3, ");
        stb.append("     T2.EXAM_SUBCLASS, ");
        stb.append("     T2.SCORE, ");
        stb.append("     T2.ABSENCE_FLG, ");
        stb.append("     T3.VALUE, ");
        stb.append("     T4.EXAM_SCORE, ");
        stb.append("     T5.EXAM_SCORE AS RANK, ");
        stb.append("     T6.EXAM_SCORE AS TOTAL, ");
        stb.append("     T7.LABEL, ");
        stb.append("     T8.PLACE_ID ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_STD_RECEPT_DAT T1 ");
        stb.append(" INNER JOIN ");
        stb.append("     ENTEXAM_STD_APPLICANTBASE_DAT T0 ");
        stb.append("      ON T0.YEAR = T1.YEAR ");
        stb.append("     AND T0.EXAMNO = T1.EXAMNO ");
        stb.append(" LEFT JOIN ");
        stb.append("     ENTEXAM_STD_SCORE_DAT T2 ");
        stb.append("      ON T2.YEAR = T1.YEAR ");
        stb.append("     AND T2.RECEPTNO = T1.RECEPTNO ");
        stb.append(" LEFT JOIN ");
        stb.append("     ENTEXAM_STD_PERFECT_MST T3 ");
        stb.append("      ON T3.YEAR = T1.YEAR ");
        stb.append("     AND T3.EXAM_SCHOOL_KIND = T1.EXAM_SCHOOL_KIND ");
        stb.append("     AND T3.APPLICANT_DIV = T1.APPLICANT_DIV ");
        stb.append("     AND T3.COURSE_DIV = T1.COURSE_DIV ");
        stb.append("     AND T3.FREQUENCY = T1.FREQUENCY ");
        stb.append("     AND T3.EXAM_SUBCLASS = T2.EXAM_SUBCLASS ");
        stb.append("     AND T3.STEPS = 'A' ");
        stb.append(" LEFT JOIN ");
        stb.append("     ENTEXAM_STD_RANK_DAT T4 ");
        stb.append("      ON T4.YEAR = T2.YEAR ");
        stb.append("     AND T4.RECEPTNO = T2.RECEPTNO ");
        stb.append("     AND T4.EXAM_SUBCLASS = T2.EXAM_SUBCLASS ");
        stb.append("     AND T4.SUMMARY_DIV = 'S' ");
        stb.append(" LEFT JOIN ");
        stb.append("     ENTEXAM_STD_RANK_DAT T5 ");
        stb.append("      ON T5.YEAR = T2.YEAR ");
        stb.append("     AND T5.RECEPTNO = T2.RECEPTNO ");
        stb.append("     AND T5.EXAM_SUBCLASS = '99' ");
        stb.append("     AND T5.SUMMARY_DIV = '2R' ");
        stb.append(" LEFT JOIN ");
        stb.append("     ENTEXAM_STD_RANK_DAT T6 ");
        stb.append("      ON T6.YEAR = T2.YEAR ");
        stb.append("     AND T6.RECEPTNO = T2.RECEPTNO ");
        stb.append("     AND T6.EXAM_SUBCLASS = '99' ");
        stb.append("     AND T6.SUMMARY_DIV = '2S' ");
        stb.append(" LEFT JOIN ");
        stb.append("     ENTEXAM_STD_PERFECT_MST T7 ");
        stb.append("      ON T7.YEAR = T1.YEAR ");
        stb.append("     AND T7.EXAM_SCHOOL_KIND = T1.EXAM_SCHOOL_KIND ");
        stb.append("     AND T7.APPLICANT_DIV = T1.APPLICANT_DIV ");
        stb.append("     AND T7.COURSE_DIV = T1.COURSE_DIV ");
        stb.append("     AND T7.FREQUENCY = T1.FREQUENCY ");
        stb.append("     AND T7.EXAM_SUBCLASS = T2.EXAM_SUBCLASS ");
        stb.append("     AND T7.VALUE = T2.SCORE ");
        stb.append(" LEFT JOIN ");
        stb.append("     ENTEXAM_STD_HALL_DAT T8 ");
        stb.append("      ON T8.YEAR = T1.YEAR ");
        stb.append("     AND T8.EXAM_SCHOOL_KIND = T1.EXAM_SCHOOL_KIND ");
        stb.append("     AND T8.APPLICANT_DIV = T1.APPLICANT_DIV ");
        stb.append("     AND T8.COURSE_DIV = T1.COURSE_DIV ");
        stb.append("     AND T8.FREQUENCY = T1.FREQUENCY ");
        stb.append("     AND T8.EXAMNO = T1.EXAMNO ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._examYear + "' AND ");
        stb.append("     T1.EXAM_SCHOOL_KIND || T1.APPLICANT_DIV || T1.COURSE_DIV || T1.FREQUENCY = '" + _param._examId + "' ");
        stb.append(" ORDER BY ");

        if ("1".equals(_param._sortDiv)) {
            stb.append("     T1.RECEPTNO, ");
        } else {
            stb.append("     RANK, ");
            stb.append("     T1.RECEPTNO, ");
        }
        stb.append("     T2.EXAM_SUBCLASS ");

        return stb.toString();
    }

    //帰国生集計SQL
    private String getKikokuTotalSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH RECEPTNO AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.YEAR, ");
        stb.append("     T1.EXAM_SCHOOL_KIND, ");
        stb.append("     T1.APPLICANT_DIV, ");
        stb.append("     T1.COURSE_DIV, ");
        stb.append("     T1.FREQUENCY, ");
        stb.append("     T1.RECEPTNO ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_STD_RECEPT_DAT T1 ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._examYear + "' AND ");
        stb.append("     T1.EXAM_SCHOOL_KIND || T1.APPLICANT_DIV || T1.COURSE_DIV || T1.FREQUENCY = '" + _param._examId + "' ");
        stb.append(" ORDER BY ");
        stb.append("     T1.RECEPTNO ");
        stb.append(" ), TOTAL_S AS ( ");
        stb.append(" SELECT ");
        stb.append("     T2.EXAM_SUBCLASS, ");
        stb.append("     MAX(T2.EXAM_SCORE) AS MAX, ");
        stb.append("     MIN(T2.EXAM_SCORE) AS MIN, ");
        stb.append("     AVG(T2.EXAM_SCORE) AS AVG ");
        stb.append(" FROM ");
        stb.append("     RECEPTNO T1 ");
        stb.append("     LEFT JOIN ");
        stb.append("         ENTEXAM_STD_RANK_DAT T2 ");
        stb.append("          ON T2.YEAR = T1.YEAR ");
        stb.append("         AND T2.RECEPTNO = T1.RECEPTNO ");
        stb.append("         AND T2.SUMMARY_DIV = 'S' ");
        stb.append(" GROUP BY ");
        stb.append("     T2.EXAM_SUBCLASS ");
        stb.append(" ), TOTAL_2S AS ( ");
        stb.append(" SELECT ");
        stb.append("     T3.EXAM_SUBCLASS, ");
        stb.append("     MAX(T3.EXAM_SCORE) AS MAX, ");
        stb.append("     MIN(T3.EXAM_SCORE) AS MIN, ");
        stb.append("     AVG(T3.EXAM_SCORE) AS AVG ");
        stb.append(" FROM ");
        stb.append("     RECEPTNO T1 ");
        stb.append("     LEFT JOIN ");
        stb.append("         ENTEXAM_STD_RANK_DAT T3 ");
        stb.append("          ON T3.YEAR = T1.YEAR ");
        stb.append("         AND T3.RECEPTNO = T1.RECEPTNO ");
        stb.append("         AND T3.SUMMARY_DIV = '2S' ");
        stb.append(" GROUP BY ");
        stb.append("     T3.EXAM_SUBCLASS ");
        stb.append(" )  ");
        stb.append(" SELECT ");
        stb.append("     * ");
        stb.append(" FROM ");
        stb.append("     TOTAL_S ");
        stb.append(" UNION ");
        stb.append(" SELECT ");
        stb.append("     * ");
        stb.append(" FROM ");
        stb.append("     TOTAL_2S ");
        stb.append(" ORDER BY ");
        stb.append("     EXAM_SUBCLASS ");

        return stb.toString();
    }

    private Map getIppanMap(final DB2UDB db2, final boolean bunpuFlg) {
        final Map retMap = new LinkedHashMap();
        PreparedStatement ps = null;
        ResultSet rs = null;
        final String sql = getIppanSql(bunpuFlg);
        log.debug(" ippan sql =" + sql);
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                if (bunpuFlg) { //分布表
                    final String testSubclassCd = rs.getString("EXAM_SUBCLASS");
                    final String bunpu01 = rs.getString("BUNPU01");
                    final String bunpu02 = rs.getString("BUNPU02");
                    final String bunpu03 = rs.getString("BUNPU03");
                    final String bunpu04 = rs.getString("BUNPU04");
                    final String bunpu05 = rs.getString("BUNPU05");
                    final String bunpu06 = rs.getString("BUNPU06");
                    final String bunpu07 = rs.getString("BUNPU07");
                    final String bunpu08 = rs.getString("BUNPU08");
                    final String bunpu09 = rs.getString("BUNPU09");
                    final String bunpu10 = rs.getString("BUNPU10");
                    final String bunpu11 = rs.getString("BUNPU11");
                    final String bunpu12 = rs.getString("BUNPU12");
                    final String bunpu13 = rs.getString("BUNPU13");
                    final String bunpu14 = rs.getString("BUNPU14");
                    final String bunpu15 = rs.getString("BUNPU15");
                    final String bunpu16 = rs.getString("BUNPU16");
                    final String bunpu17 = rs.getString("BUNPU17");
                    final String bunpu18 = rs.getString("BUNPU18");
                    final String bunpu19 = rs.getString("BUNPU19");
                    final String bunpu20 = rs.getString("BUNPU20");
                    final String bunpu21 = rs.getString("BUNPU21");

                    if (!retMap.containsKey(testSubclassCd)) {
                        final Bunpu bunpu = new Bunpu(testSubclassCd, bunpu01, bunpu02, bunpu03,
                                bunpu04, bunpu05, bunpu06, bunpu07, bunpu08, bunpu09, bunpu10, bunpu11, bunpu12, bunpu13,
                                bunpu14, bunpu15, bunpu16, bunpu17, bunpu18, bunpu19, bunpu20, bunpu21);
                        retMap.put(testSubclassCd, bunpu);
                    }
                } else { //成績表
                    final String receptno = rs.getString("RECEPTNO");

                    if (!retMap.containsKey(receptno)) {
                        final String year = rs.getString("YEAR");
                        final String examno = rs.getString("EXAMNO");
                        final String exam_School_Kind = rs.getString("EXAM_SCHOOL_KIND");
                        final String applicant_Div = rs.getString("APPLICANT_DIV");
                        final String course_Div = rs.getString("COURSE_DIV");
                        final String frequency = rs.getString("FREQUENCY");
                        final String name = rs.getString("NAME");
                        final String remark1 = rs.getString("REMARK1");
                        final String remark2 = rs.getString("REMARK2");
                        final String rank = rs.getString("RANK");
                        final String total = rs.getString("TOTAL");

                        final Ippan ippan = new Ippan(year, examno, exam_School_Kind, applicant_Div, course_Div, frequency, receptno, name, rank, total, remark1, remark2);
                        retMap.put(receptno, ippan);
                    }

                    final Ippan ippan = (Ippan)retMap.get(receptno);
                    final String exam_Subclass = rs.getString("EXAM_SUBCLASS");

                    if (!ippan.scoreMap.containsKey(exam_Subclass)) {
                        final String score = rs.getString("SCORE");
                        final String absenceFlg = rs.getString("ABSENCE_FLG");
                        ippan.scoreMap.put(exam_Subclass, new Score(exam_Subclass, null, null, score, absenceFlg));
                    }
                }
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retMap;
    }

    private String getIppanSql(final boolean bunpuFlg) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH BASE AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.YEAR, ");
        stb.append("     T1.EXAMNO, ");
        stb.append("     T1.EXAM_SCHOOL_KIND, ");
        stb.append("     T1.APPLICANT_DIV, ");
        stb.append("     T1.COURSE_DIV, ");
        stb.append("     T1.FREQUENCY, ");
        stb.append("     T1.RECEPTNO, ");
        stb.append("     T0.NAME, ");
        stb.append("     T0.REMARK1, ");
        stb.append("     T0.REMARK2, ");
        stb.append("     T2.EXAM_SUBCLASS, ");
        stb.append("     T2.SCORE, ");
        stb.append("     T2.ABSENCE_FLG, ");
        stb.append("     T3.EXAM_SCORE AS RANK, ");
        stb.append("     T4.EXAM_SCORE AS TOTAL ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_STD_RECEPT_DAT T1 ");
        stb.append(" INNER JOIN ");
        stb.append("     ENTEXAM_STD_APPLICANTBASE_DAT T0 ");
        stb.append("      ON T0.YEAR = T1.YEAR ");
        stb.append("     AND T0.EXAMNO = T1.EXAMNO ");
        stb.append(" LEFT JOIN ");
        stb.append("     ENTEXAM_STD_SCORE_DAT T2 ");
        stb.append("      ON T2.YEAR = T1.YEAR ");
        stb.append("     AND T2.RECEPTNO = T1.RECEPTNO ");
        stb.append(" LEFT JOIN ");
        stb.append("     ENTEXAM_STD_RANK_DAT T3 ");
        stb.append("      ON T3.YEAR = T2.YEAR ");
        stb.append("     AND T3.RECEPTNO = T2.RECEPTNO ");
        stb.append("     AND T3.EXAM_SUBCLASS = '99' ");
        stb.append("     AND T3.SUMMARY_DIV = 'TR' ");
        stb.append(" LEFT JOIN ");
        stb.append("     ENTEXAM_STD_RANK_DAT T4 ");
        stb.append("      ON T4.YEAR = T2.YEAR ");
        stb.append("     AND T4.RECEPTNO = T2.RECEPTNO ");
        stb.append("     AND T4.EXAM_SUBCLASS = '99' ");
        stb.append("     AND T4.SUMMARY_DIV = 'TS' ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._examYear + "' AND ");
        stb.append("     T1.EXAM_SCHOOL_KIND || T1.APPLICANT_DIV || T1.COURSE_DIV || T1.FREQUENCY = '" + _param._examId + "' AND ");
        stb.append("     T2.SCORE IS NOT NULL ");
        stb.append(" ORDER BY ");
        if ("1".equals(_param._sortDiv)) {
            stb.append("   T1.RECEPTNO, ");
        } else {
            stb.append("   RANK, ");
            stb.append("   T1.RECEPTNO, ");
        }
        stb.append("   T2.EXAM_SUBCLASS ");
        if (bunpuFlg) {
            stb.append(" ), BUNPU AS (");
            stb.append(" SELECT ");
            stb.append("   EXAM_SUBCLASS, ");
            stb.append("   SUM(CASE WHEN SCORE = 100 THEN 1 ELSE 0 END) AS BUNPU01, ");
            stb.append("   SUM(CASE WHEN SCORE BETWEEN 95 AND 99 THEN 1 ELSE 0 END) AS BUNPU02, ");
            stb.append("   SUM(CASE WHEN SCORE BETWEEN 90 AND 94 THEN 1 ELSE 0 END) AS BUNPU03, ");
            stb.append("   SUM(CASE WHEN SCORE BETWEEN 85 AND 89 THEN 1 ELSE 0 END) AS BUNPU04, ");
            stb.append("   SUM(CASE WHEN SCORE BETWEEN 80 AND 84 THEN 1 ELSE 0 END) AS BUNPU05, ");
            stb.append("   SUM(CASE WHEN SCORE BETWEEN 75 AND 79 THEN 1 ELSE 0 END) AS BUNPU06, ");
            stb.append("   SUM(CASE WHEN SCORE BETWEEN 70 AND 74 THEN 1 ELSE 0 END) AS BUNPU07, ");
            stb.append("   SUM(CASE WHEN SCORE BETWEEN 65 AND 69 THEN 1 ELSE 0 END) AS BUNPU08, ");
            stb.append("   SUM(CASE WHEN SCORE BETWEEN 60 AND 64 THEN 1 ELSE 0 END) AS BUNPU09, ");
            stb.append("   SUM(CASE WHEN SCORE BETWEEN 55 AND 59 THEN 1 ELSE 0 END) AS BUNPU10, ");
            stb.append("   SUM(CASE WHEN SCORE BETWEEN 50 AND 54 THEN 1 ELSE 0 END) AS BUNPU11, ");
            stb.append("   SUM(CASE WHEN SCORE BETWEEN 45 AND 49 THEN 1 ELSE 0 END) AS BUNPU12, ");
            stb.append("   SUM(CASE WHEN SCORE BETWEEN 40 AND 44 THEN 1 ELSE 0 END) AS BUNPU13, ");
            stb.append("   SUM(CASE WHEN SCORE BETWEEN 35 AND 39 THEN 1 ELSE 0 END) AS BUNPU14, ");
            stb.append("   SUM(CASE WHEN SCORE BETWEEN 30 AND 34 THEN 1 ELSE 0 END) AS BUNPU15, ");
            stb.append("   SUM(CASE WHEN SCORE BETWEEN 25 AND 29 THEN 1 ELSE 0 END) AS BUNPU16, ");
            stb.append("   SUM(CASE WHEN SCORE BETWEEN 20 AND 24 THEN 1 ELSE 0 END) AS BUNPU17, ");
            stb.append("   SUM(CASE WHEN SCORE BETWEEN 15 AND 19 THEN 1 ELSE 0 END) AS BUNPU18, ");
            stb.append("   SUM(CASE WHEN SCORE BETWEEN 10 AND 14 THEN 1 ELSE 0 END) AS BUNPU19, ");
            stb.append("   SUM(CASE WHEN SCORE BETWEEN  5 AND  9 THEN 1 ELSE 0 END) AS BUNPU20, ");
            stb.append("   SUM(CASE WHEN SCORE BETWEEN  0 AND  4 THEN 1 ELSE 0 END) AS BUNPU21 ");
            stb.append(" FROM ");
            stb.append("   BASE ");
            stb.append(" GROUP BY ");
            stb.append("   EXAM_SUBCLASS ");
            stb.append(" ORDER BY ");
            stb.append("   EXAM_SUBCLASS ");
        }
        if (bunpuFlg) {
            stb.append(" ) SELECT * FROM BUNPU ");
        } else {
            stb.append(" ) SELECT * FROM BASE ");
        }

        return stb.toString();
    }

    private Map getIppanTotalMap(final DB2UDB db2) {
        final Map<String, Total> retMap = new LinkedHashMap();
        PreparedStatement ps = null;
        ResultSet rs = null;
        final String sql = getIppanTotalSql();
        log.debug(" total sql =" + sql);
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String testSubclassCd = rs.getString("EXAM_SUBCLASS");
                final String max = rs.getString("MAX");
                final String min = rs.getString("MIN");
                final String avg = rs.getString("AVG");
                final String cnt = rs.getString("CNT");
                final String shigan = rs.getString("SHIGAN");
                final String juken = rs.getString("JUKEN");
                final String stddev = rs.getString("STDDEV");

                if (!retMap.containsKey(testSubclassCd)) {
                    final Total total = new Total(max, min, avg, shigan, juken, cnt, stddev);
                    retMap.put(testSubclassCd, total);
                }
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retMap;
    }

    private String getIppanTotalSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH BASE AS (SELECT ");
        stb.append("     T1.YEAR, ");
        stb.append("     T1.EXAMNO, ");
        stb.append("     T1.EXAM_SCHOOL_KIND, ");
        stb.append("     T1.APPLICANT_DIV, ");
        stb.append("     T1.COURSE_DIV, ");
        stb.append("     T1.FREQUENCY, ");
        stb.append("     T1.RECEPTNO, ");
        stb.append("     T0.NAME, ");
        stb.append("     T2.EXAM_SUBCLASS, ");
        stb.append("     T2.SCORE, ");
        stb.append("     T2.ABSENCE_FLG ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_STD_RECEPT_DAT T1 ");
        stb.append("     INNER JOIN ENTEXAM_STD_APPLICANTBASE_DAT T0 ");
        stb.append("          ON T0.YEAR = T1.YEAR ");
        stb.append("         AND T0.EXAMNO = T1.EXAMNO ");
        stb.append("     LEFT JOIN ENTEXAM_STD_SCORE_DAT T2 ");
        stb.append("          ON T2.YEAR = T1.YEAR ");
        stb.append("         AND T2.RECEPTNO = T1.RECEPTNO ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._examYear + "' AND ");
        stb.append("     T1.EXAM_SCHOOL_KIND || T1.APPLICANT_DIV || T1.COURSE_DIV || T1.FREQUENCY = '" + _param._examId + "' AND ");
        stb.append("     T2.EXAM_SUBCLASS IS NOT NULL AND ");
        stb.append("     T2.SCORE IS NOT NULL ");
        stb.append(" ORDER BY ");
        stb.append("     T1.RECEPTNO, T2.EXAM_SUBCLASS ");
        stb.append(" ), RESULT AS ( ");
        stb.append(" SELECT ");
        stb.append("     YEAR, ");
        stb.append("     EXAM_SCHOOL_KIND || APPLICANT_DIV || COURSE_DIV || FREQUENCY AS EXAMID, ");
        stb.append("     EXAM_SUBCLASS, ");
        stb.append("     MAX(SCORE) AS MAX, ");
        stb.append("     MIN(SCORE) AS MIN, ");
        stb.append("     SUM(CASE WHEN ABSENCE_FLG = 1 THEN 1 ELSE 0 END) AS CNT, ");
        stb.append("     COUNT(RECEPTNO) AS SHIGAN, ");
        stb.append("     COUNT(RECEPTNO) - SUM(CASE WHEN ABSENCE_FLG = 1 THEN 1 ELSE 0 END) AS JUKEN ");
        stb.append(" FROM ");
        stb.append("     BASE ");
        stb.append(" GROUP BY ");
        stb.append("     YEAR, EXAM_SCHOOL_KIND, APPLICANT_DIV, COURSE_DIV, FREQUENCY, EXAM_SUBCLASS ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     T1.*, ");
        stb.append("     T2.EXAM_SCORE AS STDDEV, ");
        stb.append("     T3.EXAM_SCORE AS AVG ");
        stb.append(" FROM ");
        stb.append("     RESULT T1 ");
        stb.append("     LEFT JOIN ENTEXAM_STD_AVG_DAT T2 ");
        stb.append("          ON T2.YEAR = T1.YEAR ");
        stb.append("         AND T2.EXAM_SCHOOL_KIND || T2.APPLICANT_DIV || T2.COURSE_DIV || T2.FREQUENCY = T1.EXAMID ");
        stb.append("         AND T2.EXAM_SUBCLASS = T1.EXAM_SUBCLASS ");
        stb.append("         AND T2.SUMMARY_DIV = 'V' ");
        stb.append("     LEFT JOIN ENTEXAM_STD_AVG_DAT T3 ");
        stb.append("          ON T3.YEAR = T1.YEAR ");
        stb.append("         AND T3.EXAM_SCHOOL_KIND || T3.APPLICANT_DIV || T3.COURSE_DIV || T3.FREQUENCY = T1.EXAMID ");
        stb.append("         AND T3.EXAM_SUBCLASS = T1.EXAM_SUBCLASS ");
        stb.append("         AND T3.SUMMARY_DIV = 'A' ");
        stb.append(" ORDER BY ");
        stb.append("     T1.EXAM_SUBCLASS ");

        return stb.toString();
    }

    private class Kikoku {
        final String _year;
        final String _examno;
        final String _exam_School_Kind;
        final String _applicant_Div;
        final String _course_Div;
        final String _frequency;
        final String _receptno;
        final String _name;
        final String _rank;
        final String _total;
        final String _remark;
        final String _placeId;
        final Map<String, Score> scoreMap = new LinkedHashMap();

        public Kikoku(final String year, final String examno, final String exam_School_Kind, final String applicant_Div,
                final String course_Div, final String frequency, final String receptno, final String name,
                final String rank, final String total, final String remark, final String placeId) {
            _year = year;
            _examno = examno;
            _exam_School_Kind = exam_School_Kind;
            _applicant_Div = applicant_Div;
            _course_Div = course_Div;
            _frequency = frequency;
            _receptno = receptno;
            _name = name;
            _rank = rank;
            _total = total;
            _remark = remark;
            _placeId = placeId;
        }
    }

    private class Ippan {
        final String _year;
        final String _examno;
        final String _exam_School_Kind;
        final String _applicant_Div;
        final String _course_Div;
        final String _frequency;
        final String _receptno;
        final String _name;
        final String _rank;
        final String _total;
        final String _remark1;
        final String _remark2;
        final Map<String, Score> scoreMap = new LinkedHashMap();

        public Ippan(final String year, final String examno, final String exam_School_Kind, final String applicant_Div,
                final String course_Div, final String frequency, final String receptno, final String name,
                final String rank, final String total, final String remark1, final String remark2) {
            _year = year;
            _examno = examno;
            _exam_School_Kind = exam_School_Kind;
            _applicant_Div = applicant_Div;
            _course_Div = course_Div;
            _frequency = frequency;
            _receptno = receptno;
            _name = name;
            _rank = rank;
            _total = total;
            _remark1 = remark1;
            _remark2 = remark2;
        }
    }

    private class Score {
        final String _exam_Subclass;
        final String _label;
        final String _value;
        final String _exam_Score;
        final String _absenceFlg;

        public Score(final String exam_Subclass, final String label, final String value, final String exam_Score, final String absenceFlg) {
            _exam_Subclass = exam_Subclass;
            _label = label;
            _value = value;
            _exam_Score = exam_Score;
            _absenceFlg = absenceFlg;
        }
    }

    private class Total {
        final String _max;
        final String _min;
        final String _avg;
        final String _shigan;
        final String _juken;
        final String _kesseki;
        final String _stddev;

        public Total(final String max, final String min, final String avg, final String shigan, final String juken, final String kesseki, final String stddev) {
            _max = max;
            _min = min;
            _avg = avg;
            _shigan = shigan;
            _juken = juken;
            _kesseki = kesseki;
            _stddev = stddev;
        }
    }

    private class Bunpu {
        final String _testSubclassCd;
        final String _bunpu01;
        final String _bunpu02;
        final String _bunpu03;
        final String _bunpu04;
        final String _bunpu05;
        final String _bunpu06;
        final String _bunpu07;
        final String _bunpu08;
        final String _bunpu09;
        final String _bunpu10;
        final String _bunpu11;
        final String _bunpu12;
        final String _bunpu13;
        final String _bunpu14;
        final String _bunpu15;
        final String _bunpu16;
        final String _bunpu17;
        final String _bunpu18;
        final String _bunpu19;
        final String _bunpu20;
        final String _bunpu21;

        public Bunpu(final String testSubclassCd, final String bunpu01, final String bunpu02,
                final String bunpu03, final String bunpu04, final String bunpu05, final String bunpu06,
                final String bunpu07, final String bunpu08, final String bunpu09, final String bunpu10, final String bunpu11,
                final String bunpu12, final String bunpu13, final String bunpu14, final String bunpu15, final String bunpu16,
                final String bunpu17, final String bunpu18, final String bunpu19, final String bunpu20, final String bunpu21) {
            _testSubclassCd = testSubclassCd;
            _bunpu01 = bunpu01;
            _bunpu02 = bunpu02;
            _bunpu03 = bunpu03;
            _bunpu04 = bunpu04;
            _bunpu05 = bunpu05;
            _bunpu06 = bunpu06;
            _bunpu07 = bunpu07;
            _bunpu08 = bunpu08;
            _bunpu09 = bunpu09;
            _bunpu10 = bunpu10;
            _bunpu11 = bunpu11;
            _bunpu12 = bunpu12;
            _bunpu13 = bunpu13;
            _bunpu14 = bunpu14;
            _bunpu15 = bunpu15;
            _bunpu16 = bunpu16;
            _bunpu17 = bunpu17;
            _bunpu18 = bunpu18;
            _bunpu19 = bunpu19;
            _bunpu20 = bunpu20;
            _bunpu21 = bunpu21;
        }
    }

    private static class Subclass {
        final String _cd;
        final String _subclassName;
        final String _value;
        final int _field;

        public Subclass (final String cd, final String subclassName, final String value, final int field) {
            _cd = cd;
            _subclassName = subclassName;
            _value = value;
            _field = field;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _loginYear;
        final String _loginDate;
        final String _examYear;
        final String _nendo;
        final String _examId;
        final String _examCd;
        final String _sortDiv;
        final String _schoolKind;
        final String _examName;

        final Map _subclassMap = new LinkedHashMap();
        final List _interviewCdList = new LinkedList();

        /** グラフイメージファイルの Set&lt;File&gt; */
        final Set _graphFiles = new HashSet();

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _loginYear = request.getParameter("LOGIN_YEAR");
            _nendo = KNJ_EditDate.getAutoFormatYear(db2, Integer.parseInt(_loginYear)) + "年度";
            _loginDate = request.getParameter("LOGIN_DATE").replace("/", "-");
            _examYear = request.getParameter("ENTEXAMYEAR");
            _examId = request.getParameter("APPLICANTDIV");
            _examCd = _examId.substring(1, 3);
            _sortDiv = request.getParameter("SORT_DIV");
            _schoolKind = request.getParameter("EXAM_SCHOOL_KIND");
            _examName = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT EXAM_NAME FROM ENTEXAM_STD_MST WHERE YEAR = " + _examYear + " AND EXAM_SCHOOL_KIND || APPLICANT_DIV || COURSE_DIV || FREQUENCY = '" + _examId + "' "));

            setSubclassMap(db2);
            if ("01".equals(_examCd)) {
                setInterviewCdList(db2);
            }
        }

        private void setSubclassMap(final DB2UDB db2) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT DISTINCT ");
            stb.append("     T1.EXAM_SUBCLASS, ");
            stb.append("     T1.VALUE, ");
            stb.append("     T2.NAME1 ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_STD_PERFECT_MST T1 ");
            stb.append("     LEFT JOIN ");
            stb.append("         ENTEXAM_SETTING_MST T2 ");
            stb.append("              ON T2.ENTEXAMYEAR = T1.YEAR ");
            stb.append("             AND T2.SETTING_CD = 'L009' ");
            stb.append("             AND T2.SEQ = T1.EXAM_SUBCLASS ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _examYear + "' AND ");
            stb.append("     T1.EXAM_SCHOOL_KIND || T1.APPLICANT_DIV || T1.COURSE_DIV || T1.FREQUENCY = '" + _examId + "' AND ");
            stb.append("     T1.STEPS = 'A' ");
            //面接は帰国生のみ
            if ("01".equals(_examCd)) {
                stb.append(" UNION ");
                stb.append(" SELECT DISTINCT ");
                stb.append("     T1.EXAM_SUBCLASS, ");
                stb.append("     '' AS VALUE, ");
                stb.append("     T2.NAME1 ");
                stb.append(" FROM ");
                stb.append("     ENTEXAM_STD_PERFECT_MST T1 ");
                stb.append("     LEFT JOIN ");
                stb.append("         ENTEXAM_SETTING_MST T2 ");
                stb.append("              ON T2.ENTEXAMYEAR = T1.YEAR ");
                stb.append("             AND T2.SETTING_CD = 'L009' ");
                stb.append("             AND T2.SEQ = T1.EXAM_SUBCLASS ");
                stb.append(" WHERE ");
                stb.append("     T1.YEAR = '" + _examYear + "' AND ");
                stb.append("     T1.EXAM_SCHOOL_KIND || T1.APPLICANT_DIV || T1.COURSE_DIV || T1.FREQUENCY = '" + _examId + "' AND ");
                stb.append("     STEPS <> 'A' ");
            }
            stb.append(" ORDER BY ");
            stb.append("     EXAM_SUBCLASS ");

            PreparedStatement ps = null;
            ResultSet rs = null;
            int cnt = 1;

            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();

                while (rs.next()) {
                    final String cd = rs.getString("EXAM_SUBCLASS");
                    if (!_subclassMap.containsKey(cd)) {
                        final String name = rs.getString("NAME1");
                        final String value = rs.getString("VALUE");
                        final Subclass subclass = new Subclass(cd, name, value, cnt);
                        _subclassMap.put(cd, subclass);
                        cnt++;
                    }
                }
            } catch (SQLException ex) {
                log.debug("Exception:", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        private void setInterviewCdList(final DB2UDB db2) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT DISTINCT ");
            stb.append("     YEAR, ");
            stb.append("     EXAM_SCHOOL_KIND, ");
            stb.append("     APPLICANT_DIV, ");
            stb.append("     COURSE_DIV, ");
            stb.append("     FREQUENCY, ");
            stb.append("     EXAM_SUBCLASS ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_STD_PERFECT_MST ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _examYear + "' AND ");
            stb.append("     EXAM_SCHOOL_KIND || APPLICANT_DIV || COURSE_DIV || FREQUENCY = '" + _examId + "' AND ");
            stb.append("     STEPS <> 'A' ");
            stb.append(" ORDER BY ");
            stb.append("     EXAM_SUBCLASS ");

            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();

                while (rs.next()) {
                    final String cd = rs.getString("EXAM_SUBCLASS");
                    if (!_interviewCdList.contains(cd)) {
                        _interviewCdList.add(cd);
                    }
                }
            } catch (SQLException ex) {
                log.debug("Exception:", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
        }
    }
}
// eof
