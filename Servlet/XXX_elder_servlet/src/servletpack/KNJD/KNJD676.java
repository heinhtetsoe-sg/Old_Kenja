/*
 * $Id: ef81fd2decb93ed59d46ffbb986a170d4ed35521 $
 *
 * 作成日: 2015/08/04
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

/**
 * 文京学園　クラス別未評定数一覧表
 */
public class KNJD676 {

    private static final Log log = LogFactory.getLog(KNJD676.class);

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

        final List courseGroupList = CourseGroup.getCourseGroupList(db2, _param);
        
        svf.VrSetForm("KNJD676.frm", 4);
        
        svf.VrsOut("TITLE", KenjaProperties.gengou(Integer.parseInt(_param._year)) + "年度　" + StringUtils.defaultString(_param._semestername) + "　" + StringUtils.defaultString(_param._testitemname) + "　クラス別未評定数一覧表"); // タイトル
        svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._loginDate)); // 
        
        for (int cgi = 0; cgi < courseGroupList.size(); cgi++) {
            final CourseGroup courseGroup = (CourseGroup) courseGroupList.get(cgi);
            svf.VrsOut("GRADE", courseGroup._gradeName1); // 学年

            final Map subclassMap = new TreeMap();
            for (int hri = 0; hri < courseGroup._hrList.size(); hri++) {
                final HrClass hr = (HrClass) courseGroup._hrList.get(hri);

                for (int sti = 0; sti < hr._studentList.size(); sti++) {
                    final Student student = (Student) hr._studentList.get(sti);
                    subclassMap.putAll(student._subclassMap);
                }
            }
            
            final List subclassList = new ArrayList(subclassMap.values());
            Collections.sort(subclassList);
            
            svf.VrsOut("COURSE", courseGroup._groupName);
            for (int si = 0; si < subclassList.size(); si++) {
                final Subclass subclass = (Subclass) subclassList.get(si);
                final int ssi = si + 1;
                
                svf.VrsOutn("SELECT_MARK", ssi, ("3".equals(subclass._requireFlg) ? "＊" : "")); // 選択科目表示
                svf.VrsOutn("SUBCLASS_NAME", ssi, null != subclass._subclassabbv && subclass._subclassabbv.length() > 3 ? subclass._subclassabbv.substring(0, 3) : subclass._subclassabbv); // 科目名
            }
            svf.VrEndRecord();
            
            for (int hri = 0; hri < courseGroup._hrList.size(); hri++) {
                final HrClass hr = (HrClass) courseGroup._hrList.get(hri);
                
                svf.VrsOut("HR_NAME_HEADER", "クラス"); // クラスヘッダ
                svf.VrsOut("HR_NAME", hr._hrClassName1); // 年組

                final List hishuKei = new ArrayList();
                final List sentakuKei = new ArrayList();
                final List zengouKei = new ArrayList();

                for (int subi = 0; subi < subclassList.size(); subi++) {
                    final Subclass subclass = (Subclass) subclassList.get(subi);

                    final List mihyoteiStudentList = getMihyoteiStudentList(hr._studentList, subclass._subclasscd);
                    svf.VrsOutn("SUBCLASS_NOVAL", subi + 1, zeroToNull(mihyoteiStudentList.size()));

                    if ("3".equals(subclass._requireFlg)) {
                        sentakuKei.addAll(mihyoteiStudentList);
                    } else {
                        hishuKei.addAll(mihyoteiStudentList);
                    }
                    zengouKei.addAll(mihyoteiStudentList);
                }

                // HR合計
                svf.VrsOut("TOTAL1", zeroToNull(hishuKei.size()));
                svf.VrsOut("TOTAL2", zeroToNull(sentakuKei.size()));
                svf.VrsOut("TOTAL3", zeroToNull(zengouKei.size()));
                svf.VrEndRecord();
            }

            // コース合計
            final List courseGroupStudentList = courseGroup.getStudentList();
            final List hishuKeiCg = new ArrayList();
            final List sentakuKeiCg = new ArrayList();
            final List zengouKeiCg = new ArrayList();

            boolean hasCourseGroup = false;
            for (int subi = 0; subi < subclassList.size(); subi++) {
                final Subclass subclass = (Subclass) subclassList.get(subi);

                final List mihyoteiStudentList = getMihyoteiStudentList(courseGroupStudentList, subclass._subclasscd);
                svf.VrsOutn("COURSE_SUBCLASS_NOVAL", subi + 1, zeroToNull(mihyoteiStudentList.size()));
                hasCourseGroup = hasCourseGroup || mihyoteiStudentList.size() > 0;

                if ("3".equals(subclass._requireFlg)) {
                    sentakuKeiCg.addAll(mihyoteiStudentList);
                } else {
                    hishuKeiCg.addAll(mihyoteiStudentList);
                }
                zengouKeiCg.addAll(mihyoteiStudentList);

            }

            if (hasCourseGroup) {
                svf.VrsOut("COURSE_TOTAL1", zeroToNull(hishuKeiCg.size()));
                svf.VrsOut("COURSE_TOTAL2", zeroToNull(sentakuKeiCg.size()));
                svf.VrsOut("COURSE_TOTAL3", zeroToNull(zengouKeiCg.size()));
            } else {
                svf.VrsOut("COURSE_TOTAL1", "dummy");
                svf.VrAttribute("COURSE_TOTAL1", "X=10000");
            }
            
            svf.VrEndRecord();
            
            svf.VrsOut("BLANK", "1"); // 空行
            svf.VrEndRecord();
            
            _hasData = true;
        }
    }
    
    private static String zeroToNull(final int n) {
        return 0 == n ? null : String.valueOf(n);
    }
    
    private static List getMihyoteiStudentList(final List studentList, final String subclasscd) {
        final List mihyoteiList = new ArrayList();
        for (int sti = 0; sti < studentList.size(); sti++) {
            final Student student = (Student) studentList.get(sti);
            
            final boolean isMihyotei = student.isMihyotei(subclasscd);
            if (isMihyotei) {
                mihyoteiList.add(student);
            }
        }
        return mihyoteiList;
    }
    
    private static Map getMappedMap(final Map map, final String key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new TreeMap());
        }
        return (Map) map.get(key1);
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
        final Map _recordScoreDatSubclasscdMap = new HashMap();
        final Map _scoreMap = new HashMap();

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
        
        public boolean isMihyotei(final String subclasscd) {
            if (null != _recordScoreDatSubclasscdMap.get(subclasscd)) {
                // RECORD_SCORE_DATにレコードがある
                if (null == _scoreMap.get(subclasscd)) {
                    // 得点がnull
                    return true;
                }
            }
            return false;
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
    }
    
    private static class HrClass {
        final String _grade;
        final String _hrClass;
        final String _hrName;
        final String _hrClassName1;
        final String _staffname;
        final List _studentList;

        HrClass(
                final String grade,
                final String hrClass,
                final String hrName,
                final String hrClassName1,
                final String staffname) {
            _grade = grade;
            _hrClass = hrClass;
            _hrName = hrName;
            _hrClassName1 = hrClassName1;
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
        
        public List getStudentList() {
            final List studentList = new ArrayList();
            for (int hri = 0; hri < _hrList.size(); hri++) {
                final HrClass hr = (HrClass) _hrList.get(hri);
                studentList.addAll(hr._studentList);
            }
            return studentList;
        }

        public static List getCourseGroupList(final DB2UDB db2, final Param param) {
            final List courseGroupList = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("   GDAT.GRADE_NAME1, ");
            stb.append("   CGRP.GROUP_CD, ");
            stb.append("   CGRPH.GROUP_NAME, ");
            stb.append("   T1.GRADE, ");
            stb.append("   T1.HR_CLASS, ");
            stb.append("   HDAT.HR_NAME, ");
            stb.append("   HDAT.HR_CLASS_NAME1, ");
            stb.append("   HRSTF.STAFFNAME, ");
            stb.append("   T1.ATTENDNO, ");
            stb.append("   T1.SCHREGNO, ");
            stb.append("   BASE.NAME, ");
            stb.append("   T2.CHAIRCD, ");
            stb.append("   T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("   SUBM.SUBCLASSNAME, ");
            stb.append("   SUBM.SUBCLASSABBV, ");
            stb.append("   CRE.REQUIRE_FLG, ");
            stb.append("   TREC.SUBCLASSCD AS RECORD_SCORE_DAT_SUBCLASSCD, ");
            stb.append("   TREC.SCORE ");
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
            stb.append(" INNER JOIN CHAIR_STD_DAT T2 ON T2.YEAR = T1.YEAR ");
            stb.append("     AND T2.SEMESTER = T1.SEMESTER ");
            stb.append("     AND T2.SCHREGNO = T1.SCHREGNO ");
            stb.append(" INNER JOIN CHAIR_DAT T3 ON T3.YEAR = T2.YEAR ");
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
            stb.append(" LEFT JOIN RECORD_SCORE_DAT TREC ON TREC.YEAR = T3.YEAR ");
            stb.append("     AND TREC.SEMESTER = '" + param._semester + "' ");
            stb.append("     AND TREC.TESTKINDCD || TREC.TESTITEMCD || TREC.SCORE_DIV = '" + param._testcd + "' ");
            stb.append("     AND TREC.CLASSCD = T3.CLASSCD ");
            stb.append("     AND TREC.SCHOOL_KIND = T3.SCHOOL_KIND ");
            stb.append("     AND TREC.CURRICULUM_CD = T3.CURRICULUM_CD ");
            stb.append("     AND TREC.SUBCLASSCD = T3.SUBCLASSCD ");
            stb.append("     AND TREC.SCHREGNO = T1.SCHREGNO ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.SEMESTER = '" + param.getRegdSemester() + "' ");
            stb.append("     AND T1.GRADE = '" + param._grade + "' ");
            if (!"00000".equals(param._gradeHrClass)) {
                stb.append("         AND T1.GRADE || T1.HR_CLASS = '" + param._gradeHrClass + "' ");
            }
            stb.append("         AND CGRP.GROUP_CD = '" + param._groupCd + "' ");
            stb.append(" ORDER BY ");
            stb.append("     CGRP.GROUP_CD, ");
            stb.append("     T1.GRADE, ");
            stb.append("     T1.HR_CLASS, ");
            stb.append("     T1.ATTENDNO, ");
            stb.append("     T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD ");
            
            final Map studentMap = new HashMap();
            try {
                final String sql = stb.toString();
                log.debug(" sql = " + sql);
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
                        final String hrClassName1 = rs.getString("HR_CLASS_NAME1");
                        final String staffname = rs.getString("STAFFNAME");
                        
                        final HrClass hr = new HrClass(grade, hrClass, hrName, hrClassName1, staffname);
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
                        final String score = rs.getString("SCORE");
                        
                        final Subclass subclass = new Subclass(subclasscd, subclassabbv, requireFlg);
                        student._subclassMap.put(subclasscd, subclass);
                        student._recordScoreDatSubclasscdMap.put(subclasscd, rs.getString("RECORD_SCORE_DAT_SUBCLASSCD"));
                        student._scoreMap.put(subclasscd, score);
                    }
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return courseGroupList;
        }
        
        private static Integer toInteger(final String v) {
            if (NumberUtils.isDigits(v)) {
                return Integer.valueOf(v);
            }
            return null;
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
        final String _testcd;
        final String _groupCd;
        final String _gradeHrClass;
        final String _loginDate;
        final String _testitemname;
        final String _semestername;
        
        private KNJDefineSchool _definecode;  //各学校における定数等設定のクラス
        
        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _grade = request.getParameter("GRADE");
            _testcd = request.getParameter("TESTKIND_ITEMCD");
            _groupCd = request.getParameter("GROUP_CD");
            _gradeHrClass = request.getParameter("GRADE_HR_CLASS");
            _loginDate = request.getParameter("LOGIN_DATE");

            _testitemname = getTestitem(db2, "TESTITEMNAME");
            _semestername = getSemestername(db2);
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

        private String getTestitem(final DB2UDB db2, final String field) {
            String sql = "";
            sql += " SELECT " + field + " FROM TESTITEM_MST_COUNTFLG_NEW_SDIV T1 ";
            sql += " LEFT JOIN SEMESTER_DETAIL_MST T2 ON T2.YEAR = T1.YEAR ";
            sql += "     AND T2.SEMESTER = T1.SEMESTER ";
            sql += "     AND T2.SEMESTER_DETAIL = T1.SEMESTER_DETAIL ";
            sql += " WHERE ";
            sql += "   T1.YEAR = '" + _year + "' ";
            sql += "   AND T1.SEMESTER = '" + _semester + "' ";
            sql += "   AND T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '" + _testcd + "' ";
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString(field);
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            }
            return rtn;
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
    }
}

// eof

