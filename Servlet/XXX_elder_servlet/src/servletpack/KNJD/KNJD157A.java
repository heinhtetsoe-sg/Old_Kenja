// kanji=漢字
/*
 * $Id$
 *
 * 作成日: 2021/03/22
 * 作成者: Nutec
 *
 */
package servletpack.KNJD;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
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
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.RectangleEdge;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJServletpacksvfANDdb2;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 *
 *  学校教育システム 賢者 [成績管理]
 *
 *                  ＜ＫＮＪＤ１５７Ａ＞  成績個人票
 */
public class KNJD157A {

    private static final Log log = LogFactory.getLog(KNJD157A.class);
    private boolean _nonedata = true;//該当データなしフラグ
    private KNJServletpacksvfANDdb2 _sd = new KNJServletpacksvfANDdb2();
    private Param _param = null;

    /*----------------------------*
     * HTTP Get リクエストの処理  *
     *----------------------------*/
    public void svf_out(final HttpServletRequest request, final HttpServletResponse response)
                     throws ServletException, IOException
    {
        final Vrw32alp svf = new Vrw32alp();//PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                   //Databaseクラスを継承したクラス

        PrintWriter outstrm = null;
        try {
            //  print設定
            response.setContentType("application/pdf");
            outstrm = new PrintWriter(response.getOutputStream());

            //  svf設定
            svf.VrInit();                                          //クラスの初期化
            svf.VrSetSpoolFileStream(response.getOutputStream());  //PDFファイル名の設定

            _sd.setSvfInit(request, response, svf);
            db2 = _sd.setDb(request);
            if (_sd.openDb(db2)) {
                log.error("db open error! ");
                return;
            }

            _param = getParam(db2, request);

            if (!KNJServletUtils.isEnableGraph(log)) {
                log.fatal("グラフを使える環境ではありません。");
            }

            printSvfMain(db2, svf);

        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            //該当データ無し
            if (_nonedata) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note" , "note");
                svf.VrEndPage();
            }
            //  終了処理
            svf.VrQuit();
            db2.commit();
            db2.close();      //DBを閉じる
            outstrm.close();  //ストリームを閉じる

            if (null != _param) {
                int count = 0;
                for (final Iterator it = _param._graphFiles.iterator(); it.hasNext();) {
                    final File imageFile = (File)it.next();
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
        }

    }//doGetの括り

    private static int toInt(final String s, final int def) {
        if (!NumberUtils.isDigits(s)) {
            return def;
        }
        return Integer.parseInt(s);
    }

    private void setForm(final Vrw32alp svf, final String form, final int data) {
        svf.VrSetForm(form, data);
        log.info(" form = " + form);
        if (_param._isOutputDebug) {
            log.info(" form = " + form);
        }
    }

    /** 帳票出力 **/
    private void printSvfMain(final DB2UDB db2, final Vrw32alp svf) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        String sql   = null;

        try {
            setForm(svf, "KNJD157A.xml", 1);

            ArrayList<TableData> subCnt   = new ArrayList<TableData>();
            ArrayList<TableData> classCnt = new ArrayList<TableData>();

            //日付の和暦変換に使用
            Calendar calendar = Calendar.getInstance();
            calendar.set(toInt(_param._year, 0), 4, 1);
            String dateStr = KNJ_EditDate.gengou(db2, calendar.get(Calendar.YEAR)) + "年度";

            //テスト種別
            sql = sqlTestType();
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            rs.next();
            String titleName = rs.getString("TESTITEMNAME");

            //科目毎の人数
            sql = sqlSubclassCnt();
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                subCnt.add(new TableData(
                    rs.getString("GRADE"),     //学年
                    rs.getString("HR_CLASS"),  //クラス
                    rs.getString("SUBCLASSCD"),//科目
                    rs.getString("COUNT")      //人数
                ));
            }

            //クラスの人数
            sql = sqlClassCnt();
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                classCnt.add(new TableData(
                    rs.getString("GRADE"),   //学年
                    rs.getString("HR_CLASS"),//クラス
                    "",                      //科目
                    rs.getString("COUNT")    //人数
                ));
            }

