// kanji=漢字
/*
 * $Id: d3ed763f01f84de2fe34da3928024192833205d2 $
 *
 * 作成日: 2008/05/24 10:02:00 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package servletpack.KNJH;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.PlotState;
import org.jfree.chart.plot.SpiderWebPlot;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.ui.RectangleInsets;
import org.jfree.util.TableOrder;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 * 個人成績票。
 * @author takaesu
 * @version $Id: d3ed763f01f84de2fe34da3928024192833205d2 $
 */
public class KNJH563G {
    /*pkg*/static final Log log = LogFactory.getLog(KNJH563G.class);

    private Param _param;
    private boolean _hasData;

    private final Chart _chart = new Chart();

    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {
        _param = createParam(request);
        
        Vrw32alp svf = new Vrw32alp();
        if (svf.VrInit() < 0) {
            throw new IllegalStateException("svf初期化失敗");
        }
        svf.VrSetSpoolFileStream(response.getOutputStream());
        response.setContentType("application/pdf");

        final DB2UDB db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
        try {
            db2.open();
        } catch (final Exception ex) {
            log.error("db open error!", ex);
            return;
        }

        log.error("★マスタ関連の読込み");
        _param.load(db2);

        // 対象の生徒たちを得る
        final List students = createStudents(db2, _param);
        _hasData = students.size() > 0;

        // 成績のデータを読む
        log.error("★成績関連の読込み");
        
        loadExam(db2, students, _param._thisExam);
        loadExam(db2, students, _param._beforeExam);

        // 印刷する
        log.error("★印刷");
        for (final Iterator it = students.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();

            printStudent(svf, student);
        }

        log.error("★終了処理");
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

        _chart.removeImageFiles();
        log.info("Done.");
    }

    private void printStudent(final Vrw32alp svf, final Student student) {
        log.debug(" student = " + student._schregno);
        //log.debug("☆" + student + ", 科目の数=" + student.data(_param._thisExam)._subclasses.size() + ", コースキー=" + student.regd(_param._thisExam).courseKey());
        //log.debug("今回の成績: " + student.data(_param._thisExam)._record.values());

//        _param.setSubclasses(student);

        final int sts;
        sts = svf.VrSetForm("KNJH563G.frm", 4);
        if (0 != sts) {
            log.error("VrSetFromエラー:sts=" + sts);
        }

        printStatic(svf, _param, student);
        
        printScoreDist(svf, _param, _param._thisExam, student);
        // グラフ印字(サブフォームよりも先に印字)
        //_chart.printBarGraph(svf, student, _param._thisExam);
        _chart.printRadarGraph(svf, student);
        printRecord(svf, false, student, _param._thisExam);

        // 前回の成績
        printRecord(svf, true, student, _param._beforeExam);

        svf.VrPrint();
    }
    
    private void printStatic(final Vrw32alp svf, final Param param, final Student student) {
        svf.VrsOut("SCHOOL_NAME", param._schoolName);
        
        final Regd regd = student.regd(_param._thisExam);

        svf.VrsOut("STAFFNAME", StringUtils.defaultString(param._remark2) + StringUtils.defaultString(regd._staffname));
    
        svf.VrsOut("NENDO", _param._thisExam._title);
        svf.VrsOut("LAST_EXAM_TITLE", StringUtils.isBlank(_param._beforeExam._title) ? "" : (_param._beforeExam._title + "の成績"));
    
        svf.VrsOut("HR_NAME", StringUtils.defaultString(regd._hrName) + regd.attendNoStr());

        svf.VrsOut(student._name.length() <= 10 ? "NAME" : "NAME2", student._name);

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
//        if (null != get3title) {
//            svf.VrsOut("ITEM_TOTAL3", get3title);
//        }
//        final String get5title = _param.get5title(student.courseKey());
//        if (null != get5title) {
//            svf.VrsOut("ITEM_TOTAL5", get5title);
//        }
        svf.VrsOut("ITEM_TOTAL5", "全教科");

        svf.VrsOut("ITEM_AVG", _param.formGroupDivName() + "平均");
        svf.VrsOut("PRE_ITEM_AVG", _param.formGroupDivName() + "平均");
        svf.VrsOut("ITEM_RANK", _param.formGroupDivName() + "順位");

        svf.VrsOut("ITEM_DEVIATION", "偏差値");

        // 画像
        svfVrsOutImage(svf, "BAR_LEGEND", _param.barchartLegendImage());

        svf.VrsOut("BAR_TITLE", "度数分布表");
    }
    
