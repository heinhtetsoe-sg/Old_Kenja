/*
 * $Id: 20b752c7f0f0bd500b66a75d7b474be40de92819 $
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 * 文京学園 累積科目別 得点分布表
 */
public class KNJD668 {

    private static final Log log = LogFactory.getLog(KNJD668.class);

    private boolean _hasData;
    
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
    
    private String sishagonyu(final BigDecimal bd) {
        if (null == bd) {
            return null;
        }
        return bd.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        final String[] subclasscdTotal = {SUBCLASSCD_777777, SUBCLASSCD_888888, SUBCLASSCD_999999};

        svf.VrSetForm("KNJD668.frm", 4);
        
        svf.VrsOut("TITLE", KenjaProperties.gengou(Integer.parseInt(_param._year)) + "年度　" + StringUtils.defaultString(_param._semestername) + "　" + StringUtils.defaultString(_param._testitemname) + "　クラス別平均点一覧表"); // タイトル
        svf.VrsOut("SUB_TITLE", "2".equals(_param._output) ? "（累積別）" : "（考査別）"); // サブタイトル
        svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._loginDate)); // 

        final List courseGroupList = CourseGroup.getCourseGroupList(db2, _param);

		final Map avgDatMap = RecordAverageDat.getRecordAverageDatMap(db2, _param);
		
        for (int cgi = 0; cgi < courseGroupList.size(); cgi++) {
            final CourseGroup cg = (CourseGroup) courseGroupList.get(cgi);
            final List subclassList = getSubclassList(cg, avgDatMap);
            
            svf.VrsOut("GRADE", cg._gradeName1); // 学年

            // 科目名
            svf.VrsOut("COURSE", cg._groupName);
            for (int si = 0; si < subclassList.size(); si++) {
                final Subclass subclass = (Subclass) subclassList.get(si);
                final int ssi = si + 1;
                
                svf.VrsOutn("SELECT_MARK", ssi, ("3".equals(subclass._requireFlg) ? "＊" : "")); // 選択科目表示
                svf.VrsOutn("SUBCLASS_NAME", ssi, null != subclass._subclassabbv && subclass._subclassabbv.length() > 3 ? subclass._subclassabbv.substring(0, 3) : subclass._subclassabbv); // 科目名
            }
            svf.VrEndRecord();

            // HR
            for (int li = 0; li < cg._hrList.size(); li++) {
                final HrClass hr = (HrClass) cg._hrList.get(li);
                
                svf.VrsOut("HR_NAME_HEADER", "クラス"); // クラスヘッダ
                svf.VrsOut("HR_NAME", hr._hrClassName1); // 年組

                for (int si = 0; si < subclassList.size(); si++) {
                    final Subclass subclass = (Subclass) subclassList.get(si);
                    final int ssi = si + 1;
                    
                    final RecordAverageDat avgHr = RecordAverageDat.get(avgDatMap, RecordAverageDat.getHrAvgDivKey(hr._grade, hr._hrClass), subclass._subclasscd);
                    if (null != avgHr) {
                        svf.VrsOutn("SUBCLASS_AVERAGE", ssi, sishagonyu(avgHr._avg)); // 科目名
                    }
                    
                }
                
                for (int si = 0; si < subclasscdTotal.length; si++) {
                    final String subclasscd = subclasscdTotal[si];
                    
                    final RecordAverageDat avgHr = RecordAverageDat.get(avgDatMap, RecordAverageDat.getHrAvgDivKey(hr._grade, hr._hrClass), subclasscd);
                    if (null != avgHr) {
                        svf.VrsOut("AVERAGE" + String.valueOf(si + 1), sishagonyu(avgHr._avg)); // 科目名
                    }
                }
                svf.VrEndRecord();
            }

            // コースグループ
            boolean hasCourseGroupAvg = false;
            for (int si = 0; si < subclassList.size(); si++) {
                final Subclass subclass = (Subclass) subclassList.get(si);
                final int ssi = si + 1;
                
                final RecordAverageDat avgCourseGroup = RecordAverageDat.get(avgDatMap, RecordAverageDat.getCourseGroupAvgDivKey(cg._grade, cg._groupCd), subclass._subclasscd);
                if (null != avgCourseGroup) {
                    svf.VrsOutn("AVE_SUBCLASS_AVERAGE", ssi, sishagonyu(avgCourseGroup._avg)); // 科目名
                    hasCourseGroupAvg = hasCourseGroupAvg || null != avgCourseGroup._avg;
                }
            }

            for (int si = 0; si < subclasscdTotal.length; si++) {
                final String subclasscd = subclasscdTotal[si];
                
                final RecordAverageDat avgCourseGroup = RecordAverageDat.get(avgDatMap, RecordAverageDat.getCourseGroupAvgDivKey(cg._grade, cg._groupCd), subclasscd);
                if (null != avgCourseGroup) {
                    svf.VrsOut("AVE_AVERAGE" + String.valueOf(si + 1), sishagonyu(avgCourseGroup._avg)); // 科目名
                    hasCourseGroupAvg = hasCourseGroupAvg || null != avgCourseGroup._avg;
                }
            }
            if (!hasCourseGroupAvg) {
                // 行表示のためのダミー
                svf.VrsOutn("AVE_SUBCLASS_AVERAGE", 1, "DUMMY");
                svf.VrAttributen("AVE_SUBCLASS_AVERAGE", 1, "X=10000");
            }
            svf.VrEndRecord();
            
            svf.VrsOut("BLANK", "1"); // 空行
            svf.VrEndRecord();
            
            _hasData = true;
        }
    }
    
    private List getSubclassList(final CourseGroup courseGroup, final Map avgDatMap) {
        final Set avgSubclasscdSet = new HashSet();
        for (final Iterator it = courseGroup._hrList.iterator(); it.hasNext();) {
            final HrClass hr = (HrClass) it.next();
            // HRの科目
            avgSubclasscdSet.addAll(RecordAverageDat.getSubclassMap(avgDatMap, RecordAverageDat.getHrAvgDivKey(hr._grade, hr._hrClass)).keySet());
        }
        // コースグループの科目
        avgSubclasscdSet.addAll(RecordAverageDat.getSubclassMap(avgDatMap, RecordAverageDat.getCourseGroupAvgDivKey(courseGroup._grade, courseGroup._groupCd)).keySet());

        log.debug(" subclasscdSet = " + avgSubclasscdSet + " /  course group subclasscd = " + courseGroup._subclassMap.keySet());
        
        final List subclassList = new ArrayList();
        for (final Iterator it = avgSubclasscdSet.iterator(); it.hasNext();) {
            final String subclasscd = (String) it.next();
            final Subclass subclass = (Subclass) courseGroup._subclassMap.get(subclasscd);
            if (null != subclass) {
                subclassList.add(subclass);
            }
        }
        Collections.sort(subclassList);
        return subclassList;
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

    private static class HrClass implements Comparable {
        final String _grade;
        final String _hrClass;
        final String _hrName;
        final String _hrClassName1;
        final String _staffname;

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

        public int compareTo(Object o) {
            final HrClass hrClass = (HrClass) o;
            return (_grade + _hrClass).compareTo(hrClass._grade + hrClass._hrClass);
        }
    }

    private static class CourseGroup {
        final String _grade;
        final String _gradeName1;
        final String _groupCd;
        final String _groupName;
        final List _hrList;
        
        final Map _subclassMap;

        CourseGroup(
                final String grade,
                final String gradeName1,
                final String groupCd,
                final String groupName
        ) {
            _grade = grade;
            _gradeName1 = gradeName1;
            _groupCd = groupCd;
            _groupName = groupName;
            _hrList = new ArrayList();
            _subclassMap = new HashMap();
        }

        public static List getCourseGroupList(final DB2UDB db2, final Param param) {
            final List courseGroupList = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append("     SELECT DISTINCT ");
                stb.append("       T1.YEAR, ");
                stb.append("       T1.SEMESTER, ");
                stb.append("       GDAT.GRADE_NAME1, ");
                stb.append("       CGRP.GROUP_CD, ");
                stb.append("       CGRPH.GROUP_NAME, ");
                stb.append("       T1.GRADE, ");
                stb.append("       T1.HR_CLASS, ");
                stb.append("       HDAT.HR_NAME, ");
                stb.append("       HDAT.HR_CLASS_NAME1, ");
                stb.append("       HRSTF.STAFFNAME, ");
                stb.append("       T1.COURSECD, ");
                stb.append("       T1.MAJORCD, ");
                stb.append("       T1.COURSECODE, ");
                stb.append("       TRANK.CLASSCD || '-' || TRANK.SCHOOL_KIND || '-' || TRANK.CURRICULUM_CD || '-' || TRANK.SUBCLASSCD AS SUBCLASSCD, ");
                stb.append("       SUBM.SUBCLASSABBV, ");
                stb.append("       CRE.REQUIRE_FLG ");
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
                if ("2".equals(param._output)) {
                    stb.append("     LEFT JOIN RECORD_RANK_RUIKEI_SDIV_DAT TRANK ON TRANK.YEAR = T1.YEAR ");
                } else {
                    stb.append("     LEFT JOIN RECORD_RANK_SDIV_DAT TRANK ON TRANK.YEAR = T1.YEAR ");
                }
                stb.append("         AND TRANK.SEMESTER = '" + param._semester + "' AND TRANK.TESTKINDCD || TRANK.TESTITEMCD || TRANK.SCORE_DIV = '" + param._testcd + "' ");
                stb.append("         AND TRANK.SCHREGNO = T1.SCHREGNO ");
                stb.append("     LEFT JOIN CREDIT_MST CRE ON CRE.YEAR = T1.YEAR ");
                stb.append("         AND CRE.COURSECD = T1.COURSECD ");
                stb.append("         AND CRE.MAJORCD = T1.MAJORCD ");
                stb.append("         AND CRE.GRADE = T1.GRADE ");
                stb.append("         AND CRE.COURSECODE = T1.COURSECODE ");
                stb.append("         AND CRE.CLASSCD = TRANK.CLASSCD ");
                stb.append("         AND CRE.SCHOOL_KIND = TRANK.SCHOOL_KIND ");
                stb.append("         AND CRE.CURRICULUM_CD = TRANK.CURRICULUM_CD ");
                stb.append("         AND CRE.SUBCLASSCD = TRANK.SUBCLASSCD ");
                stb.append("     LEFT JOIN SUBCLASS_MST SUBM ON SUBM.CLASSCD = TRANK.CLASSCD ");
                stb.append("         AND SUBM.SCHOOL_KIND = TRANK.SCHOOL_KIND ");
                stb.append("         AND SUBM.CURRICULUM_CD = TRANK.CURRICULUM_CD ");
                stb.append("         AND SUBM.SUBCLASSCD = TRANK.SUBCLASSCD ");
                stb.append("     WHERE ");
                stb.append("         T1.YEAR = '" + param._year + "' ");
                stb.append("         AND T1.SEMESTER = '" + param.getRegdSemester() + "' ");
                stb.append("         AND T1.GRADE = '" + param._grade + "' ");
                stb.append("         AND CGRP.GROUP_CD = '" + param._groupCd + "' ");
                if (!"00000".equals(param._gradeHrClass)) {
                    stb.append("         AND T1.GRADE || T1.HR_CLASS = '" + param._gradeHrClass + "' ");
                }
                stb.append(" ORDER BY ");
                stb.append("     CGRP.GROUP_CD, ");
                stb.append("     T1.GRADE, ");
                stb.append("     T1.HR_CLASS ");

                final String sql = stb.toString();
                log.debug(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                final Map courseGroupMap = new HashMap();
                while (rs.next()) {
                    final String groupCd = rs.getString("GROUP_CD");
                    if (null == courseGroupMap.get(groupCd)) {
                        final String grade = rs.getString("GRADE");
                        final String gradeName1 = rs.getString("GRADE_NAME1");
                        final String groupName = rs.getString("GROUP_NAME");
                        
                        final CourseGroup coursegroup = new CourseGroup(grade, gradeName1, groupCd, groupName);
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
                    
                    if (null != rs.getString("SUBCLASSCD")) {
                        final Subclass subclass = new Subclass(rs.getString("SUBCLASSCD"), rs.getString("SUBCLASSABBV"), rs.getString("REQUIRE_FLG"));
                        coursegroup._subclassMap.put(subclass._subclasscd, subclass);
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
        final String _loginDate;
        final String _testcd;
        final String _groupCd;
        final String _gradeHrClass;
        final String _output; // 帳票 1:考査別 2:累積別

        final String _testitemname;
        final String _semestername;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _grade = request.getParameter("GRADE");
            _loginDate = request.getParameter("LOGIN_DATE");
            _testcd = request.getParameter("TESTKIND_ITEMCD");
            _groupCd = request.getParameter("GROUP_CD");
            _gradeHrClass = request.getParameter("GRADE_HR_CLASS");
            _output = request.getParameter("OUTPUT");

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