            //生徒情報
            sql = sqlScore();
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            String schregno = "";
            int subclassCnt = 0;
            //帳票に印字
            ArrayList<GraphData> graph = new ArrayList<GraphData>();
            while (rs.next()) {
                //印字すべきデータがあったので該当データ有りとする
                _nonedata = false;

                if ("".equals(schregno) || schregno.equals(rs.getString("SCHREGNO")) == false) {

                    if (schregno.equals(rs.getString("SCHREGNO")) == false && "".equals(schregno) == false) {
                        //グラフ印字
                        printBarGraph(svf, graph);

                        //改ページ
                        svf.VrEndPage();
                        graph = new ArrayList<GraphData>();
                    }

                    //学籍番号を保持
                    schregno = rs.getString("SCHREGNO");
                    subclassCnt = 0;

                    //ヘッダー
                    String title = dateStr + "　" + titleName + "　成績個人票";
                    svf.VrsOut("TITLE", title);
                    svf.VrsOut("HR_NAME", rs.getString("HR_NAME") + "　" + Integer.parseInt(rs.getString("ATTENDNO")) + "番");
                    svf.VrsOut("NAME", rs.getString("NAME"));

                    //合計得点
                    svf.VrsOut("TOTAL", rs.getString("GOUKEI"));
                    //合計平均
                    if (rs.getString("HEIKIN") != null) {
                        svf.VrsOut("AVE", RoundHalfUp(rs.getString("HEIKIN")));
                    }
                    //合計順位
                    for (int cnt = 0; cnt < classCnt.size(); cnt++) {
                        if (rs.getString("HR_CLASS").equals((classCnt.get(cnt))._hrClass)) {
                            String rank = rs.getString("CLASS_AVG_RANK") == null ? "" : rs.getString("CLASS_AVG_RANK");
                            String classNum = classCnt.get(cnt)._count == null ? "" : classCnt.get(cnt)._count;
                            svf.VrsOut("RANK", rank + "/" + classNum);
                            break;
                        }
                    }
                }
                subclassCnt++;

                //科目毎の特典、順位、平均
                //科目名
                {
                    String sc = rs.getString("SUBCLASSABBV");
                    int subClassLength = KNJ_EditEdit.getMS932ByteLength(sc);
                    final String subClassField = (subClassLength <= 8)? "1": "2";
                    svf.VrsOutn("SUBCLASS_NAME" + subClassField, subclassCnt, sc);
                }

                //グラフ用にデータを保持
                final String graphSubclassCd = rs.getString("SUBCLASSCD");
                final String graphSubclassAbbv = rs.getString("SUBCLASSABBV");
                final String graphTokuten = rs.getString("TOKUTEN");
                String graphAverage = "0";//OutOfBoundsExceptionが発生するためデフォルトで0を入れておく

                //得点
                svf.VrsOutn("SCORE", subclassCnt, rs.getString("TOKUTEN"));
                if (rs.getString("TOKUTEN") == null) {
                    //点数に値がない場合は平均やグラフに値を印字しない
                    graph.add(new GraphData(
                        graphSubclassCd,
                        graphSubclassAbbv,
                        graphTokuten,
                        graphAverage
                    ));
                    continue;
                }

                //クラス順位
                for (int cnt = 0; cnt < subCnt.size(); cnt++) {
                    if (rs.getString("HR_CLASS").equals((subCnt.get(cnt))._hrClass) &&
                        graphSubclassCd.equals((subCnt.get(cnt))._subclassCd)) {

                        String rank = rs.getString("CLASS_RANK") == null ? "" : rs.getString("CLASS_RANK");
                        String classNum = (subCnt.get(cnt)._count == null) ? "": subCnt.get(cnt)._count;
                        svf.VrsOutn("HR_RANK", subclassCnt, rank + "/" + classNum);
                        break;
                    }
                }
                //クラス平均
                if (rs.getString("KURASUHEIKIN") != null) {
                    svf.VrsOutn("HR_AVE",  subclassCnt, RoundHalfUp(rs.getString("KURASUHEIKIN")));
                }

                if ("1".equals(_param._type)) {  //講座順位
                    svf.VrsOut("SELECT_NAME", "講座");
                    //講座順位
                    String rank = rs.getString("GRADE_RANK") == null ? "" : rs.getString("GRADE_RANK");
                    String num  = rs.getString("KOUZANINZU") == null ? "" : rs.getString("KOUZANINZU");
                    svf.VrsOutn("SELECT_RANK", subclassCnt, rank + "/" + num);
                    //講座平均
                    if (rs.getString("KOUZAHEIKIN") != null) {
                        svf.VrsOutn("SELECT_AVE",  subclassCnt, RoundHalfUp(rs.getString("KOUZAHEIKIN")));
                    }
                    if (rs.getString("KURASUHEIKIN") != null) {
                        graphAverage = RoundHalfUp(rs.getString("KURASUHEIKIN"));//グラフ用にデータを保持  講座の場合はクラス平均を保持
                    }
                } else if ("2".equals(_param._type)) {  //類型順位
                    svf.VrsOut("SELECT_NAME", "類型");
                    //類型順位
                    String rank = rs.getString("CHAIR_GROUP_RANK") == null ? "" : rs.getString("CHAIR_GROUP_RANK");
                    String num  = rs.getString("RUIKEININZU")      == null ? "" : rs.getString("RUIKEININZU");
                    svf.VrsOutn("SELECT_RANK", subclassCnt, rank + "/" + num);
                    //類型平均
                    if (rs.getString("RUIKEIHEIKIN") != null) {
                        svf.VrsOutn("SELECT_AVE", subclassCnt, RoundHalfUp(rs.getString("RUIKEIHEIKIN")));
                        graphAverage = RoundHalfUp(rs.getString("RUIKEIHEIKIN"));//グラフ用にデータを保持
                    }
                }

                graph.add(new GraphData(
                    graphSubclassCd,
                    graphSubclassAbbv,
                    graphTokuten,
                    graphAverage
                ));
            }

