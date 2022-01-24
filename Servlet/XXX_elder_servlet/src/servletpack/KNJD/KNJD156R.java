// kanji=漢字
/*
 * $Id: 9f3fe0af03a881676e6f4a9bb5a7aef9a818b2af $
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
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.ui.HorizontalAlignment;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import org.jfree.ui.VerticalAlignment;
import org.jfree.util.TableOrder;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJServletpacksvfANDdb2;
import servletpack.KNJZ.detail.KNJ_EditKinsoku;

/**
 * 学校教育システム 賢者 [成績管理]  成績個票
 */

public class KNJD156R {

    private static final Log log = LogFactory.getLog(KNJD156R.class);
    private static final String _333333 = "333333";
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
        private Map _mockMap = new HashMap();

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
            stb.append("          , T1.GRADE, T1.ATTENDNO, T1.SEMESTER, T1.COURSECD, T1.MAJORCD, T1.COURSECODE, T3.GROUP_CD, REGDH.HR_NAME, REGDH.HR_NAMEABBV, REGDH.HR_CLASS_NAME1 ");
            stb.append("          , CM.COURSENAME, MM.MAJORNAME, CCM.COURSECODENAME, REGDG.GRADE_CD ");
            stb.append(    "FROM    SCHREG_REGD_DAT T1 ");
            stb.append(    "INNER JOIN SEMESTER_MST T2 ON T2.YEAR = T1.YEAR AND T2.SEMESTER = T1.SEMESTER ");
            stb.append(    "LEFT JOIN COURSE_GROUP_CD_DAT T3 ON T3.YEAR = T1.YEAR AND T3.GRADE = T1.GRADE AND T3.COURSECD = T1.COURSECD AND T3.MAJORCD = T1.MAJORCD AND T3.COURSECODE = T1.COURSECODE ");
            stb.append(    "INNER JOIN SCHREG_BASE_MST BASE ON T1.SCHREGNO = BASE.SCHREGNO ");
            stb.append(    "INNER JOIN SCHREG_REGD_HDAT REGDH ON REGDH.YEAR = T1.YEAR AND ");
            stb.append(        "REGDH.SEMESTER = T1.SEMESTER AND ");
            stb.append(        "REGDH.GRADE = T1.GRADE AND REGDH.HR_CLASS = T1.HR_CLASS ");
            stb.append(    "INNER JOIN SCHREG_REGD_GDAT REGDG ON REGDG.YEAR = T1.YEAR AND REGDG.GRADE = T1.GRADE ");
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
                stb.append(" SELECT  ");
                stb.append("  T1.CLUBCD AS CD, ");
                stb.append("  CM.CLUBNAME ");
                stb.append(" FROM  ");
                stb.append("  SCHREG_CLUB_HIST_DAT T1 ");
                stb.append("  INNER JOIN CLUB_MST CM ON CM.CLUBCD = T1.CLUBCD ");
                stb.append("  INNER JOIN SEMESTER_MST T2 ON  ");
                stb.append("     ( ");
                stb.append("      T1.SDATE <= T2.SDATE AND (T1.EDATE IS NULL OR T2.SDATE <= T1.EDATE) ");
                stb.append("     OR T1.SDATE BETWEEN T2.SDATE AND T2.EDATE ");
                stb.append("     OR T1.EDATE BETWEEN T2.SDATE AND T2.EDATE ");
                stb.append("    ) ");
                stb.append(" WHERE ");
                stb.append("   T2.YEAR = '" + param._year + "' ");
                stb.append("   AND T2.SEMESTER = ? ");
                stb.append("   AND T1.SCHREGNO = ? ");
                stb.append(" ORDER BY  ");
                stb.append("  T1.SDATE, ");
                stb.append("  T1.CLUBCD ");
                
                final String sql = stb.toString();
                ps = db2.prepareStatement(sql);

