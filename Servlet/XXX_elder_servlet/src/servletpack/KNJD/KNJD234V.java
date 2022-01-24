/*
 * $Id: 713abb7d03af16e564d9f2672290b97ad186b891 $
 *
 * 作成日: 2013/07/25
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import static servletpack.KNJZ.detail.KNJ_EditEdit.getMS932ByteLength;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.CsvUtils;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

/**
 * 学校教育システム 賢者 [成績管理]  成績判定会議資料
 */

public class KNJD234V {

    private static final Log log = LogFactory.getLog(KNJD234V.class);

    private static final String SEMEALL = "9";
    private static final String SUBCLASSCD999999 = "999999";
    private static final String HYOTEI_TESTCD = "990009";
    private static final String GAKKIHYOKA_TESTCD = "990008";
    private static final String AVG_DIV_GRADE = "1";
    private static final String AVG_DIV_HR_CLASS = "2";
    private static final String AVG_DIV_COURSE = "3";
    private static final String AVG_DIV_MAJOR = "4";
//    private static final String SIDOU_INF_MARK = "1";
//    private static final String SIDOU_INF_SCORE = "2";
    private static final String SEX1 = "1";
    private static final String SEX2 = "2";
    private static final String AMIKAKE_ATTR = "Paint=(1,80,1),Bold=1";

    private static final String csv = "csv";
    private static final String D056_01 = "01";
    private static final String D056_02 = "02";
    private static final String D056_03 = "03";
    private static final String D056_SUBCLASSCD_ZERO = "00-00-00-000000";

    private static final String OUTPUTRANK_HR = "1";
    private static final String OUTPUTRANK_GRADE = "2";
    private static final String OUTPUTRANK_COURSE = "3";
    private static final String OUTPUTRANK_MAJOR = "4";

