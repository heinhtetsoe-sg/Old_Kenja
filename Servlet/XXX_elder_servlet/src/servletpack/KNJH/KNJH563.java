// kanji=漢字
/*
 * $Id: 9de7cf7bcb7cc8b5d4645e206ff720bafb5b4518 $
 *
 * 作成日: 2008/05/24 10:02:00 - JST
 * 作成者: takaesu
 *
 * Copyright(C) 2004-2008 ALP Co.,Ltd. All rights reserved.
 */
package servletpack.KNJH;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.ObjectUtils;
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
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.SvfField;

/**
 * 個人成績票。
 * @author takaesu
 * @version $Id: 9de7cf7bcb7cc8b5d4645e206ff720bafb5b4518 $
 */
public class KNJH563 {
    /*pkg*/static final Log log = LogFactory.getLog(KNJH563.class);

    /** 3教科科目コード。 */
    private static final String ALL3 = "333333";
    /** 5教科科目コード。 */
    private static final String ALL5 = "555555";
    /** 全教科科目コード。 */
    private static final String ALL9 = "999999";

    private static final String RANK_DIV_GRADE = "01";
    private static final String RANK_DIV_HR = "02";
    private static final String RANK_DIV_COURSE = "03";
    private static final String RANK_DIV_MAJOR = "04";
    private static final String RANK_DIV_COURSEGROUP = "05";

    private static final String AVG_DIV_GRADE = "01";
    private static final String AVG_DIV_HR = "02";
    private static final String AVG_DIV_COURSE = "03";
    private static final String AVG_DIV_MAJOR = "04";
    private static final String AVG_DIV_COURSEGROUP = "05";

    private static final String DEVPRI1_DEVIATION = "1";
    private static final String DEVPRI2_STDDEV = "2";