    private void svfVrsOutImage(final Vrw32alp svf, final String field, final String name) {
        final File f = new File(_param._imagePath + "/" + name);
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
        
        final int[][] scores = new int[10][];
        final int maxScore = 100;
        final int kizami = 10;
        for (int i = 0; i < scores.length; i++) {
            final int max = i == 0 ? maxScore : maxScore - i * kizami - 1;
            final int min = maxScore - (i + 1) * kizami;
            scores[i] = new int[] {min, max};
        }
        
        final int maxColum = 11;
        final String[] token0 = KNJ_EditEdit.get_token("全教科 平均", 8, 2);
        if (null != token0) {
            for (int i = 0; i < token0.length; i++) {
                svf.VrsOutn("SUBCLASS2_" + String.valueOf(i + 1), maxColum, token0[i]);
            }
        }
        final String avgKey999 = avgKey(param, student.regd(exam), param.ALL9);
        if (null != exam._distribution.get(avgKey999)) {
//            BigDecimal avgavg = null;
            final List scoreListAll9 = (List) exam._distribution.get(avgKey999);
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
//            avgavg = getAvg((List) exam._distribution.get(avgKey999));

            if (null != exam._avgDat.get(avgKey999)) {
                final AverageDat avgDat = (AverageDat) exam._avgDat.get(avgKey999);
                svf.VrsOutn("DIST_NUM", maxColum, intnum(avgDat._count, ""));
                //svf.VrsOutn("DIST_AVE", maxColum, sishaGonyuStr(avgavg));
                svf.VrsOutn("DIST_AVE", maxColum, sishaGonyuStr(avgDat._avg));
                svf.VrsOutn("DIST_MAX", maxColum, intnum(avgDat._highscore, ""));
                svf.VrsOutn("DIST_MIN", maxColum, intnum(avgDat._lowscore, ""));
                svf.VrsOutn("DIST_DEV", maxColum, sishaGonyuStr(avgDat._stddev));
            }
        }

        int column = 1;
        for (final Iterator it = student.data(exam)._subclasses.values().iterator(); it.hasNext();) {
            final SubClass subClass = (SubClass) it.next();
            final String avgKey = avgKey(param, student.regd(exam), subClass._code);
            if (null == exam._distribution.get(avgKey) && null == exam._avgDat.get(avgKey)) {
                continue;
            }
            
            if (param.ALL3.equals(subClass._code) || param.ALL5.equals(subClass._code) || param.ALL9.equals(subClass._code)) {
                continue;
            }
            
            if (getMS932ByteLength(subClass._name) <= 6) {
                svf.VrsOutn("SUBCLASS2", column, subClass._name);
            } else {
                final String[] token = KNJ_EditEdit.get_token(subClass._name, 8, 2);
                if (null != token) {
                    for (int i = 0; i < token.length; i++) {
                        svf.VrsOutn("SUBCLASS2_" + String.valueOf(i + 1), column, token[i]);
                    }
                }
            }

            if (null != exam._distribution.get(avgKey)) {
                //log.info(" code = " + record._subClass._code);
                final List scoreList = (List) exam._distribution.get(avgKey);
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
            if (null != exam._avgDat.get(avgKey)) {
                final AverageDat avgDat = (AverageDat) exam._avgDat.get(avgKey);
                svf.VrsOutn("DIST_NUM", column, intnum(avgDat._count, ""));
                svf.VrsOutn("DIST_AVE", column, sishaGonyuStr(avgDat._avg));
                svf.VrsOutn("DIST_MAX", column, intnum(avgDat._highscore, ""));
                svf.VrsOutn("DIST_MIN", column, intnum(avgDat._lowscore, ""));
                svf.VrsOutn("DIST_DEV", column, sishaGonyuStr(avgDat._stddev));
                
            }
            column += 1;
            if (maxColum - 1 <= column) {
                continue;
            }
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
        final BigDecimal avg = sum.divide(new BigDecimal(scoreList.size()), 2, BigDecimal.ROUND_HALF_UP);
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

    // 成績
    private void printRecord(final Vrw32alp svf, final boolean isPre, final Student student, final Exam exam) {
        final String pres = isPre ? "PRE_" : "";
        final ExamData dat = student.data(exam);
        final Record rec9 = (Record) dat._record.get(_param.ALL9);
        if (null != rec9) {
            svf.VrsOut(pres + "SCORE5",     rec9.getScore());
            if (!_param._rankNotPrint) {
                svf.VrsOut(pres + "RANK5",      intnum(rec9._rank, ""));
            }
            svf.VrsOut(pres + "DEVIATION5", sishaGonyuStr(rec9._deviation));
        }
        
        final AverageDat avg9 = (AverageDat) exam._avgDat.get(avgKey(_param, student.regd(exam), _param.ALL9));
        if (null != avg9) {
            svf.VrsOut(pres + "AVERAGE5",  sishaGonyuStr(avg9._avg));
            svf.VrsOut(pres + "MAX_SCORE5", avg9._highscore.toString());
            svf.VrsOut(pres + "EXAMINEE5", avg9._count.toString());
        }

        /** 成績表の最大科目数. */
        final int TABLE_SUBCLASS_MAX = 20;

        int i = 0;
        for (final Iterator it = dat._subclasses.values().iterator(); it.hasNext();) {
            final SubClass subClass = (SubClass) it.next();

//            if ("90".equals(subClass.getClassCd())) {
//                continue;
//            }

//            final boolean hasSubclass5 = (null != _param._fiveSubclass && _param._fiveSubclass.contains(subClass._code));
//            final boolean hasSubclass3 = (null != _param._threeSubclass && _param._threeSubclass.contains(subClass._code));
//            if (!hasSubclass5 && !hasSubclass3) {
//                continue;
//            }
//            if (dat._attendSubclasses.contains(subClass)) {
//                continue; // 元科目は非表示
//            }
            
            final Record record = (Record) dat._record.get(subClass._code);

            // 教科
            final Class clazz = (Class) _param._classes.get(subClass._classkey);
            if (null != clazz) {
                final int keta = getMS932ByteLength(clazz._name);
                if (keta > 6) {
                    svf.VrsOut(pres + "CLASS3", clazz._name);
                    svf.VrsOut(pres + "CLASS2", clazz._code);
                    svf.VrAttribute(pres + "CLASS2", "X=10000");
                    svf.VrsOut(pres + "CLASS", clazz._code);
                    svf.VrAttribute(pres + "CLASS", "X=10000");
                    
                } else if (keta > 4) {
                    svf.VrsOut(pres + "CLASS3", clazz._code);
                    svf.VrAttribute(pres + "CLASS3", "X=10000");
                    svf.VrsOut(pres + "CLASS2", clazz._name);
                    svf.VrsOut(pres + "CLASS", clazz._code);
                    svf.VrAttribute(pres + "CLASS", "X=10000");
                    
                } else {
                    svf.VrsOut(pres + "CLASS3", clazz._code);
                    svf.VrAttribute(pres + "CLASS3", "X=10000");
                    svf.VrsOut(pres + "CLASS2", clazz._code);
                    svf.VrAttribute(pres + "CLASS2", "X=10000");
                    svf.VrsOut(pres + "CLASS", clazz._name);
                }
            }

            // 科目
            if (dat._combinedSubclasses.contains(subClass)) {
                svf.VrAttribute(pres + "SUBCLASS", "Paint=(1,90,2),Bold=1");
            }
            svf.VrsOut(pres + "SUBCLASS", subClass._name);

            if (null != record) {
                if (record.isUnderScore()) {
                    svf.VrsOut(pres + "SCORE", "(" + record.getScore() + ")");
                } else {
                    svf.VrsOut(pres + "SCORE", record.getScore());
                }
                if (!_param._rankNotPrint) {
                    svf.VrsOut(pres + "RANK", intnum(record._rank, ""));
                }
                svf.VrsOut(pres + "DEVIATION", sishaGonyuStr(record._deviation));

                final AverageDat avgDat = (AverageDat) exam._avgDat.get(avgKey(_param, student.regd(exam), subClass._code));
                if (null != avgDat) {
                    svf.VrsOut(pres + "AVERAGE",  sishaGonyuStr(avgDat._avg));
                    svf.VrsOut(pres + "MAX_SCORE", avgDat._highscore.toString());
                    svf.VrsOut(pres + "EXAMINEE", avgDat._count.toString());    // 受験者数
                }
            }

            svf.VrEndRecord();
            if (++i >= TABLE_SUBCLASS_MAX) {
                break;
            }
        }
        if (i == 0) {
            svf.VrsOut(pres + "CLASS", "\n");
            svf.VrEndRecord(); // データが無い時もこのコマンドを発行しないと印刷されないから
        }
    }

    private static String avgKey(final Param param, final Regd regd, final String subclasscd) {
        return avgKey(param, regd.hrKey(), regd.courseKey(), regd.majorKey(), regd.coursegroupKey(), subclasscd);
    }

    private static String avgKey(final Param param, final String hrKey, final String courseKey, final String majorKey, final String coursegroupKey, final String subclasscd) {
        final String key;
        if (param.isHr()) {
            key = subclasscd + hrKey;
        } else if (param.isCourse()) {
            key = subclasscd + courseKey;
        } else if (param.isMajor()) {
            key = subclasscd + majorKey;
        } else if (param.isCoursegroup()) {
            key = subclasscd + coursegroupKey;
        } else {
            key = subclasscd;
        }
        return key;
    }
    
    private void loadExam(final DB2UDB _db2, final List students, final Exam exam) throws SQLException {
        loadSubClasses(_db2, students, exam, _param); // 科目は指示画面指定のテストの科目
        loadAverageDat(_db2, exam, _param);
        loadRecord(_db2, students, exam, _param);
        loadAttendSubclass(_db2, students, exam, _param);
    }

    private static void loadSubClasses(final DB2UDB db2, final List students, final Exam exam, final Param param) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final String sql;
            sql = "select"
                    + "  distinct T1.PROFICIENCY_SUBCLASS_CD as SUBCLASSCD"
                    + " from"
                    + "  PROFICIENCY_DAT T1"
                    + " where"
                    + "  T1.year='" + exam._year + "' AND"
                    + "  T1.semester='" + exam._semester + "' AND"
                    + "  T1.proficiencydiv='" + exam._proficiencydiv + "' AND"
                    + "  T1.proficiencycd='" + exam._proficiencycd + "' AND"
                    + "  T1.SCHREGNO = ? "
                    ;
            //log.debug("模試データにある科目のSQL=" + sql);

            ps = db2.prepareStatement(sql);
            for (final Iterator it = students.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                ps.setString(1, student._schregno);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String subClasscd = rs.getString("SUBCLASSCD");

                    final SubClass subClass = (SubClass) param._subClasses.get(subClasscd);
                    if (null != subClass) {
                        student.data(exam)._subclasses.put(subClasscd, subClass);
                    }
                }
            }
        } catch (final SQLException e) {
            log.fatal("模試データにある科目の取得でエラー");
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
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
            final Exam exam,
            final Param param
    ) throws SQLException {
        final String AVG_DIV_GRADE = "01";
        final String AVG_DIV_HR = "02";
        final String AVG_DIV_COURSE = "03";
        final String AVG_DIV_MAJOR = "04";
        final String AVG_DIV_COURSEGROUP = "05";

        final String selectFrom;
        selectFrom = "SELECT"
            + "  proficiency_subclass_cd as subclasscd,"
            + "  avg,"
            + "  stddev,"
            + "  highscore,"
            + "  lowscore,"
            + "  avg_kansan as graphAvg,"
            + "  count,"
            + "  hr_class,"
            + "  coursecd,"
            + "  majorcd,"
            + "  coursecode"
            + " FROM"
            + "  proficiency_average_dat"
            ;
        final String where;
        if (param.isHr()) {
            where = " WHERE"
                + "    year='" + exam._year + "' AND"
                + "    semester='" + exam._semester + "' AND"
                + "    proficiencydiv='" + exam._proficiencydiv + "' AND"
                + "    proficiencycd='" + exam._proficiencycd + "' AND"
                + "    data_div = '" + param._avgDataDiv + "' AND"
                + "    avg_div='" + AVG_DIV_HR + "' AND"
                + "    grade='" + exam._grade + "'"
                ;
        } else if (param.isCourse()) {
            where = " WHERE"
                + "    year='" + exam._year + "' AND"
                + "    semester='" + exam._semester + "' AND"
                + "    proficiencydiv='" + exam._proficiencydiv + "' AND"
                + "    proficiencycd='" + exam._proficiencycd + "' AND"
                + "    data_div = '" + param._avgDataDiv + "' AND"
                + "    avg_div='" + AVG_DIV_COURSE + "' AND"
                + "    grade='" + exam._grade + "' AND"
                + "    hr_class='000'"
                ;
        } else if (param.isMajor()) {
            where = " WHERE"
                + "    year='" + exam._year + "' AND"
                + "    semester='" + exam._semester + "' AND"
                + "    proficiencydiv='" + exam._proficiencydiv + "' AND"
                + "    proficiencycd='" + exam._proficiencycd + "' AND"
                + "    data_div = '" + param._avgDataDiv + "' AND"
                + "    avg_div='" + AVG_DIV_MAJOR + "' AND"
                + "    grade='" + exam._grade + "' AND"
                + "    hr_class='000'"
                ;
        } else if (param.isCoursegroup()) {
            where = " WHERE"
                + "    year='" + exam._year + "' AND"
                + "    semester='" + exam._semester + "' AND"
                + "    proficiencydiv='" + exam._proficiencydiv + "' AND"
                + "    proficiencycd='" + exam._proficiencycd + "' AND"
                + "    data_div = '" + param._avgDataDiv + "' AND"
                + "    avg_div='" + AVG_DIV_COURSEGROUP + "' AND"
                + "    grade='" + exam._grade + "' AND"
                + "    hr_class='000'"
                ;
        } else {
            where = " WHERE"
                    + "    year='" + exam._year + "' AND"
                    + "    semester='" + exam._semester + "' AND"
                    + "    proficiencydiv='" + exam._proficiencydiv + "' AND"
                    + "    proficiencycd='" + exam._proficiencycd + "' AND"
                    + "    data_div = '" + param._avgDataDiv + "' AND"
                    + "    avg_div='" + AVG_DIV_GRADE + "' AND"
                    + "    grade='" + exam._grade + "' AND"
                    + "    hr_class='000' AND"
                    + "    coursecd='0' AND"
                    + "    majorcd='000' AND"
                    + "    coursecode='0000'"
                    ;
        }
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = selectFrom + where;
//            log.debug(" avg1 sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String subclasscd = rs.getString("subclasscd");
                final SubClass subClass = (SubClass) param._subClasses.get(subclasscd);
                if (null == subClass) {
                    //log.warn("模試成績平均データが模試科目マスタに無い!:" + subclasscd);
                }
                final BigDecimal avg = rs.getBigDecimal("avg");
                final BigDecimal stddev = rs.getBigDecimal("stddev");
                final BigDecimal graphAvg = rs.getBigDecimal("graphAvg");
                final Integer count = KNJServletUtils.getInteger(rs, "count");
                final Integer highscore = KNJServletUtils.getInteger(rs, "highscore");
                final Integer lowscore = KNJServletUtils.getInteger(rs, "lowscore");
                final String hrClass = rs.getString("HR_CLASS");
                final String coursecd = rs.getString("coursecd");
                final String majorcd = rs.getString("majorcd");
                final String coursecode = rs.getString("coursecode");

                final String key = avgKey(param, hrClass, coursecd + majorcd + coursecode, coursecd + majorcd + "0000", "0" + majorcd + "0000", subclasscd);
                if (param.ALL3.equals(subclasscd) || param.ALL5.equals(subclasscd) || param.ALL9.equals(subclasscd)) {
                    final SubClass subClass0 = new SubClass(subclasscd);
                    final AverageDat avgDat = new AverageDat(subClass0, avg, graphAvg, stddev, highscore, lowscore, count, coursecd, majorcd, coursecode);
                    exam._avgDat.put(key, avgDat);
                } else {
                    final AverageDat avgDat = new AverageDat(subClass, avg, graphAvg, stddev, highscore, lowscore, count, coursecd, majorcd, coursecode);
                    exam._avgDat.put(key, avgDat);
                }
            }
        } catch (final SQLException e) {
            log.warn("模試成績平均データの取得でエラー", e);
            throw e;
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        log.debug("模試コード=" + exam._proficiencydiv + " : " + exam._proficiencycd + " の模試成績平均データの件数=" + exam._avgDat.size());
    }

