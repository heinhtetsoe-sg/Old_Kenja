/*
 * $Id: ace141b48498e792c7e300ded181925de9712fb6 $
 *
 * 作成日: 2015/08/04
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

/**
 * 文京学園　クラス別欠時数一覧表
 */
public class KNJD674 {

    private static final Log log = LogFactory.getLog(KNJD674.class);

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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        final int maxSubclass = 25;
        
        final List courseGroupList = CourseGroup.getCourseGroupList(db2, _param);
        for (int cgi = 0; cgi < courseGroupList.size(); cgi++) {
            final CourseGroup cg = (CourseGroup) courseGroupList.get(cgi);
            
            for (int hi = 0; hi < cg._hrList.size(); hi++) {
                final HrClass hr = (HrClass) cg._hrList.get(hi);
                
                svf.VrSetForm("KNJD674.frm", 4);
                
                svf.VrsOut("TITLE", KenjaProperties.gengou(Integer.parseInt(_param._year)) + "年度　" + StringUtils.defaultString(_param._semestername) + "　クラス別欠時数一覧表"); // タイトル
                svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._loginDate)); // 
                svf.VrsOut("GRADE_COURSE", cg._groupName); // 学年コース名
                svf.VrsOut("HR_NAME", hr._hrName); // 年組
                svf.VrsOut("TEACHER_NAME", hr._staffname); // 担任名

                for (int sti = 0; sti < hr._studentList.size(); sti++) {
                    final Student student = (Student) hr._studentList.get(sti);
                    final int line = sti + 1;
                    svf.VrsOutn("NO", line, NumberUtils.isDigits(student._attendno) ? String.valueOf(Integer.parseInt(student._attendno)) : student._attendno); // 番号
                    svf.VrsOutn("NAME", line, student._name); // 氏名

                    svf.VrsOutn("MUST_SUBTOTAL", line, getKekkaOutputString(student.kekkaTotal(1))); // 必修小計
                    svf.VrsOutn("SLELCT_SUBTOTAL", line, getKekkaOutputString(student.kekkaTotal(2))); // 選択小計
                    svf.VrsOutn("MUST_SELECT_SUBTOTAL", line, getKekkaOutputString(student.kekkaTotal(0))); // 必修選択小計
                }
                
                svf.VrsOut("MUST_TOTAL", getKekkaOutputString(Student.kekkaTotal(1, hr._studentList))); // 必修小計
                svf.VrsOut("SLELCT_TOTAL", getKekkaOutputString(Student.kekkaTotal(2, hr._studentList))); // 選択小計
                svf.VrsOut("MUST_SELECT_TOTAL", getKekkaOutputString(Student.kekkaTotal(0, hr._studentList))); // 必修選択小計

                svf.VrsOut("AVE_NAME", "合計");

                final List subclassList = getAllSubclassList(hr._studentList); // 表示対象の科目リスト
                Collections.sort(subclassList);

                for (int subi = 0; subi < subclassList.size(); subi++) {
                    final Subclass subclass = (Subclass) subclassList.get(subi);
                    
                    svf.VrsOut("SELECT_MARK", "3".equals(subclass._requireFlg) ? "＊" : ""); // 選択科目表示
                    svf.VrsOut("SUBCLASS_NAME", null != subclass._subclassabbv && subclass._subclassabbv.length() > 3 ? subclass._subclassabbv.substring(0, 3) : subclass._subclassabbv); // 科目名
                    
                    String total = null;
                    for (int sti = 0; sti < hr._studentList.size(); sti++) {
                        final Student student = (Student) hr._studentList.get(sti);
                        final int line = sti + 1;
                        final String kekka = (String) student._kekkaMap.get(subclass._subclasscd);
                        svf.VrsOutn("LOST", line, getKekkaOutputString(kekka));
                        total = add(total, kekka);
                    }
                    svf.VrsOut("SUBCLASS_TOTAL", getKekkaOutputString(total));

                    svf.VrEndRecord();
                }

