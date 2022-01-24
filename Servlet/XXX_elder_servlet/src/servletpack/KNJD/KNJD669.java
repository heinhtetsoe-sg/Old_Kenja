/*
 * $Id: 02c3676cd658c42a7f1e156cc6a46c2932e624e1 $
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
import java.sql.ResultSetMetaData;
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
 * 文京学園 得点分布表
 */
public class KNJD669 {

    private static final Log log = LogFactory.getLog(KNJD669.class);

    private boolean _hasData;
    
    private static String SEM_TESTCD_GAKUNENHYOTEI = "9990009";
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
    
    private static String sishagonyu(final BigDecimal bd) {
        if (null == bd) {
            return null;
        }
        return bd.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
    }
    
    
    /**
     * listを最大数ごとにグループ化したリストを得る
     * @param list
     * @param max 最大数
     * @return listを最大数ごとにグループ化したリスト
     */
    private static List getPageList(final List list, final int max) {
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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        
        final Map avgDatMap = RecordAverageDat.getRecordAverageDatMap(db2, _param);
        
		final List courseGroupList = CourseGroup.getCourseGroupList(db2, _param);
		for (int cgi = 0; cgi < courseGroupList.size(); cgi++) {
			final CourseGroup cg = (CourseGroup) courseGroupList.get(cgi);
			
			for (int hri = 0; hri < cg._hrList.size(); hri++) {
			    final HrClass hr = (HrClass) cg._hrList.get(hri);

                if (null != _param._gradeHrclass && !"00000".equals(_param._gradeHrclass)) {
                    if (!_param._gradeHrclass.equals(hr._grade + hr._hrClass)) {
                        continue;
                    }
                }

                final List subclassListAll = new ArrayList(hr._subclassMap.values());
                for (final Iterator it = subclassListAll.iterator(); it.hasNext();) {
                    final Subclass subclass = (Subclass) it.next();
                    if (SUBCLASSCD_333333.equals(subclass._subclasscd) || SUBCLASSCD_555555.equals(subclass._subclasscd) || SUBCLASSCD_777777.equals(subclass._subclasscd) || SUBCLASSCD_888888.equals(subclass._subclasscd) || SUBCLASSCD_999999.equals(subclass._subclasscd)) {
                        it.remove();
                    }
                }
                Collections.sort(subclassListAll);
                
                int perfect = 0;
                for (int j = 0; j < subclassListAll.size(); j++) {
                    final Subclass subclass = (Subclass) subclassListAll.get(j);
                    perfect = Math.max(perfect, Perfect.getPerfect(db2, _param, subclass._subclasscd, cg._groupCd));
                }
                log.debug(" course group = " + cg._groupCd + ", hrclass = " + hr._grade + hr._hrClass + " perfect = " + perfect);
                if (perfect == 0) {
                    perfect = 100;
                }
                final int kizami = perfect / 20;

                final List rangeList = Range.getRangeList(perfect, kizami);

                final List subclassPageList = getPageList(subclassListAll, 25);
                
                final String form = "KNJD669.frm";

                for (int pi = 0; pi < subclassPageList.size(); pi++) {
                    
                    final List subclassList = (List) subclassPageList.get(pi);
                    
                    svf.VrSetForm(form, 1);
                    
                    svf.VrsOut("TITLE", KenjaProperties.gengou(Integer.parseInt(_param._year)) + "年度　" + StringUtils.defaultString(_param._testitemname) + "　分布表"); // タイトル
                    svf.VrsOut("HR_NAME", hr._hrName); // 年組
                    svf.VrsOut("NUM1", String.valueOf(hr._studentList.size()) + "名"); // 人数
                    if (!StringUtils.isBlank(hr._staffname)) {
                        svf.VrsOut("TEACHER_NAME", "担任：" + StringUtils.defaultString(hr._staffname)); // 担任名
                        svf.VrsOut("TEACHER_NAME_IN1", "印");
                    }
                    if (!StringUtils.isBlank(hr._substaffname)) {
                        svf.VrsOut("TEACHER_NAME2", "副担任：" + StringUtils.defaultString(hr._substaffname)); // 担任名
                        svf.VrsOut("TEACHER_NAME_IN2", "印");
                    }
                    svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._date)); // 
                    
                    // 上段：得点分布
                    for (int i = 0; i < rangeList.size(); i++) {
                        final Range range = (Range) rangeList.get(i);
                        final String lowerS = String.valueOf(range._lower);
                        final String higherS = String.valueOf(range._higher);
                        final String printRange = StringUtils.repeat(" ", 3 - lowerS.length()) + lowerS + (range._lower == range._higher ? "" : "-" + StringUtils.repeat(" ", 3 - higherS.length()) + higherS);  
                        for (int j = 0; j < 2; j++) {
                            svf.VrsOutn("SCORE_RANGE" + String.valueOf(j + 1), i + 1, printRange);
                        }
                    }
                    for (int j = 0; j < subclassList.size(); j++) {
                        final Subclass subclass = (Subclass) subclassList.get(j);
                        final int line = j + 1;
                        svf.VrsOutn("SELECT_MARK1", line, "3".equals(subclass._requireFlg) ? "＊" : ""); // 選択科目表示
                        svf.VrsOutn("SUBCLASS_NAME1", line, null != subclass._subclassabbv && subclass._subclassabbv.length() > 3 ? subclass._subclassabbv.substring(0, 3) : subclass._subclassabbv); // 科目名
                        
                        final List targetStudentList = new ArrayList(hr._studentList);
                        for (int di = 0; di < rangeList.size(); di++) {
                            final Range r = (Range) rangeList.get(di);
                            svf.VrsOutn("DIST1_" + String.valueOf(di  + 1), line, String.valueOf(r.getInRangeStudentList(targetStudentList, subclass._subclasscd).size())); // 分布
                        }
                        final RecordAverageDat hrAvg = RecordAverageDat.getHrAvg(avgDatMap, subclass._subclasscd, hr._grade, hr._hrClass);
                        if (null != hrAvg) {
                            svf.VrsOutn("AVERAGE1", line, sishagonyu(hrAvg._avg)); // 平均
                        }
                    }
                    
                    // 上段右：順位順平均
                    printRank(svf, hr._studentList);
                    
                    // 下段右：学年評定順位順平均
                    printGakunenHyoteiRank(svf, hr._studentList);
                    
                    // 下段：コースグループ得点分布
                    svf.VrsOut("GRADE", cg._gradeName1); // 学年
                    svf.VrsOut("COURSE", cg._groupName); // コース
                    svf.VrsOut("NUM2", String.valueOf(cg.getStudentList().size()) + "名"); // 人数
                    
                    for (int j = 0; j < subclassList.size(); j++) {
                        final Subclass subclass = (Subclass) subclassList.get(j);
                        final int line = j + 1;
                        svf.VrsOutn("SELECT_MARK2", line, "3".equals(subclass._requireFlg) ? "＊" : ""); // 選択科目表示
                        svf.VrsOutn("SUBCLASS_NAME2", line, null != subclass._subclassabbv && subclass._subclassabbv.length() > 3 ? subclass._subclassabbv.substring(0, 3) : subclass._subclassabbv); // 科目名
                        
                        final List targetStudentList = new ArrayList(cg.getStudentList());
                        for (int di = 0; di < rangeList.size(); di++) {
                            final Range r = (Range) rangeList.get(di);
                            svf.VrsOutn("DIST2_" + String.valueOf(di + 1), line, String.valueOf(r.getInRangeStudentList(targetStudentList, subclass._subclasscd).size())); // 分布
                        }
                        final RecordAverageDat cgAvg = RecordAverageDat.getCourseGroupAvg(avgDatMap, subclass._subclasscd, _param._grade, cg._groupCd);
                        if (null != cgAvg) {
                            svf.VrsOutn("AVERAGE2", line, sishagonyu(cgAvg._avg)); // 平均
                        }
                    }
                    _hasData = true;
                    
                    svf.VrEndPage();
                }
			}
		}
    }

    public void printRank(final Vrw32alp svf, final List studentList) {
        final Map rankMap = new TreeMap();
        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            final Score score = (Score) student._scoreMap.get(SUBCLASSCD_777777);
            if (null != score && null != score._classRank) {
                rankMap.put(Integer.valueOf(score._classRank), score._score);
            }
        }
        int col = 1;
        int line = 1;
        for (final Iterator it = rankMap.entrySet().iterator(); it.hasNext();) {
            final Map.Entry e = (Map.Entry) it.next();
            final Integer rank = (Integer) e.getKey();
            final String score = (String) e.getValue();

            svf.VrsOutn("RANK1_" + String.valueOf(line), col, rank.toString()); // 順位
            svf.VrsOutn("TOTAL1_" + String.valueOf(line), col, score); // 合計
            if (line >= 25) {
                line = 0;
                col += 1;
            }
            line += 1;
        }
    }

    public void printGakunenHyoteiRank(final Vrw32alp svf, final List studentList) {
        final Map rankMap = new TreeMap();
        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            final Score score = (Score) student._score9990009Map.get(SUBCLASSCD_999999);
            if (null != score && null != score._classAvgRank) {
                rankMap.put(Integer.valueOf(score._classAvgRank), score._avg);
            }
        }
        int col = 1;
        int line = 1;
        for (final Iterator it = rankMap.entrySet().iterator(); it.hasNext();) {
            final Map.Entry e = (Map.Entry) it.next();
            final Integer rank = (Integer) e.getKey();
            final BigDecimal avg = (BigDecimal) e.getValue();

            svf.VrsOutn("RANK2_" + String.valueOf(line), col, rank.toString()); // 順位
            svf.VrsOutn("TOTAL2_" + String.valueOf(line), col, sishagonyu(avg)); // 合計
            if (line >= 25) {
                line = 0;
                col += 1;
            }
            line += 1;
        }
    }
    
    private static Map getMappedMap(final Map map, final String key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new TreeMap());
        }
        return (Map) map.get(key1);
    }
    
    private static class Range {
        final int _lower;
        final int _higher;
        Range(final int lower, final int higher) {
            _lower = lower;
            _higher = higher;
        }
        private static List getRangeList(final int max, final int kizami) {
            final List list = new ArrayList();
            list.add(new Range(max, max));
            for (int score = max - kizami; score >= 0; score -= kizami) {
                list.add(new Range(score, score + kizami - 1));
            }
            return list;
        }
        private List getInRangeStudentList(final List targetStudentList, final String subclasscd) {
            final List list = new ArrayList();
            for (final Iterator sit = targetStudentList.iterator(); sit.hasNext();) {
                final Student student = (Student) sit.next();
                final Score s = (Score) student._scoreMap.get(subclasscd);
                if (null != s && null != s._score) {
                    final int iscore = Integer.parseInt(s._score);
                    if (_lower <= iscore && iscore <= _higher) {
                        list.add(student);
                        sit.remove();
                    }
                }
            }
            return list;
        }
        public String toString() {
            return "{ lower : " + _lower + ", higher : " + _higher + "}";
        }
    }

    
    private static class Subclass implements Comparable {
        final String _subclasscd;
        final String _subclassname;
        final String _subclassabbv;
        final String _requireFlg;

        Subclass(
                final String subclasscd,
                final String subclassname,
                final String subclassabbv,
                final String requireFlg) {
            _subclasscd = subclasscd;
            _subclassname = subclassname;
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

    private static class Score {
        final String _chaircd;
        final String _subclasscd;
        final String _score;
        final BigDecimal _avg;
        final String _classRank;
        final String _classAvgRank;

        Score(
                final String chaircd,
                final String subclasscd,
                final String score,
                final BigDecimal avg,
                final String classRank,
                final String classAvgRank) {
            _chaircd = chaircd;
            _subclasscd = subclasscd;
            _score = score;
            _avg = avg;
            _classRank = classRank;
            _classAvgRank = classAvgRank;
        }
    }

    private static class Student {
        final String _grade;
        final String _hrClass;
        final String _attendno;
        final String _schregno;
        final String _name;
        final Map _scoreMap = new HashMap();
        final Map _score9990009Map = new HashMap();

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
        final String _substaffname;
        final List _studentList;
        final Map _subclassMap;

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
            _subclassMap = new HashMap();
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
            for (final Iterator it = _hrList.iterator(); it.hasNext();) {
                final HrClass hr = (HrClass) it.next();
                studentList.addAll(hr._studentList);
            }
            return studentList;
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
                stb.append("       HRSUBSTF.STAFFNAME AS SUBSTAFFNAME, ");
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
                stb.append("     LEFT JOIN STAFF_MST HRSUBSTF ON HRSUBSTF.STAFFCD = HDAT.SUBTR_CD1 ");
                stb.append("     WHERE ");
                stb.append("         T1.YEAR = '" + param._year + "' ");
                stb.append("         AND T1.SEMESTER = '" + param.getRegdSemester() + "' ");
                stb.append("         AND T1.GRADE = '" + param._grade + "' ");
                stb.append("         AND CGRP.GROUP_CD = '" + param._groupCd + "' ");
                stb.append("         AND NOT EXISTS (SELECT 'X' FROM SCHREG_BASE_MST W4 ");
                stb.append("                          WHERE W4.SCHREGNO = T1.SCHREGNO ");
                stb.append("                            AND ((W4.GRD_DIV IN('2','3') AND W4.GRD_DATE < '" + param._idouDate + "') ");
                stb.append("                             OR  (W4.ENT_DIV IN('4','5') AND W4.ENT_DATE > '" + param._idouDate + "')) ");
                stb.append("             ) ");
                stb.append("         AND NOT EXISTS (SELECT 'X' FROM SCHREG_TRANSFER_DAT W5 ");
                stb.append("                          WHERE W5.SCHREGNO = T1.SCHREGNO ");
                stb.append("                            AND (W5.TRANSFERCD IN ('1','2') AND '" + param._idouDate + "' BETWEEN W5.TRANSFER_SDATE AND W5.TRANSFER_EDATE) ");
                stb.append("             ) ");
                stb.append(" ) ");
                stb.append(" SELECT ");
                stb.append("   T1.GRADE_NAME1, ");
                stb.append("   T1.GROUP_CD, ");
                stb.append("   T1.GROUP_NAME, ");
                stb.append("   T1.GRADE, ");
                stb.append("   T1.HR_CLASS, ");
                stb.append("   T1.HR_NAME, ");
                stb.append("   T1.STAFFNAME, ");
                stb.append("   T1.SUBSTAFFNAME, ");
                stb.append("   T1.ATTENDNO, ");
                stb.append("   T1.SCHREGNO, ");
                stb.append("   T1.NAME, ");
                stb.append("   T2.CHAIRCD, ");
                stb.append("   T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD AS SUBCLASSCD, ");
                stb.append("   SUBM.SUBCLASSNAME, ");
                stb.append("   SUBM.SUBCLASSABBV, ");
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
                stb.append("   CAST(NULL AS VARCHAR(1)) AS SUBCLASSABBV, ");
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
                stb.append("     AND TRANK.SUBCLASSCD = '" + SUBCLASSCD_777777 + "' ");
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
                stb.append("   T1.SUBSTAFFNAME, ");
                stb.append("   T1.ATTENDNO, ");
                stb.append("   T1.SCHREGNO, ");
                stb.append("   T1.NAME, ");
                stb.append("   CAST(NULL AS VARCHAR(1)) AS CHAIRCD, ");
                stb.append("   TRANK.SUBCLASSCD AS SUBCLASSCD, ");
                stb.append("   CAST(NULL AS VARCHAR(1)) AS SUBCLASSNAME, ");
                stb.append("   CAST(NULL AS VARCHAR(1)) AS SUBCLASSABBV, ");
                stb.append("   CAST(NULL AS VARCHAR(1)) AS REQUIRE_FLG, ");
                stb.append("   TRANK.SEMESTER, ");
                stb.append("   TRANK.TESTKINDCD || TRANK.TESTITEMCD || TRANK.SCORE_DIV AS TESTCD, ");
                stb.append("   TRANK.SCORE, ");
                stb.append("   TRANK.AVG, ");
                stb.append("   TRANK.CLASS_RANK, ");
                stb.append("   TRANK.CLASS_AVG_RANK ");
                stb.append(" FROM REGD T1 ");
                stb.append(" INNER JOIN RECORD_RANK_SDIV_DAT TRANK ON TRANK.YEAR = T1.YEAR ");
                stb.append("     AND TRANK.SEMESTER || TRANK.TESTKINDCD || TRANK.TESTITEMCD || TRANK.SCORE_DIV = '" + SEM_TESTCD_GAKUNENHYOTEI + "' ");
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
                        final String substaffname = rs.getString("SUBSTAFFNAME");
                        
                        final HrClass hr = new HrClass(grade, hrClass, hrName, staffname, substaffname);
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
                    if (null == subclasscd) {
                        continue;
                    }
                    
                    if (null == hr._subclassMap.get(subclasscd)) {

                        final String subclassname = rs.getString("SUBCLASSNAME");
                        final String subclassabbv = rs.getString("SUBCLASSABBV");
                        final String requireFlg = rs.getString("REQUIRE_FLG");

                        final Subclass subclass = new Subclass(subclasscd, subclassname, subclassabbv, requireFlg);
                        hr._subclassMap.put(subclasscd, subclass);
                    }
                    
                    final Student student = Student.getStudent(schregno, hr._studentList);
                    final String chaircd = rs.getString("CHAIRCD");

                    final String semester = rs.getString("SEMESTER");
                    final String testcd = rs.getString("TESTCD");

                    final Score score = new Score(chaircd, subclasscd, rs.getString("SCORE"), rs.getBigDecimal("AVG"), rs.getString("CLASS_RANK"), rs.getString("CLASS_AVG_RANK"));
                    if (SEM_TESTCD_GAKUNENHYOTEI.equals(semester + testcd)) {
                        student._score9990009Map.put(subclasscd, score);
                    } else {
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
        
        public static RecordAverageDat getGradeAvg(final Map map, final String subclasscd, final String grade) {
            final String avgDivKey = "1" + "-" + grade + "-" + "000" + "-" + "00000000";
            return (RecordAverageDat) getMappedMap(map, subclasscd).get(avgDivKey);
        }
        
        public static RecordAverageDat getHrAvg(final Map map, final String subclasscd, final String grade, final String hrClass) {
            final String avgDivKey = "2" + "-" + grade + "-" + hrClass + "-" + "00000000";
            return (RecordAverageDat) getMappedMap(map, subclasscd).get(avgDivKey);
        }
        
        public static RecordAverageDat getCourseAvg(final Map map, final String subclasscd, final String grade, final String coursecd, final String majorcd, final String coursecode) {
            final String avgDivKey = "3" + "-" + grade + "-" + "000" + "-" + coursecd + majorcd + coursecode;
            return (RecordAverageDat) getMappedMap(map, subclasscd).get(avgDivKey);
        }
        
        public static RecordAverageDat getCourseGroupAvg(final Map map, final String subclasscd, final String grade, final String coursegroupCd) {
            final String avgDivKey = "5" + "-" + grade + "-" + "000" + "-" + "0" + coursegroupCd + "0000";
            return (RecordAverageDat) getMappedMap(map, subclasscd).get(avgDivKey);
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
                stb.append("  RECORD_AVERAGE_SDIV_DAT  ");
                stb.append(" WHERE  ");
                stb.append("  YEAR = '" + param._year + "' ");
                stb.append("  AND SEMESTER = '" + param._semester + "' ");
                stb.append("  AND TESTKINDCD || TESTITEMCD || SCORE_DIV= '" + param._testcd + "' ");

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
                    getMappedMap(map, subclasscd).put(avgDivKey, recordaveragedat);
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
    
    private static class Perfect {
        private static int getPerfect(final DB2UDB db2, final Param param, final String subclasscd, final String groupCd) {
            final int testCnt = getTestCnt(db2, param, groupCd, subclasscd);

            final Map perfect = getRow(db2, getPerfectSql(param, groupCd, subclasscd));
            log.debug(" query perfect = " + perfect);
            int perfectSumPerfect = parseInt((String) perfect.get("SUM_PERFECT"), 0);
            final int perfectCnt = parseInt((String) perfect.get("CNT"), 0);
            if (perfectCnt == 0 && testCnt == 0) {
                return 0;
            }

            if (perfectCnt != testCnt) {
                perfectSumPerfect += (testCnt - perfectCnt) * 100;
            }
            //log.debug(" sumPerfect = " + perfectSumPerfect);
            return perfectSumPerfect;
        }
        
        private static int parseInt(final String s, final int def) {
           return NumberUtils.isDigits(s) ? Integer.parseInt(s) : def;
        }

        public static int getTestCnt(final DB2UDB db2, final Param param, final String groupCd, final String subclasscd) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     COUNT(DISTINCT SEMESTER || TESTKINDCD || TESTITEMCD) AS COUNT ");
            stb.append(" FROM ");
            stb.append("     ADMIN_CONTROL_TESTITEM_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.GRADE = '" + param._grade + "' ");
            stb.append("     AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '" + subclasscd + "' ");
            stb.append("     AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE ");
            stb.append("         IN (SELECT ");
            stb.append("                 I1.COURSECD || I1.MAJORCD || I1.COURSECODE ");
            stb.append("             FROM ");
            stb.append("                 COURSE_GROUP_CD_DAT I1 ");
            stb.append("             WHERE ");
            stb.append("                 I1.YEAR    = '" + param._year + "' ");
            stb.append("                 AND I1.GRADE   = '" + param._grade + "' ");
            stb.append("                 AND I1.GROUP_CD   = '" + groupCd + "' ");
            stb.append("         ) ");

            //log.debug(" testCnt sql = " + stb.toString());
            final int testSubclassCnt = getOneInt(db2, stb.toString());
            stb.delete(0, stb.length());
            final String testSubclasscd = testSubclassCnt > 0 ? subclasscd : "00" + "-" + param._schoolKind + "-" + "00" + "-" + "000000";
            
            final StringBuffer sstb = new StringBuffer();
            sstb.append(" SELECT ");
            sstb.append("     COUNT(DISTINCT SEMESTER || TESTKINDCD || TESTITEMCD) AS COUNT ");
            sstb.append(" FROM ");
            sstb.append("     ADMIN_CONTROL_TESTITEM_DAT T1 ");
            sstb.append(" WHERE ");
            sstb.append("     T1.YEAR = '" + param._year + "' ");
            if (!param._isRuiseki) {
                sstb.append("     AND T1.SEMESTER = '" + param._semester + "' ");
            }
            if (param._isRuiseki || !param._isRuiseki && "99".equals(param._testcd.substring(0, 2))) {
                sstb.append("     AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD <= '" + param._semester + param._testcd.substring(0, 4) + "' ");
            } else {
                sstb.append("     AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD = '" + param._semester + param._testcd.substring(0, 4) + "' ");
            }
            sstb.append("     AND T1.GRADE = '" + param._grade + "' ");
            sstb.append("     AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '" + testSubclasscd + "' ");
            sstb.append("     AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE ");
            sstb.append("         IN (SELECT ");
            sstb.append("                 I1.COURSECD || I1.MAJORCD || I1.COURSECODE ");
            sstb.append("             FROM ");
            sstb.append("                 COURSE_GROUP_CD_DAT I1 ");
            sstb.append("             WHERE ");
            sstb.append("                 I1.YEAR    = '" + param._year + "' ");
            sstb.append("                 AND I1.GRADE   = '" + param._grade + "' ");
            sstb.append("                 AND I1.GROUP_CD   = '" + groupCd + "' ");
            sstb.append("         ) ");
            //log.debug(" testCnt sql = " + sstb.toString());
            final int testCnt = getOneInt(db2, sstb.toString());
            stb.delete(0, sstb.length());
            log.debug(" testSubclasscd = " + testSubclasscd + ", testCnt = " + testCnt);
            return testCnt;
        }

        //満点取得
        private static String getPerfectSql(final Param param, final String groupCd, final String subclasscd) {
            final StringBuffer stb2 = new StringBuffer();
            stb2.append(" SELECT ");
            stb2.append("     SUM(T1.PERFECT) AS SUM_PERFECT, ");
            stb2.append("     COUNT(*) AS CNT ");
            stb2.append(" FROM ");
            stb2.append("     PERFECT_RECORD_SDIV_DAT T1 ");
            stb2.append(" WHERE ");
            stb2.append("     T1.YEAR = '" + param._year + "' ");
            if (!param._isRuiseki) {
                stb2.append("     AND T1.SEMESTER = '" + param._semester + "' ");
            }
            if (param._isRuiseki || !param._isRuiseki && "99".equals(param._testcd.substring(0, 2))) {
                stb2.append("     AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD <= '" + param._semester + param._testcd.substring(0, 4) + "' ");
            } else {
                stb2.append("     AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD = '" + param._semester + param._testcd.substring(0, 4) + "' ");
            }
            stb2.append("     AND T1.TESTKINDCD IN ('01', '02') ");
            stb2.append("     AND T1.SCORE_DIV = '01' ");
            stb2.append("     AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '" + subclasscd + "' ");
            stb2.append("     AND T1.GRADE = CASE WHEN T1.DIV = '01' THEN '00' ELSE '" + param._grade + "' END ");
            stb2.append("     AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE = ");
            stb2.append("         CASE WHEN T1.DIV IN ('01','02') ");
            stb2.append("              THEN '00000000' ");
            stb2.append("              ELSE '0' || '" + groupCd + "' || '0000' END ");

            return stb2.toString();
        }
        
        private static Map getRow(final DB2UDB db2, final String sql) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map m = new HashMap();
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                final ResultSetMetaData meta = rs.getMetaData();
                if (rs.next()) {
                    for (int i = 1; i <= meta.getColumnCount(); i++) {
                        m.put(meta.getColumnName(i), rs.getString(meta.getColumnName(i)));
                    }
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return m;
        }
        
        private static int getOneInt(final DB2UDB db2, final String sql) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            int rtn = 0;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    rtn = rs.getInt(1);
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
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
        final String _schoolKind;
        final String _date;
        final String _idouDate;
        final String _testitemname;
        final String _semestername;
        final boolean _isRuiseki = false;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _grade = request.getParameter("GRADE");
            _gradeHrclass = request.getParameter("GRADE_HR_CLASS");
            _testcd = request.getParameter("TESTKIND_ITEMCD");
            _schoolKind = getSchoolKind(db2);
            _groupCd = request.getParameter("GROUP_CD");
            _date = request.getParameter("DATE");
            _idouDate = KNJ_EditDate.H_Format_Haifun(request.getParameter("IDOU_DATE"));

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
    }
}

// eof

