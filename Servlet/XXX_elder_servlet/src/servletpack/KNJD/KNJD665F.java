/*
 * $Id: e84eeb1a2bbf6ab785a0ce96f1237180a6134bc8 $
 *
 * 作成日: 2015/12/22
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;


import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Stroke;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 * 学校教育システム 賢者 [成績管理]
 */
public class KNJD665F {

    private static final Log log = LogFactory.getLog(KNJD665F.class);
    
    private static String _333333 = "333333";
    private static String _555555 = "555555";

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

            printMain(db2, svf);
            
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

    private static List getMappedList(final Map map, final String key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new ArrayList());
        }
        return (List) map.get(key1);
    }
    
    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        
        final Map subclassScoreListMap = SubclassScores.getScoreListMap(db2, _param);
        final String nendo = KNJ_EditDate.getAutoFormatYear(db2, Integer.parseInt(_param._year)) + "年度";
        
        final String subclassname333333 = "3科";
        final String subclassname555555 = "5科";
        
        final List list = Student.getStudentList(db2, _param);
        final Graph graph = new Graph();
        final List files = new ArrayList();
        for (int line = 0; line < list.size(); line++) {
            final Student student = (Student) list.get(line);
            
            log.debug(" student = " + student._schregno);
            final List printScoreList = new ArrayList();
            for (int i = 0; i < student._subclassScoreList.size(); i++) {
                final SubclassScore subScore = (SubclassScore) student._subclassScoreList.get(i);

                final List scoreList = getMappedList(subclassScoreListMap, subScore._subclasscd);
                if (scoreList.isEmpty()) {
                    continue;
                }
                printScoreList.add(subScore);
            }
            if (printScoreList.size() == 0) {
                continue;
            }

            svf.VrSetForm("KNJD665F.frm", 1);
            int subclassCount = 0;
            
			svf.VrsOut("TITLE", nendo + " " + StringUtils.defaultString(_param._testitemname) + " 個人成績表"); // タイトル
            //svf.VrsOut("TEST_NAME", _param._testitemname); // 考査名称
            svf.VrsOut("HR_NAME", "1".equals(_param._printRegd) ? student._hrName2 : student._hrName); // 年組名称
            svf.VrsOut("NAME" + (KNJ_EditEdit.getMS932ByteLength(student._name) > 20 ? "2" : "1"), student._name); // 氏名

            svf.VrsOut("SCHOOL_NAME", _param._schoolName); // 学校名
            
            svf.VrsOutn("CLASS_NAME", 6, subclassname333333); // 教科名
            svf.VrsOutn("CLASS_NAME", 7, subclassname555555); // 教科名
            
            final List subclasscdList = new ArrayList();
            final Map subclassScoreMap = new HashMap();
            boolean contains333333 = false;
            boolean contains555555 = false;
            for (int i = 0; i < printScoreList.size(); i++) {
                final SubclassScore subScore = (SubclassScore) printScoreList.get(i);
                subclasscdList.add(subScore._subclasscd);
                subclassScoreMap.put(subScore._subclasscd, subScore);
            }
            if (!contains333333) {
                subclasscdList.add(_333333);
            }
            if (!contains555555) {
                subclasscdList.add(_555555);
            }

            for (int i = 0; i < subclasscdList.size(); i++) {
                final String subclasscd = (String) subclasscdList.get(i);

                final List scoreList = getMappedList(subclassScoreListMap, subclasscd);
                if (scoreList.isEmpty()) {
                    continue;
                }

                final SubclassScore subScore = (SubclassScore) subclassScoreMap.get(subclasscd);
                final int pos, max;
                final String subclassname;
                if (_333333.equals(subclasscd)) {
                    pos = 6;
                    subclassname = subclassname333333;
                    max = 300;
                } else if (_555555.equals(subclasscd)) {
                    pos = 7;
                    subclassname = subclassname555555;
                    max = 500;
                } else {
                    if (null == subScore) {
                        continue;
                    }
                    subclassCount += 1;
                    if (subclassCount > 5) {
                        continue;
                    }
                    pos = subclassCount;
                    subclassname = subScore._classname;
                    svf.VrsOutn("CLASS_NAME", pos, subclassname); // 教科名
                    max = 100;
                }
                
                if (null != subScore) {
                    svf.VrsOutn("SCORE", pos, subScore._score); // 得点
                }
                
                if ((_333333.equals(subclasscd) || _555555.equals(subclasscd)) && (null == subScore || null == subScore._score)) {
                    // 3科、5科は得点がないならグラフは表示しない
                    continue;
                }

                final File file = graph.createGraph(_param, scoreList, subclasscd, subScore, subclassname, max);
                if (null != file) {
                    svf.VrsOut("Bitmap_Field" + String.valueOf(pos), file.getAbsolutePath()); // 
                    files.add(file);
                }
            }
            
            svf.VrEndPage();
            _hasData = true;
            
            for (final Iterator it = files.iterator(); it.hasNext();) {
                final File f = (File) it.next();
                f.delete();
            }
        }
    }
    
    private static class Student {
        final String _schregno;
        final String _grade;
        final String _hrClass;
        final String _hrName;
        final String _hrName2;
        final String _attendno;
        final String _name;
        final List _subclassScoreList = new ArrayList();
        final List _files = new ArrayList();

        Student(
                final String schregno,
                final String grade,
                final String hrClass,
                final String hrName,
                final String hrName2,
                final String attendno,
                final String name
        ) {
            _schregno = schregno;
            _grade = grade;
            _hrClass = hrClass;
            _hrName = hrName;
            _hrName2 = hrName2;
            _attendno = attendno;
            _name = name;
        }

        public static List getStudentList(final DB2UDB db2, final Param param) {
            final List studentList = new ArrayList();
            final Map studentMap = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = sql(param);
                //log.debug(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String schregno = rs.getString("SCHREGNO");
                    if (null == studentMap.get(schregno)) {
                        final String grade = rs.getString("GRADE");
                        final String hrClass = rs.getString("HR_CLASS");
                        final String hrName = rs.getString("HR_NAME");
                        final String hrName2 = rs.getString("HR_NAME2");
                        final String attendno = rs.getString("ATTENDNO");
                        final String name = rs.getString("NAME");
                        final Student student = new Student(schregno, grade, hrClass, hrName, hrName2, attendno, name);
                        studentList.add(student);
                        studentMap.put(schregno, student);
                    }
                    
                    final String subclasscd = rs.getString("SUBCLASSCD");
                    if (null != subclasscd) {
                        final String classname = rs.getString("CLASSNAME");
                        final String subclassname = rs.getString("SUBCLASSNAME");
                        final String score = rs.getString("SCORE");
                        final String gradeRank = rs.getString("GRADE_RANK");
                        final Student student = (Student) studentMap.get(schregno);
                        final SubclassScore subScore = new SubclassScore(subclasscd, classname, subclassname, score, gradeRank);
                        student._subclassScoreList.add(subScore);
                    }
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return studentList;
        }

        public static String sql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH REGD AS ( ");
            stb.append(" SELECT ");
            stb.append("     REGD.SCHREGNO, ");
            stb.append("     REGD.YEAR, ");
            stb.append("     REGD.SEMESTER, ");
            stb.append("     REGD.GRADE, ");
            stb.append("     REGD.HR_CLASS, ");
            stb.append("     HDAT.HR_NAME, ");
            stb.append("     REGDH2.HR_NAME AS HR_NAME2, ");
            stb.append("     REGD.ATTENDNO, ");
            stb.append("     BASE.NAME, ");
            stb.append("     REGD.COURSECD, ");
            stb.append("     REGD.MAJORCD, ");
            stb.append("     REGD.COURSECODE ");
            stb.append(" FROM " + param._regdTable + " REGD ");
            stb.append(" INNER JOIN " + param._regdhTable + " HDAT ON HDAT.YEAR = REGD.YEAR ");
            stb.append("     AND HDAT.SEMESTER = REGD.SEMESTER ");
            stb.append("     AND HDAT.GRADE = REGD.GRADE ");
            stb.append("     AND HDAT.HR_CLASS = REGD.HR_CLASS ");
            stb.append(" INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ");
            stb.append(" LEFT JOIN SCHREG_REGD_DAT REGD2 ON REGD2.YEAR = REGD.YEAR ");
            stb.append("        AND REGD.SEMESTER = REGD2.SEMESTER ");
            stb.append("        AND REGD.SCHREGNO = REGD2.SCHREGNO ");
            stb.append(" LEFT JOIN SCHREG_REGD_HDAT REGDH2 ON REGDH2.YEAR = REGD2.YEAR ");
            stb.append("        AND REGD2.SEMESTER = REGDH2.SEMESTER ");
            stb.append("        AND REGD2.GRADE = REGDH2.GRADE ");
            stb.append("        AND REGD2.HR_CLASS = REGDH2.HR_CLASS ");
            stb.append(" WHERE ");
            stb.append("     REGD.YEAR = '" + param._year + "' ");
            stb.append("     AND REGD.SEMESTER = '" + param._semester + "' ");
            if ("1".equals(param._categoryIsClass)) {
                stb.append("     AND REGD.GRADE || '-' || REGD.HR_CLASS IN " + SQLUtils.whereIn(true, param._categorySelected) + " ");
            } else {
                stb.append("     AND REGD.SCHREGNO IN " + SQLUtils.whereIn(true, param._categorySelected) + " ");
            }
            stb.append(" ), NOT_PERFECT AS ( ");
            stb.append(" SELECT DISTINCT I1.GROUP_DIV, REGD.SCHREGNO ");
            stb.append(" FROM REC_SUBCLASS_GROUP_DAT I1 ");
            stb.append(" INNER JOIN " + param._regdTable + " REGD ON REGD.YEAR = I1.YEAR ");
            stb.append("     AND REGD.SEMESTER = '" + param._semester + "' ");
            stb.append("     AND REGD.GRADE = I1.GRADE ");
            stb.append("     AND REGD.COURSECD = I1.COURSECD ");
            stb.append("     AND REGD.MAJORCD = I1.MAJORCD ");
            stb.append("     AND REGD.COURSECODE = I1.COURSECODE ");
            stb.append(" LEFT JOIN RECORD_SCORE_DAT I2 ON I2.YEAR = I1.YEAR ");
            stb.append("     AND I2.SEMESTER = '" + param._semester + "' ");
            stb.append("     AND I2.TESTKINDCD || I2.TESTITEMCD || I2.SCORE_DIV = '" + param._testcd + "' ");
            stb.append("     AND I2.CLASSCD = I1.CLASSCD ");
            stb.append("     AND I2.SCHOOL_KIND = I1.SCHOOL_KIND ");
            stb.append("     AND I2.CURRICULUM_CD = I1.CURRICULUM_CD ");
            stb.append("     AND I2.SUBCLASSCD = I1.SUBCLASSCD ");
            stb.append("     AND I2.SCHREGNO = REGD.SCHREGNO ");
            stb.append(" WHERE I1.YEAR = '" + param._year + "' ");
            stb.append("     AND I1.GROUP_DIV IN ('3', '5') ");
            stb.append("     AND I2.SCORE IS NULL ");
            stb.append(" ) ");
            stb.append(" SELECT DISTINCT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.GRADE, ");
            stb.append("     T1.HR_CLASS, ");
            stb.append("     T1.HR_NAME, ");
            stb.append("     T1.HR_NAME2, ");
            stb.append("     T1.ATTENDNO, ");
            stb.append("     T1.NAME, ");
            stb.append("     T4.CLASSCD || '-' || T4.SCHOOL_KIND || '-' || T4.CURRICULUM_CD || '-' || T4.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("     CLM.CLASSNAME, ");
            stb.append("     SUBM.SUBCLASSNAME, ");
            stb.append("     T_RANK.SCORE, ");
            stb.append("     T_RANK.GRADE_RANK ");
            stb.append(" FROM REGD T1 ");
            stb.append(" LEFT JOIN CHAIR_STD_DAT T3 ON T3.YEAR = T1.YEAR ");
            stb.append("     AND T3.SEMESTER = T1.SEMESTER ");
            stb.append("     AND T3.SCHREGNO = T1.SCHREGNO ");
            stb.append(" LEFT JOIN CHAIR_DAT T4 ON T4.YEAR = T3.YEAR ");
            stb.append("     AND T4.SEMESTER = T3.SEMESTER ");
            stb.append("     AND T4.CHAIRCD = T3.CHAIRCD ");
            stb.append("     AND T4.CLASSCD < '90' ");
            stb.append(" LEFT JOIN SUBCLASS_MST SUBM ON SUBM.CLASSCD = T4.CLASSCD ");
            stb.append("     AND SUBM.SCHOOL_KIND = T4.SCHOOL_KIND ");
            stb.append("     AND SUBM.CURRICULUM_CD = T4.CURRICULUM_CD ");
            stb.append("     AND SUBM.SUBCLASSCD = T4.SUBCLASSCD ");
            stb.append(" LEFT JOIN CLASS_MST CLM ON CLM.CLASSCD = T4.CLASSCD ");
            stb.append("     AND CLM.SCHOOL_KIND = T4.SCHOOL_KIND ");
            stb.append(" LEFT JOIN REC_SUBCLASS_GROUP_DAT T5 ON T5.YEAR = T1.YEAR ");
            stb.append("     AND T5.GROUP_DIV = '3' ");
            stb.append("     AND T5.GRADE = T1.GRADE ");
            stb.append("     AND T5.COURSECD = T1.COURSECD ");
            stb.append("     AND T5.MAJORCD = T1.MAJORCD ");
            stb.append("     AND T5.COURSECODE = T1.COURSECODE ");
            stb.append("     AND T5.CLASSCD = T4.CLASSCD ");
            stb.append("     AND T5.SCHOOL_KIND = T4.SCHOOL_KIND ");
            stb.append("     AND T5.CURRICULUM_CD = T4.CURRICULUM_CD ");
            stb.append("     AND T5.SUBCLASSCD = T4.SUBCLASSCD ");
            stb.append(" LEFT JOIN REC_SUBCLASS_GROUP_DAT T6 ON T6.YEAR = T1.YEAR ");
            stb.append("     AND T6.GROUP_DIV = '5' ");
            stb.append("     AND T6.GRADE = T1.GRADE ");
            stb.append("     AND T6.COURSECD = T1.COURSECD ");
            stb.append("     AND T6.MAJORCD = T1.MAJORCD ");
            stb.append("     AND T6.COURSECODE = T1.COURSECODE ");
            stb.append("     AND T6.CLASSCD = T4.CLASSCD ");
            stb.append("     AND T6.SCHOOL_KIND = T4.SCHOOL_KIND ");
            stb.append("     AND T6.CURRICULUM_CD = T4.CURRICULUM_CD ");
            stb.append("     AND T6.SUBCLASSCD = T4.SUBCLASSCD ");
            stb.append(" LEFT JOIN RECORD_RANK_SDIV_DAT T_RANK ON T_RANK.YEAR = T1.YEAR ");
            stb.append("     AND T_RANK.SEMESTER = '" + param._semester + "' ");
            stb.append("     AND T_RANK.TESTKINDCD || T_RANK.TESTITEMCD || T_RANK.SCORE_DIV = '" + param._testcd + "' ");
            stb.append("     AND T_RANK.CLASSCD = T4.CLASSCD ");
            stb.append("     AND T_RANK.SCHOOL_KIND = T4.SCHOOL_KIND ");
            stb.append("     AND T_RANK.CURRICULUM_CD = T4.CURRICULUM_CD ");
            stb.append("     AND T_RANK.SUBCLASSCD = T4.SUBCLASSCD ");
            stb.append("     AND T_RANK.SCHREGNO = T1.SCHREGNO ");
            stb.append(" WHERE ");
            stb.append("     T5.YEAR IS NOT NULL OR T6.YEAR IS NOT NULL ");
            stb.append(" UNION ALL ");
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.GRADE, ");
            stb.append("     T1.HR_CLASS, ");
            stb.append("     T1.HR_NAME, ");
            stb.append("     T1.HR_NAME2, ");
            stb.append("     T1.ATTENDNO, ");
            stb.append("     T1.NAME, ");
            stb.append("     RTRIM(T_RANK.SUBCLASSCD) AS SUBCLASSCD, ");
            stb.append("     CAST(NULL AS VARCHAR(1)) AS CLASSNAME, ");
            stb.append("     CAST(NULL AS VARCHAR(1)) AS SUBCLASSNAME, ");
            stb.append("     T_RANK.SCORE, ");
            stb.append("     T_RANK.GRADE_RANK ");
            stb.append(" FROM REGD T1 ");
            stb.append(" INNER JOIN RECORD_RANK_SDIV_DAT T_RANK ON T_RANK.YEAR = T1.YEAR ");
            stb.append("     AND T_RANK.SEMESTER = '" + param._semester + "' ");
            stb.append("     AND T_RANK.TESTKINDCD || T_RANK.TESTITEMCD || T_RANK.SCORE_DIV = '" + param._testcd + "' ");
            stb.append("     AND T_RANK.SUBCLASSCD = '" + _333333 + "' ");
            stb.append("     AND T_RANK.SCHREGNO = T1.SCHREGNO ");
            stb.append(" WHERE NOT EXISTS (SELECT 'X' FROM NOT_PERFECT WHERE SCHREGNO = T1.SCHREGNO AND GROUP_DIV = '3') ");
            stb.append(" UNION ALL ");
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.GRADE, ");
            stb.append("     T1.HR_CLASS, ");
            stb.append("     T1.HR_NAME, ");
            stb.append("     T1.HR_NAME2, ");
            stb.append("     T1.ATTENDNO, ");
            stb.append("     T1.NAME, ");
            stb.append("     RTRIM(T_RANK.SUBCLASSCD) AS SUBCLASSCD, ");
            stb.append("     CAST(NULL AS VARCHAR(1)) AS CLASSNAME, ");
            stb.append("     CAST(NULL AS VARCHAR(1)) AS SUBCLASSNAME, ");
            stb.append("     T_RANK.SCORE, ");
            stb.append("     T_RANK.GRADE_RANK ");
            stb.append(" FROM REGD T1 ");
            stb.append(" INNER JOIN RECORD_RANK_SDIV_DAT T_RANK ON T_RANK.YEAR = T1.YEAR ");
            stb.append("     AND T_RANK.SEMESTER = '" + param._semester + "' ");
            stb.append("     AND T_RANK.TESTKINDCD || T_RANK.TESTITEMCD || T_RANK.SCORE_DIV = '" + param._testcd + "' ");
            stb.append("     AND T_RANK.SUBCLASSCD = '" + _555555 + "' ");
            stb.append("     AND T_RANK.SCHREGNO = T1.SCHREGNO ");
            stb.append(" WHERE NOT EXISTS (SELECT 'X' FROM NOT_PERFECT WHERE SCHREGNO = T1.SCHREGNO AND GROUP_DIV = '5') ");
            stb.append(" ORDER BY ");
            stb.append("     GRADE, ");
            stb.append("     HR_CLASS, ");
            stb.append("     ATTENDNO, ");
            stb.append("     SUBCLASSCD ");
            return stb.toString();
        }
    }
    
    private static class SubclassScore {
        final String _subclasscd;
        final String _classname;
        final String _subclassname;
        final String _score;
        final String _gradeRank;
        public SubclassScore(String subclasscd, String classname, String subclassname, String score, String gradeRank) {
            _subclasscd = subclasscd;
            _classname = classname;
            _subclassname = subclassname;
            _score = score;
            _gradeRank = gradeRank;
        }
    }
    
    private static class SubclassScores {

        public static Map getScoreListMap(final DB2UDB db2, final Param param) {
            final Map scoreListMap = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = sql(param);
                //log.debug(" scores sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    getMappedList(scoreListMap, rs.getString("SUBCLASSCD")).add(rs.getObject("SCORE"));
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return scoreListMap;
        }

        public static String sql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH REGD_SHITEI AS ( ");
            stb.append(" SELECT DISTINCT ");
            stb.append("     REGD.YEAR ");
            stb.append("     , REGD.SEMESTER ");
            stb.append("     , REGD.GRADE ");
//            stb.append("     , REGD.HR_CLASS ");
//            stb.append("     , REGD.COURSECD ");
//            stb.append("     , REGD.MAJORCD ");
//            stb.append("     , REGD.COURSECODE ");
            stb.append(" FROM " + param._regdTable + " REGD ");
            stb.append(" WHERE ");
            stb.append("     REGD.YEAR = '" + param._year + "' ");
            stb.append("     AND REGD.SEMESTER = '" + param._semester + "' ");
            if ("1".equals(param._categoryIsClass)) {
                stb.append("     AND REGD.GRADE || '-' || REGD.HR_CLASS IN " + SQLUtils.whereIn(true, param._categorySelected) + " ");
            } else {
                stb.append("     AND REGD.SCHREGNO IN " + SQLUtils.whereIn(true, param._categorySelected) + " ");
            }
            stb.append(" ) ");
            stb.append(" , REGD AS ( ");
            stb.append(" SELECT DISTINCT ");
            stb.append("     REGD.SCHREGNO ");
            stb.append("     , REGD.YEAR ");
            stb.append("     , REGD.SEMESTER ");
            stb.append(" FROM " + param._regdTable + " REGD ");
            stb.append(" INNER JOIN REGD_SHITEI T1 ON T1.YEAR = REGD.YEAR ");
            stb.append("     AND T1.SEMESTER = REGD.SEMESTER ");
            stb.append("     AND T1.GRADE = REGD.GRADE ");
//            stb.append("     AND T1.COURSECD = REGD.COURSECD ");
//            stb.append("     AND T1.MAJORCD = REGD.MAJORCD ");
//            stb.append("     AND T1.COURSECODE = REGD.COURSECODE ");
            
            stb.append(" ), NOT_PERFECT AS ( ");
            stb.append(" SELECT DISTINCT I1.GROUP_DIV, REGD.SCHREGNO ");
            stb.append(" FROM REC_SUBCLASS_GROUP_DAT I1 ");
            stb.append(" INNER JOIN " + param._regdTable + " REGD ON REGD.YEAR = I1.YEAR ");
            stb.append("     AND REGD.SEMESTER = '" + param._semester + "' ");
            stb.append("     AND REGD.GRADE = I1.GRADE ");
            stb.append("     AND REGD.COURSECD = I1.COURSECD ");
            stb.append("     AND REGD.MAJORCD = I1.MAJORCD ");
            stb.append("     AND REGD.COURSECODE = I1.COURSECODE ");
            stb.append(" LEFT JOIN RECORD_SCORE_DAT I2 ON I2.YEAR = I1.YEAR ");
            stb.append("     AND I2.SEMESTER = '" + param._semester + "' ");
            stb.append("     AND I2.TESTKINDCD || I2.TESTITEMCD || I2.SCORE_DIV = '" + param._testcd + "' ");
            stb.append("     AND I2.CLASSCD = I1.CLASSCD ");
            stb.append("     AND I2.SCHOOL_KIND = I1.SCHOOL_KIND ");
            stb.append("     AND I2.CURRICULUM_CD = I1.CURRICULUM_CD ");
            stb.append("     AND I2.SUBCLASSCD = I1.SUBCLASSCD ");
            stb.append("     AND I2.SCHREGNO = REGD.SCHREGNO ");
            stb.append(" WHERE I1.YEAR = '" + param._year + "' ");
            stb.append("     AND I1.GROUP_DIV IN ('3', '5') ");
            stb.append("     AND I2.SCORE IS NULL ");

            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     T_RANK.CLASSCD || '-' || T_RANK.SCHOOL_KIND || '-' || T_RANK.CURRICULUM_CD || '-' || T_RANK.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("     T_RANK.SCORE ");
            stb.append(" FROM REGD T1 ");
            stb.append(" INNER JOIN RECORD_RANK_SDIV_DAT T_RANK ON T_RANK.YEAR = T1.YEAR ");
            stb.append("     AND T_RANK.SEMESTER = '" + param._semester + "' ");
            stb.append("     AND T_RANK.TESTKINDCD || T_RANK.TESTITEMCD || T_RANK.SCORE_DIV = '" + param._testcd + "' ");
            stb.append("     AND T_RANK.SCHREGNO = T1.SCHREGNO ");
            stb.append("     AND T_RANK.SUBCLASSCD NOT IN ('" + _333333 + "', '" + _555555 + "') ");
            stb.append("     AND T_RANK.SCORE IS NOT NULL ");
            stb.append(" UNION ALL ");
            stb.append(" SELECT ");
            stb.append("     T_RANK.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("     T_RANK.SCORE ");
            stb.append(" FROM REGD T1 ");
            stb.append(" INNER JOIN RECORD_RANK_SDIV_DAT T_RANK ON T_RANK.YEAR = T1.YEAR ");
            stb.append("     AND T_RANK.SEMESTER = '" + param._semester + "' ");
            stb.append("     AND T_RANK.TESTKINDCD || T_RANK.TESTITEMCD || T_RANK.SCORE_DIV = '" + param._testcd + "' ");
            stb.append("     AND T_RANK.SCHREGNO = T1.SCHREGNO ");
            stb.append("     AND T_RANK.SUBCLASSCD = '" + _333333 + "' ");
            stb.append("     AND T_RANK.SCORE IS NOT NULL ");
            stb.append(" WHERE NOT EXISTS (SELECT 'X' FROM NOT_PERFECT WHERE SCHREGNO = T1.SCHREGNO AND GROUP_DIV = '3') ");
            stb.append(" UNION ALL ");
            stb.append(" SELECT ");
            stb.append("     T_RANK.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("     T_RANK.SCORE ");
            stb.append(" FROM REGD T1 ");
            stb.append(" INNER JOIN RECORD_RANK_SDIV_DAT T_RANK ON T_RANK.YEAR = T1.YEAR ");
            stb.append("     AND T_RANK.SEMESTER = '" + param._semester + "' ");
            stb.append("     AND T_RANK.TESTKINDCD || T_RANK.TESTITEMCD || T_RANK.SCORE_DIV = '" + param._testcd + "' ");
            stb.append("     AND T_RANK.SCHREGNO = T1.SCHREGNO ");
            stb.append("     AND T_RANK.SUBCLASSCD = '" + _555555 + "' ");
            stb.append("     AND T_RANK.SCORE IS NOT NULL ");
            stb.append(" WHERE NOT EXISTS (SELECT 'X' FROM NOT_PERFECT WHERE SCHREGNO = T1.SCHREGNO AND GROUP_DIV = '5') ");
            stb.append(" ORDER BY ");
            stb.append("     SUBCLASSCD, ");
            stb.append("     SCORE ");
            return stb.toString();
        }
    }
    
    private static class Graph {
        
        private JFreeChart _chart;

        private synchronized File createGraph(
                final Param param,
                final List scoreList,
                final String subclasscd,
                final SubclassScore subScore,
                final String subclassname,
                final int max
        ) {
            if (null == param._subclassDistMap.get(subclasscd)) { // cache
                log.info(" create subclass score dist : " + subclasscd);
                final int barCount = 11;
                final int kizami = max / (barCount - 1);
                Distribution[] dist = new Distribution[barCount];
                for (int i = 0; i < barCount; i++) {
                    if (i == 0) {
                        dist[i] = new Distribution(i, max, max + 1); // maxのみ
                    } else {
                        dist[i] = new Distribution(i, max - i * kizami, max - (i - 1) * kizami);
                    }
                    //log.debug(" dist " + i + " = " + dist[i]._index + ", " + dist[i]._maxExclusive + ", " + dist[i]._minInclusive);
                }
                // 各々の点数に振り分ける
                for (final Iterator it = scoreList.iterator(); it.hasNext();) {
                    final Integer score = (Integer) it.next();
                    for (int i = 0; i < dist.length; i++) {
                        if (dist[i].inRange(score.intValue())) {
                            dist[i]._list.add(score);
                            break;
                        }
                    }
                }
                param._subclassDistMap.put(subclasscd, dist);
            }
            final Distribution[] dist = (Distribution[]) param._subclassDistMap.get(subclasscd);
            
            int ninzuMax = 0;
            int numberTick = 0;
            for (int i = 0; i < dist.length; i++) {
                ninzuMax = Math.max(ninzuMax, dist[i]._list.size());
            }
            if (ninzuMax > 40) {
                numberTick = 10;
                ninzuMax = ((ninzuMax / 10) + 2) * 10;
            } else {
                numberTick = 5;
                ninzuMax = ((ninzuMax / 5) + 2) * 5; 
            }
            
            int studentIndex = -1;
            if (null != subScore) {
                if  (NumberUtils.isDigits(subScore._score)) {
                    final Integer iScore = new Integer(subScore._score);
                    for (int i = 0; i < dist.length; i++) {
                        if (dist[i].inRange(iScore.intValue())) {
                            studentIndex = i;
                            break;
                        }
                    }
                }
                log.debug(" subScore = [" + subScore._subclasscd + "] : " + subScore._score + "(" + subScore._gradeRank + ", index = " + studentIndex + ") (size = " + scoreList.size() + ", avg = " + avg(scoreList) + ") ");
            }
            
            final JFreeChart chart = createChart(param, studentIndex, dist, subclasscd, subclassname, ninzuMax, numberTick);

            final File graphImageFile = graphImageFile(chart, (1680 - 356) / 2, (1820 - 1068) / 2);
            return graphImageFile;
        }
        
        private static String avg(final List scoreList) {
            if (scoreList.size() == 0) {
                return null;
            }
            double sum = 0;
            for (int i = 0; i < scoreList.size(); i++) {
                final Integer score = (Integer) scoreList.get(i);
                sum += score.intValue();
            }
            return new BigDecimal(sum).divide(new BigDecimal(scoreList.size()), 1, BigDecimal.ROUND_HALF_UP).toString();
        }

        /*
         * イメージファイルを作成し、そのファイル名を返す。
         */
        private static File graphImageFile(final JFreeChart chart, final int width, final int height) {
            final String tmpFileName = KNJServletUtils.createTmpFile(".png");
            log.debug("\ttmp file name=" + tmpFileName);

            final File outputFile = new File(tmpFileName);
            try {
                ChartUtilities.saveChartAsPNG(outputFile, chart, width, height);
            } catch (final IOException ioEx) {
                log.error("グラフイメージをファイル化できません。", ioEx);
            }

            return outputFile;
        }

        private JFreeChart createChart(final Param param, final int index, final Distribution[] dist, final String subclasscd, final String subclassname, final int ninzuMax, final int numberTick) {

            // 棒グラフのデータセットの生成
            final DefaultCategoryDataset barDataset = new DefaultCategoryDataset();
            for (int i = 0; i < dist.length; i++) {
                barDataset.addValue(new Integer(dist[i]._list.size()), "最初", new Integer(dist[i]._minInclusive));
            }

            final JFreeChart chart = createJFreeChart(param);
            chart.getTitle().setText(subclassname);
            chart.getCategoryPlot().setDataset(barDataset);

            final NumberAxis rangeAxis = (NumberAxis) chart.getCategoryPlot().getRangeAxis();
            rangeAxis.setRange(0, ninzuMax);            
            rangeAxis.setTickUnit(new NumberTickUnit(numberTick));

            final BarRenderer renderer = (BarRenderer) chart.getCategoryPlot().getRenderer();
            renderer.setItemLabelGenerator(new LabelGenerator(index));

            // ベースとなるグラフを生成
            return chart;
        }

        private JFreeChart createJFreeChart(final Param param) {
            if (null == _chart) {
                final Stroke stroke2 = new BasicStroke(2.0f);
                final Stroke stroke3 = new BasicStroke(3.0f);
                final String fontName = "Sazanami Gothic Regular";
                final Font tickLabelFont = new Font(fontName, Font.BOLD, 20);
                final Font labelFont = new Font(fontName, Font.BOLD, 20);
                _chart = ChartFactory.createBarChart(null, null, null, null, PlotOrientation.VERTICAL, false, false, false);
                _chart.setBackgroundPaint(Color.white);
                _chart.setTitle(new TextTitle("", new Font(fontName, Font.BOLD, 20)));
                _chart.setBorderVisible(true);
                _chart.setBorderStroke(stroke3);
                _chart.setAntiAlias(false);

                final CategoryPlot plot = _chart.getCategoryPlot();
                plot.setRangeGridlinesVisible(false);   // 目盛り
                plot.setRangeGridlineStroke(stroke3); // 目盛りの太さ
                plot.setRangeGridlinePaint(Color.black);
                plot.setOutlineStroke(stroke2);
                plot.setBackgroundPaint(Color.white);

                final CategoryAxis domainAxis = plot.getDomainAxis();
                domainAxis.setTickLabelsVisible(true);
                domainAxis.setTickLabelFont(tickLabelFont);
                domainAxis.setLabel("得点");
                domainAxis.setLabelFont(labelFont);
                domainAxis.setLowerMargin(0.0);
                domainAxis.setUpperMargin(0.0);
                domainAxis.setCategoryMargin(0.0);
                
                final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
                rangeAxis.setTickLabelsVisible(true);
                rangeAxis.setTickLabelFont(tickLabelFont);
                rangeAxis.setLabel("人数");
                rangeAxis.setLabelFont(labelFont);
                rangeAxis.setLabelAngle(Math.PI / 2);
                rangeAxis.setAutoRange(false);    // true だと上記の上限が無効になってしまう。
//                rangeAxis.setFixedAutoRange(10.0);

                final BarRenderer renderer = (BarRenderer) plot.getRenderer();
                renderer.setItemLabelFont(new Font(fontName, Font.PLAIN, 20));
                renderer.setItemLabelsVisible(true);
                renderer.setSeriesOutlinePaint(0, Color.black);
                renderer.setSeriesOutlineStroke(0, stroke2);
                renderer.setSeriesPaint(0, Color.white);
                renderer.setDrawBarOutline(true);
            }
            return _chart;
        }

//        private static JFreeChart createLineChart(final Double[] deviations, final int maxNinzu) {
    //
//           final String[] category = {"得点"};
//            
//            // 棒グラフのデータセットの生成
//           DefaultCategoryDataset dataSet = new DefaultCategoryDataset();
//           
//           for (int i = 0; i < category.length; i++) {
//               dataSet.addValue(0, "dummy0", category[i]); // 4学期表示のための仮のデータ
//           }
    //
//           for (int i = 0; i < category.length; i++) {
//               if (deviations[i] == null) {
//                   continue;
//               }
//               dataSet.addValue(deviations[i], "dummy1", category[i]); // 実際のデータ
//           }
    //
//            // ベースとなるグラフを生成
//            JFreeChart chart = ChartFactory.createLineChart(null, null, null, dataSet, PlotOrientation.VERTICAL, false, false, false);
//            chart.setBackgroundPaint(Color.white); // 背景を白に
//            Font textFont = new Font("ＭＳ ゴシック", Font.PLAIN, 20);
//            CategoryPlot plot = chart.getCategoryPlot();
////            plot.setRangeGridlinesVisible(false);   // 目盛り
//            plot.setRangeGridlineStroke(new BasicStroke(1.0f)); // 目盛りの太さ
//            plot.getRangeAxis().setTickLabelsVisible(true);
//            plot.setDomainGridlineStroke(new BasicStroke(2.0f));
//            plot.setDomainGridlinesVisible(true);
//            plot.getDomainAxis().setTickLabelsVisible(true);
//            plot.getDomainAxis().setTickLabelFont(textFont);
//            plot.getRangeAxis().setRange(15, 85);
//            NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
//            rangeAxis.setTickUnit(new NumberTickUnit(10)); // 目盛りを10刻み   
//            rangeAxis.setTickLabelFont(textFont);
//            plot.getRangeAxis().setAutoRange(false);    // true だと上記の上限が無効になってしまう。
////            plot.getRangeAxis().setFixedAutoRange(10.0);
//          
//            CategoryItemRenderer renderer = (CategoryItemRenderer) plot.getRenderer();
//            renderer.setSeriesStroke(1, new BasicStroke(2.5f)); // 折れ線の太さを設定
//            renderer.setSeriesPaint(1, Color.gray); // 折れ線の色を設定
//            StandardCategoryItemLabelGenerator labelGenerator = new StandardCategoryItemLabelGenerator();
//            labelGenerator.getNumberFormat().setMinimumFractionDigits(1); // 小数点以下をすくなくとも1桁表示
//            renderer.setBaseItemLabelGenerator(labelGenerator);
//            renderer.setBaseItemLabelsVisible(true); // 折れ線の上に値を表示
//            renderer.setBaseItemLabelFont(textFont);
//            return chart;
//        }

        private static class LabelGenerator extends StandardCategoryItemLabelGenerator {
            final int _index;

            public LabelGenerator(final int index) {
                _index = index;
            }

            public String generateLabel(final CategoryDataset dataset, final int series, final int category) {
                final Number count = dataset.getValue(series, category);
                return (category == _index ? "☆" : "") + String.valueOf(count);
            }
        }
    }
    
    private static class Distribution {
        final int _index;
        final int _minInclusive;
        final int _maxExclusive;
        final List _list = new ArrayList();
        public Distribution(final int index, final int min, final int max) {
            _index = index;
            _minInclusive = min;
            _maxExclusive = max;
        }
        public boolean inRange(final int score) {
            return _minInclusive <= score && score < _maxExclusive;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 68495 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _year;
        final String _semester;
        final String _categoryIsClass;
        final String _hrClass;
        final String _testcd;
        final String[] _categorySelected;
        final String _printRegd;
        
        final String _regdTable;
        final String _regdhTable;
        final String _semestername;
        final String _testitemname;
        private String _schoolName;
        
        final Map _subclassDistMap = new HashMap();
        
        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _categoryIsClass = request.getParameter("CATEGORY_IS_CLASS");
            _hrClass = request.getParameter("HR_CLASS");
            _testcd = request.getParameter("TESTCD");
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _semestername = getSemestername(db2, _year, _semester);
            _testitemname = getTestitemname(db2, _year, _semester, _testcd);
            _printRegd = request.getParameter("PRINT_REGD");
            boolean useRegdTable = "1".equals(request.getParameter("checkFiProperties")) && !"1".equals(request.getParameter("useFi_Hrclass"));
            if (useRegdTable) {
                _regdTable = "SCHREG_REGD_DAT";
                _regdhTable = "SCHREG_REGD_HDAT";
            } else {
                _regdTable = "SCHREG_REGD_FI_DAT";
                _regdhTable = "SCHREG_REGD_FI_HDAT";
            }
            setCertifSchoolDat(db2);
        }
        
        private String getSemestername(final DB2UDB db2, final String year, final String semester) {
            final String sql = "select"
                    + "   SEMESTERNAME "
                    + " from"
                    + "   SEMESTER_MST"
                    + " where"
                    + "   YEAR = '" + year + "'"
                    + "   and SEMESTER = '" + semester + "'"
                    + " order by SEMESTER"
                ;
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, sql));
        }
        
        
        private String getTestitemname(final DB2UDB db2, final String year, final String semester, final String testcd) {
            final String sql = "select"
                    + "   TESTITEMNAME "
                    + " from"
                    + "   TESTITEM_MST_COUNTFLG_NEW_SDIV "
                    + " where"
                    + "   YEAR = '" + year + "'"
                    + "   and SEMESTER = '" + semester + "'"
                    + "   and TESTKINDCD || TESTITEMCD || SCORE_DIV = '" + testcd + "'"
                    + " order by SEMESTER"
                ;
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, sql));
        }
        
        private void setCertifSchoolDat(final DB2UDB db2) {
            _schoolName = StringUtils.defaultString(KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT SCHOOL_NAME FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _year + "' AND CERTIF_KINDCD = '110' ")));
        }
    }
}


//eof