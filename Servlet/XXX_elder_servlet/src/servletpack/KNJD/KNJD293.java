/*
 * $Id: ae5d2262de3a1be6ad436af059ea2cf1bdc91f54 $
 *
 * 作成日: 2011/03/29
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 * 学校教育システム 賢者 [成績管理] 生徒別履修・修得簿
 */
public class KNJD293 {

    private static final Log log = LogFactory.getLog(KNJD293.class);

    private boolean _hasData;
    private static final int YEAR_COUNT = 6;
    private static final String SCHOOLCD_HONKOU = "0";
    private static final String SCHOOLCD_ZENSEKI = "1";
    private static final String SCHOOLCD_DAIKEN_KONIN = "2";
    private static final String KEY_ZENSEKI = "ZENSEKI";
    private static final String KEY_DAIKEN_KOUNIN = "DAIKEN_KOUNIN";

    Param _param;

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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        
        final Map standardCreditMap = getCreditMap(db2, null, true);
        final List studentList = getStudentList(db2);
        
        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            
            student.load(db2, _param, standardCreditMap);
            
            log.debug(" student = " + student._schregno);
            
            svf.VrSetForm("KNJD293.frm", 4);
            
            printSvfHeader(svf, student);
            
            printSvf2(svf, student, standardCreditMap);
            
            printSvf3(db2, svf, student);
            
            printSvf4(svf, student);
            
