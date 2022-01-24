/*
 * $Id: 164789ad37d02e394ff75a9a5d993cc1be5fbebd $
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
 * 文京学園 クラス・コース別順位表
 */
public class KNJD666J {

    private static final Log log = LogFactory.getLog(KNJD666J.class);

    private boolean _hasData;

    private static String SUBCLASSCD_555555 = "555555";

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

    private static String toString(final Object o) {
        return null == o ? null : o.toString();
    }

    private static String sishaGonyu(final BigDecimal bd) {
        return null == bd ? null : bd.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {

        final int MAX_STUDENT = 50;

        final Map avgDatMap = RecordAverageDat.getRecordAverageDatMap(db2, _param);

        final List groupList = Group.getGroupList(db2, _param);
        for (int cgi = 0; cgi < groupList.size(); cgi++) {
            final Group group = (Group) groupList.get(cgi);

            final String avgDivKey;
//            if ("2".equals(_param._output)) {
//                avgDivKey = RecordAverageDat.getCourseAvgDivKey(_param._grade, group._groupCd);
//            } else {
                avgDivKey = RecordAverageDat.getHrAvgDivKey(_param._grade, group._groupCd.substring(2));
//            }

            final List subclassList = new ArrayList(group._subclassMap.values());
            Collections.sort(subclassList);
            log.info(" subclasscds = " + subclassList);

            Collections.sort(group._studentList, new Student.Comparator(_param));
            final List studentPageList = getGroupList(group._studentList, MAX_STUDENT);
            for (int pi = 0; pi < studentPageList.size(); pi++) {
                final List studentList = (List) studentPageList.get(pi);

//                 final String form = "2".equals(_param._output) ? "KNJD666J_2.frm" : "KNJD666J_1.frm";
                final String form = "KNJD666J_1.frm";
                 svf.VrSetForm(form, 4);

//                 svf.VrsOut("TITLE", KenjaProperties.gengou(Integer.parseInt(_param._year)) + "年度　" + StringUtils.defaultString(_param._semestername) + "　" + StringUtils.defaultString(_param._testitemname) + "　" + ("2".equals(_param._output) ? "コース別" : "クラス別") + "素点順位表（" + ("2".equals(_param._ranking) ? "成績順" : "番号順") + "）"); // タイトル
                 svf.VrsOut("TITLE", KenjaProperties.gengou(Integer.parseInt(_param._year)) + "年度　" + StringUtils.defaultString(_param._testitemname) + "　クラス別成績一覧表（" + ("2".equals(_param._ranking) ? "成績順" : "番号順") + "）"); // タイトル
                 //svf.VrsOut("SUB_TITLE", null); // サブタイトル
                 svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._loginDate)); //
                 //svf.VrsOut("GRADE_COURSE", null); // 学年コース名
//                 if ("2".equals(_param._output)) {
//                     svf.VrsOut("COURSE_NAME", group._groupName); // コース名
//                 } else {
                     svf.VrsOut("HR_NAME", group._groupName); // 年組
                     if (null != group._substaffname) {
                         svf.VrsOut("TEACHER_NAME", "担任　" + StringUtils.defaultString(group._staffname)); // 担任名
                         
                         svf.VrsOut("TEACHER_NAME2", "副担任　" + StringUtils.defaultString(group._substaffname)); // 担任名

                     } else {
                         svf.VrsOut("TEACHER_NAME2", "担任　" + StringUtils.defaultString(group._staffname)); // 担任名
                     }
//                 }

                 for (int sti = 0; sti < studentList.size(); sti++) {
                     final Student student = (Student) studentList.get(sti);
                     final int line = sti + 1;
//                     if ("2".equals(_param._output)) {
                         svf.VrsOutn("HR_NAME", line, student._hrClassName1); // 年組
//                     }
                     svf.VrsOutn("NO", line, NumberUtils.isDigits(student._attendno) ? String.valueOf(Integer.parseInt(student._attendno)) : student._attendno); // 番号
                     svf.VrsOutn("NAME", line, student._name); // 氏名
                 }
                 final boolean isPrintSubclassTotal = pi == studentPageList.size() - 1; // 生徒の最後のページに母集団の平均点、偏差値を表示

                 for (int subi = 0; subi < 5; subi++) {
                     svf.VrsOut("GRP", "1"); // グループコード
                     if (subi == 2) {
                         if (_param._testcd.startsWith("99")) {
                             svf.VrsOut("SELECT_MARK", "得"); // 「素点」or「得点」
                         } else {
                             svf.VrsOut("SELECT_MARK", "素"); // 「素点」or「得点」
                         }
                     }
                     if (subi < subclassList.size()) {
                         final Subclass subclass = (Subclass) subclassList.get(subi);
                         svf.VrsOut("SUBCLASS_NAME", subclass._subclassabbv); // 科目名
                         for (int sti = 0; sti < studentList.size(); sti++) {
                             final int line = sti + 1;
                             final Student student = (Student) studentList.get(sti);
                             final Score s = (Score) student._scoreMap.get(subclass._subclasscd);
                             if (null != s) {
                                 svf.VrsOutn("SCORE", line, s._score); // 素点
                             }
                         }
                         if (isPrintSubclassTotal) {
                             final RecordAverageDat avgDat = RecordAverageDat.get(avgDatMap, avgDivKey, subclass._subclasscd);
                             if (null != avgDat) {
                                 svf.VrsOut("SUBCLASS_TOTAL", sishaGonyu(avgDat._avg)); // 科目合計
                             }
                         }
                     }
                     svf.VrEndRecord();
                 }
                 for (int subi = 5; subi < 9; subi++) {
                     svf.VrsOut("GRP", "1"); // グループコード
                     if (subi == 5) {
                         svf.VrsOut("SUBCLASS_NAME", "合　計　点"); // 科目名
                     } else if (subi == 6) {
                         svf.VrsOut("SELECT_MARK", "点"); // 「素点」
                         svf.VrsOut("SUBCLASS_NAME", "平　均　点"); // 科目名
                     } else if (subi == 7) {
                         svf.VrsOut("SUBCLASS_NAME", "クラス席次"); // 科目名
                     } else if (subi == 8) {
                         svf.VrsOut("SUBCLASS_NAME", "学年席次"); // 科目名
                     }
                     final List avgList = new ArrayList();
                     for (int sti = 0; sti < studentList.size(); sti++) {
                         final int line = sti + 1;
                         final Student student = (Student) studentList.get(sti);
                         final Score s = (Score) student._scoreMap.get(SUBCLASSCD_555555);
                         if (null != s) {
                             if (subi == 5) {
                                 svf.VrsOutn("SCORE", line, s._score); // 合計点
                             } else if (subi == 6) {
                                 svf.VrsOutn("SCORE", line, sishaGonyu(s._avg)); // 平均点
                                 if (null != s._avg) {
                                     avgList.add(s._avg);
                                 }
                             } else if (subi == 7) {
                                 svf.VrsOutn("SCORE", line, toString(s._classRank)); // クラス席次
                             } else if (subi == 8) {
                                 svf.VrsOutn("SCORE", line, toString(s._gradeRank)); // 学年席次
                             }
                         }
                     }
                     if (isPrintSubclassTotal) {
                         if (subi == 5) {
                             final RecordAverageDat avgDat = RecordAverageDat.get(avgDatMap, avgDivKey, SUBCLASSCD_555555);
                             if (null != avgDat) {
                                 svf.VrsOut("SUBCLASS_TOTAL", sishaGonyu(avgDat._avg)); // 科目合計
                             }
                         } else if (subi == 6) {
                             svf.VrsOut("SUBCLASS_TOTAL", sishaGonyu(getAvg(avgList))); // 科目合計
                         }
                     }
                     svf.VrEndRecord();
                 }
                 for (int subi = 9; subi < 14; subi++) {
                     svf.VrsOut("GRP", "2"); // グループコード
                     if (subi == 10) {
                         svf.VrsOut("SELECT_MARK", "偏"); // 「偏差値」
                     } else if (subi == 11) {
                         svf.VrsOut("SELECT_MARK", "差"); // 「偏差値」
                     } else if (subi == 12) {
                         svf.VrsOut("SELECT_MARK", "値"); // 「偏差値」
                     }
                     if (subi - 9 < subclassList.size()) {
                         final Subclass subclass = (Subclass) subclassList.get(subi - 9);
                         svf.VrsOut("SUBCLASS_NAME", subclass._subclassabbv); // 科目名
                         final List deviationList = new ArrayList();
                         for (int sti = 0; sti < studentList.size(); sti++) {
                             final int line = sti + 1;
                             final Student student = (Student) studentList.get(sti);
                             final Score s = (Score) student._scoreMap.get(subclass._subclasscd);
                             if (null != s) {
//                                 final BigDecimal dev = "2".equals(_param._output) ? s._courseDeviation : s._classDeviation;
                                 final BigDecimal dev = s._gradeDeviation;
                                 svf.VrsOutn("SCORE", line, sishaGonyu(dev)); // 素点
                                 if (null != dev) {
                                     deviationList.add(dev);
                                 }
                             }
                         }
//                         if (isPrintSubclassTotal) {
//                             svf.VrsOut("SUBCLASS_TOTAL", sishaGonyu(getAvg(deviationList))); // 科目合計
//                         }
                     }
                     svf.VrEndRecord();
                 }

                 for (int subi = 14; subi < 15; subi++) {
                     svf.VrsOut("GRP", "2"); // グループコード
                     svf.VrsOut("SUBCLASS_NAME", "合　　　計"); // 科目名
                     final List deviationList = new ArrayList();
                     for (int sti = 0; sti < studentList.size(); sti++) {
                         final int line = sti + 1;
                         final Student student = (Student) studentList.get(sti);
                         final Score s = (Score) student._scoreMap.get(SUBCLASSCD_555555);
                         if (null != s) {
//                             final BigDecimal dev = "2".equals(_param._output) ? s._courseDeviation : s._classDeviation;
                             final BigDecimal dev = s._gradeDeviation;
                             svf.VrsOutn("SCORE", line, sishaGonyu(dev)); // 素点
                             if (null != dev) {
                                 deviationList.add(dev);
                             }
                         }
                     }
//                     if (isPrintSubclassTotal) {
//                         svf.VrsOut("SUBCLASS_TOTAL", sishaGonyu(getAvg(deviationList))); // 科目合計
//                     }
                     svf.VrEndRecord();
                 }
                 _hasData = true;
            }
        }
    }

    private static BigDecimal getAvg(final List bdList) {
        BigDecimal total = new BigDecimal(0);
        int count = 0;
        for (final Iterator it = bdList.iterator(); it.hasNext();) {
            final BigDecimal num = (BigDecimal) it.next();
            if (null != num) {
                total = total.add(num);
                count += 1;
            }
        }
        if (count == 0) {
            return null;
        }
        return total.divide(new BigDecimal(count), 1, BigDecimal.ROUND_HALF_UP);
    }

    private static BigDecimal getAvgAvg(final List printStudentList, final String subclasscd) {
        List total = new ArrayList();
        for (final Iterator it = printStudentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            final Score score = (Score) student._scoreMap.get(subclasscd);
            if (null != score && null != score._avg) {
                total.add(score._avg);
            }
        }
        return getAvg(total);
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
        String _score;
        final BigDecimal _avg;
        final Integer _classRank;
        final Integer _classAvgRank;
        final BigDecimal _classDeviation;
        final Integer _courseRank;
        final Integer _courseAvgRank;
        final BigDecimal _courseDeviation;
        final Integer _gradeRank;
        final Integer _gradeAvgRank;
        final BigDecimal _gradeDeviation;

        Score(
                final String chaircd,
                final String subclasscd,
                final String score,
                final BigDecimal avg,
                final Integer classRank,
                final Integer classAvgRank,
                final BigDecimal classDeviation,
                final Integer courseRank,
                final Integer courseAvgRank,
                final BigDecimal courseDeviation,
                final Integer gradeRank,
                final Integer gradeAvgRank,
                final BigDecimal gradeDeviation) {
            _chaircd = chaircd;
            _subclasscd = subclasscd;
            _score = score;
            _avg = avg;
            _classRank = classRank;
            _classAvgRank = classAvgRank;
            _classDeviation = classDeviation;
            _courseRank = courseRank;
            _courseAvgRank = courseAvgRank;
            _courseDeviation = courseDeviation;
            _gradeRank = gradeRank;
            _gradeAvgRank = gradeAvgRank;
            _gradeDeviation = gradeDeviation;
        }
    }

    private static class Subclass implements Comparable {
        final String _subclasscd;
        final String _subclassname;
        final String _subclassabbv;
        final String _requireFlg;
        final Integer _recSubclassGroup5;

        Subclass(
                final String subclasscd,
                final String subclassname,
                final String subclassabbv,
                final String requireFlg,
                final Integer recSubclassGroup5) {
            _subclasscd = subclasscd;
            _subclassname = subclassname;
            _subclassabbv = subclassabbv;
            _requireFlg = requireFlg;
            _recSubclassGroup5 = recSubclassGroup5;
        }

        public int compareTo(Object o) {
            final Subclass subclass = (Subclass) o;
//            String requireFlg1 = StringUtils.defaultString(_requireFlg, "0");
//            String requireFlg2 = StringUtils.defaultString(subclass._requireFlg, "0");
//            if (!"3".equals(requireFlg1) && "3".equals(requireFlg2)) {
//                return -1;
//            } else if ("3".equals(requireFlg1) && !"3".equals(requireFlg2)) { // 選択科目は後
//                return 1;
//            }
            int rtn;
            rtn = _recSubclassGroup5.compareTo(subclass._recSubclassGroup5);
            if (0 != rtn) {
                return rtn;
            }
            return _subclasscd.compareTo(subclass._subclasscd);
        }

        public String toString() {
            return "Subclass(" + _subclasscd + ", " + _subclassname + ")";
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
        final Map _scoreMap = new HashMap();

        Student(
            final String grade,
            final String hrClass,
            final String hrName,
            final String hrClassName1,
            final String attendno,
            final String schregno,
            final String name) {
            _grade = grade;
            _hrClass = hrClass;
            _hrName = hrName;
            _hrClassName1 = hrClassName1;
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

        public Integer rankForCompare() {
            final Score scoreAll = (Score) _scoreMap.get(SUBCLASSCD_555555);
            if (null != scoreAll && null != scoreAll._gradeRank) {
                return scoreAll._gradeRank;
            }
            return new Integer(99999999);
        }

        private static class Comparator implements java.util.Comparator {
            final Param _param;
            Comparator(final Param param) {
                _param = param;
            }
            public int compare(final Object o1, final Object o2) {
                int cmp = 0;
                final Student s1 = (Student) o1;
                final Student s2 = (Student) o2;
                if ("2".equals(_param._ranking)) {
                    // 成績順
                    cmp = s1.rankForCompare().compareTo(s2.rankForCompare());
                    if (0 != cmp) return cmp;
                }
                return (s1._grade + s1._hrClass + s1._attendno).compareTo(s2._grade + s2._hrClass + s2._attendno);
            }
        }
    }

    private static class Group {
        final String _groupCd;
        final String _groupName;
        final String _staffname;
        final String _substaffname;
        final List _studentList;
        final Map _subclassMap;

        Group(
            final String groupCd,
            final String groupName,
            final String staffname,
            final String substaffname
        ) {
            _groupCd = groupCd;
            _groupName = groupName;
            _staffname = staffname;
            _substaffname = substaffname;
            _studentList = new ArrayList();
            _subclassMap = new HashMap();
        }

        public static List getGroupList(final DB2UDB db2, final Param param) {
            final List groupList = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH REGD AS ( ");
            stb.append("     SELECT ");
            stb.append("       T1.YEAR, ");
            stb.append("       T1.SEMESTER, ");
            stb.append("       GDAT.GRADE_NAME1, ");
            stb.append("       T1.GRADE, ");
            stb.append("       T1.HR_CLASS, ");
            stb.append("       HDAT.HR_NAME, ");
            stb.append("       HDAT.HR_CLASS_NAME1, ");
            stb.append("       HRSTF.STAFFNAME, ");
            stb.append("       HRSUBSTF.STAFFNAME AS SUBSTAFFNAME, ");
            stb.append("       T1.ATTENDNO, ");
            stb.append("       T1.SCHREGNO, ");
            stb.append("       T1.COURSECD, ");
            stb.append("       T1.MAJORCD, ");
            stb.append("       T1.COURSECODE, ");
            stb.append("       CM.COURSENAME, ");
            stb.append("       MM.MAJORNAME, ");
            stb.append("       CCM.COURSECODENAME, ");
            stb.append("       BASE.NAME ");
            stb.append("     FROM SCHREG_REGD_DAT T1 ");
            stb.append("     INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = T1.SCHREGNO ");
            stb.append("     INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR ");
            stb.append("         AND GDAT.GRADE = T1.GRADE ");
            stb.append("     INNER JOIN SCHREG_REGD_HDAT HDAT ON HDAT.YEAR = T1.YEAR ");
            stb.append("         AND HDAT.SEMESTER = T1.SEMESTER ");
            stb.append("         AND HDAT.GRADE = T1.GRADE ");
            stb.append("         AND HDAT.HR_CLASS = T1.HR_CLASS ");
            stb.append("     LEFT JOIN STAFF_MST HRSTF ON HRSTF.STAFFCD = HDAT.TR_CD1 ");
            stb.append("     LEFT JOIN STAFF_MST HRSUBSTF ON HRSUBSTF.STAFFCD = HDAT.SUBTR_CD1 ");
            stb.append("     LEFT JOIN COURSE_MST CM ON CM.COURSECD = T1.COURSECD ");
            stb.append("     LEFT JOIN MAJOR_MST MM ON MM.COURSECD = T1.COURSECD AND MM.MAJORCD = T1.MAJORCD ");
            stb.append("     LEFT JOIN COURSECODE_MST CCM ON CCM.COURSECODE = T1.COURSECODE ");
            stb.append("     WHERE ");
            stb.append("         T1.YEAR = '" + param._year + "' ");
            stb.append("         AND T1.SEMESTER = '" + param.getRegdSemester()  + "' ");
            stb.append("         AND T1.GRADE = '" + param._grade + "' ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("   T1.GRADE_NAME1, ");
            stb.append("   T1.GRADE, ");
            stb.append("   T1.HR_CLASS, ");
            stb.append("   T1.HR_NAME, ");
            stb.append("   T1.HR_CLASS_NAME1, ");
            stb.append("   T1.STAFFNAME, ");
            stb.append("   T1.SUBSTAFFNAME, ");
            stb.append("   T1.ATTENDNO, ");
            stb.append("   T1.SCHREGNO, ");
            stb.append("   T1.NAME, ");
            stb.append("   T1.COURSECD, ");
            stb.append("   T1.MAJORCD, ");
            stb.append("   T1.COURSECODE, ");
            stb.append("   T1.COURSENAME, ");
            stb.append("   T1.MAJORNAME, ");
            stb.append("   T1.COURSECODENAME, ");
            stb.append("   T2.CHAIRCD, ");
            stb.append("   T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("   CASE WHEN RGRP5.YEAR IS NOT NULL THEN 1 ELSE 2 END AS RGRP5_ORDER, ");
            stb.append("   SUBM.SUBCLASSNAME, ");
            stb.append("   SUBM.SUBCLASSABBV, ");
            stb.append("   TREC.SEMESTER, ");
            stb.append("   TREC.TESTKINDCD || TREC.TESTITEMCD || TREC.SCORE_DIV AS TESTCD, ");
            stb.append("   TREC.VALUE_DI, ");
            stb.append("   TRANK.SCORE, ");
            stb.append("   TRANK.AVG, ");
            stb.append("   TRANK.CLASS_RANK, ");
            stb.append("   TRANK.CLASS_AVG_RANK, ");
            stb.append("   TRANK.CLASS_DEVIATION, ");
            stb.append("   TRANK.COURSE_RANK, ");
            stb.append("   TRANK.COURSE_AVG_RANK, ");
            stb.append("   TRANK.COURSE_DEVIATION, ");
            stb.append("   TRANK.GRADE_RANK, ");
            stb.append("   TRANK.GRADE_AVG_RANK, ");
            stb.append("   TRANK.GRADE_DEVIATION ");
            stb.append(" FROM REGD T1 ");
            stb.append(" LEFT JOIN CHAIR_STD_DAT T2 ON T2.YEAR = T1.YEAR ");
            stb.append("     AND T2.SEMESTER <= T1.SEMESTER ");
            stb.append("     AND T2.SCHREGNO = T1.SCHREGNO ");
            stb.append(" LEFT JOIN CHAIR_DAT T3 ON T3.YEAR = T2.YEAR ");
            stb.append("     AND T3.SEMESTER = T2.SEMESTER ");
            stb.append("     AND T3.CHAIRCD = T2.CHAIRCD ");
            stb.append("     AND T3.CLASSCD <= '90' ");
            stb.append(" LEFT JOIN SUBCLASS_MST SUBM ON SUBM.CLASSCD = T3.CLASSCD ");
            stb.append("     AND SUBM.SCHOOL_KIND = T3.SCHOOL_KIND ");
            stb.append("     AND SUBM.CURRICULUM_CD = T3.CURRICULUM_CD ");
            stb.append("     AND SUBM.SUBCLASSCD = T3.SUBCLASSCD ");
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
            stb.append(" LEFT JOIN SUBCLASS_REPLACE_COMBINED_DAT COMB ON COMB.YEAR = T3.YEAR ");
            stb.append("     AND COMB.ATTEND_CLASSCD = T3.CLASSCD ");
            stb.append("     AND COMB.ATTEND_SCHOOL_KIND = T3.SCHOOL_KIND ");
            stb.append("     AND COMB.ATTEND_CURRICULUM_CD = T3.CURRICULUM_CD ");
            stb.append("     AND COMB.ATTEND_SUBCLASSCD = T3.SUBCLASSCD ");
            stb.append(" LEFT JOIN REC_SUBCLASS_GROUP_DAT RGRP5 ON RGRP5.YEAR = T3.YEAR ");
            stb.append("     AND RGRP5.GROUP_DIV = '5' ");
            stb.append("     AND RGRP5.GRADE = T1.GRADE ");
            stb.append("     AND RGRP5.COURSECD = T1.COURSECD ");
            stb.append("     AND RGRP5.MAJORCD = T1.MAJORCD ");
            stb.append("     AND RGRP5.COURSECODE = T1.COURSECODE ");
            stb.append("     AND RGRP5.CLASSCD = T3.CLASSCD ");
            stb.append("     AND RGRP5.SCHOOL_KIND = T3.SCHOOL_KIND ");
            stb.append("     AND RGRP5.CURRICULUM_CD = T3.CURRICULUM_CD ");
            stb.append("     AND RGRP5.SUBCLASSCD = T3.SUBCLASSCD ");
            stb.append(" WHERE ");
            stb.append("     COMB.YEAR IS NULL ");
            stb.append(" UNION ALL ");
            stb.append(" SELECT ");
            stb.append("   T1.GRADE_NAME1, ");
            stb.append("   T1.GRADE, ");
            stb.append("   T1.HR_CLASS, ");
            stb.append("   T1.HR_NAME, ");
            stb.append("   T1.HR_CLASS_NAME1, ");
            stb.append("   T1.STAFFNAME, ");
            stb.append("   T1.SUBSTAFFNAME, ");
            stb.append("   T1.ATTENDNO, ");
            stb.append("   T1.SCHREGNO, ");
            stb.append("   T1.NAME, ");
            stb.append("   T1.COURSECD, ");
            stb.append("   T1.MAJORCD, ");
            stb.append("   T1.COURSECODE, ");
            stb.append("   T1.COURSENAME, ");
            stb.append("   T1.MAJORNAME, ");
            stb.append("   T1.COURSECODENAME, ");
            stb.append("   CAST(NULL AS VARCHAR(1)) AS CHAIRCD, ");
            stb.append("   TRANK.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("   1 AS RGRP5_ORDER, ");
            stb.append("   CAST(NULL AS VARCHAR(1)) AS SUBCLASSNAME, ");
            stb.append("   CAST(NULL AS VARCHAR(1)) AS SUBCLASSABBV, ");
            stb.append("   TRANK.SEMESTER, ");
            stb.append("   TRANK.TESTKINDCD || TRANK.TESTITEMCD || TRANK.SCORE_DIV AS TESTCD, ");
            stb.append("   CAST(NULL AS VARCHAR(1)) AS VALUE_DI, ");
            stb.append("   TRANK.SCORE, ");
            stb.append("   TRANK.AVG, ");
            stb.append("   TRANK.CLASS_RANK, ");
            stb.append("   TRANK.CLASS_AVG_RANK, ");
            stb.append("   TRANK.CLASS_DEVIATION, ");
            stb.append("   TRANK.COURSE_RANK, ");
            stb.append("   TRANK.COURSE_AVG_RANK, ");
            stb.append("   TRANK.COURSE_DEVIATION, ");
            stb.append("   TRANK.GRADE_RANK, ");
            stb.append("   TRANK.GRADE_AVG_RANK, ");
            stb.append("   TRANK.GRADE_DEVIATION ");
            stb.append(" FROM REGD T1 ");
            stb.append(" INNER JOIN RECORD_RANK_SDIV_DAT TRANK ON TRANK.YEAR = T1.YEAR ");
            stb.append("     AND TRANK.SEMESTER = '" + param._semester + "' AND TRANK.TESTKINDCD || TRANK.TESTITEMCD || TRANK.SCORE_DIV = '" + param._testcd + "' ");
            stb.append("     AND TRANK.SUBCLASSCD IN ('" + SUBCLASSCD_555555 + "') ");
            stb.append("     AND TRANK.SCHREGNO = T1.SCHREGNO ");
            stb.append(" ORDER BY ");
//            if ("2".equals(param._output)) {
//                stb.append("     COURSECD, ");
//                stb.append("     MAJORCD, ");
//                stb.append("     COURSECODE, ");
//            }
            stb.append("     GRADE, ");
            stb.append("     HR_CLASS, ");
            stb.append("     ATTENDNO ");

            final Map schregMap = new HashMap();
            try {
                final String sql = stb.toString();
                log.debug(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                final Map groupMap = new HashMap();
                while (rs.next()) {
                    final String groupCd;
                    final String groupName;
                    final String grade = rs.getString("GRADE");
                    final String hrClass = rs.getString("HR_CLASS");
                    String staffname = null;
                    String substaffname = null;
//                    if ("2".equals(param._output)) {
//                        // コースごと
//                        groupCd = rs.getString("COURSECD") + rs.getString("MAJORCD") + rs.getString("COURSECODE");
//                        groupName = StringUtils.defaultString(rs.getString("MAJORNAME")) + StringUtils.defaultString(rs.getString("COURSECODENAME"));
//                    } else {
                        // HRごと
                        groupCd = grade + hrClass;
                        groupName = rs.getString("HR_NAME");
                        staffname = rs.getString("STAFFNAME");
                        substaffname = rs.getString("SUBSTAFFNAME");
//                    }

                    if (null == groupMap.get(groupCd)) {
                        final Group group = new Group(groupCd, groupName, staffname, substaffname);
                        groupList.add(group);
                        groupMap.put(groupCd, group);
                    }
                    final Group group = (Group) groupMap.get(groupCd);

                    final String schregno = rs.getString("SCHREGNO");
                    if (null == Student.getStudent(schregno, group._studentList)) {
                        final String hrName = rs.getString("HR_NAME");
                        final String hrClassName1 = rs.getString("HR_CLASS_NAME1");
                        final String attendno = rs.getString("ATTENDNO");
                        final String name = rs.getString("NAME");

                        final Student student = new Student(grade, hrClass, hrName, hrClassName1, attendno, schregno, name);
                        schregMap.put(schregno, student);
                        group._studentList.add(student);
                    }

                    final String subclasscd = rs.getString("SUBCLASSCD");
                    if (null != subclasscd) {
                        final Student student = Student.getStudent(schregno, group._studentList);
                        final String chaircd = rs.getString("CHAIRCD");

                        final String subclassname = rs.getString("SUBCLASSNAME");
                        final String subclassabbv = rs.getString("SUBCLASSABBV");
                        final String requireFlg = null; // rs.getString("REQUIRE_FLG");
                        final Integer rgrp5Order = (Integer) rs.getObject("RGRP5_ORDER");

                        if (!SUBCLASSCD_555555.equals(subclasscd)) {
                            final Subclass subclass = new Subclass(subclasscd, subclassname, subclassabbv, requireFlg, rgrp5Order);
                            group._subclassMap.put(subclasscd, subclass);
                        }

                        final String score = null != rs.getString("VALUE_DI") ? rs.getString("VALUE_DI") : rs.getString("SCORE");
                        final Score s = new Score(chaircd, subclasscd, score, rs.getBigDecimal("AVG")
                                , toInteger(rs.getString("CLASS_RANK")), toInteger(rs.getString("CLASS_AVG_RANK")), rs.getBigDecimal("CLASS_DEVIATION")
                                , toInteger(rs.getString("COURSE_RANK")), toInteger(rs.getString("COURSE_AVG_RANK")), rs.getBigDecimal("COURSE_DEVIATION")
                                , toInteger(rs.getString("GRADE_RANK")), toInteger(rs.getString("GRADE_AVG_RANK")), rs.getBigDecimal("GRADE_DEVIATION"));
                        student._scoreMap.put(subclasscd, s);
                    }
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return groupList;
        }

        private static Integer toInteger(final String s) {
            if (NumberUtils.isDigits(s)) {
                return Integer.valueOf(s);
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

        public static String getCourseAvgDivKey(final String grade, final String course) {
            return "3" + "-" + grade + "-" + "000" + "-" + course;
        }

        public static String getCourseGroupAvgDivKey(final String grade, final String coursegroupCd) {
            return "5" + "-" + grade + "-" + "000" + "-" + "0" + coursegroupCd + "0000";
        }

        public static Map getRecordAverageDatMap(final DB2UDB db2, final Param param) {
            PreparedStatement ps = null;
            ResultSet rs = null;
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
            stb.append("  AND SEMESTER || TESTKINDCD || TESTITEMCD || SCORE_DIV= '" + param._semester + param._testcd + "' ");

            final Map map = new HashMap();
            final String SUBCLASSCD_333333 = "333333";
            final String SUBCLASSCD_777777 = "777777";
            final String SUBCLASSCD_888888 = "888888";
            final String SUBCLASSCD_999999 = "999999";
            try {
                final String sql = stb.toString();
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String subclasscd;
                    if (SUBCLASSCD_333333.equals(rs.getString("SUBCLASSCD")) || SUBCLASSCD_555555.equals(rs.getString("SUBCLASSCD")) || SUBCLASSCD_777777.equals(rs.getString("SUBCLASSCD")) || SUBCLASSCD_888888.equals(rs.getString("SUBCLASSCD")) || SUBCLASSCD_999999.equals(rs.getString("SUBCLASSCD"))) {
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
        final String _testcd;
        final String _loginDate;
        //final String _output; // 1:クラス別 2:コース別
        final String _ranking; // 順位 1:番号順 2:成績順
        final String _prgid;

        final String _testitemname;
        final String _semestername;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _grade = request.getParameter("GRADE");
            _testcd = request.getParameter("TESTKIND_ITEMCD");
            _loginDate = request.getParameter("LOGIN_DATE");
            //_output = request.getParameter("OUTPUT");
            _ranking = request.getParameter("RANKING");
            _prgid = request.getParameter("PRGID");

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

