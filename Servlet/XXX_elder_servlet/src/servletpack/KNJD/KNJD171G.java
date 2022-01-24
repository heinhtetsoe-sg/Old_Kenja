/*
 * $Id: 68daa868c394904f267f2ee23047b3b4890ac134 $
 *
 * 作成日: 2012/02/21
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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

import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_Control;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

/**
 * 学校教育システム 賢者 [成績管理] 通知票
 */

public class KNJD171G {

    private static final Log log = LogFactory.getLog(KNJD171G.class);
    
    private static final String _1990008 = "1990008";
    private static final String _2990008 = "2990008";

    private boolean _hasData;
    
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
        Param param = null;
        try {
            response.setContentType("application/pdf");
            
            svf.VrInit();
            svf.VrSetSpoolFileStream(response.getOutputStream());
            
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
            
            param = createParam(db2, request);
            
            _hasData = false;
            
            printMain(db2, param, svf);
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

    private void printMain(final DB2UDB db2, final Param param, final Vrw32alp svf) {
        
        final List studentList = Student.getStudentList(db2, param);
        
        log.debug(" testcdList = " + param.getTestcdList());

        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            log.debug(" student = " + student._schregno + " : " + student._name);
            
            printStudent(db2, param, svf, student);
        }
    }
    
    private int getMS932ByteCount(final String str) {
        int ret = 0;
        if (null != str) {
            try {
                ret = str.getBytes("MS932").length;
            } catch (Exception e) {
                log.error("exception!", e);
            }
        }
        return ret;
    }
    
    private boolean notTargetSubclasscd(final Param param, final String fullSubclasscd, final String div) {
        if (null == fullSubclasscd) {
            return true;
        }
        for (final Iterator it = param._d026Map.keySet().iterator(); it.hasNext();) {
            final String subclasscd = (String) it.next();
            if (fullSubclasscd.endsWith(subclasscd)) {
                final Map m = (Map) param._d026Map.get(subclasscd);
                return (null != m.get(div)) ? true : false;
            }
        }
        return false;
    }
    