    private static void loadAttendSubclass(
            final DB2UDB db2,
            final List students,
            final Exam exam,
            final Param param
    ) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            for (final Iterator it = students.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                final Regd regd = student.regd(exam);
                if (null == regd._grade) {
                    continue;
                }
                final StringBuffer sql = new StringBuffer();
                /* 通常の成績 */
                sql.append(" SELECT ");
                sql.append("     T1.DIV, ");
                sql.append("     T1.COMBINED_SUBCLASSCD, ");
                sql.append("     T1.ATTEND_SUBCLASSCD ");
                sql.append(" FROM ");
                sql.append("     PROFICIENCY_SUBCLASS_REPLACE_COMB_DAT T1 ");
                sql.append(" WHERE ");
                sql.append("     T1.YEAR = '" + exam._year + "' ");
                sql.append("     AND T1.SEMESTER = '" + exam._semester + "' ");
                sql.append("     AND T1.PROFICIENCYDIV = '" + exam._proficiencydiv + "' ");
                sql.append("     AND T1.PROFICIENCYCD = '" + exam._proficiencycd + "' ");
                sql.append("     AND T1.GRADE = '" + regd._grade + "' ");
                sql.append("     AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE = ");
                sql.append("         CASE WHEN T1.DIV = '04' THEN '" + regd.coursegroupKey() + "' ");
                sql.append("         ELSE '" + regd.courseKey() + "' END ");
                sql.append(" ORDER BY ");
                sql.append("     T1.DIV, ");
                sql.append("     T1.COMBINED_SUBCLASSCD, ");
                sql.append("     T1.ATTEND_SUBCLASSCD ");

                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
//                    final String attendSubclasscd = rs.getString("ATTEND_SUBCLASSCD");
//                    SubClass subClass = (SubClass) param._subClasses.get(attendSubclasscd);
//                    if (null == subClass) {
//                        log.warn("合併科目のデータが模試科目マスタに無い!:" + attendSubclasscd);
//                        final Class clazz = (Class) param._classes.get(attendSubclasscd.substring(0, 2));
//                        if (null == clazz) {
//                            continue;
//                        }
//                        subClass = new SubClass(attendSubclasscd, clazz._name, clazz._abbv);
//                    }
//                    student.data(exam)._attendSubclasses.add(subClass);
                    
                    final String combinedSubclasscd = rs.getString("COMBINED_SUBCLASSCD");
                    SubClass subClass = (SubClass) param._subClasses.get(combinedSubclasscd);
                    if (null == subClass) {
                        log.warn("合併科目のデータが模試科目マスタに無い!:" + combinedSubclasscd);
                        continue;
                    }
                    student.data(exam)._combinedSubclasses.add(subClass);

                }
            }
        } catch (final SQLException e) {
            log.warn("合併科目のデータの取得でエラー", e);
        } finally {
            DbUtils.closeQuietly(rs);
        }
    }

    private static void loadRecord(
            final DB2UDB db2,
            final List students,
            final Exam exam,
            final Param param
    ) {
        final String RANK_DIV_GRADE = "01";
        final String RANK_DIV_HR = "02";
        final String RANK_DIV_COURSE = "03";
        final String RANK_DIV_MAJOR = "04";
        final String RANK_DIV_COURSEGROUP = "05";

        String sql;
        /* 通常の成績 */
        sql = "SELECT"
            + "  T1.proficiency_subclass_cd as subclasscd,"
            + "  T3.score,"
            + "  T1.score_di,"
            + "  T2.score as graphScore,"
            + "  T3.rank as grade_rank,"
            + "  T3.deviation as grade_deviation,"
            + "  T4.rank as course_rank,"
            + "  T4.deviation as course_deviation,"
            + "  T6.rank as coursegroup_rank,"
            + "  T6.deviation as coursegroup_deviation,"
            + "  T7.rank as hr_rank,"
            + "  T7.deviation as hr_deviation,"
            + "  T8.rank as major_rank,"
            + "  T8.deviation as major_deviation,"
            + "  T5.PASS_SCORE"
            + " FROM proficiency_dat T1 "
            + " LEFT JOIN proficiency_rank_dat T3 ON "
            + "     T3.year = T1.year AND "
            + "     T3.semester=T1.semester AND"
            + "     T3.proficiencydiv=T1.proficiencydiv AND"
            + "     T3.proficiencycd=T1.proficiencycd AND"
            + "     T3.proficiency_subclass_cd=T1.proficiency_subclass_cd AND"
            + "     T3.schregno = T1.schregno AND"
            + "     T3.rank_data_div ='" + param._rankDataDiv + "' AND"
            + "     T3.rank_div = '" + RANK_DIV_GRADE + "' "
            + " LEFT JOIN proficiency_rank_dat T2 ON "
            + "     T1.year = T2.year AND "
            + "     T1.semester=T2.semester AND"
            + "     T1.proficiencydiv=T2.proficiencydiv AND"
            + "     T1.proficiencycd=T2.proficiencycd AND"
            + "     T1.proficiency_subclass_cd=T2.proficiency_subclass_cd AND"
            + "     T1.schregno = T2.schregno AND "
            + "     T2.rank_data_div = '03' AND "
            + "     T2.rank_div = '" + RANK_DIV_GRADE + "' "
            + " LEFT JOIN proficiency_rank_dat T4 ON "
            + "     T4.year = T1.year AND "
            + "     T4.semester=T1.semester AND"
            + "     T4.proficiencydiv=T1.proficiencydiv AND"
            + "     T4.proficiencycd=T1.proficiencycd AND"
            + "     T4.proficiency_subclass_cd=T1.proficiency_subclass_cd AND"
            + "     T4.schregno = T1.schregno AND"
            + "     T4.rank_data_div =t3.rank_data_div AND"
            + "     T4.rank_div = '" + RANK_DIV_COURSE + "' "
            + " LEFT JOIN proficiency_rank_dat T6 ON "
            + "     T6.year = T1.year AND "
            + "     T6.semester=T1.semester AND"
            + "     T6.proficiencydiv=T1.proficiencydiv AND"
            + "     T6.proficiencycd=T1.proficiencycd AND"
            + "     T6.proficiency_subclass_cd=T1.proficiency_subclass_cd AND"
            + "     T6.schregno = T1.schregno AND"
            + "     T6.rank_data_div =t3.rank_data_div AND"
            + "     T6.rank_div = '" + RANK_DIV_COURSEGROUP + "' "
            + " LEFT JOIN PROFICIENCY_PERFECT_COURSE_DAT T5 ON T5.YEAR = T1.YEAR AND "
            + "     T5.semester=T2.semester AND"
            + "     T5.proficiencydiv=T2.proficiencydiv AND"
            + "     T5.proficiencycd=T2.proficiencycd AND"
            + "     T5.proficiency_subclass_cd=T2.proficiency_subclass_cd "
            + "     AND T5.GRADE = CASE WHEN T5.DIV = '01' THEN '00' ELSE ? END  "
            + "     AND T5.COURSECD || T5.MAJORCD || T5.COURSECODE = "
            + "       CASE WHEN T5.DIV = '01' OR T5.DIV = '02' THEN '00000000' ELSE ? END "
            + " LEFT JOIN proficiency_rank_dat T7 ON "
            + "     T7.year = T1.year AND "
            + "     T7.semester=T1.semester AND"
            + "     T7.proficiencydiv=T1.proficiencydiv AND"
            + "     T7.proficiencycd=T1.proficiencycd AND"
            + "     T7.proficiency_subclass_cd=T1.proficiency_subclass_cd AND"
            + "     T7.schregno = T1.schregno AND"
            + "     T7.rank_data_div =t3.rank_data_div AND"
            + "     T7.rank_div = '" + RANK_DIV_HR + "' "
            + " LEFT JOIN proficiency_rank_dat T8 ON "
            + "     T8.year = T1.year AND "
            + "     T8.semester=T1.semester AND"
            + "     T8.proficiencydiv=T1.proficiencydiv AND"
            + "     T8.proficiencycd=T1.proficiencycd AND"
            + "     T8.proficiency_subclass_cd=T1.proficiency_subclass_cd AND"
            + "     T8.schregno = T1.schregno AND"
            + "     T8.rank_data_div =t3.rank_data_div AND"
            + "     T8.rank_div = '" + RANK_DIV_MAJOR + "' "
            + " WHERE"
            + "  T1.year='" + exam._year + "' AND"
            + "  T1.semester='" + exam._semester + "' AND"
            + "  T1.proficiencydiv='" + exam._proficiencydiv + "' AND"
            + "  T1.proficiencycd='" + exam._proficiencycd + "' AND"
            + "  T1.proficiency_subclass_cd NOT IN ('" + param.ALL3 + "', '" + param.ALL5 + "', '" + param.ALL9 + "') AND"
            + "  T1.schregno=?"
            ;
        
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            //log.debug(" sql = " + sql);
            ps = db2.prepareStatement(sql);
            for (final Iterator it = students.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                final Regd regd = student.regd(exam);
                int i = 0;
                if (null == regd._grade) {
                    continue;
                }
                //欠点（満点マスタの合格点）
                ps.setString(++i, regd._grade);
                ps.setString(++i, regd.courseKey());
                ps.setString(++i, student._schregno);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String subclasscd = rs.getString("subclasscd");
                    SubClass subClass = (SubClass) param._subClasses.get(subclasscd);
                    if (null == subClass) {
                        log.warn("対象成績データが模試科目マスタに無い!:" + subclasscd);
                        continue;
                    }
                    final Integer score = KNJServletUtils.getInteger(rs, "score");
                    final String scoreDi = rs.getString("score_di");
                    if (score == null && StringUtils.isEmpty(scoreDi)) {
                        continue;
                    }
                    
                    final Integer graphScore = KNJServletUtils.getInteger(rs, "graphScore");
                    final Integer rank;
                    final BigDecimal deviation;
                    if (param.isHr()) {
                        rank = KNJServletUtils.getInteger(rs, "hr_rank");
                        deviation = rs.getBigDecimal("hr_deviation");
                    } else if (param.isCourse()) {
                        rank = KNJServletUtils.getInteger(rs, "course_rank");
                        deviation = rs.getBigDecimal("course_deviation");
                    } else if (param.isMajor()) {
                        rank = KNJServletUtils.getInteger(rs, "major_rank");
                        deviation = rs.getBigDecimal("major_deviation");
                    } else if (param.isCoursegroup()) {
                        rank = KNJServletUtils.getInteger(rs, "coursegroup_rank");
                        deviation = rs.getBigDecimal("coursegroup_deviation");
                    } else {
                        rank = KNJServletUtils.getInteger(rs, "grade_rank");
                        deviation = rs.getBigDecimal("grade_deviation");
                    }
                    
                    final Integer passScore = KNJServletUtils.getInteger(rs, "PASS_SCORE");

                    final Record rec = new Record(subClass, score, scoreDi, graphScore, null, rank, deviation, passScore);
                    student.data(exam)._record.put(subClass._code, rec);
                }
            }
        } catch (final SQLException e) {
            log.warn("成績データの取得でエラー", e);
        } finally {
            DbUtils.closeQuietly(rs);
        }

        final String sql1;
        sql1 = "SELECT"
            + "  t1.proficiency_subclass_cd as subclasscd,"
            + "  t1.score,"
            + "  t2.avg,"
            + "  t1.rank as grade_rank,"
            + "  t1.deviation as grade_deviation,"
            + "  t3.rank as course_rank,"
            + "  t3.deviation as course_deviation,"
            + "  t4.rank as coursegroup_rank,"
            + "  t4.deviation as coursegroup_deviation,"
            + "  t5.rank as major_rank,"
            + "  t5.deviation as major_deviation,"
            + "  t6.rank as hr_rank,"
            + "  t6.deviation as hr_deviation"
            + " FROM proficiency_rank_dat T1 "
            + " LEFT JOIN proficiency_rank_dat T2 ON "
            + "     T1.year = T2.year AND "
            + "     T1.semester=T2.semester AND"
            + "     T1.proficiencydiv=T2.proficiencydiv AND"
            + "     T1.proficiencycd=T2.proficiencycd AND"
            + "     T1.proficiency_subclass_cd=T2.proficiency_subclass_cd AND"
            + "     T1.schregno = T2.schregno AND "
            + "     T2.rank_data_div = '04' AND "
            + "     T2.rank_div = T1.rank_div "
            + " LEFT JOIN proficiency_rank_dat T3 ON "
            + "     T1.year = T3.year AND "
            + "     T1.semester=T3.semester AND"
            + "     T1.proficiencydiv=T3.proficiencydiv AND"
            + "     T1.proficiencycd=T3.proficiencycd AND"
            + "     T1.proficiency_subclass_cd=T3.proficiency_subclass_cd AND"
            + "     T1.schregno = T3.schregno AND "
            + "     T1.rank_data_div = T3.rank_data_div AND "
            + "     T3.rank_div = '" + RANK_DIV_COURSE + "' "
            + " LEFT JOIN proficiency_rank_dat T4 ON "
            + "     T1.year = T4.year AND "
            + "     T1.semester=T4.semester AND"
            + "     T1.proficiencydiv=T4.proficiencydiv AND"
            + "     T1.proficiencycd=T4.proficiencycd AND"
            + "     T1.proficiency_subclass_cd=T4.proficiency_subclass_cd AND"
            + "     T1.schregno = T4.schregno AND "
            + "     T1.rank_data_div = T4.rank_data_div AND "
            + "     T4.rank_div = '" + RANK_DIV_COURSEGROUP + "' "
            + " LEFT JOIN proficiency_rank_dat T5 ON "
            + "     T1.year = T5.year AND "
            + "     T1.semester=T5.semester AND"
            + "     T1.proficiencydiv=T5.proficiencydiv AND"
            + "     T1.proficiencycd=T5.proficiencycd AND"
            + "     T1.proficiency_subclass_cd=T5.proficiency_subclass_cd AND"
            + "     T1.schregno = T5.schregno AND "
            + "     T1.rank_data_div = T5.rank_data_div AND "
            + "     T5.rank_div = '" + RANK_DIV_MAJOR + "' "
            + " LEFT JOIN proficiency_rank_dat T6 ON "
            + "     T1.year = T6.year AND "
            + "     T1.semester=T6.semester AND"
            + "     T1.proficiencydiv=T6.proficiencydiv AND"
            + "     T1.proficiencycd=T6.proficiencycd AND"
            + "     T1.proficiency_subclass_cd=T6.proficiency_subclass_cd AND"
            + "     T1.schregno = T6.schregno AND "
            + "     T1.rank_data_div = T6.rank_data_div AND "
            + "     T6.rank_div = '" + RANK_DIV_HR + "' "
            + " WHERE"
            + "  T1.year='" + exam._year + "' AND"
            + "  T1.semester='" + exam._semester + "' AND"
            + "  T1.proficiencydiv='" + exam._proficiencydiv + "' AND"
            + "  T1.proficiencycd='" + exam._proficiencycd + "' AND"
            + "  T1.proficiency_subclass_cd IN ('" + param.ALL3 + "', '" + param.ALL5 + "', '" + param.ALL9 + "') AND"
            + "  t1.rank_data_div ='" + param._rankDataDiv + "' AND"
            + "  t1.rank_div ='" + RANK_DIV_GRADE + "' AND"
            + "  t1.schregno=?"
            ;

        try {
            ps = db2.prepareStatement(sql1);
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
                    if (param.isHr()) {
                        rank = KNJServletUtils.getInteger(rs, "hr_rank");
                        deviation = rs.getBigDecimal("hr_deviation");
                    } else if (param.isCourse()) {
                        rank = KNJServletUtils.getInteger(rs, "course_rank");
                        deviation = rs.getBigDecimal("course_deviation");
                    } else if (param.isMajor()) {
                        rank = KNJServletUtils.getInteger(rs, "major_rank");
                        deviation = rs.getBigDecimal("major_deviation");
                    } else if (param.isCoursegroup()) {
                        rank = KNJServletUtils.getInteger(rs, "coursegroup_rank");
                        deviation = rs.getBigDecimal("coursegroup_deviation");
                    } else {
                        rank = KNJServletUtils.getInteger(rs, "grade_rank");
                        deviation = rs.getBigDecimal("grade_deviation");
                    }

                    final SubClass subClass = new SubClass(subclasscd);
                    final Record rec = new Record(subClass, score, null, null, avg, rank, deviation, null);
                    student.data(exam)._record.put(subClass._code, rec);
                }
            }
        } catch (final SQLException e) {
            log.warn("成績データの取得でエラー", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        
        exam._distribution = new HashMap();
        
        final StringBuffer stb = new StringBuffer();
        stb.append("WITH schregnos AS (");
        stb.append("SELECT");
        stb.append("  t1.schregno, ");
        stb.append("  t1.grade,");
        stb.append("  t1.hr_class,");
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
        stb.append("  and t1.semester = '" + param._semester + "' ");
        stb.append("  and t1.grade = '" + exam._grade + "'");
        stb.append(") ");
        stb.append("SELECT");
        stb.append("  T1.proficiency_subclass_cd as subclasscd,");
        stb.append("  T3.score,");
        stb.append("  T2.score as graphScore, ");
        stb.append("  T5.perfect, ");
        stb.append("  T0.grade, ");
        stb.append("  T0.hr_class, ");
        stb.append("  T0.coursecd, ");
        stb.append("  T0.majorcd, ");
        stb.append("  T0.coursecode, ");
        stb.append("  T0.coursegroupCd ");
        stb.append(" FROM proficiency_dat T1 ");
        stb.append(" INNER JOIN schregnos T0 ON T0.schregno = T1.schregno ");
        stb.append(" LEFT JOIN proficiency_rank_dat T3 ON ");
        stb.append("     T3.year = T1.year AND ");
        stb.append("     T3.semester=T1.semester AND");
        stb.append("     T3.proficiencydiv=T1.proficiencydiv AND");
        stb.append("     T3.proficiencycd=T1.proficiencycd AND");
        stb.append("     T3.proficiency_subclass_cd=T1.proficiency_subclass_cd AND");
        stb.append("     T3.schregno = T1.schregno AND");
        stb.append("     T3.rank_data_div ='" + param._rankDataDiv + "' AND");
        stb.append("     T3.rank_div = '" + RANK_DIV_GRADE + "' ");
        stb.append(" LEFT JOIN proficiency_rank_dat T2 ON ");
        stb.append("     T1.year = T2.year AND ");
        stb.append("     T1.semester=T2.semester AND");
        stb.append("     T1.proficiencydiv=T2.proficiencydiv AND");
        stb.append("     T1.proficiencycd=T2.proficiencycd AND");
        stb.append("     T1.proficiency_subclass_cd=T2.proficiency_subclass_cd AND");
        stb.append("     T1.schregno = T2.schregno AND ");
        stb.append("     T2.rank_data_div = '03' AND ");
        stb.append("     T2.rank_div = '" + RANK_DIV_GRADE + "' ");
        stb.append(" LEFT JOIN PROFICIENCY_PERFECT_COURSE_DAT T5 ON T5.YEAR = T1.YEAR AND ");
        stb.append("     T5.semester=T2.semester AND");
        stb.append("     T5.proficiencydiv=T2.proficiencydiv AND");
        stb.append("     T5.proficiencycd=T2.proficiencycd AND");
        stb.append("     T5.proficiency_subclass_cd=T2.proficiency_subclass_cd ");
        stb.append("     AND T5.GRADE = CASE WHEN T5.DIV = '01' THEN '00' ELSE T0.grade END  ");
        stb.append("     AND T5.COURSECD || T5.MAJORCD || T5.COURSECODE = ");
        stb.append("       CASE WHEN T5.DIV = '01' OR T5.DIV = '02' THEN '00000000' ELSE T0.COURSECD || T0.MAJORCD || T0.COURSECODE END ");
        stb.append(" WHERE");
        stb.append("  T1.year='" + exam._year + "' AND");
        stb.append("  T1.semester='" + exam._semester + "' AND");
        stb.append("  T1.proficiencydiv='" + exam._proficiencydiv + "' AND");
        stb.append("  T1.proficiencycd='" + exam._proficiencycd + "' AND");
        stb.append("  T1.proficiency_subclass_cd NOT IN ('" + param.ALL3 + "', '" + param.ALL5 + "', '" + param.ALL9 + "') ");
        stb.append(" UNION ALL ");
        stb.append("SELECT");
        stb.append("  t1.proficiency_subclass_cd as subclasscd,");
        stb.append("  t2.avg as score,");
        stb.append("  cast(null as smallint) as graphScore,");
        stb.append("  100 as perfect, ");
        stb.append("  T0.grade, ");
        stb.append("  T0.hr_class, ");
        stb.append("  T0.coursecd, ");
        stb.append("  T0.majorcd, ");
        stb.append("  T0.coursecode, ");
        stb.append("  T0.coursegroupCd ");
        stb.append(" FROM proficiency_rank_dat T1 ");
        stb.append(" INNER JOIN schregnos T0 ON T0.schregno = T1.schregno ");
        stb.append(" LEFT JOIN proficiency_rank_dat T2 ON ");
        stb.append("     T1.year = T2.year AND ");
        stb.append("     T1.semester=T2.semester AND");
        stb.append("     T1.proficiencydiv=T2.proficiencydiv AND");
        stb.append("     T1.proficiencycd=T2.proficiencycd AND");
        stb.append("     T1.proficiency_subclass_cd=T2.proficiency_subclass_cd AND");
        stb.append("     T1.schregno = T2.schregno AND ");
        stb.append("     T2.rank_data_div = '04' AND ");
        stb.append("     T2.rank_div = T1.rank_div ");
        stb.append(" LEFT JOIN ( ");
        stb.append(" SELECT DISTINCT T1.SCHREGNO, REPEAT(t2.GROUP_DIV, 6) AS PROFICIENCY_SUBCLASS_CD ");
        stb.append(" FROM proficiency_dat T1 ");
        stb.append(" INNER JOIN schregnos T0 ON T0.SCHREGNO =T1.SCHREGNO ");
        stb.append(" INNER JOIN proficiency_subclass_group_dat t2 on t2.year = t1.year and t2.semester = t1.semester ");
        stb.append("     and t2.proficiencydiv = t1.proficiencydiv and t2.proficiencycd = t1.proficiencycd ");
        stb.append("     and t2.grade = T0.grade and t2.coursecd = T0.coursecd and t2.majorcd = T0.majorcd and t2.coursecode = T0.coursecode ");
        stb.append("     and t2.proficiency_subclass_cd = t1.proficiency_subclass_cd ");
        stb.append(" WHERE T1.YEAR = '" + exam._year + "' AND ");
        stb.append("  T1.semester='" + exam._semester + "' AND");
        stb.append("  T1.proficiencydiv='" + exam._proficiencydiv + "' AND");
        stb.append("  T1.proficiencycd='" + exam._proficiencycd + "' AND");
        stb.append("  T1.SCORE_DI = '*' ");
        stb.append(" ) T_ASTER ON T_ASTER.SCHREGNO = T1.SCHREGNO AND T_ASTER.PROFICIENCY_SUBCLASS_CD = T1.PROFICIENCY_SUBCLASS_CD ");
        stb.append(" WHERE");
        stb.append("  T1.year='" + exam._year + "' AND");
        stb.append("  T1.semester='" + exam._semester + "' AND");
        stb.append("  T1.proficiencydiv='" + exam._proficiencydiv + "' AND");
        stb.append("  T1.proficiencycd='" + exam._proficiencycd + "' AND");
        stb.append("  T1.proficiency_subclass_cd IN ('" + param.ALL3 + "', '" + param.ALL5 + "', '" + param.ALL9 + "') AND");
        stb.append("  t1.rank_data_div ='" + param._rankDataDiv + "' AND");
        stb.append("  t1.rank_div ='" + RANK_DIV_GRADE + "' ");
        stb.append("  AND T_ASTER.SCHREGNO IS NULL ");
        
        try {
            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();
            while (rs.next()) {
                final BigDecimal score = rs.getBigDecimal("score");
                if (null == score) {
                    continue;
                }
                
                final String rtnKey = avgKey(param, rs.getString("hr_class"), rs.getString("coursecd") + rs.getString("majorcd") + rs.getString("coursecode"), rs.getString("coursecd") + rs.getString("majorcd") + "0000", "0" + rs.getString("coursegroupCd") + "0000", rs.getString("subclasscd"));
                
                final List scoreList = getMappedList(exam._distribution, rtnKey);

                Number scorePercent;
                final int perfect = rs.getInt("PERFECT");
                if (100 == perfect || 0 == perfect) {
                    scorePercent = score;
                } else {
                    scorePercent = score.multiply(new BigDecimal(100)).divide(new BigDecimal(perfect), 1, BigDecimal.ROUND_HALF_UP);
                }
                scoreList.add(scorePercent);
            }

        } catch (final Exception e) {
            log.error("成績データの取得でエラー", e);
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
    
    private static List createStudents(final DB2UDB db2, final Param param) throws SQLException {
        final String[] schregnos = param.getScregnos(db2);
        final List rtn = new LinkedList();
        final String sql;
        sql = "SELECT"
            + "  t1.schregno,"
            + "  t1.grade,"
            + "  t1.hr_class,"
            + "  t1.attendno,"
            + "  t2.hr_name,"
            + "  t3.staffname,"
            + "  t1.coursecd,"
            + "  t1.majorcd,"
            + "  t1.coursecode,"
            + "  l2.group_cd as coursegroupcd,"
            + "  t0.name"
            + " FROM"
            + "  schreg_regd_dat t1 "
            + "  inner join schreg_base_mst t0 on t0.schregno = t1.schregno "
            + "  left join schreg_regd_hdat t2 on t2.year = t1.year "
            + "      and t2.semester = t1.semester "
            + "      and t2.grade = t1.grade "
            + "      and t2.hr_class = t1.hr_class "
            + "  left join staff_mst t3 on t3.staffcd = t2.tr_cd1 "
            + "  left join course_group_cd_dat l1 on l1.year = t1.year "
            + "      and l1.grade = t1.grade "
            + "      and l1.coursecd = t1.coursecd "
            + "      and l1.majorcd = t1.majorcd "
            + "      and l1.coursecode = t1.coursecode "
            + "  left join course_group_cd_hdat l2 on l2.year = l1.year "
            + "      and l2.grade = l1.grade "
            + "      and l2.group_cd = l1.group_cd "
            + " WHERE"
            + "  t1.year=? AND"
            + "  t1.semester=? AND"
            + "  t1.schregno IN " + SQLUtils.whereIn(true, schregnos)
            + " ORDER BY "
            + "  t1.grade,"
            + "  t1.hr_class,"
            + "  t1.attendno "
            ;

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            ps.setString(1, param._year);
            ps.setString(2, param._semester);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String schregno = rs.getString("schregno");
                final String grade = rs.getString("grade");
                final String hrclass = rs.getString("hr_class");
                final String attendno = rs.getString("attendno");
                final String hrName = rs.getString("hr_name");
                final String staffname = rs.getString("staffname");
                final String coursecd = rs.getString("coursecd");
                final String majorcd = rs.getString("majorcd");
                final String coursecode = rs.getString("coursecode");
                final String coursegroupcd = rs.getString("coursegroupcd");
                final String name = rs.getString("name");
                
                final Regd regd = new Regd(grade, hrclass, attendno, hrName, staffname, coursecd, majorcd, coursecode, coursegroupcd);

                final Student student = new Student(
                        schregno,
                        name
                );
                student._regd.put(param._thisExam.code(), regd);
                rtn.add(student);
            }
        } catch (final SQLException e) {
            log.error("生徒の基本情報取得でエラー");
            throw e;
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }

        try {
            ps = db2.prepareStatement(sql);
            ps.setString(1, param._beforeExam._year);
            ps.setString(2, param._beforeExam._semester);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String schregno = rs.getString("schregno");
                Student student = null;
                for (final Iterator it = rtn.iterator(); it.hasNext();) {
                    final Student s = (Student) it.next();
                    if (schregno.equals(s._schregno)) {
                        student = s;
                        break;
                    }
                }
                if (null == student) {
                    continue;
                }

                final String grade = rs.getString("grade");
                final String hrclass = rs.getString("hr_class");
                final String attendno = rs.getString("attendno");
                final String hrName = rs.getString("hr_name");
                final String staffname = rs.getString("staffname");
                final String coursecd = rs.getString("coursecd");
                final String majorcd = rs.getString("majorcd");
                final String coursecode = rs.getString("coursecode");
                final String coursegroupcd = rs.getString("coursegroupcd");
                
                final Regd beforeRegd = new Regd(grade, hrclass, attendno, hrName, staffname, coursecd, majorcd, coursecode, coursegroupcd);
                
                student._regd.put(param._beforeExam.code(), beforeRegd);
            }
        } catch (final SQLException e) {
            log.error("生徒の前回の基本情報取得でエラー", e);
            throw e;
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
        return rtn;
    }
    
    private class Chart {
        /** グラフイメージファイルの Set&lt;File&gt; */
        private final Set _graphFiles = new HashSet();

//        private JFreeChart createBarChart(
//                final DefaultCategoryDataset scoreDataset,
//                final DefaultCategoryDataset avgDataset
//        ) {
//            final JFreeChart chart = ChartFactory.createBarChart(null, null, null, scoreDataset, PlotOrientation.VERTICAL, false, false, false);
//            final CategoryPlot plot = chart.getCategoryPlot();
//            plot.setRangeGridlineStroke(new BasicStroke(2.0f)); // 目盛りの太さ
//            plot.getDomainAxis().setTickLabelsVisible(true);
//            plot.setRangeGridlinePaint(Color.black);
//            plot.setRangeGridlineStroke(new BasicStroke(1.0f));
//
//            // 追加する折れ線グラフの表示設定
//            final LineAndShapeRenderer renderer2 = new LineAndShapeRenderer();
//            renderer2.setItemLabelsVisible(true);
//            renderer2.setPaint(Color.gray);
//            plot.setDataset(1, avgDataset);
//            plot.setRenderer(1, renderer2);
//            plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
//
//            final NumberAxis numberAxis = new NumberAxis();
//            numberAxis.setTickUnit(new NumberTickUnit(10));
//            numberAxis.setTickLabelsVisible(true);
//            numberAxis.setRange(0, 100.0);
//            plot.setRangeAxis(numberAxis);
//
//            final CategoryItemRenderer renderer = plot.getRenderer();
//            renderer.setSeriesPaint(0, Color.darkGray);
//            final Font itemLabelFont = new Font("TimesRoman", Font.PLAIN, 30);
//            renderer.setItemLabelFont(itemLabelFont);
//            renderer.setItemLabelsVisible(true);
//
//            ((BarRenderer) renderer).setMaximumBarWidth(0.05);
//
//            chart.setBackgroundPaint(Color.white);
//
//            return chart;
//        }
        
//        private void printBarGraph(final Vrw32alp svf, final Student student, final Exam exam) {
//            /** 棒グラフの最大科目数. */
//            final int BAR_GRAPH_MAX_ITEM = 11;
//            
//            // グラフ用のデータ作成
//            final DefaultCategoryDataset scoreDataset = new DefaultCategoryDataset();
//            final DefaultCategoryDataset avgDataset = new DefaultCategoryDataset();
//            int i = 0;
//            final ExamData dat = student.data(exam);
//            for (final Iterator it = dat._record.values().iterator(); it.hasNext();) {
//                final Record record = (Record) it.next();
//                if (dat._attendSubclasses.contains(record._subClass)) {
//                    continue; // 元科目は非表示
//                }
//                scoreDataset.addValue(record._graphScore, "本人得点", record._subClass._abbv);
//                final AverageDat avgDat = (AverageDat) exam._averageDat.get(avgKey(student, exam, record._subClass._code));
//                final BigDecimal graphAvg = (null == avgDat) ? null : avgDat._graphAvg;
//
//                avgDataset.addValue(graphAvg, _param.formGroupDivName() + "平均点", record._subClass._abbv);
//
//                log.info("棒グラフ⇒" + record._subClass + ":素点=" + record._score + "(" + record._graphScore + ")" + ((avgDat == null) ? "" : ", 平均=" + avgDat._avg + "(" + avgDat._graphAvg + ")"));
//                if (i++ > BAR_GRAPH_MAX_ITEM) {
//                    break;
//                }
//            }
//
//            try {
//                // チャート作成
//                final JFreeChart chart = createBarChart(scoreDataset, avgDataset);
//
//                // グラフのファイルを生成
//                final File outputFile = graphImageFile(chart, 1940, 930);
//                _graphFiles.add(outputFile);
//
//                // グラフの出力
//                svf.VrsOut("BAR_LABEL", "得点");
//                svf.VrsOut("BAR", outputFile.toString());
//            } catch (Throwable t) {
//                log.fatal("error or exception!", t);
//            }
//        }
        
        private void printRadarGraph(final Vrw32alp svf, final Student student) {
            final ExamData dat = student.data(_param._thisExam);
            final ExamData datBefore = student.data(_param._beforeExam);
            // データ作成
            boolean hasBefore = false;
            final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            for (final Iterator it = dat._record.values().iterator(); it.hasNext();) {
                final Record record = (Record) it.next();
//                if (dat._attendSubclasses.contains(record._subClass)) {
//                    continue; // 元科目は非表示
//                }
                if (_param.ALL3.equals(record._subClass._code) || _param.ALL5.equals(record._subClass._code) || _param.ALL9.equals(record._subClass._code)) {
                    continue;
                }
                
                log.info("レーダーグラフ⇒" + record._subClass + ", " + record._deviation);
                if (record._deviation == null) {
                    continue;
                }
//                final Class clazz = (Class) _param._classes.get(record._subClass.getClassCd());
//                final String name = (null == clazz) ? "???" : clazz._abbv;
//                final String name = (null == clazz) ? record._subClass._abbv : clazz._abbv;
                final String name = record._subClass._abbv;
                dataset.addValue(record._deviation, "本人偏差値", name);// MAX80
                
//                dataset.addValue(50.0, "偏差値50", name);
                if (null != datBefore._record.get(record._subClass._code)) {
                    final Record recordBefore = (Record) datBefore._record.get(record._subClass._code);
                    if (null != recordBefore._deviation) {
                        dataset.addValue(recordBefore._deviation, "前回偏差値", name);// MAX80
                        hasBefore = true;
                    }
                }
            }

            // チャート作成
            try {
                final JFreeChart chart = createRaderChart(dataset);

                // グラフのファイルを生成
                final File outputFile = graphImageFile(chart, 930, 822);
                _graphFiles.add(outputFile);

                // グラフの出力
                if (0 < dataset.getColumnCount()) {
                    svfVrsOutImage(svf, "RADER_LEGEND", hasBefore ? _param.RADER_CHART_LEGEND_NO50_WITH_BEFORE : _param.RADER_CHART_LEGEND_NO50);
                    svf.VrsOut("RADER_TITLE", "科目間バランス");

                    svf.VrsOut("RADER", outputFile.toString());
                }
            } catch (Throwable t) {
                log.fatal("error or exception!", t);
            }
        }

        private JFreeChart createRaderChart(final DefaultCategoryDataset dataset) {
            final KNJH563GSpiderWebPlot plot = new KNJH563GSpiderWebPlot();
            plot.setDataset(dataset);
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
        
        private File graphImageFile(final JFreeChart chart, final int dotWidth, final int dotHeight) {
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

        private void removeImageFiles() {
            for (final Iterator it = _graphFiles.iterator(); it.hasNext();) {
                final File imageFile = (File) it.next();
                if (null == imageFile) {
                    continue;
                }
                log.fatal("グラフ画像ファイル削除:" + imageFile.getName() + " : " + imageFile.delete());
            }
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
    

    private class KNJH563GSpiderWebPlot extends SpiderWebPlot {
        
        private Color lightGrayDarker = new Color(160, 160, 160);

        private class Gauge {
            final int _value;
            final boolean _isBold;
            final List lines = new ArrayList();
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
                final Gauge[] axisGauge = {new Gauge(40, false), new Gauge(50, true), new Gauge(60, false), new Gauge(70, false)};

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
                        final int len = gauge._isBold ? 5 : 4;
                        final int p1x = (int) (baseX + len * Math.cos(Math.toRadians(angle + 90)));
                        final int p1y = (int) (baseY - len * Math.sin(Math.toRadians(angle + 90)));
                        final int p2x = (int) (baseX + len * Math.cos(Math.toRadians(angle - 90)));
                        final int p2y = (int) (baseY - len * Math.sin(Math.toRadians(angle - 90)));
                        final Line2D line = new Line2D.Double(p1x, p1y, p2x, p2y);
                        gauge.lines.add(line);
                    }

                    // 1 = end of axis
                    final Line2D.Double line = new Line2D.Double(centre, endPoint);
                    g2.setPaint(Color.gray);
                    g2.draw(line);
                    drawLabel(g2, radarArea, 0.0, cat, angle, 360.0 / catCount);
                }

                // 目盛りの線を描画
                for (int j = 0; j < axisGauge.length; j++) {
                    final Gauge gauge = axisGauge[j];
                    if (gauge._isBold) {
                        g2.setPaint(Color.gray);
                        g2.setStroke(_stroke2);
                    } else {
                        g2.setPaint(lightGrayDarker);
                        g2.setStroke(_stroke1);
                    }
                    for (final Iterator it = gauge.lines.iterator(); it.hasNext();) {
                        final Line2D line = (Line2D) it.next();
                        g2.draw(line);
                    }
                }

                // Now actually plot each of the series polygons..
                for (int series = 0; series < seriesCount; series++) {
                    drawRadarPoly(g2, radarArea, centre, info, series, catCount, headH, headW);
                }
            } else {
                drawNoDataMessage(g2, area);
            }
            g2.setClip(savedClip);
            g2.setComposite(originalComposite);
            drawOutline(g2, area);
        }
    }

    private static class Exam {

        final String _year;
        final String _semester;
        final String _proficiencydiv;
        final String _proficiencycd;
        final String _grade;
        private String _examName = "";
        private String _semestername = "";
        private String _title = "";
        
        /** 成績平均データ。 */
        private Map _avgDat = new HashMap();
        
        private Map _distribution;
        
        public Exam(final String year, final String semester, final String proficiencydiv, final String proficiencycd, final String grade) {
            _year = year;
            _semester = semester;
            _proficiencydiv = proficiencydiv;
            _proficiencycd = proficiencycd;
            _grade = grade;
        }
        
        public String code() {
            return _year + _semester + _proficiencydiv + _proficiencycd + _grade;
        }

        private void load(final DB2UDB db2, final Param param) {
            if (null == _year) {
                _title = "";
                return;
            }
            _examName = StringUtils.defaultString(KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT proficiencyname1 FROM proficiency_mst WHERE proficiencydiv = '" + _proficiencydiv + "' and proficiencycd = '" + _proficiencycd + "' ")));
            _semestername = StringUtils.defaultString(KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT semestername FROM semester_mst WHERE year='" + _year + "' AND semester='" + _semester + "'")));
            final String nendo = param._isSeireki ? _year + "年度" : KNJ_EditDate.gengou(db2, Integer.parseInt(_year)) + "(" + _year + ")年度";
            _title = nendo + "　" + _semestername + "　" + _examName;
        }

        public String toString() {
            return "Exam(" + _year + ":" + _semester + ":" + _proficiencydiv + ":" + _proficiencycd + ":" + _examName + ")";
        }
    }

    private static class Regd {
        final String _grade;
        final String _hrClass;
        final String _attendNo;
        final String _hrName;
        final String _staffname;

        final String _courseCd;
        final String _majorCd;
        final String _courseCode;
        final String _coursegroupCd;
        
        Regd(
                final String grade,
                final String hrClass,
                final String attendNo,
                final String hrName,
                final String staffname,
                final String courseCd,
                final String majorCd,
                final String courseCode,
                final String coursegroupCd
        ) {
            _grade = grade;
            _hrClass = hrClass;
            _attendNo = attendNo;
            _hrName = hrName;
            _staffname = staffname;
            _courseCd = courseCd;
            _majorCd = majorCd;
            _courseCode = courseCode;
            _coursegroupCd = coursegroupCd;
        }

        public String hrKey() {
            return _hrClass;
        }

        public String courseKey() {
            return _courseCd + _majorCd + _courseCode;
        }

        public String majorKey() {
            return _courseCd + _majorCd + "0000";
        }

        public String coursegroupKey() {
            return "0" + _coursegroupCd + "0000";
        }
        
        public String attendNoStr() {
            if (!StringUtils.isNumeric(_attendNo)) {
                return "";
            }
            final StringBuffer stb = new StringBuffer();
            stb.append(String.valueOf(Integer.parseInt(_attendNo)));
            return stb.append("番").toString();
        }
        
        public int compare(final Regd regd) {
            int rtn;
            rtn = this._grade.compareTo(regd._grade);
            if (0 != rtn) {
                return rtn;
            }
            rtn = this._hrClass.compareTo(regd._hrClass);
            if (0 != rtn) {
                return rtn;
            }
            return this._attendNo.compareTo(regd._attendNo);
        }
    }
    
    private static class ExamData {

        /** 模試データにある科目. */
        private final Map _subclasses = new TreeMap();

        /** 成績データ。 */
        private final Map _record = new TreeMap();

//        /** 元科目。 */
//        private final Set _attendSubclasses = new TreeSet();
        /** 先科目。 */
        private final Set _combinedSubclasses = new TreeSet();
        
        public String toString() {
            return "ExamData(record = " + _record + ")";
        }
    }
    
    private static class Student {
        final String _schregno;
        final String _name;
        
        final Map _regd = new HashMap();
        final Map _data = new HashMap();

        private Student(
                final String schregno,
                final String name
        ) {
            _schregno = schregno;
            _name = name;
        }
        
        final Regd regd(final Exam exam) {
            if (null == exam) {
                return new Regd(null, null, null, null, null, null, null, null, null);
            }
            if (null == _regd.get(exam.code())) {
                _regd.put(exam.code(), new Regd(null, null, null, null, null, null, null, null, null));
            }
            return (Regd) _regd.get(exam.code());
        }
        
        final ExamData data(final Exam exam) {
            if (null == exam) {
                return new ExamData();
            }
            if (null == _data.get(exam.code())) {
                _data.put(exam.code(), new ExamData());
            }
            return (ExamData) _data.get(exam.code());
        }

        public String toString() {
            return _schregno + "/" + _name;
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
        final String _classkey;

        public SubClass(final String code, final String name, final String abbv, final String classkey) {
            _code = code;
            _name = name;
            _abbv = abbv;
            _classkey = classkey;
        }

        public SubClass(final String code) {
            this(code, "xxx", "yyy", "zzz");
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
        final String _scoreDi;
        final Integer _graphScore;
        final BigDecimal _avg;
        final Integer _rank;
        final BigDecimal _deviation;
        final Integer _passScore;

        private Record(
                final SubClass subClass,
                final Integer score,
                final String scoreDi,
                final Integer graphScore,
                final BigDecimal avg,
                final Integer rank,
                final BigDecimal deviation,
                final Integer passScore
        ) {
            _subClass = subClass;
            _score = score;
            _scoreDi = scoreDi;
            _graphScore = graphScore;
            _avg = avg;
            _rank = rank;
            _deviation = deviation;
            _passScore = passScore;
        }

        public String getScore() {
            return null == _score ? (null == _scoreDi ? "" : _scoreDi) : _score.toString();
        }

        public boolean isUnderScore() {
            final String score = getScore();
            final String passScore = intnum(_passScore, "");
            if (StringUtils.isEmpty(score) || !StringUtils.isNumeric(score)) {
                return false;
            }
            if (StringUtils.isEmpty(passScore) || !StringUtils.isNumeric(passScore)) {
                return false;
            }
            final int val = Integer.parseInt(score);
            final int pass = Integer.parseInt(passScore);
            return val < pass;
        }

        public String toString() {
            return _subClass + "/" + _score + "/" + _rank + "/" + _deviation;
        }
    }
    
    private static class AverageDat {
        final SubClass _subClass;
        final BigDecimal _avg;
        final BigDecimal _graphAvg;
        final BigDecimal _stddev;
        final Integer _highscore;
        final Integer _lowscore;
        final Integer _count;
        final String _coursecd;
        final String _majorcd;
        final String _coursecode;

        private AverageDat(
                final SubClass subClass,
                final BigDecimal avg,
                final BigDecimal graphAvg,
                final BigDecimal stddev,
                final Integer highscore,
                final Integer lowscore,
                final Integer count,
                final String coursecd,
                final String majorcd,
                final String coursecode
        ) {
            _subClass = subClass;
            _avg = avg;
            _graphAvg = graphAvg;
            _stddev = stddev;
            _highscore = highscore;
            _lowscore = lowscore;
            _count = count;
            _coursecd = coursecd;
            _majorcd = majorcd;
            _coursecode = coursecode;
        }

        public String toString() {
            return _subClass + "/" + _avg + "/" + _count;
        }
    }
    
    private Param createParam(final HttpServletRequest request) {
        log.fatal("$Revision: 67345 $ $Date: 2019-05-07 18:09:11 +0900 (火, 07 5 2019) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);

        if (!KNJServletUtils.isEnableGraph(log)) {
            log.fatal("グラフを使える環境ではありません。");
        }
        final Param param = new Param(request);
        return param;
    }
    
    private static class Param {
        final String _year;
        final String _semester;

        final String _formGroupDiv;
        /** 中学か? false なら高校. */
        private boolean _isJunior;
        private boolean _isHigh;
        /** 中高一貫フラグ。 */
        private boolean _isIkkan;
        private boolean _isSeireki;

        /** [クラス指定 or 生徒指定]の値。 */
        final String _categoryIsClass;
        /** クラス or 生徒。 */
        final String[] _categorySelected;

//        final String _submitDate;
        final String _loginDate;

        /** 学校名。 */
        private String _schoolName;
        /** 担任職種名。 */
        private String _remark2;
        /** 順位を印字しないか? */
        final boolean _rankNotPrint;
        /** 順位の基準点 1:総合点,2:平均点,3:偏差値,11:傾斜総合点 */
        final String _rankDataDiv;
        /** 平均の基準点 1:得点,2:傾斜総合点 */
        final String _avgDataDiv;

        /** 教科マスタ。 */
        private Map _classes;
        final String _imagePath;

//        private final Map _subclassGroup3 = new HashMap();
//        private final Map _subclassGroup5 = new HashMap();
//        private final MultiMap _subclassGroupDat3 = new MultiHashMap();
//        private final MultiMap _subclassGroupDat5 = new MultiHashMap();

//        /** レーダーチャートの科目. */
//        private List _fiveSubclass = new ArrayList();
//        private List _threeSubclass = new ArrayList();

        final Exam _thisExam;
        private Exam _beforeExam = new Exam(null, null, null, null, null);
        /** 模試科目マスタ。 */
        private Map _subClasses;
        
        /** レーダーチャート凡例画像. */
        final String RADER_CHART_LEGEND = "RaderChartLegend.png";
        /** レーダーチャート凡例画像偏差値50なし. */
        final String RADER_CHART_LEGEND_NO50 = "RaderChartLegendNo50.png";
        /** レーダーチャート凡例画像偏差値50なし前回有り. */
        final String RADER_CHART_LEGEND_NO50_WITH_BEFORE = "RaderChartLegendNo50WithBefore.png";
        /** 棒グラフ凡例画像1(学年). */
        final String BAR_CHART_LEGEND1 = "BarChartLegendGrade.png";
        /** 棒グラフ凡例画像2(クラス). */
        final String BAR_CHART_LEGEND2 = "BarChartLegendHr.png";
        /** 棒グラフ凡例画像3(コース). */
        final String BAR_CHART_LEGEND3 = "BarChartLegendCourse.png";
        /** 棒グラフ凡例画像4(学科). */
        final String BAR_CHART_LEGEND4 = "BarChartLegendMajor.png";
        /** 棒グラフ凡例画像5(コースグループ). */
        final String BAR_CHART_LEGEND5 = "BarChartLegendCourseGroup.png";

        final String ALL3 = "333333";
        final String ALL5 = "555555";
        final String ALL9 = "999999";

        public Param(final HttpServletRequest request) {
            if ("4".equals(request.getParameter("JUNI"))) {
                _rankDataDiv = "11";
                _avgDataDiv = "2";
            } else {
                final String rankDivTemp = request.getParameter("useKnjd106cJuni" + request.getParameter("JUNI"));
                final String rankDataDiv0 = (rankDivTemp == null) ? request.getParameter("JUNI") : rankDivTemp;
                _rankDataDiv = (null != rankDataDiv0 && rankDataDiv0.length() < 2 ? "0" : "") + rankDataDiv0;
                _avgDataDiv = "1";
            }

            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _categoryIsClass = request.getParameter("CATEGORY_IS_CLASS");
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
//            _submitDate = request.getParameter("SUBMIT_DATE");
            _loginDate = request.getParameter("LOGIN_DATE");

            _formGroupDiv = request.getParameter("FORM_GROUP_DIV");
            _rankNotPrint = "1".equals(request.getParameter("JUNI_PRINT"));

            _imagePath = request.getParameter("IMAGE_PATH");

            imageFileCheck(RADER_CHART_LEGEND);
            imageFileCheck(BAR_CHART_LEGEND1);
            imageFileCheck(BAR_CHART_LEGEND2);
            imageFileCheck(BAR_CHART_LEGEND3);
            imageFileCheck(BAR_CHART_LEGEND4);
            imageFileCheck(BAR_CHART_LEGEND5);
            
            _thisExam = new Exam(_year, _semester, request.getParameter("PROFICIENCYDIV"), request.getParameter("PROFICIENCYCD"), request.getParameter("GRADE"));
        }

        public String barchartLegendImage() {
            final String barLegendImage;
            if (isHr()) {
                barLegendImage = BAR_CHART_LEGEND2;
            } else if (isCourse()){
                barLegendImage = BAR_CHART_LEGEND3;
            } else if (isMajor()) {
                barLegendImage = BAR_CHART_LEGEND4;
            } else if (isCoursegroup()) {
                barLegendImage = BAR_CHART_LEGEND5;
            } else {
                barLegendImage = BAR_CHART_LEGEND1;
            }
            return barLegendImage;
        }
        
        public String formGroupDivName() {
            final String msg;
            if (isHr()) {
                msg = "クラス";
            } else if (isCourse()){
                msg = "コース";
            } else if (isMajor()) {
                msg = "学科";
            } else if (isCoursegroup()) {
                msg = "グループ";
            } else {
                msg = "学年";
            }
            return msg;
        }
        
        public boolean isGakunen() {
            return "1".equals(_formGroupDiv);
        }
        
        public boolean isHr() {
            return "2".equals(_formGroupDiv);
        }

        public boolean isCourse() {
            return "3".equals(_formGroupDiv);
        }
        
        public boolean isMajor() {
            return "4".equals(_formGroupDiv);
        }
        
        public boolean isCoursegroup() {
            return "5".equals(_formGroupDiv);
        }
        
        private void imageFileCheck(final String fName) {
            final File f = new File(_imagePath + "/" + fName);
            if (!f.exists()) {
                log.fatal("画像ファイルが無い!⇒" + _imagePath + "/" + fName);
            }
        }

        public String[] getScregnos(final DB2UDB db2) throws SQLException {
            final String separator = "-";
            final List result = new ArrayList();

            if (!"1".equals(_categoryIsClass)) {
                return _categorySelected;
            }

            // 年組から学籍番号たちを得る
            for (int i = 0; i < _categorySelected.length; i++) {
                final String grade = StringUtils.split(_categorySelected[i], separator)[0];
                final String room = StringUtils.split(_categorySelected[i], separator)[1];
                
                final String sql = " select"
                        + "    SCHREGNO "
                        + " from"
                        + "    SCHREG_REGD_DAT"
                        + " where"
                        + "    YEAR = '" + _year + "' and"
                        + "    SEMESTER = '" + _semester + "' and"
                        + "    GRADE = '" + grade + "' and"
                        + "    HR_CLASS = '" + room + "'"
                        ;
                
                result.addAll(KnjDbUtils.getColumnDataList(KnjDbUtils.query(db2, sql), "SCHREGNO"));
            }

            final String[] rtn = new String[result.size()];
            int i = 0;
            for (final Iterator it = result.iterator(); it.hasNext();) {
                final String schregno = (String) it.next();
                rtn[i++] = schregno;
            }

            return rtn;
        }

        public void load(final DB2UDB db2) throws SQLException {
            loadIkkanFlg(db2);
            loadCertifSchool(db2);
            loadBefore(db2);
            _isSeireki = KNJ_EditDate.isSeireki(db2);
            _thisExam.load(db2, this);
            _beforeExam.load(db2, this);
            log.debug(" thisExam   = "+ _thisExam);
            log.debug(" beforeExam = "+ _beforeExam);
            _classes = setClasses(db2);
            _subClasses = setSubClasses(db2);

            // proficiency_subclass_group_dat を読込んで _fiveSubclass や _threeSubclass の箱に入れる
//            loadSubclassGroup(db2);
//            loadSubclassGroupDat(db2);
        }

        private Map setSubClasses(final DB2UDB db2) throws SQLException {
            final Map rtn = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                
                final String sql = "select"
                        + "   PROFICIENCY_SUBCLASS_CD ,"
                        + "   SUBCLASS_NAME as NAME,"
                        + "   SUBCLASS_ABBV as SUBCLASSABBV,"
                        + "   CLASSCD,"
                        + "   SCHOOL_KIND"
                        + " from PROFICIENCY_SUBCLASS_MST"
                        + " order by"
                        + "   PROFICIENCY_SUBCLASS_CD"
                    ;

                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String code = rs.getString("PROFICIENCY_SUBCLASS_CD");
                    final String name = rs.getString("NAME");
                    final String abbv = rs.getString("SUBCLASSABBV");
                    final String classkey = rs.getString("CLASSCD") + "-" + rs.getString("SCHOOL_KIND");
                    rtn.put(code, new SubClass(code, name, abbv, classkey));
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            log.debug("模試科目マスタ総数=" + rtn.size());
            return rtn;
        }

        private Map setClasses(final DB2UDB db2) throws SQLException {
            final Map rtn = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = "select"
                        + "   CLASSCD || '-' || SCHOOL_KIND AS CODE,"
                        + "   CLASSNAME,"
                        + "   CLASSABBV"
                        + " from V_CLASS_MST"
                        + " where"
                        + "   YEAR = '" + _year + "'"
                        + " order by"
                        + "   CLASSCD"
                        ;

                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String code = rs.getString("CODE");
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
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append("   select ");
                stb.append("     t1.YEAR, ");
                stb.append("     t1.SEMESTER, ");
                stb.append("     t1.PROFICIENCYDIV, ");
                stb.append("     t1.PROFICIENCYCD, ");
                stb.append("     t1.GRADE, ");
                stb.append("     case when t2.YEAR is not null then 1 else 0 end as CURRENT ");
                stb.append("   from  ");
                stb.append("     PROFICIENCY_YMST t1 ");
                stb.append("     left join PROFICIENCY_YMST t2 ON ");
                stb.append("       t2.YEAR = '" + _year + "' ");
                stb.append("       and t2.SEMESTER = '" + _semester + "' ");
                stb.append("       and t2.PROFICIENCYDIV = '" + _thisExam._proficiencydiv + "' ");
                stb.append("       and t2.PROFICIENCYCD = '" + _thisExam._proficiencycd + "' ");
                stb.append("       and t2.GRADE = '" + _thisExam._grade + "' ");
                stb.append("       and t2.YEAR = t1.YEAR ");
                stb.append("       and t2.SEMESTER = t1.SEMESTER ");
                stb.append("       and t2.PROFICIENCYDIV = t1.PROFICIENCYDIV ");
                stb.append("       and t2.PROFICIENCYCD = t1.PROFICIENCYCD ");
                stb.append("       and t2.GRADE = t1.GRADE ");
                stb.append("   where ");
                stb.append("     (t1.YEAR, INT(T1.GRADE)) IN (VALUES('" + _year + "', INT('" + _thisExam._grade + "'))) ");
                stb.append("     and t1.PROFICIENCYDIV = '" + _thisExam._proficiencydiv + "' ");
                stb.append("   order by ");
                stb.append("     t1.YEAR, ");
                stb.append("     t1.SEMESTER, ");
                stb.append("     t1.PROFICIENCYDIV, ");
                stb.append("     t1.PROFICIENCYCD ");
                
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                final Map examMap = new HashMap();
                Integer current = null;
                int n = 0;
                while (rs.next()) {
                    final String beforeYear = rs.getString("year");
                    final String beforeSemester = rs.getString("semester");
                    final String beforeProficiencydiv = rs.getString("proficiencydiv");
                    final String beforeProficiencycd = rs.getString("proficiencycd");
                    final String beforeGrade = rs.getString("grade");
                    final Integer key = new Integer(n);
                    examMap.put(key, new Exam(beforeYear, beforeSemester, beforeProficiencydiv, beforeProficiencycd, beforeGrade));
                    if (1 == rs.getInt("CURRENT")) {
                        current = key;
                    }
                    n += 1;
                }
                log.fatal(" current = " + current);
                if (null != current) {
                    final Integer tgt = new Integer(current.intValue() - 1);
                    _beforeExam = (Exam) examMap.get(tgt);
                }
                if (null == _beforeExam) {
                    _beforeExam = new Exam(null, null, null, null, null);
                }
                //log.fatal(" before = "  + _beforeExam);
            } catch (final SQLException e) {
                log.error("以前の考査取得エラー。", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
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
            }
        }

        private void loadIkkanFlg(final DB2UDB db2) {
            final String namespare2 = KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT namespare2 FROM v_name_mst WHERE year='" + _year + "' AND namecd1='Z010' AND namecd2='00'"));
            _isIkkan = (namespare2 != null) ? true : false;

            final int gradeVal = Integer.parseInt(_thisExam._grade);
            _isJunior = (gradeVal <= 3 && _isIkkan) ? true : false;
            _isHigh = !_isJunior;
            log.debug("中高一貫フラグ=" + _isIkkan);
        }

//        public void setSubclasses(final Student student) {
//            _fiveSubclass = (List) _subclassGroupDat5.get(student.courseKey());
//            _threeSubclass = (List) _subclassGroupDat3.get(student.courseKey());
//        }

//        private void loadSubclassGroup(final DB2UDB db2) {
//            PreparedStatement ps = null;
//            ResultSet rs = null;
//            try {
//                final String sql = subclassGroupSQL();
//                ps = db2.prepareStatement(sql);
//                rs = ps.executeQuery();
//                while (rs.next()) {
//                    final String groupDiv = rs.getString("group_div");
//                    final String courseCd = rs.getString("coursecd");
//                    final String majorCd = rs.getString("majorcd");
//                    final String courseCode = rs.getString("coursecode");
//                    final String groupName = rs.getString("group_name");
//
//                    final String key = courseCd + majorCd + courseCode;
//                    if ("3".equals(groupDiv)) {
//                        _subclassGroup3.put(key, groupName);
//                    } else {
//                        _subclassGroup5.put(key, groupName);
//                    }
//                }
//            } catch (final SQLException e) {
//                log.error("proficiency_subclass_group_mst の取得エラー。");
//            }
//
//            log.debug("3教科の名称たち=" + _subclassGroup3);
//            log.debug("5教科の名称たち=" + _subclassGroup5);
//        }

//        private String subclassGroupSQL() {
//            return "SELECT"
//                    + "  group_div,"
//                    + "  coursecd,"
//                    + "  majorcd,"
//                    + "  coursecode,"
//                    + "  group_name"
//                    + " FROM"
//                    + "  proficiency_subclass_group_mst"
//                    + " WHERE"
//                    + "  year='" + _thisExam._year + "' AND"
//                    + "  semester='" + _thisExam._semester + "' AND"
//                    + "  proficiencydiv='" + _thisExam._proficiencydiv + "' AND"
//                    + "  proficiencycd='" + _thisExam._proficiencycd + "' AND"
//                    + "  grade='" + _grade + "' AND"
//                    + "  group_div in ('3', '5')"   // 3教科, 5教科
//                    ;
//        }

//        private void loadSubclassGroupDat(final DB2UDB db2) {
//            PreparedStatement ps = null;
//            ResultSet rs = null;
//            try {
//                final String sql = subclassGroupDatSQL();
//                ps = db2.prepareStatement(sql);
//                rs = ps.executeQuery();
//                while (rs.next()) {
//                    final String groupDiv = rs.getString("group_div");
//                    final String courseCd = rs.getString("coursecd");
//                    final String majorCd = rs.getString("majorcd");
//                    final String courseCode = rs.getString("coursecode");
//                    final String subclassCd = rs.getString("subclasscd");
//
//                    final String key = courseCd + majorCd + courseCode;
//                    if ("3".equals(groupDiv)) {
//                        _subclassGroupDat3.put(key, subclassCd);
//                    } else {
//                        _subclassGroupDat5.put(key, subclassCd);
//                    }
//                }
//            } catch (final SQLException e) {
//                log.error("proficiency_subclass_group_dat の取得エラー");
//            }
//
//            log.debug("3教科の科目CDたち=" + _subclassGroupDat3);
//            log.debug("5教科の科目CDたち=" + _subclassGroupDat5);
//        }

//        private String subclassGroupDatSQL() {
//            return "SELECT"
//                    + "  group_div,"
//                    + "  coursecd,"
//                    + "  majorcd,"
//                    + "  coursecode,"
//                    + "  proficiency_subclass_cd as subclasscd"
//                    + " FROM"
//                    + "  proficiency_subclass_group_dat"
//                    + " WHERE"
//                    + "  year='" + _thisExam._year + "' AND"
//                    + "  semester='" + _thisExam._semester + "' AND"
//                    + "  proficiencydiv='" + _thisExam._proficiencydiv + "' AND"
//                    + "  proficiencycd='" + _thisExam._proficiencycd + "' AND"
//                    + "  grade='" + _grade + "' AND"
//                    + "  group_div in ('3', '5')"   // 3教科, 5教科
//                    ;
//        }

    }
}

// eof