            // グラフ印字
            if (graph.size() > 0) {
                printBarGraph(svf, graph);
                svf.VrEndPage();
            }
        } catch (Exception ex) {
            log.error("setSvfout set error!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }

    private void printBarGraph(final Vrw32alp svf, final ArrayList<GraphData> graph) {
        //棒グラフの最大科目数
        final int MAX_BAR_COUNT = 20;

        // グラフ用のデータ作成
        final DefaultCategoryDataset scoreDataset = new DefaultCategoryDataset();
        int subCnt = 0;//印字する科目数
        for (subCnt = 0; subCnt < graph.size(); subCnt++) {
            if (MAX_BAR_COUNT <= subCnt) {
                break;
            }

            final int score = toInt(graph.get(subCnt)._score, 0);
            final BigDecimal avg = new BigDecimal(graph.get(subCnt)._average);
            final String subclass = graph.get(subCnt)._subclassName;
            String subclassKey = null != subclass && subclass.length() > 5 ? subclass.substring(0, 5) : subclass;//5文字に切り詰める
            if (subclassKey == null) {
                //科目名称がnullのものはsubclasscdを入れておく
                subclassKey = graph.get(subCnt)._subclassCd;
            }

            scoreDataset.addValue(
                score,
                "本人点数",
                subclassKey
            );
            scoreDataset.addValue(
                avg,
                ("1".equals(_param._type)) ? "クラス平均" : "類型平均",
                subclassKey
            );
        }

        try {
            // チャート作成
            final JFreeChart chart = createBarChart(scoreDataset, subCnt);

            // グラフのファイルを生成

            //フォーム(xml)上のサイズ指定
            final float ORG_DOT_WIDTH  = 4279;
            final float ORG_DOT_HEIGHT = 2020;
            final int MARGIN_WIDTH = 200;//マージン(複雑な文字の場合に横幅が多少変わることがあるため)
            int dotWidth  = 0;
            int dotHeight = 0;
            //印字する科目の数に応じてアスペクト比を変えずに解像度を変更する
            if (subCnt > 10) {
                //10科目を超える場合→15ptで科目名が省略表示「・・・」されないようにする
                dotWidth = 7780 + MARGIN_WIDTH;
                //(11pt)dotWidth = 5788 + MARGIN_WIDTH
                //(12pt)dotWidth = 6284 + MARGIN_WIDTH
                //(13pt)dotWidth = 6784 + MARGIN_WIDTH
                //(14pt)dotWidth = 7280 + MARGIN_WIDTH
                //(15pt)dotWidth = 7780 + MARGIN_WIDTH
                //(16pt)dotWidth = 8280 + MARGIN_WIDTH
                //(17pt)dotWidth = 8776 + MARGIN_WIDTH
                //(18pt)dotWidth = 9272 + MARGIN_WIDTH
            } else {
                //10科目以下の場合→18ptで科目名が省略表示「・・・」されないようにする)
                dotWidth = 4840 + MARGIN_WIDTH;
            }
            //アスペクト比を維持するため計算値で縦幅の解像度を取得
            dotHeight = Math.round(dotWidth * ORG_DOT_HEIGHT / ORG_DOT_WIDTH);

            final File outputFile = graphImageFile(chart, dotWidth, dotHeight);

            _param._graphFiles.add(outputFile);

            if (outputFile.exists()) {
                svf.VrsOut("GRAPH", outputFile.toString());
            }
        } catch (Throwable e) {
            log.error("exception or error!", e);
        }
    }

    private JFreeChart createBarChart(final DefaultCategoryDataset scoreDataset, final int subclassCount) {
        final JFreeChart chart = ChartFactory.createBarChart(null, null, null, scoreDataset, PlotOrientation.VERTICAL, true, false, false);
        final CategoryPlot plot = chart.getCategoryPlot();
        plot.setRangeGridlineStroke(new BasicStroke(2.0f)); // 目盛りの太さ
        plot.setRangeGridlinePaint(Color.black);
        plot.setRangeGridlineStroke(new BasicStroke(1.0f));

        final NumberAxis numberAxis = new NumberAxis();
        numberAxis.setTickUnit(new NumberTickUnit(10));
        numberAxis.setTickLabelsVisible(true);
        numberAxis.setRange(0, 100.0);
        plot.setRangeAxis(numberAxis);

        //凡例を右側に表示
        chart.getLegend().setPosition(RectangleEdge.RIGHT);

        final CategoryItemRenderer renderer = plot.getRenderer();
        renderer.setSeriesPaint(0, Color.black);
        renderer.setSeriesPaint(1, Color.gray);

        //グラフの科目名のフォントサイズ(科目数に応じて可変)
        final int fontSize = (subclassCount > 10)? 15: 18;

        final Font itemLabelFont = new Font("TimesRoman", Font.PLAIN, fontSize);
        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setTickLabelsVisible(true);
        domainAxis.setTickLabelFont(itemLabelFont);
        domainAxis.setLowerMargin(0.1);

        BarRenderer barRenderer = (BarRenderer) renderer;
        barRenderer.setMaximumBarWidth(0.05);
        barRenderer.setItemMargin(0);

        chart.setBackgroundPaint(Color.white);

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
        return (int) (pixel * 1.3);
    }

    //小数第二位を四捨五入
    private String RoundHalfUp(String str) {
        BigDecimal bg = new BigDecimal(str);
        bg = bg.setScale(1, BigDecimal.ROUND_HALF_UP);
        return String.format("%.1f", bg);
    }

    /**テスト種別**/
    private String sqlTestType() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT T1.TESTITEMNAME ");
        stb.append("      , T1.SCORE_DIV ");
        stb.append("   FROM TESTITEM_MST_COUNTFLG_NEW_SDIV T1");
        stb.append("  WHERE T1.YEAR         = '" + _param._year + "' ");
        stb.append("    AND T1.SEMESTER     = '" + _param._semester + "' ");
        stb.append("    AND T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '" + _param._testcd + "' ");

        return stb.toString();
    }

    /**科目毎の人数**/
    private String sqlSubclassCnt() {
        final StringBuffer stb = new StringBuffer();
        stb.append("   SELECT GRADE ");
        stb.append("        , HR_CLASS ");
        stb.append("        , SUBCLASSCD ");
        stb.append("        , COUNT(SCORE) AS COUNT ");
        stb.append("     FROM RECORD_SCORE_DAT RSD ");
        stb.append("LEFT JOIN SCHREG_REGD_DAT SRD ");
        stb.append("       ON RSD.YEAR     = SRD.YEAR ");
        stb.append("      AND RSD.SEMESTER = SRD.SEMESTER ");
        stb.append("      AND RSD.SCHREGNO = SRD.SCHREGNO ");
        stb.append("    WHERE RSD.YEAR     = '" + _param._year + "' ");
        stb.append("      AND RSD.SEMESTER = '" + _param._semester + "' ");
        stb.append("      AND SRD.GRADE    = '" + _param._grade + "' ");
        stb.append("      AND RSD.TESTKINDCD || RSD.TESTITEMCD || RSD.SCORE_DIV = '" + _param._testcd + "' ");
        stb.append("      AND RSD.VALUE_DI IS NULL ");
        stb.append(" GROUP BY GRADE ");
        stb.append("        , HR_CLASS ");
        stb.append("        , SUBCLASSCD ");
        stb.append(" ORDER BY GRADE ");
        stb.append("        , HR_CLASS ");
        stb.append("        , SUBCLASSCD ");

        return stb.toString();
    }

    /**クラスの人数**/
    private String sqlClassCnt() {
        final StringBuffer stb = new StringBuffer();
        stb.append("  SELECT GRADE ");
        stb.append("       , HR_CLASS ");
        stb.append("       , COUNT(*)  AS COUNT ");
        stb.append("    FROM SCHREG_REGD_DAT ");
        stb.append("   WHERE YEAR     = '" + _param._year + "' ");
        stb.append("     AND SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND GRADE    = '" + _param._grade + "' ");
        stb.append("GROUP BY GRADE ");
        stb.append("       , HR_CLASS ");
        stb.append("ORDER BY GRADE ");
        stb.append("       , HR_CLASS ");

        return stb.toString();
    }

    /**成績情報**/
    private String sqlScore()
    {
        final StringBuffer stb = new StringBuffer();

        stb.append(" WITH SRD AS ( ");
        stb.append("        SELECT SRDX.YEAR ");
        stb.append("             , SRDX.SCHREGNO ");
        stb.append("             , SRDX.GRADE ");
        stb.append("             , SRDX.HR_CLASS ");
        stb.append("             , SRH.HR_NAME ");
        stb.append("             , SRDX.ATTENDNO ");
        stb.append("             , SBM.NAME ");
        stb.append("          FROM SCHREG_REGD_DAT SRDX ");
        stb.append("     LEFT JOIN SCHREG_BASE_MST SBM ");
        stb.append("            ON SRDX.SCHREGNO = SBM.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT SRH ");
        stb.append("            ON SRDX.YEAR     = SRH.YEAR ");
        stb.append("           AND SRDX.GRADE    = SRH.GRADE ");
        stb.append("           AND SRDX.HR_CLASS = SRH.HR_CLASS ");
        stb.append("           AND NOT EXISTS (  ");
        stb.append("                            SELECT 'X' ");
        stb.append("                              FROM SCHREG_REGD_HDAT TMP ");
        stb.append("                             WHERE TMP.YEAR     = SRH.YEAR ");
        stb.append("                               AND TMP.GRADE    = SRH.GRADE ");
        stb.append("                               AND TMP.HR_CLASS = SRH.HR_CLASS ");
        stb.append("                               AND TMP.SEMESTER < SRH.SEMESTER ");
        stb.append("                            ) ");
        stb.append("         WHERE ");
        if ("1".equals(_param._kubun)) { //1:クラス
            stb.append("                  SRDX.GRADE || '-' || SRDX.HR_CLASS IN " + SQLUtils.whereIn(true, _param._class) + " ");
        } else if ("2".equals(_param._kubun)) { //2:個人
            stb.append("                  SRDX.SCHREGNO IN " + SQLUtils.whereIn(true, _param._class) + " ");
        }
        stb.append("           AND SRDX.YEAR     = '" + _param._year + "' ");
        stb.append("           AND SRDX.GRADE    = '" + _param._grade + "' ");
        stb.append("      GROUP BY SRDX.YEAR ");
        stb.append("             , SRDX.SCHREGNO ");
        stb.append("             , SRDX.GRADE ");
        stb.append("             , SRDX.HR_CLASS ");
        stb.append("             , SRH.HR_NAME ");
        stb.append("             , SRDX.ATTENDNO ");
        stb.append("             , SBM.NAME ");
        stb.append(" ) ");
        stb.append("    SELECT SRD.SCHREGNO ");              //学籍番号
        stb.append("         , TCNS.TESTITEMNAME ");         //試験種別
        stb.append("         , SRD.GRADE ");                 //学年
        stb.append("         , SRD.HR_CLASS ");              //クラス
        stb.append("         , SRD.HR_NAME ");               //年組
        stb.append("         , SRD.ATTENDNO ");              //出席番号
        stb.append("         , SRD.NAME ");                  //氏名
        stb.append("         , RSD.SUBCLASSCD ");            //科目
        stb.append("         , SM.SUBCLASSABBV ");           //科目名略称
        stb.append("         , RSD.SCORE AS TOKUTEN ");      //得点
        stb.append("         , RRSD1.CLASS_RANK ");          //クラス順位
        stb.append("         , RASD1.AVG AS KURASUHEIKIN "); //クラス平均
        stb.append("         , RRCS.GRADE_RANK ");           //講座順位
        stb.append("         , RAC.AVG AS KOUZAHEIKIN ");    //講座平均
        stb.append("         , RAC.COUNT AS KOUZANINZU ");   //講座人数
        stb.append("         , RRSD2.CHAIR_GROUP_RANK ");    //類型順位
        stb.append("         , CASE WHEN CHRGRP_2.CHAIR_GROUP_CD IS NOT NULL ");
        stb.append("                THEN RASD2_2.AVG ");
        stb.append("                ELSE RASD2_1.AVG ");
        stb.append("           END AS RUIKEIHEIKIN ");       //類型平均
        stb.append("         , CASE WHEN CHRGRP_2.CHAIR_GROUP_CD IS NOT NULL ");
        stb.append("                THEN RASD2_2.COUNT ");
        stb.append("                ELSE RASD2_1.COUNT ");
        stb.append("           END AS RUIKEININZU ");        //類型人数
        stb.append("         , RRSD_SUM.SCORE AS GOUKEI ");  //合計
        stb.append("         , RRSD_SUM.AVG AS HEIKIN ");    //平均
        stb.append("         , RRSD_SUM.CLASS_AVG_RANK ");   //順位
        stb.append("      FROM SRD");
        stb.append(" LEFT JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV TCNS ");
        stb.append("        ON TCNS.YEAR            = SRD.YEAR ");
        stb.append("       AND TCNS.SEMESTER        = '" + _param._semester + "' ");
        stb.append("       AND    TCNS.TESTKINDCD ");
        stb.append("           || TCNS.TESTITEMCD ");
        stb.append("           || TCNS.SCORE_DIV   = '" + _param._testcd + "' ");
        stb.append(" LEFT JOIN RECORD_SCORE_DAT RSD ");
        stb.append("        ON RSD.YEAR            = SRD.YEAR ");
        stb.append("       AND RSD.SEMESTER        = TCNS.SEMESTER ");
        stb.append("       AND RSD.TESTKINDCD      = TCNS.TESTKINDCD ");
        stb.append("       AND RSD.TESTITEMCD      = TCNS.TESTITEMCD ");
        stb.append("       AND RSD.SCHREGNO        = SRD.SCHREGNO ");
        stb.append(" LEFT JOIN SUBCLASS_MST SM ");
        stb.append("        ON SM.SUBCLASSCD       = RSD.SUBCLASSCD ");
        stb.append("       AND SM.CURRICULUM_CD    = RSD.CURRICULUM_CD ");
        stb.append("       AND SM.CLASSCD          = RSD.CLASSCD ");
        //クラス順位、平均
        stb.append(" LEFT JOIN RECORD_RANK_SDIV_DAT RRSD1 ");
        stb.append("        ON RRSD1.YEAR          = SRD.YEAR ");
        stb.append("       AND RRSD1.SEMESTER      = TCNS.SEMESTER ");
        stb.append("       AND RRSD1.TESTKINDCD    = TCNS.TESTKINDCD ");
        stb.append("       AND RRSD1.TESTITEMCD    = TCNS.TESTITEMCD ");
        stb.append("       AND RRSD1.SCORE_DIV     = RSD.SCORE_DIV ");
        stb.append("       AND RRSD1.CLASSCD       = RSD.CLASSCD ");
        stb.append("       AND RRSD1.CURRICULUM_CD = RSD.CURRICULUM_CD ");
        stb.append("       AND RRSD1.SCHREGNO      = SRD.SCHREGNO ");
        stb.append("       AND RRSD1.SUBCLASSCD    = SM.SUBCLASSCD ");
        stb.append(" LEFT JOIN RECORD_AVERAGE_SDIV_DAT RASD1 ");
        stb.append("        ON RASD1.YEAR          = SRD.YEAR ");
        stb.append("       AND RASD1.SEMESTER      = TCNS.SEMESTER ");
        stb.append("       AND RASD1.TESTKINDCD    = TCNS.TESTKINDCD ");
        stb.append("       AND RASD1.TESTITEMCD    = TCNS.TESTITEMCD ");
        stb.append("       AND RASD1.SCORE_DIV     = TCNS.SCORE_DIV ");
        stb.append("       AND RASD1.CLASSCD       = RSD.CLASSCD ");
        stb.append("       AND RASD1.CURRICULUM_CD = RSD.CURRICULUM_CD ");
        stb.append("       AND RASD1.SUBCLASSCD    = SM.SUBCLASSCD ");
        stb.append("       AND RASD1.AVG_DIV       = '2' "); //クラス平均を取得するときは2
        stb.append("       AND RASD1.GRADE         = SRD.GRADE ");
        stb.append("       AND RASD1.HR_CLASS      = SRD.HR_CLASS ");
        //講座順位、平均
        stb.append(" LEFT JOIN RECORD_RANK_CHAIR_SDIV_DAT RRCS ");
        stb.append("        ON RRCS.YEAR           = SRD.YEAR ");
        stb.append("       AND RRCS.SEMESTER       = TCNS.SEMESTER ");
        stb.append("       AND RRCS.TESTKINDCD     = TCNS.TESTKINDCD ");
        stb.append("       AND RRCS.TESTITEMCD     = TCNS.TESTITEMCD ");
        stb.append("       AND RRCS.SCORE_DIV      = RSD.SCORE_DIV ");
        stb.append("       AND RRCS.CLASSCD        = RRCS.CLASSCD ");
        stb.append("       AND RRCS.CURRICULUM_CD  = RSD.CURRICULUM_CD ");
        stb.append("       AND RRCS.SUBCLASSCD     = SM.SUBCLASSCD ");
        stb.append("       AND RRCS.CHAIRCD        = RSD.CHAIRCD ");
        stb.append("       AND RRCS.SCHREGNO       = SRD.SCHREGNO ");
        stb.append(" LEFT JOIN RECORD_AVERAGE_CHAIR_SDIV_DAT RAC ");
        stb.append("        ON RAC.YEAR            = SRD.YEAR ");
        stb.append("       AND RAC.SEMESTER        = TCNS.SEMESTER ");
        stb.append("       AND RAC.TESTKINDCD      = TCNS.TESTKINDCD ");
        stb.append("       AND RAC.TESTITEMCD      = TCNS.TESTITEMCD ");
        stb.append("       AND RAC.SCORE_DIV       = RSD.SCORE_DIV ");
        stb.append("       AND RAC.CLASSCD         = RSD.CLASSCD ");
        stb.append("       AND RAC.CURRICULUM_CD   = RSD.CURRICULUM_CD ");
        stb.append("       AND RAC.SUBCLASSCD      = SM.SUBCLASSCD ");
        stb.append("       AND RAC.CHAIRCD         = RSD.CHAIRCD ");
        stb.append("       AND RAC.AVG_DIV         = '1' ");
        //類型平均、順位(※画面で選択された考査が対象)
        stb.append(" LEFT JOIN CHAIR_GROUP_SDIV_DAT AS CHRGRP_1 ");
        stb.append("        ON CHRGRP_1.YEAR          = TCNS.YEAR ");
        stb.append("       AND CHRGRP_1.SEMESTER      = TCNS.SEMESTER ");
        stb.append("       AND CHRGRP_1.TESTKINDCD    = TCNS.TESTKINDCD ");
        stb.append("       AND CHRGRP_1.TESTITEMCD    = TCNS.TESTITEMCD ");
        stb.append("       AND CHRGRP_1.SCORE_DIV     = RSD.SCORE_DIV ");
        stb.append("       AND CHRGRP_1.CHAIRCD       = RSD.CHAIRCD ");
        stb.append(" LEFT JOIN RECORD_AVERAGE_SDIV_DAT RASD2_1 ");
        stb.append("        ON RASD2_1.YEAR          = SRD.YEAR ");
        stb.append("       AND RASD2_1.SEMESTER      = TCNS.SEMESTER ");
        stb.append("       AND RASD2_1.TESTKINDCD    = TCNS.TESTKINDCD ");
        stb.append("       AND RASD2_1.TESTITEMCD    = TCNS.TESTITEMCD ");
        stb.append("       AND RASD2_1.SCORE_DIV     = RSD.SCORE_DIV ");
        stb.append("       AND RASD2_1.CLASSCD       = RSD.CLASSCD ");
        stb.append("       AND RASD2_1.CURRICULUM_CD = RSD.CURRICULUM_CD ");
        stb.append("       AND RASD2_1.SUBCLASSCD    = SM.SUBCLASSCD ");
        stb.append("       AND RASD2_1.AVG_DIV       = '6' "); //類型平均を取得するときは6を条件
        stb.append("       AND RASD2_1.GRADE         = SRD.GRADE ");
        stb.append("       AND RASD2_1.HR_CLASS      = '000' ");//AVG_DIV=6(類型平均)のレコードはHR_CLASSが000で固定
        stb.append("       AND RASD2_1.MAJORCD       = CHRGRP_1.CHAIR_GROUP_CD ");
        //類型平均、順位(※全考査(TESTITEMCD,TESTITEMCD,SCORE_DIV='00')が対象)
        stb.append(" LEFT JOIN CHAIR_GROUP_SDIV_DAT AS CHRGRP_2 ");
        stb.append("        ON CHRGRP_2.YEAR          = TCNS.YEAR ");
        stb.append("       AND CHRGRP_2.SEMESTER      = TCNS.SEMESTER ");
        stb.append("       AND CHRGRP_2.TESTKINDCD    = '00' ");
        stb.append("       AND CHRGRP_2.TESTITEMCD    = '00' ");
        stb.append("       AND CHRGRP_2.SCORE_DIV     = '00' ");
        stb.append("       AND CHRGRP_2.CHAIRCD       = RSD.CHAIRCD ");
        stb.append(" LEFT JOIN RECORD_AVERAGE_SDIV_DAT RASD2_2 ");
        stb.append("        ON RASD2_2.YEAR          = SRD.YEAR ");
        stb.append("       AND RASD2_2.SEMESTER      = TCNS.SEMESTER ");
        stb.append("       AND RASD2_2.TESTKINDCD    = TCNS.TESTKINDCD ");
        stb.append("       AND RASD2_2.TESTITEMCD    = TCNS.TESTITEMCD ");
        stb.append("       AND RASD2_2.SCORE_DIV     = RSD.SCORE_DIV ");
        stb.append("       AND RASD2_2.CLASSCD       = RSD.CLASSCD ");
        stb.append("       AND RASD2_2.CURRICULUM_CD = RSD.CURRICULUM_CD ");
        stb.append("       AND RASD2_2.SUBCLASSCD    = SM.SUBCLASSCD ");
        stb.append("       AND RASD2_2.AVG_DIV       = '6' "); //類型平均を取得するときは6を条件
        stb.append("       AND RASD2_2.GRADE         = SRD.GRADE ");
        stb.append("       AND RASD2_2.HR_CLASS      = '000' ");//AVG_DIV=6(類型平均)のレコードはHR_CLASSが000で固定
        stb.append("       AND RASD2_2.MAJORCD       = CHRGRP_2.CHAIR_GROUP_CD ");
        stb.append(" LEFT JOIN RECORD_RANK_SDIV_DAT RRSD2 ");
        stb.append("        ON RRSD2.SCHREGNO      = SRD.SCHREGNO ");
        stb.append("       AND RRSD2.YEAR          = SRD.YEAR ");
        stb.append("       AND RRSD2.SEMESTER      = TCNS.SEMESTER ");
        stb.append("       AND RRSD2.TESTKINDCD    = TCNS.TESTKINDCD ");
        stb.append("       AND RRSD2.TESTITEMCD    = TCNS.TESTITEMCD ");
        stb.append("       AND RRSD2.SCORE_DIV     = TCNS.SCORE_DIV ");
        stb.append("       AND RRSD2.CLASSCD       = RSD.CLASSCD ");
        stb.append("       AND RRSD2.SCHOOL_KIND   = '" + _param._schoolKind + "' ");
        stb.append("       AND RRSD2.CURRICULUM_CD = RSD.CURRICULUM_CD ");
        stb.append("       AND RRSD2.SUBCLASSCD    = SM.SUBCLASSCD ");
        stb.append(" LEFT JOIN RECORD_RANK_SDIV_DAT RRSD_SUM ");
        stb.append("        ON RRSD_SUM.SCHREGNO      = SRD.SCHREGNO ");
        stb.append("       AND RRSD_SUM.YEAR          = SRD.YEAR ");
        stb.append("       AND RRSD_SUM.SEMESTER      = TCNS.SEMESTER ");
        stb.append("       AND RRSD_SUM.TESTKINDCD    = TCNS.TESTKINDCD ");
        stb.append("       AND RRSD_SUM.TESTITEMCD    = TCNS.TESTITEMCD ");
        stb.append("       AND RRSD_SUM.SCORE_DIV     = TCNS.SCORE_DIV ");
        stb.append("       AND RRSD_SUM.SCHOOL_KIND   = '" + _param._schoolKind + "' ");
        stb.append("       AND RRSD_SUM.SUBCLASSCD    = '999999' ");
        stb.append("     WHERE SM.SUBCLASSCD IS NOT NULL");
        stb.append("  ORDER BY SRD.GRADE ");
        stb.append("         , SRD.HR_CLASS ");
        stb.append("         , SRD.ATTENDNO ");
        stb.append("         , SM.SUBCLASSCD ");

        return stb.toString();
    }

    private Param getParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        KNJServletUtils.debugParam(request, log);
        log.fatal("$Revision$ $Date$");//CVSキーワードの取り扱いに注意
        Param param = new Param(db2, request);
        return param;
    }

    //表組用印字データ
    private class TableData {
        final String _grade;     //学年
        final String _hrClass;   //クラス
        final String _subclassCd;//科目
        final String _count;     //人数

        public TableData(
            final String grade,
            final String hrClass,
            final String subclassCd,
            final String count
        ) {
            _grade      = grade;
            _hrClass    = hrClass;
            _subclassCd = subclassCd;
            _count      = count;
        }
    }

    //グラフ印字データ
    private class GraphData {
        final String _subclassCd;  //科目コード
        final String _subclassName;//科目名
        final String _score;       //得点
        final String _average;     //平均点

        public GraphData(
            final String subclassCd,
            final String subclassName,
            final String score,
            final String average
        ) {
            _subclassCd   = subclassCd;
            _subclassName = subclassName;
            _score        = score;
            _average      = average;
        }
    }

    private class Param {
        private final String _schoolKind;
        private final String _year;
        private final String _semester;
        private final String _grade;
        private final String _hrClass;
        private final String _testcd;
        private final String _kubun;
        private final String _type;
        private final String[] _class;
        final boolean _isOutputDebug;
        private final String _documentRoot;
        private String _imagepath;
        private String _extension;

        /** グラフイメージファイルの Set<File> */
        final Set _graphFiles = new HashSet();

        public Param(final DB2UDB db2, final HttpServletRequest request) throws Exception {
            _schoolKind    = request.getParameter("SCHOOLKIND");
            _year          = request.getParameter("YEAR");                      //年度
            _semester      = request.getParameter("SEMESTER");                  //学期
            _grade         = request.getParameter("GRADE");                     //学年
            _hrClass       = request.getParameter("HR_CLASS");                  //クラス
            _testcd        = request.getParameter("TESTCD");                    //テスト
            _kubun         = request.getParameter("CATEGORY_IS_CLASS");         //表示区分
            _type          = request.getParameter("type_course");               //順位・類型ラジオボタン
            _class         = request.getParameterValues("CATEGORY_SELECTED");   //選択クラス
            _documentRoot  = request.getParameter("DOCUMENTROOT");

            _isOutputDebug = "1".equals(getDbPrginfoProperties(db2, "outputDebug"));
        }

        private String getDbPrginfoProperties(final DB2UDB db2, final String propName) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJD157A' AND NAME = '" + propName + "' "));
        }
    }

}//クラスの括り