    private static Map getMappedMap(final Map map, final String key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new TreeMap());
        }
        return (Map) map.get(key1);
    }

    private static List getMappedList(final Map map, final String key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new ArrayList());
        }
        return (List) map.get(key1);
    }
    
    /**
     * 成績を印刷する
     * @param db2
     * @param param
     * @param svf
     * @param student
     * @param viewClassList
     */
    private void printStudent(
            final DB2UDB db2,
            final Param param,
            final Vrw32alp svf,
            final Student student) {
        
        final String form = "KNJD171G.frm";
        svf.VrSetForm(form, 1);
        
        printSvfHeader(svf, param, student);
        
        printAttendSemes(svf, param, student);
        
        printShoken(svf, param, student);

        printSeiseki(svf, param, student);

        svf.VrEndPage();
        _hasData = true;
    }
    
    private void printSvfHeader(final Vrw32alp svf, final Param param, final Student student) {
    	if (param._isSeireki) {
            svf.VrsOut("NENDO", StringUtils.defaultString(param._year) + "年度"); // 年度
    	} else {
            svf.VrsOut("NENDO", KenjaProperties.gengou(Integer.parseInt(param._year)) + "年度"); // 年度
    	}
        svf.VrsOut("HR_NAME", student._hrName); // 年組名称

        final int nameKeta = getMS932ByteCount(student._name);
        svf.VrsOut("NAME" + (nameKeta > 32 ? "3" : nameKeta > 26 ? "2" : "1"), student._name); // 氏名

        svf.VrsOut("TEACHER_NAME", param._tr1Name);
        
        final int tr1nameKeta = getMS932ByteCount(param._tr1Name);
        svf.VrsOut("TEACHER_NAME1" + (tr1nameKeta > 22 ? "_3" : tr1nameKeta > 18 ? "_2" : ""), param._tr1Name); // 担任名
        final int subtr1nameKeta = getMS932ByteCount(param._subtr1Name);
        svf.VrsOut("TEACHER_NAME2" + (subtr1nameKeta > 22 ? "_3" : subtr1nameKeta > 18 ? "_2" : ""), param._subtr1Name); // 担任名

        svf.VrsOut("SCHOOL_NAME", param._certifSchoolSchoolName); // 学校名

        svf.VrsOutn("SEMESTER1", 1, (String) param._semesternameMap.get("1")); // 学期
        svf.VrsOutn("SEMESTER1", 2, (String) param._semesternameMap.get("2")); // 学期
        svf.VrsOutn("SEMESTER1", 3, "合計"); // 学期
        svf.VrsOut("SEMESTER2_1", (String) param._semesternameMap.get("1")); // 学期
        svf.VrsOut("SEMESTER2_2", (String) param._semesternameMap.get("2")); // 学期
    }
    
    private static String toString(int n) {
        return String.valueOf(n);
    }
    
    /**
     * 出欠
     */
    private void printAttendSemes(final Vrw32alp svf, final Param param, final Student student) {
        for (final Iterator it = student._attendSemesDatList.iterator(); it.hasNext();) {
            final AttendSemesDat attendSemesDat = (AttendSemesDat) it.next();
            if (!NumberUtils.isDigits(attendSemesDat._semester)) {
                continue;
            }
            
            final int j;
            if ("9".equals(attendSemesDat._semester)) {
                j = 3; // 3学期制の場合
            } else {
                j = Integer.parseInt(attendSemesDat._semester);
            }
            
            svf.VrsOutn("LESSON", j, toString(attendSemesDat._lesson));
            svf.VrsOutn("SUSPEND", j, toString(attendSemesDat._suspend + attendSemesDat._mourning));
            svf.VrsOutn("MUST", j, toString(attendSemesDat._mlesson));
            svf.VrsOutn("ABSENT", j, toString(attendSemesDat._sick));
            svf.VrsOutn("ATTEND", j, toString(attendSemesDat._present));
            svf.VrsOutn("LATE", j, toString(attendSemesDat._late));
            svf.VrsOutn("EARLY", j, toString(attendSemesDat._early));
        }
    }

    /**
     * 所見
     */
    private void printShoken(final Vrw32alp svf, final Param param, final Student student) {
        for (final Iterator it = student._shokenMap.keySet().iterator(); it.hasNext();) {
            final String semester = (String) it.next(); 
            final String shoken = (String) student._shokenMap.get(semester);
            final String[] token = KNJ_EditEdit.get_token(shoken, 24 * 2, 5);
            if (null != token) {
                for (int j = 0; j < token.length; j++) {
                    svf.VrsOutn("VIEW" + semester, j + 1, token[j]); // 所見
                }
            }
        }
    }

    /**
     * 成績
     */
    private void printSeiseki(final Vrw32alp svf, final Param param, final Student student) {

        final int maxLine = 20;

        String classname = null;
        int line;
        line = 1;
        final List testcdList = param.getTestcdList();
        for (final Iterator it = student._subclassList.iterator(); it.hasNext();) {
            final Subclass subclass = (Subclass) it.next();
            
            if (notTargetSubclasscd(param, subclass._subclasscd, "ABBV1")) {
                continue;
            }
            
            //log.debug(" subclasscd = " + subclass._subclasscd + " : " + subclass._subclassname + ", " + subclass._detailMap);

            if (null == classname || !classname.equals(subclass._classname)) {
                svf.VrsOutn("CLASS_NAME", line, subclass._classname); // 科目名
            }
            svf.VrsOutn("SUBCLASS_NAME" + (getMS932ByteCount(subclass._subclassname) > 22 ? "2" : "1"), line, subclass._subclassname); // 科目名
            
            for (final Iterator tit = testcdList.iterator(); tit.hasNext();) {
                final String semtestcd = (String) tit.next();
                final SubclassDetail detail = (SubclassDetail) subclass._detailMap.get(semtestcd);
                if (null == detail) {
                    continue;
                }
                if (_1990008.equals(semtestcd)) {
                    svf.VrsOutn("VAL1", line, detail._score); // 学期評価
                } else if (_2990008.equals(semtestcd)) {
                    svf.VrsOutn("VAL2", line, detail._score); // 学期評価
                }
            }
            line++;
            classname = subclass._classname;
            if (maxLine < line) {
                break;
            }
        }
    }
    
    private static class Student {
        final String _schregno;
        final String _name;
        final String _hrName;
        final String _attendno;
        final String _gradeCourse;
        List _attendSemesDatList = new ArrayList(); // 出欠のようす
        List _subclassList = new ArrayList();
        final Map _shokenMap = new HashMap();
        //List _combinedSubclassList = new ArrayList();
        
        public Student(final String schregno, final String name, final String hrName, final String attendno, final String gradeCourse) {
            _schregno = schregno;
            _name = name;
            _hrName = hrName;
            _attendno = attendno;
            _gradeCourse = gradeCourse;
        }
        
        private static List getStudentList(final DB2UDB db2, final Param param) {
            final List studentList = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getStudentSql(param);
                log.debug(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String schregno = rs.getString("SCHREGNO");
                    final String name = "1".equals(rs.getString("USE_REAL_NAME")) ? rs.getString("REAL_NAME") : rs.getString("NAME");
                    final String hrName = rs.getString("HR_NAME"); // "1".equals(param._printRegd) ? rs.getString("REGDH_HR_NAME") : rs.getString("HR_NAME");
                    final String attendno = rs.getString("ATTENDNO");
                    final String gradeCourse = rs.getString("COURSE");
                    final Student student = new Student(schregno, name, hrName, attendno, gradeCourse);
                    studentList.add(student);
                }
                
            } catch (SQLException e) {
                log.error("exception!!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            
            AttendSemesDat.setAttendSemesDatList(db2, param, studentList);
            Subclass.setSubclassList(db2, param, studentList);
            setShokenMap(db2, param, studentList);

            return studentList;
        }
        
        private static String getStudentSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            // 異動者を除外した学籍の表 => 組平均において異動者の除外に使用
            stb.append("WITH SCHNO_A AS(");
            stb.append("  SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.GRADE, ");
            stb.append("     T1.HR_CLASS, ");
            stb.append("     REGDH.HR_NAME AS REGDH_HR_NAME, ");
            stb.append("     T1.GRADE || T1.COURSECD || T1.MAJORCD || T1.COURSECODE AS COURSE, ");
            stb.append("     T1.ATTENDNO, ");
            stb.append("     T1.SEMESTER ");
            stb.append("  FROM    SCHREG_REGD_DAT T1 ");
            stb.append("          INNER JOIN V_SEMESTER_GRADE_MST T2 ON T1.YEAR = T2.YEAR ");
            stb.append("     AND T1.SEMESTER = T2.SEMESTER ");
            stb.append("    LEFT JOIN SCHREG_REGD_DAT REGD ON REGD.YEAR = T1.YEAR ");
            stb.append("        AND T1.SEMESTER = REGD.SEMESTER ");
            stb.append("        AND T1.SCHREGNO = REGD.SCHREGNO ");
            stb.append("    LEFT JOIN SCHREG_REGD_HDAT REGDH ON REGDH.YEAR = REGD.YEAR ");
            stb.append("        AND REGD.SEMESTER = REGDH.SEMESTER ");
            stb.append("        AND REGD.GRADE = REGDH.GRADE ");
            stb.append("        AND REGD.HR_CLASS = REGDH.HR_CLASS ");
            stb.append("  WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.SEMESTER = '"+ param._semester +"' ");
            stb.append("     AND T1.GRADE = T2.GRADE ");
            stb.append("     AND T1.GRADE || T1.HR_CLASS = '" + param._gradeHrclass + "' ");
            stb.append("     AND T1.SCHREGNO IN " + SQLUtils.whereIn(true, param._categorySelected) + " ");
            //                      在籍チェック:転学(2)・退学(3)者は除外 但し異動日が学期終了日または異動基準日より小さい場合
            //                                   転入(4)・編入(5)者は除外 但し異動日が学期終了日または異動基準日より大きい場合 
            stb.append("     AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST S1 ");
            stb.append("                     WHERE   S1.SCHREGNO = T1.SCHREGNO ");
            stb.append("                         AND ((S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < CASE WHEN T2.EDATE < '" + param._date + "' THEN T2.EDATE ELSE '" + param._date + "' END) ");
            stb.append("                           OR (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE > CASE WHEN T2.EDATE < '" + param._date + "' THEN T2.EDATE ELSE '" + param._date + "' END)) ) ");
            //                      異動者チェック：留学(1)・休学(2)者は除外 但し学期終了日または基準日が異動開始日と終了日内にある場合
            stb.append("     AND NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT S1 ");
            stb.append("                     WHERE   S1.SCHREGNO = T1.SCHREGNO ");
            stb.append("                         AND S1.TRANSFERCD IN ('1','2') AND CASE WHEN T2.EDATE < '" + param._date + "' THEN T2.EDATE ELSE '" + param._date + "' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE) ");
            stb.append(" ) ");
            //メイン表
            stb.append(" SELECT ");
            stb.append("    T1.SCHREGNO, ");
            stb.append("    T1.REGDH_HR_NAME, ");
            stb.append("    T7.HR_NAME, ");
            stb.append("    T1.ATTENDNO, ");
            stb.append("    T1.COURSE, ");
            stb.append("    T5.NAME, ");
            stb.append("    T5.REAL_NAME, ");
            stb.append("    CASE WHEN T6.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END AS USE_REAL_NAME ");
            stb.append(" FROM ");
            stb.append("    SCHNO_A T1 ");
            stb.append("    INNER JOIN SCHREG_BASE_MST T5 ON T1.SCHREGNO = T5.SCHREGNO ");
            stb.append("    LEFT JOIN SCHREG_NAME_SETUP_DAT T6 ON T6.SCHREGNO = T1.SCHREGNO AND T6.DIV = '03' ");
            stb.append("    LEFT JOIN SCHREG_REGD_HDAT T7 ON T7.YEAR = '" + param._year + "' AND T7.SEMESTER = T1.SEMESTER AND T7.GRADE = T1.GRADE AND T7.HR_CLASS = T1.HR_CLASS ");
            stb.append(" ORDER BY ATTENDNO");
            return stb.toString();
        }
        
        private static void setShokenMap(final DB2UDB db2, final Param param, final List studentList) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = " SELECT * FROM HREPORTREMARK_DAT WHERE YEAR = '" + param._year + "' AND SEMESTER <= '" + param._semester + "' AND SCHREGNO = ? ";
                //log.debug(" subclass sql = " + sql);
                
                ps = db2.prepareStatement(sql);
                
                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();
                    
                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        student._shokenMap.put(rs.getString("SEMESTER"), rs.getString("COMMUNICATION"));
                    }
                }
                
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
    }
    
    /**
     * 出欠の記録
     */
    private static class AttendSemesDat {
        
        final String _semester;
        final int _lesson;
        final int _suspend;
        final int _mourning;
        final int _mlesson;
        final int _sick;
        final int _absent;
        final int _present;
        final int _late;
        final int _early;
        final int _transferDate;
        final int _offdays;
        
        public AttendSemesDat(
                final String semester,
                final int lesson,
                final int suspend,
                final int mourning,
                final int mlesson,
                final int sick,
                final int absent,
                final int present,
                final int late,
                final int early,
                final int transferDate,
                final int offdays
        ) {
            _semester = semester;
            _lesson = lesson;
            _suspend = suspend;
            _mourning = mourning;
            _mlesson = mlesson;
            _sick = sick;
            _absent = absent;
            _present = present;
            _late = late;
            _early = early;
            _transferDate = transferDate;
            _offdays = offdays;
        }
        
        private static void setAttendSemesDatList(final DB2UDB db2, final Param param, final List studentList) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                param._attendParamMap.put("schregno", "?");

                final String sql = AttendAccumulate.getAttendSemesSql(
                        param._year,
                        param._semester,
                        null,
                        param._date,
                        param._attendParamMap
                );
                ps = db2.prepareStatement(sql);
                
                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();
                    
                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        
                        final String semester = rs.getString("SEMESTER");
                        
                        if (!"9".equals(semester) && !param._semester.equals(param._knjSchoolMst._semesterDiv) && Integer.parseInt(param._semester) < Integer.parseInt(semester)) {
                            continue;
                        }
                        
                        final int lesson = rs.getInt("LESSON");
                        final int suspend = rs.getInt("SUSPEND") + rs.getInt("VIRUS") + rs.getInt("KOUDOME");
                        final int mourning = rs.getInt("MOURNING");
                        final int mlesson = rs.getInt("MLESSON");
                        final int sick = rs.getInt("SICK");
                        final int absent = rs.getInt("ABSENT");
                        final int present = rs.getInt("PRESENT");
                        final int late = rs.getInt("LATE");
                        final int early = rs.getInt("EARLY");
                        final int transferDate = rs.getInt("TRANSFER_DATE");
                        final int offdays = rs.getInt("OFFDAYS");
                        
                        final AttendSemesDat attendSemesDat = new AttendSemesDat(semester, lesson, suspend, mourning, mlesson, sick, absent, present, late, early, transferDate, offdays);
                        student._attendSemesDatList.add(attendSemesDat);
                    }
                    DbUtils.closeQuietly(rs);

                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(rs);
                db2.commit();
            }
        }
        
        public String toString() {
            return "AttendSemesDat(" + _semester + ": [" + _lesson + ", " + _suspend  + ", " + _mourning  + ", " + _mlesson  + ", " + _sick  + ", " + _present + ", " + _late + ", " + _early + "])";
        }
    }
    
    private static class Subclass {
        
        final String _subclasscd;
        final String _classname;
        final String _subclassname;
        final Map _detailMap = new HashMap();
        public Subclass(final String subclasscd, final String classname, final String subclassname) {
            _subclasscd = subclasscd;
            _classname = classname;
            _subclassname = subclassname;
        }
        
        private static Subclass getSubclass(final List list, final String subclasscd) {
            for (final Iterator it = list.iterator(); it.hasNext();) {
                final Subclass ss = (Subclass) it.next();
                if (subclasscd.equals(ss._subclasscd)) {
                    return ss;
                }
            }
            return null;
        }

        public static void setSubclassList(final DB2UDB db2, final Param param, final List studentList) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getSubclassSql(param);
                log.debug(" subclass sql = " + sql);
                
                ps = db2.prepareStatement(sql);
                
                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();
                    
                    ps.setString(1, student._schregno);

                    rs = ps.executeQuery();
                    while (rs.next()) {
                        final String subclasscd = rs.getString("CLASSCD") + "-" + rs.getString("SCHOOL_KIND") + "-" + rs.getString("CURRICULUM_CD") + "-" + rs.getString("SUBCLASSCD");
                        
                        Subclass subclass = getSubclass(student._subclassList, subclasscd);
                        if (null == subclass) {
                            subclass = new Subclass(subclasscd, rs.getString("CLASSNAME"), rs.getString("SUBCLASSNAME"));
                            student._subclassList.add(subclass);
                        }
                        
                        final String semtestcd = rs.getString("SEMTESTCD");
                        final String score = rs.getString("SCORE");
                        if (null == subclass._detailMap.get(semtestcd)) {
                            subclass._detailMap.put(semtestcd, new SubclassDetail());
                        }
                        final SubclassDetail detail = (SubclassDetail) subclass._detailMap.get(semtestcd);
                        detail._score = score;
                    }
                }
                
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
        
        private static String getSubclassSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            
            stb.append(" WITH MAIN AS ( ");
            stb.append(" SELECT DISTINCT ");
            stb.append("        T1.CLASSCD, ");
            stb.append("        T1.SCHOOL_KIND, ");
            stb.append("        T1.CURRICULUM_CD, ");
            stb.append("     T1.SUBCLASSCD, ");
            stb.append("     T2.SEMESTER || T2.TESTKINDCD || T2.TESTITEMCD || T2.SCORE_DIV AS SEMTESTCD, ");
            stb.append("     T2.SCORE ");
            stb.append(" FROM ");
            stb.append("     CHAIR_DAT T1 ");
            stb.append("     INNER JOIN CHAIR_STD_DAT T3 ON T3.YEAR = T1.YEAR AND T3.SEMESTER = T1.SEMESTER AND T3.CHAIRCD = T1.CHAIRCD ");
            stb.append("     LEFT JOIN RECORD_RANK_SDIV_DAT T2 ON T2.YEAR = T1.YEAR AND T2.SEMESTER = T1.SEMESTER ");
            stb.append("        AND T2.CLASSCD = T1.CLASSCD AND T2.SCHOOL_KIND = T1.SCHOOL_KIND AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("        AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("        AND T2.SCHREGNO = T3.SCHREGNO ");
            stb.append("        AND T2.TESTKINDCD = '99' AND T2.TESTITEMCD = '00' AND T2.SCORE_DIV = '08' ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            if (!param._semester.equals(param._knjSchoolMst._semesterDiv)) {
                stb.append("     AND T1.SEMESTER <= '" + param._semester + "' ");
            }
            stb.append("     AND T3.SCHREGNO = ? ");
            stb.append("     AND T1.CLASSCD < '90' ");
            stb.append(" ), ORDER AS ( ");
            stb.append(" SELECT DISTINCT ");
            stb.append("     T1.CLASSCD, ");
            stb.append("     T1.SCHOOL_KIND, ");
            stb.append("     T1.CURRICULUM_CD, ");
            stb.append("     T1.SUBCLASSCD, ");
            stb.append("     T4.CLASSNAME, ");
            stb.append("     VALUE(T3.SUBCLASSORDERNAME2, T3.SUBCLASSNAME) AS SUBCLASSNAME, ");
            stb.append("     VALUE(T4.SHOWORDER3, 99) AS ORDER1, ");
            stb.append("     VALUE(T3.SHOWORDER3, 99) AS ORDER2 ");
            stb.append(" FROM  ");
            stb.append("     MAIN T1 ");
            stb.append(" LEFT JOIN SUBCLASS_MST T3 ON ");
            stb.append(" T3.CLASSCD = T1.CLASSCD AND T3.SCHOOL_KIND = T1.SCHOOL_KIND AND T3.CURRICULUM_CD = T1.CURRICULUM_CD AND ");
            stb.append(" T3.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append(" LEFT JOIN CLASS_MST T4 ON ");
            stb.append(" T4.CLASSCD = T1.CLASSCD AND T4.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append(" ) ");
            stb.append(" SELECT  ");
            stb.append("        T1.SCHOOL_KIND, ");
            stb.append("        T1.CLASSCD, ");
            stb.append("        T1.CURRICULUM_CD, ");
            stb.append("     T1.SUBCLASSCD, ");
            stb.append("     T2.CLASSNAME, ");
            stb.append("     T2.SUBCLASSNAME, ");
            stb.append("     T1.SEMTESTCD, ");
            stb.append("     T1.SCORE, ");
            stb.append("     VALUE(T2.ORDER1, 99) AS ORDER1, ");
            stb.append("     VALUE(T2.ORDER2, 99) AS ORDER2 ");
            stb.append(" FROM  ");
            stb.append("     MAIN T1 ");
            stb.append(" LEFT JOIN ORDER T2 ON ");
            stb.append("     T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD = ");
            stb.append("     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD ");
            stb.append(" WHERE ");
            if ("1".equals(param._useClassDetailDat)) {
                stb.append("     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD NOT IN ( ");
                stb.append("         SELECT ");
                stb.append("             T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD ");
                stb.append("         FROM ");
                stb.append("             SUBCLASS_DETAIL_DAT T1 ");
                stb.append("         WHERE ");
                stb.append("             YEAR = '" + param._year + "' ");
                stb.append("             AND SUBCLASS_SEQ = '007' ");
                if ("1".equals(param._semester)) {
                    stb.append("         AND SUBCLASS_REMARK1 = '1' ");
                } else if ("2".equals(param._semester)) {
                    stb.append("         AND SUBCLASS_REMARK2 = '1' ");
                } else if ("3".equals(param._semester)) {
                    stb.append("         AND SUBCLASS_REMARK3 = '1' ");
                } else if ("9".equals(param._semester)) {
                    stb.append("         AND SUBCLASS_REMARK4 = '1' ");
                }
                stb.append("         ) ");
            } else {
                stb.append("     T1.SUBCLASSCD NOT IN ( ");
                stb.append("         SELECT ");
                stb.append("             NAME1 ");
                stb.append("         FROM ");
                stb.append("             NAME_MST ");
                stb.append("         WHERE ");
                stb.append("             NAMECD1 = 'D026' ");
                if ("1".equals(param._semester)) {
                    stb.append("         AND ABBV1 = '1' ");
                } else if ("2".equals(param._semester)) {
                    stb.append("         AND ABBV2 = '1' ");
                } else if ("3".equals(param._semester)) {
                    stb.append("         AND ABBV3 = '1' ");
                } else if ("9".equals(param._semester)) {
                    stb.append("         AND NAMESPARE1 = '1' ");
                }
                stb.append("         ) ");
            }
            stb.append(" ORDER BY ");
            stb.append("     VALUE(T2.ORDER1, 99), T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, VALUE(T2.ORDER2, 99) ");
            stb.append("     , T1.SUBCLASSCD, T1.SEMTESTCD ");
            return stb.toString();
        }
        
        public String toString() {
            return "ScoreSubclass(" + _subclasscd + ":" + _subclassname + ":" + _detailMap + ")";
        }
    }
    
    private static class SubclassDetail {
        String _score;
    }
    
    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 59839 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _year;
        final String _semester;
        final String _date;
        final String _grade;
        final String _gradeHrclass;
        final String[] _categorySelected;
        String _trCd1;
        String _subtrCd1;
