/**     プロジェクト/プロパティ/javaコード・スタイル/コード・テンプレート/パターン       ***/

/*
 * $Id: 706cf70e148663434023f670b156e1b081a41871 $
 *
 * 作成日: 2018/09/05
 * 作成者: yamashiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.awt.BasicStroke;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJD659D {

    private static final Log log = LogFactory.getLog(KNJD659D.class);

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

            log.fatal("$Revision: 71144 $ $Date: 2019-12-10 19:05:56 +0900 (火, 10 12 2019) $"); // CVSキーワードの取り扱いに注意
            KNJServletUtils.debugParam(request, log);

            _param = createParam(db2, request);

            _hasData = false;

            try {
                if (!KNJServletUtils.isEnableGraph(log)) {
                    log.fatal("グラフを使える環境ではありません。");
                }

                printMain(db2, svf);

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

                if (null != _param) {
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
            svf.VrQuit();
        }

    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrSetForm("KNJD659D.frm", 1);

        final List studentList = getStudentList(db2);
        final Map jviewRecordMap = getJviewRecordMap(db2);
        for (Iterator itStudent = studentList.iterator(); itStudent.hasNext();) {
            final Student student = (Student) itStudent.next();
            svf.VrsOut("TITLE", "成績グラフ");
            svf.VrsOut("HR_NAME", student._hrName + "-" + student._attendNo);
            svf.VrsOut("NAME", student._name);
            int fieldCnt = 1;

            for (Iterator itSubclass = _param._subclassList.iterator(); itSubclass.hasNext();) {
                final SubClass subClass = (SubClass) itSubclass.next();
                svf.VrsOut("SUBCLASS_NAME" + fieldCnt, subClass._subclassName);
                int viewCnt = 1;

                // グラフ用のデータ作成
                final DefaultCategoryDataset scoreDataset = new DefaultCategoryDataset();

                for (Iterator itViewCd = subClass._jviewCdList.iterator(); itViewCd.hasNext();) {
                    final ViewMst viewMst = (ViewMst) itViewCd.next();
                    final String setField = fieldCnt + "_" + viewCnt;
                    final String setField2;
                    if (KNJ_EditEdit.getMS932ByteLength(viewMst._viewName) > 12) {
                        setField2 = fieldCnt + "_" + viewCnt + "_2";
                    } else {
                        setField2 = setField;
                    }
                    svf.VrsOut("ITEM_NAME" + setField2, viewMst._viewName);

                    int grade = 5;
                    String befYear = "";
                    for (Iterator itSemester = _param._semesterMap.keySet().iterator(); itSemester.hasNext();) {
                        final String yearSeme = (String) itSemester.next();
                        final Semester semester = (Semester) _param._semesterMap.get(yearSeme);
                        final String scoreKey = yearSeme + student._schregNo + subClass._classCd + subClass._schoolKind + subClass._curriculumCd + subClass._subclassCd + viewMst._viewCd;

                        Integer score = null;
                        if (jviewRecordMap.containsKey(scoreKey)) {
                            final JviewRecord jviewRecord = (JviewRecord) jviewRecordMap.get(scoreKey);
                            svf.VrsOutn("SCORE" + setField, semester._fieldNo, jviewRecord._score);
                            if (NumberUtils.isNumber(jviewRecord._score)) {
                            	score = Integer.valueOf(jviewRecord._score);
                            }
                        }

                        if (!"".equals(befYear) && !befYear.equals(semester._year)) {
                            grade++;
                        }
                        // 得点、凡例、横軸のタイトル
                        scoreDataset.addValue(score, viewMst._viewName, String.valueOf(grade) + "年" + semester._semesterName);
                        befYear = semester._year;
                    }
                    viewCnt++;
                }
                //画像を作成
                final Chart chart = new Chart();
                final File chartFile = chart.getLineChartFile(scoreDataset);
                //画像のパス
                final String chartPath = chartFile.getAbsolutePath();
                svf.VrsOut("GRAPH" + fieldCnt, chartPath);
                fieldCnt++;
            }

            svf.VrEndPage();
            _hasData = true;
        }
    }

    private List getStudentList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getStudentSql();
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String schregNo = rs.getString("SCHREGNO");
                final String hrName = rs.getString("HR_NAME");
                final String attendNo = rs.getString("ATTENDNO");
                final String name = rs.getString("NAME");

                final Student student = new Student(schregNo, hrName, attendNo, name);
                retList.add(student);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String getStudentSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     REGD.SCHREGNO, ");
        stb.append("     REGDH.HR_NAME, ");
        stb.append("     REGD.ATTENDNO, ");
        stb.append("     BASE.NAME ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT REGD ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT REGDH ON REGD.YEAR = REGDH.YEAR ");
        stb.append("          AND REGD.SEMESTER = REGDH.SEMESTER ");
        stb.append("          AND REGD.GRADE = REGDH.GRADE ");
        stb.append("          AND REGD.HR_CLASS = REGDH.HR_CLASS ");
        stb.append("     INNER JOIN SCHREG_BASE_MST BASE ON REGD.SCHREGNO = BASE.SCHREGNO ");
        stb.append(" WHERE ");
        stb.append("     REGD.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("     AND REGD.SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND REGD.GRADE || REGD.HR_CLASS IN " + SQLUtils.whereIn(true, _param._classSelected) + " ");
        stb.append(" ORDER BY ");
        stb.append("     REGD.GRADE, ");
        stb.append("     REGD.HR_CLASS, ");
        stb.append("     REGD.ATTENDNO ");

        log.debug(stb.toString());
        return stb.toString();
    }

    private class Student {
        final String _schregNo;
        final String _hrName;
        final String _attendNo;
        final String _name;
        public Student(final String schregNo, final String hrName, final String attendNo, final String name) {
            _schregNo = schregNo;
            _hrName = hrName;
            _attendNo = attendNo;
            _name = name;
        }
    }

    private Map getJviewRecordMap(final DB2UDB db2) {
        final Map retMap = new HashMap();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getJviewRecordSql();
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String year = rs.getString("YEAR");
                final String semester = rs.getString("SEMESTER");
                final String schregNo = rs.getString("SCHREGNO");
                final String classCd = rs.getString("CLASSCD");
                final String schoolKind = rs.getString("SCHOOL_KIND");
                final String curriculumCd = rs.getString("CURRICULUM_CD");
                final String subclassCd = rs.getString("SUBCLASSCD");
                final String viewCd = rs.getString("VIEWCD");
                final String score = rs.getString("SCORE");

                final JviewRecord jviewRecord = new JviewRecord(year, semester, schregNo, classCd, schoolKind, curriculumCd, subclassCd, viewCd, score);
                final String setKey = year + semester + schregNo + classCd + schoolKind + curriculumCd + subclassCd + viewCd;
                retMap.put(setKey, jviewRecord);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retMap;
    }

    private String getJviewRecordSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH SCH_T AS ( ");
        stb.append(getStudentSql());
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     * ");
        stb.append(" FROM ");
        stb.append("     JVIEWSTAT_RECORD_DAT RECD ");
        stb.append(" WHERE ");
        stb.append("     RECD.YEAR IN " + _param._yearInState + " ");
        stb.append("     AND RECD.YEAR || RECD.SEMESTER <= '" + (_param._ctrlYear + _param._semester) + "' ");
        stb.append("     AND RECD.SCHREGNO IN (SELECT SCHREGNO FROM SCH_T) ");
        stb.append(" ORDER BY ");
        stb.append("     SCHREGNO, ");
        stb.append("     CLASSCD, ");
        stb.append("     SCHOOL_KIND, ");
        stb.append("     CURRICULUM_CD, ");
        stb.append("     SUBCLASSCD, ");
        stb.append("     YEAR, ");
        stb.append("     SEMESTER, ");
        stb.append("     VIEWCD ");

        log.debug(stb.toString());
        return stb.toString();
    }

    private class JviewRecord {
        final String _year;
        final String _semester;
        final String _schregNo;
        final String _classCd;
        final String _schoolKind;
        final String _curriculumCd;
        final String _subclassCd;
        final String _viewCd;
        final String _score;

        JviewRecord(
                final String year,
                final String semester,
                final String schregNo,
                final String classCd,
                final String schoolKind,
                final String curriculumCd,
                final String subclassCd,
                final String viewCd,
                final String score
        ) {
            _year = year;
            _semester = semester;
            _schregNo = schregNo;
            _classCd = classCd;
            _schoolKind = schoolKind;
            _curriculumCd = curriculumCd;
            _subclassCd = subclassCd;
            _viewCd = viewCd;
            _score = score;
        }

    }

    private class Chart {

        private File getLineChartFile(final DefaultCategoryDataset scoreDataset) {

            try {
                // チャート作成
                final JFreeChart chart = createLineChart(scoreDataset);

                // グラフのファイルを生成
                final File outputFile = graphImageFile(chart, 1890, 952);
                _param._graphFiles.add(outputFile);

                if (outputFile.exists()) {
                    return outputFile;
                }
            } catch (Throwable e) {
                log.error("exception or error!", e);
            }
            return null;
        }

        private JFreeChart createLineChart(
                final DefaultCategoryDataset deviationDataset
        ) {
            //凡例の有無。後ろから３番目のboolean引数で指定
            final JFreeChart chart = ChartFactory.createLineChart(null, null, null, deviationDataset, PlotOrientation.VERTICAL, true, false, false);
            final CategoryPlot plot = chart.getCategoryPlot();
            plot.setRangeGridlineStroke(new BasicStroke(2.0f)); // 目盛りの太さ
            final CategoryAxis categoryAxis = plot.getDomainAxis();
            categoryAxis.setTickLabelsVisible(true);
            categoryAxis.setTickLabelFont(categoryAxis.getTickLabelFont().deriveFont(4));
            plot.setRangeGridlinePaint(Color.gray);
            plot.setRangeGridlineStroke(new BasicStroke(1.0f));
            plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
            plot.setBackgroundPaint(Color.lightGray.brighter());

            final NumberAxis numberAxis = new NumberAxis();
            //縦軸の刻み 10点刻み
            numberAxis.setTickUnit(new NumberTickUnit(10));
            numberAxis.setTickLabelsVisible(true);
            //縦軸の範囲 50点から100点
            numberAxis.setRange(50, 100.0);
            plot.setRangeAxis(numberAxis);

            final CategoryItemRenderer renderer = new LineAndShapeRenderer();

            //凡例1の指定
            renderer.setSeriesPaint(0, Color.black);
            //実線
            renderer.setSeriesStroke(0, new BasicStroke(1.5f));

            //凡例2の指定
            renderer.setSeriesPaint(1, Color.black);
            //短い破線
            float dash1 [] = {4f, 5f};
            renderer.setSeriesStroke(1, new BasicStroke(1.5f,BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_MITER,
                    10.0f,
                    dash1,
                    0.0f));

            //凡例3の指定
            renderer.setSeriesPaint(2, Color.black);
            //長い破線
            float dash0 [] = {4f, 10f};
            renderer.setSeriesStroke(2, new BasicStroke(1.5f,BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_MITER,
                    10.0f,
                    dash0,
                    0.0f));

            plot.setRenderer(renderer);

            chart.setBackgroundPaint(Color.white);

            return chart;
        }

        private File graphImageFile(final JFreeChart chart, final int dotWidth, final int dotHeight) {
            final String tmpFileName = KNJServletUtils.createTmpFile(".png");
            //log.info("\ttmp file name=" + tmpFileName);

            final File outputFile = new File(tmpFileName);
            try {
                ChartUtilities.saveChartAsPNG(outputFile, chart, dot2pixel(dotWidth), dot2pixel(dotHeight));
            } catch (final IOException ioEx) {
                log.error("グラフイメージをファイル化できません。", ioEx);
            }

            return outputFile;
        }

        private int dot2pixel(final int dot) {
            final int pixel = dot / 4;   // おおよそ4分の1程度でしょう。

            /*
             * 少し大きめにする事でSVFに貼り付ける際の拡大を防ぐ。
             * 拡大すると粗くなってしまうから。
             */
            return (int) (pixel * 1.3);
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 71144 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    private class SubClass {
        final String _classCd;
        final String _schoolKind;
        final String _curriculumCd;
        final String _subclassCd;
        final String _subclassAbbv;
        final String _subclassName;
        final List _jviewCdList;

        SubClass(
                final String classCd,
                final String schoolKind,
                final String curriculumCd,
                final String subclassCd,
                final String subclassAbbv,
                final String subclassName
        ) {
            _classCd = classCd;
            _schoolKind = schoolKind;
            _curriculumCd = curriculumCd;
            _subclassCd = subclassCd;
            _subclassAbbv = subclassAbbv;
            _subclassName = subclassName;
            _jviewCdList = new ArrayList();
        }

    }

    private class ViewMst {
        final String _viewCd;
        final String _viewName;
        public ViewMst(
                final String viewCd,
                final String viewName
        ) {
            _viewCd = viewCd;
            _viewName = viewName;
        }
    }

    private class Semester {
        final String _year;
        final String _semester;
        final String _semesterName;
        final int _fieldNo;
        public Semester(
                final String year,
                final String semester,
                final String semesterName,
                final int fieldNo
        ) {
            _year = year;
            _semester = semester;
            _semesterName = semesterName;
            _fieldNo = fieldNo;
        }
    }

    /** パラメータクラス */
    private class Param {
        final String[] _classSelected;
        final String _ctrlDate;
        final String _ctrlSemester;
        final String _ctrlYear;
        final String _grade;
        final String _printLogRemoteAddr;
        final String _printLogRemoteIdent;
        final String _printLogStaffcd;
        final String _semester;
        final String _schoolKind;
        final boolean _useLastYear;
        final String _yearInState;
        final String _gradeInState;
        final List _patternCdList;
        final List _subclassList;
        final Map _semesterMap;

        /** グラフイメージファイルの Set&lt;File&gt; */
        final Set _graphFiles = new HashSet();

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _classSelected = request.getParameterValues("CLASS_SELECTED");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _grade = request.getParameter("GRADE");
            _printLogRemoteAddr = request.getParameter("PRINT_LOG_REMOTE_ADDR");
            _printLogRemoteIdent = request.getParameter("PRINT_LOG_REMOTE_IDENT");
            _printLogStaffcd = request.getParameter("PRINT_LOG_STAFFCD");
            _semester = request.getParameter("SEMESTER");
            _schoolKind = request.getParameter("SCHOOL_KIND");
            _useLastYear = getUseLastYear(db2);
            if (_useLastYear) {
                _yearInState = "('" + (Integer.parseInt(_ctrlYear) - 1) + "','" + _ctrlYear + "')";
            } else {
                _yearInState = "('" + _ctrlYear + "')";
            }
            if (_useLastYear) {
                _gradeInState = "('" + (Integer.parseInt(_grade) - 1) + "','" + _grade + "')";
            } else {
                _gradeInState = "('" + _grade + "')";
            }
            _patternCdList = getPatternCdList(db2);
            _subclassList = getSubclassList(db2);
            _semesterMap = getSemesterMap(db2);

        }

        private boolean getUseLastYear(final DB2UDB db2) {
            boolean retBool = false;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     COUNT(*) AS CNT ");
                stb.append(" FROM ");
                stb.append("     NAME_MST ");
                stb.append(" WHERE ");
                stb.append("     NAMECD1 = 'A023' ");
                stb.append("     AND '" + _grade + "' BETWEEN NAMESPARE2 AND NAMESPARE3 ");

                log.debug(stb.toString());
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    retBool = rs.getInt("CNT") > 0;
                }
            } catch (SQLException ex) {
                log.debug("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retBool;
        }

        private Map getSemesterMap(final DB2UDB db2) {
            final Map retMap = new TreeMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     * ");
                stb.append(" FROM ");
                stb.append("     SEMESTER_MST ");
                stb.append(" WHERE ");
                if (_useLastYear) {
                    stb.append("     YEAR IN ('" + (Integer.parseInt(_ctrlYear) - 1) + "','" + _ctrlYear + "') ");
                } else {
                    stb.append("     YEAR IN ('" + (Integer.parseInt(_ctrlYear) + 1) + "','" + _ctrlYear + "') ");
                }
                stb.append("     AND SEMESTER <> '9' ");
                stb.append(" ORDER BY ");
                stb.append("     YEAR, ");
                stb.append("     SEMESTER ");

                log.debug(stb.toString());
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                int scoreFieldCnt = 1;
                while (rs.next()) {
                    final String year = rs.getString("YEAR");
                    final String semester = rs.getString("SEMESTER");
                    final String semesterName = rs.getString("SEMESTERNAME");
                    final Semester seme = new Semester(year, semester, semesterName, scoreFieldCnt);
                    retMap.put(year + semester, seme);
                    scoreFieldCnt++;
                }
            } catch (SQLException ex) {
                log.debug("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retMap;
        }

        private List getPatternCdList(final DB2UDB db2) {
            final List retList = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     JVIEW_LPD.PATTERN_CD ");
                stb.append(" FROM ");
                stb.append("     (SELECT ");
                stb.append("          YEAR, ");
                stb.append("          SCHOOL_KIND, ");
                stb.append("          PATTERN_CD ");
                stb.append("      FROM ");
                stb.append("          JVIEWSTAT_LEVEL_PATTERN_YMST ");
                stb.append("      WHERE ");
                stb.append("          YEAR IN " + _yearInState + " ");
                stb.append("          AND SCHOOL_KIND = '" + _schoolKind + "' ");
                stb.append("          AND PERFECT = 100 ");
                stb.append("     ) AS JVIEW_LPY, ");
                stb.append("     JVIEWSTAT_LEVEL_PATTERN_DAT JVIEW_LPD ");
                stb.append(" WHERE ");
                stb.append("     JVIEW_LPY.YEAR = JVIEW_LPD.YEAR ");
                stb.append("     AND JVIEW_LPY.SCHOOL_KIND = JVIEW_LPD.SCHOOL_KIND ");
                stb.append("     AND JVIEW_LPY.PATTERN_CD = JVIEW_LPD.PATTERN_CD ");
                stb.append(" GROUP BY ");
                stb.append("     JVIEW_LPD.PATTERN_CD ");
                stb.append(" HAVING ");
                stb.append("     MAX(ASSESSLEVEL) = 5 ");

                log.debug(stb.toString());
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    retList.add(rs.getString("PATTERN_CD"));
                }
            } catch (SQLException ex) {
                log.debug("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retList;
        }

        private List getSubclassList(final DB2UDB db2) {
            final List retList = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String[] patternArray = new String[_patternCdList.size()];
                _patternCdList.toArray(patternArray);

                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT DISTINCT ");
                stb.append("     JVIEW_SUB.CLASSCD, ");
                stb.append("     JVIEW_SUB.SCHOOL_KIND, ");
                stb.append("     JVIEW_SUB.CURRICULUM_CD, ");
                stb.append("     JVIEW_SUB.SUBCLASSCD, ");
                stb.append("     MAX(SUBM.SUBCLASSABBV) AS SUBCLASSABBV, ");
                stb.append("     MAX(SUBM.SUBCLASSNAME) AS SUBCLASSNAME, ");
                stb.append("     JVIEW_SUB.VIEWCD, ");
                stb.append("     VALUE(JVIEWM.VIEWABBV, JVIEWM.VIEWNAME) AS VIEWNAME ");
                stb.append(" FROM ");
                stb.append("     JVIEWSTAT_SUBCLASS_PATTERN_DAT JVIEW_SUB   ");
                stb.append("     LEFT JOIN SUBCLASS_MST SUBM ON JVIEW_SUB.CLASSCD = SUBM.CLASSCD   ");
                stb.append("          AND JVIEW_SUB.SCHOOL_KIND = SUBM.SCHOOL_KIND   ");
                stb.append("          AND JVIEW_SUB.CURRICULUM_CD = SUBM.CURRICULUM_CD   ");
                stb.append("          AND JVIEW_SUB.SUBCLASSCD = SUBM.SUBCLASSCD ");
                stb.append("     LEFT JOIN JVIEWNAME_GRADE_MST JVIEWM ON JVIEW_SUB.GRADE = JVIEWM.GRADE ");
                stb.append("          AND JVIEW_SUB.CLASSCD = JVIEWM.CLASSCD   ");
                stb.append("          AND JVIEW_SUB.SCHOOL_KIND = JVIEWM.SCHOOL_KIND   ");
                stb.append("          AND JVIEW_SUB.CURRICULUM_CD = JVIEWM.CURRICULUM_CD   ");
                stb.append("          AND JVIEW_SUB.SUBCLASSCD = JVIEWM.SUBCLASSCD ");
                stb.append("          AND JVIEW_SUB.VIEWCD = JVIEWM.VIEWCD ");
                stb.append(" WHERE ");
                stb.append("     JVIEW_SUB.YEAR IN " + _yearInState + " ");
                stb.append("     AND JVIEW_SUB.GRADE IN " + _gradeInState + " ");
                stb.append("     AND JVIEW_SUB.PATTERN_CD IN " + SQLUtils.whereIn(true, patternArray) + " ");
                stb.append(" GROUP BY ");
                stb.append("     JVIEW_SUB.CLASSCD, ");
                stb.append("     JVIEW_SUB.SCHOOL_KIND, ");
                stb.append("     JVIEW_SUB.CURRICULUM_CD, ");
                stb.append("     JVIEW_SUB.SUBCLASSCD, ");
                stb.append("     JVIEW_SUB.VIEWCD, ");
                stb.append("     JVIEWM.VIEWNAME, ");
                stb.append("     JVIEWM.VIEWABBV ");
                stb.append(" ORDER BY ");
                stb.append("     JVIEW_SUB.CLASSCD, ");
                stb.append("     JVIEW_SUB.SCHOOL_KIND, ");
                stb.append("     JVIEW_SUB.CURRICULUM_CD, ");
                stb.append("     JVIEW_SUB.SUBCLASSCD, ");
                stb.append("     JVIEW_SUB.VIEWCD ");

                log.debug(stb.toString());
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                String befSubclass = "";
                SubClass subClass = null;
                while (rs.next()) {
                    final String classCd = rs.getString("CLASSCD");
                    final String schoolKind = rs.getString("SCHOOL_KIND");
                    final String curriculumCd = rs.getString("CURRICULUM_CD");
                    final String subclassCd = rs.getString("SUBCLASSCD");
                    final String subclassAbbv = rs.getString("SUBCLASSABBV");
                    final String subclassName = rs.getString("SUBCLASSNAME");
                    final String viewCd = rs.getString("VIEWCD");
                    final String viewName = StringUtils.replace(StringUtils.replace(rs.getString("VIEWNAME"), "　", ""), " ", ""); // 空白をカット
                    if (!befSubclass.equals(classCd + schoolKind + curriculumCd + subclassCd)) {
                        subClass = new SubClass(classCd, schoolKind, curriculumCd, subclassCd, subclassAbbv, subclassName);
                        retList.add(subClass);
                    }
                    final ViewMst viewMst = new ViewMst(viewCd, viewName);
                    subClass._jviewCdList.add(viewMst);
                    befSubclass = classCd + schoolKind + curriculumCd + subclassCd;
                }
            } catch (SQLException ex) {
                log.debug("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retList;
        }

    }
}

// eof