                for (int i = subclassList.size(); i < maxSubclass; i++) {
                    svf.VrEndRecord();
                }
                _hasData = true;
            }
        }
    }
    
    private String getKekkaOutputString(final String v) {
        if (!NumberUtils.isNumber(v)) {
            return null;
        }
        final BigDecimal bd = new BigDecimal(v);
        if (bd.doubleValue() == 0.0) {
            return null;
        }
        if (3 == _param._definecode.absent_cov || 4 == _param._definecode.absent_cov) {
            return bd.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
        }
        return bd.setScale(0, BigDecimal.ROUND_HALF_UP).toString();
    }

    private List getAllSubclassList(final List studentList) {
        final Map subclassMap = new TreeMap();
        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            subclassMap.putAll(student._subclassMap);
        }
        return new ArrayList(subclassMap.values());
    }
    
    /**
     * 数値を加算して文字列（両方nullの場合、null）で返す
     * @param num1
     * @param num2
     * @return
     */
    private static String add(String num1, String num2) {
        if (NumberUtils.isNumber(num2)) {
            if (NumberUtils.isNumber(num1)) {
                num1 = new BigDecimal(num1).add(new BigDecimal(num2)).toString();
            } else {
                num1 = num2;
            }
        }
        return num1;
    }

    private static class Subclass implements Comparable {
        final String _subclasscd;
        final String _subclassabbv;
        final String _requireFlg;
        public Subclass(final String subclasscd, final String subclassabbv, final String requireFlg) {
            _subclasscd = subclasscd;
            _subclassabbv = subclassabbv;
            _requireFlg = requireFlg;
        }
        public int compareTo(Object o) {
            final Subclass subclass = (Subclass) o;
            String requireFlg1 = StringUtils.defaultString(_requireFlg, "0");
            String requireFlg2 = StringUtils.defaultString(subclass._requireFlg, "0");
            if (!"3".equals(requireFlg1) && "3".equals(requireFlg2)) {
                return -1;
            } else if ("3".equals(requireFlg1) && !"3".equals(requireFlg2)) { // 選択科目は後
                return 1;
            }
            return _subclasscd.compareTo(subclass._subclasscd);
        }
    }

    private static class Student {
        final String _grade;
        final String _hrClass;
        final String _attendno;
        final String _schregno;
        final String _name;
        final Map _subclassMap = new HashMap();
        final Map _kekkaMap = new HashMap();

        Student(
            final String grade,
            final String hrClass,
            final String attendno,
            final String schregno,
            final String name
        ) {
            _grade = grade;
            _hrClass = hrClass;
            _attendno = attendno;
            _schregno = schregno;
            _name = name;
        }
        
        private static Student getStudent(final String schregno, final List studentList) {
            for (final Iterator it = studentList.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                if (schregno.equals(student._schregno)) {
                    return student;
                }
            }
            return null;
        }
        
        /**
         * 生徒の欠課数
         * @param div 0:合計、1:必修計、2:選択計
         * @return
         */
        private String kekkaTotal(final int div) {
            String total = null;
            for (final Iterator it = _kekkaMap.keySet().iterator(); it.hasNext();) {
                final String subclasscd = (String) it.next();
                final Subclass subclass = (Subclass) _subclassMap.get(subclasscd);
                if (0 == div || 1 == div && !"3".equals(subclass._requireFlg) || 2 == div && "3".equals(subclass._requireFlg)) {
                    if (null != _kekkaMap.get(subclasscd)) {
                        final String sick2 = (String) _kekkaMap.get(subclasscd);
                        total = add(total, sick2);
                    }
                }
            }
            return total;
        }

        /**
         * 生徒のリストの欠課数の合計
         * @param div 0:合計、1:必修計、2:選択計
         * @param studentList 生徒のリスト
         * @return
         */
        private static String kekkaTotal(final int div, final List studentList) {
            String total = null;
            for (final Iterator it = studentList.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                total = add(total, student.kekkaTotal(div));
            }
            return total;
        }

        /**
         * 指定科目の母集団の生徒の欠課合計
         * @param studentList 母集団の生徒
         * @param subclasscd 指定科目
         * @return
         */
        private static String kekkaSubclassTotal(final List studentList, final String subclasscd) {
            String total = null;
            for (final Iterator it = studentList.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                if (null != student._kekkaMap.get(subclasscd)) {
                    final String sick2 = (String) student._kekkaMap.get(subclasscd);
                    total = add(total, sick2);
                }
            }
            return total;
        }
    }
    
    private static class HrClass {
        final String _grade;
        final String _hrClass;
        final String _hrName;
        final String _staffname;
        final List _studentList;

        HrClass(
                final String grade,
                final String hrClass,
                final String hrName,
                final String staffname) {
            _grade = grade;
            _hrClass = hrClass;
            _hrName = hrName;
            _staffname = staffname;
            _studentList = new ArrayList();
        }
        
        
        private static HrClass getHrClass(final String gradeHrclass, final List hrList) {
            for (final Iterator it = hrList.iterator(); it.hasNext();) {
                final HrClass hr = (HrClass) it.next();
                if (gradeHrclass.equals(hr._grade + hr._hrClass)) {
                    return hr;
                }
            }
            return null;
        }
    }

    private static class CourseGroup {
        final String _gradeName1;
        final String _groupCd;
        final String _groupName;
        final List _hrList;

        CourseGroup(
            final String gradeName1,
            final String groupCd,
            final String groupName
        ) {
            _gradeName1 = gradeName1;
            _groupCd = groupCd;
            _groupName = groupName;
            _hrList = new ArrayList();
        }

        public static List getCourseGroupList(final DB2UDB db2, final Param param) {
            final List courseGroupList = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map studentMap = new HashMap();
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("   GDAT.GRADE_NAME1, ");
                stb.append("   CGRP.GROUP_CD, ");
                stb.append("   CGRPH.GROUP_NAME, ");
                stb.append("   T1.GRADE, ");
                stb.append("   T1.HR_CLASS, ");
                stb.append("   HDAT.HR_NAME, ");
                stb.append("   HRSTF.STAFFNAME, ");
                stb.append("   T1.ATTENDNO, ");
                stb.append("   T1.SCHREGNO, ");
                stb.append("   BASE.NAME, ");
                stb.append("   T2.CHAIRCD, ");
                stb.append("   T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD AS SUBCLASSCD, ");
                stb.append("   SUBM.SUBCLASSABBV, ");
                stb.append("   CRE.REQUIRE_FLG ");
                stb.append(" FROM SCHREG_REGD_DAT T1 ");
                stb.append(" INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = T1.SCHREGNO ");
                stb.append(" INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR ");
                stb.append("     AND GDAT.GRADE = T1.GRADE ");
                stb.append(" INNER JOIN SCHREG_REGD_HDAT HDAT ON HDAT.YEAR = T1.YEAR ");
                stb.append("     AND HDAT.SEMESTER = T1.SEMESTER ");
                stb.append("     AND HDAT.GRADE = T1.GRADE ");
                stb.append("     AND HDAT.HR_CLASS = T1.HR_CLASS ");
                stb.append(" LEFT JOIN STAFF_MST HRSTF ON HRSTF.STAFFCD = HDAT.TR_CD1 ");
                stb.append(" INNER JOIN COURSE_GROUP_CD_DAT CGRP ON CGRP.YEAR = T1.YEAR ");
                stb.append("     AND CGRP.GRADE = T1.GRADE ");
                stb.append("     AND CGRP.COURSECD = T1.COURSECD ");
                stb.append("     AND CGRP.MAJORCD = T1.MAJORCD ");
                stb.append("     AND CGRP.COURSECODE = T1.COURSECODE ");
                stb.append(" INNER JOIN COURSE_GROUP_CD_HDAT CGRPH ON CGRPH.YEAR = CGRP.YEAR ");
                stb.append("     AND CGRPH.GRADE = CGRP.GRADE ");
                stb.append("     AND CGRPH.GROUP_CD = CGRP.GROUP_CD ");
                stb.append(" LEFT JOIN CHAIR_STD_DAT T2 ON T2.YEAR = T1.YEAR ");
                stb.append("     AND T2.SEMESTER = T1.SEMESTER ");
                stb.append("     AND T2.SCHREGNO = T1.SCHREGNO ");
                stb.append(" LEFT JOIN CHAIR_DAT T3 ON T3.YEAR = T2.YEAR ");
                stb.append("     AND T3.SEMESTER = T2.SEMESTER ");
                stb.append("     AND T3.CHAIRCD = T2.CHAIRCD ");
                stb.append("     AND T3.CLASSCD <= '90' ");
                stb.append(" LEFT JOIN SUBCLASS_MST SUBM ON SUBM.CLASSCD = T3.CLASSCD ");
                stb.append("     AND SUBM.SCHOOL_KIND = T3.SCHOOL_KIND ");
                stb.append("     AND SUBM.CURRICULUM_CD = T3.CURRICULUM_CD ");
                stb.append("     AND SUBM.SUBCLASSCD = T3.SUBCLASSCD ");
                stb.append(" LEFT JOIN CREDIT_MST CRE ON CRE.YEAR = T1.YEAR ");
                stb.append("     AND CRE.COURSECD = T1.COURSECD ");
                stb.append("     AND CRE.MAJORCD = T1.MAJORCD ");
                stb.append("     AND CRE.GRADE = T1.GRADE ");
                stb.append("     AND CRE.COURSECODE = T1.COURSECODE ");
                stb.append("     AND CRE.CLASSCD = T3.CLASSCD ");
                stb.append("     AND CRE.SCHOOL_KIND = T3.SCHOOL_KIND ");
                stb.append("     AND CRE.CURRICULUM_CD = T3.CURRICULUM_CD ");
                stb.append("     AND CRE.SUBCLASSCD = T3.SUBCLASSCD ");
                stb.append(" WHERE ");
                stb.append("     T1.YEAR = '" + param._year + "' ");
                stb.append("     AND T1.SEMESTER = '" + param.getRegdSemester() + "' ");
                stb.append("     AND T1.GRADE = '" + param._grade + "' ");
                if (!"00000".equals(param._gradeHrclass)) {
                    stb.append("         AND T1.GRADE || T1.HR_CLASS = '" + param._gradeHrclass + "' ");
                }
                stb.append("     AND CGRP.GROUP_CD = '" + param._groupCd + "' ");
                stb.append(" ORDER BY ");
                stb.append("     CGRP.GROUP_CD, ");
                stb.append("     T1.GRADE, ");
                stb.append("     T1.HR_CLASS, ");
                stb.append("     T1.ATTENDNO, ");
                stb.append("     T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD ");

                final String sql = stb.toString();
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                final Map courseGroupMap = new HashMap();
                while (rs.next()) {
                    
                    final String groupCd = rs.getString("GROUP_CD");
                    if (null == courseGroupMap.get(groupCd)) {
                        final String gradeName1 = rs.getString("GRADE_NAME1");
                        final String groupName = rs.getString("GROUP_NAME");
                        
                        final CourseGroup coursegroup = new CourseGroup(gradeName1, groupCd, groupName);
                        courseGroupList.add(coursegroup);
                        courseGroupMap.put(groupCd, coursegroup);
                    }
                    final CourseGroup coursegroup = (CourseGroup) courseGroupMap.get(groupCd);
                    
                    final String grade = rs.getString("GRADE");
                    final String hrClass = rs.getString("HR_CLASS");
                    if (null == HrClass.getHrClass(grade + hrClass, coursegroup._hrList)) {
                        final String hrName = rs.getString("HR_NAME");
                        final String staffname = rs.getString("STAFFNAME");
                        
                        final HrClass hr = new HrClass(grade, hrClass, hrName, staffname);
                        coursegroup._hrList.add(hr);
                    }

                    final HrClass hr = HrClass.getHrClass(grade + hrClass, coursegroup._hrList);
                    final String schregno = rs.getString("SCHREGNO");
                    if (null == Student.getStudent(schregno, hr._studentList)) {
                        final String attendno = rs.getString("ATTENDNO");
                        final String name = rs.getString("NAME");
                        
                        final Student student = new Student(grade, hrClass, attendno, schregno, name);
                        hr._studentList.add(student);
                        studentMap.put(schregno, student);
                    }

                    if (null != rs.getString("SUBCLASSCD")) {
                        final Student student = Student.getStudent(schregno, hr._studentList);
                        final String subclasscd = rs.getString("SUBCLASSCD");
                        final String subclassabbv = rs.getString("SUBCLASSABBV");
                        final String requireFlg = rs.getString("REQUIRE_FLG");
                        
                        final Subclass subclass = new Subclass(subclasscd, subclassabbv, requireFlg);
                        student._subclassMap.put(subclasscd, subclass);
                    }
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            loadAttendSubclass(db2, param, studentMap);
            return courseGroupList;
        }

    }
    
    private static void loadAttendSubclass(
            final DB2UDB db2,
            final Param param,
            final Map studentMap
    ) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            param._attendParamMap.put("schregno", "?");
            final String sql = AttendAccumulate.getAttendSubclassSql(param._year, param._semester, null, param._date, param._attendParamMap);
            //log.debug(" attend subclass sql = " + sql);
            ps = db2.prepareStatement(sql);
            
            for (final Iterator it = studentMap.entrySet().iterator(); it.hasNext();) {
                final Map.Entry e = (Map.Entry) it.next();
                final String schregno = (String) e.getKey();
                final Student student = (Student) e.getValue();

                ps.setString(1, schregno);
                rs = ps.executeQuery();

                while (rs.next()) {
                    if (!"9".equals(rs.getString("SEMESTER"))) {
                        continue;
                    }
                    if (null == student._subclassMap.get(rs.getString("SUBCLASSCD"))) {
                        continue;
                    }
                    student._kekkaMap.put(rs.getString("SUBCLASSCD"), rs.getString("SICK2"));
                }

                DbUtils.closeQuietly(null, null, rs);
                db2.commit();
            }
            
        } catch (SQLException e) {
            log.error("SQLException", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 56595 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _year;
        final String _semester;
        final String _ctrlSemester;
        final String _grade;
        final String _gradeHrclass;
        final String _groupCd;
        final String _date;
        final String _loginDate;
        final String _semestername;
        
        private KNJDefineSchool _definecode;  //各学校における定数等設定のクラス
        private Map _attendParamMap;
        
        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _grade = request.getParameter("GRADE");
            _gradeHrclass = request.getParameter("GRADE_HR_CLASS");
            _groupCd = request.getParameter("GROUP_CD");
            _date = request.getParameter("SDATE");
            _loginDate = request.getParameter("LOGIN_DATE");
            _semestername = getSemestername(db2);
            
            _definecode = createDefineCode(db2);

            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
            _attendParamMap.put("grade", _grade);
            _attendParamMap.put("useCurriculumcd", "1");
            _attendParamMap.put("useTestCountflg", "TESTITEM_MST_COUNTFLG_NEW_SDIV");
            _attendParamMap.put("absenceDiv", "2");
        }
        
        /** 作成日 */
        public String getNow() {
            final StringBuffer stb = new StringBuffer();
            final Date date = new Date();
            final SimpleDateFormat sdfY = new SimpleDateFormat("yyyy");
            stb.append(nao_package.KenjaProperties.gengou(Integer.parseInt(sdfY.format(date))));
            final SimpleDateFormat sdf = new SimpleDateFormat("年M月d日H時m分");
            stb.append(sdf.format(date));
            return stb.toString();
        }

        public String getRegdSemester() {
            return "9".equals(_semester) ? _ctrlSemester : _semester;
        }

        private String getSemestername(final DB2UDB db2) {
            String sql = "";
            sql += " SELECT SEMESTERNAME FROM SEMESTER_MST ";
            sql += " WHERE ";
            sql += "   YEAR = '" + _year + "' ";
            sql += "   AND SEMESTER = '" + _semester + "' ";
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString("SEMESTERNAME");
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            }
            return rtn;
        }
        
        private DecimalFormat getAbsentFmt() {
            DecimalFormat absentFmt;
            switch (_definecode.absent_cov) {
            case 0:
            case 1:
            case 2:
                absentFmt = new DecimalFormat("0");
                break;
            default:
                absentFmt = new DecimalFormat("0.0");
            }
            return absentFmt;
        }
        
        /*
         *  クラス内で使用する定数設定
         */
        private KNJDefineSchool createDefineCode(
                final DB2UDB db2
        ) {
            final KNJDefineSchool definecode = new KNJDefineSchool();
            definecode.defineCode(db2, _year);         //各学校における定数等設定
            log.debug("semesdiv=" + definecode.semesdiv + "   absent_cov=" + definecode.absent_cov + "   absent_cov_late=" + definecode.absent_cov_late);
            return definecode;
        }
    }
}

// eof

