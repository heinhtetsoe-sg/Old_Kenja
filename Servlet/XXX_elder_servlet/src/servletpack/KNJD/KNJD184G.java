/*
 * $Id: 7b6aef443d85c0b08963c6f10853460f70e7a861 $
 *
 * 作成日: 2012/02/21
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

import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_Control;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

/**
 * 学校教育システム 賢者 [成績管理] 通知票
 */

public class KNJD184G {

    private static final Log log = LogFactory.getLog(KNJD184G.class);

    private boolean _hasData;
    
    private final String _19900 = "19900";
    private final String _29900 = "29900";
    private final String _99901 = "99901";
    private final String _99900 = "99900";
    
    private static final String SUBCLASSALL = "999999";

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
        
        final List studentList = getStudentList(db2);
        _param._d026Map = getD026Map(db2);
        
        log.debug(" testcdList = " + _param.getTestcdList());

        AttendSemesDat.setAttendSemesDatList(db2, _param, studentList);

        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            student._subclassList = Subclass.getSubclassList(db2, _param, student._schregno);
            student._subclassMap = toSubclassMap(student._subclassList);
            student._courseWeightingSubclassCdListMap = Student.loadCourseWeightingSubclassCdListMap(db2, _param);
        }
        Subclass.setAttendSubclassList(db2, _param, studentList);

        for (final Iterator it = studentList.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();
            log.debug(" student = " + student._schregno + " : " + student._name);
            printSvfMainSeiseki(db2, svf, student);
        }
    }
    
    private Map toSubclassMap(final List subclassList) {
        final Map map = new HashMap();
        for (final Iterator it = subclassList.iterator(); it.hasNext();) {
            final Subclass subclass = (Subclass) it.next();
            map.put(subclass._subclasscd, subclass);
        }
        return map;
    }

    private List getStudentList(final DB2UDB db2) {
        final List studentList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getStudentSql(_param);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String schregno = rs.getString("SCHREGNO");
                final String name = "1".equals(rs.getString("USE_REAL_NAME")) ? rs.getString("REAL_NAME") : rs.getString("NAME");
                final String hrName = rs.getString("HR_NAME");
                final String attendno = rs.getString("ATTENDNO");
                final String gradeCourse = rs.getString("COURSE");
                final Student student = new Student(schregno, name, hrName, attendno, gradeCourse);
                studentList.add(student);
            }
            
        } catch (SQLException e) {
            log.error("exception!!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return studentList;
    }
    
    private String getStudentSql(final Param param) {
        final StringBuffer stb = new StringBuffer();
        // 異動者を除外した学籍の表 => 組平均において異動者の除外に使用
        stb.append("WITH SCHNO_A AS(");
        stb.append("  SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.GRADE || T1.COURSECD || T1.MAJORCD || T1.COURSECODE AS COURSE, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     T1.SEMESTER ");
        stb.append("  FROM    SCHREG_REGD_DAT T1 ");
        stb.append("          , SEMESTER_MST T2 ");
        stb.append("  WHERE ");
        stb.append("     T1.YEAR = '" + param._year + "' ");
        stb.append("     AND T1.SEMESTER = '"+ param._semester +"' ");
        stb.append("     AND T1.YEAR = T2.YEAR ");
        stb.append("     AND T1.SEMESTER = T2.SEMESTER ");
        stb.append("     AND T1.GRADE || T1.HR_CLASS = '" + param._gradeHrclass + "' ");
        stb.append("     AND T1.SCHREGNO IN " + SQLUtils.whereIn(true, param._categorySelected) + " ");
        //                      在籍チェック:転学(2)・退学(3)者は除外 但し異動日が学期終了日または異動基準日より小さい場合
        //                                   転入(4)・編入(5)者は除外 但し異動日が学期終了日または異動基準日より大きい場合 
        stb.append("     AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST S1 ");
        stb.append("                     WHERE   S1.SCHREGNO = T1.SCHREGNO ");
        stb.append("                         AND ((S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < CASE WHEN T2.EDATE < '" + param._date + "' THEN T2.EDATE ELSE '" + param._date + "' END) ");
        stb.append("                           OR (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE > CASE WHEN T2.EDATE < '" + param._date + "' THEN T2.EDATE ELSE '" + param._date + "' END)) ) ");
        //                      異動者チェック：留学(1)・休学(2)者は除外 但し学期終了日または基準日が異動開始日と終了日内にある場合
        stb.append("     AND NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT S1 ");
        stb.append("                     WHERE   S1.SCHREGNO = T1.SCHREGNO ");
        stb.append("                         AND S1.TRANSFERCD IN ('1','2') AND CASE WHEN T2.EDATE < '" + param._date + "' THEN T2.EDATE ELSE '" + param._date + "' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE) ");
        stb.append(" ) ");
        //メイン表
        stb.append(" SELECT ");
        stb.append("    T1.SCHREGNO, ");
        stb.append("    T7.HR_NAME, ");
        stb.append("    T1.ATTENDNO, ");
        stb.append("    T1.COURSE, ");
        stb.append("    T5.NAME, ");
        stb.append("    T5.REAL_NAME, ");
        stb.append("    CASE WHEN T6.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END AS USE_REAL_NAME ");
        stb.append(" FROM ");
        stb.append("    SCHNO_A T1 ");
        stb.append("    INNER JOIN SCHREG_BASE_MST T5 ON T1.SCHREGNO = T5.SCHREGNO ");
        stb.append("    LEFT JOIN SCHREG_NAME_SETUP_DAT T6 ON T6.SCHREGNO = T1.SCHREGNO AND T6.DIV = '03' ");
        stb.append("    LEFT JOIN SCHREG_REGD_HDAT T7 ON T7.YEAR = '" + param._year + "' AND T7.SEMESTER = T1.SEMESTER AND T7.GRADE = T1.GRADE AND T7.HR_CLASS = T1.HR_CLASS ");
        stb.append(" ORDER BY ATTENDNO");
        return stb.toString();
    }
    
    private int getMS932ByteCount(final String str) {
        int ret = 0;
        if (null != str) {
            try {
                ret = str.getBytes("MS932").length;
            } catch (Exception e) {
                log.error("exception!", e);
            }
        }
        return ret;
    }
    
    private boolean notTargetSubclasscd(final String fullSubclasscd, final String div) {
        if (null == fullSubclasscd) {
            return true;
        }
        for (final Iterator it = _param._d026Map.keySet().iterator(); it.hasNext();) {
            final String subclasscd = (String) it.next();
            if (fullSubclasscd.endsWith(subclasscd)) {
                final Map m = (Map) _param._d026Map.get(subclasscd);
                return (null != m.get(div)) ? true : false;
            }
        }
        return false;
    }
    
    private Map getD026Map(final DB2UDB db2) {
        final Map d026Map = new HashMap();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final StringBuffer stb = new StringBuffer();
            stb.append("   SELECT ");
            stb.append("     T1.NAME1, ");
            stb.append("     T1.ABBV1, ");
            stb.append("     T1.NAMESPARE1 ");
            stb.append("   FROM ");
            stb.append("       V_NAME_MST T1 ");
            stb.append("   WHERE ");
            stb.append("       T1.YEAR = '" + _param._year + "' ");
            stb.append("       AND T1.NAMECD1 = 'D026' ");
            stb.append("       AND T1.NAME1 IS NOT NULL ");
            
            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();
            while (rs.next()) {
                final Map m = getMappedMap(d026Map, rs.getString("NAME1"));
                m.put("ABBV1", rs.getString("ABBV1"));
                m.put("NAMESPARE1", rs.getString("NAMESPARE1"));
            }
        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return d026Map;
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
    
    /**
     * 学習のようす等を印刷する
     * @param db2
     * @param svf
     * @param student
     * @param viewClassList
     */
    private void printSvfMainSeiseki(
            final DB2UDB db2,
            final Vrw32alp svf,
            final Student student) {
        
        final String form = "KNJD184G.frm";
        svf.VrSetForm(form, 1);
        
        printSvfHeader(svf, student);
        
        printSvfAttendSemes(svf, student);

        printSvfRecordRank(svf, student);

        svf.VrEndPage();
        _hasData = true;
    }
    
    private void printSvfHeader(final Vrw32alp svf, final Student student) {
        final String attendno = null == student._attendno ? "" : (NumberUtils.isDigits(student._attendno)) ? String.valueOf(Integer.parseInt(student._attendno) + "番"): student._attendno;
        svf.VrsOut("NAME", StringUtils.defaultString(student._hrName) + attendno + "　" + StringUtils.defaultString(student._name));

//        svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._descDate));
        
//        svf.VrsOut("SCHOOL_NAME", _param._certifSchoolRemark3);
        
        svf.VrsOut("JOB_NAME", "担任");
        
        svf.VrsOut("TEACHER_NAME", _param._tr1Name);
        
        svf.VrsOut("SEMESTER1_1", (String) _param._semesternameMap.get("1")); // 学期
        svf.VrsOut("SEMESTER1_2", (String) _param._semesternameMap.get("2")); // 学期
        svf.VrsOut("SEMESTER1_3", (String) _param._semesternameMap.get("9")); // 学期
        svf.VrsOutn("SEMESTER2", 1, (String) _param._semesternameMap.get("1")); // 学期
        svf.VrsOutn("SEMESTER2", 2, (String) _param._semesternameMap.get("2")); // 学期
        svf.VrsOutn("SEMESTER2", 3, (String) _param._semesternameMap.get("9")); // 学期
    }
    
    private static String notZero(int n) {
        return String.valueOf(n);
    }
    
    /**
     * 『出欠の記録』を印字する
     * @param svf
     * @param student
     */
    private void printSvfAttendSemes(final Vrw32alp svf, final Student student) {
        for (final Iterator it = student._attendSemesDatList.iterator(); it.hasNext();) {
            final AttendSemesDat attendSemesDat = (AttendSemesDat) it.next();
            if (!NumberUtils.isDigits(attendSemesDat._semester)) {
                continue;
            }
            
            final int j;
            if ("9".equals(attendSemesDat._semester)) {
                j = 3; // 3学期制の場合
            } else {
                j = Integer.parseInt(attendSemesDat._semester);
            }
            svf.VrsOutn("LESSON", j, notZero(attendSemesDat._lesson));
            svf.VrsOutn("SUSPEND", j, notZero(attendSemesDat._suspend + attendSemesDat._mourning));
            svf.VrsOutn("PRESENT", j, notZero(attendSemesDat._mlesson));
            svf.VrsOutn("ATTEND", j, notZero(attendSemesDat._present));
            svf.VrsOutn("ABSENCE", j, notZero(attendSemesDat._sick));
            svf.VrsOutn("LATE", j, notZero(attendSemesDat._late));
            svf.VrsOutn("EARLY", j, notZero(attendSemesDat._early));
        }
    }
    
    /**
     * 学習の記録
     * @param svf
     * @param student
     */
    private void printSvfRecordRank(final Vrw32alp svf, final Student student) {
        // 全教科
        final int maxLine = 29;

        Subclass scoreSubclass999999 = null;
        int line;
        line = 1;
        final List testcdList = _param.getTestcdList();
        for (final Iterator it = student._subclassList.iterator(); it.hasNext();) {
            final Subclass subclass = (Subclass) it.next();
            if (SUBCLASSALL.equals(subclass._subclasscd)) {
                scoreSubclass999999 = subclass;
                continue;
            }

            if (notTargetSubclasscd(subclass._subclasscd, "ABBV1")) {
                continue;
            }
            
            if (subclass._subclasscd.startsWith("9")) {
                continue;
            }

            svf.VrsOutn(getMS932ByteCount(subclass._subclassname) <= 20 ? "SUBCLASS1_1" : "SUBCLASS1_2", line, subclass._subclassname); // 科目名
            
            for (final Iterator tit = testcdList.iterator(); tit.hasNext();) {
                final String semtestcd = (String) tit.next();
                final SubclassDetail detail = (SubclassDetail) subclass._detailMap.get(semtestcd);
                if (null == detail) {
                    continue;
                }
                if (_19900.equals(semtestcd)) {
                    svf.VrsOutn("VALUE1", line, detail._score); // 学期評価
                    svf.VrsOutn("ABSENT1", line, student.getKekka(detail)); // 欠課時数
                } else if (_29900.equals(semtestcd)) {
                    svf.VrsOutn("VALUE2", line, detail._score); // 学期評価
                    svf.VrsOutn("ABSENT2", line, student.getKekka(detail)); // 欠課時数
                }
            }
            line++;
        }
        
        if (Integer.parseInt(_param._knjSchoolMst._semesterDiv) <= Integer.parseInt(_param._semester)) {
            line = 1;
            for (final Iterator it = student._subclassList.iterator(); it.hasNext();) {
                final Subclass subclass = (Subclass) it.next();
                if (SUBCLASSALL.equals(subclass._subclasscd)) {
                    continue;
                }
                if (notTargetSubclasscd(subclass._subclasscd, "NAMESPARE1")) {
                    continue;
                }
                svf.VrsOutn(getMS932ByteCount(subclass._subclassname) <= 20 ? "SUBCLASS2_1" : "SUBCLASS2_2", line, subclass._subclassname); // 科目名

                for (final Iterator tit = testcdList.iterator(); tit.hasNext();) {
                    final String semtestcd = (String) tit.next();
                    final SubclassDetail detail = (SubclassDetail) subclass._detailMap.get(semtestcd);
                    if (null == detail) {
                        continue;
                    }
                    if (_99901.equals(semtestcd)) {
                        svf.VrsOutn("VALUE3", line, detail._score); // 学期評価
                    } else if (_99900.equals(semtestcd)) {
                        svf.VrsOutn("KEKKA", line, student.getKekka(detail)); // 欠課時数
                        svf.VrsOutn("DIV", line, detail._score); // 評定
                        svf.VrsOutn("LESSON_TIME", line, student.getJisu(detail)); // 授業時数
                        svf.VrsOutn("CREDIT", line, detail._getCredit); // 修得単位
                    }
                }
                line++;
            }
        }

        if (null != scoreSubclass999999) {
            for (final Iterator it = testcdList.iterator(); it.hasNext();) {
                final String semtestcd = (String) it.next();
                if (null == scoreSubclass999999._detailMap.get(semtestcd)) {
                    continue;
                }
                final SubclassDetail detail = (SubclassDetail) scoreSubclass999999._detailMap.get(semtestcd);
                if (_19900.equals(semtestcd)) {
                    svf.VrsOutn("VALUE1", maxLine - 1, detail._score); // 学期評価
                    svf.VrsOut("AVERAGE1", detail._avg); // 平均点
                    svf.VrsOut("G_VIEW1_1", detail._assessMark); // 概評
                    //svf.VrsOut("AVERAGE2", null); // 平均点
                    //svf.VrsOut("G_VIEW1_2", null); // 概評
                    
                } else if (_29900.equals(semtestcd)) {
                    svf.VrsOutn("VALUE2", maxLine - 1, detail._score); // 学期評価
                    svf.VrsOut("AVERAGE3", detail._avg); // 平均点
                    svf.VrsOut("G_VIEW1_3", detail._assessMark); // 概評
                    //svf.VrsOut("AVERAGE4", null); // 平均点
                    //svf.VrsOut("G_VIEW1_4", null); // 概評
                } else if (_99901.equals(semtestcd)) {
                        
                    svf.VrsOut("G_VIEW2_1", detail._assessMark); // 概評
//                  svf.VrsOut("G_VIEW2_2", null); // 概評
//                  svf.VrsOut("G_VIEW2_3", null); // 概評
//                  svf.VrsOut("G_VIEW\2_4", null); // 概評
//                  svf.VrsOut("G_VIEW2_5", null); // 概評
                }
            }
        }
    }
    
    private static class Student {
        final String _schregno;
        final String _name;
        final String _hrName;
        final String _attendno;
        final String _gradeCourse;
        List _attendSemesDatList = Collections.EMPTY_LIST; // 出欠のようす
        List _subclassList = Collections.EMPTY_LIST;
        Map _subclassMap = Collections.EMPTY_MAP;
        Map _courseWeightingSubclassCdListMap = Collections.EMPTY_MAP;
        
        public Student(final String schregno, final String name, final String hrName, final String attendno, final String gradeCourse) {
            _schregno = schregno;
            _name = name;
            _hrName = hrName;
            _attendno = attendno;
            _gradeCourse = gradeCourse;
        }

        private static String add(final String a, final String b) {
            if (!NumberUtils.isNumber(a)) {
                return b;
            }
            if (!NumberUtils.isNumber(b)) {
                return a;
            }
            return new BigDecimal(a).add(new BigDecimal(b)).toString();
        }

        public String getKekka(final SubclassDetail detail) {
            return getKekkaKind(0, detail);
        }

        public String getJisu(final SubclassDetail detail) {
            return getKekkaKind(1, detail);
        }

        public String getKekkaKind(final int flg, final SubclassDetail detail) {
            final List attendSubclasses = getAttendSubclass(_gradeCourse, detail._subclasscd);
            final String v = detail.getKind(flg);
            if (attendSubclasses.isEmpty()) {
                return NumberUtils.isNumber(v) && Double.parseDouble(v) == 0.0 ? "" : v;
            }
            String sum = v;
            log.info(" in " + detail._semtestcd + ":" + flg + " " + detail._subclasscd + " set " + v); 
            for (final Iterator it = attendSubclasses.iterator(); it.hasNext();) {
                final String attendSubclasscd = (String) it.next();
                final Subclass attendSubclass = (Subclass) _subclassMap.get(attendSubclasscd);
                if (null != attendSubclass) {
                    final SubclassDetail attendSubclassDetail = (SubclassDetail) attendSubclass._detailMap.get(detail._semtestcd);
                    if (null != attendSubclassDetail) {
                        final String kekkaKind = getKekkaKind(flg, attendSubclassDetail);
                        sum = add(sum, kekkaKind);
                        log.info(" in " + detail._semtestcd + ":" + flg + " " + detail._subclasscd + " <- " + attendSubclassDetail._subclasscd + "(" + kekkaKind + ")"); 
                    } else {
                        log.info(" in " + detail._semtestcd + ":" + flg + " " + detail._subclasscd + " <- " + attendSubclasscd + "(null)"); 
                    }
                }
            }
            return NumberUtils.isNumber(sum) && Double.parseDouble(sum) == 0.0 ? "" : sum;
        }

        private List getAttendSubclass(final String gradeCourse, final String combinedSubclassCd) {
            return getMappedList(getMappedMap(getMappedMap(_courseWeightingSubclassCdListMap, gradeCourse), "PAIR"), combinedSubclassCd);
        }
        
        private static Map loadCourseWeightingSubclassCdListMap(final DB2UDB db2, final Param param) {
            final Map courseWeightingSubclassCdListMap = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            //final String flg = "2"; // null != exam._testCd && exam._testCd.startsWith("99") ? "2" : "1";
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append("   SELECT ");
                stb.append("     T1.GRADE || T1.COURSECD || T1.MAJORCD || T1.COURSECODE AS COURSE, ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append("       T1.ATTEND_CLASSCD || '-' || T1.ATTEND_SCHOOL_KIND || '-' || T1.ATTEND_CURRICULUM_CD || '-' || ");
                }
                stb.append("       T1.ATTEND_SUBCLASSCD AS ATTEND_SUBCLASSCD, ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append("       T1.COMBINED_CLASSCD || '-' || T1.COMBINED_SCHOOL_KIND || '-' || T1.COMBINED_CURRICULUM_CD || '-' || ");
                }
                stb.append("       T1.COMBINED_SUBCLASSCD AS COMBINED_SUBCLASSCD ");
                stb.append("   FROM ");
                stb.append("       SUBCLASS_WEIGHTING_COURSE_DAT T1 ");
                stb.append("   WHERE ");
                stb.append("       T1.YEAR = '" + param._year + "' ");
                stb.append("       AND T1.GRADE = '" + param._grade + "' ");
                //stb.append("       AND T1.FLG = '" + flg + "' ");
                
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    getMappedList(getMappedMap(courseWeightingSubclassCdListMap, rs.getString("COURSE")), "ATTEND_SUBCLASS").add(rs.getString("ATTEND_SUBCLASSCD"));
                    getMappedList(getMappedMap(courseWeightingSubclassCdListMap, rs.getString("COURSE")), "COMBINED_SUBCLASS").add(rs.getString("COMBINED_SUBCLASSCD"));
                    final List attendSubclassCdList = getMappedList(getMappedMap(getMappedMap(courseWeightingSubclassCdListMap, rs.getString("COURSE")), "PAIR"), rs.getString("COMBINED_SUBCLASSCD"));
                    if (!attendSubclassCdList.contains(rs.getString("ATTEND_SUBCLASSCD"))) {
                        attendSubclassCdList.add(rs.getString("ATTEND_SUBCLASSCD"));
                    }
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
//            for (final Iterator cit = courseWeightingSubclassCdListMap.keySet().iterator(); cit.hasNext();) {
//                final String course = (String) cit.next();
//                final Map m = (Map) courseWeightingSubclassCdListMap.get(course);
//                log.debug(" course = " + course + ", attend subclass = " + m.get("ATTEND_SUBCLASS"));
//            }
            return courseWeightingSubclassCdListMap;
        }
    }
    
    /**
     * 出欠の記録
     */
    private static class AttendSemesDat {
        
        final String _semester;
        final int _lesson;
        final int _suspend;
        final int _mourning;
        final int _mlesson;
        final int _sick;
        final int _absent;
        final int _present;
        final int _late;
        final int _early;
        final int _transferDate;
        final int _offdays;
        
        public AttendSemesDat(
                final String semester,
                final int lesson,
                final int suspend,
                final int mourning,
                final int mlesson,
                final int sick,
                final int absent,
                final int present,
                final int late,
                final int early,
                final int transferDate,
                final int offdays
        ) {
            _semester = semester;
            _lesson = lesson;
            _suspend = suspend;
            _mourning = mourning;
            _mlesson = mlesson;
            _sick = sick;
            _absent = absent;
            _present = present;
            _late = late;
            _early = early;
            _transferDate = transferDate;
            _offdays = offdays;
        }
        
        private static void setAttendSemesDatList(final DB2UDB db2, final Param param, final List studentList) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                param._attendParamMap.put("schregno", "?");
                final String sql = AttendAccumulate.getAttendSemesSql(
                        param._year,
                        param._semester,
                        null,
                        param._date,
                        param._attendParamMap
                );
                ps = db2.prepareStatement(sql);
                
                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();
                    
                    student._attendSemesDatList = new ArrayList();
                    
                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        
                        final String semester = rs.getString("SEMESTER");
                        
                        if (!param._semester.equals(param._knjSchoolMst._semesterDiv) && Integer.parseInt(param._semester) < Integer.parseInt(semester)) {
                            continue;
                        }
                        
                        final int lesson = rs.getInt("LESSON");
                        final int suspend = rs.getInt("SUSPEND") + rs.getInt("VIRUS") + rs.getInt("KOUDOME");
                        final int mourning = rs.getInt("MOURNING");
                        final int mlesson = rs.getInt("MLESSON");
                        final int sick = rs.getInt("SICK");
                        final int absent = rs.getInt("ABSENT");
                        final int present = rs.getInt("PRESENT");
                        final int late = rs.getInt("LATE");
                        final int early = rs.getInt("EARLY");
                        final int transferDate = rs.getInt("TRANSFER_DATE");
                        final int offdays = rs.getInt("OFFDAYS");
                        
                        final AttendSemesDat attendSemesDat = new AttendSemesDat(semester, lesson, suspend, mourning, mlesson, sick, absent, present, late, early, transferDate, offdays);
                        
                        student._attendSemesDatList.add(attendSemesDat);
                    }
                    
                    DbUtils.closeQuietly(rs);
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
        
        public String toString() {
            return "AttendSemesDat(" + _semester + ": [" + _lesson + ", " + _suspend  + ", " + _mourning  + ", " + _mlesson  + ", " + _sick  + ", " + _present + ", " + _late + ", " + _early + "])";
        }
    }
    
    private static class Subclass {
        
        final String _subclasscd;
        final String _classname;
        final String _subclassname;
        final Map _detailMap = new HashMap();
        public Subclass(final String subclasscd, final String classname, final String subclassname) {
            _subclasscd = subclasscd;
            _classname = classname;
            _subclassname = subclassname;
        }
        
        private static Subclass getSubclass(final List list, final String subclasscd) {
            for (final Iterator it = list.iterator(); it.hasNext();) {
                final Subclass ss = (Subclass) it.next();
                if (subclasscd.equals(ss._subclasscd)) {
                    return ss;
                }
            }
            return null;
        }

        public static List getSubclassList(final DB2UDB db2, final Param param, final String schregno) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getSubclassSql(param, schregno);
                //log.debug(" subclass sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String subclasscd;
                    if ("1".equals(param._useCurriculumcd) && !SUBCLASSALL.equals(rs.getString("SUBCLASSCD"))) {
                        subclasscd = rs.getString("CLASSCD") + "-" + rs.getString("SCHOOL_KIND") + "-" + rs.getString("CURRICULUM_CD") + "-" + rs.getString("SUBCLASSCD");
                    } else {
                        subclasscd = rs.getString("SUBCLASSCD");
                    }
                    
                    Subclass subclass = getSubclass(list, subclasscd);
                    if (null == subclass) {
                        subclass = new Subclass(subclasscd, rs.getString("CLASSNAME"), rs.getString("SUBCLASSNAME"));
                        list.add(subclass);
                    }
                    
                    final String semtestcd = rs.getString("SEMTESTCD");
                    final String score = rs.getString("SCORE");
                    final String getCredit = rs.getString("GET_CREDIT");
                    final String assessmark = rs.getString("ASSESSMARK");
                    final String avg = null == rs.getString("AVG") ? null : new BigDecimal(rs.getString("AVG")).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                    if (null == subclass._detailMap.get(semtestcd)) {
                        subclass._detailMap.put(semtestcd, new SubclassDetail());
                    }
                    final SubclassDetail detail = (SubclassDetail) subclass._detailMap.get(semtestcd);
                    detail._subclasscd = subclasscd;
                    detail._semtestcd = semtestcd;
                    detail._score = score;
                    detail._getCredit = getCredit;
                    detail._avg = avg;
                    detail._assessMark = assessmark;
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }
        
        public static void setAttendSubclassList(final DB2UDB db2, final Param param, final List studentList) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                param._attendParamMap.put("schregno", "?");
                final String sql = AttendAccumulate.getAttendSubclassSql(
                        param._year,
                        param._semester,
                        null,
                        param._date,
                        param._attendParamMap
                );
                ps = db2.prepareStatement(sql);
                
                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();
                    
                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();

                    while (rs.next()) {
                        
                        final String semester = rs.getString("SEMESTER");
                        
                        if (!param._semester.equals(param._knjSchoolMst._semesterDiv) && Integer.parseInt(param._semester) < Integer.parseInt(semester)) {
                            continue;
                        }
                        
                        final String subclasscd = rs.getString("SUBCLASSCd");
                        final String semtestcd = semester + "9900";
                        final Subclass subclass = getSubclass(student._subclassList, subclasscd);
                        if (null == subclass) {
                            continue;
                        }
                        if (null == subclass._detailMap.get(semtestcd)) {
                            subclass._detailMap.put(semtestcd, new SubclassDetail());
                        }
                        final SubclassDetail detail = (SubclassDetail) subclass._detailMap.get(semtestcd);
                        detail._subclasscd = subclasscd;
                        detail._semtestcd = semtestcd;
                        detail._jisu = rs.getString("MLESSON");
                        detail._kekka = rs.getString("SICK2");
                    }
                    DbUtils.closeQuietly(rs);
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
        
        private static String getSubclassSql(final Param param, final String schregno) {
            final StringBuffer stb = new StringBuffer();
            
            stb.append(" WITH MAIN AS ( ");
            stb.append(" SELECT ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("        T1.CLASSCD, ");
                stb.append("        T1.SCHOOL_KIND, ");
                stb.append("        T1.CURRICULUM_CD, ");
            }
            stb.append("     T1.SUBCLASSCD, ");
            stb.append("     T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD AS SEMTESTCD, ");
            stb.append("     T2.SCORE, ");
            stb.append("     T1.GET_CREDIT, ");
            stb.append("     CAST(NULL AS DECIMAL(9,5)) AS AVG, ");
            stb.append("     CAST(NULL AS VARCHAR(1)) AS ASSESSMARK ");
            stb.append(" FROM ");
            stb.append("     RECORD_SCORE_DAT T1 ");
            stb.append("     LEFT JOIN RECORD_RANK_DAT T2 ON T2.YEAR = T1.YEAR AND T2.SEMESTER = T1.SEMESTER ");
            stb.append("        AND T2.TESTKINDCD = T1.TESTKINDCD AND T2.TESTITEMCD = T1.TESTITEMCD AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("        AND T2.CLASSCD = T1.CLASSCD AND T2.SCHOOL_KIND = T1.SCHOOL_KIND AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append("        AND T2.SCHREGNO = T1.SCHREGNO ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            if (!param._semester.equals(param._knjSchoolMst._semesterDiv)) {
                stb.append("     AND T1.SEMESTER <= '" + param._semester + "' ");
            }
            stb.append("     AND (T1.TESTKINDCD = '99' AND (T1.TESTITEMCD = '00' OR T1.TESTITEMCD = '01') AND T1.SCORE_DIV = '00') ");
            stb.append("     AND T1.SCHREGNO = '" + schregno + "' ");
            stb.append(" UNION ALL ");
            stb.append(" SELECT  ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("        T1.CLASSCD, ");
                stb.append("        T1.SCHOOL_KIND, ");
                stb.append("        T1.CURRICULUM_CD, ");
            }
            stb.append("     T1.SUBCLASSCD, ");
            stb.append("     T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD AS SEMTESTCD, ");
            stb.append("     T1.SCORE, ");
            stb.append("     CAST(NULL AS SMALLINT) AS GET_CREDIT, ");
            stb.append("     T1.AVG, ");
            stb.append("     T3.ASSESSMARK ");
            stb.append(" FROM ");
            stb.append("     RECORD_RANK_DAT T1 ");
            stb.append("     LEFT JOIN SCHREG_REGD_DAT REGD ON REGD.SCHREGNO = T1.SCHREGNO ");
            stb.append("         AND REGD.YEAR = T1.YEAR ");
            stb.append("         AND REGD.SEMESTER = '" + param._semester + "' ");
            if ("1".equals(param._useAssessCourseMst)) {
                stb.append("     LEFT JOIN ASSESS_COURSE_MST T2 ON T2.ASSESSCD = '3' AND ROUND(T1.AVG, 0) BETWEEN T2.ASSESSLOW AND T2.ASSESSHIGH ");
                stb.append("         AND T2.COURSECD = REGD.COURSECD AND T2.MAJORCD = REGD.MAJORCD AND T2.COURSECODE = REGD.COURSECODE ");
                stb.append("     LEFT JOIN ASSESS_COURSE_MST T3 ON T3.ASSESSCD = '4' AND T2.ASSESSLEVEL BETWEEN T3.ASSESSLOW AND T3.ASSESSHIGH ");
                stb.append("         AND T3.COURSECD = REGD.COURSECD AND T3.MAJORCD = REGD.MAJORCD AND T3.COURSECODE = REGD.COURSECODE ");
            } else {
                stb.append("     LEFT JOIN ASSESS_MST T2 ON T2.ASSESSCD = '3' AND ROUND(T1.AVG, 0) BETWEEN T2.ASSESSLOW AND T2.ASSESSHIGH ");
                stb.append("     LEFT JOIN ASSESS_MST T3 ON T3.ASSESSCD = '4' AND T2.ASSESSLEVEL BETWEEN T3.ASSESSLOW AND T3.ASSESSHIGH ");
            }
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            if (!param._semester.equals(param._knjSchoolMst._semesterDiv)) {
                stb.append("     AND T1.SEMESTER <= '" + param._semester + "' ");
            }
            stb.append("     AND (T1.TESTKINDCD = '99' AND (T1.TESTITEMCD = '00' OR T1.TESTITEMCD = '01')) ");
            stb.append("     AND T1.SUBCLASSCD = '" + SUBCLASSALL + "' ");
            stb.append("     AND T1.SCHREGNO = '" + schregno + "' ");
            stb.append(" ), ORDER AS ( ");
            stb.append(" SELECT DISTINCT ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     T1.CLASSCD, ");
                stb.append("     T1.SCHOOL_KIND, ");
                stb.append("     T1.CURRICULUM_CD, ");
            }
            stb.append("     T1.SUBCLASSCD, ");
            stb.append("     T4.CLASSNAME, ");
            stb.append("     VALUE(T3.SUBCLASSORDERNAME2, T3.SUBCLASSNAME) AS SUBCLASSNAME, ");
            stb.append("     VALUE(T4.SHOWORDER3, 99) AS ORDER1, ");
            stb.append("     VALUE(T3.SHOWORDER3, 99) AS ORDER2 ");
            stb.append(" FROM  ");
            stb.append("     MAIN T1 ");
            stb.append(" LEFT JOIN SUBCLASS_MST T3 ON ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(" T3.CLASSCD = T1.CLASSCD AND T3.SCHOOL_KIND = T1.SCHOOL_KIND AND T3.CURRICULUM_CD = T1.CURRICULUM_CD AND ");
            }
            stb.append(" T3.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append(" LEFT JOIN CLASS_MST T4 ON ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(" T4.CLASSCD = T1.CLASSCD AND T4.SCHOOL_KIND = T1.SCHOOL_KIND ");
            } else {
                stb.append(" T4.CLASSCD = SUBSTR(T1.SUBCLASSCD, 1, 2) ");
            }
            stb.append(" ORDER BY ");
            stb.append("     VALUE(T4.SHOWORDER3, 99), VALUE(T4.SHOWORDER3, 99), T1.SUBCLASSCD ");
            stb.append(" ) ");
            stb.append(" SELECT  ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("        T1.CLASSCD, ");
                stb.append("        T1.SCHOOL_KIND, ");
                stb.append("        T1.CURRICULUM_CD, ");
            }
            stb.append("     T1.SUBCLASSCD, ");
            stb.append("     T2.CLASSNAME, ");
            stb.append("     T2.SUBCLASSNAME, ");
            stb.append("     T1.SEMTESTCD, ");
            stb.append("     T1.SCORE, ");
            stb.append("     T1.GET_CREDIT, ");
            stb.append("     T1.AVG, ");
            stb.append("     T1.ASSESSMARK, ");
            stb.append("     VALUE(T2.ORDER1, 99) AS ORDER1, ");
            stb.append("     VALUE(T2.ORDER2, 99) AS ORDER2 ");
            stb.append(" FROM  ");
            stb.append("     MAIN T1 ");
            stb.append(" LEFT JOIN ORDER T2 ON ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || ");
            }
            stb.append("   T2.SUBCLASSCD = ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
            }
            stb.append("   T1.SUBCLASSCD ");
            stb.append(" WHERE ");
            if ("1".equals(param._useCurriculumcd) && "1".equals(param._useClassDetailDat)) {
                stb.append("     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD NOT IN ( ");
                stb.append("         SELECT ");
                stb.append("             T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD ");
                stb.append("         FROM ");
                stb.append("             SUBCLASS_DETAIL_DAT T1 ");
                stb.append("         WHERE ");
                stb.append("             YEAR = '" + param._year + "' ");
                stb.append("             AND SUBCLASS_SEQ = '007' ");
                if ("1".equals(param._semester)) {
                    stb.append("         AND SUBCLASS_REMARK1 = '1' ");
                } else if ("2".equals(param._semester)) {
                    stb.append("         AND SUBCLASS_REMARK2 = '1' ");
                } else if ("3".equals(param._semester)) {
                    stb.append("         AND SUBCLASS_REMARK3 = '1' ");
                } else if ("9".equals(param._semester)) {
                    stb.append("         AND SUBCLASS_REMARK4 = '1' ");
                }
                stb.append("         ) ");
            } else {
                stb.append("     T1.SUBCLASSCD NOT IN ( ");
                stb.append("         SELECT ");
                stb.append("             NAME1 ");
                stb.append("         FROM ");
                stb.append("             NAME_MST ");
                stb.append("         WHERE ");
                stb.append("             NAMECD1 = 'D026' ");
                if ("1".equals(param._semester)) {
                    stb.append("         AND ABBV1 = '1' ");
                } else if ("2".equals(param._semester)) {
                    stb.append("         AND ABBV2 = '1' ");
                } else if ("3".equals(param._semester)) {
                    stb.append("         AND ABBV3 = '1' ");
                } else if ("9".equals(param._semester)) {
                    stb.append("         AND NAMESPARE1 = '1' ");
                }
                stb.append("         ) ");
            }
            stb.append(" ORDER BY ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     VALUE(T2.ORDER1, 99), T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, VALUE(T2.ORDER2, 99) ");
            } else {
                stb.append("     VALUE(T2.ORDER1, 99), SUBSTR(T1.SUBCLASSCD, 1, 2), VALUE(T2.ORDER2, 99) ");
            }
            stb.append("     , T1.SUBCLASSCD, T1.SEMTESTCD ");
            return stb.toString();
        }
        
        public String toString() {
            return "ScoreSubclass(" + _subclasscd + ":" + _subclassname + ":" + _detailMap + ")";
        }
    }
    
    private static class SubclassDetail {
        String _subclasscd;
        String _semtestcd;
        String _score;
        String _getCredit;
        String _avg;
        String _assessMark;
        String _jisu;
        String _kekka;
        
        private String getKind(final int flg) {
            if (flg == 1) {
                return _jisu;
            }
            return _kekka;
        }
        
        public String toString() {
            return "[score=" + _score + ", avg=" + _avg + ", rank=" + _assessMark + "]";
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
    private class Param {
        final String _year;
        final String _semester;
        final String _date;
        final String _gradeHrclass;
        final String _grade;
        final String[] _categorySelected;
        String _trCd1;
//        final String _documentRoot;
//        final String _imagePath;
//        final String _extension;
//        final String _descDate;
        
//        final String _certifSchoolSchoolName;
//        final String _certifSchoolRemark3;
//        final String _certifSchoolPrincipalName;
        final String _tr1Name;
//        final String _certifSchoolJobName;
        
        /** 教育課程コードを使用するか */
        final String _useCurriculumcd;
        final String _useClassDetailDat;
        final String _useAssessCourseMst;

       /** 各学校における定数等設定 */
        KNJSchoolMst _knjSchoolMst;
        Map _d026Map = Collections.EMPTY_MAP;
        
        Map _semesternameMap;
        final Map _attendParamMap;
        
        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("CTRL_YEAR");
            _semester = request.getParameter("SEMESTER");
            _date = request.getParameter("DATE").replace('/', '-');
            _gradeHrclass = request.getParameter("GRADE_HR_CLASS");
            _grade = null != _gradeHrclass && _gradeHrclass.length() > 2 ? _gradeHrclass.substring(0, 2) : null;
            _categorySelected = request.getParameterValues("category_selected");
            setTrCd1(db2);
//            _documentRoot = request.getParameter("DOCUMENTROOT");
//            
//            final KNJ_Control.ReturnVal returnval = getDocumentroot(db2);
//            _imagePath = null == returnval ? null : returnval.val4;
//            _extension = null == returnval ? null : returnval.val5; // 写真データの拡張子
//            _descDate = request.getParameter("DESC_DATE");
            
//            _certifSchoolSchoolName = getCertifSchoolDat(db2, "SCHOOL_NAME");
//            _certifSchoolRemark3 = getCertifSchoolDat(db2, "REMARK3");
//            _certifSchoolPrincipalName = getCertifSchoolDat(db2, "PRINCIPAL_NAME");
            _tr1Name = getStaffname(db2, _trCd1);
//            _hyosiHrName = _gradeCdStr + "年 " + getHrClassName1(db2, _year, _semester, _gradeHrclass) + "組";
//            _certifSchoolJobName = getCertifSchoolDat(db2, "JOB_NAME");
            
            try {
                _knjSchoolMst = new KNJSchoolMst(db2, _year);
            } catch (SQLException e) {
                log.warn("学校マスタ取得でエラー", e);
            }
            
            // 学期名称 _arrsemesName をセットします。
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _useClassDetailDat = request.getParameter("useClassDetailDat");
            _useAssessCourseMst = request.getParameter("useAssessCourseMst");
            _semesternameMap = getSemesterName(db2);
            
            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
        }
        
        /**
         * 写真データ格納フォルダの取得 --NO001
         */
        private KNJ_Control.ReturnVal getDocumentroot(final DB2UDB db2) {
            KNJ_Control.ReturnVal returnval = null;
            try {
                KNJ_Control imagepath_extension = new KNJ_Control(); // 取得クラスのインスタンス作成
                returnval = imagepath_extension.Control(db2);
            } catch (Exception ex) {
                log.error("getDocumentroot error!", ex);
            }
            return returnval;
        }
        
        private String getStaffname(final DB2UDB db2, final String trCd1) {
            String rtn = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = " SELECT STAFFNAME FROM STAFF_MST WHERE STAFFCD = '" + trCd1 + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    if (null != rs.getString("STAFFNAME")) {
                        rtn = rs.getString("STAFFNAME");
                    }
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }
        
        /**
         * 学期マスタ (SEMESTER_MST) をロードする
         * @param db2
         */
        private Map getSemesterName(DB2UDB db2) {
            final String sql = "SELECT SEMESTER, SEMESTERNAME FROM SEMESTER_MST WHERE YEAR = '" + _year + "' ";
            //log.debug(" semester sql = " + sql);
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map semesterNameMap = new HashMap();
            try {
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    semesterNameMap.put(rs.getString("SEMESTER"), rs.getString("SEMESTERNAME"));
                }
                
            } catch (Exception ex) {
                log.error("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return semesterNameMap;
        }
        
        private void setTrCd1(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            _trCd1 = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     * ");
                stb.append(" FROM ");
                stb.append("     SCHREG_REGD_HDAT T1 ");
                stb.append(" WHERE ");
                stb.append("     T1.YEAR = '" + _year + "' ");
                stb.append("     AND T1.SEMESTER = '" + _semester + "' ");
                stb.append("     AND T1.GRADE || T1.HR_CLASS = '" + _gradeHrclass + "' ");
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    _trCd1 = rs.getString("TR_CD1");
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
        
        private List getTestcdList() {
            final String[] testcdAll = {_19900, _29900, _99901, _99900};
            final List testcdList = new ArrayList();
            for (int i = 0; i < testcdAll.length; i++) {
                if (_semester.equals(_knjSchoolMst._semesterDiv) || Integer.parseInt(testcdAll[i].substring(0, 1)) <= Integer.parseInt(_semester)) {
                    testcdList.add(testcdAll[i]);
                }
            }
            return testcdList;
        }
    }
}

// eof

