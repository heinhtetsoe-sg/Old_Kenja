// kanji=漢字
/*
 * $Id: b45211a1c5a555f6fa3934396793875a7b547784 $
 *
 * 作成日: 2008/05/24 10:02:00 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.CategoryItemEntity;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.PlotState;
import org.jfree.chart.plot.SpiderWebPlot;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.ui.RectangleInsets;
import org.jfree.util.TableOrder;

import servletpack.KNJZ.detail.KNJServletUtils;

/**
 * 個人成績票。
 * @author takaesu
 * @version $Id: b45211a1c5a555f6fa3934396793875a7b547784 $
 */
public class KNJD105G {
    private static final Log log = LogFactory.getLog(KNJD105G.class);

    /** レーダーチャート凡例画像. */
    private static final String RADER_CHART_LEGEND = "RaderChartLegend.png";
    /** レーダーチャート凡例画像偏差値50なし. */
    private static final String RADER_CHART_LEGEND_NO50 = "RaderChartLegendNo50.png";
    /** レーダーチャート凡例画像偏差値50なし前回有り. */
    private static final String RADER_CHART_LEGEND_NO50_WITH_BEFORE = "RaderChartLegendNo50WithBefore.png";
//    /** 棒グラフ凡例画像1(学年). */
//    private static final String BAR_CHART_LEGEND1 = "BarChartLegendGrade.png";
//    /** 棒グラフ凡例画像2(コース). */
//    private static final String BAR_CHART_LEGEND2 = "BarChartLegendCourse.png";
//    /** 棒グラフ凡例画像3(コースグループ). */
//    private static final String BAR_CHART_LEGEND3 = "BarChartLegendCourseGroup.png";

    /** 成績表の最大科目数. */
    private static final int TABLE_SUBCLASS_MAX = 25;
    /** 棒グラフの最大科目数. */
    private static final int BAR_GRAPH_MAX_ITEM = 11;
    
    private static final String ALL3 = "333333";
    private static final String ALL5 = "555555";
    private static final String ALL9 = "999999";

    private static final String GROUP_DIV_GRADE = "1";
    private static final String GROUP_DIV_COURSEGROUP = "3";

    private boolean _hasData;

    /** グラフイメージファイルの Set&lt;File&gt; */
    private final Set _graphFiles = new HashSet();
    
    private Param _param;

    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {
        DB2UDB db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
        try {
            db2.open();
        } catch (final Exception ex) {
            log.error("db open error!", ex);
            return;
        }
        log.info("★マスタ関連の読込み");
        _param = createParam(request, db2);

        Vrw32alp svf = new Vrw32alp();
        if (svf.VrInit() < 0) {
            throw new IllegalStateException("svf初期化失敗");
        }
        svf.VrSetSpoolFileStream(response.getOutputStream());
        response.setContentType("application/pdf");

        // 対象の生徒たちを得る
        final List students = createStudents(db2, _param);
        if (students.size() > 0) {
        	try {
                // 成績のデータを読む
                log.info("★成績関連の読込み");
                loadExam(db2, _param, students, _param._thisExam);

                log.info("★成績関連の読込み(前回の成績)");
                for (final Iterator it = _param._beforeExamList.iterator(); it.hasNext();) {
                    final Exam exam = (Exam) it.next();
                    loadExam(db2, _param, students, exam);
                }

                // 印刷する
                log.info("★印刷");
                for (final Iterator it = students.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();
                    printStudent(_param, svf, student);
                }
                _hasData = true;
        		
                log.info("★終了処理");
                if (!_hasData) {
                    svf.VrSetForm("MES001.frm", 0);
                    svf.VrsOut("note", "note");
                    svf.VrEndPage();
                }
        	} catch (Exception e) {
        		log.error("exception!", e);
        	} finally {
                svf.VrQuit();

                if (null != db2) {
                    db2.commit();
                    db2.close();
                }
                removeImageFiles();
                log.info("Done.");
        	}
        }
    }

    private void printStudent(final Param param, Vrw32alp svf, final Student student) {
        //log.info("☆" + student + ", 科目の数=" + student._subclasses.size() + ", コースキー=" + student.courseKey());
        log.debug("今回の成績: " + student._record.get(param._thisExam));

        //log.debug("前回の成績: " + student._record.get(param._beforeExam));

        svf.VrSetForm("KNJD105G.frm", 4);
        printStatic(svf, param, student);
        
        // グラフ印字(サブフォームよりも先に印字)
//        printBarGraph(svf, param, exam, student);
        printRadarGraph(svf, param, student);
        printScoreDist(svf, param, param._thisExam, student);

        // 成績
        int print1 = printSubclass(svf, param, param._thisExam, student);

        // 前回の中間成績
        printSubclass2(svf, param, student, print1);
    }
    
    private static String scoreDistKey(final Param param, final Student student) {
        return scoreDistKey(param, student._grade, student._coursegroupCd, student._courseCd + student._majorCd + student._courseCode);
    }
    
    private static String scoreDistKey(final Param param, final String grade, final String coursegroupCd, final String course) {
        String rtnKey;
        if (GROUP_DIV_GRADE.equals(param._groupDiv)) {
            rtnKey = grade;
        } else if (GROUP_DIV_COURSEGROUP.equals(param._groupDiv)) {
            rtnKey = coursegroupCd;
        } else {
            rtnKey = course;
        }
        return rtnKey;
    }
    