//        final String _printRegd;
        final boolean _isSeireki;
//        final String _documentRoot;
//        final String _imagePath;
//        final String _extension;
//        final String _descDate;
        
        final String _certifSchoolSchoolName;
//        final String _certifSchoolRemark3;
//        final String _certifSchoolPrincipalName;
        final String _tr1Name;
        final String _subtr1Name;
//        final String _certifSchoolJobName;
        final Map _semesternameMap;
        
        /** 教育課程コードを使用するか */
        final String _useClassDetailDat;
        final String _useAssessCourseMst;

       /** 各学校における定数等設定 */
        KNJSchoolMst _knjSchoolMst;
        final Map _d026Map;
        final Map _attendParamMap;
        
        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("CTRL_YEAR");
            _isSeireki = "2".equals(KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z012' AND NAMECD2 = '00' ")));
            _semester = request.getParameter("SEMESTER");
            _date = request.getParameter("DATE").replace('/', '-');
            _gradeHrclass = request.getParameter("GRADE_HR_CLASS");
            _grade = _gradeHrclass.substring(0, 2);
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
//            _printRegd = request.getParameter("PRINT_REGD");
            setTrCd(db2);
//            _documentRoot = request.getParameter("DOCUMENTROOT");
//            
            _certifSchoolSchoolName = getCertifSchoolDat(db2, "SCHOOL_NAME");
//            _certifSchoolRemark3 = getCertifSchoolDat(db2, "REMARK3");
//            _certifSchoolPrincipalName = getCertifSchoolDat(db2, "PRINCIPAL_NAME");
            _tr1Name = getStaffname(db2, _trCd1);
            _subtr1Name = getStaffname(db2, _subtrCd1);
            
            try {
                _knjSchoolMst = new KNJSchoolMst(db2, _year);
            } catch (SQLException e) {
                log.warn("学校マスタ取得でエラー", e);
            }
            
            _d026Map = getD026Map(db2);

            // 学期名称 _arrsemesName をセットします。
            _useClassDetailDat = request.getParameter("useClassDetailDat");
            _useAssessCourseMst = request.getParameter("useAssessCourseMst");
            _semesternameMap = getSemesterName(db2);
            
            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
        }
        
        /**
         * 写真データ格納フォルダの取得 --NO001
         */
        private KNJ_Control.ReturnVal getDocumentroot(final DB2UDB db2) {
            KNJ_Control.ReturnVal returnval = null;
            try {
                KNJ_Control imagepath_extension = new KNJ_Control(); // 取得クラスのインスタンス作成
                returnval = imagepath_extension.Control(db2);
            } catch (Exception ex) {
                log.error("getDocumentroot error!", ex);
            }
            return returnval;
        }
        
        private String getStaffname(final DB2UDB db2, final String staffcd) {
            String rtn = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = " SELECT STAFFNAME FROM STAFF_MST WHERE STAFFCD = '" + staffcd + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    if (null != rs.getString("STAFFNAME")) {
                        rtn = rs.getString("STAFFNAME");
                    }
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }

        /**
         * 学期マスタ (SEMESTER_MST) をロードする
         * @param db2
         */
        private Map getSemesterName(DB2UDB db2) {
            final String sql = "SELECT SEMESTER, SEMESTERNAME FROM V_SEMESTER_GRADE_MST WHERE YEAR = '" + _year + "' AND GRADE = '" + _grade + "' ";
            //log.debug(" semester sql = " + sql);
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map semesterNameMap = new HashMap();
            try {
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    semesterNameMap.put(rs.getString("SEMESTER"), rs.getString("SEMESTERNAME"));
                }
                
            } catch (Exception ex) {
                log.error("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return semesterNameMap;
        }
        
        private void setTrCd(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            _trCd1 = null;
            _subtrCd1 = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     * ");
                stb.append(" FROM ");
                stb.append("     SCHREG_REGD_HDAT T1 ");
                stb.append(" WHERE ");
                stb.append("     T1.YEAR = '" + _year + "' ");
                stb.append("     AND T1.SEMESTER = '" + _semester + "' ");
                stb.append("     AND T1.GRADE || T1.HR_CLASS = '" + _gradeHrclass + "' ");
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    _trCd1 = rs.getString("TR_CD1");
                    _subtrCd1 = rs.getString("SUBTR_CD1");
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
        
        private List getTestcdList() {
            final String[] testcdAll = {_1990008, _2990008};
            final List testcdList = new ArrayList();
            for (int i = 0; i < testcdAll.length; i++) {
                if (_semester.equals(_knjSchoolMst._semesterDiv) || Integer.parseInt(testcdAll[i].substring(0, 1)) <= Integer.parseInt(_semester)) {
                    testcdList.add(testcdAll[i]);
                }
            }
            return testcdList;
        }
        
        private Map getD026Map(final DB2UDB db2) {
            final Map d026Map = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append("   SELECT ");
                stb.append("     T1.NAME1, ");
                stb.append("     T1.ABBV1, ");
                stb.append("     T1.NAMESPARE1 ");
                stb.append("   FROM ");
                stb.append("       V_NAME_MST T1 ");
                stb.append("   WHERE ");
                stb.append("       T1.YEAR = '" + _year + "' ");
                stb.append("       AND T1.NAMECD1 = 'D026' ");
                stb.append("       AND T1.NAME1 IS NOT NULL ");
                
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final Map m = getMappedMap(d026Map, rs.getString("NAME1"));
                    m.put("ABBV1", rs.getString("ABBV1"));
                    m.put("NAMESPARE1", rs.getString("NAMESPARE1"));
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return d026Map;
        }
        
        private String getCertifSchoolDat(final DB2UDB db2, final String field) {
            String rtn = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = " SELECT " + field + " FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _year + "' AND CERTIF_KINDCD = '104' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    if (null != rs.getString(field)) {
                        rtn = rs.getString(field);
                    }
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }
    }
}

// eof

