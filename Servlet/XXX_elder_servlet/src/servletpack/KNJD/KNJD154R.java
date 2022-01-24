// kanji=漢字
/*
 * $Id: 4366c825514e58a184b8ad75b6824c2dee1427a9 $
 *
 * 作成日: 2009/08/20
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
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
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.Arc2D;
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
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.ServletException;
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
import org.jfree.chart.entity.CategoryItemEntity;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.PlotState;
import org.jfree.chart.plot.SpiderWebPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.ui.HorizontalAlignment;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.VerticalAlignment;
import org.jfree.util.TableOrder;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJServletpacksvfANDdb2;
import servletpack.KNJZ.detail.KNJ_EditEdit;

/**
 * 学校教育システム 賢者 [成績管理]  成績個票
 */

public class KNJD154R {

    private static final Log log = LogFactory.getLog(KNJD154R.class);
    private static final String _555555 = "555555";
    private static final String _999999 = "999999";

    /** グラフイメージファイルの Set&lt;File&gt; */
    private final Set _graphFiles = new HashSet();
    
    public void svf_out (
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws ServletException, IOException {

        final Vrw32alp svf = new Vrw32alp();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        boolean hasData = false;

        final KNJServletpacksvfANDdb2 sd = new KNJServletpacksvfANDdb2();
        sd.setSvfInit(request, response, svf);
        final DB2UDB db2 = sd.setDb(request);
        if (sd.openDb(db2)) {
            log.error("db open error! ");
            return;
        }
        
        final Param param = new Param(request, db2);
        
        try {
            final List students = Student.createStudentList(db2, param);
            
            for (final Iterator it = students.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                
                printMain(svf, param, student);

                hasData = true;
            }
            
            if (!hasData) {
                log.warn("データがありません");
            }
        } catch (Exception e) {
            log.error("Exception", e);
        } finally {
            db2.commit();

            sd.closeSvf(svf, hasData);
            sd.closeDb(db2);
            
            removeImageFiles();
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
    
    private static Map getMappedMap(final Map map, final String key) {
        if (!map.containsKey(key)) {
            map.put(key, new HashMap());
        }
        return (Map) map.get(key);
    }
    
    private static List getMappedList(final Map map, final String key) {
        if (!map.containsKey(key)) {
            map.put(key, new ArrayList());
        }
        return (List) map.get(key);
    }
    
    private static class Student {

        final String _schregno;
        final Map _shokenMap = new HashMap();
        
        private String _attendNo;
        private String _gradeHrName;
        private String _grade;
        private String _courseCd;
        private String _courseName;
        private String _majorCd;
        private String _majorName;
        private String _courseCode;
        private String _courseCodeName;
        private String _groupCd;
        private String _hrName;
        private String _hrNameAbbv;
        private String _name;
        private List _subclassList = Collections.EMPTY_LIST;
        private List _mockSubclassList = Collections.EMPTY_LIST;
        private Map _profMap = new HashMap();

        public Student(final String schregno) {
            _schregno = schregno;
        }
        
        private static Student getStudent(final String schregno, final List students) {
            for (final Iterator it = students.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                if (student._schregno.equals(schregno)) {
                    return student;
                }
            }
            return null;
        }
        
        private Map getSubclassMap() {
            final Map subclassMap = new HashMap();
            for (final Iterator it = _subclassList.iterator(); it.hasNext();) {
                final Subclass subclass = (Subclass) it.next();
                subclassMap.put(subclass._subclasscd, subclass);
            }
            return subclassMap;
        }
        
        private static String getStudentSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append("    SELECT  T1.SCHREGNO, BASE.NAME, BASE.REAL_NAME, CASE WHEN T7.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END AS USE_REAL_NAME ");
            stb.append("          , T1.GRADE, T1.ATTENDNO, T1.SEMESTER, T1.COURSECD, T1.MAJORCD, T1.COURSECODE, T3.GROUP_CD, REGDH.HR_NAME, REGDH.HR_NAMEABBV ");
            stb.append("          , CM.COURSENAME, MM.MAJORNAME, CCM.COURSECODENAME ");
            stb.append(    "FROM    SCHREG_REGD_DAT T1 ");
            stb.append(    "INNER JOIN SEMESTER_MST T2 ON T2.YEAR = T1.YEAR AND T2.SEMESTER = T1.SEMESTER ");
            stb.append(    "LEFT JOIN COURSE_GROUP_CD_DAT T3 ON T3.YEAR = T1.YEAR AND T3.GRADE = T1.GRADE AND T3.COURSECD = T1.COURSECD AND T3.MAJORCD = T1.MAJORCD AND T3.COURSECODE = T1.COURSECODE ");
            stb.append(    "INNER JOIN SCHREG_BASE_MST BASE ON T1.SCHREGNO = BASE.SCHREGNO ");
            stb.append(    "INNER JOIN SCHREG_REGD_HDAT REGDH ON REGDH.YEAR = T1.YEAR AND ");
            stb.append(        "REGDH.SEMESTER = T1.SEMESTER AND ");
            stb.append(        "REGDH.GRADE = T1.GRADE AND REGDH.HR_CLASS = T1.HR_CLASS ");
            stb.append(    "LEFT JOIN COURSE_MST CM ON CM.COURSECD = T1.COURSECD ");
            stb.append(    "LEFT JOIN MAJOR_MST MM ON MM.COURSECD = T1.COURSECD AND MM.MAJORCD = T1.MAJORCD ");
            stb.append(    "LEFT JOIN COURSECODE_MST CCM ON CCM.COURSECODE = T1.COURSECODE ");
            stb.append(    "LEFT JOIN SCHREG_NAME_SETUP_DAT T7 ON T7.SCHREGNO = T1.SCHREGNO AND T7.DIV = '04' ");
            stb.append(    "WHERE   T1.YEAR = '" + param._year + "' ");
            stb.append(        "AND T1.SEMESTER = '"+ param.getRegdSemester() +"' ");
            stb.append(        "AND T1.GRADE || T1.HR_CLASS = '" + param._grade_hr_class + "' ");
            stb.append(        "AND T1.SCHREGNO IN " + SQLUtils.whereIn(true, param._categorySelected) + " ");
//            stb.append(        "AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST S1 ");
//            stb.append(           "WHERE   S1.SCHREGNO = T1.SCHREGNO ");
//            stb.append(               "AND ((S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < CASE WHEN T2.EDATE < '" + param._ctrlDate + "' THEN T2.EDATE ELSE '" + param._ctrlDate + "' END) ");
//            stb.append(               "OR (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE > CASE WHEN T2.EDATE < '" + param._ctrlDate + "' THEN T2.EDATE ELSE '" + param._ctrlDate + "' END)) ) ");
//            stb.append(        "AND NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT S1 ");
//            stb.append(           "WHERE   S1.SCHREGNO = T1.SCHREGNO ");
//            stb.append(              "AND S1.TRANSFERCD IN ('1','2') AND CASE WHEN T2.EDATE < '" + param._ctrlDate + "' THEN T2.EDATE ELSE '" + param._ctrlDate + "' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE) ");
            stb.append("ORDER BY T1.ATTENDNO");

            return stb.toString();
        }

        private static void setHExamRecordRemarkDat(final DB2UDB db2, final Param param, final List students) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     SEMESTER || TESTKINDCD || TESTITEMCD || SCORE_DIV AS TESTCD, REMARK1 ");
                stb.append(" FROM ");
                stb.append("     HEXAM_RECORD_REMARK_SDIV_DAT ");
                stb.append(" WHERE ");
                stb.append("     YEAR = '" + param._year + "' ");
                stb.append("     AND SEMESTER || TESTKINDCD || TESTITEMCD || SCORE_DIV IN " + SQLUtils.whereIn(true, param.getTargetTestKindCds()) + " ");
                stb.append("     AND REMARK_DIV = '4' ");
                stb.append("     AND SCHREGNO = ? ");
                final String sql = stb.toString();
                ps = db2.prepareStatement(sql);

                for (final Iterator it = students.iterator(); it.hasNext();) {
                    final Student student= (Student) it.next();
                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();
                    
                    while (rs.next()) {
                        student._shokenMap.put(rs.getString("TESTCD"), rs.getString("REMARK1"));
                    }
                }
            } catch (SQLException e) {
                log.error("sql exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
        
        private static List createStudentList(final DB2UDB db2, final Param param) {

            PreparedStatement ps = null;
            ResultSet rs = null;
            final List studentList = new ArrayList();
            try {
                final String sqlRegdData = Student.getStudentSql(param);
                log.debug("sqlRegdData = " + sqlRegdData);
                ps = db2.prepareStatement(sqlRegdData);
                rs = ps.executeQuery();
                
                while (rs.next()) {

                    final Student student = new Student(rs.getString("SCHREGNO"));
                    studentList.add(student);

                    student._grade = rs.getString("GRADE");
                    student._courseCd = rs.getString("COURSECD");
                    student._majorCd = rs.getString("MAJORCD");
                    student._courseCode = rs.getString("COURSECODE");
                    student._groupCd = rs.getString("GROUP_CD");
                    
                    student._courseName = rs.getString("COURSENAME");
                    student._courseCodeName = null == rs.getString("COURSECODENAME") ? "" : rs.getString("COURSECODENAME");
                    student._majorName = rs.getString("MAJORNAME");
                    student._hrName = StringUtils.defaultString(rs.getString("HR_NAME"));
                    student._hrNameAbbv = StringUtils.defaultString(rs.getString("HR_NAMEABBV"));
                    final String attendNo = rs.getString("ATTENDNO");
                    student._attendNo = null == attendNo || !NumberUtils.isDigits(attendNo) ? "" : Integer.valueOf(attendNo) + "番";
                    student._name = StringUtils.defaultString("1".equals(rs.getString("USE_REAL_NAME")) ? rs.getString("REAL_NAME") : rs.getString("NAME"));
                    
                    //log.debug("対象の生徒" + student);
                }
            } catch (Exception ex) { 
                log.error("printSvfMain read error! ", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            
            if (studentList.size() == 0) {
                log.warn("対象の生徒がいません");
                return studentList;
            }

            Subclass.setSubclassList(db2, param, studentList);
            Student.setHExamRecordRemarkDat(db2, param, studentList);
            Proficiency.setProficiencyListMap(db2, param, studentList);
            
            return studentList;
        }
    }
    
    private static class Subclass {
        final String _subclasscd;
        final String _classname;
        final String _classabbv;
        final String _subclassname;
        final String _subclassabbv;
        final Map _scoreMap = new HashMap();

        Subclass(
            final String subclasscd,
            final String classname,
            final String classabbv,
            final String subclassname,
            final String subclassabbv
        ) {
            _subclasscd = subclasscd;
            _classname = classname;
            _classabbv = classabbv;
            _subclassname = subclassname;
            _subclassabbv = subclassabbv;
        }

        public String[] getRankDiff(final String beforeTestcd, final String testcd) {
            final String rank = (String) _scoreMap.get("RANK" + testcd);
            String[] printRank = {"", ""};
            if (NumberUtils.isDigits(rank)) {
                final String beforeRank = (String) _scoreMap.get("RANK" + beforeTestcd);
                if (NumberUtils.isDigits(beforeRank)) {
                    final int diff = Integer.parseInt(beforeRank) - Integer.parseInt(rank);
                    printRank[0] = String.valueOf(Math.abs(diff));
                    if (diff > 0) {
                        printRank[1] = "↑";
                    } else if (diff < 0) {
                        printRank[1] = "↓";
                    }
                }
            }
            return printRank;
        }

        private static void setSubclassList(final DB2UDB db2, final Param param, final List studentList) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getSubclassSql(param);
                log.debug(" subclass sql = " + sql);
                ps = db2.prepareStatement(sql);
                
                for (final Iterator stit = studentList.iterator(); stit.hasNext();) {
                    final Student student = (Student) stit.next();
                    student._subclassList = new ArrayList();
                    
                    int p = 0;
                    //log.debug(" schregno = " + student._schregno);
                    ps.setString(++p, student._schregno);
                    
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        final String subclasscd = rs.getString("SUBCLASSCD");
                        Subclass subclass = null;
                        
                        final String score = rs.getString("SCORE");
                        final String avg = rs.getString("AVG");
                        if (null == score && null == avg) {
                            continue;
                        }
                        for (final Iterator it = student._subclassList.iterator(); it.hasNext();) {
                            final Subclass s = (Subclass) it.next();
                            if (s._subclasscd.equals(subclasscd)) {
                                subclass = s;
                                break;
                            }
                        }
                        if (null == subclass) {
                            final String classname = rs.getString("CLASSNAME");
                            final String classabbv = rs.getString("CLASSABBV");
                            final String subclassname = rs.getString("SUBCLASSNAME");
                            final String subclassabbv = rs.getString("SUBCLASSABBV");
                            subclass = new Subclass(subclasscd, classname, classabbv, subclassname, subclassabbv);
                            student._subclassList.add(subclass);
                        }
                        final String testcd = rs.getString("TESTCD");
                        final String avgAvg = rs.getString("AVG_AVG");
                        final String schregAvg = rs.getString("SCHREG_AVG");
                        final String rank = rs.getString("RANK");
                        final String deviation = rs.getString("DEVIATION");
                        if (null != testcd) {
                            if (null != score) {
                                subclass._scoreMap.put("SCORE" + testcd, score);
                            }
                            if (null != avg) {
                                subclass._scoreMap.put("AVG" + testcd, avg);
                            }
                            if (null != avgAvg) {
                                subclass._scoreMap.put("AVG_AVG" + testcd, avgAvg);
                            }
                            if (null != schregAvg) {
                                subclass._scoreMap.put("SCHREG_AVG" + testcd, schregAvg);
                            }
                            if (null != rank) {
                                subclass._scoreMap.put("RANK" + testcd, rank);
                            }
                            if (null != deviation) {
                                subclass._scoreMap.put("DEVIATION" + testcd, new BigDecimal(deviation));
                            }
                        }
                    }
                }
                
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        public static String getSubclassSql(final Param param) {
            final String[] printTestcd = param.getTargetTestKindCds();
            final StringBuffer stb = new StringBuffer();
            stb.append("  WITH ");
            stb.append("  REGD AS ( ");
            stb.append("    SELECT DISTINCT ");
            stb.append("         T1.SCHREGNO,  ");
            stb.append("         T1.GRADE,  ");
            stb.append("         T1.COURSECD,  ");
            stb.append("         T1.MAJORCD,  ");
            stb.append("         T1.COURSECODE,  ");
            stb.append("         T2.GROUP_CD  ");
            stb.append("    FROM    SCHREG_REGD_DAT T1  ");
            stb.append("    LEFT JOIN COURSE_GROUP_CD_DAT T2 ON T2.YEAR = T1.YEAR ");
            stb.append("        AND T2.GRADE = T1.GRADE ");
            stb.append("        AND T2.COURSECD = T1.COURSECD ");
            stb.append("        AND T2.MAJORCD = T1.MAJORCD ");
            stb.append("        AND T2.COURSECODE = T1.COURSECODE ");
            stb.append("    WHERE   T1.SCHREGNO = ? ");
            stb.append("        AND T1.YEAR = '" + param._year + "' ");
            stb.append("        AND T1.SEMESTER = '" + param.getRegdSemester() + "' ");
            stb.append("  ), ");
            stb.append("  CHAIR_A AS ( ");
            stb.append("    SELECT DISTINCT ");
            stb.append("         T1.SCHREGNO,  ");
            stb.append("         T2.CLASSCD,  ");
            stb.append("         T2.SCHOOL_KIND,  ");
            stb.append("         T2.CURRICULUM_CD,  ");
            stb.append("         T2.CLASSCD || T2.SCHOOL_KIND AS CLASS_KEY,  ");
            stb.append("         T2.CLASSCD || T2.SCHOOL_KIND || T2.CURRICULUM_CD || T2.SUBCLASSCD AS SUBCLASSCD  ");
            stb.append("    FROM    CHAIR_STD_DAT T1  ");
            stb.append("    INNER JOIN CHAIR_DAT T2 ON T1.YEAR = T2.YEAR AND T1.SEMESTER = T2.SEMESTER AND T1.CHAIRCD = T2.CHAIRCD ");
            stb.append("    INNER JOIN REGD T3 ON T3.SCHREGNO = T1.SCHREGNO ");
            stb.append("    LEFT JOIN REC_SUBCLASS_GROUP_DAT T4 ON T4.YEAR = T1.YEAR AND T4.GROUP_DIV = '3' ");
            stb.append("        AND T4.GRADE = T3.GRADE AND T4.COURSECD = T3.COURSECD AND T4.MAJORCD = T3.MAJORCD AND T4.COURSECODE = T3.COURSECODE ");
            stb.append("        AND T4.CLASSCD = T2.CLASSCD AND T4.SCHOOL_KIND = T2.SCHOOL_KIND AND T4.CURRICULUM_CD = T2.CURRICULUM_CD AND T4.SUBCLASSCD = T2.SUBCLASSCD ");
            stb.append("    LEFT JOIN REC_SUBCLASS_GROUP_DAT T5 ON T5.YEAR = T1.YEAR AND T5.GROUP_DIV = '5' ");
            stb.append("        AND T5.GRADE = T3.GRADE AND T5.COURSECD = T3.COURSECD AND T5.MAJORCD = T3.MAJORCD AND T5.COURSECODE = T3.COURSECODE ");
            stb.append("        AND T5.CLASSCD = T2.CLASSCD AND T5.SCHOOL_KIND = T2.SCHOOL_KIND AND T5.CURRICULUM_CD = T2.CURRICULUM_CD AND T5.SUBCLASSCD = T2.SUBCLASSCD ");
            stb.append("    WHERE   T1.YEAR = '" + param._year + "' ");
            stb.append("        AND T1.SEMESTER <= '" + param._semester + "' ");
//            stb.append("        AND (T4.YEAR IS NOT NULL OR T5.YEAR IS NOT NULL) ");
            stb.append("    GROUP BY  ");
            stb.append("         T1.SCHREGNO,  ");
            stb.append("         T2.CLASSCD,  ");
            stb.append("         T2.SCHOOL_KIND,  ");
            stb.append("         T2.CURRICULUM_CD,  ");
            stb.append("         T2.CLASSCD || T2.SCHOOL_KIND || T2.CURRICULUM_CD || T2.SUBCLASSCD  ");
            stb.append("  ), ");
            stb.append("  T_AVG_AVG AS ( ");
            stb.append("    SELECT DISTINCT ");
            stb.append("        T1.YEAR, T1.SEMESTER, T1.TESTKINDCD, T1.TESTITEMCD, T1.SCORE_DIV ");
            stb.append("        , T1.SUBCLASSCD, T2.GRADE  ");
            stb.append("        , SUM(AVG) / COUNT(AVG) AS AVG_AVG  ");
            stb.append("    FROM    RECORD_RANK_SDIV_DAT T1  ");
            stb.append("    INNER JOIN SCHREG_REGD_DAT T2 ON T2.YEAR = '" + param._year + "' AND T2.SEMESTER = '" + param.getRegdSemester() + "' AND T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("    WHERE   T1.YEAR = '" + param._year + "' ");
            stb.append("        AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV IN " + SQLUtils.whereIn(true, printTestcd) + " ");
            stb.append("        AND T1.SUBCLASSCD IN ('555555', '999999') ");
            stb.append("    GROUP BY  ");
            stb.append("        T1.YEAR, T1.SEMESTER, T1.TESTKINDCD, T1.TESTITEMCD, T1.SCORE_DIV ");
            stb.append("        , T1.SUBCLASSCD, T2.GRADE  ");
            
            stb.append("  ) ");
            stb.append("  SELECT  VALUE(T7.SHOWORDER3, 99) AS CLASSSHOWORDER2, VALUE(T4.SHOWORDER3, 99) AS SUBCLASSSHOWORDER2, T2.CLASS_KEY, T2.SUBCLASSCD, T7.CLASSNAME, T7.CLASSABBV, VALUE(T4.SUBCLASSORDERNAME2, T4.SUBCLASSNAME) AS SUBCLASSNAME, SUBCLASSABBV ");
            stb.append("        , T_SCORE.SEMESTER || T_SCORE.TESTKINDCD || T_SCORE.TESTITEMCD || T_SCORE.SCORE_DIV AS TESTCD  ");
            stb.append("        , T_RANK.SCORE  ");
            stb.append("        , T_RANK.GRADE_AVG_RANK AS RANK  ");
            stb.append("        , T_RANK.GRADE_DEVIATION AS DEVIATION  ");
            stb.append("        , CAST(NULL AS DECIMAL(9, 5)) AS SCHREG_AVG  ");
            stb.append("        , T_AVG.AVG  ");
            stb.append("        , CAST(NULL AS DECIMAL(9, 5)) AS AVG_AVG  ");
            stb.append("  FROM    CHAIR_A T2 ");
            stb.append("  INNER JOIN REGD T3 ON T3.SCHREGNO = T2.SCHREGNO ");
            stb.append("  LEFT JOIN SUBCLASS_MST T4 ON T4.CLASSCD || T4.SCHOOL_KIND || T4.CURRICULUM_CD || T4.SUBCLASSCD = T2.SUBCLASSCD ");
            stb.append("  LEFT JOIN CLASS_MST T7 ON T7.CLASSCD = T4.CLASSCD AND T7.SCHOOL_KIND = T4.SCHOOL_KIND  ");
            stb.append("  LEFT JOIN RECORD_SCORE_DAT T_SCORE ON T_SCORE.YEAR = '" + param._year + "'  ");
            stb.append("         AND T_SCORE.SEMESTER || T_SCORE.TESTKINDCD || T_SCORE.TESTITEMCD || T_SCORE.SCORE_DIV IN " + SQLUtils.whereIn(true, printTestcd) + " ");
            stb.append("         AND T_SCORE.SCHREGNO = T2.SCHREGNO ");
            stb.append("         AND T_SCORE.CLASSCD || T_SCORE.SCHOOL_KIND || T_SCORE.CURRICULUM_CD || T_SCORE.SUBCLASSCD = T2.SUBCLASSCD ");
            stb.append("  LEFT JOIN RECORD_RANK_SDIV_DAT T_RANK ON T_RANK.YEAR = '" + param._year + "'  ");
            stb.append("         AND T_RANK.SEMESTER = T_SCORE.SEMESTER ");
            stb.append("         AND T_RANK.TESTKINDCD = T_SCORE.TESTKINDCD ");
            stb.append("         AND T_RANK.TESTITEMCD = T_SCORE.TESTITEMCD ");
            stb.append("         AND T_RANK.SCORE_DIV = T_SCORE.SCORE_DIV ");
            stb.append("         AND T_RANK.CLASSCD = T_SCORE.CLASSCD ");
            stb.append("         AND T_RANK.SCHOOL_KIND = T_SCORE.SCHOOL_KIND ");
            stb.append("         AND T_RANK.CURRICULUM_CD = T_SCORE.CURRICULUM_CD ");
            stb.append("         AND T_RANK.SUBCLASSCD = T_SCORE.SUBCLASSCD ");
            stb.append("         AND T_RANK.SCHREGNO = T2.SCHREGNO ");
            stb.append("  LEFT JOIN RECORD_AVERAGE_SDIV_DAT T_AVG ON T_AVG.YEAR = '" + param._year + "'  ");
            stb.append("         AND T_AVG.SEMESTER = T_RANK.SEMESTER ");
            stb.append("         AND T_AVG.TESTKINDCD = T_RANK.TESTKINDCD ");
            stb.append("         AND T_AVG.TESTITEMCD = T_RANK.TESTITEMCD ");
            stb.append("         AND T_AVG.SCORE_DIV = T_RANK.SCORE_DIV ");
            stb.append("         AND T_AVG.CLASSCD = T_RANK.CLASSCD ");
            stb.append("         AND T_AVG.SCHOOL_KIND = T_RANK.SCHOOL_KIND ");
            stb.append("         AND T_AVG.CURRICULUM_CD = T_RANK.CURRICULUM_CD ");
            stb.append("         AND T_AVG.SUBCLASSCD = T_RANK.SUBCLASSCD ");
            stb.append("         AND T_AVG.AVG_DIV = '1' ");
            stb.append("         AND T_AVG.GRADE = T3.GRADE ");
            stb.append("         AND T_AVG.COURSECD = '0' ");
            stb.append("         AND T_AVG.MAJORCD = '000' ");
            stb.append("         AND T_AVG.COURSECODE = '0000' ");
            stb.append("  WHERE ");
            stb.append("      T2.CLASSCD < '90' ");
            stb.append("  UNION ALL ");
            stb.append("  SELECT  999 AS CLASSSHOWORDER2, 999 AS SUBCLASSSHOWORDER2, '99' AS CLASS_KEY, T_RANK.SUBCLASSCD, CAST(NULL AS VARCHAR(1)) AS CLASSNAME, CAST(NULL AS VARCHAR(1)) AS CLASSABBV, CAST(NULL AS VARCHAR(1)) AS SUBCLASSNAME, CAST(NULL AS VARCHAR(1)) AS SUBCLASSABBV ");
            stb.append("        , T_RANK.SEMESTER || T_RANK.TESTKINDCD || T_RANK.TESTITEMCD || T_RANK.SCORE_DIV AS TESTCD  ");
            stb.append("        , CAST(NULL AS SMALLINT) AS SCORE  ");
            stb.append("        , T_RANK.GRADE_AVG_RANK AS RANK  ");
            stb.append("        , T_RANK.GRADE_DEVIATION AS DEVIATION  ");
            stb.append("        , T_RANK.AVG AS SCHREG_AVG  ");
            stb.append("        , T_AVG.AVG  ");
            stb.append("        , L1.AVG_AVG  ");
            stb.append("  FROM    RECORD_RANK_SDIV_DAT T_RANK ");
            stb.append("  INNER JOIN REGD T3 ON T3.SCHREGNO = T_RANK.SCHREGNO ");
            stb.append("  LEFT JOIN RECORD_AVERAGE_SDIV_DAT T_AVG ON T_AVG.YEAR = '" + param._year + "'  ");
            stb.append("         AND T_AVG.SEMESTER = T_RANK.SEMESTER ");
            stb.append("         AND T_AVG.TESTKINDCD = T_RANK.TESTKINDCD ");
            stb.append("         AND T_AVG.TESTITEMCD = T_RANK.TESTITEMCD ");
            stb.append("         AND T_AVG.SCORE_DIV = T_RANK.SCORE_DIV ");
            stb.append("         AND T_AVG.SUBCLASSCD = T_RANK.SUBCLASSCD ");
            stb.append("         AND T_AVG.AVG_DIV = '1' ");
            stb.append("         AND T_AVG.GRADE = T3.GRADE ");
            stb.append("         AND T_AVG.COURSECD = '0' ");
            stb.append("         AND T_AVG.MAJORCD = '000' ");
            stb.append("         AND T_AVG.COURSECODE = '0000' ");
            stb.append("  LEFT JOIN T_AVG_AVG L1 ON L1.YEAR = T_AVG.YEAR  ");
            stb.append("         AND L1.SEMESTER = T_AVG.SEMESTER ");
            stb.append("         AND L1.TESTKINDCD = T_AVG.TESTKINDCD ");
            stb.append("         AND L1.TESTITEMCD = T_AVG.TESTITEMCD ");
            stb.append("         AND L1.SCORE_DIV = T_AVG.SCORE_DIV ");
            stb.append("         AND L1.SUBCLASSCD = T_AVG.SUBCLASSCD ");
            stb.append("         AND L1.GRADE = T3.GRADE ");
            stb.append("  WHERE T_RANK.YEAR = '" + param._year + "'  ");
            stb.append("        AND T_RANK.SEMESTER || T_RANK.TESTKINDCD || T_RANK.TESTITEMCD || T_RANK.SCORE_DIV IN " + SQLUtils.whereIn(true, printTestcd) + " ");
            stb.append("        AND T_RANK.SUBCLASSCD IN ('555555', '999999') ");
            stb.append("  ORDER BY ");
            stb.append("     CLASSSHOWORDER2, CLASS_KEY, SUBCLASSSHOWORDER2, SUBCLASSCD, TESTCD  ");
            return stb.toString();
        }
    }
    
    private static class Proficiency {
        final String _semester;
        final String _proficiencydiv;
        final String _proficiencycd;
        final String _proficiencyname1;
        final String _proficiencySubclassCd;
        final String _subclassName;
        final String _rankDiv;
        final String _score;
        final String _rank;
        final String _deviation;
        final String _avg;
        final int _perfect;

        Proficiency(
            final String semester,
            final String proficiencydiv,
            final String proficiencycd,
            final String proficiencyname1,
            final String proficiencySubclassCd,
            final String subclassName,
            final String rankDiv,
            final String score,
            final String rank,
            final String deviation,
            final String avg,
            final int perfect
        ) {
            _semester = semester;
            _proficiencydiv = proficiencydiv;
            _proficiencycd = proficiencycd;
            _proficiencyname1 = proficiencyname1;
            _proficiencySubclassCd = proficiencySubclassCd;
            _subclassName = subclassName;
            _rankDiv = rankDiv;
            _score = score;
            _rank = rank;
            _deviation = deviation;
            _avg = avg;
            _perfect = perfect;
        }

        public static void setProficiencyListMap(final DB2UDB db2, final Param param, final List studentList) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = sql(param);
                log.info(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                
                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();
                    
                    final Set subclassSet = new HashSet();
                    final Set testcdSet = new HashSet();
                    
                    final Map map = new HashMap();
                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        final String semester = rs.getString("SEMESTER");
                        final String proficiencydiv = rs.getString("PROFICIENCYDIV");
                        final String proficiencycd = rs.getString("PROFICIENCYCD");
                        String proficiencyname1 = rs.getString("PROFICIENCYNAME1");
                        final String subclassCd = rs.getString("PROFICIENCY_SUBCLASS_CD");
                        final String subclassName = rs.getString("SUBCLASS_NAME");
                        final String testcd = semester + proficiencydiv + proficiencycd;
                        
                        if (!subclassSet.contains(subclassCd)) {
                            getMappedList(map, "SUBCLASSCD").add(subclassCd);
                            getMappedMap(map, "SUBCLASSNAME").put(subclassCd, subclassName);
                            subclassSet.add(subclassCd);
                        }
                        
                        if (!testcdSet.contains(testcd)) {
                            getMappedList(map, "TESTCD").add(testcd);
                            
                            if (null != proficiencyname1 && (-1 != proficiencyname1.indexOf(' ') || -1 != proficiencyname1.indexOf('　') || getMS932ByteLength(proficiencyname1) > 5)) {
                                int spaceIdx = -1;
                                int ketaTotal = 0;
                                for (int i = 0; i < proficiencyname1.length(); i++) {
                                    final String ch = proficiencyname1.substring(i, i + 1);
                                    final int keta = getMS932ByteLength(ch);
                                    if (ketaTotal + keta > 5) {
                                        spaceIdx = i;
                                        break;
                                    }
                                    ketaTotal += keta;
                                }
                                if (-1 == spaceIdx) {
                                    spaceIdx = proficiencyname1.indexOf(' ');
                                }
                                if (-1 == spaceIdx) {
                                    spaceIdx = proficiencyname1.indexOf('　');
                                }
                                if (0 < spaceIdx) {
                                    proficiencyname1 = proficiencyname1.substring(0, spaceIdx);
                                }
                            }

                            getMappedMap(map, "TESTNAME").put(testcd, proficiencyname1);
                            testcdSet.add(testcd);
                        }

                        final String rankDiv = rs.getString("RANK_DIV");
                        final String score = rs.getString("SCORE");
                        final String rank = rs.getString("RANK");
                        final String deviation = rs.getString("DEVIATION");
                        final String avg = rs.getString("AVG");
                        String sPerfect = rs.getString("PERFECT");
                        final int perfect;
                        if (NumberUtils.isNumber(sPerfect)) {
                            perfect = (int) Double.parseDouble(sPerfect);
                        } else {
                            final double davg = NumberUtils.isNumber(avg) ? Double.parseDouble(avg) : 0.0;
                            final double dscore = NumberUtils.isNumber(score) ? Double.parseDouble(score) : 0.0;
                            perfect = (int) Math.max(davg, dscore);
                        }
                        
                        final Proficiency proficiency = new Proficiency(semester, proficiencydiv, proficiencycd, proficiencyname1, subclassCd, subclassName, rankDiv, score, rank, deviation, avg, perfect);

                        getMappedMap(getMappedMap(map, "TEST_DATA"), subclassCd).put(testcd, proficiency);
                    }
                    student._profMap = map;
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        public static String sql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("   T1.SEMESTER, ");
            stb.append("   T1.PROFICIENCYDIV, ");
            stb.append("   T1.PROFICIENCYCD, ");
            stb.append("   T2.PROFICIENCYNAME1, ");
            stb.append("   T1.PROFICIENCY_SUBCLASS_CD, ");
            stb.append("   T4.SUBCLASS_NAME, ");
            stb.append("   T1.RANK_DIV, ");
            stb.append("   T1.SCORE, ");
            stb.append("   T1.RANK, ");
            stb.append("   T1.DEVIATION, ");
            stb.append("   T_AVG.AVG, ");
            stb.append("   T_PFC.PERFECT ");
            stb.append(" FROM PROFICIENCY_RANK_DAT T1 ");
            stb.append(" INNER JOIN PROFICIENCY_MST T2 ON T2.PROFICIENCYDIV = T1.PROFICIENCYDIV ");
            stb.append("   AND T2.PROFICIENCYCD = T1.PROFICIENCYCD ");
            stb.append(" INNER JOIN PROFICIENCY_YMST T2Y ON T2Y.YEAR = T1.YEAR ");
            stb.append("   AND T2Y.SEMESTER =  T1.SEMESTER ");
            stb.append("   AND T2Y.PROFICIENCYDIV = T1.PROFICIENCYDIV ");
            stb.append("   AND T2Y.PROFICIENCYCD = T1.PROFICIENCYCD ");
            stb.append(" INNER JOIN PROFICIENCY_SUBCLASS_MST T4 ON T4.PROFICIENCY_SUBCLASS_CD = T1.PROFICIENCY_SUBCLASS_CD ");
            stb.append(" INNER JOIN SCHREG_REGD_DAT REGD ON REGD.SCHREGNO = T1.SCHREGNO ");
            stb.append("   AND REGD.YEAR = T1.YEAR ");
            stb.append("   AND REGD.SEMESTER = T1.SEMESTER ");
            stb.append("   AND T2Y.GRADE = REGD.GRADE ");
            stb.append(" LEFT JOIN COURSE_GROUP_CD_DAT GRC ON GRC.YEAR = REGD.YEAR ");
            stb.append("   AND GRC.GRADE = REGD.GRADE ");
            stb.append("   AND GRC.COURSECD = REGD.COURSECD ");
            stb.append("   AND GRC.MAJORCD = REGD.MAJORCD ");
            stb.append("   AND GRC.COURSECODE = REGD.COURSECODE ");
            stb.append(" LEFT JOIN PROFICIENCY_AVERAGE_DAT T_AVG ON T_AVG.YEAR = T1.YEAR ");
            stb.append("   AND T_AVG.SEMESTER = T1.SEMESTER ");
            stb.append("   AND T_AVG.PROFICIENCYDIV = T1.PROFICIENCYDIV ");
            stb.append("   AND T_AVG.PROFICIENCYCD = T1.PROFICIENCYCD ");
            stb.append("   AND T_AVG.PROFICIENCY_SUBCLASS_CD = T1.PROFICIENCY_SUBCLASS_CD ");
            stb.append("   AND T_AVG.DATA_DIV = '1' ");
            stb.append("   AND T_AVG.AVG_DIV = T1.RANK_DATA_DIV ");
            stb.append("   AND T_AVG.GRADE = REGD.GRADE ");
            stb.append("   AND T_AVG.HR_CLASS = CASE WHEN T1.RANK_DATA_DIV = '02' THEN REGD.HR_CLASS ELSE '000' END ");
            stb.append("   AND T_AVG.COURSECD = CASE WHEN T1.RANK_DATA_DIV = '03' THEN REGD.COURSECD ELSE '0' END ");
            stb.append("   AND T_AVG.MAJORCD = CASE WHEN T1.RANK_DATA_DIV = '03' THEN REGD.MAJORCD  ");
            stb.append("                            WHEN T1.RANK_DATA_DIV = '05' THEN GRC.GROUP_CD ");
            stb.append("                            ELSE '000' END ");
            stb.append("   AND T_AVG.COURSECODE = CASE WHEN T1.RANK_DATA_DIV = '03' THEN REGD.COURSECODE ELSE '0000' END ");
            stb.append(" LEFT JOIN PROFICIENCY_PERFECT_COURSE_DAT T_PFC ON T_PFC.YEAR = T1.YEAR ");
            stb.append("   AND T_PFC.SEMESTER = T1.SEMESTER ");
            stb.append("   AND T_PFC.PROFICIENCYDIV = T1.PROFICIENCYDIV ");
            stb.append("   AND T_PFC.PROFICIENCYCD = T1.PROFICIENCYCD ");
            stb.append("   AND T_PFC.PROFICIENCY_SUBCLASS_CD = T1.PROFICIENCY_SUBCLASS_CD ");
            stb.append("   AND T_PFC.GRADE = CASE WHEN T_PFC.DIV = '01' THEN '00' ELSE REGD.GRADE END ");
            stb.append("   AND T_PFC.COURSECD = CASE WHEN T_PFC.DIV = '03' THEN REGD.COURSECD ELSE '0' END ");
            stb.append("   AND T_PFC.MAJORCD = CASE WHEN T_PFC.DIV = '03' THEN REGD.MAJORCD  ");
            stb.append("                            WHEN T_PFC.DIV = '05' THEN GRC.GROUP_CD ");
            stb.append("                            ELSE '000' END ");
            stb.append("   AND T_PFC.COURSECODE = CASE WHEN T_PFC.DIV = '03' THEN REGD.COURSECODE ELSE '0000' END ");
            stb.append(" WHERE ");
            stb.append("   T1.YEAR = '" + param._year + "' ");
            stb.append("   AND T1.SEMESTER <= '" + param._semester + "' ");
            stb.append("   AND T1.SCHREGNO = ? ");
            stb.append("   AND T1.RANK_DATA_DIV = '01' ");
            stb.append("   AND T1.RANK_DIV = '01' ");
            stb.append(" ORDER BY ");
            stb.append("   T1.PROFICIENCYDIV, ");
            stb.append("   T1.PROFICIENCYCD, ");
            stb.append("   T1.PROFICIENCY_SUBCLASS_CD ");
            return stb.toString();
        }
    }
    
    private void printMain(final Vrw32alp svf, final Param param, final Student student) {
        final String form = "KNJD154R.frm";
        svf.VrSetForm(form, 4);
        
        svf.VrsOut("TITLE", KenjaProperties.gengou(Integer.parseInt(param._year))+ "年度　　成　績　個　票"); // 
        svf.VrsOut("SCHOOL_NAME", param._schoolName); // 学校名
        final String attendno = StringUtils.defaultString(NumberUtils.isDigits(student._attendNo) ? String.valueOf(Integer.parseInt(student._attendNo)) : student._attendNo);
        svf.VrsOut("GRADE_HR_NAME", StringUtils.defaultString(student._hrName) + "　" + attendno); // 年組番
        if (param._staffNames.size() > 0) {
            final String staffname = (String) param._staffNames.get(0);
            final int keta = getMS932ByteLength(staffname);
            svf.VrsOut("TEACHER_NAME" + (keta > 30 ? "3" : keta > 20 ? "2" : "1"), staffname); // 担任名
        }
        svf.VrsOut("NAME", student._name); // 氏名

        for (int semesi = 1; semesi <= 3; semesi++) {
            svf.VrsOut("SEMESTER_NAME" + String.valueOf(semesi), (String) param._semesterMap.get(String.valueOf(semesi))); // 学期名
            svf.VrsOut("SEMESTER_NAME" + String.valueOf(semesi) + "_3", (String) param._semesterMap.get(String.valueOf(semesi))); // 学期名
        }

        final String[] fieldFooter = {"1_1", "1_2", "2_1", "2_2", "3"};
        for (int testi = 0; testi < param._testCds.length; testi++) {
            if (fieldFooter.length <= testi) {
                break;
            }
            final String testname;
            if (param._testCds[testi].equals("3020101")) {
                testname = "学年末";
            } else if (param._testCds[testi].endsWith("010101")) {
                testname = "中間";
            } else if (param._testCds[testi].endsWith("020101")) {
                testname = "期末";
            } else {
                testname = (String) param._testItemNames.get(param._testCds[testi]);
            }
            svf.VrsOut("TESTITEM_NAME" + fieldFooter[testi], testname); // 考査名称
            svf.VrsOut("TESTITEM_NAME" + fieldFooter[testi] + "_2", testname); // 考査名称
        }

        final Map subclassMap = student.getSubclassMap();
        log.info("subclassMap = " + subclassMap.keySet());
        for (int testi = 0; testi < param.getTargetTestKindCds().length; testi++) {
            if (fieldFooter.length <= testi) {
                break;
            }
            final String testCd = param.getTargetTestKindCds()[testi];
            if (null != subclassMap.get(_555555)) {
                final Subclass subc = (Subclass) subclassMap.get(_555555);
                svf.VrsOut("SCORE5_" + fieldFooter[testi], sishaGonyu((String) subc._scoreMap.get("SCHREG_AVG" + testCd))); // 平均
                svf.VrsOut("GRADE_AVE5_" + fieldFooter[testi], sishaGonyu((String) subc._scoreMap.get("AVG_AVG" + testCd))); // 平均の平均点
                if (1 <= testi) {
                    String[] rankDiff = subc.getRankDiff(param._testCds[testi - 1], testCd);
                    svf.VrsOut("RANK" + fieldFooter[testi], rankDiff[0]); // 順位変動
                    svf.VrsOut("RANK_UPDOWN" + fieldFooter[testi], rankDiff[1]); // 順位変動向き
                }
            }
            
            if (null != subclassMap.get(_999999)) {
                final Subclass subc = (Subclass) subclassMap.get(_999999);
                svf.VrsOut("SCOREALL_" + fieldFooter[testi], sishaGonyu((String) subc._scoreMap.get("SCHREG_AVG" + testCd))); // 平均
                svf.VrsOut("GRADE_AVEALL_" + fieldFooter[testi], sishaGonyu((String) subc._scoreMap.get("AVG_AVG" + testCd))); // 平均の平均点
            }
        }

        for (int testi = 0; testi < param.getTargetTestKindCds().length; testi++) {
            if (fieldFooter.length <= testi) {
                break;
            }
            final String testCd = param.getTargetTestKindCds()[testi];
            final String shoken = (String) student._shokenMap.get(testCd);
            
            final String[] token = KNJ_EditEdit.get_token(shoken, 80, 3);
            if (null != token) {
                for (int j = 0; j < token.length; j++) {
                    final int line = j + 1;
                    svf.VrsOutn("VIEW" + String.valueOf(line), testi + 1, token[j]); // 所見
                }
            }
        }

        if ("2".equals(param._graph)) {
            final ProficiencyChart chart = new ProficiencyChart();
            
            final int maxSubclass = 5;
            List subclasscdList = getMappedList(student._profMap, "SUBCLASSCD");
            subclasscdList = subclasscdList.size() > maxSubclass ? subclasscdList.subList(0, maxSubclass) : subclasscdList; 
            final String field;
            final boolean use5 = subclasscdList.size() > 4;
            final boolean use4 = subclasscdList.size() == 4;
            if (use5) {
            	field = "SMALL_TEST5_1";
            } else if (use4) {
            	field = "SMALL_TEST4_1";
            } else {
            	// 3行
            	field = "SMALL_TEST1";
            }
            
            final int w = 2564;
            for (int subi = 0; subi < subclasscdList.size(); subi++) {
                final String subclasscd = (String) subclasscdList.get(subi);
                final List testcdList = getMappedList(student._profMap, "TESTCD");
                
                final int h;
                if (use5) {
                	h = 200;
                } else if (use4) {
                	h = 260;
                } else {
                    h = 375;
                }

                final File file = chart.file(testcdList, getMappedMap(student._profMap, "TESTNAME"), getMappedMap(getMappedMap(student._profMap, "TEST_DATA"), subclasscd), w, h);
                if (file.exists()) {
                	final int n = subi + 1;
                    svf.VrsOutn("PROF_SUBCLASS_NAME1", n, (String) (getMappedMap(student._profMap, "SUBCLASSNAME")).get(subclasscd)); // 画像
                    final String y = String.valueOf((int) (3276 + h * subi + h / 2 - 35));
					svf.VrAttributen("PROF_SUBCLASS_NAME1", n, "Y=" + y); // 画像
                    svf.VrsOutn(field, n, file.getAbsolutePath()); // 画像
                }
            }
        } else {
            final File file = getRadarGraph(param, student);
            if (file.exists()) {
                svf.VrsOut("RADAR", file.getAbsolutePath()); // 画像
            }
        }
        
        
        final int maxSubclass = 15;
        int print = 0;
        for (int i = 0; i < student._subclassList.size(); i++) {
            final Subclass subclass = (Subclass) student._subclassList.get(i);
            if (subclass == subclassMap.get(_999999) || subclass == subclassMap.get(_555555)) {
                continue;
            }
            if (param._d046List.contains(subclass._subclasscd)) {
                continue;
            }
            svf.VrsOut("SUBCLASS_NAME", subclass._subclassname); // 科目名
            
            for (int testi = 0; testi < param._testCds.length; testi++) {
                if (fieldFooter.length <= testi) {
                    break;
                }
                final String score = (String) subclass._scoreMap.get("SCORE" + param._testCds[testi]);
                svf.VrsOut("SCORE" + fieldFooter[testi], score); // 学期名
                final String avg = (String) subclass._scoreMap.get("AVG" + param._testCds[testi]);
                svf.VrsOut("GRADE_AVE" + fieldFooter[testi], sishaGonyu(avg)); // 学期名
            }
            svf.VrEndRecord();
            print += 1;
        }
        if (print == 0 || print % maxSubclass != 0) {
            for (int i = 0; i < (maxSubclass - (print % maxSubclass)); i++) {
                svf.VrsOut("SUBCLASS_NAME", ""); // 科目名
                svf.VrEndRecord();
            }
        }
    }
    
    private File getRadarGraph(final Param param, final Student student) {
        // データ作成
        final DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (final Iterator it = student._subclassList.iterator(); it.hasNext();) {
            final Subclass subclass = (Subclass) it.next();
            
            if (_555555.equals(subclass._subclasscd) || _999999.equals(subclass._subclasscd)) {
                continue;
            }
            final BigDecimal deviation = (BigDecimal) subclass._scoreMap.get("DEVIATION" + param._testCd);
            if (null == deviation) {
                continue;
            }
            
            final String key = subclass._subclassabbv;
            dataset.addValue(deviation, "本人偏差値", key);// MAX80
            dataset.addValue(50.0, "偏差値50", key);
            dataset.addValue(75.0, "偏差値75", key);
        }

        try {
            // グラフのファイルを生成
            final String title = StringUtils.defaultString((String) param._testItemNames.get(param._testCd));
            final int w = 1484, h = 1384;
            final File outputFile = Param.graphImageFile(RaderChart.createRaderChart(title, dataset), w, h);
            if (outputFile.exists()) {
                _graphFiles.add(outputFile);
                return outputFile;
            }
        } catch (Throwable e) {
            log.error("exception or error!", e);
        }
        return null;
    }

    private static String sishaGonyu(final String s) {
        if (!NumberUtils.isNumber(s)) {
            return null;
        }
        final String rtn = new BigDecimal(s).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
        return rtn;
    }


    private void svfVrsOutnKurikaeshi(final Vrw32alp svf, final String field, final int j, final String[] token) {
        if (null != token) {
            for (int i = 0; i < token.length; i++) {
                svf.VrsOutn(field + String.valueOf(i + 1), j, token[i]);
            }
        }
    }
    
    private class ProficiencyChart {
        
        private final Font _font7 = new Font("TimesRoman", Font.PLAIN, 7);
        private final Font _font6 = new Font("TimesRoman", Font.PLAIN, 6);
        private final BasicStroke _outlineStroke = new BasicStroke();
        private final BasicStroke _graph1Stroke = new BasicStroke(3f);
        
        private File file(final List testcdList, final Map testnameMap, final Map proficiencyMap, final int w, final int h) {
            try {
                // グラフのファイルを生成
                final File outputFile = Param.graphImageFile(createChart(testcdList, testnameMap, proficiencyMap), w, h);
                if (outputFile.exists()) {
                    _graphFiles.add(outputFile);
                    return outputFile;
                }
            } catch (Throwable e) {
                log.error("exception or error!", e);
            }
            return null;
        }
        

        private JFreeChart createChart(final List testcdList, final Map testnameMap, final Map proficiencyMap) {
            final CategoryDataset dataset = new DefaultCategoryDataset();
            final JFreeChart chart = ChartFactory.createLineChart(null, null, null, dataset, PlotOrientation.VERTICAL, false, false, false);
            
            final CategoryPlot plot = (CategoryPlot) chart.getPlot();
            plot.setDataset(dataset);

            final CategoryAxis domainAxis = plot.getDomainAxis();
            final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();

//            domainAxis.setCategoryLabelPositions(
//                    CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 6.0)
//            );

            plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);

            plot.setOutlineStroke(_outlineStroke);

            // set the background color for the chart...
            chart.setBackgroundPaint(Color.white);
            final RectangleInsets insets = plot.getInsets();
            plot.setInsets(new RectangleInsets(insets.getTop(), insets.getLeft(), insets.getBottom() - 8, insets.getRight()));
//            ValueAxis vx = plot.getRangeAxis();

//            final LegendTitle legend = chart.getLegend();
//            legend.setItemFont(_font);
//            legend.setBorder(BlockBorder.NONE);

            final DefaultCategoryDataset scores = new DefaultCategoryDataset();
            final DefaultCategoryDataset avgs = new DefaultCategoryDataset();
            final BigDecimal zero = new BigDecimal(0);
            int perfect = 0;
            for (final Iterator it = testcdList.iterator(); it.hasNext();) {
                final String testcd = (String) it.next();
                final String testname = (String) testnameMap.get(testcd);
                final Proficiency prof = (Proficiency) proficiencyMap.get(testcd);
                if (null == prof || null == testname) {
                    continue;
                }
                scores.addValue(null == prof._score ? null : new BigDecimal(prof._score), "得点", testname);
                avgs.addValue(null == prof._avg ? null : new BigDecimal(prof._avg).setScale(1, BigDecimal.ROUND_HALF_UP), "平均", testname);
                perfect = Math.max(perfect, prof._perfect);
            }
            final Font font;
            final boolean useLarger = scores.getColumnCount() <= 27;
			if (useLarger) {
                for (int i = 0, cnt = 20 - scores.getColumnCount(); i < cnt; i++) {
                    scores.addValue(null, "得点", "\n" + String.valueOf(i));
                    avgs.addValue(null, "平均", "\n" + String.valueOf(i));
                }
                font = _font7;
            } else {
                font = _font6;
            }
            domainAxis.setTickLabelFont(font);
            rangeAxis.setTickLabelFont(font);
            rangeAxis.setRange(0, perfect);
            rangeAxis.setTickUnit(new NumberTickUnit(perfect / 5));
            
            plot.setDataset(1, avgs);
            plot.setDataset(2, scores);
            final BarRenderer br = new BarRenderer();
            br.setPaint(Color.lightGray);
            plot.setRenderer(1, br);
            final LineAndShapeRenderer lsr = new LineAndShapeRenderer();
//          renderer1.setLinesVisible(false);
            lsr.setStroke(_graph1Stroke);
            lsr.setPaint(Color.black);
            plot.setRenderer(2, lsr);

            plot.setBackgroundPaint(Color.white);
            plot.setOutlinePaint(Color.black);
            plot.setRangeGridlinePaint(Color.black);
            plot.setRangeGridlinesVisible(true);

            return chart;
         }
    }
    
    private static class RaderChart extends SpiderWebPlot {

        /** SpiderWebPlotにないので追加 */
        private double _minValue = 0.0;
        private Font _valueLabelFont;
        
        protected double getMinValue() {
            return _minValue;
        }
        
        protected void setMinValue(final double minValue) {
            _minValue = minValue;
        }
        
        protected void setValueLabelFont(final Font valueLabelFont) {
            _valueLabelFont = valueLabelFont;
        }
        
        public void draw(Graphics2D g2, Rectangle2D area, Point2D anchor, PlotState parentState, PlotRenderingInfo info) {

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
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, getForegroundAlpha()));

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

                // Now actually plot each of the series polygons..
                for (int series = 0; series < seriesCount; series++) {
                    drawRadarPoly(g2, radarArea, centre, info, series, catCount, headH, headW);
                }

                // draw the axis and category label
                for (int cat = 0; cat < catCount; cat++) {
                    double angle = getStartAngle() + (getDirection().getFactor() * cat * 360 / catCount);

                    final Point2D endPoint = getWebPoint(radarArea, angle, 1);

                    // 1 = end of axis
                    final Line2D.Double line = new Line2D.Double(centre, endPoint);
                    g2.setPaint(Color.gray);
                    g2.draw(line);
                    
                    
                    Number dataValue = getPlotValue(0, cat);
                    if (null != dataValue) {
                        final double value = dataValue.doubleValue();
                        drawLabel(g2, radarArea, value, cat, angle, 360.0 / catCount);
                    }
                }
                drawBaseDeviation(g2, radarArea, 360.0 / catCount);
                
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
                                     double headH, double headW) {

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

                        double angle = getStartAngle() + (getDirection().getFactor() * cat * 360 / catCount);

                        // The following angle calc will ensure there isn't a top 
                        // vertical axis - this may be useful if you don't want any 
                        // given criteria to 'appear' move important than the 
                        // others..
                        //  + (getDirection().getFactor() 
                        //        * (cat + 0.5) * 360 / catCount);

                        // find the point at the appropriate distance end point 
                        // along the axis/angle identified above and add it to the
                        // polygon

                        Point2D point = getWebPoint(plotArea, angle, (value - getMinValue()) / (getMaxValue() - getMinValue()));
                        polygon.addPoint((int) point.getX(), (int) point.getY());

                        // put an elipse at the point being plotted..

//                        Paint paint = getSeriesPaint(series);
//                        Paint outlinePaint = getSeriesOutlinePaint(series);
//                        Stroke outlineStroke = getSeriesOutlineStroke(series);
//
//                        Ellipse2D head = new Ellipse2D.Double(point.getX() - headW / 2, point.getY() - headH / 2, headW, headH);
//                        g2.setPaint(paint);
//                        g2.fill(head);
//                        g2.setStroke(outlineStroke);
//                        g2.setPaint(outlinePaint);
//                        g2.draw(head);

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
                       
                            Shape area = new Rectangle((int) (point.getX() - headW), (int) (point.getY() - headH), (int) (headW * 2), (int) (headH * 2));
                            CategoryItemEntity entity = new CategoryItemEntity(area, tip, url, getDataset(), series, getDataset().getColumnKey(cat), cat); 
                            entities.add(entity);
                        }

                        // then draw the axis and category label, but only on the 
                        // first time through.....

//                        if (series == 0) {
//                            Point2D endPoint = getWebPoint(plotArea, angle, 1); 
//                                                                 // 1 = end of axis
//                            Line2D  line = new Line2D.Double(centre, endPoint);
//                            g2.draw(line);
//                            drawLabel(g2, plotArea, value, cat, angle, 360.0 / catCount);
//                        }
                    }
                }
            }
            // Plot the polygon
        
            Stroke outlineStroke = getSeriesOutlineStroke(series);
            Paint paint = getSeriesPaint(series);
            g2.setStroke(outlineStroke);
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
        
        /**
         * Draws the label for one axis.
         * 
         * @param g2  the graphics device.
         * @param plotArea  the plot area
         * @param value  the value of the label.
         * @param cat  the category (zero-based index).
         * @param startAngle  the starting angle.
         * @param extent  the extent of the arc.
         */
        protected void drawLabel(Graphics2D g2, Rectangle2D plotArea, double value, 
                                 int cat, double startAngle, double extent) {
            FontRenderContext frc = g2.getFontRenderContext();
     
            String label = null;
            if (this.getDataExtractOrder() == TableOrder.BY_ROW) {
                // if series are in rows, then the categories are the column keys
                label = this.getLabelGenerator().generateColumnLabel(this.getDataset(), cat);
            } else {
                // if series are in columns, then the categories are the row keys
                label = this.getLabelGenerator().generateRowLabel(this.getDataset(), cat);
            }
     
            final Rectangle2D labelBounds = getLabelFont().getStringBounds(label, frc);
            final LineMetrics lm = getLabelFont().getLineMetrics(label, frc);
            final double ascent = lm.getAscent();

            final Point2D labelLocation = calculateLabelLocation(labelBounds, ascent, plotArea, startAngle, 1.0);

            final Font valueLabelFont = null == _valueLabelFont ? getLabelFont() : _valueLabelFont;
            final String valueLabel = new BigDecimal(value).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
            final Rectangle2D valueLabelBounds = valueLabelFont.getStringBounds(valueLabel, frc);
            final LineMetrics valueLm = valueLabelFont.getLineMetrics(valueLabel, frc);
            final double valueAscent = valueLm.getAscent();
            final Point2D valueLabelLocation = calculateLabelLocation(valueLabelBounds, valueAscent, plotArea, startAngle, 0.7);

            Composite saveComposite;
            saveComposite = g2.getComposite();
        
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            g2.setPaint(getLabelPaint());
            g2.setFont(getLabelFont());
            g2.drawString(label, (float) labelLocation.getX(), (float) labelLocation.getY());
            g2.setComposite(saveComposite);
            
            saveComposite = g2.getComposite();
            
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            
//            final int imageWidth = (int) valueLabelBounds.getWidth();
//            final int imageHeight = (int) valueLabelBounds.getHeight();
//            final AffineTransform op = new AffineTransform();
//            op.rotate(- 45 / 180.0 * Math.PI, imageWidth / 2, imageHeight / 2);
//            final BufferedImage image = new BufferedImage(imageWidth + 5, imageHeight + 10, BufferedImage.TYPE_BYTE_GRAY);
//            final Graphics2D g = image.createGraphics();
//            g.fillRect(0, 0, image.getWidth(), image.getHeight());
//            g.setColor(Color.black);
//            g.drawRect(0, 0, image.getWidth() - 1, image.getHeight() - 1);
//            g.setPaint(getLabelPaint());
//            g.setFont(valueLabelFont);
//            g.transform(op);
//            g.drawString(valueLabel, 0, 15);
//            g2.drawImage(image, (int) valueLabelLocation.getX(), (int) valueLabelLocation.getY(), null);
//            g2.setComposite(saveComposite);
            
            g2.setPaint(getLabelPaint());
            g2.setFont(valueLabelFont);
            g2.drawString(valueLabel, (int) valueLabelLocation.getX(), (int) valueLabelLocation.getY());
            g2.setComposite(saveComposite);
        }
        
        /**
         * Draws the label for one axis.
         * 
         * @param g2  the graphics device.
         * @param plotArea  the plot area
         * @param value  the value of the label.
         * @param cat  the category (zero-based index).
         * @param startAngle  the starting angle.
         * @param extent  the extent of the arc.
         */
        protected void drawBaseDeviation(Graphics2D g2, Rectangle2D plotArea, double extent) {
            FontRenderContext frc = g2.getFontRenderContext();
     
            final Font valueLabelFont = null == _valueLabelFont ? getLabelFont() : _valueLabelFont;
            final String valueLabel = new BigDecimal(25.0).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
            final Rectangle2D valueLabelBounds = valueLabelFont.getStringBounds(valueLabel, frc);
            final LineMetrics valueLm = valueLabelFont.getLineMetrics(valueLabel, frc);
            final double valueAscent = valueLm.getAscent();

            final double[][] valueAngle = {{25.0, 180.0, 0.05}, {50.0, 94, 0.46}, {75.0, 92, 0.91}};
            for (int i = 0; i < valueAngle.length; i++) {
                final String valueLabel1 = new BigDecimal(valueAngle[i][0]).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                
                final Point2D valueLabelLocation = calculateLabelLocation(valueLabelBounds, valueAscent, plotArea, valueAngle[i][1], valueAngle[i][2]);

                final Composite saveComposite = g2.getComposite();
            
                g2.setFont(valueLabelFont);
                g2.drawString(valueLabel1, (float) valueLabelLocation.getX(), (float) valueLabelLocation.getY());
                g2.setComposite(saveComposite);
            }
        }
        
        /**
         * Returns the location for a label
         * 
         * @param labelBounds the label bounds.
         * @param ascent the ascent (height of font).
         * @param plotArea the plot area
         * @param startAngle the start angle for the pie series.
         * 
         * @return The location for a label.
         */
        protected Point2D calculateLabelLocation(Rectangle2D labelBounds, 
                                                 double ascent,
                                                 Rectangle2D plotArea, 
                                                 double startAngle,
                                                 double rate)
        {
            Arc2D arc1 = new Arc2D.Double(plotArea, startAngle, 0, Arc2D.OPEN);
            Point2D point1 = arc1.getEndPoint();
            point1.setLocation(plotArea.getCenterX() + rate * (point1.getX() - plotArea.getCenterX()), plotArea.getCenterY() + rate * (point1.getY() - plotArea.getCenterY()));

            double deltaX = - (point1.getX() - plotArea.getCenterX()) * this.getAxisLabelGap();
            double deltaY = - (point1.getY() - plotArea.getCenterY()) * this.getAxisLabelGap();

            double labelX = point1.getX() - deltaX;
            double labelY = point1.getY() - deltaY;

            if (labelX < plotArea.getCenterX()) {
                labelX -= labelBounds.getWidth();
            }
        
            if (labelX == plotArea.getCenterX()) {
                labelX -= labelBounds.getWidth() / 2;
            }

            if (labelY > plotArea.getCenterY()) {
                labelY += ascent;
            }

            return new Point2D.Double(labelX, labelY);
        }

        private static JFreeChart createRaderChart(final String title, final DefaultCategoryDataset dataset) {
            final RaderChart plot = new RaderChart();
            plot.setDataset(dataset);
            plot.setWebFilled(false);
            plot.setMinValue(25.0);
            plot.setMaxValue(75.0);

            // 実データ
            plot.setSeriesOutlineStroke(0, new BasicStroke(2.0f));
            plot.setSeriesOutlinePaint(0, Color.black);
            plot.setSeriesPaint(0, Color.black);

            // 偏差値50.0、75.0の線
            final BasicStroke stroke = new BasicStroke(1.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f, new float[] {2.0f, 2.0f}, 0.0f);
            plot.setSeriesOutlineStroke(1, stroke);
            plot.setSeriesOutlinePaint(1, Color.darkGray);
            plot.setSeriesPaint(1, Color.darkGray);
            plot.setSeriesOutlineStroke(2, stroke);
            plot.setSeriesOutlinePaint(2, Color.darkGray);
            plot.setSeriesPaint(2, Color.darkGray);

            plot.setOutlinePaint(Color.white);

            plot.setLabelFont(new Font("TimesRoman", Font.BOLD, 12));
            plot.setValueLabelFont(new Font("TimesRoman", Font.PLAIN, 12));
            plot.setLabelPaint(Color.black);

            final JFreeChart chart = new JFreeChart(null, null, plot, false);
            chart.setBackgroundPaint(Color.white);
            chart.setTitle(new TextTitle("レーダーチャート（" + StringUtils.defaultString(title) + "）", new Font("TimesRoman", Font.BOLD, 14), Color.black, RectangleEdge.TOP, HorizontalAlignment.CENTER, VerticalAlignment.TOP, RectangleInsets.ZERO_INSETS));

            return chart;
        }
    }

    private static class Param {

        static final String SEMEALL = "9";
        
        final String _year;
        final String _semester;
        final String _ctrlSemester;
        final String _grade_hr_class;
        
        final String _pattern;
        final String _testCd;
        final String _ctrlDate;
        final String[] _dispSemester;
        
        // 素点等を表示する成績のテスト種別コード
        final String[] _testCds = {"1010101", "1020101", "2010101", "2020101", "3020101"};
        final String[] _categorySelected;
        final String _outputDiv;
        final File _logoFile;
        final String _graph;
        private String _imagePath;
        private String _extension;
        
        private String _schoolName;
        private String _principalName;
        private String _jobName;
        private String _hrJobName;
        private final List _staffNames;

        private boolean _isSeireki;

        /** 学期・テスト種別と考査名称のマップ */
        final Map _testItemNames;
        final Map _semesterMap;
        final boolean _isNotPrintNote;
        final String _documentRoot;
        
        /** 平均値を表示しない */
        final boolean _isNotPrintAvg;

        /** 名称マスタ「D046」 登録された学期に表示しない科目のリスト */
        private List _d046List;
        
        final String _useClassDetailDat;

        
        Param(final HttpServletRequest request, final DB2UDB db2) {
            log.info(" $Revision: 72387 $");
            KNJServletUtils.debugParam(request, log);

            _year = request.getParameter("CTRL_YEAR");
            _semester = request.getParameter("SEMESTER");           
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");            
            _ctrlDate = StringUtils.replace(request.getParameter("CTRL_DATE"), "/", "-");
            _testCd = request.getParameter("TEST_CD");
            _grade_hr_class = request.getParameter("GRADE_HR_CLASS");
            _pattern = request.getParameter("TYOUHYOU_PATTERN");
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _outputDiv = request.getParameter("OUTPUT_DIV");
            _isNotPrintNote = "1".equals(request.getParameter("HANREI_SYUTURYOKU_NASI"));
            _documentRoot = request.getParameter("DOCUMENTROOT");
            _isNotPrintAvg = "1".equals(request.getParameter("AVG_PRINT"));
            _dispSemester = new String[]{"1", "2", "3"};
            _useClassDetailDat = request.getParameter("useClassDetailDat");
            _graph = request.getParameter("GRAPH");

            try {
                loadNameMstZ012(db2);
                loadNameMstD046(db2);
                loadControlMst(db2);
            } catch (SQLException e) {
                log.error("名称マスタ読み込みエラー", e);
            }
            _logoFile = new File(_documentRoot + "/" + _imagePath + "/" + "SCHOOLLOGO." + _extension);

            _semesterMap = loadSemester(db2);
            _testItemNames = loadTestItemMst(db2);
            
            _staffNames = getStaffNames(db2, getRegdSemester());

            setCertifSchoolDat(db2);
        }
        
        public String[] getTargetSemester() {
            final List list = new ArrayList();
            for (int i = 0; i < _dispSemester.length; i++) {
                if (_semester.compareTo(_dispSemester[i]) >= 0) {
                    list.add(_dispSemester[i]);
                }
            }
            
            final String[] elems = new String[list.size()];
            list.toArray(elems);
            return elems;
        }
        
        public String[] getTargetTestKindCds() {
            final List list = new ArrayList();
            for (int i = 0; i < _testCds.length; i++) {
                if (_testCd.compareTo(_testCds[i]) >= 0) {
                    list.add(_testCds[i]);
                }
            }
            
            final String[] elems = new String[list.size()];
            list.toArray(elems);
            return elems;
        }

        
        private void loadControlMst(final DB2UDB db2) {
            final String sql = "SELECT IMAGEPATH, EXTENSION FROM CONTROL_MST ";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    _imagePath = rs.getString("IMAGEPATH");
                    _extension = rs.getString("EXTENSION");
                }
            } catch (SQLException e) {
                log.error("Exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        private void setCertifSchoolDat(final DB2UDB db2) {
            StringBuffer sql = new StringBuffer();
            sql.append(" SELECT SCHOOL_NAME, JOB_NAME, PRINCIPAL_NAME, REMARK2, REMARK4, REMARK5 FROM CERTIF_SCHOOL_DAT ");
            sql.append(" WHERE YEAR = '" + _year + "' AND CERTIF_KINDCD = '110' ");
            log.debug("certif_school_dat sql = " + sql.toString());
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    _schoolName = rs.getString("SCHOOL_NAME");
                    _jobName = rs.getString("JOB_NAME");
                    _principalName = rs.getString("PRINCIPAL_NAME");
                    _hrJobName = rs.getString("REMARK2");
                }
                
            } catch (Exception ex) {
                log.error("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
        
        private Map loadTestItemMst(final DB2UDB db2) {
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT SEMESTER || TESTKINDCD || TESTITEMCD || SCORE_DIV AS KEY, TESTITEMNAME FROM TESTITEM_MST_COUNTFLG_NEW_SDIV ");
            sql.append(" WHERE YEAR = '" + _year + "' ");
            log.debug("testitemmstcountflgnew sql = " + sql.toString());
            
            final Map testItemNames = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    testItemNames.put(rs.getString("KEY"), rs.getString("TESTITEMNAME"));
                }
                
            } catch (Exception ex) {
                log.error("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return testItemNames;
        }

        public String getNendo() {
            return _isSeireki ? _year + "年度" : nao_package.KenjaProperties.gengou(Integer.parseInt(_year)) + "年度";
        }
        
        public String getRegdSemester() {
            return Param.SEMEALL.equals(_semester) ? _ctrlSemester : _semester;
        }
        
        private Map loadSemester(final DB2UDB db2) {
            final Map semesterMap = new TreeMap();
            
            final String sql = "SELECT SEMESTER, SEMESTERNAME FROM SEMESTER_MST WHERE YEAR = '" + _year + "' ORDER BY SEMESTER";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    semesterMap.put(rs.getString("SEMESTER"), rs.getString("SEMESTERNAME"));
                }
            } catch (Exception ex) {
                log.error("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return semesterMap;
        }

        private void loadNameMstZ012(final DB2UDB db2) throws SQLException {
            _isSeireki = false;
            final String sql = "SELECT NAMECD2, NAME1 FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'Z012'";
            PreparedStatement ps = db2.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                if ("2".equals(rs.getString("NAME1"))) _isSeireki = true;
            }
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
            log.debug("(名称マスタZ012):西暦フラグ = " + _isSeireki);
        }

        public List getStaffNames(final DB2UDB db2, final String semester)
        {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final List list = new LinkedList();
            try{
                final StringBuffer stb = new StringBuffer();
                stb.append("SELECT  ");
                stb.append("        CASE WHEN L11.STAFFCD IS NOT NULL THEN VALUE(L1.STAFFNAME_REAL, L1.STAFFNAME) ELSE L1.STAFFNAME END AS TR_NAME1 ");
                stb.append("       ,CASE WHEN L21.STAFFCD IS NOT NULL THEN VALUE(L2.STAFFNAME_REAL, L2.STAFFNAME) ELSE L2.STAFFNAME END AS TR_NAME2 ");
                stb.append("       ,CASE WHEN L31.STAFFCD IS NOT NULL THEN VALUE(L3.STAFFNAME_REAL, L3.STAFFNAME) ELSE L3.STAFFNAME END AS TR_NAME3 ");
                stb.append("FROM    SCHREG_REGD_HDAT T1 ");
                stb.append("LEFT JOIN STAFF_MST L1 ON L1.STAFFCD = T1.TR_CD1 ");
                stb.append("LEFT JOIN STAFF_NAME_SETUP_DAT L11 ON L11.YEAR = T1.YEAR AND L11.STAFFCD = L1.STAFFCD AND L11.DIV = '04' ");
                stb.append("LEFT JOIN STAFF_MST L2 ON L2.STAFFCD = T1.TR_CD2 ");
                stb.append("LEFT JOIN STAFF_NAME_SETUP_DAT L21 ON L21.YEAR = T1.YEAR AND L21.STAFFCD = L2.STAFFCD AND L21.DIV = '04' ");
                stb.append("LEFT JOIN STAFF_MST L3 ON L3.STAFFCD = T1.TR_CD3 ");
                stb.append("LEFT JOIN STAFF_NAME_SETUP_DAT L31 ON L31.YEAR = T1.YEAR AND L31.STAFFCD = L3.STAFFCD AND L31.DIV = '04' ");
                stb.append("WHERE   T1.YEAR = '" + _year + "' ");
                stb.append(    "AND T1.GRADE||T1.HR_CLASS = '" + _grade_hr_class + "' ");
                stb.append("    AND T1.SEMESTER = '" + semester + "' ");
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    if (rs.getString("TR_NAME1") != null) list.add(rs.getString("TR_NAME1"));
                    if (rs.getString("TR_NAME2") != null) list.add(rs.getString("TR_NAME2"));
                    if (rs.getString("TR_NAME3") != null) list.add(rs.getString("TR_NAME3"));
                }
            } catch (Exception ex) {
                log.error("List Staff_name() Staff_name error!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }
        
        private void loadNameMstD046(final DB2UDB db2) {
            
            final StringBuffer sql = new StringBuffer();
//            if ("1".equals(_useClassDetailDat)) {
//                final String field = "SUBCLASS_REMARK" + (SEMEALL.equals(_semester) ? "4" : String.valueOf(Integer.parseInt(_semester)));
//                sql.append(" SELECT CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD AS SUBCLASSCD FROM SUBCLASS_DETAIL_DAT ");
//                sql.append(" WHERE YEAR = '" + _year + "' AND SUBCLASS_SEQ = '008' AND " + field + " = '1'  ");
//            } else {
                final String field = SEMEALL.equals(_semester) ? "NAMESPARE1" : "ABBV" + _semester;
                sql.append(" SELECT NAME1 AS SUBCLASSCD FROM V_NAME_MST ");
                sql.append(" WHERE YEAR = '" + _year + "' AND NAMECD1 = 'D046' AND " + field + " = '1'  ");
//            }
            
            PreparedStatement ps = null;
            ResultSet rs = null;
            _d046List = new ArrayList();
            try {
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    _d046List.add(StringUtils.replace(rs.getString("SUBCLASSCD"), "-", ""));
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }
        
        private static File graphImageFile(final JFreeChart chart, final int dotWidth, final int dotHeight) {
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
    }
}
