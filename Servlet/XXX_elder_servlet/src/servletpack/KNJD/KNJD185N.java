// kanji=漢字
/*
 * $Id: 670c5ec32e3161c14cfeb81a234d550a3963f953 $
 *
 * 作成日: 2009/08/20
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJD;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJServletpacksvfANDdb2;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

/**
 * 学校教育システム 賢者 [成績管理]  成績通知票
 */

public class KNJD185N {

    private static final Log log = LogFactory.getLog(KNJD185N.class);

    private static final String SUBCLASS_ALL = "999999";
    
    private Param _param;

    /**
     *  KNJD.classから最初に起動されるクラス。
     */
    public void svf_out (
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws ServletException, IOException {

        final KNJServletpacksvfANDdb2 sd = new KNJServletpacksvfANDdb2();
        Vrw32alp svf = new Vrw32alp();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;              // Databaseクラスを継承したクラス
        boolean hasData = false;

        sd.setSvfInit(request, response, svf);
        db2 = sd.setDb(request);
        if (sd.openDb(db2)) {
            log.error("db open error! ");
            return;
        }
        
        _param = createParam(request, db2);
        
        final Form form = new Form();
        
        try {
            final List students = createStudents(db2);
            
            for (final Iterator it = students.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                
                form.print(svf, student);
                
                hasData = true;
            }
            
        } catch (Exception e) {
            log.error("Exception", e);
        } finally {
            db2.commit();
        }
        
        sd.closeSvf(svf, hasData);
        sd.closeDb(db2);
    }

    private static List getMappedList(final Map m, final Object key) {
        if (null == m.get(key)) {
            m.put(key, new ArrayList());
        }
        return (List) m.get(key);
    }

    /**
     * 表示するデータをセットした生徒のリストを得る
     * @param db2
     * @param _param
     * @return
     */
    private List createStudents(final DB2UDB db2) {
        
        final List students = Student.getStudents(db2, _param);
        
        if (students.size() == 0) {
            log.warn("対象の生徒がいません");
            return students;
        }
        
        Student.setAttendData(db2, students, _param);
        for (final Iterator it = students.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            
            Student.setScoreValue(db2, student, _param);
            AbsenceHigh.setAbsenceHigh(db2, student, _param);
            student.setRecDetail(db2, _param);
        }
        Student.setRemark(db2, students, _param);
        
        return students;
    }

    private static int getMS932ByteLength(final String s) {
        int length = 0;
        if (null != s) {
            try {
                length = s.getBytes("MS932").length;
            } catch (Exception e) {
                log.error("exception!", e);
            }
        }
        return length;
    }
    
    private static class Student {
        
        final String _schregno;
        private String _name;
        private String _attendNo;
        private String _grade;
        private String _hrClass;
        private String _courseCd;
        private String _courseName;
        private String _majorCd;
        private String _majorName;
        private String _courseCode;
        private String _courseCodeName;
        private String _hrName;
        private String _hrNameAbbv;
        
        final List _subclassInfos = new ArrayList();
        
        int _totalCredit = 0;
        
        final Map _subclassMap = new HashMap(); 
        final Map _attendMap = new TreeMap();
        final Map _scoreRankMap = new HashMap(); 
        final Map _subclassAbsenceHigh = new HashMap(); 
        final Map _spSubclassAbsenceHigh = new HashMap(); 
        final Map _remarkMap = new HashMap();
        
//        int _previousScredits;
        
        /** 総合的な学習の時間の科目が単位マスタに登録されているか */
        // public boolean _hasSogotekinaGakusyunoJikan;
        
        public Student(final String schregno) {
            _schregno = schregno;
        }
        
        /**
         * 
         * @param subclassCd
         * @return
         */
        private Subclass findSubclass(final String subclassCd) {
            if (null == (Subclass) _subclassMap.get(subclassCd)) {
                _subclassMap.put(subclassCd, new Subclass(_schregno, subclassCd));
            }
            return (Subclass) _subclassMap.get(subclassCd);
        }
        
        /**
         * 指定のいずれかの科目コードのレコードがあるか
         * @param subclassCd 科目コード
         * @return 指定のいずれかの科目コードのレコードがあるならtrue
         */
        public boolean hasSubclassInfo(final String[] subclassCds) {
            for (int i = 0; i < subclassCds.length; i++) {
                if (null != getSubclassInfo(subclassCds[i])) {
                    return true;
                }
            }
            return false;
        }
        
        /**
         * 指定の科目コードのレコードがあるか
         * @param subclassCd 科目コード
         * @return 指定の科目コードのレコードがあるならtrue
         */
        public SubclassInfo getSubclassInfo(final String subclassCd) {
            if (null == subclassCd) {
                return null;
            }
            for (final Iterator it = _subclassInfos.iterator(); it.hasNext();) {
                final SubclassInfo info = (SubclassInfo) it.next();
                if (subclassCd.equals(info._subclassCd)) {
                    return info;
                }
            }
            return null;
        }
        
        public String toString() {
            return _schregno + ":" + _name;
        }
        
        // -----
        
        public static List getStudents(final DB2UDB db2, final Param param) {
            PreparedStatement ps = null;
            ResultSet rs1 = null;
            List students = new ArrayList();
            try {
                final String sqlRegdData = sqlRegdData(param);
                log.debug("sqlRegdData = " + sqlRegdData);
                ps = db2.prepareStatement(sqlRegdData);
                rs1 = ps.executeQuery();
                
                while( rs1.next() ){
                    
                    Student student = new Student(rs1.getString("SCHREGNO"));
                    students.add(student);
                    
                    student._grade = rs1.getString("GRADE");
                    student._hrClass = rs1.getString("HR_CLASS");
                    student._courseCd = rs1.getString("COURSECD");
                    student._majorCd = rs1.getString("MAJORCD");
                    student._courseCode = rs1.getString("COURSECODE");
                    
                    student._courseName = rs1.getString("COURSENAME");
                    student._majorName = rs1.getString("MAJORNAME");
                    student._courseCodeName = StringUtils.defaultString(rs1.getString("COURSECODENAME"), "");
                    student._hrName = StringUtils.defaultString(rs1.getString("HR_NAME"), "");
                    student._hrNameAbbv = StringUtils.defaultString(rs1.getString("HR_NAMEABBV"), "");
                    student._attendNo = null == rs1.getString("ATTENDNO") || !NumberUtils.isDigits(rs1.getString("ATTENDNO"))? "" : Integer.parseInt(rs1.getString("ATTENDNO")) + "番";
                    final String name = StringUtils.defaultString(rs1.getString("NAME"), "");
                    final String realName = StringUtils.defaultString(rs1.getString("REAL_NAME"), "");
                    student._name = "1".equals(rs1.getString("USE_REAL_NAME")) ? realName : name;
                    
                    //log.debug("対象の生徒" + student);
                }
            } catch( Exception ex ) { 
                log.error("printSvfMain read error! ", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs1);
                db2.commit();
            }
            return students;
        }
        
        private static String sqlRegdData(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append("WITH SCHNO_A AS(");
            stb.append(    "SELECT  T1.SCHREGNO, T1.GRADE, T1.HR_CLASS, T1.ATTENDNO, T1.SEMESTER, T1.COURSECD, T1.MAJORCD, T1.COURSECODE ");
            stb.append(    "FROM    SCHREG_REGD_DAT T1, V_SEMESTER_GRADE_MST T2 ");
            stb.append(    "WHERE   T1.YEAR = '" + param._year + "' ");
            stb.append(        "AND T1.SEMESTER = '"+ param.getRegdSemester() +"' ");
            stb.append(        "AND T1.YEAR = T2.YEAR ");
            stb.append(        "AND T1.SEMESTER = T2.SEMESTER ");
            stb.append(        "AND T1.GRADE = T2.GRADE ");
            stb.append(        "AND T1.GRADE||T1.HR_CLASS = '" + param._grade_hr_class + "' ");
            stb.append(        "AND T1.SCHREGNO IN " + SQLUtils.whereIn(true, param._categorySelected) + " ");
            stb.append(        "AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST S1 ");
            stb.append(           "WHERE   S1.SCHREGNO = T1.SCHREGNO ");
            stb.append(               "AND ((S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < CASE WHEN T2.EDATE < '" + param._edate + "' THEN T2.EDATE ELSE '" + param._edate + "' END) ");
            stb.append(               "OR (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE > CASE WHEN T2.EDATE < '" + param._edate + "' THEN T2.EDATE ELSE '" + param._edate + "' END)) ) ");
            stb.append(        "AND NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT S1 ");
            stb.append(           "WHERE   S1.SCHREGNO = T1.SCHREGNO ");
            stb.append(              "AND S1.TRANSFERCD IN ('1','2') AND CASE WHEN T2.EDATE < '" + param._edate + "' THEN T2.EDATE ELSE '" + param._edate + "' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE) ");
            stb.append(    ") ");
            
            stb.append("SELECT  T1.SCHREGNO, T1.ATTENDNO, T2.HR_NAME, T2.HR_NAMEABBV, ");
            stb.append(        "T5.NAME, T5.REAL_NAME, CASE WHEN T7.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END AS USE_REAL_NAME, ");
            stb.append(        "T3.COURSENAME, T4.MAJORNAME, T6.COURSECODENAME, ");
            stb.append(        "T1.GRADE, T1.HR_CLASS, T1.COURSECD, T1.MAJORCD, T1.COURSECODE ");
            stb.append("FROM    SCHNO_A T1 ");
            stb.append(        "INNER JOIN SCHREG_BASE_MST T5 ON T1.SCHREGNO = T5.SCHREGNO ");
            stb.append(        "INNER JOIN SCHREG_REGD_HDAT T2 ON T2.YEAR = '" + param._year + "' AND ");
            stb.append(                                          "T2.SEMESTER = T1.SEMESTER AND ");
            stb.append(                                          "T2.GRADE || T2.HR_CLASS = '" + param._grade_hr_class + "' ");
            stb.append(        "LEFT JOIN COURSE_MST T3 ON T3.COURSECD = T1.COURSECD ");
            stb.append(        "LEFT JOIN MAJOR_MST T4 ON T4.COURSECD = T1.COURSECD AND T4.MAJORCD = T1.MAJORCD ");
            stb.append(        "LEFT JOIN COURSECODE_MST T6 ON T6.COURSECODE = T1.COURSECODE ");
            stb.append(        "LEFT JOIN SCHREG_NAME_SETUP_DAT T7 ON T7.SCHREGNO = T1.SCHREGNO AND T7.DIV = '03' ");
            stb.append("ORDER BY ATTENDNO");
            return stb.toString();
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

        public String getKekkaJisu(final Param param, final String semester) {
            BigDecimal rtn = new BigDecimal(0);
            for (final Iterator it = _subclassMap.values().iterator(); it.hasNext();) {
                final Subclass subclass = (Subclass) it.next();
                
                final SubclassInfo info = getSubclassInfo(subclass._subclassCd);
                if (null == info || info._replaceflg == SubclassInfo.GAPPEI_MOTO) {
                    // 合併元は対象外
                    continue;
                }
                
                BigDecimal sick = getSick(param, subclass, semester, info);
                if (null != sick) {
                    rtn = rtn.add(sick);
                }
            }
            return rtn.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
        }
        
        private BigDecimal getSick(final Param param, final Subclass subclass, final String semester, final SubclassInfo info) {
            BigDecimal sick = null;
            final SubclassAttendance sa = (SubclassAttendance) subclass._attendSubclassMap.get(semester);
            if (null == sa) {
                return null;
            }
            if (info._replaceflg == SubclassInfo.GAPPEI_SAKI) {
                if (getMappedList(param._courseNotPrintSubclasscdListMap, _grade + _courseCd + _majorCd + _courseCode).size() > 0) {
                    // 元科目の単位マスタがある
                    sick = sa._replacedSick;
                } else {
                    // 元科目の単位マスタがない
                    sick = sa._sick;
                }
            } else {
                sick = sa._sick;
            }
            return sick;
        }
        
        /**
         * 通知書所見
         */
        public static void setRemark(final DB2UDB db2, final List students, final Param param) {
            PreparedStatement ps = null;
            
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     REMARK ");
            stb.append(" FROM ");
            stb.append("     HTESTREMARK_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + param._year + "' ");
            stb.append("     AND SCHREGNO = ? ");
            stb.append("     AND SEMESTER || TESTKINDCD || TESTITEMCD = '" + param._shokenSemTestCd + "' ");
            stb.append("     AND CLASSCD = '00' ");
            stb.append("     AND SCHOOL_KIND = '00' ");
            stb.append("     AND CURRICULUM_CD = '00' ");
            stb.append("     AND SUBCLASSCD = '000000' ");
            stb.append("     AND DIV = '1' ");
            
            try {
                ps = db2.prepareStatement(stb.toString());
                
                for (final Iterator it = students.iterator(); it.hasNext();) {
                    final Student student= (Student) it.next();
                    ps.setString(1, student._schregno);
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        student._remarkMap.put(param._shokenSemTestCd, rs.getString("REMARK"));
                    }
                    DbUtils.closeQuietly(rs);
                }
                
            } catch (Exception e) {
                log.error("exception! sql = " + stb.toString(), e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }
        
        private void setRecDetail (
                final DB2UDB db2,
                final Param param
        ) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Student student = this;
            student._subclassInfos.clear();
            try {
                final String sql = sqlSubclass(param);
                // log.debug(" subclass sql = " + prestatementSubclass);
                ps = db2.prepareStatement(sql);
                
                int pp = 0;
                ps.setString(++pp, student._schregno);
                ps.setString(++pp, student._schregno);
                ps.setString(++pp, student._schregno);
                ps.setString(++pp, student._schregno);
                rs = ps.executeQuery();
                
                while (rs.next()) {
                    
                    final String classabbv = rs.getString("CLASSABBV");
                    final String subclassname = rs.getString("SUBCLASSNAME");
                    final String credits = rs.getString("CREDITS");
                    final String compCredit = rs.getString("COMP_CREDIT");
                    final String getCredit = rs.getString("GET_CREDIT");
                    final String subclassCd = rs.getString("SUBCLASSCD");
                    final String attendSubclasscd = rs.getString("ATTEND_SUBCLASSCD");
                    final String attendSubclasscd2 = rs.getString("ATTEND_SUBCLASSCD_2");
                    final String attendSubclasscd3 = rs.getString("ATTEND_SUBCLASSCD_3");
                    
                    final String combinedSubclasscd = rs.getString("COMBINED_SUBCLASSCD");

                    final String namespare1 = rs.getString("NAMESPARE1");
                    
                    final int replaceflg = rs.getInt("REPLACEFLG");
                    final String calculateCreditFlg = StringUtils.defaultString(rs.getString("CALCULATE_CREDIT_FLG"), "0");
                    
                    final Subclass subclass = (Subclass) student._subclassMap.get(subclassCd);
                    
                    final SubclassInfo info = new SubclassInfo(classabbv, subclassname, credits, compCredit, getCredit, subclassCd, 
                            new String[] { attendSubclasscd, attendSubclasscd2, attendSubclasscd3}, combinedSubclasscd, namespare1, subclass, replaceflg, calculateCreditFlg);
                    student._subclassInfos.add(info);
                }
            } catch (SQLException e) {
                log.error("SQLException", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            
            int totalCredit = 0;
            for (final Iterator it = student._subclassInfos.iterator(); it.hasNext();) {
                final SubclassInfo info = (SubclassInfo) it.next();
                if (null == info._getCredit
                        || (SubclassInfo.GAPPEI_MOTO == info._replaceflg && student.hasSubclassInfo(new String[] {info._combinedSubclasscd}))
                        || (2 == info._replaceflg && (student.hasSubclassInfo(info._attendSubclasscds) || student.hasSubclassInfo(new String[] {info._combinedSubclasscd})))) {
                    // 加算しない。
                } else {
                    totalCredit += Integer.parseInt(info._getCredit);
                }
            }
            student._totalCredit = totalCredit;
        }
        
        private static String sqlSubclass(final Param param) {
            
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH ");
            stb.append(" SUBCLASS_CREDITS AS(");
            stb.append("   SELECT ");
            stb.append("   CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || ");
            stb.append("          SUBCLASSCD AS SUBCLASSCD, CREDITS, L1.NAMESPARE1 ");
            stb.append("   FROM   CREDIT_MST T1");
            stb.append("          LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'Z011' ");
            stb.append("               AND L1.NAMECD2 = T1.REQUIRE_FLG ");
            stb.append("        , (SELECT  T3.GRADE, T3.COURSECD, T3.MAJORCD, T3.COURSECODE");
            stb.append("           FROM    SCHREG_REGD_DAT T3");
            stb.append("           WHERE   T3.SCHREGNO = ?");
            stb.append("               AND T3.YEAR = '" + param._year + "'");
            stb.append("               AND T3.GRADE || T3.HR_CLASS = '" + param._grade_hr_class + "'");
            stb.append("               AND T3.SEMESTER = (SELECT  MAX(SEMESTER)");
            stb.append("                                  FROM    SCHREG_REGD_DAT T4");
            stb.append("                                  WHERE   T4.YEAR = '" + param._year + "'");
            stb.append("                                      AND T4.SEMESTER <= '" + param.getRegdSemester() + "'");
            stb.append("                                      AND T4.SCHREGNO = T3.SCHREGNO)");
            stb.append("          )T2 ");
            stb.append("   WHERE T1.YEAR = '" + param._year + "'");
            stb.append("     AND T1.GRADE = T2.GRADE");
            stb.append("     AND T1.COURSECD = T2.COURSECD");
            stb.append("     AND T1.MAJORCD = T2.MAJORCD");
            stb.append("     AND T1.COURSECODE = T2.COURSECODE");
            stb.append(" ) ");
            
            stb.append(" ,COMBINED_SUBCLASSCD AS(");
            stb.append("   SELECT ");
            stb.append("   COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || ");
            stb.append("          COMBINED_SUBCLASSCD AS COMBINED_SUBCLASSCD, CALCULATE_CREDIT_FLG, ");
            stb.append("   ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ");
            stb.append("          ATTEND_SUBCLASSCD AS ATTEND_SUBCLASSCD, ");
            stb.append("          ROW_NUMBER() OVER(ORDER BY  ");
            stb.append("   ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ");
            stb.append("          ATTEND_SUBCLASSCD) AS ORDER ");
            stb.append("   FROM   SUBCLASS_REPLACE_COMBINED_DAT");
            stb.append("   WHERE  YEAR = '" + param._year + "' ");
            stb.append(" )");
            
            stb.append(" ,ATTEND_SUBCLASSCD AS(");
            stb.append("   SELECT ");
            stb.append("   ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ");
            stb.append("          ATTEND_SUBCLASSCD AS ATTEND_SUBCLASSCD, MAX(PRINT_FLG1) AS PRINT_FLG, MAX(CALCULATE_CREDIT_FLG) AS CALCULATE_CREDIT_FLG, MAX(");
            stb.append("   COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || ");
            stb.append("          COMBINED_SUBCLASSCD) AS COMBINED_SUBCLASSCD");
            stb.append("   FROM   SUBCLASS_REPLACE_COMBINED_DAT");
            stb.append("   WHERE  YEAR = '" + param._year + "' ");
            stb.append("   GROUP BY ");
            stb.append("   ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ");
            stb.append("          ATTEND_SUBCLASSCD");
            stb.append(" )");

            stb.append(" ,REGD AS(");
            stb.append("   SELECT ");
            stb.append("      SCHREGNO, GRADE, COURSECD, MAJORCD, COURSECODE ");
            stb.append("   FROM   SCHREG_REGD_DAT ");
            stb.append("   WHERE  YEAR = '" + param._year + "' ");
            stb.append("      AND SEMESTER = '" + param.getRegdSemester() + "' ");
            stb.append("      AND SCHREGNO = ? ");
            stb.append(" )");

            stb.append(", CHAIR_A AS(");
            stb.append("   SELECT  ");
            stb.append("   T1.SCHREGNO, T2.CLASSCD, T2.SCHOOL_KIND, T2.CURRICULUM_CD, ");
            stb.append("   T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || ");
            stb.append("            T2.SUBCLASSCD AS SUBCLASSCD");
            stb.append("   FROM    CHAIR_STD_DAT T1, CHAIR_DAT T2");
            stb.append("   WHERE   T1.SCHREGNO = ?");
            stb.append("       AND T1.YEAR = '" + param._year + "'");
            stb.append("       AND T1.SEMESTER <= '" + param.getRegdSemester() + "'");
            stb.append("       AND T2.YEAR  = '" + param._year + "'");
            stb.append("       AND T2.SEMESTER = T1.SEMESTER ");
            stb.append("       AND T2.SEMESTER = T1.SEMESTER");
            stb.append("       AND T2.CHAIRCD = T1.CHAIRCD");
            stb.append("       AND (CLASSCD BETWEEN '" + KNJDefineSchool.subject_D + "' AND '" + KNJDefineSchool.subject_U + "' OR CLASSCD = '" + KNJDefineSchool.subject_T + "' OR CLASSCD = '94')");
            stb.append("   GROUP BY ");
            stb.append("   T1.SCHREGNO, T2.CLASSCD, T2.SCHOOL_KIND, T2.CURRICULUM_CD, ");
            stb.append("   T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || ");
            stb.append("             T2.SUBCLASSCD");
            stb.append(" )");

            stb.append(", CHAIR_A2 AS(");
            stb.append("   SELECT * ");
            stb.append("   FROM    CHAIR_A T1 ");
            stb.append("   UNION ");
            stb.append("   SELECT  ");
            stb.append("   T1.SCHREGNO, T3.COMBINED_CLASSCD AS CLASSCD, T3.COMBINED_SCHOOL_KIND AS SCHOOL_KIND, T3.COMBINED_CURRICULUM_CD AS CURRICULUM_CD, ");
            stb.append("   T3.COMBINED_CLASSCD || '-' || T3.COMBINED_SCHOOL_KIND || '-' || T3.COMBINED_CURRICULUM_CD || '-' || ");
            stb.append("            T3.COMBINED_SUBCLASSCD AS SUBCLASSCD");
            stb.append("   FROM    CHAIR_A T1 ");
            stb.append("           INNER JOIN REGD T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("           INNER JOIN SUBCLASS_WEIGHTING_COURSE_DAT T3 ON T3.YEAR = '" + param._year + "'");
            if (param._testCd.startsWith("99")) {
                stb.append("               AND T3.FLG = '2' ");
            } else {
                stb.append("               AND T3.FLG = '1' ");
            }
            stb.append("               AND T3.GRADE = T2.GRADE ");
            stb.append("               AND T3.COURSECD = T2.COURSECD ");
            stb.append("               AND T3.MAJORCD = T2.MAJORCD ");
            stb.append("               AND T3.COURSECODE = T2.COURSECODE ");
            stb.append("               AND T3.ATTEND_CLASSCD = T1.CLASSCD ");
            stb.append("               AND T3.ATTEND_SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("               AND T3.ATTEND_CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("               AND T3.ATTEND_SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append(" )");
            
            stb.append(" SELECT  T2.SUBCLASSCD, T7.CLASSABBV, VALUE(T4.SUBCLASSORDERNAME2,T4.SUBCLASSNAME) AS SUBCLASSNAME");
            stb.append("       , T6.CREDITS, T6.NAMESPARE1 ");
            stb.append("       , CASE WHEN T5.COMBINED_SUBCLASSCD IS NOT NULL AND T9.ATTEND_SUBCLASSCD IS NOT NULL THEN 2"); // 元科目かつ先科目
            stb.append("              WHEN T5.COMBINED_SUBCLASSCD IS NOT NULL THEN 9"); // 先科目
            stb.append("              WHEN T9.ATTEND_SUBCLASSCD IS NOT NULL THEN 1"); // 元科目
            stb.append("              ELSE 0 END AS REPLACEFLG");
            stb.append("       , T5.ATTEND_SUBCLASSCD ");
            stb.append("       , T5_2.ATTEND_SUBCLASSCD AS ATTEND_SUBCLASSCD_2 ");
            stb.append("       , T5_3.ATTEND_SUBCLASSCD AS ATTEND_SUBCLASSCD_3  ");
            stb.append("       , T9.COMBINED_SUBCLASSCD ");
            stb.append("       , T9.PRINT_FLG");
            stb.append("       , CASE WHEN T5.CALCULATE_CREDIT_FLG IS NOT NULL THEN T5.CALCULATE_CREDIT_FLG");
            stb.append("              WHEN T9.CALCULATE_CREDIT_FLG IS NOT NULL THEN T9.CALCULATE_CREDIT_FLG");
            stb.append("              ELSE NULL END AS CALCULATE_CREDIT_FLG");
            stb.append("       , REC_SCORE.COMP_CREDIT ");
            stb.append("       , REC_SCORE.GET_CREDIT AS GET_CREDIT ");
            stb.append("       , REC_SCORE.ADD_CREDIT ");
            stb.append(" FROM    CHAIR_A2 T2");
            stb.append(" LEFT JOIN SUBCLASS_MST T4 ON ");
            stb.append("    T4.CLASSCD || '-' || T4.SCHOOL_KIND || '-' || T4.CURRICULUM_CD || '-' || ");
            stb.append("   T4.SUBCLASSCD = T2.SUBCLASSCD");
            stb.append(" LEFT JOIN CLASS_MST T7 ON ");
            stb.append("   T7.CLASSCD || '-' || T7.SCHOOL_KIND = ");
            stb.append("   T2.CLASSCD || '-' || T2.SCHOOL_KIND ");
            stb.append(" LEFT JOIN SUBCLASS_CREDITS T6 ON T6.SUBCLASSCD = T2.SUBCLASSCD");
            stb.append(" LEFT JOIN COMBINED_SUBCLASSCD T5 ON T5.COMBINED_SUBCLASSCD = T2.SUBCLASSCD AND T5.ORDER = 1 ");
            stb.append(" LEFT JOIN COMBINED_SUBCLASSCD T5_2 ON T5_2.COMBINED_SUBCLASSCD = T2.SUBCLASSCD AND T5_2.ORDER = 2 ");
            stb.append(" LEFT JOIN COMBINED_SUBCLASSCD T5_3 ON T5_3.COMBINED_SUBCLASSCD = T2.SUBCLASSCD AND T5_3.ORDER = 3 ");
            stb.append(" LEFT JOIN ATTEND_SUBCLASSCD T9 ON T9.ATTEND_SUBCLASSCD = T2.SUBCLASSCD");
            stb.append(" LEFT JOIN RECORD_SCORE_DAT REC_SCORE ON REC_SCORE.YEAR = '" + param._year + "' ");
            stb.append("       AND REC_SCORE.SCHREGNO = ? AND ");
            stb.append("    REC_SCORE.CLASSCD || '-' || REC_SCORE.SCHOOL_KIND || '-' || REC_SCORE.CURRICULUM_CD || '-' || ");
            stb.append("       REC_SCORE.SUBCLASSCD = T2.SUBCLASSCD ");
            stb.append("       AND REC_SCORE.SEMESTER || REC_SCORE.TESTKINDCD || REC_SCORE.TESTITEMCD = '" + Param._99900 + "' ");
            stb.append("       AND REC_SCORE.SCORE_DIV IN ('00', '01') ");
            stb.append(" ORDER BY T2.SUBCLASSCD ");
            return stb.toString();
        }
        
        /**
         * 各生徒に１日ごと、科目ごとの出欠データをセットする
         * @param db2
         * @param students
         * @param param
         */
        private static void setAttendData(final DB2UDB db2, final List students, final Param param) {
            
            PreparedStatement ps = null;
            ResultSet rs = null;
            
            final String[] targetSemesters = new String[]{"1", "2", "3", Param.SEMEALL};
            for (int i = 0; i < targetSemesters.length; i++) {
                try {
                    final String semester = targetSemesters[i];
                    // log.debug(" semester = " + semester + " , " + param._semesterMap.get(semester));
                    final Semester semesS = (Semester) param._semesterMap.get(semester);
                    if (null == semesS) {
                        log.debug(" 対象学期がありません。:" + semester);
                        continue;
                    } else {
                        log.debug(" 対象学期:" + semester);
                    }
                    final String sdate = Param.SEMEALL.equals(semester) ? param._sdate : semesS._sdate;
                    final String edate = param._edate.compareTo(semesS._edate) < 0 ? param._edate : semesS._edate;
                    try {
                        param._attendParamMap.put("grade", param._grade_hr_class.substring(0, 2));
                        param._attendParamMap.put("hrClass", param._grade_hr_class.substring(2, 5));
                        String sql;
                        sql = AttendAccumulate.getAttendSemesSql(
                                param._year,
                                semesS._cd,
                                sdate,
                                edate,
                                param._attendParamMap
                        );
                        
                        //log.debug(" attend semes sql = " + sql);
                        ps = db2.prepareStatement(sql);
                        rs = ps.executeQuery();
                        while (rs.next()) {
                            final Student student = Student.getStudent(rs.getString("SCHREGNO"), students);
                            if (student == null || !"9".equals(rs.getString("SEMESTER"))) {
                                continue;
                            }
                            final int lesson = rs.getInt("LESSON");
                            final int mourning = rs.getInt("MOURNING");
                            final int suspend = rs.getInt("SUSPEND");
                            final int abroad = rs.getInt("TRANSFER_DATE");
                            final int mlesson = rs.getInt("MLESSON");
                            final int absence = rs.getInt("SICK");
                            final int attend = rs.getInt("PRESENT");
                            final int late = rs.getInt("LATE");
                            final int early = rs.getInt("EARLY");
                            final int virus = "true".equals(param._useVirus) ? rs.getInt("VIRUS") : 0;
                            final int koudome = "true".equals(param._useKoudome) ? rs.getInt("KOUDOME") : 0;
                                
                            final Attendance attendance = new Attendance(lesson, mourning, suspend, abroad, mlesson, absence, attend, late, early, virus, koudome);
                            // log.debug("   schregno = " + student._schregno + " , attendance = " + attendance);
                            student._attendMap.put(semester, attendance);
                        }
                        
                    } catch (SQLException e) {
                        log.error("sql exception!", e);
                    } finally {
                        DbUtils.closeQuietly(null, ps, rs);
                        db2.commit();
                    }
                    
                    String sql = null;
                    try {
                        param._attendParamMap.put("grade", param._grade_hr_class.substring(0, 2));
                        param._attendParamMap.put("hrClass", param._grade_hr_class.substring(2, 5));
                        
                        sql = AttendAccumulate.getAttendSubclassSql(
                                param._year,
                                semesS._cd,
                                sdate,
                                edate,
                                param._attendParamMap
                        );
                        
                        //log.debug(" attend subclass sql = " + sql);
                        ps = db2.prepareStatement(sql);
                        rs = ps.executeQuery();
                        
                        while (rs.next()) {
                            final Student student = Student.getStudent(rs.getString("SCHREGNO"), students);
                            if (student == null || !"9".equals(rs.getString("SEMESTER"))) {
                                continue;
                            }
                            final String subclassCd = rs.getString("SUBCLASSCD");
                            
                            final BigDecimal lesson = rs.getBigDecimal("MLESSON");
                            final BigDecimal rawSick = rs.getBigDecimal("SICK1");
                            final BigDecimal sick = rs.getBigDecimal("SICK2");
                            final BigDecimal absent = rs.getBigDecimal("ABSENT");
                            final BigDecimal late = "1".equals(param._chikokuHyoujiFlg) ? rs.getBigDecimal("LATE") : rs.getBigDecimal("LATE2");
                            final BigDecimal early = "1".equals(param._chikokuHyoujiFlg) ? rs.getBigDecimal("EARLY") : rs.getBigDecimal("EARLY2");
                            final BigDecimal rawReplacedAbsence = rs.getBigDecimal("RAW_REPLACED_SICK");
                            final BigDecimal replacedAbsence = rs.getBigDecimal("REPLACED_SICK");
                            
                            final SubclassAttendance sa = new SubclassAttendance(lesson, rawSick, sick, absent, late, early, rawReplacedAbsence, replacedAbsence);
                            
                            student.findSubclass(subclassCd)._attendSubclassMap.put(semester, sa);
                        }
                    } catch (SQLException e) {
                        log.debug("sql exception! sql = " + sql, e);
                    } finally {
                        DbUtils.closeQuietly(null, ps, rs);
                        db2.commit();
                    }
                
                } catch (Exception e) {
                    log.error("exception!", e);
                }
            }
        }
        
        /**
         * 成績・序列・欠点をセットする
         * @param db2
         * @param student
         * @param param
         */
        private static void setScoreValue(final DB2UDB db2, final Student student, final Param param) {
            
            final String sql = sqlRecordScore(param, student._schregno);
//            log.debug("setScoreValue sql = " + sql);
            
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String subclassCd = rs.getString("SUBCLASSCD");
                    final String testCd = rs.getString("TESTCD");
                    
                    if (SUBCLASS_ALL.equals(subclassCd)) {
                        final ScoreRank sr = new ScoreRank(testCd, rs.getString("SCORE"), rs.getBigDecimal("AVG"));
                        sr._grade._rank = rs.getString("GRADE_RANK");
                        sr._grade._avgRank = rs.getString("GRADE_AVG_RANK");
                        sr._grade._count = rs.getString("GRADE_COUNT");
                        sr._course._rank = rs.getString("COURSE_RANK");
                        sr._course._avgRank = rs.getString("COURSE_AVG_RANK");
                        sr._course._count = rs.getString("COURSE_COUNT");
                        sr._hr._rank = rs.getString("CLASS_RANK");
                        sr._hr._avgRank = rs.getString("CLASS_AVG_RANK");
                        sr._hr._count = rs.getString("CLASS_COUNT");
                        sr._courseGroup._rank = rs.getString("COURSE_GROUP_RANK");
                        sr._courseGroup._avgRank = rs.getString("COURSE_GROUP_AVG_RANK");
                        sr._courseGroup._count = rs.getString("COURSE_GROUP_COUNT");
                        
                        student._scoreRankMap.put(testCd, sr);
                        // log.debug(" scoreRank = " + student + " , semTestKindCd = " + testKindCd + " , tableDiv = " + tableDiv + " "+ sr);
                    } else {
                        final String value;
                        if ("-".equals(rs.getString("VALUE_DI"))) {
                            value = "保留";
                        } else if ("*".equals(rs.getString("VALUE_DI"))) {
                            // 欠試
                            value = "";
                        } else if (null == rs.getString("RECORD_SCORE_DAT_SUBCLASSCD")) {
                            // RECORD_SCORE_DAT.SUBCLASSCDがnullならテスト無
                            value = "/";
                        } else if (Param._99902.equals(testCd)) {
                            // 平常点はRECORD_SCORE_DAT.SCOREを表示
                            value = rs.getString("RECORD_SCORE_DAT_SCORE");
                        } else if (!Param._99900.equals(testCd) && null != testCd && testCd.endsWith("9900")) {
                            value = rs.getString("ASSESSLEVEL");
                        } else {
                            value = rs.getString("VALUE");
                        }
                        student.findSubclass(subclassCd)._scoreMap.put(testCd, value);
                    }
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
        
        private static String sqlRecordScore(final Param param, final String schregno) {
            
            final String[] targets = param.getTargetSemTestCds();
            
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH RECORD_VALUE AS ( ");
            stb.append("   SELECT  T1.YEAR, T1.SCHREGNO, ");
            stb.append("           T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, ");
            stb.append("           T1.SUBCLASSCD, T1.SEMESTER, ");
            stb.append("           T1.TESTKINDCD, T1.TESTITEMCD,  T1.SCORE AS VALUE, ");
            stb.append("           T1.SCORE, VALUE(TSUBASSESS.ASSESSLEVEL, TASSESS.ASSESSLEVEL) AS ASSESSLEVEL, T1.AVG, ");
            stb.append("           T1.GRADE_RANK, T1.GRADE_AVG_RANK, T8.COUNT AS GRADE_COUNT, ");
            stb.append("           T1.COURSE_RANK, T1.COURSE_AVG_RANK, T3.COUNT AS COURSE_COUNT, ");
            stb.append("           T1.CLASS_RANK, T1.CLASS_AVG_RANK, T4.COUNT AS CLASS_COUNT, ");
            stb.append("           T1.MAJOR_RANK AS COURSE_GROUP_RANK, ");
            stb.append("           T1.MAJOR_AVG_RANK AS COURSE_GROUP_AVG_RANK, T7.COUNT AS COURSE_GROUP_COUNT ");
            stb.append("   FROM    RECORD_RANK_DAT T1");
            stb.append("   LEFT JOIN SCHREG_REGD_DAT T2 ON T2.YEAR = T1.YEAR ");
            stb.append("        AND T2.SEMESTER = '" + param.getRegdSemester() + "' ");
            stb.append("        AND T2.GRADE || T2.HR_CLASS = '" + param._grade_hr_class + "'");
            stb.append("        AND T2.SCHREGNO = '" + schregno + "'");
            stb.append("   LEFT JOIN COURSE_GROUP_CD_DAT T2_2 ON T1.YEAR = T2_2.YEAR ");
            stb.append("        AND T2.GRADE = T2_2.GRADE ");
            stb.append("        AND T2.COURSECD = T2_2.COURSECD ");
            stb.append("        AND T2.MAJORCD = T2_2.MAJORCD ");
            stb.append("        AND T2.COURSECODE = T2_2.COURSECODE");
            stb.append("   LEFT JOIN RECORD_AVERAGE_DAT T3 ON T3.YEAR = T1.YEAR ");
            stb.append("        AND T3.SEMESTER = T1.SEMESTER AND T3.TESTKINDCD = T1.TESTKINDCD AND T3.TESTITEMCD = T1.TESTITEMCD ");
            stb.append("        AND T3.CLASSCD = T1.CLASSCD AND T3.SCHOOL_KIND = T1.SCHOOL_KIND AND T3.CURRICULUM_CD = T1.CURRICULUM_CD AND T3.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("        AND T3.GRADE = T2.GRADE ");
            stb.append("        AND T3.AVG_DIV = '3' ");
            stb.append("        AND T3.HR_CLASS = '000' ");
            stb.append("        AND T3.COURSECD || T3.MAJORCD || T3.COURSECODE = T2.COURSECD || T2.MAJORCD || T2.COURSECODE ");
            stb.append("   LEFT JOIN RECORD_AVERAGE_DAT T4 ON T4.YEAR = T1.YEAR ");
            stb.append("        AND T4.SEMESTER = T1.SEMESTER AND T4.TESTKINDCD = T1.TESTKINDCD AND T4.TESTITEMCD = T1.TESTITEMCD ");
            stb.append("        AND T4.CLASSCD = T1.CLASSCD AND T4.SCHOOL_KIND = T1.SCHOOL_KIND AND T4.CURRICULUM_CD = T1.CURRICULUM_CD AND T4.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("        AND T4.GRADE = T2.GRADE ");
            stb.append("        AND T4.AVG_DIV = '2' ");
            stb.append("        AND T4.HR_CLASS = T2.HR_CLASS ");
            stb.append("        AND T4.COURSECD || T4.MAJORCD || T4.COURSECODE = '00000000' ");
            stb.append("   LEFT JOIN RECORD_AVERAGE_DAT T7 ON T7.YEAR = T1.YEAR ");
            stb.append("        AND T7.SEMESTER = T1.SEMESTER AND T7.TESTKINDCD = T1.TESTKINDCD AND T7.TESTITEMCD = T1.TESTITEMCD ");
            stb.append("        AND T7.CLASSCD = T1.CLASSCD AND T7.SCHOOL_KIND = T1.SCHOOL_KIND AND T7.CURRICULUM_CD = T1.CURRICULUM_CD AND T7.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("        AND T7.GRADE = T2.GRADE ");
            stb.append("        AND T7.AVG_DIV = '5' ");
            stb.append("        AND T7.HR_CLASS = '000' ");
            stb.append("        AND T7.COURSECD || T7.MAJORCD || T7.COURSECODE = '0' || T2_2.GROUP_CD || '0000' ");
            stb.append("   LEFT JOIN RECORD_AVERAGE_DAT T8 ON T8.YEAR = T1.YEAR ");
            stb.append("        AND T8.SEMESTER = T1.SEMESTER AND T8.TESTKINDCD = T1.TESTKINDCD AND T8.TESTITEMCD = T1.TESTITEMCD ");
            stb.append("        AND T8.CLASSCD = T1.CLASSCD AND T8.SCHOOL_KIND = T1.SCHOOL_KIND AND T8.CURRICULUM_CD = T1.CURRICULUM_CD AND T8.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("        AND T8.GRADE = T2.GRADE ");
            stb.append("        AND T8.AVG_DIV = '1' ");
            stb.append("        AND T8.HR_CLASS = '000' ");
            stb.append("        AND T8.COURSECD || T8.MAJORCD || T8.COURSECODE = '00000000' ");
            stb.append("   LEFT JOIN ASSESS_MST TASSESS ON TASSESS.ASSESSCD = '3' AND T1.SCORE BETWEEN TASSESS.ASSESSLOW AND TASSESS.ASSESSHIGH ");
            stb.append("   LEFT JOIN ASSESS_SUBCLASS_MST TSUBASSESS ON TSUBASSESS.YEAR = T1.YEAR ");
            stb.append("        AND T1.SCORE BETWEEN TSUBASSESS.ASSESSLOW AND TSUBASSESS.ASSESSHIGH ");
            stb.append("        AND TSUBASSESS.GRADE = T2.GRADE ");
            stb.append("        AND TSUBASSESS.COURSECD = T2.COURSECD ");
            stb.append("        AND TSUBASSESS.MAJORCD = T2.MAJORCD ");
            stb.append("        AND TSUBASSESS.COURSECODE = T2.COURSECODE ");
            stb.append("        AND TSUBASSESS.CLASSCD = T1.CLASSCD AND TSUBASSESS.SCHOOL_KIND = T1.SCHOOL_KIND AND TSUBASSESS.CURRICULUM_CD = T1.CURRICULUM_CD AND TSUBASSESS.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("   WHERE   T1.YEAR = '" + param._year + "' AND T1.SCHREGNO = '" + schregno +"' ");
            stb.append("           AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD IN " + SQLUtils.whereIn(true, targets));
            stb.append(" ), ETC AS ( "); // 保留入力、平常点等
            stb.append("   SELECT ");
            stb.append("      T1.SCHREGNO");
            stb.append("      , T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD ");
            stb.append("      , T1.SUBCLASSCD");
            stb.append("      , T1.SEMESTER, T1.TESTKINDCD, T1.TESTITEMCD ");
            stb.append("      , T1.SCORE, T1.VALUE_DI ");
            stb.append("   FROM RECORD_SCORE_DAT T1");
            stb.append("   WHERE   T1.YEAR = '" + param._year + "' ");
            stb.append("           AND (T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '9990201' AND T1.SCORE IS NOT NULL ");
            stb.append("             OR T1.VALUE_DI IS NOT NULL ");
            stb.append("           ) ");
            stb.append(" ), SUBCLASSES AS ( ");
            stb.append("   SELECT ");
            stb.append("      T1.SCHREGNO, T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD , T1.SEMESTER, T1.TESTKINDCD, T1.TESTITEMCD ");
            stb.append("   FROM RECORD_VALUE T1");
            stb.append("   UNION ");
            stb.append("   SELECT ");
            stb.append("      T1.SCHREGNO, T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD , T1.SEMESTER, T1.TESTKINDCD, T1.TESTITEMCD ");
            stb.append("   FROM ETC T1");
            stb.append(" ) ");
            stb.append("   SELECT ");
            stb.append("           CASE WHEN T1.SUBCLASSCD = '" + SUBCLASS_ALL + "' THEN T1.SUBCLASSCD ");
            stb.append("              ELSE T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD END ");
            stb.append("           AS SUBCLASSCD ");
            stb.append("           ,T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD AS TESTCD ");
            stb.append("           ,T2.SCORE, T2.ASSESSLEVEL, T2.AVG ");
            stb.append("           ,T2.GRADE_RANK, T2.GRADE_AVG_RANK, T2.GRADE_COUNT ");
            stb.append("           ,T2.COURSE_RANK, T2.COURSE_AVG_RANK, T2.COURSE_COUNT ");
            stb.append("           ,T2.CLASS_RANK, T2.CLASS_AVG_RANK, T2.CLASS_COUNT ");
            stb.append("           ,T2.COURSE_GROUP_RANK, T2.COURSE_GROUP_AVG_RANK, T2.COURSE_GROUP_COUNT ");
            stb.append("           ,T2.VALUE ");
            stb.append("           ,T3.VALUE_DI ");
            stb.append("           ,T3.SCORE AS RECORD_SCORE_DAT_SCORE ");
            stb.append("           ,T4.SUBCLASSCD AS RECORD_SCORE_DAT_SUBCLASSCD ");
            stb.append("   FROM    SUBCLASSES T1");
            stb.append("   LEFT JOIN RECORD_VALUE T2 ON T2.SCHREGNO = T1.SCHREGNO AND T2.CLASSCD = T1.CLASSCD AND T2.SCHOOL_KIND = T1.SCHOOL_KIND AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("       AND T2.SEMESTER = T1.SEMESTER AND T2.TESTKINDCD = T1.TESTKINDCD AND T2.TESTITEMCD = T1.TESTITEMCD ");
            stb.append("   LEFT JOIN ETC T3 ON T3.SCHREGNO = T1.SCHREGNO AND T3.CLASSCD = T1.CLASSCD AND T3.SCHOOL_KIND = T1.SCHOOL_KIND AND T3.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("       AND T3.SEMESTER = T1.SEMESTER AND T3.TESTKINDCD = T1.TESTKINDCD AND T3.TESTITEMCD = T1.TESTITEMCD ");
            stb.append("   LEFT JOIN RECORD_SCORE_DAT T4 ON T4.YEAR = '" + param._year + "' AND T4.SCHREGNO = T1.SCHREGNO AND T4.CLASSCD = T1.CLASSCD AND T4.SCHOOL_KIND = T1.SCHOOL_KIND AND T4.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("       AND T4.SEMESTER = T1.SEMESTER AND T4.TESTKINDCD = T1.TESTKINDCD AND T4.TESTITEMCD = T1.TESTITEMCD ");
            stb.append("   WHERE T1.SCHREGNO = '" + schregno +"' ");
            stb.append("       AND (T1.CLASSCD BETWEEN '" + KNJDefineSchool.subject_D + "' AND '" + KNJDefineSchool.subject_U + "' ");
            stb.append("            OR T1.CLASSCD = '" + KNJDefineSchool.subject_T + "'");
            stb.append("            OR T1.CLASSCD = '94'");
            stb.append("            OR T1.SUBCLASSCD = '" + SUBCLASS_ALL + "')");
            return stb.toString();
        }
    }
    
    /**
     * 1日出欠データ
     */
    private static class Attendance {
        
        final int _lesson;
        /** 忌引 */
        final int _mourning;
        /** 出停 */
        final int _suspend;
        /** 留学 */
        final int _abroad;
        /** 出席すべき日数 */
        final int _mlesson;
        /** 公欠 */
        final int _absence;
        final int _attend;
        /** 遅刻 */
        final int _late;
        /** 早退 */
        final int _leave;
        /** 出停伝染病 */
        final int _virus;
 
        final int _koudome;
        
        public Attendance() {
            this(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        }
        
        public Attendance(
                final int lesson,
                final int mourning,
                final int suspend,
                final int abroad,
                final int mlesson,
                final int absence,
                final int attend,
                final int late,
                final int leave,
                final int virus,
                final int koudome
        ) {
            _lesson = lesson;
            _mourning = mourning;
            _suspend = suspend;
            _abroad = abroad;
            _mlesson = mlesson;
            _absence = absence;
            _attend = attend;
            _late = late;
            _leave = leave;
            _virus = virus;
            _koudome = koudome;
        }
        
        public String toString() {
            return "[lesson=" + _lesson + 
            ",mlesson=" + _mlesson + 
            ",mourning=" + _mourning + 
            ",suspend=" + _suspend + 
            ",abroad=" + _abroad +
            ",absence=" + _absence + 
            ",attend=" + _attend + 
            ",late=" + _late +
            ",leave=" + _leave;
        }
    }
    
    /**
     * 科目ごとの出欠データ
     */
    private static class SubclassAttendance {
        final int _lesson;
        /** 換算前の欠席数 */
        final BigDecimal _rawSick;
        /** 換算後の欠課数 */
        final BigDecimal _sick;
        /** 公欠 */
        final int _absent;
        /** 遅刻早退 */
        final int _lateearly;
        /** 換算後の合併欠課数 */
        final BigDecimal _replacedSick;
        
        String _subclassCd = null;
        
        public SubclassAttendance(
                final BigDecimal lesson,
                final BigDecimal rawSick,
                final BigDecimal sick,
                final BigDecimal absent,
                final BigDecimal late,
                final BigDecimal early,
                final BigDecimal rawReplacedSick,
                final BigDecimal replacedSick) {
            _lesson = lesson.intValue();
            _rawSick = rawSick;
            _sick = sick;
            _absent = absent.intValue();
            _lateearly = late.add(early).intValue();
            _replacedSick = replacedSick;
        }
        
        private String getKekkaString() {
            return "SubclassAttendance(lesson = " + _lesson + " , rawSick = " + _rawSick + ", sick = " + _sick + " , absent = " + _absent + 
            " , lateearly = " + _lateearly + ((_replacedSick.intValue() != 0) ? " , replacedSick = " + _replacedSick : "") + ")";
        }
        
        public String toString() {
            return getKekkaString();
        }
        
        private static String formatInt(int n) {
            return n == 0 ? "" : String.valueOf(n);
        }
        
        private static String formatBigDecimal(BigDecimal n) {
            return n == null ? null : (n.doubleValue() == 0) ? formatInt(0) : sishaGonyu(n);
        }
    }
    
    /**
     * 生徒の科目ごとの成績・出欠データ
     */
    private static class Subclass {
        final String _schregno;
        final String _subclassCd;
        final String _classCd;
        final Map _scoreMap = new HashMap();
        final Map _attendSubclassMap = new HashMap();
        
        public Subclass(
                final String schregno,
                final String subclassCd
        ) {
            _schregno = schregno;
            _subclassCd = subclassCd;
            _classCd = subclassCd.substring(0, 2);
        }
        
        public String toString() {
            return _schregno + " : " + _subclassCd + ":" + _scoreMap;
        }
    }
    
    /**
     * 欠課数上限値
     */
    private static class AbsenceHigh {
        /** 履修上限 */
        final String _compAbsenceHigh;
        /** 修得上限 */
        final String _getAbsenceHigh;
        
        public AbsenceHigh(final String absenceHigh, final String getAbsenceHigh) {
            _compAbsenceHigh = absenceHigh;
            _getAbsenceHigh = getAbsenceHigh;
        }
        
        private static boolean isOver(final String kekka, final String absenceHigh) {
            if (null == kekka || !NumberUtils.isNumber(kekka) || Double.parseDouble(kekka) == 0) {
                return false;
            }
            return absenceHigh == null || Double.parseDouble(absenceHigh) < Double.parseDouble(kekka);
        }
        
        public String toString() {
            return " 履修上限値" + _compAbsenceHigh + " , 修得上限値" + _getAbsenceHigh;
        }
        
        // ------
        
        /**
         * 生徒に欠課数上限値をセットする
         * @param db2
         * @param student 生徒
         * @param param
         */
        public static void setAbsenceHigh(final DB2UDB db2, final Student student, final Param param) {
            String absenceHighSql = "";
            String spAbsenceHighSql = "";
            if (param._knjSchoolMst.isHoutei()) {
                absenceHighSql = sqlHouteiJisu(student, null, param, false);
                spAbsenceHighSql = sqlHouteiJisu(student, null, param, true);
            } else {
                absenceHighSql = sqlJituJisuSql(student, null, param, false);
                spAbsenceHighSql = sqlJituJisuSql(student, null, param, true);
            }
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(absenceHighSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String absenceHigh = StringUtils.defaultString(rs.getString("ABSENCE_HIGH"), "0");
                    final String getAbsenceHigh = StringUtils.defaultString(rs.getString("GET_ABSENCE_HIGH"), "0");
                    
                    student._subclassAbsenceHigh.put(rs.getString("SUBCLASSCD"), new AbsenceHigh(absenceHigh, getAbsenceHigh));
                }
            } catch (SQLException e) {
                log.error(e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            
            try {
                ps = db2.prepareStatement(spAbsenceHighSql);
                rs = ps.executeQuery();
                student._spSubclassAbsenceHigh.clear();
                while (rs.next()) {
                    final String compAbsenceHigh = StringUtils.defaultString(rs.getString("ABSENCE_HIGH"), "0");
                    final String getAbsenceHigh = StringUtils.defaultString(rs.getString("GET_ABSENCE_HIGH"), "0");
                    
                    student._spSubclassAbsenceHigh.put(rs.getString("SPECIAL_GROUP_CD"), new AbsenceHigh(compAbsenceHigh, getAbsenceHigh));
                }
            } catch (SQLException e) {
                log.error(e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
        
        private static String sqlHouteiJisu(final Student student, final String subclassCd, final Param param, final boolean isGroup) {
            final String tableName = isGroup ? "V_CREDIT_SPECIAL_MST" : "V_CREDIT_MST";
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T2.SCHREGNO, ");
            if (!isGroup) {
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append("    T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
                }
                stb.append("     T1.SUBCLASSCD AS SUBCLASSCD, ");
            } else {
                stb.append("     T1.SPECIAL_GROUP_CD, ");
            }
            stb.append("     VALUE(T1.ABSENCE_HIGH, 0) ");
            stb.append("       AS ABSENCE_HIGH, ");
            stb.append("     VALUE(T1.GET_ABSENCE_HIGH, 0) ");
            stb.append("       AS GET_ABSENCE_HIGH ");
            stb.append(" FROM ");
            stb.append("     " + tableName + " T1 ");
            stb.append("     INNER JOIN SCHREG_REGD_DAT T2 ON ");
            stb.append("       T2.GRADE = T1.GRADE AND ");
            stb.append("       T2.COURSECD = T1.COURSECD AND ");
            stb.append("       T2.MAJORCD = T1.MAJORCD AND ");
            stb.append("       T2.COURSECODE = T1.COURSECODE AND ");
            stb.append("       T2.YEAR = T1.YEAR AND ");
            stb.append("       T2.SEMESTER = '" + param.getRegdSemester() + "' ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            if (!isGroup) {
                if (null != subclassCd) {
                    stb.append("     AND ");
                    if ("1".equals(param._useCurriculumcd)) {
                        stb.append("    T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
                    }
                    stb.append("         T1.SUBCLASSCD = '" + subclassCd + "' ");
                }
            }
            stb.append("     AND T2.SCHREGNO = '" + student._schregno + "' ");
            return stb.toString();
        }
        
        private static String sqlJituJisuSql(final Student student, final String subclassCd, final Param param, final boolean isGroup) {
            final String tableName = isGroup ? "SCHREG_ABSENCE_HIGH_SPECIAL_DAT" : "SCHREG_ABSENCE_HIGH_DAT";
            final String tableName2 = isGroup ? "V_CREDIT_SPECIAL_MST" : "V_CREDIT_MST";
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T2.SCHREGNO, ");
            if (!isGroup) {
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append("    T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
                }
                stb.append("     T1.SUBCLASSCD AS SUBCLASSCD, ");
            } else {
                stb.append("     T1.SPECIAL_GROUP_CD, ");
            }
            stb.append("     VALUE(T1.COMP_ABSENCE_HIGH, 0) ");
            stb.append("        AS ABSENCE_HIGH, ");
            stb.append("     VALUE(T1.GET_ABSENCE_HIGH, 0) ");
            stb.append("        AS GET_ABSENCE_HIGH ");
            stb.append(" FROM ");
            stb.append("     " + tableName + " T1 ");
            stb.append("     INNER JOIN SCHREG_REGD_DAT T2 ON ");
            stb.append("       T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("       AND T2.YEAR = T1.YEAR ");
            stb.append("       AND T2.SEMESTER = '" + param.getRegdSemester() + "' ");
            stb.append("     LEFT JOIN " + tableName2 + " T3 ON ");
            if (!isGroup) {
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append("    T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || ");
                }
                stb.append("       T3.SUBCLASSCD = ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append("    T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
                }
                stb.append("       T1.SUBCLASSCD ");
            } else {
                stb.append("       T3.SPECIAL_GROUP_CD = T1.SPECIAL_GROUP_CD ");
            }
            stb.append("       AND T3.COURSECD = T2.COURSECD ");
            stb.append("       AND T3.MAJORCD = T2.MAJORCD ");
            stb.append("       AND T3.GRADE = T2.GRADE ");
            stb.append("       AND T3.COURSECODE = T2.COURSECODE ");
            stb.append("       AND T3.YEAR = T1.YEAR ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.DIV = '2' ");
            if (!isGroup) {
                if (null != subclassCd) {
                    stb.append("     AND ");
                    if ("1".equals(param._useCurriculumcd)) {
                        stb.append("    T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
                    }
                    stb.append("         T1.SUBCLASSCD = '" + subclassCd + "' ");
                }
            }
            stb.append("     AND T1.SCHREGNO = '" + student._schregno + "' ");
            return stb.toString();
        }
    }
    
    private static class Rank {
        String _rank;
        String _avgRank;
        String _count;
    }
    
    /**
     * 序列データ
     */
    private static class ScoreRank {
        final String _testCd;
        final String _totalScore;
        final BigDecimal _avg;
        final Rank _grade = new Rank();
        final Rank _course = new Rank();
        final Rank _hr = new Rank();
        final Rank _courseGroup = new Rank();

        public ScoreRank(
                final String testCd,
                final String totalScore,
                final BigDecimal avg) {
            _testCd = testCd;
            _totalScore = totalScore;
            _avg = avg;
        }
        
        public String toString() {
            return " ScoreRank " + _testCd + " (" + _totalScore + " , " + _avg + ") ";
        }
    }
    
    /**
     * 学期
     */
    private static class Semester {
        final String _cd;
        final String _name;
        final String _sdate;
        final String _edate;
        public Semester(final String semester, final String name, final String sdate, final String edate) {
            _cd = semester;
            _name = name;
            _sdate = sdate;
            _edate = edate;
        }
        
        public String toString() {
            return "(" + _name + " [" + _sdate + "," + _edate + "])";
        }
    }
    
    private static class SubclassInfo {
        final static int GAPPEI_NASI = 0;
        final static int GAPPEI_MOTO = 1;
        final static int GAPPEI_MOTO_SAKI = 2;
        final static int GAPPEI_SAKI = 9;
        final static String GAPPEI_TANNI_KOTEI = "1";
        final static String GAPPEI_TANNI_KASAN = "2";
        
        final String _classabbv;
        final String _subclassname; 
        final String _credits;
        final String _compCredit;
        final String _getCredit;
        final String _subclassCd; 
        final String[] _attendSubclasscds;
        final String _combinedSubclasscd;
        final String _namespare1;
        final Subclass _subclass;
        
        final int _replaceflg; // 0:合併設定なし、1:元科目、2:先科目かつ元科目、9:先科目

        final String _calculateCreditFlg;
        
        public SubclassInfo(
                final String classabbv,
                final String subclassname,
                final String credits,
                final String compCredit,
                final String getCredit,
                final String subclassCd,
                final String[] attendSubclasscds,
                final String combinedSubclasscd,
                final String namespare1,
                final Subclass subclass,
                final int replaceFlg,
                final String calculateCreditFlg) {
            _classabbv = classabbv;
            _subclassname = subclassname;
            _credits = credits;
            _compCredit = compCredit;
            _getCredit = getCredit;
            _subclassCd = subclassCd;
            _attendSubclasscds = attendSubclasscds;
            _combinedSubclasscd = combinedSubclasscd;
            _namespare1 = namespare1;
            _subclass = subclass;
            
            _replaceflg = replaceFlg;
            _calculateCreditFlg = calculateCreditFlg;            
        }
      
        public boolean isNotPrintSubclassList(final Param param) {
            boolean rtn = false;
            
            if (GAPPEI_MOTO == _replaceflg && param._isNoPrintMoto) { rtn = true; }
            
            if (param.isD026ContainSubclasscd(_subclassCd)) { rtn = true; }
            
            return rtn;
        }
    }
    
    private static String sishaGonyu(final BigDecimal bd) {
        return null == bd ? null : bd.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
    }

    private class Form {
        
        private static final int MAX_RECORD = 17;

        public void print(final Vrw32alp svf, final Student student) {

            log.debug(" student = " + student._schregno + "(" + student._attendNo + ")");
            
            final String formName = "KNJD185N.frm";
            log.debug(" FORM = " + formName);

            svf.VrSetForm(formName, 4);
            
            final Map fieldMap = new HashMap();
            fieldMap.put("10101", "SCORE1");
            fieldMap.put("10201", "SCORE2");
            fieldMap.put("19900", "PRE_DIV1");
            fieldMap.put("20101", "SCORE3");
            fieldMap.put("20201", "SCORE4");
            fieldMap.put("29900", "PRE_DIV2");
            fieldMap.put("30201", "SCORE5");

            printHeader(svf, student, fieldMap);

            printRecDetail(svf, student, fieldMap);

            svf.VrEndPage();
        }
        
        private String[] get_token(final String strx, final int f_len, final int f_cnt) {
            final String[] token = KNJ_EditEdit.get_token(strx, f_len, f_cnt);
            if (null == token) {
                return new String[]{};
            }
            return token;
        }
        
        private String intnum(final String s) {
            return (NumberUtils.isDigits(s) ? String.valueOf(Integer.parseInt(s)) : "");
        }

        /**
         * ヘッダ印字
         * @param svf
         * @param student
         */
        private void printHeader(final Vrw32alp svf, final Student student, final Map fieldMap) {
            svf.VrsOut("NENDO", _param.getNendo() + " " + StringUtils.defaultString(_param.getSemesterName(_param._semester)) + " " + StringUtils.defaultString(_param._testitemname)); // 年度
            svf.VrsOut("COURSE_NAME", StringUtils.defaultString(student._majorName) + StringUtils.defaultString(student._courseCodeName)); // 課程、学科
            svf.VrsOut("HR_NAME", "第 " + intnum(student._grade) + " 学年 " + intnum(student._hrClass) + "組 " + student._attendNo); // 年組番
            svf.VrsOut("NAME", "　" + StringUtils.defaultString(student._name)); // 生徒氏名

            final String[] semesScore = new String[] {"1", "2", "3", Param.SEMEALL};
            for (int i = 0; i < semesScore.length; i++) {
                svf.VrsOut("SEMESTER_NAME1_" + String.valueOf(i + 1), _param.getSemesterName(semesScore[i])); // 考査名称
            }
            svf.VrsOut("PRINCIPAL_NAME", "学校長　" + StringUtils.defaultString(_param._principalName)); // 校長名
            if (_param._staffNames != null && _param._staffNames.size() > 0) {
                final String staffname = (String) _param._staffNames.get(0);
                svf.VrsOut("TEACHER_NAME", "担　任　" + StringUtils.defaultString(_param._principalNameSpc) + StringUtils.defaultString(staffname)); // 担任名
            }
            svf.VrsOut("SCHOOL_NAME", _param._schoolName); // 学校名

            final ScoreRank sr99900 = (ScoreRank) student._scoreRankMap.get("99900");
            if (sr99900 != null) {
                svf.VrsOut("AVE_GRADE_VAL", sishaGonyu(sr99900._avg)); // 平均学年評定
            }

            svf.VrsOut("RANK_NAME1", "学級席次");
            svf.VrsOut("RANK_NAME2", ("1".equals(_param._groupDiv) ? "学年" : "2".equals(_param._groupDiv) ? "コース" : "3".equals(_param._groupDiv) ? "グループ" : "") + "席次");

            final String[] semTestKindCds = _param.getTargetSemTestCds();
            for (int k = 0; k < semTestKindCds.length; k++) {
                final ScoreRank sr = (ScoreRank) student._scoreRankMap.get(semTestKindCds[k]);
                if (null != fieldMap.get(semTestKindCds[k]) && fieldMap.get(semTestKindCds[k]).toString().startsWith("PRE_")) {
                    continue;
                }
                final String field = "AVE_" + fieldMap.get(semTestKindCds[k]);
                if (sr != null) {
                    svf.VrsOutn(field, 1, sishaGonyu(sr._avg)); // 平均素点
                    
                    svf.VrsOutn(field, 2, "2".equals(_param._rankDiv) ? sr._hr._avgRank : sr._hr._rank); // 学級席次
                    svf.VrsOut("RANK1", sr._hr._count); // 順位
                    
                    Rank r;
                    if ("1".equals(_param._rankDiv)) {
                        r = sr._grade;
                    } else if ("2".equals(_param._rankDiv)) {
                        r = sr._course;
                    } else {
                        r = sr._courseGroup;
                    }
                    svf.VrsOutn(field, 3, "2".equals(_param._rankDiv) ? r._avgRank : r._rank); // コース席次
                    svf.VrsOut("RANK2", r._count); // 順位
                }
            }
            
            if (Param.SEMEALL.equals(_param._semester)) {
                svf.VrsOut("AVE_GET_CREDIT", String.valueOf(student._totalCredit));
            }
            
            
            final String[] shoken = get_token((String) student._remarkMap.get(_param._shokenSemTestCd), 50, 99);
            for (int j = 0, shokenMaxLine = 10; j < Math.min(shokenMaxLine, shoken.length); j++) {
                final int line = j + 1;
                svf.VrsOutn("FIELD1", line, shoken[j]); // 所見
            }
            
            // 修了証
            if (Param.SEMEALL.equals(_param._semester)) {
                svf.VrsOut("CERTIF_NAME", "修了証"); // 終了証名称
                if (null != _param._stampPath) {
                    svf.VrsOut("STAFF_STAMP", _param._stampPath);
                }
                svf.VrsOut("CERTIF_TEXT", "第" + (NumberUtils.isDigits(_param._gradeCd) ? String.valueOf(Integer.parseInt(_param._gradeCd)) : "") + "学年" + StringUtils.defaultString(student._courseName) + "課程を修了したことを証します。" ); // 証明文言
                svf.VrsOut("CERTIF_DATE", KNJ_EditDate.h_format_JP(_param._descDate)); // 証明日付
                svf.VrsOut("CERTIF_SCHOOL_NAME", _param._schoolName); // 証明書学校名
                svf.VrsOut("CERTIF_PRINCIPAL_NAME", _param._principalName); // 証明書校長名
                svf.VrsOut("CERTIF_STAMP_NAME", "印"); // 印
            }
        }
        
        private void printRecDetail(final Vrw32alp svf, final Student student, final Map fieldMap) {

            final int max = 23;
            int line = 0;
            printAttendance(svf, student);
            String classabbvOld = null;
            for (final Iterator it = student._subclassInfos.iterator(); it.hasNext();) {
                printAttendance(svf, student);

                final SubclassInfo info = (SubclassInfo) it.next();
                if (null == classabbvOld || !classabbvOld.equals(info._classabbv)) {
                    svf.VrsOut("CLASS_NAME" + (getMS932ByteLength(info._classabbv) > 12 ? "2" : "1"), info._classabbv); // 教科名
                }
                final int subnamelen = getMS932ByteLength(info._subclassname);
                svf.VrsOut("SUBCLASS_NAME" + (subnamelen > 20 ? "3_1" : subnamelen > 12 ? "2" : "1"), info._subclassname);
                svf.VrsOut("CREDIT", info._credits);
                final Subclass subclass = (Subclass) student._subclassMap.get(info._subclassCd);
                
                final String[] semTestKindCds = _param.getTargetSemTestCds();
                for (int j = 0; j < semTestKindCds.length; j++) {
                    final String semTestKindCd = semTestKindCds[j];
                    if (null == subclass) {
                        svf.VrsOut((String) fieldMap.get(semTestKindCd), "/");
                    } else {
                        if (Param._99900.equals(semTestKindCd)) {
                            if (info._replaceflg != SubclassInfo.GAPPEI_MOTO) {
                                // 合併元は表示無し
                                svf.VrsOut("NORMAL", (String) subclass._scoreMap.get(Param._99902)); // 平常点
                                svf.VrsOut("GRADE_VAL", (String) subclass._scoreMap.get(Param._99900)); // 学年評定
                                svf.VrsOut("GET_CREDIT", info._getCredit); // 修得単位
                            }
                        } else {
                            final String val;
                            if (!subclass._scoreMap.keySet().contains(semTestKindCd)) {
                                val = "/";
                            } else {
                                val = (String) subclass._scoreMap.get(semTestKindCd);
                            }
                            svf.VrsOut((String) fieldMap.get(semTestKindCd), val);
                        }
                    }
                }
                
                if (null != subclass) {
	                // 合併元は表示無し
	                final BigDecimal sick = student.getSick(_param, subclass, Param.SEMEALL, info);
	                svf.VrsOut("KEKKA1", SubclassAttendance.formatBigDecimal(sick)); // 欠課時数
                }

                final String classcd = info._subclassCd.substring(0, 2);
                svf.VrsOut("GRP1", String.valueOf(classcd));
                svf.VrsOut("GRP2", String.valueOf(line));
                svf.VrEndRecord();
                line += 1;
                classabbvOld = info._classabbv;
            }
            for (int i = line; i < max; i++) {
                svf.VrsOut("GRP1", String.valueOf(i));
                svf.VrsOut("GRP2", String.valueOf(i));
                svf.VrEndRecord();
            }
        }

        private void printAttendance(final Vrw32alp svf, final Student student) {
            final String[] semes = new String[] {"1", "2", "3", Param.SEMEALL};
            for (int j = 0; j < semes.length; j++) {
                final int line = j + 1;
                final Semester semesterObj = (Semester) _param._semesterMap.get(semes[j]);
                if (null != semesterObj) {
                    svf.VrsOutn("SEMESTER_NAME2_1", line, semesterObj._name);
                }
                if (_param._semester.compareTo(semes[j]) < 0) {
                    continue;
                }
                final Attendance sum = (Attendance) student._attendMap.get(semes[j]);
                if (null != sum) {
                    svf.VrsOutn("LESSON", line, String.valueOf(sum._lesson)); // 授業日数
                    svf.VrsOutn("MOURNING", line, String.valueOf(sum._mourning + sum._suspend + sum._virus + sum._koudome)); // 出停・忌引等日数
                    svf.VrsOutn("PRESENT", line, String.valueOf(sum._mlesson)); // 出席すべき日数
                    svf.VrsOutn("ABSENCE", line, String.valueOf(sum._absence)); // 欠席日数
                    svf.VrsOutn("ATTEND", line, String.valueOf(sum._attend)); // 出席日数
                    svf.VrsOutn("LATE", line, String.valueOf(sum._late)); // 遅刻回数
                    svf.VrsOutn("EARLY", line, String.valueOf(sum._leave)); // 早退回数
                    svf.VrsOutn("KEKKA2", line, String.valueOf(student.getKekkaJisu(_param, semes[j]))); // 欠課時数
                }
            }
        }
        
        private int getFieldColumnSemester(final String semester) {
            final String[] semesters = _param.getTargetSemester();
            for (int i = 0; i < semesters.length; i++) {
                if (semesters[i].equals(semester)) {
                    return (i + 1);
                }
            }
            return 0;
        }

        private int getFieldColumnTest(final String semTestKindCd) {
            final String[] testCds = _param.getTargetSemTestCds();
            for (int i = 0; i < testCds.length; i++) {
                if (testCds[i].equals(semTestKindCd)) {
                    return (i + 1);
                }
            }
            return 0;
        }
    }
    
    private Param createParam(final HttpServletRequest request, final DB2UDB db2) {
        log.fatal(" $Revision: 75848 $ $Date: 2020-08-05 12:26:41 +0900 (水, 05 8 2020) $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(request, db2);
        return param;
    }
    
    /**
     * パラメータクラス
     */
    private static class Param {
        
        static final String SEMEALL = "9";
        static final String _99900 = "99900";
        static final String _99902 = "99902"; // 平常点のコード

        final String _year;
        final String _semester;
        final String _testCd;
        final String _shokenSemTestCd;
        final String _ctrlSemester;
        final String _grade_hr_class;
        
        private String _sdate;
        final String _edate;
        final String _descDate;
        private String _gradeCd;
        final String[] _dispSemester;
        final String[] _allSemTestCds;
        final String[] _categorySelected;
        
        final String _groupDiv;
        
        private String _schoolName;
        private String _z010name1;
        private String _principalName;
        private String _principalNameSpc;
        private String _jobName;
        private String _hrJobName;
        private String _schoolAddress;
        private String _schoolTelNo;
        private List _staffNames;
        private KNJSchoolMst _knjSchoolMst;
        
        private KNJDefineSchool _definecode;
        
        private TreeMap _semesterMap;
        private String _testitemname;
        final String _rankDiv;
        /** D016 */
        private boolean _isNoPrintMoto;
        /** D026 */
        private final List _d026List = new ArrayList();
        /** D016 */
        private boolean _isMirishuu;

        final String _documentRoot;
        private String _imagePath;
        private String _extension;
        private String _stampPath;
        
        /** 欠課換算前の遅刻・早退を表示する */
        final String _chikokuHyoujiFlg;
        
        private String _attendEndDateSemester;
        
        /** 単位マスタの警告数は単位が回数か */
        private boolean _absenceWarnIsUnitCount;
        
        /** 教育課程コードを使用するか */
        final String _useCurriculumcd;
        final String _useClassDetailDat;
        final String _useVirus;
        final String _useKoudome;
        final Map _attendParamMap;

        private Map _courseNotPrintSubclasscdListMap;
        
        public Param(final HttpServletRequest request, final DB2UDB db2) {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _testCd = request.getParameter("TESTKINDCD");
            if (_99900.equals(_semester + _testCd)) {
                _shokenSemTestCd = "3" + "0201";
            } else if ("9900".equals(_testCd)) {
                _shokenSemTestCd = _semester + "0201";
            } else {
                _shokenSemTestCd = _semester + _testCd;
            }
            _ctrlSemester = request.getParameter("CTRL_SEME");            
            _edate = request.getParameter("DATE").replace('/', '-');
            _descDate = null == request.getParameter("DESC_DATE") ? null : request.getParameter("DESC_DATE").replace('/', '-');
            _grade_hr_class = request.getParameter("GRADE_HR_CLASS");
            _categorySelected = request.getParameterValues("category_selected");
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _useClassDetailDat = request.getParameter("useClassDetailDat");
            _useVirus = request.getParameter("useVirus");
            _useKoudome = request.getParameter("useKoudome");
            _groupDiv = request.getParameter("GROUP_DIV");
            
            _rankDiv = request.getParameter("RANK_DIV");
            _documentRoot = request.getParameter("DOCUMENTROOT");
            _dispSemester = new String[]{"1", "2", "3", SEMEALL};

            _allSemTestCds = new String[]{"10101", "10201", "19900", "20101", "20201", "29900", "30201", _99900};
            log.debug(" target test cd = " + ArrayUtils.toString(getTargetSemTestCds()));
            _chikokuHyoujiFlg = request.getParameter("chikokuHyoujiFlg");
            load(db2);
            try {
                setSubclassReplaceCombinedDatSakiKamoku(db2);
            } catch (Exception e) {
                log.fatal("exception!", e);
            }
            
            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
        }
        
        public boolean isGakunenmatu(final String semTestKindCd) {
            return SEMEALL.equals(semTestKindCd.substring(0,1));
        }
        
        private void setSubclassReplaceCombinedDatSakiKamoku(final DB2UDB db2) throws SQLException {
            _courseNotPrintSubclasscdListMap = new HashMap();
            final StringBuffer stb = new StringBuffer(); 
            stb.append(" SELECT DISTINCT ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append(" T1.COMBINED_CLASSCD || '-' || T1.COMBINED_SCHOOL_KIND || '-' || T1.COMBINED_CURRICULUM_CD || '-' || ");
            }
            stb.append(" T1.COMBINED_SUBCLASSCD AS COMBINED_SUBCLASSCD, ");
            stb.append(" T2.GRADE, T2.COURSECD || T2.MAJORCD || T2.COURSECODE AS COURSE ");
            stb.append(" FROM SUBCLASS_REPLACE_COMBINED_DAT T1 ");
            stb.append(" INNER JOIN CREDIT_MST T2 ON T2.YEAR = T1.YEAR ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append("     AND T2.CLASSCD = T1.ATTEND_CLASSCD ");
                stb.append("     AND T2.SCHOOL_KIND = T1.ATTEND_SCHOOL_KIND ");
                stb.append("     AND T2.CURRICULUM_CD = T1.ATTEND_CURRICULUM_CD ");
            }
            stb.append("     AND T2.SUBCLASSCD = T1.ATTEND_SUBCLASSCD ");
            stb.append(" WHERE T1.YEAR = '" + _year + "' ");

            PreparedStatement ps = db2.prepareStatement(stb.toString());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                getMappedList(_courseNotPrintSubclasscdListMap, rs.getString("GRADE") + rs.getString("COURSE")).add(rs.getString("COMBINED_SUBCLASSCD"));
            }
            DbUtils.closeQuietly(null, ps, rs);
            log.info(" not print subclasscd list = " + _courseNotPrintSubclasscdListMap);
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
        
        public String getRegdSemester() {
            return Param.SEMEALL.equals(_semester) ? _ctrlSemester : _semester;
        }
        
        public String getMaxSemester() {
            try {
                return _knjSchoolMst._semesterDiv;
            } catch (Exception ex) {
                log.error("getMaxSemester exception!", ex);
            }
            try {
                return String.valueOf(_definecode.semesdiv);
            } catch (Exception ex) {
                log.error("getMaxSemester exception!", ex);
            }
            return null;
        }
        
        public String[] getTargetSemester() {
            final List list = new ArrayList();
            for (int i = 0; i < _dispSemester.length; i++) {
                final String dseme = _dispSemester[i];
                if (_semester.compareTo(dseme) >= 0) {
                    list.add(dseme);
                }
            }
            
            final String[] elems = new String[list.size()];
            list.toArray(elems);
            return elems;
        }
        
        public String[] getTargetSemTestCds() {
            final List list = new ArrayList();
            for (int i = 0; i < _allSemTestCds.length; i++) {
                if ((_semester + _testCd).compareTo(_allSemTestCds[i]) >= 0) {
                    list.add(_allSemTestCds[i]);
                }
            }
            final String[] elems = new String[list.size()];
            list.toArray(elems);
            return elems;
        }
        
        private void setCertifSchoolDat(final DB2UDB db2) {
            StringBuffer sql = new StringBuffer();
            sql.append(" SELECT SCHOOL_NAME, JOB_NAME, PRINCIPAL_NAME, REMARK2, REMARK4, REMARK5 FROM CERTIF_SCHOOL_DAT ");
            sql.append(" WHERE YEAR = '" + _year + "' AND CERTIF_KINDCD = '104' ");
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
                    _schoolAddress = rs.getString("REMARK4");
                    _schoolTelNo = rs.getString("REMARK5");
                }
            } catch (Exception ex) {
                log.error("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            if (null != _principalName) { 
                String spc = "";
                for (int i = 0; i < _principalName.length(); i++) {
                    final char c = _principalName.charAt(i);
                    if (c == ' ' || c == '　') {
                        spc += String.valueOf(c);
                    } else {
                        break;
                    }
                }
                _principalNameSpc = spc;
            }
        }
        
        public void load(final DB2UDB db2) {
            try {
                loadNameMstD016(db2);
                loadNameMstD026(db2);
            } catch (SQLException e) {
                log.error("名称マスタ読み込みエラー", e);
            }
            
            try {
                _knjSchoolMst = new KNJSchoolMst(db2, _year);
            } catch (SQLException e) {
                log.warn("学校マスタ読み込みエラー", e);
            }
            
            _definecode = new KNJDefineSchool();
            _definecode.defineCode(db2, _year);
            if (log.isDebugEnabled()) {
                log.debug("schoolmark=" + _definecode.schoolmark + " *** semesdiv=" + _definecode.semesdiv + " " + 
                        "*** absent_cov=" + _definecode.absent_cov + " *** absent_cov_late=" + _definecode.absent_cov_late);
            }
            
            loadSemester(db2);
            loadTestItem(db2);
            loadControlMst(db2);
            loadAttendEdateSemester(db2);
            loadGradeCd(db2);
            
            final File file = new File(_documentRoot + "/" + _imagePath + "/" + "SCHOOLSTAMP." + _extension);
            _stampPath = file.exists() ? file.getAbsolutePath() : null;
            
            _staffNames = getStaffNames(db2, Param.SEMEALL.equals(_semester) ? _ctrlSemester : _semester);
            
            setCertifSchoolDat(db2);
        }
        
        public String getNendo() {
            final String[] arr = KNJ_EditDate.tate_format(KNJ_EditDate.h_format_JP(_year + "-04-01"));
            return arr[0] + " " + arr[1] + " 年度";
        }
        
        private void loadGradeCd(final DB2UDB db2) {
            
            final String sql = "SELECT GRADE_CD FROM SCHREG_REGD_GDAT "
                + " WHERE YEAR = '" + _year + "' AND GRADE = '" + _grade_hr_class.substring(0, 2) + "'";
             PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    _gradeCd = rs.getString("GRADE_CD");
                }
                
            } catch (Exception ex) {
                log.error("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
        
        private void loadSemester(final DB2UDB db2) {
            _semesterMap = new TreeMap();
            
            final String sql = "SELECT SEMESTER, SEMESTERNAME, SDATE, EDATE FROM V_SEMESTER_GRADE_MST "
                + " WHERE YEAR = '" + _year + "' AND GRADE = '" + _grade_hr_class.substring(0, 2) + "' ORDER BY SEMESTER";
            //log.debug(" semester sql = " + sql);
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                boolean first = true;
                while (rs.next()) {
                    final String cd = rs.getString("SEMESTER");
                    final String name = rs.getString("SEMESTERNAME");
                    final String sdate = rs.getString("SDATE");
                    final String edate = rs.getString("EDATE");
                    final Semester semester = new Semester(cd, name, sdate, edate);
                    _semesterMap.put(cd, semester);
                    
                    if (first) {
                        _sdate = sdate;
                        first = false;
                    }
                }
                
            } catch (Exception ex) {
                log.error("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
        
        private void loadTestItem(final DB2UDB db2) {
            
            final String sql = "SELECT TESTITEMNAME FROM TESTITEM_MST_COUNTFLG_NEW "
                + " WHERE YEAR = '" + _year + "' AND SEMESTER = '" + _semester + "' AND TESTKINDCD || TESTITEMCD = '" + _testCd + "' ";
            //log.debug(" semester sql = " + sql);
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    _testitemname = rs.getString("TESTITEMNAME");
                }
                
            } catch (Exception ex) {
                log.error("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
        
        public String getSemesterName(final String semester) {
            Semester s = (Semester) _semesterMap.get(semester);
            return s == null ? null : s._name;
        }
        
        private void loadNameMstD016(final DB2UDB db2) throws SQLException {
            _isNoPrintMoto = false;
            _isMirishuu = false;
            final String sql = "SELECT NAMECD2, NAMESPARE1, NAMESPARE2 FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'D016'";
            final PreparedStatement ps = db2.prepareStatement(sql);
            final ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                final String name = rs.getString("NAMESPARE1");
                final String name2 = rs.getString("NAMESPARE2");
                if ("Y".equals(name)) _isNoPrintMoto = true;
                if ("Y".equals(name2)) _isMirishuu = true;
            }
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
            log.debug("(名称マスタD016):元科目を表示しない = " + _isNoPrintMoto);
            log.debug("(名称マスタD016):未履修 = " + _isMirishuu);
        }
        
        
        private void loadNameMstD026(final DB2UDB db2) {
            
            final StringBuffer sql = new StringBuffer();
            if ("1".equals(_useCurriculumcd) && "1".equals(_useClassDetailDat)) {
                final String field = "SUBCLASS_REMARK" + (SEMEALL.equals(_semester) ? "4" : String.valueOf(Integer.parseInt(_semester)));
                sql.append(" SELECT CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS SUBCLASSCD FROM SUBCLASS_DETAIL_DAT ");
                sql.append(" WHERE YEAR = '" + _year + "' AND SUBCLASS_SEQ = '007' AND " + field + " = '1'  ");
            } else {
                final String field = SEMEALL.equals(_semester) ? "NAMESPARE1" : "ABBV" + _semester;
                sql.append(" SELECT NAME1 AS SUBCLASSCD FROM V_NAME_MST ");
                sql.append(" WHERE NAMECD1 = 'D026' AND " + field + " = '1'  ");
            }
            
            PreparedStatement ps = null;
            ResultSet rs = null;
            _d026List.clear();
            try {
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String subclasscd = rs.getString("SUBCLASSCD");
                    _d026List.add(subclasscd);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        private String loadAttendEdateSemester(final DB2UDB db2) {
            final StringBuffer stb = new StringBuffer(); 
            stb.append(" SELECT T1.YEAR, T1.SEMESTER, T1.SDATE, T1.EDATE, T2.SEMESTER AS NEXT_SEMESTER, T2.SDATE AS NEXT_SDATE ");
            stb.append(" FROM V_SEMESTER_GRADE_MST T1 ");
            stb.append(" LEFT JOIN V_SEMESTER_GRADE_MST T2 ON T2.YEAR = T1.YEAR ");
            stb.append("     AND T2.GRADE = T1.GRADE ");
            stb.append("     AND INT(T2.SEMESTER) = INT(T1.SEMESTER) + 1 ");
            stb.append(" WHERE T1.YEAR = '" + _year + "' ");
            stb.append("     AND T1.SEMESTER <> '9' ");
            stb.append("     AND (('" + _edate + "' BETWEEN T1.SDATE AND T1.EDATE) ");
            stb.append("          OR ('" + _edate + "' BETWEEN T1.EDATE AND VALUE(T2.SDATE, '9999-12-30'))) ");
            
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    _attendEndDateSemester = rs.getString("SEMESTER");
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return _attendEndDateSemester;
        }

        public boolean isD026ContainSubclasscd(String subclasscd) {
            if (null == subclasscd) {
                return false;
            }
            if ("1".equals(_useCurriculumcd)) {
                if ("1".equals(_useClassDetailDat)) {
                } else if (StringUtils.split(subclasscd, "-").length == 3) {
                    subclasscd = StringUtils.split(subclasscd, "-")[3]; // clascd '-' school_kind '-' curriculum_cd '-' subclasscd
                }
            }
            return _d026List.contains(subclasscd);
        }
        
        public List getStaffNames(final DB2UDB db2, final String semester) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            List list = new LinkedList();
            try{
                StringBuffer stb = new StringBuffer();
                stb.append("SELECT  (SELECT STAFFNAME FROM STAFF_MST T2 WHERE T2.STAFFCD = T1.TR_CD1) AS TR_NAME1 ");
                stb.append(       ",(SELECT STAFFNAME FROM STAFF_MST T2 WHERE T2.STAFFCD = T1.TR_CD2) AS TR_NAME2 ");
                stb.append(       ",(SELECT STAFFNAME FROM STAFF_MST T2 WHERE T2.STAFFCD = T1.TR_CD3) AS TR_NAME3 ");
                stb.append("FROM    SCHREG_REGD_HDAT T1 ");
                stb.append("WHERE   T1.YEAR = '" + _year + "' ");
                stb.append(    "AND T1.GRADE||T1.HR_CLASS = '" + _grade_hr_class + "' ");
                stb.append("    AND T1.SEMESTER = '" + semester + "' ");
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    if( rs.getString("TR_NAME1") != null )list.add( rs.getString("TR_NAME1") );
                    if( rs.getString("TR_NAME2") != null )list.add( rs.getString("TR_NAME2") );
                    if( rs.getString("TR_NAME3") != null )list.add( rs.getString("TR_NAME3") );
                }
            } catch (Exception ex) {
                log.error("List Staff_name() Staff_name error!", ex );
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }
    }
}
