/*
 * $Id: 1f1a9cc03209d00e19d9f725f9252516cea4cb88 $
 *
 * 作成日: 2009/10/17
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJZ;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 *  学校教育システム 賢者 [マスタ管理]
 *
 *                  ＜ＫＮＪＺ２３９Ａ＞  選択科目群一覧表
 */
public class KNJZ239A {

    private static final Log log = LogFactory.getLog(KNJZ239A.class);

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

    private static int getMS932ByteLength(final String s) {
        int rtn = 0;
        if (null != s) {
            try {
                rtn = s.getBytes("MS932").length;
            } catch(Exception e) {
                log.error("exception!", e);
            }
        }
        return rtn;
    }

    /** 出力処理 */
    private void printMain(final DB2UDB db2, final Vrw32alp svf) {

        final List list = SubclassCompSelectGroup.getCourseList(db2, _param);
        
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Course course = (Course) it.next();

            svf.VrSetForm("KNJZ239A.frm", 4);
            svf.VrsOut("NENDO", KNJ_EditDate.h_format_JP_N(_param._yearComb + "-01-01") + "度");
            svf.VrsOut("TIME", _param._loginHour + "時" + _param._loginMinutes + "分");
            svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._loginDate));
            svf.VrsOut("STAFFNAME_SHOW", _param._loginStaffName);
            svf.VrsOut("SCHOOLNAME1", _param._schoolName);
            svf.VrsOut("NOTICE", _param._requireInfo);
            svf.VrsOut("GRADE", course._coursename);
            
            for (final Iterator cit = course._groupList.iterator(); cit.hasNext();) {
                
                final SubclassCompSelectGroup group = (SubclassCompSelectGroup) cit.next();

                for (final Iterator sit = group._subclassList.iterator(); sit.hasNext();) {
                    svf.VrsOut("GROUPCD", group._groupcd);
                    svf.VrsOut("GROUP_NAME", group._groupname);
                    
                    final Subclass subclass = (Subclass) sit.next();

                    svf.VrsOut("CLASS_NAME" + (getMS932ByteLength(subclass._classname) > 16 ? "_2" : "_1"), subclass._classname);
                    svf.VrsOut("SUBCLASSCD", subclass._subclasscd);
                    svf.VrsOut("SUBCLASS" + (getMS932ByteLength(subclass._subclassname) > 20 ? "_2" : "_1"), subclass._subclassname);

                    svf.VrsOut("COURSE_NAME", StringUtils.defaultString(subclass._coursename) + StringUtils.defaultString(subclass._majorname) + StringUtils.defaultString(subclass._coursecodename));
                    svf.VrsOut("CREDIT", subclass._credits);
                    svf.VrsOut("SEL_SUBCLASS", subclass._requireFlgName); // 必履修フラグ
                    svf.VrEndRecord();
                }
                
                svf.VrsOut("SEL_CREDIT_NAME", "（選択単位数）");
                svf.VrsOut("SEL_CREDIT", group._selectcredits);
                svf.VrEndRecord();
                _hasData = true;
                
            }
        }
    }

    private static class Course {
        final String _year;
        final String _coursecode;
        final String _coursename;
        final List _groupList = new ArrayList();

        Course(
            final String year,
            final String coursecode,
            final String coursename
        ) {
            _year = year;
            _coursecode = coursecode;
            _coursename = coursename;
        }
        
        static Course getCourse(final List list, final String year, final String coursecode) {
            Course rtn = null;
            for (final Iterator it = list.iterator(); it.hasNext();) {
                Course c = (Course) it.next();
                if (null != c._year && c._year.equals(year) && null != c._coursecode && c._coursecode.equals(coursecode) || null == c._year && null == year && null == c._coursecode && null == coursecode) {
                    rtn = c;
                    break;
                }
            }
            return rtn;
        }
    }
    
    private static class SubclassCompSelectGroup {
        final String _year;
        final String _groupcd;
        final String _groupname;
        final String _coursecode;
        final String _selectcredits;
        final List _subclassList = new ArrayList();

        SubclassCompSelectGroup(
            final String year,
            final String groupcd,
            final String groupname,
            final String coursecode,
            final String selectcredits
        ) {
            _year = year;
            _groupcd = groupcd;
            _groupname = groupname;
            _coursecode = coursecode;
            _selectcredits = selectcredits;
        }
        
        static SubclassCompSelectGroup getGroup(final List list, final String year, final String groupcd) {
            SubclassCompSelectGroup rtn = null;
            for (final Iterator it = list.iterator(); it.hasNext();) {
                SubclassCompSelectGroup g = (SubclassCompSelectGroup) it.next();
                if (null != g._year && g._year.equals(year) && null != g._groupcd && g._groupcd.equals(groupcd) || null == g._year && null == year && null == g._groupcd && null == groupcd) {
                    rtn = g;
                    break;
                }
            }
            return rtn;
        }

        public static List getCourseList(final DB2UDB db2, final Param param) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = sql(param);
                log.debug(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String year = rs.getString("YEAR");
                    final String coursecdoe = rs.getString("COURSECODE");

                    if (null == Course.getCourse(list, year, coursecdoe)) {
                        final String coursecodename = StringUtils.defaultString(rs.getString("GRADENAME")) + StringUtils.defaultString(rs.getString("COURSENAME")) + StringUtils.defaultString(rs.getString("MAJORNAME")) + StringUtils.defaultString(rs.getString("COURSECODENAME"));
                        Course grade = new Course(year, coursecdoe, coursecodename);
                        list.add(grade);
                    }
                    final Course grade = Course.getCourse(list, year, coursecdoe);
                    
                    final String groupcd = rs.getString("GROUPCD");
                    if (null == getGroup(grade._groupList, year, groupcd)) {
                        final String groupname = rs.getString("GROUPNAME");
                        final String selectcredits = rs.getString("SELECT_CREDITS");
                        final SubclassCompSelectGroup subclasscompselectgroup = new SubclassCompSelectGroup(year, groupcd, groupname, coursecdoe, selectcredits);
                        grade._groupList.add(subclasscompselectgroup);
                    }

                    final SubclassCompSelectGroup group = getGroup(grade._groupList, year, groupcd);
                    final String classcd = rs.getString("CLASSCD");
                    final String classname = rs.getString("CLASSNAME");
                    final String schoolKind = "1".equals(param._useCurriculumcd) ? rs.getString("SCHOOL_KIND") : null;
                    final String curriculumCd = "1".equals(param._useCurriculumcd) ? rs.getString("CURRICULUM_CD") : null;
                    final String subclasscd = rs.getString("SUBCLASSCD");
                    final String subclassname = rs.getString("SUBCLASSNAME");
                    final String credits = rs.getString("CREDITS");
                    final String coursename = rs.getString("COURSENAME");
                    final String majorname = rs.getString("MAJORNAME");
                    final String coursecodename = rs.getString("COURSECODENAME");
                    final String requireFlgName = rs.getString("REQUIRE_FLG_NAME");
                    
                    group._subclassList.add(new Subclass(classcd, classname, schoolKind, curriculumCd, subclasscd, subclassname, credits, coursename, majorname, coursecodename, requireFlgName));
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }
        
        /** 対象のSQL */
        private static String sql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.YEAR, T1.GROUPCD, T2.NAME AS GROUPNAME, T1.GRADE, T7.GRADE_NAME1 AS GRADENAME ");
            stb.append("     ,T1.GRADE || T1.COURSECD || T1.MAJORCD || T1.COURSECODE AS COURSECODE ");
            stb.append("     ,T1.CLASSCD, T3.CLASSNAME ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("         ,T1.SCHOOL_KIND ");
                stb.append("         ,T1.CURRICULUM_CD ");
            }
            stb.append("     ,T1.SUBCLASSCD, T4.SUBCLASSNAME ");
            stb.append("     ,T5.CREDITS ");
            stb.append("     ,T7.COURSENAME ");
            stb.append("     ,T8.MAJORNAME ");
            stb.append("     ,T9.COURSECODENAME ");
            stb.append("     ,T6.NAMESPARE1 AS REQUIRE_FLG_NAME ");
            stb.append("     ,T2.CREDITS AS SELECT_CREDITS ");
            stb.append(" FROM ");
            stb.append("     SUBCLASS_COMP_SELECT_DAT T1 ");
            stb.append("     INNER JOIN SUBCLASS_COMP_SELECT_MST T2 ON T1.YEAR = T2.YEAR ");
            stb.append("         AND T1.GRADE = T2.GRADE ");
            stb.append("         AND T1.COURSECD = T2.COURSECD ");
            stb.append("         AND T1.MAJORCD = T2.MAJORCD ");
            stb.append("         AND T1.COURSECODE = T2.COURSECODE ");
            stb.append("         AND T1.GROUPCD = T2.GROUPCD ");
            stb.append("     INNER JOIN CLASS_MST T3 ON T3.CLASSCD = T1.CLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("         AND T3.SCHOOL_KIND = T1.SCHOOL_KIND ");
            }
            stb.append("     INNER JOIN SUBCLASS_MST T4 ON T4.SUBCLASSCD = T1.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("         AND T4.CLASSCD = T1.CLASSCD ");
                stb.append("         AND T4.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("         AND T4.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append("     LEFT JOIN CREDIT_MST T5 ON T5.YEAR = T1.YEAR ");
            stb.append("         AND T5.GRADE = T1.GRADE ");
            stb.append("         AND T5.COURSECD = T1.COURSECD ");
            stb.append("         AND T5.MAJORCD = T1.MAJORCD ");
            stb.append("         AND T5.COURSECODE = T1.COURSECODE ");
            stb.append("         AND T5.CLASSCD = T1.CLASSCD ");
            stb.append("         AND T5.SUBCLASSCD = T1.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("         AND T5.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("         AND T5.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append("     LEFT JOIN COURSE_MST T7 ON T5.COURSECD = T7.COURSECD ");
            stb.append("     LEFT JOIN MAJOR_MST T8 ON T5.COURSECD = T8.COURSECD ");
            stb.append("         AND T5.MAJORCD = T8.MAJORCD ");
            stb.append("     LEFT JOIN COURSECODE_MST T9 ON T5.COURSECODE = T9.COURSECODE ");
            stb.append("     LEFT JOIN NAME_MST T6 ON T6.NAMECD1 = 'Z011' ");
            stb.append("         AND T6.NAMECD2 = T5.REQUIRE_FLG ");
            stb.append("     LEFT JOIN SCHREG_REGD_GDAT T7 ON T7.YEAR = T1.YEAR ");
            stb.append("         AND T7.GRADE = T1.GRADE ");
            if ("1".equals(param._useSchool_KindField) && !StringUtils.isBlank(param._SCHOOLKIND) && !StringUtils.isBlank(param._SCHOOLCD)) {
                stb.append("   AND T7.SCHOOL_KIND = '" + param._SCHOOLKIND + "' ");
            }
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._yearComb + "' ");
            if (!param._allGrade.equals(param._grade)) {
                stb.append("     AND T1.GRADE = '" + param._grade + "' ");
            }
            if ("1".equals(param._useSchool_KindField) && !StringUtils.isBlank(param._SCHOOLKIND) && !StringUtils.isBlank(param._SCHOOLCD)) {
                stb.append("   AND T1.SCHOOL_KIND = '" + param._SCHOOLKIND + "' ");
            }
            stb.append(" ORDER BY ");
            stb.append("     T1.GRADE, T1.GROUPCD, T1.CLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("         ,T1.SCHOOL_KIND ");
                stb.append("         ,T1.CURRICULUM_CD ");
            }
            stb.append("     , T1.SUBCLASSCD ");
            return stb.toString();
        }
    }
    
    private static class Subclass {
        final String _classcd;
        final String _classname;
        final String _schoolKind;
        final String _curriculumCd;
        final String _subclasscd;
        final String _subclassname;
        final String _credits;
        final String _coursename;
        final String _majorname;
        final String _coursecodename;
        final String _requireFlgName;

        Subclass(
            final String classcd,
            final String classname,
            final String schoolKind,
            final String curriculumCd,
            final String subclasscd,
            final String subclassname,
            final String credits,
            final String coursename,
            final String majorname,
            final String coursecodename,
            final String requireFlgName
        ) {
            _classcd = classcd;
            _classname = classname;
            _schoolKind = schoolKind;
            _curriculumCd = curriculumCd;
            _subclasscd = subclasscd;
            _subclassname = subclassname;
            _credits = credits;
            _coursename = coursename;
            _majorname = majorname;
            _coursecodename = coursecodename;
            _requireFlgName = requireFlgName;
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
        final String _allGrade= "99"; // 全学年のコード

        /** 年度 */
        final String _year;

        /** 指定年度 */
        final String _yearComb;
        final String _semester;
        
        /** 指定学年 */
        final String _grade;

        /** ログイン職員 */
        final String _loginStaffCd;

        /** ログイン日付 */
        final String _loginDate;

        /** ログイン時間 */
        final String _loginHour;
        final String _loginMinutes;
        
        final String _useCurriculumcd;

        private final String _useSchool_KindField;
        private final String _SCHOOLCD;
        private final String _SCHOOLKIND;

//        String _gradeName = null;
        String _schoolKind = null;
        String _schoolName = null;
        String _loginStaffName = null;
        String _requireInfo = null;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _yearComb = request.getParameter("YEAR_COMB");
            _semester = request.getParameter("SEMESTER");
            _grade = request.getParameter("GRADE");
            _loginStaffCd = request.getParameter("STAFFCD");
            _loginDate = request.getParameter("LOGIN_DATE");
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            _SCHOOLCD= request.getParameter("SCHOOLCD");
            _SCHOOLKIND = request.getParameter("SCHOOLKIND");

            final Calendar cal = Calendar.getInstance();
            _loginHour = String.valueOf(cal.get(Calendar.HOUR_OF_DAY));
            _loginMinutes = String.valueOf(cal.get(Calendar.MINUTE));
            _useCurriculumcd = request.getParameter("useCurriculumcd");

            setLoginStaffName(db2);
            setSchoolName(db2);
//            setGradeName(db2);
            setRequireInfo(db2);
        }
        
//        /** 学年名取得 */
//        private void setGradeName(DB2UDB db2) {
//            PreparedStatement ps = null;
//            ResultSet rs = null;
//            _gradeName = null;
//            try {
//                String sql = "SELECT SCHOOL_KIND, GRADE_NAME1 FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _yearComb + "' AND GRADE = '" + _grade + "' ";
//                ps = db2.prepareStatement(sql);
//                rs = ps.executeQuery();
//                
//                while (rs.next()) {
//                    _schoolKind = rs.getString("SCHOOL_KIND");
//                    _gradeName = rs.getString("GRADE_NAME1") != null ? rs.getString("GRADE_NAME1") : "";
//                }
//                
//            } catch (SQLException e) {
//                log.error("Exception:", e);
//            } finally {
//                db2.commit();
//                DbUtils.closeQuietly(null, ps, rs);
//            }
//            
//            if (_gradeName == null) {
//                _gradeName = "全学年";
//            }
//        }

        /** 学校名取得 */
        private void setSchoolName(DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            _schoolName = null;
            try {
                String sql = "SELECT SCHOOLNAME1 FROM SCHOOL_MST WHERE YEAR = '" + _year + "' ";
                if ("1".equals(_useSchool_KindField) && !StringUtils.isBlank(_SCHOOLKIND) && !StringUtils.isBlank(_SCHOOLCD)) {
                    sql += ("   AND SCHOOL_KIND = '" + _SCHOOLKIND + "' ");
                }
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                
                while (rs.next()) {
                    _schoolName = rs.getString("SCHOOLNAME1");
                }
                
            } catch (SQLException e) {
                log.error("Exception:", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        /** スタッフ名取得 */
        private void setLoginStaffName(DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            _loginStaffName = null;
            try {
                String sql = "SELECT STAFFNAME FROM STAFF_MST WHERE STAFFCD = '" + _loginStaffCd + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                
                while (rs.next()) {
                    _loginStaffName = rs.getString("STAFFNAME");
                }
                
            } catch (SQLException e) {
                log.error("Exception:", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }
        
        /** 必履修備考取得 */
        private void setRequireInfo(DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            _requireInfo = null;
            StringBuffer info = new StringBuffer();
            try {
                String sql = "SELECT VALUE(NAMESPARE1,'') || ':' || VALUE(NAME1,'') AS NOTE FROM NAME_MST WHERE NAMECD1 = 'Z011' ORDER BY NAMECD2 ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                
                info.append("※必履修区分…");
                String noteSep = "";
                while (rs.next()) {
                    info.append(noteSep + rs.getString("NOTE"));
                    noteSep = "、";
                }
                
            } catch (SQLException e) {
                log.error("Exception:", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            _requireInfo = info.toString();
        }
    }
}

// eof

