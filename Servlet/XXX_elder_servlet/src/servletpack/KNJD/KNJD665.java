/*
 * $Id: 40f51cd279d3d2be9d28133f5a8d0a53c6b51432 $
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

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 * 文京学園　考査欠席者一覧表
 */
public class KNJD665 {

    private static final Log log = LogFactory.getLog(KNJD665.class);

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

        final int maxSubclass = 35;
        final int maxStudent = 50;

        final List courseGroupList = CourseGroup.getCourseGroupList(db2, _param);
        for (int i = 0; i < courseGroupList.size(); i++) {
            final CourseGroup courseGroup = (CourseGroup) courseGroupList.get(i);
            
            final List targetStudentList = new ArrayList();
            final Map subclassMap = new TreeMap();
            for (int sti = 0; sti < courseGroup._studentList.size(); sti++) {
                final Student student = (Student) courseGroup._studentList.get(sti);

                for (final Iterator it = student._subclassMap.keySet().iterator(); it.hasNext();) {
                    final String subclasscd = (String) it.next();
                    if (student.isKesshi(subclasscd)) { // マークが1つ以上ある生徒が対象
                        targetStudentList.add(student);
                        subclassMap.putAll(student._subclassMap);
                        break;
                    }
                }
            }
            
            if (targetStudentList.size() == 0) { // 対象者がない場合、表示しない
                continue;
            }
            log.debug(" student size = " + targetStudentList.size());
            
            final List subclassListAll = new ArrayList(subclassMap.values());
            Collections.sort(subclassListAll);

            final List subclassListList = getGroupList(subclassListAll, maxSubclass);
            for (int subpi = 0; subpi < subclassListList.size(); subpi++) {
                final List subclassList = (List) subclassListList.get(subpi);
                
                final List studentListList = getGroupList(targetStudentList, maxStudent);
                for (int stpi = 0; stpi < studentListList.size(); stpi++) {
                    final List targetStudentListp = (List) studentListList.get(stpi);
                    
                    svf.VrSetForm("KNJD665.frm", 4);
                    
                    svf.VrsOut("TITLE", KenjaProperties.gengou(Integer.parseInt(_param._year)) + "年度　" + StringUtils.defaultString(_param._semestername) + "　" + StringUtils.defaultString(_param._testitemname) + "　欠席者一覧表"); // タイトル
                    svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._loginDate)); // 
                    //svf.VrsOut("PAGE", null); // ページ
                    svf.VrsOut("GRADE_COURSE", StringUtils.defaultString(courseGroup._gradeName1) + "　" + StringUtils.defaultString(courseGroup._groupName)); // 学年コース名
                    
                    for (int sti = 0; sti < targetStudentListp.size(); sti++) {
                        final Student student = (Student) targetStudentListp.get(sti);
                        final int line = sti + 1;
                        svf.VrsOutn("NO", line, String.valueOf(maxStudent * stpi + line)); // 番号
                        svf.VrsOutn("HR_NAME", line, student.getHrnameAttendno()); // クラス名番号
                        svf.VrsOutn("NAME", line, student._name); // 氏名
                    }

                    if (subpi == subclassListList.size() - 1) {
                        for (int sti = 0; sti < targetStudentListp.size(); sti++) {
                            final Student student = (Student) targetStudentListp.get(sti);
                            final int line = sti + 1;
                            
                            int hisshu = 0;
                            int sentaku = 0;
                            int gokei = 0;
                            for (final Iterator it = subclassListAll.iterator(); it.hasNext();) {
                                final Subclass subclass = (Subclass) it.next();
                                final boolean kesshi = student.isKesshi(subclass._subclasscd);
                                if (kesshi) {
                                    if ("3".equals(subclass._requireFlg)) {
                                        sentaku += 1;
                                    } else {
                                        hisshu += 1;
                                    }
                                    gokei += 1;
                                }
                            }
                            if (hisshu > 0) {
                                svf.VrsOutn("MUST_SUBTOTAL", line, String.valueOf(hisshu)); // 必修小計
                            }
                            if (sentaku > 0) {
                                svf.VrsOutn("SLELCT_SUBTOTAL", line, String.valueOf(sentaku)); // 選択小計
                            }
                            if (gokei > 0) {
                                svf.VrsOutn("MUST_SELECT_SUBTOTAL", line, String.valueOf(gokei)); // 必修選択小計
                            }
                            //svf.VrsOut("REMARK_TOTAL", null); // 備考
                        }
                        
                        if (stpi == studentListList.size() - 1) {
                            int hisshuTotal = 0;
                            int sentakuTotal = 0;
                            int gokeiTotal = 0;
                            for (int sti = 0; sti < targetStudentList.size(); sti++) {
                                final Student student = (Student) targetStudentList.get(sti);
                                
                                int hisshu = 0;
                                int sentaku = 0;
                                int gokei = 0;
                                for (final Iterator it = subclassListAll.iterator(); it.hasNext();) {
                                    final Subclass subclass = (Subclass) it.next();
                                    final boolean kesshi = student.isKesshi(subclass._subclasscd);
                                    if (kesshi) {
                                        if ("3".equals(subclass._requireFlg)) {
                                            sentaku += 1;
                                        } else {
                                            hisshu += 1;
                                        }
                                        gokei += 1;
                                    }
                                }
                                hisshuTotal += hisshu;
                                sentakuTotal += sentaku;
                                gokeiTotal += gokei;
                            }
                            if (hisshuTotal > 0) {
                                svf.VrsOut("MUST_TOTAL", String.valueOf(hisshuTotal)); // 必修小計
                            }
                            if (sentakuTotal > 0) {
                                svf.VrsOut("SLELCT_TOTAL", String.valueOf(sentakuTotal)); // 選択小計
                            }
                            if (gokeiTotal > 0) {
                                svf.VrsOut("MUST_SELECT_TOTAL", String.valueOf(gokeiTotal)); // 必修選択小計
                            }
                        }
                    }

                    for (final Iterator it = subclassList.iterator(); it.hasNext();) {
                        final Subclass subclass = (Subclass) it.next();

                        svf.VrsOut("SELECT_MARK", "3".equals(subclass._requireFlg) ? "＊" : ""); // 選択科目表示
                        svf.VrsOut("SUBCLASS_NAME", null != subclass._subclassabbv && subclass._subclassabbv.length() > 3 ? subclass._subclassabbv.substring(0, 3) : subclass._subclassabbv); // 科目名

                        for (int sti = 0; sti < targetStudentListp.size(); sti++) {
                            final Student student = (Student) targetStudentListp.get(sti);
                            final int line = sti + 1;

                            final boolean kesshi = student.isKesshi(subclass._subclasscd);
                            if (kesshi) {
                                svf.VrsOutn("LOST", line, "●"); // 欠試
                            }
                            _hasData = true;
                        }
                        
                        if (stpi == studentListList.size() - 1) { // 科目ごとの最後のページ
                            int subclassTotal = 0;
                            for (int sti = 0; sti < targetStudentList.size(); sti++) {
                                final Student student = (Student) targetStudentList.get(sti);
                                final boolean kesshi = student.isKesshi(subclass._subclasscd);
                                if (kesshi) {
                                    subclassTotal += 1;
                                }
                            }
                            if (subclassTotal > 0) {
                                svf.VrsOut("SUBCLASS_TOTAL", String.valueOf(subclassTotal)); // 科目合計
                            }
                        }

                        svf.VrEndRecord();
                    }

                    for (int j = subclassList.size(); j < maxSubclass; j++) {
                        svf.VrEndRecord();
                    }
                }
            }
        }
    }
    
    private List getGroupList(final List list, final int max) {
        final List rtn = new ArrayList();
        List current = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Object o = it.next();
            if (null == current || current.size() >= max) {
                current = new ArrayList();
                rtn.add(current);
            }
            current.add(o);
        }
        return rtn;
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
        final String _hrName;
        final String _hrClassName1;
        final String _attendno;
        final String _schregno;
        final String _name;
        final Map _subclassMap = new HashMap();
        final Map _valueDiMap = new HashMap();

        Student(
            final String grade,
            final String hrClass,
            final String hrName,
            final String hrClassName1,
            final String attendno,
            final String schregno,
            final String name
        ) {
            _grade = grade;
            _hrClass = hrClass;
            _hrName = hrName;
            _hrClassName1 = hrClassName1;
            _attendno = attendno;
            _schregno = schregno;
            _name = name;
        }
        
        public String getHrnameAttendno() {
            return StringUtils.defaultString(_hrClassName1) + "-" + (NumberUtils.isDigits(_attendno) ? String.valueOf(Integer.parseInt(_attendno)) : _attendno);
        }

        public boolean isKesshi(final String subclasscd) {
            final String s = (String) _valueDiMap.get(subclasscd);
            if (null != s) {
                return true;
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

    private static class CourseGroup {
        final String _gradeName1;
        final String _groupCd;
        final String _groupName;
        final List _studentList;

        CourseGroup(
            final String gradeName1,
            final String groupCd,
            final String groupName
        ) {
            _gradeName1 = gradeName1;
            _groupCd = groupCd;
            _groupName = groupName;
            _studentList = new ArrayList();
        }

        public static List getCourseGroupList(final DB2UDB db2, final Param param) {
            final List courseGroupList = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("   GDAT.GRADE_NAME1, ");
                stb.append("   CGRP.GROUP_CD, ");
                stb.append("   CGRPH.GROUP_NAME, ");
                stb.append("   T1.GRADE, ");
                stb.append("   T1.HR_CLASS, ");
                stb.append("   HDAT.HR_NAME, ");
                stb.append("   HDAT.HR_CLASS_NAME1, ");
                stb.append("   T1.ATTENDNO, ");
                stb.append("   T1.SCHREGNO, ");
                stb.append("   BASE.NAME, ");
                stb.append("   T2.CHAIRCD, ");
                stb.append("   T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD AS SUBCLASSCD, ");
                stb.append("   SUBM.SUBCLASSNAME, ");
                stb.append("   SUBM.SUBCLASSABBV, ");
                stb.append("   CRE.REQUIRE_FLG, ");
                stb.append("   TREC.VALUE_DI ");
                stb.append(" FROM SCHREG_REGD_DAT T1 ");
                stb.append(" INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = T1.SCHREGNO ");
                stb.append(" INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR ");
                stb.append("     AND GDAT.GRADE = T1.GRADE ");
                stb.append(" INNER JOIN SCHREG_REGD_HDAT HDAT ON HDAT.YEAR = T1.YEAR ");
                stb.append("     AND HDAT.SEMESTER = T1.SEMESTER ");
                stb.append("     AND HDAT.GRADE = T1.GRADE ");
                stb.append("     AND HDAT.HR_CLASS = T1.HR_CLASS ");
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
                stb.append("     AND CGRP.GROUP_CD = '" + param._groupCd + "' ");
                stb.append("     AND T3.CLASSCD <= '90' ");
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
                    
                    final String schregno = rs.getString("SCHREGNO");
                    if (null == Student.getStudent(schregno, coursegroup._studentList)) {
                        final String grade = rs.getString("GRADE");
                        final String hrClass = rs.getString("HR_CLASS");
                        final String hrName = rs.getString("HR_NAME");
                        final String hrClassName1 = rs.getString("HR_CLASS_NAME1");
                        final String attendno = rs.getString("ATTENDNO");
                        final String name = rs.getString("NAME");
                        
                        final Student student = new Student(grade, hrClass, hrName, hrClassName1, attendno, schregno, name);
                        coursegroup._studentList.add(student);
                    }
                    
                    final Student student = Student.getStudent(schregno, coursegroup._studentList);
                    
                    if (null != rs.getString("SUBCLASSCD")) {
                        final String subclasscd = rs.getString("SUBCLASSCD");
                        final String subclassabbv = rs.getString("SUBCLASSABBV");
                        final String requireFlg = rs.getString("REQUIRE_FLG");
                        final String valueDi = rs.getString("VALUE_DI");
                        
                        final Subclass subclass = new Subclass(subclasscd, subclassabbv, requireFlg);
                        student._subclassMap.put(subclasscd, subclass);
                        
                        student._valueDiMap.put(subclasscd, valueDi);
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
        final String _loginDate;
        final String _testitemname;
        final String _semestername;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _grade = request.getParameter("GRADE");
            _testcd = request.getParameter("TESTKIND_ITEMCD");
            _groupCd = request.getParameter("GROUP_CD");
            _loginDate = request.getParameter("LOGIN_DATE");
            _testitemname = getTestitemname(db2);
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

        private String getTestitemname(final DB2UDB db2) {
            String sql = "";
            sql += " SELECT TESTITEMNAME FROM TESTITEM_MST_COUNTFLG_NEW_SDIV ";
            sql += " WHERE ";
            sql += "   YEAR = '" + _year + "' ";
            sql += "   AND SEMESTER = '" + _semester + "' ";
            sql += "   AND TESTKINDCD || TESTITEMCD || SCORE_DIV = '" + _testcd + "' ";
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString("TESTITEMNAME");
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

