/*
 * $Id: 09816dbddc09467421080ea2514ccf60da28d08f $
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

import servletpack.KNJZ.detail.KNJDefineCode;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

/**
 * 文京学園　赤点・注意点者一覧表
 */
public class KNJD673 {

    private static final Log log = LogFactory.getLog(KNJD673.class);

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
    
    private static String sishaGonyu(final BigDecimal bd) {
        return null == bd ? null : bd.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {

        final int maxSubclass = 25;
        final List courseGroupList = CourseGroup.getCourseGroupList(db2, _param);
        final Map avgDatMap = RecordAverageDat.getRecordAverageDatMap(db2, _param);
        
        for (int cgi = 0; cgi < courseGroupList.size(); cgi++) {
            final CourseGroup courseGroup = (CourseGroup) courseGroupList.get(cgi);
            
            log.debug(" courseGroup = " + courseGroup._groupCd);

            for (int hri = 0; hri < courseGroup._hrList.size(); hri++) {
                final HrClass hr = (HrClass) courseGroup._hrList.get(hri);

                final List targetStudentList = new ArrayList();
                final Map subclassMap = new TreeMap();
                for (int sti = 0; sti < hr._studentList.size(); sti++) {
                    final Student student = (Student) hr._studentList.get(sti);

                    for (final Iterator it = student._subclassMap.keySet().iterator(); it.hasNext();) {
                        final String subclasscd = (String) it.next();
                        if (null != student.getMark(subclasscd)) { // マークが1つ以上ある生徒が対象
                            targetStudentList.add(student);
                            subclassMap.putAll(student._subclassMap);
                            //log.debug(" schregno = " + student._schregno + ", subclasscds = " + student._subclassMap.keySet());
                            break;
                        }
                    }
                }
                
                if (targetStudentList.size() == 0) { // 対象者がない場合、表示しない
                    continue;
                }
                
                svf.VrSetForm("KNJD673.frm", 4);
                
                svf.VrsOut("TITLE", KenjaProperties.gengou(Integer.parseInt(_param._year)) + "年度　" + StringUtils.defaultString(_param._semestername) + "　" + StringUtils.defaultString(_param._testitemname) + "　赤点・注意点者一覧表（" + ("2".equals(_param._output) ? "累積別" : "考査別")  + "）"); // タイトル
                svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._loginDate)); // 
                svf.VrsOut("GRADE_COURSE", courseGroup._groupName); // 学年コース名
                svf.VrsOut("HR_NAME", hr._hrName); // 年組
                svf.VrsOut("TEACHER_NAME", hr._staffname); // 担任名

                final List subclassList = new ArrayList(subclassMap.values());
                Collections.sort(subclassList);
                
                final List hishuKeiTotal = new ArrayList();
                final List sentakuKeiTotal = new ArrayList();
                final List gouKeiTotal = new ArrayList();

                for (int sti = 0; sti < targetStudentList.size(); sti++) {
                    final Student student = (Student) targetStudentList.get(sti);
                    final int line = sti + 1;
                    svf.VrsOutn("NO", line, NumberUtils.isDigits(student._attendno) ? String.valueOf(Integer.parseInt(student._attendno)) : student._attendno); // 番号
                    svf.VrsOutn("NAME", line, student._name); // 氏名

                    final List hishuKei = new ArrayList();
                    final List sentakuKei = new ArrayList();
                    final List gouKei = new ArrayList();

                    for (final Iterator it = subclassList.iterator(); it.hasNext();) {
                        final Subclass subclass = (Subclass) it.next();
                        
                        final String mark = student.getMark(subclass._subclasscd);
                        if (null != mark) {
                            if ("3".equals(subclass._requireFlg)) {
                                sentakuKei.add(subclass._subclasscd);
                            } else {
                                hishuKei.add(subclass._subclasscd);
                            }
                            gouKei.add(subclass._subclasscd);
                        }
                    }
                    if (hishuKei.size() > 0) {
                        svf.VrsOutn("MUST_SUBTOTAL", line, String.valueOf(hishuKei.size())); // 必修小計
                    }
                    if (sentakuKei.size() > 0) {
                        svf.VrsOutn("SLELCT_SUBTOTAL", line, String.valueOf(sentakuKei.size())); // 選択小計
                    }
                    if (gouKei.size() > 0) {
                        svf.VrsOutn("MUST_SELECT_SUBTOTAL", line, String.valueOf(gouKei.size())); // 必修選択小計
                    }
                    //svf.VrsOutn("REMARK_SUBTOTAL", line, null); // 備考
                    
                    hishuKeiTotal.addAll(hishuKei);
                    sentakuKeiTotal.addAll(sentakuKei);
                    gouKeiTotal.addAll(gouKei);
                }
                if (hishuKeiTotal.size() > 0) {
                    svf.VrsOut("MUST_TOTAL", String.valueOf(hishuKeiTotal.size())); // 必修小計
                }
                if (sentakuKeiTotal.size() > 0) {
                    svf.VrsOut("SLELCT_TOTAL", String.valueOf(sentakuKeiTotal.size())); // 選択小計
                }
                if (gouKeiTotal.size() > 0) {
                    svf.VrsOut("MUST_SELECT_TOTAL", String.valueOf(gouKeiTotal.size())); // 必修選択小計
                }
                //svf.VrsOut("REMARK_TOTAL", null); // 備考

                svf.VrsOut("AVE_NAME", "コースグループ平均"); // 平均名称
                svf.VrsOut("MARK_NAME", "●：赤点者　　▲：注意点者　　欠：欠時数オーバー者　　◎：赤点者＋欠時数オーバー者"); // 記号名称
                
                for (final Iterator it = subclassList.iterator(); it.hasNext();) {
                    final Subclass subclass = (Subclass) it.next();
                    
                    svf.VrsOut("SELECT_MARK", "3".equals(subclass._requireFlg) ? "＊" : ""); // 選択科目表示
                    svf.VrsOut("SUBCLASS_NAME", null != subclass._subclassabbv && subclass._subclassabbv.length() > 3 ? subclass._subclassabbv.substring(0, 3) : subclass._subclassabbv); // 科目名
                    
                    for (int sti = 0; sti < targetStudentList.size(); sti++) {
                        final Student student = (Student) targetStudentList.get(sti);
                        final int line = sti + 1;
                        
                        final String mark = student.getMark(subclass._subclasscd);
                        svf.VrsOutn("MARK", line, mark); // 記号
                        Score s = (Score) student._scoreMap.get(subclass._subclasscd);
                        if (null != s) {
                            svf.VrsOutn("LOST", line, s._score); // 欠試
                        }
                    }
                    
                    final RecordAverageDat courseGroupAvg = RecordAverageDat.get(avgDatMap, RecordAverageDat.getCourseGroupAvgDivKey(_param._grade, courseGroup._groupCd), subclass._subclasscd);
                    if (null != courseGroupAvg) {
                        svf.VrsOut("SUBCLASS_TOTAL", sishaGonyu(courseGroupAvg._avg)); // 科目合計
                    }
                    _hasData = true;
                    svf.VrEndRecord();
                }
                
                for (int i = subclassList.size(); i < maxSubclass; i++) {
                    svf.VrEndRecord();
                }
            }
            
        }
    }
    
    private static Map getMappedMap(final Map map, final String key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new TreeMap());
        }
        return (Map) map.get(key1);
    }

    private static class Score {
        final String _subclasscd;
        String _score;
        final Integer _scoreLine;
        final Integer _assesslevel;

        Score(
                final String subclasscd,
                final String score,
                final Integer scoreLine,
                final Integer assesslevel) {
            _subclasscd = subclasscd;
            _score = score;
            _scoreLine = scoreLine;
            _assesslevel = assesslevel;
        }
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
        final Map _scoreMap = new HashMap();
        final List _kekkaOverSubclasscdList = new ArrayList();

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
        
        public String getMark(final String subclasscd) {
            boolean isAkaten = false;
            boolean isTyui = false;
            Score s = (Score) _scoreMap.get(subclasscd);
            if (null != s) {
                if (null != s._assesslevel && s._assesslevel.intValue() == 1) {
                    isAkaten = true;
                }
                if (null == s._assesslevel && NumberUtils.isDigits(s._score) && null != s._scoreLine && Integer.parseInt(s._score) < s._scoreLine.intValue()) {
                    // 得点が、ライン点未満
                    isAkaten = true;
                }
                if (!isAkaten && null != s._assesslevel && s._assesslevel.intValue() == 2) {
                    isTyui = true;
                }
            }
            final boolean isKetujiOver = _kekkaOverSubclasscdList.contains(subclasscd);

            final String mark;
            if (isAkaten) {
                if (isKetujiOver) {
                    // 赤点かつ欠時オーバー
                    mark = "◎";
                } else {
                    // 赤点
                    mark = "●";
                }
            } else if (isTyui) {
                // 注意
                mark = "▲";
            } else if (isKetujiOver) {
                // 欠時オーバー
                mark = "欠";
            } else {
                mark = null;
            }
            return mark;
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
            stb.append("   SUBM.SUBCLASSNAME, ");
            stb.append("   SUBM.SUBCLASSABBV, ");
            stb.append("   CRE.REQUIRE_FLG, ");
            stb.append("   TRANK.SCORE, ");
            stb.append("   TPS.SCORE_LINE, ");
            stb.append("   ASLV.ASSESSLEVEL ");
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
            if ("2".equals(param._output)) {
                stb.append(" LEFT JOIN RECORD_RANK_RUIKEI_SDIV_DAT TRANK ON TRANK.YEAR = T3.YEAR ");
            } else {
                stb.append(" LEFT JOIN RECORD_RANK_SDIV_DAT TRANK ON TRANK.YEAR = T3.YEAR ");
            }
            stb.append("     AND TRANK.SEMESTER = '" + param._semester + "' ");
            stb.append("     AND TRANK.TESTKINDCD || TRANK.TESTITEMCD || TRANK.SCORE_DIV = '" + param._testcd + "' ");
            stb.append("     AND TRANK.CLASSCD = T3.CLASSCD ");
            stb.append("     AND TRANK.SCHOOL_KIND = T3.SCHOOL_KIND ");
            stb.append("     AND TRANK.CURRICULUM_CD = T3.CURRICULUM_CD ");
            stb.append("     AND TRANK.SUBCLASSCD = T3.SUBCLASSCD ");
            stb.append("     AND TRANK.SCHREGNO = T1.SCHREGNO ");
            stb.append(" LEFT JOIN PASS_SCORE_DAT TPS ON TPS.YEAR = T3.YEAR ");
            stb.append("     AND TPS.SEMESTER = '" + param._semester + "' ");
            stb.append("     AND TPS.TESTKINDCD || TPS.TESTITEMCD || TPS.SCORE_DIV = '" + param._testcd + "' ");
            if ("2".equals(param._output)) {
//                if ("9".equals(param._semester)) {
//                    stb.append("     AND TPS.RUISEKI_DIV = '3' ");
//                } else {
//                    stb.append("     AND TPS.RUISEKI_DIV = '2' ");
//                }
                stb.append("     AND TPS.RUISEKI_DIV = '3' ");
            } else {
                stb.append("     AND TPS.RUISEKI_DIV = '1' ");
            }
            stb.append("     AND TPS.CLASSCD = T3.CLASSCD ");
            stb.append("     AND TPS.SCHOOL_KIND = T3.SCHOOL_KIND ");
            stb.append("     AND TPS.CURRICULUM_CD = T3.CURRICULUM_CD ");
            stb.append("     AND TPS.SUBCLASSCD = T3.SUBCLASSCD ");
            stb.append("     AND TPS.PASS_DIV = '5' ");
            stb.append("     AND TPS.GRADE = T1.GRADE ");
            stb.append("     AND TPS.HR_CLASS = '000' ");
            stb.append("     AND TPS.COURSECD = '0' ");
            stb.append("     AND TPS.MAJORCD = CGRP.GROUP_CD ");
            stb.append("     AND TPS.COURSECODE = '0000' ");
            stb.append(" LEFT JOIN ASSESS_LEVEL_SDIV_MST ASLV ON ASLV.YEAR = T3.YEAR ");
            stb.append("     AND ASLV.SEMESTER = '" + param._semester + "' ");
            stb.append("     AND ASLV.TESTKINDCD || ASLV.TESTITEMCD || ASLV.SCORE_DIV = '" + param._testcd + "' ");
            if ("2".equals(param._output)) {
//                if ("9".equals(param._semester)) {
//                    stb.append("     AND ASLV.RUISEKI_DIV = '3' ");
//                } else {
//                    stb.append("     AND ASLV.RUISEKI_DIV = '2' ");
//                }
                stb.append("     AND ASLV.RUISEKI_DIV = '3' ");
            } else {
                stb.append("     AND ASLV.RUISEKI_DIV = '1' ");
            }
            stb.append("     AND ASLV.CLASSCD = T3.CLASSCD ");
            stb.append("     AND ASLV.SCHOOL_KIND = T3.SCHOOL_KIND ");
            stb.append("     AND ASLV.CURRICULUM_CD = T3.CURRICULUM_CD ");
            stb.append("     AND ASLV.SUBCLASSCD = T3.SUBCLASSCD ");
            stb.append("     AND ASLV.DIV = '5' ");
            stb.append("     AND ASLV.GRADE = T1.GRADE ");
            stb.append("     AND ASLV.HR_CLASS = '000' ");
            stb.append("     AND ASLV.COURSECD = '0' ");
            stb.append("     AND ASLV.MAJORCD = CGRP.GROUP_CD ");
            stb.append("     AND ASLV.COURSECODE = '0000' ");
            stb.append("     AND TRANK.SCORE BETWEEN ASLV.ASSESSLOW AND ASLV.ASSESSHIGH ");
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
            
            final Map studentMap = new HashMap();
            try {
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
                        final String score = rs.getString("SCORE");
                        final String scoreLine = rs.getString("SCORE_LINE");
                        final String assesslevel = rs.getString("ASSESSLEVEL");
                        
                        final Subclass subclass = new Subclass(subclasscd, subclassabbv, requireFlg);
                        student._subclassMap.put(subclasscd, subclass);
                        final Score s = new Score(subclasscd, score, toInteger(scoreLine), toInteger(assesslevel));
                        student._scoreMap.put(subclasscd, s);
                    }
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            
            try {
                final StringBuffer scstb = new StringBuffer();
                scstb.append(" SELECT ");
                scstb.append("   T2.SCHREGNO, ");
                scstb.append("   T2.CHAIRCD, ");
                scstb.append("   T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD AS SUBCLASSCD, ");
                scstb.append("   TREC.SEMESTER, ");
                scstb.append("   TREC.TESTKINDCD || TREC.TESTITEMCD || TREC.SCORE_DIV AS TESTCD, ");
                scstb.append("   TREC.VALUE_DI ");
                scstb.append(" FROM CHAIR_STD_DAT T2 ");
                scstb.append(" INNER JOIN CHAIR_DAT T3 ON T3.YEAR = T2.YEAR ");
                scstb.append("     AND T3.SEMESTER = T2.SEMESTER ");
                scstb.append("     AND T3.CHAIRCD = T2.CHAIRCD ");
                scstb.append("     AND T3.CLASSCD <= '90' ");
                scstb.append(" INNER JOIN RECORD_SCORE_DAT TREC ON TREC.YEAR = T3.YEAR ");
                if ("2".equals(param._output)) {
                    scstb.append("     AND TREC.SEMESTER = '" + param._semester + "' AND TREC.TESTKINDCD || TREC.TESTITEMCD || TREC.SCORE_DIV <= '" + param._testcd + "' ");
                } else {
                    scstb.append("     AND TREC.SEMESTER = '" + param._semester + "' AND TREC.TESTKINDCD || TREC.TESTITEMCD = '" + param._testcd.substring(0, 4) + "' ");
                }
                scstb.append("     AND TREC.SCORE_DIV = '01' ");
                scstb.append("     AND TREC.CLASSCD = T3.CLASSCD ");
                scstb.append("     AND TREC.SCHOOL_KIND = T3.SCHOOL_KIND ");
                scstb.append("     AND TREC.CURRICULUM_CD = T3.CURRICULUM_CD ");
                scstb.append("     AND TREC.SUBCLASSCD = T3.SUBCLASSCD ");
                scstb.append("     AND TREC.SCHREGNO = T2.SCHREGNO ");
                scstb.append("     AND TREC.VALUE_DI = '*' ");
                scstb.append(" WHERE ");
                scstb.append("     T2.YEAR = '" + param._year + "' ");

                final String sql = scstb.toString();
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final Student student = (Student) studentMap.get(rs.getString("SCHREGNO"));
                    if (null == student) {
                        continue;
                    }
                    final Score s = (Score) student._scoreMap.get(rs.getString("SUBCLASSCD"));
                    if (null == s) {
                        continue;
                    }
                    s._score = "*"; // 1個でも欠試があれば"*"
                    log.info(" schregno = " + student._schregno + " , " + s._subclasscd + ", " + s._score);
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            
            try {
                final StringBuffer scstb = new StringBuffer();
                scstb.append(" SELECT ");
                scstb.append("   T2.SCHREGNO, ");
                scstb.append("   T2.CHAIRCD, ");
                scstb.append("   T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD AS SUBCLASSCD, ");
                scstb.append("   TSUP.SEMESTER, ");
                scstb.append("   TSUP.TESTKINDCD || TSUP.TESTITEMCD || TSUP.SCORE_DIV AS TESTCD, ");
                scstb.append("   TSUP.SCORE ");
                scstb.append(" FROM CHAIR_STD_DAT T2 ");
                scstb.append(" INNER JOIN CHAIR_DAT T3 ON T3.YEAR = T2.YEAR ");
                scstb.append("     AND T3.SEMESTER = T2.SEMESTER ");
                scstb.append("     AND T3.CHAIRCD = T2.CHAIRCD ");
                scstb.append("     AND T3.CLASSCD <= '90' ");
                scstb.append(" INNER JOIN SUPP_EXA_SDIV_DAT TSUP ON TSUP.YEAR = T3.YEAR ");
                scstb.append("     AND TSUP.SEMESTER = '" + param._semester + "' AND TSUP.TESTKINDCD || TSUP.TESTITEMCD || TSUP.SCORE_DIV = '" + param._testcd + "' ");
                scstb.append("     AND TSUP.CLASSCD = T3.CLASSCD ");
                scstb.append("     AND TSUP.SCHOOL_KIND = T3.SCHOOL_KIND ");
                scstb.append("     AND TSUP.CURRICULUM_CD = T3.CURRICULUM_CD ");
                scstb.append("     AND TSUP.SUBCLASSCD = T3.SUBCLASSCD ");
                scstb.append("     AND TSUP.SCHREGNO = T2.SCHREGNO ");
                scstb.append("     AND TSUP.SCORE IS NOT NULL ");
                scstb.append(" WHERE ");
                scstb.append("     T2.YEAR = '" + param._year + "' ");

                final String sql = scstb.toString();
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final Student student = (Student) studentMap.get(rs.getString("SCHREGNO"));
                    if (null == student) {
                        continue;
                    }
                    final Score s = (Score) student._scoreMap.get(rs.getString("SUBCLASSCD"));
                    if (null == s) {
                        continue;
                    }
                    s._score = rs.getString("SCORE");
                    log.info(" supp schregno = " + student._schregno + " , " + s._subclasscd + ", " + s._score);
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
        
        private static void loadAttendSubclass(
                final DB2UDB db2,
                final Param param,
                final Map studentMap
        ) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final Map paramMap = new HashMap();
                paramMap.put("useCurriculumcd", "1");
                paramMap.put("useVirus", param._useVirus);
                paramMap.put("useKoudome", param._useKoudome);
                paramMap.put("useTestCountflg", "TESTITEM_MST_COUNTFLG_NEW_SDIV");
                paramMap.put("DB2UDB", db2);
                paramMap.put("absenceDiv", "2");

                final String sql = AttendAccumulate.getAttendSubclassSql(
                        param._semesFlg,
                        param._definecode,
                        param._knjSchoolMst,
                        param._year,
                        param.SSEMESTER,
                        param._semester,
                        (String) param._hasuuMap.get("attendSemesInState"),
                        param._periodInState,
                        (String) param._hasuuMap.get("befDayFrom"),
                        (String) param._hasuuMap.get("befDayTo"),
                        (String) param._hasuuMap.get("aftDayFrom"),
                        (String) param._hasuuMap.get("aftDayTo"),
                        null,
                        null,
                        "?",
                        paramMap
                        );
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
                        if (!NumberUtils.isNumber(rs.getString("SICK2")) || !NumberUtils.isNumber(rs.getString("ABSENCE_HIGH"))) {
                            continue;
                        }
                        final BigDecimal sick2 = rs.getBigDecimal("SICK2");
                        final BigDecimal absenceHigh = rs.getBigDecimal("ABSENCE_HIGH");
                        if (0 < sick2.compareTo(absenceHigh)) {
                            student._kekkaOverSubclasscdList.add(rs.getString("SUBCLASSCD"));
                        }
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
        
        private static Integer toInteger(final String v) {
            if (NumberUtils.isDigits(v)) {
                return Integer.valueOf(v);
            }
            return null;
        }

    }

    private static class RecordAverageDat {
        final String _subclasscd;
        final String _avgDivKey;
        final String _score;
        final String _highscore;
        final String _lowscore;
        final String _count;
        final BigDecimal _avg;
        final BigDecimal _stddev;

        RecordAverageDat(
            final String subclasscd,
            final String avgDivKey,
            final String score,
            final String highscore,
            final String lowscore,
            final String count,
            final BigDecimal avg,
            final BigDecimal stddev
        ) {
            _subclasscd = subclasscd;
            _avgDivKey = avgDivKey;
            _score = score;
            _highscore = highscore;
            _lowscore = lowscore;
            _count = count;
            _avg = avg;
            _stddev = stddev;
        }
        
        public static Map getSubclassMap(final Map avgDatMap, final String avgDivKey) {
            return getMappedMap(avgDatMap, avgDivKey);
        }
        
        public static RecordAverageDat get(final Map avgDatMap, final String avgDivKey, final String subclasscd) {
            return (RecordAverageDat) getSubclassMap(avgDatMap, avgDivKey).get(subclasscd);
        }
        
        public static String getGradeAvgDivKey(final String grade) {
            return "1" + "-" + grade + "-" + "000" + "-" + "00000000";
        }
        
        public static String getHrAvgDivKey(final String grade, final String hrClass) {
            return "2" + "-" + grade + "-" + hrClass + "-" + "00000000";
        }
        
        public static String getCourseAvgDivKey(final String grade, final String coursecd, final String majorcd, final String coursecode) {
            return "3" + "-" + grade + "-" + "000" + "-" + coursecd + majorcd + coursecode;
        }
        
        public static String getCourseGroupAvgDivKey(final String grade, final String coursegroupCd) {
            return "5" + "-" + grade + "-" + "000" + "-" + "0" + coursegroupCd + "0000";
        }

        public static Map getRecordAverageDatMap(final DB2UDB db2, final Param param) {
            final Map map = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT  ");
                stb.append("  CLASSCD, ");
                stb.append("  SCHOOL_KIND, ");
                stb.append("  CURRICULUM_CD, ");
                stb.append("  SUBCLASSCD, ");
                stb.append("  AVG_DIV || '-' || GRADE || '-' || HR_CLASS || '-' || COURSECD || MAJORCD || COURSECODE AS AVG_DIV_KEY, ");
                stb.append("  SCORE, ");
                stb.append("  HIGHSCORE, ");
                stb.append("  LOWSCORE, ");
                stb.append("  COUNT, ");
                stb.append("  AVG, ");
                stb.append("  STDDEV ");
                stb.append(" FROM  ");
                if ("2".equals(param._output)) {
                    stb.append("  RECORD_AVERAGE_RUIKEI_SDIV_DAT  ");
                } else {
                    stb.append("  RECORD_AVERAGE_SDIV_DAT  ");
                }
                stb.append(" WHERE  ");
                stb.append("  YEAR = '" + param._year + "' ");
                stb.append("  AND SEMESTER || TESTKINDCD || TESTITEMCD || SCORE_DIV= '" + param._semester + param._testcd + "' ");

                final String sql = stb.toString();
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String subclasscd;
                    if ("333333".equals(rs.getString("SUBCLASSCD")) || "555555".equals(rs.getString("SUBCLASSCD")) || "999999".equals(rs.getString("SUBCLASSCD")) || "777777".equals(rs.getString("SUBCLASSCD")) || "888888".equals(rs.getString("SUBCLASSCD")) ) {
                        subclasscd = rs.getString("SUBCLASSCD");
                    } else {
                        subclasscd = rs.getString("CLASSCD") + "-" + rs.getString("SCHOOL_KIND") + "-" + rs.getString("CURRICULUM_CD") + "-" + rs.getString("SUBCLASSCD");
                    }
                    final String avgDivKey = rs.getString("AVG_DIV_KEY");
                    final String score = rs.getString("SCORE");
                    final String highscore = rs.getString("HIGHSCORE");
                    final String lowscore = rs.getString("LOWSCORE");
                    final String count = rs.getString("COUNT");
                    final BigDecimal avg = rs.getBigDecimal("AVG");
                    final BigDecimal stddev = rs.getBigDecimal("STDDEV");
                    final RecordAverageDat recordaveragedat = new RecordAverageDat(subclasscd, avgDivKey, score, highscore, lowscore, count, avg, stddev);
                    getMappedMap(map, avgDivKey).put(subclasscd, recordaveragedat);
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return map;
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
        final String _testcd;
        final String _groupCd;
        final String _loginDate;
        final String _output;
        final String _testitemname;
        final String _edate;
        final String _semestername;
        
        final String _useVirus;
        final String _useKoudome;
        
        private KNJDefineSchool _definecode;  //各学校における定数等設定のクラス
        private KNJSchoolMst _knjSchoolMst;
        
        /** 端数計算共通メソッド引数 */
        private String _periodInState;
        private Map _attendSemesMap;
        private Map _hasuuMap;
        private boolean _semesFlg;
        private String _sDate;
        private final String SSEMESTER = "1";


        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _grade = request.getParameter("GRADE");
            _gradeHrclass = request.getParameter("GRADE_HR_CLASS");
            _testcd = request.getParameter("TESTKIND_ITEMCD");
            _groupCd = request.getParameter("GROUP_CD");
            _loginDate = request.getParameter("LOGIN_DATE");
            _output = request.getParameter("OUTPUT");

            _testitemname = getTestitem(db2, "TESTITEMNAME");
            String edate = getTestitem(db2, "EDATE");
            if (null == edate) {
                _edate = _loginDate;
                log.warn(" 出欠集計日付がnull -> " + _loginDate);
            } else {
                _edate = edate;
            }
            _semestername = getSemestername(db2);
            
            _useVirus = request.getParameter("useVirus");
            _useKoudome = request.getParameter("useKoudome");
            try {
                _knjSchoolMst = new KNJSchoolMst(db2, _year);
            } catch (SQLException e) {
                log.warn("学校マスタ取得でエラー", e);
            }
            loadAttendSemesArgument(db2);
            _definecode = createDefineCode(db2);
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
        
        private KNJDefineCode setClasscode0(final DB2UDB db2, final String year) {
            KNJDefineCode definecode = null;
            try {
                definecode = new KNJDefineCode();
                definecode.defineCode(db2, year);         //各学校における定数等設定
            } catch (Exception ex) {
                log.warn("semesterdiv-get error!", ex);
            }
            return definecode;
        }

        /**
         * 名称マスタ NAMECD1='Z010' NAMECD2='00'読込
         */
        private String setZ010Name1(DB2UDB db2) {
            String name1 = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement("SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' ");
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

        private void loadAttendSemesArgument(DB2UDB db2) {
            
            try {
                loadSemester(db2, _year);
                // 出欠の情報
                final KNJDefineCode definecode0 = setClasscode0(db2, _year);
                final String z010Name1 = setZ010Name1(db2);
                _periodInState = AttendAccumulate.getPeiodValue(db2, definecode0, _year, SSEMESTER, _semester);
                _attendSemesMap = AttendAccumulate.getAttendSemesMap(db2, z010Name1, _year);
                _hasuuMap = AttendAccumulate.getHasuuMap(_attendSemesMap, _sDate, _edate); // _sDate: 年度開始日, _date: LOGIN_DATE
                _semesFlg = ((Boolean) _hasuuMap.get("semesFlg")).booleanValue();
            } catch (Exception e) {
                log.debug("loadAttendSemesArgument exception", e);
            }
        }
        
        /**
         * 年度の開始日を取得する 
         */
        private void loadSemester(final DB2UDB db2, String year) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map map = new HashMap();
            final List list = new ArrayList();
            try {
                ps = db2.prepareStatement(sqlSemester(year));
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String semester = rs.getString("SEMESTER");
                    final String name = rs.getString("SEMESTERNAME");
                    map.put(semester, name);

                    final String sDate = rs.getString("SDATE");
                    list.add(sDate);
                }
            } catch (final Exception ex) {
                log.error("テスト項目のロードでエラー", ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }

            if (!list.isEmpty()) {
                _sDate = (String) list.get(0);
            }
            log.debug("年度の開始日=" + _sDate);
        }
            
        private String sqlSemester(String year) {
            final String sql;
            sql = "select"
                + "   SEMESTER,"
                + "   SEMESTERNAME,"
                + "   SDATE"
                + " from"
                + "   SEMESTER_MST"
                + " where"
                + "   YEAR='" + year + "'"
                + " order by SEMESTER"
            ;
            return sql;
        }    
        
        private String getSchoolName(final DB2UDB db2) {
            String schoolName = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1='Z010' AND NAMECD2='00' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    schoolName = rs.getString("NAME1");
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return schoolName;
        }
    }
}

// eof