    public void svf_out(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        log.fatal("$Revision: 74674 $ $Date: 2020-06-02 19:23:19 +0900 (火, 02 6 2020) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);

        if (!KNJServletUtils.isEnableGraph(log)) {
            log.fatal("グラフを使える環境ではありません。");
        }

        final DB2UDB db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
        try {
            db2.open();
        } catch (final Exception ex) {
            log.error("db open error!", ex);
            return;
        }

        boolean hasData = false;
        Param param = null;
        Form _form = null;
        try {
            param = new Param(request, db2);
            _form = new Form(param, response);

            log.error("★マスタ関連の読込み");
            param.load(db2);

            // 対象の生徒たちを得る
            final List<Student> students = Student.createStudents(db2, param);

            // 成績のデータを読む
            log.error("★成績関連の読込み");

            loadExam(db2, param, students, param._thisExam);
            loadExam(db2, param, students, param._beforeExam);

            // 印刷する
            log.error("★印刷");
            for (final Student student : students) {
                if (param._isOutputDebug) {
                	log.info("☆" + student + ", 科目の数=" + student.getExamData(param._thisExam)._subclasses.size() + ", コースキー=" + student._regd.courseKey());
                	log.info("今回の成績: " + student.getExamData(param._thisExam)._record.values());
                }

//                _param.setSubclasses(student);

                if (!param._hyoshiNotPrint) {
                	_form.printHyoshi(db2, param, student);
                }

                _form.printMain(param, student);
                hasData = true;
            }

            log.error("★終了処理");
            if (!hasData) {
            	_form._svf.VrSetForm("MES001.frm", 0);
            	_form._svf.VrsOut("note", "note");
            	_form._svf.VrEndPage();
            }
            _form._svf.VrQuit();

        } catch (Exception e) {
        	log.error("exception!", e);
        } finally {
            if (null != db2) {
            	db2.commit();
            	db2.close();
            }
            if (null != param) {
            	param.removeImageFiles();
            }
            log.info("Done.");
        }
    }

    private void loadExam(final DB2UDB db2, final Param param, final List<Student> students, final Exam exam) throws SQLException {
    	SubClass.loadSubClasses(db2, param, students, exam); // 科目は指示画面指定のテストの科目
        loadAverageDat(db2, param, exam);
        loadRecord(db2, param, students, exam);
		loadAttendSubclass(db2, param, students, exam);
        loadRecordOther(db2, param, students, exam);
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
    private void loadAverageDat(
            final DB2UDB db2,
            final Param param,
            final Exam exam
    ) throws SQLException {

        final String selectFrom;
        selectFrom = "SELECT"
            + "  proficiency_subclass_cd as subclasscd,"
            + "  avg,"
            + "  stddev,"
            + "  highscore,"
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
                final SubClass subClass = param._subClasses.get(subclasscd);
                if (null == subClass) {
                    log.warn("模試成績平均データが模試科目マスタに無い!:" + subclasscd);
                }
                final BigDecimal avg = rs.getBigDecimal("avg");
                final BigDecimal stddev = rs.getBigDecimal("stddev");
                final BigDecimal graphAvg = rs.getBigDecimal("graphAvg");
                final Integer count = KNJServletUtils.getInteger(rs, "count");
                final Integer highScore = KNJServletUtils.getInteger(rs, "highscore");
                final String hrClass = rs.getString("HR_CLASS");
                final String coursecd = rs.getString("coursecd");
                final String majorcd = rs.getString("majorcd");
                final String coursecode = rs.getString("coursecode");

                final String key;
                if (param.isHr()) {
                    key = subclasscd + hrClass;
                } else if (param.isCourse()) {
                    key = subclasscd + coursecd + majorcd + coursecode;
                } else if (param.isMajor()) {
                    key = subclasscd + coursecd + majorcd + "0000";
                } else if (param.isCoursegroup()) {
                    key = subclasscd + "0" + majorcd + "0000";
                } else {
                    key = subclasscd;
                }
                if (ALL3.equals(subclasscd) || ALL5.equals(subclasscd) || ALL9.equals(subclasscd)) {
                    final SubClass subClass0 = new SubClass(subclasscd);
                    final AverageDat avgDat = new AverageDat(subClass0, avg, graphAvg, stddev, highScore, count, coursecd, majorcd, coursecode);
                    exam._averageDatOther.put(key, avgDat);
                } else {
                    final AverageDat avgDat = new AverageDat(subClass, avg, graphAvg, stddev, highScore, count, coursecd, majorcd, coursecode);
                    exam._averageDat.put(key, avgDat);
                }
            }
        } catch (final SQLException e) {
            log.warn("模試成績平均データの取得でエラー");
            throw e;
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        log.debug("模試コード=" + exam._proficiencydiv + " : " + exam._proficiencycd + " の模試成績平均データの件数=" + exam._averageDat.size());
    }

    private String sqlAttendSubclass(final Regd regd, final Exam exam) {
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
        return sql.toString();
    }

    private void loadAttendSubclass(final DB2UDB db2, final Param param, final List<Student> students, final Exam exam) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            for (final Student student : students) {
		        final Regd regd = student._regds.get(exam);
                if (null == regd._grade) {
                    continue;
                }
                final String sql = sqlAttendSubclass(regd, exam);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String subclasscd = rs.getString("ATTEND_SUBCLASSCD");
                    SubClass subClass = param._subClasses.get(subclasscd);
                    if (null == subClass) {
                        log.warn("合併科目のデータが模試科目マスタに無い!:" + subclasscd);
                        final Class clazz = param._classes.get(subclasscd.substring(0, 2));
                        if (null == clazz) {
                            continue;
                        }
                        subClass = new SubClass(subclasscd, clazz._name, clazz._abbv, "", "");
                    }
                    student.getExamData(exam)._attendSubclasses.add(subClass);
                }
            }
        } catch (final SQLException e) {
            log.warn("合併科目のデータの取得でエラー", e);
        } finally {
            DbUtils.closeQuietly(rs);
        }
    }

    private void loadRecord(final DB2UDB db2, final Param param, final List<Student> students, final Exam exam) {
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
            + "  T9.score as score_kansannashi,"
            + "  T5.PASS_SCORE"
            + " FROM proficiency_dat T1 "
            + " INNER JOIN PROFICIENCY_SUBCLASS_MST MST ON MST.PROFICIENCY_SUBCLASS_CD = T1.PROFICIENCY_SUBCLASS_CD "
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
            + " LEFT JOIN proficiency_rank_dat T9 ON "
            + "     T1.year = T9.year AND "
            + "     T1.semester=T9.semester AND"
            + "     T1.proficiencydiv=T9.proficiencydiv AND"
            + "     T1.proficiencycd=T9.proficiencycd AND"
            + "     T1.proficiency_subclass_cd=T9.proficiency_subclass_cd AND"
            + "     T1.schregno = T9.schregno AND "
            + "     T9.rank_data_div = '01' AND "
            + "     T9.rank_div = '" + RANK_DIV_GRADE + "' "
            + " WHERE"
            + "  T1.year='" + exam._year + "' AND"
            + "  T1.semester='" + exam._semester + "' AND"
            + "  T1.proficiencydiv='" + exam._proficiencydiv + "' AND"
            + "  T1.proficiencycd='" + exam._proficiencycd + "' AND"
            + "  T1.schregno=?"
            ;
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			if (param._isOutputDebug) {
				log.info(" record sql = " + sql);
			}
		    ps = db2.prepareStatement(sql);
		    for (final Student student : students) {
		        final Regd regd = student._regds.get(exam);
		        int i = 0;
		        if (null == regd._grade) {
		            continue;
		        }
		        //欠点（満点マスタの合格点）
		        ps.setString(++i, regd._grade);
		        ps.setString(++i, regd.courseKey());
		        ps.setString(++i, student._schregno);
				if (param._isOutputDebug) {
					log.info(" sql param = " + ArrayUtils.toString(new Object[] {regd._grade, regd.courseKey(), student._schregno}));
				}
		        rs = ps.executeQuery();
		        while (rs.next()) {
		            final String subclasscd = rs.getString("subclasscd");
		            SubClass subClass = param._subClasses.get(subclasscd);
		            if (null == subClass) {
		                log.warn("対象成績データが模試科目マスタに無い!:" + subclasscd);
		                final Class clazz = param._classes.get(subclasscd.substring(0, 2) + (param._useCLASS_MST_SCHOOL_KIND ? (param._schoolKind) : ""));
		                if (null == clazz) {
		                    continue;
		                }
		                subClass = new SubClass(subclasscd, clazz._name, clazz._abbv, "", "");
		            }
		            final Integer score1 = KNJServletUtils.getInteger(rs, "score");
		            final Integer scoreKansannnashi = KNJServletUtils.getInteger(rs, "score_kansannashi");
		            final Integer score = "1".equals(param._knjh563PrintScoreKansannashi) ? scoreKansannnashi : score1;
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
		            student.getExamData(exam)._record.put(subClass, rec);
		        }
		    }
		} catch (final SQLException e) {
		    log.warn("成績データの取得でエラー", e);
		} finally {
		    DbUtils.closeQuietly(null, ps, rs);
		}
    }

    private void loadRecordOther(final DB2UDB db2, final Param param, final List<Student> students, final Exam exam) {
    	final String sql;
    	sql = "SELECT"
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
    			+ "  t6.deviation as hr_deviation,"
    			+ "  T7.score as score_kansannashi"
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
    			+ " LEFT JOIN proficiency_rank_dat T7 ON "
    			+ "     T1.year = T7.year AND "
    			+ "     T1.semester=T7.semester AND"
    			+ "     T1.proficiencydiv=T7.proficiencydiv AND"
    			+ "     T1.proficiencycd=T7.proficiencycd AND"
    			+ "     T1.proficiency_subclass_cd=T7.proficiency_subclass_cd AND"
    			+ "     T1.schregno = T7.schregno AND "
    			+ "     '01' = T7.rank_data_div AND "
    			+ "     T7.rank_div = '" + RANK_DIV_GRADE + "' "
    			+ " WHERE"
    			+ "  T1.year='" + exam._year + "' AND"
    			+ "  T1.semester='" + exam._semester + "' AND"
    			+ "  T1.proficiencydiv='" + exam._proficiencydiv + "' AND"
    			+ "  T1.proficiencycd='" + exam._proficiencycd + "' AND"
    			+ "  T1.proficiency_subclass_cd IN ('" + ALL3 + "', '" + ALL5 + "', '" + ALL9 + "') AND"
    			+ "  t1.rank_data_div ='" + param._rankDataDiv + "' AND"
    			+ "  t1.rank_div ='" + RANK_DIV_GRADE + "' AND"
    			+ "  t1.schregno=?"
    			;
        PreparedStatement ps = null;
        ResultSet rs = null;
    	try {
            ps = db2.prepareStatement(sql);

            for (final Student student : students) {

                ps.setString(1, student._schregno);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String subclasscd = rs.getString("subclasscd");
                    final Integer score1 = KNJServletUtils.getInteger(rs, "score");
                    final Integer scoreKansannnashi = KNJServletUtils.getInteger(rs, "score_kansannashi");
                    final Integer score = "1".equals(param._knjh563PrintScoreKansannashi) ? scoreKansannnashi : score1;

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
                    student.getExamData(exam)._recordOther.put(subClass._code, rec);
                }
            }
    	} catch (Exception e) {
    		log.error("exception!", e);
    	} finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
    	}
    }

    private static class Exam {

        private final String _year;
        private final String _semester;
        private final String _proficiencydiv;
        private final String _proficiencycd;
        private final String _grade;
        private String _examName = "";
        private String _semestername = "";
        private String _title = "";

        /** 成績平均データ。 */
        private Map<String, AverageDat> _averageDat = new HashMap<String, AverageDat>();
        /** 成績平均データ。3教科,5教科用 */
        private Map<String, AverageDat> _averageDatOther = new HashMap<String, AverageDat>();

        public Exam(final String year, final String semester, final String proficiencydiv, final String proficiencycd, final String grade) {
            _year = year;
            _semester = semester;
            _proficiencydiv = proficiencydiv;
            _proficiencycd = proficiencycd;
            _grade = grade;
        }

        private void loadTitle(final DB2UDB db2) {
            if (null == _year) {
                _title = "";
                return;
            }
            _examName = StringUtils.defaultString(KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT PROFICIENCYNAME1 FROM proficiency_mst WHERE proficiencydiv = '" + _proficiencydiv + "' and proficiencycd = '" + _proficiencycd + "' ")));
            _semestername = StringUtils.defaultString(KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT semestername FROM semester_mst WHERE year='" + _year + "' AND semester='" + _semester + "'")));
            _title = nendo(db2) + "　" + _semestername + "　" + _examName;
        }

        String nendo(final DB2UDB db2) {
            return KNJ_EditDate.gengou(db2, Integer.parseInt(_year)) + "(" + _year + ")年度";
        }

        public String toString() {
            return "Exam(" + _year + ":" + _semester + ":" + _proficiencydiv + ":" + _proficiencycd + ":" + _examName + ")";
        }

		public String getExamKey() {
			return _year + _semester + _proficiencydiv + _proficiencycd;
		}
    }

    private static class Form {

        /** 成績表の最大科目数. */
        private final int TABLE_SUBCLASS_MAX = 20;
        /** 棒グラフの最大科目数. */
        private final int BAR_GRAPH_MAX_ITEM = 11;
        /** 生徒氏名のフォント切替の境目文字数. */
        private final int STUDENT_NAME_DELIMTER_COUNT = 10;

        private Vrw32alp _svf;
        private final Param _param;

        private Map<String, Map<String, SvfField>> _formInfo = new HashMap<String, Map<String, SvfField>>();

        private String _currentForm;

        public Form(final Param param, final HttpServletResponse response) throws IOException {
            _param = param;
            _svf = new Vrw32alp();
            if (_svf.VrInit() < 0) {
                throw new IllegalStateException("svf初期化失敗");
            }
            _svf.VrSetSpoolFileStream(response.getOutputStream());
            response.setContentType("application/pdf");
        }

        public void setForm2() {
        	if (_param._isTosajoshi) {
                if (_param._hogoshaNotPrint) {
                	_currentForm = "KNJH563_3TOSAJOSHI.frm";
                } else {
                	_currentForm = "KNJH563_2TOSAJOSHI.frm";
                }
        	} else {
                if (_param._hogoshaNotPrint) {
                	_currentForm = "KNJH563_3.frm";
                } else {
                	_currentForm = "KNJH563_2.frm";
                }
        	}
            final int sts = _svf.VrSetForm(_currentForm, 4);
            if (null == _formInfo.get(_currentForm)) {
            	log.info(" form = " + _currentForm);
            	final Map<String, SvfField> info = new HashMap<String, SvfField>();
            	try {
            		info.putAll(SvfField.getSvfFormFieldInfoMapGroupByName(_svf));
            	} catch (Throwable t) {
            		log.error("exception!", t);
            	}
            	_formInfo.put(_currentForm, info);
            }
            if (0 != sts) {
                log.error("VrSetFromエラー:sts=" + sts);
            }
        }

        private int getFieldKeta(final Param param, final String fieldname) {
        	int length = -1;
        	try {
            	final Map<String, SvfField> info = _formInfo.get(_currentForm);
            	if (null != info) {
            		SvfField field = info.get(fieldname);
            		length = field._fieldLength;
//            		if (param._isOutputDebug) {
//            			log.info(" field " + fieldname + " = " + length);
//            		}
            	}
        	} catch (Throwable e) {
        	}
        	return length;
        }

        public void printHyoshi(final DB2UDB db2, final Param param, final Student student) {

            final int sts = _svf.VrSetForm("KNJH563_1.frm", 1);
            if (0 != sts) {
                log.error("VrSetFromエラー:sts=" + sts);
            }

            _svf.VrsOut("NENDO", param._thisExam.nendo(db2));
            _svf.VrsOut("SEMESTER", param._thisExam._semestername);
            _svf.VrsOut("TESTNAME", param._thisExam._examName);
            _svf.VrsOut("HR_NAME", student._regd._hrName + student._regd.attendNoStr(param));

            if (student._name.length() <= STUDENT_NAME_DELIMTER_COUNT) {
                _svf.VrsOut("NAME", student._name);
            } else {
                _svf.VrsOut("NAME_2", student._name);
            }
            _svf.VrEndPage();
        }

        public void printHeader(final Param param, final Student student) {
        	if (!_param._isTosajoshi && !_param._isChiyodaKudan) {
                _svf.VrsOut("SCHOOL_NAME", param._schoolName);

                final String staffName = param._staffs.get(student._regd._grade + student._regd._hrClass);
                _svf.VrsOut("STAFFNAME", StringUtils.defaultString(param._remark2) + StringUtils.defaultString(staffName));
            }

            _svf.VrsOut("NENDO", _param._thisExam._title);
            _svf.VrsOut("LAST_EXAM_TITLE", StringUtils.isBlank(_param._beforeExam._title) ? "" : (_param._beforeExam._title + "の成績"));

            _svf.VrsOut("HR_NAME", student._regd._hrName + student._regd.attendNoStr(param));

            if (student._name.length() <= STUDENT_NAME_DELIMTER_COUNT) {  // 全角で規定文字数を超えたらフォントを変える
                _svf.VrsOut("NAME", student._name);
            } else {
                _svf.VrsOut("NAME_2", student._name);
            }

            // 提出日
            try {
                final Calendar cal = KNJServletUtils.parseDate(param._submitDate.replace('/', '-'));
                final String month = (cal.get(Calendar.MONTH) + 1) + "月";
                final String day = cal.get(Calendar.DATE) + "日";
                _svf.VrsOut("DATE", month + day);
            } catch (final ParseException e) {
                log.error("提出日が解析できない");
                _svf.VrsOut("DATE", param._submitDate);
            }

//            final String get3title = _param.get3title(student.courseKey());
//            if (null != get3title) {
//                _svf.VrsOut("ITEM_TOTAL3", get3title);
//            }
//            final String get5title = _param.get5title(student.courseKey());
//            if (null != get5title) {
//                _svf.VrsOut("ITEM_TOTAL5", get5title);
//            }
            _svf.VrsOut("ITEM_TOTAL5", "全教科");

            String barLegendImage = null;
            final String msg;
            if (param.isHr()) {
                barLegendImage = param.BAR_CHART_LEGEND2;
                msg = "クラス";
            } else if (param.isCourse()){
                barLegendImage = param.BAR_CHART_LEGEND3;
                msg = "コース";
            } else if (param.isMajor()) {
                barLegendImage = param.BAR_CHART_LEGEND4;
                msg = "学科";
            } else if (param.isCoursegroup()) {
                barLegendImage = param.BAR_CHART_LEGEND5;
                msg = "グループ";
            } else {
                barLegendImage = param.BAR_CHART_LEGEND1;
                msg = "学年";
            }

            _svf.VrsOut("ITEM_AVG", msg + "平均");
            _svf.VrsOut("ITEM_RANK", msg + "順位");

            if (DEVPRI1_DEVIATION.equals(_param._deviationPrint)) {
                _svf.VrsOut("ITEM_DEVIATION", "偏差値");
            } else if (DEVPRI2_STDDEV.equals(_param._deviationPrint)) {
                _svf.VrsOut("ITEM_DEVIATION", "標準偏差");
            }

            // 画像
            _svf.VrsOut("RADER_LEGEND", _param._imagePath + "/" + param.RADER_CHART_LEGEND);
            final File f = new File(_param._imagePath + "/" + barLegendImage);
            if (f.exists()) {
                _svf.VrsOut("BAR_LEGEND", f.getPath());
            }
        }

        /**
         * 成績部分の印刷
         * @param param パラメータ
         * @param student 生徒
         */
        public void printMain(final Param param, final Student student) {

            setForm2();

        	printHeader(param, student);

        	final ExamData dat = student.getExamData(param._thisExam);
            dat._regd = student._regd;
            dat._exam = _param._thisExam;

            // グラフ印字(サブフォームよりも先に印字)
            printBarGraph(dat);
            printRadarGraph(dat);

            // 成績
            printTab(param, dat);

            // 前回の成績
            printBeforeTab(param, student);

            _svf.VrPrint();
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

        private static int dot2pixel(final int dot) {
            final int pixel = dot / 4;   // おおよそ4分の1程度でしょう。

            /*
             * 少し大きめにする事でSVFに貼り付ける際の拡大を防ぐ。
             * 拡大すると粗くなってしまうから。
             */
            return (int) (pixel * 1.3);
        }

        public void printBarGraph(final ExamData dat) {
            // グラフ用のデータ作成
            final String avgMsg = _param.isGakunen() ? "学年" : _param.isCoursegroup() ? "グループ" : _param.isCourse() ? "コース" : _param.isHr() ? "クラス" : "学科";
            final DefaultCategoryDataset scoreDataset = new DefaultCategoryDataset();
            final DefaultCategoryDataset avgDataset = new DefaultCategoryDataset();
            int i = 0;
            for (final Record record : dat._record.values()) {
                if (dat._attendSubclasses.contains(record._subClass)) {
                    continue; // 元科目は非表示
                }
                scoreDataset.addValue(record._graphScore, "本人得点", record._subClass._abbv);
                final AverageDat avgDat = dat._exam._averageDat.get(avgDatKey(_param, dat._regd, record._subClass._code));
                final BigDecimal graphAvg = (null == avgDat) ? null : avgDat._graphAvg;

                avgDataset.addValue(graphAvg, avgMsg + "平均点", record._subClass._abbv);

                log.info("棒グラフ⇒" + record._subClass + ":素点=" + record._score + "(" + record._graphScore + ")" + ((avgDat == null) ? "" : ", 平均=" + avgDat._avg + "(" + avgDat._graphAvg + ")"));
                if (i++ > BAR_GRAPH_MAX_ITEM) {
                    break;
                }
            }

            try {
                // チャート作成
                final JFreeChart chart = createBarChart(scoreDataset, avgDataset);

                // グラフのファイルを生成
                final File outputFile = graphImageFile(chart, 1940, 930);
                _param._graphFiles.add(outputFile);

                // グラフの出力
                _svf.VrsOut("BAR", outputFile.toString());
                
                if (_param._isTosajoshi) {
                    _svf.VrsOut("BAR_TITLE", "得点率（％）グラフ");
                    _svf.VrsOut("BAR_LABEL", "得点率（％）");
                    _svf.VrsOut("BAR_SCORE_TITLE", "本人得点率（％）");
                    _svf.VrsOut("BAR_AVG_TITLE", avgMsg + "平均");
                } else {
                    _svf.VrsOut("BAR_TITLE", "得点グラフ");
                    _svf.VrsOut("BAR_LABEL", "得点");
                    _svf.VrsOut("BAR_SCORE_TITLE", "本人得点");
                    _svf.VrsOut("BAR_AVG_TITLE", avgMsg + "平均点");
                }

            } catch (Throwable t) {
                log.fatal("error or exception!", t);
            }
        }

        private JFreeChart createBarChart(
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

        private void printRadarGraph(final ExamData dat) {
        	
            _svf.VrsOut("RADER_TITLE", "教科間バランス");

            // データ作成
            final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            for (final Record record : dat._record.values()) {
                if (dat._attendSubclasses.contains(record._subClass)) {
                    continue; // 元科目は非表示
                }
                setDataset(dataset, record);
            }

            // チャート作成
            try {
                final JFreeChart chart = createRaderChart(dataset);

                // グラフのファイルを生成
                final File outputFile = graphImageFile(chart, 930, 822);
                _param._graphFiles.add(outputFile);

                // グラフの出力
                if (0 < dataset.getColumnCount()) {
                    _svf.VrsOut("RADER", outputFile.toString());
                }
            } catch (Throwable t) {
                log.fatal("error or exception!", t);
            }
        }

        private void setDataset(final DefaultCategoryDataset dataset, final Record record) {
            log.info("レーダーグラフ⇒" + record._subClass + ", " + record._deviation);
            if (record._deviation == null) {
                return;
            }
//            final Class clazz = (Class) _param._classes.get(record._subClass.getClassCd());
//            final String name = (null == clazz) ? "???" : clazz._abbv;
//            final String name = (null == clazz) ? record._subClass._abbv : clazz._abbv;
            final String name = record._subClass._abbv;
            dataset.addValue(record._deviation, "本人偏差値", name);// MAX80, MIN20

            dataset.addValue(50.0, "偏差値50", name);
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

        public void printTab(final Param param, final ExamData dat) {

        	final List<SubClass> subclassList = new ArrayList<SubClass>();
            for (final SubClass subclass : dat._subclasses.values()) {

//                if ("90".equals(subClass.getClassCd())) {
//                    continue;
//                }

//                final boolean hasSubclass5 = (null != _param._fiveSubclass && _param._fiveSubclass.contains(subClass._code));
//                final boolean hasSubclass3 = (null != _param._threeSubclass && _param._threeSubclass.contains(subClass._code));
//                if (!hasSubclass5 && !hasSubclass3) {
//                    continue;
//                }
                if (dat._attendSubclasses.contains(subclass)) {
                    if (_param._isOutputDebug) {
                    	log.info(" not print subClass : " + subclass._code + ":" + subclass._name);
                    }
                    continue; // 元科目は非表示
                }
                subclassList.add(subclass);

                if (subclassList.size() >= TABLE_SUBCLASS_MAX) {
                    break;
                }
            }

            final List<BigDecimal> devList = new ArrayList<BigDecimal>();
            for (final SubClass subClass : subclassList) {

                final Record record = dat._record.get(subClass);
                if (null != record && null != record._deviation) {
                	devList.add(record._deviation);
                }
            }

            final Record rec9 = dat._recordOther.get(ALL9);
            if (null != rec9) {
                _svf.VrsOut("SCORE5",     rec9.getScore());
                //指示画面で順位を出力しないのチェックを"付けていない"なら出力
                if (!_param._rankNotPrint) {
                    //土佐女子高校の場合、さらにチェックを行う。
                    if (_param._isTosajoshi && "H".equals(_param._schoolKind)) {
                        //2019年1学期かつ指示画面選択状態が下記3つの出力条件を指定した場合"以外は"、全教科の学年順位を出力する。
                        //1.平均点・順位・...の出力を学年
                        //2.順位の基準点を総合点
                        //3.偏差値を出力する
                        if (!"2019".equals(_param._year) || !"1".equals(_param._semester)
                            || !_param.isGakunen() || !"3".equals(_param._juni) || !DEVPRI1_DEVIATION.equals(_param._deviationPrint)) {
                            _svf.VrsOut("RANK5",      ObjectUtils.toString(rec9._rank));
                        }
                    } else {
                        _svf.VrsOut("RANK5",      ObjectUtils.toString(rec9._rank));
                    }
                }
                if (DEVPRI1_DEVIATION.equals(_param._deviationPrint)) {
                	if (_param._isTosajoshi) {
                		_svf.VrsOut("DEVIATION5", average(devList));
                	} else {
                		_svf.VrsOut("DEVIATION5", ObjectUtils.toString(rec9._deviation));
                	}
                }
            }

            final AverageDat avg9 = dat._exam._averageDatOther.get(avgDatKey(_param, dat._regd, ALL9));
            if (null != avg9) {
                if (!_param._isTosajoshi || !"H".equals(param._schoolKind)) {
                    _svf.VrsOut("AVERAGE5",  sishaGonyu(avg9._avg));
                    _svf.VrsOut("MAX_SCORE5", ObjectUtils.toString(avg9._highScore));
                }
                _svf.VrsOut("EXAMINEE5", ObjectUtils.toString(avg9._count));
                if (DEVPRI2_STDDEV.equals(_param._deviationPrint)) {
                    _svf.VrsOut("DEVIATION5", sishaGonyu(avg9._stddev));
                }
            }

            for (final SubClass subclass : subclassList) {

                final Record record = dat._record.get(subclass);

                // 教科
                if (null != subclass._classname) {
                    _svf.VrsOut("CLASS", subclass._classname);
                } else {
                    final Class clazz = param._classes.get(subclass.getClassCd());
                	if (null == clazz) {
                        log.info(" no clazz:" + subclass.getClassCd());
                    } else {
                        _svf.VrsOut("CLASS", clazz._name);
                    }
                }

                // 科目
                final int mojisu1 = getFieldKeta(_param, "SUBCLASS") / 2; // 縦書きなので2で割る
                final int mojisu2 = getFieldKeta(_param, "SUBCLASS2") / 2;
                final int minmojisu = Math.min(mojisu1, mojisu2);
				if (0 < minmojisu && minmojisu < subclass._name.length() && mojisu1 < mojisu2) {
                	_svf.VrsOut("SUBCLASS2", subclass._name);
                } else {
                	_svf.VrsOut("SUBCLASS", subclass._name);
                }

                if (null != record) {
                    if (record.isUnderScore() && !_param._isKumamoto) {
                        _svf.VrsOut("SCORE", "(" + record.getScore() + ")");
                    } else {
                        _svf.VrsOut("SCORE", record.getScore());
                    }
                    //指示画面で順位を出力しないのチェックを"付けていない"なら出力
                    if (!_param._rankNotPrint) {
                        //土佐女子高校の場合、さらにチェックを行う。
                        if (_param._isTosajoshi && "H".equals(_param._schoolKind)) {
                            //2019年1学期かつ指示画面選択状態が下記3つの出力条件を指定した場合"以外は"、全教科の学年順位を出力する。
                            //1.平均点・順位・...の出力を学年
                            //2.順位の基準点を総合点
                            //3.偏差値を出力する
                            if (!"2019".equals(_param._year) || !"1".equals(_param._semester)
                                || !_param.isGakunen() || !"3".equals(_param._juni) || !DEVPRI1_DEVIATION.equals(_param._deviationPrint)) {
                                _svf.VrsOut("RANK",      ObjectUtils.toString(record._rank));
                            }
                        } else {
                            _svf.VrsOut("RANK",      ObjectUtils.toString(record._rank));
                        }
                    }
                    if (DEVPRI1_DEVIATION.equals(_param._deviationPrint)) {
                        _svf.VrsOut("DEVIATION", ObjectUtils.toString(record._deviation));
                    }

                    final AverageDat avgDat = dat._exam._averageDat.get(avgDatKey(_param, dat._regd, subclass._code));
                    if (null != avgDat) {
                        _svf.VrsOut("AVERAGE",  sishaGonyu(avgDat._avg));
                        _svf.VrsOut("MAX_SCORE", ObjectUtils.toString(avgDat._highScore));
                        _svf.VrsOut("EXAMINEE", ObjectUtils.toString(avgDat._count));    // 受験者数
                        if (DEVPRI2_STDDEV.equals(_param._deviationPrint)) {
                            _svf.VrsOut("DEVIATION", sishaGonyu(avgDat._stddev));
                        }
                    }
                }

                _svf.VrEndRecord();
            }
            if (subclassList.size() == 0) {
                _svf.VrsOut("CLASS", "\n");
                _svf.VrEndRecord(); // データが無い時もこのコマンドを発行しないと印刷されないから
            }
        }

        public void printBeforeTab(final Param param, final Student student) {

            final ExamData bef = student.getExamData(param._beforeExam);
            final Regd regd = student._beforeRegd;
            final Exam exam = _param._beforeExam;

            final boolean rankPrint;
            if (param._isMeiji) {
                rankPrint = ("1".equals(exam._semester) && NumberUtils.isDigits(exam._grade) && 1 == Integer.parseInt(exam._grade)) ? false : !_param._rankNotPrint;
            } else {
                rankPrint = !_param._rankNotPrint;
            }

        	final List<SubClass> subclassList = new ArrayList<SubClass>();
            for (final SubClass subclass : bef._subclasses.values()) {

//                if ("90".equals(subClass.getClassCd())) {
//                    continue;
//                }

//                final boolean hasSubclass5 = (null != _param._fiveSubclass && _param._fiveSubclass.contains(subClass._code));
//                final boolean hasSubclass3 = (null != _param._threeSubclass && _param._threeSubclass.contains(subClass._code));
//                if (!hasSubclass5 && !hasSubclass3) {
//                    continue;
//                }
                if (bef._attendSubclasses.contains(subclass)) {
                    if (_param._isOutputDebug) {
                    	log.info(" not print subClass : " + subclass._code + ":" + subclass._name);
                    }
                    continue; // 元科目は非表示
                }
                subclassList.add(subclass);

                if (subclassList.size() >= TABLE_SUBCLASS_MAX) {
                    break;
                }
            }

            final Record rec9 = bef._recordOther.get(ALL9);
            if (null != rec9) {
                _svf.VrsOut("PRE_SCORE5",     rec9.getScore());
                if (rankPrint) {
                    _svf.VrsOut("PRE_RANK5",      ObjectUtils.toString(rec9._rank));
                }
            }

            final AverageDat avg9 = exam._averageDatOther.get(avgDatKey(_param, regd, ALL9));
            if (null != avg9) {
            	_svf.VrsOut("PRE_AVERAGE5", sishaGonyu(avg9._avg));
            }

            for (final SubClass subclass : subclassList) {

                final Record record = bef._record.get(subclass);

                // 教科
                if (null != subclass._classname) {
                    _svf.VrsOut("PRE_CLASS", subclass._classname);
                } else {
                    final Class clazz = param._classes.get(subclass.getClassCd());
                	if (null == clazz) {
                        log.info(" no clazz:" + subclass.getClassCd());
                    } else {
                        _svf.VrsOut("PRE_CLASS", clazz._name);
                    }
                }

                // 科目
                final int mojisu1 = getFieldKeta(_param, "PRE_SUBCLASS") / 2; // 縦書きなので2で割る
                final int mojisu2 = getFieldKeta(_param, "PRE_SUBCLASS2") / 2;
                final int minmojisu = Math.min(mojisu1, mojisu2);
				if (0 < minmojisu && minmojisu < subclass._name.length() && mojisu1 < mojisu2) {
                	_svf.VrsOut("PRE_SUBCLASS2", subclass._name);
                } else {
                	_svf.VrsOut("PRE_SUBCLASS", subclass._name);
                }

                if (null != record) {
                    if (record.isUnderScore() && !_param._isKumamoto) {
                        _svf.VrsOut("PRE_SCORE", "(" + record.getScore() + ")");
                    } else {
                        _svf.VrsOut("PRE_SCORE", record.getScore());
                    }
                    if (rankPrint) {
                        _svf.VrsOut("PRE_RANK",      ObjectUtils.toString(record._rank));
                    }

                    final AverageDat avgDat = exam._averageDat.get(avgDatKey(_param, regd, subclass._code));
                    if (null != avgDat) {
                        _svf.VrsOut("PRE_AVERAGE", sishaGonyu(avgDat._avg));
                    }
                }

                _svf.VrEndRecord();
            }
            if (subclassList.size() == 0) {
                _svf.VrsOut("PRE_CLASS", "\n");
                _svf.VrEndRecord(); // データが無い時もこのコマンドを発行しないと印刷されないから
            }
        }
    }

    private static class Regd {
        private final String _grade;
        private final String _hrClass;
        private final String _attendNo;
        private final String _hrName;

        private final String _courseCd;
        private final String _majorCd;
        private final String _courseCode;
        private final String _coursegroupCd;

        Regd(
                final String grade,
                final String hrClass,
                final String attendNo,
                final String hrName,
                final String courseCd,
                final String majorCd,
                final String courseCode,
                final String coursegroupCd
        ) {
            _grade = grade;
            _hrClass = hrClass;
            _attendNo = attendNo;
            _hrName = hrName;
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

        public String attendNoStr(final Param param) {
            if (!StringUtils.isNumeric(_attendNo)) {
                return "";
            }
            final StringBuffer stb = new StringBuffer();
            final String n = String.valueOf(Integer.parseInt(_attendNo));
            for (int i = 0; i < n.length(); i++) {
                stb.append(param._nums.get(n.substring(i, i + 1)));
            }
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

        private Exam _exam = new Exam(null, null, null, null, null);

        private Regd _regd = new Regd(null, null, null, null, null, null, null, null);

        /** 模試データにある科目. */
        private final Map<String, SubClass> _subclasses = new TreeMap<String, SubClass>();

        /** 成績データ。 */
        private final Map<SubClass, Record> _record = new TreeMap<SubClass, Record>();

        /** 元科目。 */
        private final Set<SubClass> _attendSubclasses = new TreeSet<SubClass>();

        /** 成績データ。3教科,5教科用 */
        private final Map<String, Record> _recordOther = new HashMap<String, Record>();
    }

    private static class Student implements Comparable<Student> {
        private final String _schregno;

        private final Regd _regd;

        private Regd _beforeRegd = new Regd(null, null, null, null, null, null, null, null);

        private final Map<Exam, Regd> _regds = new HashMap<Exam, Regd>();

        private final String _name;

        private final Map<String, ExamData> _datas = new HashMap<String, ExamData>();

        private Student(final String schregno, final Regd regd, final String name) {
            _schregno = schregno;
            _regd = regd;
            _name = name;
        }

        public final ExamData getExamData(final Exam exam) {
        	return (ExamData) _datas.get(exam.getExamKey());
        }

        public String toString() {
            return _schregno + "/" + _name;
        }

        public int compareTo(final Student that) {
            return _regd.compare(that._regd);
        }

        private static List<Student> createStudents(final DB2UDB db2, final Param param) throws SQLException {

        	final String[] schregnos = param.getScregnos(db2);
            final List<Student> rtn = new LinkedList<Student>();

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = Student.studentsSQL(schregnos, param._year, param._semester);
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
                    final String coursegroupcd = rs.getString("coursegroupcd");
                    final String name = rs.getString("name");

                    final Regd regd = new Regd(grade, hrclass, attendno, hrName, coursecd, majorcd, coursecode, coursegroupcd);

                    final Student student = new Student(
                            schregno,
                            regd,
                            name
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

            try {
                final Exam exam = param._beforeExam;
                final String sql = Student.studentsSQL(schregnos, exam._year, exam._semester);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String schregno = rs.getString("schregno");
                    Student student = null;
                    for (final Student s : rtn) {
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
                    final String coursecd = rs.getString("coursecd");
                    final String majorcd = rs.getString("majorcd");
                    final String coursecode = rs.getString("coursecode");
                    final String coursegroupcd = rs.getString("coursegroupcd");

                    final Regd beforeRegd = new Regd(grade, hrclass, attendno, hrName, coursecd, majorcd, coursecode, coursegroupcd);

                    student._beforeRegd = beforeRegd;

                }
            } catch (final SQLException e) {
                log.error("生徒の前回の基本情報取得でエラー");
                throw e;
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            Collections.sort(rtn);
            for (final Student s : rtn) {
                s._datas.put(param._thisExam.getExamKey(), new ExamData());
                s._datas.put(param._beforeExam.getExamKey(), new ExamData());
                s._regds.put(param._thisExam, s._regd);
                s._regds.put(param._beforeExam, s._beforeRegd);
            }
            return rtn;
        }

        private static String studentsSQL(final String[] selected, final String year, final String semester) {
            final String students = SQLUtils.whereIn(true, selected);

            final String sql;
            sql = "SELECT"
                + "  t1.schregno,"
                + "  t1.grade,"
                + "  t1.hr_class,"
                + "  t1.attendno,"
                + "  t1.hr_name,"
                + "  t1.coursecd,"
                + "  t1.majorcd,"
                + "  t1.coursecode,"
                + "  l2.group_cd as coursegroupcd,"
                + "  t1.name"
                + " FROM"
                + "  v_schreg_info t1 "
                + "  left join course_group_cd_dat l1 on l1.year = t1.year "
                + "      and l1.grade = t1.grade "
                + "      and l1.coursecd = t1.coursecd "
                + "      and l1.majorcd = t1.majorcd "
                + "      and l1.coursecode = t1.coursecode "
                + "  left join course_group_cd_hdat l2 on l2.year = l1.year "
                + "      and l2.grade = l1.grade "
                + "      and l2.group_cd = l1.group_cd "
                + " WHERE"
                + "  t1.year='" + year + "' AND"
                + "  t1.semester='" + semester + "' AND"
                + "  t1.schregno IN " + students
                ;
            return sql;
        }
    }

    /**
     * 教科。
     */
    private static class Class {
        final String _code;
        final String _schoolKind;
        final String _name;
        final String _abbv;

        public Class(final String code, final String schoolKind, final String name, final String abbv) {
            _code = code;
            _schoolKind = schoolKind;
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
    private static class SubClass implements Comparable<SubClass> {
        final String _code;
        final String _classname;
        final String _classabbv;
        final String _name;
        final String _abbv;

        public SubClass(final String code, final String classname, final String classabbv, final String name, final String abbv) {
            _code = code;
            _classname = classname;
            _classabbv = classabbv;
            _name = name;
            _abbv = abbv;
        }

        public SubClass(final String code) {
            this(code, "", "", "xxx", "yyy");
        }

        public String getClassCd() {
            return _code.substring(0, 2);
        }

        public String toString() {
            return _code + ":" + _abbv;
        }

        public int compareTo(final SubClass that) {
            return this._code.compareTo(that._code);
        }

        private static void loadSubClasses(final DB2UDB db2, final Param param, final List<Student> students, final Exam exam) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                ps = db2.prepareStatement(sqlSubClasses(exam));
                for (final Student student : students) {
                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        final String subClasscd = rs.getString("SUBCLASSCD");

                        final SubClass subClass = param._subClasses.get(subClasscd);
                        if (null != subClass) {
                        	student.getExamData(exam)._subclasses.put(subClasscd, subClass);
                        }
                    }
                }
            } catch (final SQLException e) {
                log.fatal("模試データにある科目の取得でエラー");
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        private static String sqlSubClasses(final Exam exam) {
            final String rtn;
            rtn = "select"
                    + "  distinct T1.PROFICIENCY_SUBCLASS_CD as SUBCLASSCD"
                    + " from"
                    + "  PROFICIENCY_DAT T1"
                    + "  inner join PROFICIENCY_SUBCLASS_MST T2 on T2.PROFICIENCY_SUBCLASS_CD = T1.PROFICIENCY_SUBCLASS_CD "
                    + " where"
                    + "  T1.year='" + exam._year + "' AND"
                    + "  T1.semester='" + exam._semester + "' AND"
                    + "  T1.proficiencydiv='" + exam._proficiencydiv + "' AND"
                    + "  T1.proficiencycd='" + exam._proficiencycd + "' AND"
                    + "  T1.SCHREGNO = ? "
                    ;
            log.debug("模試データにある科目のSQL=" + rtn);
            return rtn;
        }

    }

    private class Record {
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
            final String passScore = ObjectUtils.toString(_passScore);
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

    private static String average(final List<BigDecimal> bdList) {
    	BigDecimal sum = new BigDecimal(0);
    	int n = 0;
    	for (final BigDecimal bd : bdList) {
    		if (null != bd) {
    			sum = sum.add(bd);
    			n += 1;
    		}
    	}
    	return n == 0 ? null : sum.divide(new BigDecimal(n), 1, BigDecimal.ROUND_HALF_UP).toString();
    }

    private static String sishaGonyu(final BigDecimal bd) {
        return bd.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
    }

    private static String avgDatKey(final Param param, final Regd regd, final String subclasscd) {
        final String key;
        if (param.isHr()) {
            key = subclasscd + regd.hrKey();
        } else if (param.isCourse()) {
            key = subclasscd + regd.courseKey();
        } else if (param.isMajor()) {
            key = subclasscd + regd.majorKey();
        } else if (param.isCoursegroup()) {
            key = subclasscd + regd.coursegroupKey();
        } else {
            key = subclasscd;
        }
        return key;
    }

    private class AverageDat {
        final SubClass _subClass;
        final BigDecimal _avg;
        final BigDecimal _graphAvg;
        final BigDecimal _stddev;
        final Integer _highScore;
        final Integer _count;
        final String _coursecd;
        final String _majorcd;
        final String _coursecode;

        private AverageDat(
                final SubClass subClass,
                final BigDecimal avg,
                final BigDecimal graphAvg,
                final BigDecimal stddev,
                final Integer highScore,
                final Integer count,
                final String coursecd,
                final String majorcd,
                final String coursecode
        ) {
            _subClass = subClass;
            _avg = avg;
            _graphAvg = graphAvg;
            _stddev = stddev;
            _highScore = highScore;
            _count = count;
            _coursecd = coursecd;
            _majorcd = majorcd;
            _coursecode = coursecode;
        }

        public String toString() {
            return _subClass + "/" + _avg + "/" + _count;
        }
    }

    private static class Param {

        /** レーダーチャート凡例画像. */
        private final String RADER_CHART_LEGEND = "RaderChartLegend.png";
        /** 棒グラフ凡例画像1(学年). */
        private final String BAR_CHART_LEGEND1 = "BarChartLegendGrade.png";
        /** 棒グラフ凡例画像2(クラス). */
        private final String BAR_CHART_LEGEND2 = "BarChartLegendHr.png";
        /** 棒グラフ凡例画像3(コース). */
        private final String BAR_CHART_LEGEND3 = "BarChartLegendCourse.png";
        /** 棒グラフ凡例画像4(学科). */
        private final String BAR_CHART_LEGEND4 = "BarChartLegendMajor.png";
        /** 棒グラフ凡例画像5(コースグループ). */
        private final String BAR_CHART_LEGEND5 = "BarChartLegendCourseGroup.png";

        private final String _year;
        private final String _semester;
        final String _grade;
        final String _schoolKind;

        private final String _formGroupDiv;

        /** [クラス指定 or 生徒指定]の値。 */
        private final String _div;
        /** クラス or 生徒。 */
        private final String[] _values;

        private final String _submitDate;
        private final String _loginDate;

        /** クラス指定 or 生徒指定。 */
        private final boolean _isClassMode;

        /** 学校名。 */
        private String _schoolName;
        /** 担任職種名。 */
        private String _remark2;
        /** 偏差値を印字するか? */
        private final String _deviationPrint;
        /** 順位を印字しないか? */
        private final boolean _rankNotPrint;
        /** 保護者欄を印刷しないか? */
        private final boolean _hogoshaNotPrint;
        /** 表紙を印刷しないか? */
        private final boolean _hyoshiNotPrint;
        /** 順位の基準点 1:総合点,2:平均点,3:偏差値,11:傾斜総合点 */
        private final String _rankDataDiv;
        /** 平均の基準点 1:得点,2:傾斜総合点 */
        private final String _avgDataDiv;
        private final Map<String, String> _staffs = new HashMap<String, String>();

        private final Map<String, String> _nums = new HashMap<String, String>();

        private final boolean _useCLASS_MST_SCHOOL_KIND;

        /** 教科マスタ。 */
        private Map<String, Class> _classes;
        /** 模試科目マスタ。 */
        private Map<String, SubClass> _subClasses;
        private final String _imagePath;

//        private final Map _subclassGroup3 = new HashMap();
//        private final Map _subclassGroup5 = new HashMap();
//        private final MultiMap _subclassGroupDat3 = new MultiHashMap();
//        private final MultiMap _subclassGroupDat5 = new MultiHashMap();

//        /** レーダーチャートの科目. */
//        private List _fiveSubclass = new ArrayList();
//        private List _threeSubclass = new ArrayList();

        private boolean _isKumamoto;
        private boolean _isMeiji;
        private boolean _isChiyodaKudan;
        private boolean _isTosajoshi;

        private final Exam _thisExam;
        private Exam _beforeExam = new Exam(null, null, null, null, null);
        private boolean _isOutputDebug;

        private final String _knjh563PrintScoreKansannashi;

        /** グラフイメージファイルの Set&lt;File&gt; */
        private final Set<File> _graphFiles = new HashSet<File>();

        private final String _juni;
        private final boolean _exist2019Flg;
        public Param(final HttpServletRequest request, final DB2UDB db2) {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _grade = request.getParameter("GRADE");
            _div = request.getParameter("CATEGORY_IS_CLASS");
            _values = request.getParameterValues("CATEGORY_SELECTED");
            _submitDate = request.getParameter("SUBMIT_DATE");
            _loginDate = request.getParameter("LOGIN_DATE");

            _isClassMode = "1".equals(_div);

            _formGroupDiv = request.getParameter("FORM_GROUP_DIV");
            _deviationPrint = request.getParameter("DEVIATION_PRINT");
            _hogoshaNotPrint = "1".equals(request.getParameter("HOGOSHA_PRINT"));
            _hyoshiNotPrint = "1".equals(request.getParameter("HYOSHI_PRINT"));

            _imagePath = request.getParameter("IMAGE_PATH");
            _nums.put("0", "０");
            _nums.put("1", "１");
            _nums.put("2", "２");
            _nums.put("3", "３");
            _nums.put("4", "４");
            _nums.put("5", "５");
            _nums.put("6", "６");
            _nums.put("7", "７");
            _nums.put("8", "８");
            _nums.put("9", "９");

            imageFileCheck(RADER_CHART_LEGEND);
            imageFileCheck(BAR_CHART_LEGEND1);
            imageFileCheck(BAR_CHART_LEGEND2);
            imageFileCheck(BAR_CHART_LEGEND3);
            imageFileCheck(BAR_CHART_LEGEND4);
            imageFileCheck(BAR_CHART_LEGEND5);

            _isOutputDebug = "1".equals(getDbPrginfoProperties(db2, "outputDebug"));
            _thisExam = new Exam(_year, _semester, request.getParameter("PROFICIENCYDIV"), request.getParameter("PROFICIENCYCD"), request.getParameter("GRADE"));
            _knjh563PrintScoreKansannashi = request.getParameter("knjh563PrintScoreKansannashi");
            _schoolKind = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND GRADE = '" + _grade + "' "));
            _useCLASS_MST_SCHOOL_KIND = KnjDbUtils.setTableColumnCheck(db2, "CLASS_MST", "SCHOOL_KIND") && KnjDbUtils.setTableColumnCheck(db2, "PROFICIENCY_SUBCLASS_MST", "SCHOOL_KIND");

            _juni = request.getParameter("JUNI");

            final String z010 = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' "));
            _isChiyodaKudan = "chiyoda".equals(z010) ? true : false;
			_isTosajoshi = "tosajoshi".equals(z010) ? true : false;
            if (_isTosajoshi && "H".equals(_schoolKind) && "2019".equals(_year) && "1".equals(_semester)) {
                final String existChk = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT CASE WHEN PROFICIENCYDIV IS NOT NULL THEN '1' ELSE '' END FROM PROFICIENCY_YMST WHERE YEAR = '2019' "
                                                                                   + "AND SEMESTER = '1' AND GRADE = '" + _grade + "' AND PROFICIENCYDIV = '" + _thisExam._proficiencydiv
                                                                                   + "' AND PROFICIENCYCD = '" + _thisExam._proficiencycd + "' "));
                if (!"".equals(StringUtils.defaultString(existChk, ""))) {
                	_exist2019Flg = true;
                    _rankDataDiv = "01";
                } else {
                	_exist2019Flg = false;
                    _rankDataDiv = "03";
                }
                if ("4".equals(request.getParameter("JUNI"))) {
                    _avgDataDiv = "2";
                } else {
                    _avgDataDiv = "1";
                }
            } else {
            	_exist2019Flg = false;
                if ("4".equals(request.getParameter("JUNI"))) {
                    _rankDataDiv = "11";
                    _avgDataDiv = "2";
                } else {
                    final String rankDivTemp = request.getParameter("useKnjd106cJuni" + request.getParameter("JUNI"));
                    final String rankDataDiv0 = (rankDivTemp == null) ? request.getParameter("JUNI") : rankDivTemp;
                    _rankDataDiv = (null != rankDataDiv0 && rankDataDiv0.length() < 2 ? "0" : "") + rankDataDiv0;
                    _avgDataDiv = "1";
                }
            }
            if (_exist2019Flg) {
                _rankNotPrint = true;
            } else {
                _rankNotPrint = "1".equals(request.getParameter("JUNI_PRINT"));
            }

        }

        private void removeImageFiles() {
            for (final File imageFile : _graphFiles) {
                if (null == imageFile) {
                    continue;
                }
                log.fatal("グラフ画像ファイル削除:" + imageFile.getName() + " : " + imageFile.delete());
            }
        }

        private static String getDbPrginfoProperties(final DB2UDB db2, final String propName) {
        	try {
        		return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJH563' AND NAME = '" + propName + "' "));
        	} catch (Throwable e) {
        		log.error("exception", e);
        	}
        	return null;
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
            PreparedStatement ps = null;
            ResultSet rs = null;
            final List<String> result = new ArrayList<String>();

            if (!_isClassMode) {
                return _values;
            }

            // 年組から学籍番号たちを得る
            for (int i = 0; i < _values.length; i++) {
                final String grade = StringUtils.split(_values[i], separator)[0];
                final String room = StringUtils.split(_values[i], separator)[1];

                try {
                    ps = db2.prepareStatement(hrToStudentsSQL(grade, room));
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        final String schregno = rs.getString("schregno");
                        result.add(schregno);
                    }
                } catch (final SQLException e) {
                    log.error("年組から学籍番号たちを得る際にエラー");
                    throw e;
                }
            }
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);

            final String[] rtn = new String[result.size()];
            int i = 0;
            for (final String schregno : result) {
                rtn[i++] = schregno;
            }

            return rtn;
        }

        public void load(final DB2UDB db2) throws SQLException {
        	final String z010 = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT NAME1 FROM V_NAME_MST WHERE NAMECD1 = 'Z010' AND YEAR = '"  + _year  + "' AND NAMECD2 = '00' "));
            _isKumamoto = "kumamoto".equals(z010);
            _isMeiji = "meiji".equals(z010);
            //_isTosajoshi = "tosajoshi".equals(z010); //paramの際に利用するため、設定値を移動
            loadCertifSchool(db2);
            loadRegdHdat(db2);
            loadBefore(db2);
            _thisExam.loadTitle(db2);
            _beforeExam.loadTitle(db2);
            log.debug(" thisExam   = "+ _thisExam);
            log.debug(" beforeExam = "+ _beforeExam);
            _classes = setClasses(db2);
            _subClasses = setSubClasses(db2);
        }

        private Map<String, SubClass> setSubClasses(final DB2UDB db2) throws SQLException {
            final Map<String, SubClass> rtn = new HashMap<String, SubClass>();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sqlSubClasses());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String code = rs.getString("SUBCLASSCD");
                    final String classname = rs.getString("CLASSNAME");
                    final String classabbv = rs.getString("CLASSABBV");
                    final String name = rs.getString("NAME");
                    final String abbv = rs.getString("SUBCLASSABBV");
                    rtn.put(code, new SubClass(code, classname, classabbv, name, abbv));
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            log.debug("模試科目マスタ総数=" + rtn.size());
            return rtn;
        }

        public String sqlSubClasses() {
            return "select"
                    + "   t1.PROFICIENCY_SUBCLASS_CD as SUBCLASSCD,"
                    + "   t2.classname ,"
                    + "   t2.classabbv ,"
                    + "   t1.SUBCLASS_NAME as NAME,"
                    + "   t1.SUBCLASS_ABBV as SUBCLASSABBV"
                    + " from PROFICIENCY_SUBCLASS_MST t1 "
                    + " left join CLASS_MST t2 on t2.classcd = t1.classcd "
            		+ (_useCLASS_MST_SCHOOL_KIND ? " AND T2.SCHOOL_KIND = T1.SCHOOL_KIND " : "")
                    + " order by"
                    + "   t1.PROFICIENCY_SUBCLASS_CD"
                ;
        }

        private Map<String, Class> setClasses(final DB2UDB db2) throws SQLException {
            final Map<String, Class> rtn = new HashMap<String, Class>();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sqlClasses());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String code = rs.getString("CLASSCD");
                    final String schoolKind = _useCLASS_MST_SCHOOL_KIND ? rs.getString("SCHOOL_KIND") : "";
                    final String name = rs.getString("CLASSNAME");
                    final String abbv = rs.getString("CLASSABBV");
                    rtn.put(code + schoolKind, new Class(code, schoolKind, name, abbv));
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            log.debug("教科マスタ=" + rtn);
            return rtn;
        }

        public String sqlClasses() {
            return "select"
                    + "   CLASSCD,"
            		+ (_useCLASS_MST_SCHOOL_KIND ? " SCHOOL_KIND, " : "")
                    + "   CLASSNAME,"
                    + "   CLASSABBV"
                    + " from V_CLASS_MST"
                    + " where"
                    + "   YEAR = '" + _year + "'"
                    + " order by"
                    + "   CLASSCD"
                ;
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
                log.fatal(" before = "  + _beforeExam);
            } catch (final SQLException e) {
                log.error("以前の考査取得エラー。");
            }
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }

        private void loadRegdHdat(final DB2UDB db2) {
            final String sql;
            sql = "SELECT t1.grade ||  t1.hr_class AS code, t2.staffname"
                + " FROM schreg_regd_hdat t1 INNER JOIN v_staff_mst t2 ON t1.year=t2.year AND t1.tr_cd1=t2.staffcd"
                + " WHERE t1.year = '" + _year + "'"
                + " AND t1.semester = '" + _semester + "'";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String code = rs.getString("code");
                    final String name = rs.getString("staffname");
                    _staffs.put(code, name);
                }
            } catch (final SQLException e) {
                log.warn("担任名の取得でエラー");
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private void loadCertifSchool(final DB2UDB db2) throws SQLException {
            final String key = "J".equals(_schoolKind) ? "110" : "109";

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

        private String hrToStudentsSQL(final String grade, final String room) {
            return " select"
                    + "    SCHREGNO as schregno"
                    + " from"
                    + "    SCHREG_REGD_DAT"
                    + " where"
                    + "    YEAR = '" + _year + "' and"
                    + "    SEMESTER = '" + _semester + "' and"
                    + "    GRADE = '" + grade + "' and"
                    + "    HR_CLASS = '" + room + "'"
                    ;
        }
    }
} // KNJH563

// eof
