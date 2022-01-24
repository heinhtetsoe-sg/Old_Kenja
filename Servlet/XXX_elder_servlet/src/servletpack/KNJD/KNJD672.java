/*
 * $Id: 680b5333e71ea4e59d01678b6c8d899711fd474d $
 *
 * 作成日: 2015/08/05
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;


import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJServletUtils;

/**
 * 文京学園 得点通知表
 */
public class KNJD672 {

    private static final Log log = LogFactory.getLog(KNJD672.class);

    private boolean _hasData;
    
    private static String SUBCLASSCD_333333 = "333333";
    private static String SUBCLASSCD_555555 = "555555";
    private static String SUBCLASSCD_777777 = "777777";
    private static String SUBCLASSCD_888888 = "888888";
    private static String SUBCLASSCD_999999 = "999999";

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
    
    /**
     *  文字数を取得
     */
    private static int getMS932ByteLength(final String str)
    {
        int ret = 0;
        if (null != str) {
            try {
                ret = str.getBytes("MS932").length;                      //byte数を取得
            } catch (Exception ex) {
                log.error("retStringByteValue error!", ex);
            }
        }
        return ret;
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        final int MAX_SUBCLASS = 25;
        final String gengou = KNJ_EditDate.gengou(db2, Integer.parseInt(_param._year));

        final List studentList = Student.getStudentList(db2, _param);
        for (int sti = 0; sti < studentList.size(); sti++) {
            final Student student = (Student) studentList.get(sti);
            log.debug(" schregno = " + student._schregno);

            final String form = "KNJD672.frm"; 
            svf.VrSetForm(form, 4);
            
			svf.VrsOut("TITLE", gengou + "年度　" + StringUtils.defaultString(_param._semestername) + "　得点通知表"); // タイトル
            svf.VrsOut("GRADE_COURSE", StringUtils.defaultString(student._coursegroup._gradeName1) + "　" + StringUtils.defaultString(student._coursegroup._groupName)); // 学年コース名

            if (null == student._hr._substaffname) {
                svf.VrsOut("TEACHER_NAME2", "　　　担任　" + StringUtils.defaultString(student._hr._staffname)); // 担任名
                svf.VrsOut("TEACHER_NAME_IN2", "印");
                svf.VrsOut("TEACHER_NAME_MARU2", "○");
            } else {
                svf.VrsOut("TEACHER_NAME", "　　　担任　" + StringUtils.defaultString(student._hr._staffname)); // 担任名
                svf.VrsOut("TEACHER_NAME_IN1", "印");
                svf.VrsOut("TEACHER_NAME_MARU1", "○");
                
                svf.VrsOut("TEACHER_NAME2", "　　副担任　" + StringUtils.defaultString(student._hr._substaffname)); // 担任名
                svf.VrsOut("TEACHER_NAME_IN2", "印");
                svf.VrsOut("TEACHER_NAME_MARU2", "○");
            }
            
            final String printAttendno = NumberUtils.isDigits(student._attendno) ? String.valueOf(Integer.parseInt(student._attendno)) + "番" : null != student._attendno ? student._attendno + "番" : "";
            svf.VrsOut("HR_NAME", StringUtils.defaultString(student._hr._hrName) + " " + printAttendno); // 年組
            svf.VrsOut("NAME", student._name); // 氏名

            for (int ti = 0; ti < _param._testitemList.size(); ti++) {
                final Testitem testitem = (Testitem) _param._testitemList.get(ti);
                
                if ("9".equals(testitem._semester)) {
                    svf.VrsOut("SEMESTER_NAME3", "学年総合"); // 学期名
                } else {
                    svf.VrsOut("SEMESTER_NAME" + testitem._semester, testitem._semestername); // 学期名
                }

                svf.VrsOut("TEST_NAME" + String.valueOf(ti + 1), testitem._testitemabbv1); // 考査名
                
                final String[] subclasscdTotal = {SUBCLASSCD_999999, SUBCLASSCD_777777};
                
                for (int si = 0; si < subclasscdTotal.length; si++) {
                    final String subclasscd = subclasscdTotal[si];
                    final int line = ti * 4 + si * 2;
                    
                    svf.VrsOutn("UP", line + 2, "↑"); // 上矢印
                    svf.VrsOutn("REMARK", line + 2, si == 0 ? "全科目" : si == 1 ? "必修のみ" : ""); // 備考
                    
                    if ((testitem._semester + testitem._testcd).compareTo(_param._semester + _param._testcd) > 0) {
                        continue;
                    }
                    final Map subclassScoreMap = getMappedMap(student._scoreMap, testitem._semester + testitem._testcd);
                    
                    final Score s = (Score) subclassScoreMap.get(subclasscd);
                    if (null != s) {
                        svf.VrsOutn("SUBTOTAL3", line + 1, s._score); // 合計
                        svf.VrsOutn("AVERAGE3", line + 1, sishaGonyu(s._avg)); // 平均
                        svf.VrsOutn("CLASS_RANK3", line + 1, toString(s._classAvgRank)); // クラス席次
                        svf.VrsOutn("COURSE_RANK3", line + 1, toString(s._courseAvgRank)); // コース席次
                    }
                }
            }

            final Map subclassMap = new HashMap(student._subclassMap);
            subclassMap.remove(SUBCLASSCD_333333);
            subclassMap.remove(SUBCLASSCD_555555);
            subclassMap.remove(SUBCLASSCD_777777);
            subclassMap.remove(SUBCLASSCD_888888);
            subclassMap.remove(SUBCLASSCD_999999);
            final List subclassList = new ArrayList(subclassMap.values());
            Collections.sort(subclassList);

            for (int subi = 0; subi < subclassList.size(); subi++) {
                final Subclass subclass = (Subclass) subclassList.get(subi);
                
                svf.VrsOut("SELECT_MARK", "3".equals(subclass._requireFlg) ? "＊" : ""); // 選択科目表示
                if (StringUtils.defaultString(subclass._subclassname).length() > 12) {
                    svf.VrsOut("SUBCLASS_NAME3_1", subclass._subclassname.substring(0, 7)); // 科目名
                    svf.VrsOut("SUBCLASS_NAME3_2", subclass._subclassname.substring(7)); // 科目名
                } else if (StringUtils.defaultString(subclass._subclassname).length() > 6) {
                    svf.VrsOut("SUBCLASS_NAME2_1", subclass._subclassname.substring(0, 6)); // 科目名
                    svf.VrsOut("SUBCLASS_NAME2_2", subclass._subclassname.substring(6)); // 科目名
                } else {
                    svf.VrsOut("SUBCLASS_NAME1", subclass._subclassname); // 科目名
                }
                
                for (int ti = 0; ti < _param._testitemList.size(); ti++) {
                    final Testitem testitem = (Testitem) _param._testitemList.get(ti);

                    final Map subclassScoreMap = getMappedMap(student._scoreMap, testitem._semester + testitem._testcd);
                    if (subclassScoreMap.isEmpty()) {
                        continue;
                    }
                    
                    final int line = ti * 4;

                    final Score s = (Score) subclassScoreMap.get(subclass._subclasscd);
                    if (null != s) {
                        svf.VrsOutn("SCORE", line + 1, s._score); // 素点
                        if ("H".equals(student._schoolKind)) {
                            svf.VrsOutn("SCORE", line + 2, s._kariHyotei); // 仮評定
                        }
                        svf.VrsOutn("SCORE", line + 3, toString(s._classAvgRank)); // クラス席次
                        svf.VrsOutn("SCORE", line + 4, toString(s._courseAvgRank)); // コース席次
                    }
                }
                svf.VrEndRecord();
            }
            
            for (int i = subclassList.size(); i < MAX_SUBCLASS; i++) {
                svf.VrEndRecord();
            }
            _hasData = true;
        }
    }
    
