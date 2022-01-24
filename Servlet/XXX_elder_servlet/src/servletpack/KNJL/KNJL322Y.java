/*
 * $Id: efc2061ed98f2d53f7a7bd26a64a6d6e3fe770fa $
 *
 * 作成日: 2010/11/09
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;


import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;

/**
 *
 *  学校教育システム 賢者 [入試管理]
 *
 *                  ＜ＫＮＪＬ３２２Ｙ＞  平均点一覧
 **/
public class KNJL322Y {
    
    private static final Log log = LogFactory.getLog(KNJL322Y.class);
    
    private boolean _hasData;
    
    Param _param;
    
    private Map _examCourses;
    
    private final String TEST_SUBCLASSCD_CALC = "2SCORE3";
    private final String TEST_SUBCLASSCD_TOTAL1 = "TOTAL1";
    private final String TEST_SUBCLASSCD_TOTAL2 = "TOTAL2";
    private final String TEST_SUBCLASSCD_TOTAL3 = "TOTAL3";
    private final String TEST_SUBCLASSCD_TOTAL4 = "TOTAL4";
    private final String TEST_SUBCLASSCD_NAISIN = "NAISIN";
    private final String TEST_SUBCLASSCD_ENGLISH_LISTENING = "ENGLISH_LISTENING";
    
    private final String SUBCLASS_ALL_COURSECD = "ACOURSECD";
    private final String SUBCLASS_ALL_MAJORCD = "AMAJORCD";
    private final String SUBCLASS_ALL_EXAMCOURSECD = "AEXAMCOURSECD";
    
