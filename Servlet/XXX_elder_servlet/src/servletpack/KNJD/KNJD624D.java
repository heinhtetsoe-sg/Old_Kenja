// kanji=漢字
/*
 * $Id: 864008d322d4ed4af145d5d1f0b7065da172cf3c $
 */
package servletpack.KNJD;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditEdit;

/**
 * 学校教育システム 賢者 [成績管理]  成績通知票
 */
public class KNJD624D {

    private static final Log log = LogFactory.getLog(KNJD624D.class);

    private boolean _hasData;
    private Param _param;

    private static final String AVGDIV_GRADE = "1";
    private static final String AVGDIV_HR = "2";
    private static final String AVGDIV_COURSE = "3";
    
    private static final String ATTR_MIGIYOSE = "HENSYU=1";
    private static final String ATTR_CENTER = "HENSYU=3";

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

            for (int i = 0; i < _param._categorySelected.length; i++) {
                printMain(db2, svf, _param._categorySelected[i]);
            }

        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            db2.commit();
            db2.close();

            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
            svf.VrQuit();
        }
    }

    private static List getPageList(final Collection list, final int count) {
        final List rtn = new ArrayList();
        List current = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Object o = it.next();
            if (null == current || current.size() >= count) {
                current = new ArrayList();
                rtn.add(current);
            }
            current.add(o);
        }
        return rtn;
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf, final String subclasscd) {

        final int COLUMN_RUIKEI = 11;

        final List rangeList = new ArrayList();
        rangeList.add(new ScoreRange(80, 100, true));
        rangeList.add(new ScoreRange(70, 79));
        rangeList.add(new ScoreRange(60, 69));
        rangeList.add(new ScoreRange(50, 59));
        rangeList.add(new ScoreRange(40, 49));
        rangeList.add(new ScoreRange(30, 39, true));
        rangeList.add(new ScoreRange(0, 29, true));

        final String subclassname = (String) _param._subclassnameMap.get(subclasscd);
        final Subclass subclass = new Subclass(subclasscd, subclassname);
        final Map avgDatMap = AvgDat.getAvgDatMap(db2, _param, subclass);
        final HrclassAttendnoComparator comparator = new HrclassAttendnoComparator();
        final List printHrListAll = HrClass.getPrintHrList(db2, _param, subclass);
        final List pageList = getPageList(printHrListAll, 10);
        
        for (int pi = 0; pi < pageList.size(); pi++) {
            final List printHrList = (List) pageList.get(pi);

            final String form = "KNJD624D.frm";
            svf.VrSetForm(form, 4);

            svf.VrsOut("TITLE", KenjaProperties.gengou(Integer.parseInt(_param._year)) + "年度 " +  _param._semesterName + " " + _param._testName + " 科目別度数分布表"); // テスト名称
            svf.VrsOut("GRADE", _param._gradeName); // 学年
            svf.VrsOut("SUBJECT", subclass._subclassname); // 科目名称
            
            final Map scoreDistributionMap = new HashMap();
            for (int hri = 0; hri < printHrList.size(); hri++) {
                final HrClass hrClass = (HrClass) printHrList.get(hri);
                for (int ri = 0; ri < rangeList.size(); ri++) {
                    final ScoreRange range = (ScoreRange) rangeList.get(ri);
                    final ScoreDistribution sd = ScoreDistribution.getScoreDistribution(subclass, hrClass._students, range);
                    Collections.sort(sd._studentList, comparator);
                    scoreDistributionMap.put(ScoreDistribution.getKey(hrClass, ri), sd);
                }
            }
            
            svf.VrsOut("DATA_TITLE", "クラス");
            svf.VrsOut("DATA_TITLE_GRP", "H1");
            for (int hri = 0; hri < COLUMN_RUIKEI; hri++) {
                final int hrcol = hri + 1;
                svf.VrsOut("DATA_GRP" + String.valueOf(hrcol) , "H1");
                if (hri < printHrList.size()) {
                    final HrClass hrClass = (HrClass) printHrList.get(hri);
                    svf.VrsOutn("DATA_1", hrcol, hrClass._hrClassName);
                    svf.VrAttributen("DATA_1", hrcol, ATTR_CENTER);
                }
            }
            svf.VrsOutn("DATA_1", COLUMN_RUIKEI, "累計");
            svf.VrsOut("DATA_GRP" + String.valueOf(COLUMN_RUIKEI) , "H1");
            svf.VrAttributen("DATA_1", COLUMN_RUIKEI, ATTR_CENTER);
            svf.VrEndRecord();
            
            for (int ri = 0; ri < rangeList.size(); ri++) {
                final ScoreRange range = (ScoreRange) rangeList.get(ri);
                final ScoreDistribution total = new ScoreDistribution();
                int maxStudent = 1;
                for (int hri = 0; hri < printHrList.size(); hri++) {
                    final HrClass hrClass = (HrClass) printHrList.get(hri);
                    final ScoreDistribution sd = (ScoreDistribution) scoreDistributionMap.get(ScoreDistribution.getKey(hrClass, ri));
                    total.add(sd);
                    if (range._isPrintStudentName) {
                        // 生徒名を印字する最大行
                        maxStudent = Math.max(maxStudent, sd._studentList.size());
                    }
                }
                for (int sti = 0; sti < maxStudent; sti++) {
                    svf.VrsOut("DATA_TITLE_GRP", String.valueOf(ri));
                    final boolean isCenter = sti == (maxStudent > 1 ? maxStudent - 1 : maxStudent) / 2;
                    if (isCenter) {
                        svf.VrsOut("DATA_TITLE", String.valueOf(range._highInclusive) + "～" + String.valueOf(range._lowInclusive));
                    }
                    for (int hri = 0; hri < COLUMN_RUIKEI; hri++) {
                        final int hrcol = hri + 1;
                        svf.VrsOut("DATA_GRP" + String.valueOf(hrcol), String.valueOf(ri));
                        if (hri < printHrList.size()) {
                            final HrClass hrClass = (HrClass) printHrList.get(hri);
                            final ScoreDistribution sd = (ScoreDistribution) scoreDistributionMap.get(ScoreDistribution.getKey(hrClass, ri));
                            if (range._isPrintStudentName) {
                                if (sti < sd._studentList.size()) {
                                    final Student student = (Student) sd._studentList.get(sti);
                                    //log.info(" " + hrClass._hrClassName + " (" + (sti + 1) + "/" + String.valueOf(sd._studentList.size()) + ") " + student._attendNo + ":" + student._schregno + " " + student._name + ", score = " + student._scoreMap.get(subclass._subclasscd));
                                    final String name = StringUtils.defaultString(student._name) + (null != student._scholarship ? "*" : "");
                                    final int nameKeta = KNJ_EditEdit.getMS932ByteLength(name);
                                    if (nameKeta <= 16) {
                                        svf.VrsOutn("DATA_1", hrcol, name);
                                    } else if (nameKeta <= 20) {
                                        svf.VrsOutn("DATA_2", hrcol, name);
                                    } else if (nameKeta <= 26) {
                                        svf.VrsOutn("DATA_3", hrcol, name);
                                    } else {
                                        svf.VrsOutn("DATA_4", hrcol, name);
                                    }
                                }
                            } else {
                                if (sd._studentList.size() > 0) {
                                    svf.VrsOutn("DATA_1", hrcol, String.valueOf(sd._studentList.size()));
                                    svf.VrAttributen("DATA_1", hrcol, ATTR_MIGIYOSE);
                                }
                            }
                        }
                    }
                    svf.VrsOut("DATA_GRP" + String.valueOf(COLUMN_RUIKEI), String.valueOf(ri));
                    if (isCenter) {
                        if (total._studentList.size() > 0) {
                            svf.VrsOutn("DATA_1", COLUMN_RUIKEI, String.valueOf(total._studentList.size()));
                            svf.VrAttributen("DATA_1", COLUMN_RUIKEI, ATTR_MIGIYOSE);
                        }
                    }
                    svf.VrEndRecord();
                }
            }
            
            final AvgDat gradeAvgDat = AvgDat.getGradeAvg(avgDatMap, subclass, _param._grade);
            // 受験者数
            svf.VrsOut("DATA_TITLE", "受験者数");
            svf.VrsOut("DATA_TITLE_GRP", "F1");
            for (int hri = 0; hri < COLUMN_RUIKEI; hri++) {
                final int hrcol = hri + 1;
                svf.VrsOut("DATA_GRP" + String.valueOf(hrcol) , "F1");
                if (hri < printHrList.size()) {
                    final HrClass hrClass = (HrClass) printHrList.get(hri);
                    final AvgDat hrAvgDat = AvgDat.getHrAvg(avgDatMap, subclass, hrClass._grade, hrClass._hrClass);
                    if (null != hrAvgDat) {
                        svf.VrsOutn("DATA_1", hrcol, String.valueOf(hrAvgDat._count));
                        svf.VrAttributen("DATA_1", hrcol, ATTR_MIGIYOSE);
                    }
                }
            }
            svf.VrsOut("DATA_GRP" + String.valueOf(COLUMN_RUIKEI) , "F1");
            if (null != gradeAvgDat) {
                svf.VrsOutn("DATA_1", COLUMN_RUIKEI, String.valueOf(gradeAvgDat._count));
                svf.VrAttributen("DATA_1", COLUMN_RUIKEI, ATTR_MIGIYOSE);
            }
            svf.VrEndRecord();
            
            // 平均
            svf.VrsOut("DATA_TITLE", "平　均");
            svf.VrsOut("DATA_TITLE_GRP", "F2");
            for (int hri = 0; hri < COLUMN_RUIKEI; hri++) {
                final int hrcol = hri + 1;
                svf.VrsOut("DATA_GRP" + String.valueOf(hrcol) , "F2");
                if (hri < printHrList.size()) {
                    final HrClass hrClass = (HrClass) printHrList.get(hri);
                    final AvgDat hrAvgDat = AvgDat.getHrAvg(avgDatMap, subclass, hrClass._grade, hrClass._hrClass);
                    if (null != hrAvgDat) {
                        svf.VrsOutn("DATA_1", hrcol, sishaGonyu(hrAvgDat._avg, 1));
                        svf.VrAttributen("DATA_1", hrcol, ATTR_MIGIYOSE);
                    }
                }
            }
            svf.VrsOut("DATA_GRP" + String.valueOf(COLUMN_RUIKEI) , "F2");
            if (null != gradeAvgDat) {
                svf.VrsOutn("DATA_1", COLUMN_RUIKEI, sishaGonyu(gradeAvgDat._avg, 1));
                svf.VrAttributen("DATA_1", COLUMN_RUIKEI, ATTR_MIGIYOSE);
            }
            svf.VrEndRecord();

            svf.VrsOut("COMMENT", "※氏名に「*」が付加されている生徒は特待");
            svf.VrEndRecord();
            
            _hasData = true;
        }
    }
    
    private static String sishaGonyu(final String s, final int scale) {
        return null == s ? null : new BigDecimal(s).setScale(scale, BigDecimal.ROUND_HALF_UP).toString();
    }

    private static class HrClass {
        final String _grade;
        final String _hrClass;
        final String _hrName;
        final String _hrClassName;
        private List _students;
//        private String _course;

        public HrClass(
                final String grade,
                final String hrClass,
                final String hrName,
                final String hrClassName
        ) {
            _grade = grade;
            _hrClass = hrClass;
            _hrName = hrName;
            _hrClassName = hrClassName;
        }

        private static List getStudents(
                final DB2UDB db2,
                final Param param,
                final String grade,
                final String pHrClass,
                final Subclass subclass
        ) {
            final List studentList = new ArrayList();
            final String sql = getStudentsSql(param, grade, pHrClass, subclass);
//            log.info(" sql = " + sql);
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map studentMap = new HashMap();
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String schregno = rs.getString("SCHREGNO");
                    if (null == studentMap.get(schregno)) {
                        final String hrClass = rs.getString("HR_CLASS");
                        final String course = rs.getString("COURSE");
                        final String attendNo = rs.getString("ATTENDNO");
                        final String name = rs.getString("NAME");
                        final String sex = rs.getString("SEX");
                        final String scholarship = rs.getString("SCHOLARSHIP");
                        final Student student = new Student(schregno, hrClass, course, attendNo, name, sex, scholarship);
                        studentList.add(student);
                        studentMap.put(schregno, student);
                    }
                    if (null != rs.getString("SUBCLASSCD")) {
                        final Student student = (Student) studentMap.get(schregno);
                        student._scoreMap.put(rs.getString("SUBCLASSCD"), rs.getString("SCORE"));
                    }
                }
            } catch (Exception e) {
                log.fatal("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return studentList;
        }

        private static String getStudentsSql(final Param param, final String grade, final String hrClass, final Subclass subclass) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.HR_CLASS, ");
            stb.append("     T1.COURSECD || T1.MAJORCD || T1.COURSECODE AS COURSE, ");
            stb.append("     T1.ATTENDNO, ");
            stb.append("     BASE.NAME, ");
            stb.append("     BASE.SEX, ");
            stb.append("     CASE WHEN NMA044.NAMECD2 IS NOT NULL THEN TSCHOL.SCHOLARSHIP END AS SCHOLARSHIP, ");
            stb.append("     L1.CLASSCD || '-' || L1.SCHOOL_KIND || '-' || L1.CURRICULUM_CD || '-' || L1.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("     L1.SCORE ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT T1 ");
            stb.append("     LEFT JOIN SCHREG_BASE_MST BASE ON T1.SCHREGNO = BASE.SCHREGNO ");
            stb.append("     LEFT JOIN RECORD_RANK_SDIV_DAT L1 ON T1.YEAR = L1.YEAR ");
            stb.append("          AND L1.SEMESTER = '" + param._semester + "' ");
            stb.append("          AND L1.TESTKINDCD || L1.TESTITEMCD || L1.SCORE_DIV = '" + param._testCd + "' ");
            stb.append("          AND T1.SCHREGNO = L1.SCHREGNO ");
            stb.append("          AND L1.CLASSCD <= '90' ");
            if (null != subclass) {
                stb.append("          AND L1.CLASSCD || '-' || L1.SCHOOL_KIND || '-' || L1.CURRICULUM_CD || '-' || L1.SUBCLASSCD = '" + subclass._subclasscd + "' ");
            }
            stb.append("          AND L1.SUBCLASSCD NOT IN ('333333', '555555', '999999') ");
            stb.append("     LEFT JOIN SEMESTER_MST SEME ON SEME.YEAR = T1.YEAR AND SEME.SEMESTER = T1.SEMESTER ");
            stb.append("     LEFT JOIN SCHREG_SCHOLARSHIP_HIST_DAT TSCHOL ON TSCHOL.SCHREGNO = T1.SCHREGNO ");
            stb.append("          AND ( ");
            stb.append("              TSCHOL.FROM_DATE BETWEEN SEME.SDATE AND SEME.EDATE ");
            stb.append("           OR TSCHOL.TO_DATE BETWEEN SEME.SDATE AND SEME.EDATE ");
            stb.append("           OR SEME.SDATE BETWEEN TSCHOL.FROM_DATE AND VALUE(TSCHOL.TO_DATE, '9999-12-31') ");
            stb.append("           OR SEME.EDATE BETWEEN TSCHOL.FROM_DATE AND VALUE(TSCHOL.TO_DATE, '9999-12-31') ");
            stb.append("              ) ");
            stb.append("          AND TSCHOL.SCHOLARSHIP IS NOT NULL ");
            stb.append("     LEFT JOIN V_NAME_MST NMA044 ON NMA044.YEAR = T1.YEAR ");
            stb.append("          AND NMA044.NAMECD1 = 'A044' ");
            stb.append("          AND NMA044.NAMECD2 = TSCHOL.SCHOLARSHIP ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            if ("9".equals(param._semester)) {
                stb.append("     AND T1.SEMESTER = '" + param._ctrlSeme + "' ");
            } else {
                stb.append("     AND T1.SEMESTER = '" + param._semester + "' ");
            }
            if ("1".equals(param._use_school_detail_gcm_dat)) {
                stb.append("     AND T1.COURSECD || '-' || T1.MAJORCD = '" + param._major + "' ");
            }
            stb.append("     AND T1.GRADE = '" + grade + "' ");
            stb.append("     AND T1.HR_CLASS = '" + hrClass + "' ");
            stb.append(" ORDER BY ");
            stb.append("     T1.ATTENDNO, CASE WHEN NMA044.NAMECD2 IS NOT NULL THEN TSCHOL.SCHOLARSHIP END ");

            return stb.toString();
        }

        private static List getPrintHrList(final DB2UDB db2, final Param param, final Subclass subclass) {
            final List rtn = new ArrayList();
            final String sql = getHrSql(param);
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final HrClass hrClass = new HrClass(rs.getString("GRADE"), rs.getString("HR_CLASS"), rs.getString("HR_NAME"), rs.getString("HR_CLASS_NAME1"));
                    rtn.add(hrClass);
                }
            } catch (Exception e) {
                log.fatal("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            for (final Iterator it = rtn.iterator(); it.hasNext();) {
                final HrClass hrClass = (HrClass) it.next();
                hrClass._students = HrClass.getStudents(db2, param, hrClass._grade, hrClass._hrClass, subclass);
                for (final Iterator stit = hrClass._students.iterator(); stit.hasNext();) {
                    final Student student = (Student) stit.next();
                    if (null != student._course) {
//                        hrClass._course = student._course;
                        break;
                    }
                }
                if (null != subclass) {
                    boolean hrClassHasData = false;
                    for (final Iterator sit = hrClass._students.iterator(); sit.hasNext();) {
                        final Student student = (Student) sit.next();
                        if (student._scoreMap.get(subclass._subclasscd) != null) {
                            hrClassHasData = true;
                            break;
                        }
                    }
                    if (!hrClassHasData) {
                        it.remove();
                        continue;
                    }
                }
            }
            return rtn;
        }

        private static String getHrSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     GRADE, ");
            stb.append("     HR_CLASS, ");
            stb.append("     HR_NAME, ");
            stb.append("     HR_CLASS_NAME1 ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_HDAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + param._year + "' ");
            if ("9".equals(param._semester)) {
                stb.append("     AND SEMESTER = '" + param._ctrlSeme + "' ");
            } else {
                stb.append("     AND SEMESTER = '" + param._semester + "' ");
            }
            stb.append("     AND GRADE = '" + param._grade + "' ");
            stb.append(" ORDER BY ");
            stb.append("     GRADE, ");
            stb.append("     HR_CLASS ");

            return stb.toString();
        }
    }

    private static class AvgDat {
        final String _score;
        final String _count;
        final String _avg;

        public AvgDat(
                final String score,
                final String cnt,
                final String avg
        ) {
            _score = score;
            _count = cnt;
            _avg = avg;
        }
        
        public static AvgDat getGradeAvg(final Map avgDatMap, final Subclass subclass, final String grade) {
            return (AvgDat) avgDatMap.get(subclass._subclasscd + ":" + AVGDIV_GRADE + ":" + grade);
        }
        
        public static AvgDat getHrAvg(final Map avgDatMap, final Subclass subclass, final String grade, final String hrClass) {
            return (AvgDat) avgDatMap.get(subclass._subclasscd + ":" + AVGDIV_HR + ":" + grade + hrClass);
        }

        private static Map getAvgDatMap(
                final DB2UDB db2,
                final Param param,
                final Subclass subclass
        ) {
            final Map retAvgMap = new HashMap();
            final String sql = getAvgSql(param, subclass);
            // log.debug(" avg sql =" + sql);
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String score = rs.getString("SCORE");
                    final String count = rs.getString("COUNT");
                    final String avg = rs.getString("AVG");
                    final AvgDat avgDat = new AvgDat(score, count, avg);
                    final String avgDiv = rs.getString("AVG_DIV");
                    String key = null;
                    if (AVGDIV_GRADE.equals(avgDiv)) {
                        key = rs.getString("GRADE");
                    } else if (AVGDIV_HR.equals(avgDiv)) {
                        key = rs.getString("GRADE") + rs.getString("HR_CLASS");
                    } else if (AVGDIV_GRADE.equals(avgDiv)) {
                        key = rs.getString("GRADE") + rs.getString("COURSE");
                    }
                    retAvgMap.put(rs.getString("SUBCLASSCD") + ":" + avgDiv + ":" + key, avgDat);
                }
            } catch (Exception e) {
                log.fatal("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retAvgMap;
        }

        private static String getAvgSql(final Param param, final Subclass subclass) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     AVG_DIV, ");
            stb.append("     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("     GRADE, ");
            stb.append("     HR_CLASS, ");
            stb.append("     COURSECD || MAJORCD || COURSECODE AS COURSE, ");
            stb.append("     SCORE, ");
            stb.append("     SCORE AS SCORE_KANSAN, ");
            stb.append("     COUNT, ");
            stb.append("     AVG, ");
            stb.append("     AVG AS AVG_KANSAN ");
            stb.append(" FROM ");
//            stb.append("     RECORD_AVERAGE_CONV_DAT ");
            stb.append("     RECORD_AVERAGE_SDIV_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + param._year + "' ");
            stb.append("     AND SEMESTER = '" + param._semester + "' ");
            stb.append("     AND TESTKINDCD || TESTITEMCD || SCORE_DIV = '" + param._testCd + "' ");
            if (null != subclass) {
                stb.append("     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '" + subclass._subclasscd + "' ");
            }
            stb.append("     AND AVG_DIV = '" + AVGDIV_GRADE + "' ");
            stb.append("     AND GRADE = '" + param._grade + "' ");
            stb.append(" UNION ");
            stb.append(" SELECT ");
            stb.append("     AVG_DIV, ");
            stb.append("     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("     GRADE, ");
            stb.append("     HR_CLASS, ");
            stb.append("     COURSECD || MAJORCD || COURSECODE AS COURSE, ");
            stb.append("     SCORE, ");
            stb.append("     SCORE AS SCORE_KANSAN, ");
            stb.append("     COUNT, ");
            stb.append("     AVG, ");
            stb.append("     AVG AS AVG_KANSAN ");
            stb.append(" FROM ");
//          stb.append("     RECORD_AVERAGE_CONV_DAT ");
            stb.append("     RECORD_AVERAGE_SDIV_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + param._year + "' ");
            stb.append("     AND SEMESTER = '" + param._semester + "' ");
            stb.append("     AND TESTKINDCD || TESTITEMCD || SCORE_DIV = '" + param._testCd + "' ");
            if (null != subclass) {
                stb.append("     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '" + subclass._subclasscd + "' ");
            }
            stb.append("     AND AVG_DIV = '" + AVGDIV_HR + "' ");
            stb.append("     AND GRADE = '" + param._grade + "' ");
            stb.append(" UNION ");
            stb.append(" SELECT ");
            stb.append("     AVG_DIV, ");
            stb.append("     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("     GRADE, ");
            stb.append("     HR_CLASS, ");
            stb.append("     COURSECD || MAJORCD || COURSECODE AS COURSE, ");
            stb.append("     SCORE, ");
            stb.append("     SCORE AS SCORE_KANSAN, ");
            stb.append("     COUNT, ");
            stb.append("     AVG, ");
            stb.append("     AVG AS AVG_KANSAN ");
            stb.append(" FROM ");
            stb.append("     RECORD_AVERAGE_SDIV_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + param._year + "' ");
            stb.append("     AND SEMESTER = '" + param._semester + "' ");
            stb.append("     AND TESTKINDCD || TESTITEMCD || SCORE_DIV = '" + param._testCd + "' ");
            if (null != subclass) {
                stb.append("     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '" + subclass._subclasscd + "' ");
            }
            stb.append("     AND AVG_DIV = '" + AVGDIV_COURSE + "' ");
            stb.append("     AND GRADE = '" + param._grade + "' ");
            return stb.toString();
        }
    }

    private static class Student {
        final String _schregno;
        final String _hrClass;
        final String _course;
        final String _attendNo;
        final String _name;
        final String _sex;
        final String _scholarship;
        final Map _scoreMap = new HashMap();

        public Student(
                final String schregno,
                final String hrClass,
                final String course,
                final String attendNo,
                final String name,
                final String sex,
                final String scholarship
        ) {
            _schregno = schregno;
            _hrClass = hrClass;
            _course = course;
            _attendNo = attendNo;
            _name = name;
            _sex = sex;
            _scholarship = scholarship;
        }
    }

    private static class ScoreRange {
        final int _lowInclusive;
        final int _highInclusive;
        final boolean _isPrintStudentName;
        public ScoreRange(final int low, final int high) {
            this(low, high, false);
        }
        public ScoreRange(final int low, final int high, final boolean isPrintStudentName) {
            _lowInclusive = low;
            _highInclusive = high;
            _isPrintStudentName = isPrintStudentName;
        }
    }

    private static class ScoreDistribution {
        final List _studentList = new ArrayList();
        public void add(ScoreDistribution dist) {
            _studentList.addAll(dist._studentList);
        }
        
        public static String getKey(final HrClass hrClass, int ri) {
            return hrClass._grade + hrClass._hrClass + "-" + String.valueOf(ri);
        }

        private static ScoreDistribution getScoreDistribution(final Subclass subclass, final List students, final ScoreRange range) {
            final ScoreDistribution sd = new ScoreDistribution();
            for (final Iterator itr = students.iterator(); itr.hasNext();) {
                final Student student = (Student) itr.next();
                if (null != student._scoreMap.get(subclass._subclasscd)) {
                    final int score = Integer.parseInt((String) student._scoreMap.get(subclass._subclasscd));
                    if (range._lowInclusive <= score && score <= range._highInclusive) {
                        sd._studentList.add(student);
                    }
                }
            }
            return sd;
        }

        public int totalCount() {
            return _studentList.size();
        }
    }

    private static class Subclass {
        final String _subclasscd;
        final String _subclassname;

        public Subclass(
                final String subclasscd,
                final String subclassname
        ) {
            _subclasscd = subclasscd;
            _subclassname = subclassname;
        }
    }

    private static class HrclassAttendnoComparator implements Comparator {
        public int compare(final Object o1, final Object o2) {
            final Student s1 = (Student) o1;
            final Student s2 = (Student) o2;
            return (s1._hrClass + s1._attendNo).compareTo(s2._hrClass + s2._attendNo);
        }
    }

    private static class ScoreComparator implements Comparator {
        final Subclass _subclass;
        public ScoreComparator(final Subclass subclass) {
            _subclass = subclass;
        }
        public int compare(final Object o1, final Object o2) {
            final Student s1 = (Student) o1;
            final Student s2 = (Student) o2;
            final String score1 = (String) s1._scoreMap.get(_subclass._subclasscd);
            final String score2 = (String) s2._scoreMap.get(_subclass._subclasscd);
            if (null != score1 || null != score2) {
                if (null == score1) {
                    return 1;
                } else if (null == score2) {
                    return -1;
                }
                final Integer score1i = Integer.valueOf(score1);
                final Integer score2i = Integer.valueOf(score2);
                final int cmp = - score1i.compareTo(score2i); // 降順
                if (0 != cmp) {
                    return cmp;
                }
            }
            return (s1._hrClass + s1._attendNo).compareTo(s2._hrClass + s2._attendNo);
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 57328 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _year;
        final String _semester;
        final String _ctrlSeme;
        final String _semesterName;
        final String _ctrlDate;
        final String _testCd;
        final String _testName;
        final String _grade;
        final String _gradeName;
        final String _useCurriculumcd;
        final String _printOrderScore;
        final String[] _categorySelected;
        final Map _subclassnameMap;
        final boolean _isPrintCourseAvg;
        final String _major;
        final String _use_school_detail_gcm_dat;
        final String _useSchool_KindField;
        final String SCHOOLKIND;
        final String SCHOOLCD;

        Param(final DB2UDB db2, final HttpServletRequest request) {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _ctrlSeme = request.getParameter("CTRL_SEME");
            _ctrlDate = request.getParameter("LOGIN_DATE");
            _testCd = request.getParameter("TESTCD");
            _grade = request.getParameter("GRADE");
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _printOrderScore = request.getParameter("PRINT_ORDER_SCORE");
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _major = request.getParameter("MAJOR");
            _use_school_detail_gcm_dat = request.getParameter("use_school_detail_gcm_dat");
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            SCHOOLKIND = request.getParameter("SCHOOLKIND");
            SCHOOLCD = request.getParameter("SCHOOLCD");
            _semesterName = getSemesterName(db2, _year, _semester);
            _testName = getTestName(db2, _year, _semester, _testCd);
            _gradeName = getGradeName(db2, _year, _grade);
            _subclassnameMap = getSubclassname(db2);

            final String z010 = setZ010Name1(db2);
            log.info(" z010 name1 = " + z010);
            _isPrintCourseAvg = "sundaikoufu".equals(z010);
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

        private String getSemesterName(final DB2UDB db2, final String year, final String semester) {
            String rtn = null;
            final String sql = "SELECT SEMESTERNAME FROM SEMESTER_MST WHERE YEAR = '" + year + "' AND SEMESTER = '" + semester + "'";
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
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }

        private String getTestName(final DB2UDB db2, final String year, final String semester, final String testCd) {
            String rtn = null;
            String sql = "";
            if ("1".equals(_use_school_detail_gcm_dat)) {
                sql = "SELECT TESTITEMNAME FROM TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV WHERE YEAR = '" + _year + "'" +
                        " AND SEMESTER = '" + _semester + "' " +
                        " AND GRADE = '00' " +
                        " AND COURSECD || '-' || MAJORCD = '" + _major + "' " +
                        " AND TESTKINDCD || TESTITEMCD || SCORE_DIV = '" + testCd + "' ";
                if ("1".equals(_useSchool_KindField) && !StringUtils.isBlank(SCHOOLKIND)) {
                    sql += " AND SCHOOLCD = '" + SCHOOLCD + "' " +
                           " AND SCHOOL_KIND = '" + SCHOOLKIND + "' ";
                }
            } else {
                sql += "SELECT TESTITEMNAME FROM TESTITEM_MST_COUNTFLG_NEW_SDIV ";
                sql += "WHERE YEAR = '" + year + "' AND SEMESTER = '" + semester + "' AND TESTKINDCD || TESTITEMCD || SCORE_DIV = '" + testCd + "'";
            }
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
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }

        private String getGradeName(final DB2UDB db2, final String year, final String grade) {
            String rtn = null;
            final String sql = "SELECT GRADE_NAME1 FROM SCHREG_REGD_GDAT WHERE YEAR = '" + year + "' AND GRADE = '" + grade + "'";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString("GRADE_NAME1");
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }

        private Map getSubclassname(final DB2UDB db2) {
            Map rtn = new HashMap();
            final String sql = "SELECT CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS SUBCLASSCD, SUBCLASSNAME FROM SUBCLASS_MST ";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn.put(rs.getString("SUBCLASSCD"), rs.getString("SUBCLASSNAME"));
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }

    }
}

// eof