            printSvf1(db2, svf, student, standardCreditMap);
            _hasData = true;
        }
    }

    private void printSvfHeader(final Vrw32alp svf, final Student student) {
//        svf.VrsOut("HR_NAME", student._hrname);
        svf.VrsOut("ATTENDNO", student._attendno);
        svf.VrsOut("NAME", student._name);
        svf.VrsOut("DATE", _param._dateString);
        svf.VrAttribute("SAMPLE", "Paint=(0,70,2)");
        svf.VrsOut("GRD_CHECKDATE", KenjaProperties.gengou(Integer.parseInt(_param._year)) + "年   月   日");
    }
    
    private final String getGengoNendo(final int year) {
        return KenjaProperties.gengou(year).substring(2);
    }
    
    /**
     * 「１　教科・科目別修得単位数（学校外の学修・大検又は高認含む）」出力
     * @param svf
     * @param student
     */
    private void printSvf1(final DB2UDB db2, final Vrw32alp svf, final Student student, final Map standardCreditMap) {
        
        final Map creditMap = getCreditMap(db2, student, false);
        
        final int MAX_LINE = 90;
        
        for (int i = 0; i < YEAR_COUNT; i++) {
            final int iyear = student._startYear + i;
            if (student._schregYearSet.contains(String.valueOf(iyear))) {
                svf.VrsOut("HEADER_YEAR" + String.valueOf(i + 1), getGengoNendo(iyear));
            }
        }
        
        int totalline = 0;
        
        for (final Iterator itc = student._studyrecClazzList.iterator(); itc.hasNext();) {
            final Clazz clazz = (Clazz) itc.next();
            final List printClassNameList = clazz.getPrintClassNameList();
            
            int classline = 0;
            
            for (final Iterator its = clazz._subclassList.iterator(); its.hasNext();) {
                final Subclass subclass = (Subclass) its.next();
                if (classline < printClassNameList.size()) {
                    // 教科名表示
                    svf.VrsOut("CLASS1", (String) printClassNameList.get(classline));
                }
                svf.VrsOut("CLASSCD", clazz._classcd); // 教科名
                svf.VrsOut("SELECTCLASS", (String) _param._selectKindMap.get(subclass._selectKind)); // 科目種別名
                svf.VrsOut("SUBCLASS", subclass._subclassname);
                if (creditMap.containsKey(clazz._classcd + subclass._subclasscd)) {
                    svf.VrsOut("SET_CREDIT", creditMap.get(clazz._classcd + subclass._subclasscd).toString()); // 設置単位数（単位マスタの単位）
                }
                
                printSvfStudyrec(svf, student, subclass);

                // 修得の完了
                if (subclass._isSyutokuKanryo) {
                    svf.VrsOut("COMP_CREDIT", "○");
                }
                
                svf.VrEndRecord();
                totalline += 1;
                classline += 1;
            }
            
            for (; classline < printClassNameList.size(); classline++) {
                svf.VrsOut("CLASSCD", clazz._classcd); // 教科名
                svf.VrsOut("CLASS1", (String) printClassNameList.get(classline));
                svf.VrEndRecord();
                totalline += 1;
            }
        }
        
        for (int i = 0; i < MAX_LINE - totalline; i++) {
            svf.VrsOut("CLASSCD", String.valueOf(i));
            svf.VrEndRecord();
        }
        // 年度ごとの単位
        printSvfTotalCredit(svf, "修得単位数合計", student, false);
        svf.VrEndRecord();
        
        // 年度ごとの単位累計
        printSvfTotalCredit(svf, "修得単位数累計", student, true);
        svf.VrEndRecord();
    }

    private void printSvfStudyrec(final Vrw32alp svf, final Student student, final Subclass subclass) {
        
        for (final Iterator it = subclass._yearStudyMap.keySet().iterator(); it.hasNext();) {
            final String year = (String) it.next();
            final Integer idx = (Integer) student._yearColumn.get(year);
            if ("1".equals(subclass._yearStudyMap.get(year))) { // 網掛け部分は登録不可
                svf.VrAttribute("COMP_YEAR" + String.valueOf(idx), "Paint=(0,70,2)");
            }
        }
        for (final Iterator itsr = subclass._yearCreditAccumulateMap.keySet().iterator(); itsr.hasNext();) {
            final String key = (String) itsr.next();
            final Accumulate creditAccumulate = (Accumulate) subclass._yearCreditAccumulateMap.get(key);
            if (null != creditAccumulate) {
                final String field;
                if (KEY_ZENSEKI.equals(key)) { // 前籍校
                    field = "BEFORE_CREDIT";
                } else if (KEY_DAIKEN_KOUNIN.equals(key)) { // 大検・高認
                    field = "ADD_CREDIT";
                } else if (!"1".equals(subclass._yearStudyMap.get(key))){
                    final Integer idx = (Integer) student._yearColumn.get(key);
                    field = "COMP_YEAR" + String.valueOf(idx);
                } else {
                    field = null;
                }
                if (null != field) {
                    if (0 != creditAccumulate._accGetCredits) {
                        svf.VrsOut(field, String.valueOf(creditAccumulate._accGetCredits));
                    } else if (0 != creditAccumulate._accCompCredits) {
                        svf.VrsOut(field, "(" + String.valueOf(creditAccumulate._accCompCredits) + ")");
                    }
                }
            }
        }
    }

    private void printSvfTotalCredit(final Vrw32alp svf, final String title, final Student student, final boolean isRuiseki) {
        svf.VrsOut("GET_CREDIT_TITLE", title);
        final Map yearColumn = student._yearColumn;
        final Map yearAccumulate = student._yearCreditAccumulateMap;
        final List yearList = student._yearList;
        final Accumulate ruisekiAcc = new Accumulate();
        for (final Iterator it = yearList.iterator(); it.hasNext();) {
            final String key = (String) it.next();
            final String field;
            if (KEY_ZENSEKI.equals(key)) {
                field = "TOTAL_BEFORE_CREDIT";
            } else if (KEY_DAIKEN_KOUNIN.equals(key)) {
                field = "TOTAL_ADD_CREDIT";
            } else {
                if (!student._annualYearMap.values().contains(key)) {
                    field = null;
                } else {
                    field = "TOTAL_COMP_YEAR" + (Integer) yearColumn.get(key);
                }
            }
            if (null == field) {
                continue;
            }
            final Accumulate acc = getCreditAccumulate(yearAccumulate, key);
            ruisekiAcc.add(acc);
            final Accumulate a = isRuiseki ? ruisekiAcc : acc;
            if (a._accGetCredits != 0) {
                svf.VrsOut(field, String.valueOf(a._accGetCredits));
            }
        }
    }
    
    private int getMS932ByteSize(final String s) {
        int ret = 0;
        if (null != s) {
            try {
                ret = s.getBytes("MS932").length;
            } catch (Exception e) {
            }
        }
        return ret;
    }

    /**
     * 「２　必履修科目の点検」出力
     * @param svf
     * @param student
     */
    private void printSvf2(final Vrw32alp svf, final Student student, final Map standardCreditMap) {
        
        final int MAX_LINE = 12;
        
        for (int i = 0; i < _param._checkClazzList.size(); i++) {
            final Clazz clazz = (Clazz) _param._checkClazzList.get(i);
            final List subclassnameList = (List) student._classRequireDatMap.get(clazz._classcd);
            
            if (i == MAX_LINE - 1) {
                svf.VrsOut("SELECT_NO_2", String.valueOf(i + 1));
                svf.VrsOut("SELECT_CLASSNAME_1_2", clazz._classname);
                if (null != subclassnameList && subclassnameList.size() != 0) {
                    svf.VrsOut("SELECT_OOMP_2", "○");
                }
            } else {
                svf.VrsOutn("SELECT_NO_1", i + 1, String.valueOf(i + 1));
                svf.VrsOutn(getMS932ByteSize(clazz._classname) > 10 ? "SELECT_CLASSNAME_2" : "SELECT_CLASSNAME_1", i + 1, clazz._classname);
                if (null != subclassnameList && subclassnameList.size() != 0) {
                    svf.VrsOutn("SELECT_OOMP_1", i + 1, "○");
                    int k = 1;
                    for (final Iterator itsub = subclassnameList.iterator(); itsub.hasNext(); k += 1) {
                        final String subclassname = (String) itsub.next();
                        svf.VrsOutn("SELECT_SUBCLASSNAME" + String.valueOf(k) + "_" + (getMS932ByteSize(clazz._classname) > 10 ?  "2" : "1"), i + 1, subclassname);
                    }
                }
            }
        }
    }
    
    /**
     * 「３　学校外の学修・大検又は高認に関する記録」出力
     * @param db2
     * @param svf
     * @param student
     */
    private void printSvf3(final DB2UDB db2, final Vrw32alp svf, final Student student) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getQualifiedDatSql(student._schregno);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            int i = 1;
            while (rs.next()) {
                
                if (NumberUtils.isDigits(rs.getString("YEAR"))) {
                    svf.VrsOutn("OUT_YEAR", i, getGengoNendo(Integer.parseInt(rs.getString("YEAR"))));   // 年度
                }
                svf.VrsOutn("OUT_SUBJECT", i, rs.getString("NAME1"));  // 内容・資格
                svf.VrsOutn("OUT_CONV_SUBJECT", i, rs.getString("SUBCLASSNAME"));  // 科目名
                svf.VrsOutn("OUT_CREDIT", i, rs.getString("CREDITS"));  // 互換単位数
                i++;
            }
            
        } catch (SQLException e) {
            log.error("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }

    /**
     * 「４　修得単位数の合計」出力
     * @param svf
     * @param student
     * @param yearList
     * @param yearAccumulateMap
     */
    private void printSvf4(final Vrw32alp svf, final Student student) {
        
        final int MAX_LINE = 8;
        final Accumulate total = new Accumulate();
        final Accumulate accZenseki = getCreditAccumulate(student._yearCreditAccumulateMap, KEY_ZENSEKI);
        final Accumulate accDaikenkonin = getCreditAccumulate(student._yearCreditAccumulateMap, KEY_DAIKEN_KOUNIN);
        int j;
        j = 1;
        svf.VrsOutn("GRADE_YEAR", j, "入学前");
        final int nyugakuMaeCredit = accZenseki._accGetCredits + accDaikenkonin._accGetCredits;
        final int compCredits = accZenseki._accCompCredits + accDaikenkonin._accCompCredits;
        if (0 != nyugakuMaeCredit) {
            svf.VrsOutn("GRADE_CREDIT", j, String.valueOf(nyugakuMaeCredit));
//        } else if (0 != compCredits) {
//            svf.VrsOutn("GRADE_CREDIT", j, "(" + String.valueOf(compCredits) + ")");
        }
        total.add(nyugakuMaeCredit, compCredits);
        j++;

        for (int i = 0; i < YEAR_COUNT; i++) {
            final Integer annual = new Integer(i + 1);
            if (annual.intValue() > 4 && !student._annualYearMap.containsKey(annual)) {
                continue;
            }
            final String year = (String) student._annualYearMap.get(annual);
            svf.VrsOutn("GRADE_YEAR", j, annual + "年次");
            final Accumulate acc = getCreditAccumulate(student._yearCreditAccumulateMap, year);
            if (0 != acc._accGetCredits) {
                svf.VrsOutn("GRADE_CREDIT", j, String.valueOf(acc._accGetCredits));
//            } else if (0 != acc._accCompCredits) {
//                svf.VrsOutn("GRADE_CREDIT", j, "(" + String.valueOf(acc._accCompCredits) + ")");
            }
            total.add(acc);
            j++;
        }

        j = MAX_LINE;
        svf.VrsOutn("GRADE_YEAR", j, "合計");
        if (0 != total._accGetCredits) {
            svf.VrsOutn("GRADE_CREDIT", j, String.valueOf(total._accGetCredits));
//        } else if (0 != total._accCompCredits) {
//            svf.VrsOutn("GRADE_CREDIT", j, "(" + String.valueOf(total._accCompCredits) + ")");
        }
    }
    
    private static Accumulate getCreditAccumulate(final Map accumulateMap, final String key) {
        if (null == accumulateMap.get(key)) {
            accumulateMap.put(key, new Accumulate());
        }
        return (Accumulate) accumulateMap.get(key);
    }
    
    /**
     * 学校外における学修の単位数を取得するSQL
     */
    private String getQualifiedDatSql(final String schregno) {
        
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("   W1.CONDITION_DIV ");
        stb.append("  ,W1.YEAR ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("  ,W1.CLASSCD || W1.SCHOOL_KIND || W1.CURRICULUM_CD || W1.SUBCLASSCD AS SUBCLASSCD ");
        } else {
            stb.append("  ,W1.SUBCLASSCD ");
        }
        stb.append("  ,W1.CONTENTS ");
        stb.append("  ,W1.CREDITS ");
        stb.append("  ,W2.SUBCLASSNAME ");
        stb.append("  ,CASE W1.CONDITION_DIV WHEN '1' then W4.NAME1 ELSE W5.NAME1 END AS NAME1 ");
        stb.append(" FROM SCHREG_QUALIFIED_DAT W1 ");
        stb.append("  LEFT JOIN SUBCLASS_MST W2 ON W2.SUBCLASSCD = W1.SUBCLASSCD ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("  AND W2.CLASSCD = W1.CLASSCD ");
            stb.append("  AND W2.SCHOOL_KIND = W1.SCHOOL_KIND ");
            stb.append("  AND W2.CURRICULUM_CD = W1.CURRICULUM_CD ");
        }
        stb.append("  LEFT JOIN NAME_MST W4 ON W4.NAMECD1 = 'H305' AND W4.NAMECD2 = W1.CONTENTS AND W1.CONDITION_DIV = '1'");
        stb.append("  LEFT JOIN NAME_MST W5 ON W5.NAMECD1 = 'H306' AND W5.NAMECD2 = W1.CONTENTS AND W1.CONDITION_DIV = '2'");
        stb.append(" WHERE ");
        stb.append("   W1.YEAR <='" + _param._year + "' AND W1.SCHREGNO = '" + schregno + "' ");
        stb.append(" ORDER BY ");
        stb.append("   W1.YEAR ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("  ,W1.CLASSCD || W1.SCHOOL_KIND || W1.CURRICULUM_CD || W1.SUBCLASSCD ");
        } else {
            stb.append("  ,W1.SUBCLASSCD ");
        }
        return stb.toString();
    }
    
    private List getStudentList(final DB2UDB db2) {
        final List studentList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getStudentSql();
             log.debug(" student sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final Student student = new Student(rs.getString("SCHREGNO"), rs.getString("NAME"),
                        rs.getString("GRADE"), rs.getString("HR_NAME"), rs.getString("ATTENDNO"),
                        rs.getString("COURSECD"), rs.getString("MAJORCD"), rs.getString("COURSECODE"),
                        rs.getInt("START_YEAR"), rs.getString("ENTCOURSECD"), rs.getString("ENTMAJORCD"));
                studentList.add(student);
            }
            
        } catch (SQLException e) {
            log.error("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return studentList;
    }
    
    /**
     * 単位数(標準単位数 or 設置単位数)のマップを得る
     * @param db2
     * @param student
     * @param isStandard 標準単位数か
     * @return 標準単位数指定なら標準単位数、そうでなければ設置単位数（生徒が入学した年度・課程学科コースの単位マスタ）のマップを得る
     */
    private Map getCreditMap(final DB2UDB db2, final Student student, final boolean isStandard) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        final Map creditMap = new HashMap();
        try {
            final String sql = isStandard ? getStandardSubclassCreditSql() : getSubclassCreditSql(student);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            
            while (rs.next()) {
                creditMap.put(rs.getString("CLASSCD") + rs.getString("SUBCLASSCD"), Integer.valueOf(rs.getString("CREDITS")));
            }
            
        } catch (SQLException ex) {
            log.error("exception!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return creditMap;
    }
    
    private String getStandardSubclassCreditSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     T1.CLASSCD || T1.SCHOOL_KIND AS CLASSCD ");
            stb.append("     ,T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD AS SUBCLASSCD ");
        } else {
            stb.append("     T1.CLASSCD ");
            stb.append("     ,T1.SUBCLASSCD ");
        }
        stb.append("     ,T1.CREDITS ");
        stb.append(" FROM ");
        stb.append("     STANDARD_CREDIT_MST T1 ");
        stb.append(" WHERE ");
        stb.append("     T1.CURRICULUM_CD = '2' ");
        return stb.toString();
    }
    
    private String getSubclassCreditSql(final Student student) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     T1.CLASSCD || T1.SCHOOL_KIND AS CLASSCD ");
            stb.append("     ,T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD AS SUBCLASSCD ");
        } else {
            stb.append("     T1.CLASSCD ");
            stb.append("     ,T1.SUBCLASSCD ");
        }
        stb.append("     ,T1.CREDITS ");
        stb.append(" FROM ");
        stb.append("     CREDIT_MST T1 ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.GRADE = '" + student._grade + "' ");
        stb.append("     AND T1.COURSECD = '" + student._coursecd + "' ");
        stb.append("     AND T1.MAJORCD = '" + student._majorcd + "' ");
        stb.append("     AND T1.COURSECODE = '" + student._coursecode + "' ");
        stb.append("     AND T1.CREDITS IS NOT NULL ");
        return stb.toString();
    }

    private String getStudentSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH TMP AS ( ");
        stb.append(" SELECT ");
        stb.append("    T1.SCHREGNO, T4.NAME, T1.GRADE, T1.HR_CLASS, T2.HR_NAME, T1.ATTENDNO, T1.COURSECD, T1.MAJORCD, T1.COURSECODE, ");
        stb.append("    CASE WHEN VALUE(ENT_DIV, '') IN ('04', '05') ");
        stb.append("       THEN INT(T1.YEAR) - INT(T1.GRADE) + 1 ");
        stb.append("       ELSE INT(FISCALYEAR(VALUE(T4.ENT_DATE, '" + _param._year + "-04-01'))) END AS START_YEAR ");
        stb.append(" FROM SCHREG_REGD_DAT T1 ");
        stb.append(" LEFT JOIN SCHREG_REGD_HDAT T2 ON T2.YEAR = T1.YEAR ");
        stb.append("    AND T2.SEMESTER = T1.SEMESTER ");
        stb.append("    AND T2.GRADE = T1.GRADE ");
        stb.append("    AND T2.HR_CLASS = T1.HR_CLASS ");
        stb.append(" INNER JOIN SCHREG_BASE_MST T4 ON T4.SCHREGNO = T1.SCHREGNO ");
        stb.append(" WHERE ");
        stb.append("    T1.YEAR = '" + _param._year + "' ");
        stb.append("    AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append("    AND T1.GRADE || T1.HR_CLASS = '" + _param._gradeHrclass + "' ");
        stb.append("    AND T1.SCHREGNO IN " + SQLUtils.whereIn(true, _param._categorySelected));
        stb.append(" ), SCHREG_ENT_YEAR AS ( ");
        stb.append("    SELECT T1.SCHREGNO, MIN(T1.YEAR) AS YEAR ");
        stb.append("    FROM SCHREG_REGD_DAT T1 INNER JOIN TMP T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("    GROUP BY T1.SCHREGNO ");
        stb.append(" ), SCHREG_ENT_YEAR_SEMESTER AS ( ");
        stb.append("    SELECT T1.SCHREGNO,  T1.YEAR, MIN(T2.SEMESTER) AS SEMESTER ");
        stb.append("    FROM SCHREG_ENT_YEAR T1 ");
        stb.append("    INNER JOIN SCHREG_REGD_DAT T2 ON T2.SCHREGNO = T1.SCHREGNO AND T2.YEAR = T1.YEAR ");
        stb.append("    GROUP BY T1.SCHREGNO, T1.YEAR  ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("    T1.SCHREGNO, T1.NAME, T1.GRADE, T1.HR_CLASS, T1.HR_NAME, T1.ATTENDNO, T1.COURSECD, T1.MAJORCD, T1.COURSECODE, ");
        stb.append("    T1.START_YEAR, T3.COURSECD AS ENTCOURSECD, T3.MAJORCD AS ENTMAJORCD ");
        stb.append(" FROM TMP T1 ");
        stb.append(" LEFT JOIN SCHREG_ENT_YEAR_SEMESTER T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append(" LEFT JOIN SCHREG_REGD_DAT T3 ON T3.SCHREGNO = T2.SCHREGNO AND T3.YEAR = T2.YEAR AND T3.SEMESTER = T2.SEMESTER ");
        stb.append(" ORDER BY ");
        stb.append("    T1.ATTENDNO ");
        return stb.toString();
    }
    
    /**
     * 生徒
     */
    private static class Student {
        final String _schregno;
        final String _name;
        final String _grade;      // 現在の学年
        final String _hrname;      // 現在のホームルーム名
        final String _attendno;      // 現在の出席番号
        final String _coursecd;   // 課程
        final String _majorcd;    // 学科
        final String _coursecode; // コース
        final int _startYear;
        final String _entcoursecd;
        final String _entmajorcd;
        final List _studyrecClazzList;
        final Map _classRequireDatMap;
        
        final Map _yearCreditAccumulateMap;
        final Map _yearColumn;
        final List _yearList;
        Set _schregYearSet = Collections.EMPTY_SET;
        Map _annualYearMap = Collections.EMPTY_MAP;
        
        Student(
                final String schregno,
                final String name,
                final String grade,
                final String hrname,
                final String attendno,
                final String coursecd,
                final String majorcd,
                final String coursecode,
                final int startYear,
                final String entcoursecd,
                final String entmajorcd
                ) {
            _schregno = schregno;
            _name = name;
            _grade = grade;
            _hrname = hrname;
            _attendno = attendno;
            _coursecd = coursecd;
            _majorcd = majorcd;
            _coursecode = coursecode;
            _startYear = startYear;
            _entcoursecd = entcoursecd;
            _entmajorcd = entmajorcd;
            _studyrecClazzList = new ArrayList();
            _classRequireDatMap = new HashMap();
            
            _yearCreditAccumulateMap = new HashMap(); // 年度と合計単位のマップ
            _yearColumn = new HashMap(); // 年度と列のマップ

            _yearList = new ArrayList();
            _yearList.add(KEY_ZENSEKI);
            _yearList.add(KEY_DAIKEN_KOUNIN);
            for (int i = 0; i < YEAR_COUNT; i++) {
                final String year = String.valueOf(_startYear + i);
                _yearList.add(year);
                final Integer col = new Integer(i + 1);
                _yearColumn.put(year, col);
            }
        }
        
        public Clazz getClazz(final String classcd) {
            for (final Iterator it = _studyrecClazzList.iterator(); it.hasNext();) {
                final Clazz clazz = (Clazz) it.next();
                if (null != clazz._classcd && clazz._classcd.equals(classcd)) {
                    return clazz;
                }
            }
            return null;
        }
        
        public void load(final DB2UDB db2, final Param param, final Map standardCreditMap) {
            loadAnnual(db2);
            loadStudyrec(db2, param, standardCreditMap);
            setYearSet();
            loadClassRequireDat(db2, param);
        }
        
        private void loadAnnual(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            _annualYearMap = new HashMap();
            try {
                final String sql = "SELECT ANNUAL, MAX(YEAR) AS YEAR FROM SCHREG_REGD_DAT WHERE SCHREGNO = '" + _schregno + "' GROUP BY ANNUAL ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    if (org.apache.commons.lang.math.NumberUtils.isDigits(rs.getString("ANNUAL"))) {
                        _annualYearMap.put(Integer.valueOf(rs.getString("ANNUAL")), rs.getString("YEAR"));
                    }
                }
            } catch (SQLException ex) {
                log.debug("Exception:", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
        
        private void loadStudyrec(final DB2UDB db2, final Param param, final Map standardCreditMap) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getRecordSql(param);
                log.debug(" record sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                
                while (rs.next()) {
                    
                    final String classcd = rs.getString("CLASSCD");
                    final String subclasscd = rs.getString("SUBCLASSCD");
                    final String year = rs.getString("YEAR");
                    
                    if (null == getClazz(classcd)) {
                        _studyrecClazzList.add(new Clazz(classcd, rs.getString("CLASSNAME")));
                    }
                    final Clazz clazz = getClazz(classcd);
                    if (null == clazz.getSubclass(subclasscd)) {
                        clazz._subclassList.add(new Subclass(subclasscd, rs.getString("SUBCLASSNAME"), rs.getString("SELECTKIND")));
                    }
                    final Subclass subclass = clazz.getSubclass(subclasscd);
                    subclass._yearStudyMap.put(year, rs.getString("STUDY"));
                    subclass._studyrecList.add(new Studyrec(year, rs.getString("SCHOOLCD"), rs.getString("GET_CREDIT"), rs.getString("ADD_CREDIT"), rs.getString("COMP_CREDIT")));
                }
                
            } catch (SQLException ex) {
                log.debug("Exception:", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            
            for (final Iterator itc = _studyrecClazzList.iterator(); itc.hasNext();) {
                final Clazz clazz = (Clazz) itc.next();
                for (final Iterator its = clazz._subclassList.iterator(); its.hasNext();) {
                    final Subclass subclass = (Subclass) its.next();
                    
                    accumulateStydyrecCredit(subclass);
                    
                    // 修得の完了（標準単位マスタとの比較）
                    subclass.setSyutokuKanryo((Integer) standardCreditMap.get(clazz._classcd + subclass._subclasscd));
                }
            }
        }
        
        private void setYearSet() {
            
            final Set yearSet = new TreeSet();
            for (final Iterator it = _studyrecClazzList.iterator(); it.hasNext();) {
                final Clazz clazz = (Clazz) it.next();
                for (final Iterator its = clazz._subclassList.iterator(); its.hasNext();) {
                    final Subclass subclass = (Subclass) its.next();
                    for (final Iterator itsr = subclass._studyrecList.iterator(); itsr.hasNext();) {
                        final Studyrec studyrec = (Studyrec) itsr.next();
                        yearSet.add(studyrec._year);
                    }
                }
            }
            for (int i = 0; i < YEAR_COUNT / 2; i++) {
                final String year = String.valueOf(_startYear + i);
                yearSet.add(year);
            }
            _schregYearSet = yearSet;
        }
        
        private void loadClassRequireDat(final DB2UDB db2, final Param param) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getClassRequireDatSql(param);
                log.debug(" class require sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                
                while (rs.next()) {
                    
                    final String classcd = rs.getString("CLASSCD");
                    final String subclassname = rs.getString("SUBCLASSNAME");
                    
                    if (null == _classRequireDatMap.get(classcd)) {
                        _classRequireDatMap.put(classcd, new ArrayList());
                    }
                    final List subclassnameList = (List) _classRequireDatMap.get(classcd); 
                    subclassnameList.add(subclassname);
                }
                
            } catch (SQLException ex) {
                log.debug("Exception:", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
        
        private String getRecordSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH SUBCLASS_HOLD AS ( ");
            stb.append("     SELECT ");
            stb.append("         * ");
            stb.append("     FROM ");
            stb.append("         SUBCLASS_HOLD_DAT T1 ");
            stb.append("     WHERE ");
            stb.append("         T1.ENTYEAR = '" + _startYear + "' ");
            stb.append("         AND T1.COURSECD = '" + _entcoursecd + "' ");
            stb.append("         AND T1.MAJORCD = '" + _entmajorcd + "' ");
            stb.append(" ), SUBCLASS_HOLD_YEAR AS ( ");
            stb.append("     SELECT '0' AS YEAR, CLASSCD, SUBCLASSCD, SELECTKIND, '' AS STUDY ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     ,SCHOOL_KIND ");
                stb.append("     ,CURRICULUM_CD ");
            }
            stb.append("     FROM SUBCLASS_HOLD ");
            stb.append("     UNION ALL ");
            stb.append("     SELECT '" + (_startYear + 0) + "' AS YEAR, CLASSCD, SUBCLASSCD, SELECTKIND, STUDY1 AS STUDY ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     ,SCHOOL_KIND ");
                stb.append("     ,CURRICULUM_CD ");
            }
            stb.append("     FROM SUBCLASS_HOLD ");
            stb.append("     UNION ALL ");
            stb.append("     SELECT '" + (_startYear + 1) + "' AS YEAR, CLASSCD, SUBCLASSCD, SELECTKIND, STUDY2 AS STUDY ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     ,SCHOOL_KIND ");
                stb.append("     ,CURRICULUM_CD ");
            }
            stb.append("     FROM SUBCLASS_HOLD ");
            stb.append("     UNION ALL ");
            stb.append("     SELECT '" + (_startYear + 2) + "' AS YEAR, CLASSCD, SUBCLASSCD, SELECTKIND, STUDY3 AS STUDY ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     ,SCHOOL_KIND ");
                stb.append("     ,CURRICULUM_CD ");
            }
            stb.append("     FROM SUBCLASS_HOLD ");
            stb.append("     UNION ALL ");
            stb.append("     SELECT '" + (_startYear + 3) + "' AS YEAR, CLASSCD, SUBCLASSCD, SELECTKIND, STUDY4 AS STUDY ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     ,SCHOOL_KIND ");
                stb.append("     ,CURRICULUM_CD ");
            }
            stb.append("     FROM SUBCLASS_HOLD ");
            stb.append("     UNION ALL ");
            stb.append("     SELECT '" + (_startYear + 4) + "' AS YEAR, CLASSCD, SUBCLASSCD, SELECTKIND, STUDY5 AS STUDY ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     ,SCHOOL_KIND ");
                stb.append("     ,CURRICULUM_CD ");
            }
            stb.append("     FROM SUBCLASS_HOLD ");
            stb.append("     UNION ALL ");
            stb.append("     SELECT '" + (_startYear + 5) + "' AS YEAR, CLASSCD, SUBCLASSCD, SELECTKIND, STUDY6 AS STUDY ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     ,SCHOOL_KIND ");
                stb.append("     ,CURRICULUM_CD ");
            }
            stb.append("     FROM SUBCLASS_HOLD ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     T1.YEAR ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     ,T1.CLASSCD || T1.SCHOOL_KIND AS CLASSCD ");
            } else {
                stb.append("     ,T1.CLASSCD ");
            }
            stb.append("     ,VALUE(T4.CLASSNAME, '') AS CLASSNAME ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     ,T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD AS SUBCLASSCD ");
            } else {
                stb.append("     ,T1.SUBCLASSCD ");
            }
            stb.append("     ,VALUE(T3.SUBCLASSNAME, '') AS SUBCLASSNAME ");
            stb.append("     ,T1.SELECTKIND ");
            stb.append("     ,T1.STUDY ");
            stb.append("     ,T2.SCHOOLCD ");
            stb.append("     ,T2.ANNUAL ");
            stb.append("     ,T2.SCHREGNO ");
            stb.append("     ,CASE WHEN T5.SUBCLASSCD IS NOT NULL THEN NULL ELSE T2.GET_CREDIT END AS GET_CREDIT ");
            stb.append("     ,CASE WHEN T5.SUBCLASSCD IS NOT NULL THEN NULL ELSE T2.ADD_CREDIT END AS ADD_CREDIT ");
            stb.append("     ,T2.COMP_CREDIT ");
            stb.append(" FROM ");
            stb.append("     SUBCLASS_HOLD_YEAR T1 ");
            stb.append(" LEFT JOIN SCHREG_STUDYREC_DAT T2 ON T2.YEAR = T1.YEAR ");
            stb.append("     AND T2.CLASSCD = T1.CLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("     AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append("     AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("     AND T2.SCHREGNO = '" + _schregno + "' ");
            stb.append(" LEFT JOIN SUBCLASS_MST T3 ON T3.SUBCLASSCD = T1.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     AND T3.CLASSCD = T1.CLASSCD ");
                stb.append("     AND T3.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("     AND T3.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append(" LEFT JOIN CLASS_MST T4 ON T4.CLASSCD = T1.CLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     AND T4.SCHOOL_KIND = T1.SCHOOL_KIND ");
            }
            stb.append(" LEFT JOIN SUBCLASS_MST T5 ON T5.SUBCLASSCD = T3.SUBCLASSCD2 ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     AND T5.CLASSCD = T3.CLASSCD ");
                stb.append("     AND T5.SCHOOL_KIND = T3.SCHOOL_KIND ");
                stb.append("     AND T5.CURRICULUM_CD = T3.CURRICULUM_CD ");
            }
            stb.append(" WHERE ");
            stb.append("     T1.YEAR <= '" + param._year + "' ");
            stb.append(" ORDER BY ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     T1.CLASSCD,  ");
                stb.append("     T1.SCHOOL_KIND,  ");
                stb.append("     T1.CURRICULUM_CD,  ");
            }
            stb.append("     T1.SUBCLASSCD, T2.YEAR, T2.SCHOOLCD ");
            return stb.toString();
        }
        
        /**
         * 科目ごと、年度ごとの単位数を集計する
         * @param subclass 集計する科目
         */
        private void accumulateStydyrecCredit(final Subclass subclass) {
            
            for (final Iterator itsr = subclass._studyrecList.iterator(); itsr.hasNext();) {
                final Studyrec studyrec = (Studyrec) itsr.next();
                
                // 入学前（前籍校、大検・高認）
                if ("0".equals(studyrec._year) || null != studyrec._year && Integer.parseInt(studyrec._year) < _startYear) {
                    final String key;
                    if (SCHOOLCD_ZENSEKI.equals(studyrec._schoolCd)) { // 前籍校
                        key = KEY_ZENSEKI;
                    } else if (SCHOOLCD_DAIKEN_KONIN.equals(studyrec._schoolCd)) { // 大検・高認
                        key = KEY_DAIKEN_KOUNIN;
                    } else {
                        key = null;
                    }
                    if (null != key && null != studyrec.intGetAddCredit()) {
                        final int getAddCredits = studyrec.intGetAddCredit().intValue();
                        final int compCredits = NumberUtils.isDigits(studyrec._compCredit) ? Integer.parseInt(studyrec._compCredit) : 0;
                        final Accumulate yearAcc = getCreditAccumulate(_yearCreditAccumulateMap, key);
                        yearAcc.add(getAddCredits, compCredits);
                        final Accumulate subAcc = getCreditAccumulate(subclass._yearCreditAccumulateMap, key);
                        subAcc.add(getAddCredits, compCredits);
                    }
                } else { // 各年度
                    final Integer idx = (Integer) _yearColumn.get(studyrec._year);
                    if (null != idx) {
                        if (!"1".equals(subclass._yearStudyMap.get(studyrec._year))) { // 網掛け部分は登録不可
                            if (null != studyrec.intGetAddCredit() && (SCHOOLCD_HONKOU.equals(studyrec._schoolCd) || SCHOOLCD_DAIKEN_KONIN.equals(studyrec._schoolCd))) {
                                final int getAddCredits = studyrec.intGetAddCredit().intValue();
                                final int compCredits = NumberUtils.isDigits(studyrec._compCredit) ? Integer.parseInt(studyrec._compCredit) : 0;
                                final Accumulate yearAcc = getCreditAccumulate(_yearCreditAccumulateMap, studyrec._year);
                                yearAcc.add(getAddCredits, compCredits);
                                final Accumulate subAcc = getCreditAccumulate(subclass._yearCreditAccumulateMap, studyrec._year);
                                subAcc.add(getAddCredits, compCredits);
                            }
                        }
                    }
                }
            }
        }
        
        private String getClassRequireDatSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("   T1.CLASSCD || T1.SCHOOL_KIND AS CLASSCD, ");
                stb.append("   T2.CLASSCD || T2.SCHOOL_KIND || T2.CURRICULUM_CD || T2.SUBCLASSCD AS SUBCLASSCD, ");
            } else {
                stb.append("   T1.CLASSCD, ");
                stb.append("   T2.SUBCLASSCD, ");
            }
            stb.append("   T3.SUBCLASSNAME ");
            stb.append(" FROM ");
            stb.append("   CLASS_REQUIRED_DAT T1 ");
            stb.append("   INNER JOIN SUBCLASS_REQUIRED_STUDY_DAT T2 ON T2.CLASSCD = T1.CLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("      AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");
            }
            stb.append("      AND T2.COURSECD = T1.COURSECD ");
            stb.append("      AND T2.MAJORCD = T1.MAJORCD ");
            stb.append("      AND T2.SEQ = T1.SEQ ");
            stb.append("   INNER JOIN SUBCLASS_MST T3 ON T3.SUBCLASSCD = T2.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("      AND T3.CLASSCD = T2.CLASSCD ");
                stb.append("      AND T3.SCHOOL_KIND = T2.SCHOOL_KIND ");
                stb.append("      AND T3.CURRICULUM_CD = T2.CURRICULUM_CD ");
            }
            stb.append(" WHERE ");
            stb.append("   T1.SCHREGNO = '" + _schregno + "' ");
            stb.append("   AND VALUE(T1.ERR_FLG, '') <> '1' ");
            return stb.toString();
        }
    }
    
    /**
     * 教科
     */
    private static class Clazz {
        final String _classcd;
        final String _classname;
        final List _subclassList;
        Clazz(
                final String classcd,
                final String classname
                ) {
            _classcd = classcd;
            _classname = classname;
            _subclassList = new ArrayList();
        }
        public List getPrintClassNameList() {
            final List list = new ArrayList();
            if (null == _classname) {
                return list;
            }
            final int chars;
            if (_classname.length() >= _subclassList.size() * 2) {
                chars = 2;
            } else {
                chars = 1;
            }
            for (int i = 0; i < _classname.length() / chars + ((_classname.length() % chars == 0) ? 0 : 1); i++) {
                final int len = _classname.length() >= chars * (i + 1) ? chars : _classname.length() - chars * i;
                list.add(String.valueOf(_classname.substring(i * chars, i * chars + len)));
            }
            return list;
        }
        
        public Subclass getSubclass(final String subclasscd) {
            for (final Iterator it = _subclassList.iterator(); it.hasNext();) {
                final Subclass subclass = (Subclass) it.next();
                if (null != subclass._subclasscd && subclass._subclasscd.equals(subclasscd)) {
                    return subclass;
                }
            }
            return null;
        }
    }

    /**
     * 科目
     */
    private static class Subclass {
        final String _subclasscd;
        final String _subclassname;
        final String _selectKind;
        final Map _yearStudyMap; // 履修登録できない網掛け部分
        final List _studyrecList;
        final Map _yearCreditAccumulateMap;
        boolean _isSyutokuKanryo;
        Subclass(
                final String subclasscd,
                final String subclassname,
                final String selectKind
                ) {
            _subclasscd = subclasscd;
            _subclassname = subclassname;
            _selectKind = selectKind;
            _yearStudyMap = new HashMap();
            _studyrecList = new ArrayList();
            _yearCreditAccumulateMap = new HashMap();
            _isSyutokuKanryo = false;
        }
        public void setSyutokuKanryo(final Integer standardCredits) {
            if (null != standardCredits) {
                int totalCredit = 0;
                for (final Iterator it = _yearCreditAccumulateMap.keySet().iterator(); it.hasNext();) {
                    final String key = (String) it.next();
                    final Accumulate acc = getCreditAccumulate(_yearCreditAccumulateMap, key);
                    totalCredit += acc._accGetCredits;
                }
                _isSyutokuKanryo = standardCredits.intValue() <= totalCredit;
            }
        }
    }
    
    /**
     * 学習記録データ(単位)
     */
    private static class Studyrec {
        final String _year;
        final String _schoolCd;
        final String _getCredit;
        final String _addCredit;
        final String _compCredit;
        Studyrec(
                final String year,
                final String schoolCd,
                final String getCredit,
                final String addCredit,
                final String compCredit) {
            _year = year;
            _schoolCd = schoolCd;
            _getCredit = getCredit;
            _addCredit = addCredit;
            _compCredit = compCredit;
        }
        public Integer intGetAddCredit() {
            if (StringUtils.isBlank(_getCredit) && StringUtils.isBlank(_addCredit)) {
                return null;
            }
            return new Integer(toInt(_getCredit) + toInt(_addCredit));
        }
        public int toInt(final String str) {
            if (NumberUtils.isDigits(str)) {
                return Integer.parseInt(str);
            }
            return 0;
        }
        public String toString() {
            return "[Studyrec schoolCd = "+  _schoolCd + " : credits = " + intGetAddCredit() + "]";
        }
    }
    
    /**
     * 単位合計
     */
    private static class Accumulate {
        /**
         * 合計単位数
         */
        int _accGetCredits = 0;
        int _accCompCredits = 0;
        public void add(final int getCredits, final int compCredits) {
            _accGetCredits += getCredits;
            _accCompCredits += compCredits;
        }
        public void add(final Accumulate a) {
            if (null == a) {
                return;
            }
            add(a._accGetCredits, a._accCompCredits);
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 56595 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _semester;
        private final String _gradeHrclass;
        private final String _date;
        private final String _dateString;
        private final String[] _categorySelected;
        private final String _useCurriculumcd;
        private final String _useClassDetailDat;
        
        private final List _checkClazzList;
        private final Map _selectKindMap;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _gradeHrclass = request.getParameter("GRADE_HR_CLASS");
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _useClassDetailDat = request.getParameter("useClassDetailDat");
            
            _date = request.getParameter("CTRL_DATE");
            final Calendar cal = Calendar.getInstance();
            final NumberFormat nf = new DecimalFormat("0");
            _dateString = KNJ_EditDate.h_format_JP(_date) + nf.format(cal.get(Calendar.HOUR_OF_DAY)) + "時" + cal.get(Calendar.MINUTE) + "分";
            
            _checkClazzList = getCheckClazzList(db2);
            _selectKindMap = getSelectKindNameMap(db2);
        }
        
        private List getCheckClazzList(final DB2UDB db2) {
            final List clazzList = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                if ("1".equals(_useCurriculumcd) && "1".equals(_useClassDetailDat)) {
                    stb.append(" SELECT ");
                    stb.append("     T1.CLASSCD || T1.SCHOOL_KIND AS CLASSCD, ");
                    stb.append("     VALUE(T2.CLASSNAME, '') AS CLASSNAME ");
                    stb.append(" FROM ");
                    stb.append("     CLASS_DETAIL_DAT T1 INNER JOIN CLASS_MST T2 ON T2.CLASSCD || '-' || T2.SCHOOL_KIND = T1.CLASSCD || '-' || T1.SCHOOL_KIND ");
                    stb.append(" WHERE ");
                    stb.append("     T1.YEAR = '" + _year + "' ");
                    stb.append("     AND T1.CLASS_SEQ = '005' ");
                    stb.append(" ORDER BY ");
                    stb.append("     T1.CLASS_REMARK1, T1.CLASSCD || T1.SCHOOL_KIND ");
                } else {
                    stb.append(" SELECT ");
                    if ("1".equals(_useCurriculumcd)) {
                        stb.append("     T2.CLASSCD || T2.SCHOOL_KIND AS CLASSCD, ");
                    } else {
                        stb.append("     T2.CLASSCD , ");
                    }
                    stb.append("     VALUE(T2.CLASSNAME, '') AS CLASSNAME ");
                    stb.append(" FROM ");
                    stb.append("     NAME_MST T1 INNER JOIN CLASS_MST T2 ON T2.CLASSCD = T1.NAME1 ");
                    stb.append(" WHERE ");
                    stb.append("     T1.NAMECD1 = 'D031' ");
                    stb.append(" ORDER BY T1.NAMECD2 ");
                }
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String classcd = rs.getString("CLASSCD");
                    final String classname = rs.getString("CLASSNAME");
                    final Clazz clazz = new Clazz(classcd, classname);
                    clazzList.add(clazz);
                }
                
            } catch (SQLException ex) {
                log.error(ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return clazzList;
        }
        
        private Map getSelectKindNameMap(final DB2UDB db2) {
            final Map nameMap = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = " SELECT  T1.NAMECD2, T1.NAME1 FROM NAME_MST T1 WHERE T1.NAMECD1 = 'D032' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String namecd2 = rs.getString("NAMECD2");
                    final String name1 = rs.getString("NAME1");
                    nameMap.put(namecd2, name1);
                }
                
            } catch (SQLException ex) {
                log.error(ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return nameMap;
        }
    }
}

// eof