    private static String toString(final Object o) {
        if (null == o) {
            return null;
        }
        return o.toString();
    }
    
    private static String sishaGonyu(final BigDecimal bd) {
        return null == bd ? null : bd.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
    }
    
    private static Map getMappedMap(final Map map, final String key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new TreeMap());
        }
        return (Map) map.get(key1);
    }

    private static class Score {
        final String _chaircd;
        final String _subclasscd;
        final String _score;
        final BigDecimal _avg;
        final Integer _classRank;
        final Integer _classAvgRank;
        final Integer _courseRank;
        final Integer _courseAvgRank;
        final Integer _courseGroupRank;
        final Integer _courseGroupAvgRank;
        final String _kariHyotei;

        Score(
                final String chaircd,
                final String subclasscd,
                final String score,
                final BigDecimal avg,
                final Integer classRank,
                final Integer classAvgRank,
                final Integer courseRank,
                final Integer courseAvgRank,
                final Integer courseGroupRank,
                final Integer courseGroupAvgRank,
                final String kariHyotei) {
            _chaircd = chaircd;
            _subclasscd = subclasscd;
            _score = score;
            _avg = avg;
            _classRank = classRank;
            _classAvgRank = classAvgRank;
            _courseRank = courseRank;
            _courseAvgRank = courseAvgRank;
            _courseGroupRank = courseGroupRank;
            _courseGroupAvgRank = courseGroupAvgRank;
            _kariHyotei = kariHyotei;
        }
    }
    
    private static class Subclass implements Comparable {
        final String _subclasscd;
        final String _subclassname;
        final String _requireFlg;

        Subclass(
                final String subclasscd,
                final String subclassname,
                final String requireFlg) {
            _subclasscd = subclasscd;
            _subclassname = subclassname;
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
        final String _schoolKind;
        final String _hrClass;
        final String _attendno;
        final String _schregno;
        final String _name;
        final HrClass _hr;
        final CourseGroup _coursegroup;
        final Map _scoreMap = new HashMap();
        final Map _subclassMap = new HashMap();

        Student(
            final String grade,
            final String schoolKind,
            final String hrClass,
            final String attendno,
            final String schregno,
            final String name,
            final HrClass hr,
            final CourseGroup coursegroup) {
            _grade = grade;
            _schoolKind = schoolKind;
            _hrClass = hrClass;
            _attendno = attendno;
            _schregno = schregno;
            _name = name;
            _hr = hr;
            _coursegroup = coursegroup;
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
        
        public static List getStudentList(final DB2UDB db2, final Param param) {
            final List studentList = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" WITH REGD AS ( ");
                stb.append("     SELECT ");
                stb.append("       T1.YEAR, ");
                stb.append("       T1.SEMESTER, ");
                stb.append("       GDAT.GRADE_NAME1, ");
                stb.append("       CGRP.GROUP_CD, ");
                stb.append("       CGRPH.GROUP_NAME, ");
                stb.append("       T1.GRADE, ");
                stb.append("       GDAT.SCHOOL_KIND, ");
                stb.append("       T1.HR_CLASS, ");
                stb.append("       HDAT.HR_NAME, ");
                stb.append("       HRSTF.STAFFNAME, ");
                stb.append("       SUBHRSTF.STAFFNAME AS SUBSTAFFNAME, ");
                stb.append("       T1.ATTENDNO, ");
                stb.append("       T1.SCHREGNO, ");
                stb.append("       T1.COURSECD, ");
                stb.append("       T1.MAJORCD, ");
                stb.append("       T1.COURSECODE, ");
                stb.append("       BASE.NAME ");
                stb.append("     FROM SCHREG_REGD_DAT T1 ");
                stb.append("     INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = T1.SCHREGNO ");
                stb.append("     INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR ");
                stb.append("         AND GDAT.GRADE = T1.GRADE ");
                stb.append("     INNER JOIN SCHREG_REGD_HDAT HDAT ON HDAT.YEAR = T1.YEAR ");
                stb.append("         AND HDAT.SEMESTER = T1.SEMESTER ");
                stb.append("         AND HDAT.GRADE = T1.GRADE ");
                stb.append("         AND HDAT.HR_CLASS = T1.HR_CLASS ");
                stb.append("     INNER JOIN COURSE_GROUP_CD_DAT CGRP ON CGRP.YEAR = T1.YEAR ");
                stb.append("         AND CGRP.GRADE = T1.GRADE ");
                stb.append("         AND CGRP.COURSECD = T1.COURSECD ");
                stb.append("         AND CGRP.MAJORCD = T1.MAJORCD ");
                stb.append("         AND CGRP.COURSECODE = T1.COURSECODE ");
                stb.append("     INNER JOIN COURSE_GROUP_CD_HDAT CGRPH ON CGRPH.YEAR = CGRP.YEAR ");
                stb.append("         AND CGRPH.GRADE = CGRP.GRADE ");
                stb.append("         AND CGRPH.GROUP_CD = CGRP.GROUP_CD ");
                stb.append("     LEFT JOIN STAFF_MST HRSTF ON HRSTF.STAFFCD = HDAT.TR_CD1 ");
                stb.append("     LEFT JOIN STAFF_MST SUBHRSTF ON SUBHRSTF.STAFFCD = HDAT.SUBTR_CD1 ");
                stb.append("     WHERE ");
                stb.append("         T1.YEAR = '" + param._year + "' ");
                stb.append("         AND T1.SEMESTER = '" + param.getRegdSemester()  + "' ");
                stb.append("         AND T1.GRADE = '" + param._grade + "' ");
                stb.append("         AND T1.GRADE || T1.HR_CLASS = '" + param._gradeHrclass + "' ");
                stb.append("         AND T1.SCHREGNO IN " + SQLUtils.whereIn(true, param._schregSelected) + " ");
                stb.append(" ) ");
                stb.append(" SELECT ");
                stb.append("   T1.GRADE_NAME1, ");
                stb.append("   T1.GROUP_CD, ");
                stb.append("   T1.GROUP_NAME, ");
                stb.append("   T1.GRADE, ");
                stb.append("   T1.SCHOOL_KIND, ");
                stb.append("   T1.HR_CLASS, ");
                stb.append("   T1.HR_NAME, ");
                stb.append("   T1.STAFFNAME, ");
                stb.append("   T1.SUBSTAFFNAME, ");
                stb.append("   T1.ATTENDNO, ");
                stb.append("   T1.SCHREGNO, ");
                stb.append("   T1.NAME, ");
                stb.append("   T2.CHAIRCD, ");
                stb.append("   T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD AS SUBCLASSCD, ");
                stb.append("   VALUE(SUBM.SUBCLASSORDERNAME2, SUBM.SUBCLASSNAME) AS SUBCLASSNAME, ");
                stb.append("   CRE.REQUIRE_FLG, ");
                stb.append("   TSCORE.SEMESTER, ");
                stb.append("   TSCORE.TESTKINDCD || TSCORE.TESTITEMCD || TSCORE.SCORE_DIV AS TESTCD, ");
                stb.append("   TRANK.SCORE, ");
                stb.append("   TKARI.SCORE AS KARI_HYOTEI, ");
                stb.append("   TRANK.AVG, ");
                stb.append("   TRANK.CLASS_RANK, ");
                stb.append("   TRANK.CLASS_AVG_RANK, ");
                stb.append("   TRANK.COURSE_RANK, ");
                stb.append("   TRANK.COURSE_AVG_RANK, ");
                stb.append("   TRANK.MAJOR_RANK, ");
                stb.append("   TRANK.MAJOR_AVG_RANK ");
                stb.append(" FROM REGD T1 ");
                stb.append(" LEFT JOIN CHAIR_STD_DAT T2 ON T2.YEAR = T1.YEAR ");
                stb.append("     AND T2.SEMESTER <= T1.SEMESTER ");
                stb.append("     AND T2.SCHREGNO = T1.SCHREGNO ");
                stb.append(" LEFT JOIN CHAIR_DAT T3 ON T3.YEAR = T2.YEAR ");
                stb.append("     AND T3.SEMESTER = T2.SEMESTER ");
                stb.append("     AND T3.CHAIRCD = T2.CHAIRCD ");
                stb.append("     AND T3.CLASSCD < '90' ");
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
                stb.append(" LEFT JOIN RECORD_SCORE_DAT TSCORE ON TSCORE.YEAR = T3.YEAR ");
                stb.append("     AND TSCORE.SEMESTER || TSCORE.TESTKINDCD || TSCORE.TESTITEMCD || TSCORE.SCORE_DIV <= '" + param._semester + param._testcd + "' ");
                stb.append("     AND TSCORE.SCORE_DIV = '08' ");
                stb.append("     AND TSCORE.CLASSCD = T3.CLASSCD ");
                stb.append("     AND TSCORE.SCHOOL_KIND = T3.SCHOOL_KIND ");
                stb.append("     AND TSCORE.CURRICULUM_CD = T3.CURRICULUM_CD ");
                stb.append("     AND TSCORE.SUBCLASSCD = T3.SUBCLASSCD ");
                stb.append("     AND TSCORE.SCHREGNO = T1.SCHREGNO ");
                stb.append(" LEFT JOIN RECORD_RANK_SDIV_DAT TRANK ON TRANK.YEAR = T3.YEAR ");
                stb.append("     AND TRANK.SEMESTER = TSCORE.SEMESTER ");
                stb.append("     AND TRANK.TESTKINDCD = TSCORE.TESTKINDCD ");
                stb.append("     AND TRANK.TESTITEMCD = TSCORE.TESTITEMCD ");
                stb.append("     AND TRANK.SCORE_DIV = TSCORE.SCORE_DIV ");
                stb.append("     AND TRANK.CLASSCD = TSCORE.CLASSCD ");
                stb.append("     AND TRANK.SCHOOL_KIND = TSCORE.SCHOOL_KIND ");
                stb.append("     AND TRANK.CURRICULUM_CD = TSCORE.CURRICULUM_CD ");
                stb.append("     AND TRANK.SUBCLASSCD = TSCORE.SUBCLASSCD ");
                stb.append("     AND TRANK.SCHREGNO = TSCORE.SCHREGNO ");
                stb.append(" LEFT JOIN RECORD_SCORE_DAT TKARI ON TKARI.YEAR = T3.YEAR ");
                stb.append("     AND TKARI.SEMESTER = TSCORE.SEMESTER ");
                stb.append("     AND TKARI.TESTKINDCD = TSCORE.TESTKINDCD ");
                stb.append("     AND TKARI.TESTITEMCD = TSCORE.TESTITEMCD ");
                stb.append("     AND TKARI.SCORE_DIV = '09' ");
                stb.append("     AND TKARI.CLASSCD = TSCORE.CLASSCD ");
                stb.append("     AND TKARI.SCHOOL_KIND = TSCORE.SCHOOL_KIND ");
                stb.append("     AND TKARI.CURRICULUM_CD = TSCORE.CURRICULUM_CD ");
                stb.append("     AND TKARI.SUBCLASSCD = TSCORE.SUBCLASSCD ");
                stb.append("     AND TKARI.SCHREGNO = TSCORE.SCHREGNO ");
                stb.append(" WHERE ");
                stb.append("   NOT EXISTS (SELECT 'X' FROM SUBCLASS_REPLACE_COMBINED_DAT ");
                stb.append("               WHERE YEAR = '" + param._year + "' ");
                stb.append("                 AND ATTEND_CLASSCD = T3.CLASSCD ");
                stb.append("                 AND ATTEND_SCHOOL_KIND = T3.SCHOOL_KIND ");
                stb.append("                 AND ATTEND_CURRICULUM_CD = T3.CURRICULUM_CD ");
                stb.append("                 AND ATTEND_SUBCLASSCD = T3.SUBCLASSCD) ");
                stb.append(" UNION ALL ");
                stb.append(" SELECT ");
                stb.append("   T1.GRADE_NAME1, ");
                stb.append("   T1.GROUP_CD, ");
                stb.append("   T1.GROUP_NAME, ");
                stb.append("   T1.GRADE, ");
                stb.append("   T1.SCHOOL_KIND, ");
                stb.append("   T1.HR_CLASS, ");
                stb.append("   T1.HR_NAME, ");
                stb.append("   T1.STAFFNAME, ");
                stb.append("   T1.SUBSTAFFNAME, ");
                stb.append("   T1.ATTENDNO, ");
                stb.append("   T1.SCHREGNO, ");
                stb.append("   T1.NAME, ");
                stb.append("   CAST(NULL AS VARCHAR(1)) AS CHAIRCD, ");
                stb.append("   TRANK.SUBCLASSCD AS SUBCLASSCD, ");
                stb.append("   CAST(NULL AS VARCHAR(1)) AS SUBCLASSNAME, ");
                stb.append("   CAST(NULL AS VARCHAR(1)) AS REQUIRE_FLG, ");
                stb.append("   TRANK.SEMESTER, ");
                stb.append("   TRANK.TESTKINDCD || TRANK.TESTITEMCD || TRANK.SCORE_DIV AS TESTCD, ");
                stb.append("   TRANK.SCORE, ");
                stb.append("   CAST(NULL AS SMALLINT) AS KARI_HYOTEI, ");
                stb.append("   TRANK.AVG, ");
                stb.append("   TRANK.CLASS_RANK, ");
                stb.append("   TRANK.CLASS_AVG_RANK, ");
                stb.append("   TRANK.COURSE_RANK, ");
                stb.append("   TRANK.COURSE_AVG_RANK, ");
                stb.append("   TRANK.MAJOR_RANK, ");
                stb.append("   TRANK.MAJOR_AVG_RANK ");
                stb.append(" FROM REGD T1 ");
                stb.append(" INNER JOIN RECORD_RANK_SDIV_DAT TRANK ON TRANK.YEAR = T1.YEAR ");
                stb.append("     AND TRANK.SEMESTER || TRANK.TESTKINDCD || TRANK.TESTITEMCD || TRANK.SCORE_DIV <= '" + param._semester + param._testcd + "' ");
                stb.append("     AND TRANK.SCORE_DIV = '08' ");
                stb.append("     AND TRANK.SUBCLASSCD IN ('" + SUBCLASSCD_777777 + "', '" + SUBCLASSCD_888888 + "', '" + SUBCLASSCD_999999 + "') ");
                stb.append("     AND TRANK.SCHREGNO = T1.SCHREGNO ");
                stb.append(" ORDER BY ");
                stb.append("     GRADE, ");
                stb.append("     HR_CLASS, ");
                stb.append("     ATTENDNO ");

                final String sql = stb.toString();
                log.debug(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    
                    final String schregno = rs.getString("SCHREGNO");

                    if (null == Student.getStudent(schregno, studentList)) {
                        
                        final String grade = rs.getString("GRADE");
                        final String schoolKind = rs.getString("SCHOOL_KIND");
                        final String hrClass = rs.getString("HR_CLASS");
                        final String hrName = rs.getString("HR_NAME");
                        final String staffname = rs.getString("STAFFNAME");
                        final String substaffname = rs.getString("SUBSTAFFNAME");
                        
                        final HrClass hr = new HrClass(grade, hrClass, hrName, staffname, substaffname);

                        final String attendno = rs.getString("ATTENDNO");
                        final String name = rs.getString("NAME");
                        
                        final String groupCd = rs.getString("GROUP_CD");
                        final String gradeName1 = rs.getString("GRADE_NAME1");
                        final String groupName = rs.getString("GROUP_NAME");

                        final Student student = new Student(grade, schoolKind, hrClass, attendno, schregno, name, hr, new CourseGroup(gradeName1, groupCd, groupName));
                        studentList.add(student);
                    }

                    final String subclasscd = rs.getString("SUBCLASSCD");
                    if (null != subclasscd) {
                        final Student student = Student.getStudent(schregno, studentList);
                        final String chaircd = rs.getString("CHAIRCD");
                        
                        final String subclassname = rs.getString("SUBCLASSNAME");
                        final String requireFlg = rs.getString("REQUIRE_FLG");
                        
                        final Subclass subclass = new Subclass(subclasscd, subclassname, requireFlg);
                        student._subclassMap.put(subclasscd, subclass);

                        if (null != rs.getString("TESTCD")) {
                            final String semester = rs.getString("SEMESTER");
                            final String testcd = rs.getString("TESTCD");
                            
                            final Score s = new Score(chaircd, subclasscd, rs.getString("SCORE"), rs.getBigDecimal("AVG")
                                    , toInteger(rs.getString("CLASS_RANK")), toInteger(rs.getString("CLASS_AVG_RANK"))
                                    , toInteger(rs.getString("COURSE_RANK")), toInteger(rs.getString("COURSE_AVG_RANK"))
                                    , toInteger(rs.getString("MAJOR_RANK")), toInteger(rs.getString("MAJOR_AVG_RANK"))
                                    , rs.getString("KARI_HYOTEI"));
                            getMappedMap(student._scoreMap, semester + testcd).put(subclasscd, s);
                        }
                    }
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return studentList;
        }
        
        private static Integer toInteger(final String s) {
            if (NumberUtils.isDigits(s)) {
                return Integer.valueOf(s);
            }
            return null;
        }
    }

    private static class HrClass {
        final String _grade;
        final String _hrClass;
        final String _hrName;
        final String _staffname;
        final String _substaffname;
        final List _studentList;

        HrClass(
                final String grade,
                final String hrClass,
                final String hrName,
                final String staffname,
                final String substaffname) {
            _grade = grade;
            _hrClass = hrClass;
            _hrName = hrName;
            _staffname = staffname;
            _substaffname = substaffname;
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

        CourseGroup(
            final String gradeName1,
            final String groupCd,
            final String groupName
        ) {
            _gradeName1 = gradeName1;
            _groupCd = groupCd;
            _groupName = groupName;
        }
    }
    
    private static class Testitem {
        final String _semester;
        final String _testcd;
        final String _semestername;
        final String _testitemname;
        final String _testitemabbv1;
        public Testitem(final String semester, final String testcd, final String semestername, final String testitemname, final String testitemabbv1) {
            _semester = semester;
            _testcd = testcd;
            _testitemname = testitemname;
            _testitemabbv1 = testitemabbv1;
            _semestername = semestername;
        }
        
        private static List getTestitemList(final DB2UDB db2, final Param param) {
            String sql = "";
            sql += " SELECT T1.SEMESTER, T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV AS TESTCD, T2.SEMESTERNAME, L1.TESTITEMNAME, L1.TESTITEMABBV1 ";
            sql += " FROM  ";
            sql += "     ADMIN_CONTROL_SDIV_DAT T1  ";
            sql += "     INNER JOIN SEMESTER_MST T2 ON T2.YEAR = T1.YEAR AND T2.SEMESTER = T1.SEMESTER ";
            sql += "     INNER JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV L1 ON T1.YEAR = L1.YEAR  ";
            sql += "         AND T1.SEMESTER = L1.SEMESTER  ";
            sql += "         AND T1.TESTKINDCD = L1.TESTKINDCD  ";
            sql += "         AND T1.TESTITEMCD = L1.TESTITEMCD  ";
            sql += "         AND T1.SCORE_DIV = L1.SCORE_DIV  ";
            sql += " WHERE  ";
            sql += "     T1.YEAR = '" + param._year + "'  ";
            sql += " AND (T1.SEMESTER <> '9' AND T1.TESTKINDCD <> '99' OR T1.SEMESTER = '9' AND T1.TESTKINDCD = '99')  ";
            sql += " AND T1.SCORE_DIV = '08'  ";
            sql += " AND T1.CLASSCD = '00'  ";
            sql += " AND T1.SCHOOL_KIND = '" + param._schoolKind + "'  ";
            sql += " AND T1.CURRICULUM_CD = '00'  ";
            sql += " AND T1.SUBCLASSCD = '000000'  ";
            sql += " ORDER BY T1.SEMESTER, T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV ";
            PreparedStatement ps = null;
            ResultSet rs = null;
            List rtn = new ArrayList();
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String semester = rs.getString("SEMESTER");
                    final String testcd = rs.getString("TESTCD");
                    final String semestername = rs.getString("SEMESTERNAME");
                    final String testitemname = rs.getString("TESTITEMNAME");
                    final String testitemabbv1 = rs.getString("TESTITEMABBV1");
                    final Testitem testitem = new Testitem(semester, testcd, semestername, testitemname, testitemabbv1);
                    rtn.add(testitem);
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            }
            return rtn;
        }
    }
    
    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 61236 $");
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
        final String[] _schregSelected;
        final String _testcd;
        final String _loginDate;

        final String _schoolKind;
        final List _testitemList;
        final String _semestername;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _grade = request.getParameter("GRADE");
            _gradeHrclass = request.getParameter("GRADE_HR_CLASS");
            _schregSelected = request.getParameterValues("SCHREG_SELECTED");
            _testcd = request.getParameter("TESTKIND_ITEMCD");
            _loginDate = request.getParameter("LOGIN_DATE");

            _schoolKind = getSchoolKind(db2);
            _testitemList = Testitem.getTestitemList(db2, this);
            _semestername = getSemestername(db2);
        }
        
        public String getRegdSemester() {
            return "9".equals(_semester) ? _ctrlSemester : _semester;
        }

        private String getSchoolKind(final DB2UDB db2) {
            String sql = "";
            sql += " SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT ";
            sql += " WHERE ";
            sql += "   YEAR = '" + _year + "' ";
            sql += "   AND GRADE = '" + _grade + "' ";
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString("SCHOOL_KIND");
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