    private static final String MAIN_CSV = "csv_main";

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
        Vrw32alp svf = null;
        DB2UDB db2 = null;
        try {
            response.setContentType("application/pdf");

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _hasData = false;

            _param = createParam(db2, request);

            final List<Student> studentList = getStudentList(db2, _param);
            final List<HrClass> hrClassList = HrClass.getHrClassList(studentList);
            setData(db2, _param, studentList, hrClassList);
            _param._averageDatMap = AverageDat.getAverageDatMap(db2, _param);

            if (csv.equals(_param._cmd)) {
                outputCsv(db2, request, response, studentList);
            } else if (MAIN_CSV.equals(_param._cmd)) {
                printMainCsv(db2, request, response, studentList, hrClassList);
            } else {
                svf = new Vrw32alp();
                svf.VrInit();
                svf.VrSetSpoolFileStream(response.getOutputStream());

                printMain(db2, svf, studentList, hrClassList);
            }

        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            if (csv.equals(_param._cmd)) {
            } else if (MAIN_CSV.equals(_param._cmd)) {
            } else {
                if (!_hasData) {
                    svf.VrSetForm("MES001.frm", 0);
                    svf.VrsOut("note", "note");
                    svf.VrEndPage();
                }
                svf.VrQuit();
            }

            if (null != db2) {
                db2.commit();
                db2.close();
            }
        }
    }

    private void outputCsv(final DB2UDB db2, final HttpServletRequest request, final HttpServletResponse response, final List<Student> studentList) {
        final String filename = getTitle(db2) + StringUtils.defaultString(_param._d056Name1) + ".csv";
        final Map parameterMap = new HashMap();
        parameterMap.put("HttpServletRequest", request);
        CsvUtils.outputLines(log, response, filename, getCsvOutputLine(_param, studentList));
    }


    private static List getCsvOutputLine(final Param param, final List<Student> studentList) {
        final List lines = new ArrayList();

        List<Student> outputStudentList = Collections.EMPTY_LIST;
        if (D056_01.equals(param._d056)) {
            lines.add(Arrays.asList("※特殊事情\"主な理由\"区分", "※年度", "※学期", "※考査種別（大分類）", "※考査種別（中分類）", "※考査種別（小分類）", "※教科コード", "※校種コード", "※教育課程コード", "※科目コード", "年組", "出席番号", "※学籍番号", "生徒名", StringUtils.defaultString(param._d056Name2), "備考", "DUMMY")); // 担任
            // 3.成績のつかなかった者
            outputStudentList = getKessiStudentList(studentList);
            for (final Student student : outputStudentList) {
                for (final SubclassScore subScore : student.getKesshiSubclassList()) {
                    final List line = new ArrayList();
                    line.add(StringUtils.defaultString(param._d056)); // ※特殊事情\"主な理由\"区分
                    line.add(param._year); // ※年度
                    line.add(param._semester); // ※学期
                    line.add(param._testItem._testkindcd); //※考査種別（大分類）
                    line.add(param._testItem._testitemcd); // ※考査種別（中分類）
                    line.add(param._testItem._scoreDiv); //※考査種別（小分類）
                    final String[] subclasscdSplit = StringUtils.split(subScore._subclass._subclasscd, "-");
                    line.add(StringUtils.defaultString(subclasscdSplit[0])); // 教科コード
                    line.add(StringUtils.defaultString(subclasscdSplit[1])); // 校種コード
                    line.add(StringUtils.defaultString(subclasscdSplit[2])); // 教育課程コード
                    line.add(StringUtils.defaultString(subclasscdSplit[3])); // 科目コード
                    line.add(StringUtils.defaultString(student._hrName)); // 年組
                    line.add(StringUtils.defaultString((NumberUtils.isDigits(student._attendno)) ? Student.attendnodf.format(Integer.parseInt(student._attendno)) : student._attendno)); // 出席番号
                    line.add(student._schregno); // ※学籍番号
                    line.add(StringUtils.defaultString(student._name)); // 生徒名
                    line.add(StringUtils.defaultString(subScore._subclass._subclassabbv)); // 科目名
                    line.add(StringUtils.defaultString(student.getSpecialReasonTestDatRemark(param._d056 + subScore._subclass._subclasscd))); // 備考
                    line.add("DUMMY");
                    lines.add(line);
                }
            }
        } else if (D056_02.equals(param._d056) || D056_03.equals(param._d056)) {
            lines.add(Arrays.asList("※特殊事情\"主な理由\"区分", "※年度", "※学期", "※考査種別（大分類）", "※考査種別（中分類）", "※考査種別（小分類）", "年組", "出席番号", "※学籍番号", "生徒名", StringUtils.defaultString(param._d056Name2), "備考", "DUMMY")); // 担任
            if (D056_02.equals(param._d056)) {
                // 4. 欠席・遅刻・早退の多い者
                outputStudentList = getAttendOverStudentList(param, studentList);
            } else {
                // 9.出席の正常でない者
                outputStudentList = getAttendSubclassOverStudentList(param, studentList);
            }
            for (final Student student : outputStudentList) {
                final List<String> line = new ArrayList<String>();
                line.add(StringUtils.defaultString(param._d056)); // ※特殊事情\"主な理由\"区分
                line.add(param._year); // ※年度
                line.add(param._semester); // ※学期
                line.add(param._testItem._testkindcd); //※考査種別（大分類）
                line.add(param._testItem._testitemcd); // ※考査種別（中分類）
                line.add(param._testItem._scoreDiv); //※考査種別（小分類）
                line.add(StringUtils.defaultString(student._hrName)); // 年組
                line.add(StringUtils.defaultString((NumberUtils.isDigits(student._attendno)) ? Student.attendnodf.format(Integer.parseInt(student._attendno)) : student._attendno)); // 出席番号
                line.add(student._schregno); // ※学籍番号
                line.add(StringUtils.defaultString(student._name)); // 生徒名
                line.add(StringUtils.defaultString(getStudentCsvText(param, student))); // 区分名
                line.add(StringUtils.defaultString(student.getSpecialReasonTestDatRemark(param._d056))); // 備考
                line.add("DUMMY");
                lines.add(line);
            }
        }
        return lines;
    }

    private static String getStudentCsvText(final Param param, final Student student) {
        final StringBuffer stb = new StringBuffer();
        if (D056_02.equals(param._d056)) {
            // 4. 欠席・遅刻・早退の多い者
            stb.append(String.valueOf(student._attendance._absence) + "／" + String.valueOf(student._attendance._late) + "／" + String.valueOf(student._attendance._early));
        } else if (D056_03.equals(param._d056)) {
            // 9.出席の正常でない者
            stb.append(getAttendSubclassOverText(param, student));
        }
        return stb.toString();
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf, final List<Student> studentList, final List hrClassList) {

        print1234(db2, svf, hrClassList, studentList);
        if ("1".equals(_param._outputDosubupu)) {
            print5(svf, hrClassList, studentList, _param._averageDatMap);
        }
        int bango = 5;
        if (!"0".equals(_param._testItem._sidouInput)) {
            bango++; // 6
            if ("1".equals(_param._outputShidou)) {
                printTuishido(svf, bango, studentList);
            }
        }

        bango++; // 6 or 7
        if ("1".equals(_param._outputYuryo)) {
            if ("1".equals(_param._jouiCourse)) {
                for (final Map.Entry<String, List<Student>> e : Student.groupByCourse(studentList).entrySet()) {
                    final String course = e.getKey();
                    final List<Student> studentListCourse = e.getValue();
                    printJoui(svf, bango, studentListCourse, course);
                }
            } else {
                printJoui(svf, bango, studentList, null);
            }
        }

        bango++; // 7 or 8
        if ("1".equals(_param._outputFushin)) {
            if ("1".equals(_param._kaiCourse)) {
                for (final Map.Entry<String, List<Student>> e : Student.groupByCourse(studentList).entrySet()) {
                    final String course = e.getKey();
                    final List<Student> studentListCourse = e.getValue();
                    final Student student0 = studentListCourse.get(0);
                    printKai(svf, bango, studentListCourse, course, StringUtils.defaultString(student0._majorname) + StringUtils.defaultString(student0._coursecodename));
                }
            } else {
                printKai(svf, bango, studentList, null, null);
            }
        }
        bango++; // 8 or 9
        if ("1".equals(_param._outputKyokakamoku)) {
            printShukketsuIjou(svf, bango, HrClass.getZaisekiList(studentList, null, _param, _param._edate));
        }

        if ("1".equals(_param._knjd234vPrintTsusanKettenKamokusu)) {
            bango++; // 9 or 10
            printSeitobetsuTsusanKettenKamokusu(svf, bango, studentList);
        }
        _hasData = true;
    }

    private void printMainCsv(final DB2UDB db2, final HttpServletRequest request, final HttpServletResponse response, final List studentList, final List hrClassList) {
        final List lines = new ArrayList();

        newLine(lines).addAll(Arrays.asList(getTitle(db2))); // タイトル
        newLine(lines).addAll(Arrays.asList("")); //改行

        lines.addAll(print1234_Csv(db2, hrClassList, studentList));
        newLine(lines).addAll(Arrays.asList("")); //改行

        if ("1".equals(_param._outputDosubupu)) {
            lines.addAll(print5_Csv(hrClassList, studentList, _param._averageDatMap));
            newLine(lines).addAll(Arrays.asList("")); //改行
        }
        int bango = 5;
        if (!"0".equals(_param._testItem._sidouInput)) {
            bango++; // 6
            if ("1".equals(_param._outputShidou)) {
                lines.addAll(printTuishido_Csv(bango, studentList));
                newLine(lines).addAll(Arrays.asList("")); //改行
            }
        }

        bango++; // 6 or 7
        if ("1".equals(_param._outputYuryo)) {
            if ("1".equals(_param._jouiCourse)) {
                for (final Iterator it = Student.groupByCourse(studentList).entrySet().iterator(); it.hasNext();) {
                    final Map.Entry e = (Map.Entry) it.next();
                    final String course = (String) e.getKey();
                    final List studentListCourse = (List) e.getValue();
                    lines.addAll(printJoui_Csv(bango, studentListCourse, course));
                }
            } else {
                lines.addAll(printJoui_Csv(bango, studentList, null));
            }
            newLine(lines).addAll(Arrays.asList("")); //改行
        }

        bango++; // 7 or 8
        if ("1".equals(_param._outputFushin)) {
            if ("1".equals(_param._kaiCourse)) {
                for (final Iterator it = Student.groupByCourse(studentList).entrySet().iterator(); it.hasNext();) {
                    final Map.Entry e = (Map.Entry) it.next();
                    final String course = (String) e.getKey();
                    final List<Student> studentListCourse = (List) e.getValue();
                    final Student student0 = studentListCourse.get(0);
                    lines.addAll(printKai_Csv(bango, studentListCourse, course, StringUtils.defaultString(student0._majorname) + StringUtils.defaultString(student0._coursecodename)));
                }
            } else {
                lines.addAll(printKai_Csv(bango, studentList, null, null));
            }
            newLine(lines).addAll(Arrays.asList("")); //改行
        }

        bango++; // 8 or 9
        if ("1".equals(_param._outputKyokakamoku)) {
            lines.addAll(printShukketsuIjou_Csv(bango, HrClass.getZaisekiList(studentList, null, _param, _param._edate)));
            newLine(lines).addAll(Arrays.asList("")); //改行
        }

        if ("1".equals(_param._knjd234vPrintTsusanKettenKamokusu)) {
            bango++; // 9 or 10
            lines.addAll(printSeitobetsuTsusanKettenKamokusu_Csv(bango, studentList));
            newLine(lines).addAll(Arrays.asList("")); //改行
        }
        _hasData = true;
        final Map parameterMap = new HashMap();
        parameterMap.put("HttpServletRequest", request);
        CsvUtils.outputLines(log, response, getTitle(db2) + ".csv", lines, parameterMap);
    }

    private List<String> newLine(final List<List<String>> lines) {
        final List<String> line = new ArrayList<String>();
        lines.add(line);
        return line;
    }

    private static String mkString(final List remarkList, final String comma) {
        final StringBuffer stb = new StringBuffer();
        String cm = "";
        for (final Iterator it = remarkList.iterator(); it.hasNext();) {
            stb.append(cm).append(it.next());
            cm = comma;
        }
        return stb.toString();
    }

    private static <A, B> List<B> getMappedList(final Map<A, List<B>> map, final A key) {
        if (null == map.get(key)) {
            map.put(key, new ArrayList<B>());
        }
        return map.get(key);
    }

    private static <A, B> Set<B> getMappedSet(final Map<A, Set<B>> map, final A key) {
        if (null == map.get(key)) {
            map.put(key, new TreeSet<B>());
        }
        return map.get(key);
    }

    private static boolean isSubclass999999(final String subclasscd, final Param param) {
        if ("1".equals(param._useCurriculumcd)) {
            final String split = StringUtils.split(subclasscd, "-")[3];
            if (SUBCLASSCD999999.equals(split)) {
                return true;
            }
        }
        if (SUBCLASSCD999999.equals(subclasscd)) {
            return true;
        }
        return false;
    }

    private static void setData(final DB2UDB db2, final Param param, final List<Student> studentList, final List<HrClass> hrClassList) {
        log.debug(" setData ");

        final Map<String, Student> studentMap = new HashMap<String, Student>();
        for (final Student student : studentList) {
            studentMap.put(student._schregno, student);
        }

        if ("1".equals(param._outputShukketsu) || "1".equals(param._outputKyokakamoku)) {
            // １日出欠
            PreparedStatement ps = null;
            try {
                param._attendParamMap.put("hrClass", "?");
                String sql = AttendAccumulate.getAttendSemesSql(
                        param._year,
                        param._semester,
                        param._sdate,
                        param._edate,
                        param._attendParamMap
                        );
                ps = db2.prepareStatement(sql);
                final Integer zero = new Integer(0);
                for (final HrClass hrClass : hrClassList) {
                    if (param._isOutputDebug) {
                        log.info(" set Attendance " + hrClass);
                    }

                    for (final Map row : KnjDbUtils.query(db2, ps, new Object[] { hrClass._hrClass})) {
                        final Student student = studentMap.get(KnjDbUtils.getString(row, "SCHREGNO"));
                        if (student == null || !"9".equals(KnjDbUtils.getString(row, "SEMESTER"))) {
                            continue;
                        }
                        final int lesson = KnjDbUtils.getInt(row, "LESSON", zero).intValue();
                        final int mourning = KnjDbUtils.getInt(row, "MOURNING", zero).intValue();
                        final int suspend = KnjDbUtils.getInt(row, "SUSPEND", zero).intValue() + KnjDbUtils.getInt(row, "VIRUS", zero).intValue() + KnjDbUtils.getInt(row, "KOUDOME", zero).intValue();
                        final int abroad = KnjDbUtils.getInt(row, "TRANSFER_DATE", zero).intValue();
                        final int mlesson = KnjDbUtils.getInt(row, "MLESSON", zero).intValue();
                        final int absence = KnjDbUtils.getInt(row, "SICK", zero).intValue();
                        final int attend = KnjDbUtils.getInt(row, "PRESENT", zero).intValue();
                        final int late = KnjDbUtils.getInt(row, "LATE", zero).intValue();
                        final int early = KnjDbUtils.getInt(row, "EARLY", zero).intValue();

                        final Attendance attendance = new Attendance(lesson, mourning, suspend, abroad, mlesson, absence, attend, late, early);
                        // log.debug("   schregno = " + student._schregno + " , attendance = " + attendance);
                        student._attendance = attendance;
                    }
                }

            } catch (SQLException e) {
                log.error("sql exception!", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }

            // 科目出欠
            try {
                final String sql = AttendAccumulate.getAttendSubclassSql(
                        param._year,
                        param._semester,
                        param._sdate,
                        param._edate,
                        param._attendParamMap);

                ps = db2.prepareStatement(sql);

                for (final HrClass hrClass : hrClassList) {
                    if (param._isOutputDebug) {
                        log.info(" set SubclassAttendance " + hrClass);
                    }

                    for (final Map row : KnjDbUtils.query(db2, ps, new Object[] { hrClass._hrClass})) {
                        final Student student = studentMap.get(KnjDbUtils.getString(row, "SCHREGNO"));
                        if (student == null || !"9".equals(KnjDbUtils.getString(row, "SEMESTER"))) {
                            continue;
                        }
                        final String subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");
                        if (null == param._subclassMap.get(subclasscd)) {
                            continue;
                        }
                        final Subclass subclass = param._subclassMap.get(subclasscd);
                        final BigDecimal lesson = KnjDbUtils.getBigDecimal(row, "MLESSON", null);
                        final BigDecimal sick = subclass._isSaki ? KnjDbUtils.getBigDecimal(row, "REPLACED_SICK", null) : KnjDbUtils.getBigDecimal(row, "SICK2", null);
                        final SubclassAttendance sa = new SubclassAttendance(student, subclass, lesson, sick);

                        student._subclassAttendance.put(subclasscd, sa);
//                    log.debug("   schregno = " + student._schregno + " , subclcasscd = " + subclasscd + " , subclass attendance = " + sa);
                    }
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }


        // 単位
        try {
            final String sql = SubclassScore.getSubclassCredit(param);
            if (param._isOutputDebugQuery) {
                log.info(" setCredit  sql = " + sql);
            }
            for (final Map row : KnjDbUtils.query(db2, sql)) {
                final String schregno = KnjDbUtils.getString(row, "SCHREGNO");
                final Student student = studentMap.get(schregno);
                if (null == student) {
                    continue;
                }
                final String subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");

                if (!param._subclassMap.containsKey(subclasscd)) {
                    continue;
                }
                final Subclass subclass = param._subclassMap.get(subclasscd);

                final Integer credits = KnjDbUtils.getInt(row, "CREDITS", null);
                subclass._courseCreditsMap.put(student.course(), credits);
                getMappedSet(subclass._creditsCourseMap, credits).add(student.course());
            }
        } catch (Exception ex) {
            log.fatal("exception!", ex);
        }

        // 成績
        try {
            final String sql = SubclassScore.getSubclassScoreSql(param);
            if (param._isOutputDebugQuery) {
                log.info(" setRecord  sql = " + sql);
            }
            for (final Map row : KnjDbUtils.query(db2, sql)) {
                final String schregno = KnjDbUtils.getString(row, "SCHREGNO");
                final Student student = studentMap.get(schregno);
                if (null == student) {
                    continue;
                }

                final String subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");
                final String classabbv = KnjDbUtils.getString(row, "CLASSABBV");
                final String subclassname = KnjDbUtils.getString(row, "SUBCLASSNAME");
                final String subclassabbv = KnjDbUtils.getString(row, "SUBCLASSABBV");
                final String score = KnjDbUtils.getString(row, "SCORE");
                final BigDecimal avg = KnjDbUtils.getBigDecimal(row, "AVG", null);
                final SubclassScore.DivRank gradeRank = new SubclassScore.DivRank(KnjDbUtils.getString(row, "GRADE_RANK"), KnjDbUtils.getString(row, "GRADE_AVG_RANK"));
                final SubclassScore.DivRank classRank = new SubclassScore.DivRank(KnjDbUtils.getString(row, "CLASS_RANK"), KnjDbUtils.getString(row, "CLASS_AVG_RANK"));
                final SubclassScore.DivRank courseRank = new SubclassScore.DivRank(KnjDbUtils.getString(row, "COURSE_RANK"), KnjDbUtils.getString(row, "COURSE_AVG_RANK"));
                final SubclassScore.DivRank majorRank = new SubclassScore.DivRank(KnjDbUtils.getString(row, "MAJOR_RANK"), KnjDbUtils.getString(row, "MAJOR_AVG_RANK"));
                final String slumpScore = null; //KnjDbUtils.getString(row, "SLUMP_SCORE");
                final String slumpMarkCd = null; //KnjDbUtils.getString(row, "SLUMP_MARK_CD");
                final String slumpMark = null; //KnjDbUtils.getString(row, "SLUMP_MARK");

                if (!param._subclassMap.containsKey(subclasscd)) {
                    param._subclassMap.put(subclasscd, new Subclass(subclasscd, classabbv, subclassname, subclassabbv, false, false));
                }
                final Subclass subclass = param._subclassMap.get(subclasscd);
                subclass._subclassScoreAllNull = KnjDbUtils.getString(row, "SCORE_ALL_NULL");

                final SubclassScore subclassscore = new SubclassScore(student, param._testItem, subclass, score, avg, gradeRank,
                        classRank, courseRank, majorRank, slumpScore, slumpMarkCd, slumpMark);

                if (isSubclass999999(subclasscd, param)) {
                    if ("1".equals(KnjDbUtils.getString(row, "TESTFLG"))) {
                        student._subclassScore999999BeforeTest = subclassscore;
                    } else {
                        student._subclassScore999999 = subclassscore;
                    }
                } else {
                    student._subclassScore.put(subclasscd, subclassscore);
                }
            }
        } catch (Exception ex) {
            log.fatal("exception!", ex);
        }

        if ("1".equals(param._outputDosubupu)) {
            // 成績 平均の平均
            for (final HrClass hrClass : hrClassList) {

                //hrClass._avgAvg = calcAverage(" average " + hrClass._hrClass, toBd(getAvgList(hrClass._studentList, SUBCLASSCD999999)));
                hrClass._avgAvg = calcAverage(toBd(getAvgList(hrClass._studentList)));

//                try {
//                    final StringBuffer sql = new StringBuffer();
//                    sql.append(" WITH TGT AS ( ");
//                    sql.append(" SELECT  ");
//                    sql.append("   T2.GRADE || T2.HR_CLASS AS GRADE_HR_CLASS, T2.SCHREGNO, T1.AVG ");
//                    sql.append(" FROM RECORD_RANK_SDIV_DAT T1 ");
//                    sql.append(" INNER JOIN SCHREG_REGD_DAT T2 ON T2.SCHREGNO = T1.SCHREGNO ");
//                    sql.append("   AND T2.YEAR = T1.YEAR ");
//                    sql.append("   AND T2.SEMESTER = '" + (SEMEALL.equals(param._semester) ? param._ctrlSemester : param._semester) + "' ");
//                    sql.append("   AND T2.GRADE = '" + param._grade + "' ");
//                    sql.append("   AND T2.HR_CLASS = '" + hrClass._hrClass + "' ");
//                    sql.append(" WHERE ");
//                    sql.append("   T1.YEAR = '" + param._year + "' ");
//                    sql.append("   AND T1.SEMESTER = '" + param._semester + "' ");
//                    sql.append("   AND T1.TESTKINDCD = '" + param._testcd.substring(0, 2) + "' AND T1.TESTITEMCD = '" + param._testcd.substring(2, 4) + "' AND T1.SCORE_DIV = '" + param._testcd.substring(4) + "' ");
//                    sql.append("   AND T1.SUBCLASSCD = '999999' ");
//                    sql.append("   AND T1.AVG IS NOT NULL ");
//                    sql.append(" ) ");
//                    sql.append(" SELECT T1.GRADE_HR_CLASS, SUM(T1.AVG) / COUNT(*) AS AVGAVG ");
//                    sql.append(" FROM TGT T1 ");
//                    sql.append(" GROUP BY T1.GRADE_HR_CLASS ");
//
//                    if (param._isOutputDebug) {
//                    	log.info(" setavg avg  sql = " + sql);
//                    }
//                    for (final Iterator it = KnjDbUtils.query(db2, sql.toString()).iterator(); it.hasNext();) {
//                        final Map row = (Map) it.next();
//                        hrClass._avgAvg = sishaGonyu(KnjDbUtils.getBigDecimal(row, "AVGAVG", null));
//                    }
//                } catch (Exception ex) {
//                    log.fatal("exception!", ex);
//                }
            }
        }

        // 主な理由備考
        try {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT T1.SCHREGNO, T1.REASON_DIV, T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, T1.REMARK ");
            stb.append(" FROM SPECIAL_REASON_TEST_DAT T1 ");
            stb.append(" WHERE  ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.SEMESTER = '" + param._semester + "' ");
            stb.append("     AND T1.TESTKINDCD = '" + param._testcd.substring(0, 2) + "' AND T1.TESTITEMCD = '" + param._testcd.substring(2, 4) + "' AND T1.SCORE_DIV = '" + param._testcd.substring(4) + "' ");
            // log.debug(" setSpecialReason  sql = " + stb);
            for (final Map row : KnjDbUtils.query(db2, stb.toString())) {
                final String schregno = KnjDbUtils.getString(row, "SCHREGNO");
                final Student student = studentMap.get(schregno);
                if (null == student) {
                    continue;
                }
                student._specialReasonTestDatRemarkMap.put(KnjDbUtils.getString(row, "REASON_DIV") + KnjDbUtils.getString(row, "SUBCLASSCD"), KnjDbUtils.getString(row, "REMARK"));
            }
        } catch (Exception ex) {
            log.fatal("exception!", ex);
        }

        final TreeSet<String> entDateSet = new TreeSet<String>();
        for (final Student student : studentList) {
            if (null != student._entdate && (null == student._entdiv || ArrayUtils.contains(new String[] {"1", "2", "3"}, student._entdiv))) {
                entDateSet.add(student._entdate);
            }
        }
        if (!entDateSet.isEmpty()) {
            final String entDateMin = entDateSet.first();
            if (param._yearSdate.compareTo(entDateMin) < 1) {
                param._yearSdate = entDateMin;
            }
        }
        log.info(" entDateSet = " + entDateSet + ", yearSdate = " + param._yearSdate);

        if ("1".equals(param._knjd234vPrintTsusanKettenKamokusu)) {
            setTsusanKamokusu(db2, param, studentList);
        }
    }

    private static void setTsusanKamokusu(final DB2UDB db2, final Param param, final List<Student> studentList) {
        final Map<String, TestItem> testcdTestitemMap = new HashMap<String, TestItem>();
        for (final TestItem t : param._tusanTestItemList) {
            testcdTestitemMap.put(t._year + t.getSemeTestcd(), t);
        }

        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     YEAR, SEMESTER, TESTKINDCD, TESTITEMCD, SCORE_DIV ");
        stb.append("   , CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS SUBCLASSCD ");
        stb.append("   , SCHREGNO ");
        stb.append("   , SCORE ");
        stb.append(" FROM RECORD_RANK_SDIV_DAT T1 ");
        stb.append(" WHERE ");
        stb.append("   (T1.YEAR < '" + param._year + "' ");
        stb.append("    OR T1.YEAR = '" + param._year + "' AND SEMESTER || '-' || TESTKINDCD || '-' || TESTITEMCD || '-' || SCORE_DIV <= '" + param._testItem.getSemeTestcd() + "' ");
        stb.append("   ) AND (T1.SCORE_DIV = '01' OR T1.TESTKINDCD <> '99' AND T1.SCORE_DIV = '08') ");
        stb.append("   AND T1.SCHREGNO = ? ");
        stb.append("   AND T1.SCORE <= " + String.valueOf(param._shidouTensuInf2) + " ");

        PreparedStatement ps = null;

        try {
            ps = db2.prepareStatement(stb.toString());

            final TreeSet<String> testcdSet = new TreeSet<String>();

            for (final Student student : studentList) {

                for (final Map row : KnjDbUtils.query(db2, ps, new Object[] { student._schregno})) {

                    final String testcd = KnjDbUtils.getString(row, "YEAR") + KnjDbUtils.getString(row, "SEMESTER") + "-" + KnjDbUtils.getString(row, "TESTKINDCD") + "-" + KnjDbUtils.getString(row, "TESTITEMCD") + "-" + KnjDbUtils.getString(row, "SCORE_DIV");
                    final TestItem testItem = testcdTestitemMap.get(testcd);
                    if (null == testItem) {
                        log.info(" null tsusan testcd : " + testcd);
                        continue;
                    }
                    testcdSet.add(testcd);
                    final String subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");
                    final Subclass subclass = param._subclassMap.get(subclasscd);
                    if (null == subclass) {
                        log.info(" null tsusan subclass : " + subclasscd);
                        continue;
                    }
                    final SubclassScore subScore = new SubclassScore(student, testItem, subclass, KnjDbUtils.getString(row, "SCORE"), null, null, null, null, null, null, null, null);
                    getMappedList(student._tsusanKettenKamokuListMap, testcd).add(subScore);
                }
//                if (rowList.size() > 0) {
//                    log.info(" row size = " + rowList.size() + " " + student._schregno + " => " + student._tsusanKettenKamokuListMap.size());
//                }
            }
            log.info(" testcdSet = " + testcdSet);

        } catch (Exception e) {
            log.error("excepton!", e);
        } finally {
            DbUtils.closeQuietly(ps);
        }
    }

    private static List<Student> getStudentList(final DB2UDB db2, final Param param) {
        final List<Student> studentList = new ArrayList<Student>();
        final Map<String, Student> studentMap = new HashMap<String, Student>();
        try {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH SCHREG_TRANSFER1 AS ( ");
            stb.append("   SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     MAX(T1.TRANSFER_SDATE) AS TRANSFER_SDATE ");
            stb.append("   FROM SCHREG_TRANSFER_DAT T1 ");
            stb.append("   LEFT JOIN V_SEMESTER_GRADE_MST T2 ON T2.YEAR = '" + param._year + "' AND T2.SEMESTER = '9' AND GRADE = '" + param._grade + "' ");
            stb.append("   WHERE ");
            stb.append("     T2.SDATE BETWEEN T1.TRANSFER_SDATE AND VALUE(T1.TRANSFER_EDATE, '9999-12-31') ");
            stb.append("   GROUP BY T1.SCHREGNO ");
            stb.append(" ), SCHREG_TRANSFER2 AS ( ");
            stb.append("   SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     MAX(T1.TRANSFER_SDATE) AS TRANSFER_SDATE ");
            stb.append("   FROM SCHREG_TRANSFER_DAT T1 ");
            stb.append("   WHERE ");
            stb.append("     '" + param._edate + "' BETWEEN T1.TRANSFER_SDATE AND VALUE(T1.TRANSFER_EDATE, '9999-12-31') ");
            stb.append("   GROUP BY T1.SCHREGNO ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     T1.GRADE, ");
            stb.append("     GDAT.GRADE_CD, ");
            stb.append("     T1.HR_CLASS, ");
            stb.append("     T1.ATTENDNO, ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T2.HR_NAME, ");
            stb.append("     T2.HR_NAMEABBV, ");
            stb.append("     T3.NAME, ");
            stb.append("     T3.SEX, ");
            stb.append("     NMZ002.NAME2 AS SEX_NAME, ");
            stb.append("     T3.ENT_DATE, ");
            stb.append("     T3.ENT_DIV, ");
            stb.append("     NMA002.NAME1 AS ENT_DIV_NAME, ");
            stb.append("     T3.GRD_DATE, ");
            stb.append("     T3.GRD_DIV, ");
            stb.append("     NMA003.NAME1 AS GRD_DIV_NAME, ");
            stb.append("     T5.TRANSFERCD AS TRANSFERCD1, ");
            stb.append("     NMA004_1.NAME1 AS TRANSFER_NAME1, ");
            stb.append("     T5.TRANSFERREASON AS TRANSFERREASON1, ");
            stb.append("     T5.TRANSFER_SDATE AS TRANSFER_SDATE1, ");
            stb.append("     T5.TRANSFER_EDATE AS TRANSFER_EDATE1, ");
            stb.append("     T7.TRANSFERCD AS TRANSFERCD2, ");
            stb.append("     NMA004_2.NAME1 AS TRANSFER_NAME2, ");
            stb.append("     T7.TRANSFERREASON AS TRANSFERREASON2, ");
            stb.append("     T7.TRANSFER_SDATE AS TRANSFER_SDATE2, ");
            stb.append("     T7.TRANSFER_EDATE AS TRANSFER_EDATE2, ");
            stb.append("     T1.COURSECD, ");
            stb.append("     T1.MAJORCD, ");
            stb.append("     T9.MAJORNAME, ");
            stb.append("     T1.COURSECODE, ");
            stb.append("     T8.COURSECODENAME ");
            if (param._isNaraken) {
                stb.append("   , REFUS.SCHREGNO AS REFUSAL ");
            }
            stb.append(" FROM SCHREG_REGD_DAT T1 ");
            stb.append(" LEFT JOIN SCHREG_REGD_HDAT T2 ON T2.YEAR = T1.YEAR ");
            stb.append("     AND T2.SEMESTER = T1.SEMESTER ");
            stb.append("     AND T2.GRADE = T1.GRADE ");
            stb.append("     AND T2.HR_CLASS = T1.HR_CLASS ");
            stb.append(" LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR ");
            stb.append("     AND GDAT.GRADE = T1.GRADE ");
            stb.append(" LEFT JOIN SCHREG_BASE_MST T3 ON T3.SCHREGNO = T1.SCHREGNO ");
            stb.append(" LEFT JOIN SCHREG_TRANSFER1 T4 ON T4.SCHREGNO = T1.SCHREGNO ");
            stb.append(" LEFT JOIN SCHREG_TRANSFER_DAT T5 ON T5.SCHREGNO = T1.SCHREGNO ");
            stb.append("     AND T5.TRANSFER_SDATE = T4.TRANSFER_SDATE ");
            stb.append(" LEFT JOIN SCHREG_TRANSFER2 T6 ON T6.SCHREGNO = T1.SCHREGNO ");
            stb.append(" LEFT JOIN SCHREG_TRANSFER_DAT T7 ON T7.SCHREGNO = T1.SCHREGNO ");
            stb.append("     AND T7.TRANSFER_SDATE = T6.TRANSFER_SDATE ");
            stb.append(" LEFT JOIN COURSECODE_MST T8 ON T8.COURSECODE = T1.COURSECODE ");
            stb.append(" LEFT JOIN MAJOR_MST T9 ON T9.COURSECD = T1.COURSECD AND T9.MAJORCD = T1.MAJORCD ");
            stb.append(" LEFT JOIN NAME_MST NMA004_1 ON NMA004_1.NAMECD1 = 'A004' AND NMA004_1.NAMECD2 = T5.TRANSFERCD ");
            stb.append(" LEFT JOIN NAME_MST NMA004_2 ON NMA004_2.NAMECD1 = 'A004' AND NMA004_2.NAMECD2 = T7.TRANSFERCD ");
            stb.append(" LEFT JOIN NAME_MST NMZ002 ON NMZ002.NAMECD1 = 'Z002' AND NMZ002.NAMECD2 = T3.SEX ");
            stb.append(" LEFT JOIN NAME_MST NMA002 ON NMA002.NAMECD1 = 'A002' AND NMA002.NAMECD2 = T3.ENT_DIV ");
            stb.append(" LEFT JOIN NAME_MST NMA003 ON NMA003.NAMECD1 = 'A003' AND NMA003.NAMECD2 = T3.GRD_DIV ");
            if (param._isNaraken) {
                stb.append("     LEFT JOIN SCHREG_SCHOOL_REFUSAL_DAT REFUS ON REFUS.YEAR = T1.YEAR ");
                stb.append("          AND REFUS.SCHREGNO = T1.SCHREGNO ");
            }
            stb.append(" WHERE ");
            stb.append(" T1.YEAR = '" + param._year + "' ");
            stb.append(" AND T1.SEMESTER = '" + (SEMEALL.equals(param._semester) ? param._ctrlSemester : param._semester) + "' ");
            stb.append(" AND T1.GRADE = '" + param._grade + "' ");
            if (!("00".equals(param._major) || "0000".equals(param._major))) {
                if (param._major.length() == 2) {
                    stb.append(" AND T1.COURSECD || SUBSTR(T1.MAJORCD, 1, 1) = '" + param._major + "' ");
                } else {
                    stb.append(" AND T1.COURSECD || T1.MAJORCD = '" + param._major + "' ");
                }
            }
            stb.append(" ORDER BY ");
            stb.append("     T1.GRADE, ");
            stb.append("     T1.HR_CLASS, ");
            stb.append("     T1.ATTENDNO ");

            if (param._isOutputDebugQuery) {
                log.info(" regd sql = " + stb.toString());
            }
            for (final Map row : KnjDbUtils.query(db2, stb.toString())) {
                final String grade = KnjDbUtils.getString(row, "GRADE");
                final String gradeCd = KnjDbUtils.getString(row, "GRADE_CD");
                final String hrClass = KnjDbUtils.getString(row, "HR_CLASS");
                final String hrNameabbv = KnjDbUtils.getString(row, "HR_NAMEABBV");
                final String attendno = KnjDbUtils.getString(row, "ATTENDNO");
                final String schregno = KnjDbUtils.getString(row, "SCHREGNO");
                final String hrName = KnjDbUtils.getString(row, "HR_NAME");
                final String name = KnjDbUtils.getString(row, "NAME");
                final String sex = KnjDbUtils.getString(row, "SEX");
                final String sexName = KnjDbUtils.getString(row, "SEX_NAME");
                final String entdiv = KnjDbUtils.getString(row, "ENT_DIV");
                final String entdivName = KnjDbUtils.getString(row, "ENT_DIV_NAME");
                final String entdate = KnjDbUtils.getString(row, "ENT_DATE");
                final String grddiv = KnjDbUtils.getString(row, "GRD_DIV");
                final String grddivName = KnjDbUtils.getString(row, "GRD_DIV_NAME");
                final String grddate = KnjDbUtils.getString(row, "GRD_DATE");
                final String transfercd1 = KnjDbUtils.getString(row, "TRANSFERCD1");
                final String transferName1 = KnjDbUtils.getString(row, "TRANSFER_NAME1");
                final String transferreason1 = KnjDbUtils.getString(row, "TRANSFERREASON1");
                final String transferSdate1 = KnjDbUtils.getString(row, "TRANSFER_SDATE1");
                final String transferEdate1 = KnjDbUtils.getString(row, "TRANSFER_EDATE1");
                final String transfercd2 = KnjDbUtils.getString(row, "TRANSFERCD2");
                final String transferName2 = KnjDbUtils.getString(row, "TRANSFER_NAME2");
                final String transferreason2 = KnjDbUtils.getString(row, "TRANSFERREASON2");
                final String transferSdate2 = KnjDbUtils.getString(row, "TRANSFER_SDATE2");
                final String transferEdate2 = KnjDbUtils.getString(row, "TRANSFER_EDATE2");
                final String coursecd = KnjDbUtils.getString(row, "COURSECD");
                final String majorcd = KnjDbUtils.getString(row, "MAJORCD");
                final String majorname = KnjDbUtils.getString(row, "MAJORNAME");
                final String coursecode = KnjDbUtils.getString(row, "COURSECODE");
                final String coursecodename = KnjDbUtils.getString(row, "COURSECODENAME");
                String refusal = null;
                if (param._isNaraken) {
                    refusal = KnjDbUtils.getString(row, "REFUSAL");
                }
                final Student student = new Student(grade, gradeCd, hrClass, hrNameabbv, attendno, schregno, hrName, name, sex, sexName, entdiv, entdivName, entdate, grddiv, grddivName, grddate,
                        transfercd1, transferName1, transferreason1 ,transferSdate1, transferEdate1,
                        transfercd2, transferName2, transferreason2, transferSdate2, transferEdate2,
                        coursecd, majorcd, majorname, coursecode, coursecodename, refusal);
                studentList.add(student);
                studentMap.put(student._schregno, student);
            }
        } catch (Exception ex) {
            log.fatal("exception!", ex);
        }
        if ("1".equals(param._outputShidou) || "1".equals(param._outputYuryo) || "1".equals(param._outputFushin)) {
            if ("1".equals(param._outputRemark)) {
                // 出身学校
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     T1.SCHREGNO, ");
                stb.append("     T3.ENT_DIV, ");
                stb.append("     NMA002.NAME1 AS ENT_DIV_NAME, ");
                stb.append("     FINSCH.FINSCHOOL_NAME ");
                stb.append(" FROM SCHREG_REGD_DAT T1 ");
                stb.append(" INNER JOIN SCHREG_BASE_MST T3 ON T3.SCHREGNO = T1.SCHREGNO ");
                stb.append(" INNER JOIN FINSCHOOL_MST FINSCH ON FINSCH.FINSCHOOLCD = T3.FINSCHOOLCD ");
                stb.append(" LEFT JOIN NAME_MST NMA002 ON NMA002.NAMECD1 = 'A002' ");
                stb.append("   AND NMA002.NAMECD2 = T3.ENT_DIV ");
                stb.append(" WHERE ");
                stb.append(" T1.YEAR = '" + param._year + "' ");
                stb.append(" AND T1.SEMESTER = '" + (SEMEALL.equals(param._semester) ? param._ctrlSemester : param._semester) + "' ");
                stb.append(" AND T1.GRADE = '" + param._grade + "' ");

                try {
                    for (final Map row : KnjDbUtils.query(db2, stb.toString())) {
                        final Student student = studentMap.get(KnjDbUtils.getString(row, "SCHREGNO"));
                        if (null == student) {
                            continue;
                        }
                        final String entDivName;
                        final String entDiv = KnjDbUtils.getString(row, "ENT_DIV");
                        if ("1".equals(entDiv) || "2".equals(entDiv) || "3".equals(entDiv)) {
                            entDivName = "（" + StringUtils.defaultString(KnjDbUtils.getString(row, "ENT_DIV_NAME")) + "）";
                        } else {
                            entDivName = "";
                        }
                        student._remarkList.add(StringUtils.defaultString(KnjDbUtils.getString(row, "FINSCHOOL_NAME")) + entDivName);
                    }
                } catch (Exception ex) {
                    log.fatal("exception!", ex);
                }

            } else if ("2".equals(param._outputRemark)) {
                // 部活
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT DISTINCT ");
                stb.append("     T1.SCHREGNO, ");
                stb.append("     T2.SDATE, ");
                stb.append("     T2.CLUBCD, ");
                stb.append("     T3.CLUBNAME ");
                stb.append(" FROM SCHREG_REGD_DAT T1 ");
                stb.append(" INNER JOIN SCHREG_CLUB_HIST_DAT T2 ON T2.SCHREGNO = T1.SCHREGNO ");
                stb.append("     AND T2.SDATE <= '" + param._edate + "' ");
                stb.append("     AND VALUE(T2.EDATE, '9999-12-31') >= '" + param._edate + "' ");
                stb.append(" INNER JOIN CLUB_MST T3 ON T3.CLUBCD = T2.CLUBCD ");
                stb.append(" WHERE ");
                stb.append(" T1.YEAR = '" + param._year + "' ");
                stb.append(" AND T1.SEMESTER = '" + (SEMEALL.equals(param._semester) ? param._ctrlSemester : param._semester) + "' ");
                stb.append(" AND T1.GRADE = '" + param._grade + "' ");
                stb.append(" ORDER BY T1.SCHREGNO, T2.SDATE, T2.CLUBCD ");

                try {
                    for (final Map row : KnjDbUtils.query(db2, stb.toString())) {
                        final Student student = studentMap.get(KnjDbUtils.getString(row, "SCHREGNO"));
                        if (null == student) {
                            continue;
                        }
                        if (!student._remarkList.contains(KnjDbUtils.getString(row, "CLUBNAME"))) {
                            student._remarkList.add(KnjDbUtils.getString(row, "CLUBNAME"));
                        }
                    }
                } catch (Exception ex) {
                    log.fatal("exception!", ex);
                }

            } else if ("3".equals(param._outputRemark)) {
                // 希望進路
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("    T1.SCHREGNO, ");
                stb.append("    T1.COURSE_KIND, ");
                stb.append("    T2.SCHOOL_NAME, ");
                stb.append("    T3.FACULTYNAME, ");
                stb.append("    S1.JOBTYPE_SNAME AS JOBTYPE_SNAME1, ");
                stb.append("    T1.REMARK ");
                stb.append(" FROM COURSE_HOPE_DAT T1 ");
                stb.append("     LEFT JOIN COLLEGE_MST T2 ON T2.SCHOOL_CD = T1.SCHOOL_CD1 ");
                stb.append("     LEFT JOIN COLLEGE_FACULTY_MST T3 ON T3.SCHOOL_CD = T2.SCHOOL_CD ");
                stb.append("         AND T3.FACULTYCD = T1.FACULTYCD1 ");
                stb.append("     LEFT JOIN JOBTYPE_S_MST S1 ON T1.JOBTYPE_LCD1 = S1.JOBTYPE_LCD AND T1.JOBTYPE_MCD1 = S1.JOBTYPE_MCD AND T1.JOBTYPE_SCD1 = S1.JOBTYPE_SCD ");
                stb.append(" WHERE ");
                stb.append("  T1.ENTRYDATE = (SELECT MAX(ENTRYDATE) FROM COURSE_HOPE_DAT  WHERE SCHREGNO = T1.SCHREGNO) ");
                stb.append("    AND T1.SEQ = (SELECT MAX(SEQ) FROM COURSE_HOPE_DAT WHERE SCHREGNO = T1.SCHREGNO ");
                stb.append("    AND ENTRYDATE = T1.ENTRYDATE) ");
                stb.append("    AND  EXISTS (SELECT 'X' FROM ");
                stb.append("      SCHREG_REGD_DAT ");
                stb.append("      WHERE YEAR = '" + param._year + "' ");
                stb.append("          AND SEMESTER = '" + (SEMEALL.equals(param._semester) ? param._ctrlSemester : param._semester) + "' ");
                stb.append("          AND GRADE = '" + param._grade + "' ");
                stb.append("          AND SCHREGNO = T1.SCHREGNO ");
                stb.append("    ) ");

                try {
                    for (final Map row : KnjDbUtils.query(db2, stb.toString())) {
                        final Student student = studentMap.get(KnjDbUtils.getString(row, "SCHREGNO"));
                        if (null == student) {
                            continue;
                        }
                        String remark;
                        remark = KnjDbUtils.getString(row, "REMARK");
                        if (!StringUtils.isBlank(remark)) {
                            if (!student._remarkList.contains(remark)) {
                                student._remarkList.add(remark);
                            }
                        }

                        remark = "";
                        if ("2".equals(KnjDbUtils.getString(row, "COURSE_KIND"))) {
                            remark = StringUtils.defaultString(KnjDbUtils.getString(row, "JOBTYPE_SNAME1"));
                        } else if ("1".equals(KnjDbUtils.getString(row, "COURSE_KIND"))) {
                            remark = StringUtils.defaultString(KnjDbUtils.getString(row, "SCHOOL_NAME")) + StringUtils.defaultString(KnjDbUtils.getString(row, "FACULTYNAME"));
                        }
                        if (!StringUtils.isBlank(remark)) {
                            if (!student._remarkList.contains(remark)) {
                                student._remarkList.add(remark);
                            }
                        }
                    }
                } catch (Exception ex) {
                    log.fatal("exception!", ex);
                }
            }
        }
        return studentList;
    }

    /**
     * 生徒のリストから生徒の科目の得点のリストを得る
     * @param studentList 生徒のリスト
     * @param subclasscd 科目
     * @return 生徒のリストから生徒の科目の得点のリストを得る
     */
    private static List<String> getScoreList(final List<Student> studentList, final String subclasscd) {
        final List<String> scoreList = new ArrayList<String>();
        for (final Student student : studentList) {
            final String score = student.getScore(subclasscd);
            if (null != score) {
                scoreList.add(score);
            }
        }
        return scoreList;
    }

    /**
     * 生徒のリストから生徒の科目の平均のリストを得る
     * @param studentList 生徒のリスト
     * @param subclasscd 科目
     * @return 生徒のリストから生徒の科目の平均のリストを得る
     */
    private static List<String> getAvgList(final List<Student> studentList) {
        final List<String> avgList = new ArrayList<String>();
        for (final Student student : studentList) {
            final String avg = null == student._subclassScore999999 || null == student._subclassScore999999._avg ? null : student._subclassScore999999._avg.toString();
            if (null != avg) {
                avgList.add(avg);
            }
        }
        return avgList;
    }

    /**
     *　生徒の科目のリストを得る
     * @param param
     * @param studentList 生徒のリスト
     * @param includeAttend 出欠のある科目を含めるか
     * @return 生徒の科目のリスト。includeAttendが<code>true</code>の場合、ATTEND_SUBCLASS_DATの科目を含める
     */
    private static List<Subclass> getSubclassList(final Param param, final List<Student> studentList, final boolean includeAttend) {
        final Set<String> set = new TreeSet<String>(); // 科目コードのセット
        for (final Student student : studentList) {
            for (final SubclassScore subScore : student._subclassScore.values()) {
                if (null != subScore._subclass._subclasscd) {
                    set.add(subScore._subclass._subclasscd);
                }
            }
            if (includeAttend) {
                for (final SubclassAttendance sa : student._subclassAttendance.values()) {
                    if (null == sa._subclass || null == sa._subclass._subclasscd || sa._subclass._subclasscd.length()  < 2 || Integer.parseInt(sa._subclass._subclasscd.substring(0, 2)) > 90) {
                        continue;
                    }
                    if (null != sa._subclass._subclasscd) {
                        set.add(sa._subclass._subclasscd);
                    }
                }
            }
        }
        final List<Subclass> rtn = new ArrayList<Subclass>();
        for (final String subclasscd : set) {
            final Subclass subclass = param._subclassMap.get(subclasscd);
            if (null != subclass) {
                rtn.add(subclass);
            }
        }
        return rtn;
    }

    private static class Student {

        private static DecimalFormat attendnodf = new DecimalFormat("00");

        final String _grade;
        final String _gradeCd;
        final String _hrClass;
        final String _hrNameabbv;
        final String _attendno;
        final String _schregno;
        final String _hrName;
        final String _name;
        final String _sex;
        final String _sexName;
        final String _entdiv;
        final String _entdivName;
        final String _entdate;
        final String _grddiv;
        final String _grddivName;
        final String _grddate;
        // 年度開始日時点の異動データ
        final String _transfercd1;
        final String _transfername1;
        final String _transferreason1;
        final String _transferSdate1;
        final String _transferEdate1;
        // パラメータ指定日付時点の異動データ
        final String _transfercd2;
        final String _transfername2;
        final String _transferreason2;
        final String _transferSdate2;
        final String _transferEdate2;
        final String _coursecd;
        final String _majorcd;
        final String _majorname;
        final String _coursecode;
        final String _coursecodename;
        final String _refusal;
        final Map<String, SubclassScore> _subclassScore;
        final Map<String, SubclassAttendance> _subclassAttendance;
        final Map<String, List<SubclassScore>> _tsusanKettenKamokuListMap = new TreeMap<String, List<SubclassScore>>();

        private SubclassScore _subclassScore999999;
        private SubclassScore _subclassScore999999BeforeTest;
        private Attendance _attendance;
        private List<String> _remarkList = new ArrayList<String>();
        private Map<String, String> _specialReasonTestDatRemarkMap = new HashMap<String, String>();

        Student(
            final String grade,
            final String gradeCd,
            final String hrClass,
            final String hrNameabbv,
            final String attendno,
            final String schregno,
            final String hrName,
            final String name,
            final String sex,
            final String sexName,
            final String entdiv,
            final String entdivName,
            final String entdate,
            final String grddiv,
            final String grddivName,
            final String grddate,
            final String transfercd1,
            final String transfername1,
            final String transferreason1,
            final String transferSdate1,
            final String transferEdate1,
            final String transfercd2,
            final String transfername2,
            final String transferreason2,
            final String transferSdate2,
            final String transferEdate2,
            final String coursecd,
            final String majorcd,
            final String majorname,
            final String coursecode,
            final String coursecodename,
            final String refusal
        ) {
            _grade = grade;
            _gradeCd = gradeCd;
            _hrClass = hrClass;
            _hrNameabbv = hrNameabbv;
            _attendno = attendno;
            _schregno = schregno;
            _hrName = hrName;
            _name = name;
            _sex = sex;
            _sexName = sexName;
            _entdiv = entdiv;
            _entdivName = entdivName;
            _entdate = entdate;
            _grddiv = grddiv;
            _grddivName = grddivName;
            _grddate = grddate;
            _transfercd1 = transfercd1;
            _transfername1 = transfername1;
            _transferreason1 = transferreason1;
            _transferSdate1 = transferSdate1;
            _transferEdate1 = transferEdate1;
            _transfercd2 = transfercd2;
            _transfername2 = transfername2;
            _transferreason2 = transferreason2;
            _transferSdate2 = transferSdate2;
            _transferEdate2 = transferEdate2;
            _coursecd = coursecd;
            _majorcd = majorcd;
            _majorname = majorname;
            _coursecode = coursecode;
            _coursecodename = coursecodename;
            _refusal = refusal;
            _subclassScore = new TreeMap();
            _subclassAttendance = new TreeMap();
        }

        public String coursecdMajorcd() {
            return _coursecd + _majorcd;
        }

        public String course() {
            return _coursecd + _majorcd + _coursecode;
        }

        public String getSpecialReasonTestDatRemark(final String key) {
            // log.debug(_schregno + " >> " + _specialReasonTestDatRemarkMap + " , " + key + " -> " + _specialReasonTestDatRemarkMap.get(key));
            return _specialReasonTestDatRemarkMap.get(key);
        }

        private String getHrNameabbvAttendnoCd(final Param param) {
            return
            StringUtils.defaultString(_hrNameabbv) + "-" +
            StringUtils.defaultString((NumberUtils.isDigits(_attendno)) ? attendnodf.format(Integer.parseInt(_attendno)) : _attendno);
        }

        private String getHrclassAttendnoCd() {
            return
            StringUtils.defaultString((NumberUtils.isDigits(_hrClass)) ? String.valueOf(Integer.parseInt(_hrClass)) : _hrClass) +
            StringUtils.defaultString((NumberUtils.isDigits(_attendno)) ? attendnodf.format(Integer.parseInt(_attendno)) : _attendno);
        }

        private String getGradeHrclassAttendnoCd() {
            return
            StringUtils.defaultString((NumberUtils.isDigits(_gradeCd)) ? String.valueOf(Integer.parseInt(_gradeCd)) : _gradeCd) + "-" +
            StringUtils.defaultString((NumberUtils.isDigits(_hrClass)) ? String.valueOf(Integer.parseInt(_hrClass)) : _hrClass) + "-" +
            StringUtils.defaultString((NumberUtils.isDigits(_attendno)) ? attendnodf.format(Integer.parseInt(_attendno)) : _attendno);
        }

        private List<SubclassScore> getKesshiSubclassList() {
            final List<SubclassScore> list = new ArrayList<SubclassScore>();
            for (final String subclasscd : _subclassScore.keySet()) {
                final SubclassScore subScore = _subclassScore.get(subclasscd);
                if (null == subScore._score && !"1".equals(subScore._subclass._subclassScoreAllNull)) {
                    list.add(subScore);
                }
            }
            return list;
        }

        private List<SubclassScore> getKettenSubclassList(final Param param) {
            final List<SubclassScore> list = new ArrayList<SubclassScore>();
            for (final String subclasscd : _subclassScore.keySet()) {
                final SubclassScore subScore = _subclassScore.get(subclasscd);
                if (param.isYouTuishido(subScore._score)) {
                    list.add(subScore);
                }
            }
            return list;
        }

        private String getScore(final String subclasscd) {
            final SubclassScore subScore;
            if (SUBCLASSCD999999.equals(subclasscd)) {
                subScore = _subclassScore999999;
            } else {
                subScore = _subclassScore.get(subclasscd);
            }
            return null == subScore ? null : subScore._score;
        }

        private String getAvg(final String subclasscd) {
            final SubclassScore subScore;
            if (SUBCLASSCD999999.equals(subclasscd)) {
                subScore = _subclassScore999999;
            } else {
                subScore = _subclassScore.get(subclasscd);
            }
            return null == subScore ? null : sishaGonyu(subScore._avg);
        }

        public static Map<String, List<Student>> groupByCourse(final List<Student> studentList) {
            final Map<String, List<Student>> rtn = new TreeMap<String, List<Student>>();
            for (final Student student : studentList) {
                getMappedList(rtn, student.course()).add(student);
            }
            return rtn;
        }

        public String getRemark() {
            final List<String> itemList = new ArrayList<String>();
            for (final String item : _remarkList) {
                if (!StringUtils.isBlank(item)) {
                    itemList.add(item);
                }
            }
            return getText(itemList, "、");
        }

        /**
         *
         * @param flg 1:学年開始日 2:指定日付 0:どちらか
         * @return
         */
        public boolean isRyugaku(final int flg) {
            final String cd = "1"; // 留学
            return flg == 1 && cd.equals(_transfercd1)  || flg == 2 && cd.equals(_transfercd2) || flg == 0 && (cd.equals(_transfercd1) || cd.equals(_transfercd2));
        }

        /**
         *
         * @param flg 1:学年開始日 2:指定日付 0:どちらか
         * @return
         */
        public boolean isKyugaku(final int flg) {
            final String cd = "2"; // 休学
            return flg == 1 && cd.equals(_transfercd1)  || flg == 2 && cd.equals(_transfercd2) || flg == 0 && (cd.equals(_transfercd1) || cd.equals(_transfercd2));
        }

        /**
         *
         * @param flg 1:学年開始日 2:指定日付 0:どちらか
         * @return
         */
        public boolean isRyugakuKyugaku(final int flg) {
            return isRyugaku(flg) || isKyugaku(flg);
        }

        public boolean isTenhennyuugaku(final Param param) {
            final boolean isTenhennyuugaku = ("4".equals(_entdiv) || "5".equals(_entdiv) || "7".equals(_entdiv)) && (null == _entdate || param._yearSdate.compareTo(_entdate) <= 0);
//            if (null != _entdate) {
//                log.info(" " + toString() + " Tenhennyuugaku " + _entdate + " ( " + param._yearSdate + ")");
//            }
            return isTenhennyuugaku;
        }

        public boolean isTenhennyuugaku(final Param param, final String date) {
            final boolean isTenhennyuugaku = ("4".equals(_entdiv) || "5".equals(_entdiv) || "7".equals(_entdiv)) && (null == _entdate || param._yearSdate.compareTo(_entdate) <= 0 && null != _entdate && _entdate.compareTo(date) <= 0);
            return isTenhennyuugaku;
        }

        public boolean isJoseki(final Param param, final String date) {
            final boolean isJoseki = null != _grddiv && !"4".equals(_grddiv) && null != _grddate && ((param._yearSdate.compareTo(_grddate) <= 0 && (null == date || _grddate.compareTo(date) <= 0)));
//            if (isJoseki) {
//                log.debug(" " + toString() + " joseki = " + isJoseki + " : " + _grddiv + " / "   + _grddate + " ( " + param._yearSdate + ", " + date + ")");
//            }
            return isJoseki;
        }

        public boolean isCountJoseki(final Param param, final String date) {
            //                                                                                                                                                             isJosekiとの違いは↓のみ
            final boolean isJoseki = null != _grddiv && !"4".equals(_grddiv) && null != _grddate && ((param._yearSdate.compareTo(_grddate) <= 0 && (null == date || _grddate.compareTo(date) < 0)));
            return isJoseki;
        }

        public String toString() {
            return "Student(" + _schregno + ", " + _hrName + ", " + _attendno + ", " + _name +")";
        }

        public static List<Student> filterCourses(final Param param, final List<Student> studentList, final Collection<String> courses) {
            final List<Student> list = new ArrayList<Student>();
            for (final Student student : studentList) {
                if ("1".equals(param._kyugaku) && student.isKyugaku(2)) {
                    continue;
                }
                if (courses.contains(student.course())) {
                    list.add(student);
                }
            }
            return list;
        }
    }

    /**
     * 1日出欠データ
     */
    private static class Attendance {

        final int _lesson;
        /** 忌引 */
        final int _mourning;
        /** 出停 */
        final int _suspend;
        /** 留学 */
        final int _abroad;
        /** 出席すべき日数 */
        final int _mlesson;
        /** 公欠 */
        final int _absence;
        final int _attend;
        /** 遅刻 */
        final int _late;
        /** 早退 */
        final int _early;

        public Attendance(
                final int lesson,
                final int mourning,
                final int suspend,
                final int abroad,
                final int mlesson,
                final int absence,
                final int attend,
                final int late,
                final int early
        ) {
            _lesson = lesson;
            _mourning = mourning;
            _suspend = suspend;
            _abroad = abroad;
            _mlesson = mlesson;
            _absence = absence;
            _attend = attend;
            _late = late;
            _early = early;
        }

        public BigDecimal getAttendOverLimit(final Param param) {
            final BigDecimal limit = new BigDecimal(_mlesson).multiply(new BigDecimal(param._nissuuBunshi)).divide(new BigDecimal(param._nissuBunbo), 0, BigDecimal.ROUND_HALF_UP);
            return limit;
        }

        public boolean attendOver(final Param param) {
            boolean isOver = false;
            if (new BigDecimal(_absence).compareTo(getAttendOverLimit(param)) > 0) {
                isOver = true;
            }
            return isOver;
        }

        public String toString() {
            return "[lesson=" + _lesson +
            ",mlesson=" + _mlesson +
            ",mourning=" + _mourning +
            ",suspend=" + _suspend +
            ",abroad=" + _abroad +
            ",absence=" + _absence +
            ",attend=" + _attend +
            ",late=" + _late +
            ",leave=" + _early;
        }
    }

    private static class HrClass {
        final String _grade;
        final String _hrClass;
        final String _hrNameabbv;
        final List<Student> _studentList;
        String _avgAvg;

        HrClass(
            final String grade,
            final String hrClass,
            final String hrNameabbv,
            final List<Student> studentList
        ) {
            _grade = grade;
            _hrClass = hrClass;
            _hrNameabbv = hrNameabbv;
            _studentList = Collections.unmodifiableList(studentList);
        }

        public String getCode() {
            return _grade + _hrClass;
        }

        public static List<Student> getZaisekiList(final List<Student> studentList, final String sex, final Param param, final String date) {
            final List<Student> list = new ArrayList<Student>();
            for (final Student student : studentList) {
                if ((null == sex || sex.equals(student._sex)) && !student.isCountJoseki(param, date) && (!student.isTenhennyuugaku(param) || student.isTenhennyuugaku(param, date))) {
                    list.add(student);
                }
            }
            return list;
        }

        public static List<Student> getStudentCountRyugakuKyugakuSex(final List<Student> studentList, final int flg, final String sex) {
            final List<Student> list = new ArrayList();
            for (final Student student : studentList) {
                if (sex.equals(student._sex)) {
                    if (student.isRyugakuKyugaku(flg)) {
                        list.add(student);
                    }
                }
            }
            return list;
        }

        public static HrClass getHrClass(final String grade, final String hrClass, final List<HrClass> hrClassList) {
            for (final HrClass hrclass : hrClassList) {
                if (hrclass._grade.equals(grade) && hrclass._hrClass.equals(hrClass)) {
                    return hrclass;
                }
            }
            return null;
        }

        public static List<HrClass> getHrClassList(final List<Student> studentList) {
            final Map<String, List<Student>> gradeHrclassStudentListMap = new TreeMap<String, List<Student>>();
            for (final Student student : studentList) {
                getMappedList(gradeHrclassStudentListMap, student._grade + student._hrClass).add(student);
            }

            final List<HrClass> list = new ArrayList<HrClass>();
            for (final List<Student> gradeHrclassStudentList : gradeHrclassStudentListMap.values()) {
                final Student st0 = gradeHrclassStudentList.get(0);
                list.add(new HrClass(st0._grade, st0._hrClass, st0._hrNameabbv, gradeHrclassStudentList));
            }
            return list;
        }

        public String getIdouBiko(final DB2UDB db2, final Param param) {
            final List<String> idouItemList = new ArrayList<String>();
            for (final Student student : _studentList) {
                final List<String> list = new ArrayList<String>();
                if (student.isTenhennyuugaku(param, param._edate)) {
                    list.add(StringUtils.defaultString(student._entdivName) + StringUtils.defaultString(KNJ_EditDate.h_format_JP(db2, student._entdate)));
                }
                if (student.isRyugakuKyugaku(2)) {
                    list.add(StringUtils.defaultString(student._transfername2) + StringUtils.defaultString(KNJ_EditDate.h_format_JP(db2, student._transferSdate2)) + "～" + StringUtils.defaultString(KNJ_EditDate.h_format_JP(db2, student._transferEdate2)) + " " + StringUtils.defaultString(student._transferreason2));
                } else if (student.isRyugakuKyugaku(1)) {
                    list.add(StringUtils.defaultString(student._transfername1) + StringUtils.defaultString(KNJ_EditDate.h_format_JP(db2, student._transferSdate1)) + "～" + StringUtils.defaultString(KNJ_EditDate.h_format_JP(db2, student._transferEdate1)) + " " + StringUtils.defaultString(student._transferreason1));
                }
                if (student.isJoseki(param, param._edate)) {
                    list.add(StringUtils.defaultString(student._grddivName) + StringUtils.defaultString(KNJ_EditDate.h_format_JP(db2, student._grddate)));
                }
                if (null != student._grddate) {
                    log.info(" joseki? " + student.isJoseki(param, param._edate) + " / " + student._grddate + ", " + param._edate + ", " + student._grddiv);
                }
                if (!list.isEmpty()) {
                    idouItemList.add(StringUtils.defaultString(student._name) + "（" + getText(list, "、") + "）");
                }
            }
            return getText(idouItemList, "、");
        }

        public String toString() {
            return "HrClass(" + _grade + _hrClass + ":" + _hrNameabbv + ")";
        }
    }

    private static class Subclass implements Comparable<Subclass> {
        final String _subclasscd;
        final String _classabbv;
        final String _subclassname;
        final String _subclassabbv;
        final boolean _isSaki;
        final boolean _isMoto;
        final HashMap<String, Integer> _courseCreditsMap; // Map<String (COURSECD + MAJORCD + COURSECODE), Integer (CREDIT)>
        final HashMap<Integer, Set<String>> _creditsCourseMap; // Map<Integer (CREDIT), Set<String (COURSECD + MAJORCD + COURSECODE)>>
        String _subclassScoreAllNull;
        Subclass(
            final String subclasscd,
            final String classabbv,
            final String subclassname,
            final String subclassabbv,
            final boolean isSaki,
            final boolean isMoto
        ) {
            _subclasscd = subclasscd;
            _classabbv = classabbv;
            _subclassname = subclassname;
            _subclassabbv = subclassabbv;
            _courseCreditsMap = new HashMap();
            _creditsCourseMap = new HashMap();
            _isSaki = isSaki;
            _isMoto = isMoto;
        }

        public int compareTo(final Subclass s) {
            return _subclasscd.compareTo(s._subclasscd);
        }

        public String toString() {
            return "Subclass(" + _subclasscd + ":" + _subclassname + ")";
        }

        /**
         *
         * @param course
         * @return  // Map<Integer, Set<String>>
         */
        public Map<Integer, Set<String>> getCreditCourseCollectionMap(final String course) {
            if (null == course) {
                return _creditsCourseMap;
            }
            final Map<Integer, Set<String>> rtn = new HashMap();
            for (final Map.Entry<Integer, Set<String>> e : _creditsCourseMap.entrySet()) {
                final Integer credit = e.getKey();
                final Set<String> col = e.getValue();
                if (col.contains(course)) {
                    rtn.put(credit, col);
                    break;
                }
            }
            return rtn;
        }
    }

    private static List<BigDecimal> toBd(final List<String> stringList) {
        final List<BigDecimal> rtn = new ArrayList<BigDecimal>();
        for (final String s : stringList) {
            rtn.add(new BigDecimal(s));
        }
        return rtn;
    }

    private static String sishaGonyu(final BigDecimal avg) {
        return null == avg ? null : avg.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
    }

    /**
     * 生徒の科目の得点
     */
    private static class SubclassScore {
        final Student _student;
        final TestItem _testItem;
        final Subclass _subclass;
        final String _score;
        final BigDecimal _avg;
        final DivRank _gradeRank;
        final DivRank _classRank;
        final DivRank _courseRank;
        final DivRank _majorRank;
//        final String _slumpScore;
//        final String _slumpMarkCd;
//        final String _slumpMark;


        SubclassScore(
            final Student student,
            final TestItem testItem,
            final Subclass subclass,
            final String score,
            final BigDecimal avg,
            final DivRank gradeRank,
            final DivRank classRank,
            final DivRank courseRank,
            final DivRank majorRank,
            final String slumpScore,
            final String slumpMarkCd,
            final String slumpMark
        ) {
            _student = student;
            _testItem = testItem;
            _subclass = subclass;
            _score = score;
            _avg = avg;
            _gradeRank = gradeRank;
            _classRank = classRank;
            _courseRank = courseRank;
            _majorRank = majorRank;
//            _slumpScore = slumpScore;
//            _slumpMarkCd = slumpMarkCd;
//            _slumpMark = slumpMark;
        }

        public String getRank(final String rankDiv, final Param param) {
            final DivRank divRank;
            if (OUTPUTRANK_HR.equals(rankDiv)) {
                divRank = _classRank;
            } else if (OUTPUTRANK_MAJOR.equals(rankDiv)) {
                divRank = _majorRank;
            } else if (OUTPUTRANK_COURSE.equals(rankDiv)) {
                divRank = _courseRank;
            } else {
                divRank = _gradeRank;
            }
            return divRank.get(param);
        }

        public static String getSubclassCredit(final Param param) {
            final StringBuffer stb = new StringBuffer();

            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.SEMESTER, ");
            stb.append("     T20.CLASSCD || '-' || T20.SCHOOL_KIND || '-' || T20.CURRICULUM_CD || '-' || T20.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("     T20.CREDITS ");
            stb.append(" FROM SCHREG_REGD_DAT T1 ");
            stb.append(" INNER JOIN CREDIT_MST T20 ON T20.YEAR = T1.YEAR ");
            stb.append("     AND T20.GRADE = T1.GRADE ");
            stb.append("     AND T20.COURSECD= T1.COURSECD ");
            stb.append("     AND T20.MAJORCD = T1.MAJORCD ");
            stb.append("     AND T20.COURSECODE = T1.COURSECODE ");
            stb.append(" WHERE ");
            stb.append(" T1.YEAR = '" + param._year + "' ");
            stb.append(" AND T1.SEMESTER = '" + (SEMEALL.equals(param._semester) ? param._ctrlSemester : param._semester) + "' ");
            stb.append(" AND T1.GRADE = '" + param._grade + "' ");
            stb.append(" AND T20.CLASSCD <= '90' ");
            return stb.toString();
        }

        public static String getSubclassScoreSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append("   WITH REL_COUNT AS (");
            stb.append("   SELECT SUBCLASSCD");
            stb.append("     , CLASSCD ");
            stb.append("     , SCHOOL_KIND ");
            stb.append("     , CURRICULUM_CD ");
            stb.append("     , COUNT(*) AS COUNT ");
            stb.append("          FROM RELATIVEASSESS_MST ");
            stb.append("          WHERE GRADE = '" + param._grade + "' AND ASSESSCD = '3' ");
            stb.append("   GROUP BY SUBCLASSCD");
            stb.append("     , CLASSCD ");
            stb.append("     , SCHOOL_KIND ");
            stb.append("     , CURRICULUM_CD ");
            stb.append("   ) ");

            stb.append("   , SCORE_ALL_NULL_SUBCLASS AS (");
            stb.append("   SELECT T1.SUBCLASSCD");
            stb.append("     , T1.CLASSCD ");
            stb.append("     , T1.SCHOOL_KIND ");
            stb.append("     , T1.CURRICULUM_CD ");
            stb.append("    FROM RECORD_SCORE_DAT T1 ");
            if ("9".equals(param._semester) && HYOTEI_TESTCD.equals(param._testcd)) {
                stb.append("     LEFT JOIN RECORD_PROV_FLG_DAT PROV ON PROV.YEAR = T1.YEAR ");
                stb.append("         AND PROV.CLASSCD = T1.CLASSCD ");
                stb.append("         AND PROV.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("         AND PROV.CURRICULUM_CD = T1.CURRICULUM_CD ");
                stb.append("         AND PROV.SUBCLASSCD = T1.SUBCLASSCD ");
                stb.append("         AND PROV.SCHREGNO = T1.SCHREGNO ");
            }
            stb.append("    WHERE T1.YEAR = '" + param._year + "' ");
            stb.append("        AND T1.SEMESTER = '" + param._semester + "' ");
            stb.append("        AND T1.TESTKINDCD = '" + param._testcd.substring(0, 2) + "' AND T1.TESTITEMCD = '" + param._testcd.substring(2, 4) + "' AND T1.SCORE_DIV = '" + param._testcd.substring(4) + "' ");
            stb.append("   GROUP BY T1.SUBCLASSCD");
            stb.append("     , T1.CLASSCD ");
            stb.append("     , T1.SCHOOL_KIND ");
            stb.append("     , T1.CURRICULUM_CD ");
            stb.append("    HAVING MAX(T1.SCORE) IS NULL ");
            stb.append("       AND MAX(T1.VALUE_DI) IS NULL ");
            if ("9".equals(param._semester) && HYOTEI_TESTCD.equals(param._testcd)) {
                if ("1".equals(param._kariHyotei)) {
                    stb.append("    AND ");
                } else {
                    stb.append("    OR ");
                }
                stb.append("           (MIN(PROV.PROV_FLG) IS NOT NULL OR T1.CLASSCD = '90' OR T1.CLASSCD IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = '" + param._d008Namecd1 + "')) ");
            }
            stb.append("   ) ");
            if ("9".equals(param._semester) && HYOTEI_TESTCD.equals(param._testcd)) {
                getHyoteiDataSql(false, param, stb);
            }
            stb.append(" SELECT ");
            stb.append("     0 AS TESTFLG, ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.SEMESTER, ");
            stb.append("     T20.CLASSCD || '-' || T20.SCHOOL_KIND || '-' || T20.CURRICULUM_CD || '-' || T20.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("     T6.CLASSABBV, ");
            stb.append("     T3.SUBCLASSNAME, ");
            stb.append("     T3.SUBCLASSABBV, ");
            stb.append("     T2.SCORE, ");
            stb.append("     T2.AVG, ");
            stb.append("     T2.GRADE_RANK, ");
            stb.append("     T2.GRADE_AVG_RANK, ");
            stb.append("     T2.CLASS_RANK, ");
            stb.append("     T2.CLASS_AVG_RANK, ");
            stb.append("     T2.COURSE_RANK, ");
            stb.append("     T2.COURSE_AVG_RANK, ");
            stb.append("     T2.MAJOR_RANK, ");
            stb.append("     T2.MAJOR_AVG_RANK, ");
            stb.append("     CASE WHEN T9.SUBCLASSCD IS NOT NULL THEN '1' END AS SCORE_ALL_NULL ");
            stb.append(" FROM SCHREG_REGD_DAT T1 ");
            stb.append(" INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = T1.SCHREGNO ");
            stb.append("     AND NOT (VALUE(BASE.ENT_DIV, '') IN ('4', '5') AND VALUE(BASE.ENT_DATE, '1900-01-01') > '" + param._edate + "') ");
            stb.append("     AND NOT (VALUE(BASE.GRD_DIV, '') IN ('1', '2', '3') AND VALUE(BASE.GRD_DATE, '9999-12-31') < '" + param._edate + "') ");
            stb.append(" INNER JOIN RECORD_SCORE_DAT T20 ON T20.YEAR = T1.YEAR ");
            stb.append("     AND T20.SEMESTER = '" + param._semester + "' ");
            stb.append("     AND T20.TESTKINDCD = '" + param._testcd.substring(0, 2) + "' AND T20.TESTITEMCD = '" + param._testcd.substring(2, 4) + "' AND T20.SCORE_DIV = '" + param._testcd.substring(4) + "' ");
            stb.append("     AND T20.SCHREGNO = T1.SCHREGNO ");
            if ("9".equals(param._semester) && HYOTEI_TESTCD.equals(param._testcd)) {
                stb.append(" LEFT JOIN HYOTEI_DATA T2 ON T2.SUBCLASSCD = T20.CLASSCD || '-' || T20.SCHOOL_KIND || '-' || T20.CURRICULUM_CD || '-' || T20.SUBCLASSCD ");
                stb.append("     AND T2.SCHREGNO = T20.SCHREGNO ");
            } else {
                stb.append(" LEFT JOIN RECORD_RANK_SDIV_DAT T2 ON T2.YEAR = T20.YEAR ");
                stb.append("     AND T2.SEMESTER = T20.SEMESTER ");
                stb.append("     AND T2.TESTKINDCD || T2.TESTITEMCD || T2.SCORE_DIV = T20.TESTKINDCD || T20.TESTITEMCD || T20.SCORE_DIV ");
                stb.append("     AND T2.CLASSCD = T20.CLASSCD ");
                stb.append("     AND T2.SCHOOL_KIND = T20.SCHOOL_KIND ");
                stb.append("     AND T2.CURRICULUM_CD = T20.CURRICULUM_CD ");
                stb.append("     AND T2.SUBCLASSCD = T20.SUBCLASSCD ");
                stb.append("     AND T2.SCHREGNO = T20.SCHREGNO ");
            }
            stb.append(" LEFT JOIN SUBCLASS_MST T3 ON T3.SUBCLASSCD = T20.SUBCLASSCD ");
            stb.append("     AND T3.SCHOOL_KIND = T20.SCHOOL_KIND ");
            stb.append("     AND T3.CURRICULUM_CD = T20.CURRICULUM_CD ");
            stb.append("     AND T3.CLASSCD = T20.CLASSCD ");
//            stb.append(" LEFT JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV T4 ON T4.YEAR = T20.YEAR ");
//            stb.append("     AND T4.SEMESTER = T20.SEMESTER ");
//            stb.append("     AND T4.TESTKINDCD = T20.TESTKINDCD ");
//            stb.append("     AND T4.TESTITEMCD = T20.TESTITEMCD ");
//            stb.append("     AND T4.SCORE_DIV = T20.SCORE_DIV ");
            stb.append("  LEFT JOIN CLASS_MST T6 ON T6.CLASSCD = T20.CLASSCD ");
            stb.append("     AND T6.SCHOOL_KIND = T20.SCHOOL_KIND ");
            stb.append("  LEFT JOIN SCORE_ALL_NULL_SUBCLASS T9 ON T9.SUBCLASSCD = T20.SUBCLASSCD ");
            stb.append("     AND T9.CLASSCD = T20.CLASSCD ");
            stb.append("     AND T9.SCHOOL_KIND = T20.SCHOOL_KIND ");
            stb.append("     AND T9.CURRICULUM_CD = T20.CURRICULUM_CD ");
            stb.append(" WHERE ");
            stb.append(" T1.YEAR = '" + param._year + "' ");
            stb.append(" AND T1.SEMESTER = '" + (SEMEALL.equals(param._semester) ? param._ctrlSemester : param._semester) + "' ");
            stb.append(" AND T1.GRADE = '" + param._grade + "' ");
            stb.append(" AND T20.CLASSCD <= '90' ");
            stb.append(" UNION ALL ");
            stb.append(" SELECT ");
            stb.append("     0 AS TESTFLG, ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.SEMESTER, ");
            stb.append("     '00' || '-' || '00' || '-' || '00' || '-' || ");
            stb.append("     T2.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("     CAST(NULL AS VARCHAR(1)) AS CLASSABBV, ");
            stb.append("     CAST(NULL AS VARCHAR(1)) AS SUBCLASSNAME, ");
            stb.append("     CAST(NULL AS VARCHAR(1)) AS SUBCLASSABBV, ");
            stb.append("     T2.SCORE, ");
            stb.append("     T2.AVG, ");
            stb.append("     T2.GRADE_RANK, ");
            stb.append("     T2.GRADE_AVG_RANK, ");
            stb.append("     T2.CLASS_RANK, ");
            stb.append("     T2.CLASS_AVG_RANK, ");
            stb.append("     T2.COURSE_RANK, ");
            stb.append("     T2.COURSE_AVG_RANK, ");
            stb.append("     T2.MAJOR_RANK, ");
            stb.append("     T2.MAJOR_AVG_RANK, ");
//            stb.append("     CAST(NULL AS SMALLINT) AS SLUMP_SCORE, ");
//            stb.append("     CAST(NULL AS VARCHAR(1)) AS SLUMP_MARK_CD, ");
//            stb.append("     CAST(NULL AS VARCHAR(1)) AS SLUMP_MARK, ");
            stb.append("     CAST(NULL AS VARCHAR(1)) AS SCORE_ALL_NULL ");
            stb.append(" FROM SCHREG_REGD_DAT T1 ");
            stb.append(" INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = T1.SCHREGNO ");
            stb.append("     AND NOT (VALUE(BASE.ENT_DIV, '') IN ('4', '5') AND VALUE(BASE.ENT_DATE, '1900-01-01') > '" + param._edate + "') ");
            stb.append("     AND NOT (VALUE(BASE.GRD_DIV, '') IN ('1', '2', '3') AND VALUE(BASE.GRD_DATE, '9999-12-31') < '" + param._edate + "') ");
            if ("9".equals(param._semester) && HYOTEI_TESTCD.equals(param._testcd)) {
                stb.append(" INNER JOIN HYOTEI_DATA T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            } else {
                stb.append(" INNER JOIN RECORD_RANK_SDIV_DAT T2 ON T2.YEAR = T1.YEAR ");
                stb.append("     AND T2.SEMESTER = '" + param._semester + "' ");
                stb.append("     AND T2.TESTKINDCD = '" + param._testcd.substring(0, 2) + "' AND T2.TESTITEMCD = '" + param._testcd.substring(2, 4) + "' AND T2.SCORE_DIV = '" + param._testcd.substring(4) + "' ");
                stb.append("     AND T2.SCHREGNO = T1.SCHREGNO ");
            }
            stb.append(" WHERE ");
            stb.append(" T1.YEAR = '" + param._year + "' ");
            stb.append(" AND T1.SEMESTER = '" + (SEMEALL.equals(param._semester) ? param._ctrlSemester : param._semester) + "' ");
            stb.append(" AND T1.GRADE = '" + param._grade + "' ");
            stb.append(" AND T2.SUBCLASSCD = '" + SUBCLASSCD999999 + "' ");
            if (null != param._beforeTestItem) {
                // 前定期考査順位
                stb.append(" UNION ALL ");
                stb.append(" SELECT ");
                stb.append("     1 AS TESTFLG, ");
                stb.append("     T1.SCHREGNO, ");
                stb.append("     T1.SEMESTER, ");
                stb.append("     T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD AS SUBCLASSCD, ");
                stb.append("     CAST(NULL AS VARCHAR(1)) AS CLASSABBV, ");
                stb.append("     CAST(NULL AS VARCHAR(1)) AS SUBCLASSNAME, ");
                stb.append("     CAST(NULL AS VARCHAR(1)) AS SUBCLASSABBV, ");
                stb.append("     T2.SCORE, ");
                stb.append("     T2.AVG, ");
                stb.append("     T2.GRADE_RANK, ");
                stb.append("     T2.GRADE_AVG_RANK, ");
                stb.append("     T2.CLASS_RANK, ");
                stb.append("     T2.CLASS_AVG_RANK, ");
                stb.append("     T2.COURSE_RANK, ");
                stb.append("     T2.COURSE_AVG_RANK, ");
                stb.append("     T2.MAJOR_RANK, ");
                stb.append("     T2.MAJOR_AVG_RANK, ");
//                stb.append("     CAST(NULL AS SMALLINT) AS SLUMP_SCORE, ");
//                stb.append("     CAST(NULL AS VARCHAR(1)) AS SLUMP_MARK_CD, ");
//                stb.append("     CAST(NULL AS VARCHAR(1)) AS SLUMP_MARK, ");
                stb.append("     CAST(NULL AS VARCHAR(1)) AS SCORE_ALL_NULL ");
                stb.append(" FROM SCHREG_REGD_DAT T1 ");
                stb.append(" INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = T1.SCHREGNO ");
                stb.append("     AND NOT (VALUE(BASE.ENT_DIV, '') IN ('4', '5') AND VALUE(BASE.ENT_DATE, '1900-01-01') > '" + param._edate + "') ");
                stb.append("     AND NOT (VALUE(BASE.GRD_DIV, '') IN ('1', '2', '3') AND VALUE(BASE.GRD_DATE, '9999-12-31') < '" + param._edate + "') ");
                stb.append(" INNER JOIN RECORD_RANK_SDIV_DAT T2 ON T2.YEAR = T1.YEAR ");
                stb.append("     AND T2.SEMESTER = '" + param._beforeTestItem._semester + "' ");
                stb.append("     AND T2.TESTKINDCD = '" + param._beforeTestItem._testkindcd + "' ");
                stb.append("     AND T2.TESTITEMCD = '" + param._beforeTestItem._testitemcd + "' ");
                stb.append("     AND T2.SCORE_DIV = '" + param._beforeTestItem._scoreDiv + "' ");
                stb.append("     AND T2.SCHREGNO = T1.SCHREGNO ");
                stb.append(" WHERE ");
                stb.append(" T1.YEAR = '" + param._year + "' ");
                stb.append(" AND T1.SEMESTER = '" + param._beforeTestItem._semester + "' ");
                stb.append(" AND T1.GRADE = '" + param._grade + "' ");
                stb.append(" AND T2.SUBCLASSCD = '" + SUBCLASSCD999999 + "' ");
            }
            return stb.toString();
        }

        public static void getHyoteiDataSql(final boolean isAvg, final Param param, final StringBuffer stb) {
            if (isAvg) {
                stb.append(" WITH ");
            } else {
                stb.append(" , ");
            }
            stb.append("   REGD AS ( ");
            stb.append("     SELECT ");
            stb.append("      T1.SCHREGNO, ");
            stb.append("      T1.GRADE, ");
            stb.append("      T1.HR_CLASS, ");
            stb.append("      T1.COURSECD, ");
            stb.append("      T1.MAJORCD, ");
            stb.append("      T1.COURSECODE ");
            stb.append("     FROM SCHREG_REGD_DAT T1 ");
            stb.append("     WHERE ");
            stb.append("      T1.YEAR = '" + param._year + "' ");
            stb.append("      AND T1.SEMESTER = '" + (SEMEALL.equals(param._semester) ? param._ctrlSemester : param._semester) + "' ");
            stb.append("      AND T1.GRADE = '" + param._grade + "' ");
            stb.append(" ), REC AS ( ");
            stb.append("     SELECT ");
            stb.append("      T1.SCHREGNO, ");
            stb.append("      T1.CLASSCD, ");
            stb.append("      T1.SCHOOL_KIND, ");
            stb.append("      T1.CURRICULUM_CD, ");
            stb.append("      T1.SUBCLASSCD, ");
            stb.append("      T1.SCORE ");
            stb.append("     FROM RECORD_SCORE_DAT T1 ");
            stb.append("     INNER JOIN REGD T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("     LEFT JOIN RECORD_PROV_FLG_DAT PROV ON PROV.YEAR = T1.YEAR ");
            stb.append("         AND PROV.CLASSCD = T1.CLASSCD ");
            stb.append("         AND PROV.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("         AND PROV.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("         AND PROV.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("         AND PROV.SCHREGNO = T1.SCHREGNO ");
            stb.append("     WHERE ");
            stb.append("      T1.YEAR = '" + param._year + "' ");
            stb.append("      AND T1.SEMESTER = '9' ");
            stb.append("      AND T1.TESTKINDCD = '99' ");
            stb.append("      AND T1.TESTITEMCD = '00' ");
            stb.append("      AND T1.SCORE_DIV = '09' ");
            if ("1".equals(param._kariHyotei)) {
                stb.append(" AND PROV.PROV_FLG = '1' ");
            } else {
                stb.append(" AND PROV.PROV_FLG IS NULL ");
            }
            stb.append(" ), REC2 AS ( ");
            stb.append("     SELECT SCHREGNO, CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS SUBCLASSCD, SCORE, SCORE AS AVG ");
            stb.append("     FROM REC T1 ");
            stb.append("     WHERE SCORE IS NOT NULL ");
            stb.append("     UNION ALL ");
            stb.append("     SELECT SCHREGNO, '999999' AS SUBCLASSCD, SUM(SCORE) AS SCORE, SUM(SCORE) / FLOAT(COUNT(SCORE)) AS AVG ");
            stb.append("     FROM REC T1 ");
            stb.append("     GROUP BY SCHREGNO ");
            stb.append("     HAVING COUNT(SCORE) <> 0 ");
            stb.append(" ), SCHREG_TOTAL_RANK AS ( ");
            stb.append("     SELECT T1.SCHREGNO, T1.SUBCLASSCD, 'GRADE' AS DIV, RANK() OVER(PARTITION BY T1.SUBCLASSCD, T2.GRADE ORDER BY SCORE DESC) AS RANK ");
            stb.append("     FROM REC2 T1 INNER JOIN REGD T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("     UNION  ");
            stb.append("     SELECT T1.SCHREGNO, T1.SUBCLASSCD, 'HR' AS DIV, RANK() OVER(PARTITION BY T1.SUBCLASSCD, T2.GRADE, T2.HR_CLASS ORDER BY SCORE DESC) AS RANK ");
            stb.append("     FROM REC2 T1 INNER JOIN REGD T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("     UNION  ");
            stb.append("     SELECT T1.SCHREGNO, T1.SUBCLASSCD, 'COURSE' AS DIV, RANK() OVER(PARTITION BY T1.SUBCLASSCD, T2.GRADE, T2.COURSECD, T2.MAJORCD, T2.COURSECODE ORDER BY SCORE DESC) AS RANK ");
            stb.append("     FROM REC2 T1 INNER JOIN REGD T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("     UNION  ");
            stb.append("     SELECT T1.SCHREGNO, T1.SUBCLASSCD, 'MAJOR' AS DIV, RANK() OVER(PARTITION BY T1.SUBCLASSCD, T2.GRADE, T2.COURSECD, T2.MAJORCD ORDER BY SCORE DESC) AS RANK ");
            stb.append("     FROM REC2 T1 INNER JOIN REGD T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append(" ), SCHREG_AVG_RANK AS ( ");
            stb.append("     SELECT T1.SCHREGNO, T1.SUBCLASSCD, 'GRADE' AS DIV, RANK() OVER(PARTITION BY T1.SUBCLASSCD, T2.GRADE ORDER BY AVG DESC) AS RANK ");
            stb.append("     FROM REC2 T1 INNER JOIN REGD T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("     UNION  ");
            stb.append("     SELECT T1.SCHREGNO, T1.SUBCLASSCD, 'HR' AS DIV, RANK() OVER(PARTITION BY T1.SUBCLASSCD, T2.GRADE, T2.HR_CLASS ORDER BY AVG DESC) AS RANK ");
            stb.append("     FROM REC2 T1 INNER JOIN REGD T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("     UNION  ");
            stb.append("     SELECT T1.SCHREGNO, T1.SUBCLASSCD, 'COURSE' AS DIV, RANK() OVER(PARTITION BY T1.SUBCLASSCD, T2.GRADE, T2.COURSECD, T2.MAJORCD, T2.COURSECODE ORDER BY AVG DESC) AS RANK ");
            stb.append("     FROM REC2 T1 INNER JOIN REGD T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("     UNION  ");
            stb.append("     SELECT T1.SCHREGNO, T1.SUBCLASSCD, 'MAJOR' AS DIV, RANK() OVER(PARTITION BY T1.SUBCLASSCD, T2.GRADE, T2.COURSECD, T2.MAJORCD ORDER BY AVG DESC) AS RANK ");
            stb.append("     FROM REC2 T1 INNER JOIN REGD T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append(" ), SUBCLASS_AVG AS ( ");
            stb.append("     SELECT T1.SUBCLASSCD, 'GRADE' AS DIV, T2.GRADE AS KEY, SUM(SCORE) AS SCORE, COUNT(SCORE) AS COUNT, MAX(SCORE) AS MAX, MIN(SCORE) AS MIN, SUM(SCORE) / FLOAT(COUNT(SCORE)) AS AVG ");
            stb.append("     FROM REC2 T1 INNER JOIN REGD T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("     GROUP BY T1.SUBCLASSCD, T2.GRADE ");
            stb.append("     UNION  ");
            stb.append("     SELECT T1.SUBCLASSCD, 'HR' AS DIV, T2.GRADE || T2.HR_CLASS AS KEY, SUM(SCORE) AS SCORE, COUNT(SCORE) AS COUNT, MAX(SCORE) AS MAX, MIN(SCORE) AS MIN, SUM(SCORE) / FLOAT(COUNT(SCORE)) AS AVG ");
            stb.append("     FROM REC2 T1 INNER JOIN REGD T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("     GROUP BY T1.SUBCLASSCD, T2.GRADE, T2.HR_CLASS ");
            stb.append("     UNION  ");
            stb.append("     SELECT T1.SUBCLASSCD, 'COURSE' AS DIV, T2.GRADE || T2.COURSECD || T2.MAJORCD || T2.COURSECODE AS KEY, SUM(SCORE) AS SCORE, COUNT(SCORE) AS COUNT, MAX(SCORE) AS MAX, MIN(SCORE) AS MIN, SUM(SCORE) / FLOAT(COUNT(SCORE)) AS AVG ");
            stb.append("     FROM REC2 T1 INNER JOIN REGD T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("     GROUP BY T1.SUBCLASSCD, T2.GRADE, T2.COURSECD, T2.MAJORCD, T2.COURSECODE ");
            stb.append("     UNION  ");
            stb.append("     SELECT T1.SUBCLASSCD, 'MAJOR' AS DIV, T2.GRADE || T2.COURSECD || T2.MAJORCD || '0000' AS KEY, SUM(SCORE) AS SCORE, COUNT(SCORE) AS COUNT, MAX(SCORE) AS MAX, MIN(SCORE) AS MIN, SUM(SCORE) / FLOAT(COUNT(SCORE)) AS AVG ");
            stb.append("     FROM REC2 T1 INNER JOIN REGD T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("     GROUP BY T1.SUBCLASSCD, T2.GRADE, T2.COURSECD, T2.MAJORCD ");
            stb.append(" ) ");
            if (isAvg) {
                stb.append(" SELECT  ");
                stb.append("   T1.DIV, ");
                stb.append("   T1.KEY, ");
                stb.append("   T1.SUBCLASSCD, ");
                stb.append("   T1.SCORE, ");
                stb.append("   T1.COUNT, ");
                stb.append("   T1.MAX, ");
                stb.append("   T1.MIN, ");
                stb.append("   T1.AVG ");
                stb.append(" FROM SUBCLASS_AVG T1 ");
            } else {
                stb.append(" , HYOTEI_DATA AS ( ");
                stb.append(" SELECT  ");
                stb.append("   T1.SCHREGNO, ");
                stb.append("   T1.SUBCLASSCD, ");
                stb.append("   T1.SCORE, ");
                stb.append("   T1.AVG, ");
                stb.append("   R1.RANK AS GRADE_RANK, ");
                stb.append("   A1.RANK AS GRADE_AVG_RANK, ");
                stb.append("   R2.RANK AS CLASS_RANK, ");
                stb.append("   A2.RANK AS CLASS_AVG_RANK, ");
                stb.append("   R3.RANK AS COURSE_RANK, ");
                stb.append("   A3.RANK AS COURSE_AVG_RANK, ");
                stb.append("   R4.RANK AS MAJOR_RANK, ");
                stb.append("   A4.RANK AS MAJOR_AVG_RANK, ");
                stb.append("   S1.AVG AS GRADE_AVG, ");
                stb.append("   S2.AVG AS HR_AVG, ");
                stb.append("   S3.AVG AS COURSE_AVG, ");
                stb.append("   S4.AVG AS MAJOR_AVG ");
                stb.append(" FROM REC2 T1 ");
                stb.append(" INNER JOIN REGD ON REGD.SCHREGNO = T1.SCHREGNO ");
                stb.append(" LEFT JOIN SCHREG_TOTAL_RANK R1 ON R1.SCHREGNO = T1.SCHREGNO AND R1.SUBCLASSCD = T1.SUBCLASSCD AND R1.DIV = 'GRADE' ");
                stb.append(" LEFT JOIN SCHREG_TOTAL_RANK R2 ON R2.SCHREGNO = T1.SCHREGNO AND R2.SUBCLASSCD = T1.SUBCLASSCD AND R2.DIV = 'HR' ");
                stb.append(" LEFT JOIN SCHREG_TOTAL_RANK R3 ON R3.SCHREGNO = T1.SCHREGNO AND R3.SUBCLASSCD = T1.SUBCLASSCD AND R3.DIV = 'COURSE' ");
                stb.append(" LEFT JOIN SCHREG_TOTAL_RANK R4 ON R4.SCHREGNO = T1.SCHREGNO AND R4.SUBCLASSCD = T1.SUBCLASSCD AND R4.DIV = 'MAJOR' ");
                stb.append(" LEFT JOIN SCHREG_AVG_RANK A1 ON A1.SCHREGNO = T1.SCHREGNO AND A1.SUBCLASSCD = T1.SUBCLASSCD AND A1.DIV = 'GRADE' ");
                stb.append(" LEFT JOIN SCHREG_AVG_RANK A2 ON A2.SCHREGNO = T1.SCHREGNO AND A2.SUBCLASSCD = T1.SUBCLASSCD AND A2.DIV = 'HR' ");
                stb.append(" LEFT JOIN SCHREG_AVG_RANK A3 ON A3.SCHREGNO = T1.SCHREGNO AND A3.SUBCLASSCD = T1.SUBCLASSCD AND A3.DIV = 'COURSE' ");
                stb.append(" LEFT JOIN SCHREG_AVG_RANK A4 ON A4.SCHREGNO = T1.SCHREGNO AND A4.SUBCLASSCD = T1.SUBCLASSCD AND A4.DIV = 'MAJOR' ");
                stb.append(" LEFT JOIN SUBCLASS_AVG S1 ON S1.SUBCLASSCD = T1.SUBCLASSCD AND S1.DIV = 'GRADE' AND S1.KEY = REGD.GRADE ");
                stb.append(" LEFT JOIN SUBCLASS_AVG S2 ON S2.SUBCLASSCD = T1.SUBCLASSCD AND S2.DIV = 'HR' AND S2.KEY = REGD.GRADE || REGD.HR_CLASS ");
                stb.append(" LEFT JOIN SUBCLASS_AVG S3 ON S3.SUBCLASSCD = T1.SUBCLASSCD AND S3.DIV = 'COURSE' AND S3.KEY = REGD.GRADE || '000' || REGD.COURSECD || REGD.MAJORCD || REGD.COURSECODE ");
                stb.append(" LEFT JOIN SUBCLASS_AVG S4 ON S4.SUBCLASSCD = T1.SUBCLASSCD AND S4.DIV = 'MAJOR' AND S4.KEY = REGD.GRADE || '000' || REGD.COURSECD || REGD.MAJORCD || '0000' ");
                stb.append(" ) ");
            }
        }
        public String toString() {
            return "SubclassScore(" + _subclass + ", " + _score + ", " + _avg + ")";
        }

        public Number compareValue(final Param param) {
            if ("2".equals(param._outputKijun)) {
                return _avg;
            }
            return null == _score ? null : Integer.valueOf(_score);
        }

        static class DivRank {
            final String _rank;
            final String _avgRank;
            DivRank(final String rank, final String avgRank) {
                _rank = rank;
                _avgRank = avgRank;
            }
            String get(final Param param) {
                final String rank;
                if ("2".equals(param._outputKijun)) {
                    rank = _avgRank;
                } else {
                    rank = _rank;
                }
                return rank;
            }
        }
    }

    /**
     * 科目ごとの出欠データ
     */
    private static class SubclassAttendance {
        final Student _student;
        final Subclass _subclass;
        Integer _jugyoJisu;
        final int _mlesson;
        /** 換算後の欠課数 */
        final BigDecimal _sick;

        public SubclassAttendance(
                final Student student,
                final Subclass subclass,
                final BigDecimal mlesson,
                final BigDecimal sick
                ) {
            _student = student;
            _subclass =subclass;
            _mlesson = mlesson.intValue();
            _sick = sick;
        }

        public int jugyouJisu(final Param param) {
            if (null == _jugyoJisu) {
                if (param._useHouteiKekkaJisu) {
                    final Integer credits = _subclass._courseCreditsMap.get(_student.course());
                    if (null == credits) {
                        _jugyoJisu = new Integer(0);
                    } else {
                        _jugyoJisu = new Integer(credits.intValue() * param._syusuInt);
                    }
                    if (param._isOutputDebug) {
                        param.logOnce("jugyoJisu " + _subclass._subclasscd + ":" + _subclass._subclassabbv + " (useHouteiKekkaJisu) = " + _jugyoJisu + " (course = " + _student.course() + ", credits = " + credits + ", syusu = " + param._syusuInt + ")");
                    }
                } else {
                    _jugyoJisu = new Integer(_mlesson);
                    if (param._isOutputDebug) {
                        param.logOnce("jugyoJisu " + _subclass._subclasscd + ":" + _subclass._subclassabbv + " (mlesson) = " + _jugyoJisu);
                    }
                }
            }
            return _jugyoJisu.intValue();
        }

        /**
         * 欠課時数超過判定上限を得る
         * @param param
         * @return 欠課時数超過判定上限
         */
        public BigDecimal getAttendOverLimit(final Param param) {
            if (param._isNaraken && (SEMEALL.equals(param._semester) || param._isLastSemester) && null != _student._refusal) {
                final BigDecimal limit = new BigDecimal(jugyouJisu(param)).multiply(new BigDecimal(1)).divide(new BigDecimal(2), 0, BigDecimal.ROUND_DOWN);
                log.info(" refusal " + _student._schregno + "(" + _student._grade + _student._hrClass + "-" + _student._attendno + ") " + _subclass._subclasscd + " jisu = " + jugyouJisu(param) + ", limit = " + limit);
                return limit;
            }
            final BigDecimal limit = new BigDecimal(jugyouJisu(param)).multiply(new BigDecimal(param._jisuuBunshi)).divide(new BigDecimal(param._jisuBunbo), 1, BigDecimal.ROUND_HALF_UP);
            return limit;
        }

        /**
         * 欠課時数超過しているか
         * @param param
         * @return 欠課時数超過しているか
         */
        public boolean isAttendOver(final Param param) {
            boolean isOver = false;
            BigDecimal attendOverLimit = getAttendOverLimit(param);
            if (attendOverLimit.doubleValue() > 0 && _sick.compareTo(attendOverLimit) > 0) {
                isOver = true;
            }
            return isOver;
        }
        public String toString() {
            return "SubclassAttendance(" + _subclass + " : " + _mlesson + ", " + (null == _sick ? null : _sick.setScale(1, BigDecimal.ROUND_HALF_UP).toString()) + ")";
        }
    }

    /**
     * 平均点
     */
    private static class AverageDat {

        private static Map getAverageDatMap(final DB2UDB db2, final Param param) {
            final Map averageDatMap = new HashMap();
            // 科目のHR平均
            if ("9".equals(param._semester) && HYOTEI_TESTCD.equals(param._testcd)) {
                try {
                    final StringBuffer stb = new StringBuffer();
                    SubclassScore.getHyoteiDataSql(true, param, stb);
                    for (final Map row : KnjDbUtils.query(db2, stb.toString())) {
                        final String div = KnjDbUtils.getString(row, "DIV");
                        final String subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");
                        final String score = KnjDbUtils.getString(row, "SCORE");
                        final String highscore = KnjDbUtils.getString(row, "MAX");
                        final String lowscore = KnjDbUtils.getString(row, "MIN");
                        final String count = KnjDbUtils.getString(row, "COUNT");
                        final BigDecimal avg = KnjDbUtils.getBigDecimal(row, "AVG", null);
                        final AverageDat averagedat = new AverageDat(subclasscd, null, null, null,
                                null, null, null, score, highscore, lowscore, count, avg);
                        String key = null;
                        if ("GRADE".equals(div)) {
                            key = AVG_DIV_GRADE + subclasscd + KnjDbUtils.getString(row, "KEY");
                        } else if ("HR".equals(div)) {
                            key = AVG_DIV_HR_CLASS + subclasscd + KnjDbUtils.getString(row, "KEY");
                        } else if ("COURSE".equals(div)) {
                            key = AVG_DIV_COURSE + subclasscd + KnjDbUtils.getString(row, "KEY");
                        } else if ("MAJOR".equals(div)) {
                            key = AVG_DIV_MAJOR + subclasscd + KnjDbUtils.getString(row, "KEY");
                        }
                        if (null != key) {
                            averageDatMap.put(key, averagedat);
                        }
                    }
                } catch (Exception ex) {
                    log.fatal("exception!", ex);
                }
            } else {
                try {
                    final String averageSql = AverageDat.getAverageSql(param);
                    for (final Map row : KnjDbUtils.query(db2, averageSql)) {
                        final String grade = KnjDbUtils.getString(row, "GRADE");
                        final String hrClass = KnjDbUtils.getString(row, "HR_CLASS");
                        final String subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");
                        final String avgDiv = KnjDbUtils.getString(row, "AVG_DIV");
                        final String coursecd = KnjDbUtils.getString(row, "COURSECD");
                        final String majorcd = KnjDbUtils.getString(row, "MAJORCD");
                        final String coursecode = KnjDbUtils.getString(row, "COURSECODE");
                        final String score = KnjDbUtils.getString(row, "SCORE");
                        final String highscore = KnjDbUtils.getString(row, "HIGHSCORE");
                        final String lowscore = KnjDbUtils.getString(row, "LOWSCORE");
                        final String count = KnjDbUtils.getString(row, "COUNT");
                        final BigDecimal avg = KnjDbUtils.getBigDecimal(row, "AVG", null);
                        final AverageDat averagedat = new AverageDat(subclasscd, avgDiv, grade, hrClass,
                                coursecd, majorcd, coursecode, score, highscore, lowscore, count, avg);
                        final String keySbclasscd = isSubclass999999(subclasscd, param) ? SUBCLASSCD999999 : subclasscd;
                        String key = null;
                        if (AVG_DIV_GRADE.equals(avgDiv)) {
                            key = avgDiv + keySbclasscd + grade;
                        } else if (AVG_DIV_HR_CLASS.equals(avgDiv)) {
                            key = avgDiv + keySbclasscd + grade + hrClass;
                        } else if (AVG_DIV_COURSE.equals(avgDiv) || AVG_DIV_MAJOR.equals(avgDiv)) {
                            key = avgDiv + keySbclasscd + grade + coursecd + majorcd + coursecode;
                        }
                        if (null != key) {
                            averageDatMap.put(key, averagedat);
                        }
                    }
                } catch (Exception ex) {
                    log.fatal("exception!", ex);
                }
            }
            return averageDatMap;
        }

        final String _subclasscd;
        final String _avgDiv;
        final String _grade;
        final String _hrClass;
        final String _coursecd;
        final String _majorcd;
        final String _coursecode;
        final String _score;
        final String _highscore;
        final String _lowscore;
        final String _count;
        final BigDecimal _avg;

        AverageDat(
            final String subclasscd,
            final String avgDiv,
            final String grade,
            final String hrClass,
            final String coursecd,
            final String majorcd,
            final String coursecode,
            final String score,
            final String highscore,
            final String lowscore,
            final String count,
            final BigDecimal avg
        ) {
            _subclasscd = subclasscd;
            _avgDiv = avgDiv;
            _grade = grade;
            _hrClass = hrClass;
            _coursecd = coursecd;
            _majorcd = majorcd;
            _coursecode = coursecode;
            _score = score;
            _highscore = highscore;
            _lowscore = lowscore;
            _count = count;
            _avg = avg;
        }

        public static String getAverageSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("     T1.AVG_DIV, ");
            stb.append("     T1.GRADE, ");
            stb.append("     T1.HR_CLASS, ");
            stb.append("     T1.COURSECD, ");
            stb.append("     T1.MAJORCD, ");
            stb.append("     T1.COURSECODE, ");
            stb.append("     T1.SCORE, ");
            stb.append("     T1.HIGHSCORE, ");
            stb.append("     T1.LOWSCORE, ");
            stb.append("     T1.COUNT, ");
            stb.append("     T1.AVG ");
            stb.append(" FROM RECORD_AVERAGE_SDIV_DAT T1 ");
            stb.append(" WHERE ");
            stb.append(" T1.YEAR = '" + param._year + "' ");
            stb.append(" AND T1.SEMESTER = '" + param._semester + "' ");
            stb.append(" AND T1.GRADE = '" + param._grade + "' ");
            stb.append(" AND T1.TESTKINDCD = '" + param._testcd.substring(0, 2) + "' AND T1.TESTITEMCD = '" + param._testcd.substring(2, 4) + "' AND T1.SCORE_DIV = '" + param._testcd.substring(4) + "' ");
            stb.append(" AND T1.SUBCLASSCD NOT IN ('333333', '555555') ");
            return stb.toString();
        }
    }

    /**
     * 得点分布
     */
    private static class ScoreDistribution {
        final List<BigDecimal> _scoreList = new ArrayList<BigDecimal>(); // 母集団の得点
        void addScore(final String score) {
            if (NumberUtils.isNumber(score)) {
                _scoreList.add(new BigDecimal(score));
            }
        }
        void addScoreList(final List<String> scoreList) {
            for (final String score : scoreList) {
                addScore(score);
            }
        }
        int getCount(final int lower, final int upper) {
            return getList(lower, upper).size();
        }
        List<BigDecimal> getList(final int lower, final int upper) {
            final List<BigDecimal> rtn = new ArrayList<BigDecimal>();
            for (final BigDecimal i : _scoreList) {
                if (null == i) {
                    continue;
                }
                if (lower <= i.intValue() && i.intValue() <= upper) {
                    rtn.add(i);
                }
            }
            return rtn;
        }

        /**
         * 得点分布のテーブル
         */
        private static int[][] getDistributionScoreTable(final Param param) {
            final int[][] table = new int[21][];
            if (param._is5dankai) {
                for (int i = table.length - 5; i < table.length; i++) { // 下に評定用5段階
                    final int v = table.length - i;
                    table[i] = new int[] {v, v};
                }
            } else {
                for (int i = 0; i < table.length; i++) {
                    final int lower = 100 - 5 * i;
                    final int upper = Math.min(lower + 4, 100);
                    table[i] = new int[] {lower, upper};
                }
            }
            return table;
        }
    }

    private static List getLineList(final String s, final int keta) {
        if (StringUtils.isBlank(s)) return Collections.EMPTY_LIST;
        final List rtn = new ArrayList();
        StringBuffer current = null;
        int bytes = 0;
        for (int i = 0; i < s.length(); i++) {
            final String chr = String.valueOf(s.charAt(i));
            final int chrKeta = getMS932ByteLength(chr);
            if (null == current || bytes + chrKeta > keta) {
                current = new StringBuffer();
                rtn.add(current);
                bytes = 0;
            }
            current.append(chr);
            bytes += chrKeta;
        }
        for (int i = 0; i < rtn.size(); i++) {
            rtn.set(i, rtn.get(i).toString());
        }
        // log.debug(" rtn = " + rtn);
        return rtn;
    }

    private static String zeroToNull(final int n) {
        return 0 == n ? null : String.valueOf(n);
    }

    private static String zenkaku(final int n) {
        final StringBuffer stb = new StringBuffer();
        final String s = String.valueOf(n);
        for (int i = 0; i < s.length(); i++) {
            stb.append((char) (s.charAt(i) - '0' + '０'));
        }
        return stb.toString();
    }

    private static <T> List<List<T>> getPageList(final List<T> list, final int count) {
        final List<List<T>> rtn = new ArrayList<List<T>>();
        List<T> current = null;
        for (final T o : list) {
            if (null == current || current.size() >= count) {
                current = new ArrayList<T>();
                rtn.add(current);
            }
            current.add(o);
        }
        return rtn;
    }

    private static String getText(final List list, final String comma) {
        String cm = "";
        final StringBuffer stb = new StringBuffer();
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Object o = it.next();
            if (null != o && !StringUtils.isBlank(o.toString())) {
                stb.append(cm).append(o.toString());
                cm = comma;
            }
        }
        return stb.toString();
    }

    private static <T> List<T> take(final List<T> list, final int count) {
        return 0 < count && count < list.size() ? new ArrayList<T>(list.subList(0, count)) : new ArrayList<T>(list);
    }

    private static <T> List<T> drop(final List<T> list, final int count) {
        return 0 < count && count < list.size() ? new ArrayList<T>(list.subList(count, list.size())) : 0 < count ? new ArrayList<T>() : new ArrayList<T>(list);
    }

    private String getTitle(final DB2UDB db2) {
        final String nendo = _param._nendo + "年度";
        final String title = StringUtils.defaultString(_param._testItem._semestername) + StringUtils.defaultString(_param._testItem._testitemname) + "成績判定会議資料";
        final String gradename = "（" + StringUtils.defaultString(_param._gradeName2, "第" + String.valueOf(Integer.parseInt(_param._grade))  + "学年") + "）";
        return nendo + title + gradename;
    }

    private void setForm(final Vrw32alp svf, final String formname, int div) {
        log.info(" set form " + formname);
        svf.VrSetForm(formname, div);
    }

    private void print1234(final DB2UDB db2, final Vrw32alp svf, final List<HrClass> hrClassList, final List<Student> studentList) {

        final List<Student> zaisekiList = HrClass.getZaisekiList(studentList, null, _param, _param._edate);
        if ("2".equals(_param._knjd234vPattern)) {
            final String form = "KNJD234V_1B.frm";
            setForm(svf, form, 1);
            svf.VrsOut("REGD_DATE", _param._edateStr); // 在籍対象日付
            svf.VrsOut("REDG_NAME1", _param._nendo + "年度"); // 在籍年度名称
            svf.VrsOut("TITLE", getTitle(db2)); // タイトル

            // 1.在籍
            printB1(svf, studentList);

            // 2.異動一覧
            printB2(db2, svf, studentList);

            // 3.成績がつかなかった者
            print3(svf, 0, take(getPrint3OutputList(zaisekiList), 30));

            // 4.欠席・遅刻・早退の多い者
            if ("1".equals(_param._outputShukketsu)) {
                print4(svf, 0, take(getAttendOverStudentList(_param, zaisekiList), 25));
            }

            svf.VrEndPage();

        } else {
            final String form = "3".equals(_param._knjd234vPattern) ? "KNJD234V_1C.frm" : "KNJD234V_1A.frm";
            final int maxLine0 = "3".equals(_param._knjd234vPattern) ? 20 : 10;
            final List<List<HrClass>> printHrClasspageList = getPageList(hrClassList, maxLine0);
            final List<String[]> print3OutputListAll = getPrint3OutputList(zaisekiList);
            final int maxLine1 = 30;
            final List<List<String[]>> print3PageList = getPageList(print3OutputListAll, maxLine1);
            final List<Student> attendOverStudentListAll = "1".equals(_param._outputShukketsu) ? getAttendOverStudentList(_param, zaisekiList) : Collections.EMPTY_LIST;
            final int maxLine2 = 25;
            final List<List<Student>> attendOverPageList = getPageList(attendOverStudentListAll, maxLine2);

            for (int pi = 0, pmax = Math.max(1, Math.max(Math.max(printHrClasspageList.size(), print3PageList.size()), attendOverPageList.size())); pi < pmax; pi++) {
                final boolean isLastHrPage = pi == printHrClasspageList.size() - 1;
                final List<HrClass> print1OutputList = printHrClasspageList.size() > pi ? printHrClasspageList.get(pi) : Collections.EMPTY_LIST;
                final List<String[]> print3OutputList = print3PageList.size() > pi ? print3PageList.get(pi) : Collections.EMPTY_LIST;
                final List<Student> attendOverStudentList = attendOverPageList.size() > pi ? attendOverPageList.get(pi) : Collections.EMPTY_LIST;

                setForm(svf, form, 1);
                if (!"3".equals(_param._knjd234vPattern) && _param._isReitaku) {
                    svf.VrsOut("DEFAULT_NAME", "３．成績不振者(" + _param._SlumpTensuInf + "点以下)");
                }
                svf.VrsOut("REGD_DATE", _param._edateStr); // 在籍対象日付
                svf.VrsOut("REDG_NAME1", _param._nendo + "年度"); // 在籍年度名称
                svf.VrsOut("TITLE", getTitle(db2)); // タイトル

                // 1.在籍
                print1(db2, svf, print1OutputList, isLastHrPage ? hrClassList : null);

                // 2.組別平均点度数分布
                if (!"3".equals(_param._knjd234vPattern)) {
                    print2(svf, print1OutputList, isLastHrPage ? hrClassList : null);
                }

                // 3.成績がつかなかった者
                print3(svf, maxLine1 * pi, print3OutputList);

                if ("1".equals(_param._outputShukketsu)) {
                    // 4.欠席・遅刻・早退の多い者
                    print4(svf, maxLine2 * pi, attendOverStudentList);
                }

                svf.VrEndPage();
            }
        }
    }

    private void print1(final DB2UDB db2, final Vrw32alp svf, final List<HrClass> pageHrClassList, final List<HrClass> hrClassList) {
        svf.VrsOut("REDG_NAME2", "現在"); // 在籍年度名称
        String biko = null;
        for (int j = 0; j < pageHrClassList.size(); j++) {
            final HrClass hrClass = pageHrClassList.get(j);
            final int gyo = j + 1;
            final int maleCount = HrClass.getZaisekiList(hrClass._studentList, SEX1, _param, _param._yearSdate).size();
            final int femaleCount = HrClass.getZaisekiList(hrClass._studentList, SEX2, _param, _param._yearSdate).size();
            final int count = HrClass.getZaisekiList(hrClass._studentList, null, _param, _param._yearSdate).size();
            svf.VrsOutn("HR_NAME1", gyo, hrClass._hrNameabbv); // クラス名
            svf.VrsOutn("REGD_MALE1", gyo, String.valueOf(maleCount)); // 在籍男
            svf.VrsOutn("REGD_FEMALE1", gyo, String.valueOf(femaleCount)); // 在籍女
            svf.VrsOutn("REGD_TOTAL1", gyo, String.valueOf(count)); // 在籍男女

            //X月現在
            final int maleCount2 = HrClass.getZaisekiList(hrClass._studentList, SEX1, _param, _param._edate).size();
            final int femaleCount2 = HrClass.getZaisekiList(hrClass._studentList, SEX2, _param, _param._edate).size();
            final int count2 = HrClass.getZaisekiList(hrClass._studentList, null, _param, _param._edate).size();
            svf.VrsOutn("REGD_MALE2", gyo, String.valueOf(maleCount2)); // 在籍男
            svf.VrsOutn("REGD_FEMALE2", gyo, String.valueOf(femaleCount2)); // 在籍女
            svf.VrsOutn("REGD_TOTAL2", gyo, String.valueOf(count2)); // 在籍男女

            biko = hrClass.getIdouBiko(db2, _param);
            if (getMS932ByteLength(biko) > 116) {
                svf.VrsOutn("FIELD2_2", gyo, biko); // 備考
            } else {
                svf.VrsOutn("FIELD2", gyo, biko); // 備考
            }
        }
        if (getMS932ByteLength(biko) > 116 * 2 && pageHrClassList.size() == 1) {
            final String[] token = KNJ_EditEdit.get_token(biko, 116 * 2, 10);
            if (null != token) {
                for (int j = 1; j < token.length; j++) {
                    svf.VrsOutn("FIELD2_2", j + 1, token[j]); // 備考
                }
            }
        }

        if (null != hrClassList) {
            int[] totalmaleCount = new int[] {0, 0};
            int[] totalfemaleCount = new int[] {0, 0};
            int[] totalCount = new int[] {0, 0};
            int[] totalmaleRyugakuKyugakuCount = new int[] {0, 0};
            int[] totalfemaleRyugakuKyugakuCount = new int[] {0, 0};
            int[] totalRyugakuKyugakuCount = new int[] {0, 0};
            int flg;

            for (int j = 0; j < hrClassList.size(); j++) {
                final HrClass hrClass = hrClassList.get(j);
                final int maleCount = HrClass.getZaisekiList(hrClass._studentList, SEX1, _param, _param._yearSdate).size();
                final int femaleCount = HrClass.getZaisekiList(hrClass._studentList, SEX2, _param, _param._yearSdate).size();
                final int count = HrClass.getZaisekiList(hrClass._studentList, null, _param, _param._yearSdate).size();
                totalmaleCount[0] += maleCount;
                totalfemaleCount[0] += femaleCount;
                totalCount[0] += count;
                flg = 1; // 年度開始日
                totalmaleRyugakuKyugakuCount[0] += HrClass.getStudentCountRyugakuKyugakuSex(hrClass._studentList, flg, SEX1).size();
                totalfemaleRyugakuKyugakuCount[0] += HrClass.getStudentCountRyugakuKyugakuSex(hrClass._studentList, flg, SEX2).size();
                totalRyugakuKyugakuCount[0] += HrClass.getStudentCountRyugakuKyugakuSex(hrClass._studentList, flg, SEX1).size() + HrClass.getStudentCountRyugakuKyugakuSex(hrClass._studentList, flg, SEX2).size();

                //X月現在
                final int maleCount2 = HrClass.getZaisekiList(hrClass._studentList, SEX1, _param, _param._edate).size();
                final int femaleCount2 = HrClass.getZaisekiList(hrClass._studentList, SEX2, _param, _param._edate).size();
                final int count2 = HrClass.getZaisekiList(hrClass._studentList, null, _param, _param._edate).size();
                totalmaleCount[1] += maleCount2;
                totalfemaleCount[1] += femaleCount2;
                totalCount[1] += count2;
                flg = 2; // 指定日付
                totalmaleRyugakuKyugakuCount[1] += HrClass.getStudentCountRyugakuKyugakuSex(hrClass._studentList, flg, SEX1).size();
                totalfemaleRyugakuKyugakuCount[1] += HrClass.getStudentCountRyugakuKyugakuSex(hrClass._studentList, flg, SEX2).size();
                totalRyugakuKyugakuCount[1] += HrClass.getStudentCountRyugakuKyugakuSex(hrClass._studentList, flg, SEX1).size() + HrClass.getStudentCountRyugakuKyugakuSex(hrClass._studentList, flg, SEX2).size();
            }

            final int LINE_TOTAL = "3".equals(_param._knjd234vPattern) ? 21 : 11;
            svf.VrsOutn("HR_NAME1", LINE_TOTAL, "小計"); // クラス名
            svf.VrsOutn("REGD_MALE1", LINE_TOTAL, String.valueOf(totalmaleCount[0]) + "(" + String.valueOf(totalmaleRyugakuKyugakuCount[0]) + ")"); // 在籍男
            svf.VrsOutn("REGD_FEMALE1", LINE_TOTAL, String.valueOf(totalfemaleCount[0]) + "(" + String.valueOf(totalfemaleRyugakuKyugakuCount[0]) + ")"); // 在籍女
            svf.VrsOutn("REGD_TOTAL1", LINE_TOTAL, String.valueOf(totalCount[0]) + "(" + String.valueOf(totalRyugakuKyugakuCount[0]) + ")"); // 在籍男女
            svf.VrsOutn("REGD_MALE2", LINE_TOTAL, String.valueOf(totalmaleCount[1]) + "(" + String.valueOf(totalmaleRyugakuKyugakuCount[1]) + ")"); // 在籍男
            svf.VrsOutn("REGD_FEMALE2", LINE_TOTAL, String.valueOf(totalfemaleCount[1]) + "(" + String.valueOf(totalfemaleRyugakuKyugakuCount[1]) + ")"); // 在籍女
            svf.VrsOutn("REGD_TOTAL2", LINE_TOTAL, String.valueOf(totalCount[1]) + "(" + String.valueOf(totalRyugakuKyugakuCount[1]) + ")"); // 在籍男女

            svf.VrsOut("TRANSFER_COMMENT", _param._transferComment); // タイトル
        } else {
            svf.VrAttribute("TRANSFER_COMMENT", "X=10000"); // タイトル
        }
    }

    private void printB1(final Vrw32alp svf, final List<Student> studentList) {
        svf.VrsOut("REDG_NAME2", "現在"); // 在籍年度名称
        List tousho = new ArrayList();
        List ryukyu0 = new ArrayList();
        List ryukyu1 = new ArrayList();
        List tenhen = new ArrayList();
        List sotu = new ArrayList();
        List tentai = new ArrayList();
        for (final Student student : studentList) {
            if (null != student._entdate) {
                if (between(student._entdate, null, _param._yearSdate) && !between(student._grddate, null, _param._yearSdate) && !student.isTenhennyuugaku(_param, _param._edate)) {
                    tousho.add(student);
                    if (null != student._transferSdate1) {
                        ryukyu0.add(student);
                    }
                } else if (student.isTenhennyuugaku(_param, _param._edate)) {
                    tenhen.add(student);
                }
            }
            if (null != student._grddate) {
                if (between(student._grddate, _param._yearSdate, _param._edate, false)) {
                    if ("1".equals(student._grddiv)) {
                        sotu.add(student);
                    } else if ("2".equals(student._grddiv) || "3".equals(student._grddiv)) {
                        tentai.add(student);
                    }
                }
            }
            if (null != student._transferSdate2) {
                ryukyu1.add(student);
            }
        }
        svf.VrsOutn("REGD_TOTAL1", 1, String.valueOf(tousho.size()) + "(" + String.valueOf(ryukyu0.size()) + ")");
        svf.VrsOutn("REGD_TOTAL1", 2, String.valueOf(tenhen.size()));
        svf.VrsOutn("REGD_TOTAL1", 3, String.valueOf(sotu.size()));
        svf.VrsOutn("REGD_TOTAL1", 4, String.valueOf(tentai.size()));
        svf.VrsOutn("REGD_TOTAL1", 5, String.valueOf(tousho.size() + tenhen.size() - sotu.size() - tentai.size()) + "(" + String.valueOf(ryukyu1.size()) + ")");
    }

    private boolean between(final String date, final String sdate, final String edate) {
        return between(date, sdate, edate, true);
    }

    private boolean between(final String date, final String sdate, final String edate, final boolean checkSdateEq) {
        return null != date && (null == sdate || checkSdateEq && sdate.equals(date) || sdate.compareTo(date) < 0) && (null == edate || date.compareTo(edate) <= 0);
    }

    private static <A, B> TreeMap<A, B> merge(final Map<A, B> map1, final Map<A, B> map2) {
        final TreeMap<A, B> rtn = new TreeMap<A, B>();
        rtn.putAll(map1);
        rtn.putAll(map2);
        return rtn;
    }

    private static Comparator<String> majorComparator(final Map<String, String> majorHrclassMap) {
        return new Comparator<String>() {
            public int compare(final String majorcd1, final String majorcd2) {
                final String hrClass1 = StringUtils.defaultString(majorHrclassMap.get(majorcd1), "999");
                final String hrClass2 = StringUtils.defaultString(majorHrclassMap.get(majorcd2), "999");
                int cmp;
                cmp = hrClass1.compareTo(hrClass2);
                if (0 != cmp) {
                    return cmp;
                }
                return majorcd1.compareTo(majorcd2);
            }
        };
    }

    private void print2(final Vrw32alp svf, final List<HrClass> pageHrClassList, final List<HrClass> hrClassList) {

        final int[][] table = ScoreDistribution.getDistributionScoreTable(_param);
        for (int ti = 0; ti < table.length; ti++) {
            if (null != table[ti]) {
                svf.VrsOut("POINT_AREA" + String.valueOf(ti + 1), String.valueOf(table[ti][0]) + (table[ti][0] == table[ti][1] ? "" : "～" + String.valueOf(table[ti][1]))); // 点数
            }
        }

        for (int j = 0; j < pageHrClassList.size(); j++) {
            final HrClass hrClass = pageHrClassList.get(j);
            final ScoreDistribution distHrclass = new ScoreDistribution();

            final List<String> scoreList999999 = getAvgList(hrClass._studentList);
            distHrclass.addScoreList(scoreList999999);
            final int gyo = j + 1;
            final Tuple<Integer, Integer> zaisekishaAndKesshisha = getZaisekishaAndKesshisha(_param, hrClass._studentList);
            final int zaisekiSize = zaisekishaAndKesshisha._first;
            final int kessiSize = zaisekishaAndKesshisha._second;
            final String clname = hrClass._hrNameabbv;
            svf.VrsOutn("HR_NAME2" + (getMS932ByteLength(clname) > 10 ? "_3" : getMS932ByteLength(clname) > 6 ? "_2" : ""), gyo, clname); // クラス名
            svf.VrsOutn("REGD_NUM", gyo, String.valueOf(zaisekiSize)); // 在籍数
            svf.VrsOutn("EXAM_NUM", gyo, String.valueOf(zaisekiSize - kessiSize)); // 受験者
            svf.VrsOutn("NO_EXAM_NUM", gyo, String.valueOf(kessiSize)); // 欠試者
            for (int k = 0; k < table.length; k++) {
                if (null != table[k]) {
                    svf.VrsOutn("POINT" + (k + 1), gyo, zeroToNull(distHrclass.getCount(table[k][0], table[k][1]))); // 点数
                }
            }
        }

        final boolean isPrintGakunen = null != hrClassList;
        if (isPrintGakunen) {
            final Map<String, List<Student>> studentListMapCoursecode = new HashMap<String, List<Student>>();
            final TreeMap<String, String> nameMapCoursecode = new TreeMap<String, String>();
            final Map<String, List<Student>> studentListMapMajor = new HashMap<String, List<Student>>();
            final TreeMap<String, String> nameMapMajor = new TreeMap<String, String>();
            final TreeMap<String, Set<String>> majorCourseMap = new TreeMap<String, Set<String>>();

            for (int j = 0; j < hrClassList.size(); j++) {
                final HrClass hrClass = hrClassList.get(j);
                for (final Student student : hrClass._studentList) {
                    if (null != student._coursecode && !StringUtils.isBlank(student._coursecodename)) {
                        nameMapCoursecode.put("COURSECODE:" + student._coursecode, student._coursecodename);
                        getMappedList(studentListMapCoursecode, "COURSECODE:" + student._coursecode).add(student);
                    }
                    if (null != student.coursecdMajorcd() && !StringUtils.isBlank(student._majorname)) {
                        nameMapMajor.put("COURSEMAJOR:" + student.coursecdMajorcd(), student._majorname);
                        getMappedList(studentListMapMajor, "COURSEMAJOR:" + student.coursecdMajorcd()).add(student);

                        // 学科とコースの関連付け
                        if (null != student._coursecode && !StringUtils.isBlank(student._coursecodename)) {
                            getMappedSet(majorCourseMap, "COURSEMAJOR:" + student.coursecdMajorcd()).add("COURSECODE:" + student._coursecode);
                        }
                    }
                }
            }

            final TreeMap<String, List<Student>> groupStudentListMap = merge(studentListMapCoursecode, studentListMapMajor);
            final TreeMap<String, String> groupNameMap = merge(nameMapCoursecode, nameMapMajor);
            log.debug(" majorCourseMap = " + majorCourseMap + ", size = " + majorCourseMap.size());
            List<String> majorCourseCodeList = new ArrayList<String>(majorCourseMap.keySet());
            if (true) {
                final Map<String, String> majorHrclassMap = new TreeMap();
                for (final String majorcd : majorCourseCodeList) {
                    final List<Student> students = studentListMapMajor.get(majorcd);
                    String hrClass = "999";
                    if (null != students) {
                        for (final Student student : students) {
                            if (null != student._hrClass && student._hrClass.compareTo(hrClass) < 0) {
                                hrClass = student._hrClass;
                            }
                        }
                    }
                    majorHrclassMap.put(majorcd, hrClass);
                }
                Collections.sort(majorCourseCodeList, majorComparator(majorHrclassMap));
            }

            final List<String> groupList = new ArrayList<String>();
            for (final String majorcd : majorCourseCodeList) {
                final Set<String> coursecodes = majorCourseMap.get(majorcd);
                if (coursecodes.size() > 1) {
                    // 各学科にコースが2つ以上設定されていれば、コースごとに出力する
                    groupList.addAll(coursecodes);
                } else {
                    // コースが設定されていなければ（「コースなし」の１コースのみとして）、学科ごとに出力する
                    groupList.add(majorcd);
                }
            }

            final int max = 15;
            int LINE_GRADE = 11;  // 学年
            if (groupList.size() > max - LINE_GRADE) {
                LINE_GRADE = Math.max(pageHrClassList.size() + 1, max - groupList.size());
            }
            final ScoreDistribution distGrade = new ScoreDistribution();
            int totalZaisekiCount = 0;
            int totalJukenCount = 0;
            int totalKessiCount = 0;
            for (int j = 0; j < hrClassList.size(); j++) {
                final HrClass hrClass = hrClassList.get(j);

                final List<String> scoreList999999 = getAvgList(hrClass._studentList);
                distGrade.addScoreList(scoreList999999);
                final Tuple<Integer, Integer> zaisekishaAndKesshisha = getZaisekishaAndKesshisha(_param, hrClass._studentList);
                final int zaisekiSize = zaisekishaAndKesshisha._first;
                final int kessiSize = zaisekishaAndKesshisha._second;
                totalZaisekiCount += zaisekiSize;
                totalJukenCount += zaisekiSize - kessiSize;
                totalKessiCount += kessiSize;
            }

            // 度数分布学年
            final String clname2 = _param.getGradeTitle();
            svf.VrsOutn("HR_NAME2" + (getMS932ByteLength(clname2) > 10 ? "_3" : getMS932ByteLength(clname2) > 6 ? "_2" : ""), LINE_GRADE, clname2); // クラス名
            svf.VrsOutn("REGD_NUM", LINE_GRADE, String.valueOf(totalZaisekiCount)); // 在籍数
            svf.VrsOutn("EXAM_NUM", LINE_GRADE, String.valueOf(totalJukenCount)); // 受験者
            svf.VrsOutn("NO_EXAM_NUM", LINE_GRADE, String.valueOf(totalKessiCount)); // 欠試者
            for (int k = 0; k < table.length; k++) {
                if (null != table[k]) {
                    svf.VrsOutn("POINT" + (k + 1), LINE_GRADE, zeroToNull(distGrade.getCount(table[k][0], table[k][1]))); // 点数
                }
            }
            for (int i = 0; i < groupList.size(); i++) {
                final String groupCode = groupList.get(i);
                final ScoreDistribution distCourse = new ScoreDistribution();
                final List<Student> groupStudentList = groupStudentListMap.get(groupCode);
                final List<String> scoreList999999 = getAvgList(groupStudentList);
                distCourse.addScoreList(scoreList999999);
                final Tuple<Integer, Integer> zaisekishaAndKesshisha = getZaisekishaAndKesshisha(_param, groupStudentList);
                final int zaisekiSize = zaisekishaAndKesshisha._first;
                final int kessiSize = zaisekishaAndKesshisha._second;
                final int gyo = LINE_GRADE + 1 + i;
                final String clname = groupNameMap.get(groupCode);
                svf.VrsOutn("HR_NAME2" + (getMS932ByteLength(clname) > 10 ? "_3" : getMS932ByteLength(clname) > 6 ? "_2" : ""), gyo, clname); // クラス名
                svf.VrsOutn("REGD_NUM", gyo, String.valueOf(zaisekiSize)); // 在籍数
                svf.VrsOutn("EXAM_NUM", gyo, String.valueOf(zaisekiSize - kessiSize)); // 受験者
                svf.VrsOutn("NO_EXAM_NUM", gyo, String.valueOf(kessiSize)); // 欠試者
                for (int k = 0; k < table.length; k++) {
                    if (null != table[k]) {
                        svf.VrsOutn("POINT" + (k + 1), gyo, zeroToNull(distCourse.getCount(table[k][0], table[k][1]))); // 点数
                    }
                }
            }
        }
    }

    private void printB2(final DB2UDB db2, final Vrw32alp svf, final List<Student> studentList) {
        final List<Student> idou = new ArrayList<Student>();
        for (final Student student : studentList) {
            if (student.isRyugakuKyugaku(0) || student.isTenhennyuugaku(_param, _param._edate) || student.isJoseki(_param, _param._edate)) {
                idou.add(student);
            }
        }
        int gyo = 0;
        for (int i = 0, max = 40; i < Math.min(idou.size(), max); i++) {
            final Student student = idou.get(i);
            gyo += 1;

            svf.VrsOutn("SCHREG_NO1", gyo, student._schregno); // 学籍番号
            svf.VrsOutn("NAME", gyo, student._name); // 氏名
            svf.VrsOutn("SEX", gyo, student._sexName);
            boolean hasdata = false;
            if (student.isTenhennyuugaku(_param, _param._edate)) {
                if (hasdata) {
                    gyo += 1;
                }
                svf.VrsOutn("GRD_DATE", gyo, KNJ_EditDate.h_format_JP(db2, student._entdate));
                svf.VrsOutn("GRD_REASON", gyo, StringUtils.defaultString(student._entdivName));
                hasdata = true;
            }
            if (student.isRyugakuKyugaku(1)) {
                if (hasdata) {
                    gyo += 1;
                }
                svf.VrsOutn("GRD_DATE", gyo, KNJ_EditDate.h_format_JP(db2, student._transferSdate1));
                svf.VrsOutn("GRD_REASON", gyo, StringUtils.defaultString(student._transfername1) + " " + StringUtils.defaultString(student._transferreason1));
                hasdata = true;
            } else if (student.isRyugakuKyugaku(2)) {
                if (hasdata) {
                    gyo += 1;
                }
                svf.VrsOutn("GRD_DATE", gyo, KNJ_EditDate.h_format_JP(db2, student._transferSdate2));
                svf.VrsOutn("GRD_REASON", gyo, StringUtils.defaultString(student._transfername2) + " " + StringUtils.defaultString(student._transferreason2));
                hasdata = true;
            }
            if (student.isJoseki(_param, _param._edate)) {
                if (hasdata) {
                    gyo += 1;
                }
                svf.VrsOutn("GRD_DATE", gyo, KNJ_EditDate.h_format_JP(db2, student._grddate));
                svf.VrsOutn("GRD_REASON", gyo, StringUtils.defaultString(student._grddivName));
                hasdata = true;
            }
        }
    }

    private List<String[]> getPrint3OutputList(final List<Student> studentList) {
        final List<String[]> outputList = new ArrayList<String[]>();
        final Map<String, Integer> slumpSumMap = new LinkedMap();  //麗澤のみ利用
        final Map<String, Student> slumpStudentMap = new LinkedMap();
        for (final Student student : studentList) {
            if ("1".equals(_param._kyugaku) && student.isKyugaku(2)) {
                continue;
            }
            if (_param._isReitaku) {
                final Map<String, Integer> slumpMap = new LinkedMap();
                for (final String subclasscd : student._subclassScore.keySet()) {
                    if ("".equals(StringUtils.defaultString(subclasscd, ""))) continue;
                    final SubclassScore subScore = student._subclassScore.get(subclasscd);
                    if (!"".equals(StringUtils.defaultString(subScore._score, ""))) {
                        if (Integer.parseInt(_param._SlumpTensuInf) >= Integer.parseInt(subScore._score)) {
                            if (!slumpStudentMap.containsKey(student._schregno)) {
                                slumpStudentMap.put(student._schregno, student);
                            }
                            slumpMap.put(student._schregno + ":" + subclasscd, Integer.parseInt(subScore._score));
                        }
                    }

                }
                slumpSumMap.putAll(slumpMap);
                if (_param._isOutputDebug3) {
                    log.info(" student = " + student + ", subclass(" + slumpMap.size() + ") = " + student._subclassScore);
                }
            } else {
                final List<SubclassScore> kessiSubclassList = student.getKesshiSubclassList();
                if (kessiSubclassList.size() == 0) {
                    continue;
                }
                final List<SubclassScore> notAllNullList = new ArrayList();
                for (final String subclasscd : student._subclassScore.keySet()) {
                    final SubclassScore subScore = student._subclassScore.get(subclasscd);
                    if (!"1".equals(subScore._subclass._subclassScoreAllNull)) {
                        notAllNullList.add(subScore);
                    }

                }
                if (_param._isOutputDebug3) {
                    log.info(" student = " + student + ", kessi(" + kessiSubclassList.size() + ") = " + kessiSubclassList + ", subclass(" + notAllNullList.size() + ") = " + student._subclassScore);
                }
                if (notAllNullList.size() == kessiSubclassList.size()) {
                    List remarkList = new ArrayList();
                    for (int j = 0; j < kessiSubclassList.size(); j++) {
                        final SubclassScore subclassScore = kessiSubclassList.get(j);
                        final String specialReasonTestDatRemark = student.getSpecialReasonTestDatRemark(D056_01 + subclassScore._subclass._subclasscd);
                        if (!StringUtils.isBlank(specialReasonTestDatRemark)  && !remarkList.contains(specialReasonTestDatRemark)) {
                            remarkList.add(specialReasonTestDatRemark);
                        }
                    }
                    String[] arr = { student.getHrNameabbvAttendnoCd(_param), student._schregno, student._name, "全科目", mkString(remarkList, " ")};
                    outputList.add(arr);
                } else {
                    for (int j = 0; j < kessiSubclassList.size(); j++) {
                        final SubclassScore subclassScore = kessiSubclassList.get(j);
                        final String specialReasonTestDatRemark = student.getSpecialReasonTestDatRemark(D056_01 + subclassScore._subclass._subclasscd);
                        String[] arr = { student.getHrNameabbvAttendnoCd(_param), student._schregno, student._name, subclassScore._subclass._subclassabbv, specialReasonTestDatRemark};
                        if (0 != j) {
                            arr[0] = arr[1] = arr[2] = null;
                        }
                        outputList.add(arr);
                    }
                }
            }
        }
        if (_param._isReitaku && slumpSumMap.size() > 0) {
            List<Entry<String, Integer>> list_entries = new ArrayList<Entry<String, Integer>>(slumpSumMap.entrySet());

            for(Entry<String, Integer> entry : list_entries) {
                String schPsubcls = entry.getKey();
                Integer schVal = entry.getValue();
                if (_param._isOutputDebug3) {
                    log.info(" schPsubcls = " + schPsubcls + "  val = " + schVal.intValue());
                }
                String[] cutStr = StringUtils.split(schPsubcls, ':');
                Student student = (Student)slumpStudentMap.get(cutStr[0]);
                if (student == null) continue;
                final SubclassScore subScore = student._subclassScore.get(cutStr[1]);
                final String specialReasonTestDatRemark = student.getSpecialReasonTestDatRemark(D056_01 + subScore._subclass._subclasscd);
                String[] arr = { student.getHrNameabbvAttendnoCd(_param), student._schregno, student._name, subScore._subclass._subclassabbv, specialReasonTestDatRemark};
                if (_param._isOutputDebug3) {
                    log.info(" arr = " + arr);
                }
                outputList.add(arr);
            }
        }

        return outputList;
    }

    private void print3(final Vrw32alp svf, final int startNo, final List<String[]> print3OutputList) {
        for (int j = 0; j < print3OutputList.size(); j++) {
            final String[] arr = print3OutputList.get(j);
            final int gyo = j + 1;
            svf.VrsOutn("NO_3", gyo, String.valueOf(startNo + gyo)); // 年組番号
            svf.VrsOutn("HR_NO1", gyo, arr[0]); // 年組番号
            svf.VrsOutn("SCHREG_NO2", gyo, arr[1]); // 学籍番号
            final String nameField = getMS932ByteLength(arr[2]) > 20 && !"2".equals(_param._knjd234vPattern) ? "NAME1_2" : "NAME1";
            svf.VrsOutn(nameField, gyo, arr[2]); // 氏名
            if ("2".equals(_param._knjd234vPattern)) {
                svf.VrsOutn("NO_GET_REASON1", gyo, arr[3]); // 理由
                svf.VrsOutn("NO_GET_REASON2", gyo, arr[4]); // 欠課理由
            } else {
                svf.VrsOutn("NO_GET_REASON1" + (getMS932ByteLength(arr[3]) > 20 ? "_2" : ""), gyo, arr[3]); // 理由
                svf.VrsOutn("NO_GET_REASON2" + (40 < getMS932ByteLength(arr[4]) ? "_2" : ""), gyo, arr[4]); // 欠課理由
            }
        }
    }

    private void print4(final Vrw32alp svf, final int startNo, final List<Student> attendOverStudentList) {
        svf.VrsOut("ABSENT_MANY", "４．欠席・早退・遅刻の多い者（欠席が" + _param._kesseki + "、遅刻が" + _param._chikoku + "、早退が" + _param._soutai + "以上の者）");
        for (int j = 0; j < attendOverStudentList.size(); j++) {
            final Student student = attendOverStudentList.get(j);
            final int gyo = j + 1;
            svf.VrsOutn("NO_4", gyo, String.valueOf(startNo + gyo)); // 年組番号
            svf.VrsOutn("HR_NO2", gyo, student.getHrNameabbvAttendnoCd(_param)); // 年組番号
            svf.VrsOutn("SCHREG_NO3", gyo, student._schregno); // 学籍番号
            final String nameField = getMS932ByteLength(student._name) > 20 && !"2".equals(_param._knjd234vPattern) ? "NAME2_2" : "NAME2";
            svf.VrsOutn(nameField, gyo, student._name); // 氏名
            svf.VrsOutn("ABSENT", gyo, zeroToNull(student._attendance._absence)); // 欠席
            svf.VrsOutn("LATE", gyo, zeroToNull(student._attendance._late)); // 遅刻
            svf.VrsOutn("EARLY", gyo, zeroToNull(student._attendance._early)); // 早退
            final String specialReasonTestDatRemark = student.getSpecialReasonTestDatRemark(D056_02 + D056_SUBCLASSCD_ZERO);
            if ("2".equals(_param._knjd234vPattern)) {
                svf.VrsOutn("DI_REASON", gyo, specialReasonTestDatRemark); // 欠課理由
            } else {
                svf.VrsOutn("DI_REASON" + (getMS932ByteLength(specialReasonTestDatRemark) > 40 ? "_2" : ""), gyo, specialReasonTestDatRemark); // 欠課理由
            }
        }
    }

    private static List<Student> getAttendOverStudentList(final Param param, final List<Student> studentList) {
        final List<Student> rtn = new ArrayList<Student>();
        for (final Student student : studentList) {
            if ("1".equals(param._kyugaku) && student.isKyugaku(2)) {
                continue;
            }
            if (null != student._attendance) {
                if (student._attendance._absence >= param._kesseki ||
                    student._attendance._late >= param._chikoku ||
                    student._attendance._early >= param._soutai) {
                    rtn.add(student);
                }
            }
        }
        return rtn;
    }

    private static List<Student> getKessiStudentList(final List<Student> studentList) {
        final List<Student> rtn = new ArrayList<Student>();
        for (final Student student : studentList) {
            // log.debug(" student = " + student + ", kessi = " + student.getKesshiSubclassList());
            if (student.getKesshiSubclassList().size() != 0) {
                rtn.add(student);
            }
        }
        return rtn;
    }

    private static Tuple<Integer, Integer> getZaisekishaAndKesshisha(final Param param, final List<Student> studentList) {
        final List<Student> zaisekisha;

        final List<Student> zaisekiList = HrClass.getZaisekiList(studentList, null, param, param._edate);
        final List<Student> allScoreList = new ArrayList<Student>();
        for (final Student student : studentList) {
            if (null != student._subclassScore999999) {
                allScoreList.add(student);
            }
        }

        if (allScoreList.size() == 0) {
            zaisekisha = zaisekiList;
        } else {
            zaisekisha = new ArrayList<Student>(zaisekiList);
            for (final Student student : allScoreList) {
                if (!zaisekisha.contains(student)) {
                    log.info(" add " + student + " to zaisekisha.");
                    zaisekisha.add(student);
                }
            }
            Collections.sort(zaisekisha, new StudentRegdComparator());
            if (param._isOutputDebug) {
                for (final Student student : zaisekisha) {
                    if (!zaisekiList.contains(student)) {
                        log.info(" score student " + student + " not zaiseki in " + param._edate);
                    }
                }
            }
        }

        final List<Student> kesshisha = new ArrayList<Student>();

        boolean hasAnyScore = false;
        for (final Student student : studentList) {
            hasAnyScore = hasAnyScore || student._subclassScore.size() > 0;
            if (null == student._subclassScore999999) {
                kesshisha.add(student);
            }
        }
        if (!hasAnyScore) {
            kesshisha.clear();
        }
        for (final Iterator<Student> kesshiIt = kesshisha.iterator(); kesshiIt.hasNext();) {
            final Student student = kesshiIt.next();
            if (!zaisekiList.contains(student)) {
                log.info(" kesshi student " + student + " not zaisekisha, removed.");
                kesshiIt.remove();
            }
        }
        if (param._isOutputDebug && kesshisha.size() > 0) {
            log.info(" kesshisha = " + kesshisha);
        }

        return Tuple.of(zaisekisha.size(), kesshisha.size());
    }

    private void print5(final Vrw32alp svf, final List<HrClass> hrClassList, final List<Student> studentList, final Map averageDatMap) {
        final String form = "2".equals(_param._knjd234vPattern) ? "KNJD234V_2B.frm" : "KNJD234V_2A.frm";

        // 下段 欠試数
        final int[] kessiCount = new int[10];
        final List allKessiScoreSubclassList = new ArrayList();
        final Map allTuishidoScoreSubclassMap = new HashMap();
        for (final Student student : studentList) {
            final List<SubclassScore> kessiSubclassList = student.getKettenSubclassList(_param);
            if (kessiSubclassList.size() > 0) {
                final int count = Math.min(10, kessiSubclassList.size());
                kessiCount[count - 1]++;
            }
            allKessiScoreSubclassList.addAll(kessiSubclassList);
            for (final SubclassScore subScore : kessiSubclassList) {
                getMappedList(allTuishidoScoreSubclassMap, subScore._subclass._subclasscd).add(student);
            }
        }
        int kessiTotalCount = 0;
        for (int c = 1; c <= 10; c++) {
            kessiTotalCount += kessiCount[c - 1];
        }

        final int maxHrPerPage = 10;
        final int maxSubclassLine = 40;
        final List<List<HrClass>> hrPageList = getPageList(hrClassList, maxHrPerPage);
        for (int hrpi = 0; hrpi < hrPageList.size(); hrpi++) {
            final List<HrClass> pageHrClassLit = hrPageList.get(hrpi);

            setForm(svf, form, 4);
            svf.VrsOut("TOTAL_NAME", "クラス平均"); // 合計名称

            if (hrpi == hrPageList.size() - 1) {
                for (int c = 1; c <= 10; c++) {
                    svf.VrsOut("NO_GET_SUM" + c, String.valueOf(kessiCount[c - 1])); // 個人欠点保有者
                }

                svf.VrsOut("NO_GET_TOTAL1", String.valueOf(kessiTotalCount)); // 個人欠点保有者合計
                svf.VrsOut("NO_GET_TOTAL2", String.valueOf(allKessiScoreSubclassList.size())); // 個人欠点保有者合計
            }

            // HR名称とHRごとの総合点平均点
            for (int j = 0; j < pageHrClassLit.size(); j++) {
                final HrClass hrClass = pageHrClassLit.get(j);
                final int gyo = j + 1;
                svf.VrsOutn("HR_NAME1", gyo, hrClass._hrNameabbv); // クラス名
            }

            final List<Subclass> subclassList = getSubclassList(_param, studentList, false);
            // 平均の平均
//            final Map hrAverages = new HashMap();
//            for (int i = 0; i < subclassList.size(); i++) {
//                final Subclass subclass = (Subclass) subclassList.get(i);
//
//                for (int j = 0; j < pageHrClassLit.size(); j++) {
//                    final HrClass hrClass = pageHrClassLit.get(j);
//                    final AverageDat avgDat = (AverageDat) averageDatMap.get(AVG_DIV_HR_CLASS + subclass._subclasscd + hrClass._grade + hrClass._hrClass);
//                    if (null != avgDat && null != avgDat._avg) {
//                        if (null == hrAverages.get(hrClass.getCode())) {
//                            hrAverages.put(hrClass.getCode(), new ArrayList());
//                        }
//                        ((List) hrAverages.get(hrClass.getCode())).add(avgDat._avg);
//                    }
//                }
//            }
//            for (int j = 0; j < pageHrClassLit.size(); j++) {
//                final HrClass hrClass = pageHrClassLit.get(j);
//                final int gyo = j + 1;
//                final List averages = (List) hrAverages.get(hrClass.getCode());
//                if (null != averages) {
//                    svf.VrsOut("TOTAL" + gyo, getAvg(averages)); // 平均の平均
//                }
//            }
            for (int j = 0; j < pageHrClassLit.size(); j++) {
                final HrClass hrClass = pageHrClassLit.get(j);
                final int gyo = j + 1;
                svf.VrsOut("TOTAL" + gyo, hrClass._avgAvg); // 平均の平均
            }

            // 科目毎のレコード
            int consume = 0;
            for (int i = 0; i < subclassList.size(); i++) {
                final Subclass subclass = subclassList.get(i);

                final Map courseMap;
                if ("1".equals(_param._dosubunpuCourse)) {
                    courseMap = subclass._courseCreditsMap;
                } else {
                    courseMap = subclass.getCreditCourseCollectionMap(null);
                }

                final List courseMapEntrySetList = new ArrayList(courseMap.entrySet());
                if ("1".equals(_param._dosubunpuCourse)) {
                    // Map.Entry#getKeyがコース
                    Collections.sort(courseMapEntrySetList, new CompareByHrClass(_param, hrClassList));
                }
                for (final Iterator itc = courseMapEntrySetList.iterator(); itc.hasNext();) {
                    final Map.Entry e = (Map.Entry) itc.next();
                    final Collection courses;
                    final Object credit;
                    if ("1".equals(_param._dosubunpuCourse)) {
                        final String course = (String) e.getKey();
                        credit = e.getValue();
                        courses = Collections.singleton(course);
                    } else {
                        credit = e.getKey();
                        courses = (Collection) e.getValue();
                    }
                    svf.VrsOut("course1", subclass._classabbv); // 教科
                    printSubclassname(svf, subclass);
                    svf.VrsOut("credit1", null == credit ? null : credit.toString()); // 単位数

                    for (int j = 0; j < pageHrClassLit.size(); j++) {
                        final HrClass hrClass = pageHrClassLit.get(j);
                        final List<Student> filteredStudentList = Student.filterCourses(_param, hrClass._studentList, courses);
                        if (0 != filteredStudentList.size()) {
                            final int gyo = j + 1;
                            svf.VrsOut("SCORE" + gyo, calcAverage(toBd(getScoreList(filteredStudentList, subclass._subclasscd)))); // 成績

//                            final AverageDat avgDat = (AverageDat) averageDatMap.get(AVG_DIV_HR_CLASS + subclass._subclasscd + hrClass._grade + hrClass._hrClass);
//                            if (null != avgDat) {
//                                final int gyo = j + 1;
//                                svf.VrsOut("SCORE" + gyo, sishaGonyu(avgDat._avg)); // 成績
//                            }

                        }
                    }

                    if (hrpi == hrPageList.size() - 1) {
                        final ScoreDistribution distAll = new ScoreDistribution();
                        distAll.addScoreList(getScoreList(Student.filterCourses(_param, studentList, courses), subclass._subclasscd));
                        final int[][] table = ScoreDistribution.getDistributionScoreTable(_param);
                        for (int j = 0; j < table.length; j++) {
                            if (null != table[j]) {
                                final int distidx = j + 1;
                                svf.VrsOut("POINT_AREA" + distidx, String.valueOf(table[j][0]) + (table[j][0] == table[j][1] ? "" : "～" + String.valueOf(table[j][1]))); // 点数
                                svf.VrsOut("POINT" + distidx, zeroToNull(distAll.getCount(table[j][0], table[j][1]))); // 点数
                            }
                        }

//                    final String key;
//                    if (!("00".equals(_param._major) || "0000".equals(_param._major))) {
//                        final TreeSet majorSet = new TreeSet();
//                        for (final Iterator it = studentList.iterator(); it.hasNext();) {
//                            final Student student = (Student) it.next();
//                            majorSet.add(student.coursecdMajorcd());
//                        }
//                        final String major = majorSet.size() > 0 ? (String) majorSet.first() : null;
//                        key = AVG_DIV_MAJOR + subclass._subclasscd + _param._grade + major + "0000";
//                    } else {
//                        key = AVG_DIV_GRADE + subclass._subclasscd + _param._grade;
//                    }
//                    final AverageDat avgDatSel = (AverageDat) averageDatMap.get(key);
//                    if (null != avgDatSel) {
//                        svf.VrsOut("AVE_CLASS", sishaGonyu(avgDatSel._avg)); //
//                        svf.VrsOut("NUM", avgDatSel._count); // 人数
//                        svf.VrsOut("MAX_SCORE", avgDatSel._highscore); // 最高点
//                        svf.VrsOut("MIN_SCORE", avgDatSel._lowscore); // 最低点
//                    }

                        String avg = null;
                        Integer avgDatCount = null;
                        Integer avgDatHighscore = new Integer(Integer.MIN_VALUE);
                        Integer avgDatLowscore = new Integer(Integer.MAX_VALUE);
//                    	if (true) {
                            final List scoreBdList = new ArrayList(distAll._scoreList);
                            Collections.sort(scoreBdList);

                            if (!scoreBdList.isEmpty()) {
                                avg = calcAverage(scoreBdList);
                                avgDatCount = new Integer(scoreBdList.size());
                                avgDatHighscore = new Integer(((BigDecimal) scoreBdList.get(scoreBdList.size() - 1)).intValue());
                                avgDatLowscore = new Integer(((BigDecimal) scoreBdList.get(0)).intValue());
                            }
//                    	} else {
//                    		Integer avgDatSum = null;
//                    		for (final Iterator it = courses.iterator(); it.hasNext();) {
//                    			final String course = (String) it.next();
//
//                    			final String key = AVG_DIV_COURSE + subclass._subclasscd + _param._grade + course;
//
//                    			final AverageDat avgDat = (AverageDat) averageDatMap.get(key);
//                    			if (null != avgDat) {
//                    				avgDatSum = !NumberUtils.isDigits(avgDat._score) ? avgDatSum : new Integer(Integer.parseInt(avgDat._score) + (null == avgDatSum ? 0 : avgDatSum.intValue()));
//                    				avgDatCount = !NumberUtils.isDigits(avgDat._count) ? avgDatCount : new Integer(Integer.parseInt(avgDat._count) + (null == avgDatCount ? 0 : avgDatCount.intValue()));
//                    				avgDatHighscore = NumberUtils.isDigits(avgDat._highscore) ? new Integer(Math.max(avgDatHighscore.intValue(), Integer.valueOf(avgDat._highscore).intValue())) : avgDatHighscore;
//                    				avgDatLowscore = NumberUtils.isDigits(avgDat._lowscore) ? new Integer(Math.min(avgDatLowscore.intValue(), Integer.valueOf(avgDat._lowscore).intValue())) : avgDatLowscore;
//                    			}
//                    		}
//                    		if (null != avgDatSum && null != avgDatCount && 0 != avgDatCount.intValue()) {
//                    			avg = new BigDecimal(avgDatSum.longValue()).divide(new BigDecimal(avgDatCount.longValue()), 1, BigDecimal.ROUND_HALF_UP).toString();
//                    		}
//                    	}
                        svf.VrsOut("AVE_CLASS", avg); //
                        svf.VrsOut("NUM", null == avgDatCount ? null : avgDatCount.toString()); // 人数
                        if (avgDatHighscore.intValue() != Integer.MIN_VALUE) {
                            svf.VrsOut("MAX_SCORE", avgDatHighscore.toString()); // 最高点
                        }
                        if (avgDatLowscore.intValue() != Integer.MAX_VALUE) {
                            svf.VrsOut("MIN_SCORE", avgDatLowscore.toString()); // 最低点
                        }
                        if (null != allTuishidoScoreSubclassMap.get(subclass._subclasscd)) {
                            final List list = (List) allTuishidoScoreSubclassMap.get(subclass._subclasscd);
                            final int count = Student.filterCourses(_param, list, courses).size();
                            if (count > 0) {
                                svf.VrsOut("FAIL_STD", String.valueOf(count)); // 欠点者数
                            }
                        }
                    }
                    consume += 1;
                    svf.VrEndRecord();
                }
            }
            for (int i = (consume > 0 && consume % maxSubclassLine == 0 ? maxSubclassLine : consume <= maxSubclassLine ? consume : consume % maxSubclassLine); i < maxSubclassLine; i++) {
                svf.VrEndRecord();
            }
            if ("2".equals(_param._knjd234vPattern)) {
                // HR別平均点は印字無しのため1ページのみ出力
                break;
            }

        }
    }

    private static class CompareByHrClass implements Comparator {
        final Param _param;
        final Map _courseHrClass;
        public CompareByHrClass(final Param param, final List<HrClass> hrClassList) {
            _param = param;
            _courseHrClass = new HashMap();
            for (final HrClass hr : hrClassList) {
                for (final Student student : hr._studentList) {
                    if (!_courseHrClass.containsKey(student.course())) {
                        _courseHrClass.put(student.course(), hr._grade + hr._hrClass);
                    }
                }
            }
        }
        public int compare(final Object o1, final Object o2) {
            final Map.Entry e1 = (Map.Entry) o1;
            final Map.Entry e2 = (Map.Entry) o2;
            final String course1 = (String) e1.getKey();
            final String course2 = (String) e2.getKey();
            final String gradeHrclass1 = StringUtils.defaultString((String) _courseHrClass.get(course1), "99999");
            final String gradeHrclass2 = StringUtils.defaultString((String) _courseHrClass.get(course2), "99999");
            return gradeHrclass1.compareTo(gradeHrclass2);
        }
    }

//        log.info(debugString + " elems (" + elems.size() + ", avg = " + avg + ") = " + elems);
//		return avg;
//    }

    private static BigDecimal sum(final List<BigDecimal> elems) {
        if (0 == elems.size()) return null;
        BigDecimal sum = new BigDecimal(0);
        for (final BigDecimal e : elems) {
            sum = sum.add(e);
        }
        log.debug(" elems = " + elems);
        return sum;
    }

    private static String calcAverage(final List<BigDecimal> elems) {
        if (0 == elems.size()) return null;
        BigDecimal sum = sum(elems);
        log.debug(" elems = " + elems);
        return sum.divide(new BigDecimal(elems.size()), 1, BigDecimal.ROUND_HALF_UP).toString();
    }

    private void printSubclassname(final Vrw32alp svf, final Subclass subclass) {
        if (StringUtils.defaultString(subclass._subclassname).length() <= 12) {
            svf.VrsOut("SUBCLASS", subclass._subclassname); // 科目
        } else {
            svf.VrsOut("SUBCLASS_1", subclass._subclassname.substring(0, 12)); // 科目
            svf.VrsOut("SUBCLASS_2", subclass._subclassname.substring(12)); // 科目
        }
    }

    // 追指導
    private void printTuishido(final Vrw32alp svf, final int bango, final List<Student> studentList) {
        final String form = "KNJD234V_3A.frm";

        final List<Student> tuishidoStudentListAll = new ArrayList<Student>();
        for (final Student student : studentList) {
            if ("1".equals(_param._kyugaku) && student.isKyugaku(2)) {
                continue;
            }
//            if (SIDOU_INF_SCORE.equals(_param._testItem._sidouInputInf)) {
                if (student.getKettenSubclassList(_param).size() >= _param._shidouKamokusuInf2) {
                    tuishidoStudentListAll.add(student);
                }
//            } else if (SIDOU_INF_MARK.equals(_param._testItem._sidouInputInf)) {
//                if (student.getTuishidoSubclassList(_param).size() >= _param._shidouKamokusuInf1) {
//                    tuishidoStudentList.add(student);
//                }
//            }
        }

        final List<List<Student>> pageList = getPageList(tuishidoStudentListAll, 40);
        for (int pi = 0; pi < Math.max(1, pageList.size()); pi++) {
            final List<Student> tuishidoStudentList = pi < pageList.size() ? pageList.get(pi) : Collections.EMPTY_LIST;

            setForm(svf, form, 4);
//          if (SIDOU_INF_SCORE.equals(_param._testItem._sidouInputInf)) {
              svf.VrsOut("ADD_LEAD",  zenkaku(bango) + "．追指導を要する生徒(" + _param._shidouTensuInf2 + "点以下科目" + _param._shidouKamokusuInf2 + "以上を有する生徒)");
//          } else if (SIDOU_INF_MARK.equals(_param._testItem._sidouInputInf)) {
//              svf.VrsOut("ADD_LEAD", zenkaku(bango) + "．追指導を要する生徒(指導" + StringUtils.defaultString(_param._sidouHyoji) + "の科目数　" + _param._shidouKamokusuInf1 + "以上)");
//          } else {
//              log.debug(" sidouInputInf = " + _param._testItem._sidouInputInf);
//          }
            int rankCol;
            rankCol = 1;
            if (_param._shidouHrRank) {
                svf.VrsOut("RANK_NAME" + (rankCol++), "学級順位");
            }
            if (_param._shidouCourseRank) {
                svf.VrsOut("RANK_NAME" + (rankCol++), "コース順位");
            }
            if (_param._shidouMajorRank) {
                svf.VrsOut("RANK_NAME" + (rankCol++), "学科順位");
            }
            if (_param._shidouGradeRank) {
                svf.VrsOut("RANK_NAME" + (rankCol++), "学年順位");
            }
            if (null != _param._beforeTestItem) {
                svf.VrsOut("RANK_NAME5", StringUtils.defaultString(_param._beforeTestItem.getSemesterTestitemname(), "前定期考査") + "順位");
            }
            svf.VrsOut("REMARK_TITLE", _param.getRemarkTitle());

            for (int j = 0; j < tuishidoStudentList.size(); j++) {
                final Student student = tuishidoStudentList.get(j);
                int gyo = j + 1;
                svf.VrsOutn("HR_NO", gyo, student.getHrNameabbvAttendnoCd(_param)); // 年組番号
                svf.VrsOutn("SCHREG_NO", gyo, student._schregno); // 学籍番号
                svf.VrsOut("name" + gyo, student._name); // 氏名
                if (null != student._subclassScore999999) {
                    svf.VrsOut("TOTAL" + gyo, student._subclassScore999999._score); // 総合点
                    svf.VrsOut("AVERAGE" + gyo, sishaGonyu(student._subclassScore999999._avg)); // 平均点
                    rankCol = 1;
                    if (_param._shidouHrRank) {
                        svf.VrsOutn("GRADE_RANK" + gyo, rankCol++, student._subclassScore999999.getRank(OUTPUTRANK_HR, _param)); // 学級順位
                    }
                    if (_param._shidouCourseRank) {
                        svf.VrsOutn("GRADE_RANK" + gyo, rankCol++, student._subclassScore999999.getRank(OUTPUTRANK_COURSE, _param)); // コース順位
                    }
                    if (_param._shidouMajorRank) {
                        svf.VrsOutn("GRADE_RANK" + gyo, rankCol++, student._subclassScore999999.getRank(OUTPUTRANK_MAJOR, _param)); // 学科順位
                    }
                    if (_param._shidouGradeRank) {
                        svf.VrsOutn("GRADE_RANK" + gyo, rankCol++, student._subclassScore999999.getRank(OUTPUTRANK_GRADE, _param)); // 学年順位
                    }
                }
                svf.VrsOut("FAIL" + gyo, String.valueOf(student.getKettenSubclassList(_param).size())); // 欠点科目数
                if (null != _param._beforeTestItem && null != student._subclassScore999999BeforeTest) {
                    svf.VrsOutn("GRADE_RANK" + gyo, 5, student._subclassScore999999BeforeTest.getRank(_param._outputRankBefore, _param)); // 前定期考査学級順位
                }
                svf.VrsOut("REMARK" + gyo + "_" + (getMS932ByteLength(student.getRemark()) > 40 ? "2" : "1"), student.getRemark()); // 備考
            }
            int consume = 0;
            final List<Subclass> subclassList;
            if (tuishidoStudentList.size() == 0) {
                subclassList = Collections.emptyList();
            } else {
                subclassList = getSubclassList(_param, studentList, false);
                for (int i = 0; i < subclassList.size(); i++) {
                    final Subclass subclass = subclassList.get(i);

                    for (final Iterator itc = subclass.getCreditCourseCollectionMap(null).entrySet().iterator(); itc.hasNext();) {
                        final Map.Entry e = (Map.Entry) itc.next();
                        final Object credit = e.getKey();
                        final Collection courses = (Collection) e.getValue();

                        svf.VrsOut("course1", subclass._classabbv); // 教科
                        printSubclassname(svf, subclass);
                        svf.VrsOut("credit1", null == credit ? null : credit.toString()); // 単位数

                        for (int j = 0; j < tuishidoStudentList.size(); j++) {
                            final Student student = tuishidoStudentList.get(j);
                            if (!courses.contains(student.course())) {
                                continue;
                            }
                            final int gyo = j + 1;
                            final String field = gyo <= 10 ? "SCORE" : "POINT";
                            final int gyo1 = gyo <= 10 ? gyo : (gyo - 10);
                            final SubclassScore subScore = student._subclassScore.get(subclass._subclasscd);
                            if (null != subScore) {
//                                if (SIDOU_INF_SCORE.equals(_param._testItem._sidouInputInf)) {
//                                    svf.VrsOut(field + gyo1, subScore._score);
//                                } else if (SIDOU_INF_MARK.equals(_param._testItem._sidouInputInf)) {
//                                    svf.VrsOut(field + gyo1, subScore._slumpMark);
//                                }
                                svf.VrsOut(field + gyo1, subScore._score);
                                if (_param.isYouTuishido(subScore._score)) {
                                    svf.VrAttribute(field + gyo1, AMIKAKE_ATTR);
                                }
                            }
                        }
                        consume += 1;
                        svf.VrEndRecord();
                    }
                }
            }
            for (int i = (consume <= 35 ? consume : consume % 35); i < 35; i++) {
                svf.VrEndRecord();
            }
        }
    }

    private void printJoui(final Vrw32alp svf, final int bango, final List<Student> studentList, final String course) {
        final StudentScoreComparator comparator = new StudentScoreComparator(_param, StudentScoreComparator.ORDER_BY_SCORE_DESC);
        final String form = "KNJD234V_4A.frm";
        final int maxLine = 40;
        final List<Student> scoreStudentListAll1 = new ArrayList<Student>(studentList);
        Collections.sort(scoreStudentListAll1, comparator);
        for (final Iterator<Student> it = scoreStudentListAll1.iterator(); it.hasNext();) {
            final Student student = it.next();
            if ("1".equals(_param._kyugaku) && student.isKyugaku(2)) {
                it.remove();
            }
        }
        final List<Student> scoreStudentListAll = take(scoreStudentListAll1, _param._yuryo);
        if (scoreStudentListAll.size() > 0) {
            // 一番最後の生徒と同じ成績の生徒は、同一の順位として表示対象とする
            final Student lastStudent = scoreStudentListAll.get(scoreStudentListAll.size() - 1);
            for (final Student s : drop(scoreStudentListAll1, _param._yuryo)) {
                if (null == lastStudent._subclassScore999999 || comparator.compareScore(lastStudent._subclassScore999999, s._subclassScore999999) != 0) {
                    break;
                }
                scoreStudentListAll.add(s);
            }
        }

        final List<List<Student>> pageList = getPageList(scoreStudentListAll, maxLine);
        for (int pi = 0; pi < Math.max(1, pageList.size()); pi++) {
            final List<Student> scoreStudentList = pi < pageList.size() ? pageList.get(pi) : Collections.EMPTY_LIST;
            final Student first = scoreStudentList.get(0);
            final String majorCoursecodeName = null == course ? "" : (" " + StringUtils.defaultString(first._majorname) + StringUtils.defaultString(first._coursecodename));
            setForm(svf, form, 4);
            svf.VrsOut("ADD_RANK", zenkaku(bango) + "．成績上位者(" + _param.getGradeTitle() + "上位者" + String.valueOf(_param._yuryo) + "位まで" + majorCoursecodeName + ")");
            int rankcol;
            rankcol = 1;
            if (_param._yuryoHrRank) {
                svf.VrsOut("RANK_NAME" + (rankcol++), "学級順位");
            }
            if (_param._yuryoCourseRank) {
                svf.VrsOut("RANK_NAME" + (rankcol++), "コース順位");
            }
            if (_param._yuryoMajorRank) {
                svf.VrsOut("RANK_NAME" + (rankcol++), "学科順位");
            }
            if (_param._yuryoGradeRank) {
                svf.VrsOut("RANK_NAME" + (rankcol++), "学年順位");
            }
            if (null != _param._beforeTestItem) {
                svf.VrsOut("RANK_NAME5", StringUtils.defaultString(_param._beforeTestItem.getSemesterTestitemname(), "前定期考査") + "順位");
            }
            svf.VrsOut("REMARK_TITLE", _param.getRemarkTitle());

            for (int j = 0; j < scoreStudentList.size(); j++) {
                final Student student = scoreStudentList.get(j);
                final int gyo = j + 1;
                svf.VrsOutn("NO", gyo, String.valueOf(pi * maxLine + gyo)); // 番号
                svf.VrsOutn("HR_NO", gyo, student.getHrNameabbvAttendnoCd(_param)); // 年組番号
                svf.VrsOutn("SCHREG_NO", gyo, student._schregno); // 学籍番号
                svf.VrsOut("name" + gyo, student._name); // 氏名
                if (null != student._subclassScore999999) {
                    svf.VrsOut("TOTAL" + gyo, student._subclassScore999999._score); // 総合点
                    svf.VrsOut("AVERAGE" + gyo, sishaGonyu(student._subclassScore999999._avg)); // 平均点
                    rankcol = 1;
                    if (_param._yuryoHrRank) {
                        svf.VrsOutn("GRADE_RANK" + gyo, rankcol++, student._subclassScore999999.getRank(OUTPUTRANK_HR, _param)); // 学級順位
                    }
                    if (_param._yuryoCourseRank) {
                        svf.VrsOutn("GRADE_RANK" + gyo, rankcol++, student._subclassScore999999.getRank(OUTPUTRANK_COURSE, _param)); // コース順位
                    }
                    if (_param._yuryoMajorRank) {
                        svf.VrsOutn("GRADE_RANK" + gyo, rankcol++, student._subclassScore999999.getRank(OUTPUTRANK_MAJOR, _param)); // 学科順位
                    }
                    if (_param._yuryoGradeRank) {
                        svf.VrsOutn("GRADE_RANK" + gyo, rankcol++, student._subclassScore999999.getRank(OUTPUTRANK_GRADE, _param)); // 学年順位
                    }
                }
                svf.VrsOut("FAIL" + gyo, String.valueOf(student.getKettenSubclassList(_param).size())); // 欠点科目数
                if (null != _param._beforeTestItem && null != student._subclassScore999999BeforeTest) {
                    svf.VrsOutn("GRADE_RANK" + gyo, 5, student._subclassScore999999BeforeTest.getRank(_param._outputRankBefore, _param)); // 前定期考査学級順位
                }
                svf.VrsOut("REMARK" + gyo + "_" + (getMS932ByteLength(student.getRemark()) > 40 ? "2" : "1"), student.getRemark()); // 備考
            }

            final List<Subclass> subclassList;
            int consume = 0;
            if (scoreStudentList.size() == 0) {
                subclassList = Collections.EMPTY_LIST;
            } else {
                subclassList = getSubclassList(_param, studentList, false);
                for (int i = 0; i < subclassList.size(); i++) {
                    final Subclass subclass = subclassList.get(i);

//                    if ("J".equals(_param._schoolKind)) {
//
//                		svf.VrsOut("course1", subclass._classabbv); // 教科
//                		printSubclassname(svf, subclass);
//
//                		for (int j = 0; j < scoreStudentList.size(); j++) {
//                			final Student student = (Student) scoreStudentList.get(j);
//                			final int gyo = j + 1;
//                			final String field = gyo <= 10 ? "SCORE" : "POINT";
//                			final int gyo1 = gyo <= 10 ? gyo : (gyo - 10);
//                			final String score = student.getScore(subclass._subclasscd);
//                			svf.VrsOut(field + gyo1, score); // 成績
//                			if (_param.isYouTuishido(score)) {
//                				svf.VrAttribute(field + gyo1, AMIKAKE_ATTR);
//                			}
//                		}
//                		svf.VrEndRecord();
//                		consume += 1;
//
//                    } else {
                        for (final Iterator itc = subclass.getCreditCourseCollectionMap(course).entrySet().iterator(); itc.hasNext();) {
                            final Map.Entry e = (Map.Entry) itc.next();
                            final Object credit = e.getKey();
                            final Collection courses = (Collection) e.getValue();

                            svf.VrsOut("course1", subclass._classabbv); // 教科
                            printSubclassname(svf, subclass);
                            svf.VrsOut("credit1", null == credit ? null : credit.toString()); // 単位数

                            for (int j = 0; j < scoreStudentList.size(); j++) {
                                final Student student = scoreStudentList.get(j);
                                if (!courses.contains(student.course())) {
                                    continue;
                                }
                                final int gyo = j + 1;
                                final String field = gyo <= 10 ? "SCORE" : "POINT";
                                final int gyo1 = gyo <= 10 ? gyo : (gyo - 10);
                                final String score = student.getScore(subclass._subclasscd);
                                svf.VrsOut(field + gyo1, score); // 成績
                                if (_param.isYouTuishido(score)) {
                                    svf.VrAttribute(field + gyo1, AMIKAKE_ATTR);
                                }
                            }
                            svf.VrEndRecord();
                            consume += 1;
                        }
//                    }

                }
            }
            for (int i = (consume <= 35 ? consume : consume % 35); i < 35; i++) {
                svf.VrEndRecord();
            }
        }
    }

    private void printKai(final Vrw32alp svf, final int bango, final List<Student> studentList, final String course, final String majornameCoursecodename) {
        final String form = "KNJD234V_5A.frm";
        final int maxStudentLine = 40;
        final int maxSubclassLine = 35;

        final List<Student> scoreStudentListAll = getKaiScoreStudentListAll(studentList);

        final List<List<Student>> pageList = getPageList(scoreStudentListAll, maxStudentLine);
        log.info(" kai student page " + pageList.size());
        for (int pi = 0; pi < Math.max(1, pageList.size()); pi++) {
            final List<Student> scoreStudentList = pi < pageList.size() ? (List) pageList.get(pi) : Collections.EMPTY_LIST;
            final String majorCoursecodeName = null == course ? "" : (" " + majornameCoursecodename);
            setForm(svf, form, 4);
            final String title;
            if ("2".equals(_param._fushinDiv)) {
                title = zenkaku(bango) + "．成績下位者(" + _param.getGradeTitle() + "欠点科目数" + String.valueOf(_param._kettenCount) + "以上" + majorCoursecodeName + ")";
            } else {
                title = zenkaku(bango) + "．成績下位者(" + _param.getGradeTitle() + "下位者" + String.valueOf(_param._fushin) + "位まで" + majorCoursecodeName + ")";
            }
            svf.VrsOut("ADD_RANK", title);
            int rankcol;
            rankcol = 1;
            if (_param._fushinHrRank) {
                svf.VrsOut("RANK_NAME" + (rankcol++), "学級順位");
            }
            if (_param._fushinCourseRank) {
                svf.VrsOut("RANK_NAME" + (rankcol++), "コース順位");
            }
            if (_param._fushinMajorRank) {
                svf.VrsOut("RANK_NAME" + (rankcol++), "学科順位");
            }
            if (_param._fushinGradeRank) {
                svf.VrsOut("RANK_NAME" + (rankcol++), "学年順位");
            }
            if (null != _param._beforeTestItem) {
                svf.VrsOut("RANK_NAME5", StringUtils.defaultString(_param._beforeTestItem.getSemesterTestitemname(), "前定期考査") + "順位");
            }
            svf.VrsOut("REMARK_TITLE", _param.getRemarkTitle());

            for (int j = 0; j < scoreStudentList.size(); j++) {
                final Student student = scoreStudentList.get(j);
                final int gyo = j + 1;
                svf.VrsOutn("NO", gyo, String.valueOf(pi * maxStudentLine + gyo)); // 番号
                svf.VrsOutn("HR_NO", gyo, student.getHrNameabbvAttendnoCd(_param)); // 年組番号
                svf.VrsOutn("CHREG_NO", gyo, student._schregno); // 学籍番号
                svf.VrsOut("name" + gyo, student._name); // 氏名
                if (null != student._subclassScore999999) {
                    svf.VrsOut("TOTAL" + gyo, student._subclassScore999999._score); // 総合点
                    svf.VrsOut("AVERAGE" + gyo, sishaGonyu(student._subclassScore999999._avg)); // 平均点
                    rankcol = 1;
                    if (_param._fushinHrRank) {
                        svf.VrsOutn("GRADE_RANK" + gyo, rankcol++, student._subclassScore999999.getRank(OUTPUTRANK_HR, _param)); // 学級順位
                    }
                    if (_param._fushinCourseRank) {
                        svf.VrsOutn("GRADE_RANK" + gyo, rankcol++, student._subclassScore999999.getRank(OUTPUTRANK_COURSE, _param)); // コース順位
                    }
                    if (_param._fushinMajorRank) {
                        svf.VrsOutn("GRADE_RANK" + gyo, rankcol++, student._subclassScore999999.getRank(OUTPUTRANK_MAJOR, _param)); // 学科順位
                    }
                    if (_param._fushinGradeRank) {
                        svf.VrsOutn("GRADE_RANK" + gyo, rankcol++, student._subclassScore999999.getRank(OUTPUTRANK_GRADE, _param)); // 学年順位
                    }
                }
                svf.VrsOut("FAIL" + gyo, String.valueOf(student.getKettenSubclassList(_param).size())); // 欠点科目数
                if (null != _param._beforeTestItem && null != student._subclassScore999999BeforeTest) {
                    svf.VrsOutn("GRADE_RANK" + gyo, 5, student._subclassScore999999BeforeTest.getRank(_param._outputRankBefore, _param)); // 前定期考査学級順位
                }
                svf.VrsOut("REMARK" + gyo + "_" + (getMS932ByteLength(student.getRemark()) > 40 ? "2" : "1"), student.getRemark()); // 備考
            }

            final List<Subclass> subclassList;
            int consume = 0;
            log.info(" kai scoreStudentList size " + scoreStudentList.size());
            if (scoreStudentList.size() == 0) {
                subclassList = Collections.EMPTY_LIST;
            } else {
                subclassList = getSubclassList(_param, scoreStudentListAll, false);
                for (int i = 0; i < subclassList.size(); i++) {
                    final Subclass subclass = subclassList.get(i);

                    for (final Iterator itc = subclass.getCreditCourseCollectionMap(course).entrySet().iterator(); itc.hasNext();) {
                        final Map.Entry e = (Map.Entry) itc.next();
                        final Object credit = e.getKey();
                        final Collection courses = (Collection) e.getValue();

                        svf.VrsOut("course1", subclass._classabbv); // 教科
                        printSubclassname(svf, subclass);
                        svf.VrsOut("credit1", null == credit ? null : credit.toString()); // 単位数

                        for (int j = 0; j < scoreStudentList.size(); j++) {
                            final Student student = scoreStudentList.get(j);
                            if (!courses.contains(student.course())) {
                                continue;
                            }
                            final int gyo = j + 1;
                            final String field = gyo <= 10 ? "SCORE" : "POINT";
                            final int gyo1 = gyo <= 10 ? gyo : (gyo - 10);
                            final String score = student.getScore(subclass._subclasscd);
                            svf.VrsOut(field + gyo1, score); // 成績
                            if (_param.isYouTuishido(score)) {
                                svf.VrAttribute(field + gyo1, AMIKAKE_ATTR);
                            }
                        }
                        consume += 1;
                        svf.VrEndRecord();
                    }
                }
            }
            for (int i = (consume <= maxSubclassLine ? consume : consume % maxSubclassLine); i < maxSubclassLine; i++) {
                svf.VrEndRecord();
            }
        }
    }

    private List<Student> getKaiScoreStudentListAll(final List<Student> studentList) {
        List<Student> scoreStudentListAll = new ArrayList<Student>(studentList);
        if ("1".equals(_param._kesshiNozoku)) {
            for (final Iterator<Student> it = scoreStudentListAll.iterator(); it.hasNext();) {
                final Student student = it.next();
                if (student._subclassScore999999 == null ||  student._subclassScore999999._gradeRank == null) {
                    it.remove();
                }
            }
        }
        for (final Iterator<Student> it = scoreStudentListAll.iterator(); it.hasNext();) {
            final Student student = it.next();
            if ("1".equals(_param._kyugaku) && student.isKyugaku(2)) {
                it.remove();
            }
        }
        final StudentScoreComparator studentOrderByScoreAsc = new StudentScoreComparator(_param, StudentScoreComparator.ORDER_BY_SCORE_ASC);
        if ("2".equals(_param._fushinDiv)) {
            for (final Iterator<Student> it = scoreStudentListAll.iterator(); it.hasNext();) {
                final Student student = it.next();
                if (student.getKettenSubclassList(_param).size() < _param._kettenCount) {
                    it.remove();
                }
            }
            Collections.sort(scoreStudentListAll, studentOrderByScoreAsc);
        } else {
            Collections.sort(scoreStudentListAll, studentOrderByScoreAsc);
            scoreStudentListAll = take(scoreStudentListAll, _param._fushin);
        }
        return scoreStudentListAll;
    }

    private void printShukketsuIjou(final Vrw32alp svf, final int bango, final List<Student> studentList) {
        final String form = "KNJD234V_6A.frm";

        String text;
        if (_param._isNaraken) {
            text =  zenkaku(bango) + "．出欠が条件に該当する者（欠席日数が出席すべき日数の" + String.valueOf(_param._nissuuBunshi) + "/" + String.valueOf(_param._nissuBunbo) + "を超える者、";
        }else {
            text =  zenkaku(bango) + "．出席の正常でない者（欠席日数が出席すべき日数の" + String.valueOf(_param._nissuuBunshi) + "/" + String.valueOf(_param._nissuBunbo) + "を超える者、";
        }

        if (_param._useHouteiKekkaJisu) {
            text += "及び欠課時数が法定時間の" + String.valueOf(_param._jisuuBunshi) + "/" + String.valueOf(_param._jisuBunbo) + "を超える科目を有する者とその科目）";
        } else {
            text += "及び欠課時数が実時数の" + String.valueOf(_param._jisuuBunshi) + "/" + String.valueOf(_param._jisuBunbo) + "を超える科目を有する者とその科目）";
        }

        final List<Student> attendOverStudentListAll = getAttendSubclassOverStudentList(_param, studentList);
        final int maxLine = 20;
        final List<List<Student>> pageList = getPageList(attendOverStudentListAll, maxLine);

        for (int pi = 0; pi < Math.max(1, pageList.size()); pi++) {
            setForm(svf, form, 4);
            svf.VrsOut("ATTEND_ABNORMAL", text);
            final List<Student> attendOverStudentList = pi < pageList.size() ? pageList.get(pi) : Collections.EMPTY_LIST;

            for (int j = 0; j < attendOverStudentList.size(); j++) {
                final Student student = attendOverStudentList.get(j);
                final int gyo = j + 1;
                svf.VrsOutn("NO", gyo, String.valueOf(maxLine * pi + j + 1)); // 連番
                svf.VrsOutn("HR_NO", gyo, student.getHrNameabbvAttendnoCd(_param)); // 年組番号
                svf.VrsOutn("SCHREG_NO", gyo, student._schregno); // 学籍番号
                svf.VrsOut("name" + gyo, student._name); // 氏名
                svf.VrsOut("ABSENT_UP" + gyo, String.valueOf(student._attendance._absence)); // 欠席日数
                svf.VrsOut("ABSENT_DOWN" + gyo, String.valueOf(student._attendance._mlesson)); // 欠席日数
                if  (student._attendance.attendOver(_param)) {
                    svf.VrAttribute("ABSENT_UP" + gyo, AMIKAKE_ATTR);
                    svf.VrAttribute("ABSENT_DOWN" + gyo, AMIKAKE_ATTR);
                }
                svf.VrsOut("REMARK" + gyo + "_1", student.getSpecialReasonTestDatRemark(D056_03 + D056_SUBCLASSCD_ZERO)); // 備考
            }

            final List<Subclass> subclassList;
            int consume = 0;
            if (attendOverStudentList.size() == 0) {
                subclassList = Collections.EMPTY_LIST;
            } else {
                subclassList = getSubclassList(_param, studentList, true);
                for (int i = 0; i < subclassList.size(); i++) {
                    final Subclass subclass = subclassList.get(i);

                    for (final Iterator itc = subclass.getCreditCourseCollectionMap(null).entrySet().iterator(); itc.hasNext();) {
                        final Map.Entry e = (Map.Entry) itc.next();
                        final Object credit = e.getKey();
                        final Collection courses = (Collection) e.getValue();

                        svf.VrsOut("course1", subclass._classabbv); // 教科
                        printSubclassname(svf, subclass);
                        svf.VrsOut("credit1", null == credit ? null : credit.toString()); // 単位数

                        for (int j = 0; j < attendOverStudentList.size(); j++) {
                            final Student student = attendOverStudentList.get(j);
                            if (!courses.contains(student.course())) {
                                continue;
                            }
                            final String field = j < 5 ? "SCORE" : "POINT";
                            final int gyo1 = j < 5 ? j : (j - 5);
                            final SubclassAttendance subatt = student._subclassAttendance.get(subclass._subclasscd);
                            if (null != subatt) {
                                final String data = String.valueOf(subatt._sick.intValue() + "/" + subatt.jugyouJisu(_param));
                                final String attField = field + (gyo1 * 2 + 1) + (data.length() > 5 ? "_2" : "");
                                svf.VrsOut(attField, data); // 成績
                                if (subatt.isAttendOver(_param)) {
                                    svf.VrAttribute(attField, AMIKAKE_ATTR); // 成績
                                }
                            }
                            svf.VrsOut(field + (gyo1 * 2 + 2), student.getScore(subclass._subclasscd)); // 成績
                        }
                        consume += 1;
                        svf.VrEndRecord();
                    }
                }
            }
            for (int i = (consume <= 40 ? consume : consume % 40); i < 40; i++) {
                svf.VrEndRecord();
            }
        }
    }

    private void printSeitobetsuTsusanKettenKamokusu(final Vrw32alp svf, final int bango, final List<Student> studentList) {

        final int max = 50;
        final List<SubclassScore> printList = new ArrayList<SubclassScore>();
        for (final Student student : studentList) {
            for (final String testcd : student._tsusanKettenKamokuListMap.keySet()) {
                final List<SubclassScore> kettenKamokuList = getMappedList(student._tsusanKettenKamokuListMap, testcd);
                printList.addAll(kettenKamokuList);
            }
        }
        if (printList.isEmpty()) {
            printList.add(null); // ページを印字するダミー
        }

        final List<List<SubclassScore>> pageList = getPageList(printList, max);
        for (int pi = 0; pi < pageList.size(); pi++) {
            final List<SubclassScore> list = pageList.get(pi);

            final String form = "KNJD234V_7.frm";
            setForm(svf, form, 4);

            svf.VrsOut("ADD_LEAD", zenkaku(bango) + "．生徒別通算欠点科目数（" + String.valueOf(_param._shidouTensuInf2) + "点以下科目を有する者）");

            svf.VrsOut("DIV_NAME", StringUtils.defaultString(StringUtils.defaultString(_param._d053Map.get("01"), _param._d053Map.get("08")))); // 評価名称

            for (int i = 0; i < list.size(); i++) {
                final SubclassScore subScore = list.get(i);
                if (null == subScore) {
                    continue;
                }
                final TestItem testItem = subScore._testItem;

                svf.VrsOut("HR_NO", subScore._student.getGradeHrclassAttendnoCd()); // 年組番号
                svf.VrsOut("NAME", subScore._student._name); // 氏名
                svf.VrsOut("NENDO", testItem._year); // 年度
                svf.VrsOut("SEMESTER", testItem._semester); // 学期
                svf.VrsOut("TEST_NAME", testItem._testitemname); // 考査名
                svf.VrsOut("SUBCLASS", subScore._subclass._subclassname); // 科目
                svf.VrsOut("DIV", subScore._score); // 評価
                svf.VrEndRecord();
            }
            for (int i = list.size(); i < max; i++) {
                svf.VrsOut("HR_NO", String.valueOf(i)); // 年組番号
                svf.VrAttribute("HR_NO", "X=10000"); // 氏名
                svf.VrsOut("NAME", String.valueOf(i)); // 氏名
                svf.VrAttribute("NAME", "X=10000"); // 氏名
                svf.VrEndRecord();
            }
        }
    }

    private List print1234_Csv(final DB2UDB db2, final List<HrClass> hrClassList, final List<Student> studentList) {
        final List lines = new ArrayList();

        final List<Student> zaisekiList = HrClass.getZaisekiList(studentList, null, _param, _param._edate);
        if ("2".equals(_param._knjd234vPattern)) {
            // 1.在籍
            lines.addAll(printB1_Csv(db2, studentList));

            // 2.異動一覧
            lines.addAll(printB2_Csv(db2, studentList));

            // 3.成績がつかなかった者
            lines.addAll(print3_Csv(getPrint3OutputList(zaisekiList)));

            // 4.欠席・遅刻・早退の多い者
            if ("1".equals(_param._outputShukketsu)) {
                lines.addAll(print4_Csv(getAttendOverStudentList(_param, zaisekiList)));
            }

        } else {
            final List<Student> attendOverStudentListAll = "1".equals(_param._outputShukketsu) ? getAttendOverStudentList(_param, zaisekiList) : Collections.EMPTY_LIST;

            // 1.在籍
            lines.addAll(print1_Csv(db2, hrClassList));

            // 2.組別平均点度数分布
            if (!"3".equals(_param._knjd234vPattern)) {
                lines.addAll(print2_Csv(hrClassList));
            }

            // 3.成績がつかなかった者
            lines.addAll(print3_Csv(getPrint3OutputList(zaisekiList)));

            if ("1".equals(_param._outputShukketsu)) {
                // 4.欠席・遅刻・早退の多い者
                lines.addAll(print4_Csv(attendOverStudentListAll));
            }
        }
        return lines;
    }

    private List print1_Csv(final DB2UDB db2, final List<HrClass> hrClassList) {
        final List lines = new ArrayList();

        newLine(lines).addAll(Arrays.asList("１．在籍(" + _param._edateStr + "現在)"));
        newLine(lines).addAll(Arrays.asList("", _param._nendo + "年度","","", "現在","","", "備考(在籍異動者／異動日付／事由等)"));
        newLine(lines).addAll(Arrays.asList("クラス＼人数", "男","女","計", "男","女","計"));

        int[] totalmaleCount = new int[] {0, 0};
        int[] totalfemaleCount = new int[] {0, 0};
        int[] totalCount = new int[] {0, 0};
        int[] totalmaleRyugakuKyugakuCount = new int[] {0, 0};
        int[] totalfemaleRyugakuKyugakuCount = new int[] {0, 0};
        int[] totalRyugakuKyugakuCount = new int[] {0, 0};
        int flg;
        for (int j = 0; j < hrClassList.size(); j++) {
            final HrClass hrClass = hrClassList.get(j);

            final List line = newLine(lines);

            final int maleCount = HrClass.getZaisekiList(hrClass._studentList, SEX1, _param, _param._yearSdate).size();
            final int femaleCount = HrClass.getZaisekiList(hrClass._studentList, SEX2, _param, _param._yearSdate).size();
            final int count = HrClass.getZaisekiList(hrClass._studentList, null, _param, _param._yearSdate).size();
            line.add(hrClass._hrNameabbv);          // クラス名
            line.add(String.valueOf(maleCount));   // 在籍男
            line.add(String.valueOf(femaleCount)); // 在籍女
            line.add(String.valueOf(count));       // 在籍男女
            totalmaleCount[0] += maleCount;
            totalfemaleCount[0] += femaleCount;
            totalCount[0] += count;
            flg = 1; // 年度開始日
            totalmaleRyugakuKyugakuCount[0] += HrClass.getStudentCountRyugakuKyugakuSex(hrClass._studentList, flg, SEX1).size();
            totalfemaleRyugakuKyugakuCount[0] += HrClass.getStudentCountRyugakuKyugakuSex(hrClass._studentList, flg, SEX2).size();
            totalRyugakuKyugakuCount[0] += HrClass.getStudentCountRyugakuKyugakuSex(hrClass._studentList, flg, SEX1).size() + HrClass.getStudentCountRyugakuKyugakuSex(hrClass._studentList, flg, SEX2).size();

            //X月現在
            final int maleCount2 = HrClass.getZaisekiList(hrClass._studentList, SEX1, _param, _param._edate).size();
            final int femaleCount2 = HrClass.getZaisekiList(hrClass._studentList, SEX2, _param, _param._edate).size();
            final int count2 = HrClass.getZaisekiList(hrClass._studentList, null, _param, _param._edate).size();
            line.add(String.valueOf(maleCount2));   // 在籍男
            line.add(String.valueOf(femaleCount2)); // 在籍女
            line.add(String.valueOf(count2));       // 在籍男女
            totalmaleCount[1] += maleCount2;
            totalfemaleCount[1] += femaleCount2;
            totalCount[1] += count2;
            flg = 2; // 指定日付
            totalmaleRyugakuKyugakuCount[1] += HrClass.getStudentCountRyugakuKyugakuSex(hrClass._studentList, flg, SEX1).size();
            totalfemaleRyugakuKyugakuCount[1] += HrClass.getStudentCountRyugakuKyugakuSex(hrClass._studentList, flg, SEX2).size();
            totalRyugakuKyugakuCount[1] += HrClass.getStudentCountRyugakuKyugakuSex(hrClass._studentList, flg, SEX1).size() + HrClass.getStudentCountRyugakuKyugakuSex(hrClass._studentList, flg, SEX2).size();

            final String biko = hrClass.getIdouBiko(db2, _param);
            line.add(biko);       // 備考
        }
        final List line2 = newLine(lines);
        line2.add("小計");          // クラス名
        line2.add(String.valueOf(totalmaleCount[0]) + "(" + String.valueOf(totalmaleRyugakuKyugakuCount[0]) + ")");     // 在籍男
        line2.add(String.valueOf(totalfemaleCount[0]) + "(" + String.valueOf(totalfemaleRyugakuKyugakuCount[0]) + ")"); // 在籍女
        line2.add(String.valueOf(totalCount[0]) + "(" + String.valueOf(totalRyugakuKyugakuCount[0]) + ")");             // 在籍男女
        line2.add(String.valueOf(totalmaleCount[1]) + "(" + String.valueOf(totalmaleRyugakuKyugakuCount[1]) + ")");     // 在籍男
        line2.add(String.valueOf(totalfemaleCount[1]) + "(" + String.valueOf(totalfemaleRyugakuKyugakuCount[1]) + ")"); // 在籍女
        line2.add(String.valueOf(totalCount[1]) + "(" + String.valueOf(totalRyugakuKyugakuCount[1]) + ")");             // 在籍男女
        final String setTransferComment = ("".equals(_param._transferComment) || null == _param._transferComment) ? "※()内は留学、休学者数": _param._transferComment;
        newLine(lines).addAll(Arrays.asList(new String[] {"", setTransferComment})); // 補足

        _hasData = true;

        return lines;
    }

    private List printB1_Csv(final DB2UDB db2, final List<Student> studentList) {
        final List lines = new ArrayList();

        newLine(lines).addAll(Arrays.asList("１．在籍数(" + _param._edateStr + "現在)"));

        List tousho = new ArrayList();
        List ryukyu0 = new ArrayList();
        List ryukyu1 = new ArrayList();
        List tenhen = new ArrayList();
        List sotu = new ArrayList();
        List tentai = new ArrayList();
        for (final Student student : studentList) {
            if (null != student._entdate) {
                if (between(student._entdate, null, _param._yearSdate) && !between(student._grddate, null, _param._yearSdate) && !student.isTenhennyuugaku(_param, _param._edate)) {
                    tousho.add(student);
                    if (null != student._transferSdate1) {
                        ryukyu0.add(student);
                    }
                } else if (student.isTenhennyuugaku(_param, _param._edate)) {
                    tenhen.add(student);
                }
            }
            if (null != student._grddate) {
                if (between(student._grddate, _param._yearSdate, _param._edate, false)) {
                    if ("1".equals(student._grddiv)) {
                        sotu.add(student);
                    } else if ("2".equals(student._grddiv) || "3".equals(student._grddiv)) {
                        tentai.add(student);
                    }
                }
            }
            if (null != student._transferSdate2) {
                ryukyu1.add(student);
            }
        }
        newLine(lines).addAll(Arrays.asList("年度当初在籍数", String.valueOf(tousho.size()) + "(" + String.valueOf(ryukyu0.size()) + ")"));
        newLine(lines).addAll(Arrays.asList("転編入者等の数", String.valueOf(tenhen.size())));
        newLine(lines).addAll(Arrays.asList("卒業者の数", String.valueOf(sotu.size())));
        newLine(lines).addAll(Arrays.asList("転退学者の数", String.valueOf(tentai.size())));
        newLine(lines).addAll(Arrays.asList("現在の在籍数", String.valueOf(tousho.size() + tenhen.size() - sotu.size() - tentai.size()) + "(" + String.valueOf(ryukyu1.size()) + ")"));
        newLine(lines).addAll(Arrays.asList("", "※()内は留学、休学者数"));

        return lines;
    }

    private List print2_Csv(final List<HrClass> hrClassList) {
        final List lines = new ArrayList();

        newLine(lines).addAll(Arrays.asList(new String[] {""}));
        newLine(lines).addAll(Arrays.asList(new String[] {"２．組別平均点度数分布"}));

        final Map studentListMapCoursecode = new HashMap();
        final TreeMap nameMapCoursecode = new TreeMap();
        final Map<String, List<Student>> studentListMapMajor = new HashMap<String, List<Student>>();
        final TreeMap nameMapMajor = new TreeMap();
        final TreeMap majorCourseMap = new TreeMap();
        for (int j = 0; j < hrClassList.size(); j++) {
            final HrClass hrClass = hrClassList.get(j);
            for (final Student student : hrClass._studentList) {
                if (null != student._coursecode && !StringUtils.isBlank(student._coursecodename)) {
                    nameMapCoursecode.put("COURSECODE:" + student._coursecode, student._coursecodename);
                    getMappedList(studentListMapCoursecode, "COURSECODE:" + student._coursecode).add(student);
                }
                if (null != student.coursecdMajorcd() && !StringUtils.isBlank(student._majorname)) {
                    nameMapMajor.put("COURSEMAJOR:" + student.coursecdMajorcd(), student._majorname);
                    getMappedList(studentListMapMajor, "COURSEMAJOR:" + student.coursecdMajorcd()).add(student);

                    // 学科とコースの関連付け
                    if (null != student._coursecode && !StringUtils.isBlank(student._coursecodename)) {
                        getMappedSet(majorCourseMap, "COURSEMAJOR:" + student.coursecdMajorcd()).add("COURSECODE:" + student._coursecode);
                    }
                }
            }
        }

        final List groupList = new ArrayList();
        final TreeMap groupStudentListMap = merge(studentListMapCoursecode, studentListMapMajor);
        final TreeMap groupNameMap = merge(nameMapCoursecode, nameMapMajor);
        log.debug(" majorCourseMap = " + majorCourseMap + ", size = " + majorCourseMap.size());
        List<String> majorCourseCodeList = new ArrayList<String>(majorCourseMap.keySet());
        if (true) {
            final Map<String, String> majorHrclassMap = new TreeMap<String, String>();
            for (final String majorcd : majorCourseCodeList) {
                final List<Student> students = studentListMapMajor.get(majorcd);
                String hrClass = "999";
                if (null != students) {
                    for (Student student : students) {
                        if (null != student._hrClass && student._hrClass.compareTo(hrClass) < 0) {
                            hrClass = student._hrClass;
                        }
                    }
                }
                majorHrclassMap.put(majorcd, hrClass);
            }
            Collections.sort(majorCourseCodeList, majorComparator(majorHrclassMap));
        }

        for (final String majorcd : majorCourseCodeList) {
            final Set coursecodes = (Set) majorCourseMap.get(majorcd);
            if (coursecodes.size() > 1) {
                // 各学科にコースが2つ以上設定されていれば、コースごとに出力する
                groupList.addAll(coursecodes);
            } else {
                // コースが設定されていなければ（「コースなし」の１コースのみとして）、学科ごとに出力する
                groupList.add(majorcd);
            }
        }

        final int[][] table = ScoreDistribution.getDistributionScoreTable(_param);
        final Map<String, String> printAreaMap = new HashMap();
        for (int ti = 0; ti < table.length; ti++) {
            if (null != table[ti]) {
                printAreaMap.put("POINT_AREA" + String.valueOf(ti + 1), String.valueOf(table[ti][0]) + (table[ti][0] == table[ti][1] ? "" : "～" + String.valueOf(table[ti][1]))); // 点数
            }
        }
        final int max = 15;
        int LINE_GRADE = 11;  // 学年
        if (groupList.size() > max - LINE_GRADE) {
            LINE_GRADE = Math.max(hrClassList.size() + 1, max - groupList.size());
        }
        final ScoreDistribution distGrade = new ScoreDistribution();
        int totalZaisekiCount = 0;
        int totalJukenCount = 0;
        int totalKessiCount = 0;
        final List<Map<String, String>> setList = new ArrayList<Map<String, String>>();
        for (int j = 0; j < hrClassList.size() && j < 10; j++) {
            final HrClass hrClass = hrClassList.get(j);
            final Map setDataMap1 = new HashMap();
            final ScoreDistribution distHrclass = new ScoreDistribution();

            final List scoreList999999 = getAvgList(hrClass._studentList);
            distHrclass.addScoreList(scoreList999999);
            distGrade.addScoreList(scoreList999999);
            final Tuple<Integer, Integer> zaisekishaAndKesshisha = getZaisekishaAndKesshisha(_param, hrClass._studentList);
            final int zaisekiSize = zaisekishaAndKesshisha._first;
            final int kessiSize = zaisekishaAndKesshisha._second;
            final String clname = hrClass._hrNameabbv;
            setDataMap1.put("HR_NAME2", clname);                                     // クラス名
            setDataMap1.put("REGD_NUM", String.valueOf(zaisekiSize));               // 在籍数
            setDataMap1.put("EXAM_NUM", String.valueOf(zaisekiSize - kessiSize));   // 受験者
            setDataMap1.put("NO_EXAM_NUM", String.valueOf(kessiSize));              // 欠試者
            for (int k = 0; k < table.length; k++) {
                if (null != table[k]) {
                    setDataMap1.put("POINT" + (k + 1), zeroToNull(distHrclass.getCount(table[k][0], table[k][1]))); // 点数
                }
            }
            totalZaisekiCount += zaisekiSize;
            totalJukenCount += zaisekiSize - kessiSize;
            totalKessiCount += kessiSize;
            setList.add(setDataMap1);
        }
        // 度数分布学年
        final String clname2 = _param.getGradeTitle();
        final Map<String, String> setDataMap2 = new HashMap<String, String>();
        setDataMap2.put("HR_NAME2", clname2);                                // クラス名
        setDataMap2.put("REGD_NUM", String.valueOf(totalZaisekiCount));     // 在籍数
        setDataMap2.put("EXAM_NUM", String.valueOf(totalJukenCount));        // 受験者
        setDataMap2.put("NO_EXAM_NUM", String.valueOf(totalKessiCount));     // 欠試者
        for (int k = 0; k < table.length; k++) {
            if (null != table[k]) {
                setDataMap2.put("POINT" + (k + 1), zeroToNull(distGrade.getCount(table[k][0], table[k][1]))); // 点数
            }
        }
        setList.add(setDataMap2);
        for (int i = 0; i < groupList.size(); i++) {
            final String groupCode = (String) groupList.get(i);
            final Map setDataMap3 = new HashMap();
            final ScoreDistribution distCourse = new ScoreDistribution();
            final List groupStudentList = (List) groupStudentListMap.get(groupCode);
            final List scoreList999999 = getAvgList(groupStudentList);
            distCourse.addScoreList(scoreList999999);
            final Tuple<Integer, Integer> zaisekishaAndKesshisha = getZaisekishaAndKesshisha(_param, groupStudentList);
            final int zaisekiSize = zaisekishaAndKesshisha._first;
            final int kessiSize = zaisekishaAndKesshisha._second;
            final String clname = (String) groupNameMap.get(groupCode);
            setDataMap3.put("HR_NAME2", clname);                                     // クラス名
            setDataMap3.put("REGD_NUM", String.valueOf(zaisekiSize));                // 在籍数
            setDataMap3.put("EXAM_NUM", String.valueOf(zaisekiSize - kessiSize));    // 受験者
            setDataMap3.put("NO_EXAM_NUM", String.valueOf(kessiSize));               // 欠試者
            for (int k = 0; k < table.length; k++) {
                if (null != table[k]) {
                    setDataMap3.put("POINT" + (k + 1), zeroToNull(distCourse.getCount(table[k][0], table[k][1]))); // 点数
                }
            }
            setList.add(setDataMap3);
        }

        // クラス名
        final List<String> line0 = newLine(lines);
        line0.add("クラス");
        for (int i = 0; i < setList.size(); i++) {
            final Map<String, String> dataMap = (Map) setList.get(i);
            line0.add(dataMap.get("HR_NAME2"));
        }

        // 在籍
        final List<String> line1 = newLine(lines);
        line1.add("在籍");
        for (int i = 0; i < setList.size(); i++) {
            final Map<String, String> dataMap = (Map) setList.get(i);
            line1.add(dataMap.get("REGD_NUM"));
        }

        // 受験者
        final List<String> line2 = newLine(lines);
        line2.add("受験者");
        for (int i = 0; i < setList.size(); i++) {
            final Map<String, String> dataMap = (Map) setList.get(i);
            line2.add(dataMap.get("EXAM_NUM"));
        }

        // 欠査者
        final List<String> line3 = newLine(lines);
        line3.add("欠査者");
        for (int i = 0; i < setList.size(); i++) {
            final Map<String, String> dataMap = (Map) setList.get(i);
            line3.add(dataMap.get("NO_EXAM_NUM"));
        }

        // 点数
        for (int i = 0; i < table.length; i++) {
            final List<String> line4 = newLine(lines);
            line4.add(printAreaMap.get("POINT_AREA" + (i + 1)));
            if (null != table[i]) {
                for (int j = 0; j < setList.size(); j++) {
                    final Map<String, String> dataMap = (Map) setList.get(j);
                    line4.add(dataMap.get("POINT" + (i + 1)));
                }
            }
        }

        return lines;
    }

    private List printB2_Csv(final DB2UDB db2, final List<Student> studentList) {
        final List lines = new ArrayList();

        newLine(lines).addAll(Arrays.asList(""));
        newLine(lines).addAll(Arrays.asList("２．異動一覧"));
        newLine(lines).addAll(Arrays.asList("No.","学籍番号","氏名","性別","異動日","異動事由"));

        final List<Student> idou = new ArrayList<Student>();
        for (final Student student : studentList) {
            if (student.isRyugakuKyugaku(0) || student.isTenhennyuugaku(_param, _param._edate) || student.isJoseki(_param, _param._edate)) {
                idou.add(student);
            }
        }
        int gyoNo = 0;
        for (int i = 0; i < idou.size(); i++) {
            final Student student = idou.get(i);

            final List<String> line = newLine(lines);

            gyoNo++;
            line.add(String.valueOf(gyoNo));   // 通し番号
            line.add(student._schregno);        // 学籍番号
            line.add(student._name);            // 氏名
            line.add(student._sexName);         // 性別
            if (student.isTenhennyuugaku(_param, _param._edate)) {
                line.add(KNJ_EditDate.h_format_JP(db2, student._entdate));
                line.add(StringUtils.defaultString(student._entdivName));
            }
            if (student.isRyugakuKyugaku(1)) {
                line.add(KNJ_EditDate.h_format_JP(db2, student._transferSdate1));
                line.add(StringUtils.defaultString(student._transfername1) + " " + StringUtils.defaultString(student._transferreason1));
            } else if (student.isRyugakuKyugaku(2)) {
                line.add(KNJ_EditDate.h_format_JP(db2, student._transferSdate2));
                line.add(StringUtils.defaultString(student._transfername2) + " " + StringUtils.defaultString(student._transferreason2));
            }
            if (student.isJoseki(_param, _param._edate)) {
                line.add(KNJ_EditDate.h_format_JP(db2, student._grddate));
                line.add(StringUtils.defaultString(student._grddivName));
            }
        }

        return lines;
    }

    private List print3_Csv(final List<String[]> print3OutputList) {
        final List lines = new ArrayList();

        newLine(lines).addAll(Arrays.asList(""));
        newLine(lines).addAll(Arrays.asList("３．成績がつかなかった者"));
        if ("2".equals(_param._knjd234vPattern)) {
            newLine(lines).addAll(Arrays.asList("No.", "学籍番号", "氏名", "主な理由"));
        } else {
            newLine(lines).addAll(Arrays.asList("No.", "年組番号", "氏名", "科目名", "主な理由"));
        }

        for (int j = 0; j < print3OutputList.size(); j++) {
            final String[] arr = print3OutputList.get(j);

            final List line = newLine(lines);

            final int gyo = j + 1;
            line.add(String.valueOf(gyo)); // 通し番号
            if ("2".equals(_param._knjd234vPattern)) {
                line.add(arr[1]); // 学籍番号
            } else {
                line.add(arr[0]); // 年組番号
            }
            line.add(arr[2]); // 氏名
            line.add(arr[3]); // 理由
            line.add(arr[4]); // 欠課理由
        }

        return lines;
    }

    private List print4_Csv(final List<Student> attendOverStudentList) {
        final List lines = new ArrayList();

        newLine(lines).addAll(Arrays.asList(new String[] {""}));
        newLine(lines).addAll(Arrays.asList("４．欠席・早退・遅刻の多い者（欠席が" + _param._kesseki + "、遅刻が" + _param._chikoku + "、早退が" + _param._soutai + "以上の者）"));
        if ("2".equals(_param._knjd234vPattern)) {
            newLine(lines).addAll(Arrays.asList("No.", "学籍番号", "氏名", "欠席", "遅刻", "早退", "主な理由"));
        } else {
            newLine(lines).addAll(Arrays.asList("No.", "年組番号", "氏名", "欠席", "遅刻", "早退", "主な理由"));
        }

        for (int j = 0; j < attendOverStudentList.size(); j++) {
            final Student student = attendOverStudentList.get(j);
            final List<String> line = newLine(lines);
            final int gyo = j + 1;

            line.add(String.valueOf(gyo));  // 通し番号
            if ("2".equals(_param._knjd234vPattern)) {
                line.add(student._schregno); // 学籍番号
            } else {
                line.add(student.getHrNameabbvAttendnoCd(_param)); // 年組番号
            }
            line.add(student._name);                              // 氏名
            line.add(zeroToNull(student._attendance._absence)); // 欠席
            line.add(zeroToNull(student._attendance._late));    // 遅刻
            line.add(zeroToNull(student._attendance._early));   // 早退
            final String specialReasonTestDatRemark = student.getSpecialReasonTestDatRemark(D056_02 + D056_SUBCLASSCD_ZERO);
            line.add(specialReasonTestDatRemark);                 // 欠課理由
        }

        return lines;
    }

    private List print5_Csv(final List<HrClass> hrClassList, final List<Student> studentList, final Map averageDatMap) {
        final List lines    = new ArrayList();
        final List dataList = new ArrayList();

        final String setTitle = ("2".equals(_param._knjd234vPattern)) ? "５．科目別度数分布": "５．組・科目別平均および科目別度数分布";
        newLine(lines).addAll(Arrays.asList(setTitle));

        // 下段 欠試数
        final int[] kessiCount = new int[10];
        final List allKessiScoreSubclassList = new ArrayList();
        final Map allTuishidoScoreSubclassMap = new HashMap();
        for (final Student student : studentList) {
            final List<SubclassScore> kessiSubclassList = student.getKettenSubclassList(_param);
            if (kessiSubclassList.size() > 0) {
                final int count = Math.min(10, kessiSubclassList.size());
                kessiCount[count - 1]++;
            }
            allKessiScoreSubclassList.addAll(kessiSubclassList);
            for (final SubclassScore subScore : kessiSubclassList) {
                getMappedList(allTuishidoScoreSubclassMap, subScore._subclass._subclasscd).add(student);
            }
        }
        int kessiTotalCount = 0;
        final Map setKettenData = new HashMap();
        for (int c = 1; c <= 10; c++) {
            setKettenData.put("NO_GET_SUM" + c, String.valueOf(kessiCount[c - 1])); // 個人欠点保有者
            kessiTotalCount += kessiCount[c - 1];
        }
        setKettenData.put("NO_GET_TOTAL1", String.valueOf(kessiTotalCount));                  // 個人欠点保有者合計
        setKettenData.put("NO_GET_TOTAL2", String.valueOf(allKessiScoreSubclassList.size())); // 個人欠点保有者合計

        // HR名称とHRごとの平均の平均
        final Map setHrData = new HashMap();
        for (int j = 0; j < hrClassList.size(); j++) {
            final HrClass hrClass = hrClassList.get(j);
            final int gyo = j + 1;
            setHrData.put("HR_NAME1_" + gyo, hrClass._hrNameabbv); // クラス名
            setHrData.put("TOTAL"     + gyo, hrClass._avgAvg);     // 平均の平均
        }

        // 科目毎のレコード
        final List<Subclass> subclassList = getSubclassList(_param, studentList, false);
        final Map setPointMap = new HashMap();
        for (int i = 0; i < subclassList.size(); i++) {
            final Subclass subclass = subclassList.get(i);

            final Map courseMap;
            if ("1".equals(_param._dosubunpuCourse)) {
                courseMap = subclass._courseCreditsMap;
            } else {
                courseMap = subclass.getCreditCourseCollectionMap(null);
            }

            final List courseMapEntrySetList = new ArrayList(courseMap.entrySet());
            if ("1".equals(_param._dosubunpuCourse)) {
                // Map.Entry#getKeyがコース
                Collections.sort(courseMapEntrySetList, new CompareByHrClass(_param, hrClassList));
            }
            for (final Iterator itc = courseMapEntrySetList.iterator(); itc.hasNext();) {
                final Map.Entry e = (Map.Entry) itc.next();
                final Collection courses;
                final Object credit;
                final Map setDataMap = new HashMap();

                if ("1".equals(_param._dosubunpuCourse)) {
                    final String course = (String) e.getKey();
                    credit = e.getValue();
                    courses = Collections.singleton(course);
                } else {
                    credit = e.getKey();
                    courses = (Collection) e.getValue();
                }

                setDataMap.put("course1", subclass._classabbv);     // 教科
                setDataMap.put("SUBCLASS", subclass._subclassname); // 科目
                setDataMap.put("credit1", null == credit ? null : credit.toString()); // 単位

                for (int j = 0; j < hrClassList.size(); j++) {
                    final HrClass hrClass = hrClassList.get(j);
                    final List filteredStudentList = Student.filterCourses(_param, hrClass._studentList, courses);
                    if (0 != filteredStudentList.size()) {
                        final int gyo = j + 1;
                        setDataMap.put("SCORE" + gyo, calcAverage(toBd(getScoreList(filteredStudentList, subclass._subclasscd)))); // 成績
                    }
                }

                final ScoreDistribution distAll = new ScoreDistribution();
                distAll.addScoreList(getScoreList(Student.filterCourses(_param, studentList, courses), subclass._subclasscd));
                final int[][] table = ScoreDistribution.getDistributionScoreTable(_param);
                for (int j = 0; j < table.length; j++) {
                    if (null != table[j]) {
                        final int distidx = j + 1;
                        setPointMap.put("POINT_AREA" + distidx, String.valueOf(table[j][0]) + (table[j][0] == table[j][1] ? "" : "～" + String.valueOf(table[j][1]))); // 点数
                        setDataMap.put("POINT" + distidx, zeroToNull(distAll.getCount(table[j][0], table[j][1]))); // 点数
                    }
                }

                Integer avgDatSum = null;
                Integer avgDatCount = null;
                Integer avgDatHighscore = new Integer(Integer.MIN_VALUE);
                Integer avgDatLowscore = new Integer(Integer.MAX_VALUE);
                for (final Iterator it = courses.iterator(); it.hasNext();) {
                    final String course = (String) it.next();

                    final String key = AVG_DIV_COURSE + subclass._subclasscd + _param._grade + course;

                    final AverageDat avgDat = (AverageDat) averageDatMap.get(key);
                    if (null != avgDat) {
                        avgDatSum = !NumberUtils.isDigits(avgDat._score) ? avgDatSum : new Integer(Integer.parseInt(avgDat._score) + (null == avgDatSum ? 0 : avgDatSum.intValue()));
                        avgDatCount = !NumberUtils.isDigits(avgDat._count) ? avgDatCount : new Integer(Integer.parseInt(avgDat._count) + (null == avgDatCount ? 0 : avgDatCount.intValue()));
                        avgDatHighscore = NumberUtils.isDigits(avgDat._highscore) ? new Integer(Math.max(avgDatHighscore.intValue(), Integer.valueOf(avgDat._highscore).intValue())) : avgDatHighscore;
                        avgDatLowscore = NumberUtils.isDigits(avgDat._lowscore) ? new Integer(Math.min(avgDatLowscore.intValue(), Integer.valueOf(avgDat._lowscore).intValue())) : avgDatLowscore;
                    }
                }
                String avg = null;
                if (null != avgDatSum && null != avgDatCount && 0 != avgDatCount.intValue()) {
                    avg = new BigDecimal(avgDatSum.longValue()).divide(new BigDecimal(avgDatCount.longValue()), 1, BigDecimal.ROUND_HALF_UP).toString();
                }
                setDataMap.put("AVE_CLASS", avg); // 科目平均
                setDataMap.put("NUM", null == avgDatCount ? null : avgDatCount.toString()); // 人数
                if (avgDatHighscore.intValue() != Integer.MIN_VALUE) {
                    setDataMap.put("MAX_SCORE", avgDatHighscore.toString()); // 最高点
                }
                if (avgDatLowscore.intValue() != Integer.MAX_VALUE) {
                    setDataMap.put("MIN_SCORE", avgDatLowscore.toString()); // 最低点
                }
                if (null != allTuishidoScoreSubclassMap.get(subclass._subclasscd)) {
                    final List list = (List) allTuishidoScoreSubclassMap.get(subclass._subclasscd);
                    final int count = Student.filterCourses(_param, list, courses).size();
                    if (count > 0) {
                        setDataMap.put("FAIL_STD", String.valueOf(count)); //
                    }
                }
                dataList.add(setDataMap);
            }
        }

        // 教科名
        final List line1 = newLine(lines);
        line1.add("");
        line1.add("教科");
        for (int i = 0; i < dataList.size(); i++) {
            final Map dataMap = (Map) dataList.get(i);
            line1.add(dataMap.get("course1"));
        }
        if (!"2".equals(_param._knjd234vPattern)) {
            line1.add("クラス平均");
        }

        // 科目名
        final List line2 = newLine(lines);
        line2.add("");
        line2.add("科目");
        for (int i = 0; i < dataList.size(); i++) {
            final Map dataMap = (Map) dataList.get(i);
            line2.add(dataMap.get("SUBCLASS"));
        }

        // 単位数
        final List line3 = newLine(lines);
        line3.add("");
        line3.add("単位数");
        for (int i = 0; i < dataList.size(); i++) {
            final Map dataMap = (Map) dataList.get(i);
            line3.add(dataMap.get("credit1"));
        }

        // クラス別平均
        if (!"2".equals(_param._knjd234vPattern)) {
            boolean firstFlg4 = true;
            for (int j = 0; j < hrClassList.size(); j++) {
                final List line4 = newLine(lines);
                final int gyo = j + 1;
                if (firstFlg4) {
                    line4.add("クラス別平均");
                } else {
                    line4.add("");
                }
                line4.add(setHrData.get("HR_NAME1_" + gyo)); // クラス名
                for (int i = 0; i < dataList.size(); i++) {
                    final Map dataMap = (Map) dataList.get(i);
                    line4.add(dataMap.get("SCORE" + gyo)); // 成績
                }
                line4.add(setHrData.get("TOTAL" + gyo)); // 平均の平均

                firstFlg4 = false;
            }
        }

        // 度数分布
        boolean firstFlg5 = true;
        final int[][] table = ScoreDistribution.getDistributionScoreTable(_param);
        for (int i = 0; i < table.length; i++) {
            final List line5 = newLine(lines);
            if (firstFlg5) {
                line5.add("度数分布");
            } else {
                line5.add("");
            }
            line5.add(setPointMap.get("POINT_AREA" + (i + 1)));// 点数
            if (null != table[i]) {
                for (int j = 0; j < dataList.size(); j++) {
                    final Map dataMap = (Map) dataList.get(j);
                    line5.add(dataMap.get("POINT" + (i + 1)));// 点数
                }
            }

            firstFlg5 = false;
        }

        // 人数
        final List line6 = newLine(lines);
        line6.add("");
        line6.add("人数");
        for (int i = 0; i < dataList.size(); i++) {
            final Map dataMap = (Map) dataList.get(i);
            line6.add(dataMap.get("NUM"));
        }

        // 科目平均
        final List line7 = newLine(lines);
        line7.add("");
        line7.add("科目平均");
        for (int i = 0; i < dataList.size(); i++) {
            final Map dataMap = (Map) dataList.get(i);
            line7.add(dataMap.get("AVE_CLASS"));
        }

        // 最高点
        final List line8 = newLine(lines);
        line8.add("");
        line8.add("最高点");
        for (int i = 0; i < dataList.size(); i++) {
            final Map dataMap = (Map) dataList.get(i);
            line8.add(dataMap.get("MAX_SCORE"));
        }

        // 最低点
        final List line9 = newLine(lines);
        line9.add("");
        line9.add("最低点");
        for (int i = 0; i < dataList.size(); i++) {
            final Map dataMap = (Map) dataList.get(i);
            line9.add(dataMap.get("MIN_SCORE"));
        }

        // 欠点者数
        final List line10 = newLine(lines);
        line10.add("");
        line10.add("欠点者数");
        for (int i = 0; i < dataList.size(); i++) {
            final Map dataMap = (Map) dataList.get(i);
            line10.add(dataMap.get("FAIL_STD"));
        }

        // 個人欠点保有者
        newLine(lines).addAll(Arrays.asList("","個人欠点保有者","科目数","1","2","3","4","5","6","7","8","9","10～","合計","延べ欠点数"));
        final List line11 = newLine(lines);
        line11.add("");
        line11.add("");
        line11.add("人数");
        for (int c = 1; c <= 10; c++) {
            line11.add(setKettenData.get("NO_GET_SUM" + c));
        }
        line11.add(setKettenData.get("NO_GET_TOTAL1"));
        line11.add(setKettenData.get("NO_GET_TOTAL2"));

        return lines;
    }

    // 追指導
    private List printTuishido_Csv(final int bango, final List<Student> studentList) {
        final List lines = new ArrayList();

        // タイトル
        newLine(lines).addAll(Arrays.asList(zenkaku(bango) + "．追指導を要する生徒(" + _param._shidouTensuInf2 + "点以下科目" + _param._shidouKamokusuInf2 + "以上を有する生徒)"));

        final List<Student> tuishidoStudentListAll = new ArrayList<Student>();
        for (final Student student : studentList) {
            if ("1".equals(_param._kyugaku) && student.isKyugaku(2)) {
                continue;
            }
            if (student.getKettenSubclassList(_param).size() >= _param._shidouKamokusuInf2) {
                tuishidoStudentListAll.add(student);
            }
        }

        final List setDataList = new ArrayList();
        for (int j = 0; j < tuishidoStudentListAll.size(); j++) {
            final Student student = tuishidoStudentListAll.get(j);
            final Map setDataMap   = new HashMap();
            setDataMap.put("HR_NO", student.getHrNameabbvAttendnoCd(_param)); // 年組番号
            setDataMap.put("SCHREG_NO", student._schregno); // 学籍番号
            setDataMap.put("name", student._name); // 氏名
            if (null != student._subclassScore999999) {
                setDataMap.put("TOTAL", student._subclassScore999999._score); // 総合点
                setDataMap.put("AVERAGE", sishaGonyu(student._subclassScore999999._avg));                       // 平均点
                setDataMap.put("GRADE_RANK1", student._subclassScore999999.getRank(OUTPUTRANK_HR, _param));     // 学級順位
                setDataMap.put("GRADE_RANK2", student._subclassScore999999.getRank(OUTPUTRANK_COURSE, _param)); // コース順位
                setDataMap.put("GRADE_RANK3", student._subclassScore999999.getRank(OUTPUTRANK_MAJOR, _param));  // 学科順位
                setDataMap.put("GRADE_RANK4", student._subclassScore999999.getRank(OUTPUTRANK_GRADE, _param));  // 学年順位
            }
            setDataMap.put("FAIL", String.valueOf(student.getKettenSubclassList(_param).size())); // 欠点科目数
            if (null != _param._beforeTestItem && null != student._subclassScore999999BeforeTest) {
                setDataMap.put("GRADE_RANK5_FLG", student._subclassScore999999BeforeTest);
                setDataMap.put("GRADE_RANK5", student._subclassScore999999BeforeTest.getRank(_param._outputRankBefore, _param)); // 前定期考査学級順位
            }
            setDataMap.put("REMARK", student.getRemark()); // 備考

            setDataList.add(setDataMap);
        }
        final List subclassInfoList = new ArrayList();
        final List<Subclass> subclassList;
        if (tuishidoStudentListAll.size() == 0) {
            subclassList = Collections.EMPTY_LIST;
        } else {
            subclassList = getSubclassList(_param, studentList, false);
            for (int i = 0; i < subclassList.size(); i++) {
                final Subclass subclass = subclassList.get(i);

                for (final Iterator itc = subclass.getCreditCourseCollectionMap(null).entrySet().iterator(); itc.hasNext();) {
                    final Map.Entry e = (Map.Entry) itc.next();
                    final Object credit = e.getKey();
                    final Collection courses = (Collection) e.getValue();
                    final Map subclassInfoMap = new HashMap();

                    subclassInfoMap.put("course1", subclass._classabbv); // 教科
                    subclassInfoMap.put("SUBCLASS", subclass._subclassname); // 科目
                    subclassInfoMap.put("credit1", null == credit ? null : credit.toString()); // 単位数

                    for (int j = 0; j < tuishidoStudentListAll.size(); j++) {
                        final Student student = tuishidoStudentListAll.get(j);
                        if (!courses.contains(student.course())) {
                            continue;
                        }
                        final SubclassScore subScore = student._subclassScore.get(subclass._subclasscd);
                        if (null != subScore) {
                            subclassInfoMap.put(student._schregno, subScore._score); // 点数
                        }
                    }
                    subclassInfoList.add(subclassInfoMap);
                }
            }
        }

        // 教科名
        final List line1 = newLine(lines);
        line1.add("");
        line1.add("");
        for (int i = 0; i < subclassInfoList.size(); i++) {
            final Map dataMap = (Map) subclassInfoList.get(i);
            line1.add(dataMap.get("course1"));
        }

        // 科目名等情報
        final List line2 = newLine(lines);
        line2.add("年・組番号");
        line2.add("氏名");
        for (int i = 0; i < subclassInfoList.size(); i++) {
            final Map dataMap = (Map) subclassInfoList.get(i);
            line2.add(dataMap.get("SUBCLASS"));
        }
        line2.add("総計");
        line2.add("平均");
        line2.add("欠点数");
        if (_param._shidouHrRank) {
            line2.add("学級順位");
        }
        if (_param._shidouCourseRank) {
            line2.add("コース順位");
        }
        if (_param._shidouMajorRank) {
            line2.add("学科順位");
        }
        if (_param._shidouGradeRank) {
            line2.add("学年順位");
        }
        if (null != _param._beforeTestItem) {
            line2.add(StringUtils.defaultString(_param._beforeTestItem.getSemesterTestitemname(), "前定期考査") + "順位");
        }
        line2.add(_param.getRemarkTitle());

        // 単位数
        final List line3 = newLine(lines);
        line3.add("");
        line3.add("");
        for (int i = 0; i < subclassInfoList.size(); i++) {
            final Map dataMap = (Map) subclassInfoList.get(i);
            line3.add(dataMap.get("credit1"));
        }

        // 各データセット
        for (Iterator it = setDataList.iterator(); it.hasNext();) {
            Map setDat = (Map) it.next();
            final List setline = newLine(lines);

            setline.add(setDat.get("HR_NO"));       // 年組番号
            setline.add(setDat.get("name"));        // 氏名
            // 点数
            for (int i = 0; i < subclassInfoList.size(); i++) {
                final Map dataMap = (Map) subclassInfoList.get(i);
                setline.add(dataMap.get(setDat.get("SCHREG_NO")));
            }
            setline.add(setDat.get("TOTAL"));       // 総計
            setline.add(setDat.get("AVERAGE"));     // 平均点
            setline.add(setDat.get("FAIL"));        // 欠点科目数
            if (_param._shidouHrRank) {
                setline.add(setDat.get("GRADE_RANK1")); // 学級順位
            }
            if (_param._shidouCourseRank) {
                setline.add(setDat.get("GRADE_RANK2")); // コース順位
            }
            if (_param._shidouMajorRank) {
                setline.add(setDat.get("GRADE_RANK3")); // 学科順位
            }
            if (_param._shidouGradeRank) {
                setline.add(setDat.get("GRADE_RANK4")); // 学年順位
            }
            if (null != _param._beforeTestItem && null != setDat.get("GRADE_RANK5_FLG")) {
                setline.add(setDat.get("GRADE_RANK5")); // 前定期考査学級順位
            }
            setline.add(setDat.get("REMARK"));      // 備考
        }

        return lines;
    }

    private List printJoui_Csv(final int bango, final List<Student> studentList, final String course) {
        final StudentScoreComparator comparator = new StudentScoreComparator(_param, StudentScoreComparator.ORDER_BY_SCORE_DESC);
        final List lines = new ArrayList();

        final List<Student> scoreStudentListAll1 = new ArrayList<Student>(studentList);
        Collections.sort(scoreStudentListAll1, comparator);
        for (final Iterator<Student> it = scoreStudentListAll1.iterator(); it.hasNext();) {
            final Student student = it.next();
            if ("1".equals(_param._kyugaku) && student.isKyugaku(2)) {
                it.remove();
            }
        }
        final List<Student> scoreStudentListAll = take(scoreStudentListAll1, _param._yuryo);
        if (scoreStudentListAll.size() > 0) {
            // 一番最後の生徒と同じ成績の生徒は、同一の順位として表示対象とする
            final Student lastStudent = scoreStudentListAll.get(scoreStudentListAll.size() - 1);
            for (final Iterator<Student> its = drop(scoreStudentListAll1, _param._yuryo).iterator(); its.hasNext();) {
                final Student s = its.next();
                if (null == lastStudent._subclassScore999999 || comparator.compareScore(lastStudent._subclassScore999999, s._subclassScore999999) != 0) {
                    break;
                }
                scoreStudentListAll.add(s);
            }
        }

        // タイトル
        final Student first = scoreStudentListAll.get(0);
        final String majorCoursecodeName = null == course ? "" : (" " + StringUtils.defaultString(first._majorname) + StringUtils.defaultString(first._coursecodename));
        newLine(lines).addAll(Arrays.asList(new String[] {zenkaku(bango) + "．成績上位者(" + _param.getGradeTitle() + "上位者" + String.valueOf(_param._yuryo) + "位まで" + majorCoursecodeName + ")"}));

        final List<Map> setDataList = new ArrayList();
        for (int j = 0; j < scoreStudentListAll.size(); j++) {
            final Student student = scoreStudentListAll.get(j);
            final Map setDataMap   = new HashMap();
            final int gyo = j + 1;
            setDataMap.put("NO", String.valueOf(gyo));      // 番号
            setDataMap.put("HR_NO", student.getHrNameabbvAttendnoCd(_param)); // 年組番号
            setDataMap.put("SCHREG_NO", student._schregno); // 学籍番号
            setDataMap.put("name", student._name);          // 氏名
            if (null != student._subclassScore999999) {
                setDataMap.put("TOTAL", student._subclassScore999999._score); // 総合点
                setDataMap.put("AVERAGE", sishaGonyu(student._subclassScore999999._avg));                       // 平均点
                setDataMap.put("GRADE_RANK1", student._subclassScore999999.getRank(OUTPUTRANK_HR, _param));     // 学級順位
                setDataMap.put("GRADE_RANK2", student._subclassScore999999.getRank(OUTPUTRANK_COURSE, _param)); // コース順位
                setDataMap.put("GRADE_RANK3", student._subclassScore999999.getRank(OUTPUTRANK_MAJOR, _param));  // 学科順位
                setDataMap.put("GRADE_RANK4", student._subclassScore999999.getRank(OUTPUTRANK_GRADE, _param));  // 学年順位
            }
            setDataMap.put("FAIL", String.valueOf(student.getKettenSubclassList(_param).size())); // 欠点科目数
            if (null != _param._beforeTestItem && null != student._subclassScore999999BeforeTest) {
                setDataMap.put("GRADE_RANK5_FLG", student._subclassScore999999BeforeTest);
                setDataMap.put("GRADE_RANK5", student._subclassScore999999BeforeTest.getRank(_param._outputRankBefore, _param)); // 前定期考査学級順位
            }
            setDataMap.put("REMARK", student.getRemark()); // 備考

            setDataList.add(setDataMap);
        }

        final List subclassInfoList = new ArrayList();
        final List<Subclass> subclassList;
        if (scoreStudentListAll.size() == 0) {
            subclassList = Collections.EMPTY_LIST;
        } else {
            subclassList = getSubclassList(_param, studentList, false);
            for (int i = 0; i < subclassList.size(); i++) {
                final Subclass subclass = subclassList.get(i);

                for (final Iterator itc = subclass.getCreditCourseCollectionMap(course).entrySet().iterator(); itc.hasNext();) {
                    final Map.Entry e = (Map.Entry) itc.next();
                    final Object credit = e.getKey();
                    final Collection courses = (Collection) e.getValue();
                    final Map subclassInfoMap = new HashMap();

                    subclassInfoMap.put("course1", subclass._classabbv); // 教科
                    subclassInfoMap.put("SUBCLASS", subclass._subclassname); // 科目
                    subclassInfoMap.put("credit1", null == credit ? null : credit.toString()); // 単位数

                    for (int j = 0; j < scoreStudentListAll.size(); j++) {
                        final Student student = scoreStudentListAll.get(j);
                        if (!courses.contains(student.course())) {
                            continue;
                        }
                        final String score = student.getScore(subclass._subclasscd);
                        subclassInfoMap.put(student._schregno, score); // 成績
                    }
                    subclassInfoList.add(subclassInfoMap);
                }
            }
        }

        // 教科名
        final List line1 = newLine(lines);
        line1.add("");
        line1.add("");
        line1.add("");
        for (int i = 0; i < subclassInfoList.size(); i++) {
            final Map dataMap = (Map) subclassInfoList.get(i);
            line1.add(dataMap.get("course1"));
        }

        // 科目名等情報
        final List line2 = newLine(lines);
        line2.add("№");
        line2.add("年・組番号");
        line2.add("氏名");
        for (int i = 0; i < subclassInfoList.size(); i++) {
            final Map dataMap = (Map) subclassInfoList.get(i);
            line2.add(dataMap.get("SUBCLASS"));
        }
        line2.add("総計");
        line2.add("平均");
        line2.add("欠点数");
        if (_param._yuryoHrRank) {
            line2.add("学級順位");
        }
        if (_param._yuryoCourseRank) {
            line2.add("コース順位");
        }
        if (_param._yuryoMajorRank) {
            line2.add("学科順位");
        }
        if (_param._yuryoGradeRank) {
            line2.add("学年順位");
        }
        if (null != _param._beforeTestItem) {
            line2.add(StringUtils.defaultString(_param._beforeTestItem.getSemesterTestitemname(), "前定期考査") + "順位");
        }
        line2.add(_param.getRemarkTitle());

        // 単位数
        final List line3 = newLine(lines);
        line3.add("");
        line3.add("");
        line3.add("");
        for (int i = 0; i < subclassInfoList.size(); i++) {
            final Map dataMap = (Map) subclassInfoList.get(i);
            line3.add(dataMap.get("credit1"));
        }

        // 各データセット
        for (final Map setDat : setDataList) {
            final List setline = newLine(lines);

            setline.add(setDat.get("NO"));          // 番号
            setline.add(setDat.get("HR_NO"));       // 年組番号
            setline.add(setDat.get("name"));        // 氏名
            // 点数
            for (int i = 0; i < subclassInfoList.size(); i++) {
                final Map dataMap = (Map) subclassInfoList.get(i);
                setline.add(dataMap.get(setDat.get("SCHREG_NO")));
            }
            setline.add(setDat.get("TOTAL"));       // 総計
            setline.add(setDat.get("AVERAGE"));     // 平均点
            setline.add(setDat.get("FAIL"));        // 欠点科目数
            if (_param._yuryoHrRank) {
                setline.add(setDat.get("GRADE_RANK1")); // 学級順位
            }
            if (_param._yuryoCourseRank) {
                setline.add(setDat.get("GRADE_RANK2")); // コース順位
            }
            if (_param._yuryoMajorRank) {
                setline.add(setDat.get("GRADE_RANK3")); // 学科順位
            }
            if (_param._yuryoGradeRank) {
                setline.add(setDat.get("GRADE_RANK4")); // 学年順位
            }
            if (null != _param._beforeTestItem && null != setDat.get("GRADE_RANK5_FLG")) {
                setline.add(setDat.get("GRADE_RANK5")); // 前定期考査学級順位
            }
            setline.add(setDat.get("REMARK"));      // 備考
        }

        return lines;
    }

    private List printKai_Csv(final int bango, final List<Student> studentList, final String course, final String majornameCoursecodename) {
        final List lines = new ArrayList();

        final List<Student> scoreStudentListAll = getKaiScoreStudentListAll(studentList);

        // タイトル
        final String majorCoursecodeName = null == course ? "" : (" " + majornameCoursecodename);
        String title;
        if ("2".equals(_param._fushinDiv)) {
            title = zenkaku(bango) + "．成績下位者(" + _param.getGradeTitle() + "欠点科目数" + String.valueOf(_param._kettenCount) + "以上" + majorCoursecodeName + ")";
        } else {
            title = zenkaku(bango) + "．成績下位者(" + _param.getGradeTitle() + "下位者" + String.valueOf(_param._fushin) + "位まで" + majorCoursecodeName + ")";
        }
        newLine(lines).addAll(Arrays.asList(new String[] {title}));

        final List setDataList = new ArrayList();
        for (int j = 0; j < scoreStudentListAll.size(); j++) {
            final Student student = (Student) scoreStudentListAll.get(j);
            final Map setDataMap   = new HashMap();
            final int gyo = j + 1;
            setDataMap.put("NO", String.valueOf(gyo));      // 番号
            setDataMap.put("HR_NO", student.getHrNameabbvAttendnoCd(_param)); // 年組番号
            setDataMap.put("SCHREG_NO", student._schregno); // 学籍番号
            setDataMap.put("name", student._name);          // 氏名
            if (null != student._subclassScore999999) {
                setDataMap.put("TOTAL", student._subclassScore999999._score); // 総合点
                setDataMap.put("AVERAGE", sishaGonyu(student._subclassScore999999._avg));                       // 平均点
                setDataMap.put("GRADE_RANK1", student._subclassScore999999.getRank(OUTPUTRANK_HR, _param));     // 学級順位
                setDataMap.put("GRADE_RANK2", student._subclassScore999999.getRank(OUTPUTRANK_COURSE, _param)); // コース順位
                setDataMap.put("GRADE_RANK3", student._subclassScore999999.getRank(OUTPUTRANK_MAJOR, _param));  // 学科順位
                setDataMap.put("GRADE_RANK4", student._subclassScore999999.getRank(OUTPUTRANK_GRADE, _param));  // 学年順位
            }
            setDataMap.put("FAIL", String.valueOf(student.getKettenSubclassList(_param).size())); // 欠点科目数
            if (null != _param._beforeTestItem && null != student._subclassScore999999BeforeTest) {
                setDataMap.put("GRADE_RANK5_FLG", student._subclassScore999999BeforeTest);
                setDataMap.put("GRADE_RANK5", student._subclassScore999999BeforeTest.getRank(_param._outputRankBefore, _param)); // 前定期考査学級順位
            }
            setDataMap.put("REMARK", student.getRemark()); // 備考

            setDataList.add(setDataMap);
        }

        final List subclassInfoList = new ArrayList();
        final List<Subclass> subclassList;
        if (scoreStudentListAll.size() == 0) {
            subclassList = Collections.EMPTY_LIST;
        } else {
            subclassList = getSubclassList(_param, scoreStudentListAll, false);
            for (int i = 0; i < subclassList.size(); i++) {
                final Subclass subclass = subclassList.get(i);

                for (final Iterator itc = subclass.getCreditCourseCollectionMap(course).entrySet().iterator(); itc.hasNext();) {
                    final Map.Entry e = (Map.Entry) itc.next();
                    final Object credit = e.getKey();
                    final Collection courses = (Collection) e.getValue();
                    final Map subclassInfoMap = new HashMap();

                    subclassInfoMap.put("course1", subclass._classabbv); // 教科
                    subclassInfoMap.put("SUBCLASS", subclass._subclassname); // 科目
                    subclassInfoMap.put("credit1", null == credit ? null : credit.toString()); // 単位数

                    for (int j = 0; j < scoreStudentListAll.size(); j++) {
                        final Student student = (Student) scoreStudentListAll.get(j);
                        if (!courses.contains(student.course())) {
                            continue;
                        }
                        final String score = student.getScore(subclass._subclasscd);
                        subclassInfoMap.put(student._schregno, score); // 成績
                    }
                    subclassInfoList.add(subclassInfoMap);
                }
            }
        }

        // 教科名
        final List line1 = newLine(lines);
        line1.add("");
        line1.add("");
        line1.add("");
        for (int i = 0; i < subclassInfoList.size(); i++) {
            final Map dataMap = (Map) subclassInfoList.get(i);
            line1.add(dataMap.get("course1"));
        }

        // 科目名等情報
        final List line2 = newLine(lines);
        line2.add("№");
        line2.add("年・組番号");
        line2.add("氏名");
        for (int i = 0; i < subclassInfoList.size(); i++) {
            final Map dataMap = (Map) subclassInfoList.get(i);
            line2.add(dataMap.get("SUBCLASS"));
        }
        line2.add("総計");
        line2.add("平均");
        line2.add("欠点数");
        if (_param._fushinHrRank) {
            line2.add("学級順位");
        }
        if (_param._fushinCourseRank) {
        line2.add("コース順位");
        }
        if (_param._fushinMajorRank) {
            line2.add("学科順位");
        }
        if (_param._fushinGradeRank) {
            line2.add("学年順位");
        }
        if (null != _param._beforeTestItem) {
            line2.add(StringUtils.defaultString(_param._beforeTestItem.getSemesterTestitemname(), "前定期考査") + "順位");
        }
        line2.add(_param.getRemarkTitle());

        // 単位数
        final List line3 = newLine(lines);
        line3.add("");
        line3.add("");
        line3.add("");
        for (int i = 0; i < subclassInfoList.size(); i++) {
            final Map dataMap = (Map) subclassInfoList.get(i);
            line3.add(dataMap.get("credit1"));
        }

        // 各データセット
        for (Iterator it = setDataList.iterator(); it.hasNext();) {
            Map setDat = (Map) it.next();
            final List setline = newLine(lines);

            setline.add(setDat.get("NO"));          // 番号
            setline.add(setDat.get("HR_NO"));       // 年組番号
            setline.add(setDat.get("name"));        // 氏名
            // 点数
            for (int i = 0; i < subclassInfoList.size(); i++) {
                final Map dataMap = (Map) subclassInfoList.get(i);
                setline.add(dataMap.get(setDat.get("SCHREG_NO")));
            }
            setline.add(setDat.get("TOTAL"));       // 総計
            setline.add(setDat.get("AVERAGE"));     // 平均点
            setline.add(setDat.get("FAIL"));        // 欠点科目数
            if (_param._fushinHrRank) {
                setline.add(setDat.get("GRADE_RANK1")); // 学級順位
            }
            if (_param._fushinCourseRank) {
                setline.add(setDat.get("GRADE_RANK2")); // コース順位
            }
            if (_param._fushinMajorRank) {
                setline.add(setDat.get("GRADE_RANK3")); // 学科順位
            }
            if (_param._fushinGradeRank) {
                setline.add(setDat.get("GRADE_RANK4")); // 学年順位
            }
            if (null != _param._beforeTestItem && null != setDat.get("GRADE_RANK5_FLG")) {
                setline.add(setDat.get("GRADE_RANK5")); // 前定期考査学級順位
            }
            setline.add(setDat.get("REMARK"));      // 備考
        }

        return lines;
    }

    private List printShukketsuIjou_Csv(final int bango, final List<Student> studentList) {
        final List lines = new ArrayList();

        String text;
        text =  zenkaku(bango) + "．出席の正常でない者（欠席日数が出席すべき日数の" + String.valueOf(_param._nissuuBunshi) + "/" + String.valueOf(_param._nissuBunbo) + "を超える者、";
        if (_param._useHouteiKekkaJisu) {
            text += "及び欠課時数が法定時間の" + String.valueOf(_param._jisuuBunshi) + "/" + String.valueOf(_param._jisuBunbo) + "を超える科目を有する者とその科目）";
        } else {
            text += "及び欠課時数が実時数の" + String.valueOf(_param._jisuuBunshi) + "/" + String.valueOf(_param._jisuBunbo) + "を超える科目を有する者とその科目）";
        }

        final List<Student> attendOverStudentListAll = getAttendSubclassOverStudentList(_param, studentList);

        // タイトル
        newLine(lines).addAll(Arrays.asList(new String[] {text}));

        final List<Map> setDataList = new ArrayList();
        for (int j = 0; j < attendOverStudentListAll.size(); j++) {
            final Student student = (Student) attendOverStudentListAll.get(j);
            final Map setDataMap   = new HashMap();
            final int gyo = j + 1;
            setDataMap.put("NO", String.valueOf(gyo));      // 番号
            setDataMap.put("HR_NO", student.getHrNameabbvAttendnoCd(_param)); // 年組番号
            setDataMap.put("SCHREG_NO", student._schregno); // 学籍番号
            setDataMap.put("name", student._name);          // 氏名
            setDataMap.put("ABSENT_UP", String.valueOf(student._attendance._absence));   // 欠席日数
            setDataMap.put("ABSENT_DOWN", String.valueOf(student._attendance._mlesson)); // 欠席日数
            setDataMap.put("REMARK", student.getSpecialReasonTestDatRemark(D056_03 + D056_SUBCLASSCD_ZERO)); // 備考

            setDataList.add(setDataMap);
        }

        final List<Map<String, String>> subclassInfoList = new ArrayList<Map<String, String>>();
        final List<Subclass> subclassList;
        if (attendOverStudentListAll.size() == 0) {
            subclassList = Collections.EMPTY_LIST;
        } else {
            subclassList = getSubclassList(_param, studentList, true);
            for (int i = 0; i < subclassList.size(); i++) {
                final Subclass subclass = subclassList.get(i);

                for (final Iterator itc = subclass.getCreditCourseCollectionMap(null).entrySet().iterator(); itc.hasNext();) {
                    final Map.Entry e = (Map.Entry) itc.next();
                    final Object credit = e.getKey();
                    final Collection courses = (Collection) e.getValue();
                    final Map<String, String> subclassInfoMap = new HashMap<String, String>();

                    subclassInfoMap.put("course1", subclass._classabbv); // 教科
                    subclassInfoMap.put("SUBCLASS", subclass._subclassname); // 科目
                    subclassInfoMap.put("credit1", null == credit ? null : credit.toString()); // 単位数

                    for (int j = 0; j < attendOverStudentListAll.size(); j++) {
                        final Student student = attendOverStudentListAll.get(j);
                        if (!courses.contains(student.course())) {
                            continue;
                        }
                        final SubclassAttendance subatt = student._subclassAttendance.get(subclass._subclasscd);
                        if (null != subatt) {
                            final String data = String.valueOf(subatt._sick.intValue() + "/" + subatt.jugyouJisu(_param));
                            subclassInfoMap.put(student._schregno + "_1", data); // 成績
                        }
                        subclassInfoMap.put(student._schregno + "_2", student.getScore(subclass._subclasscd)); // 成績
                    }
                    subclassInfoList.add(subclassInfoMap);
                }
            }
        }

        // 教科名
        final List line1 = newLine(lines);
        line1.add("");
        line1.add("");
        line1.add("");
        for (int i = 0; i < subclassInfoList.size(); i++) {
            final Map dataMap = (Map) subclassInfoList.get(i);
            line1.add(dataMap.get("course1"));
        }

        // 科目名等情報
        final List line2 = newLine(lines);
        line2.add("№");
        line2.add("組");
        line2.add("氏名");
        for (int i = 0; i < subclassInfoList.size(); i++) {
            final Map dataMap = (Map) subclassInfoList.get(i);
            line2.add(dataMap.get("SUBCLASS"));
        }
        line2.add("欠席日数");
        line2.add("主な理由");

        // 単位数
        final List line3 = newLine(lines);
        line3.add("");
        line3.add("");
        line3.add("");
        for (int i = 0; i < subclassInfoList.size(); i++) {
            final Map dataMap = (Map) subclassInfoList.get(i);
            line3.add(dataMap.get("credit1"));
        }

        // 各データセット
        for (final Map setDat : setDataList) {
            final List setline1 = newLine(lines);

            setline1.add(setDat.get("NO"));          // 番号
            setline1.add(setDat.get("HR_NO"));       // 年組番号
            setline1.add(setDat.get("name"));        // 氏名
            // 点数
            for (int i = 0; i < subclassInfoList.size(); i++) {
                final Map dataMap = (Map) subclassInfoList.get(i);
                setline1.add(dataMap.get(setDat.get("SCHREG_NO") + "_1"));
            }
            final String setAbsent = setDat.get("ABSENT_UP") + "/" + setDat.get("ABSENT_DOWN");
            setline1.add(setAbsent);             // 欠席日数
            setline1.add(setDat.get("REMARK"));  // 備考

            final List setline2 = newLine(lines);
            setline2.add("");
            setline2.add("");
            setline2.add("参考評点");
            // 点数
            for (int i = 0; i < subclassInfoList.size(); i++) {
                final Map dataMap = (Map) subclassInfoList.get(i);
                setline2.add(dataMap.get(setDat.get("SCHREG_NO") + "_2"));
            }
        }

        return lines;
    }

    private List<List<String>> printSeitobetsuTsusanKettenKamokusu_Csv(final int bango, final List<Student> studentList) {
        final List<List<String>> lines = new ArrayList<List<String>>();

        final List<SubclassScore> printList = new ArrayList<SubclassScore>();
        for (final Student student : studentList) {
            for (final String testcd : student._tsusanKettenKamokuListMap.keySet()) {
                final List<SubclassScore> kettenKamokuList = getMappedList(student._tsusanKettenKamokuListMap, testcd);
                printList.addAll(kettenKamokuList);
            }
        }
        if (printList.isEmpty()) {
            printList.add(null); // ページを印字するダミー
        }

        newLine(lines).addAll(Arrays.asList(zenkaku(bango) + "．生徒別通算欠点科目数（" + String.valueOf(_param._shidouTensuInf2) + "点以下科目を有する者）"));

        final String setDivName = StringUtils.defaultString(StringUtils.defaultString(_param._d053Map.get("01"), _param._d053Map.get("08")));
        newLine(lines).addAll(Arrays.asList("年組番号", "氏名", "年度", "学期", "考査名", "科目名", setDivName));

        for (int i = 0; i < printList.size(); i++) {
            final SubclassScore subScore = printList.get(i);
            if (null == subScore) {
                continue;
            }
            final TestItem testItem = subScore._testItem;

            final List<String> line = newLine(lines);
            line.add(subScore._student.getGradeHrclassAttendnoCd()); // 年組番号
            line.add(subScore._student._name);          // 氏名
            line.add(testItem._year);                   // 年度
            line.add(testItem._semester);               // 学期
            line.add(testItem._testitemname);           // 考査名
            line.add(subScore._subclass._subclassname); // 科目
            line.add(subScore._score);                  // 評価
        }
        return lines;
    }

    /**
     * 生徒が欠課時数超過している場合、判定結果文字列を返す
     * @param param
     * @param student 生徒
     * @return 欠課時数超過している場合、判定結果文字列。それ以外はnull
     */
    private static String getAttendSubclassOverText(final Param param, final Student student) {
        final List<String> rtn = new ArrayList<String>();
        for (final SubclassAttendance sa : student._subclassAttendance.values()) {
            if (null == sa._subclass || null == sa._subclass._subclasscd || sa._subclass._subclasscd.length()  < 2 || Integer.parseInt(sa._subclass._subclasscd.substring(0, 2)) > 90) {
                continue;
            }
            // 欠課時数が実時数の１／３を超える科目
            if (sa.isAttendOver(param)) {
                final Subclass subclass = param._subclassMap.get(sa._subclass._subclasscd);
                final String subclassname = (null == subclass) ? null : subclass._subclassname;
                if (param._isOutputDebug) {
                    log.info(" student : " + student._schregno + " " + student._name + ", subclasscd = " + sa._subclass._subclasscd + ", mlesson = " + sa._mlesson + ", limit = " + sa.getAttendOverLimit(param) + ",  sick = " + sa._sick);
                }
                rtn.add(StringUtils.defaultString(subclassname) + "(" + sa._sick + "/" + sa.jugyouJisu(param) + ")");
            }
        }
        if (null != student._attendance) {
            if (student._attendance.attendOver(param)) {
                if (param._isOutputDebug) {
                    log.info(" student : " + student._schregno + " " + student._name + ", mlesson = " + student._attendance._mlesson + ", limit = " + student._attendance.getAttendOverLimit(param) + ",  sick = " + student._attendance._absence);
                }
                rtn.add("欠席日数(" + student._attendance._absence + ")");
            }
        }
        return rtn.isEmpty() ? null : getText(rtn, "／");
    }

    /**
     * 欠課時数超過の生徒を得る
     * @param param
     * @param studentList 生徒のリスト
     * @return studentListの生徒のうち欠課時数超過の生徒
     */
    private static List<Student> getAttendSubclassOverStudentList(final Param param, final List<Student> studentList) {
        final List<Student> rtn = new ArrayList<Student>();
        for (final Student student : studentList) {
            if ("1".equals(param._kyugaku) && student.isKyugaku(2)) {
                continue;
            }
            final String overText = getAttendSubclassOverText(param, student);
            if (null != overText) {
                if (param._isOutputDebug) {
                    log.info(" 欠課時数超過 " + student._schregno + " " + overText);
                }
                rtn.add(student);
            }
        }
        return rtn;
    }

    private static class StudentScoreComparator implements Comparator<Student> {

        /* 昇順 */
        static int ORDER_BY_SCORE_ASC = 1;
        /* 降順 */
        static int ORDER_BY_SCORE_DESC = -1;

        final StudentRegdComparator _regdComparator = new StudentRegdComparator();

        final Param _param;
        /**
         * 1:昇順、-1:降順
         */
        final int _sort;
        public StudentScoreComparator(final Param param, final int sort) {
            _param = param;
            _sort = sort;
        }

        public int compareScore(final SubclassScore ss1, final SubclassScore ss2) {
            if (null == ss1 && null == ss2) {
                return 0;
            } else if (null == ss1) {
                return 1;
            } else if (null == ss2) {
                return -1;
            } else if (null == ss1.compareValue(_param) && null == ss2.compareValue(_param)) {
                return 0;
            } else if (null == ss1.compareValue(_param)) {
                return 1;
            } else if (null == ss2.compareValue(_param)) {
                return -1;
            }
            final Double v1 = new Double(ss1.compareValue(_param).doubleValue());
            final Double v2 = new Double(ss2.compareValue(_param).doubleValue());
            final int rtn = v1.compareTo(v2);
            if (rtn == 0) {
                return 0;
            }
            return _sort * rtn;
        }

        public int compareKetten(final int ketten1, final int ketten2) {
            return ketten2 - ketten1;
        }

        public int compare(final Student std1, final Student std2) {
            if (_sort == ORDER_BY_SCORE_ASC && "1".equals(_param._fushinOrderKettenCount)) {
                // 欠点科目数順、
                final int cmpScore = compareKetten(std1.getKettenSubclassList(_param).size(), std2.getKettenSubclassList(_param).size());
                if (cmpScore != 0) {
                    return cmpScore;
                }
            }
            final int cmpScore = compareScore(std1._subclassScore999999, std2._subclassScore999999);
            if (cmpScore != 0) {
                return cmpScore;
            }
            return _regdComparator.compare(std1, std2);
        }


    }

    private static class StudentRegdComparator implements Comparator<Student> {
        public int compare(final Student std1, final Student std2) {
            return std1.getHrclassAttendnoCd().compareTo(std2.getHrclassAttendnoCd());
        }
    }

    private static class TestItem implements Comparable<TestItem> {
        final String _year;
        final String _semester;
        final String _semestername;
        final String _testkindcd;
        final String _testitemcd;
        final String _scoreDiv;
        final String _testitemname;
        final String _sidouInput;
//        final String _sidouInputInf;
        public TestItem(final String year, final String semester, final String semestername,
                final String testkindcd, final String testitemcd, final String scoreDiv,
                final String testitemname, final String sidouInput, final String sidouInputInf
                ) {
            _year = year;
            _semester = semester;
            _semestername = semestername;
            _testkindcd = testkindcd;
            _testitemcd = testitemcd;
            _scoreDiv = scoreDiv;
            _testitemname = testitemname;
            _sidouInput = sidouInput;
//            _sidouInputInf = sidouInputInf;
        }
        public String getSemeTestcd() {
            return _semester + "-" +_testkindcd + "-" +_testitemcd + "-" + _scoreDiv;
        }
        public String getSemesterTestitemname() {
            if (null == _semestername && null == _testitemname) {
                return null;
            }
            return StringUtils.defaultString(_semestername) + StringUtils.defaultString(_testitemname);
        }
        public String toString() {
            return "TestItem(" + _semester + _testkindcd + _testitemcd + "(" + _scoreDiv + "))"; //, sidouInput=" + _sidouInput + ", sidouInputInf=" + _sidouInputInf + ")";
        }
        public int compareTo(final TestItem ot) {
            return (_year + getSemeTestcd()).compareTo(ot._year + ot.getSemeTestcd());
        }
    }

    private static class Tuple<K, V> implements Comparable<Tuple<K, V>> {
        final K _first;
        final V _second;
        private Tuple(final K first, final V second) {
            _first = first;
            _second = second;
        }
        public static <K, V> Tuple<K, V> of(final K first, final V second) {
            return new Tuple<K, V>(first, second);
        }
        public int compareTo(final Tuple<K, V> to) {
            int cmp;
            if (null == _first && !(_first instanceof Comparable)) {
                return 1;
            } else if (null == to._first && !(to._first instanceof Comparable)) {
                return -1;
            }
            cmp = ((Comparable) _first).compareTo(to._first);
            if (0 != cmp) {
                return cmp;
            }
            if (null == _second && !(_second instanceof Comparable)) {
                return 1;
            } else if (null == to._second && !(to._second instanceof Comparable)) {
                return -1;
            }
            cmp = ((Comparable) _second).compareTo(to._second);
            return cmp;
        }
        public String toString() {
            return "(" + _first + ", " + _second + ")";
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 75851 $");
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
        final String _schoolKind;
        final String _testcd;
        final String _testcdBefore;
        final String _scoreDiv;
        final String _dateDiv; // 1:累計 2:学期
        final String _sdate;
        final String _edate;
        final String _outputShidou; // 指導入力 出力する
        final String _outputShukketsu; // 出欠 出力する
        final String _outputDosubupu; // 度数分布 出力する
        final String _outputKyokakamoku; // 教科・科目 出力する
        final String _outputYuryo; // 成績上位者 出力する
        final String _outputFushin; // 成績下位者 出力する
        final boolean _shidouHrRank;
        final boolean _shidouCourseRank;
        final boolean _shidouMajorRank;
        final boolean _shidouGradeRank;
        final boolean _yuryoHrRank;
        final boolean _yuryoCourseRank;
        final boolean _yuryoMajorRank;
        final boolean _yuryoGradeRank;
        final boolean _fushinHrRank;
        final boolean _fushinCourseRank;
        final boolean _fushinMajorRank;
        final boolean _fushinGradeRank;
        final int _nissuuBunshi;
        final int _nissuBunbo;
        final int _jisuuBunshi;
        final int _jisuBunbo;
        final int _kesseki;
        final int _chikoku;
        final int _soutai;
        final int _shidouTensuInf2;
        final int _shidouKamokusuInf2;
        final int _yuryo;
        final String _fushinDiv; // 1:不振者 2:欠点科目数
        final int _fushin;
        final int _kettenCount; // 欠点科目数
        final String _fushinOrderKettenCount; // 不振者は欠点科目数順に表示
        final String _kariHyotei; // 990009:学年評定の場合
        final String _major; // _knjd234vUseMajorcdKeta1 = '1'の場合2桁（'すべて'は'00'）、それ以外は4桁（'すべて'は'0000'）
        final String _outputRankBefore; // 1:学級 2:学年 3:コース 4:学科
        final String _outputKijun; // 1:総計 2:平均点
        final String _outputRemark; // 1:出身学校 2:部活動 3:進路希望
        final String _useCurriculumcd;
        final String _knjd234vPrintTsusanKettenKamokusu;
        final String _useSchool_KindField;
        final String _use_prg_schoolkind;
        final List<TestItem> _tusanTestItemList;
        private String _sidouHyoji;
        private String _d054Namecd2Max;
        private String _d056Name1;
        private String _d056Name2;
        final TestItem _testItem;
        final TestItem _beforeTestItem;
        private String _yearSdate;
        final String _use_school_detail_gcm_dat;
        final String _knjd234vPattern;
        final String _knjd234vUseMajorcdKeta1;
        final String _cmd;
        final String _d056; // 01:成績がつかなかった者 02:欠席・早退・遅刻の多い者 03:出席の正常でない者 (SPECIAL_REASON_TEST_DAT.REASON_DIV)
        final String _kesshiNozoku;
        final String _setSchoolKind;
        final String _dosubunpuCourse;
        final String _jouiCourse;
        final String _kaiCourse;
        final String _kyugaku;
        final String _gradeName2;
        final String _transferComment;
        final String _nendo;
        final String _edateStr;
        final Map<String, String> _d053Map;
        private boolean _isLastSemester = false;
        private boolean _isHoutei = false;
        private boolean _useHouteiKekkaJisu = false;
        final boolean _isChiyodaKudan;
        final boolean _isNaraken;
        final boolean _is5dankai;
        final boolean _isReitaku;
        int _syusuInt = 0;
        final String _outputSlump;   //成績不振者(麗澤のみ)
        final String _SlumpTensuInf;   //成績不振者(麗澤のみ)
        final String _d008Namecd1;

        private Map<String, Subclass> _subclassMap;

        private Map _averageDatMap = Collections.EMPTY_MAP;
        final Map _attendParamMap;

        final Set<String> _logOnceSet;
        final boolean _isOutputDebug;
        final boolean _isOutputDebugQuery;
        final boolean _isOutputDebug3;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("CTRL_YEAR");
            _semester = request.getParameter("SEMESTER");
            _grade = request.getParameter("GRADE");
            _nendo = KNJ_EditDate.getAutoFormatYear(db2, Integer.parseInt(_year));

            _testcd = request.getParameter("TEST_CD");
            _testcdBefore = request.getParameter("TEST_CD_BEFORE");
            _scoreDiv = _testcd.substring(4);
            _dateDiv = request.getParameter("DATE_DIV");
            _edate = request.getParameter("EDATE").replace('/', '-');
            _edateStr = KNJ_EditDate.getAutoFormatDate(db2, _edate);

            if (parameterHas("checkOutputEachItem", request.getParameterNames())) {
                _outputKyokakamoku = request.getParameter("OUTPUT_KYOKAKAMOKU");
                _outputShukketsu = request.getParameter("OUTPUT_SHUKKETSU");
                _outputDosubupu = request.getParameter("OUTPUT_DOSUBUPU");
                _outputShidou = request.getParameter("OUTPUT_SHIDOU");
                _outputYuryo = request.getParameter("OUTPUT_YURYO");
                _outputFushin = request.getParameter("OUTPUT_FUSHIN");
            } else {
                _outputKyokakamoku = "1";
                _outputShukketsu = "1";
                _outputDosubupu = "1";
                _outputShidou = "1";
                _outputYuryo = "1";
                _outputFushin = "1";
            }

            _shidouTensuInf2 = toInt(request.getParameter("SHIDOU_TENSU_INF2"), 0);
            _shidouKamokusuInf2 = toInt(request.getParameter("SHIDOU_KAMOKUSU_INF2"), 0);
            _kesseki = toInt(request.getParameter("KESSEKI"), 0);
            _chikoku = toInt(request.getParameter("CHIKOKU"), 0);
            _soutai = toInt(request.getParameter("SOUTAI"), 0);
            _nissuuBunshi = toInt(request.getParameter("NISSUU_BUNSHI"), 0);
            _nissuBunbo = toInt(request.getParameter("NISSUU_BUNBO"), 1);
            _jisuuBunshi = toInt(request.getParameter("JISUU_BUNSHI"), 0);
            _jisuBunbo = toInt(request.getParameter("JISUU_BUNBO"), 1);
            _sdate = request.getParameter("SDATE").replace('/', '-');
            _kariHyotei = request.getParameter("KARI_HYOTEI");
            _major = request.getParameter("MAJOR");
            _outputRankBefore = request.getParameter("OUTPUT_RANK_BEFORE");
            _outputKijun = request.getParameter("OUTPUT_KIJUN");
            _outputRemark = request.getParameter("OUTPUT_REMARK");
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _yearSdate = getYearSdate(db2);
            _use_school_detail_gcm_dat = request.getParameter("use_school_detail_gcm_dat");
            _knjd234vPattern = request.getParameter("knjd234vPattern");
            _knjd234vUseMajorcdKeta1 = request.getParameter("knjd234vUseMajorcdKeta1");
            _knjd234vPrintTsusanKettenKamokusu = request.getParameter("knjd234vPrintTsusanKettenKamokusu");
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            _use_prg_schoolkind = request.getParameter("use_prg_schoolkind");
            _tusanTestItemList = getTsusanTestKindItemList(db2);
            _cmd = request.getParameter("cmd");
            _d056 = request.getParameter("D056");
            _subclassMap = getSubclassMap(db2);
            _yuryo = toInt(request.getParameter("YURYO"), 0);
            _fushinDiv = request.getParameter("FUSHIN_DIV");
            _fushin = toInt(request.getParameter("FUSHIN"), 0);
            _fushinOrderKettenCount = request.getParameter("FUSHIN_ORDER_KETTEN_COUNT");
            _kettenCount = toInt(request.getParameter("KETTEN_COUNT"), 0);
//            _date = request.getParameter("DATE").replace('/', '-');
//            _ctrlYear = request.getParameter("CTRL_YEAR");
//            _ctrlDate = request.getParameter("CTRL_DATE");
            _kesshiNozoku = request.getParameter("KESSHI_NOZOKU");
            _setSchoolKind = request.getParameter("setSchoolKind");
            _dosubunpuCourse = request.getParameter("DOSUBUPU_COURSE");
            _jouiCourse = request.getParameter("JOUI_COURSE");
            _kaiCourse = request.getParameter("KAI_COURSE");
            _kyugaku = request.getParameter("KYUGAKU");
            final boolean hasOutputRankSet = "1".equals(request.getParameter("OUTPUT_RANK_SET"));
            _shidouHrRank = !hasOutputRankSet || "1".equals(request.getParameter("SHIDOU_HR_RANK"));
            _shidouCourseRank = !hasOutputRankSet || "1".equals(request.getParameter("SHIDOU_COURSE_RANK"));
            _shidouMajorRank = !hasOutputRankSet || "1".equals(request.getParameter("SHIDOU_MAJOR_RANK"));
            _shidouGradeRank = !hasOutputRankSet || "1".equals(request.getParameter("SHIDOU_GRADE_RANK"));
            _yuryoHrRank = !hasOutputRankSet || "1".equals(request.getParameter("YURYO_HR_RANK"));
            _yuryoCourseRank = !hasOutputRankSet || "1".equals(request.getParameter("YURYO_COURSE_RANK"));
            _yuryoMajorRank = !hasOutputRankSet || "1".equals(request.getParameter("YURYO_MAJOR_RANK"));
            _yuryoGradeRank = !hasOutputRankSet || "1".equals(request.getParameter("YURYO_GRADE_RANK"));
            _fushinHrRank = !hasOutputRankSet || "1".equals(request.getParameter("FUSHIN_HR_RANK"));
            _fushinCourseRank = !hasOutputRankSet || "1".equals(request.getParameter("FUSHIN_COURSE_RANK"));
            _fushinMajorRank = !hasOutputRankSet || "1".equals(request.getParameter("FUSHIN_MAJOR_RANK"));
            _fushinGradeRank = !hasOutputRankSet || "1".equals(request.getParameter("FUSHIN_GRADE_RANK"));
            final String[] outputDebug = StringUtils.split(getDbPrginfoProperties(db2, "outputDebug"));
            _isOutputDebug = ArrayUtils.contains(outputDebug, "1");
            _isOutputDebugQuery = ArrayUtils.contains(outputDebug, "query");
            _isOutputDebug3 = ArrayUtils.contains(outputDebug, "debug3");
            _logOnceSet = new HashSet<String>();
            _d053Map = KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, " SELECT NAMECD2, NAME1 FROM NAME_MST WHERE NAMECD1 = 'D053' "), "NAMECD2", "NAME1");
            _schoolKind = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND GRADE = '" + _grade + "' "));
            final String tmpD008Cd = "D" + _schoolKind + "08";
            String d008Namecd2CntStr = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT COUNT(*) FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = '" + tmpD008Cd + "' "));
            int d008Namecd2Cnt = Integer.parseInt(StringUtils.defaultIfEmpty(d008Namecd2CntStr, "0"));
            _d008Namecd1 = d008Namecd2Cnt > 0 ? tmpD008Cd : "D008";

            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
            if ("1".equals(_use_school_detail_gcm_dat)) {
                _attendParamMap.put("useTestCountflg", "TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV");
            } else {
                _attendParamMap.put("useTestCountflg", "TESTITEM_MST_COUNTFLG_NEW_SDIV");
            }
            try {
                final Map knjSchoolMstParamMap = new HashMap();
                if ("1".equals(_use_school_detail_gcm_dat)) {
                    knjSchoolMstParamMap.put("TABLENAME", "V_SCHOOL_GCM_MST");
                }
                final KNJSchoolMst _knjSchoolMst = new KNJSchoolMst(db2, _year, knjSchoolMstParamMap);
                _isLastSemester = _knjSchoolMst._semesterDiv.equals(_semester);
                _isHoutei = _knjSchoolMst.isHoutei();
                log.info(" isHoutei = " + _isHoutei);
                _useHouteiKekkaJisu = _isHoutei && !"1".equals(_use_school_detail_gcm_dat);
                if (_useHouteiKekkaJisu) {
                    final Map<String, String> houteiSyusuSemesterMap = new HashMap<String, String>();
                    try {
                        final String table;
                        if ("1".equals(_use_school_detail_gcm_dat)) {
                            table = "V_SCHOOL_GCM_MST";
                        } else {
                            table = "V_SCHOOL_MST";
                        }

                        final List rowList = KnjDbUtils.query(db2, " SELECT * FROM " + table + " WHERE YEAR = '" + _year + "' ");
                        final Map vSchoolMst;
                        if (rowList.size() > 1) {
                            vSchoolMst = (Map) KnjDbUtils.getKeyMap(rowList, "SCHOOL_KIND").get(_schoolKind);
                        } else {
                            vSchoolMst = KnjDbUtils.firstRow(rowList);
                        }

                        for (int semei = 1; semei <= 10; semei++) {
                            final String seme = String.valueOf(semei);
                            houteiSyusuSemesterMap.put(seme, KnjDbUtils.getString(vSchoolMst, "HOUTEI_SYUSU_SEMESTER" + seme));
                        }
                    } catch (Exception ex) {
                        log.error("exception!", ex);
                    }

                    String sdateSemester = null;
                    String edateSemester = null;
                    try {
//                        final StringBuffer stb = new StringBuffer();
//                        stb.append(" SELECT ");
//                        stb.append("     T1.SEMESTER, ");
//                        stb.append("     T1.SDATE, ");
//                        stb.append("     T1.EDATE, ");
//                        stb.append("     CASE WHEN '" + _sdate + "' < T1.SDATE THEN 1 END AS SDATE_BEFORE, ");
//                        stb.append("     CASE WHEN '" + _sdate + "' BETWEEN T1.SDATE AND T1.EDATE THEN 1 END AS IS_SDATE_SEMESTER, ");
//                        stb.append("     CASE WHEN '" + _edate + "' BETWEEN T1.SDATE AND T1.EDATE THEN 1 END AS IS_EDATE_SEMESTER, ");
//                        stb.append("     CASE WHEN T1.EDATE < '" + _edate + "' THEN 1 END AS EDATE_AFTER ");
//                        stb.append(" FROM V_SEMESTER_GRADE_MST T1 ");
//                        stb.append(" WHERE T1.YEAR = '" + _year + "' ");
//                        stb.append("   AND T1.SEMESTER <> '9' ");
//                        stb.append("   AND T1.GRADE = '" + _grade + "' ");
//                        stb.append(" ORDER BY T1.SEMESTER ");
//
//                        String firstSemester = null;
//                        String lastSemester = null;
//                        boolean sdateBeforeFirst = false;
//                        boolean edateAfterLast = false;
//                        for (final Iterator it = KnjDbUtils.query(db2, stb.toString()).iterator(); it.hasNext();) {
//                            final Map row = (Map) it.next();
//                            if (null == firstSemester) {
//                                firstSemester = KnjDbUtils.getString(row, "SEMESTER");
//                                sdateBeforeFirst = "1".equals(KnjDbUtils.getString(row, "SDATE_BEFORE"));
//                            }
//                            if ("1".equals(KnjDbUtils.getString(row, "IS_SDATE_SEMESTER"))) {
//                                sdateSemester = KnjDbUtils.getString(row, "SEMESTER");
//                            }
//                            lastSemester = KnjDbUtils.getString(row, "SEMESTER");
//                            edateAfterLast = "1".equals(KnjDbUtils.getString(row, "EDATE_AFTER"));
//                            if ("1".equals(KnjDbUtils.getString(row, "IS_EDATE_SEMESTER"))) {
//                                edateSemester = KnjDbUtils.getString(row, "SEMESTER");
//                            }
//                        }
//                        if (null == sdateSemester && sdateBeforeFirst) {
//                            sdateSemester = firstSemester;
//                        }
//                        if (null == edateSemester && edateAfterLast) {
//                            edateSemester = lastSemester;
//                        }

                        if ("2".equals(_dateDiv)) {
                            sdateSemester = _semester;
                            edateSemester = _semester;
                        } else {
                            sdateSemester = "1";
                            edateSemester = _knjSchoolMst._semesterDiv;
                        }

                        log.info(" sdate " + _sdate + " Semester = " + sdateSemester + ", edate " + _edate + " Semester = " + edateSemester);
                    } catch (Exception ex) {
                        log.error("exception!", ex);
                    }

                    String first = null;
                    String last = null;
                    if (NumberUtils.isDigits(sdateSemester)) {
                        first = sdateSemester;
                    }
                    if (NumberUtils.isDigits(edateSemester)) {
                        last = edateSemester;
                    }
                    _syusuInt = 0;
                    if (NumberUtils.isDigits(first) || NumberUtils.isDigits(last)) {
                        if (!NumberUtils.isDigits(first)) {
                            first = last;
                        } else if (!NumberUtils.isDigits(last)) {
                            last = first;
                        }
                        for (int seme = Integer.parseInt(first); seme <= Integer.parseInt(last); seme++) {
                            final String syusu = houteiSyusuSemesterMap.get(String.valueOf(seme));
                            if (!NumberUtils.isDigits(syusu)) {
                                log.warn("週数設定なし：" + seme + " = " + syusu);
                                continue;
                            }
                            _syusuInt += Integer.parseInt(syusu);
                        }
                    }
                    log.info(" syusuInt = " + _syusuInt);
                    if (!"2".equals(_dateDiv)) {
                        if (_syusuInt == 0 && NumberUtils.isDigits(_knjSchoolMst._jituSyusu)) {
                            _syusuInt = Integer.parseInt(_knjSchoolMst._jituSyusu);
                            log.info(" syusuInt (jituSyusu) = " + _syusuInt);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            final String z010 = getNameMstZ010(db2, "NAME1");
            _isChiyodaKudan = "chiyoda".equals(z010);
            _isNaraken = "naraken".equals(z010);
            _isReitaku = "reitaku".equals(z010);
            _is5dankai = "09".equals(_scoreDiv) || _isChiyodaKudan && GAKKIHYOKA_TESTCD.equals(_testcd);

            if (_isReitaku) {
                _outputSlump = request.getParameter("OUTPUT_SLUMP");
                _SlumpTensuInf = request.getParameter("SLUMP_TENSU_INF2");
            } else {
                _outputSlump = "";
                _SlumpTensuInf = "";
            }
            final Map d054Row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, " SELECT NAMECD2, NAME1 FROM NAME_MST WHERE NAMECD1 = 'D054' AND NAMECD2 = (SELECT MAX(NAMECD2) AS NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'D054') "));
            _d054Namecd2Max = KnjDbUtils.getString(d054Row, "NAMECD2");
            _sidouHyoji = KnjDbUtils.getString(d054Row, "NAME1");

            final Map d056 = KnjDbUtils.firstRow(KnjDbUtils.query(db2, " SELECT NAME1, NAME2 FROM NAME_MST WHERE NAMECD1 = 'D056' AND NAMECD2 = '" + _d056 + "' "));
            _d056Name1 = KnjDbUtils.getString(d056, "NAME1");
            _d056Name2 = KnjDbUtils.getString(d056, "NAME2");
            _testItem = getTestKindItem(db2);
            TestItem bef = getBeforeTestKindItem(db2, _setSchoolKind);
            if (null == bef) {
                bef = getBeforeTestKindItem(db2, "00");
            }
            _beforeTestItem = bef;
            _gradeName2 = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT T1.GRADE_NAME2 FROM SCHREG_REGD_GDAT T1 WHERE T1.YEAR = '" + _year + "' AND T1.GRADE = '" + _grade + "' "));
            _transferComment = getTransferComment(db2);
        }

        private void logOnce(final String message) {
            if (_logOnceSet.contains(message)) {
                return;
            }
            log.info(message);
            _logOnceSet.add(message);
        }

        private static String getDbPrginfoProperties(final DB2UDB db2, final String propName) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJD234V' AND NAME = '" + propName + "' "));
        }

        private static String getNameMstZ010(final DB2UDB db2, final String fieldname) {
            return KnjDbUtils.getString(KnjDbUtils.firstRow(KnjDbUtils.query(db2, "SELECT " + fieldname + " FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00'")), fieldname);
        }

        private boolean parameterHas(final String parameterName, final Enumeration enums) {
            for (;enums.hasMoreElements();) {
                final String name = (String) enums.nextElement();
                if (parameterName.equals(name)) {
                    return true;
                }
            }
            return false;
        }

        public String getRemarkTitle() {
            String rtn = "";
            if ("1".equals(_outputRemark)) {
                rtn = "出身学校";
            } else if ("2".equals(_outputRemark)) {
                rtn = "部活動";
            } else if ("3".equals(_outputRemark)) {
                rtn = "進路希望";
            }
            return rtn;
        }

        public boolean isYouTuishido(final String score) {
            boolean tuishidoFlg = false;
            if (_is5dankai) {
                return "1".equals(score);
            } else if (NumberUtils.isDigits(score) && Integer.parseInt(score) <= _shidouTensuInf2) {
                tuishidoFlg = true;
            }
            return tuishidoFlg;
        }

        private String getGradeTitle() {
            return !("00".equals(_major) || "0000".equals(_major)) ? "学科" : "学年";
        }

//        public boolean isTuishido(final SubclassScore subScore) {
//            if (null == _testItem._sidouInput) {
//                return false;
//            }
//            boolean tuishidoFlg = false;
//            if (SIDOU_INF_SCORE.equals(_testItem._sidouInputInf)) {
//                if (NumberUtils.isDigits(subScore._slumpScore) && Integer.parseInt(subScore._slumpScore) <= _shidouTensuInf2) {
//                    tuishidoFlg = true;
//                }
//            } else if (SIDOU_INF_MARK.equals(_testItem._sidouInputInf)) {
//                if (null != subScore._slumpMarkCd && subScore._slumpMarkCd.equals(_d054Namecd2Max)) {
//                    tuishidoFlg = true;
//                }
//            }
//            return tuishidoFlg;
//        }

        private static int toInt(final String s, final int defaultInt) {
            return NumberUtils.isNumber(s) ? Integer.parseInt(s) : defaultInt;
        }

        private String getTransferComment(final DB2UDB db2) {
            StringBuffer rtn = new StringBuffer();
            rtn.append("※()内は留学、休学");
            final String comma = "、";
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     T1.NAME1 ");
                stb.append(" FROM V_NAME_MST T1 ");
                stb.append(" WHERE T1.YEAR = '" + _year + "' ");
                stb.append("   AND T1.NAMECD1 = 'A004' ");
                stb.append("   AND T1.NAMECD2 NOT IN ('1', '2') ");
                stb.append("   AND T1.NAME1 IS NOT NULL ");
                stb.append(" ORDER BY T1.NAMECD2 ");

                for (final Map row : KnjDbUtils.query(db2, stb.toString())) {
                    if (StringUtils.isBlank(KnjDbUtils.getString(row, "NAME1"))) {
                        continue;
                    }
                    rtn.append(comma).append(StringUtils.trim(KnjDbUtils.getString(row, "NAME1")));
                }

            } catch (Exception ex) {
                log.fatal("exception!", ex);
            }
            rtn.append("者数");
            return rtn.toString();
        }

        private String getYearSdate(DB2UDB db2) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.SDATE ");
            stb.append(" FROM V_SEMESTER_GRADE_MST T1 ");
            stb.append(" WHERE T1.YEAR = '" + _year + "' ");
            stb.append("   AND T1.SEMESTER = '9' ");
            stb.append("   AND T1.GRADE = '" + _grade + "' ");

            return StringUtils.defaultString(KnjDbUtils.getOne(KnjDbUtils.query(db2, stb.toString())), _year + "-04-01");
        }

        private List<TestItem> getTsusanTestKindItemList(final DB2UDB db2) {
            List<TestItem> list = new ArrayList<TestItem>();
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     T1.YEAR, ");
                stb.append("     T1.SEMESTER, ");
                stb.append("     T1.TESTKINDCD, ");
                stb.append("     T1.TESTITEMCD, ");
                stb.append("     T1.SCORE_DIV, ");
                stb.append("     T1.TESTITEMNAME, ");
                stb.append("     T1.SIDOU_INPUT, ");
                stb.append("     T1.SIDOU_INPUT_INF ");
                if ("1".equals(_use_school_detail_gcm_dat)) {
                    stb.append(" FROM TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV T1 ");
                } else {
                    stb.append(" FROM TESTITEM_MST_COUNTFLG_NEW_SDIV T1 ");
                }
                stb.append(" WHERE (T1.YEAR < '" + _year + "' ");
                stb.append("     OR T1.YEAR = '" + _year + "' ");
                stb.append("   AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV <= '" + _semester + _testcd + "' ");
                stb.append("       ) ");
                if ("1".equals(_use_school_detail_gcm_dat)) {
                    stb.append("   AND T1.GRADE = '00' ");
                    stb.append("   AND T1.COURSECD || T1.MAJORCD = '" + _major + "' ");
                }
                stb.append("   AND (T1.SCORE_DIV = '01' ");
                stb.append("     OR T1.TESTKINDCD <> '99' AND T1.SCORE_DIV = '08') ");
                stb.append(" ORDER BY T1.YEAR, T1.SEMESTER, T1.TESTKINDCD, T1.TESTITEMCD, T1.SCORE_DIV ");

                log.info(" tsusan testitem sql ="  + stb.toString());

                for (final Map row : KnjDbUtils.query(db2, stb.toString())) {
                    final String year = KnjDbUtils.getString(row, "YEAR");
                    final String semester = KnjDbUtils.getString(row, "SEMESTER");
                    final String testkindcd = KnjDbUtils.getString(row, "TESTKINDCD");
                    final String testitemcd = KnjDbUtils.getString(row, "TESTITEMCD");
                    final String scoreDiv = KnjDbUtils.getString(row, "SCORE_DIV");
                    final String testitemname = KnjDbUtils.getString(row, "TESTITEMNAME");
                    final String sidouInput = KnjDbUtils.getString(row, "SIDOU_INPUT");
                    final String sidouInputInf = KnjDbUtils.getString(row, "SIDOU_INPUT_INF");
                    final TestItem testItem = new TestItem(
                            year, semester, "", testkindcd, testitemcd, scoreDiv, testitemname, sidouInput, sidouInputInf);
                    list.add(testItem);
                }

            } catch (Exception ex) {
                log.fatal("exception!", ex);
            }
            return list;
        }

        private TestItem getTestKindItem(DB2UDB db2) {
            TestItem testItem = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     T1.YEAR, ");
                stb.append("     T1.SEMESTER, ");
                stb.append("     T2.SEMESTERNAME, ");
                stb.append("     T1.TESTKINDCD, ");
                stb.append("     T1.TESTITEMCD, ");
                stb.append("     T1.SCORE_DIV, ");
                stb.append("     T1.TESTITEMNAME, ");
                stb.append("     T1.SIDOU_INPUT, ");
                stb.append("     T1.SIDOU_INPUT_INF ");
                if ("1".equals(_use_school_detail_gcm_dat)) {
                    stb.append(" FROM TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV T1 ");
                } else {
                    stb.append(" FROM TESTITEM_MST_COUNTFLG_NEW_SDIV T1 ");
                }
                stb.append(" LEFT JOIN V_SEMESTER_GRADE_MST T2 ON T2.YEAR = T1.YEAR ");
                stb.append("   AND T2.SEMESTER = T1.SEMESTER ");
                stb.append("   AND T2.GRADE = '" + _grade + "' ");
                stb.append(" WHERE T1.YEAR = '" + _year + "' ");
                stb.append("   AND T1.SEMESTER = '" + _semester + "' ");
                stb.append("   AND T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '" + _testcd + "' ");
                if ("1".equals(_use_school_detail_gcm_dat)) {
                    stb.append("   AND T1.GRADE = '00' ");
                    stb.append("   AND T1.COURSECD || T1.MAJORCD = '" + _major + "' ");
                }
                stb.append(" ORDER BY T1.SEMESTER, T1.TESTKINDCD, T1.TESTITEMCD, T1.SCORE_DIV ");

                log.debug(" testitem sql ="  + stb.toString());

                for (final Map row : KnjDbUtils.query(db2, stb.toString())) {
                    final String year = KnjDbUtils.getString(row, "YEAR");
                    final String semester = KnjDbUtils.getString(row, "SEMESTER");
                    final String semestername = KnjDbUtils.getString(row, "SEMESTERNAME");
                    final String testkindcd = KnjDbUtils.getString(row, "TESTKINDCD");
                    final String testitemcd = KnjDbUtils.getString(row, "TESTITEMCD");
                    final String scoreDiv = KnjDbUtils.getString(row, "SCORE_DIV");
                    final String testitemname = KnjDbUtils.getString(row, "TESTITEMNAME");
                    final String sidouInput = KnjDbUtils.getString(row, "SIDOU_INPUT");
                    final String sidouInputInf = KnjDbUtils.getString(row, "SIDOU_INPUT_INF");
                    testItem = new TestItem(
                            year, semester, semestername, testkindcd, testitemcd, scoreDiv, testitemname, sidouInput, sidouInputInf);
                }

            } catch (Exception ex) {
                log.fatal("exception!", ex);
            }
            log.debug(" testItem = " + testItem);
            return testItem;
        }

        private TestItem getBeforeTestKindItem(DB2UDB db2, final String setSchoolKind) {
            TestItem beforeTestItem = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT DISTINCT ");
                stb.append("     T1.YEAR, ");
                stb.append("     T1.SEMESTER, ");
                stb.append("     T2.SEMESTERNAME, ");
                stb.append("     T1.TESTKINDCD, ");
                stb.append("     T1.TESTITEMCD, ");
                stb.append("     T1.SCORE_DIV, ");
                stb.append("     T1.TESTITEMNAME, ");
                stb.append("     T1.SIDOU_INPUT, ");
                stb.append("     T1.SIDOU_INPUT_INF, ");
                stb.append("     CASE WHEN T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV >= '" + _semester + _testcd + "' THEN 1 END AS HON ");
                if ("1".equals(_use_school_detail_gcm_dat)) {
                    stb.append(" FROM TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV T1 ");
                    stb.append(" INNER JOIN ADMIN_CONTROL_GCM_SDIV_DAT T0 ON T0.YEAR = T1.YEAR ");
                } else {
                    stb.append(" FROM TESTITEM_MST_COUNTFLG_NEW_SDIV T1 ");
                    stb.append(" INNER JOIN ADMIN_CONTROL_SDIV_DAT T0 ON T0.YEAR = T1.YEAR ");
                }
                stb.append("   AND T0.SEMESTER = T1.SEMESTER ");
                stb.append("   AND T0.TESTKINDCD = T1.TESTKINDCD ");
                stb.append("   AND T0.TESTITEMCD = T1.TESTITEMCD ");
                stb.append("   AND T0.SCORE_DIV = T1.SCORE_DIV ");
                stb.append("   AND T0.CLASSCD = '00' ");
                stb.append("   AND T0.SCHOOL_KIND = '" + setSchoolKind +"' ");
                stb.append("   AND T0.CURRICULUM_CD = '00' ");
                stb.append("   AND T0.SUBCLASSCD = '000000' ");
                if ("1".equals(_use_school_detail_gcm_dat)) {
                    stb.append("   AND T0.GRADE = T1.GRADE ");
                    stb.append("   AND T0.COURSECD = T1.COURSECD ");
                    stb.append("   AND T0.MAJORCD = T1.MAJORCD ");
                }
                stb.append(" LEFT JOIN V_SEMESTER_GRADE_MST T2 ON T2.YEAR = T1.YEAR ");
                stb.append("   AND T2.SEMESTER = T1.SEMESTER ");
                stb.append("   AND T2.GRADE = '" + _grade + "' ");
                stb.append(" WHERE T1.YEAR = '" + _year + "' ");
                if (null != _testcdBefore) {
                    stb.append("   AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '" + _testcdBefore + "' ");
                } else {
                    stb.append("   AND NOT (T1.SEMESTER = '9' AND T1.TESTKINDCD = '99' AND T1.TESTITEMCD = '00' AND T1.SCORE_DIV = '08') "); // 学年評価をのぞく
                    stb.append("   AND NOT (T1.SEMESTER <> '9' AND T1.SCORE_DIV = '09') "); // 学年末以外の仮評価をのぞく
                    stb.append("   AND T1.SCORE_DIV = '" + _scoreDiv + "' ");
                }
                if ("1".equals(_use_school_detail_gcm_dat)) {
                    stb.append("   AND T0.GRADE = '00' ");
                    stb.append("   AND T0.COURSECD || T0.MAJORCD = '" + _major + "' ");
                }
                stb.append(" ORDER BY YEAR, SEMESTER, TESTKINDCD, TESTITEMCD, SCORE_DIV ");

                log.debug(" before testitem sql ="  + stb.toString());

                for (final Map row : KnjDbUtils.query(db2, stb.toString())) {
                    final String year = KnjDbUtils.getString(row, "YEAR");
                    final String semester = KnjDbUtils.getString(row, "SEMESTER");
                    final String semestername = KnjDbUtils.getString(row, "SEMESTERNAME");
                    final String testkindcd = KnjDbUtils.getString(row, "TESTKINDCD");
                    final String testitemcd = KnjDbUtils.getString(row, "TESTITEMCD");
                    final String scoreDiv = KnjDbUtils.getString(row, "SCORE_DIV");
                    final String testitemname = KnjDbUtils.getString(row, "TESTITEMNAME");
                    final String sidouInput = KnjDbUtils.getString(row, "SIDOU_INPUT");
                    final String sidouInputInf = KnjDbUtils.getString(row, "SIDOU_INPUT_INF");
                    if ("1".equals(KnjDbUtils.getString(row, "HON"))) {
                        break;
                    }
                    if (null == _testcdBefore && "99".equals(testkindcd)) {
                        continue;
                    }
                    beforeTestItem = new TestItem(
                            year, semester, semestername, testkindcd, testitemcd, scoreDiv, testitemname, sidouInput, sidouInputInf);
                }

            } catch (Exception ex) {
                log.fatal("exception!", ex);
            }
            log.info(" before testItem = " + beforeTestItem);
            return beforeTestItem;
        }

        private Map<String, Subclass> getSubclassMap(DB2UDB db2) {
            Map<String, Subclass> map = new HashMap<String, Subclass>();
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" WITH REPL AS ( ");
                stb.append(" SELECT DISTINCT '1' AS DIV, COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || COMBINED_SUBCLASSCD AS SUBCLASSCD FROM SUBCLASS_REPLACE_COMBINED_DAT WHERE YEAR = '" + _year + "' ");
                stb.append(" UNION ");
                stb.append(" SELECT DISTINCT '2' AS DIV, ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ATTEND_SUBCLASSCD AS SUBCLASSCD FROM SUBCLASS_REPLACE_COMBINED_DAT WHERE YEAR = '" + _year + "' ");
                stb.append(" ) ");
                stb.append(" SELECT ");
                stb.append("     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ");
                stb.append("     T2.CLASSABBV, ");
                stb.append("     T1.SUBCLASSNAME, ");
                stb.append("     T1.SUBCLASSABBV, ");
                stb.append("     CASE WHEN L1.SUBCLASSCD IS NOT NULL THEN 1 END AS IS_SAKI, ");
                stb.append("     CASE WHEN L2.SUBCLASSCD IS NOT NULL THEN 1 END AS IS_MOTO ");
                stb.append(" FROM SUBCLASS_MST T1 ");
                stb.append(" LEFT JOIN REPL L1 ON L1.DIV = '1' AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = L1.SUBCLASSCD ");
                stb.append(" LEFT JOIN REPL L2 ON L2.DIV = '2' AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = L2.SUBCLASSCD ");
                stb.append(" LEFT JOIN CLASS_MST T2 ON T2.CLASSCD = T1.CLASSCD ");
                stb.append("   AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ");

                log.debug(" subclass sql ="  + stb.toString());

                for (final Map row : KnjDbUtils.query(db2, stb.toString())) {
                    final String subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");
                    final String classabbv = KnjDbUtils.getString(row, "CLASSABBV");
                    final String subclassname = KnjDbUtils.getString(row, "SUBCLASSNAME");
                    final String subclassabbv = KnjDbUtils.getString(row, "SUBCLASSABBV");
                    final boolean isSaki = "1".equals(KnjDbUtils.getString(row, "IS_SAKI"));
                    final boolean isMoto = "1".equals(KnjDbUtils.getString(row, "IS_MOTO"));
                    map.put(subclasscd, new Subclass(subclasscd, classabbv, subclassname, subclassabbv, isSaki, isMoto));
                }

            } catch (Exception ex) {
                log.fatal("exception!", ex);
            }
            return map;
        }
    }
}

// eof

