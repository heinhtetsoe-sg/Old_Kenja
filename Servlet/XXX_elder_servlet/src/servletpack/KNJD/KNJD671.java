/*
 * $Id: 10cc2f053f830c6ee1d9e14f6a18cd9c92fc5035 $
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
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 * 文京学園 得点上位者一覧表
 */
public class KNJD671 {

    private static final Log log = LogFactory.getLog(KNJD671.class);

    private boolean _hasData;
    
    private static String SUBCLASSCD_333333 = "333333";
    private static String SUBCLASSCD_555555 = "555555";
    private static String SUBCLASSCD_777777 = "777777";
    private static String SUBCLASSCD_888888 = "888888";
    private static String SUBCLASSCD_999999 = "999999";

    private static int SORT_SCORE = 1;
    private static int SORT_AVG = 2;

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
    
    /**
     *  文字数を取得
     */
    private static int getMS932ByteLength(final String str) {
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
        
		final List courseGroupList = CourseGroup.getCourseGroupList(db2, _param);
		for (int line = 0; line < courseGroupList.size(); line++) {
			final CourseGroup cg = (CourseGroup) courseGroupList.get(line);
			
			for (final Iterator it = cg._hrList.iterator(); it.hasNext();) {
			    final HrClass hrClass = (HrClass) it.next();

		        svf.VrSetForm("KNJD671.frm", 4);
		        
		        svf.VrsOut("TITLE", KenjaProperties.gengou(Integer.parseInt(_param._year)) + "年度　" + StringUtils.defaultString(_param._semestername) + "　" + StringUtils.defaultString(_param._testitemname) + "　得点上位者一覧"); // タイトル
		        svf.VrsOut("GRADE_NAME", cg._gradeName1); // 学年名
		        svf.VrsOut("GROUP_NAME", cg._groupName); // コースグループ名
		        svf.VrsOut("HR_NAME", hrClass._hrName); // 年組名
		        svf.VrsOut("STAFFNAME", hrClass._staffname); // 担任名
		        svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._loginDate)); // 作成日

			    final List score999999List = sortScoreHrRank(hrClass._studentList, SUBCLASSCD_999999, SORT_AVG, 10);
			    
			    for (int i = 0; i < score999999List.size(); i++) {
			        final Score s = (Score) score999999List.get(i);
			        svf.VrsOut("MAIN_RANK", null != s._classAvgRank ? s._classAvgRank + "位" : ""); // 順位
			        svf.VrsOut("MAIN_AVG", sishagonyu(s._avg)); // 平均点
			        svf.VrsOut("MAIN_NAME", s._student._name); // 氏名
			        svf.VrEndRecord();
			    }
			    
			    final List printSubclassListList = PrintSubclass.getPrintSubclassListList(hrClass, 5); // 5列で分割
			    
			    for (final Iterator sublit = printSubclassListList.iterator(); sublit.hasNext();) {
			        final List sublist = (List) sublit.next();
			        
			        int listMaxSize = 0;
			        for (final Iterator subit = sublist.iterator(); subit.hasNext();) {
			            final PrintSubclass ps = (PrintSubclass) subit.next();
			            ps._scoreList = sortScoreHrRank(hrClass._studentList, ps._subclasscd, SORT_SCORE, 10);
			            listMaxSize = Math.max(listMaxSize, ps._scoreList.size());
			        }
			        
                    for (int subi = 0; subi < sublist.size(); subi++) {
                        final PrintSubclass ps = (PrintSubclass) sublist.get(subi);
                        final String ssubi = String.valueOf(subi + 1);
                        svf.VrsOut("SUBCLASS" + ssubi + (getMS932ByteLength(ps._subclassname) > 32 ? "_2" : ""), ps._subclassname); // 科目
                        svf.VrsOut("SELECT_MARK" + ssubi, ps._requireFlgSet.contains("3") ? "＊" : ""); // 選択科目
                    }
                    svf.VrEndRecord();

                    for (int i = 0; i < listMaxSize; i++) {
			            for (int subi = 0; subi < sublist.size(); subi++) {
			                final PrintSubclass ps = (PrintSubclass) sublist.get(subi);
			                final String ssubi = String.valueOf(subi + 1);
			                if (i < ps._scoreList.size()) {
			                    final Score s = (Score) ps._scoreList.get(i);
			                    final String beforeRank = i == 0 || ((Score) ps._scoreList.get(i - 1))._classRank == null ? null : ((Score) ps._scoreList.get(i - 1))._classRank;
			                    if (null == beforeRank || !beforeRank.equals(s._classRank)) {
	                                svf.VrsOut("RANK" + ssubi, null == s._classRank ? "" : s._classRank + "位"); // 順位（科目ごと）
			                    }
	                            svf.VrsOut("SCORE" + ssubi, s._score); // 得点（科目ごと）
	                            svf.VrsOut("NAME" + ssubi + (getMS932ByteLength(s._student._name) > 20 ? "_2" : ""), s._student._name); // 氏名（科目ごと）
			                }
			            }
			            svf.VrEndRecord();
			        }
                    
                    svf.VrsOut("KARA", "1"); // 空行
                    svf.VrEndRecord();
                    _hasData = true;
			    }
			}
		}
    }
    
    private static class PrintSubclass implements Comparable {
        final String _subclasscd;
        final String _subclassname;
        final Set _requireFlgSet = new HashSet();
        List _scoreList = Collections.EMPTY_LIST;
        PrintSubclass(
                final String subclasscd,
                final String subclassname) {
            _subclasscd = subclasscd;
            _subclassname = subclassname;
        }
        
        public int compareTo(Object o) {
            final PrintSubclass subclass = (PrintSubclass) o;
            if (!_requireFlgSet.contains("3") && subclass._requireFlgSet.contains("3")) {
                return -1;
            } else if (_requireFlgSet.contains("3") && !subclass._requireFlgSet.contains("3")) { // 選択科目は後
                return 1;
            }
            return _subclasscd.compareTo(subclass._subclasscd);
        }
        
        private static List getPrintSubclassListList(final HrClass hr, final int num) {
            final List studentList = hr._studentList;
            final TreeMap subclassMap = new TreeMap();
            for (final Iterator stit = studentList.iterator(); stit.hasNext();) {
                final Student student = (Student) stit.next();
                for (final Iterator scit = student._scoreMap.values().iterator(); scit.hasNext();) {
                    final Score score = (Score) scit.next();
                    if (null != score._subclassname && !subclassMap.containsKey(score._subclasscd)) {
                        subclassMap.put(score._subclasscd, new PrintSubclass(score._subclasscd, score._subclassname));
                    }
                    final PrintSubclass printSubclass = (PrintSubclass) subclassMap.get(score._subclasscd);
                    if (null != printSubclass) {
                        printSubclass._requireFlgSet.add(score._requireFlg);
                    }
                }
            }
            subclassMap.remove(SUBCLASSCD_333333);
            subclassMap.remove(SUBCLASSCD_555555);
            subclassMap.remove(SUBCLASSCD_777777);
            subclassMap.remove(SUBCLASSCD_888888);
            subclassMap.remove(SUBCLASSCD_999999);
            
            final List subclassList = new ArrayList(subclassMap.values());
            Collections.sort(subclassList);

            final List list = new ArrayList();
            List current = null;
            for (final Iterator it = subclassList.iterator(); it.hasNext();) {
                final Object o = it.next();
                if (null == current || current.size() >= num) {
                    current = new ArrayList();
                    list.add(current);
                }
                current.add(o);
            }
            log.debug(" hr = " + hr._grade + hr._hrClass + ": subclass size = " + subclassList.size());
            return list;
        }
    }

    /**
     * 生徒を科目コードのクラス順位でソートして最大順位までのリスト
     * @param studentList 生徒のリスト
     * @param subclasscd 科目コード
     * @param maxRank 最大順位
     * @return
     */
    private static List sortScoreHrRank(final List studentList, final String subclasscd, final int sortDiv, final int maxRank) {
        List rankList = new ArrayList();
        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            if (null != student._scoreMap.get(subclasscd)) {
                final Score score = (Score) student._scoreMap.get(subclasscd);
                if (null != score.rank(sortDiv)) {
                    rankList.add(score);
                }
            }
        }
        
        Collections.sort(rankList, new Score.Comparator(sortDiv));
        int removeIdx = -1;
        for (int i = 0; i < rankList.size(); i++) {
            final Score score = (Score) rankList.get(i);
            if (!NumberUtils.isDigits(score.rank(sortDiv)) || Integer.parseInt(score.rank(sortDiv)) > maxRank) {
                removeIdx = i;
                break;
            }
        }
        if (removeIdx >= 0) {
            rankList = rankList.subList(0, removeIdx);
        }
        return rankList;
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
        final String _subclassname;
        final String _requireFlg;
        final String _score;
        final BigDecimal _avg;
        final String _classRank;
        final String _classAvgRank;
        Student _student = null;

        Score(
                final String chaircd,
                final String subclasscd,
                final String subclassname,
                final String requireFlg,
                final String score,
                final BigDecimal avg,
                final String classRank,
                final String classAvgRank) {
            _chaircd = chaircd;
            _subclasscd = subclasscd;
            _subclassname = subclassname;
            _requireFlg = requireFlg;
            _score = score;
            _avg = avg;
            _classRank = classRank;
            _classAvgRank = classAvgRank;
        }
        
        public String rank(int sortDiv) {
            if (SORT_AVG == sortDiv) {
                return _classAvgRank;
            }
            return _classRank;
        }

        private static class Comparator implements java.util.Comparator {
            final int _div;
            Comparator(final int div) {
                _div = div;
            }
            public int compare(final Object o1, final Object o2) {
                int cmp = 0;
                final Score s1 = (Score) o1;
                final Score s2 = (Score) o2;
                final Integer rank1 = !NumberUtils.isDigits(s1.rank(_div)) ? new Integer(999999) : Integer.valueOf(s1.rank(_div));
                final Integer rank2 = !NumberUtils.isDigits(s2.rank(_div)) ? new Integer(999999) : Integer.valueOf(s2.rank(_div));
                cmp = rank1.compareTo(rank2);
                if (0 != cmp) return cmp;
                if (null == s2._student || null == s2._student._attendno) return -1;
                if (null == s1._student || null == s1._student._attendno) return 1;
                return s1._student._attendno.compareTo(s2._student._attendno);
            }
        }
    }

    private static class Student {
        final String _grade;
        final String _hrClass;
        final String _attendno;
        final String _schregno;
        final String _name;
        final Map _scoreMap = new HashMap();

        Student(
            final String grade,
            final String hrClass,
            final String hrName,
            final String staffname,
            final String attendno,
            final String schregno,
            final String name) {
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
                stb.append("       T1.HR_CLASS, ");
                stb.append("       HDAT.HR_NAME, ");
                stb.append("       HRSTF.STAFFNAME, ");
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
                stb.append("     WHERE ");
                stb.append("         T1.YEAR = '" + param._year + "' ");
                stb.append("         AND T1.SEMESTER = '" + param.getRegdSemester() + "' ");
                stb.append("         AND T1.GRADE = '" + param._grade + "' ");
                if (!"00000".equals(param._gradeHrclass)) {
                    stb.append("         AND T1.GRADE || T1.HR_CLASS = '" + param._gradeHrclass + "' ");
                }
                stb.append("         AND CGRP.GROUP_CD = '" + param._groupCd + "' ");
                stb.append(" ) ");
                stb.append(" SELECT ");
                stb.append("   T1.GRADE_NAME1, ");
                stb.append("   T1.GROUP_CD, ");
                stb.append("   T1.GROUP_NAME, ");
                stb.append("   T1.GRADE, ");
                stb.append("   T1.HR_CLASS, ");
                stb.append("   T1.HR_NAME, ");
                stb.append("   T1.STAFFNAME, ");
                stb.append("   T1.ATTENDNO, ");
                stb.append("   T1.SCHREGNO, ");
                stb.append("   T1.NAME, ");
                stb.append("   T2.CHAIRCD, ");
                stb.append("   T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD AS SUBCLASSCD, ");
                stb.append("   SUBM.SUBCLASSNAME, ");
                stb.append("   CRE.REQUIRE_FLG, ");
                stb.append("   TREC.SEMESTER, ");
                stb.append("   TREC.TESTKINDCD || TREC.TESTITEMCD || TREC.SCORE_DIV AS TESTCD, ");
                stb.append("   TRANK.SCORE, ");
                stb.append("   TRANK.AVG, ");
                stb.append("   TRANK.CLASS_RANK, ");
                stb.append("   TRANK.CLASS_AVG_RANK ");
                stb.append(" FROM REGD T1 ");
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
                stb.append(" LEFT JOIN RECORD_SCORE_DAT TREC ON TREC.YEAR = T3.YEAR ");
                stb.append("     AND TREC.SEMESTER = '" + param._semester + "' AND TREC.TESTKINDCD || TREC.TESTITEMCD || TREC.SCORE_DIV = '" + param._testcd + "' ");
                stb.append("     AND TREC.CLASSCD = T3.CLASSCD ");
                stb.append("     AND TREC.SCHOOL_KIND = T3.SCHOOL_KIND ");
                stb.append("     AND TREC.CURRICULUM_CD = T3.CURRICULUM_CD ");
                stb.append("     AND TREC.SUBCLASSCD = T3.SUBCLASSCD ");
                stb.append("     AND TREC.SCHREGNO = T1.SCHREGNO ");
                stb.append(" LEFT JOIN RECORD_RANK_SDIV_DAT TRANK ON TRANK.YEAR = T3.YEAR ");
                stb.append("     AND TRANK.SEMESTER = '" + param._semester + "' AND TRANK.TESTKINDCD || TRANK.TESTITEMCD || TRANK.SCORE_DIV = '" + param._testcd + "' ");
                stb.append("     AND TRANK.CLASSCD = T3.CLASSCD ");
                stb.append("     AND TRANK.SCHOOL_KIND = T3.SCHOOL_KIND ");
                stb.append("     AND TRANK.CURRICULUM_CD = T3.CURRICULUM_CD ");
                stb.append("     AND TRANK.SUBCLASSCD = T3.SUBCLASSCD ");
                stb.append("     AND TRANK.SCHREGNO = T1.SCHREGNO ");
                stb.append(" UNION ALL ");
                stb.append(" SELECT ");
                stb.append("   T1.GRADE_NAME1, ");
                stb.append("   T1.GROUP_CD, ");
                stb.append("   T1.GROUP_NAME, ");
                stb.append("   T1.GRADE, ");
                stb.append("   T1.HR_CLASS, ");
                stb.append("   T1.HR_NAME, ");
                stb.append("   T1.STAFFNAME, ");
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
                stb.append("   TRANK.AVG, ");
                stb.append("   TRANK.CLASS_RANK, ");
                stb.append("   TRANK.CLASS_AVG_RANK ");
                stb.append(" FROM REGD T1 ");
                stb.append(" INNER JOIN RECORD_RANK_SDIV_DAT TRANK ON TRANK.YEAR = T1.YEAR ");
                stb.append("     AND TRANK.SEMESTER = '" + param._semester + "' AND TRANK.TESTKINDCD || TRANK.TESTITEMCD || TRANK.SCORE_DIV = '" + param._testcd + "' ");
                stb.append("     AND TRANK.SUBCLASSCD = '" + SUBCLASSCD_999999 + "' ");
                stb.append("     AND TRANK.SCHREGNO = T1.SCHREGNO ");
                stb.append(" ORDER BY ");
                stb.append("     GROUP_CD, ");
                stb.append("     GRADE, ");
                stb.append("     HR_CLASS, ");
                stb.append("     ATTENDNO ");

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
                        final String staffname = rs.getString("STAFFNAME");
                        
                        final HrClass hr = new HrClass(grade, hrClass, hrName, staffname);
                        coursegroup._hrList.add(hr);
                    }

                    final HrClass hr = HrClass.getHrClass(grade + hrClass, coursegroup._hrList);
                    final String schregno = rs.getString("SCHREGNO");
                    if (null == Student.getStudent(schregno, hr._studentList)) {
                        final String hrName = rs.getString("HR_NAME");
                        final String staffname = rs.getString("STAFFNAME");
                        final String attendno = rs.getString("ATTENDNO");
                        final String name = rs.getString("NAME");
                        
                        final Student student = new Student(grade, hrClass, hrName, staffname, attendno, schregno, name);
                        hr._studentList.add(student);
                    }
                    
                    final String subclasscd = rs.getString("SUBCLASSCD");
                    if (null != subclasscd) {
                        final Student student = Student.getStudent(schregno, hr._studentList);
                        final String chaircd = rs.getString("CHAIRCD");
                        
                        final String subclassname = rs.getString("SUBCLASSNAME");
                        final String requireFlg = rs.getString("REQUIRE_FLG");

                        final Score score = new Score(chaircd, subclasscd, subclassname, requireFlg, rs.getString("SCORE"), rs.getBigDecimal("AVG"), rs.getString("CLASS_RANK"), rs.getString("CLASS_AVG_RANK"));
                        score._student = student;
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
        final String _testitemname;
        final String _semestername;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _grade = request.getParameter("GRADE");
            _gradeHrclass = request.getParameter("GRADE_HR_CLASS");
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