    private void svfVrsOutImage(final Vrw32alp svf, final String field, final String name, final Param param) {
        final File f = new File(param._imagePath + "/" + name);
        if (f.exists()) {
            svf.VrsOut(field, f.getPath());
        }
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

    private void printScoreDist(final Vrw32alp svf, final Param param, final Exam exam, final Student student) {
        final String scoreDistKey = scoreDistKey(param, student);
        final Map subclassMap = getMappedMap(exam._distribution, scoreDistKey);
        
        final int[][] scores = new int[10][];
        final int maxScore = 100;
        final int kizami = 10;
        for (int i = 0; i < scores.length; i++) {
            final int max = i == 0 ? maxScore : maxScore - i * kizami - 1;
            final int min = maxScore - (i + 1) * kizami;
            scores[i] = new int[] {min, max};
        }
        
        log.debug(" key = " + subclassMap.keySet());
        
        final int maxColum = 16;
        svf.VrsOutn("SUBCLASS2", maxColum, "全教科　平均");
        if (null != subclassMap.get(allsubclasscd(param))) {
            BigDecimal avgavg = null;
            final List scoreListAll9 = (List) subclassMap.get(allsubclasscd(param));
            final List copyList = new ArrayList(scoreListAll9);
            for (int i = 0; i < scores.length; i++) {
                final int min = scores[i][0];
                final int max = scores[i][1];
//                log.info(" min = " + min + ", max = " + max);
                int count = 0;
                for (final Iterator sit = copyList.iterator(); sit.hasNext();) {
                    final BigDecimal score = (BigDecimal) sit.next();
                    final int iscore = sishaGonyu(score).intValue();
                    if (min <= iscore && iscore <= max) {
                        count += 1;
                    }
                }
                svf.VrsOutn("DIST" + String.valueOf(i + 1), maxColum, String.valueOf(count));
            }
            avgavg = getAvg((List) subclassMap.get(allsubclasscd(param)));

            if (null != exam._avgDat.get(avgKey(allsubclasscd(param), student))) {
                final AverageDat avgDat = (AverageDat) exam._avgDat.get(avgKey(allsubclasscd(param), student));
                svf.VrsOutn("DIST_NUM", maxColum, intnum(avgDat._count, ""));
                svf.VrsOutn("DIST_AVE", maxColum, sishaGonyuStr(avgavg));
                svf.VrsOutn("DIST_MAX", maxColum, intnum(avgDat._highscore, ""));
                svf.VrsOutn("DIST_MIN", maxColum, intnum(avgDat._lowscore, ""));
//                if (!student.hasKesshi(param, exam, null)) {
//                    svf.VrsOutn("DIST_DEV", maxColum, sishaGonyuStr(avgDat._stdDev));
//                }
            }
        }
        svf.VrsOutn("DIST_DEV", maxColum, "／");

        int column = 1;
        for (final Iterator it = student.subclasses(exam).values().iterator(); it.hasNext();) {
            final SubClass subclass = (SubClass) it.next();
//            if ("99".equals(exam.getKindCd()) && record._subClass._isMoto) { // 学期・学年成績は元科目を表示しない
//                continue;
//            }
            
            final Record record = (Record) student.record(exam).get(subclass._code);
            
            if (null == record) {
                continue;
            }
            
            if (null == subclassMap.get(record._subClass._code) && null == exam._avgDat.get(avgKey(record._subClass._code, student))) {
                continue;
            }
            if (_param._isHigh && exam.notTargetSubclasscdList(student.gradeCourse()).contains(record._subClass._code)) {
                continue;
            }
            
            if (ALL3.equals(record._subClass._code) || ALL5.equals(record._subClass._code) || ALL9.equals(record._subClass._code)) {
                continue;
            }
            
            if (maxColum <= column) {
                break;
            }
            if (exam.getAttendSubclass(student.gradeCourse()).contains(record._subClass._code)) {
                // 合併元科目は度数分布表に表示しない
                continue;
            }
            boolean isAmikake = false;
            if ((param._isJunior || param._isHigh && "9900".equals(param._testCd)) && !exam.getAttendSubclass(student.gradeCourse()).contains(record._subClass._code)) {
                isAmikake = true;
            }
            if (StringUtils.defaultString(record._subClass._name).length() > 9) {
                svf.VrsOutn("SUBCLASS2_3_1", column, record._subClass._name);
                if (isAmikake ) {
                    svf.VrAttributen("SUBCLASS2_3_1", column, "Paint=(1,90,2),Bold=1");
                    svf.VrAttributen("SUBCLASS2_3_2", column, "Paint=(1,90,2),Bold=1");
                }
            } else if (StringUtils.defaultString(record._subClass._name).length() > 7) {
                svf.VrsOutn("SUBCLASS2_2", column, record._subClass._name);
                if (isAmikake ) {
                    svf.VrAttributen("SUBCLASS2_2", column, "Paint=(1,90,2),Bold=1");
                }
            } else {
                svf.VrsOutn("SUBCLASS2", column, record._subClass._name);
                if (isAmikake ) {
                    svf.VrAttributen("SUBCLASS2", column, "Paint=(1,90,2),Bold=1");
                }
            }
            
            if (null != subclassMap.get(record._subClass._code)) {
                //log.info(" code = " + record._subClass._code);
                final List scoreList = (List) subclassMap.get(record._subClass._code);
                for (int i = 0; i < scores.length; i++) {
                    final int min = scores[i][0];
                    final int max = scores[i][1];
//                    log.info(" min = " + min + ", max = " + max);
                    int count = 0;
                    for (final Iterator sit = scoreList.iterator(); sit.hasNext();) {
                        final BigDecimal score = (BigDecimal) sit.next();
                        final int iscore = sishaGonyu(score).intValue();
                        if (min <= iscore && iscore <= max) {
                            count += 1;
                        }
                    }
                    svf.VrsOutn("DIST" + String.valueOf(i + 1), column, String.valueOf(count));
                }
            }
            if (null != exam._avgDat.get(avgKey(record._subClass._code, student))) {
                final AverageDat avgDat = (AverageDat) exam._avgDat.get(avgKey(record._subClass._code, student));
                svf.VrsOutn("DIST_NUM", column, intnum(avgDat._count, ""));
                svf.VrsOutn("DIST_AVE", column, sishaGonyuStr(avgDat._avg));
                svf.VrsOutn("DIST_MAX", column, intnum(avgDat._highscore, ""));
                svf.VrsOutn("DIST_MIN", column, intnum(avgDat._lowscore, ""));
                svf.VrsOutn("DIST_DEV", column, sishaGonyuStr(avgDat._stdDev));
                
            }
            column += 1;
        }
    }
    
    private static BigDecimal getAvg(final List scoreList) {
        if (scoreList.isEmpty()) {
            return null;
        }
        BigDecimal sum = new BigDecimal(0);
        for (final Iterator it = scoreList.iterator(); it.hasNext();) {
            final BigDecimal bd = (BigDecimal) it.next();
            sum = sum.add(bd);
        }
        final BigDecimal avg = sum.divide(new BigDecimal(scoreList.size()), 1, BigDecimal.ROUND_HALF_UP);
        return avg;
    }

    private static String sishaGonyuStr(final BigDecimal bd) {
        return null == bd ? null : sishaGonyu(bd).toString();
    }
    
    private static BigDecimal sishaGonyu(final BigDecimal bd) {
        return null == bd ? null : bd.setScale(1, BigDecimal.ROUND_HALF_UP);
    }

    private static final String intnum(final Integer i, final String def) {
        return (null == i) ? def : i.toString();
    }

    private static void loadExam(final DB2UDB db2, final Param param, final List students, final Exam exam) throws SQLException {
        loadSubClasses(db2, param, exam, students);
        loadAverageDat(db2, param, exam);
        loadRecord(db2, param, exam, students);
        loadScoreDistribution(db2, param, exam, students);
        loadCourseWeightingSubclassCdListMap(db2, exam, param);
//        if (_param._isConvertScoreTo100) {
//            loadConvertScore(_db2, exam);
//        }
    }

//    private void loadConvertScore(DB2UDB db2, Exam exam) {
//        exam._convertedScore.load(db2, exam);
//    }

    private static void loadSubClasses(final DB2UDB db2, final Param param, final Exam exam, final List students) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            String rtn;
            rtn = "select";
            if ("1".equals(param._useCurriculumcd)) {
                rtn +="  distinct T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD";
            } else {
                rtn +="  distinct T1.SUBCLASSCD";
            }
            rtn     +=" from"
                    + "  RECORD_SCORE_DAT T1 "
                    + " where"
                    + "  T1.YEAR = '" + exam._year + "' and"
                    + "  T1.SEMESTER = '" + exam._semester + "' and"
                    + "  T1.TESTKINDCD || T1.TESTITEMCD = '" + exam._testCd + "' and"
                    + "  T1.SCHREGNO = ? "
                    ;
            log.debug("成績入力データの科目のSQL=" + rtn);

            ps = db2.prepareStatement(rtn);
            for (final Iterator it = students.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                ps.setString(1, student._schregno);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String subClasscd = rs.getString("SUBCLASSCD");

                    final SubClass subClass = (SubClass) param._subClasses.get(subClasscd);
                    if (null != subClass) {
                        student.subclasses(exam).put(subClasscd, subClass);
                    }
                }
                DbUtils.closeQuietly(rs);
                db2.commit();
            }
        } catch (final SQLException e) {
            log.fatal("成績入力データの科目取得でエラー");
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
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
            final Param param,
            final Exam exam
    ) throws SQLException {
        
        String selectFrom;
        selectFrom = "SELECT";
        if ("1".equals(param._useCurriculumcd)) {
            selectFrom += "  case when t1.subclasscd in ('" + ALL3 + "', '" + ALL5 + "', '" + ALL9 + "') then t1.subclasscd  ";
            selectFrom += "   else t1.classcd || '-' || t1.school_kind || '-' || t1.curriculum_cd || '-' || t1.subclasscd ";
            selectFrom += "  end as subclasscd,";
        } else {
            selectFrom += "  t1.subclasscd,";
        }
        selectFrom += ""
            + "  t1.avg,"
            + "  t1.stddev,"
            + "  t1.highscore,"
            + "  t1.lowscore,"
            + "  t1.count,"
            + "  t1.coursecd,"
            + "  t1.majorcd,"
            + "  t1.coursecode,";
        if ("9900".equals(param._testCd)) {
            selectFrom += ""
                    + "  value(t2.perfect, 5) as perfect";
        } else {
            selectFrom += ""
                    + "  value(t2.perfect, 100) as perfect";
        }
        selectFrom += ""
            + " FROM"
            + "  record_average_dat t1"
            + "  LEFT JOIN perfect_record_dat t2 ON t2.year = t1.year AND "
            + "    t2.semester= t1.semester AND"
            + "    t2.testkindcd= t1.testkindcd AND"
            + "    t2.testitemcd= t1.testitemcd AND";
            if ("1".equals(param._useCurriculumcd)) {
                selectFrom += ""
                    + "    t2.classcd= t1.classcd AND"
                    + "    t2.school_kind= t1.school_kind AND"
                    + "    t2.curriculum_cd= t1.curriculum_cd AND";
            }
        selectFrom += ""
            + "    t2.subclasscd= t1.subclasscd AND"
            + "    t2.grade = (case when t2.div = '01' then '00' else '" + exam._grade + "' end) AND"
            + "    t2.coursecd || t2.majorcd || t2.coursecode = "
            + "        (case when t2.div in ('01', '02') then '00000000' "
            + "              when t2.div = '04' then '0' || t1.majorcd || '0000' "
            + "              else t1.coursecd || t1.majorcd || t1.coursecode end) "
            ;
        final String where;
        if (GROUP_DIV_GRADE.equals(param._groupDiv)) {
            where = " WHERE"
                + "    t1.year='" + exam._year + "' AND"
                + "    t1.semester='" + exam._semester + "' AND"
                + "    t1.testkindcd='" + exam.getKindCd() + "' AND"
                + "    t1.testitemcd='" + exam.getItemCd() + "' AND"
                + "    t1.avg_div='1' AND"
                + "    t1.grade= '" + exam._grade + "' AND"
                + "    t1.hr_class='000' AND"
                + "    t1.coursecd='0' AND"
                + "    t1.majorcd='000' AND"
                + "    t1.coursecode='0000'"
                ;
        } else if (GROUP_DIV_COURSEGROUP.equals(param._groupDiv)) {
            where = " WHERE"
                + "    t1.year='" + exam._year + "' AND"
                + "    t1.semester='" + exam._semester + "' AND"
                + "    t1.testkindcd='" + exam.getKindCd() + "' AND"
                + "    t1.testitemcd='" + exam.getItemCd() + "' AND"
                + "    t1.avg_div='5' AND"
                + "    t1.grade = '" + exam._grade + "' AND"
                + "    t1.hr_class='000'"
                ;
        } else {
            where = " WHERE"
                + "    t1.year='" + exam._year + "' AND"
                + "    t1.semester='" + exam._semester + "' AND"
                + "    t1.testkindcd='" + exam.getKindCd() + "' AND"
                + "    t1.testitemcd='" + exam.getItemCd() + "' AND"
                + "    t1.avg_div='3' AND"
                + "    t1.grade = '" + exam._grade + "' AND"
                + "    t1.hr_class='000'"
                ;
        }

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = selectFrom + where;
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String subclasscd = rs.getString("subclasscd");
                final SubClass subClass = (SubClass) param._subClasses.get(subclasscd);
                if (null == subClass) {
                    log.warn("成績平均データが科目マスタに無い!:" + subclasscd);
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
                final Integer count = KNJServletUtils.getInteger(rs, "count");
                final Integer highscore = KNJServletUtils.getInteger(rs, "highscore");
                final Integer lowscore = KNJServletUtils.getInteger(rs, "lowscore");
                final String coursecd = rs.getString("coursecd");
                final String majorcd = rs.getString("majorcd");
                final String coursecode = rs.getString("coursecode");

                final AverageDat avgDat;
                if (ALL3.equals(subclasscd) || ALL5.equals(subclasscd) || ALL9.equals(subclasscd)) {
                    final SubClass subClass0 = new SubClass(subclasscd);
                    avgDat = new AverageDat(subClass0, avg, avgPercent, stdDev, highscore, lowscore, count);
                } else {
                    avgDat = new AverageDat(subClass, avg, avgPercent, stdDev, highscore, lowscore, count);
                }
                final String avgKey;
                if (GROUP_DIV_GRADE.equals(param._groupDiv)) {
                    avgKey = subclasscd;
                } else if (GROUP_DIV_COURSEGROUP.equals(param._groupDiv)) {
                    avgKey = majorcd + "-" + subclasscd;
                } else {
                    avgKey = coursecd + majorcd + coursecode + "-" + subclasscd;
                }
                exam._avgDat.put(avgKey, avgDat);
            }
        } catch (final SQLException e) {
            log.warn("成績平均データの取得でエラー");
            throw e;
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        log.debug("テストコード=" + exam.getKindCd() + exam.getItemCd() + " の成績平均データの件数=" + exam._avgDat.size());
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
            final Param param,
            final Exam exam,
            final List students
    ) {
        final String gradeRank  = "2".equals(param._outputKijun) ? "grade_avg_rank" : "3".equals(param._outputKijun) ? "grade_deviation_rank" : "grade_rank";
        final String courseRank = "2".equals(param._outputKijun) ? "course_avg_rank" : "3".equals(param._outputKijun) ? "course_deviation_rank" : "course_rank";
        final String majorRank = "2".equals(param._outputKijun) ? "major_avg_rank" : "3".equals(param._outputKijun) ? "major_deviation_rank" : "major_rank";

        String sql;
        /* 通常の成績 */
        sql = "SELECT";
        if ("1".equals(param._useCurriculumcd)) {
            sql +="  t1.classcd || '-' || t1.school_kind || '-' || t1.curriculum_cd || '-' || t1.subclasscd as subclasscd,";
        } else {
            sql +="  t1.subclasscd,";
        }
        
        sql +="  t2.score,";
        if (GROUP_DIV_GRADE.equals(param._groupDiv)) {
            sql += "  t2." + gradeRank + " as rank,";
            sql +=  " t2.grade_deviation as deviation,";
        } else if (GROUP_DIV_COURSEGROUP.equals(param._groupDiv)) {
            sql += "  t2." + majorRank + " as rank,";
            sql += "  t2.major_deviation as deviation,";
        } else {
            sql += "  t2." + courseRank + " as rank,";
            sql += "  t2.course_deviation as deviation,";
        }
        sql +="  t3.div,";
        if ("9900".equals(exam._testCd)) {
            sql +="  value(t3.perfect, 5) as perfect";
        } else {
            sql +="  value(t3.perfect, 100) as perfect";
        }
        sql +="  , t1.value_di";
        sql +=" FROM"
            + "  record_score_dat t1 "
            + " LEFT JOIN record_rank_dat t2 ON"
            + "    t1.year=t2.year AND"
            + "    t1.semester=t2.semester AND"
            + "    t1.testkindcd=t2.testkindcd AND"
            + "    t1.testitemcd=t2.testitemcd AND";
        if ("1".equals(param._useCurriculumcd)) {
            sql +="    t1.classcd=t2.classcd AND"
                + "    t1.school_kind=t2.school_kind AND"
                + "    t1.curriculum_cd=t2.curriculum_cd AND";
        }
        sql +="    t1.subclasscd=t2.subclasscd AND"
            + "    t1.schregno=t2.schregno "
            + " LEFT JOIN perfect_record_dat t3 ON"
            + "    t1.year=t3.year AND"
            + "    t1.semester=t3.semester AND"
            + "    t1.testkindcd=t3.testkindcd AND"
            + "    t1.testitemcd=t3.testitemcd AND";
        if ("1".equals(param._useCurriculumcd)) {
            sql +="    t1.classcd=t3.classcd AND"
                + "    t1.school_kind=t3.school_kind AND"
                + "    t1.curriculum_cd=t3.curriculum_cd AND";
        }
        sql +="    t1.subclasscd=t3.subclasscd AND"
            + "    t3.grade = (case when t3.div = '01' then '00' else ? end) AND "
            + "    t3.coursecd || t3.majorcd || t3.coursecode = "
            + "        (case when t3.div in ('01', '02') then '00000000' "
            + "              when t3.div = '04' then '0' || ? || '0000' "
            + "              else ? end) "
            + " WHERE"
            + "  t1.year='" + exam._year + "' AND"
            + "  t1.semester='" + exam._semester + "' AND"
            + "  t1.testkindcd='" + exam.getKindCd() + "' AND"
            + "  t1.testitemcd='" + exam.getItemCd() + "' AND"
            //+ "  t1.score_div='01' AND"
            + "  t1.schregno=?"
            ;
        
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = db2.prepareStatement(sql);
            for (final Iterator it = students.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                ps.setString(1, student._grade);
                ps.setString(2, student._coursegroupCd);
                ps.setString(3, student._courseCd + student._majorCd + student._courseCode);
                ps.setString(4, student._schregno);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String subclasscd = rs.getString("subclasscd");
                    SubClass subClass = (SubClass) param._subClasses.get(subclasscd);
                    if (null == subClass) {
                        log.warn("対象成績データが科目マスタに無い!:" + subclasscd);
                        final Class clazz;
                        if ("1".equals(param._useCurriculumcd)) {
                            final String[] split = StringUtils.split(subclasscd, "-");
                            clazz = (Class) param._classes.get(split[0] + "-" + split[1]);
                        } else {
                            clazz = (Class) param._classes.get(subclasscd.substring(0, 2));
                        }
                        if (null == clazz) {
                            continue;
                        }
                        subClass = new SubClass(subclasscd, clazz._name, clazz._abbv);
                    }
                    final Integer score = KNJServletUtils.getInteger(rs, "score");
                    Integer scorePercent = null;
                    final int perfect = rs.getInt("PERFECT");
                    if (100 == perfect) {
                        scorePercent = score;
                    } else if (null != score) {
                        scorePercent = new Integer(new BigDecimal(rs.getString("SCORE")).multiply(new BigDecimal(100)).divide(new BigDecimal(perfect), 0, BigDecimal.ROUND_HALF_UP).intValue());
                    }
                    final Record rec = new Record(subClass, score, scorePercent, null, KNJServletUtils.getInteger(rs, "rank"), rs.getBigDecimal("deviation"), rs.getString("VALUE_DI"));
                    student.record(exam).put(subClass._code, rec);
                }
            	DbUtils.closeQuietly(rs);
                db2.commit();
            }
        } catch (final SQLException e) {
            log.warn("成績データの取得でエラー", e);
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }

        String sql2 = "SELECT"
            + "  subclasscd,"
            + "  score,";
        if (GROUP_DIV_GRADE.equals(param._groupDiv)) {
            sql2 += "  t2." + gradeRank + " as rank,";
            sql2 +=  " t2.grade_deviation as deviation,";
        } else if (GROUP_DIV_COURSEGROUP.equals(param._groupDiv)) {
            sql2 += "  t2." + majorRank + " as rank,";
            sql2 += "  t2.major_deviation as deviation,";
        } else {
            sql2 += "  t2." + courseRank + " as rank,";
            sql2 += "  t2.course_deviation as deviation,";
        }
        sql2+="  avg"
            + " FROM record_rank_dat t2 "
            + " WHERE"
            + "  year='" + exam._year + "' AND"
            + "  semester='" + exam._semester + "' AND"
            + "  testkindcd='" + exam.getKindCd() + "' AND"
            + "  testitemcd='" + exam.getItemCd() + "' AND"
            + "  subclasscd IN ('" + ALL3 + "', '" + ALL5 + "', '" + ALL9 + "') AND"
            + "  schregno=?"
            ;
        try {
        	ps = db2.prepareStatement(sql2);
            for (final Iterator it = students.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                ps.setString(1, student._schregno);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final SubClass subClass = new SubClass(rs.getString("subclasscd"));
                    final Record rec = new Record(subClass, KNJServletUtils.getInteger(rs, "score"), null, rs.getBigDecimal("avg"), KNJServletUtils.getInteger(rs, "rank"), rs.getBigDecimal("deviation"), null);
                    student.record(exam).put(subClass._code, rec);
                }
            	DbUtils.closeQuietly(rs);
                db2.commit();
            }
        } catch (final SQLException e) {
            log.warn("成績データの取得でエラー", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }
    
    private static Map getMappedMap(final Map map, final String key) {
        if (null == map.get(key)) {
            map.put(key, new TreeMap());
        }
        return (Map) map.get(key);
    }
    
    private static List getMappedList(final Map map, final String key) {
        if (null == map.get(key)) {
            map.put(key, new ArrayList());
        }
        return (List) map.get(key);
    }
    
    private static String allsubclasscd(final Param param) {
        return param._isJunior ? (param._testCd != null && param._testCd.startsWith("99") ? ALL9 : ALL5) : ALL9;
    }
    
    /**
     * 生徒に成績データを関連付ける。
     * @param db2 DB
     * @param kindCd テスト種別コード
     * @param itemCd テスト項目コード
     * @param scoreDistributions 得点分布の格納場所
     * @param students 生徒たち
     */
    private static void loadScoreDistribution(
            final DB2UDB db2,
            final Param param,
            final Exam exam,
            final List students
    ) {
        exam._distribution = new HashMap();
        
        final StringBuffer stb = new StringBuffer();
        /* 通常の成績 */
        stb.append("WITH schregnos AS (");
        stb.append("SELECT");
        stb.append("  t1.schregno, ");
        stb.append("  t1.grade,");
        stb.append("  t1.coursecd,");
        stb.append("  t1.majorcd,");
        stb.append("  t1.coursecode,");
        stb.append("  t4.group_cd as coursegroupCd ");
        stb.append(" FROM");
        stb.append("  schreg_regd_dat t1 ");
        stb.append("    LEFT JOIN course_group_cd_dat t4 ON t1.year=t4.year AND");
        stb.append("      t1.grade=t4.grade AND");
        stb.append("      t1.coursecd=t4.coursecd AND");
        stb.append("      t1.majorcd=t4.majorcd AND");
        stb.append("      t1.coursecode=t4.coursecode");
        stb.append(" WHERE ");
        stb.append("  t1.year = '" + param._year + "' ");
        stb.append("  and t1.semester = '" + ("9".equals(param._semester) ? param._ctrlSeme : param._semester) + "' ");
        stb.append("  and t1.grade = '" + param._grade + "' ");
        stb.append(") ");
        stb.append("SELECT");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append("  t1.classcd || '-' || t1.school_kind || '-' || t1.curriculum_cd || '-' || t1.subclasscd as subclasscd,");
        } else {
            stb.append("  t1.subclasscd,");
        }
        if ("9900".equals(exam._testCd) || "9901".equals(exam._testCd)) {
            stb.append("  cast(t1.value as decimal(8,5)) as score,");
        } else {
            stb.append("  cast(t1.score as decimal(8,5)) as score,");
        }
        if ("9".equals(exam._semester) && "9900".equals(exam._testCd)) {
            stb.append("  value(t3.perfect, 5) as perfect,");
        } else {
            stb.append("  value(t3.perfect, 100) as perfect,");
        }
        stb.append("  t2.grade,");
        stb.append("  t2.coursecd,");
        stb.append("  t2.majorcd,");
        stb.append("  t2.coursecode,");
        stb.append("  t2.coursegroupCd");
        stb.append(" FROM");
        stb.append("  record_score_dat t1 ");
        stb.append(" INNER JOIN schregnos t2 ON t2.schregno = t1.schregno ");
        stb.append(" LEFT JOIN perfect_record_dat t3 ON");
        stb.append("    t1.year=t3.year AND");
        stb.append("    t1.semester=t3.semester AND");
        stb.append("    t1.testkindcd=t3.testkindcd AND");
        stb.append("    t1.testitemcd=t3.testitemcd AND");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append("    t1.classcd=t3.classcd AND");
            stb.append("    t1.school_kind=t3.school_kind AND");
            stb.append("    t1.curriculum_cd=t3.curriculum_cd AND");
        }
        stb.append("    t1.subclasscd=t3.subclasscd AND");
        stb.append("    t3.grade = (case when t3.div = '01' then '00' else t2.grade end) AND ");
        stb.append("    t3.coursecd || t3.majorcd || t3.coursecode = ");
        stb.append("        (case when t3.div in ('01', '02') then '00000000' ");
        stb.append("              when t3.div = '04' then '0' || t2.coursegroupcd || '0000' ");
        stb.append("              else t2.coursecd || t2.majorcd || t2.coursecode end) ");
        stb.append(" WHERE");
        stb.append("  t1.year='" + exam._year + "' AND");
        stb.append("  t1.semester='" + exam._semester + "' AND");
        stb.append("  t1.testkindcd='" + exam.getKindCd() + "' AND");
        stb.append("  t1.testitemcd='" + exam.getItemCd() + "' ");
        stb.append( " union all ");
        stb.append( "SELECT");
        stb.append("  t1.subclasscd,");
        stb.append("  t1.avg as score, ");
        if ("9".equals(exam._semester) && "9900".equals(exam._testCd)) {
            stb.append("  5 as perfect,");
        } else {
            stb.append("  100 as perfect,");
        }
        stb.append("  t2.grade,");
        stb.append("  t2.coursecd,");
        stb.append("  t2.majorcd,");
        stb.append("  t2.coursecode,");
        stb.append("  t2.coursegroupCd");
        stb.append(" FROM");
        stb.append("  record_rank_dat t1 ");
        stb.append(" INNER JOIN schregnos t2 ON t2.schregno = t1.schregno ");
        stb.append(" WHERE");
        stb.append("  t1.year='" + exam._year + "' AND");
        stb.append("  t1.semester='" + exam._semester + "' AND");
        stb.append("  t1.testkindcd='" + exam.getKindCd() + "' AND");
        stb.append("  t1.testitemcd='" + exam.getItemCd() + "' AND");
        stb.append("  t1.subclasscd='" + allsubclasscd(param) + "' ");
        
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            log.debug(" dist sql = " + stb.toString());
            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();
            while (rs.next()) {
                final BigDecimal score = rs.getBigDecimal("score");
                if (null == score) {
                    continue;
                }
                
                final String rtnKey = scoreDistKey(param, rs.getString("grade"), rs.getString("coursegroupCd"), rs.getString("coursecd") + rs.getString("majorcd") + rs.getString("coursecode"));
                
                final Map subclassMap = getMappedMap(exam._distribution, rtnKey);

                final String subclasscd = rs.getString("subclasscd");
                
                final List scoreList = getMappedList(subclassMap, subclasscd);
                
                if (allsubclasscd(param).equals(subclasscd) && !"9900".equals(exam._testCd)) {
                    scoreList.add(score);
                } else {
                    Number scorePercent;
                    final int perfect = rs.getInt("PERFECT");
                    if (100 == perfect) {
                        scorePercent = score;
                    } else {
                        scorePercent = score.multiply(new BigDecimal(100)).divide(new BigDecimal(perfect), 1, BigDecimal.ROUND_HALF_UP);
                    }
                    //log.debug(" scorePercent = " + scorePercent);
                    scoreList.add(scorePercent);
                }
            }

        } catch (final Exception e) {
            log.warn("成績データの取得でエラー", e);
        } finally {
            DbUtils.closeQuietly(rs);
            db2.commit();
        }
    }
    
    private static void loadCourseWeightingSubclassCdListMap(final DB2UDB db2, final Exam exam, final Param param) {
        final Map courseWeightingSubclassCdListMap = new HashMap();
        PreparedStatement ps = null;
        ResultSet rs = null;
        final String flg = null != exam._testCd && exam._testCd.startsWith("99") ? "2" : "1";
        try {
            final StringBuffer stb = new StringBuffer();
            stb.append("   SELECT ");
            stb.append("     T1.GRADE || T1.COURSECD || T1.MAJORCD || T1.COURSECODE AS COURSE, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("       T1.ATTEND_CLASSCD || '-' || T1.ATTEND_SCHOOL_KIND || '-' || T1.ATTEND_CURRICULUM_CD || '-' || ");
            }
            stb.append("       T1.ATTEND_SUBCLASSCD AS ATTEND_SUBCLASSCD, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("       T1.COMBINED_CLASSCD || '-' || T1.COMBINED_SCHOOL_KIND || '-' || T1.COMBINED_CURRICULUM_CD || '-' || ");
            }
            stb.append("       T1.COMBINED_SUBCLASSCD AS COMBINED_SUBCLASSCD ");
            stb.append("   FROM ");
            stb.append("       SUBCLASS_WEIGHTING_COURSE_DAT T1 ");
            stb.append("   WHERE ");
            stb.append("       T1.YEAR = '" + param._year + "' ");
            stb.append("       AND T1.GRADE = '" + param._grade + "' ");
            stb.append("       AND T1.FLG = '" + flg + "' ");
            
            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();
            while (rs.next()) {
                getMappedList(getMappedMap(courseWeightingSubclassCdListMap, rs.getString("COURSE")), "ATTEND_SUBCLASS").add(rs.getString("ATTEND_SUBCLASSCD"));
                getMappedList(getMappedMap(courseWeightingSubclassCdListMap, rs.getString("COURSE")), "COMBINED_SUBCLASS").add(rs.getString("COMBINED_SUBCLASSCD"));
                final List list = getMappedList(getMappedMap(getMappedMap(courseWeightingSubclassCdListMap, "PAIR"), rs.getString("COURSE")), rs.getString("COMBINED_SUBCLASSCD"));
                if (!list.contains(rs.getString("ATTEND_SUBCLASSCD"))) {
                    list.add(rs.getString("ATTEND_SUBCLASSCD"));
                }
            }
        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
//        for (final Iterator cit = courseWeightingSubclassCdListMap.keySet().iterator(); cit.hasNext();) {
//            final String course = (String) cit.next();
//            final Map m = (Map) courseWeightingSubclassCdListMap.get(course);
//            log.debug(" course = " + course + ", attend subclass = " + m.get("ATTEND_SUBCLASS"));
//        }
        exam._courseWeightingSubclassCdListMap = courseWeightingSubclassCdListMap;
    } 

    private static List createStudents(final DB2UDB db2, final Param param) throws SQLException {
        final List rtn = new LinkedList();
        
        final String sql;
        sql = "SELECT"
            + "  t1.schregno,"
            + "  t1.grade,"
            + "  t1.hr_class,"
            + "  t1.attendno,"
            + "  regdh.hr_name,"
            + "  t1.coursecd,"
            + "  t1.majorcd,"
            + "  t1.coursecode,"
            + "  base.name,"
            + "  t4.group_cd as coursegroupCd,"
            + "  t6.staffname"
            + " FROM"
            + "  schreg_regd_dat t1 "
            + "  inner join schreg_base_mst base on base.schregno = t1.schregno "
            + "  inner join schreg_regd_hdat regdh on regdh.YEAR = t1.YEAR and regdh.SEMESTER = t1.SEMESTER and regdh.GRADE = t1.GRADE and regdh.HR_CLASS = t1.HR_CLASS "
            + "    LEFT JOIN course_group_cd_dat t4 ON t1.year=t4.year AND"
            + "      t1.grade=t4.grade AND"
            + "      t1.coursecd=t4.coursecd AND"
            + "      t1.majorcd=t4.majorcd AND"
            + "      t1.coursecode=t4.coursecode"
            + "    LEFT JOIN schreg_regd_hdat t5 ON t1.year=t5.year AND"
            + "      t1.semester=t5.semester AND"
            + "      t1.grade=t5.grade AND"
            + "      t1.hr_class=t5.hr_class "
            + "    LEFT JOIN staff_mst t6 ON t6.staffcd=t5.tr_cd1 "
            + " WHERE"
            + "  t1.year='" + param._year + "' AND"
            + "  t1.semester='" + ("9".equals(param._semester) ? param._ctrlSeme : param._semester) + "' AND"
            + "  t1.schregno IN " + SQLUtils.whereIn(true, param.getScregnos(db2))
            ;
        
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
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
                final String staffname = rs.getString("staffname");

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
                        staffname
                );
                rtn.add(student);
            }
        } catch (final SQLException e) {
            log.error("生徒の基本情報取得でエラー");
            throw e;
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
        Collections.sort(rtn);
        return rtn;
    }

    private File graphImageFile(final JFreeChart chart, final int dotWidth, final int dotHeight) {
        final String tmpFileName = KNJServletUtils.createTmpFile(".png");
        log.debug("\ttmp file name=" + tmpFileName);

        final File outputFile = new File(tmpFileName);
        try {
            ChartUtilities.saveChartAsPNG(outputFile, chart, dot2pixel(dotWidth), dot2pixel(dotHeight));
        } catch (final IOException ioEx) {
            log.error("グラフイメージをファイル化できません。", ioEx);
        }

        return outputFile;
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

    private static int dot2pixel(final int dot) {
        final int pixel = dot / 4;   // おおよそ4分の1程度でしょう。

        /*
         * 少し大きめにする事でSVFに貼り付ける際の拡大を防ぐ。
         * 拡大すると粗くなってしまうから。
         */
        return (int) (pixel * 1.3);
    }
    
    private void printStatic(final Vrw32alp svf, final Param param, final Student student) {
        svf.VrsOut("SCHOOL_NAME", param._schoolName);

        svf.VrsOut("STAFFNAME", param._remark2 + StringUtils.defaultString(student._staffname));

        svf.VrsOut("NENDO", param._thisExam._title);
        for (int i = 0; i < param._beforeExamList.size(); i++) {
            final Exam beforeExam = (Exam) param._beforeExamList.get(i);
            final String line = String.valueOf(i + 1);
            svf.VrsOut("PRE_SEMESTER_NAME" + line + (getMS932ByteLength(beforeExam._semestername) > 6 ? "_2" : ""), beforeExam._semestername);
            svf.VrsOut("PRE_EXAM_NAME" + line + (getMS932ByteLength(beforeExam._examName) > 6 ? "_2" : ""), beforeExam._examName);
            svf.VrsOut("PRE_SCORE_NAME" + line, "得点");
            svf.VrsOut("PRE_DEVI_NAME" + line, "偏差値");
        }
    
        svf.VrsOut("HR_NAME", student._hrName + String.valueOf(Integer.parseInt(student._attendNo)) + "番");

        svf.VrsOut((StringUtils.defaultString(student._name).length() <= 10 ? "NAME" : "NAME_2"), student._name);
        if ("1".equals(param._use_SchregNo_hyoji)) {
            svf.VrsOut("SCHREGNO", student._schregno);
        }

//        // 提出日
//        try {
//            final Calendar cal = KNJServletUtils.parseDate(param._submitDate.replace('/', '-'));
//            final String month = (cal.get(Calendar.MONTH) + 1) + "月";
//            final String day = cal.get(Calendar.DATE) + "日";
//            svf.VrsOut("DATE", month + day);
//        } catch (final ParseException e) {
//            log.error("提出日が解析できない");
//            svf.VrsOut("DATE", param._submitDate);
//        }

//        final String get3title = _param.get3title(student.courseKey());
//        _svf.VrsOut("ITEM_TOTAL3", get3title);
//        final String get5title = _param.get5title(student.courseKey());
//        if (null != get5title) {
//            _svf.VrsOut("ITEM_TOTAL5", get5title);
//        }
//        if (null != get3title) {
//            _svf.VrsOut("ITEM_TOTAL3_1", get3title + "平均");
//        }
//        if (null != get5title) {
//            _svf.VrsOut("ITEM_TOTAL5_1", get5title + "平均");
//        }
        svf.VrsOut("ITEM_TOTAL5", "全教科");

//        final String barLegendImage;
        final String msg;
        if (GROUP_DIV_GRADE.equals(param._groupDiv)) {
//            barLegendImage = BAR_CHART_LEGEND1;
            msg = "学年";
        } else if (GROUP_DIV_COURSEGROUP.equals(param._groupDiv)) {
//            barLegendImage = BAR_CHART_LEGEND3;
            msg = "グループ";
        } else {
//            barLegendImage = BAR_CHART_LEGEND2;
            msg = "コース";
        }

        svf.VrsOut("ITEM_AVG", msg + "平均");
        svf.VrsOut("ITEM_RANK", msg + "順位");
        svf.VrsOut("ITEM_DEVIATION", "偏差値");
        
//        svf.VrsOut("BAR_LEGEND", param._imagePath + "/" + barLegendImage);

        svf.VrsOut("BAR_TITLE", "度数分布表");
    }

//    private void printBarGraph(final Vrw32alp svf, final Param param, final Exam exam, final Student student) {
//        // グラフ用のデータ作成
//        final DefaultCategoryDataset scoreDataset = new DefaultCategoryDataset();
//        final DefaultCategoryDataset avgDataset = new DefaultCategoryDataset();
//        int i = 0;
//        for (final Iterator it = student.record(exam).values().iterator(); it.hasNext();) {
//            final Record record = (Record) it.next();
//            if (record._subClass._isMoto) {
//                continue;
//            }
//            
//            if ("90".equals(record._subClass.getClassCd(param))) {
//                continue;
//            }
//
//            if (exam.getAttendSubclassCdList(student).contains(record._subClass._code)) {
//                continue;
//            }
//
//            final String abbv = record._subClass._abbv;
//            final String subclassKey = null != abbv && abbv.length() > 4 ? abbv.substring(0, 4) : abbv;
//            scoreDataset.addValue(record._scorePercent, "本人得点", subclassKey);
//            final String key;
//            if (GROUP_DIV_GRADE.equals(param._groupDiv)) {
//                key = record._subClass._code;
//            } else if (GROUP_DIV_COURSEGROUP.equals(param._groupDiv)) {
//                key = record._subClass._code + student.courseGroupKey();
//            } else {
//                key = record._subClass._code + student.courseKey();
//            }
//            final AverageDat avgDat = (AverageDat) exam._avgDat.get(key);
//            final BigDecimal avgPercent = (null == avgDat) ? null : avgDat._avgPercent;
//
//            final String msg = GROUP_DIV_GRADE.equals(param._groupDiv) ? "学年" : GROUP_DIV_COURSEGROUP.equals(param._groupDiv) ? "グループ" : "コース";
//            avgDataset.addValue(avgPercent, msg + "平均点", subclassKey);
//
//            log.info("棒グラフ⇒" + record._subClass + ":素点=" + record._score + ", 平均=" + avgPercent);
//            if (i++ > BAR_GRAPH_MAX_ITEM) {
//                break;
//            }
//        }
//
//        try {
//            // チャート作成
//            final JFreeChart chart = createBarChart(scoreDataset, avgDataset);
//
//            // グラフのファイルを生成
//            final File outputFile = graphImageFile(chart, 1940, 930);
//            _graphFiles.add(outputFile);
//
//            // グラフの出力
//            svf.VrsOut("BAR_LABEL", "得点");
//            
//            if (outputFile.exists()) {
//                svf.VrsOut("BAR", outputFile.toString());
//            }
//        } catch (Throwable e) {
//            log.error("exception or error!", e);
//        }
//    }

//    private JFreeChart createBarChart(
//            final DefaultCategoryDataset scoreDataset,
//            final DefaultCategoryDataset avgDataset
//    ) {
//        final JFreeChart chart = ChartFactory.createBarChart(null, null, null, scoreDataset, PlotOrientation.VERTICAL, false, false, false);
//        final CategoryPlot plot = chart.getCategoryPlot();
//        plot.setRangeGridlineStroke(new BasicStroke(2.0f)); // 目盛りの太さ
//        plot.getDomainAxis().setTickLabelsVisible(true);
//        plot.setRangeGridlinePaint(Color.black);
//        plot.setRangeGridlineStroke(new BasicStroke(1.0f));
//
//        // 追加する折れ線グラフの表示設定
//        final LineAndShapeRenderer renderer2 = new LineAndShapeRenderer();
//        renderer2.setItemLabelsVisible(true);
//        renderer2.setPaint(Color.gray);
//        plot.setDataset(1, avgDataset);
//        plot.setRenderer(1, renderer2);
//        plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
//
//        final NumberAxis numberAxis = new NumberAxis();
//        numberAxis.setTickUnit(new NumberTickUnit(10));
//        numberAxis.setTickLabelsVisible(true);
//        numberAxis.setRange(0, 100.0);
//        plot.setRangeAxis(numberAxis);
//
//        final CategoryItemRenderer renderer = plot.getRenderer();
//        renderer.setSeriesPaint(0, Color.darkGray);
//        final Font itemLabelFont = new Font("TimesRoman", Font.PLAIN, 30);
//        renderer.setItemLabelFont(itemLabelFont);
//        renderer.setItemLabelsVisible(true);
//
//        ((BarRenderer) renderer).setMaximumBarWidth(0.05);
//
//        chart.setBackgroundPaint(Color.white);
//
//        return chart;
//    }
    
    private void printRadarGraph(final Vrw32alp svf, final Param param, final Student student) {
        // データ作成
        final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
//        for (final Iterator it0 = param._classOrder.iterator(); it0.hasNext();) {
//            final String classCd = (String) it0.next();
//            
//            for (final Iterator it = student.record(exam).values().iterator(); it.hasNext();) {
//                final Record record = (Record) it.next();
//                if (!classCd.equals(record._subClass.getClassCd(param))) {
//                    continue;
//                }
//                if (exam.getAttendSubclassCdList(student).contains(record._subClass._code)) {
//                    continue;
//                }
//                log.info("レーダーグラフ⇒" + record._subClass + ", " + record._deviation);
//                final Class clazz = (Class) param._classes.get(record._subClass.getClassKey(param));
//                final String name = (null == clazz) ? "" : clazz._abbv;
//                dataset.addValue(record._deviation, "本人偏差値", name);// MAX80, MIN20
//                
//                dataset.addValue(50.0, "偏差値50", name);
//            }
//        }

        final Exam exam = param._thisExam;
        final Exam before = param._beforeExam0;
        
        for (final Iterator it = student.record(exam).values().iterator(); it.hasNext();) {
            final Record record = (Record) it.next();
            if (null == record._score) {
                continue;
            }
//            if ("99".equals(exam.getKindCd()) && record._subClass._isMoto) { // 学期・学年成績は元科目を表示しない
//                continue;
//            }
            if (ALL3.equals(record._subClass._code) || ALL5.equals(record._subClass._code) || ALL9.equals(record._subClass._code)) {
                continue;
            }
            if (!"99".equals(exam.getKindCd()) && null == exam._avgDat.get(avgKey(record._subClass._code, student))) {
                continue;
            }
            if (param._isHigh && exam.notTargetSubclasscdList(student.gradeCourse()).contains(record._subClass._code)) {
                continue;
            }
            
            // 元科目をのぞく
            if (exam.getAttendSubclass(student.gradeCourse()).contains(record._subClass._code)) {
                continue;
            }

            final String key;
            if (param._isJunior) {
                final Class clazz = (Class) param._classes.get(record._subClass.getClassKey(param));
                key = (null == clazz) ? "" : clazz._abbv;
            } else {
                key = record._subClass._abbv;
            }
            dataset.addValue(record._deviation, "本人偏差値", key);// MAX80
//            dataset.addValue(50.0, "偏差値50", name);
            BigDecimal beforeDeviation = null;
            if (param.isPrintRadarWithBefore()) {
                // 前回の試験が中間の場合
                final Record beforeRecord = (Record) student.record(before).get(record._subClass._code);
                if (null != beforeRecord) {
                    beforeDeviation = beforeRecord._deviation;
                    dataset.addValue(beforeDeviation, "前回の偏差値", key);
                    
                }
            }
            log.info("レーダーグラフ⇒" + record._subClass + ", " + record._deviation + " <- " + beforeDeviation);
        }

        try {
            // チャート作成
            final JFreeChart chart = createRaderChart(dataset);

            // グラフのファイルを生成
            final File outputFile = graphImageFile(chart, 930, 784);
            _graphFiles.add(outputFile);

            // グラフの出力
            if (0 < dataset.getColumnCount() && outputFile.exists()) {
                if (param._isJunior) {
                    svf.VrsOut("RADER_TITLE", "教科間バランス");
                } else {
                    svf.VrsOut("RADER_TITLE", "科目間バランス");
                }
                // 画像
                svfVrsOutImage(svf, "RADER_LEGEND", param.isPrintRadarWithBefore() ? RADER_CHART_LEGEND_NO50_WITH_BEFORE : RADER_CHART_LEGEND_NO50, param);

                svf.VrsOut("RADER", outputFile.toString());
            }
        } catch (Throwable e) {
            log.error("exception or error!", e);
        }
    }

    private JFreeChart createRaderChart(final DefaultCategoryDataset dataset) {
        final KNJD105GSpiderWebPlot plot = new KNJD105GSpiderWebPlot();
        plot.setDataset(dataset);
        plot.setWebFilled(false);
        plot.setMaxValue(80.0);

        // 実データ
        plot.setSeriesOutlineStroke(0, new BasicStroke(1.0f));
        plot.setSeriesOutlinePaint(0, Color.black);
        plot.setSeriesPaint(0, Color.black);

        // 以前の成績
        final BasicStroke stroke = new BasicStroke(1.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f, new float[] {4.0f, 4.0f}, 0.0f);
        plot.setSeriesOutlineStroke(1, stroke);
        plot.setSeriesOutlinePaint(1, Color.darkGray);
        plot.setSeriesPaint(1, Color.darkGray);

        plot.setOutlinePaint(Color.white);

        final JFreeChart chart = new JFreeChart(null, new Font("TimesRoman", Font.PLAIN, 15), plot, false);
        chart.setBackgroundPaint(Color.white);

        return chart;
    }

    /**
     * 網掛けする。
     * @param field フィールド
     * @param value 値
     */
    private static void amikake(final Vrw32alp svf, final String field, final String value) {
        svf.VrAttribute(field, "Paint=(2,70,1),Bold=1");
        svf.VrsOut(field, value);
        svf.VrAttribute(field, "Paint=(0,0,0),Bold=0");
    }

    private static void kasen(final Vrw32alp svf, final String field, final String value) {
        svf.VrAttribute(field, "UnderLine=(0,1,1)");
        svf.VrsOut(field, value);
    }
    
    private String avgKey(final String subclasscd, final Student student) {
        if (GROUP_DIV_GRADE.equals(_param._groupDiv)) {
            return subclasscd;
        } else if (GROUP_DIV_COURSEGROUP.equals(_param._groupDiv)) {
            return student._coursegroupCd + "-" + subclasscd;
        } else {
            return student._courseCd + student._majorCd + student._courseCode + "-" + subclasscd;
        }
    }

    private int printSubclass(final Vrw32alp svf, final Param param, final Exam exam, final Student student) {
        if (!student.hasKesshi(param, exam, null)) {
            final Record rec9 = (Record) student.record(exam).get(allsubclasscd(param));
            if (null != rec9) {
                svf.VrsOut("SCORE5", intnum(rec9._score, ""));
                svf.VrsOut("RANK5",      intnum(rec9._rank, ""));
                svf.VrsOut("DEVIATION5", sishaGonyuStr(rec9._deviation));
            }
            final AverageDat avg9 = (AverageDat) exam._avgDat.get(avgKey(allsubclasscd(param), student));
            if (null != avg9) {
                svf.VrsOut("AVERAGE5",  sishaGonyuStr(avg9._avg));
                svf.VrsOut("MAX_SCORE5", intnum(avg9._highscore, ""));
                svf.VrsOut("EXAMINEE5", intnum(avg9._count, ""));
            }
        }

        List subclassList = new ArrayList(student.subclasses(exam).values());
        for (final Iterator it = subclassList.iterator(); it.hasNext();) {
            final SubClass subClass = (SubClass) it.next();

            if ("90".equals(subClass.getClassCd(param))) {
                it.remove();
            }
            
//            if ("99".equals(exam.getKindCd()) && subClass._isMoto) { // 学期・学年成績は元科目を表示しない
//                continue;
//            }
            if (_param._isHigh && exam.notTargetSubclasscdList(student.gradeCourse()).contains(subClass._code)) {
            	it.remove();
            }

            final Record record = (Record) student.record(exam).get(subClass._code);
//            final AverageDat avgDat = (AverageDat) exam._avgDat.get(avgKey(subClass._code, student));
//            if (!"99".equals(exam.getKindCd()) && null == avgDat) { // 定期考査は考査実施科目のみを表示
//                continue;
//            }

            if (null == record) {
                continue;
            }
        }
        if (subclassList.size() == 0) {
//            svf.VrsOut("CLASS", "\n");
//            svf.VrEndRecord(); // データが無い時もこのコマンドを発行しないと印刷されないから
            return 0;
        } else if (subclassList.size() > TABLE_SUBCLASS_MAX) {
        	subclassList = subclassList.subList(0, TABLE_SUBCLASS_MAX);
        }
        
        for (final Iterator it = subclassList.iterator(); it.hasNext();) {
            final SubClass subClass = (SubClass) it.next();

            final Record record = (Record) student.record(exam).get(subClass._code);
            final AverageDat avgDat = (AverageDat) exam._avgDat.get(avgKey(subClass._code, student));
//            if (!"99".equals(exam.getKindCd()) && null == avgDat) { // 定期考査は考査実施科目のみを表示
//                continue;
//            }
            
            // 教科
            final Class clazz = (Class) param._classes.get(subClass.getClassKey(param));
//            if (_fiveClassCd.contains(clazz._code)) {
//                amikake(getFClass(), clazz._name);
//            } else {
//                _svf.VrsOut(getFClass(), clazz._name);
//            }
            if (getMS932ByteLength(clazz._name) == 8) {
                svf.VrsOut("CLASS_4", clazz._name);
                svf.VrsOut("CLASS", clazz._code);
                svf.VrsOut("CLASS_2", clazz._code);
                svf.VrAttribute("CLASS", "X=10000");
                svf.VrAttribute("CLASS_2", "X=10000");
            } else if (getMS932ByteLength(clazz._name) > 6) {
                svf.VrsOut("CLASS", clazz._code);
                svf.VrsOut("CLASS_2", clazz._name);
                svf.VrsOut("CLASS_4", clazz._code);
                svf.VrAttribute("CLASS", "X=10000");
                svf.VrAttribute("CLASS_4", "X=10000");
            } else {
                svf.VrsOut("CLASS", clazz._name);
                svf.VrsOut("CLASS_2", clazz._code);
                svf.VrsOut("CLASS_4", clazz._code);
                svf.VrAttribute("CLASS_2", "X=10000");
                svf.VrAttribute("CLASS_4", "X=10000");
            }

            // 科目
            boolean isAmikake = false;
            if ((_param._isJunior || _param._isHigh && "9900".equals(param._testCd)) && !exam.getAttendSubclass(student.gradeCourse()).contains(subClass._code)) {
                isAmikake = true;
            }
            if (StringUtils.defaultString(subClass._name).length() > 9) {
                svf.VrsOut("SUBCLASS_3_1", subClass._name);
                if (isAmikake) {
                    svf.VrAttribute("SUBCLASS_3_1", "Paint=(1,90,2),Bold=1");
                    svf.VrAttribute("SUBCLASS_3_2", "Paint=(1,90,2),Bold=1");
                }
            } else if (StringUtils.defaultString(subClass._name).length() > 7) {
                svf.VrsOut("SUBCLASS_2", subClass._name);
                if (isAmikake) {
                    svf.VrAttribute("SUBCLASS_2", "Paint=(1,90,2),Bold=1");
                }
            } else {
                svf.VrsOut("SUBCLASS", subClass._name);
                if (isAmikake) {
                    svf.VrAttribute("SUBCLASS", "Paint=(1,90,2),Bold=1");
                }
            }
            
            if (!student.hasKesshi(param, exam, subClass._code)) {
                final String score = intnum(record._score, "");
                if (param.isUnderScore(score)) {
                    svf.VrsOut("SCORE", "(" + score + ")");
                } else {
                    svf.VrsOut("SCORE", score);
                }

                svf.VrsOut("RANK", intnum(record._rank, ""));
                svf.VrsOut("DEVIATION", sishaGonyuStr(record._deviation));

                if (null != avgDat) {
                    svf.VrsOut("AVERAGE",  sishaGonyuStr(avgDat._avg));
                    svf.VrsOut("MAX_SCORE", intnum(avgDat._highscore, ""));
                    svf.VrsOut("EXAMINEE", intnum(avgDat._count, ""));    // 受験者数
                }
            }

            svf.VrEndRecord();
        }
        return subclassList.size();
    }
    
    private void printSubclass2(final Vrw32alp svf, final Param param, final Student student, final int print1) {
        
        final TreeSet subclasscdSet = new TreeSet();
        for (int ei = 0; ei < param._beforeExamList.size(); ei++) {
            final Exam exam = (Exam) param._beforeExamList.get(ei);
            final String line = String.valueOf(ei + 1);
            final Map subclassMap = (Map) student.subclasses(exam);
            subclasscdSet.addAll(subclassMap.keySet());
            
            if (!student.hasKesshi(param, exam, null)) {
                final Record rec9 = (Record) student.record(exam).get(allsubclasscd(param));
                if (null != rec9) {
                    svf.VrsOut("PRE_SCORE5_" + line, intnum(rec9._score, ""));
                    svf.VrsOut("PRE_DEVI5_" + line, sishaGonyuStr(rec9._deviation));
                }
            }
        }
        
        int i = 0;
        for (final Iterator sit = subclasscdSet.iterator(); sit.hasNext();) {
            final String subclasscd = (String) sit.next();
            
            boolean printed = false;
            for (int ei = 0; ei < param._beforeExamList.size(); ei++) {
                
                final Exam exam = (Exam) param._beforeExamList.get(ei);
                final String line = String.valueOf(ei + 1);
                

                final SubClass subClass = (SubClass) student.subclasses(exam).get(subclasscd);

                if (null == subClass) {
                    continue;
                }
                if ("90".equals(subClass.getClassCd(param))) {
                    continue;
                }
                
//                if ("99".equals(exam.getKindCd()) && subClass._isMoto) { // 学期・学年成績は元科目を表示しない
//                    continue;
//                }
                if (_param._isHigh && exam.notTargetSubclasscdList(student.gradeCourse()).contains(subClass._code)) {
                    continue;
                }

                final Record record = (Record) student.record(exam).get(subClass._code);
//                final AverageDat avgDat = (AverageDat) exam._avgDat.get(subClass._code);
//                if (!"99".equals(exam.getKindCd()) && null == avgDat) { // 定期考査は考査実施科目のみを表示
//                    continue;
//                }
                
                // 教科
                final Class clazz = (Class) param._classes.get(subClass.getClassKey(param));
                if (getMS932ByteLength(clazz._name) == 8) {
                    svf.VrsOut("PRE_CLASS_4", clazz._name);
                    svf.VrsOut("PRE_CLASS", clazz._code);
                    svf.VrsOut("PRE_CLASS_2", clazz._code);
                    svf.VrAttribute("PRE_CLASS", "X=10000");
                    svf.VrAttribute("PRE_CLASS_2", "X=10000");
                } else if (getMS932ByteLength(clazz._name) > 6) {
                    svf.VrsOut("PRE_CLASS", clazz._code);
                    svf.VrsOut("PRE_CLASS_2", clazz._name);
                    svf.VrsOut("PRE_CLASS_4", clazz._code);
                    svf.VrAttribute("PRE_CLASS", "X=10000");
                    svf.VrAttribute("PRE_CLASS_4", "X=10000");
                } else {
                    svf.VrsOut("PRE_CLASS", clazz._name);
                    svf.VrsOut("PRE_CLASS_2", clazz._code);
                    svf.VrsOut("PRE_CLASS_4", clazz._code);
                    svf.VrAttribute("PRE_CLASS_2", "X=10000");
                    svf.VrAttribute("PRE_CLASS_4", "X=10000");
                }

                // 科目
                boolean isAmikake = false;
                if ((_param._isJunior || _param._isHigh && "9900".equals(param._testCd)) && !exam.getAttendSubclass(student.gradeCourse()).contains(subClass._code)) {
                    isAmikake = true;
                }
                if (StringUtils.defaultString(subClass._name).length() > 9) {
                    svf.VrsOut("PRE_SUBCLASS_3_1", subClass._name);
                    if (isAmikake) {
                        svf.VrAttribute("PRE_SUBCLASS_3_1", "Paint=(1,90,2),Bold=1");
                        svf.VrAttribute("PRE_SUBCLASS_3_2", "Paint=(1,90,2),Bold=1");
                    }
                } else if (StringUtils.defaultString(subClass._name).length() > 7) {
                    svf.VrsOut("PRE_SUBCLASS_2", subClass._name);
                    if (isAmikake) {
                        svf.VrAttribute("PRE_SUBCLASS_2", "Paint=(1,90,2),Bold=1");
                    }
                } else {
                    svf.VrsOut("PRE_SUBCLASS", subClass._name);
                    if (isAmikake) {
                        svf.VrAttribute("PRE_SUBCLASS", "Paint=(1,90,2),Bold=1");
                    }
                }

                if (null == record) {
                    continue;
                }

                if (!student.hasKesshi(param, exam, record._subClass._code)) {
                    final String score = intnum(record._score, "");
                    if (param.isUnderScore(score)) {
                        svf.VrsOut("PRE_SCORE" + line, "(" + score + ")");
                    } else {
                        svf.VrsOut("PRE_SCORE" + line, score);
                    }
                    
                    svf.VrsOut("PRE_DEVI" + line, sishaGonyuStr(record._deviation));
                }
                printed = true;
            }
            
            if (printed) {
                svf.VrEndRecord();
                if (++i >= TABLE_SUBCLASS_MAX) {
                    break;
                }
                
            }
        }
        if (i == 0 && print1 == 0) {
            svf.VrsOut("CLASS", "\n");
            svf.VrEndRecord(); // データが無い時もこのコマンドを発行しないと印刷されないから
        }
    }

    private static class Exam {

        final String _year;
        final String _semester;
        final String _testCd;
        final String _useCurriculumcd;
        private String _grade;
        private String _examName = "";
        private String _semestername = "";
        private String _title = "";
        
        /** 成績平均データ。 */
        private Map _avgDat = new HashMap();
        /** 前回の換算した得点、平均、最高点 */
        private ConvertedScore _convertedScore;
        
        private Map _distribution;
        private Map _courseWeightingSubclassCdListMap;
        
        public Exam(final String year, final String semester, final String testCd, final String useCurriculumcd) {
            _year = year;
            _semester = semester;
            _testCd = testCd;
            _useCurriculumcd = useCurriculumcd; 
        }

        public String getKindCd() {
            return null == _testCd ? null : _testCd.substring(0, 2);
        }

        public String getItemCd() {
            return null == _testCd ? null : _testCd.substring(2);
        }
        
        private void load(final Param param, final DB2UDB db2) {
            _convertedScore = null;
            loadExam(db2);
        }
        
        private List getAttendSubclass(final String gradeCourse) {
            return getMappedList(getMappedMap(_courseWeightingSubclassCdListMap, gradeCourse), "ATTEND_SUBCLASS");
        }
        
        private List notTargetSubclasscdList(final String gradeCourse) {
            final List notTargetSubclassCdList;
            if (null != _testCd && _testCd.startsWith("99")) {
                notTargetSubclassCdList = Collections.EMPTY_LIST; // getMappedList(getMappedMap(_courseWeightingSubclassCdListMap, gradeCourse), "ATTEND_SUBCLASS");
            } else {
                // [学期末、学年末]以外は先を表示しない
                notTargetSubclassCdList = getMappedList(getMappedMap(_courseWeightingSubclassCdListMap, gradeCourse), "COMBINED_SUBCLASS");
            }
            return notTargetSubclassCdList;
        }
        
        private Map getCombinedAttendSubclassMap(final String course) {
            return getMappedMap(getMappedMap(_courseWeightingSubclassCdListMap, "PAIR"), course);
        }

        private List getAttendSubclassWithCombinedSubclasscdList(final String course, final String combiendSubclasscd) {
            return getMappedList(getCombinedAttendSubclassMap(course), combiendSubclasscd);
        }
        
        private void loadExam(final DB2UDB db2) {
            if (null == _year) {
                _title = "";
                return;
            }
            PreparedStatement ps = null;
            ResultSet rs = null;
            _examName = "";
            try {
                final String sql = "SELECT testitemname FROM testitem_mst_countflg_new"
                    + " WHERE year='" + _year + "' AND semester='" + _semester + "'"
                    + " AND testkindcd='" + getKindCd() + "' AND testitemcd='" + getItemCd() + "'"
                    ;
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    _examName = StringUtils.defaultString(rs.getString("testitemname"));
                }
            } catch (final SQLException e) {
                log.error("考査名取得エラー。");
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            _semestername = "";
            try {
                final String sql = "SELECT semestername FROM semester_mst"
                    + " WHERE year='" + _year + "' AND semester='" + _semester + "'"
                    ;
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    _semestername = StringUtils.defaultString(rs.getString("semestername"));
                }
            } catch (final SQLException e) {
                log.error("学期名取得エラー。");
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            final String nendo = KenjaProperties.gengou(Integer.parseInt(_year)) + "(" + _year + ")年度";
            _title = nendo + "　" + _semestername + "　" + _examName;
        }
    }

    private static class Student implements Comparable {
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
        final String _staffname;

        /** 成績科目 */
        final Map _subclasses = new HashMap();

        /** 成績データ。 */
        final Map _record = new HashMap();

        Student(
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
                final String staffname
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
            _staffname = staffname;
        }
        
        public Map record(final Exam exam) {
            if (null == _record.get(exam)) {
                _record.put(exam, new TreeMap());
            }
            return (Map) _record.get(exam);
        }
        
        public Map subclasses(final Exam exam) {
            if (null == _subclasses.get(exam)) {
                _subclasses.put(exam, new TreeMap());
            }
            return (Map) _subclasses.get(exam);
        }
        
//        public String attendSubclassCdKey() {
//            return _grade + courseKey();
//        }
//
//        public String courseKey() {
//            return _courseCd + _majorCd + _courseCode;
//        }
//
//        public String courseGroupKey() {
//            return "0" + _coursegroupCd + "0000";
//        }
        
        public String gradeCourse() {
            return _grade + _courseCd + _majorCd + _courseCode;
        }

        public String toString() {
            return _schregno + "/" + _name;
        }

        public int compareTo(Object o) {
            if (!(o instanceof Student)) {
                return -1;
            }
            final Student that = (Student) o;
            int rtn;
            rtn = this._grade.compareTo(that._grade);
            if (0 != rtn) {
                return rtn;
            }
            rtn = this._hrClass.compareTo(that._hrClass);
            if (0 != rtn) {
                return rtn;
            }
            return this._attendNo.compareTo(that._attendNo);
        }
        
        public boolean hasKesshi(final Param param, final Exam exam, final String subclasscd) {
            boolean hasAttendSubclasscdKesshi = false;
            // 2018/04/09 帳票での合併元科目の得点チェックと印字制御をカット
            // 一部の元科目に得点を入力しない際、先科目得点と合計点は合併設定を外せば作成されるが、元科目以外網掛けのため合併設定を戻す。
            // その際このチェックで先科目と合計点を印字しないようにチェックしている
//            final String course = gradeCourse();
//            if (null == subclasscd) {
//                for (final Iterator cait = exam.getCombinedAttendSubclassMap(course).entrySet().iterator(); cait.hasNext();) {
//                    final Map.Entry e = (Map.Entry) cait.next();
//                    final String combinedSubclasscd = (String) e.getKey();
//                    final List attendSubclasscdList = exam.getAttendSubclassWithCombinedSubclasscdList(course, combinedSubclasscd);
//                    for (final Iterator ait = attendSubclasscdList.iterator(); ait.hasNext();) {
//                        final String attendSubclasscd = (String) ait.next();
//                        final Record scoreAtt = (Record) record(exam).get(attendSubclasscd);
//                        if (null == scoreAtt || null == scoreAtt._score || null != scoreAtt._valueDi) {
//                            hasAttendSubclasscdKesshi = true;
//                            break;
//                        }
//                    }
//                }
//            } else {
//                final List attendSubclasscdList = exam.getAttendSubclassWithCombinedSubclasscdList(course, subclasscd);
//                for (final Iterator ait = attendSubclasscdList.iterator(); ait.hasNext();) {
//                    final String attendSubclasscd = (String) ait.next();
//                    final Record scoreAtt = (Record) record(exam).get(attendSubclasscd);
//                    if (null == scoreAtt || null == scoreAtt._score || null != scoreAtt._valueDi) {
//                        hasAttendSubclasscdKesshi = true;
//                        break;
//                    }
//                }
//            }
            return hasAttendSubclasscdKesshi;
        }
    }

    /**
     * 教科。
     */
    private static class Class {
        final String _code;
        final String _name;
        final String _abbv;

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
    private static class SubClass implements Comparable {
        final String _code;
        final String _name;
        final String _abbv;

        public SubClass(final String code, final String name, final String abbv) {
            _code = code;
            _name = name;
            _abbv = abbv;
        }

        public SubClass(final String code) {
            this(code, "xxx", "yyy");
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

        public String toString() {
            return _code + ":" + _abbv;
        }

        public int compareTo(final Object o) {
            if (!(o instanceof SubClass)) {
                return -1;
            }
            final SubClass that = (SubClass) o;
            return this._code.compareTo(that._code);
        }
    }

    private static class Record {
        final SubClass _subClass;
        final Integer _score;
        final Integer _scorePercent;
        final BigDecimal _avg;
        final Integer _rank;
        final BigDecimal _deviation;
        final String _valueDi;

        Record(
                final SubClass subClass,
                final Integer score,
                final Integer scorePercent,
                final BigDecimal avg,
                final Integer rank,
                final BigDecimal deviation,
                final String valueDi
        ) {
            _subClass = subClass;
            _score = score;
            _scorePercent = scorePercent;
            _avg = avg;
            _rank = rank;
            _deviation = deviation;
            _valueDi = valueDi;
        }

        public String toString() {
            return _subClass + "/" + _score + "/" + _rank + "/" + _deviation;
        }
    }

    private static class AverageDat {
        final SubClass _subClass;
        final BigDecimal _avg;
        final BigDecimal _avgPercent;
        final BigDecimal _stdDev;
        final Integer _highscore;
        final Integer _lowscore;
        final Integer _count;

        AverageDat(
                final SubClass subClass,
                final BigDecimal avg,
                final BigDecimal avgPercent,
                final BigDecimal stdDev,
                final Integer highscore,
                final Integer lowscore,
                final Integer count
        ) {
            _subClass = subClass;
            _avg = avg;
            _avgPercent = avgPercent;
            _stdDev = stdDev;
            _highscore = highscore;
            _lowscore = lowscore;
            _count = count;
        }

        public String toString() {
            return _subClass + "/" + _avg + "/" + _count;
        }
    }
    
    /** 「100点に換算する」場合に表示するデータ */
    private static class ConvertedScore {
    }
    
    private class KNJD105GSpiderWebPlot extends SpiderWebPlot {
        
        private Color lightGrayDarker = new Color(160, 160, 160);

        private class Gauge {
            final int _value;
            final boolean _isBold;
            final List _shapes = new ArrayList();
            final List _shapes2 = new ArrayList();
                       int _x;
            int _y;
            Gauge(final int value, final boolean isBold) {
                _value = value;
                _isBold = isBold;
            }
        }

        /**
         * 目盛りのスタイル
         */
        //private BasicStroke _gaugeStroke = new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1f, new float[] { 3f }, 0);
        private BasicStroke _stroke1 = new BasicStroke();
        private BasicStroke _stroke2 = new BasicStroke(2f);

        public void draw(Graphics2D g2, Rectangle2D area, Point2D anchor,
                PlotState parentState, PlotRenderingInfo info) {

            CategoryDataset dataset = getDataset();

            // adjust for insets...
            RectangleInsets insets = getInsets();
            insets.trim(area);

            if (info != null) {
                info.setPlotArea(area);
                info.setDataArea(area);
            }

            drawBackground(g2, area);
            drawOutline(g2, area);

            Shape savedClip = g2.getClip();

            g2.clip(area);
            Composite originalComposite = g2.getComposite();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                    getForegroundAlpha()));

            if (!DatasetUtilities.isEmptyOrNull(dataset)) {
                int seriesCount = 0, catCount = 0;

                if (getDataExtractOrder() == TableOrder.BY_ROW) {
                    seriesCount = dataset.getRowCount();
                    catCount = dataset.getColumnCount();
                } else {
                    seriesCount = dataset.getColumnCount();
                    catCount = dataset.getRowCount();
                }

                // Next, setup the plot area

                // adjust the plot area by the interior spacing value

                double gapHorizontal = area.getWidth() * getInteriorGap();
                double gapVertical = area.getHeight() * getInteriorGap();

                double X = area.getX() + gapHorizontal / 2;
                double Y = area.getY() + gapVertical / 2;
                double W = area.getWidth() - gapHorizontal;
                double H = area.getHeight() - gapVertical;

                double headW = area.getWidth() * this.headPercent;
                double headH = area.getHeight() * this.headPercent;

                // make the chart area a square
                double min = Math.min(W, H) / 2;
                X = (X + X + W) / 2 - min;
                Y = (Y + Y + H) / 2 - min;
                W = 2 * min;
                H = 2 * min;

                Point2D centre = new Point2D.Double(X + W / 2, Y + H / 2);
                Rectangle2D radarArea = new Rectangle2D.Double(X, Y, W, H);

                // 目盛りのリストを準備する
                final Gauge[] axisGauge = {new Gauge(40, false), new Gauge(50, false), new Gauge(60, false), new Gauge(70, false)};

                // draw the axis and category label
                for (int cat = 0; cat < catCount; cat++) {
                    double angle = getStartAngle() + (getDirection().getFactor() * cat * 360 / catCount);

                    final Point2D endPoint = getWebPoint(radarArea, angle, 1);

                    // 目盛りの線を追加
                    for (int j = 0; j < axisGauge.length; j++) {
                        final Gauge gauge = axisGauge[j];
                        final double rate = gauge._value / getMaxValue();
                        final int baseX = (int) (centre.getX() + (endPoint.getX() - centre.getX()) * rate);
                        final int baseY = (int) (centre.getY() + (endPoint.getY() - centre.getY()) * rate);
                        //final int len = gauge._isBold ? 4 : 3;
                        //final int len2 = len - 1;
                        final Shape shape = new Rectangle2D.Double(baseX - 1, baseY - 1, 3, 3);  //getPolygon(angle, len, baseX, baseY);
//                        final Shape shape2 = new Rectangle2D.Double(baseX - 1, baseY - 1, 2, 2);
                        //gauge._x = p1x;
                        //gauge._y = p1y;
                        gauge._shapes.add(shape);
//                        gauge._shapes2.add(shape2);
                        if (cat == 0) {
                            gauge._x = 149;
                            gauge._y = 84 - 11 * j;
                        }
                    }
                    
                    // 1 = end of axis
                    final Line2D.Double line = new Line2D.Double(centre, endPoint);
                    g2.setPaint(Color.gray);
                    g2.draw(line);
                    drawLabel(g2, radarArea, 0.0, cat, angle, 360.0 / catCount);
                }
                
                // 目盛りの値を描画
                for (int j = 0; j < axisGauge.length; j++) {
                    final Gauge gauge = axisGauge[j];
                    g2.setPaint(Color.black);
                    g2.drawString(String.valueOf(gauge._value), gauge._x + 5, gauge._y);
                }

                // Now actually plot each of the series polygons..
                for (int series = 0; series < seriesCount; series++) {
                    drawRadarPoly(g2, radarArea, centre, info, series, catCount, headH, headW, axisGauge);
                }

            } else {
                drawNoDataMessage(g2, area);
            }
            g2.setClip(savedClip);
            g2.setComposite(originalComposite);
            drawOutline(g2, area);
        }
        
        /**
         * Returns the value to be plotted at the interseries of the 
         * series and the category.  This allows us to plot
         * BY_ROW or BY_COLUMN which basically is just reversing the
         * definition of the categories and data series being plotted
         * 
         * @param series the series to be plotted 
         * @param cat the category within the series to be plotted
         * 
         * @return The value to be plotted
         */
        public Number getPlotValue(int series, int cat) {
            Number value = null;
            if (getDataExtractOrder() == TableOrder.BY_ROW) {
                value = getDataset().getValue(series, cat);
            }
            else if (getDataExtractOrder() == TableOrder.BY_COLUMN) {
                value = getDataset().getValue(cat, series);
            }
            return value;
        }
        
        /**
         * Draws a radar plot polygon.
         * 
         * @param g2 the graphics device.
         * @param plotArea the area we are plotting in (already adjusted).
         * @param centre the centre point of the radar axes
         * @param info chart rendering info.
         * @param series the series within the dataset we are plotting
         * @param catCount the number of categories per radar plot
         * @param headH the data point height
         * @param headW the data point width
         */
        protected void drawRadarPoly(Graphics2D g2, 
                                     Rectangle2D plotArea,
                                     Point2D centre,
                                     PlotRenderingInfo info,
                                     int series, int catCount,
                                     double headH, double headW,
                                     final Gauge[] axisGauge) {

            Polygon polygon = new Polygon();

            EntityCollection entities = null;
            if (info != null) {
                entities = info.getOwner().getEntityCollection();
            }

            // plot the data...
            for (int cat = 0; cat < catCount; cat++) {
                Number dataValue = getPlotValue(series, cat);

                if (dataValue != null) {
                    double value = dataValue.doubleValue();
      
                    if (value >= 0) { // draw the polygon series...
                  
                        // Finds our starting angle from the centre for this axis

                        double angle = getStartAngle()
                            + (getDirection().getFactor() * cat * 360 / catCount);

                        // The following angle calc will ensure there isn't a top 
                        // vertical axis - this may be useful if you don't want any 
                        // given criteria to 'appear' move important than the 
                        // others..
                        //  + (getDirection().getFactor() 
                        //        * (cat + 0.5) * 360 / catCount);

                        // find the point at the appropriate distance end point 
                        // along the axis/angle identified above and add it to the
                        // polygon

                        Point2D point = getWebPoint(plotArea, angle, value / getMaxValue());
                        polygon.addPoint((int) point.getX(), (int) point.getY());

                        // put an elipse at the point being plotted..

                        Paint paint = getSeriesPaint(series);
                        Paint outlinePaint = getSeriesOutlinePaint(series);
                        Stroke outlineStroke = getSeriesOutlineStroke(series);

                        Ellipse2D head = new Ellipse2D.Double(point.getX() 
                                - headW / 2, point.getY() - headH / 2, headW, 
                                headH);
                        g2.setPaint(paint);
                        g2.fill(head);
                        g2.setStroke(outlineStroke);
                        g2.setPaint(outlinePaint);
                        g2.draw(head);

                        if (entities != null) {
                            String tip = null;
//                            if (this.toolTipGenerator != null) {
//                                tip = this.toolTipGenerator.generateToolTip(
//                                        this.dataset, series, cat);
//                            }

                            String url = null;
//                            if (this.urlGenerator != null) {
//                                url = this.urlGenerator.generateURL(this.dataset, 
//                                       series, cat);
//                            } 
                       
                            Shape area = new Rectangle((int) (point.getX() - headW), 
                                    (int) (point.getY() - headH), 
                                    (int) (headW * 2), (int) (headH * 2));
                            CategoryItemEntity entity = new CategoryItemEntity(
                                    area, tip, url, getDataset(), series,
                                    getDataset().getColumnKey(cat), cat); 
                            entities.add(entity);                                
                        }

                        // then draw the axis and category label, but only on the 
                        // first time through.....

                        if (series == 0) {
                            Point2D endPoint = getWebPoint(plotArea, angle, 1); 
                                                                 // 1 = end of axis
                            Line2D  line = new Line2D.Double(centre, endPoint);
                            g2.draw(line);
                            drawLabel(g2, plotArea, value, cat, angle, 360.0 / catCount);
                            
                            // 目盛りの線を描画
                            for (int j = 0; j < axisGauge.length; j++) {
                                final Gauge gauge = axisGauge[j];
//                                if (gauge._isBold) {
//                                    g2.setPaint(Color.gray);
//                                    g2.setStroke(_stroke2);
//                                } else {
//                                    g2.setPaint(lightGrayDarker);
//                                    g2.setStroke(_stroke1);
//                                }
                                for (final Iterator it = gauge._shapes.iterator(); it.hasNext();) {
                                    final Shape shape = (Shape) it.next();
                                    g2.setPaint(Color.black);
                                    g2.fill(shape);
                                }
                                for (final Iterator it = gauge._shapes2.iterator(); it.hasNext();) {
                                    final Shape shape = (Shape) it.next();
                                    g2.setPaint(Color.white);
                                    g2.fill(shape);
                                }
                            }
                        }
                    }
                }
            }
            // Plot the polygon
        
            Paint paint = getSeriesPaint(series);
            g2.setPaint(paint);
            g2.draw(polygon);

            // Lastly, fill the web polygon if this is required
        
//            if (super.webFilled) {
//                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 
//                        0.1f));
//                g2.fill(polygon);
//                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 
//                        getForegroundAlpha()));
//            }
        }

        public Shape getPolygon(double angle, final int len, final int baseX, final int baseY) {
            final Point2D[] ps = getPoints(angle, baseX, baseY, len);
            //final Shape shape = new Line2D.Double(p1, p3);
            final int[] xpoints = new int[4];
            final int[] ypoints = new int[4];
            for (int i = 0; i < 4; i++) {
                xpoints[i] = (int) ps[i].getX();
                ypoints[i] = (int) ps[i].getY();
            }
            final Shape shape = new Polygon(xpoints, ypoints, 4);
            return shape;
        }

        public Point2D[] getPoints(double angle, final int baseX, final int baseY, final int len) {
            final Point2D p1 = new Point2D.Double(baseX + len * Math.cos(Math.toRadians(angle + 90)), baseY - len * Math.sin(Math.toRadians(angle + 90)));
            final Point2D p2 = new Point2D.Double(baseX + len * Math.cos(Math.toRadians(angle +  0)), baseY - len * Math.sin(Math.toRadians(angle +  0)));
            final Point2D p3 = new Point2D.Double(baseX + len * Math.cos(Math.toRadians(angle - 90)), baseY - len * Math.sin(Math.toRadians(angle - 90)));
            final Point2D p4 = new Point2D.Double(baseX + len * Math.cos(Math.toRadians(angle -180)), baseY - len * Math.sin(Math.toRadians(angle -180)));
            final Point2D[] ps = {p1, p2, p3, p4};
            return ps;
        }
    }
    
    private Param createParam(final HttpServletRequest request, final DB2UDB db2) throws SQLException {
        log.fatal("$Revision: 72387 $ $Date: 2020-02-13 18:19:53 +0900 (木, 13 2 2020) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);

        if (!KNJServletUtils.isEnableGraph(log)) {
            log.fatal("グラフを使える環境ではありません。");
        }
        final Param param = new Param(request, db2);
        return param;
    }

    private static class Param {
        final String _year;
        final String _semester;
        final String _ctrlSeme;
        final String _testCd;
        final String _grade;
        private String _schoolKind;
        final String _categoryIsClass;
        final String _groupDiv;

        /** [クラス指定 or 生徒指定]の値。 */
        final String _div;
        /** クラス or 生徒。 */
        final String[] _categorySelected;

//        final String _submitDate;
        final String _loginDate;

        /** 学校名。 */
        private String _schoolName;
        /** 担任職種名。 */
        private String _remark2;
        /** 基準点 */
        final String _outputKijun;

        /** 教科マスタ。 */
        private Map _classes;
        final String _imagePath;

        final String _useCurriculumcd;
        final String _useClassDetailDat;

//        final List _classOrder = new ArrayList();

        /** 中学か? false なら高校. */
        private boolean _isJunior;
        private boolean _isHigh;

        /** 欠点 */
        final int _borderScore;
        
//        /** 100点満点に換算する */
//        private final boolean _isConvertScoreTo100;
        
        final Exam _thisExam;
        private List _beforeExamList;
        private Exam _beforeExam0; // 1つ前の試験

        /** 科目マスタ。 */
        private Map _subClasses;

        /** 氏名欄に学籍番号の表示/非表示 1:表示 それ以外:非表示 */
        final String _use_SchregNo_hyoji;
        
        public Param(final HttpServletRequest request, final DB2UDB db2) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _ctrlSeme = request.getParameter("CTRL_SEME");
            _testCd = request.getParameter("TESTCD");
            _div = request.getParameter("CATEGORY_IS_CLASS");
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
//            _submitDate = request.getParameter("SUBMIT_DATE");
            _loginDate = request.getParameter("LOGIN_DATE");
            _grade = request.getParameter("GRADE");
            _categoryIsClass = request.getParameter("CATEGORY_IS_CLASS");

            // 1=学年, 2=コース
            _groupDiv =  request.getParameter("GROUP_DIV");

            _imagePath = request.getParameter("IMAGE_PATH");
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _useClassDetailDat = request.getParameter("useClassDetailDat");
            _outputKijun = request.getParameter("OUTPUT_KIJUN");
            log.debug(" record_rank_dat outputKijun ? = " + _outputKijun);
            imageFileCheck(RADER_CHART_LEGEND);
            imageFileCheck(RADER_CHART_LEGEND_NO50);
            imageFileCheck(RADER_CHART_LEGEND_NO50_WITH_BEFORE);
//            imageFileCheck(BAR_CHART_LEGEND1);
//            imageFileCheck(BAR_CHART_LEGEND2);
            
            _borderScore = Integer.parseInt(!NumberUtils.isDigits(request.getParameter("KETTEN")) ? "0" : request.getParameter("KETTEN"));
//            _isPrintDistribution = "2".equals(request.getParameter("USE_GRAPH"));
//            _isPrintGuardianComment = "1".equals(request.getParameter("USE_HOGOSYA"));
//            _isConvertScoreTo100 = "1".equals(request.getParameter("KANSAN"));

            _use_SchregNo_hyoji = request.getParameter("use_SchregNo_hyoji");
            
            _thisExam = new Exam(_year, _semester, _testCd, _useCurriculumcd);
            _thisExam._grade = _grade;
            
            loadSchoolKind(db2);
            _isJunior = "J".equals(_schoolKind);
            log.debug("中学校?:" + _isJunior);
            _isHigh = !_isJunior;

            loadCertifSchool(db2);
            loadBefore(db2);
            _thisExam.load(this, db2);
            for (final Iterator it = _beforeExamList.iterator(); it.hasNext();) {
                final Exam beforeExam = (Exam) it.next();
                beforeExam.load(this, db2);
            }
            _classes = setClasses(db2);
            _subClasses = setSubClasses(db2);
//            loadClassOrder(db2);
        }

        private void imageFileCheck(final String fName) {
            final File f = new File(_imagePath + "/" + fName);
            if (!f.exists()) {
                log.fatal("画像ファイルが無い!⇒" + _imagePath + "/" + fName);
            }
        }

        // 前回が中間試験ならレーダーチャートに前回の試験を表示する
        private boolean isPrintRadarWithBefore() {
            return "01".equals(_beforeExam0.getKindCd());
        }

        public String[] getScregnos(final DB2UDB db2) throws SQLException {
            final String separator = "-";
            PreparedStatement ps = null;
            ResultSet rs = null;
            final List result = new ArrayList();

            if (!"1".equals(_categoryIsClass)) {
                return _categorySelected;
            }

            // 年組から学籍番号たちを得る
            for (int i = 0; i < _categorySelected.length; i++) {
                final String grade = StringUtils.split(_categorySelected[i], separator)[0];
                final String room = StringUtils.split(_categorySelected[i], separator)[1];
                
                try {
                    final String sql = " select"
                            + "    SCHREGNO as schregno"
                            + " from"
                            + "    SCHREG_REGD_DAT"
                            + " where"
                            + "    YEAR = '" + _year + "' and"
                            + "    SEMESTER = '" + ("9".equals(_semester) ? _ctrlSeme : _semester) + "' and"
                            + "    GRADE = '" + grade + "' and"
                            + "    HR_CLASS = '" + room + "'"
                            ;

                    ps = db2.prepareStatement(sql);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        final String schregno = rs.getString("schregno");
                        result.add(schregno);
                    }
                } catch (final SQLException e) {
                    log.error("年組から学籍番号たちを得る際にエラー");
                    throw e;
                } finally {
                	DbUtils.closeQuietly(rs);
                	db2.commit();
                }
            }
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);

            final String[] rtn = new String[result.size()];
            int i = 0;
            for (final Iterator it = result.iterator(); it.hasNext();) {
                final String schregno = (String) it.next();
                rtn[i++] = schregno;
            }

            return rtn;
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
                sql +="   coalesce(SUBCLASSORDERNAME2, SUBCLASSNAME ) as NAME,"
                    + "   SUBCLASSABBV"
                    + " from V_SUBCLASS_MST"
                    + " where"
                    + "   YEAR = '" + _year + "'"
                    + " order by";
                if  ("1".equals(_useCurriculumcd)) {
                    sql +="   CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD";
                } else {
                    sql +="   SUBCLASSCD";
                }

                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String code = rs.getString("SUBCLASSCD");
                    final String name = rs.getString("NAME");
                    final String abbv = rs.getString("SUBCLASSABBV");
                    rtn.put(code, new SubClass(code, name, abbv));
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            log.debug("科目マスタ総数=" + rtn.size());
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
                    + " from CLASS_MST"
                    + " order by";
                if ("1".equals(_useCurriculumcd)) {
                    sql +="   CLASSCD || '-' || SCHOOL_KIND ";
                } else {
                    sql +="   CLASSCD";
                }
                ;

                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String code = rs.getString("CLASSCD");
                    final String name = rs.getString("CLASSNAME");
                    final String abbv = rs.getString("CLASSABBV");
                    rtn.put(code, new Class(code, name, abbv));
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            //log.debug("教科マスタ=" + rtn);
            return rtn;
        }

        private void loadBefore(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
//            boolean differentYear = false;
            final List beforeExamList = new ArrayList();
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append("   select ");
                stb.append("     t1.YEAR, ");
                stb.append("     t1.SEMESTER, ");
                stb.append("     t1.TESTKINDCD || t1.TESTITEMCD as testcd, ");
                stb.append("     case when t2.YEAR is not null then 1 else 0 end as CURRENT ");
                stb.append("   from  ");
                stb.append("     TESTITEM_MST_COUNTFLG_NEW t1 ");
                stb.append("     left join TESTITEM_MST_COUNTFLG_NEW t2 ON ");
                stb.append("       t2.YEAR = '" + _year + "' ");
                stb.append("       and t2.SEMESTER = '" + _semester + "' ");
                stb.append("       and t2.TESTKINDCD = '" + _thisExam.getKindCd() + "' ");
                stb.append("       and t2.TESTITEMCD = '" + _thisExam.getItemCd() + "' ");
                stb.append("       and t2.YEAR = t1.YEAR ");
                stb.append("       and t2.SEMESTER = t1.SEMESTER ");
                stb.append("       and t2.TESTKINDCD = t1.TESTKINDCD ");
                stb.append("       and t2.TESTITEMCD = t1.TESTITEMCD ");
                stb.append("   where ");
//                stb.append("     t1.YEAR IN ('" + _year + "', '" + (Integer.parseInt(_year) - 1) + "') ");
                stb.append("     t1.YEAR IN ('" + _year + "') ");
//                stb.append("     and t1.SEMESTER <> '9' ");
//                stb.append("     and t1.TESTKINDCD <> '99' ");
                stb.append(" order by ");
                stb.append("     t1.YEAR, ");
                stb.append("     t1.SEMESTER, ");
                stb.append("     t1.TESTKINDCD, ");
                stb.append("     t1.TESTITEMCD ");
                
                log.debug(" loadBefore sql = " + stb.toString());
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    if (1 == rs.getInt("CURRENT")) {
                        break;
                    }
                    final String beforeYear = rs.getString("year");
                    final String beforeSemester = rs.getString("semester");
                    final String beforeTestCd = rs.getString("testcd");
                    
                    if (_testCd.startsWith("99")) {
                        if ("9901".equals(_testCd) && "9".equals(beforeSemester) && "9900".equals(beforeTestCd)) {
                            continue;
                        }
                        if ("3".equals(beforeSemester) && "9900".equals(beforeTestCd)) {
                            continue;
                        }
                        // 前回の試験は9900を表示
                        if (!"9900".equals(beforeTestCd)) {
                            continue;
                        }
                    } else {
                        // 前回の試験は9900以外を表示
                        if ("9900".equals(beforeTestCd)) {
                            continue;
                        }
                    }
                    
                    final Exam beforeExam = new Exam(beforeYear, beforeSemester, beforeTestCd, _useCurriculumcd);
//                    if (!_year.equals(beforeYear)) {
//                        differentYear = true;
//                    } else {
                    beforeExam._grade = _grade;
//                    }
                    beforeExamList.add(beforeExam);
                }
            } catch (final SQLException e) {
                log.error("以前の考査取得エラー。", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            _beforeExamList = new ArrayList();
            for (final ListIterator it = beforeExamList.listIterator(beforeExamList.size()); it.hasPrevious();) {
                _beforeExamList.add(it.previous());
            }
            _beforeExam0 = (_beforeExamList.size() == 0) ? new Exam(null, null, null, null) : (Exam) _beforeExamList.get(0);
            
//            if (differentYear) {
//                try {
//                    final StringBuffer stb = new StringBuffer();
//                    stb.append(" with REGD as ( ");
//                    stb.append("   select distinct ");
//                    stb.append("     t1.SCHREGNO ");
//                    stb.append("   from  ");
//                    stb.append("     SCHREG_REGD_DAT t1 ");
//                    stb.append("   where  ");
//                    stb.append("     t1.YEAR = '" + _thisExam._year + "' ");
//                    stb.append("     and t1.SEMESTER = '" + _thisExam._semester + "' ");
//                    stb.append("     and t1.GRADE = '" + _grade + "' ");
//                    stb.append(" ) ");
//                    stb.append("   select  ");
//                    stb.append("     MAX(t1.GRADE) AS GRADE ");
//                    stb.append("   from  ");
//                    stb.append("     SCHREG_REGD_DAT t1 ");
//                    stb.append("   inner join REGD t2 on t2.schregno = t1.schregno ");
//                    stb.append("   where  ");
//                    stb.append("     t1.YEAR = '" + _beforeExam._year + "' ");
//                    stb.append("     and t1.SEMESTER = '" + _beforeExam._semester + "' ");
//                    
//                    ps = db2.prepareStatement(stb.toString());
//                    rs = ps.executeQuery();
//                    if (rs.next()) {
//                        _beforeExam._grade = rs.getString("GRADE");
//                    }
//                } catch (final SQLException e) {
//                    log.error("以前の考査取得エラー。");
//                } finally {
//                    db2.commit();
//                    DbUtils.closeQuietly(null, ps, rs);
//                }
//            }
        }
        
        private void loadSchoolKind(final DB2UDB db2) throws SQLException {

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = "SELECT school_kind FROM schreg_regd_gdat "
                    + " WHERE year='" + _year + "' AND grade='" + _grade + "'";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    _schoolKind = rs.getString("school_Kind");
                }
            } catch (final SQLException e) {
                log.error("学校名取得エラー。");
                throw e;
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
                final String sql = "SELECT school_name, remark2 FROM certif_school_dat"
                    + " WHERE year='" + _year + "' AND certif_kindcd='" + key + "'";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    _schoolName = rs.getString("school_name");
                    _remark2 = rs.getString("remark2");
                }
            } catch (final SQLException e) {
                log.error("学校名取得エラー。");
                throw e;
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

//        private void loadClassOrder(final DB2UDB db2) {
//            final String sql;
//            if ("1".equals(_useCurriculumcd) && "1".equals(_useClassDetailDat)) {
//                sql = "SELECT classcd || '-' || school_kind AS classcd FROM class_detail_dat"
//                        + " WHERE year='" + _year + "' AND class_seq='004' "
//                        + " ORDER BY int(value(class_remark1, '99')), classcd || '-' || school_kind "
//                        ;
//            } else {
//                final String field1 = _isJunior ? "name1" : "name2";
//                final String field2 = _isJunior ? "namespare1" : "namespare2";
//                sql = "SELECT " + field1 + " AS classcd FROM v_name_mst"
//                    + " WHERE year='" + _year + "' AND namecd1='D009' AND " + field1 + " IS NOT NULL "
//                    + " ORDER BY " + field2
//                    ;
//            }
//
//            PreparedStatement ps = null;
//            ResultSet rs = null;
//            try {
//                ps = db2.prepareStatement(sql);
//                rs = ps.executeQuery();
//                while (rs.next()) {
//                    _classOrder.add(rs.getString("classcd"));
//                }
//            } catch (final SQLException e) {
//                log.error("教科表示順取得エラー。");
//            } finally {
//                DbUtils.closeQuietly(null, ps, rs);
//                db2.commit();
//            }
//            log.debug("教科表示順=" + _classOrder);
//        }

        public boolean isUnderScore(final String score) {
            if (StringUtils.isEmpty(score)) {
                return false;
            }
            final int val = Integer.parseInt(score);
            return val < _borderScore;
        }
    }
} // KNJD105G

// eof