                final String[] semes = param.getTargetSemester();
                for (int i = 0; i < semes.length; i++) {
                    ps.setString(1, semes[i]);

                    for (final Iterator it = students.iterator(); it.hasNext();) {
                        final Student student= (Student) it.next();
                        ps.setString(2, student._schregno);
                        rs = ps.executeQuery();
                        
                        final Set addSet = new HashSet();
                        
                        while (rs.next()) {
                            if (null == rs.getString("CLUBNAME")) {
                                continue;
                            }
                            if (addSet.contains(rs.getString("CD"))) {
                                continue;
                            }
                            final String key = "CLUB" + semes[i];
                            String shoken = StringUtils.defaultString((String) student._shokenMap.get(key));
                            if (!StringUtils.isBlank(shoken)) {
                                shoken += "\n";
                            }
                            student._shokenMap.put(key, shoken + rs.getString("CLUBNAME"));
                            addSet.add(rs.getString("CD"));
                        }
                    }
                }

            } catch (SQLException e) {
                log.error("sql exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT  ");
                stb.append("  T1.COMMITTEE_FLG, ");
                stb.append("  T1.COMMITTEECD, ");
                stb.append("  T1.EXECUTIVECD, ");
                stb.append("  CM.COMMITTEENAME, ");
                stb.append("  NMJ003.NAME1 AS COMMITTEE_FLG_NAME, ");
                stb.append("  NMJ002.NAME1 AS EXECUTIVECD_NAME ");
                stb.append(" FROM  ");
                stb.append("  SCHREG_COMMITTEE_HIST_DAT T1 ");
                stb.append("  INNER JOIN COMMITTEE_MST CM ON CM.COMMITTEE_FLG = T1.COMMITTEE_FLG ");
                stb.append("     AND CM.COMMITTEECD = T1.COMMITTEECD ");
                stb.append("  LEFT JOIN NAME_MST NMJ003 ON NMJ003.NAMECD1 = 'J003' AND NMJ003.NAMECD2 = T1.COMMITTEE_FLG ");
                stb.append("  LEFT JOIN NAME_MST NMJ002 ON NMJ002.NAMECD1 = 'J002' AND NMJ002.NAMECD2 = T1.EXECUTIVECD ");
                stb.append(" WHERE ");
                stb.append("   T1.YEAR = '" + param._year + "' ");
                stb.append("   AND T1.SEMESTER = ? ");
                stb.append("   AND T1.SCHREGNO = ? ");
                stb.append(" ORDER BY  ");
                stb.append("  T1.SEQ ");
                
                final String sql = stb.toString();
                ps = db2.prepareStatement(sql);

                final String[] semes = param.getTargetSemester();
                for (int i = 0; i < semes.length; i++) {
                    ps.setString(1, semes[i]);

                    for (final Iterator it = students.iterator(); it.hasNext();) {
                        final Student student= (Student) it.next();
                        ps.setString(2, student._schregno);
                        rs = ps.executeQuery();
                        
                        final Set addSet = new HashSet();
                        
                        while (rs.next()) {
                            if (null == rs.getString("COMMITTEENAME")) {
                                continue;
                            }
                            final String cd = rs.getString("COMMITTEE_FLG") + rs.getString("COMMITTEECD");
                            if (addSet.contains(cd)) {
                                continue;
                            }
                            final String key = "COMMITTEE" + semes[i];
                            String shoken = StringUtils.defaultString((String) student._shokenMap.get(key));
                            if (!StringUtils.isBlank(shoken)) {
                                shoken += "\n";
                            }
                            student._shokenMap.put(key, shoken + rs.getString("COMMITTEENAME"));
                            addSet.add(cd);
                        }
                    }
                }

            } catch (SQLException e) {
                log.error("sql exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }

            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     REMARK1");
                stb.append(" FROM ");
                stb.append("     HEXAM_RECORD_REMARK_SDIV_DAT ");
                stb.append(" WHERE ");
                stb.append("     YEAR = '" + param._year + "' ");
                stb.append("     AND SEMESTER = '9' ");
                stb.append("     AND TESTKINDCD = '00' AND TESTITEMCD = '00' AND SCORE_DIV = '00' ");
                stb.append("     AND REMARK_DIV = '5' ");
                stb.append("     AND SCHREGNO = ? ");
                final String sql = stb.toString();
                ps = db2.prepareStatement(sql);

                for (final Iterator it = students.iterator(); it.hasNext();) {
                    final Student student= (Student) it.next();
                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();
                    
                    while (rs.next()) {
                        student._shokenMap.put("REMARK1", rs.getString("REMARK1"));
                    }
                }
            } catch (SQLException e) {
                log.error("sql exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }

            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     SEMESTER ");
                stb.append("     , REMARK2 ");
                stb.append(" FROM ");
                stb.append("     HEXAM_RECORD_REMARK_SDIV_DAT ");
                stb.append(" WHERE ");
                stb.append("     YEAR = '" + param._year + "' ");
                stb.append("     AND SEMESTER <= '" + param._semester + "' ");
                stb.append("     AND TESTKINDCD = '00' AND TESTITEMCD = '00' AND SCORE_DIV = '00' ");
                stb.append("     AND REMARK_DIV = '6' ");
                stb.append("     AND SCHREGNO = ? ");
                final String sql = stb.toString();
                ps = db2.prepareStatement(sql);

                for (final Iterator it = students.iterator(); it.hasNext();) {
                    final Student student= (Student) it.next();
                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();
                    
                    while (rs.next()) {
                        student._shokenMap.put("OTHER" + rs.getString("SEMESTER"), rs.getString("REMARK2"));
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
                    
                    final String gradeCd = rs.getString("GRADE_CD");

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
                    student._gradeHrName = "第" + (NumberUtils.isDigits(gradeCd) ? String.valueOf(Integer.parseInt(gradeCd)) : " ") + "学年　" + StringUtils.defaultString(rs.getString("HR_CLASS_NAME1")) + "組";
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
            Mock.setMockListMap(db2, param, studentList);
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

        public String getRankDiff(final String beforeTestcd, final String testcd) {
            final String rank = (String) _scoreMap.get("RANK" + testcd);
            String printRank = "";
            if (NumberUtils.isDigits(rank)) {
                final String beforeRank = (String) _scoreMap.get("RANK" + beforeTestcd);
                if (NumberUtils.isDigits(beforeRank)) {
                    final int diff = Integer.parseInt(beforeRank) - Integer.parseInt(rank);
                    printRank = String.valueOf(diff);
                    if (diff > 0) {
                        printRank = "+" + printRank;
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
                        
                        if (param.isD046ContainSubclasscd(subclasscd)) {
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
                        final String score = rs.getString("SCORE");
                        final String avg = rs.getString("AVG");
                        final String gaihyo = rs.getString("GAIHYO");
                        final String rank = rs.getString("RANK");
                        final BigDecimal deviation = rs.getBigDecimal("DEVIATION");
                        if (null != testcd) {
                            if (null != score) {
                                subclass._scoreMap.put("SCORE" + testcd, score);
                            }
                            if (null != avg) {
                                subclass._scoreMap.put("AVG" + testcd, avg);
                            }
                            if (null != avg) {
                                subclass._scoreMap.put("GAIHYO" + testcd, gaihyo);
                            }
                            if (null != rank) {
                                subclass._scoreMap.put("RANK" + testcd, rank);
                            }
                            if (null != deviation) {
                                subclass._scoreMap.put("DEVIATION" + testcd, deviation.setScale(1, BigDecimal.ROUND_HALF_UP));
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
            stb.append("         T2.CLASSCD || '-' || T2.SCHOOL_KIND ||  '-' || T2.CURRICULUM_CD ||  '-' || T2.SUBCLASSCD AS SUBCLASSCD  ");
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
            stb.append("    GROUP BY  ");
            stb.append("         T1.SCHREGNO,  ");
            stb.append("         T2.CLASSCD,  ");
            stb.append("         T2.SCHOOL_KIND,  ");
            stb.append("         T2.CURRICULUM_CD,  ");
            stb.append("         T2.CLASSCD || '-' || T2.SCHOOL_KIND ||  '-' || T2.CURRICULUM_CD ||  '-' || T2.SUBCLASSCD  ");
            stb.append("  ) ");
            stb.append("  SELECT  VALUE(T7.SHOWORDER3, 99) AS CLASSSHOWORDER2, VALUE(T4.SHOWORDER3, 99) AS SUBCLASSSHOWORDER2, T2.CLASS_KEY, T2.SUBCLASSCD, T7.CLASSNAME, T7.CLASSABBV, VALUE(T4.SUBCLASSORDERNAME2, T4.SUBCLASSNAME) AS SUBCLASSNAME, SUBCLASSABBV ");
            stb.append("        , T_SCORE.SEMESTER || T_SCORE.TESTKINDCD || T_SCORE.TESTITEMCD || T_SCORE.SCORE_DIV AS TESTCD  ");
            stb.append("        , T_RANK.SCORE  ");
            if ("1".equals(param._avgDiv)) {
                stb.append("        , T_RANK.GRADE_AVG_RANK AS RANK  ");
                stb.append("        , T_RANK.GRADE_DEVIATION AS DEVIATION  ");
            } else if ("3".equals(param._avgDiv)) {
                stb.append("        , T_RANK.COURSE_AVG_RANK AS RANK  ");
                stb.append("        , T_RANK.COURSE_DEVIATION AS DEVIATION  ");
            } else {
                stb.append("        , T_RANK.MAJOR_AVG_RANK AS RANK  ");
                stb.append("        , T_RANK.MAJOR_DEVIATION AS DEVIATION  ");
            }
            stb.append("        , CAST(NULL AS DECIMAL(9, 5)) AS AVG  ");
            stb.append("        , CAST(NULL AS VARCHAR(1)) AS GAIHYO  ");
            stb.append("  FROM    CHAIR_A T2 ");
            stb.append("  INNER JOIN REGD T3 ON T3.SCHREGNO = T2.SCHREGNO ");
            stb.append("  LEFT JOIN SUBCLASS_MST T4 ON T4.CLASSCD || '-' || T4.SCHOOL_KIND || '-' || T4.CURRICULUM_CD || '-' || T4.SUBCLASSCD = T2.SUBCLASSCD ");
            stb.append("  LEFT JOIN CLASS_MST T7 ON T7.CLASSCD = T4.CLASSCD AND T7.SCHOOL_KIND = T4.SCHOOL_KIND  ");
            stb.append("  LEFT JOIN RECORD_SCORE_DAT T_SCORE ON T_SCORE.YEAR = '" + param._year + "'  ");
            stb.append("         AND T_SCORE.SEMESTER || T_SCORE.TESTKINDCD || T_SCORE.TESTITEMCD || T_SCORE.SCORE_DIV IN " + SQLUtils.whereIn(true, printTestcd) + " ");
            stb.append("         AND T_SCORE.SCHREGNO = T2.SCHREGNO ");
            stb.append("         AND T_SCORE.CLASSCD || '-' || T_SCORE.SCHOOL_KIND || '-' || T_SCORE.CURRICULUM_CD || '-' || T_SCORE.SUBCLASSCD = T2.SUBCLASSCD ");
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
            stb.append("  WHERE ");
            stb.append("      T2.CLASSCD < '90' ");
            stb.append("  UNION ALL ");
            stb.append("  SELECT  999 AS CLASSSHOWORDER2, 999 AS SUBCLASSSHOWORDER2, '99' AS CLASS_KEY, T_RANK.SUBCLASSCD, CAST(NULL AS VARCHAR(1)) AS CLASSNAME, CAST(NULL AS VARCHAR(1)) AS CLASSABBV, CAST(NULL AS VARCHAR(1)) AS SUBCLASSNAME, CAST(NULL AS VARCHAR(1)) AS SUBCLASSABBV ");
            stb.append("        , T_RANK.SEMESTER || T_RANK.TESTKINDCD || T_RANK.TESTITEMCD || T_RANK.SCORE_DIV AS TESTCD  ");
            stb.append("        , CAST(NULL AS SMALLINT) AS SCORE  ");
            if ("1".equals(param._avgDiv)) {
                stb.append("        , T_RANK.GRADE_AVG_RANK AS RANK  ");
                stb.append("        , T_RANK.GRADE_DEVIATION AS DEVIATION  ");
            } else if ("3".equals(param._avgDiv)) {
                stb.append("        , T_RANK.COURSE_AVG_RANK AS RANK  ");
                stb.append("        , T_RANK.COURSE_DEVIATION AS DEVIATION  ");
            } else {
                stb.append("        , T_RANK.MAJOR_AVG_RANK AS RANK  ");
                stb.append("        , T_RANK.MAJOR_DEVIATION AS DEVIATION  ");
            }
            stb.append("        , T_RANK.AVG  ");
            stb.append("        , T4.RANK_MARK AS GAIHYO  ");
            stb.append("  FROM    RECORD_RANK_SDIV_DAT T_RANK ");
            stb.append("  INNER JOIN REGD T3 ON T3.SCHREGNO = T_RANK.SCHREGNO ");
            stb.append("  LEFT JOIN ASSESS_RANK_SDIV_MST T4 ON T4.YEAR = T_RANK.YEAR AND T4.SEMESTER = T_RANK.SEMESTER AND T4.TESTKINDCD = T_RANK.TESTKINDCD AND T4.TESTITEMCD = T_RANK.TESTITEMCD AND T4.SCORE_DIV = T_RANK.SCORE_DIV ");
            if ("1".equals(param._avgDiv)) {
                // ASSESS_RANK_SDIV_MST区分はコース
                stb.append("  AND T4.DIV = '3' AND T4.GRADE = T3.GRADE AND T4.COURSECD = T3.COURSECD AND T4.MAJORCD = T3.MAJORCD AND T4.COURSECODE = T3.COURSECODE ");
                stb.append("  AND T_RANK.GRADE_AVG_RANK BETWEEN T4.RANK_LOW AND T4.RANK_HIGH ");
            } else if ("3".equals(param._avgDiv)) {
                stb.append("  AND T4.DIV = '3' AND T4.GRADE = T3.GRADE AND T4.COURSECD = T3.COURSECD AND T4.MAJORCD = T3.MAJORCD AND T4.COURSECODE = T3.COURSECODE ");
                stb.append("  AND T_RANK.COURSE_AVG_RANK BETWEEN T4.RANK_LOW AND T4.RANK_HIGH ");
            } else {
                stb.append("  AND T4.DIV = '5' AND T4.GRADE = T3.GRADE AND T4.COURSECD = '0' AND T4.MAJORCD = T3.GROUP_CD AND T4.COURSECODE = '0000' ");
                stb.append("  AND T_RANK.MAJOR_AVG_RANK BETWEEN T4.RANK_LOW AND T4.RANK_HIGH ");
            }
            stb.append("  WHERE T_RANK.YEAR = '" + param._year + "'  ");
            stb.append("        AND T_RANK.SEMESTER || T_RANK.TESTKINDCD || T_RANK.TESTITEMCD || T_RANK.SCORE_DIV IN " + SQLUtils.whereIn(true, printTestcd) + " ");
            stb.append("        AND T_RANK.SUBCLASSCD IN ('333333', '555555') ");
            stb.append("  ORDER BY ");
            stb.append("     CLASSSHOWORDER2, CLASS_KEY, SUBCLASSSHOWORDER2, SUBCLASSCD, TESTCD  ");
            return stb.toString();
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

            plot.setLabelFont(new Font("TimesRoman", Font.PLAIN, 10));
            plot.setValueLabelFont(new Font("TimesRoman", Font.PLAIN, 10));
            plot.setLabelPaint(Color.black);

            final JFreeChart chart = new JFreeChart(null, null, plot, false);
            chart.setBackgroundPaint(Color.white);
            chart.setTitle(new TextTitle("レーダーチャート（" + StringUtils.defaultString(title) + "）", new Font("TimesRoman", Font.PLAIN, 12), Color.black, RectangleEdge.TOP, HorizontalAlignment.LEFT, VerticalAlignment.TOP, RectangleInsets.ZERO_INSETS));

            return chart;
        }
    }
    
    private File getRadarGraph(final Param param, final Student student) {
        // データ作成
        final DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (final Iterator it = student._subclassList.iterator(); it.hasNext();) {
            final Subclass subclass = (Subclass) it.next();
            
            if (_333333.equals(subclass._subclasscd) || _555555.equals(subclass._subclasscd)) {
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
            final String title = (String) param._testItemNames.get(param._testCd);
            final int w = 1484;
            final int h = 1384;
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

    private void printMain(final Vrw32alp svf, final Param param, final Student student) {
        final String form1;
        final String form2;
        form1 = "2".equals(param._outputDiv) ? "KNJD156R_2_2.frm" : "KNJD156R_1_2.frm";
        form2 = "KNJD156R_3_2.frm";
        log.info(" form = " + form1);
        svf.VrSetForm(form1, 4);
        
        svf.VrsOut("TITLE", KenjaProperties.gengou(Integer.parseInt(param._year))+ "年度　　成　績　個　票"); // 
        svf.VrsOut("SCHOOL_NAME", param._schoolName); // 学校名
        svf.VrsOut("GRADE_HR_NAME", student._gradeHrName + "　" + student._attendNo); // 年組番
        if (!param._isNotPrintNote) {
            svf.VrsOut("TITLE_GAIHYO1", "概評");
            svf.VrsOut("TITLE_GAIHYO2", "概評");
        }
        svf.VrsOut("NAME", student._name); // 氏名
        if (param._staffNames.size() > 0) {
            final String staffname = (String) param._staffNames.get(0);
            if (null != staffname) {
                final int keta = getMS932ByteLength(staffname);
                final int fieldKeta = keta > 30 ? 40 : keta > 20 ? 30 : 20;
                svf.VrsOut("TEACHER_NAME" + (keta > 30 ? "3" : keta > 20 ? "2" : "1"), staffname + " " + StringUtils.repeat(" ", fieldKeta - keta - 1 - 2) + "印"); // 担任名
            }
            if (param._staffNames.size() > 1) {
                final String staffname2 = (String) param._staffNames.get(1);
                if (null != staffname2) {
                    final int keta2 = getMS932ByteLength(staffname2);
                    final int fieldKeta2 = keta2 > 30 ? 40 : keta2 > 20 ? 30 : 20;
                    svf.VrsOut("TEACHER2_NAME" + (keta2 > 30 ? "3" : keta2 > 20 ? "2" : "1"), staffname2 + " " + StringUtils.repeat(" ", fieldKeta2 - keta2 - 1 - 2) + "印"); // 担任名2
                }
            }
        }

        final String[] fieldFooter = {"1_1", "1_2", "2_1", "2_2", "3"};
        for (int testi = 0; testi < param._testCds.length; testi++) {
            if (fieldFooter.length <= testi) {
                break;
            }
//            final String semestername = (String) param._semesterMap.get(param._testCds[testi].substring(0, 1));
            final String testitemname = (String) param._testItemNames.get(param._testCds[testi]);
            svf.VrsOut("SEMESTER_NAME" + fieldFooter[testi], testitemname); // 学期名
        }
        
        if (!param._isNotPrintNote) {
            svf.VrsOut("GENERAL_VIEW_AREA", (String) param._assessRankSdivMstText.get(param.getAssessRankSdivMstKey(student._grade, student._courseCd, student._majorCd, student._courseCode, student._groupCd))); // 概評範囲
        }
        
        if ("2".equals(param._outputDiv)) {
            svf.VrsOut("SEMESTER_NAME1_3", (String) param._semesterMap.get("1")); // 学期名
            svf.VrsOut("SEMESTER_NAME2_3", (String) param._semesterMap.get("2")); // 学期名
            svf.VrsOut("SEMESTER_NAME3_3", (String) param._semesterMap.get("3")); // 学期名
            
            final String[] semes = param.getTargetSemester();
            for (int j = 0; j < semes.length; j++) {
                final int i = Integer.parseInt(semes[j]);
                svfVrsOutnKurikaeshi(svf, "CLUB",      i, KNJ_EditKinsoku.getTokenList((String) student._shokenMap.get("CLUB" + semes[j]),      18, 5));
                svfVrsOutnKurikaeshi(svf, "COMMITTEE", i, KNJ_EditKinsoku.getTokenList((String) student._shokenMap.get("COMMITTEE" + semes[j]), 18, 5));
                svfVrsOutnKurikaeshi(svf, "OTHER",     i, KNJ_EditKinsoku.getTokenList((String) student._shokenMap.get("OTHER" + semes[j]),     18, 5));
            }
        } else {
            final File outputFile = getRadarGraph(param, student);
            if (null != outputFile) {
                // グラフの出力
                svf.VrsOut("Bitmap_Field1", outputFile.toString());
            }
        }

        final List viewToken = KNJ_EditKinsoku.getTokenList((String) student._shokenMap.get("REMARK1"), 30, 13);
        if (null != viewToken) {
            for (int j = 0; j < viewToken.size(); j++) {
                svf.VrsOutn("VIEW", j + 1, (String) viewToken.get(j)); // 所見
            }
        }

        printJituryoku(svf, param, student, 0, 12);

        final Map subclassMap = student.getSubclassMap();
        for (int testi = 0; testi < param._testCds.length; testi++) {
            if (fieldFooter.length <= testi) {
                break;
            }
            if (null != subclassMap.get(_333333)) {
                final Subclass subc = (Subclass) subclassMap.get(_333333);
                svf.VrsOut("SCORE5_" + fieldFooter[testi], sishaGonyu((String) subc._scoreMap.get("AVG" + param._testCds[testi]))); // 平均
                if (!param._isNotPrintNote) {
                    svf.VrsOut("GENERAL_VIEW3_" + fieldFooter[testi], (String) subc._scoreMap.get("GAIHYO" + param._testCds[testi])); // 概評
                }
                if (1 <= testi) {
                    svf.VrsOut("RANK3_" + fieldFooter[testi], subc.getRankDiff(param._testCds[testi - 1], param._testCds[testi])); // 順位変動
                }
            }
            
            if (null != subclassMap.get(_555555)) {
                final Subclass subc = (Subclass) subclassMap.get(_555555);
                svf.VrsOut("SCOREALL_" + fieldFooter[testi], sishaGonyu((String) subc._scoreMap.get("AVG" + param._testCds[testi]))); // 平均
                if (!param._isNotPrintNote) {
                    svf.VrsOut("GENERAL_VIEW5_" + fieldFooter[testi], (String) subc._scoreMap.get("GAIHYO" + param._testCds[testi])); // 概評
                }
                if (1 <= testi) {
                    svf.VrsOut("RANK5_" + fieldFooter[testi], subc.getRankDiff(param._testCds[testi - 1], param._testCds[testi])); // 順位変動
                }
            }
        }

        final int maxSubclass = 20;
        int print = 0;
        for (int i = 0; i < student._subclassList.size(); i++) {
            final Subclass subclass = (Subclass) student._subclassList.get(i);
            if (subclass == subclassMap.get(_333333) || subclass == subclassMap.get(_555555)) {
                continue;
            }
            final String subclassname = StringUtils.defaultString(subclass._subclassname);
            if (subclassname.length() <= 5) {
                svf.VrsOut("SUBCLASS_NAME1", subclassname); // 科目名
            } else if (subclassname.length() <= 10) {
                svf.VrsOut("SUBCLASS_NAME2_1", subclassname.substring(0, 5)); // 科目名
                svf.VrsOut("SUBCLASS_NAME2_2", subclassname.substring(5)); // 科目名
            } else {
                svf.VrsOut("SUBCLASS_NAME3_1", subclassname.substring(0, 7)); // 科目名
                svf.VrsOut("SUBCLASS_NAME3_2", subclassname.substring(7)); // 科目名
            }
            
            for (int testi = 0; testi < param._testCds.length; testi++) {
                if (fieldFooter.length <= testi) {
                    break;
                }
                final String score = (String) subclass._scoreMap.get("SCORE" + param._testCds[testi]);
                svf.VrsOut("SCORE" + fieldFooter[testi], score); // 学期名
            }
            svf.VrEndRecord();
            print += 1;
        }
        for (int i = print; i < maxSubclass; i++) {
            svf.VrsOut("SUBCLASS_NAME1", ""); // 科目名
            svf.VrEndRecord();
        }

        svf.VrSetForm(form2, 1);
        printJituryoku(svf, param, student, 12, 12 + 28);
        svf.VrEndPage();
    }


    private static String sishaGonyu(final String s) {
        if (!NumberUtils.isNumber(s)) {
            return null;
        }
        final String rtn = new BigDecimal(s).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
        return rtn;
    }


    private void svfVrsOutnKurikaeshi(final Vrw32alp svf, final String field, final int j, final List token) {
        if (null != token) {
            for (int i = 0; i < token.size(); i++) {
                svf.VrsOutn(field + String.valueOf(i + 1), j, (String) token.get(i));
            }
        }
    }
    
    private void printJituryoku(final Vrw32alp svf, final Param param, final Student student, final int startIndex, final int endIndexExclude) {
        
        final Map map = student._mockMap;
        final List subclassCdList = getMappedList(map, "SUBCLASSCD");
        
        final int subclassMax = 14;
        for (int i = 0; i < Math.min(subclassCdList.size(),  subclassMax); i++) {
            final String subclasscd = (String) subclassCdList.get(i);
            if (_999999.equals(subclasscd)) {
                continue;
            }
            svf.VrsOut("MOCK_CLASS_NAME" + String.valueOf(i + 1), (String) getMappedMap(map, "SUBCLASSNAME").get(subclasscd)); // 実力テスト教科名
        }
        boolean isPrint = false;
        for (int j = 0; j < endIndexExclude - startIndex && j + startIndex < getMappedList(map, "TESTCD").size(); j++) {
            final String mockcd = (String) getMappedList(map, "TESTCD").get(j + startIndex);
            final String mockname = (String) getMappedMap(map, "TESTNAME").get(mockcd);
            final Map mockDataMap = getMappedMap(getMappedMap(map, "TEST_DATA"), mockcd);
            final int line = j + 1;
            svf.VrsOutn("MOCK_NAME", line, mockname); // 実力テスト名称
            svf.VrsOutn("MOCK_DATE", line, null); // 実力テスト実施日
            
            final List deviationList = new ArrayList();
            for (int i = 0; i < Math.min(subclassCdList.size(),  subclassMax); i++) {
                final String subclasscd = (String) subclassCdList.get(i);
                if (_999999.equals(subclasscd)) {
                    continue;
                }
                String dev = null;
                final Mock mock = (Mock) mockDataMap.get(subclasscd);
                if (null != mock) {
                    dev = mock._gradeDeviation;
                }
                if (null != dev) {
                    svf.VrsOutn("MOCK_CLASS_DEVIATION" + String.valueOf(i + 1), line, sishaGonyu(dev)); // 実力テスト偏差値
                    deviationList.add(dev);
                }
            }

//            svf.VrsOutn("MOCK_DEVIATION_ALL", line, avg(deviationList)); // 実力テスト総合
            final Mock mock999999 = (Mock) mockDataMap.get(_999999);
            if (null != mock999999) {
                svf.VrsOutn("MOCK_DEVIATION_ALL", line, sishaGonyu(mock999999._gradeDeviation)); // 実力テスト総合
                svf.VrsOutn("MOCK_DEVIATION_RANK", line, mock999999._gradeRank); // 実力テスト順位
                svf.VrsOutn("MOCK_DEVIATION_SUM", line, mock999999._count); // 実力テスト参加人数
            }
            isPrint = true;
        }
        if (!isPrint) {
            svf.VrsOutn("MOCK_NAME", 1, "　"); // 実力テスト名称
        }
    }

    private static String avg(final List list) {
        BigDecimal bd = new BigDecimal(0);
        int c = 0;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final String s = (String) it.next();
            if (NumberUtils.isNumber(s)) {
                bd = bd.add(new BigDecimal(s));
                c += 1;
            }
        }
        if (c == 0) {
            return null;
        }
        return bd.divide(new BigDecimal(c), 1, BigDecimal.ROUND_HALF_UP).toString();
    }

    private static class Mock {
        final String _mockcd;
        final String _mockname1;
        final String _mockSubclassCd;
        final String _subclassName;
        final String _score;
        final String _gradeRank;
        final String _gradeDeviation;
        final String _count;

        Mock(
            final String mockcd,
            final String mockname1,
            final String mockSubclassCd,
            final String subclassName,
            final String score,
            final String gradeRank,
            final String gradeDeviation,
            final String count
        ) {
            _mockcd = mockcd;
            _mockname1 = mockname1;
            _mockSubclassCd = mockSubclassCd;
            _subclassName = subclassName;
            _score = score;
            _gradeRank = gradeRank;
            _gradeDeviation = gradeDeviation;
            _count = count;
        }

        public static void setMockListMap(final DB2UDB db2, final Param param, final List studentList) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = sql(param);
                ps = db2.prepareStatement(sql);
                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();
                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();
                    final Map map = new HashMap();
                    final Set subclassSet = new HashSet();
                    final Set testcdSet = new HashSet();
                    while (rs.next()) {
                        final String subclassCd = rs.getString("SUBCLASSCD");
                        if ("333333".equals(subclassCd) || "555555".equals(subclassCd) || null == subclassCd) {
                            continue;
                        }
                        final String subclassName = rs.getString("SUBCLASSNAME");
                        if (!subclassSet.contains(subclassCd)) {
                            getMappedList(map, "SUBCLASSCD").add(subclassCd);
                            getMappedMap(map, "SUBCLASSNAME").put(subclassCd, subclassName);
                            subclassSet.add(subclassCd);
                        }
                        final String mockcd = rs.getString("MOCKCD");
                        final String mockname1 = rs.getString("MOCKNAME1");
                        if (!testcdSet.contains(mockcd)) {
                            getMappedList(map, "TESTCD").add(mockcd);
                            getMappedMap(map, "TESTNAME").put(mockcd, mockname1);
                            testcdSet.add(mockcd);
                        }
                        final String score = rs.getString("SCORE");
                        final String gradeRank = rs.getString("GRADE_RANK");
                        final String gradeDeviation = rs.getString("GRADE_DEVIATION");
                        final String count = rs.getString("COUNT");
                        final Mock mock = new Mock(mockcd, mockname1, subclassCd, subclassName, score, gradeRank, gradeDeviation, count);
                        getMappedMap(getMappedMap(map, "TEST_DATA"), mockcd).put(subclassCd, mock);
                    }
                    student._mockMap = map;
                    DbUtils.closeQuietly(rs);
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
            stb.append("   T1.MOCKCD, ");
            stb.append("   T2.MOCKNAME1, ");
            stb.append("   CASE WHEN T1.MOCK_SUBCLASS_CD = '" + _999999 + "' THEN '" + _999999 + "' ELSE T4.CLASSCD || '-' || T4.SCHOOL_KIND || '-' || T4.CURRICULUM_CD || '-' || T4.SUBCLASSCD END AS SUBCLASSCD, ");
            stb.append("   VALUE(SUB.SUBCLASSABBV, SUB.SUBCLASSNAME) AS SUBCLASSNAME, ");
            stb.append("   T1.SCORE, ");
            stb.append("   T1.RANK AS GRADE_RANK, ");
            stb.append("   T1.DEVIATION AS GRADE_DEVIATION, ");
            stb.append("   T1.CNT AS COUNT ");
            stb.append(" FROM MOCK_RANK_RANGE_DAT T1 ");
            stb.append(" INNER JOIN MOCK_MST T2 ON T2.MOCKCD = T1.MOCKCD ");
            stb.append(" LEFT JOIN MOCK_SUBCLASS_MST T4 ON T4.MOCK_SUBCLASS_CD = T1.MOCK_SUBCLASS_CD ");
            stb.append(" LEFT JOIN SUBCLASS_MST SUB ON T4.CLASSCD = SUB.CLASSCD AND T4.SCHOOL_KIND = SUB.SCHOOL_KIND AND T4.CURRICULUM_CD = SUB.CURRICULUM_CD AND T4.SUBCLASSCD = SUB.SUBCLASSCD ");
            stb.append(" LEFT JOIN (SELECT DISTINCT YEAR, GRADE, MIN(SEQ) AS SEQ, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD FROM RECORD_MOCK_ORDER_SUB_DAT GROUP BY YEAR, GRADE, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD) T5 ON T5.YEAR = T1.YEAR ");
            stb.append("    AND T5.GRADE = '" + param._grade_hr_class.substring(0, 2) + "' ");
            stb.append("    AND T5.CLASSCD = T4.CLASSCD ");
            stb.append("    AND T5.SCHOOL_KIND = T4.SCHOOL_KIND ");
            stb.append("    AND T5.CURRICULUM_CD = T4.CURRICULUM_CD ");
            stb.append("    AND T5.SUBCLASSCD = T4.SUBCLASSCD ");
            stb.append(" INNER JOIN (SELECT DISTINCT YEAR, GRADE, MOCKCD FROM RECORD_MOCK_ORDER_DAT WHERE TEST_DIV = '3' GROUP BY YEAR, GRADE, MOCKCD) T7 ON T7.YEAR = T1.YEAR ");
            stb.append("    AND T7.GRADE = '" + param._grade_hr_class.substring(0, 2) + "' ");
            stb.append("    AND T7.MOCKCD = T1.MOCKCD ");
            stb.append(" WHERE ");
            stb.append("   T1.YEAR = '" + param._year + "' ");
            stb.append("   AND T1.SCHREGNO = ? ");
            stb.append("   AND T1.RANK_RANGE = '2' "); // 校内
            stb.append("   AND T1.RANK_DIV = '02' "); // 学年
            stb.append("   AND T1.MOCKDIV = '1' "); // 合計での順位
            stb.append("   AND (T4.MOCK_SUBCLASS_CD IS NOT NULL AND T1.DEVIATION IS NOT NULL OR T1.MOCK_SUBCLASS_CD = '" + _999999 + "' ");
            stb.append("        AND (T1.RANK IS NOT NULL OR T1.DEVIATION IS NOT NULL OR T1.CNT IS NOT NULL)) ");
            stb.append(" ORDER BY ");
            stb.append("   VALUE(T2.MOSI_DATE, '9999-12-31'), T1.MOCKCD, VALUE(T5.SEQ, 99), CASE WHEN T1.MOCK_SUBCLASS_CD = '" + _999999 + "' THEN '" + _999999 + "' ELSE T4.CLASSCD || '-' || T4.SCHOOL_KIND || '-' || T4.CURRICULUM_CD || '-' || T4.SUBCLASSCD END, T1.MOCK_SUBCLASS_CD "); // 同一の賢者科目は模試科目MAXを表示
            return stb.toString();
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
        final String _avgDiv; // 1:学年 3:コース 5:コースグループ
        final File _logoFile;
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
        Map _assessRankSdivMstText = Collections.EMPTY_MAP;
        
        Param(final HttpServletRequest request, final DB2UDB db2) {
            log.debug(" $Revision: 56595 $");
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
            _avgDiv = request.getParameter("AVG_DIV");
            _isNotPrintNote = "1".equals(request.getParameter("HANREI_SYUTURYOKU_NASI"));
            _documentRoot = request.getParameter("DOCUMENTROOT");
            _isNotPrintAvg = "1".equals(request.getParameter("AVG_PRINT"));
            _dispSemester = new String[]{"1", "2", "3"};
            _useClassDetailDat = request.getParameter("useClassDetailDat");

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
            setAssessRankSdivMst(db2);
        }
        
        public String getAssessRankSdivMstKey(final String grade, final String coursecd, final String majorcd, final String coursecode, final String groupCd) {
            if ("1".equals(_avgDiv) || "3".equals(_avgDiv)) {
                return grade + "-" + coursecd + "-" + majorcd + "-" + coursecode;
            }
            return grade + "-" + groupCd;
        }
        
        public void setAssessRankSdivMst(final DB2UDB db2) {

            final StringBuffer stb = new StringBuffer();
            stb.append("  SELECT * FROM ASSESS_RANK_SDIV_MST T4 ");
            stb.append("  WHERE T4.YEAR = '" + _year + "' AND T4.SEMESTER || T4.TESTKINDCD || T4.TESTITEMCD || T4.SCORE_DIV = '" + _testCd + "' ");
            if ("1".equals(_avgDiv) || "3".equals(_avgDiv)) {
                stb.append("  AND T4.DIV = '3' ");
            } else {
                stb.append("  AND T4.DIV = '5' ");
            }
            stb.append("  ORDER BY RANK_LEVEL ");

            PreparedStatement ps = null;
            ResultSet rs = null;
            _assessRankSdivMstText = new HashMap();
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String rankLow = rs.getString("RANK_LOW");
                    final String rankHigh = rs.getString("RANK_HIGH");
                    if (!NumberUtils.isNumber(rankLow) && !NumberUtils.isNumber(rankHigh)) {
                        continue;
                    }
                    
                    final String key = getAssessRankSdivMstKey(rs.getString("GRADE"), rs.getString("COURSECD"), rs.getString("MAJORCD"), rs.getString("COURSECODE"), rs.getString("MAJORCD"));
                    final String rankMark = rs.getString("RANK_MARK");
                    String text = StringUtils.defaultString(rankLow) + "\uFF5E" + StringUtils.defaultString(rankHigh) + " " + StringUtils.defaultString(rankMark);
                    if (!StringUtils.isBlank((String) _assessRankSdivMstText.get(key))) {
                        text = _assessRankSdivMstText.get(key) + ", " + text; 
                    }
                    _assessRankSdivMstText.put(key, text);
                }
            } catch (SQLException e) {
                log.error("Exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            //log.debug(" assessRankSdivMst = " + _assessRankSdivMstText);
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
            final String key = "109";
            StringBuffer sql = new StringBuffer();
            sql.append(" SELECT SCHOOL_NAME, JOB_NAME, PRINCIPAL_NAME, REMARK2, REMARK4, REMARK5 FROM CERTIF_SCHOOL_DAT ");
            sql.append(" WHERE YEAR = '" + _year + "' AND CERTIF_KINDCD = '" + key + "' ");
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
            return _isSeireki ? _year + "年度" : KenjaProperties.gengou(Integer.parseInt(_year)) + "年度";
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


        public String getSchoolKind(final DB2UDB db2)
        {
            PreparedStatement ps = null;
            ResultSet rs = null;
            String schoolKind = null;
            try{
                final StringBuffer stb = new StringBuffer();
                stb.append("SELECT  ");
                stb.append("        SCHOOL_KIND ");
                stb.append("FROM    SCHREG_REGD_GDAT T1 ");
                stb.append("WHERE   T1.YEAR = '" + _year + "' ");
                stb.append(    "AND T1.GRADE = '" + _grade_hr_class.substring(0, 2) + "' ");
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    schoolKind = rs.getString("SCHOOL_KIND");
                }
            } catch (Exception ex) {
                log.error("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return schoolKind;
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
        
        public boolean isD046ContainSubclasscd(final String subclasscd) {
            return _d046List.contains(subclasscd);
        }
        
        private void loadNameMstD046(final DB2UDB db2) {
            
            final StringBuffer sql = new StringBuffer();
            if ("1".equals(_useClassDetailDat)) {
                final String field = "SUBCLASS_REMARK" + (SEMEALL.equals(_semester) ? "4" : String.valueOf(Integer.parseInt(_semester)));
                sql.append(" SELECT CLASSCD || SCHOOL_KIND || CURRICULUM_CD || SUBCLASSCD AS SUBCLASSCD FROM SUBCLASS_DETAIL_DAT ");
                sql.append(" WHERE YEAR = '" + _year + "' AND SUBCLASS_SEQ = '008' AND " + field + " = '1'  ");
            } else {
                final String field = SEMEALL.equals(_semester) ? "NAMESPARE1" : "ABBV" + _semester;
                sql.append(" SELECT NAME1 AS SUBCLASSCD FROM V_NAME_MST ");
                sql.append(" WHERE YEAR = '" + _year + "' AND NAMECD1 = 'D046' AND " + field + " = '1'  ");
            }
            
            PreparedStatement ps = null;
            ResultSet rs = null;
            _d046List = new ArrayList();
            try {
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String subclasscd = rs.getString("SUBCLASSCD");
                    _d046List.add(subclasscd);
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