    private final String MAJORCD001 = "001";
    private final String MAJORCD002 = "002";
    private final String IPPAN = "3";
    
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
            
            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            if (null != db2) {
                db2.commit();
                db2.close();
            }
            svf.VrQuit();
        }
    }
    
    private void loadExamCourses(final DB2UDB db2, final String testDiv0) {
        _examCourses = new TreeMap();
        
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            StringBuffer sql = new StringBuffer();
            sql.append(" SELECT  ");
            sql.append("     T1.TESTDIV, ");
            sql.append("     T1.COURSECD, ");
            sql.append("     T1.MAJORCD, ");
            sql.append("     T1.EXAMCOURSECD, ");
            sql.append("     T1.EXAMCOURSE_NAME ");
            sql.append(" FROM ");
            sql.append("    ENTEXAM_COURSE_MST T1 ");
            sql.append(" WHERE ");
            sql.append("    T1.ENTEXAMYEAR = '" + _param._entexamYear + "' ");
            sql.append("    AND T1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
            sql.append("    AND T1.TESTDIV = '" + testDiv0 + "' ");
            final String courseSql = sql.toString();
            log.debug(" course sql = " + courseSql);
            ps = db2.prepareStatement(courseSql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String testDiv = rs.getString("TESTDIV");
                final String courseCd = rs.getString("COURSECD");
                final String majorCd = rs.getString("MAJORCD");
                final String examCourseCd = rs.getString("EXAMCOURSECD");
                final String examCourseName = rs.getString("EXAMCOURSE_NAME");
                final Course course = new Course(courseCd, majorCd, examCourseCd, examCourseName);
                _examCourses.put(getCode(testDiv, courseCd, majorCd, examCourseCd), course);
            }
            if ("2".equals(_param._applicantDiv)) { // 合計平均用
                final String testDiv = testDiv0;
                final String courseCd = SUBCLASS_ALL_COURSECD;
                final String majorCd = SUBCLASS_ALL_MAJORCD;
                final String examCourseCd = SUBCLASS_ALL_EXAMCOURSECD;
                final String examCourseName = _param._subTitle;
                final Course course = new Course(courseCd, majorCd, examCourseCd, examCourseName);
                _examCourses.put(getCode(testDiv, courseCd, majorCd, examCourseCd), course);
            }
        } catch (SQLException e) {
            log.error("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }
    
    private String getCode(final String testDiv, final String courseCd, final String majorCd, final String examCourseCd) {
        return testDiv + courseCd + majorCd + examCourseCd;
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        
        if (("2".equals(_param._applicantDiv)) && "2".equals(_param._testDiv)) {
            return; // 高校入試区分推薦
        }
        
        final boolean isJunior = ("1".equals(_param._applicantDiv)); // 中学
        final boolean isHighTestGakutoku = ("2".equals(_param._applicantDiv) && "1".equals(_param._testDiv)); // 高校学特(入試区分1)
        final boolean isHighTestIppan    = ("2".equals(_param._applicantDiv) && "3".equals(_param._testDiv)); // 高校一般(入試区分3)
        
        
        final String frm;
        if (isJunior) {
            frm = "KNJL322Y_1.frm";
        } else if (isHighTestIppan) {
            frm = "KNJL322Y_3.frm";
        } else if (isHighTestGakutoku){
            frm = "KNJL322Y_2.frm";
        } else {
            return;
        }
        
        svf.VrSetForm(frm, 1);
        
        svf.VrsOut("NENDO", _param._entexamYear + "年度");
        svf.VrsOut("DATE", _param._dateString);
        svf.VrsOut("TITLE", _param._title);
        svf.VrsOut("SUBTITLE", _param._subTitle);
        
        try {
            if (isJunior) {
                printJunior(db2, svf);
            } else {
                printHigh(db2, svf);
            }
            
        } catch (Exception ex) {
            log.debug("Exception:", ex);
        }
    }
    
    private Map getJuniorSubclassMap(final String testDiv) {
        final Map svfSubclassField = new HashMap();
        if ("5".equals(testDiv)) { // 適性検査型入試
            svfSubclassField.put("3", "JAPANESE"); // 適性
            svfSubclassField.put(TEST_SUBCLASSCD_TOTAL2, "AVERAGE1"); // 平均点
        } else {
            svfSubclassField.put("1", "JAPANESE"); // 国語
            svfSubclassField.put("2", "ARITHMETIC"); // 算数
            svfSubclassField.put(TEST_SUBCLASSCD_CALC, "CALC"); // 計算
            svfSubclassField.put(TEST_SUBCLASSCD_TOTAL2, "AVERAGE1"); // 平均点
        }
        final Map map = new HashMap(svfSubclassField);
        return map;
    }

    private void printJunior(final DB2UDB db2, final Vrw32alp svf) {
        
        int t = 0;
        for (Iterator ite = _param._testDivNames.keySet().iterator();ite.hasNext();) { // 推薦入試(入試区分=1)は点数が無いため表示しない
            final String testDiv = (String) ite.next();
            final String testDivName = (String) _param._testDivNames.get(testDiv);
            loadExamCourses(db2, testDiv);
            loadAvg(db2, getAvgSql());
            loadAvg(db2, getAvgTotalSql());
            
            t++;
            if ("5".equals(testDiv)) { // 適性検査型入試
                svf.VrsOutn("JAPANESE_NAME",   t, "適性");
                svf.VrsOutn("SUBJECT_NAME",    t, "１教科");
                svf.VrsOutn("AVERAGE_NAME",    t, "平均点");
            } else {
                svf.VrsOutn("JAPANESE_NAME",   t, "国語");
                svf.VrsOutn("ARITHMETIC_NAME", t, "算数");
                svf.VrsOutn("CALC_NAME",       t, "計算");
                svf.VrsOutn("SUBJECT_NAME",    t, "２教科");
                svf.VrsOutn("AVERAGE_NAME",    t, "平均点");
            }
            final Map svfSubclassField = getJuniorSubclassMap(testDiv);
            
            for (final Iterator it = _examCourses.keySet().iterator(); it.hasNext(); ) {
                final String code = (String) it.next();
                final Course course = (Course) _examCourses.get(code);
                svf.VrsOutn("COURSE", t, testDivName);
                for (final Iterator itSub = svfSubclassField.keySet().iterator(); itSub.hasNext(); ) {
                    final String testSubclassCd = (String) itSub.next();
                    final String field = (String) svfSubclassField.get(testSubclassCd);
                    final String avg = null == course.getAvg(testSubclassCd) ? "" : course.getAvg(testSubclassCd).toString();
                    svf.VrsOutn(field, t, avg);
                }
            }
            _hasData = true;
        }
        if (_hasData) {
            svf.VrEndPage();
        }
    }
    
    private Map getHighSubclassMap(final Course course) {
        final Map svfSubclassField = new HashMap();
        svfSubclassField.put("1", "JAPANESE");
        svfSubclassField.put("2", "MATHEMATICS");
        svfSubclassField.put("3", "SOCIAL");
        svfSubclassField.put("4", "SCIENCE");
        if (SUBCLASS_ALL_EXAMCOURSECD.equals(course._examCourseCd)) {
            svfSubclassField.put(TEST_SUBCLASSCD_ENGLISH_LISTENING, "ENGLISH1");
        } else if ("2".equals(_param._keisha) && MAJORCD002.equals(course._majorCd)) {
            svfSubclassField.put("5", "ENGLISH1");
            svfSubclassField.put("6", "LISTENING");
        } else {
            svfSubclassField.put("5", "ENGLISH1");
            if (IPPAN.equals(_param._testDiv) && MAJORCD001.equals(course._majorCd) || SUBCLASS_ALL_EXAMCOURSECD.equals(course._examCourseCd)) {
            } else {
                svfSubclassField.put("6", "LISTENING");
            }
        }
        svfSubclassField.put(TEST_SUBCLASSCD_NAISIN, "CONFRPT");
        if (IPPAN.equals(_param._testDiv)) {
            if (SUBCLASS_ALL_EXAMCOURSECD.equals(course._examCourseCd)) {
                svfSubclassField.put(TEST_SUBCLASSCD_TOTAL2, "AVERAGE1");
            } else if ("2".equals(_param._keisha)) {
                svfSubclassField.put(TEST_SUBCLASSCD_TOTAL4, "AVERAGE1");
            } else {
                svfSubclassField.put(TEST_SUBCLASSCD_TOTAL2, "AVERAGE1");
            }
        } else {
            if (SUBCLASS_ALL_EXAMCOURSECD.equals(course._examCourseCd)) {
                svfSubclassField.put(TEST_SUBCLASSCD_TOTAL2, "AVERAGE1");
                svfSubclassField.put(TEST_SUBCLASSCD_TOTAL1, "AVERAGE2");
            } else if ("2".equals(_param._keisha)) {
                svfSubclassField.put(TEST_SUBCLASSCD_TOTAL4, "AVERAGE1");
                svfSubclassField.put(TEST_SUBCLASSCD_TOTAL3, "AVERAGE2");
            } else {
                svfSubclassField.put(TEST_SUBCLASSCD_TOTAL2, "AVERAGE1");
                svfSubclassField.put(TEST_SUBCLASSCD_TOTAL1, "AVERAGE2");
            }
        }
        return svfSubclassField;
    }
    
    private void printHigh(final DB2UDB db2, final Vrw32alp svf) {
        loadExamCourses(db2, _param._testDiv);
        loadAvg(db2, getAvgSql());
        loadAvg(db2, getAvgTotalSql());
        int t = 1;
        for (final Iterator it = _examCourses.keySet().iterator(); it.hasNext(); ) {
            final String code = (String) it.next();
            final Course course = (Course) _examCourses.get(code);
            final Map svfSubclassField = getHighSubclassMap(course);
            if (IPPAN.equals(_param._testDiv) && MAJORCD001.equals(course._majorCd) || SUBCLASS_ALL_EXAMCOURSECD.equals(course._examCourseCd)) {
            } else {
                svf.VrsOutn("LISTENINGNAME", t, "リスニング");
            }
            svf.VrsOutn("SUBJECTNUN", t, "合計");
            svf.VrsOutn("SUBJECTNUN2", t, "合計");
            final String keishaHaitenTitle = "2".equals(_param._keisha) ? "（傾斜配点）" : "";
            svf.VrsOutn("COURSE", t, course._examCourseName + (MAJORCD002.equals(course._majorCd) ? keishaHaitenTitle : ""));
            for (final Iterator itSub = svfSubclassField.keySet().iterator(); itSub.hasNext(); ) {
                final String testSubclassCd = (String) itSub.next();
                final String field = (String) svfSubclassField.get(testSubclassCd);
                final String avg = null == course.getAvg(testSubclassCd) ? "" : course.getAvg(testSubclassCd).toString();
                svf.VrsOutn(field, t, avg);
            }
            t += 1;
        }
        svf.VrEndPage();
        _hasData = true;
    }
    
    private void loadAvg(final DB2UDB db2, final String sql) {
        
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            
            while (rs.next()) {
                
                final String testDiv = rs.getString("TESTDIV");
                final String courseCd = rs.getString("COURSECD");
                final String majorCd = rs.getString("MAJORCD");
                final String examCourseCd = rs.getString("EXAMCOURSECD");
                
                final Course course = (Course) _examCourses.get(getCode(testDiv, courseCd, majorCd, examCourseCd));
                if (null == course) {
                    continue; // 読み込んだ指定コースのみ平均点を設定する
                }
                
                final String sum = rs.getString("SUM");
                final String count = rs.getString("COUNT");
                if (!NumberUtils.isNumber(sum) || !NumberUtils.isNumber(count)) {
                    continue;
                }
                final String testSubclassCd = rs.getString("TESTSUBCLASSCD");
                final BigDecimal avg  = new BigDecimal(sum).divide(new BigDecimal(count), 1, BigDecimal.ROUND_HALF_UP);
                course.addAvg(testSubclassCd, avg);
                log.debug(" course = " + course._examCourseName + " : testsubcl = " + testSubclassCd + " , " + sum + " / " + count + " = " + avg);
            }
            
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }
    
    private String getAvgSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH MAIN AS ( ");
        stb.append(" SELECT  ");
        stb.append("     T1.ENTEXAMYEAR, ");
        stb.append("     T1.APPLICANTDIV, ");
        stb.append("     T1.TESTDIV, ");
        stb.append("     T3.DESIREDIV, ");
        stb.append("     T4.COURSECD, ");
        stb.append("     T4.MAJORCD, ");
        stb.append("     T4.EXAMCOURSECD, ");
        stb.append("     T2.EXAM_TYPE, ");
        stb.append("     T1.EXAMNO, ");
        stb.append("     T2.RECEPTNO ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_APPLICANTDESIRE_DAT T1 ");
        stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DAT T0 ON T0.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("         AND T0.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("         AND T0.EXAMNO = T1.EXAMNO ");
        stb.append("     INNER JOIN ENTEXAM_RECEPT_DAT T2 ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("         AND T2.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("         AND T2.TESTDIV = T1.TESTDIV ");
        stb.append("         AND T2.EXAM_TYPE = '1' ");
        stb.append("         AND T2.EXAMNO = T1.EXAMNO ");
        stb.append("     LEFT JOIN ENTEXAM_WISHDIV_MST T3 ON T3.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("         AND T3.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("         AND T3.TESTDIV = T1.TESTDIV ");
        stb.append("         AND T3.DESIREDIV = T1.DESIREDIV ");
        stb.append("         AND T3.WISHNO = '1' ");
        stb.append("     LEFT JOIN ENTEXAM_COURSE_MST T4 ON T4.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("         AND T4.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("         AND T4.TESTDIV = T1.TESTDIV ");
        stb.append("         AND T4.COURSECD = T3.COURSECD ");
        stb.append("         AND T4.MAJORCD = T3.MAJORCD ");
        stb.append("         AND T4.EXAMCOURSECD = T3.EXAMCOURSECD ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT T5 ON T5.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("         AND T5.EXAMNO = T1.EXAMNO ");
        stb.append(" WHERE ");
        stb.append("     T1.ENTEXAMYEAR = '" + _param._entexamYear + "' ");
        stb.append("     AND T1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        if ("2".equals(_param._applicantDiv)) {
            if ("1".equals(_param._inout)) {
                stb.append("     AND SUBSTR(T1.EXAMNO, 1, 1) <> '6' ");
            } else if ("2".equals(_param._inout)) {
                stb.append("     AND SUBSTR(T1.EXAMNO, 1, 1) = '6' ");
            }
            if ("2".equals(_param._kikoku)) {
                stb.append("     AND VALUE(T0.INTERVIEW_ATTEND_FLG, '0')  = '1' ");
            } else {
                stb.append("     AND VALUE(T0.INTERVIEW_ATTEND_FLG, '0') != '1' ");
            }
        }
        stb.append("     AND VALUE(T2.JUDGEDIV, '') <> '4' ");
        stb.append(" ), SCORES AS ( ");
        // 科目ごと
        stb.append(" SELECT  ");
        stb.append("     T1.TESTDIV, ");
        stb.append("     T1.EXAMNO, ");
        stb.append("     T1.RECEPTNO, ");
        stb.append("     T1.DESIREDIV, ");
        stb.append("     T1.COURSECD, ");
        stb.append("     T1.MAJORCD, ");
        stb.append("     T1.EXAMCOURSECD, ");
        stb.append("     T3.TESTSUBCLASSCD, ");
        stb.append("     T3.SCORE ");
        stb.append(" FROM ");
        stb.append("     MAIN T1 ");
        stb.append("     INNER JOIN ENTEXAM_SCORE_DAT T3 ON T3.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("         AND T3.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("         AND T3.TESTDIV = T1.TESTDIV ");
        stb.append("         AND T3.EXAM_TYPE = T1.EXAM_TYPE ");
        stb.append("         AND T3.RECEPTNO = T1.RECEPTNO ");
        stb.append(" WHERE ");
        stb.append("     T3.SCORE IS NOT NULL ");
        if ("2".equals(_param._applicantDiv)) { // 英語・リスニングは別途計算する
            stb.append("     AND T3.TESTSUBCLASSCD NOT IN ('5', '6') ");
        }
        // 中学・計算
        if ("1".equals(_param._applicantDiv)) {
            stb.append(" UNION ALL ");
            stb.append(" SELECT  ");
            stb.append("     T1.TESTDIV, ");
            stb.append("     T1.EXAMNO, ");
            stb.append("     T1.RECEPTNO, ");
            stb.append("     T1.DESIREDIV, ");
            stb.append("     T1.COURSECD, ");
            stb.append("     T1.MAJORCD, ");
            stb.append("     T1.EXAMCOURSECD, ");
            stb.append("     '" + TEST_SUBCLASSCD_CALC + "' AS TESTSUBCLASSCD, ");
            stb.append("     T3.SCORE3 AS SCORE ");
            stb.append(" FROM ");
            stb.append("     MAIN T1 ");
            stb.append("     INNER JOIN ENTEXAM_SCORE_DAT T3 ON T3.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
            stb.append("         AND T3.APPLICANTDIV = T1.APPLICANTDIV ");
            stb.append("         AND T3.TESTDIV = T1.TESTDIV ");
            stb.append("         AND T3.EXAM_TYPE = T1.EXAM_TYPE ");
            stb.append("         AND T3.RECEPTNO = T1.RECEPTNO ");
            stb.append(" WHERE ");
            stb.append("     T3.TESTSUBCLASSCD = '2' ");
            stb.append("     AND T3.SCORE3 IS NOT NULL ");
        }
        // 高校・英語
        if ("2".equals(_param._applicantDiv)) {
            // 指定入試区分全体の英語 (傾斜配点が"あり"でも500点満点表示 = SCORE2を使用しない)
            stb.append(" UNION ALL ");
            stb.append(" SELECT  ");
            stb.append("     T1.TESTDIV, ");
            stb.append("     T1.EXAMNO, ");
            stb.append("     T1.RECEPTNO, ");
            stb.append("     T1.DESIREDIV, ");
            stb.append("     T1.COURSECD, ");
            stb.append("     T1.MAJORCD, ");
            stb.append("     T1.EXAMCOURSECD, ");
            stb.append("     '" + TEST_SUBCLASSCD_ENGLISH_LISTENING + "' AS TESTSUBCLASSCD, ");
            stb.append("     VALUE(T3.SCORE, 0) + VALUE(T4.SCORE, 0) AS SCORE ");
            stb.append(" FROM ");
            stb.append("     MAIN T1 ");
            stb.append("     LEFT JOIN ENTEXAM_SCORE_DAT T3 ON T3.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
            stb.append("         AND T3.APPLICANTDIV = T1.APPLICANTDIV ");
            stb.append("         AND T3.TESTDIV = T1.TESTDIV ");
            stb.append("         AND T3.EXAM_TYPE = T1.EXAM_TYPE ");
            stb.append("         AND T3.RECEPTNO = T1.RECEPTNO ");
            stb.append("         AND T3.TESTSUBCLASSCD = '5' ");
            stb.append("     LEFT JOIN ENTEXAM_SCORE_DAT T4 ON T4.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
            stb.append("         AND T4.APPLICANTDIV = T1.APPLICANTDIV ");
            stb.append("         AND T4.TESTDIV = T1.TESTDIV ");
            stb.append("         AND T4.EXAM_TYPE = T1.EXAM_TYPE ");
            stb.append("         AND T4.RECEPTNO = T1.RECEPTNO ");
            stb.append("         AND T4.TESTSUBCLASSCD = '6' ");
            stb.append(" WHERE ");
            stb.append("     VALUE(T3.SCORE, -1) <> -1 OR  VALUE(T4.SCORE, -1) <> -1 ");
            // 英語
            stb.append(" UNION ALL ");
            stb.append(" SELECT  ");
            stb.append("     T1.TESTDIV, ");
            stb.append("     T1.EXAMNO, ");
            stb.append("     T1.RECEPTNO, ");
            stb.append("     T1.DESIREDIV, ");
            stb.append("     T1.COURSECD, ");
            stb.append("     T1.MAJORCD, ");
            stb.append("     T1.EXAMCOURSECD, ");
            stb.append("     T3.TESTSUBCLASSCD, ");
            if ("2".equals(_param._keisha)) {
                stb.append("     CASE WHEN T1.MAJORCD = '002' THEN T3.SCORE2 ELSE T3.SCORE END AS SCORE ");
            } else {
                stb.append("     T3.SCORE AS SCORE ");
            }
            stb.append(" FROM ");
            stb.append("     MAIN T1 ");
            stb.append("     INNER JOIN ENTEXAM_SCORE_DAT T3 ON T3.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
            stb.append("         AND T3.APPLICANTDIV = T1.APPLICANTDIV ");
            stb.append("         AND T3.TESTDIV = T1.TESTDIV ");
            stb.append("         AND T3.EXAM_TYPE = T1.EXAM_TYPE ");
            stb.append("         AND T3.RECEPTNO = T1.RECEPTNO ");
            stb.append(" WHERE ");
            stb.append("     T3.TESTSUBCLASSCD = '5' ");
            stb.append("     AND T3.SCORE IS NOT NULL ");
            // リスニング
            stb.append(" UNION ALL ");
            stb.append(" SELECT  ");
            stb.append("     T1.TESTDIV, ");
            stb.append("     T1.EXAMNO, ");
            stb.append("     T1.RECEPTNO, ");
            stb.append("     T1.DESIREDIV, ");
            stb.append("     T1.COURSECD, ");
            stb.append("     T1.MAJORCD, ");
            stb.append("     T1.EXAMCOURSECD, ");
            stb.append("     T3.TESTSUBCLASSCD, ");
            if ("2".equals(_param._keisha)) {
                stb.append("     CASE WHEN T1.MAJORCD = '002' THEN T3.SCORE2 ELSE T3.SCORE END AS SCORE ");
            } else {
                stb.append("     T3.SCORE AS SCORE ");
            }
            stb.append(" FROM ");
            stb.append("     MAIN T1 ");
            stb.append("     INNER JOIN ENTEXAM_SCORE_DAT T3 ON T3.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
            stb.append("         AND T3.APPLICANTDIV = T1.APPLICANTDIV ");
            stb.append("         AND T3.TESTDIV = T1.TESTDIV ");
            stb.append("         AND T3.EXAM_TYPE = T1.EXAM_TYPE ");
            stb.append("         AND T3.RECEPTNO = T1.RECEPTNO ");
            stb.append(" WHERE ");
            stb.append("     T3.TESTSUBCLASSCD = '6' ");
            stb.append("     AND T3.SCORE IS NOT NULL ");
        }
        stb.append(" ) ");
        // テスト区分各コースの各科目ごとの合計
        stb.append(" SELECT ");
        stb.append("     T1.TESTDIV, ");
        stb.append("     T1.COURSECD, ");
        stb.append("     T1.MAJORCD, ");
        stb.append("     T1.EXAMCOURSECD, ");
        stb.append("     T1.TESTSUBCLASSCD, ");
        stb.append("     SUM(SCORE) AS SUM, ");
        stb.append("     COUNT(*) AS COUNT ");
        stb.append(" FROM ");
        stb.append("     SCORES T1 ");
        stb.append(" GROUP BY ");
        stb.append("     T1.TESTDIV, ");
        stb.append("     T1.COURSECD, ");
        stb.append("     T1.MAJORCD, ");
        stb.append("     T1.EXAMCOURSECD, ");
        stb.append("     T1.TESTSUBCLASSCD ");
        // テスト区分全コースの各科目ごとの合計（使用するのは高校のみ）
        stb.append(" UNION ALL ");
        stb.append(" SELECT  ");
        stb.append("     T1.TESTDIV, ");
        stb.append("     '" + SUBCLASS_ALL_COURSECD + "' AS COURSECD, ");
        stb.append("     '" + SUBCLASS_ALL_MAJORCD + "' AS MAJORCD, ");
        stb.append("     '" + SUBCLASS_ALL_EXAMCOURSECD + "' AS EXAMCOURSECD, ");
        stb.append("     T1.TESTSUBCLASSCD, ");
        stb.append("     SUM(SCORE) AS SUM, ");
        stb.append("     COUNT(*) AS COUNT ");
        stb.append(" FROM ");
        stb.append("     SCORES T1 ");
        stb.append(" GROUP BY ");
        stb.append("     T1.TESTDIV,");
        stb.append("     T1.TESTSUBCLASSCD ");
        return stb.toString();
    }
    
    private String getAvgTotalSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH MAIN AS ( ");
        stb.append(" SELECT  ");
        stb.append("     T1.ENTEXAMYEAR, ");
        stb.append("     T1.APPLICANTDIV, ");
        stb.append("     T1.TESTDIV, ");
        stb.append("     T3.DESIREDIV, ");
        stb.append("     T4.COURSECD, ");
        stb.append("     T4.MAJORCD, ");
        stb.append("     T4.EXAMCOURSECD, ");
        stb.append("     T2.EXAM_TYPE, ");
        stb.append("     T1.EXAMNO, ");
        stb.append("     T2.RECEPTNO, ");
        stb.append("     T2.ATTEND_ALL_FLG, ");
        stb.append("     T2.TOTAL1, ");
        stb.append("     T2.TOTAL2, ");
        stb.append("     T2.TOTAL3, ");
        stb.append("     T2.TOTAL4, ");
        stb.append("     T5.AVERAGE_ALL ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_APPLICANTDESIRE_DAT T1 ");
        stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DAT T0 ON T0.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("         AND T0.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("         AND T0.EXAMNO = T1.EXAMNO ");
        stb.append("     INNER JOIN ENTEXAM_RECEPT_DAT T2 ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("         AND T2.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("         AND T2.TESTDIV = T1.TESTDIV ");
        stb.append("         AND T2.EXAM_TYPE = '1' ");
        stb.append("         AND T2.EXAMNO = T1.EXAMNO ");
        stb.append("     LEFT JOIN ENTEXAM_WISHDIV_MST T3 ON T3.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("         AND T3.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("         AND T3.TESTDIV = T1.TESTDIV ");
        stb.append("         AND T3.DESIREDIV = T1.DESIREDIV ");
        stb.append("         AND T3.WISHNO = '1' ");
        stb.append("     LEFT JOIN ENTEXAM_COURSE_MST T4 ON T4.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("         AND T4.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("         AND T4.TESTDIV = T1.TESTDIV ");
        stb.append("         AND T4.COURSECD = T3.COURSECD ");
        stb.append("         AND T4.MAJORCD = T3.MAJORCD ");
        stb.append("         AND T4.EXAMCOURSECD = T3.EXAMCOURSECD ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT T5 ON T5.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("         AND T5.EXAMNO = T1.EXAMNO ");
        stb.append(" WHERE ");
        stb.append("     T1.ENTEXAMYEAR = '" + _param._entexamYear + "' ");
        stb.append("     AND T1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        if ("2".equals(_param._applicantDiv)) {
            if ("1".equals(_param._inout)) {
                stb.append("     AND SUBSTR(T1.EXAMNO, 1, 1) <> '6' ");
            } else if ("2".equals(_param._inout)) {
                stb.append("     AND SUBSTR(T1.EXAMNO, 1, 1) = '6' ");
            }
            if ("2".equals(_param._kikoku)) {
                stb.append("     AND VALUE(T0.INTERVIEW_ATTEND_FLG, '0')  = '1' ");
            } else {
                stb.append("     AND VALUE(T0.INTERVIEW_ATTEND_FLG, '0') != '1' ");
            }
        }
        stb.append("     AND VALUE(T2.JUDGEDIV, '') <> '4' ");
        stb.append(" ), SCORES AS ( ");
        // 合計点1
        stb.append(" SELECT  ");
        stb.append("     T1.TESTDIV, ");
        stb.append("     T1.EXAMNO, ");
        stb.append("     T1.RECEPTNO,     ");
        stb.append("     T1.DESIREDIV, ");
        stb.append("     T1.COURSECD, ");
        stb.append("     T1.MAJORCD, ");
        stb.append("     T1.EXAMCOURSECD, ");
        stb.append("     '" + TEST_SUBCLASSCD_TOTAL1 + "' AS TESTSUBCLASSCD, ");
        stb.append("     T1.TOTAL1 AS SCORE, ");
        stb.append("     T1.ATTEND_ALL_FLG");
        stb.append(" FROM ");
        stb.append("     MAIN T1 ");
        stb.append(" WHERE ");
        stb.append("     T1.TOTAL1 IS NOT NULL ");
        stb.append("     AND T1.ATTEND_ALL_FLG = '1' ");
        // 合計点2
        stb.append(" UNION ALL ");
        stb.append(" SELECT  ");
        stb.append("     T1.TESTDIV, ");
        stb.append("     T1.EXAMNO, ");
        stb.append("     T1.RECEPTNO,     ");
        stb.append("     T1.DESIREDIV, ");
        stb.append("     T1.COURSECD, ");
        stb.append("     T1.MAJORCD, ");
        stb.append("     T1.EXAMCOURSECD, ");
        stb.append("     '" + TEST_SUBCLASSCD_TOTAL2 + "' AS TESTSUBCLASSCD, ");
        stb.append("     T1.TOTAL2 AS SCORE, ");
        stb.append("     T1.ATTEND_ALL_FLG");
        stb.append(" FROM ");
        stb.append("     MAIN T1 ");
        stb.append(" WHERE ");
        stb.append("     T1.TOTAL2 IS NOT NULL ");
        stb.append("     AND T1.ATTEND_ALL_FLG = '1' ");
        // 合計点3
        stb.append(" UNION ALL ");
        stb.append(" SELECT  ");
        stb.append("     T1.TESTDIV, ");
        stb.append("     T1.EXAMNO, ");
        stb.append("     T1.RECEPTNO,     ");
        stb.append("     T1.DESIREDIV, ");
        stb.append("     T1.COURSECD, ");
        stb.append("     T1.MAJORCD, ");
        stb.append("     T1.EXAMCOURSECD, ");
        stb.append("     '" + TEST_SUBCLASSCD_TOTAL3 + "' AS TESTSUBCLASSCD, ");
        stb.append("     T1.TOTAL3 AS SCORE, ");
        stb.append("     T1.ATTEND_ALL_FLG");
        stb.append(" FROM ");
        stb.append("     MAIN T1 ");
        stb.append(" WHERE ");
        stb.append("     T1.TOTAL3 IS NOT NULL ");
        stb.append("     AND T1.ATTEND_ALL_FLG = '1' ");
        // 合計点4
        stb.append(" UNION ALL ");
        stb.append(" SELECT  ");
        stb.append("     T1.TESTDIV, ");
        stb.append("     T1.EXAMNO, ");
        stb.append("     T1.RECEPTNO,     ");
        stb.append("     T1.DESIREDIV, ");
        stb.append("     T1.COURSECD, ");
        stb.append("     T1.MAJORCD, ");
        stb.append("     T1.EXAMCOURSECD, ");
        stb.append("     '" + TEST_SUBCLASSCD_TOTAL4 + "' AS TESTSUBCLASSCD, ");
        stb.append("     T1.TOTAL4 AS SCORE, ");
        stb.append("     T1.ATTEND_ALL_FLG");
        stb.append(" FROM ");
        stb.append("     MAIN T1 ");
        stb.append(" WHERE ");
        stb.append("     T1.TOTAL4 IS NOT NULL ");
        stb.append("     AND T1.ATTEND_ALL_FLG = '1' ");
        // 内申点
        stb.append(" UNION ALL ");
        stb.append(" SELECT  ");
        stb.append("     T1.TESTDIV, ");
        stb.append("     T1.EXAMNO, ");
        stb.append("     T1.RECEPTNO,     ");
        stb.append("     T1.DESIREDIV, ");
        stb.append("     T1.COURSECD, ");
        stb.append("     T1.MAJORCD, ");
        stb.append("     T1.EXAMCOURSECD, ");
        stb.append("     '" + TEST_SUBCLASSCD_NAISIN + "' AS TESTSUBCLASSCD, ");
        stb.append("     T1.AVERAGE_ALL AS SCORE, ");
        stb.append("     T1.ATTEND_ALL_FLG");
        stb.append(" FROM ");
        stb.append("     MAIN T1 ");
        stb.append(" WHERE ");
        stb.append("     T1.AVERAGE_ALL IS NOT NULL ");
        stb.append(" ) ");
        
        stb.append(" SELECT ");
        stb.append("     T1.TESTDIV, ");
        stb.append("     T1.COURSECD, ");
        stb.append("     T1.MAJORCD, ");
        stb.append("     T1.EXAMCOURSECD, ");
        stb.append("     T1.TESTSUBCLASSCD, ");
        stb.append("     SUM(SCORE) AS SUM, ");
        stb.append("     COUNT(*) AS COUNT ");
        stb.append(" FROM ");
        stb.append("     SCORES T1 ");
        stb.append(" GROUP BY ");
        stb.append("     T1.TESTDIV, ");
        stb.append("     T1.COURSECD, ");
        stb.append("     T1.MAJORCD, ");
        stb.append("     T1.EXAMCOURSECD, ");
        stb.append("     T1.TESTSUBCLASSCD ");
        if ("2".equals(_param._applicantDiv)) {
            // 合計点1
            stb.append(" UNION ALL ");
            stb.append(" SELECT  ");
            stb.append("     T1.TESTDIV, ");
            stb.append("     '" + SUBCLASS_ALL_COURSECD + "' AS COURSECD, ");
            stb.append("     '" + SUBCLASS_ALL_MAJORCD + "' AS MAJORCD, ");
            stb.append("     '" + SUBCLASS_ALL_EXAMCOURSECD + "' AS EXAMCOURSECD, ");
            stb.append("     T1.TESTSUBCLASSCD, ");
            stb.append("     SUM(SCORE) AS SUM, ");
            stb.append("     COUNT(*) AS COUNT ");
            stb.append(" FROM ");
            stb.append("     SCORES T1 ");
            stb.append(" GROUP BY ");
            stb.append("     T1.TESTDIV,");
            stb.append("     T1.TESTSUBCLASSCD ");
        }
        return stb.toString();
    }
    
    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 70683 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }
    
    private class Course {
        final String _courseCd;
        final String _majorCd;
        final String _examCourseCd;
        final String _examCourseName;
        final Map _avgs = new HashMap();
        public Course(final String courseCd, final String majorCd, final String examCourseCd, final String examCourseName) {
            _courseCd = courseCd;
            _majorCd = majorCd;
            _examCourseCd = examCourseCd;
            _examCourseName = examCourseName;
        }
        public void addAvg(final String testSubclassCd, final BigDecimal avg) {
            _avgs.put(testSubclassCd, avg);
        }
        public BigDecimal getAvg(final String testSubclassCd) {
            return (BigDecimal) _avgs.get(testSubclassCd);
        }
        public String toString() {
            return "[" + _courseCd + ":"+  _majorCd + ":" + _examCourseCd + ":" + _examCourseName + "]";
        }
    }
    
    /** パラメータクラス */
    private class Param {
        final String _entexamYear;
        final String _applicantDiv;
        final String _testDiv;
        final String _title;
        final String _subTitle;
        final String _dateString;
        final Map _testDivNames;
        final String _keisha;
        final String _inout; // 1:外部生のみ、2:内部生のみ、3:全て
        final String _kikoku; //対象者(帰国生)(高校のみ) 1:帰国生除く 2:帰国生のみ

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamYear = request.getParameter("YEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _title = getSchoolName(db2);
            _testDivNames = getTestDivNames(db2);
            _subTitle = (String) _testDivNames.get(_testDiv);
            final SimpleDateFormat sdf = new SimpleDateFormat("yyyy年M月d日");
            final Calendar cal = Calendar.getInstance();
            final String loginDate = request.getParameter("LOGIN_DATE");
            _dateString = sdf.format(Date.valueOf(loginDate)) + cal.get(Calendar.HOUR_OF_DAY) + "時" + cal.get(Calendar.MINUTE) + "分";
            _keisha = request.getParameter("KEISHA");
            _inout = request.getParameter("INOUT");
            _kikoku = request.getParameter("KIKOKU");
        }
        
        private String getSchoolName(DB2UDB db2) {
            String schoolName = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'L003' AND NAMECD2 = '" + _applicantDiv + "'");
                rs = ps.executeQuery();
                if (rs.next() && null != rs.getString("NAME1")) {
                  schoolName = rs.getString("NAME1"); 
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return schoolName;
        }
        
        private Map getTestDivNames(DB2UDB db2) {
            Map map = new LinkedHashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            final String namecd1 = "1".equals(_applicantDiv) ? "L024" : "L004";
            StringBuffer stb = new StringBuffer();
            if ("1".equals(_applicantDiv)) {
                stb.append(" SELECT ");
                stb.append("     NAMECD2, ");
                stb.append("     NAME1 ");
                stb.append(" FROM ");
                stb.append("     NAME_MST ");
                stb.append(" WHERE ");
                stb.append("     NAMECD1 = '" + namecd1 + "' AND ");
                stb.append("     NAMECD2 != '1' AND "); // 推薦入試(入試区分=1)は点数が無いため表示しない
                stb.append("     NAMESPARE1 || '-' || NAMECD2 <= (SELECT ");
                stb.append("                                         NAMESPARE1 || '-' || NAMECD2 ");
                stb.append("                                     FROM ");
                stb.append("                                         NAME_MST ");
                stb.append("                                     WHERE ");
                stb.append("                                         NAMECD1 = '" + namecd1 + "' AND ");
                stb.append("                                         NAMECD2 = '" + _testDiv + "' ");
                stb.append("                                     ) ");
                stb.append(" ORDER BY ");
                stb.append("     NAMESPARE1, ");
                stb.append("     NAMECD2 ");
            } else {
                stb.append(" SELECT ");
                stb.append("     NAMECD2, ");
                stb.append("     NAME1 ");
                stb.append(" FROM ");
                stb.append("     NAME_MST ");
                stb.append(" WHERE ");
                stb.append("     NAMECD1 = '" + namecd1 + "' ");
                stb.append(" ORDER BY ");
                stb.append("     NAMECD2 ");
            }
            try {
                final String sql = stb.toString();
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next() && null != rs.getString("NAME1")) {
                    final String testDiv = rs.getString("NAMECD2");
                    final String testDivName = rs.getString("NAME1");
                    map.put(testDiv, testDivName);
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return map;
        }
    }
}

// eof
