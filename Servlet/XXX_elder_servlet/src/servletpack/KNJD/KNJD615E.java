/*
 * $Id: a696e9621c34f703d6720e517560f8d2b7e08cc7 $
 *
 * 作成日: 2011/05/16
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

/**
 * 学校教育システム 賢者 [成績管理]  三重県・個人成績表（通算）
 */

public class KNJD615E {

    private static final Log log = LogFactory.getLog(KNJD615C.class);
    
    private static String SEMES9 = "9";
    
    private static final String _9900 = "9900";
    
    private static final String _999999 = "999999";
    private static final String _555555 = "555555";
    private static final String _333333 = "333333";
    
    private static final Subclass _subclass999999 = new Subclass(_999999, "合計");
    
    private static final String PATTERN1 = "1";
    private static final String PATTERN2 = "2";
    private static final String PATTERN3 = "3";
    private static final String PATTERN4 = "4";

    private static final String GAKUNENSEISEKI_TESTCD = _9900 + "08";
    private static final String HYOTEI_TESTCD = _9900 + "09";

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
            if (null != db2) {
                db2.commit();
                db2.close();
            }
            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
            svf.VrQuit();
        }
    }

    public static List getMappedList(final Map map, final Object key) {
        if (!map.containsKey(key)) {
            map.put(key, new ArrayList());
        }
        return (List) map.get(key);
    }
    
    private static String sishagonyu(final String avg) {
        return null == avg ? null : new BigDecimal(avg).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        
        String form = "KNJD615E.frm";
        
        final List studentList = Student.getStudentList(db2, _param);

        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            
            final Student student = (Student) it.next();
            
            log.fatal(" schregno = " + student._schregno);

            final Map pageMap = new TreeMap();
            for (final Iterator ityg = student._printYearGradeList.iterator(); ityg.hasNext();) {
                final YearGrade yg = (YearGrade) ityg.next();
                if (yg._isRyunen && "0".equals(_param._knjSchoolMst._schoolDiv)) { // 学年制は留年を非表示
                    continue;
                }
                final Integer page;
                if (pageMap.size() == 0) {
                    page = new Integer(1); // 改ページ
                } else {
                    page = new Integer(pageMap.size() / 3 + pageMap.size() % 3 == 0 ?  0 : 1);
                }
                getMappedList(pageMap, page).add(yg);
                yg._printLine = getMappedList(pageMap, page).size();
            }

            for (final Iterator itp = pageMap.keySet().iterator(); itp.hasNext();) {
                final Integer page = (Integer) itp.next();
                final List pageYearGradeList = getMappedList(pageMap, page);
                
                log.debug(" page = " + page + " / " + pageMap.size() + ", pageYearGradeList = " + pageYearGradeList);
                
                svf.VrSetForm(form, 4);
                
                printSeiseki(svf, student, pageYearGradeList);
            }
        }
    }
    
    private void printSeiseki(final Vrw32alp svf, final Student student, final List printYearGradeList) {
        
        svf.VrsOut("TITLE",  "個人成績表（通算）");
        svf.VrsOut("NAME", student._name);
        
//        log.info(" regdMapList = " + student._regdMapList);
        for (final Iterator it = student._regdMapList.iterator(); it.hasNext();) {
            final Map map = (Map) it.next();
            final YearGrade yg = YearGrade.getYearGrade(KnjDbUtils.getString(map, "YEAR"), KnjDbUtils.getString(map, "GRADE_CD"), printYearGradeList);
            if (null == yg) {
                log.info(" null YearGrade " + KnjDbUtils.getString(map, "YEAR") + ", " + KnjDbUtils.getString(map, "GRADE_CD"));
                continue;
            }
            final String sgradeCd = KnjDbUtils.getString(map, "GRADE_CD");
            final String sattendno = KnjDbUtils.getString(map, "ATTENDNO");
            final String hrName = KnjDbUtils.getString(map, "HR_NAME");
            final String attendno = NumberUtils.isDigits(sattendno) ? (String.valueOf(Integer.parseInt(sattendno)) + "番") : sattendno;
            final int i = NumberUtils.isDigits(sgradeCd) ? Integer.parseInt(sgradeCd) : -1;
//            log.info(" regd i = " + i + " / gradeCd = " + sgradeCd);
            svf.VrsOut("HR_NAME" + i, hrName + " " + attendno);
            svf.VrsOut("NENDO" + i, KenjaProperties.gengou(Integer.parseInt(yg._year)) + "年度");
        }
        
        final Map studentYearTestItemListMap = student.getYearTestItemListMap(_param);
        // 学年ごとに表示
        for (final Iterator ityg = printYearGradeList.iterator(); ityg.hasNext();) {
            final YearGrade yg = (YearGrade) ityg.next();
            svf.VrsOut("GRADE_NAME" + String.valueOf(yg._printLine), Integer.parseInt(yg._gradeCd) + "学年");
            
            final List testitems = getMappedList(studentYearTestItemListMap, yg._year);
            for (int ti = 0; ti < testitems.size(); ti++) {
                
                final TestItem testitem = (TestItem) testitems.get(ti);
                
                final String line = String.valueOf(yg._printLine) + "_" + String.valueOf(ti + 1);
                if (SEMES9.equals(testitem._semester) && GAKUNENSEISEKI_TESTCD.equals(testitem._testcd)) {
                    svf.VrsOut("SEM_TESTNAME" + String.valueOf(yg._printLine) + "_9", "学年成績");
                } else if (SEMES9.equals(testitem._semester) && HYOTEI_TESTCD.equals(testitem._testcd)) {
                    svf.VrsOut("GRAD_VALUENAME" + String.valueOf(yg._printLine), "学年評定");
                } else {
                    svf.VrsOut("SEM_TESTNAME" + line, testitem._testitemname);
                }
            }
            svf.VrsOut("ABSENCE_NAME" + String.valueOf(yg._printLine), "欠課時数");
        }
        
        final int maxSubclass = 20;
        int subclassLine = 0;
        
        final RecordSubclass recSubclass999999 = RecordSubclass.createRecordSubclass(student._recordSubclasses, _subclass999999);
        for (final Iterator it = student._recordSubclasses.values().iterator(); it.hasNext();) { // 教科
            
            final RecordSubclass recSubclass = (RecordSubclass) it.next();
            
            if (recSubclass._subclass == _subclass999999) {
                continue;
            }
            
            printSubclassLine(svf, recSubclass, recSubclass999999, studentYearTestItemListMap, printYearGradeList); // 科目の1行
            
            svf.VrEndRecord();
            subclassLine += 1;
        }
        for (int i = subclassLine; i < maxSubclass; i++) {
            svf.VrEndRecord();
        }
        _hasData = true;
    }
    
    private void printSubclassLine(
            final Vrw32alp svf,
            final RecordSubclass recSubclass,
            final RecordSubclass recSubclass999999,
            final Map studentYearTestItemListMap,
            final List printYearGradeList) {
        
        if (null != recSubclass) {
            // 科目名
            final String subclassname = recSubclass._subclass._subclassname;
            if (null != subclassname && subclassname.length() > 6) {
                if (subclassname.length() > 8) {
                    svf.VrsOut("SUBCLASS2_1", subclassname.substring(0, 8));
                    svf.VrsOut("SUBCLASS2_2", subclassname.substring(8));
                } else {
                    svf.VrsOut("SUBCLASS2_1", subclassname);
                }
            } else {
                svf.VrsOut("SUBCLASS1", subclassname);
            }
            
            // 学年ごとに表示
            for (final Iterator ityg = printYearGradeList.iterator(); ityg.hasNext();) {
                
                final YearGrade yg = (YearGrade) ityg.next();
                
                final List testitems = getMappedList(studentYearTestItemListMap, yg._year);
                for (int ti = 0; ti < testitems.size(); ti++) {
                    
                    final TestItem testitem = (TestItem) testitems.get(ti);
                    
                    if (testitem._year.equals(_param._year) && testitem._semester.compareTo(_param._semester) > 0) {
                        continue; // 年度がログイン年度ならパラメータの学期以降は表示しない
                    }

                    final String line = String.valueOf(yg._printLine) + "_" + String.valueOf(ti + 1);

                    final RecordScore recScore = (RecordScore) recSubclass._scores.get(testitem.getUniqueCd());
                    if (null != recScore) {
                        if (SEMES9.equals(testitem._semester) && GAKUNENSEISEKI_TESTCD.equals(testitem._testcd)) {
                            // 学年成績
                            svf.VrsOut("SCORE" + String.valueOf(yg._printLine) + "_9", recScore._score);
                        } else if (SEMES9.equals(testitem._semester) && HYOTEI_TESTCD.equals(testitem._testcd)) {
                            // 学年評定
                            if ("2".equals(_param._outputHyoutei)) {
                                svf.VrsOut("GRAD_VALUE" + String.valueOf(yg._printLine), recScore._studyrecValuation);
                            } else {
                                svf.VrsOut("GRAD_VALUE" + String.valueOf(yg._printLine), recScore._score);
                            }
                        } else {
                            svf.VrsOut("SCORE" + line, recScore._score);
                        }
                    }

                    final RecordScore recScore999999 = (RecordScore) recSubclass999999._scores.get(testitem.getUniqueCd());
                    if (null != recScore999999) {
                        if (SEMES9.equals(testitem._semester) && GAKUNENSEISEKI_TESTCD.equals(testitem._testcd)) {
                            svf.VrsOut("AVERAGE" + String.valueOf(yg._printLine) + "_9", sishagonyu(recScore999999._avg));
                        } else if (SEMES9.equals(testitem._semester) && HYOTEI_TESTCD.equals(testitem._testcd)) {
                            svf.VrsOut("GRAD_AVERAGE" + String.valueOf(yg._printLine), sishagonyu(recScore999999._avg));
                        } else {
                            svf.VrsOut("AVERAGE" + line, sishagonyu(recScore999999._avg));
                        }
                    }
                }
                
                svf.VrsOut("ABSENCE" + String.valueOf(yg._printLine), (String) recSubclass._kekkas.get(yg._year));
            }
        }
    }
    
    private static class Student {
        final String _schregno;
        String _name;
        String _attendno;
        Map _recordSubclasses = Collections.EMPTY_MAP; // Subclass と RecordSubclass のマップ
        List _regdMapList = Collections.EMPTY_LIST;
        List _printYearGradeList = Collections.EMPTY_LIST;
        Student(final String schregno) {
            _schregno = schregno;
        }
        
        public Map getYearTestItemListMap(final Param param) {
            final Set testItemUniqueCdSet = new HashSet();
            for (final Iterator it = _recordSubclasses.keySet().iterator(); it.hasNext();) {
                final Subclass subclass = (Subclass) it.next();
                final RecordSubclass recSubclass = (RecordSubclass) _recordSubclasses.get(subclass);
                testItemUniqueCdSet.addAll(recSubclass._scores.keySet());
            }
            
            final Map studentYearTestItemListMap = new HashMap();
            for (final Iterator it = param._yearTestItemsMap.keySet().iterator(); it.hasNext();) {
                final String year = (String) it.next();
                final List testItemList = getMappedList(param._yearTestItemsMap, year);
                for (final Iterator tit = testItemList.iterator(); tit.hasNext();) {
                    final TestItem ti = (TestItem) tit.next();
                    if (testItemUniqueCdSet.contains(ti.getUniqueCd()) || SEMES9.equals(ti._semester) && HYOTEI_TESTCD.equals(ti._testcd) || SEMES9.equals(ti._semester) && GAKUNENSEISEKI_TESTCD.equals(ti._testcd)) {
                        getMappedList(studentYearTestItemListMap, year).add(ti);
                    }
                }
            }
            
            for (final Iterator it = studentYearTestItemListMap.values().iterator(); it.hasNext();) {
                final List testItemList = (List) it.next();
                Collections.sort(testItemList);
            }
            
            return studentYearTestItemListMap;
        }

        public TestItem getTestItem(final Param param, final String year, final String semester, final String testcd) {
            if (null == year || null == semester || null == testcd) {
                return null;
            }
            TestItem rtn = TestItem.getTestItem(year, semester, testcd, getMappedList(param._yearTestItemsMap, year));
            return rtn;
        }
        
        public RecordSubclass getRecordSubclass(final String subclasscd) {
            if (null == subclasscd) {
                return null;
            }
            for (final Iterator it = _recordSubclasses.keySet().iterator(); it.hasNext();) {
                final Subclass subclass = (Subclass) it.next();
                if (subclasscd.equals(subclass._subclasscd)) {
                    return (RecordSubclass) _recordSubclasses.get(subclass);
                }
            }
            return null;
        }
        
        public static List getStudentList(final DB2UDB db2, final Param param) {
            final List students = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            
            String sql = null;
            try {
                sql = getRegdSql(param);
                log.debug(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                
                while (rs.next()) {
                    final Student student = new Student(rs.getString("SCHREGNO"));
                    students.add(student);
                    student._name = rs.getString("NAME");
                    student._attendno = NumberUtils.isNumber(rs.getString("ATTENDNO")) ? String.valueOf(Integer.parseInt(rs.getString("ATTENDNO"))) : rs.getString("ATTENDNO");
                }
                
            } catch (SQLException ex) {
                log.debug("Exception: sql = " + sql, ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            
            final String z010Name1 = param.setZ010Name1(db2);
            
            for (final Iterator it = students.iterator(); it.hasNext();) {
                
                final Student student = (Student) it.next();
                
                student._printYearGradeList = getPrintYearGradeList(db2, param, student._schregno);
                
                final Map paramMap = new HashMap();
                paramMap.put("useCurriculumcd", "1");
                paramMap.put("useVirus", param._useVirus);
                paramMap.put("useKoudome", param._useKoudome);
                paramMap.put("useTestCountflg", "TESTITEM_MST_COUNTFLG_NEW_SDIV");

                student._recordSubclasses = RecordSubclass.load(db2, param, student, student._printYearGradeList);
                student._regdMapList = getRegdGradeList(db2, param, student._schregno);
                
                for (final Iterator ityg = student._printYearGradeList.iterator(); ityg.hasNext();) {
                    final YearGrade yg = (YearGrade) ityg.next();
                    if (yg._isRyunen && "0".equals(param._knjSchoolMst._schoolDiv)) { // 学年制は留年を非表示
                        continue;
                    }
                    
                    try {
                        if (null == param._psMap.get("ATTEND_" + yg._year)) {
                            /** 端数計算共通メソッド引数 */
                            final String SSEMESTER = "1";
                            final String ESEMESTER = "9"; 
                            // 出欠の情報
                            final KNJDefineSchool defineschool = param.setClasscode0(db2, yg._year);
                            final String periodInState = AttendAccumulate.getPeiodValue(db2, defineschool, yg._year, SSEMESTER, ESEMESTER);
                            final Map attendSemesMap = AttendAccumulate.getAttendSemesMap(db2, z010Name1, yg._year);
                            
                            final Map semesterMap = param.getSemesterMst(db2, yg._year);
                            final String sdate = (String) semesterMap.get("SDATE");
                            final String edate = (String) semesterMap.get("EDATE");
                            log.info(" attend date range " + yg._year + " = " + sdate + " - " + edate);
                            
                            final Map hasuuMap = AttendAccumulate.getHasuuMap(attendSemesMap, sdate, edate);
                            final boolean semesFlg = ((Boolean) hasuuMap.get("semesFlg")).booleanValue();

                            final String attendSql = AttendAccumulate.getAttendSubclassSql(
                                    semesFlg,
                                    defineschool,
                                    param._knjSchoolMst,
                                    yg._year,
                                    SSEMESTER,
                                    "9",
                                    (String) hasuuMap.get("attendSemesInState"),
                                    periodInState,
                                    (String) hasuuMap.get("befDayFrom"),
                                    (String) hasuuMap.get("befDayTo"),
                                    (String) hasuuMap.get("aftDayFrom"),
                                    (String) hasuuMap.get("aftDayTo"),
                                    null,
                                    null,
                                    "?",
                                    paramMap
                            );
                            //log.debug(" attend sql = " + attendSql);
                            PreparedStatement pps = db2.prepareStatement(attendSql);
                            param._psMap.put("ATTEND_" + yg._year, pps);
                        }
                        
                        final PreparedStatement pps = (PreparedStatement) param._psMap.get("ATTEND_" + yg._year);
                        pps.setString(1, student._schregno);
                        rs = pps.executeQuery();
                        while (rs.next()) {
                            final String subclasscd = rs.getString("SUBCLASSCD");
                            final RecordSubclass rec = student.getRecordSubclass(subclasscd);
                            if (null != rec && rs.getBigDecimal("SICK2").doubleValue() != 0.0) {
                                rec._kekkas.put(yg._year, rs.getString("SICK2"));
                                //log.info(" schregno = " + student._schregno + ", subclasscd = " + subclasscd + ", " + rec._kekkas);
                            }
                        }
                        DbUtils.closeQuietly(rs);
                        
                    } catch (Exception e) {
                        log.error("exception!", e);
                    } finally {
                        db2.commit();
                        DbUtils.closeQuietly(rs);
                    }
                }
            }
            
            for (final Iterator it = param._psMap.values().iterator(); it.hasNext();) {
                PreparedStatement aps = (PreparedStatement) it.next();
                DbUtils.closeQuietly(aps);
            }

            return students;
        }

        private static List getPrintYearGradeList(final DB2UDB db2, final Param param, final String schregno) {
            final List printGradeList = new ArrayList();
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH REGD AS ( ");
            stb.append("  SELECT DISTINCT GRADE, YEAR ");
            stb.append("   FROM SCHREG_REGD_DAT T1 ");
            stb.append("   WHERE ");
            stb.append("   T1.SCHREGNO = '" + schregno + "' ");
            stb.append(" ) ");
            stb.append("  SELECT  ");
            stb.append("   T1.YEAR, ");
            stb.append("   T1.GRADE, ");
            stb.append("   T2.GRADE_CD, ");
            stb.append("   CASE WHEN  (T1.YEAR, T1.GRADE) NOT IN (SELECT MAX(YEAR), GRADE FROM REGD GROUP BY GRADE) THEN 1 ELSE 0 END AS IS_RYUNEN, ");
            stb.append("   CASE WHEN  (T1.YEAR, T1.GRADE) NOT IN (SELECT MIN(YEAR), GRADE FROM REGD GROUP BY GRADE) THEN 1 ELSE 0 END AS IS_RYUNEN_NEXT ");
            stb.append("  FROM REGD T1 ");
            stb.append("  INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ");
            stb.append("  WHERE T1.YEAR <= '" + param._year + "' ");
            stb.append("  ORDER BY ");
            stb.append("   T1.YEAR ");
            
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = stb.toString();
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String year = rs.getString("YEAR");
                    final String gradeCd = rs.getString("GRADE_CD");
                    final boolean isRyunen = "1".equals(rs.getString("IS_RYUNEN"));
                    final boolean isRyunenNext = "1".equals(rs.getString("IS_RYUNEN_NEXT"));
                    printGradeList.add(new YearGrade(year, gradeCd, isRyunen, isRyunenNext));
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            Collections.sort(printGradeList);
            return printGradeList;
        }

        public static String getRegdSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            // 異動者を除外した学籍の表 => 組平均において異動者の除外に使用
            stb.append("WITH SCHNO_A AS(");
            stb.append(    "SELECT  T1.SCHREGNO, T1.GRADE, T1.HR_CLASS, T1.ATTENDNO, T1.YEAR, T1.SEMESTER, T1.COURSECD, T1.MAJORCD, T1.COURSECODE ");
            stb.append(    "FROM    SCHREG_REGD_DAT T1,SEMESTER_MST T2 ");
            stb.append(    "WHERE   T1.YEAR = '" + param._year + "' ");
            if (SEMES9.equals(param._semester)) {
                stb.append(        "AND T1.SEMESTER = '"+ param._semeFlg +"' ");
            } else {
                stb.append(        "AND T1.SEMESTER = '"+ param._semester +"' ");
            }
            stb.append(        "AND T1.YEAR = T2.YEAR ");
            stb.append(        "AND T1.SEMESTER = T2.SEMESTER ");
            stb.append(        "AND T1.GRADE || T1.HR_CLASS = '" + param._gradeHrclass + "' ");
            stb.append(        "AND T1.SCHREGNO IN " + SQLUtils.whereIn(true, param._categorySelected) + " ");
//            //                      在籍チェック:転学(2)・退学(3)者は除外 但し異動日が学期終了日または異動基準日より小さい場合
//            //                                   転入(4)・編入(5)者は除外 但し異動日が学期終了日または異動基準日より大きい場合 
//            stb.append(        "AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST S1 ");
//            stb.append(                       "WHERE   S1.SCHREGNO = T1.SCHREGNO ");
//            stb.append(                           "AND ((S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < CASE WHEN T2.EDATE < '" + param._date + "' THEN T2.EDATE ELSE '" + param._date + "' END) ");
//            stb.append(                             "OR (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE > CASE WHEN T2.EDATE < '" + param._date + "' THEN T2.EDATE ELSE '" + param._date + "' END)) ) ");
//            //                      異動者チェック：留学(1)・休学(2)者は除外 但し学期終了日または基準日が異動開始日と終了日内にある場合
//            stb.append(        "AND NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT S1 ");
//            stb.append(                       "WHERE   S1.SCHREGNO = T1.SCHREGNO ");
//            stb.append(                           "AND S1.TRANSFERCD IN ('1','2') AND CASE WHEN T2.EDATE < '" + param._date + "' THEN T2.EDATE ELSE '" + param._date + "' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE) ");
            stb.append(    ") ");
            
            //メイン表
            stb.append("SELECT  T1.SCHREGNO, T1.ATTENDNO, T2.HR_NAME, T5.NAME, ");
            stb.append("        T1.GRADE, T1.COURSECD, T1.MAJORCD, T1.COURSECODE ");
            stb.append("FROM    SCHNO_A T1 ");
            stb.append("        INNER JOIN SCHREG_BASE_MST T5 ON T1.SCHREGNO = T5.SCHREGNO ");
            stb.append("        INNER JOIN SCHREG_REGD_HDAT T2 ON T2.YEAR = T1.YEAR AND ");
            stb.append("                                          T2.SEMESTER = T1.SEMESTER AND ");
            stb.append("                                          T2.GRADE || T2.HR_CLASS = T1.GRADE || T1.HR_CLASS ");
            stb.append("ORDER BY T1.GRADE, T1.HR_CLASS, ATTENDNO");
            return stb.toString();
        }
        
        private static List getRegdGradeList(final DB2UDB db2, final Param param, final String schregno) {
            final StringBuffer sql = new StringBuffer();
            sql.append(" WITH REGD_MAX_SEMESTER AS ( ");
            sql.append(" SELECT ");
            sql.append("     T1.SCHREGNO, T1.YEAR, MAX(SEMESTER) AS SEMESTER, T1.GRADE ");
            sql.append(" FROM ");
            sql.append("     SCHREG_REGD_DAT T1 ");
            sql.append(" WHERE ");
            sql.append("     T1.SCHREGNO = '" + schregno + "' ");
            sql.append("     AND (T1.YEAR < '" + param._year + "' ");
            sql.append("          OR (T1.YEAR = '" + param._year + "' ");
            if (SEMES9.equals(param._semester)) {
                sql.append("               AND T1.SEMESTER <= '" + param._semeFlg + "'");
            } else {
                sql.append("               AND T1.SEMESTER <= '" + param._semester + "'");
            }
            sql.append("          )) ");
            sql.append(" GROUP BY ");
            sql.append("     T1.SCHREGNO, T1.YEAR, T1.GRADE ");
            sql.append(" ) ");
            sql.append(" SELECT ");
            sql.append("     T1.YEAR, ");
            sql.append("     T1.GRADE, ");
            sql.append("     T2.GRADE_CD, ");
            sql.append("     T4.HR_NAME, ");
            sql.append("     T3.ATTENDNO ");
            sql.append(" FROM ");
            sql.append("     REGD_MAX_SEMESTER T1 ");
            sql.append("     INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR ");
            sql.append("         AND T2.GRADE = T1.GRADE ");
            sql.append("     INNER JOIN SCHREG_REGD_DAT T3 ON T3.YEAR = T1.YEAR ");
            sql.append("         AND T3.SEMESTER = T1.SEMESTER ");
            sql.append("         AND T3.GRADE = T1.GRADE ");
            sql.append("         AND T3.SCHREGNO = T1.SCHREGNO ");
            sql.append("     LEFT JOIN SCHREG_REGD_HDAT T4 ON T4.YEAR = T3.YEAR ");
            sql.append("         AND T4.SEMESTER = T3.SEMESTER ");
            sql.append("         AND T4.GRADE = T3.GRADE ");
            sql.append("         AND T4.HR_CLASS = T3.HR_CLASS ");
            sql.append(" ORDER BY ");
            sql.append("     T1.YEAR ");
            
            return KnjDbUtils.query(db2, sql.toString());
        }
    }
    
    private static class Subclass implements Comparable {
        final String _subclasscd;
        final String _subclassname;
        Subclass(final String subclasscd, final String subclassname) {
            _subclasscd = subclasscd;
            _subclassname = subclassname;
        }
        public int hashCode() {
            return ("SUBCLASS" + _subclasscd).hashCode();
        }
        public int compareTo(Object o) {
            if (null == o || !(o instanceof Subclass)) {
                return -1;
            }
            final Subclass other = (Subclass) o;
            int ret = 0;
            if (0 == ret) {
                ret = _subclasscd.compareTo(other._subclasscd);
            }
            return ret;
        }
        public String toString() {
            return "(" + _subclasscd + ":" + _subclassname + ")";
        }
    }
    
    private static String getSubclasscd(final ResultSet rs, final Param param) throws SQLException {
        final String subclassCd = rs.getString("SUBCLASSCD");
        if (_999999.equals(subclassCd) || _333333.equals(subclassCd) || _555555.equals(subclassCd)) {
            return subclassCd;
        }
        return rs.getString("CLASSCD") + "-" + rs.getString("SCHOOL_KIND") + "-" + rs.getString("CURRICULUM_CD") + "-" + subclassCd;
    }
    
    private static class RecordSubclass implements Comparable {
        final Subclass _subclass;
        final Map _scores = new HashMap();
        final Map _kekkas = new HashMap();
        RecordSubclass(final Subclass subclass) {
            _subclass = subclass;
        }
        public static Map load(final DB2UDB db2, final Param param, final Student student, final List printGrade) {
            final Set testitemNotExistsSet = new TreeSet();
            final Map recordSubclassMap = new TreeMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = sql(param, student._schregno, printGrade);
                log.info(" record sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String year = rs.getString("YEAR");
                    final String semes = rs.getString("SEMESTER");
                    final String testcd = rs.getString("TESTCD");
                    
                    final TestItem testitem = student.getTestItem(param, year, semes, testcd);
                    if (null == testitem) {
                        testitemNotExistsSet.add("考査種別なし:(year, semester, testcd) = (" + year + ", " + semes + ", " + testcd + ")");
                        continue;
                    }
                    
                    final String subclasscd = getSubclasscd(rs, param);
                    final Subclass subclass = (Subclass) param._subclassMap.get(subclasscd);
                    if (null == subclass) {
//                        log.debug(" 科目無し : " + subclasscd);
                        continue;
                    }
                    // log.debug(" subclass = " + subclass);
                    final String score = rs.getString("SCORE");
                    final String avg = rs.getString("AVG");
                    final String studyrecValuation = rs.getString("VALUATION");

                    final RecordSubclass recSubclass = createRecordSubclass(recordSubclassMap, subclass);
                    
                    recSubclass._scores.put(testitem.getUniqueCd(), new RecordScore(testitem, score, avg, studyrecValuation));
                }
                
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            for (final Iterator it = testitemNotExistsSet.iterator(); it.hasNext();) {
                final String notExist = (String) it.next();
                log.info(notExist);
            }

            return recordSubclassMap;
        }
        
        public static String sql(final Param param, final String schregno, final List printYearGradeList) {
            
            final StringBuffer stbys = new StringBuffer();
            String union = "";
            for (final Iterator ityg = printYearGradeList.iterator(); ityg.hasNext();) {
                final YearGrade yg = (YearGrade) ityg.next();
                
                for (final Iterator it = getMappedList(param._yearTestItemsMap, yg._year).iterator(); it.hasNext();) {
                    final TestItem testitem = (TestItem) it.next();
                    stbys.append(union).append(" VALUES('" ).append(schregno).append("', '").append(testitem._year).append("', '").append(testitem._semester).append("', '").append(testitem._testcd).append("') ");
                    union = " UNION ";
                }
            }
            
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH YEAR_SEMESTER (SCHREGNO, YEAR, SEMESTER, TESTCD) AS ( ");
            stb.append(stbys);
            stb.append(" ), SUBCLASSES AS ( ");
            stb.append(" SELECT DISTINCT  ");
            stb.append("     T1.SCHREGNO, T1.YEAR, T1.SEMESTER, T1.TESTKINDCD, T1.TESTITEMCD, ");
            stb.append("     T1.SCORE_DIV, ");
            stb.append("     T1.CLASSCD, ");
            stb.append("     T1.SCHOOL_KIND, ");
            stb.append("     T1.CURRICULUM_CD, ");
            stb.append("     T1.SUBCLASSCD ");
            stb.append(" FROM  ");
            stb.append("     RECORD_RANK_SDIV_DAT T1 ");
            stb.append("     INNER JOIN YEAR_SEMESTER T4 ON T4.SCHREGNO = T1.SCHREGNO ");
            stb.append("         AND T4.YEAR = T1.YEAR ");
            stb.append("         AND T4.SEMESTER = T1.SEMESTER ");
            stb.append("         AND T4.TESTCD = T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV ");
            if ("1".equals(param._outputSougou)) {
                stb.append(" WHERE ");
                stb.append("     T1.CLASSCD <> '90' ");
            } else {
                stb.append(" UNION ");
                stb.append(" SELECT DISTINCT  ");
                stb.append("     T1.SCHREGNO, T1.YEAR, T1.SEMESTER, T1.TESTKINDCD, T1.TESTITEMCD, ");
                stb.append("     T1.SCORE_DIV, ");
                stb.append("     T1.CLASSCD, ");
                stb.append("     T1.SCHOOL_KIND, ");
                stb.append("     T1.CURRICULUM_CD, ");
                stb.append("     T1.SUBCLASSCD ");
                stb.append(" FROM  ");
                stb.append("     RECORD_SCORE_DAT T1 ");
                stb.append("     INNER JOIN YEAR_SEMESTER T4 ON T4.SCHREGNO = T1.SCHREGNO ");
                stb.append("         AND T4.YEAR = T1.YEAR ");
                stb.append("         AND T4.SEMESTER = T1.SEMESTER ");
                stb.append("         AND T4.TESTCD = T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV ");
                stb.append(" WHERE ");
                stb.append("     T1.CLASSCD <> '90' ");
            }
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     T0.YEAR, ");
            stb.append("     T0.SEMESTER, ");
            stb.append("     T0.CLASSCD, ");
            stb.append("     T0.SCHOOL_KIND, ");
            stb.append("     T0.CURRICULUM_CD, ");
            stb.append("     T0.SUBCLASSCD, ");
            stb.append("     T0.TESTKINDCD || T0.TESTITEMCD || T0.SCORE_DIV AS TESTCD, ");
            stb.append("     T1.SCORE, ");
            stb.append("     T1.AVG, ");
            stb.append("     T3.VALUATION ");
            stb.append(" FROM ");
            stb.append("     SUBCLASSES T0 ");
            stb.append("     LEFT JOIN RECORD_RANK_SDIV_DAT T1 ON T1.SCHREGNO = T0.SCHREGNO ");
            stb.append("         AND T1.YEAR = T0.YEAR ");
            stb.append("         AND T1.SEMESTER = T0.SEMESTER ");
            stb.append("         AND T1.TESTKINDCD = T0.TESTKINDCD ");
            stb.append("         AND T1.TESTITEMCD = T0.TESTITEMCD ");
            stb.append("         AND T1.SCORE_DIV = T0.SCORE_DIV ");
            stb.append("         AND T1.CLASSCD = T0.CLASSCD ");
            stb.append("         AND T1.SCHOOL_KIND = T0.SCHOOL_KIND ");
            stb.append("         AND T1.CURRICULUM_CD = T0.CURRICULUM_CD ");
            stb.append("         AND T1.SUBCLASSCD = T0.SUBCLASSCD ");
            stb.append("         AND T1.SCORE IS NOT NULL ");
            stb.append("     LEFT JOIN SCHREG_STUDYREC_DAT T3 ON T3.YEAR = T1.YEAR ");
            stb.append("         AND T3.SCHREGNO = T0.SCHREGNO ");
            stb.append("         AND T3.CLASSCD = T0.CLASSCD ");
            stb.append("         AND T3.SCHOOL_KIND = T0.SCHOOL_KIND ");
            stb.append("         AND T3.CURRICULUM_CD = T0.CURRICULUM_CD ");
            stb.append("         AND T3.SUBCLASSCD = T0.SUBCLASSCD ");
            stb.append(" ORDER BY ");
            stb.append("     T0.YEAR, ");
            stb.append("     T0.SEMESTER, ");
            stb.append("     T0.CLASSCD, ");
            stb.append("     T0.SCHOOL_KIND, ");
            stb.append("     T0.CURRICULUM_CD, ");
            stb.append("     T0.SUBCLASSCD, ");
            stb.append("     T3.SCHOOLCD, ");
            stb.append("     T3.ANNUAL ");
            return stb.toString();
        }
        
        public static RecordSubclass createRecordSubclass(final Map recordSubclassMap, final Subclass subclass) {
            if (!recordSubclassMap.containsKey(subclass)) {
                recordSubclassMap.put(subclass, new RecordSubclass(subclass));
            }
            return (RecordSubclass) recordSubclassMap.get(subclass);
        }
        
        public int hashCode() {
            return "RecordSubclass".hashCode() + _subclass.hashCode();
        }
        public int compareTo(Object o) {
            if (null == o || !(o instanceof RecordSubclass)) {
                return -1;
            }
            final RecordSubclass other = (RecordSubclass) o;
            return _subclass.compareTo(other._subclass);
        }
        public String toString() {
            return "RecordSubclass" + _subclass.toString();
        }
    }
    
    private static class TestItem implements Comparable {
        final String _year;
        final String _semester;
        final String _testcd; // TESTKINDCD + TESTITEMCD + SCORE_DIV
        final String _testitemname;
        public TestItem(final String year, final String semester, final String testcd, final String testitemname) {
            _year = year;
            _semester = semester;
            _testcd = testcd;
            _testitemname = testitemname;
        }
        public String getUniqueCd() {
            return _year + _semester + _testcd;
        }
        public int compareTo(Object o) {
            if (!(o instanceof TestItem)) {
                return -1;
            }
            final TestItem other = (TestItem) o;
            int ret;
            ret = _year.compareTo(other._year);
            if (0 != ret) {
                return ret;
            }
            ret = _semester.compareTo(other._semester);
            if (0 != ret) {
                return ret;
            }
            ret = _testcd.compareTo(other._testcd);
            return ret;
        }
        public int hashCode() {
            return _year.hashCode() + _semester.hashCode() + _testcd.hashCode();
        }
        public static TestItem getTestItem(final String year, final String semester, final String testcd, final Collection col) {
            TestItem rtn = null;
            for (final Iterator semIt = col.iterator(); semIt.hasNext();) {
                final TestItem testitem = (TestItem) semIt.next();
                if (testcd.equals(testitem._testcd) && year.equals(testitem._year) && semester.equals(testitem._semester)) {
                    rtn = testitem;
                    break;
                }
            }
            return rtn;
        }
        public String toString() {
            return "[" + _year + ":" + _semester + _testcd + "]";
        }
    }
    
    private static class RecordScore {
        final TestItem _testcd;
        final String _score;    // RECORD_RANK_DAT.SCORE
        final String _avg;      // RECORD_RANK_DAT.AVG
        final String _studyrecValuation;

        RecordScore(final TestItem testcd,
                final String score,
                final String avg,
                final String studyrecValuation) {
            _testcd = testcd;
            _score =score;
            _avg = avg;
            _studyrecValuation = studyrecValuation;
        }
        public String toString() {
            return "RecordScore(" + _testcd + ":" + _score + ")";
        }
    }
    
    private static class YearGrade implements Comparable {
        final String _year;
        final String _gradeCd;
        final boolean _isRyunen;
        final boolean _isRyunenNext;
        int _printLine;
        YearGrade(final String year, final String gradeCd, final boolean isRyunen, final boolean isRyunenNext) {
            _year = year;
            _gradeCd = gradeCd;
            _isRyunen = isRyunen;
            _isRyunenNext = isRyunenNext;
        }
        
        public int compareTo(final Object o) {
            if (null == o || !(o instanceof YearGrade)) {
                return -1;
            }
            final YearGrade other = (YearGrade) o;
            return _year.compareTo(other._year);
        }

        public static YearGrade getYearGrade(final String year, final String gradeCd, final List printGrade) {
            for (final Iterator it = printGrade.iterator(); it.hasNext();) {
                final YearGrade yg = (YearGrade) it.next();
                if (yg._year.equals(year) && yg._gradeCd.equals(gradeCd)) {
                    return yg;
                }
            }
            return null;
        }

        public String toString() {
            return "YearGrade(" + _year + ", " + _gradeCd + ", ryunen? " + _isRyunen + ", ryunenNext?" + _isRyunenNext + ")";
        }
    }
    
    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 56966 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        
        final String _year;
        final String _semester;
        final String _semeFlg;
        final String _grade;
        final String _gradeHrclass;
        final String[] _categorySelected;
        final String _outputHyoutei;
        final String _outputSougou;
        final Map _yearTestItemsMap;
        final String _useVirus;
        final String _useKoudome;
        final Map _psMap = new HashMap();
        final Map _defineschoolMap = new HashMap();
        
        final Map _subclassMap;
        KNJSchoolMst _knjSchoolMst = null;
        
        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _semeFlg = request.getParameter("SEME_FLG");
            _grade = request.getParameter("GRADE");
            _gradeHrclass = request.getParameter("GRADE_HR_CLASS");
            _categorySelected = request.getParameterValues("category_selected");
            _outputHyoutei = request.getParameter("OUTPUT_HYOUTEI");
            _outputSougou = request.getParameter("OUTPUT_SOUGOU");
            _useVirus = request.getParameter("useVirus");
            _useKoudome = request.getParameter("useKoudome");
            _yearTestItemsMap = getYearTestItems(db2);
            
            _subclassMap = getAllSubclasses(db2);
            
            try {
                _knjSchoolMst = new KNJSchoolMst(db2, _year);
            } catch (Exception e) {
                log.error("exception!", e);
            }
        }
        
        private Map getAllSubclasses(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map subclassMap = new TreeMap();
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     T1.CLASSCD, ");
                stb.append("     T1.SCHOOL_KIND, ");
                stb.append("     T1.CURRICULUM_CD, ");
                stb.append("     T1.SUBCLASSCD, ");
                stb.append("     T1.SUBCLASSNAME  ");
                stb.append(" FROM ");
                stb.append("     SUBCLASS_MST T1 ");
                stb.append(" ORDER BY ");
                stb.append("     T1.SUBCLASSCD ");
                
                final String sql = stb.toString();
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    
                    final String subclasscd = getSubclasscd(rs, this);
                    final String subclassname = rs.getString("SUBCLASSNAME");
                    
                    subclassMap.put(subclasscd, new Subclass(subclasscd, subclassname));
                }
                subclassMap.put(_999999, _subclass999999);
                
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return subclassMap;
        }
        
        private Map getYearTestItems(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map yearTestItems = new HashMap();
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("   T1.YEAR, T1.SEMESTER, T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV AS TESTCD, VALUE(T1.TESTITEMNAME, '') AS TESTITEMNAME ");
                stb.append(" FROM ");
                stb.append("   TESTITEM_MST_COUNTFLG_NEW_SDIV T1 ");
                stb.append(" WHERE ");
                stb.append("   T1.YEAR <= '" + _year + "' ");
                stb.append("   AND T1.SCORE_DIV IN ('01', '08', '09') ");
                stb.append("   AND (NOT (T1.SEMESTER <> '9' AND T1.SCORE_DIV = '09'))");
                stb.append(" ORDER BY ");
                stb.append("   T1.YEAR, T1.SEMESTER, T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV ");

                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String year = rs.getString("YEAR");
                    final String semester = rs.getString("SEMESTER");
                    final String testcd = rs.getString("TESTCD");
                    final String testitemname = rs.getString("TESTITEMNAME");
                    
                    getMappedList(yearTestItems, year).add(new TestItem(year, semester, testcd, testitemname));
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return yearTestItems;
        }
        
        private KNJDefineSchool setClasscode0(final DB2UDB db2, final String year) {
            if (null == _defineschoolMap.get(year)) {
                KNJDefineSchool defineschool = null;
                try {
                    defineschool = new KNJDefineSchool();
                    defineschool.defineCode(db2, year);         //各学校における定数等設定
                } catch (Exception ex) {
                    log.warn("semesterdiv-get error!", ex);
                }
                _defineschoolMap.put(year, defineschool);
            }
            return (KNJDefineSchool) _defineschoolMap.get(year);
        }

        /**
         * 名称マスタ NAMECD1='Z010' NAMECD2='00'読込
         */
        private String setZ010Name1(DB2UDB db2) {
            String name1 = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    name1 = rs.getString("NAME1");
                }
            } catch (SQLException ex) {
                log.debug("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return name1;
        }

        private Map getSemesterMst(final DB2UDB db2, final String year) {
            Map rtn = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = "SELECT SDATE, EDATE FROM SEMESTER_MST WHERE YEAR = '" + year + "' AND SEMESTER = '9' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    rtn.put("SDATE", rs.getString("SDATE"));
                    rtn.put("EDATE", rs.getString("EDATE"));
                }
            } catch (SQLException ex) {
                log.debug("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }
    }
}

// eof
