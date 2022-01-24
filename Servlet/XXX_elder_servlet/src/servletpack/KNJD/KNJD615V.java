// kanji=漢字
/*
 * $Id: b4237f715b567e705022ec49b5b86ad869d53005 $
 *
 * 作成日: 2007/05/14
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import static servletpack.KNJZ.detail.KNJ_EditEdit.*;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJD.detail.RecordRankSdivSoutenDat;
import servletpack.KNJZ.detail.CsvUtils;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_EditKinsoku;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.StaffInfo;
import servletpack.KNJZ.detail.SvfField;
import servletpack.KNJZ.detail.SvfForm;
import servletpack.KNJZ.detail.dao.AttendAccumulate;
import servletpack.pdf.IPdf;
import servletpack.pdf.SvfPdf;

/**
 * 成績一覧表（成績判定会議用）を印刷します。
 * 成績処理改訂版。
 * 成績判定会議用をBASEとし、従来の成績一覧表の成績処理改訂版の印刷処理を行います。
 * @author nakamoto
 * @version $Id: b4237f715b567e705022ec49b5b86ad869d53005 $
 */
public class KNJD615V {
    private static final Log log = LogFactory.getLog(KNJD615V.class);

    private static final DecimalFormat DEC_FMT1 = new DecimalFormat("0.0");
    private static final DecimalFormat DEC_FMT2 = new DecimalFormat("0");
    private static final String SEMEALL = "9";

    static final String PATTERN1 = "1"; // 科目固定型
    static final String PATTERN2 = "2"; // 科目変動型
    static final String PATTERN3 = "3"; // 科目固定型（仮評定付）
    static final String PATTERN4 = "4"; // 成績の記録
    static final String PATTERN5 = "5"; // 欠課時数と出欠の記録

    private static final String OUTPUT_KJUN2 = "2";
    private static final String OUTPUT_KJUN3 = "3";
    private static final String OUTPUT_RANK1 = "1";
    private static final String OUTPUT_RANK2 = "2";
    private static final String OUTPUT_RANK3 = "3";
    private static final String OUTPUT_RANK4 = "4";

    private static final String OUTPUT_ORDER_2_SEISEKIJUN = "2";

    private static final String SIDOU_INPUT_INF_MARK = "1";
    private static final String SIDOU_INPUT_INF_SCORE = "2";

    private static final String SCORE_DIV_01 = "01";
    private static final String SCORE_DIV_02 = "02";
    private static final String SCORE_DIV_08 = "08";
    private static final String SCORE_DIV_09 = "09";

    private static final String ATTRIBUTE_KETTEN = "Paint=(1,90,1),Bold=1";
    private static final String ATTRIBUTE_KEKKAOVER = "Paint=(1,90,1),Bold=1";
    private static final String ATTRIBUTE_ELECTDIV = "Paint=(2,90,2),Bold=1";
    private static final String ATTRIBUTE_NORMAL = "Paint=(0,0,0),Bold=0";
    private static final String ATTRIBUTE_CENTERING = "Hensyu=3"; // 中央寄せ

    private static final String csv = "csv";

    private final static String DATA_SELECT_ALL = "0";
    private final static String DATA_SELECT_RYOUSEI = "1";
    private final static String DATA_SELECT_SUISEN = "2";
    private final static String DATA_SELECT_KAIGAI = "3";
    private final static String DATA_SELECT_IB = "4";
    private final static String DATA_SELECT_A_HOUSHIKI = "6";

    private final static String PRGID_KNJD615H = "KNJD615H";
    private final static String SUISEN = "4";
    private final static String KAIGAI = "3";
    private final static String A_HOUSHIKI = "('01','05','08')";
    private final static String IB_COURSECODE = "0002";
    private final static String PRGID_KNJD615P = "KNJD615P";

    private static final String ALL9 = "999999";
    protected boolean _hasData;

    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws ServletException, IOException {
        Vrw32alp svf = null;
        DB2UDB db2 = null;
        Param param = null;
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            // パラメータの取得
            param = createParam(request, db2);

            if (csv.equals(param._cmd)) {
                final List<List<String>> outputLines = new ArrayList<List<String>>();
                final Map csvParam = new HashMap();
                csvParam.put("HttpServletRequest", request);
                setOutputCsvLines(db2, param, outputLines);
                CsvUtils.outputLines(log, response, param._title + ".csv", outputLines, csvParam);
            } else {
                svf = new Vrw32alp();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
                svf.VrInit();
                svf.VrSetSpoolFileStream(response.getOutputStream());

                final IPdf ipdf = new SvfPdf(svf);

                response.setContentType("application/pdf");

                printMain(db2, param, ipdf);
            }

        } catch (final Exception ex) {
            log.error("error! ", ex);
        } finally {
            if (null != db2) {
                db2.commit();
                db2.close();
            }
            if (null != param) {
                param.close();

                if (csv.equals(param._cmd)) {
                } else {
                    if (!_hasData) {
                        svf.VrSetForm("MES001.frm", 0);
                        svf.VrsOut("note" , "note");
                        svf.VrEndPage();
                    }
                    int ret = svf.VrQuit();
                    if (ret == 0) {
                        log.info("===> VrQuit():" + ret);
                    }
                }
            }
        }
    }

    private void setOutputCsvLines(final DB2UDB db2, final Param param, final List<List<String>> outputList) {

        for (final String classSelected : param._classSelected) { //印刷対象HR組

            final StudentGroup studentGroup = new StudentGroup(classSelected, false);
            log.info(" classSelected = " + classSelected);

            final List<Course> courses = Course.createCourses(db2, param, classSelected);
            log.debug("コース数=" + courses.size());
            studentGroup._courses = courses;

            studentGroup.load(db2, param, null);

            final boolean isPrintHrCoursePage = param._isPrintGroupHr && OUTPUT_RANK3.equals(param._outputRank) && studentGroup._courses.size() > 1 && param._isOutputCoursePage;
            if (isPrintHrCoursePage) {
                // 印刷処理
                for (int i = 0; i < studentGroup._courses.size(); i++) {
                    final Form form = new Form();

                    final StudentGroup hrInfo1 = new StudentGroup(classSelected, true);
                    hrInfo1._isPrintHrCoursePage = true;
                    Course course = studentGroup._courses.get(i);
                    hrInfo1._courses = new ArrayList<Course>();
                    hrInfo1._courses.add(course);
                    hrInfo1._courseName = course._name;

                    hrInfo1.load(db2, param, course._code);

                    if (form.outputCsv(db2, outputList, param, hrInfo1)) {
                        _hasData = true;
                    }
                }
            } else {
                // 印刷処理
                final Form form = new Form();
                form.outputCsv(db2, outputList, param, studentGroup);
            }
        }
    }

    private void printMain(final DB2UDB db2, final Param param, final IPdf ipdf) {
        final List<ReportInfo> bkupReportInfo = new ArrayList<ReportInfo>();
        _hasData = false;
        for (final String classSelected : param._classSelected) { //印刷対象HR組
            final long start = System.currentTimeMillis();
            log.info(" classSelected = " + classSelected);
            final StudentGroup studentGroup = new StudentGroup(classSelected, false);

            final List<Course> courses = Course.createCourses(db2, param, studentGroup._cd);
            log.debug("コース数=" + courses.size());
            studentGroup._courses = courses;

            studentGroup.load(db2, param, null);
            final boolean isPrintHrCoursePage = param._isPrintGroupHr && OUTPUT_RANK3.equals(param._outputRank) && studentGroup._courses.size() > 1 && param._isOutputCoursePage;
            if (isPrintHrCoursePage) {
                // 印刷処理
                for (int i = 0; i < studentGroup._courses.size(); i++) {

                    final StudentGroup hrInfo1 = new StudentGroup(studentGroup._cd, true);
                    hrInfo1._isPrintHrCoursePage = true;
                    Course course = studentGroup._courses.get(i);
                    hrInfo1._courses = new ArrayList<Course>();
                    hrInfo1._courses.add(course);
                    hrInfo1._courseName = course._name;

                    hrInfo1.load(db2, param, course._code);

                    final Form form = new Form();
                    form.print(db2, ipdf, param, hrInfo1);
                    if (form._hasData) {
                        _hasData = true;
                    }
                }
            } else {
                // 印刷処理
                final Form form = new Form();
                form.print(db2, ipdf, param, studentGroup);
                if (form._hasData) {
                    _hasData = true;
                }
            }
            // クラス毎の情報を保持する。
            studentGroup.bkupInfo(param, bkupReportInfo);
            final long end = System.currentTimeMillis();
            param._elapsed.put(studentGroup._cd, new BigDecimal(end - start).divide(new BigDecimal(1000), 1, BigDecimal.ROUND_HALF_UP) + " [s]");

        }
        if (_hasData && param._isPrintGroupHr && "1".equals(param._printreport)) {
            printSummary(db2, param, bkupReportInfo, ipdf);
            _hasData = true;
        }
    }

    private void printSummary(final DB2UDB db2, final Param param, final List<ReportInfo> bkupReportInfo, final IPdf ipdf) {
        //サブクラスコード->対象クラスのリストを作成する。
        String formname = "KNJD615V_6.frm";
        ipdf.VrSetForm(formname, 1);

        Map<String, List<SubClass>> subclasscdlist = createSubclasscdList(bkupReportInfo);
        int colcnt = 1;
        int pagecnt = 0;
        boolean bprintoutwait = false;
        for (final String sclkey : subclasscdlist.keySet()) {
            final List<SubClass> infolist = subclasscdlist.get(sclkey);
            for (final SubClass outinfo : infolist) {
                final String scfieldname = "SUBCLASS_NAME" + (colcnt - (pagecnt * 20)) + "_" + ((outinfo._subclassname).length() <= 8 ? "1" : "2");
                ipdf.VrsOut(scfieldname, outinfo._subclassname);
                if (bprintoutwait && (colcnt % 20 == 0)) {
                    printSummarySub(db2, param, bkupReportInfo, ipdf, pagecnt, subclasscdlist);
                    pagecnt++;
                    bprintoutwait = false;
                } else {
                    bprintoutwait = true;
                }
                colcnt++;
                break; //タイトル部は、List内の先頭データでのみ科目名称を出力(同じ科目で並んでいるので、2番目以降は参照不要)
            }
        }
        if (bprintoutwait) {
            printSummarySub(db2, param, bkupReportInfo, ipdf, pagecnt, subclasscdlist);
        }
    }

    private void printSummarySub(final DB2UDB db2, final Param param, final List<ReportInfo> bkupReportInfo, final IPdf ipdf, final int pagecnt, Map<String, List<SubClass>> subclasscdlist) {
        //Title
        ipdf.VrsOut("TITLE", param._gdatgradename + " クラス成績表"); // タイトル
        ipdf.VrsOut("GRADE", param._testItem._testitemname); // 学期
        final String printDateTime = KNJ_EditDate.h_format_thi(param._logindate, 0);// + "　" + cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE);
        ipdf.VrsOut("DATE", printDateTime); // 学期

        final Set<String> sclkeys = subclasscdlist.keySet();
        //クラス毎に処理
        int linecnt = 1;
        for (final ReportInfo repoinfo : bkupReportInfo) {
            final String hrfieldname = "HR_NAME";
            ipdf.VrsOutn(hrfieldname, linecnt, repoinfo._hrName);
            final Set<String> sckeys = repoinfo._reportlist._subclasses.keySet();
            for (final String sckey : sckeys) {
                final SubClass sc = repoinfo._reportlist._subclasses.get(sckey);
                int cnt = 0;
                for (final String chkcode : sclkeys) {
                    if (sc._subclasscd.equals(chkcode)) {
                        break;
                    }
                    cnt++;
                }
                if (cnt < sclkeys.size() && (pagecnt) * 20 <= cnt && cnt < (pagecnt+1) * 20) {
                    final int cntwk = (cnt % 20) + 1 ;//0ベース->1ベースに修正。
                    //cnt列目に出力
                    final String staffstr = arrangeSomeStaffName(sc._staffname, sc._otherstafflist);
                    String chfieldname = "CHARGE" + cntwk + "_" + (getMS932ByteLength(staffstr) <= 6 ? "1" : "2");
                    ipdf.VrsOutn(chfieldname, linecnt, staffstr);
                    //staffstr
                    final String avefieldname = "AVERAGE" + cntwk;
                    ipdf.VrsOutn(avefieldname, linecnt, sc._scoreHrAverage);
                }
            }
            linecnt++;
        }
        if (linecnt > 1) {
            ipdf.VrEndPage();
            _hasData = true;
        }
    }

    private String arrangeSomeStaffName(final String staffname, final List<String> otherstafflist) {
        String retstr;
        if (otherstafflist.size() == 0) {
            retstr = staffname;
        } else {
            retstr = getmyouji(staffname);
            for (int cnt = 0;cnt < otherstafflist.size();cnt++) {
                retstr += "・" + getmyouji(otherstafflist.get(cnt));
            }
        }
        return retstr;
    }

    private String getmyouji(final String cutstaffname) {
        String[] staffnamecut;
        if (cutstaffname.indexOf("　") >= 0) {
            staffnamecut = StringUtils.split(cutstaffname, "　");
        } else {
            staffnamecut = StringUtils.split(cutstaffname, " ");
        }
        return staffnamecut[0];
    }

    private Map<String, List<SubClass>> createSubclasscdList(final List<ReportInfo> bkupReportInfo) {
        Map<String, List<SubClass>> retlist = new LinkedMap();
        for (final ReportInfo repoinfo : bkupReportInfo) {
            for (final SubClass sc : new ArrayList<SubClass>(repoinfo._reportlist._subclasses.values())) {
                if (!retlist.containsKey(sc._subclasscd)) {
                    retlist.put(sc._subclasscd, new ArrayList<SubClass>());
                }
                retlist.get(sc._subclasscd).add(sc);
            }
        }
        return retlist;
    }

    /**
     *
     * @param subclassCnt
     * @param subclassOrder
     * @param abbvName
     * @param abbvLen
     * @param abbvStrCnt
     * @return
     */
    private static ClassAbbvFieldSet setClassAbbv(final int subclassCnt, final int subclassOrder, final String abbvName, final int abbvLen, final int abbvStrCnt) {
        String fieldNum  = "";
        String setString = "";
        ClassAbbvFieldSet retData = new ClassAbbvFieldSet(fieldNum, setString);
        try {
            if (0 != subclassCnt && 0 != subclassOrder && null != abbvName && 0 != abbvLen && 0 != abbvStrCnt) {
                if (1 == subclassCnt) {
                    fieldNum  = (abbvLen > 6) ? "3": (abbvLen > 4) ? "2": "1";
                    setString = abbvName;
                } else if (2 == subclassCnt) {
                    if (abbvStrCnt > 4) {
                        fieldNum = "2";
                        setString = (1 == subclassOrder) ? abbvName.substring(0, 3): abbvName.substring(3) + "　";
                    } else {
                        fieldNum = "1";
                        if (3 == abbvStrCnt || 4 == abbvStrCnt) {
                            setString = (1 == subclassOrder) ? abbvName.substring(0, 2): abbvName.substring(2) + "　";
                        } else if (2 == abbvStrCnt || 1 == abbvStrCnt) {
                            setString = (1 == subclassOrder) ? "　"+ abbvName.substring(0, 1): abbvName.substring(1) + "　";
                        }
                    }
                }
                if (subclassCnt > 2) {
                    fieldNum = "1";
                    final boolean oddNumber = subclassCnt % 2 == 1;
                    final int herfNo        = subclassCnt / 2;
                    final int middleNo      = herfNo + 1;
                    final int noTextNo      = herfNo + 2;
                    final int evenStartNo   = herfNo - 1;

                    //科目数が奇数の時
                    if (oddNumber) {
                        if (5 == abbvStrCnt) {
                            if (subclassOrder < herfNo || noTextNo < subclassOrder) {
                                setString = "";
                            } else {
                                setString = (herfNo == subclassOrder) ? abbvName.substring(0, 2): (middleNo == subclassOrder) ? abbvName.substring(2, 4): abbvName.substring(4) + "　";
                            }
                        } else if (3 == abbvStrCnt || 4 == abbvStrCnt) {
                            if (subclassOrder < herfNo || noTextNo < subclassOrder) {
                                setString = "";
                            } else {
                                setString = (herfNo == subclassOrder) ? "　"+ abbvName.substring(0, 1): (middleNo == subclassOrder) ? abbvName.substring(1, 3): abbvName.substring(3) + "　";
                            }
                        } else if (2 == abbvStrCnt || 1 == abbvStrCnt) {
                            if (subclassOrder != middleNo) {
                                setString = "";
                            } else {
                                setString = abbvName.substring(0);
                            }
                        }

                    // 科目数が偶数の時
                    } else {
                        if (5 == abbvStrCnt) {
                            if (subclassOrder < evenStartNo || noTextNo <= subclassOrder) {
                                setString = "";
                            } else {
                                setString = (evenStartNo == subclassOrder) ? "　"+ abbvName.substring(0, 1): (herfNo == subclassOrder) ? abbvName.substring(1, 3): abbvName.substring(3);
                            }
                        } else if (3 == abbvStrCnt || 4 == abbvStrCnt) {
                            if (subclassOrder < herfNo || noTextNo <= subclassOrder) {
                                setString = "";
                            } else {
                                setString = (herfNo == subclassOrder) ? abbvName.substring(0, 2): abbvName.substring(2) + "　";
                            }
                        } else if (2 == abbvStrCnt || 1 == abbvStrCnt) {
                            if (subclassOrder < herfNo || noTextNo <= subclassOrder) {
                                setString = "";
                            } else {
                                setString = (herfNo == subclassOrder) ? "　"+ abbvName.substring(0, 1): abbvName.substring(1) + "　";
                            }
                        }
                    }
                }
            }
            retData = new ClassAbbvFieldSet(fieldNum, setString);
        } catch (final Exception ex) {
            log.error("classAbbvSetError!!", ex);
        }

        return retData;
    }

    private static String stringSum(final List<String> nums) {
        String n = null;
        for (final String num : nums) {
            n = addnum(n, num);
        }
        return n;
    }

    private static String sum(final List<Integer> nums) {
        if (nums.isEmpty()) {
            return "";
        }
        BigDecimal sum = new BigDecimal(0);
        for (final Integer num : nums) {
            sum = sum.add(new BigDecimal(num));
        }
        return sum.toString();
    }

    private static String avg(final List<Integer> nums) {
        final String sum = sum(nums);
        if (!NumberUtils.isNumber(sum)) {
            return sum;
        }
        return new BigDecimal(sum).divide(new BigDecimal(nums.size()), 1, BigDecimal.ROUND_HALF_UP).toString();
    }

    private static String max(final List<Integer> nums) {
        if (nums.isEmpty()) {
            return "";
        }
        Collections.sort(nums);
        return nums.get(nums.size() - 1).toString();
    }

    private static String min(final List<Integer> nums) {
        if (nums.isEmpty()) {
            return "";
        }
        Collections.sort(nums);
        return nums.get(0).toString();
    }

    private static Integer toInt(final String numstr, final Integer def) {
        if (NumberUtils.isNumber(numstr)) {
            return (int) Double.parseDouble(numstr);
        }
        return def;
    }

    private static BigDecimal toBigDecimal(final String numstr, final BigDecimal def) {
        if (NumberUtils.isNumber(numstr)) {
            return new BigDecimal(numstr);
        }
        return def;
    }

    public static <A, B> Set<B> getMappedTreeSet(final Map<A, Set<B>> map, final A key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new TreeSet<B>());
        }
        return map.get(key1);
    }

    public static <A, B> List<B> getMappedList(final Map<A, List<B>> map, final A key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new ArrayList<B>());
        }
        return map.get(key1);
    }

    public static <A, B, C> Map<B, C> getMappedHashMap(final Map<A, Map<B, C>> map, final A key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new HashMap<B, C>());
        }
        return map.get(key1);
    }

    private static String zeroToNull(final Param param, final int num) {
        return num == 0 ? ("1".equals(param._printKekka0) ? "0" : "") : String.valueOf(num);
    }

    private static String nullToBlank(final Object o) {
        return null == o ? null : o.toString();
    }

    private static String addnum(final String num1, final String num2) {
        if (!NumberUtils.isDigits(num1)) { return num2; }
        if (!NumberUtils.isDigits(num2)) { return num1; }
        return String.valueOf(Integer.parseInt(num1) + Integer.parseInt(num2));
    }

    private static String sishaGonyu(final BigDecimal bd) {
        return null == bd ? null : bd.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
    }

    private static String sishaGonyu2(final String val, final int keta) {
        if (!NumberUtils.isNumber(val)) {
            return null;
        }
        return new BigDecimal(val).setScale(keta, BigDecimal.ROUND_HALF_UP).toString();
    }

    private static String prepend(final String a, final String b) {
        if (StringUtils.isEmpty(b)) {
            return "";
        }
        return a + b;
    }

    private static String append(final String a, final String b) {
        if (StringUtils.isEmpty(a)) {
            return "";
        }
        return a + b;
    }

    private static String kakko(final String s) {
        return prepend("（", append(s, "）"));
    }

    private static String mkString(final Collection<String> list, final String comma) {
        final StringBuffer stb = new StringBuffer();
        String comma0 = "";
        for (final String s : list) {
            if (StringUtils.isEmpty(s)) {
                continue;
            }
            stb.append(comma0).append(s);
            comma0 = comma;
        }
        return stb.toString();
    }

    // StringUtils.defaultString
    private static String defstr(final Object str1, final String ... str2) {
        if (null != str1) {
            return str1.toString();
        }
        if (null != str2) {
            for (final String s : str2) {
                if (null != s) {
                    return s;
                }
            }
        }
        return "";
    }

    private static class Course {
        final String _grade;
        final String _hrclass;
        final String _code;
        final String _name;

        public Course(
                final String grade,
                final String hrclass,
                final String code,
                final String name
        ) {
            _grade = grade;
            _hrclass = hrclass;
            _code = code;
            _name = name;
        }

        private static List<Course> createCourses(final DB2UDB db2, final Param param, final String gradeHrclass) {
            final List<Course> rtn = new ArrayList<Course>();

            final StringBuffer stb = new StringBuffer();

            stb.append(" SELECT ");
            stb.append("     W1.GRADE, ");
            stb.append("     W1.HR_CLASS, ");
            stb.append("     W1.COURSECD || W1.MAJORCD || W1.COURSECODE as COURSECD, ");
            stb.append("     L1.COURSECODENAME ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT W1 ");
            stb.append("     LEFT JOIN COURSECODE_MST L1 ON L1.COURSECODE=W1.COURSECODE ");
            stb.append(" WHERE ");
            stb.append("     W1.YEAR = '" + param._year + "' AND ");
            if (!SEMEALL.equals(param._semester)) {
                stb.append(" W1.SEMESTER = '" + param._semester + "' AND ");
            } else {
                stb.append(" W1.SEMESTER = '" + param._semeFlg + "' AND ");
            }
            stb.append("     W1.GRADE || W1.HR_CLASS = '" + gradeHrclass + "' ");
            stb.append(" GROUP BY ");
            stb.append("     W1.GRADE, ");
            stb.append("     W1.HR_CLASS, ");
            stb.append("     W1.COURSECD, ");
            stb.append("     W1.MAJORCD, ");
            stb.append("     W1.COURSECODE, ");
            stb.append("     L1.COURSECODENAME ");

            final String sql = stb.toString();

            for (final Map row : KnjDbUtils.query(db2, sql)) {
                final String grade = KnjDbUtils.getString(row, "GRADE");
                final String hrclass = KnjDbUtils.getString(row, "HR_CLASS");
                final String coursecd = KnjDbUtils.getString(row, "COURSECD");
                final String name = KnjDbUtils.getString(row, "COURSECODENAME");

                final Course course = new Course(grade, hrclass, coursecd, name);

                rtn.add(course);
            }
            return rtn;
        }

        public String toString() {
            return _code + ":" + _name;
        }
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * <<学級のクラス>>。
     */
    private static class StudentGroup implements Comparable<StudentGroup> {
        static final String AVG_FLG_HR = "HR";
        static final String AVG_FLG_GRADE = "GRADE";
        static final String AVG_FLG_COURSE = "COURSE";
        static final String AVG_FLG_MAJOR = "MAJOR";

        final String _cd;
        final boolean _isCourse;
        String _courseName;
        private List<Course> _courses;
        private boolean _isPrintHrCoursePage;
        private String _staffname;
        private String _hrName;
        private String _gradeName1;
        private String _majorname;
        private String _coursecodename;

        private List<Student> _students = Collections.emptyList();

        private final Map<String, SubClass> _subclasses = new TreeMap<String, SubClass>();
        private Map<String, String> _recordRankSdivAverageMap;
        private List<String> _attendHrRemark = Collections.EMPTY_LIST;
        private RecordRankSdivSoutenDat.Store recordRankSdivSoutenDatStore;

        public StudentGroup(final String cd, final boolean isCourse) {
            _cd = cd;
            _isCourse = isCourse;
        }

        public void load(final DB2UDB db2, final Param param, final String course) {
            if (param._isPrintGroupGrade) {
                _gradeName1 = defstr(KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT GRADE_NAME1 FROM SCHREG_REGD_GDAT WHERE YEAR = '" + param._year + "' AND GRADE = '" + _cd + "' ")));
            } else if (param._isPrintGroupCourse) {
                _majorname = defstr(KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT MAJORNAME FROM MAJOR_MST WHERE COURSECD = '" + _cd.substring(0, 1) + "' AND MAJORCD = '" + _cd.substring(1, 4) + "' ")));
                _coursecodename = defstr(KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT COURSECODENAME FROM COURSECODE_MST WHERE COURSECODE = '" + _cd.substring(4) + "' ")));
            } else if (param._isPrintGroupHr) {
                final Map<String, String> row = Hrclass_Staff(db2, param);
                _staffname = KnjDbUtils.getString(row, "STAFFNAME");
                _hrName = KnjDbUtils.getString(row, "HR_NAME");
            }

            if (param._isOutputDebug) {
                log.info(" load student.");
            }
            _students = loadStudents(db2, param, course);

            final Map<String, Student> studentMap = new HashMap();
            for (final Student student : _students) {
                studentMap.put(student._schregno, student);
            }
            if (param._isOutputDebug) {
                log.info(" load rank.");
            }
            loadRank(db2, param, studentMap);

            if (0 < param._rankStart || param._rankEnd < Integer.MAX_VALUE) {
                final List<Student> notPrintStudentList = new ArrayList<Student>();
                for (final Student student : _students) {
                    final int rank = student.getRankForCompare(param);
                    if (0 != rank && (rank < param._rankStart || param._rankEnd < rank)) {
                        notPrintStudentList.add(student);
                    }
                }
                if (0 < notPrintStudentList.size()) {
                    _students.removeAll(notPrintStudentList);
                    log.info(" not print student size = " + notPrintStudentList.size() + ", print size = " + _students.size());

                    studentMap.clear();
                    for (final Student student : _students) {
                        studentMap.put(student._schregno, student);
                    }
                }
                if (!OUTPUT_ORDER_2_SEISEKIJUN.equals(param._outputOrder) && !notPrintStudentList.isEmpty() && ("1".equals(param._notEmptyLine) || !param._isPrintGroupHr)) {
                    // 表示を詰める
                    for (int i = 0; i < _students.size(); i++) {
                        final Student student = _students.get(i);
                        student._gnum = i + 1;
                    }
                }
            }

            setStudentsInfo(db2, param, _students);
            loadAttend(db2, param, _students);
            if (param._isOutputDebug) {
                log.info(" load attendRemark.");
            }
            loadAttendRemark(db2, param, studentMap);
            if (param._isOutputDebug) {
                log.info(" load hrclassAverage.");
            }
            _recordRankSdivAverageMap = getRecordRankSdivAverageMap(db2, param, _students);

            loadScoreDetail(db2, param, studentMap);
            if (param._isOutputDebug) {
                log.info(" set subclass average.");
            }
            setSubclassAverage(param, _students);
            setSubclassGradeAverage(db2, param);
        }

        public String getName(final Param param) {
            String rtn = "";
            if (param._isPrintGroupGrade) {
                rtn = _gradeName1;
            } else if (param._isPrintGroupCourse) {
                rtn = _majorname + _coursecodename;
            } else if (param._isPrintGroupHr) {
                rtn = _hrName + (_isCourse ? "(" + defstr(_courseName) + ")" : "");
            }
            return rtn;
        }

        public Map<String, String> Hrclass_Staff(final DB2UDB db2, final Param param) {
            String sql = "";
            sql = "SELECT "
                    + "REGDH.HR_NAME,"
                    + "REGDH.HR_NAMEABBV,"
                    + "STFM.STAFFNAME "
                + "FROM "
                    + "SCHREG_REGD_HDAT REGDH "
                    + "LEFT JOIN STAFF_MST STFM ON STFM.STAFFCD = REGDH.TR_CD1 "
                + "WHERE "
                        + "REGDH.YEAR = '" + param._year + "' "
                    + "AND REGDH.GRADE || REGDH.HR_CLASS = '" + _cd + "' ";
            if (param._semester.equals(SEMEALL))	sql = sql
                    + "AND REGDH.SEMESTER = '" + param._semeFlg + "'";
            else						sql = sql
                    + "AND REGDH.SEMESTER = '" + param._semester + "'";

            return KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql));
        }

        private void loadAttendRemark(final DB2UDB db2, final Param param, final Map<String, Student> studentMap) {
            final StringBuffer sql = new StringBuffer();
            if ("1".equals(param._knjd615vSelectBikoTermType)) {
                sql.append("     SELECT ");
                //sql.append("         T2.SCHOOL_KIND, ");
                sql.append("         T2.COLLECTION_CD, ");
                sql.append("         T1.SCHREGNO, ");
                sql.append("         T1.ATTEND_REMARK AS REMARK ");
                sql.append("     FROM ");
                sql.append("         ATTEND_REASON_COLLECTION_DAT T1 ");
                sql.append("         INNER JOIN ATTEND_REASON_COLLECTION_MST T2 ");
                sql.append("             ON T1.YEAR = T2.YEAR ");
                sql.append("             AND T1.SCHOOL_KIND = T2.SCHOOL_KIND ");
                sql.append("             AND T1.COLLECTION_CD = T2.COLLECTION_CD ");
                sql.append("         LEFT JOIN SCHREG_REGD_GDAT GDAT ");
                sql.append("           ON GDAT.YEAR = T1.YEAR ");
                sql.append("          AND GDAT.SCHOOL_KIND = T1.SCHOOL_KIND ");
                sql.append("     WHERE ");
                sql.append("         T1.YEAR = '" + param._year + "' ");
                sql.append("         AND GDAT.GRADE = '" + param._grade + "' ");
                sql.append("         AND T1.COLLECTION_CD = '" + param._bikoTermType + "' ");
            } else {
                sql.append(" SELECT ");
                sql.append("     SD.SCHREGNO, ");
                sql.append("     RMK.SEMESTER, ");
                sql.append("     RMK.MONTH, ");
                sql.append("     RMK.REMARK1 AS REMARK ");
                sql.append(" FROM ");
                sql.append("     SCHREG_REGD_DAT SD ");
                sql.append(" INNER JOIN ATTEND_SEMES_REMARK_DAT RMK ON SD.YEAR = RMK.YEAR ");
                sql.append("      AND SD.SCHREGNO = RMK.SCHREGNO ");
                if ("2".equals(param._bikoKind)) {
                    sql.append("      AND RMK.SEMESTER >= '" + (SEMEALL.equals(param._semester) ? param._semeFlg : param._semester) + "' ");
                }
                sql.append("      AND INT(RMK.MONTH) + CASE WHEN INT(RMK.MONTH) < 4 THEN 12 ELSE 0 END <= " + param.getAttendRemarkMonth(db2, param._date) + " ");
                sql.append(" WHERE ");
                sql.append("     SD.YEAR = '" + param._year + "' ");
                sql.append("     AND SD.SEMESTER = '" + (SEMEALL.equals(param._semester) ? param._semeFlg : param._semester) + "' ");
                sql.append("     AND SD.GRADE = '" + param._grade + "' ");
                sql.append(" ORDER BY SD.SCHREGNO, RMK.SEMESTER, INT(RMK.MONTH) + CASE WHEN INT(RMK.MONTH) < 4 THEN 12 ELSE 0 END  ");
            }
            for (final Map row : KnjDbUtils.query(db2, sql.toString())) {
                final Student student = getStudent(studentMap, KnjDbUtils.getString(row, "SCHREGNO"));
                if (null == student) {
                    continue;
                }
                final String remark = KnjDbUtils.getString(row, "REMARK");
                if (null == remark && !"1".equals(param._knjd615vPrintNullRemark)) {
                    continue;
                }
                if ("3".equals(param._bikoKind) || null == student._attendSemesRemarkDatRemark1) {
                    student._attendSemesRemarkDatRemark1 = "";
                } else {
                    student._attendSemesRemarkDatRemark1 += null == remark ? "" : " ";
                }
                student._attendSemesRemarkDatRemark1 += defstr(remark);
            }

            final Set<String> argSet = new HashSet<String>();
            for (final Student student : studentMap.values()) {
                final String a = "('" + param._grade + "', '" + student._hrClass + "')";
                argSet.add(a);
            }
            final String argGradeHrclass = mkString(argSet, ", ");
            if (argGradeHrclass.length() > 0) {
                _attendHrRemark = new ArrayList<String>();
                if ("1".equals(param._useAttendSemesHrRemark) && param._hasATTEND_SEMES_REMARK_HR_DAT) {
                    final StringBuffer hrsql = new StringBuffer();
                    hrsql.append(" SELECT ");
                    hrsql.append("     RMK.SEMESTER, ");
                    hrsql.append("     RMK.MONTH, ");
                    hrsql.append("     RMK.REMARK1 AS REMARK ");
                    hrsql.append(" FROM ");
                    hrsql.append("     ATTEND_SEMES_REMARK_HR_DAT RMK ");
                    hrsql.append(" WHERE ");
                    hrsql.append("     RMK.YEAR = '" + param._year + "' ");
                    if ("2".equals(param._bikoKind)) {
                        hrsql.append("      AND RMK.SEMESTER >= '" + (SEMEALL.equals(param._semester) ? param._semeFlg : param._semester) + "' ");
                    }
                    hrsql.append("     AND INT(RMK.MONTH) + CASE WHEN INT(RMK.MONTH) < 4 THEN 12 ELSE 0 END <= " + param.getAttendRemarkMonth(db2, param._date) + " ");
                    hrsql.append("     AND (RMK.GRADE, RMK.HR_CLASS) IN (VALUES " + argGradeHrclass.toString() + ") ");
                    hrsql.append(" ORDER BY RMK.SEMESTER, INT(RMK.MONTH) + CASE WHEN INT(RMK.MONTH) < 4 THEN 12 ELSE 0 END  ");

                    if (param._isOutputDebugQuery) {
                        log.info(" hrsql = " + hrsql);
                    }
                    for (final Map row : KnjDbUtils.query(db2, hrsql.toString())) {
                        final String remark = KnjDbUtils.getString(row, "REMARK");
                        if (null != remark) {
                            _attendHrRemark.add(remark);
                        }
                    }
                }

                if (param._hasATTEND_BATCH_INPUT_HDAT) {
                    final StringBuffer hrsql = new StringBuffer();
                    hrsql.append(" SELECT ");
                    hrsql.append("     T1.SEQNO ");
                    hrsql.append("   , T1.FROM_DATE ");
                    hrsql.append("   , T1.TO_DATE ");
                    hrsql.append("   , T1.DI_REMARK ");
                    hrsql.append(" FROM ATTEND_BATCH_INPUT_HDAT T1 ");
                    hrsql.append(" INNER JOIN SEMESTER_MST SEME ON SEME.YEAR = T1.YEAR AND SEME.SEMESTER <> '9' AND T1.FROM_DATE BETWEEN SEME.SDATE AND SEME.EDATE ");
                    if ("2".equals(param._bikoKind)) {
                        hrsql.append("      AND SEME.SEMESTER >= '" + (SEMEALL.equals(param._semester) ? param._semeFlg : param._semester) + "' ");
                    }
                    hrsql.append(" LEFT JOIN ATTEND_BATCH_INPUT_HR_DAT L1 ON ");
                    hrsql.append("      L1.YEAR = T1.YEAR ");
                    hrsql.append("    AND L1.SEQNO = T1.SEQNO ");
                    hrsql.append("    AND (L1.GRADE, L1.HR_CLASS) IN (VALUES " + argGradeHrclass.toString() + ") ");
                    hrsql.append("  WHERE ");
                    hrsql.append("      T1.YEAR = '" + param._year + "' ");
                    hrsql.append("      AND ( T1.INPUT_TYPE = '1' OR T1.INPUT_TYPE = '3' AND L1.YEAR IS NOT NULL) ");
                    hrsql.append("  ORDER BY ");
                    hrsql.append("      T1.FROM_DATE, T1.SEQNO ");

                    if (param._isOutputDebugQuery) {
                        log.info(" hrsql 2 = " + hrsql);
                    }
                    for (final Map row : KnjDbUtils.query(db2, hrsql.toString())) {
                        final String remark = KnjDbUtils.getString(row, "DI_REMARK");
                        if (null == remark) {
                            continue;
                        }
                        final String fromDate = KnjDbUtils.getString(row, "FROM_DATE");
                        final String toDate = KnjDbUtils.getString(row, "TO_DATE");
                        if (null != fromDate && null != toDate) {
                            if (fromDate.equals(toDate)) {
                                _attendHrRemark.add(KNJ_EditDate.h_format_JP_MD(fromDate) + " " + remark);
                            } else {
                                _attendHrRemark.add(KNJ_EditDate.h_format_JP_MD(fromDate) + "～" + KNJ_EditDate.h_format_JP_MD(toDate) + " " + remark);
                            }
                        } else {
                            _attendHrRemark.add(remark);
                        }
                    }
                }
            }
        }

        private List<Student> loadStudents(final DB2UDB db2, final Param param, final String course) {
            final List<Student> students = new LinkedList();
            final StringBuffer stb = new StringBuffer();

            stb.append(" SELECT ");
            stb.append("    REGD.SCHREGNO, ");
            stb.append("    REGD.HR_CLASS, ");
            stb.append("    REGDH.HR_NAME, ");
            stb.append("    REGD.ATTENDNO, ");
            if (PRGID_KNJD615H.equals(param._prgId)) {
                stb.append("    Z002.NAME1 AS SEX, ");
                stb.append("    CASE WHEN DOMI.SCHREGNO IS NOT NULL THEN '寮' ELSE '' END AS RYOU, ");
                stb.append("    CASE WHEN BASE_DE.BASE_REMARK1 = '" + KAIGAI + "' THEN '海' ELSE '' END AS KAIGAI, ");
                stb.append("    CASE WHEN BASE_DE.BASE_REMARK1 = '" + SUISEN + "' THEN '推' ELSE '' END AS SUISEN, ");
                stb.append("    CASE WHEN REGD.COURSECODE = '" + IB_COURSECODE + "' THEN 'IB' ELSE '' END AS IB, ");
                stb.append("    CASE WHEN BASE_DE.BASE_REMARK2 IN " + A_HOUSHIKI + " THEN 'Ａ' ELSE '' END AS AHOUSHIKI, ");
            } else {
                stb.append("    Z002.ABBV1 AS SEX, ");
                stb.append("    '' AS RYOU, ");
                stb.append("    '' AS KAIGAI, ");
                stb.append("    '' AS SUISEN, ");
                stb.append("    '' AS IB, ");
                stb.append("    '' AS AHOUSHIKI, ");
            }
            stb.append("    REGD.COURSECD || REGD.MAJORCD AS MAJOR, ");
            stb.append("    REGD.COURSECD || REGD.MAJORCD || REGD.COURSECODE AS COURSE, ");
            stb.append("    CCM.COURSECODENAME ");
            if (param._isNaraken) {
                stb.append("     , REFUS.SCHREGNO AS REFUSAL ");
            }
            stb.append(" FROM ");
            stb.append("    SCHREG_REGD_DAT REGD ");
            stb.append("    INNER JOIN SCHREG_BASE_MST W3 ON W3.SCHREGNO = REGD.SCHREGNO ");
            stb.append("    LEFT JOIN SCHREG_REGD_HDAT REGDH ON REGD.YEAR = REGDH.YEAR ");
            stb.append("         AND REGD.SEMESTER = REGDH.SEMESTER ");
            stb.append("         AND REGD.GRADE = REGDH.GRADE ");
            stb.append("         AND REGD.HR_CLASS = REGDH.HR_CLASS ");
            stb.append("    LEFT JOIN SCHREG_BASE_DETAIL_MST BASE_DE ON REGD.SCHREGNO = BASE_DE.SCHREGNO ");
            stb.append("         AND BASE_DE.BASE_SEQ = '014' ");
            stb.append("    LEFT JOIN NAME_MST Z002 ON Z002.NAMECD1 = 'Z002' AND W3.SEX = Z002.NAMECD2 ");
            stb.append("    LEFT JOIN COURSECODE_MST CCM ON CCM.COURSECODE = REGD.COURSECODE ");
            if (PRGID_KNJD615H.equals(param._prgId)) {
                stb.append("    LEFT JOIN ( ");
                stb.append("            SELECT ");
                stb.append("                * ");
                stb.append("            FROM ");
                stb.append("                SCHREG_DOMITORY_HIST_DAT ");
                stb.append("            WHERE ");
                stb.append("                '" + param._logindate + "' BETWEEN DOMI_ENTDAY AND VALUE(DOMI_OUTDAY, '9999-12-31') ");
                stb.append("    ) DOMI ON REGD.SCHREGNO = DOMI.SCHREGNO ");
            }
            if (param._isNaraken) {
                stb.append("     LEFT JOIN SCHREG_SCHOOL_REFUSAL_DAT REFUS ON REFUS.YEAR = REGD.YEAR ");
                stb.append("          AND REFUS.SCHREGNO = REGD.SCHREGNO ");
            }
            stb.append(" WHERE ");
            stb.append("    REGD.YEAR = '" + param._year + "' ");
            if (!SEMEALL.equals(param._semester)) {
                stb.append("    AND REGD.SEMESTER = '" + param._semester + "' ");
            } else {
                stb.append("    AND REGD.SEMESTER = '" + param._semeFlg + "' ");
            }
            if (param._isPrintGroupGrade) {
                stb.append("    AND REGD.GRADE = '" + _cd + "' ");
                if (PRGID_KNJD615P.equals(param._prgId) && OUTPUT_ORDER_2_SEISEKIJUN.equals(param._outputOrder)) {
                    stb.append("         AND REGD.GRADE || REGD.HR_CLASS IN " + SQLUtils.whereIn(true, param._paramClassSelected));
                }
            } else if (param._isPrintGroupCourse) {
                stb.append("    AND REGD.GRADE = '" + param._grade + "' AND REGD.COURSECD = '" + _cd.substring(0, 1) + "' AND REGD.MAJORCD = '" + _cd.substring(1, 4) + "' AND REGD.COURSECODE = '" + _cd.substring(4) + "' ");
            } else {
                stb.append("    AND REGD.GRADE = '" + _cd.substring(0, 2) + "' AND REGD.HR_CLASS = '" + _cd.substring(2) + "' ");
            }
            if (PRGID_KNJD615H.equals(param._prgId)) {
                String sbuf = "";
                String sep = "";
                if ("1".equals(param._dataSelChk_Ryousei)) {
                    sbuf += "    " + sep + " DOMI.SCHREGNO IS NOT NULL ";
                    sep = "OR";
                }
                if ("1".equals(param._dataSelChk_Kaigai)) {
                    sbuf += "    " + sep + " BASE_DE.BASE_REMARK1 = '" + KAIGAI + "' ";
                    sep = "OR";
                }
                if ("1".equals(param._dataSelChk_Suisen)) {
                    sbuf += "    " + sep + " BASE_DE.BASE_REMARK1 = '" + SUISEN + "' ";
                    sep = "OR";
                }
                if ("1".equals(param._dataSelChk_IBCourse)) {
                    sbuf += "    " + sep + " REGD.COURSECODE = '" + IB_COURSECODE + "' ";
                    sep = "OR";
                }
                if ("1".equals(param._dataSelChk_AType)) {
                    sbuf += "    " + sep + " BASE_DE.BASE_REMARK2 IN " + A_HOUSHIKI + " ";
                    sep = "OR";
                }
                if (!"".equals(sbuf)) {
                    sbuf = " AND (" + sbuf + ") ";
                    stb.append(sbuf);
                }
            }
            if (!param._isPrintGroupGrade && !param._isPrintGroupCourse && null != course) {
                stb.append("    AND REGD.COURSECD || REGD.MAJORCD || REGD.COURSECODE = '" + course + "' ");
            }
            stb.append(" ORDER BY ");
            stb.append("    REGD.GRADE, REGD.HR_CLASS, REGD.ATTENDNO");

            final String sql = stb.toString();
            log.debug("sql = "+ sql);
            int gnum = 0;
            final Integer zero = new Integer(0);
            for (final Map row : KnjDbUtils.query(db2, sql)) {
                if ("1".equals(param._notEmptyLine) || !param._isPrintGroupHr) {
                    gnum = students.size() + 1;
                } else {
                    gnum = KnjDbUtils.getInt(row, "ATTENDNO", zero).intValue();
                }
                final String schregno = KnjDbUtils.getString(row, "SCHREGNO");
                final String sex = KnjDbUtils.getString(row, "SEX");
                final String coursecodename = KnjDbUtils.getString(row, "COURSECODENAME");
                final String ryou = KnjDbUtils.getString(row, "RYOU");
                final String kaigai = KnjDbUtils.getString(row, "KAIGAI");
                final String suisen = KnjDbUtils.getString(row, "SUISEN");
                final String ib = KnjDbUtils.getString(row, "IB");
                final String ahoushiki = KnjDbUtils.getString(row, "AHOUSHIKI");
                String schoolRefusal = null;
                if (param._isNaraken) {
                    schoolRefusal = KnjDbUtils.getString(row, "REFUSAL");
                }
                final String hrClass = KnjDbUtils.getString(row, "HR_CLASS");
                final String hrName = KnjDbUtils.getString(row, "HR_NAME");
                final String regdCourse = KnjDbUtils.getString(row, "COURSE");
                final String major = KnjDbUtils.getString(row, "MAJOR");
                final Student student = new Student(schregno, hrClass, hrName, regdCourse, major, sex, coursecodename, this, ryou, kaigai, suisen, ib, ahoushiki, schoolRefusal);
                student._gnum = gnum;
                students.add(student);
            }
            return students;
        }

        private void setStudentsInfo(final DB2UDB db2, final Param param, final List<Student> students) {

            final StringBuffer stb = new StringBuffer();
            stb.append("SELECT  REGD.SCHREGNO, REGD.ATTENDNO, BASE.NAME, BASE.NAME_ENG, REGDH.HR_NAME ");
            stb.append("      , ENTGRD.ENT_DIV ");
            stb.append("      , ENTGRD.GRD_DIV ");
            stb.append("      , CASE WHEN (ENTGRD.ENT_DIV IN ('4','5','7') AND ENTGRD.ENT_DATE >= W2.SDATE) THEN ENTGRD.ENT_DATE ELSE NULL END AS ENT_DATE ");  //卒業チェック
            stb.append("      , CASE WHEN (ENTGRD.GRD_DIV IN ('2','3','6','7') AND ENTGRD.GRD_DATE <= W2.EDATE) THEN ENTGRD.GRD_DATE ELSE NULL END AS GRD_DATE ");  //入学チェック
            stb.append("      , TRF.TRANSFERCD ");
            stb.append("      , TRF.TRANSFER_SDATE ");
            stb.append("      , TRF.TRANSFER_EDATE ");
            if (param._isTokiwagi) {
                stb.append("     , NMA044.ABBV1 AS SCHOLARSHIP_ABBV1 ");
            }
            stb.append("FROM   SCHREG_REGD_DAT REGD ");
            stb.append("INNER  JOIN SCHREG_REGD_HDAT  REGDH ON REGDH.YEAR = REGD.YEAR AND REGDH.SEMESTER = REGD.SEMESTER AND REGDH.GRADE = REGD.GRADE AND REGDH.HR_CLASS = REGD.HR_CLASS ");
            stb.append("INNER  JOIN V_SEMESTER_GRADE_MST    W2 ON W2.YEAR = REGD.YEAR AND W2.SEMESTER = '" + param._semester + "' AND W2.GRADE = REGD.GRADE ");
            stb.append("INNER  JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ");
            stb.append("LEFT   JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = REGD.YEAR AND GDAT.GRADE = REGD.GRADE ");
            stb.append("LEFT   JOIN SCHREG_ENT_GRD_HIST_DAT ENTGRD ON ENTGRD.SCHREGNO = REGD.SCHREGNO ");
            stb.append("                              AND ENTGRD.SCHOOL_KIND = GDAT.SCHOOL_KIND ");
            stb.append("                              AND ((ENTGRD.GRD_DIV IN ('2','3','6','7') AND ENTGRD.GRD_DATE <= W2.EDATE) ");
            stb.append("                                OR (ENTGRD.ENT_DIV IN ('4','5','7') AND ENTGRD.ENT_DATE >= W2.SDATE)) ");
            stb.append("LEFT   JOIN SCHREG_TRANSFER_DAT TRF ON TRF.SCHREGNO = REGD.SCHREGNO ");
            stb.append("                                  AND (TRF.TRANSFERCD IN ('1','2') ");
            stb.append("                                  AND ( W2.EDATE BETWEEN TRF.TRANSFER_SDATE AND TRF.TRANSFER_EDATE ");
            stb.append("                                        OR TRF.TRANSFER_SDATE BETWEEN W2.SDATE AND W2.EDATE ");
            stb.append("                                        OR TRF.TRANSFER_EDATE BETWEEN W2.SDATE AND W2.EDATE) ) ");
            if (param._isTokiwagi) {
                stb.append("     LEFT JOIN SEMESTER_MST SEME2 ON SEME2.YEAR = REGD.YEAR AND SEME2.SEMESTER = REGD.SEMESTER ");
                stb.append("     LEFT JOIN SCHREG_SCHOLARSHIP_HIST_DAT TSCHOL ON TSCHOL.SCHREGNO = REGD.SCHREGNO ");
                stb.append("          AND ( ");
                stb.append("              TSCHOL.FROM_DATE BETWEEN SEME2.SDATE AND SEME2.EDATE ");
                stb.append("           OR TSCHOL.TO_DATE BETWEEN SEME2.SDATE AND SEME2.EDATE ");
                stb.append("           OR SEME2.SDATE BETWEEN TSCHOL.FROM_DATE AND VALUE(TSCHOL.TO_DATE, '9999-12-31') ");
                stb.append("           OR SEME2.EDATE BETWEEN TSCHOL.FROM_DATE AND VALUE(TSCHOL.TO_DATE, '9999-12-31') ");
                stb.append("              ) ");
                stb.append("          AND TSCHOL.SCHOLARSHIP IS NOT NULL ");
                stb.append("     LEFT JOIN V_NAME_MST NMA044 ON NMA044.YEAR = REGD.YEAR ");
                stb.append("          AND NMA044.NAMECD1 = 'A044' ");
                stb.append("          AND NMA044.NAMECD2 = TSCHOL.SCHOLARSHIP ");
            }
            stb.append("WHERE   REGD.YEAR = '" + param._year + "' ");
            stb.append("    AND REGD.SCHREGNO = ? ");
            if (!SEMEALL.equals(param._semester)) {
                stb.append("AND REGD.SEMESTER = '" + param._semester + "' ");
            } else {
                stb.append("AND REGD.SEMESTER = '" + param._semeFlg + "' ");
            }

            final String psKey = "STUDENT_BASE_KEY";
            try {
                if (!param._psMap.containsKey(psKey)) {
                    param._psMap.put(psKey, db2.prepareStatement(stb.toString()));
                }
            } catch (Exception e) {
                log.error("Exception", e);
                return;
            }

            final Map<String, String> a002Name1Map = KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, "SELECT NAMECD2, NAME1 FROM NAME_MST WHERE NAMECD1 = 'A002' "), "NAMECD2", "NAME1");
            final Map<String, String> a003Name1Map = KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, "SELECT NAMECD2, NAME1 FROM NAME_MST WHERE NAMECD1 = 'A003' "), "NAMECD2", "NAME1");
            final Map<String, String> a004Name1Map = KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, "SELECT NAMECD2, NAME1 FROM NAME_MST WHERE NAMECD1 = 'A004' "), "NAMECD2", "NAME1");

            for (final Student student : students) {

                final Map rs = KnjDbUtils.firstRow(KnjDbUtils.query(db2, param.getPs(psKey), new Object[] {student._schregno}));
                if (!rs.isEmpty()) {
                    String transInfo = "";

                    final String entDate = KnjDbUtils.getString(rs, "ENT_DATE");
                    final String grdDate = KnjDbUtils.getString(rs, "GRD_DATE");
                    final String transferSdate = KnjDbUtils.getString(rs, "TRANSFER_SDATE");
                    if (null != grdDate) {
                        final String name1 = a003Name1Map.get(KnjDbUtils.getString(rs, "GRD_DIV"));
                        if (null != name1) {
                            transInfo = KNJ_EditDate.h_format_JP(db2, grdDate) + name1;
                        }
                    } else if (null != entDate) {
                        final String name1 = a002Name1Map.get(KnjDbUtils.getString(rs, "ENT_DIV"));
                        if (null != name1) {
                            transInfo = KNJ_EditDate.h_format_JP(db2, entDate) + name1;
                        }

                    } else if (null != transferSdate) {
                        final String a004Name1 = a004Name1Map.get(KnjDbUtils.getString(rs, "TRANSFERCD"));
                        final String transferEdate = KnjDbUtils.getString(rs, "TRANSFER_EDATE");

                        if (null != transferSdate && null != a004Name1) {
                            final StringBuffer sb = new StringBuffer();
                            sb.append(KNJ_EditDate.h_format_JP(db2, transferSdate));
                            if (null != transferEdate) {
                                sb.append("～");
                                sb.append(KNJ_EditDate.h_format_JP(db2, transferEdate));
                            }
                            sb.append(a004Name1);
                            transInfo = sb.toString();
                        }
                    }
                    student._attendno = KnjDbUtils.getString(rs, "ATTENDNO");
                    student._name = KnjDbUtils.getString(rs, "NAME");
                    student._nameEng = KnjDbUtils.getString(rs, "NAME_ENG");
                    student._transInfo = transInfo;
                    if (param._isTokiwagi) {
                        student._scholarshipName = null == KnjDbUtils.getString(rs, "SCHOLARSHIP_ABBV1") ? "" : "(" + KnjDbUtils.getString(rs, "SCHOLARSHIP_ABBV1") + ")";
                    }
                }
            }
        }

        private Student getStudent(final Map<String, Student> studentMap, String code) {
            if (code == null) {
                return null;
            }
            return studentMap.get(code);
        }

        private void loadAttend(final DB2UDB db2, final Param param, final List<Student> students) {
            if (param._isPrintAttend) {
                final String psKey = "ATTENDSEMES";
                if (null == param.getPs(psKey)) {
                    final Map attendParamMap;
                    if (param._schChrDatCount == 0) {
                        attendParamMap = new HashMap(param._attendParamMap); // コピーする
                        attendParamMap.put("hrClass", "?");
                    } else {
                        attendParamMap = param._attendParamMap;
                        attendParamMap.put("schregno", "?");
                    }

                    final String sql = AttendAccumulate.getAttendSemesSql(
                            param._year,
                            param._semester,
                            param._sdate,
                            param._date,
                            attendParamMap
                    );
                    log.debug(" sql = " + sql);

                    try {
                        param._psMap.put(psKey, db2.prepareStatement(sql));
                    } catch (Exception e) {
                        log.error("exception!", e);
                        return;
                    }
                }

                if (param._isOutputDebug) {
                    log.info("load attend.");
                }

                final long attendStartTime = System.currentTimeMillis();
                long attendAccTime = 0;

                if (param._schChrDatCount == 0) {
                    final Set<String> hrClasses = new HashSet<String>();
                    final Map<String, Student> studentMap = new HashMap<String, Student>();
                    for (final Student student : students) {
                        if (null == student._hrClass) {
                            continue;
                        }
                        hrClasses.add(student._hrClass);
                        studentMap.put(student._schregno, student);
                    }

                    for (final String hrClass : hrClasses) {

                        final long indStart = System.currentTimeMillis();
                        final List<Map<String, String>> rowList = KnjDbUtils.query(db2, param.getPs(psKey), new Object[] {hrClass});
                        final long indEnd = System.currentTimeMillis();
                        attendAccTime += indEnd - indStart;

                        for (final Map row : rowList) {
                            final Student student = studentMap.get(KnjDbUtils.getString(row, "SCHREGNO"));
                            if (null == student) {
                                continue;
                            }

                            if (!SEMEALL.equals(KnjDbUtils.getString(row, "SEMESTER"))) {
                                continue;
                            }

                            student._attendInfo = createAttendInfo(row);
                        }
                    }

                    final long attendEndTime = System.currentTimeMillis();
                    if (param._isOutputDebug) {
                        log.info("load attend (hr) elapsed time = " + (attendEndTime - attendStartTime) + "[ms] ( query time = " + attendAccTime + "[ms] / student count = " + students.size() + ")");
                    }

                } else {
                    for (final Student student : students) {

                        final long indStart = System.currentTimeMillis();
                        final List<Map<String, String>> rowList = KnjDbUtils.query(db2, param.getPs(psKey), new Object[] {student._schregno});
                        final long indEnd = System.currentTimeMillis();
                        attendAccTime += indEnd - indStart;

                        for (final Map row : rowList) {
                            if (!SEMEALL.equals(KnjDbUtils.getString(row, "SEMESTER"))) {
                                continue;
                            }

                            student._attendInfo = createAttendInfo(row);
                        }
                    }

                    final long attendEndTime = System.currentTimeMillis();
                    if (param._isOutputDebug) {
                        log.info("load attend elapsed time = " + (attendEndTime - attendStartTime) + "[ms] ( query time = " + attendAccTime + "[ms] / student count = " + students.size() + ")");
                    }
                }
            }
        }

        private AttendInfo createAttendInfo(final Map row) {
            final Integer zero = new Integer(0);
            final AttendInfo attendInfo = new AttendInfo(
                    KnjDbUtils.getInt(row, "LESSON", zero).intValue(),
                    KnjDbUtils.getInt(row, "MLESSON", zero).intValue(),
                    KnjDbUtils.getInt(row, "SUSPEND", zero).intValue(),
                    KnjDbUtils.getInt(row, "MOURNING", zero).intValue(),
                    KnjDbUtils.getInt(row, "SICK", zero).intValue(),
                    KnjDbUtils.getInt(row, "PRESENT", zero).intValue(),
                    KnjDbUtils.getInt(row, "LATE", zero).intValue(),
                    KnjDbUtils.getInt(row, "EARLY", zero).intValue(),
                    KnjDbUtils.getInt(row, "TRANSFER_DATE", zero).intValue()
            );
            return attendInfo;
        }

        private String getRecordRankSdivAverage(final String groupFlg, final String subclasscd, final String field) {
            return _recordRankSdivAverageMap.get(avgKey(groupFlg, subclasscd,  field));
        }

        private String avgKey(final String groupFlg, final String subclasscd, final String field) {
            return groupFlg + "|" + subclasscd + "|" + field;
        }

        private Map<String, String> getRecordRankSdivAverageMap(final DB2UDB db2, final Param param, final List<Student> students) {
            final Map<String, String> averageMap = new HashMap<String, String>();
            if (param._isKwansei && !PRGID_KNJD615P.equals(param._prgId)) {
                final Set<String> hrClassSet = new HashSet();
                final Set<String> courseSet = new HashSet();
                final Set<String> majorSet = new HashSet();
                for (final Student student : students) {
                    if (defstr(student._hrClass).length() == 3) {
                        hrClassSet.add(student._hrClass);
                    }
                    if (defstr(student._course).length() == 8) {
                        courseSet.add(student._course);
                    }
                    if (defstr(student._major).length() == 4) {
                        majorSet.add(student._major);
                    }
                }

                final String subclasscd = param.SUBCLASSCD999999;
                for (final String flg : Arrays.asList(StudentGroup.AVG_FLG_HR, StudentGroup.AVG_FLG_GRADE, StudentGroup.AVG_FLG_COURSE, StudentGroup.AVG_FLG_MAJOR)) {

                    //log.info(" store.getAverageMap(" + flg + ", " + subclasscd + ") = " + recordRankSdivSoutenDatStore.getAverageMap(flg, subclasscd));
                    for (final Map.Entry<String, RecordRankSdivSoutenDat.Average> e : recordRankSdivSoutenDatStore.getAverageMap(flg, subclasscd).entrySet()) {
                        final String flgVal = e.getKey();
                        final RecordRankSdivSoutenDat.Average average = e.getValue();

                        if (StudentGroup.AVG_FLG_HR.equals(flg) && !hrClassSet.contains(flgVal)) {
                            continue;
                        } else if (StudentGroup.AVG_FLG_GRADE.equals(flg)) {
                        } else if (StudentGroup.AVG_FLG_COURSE.equals(flg) && !courseSet.contains(flgVal)) {
                            continue;
                        } else if (StudentGroup.AVG_FLG_MAJOR.equals(flg) && !majorSet.contains(flgVal)) {
                            continue;
                        }
                        averageMap.put(avgKey(flg, subclasscd, "TOTAL"), average.getAvg(0));
                        averageMap.put(avgKey(flg, subclasscd, "AVG"), average.getAvgAvg(1));
                        averageMap.put(avgKey(flg, subclasscd, "COUNT"), String.valueOf(average.getCount()));
                    }
                }

                return averageMap;
            }

            final Set<String> courseSet = new HashSet();
            final Set<String> majorSet = new HashSet();
            for (final Student student : students) {
                if (defstr(student._course).length() == 8) {
                    courseSet.add("('" + student._course.substring(0, 1) + "', '" + student._course.substring(1, 4) + "', '" + student._course.substring(4) + "')");
                }
                if (defstr(student._major).length() == 4) {
                    majorSet.add("('" + student._major.substring(0, 1) + "', '" + student._major.substring(1) + "')");
                }
            }
            if (courseSet.isEmpty() || majorSet.isEmpty()) {
                log.warn("warning:PARAMETER(couse or major) is NULL .");
                return averageMap;
            }

            final StringBuffer stb = new StringBuffer();
            final String testkindcd = param._testKindCd.substring(0, 2);
            final String testitemcd = param._testKindCd.substring(2, 4);
            final String scoreDiv = param._testKindCd.substring(4);

            stb.append("WITH ");
            //対象生徒の表 クラスの生徒
            stb.append(" SCHNO_A AS(");
            stb.append("     SELECT  W1.SCHREGNO ");
            stb.append("            ,W1.GRADE,W1.COURSECD, W1.MAJORCD, W1.COURSECODE ");
            stb.append("            ,CASE WHEN W1.GRADE||W1.HR_CLASS = '" + _cd + "' THEN '1' ELSE '0' END AS IS_HR ");
            stb.append("            ,CASE WHEN (W1.COURSECD, W1.MAJORCD, W1.COURSECODE) IN (VALUES " + mkString(courseSet, ",") + ") THEN '1' ELSE '0' END AS IS_COURSE ");
            stb.append("            ,CASE WHEN (W1.COURSECD, W1.MAJORCD) IN (VALUES " + mkString(majorSet, ",") + ") THEN '1' ELSE '0' END AS IS_MAJOR ");
            stb.append("     FROM    SCHREG_REGD_DAT W1 ");
            stb.append("     INNER JOIN V_SEMESTER_GRADE_MST W2 ON W2.YEAR = W1.YEAR AND W2.SEMESTER = W1.SEMESTER AND W2.GRADE = W1.GRADE ");
            stb.append("     LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = W1.YEAR AND GDAT.GRADE = W1.GRADE ");
            stb.append("     LEFT JOIN SCHREG_ENT_GRD_HIST_DAT ENTGRD ON ENTGRD.SCHREGNO = W1.SCHREGNO AND ENTGRD.SCHOOL_KIND = GDAT.SCHOOL_KIND ");

            stb.append("     WHERE   W1.YEAR = '" + param._year + "' ");
            if (SEMEALL.equals(param._semester)) {
                stb.append("     AND W1.SEMESTER = '" + param._semeFlg + "' ");
            } else {
                stb.append("     AND W1.SEMESTER = '" + param._semester + "' ");
                stb.append("     AND NOT (ENTGRD.GRD_DIV IN ('2','3','6','7') AND ENTGRD.GRD_DATE IS NOT NULL AND ENTGRD.GRD_DATE < W2.SDATE) "); // 学期開始日前に在籍していない生徒は対象外
            }
            stb.append("         AND W1.GRADE = '" + param._grade + "' ");
            stb.append(") ");

            for (final String flg : Arrays.asList(StudentGroup.AVG_FLG_HR, StudentGroup.AVG_FLG_GRADE, StudentGroup.AVG_FLG_COURSE, StudentGroup.AVG_FLG_MAJOR)) {
                final StringBuffer stb2 = new StringBuffer();
                stb2.append(stb);
                stb2.append("SELECT ");
                stb2.append("        W3.SUBCLASSCD ");
                stb2.append("       ,DECIMAL(ROUND(AVG(FLOAT(W3.SCORE))*10,0)/10,5,1) AS AVG_HR_TOTAL ");
                stb2.append("       ,DECIMAL(ROUND(AVG(FLOAT(W3.AVG))*10,0)/10,5,1) AS AVG_HR_AVERAGE ");
                stb2.append("       ,COUNT(W3.SCORE) AS COUNT ");
                stb2.append("  FROM  RECORD_RANK_SDIV_DAT W3 ");
                stb2.append(" WHERE  W3.YEAR = '" + param._year + "' ");
                stb2.append("   AND  W3.SEMESTER = '" + param._semester + "' ");
                stb2.append("   AND  W3.TESTKINDCD = '" + testkindcd + "' AND W3.TESTITEMCD = '" + testitemcd + "' AND W3.SCORE_DIV = '" + scoreDiv + "' ");
                stb2.append("   AND  W3.SUBCLASSCD IN ('333333', '555555', '" + param.SUBCLASSCD999999 + "') ");
                stb2.append("   AND  W3.SCHREGNO IN ( SELECT W1.SCHREGNO FROM SCHNO_A W1 ");
                if (StudentGroup.AVG_FLG_HR.equals(flg)) {
                    stb2.append(" WHERE W1.IS_HR = '1' ");
                } else if (StudentGroup.AVG_FLG_GRADE.equals(flg)) {
                } else if (StudentGroup.AVG_FLG_COURSE.equals(flg)) {
                    stb2.append(" WHERE W1.IS_COURSE = '1' ");
                } else if (StudentGroup.AVG_FLG_MAJOR.equals(flg)) {
                    stb2.append(" WHERE W1.IS_MAJOR = '1' ");
                }
                stb2.append(" ) ");
                stb2.append(" GROUP BY W3.SUBCLASSCD ");
                final String sql = stb2.toString();
                if (param._isOutputDebugQuery) {
                    log.info(" avg " + flg + " sql = " + sql);
                }

                for (final Map row : KnjDbUtils.query(db2, sql)) {

                    final String subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");

                    averageMap.put(avgKey(flg, subclasscd, "TOTAL"), KnjDbUtils.getString(row, "AVG_HR_TOTAL"));
                    averageMap.put(avgKey(flg, subclasscd, "AVG"), KnjDbUtils.getString(row, "AVG_HR_AVERAGE"));
                    averageMap.put(avgKey(flg, subclasscd, "COUNT"), KnjDbUtils.getString(row, "COUNT"));
                }
            }
            return averageMap;
        }

        private String[] toArray(final Set<String> set) {
            final List<String> list = new ArrayList(set);
            final String[] arr = new String[set.size()];
            for (int i = 0; i < list.size(); i++) {
                arr[i] = list.get(i);
            }
            return arr;
        }

        private void loadRank(final DB2UDB db2, final Param param, final Map<String, Student> studentMap) {
            if (param._isKwansei && !PRGID_KNJD615P.equals(param._prgId)) {
                recordRankSdivSoutenDatStore = RecordRankSdivSoutenDat.Store.load(db2, param._year, param._semester + param._testKindCd, param._semeFlg, param._grade);
                for (final Student student : studentMap.values()) {
                    final RecordRankSdivSoutenDat recordRankSdivSoutenDat = recordRankSdivSoutenDatStore.getRecordRankSdivSoutenDat(student._schregno, param.SUBCLASSCD999999);
                    if (null != recordRankSdivSoutenDat) {
                        student._scoreSum999999 = defstr(recordRankSdivSoutenDat._totalPoint);
                        student._scoreAvg999999 = recordRankSdivSoutenDat.getTotalAvg(1);
                        student._classRank = recordRankSdivSoutenDat._classRank;

                        if (OUTPUT_RANK1.equals(param._outputRank)) {
                            student._rank = recordRankSdivSoutenDat._classRank;
                        } else if (OUTPUT_RANK2.equals(param._outputRank)) {
                            student._rank = recordRankSdivSoutenDat._gradeRank;
                        } else if (OUTPUT_RANK3.equals(param._outputRank)) {
                            student._rank = recordRankSdivSoutenDat._courseRank;
                        } else if (OUTPUT_RANK4.equals(param._outputRank)) {
                            student._rank = recordRankSdivSoutenDat._majorRank;
                        }
                    }
                }
                return;
            }

            final String testkindcd = param._testKindCd.substring(0, 2);
            final String testitemcd = param._testKindCd.substring(2, 4);
            final String scoreDiv = param._testKindCd.substring(4);

            String _rankFieldName = null;
            if (OUTPUT_ORDER_2_SEISEKIJUN.equals(param._outputValue)) {
                _rankFieldName = null;
            } else if (OUTPUT_RANK1.equals(param._outputRank)) {
                if (OUTPUT_KJUN2.equals(param._outputKijun)) {
                    _rankFieldName = "CLASS_AVG_RANK";
                } else {
                    _rankFieldName = "CLASS_RANK";
                }
            } else if (OUTPUT_RANK2.equals(param._outputRank)) {
                if (OUTPUT_KJUN2.equals(param._outputKijun)) {
                    _rankFieldName = "GRADE_AVG_RANK";
                } else {
                    _rankFieldName = "GRADE_RANK";
                }
            } else if (OUTPUT_RANK3.equals(param._outputRank)) {
                if (OUTPUT_KJUN2.equals(param._outputKijun)) {
                    _rankFieldName = "COURSE_AVG_RANK";
                } else {
                    _rankFieldName = "COURSE_RANK";
                }
            } else if (OUTPUT_RANK4.equals(param._outputRank)) {
                if (OUTPUT_KJUN2.equals(param._outputKijun)) {
                    _rankFieldName = "MAJOR_AVG_RANK";
                } else {
                    _rankFieldName = "MAJOR_RANK";
                }
            }

            final StringBuffer stb = new StringBuffer();
            stb.append("WITH ");

            //対象生徒の表 クラスの生徒
            stb.append(" SCHNO_A AS(");
            stb.append("     SELECT  W1.SCHREGNO ");
            stb.append("          ,  W1.GRADE ");
            stb.append("          ,  W1.HR_CLASS ");
            stb.append("          ,  W1.COURSECD ");
            stb.append("          ,  W1.MAJORCD ");
            stb.append("          ,  W1.COURSECODE ");
            stb.append("     FROM    SCHREG_REGD_DAT W1 ");
            stb.append("     INNER JOIN V_SEMESTER_GRADE_MST W2 ON W2.YEAR = W1.YEAR AND W2.SEMESTER = W1.SEMESTER AND W2.GRADE = W1.GRADE ");

            stb.append("     WHERE   W1.YEAR = '" + param._year + "' ");
            if (SEMEALL.equals(param._semester)) {
                stb.append("     AND W1.SEMESTER = '" + param._semeFlg + "' ");
            } else {
                stb.append("     AND W1.SEMESTER = '" + param._semester + "' ");
                stb.append("     AND W1.SCHREGNO NOT IN (SELECT S1.SCHREGNO FROM SCHREG_BASE_MST S1");
                stb.append("                    WHERE S1.GRD_DIV IN ('2','3','6','7') AND S1.GRD_DATE < W2.SDATE) ");
            }
            if (param._isPrintGroupGrade) {
                stb.append("         AND W1.GRADE = '" + param._grade + "' ");
                if (PRGID_KNJD615P.equals(param._prgId) && OUTPUT_ORDER_2_SEISEKIJUN.equals(param._outputOrder)) {
                    stb.append("         AND W1.GRADE || W1.HR_CLASS IN " + SQLUtils.whereIn(true, param._paramClassSelected));
                }
            } else if (param._isPrintGroupCourse) {
                stb.append("         AND W1.GRADE = '" + param._grade + "' ");
                stb.append("         AND W1.COURSECD = '" + _cd.substring(0, 1) + "' ");
                stb.append("         AND W1.MAJORCD = '" + _cd.substring(1, 4) + "' ");
                stb.append("         AND W1.COURSECODE = '" + _cd.substring(4) + "' ");
            } else {
                stb.append("         AND W1.GRADE = '" + _cd.substring(0, 2) + "' ");
                stb.append("         AND W1.HR_CLASS = '" + _cd.substring(2) + "' ");
            }
            stb.append(") ");

            if ("2".equals(param._outputValue)) {
                stb.append(" , DEVIATION_SUM AS ( ");
                stb.append("     SELECT  ");
                stb.append("           T1.SCHREGNO ");
                stb.append("         , L1.GRADE ");
                stb.append("         , L1.HR_CLASS ");
                stb.append("         , L1.COURSECD ");
                stb.append("         , L1.MAJORCD ");
                stb.append("         , L1.COURSECODE ");
                stb.append("         , SUM(T1.GRADE_DEVIATION) AS GRADE_DEVIATION_SUM ");
                stb.append("         , DECIMAL(ROUND(AVG(T1.GRADE_DEVIATION)*10,0)/10,5,1) AS GRADE_DEVIATION_AVG ");
                stb.append("         , SUM(T1.COURSE_DEVIATION) AS COURSE_DEVIATION_SUM ");
                stb.append("         , DECIMAL(ROUND(AVG(T1.COURSE_DEVIATION)*10,0)/10,5,1) AS COURSE_DEVIATION_AVG ");
                stb.append("     FROM RECORD_RANK_SDIV_DAT T1 ");
                stb.append("     INNER JOIN SCHREG_REGD_DAT L1 ON ");
                stb.append("              L1.SCHREGNO = T1.SCHREGNO ");
                stb.append("          AND L1.YEAR = T1.YEAR ");
                if (SEMEALL.equals(param._semester)) {
                    stb.append("     AND L1.SEMESTER = '" + param._semeFlg + "' ");
                } else {
                    stb.append("     AND L1.SEMESTER = '" + param._semester + "' ");
                }
                stb.append("     INNER JOIN SUBCLASS_MST L2 ON "); // 合計科目を含めない
                stb.append("            L2.CLASSCD = T1.CLASSCD ");
                stb.append("        AND L2.SCHOOL_KIND = T1.SCHOOL_KIND ");
                stb.append("        AND L2.CURRICULUM_CD = T1.CURRICULUM_CD ");
                stb.append("        AND L2.SUBCLASSCD = T1.SUBCLASSCD ");
                stb.append("     INNER JOIN RECORD_RANK_SDIV_DAT L3 ON "); // 科目999999のある生徒が母集団
                stb.append("            L3.YEAR = T1.YEAR ");
                stb.append("        AND L3.SEMESTER = T1.SEMESTER ");
                stb.append("        AND L3.TESTKINDCD = T1.TESTKINDCD ");
                stb.append("        AND L3.TESTITEMCD = T1.TESTITEMCD ");
                stb.append("        AND L3.SCORE_DIV = T1.SCORE_DIV ");
                stb.append("        AND L3.SUBCLASSCD = '999999' ");
                stb.append("        AND L3.SCHREGNO = T1.SCHREGNO ");
                stb.append("      WHERE ");
                stb.append("          T1.YEAR = '" + param._year + "' ");
                if (SEMEALL.equals(param._semester)) {
                    stb.append("     AND T1.SEMESTER = '" + param._semeFlg + "' ");
                } else {
                    stb.append("     AND T1.SEMESTER = '" + param._semester + "' ");
                }
                stb.append("        AND T1.TESTKINDCD = '" + testkindcd + "' ");
                stb.append("        AND T1.TESTITEMCD = '" + testitemcd + "' ");
                stb.append("        AND T1.SCORE_DIV = '" + scoreDiv + "' ");
                stb.append("        AND L1.GRADE = '" + param._grade + "' ");
                stb.append("        AND (T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD) NOT IN (SELECT ATTEND_CLASSCD, ATTEND_SCHOOL_KIND, ATTEND_CURRICULUM_CD, ATTEND_SUBCLASSCD ");
                stb.append("          FROM SUBCLASS_REPLACE_COMBINED_DAT  ");
                stb.append("          WHERE YEAR = '" + param._year  + "' ");
                stb.append("            ) ");
                stb.append("     GROUP BY ");
                stb.append("       T1.SCHREGNO ");
                stb.append("     , L1.GRADE ");
                stb.append("     , L1.HR_CLASS ");
                stb.append("     , L1.COURSECD ");
                stb.append("     , L1.MAJORCD ");
                stb.append("     , L1.COURSECODE ");
                stb.append(" ) ");
                stb.append(" , DEVIATION_SUM_RANK AS ( ");
                stb.append("    SELECT  ");
                stb.append("       T1.SCHREGNO ");
                stb.append("     , T1.HR_CLASS ");
                stb.append("     , T1.COURSECD ");
                stb.append("     , T1.MAJORCD ");
                stb.append("     , T1.COURSECODE ");
                stb.append("     , T1.GRADE_DEVIATION_SUM ");
                stb.append("     , T1.GRADE_DEVIATION_AVG ");
                stb.append("     , RANK() OVER(PARTITION BY T1.GRADE                                                      ORDER BY T1.GRADE_DEVIATION_SUM DESC) AS GRADE_DEVIATION_SUM_RANK ");    // 各科目の学年偏差値の合計で学年順位
                stb.append("     , RANK() OVER(PARTITION BY T1.GRADE                                                      ORDER BY T1.GRADE_DEVIATION_AVG DESC) AS GRADE_DEVIATION_AVG_RANK ");    // 各科目の学年偏差値の平均で学年順位
                stb.append("     , RANK() OVER(PARTITION BY T1.GRADE, T1.HR_CLASS                                         ORDER BY T1.GRADE_DEVIATION_SUM DESC) AS GRADE_DEVIATION_SUM_HR_RANK "); // 各科目の学年偏差値の合計でHR順位
                stb.append("     , RANK() OVER(PARTITION BY T1.GRADE, T1.HR_CLASS                                         ORDER BY T1.GRADE_DEVIATION_AVG DESC) AS GRADE_DEVIATION_AVG_HR_RANK "); // 各科目の学年偏差値の平均でHR順位
                stb.append("     , T1.COURSE_DEVIATION_SUM ");
                stb.append("     , T1.COURSE_DEVIATION_AVG ");
                stb.append("     , RANK() OVER(PARTITION BY T1.GRADE,              T1.COURSECD, T1.MAJORCD, T1.COURSECODE ORDER BY T1.COURSE_DEVIATION_SUM DESC) AS COURSE_DEVIATION_SUM_RANK ");    // 各科目のコース偏差値の合計でコース順位
                stb.append("     , RANK() OVER(PARTITION BY T1.GRADE,              T1.COURSECD, T1.MAJORCD, T1.COURSECODE ORDER BY T1.COURSE_DEVIATION_AVG DESC) AS COURSE_DEVIATION_AVG_RANK ");    // 各科目のコース偏差値の合計でコース順位
                stb.append("     , RANK() OVER(PARTITION BY T1.GRADE, T1.HR_CLASS, T1.COURSECD, T1.MAJORCD, T1.COURSECODE ORDER BY T1.COURSE_DEVIATION_SUM DESC) AS COURSE_DEVIATION_SUM_HR_RANK "); // 各科目のコース偏差値の合計でHR順位
                stb.append("     , RANK() OVER(PARTITION BY T1.GRADE, T1.HR_CLASS, T1.COURSECD, T1.MAJORCD, T1.COURSECODE ORDER BY T1.COURSE_DEVIATION_AVG DESC) AS COURSE_DEVIATION_AVG_HR_RANK "); // 各科目のコース偏差値の合計でHR順位
                stb.append("   FROM DEVIATION_SUM T1 ");
                stb.append(" ) ");
            }

            //メイン表
            stb.append("SELECT  RANK.SCHREGNO ");
            stb.append("       ,RANK.SUBCLASSCD ");
            if ("2".equals(param._outputValue)) {
                if (OUTPUT_KJUN2.equals(param._outputKijun)) {
                    if (OUTPUT_RANK3.equals(param._outputRank)) {
                        stb.append("   , DSR.COURSE_DEVIATION_AVG_HR_RANK AS CLASS_RANK ");
                        stb.append("   , DSR.COURSE_DEVIATION_AVG_RANK    AS TOTAL_RANK ");
                    } else {
                        stb.append("   , DSR.GRADE_DEVIATION_AVG_HR_RANK  AS CLASS_RANK ");
                        stb.append("   , DSR.GRADE_DEVIATION_AVG_RANK     AS TOTAL_RANK ");
                    }
                } else {
                    if (OUTPUT_RANK3.equals(param._outputRank)) {
                        stb.append("   , DSR.COURSE_DEVIATION_SUM_HR_RANK AS CLASS_RANK ");
                        stb.append("   , DSR.COURSE_DEVIATION_SUM_RANK    AS TOTAL_RANK ");
                    } else {
                        stb.append("   , DSR.GRADE_DEVIATION_SUM_HR_RANK  AS CLASS_RANK ");
                        stb.append("   , DSR.GRADE_DEVIATION_SUM_RANK     AS TOTAL_RANK ");
                    }
                }
                if (OUTPUT_RANK3.equals(param._outputRank)) {
                    stb.append("   , DSR.COURSE_DEVIATION_SUM AS SCORE ");
                    stb.append("   , DSR.COURSE_DEVIATION_AVG AS TOTAL_AVG ");
                } else {
                    stb.append("   , DSR.GRADE_DEVIATION_SUM AS SCORE ");
                    stb.append("   , DSR.GRADE_DEVIATION_AVG AS TOTAL_AVG ");
                }
            } else {
                if (OUTPUT_KJUN2.equals(param._outputKijun)) {
                    stb.append("   ,RANK.CLASS_AVG_RANK AS CLASS_RANK");
                    stb.append("   ," + _rankFieldName + "  AS TOTAL_RANK ");
                } else {
                    stb.append("   ,RANK.CLASS_RANK ");
                    stb.append("   ," + _rankFieldName + "  AS TOTAL_RANK ");
                }
                stb.append("       ,RANK.SCORE ");
                stb.append("       ,DECIMAL(ROUND(FLOAT(RANK.AVG)*10,0)/10,5,1) AS TOTAL_AVG ");
            }
            stb.append("  FROM RECORD_RANK_SDIV_DAT RANK ");
            stb.append("  INNER JOIN SCHNO_A T1 ");
            stb.append("      ON RANK.SCHREGNO = T1.SCHREGNO ");
            if ("2".equals(param._outputValue)) {
                stb.append("  INNER JOIN DEVIATION_SUM_RANK DSR ON DSR.SCHREGNO = T1.SCHREGNO ");
            }
            stb.append(" WHERE ");
            stb.append("        RANK.YEAR = '" + param._year + "' ");
            stb.append("   AND  RANK.SEMESTER = '" + param._semester + "' ");
            stb.append("   AND  RANK.TESTKINDCD = '" + testkindcd + "' ");
            stb.append("   AND  RANK.TESTITEMCD = '" + testitemcd + "' ");
            stb.append("   AND  RANK.SCORE_DIV = '" + scoreDiv + "' ");
            stb.append("   AND  RANK.SUBCLASSCD IN ('333333', '555555', '" + param.SUBCLASSCD999999 + "') ");

            for (final Map row : KnjDbUtils.query(db2, stb.toString())) {
                final String schregno = KnjDbUtils.getString(row, "SCHREGNO");
                final Student student = getStudent(studentMap, schregno);
                if (null == student) {
                    continue;
                }
                final String subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");
                if ("333333".equals(subclasscd)) {
                    student._scoreSum333333 = KnjDbUtils.getString(row, "SCORE");
                    student._scoreAvg333333 = KnjDbUtils.getString(row, "TOTAL_AVG");
                } else if ("555555".equals(subclasscd)) {
                    student._scoreSum555555 = KnjDbUtils.getString(row, "SCORE");
                    student._scoreAvg555555 = KnjDbUtils.getString(row, "TOTAL_AVG");
                } else {
                    student._scoreSum999999 = KnjDbUtils.getString(row, "SCORE");
                    student._scoreAvg999999 = KnjDbUtils.getString(row, "TOTAL_AVG");
                    student._classRank = KnjDbUtils.getInt(row, "CLASS_RANK", new Integer(0)).intValue();
                    student._rank = KnjDbUtils.getInt(row, "TOTAL_RANK", new Integer(0)).intValue();
                }
            }
        }

        private void loadScoreDetail(final DB2UDB db2, final Param param, final Map<String, Student> studentMap) {
            if (param._isOutputDebug) {
                log.info(" load scoreDetail.");
            }
            final Integer zero = new Integer(0);

            final String psKeyScoreDetail = "SCORE_DETAIL";
            try {
                if (null == param.getPs(psKeyScoreDetail)) {
                    final String sql = sqlStdSubclassDetail(param);
                    if (param._isOutputDebugQuery) {
                        log.info(" subclass detail sql = " + sql);
                    } else {
                        log.debug(" subclass detail sql = " + sql);
                    }

                    param._psMap.put(psKeyScoreDetail, db2.prepareStatement(sql));
                }
            } catch (Exception e) {
                log.error("Exception", e);
            }

            Object[] arg;
            if (param._isPrintGroupGrade) {
                arg = null;
            } else if (param._isPrintGroupCourse) {
                arg = new Object[] {_cd.substring(0, 1), _cd.substring(1, 4), _cd.substring(4)};
            } else { // if (param._isPrintGroupHr) {
                arg = new Object[] {_cd.substring(0, 2), _cd.substring(2)};
            }

            final long scoreSubclassStart = System.currentTimeMillis();

            final long scoreStartTime = System.currentTimeMillis();
            final List<Map<String, String>> rowListScore = KnjDbUtils.query(db2, param.getPs(psKeyScoreDetail), arg);
            final long scoreEndTime = System.currentTimeMillis();
            for (final Map row : rowListScore) {
//                    if (param._enablePringFlg && "1".equals(KnjDbUtils.getString(row, "PRINT_FLG"))) {
//                        continue;
//                    }
                final Student student = getStudent(studentMap, KnjDbUtils.getString(row, "SCHREGNO"));
                if (student == null) {
                    continue;
                }

                final String subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");
                final String classCd = subclasscd == null ? "" : subclasscd.substring(0, 2);
                if (classCd.compareTo(KNJDefineSchool.subject_U) <= 0 || classCd.equals(KNJDefineSchool.subject_T)) {
                    final SubClass subclass = getSubclass(row, _subclasses);
                    final ScoreDetail scoreDetail = new ScoreDetail(
                            subclass,
                            student,
                            KnjDbUtils.getString(row, "CHAIRCD"),
                            KnjDbUtils.getString(row, "SCORE"),
                            KnjDbUtils.getString(row, "RECORD_SCORE_DAT_SCORE"),
                            KnjDbUtils.getString(row, "VALUE_DI"),
                            KnjDbUtils.getString(row, "SUPP_SCORE"),
                            KnjDbUtils.getString(row, "ASSESS_LEVEL"),
                            KnjDbUtils.getString(row, "KARI_HYOUTEI"),
                            KnjDbUtils.getString(row, "PASS_SCORE"),
                            KnjDbUtils.getString(row, "PROV_FLG"),
                            KnjDbUtils.getInt(row, "REPLACEMOTO", zero),
                            KnjDbUtils.getString(row, "PRINT_FLG"),
                            KnjDbUtils.getString(row, "SLUMP"),
                            KnjDbUtils.getString(row, "SLUMP_MARK"),
                            KnjDbUtils.getString(row, "SLUMP_SCORE"),
                            KnjDbUtils.getInt(row, "COMP_CREDIT", zero),
                            KnjDbUtils.getInt(row, "GET_CREDIT", zero),
                            KnjDbUtils.getInt(row, "CREDITS", zero),
                            KnjDbUtils.getString(row, "CLASS_DEVIATION"),
                            KnjDbUtils.getString(row, "GRADE_DEVIATION"),
                            KnjDbUtils.getString(row, "COURSE_DEVIATION"),
                            KnjDbUtils.getString(row, "MAJOR_DEVIATION")
                            );
                    student._scoreDetails.put(scoreDetail._subclass._subclasscd, scoreDetail);
                }
            }
            final long scoreSubclassEnd = System.currentTimeMillis();
            if (param._isOutputDebug) {
                log.info(" load scoreDetail elapsed time = " + (scoreSubclassEnd - scoreSubclassStart) + "[ms] ( query time = " + (scoreEndTime - scoreStartTime) + "[ms] / count = " + rowListScore.size() + ")");
            }

            if (param._isPrintGroupHr && "1".equals(param._printreport)) {
                if (param._isOutputDebug) {
                    log.info(" set subclass staff.");
                }

                final String psKeySubclassStaff = "SUBCLASS_STAFF";
                try {
                    if (null == param.getPs(psKeySubclassStaff)) {
                        final String sql = sqlStdSubclassStaff(param);
                        if (param._isOutputDebugQuery) {
                            log.info(" subclass staff sql = " + sql);
                        } else {
                            log.debug(" subclass staff sql = " + sql);
                        }

                        param._psMap.put(psKeySubclassStaff, db2.prepareStatement(sql));
                    }
                } catch (Exception e) {
                    log.error("Exception", e);
                }

                final Set<String> setStaffSubclasscd = new HashSet<String>();
                for (final Map row : KnjDbUtils.query(db2, param.getPs(psKeySubclassStaff), arg)) {
                    final String subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");
                    if (setStaffSubclasscd.contains(subclasscd)) {
                        continue;
                    }
                    final SubClass subClass = _subclasses.get(subclasscd);
                    if (null == subClass) {
                        continue;
                    }
                    final String staff2 = KnjDbUtils.getString(row, "STAFF2");
                    if (!StringUtils.isEmpty(staff2)) {
                        subClass._otherstafflist.add(staff2);
                    }
                    final String staff3 = KnjDbUtils.getString(row, "STAFF3");
                    if (!StringUtils.isEmpty(staff3)) {
                        subClass._otherstafflist.add(staff3);
                    }
                    setStaffSubclasscd.add(subclasscd);
                }
            }

            if (PRGID_KNJD615H.equals(param._prgId)) {
                try {
                    final String psKey = "SCORE_DEVIATION";
                    if (null == param.getPs(psKey)) {
                        final String sql = sqlStdSubclassScoreDeviation(param);
                        if (param._isOutputDebugQuery) {
                            log.info(" subclass deviation sql = " + sql);
                        } else {
                            log.debug(" subclass deviation sql = " + sql);
                        }

                        param._psMap.put(psKey, db2.prepareStatement(sql));
                    }

                    for (final Map row : KnjDbUtils.query(db2, param.getPs(psKey), new Object[] {_cd.substring(0, 2), _cd.substring(2)})) {
//                        if (param._enablePringFlg && "1".equals(KnjDbUtils.getString(row, "PRINT_FLG"))) {
//                            continue;
//                        }
                        final Student student = getStudent(studentMap, KnjDbUtils.getString(row, "SCHREGNO"));
                        if (student == null) {
                            continue;
                        }
                        if (student._schScoreCnt == null) {
                            student._schScoreCnt = new SchScoreCnt();
                        }

                        ScoreDetail scoreDetail = student._scoreDetails.get(KnjDbUtils.getString(row, "SUBCLASSCD"));
                        if (null != scoreDetail) {
                            if (!"99".equals(KnjDbUtils.getString(row, "CURRICULUM_CD"))) {
                                String scoreval = KnjDbUtils.getString(row, "SCORE");
                                if (scoreval != null && !"".equals(scoreval)) {
                                    if ("5".equals(scoreval)) student._schScoreCnt._cntScore5++;
                                    if ("4".equals(scoreval)) student._schScoreCnt._cntScore4++;
                                    if ("3".equals(scoreval)) student._schScoreCnt._cntScore3++;
                                    if ("2".equals(scoreval)) student._schScoreCnt._cntScore2++;
                                    if ("1".equals(scoreval)) student._schScoreCnt._cntScore1++;
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error("Exception", e);
                }
            }

            if (param._isPrintAttend || param._isNaraken) {
                final String psKey = "ATTENDSUBCLASS";
                if (null == param.getPs(psKey)) {
                    final Map attendParamMap;
                    if (param._schChrDatCount == 0) {
                        // 処理が重くないので年組指定
                        attendParamMap = new HashMap(param._attendParamMap); // 念のためコピーする
                        attendParamMap.put("hrClass", "?");
                    } else {
                        attendParamMap = param._attendParamMap;
                        attendParamMap.put("schregno", "?");
                    }

                    final String sql = AttendAccumulate.getAttendSubclassSql(
                            param._year,
                            param._semester,
                            param._sdate,
                            param._date,
                            attendParamMap
                            );
                    //log.debug(" attend subclass sql = " + sql);
                    try {
                        param._psMap.put(psKey, db2.prepareStatement(sql));
                    } catch (Exception e) {
                        log.error("exception!", e);
                        return;
                    }
                }

                log.info("load attendSubclass");
                long attendSubclassStart = System.currentTimeMillis();
                long attendSubclassAcc = 0;

                if (param._schChrDatCount == 0) {
                    final Set<String> hrClasses = new HashSet<String>();
                    for (final Student student : _students) {
                        if (null == student._hrClass) {
                            continue;
                        }
                        hrClasses.add(student._hrClass);
                    }

                    for (final String hrClass : hrClasses) {
                        long attendSubclassAccStart = System.currentTimeMillis();
                        final List<Map<String, String>> rowList = KnjDbUtils.query(db2, param.getPs(psKey), new Object[] {hrClass});
                        attendSubclassAcc += (System.currentTimeMillis() - attendSubclassAccStart);

                        for (final Map<String, String> row : rowList) {
                            final Student student = studentMap.get(KnjDbUtils.getString(row, "SCHREGNO"));
                            if (null == student) {
                                continue;
                            }
                            setSubclassAttendInfo(row, param, student);
                        }
                    }
                    long attendSubclassEnd = System.currentTimeMillis();
                    log.info("load attendSubclass (hr) elapsed time = " + (attendSubclassEnd - attendSubclassStart) + "[ms] ( query time = " + attendSubclassAcc + "[ms] / student count = " + _students.size() + ")");
                } else {
                    for (final Student student : _students) {
                        long attendSubclassAccStart = System.currentTimeMillis();
                        final List<Map<String, String>> rowList = KnjDbUtils.query(db2, param.getPs(psKey), new Object[] {student._schregno});
                        attendSubclassAcc += (System.currentTimeMillis() - attendSubclassAccStart);

                        for (final Map<String, String> row : rowList) {
                            setSubclassAttendInfo(row, param, student);
                        }
                    }
                    long attendSubclassEnd = System.currentTimeMillis();
                    log.info("load attendSubclass elapsed time = " + (attendSubclassEnd - attendSubclassStart) + "[ms] ( query time = " + attendSubclassAcc + "[ms] / student count = " + _students.size() + ")");
                }
            }

            if (param._isShimaneken) {
                if ("1".equals(param._keikokutenKubun)) {
                    // 講座
                    final Map<SubClass, Set<String>> subclassChairsMap = new TreeMap<SubClass, Set<String>>();
                    for (final SubClass subclass : _subclasses.values()) {
                        for (final Student student : _students) {
                            final ScoreDetail detail = student._scoreDetails.get(subclass.keySubclasscd());
                            if (null != detail && null != detail._chaircd) {
                                getMappedTreeSet(subclassChairsMap, subclass).add(detail._chaircd);
                            }
                        }
                        if (param._isOutputDebug) {
                            log.info(" subclass " + subclass._subclasscd + " chairs = " + subclassChairsMap.get(subclass));
                        }
                    }

                    final StringBuffer sql = new StringBuffer();
                    sql.append(" SELECT ");
                    sql.append("   T1.CHAIRCD ");
                    sql.append(" , T1.CHAIRNAME ");
                    if (SCORE_DIV_01.equals(param._scoreDiv)) {
                        sql.append(" , DET003.REMARK1 ");
                    } else if (SCORE_DIV_08.equals(param._scoreDiv)) {
                        sql.append(" , DET003.REMARK2 ");
                    } else if (SCORE_DIV_09.equals(param._scoreDiv)) {
                        sql.append(" , DET003.REMARK3 ");
                    } else {
                        sql.append(" , CAST(NULL AS VARCHAR(1)) ");
                    }
                    sql.append("       AS KEIKOKUTEN ");
                    sql.append(" FROM ");
                    sql.append("   CHAIR_DAT T1 ");
                    sql.append("   INNER JOIN CHAIR_DETAIL_DAT DET003 ON DET003.YEAR = T1.YEAR ");
                    sql.append("       AND DET003.SEMESTER = T1.SEMESTER ");
                    sql.append("       AND DET003.CHAIRCD = T1.CHAIRCD ");
                    sql.append("       AND DET003.SEQ = '003' ");
                    sql.append("   WHERE ");
                    sql.append("       T1.YEAR = '" + param._year + "' ");
                    if (SEMEALL.equals(param._semester)) {
                        sql.append("       AND T1.SEMESTER = '" + param._semeFlg + "' ");
                    } else {
                        sql.append("       AND T1.SEMESTER = '" + param._semester + "' ");
                    }
                    sql.append("       AND (T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD) = (?, ?, ?, ?) ");
                    PreparedStatement ps = null;

                    try {
                        ps = db2.prepareStatement(sql.toString());
                        for (final Map.Entry<SubClass, Set<String>> e : subclassChairsMap.entrySet()) {
                            final SubClass subclass = e.getKey();

                            final String[] split = subclass._subclasscd.split("-");
                            if (split.length == 4) {
                                subclass._chairInfos.putAll(KnjDbUtils.getKeyMap(KnjDbUtils.query(db2, ps, split), "CHAIRCD"));
                            }
                        }

                    } catch (Exception e) {
                        log.error("exception!", e);
                    } finally {
                        DbUtils.closeQuietly(ps);
                        db2.commit();
                    }

                } else if ("2".equals(param._keikokutenKubun)) {
                    // 類型平均 (講座グループ）
                    final String testkindcd = param._testKindCd.substring(0, 2);
                    final String testitemcd = param._testKindCd.substring(2, 4);
                    final String scoreDiv = param._testKindCd.substring(4);

                    final StringBuffer sql = new StringBuffer();
                    sql.append(" WITH CHAIR_GROUP_SDIV AS ( ");
                    sql.append(" SELECT ");
                    sql.append("     0 AS PRIORITY, ");
                    sql.append("     W1.CHAIR_GROUP_CD, ");
                    sql.append("     W2.SEMESTER, ");
                    sql.append("     W2.TESTKINDCD, ");
                    sql.append("     W2.TESTITEMCD, ");
                    sql.append("     W2.SCORE_DIV, ");
                    sql.append("     W2.CHAIRCD ");
                    sql.append(" FROM ");
                    sql.append("     CHAIR_GROUP_MST W1 ");
                    sql.append("     INNER JOIN CHAIR_GROUP_SDIV_DAT W2 ON W2.YEAR = W1.YEAR ");
                    sql.append("         AND W2.SEMESTER = W1.SEMESTER ");
                    sql.append("         AND W2.CHAIR_GROUP_CD = W1.CHAIR_GROUP_CD ");
                    sql.append(" WHERE ");
                    sql.append("     W1.YEAR = '" + param._year + "' ");
                    if (SEMEALL.equals(param._semester)) {
                        sql.append("     AND W1.SEMESTER = '" + param._semester + "' "); //ログイン学期
                    } else {
                        sql.append("     AND W1.SEMESTER = '" + param._semeFlg + "' ");
                    }
                    sql.append("     AND W2.TESTKINDCD   = '" + testkindcd + "' ");
                    sql.append("     AND W2.TESTITEMCD   = '" + testitemcd + "' ");
                    sql.append("     AND W2.SCORE_DIV    = '" + scoreDiv + "' ");
                    sql.append(" UNION ALL ");
                    sql.append(" SELECT ");
                    sql.append("     1 AS PRIORITY, ");
                    sql.append("     W1.CHAIR_GROUP_CD, ");
                    sql.append("     W2.SEMESTER, ");
                    sql.append("     W2.TESTKINDCD, ");
                    sql.append("     W2.TESTITEMCD, ");
                    sql.append("     W2.SCORE_DIV, ");
                    sql.append("     W2.CHAIRCD ");
                    sql.append(" FROM ");
                    sql.append("     CHAIR_GROUP_MST W1 ");
                    sql.append("     INNER JOIN CHAIR_GROUP_SDIV_DAT W2 ON W2.YEAR = W1.YEAR ");
                    sql.append("         AND W2.SEMESTER = W1.SEMESTER ");
                    sql.append("         AND W2.CHAIR_GROUP_CD = W1.CHAIR_GROUP_CD ");
                    sql.append(" WHERE ");
                    sql.append("     W1.YEAR = '" + param._year + "' ");
                    if (SEMEALL.equals(param._semester)) {
                        sql.append("     AND W1.SEMESTER = '" + param._semester + "' "); //ログイン学期
                    } else {
                        sql.append("     AND W1.SEMESTER = '" + param._semeFlg + "' ");
                    }
                    sql.append("     AND W2.TESTKINDCD   = '00' ");
                    sql.append("     AND W2.TESTITEMCD   = '00' ");
                    sql.append("     AND W2.SCORE_DIV    = '00' ");
                    sql.append(") ");
                    sql.append(", CHAIR_GROUP AS ( ");
                    sql.append(" SELECT ");
                    sql.append("     SUBSTR(W1.CHAIR_GROUP_CD, MAX(LENGTH(W1.CHAIR_GROUP_CD)-2, 1)) AS CHAIR_GROUP_CD, "); //下３桁
                    sql.append("     W1.CLASSCD, W1.SCHOOL_KIND, W1.CURRICULUM_CD, W1.SUBCLASSCD, ");
                    sql.append("     W2.CHAIRCD ");
                    sql.append(" FROM ");
                    sql.append("     CHAIR_GROUP_MST W1 ");
                    sql.append("     INNER JOIN CHAIR_GROUP_SDIV_DAT W2 ON ");
                    sql.append("             W2.YEAR = W1.YEAR ");
                    sql.append("         AND W2.SEMESTER  = W1.SEMESTER ");
                    sql.append("         AND W2.CHAIR_GROUP_CD = W1.CHAIR_GROUP_CD ");
                    sql.append("     INNER JOIN ( ");
                    sql.append("         SELECT ");
                    sql.append("             CHAIRCD, ");
                    sql.append("             MIN(PRIORITY) AS PRIORITY ");
                    sql.append("         FROM ");
                    sql.append("             CHAIR_GROUP_SDIV T1 ");
                    sql.append("         GROUP BY ");
                    sql.append("             CHAIRCD ");
                    sql.append("     ) W3 ON W3.CHAIRCD = W2.CHAIRCD ");
                    sql.append("     INNER JOIN CHAIR_GROUP_SDIV W4 ON ");
                    sql.append("             W4.CHAIRCD = W3.CHAIRCD ");
                    sql.append("         AND W4.PRIORITY = W3.PRIORITY ");
                    sql.append("         AND W4.SEMESTER = W2.SEMESTER ");
                    sql.append("         AND W4.TESTKINDCD = W2.TESTKINDCD ");
                    sql.append("         AND W4.TESTITEMCD = W2.TESTITEMCD ");
                    sql.append("         AND W4.SCORE_DIV = W2.SCORE_DIV ");
                    sql.append(" WHERE ");
                    sql.append("     W1.YEAR = '" + param._year + "' ");
                    if (SEMEALL.equals(param._semester)) {
                        sql.append("     AND W1.SEMESTER = '" + param._semester + "' "); //ログイン学期
                    } else {
                        sql.append("     AND W1.SEMESTER = '" + param._semeFlg + "' ");
                    }
                    sql.append(")  ");
                    //講座名簿とリンクし、生徒別科目別の講座グループを取得
                    sql.append("SELECT ");
                    sql.append("    CHR.CLASSCD || '-' || CHR.SCHOOL_KIND || '-' || CHR.CURRICULUM_CD || '-' || CHR.SUBCLASSCD AS SUBCLASSCD, STD.SCHREGNO, ");
                    sql.append("    MIN(GRP.CHAIR_GROUP_CD) AS CHAIR_GROUP_CD "); //MIN講座グループ
                    sql.append("FROM ");
                    sql.append("    CHAIR_DAT CHR ");
                    sql.append("    INNER JOIN CHAIR_STD_DAT STD ON STD.YEAR = CHR.YEAR ");
                    sql.append("        AND STD.SEMESTER = CHR.SEMESTER ");
                    sql.append("        AND STD.CHAIRCD = CHR.CHAIRCD ");
                    sql.append("    INNER JOIN CHAIR_GROUP GRP ON GRP.CHAIRCD = STD.CHAIRCD ");
                    sql.append("        AND GRP.CLASSCD = CHR.CLASSCD AND GRP.SCHOOL_KIND = CHR.SCHOOL_KIND AND GRP.CURRICULUM_CD = CHR.CURRICULUM_CD AND GRP.SUBCLASSCD = CHR.SUBCLASSCD ");
                    sql.append("    INNER JOIN SCHREG_REGD_DAT REGD ON REGD.SCHREGNO = STD.SCHREGNO ");
                    sql.append("        AND REGD.YEAR = STD.YEAR ");
                    sql.append("        AND REGD.SEMESTER = STD.SEMESTER ");
                    sql.append("WHERE ");
                    sql.append("    CHR.YEAR = '" + param._year + "' ");
                    sql.append("    AND '" + param._date + "' BETWEEN STD.APPDATE AND STD.APPENDDATE ");
                    sql.append("    AND STD.SCHREGNO = ? ");
                    sql.append("GROUP BY ");
                    sql.append("    CHR.CLASSCD, CHR.SCHOOL_KIND, CHR.CURRICULUM_CD, CHR.SUBCLASSCD, STD.SCHREGNO ");

                    PreparedStatement ps = null;

                    try {
                        ps = db2.prepareStatement(sql.toString());
                        if (param._isOutputDebugQuery) {
                            log.info(" chairgroup sql = " + sql.toString());
                        }

                        for (final Student student : _students) {
                            student._subclasscdChairGroupCdMap.putAll(KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, ps, new Object[] {student._schregno}), "SUBCLASSCD", "CHAIR_GROUP_CD"));
                            if (param._isOutputDebug) {
                                log.info(" schregno = " + student._schregno + ", " + student._subclasscdChairGroupCdMap);
                            }
                        }

                    } catch (Exception e) {
                        log.error("exception!", e);
                    } finally {
                        DbUtils.closeQuietly(ps);
                        db2.commit();
                    }

                    final StringBuffer sqlAvg = new StringBuffer();
                    sqlAvg.append(" SELECT ");
                    sqlAvg.append("     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS SUBCLASSCD ");
                    sqlAvg.append("   , MAJORCD AS CHAIR_GROUP_CD ");
                    sqlAvg.append("   , AVG ");
                    sqlAvg.append(" FROM ");
                    sqlAvg.append("     RECORD_AVERAGE_SDIV_DAT T1 ");
                    sqlAvg.append(" WHERE ");
                    sqlAvg.append("     T1.YEAR = '" + param._year + "' ");
                    sqlAvg.append(" AND T1.SEMESTER = '" + param._semester + "' ");
                    sqlAvg.append(" AND T1.TESTKINDCD = '" + testkindcd + "' ");
                    sqlAvg.append(" AND T1.TESTITEMCD = '" + testitemcd + "' ");
                    sqlAvg.append(" AND T1.SCORE_DIV  = '" + scoreDiv + "' ");
                    sqlAvg.append(" AND T1.AVG_DIV    = '6' "); // 講座グループ
                    sqlAvg.append(" AND T1.GRADE      = '" + param._grade + "' ");
                    sqlAvg.append(" AND (T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD) = (?, ?, ?, ?) ");

                    try {
                        ps = db2.prepareStatement(sqlAvg.toString());

                        for (final SubClass subclass : _subclasses.values()) {
                            final String[] split = subclass._subclasscd.split("-");
                            if (split.length == 4) {
                                subclass._chairGroupAvgs.putAll(KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, ps, split), "CHAIR_GROUP_CD", "AVG"));
                            }
                        }

                    } catch (Exception e) {
                        log.error("exception!", e);
                    } finally {
                        DbUtils.closeQuietly(ps);
                        db2.commit();
                    }
                }
            }
        }

        private void setSubclassAttendInfo(final Map<String, String> row, final Param param, final Student student) {
            final Integer zero = new Integer(0);
            final String semester = KnjDbUtils.getString(row, "SEMESTER");

            if (param._isNaraken) {
                if (SEMEALL.equals(param._semester) || param._isLastSemester) {
                    // 学年末・最終学期指定のときは参照学期は9(すべて)
                    if (!SEMEALL.equals(semester)) {
                        return;
                    }
                } else {
                    // 学年末・最終学期以外の指定のときは参照学期は指定学期
                    if (!param._semester.equals(semester)) {
                        return;
                    }
                }

            } else {
                if (!SEMEALL.equals(semester)) {
                    return;
                }
            }

            ScoreDetail scoreDetail = student._scoreDetails.get(KnjDbUtils.getString(row, "SUBCLASSCD"));
            if (null == scoreDetail) {
                final SubClass subClass = _subclasses.get(KnjDbUtils.getString(row, "SUBCLASSCD"));
                if (null != subClass) {
                    scoreDetail = new ScoreDetail(subClass, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
                    student._scoreDetails.put(KnjDbUtils.getString(row, "SUBCLASSCD"), scoreDetail);
                }
                if (null == scoreDetail) {
                    // log.fatal(" no detail " + student._schregno + ", " + KnjDbUtils.getString(row, "SUBCLASSCD"));
                    return;
                }
            }

            final String specialGroupCd = KnjDbUtils.getString(row, "SPECIAL_GROUP_CD");
            final Integer specialAbsentMinutes = KnjDbUtils.getInt(row, "SPECIAL_SICK_MINUTES3", null);
            if (specialGroupCd != null && specialAbsentMinutes != null) {
                if (!student._spGroupAbsentMinutes.containsKey(specialGroupCd)) {
                    student._spGroupAbsentMinutes.put(specialGroupCd, new Integer(0));
                }
                int minute = student._spGroupAbsentMinutes.get(specialGroupCd).intValue();
                student._spGroupAbsentMinutes.put(specialGroupCd, new Integer(minute + specialAbsentMinutes.intValue()));
            }


            final String jisuField;
            if ("1".equals(param._knjd615vJugyoJisuLesson)) {
                // 授業時数
                jisuField = "LESSON";
            } else {
                // 出席すべき時数
                jisuField = "MLESSON";
            }
            if (param._isHagoromo) {
                final Integer integerJisu = KnjDbUtils.getInt(row, jisuField, zero) - KnjDbUtils.getInt(row, "ABSENT", zero) - KnjDbUtils.getInt(row, "MOURNING", zero);
                if (null != integerJisu) {
                    scoreDetail._subclass._jisuSet.add(integerJisu);
                }
            } else {
                final Integer integerJisu = KnjDbUtils.getInt(row, jisuField, zero);
                if (0 != integerJisu.intValue()) {
                    scoreDetail._subclass._jisuSet.add(integerJisu);
                }
            }
            scoreDetail._jisu = KnjDbUtils.getInt(row, "LESSON", zero) - KnjDbUtils.getInt(row, "ABSENT", zero) - KnjDbUtils.getInt(row, "MOURNING", zero);
            scoreDetail._absenceHigh = KnjDbUtils.getBigDecimal(row, "ABSENCE_HIGH", null);
            if (null != scoreDetail._replacemoto && scoreDetail._replacemoto.intValue() == -1) {
                scoreDetail._absent = Double.valueOf(KnjDbUtils.getString(row, "REPLACED_SICK"));
            } else {
                scoreDetail._absent = Double.valueOf(KnjDbUtils.getString(row, "SICK2"));
            }
            if (param._isNaraken) {
                if ("08".equals(param._scoreDiv)) {
                    scoreDetail._isNarakenKekkaOver = isNarakenKekkaOver(param, student, scoreDetail);
                }
            } else {
                scoreDetail._isKekkaOver = scoreDetail.judgeOver(scoreDetail._absent, scoreDetail._absenceHigh);
            }

        }

        private boolean isNarakenKekkaOver(final Param param, final Student student, final ScoreDetail scoreDetail) {
            if (null == scoreDetail._absent) {
                return false;
            }
            int credits = 0;
            int week = 0;
            int jisu = 0;
            param.logOnce(" 上限値算出 分子 = " + param._knjSchoolMst._syutokuBunsi + ", 分母 = " + param._knjSchoolMst._syutokuBunbo);
            if (param._knjSchoolMst.isHoutei()) {
                if (null == scoreDetail._credits) {
                    log.warn("単位無し：" + student._schregno + ", " + scoreDetail._subclass._subclasscd + ", kekka = " + scoreDetail._absent);
                } else {
                    final String sWeek;
                    if (SEMEALL.equals(param._semester) || param._isLastSemester) {
                        sWeek = KnjDbUtils.getString(param._vSchoolMst, "JITU_SYUSU");
                    } else {
                        sWeek = KnjDbUtils.getString(param._vSchoolMst, "HOUTEI_SYUSU_SEMESTER" + param._semester);
                    }
                    if (!NumberUtils.isDigits(sWeek)) {
                        log.warn("週数無し： semester = " + param._semester);
                    } else {
                        credits = scoreDetail._credits.intValue();
                        week = Integer.parseInt(sWeek);
                        jisu = credits * week;
                        param.logOnce(" subclass " + scoreDetail._subclass._subclasscd + ", 単位数 " + credits + ", 週数 " + week + ", 時数 " + jisu);
                    }
                }
            } else { // param._knjSchoolMst.isJitsu()
                jisu = scoreDetail._jisu;
                param.logOnce(" subclass " + scoreDetail._subclass._subclasscd + ", 時数 " + jisu);
            }
            boolean rtn = false;
            if (jisu != 0) {
                int jougenti = 0;
                final boolean isFutoukouTaiou = (SEMEALL.equals(param._semester) || param._isLastSemester) && null != student._schoolRefusal;
                if (isFutoukouTaiou) {
                    jougenti = jisu / 2; // 端数切捨て
                } else {
                    //jougenti = jisu / 3; // 端数切捨て
                    if (NumberUtils.isNumber(param._knjSchoolMst._syutokuBunsi) && NumberUtils.isNumber(param._knjSchoolMst._syutokuBunbo) && new BigDecimal(param._knjSchoolMst._syutokuBunbo).doubleValue() != 0.0) {
                        jougenti = new BigDecimal(jisu).multiply(new BigDecimal(param._knjSchoolMst._syutokuBunsi)).divide(new BigDecimal(param._knjSchoolMst._syutokuBunbo), 0, BigDecimal.ROUND_DOWN).intValue(); // 端数切捨て
                    } else {
                        param.logOnce(" 上限値算出エラー 分子 = " + param._knjSchoolMst._syutokuBunsi + ", 分母 = " + param._knjSchoolMst._syutokuBunbo);
                    }
                }
                if (jougenti > 0) {
                    rtn = jougenti < scoreDetail._absent.doubleValue();
                    if (rtn) {
                        if (param._isOutputDebug) {
                            log.info(" student " + student._attendno +  ":" + student._schregno + ", subclass " + scoreDetail._subclass._subclasscd + ", 時数 " + jisu + ", 上限 " + jougenti + ", 欠席 " + scoreDetail._absent + (isFutoukouTaiou ? " (不登校傾向対応)" : ""));
                        }
                    }
                }
            }
            return rtn;
        }

        /**
         *  PrepareStatement作成 --> 成績・評定・欠課データの表
         */
        private String sqlStdSubclassDetail(final Param param) {
            final StringBuffer stb = new StringBuffer();
            final String testkindcd = param._testKindCd.substring(0, 2);
            final String testitemcd = param._testKindCd.substring(2, 4);
            final String scoreDiv = param._testKindCd.substring(4);
            stb.append("WITH ");

            //対象生徒の表 クラスの生徒
            stb.append(" SCHNO_A AS(");
            stb.append("     SELECT  W1.SCHREGNO,W1.YEAR,W1.SEMESTER ");
            stb.append("            ,W1.GRADE, W1.HR_CLASS, W1.COURSECD, W1.MAJORCD, W1.COURSECODE ");
            stb.append("            , 0 AS LEAVE ");
            stb.append("     FROM    SCHREG_REGD_DAT W1 ");
            stb.append("     INNER JOIN V_SEMESTER_GRADE_MST W2 ON W2.YEAR = W1.YEAR AND W2.SEMESTER = W1.SEMESTER AND W2.GRADE = W1.GRADE ");

            stb.append("     WHERE   W1.YEAR = '" + param._year + "' ");
            if (SEMEALL.equals(param._semester)) {
                stb.append("     AND W1.SEMESTER = '" + param._semeFlg + "' ");
            } else {
                stb.append("     AND W1.SEMESTER = '" + param._semester + "' ");
                stb.append("     AND W1.SCHREGNO NOT IN (SELECT S1.SCHREGNO FROM  SCHREG_BASE_MST S1");
                stb.append("                    WHERE S1.GRD_DIV IN ('2','3','6','7') AND S1.GRD_DATE < W2.SDATE) ");
            }

            if (param._isPrintGroupGrade) {
                stb.append("         AND W1.GRADE = '" + param._grade + "' ");
                if (PRGID_KNJD615P.equals(param._prgId) && OUTPUT_ORDER_2_SEISEKIJUN.equals(param._outputOrder)) {
                    stb.append("         AND W1.GRADE || W1.HR_CLASS IN " + SQLUtils.whereIn(true, param._paramClassSelected));
                }
            } else if (param._isPrintGroupCourse) {
                stb.append("         AND W1.GRADE = '" + param._grade + "' ");
                stb.append("         AND W1.COURSECD = ? ");
                stb.append("         AND W1.MAJORCD = ? ");
                stb.append("         AND W1.COURSECODE = ? ");
            } else { // if (param._isPrintGroupHr) {
                stb.append("         AND W1.GRADE = ? AND W1.HR_CLASS = ? ");
            }

            stb.append(") ");

            //対象講座の表
            stb.append(",CHAIR_A AS(");
            stb.append("     SELECT W1.SCHREGNO, W2.CHAIRCD, ");
            stb.append("            W2.CLASSCD, W2.SCHOOL_KIND, W2.CURRICULUM_CD, W2.SUBCLASSCD AS SUBCLASSCD_ONLY, ");
            stb.append("            W2.SEMESTER, W1.APPDATE, W1.APPENDDATE ");
            stb.append("     FROM   CHAIR_STD_DAT W1 ");
            stb.append("     INNER JOIN CHAIR_DAT W2 ON W2.YEAR = W1.YEAR ");
            stb.append("         AND W2.SEMESTER = W1.SEMESTER ");
            stb.append("         AND W2.CHAIRCD = W1.CHAIRCD ");
            stb.append("     INNER JOIN SEMESTER_MST SEME ON SEME.YEAR = W1.YEAR ");
            stb.append("         AND SEME.SEMESTER = W1.SEMESTER ");
            if ("1".equals(param._printSubclassLastChairStd)) {
                stb.append("         AND SEME.EDATE = W1.APPENDDATE ");
            }
            stb.append("     INNER JOIN SCHNO_A W3 ON W3.SCHREGNO = W1.SCHREGNO ");
            stb.append("     WHERE  W1.YEAR = '" + param._year + "' ");
            stb.append("        AND W1.SEMESTER <= '" + param._semester + "' ");
            stb.append("     )");

            stb.append(",T_CHAIR_STF AS(");
            stb.append("     SELECT W1.CHAIRCD, W1.SEMESTER, MIN(W3.STAFFCD) AS STAFFCD ");
            stb.append("     FROM   CHAIR_A W1 ");
            stb.append("     INNER JOIN CHAIR_STF_DAT W3 ON W3.YEAR = '" + param._year + "' ");
            stb.append("         AND W3.SEMESTER = W1.SEMESTER ");
            stb.append("         AND W3.CHAIRCD = W1.CHAIRCD ");
            stb.append("         AND W3.CHARGEDIV = 1 ");
            stb.append("     GROUP BY W1.CHAIRCD, W1.SEMESTER ");
            stb.append("     )");

            stb.append(",CREDITS_A AS(");
            stb.append("    SELECT  T1.SCHREGNO, ");
            stb.append("            CRED.CLASSCD, CRED.SCHOOL_KIND, CRED.CURRICULUM_CD, CRED.SUBCLASSCD, CRED.CREDITS ");
            stb.append("    FROM    SCHNO_A T1 ");
            stb.append("    INNER JOIN (SELECT SCHREGNO, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD_ONLY ");
            stb.append("                FROM CHAIR_A ");
            stb.append("                GROUP BY SCHREGNO, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD_ONLY ");
            stb.append("               ) T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("    INNER JOIN CREDIT_MST CRED ON ");
            stb.append("            CRED.YEAR = '" + param._year + "' ");
            stb.append("        AND CRED.GRADE = T1.GRADE ");
            stb.append("        AND CRED.COURSECD = T1.COURSECD ");
            stb.append("        AND CRED.MAJORCD = T1.MAJORCD ");
            stb.append("        AND CRED.COURSECODE = T1.COURSECODE ");
            stb.append("        AND CRED.CLASSCD = T2.CLASSCD ");
            stb.append("        AND CRED.SCHOOL_KIND = T2.SCHOOL_KIND ");
            stb.append("        AND CRED.CURRICULUM_CD = T2.CURRICULUM_CD ");
            stb.append("        AND CRED.SUBCLASSCD = T2.SUBCLASSCD_ONLY ");
            stb.append(") ");

            // 単位数の表
            stb.append(",CREDITS_B AS(");
            stb.append("    SELECT ");
            stb.append("         T1.SCHREGNO ");
            stb.append("       , T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD ");
            stb.append("       , T1.CREDITS");
            stb.append("    FROM CREDITS_A T1");
            stb.append("    WHERE (T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD) ");
            stb.append("          NOT IN (SELECT T2.COMBINED_CLASSCD, T2.COMBINED_SCHOOL_KIND, T2.COMBINED_CURRICULUM_CD, T2.COMBINED_SUBCLASSCD ");
            stb.append("                  FROM SUBCLASS_REPLACE_COMBINED_DAT T2 ");
            stb.append("                  WHERE  T2.YEAR = '" + param._year + "' ");
            stb.append("                     AND T2.CALCULATE_CREDIT_FLG = '2'");
            stb.append("                  ) ");
            stb.append("    UNION ");
            stb.append("    SELECT ");
            stb.append("         T1.SCHREGNO ");
            stb.append("       , T2.COMBINED_CLASSCD AS CLASSCD, T2.COMBINED_SCHOOL_KIND AS SCHOOL_KIND, T2.COMBINED_CURRICULUM_CD AS CURRICULUM_CD, COMBINED_SUBCLASSCD AS SUBCLASSCD ");
            stb.append("       , SUM(T1.CREDITS) AS CREDITS ");
            stb.append("    FROM CREDITS_A T1 ");
            stb.append("    INNER JOIN SUBCLASS_REPLACE_COMBINED_DAT T2 ON ");
            stb.append("            T2.YEAR = '" + param._year + "' ");
            stb.append("        AND T2.CALCULATE_CREDIT_FLG = '2'");
            stb.append("        AND T2.ATTEND_CLASSCD = T1.CLASSCD ");
            stb.append("        AND T2.ATTEND_SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("        AND T2.ATTEND_CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("        AND T2.ATTEND_SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("    GROUP BY T1.SCHREGNO, ");
            stb.append("            T2.COMBINED_CLASSCD, T2.COMBINED_SCHOOL_KIND, T2.COMBINED_CURRICULUM_CD, T2.COMBINED_SUBCLASSCD ");
            stb.append(") ");
            stb.append(" , REL_COUNT AS (");
            stb.append("   SELECT SUBCLASSCD");
            stb.append("     , CLASSCD ");
            stb.append("     , SCHOOL_KIND ");
            stb.append("     , CURRICULUM_CD ");
            stb.append("     , COUNT(*) AS COUNT ");
            stb.append("   FROM RELATIVEASSESS_MST ");
            stb.append("   WHERE GRADE = '" + param._grade + "' AND ASSESSCD = '3' ");
            stb.append("   GROUP BY SUBCLASSCD");
            stb.append("     , CLASSCD ");
            stb.append("     , SCHOOL_KIND ");
            stb.append("     , CURRICULUM_CD ");
            stb.append(" ) ");

            //成績データの表（通常科目）
            stb.append(",RECORD_REC AS(");
            stb.append("    SELECT  W3.SCHREGNO, ");
            stb.append("            W3.CLASSCD, W3.SCHOOL_KIND, W3.CURRICULUM_CD, W3.SUBCLASSCD ");
            stb.append("     , W3.SCORE ");
            stb.append("     , W3.CLASS_DEVIATION ");
            stb.append("     , W3.GRADE_DEVIATION ");
            stb.append("     , W3.COURSE_DEVIATION ");
            stb.append("     , W3.MAJOR_DEVIATION ");
            stb.append("     , CASE WHEN VALUE(T2.COUNT, 0) > 0 THEN ");
            stb.append("          (SELECT MAX(L3.ASSESSLEVEL) ");
            stb.append("           FROM RELATIVEASSESS_MST L3 ");
            stb.append("           WHERE L3.GRADE = '" + param._grade + "' AND L3.ASSESSCD = '3' ");
            stb.append("             AND W3.SCORE BETWEEN L3.ASSESSLOW AND L3.ASSESSHIGH  ");
            stb.append("             AND L3.SUBCLASSCD = W3.SUBCLASSCD ");
            stb.append("             AND L3.CLASSCD = W3.CLASSCD ");
            stb.append("             AND L3.SCHOOL_KIND = W3.SCHOOL_KIND ");
            stb.append("             AND L3.CURRICULUM_CD = W3.CURRICULUM_CD ");
            stb.append("          ) ELSE ");
            if ("1".equals(param._useAssessCourseMst)) {
                stb.append("          VALUE((SELECT MAX(L3.ASSESSLEVEL) ");
                stb.append("           FROM ASSESS_COURSE_MST L3 ");
                stb.append("           WHERE L3.ASSESSCD = '3' ");
                stb.append("             AND L3.COURSECD = SCH.COURSECD  ");
                stb.append("             AND L3.MAJORCD = SCH.MAJORCD  ");
                stb.append("             AND L3.COURSECODE = SCH.COURSECODE  ");
                stb.append("             AND W3.SCORE BETWEEN L3.ASSESSLOW AND L3.ASSESSHIGH  ");
                stb.append("           ),  ");
                stb.append("           (SELECT MAX(L3.ASSESSLEVEL) ");
                stb.append("           FROM ASSESS_MST L3 ");
                stb.append("           WHERE L3.ASSESSCD = '3' ");
                stb.append("             AND W3.SCORE BETWEEN L3.ASSESSLOW AND L3.ASSESSHIGH  ");
                stb.append("          )) ");
            } else {
                stb.append("          (SELECT MAX(L3.ASSESSLEVEL) ");
                stb.append("           FROM ASSESS_MST L3 ");
                stb.append("           WHERE L3.ASSESSCD = '3' ");
                stb.append("             AND W3.SCORE BETWEEN L3.ASSESSLOW AND L3.ASSESSHIGH  ");
                stb.append("          ) ");
            }
            stb.append("         END AS ASSESS_LEVEL ");
            stb.append("    FROM    RECORD_RANK_SDIV_DAT W3 ");
            stb.append("    INNER JOIN SCHNO_A SCH ON W3.SCHREGNO = SCH.SCHREGNO ");
            stb.append("       AND SCH.LEAVE = 0 ");
            stb.append("    LEFT JOIN REL_COUNT T2 ON T2.SUBCLASSCD = W3.SUBCLASSCD ");
            stb.append("            AND T2.CLASSCD = W3.CLASSCD ");
            stb.append("            AND T2.SCHOOL_KIND = W3.SCHOOL_KIND ");
            stb.append("            AND T2.CURRICULUM_CD = W3.CURRICULUM_CD ");
            stb.append("    WHERE   W3.YEAR = '" + param._year + "' ");
            stb.append("            AND W3.SEMESTER = '" + param._semester + "' ");
            stb.append("            AND W3.TESTKINDCD = '" + testkindcd + "' ");
            stb.append("            AND W3.TESTITEMCD = '" + testitemcd + "' ");
            stb.append("            AND W3.SCORE_DIV = '" + scoreDiv + "' ");
            stb.append("     ) ");

            //仮評定の表
            stb.append(",RECORD_KARI_HYOUTEI AS(");
            stb.append("    SELECT  W3.SCHREGNO ");
            stb.append("           ,W3.CLASSCD ");
            stb.append("           ,W3.SCHOOL_KIND ");
            stb.append("           ,W3.CURRICULUM_CD ");
            stb.append("           ,W3.SUBCLASSCD AS SUBCLASSCD_ONLY ");
            stb.append("           ,W3.SCORE AS KARI_HYOUTEI ");
            stb.append("           ,T2.PROV_FLG ");
            stb.append("    FROM    RECORD_SCORE_DAT W3 ");
            stb.append("    INNER JOIN SCHNO_A W1 ON W3.SCHREGNO = W1.SCHREGNO ");
            stb.append("       AND W1.LEAVE = 0 ");
            stb.append("    LEFT JOIN RECORD_PROV_FLG_DAT T2 ON T2.YEAR = W3.YEAR ");
            stb.append("            AND T2.CLASSCD = W3.CLASSCD ");
            stb.append("            AND T2.SCHOOL_KIND = W3.SCHOOL_KIND ");
            stb.append("            AND T2.CURRICULUM_CD = W3.CURRICULUM_CD ");
            stb.append("            AND T2.SUBCLASSCD = W3.SUBCLASSCD ");
            stb.append("            AND T2.SCHREGNO = W3.SCHREGNO ");
            stb.append("            AND T2.PROV_FLG = '1' ");
            stb.append("    WHERE   W3.YEAR = '" + param._year + "' ");
            stb.append("        AND W3.SEMESTER = '" + param._semester + "' ");
            stb.append("        AND W3.TESTKINDCD = '" + testkindcd + "' ");
            stb.append("        AND W3.TESTITEMCD = '" + testitemcd + "' ");
            stb.append("        AND W3.SCORE_DIV = '" + SCORE_DIV_09 + "' ");
            stb.append("     ) ");

            //成績不振科目データの表
            stb.append(",RECORD_SLUMP AS(");
            stb.append("    SELECT  W3.SCHREGNO, ");
            stb.append("            W3.CLASSCD, W3.SCHOOL_KIND, W3.CURRICULUM_CD, W3.SUBCLASSCD, ");
            stb.append("            CASE WHEN W1.SIDOU_INPUT = '1' THEN W3.SLUMP END AS SLUMP, ");
            stb.append("            CASE WHEN W1.SIDOU_INPUT = '1' AND W1.SIDOU_INPUT_INF = '1' THEN W3.MARK END AS SLUMP_MARK, ");
            stb.append("            CASE WHEN W1.SIDOU_INPUT = '1' AND W1.SIDOU_INPUT_INF = '2' THEN ");
            stb.append("         CASE WHEN VALUE(T2.COUNT, 0) > 0 THEN ");
            stb.append("          (SELECT MAX(L3.ASSESSLEVEL) ");
            stb.append("           FROM RELATIVEASSESS_MST L3 ");
            stb.append("           WHERE L3.GRADE = '" + param._grade + "' AND L3.ASSESSCD = '3' ");
            stb.append("             AND W3.SCORE BETWEEN L3.ASSESSLOW AND L3.ASSESSHIGH  ");
            stb.append("             AND L3.SUBCLASSCD = W3.SUBCLASSCD ");
            stb.append("             AND L3.CLASSCD = W3.CLASSCD ");
            stb.append("             AND L3.SCHOOL_KIND = W3.SCHOOL_KIND ");
            stb.append("             AND L3.CURRICULUM_CD = W3.CURRICULUM_CD ");
            stb.append("          ) ELSE ");

            if ("1".equals(param._useAssessCourseMst)) {
                stb.append("          VALUE((SELECT MAX(L3.ASSESSLEVEL) ");
                stb.append("           FROM ASSESS_COURSE_MST L3 ");
                stb.append("           WHERE L3.ASSESSCD = '3' ");
                stb.append("             AND L3.COURSECD = SCH.COURSECD  ");
                stb.append("             AND L3.MAJORCD = SCH.MAJORCD  ");
                stb.append("             AND L3.COURSECODE = SCH.COURSECODE  ");
                stb.append("             AND W3.SCORE BETWEEN L3.ASSESSLOW AND L3.ASSESSHIGH  ");
                stb.append("           ),  ");
                stb.append("           (SELECT MAX(L3.ASSESSLEVEL) ");
                stb.append("           FROM ASSESS_MST L3 ");
                stb.append("           WHERE L3.ASSESSCD = '3' ");
                stb.append("             AND W3.SCORE BETWEEN L3.ASSESSLOW AND L3.ASSESSHIGH  ");
                stb.append("          )) ");
            } else {
                stb.append("          (SELECT MAX(L3.ASSESSLEVEL) ");
                stb.append("           FROM ASSESS_MST L3 ");
                stb.append("           WHERE L3.ASSESSCD = '3' ");
                stb.append("             AND W3.SCORE BETWEEN L3.ASSESSLOW AND L3.ASSESSHIGH  ");
                stb.append("          ) ");
            }
            stb.append("         END ");
            stb.append("        END AS SLUMP_SCORE ");
            stb.append("    FROM    RECORD_SLUMP_SDIV_DAT W3 ");
            stb.append("    INNER JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV W1 ON W1.YEAR = W3.YEAR ");
            stb.append("            AND W3.SEMESTER = W1.SEMESTER ");
            stb.append("            AND W3.TESTKINDCD = W1.TESTKINDCD ");
            stb.append("            AND W3.TESTITEMCD = W1.TESTITEMCD ");
            stb.append("            AND W3.SCORE_DIV = W1.SCORE_DIV ");
            stb.append("    LEFT JOIN REL_COUNT T2 ON T2.SUBCLASSCD = W3.SUBCLASSCD ");
            stb.append("            AND T2.CLASSCD = W3.CLASSCD ");
            stb.append("            AND T2.SCHOOL_KIND = W3.SCHOOL_KIND ");
            stb.append("            AND T2.CURRICULUM_CD = W3.CURRICULUM_CD ");
            stb.append("    INNER JOIN SCHNO_A SCH ON SCH.SCHREGNO = W3.SCHREGNO ");
            stb.append("       AND SCH.LEAVE = 0 ");
            stb.append("    WHERE   W3.YEAR = '" + param._year + "' ");
            stb.append("            AND W3.SEMESTER = '" + param._semester + "' ");
            stb.append("            AND W3.TESTKINDCD = '" + testkindcd + "' ");
            stb.append("            AND W3.TESTITEMCD = '" + testitemcd + "' ");
            stb.append("            AND W3.SCORE_DIV = '" + scoreDiv + "' ");
            stb.append("     ) ");

            stb.append(" ,CHAIR_A2 AS ( ");
            stb.append("     SELECT  T1.SCHREGNO, T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD_ONLY, ");
            stb.append("             MIN(T1.CHAIRCD) AS CHAIRCD, ");
            stb.append("             MIN(T2.STAFFCD) AS STAFFCD ");
            stb.append("     FROM    CHAIR_A T1");
            stb.append("     LEFT JOIN T_CHAIR_STF T2 ON T2.SEMESTER = T1.SEMESTER ");
            stb.append("         AND T2.CHAIRCD = T1.CHAIRCD ");
            if (!SEMEALL.equals(param._semester)) {
                stb.append(" WHERE   T1.SEMESTER = '" + param._semester + "'");
            }
            stb.append("     GROUP BY T1.SCHREGNO, T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD_ONLY ");
            stb.append(" ) ");

            stb.append(" ,COMBINED_SUBCLASS AS ( ");
            stb.append("    SELECT ");
            stb.append("           COMBINED_CLASSCD AS CLASSCD, COMBINED_SCHOOL_KIND AS SCHOOL_KIND, COMBINED_CURRICULUM_CD AS CURRICULUM_CD, COMBINED_SUBCLASSCD AS SUBCLASSCD");
            stb.append("    FROM   SUBCLASS_REPLACE_COMBINED_DAT");
            stb.append("    WHERE  YEAR = '" + param._year + "'");
            stb.append("    GROUP BY ");
            stb.append("           COMBINED_CLASSCD, COMBINED_SCHOOL_KIND, COMBINED_CURRICULUM_CD, COMBINED_SUBCLASSCD");
            stb.append(" ) ");

            stb.append(" ,ATTEND_SUBCLASS AS ( ");
            stb.append("    SELECT ");
            stb.append("           ATTEND_CLASSCD AS CLASSCD, ATTEND_SCHOOL_KIND AS SCHOOL_KIND, ATTEND_CURRICULUM_CD AS CURRICULUM_CD, ATTEND_SUBCLASSCD AS SUBCLASSCD, MAX(PRINT_FLG2) AS PRINT_FLG");
            stb.append("    FROM   SUBCLASS_REPLACE_COMBINED_DAT");
            stb.append("    WHERE  YEAR = '" + param._year + "'");
            stb.append("    GROUP BY ");
            stb.append("           ATTEND_CLASSCD, ATTEND_SCHOOL_KIND, ATTEND_CURRICULUM_CD, ATTEND_SUBCLASSCD");
            stb.append(" ) ");

            //メイン表
            stb.append(" SELECT ");
            stb.append("         T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD_ONLY AS SUBCLASSCD ");
            stb.append("        ,T1.SCHREGNO ");
            stb.append("        ,T1.CHAIRCD ");
            stb.append("        ,T3.SCORE ");
            stb.append("        ,RSD.SCORE AS RECORD_SCORE_DAT_SCORE ");
            stb.append("        ,T3.CURRICULUM_CD ");
            stb.append("        ,T3.CLASS_DEVIATION ");
            stb.append("        ,T3.GRADE_DEVIATION ");
            stb.append("        ,T3.COURSE_DEVIATION ");
            stb.append("        ,T3.MAJOR_DEVIATION ");
            stb.append("        ,RSD.VALUE_DI ");
            stb.append("        ,T3.ASSESS_LEVEL ");
            stb.append("        ,SUPP.SCORE AS SUPP_SCORE ");
            stb.append("        ,RSD.COMP_CREDIT ");
            stb.append("        ,RSD.GET_CREDIT ");
            stb.append("        ,KARIHYO.KARI_HYOUTEI ");
            stb.append("        ,KARIHYO.PROV_FLG ");
            stb.append("        ,CREM.CREDITS ");
            if (param._isPrintPerfect) {
                stb.append("        ,VALUE(PERF.PERFECT, 100) AS PERFECT ");
            } else {
                stb.append("        ,CASE WHEN PERF.DIV IS NULL THEN 100 ELSE PERF.PERFECT END AS PERFECT ");
            }
            stb.append("        ,PERF.PASS_SCORE ");
            stb.append("        ,SUBM.SUBCLASSABBV ");
            stb.append("        ,SUBM.SUBCLASSNAME ");
            stb.append("        ,CM.CLASSABBV AS CLASSNAME ");
            stb.append("        ,SUBM.ELECTDIV ");
            stb.append("        ,CASE WHEN T9.SUBCLASSCD IS NOT NULL THEN -1");
            stb.append("              WHEN T10.SUBCLASSCD IS NOT NULL THEN 1 ELSE 0 END AS REPLACEMOTO ");
            stb.append("        ,VALUE(T10.PRINT_FLG,'0') AS PRINT_FLG");
            stb.append("        ,K1.SLUMP ");
            stb.append("        ,K1.SLUMP_MARK ");
            stb.append("        ,K1.SLUMP_SCORE ");

            stb.append("    , STFM.STAFFNAME ");
            stb.append("    , RAD.STDDEV ");
            //対象生徒・講座の表
            stb.append(" FROM CHAIR_A2 T1 ");
            stb.append(" INNER JOIN SCHNO_A SCH ON SCH.SCHREGNO = T1.SCHREGNO ");
            //成績の表
            stb.append(" LEFT JOIN RECORD_REC T3 ON T3.CLASSCD = T1.CLASSCD AND T3.SCHOOL_KIND = T1.SCHOOL_KIND AND T3.CURRICULUM_CD = T1.CURRICULUM_CD AND T3.SUBCLASSCD = T1.SUBCLASSCD_ONLY AND T3.SCHREGNO = T1.SCHREGNO");

            stb.append(" LEFT JOIN RECORD_SCORE_DAT RSD ON RSD.YEAR = '" + param._year + "' ");
            stb.append("        AND RSD.SEMESTER = '" + param._semester + "' ");
            stb.append("        AND RSD.TESTKINDCD = '" + testkindcd + "' ");
            stb.append("        AND RSD.TESTITEMCD = '" + testitemcd + "' ");
            stb.append("        AND RSD.SCORE_DIV = '" + scoreDiv + "' ");
            stb.append("        AND RSD.CLASSCD = T1.CLASSCD AND RSD.SCHOOL_KIND = T1.SCHOOL_KIND AND RSD.CURRICULUM_CD = T1.CURRICULUM_CD AND RSD.SUBCLASSCD = T1.SUBCLASSCD_ONLY AND RSD.SCHREGNO = T1.SCHREGNO ");

            stb.append(" LEFT JOIN RECORD_KARI_HYOUTEI KARIHYO ON KARIHYO.CLASSCD = T1.CLASSCD AND KARIHYO.SCHOOL_KIND = T1.SCHOOL_KIND AND KARIHYO.CURRICULUM_CD = T1.CURRICULUM_CD AND KARIHYO.SUBCLASSCD_ONLY = T1.SUBCLASSCD_ONLY AND KARIHYO.SCHREGNO = T1.SCHREGNO");
            //合併先科目の表
            stb.append(" LEFT JOIN COMBINED_SUBCLASS T9 ON T9.CLASSCD = T1.CLASSCD AND T9.SCHOOL_KIND = T1.SCHOOL_KIND AND T9.CURRICULUM_CD = T1.CURRICULUM_CD AND T9.SUBCLASSCD = T1.SUBCLASSCD_ONLY ");
            //合併元科目の表
            stb.append(" LEFT JOIN ATTEND_SUBCLASS T10 ON T10.CLASSCD = T1.CLASSCD AND T10.SCHOOL_KIND = T1.SCHOOL_KIND AND T10.CURRICULUM_CD = T1.CURRICULUM_CD AND T10.SUBCLASSCD = T1.SUBCLASSCD_ONLY ");

            stb.append(" LEFT JOIN CREDITS_B CREM ON CREM.CLASSCD = T1.CLASSCD AND CREM.SCHOOL_KIND = T1.SCHOOL_KIND AND CREM.CURRICULUM_CD = T1.CURRICULUM_CD AND CREM.SUBCLASSCD = T1.SUBCLASSCD_ONLY AND CREM.SCHREGNO = T1.SCHREGNO");
            stb.append(" LEFT JOIN SUBCLASS_MST SUBM ON SUBM.CLASSCD = T1.CLASSCD AND SUBM.SCHOOL_KIND = T1.SCHOOL_KIND AND SUBM.CURRICULUM_CD = T1.CURRICULUM_CD AND SUBM.SUBCLASSCD = T1.SUBCLASSCD_ONLY ");
            stb.append(" LEFT JOIN CLASS_MST CM ON CM.CLASSCD = T1.CLASSCD AND CM.SCHOOL_KIND = T1.SCHOOL_KIND ");
            //成績不振科目データの表
            stb.append(" LEFT JOIN RECORD_SLUMP K1 ON K1.SCHREGNO = T1.SCHREGNO AND K1.CLASSCD = T1.CLASSCD AND K1.SCHOOL_KIND = T1.SCHOOL_KIND AND K1.CURRICULUM_CD = T1.CURRICULUM_CD AND K1.SUBCLASSCD = T1.SUBCLASSCD_ONLY ");
            stb.append(" LEFT JOIN STAFF_MST STFM ON STFM.STAFFCD = T1.STAFFCD ");
            stb.append(" LEFT JOIN SUBCLASS_DETAIL_DAT SDET ON SDET.YEAR = '" + param._year + "' AND SDET.CLASSCD = T1.CLASSCD AND SDET.SCHOOL_KIND = T1.SCHOOL_KIND AND SDET.CURRICULUM_CD = T1.CURRICULUM_CD AND ");
            stb.append("     SDET.SUBCLASSCD = T1.SUBCLASSCD_ONLY AND ");
            stb.append("     SDET.SUBCLASS_SEQ = '012' ");
            stb.append(" LEFT JOIN PERFECT_RECORD_DAT PERF ON PERF.YEAR = '" + param._year + "' AND PERF.SEMESTER = '" + param._semester + "' ");
            stb.append("     AND PERF.TESTKINDCD = '" + testkindcd + "' ");
            stb.append("     AND PERF.TESTITEMCD = '" + testitemcd + "' ");
            stb.append("     AND PERF.CLASSCD = T1.CLASSCD AND PERF.SCHOOL_KIND = T1.SCHOOL_KIND AND PERF.CURRICULUM_CD = T1.CURRICULUM_CD AND PERF.SUBCLASSCD = T1.SUBCLASSCD_ONLY ");
            stb.append("     AND PERF.GRADE = CASE WHEN DIV = '01' THEN '00' ELSE SCH.GRADE END ");
            stb.append("     AND (PERF.DIV IN ('01','02')  AND PERF.COURSECD = '0' AND PERF.MAJORCD = '000' AND PERF.COURSECODE = '0000' ");
            stb.append("       OR PERF.DIV NOT IN ('01','02')  AND PERF.COURSECD = SCH.COURSECD AND PERF.MAJORCD = SCH.MAJORCD AND PERF.COURSECODE = SCH.COURSECODE ");
            stb.append("         ) ");
            stb.append(" LEFT JOIN SUPP_EXA_SDIV_DAT SUPP ON SUPP.YEAR = '" + param._year + "' ");
            stb.append("             AND SUPP.SEMESTER = '" + param._semester + "' ");
            stb.append("             AND SUPP.TESTKINDCD = '" + testkindcd + "' ");
            stb.append("             AND SUPP.TESTITEMCD = '" + testitemcd + "' ");
            stb.append("             AND SUPP.SCORE_DIV = '" + scoreDiv + "' ");
            stb.append("             AND SUPP.CLASSCD = T1.CLASSCD AND SUPP.SCHOOL_KIND = T1.SCHOOL_KIND AND SUPP.CURRICULUM_CD = T1.CURRICULUM_CD AND SUPP.SUBCLASSCD = T1.SUBCLASSCD_ONLY ");
            stb.append("             AND SUPP.SCHREGNO = T1.SCHREGNO ");
            stb.append("             AND SUPP.SCORE_FLG = '2' ");
            stb.append(" LEFT JOIN RECORD_AVERAGE_SDIV_DAT RAD ");
            stb.append("        ON RAD.YEAR          = '"+ param._year +"' ");
            stb.append("       AND RAD.SEMESTER      = '"+ param._semester +"' ");
            stb.append("       AND RAD.TESTKINDCD    = '"+ testkindcd +"' ");
            stb.append("       AND RAD.TESTITEMCD    = '"+ testitemcd +"' ");
            stb.append("       AND RAD.SCORE_DIV     = '"+ scoreDiv +"' ");
            stb.append("       AND RAD.CLASSCD       = T1.CLASSCD ");
            stb.append("       AND RAD.SCHOOL_KIND   = T1.SCHOOL_KIND ");
            stb.append("       AND RAD.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("       AND RAD.SUBCLASSCD    = T1.SUBCLASSCD_ONLY ");
            stb.append("       AND RAD.AVG_DIV       = '2' ");
            stb.append("       AND RAD.GRADE         = SCH.GRADE ");
            stb.append("       AND RAD.HR_CLASS      = SCH.HR_CLASS ");
            stb.append("       AND RAD.COURSECD || RAD.MAJORCD || RAD.COURSECODE = '00000000' ");
            if (null != param._takeSemes && !SEMEALL.equals(param._takeSemes)) {
                stb.append(" WHERE ");
                stb.append("     ((SDET.SUBCLASS_REMARK" + param._takeSemes + " = '1' ");
                if (param._takeSemes.equals(param._knjSchoolMst._semesterDiv)) {
                    stb.append("   OR  SDET.SUBCLASS_REMARK1 IS NULL AND SDET.SUBCLASS_REMARK2 IS NULL AND SDET.SUBCLASS_REMARK3 IS NULL ");
                }
                stb.append("      ) ");
                if (param._takeSemes.equals(param._knjSchoolMst._semesterDiv)) {
                    stb.append("   OR SDET.SUBCLASSCD IS NULL ");
                }
                stb.append("     ) ");
            }
            stb.append(" ORDER BY T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD_ONLY, T1.SCHREGNO");

            return stb.toString();
        }

        /**
         *  PrepareStatement作成 --> 成績・評定・欠課データの表
         */
        private String sqlStdSubclassStaff(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append("WITH ");

            //対象生徒の表 クラスの生徒
            stb.append(" SCHNO_A AS(");
            stb.append("     SELECT  W1.SCHREGNO,W1.YEAR,W1.SEMESTER ");
            stb.append("            ,W1.GRADE, W1.HR_CLASS, W1.COURSECD, W1.MAJORCD, W1.COURSECODE ");
            stb.append("            , 0 AS LEAVE ");
            stb.append("     FROM    SCHREG_REGD_DAT W1 ");
            stb.append("     INNER JOIN V_SEMESTER_GRADE_MST W2 ON W2.YEAR = W1.YEAR AND W2.SEMESTER = W1.SEMESTER AND W2.GRADE = W1.GRADE ");

            stb.append("     WHERE   W1.YEAR = '" + param._year + "' ");
            if (SEMEALL.equals(param._semester)) {
                stb.append("     AND W1.SEMESTER = '" + param._semeFlg + "' ");
            } else {
                stb.append("     AND W1.SEMESTER = '" + param._semester + "' ");
                stb.append("     AND W1.SCHREGNO NOT IN (SELECT S1.SCHREGNO FROM  SCHREG_BASE_MST S1");
                stb.append("                    WHERE S1.GRD_DIV IN ('2','3','6','7') AND S1.GRD_DATE < W2.SDATE) ");
            }

            if (param._isPrintGroupGrade) {
                stb.append("         AND W1.GRADE = '" + param._grade + "' ");
            } else if (param._isPrintGroupCourse) {
                stb.append("         AND W1.GRADE = '" + param._grade + "' ");
                stb.append("         AND W1.COURSECD = ? ");
                stb.append("         AND W1.MAJORCD = ? ");
                stb.append("         AND W1.COURSECODE = ? ");
            } else { // if (param._isPrintGroupHr) {
                stb.append("         AND W1.GRADE = ? AND W1.HR_CLASS = ? ");
            }

            stb.append(") ");

            //対象講座の表
            stb.append(",CHAIR_A AS(");
            stb.append("     SELECT W1.SCHREGNO, W2.CHAIRCD, ");
            stb.append("            W2.CLASSCD, W2.SCHOOL_KIND, W2.CURRICULUM_CD, W2.SUBCLASSCD AS SUBCLASSCD_ONLY, ");
            stb.append("            W2.SEMESTER, W1.APPDATE, W1.APPENDDATE ");
            stb.append("     FROM   CHAIR_STD_DAT W1 ");
            stb.append("     INNER JOIN CHAIR_DAT W2 ON W2.YEAR = W1.YEAR ");
            stb.append("         AND W2.SEMESTER = W1.SEMESTER ");
            stb.append("         AND W2.CHAIRCD = W1.CHAIRCD ");
            stb.append("     INNER JOIN SEMESTER_MST SEME ON SEME.YEAR = W1.YEAR ");
            stb.append("         AND SEME.SEMESTER = W1.SEMESTER ");
            if ("1".equals(param._printSubclassLastChairStd)) {
                stb.append("         AND SEME.EDATE = W1.APPENDDATE ");
            }
            stb.append("     WHERE  W1.YEAR = '" + param._year + "' ");
            stb.append("        AND W1.SEMESTER <= '" + param._semester + "' ");
            stb.append("        AND EXISTS( ");
            stb.append("            SELECT 'X' ");
            stb.append("            FROM SCHNO_A W3 ");
            stb.append("                 LEFT JOIN SCHREG_REGD_GDAT WG ON WG.YEAR = W3.YEAR AND WG.GRADE = W3.GRADE ");
            stb.append("            WHERE W3.SCHREGNO = W1.SCHREGNO");
            stb.append("              AND WG.SCHOOL_KIND = W2.SCHOOL_KIND ");
            stb.append("        )");
            stb.append("     )");

            stb.append(",CHAIR_STF AS(");
            stb.append("     SELECT W1.CHAIRCD, W1.SEMESTER, MIN(STAFFCD) AS STAFFCD ");
            stb.append("     FROM   CHAIR_A W1 ");
            stb.append("     LEFT JOIN CHAIR_STF_DAT W3 ON W3.YEAR = '" + param._year + "' ");
            stb.append("         AND W3.SEMESTER = W1.SEMESTER ");
            stb.append("         AND W3.CHAIRCD = W1.CHAIRCD ");
            stb.append("         AND W3.CHARGEDIV = 1 ");
            stb.append("     GROUP BY W1.CHAIRCD, W1.SEMESTER ");
            stb.append("     )");

            stb.append(" ,CHAIR_A2 AS ( ");
            stb.append("     SELECT  W2.SCHREGNO, W2.CLASSCD, W2.SCHOOL_KIND, W2.CURRICULUM_CD, W2.SUBCLASSCD_ONLY, ");
            stb.append("             MIN(W22.STAFFCD) AS STAFFCD ");
            stb.append("     FROM    CHAIR_A W2");
            stb.append("     LEFT JOIN CHAIR_STF W22 ON W22.SEMESTER = W2.SEMESTER ");
            stb.append("         AND W22.CHAIRCD = W2.CHAIRCD ");
            if (!SEMEALL.equals(param._semester)) {
                stb.append(" WHERE   W2.SEMESTER = '" + param._semester + "'");
            }
            stb.append("     GROUP BY W2.SCHREGNO, W2.CLASSCD, W2.SCHOOL_KIND, W2.CURRICULUM_CD, W2.SUBCLASSCD_ONLY ");
            stb.append(" ) ");

            stb.append(" ,CHAIR_A3 AS ( ");
            stb.append("     SELECT  W3.SCHREGNO, W3.CLASSCD, W3.SCHOOL_KIND, W3.CURRICULUM_CD, W3.SUBCLASSCD_ONLY, ");
            stb.append("             MIN(W33.STAFFCD) AS STAFFCD ");
            stb.append("     FROM    CHAIR_A W3");
            stb.append("     LEFT JOIN CHAIR_A2 WA2 ON WA2.SCHREGNO = W3.SCHREGNO AND WA2.SUBCLASSCD_ONLY = W3.SUBCLASSCD_ONLY ");
            stb.append("         AND WA2.CLASSCD = W3.CLASSCD AND WA2.SCHOOL_KIND = W3.SCHOOL_KIND AND WA2.CURRICULUM_CD = W3.CURRICULUM_CD ");
            stb.append("     LEFT JOIN CHAIR_STF W33 ON W33.SEMESTER = W3.SEMESTER ");
            stb.append("         AND W33.CHAIRCD = W3.CHAIRCD ");
            stb.append(" WHERE WA2.STAFFCD IS NOT NULL AND W33.STAFFCD <> WA2.STAFFCD ");
            if (!SEMEALL.equals(param._semester)) {
                stb.append("    AND W3.SEMESTER = '" + param._semester + "'");
            }
            stb.append("     GROUP BY W3.SCHREGNO, W3.CLASSCD, W3.SCHOOL_KIND, W3.CURRICULUM_CD, W3.SUBCLASSCD_ONLY");
            stb.append(" ) ");
            stb.append(" ,CHAIR_A4 AS ( ");
            stb.append("     SELECT  W4.SCHREGNO, ");
            stb.append("             W4.CLASSCD, W4.SCHOOL_KIND, W4.CURRICULUM_CD, W4.SUBCLASSCD_ONLY, ");
            stb.append("             MIN(W44.STAFFCD) AS STAFFCD ");
            stb.append("     FROM    CHAIR_A W4");
            stb.append("       LEFT JOIN CHAIR_A2 WA2 ON WA2.SCHREGNO = W4.SCHREGNO ");
            stb.append("           AND WA2.CLASSCD = W4.CLASSCD AND WA2.SCHOOL_KIND = W4.SCHOOL_KIND AND WA2.CURRICULUM_CD = W4.CURRICULUM_CD AND WA2.SUBCLASSCD_ONLY = W4.SUBCLASSCD_ONLY ");
            stb.append("       LEFT JOIN CHAIR_A3 WA3 ON WA3.SCHREGNO = W4.SCHREGNO ");
            stb.append("           AND WA3.CLASSCD = W4.CLASSCD AND WA3.SCHOOL_KIND = W4.SCHOOL_KIND AND WA3.CURRICULUM_CD = W4.CURRICULUM_CD AND WA3.SUBCLASSCD_ONLY = W4.SUBCLASSCD_ONLY ");
            stb.append("       LEFT JOIN CHAIR_STF W44 ON W44.SEMESTER = W4.SEMESTER ");
            stb.append("           AND W44.CHAIRCD = W4.CHAIRCD ");
            stb.append("     WHERE WA2.STAFFCD IS NOT NULL AND W44.STAFFCD <> WA3.STAFFCD AND W44.STAFFCD <> WA2.STAFFCD ");
            if (!SEMEALL.equals(param._semester)) {
                stb.append(" AND   W4.SEMESTER = '" + param._semester + "'");
            }
            stb.append("     GROUP BY W4.SCHREGNO, W4.CLASSCD, W4.SCHOOL_KIND, W4.CURRICULUM_CD, W4.SUBCLASSCD_ONLY ");
            stb.append(" ) ");

            //メイン表
            stb.append(" SELECT DISTINCT ");
            stb.append("     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD_ONLY AS SUBCLASSCD ");
            stb.append("    , STFM2.STAFFNAME AS STAFF2 ");
            stb.append("    , STFM3.STAFFNAME AS STAFF3 ");
            //対象生徒・講座の表
            stb.append(" FROM CHAIR_A2 T1 ");
            stb.append(" INNER JOIN SCHNO_A SCH ON SCH.SCHREGNO = T1.SCHREGNO ");
            //成績不振科目データの表
            stb.append(" LEFT JOIN SUBCLASS_DETAIL_DAT SDET ON SDET.YEAR = '" + param._year + "' AND SDET.CLASSCD = T1.CLASSCD AND SDET.SCHOOL_KIND = T1.SCHOOL_KIND AND SDET.CURRICULUM_CD = T1.CURRICULUM_CD AND ");
            stb.append("     SDET.SUBCLASSCD = T1.SUBCLASSCD_ONLY AND ");
            stb.append("     SDET.SUBCLASS_SEQ = '012' ");
            stb.append(" LEFT JOIN CHAIR_A3 CA3 ON CA3.SCHREGNO = T1.SCHREGNO ");
            stb.append("     AND CA3.CLASSCD = T1.CLASSCD AND CA3.SCHOOL_KIND = T1.SCHOOL_KIND AND CA3.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("     AND CA3.SUBCLASSCD_ONLY = T1.SUBCLASSCD_ONLY ");
            stb.append(" LEFT JOIN STAFF_MST STFM2 ON STFM2.STAFFCD = CA3.STAFFCD ");
            stb.append(" LEFT JOIN CHAIR_A4 CA4 ON CA4.SCHREGNO = T1.SCHREGNO AND CA4.CLASSCD = T1.CLASSCD AND CA4.SCHOOL_KIND = T1.SCHOOL_KIND AND CA4.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("     AND CA4.SUBCLASSCD_ONLY = T1.SUBCLASSCD_ONLY ");
            stb.append(" LEFT JOIN STAFF_MST STFM3 ON STFM3.STAFFCD = CA4.STAFFCD ");
            if (null != param._takeSemes && !SEMEALL.equals(param._takeSemes)) {
                stb.append(" WHERE ");
                stb.append("     ((SDET.SUBCLASS_REMARK" + param._takeSemes + " = '1' ");
                if (param._takeSemes.equals(param._knjSchoolMst._semesterDiv)) {
                    stb.append("   OR  SDET.SUBCLASS_REMARK1 IS NULL AND SDET.SUBCLASS_REMARK2 IS NULL AND SDET.SUBCLASS_REMARK3 IS NULL ");
                }
                stb.append("      ) ");
                if (param._takeSemes.equals(param._knjSchoolMst._semesterDiv)) {
                    stb.append("   OR SDET.SUBCLASSCD IS NULL ");
                }
                stb.append("     ) ");
            }
            stb.append(" ORDER BY T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD_ONLY ");

            return stb.toString();
        }

        private String sqlStdSubclassScoreDeviation(final Param param) {
            final StringBuffer stb = new StringBuffer();

            stb.append(" SELECT ");
            stb.append("     SDIV.SCHREGNO, ");
            stb.append("     SDIV.CLASSCD ||'-' || SDIV.SCHOOL_KIND || '-'|| SDIV.CURRICULUM_CD || '-'|| SDIV.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("     SDIV.CURRICULUM_CD, ");
            stb.append("     SDIV.SCORE ");
            stb.append(" FROM ");
            stb.append("     RECORD_RANK_SDIV_DAT SDIV ");
            stb.append("     INNER JOIN SUBCLASS_MST T2 ON T2.CLASSCD = SDIV.CLASSCD ");
            stb.append("         AND T2.SCHOOL_KIND = SDIV.SCHOOL_KIND ");
            stb.append("         AND T2.CURRICULUM_CD = SDIV.CURRICULUM_CD ");
            stb.append("         AND T2.SUBCLASSCD = SDIV.SUBCLASSCD ");
            stb.append(" WHERE ");
            stb.append("         SDIV.YEAR     = '" + param._year + "' ");
            stb.append("     AND SDIV.SEMESTER = '" + param._semester + "' ");
            stb.append("     AND SDIV.TESTKINDCD || SDIV.TESTITEMCD || SDIV.SCORE_DIV = '990009' ");
            stb.append("     AND SCHREGNO IN (SELECT ");
            stb.append("                          SCHREGNO ");
            stb.append("                      FROM ");
            stb.append("                          SCHREG_REGD_DAT ");
            stb.append("                      WHERE ");
            stb.append("                             YEAR     = '" + param._year + "' ");
            if (SEMEALL.equals(param._semester)) {
                stb.append("                     AND SEMESTER = '" + param._semeFlg + "' ");
            } else {
                stb.append("                     AND SEMESTER = '" + param._semester + "' ");
            }
            stb.append("                         AND GRADE    = ? ");
            stb.append("                         AND HR_CLASS = ? ");
            stb.append("                      ) ");

            return stb.toString();
        }

        /**
         * 科目クラスの取得（教科名・科目名・単位・授業時数）
         * @param rs 生徒別科目別明細
         * @return 科目のクラス
         */
        private SubClass getSubclass(final Map row, final Map<String, SubClass> subclasses) {
            final Integer zero = new Integer(0);
            final String subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");
            //科目クラスのインスタンスを更新して返す
            if (!subclasses.containsKey(subclasscd)) {
                //科目クラスのインスタンスを作成して返す
                final String classabbv = KnjDbUtils.getString(row, "CLASSNAME");
                final String subclassabbv = KnjDbUtils.getString(row, "SUBCLASSABBV");
                final String subclassname = KnjDbUtils.getString(row, "SUBCLASSNAME");
                final String staffname = KnjDbUtils.getString(row, "STAFFNAME");
                final boolean electdiv = "1".equals(KnjDbUtils.getString(row, "ELECTDIV"));
                final List<String> otherstafflist = new ArrayList<String>();
                final String standardDeviation = KnjDbUtils.getString(row, "STDDEV");
                subclasses.put(subclasscd, new SubClass(subclasscd, classabbv, subclassabbv, subclassname, electdiv, staffname, otherstafflist, standardDeviation));
            }
            final SubClass subclass = subclasses.get(subclasscd);
            if (KnjDbUtils.getString(row, "CREDITS") != null) {
                final int credit = KnjDbUtils.getInt(row, "CREDITS", zero).intValue();
                if (0 < credit) {
                    subclass._creditSet.add(credit);
                }
            }
            if (KnjDbUtils.getString(row, "PERFECT") != null) {
                final int perfect = KnjDbUtils.getInt(row, "PERFECT", zero).intValue();
                if (0 < perfect) {
                    subclass._perfectSet.add(perfect);
                }
            }
            return subclass;
        }

        /**
         * Studentクラスの成績から科目別学級平均および合計を算出し、SubClassクラスのフィールドにセットする。
         */
        public static void setSubclassAverage(final Param param, final List<Student> students) {
            final Map<SubClass, List<ScoreDetail>> subClassScoreDetailList = new HashMap<SubClass, List<ScoreDetail>>();

            for (final Student student : students) {
                for (final ScoreDetail detail : student.getCountScoreDetails(param)) {
                    final boolean useD001 = param._d065Name1List.contains(detail._subclass.keySubclasscd());
                    if (useD001) {
                        continue;
                    }
                    if (detail._isNarakenKekkaOver) {
                        continue;
                    }
                    final String scorevalue = detail._score;
                    if (StringUtils.isNumeric(scorevalue) || ScoreDetail.isFailCount(param, detail)) {
                        getMappedList(subClassScoreDetailList, detail._subclass).add(detail);
                    }
                }
            }

            for (final SubClass subClass : subClassScoreDetailList.keySet()) {
                final List<ScoreDetail> detailList = subClassScoreDetailList.get(subClass);

                final List<ScoreDetail> failList = new ArrayList<ScoreDetail>();
                final List<Integer> scoreList = new ArrayList<Integer>();
                for (final ScoreDetail detail : detailList) {
                    final String scorevalue = detail._score;
                    if (ScoreDetail.isFailCount(param, detail)) {
                        failList.add(detail);
                    }
                    if (StringUtils.isNumeric(scorevalue)) {
                        scoreList.add(Integer.parseInt(scorevalue));
                    }
                }
                if (scoreList.size() > 0) {
                    subClass._scoretotal = sum(scoreList);
                    subClass._scoreCount = String.valueOf(scoreList.size());
                    subClass._scoreHrAverage = avg(scoreList);
                    subClass._scoreMax = max(scoreList);
                    subClass._scoreMin = min(scoreList);
                }
                if (failList.size() > 0) {
                    subClass._scoreFailCnt = String.valueOf(failList.size());
                }
            }
        }

//        private void setHrTotal(final Param param, final List<Student> students) {
//        	// 学級平均の算出
//            int totalT = 0;
//            int countT = 0;
//            double totalA = 0;
//            int countA = 0;
//            for (final Student student : students) {
//                final Total t = student._total;
//                if (null != t) {
//                    if (0 < t._count) {
//                        totalT += t._total;
//                        countT++;
//                    }
//                    if (0< t._count) {
//                        totalA += t._total;
//                        countA += t._count;
//                    }
//                }
//            }
//            if (0 < countT) {
//                final double avg = (float) totalT / (float) countT;
//                _avgHrTotalScore = new BigDecimal(avg);
//            }
//            if (0 < countA) {
//                final double avg = (float) totalA / (float) countA;
//                _avgHrAverageScore = new BigDecimal(avg);
//            }
//        }

        // 出席率
        public static BigDecimal shussekiRitsu(final List<Student> students) {
            final List<Integer> mlessons = new ArrayList<Integer>();
            final List<Integer> presents = new ArrayList<Integer>();
            for (final Student student : students) {
                mlessons.add(student._attendInfo._mLesson);
                presents.add(student._attendInfo._present);
            }
            return percentage(sum(presents), sum(mlessons));
        }

        // 欠席率
        public static BigDecimal kessekiRitsu(final List<Student> students) {
            final List<Integer> mlessons = new ArrayList<Integer>();
            final List<Integer> sicks = new ArrayList<Integer>();
            for (final Student student : students) {
                mlessons.add(student._attendInfo._mLesson);
                sicks.add(student._attendInfo._sick);
            }
            return percentage(sum(sicks), sum(mlessons));
        }

        private static BigDecimal percentage(final String n1, final String n2) {
            if (!NumberUtils.isNumber(n1) || !NumberUtils.isNumber(n2)) {
                return null;
            }
            final BigDecimal n2b = new BigDecimal(n2);
            if (n2b.doubleValue() == 0.0) {
                return null;
            }
            return new BigDecimal(n1).multiply(new BigDecimal(100)).divide(n2b, 1, BigDecimal.ROUND_HALF_UP);
        }

        // 履修単位数
        public static String maxCompCredits(final Param param, final List<Student> students) {
            final List<Integer> nums = new ArrayList<Integer>();
            for (final Student student : students) {
                final String totalCompCredit = student.getTotalCompCredit(param);
                if (NumberUtils.isDigits(totalCompCredit)) {
                    nums.add(Integer.parseInt(totalCompCredit));
                }
            }
            return append(max(nums), "単位");
        }

        // 授業日数
        public static String maxMLesson(final List<Student> students) {
            final List<Integer> nums = new ArrayList<Integer>();
            for (final Student student : students) {
                nums.add(student._attendInfo._mLesson);
            }
            return append(max(nums), "日");
        }

        // 出停忌引
        public static String suspendMourningTotal(final Param param, final List<Student> students) {
            final List<String> nums = new ArrayList<String>();
            for (final Student student : students) {
                nums.add(zeroToNull(param, student._attendInfo._suspend + student._attendInfo._mourning));
            }
            return stringSum(nums);
        }

        // 欠席
        public static String kessekiTotal(final Param param, final List<Student> students) {
            final List<String> nums = new ArrayList<String>();
            for (final Student student : students) {
                nums.add(zeroToNull(param, student._attendInfo._sick));
            }
            return stringSum(nums);
        }

        // 遅刻
        public static String lateTotal(final Param param, final List<Student> students) {
            final List<String> nums = new ArrayList<String>();
            for (final Student student : students) {
                nums.add(zeroToNull(param, student._attendInfo._late));
            }
            return stringSum(nums);
        }

        // 早退
        public static String earlyTotal(final Param param, final List<Student> students) {
            final List<String> nums = new ArrayList<String>();
            for (final Student student : students) {
                nums.add(zeroToNull(param, student._attendInfo._early));
            }
            return stringSum(nums);
        }

        private static List<Integer> totalScores(final List<Student> students) {
            final List<Integer> scores = new ArrayList<Integer>();
            for (final Student student : students) {
                final String s = student._scoreSum999999;
                if (NumberUtils.isDigits(s)) {
                    scores.add(Integer.parseInt(s));
                }
            }
            return scores;
        }

        // 総合点の最高点
        public static String totalMax(final List<Student> students) {
            return max(totalScores(students));
        }

        // 総合点の最低点
        public static String totalMin(final List<Student> students) {
            return min(totalScores(students));
        }


        // 欠点の合計
        public static String failTotal(final Param param, final List<Student> students) {
            int countFail = 0;
            for (final Student student : students) {
                if (0 < student.countFail(param)) {
                    countFail += student.countFail(param);
                }
            }
            if (0 == countFail) {
                return "";
            }
            return String.valueOf(countFail);
        }

        /**
         * 科目の学年平均得点
         * @param db2
         * @throws SQLException
         */
        private void setSubclassGradeAverage(final DB2UDB db2, final Param param) {
            final String sql = getRecordAverageSdivDatSql(param, param._avgDiv);
            if (null != sql) {
                if (param._isOutputDebugQuery) {
                    log.info(" gradeAverage sql = " + sql);
                } else {
                    log.debug(" gradeAverage sql = " + sql);
                }
                for (final Map row : KnjDbUtils.query(db2, sql)) {
                    final String subclassCd = KnjDbUtils.getString(row, "SUBCLASSCD");
                    final SubClass subclass = _subclasses.get(subclassCd);
                    final BigDecimal avg = KnjDbUtils.getBigDecimal(row, "AVG", null);
                    if (subclass == null || avg == null) {
                        continue;
                    }
                    if (param._isOutputDebugQuery) {
                        log.info("subclass " + subclass._subclasscd + ":" + subclass._subclassabbv + " , gradeAvg => " + avg);
                    }
                    subclass._recordAverageSdivDatAvg = sishaGonyu(avg);
                }
            }

            // コース毎改頁なら「クラス平均点」はページの生徒から算出した値ではなくRECORD_AVERAGE_SDIV_DATから表示する
            if (_isPrintHrCoursePage) {
                final String sql2 = getRecordAverageSdivDatSql(param, "2");
                if (null != sql2) {
                    if (param._isOutputDebugQuery) {
                        log.info(" hrAverage sql = " + sql2);
                    } else {
                        log.debug(" hrAverage sql = " + sql2);
                    }
                    for (final Map row : KnjDbUtils.query(db2, sql2)) {
                        final String subclassCd = KnjDbUtils.getString(row, "SUBCLASSCD");
                        final SubClass subclass = _subclasses.get(subclassCd);
                        final BigDecimal avg = KnjDbUtils.getBigDecimal(row, "AVG", null);
                        if (subclass == null || avg == null) {
                            continue;
                        }
                        if (param._isOutputDebugQuery) {
                            log.info("subclass " + subclass._subclasscd + ":" + subclass._subclassabbv + " , hrAvg => " + avg + " (calc = " + subclass._scoreHrAverage + ")");
                        }
                        subclass._scoreHrAverage = sishaGonyu(avg);
                    }
                }
            }
        }

        private String getRecordAverageSdivDatSql(final Param param, final String avgDiv) {
            final String testkindcd = param._testKindCd.substring(0, 2);
            final String testitemcd = param._testKindCd.substring(2, 4);
            final String scoreDiv = param._testKindCd.substring(4);

            final Collection<String> cds = new TreeSet<String>();
            final StringBuffer stb = new StringBuffer();
            // param._avgDivのRECORD_AVERAGE_SDIV_DAT
            stb.append("SELECT ");
            stb.append("    T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("    T1.AVG ");
            stb.append("FROM ");
            stb.append("    RECORD_AVERAGE_SDIV_DAT T1 ");
            stb.append("WHERE ");
            stb.append("    T1.YEAR = '" + param._year + "'");
            stb.append("    AND T1.SEMESTER = '" + param._semester + "' ");
            stb.append("    AND T1.TESTKINDCD = '" + testkindcd + "'");
            stb.append("    AND T1.TESTITEMCD = '" + testitemcd + "'");
            stb.append("    AND T1.SCORE_DIV = '" + scoreDiv + "'");
            stb.append("    AND T1.AVG_DIV = '" + avgDiv + "' ");
            stb.append("    AND T1.GRADE = '" + param._grade + "' ");
            stb.append("    AND T1.SUBCLASSCD <> '" + param.SUBCLASSCD999999 + "' ");
            if ("2".equals(avgDiv)) {
                for (final Student student : _students) {
                    if (null != student._hrClass) {
                        cds.add(student._hrClass);
                    }
                }
                if (cds.size() == 0) {
                    log.warn("warning:PARAMETER(hrclasses) is NULL .");
                    return null;
                }
                stb.append("    AND T1.HR_CLASS IN ('" + mkString(cds, "','")  + "') ");
                stb.append("    ORDER BY HR_CLASS ");

            } else if ("3".equals(avgDiv)) {
                for (final Student student : _students) {
                    if (null != student._course) {
                        cds.add(student._course);
                    }
                }
                if (cds.size() == 0) {
                    log.warn("warning:PARAMETER(courses) is NULL .");
                    return null;
                }
                stb.append("    AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE IN ('" + mkString(cds, "','") + "') ");
                stb.append("    ORDER BY T1.COURSECD || T1.MAJORCD || T1.COURSECODE ");
            } else if ("4".equals(avgDiv)) {
                for (final Student student : _students) {
                    if (null != student._course && student._course.length() >= 4) {
                        cds.add(student._course.substring(0, 4));
                    }
                }
                if (cds.size() == 0) {
                    log.warn("warning:PARAMETER(majorcds) is NULL .");
                    return null;
                }
                stb.append("    AND T1.COURSECD || T1.MAJORCD IN ('" + mkString(cds, "','") + "') ");
                stb.append("    ORDER BY T1.COURSECD || T1.MAJORCD ");
            }
            return stb.toString();
        }

//        /**
//         * 順位の算出
//         */
//        private List<Total> createRanking(final Param param, final List<Student> students) {
//            final List<Total> list = new LinkedList<Total>();
//            for (final Student student : students) {
//                final Total total = student._total;
//                if (0 < total._count) {
//                    list.add(total);
//                }
//            }
//
//            Collections.sort(list);
//            return list;
//        }

//        private int rank(final Student student) {
//            final Total total = student._total;
//            if (0 >= total._count) {
//                return -1;
//            }
//            return 1 + _ranking.indexOf(total);
//        }

//        private static int[] setMaxMin(
//                int maxInt,
//                int minInt,
//                int targetInt
//        ) {
//            if (0 < targetInt) {
//            	maxInt = Math.max(maxInt, targetInt);
//                if (0 == minInt) {
//                    minInt = targetInt;
//                } else {
//                	minInt = Math.min(minInt, targetInt);
//                }
//            }
//            return new int[]{maxInt, minInt};
//        }

        public int compareTo(final StudentGroup that) {
            return _cd.compareTo(that._cd);
        }

        public String toString() {
            return "StudentGroup(" + _cd + ")";
        }
        public void bkupInfo(final Param param, final List<ReportInfo> bkupReportInfo) {
            String groupFlg = null;
            if (OUTPUT_RANK1.equals(param._outputRank)) {
                groupFlg = StudentGroup.AVG_FLG_HR;
            } else if (OUTPUT_RANK2.equals(param._outputRank)) {
                groupFlg = StudentGroup.AVG_FLG_GRADE;
            } else if (OUTPUT_RANK3.equals(param._outputRank)) {
                groupFlg = StudentGroup.AVG_FLG_COURSE;
            } else if (OUTPUT_RANK4.equals(param._outputRank)) {
                groupFlg = StudentGroup.AVG_FLG_MAJOR;
            }
            final String hrclass = _cd;
            ReportDetailInfo adddetailwk = new ReportDetailInfo(getRecordRankSdivAverage(groupFlg, param.SUBCLASSCD999999, "AVG"), getRecordRankSdivAverage(groupFlg, param.SUBCLASSCD999999, "TOTAL"), _subclasses);
            ReportInfo addwk = new ReportInfo(hrclass, _hrName, _courses, adddetailwk);
            bkupReportInfo.add(addwk);
        }
    }

    private static class ReportInfo {
        final String _hrclass;
        final String _hrName;
        final List<Course> _courses;
        final ReportDetailInfo _reportlist;
        ReportInfo(final String hrclass, final String hrName, final List<Course> courses, final ReportDetailInfo reportlist) {
            _hrclass = hrclass;
            _hrName = hrName;
            _courses = courses;
            _reportlist = reportlist;
        }
    }

    private static class ReportDetailInfo{
        final Map<String, SubClass> _subclasses;
        final String _gavg;
        final String _gtotal;
        ReportDetailInfo(final String gavg, final String gtotal, final Map<String, SubClass> subclasses) {
            _gavg = gavg;
            _gtotal = gtotal;
            _subclasses = new TreeMap<String, SubClass>(subclasses);
        }
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * <<生徒のクラス>>。
     */
    private static class Student implements Comparable<Student> {
        int _gnum;  // 行番号
        final String _schregno;  // 学籍番号
        final String _hrClass;
        final String _hrName;
        final String _course;
        final String _major;
        final String _ryou;
        final String _kaigai;
        final String _suisen;
        final String _ib;
        final String _aHouShiki;
        final StudentGroup _studentGroup;
        final String _sex;
        final String _coursecodename;
        final String _schoolRefusal;
        private String _attendno;
        private String _name;
        private String _nameEng;
        private String _transInfo;
        private AttendInfo _attendInfo = new AttendInfo(0, 0, 0, 0, 0, 0, 0, 0, 0);
        private Map<String, String> _subclasscdChairGroupCdMap = new HashMap<String, String>();
        private String _scoreSum999999;
        private String _scoreAvg999999;
        private String _scoreSum333333;
        private String _scoreAvg333333;
        private String _scoreSum555555;
        private String _scoreAvg555555;
        private int _classRank;
        private int _rank;
        private SchScoreCnt _schScoreCnt;
        private final Map<String, ScoreDetail> _scoreDetails = new TreeMap<String, ScoreDetail>();
//        private Total _total;
//        private int _compCredit;  // 今年度の履修単位数
//        private int _getCredit;  // 今年度の修得単位数
//        private int _qualifiedCredits;  // 今年度の認定単位数
//        private int _previousCredits;  // 前年度までの修得単位数
//        private int _previousMirisyu;  // 前年度までの未履修（必須科目）数
//        private boolean _isGradePoor;  // 成績不振者
//        private boolean _isAttendPerfect;  // 皆勤者
//        private boolean _isKekkaOver;  // 欠課時数超過が1科目でもある者
        private Map<String, Integer> _spGroupAbsentMinutes = new HashMap<String, Integer>(); // 特活グループコードごとの欠課時分
        private String _attendSemesRemarkDatRemark1;
        private String _scholarshipName;

        Student(
                final String schregno,
                final String hrClass,
                final String hrName,
                final String course,
                final String major,
                final String sex,
                final String coursecodename,
                final StudentGroup studentGroup,
                final String ryou,
                final String kaigai,
                final String suisen,
                final String ib,
                final String aHouShiki,
                final String schoolRefusal
        ) {
            _schregno = schregno;
            _hrClass = hrClass;
            _hrName = hrName;
            _course = course;
            _major = major;
            _sex = sex;
            _coursecodename = coursecodename;
            _studentGroup = studentGroup;
            _ryou = ryou;
            _kaigai = kaigai;
            _suisen = suisen;
            _ib = ib;
            _aHouShiki = aHouShiki;
            _schoolRefusal = schoolRefusal;
        }

        /**
         * 出席番号順にソートします。
         * {@inheritDoc}
         */
        public int compareTo(final Student that) {
            int rtn;
            rtn = _studentGroup.compareTo(that._studentGroup);
            if (0 != rtn) return rtn;
            rtn = _hrClass.compareTo(that._hrClass);
            if (0 != rtn) return rtn;
            rtn = _attendno.compareTo(that._attendno);
            return rtn;
        }

        public String toString() {
            return _attendno + ":" + _name;
        }

//        public int getTotalScore(final Param param) {
//            int total = 0;
//            int count = 0;
//
//            for (final ScoreDetail detail : _scoreDetails.values()) {
//                if (!param._isPrintSakiKamoku && null != detail._subclass && param.getSubclassMst(detail._subclass.keySubclasscd())._isSaki) {
//                    continue;
//                } else if (param._isNoPrintMoto && null != detail._subclass && param.getSubclassMst(detail._subclass.keySubclasscd())._isMoto) {
//                    continue;
//                }
//
//                final String scoreValue = detail._score;
//                if (ScoreDetail.isAddTotal(detail._replacemoto, param)) {
//                	if (null != scoreValue && StringUtils.isNumeric(scoreValue)) {
//                		total += Integer.parseInt(scoreValue);
//                		count++;
//                	}
//                }
//            }
//
//            _total = total;
//            _count = count;
//            if (0 < count) {
//                final double avg = (float) total / (float) count;
//                _avgBigDecimal = new BigDecimal(avg);
//            } else {
//                _avgBigDecimal = null;
//            }
//        }

        public List<ScoreDetail> getCountScoreDetails(final Param param) {
            final List<ScoreDetail> rtn = new ArrayList<ScoreDetail>(_scoreDetails.values());
            for (final Iterator<ScoreDetail> it = rtn.iterator(); it.hasNext();) {
                final ScoreDetail detail = it.next();
                if (!param._isPrintSakiKamoku && null != detail._subclass && param.getSubclassMst(detail._subclass.keySubclasscd()).isSaki(param, Collections.singleton(_course))) {
                    it.remove();
                } else if (param._isNoPrintMoto && null != detail._subclass && param.getSubclassMst(detail._subclass.keySubclasscd()).isMoto(param, Collections.singleton(_course))) {
                    it.remove();
                }
            }
            return rtn;
        }


        // 履修単位数
        public String getTotalCompCredit(final Param param) {
            List<Integer> nums = new ArrayList<Integer>();

            for (final ScoreDetail detail : getCountScoreDetails(param)) {
                final Integer v = detail.getCompCredit();
                if (null != v) {
                    nums.add(v);
                }
            }
            return sum(nums);
        }

        // 修得単位数
        public String getTotalGetCredit(final Param param) {
            List<Integer> nums = new ArrayList<Integer>();

            for (final ScoreDetail detail : getCountScoreDetails(param)) {
                final Integer v = detail.getGetCredit();
                if (null != v) {
                    nums.add(v);
                }
            }
            return sum(nums);
        }

        // 特活欠課時数
        public int getTokubetukatudouKetujisu(final Param param) {
            int specialAbsent = 0;
            for (final Integer groupAbsentMinutes : _spGroupAbsentMinutes.values()) {
                specialAbsent += AttendInfo.getSpecialAttendExe(param, groupAbsentMinutes.intValue());
            }
            return specialAbsent;
        }

        /**
         * @return 欠課超過が1科目でもあるなら true を戻します。
         */
        public boolean isKekkaOver(final Param param) {
            return null != getKekkaOverKamokuCount(param);
        }

        public String getKekkaOverKamokuCount(final Param param) {
            int count = 0;
            for (final ScoreDetail detail : getCountScoreDetails(param)) {
                if (detail._isKekkaOver || detail._isNarakenKekkaOver) {
                    count += 1;
                }
            }
            return count == 0 ? null : String.valueOf(count);
        }

        public String getKettenKamokuCount(final Param param) {
            int count = 0;
            for (final ScoreDetail detail : getCountScoreDetails(param)) {
                if (ScoreDetail.isFailCount(param, detail)) {
                    count += 1;
                }
            }
            return count == 0 ? null : String.valueOf(count);
        }

        public String getKettenTanni(final Param param) {
            int credit = 0;
            for (final ScoreDetail detail : getCountScoreDetails(param)) {
                if (ScoreDetail.isFailCount(param, detail) && null != detail._credits) {
                    credit += detail._credits.intValue();
                }
            }
            return credit == 0 ? null : String.valueOf(credit);
        }

        public String getRemark(final Param param) {
            String remark = "";
            if (param._outputBiko && null != _attendSemesRemarkDatRemark1) {
                remark += _attendSemesRemarkDatRemark1 + " ";
            }
            if (param._knjd615vNotPrintTransInfo) {
                if (param._isOutputDebug) {
                    if (_transInfo.length() > 0) {
                        log.info(" not print transInfo " + _schregno + " = " + _transInfo);
                    }
                }
            } else {
                remark += _transInfo.toString();  // 備考
            }
            return remark;
        }

        public String getPrintAttendno() {
            return getPrintAttendno(-1);
        }

        /**
         *
         * @param keta 値が1以上の場合前にスペースを追加する
         * @return
         */
        public String getPrintAttendno(final int keta) {
            String no = defstr(NumberUtils.isDigits(_attendno) ? String.valueOf(Integer.parseInt(_attendno)) : _attendno);
            if (1 <= keta) {
                no = StringUtils.repeat(" ", keta - KNJ_EditEdit.getMS932ByteLength(no)) + no;
            }
            return no;
        }

        public int countFail(final Param param) {

            final List<ScoreDetail> list = new ArrayList<ScoreDetail>();

            for (final ScoreDetail detail : getCountScoreDetails(param)) {
                if (ScoreDetail.isAddTotal(detail._replacemoto, param)) {
                    if (ScoreDetail.isFailCount(param, detail)) {
                        list.add(detail);
                    }
                }
            }
            return list.size();
        }

        private int getRankForCompare(final Param param) {
            if (!param._isPrintGroupHr) {
                return _rank;
            }
            return _classRank;
        }

        private static class RankComparator implements Comparator<Student> {
            final Param _param;
            public RankComparator(final Param param) {
                _param = param;
            }
            public int compare(final Student s1, final Student s2) {
                int cmp = 0;
                if (null == s1 && null == s2) {
                    return 0;
                } else if (null == s1) {
                    cmp = 1;
                } else if (null == s2) {
                    cmp = -1;
                }
                if (0 != cmp) {
                    return cmp;
                }
                final int rank1 = s1.getRankForCompare(_param);
                final int rank2 = s2.getRankForCompare(_param);
                if (rank1 != rank2) {
                    if (rank1 == 0 && rank2 == 0) {
                        cmp = 0;
                    } else if (rank1 == 0) {
                        cmp = 1;
                    } else if (rank2 == 0) {
                        cmp = -1;
                    } else {
                        cmp = rank1 - rank2; // _classRankの昇順
                    }
                }
                if (0 != cmp) {
                    return cmp;
                }
                cmp = s1.compareTo(s2);
                return cmp;
            }

        }
    }

    //--- 内部クラス -------------------------------------------------------
    private static class AttendInfo {
        final int _lesson;
        final int _mLesson;
        final int _suspend;
        final int _mourning;
        final int _sick;
        final int _present;
        final int _late;
        final int _early;
        final int _transDays;

        AttendInfo(
                final int lesson,
                final int mLesson,
                final int suspend,
                final int mourning,
                final int sick,
                final int present,
                final int late,
                final int early,
                final int transDays
        ) {
            _lesson = lesson;
            _mLesson = mLesson;
            _suspend = suspend;
            _mourning = mourning;
            _sick = sick;
            _present = present;
            _late = late;
            _early = early;
            _transDays = transDays;
        }

        /**
         * @return 皆勤（欠席、遅刻、早退が０）なら true を戻す。
         */
        public boolean isAttendPerfect() {
            if (_sick == 0 && _late == 0 && _early == 0) { return true; }
            return false;
        }

        public String toString() {
            return "Attendance(" + _lesson + ", " + _mLesson + ", " + _suspend + ", " + _mourning + ", " + _sick + ", " + _present + ", " + _late + ", " + _early + ")";
        }

        /**
         * 欠課時分を欠課時数に換算した値を得る
         * @param kekka 欠課時分
         * @return 欠課時分を欠課時数に換算した値
         */
        private static int getSpecialAttendExe(final Param param, final int kekka) {
            final int jituJifun = (param._knjSchoolMst._jituJifunSpecial == null) ? 50 : Integer.parseInt(param._knjSchoolMst._jituJifunSpecial);
            final BigDecimal bigKekka = new BigDecimal(kekka);
            final BigDecimal bigJitu = new BigDecimal(jituJifun);
            final BigDecimal bigD = bigKekka.divide(bigJitu, 1, BigDecimal.ROUND_DOWN);
            final String retSt = bigD.toString();
            final int retIndex = retSt.indexOf(".");
            int seisu = 0;
            if (retIndex > 0) {
                seisu = Integer.parseInt(retSt.substring(0, retIndex));
                final int hasu = Integer.parseInt(retSt.substring(retIndex + 1, retIndex + 2));
                seisu = hasu < 5 ? seisu : seisu + 1;
            } else {
                seisu = Integer.parseInt(retSt);
            }
            return seisu;
        }
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * <<科目のクラスです>>。
     */
    private static class SubClass implements Comparable<SubClass> {
        final String _classabbv;
        final String _classcd;
        final String _subclasscd;
        final String _subclassabbv;
        final String _subclassname;
        final boolean _electdiv; // 選択科目
        final String _staffname;  // 科目担当者名
        final String _standardDeviation; // 標準偏差
        final TreeSet<Integer> _creditSet = new TreeSet<Integer>();  // 単位
        final TreeSet<Integer> _jisuSet = new TreeSet<Integer>();  // 授業時数
        private String _scoreHrAverage;  // 学級平均
        private String _recordAverageSdivDatAvg;  // 学年orコース平均
        String _scoretotal = "";  // 学級合計
        String _scoreCount = "";  // 学級人数
        String _scoreMax = "";  // 最高点
        String _scoreMin = "";  // 最低点
        String _scoreFailCnt = "";  // 欠点者数
        final TreeSet<Integer> _perfectSet = new TreeSet<Integer>();  // 満点
        final List<String> _otherstafflist; //他の先生(MAX2人)
        final Map<String, Map<String, String>> _chairInfos = new TreeMap<String, Map<String, String>>(); // 講座ごとの設定
        final Map<String, String> _chairGroupAvgs = new TreeMap<String, String>(); // <講座グループコード, 平均点>

        SubClass(
                final String subclasscd,
                final String classabbv,
                final String subclassabbv,
                final String subclassname,
                final boolean electdiv,
                final String staffname,
                final List<String> otherstafflist,
                final String standardDeviation
        ) {
            _classabbv = classabbv;
            _classcd = subclasscd.substring(0, 2);
            _subclasscd = subclasscd;
            _subclassabbv = subclassabbv;
            _subclassname = subclassname;
            _electdiv = electdiv;
            _staffname = staffname;
            _otherstafflist = otherstafflist;
            _standardDeviation = standardDeviation;
        }

        public boolean equals(final Object obj) {
            if (!(obj instanceof SubClass)) return false;
            final SubClass that = (SubClass) obj;
            return _subclasscd.equals(that._subclasscd);
        }

        @Override
        public int compareTo(final SubClass subclass) {
            return keySubclasscd().compareTo(subclass.keySubclasscd());
        }

        public int hashCode() {
            return ("Subclass(" + _subclasscd + ")").hashCode() * 17;
        }

        public String toString() {
            return "["+_classabbv + " , " +_subclasscd + " , " +_subclassabbv + " , " +_electdiv + " , " +_creditSet + " , " +_jisuSet +"]";
        }

        public String keySubclasscd() {
            return _subclasscd.substring(0);
        }

        public String getPrintCredit(final Param param) {
            final StringBuffer stb = new StringBuffer();
            if (0 < _creditSet.size()) {
                if (_creditSet.size() == 1) {
                    stb.append(_creditSet.last());
                } else {
                    if (param._isPrintPerfect) {
                        stb.append(String.valueOf(_creditSet.first()) + Param.FROM_TO_MARK + String.valueOf(_creditSet.last()));
                    } else {
                        stb.append(String.valueOf(_creditSet.first()) + " " + Param.FROM_TO_MARK + " " + String.valueOf(_creditSet.last()));
                    }
                }
            }
            if (param._isPrintPerfect) {
                if (0 < _perfectSet.size()) {
                    if (_perfectSet.size() == 1) {
                        stb.append("(").append(_perfectSet.last()).append(")");
                    } else {
                        stb.append("(*)");
                    }
                }
            }
            return stb.toString();
        }

        public String getJisu() {
            if (_jisuSet.isEmpty()) {
                return "";
            }
            return _jisuSet.last().toString(); // MAX
        }

        private static class PrintOrderSorter implements Comparator<SubClass> {
            final Param _param;
            PrintOrderSorter(final Param param) {
                _param = param;
            }
            public int compare(final SubClass o1, final SubClass o2) {
                int cmp;
                final Integer ele1 = o1._electdiv ? 1 : 0;
                final Integer ele2 = o2._electdiv ? 1 : 0;
                cmp = ele1.compareTo(ele2);
                if (0 != cmp) {
                    return cmp;
                }
                cmp = o1._subclasscd.compareTo(o2._subclasscd);
                return cmp;
            }
        }
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * <<生徒別科目別データのクラスです>>。
     */
    private static class ScoreDetail {
        final SubClass _subclass;
        final Student _student;
        final String _chaircd;
        Double _absent;
        Integer _jisu;
        final String _score;
        final String _recordScoreDatScore;
        final String _valueDi;
        final String _suppScore;
        final String _assessLevel;
        final String _karihyotei;
        final String _passScore;
        final String _provFlg;
        final Integer _replacemoto;
        final String _print_flg;
        final Integer _compCredit;
        final Integer _getCredit;
        BigDecimal _absenceHigh;
        final Integer _credits;
        boolean _isKekkaOver;
        boolean _isNarakenKekkaOver;
        final String _slump;
        final String _slumpMark;
        final String _slumpScore;
        final String _classDeviation;
        final String _gradeDeviation;
        final String _courseDeviation;
        final String _majorDeviation;

        ScoreDetail(
                final SubClass subclass,
                final Student student,
                final String chaircd,
                final String score,
                final String recordScoreDatScore,
                final String valueDi,
                final String suppScore,
                final String assessLevel,
                final String karihyotei,
                final String passScore,
                final String provFlg,
                final Integer replacemoto,
                final String print_flg,
                final String slump,
                final String slumpMark,
                final String slumpScore,
                final Integer compCredit,
                final Integer getCredit,
                final Integer credits,
                final String classDeviation,
                final String gradeDeviation,
                final String courseDeviation,
                final String majorDeviation
        ) {
            _subclass = subclass;
            _student = student;
            _chaircd = chaircd;
            _score = score;
            _recordScoreDatScore = recordScoreDatScore;
            _valueDi = valueDi;
            _suppScore = suppScore;
            _assessLevel = assessLevel;
            _karihyotei = karihyotei;
            _passScore = passScore;
            _provFlg = provFlg;
            _replacemoto = replacemoto;
            _print_flg = print_flg;
            _compCredit = compCredit;
            _getCredit = getCredit;
            _credits = credits;
            _slump = slump;
            _slumpScore = slumpScore;
            _slumpMark = slumpMark;
            _classDeviation = classDeviation;
            _gradeDeviation = gradeDeviation;
            _courseDeviation = courseDeviation;
            _majorDeviation = majorDeviation;
        }

        /**
         * 欠課時数超過ならTrueを戻します。
         * @param absent 欠課時数
         * @param absenceHigh 超過対象欠課時数（CREDIT_MST）
         * @return
         */
        private boolean judgeOver(final Double absent, final BigDecimal absenceHigh) {
            if (null == absent || null == absenceHigh) {
                return false;
            }
            if (0.1 > absent.floatValue() || 0.0 == absenceHigh.doubleValue()) {
                return false;
            }
            if (absenceHigh.doubleValue() < absent.doubleValue()) {
                return true;
            }
            return false;
        }

        /**
         * @return 合併元科目はnullを、以外はcompCreditを戻します。
         */
        public Integer getCompCredit() {
            return enableCredit() ? _compCredit : null;
        }

        /**
         * @return 合併元科目はnullを、以外はgetCreditを戻します。
         */
        public Integer getGetCredit() {
            return enableCredit() ? _getCredit : null;
        }

        /**
         * @return 合併元科目はFalseを、以外はTrueを戻します。
         */
        private boolean enableCredit() {
            if (null != _replacemoto && _replacemoto.intValue() >= 1) {
                return false;
            }
            return true;
        }

        private String getPrintDeviation(final Param param, final int scale) {
            String dev = null;
            if (OUTPUT_RANK1.equals(param._outputRank)) {
                //学級偏差値
                dev = _classDeviation;
            } else if (OUTPUT_RANK2.equals(param._outputRank)) {
                //学年偏差値
                dev = _gradeDeviation;
            } else if (OUTPUT_RANK3.equals(param._outputRank)) {
                //コース偏差値
                dev = _courseDeviation;
            } else if (OUTPUT_RANK4.equals(param._outputRank)) {
                //学科偏差値
                dev = _majorDeviation;
            }
            if (NumberUtils.isNumber(dev)) {
                dev = new BigDecimal(dev).setScale(scale, BigDecimal.ROUND_DOWN).toString();
            }
            return dev;
        }

        public static Boolean hoge(final Param param, final ScoreDetail detail) {
            if (null != param._testItem._sidouinput) {
                if (SIDOU_INPUT_INF_MARK.equals(param._testItem._sidouinputinf)) { // 記号
                    if (null != param._d054Namecd2Max && null != detail._slumpMark) {
                        if (param._d054Namecd2Max.equals(detail._slumpMark)) {
                            return Boolean.TRUE;
                        }
                        return Boolean.FALSE;
                    }
                } else if (SIDOU_INPUT_INF_SCORE.equals(param._testItem._sidouinputinf)) { // 得点
                    if (null != detail._slumpScore) {
                        if ("1".equals(detail._slumpScore)) {
                            return Boolean.TRUE;
                        }
                        return Boolean.FALSE;
                    }
                }
            }
            return null;
        }

        private static boolean is5dankai(final Param param) {
            return param._testKindCd != null && param._testKindCd.endsWith("09") || param._is5dankai;
        }

        private static boolean isFail(final Param param, final ScoreDetail detail) {
            final boolean useD001 = param._d065Name1List.contains(detail._subclass.keySubclasscd());
            if (useD001) {
                return false;
            }
            Boolean rtn = null;
            if (!is5dankai(param) && param._useSlumpSdivDatSlump) {
                Boolean slump = hoge(param, detail);
                if (null != slump) {
                    rtn = slump;
                }
            }
            if (null == rtn) {
                if (is5dankai(param)) {
                    rtn = new Boolean("*".equals(detail._valueDi) || "1".equals(detail._score));
                } else {
                    final boolean setPassScore = NumberUtils.isDigits(detail._passScore);
                    rtn = new Boolean("*".equals(detail._valueDi) || !setPassScore && "1".equals(detail._assessLevel) || setPassScore && NumberUtils.isDigits(detail._score) && Integer.parseInt(detail._score) < Integer.parseInt(detail._passScore));
                }
            }
            if (param._isOutputDebugKetten) {
                if (rtn.booleanValue()) {
                    log.info(" isFail " + rtn + " <- " + param._useSlumpSdivDatSlump + " / " + detail._valueDi + " / assessLevel = " + detail._assessLevel + " / score = " + detail._score + " / passScore = " + detail._passScore + "");
                }
            }
            return rtn.booleanValue();
        }

        /**
         * @param replacemoto
         * @param knjdObj
         * @return 成績総合計に組み入れる場合Trueを戻します。
         */
        private static boolean isAddTotal(final Integer replacemoto, final Param param) {
            if (param._isGakunenMatu && null != replacemoto && 0 < replacemoto.intValue()) { return false; }
            return true;
        }

        private static boolean isFailCount(final Param param, final ScoreDetail detail) {
            final boolean useD001 = param._d065Name1List.contains(detail._subclass.keySubclasscd());
            if (useD001) {
                return false;
            }
            if (!is5dankai(param) && param._useSlumpSdivDatSlump) {
                Boolean slump = hoge(param, detail);
                if (null != slump) {
                    return slump.booleanValue();
                }
            }
            if (is5dankai(param)) {
                return (!param._kettenshaSuuNiKessaShaWoFukumenai && "*".equals(detail._valueDi)) || "1".equals(detail._score);
            }
            final boolean setPassScore = NumberUtils.isDigits(detail._passScore);
            return (!param._kettenshaSuuNiKessaShaWoFukumenai && "*".equals(detail._valueDi)) || !setPassScore && "1".equals(detail._assessLevel) || setPassScore && NumberUtils.isDigits(detail._score) && Integer.parseInt(detail._score) < Integer.parseInt(detail._passScore);
        }

        public String toString() {
            return (_subclass + " , " + _absent + " , " + _jisu + " , " + _score + " , " + _replacemoto + " , " + _print_flg + " , " + _compCredit + " , " + _getCredit + " , " + _absenceHigh + " , " + _credits + " , " + _isKekkaOver);
        }
    }

//    //--- 内部クラス -------------------------------------------------------
//    /**
//     * <<生徒別総合成績データのクラスです>>。
//     */
//    private static class Total {
//        final int _total;  // 総合点
//        final int _count;  // 件数（成績）
//        final BigDecimal _avgBigDecimal;  // 平均点
//
//        /**
//         * 生徒別総合点・件数・履修単位数・修得単位数・特別活動欠課時数を算出します。
//         * @param student
//         */
//        Total(final Param param, final Student student) {
//
//            int total = 0;
//            int count = 0;
//
//            int compCredit = 0;
//            int getCredit = 0;
//
//            for (final ScoreDetail detail : student._scoreDetails.values()) {
//                if (!param._isPrintSakiKamoku && null != detail._subclass && param.getSubclassMst(detail._subclass.keySubclasscd())._isSaki) {
//                    continue;
//                } else if (param._isNoPrintMoto && null != detail._subclass && param.getSubclassMst(detail._subclass.keySubclasscd())._isMoto) {
//                    continue;
//                }
//
//                final String scoreValue = detail._score;
//                if (ScoreDetail.isAddTotal(detail._replacemoto, param)) {
//                	if (null != scoreValue && StringUtils.isNumeric(scoreValue)) {
//                		total += Integer.parseInt(scoreValue);
//                		count++;
//                	}
//                }
//
//                final Integer c = detail.getCompCredit();
//                if (null != c) {
//                    compCredit += c.intValue();
//                }
//
//                final Integer g = detail.getGetCredit();
//                if (null != g) {
//                    getCredit += g.intValue();
//                }
//            }
//
//            _total = total;
//            _count = count;
//            if (0 < count) {
//                final double avg = (float) total / (float) count;
//                _avgBigDecimal = new BigDecimal(avg);
//            } else {
//                _avgBigDecimal = null;
//            }
//            if (0 < compCredit) {
//                student._compCredit = compCredit;
//            }
//            if (0 < getCredit) {
//                student._getCredit = getCredit;
//            }
//        }
//
//        /**
//         * {@inheritDoc}
//         */
//        public int compareTo(final Total that) {
//            return that._avgBigDecimal.compareTo(this._avgBigDecimal);
//        }
//
//        /**
//         * {@inheritDoc}
//         */
//        public boolean equals(final Object o) {
//            if (!(o instanceof Total)) return false;
//            final Total that = (Total) o;
//            return that._avgBigDecimal.equals(this._avgBigDecimal);
//        }
//
//        /**
//         * {@inheritDoc}
//         */
//        public String toString() {
//            return _avgBigDecimal.toString();
//        }
//    }

    static class ClassAbbvFieldSet {
        final String _fieldNumer;
        final String _setAbbv;
        public ClassAbbvFieldSet(
                final String fieldNumer,
                final String setAbbv
        ) {
            _fieldNumer = fieldNumer;
            _setAbbv = setAbbv;
        }
    }

    static class Form {

        boolean _hasData = false;

        public void print(final DB2UDB db2, final IPdf ipdf, final Param param, final StudentGroup studentGroup) {
            if ("2".equals(param._outputPattern)) {
                final Form2 form2 = new Form2();
                form2.print2(ipdf, param, studentGroup);
                if (form2._hasData) {
                    _hasData = true;
                }
            } else {
                final Form1 form = new Form1(param);
                for (final List<Student> studentList : getStudentListList(getPrintStudentAll(param, studentGroup), param._formMaxLine)) {
                    form.print(db2, ipdf, studentGroup, studentList);
                }
                if (form._hasData) {
                    _hasData = true;
                }
            }
        }

        public boolean outputCsv(final DB2UDB db2, final List<List<String>> outputList, final Param param, final StudentGroup studentGroup) {
            boolean hasData = false;
            if ("2".equals(param._outputPattern)) {
                throw new IllegalArgumentException("not implemented pattern:" + param._outputPattern);
            } else {
                final List<Student> studentsAll = getPrintStudentAll(param, studentGroup);
                if (Form1.outputCsv(db2, outputList, param, studentGroup, studentsAll)) {
                    hasData = true;
                }
            }
            return hasData;
        }

        private static List<Student> getPrintStudentAll(final Param param, final StudentGroup studentGroup) {
            final List<Student> studentsAll = new ArrayList(studentGroup._students);
            if (OUTPUT_ORDER_2_SEISEKIJUN.equals(param._outputOrder)) {
                Collections.sort(studentsAll, new Student.RankComparator(param));
                for (int i = 0; i < studentsAll.size(); i++) {
                    final Student student = studentsAll.get(i);
                    student._gnum = i + 1;
                }
            }

            return studentsAll;
        }

        private static String getKekkaString(final Param param, final Double absent) {
            return null == absent ? "" : 0.0 == absent.doubleValue() && !"1".equals(param._printKekka0)  ? "" : param.getAbsentFmt().format(absent.floatValue());
        }

        private static List<String> newLine(final List<List<String>> listList) {
            final List<String> line = line();
            listList.add(line);
            return line;
        }

        private static List<String> line() {
            return line(0);
        }

        private static List<String> line(final int size) {
            final List<String> line = new ArrayList<String>();
            for (int i = 0; i < size; i++) {
                line.add(null);
            }
            return line;
        }

        private static int currentColumn(final List<List> lineList) {
            int max = 0;
            for (final List line : lineList) {
                max = Math.max(max, line.size());
            }
            return max;
        }

        private static <T> List<T> setSameSize(final List<T> list, final int max) {
            for (int i = list.size(); i < max; i++) {
                list.add(null);
            }
            return list;
        }

        private static List<List<Student>> getStudentListList(final List<Student> students, final int count) {
            final List<List<Student>> rtn = new ArrayList<List<Student>>();
            List<Student> current = null;
            int page = 0;
            for (final Student student : students) {
                final int cpage = student._gnum / count + (student._gnum % count != 0 ? 1 : 0);
                if (null == current || page < cpage) {
                    current = new ArrayList<Student>();
                    rtn.add(current);
                }
                current.add(student);
                page = cpage;
            }
            return rtn;
        }

        private static List<List<SubClass>> getSubClassListList(final Param param, final Collection<SubClass> subclasses, final int count) {
            final List<List<SubClass>> rtn = new ArrayList<List<SubClass>>();
            List<SubClass> current = null;
            for (final SubClass subClass : subclasses) {

                if (null == current || current.size() >= count) {
                    current = new ArrayList<SubClass>();
                    rtn.add(current);
                }
                current.add(subClass);
            }
            return rtn;
        }

        /**
         *
         * @param ipdf
         * @param field
         * @param line
         * @param data
         */
        private static void svfsetString1(final IPdf ipdf, final String field, final int line, final String pf, final int col, final String data) {
            ipdf.VrsOutn(field, line, data);
        }

        private static void printAttendInfo(final IPdf ipdf, final Param param, final AttendInfo attendInfo, final int line) {
            final String fieldLesson;
            final String fieldMLesson;
            final String fieldSusMour;
            final String fieldSick;
            final String fieldPresent;
            final String fieldLate;
            final String fieldEarly;
            final String fieldAbroad;
            if (PATTERN2.equals(param._outputPattern)) {
                fieldLesson = "LESSON";
                fieldSusMour = "SUSPEND";
                fieldAbroad = "ABROAD";
                fieldMLesson = "PRESENT";
                fieldPresent = "ATTEND";
                fieldSick = "ABSENCE";
                fieldLate = "LATE";
                fieldEarly = "LEAVE";
            } else if (PATTERN3.equals(param._outputPattern)) {
                fieldLesson = "APPOINT_DAY1";
                fieldSusMour = "SUSPEND_DAY1";
                fieldAbroad = "ABROAD1";
                fieldMLesson = "PRESENT1";
                fieldPresent = "ATTEND1";
                fieldSick = "ABSENCE1";
                fieldLate = "LATE1";
                fieldEarly = "EARLY1";
            } else if (PATTERN5.equals(param._outputPattern)) {
                fieldLesson = "APPOINT_DAY1";
                fieldSusMour = "SUSMOUR_DAY1";
                fieldAbroad = "ABROAD1";
                fieldMLesson = "PRESENT1";
                fieldPresent = "ATTEND1";
                fieldSick = "ABSENCE1";
                fieldLate = "LATE1";
                fieldEarly = "EARLY1";
            } else {
                fieldLesson = "LESSON";
                fieldSusMour = "SUSPEND1";
                fieldAbroad = "ABROAD1";
                fieldMLesson = "PRESENT1";
                fieldPresent = "ATTEND1";
                fieldSick = "ABSENCE1";
                fieldLate = "TOTAL_LATE1";
                fieldEarly = "LEAVE1";
            }
            ipdf.VrsOutn(fieldLesson,  line, zeroToNull(param, attendInfo._lesson));      // 授業日数
            ipdf.VrsOutn(fieldSusMour, line, zeroToNull(param, attendInfo._suspend + attendInfo._mourning));      // 出席停止
            ipdf.VrsOutn(fieldAbroad,  line, zeroToNull(param, attendInfo._transDays));        // 留学
            ipdf.VrsOutn(fieldMLesson, line, zeroToNull(param, attendInfo._mLesson));      // 出席すべき日数
            ipdf.VrsOutn(fieldPresent, line, attendInfo._lesson == 0 ? ("1".equals(param._printKekka0) ? "0" : "") : String.valueOf(attendInfo._present));      // 出席日数 (0は表示する)
            ipdf.VrsOutn(fieldSick,    line, zeroToNull(param, attendInfo._sick));       // 欠席日数
            ipdf.VrsOutn(fieldLate,    line, zeroToNull(param, attendInfo._late));      // 遅刻回数
            ipdf.VrsOutn(fieldEarly,   line, zeroToNull(param, attendInfo._early));        // 早退回数
        }

        private static boolean isAlpPdf(final IPdf ipdf) {
            return "1".equals(ipdf.getParameter("AlpPdf"));
        }

        private static void setForm(final IPdf ipdf, final String formname, final int n, final Param param) {
            param._currentform = formname;
            log.info(" form = " + param._currentform);
            ipdf.VrSetForm(param._currentform, n);

            if (ipdf instanceof SvfPdf) {
                final SvfPdf svfPdf = (SvfPdf) ipdf;
                modifyForm(svfPdf.getVrw32alp(), n, param);
            }
        }

        private static void modifyForm(final Vrw32alp svf, final int n, final Param param) {

            param._svfformModifyKeys = getModifyKeys(param);
            String key = mkString(param._svfformModifyKeys, "|");
            if (key.length() != 0) {
                key = param._currentform + key;
            }
            if (!StringUtils.isBlank(key) && !param._createdFiles.containsKey(key)) {
                final SvfForm svfForm = new SvfForm(new File(svf.getPath(param._currentform)));
                if (svfForm.readFile()) {
                    modifyForm(param._svfformModifyKeys, svfForm);
                    try {
                        File newFormFile = svfForm.writeTempFile();
                        if (null != newFormFile && newFormFile.exists()) {
                            param._createdFiles.put(key, newFormFile);
                        }
                    } catch (Exception e) {
                        log.info("exception!", e);
                    }
                }
            }
            if (param._createdFiles.containsKey(key)) {
                final File newFormFile = param._createdFiles.get(key);
                param._currentform = newFormFile.getName();
                svf.VrSetForm(param._currentform, n);
            }

            if (null != param._currentform && null == param._formFieldInfoMap.get(param._currentform)) {
                param._formFieldInfoMap.put(param._currentform, SvfField.getSvfFormFieldInfoMapGroupByName(svf));
                //debugFormInfo(param);
            }
        }

        static final String KEY_REMOVE_FIELD_STAFFNAME = "REMOVE_FIELD_STAFFNAME";
        static final String KEY_MIGITSUME_TANNINMEI = "KEY_MIGITSUME_TANNINMEI";
        static final String KEY_REMOVE_TEXT_TANNINMEI = "REMOVE_TEXT_TANNINMEI";
        static final String KEY_REMOVE_TEXT_INN = "REMOVE_TEXT_INN";
        static final String KEY_REMOVE_TEXT_MARU = "REMOVE_TEXT_MARU";
        static final String KEY_ADD_SUBTITLE = "ADD_SUBTITLE";
        static final String KEY_REMOVE_HR_HEIKIN = "REMOVE_HR_HEIKIN";
        static final String KEY_REMOVE_PARAM_RANK = "REMOVE_PARAM_RANK";
        static final String KEY_SCORE_FIELD_7KETA = "SCORE_FIELD_7KETA";
        static final String KEY_SUBJECT1_6KETA = "SUBJECT1_6KETA";
        private static List<String> getModifyKeys(final Param param) {
            List<String> keys = new ArrayList<String>();
            if (param._isNaraken) {
                keys.add(KEY_REMOVE_TEXT_MARU);
            } else if (param._isYamamura) {
                keys.add(KEY_REMOVE_TEXT_INN);
                keys.add(KEY_REMOVE_TEXT_MARU);
            } else if (param._isShimaneken) {
                keys.add(KEY_MIGITSUME_TANNINMEI);
                keys.add(KEY_REMOVE_TEXT_TANNINMEI);
                keys.add(KEY_REMOVE_TEXT_INN);
                keys.add(KEY_REMOVE_TEXT_MARU);
            } else if (!param._isPrintGroupHr) {
                // 担任名印字しない
                keys.add(KEY_REMOVE_TEXT_TANNINMEI);
                keys.add(KEY_REMOVE_TEXT_INN);
                keys.add(KEY_REMOVE_TEXT_MARU);
            }
            if (null != param._groupDiv) {
                keys.add(KEY_ADD_SUBTITLE);
                if (param._isPrintGroupHr) {
//                    	keys.add(KEY_REMOVE_PARAM_RANK);
                } else {
                    keys.add(KEY_REMOVE_HR_HEIKIN);
                }
            }
            if (param._isNichidaiNikou) {
                keys.add(KEY_SCORE_FIELD_7KETA);
            }
            if ("KNJD615P.frm".equals(param._currentform)) {
                keys.add(KEY_SUBJECT1_6KETA);
            }
            return keys;
        }

        private static void modifyForm(List<String> keys, final SvfForm svfForm) {
            if (keys.contains(KEY_REMOVE_FIELD_STAFFNAME)) {
                final List<String> fieldnames = new ArrayList<String>(svfForm.getFieldNameMap().keySet());
                Collections.sort(fieldnames);
                for (final String fieldname : fieldnames) {
                    if (0 == fieldname.indexOf("HR_TEACHER")) {
                        log.info(" remove field :" + fieldname);
                        final SvfForm.Field field = svfForm.getField(fieldname);
                        svfForm.removeField(field);
                    }
                }
            }
            if (keys.contains(KEY_REMOVE_TEXT_TANNINMEI)) {
                for (SvfForm.KoteiMoji koteiMoji : svfForm.getKoteiMojiListWithText("担任：")) {
                    log.info(" remove koteiMoji :" + koteiMoji);
                    svfForm.removeKoteiMoji(koteiMoji);
                }
            }
            if (keys.contains(KEY_REMOVE_TEXT_MARU)) {
                for (SvfForm.KoteiMoji koteiMoji : svfForm.getKoteiMojiListWithText("○")) {
                    log.info(" remove koteiMoji :" + koteiMoji);
                    svfForm.removeKoteiMoji(koteiMoji);
                }
            }
            if (keys.contains(KEY_REMOVE_TEXT_INN)) {
                for (SvfForm.KoteiMoji koteiMoji : svfForm.getKoteiMojiListWithText("印")) {
                    log.info(" remove koteiMoji :" + koteiMoji);
                    svfForm.removeKoteiMoji(koteiMoji);
                }
            }
            if (keys.contains(KEY_MIGITSUME_TANNINMEI)) {
                SvfForm.Field HR_TEACHER = null;
                for (final String fieldname : Arrays.asList("HR_TEACHER", "HR_TEACHER2")) {
                    final SvfForm.Field field = svfForm.getField(fieldname);
                    svfForm.removeField(field);
                    if ("HR_TEACHER".equals(fieldname)) {
                        HR_TEACHER = field;
                    }
                }
                if (null != HR_TEACHER) {
                    svfForm.addField(HR_TEACHER.setX(5226).setFieldLength(45).setHenshuShiki("\"\"担任:\"\" + HR_TEACHER").setPrintMethod(SvfForm.Field.PrintMethod.MIGITSUME));
                }
            }
            if (keys.contains(KEY_ADD_SUBTITLE)) {
                final boolean hasSubtitle = svfForm.getFieldNameMap().containsKey("SUBTITLE");
                if (!hasSubtitle) {
                    final SvfForm.Field title = svfForm.getField("TITLE");
                    if (null == title) {
                        log.warn(" no field TITLE");
                    } else {
                        svfForm.addField(title.copyTo("SUBTITLE").setY(title._position._y + 120).setHenshuShiki(""));
                    }
                }
            }
            if (keys.contains(KEY_REMOVE_HR_HEIKIN)) {
                for (final String text : Arrays.asList("学級平均", "組平均", "学級順位")) {
                    for (SvfForm.KoteiMoji koteiMoji : svfForm.getKoteiMojiListWithText(text)) {
                        log.info(" remove koteiMoji :" + koteiMoji);
                        svfForm.removeKoteiMoji(koteiMoji);
                    }
                }
                for (final String fieldname : Arrays.asList("AVE_CLASS", "CLASS_RANK1")) {
                    final SvfForm.Field field = svfForm.getField(fieldname);
                    if (null != field) {
                        svfForm.removeField(field);
                    }
                }
            }
            if (keys.contains(KEY_REMOVE_PARAM_RANK)) {
                for (final String fieldname : Arrays.asList("ITEM", "ITEM8", "AVE_SUBCLASS", "ITEM7", "RANK1")) {
                    final SvfForm.Field field = svfForm.getField(fieldname);
                    if (null != field) {
                        svfForm.removeField(field);
                    }
                }
            }
            if (keys.contains(KEY_SCORE_FIELD_7KETA)) {
                for (final SvfForm.Field field : svfForm.getElementList(SvfForm.Field.class)) {
                    if (field._fieldname.matches("SCORE[0-9]+_2")) {
                        svfForm.removeField(field);
                        svfForm.addField(field.setFieldLength(7).setCharPoint10(field._charPoint10 - 10).addY(5));
                    }
                }
            }
            if (keys.contains(KEY_SUBJECT1_6KETA)) {
                final SvfForm.Field field = svfForm.getField("SUBJECT1");
                if (null != field) {
                    svfForm.removeField(field);
                    svfForm.addField(field.addX(-50).setFieldLength(8).setEndX(field._endX + 50));
                }
            }
        }

        public static boolean hasField(final IPdf ipdf, final String field, final Param param) {
            if (!(ipdf instanceof SvfPdf)) {
                log.warn("not svfpdf.");
                return true;
            }
            final Map fieldMap = getMappedHashMap(param._formFieldInfoMap, param._currentform);
            final boolean hasField = null != fieldMap.get(field);
            if (param._isOutputDebug) {
                if (!hasField) {
                    log.warn(" form " + param._currentform + " has no field :" + field);
                }
            }
            return hasField;
        }

        public static int getFieldKeta(final IPdf ipdf, final String field, final Param param) {
            if (!(ipdf instanceof SvfPdf)) {
                log.warn("not svfpdf.");
                return -1;
            }
            final Map<String, SvfField> fieldMap = getMappedHashMap(param._formFieldInfoMap, param._currentform);
            int keta = -1;
            try {
                final SvfField f = (SvfField) fieldMap.get(field);
                if (null != f) {
                    keta = f._fieldLength;
                }
            } catch (Throwable t) {
                log.info("not found SvfField.class");
            }
            final String logVal = " form " + param._currentform + " " + field + " keta = " + keta;
            if (param._isOutputDebug && !param._formFieldInfoLog.contains(logVal)) {
                log.warn(logVal);
                param._formFieldInfoLog.add(logVal);
            }
            return keta;
        }

        public static SvfField getSvfField(final IPdf ipdf, final String field, final Param param) {
            if (!(ipdf instanceof SvfPdf)) {
                log.warn("not svfpdf.");
                return null;
            }
            final Map<String, SvfField> fieldMap = getMappedHashMap(param._formFieldInfoMap, param._currentform);
            final SvfField f = fieldMap.get(field);
            return f;
        }

        protected static String getFieldForData(final IPdf ipdf, final String[] fields, final String data, final Param param) {
            final int keta = KNJ_EditEdit.getMS932ByteLength(data);
            String fieldFound = null;
            int lastFieldLength = -1;
            searchField:
            for (int i = 0; i < fields.length; i++) {
                final String fieldname = fields[i];
                final SvfField svfField = getSvfField(ipdf, fieldname, param);
                if (null == svfField) {
                    param.logOnce("no field : " + fieldname);
                    continue searchField;
                }
                fieldFound = fieldname;
                lastFieldLength = svfField._fieldLength;
                if (keta <= svfField._fieldLength) {
                    return fieldname;
                }
            }
            if (-1 != lastFieldLength && lastFieldLength < keta) {
                log.info(" 桁不足 : " + fieldFound + " ( " + lastFieldLength + ") , data = " + data + "(" + keta + ")");
            }
            return fieldFound;
        }

        private static String getPrintScore2(final Param param, final ScoreDetail detail) {
            String printScore = "";
            if (param._isMeikei && "*".equals(detail._valueDi)) {
                printScore = defstr(detail._suppScore, detail._valueDi);
            } else if (param._isNichidaiNikou) {
                if ("*".equals(detail._valueDi)) {
                    if (StringUtils.isBlank(detail._suppScore)) {
                        printScore = "(欠)"; // 欠席
                    } else {
                        printScore = "(" + detail._suppScore + ")"; // 欠席見込点
                    }
                } else if ("**".equals(detail._valueDi)) {
                    if (StringUtils.isBlank(detail._suppScore)) {
                        printScore = "(公欠)"; // 公欠
                    } else {
                        printScore = "(" + detail._suppScore + ")"; // 公欠見込点
                    }
                } else {
                    printScore = defstr(detail._valueDi, detail._score);
                }
            } else if (null != detail._valueDi) {
                printScore = detail._valueDi;
            } else {
                final boolean useD001 = param._d065Name1List.contains(detail._subclass.keySubclasscd());
                if (useD001) {
                    printScore = param._d001Name1Map.get(detail._score);
                } else {
                    printScore = detail._score;
                }

            }
            return printScore;
        }

        private static Tuple<String, Tuple<String, List<String>>> getPrintScoreAndMarkAndAttribute(final Param param, final ScoreDetail detail) {
            String printScore;
            List<String> attributes = new ArrayList<String>();
            final boolean useD001 = param._d065Name1List.contains(detail._subclass.keySubclasscd());
            if (null == detail._score) {
                if (param._isMeikei && "*".equals(detail._valueDi)) {
                    printScore = defstr(detail._suppScore, detail._valueDi);
                } else if (param._isNichidaiNikou) {
                    if ("*".equals(detail._valueDi)) {
                        if (StringUtils.isBlank(detail._suppScore)) {
                            printScore = "(欠)"; // 欠席
                            attributes.add(ATTRIBUTE_CENTERING);
                        } else {
                            printScore = "(" + detail._suppScore + ")"; // 欠席見込点
                        }
                    } else if ("**".equals(detail._valueDi)) {
                        if (StringUtils.isBlank(detail._suppScore)) {
                            printScore = "(公欠)"; // 公欠
                            attributes.add(ATTRIBUTE_CENTERING);
                        } else {
                            printScore = "(" + detail._suppScore + ")"; // 公欠見込点
                        }
                    } else {
                        printScore = detail._valueDi;
                    }
                } else {
                    printScore = detail._valueDi;
                }
            } else {
                if (useD001) {
                    printScore = param._d001Name1Map.get(detail._score);
                } else {
                    printScore = detail._score;
                }
            }
            if (detail._isNarakenKekkaOver) {
                if (null == printScore) {
                    printScore = detail._recordScoreDatScore;
                }
                printScore = "(" + (StringUtils.isEmpty(printScore) ? "  " : printScore) + ")";
            }

            String mark = null;
            if (param._isShimaneken) {
                if (NumberUtils.isNumber(printScore)) {

                    if (null != detail._subclass) {
                        BigDecimal border = null;
                        if ("1".equals(param._keikokutenKubun)) {
                            final Map<String, String> chairInfo = detail._subclass._chairInfos.get(detail._chaircd);
                            if (null != chairInfo) {
                                border = toBigDecimal(KnjDbUtils.getString(chairInfo, "KEIKOKUTEN"), BigDecimal.ZERO);
                                if (param._isOutputDebug) {
                                    param.logOnce(" subclass = " + detail._subclass.keySubclasscd() + ", chaircd = " + detail._chaircd + ", border = " + border);
                                }
                            }
                        } else if ("2".equals(param._keikokutenKubun)) {
                            final String chairGroupCd = detail._student._subclasscdChairGroupCdMap.get(detail._subclass.keySubclasscd());
                            if (null != chairGroupCd) {
                                final BigDecimal avg = toBigDecimal(detail._subclass._chairGroupAvgs.get(chairGroupCd), BigDecimal.ZERO);
                                final BigDecimal bunsi = toBigDecimal(param._ruikeiheikinBunsi, BigDecimal.ZERO);
                                final BigDecimal bunbo = toBigDecimal(param._ruikeiheikinBunbo, BigDecimal.ONE);
                                border = avg.multiply(bunsi).divide(bunbo, 5, BigDecimal.ROUND_HALF_UP);
                                if (param._isOutputDebug) {
                                    param.logOnce(" subclass = " + detail._subclass.keySubclasscd() + ", chairGroupCd = " + chairGroupCd + ", avg = " + avg + ", bunsi = " + bunsi + ", bunbo = "+ bunbo + ", border = " + border);
                                }
                            }
                        }
                        if (null != border) {
                            if (new BigDecimal(printScore).compareTo(border) <= 0) {
                                mark = "▲" + StringUtils.repeat(" ", 3 - defstr(printScore).length());
                            }
                        }
                    }
                }
            }
            if (null == mark) {
                if (!useD001 && "990009".equals(param._testKindCd) && param._creditDrop && "1".equals(detail._score)) {
                    mark = "*";
                }
            }

            return Tuple.of(printScore, Tuple.of(defstr(mark, ""), attributes));
        }

        private static class Form2 {
            boolean _hasData = false;

            private static List<List<Student>> getPageList(final List<Student> list, final Param param) {
                final List<List<Student>> rtn = new ArrayList();
                List<Student> current = null;
                int currentLines = 0;
                for (final Student o : list) {
                    final int scoreDetailListSize = getScoreDetailList(param, o).size();
                    final int studentLine = scoreDetailListSize / param._formMaxColumn + (scoreDetailListSize % param._formMaxColumn == 0 ? 0 : 1);
                    if (null == current || currentLines + Math.max(1, studentLine) > param._formMaxLine) {
                        current = new ArrayList<Student>();
                        currentLines = 0;
                        rtn.add(current);
                    }
                    current.add(o);
                    currentLines += studentLine;
                }
                return rtn;
            }

            public void print2(final IPdf ipdf, final Param param, final StudentGroup studentGroup) {

                final List<Student> studentsAll = getPrintStudentAll(param, studentGroup);

                final List<List<Student>> pageList = getPageList(studentsAll, param);

                for (int pi = 0; pi < pageList.size(); pi++) {
                    setForm(ipdf, param._formname, 1, param);

                    final List<Student> students = pageList.get(pi);
                    printHeader2(ipdf, param, studentGroup);

                    int line = 0;
                    for (int j = 0; j < students.size(); j++) {
                        line += 1;
                        final Student student = students.get(j);

                        // 学籍番号表記
                        String nameNo = "1";
                        if ("1".equals(param._use_SchregNo_hyoji)) {
                            ipdf.VrsOutn("SCHREG_NO", line, student._schregno); // 学籍番号
                            nameNo = "2"; // 学籍番号表示用の氏名フィールド
                        }

                        ipdf.VrsOutn("ATTENDNO", line, student.getPrintAttendno()); // 出席番号

                        String showName = "";
                        try {
                            showName = param._staffInfo.getStrEngOrJp(student._name, student._nameEng);
                        } catch (Throwable t) {
                            showName = student._name;
                        }
                        if ("1".equals(param._knjd615vNameKirikae)) {
                            final int ketaName = getMS932ByteLength(showName);
                            ipdf.VrsOutn("NAME" + nameNo + (ketaName <= 20 ? "" : ketaName <= 30 ? "_2" : "_3"), line, showName);
                        } else {
                            ipdf.VrsOutn("NAME" + nameNo, line, showName); // 生徒氏名
                        }

                        ipdf.VrsOutn("TOTAL", line, student._scoreSum999999); // 総合点
                        ipdf.VrsOutn("AVERAGE", line, student._scoreAvg999999); // 平均点
                        final String classRank = 0 >= student._classRank ? "" : String.valueOf(student._classRank);
                        ipdf.VrsOutn("RANK1", line, classRank); // 学級順位
                        final String rank = 0 >= student._rank ? "" : String.valueOf(student._rank);
                        ipdf.VrsOutn("RANK2", line, rank); // 指定順位
                        ipdf.VrsOutn("FAIL1", line, student.getKettenKamokuCount(param)); // 欠点科目数
                        printAttendInfo(ipdf, param, student._attendInfo, line);
                        printRemark(ipdf, param, student, line);

                        int col = 0;
                        final List<ScoreDetail> detaillist = getScoreDetailList(param, student);
                        for (int si = 0; si < detaillist.size(); si++) {
                            final ScoreDetail detail = detaillist.get(si);
                            if (col >= param._formMaxColumn) {
                                line += 1;
                                col = 0;
                            }
                            col++;
                            final String scol = String.valueOf(col);
                            if (ScoreDetail.isFail(param, detail)) { ipdf.VrAttributen("GRADING" + scol, line, ATTRIBUTE_KETTEN); }
                            String scoreField2 = "";
                            String printScore = getPrintScore2(param, detail);
                            if (detail._isNarakenKekkaOver) {
                                if (null == printScore) {
                                    printScore = detail._recordScoreDatScore;
                                }
                                printScore = "(" + (StringUtils.isEmpty(printScore) ? " " : printScore) + ")";
                                scoreField2 = getMS932ByteLength(printScore) > 3 ? "_2": "";
                            }
                            ipdf.VrsOutn("GRADING" + scol + scoreField2, line, printScore); // 成績
                            if (ScoreDetail.isFail(param, detail)) { ipdf.VrAttributen("GRADING" + scol, line, ATTRIBUTE_NORMAL); }
                            ipdf.VrsOutn("SUBJECT" + scol, line, detail._subclass._subclassabbv); // 科目名

                             // 単位数
                            ipdf.VrsOutn("CREDIT" + scol, line, nullToBlank(detail._credits));
                            ipdf.VrsOutn("TOTALLESSON" + scol, line, nullToBlank(detail._jisu)); // 総時数
                            final String pf = (param._definecode.absent_cov == 3 || param._definecode.absent_cov == 4) ? "_2" : "";
                            final String field = "KEKKA" + scol + pf;
                            if (detail._isKekkaOver) { ipdf.VrAttributen(field, line, ATTRIBUTE_KEKKAOVER); }
                            ipdf.VrsOutn(field, line, getKekkaString(param, detail._absent)); // 欠課数
                            if (detail._isKekkaOver) { ipdf.VrAttributen(field, line, ATTRIBUTE_NORMAL); }
                            _hasData = true;
                        }
                    }
                    ipdf.VrEndPage();
                }
            }

            private static List<ScoreDetail> getScoreDetailList(final Param param, final Student student) {
                final List<ScoreDetail> detaillist = new ArrayList(student.getCountScoreDetails(param));
                Collections.sort(detaillist, new ScoreDetailComparator());
                final List<ScoreDetail> rtn = new ArrayList();
                for (int si = 0; si < detaillist.size(); si++) {
                    final ScoreDetail detail = detaillist.get(si);
                    if (param._notOutputSougou && "90".equals(detail._subclass._classcd)) {
                        continue;
                    }
                    rtn.add(detail);
                }
                return rtn;
            }

            private static void printRemark(final IPdf ipdf, final Param param, final Student student, final int line) {
                final String remark = student.getRemark(param);

                final int keta = getMS932ByteLength(remark);
                ipdf.VrsOutn("REMARK" + (keta <= 20 ? "" : keta <= 20 * 3 ? "2_1" : "3_1"), line, remark);  // 備考
            }

            public static class ScoreDetailComparator implements Comparator<ScoreDetail> {
                public int compare(final ScoreDetail s1, final ScoreDetail s2) {
                    return s1._subclass._subclasscd.compareTo(s2._subclass._subclasscd);
                }
            }

            private void printHeader2(final IPdf ipdf, final Param param, final StudentGroup studentGroup) {
                if (isAlpPdf(ipdf)) {
                    ipdf.VrsOut("TITLE", param._nendo + "　" + defstr(param._semesterName) + "  " + defstr(param._testItem._testitemname) + "　成績一覧表　"); // 年度
                } else {
                    ipdf.VrsOut("NENDO", param._nendo); // 年度
                    ipdf.VrsOut("TERM", param._semesterName); // 学期
                    ipdf.VrsOut("TEST", param._testItem._testitemname); // テスト名
                }
                ipdf.VrsOut("CLASSNAME", studentGroup.getName(param)); // クラス名

                if (param._isNaraken) {
                    ipdf.VrsOut("NOTE1", "()");
                    ipdf.VrsOut("NOTE2", "：欠課時数超過");
                } else {
                    ipdf.VrAttribute("NOTE1", ATTRIBUTE_KEKKAOVER);
                    ipdf.VrsOut("NOTE1", " ");
                    ipdf.VrsOut("NOTE2", "：欠課時数超過");
                }
                ipdf.VrAttribute("NOTE3", ATTRIBUTE_KETTEN);
                ipdf.VrsOut("NOTE3", " ");
                ipdf.VrsOut("NOTE4", "：欠点科目");

                ipdf.VrsOut("DATE2", param._attendTermKekka); // 出欠集計範囲

                ipdf.VrsOut("DETAIL1", "科目名"); // 項目
                ipdf.VrsOut("DETAIL2", "総時数"); // 項目

                ipdf.VrsOut("DETAIL1_1", param._item1Name); // 項目
                ipdf.VrsOut("DETAIL1_2", "単位数"); // 項目
                ipdf.VrsOut("DETAIL2_1", "欠課"); // 項目

                ipdf.VrsOut("T_TOTAL", param._form2Item4Name); // 項目
                ipdf.VrsOut("T_AVERAGE", param._form2Item5Name); // 項目
                ipdf.VrsOut("T_RANK1", "学級順位"); // 項目
                ipdf.VrsOut("T_RANK2", param._rankName + "順位"); // 項目

                ipdf.VrsOut("DATE3", param._attendTerm); // 出欠の記録の集計範囲

                ipdf.VrsOut("ymd1", param._now); // 作成日
                ipdf.VrsOut(getFieldForData(ipdf, new String[] {"HR_TEACHER", "HR_TEACHER2"}, studentGroup._staffname, param), studentGroup._staffname);  //担任名
                for (int i = 1; i <= 8; i++) {
                    final String name1 = param._d055StampMap.get("0" + String.valueOf(i));
                    if (StringUtils.isBlank(name1)) {
                        continue;
                    }
                    ipdf.VrsOut(getFieldForData(ipdf, new String[] {"JOB" + String.valueOf(i) + "_1", "JOB" + String.valueOf(i) + "_2"}, name1, param), name1);
                }
            }
        }

        static class Form1 {
            final Param _param;
            boolean _hasData;
            Form1(final Param param) {
                _param = param;
            }

            private void print(final DB2UDB db2, final IPdf ipdf, final StudentGroup studentGroup, final List<Student> stulist) {
                setForm(ipdf, _param._formname, 4, _param);

                if (!checkForm(ipdf, _param._formname2)) {
                    _param._formname2 = null;
                }
                if (!checkForm(ipdf, _param._formname3)) {
                    _param._formname3 = null;
                }

                final List<SubClass> printSubclassList = getPrintSubclassList(_param, studentGroup, stulist);

                final List<List<SubClass>> subclassListList;
                if (null != _param._formname2) {
                    subclassListList = getSubClassListList(_param, printSubclassList, _param._form2MaxColumn);
                    if (subclassListList.size() > 0) {
                        final List<SubClass> lastSubclassList = subclassListList.get(subclassListList.size() - 1);
                        log.debug(" oosugidesu! " + lastSubclassList.size() + " / " + _param._formMaxColumn);
                        if (lastSubclassList.size() > _param._formMaxColumn) {
                            //①下記の②の条件"以外"の場合にダミーページを利用。
                            if (null == _param._formname3 || subclassListList.size() != 1
                                    || _param._formMaxColumn >= lastSubclassList.size() || null != _param._formname3 && lastSubclassList.size() > _param._form3MaxColumn) {
                                // 最後のページの科目が多すぎて集計欄が表示できないので、集計欄のみ表示するためのダミーを追加
                                final List<SubClass> dummy = new ArrayList<SubClass>();
                                subclassListList.add(dummy);
                            }
                        }
                    }
                } else {
                    subclassListList = getSubClassListList(_param, printSubclassList, _param._formMaxColumn);
                }

                for (int pi = 0, pages = subclassListList.size(); pi < pages; pi++) {
                    final List<SubClass> subclassList = subclassListList.get(pi);
                    final boolean isLastPage = pi == pages - 1;

                    log.info(" subclassList page = " + String.valueOf(pi + 1) + " / " + subclassListList.size());

                    final int maxCol;
                    if (null != _param._formname3 && pages == 1 && _param._formMaxColumn < subclassList.size() && subclassList.size() <= _param._form3MaxColumn) {
                        //②利用フォームが設定(PATTERN1に該当)されていて、1ページに収まる出力で、param._form3MaxColumnに収まる出力の場合のみ
                        setForm(ipdf, _param._formname3, 4, _param);
                        maxCol = _param._form3MaxColumn;
                    } else if (pi < pages - 1 && null != _param._formname2) {
                        // 右側集計欄は最後のページのみ
                        setForm(ipdf, _param._formname2, 4, _param);
                        maxCol = _param._form2MaxColumn;
                    } else {
                        setForm(ipdf, _param._formname, 4, _param);
                        maxCol = _param._formMaxColumn;
                    }
                    ipdf.addRecordField(_param._recordField);
                    printHeader(ipdf, studentGroup);
                    if (isLastPage) {
                        if (!PATTERN5.equals(_param._outputPattern)) {
                            printGroupInfo(db2, ipdf, studentGroup);
                        }
                    }

                    for (int sti = 0; sti < stulist.size(); sti++) {
                        final Student student = stulist.get(sti);
                        final int line = gnumToLine(student._gnum, _param._formMaxLine);
                        printStudentName(ipdf, line, student);

                        if (isLastPage) {
                            printStudentTotal(db2, ipdf, line, student);
                        }
                        if (PRGID_KNJD615H.equals(_param._prgId)) {
                            printHyotei(ipdf, line, student);
                        } else {
                            printRemark(ipdf, line, student);
                        }
                    }
                    final List<String> studentCourses = getStudentCourses(stulist);
                    //教科毎の科目数をカウント
                    final Map<String, String> classSubclassMap = new TreeMap<String, String>();
                    int subClassCnt = 1;
                    String befClassCd = "";
                    for (final SubClass subclass : subclassList) {

                        if (!"".equals(befClassCd) && !befClassCd.equals(subclass._classcd)) {
                            subClassCnt = 1;
                        }

                        classSubclassMap.put(subclass._classcd, String.valueOf(subClassCnt));
                        subClassCnt++;
                        befClassCd = subclass._classcd;
                    }
                    befClassCd = "";
                    int subClassOrder = 1;
                    int totalJisu = 0;
                    int totalCredit = 0;
                    for (int subi = 0, size = subclassList.size(); subi < size; subi++) {

                        final SubClass subclass = subclassList.get(subi);
                        if (_param._isOutputDebug) {
                            log.info("p=" + pi + ", i=" + subi + ", subclasscd=" + subclass._subclasscd + " " + subclass._subclassabbv);
                        }
                        final boolean pat3isKariHyotei = PATTERN3.equals(_param._outputPattern) && pat3SubclassIsKariHyotei(_param, subclass, stulist);
                        final int abbvLen = getMS932ByteLength(subclass._classabbv);
                        final int abbvStrCnt = defstr(subclass._classabbv).length();
                        if (!"".equals(befClassCd) && !befClassCd.equals(subclass._classcd)) {
                            subClassOrder = 1;
                        }
                        final ClassAbbvFieldSet abbvFieldSet = setClassAbbv(Integer.parseInt(classSubclassMap.get(subclass._classcd)), subClassOrder, subclass._classabbv, abbvLen, abbvStrCnt);
                        printSubclassHeader(ipdf, subi + 1, subclass, pat3isKariHyotei, abbvFieldSet, studentCourses);
                        if (NumberUtils.isDigits(subclass.getJisu())) totalJisu += Integer.parseInt(subclass.getJisu());
                        if (0 < subclass._creditSet.size()) {
                            totalCredit += subclass._creditSet.last(); // MAX
                        }

                        for (final Student student : stulist) {
                            final int line = gnumToLine(student._gnum, _param._formMaxLine);
                            final ScoreDetail detail = student._scoreDetails.get(subclass._subclasscd);
                            if (null != detail) {
                                printScoreKekka(ipdf, student._schregno, detail, line, subi + 1, subclass, pat3isKariHyotei);
                            }
                        }

                        //学級平均・合計
                        printSubclassStat(ipdf, subi + 1, subclass);

                        ipdf.VrEndRecord();
                        subClassOrder++;
                        befClassCd = subclass._classcd;
                        _hasData = true;
                    }
                    if(PATTERN1.equals(_param._outputPattern) && _param._isKomazawa) {
                        ipdf.VrsOut("TOTAL_CREDIT", String.valueOf(totalCredit));
                        final String present = defstr(getShukkoTeisu(db2, _param._year, _param._semester, _param._grade, "0", "000"));
                        ipdf.VrsOut("PRESENT1", present);
                        ipdf.VrsOut("SCHOOL_TIME", String.valueOf(totalJisu));
                    }

                    if (PATTERN3.equals(_param._outputPattern) || PATTERN4.equals(_param._outputPattern) || PATTERN5.equals(_param._outputPattern) || PATTERN1.equals(_param._outputPattern)) {
                        for (int i = subclassList.size(); i < maxCol; i++) {
                            //教科名
                            Form1.setRecordString(ipdf, "credit1", i + 1, "DUMMY");
                            ipdf.VrAttribute("credit1", "X=10000");
                            ipdf.VrEndRecord();
                            _hasData = true;
                        }
                    }
                }
            }

            private static int gnumToLine(final int gnum, final int maxLine) {
                return 0 != gnum && gnum % maxLine == 0 ? maxLine : gnum % maxLine;
            }

            private boolean checkForm(final IPdf ipdf, final String formname) {
                if (null != formname && ipdf instanceof SvfPdf) {
                    final SvfPdf svfPdf = (SvfPdf) ipdf;
                    final String path = svfPdf.getVrw32alp().getPath(formname);
                    boolean exists = false;
                    if (null != path) {
                        exists = new File(path).exists();
                    }
                    if (!exists) {
                        log.warn("no form file : " + path);
                    }
                    return exists;
                }
                return false;
            }

            private static List<SubClass> getPrintSubclassList(final Param param, final StudentGroup studentGroup, final List<Student> stulist) {
                final List<SubClass> printSubclassList = new ArrayList<SubClass>(studentGroup._subclasses.values());
                final List<String> courses = getStudentCourses(stulist);
                for (final Iterator<SubClass> it = printSubclassList.iterator(); it.hasNext();) {
                    final SubClass subclass = it.next();
                    final boolean notOutputColumn = "90".equals(subclass._classcd) && param._notOutputSougou;
                    if (notOutputColumn) {
                        it.remove();
                        continue;
                    }
                    if (!param._isPrintSakiKamoku && param.getSubclassMst(subclass.keySubclasscd()).isSaki(param, courses)) {
                        if (param._isOutputDebug) {
                            log.info(" skip saki " + subclass.keySubclasscd());
                        }
                        it.remove();
                        continue;
                    }
                    if (param._isNoPrintMoto && param.getSubclassMst(subclass.keySubclasscd()).isMoto(param, courses)) {
                        if (param._isOutputDebug) {
                            log.info(" skip moto " + subclass.keySubclasscd());
                        }
                        it.remove();
                        continue;
                    }
                    if (param._d079Name1List.contains(subclass.keySubclasscd())) {
                        if (param._isOutputDebug) {
                            log.info(" skip d079 " + subclass.keySubclasscd());
                        }
                        it.remove();
                        continue;
                    }
                    if (PRGID_KNJD615P.equals(param._prgId)) {
                        boolean hasScore = false;
                        for (final Student student : stulist) {
                            final ScoreDetail detail = student._scoreDetails.get(subclass._subclasscd);
                            if (null != detail && (!StringUtils.isBlank(detail._score) || !StringUtils.isBlank(detail._valueDi))) {
                                hasScore = true;
                                break;
                            }
                        }
                        if (!hasScore) {
                            if (param._isOutputDebug) {
                                log.info(" skip all blank " + subclass.keySubclasscd());
                            }
                            it.remove();
                            continue;
                        }
                    }
                }
                Collections.sort(printSubclassList, new SubClass.PrintOrderSorter(param));
                return printSubclassList;
            }

            private static List<String> getStudentCourses(final List<Student> studentList) {
                final List<String> courses = new ArrayList<String>();
                for (final Student student : studentList) {
                    if (null != student._course) {
                        courses.add(student._course);
                    }
                }
                return courses;
            }

            public static boolean outputCsv(final DB2UDB db2, final List<List<String>> outputList, final Param param, final StudentGroup studentGroup, final List<Student> stulist) {

                boolean hasData = false;

                final List<SubClass> printSubclassList = getPrintSubclassList(param, studentGroup, stulist);

                param._formMaxLine = printSubclassList.size();
                param._formMaxColumn = stulist.size();

                final List<List<String>> headerLineList = new ArrayList<List<String>>();
                final List<String> header1Line = newLine(headerLineList);
                header1Line.addAll(Arrays.asList("", param._nendo + "　" + param._title + param._subtitle));
                final String staffname = param._isPrintGroupHr ? "担任：" + defstr(studentGroup._staffname): "";

                final List<String> header2Line = newLine(headerLineList);
                header2Line.addAll(Arrays.asList(studentGroup.getName(param), "", "出欠集計範囲：" + param._attendTermKekka, "", "", "", "", "", staffname));

                final List<List<String>> blockStudentName = new ArrayList<List<String>>();
                List<String> nameLine0 = newLine(blockStudentName);
                List<String> nameLine1 = newLine(blockStudentName);
                List<String> nameLineSex;
                List<String> nameLineRyou;
                List<String> nameLineKaigai;
                List<String> nameLineSuisen;
                List<String> nameLineIb;
                List<String> nameLineAhoushiki;
                if (PRGID_KNJD615H.equals(param._prgId)) {
                    nameLineSex = newLine(blockStudentName);
                    nameLineRyou = newLine(blockStudentName);
                    nameLineKaigai = newLine(blockStudentName);
                    nameLineSuisen = newLine(blockStudentName);
                    nameLineIb = newLine(blockStudentName);
                    nameLineAhoushiki = newLine(blockStudentName);
                } else {
                    nameLineSex = new ArrayList<String>();
                    nameLineRyou = new ArrayList<String>();
                    nameLineKaigai = new ArrayList<String>();
                    nameLineSuisen = new ArrayList<String>();
                    nameLineIb = new ArrayList<String>();
                    nameLineAhoushiki = new ArrayList<String>();
                }

                nameLine0.add("教科");
                nameLine0.add("科目");
                nameLine0.add("単位数");
                if (!PRGID_KNJD615H.equals(param._prgId) && null != fieldClassTeacher(null, param, "")) {
                    nameLine0.add("教科担任名");
                }
                if (PRGID_KNJD615H.equals(param._prgId)) {
                    nameLine0.add("授業時数");
                } else if (PATTERN1.equals(param._outputPattern)) {
                    nameLine0.add("授業時数");
                }
                if (PATTERN1.equals(param._outputPattern)) {
                    nameLine0.add(param._item1Name + "・" + param._item2Name);
                    nameLine0.add("");
                } else if (PATTERN3.equals(param._outputPattern)) {
                    if (PRGID_KNJD615H.equals(param._prgId)) {
                        nameLine0.add("No.");
                    } else {
                        nameLine0.add("");
                    }
                }
                final int headerSize = nameLine0.size();
                if(PRGID_KNJD615H.equals(param._prgId)) {
                    final int innerHeaderSize = -1;
                    setSameSize(nameLine1, headerSize + innerHeaderSize);
                    nameLine1.add("氏名");
                    setSameSize(nameLineSex, headerSize + innerHeaderSize);
                    nameLineSex.add("性別");
                    setSameSize(nameLineRyou, headerSize + innerHeaderSize);
                    nameLineRyou.add("寮生");
                    setSameSize(nameLineKaigai, headerSize + innerHeaderSize);
                    nameLineKaigai.add("海外");
                    setSameSize(nameLineSuisen, headerSize + innerHeaderSize);
                    nameLineSuisen.add("推薦");
                    setSameSize(nameLineIb, headerSize + innerHeaderSize);
                    nameLineIb.add("IB");
                    setSameSize(nameLineAhoushiki, headerSize + innerHeaderSize);
                    nameLineAhoushiki.add("方式");
                } else {
                    setSameSize(nameLine1, headerSize);
                    setSameSize(nameLineSex, headerSize);
                    setSameSize(nameLineRyou, headerSize);
                    setSameSize(nameLineKaigai, headerSize);
                    setSameSize(nameLineSuisen, headerSize);
                    setSameSize(nameLineIb, headerSize);
                    setSameSize(nameLineAhoushiki, headerSize);
                }

                for (final Student student : stulist) {

                    nameLine0.add(student.getPrintAttendno());
//                    if (param._isTokiwagi) {
//                        nameLine1.add(defstr(student._name) + defstr(student._scholarshipName));
//                    } else {
                        String showName = "";
                        try {
                            showName = param._staffInfo.getStrEngOrJp(student._name, student._nameEng);
                        } catch (Throwable t) {
                            showName = student._name;
                        }
                        nameLine1.add(showName);
                        if (PRGID_KNJD615H.equals(param._prgId)) {
                            nameLineSex.add(student._sex);
                            nameLineRyou.add(student._ryou);
                            nameLineKaigai.add(student._kaigai);
                            nameLineSuisen.add(student._suisen);
                            nameLineIb.add(student._ib);
                            nameLineAhoushiki.add(student._aHouShiki);
                        }
//                    }
                }

                if (PRGID_KNJD615H.equals(param._prgId)) {
                    nameLine0.add("合計");
                    nameLine0.add("学級平均");
                    nameLine0.add(param._rankName + "平均");
                    nameLine0.add("欠点者数");
                    nameLine0.add("最高点");
                    nameLine0.add("最低点");
                } else if (PATTERN1.equals(param._outputPattern)) {
                    nameLine0.add("合計");
                    nameLine0.add("学級平均");
                    nameLine0.add(param._rankName + "平均");
                    nameLine0.add(!param._useKetten ? "" : "欠点者数");
                    nameLine0.add("最高点");
                    nameLine0.add("最低点");
                } else if (PATTERN3.equals(param._outputPattern)) {
                    nameLine0.add("合計");
                    nameLine0.add("人数");
                    nameLine0.add("組平均");
                    nameLine0.add("学年・科平均");
                    nameLine0.add("欠点者数");
                    nameLine0.add("授業時数");
                } else if (PATTERN4.equals(param._outputPattern)) {
                    nameLine0.add("合計");
                    nameLine0.add("人数");
                    nameLine0.add("組平均");
                    nameLine0.add(param._rankName + "平均");
                    nameLine0.add("欠点者数");
                } else if (PATTERN5.equals(param._outputPattern)) {
                    nameLine0.add("授業時数");
                }

                final List<List<String>> blockSubclassList = new ArrayList<List<String>>();

                String classabbvbefore = null;

                for (int coli = 0, size = printSubclassList.size(); coli < size; coli++) {

                    final List<String> line1 = newLine(blockSubclassList);
                    List<String> line2 = null;
                    List<String> line3 = null;

                    final SubClass subclass = printSubclassList.get(coli);

                    final boolean diff = !(null == subclass._classabbv && null == classabbvbefore || null != subclass._classabbv && subclass._classabbv.equals(classabbvbefore));
                    line1.add(diff ? subclass._classabbv : "");
                    line1.add(subclass._subclassname);
                    line1.add(subclass.getPrintCredit(param));
                    if (!PRGID_KNJD615H.equals(param._prgId) && null != fieldClassTeacher(null, param, subclass._staffname)) {
                        line1.add(subclass._staffname);
                    }
                    if (PRGID_KNJD615H.equals(param._prgId)) {
                        line1.add(subclass.getJisu());
                    } else if (PATTERN1.equals(param._outputPattern)) {
                        line1.add(subclass.getJisu());
                    }
                    final boolean pat3isKariHyotei = PATTERN3.equals(param._outputPattern) && pat3SubclassIsKariHyotei(param, subclass, stulist);
                    if (PRGID_KNJD615H.equals(param._prgId)) {
                        line2 = setSameSize(newLine(blockSubclassList), line1.size());
                        line3 = setSameSize(newLine(blockSubclassList), line1.size());

                        line1.add(param._item1Name);
                        line2.add("偏差値");
                        line3.add("欠課");
                    } else if (PATTERN1.equals(param._outputPattern)) {
                        line2 = setSameSize(newLine(blockSubclassList), line1.size());

                        line1.add(param._item1Name);
                        line1.add("");
                        line2.add(param._item2Name);
                        line2.add("");
                    } else if (PATTERN3.equals(param._outputPattern)) {
                        line2 = setSameSize(newLine(blockSubclassList), line1.size());
                        line3 = setSameSize(newLine(blockSubclassList), line1.size());

                        line1.add(param._item1Name);
                        line2.add(pat3isKariHyotei ? "仮評定" : "評定");
                        line3.add("欠課");
                    }

                    for (final Student student : stulist) {

                        // 欠課
                        List<String> scoreLine = null;
                        List<String> deviationLine = null;
                        List<String> absenceLine = null;
                        if (PRGID_KNJD615H.equals(param._prgId)) {
                            scoreLine = line1;
                            deviationLine = line2;
                            absenceLine = line3;
                        } else if (PATTERN3.equals(param._outputPattern)) {
                            scoreLine = line1;
                            absenceLine = line3;
                        } else if (PATTERN5.equals(param._outputPattern)) {
                            absenceLine = line1;
                        } else if (PATTERN4.equals(param._outputPattern)){
                            scoreLine = line1;
                        } else if (PATTERN1.equals(param._outputPattern)){
                            scoreLine = line1;
                            absenceLine = line2;
                        }
                        if (student._scoreDetails.containsKey(subclass._subclasscd)) {
                            final ScoreDetail detail = student._scoreDetails.get(subclass._subclasscd);

                            if (null != scoreLine) {
                                if ("2".equals(param._outputValue)) {
                                    scoreLine.add(detail.getPrintDeviation(param, 1));
                                } else {
                                    final Tuple<String, Tuple<String, List<String>>> printScoreAndMarkAndAttribute = getPrintScoreAndMarkAndAttribute(param, detail);
                                    final String printScore = printScoreAndMarkAndAttribute._first;
                                    final String mark = printScoreAndMarkAndAttribute._second._first;

                                    scoreLine.add(mark + defstr(printScore));
                                }
                                if (!PRGID_KNJD615H.equals(param._prgId) && PATTERN3.equals(param._outputPattern)) {
                                    // 評定
                                    final boolean isPrint = !SEMEALL.equals(param._semester) || SEMEALL.equals(param._semester) && (pat3isKariHyotei && "1".equals(detail._provFlg) || !pat3isKariHyotei && null == detail._provFlg); // 学年末以外は対象。学年末は「仮評定ならPROV_FLG=1が対象。そうでなければPROV_FLG=NULLが対象。」
                                    line2.add(isPrint ? detail._karihyotei : "");
                                }
                                if (PRGID_KNJD615H.equals(param._prgId)) {
                                    //偏差値
                                    deviationLine.add(detail.getPrintDeviation(param, 0));
                                }
                            }

                            final boolean isOutputJisu = PATTERN1.equals(param._outputPattern) && param._isOutputStudentJisu && null != detail._jisu && !subclass.getJisu().equals(detail._jisu.toString());
                            if (null != absenceLine) {
                                final String val;
                                if (isOutputJisu) {
                                    val = (null == detail._absent ? "0" : String.valueOf(detail._absent.intValue())) + "/" + detail._jisu.toString();
                                } else {
                                    val = getKekkaString(param, detail._absent);
                                }
                                absenceLine.add(val);
                            }
                        } else {
                            if (null != scoreLine) {
                                scoreLine.add("");
                            }
                            if (null != deviationLine) {
                                deviationLine.add("");
                            }
                            if (!PRGID_KNJD615H.equals(param._prgId) && PATTERN3.equals(param._outputPattern)) {
                                // 評定
                                line2.add("");
                            }
                            if (null != absenceLine) {
                                absenceLine.add("");
                            }
                        }
                    }

                    if (PATTERN5.equals(param._outputPattern)) {
                        line1.add(subclass.getJisu());
                    }
                    final boolean useD001 = param._d065Name1List.contains(subclass.keySubclasscd());
                    if (!useD001) {
                        //学級平均・合計
                        if (PRGID_KNJD615H.equals(param._prgId)) {
                            String divstr = "/";
                            if (subclass._scoreCount == null || "".equals(subclass._scoreCount)) {
                                divstr = "";
                            }
                            line1.add(defstr(subclass._scoretotal) + divstr + defstr(subclass._scoreCount));
                            line1.add(subclass._scoreHrAverage);
                            line1.add(subclass._recordAverageSdivDatAvg);
                            line1.add(subclass._scoreFailCnt);
                            line1.add(subclass._scoreMax);
                            line1.add(subclass._scoreMin);
                        } else if (PATTERN3.equals(param._outputPattern)) {
                            line1.add(subclass._scoretotal);
                            line1.add(subclass._scoreCount);
                            line1.add(subclass._scoreHrAverage);
                            line1.add(subclass._recordAverageSdivDatAvg);
                            line1.add(param._useKetten ? subclass._scoreFailCnt : "");
                            line1.add(subclass.getJisu());
                        } else if (PATTERN4.equals(param._outputPattern)) {
                            line1.add(subclass._scoretotal);
                            line1.add(subclass._scoreCount);
                            line1.add(subclass._scoreHrAverage);
                            line1.add(subclass._recordAverageSdivDatAvg);
                            line1.add(param._useKetten ? subclass._scoreFailCnt : "");
                        } else if (PATTERN1.equals(param._outputPattern)) {
                            if (!StringUtils.isBlank(subclass._scoretotal) || !StringUtils.isBlank(subclass._scoreCount)) {
                                line1.add(defstr(subclass._scoretotal) + "/" + defstr(subclass._scoreCount));
                            } else {
                                line1.add("");
                            }
                            line1.add(subclass._scoreHrAverage);
                            line1.add(subclass._recordAverageSdivDatAvg);
                            line1.add(param._useKetten ? subclass._scoreFailCnt : "");
                            line1.add(subclass._scoreMax);
                            line1.add(subclass._scoreMin);
                        }
                    }

                    hasData = true;
                    classabbvbefore = subclass._classabbv;
                }

                final boolean outputScoreColumns = PATTERN1.equals(param._outputPattern) || PATTERN3.equals(param._outputPattern) || PATTERN4.equals(param._outputPattern);
                final boolean outputAttendColumns = !PRGID_KNJD615H.equals(param._prgId) && (PATTERN1.equals(param._outputPattern) || PATTERN3.equals(param._outputPattern) || PATTERN5.equals(param._outputPattern));
                final boolean outputHyoteicolumns = PRGID_KNJD615H.equals(param._prgId);
                final int scoreColumns = outputScoreColumns ? 5 : 0;
                final int attendColumns = outputAttendColumns ? 8 : 0;
                final int hyoteiColumns = outputHyoteicolumns ? 5 : 0;
                final int totalColumns = scoreColumns + attendColumns + hyoteiColumns + 1;
                final List[] columnsStudentTotalHeader = new List[totalColumns];
                for (int i = 0; i < columnsStudentTotalHeader.length; i++) {
                    columnsStudentTotalHeader[i] = new ArrayList();

                    for (int j = 0; j < headerSize - 1; j++) {
                        columnsStudentTotalHeader[i].add(null);
                    }
                }
                int j = 0;
                if (outputScoreColumns) {
                    columnsStudentTotalHeader[j++].add(param._item4Name);
                    columnsStudentTotalHeader[j++].add(param._item5Name);
                    columnsStudentTotalHeader[j++].add("学級順位");
                    columnsStudentTotalHeader[j++].add(param._rankName + "順位");
                    columnsStudentTotalHeader[j++].add(!param._useKetten ? "" : "欠点科目数");
                }
                if (outputAttendColumns) {
                    columnsStudentTotalHeader[j++].add("授業日数");
                    columnsStudentTotalHeader[j++].add("出席停止・忌引き等の日数");
                    columnsStudentTotalHeader[j++].add("留学中の授業日数");
                    columnsStudentTotalHeader[j++].add("出席しなければならない日数");
                    columnsStudentTotalHeader[j++].add("欠席日数");
                    columnsStudentTotalHeader[j++].add("出席日数");
                    columnsStudentTotalHeader[j++].add("遅刻回数");
                    columnsStudentTotalHeader[j++].add("早退回数");
                }
                if (outputHyoteicolumns) {
                    columnsStudentTotalHeader[j++].add("評定5");
                    columnsStudentTotalHeader[j++].add("評定4");
                    columnsStudentTotalHeader[j++].add("評定3");
                    columnsStudentTotalHeader[j++].add("評定2");
                    columnsStudentTotalHeader[j++].add("評定1");
                }
                if (!PRGID_KNJD615H.equals(param._prgId)
                        && (PATTERN1.equals(param._outputPattern) || PATTERN3.equals(param._outputPattern) || PATTERN4.equals(param._outputPattern) || PATTERN5.equals(param._outputPattern))
                        ) {
                    columnsStudentTotalHeader[j++].add("備考");
                }
                final List[] columnsStudentTotal = new List[totalColumns];
                for (int i = 0; i < columnsStudentTotal.length; i++) {
                    columnsStudentTotal[i] = new ArrayList();
                }
                for (final Student student : stulist) {

                    int i = 0;
                    final AttendInfo attendInfo = student._attendInfo;
                    final String scoreAvg = param._isSapporo && !param._testKindCd.startsWith("99") ? "" : student._scoreAvg999999;
                    final String classRank = 0 >= student._classRank ? "" : String.valueOf(student._classRank);
                    final String rank = 0 >= student._rank ? "" : String.valueOf(student._rank);
                    final String countFail = !param._useKetten || 0 >= student.countFail(param) ? "" : String.valueOf(student.countFail(param));
                    if (outputScoreColumns) {
                        columnsStudentTotal[i++].add(student._scoreSum999999);
                        columnsStudentTotal[i++].add(scoreAvg);
                        columnsStudentTotal[i++].add(classRank);
                        columnsStudentTotal[i++].add(rank);
                        columnsStudentTotal[i++].add(countFail);
                    }
                    if (outputAttendColumns) {
                        columnsStudentTotal[i++].add(zeroToNull(param, attendInfo._lesson));
                        columnsStudentTotal[i++].add(zeroToNull(param, attendInfo._suspend + attendInfo._mourning));
                        columnsStudentTotal[i++].add(zeroToNull(param, attendInfo._transDays));
                        columnsStudentTotal[i++].add(zeroToNull(param, attendInfo._mLesson));
                        columnsStudentTotal[i++].add(zeroToNull(param, attendInfo._sick));
                        columnsStudentTotal[i++].add(String.valueOf(attendInfo._present));
                        columnsStudentTotal[i++].add(zeroToNull(param, attendInfo._late));
                        columnsStudentTotal[i++].add(zeroToNull(param, attendInfo._early));
                    }
                    if (outputHyoteicolumns) {
                        if (student._schScoreCnt != null) {
                            columnsStudentTotal[i++].add(String.valueOf(student._schScoreCnt._cntScore5));
                            columnsStudentTotal[i++].add(String.valueOf(student._schScoreCnt._cntScore4));
                            columnsStudentTotal[i++].add(String.valueOf(student._schScoreCnt._cntScore3));
                            columnsStudentTotal[i++].add(String.valueOf(student._schScoreCnt._cntScore2));
                            columnsStudentTotal[i++].add(String.valueOf(student._schScoreCnt._cntScore1));
                        } else {
                            columnsStudentTotal[i++].add("");
                            columnsStudentTotal[i++].add("");
                            columnsStudentTotal[i++].add("");
                            columnsStudentTotal[i++].add("");
                            columnsStudentTotal[i++].add("");
                        }
                    }

                    if (!PRGID_KNJD615H.equals(param._prgId)) {
                        columnsStudentTotal[i++].add(student.getRemark(param));
                    }

                }

                final List columnsStudentTotalAll = new ArrayList();
                setColumnList(columnsStudentTotalAll, totalColumns);
                joinColumnListArray(columnsStudentTotalAll, columnsStudentTotalHeader);
                joinColumnListArray(columnsStudentTotalAll, columnsStudentTotal);

                if (!PATTERN5.equals(param._outputPattern)) {
                    List[] columnStudentTotalFooterList = new List[totalColumns];
                    for (int i = 0; i < columnStudentTotalFooterList.length; i++) {
                        columnStudentTotalFooterList[i] = new ArrayList();
                    }

                    //学級合計
                    String groupFlg = null;
                    if (OUTPUT_RANK1.equals(param._outputRank)) {
                        groupFlg = StudentGroup.AVG_FLG_HR;
                    } else if (OUTPUT_RANK2.equals(param._outputRank)) {
                        groupFlg = StudentGroup.AVG_FLG_GRADE;
                    } else if (OUTPUT_RANK3.equals(param._outputRank)) {
                        groupFlg = StudentGroup.AVG_FLG_COURSE;
                    } else if (OUTPUT_RANK4.equals(param._outputRank)) {
                        groupFlg = StudentGroup.AVG_FLG_MAJOR;
                    }
                    final String gavg = studentGroup.getRecordRankSdivAverage(groupFlg, param.SUBCLASSCD999999, "AVG");
                    final String gtotal = studentGroup.getRecordRankSdivAverage(groupFlg, param.SUBCLASSCD999999, "TOTAL");

                    final String totalMax = StudentGroup.totalMax(studentGroup._students);
                    final String totalMin = StudentGroup.totalMin(studentGroup._students);
                    final String failHrTotal = param._isSapporo ? "" : StudentGroup.failTotal(param, studentGroup._students);
                    final String suspendMourningTotal = StudentGroup.suspendMourningTotal(param, studentGroup._students);
                    final String kessekiTotal = StudentGroup.kessekiTotal(param, studentGroup._students);
                    final String lateTotal = StudentGroup.lateTotal(param, studentGroup._students);
                    final String earlyTotal = StudentGroup.earlyTotal(param, studentGroup._students);
                    final String avgHrTotal = studentGroup.getRecordRankSdivAverage(StudentGroup.AVG_FLG_HR, param.SUBCLASSCD999999, "TOTAL");
                    final String avgHrAverage = studentGroup.getRecordRankSdivAverage(StudentGroup.AVG_FLG_HR, param.SUBCLASSCD999999, "AVG");
                    final String avgHrCount = studentGroup.getRecordRankSdivAverage(StudentGroup.AVG_FLG_HR, param.SUBCLASSCD999999, "COUNT");

                    if (PRGID_KNJD615H.equals(param._prgId)) {
                        setSameSize(columnStudentTotalFooterList[0], 6);
                        columnStudentTotalFooterList[0].set(1, defstr(avgHrTotal));
                        setSameSize(columnStudentTotalFooterList[1], 2).set(1, defstr(avgHrAverage));
                        setSameSize(columnStudentTotalFooterList[4], 4).set(3, failHrTotal);
                        columnStudentTotalFooterList[0].set(4, totalMax);
                        columnStudentTotalFooterList[0].set(5, totalMin);
                    } else if (PATTERN1.equals(param._outputPattern)) {
                        setSameSize(columnStudentTotalFooterList[0], 7);
                        columnStudentTotalFooterList[0].set(1, defstr(avgHrTotal));
                        columnStudentTotalFooterList[0].set(2, null != gtotal ? "" : gtotal);
                        columnStudentTotalFooterList[0].set(3, failHrTotal);
                        columnStudentTotalFooterList[0].set(4, totalMax);
                        columnStudentTotalFooterList[0].set(5, totalMin);
                        setSameSize(columnStudentTotalFooterList[6], 1).set(0, suspendMourningTotal);
                        setSameSize(columnStudentTotalFooterList[9], 1).set(0, kessekiTotal);
                        setSameSize(columnStudentTotalFooterList[11], 1).set(0, lateTotal);
                        setSameSize(columnStudentTotalFooterList[12], 1).set(0, earlyTotal);
                        setSameSize(columnStudentTotalFooterList[1], 3);
                        columnStudentTotalFooterList[1].set(1, param._isSapporo && !param._testKindCd.startsWith("99") || null == avgHrAverage ? "" : avgHrAverage);
                        columnStudentTotalFooterList[1].set(2, param._isSapporo && !param._testKindCd.startsWith("99") || null == gavg ? "" : gavg);

                        setSameSize(columnStudentTotalFooterList[5], 8);
                        columnStudentTotalFooterList[5].set(7, "出席率 " + (append(sishaGonyu(StudentGroup.shussekiRitsu(studentGroup._students)), "%")));
                        setSameSize(columnStudentTotalFooterList[6], 8);
                        columnStudentTotalFooterList[6].set(7, "欠席率 " + (append(sishaGonyu(StudentGroup.kessekiRitsu(studentGroup._students)), "%")));
                    } else if (PATTERN3.equals(param._outputPattern)) {
                        setSameSize(columnStudentTotalFooterList[0], 3).set(2, defstr(avgHrTotal));
                        setSameSize(columnStudentTotalFooterList[1], 3).set(2, defstr(avgHrAverage));
                        setSameSize(columnStudentTotalFooterList[4], 5).set(4, failHrTotal);
                        setSameSize(columnStudentTotalFooterList[6], 1).set(0, suspendMourningTotal);
                        setSameSize(columnStudentTotalFooterList[9], 1).set(0, kessekiTotal);
                        setSameSize(columnStudentTotalFooterList[11], 1).set(0, lateTotal);
                        setSameSize(columnStudentTotalFooterList[12], 1).set(0, earlyTotal);
                    } else if (PATTERN4.equals(param._outputPattern)) {
                        columnStudentTotalFooterList[0].add(defstr(avgHrTotal));
                        columnStudentTotalFooterList[0].add(avgHrCount);
                        columnStudentTotalFooterList[0].add(defstr(avgHrAverage));
                        columnStudentTotalFooterList[0].add(defstr(gavg));
                        columnStudentTotalFooterList[0].add(failHrTotal);
                    }
                    joinColumnListArray(columnsStudentTotalAll, columnStudentTotalFooterList);
                }


                final List<List<String>> columnList = new ArrayList<List<String>>();
                columnList.addAll(blockStudentName);
                columnList.addAll(blockSubclassList);
                columnList.addAll(columnsStudentTotalAll);

                outputList.addAll(headerLineList);
                outputList.addAll(columnListToLines(columnList));
                newLine(outputList); // ブランク
                newLine(outputList); // ブランク

                return hasData;
            }

            private static List<List<String>> columnListToLines(final List<List<String>> columnList) {
                final List<List<String>> lines = new ArrayList<List<String>>();
                int maxLine = 0;
                for (final List<String> column : columnList) {
                    maxLine = Math.max(maxLine, column.size());
                }
                for (int li = 0; li < maxLine; li++) {
                    lines.add(line(columnList.size()));
                }
                for (int ci = 0; ci < columnList.size(); ci++) {
                    final List<String> column = columnList.get(ci);
                    for (int li = 0; li < column.size(); li++) {
                        lines.get(li).set(ci, column.get(li));
                    }
                }
                return lines;
            }

            private static List<List> setColumnList(final List<List> columnsList, final int column) {
                for (int i = columnsList.size(); i < column; i++) {
                    columnsList.add(new ArrayList<List>());
                }
                return columnsList;
            }

            private static List<List> joinColumnListArray(final List<List> columnsList, final List[] columnStudentFooterList) {
                for (int i = 0; i < columnStudentFooterList.length; i++) {
                    columnsList.get(i).addAll(columnStudentFooterList[i]);
                }
                return columnsList;
            }

            /**
             * ページ見出し・項目・欄外記述を印刷します。
             * @param ipdf
             * @param studentGroup
             */
            private void printHeader(final IPdf ipdf, final StudentGroup studentGroup) {
                final Param param = _param;
                ipdf.VrsOut("ymd1", param._now); // 作成日
                if (Param._isDemo) {
                    ipdf.VrsOut("DATE", "出欠集計範囲：" + param._attendTermKekka);  // 欠課の集計範囲
                } else {
                    ipdf.VrsOut("DATE", param._attendTermKekka);  // 欠課の集計範囲
                }
                if (!PATTERN3.equals(param._outputPattern) && !PATTERN4.equals(param._outputPattern) && !PATTERN5.equals(param._outputPattern)) {
                    if (Param._isDemo) {
                        ipdf.VrsOut("DATE2", "出欠の記録(" + param._attendTerm + ")");
                    } else {
                        ipdf.VrsOut("DATE2", param._attendTerm);
                    }
                }
                if (param._isPrintPerfect) {
                    ipdf.VrsOut("ITEM_NAME_CREDIT", "単位数(満点)");
                }

                ipdf.VrsOut(getFieldForData(ipdf, new String[] {"HR_TEACHER", "HR_TEACHER2"}, studentGroup._staffname, param), studentGroup._staffname);  //担任名
                ipdf.VrsOut("HR_NAME", studentGroup.getName(param));  //組名称
                if (!PATTERN5.equals(param._outputPattern)) {
                    ipdf.VrsOut("ITEM7", param._rankName + "順位");
                }
                if (PATTERN3.equals(param._outputPattern)) {
                    ipdf.VrsOut("ITEM", "学年・科平均");
                } else if (PATTERN4.equals(param._outputPattern)) {
                    if (isAlpPdf(ipdf)) {
                        ipdf.VrsOut("ITEM", param._rankName + "平均");
                    } else {
                        ipdf.VrsOut("ITEM", param._rankName);
                    }
                    ipdf.VrsOut("ITEM4", param._item4Name);
                    ipdf.VrsOut("ITEM5", param._item5Name);
                } else if (!PATTERN5.equals(param._outputPattern)) {
                    ipdf.VrsOut("ITEM4", param._item4Name);
                    ipdf.VrsOut("ITEM5", param._item5Name);
                    if (param._isSapporo && !param._testKindCd.startsWith("99")) {
                        ipdf.VrAttribute("ITEM5", "X=10000");
                    }
                    if (Param._isDemo) {
                        ipdf.VrsOut("ITEM8", param._rankName + "平均");
                    } else {
                        ipdf.VrsOut("ITEM8", param._rankName);
                    }
                }
                if (PATTERN1.equals(param._outputPattern)) {
                    if (param._isKomazawa) {
                        ipdf.VrsOut("ITEM4", "総合成績");
                        ipdf.VrsOut("ITEM9", "偏差値");
                    }
                    if (param._useKetten) {
                        if (!param._isKomazawa) {
                            ipdf.VrsOut("ITEM9", "欠点科目数");
                        }
                        ipdf.VrsOut("ITEM10", "欠点者数");
                    }
                    if (Param._isDemo) {
                        ipdf.VrsOut("ITEM11", "修得単位数");
                    }
                    ipdf.VrsOut("ITEM6", param._item1Name + "・" + param._item2Name);
                }

                // 一覧表枠外の文言
                if (!PATTERN3.equals(param._outputPattern) && !PATTERN4.equals(param._outputPattern) && !PATTERN5.equals(param._outputPattern)) {
                    if (Param._isDemo || param._isNaraken) {

                    } else {
                        ipdf.VrAttribute("NOTE1",  ATTRIBUTE_KEKKAOVER);
                    }
                    if (param._isNaraken) {
                        ipdf.VrsOut("NOTE1", "()");
                    } else {
                        ipdf.VrsOut("NOTE1", " ");
                    }
                    if (Param._isDemo) {
                        ipdf.VrsOut("NOTE2",  " ：欠課時数超過者" );
                    } else {
                        ipdf.VrsOut("NOTE2",  "：欠課時数超過者" );
                    }
                    if (param._useKetten) {
                        if (Param._isDemo) {

                        } else {
                            ipdf.VrAttribute("NOTE3",  ATTRIBUTE_KETTEN);
                        }
                        ipdf.VrsOut("NOTE3",  " " );
                        if (Param._isDemo) {
                            ipdf.VrsOut("NOTE4",  " ：欠点" );
                        } else {
                            ipdf.VrsOut("NOTE4",  "：欠点" );
                        }
                    }
                }

                ipdf.VrsOut("TITLE", param._nendo + "　" + param._title);
                ipdf.VrsOut("SUBTITLE", param._subtitle);
                if (param._isMieken) {
                    ipdf.VrsOut("NAME_TITLE", "名前");
                }

                for (int i = 1; i <= 8; i++) {
                    final String name1 = param._d055StampMap.get("0" + String.valueOf(i));
                    if (StringUtils.isBlank(name1)) {
                        continue;
                    }
                    final String field = getFieldForData(ipdf, new String[] {"JOB" + String.valueOf(i) + "_1", "JOB" + String.valueOf(i) + "_2"}, name1, param);
                    if (Param._isDemo) {
                        ipdf.VrsOutn(field, i, name1);
                    } else {
                        if (hasField(ipdf, "JOB_IMG" + String.valueOf(i), param)) {
                            if (null != param._ineiWakuPath) {
                                ipdf.VrImageOut("JOB_IMG" + String.valueOf(i), param._ineiWakuPath);
                            }
                        }
                        ipdf.VrsOut(field, name1);
                    }
                }
            }

            /**
             * 学級データの印字処理(学級平均) 右下
             * @param ipdf
             */
            private void printGroupInfo(final DB2UDB db2, final IPdf ipdf, final StudentGroup studentGroup) {
                final String _333333 = "333333";
                final String _555555 = "333333";
                final int col = -1;
                //学級合計
                String groupFlg = null;
                if (OUTPUT_RANK1.equals(_param._outputRank)) {
                    groupFlg = StudentGroup.AVG_FLG_HR;
                } else if (OUTPUT_RANK2.equals(_param._outputRank)) {
                    groupFlg = StudentGroup.AVG_FLG_GRADE;
                } else if (OUTPUT_RANK3.equals(_param._outputRank)) {
                    groupFlg = StudentGroup.AVG_FLG_COURSE;
                } else if (OUTPUT_RANK4.equals(_param._outputRank)) {
                    groupFlg = StudentGroup.AVG_FLG_MAJOR;
                }
                final String gavg = studentGroup.getRecordRankSdivAverage(groupFlg, _param.SUBCLASSCD999999, "AVG");
                final String gtotal = studentGroup.getRecordRankSdivAverage(groupFlg, _param.SUBCLASSCD999999, "TOTAL");
                final String avgHrTotal = studentGroup.getRecordRankSdivAverage(StudentGroup.AVG_FLG_HR, _param.SUBCLASSCD999999, "TOTAL");
                final String avgHrAverage = studentGroup.getRecordRankSdivAverage(StudentGroup.AVG_FLG_HR, _param.SUBCLASSCD999999, "AVG");
                final String avgHrCount = studentGroup.getRecordRankSdivAverage(StudentGroup.AVG_FLG_HR, _param.SUBCLASSCD999999, "COUNT");

                // 学級合計
                if (null != avgHrTotal) {
                    if (PRGID_KNJD615H.equals(_param._prgId)) {
                        svfsetString1(ipdf, "TOTAL1", _param._formMaxLine + 2, "", col, avgHrTotal);
                    } else if (PRGID_KNJD615P.equals(_param._prgId)) {
                        final int line = _param._formMaxLine + 3;
                        svfsetString1(ipdf, "TOTAL1", line, "", col, avgHrTotal);
                        svfsetString1(ipdf, "TOTAL33", line, "", col, studentGroup.getRecordRankSdivAverage(StudentGroup.AVG_FLG_HR, _333333, "TOTAL"));
                        svfsetString1(ipdf, "TOTAL55", line, "", col, studentGroup.getRecordRankSdivAverage(StudentGroup.AVG_FLG_HR, _555555, "TOTAL"));
                    } else if (PATTERN1.equals(_param._outputPattern)) {
                        svfsetString1(ipdf, "TOTAL1", _param._formMaxLine + 2, "", col, avgHrTotal);
                    } else if (PATTERN3.equals(_param._outputPattern)) {
                        svfsetString1(ipdf, "TOTAL1", _param._formMaxLine + 3, "", col, avgHrTotal);
                    } else if (PATTERN4.equals(_param._outputPattern)) {
                        svfsetString1(ipdf, "TOTAL1", _param._formMaxLine + 1, "", col, avgHrTotal);
                    }
                }
                // 学年合計
                if (null != gtotal) {
                    if (PATTERN1.equals(_param._outputPattern)) {
                        svfsetString1(ipdf, "TOTAL1", _param._formMaxLine + 3, "", col, gtotal);
                    }
                }
                //学級合計の母集団の生徒数
                if (PRGID_KNJD615P.equals(_param._prgId)) {
                    final int line = _param._formMaxLine + 1;
                    if (Integer.parseInt(defstr(avgHrCount, "0")) > 0) {
                        svfsetString1(ipdf, "TOTAL1", line, "", col, avgHrCount);
                    }
                    if (Integer.parseInt(defstr(studentGroup.getRecordRankSdivAverage(StudentGroup.AVG_FLG_HR, _333333, "COUNT"), "0")) > 0) {
                        svfsetString1(ipdf, "TOTAL33", line, "", col, studentGroup.getRecordRankSdivAverage(StudentGroup.AVG_FLG_HR, _333333, "COUNT"));
                    }
                    if (Integer.parseInt(defstr(studentGroup.getRecordRankSdivAverage(StudentGroup.AVG_FLG_HR, _555555, "COUNT"), "0")) > 0) {
                        svfsetString1(ipdf, "TOTAL55", line, "", col, studentGroup.getRecordRankSdivAverage(StudentGroup.AVG_FLG_HR, _555555, "COUNT"));
                    }
                } else if (PATTERN4.equals(_param._outputPattern)) {
                    if (Integer.parseInt(defstr(avgHrCount, "0")) > 0) {
                        svfsetString1(ipdf, "TOTAL1", _param._formMaxLine + 2, "", col, avgHrCount);
                    }
                }
                // 学級平均
                if (null != avgHrAverage) {
                    if (PRGID_KNJD615H.equals(_param._prgId)) {
                        svfsetString1(ipdf, "AVERAGE1", _param._formMaxLine + 2, "", col, avgHrAverage);
                    } else if (PRGID_KNJD615P.equals(_param._prgId)) {
                        final int line = _param._formMaxLine + 3;
                        svfsetString1(ipdf, "AVERAGE1", line, "", col, avgHrAverage);
                        svfsetString1(ipdf, "AVERAGE33", line, "", col, studentGroup.getRecordRankSdivAverage(StudentGroup.AVG_FLG_HR, _333333, "AVG"));
                        svfsetString1(ipdf, "AVERAGE55", line, "", col, studentGroup.getRecordRankSdivAverage(StudentGroup.AVG_FLG_HR, _555555, "AVG"));
                    } else if (PATTERN1.equals(_param._outputPattern)) {
                        if (_param._isSapporo && !_param._testKindCd.startsWith("99")) {
                        } else {
                            svfsetString1(ipdf, "AVERAGE1", _param._formMaxLine + 2, "", col, avgHrAverage);
                        }
                    } else if (PATTERN3.equals(_param._outputPattern)) {
                        svfsetString1(ipdf, "AVERAGE1", _param._formMaxLine + 3, "", col, avgHrAverage);
                    } else if (PATTERN4.equals(_param._outputPattern)) {
                        svfsetString1(ipdf, "TOTAL1", _param._formMaxLine + 3, "", col, avgHrAverage);
                    }
                }
                // 指定母集団平均
                if (null != gavg) {
                    if (PATTERN1.equals(_param._outputPattern)) {
                        if (_param._isSapporo && !_param._testKindCd.startsWith("99")) {
                        } else {
                            svfsetString1(ipdf, "AVERAGE1", _param._formMaxLine + 3, "", col, gavg);
                        }
                    } else if (PATTERN3.equals(_param._outputPattern)) {
                        // なし
                    } else if (PATTERN4.equals(_param._outputPattern)) {
                        svfsetString1(ipdf, "TOTAL1", _param._formMaxLine + 4, "", col, gavg);
                    }
                }
                //欠点者数
                final String failHrTotal = _param._useKetten ? StudentGroup.failTotal(_param, studentGroup._students) : "";
                if (null != failHrTotal) {
                    if (PRGID_KNJD615H.equals(_param._prgId)) {
                        svfsetString1(ipdf, "FAIL1", _param._formMaxLine + 4, "", col, failHrTotal); // 欠点者数
                    } else  if (PATTERN1.equals(_param._outputPattern)) {
                        svfsetString1(ipdf, "TOTAL1", _param._formMaxLine + 4, "", col, failHrTotal); // 欠点者数
                    } else if (PATTERN3.equals(_param._outputPattern)) {
                        svfsetString1(ipdf, "FAIL1", _param._formMaxLine + 5, "", col, failHrTotal); // 欠点者数
                    } else if (PATTERN4.equals(_param._outputPattern)) {
                        svfsetString1(ipdf, "TOTAL1", _param._formMaxLine + 5, "", col, failHrTotal); // 欠点者数
                    }
                }
                //最高点
                final String totalMax = StudentGroup.totalMax(studentGroup._students);
                if (null != totalMax) {
                    if (PRGID_KNJD615H.equals(_param._prgId)) {
                        svfsetString1(ipdf, "TOTAL1", _param._formMaxLine + 5, "", col, totalMax); // 最高点
                    } else if (PATTERN1.equals(_param._outputPattern)) {
                        svfsetString1(ipdf, "TOTAL1", _param._formMaxLine + 5, "", col, totalMax); // 最高点
                    } else if (PATTERN3.equals(_param._outputPattern)) {
                        // なし
                    } else if (PATTERN4.equals(_param._outputPattern)) {
                        // なし
                    }
                }
                //最低点
                final String totalMin = StudentGroup.totalMin(studentGroup._students);
                if (null != totalMin) {
                    if (PRGID_KNJD615H.equals(_param._prgId)) {
                        svfsetString1(ipdf, "TOTAL1", _param._formMaxLine + 6, "", col, totalMin); // 最低点
                    } else if (PATTERN1.equals(_param._outputPattern)) {
                        svfsetString1(ipdf, "TOTAL1", _param._formMaxLine + 6, "", col, totalMin); // 最低点
                    } else if (PATTERN3.equals(_param._outputPattern)) {
                        // なし
                    } else if (PATTERN4.equals(_param._outputPattern)) {
                        // なし
                    }
                }

                final String suspendMourning = StudentGroup.suspendMourningTotal(_param, studentGroup._students);
                final String kessekiTotal = StudentGroup.kessekiTotal(_param, studentGroup._students);
                final String lateTotal = StudentGroup.lateTotal(_param, studentGroup._students);
                final String earlyTotal = StudentGroup.earlyTotal(_param, studentGroup._students);

                // 出停忌引、欠席日数、遅刻回数、早退回数
                final int attendTotalLine = _param._formMaxLine + 1;
                if (PATTERN1.equals(_param._outputPattern)) {
                    if(!_param._isKomazawa) {
                        if (defstr(suspendMourning).length() > 3 && hasField(ipdf, "SUSPEND_TOTAL", _param)) {
                            ipdf.VrsOut("SUSPEND_TOTAL", suspendMourning);
                        } else {
                            ipdf.VrsOutn("SUSPEND1", attendTotalLine, suspendMourning);
                        }
                        if (defstr(kessekiTotal).length() > 4 && hasField(ipdf, "ABSENCE_TOTAL", _param)) {
                            ipdf.VrsOut("ABSENCE_TOTAL", kessekiTotal);
                        } else {
                            ipdf.VrsOutn("ABSENCE1", attendTotalLine, kessekiTotal);
                        }
                        if (defstr(lateTotal).length() > 3 && hasField(ipdf, "TOTAL_LATE_TOTAL", _param)) {
                            ipdf.VrsOut("TOTAL_LATE_TOTAL", lateTotal);
                        } else {
                            ipdf.VrsOutn("TOTAL_LATE1", attendTotalLine, lateTotal);
                        }
                        if (defstr(earlyTotal).length() > 3 && hasField(ipdf, "LEAVE_TOTAL", _param)) {
                            ipdf.VrsOut("LEAVE_TOTAL", earlyTotal);
                        } else {
                            ipdf.VrsOutn("LEAVE1", attendTotalLine, earlyTotal);
                        }
                    }
                } else if (PATTERN3.equals(_param._outputPattern)) {
                    if (defstr(suspendMourning).length() > 3 && hasField(ipdf, "SUSPEND_DAY_TOTAL", _param)) {
                        ipdf.VrsOut("SUSPEND_DAY_TOTAL", suspendMourning);
                    } else {
                        ipdf.VrsOutn("SUSPEND_DAY1", attendTotalLine, suspendMourning);
                    }
                    if (defstr(kessekiTotal).length() > 4 && hasField(ipdf, "ABSENCE_TOTAL", _param)) {
                        ipdf.VrsOut("ABSENCE_TOTAL", kessekiTotal);
                    } else {
                        ipdf.VrsOutn("ABSENCE1", attendTotalLine, kessekiTotal);
                    }
                    if (defstr(lateTotal).length() > 3 && hasField(ipdf, "LATE_TOTAL", _param)) {
                        ipdf.VrsOut("LATE_TOTAL", lateTotal);
                    } else {
                        ipdf.VrsOutn("LATE1", attendTotalLine, lateTotal);
                    }
                    if (defstr(earlyTotal).length() > 3 && hasField(ipdf, "EARLY_TOTAL", _param)) {
                        ipdf.VrsOut("EARLY_TOTAL", earlyTotal);
                    } else {
                        ipdf.VrsOutn("EARLY1", attendTotalLine, earlyTotal);
                    }
                }

                //出席率・欠席
                final BigDecimal shussekiRitsu = StudentGroup.shussekiRitsu(studentGroup._students);
                if (null != shussekiRitsu) {
                    if (!PATTERN3.equals(_param._outputPattern) && !PATTERN4.equals(_param._outputPattern)) {
                        ipdf.VrsOut("PER_ATTEND", DEC_FMT1.format(shussekiRitsu) + (Param._isDemo ? "%" : ""));
                    }
                }
                final BigDecimal kessekiRitsu = StudentGroup.kessekiRitsu(studentGroup._students);
                if (null != kessekiRitsu) {
                    if (!PATTERN3.equals(_param._outputPattern) && !PATTERN4.equals(_param._outputPattern)) {
                        ipdf.VrsOut("PER_ABSENCE", DEC_FMT1.format(kessekiRitsu.setScale(1,BigDecimal.ROUND_HALF_UP)) + (Param._isDemo ? "%" : ""));
                    }
                }

                if (PATTERN1.equals(_param._outputPattern) && _param._isKomazawa) {
                    Map resultMap = getScoreData(db2, ALL9, _param._grade, studentGroup._cd);
                    ipdf.VrsOutn("TOTAL1", 52, (String)resultMap.get("AVG"));       // 学級平均(年組)
                    ipdf.VrsOutn("TOTAL1", 54, (String)resultMap.get("COUNT"));     // 欠点者数(年組)
                    ipdf.VrsOutn("TOTAL1", 55, (String)resultMap.get("MAX_SCORE")); // 最高点(年組)
                    ipdf.VrsOutn("TOTAL1", 56, (String)resultMap.get("MIN_SCORE")); // 最低点(年組)
                    ipdf.VrsOutn("TOTAL1", 57, sishaGonyu2((String)resultMap.get("STDDEV"),0)); // 標準偏差(年組)

                    resultMap = getScoreData(db2, ALL9, _param._grade, "");
                    ipdf.VrsOutn("TOTAL1", 53, (String)resultMap.get("AVG"));       // 学級平均(学年)
                    ipdf.VrsOutn("DEVI1", 54, (String)resultMap.get("COUNT"));     // 欠点者数(学年)
                    ipdf.VrsOutn("DEVI1", 55, (String)resultMap.get("MAX_SCORE")); // 最高点(学年)
                    ipdf.VrsOutn("DEVI1", 56, (String)resultMap.get("MIN_SCORE")); // 最低点(学年)
                    ipdf.VrsOutn("DEVI1", 57, sishaGonyu2((String)resultMap.get("STDDEV"),0)); // 標準偏差(学年)
                }

                String printRemark = null;
                final int lines = 6;
                int keta = 20;
                String field = null;
                for (final boolean replaceNewLine : new boolean[] {false, true}) {
                    printRemark = null;
                    if ("1".equals(_param._useAttendSemesHrRemark) && PATTERN1.equals(_param._outputPattern)) {
                        for (final String remark1 : studentGroup._attendHrRemark) {
                            if (null == printRemark) {
                                printRemark = "";
                            } else {
                                printRemark += "\r\n";
                            }
                            if (replaceNewLine) {
                                printRemark += remark1.replace("\r\n", "\n").replace("\n", "");
                            } else {
                                printRemark += remark1;
                            }
                        }
                    }

                    //

                    log.info(" printRemark (" + replaceNewLine + ") = [" + printRemark + "\n]");

                    if (KNJ_EditKinsoku.getTokenList(printRemark, 20).size() <= lines) {
                        keta = 20;
                        field = "REMARK1";
                        break;
                    } else if (KNJ_EditKinsoku.getTokenList(printRemark, 30).size() <= lines) {
                        keta = 30;
                        field = "REMARK1_2";
                        break;
                    } else if (KNJ_EditKinsoku.getTokenList(printRemark, 40).size() <= lines) {
                        keta = 40;
                        field = "REMARK1_3";
                        break;
                    } else if (KNJ_EditKinsoku.getTokenList(printRemark, 80).size() <= lines) {
                        keta = 80;
                        field = "REMARK2_1";
                        break;
                    } else if (replaceNewLine) {
                        keta = 80;
                        field = "REMARK2_1";
                    }
                }
                final List<String> tokenList = KNJ_EditKinsoku.getTokenList(printRemark, keta);
                if ("REMARK2_1".equals(field)) {
                    final List<String> tokenListSrc = new ArrayList<String>(tokenList);
                    final List<String> tokenListTmp = new ArrayList<String>();
                    tokenList.clear();
                    for (int i = 0; i < tokenListSrc.size(); i++) {
                        final List<String> sub = KNJ_EditKinsoku.getTokenList(tokenListSrc.get(i), 40);
                        for (int j = 0; j < sub.size(); j++) {
                            tokenListTmp.add(sub.get(j) + StringUtils.repeat(" ", 40 - getMS932ByteLength(sub.get(j))));
                        }
                    }
                    for (int i = 0; i < tokenListTmp.size(); i+= 2) {
                        if (i + 1 < tokenListTmp.size()) {
                            tokenList.add(tokenListTmp.get(i) + tokenListTmp.get(i + 1));
                        } else {
                            tokenList.add(tokenListTmp.get(i));
                        }
                    }
                }
                for (int i = 0; i < tokenList.size(); i++) {
                    ipdf.VrsOutn(field, _param._formMaxLine + i + 1, tokenList.get(i));
                }
            }

            /**
             * 生徒別総合点・平均点・順位を印刷します。
             * @param ipdf
             */
            private void printStudentTotal(final DB2UDB db2, final IPdf ipdf, final int line, final Student student) {

                if (!PATTERN5.equals(_param._outputPattern)) {
                    if (null != student._scoreSum999999) {
                        ipdf.VrsOutn("TOTAL1", line, student._scoreSum999999);  //総合点

                        // 平均点
                        if (_param._isSapporo && !_param._testKindCd.startsWith("99")) {
                        } else {
                            final BigDecimal bd999 = new BigDecimal(999);
                            String kettenAttribute = null;
                            if (PRGID_KNJD615P.equals(_param._prgId) && _param._isKwansei) {
                                if (toBigDecimal(student._scoreAvg999999, bd999).setScale(0, BigDecimal.ROUND_HALF_UP).doubleValue() < 65) {
                                    kettenAttribute = "Paint=(13,70,2),Bold=1"; // 65点未満ピンク
                                }
                            }
                            if (null != kettenAttribute) {
                                ipdf.VrAttributen("AVERAGE1", line, kettenAttribute);
                            }

                            ipdf.VrsOutn("AVERAGE1", line, student._scoreAvg999999);

                            if (null != kettenAttribute) {
                                ipdf.VrAttributen("AVERAGE1", line, ATTRIBUTE_NORMAL);
                            }
                        }
                    }
                    //順位（学級）
                    if (1 <= student._classRank) {
                        ipdf.VrsOutn("CLASS_RANK1", line, String.valueOf(student._classRank));
                    }
                    //順位（学年orコース）
                    if (1 <= student._rank) {
                        ipdf.VrsOutn("RANK1", line, String.valueOf(student._rank));
                    }
                    //欠点科目数
                    if (_param._useKetten) {
                        if (0 < student.countFail(_param)) {
                            ipdf.VrsOutn("FAIL1", line, String.valueOf(student.countFail(_param)));
                        }
                    }
                    if (PATTERN1.equals(_param._outputPattern) && _param._isKomazawa) {
                        //偏差値
                        final String gradeDeviation = defstr(getRankSdiv(db2, "GRADE_DEVIATION", student._schregno));
                        ipdf.VrsOutn("DEVI1", line, String.valueOf(gradeDeviation));
                        //評定平均
                        final String val1 = defstr(getRankSdiv(db2, "SCORE", student._schregno), "0");
                        final String val2 = defstr(getAssessMark(db2, val1));
                        ipdf.VrsOutn("VALUE1", line, val1);
                        ipdf.VrsOutn("VALUE2", line, val2);
                    }
                    if (PRGID_KNJD615P.equals(_param._prgId)) {
                        ipdf.VrsOutn("TOTAL33", line, student._scoreSum333333);  //総合点
                        ipdf.VrsOutn("AVERAGE33", line, student._scoreAvg333333);  //平均点
                        ipdf.VrsOutn("TOTAL55", line, student._scoreSum555555);  //総合点
                        ipdf.VrsOutn("AVERAGE55", line, student._scoreAvg555555);  //平均点
                    }
                }
                if (!PRGID_KNJD615H.equals(_param._prgId) && !PATTERN4.equals(_param._outputPattern)) {
                    printAttendInfo(ipdf, _param, student._attendInfo, line);
                }
            }

            /**
             * 生徒の氏名・備考を印字
             * @param ipdf
             */
            private void printStudentName(final IPdf ipdf, final int line, final Student student) {
                ipdf.VrsOutn("NUMBER", line, student.getPrintAttendno());  // 出席番号
                // 学籍番号表記
                String nameNo = "1";
                String showName = "";
                try {
                    showName = _param._staffInfo.getStrEngOrJp(student._name, student._nameEng);
                } catch (Throwable t) {
                    showName = student._name;
                }
                if ("1".equals(_param._use_SchregNo_hyoji)) {
                    ipdf.VrsOutn("SCHREG_NO", line, student._schregno); // 学籍番号
                    nameNo = "2" + (getMS932ByteLength(showName) > 16 ? "_2" : "_1"); // 学籍番号表示用の氏名フィールド
                }
                if (PRGID_KNJD615H.equals(_param._prgId)) {
                    final String nameField = getMS932ByteLength(showName) > 16 ? "3_1" : getMS932ByteLength(showName) > 16 ? "2_1" : "1_1";
                    ipdf.VrsOutn("name" + nameField, line, showName);    // 氏名
                    ipdf.VrsOutn("SEX", line, student._sex); // 性別
                    ipdf.VrsOutn("DORMITORY1", line, student._ryou);
                    ipdf.VrsOutn("OVERSEA1", line, student._kaigai);
                    ipdf.VrsOutn("RECOMMEND1", line, student._suisen);
                    ipdf.VrsOutn("IB1", line, student._ib);
                    ipdf.VrsOutn("METHOD1", line, student._aHouShiki);
                } else if (PRGID_KNJD615P.equals(_param._prgId)) {
                    ipdf.VrsOutn("name1", line, showName);    // 氏名
                    ipdf.VrsOutn("SEX", line, student._sex); // 性別
                    ipdf.VrsOutn("COURSE", line, student._coursecodename);
                    if (_param._isKwansei) {
                        ipdf.VrsOutn("HR_NAME_NUMBER", line, defstr(student._hrName) + " " + defstr(student.getPrintAttendno(2)) + "番");
                    }
                } else if (PATTERN3.equals(_param._outputPattern)) {
                    if ("1".equals(_param._knjd615vNameKirikae)) {
                        final int ketaName = getMS932ByteLength(showName);
                        ipdf.VrsOutn("NAME" + nameNo + (ketaName <= 20 ? "" : ketaName <= 30 ? "_2" : "_3"), line, showName);
                    } else {
                        ipdf.VrsOutn("NAME" + nameNo, line, showName);  // 氏名
                    }
                } else if (Param._isDemo) {
                    ipdf.VrsOutn("name", line, showName);    // 氏名
                } else {
                    if (_param._isTokiwagi) {
//                        final String field = "NAME" + nameNo;
//                        final int fieldKeta;
//                        if ("name2_2".equals(field)) {
//                            fieldKeta = 20;
//                        } else if ("name2_1".equals(field)) {
//                            fieldKeta = 16;
//                        } else {
//                            fieldKeta = 20;
//                        }
//                        ipdf.VrsOutn(field, line, defstr(student._name) + StringUtils.repeat(" ", fieldKeta - namelen) + defstr(student._scholarshipName));    // 氏名
                        ipdf.VrsOutn("NAME" + nameNo, line, showName);    // 氏名
                    } else {
                        if ("1".equals(_param._knjd615vNameKirikae)) {
                            final int ketaName = getMS932ByteLength(showName);
                            // 氏名
                            ipdf.VrsOutn("NAME" + nameNo + (ketaName <= 20 ? "" : ketaName <= 30 ? "_2" : "_3"), line, showName);
                        } else {
                            ipdf.VrsOutn("NAME" + nameNo, line, showName);    // 氏名
                        }
                    }
                }
            }

            private void printRemark(final IPdf ipdf, final int line, final Student student) {
                final String remark = student.getRemark(_param);
                final int keta = getMS932ByteLength(remark);
                if (Param._isDemo) {
                    // PATTERN1.equals(_param._outputPattern)
                    if (keta > 40) {
                        final String[] tokens = KNJ_EditEdit.get_token(remark, 40, 2);
                        if (null != tokens) {
                            for (int i = 0; i < Math.min(2, tokens.length); i++) {
                                ipdf.VrsOutn("REMARK2_" + String.valueOf(i + 1), line, tokens[i]);  // 備考
                            }
                        }
                    } else {
                        ipdf.VrsOutn("REMARK" + (keta <= 20 ? "1" : keta <= 30 ? "1_2" : "1_3"), line, remark);  // 備考
                    }
                } else if (ipdf instanceof SvfPdf) {
                    //final SvfPdf svfPdf = (SvfPdf) ipdf;
                    String[] fields = null;
                    if (PATTERN1.equals(_param._outputPattern)) {
                        fields = new String[] {"REMARK", "REMARK1_2", "REMARK1_3", "REMARK2_1"};
                    } else if (PATTERN3.equals(_param._outputPattern)) {
                        fields = new String[] {"REMARK", "REMARK1_2", "REMARK1_3", "REMARK2_1"};
                    } else if (PATTERN4.equals(_param._outputPattern)){
                        fields = new String[] {"REMARK1", "REMARK2"};
                    } else if (PATTERN5.equals(_param._outputPattern)) {
                        fields = new String[] {"REMARK1", "REMARK1_2"};
                    }
                    if (null != fields) {
                        ipdf.VrsOutn(getFieldForData(ipdf, fields, remark, _param), line, remark);  // 備考
                    }
                } else if (PATTERN1.equals(_param._outputPattern)) {
                    ipdf.VrsOutn("REMARK" + (keta <= 20 ? "1" : keta <= 30 ? "1_2" : keta <= 40 ? "1_3" : "2_1"), line, remark);  // 備考
                } else if (PATTERN3.equals(_param._outputPattern)) {
                    ipdf.VrsOutn("REMARK" + (keta <= 20 ? "" : keta <= 30 ? "1_2" : "2_1"), line, remark);  // 備考
                } else if (PATTERN4.equals(_param._outputPattern)){
                    ipdf.VrsOutn("REMARK1", line, remark);  // 備考
                } else if (PATTERN5.equals(_param._outputPattern)) {
                    ipdf.VrsOutn("REMARK1" + (keta <= 70 ? "" : "_2"), line, remark);  // 備考
                }
            }
            private void printHyotei(final IPdf ipdf, final int line, final Student student) {
                final SchScoreCnt schScoreCnt = student._schScoreCnt;
                if (schScoreCnt != null) {
                    ipdf.VrsOutn("VAL5", line, String.valueOf(schScoreCnt._cntScore5));
                    ipdf.VrsOutn("VAL4", line, String.valueOf(schScoreCnt._cntScore4));
                    ipdf.VrsOutn("VAL3", line, String.valueOf(schScoreCnt._cntScore3));
                    ipdf.VrsOutn("VAL2", line, String.valueOf(schScoreCnt._cntScore2));
                    ipdf.VrsOutn("VAL1", line, String.valueOf(schScoreCnt._cntScore1));
                }
            }

            /**
             * 該当科目名および科目別成績等を印字する処理
             * @param ipdf
             * @param subclass
             * @param line：科目の列番号
             * @return
             */
            private void printSubclassHeader(
                    final IPdf ipdf,
                    final int col,
                    final SubClass subclass,
                    final boolean pat3isKariHyotei,
                    final ClassAbbvFieldSet abbvFieldSet,
                    final List<String> studentCourses
            ) {
                //教科名
                if (PATTERN5.equals(_param._outputPattern) || PATTERN4.equals(_param._outputPattern)) {
                    Form1.setRecordString(ipdf, "GRPCD", col, subclass._classcd);
                    Form1.setRecordString(ipdf, "course" + abbvFieldSet._fieldNumer, col, abbvFieldSet._setAbbv);
                } else {
                    Form1.setRecordString(ipdf, "course1", col, subclass._classabbv);
                }
                //科目名
                if (PATTERN4.equals(_param._outputPattern) || PATTERN5.equals(_param._outputPattern)) {
                    final String[] subclassfields = null != subclass._subclassname && subclass._subclassname.length() <= 7 ? new String[] {"SUBCLASS"} : new String[] {"SUBCLASS_1", "SUBCLASS_2"};
                    if (subclass._electdiv) {
                        for (int i = 0; i < subclassfields.length; i++) {
                            ipdf.VrAttribute(subclassfields[i], ATTRIBUTE_ELECTDIV);
                        }
                    }
                    if (null != subclass._subclassname && subclass._subclassname.length() <= 7) {
                        Form1.setRecordString(ipdf, subclassfields[0], col, subclass._subclassname);
                    } else {
                        final String[] token = new String[2];
                        token[0] = subclass._subclassname.substring(0, Math.min(subclass._subclassname.length(), 8));
                        token[1] = (subclass._subclassname.length () <= 8) ? "" : subclass._subclassname.substring(8, Math.min(subclass._subclassname.length(), 8 * 2));
                        if (null != token) {
                            for (int i = 0; i < token.length && i < subclassfields.length; i++) {
                                Form1.setRecordString(ipdf, subclassfields[i], col, token[i]);
                            }
                        }
                    }
                    if (subclass._electdiv) {
                        for (int i = 0; i < subclassfields.length; i++) {
                            ipdf.VrAttribute(subclassfields[i], ATTRIBUTE_ELECTDIV);
                        }
                    }
                } else {
                    final String subclassfield = PATTERN3.equals(_param._outputPattern) ? "SUBCLASS" : "subject1";
                    if (subclass._electdiv) { ipdf.VrAttribute(subclassfield, ATTRIBUTE_ELECTDIV); }
                    String abbv = subclass._subclassabbv;
                    if (_param._isHirogaku) {
                        if (_param.getSubclassMst(subclass.keySubclasscd()).isMoto(_param, studentCourses)) {
                            abbv = kakko(abbv);
                        }
                    }
                    Form1.setRecordString(ipdf, subclassfield, col, abbv);
                    if (subclass._electdiv) { ipdf.VrAttribute(subclassfield, ATTRIBUTE_NORMAL); }
                }
                //単位数
                final String creditStr = subclass.getPrintCredit(_param);
                final int creditStrLen = getMS932ByteLength(creditStr);
                final int creditFieldKeta = getFieldKeta(ipdf, "credit1", _param);
                final String creditField = (creditFieldKeta < creditStrLen && creditFieldKeta < getFieldKeta(ipdf, "credit1_2", _param)) ? "credit1_2" :"credit1";
                Form1.setRecordString(ipdf, creditField, col, creditStr);
                Form1.setRecordString(ipdf, fieldClassTeacher(ipdf, _param, subclass._staffname), col, subclass._staffname);
                if (PATTERN1.equals(_param._outputPattern)) {
                    //授業時数
                    Form1.setRecordString(ipdf, "lesson1", col, subclass.getJisu());
                }
                //項目名
                if (!PATTERN4.equals(_param._outputPattern) && !PATTERN5.equals(_param._outputPattern)) {
                    final int mojisu = getMS932ByteLength(_param._item1Name);
                    final int field1Keta = getFieldKeta(ipdf, "ITEM1", _param);
                    final String fieldname;
                    if (field1Keta < mojisu && field1Keta < getFieldKeta(ipdf, "ITEM1_2", _param)) {
                        fieldname = "ITEM1_2";
                    } else {
                        fieldname = "ITEM1";
                    }
                    Form1.setRecordString(ipdf, fieldname, col, _param._item1Name);
                }
                if (PATTERN3.equals(_param._outputPattern)) {
                    Form1.setRecordString(ipdf, "ITEM2_2", col, pat3isKariHyotei ? "仮評定" : "評定");
                    Form1.setRecordString(ipdf, "ITEM3", col, "欠課");
                    if (PRGID_KNJD615H.equals(_param._prgId)) {
                        Form1.setRecordString(ipdf, "ITEM2", col, "偏差値");
                    }
                } else if (!PATTERN4.equals(_param._outputPattern) && !PATTERN5.equals(_param._outputPattern)) {
                    Form1.setRecordString(ipdf, "ITEM2", col, _param._item2Name);
                }
            }

            /**
             * パターン3（仮評定付き）で科目が仮評定を表示するならtrue、そうでなければfalse
             * @param param
             * @param subclass 科目
             * @param studentList 生徒のリスト
             * @return パターン3（仮評定付き）で科目が仮評定を表示するならtrue、そうでなければfalse
             */
            private static boolean pat3SubclassIsKariHyotei(final Param param, final SubClass subclass, final List<Student> studentList) {
                if (!"9".equals(param._semester)) {
                    return true; // 学年末以外は仮評定を表示
                }
                final boolean isD008 = param._d008Namecd2List.contains(subclass._classcd) || "90".equals(subclass._classcd);
                int valueCount = 0;
                int provFlgCount = 0;
                for (final Student student : studentList) {
                    final ScoreDetail detail = student._scoreDetails.get(subclass._subclasscd);
                    if (null != detail) {
                        if (null != detail._karihyotei) {
                            valueCount += 1;
                        }
                        if ("1".equals(detail._provFlg)) {
                            provFlgCount += 1;
                        }
                    }
                }
                // 評定がないもしくは仮評定フラグが1なら仮評定を表示する。そうでなければ本評定を印字する。
                final boolean isKari = valueCount == 0 && !isD008 || provFlgCount > 0;
                if (param._isOutputDebug) {
                    log.info(" kari " + subclass.keySubclasscd() + " = " + isKari + "　( prov/value = " + provFlgCount + "/" + valueCount + ")");
                }
                return isKari;
            }

            public static String fieldClassTeacher(final IPdf ipdf, final Param param, final String staffname) {
                String fieldClassTeacher = null;
                if ((PATTERN4.equals(param._outputPattern) || PATTERN5.equals(param._outputPattern)) && getMS932ByteLength(staffname) > 4) {
                    fieldClassTeacher  = "CLASS_TEACHER2";
                } else if (PATTERN3.equals(param._outputPattern) && getMS932ByteLength(staffname) > 14 && (null == ipdf || hasField(ipdf, "CLASS_TEACHER3_1", param))) {
                    fieldClassTeacher  = "CLASS_TEACHER3_1";
                } else if (PATTERN3.equals(param._outputPattern) && getMS932ByteLength(staffname) > 10 && (null == ipdf || hasField(ipdf, "CLASS_TEACHER2", param))) {
                    fieldClassTeacher  = "CLASS_TEACHER2";
                } else if (!PATTERN1.equals(param._outputPattern)) {
                    fieldClassTeacher  = "CLASS_TEACHER";
                }
                return fieldClassTeacher;
            }

            public void printSubclassStat(final IPdf ipdf, final int col, final SubClass subclass) {
                if (PATTERN5.equals(_param._outputPattern)) {
                    Form1.setRecordString(ipdf, "APPOINT_TIMES", col, subclass.getJisu());
                }

                final boolean useD001 = _param._d065Name1List.contains(subclass.keySubclasscd());
                if (useD001) {
                    return;
                }

                if (PRGID_KNJD615H.equals(_param._prgId)) {
                    Form1.setRecordString(ipdf, "TOTAL_SUBCLASS1", col, subclass._scoretotal);
                    Form1.setRecordString(ipdf, "TOTAL_SUBCLASS2", col, subclass._scoreCount);
                    Form1.setRecordString(ipdf, "TOTAL_SUBCLASS3", col, subclass._scoreHrAverage);
                    Form1.setRecordString(ipdf, "TOTAL_SUBCLASS4", col, subclass._recordAverageSdivDatAvg);
                    Form1.setRecordString(ipdf, "TOTAL_SUBCLASS5", col, subclass._scoreFailCnt);
                    Form1.setRecordString(ipdf, "TOTAL_SUBCLASS6", col, subclass.getJisu());
                    Form1.setRecordString(ipdf, "MAX_SCORE", col, subclass._scoreMax);
                    Form1.setRecordString(ipdf, "MIN_SCORE", col, subclass._scoreMin);
                } else if (PRGID_KNJD615P.equals(_param._prgId)) {
                    if (_param._isKwansei && OUTPUT_ORDER_2_SEISEKIJUN.equals(_param._outputOrder)) {
                        // 出力しない
                    } else {
                        Form1.setRecordString(ipdf, "TOTAL_SUBCLASS", col, subclass._scoretotal);
                        Form1.setRecordString(ipdf, "TOTAL_SUBCLASS2", col, subclass._scoreCount);
                        Form1.setRecordString(ipdf, "TOTAL_SUBCLASS3", col, subclass._scoreHrAverage);
                    }
                } else if (PATTERN3.equals(_param._outputPattern)) {
                    Form1.setRecordString(ipdf, "TOTAL_SUBCLASS1", col, subclass._scoretotal);
                    Form1.setRecordString(ipdf, "TOTAL_SUBCLASS2", col, subclass._scoreCount);
                    Form1.setRecordString(ipdf, "TOTAL_SUBCLASS3", col, subclass._scoreHrAverage);
                    Form1.setRecordString(ipdf, "TOTAL_SUBCLASS4", col, subclass._recordAverageSdivDatAvg);
                    if (_param._useKetten) {
                        Form1.setRecordString(ipdf, "TOTAL_SUBCLASS5", col, subclass._scoreFailCnt);
                    }
                    Form1.setRecordString(ipdf, "TOTAL_SUBCLASS6", col, subclass.getJisu());
                } else if (PATTERN4.equals(_param._outputPattern)) {
                    Form1.setRecordString(ipdf, "TOTAL_SUBCLASS", col, subclass._scoretotal);
                    Form1.setRecordString(ipdf, "NUM", col, subclass._scoreCount);
                    Form1.setRecordString(ipdf, "AVE_CLASS2", col, subclass._scoreHrAverage);
                    Form1.setRecordString(ipdf, "AVE_SUBCLASS2", col, subclass._recordAverageSdivDatAvg);
                    if (_param._useKetten) {
                        Form1.setRecordString(ipdf, "FAIL_STD", col, subclass._scoreFailCnt);
                    }
                } else if (PATTERN1.equals(_param._outputPattern)) {
                    Form1.setRecordString(ipdf, "AVE_CLASS", col, subclass._scoreHrAverage);
                    Form1.setRecordString(ipdf, "AVE_SUBCLASS", col, subclass._recordAverageSdivDatAvg);
                    if (!StringUtils.isBlank(subclass._scoretotal) || !StringUtils.isBlank(subclass._scoreCount)) {
                        Form1.setRecordString(ipdf, "TOTAL_SUBCLASS", col, defstr(subclass._scoretotal) + "/" + defstr(subclass._scoreCount));
                    }
                    Form1.setRecordString(ipdf, "MAX_SCORE", col, subclass._scoreMax);
                    Form1.setRecordString(ipdf, "MIN_SCORE", col, subclass._scoreMin);
                    if (_param._useKetten) {
                        Form1.setRecordString(ipdf, "FAIL_STD", col, subclass._scoreFailCnt);
                    }
                    if (_param._isKomazawa) {
                        Form1.setRecordString(ipdf, "STD_DEVI", col, subclass._standardDeviation);
                    }
                }
            }

            /**
             * 生徒別科目別素点・評定・欠課時数等を印刷します。
             * @param ipdf
             * @param line 生徒の行番
             */
            private void printScoreKekka(
                    final IPdf ipdf,
                    final String schregno,
                    final ScoreDetail detail,
                    final int line,
                    final int col,
                    final SubClass subclass,
                    final boolean pat3isKariHyotei
            ) {
                final String sline = String.valueOf(line);
                final boolean isPrintScore = !PATTERN5.equals(_param._outputPattern);
                final boolean isPrintKariHyotei = PATTERN3.equals(_param._outputPattern);
                final boolean isFieldKurikaeshi = PRGID_KNJD615P.equals(_param._prgId);
                if (isPrintScore) {
                    final List<String> attributes = new ArrayList<String>();
                    String printScore = "";
                    if ("2".equals(_param._outputValue)) {
                        printScore = defstr(detail.getPrintDeviation(_param, 1));
                    } else {
                        final Tuple<String, Tuple<String, List<String>>> printScoreAndMarkAndAttribute = getPrintScoreAndMarkAndAttribute(_param, detail);
                        printScore = printScoreAndMarkAndAttribute._first;
                        final String mark = printScoreAndMarkAndAttribute._second._first;
                        attributes.addAll(printScoreAndMarkAndAttribute._second._second);
                        if (detail._isNarakenKekkaOver) {
                            log.info(" " + sline + " : " + schregno + ", score " + printScore);
                        }
                        printScore = mark + defstr(printScore);
                    }

                    final String scoreField = PATTERN3.equals(_param._outputPattern) ? "GRADING1_" : "SCORE";
                    final boolean kettenAmikake;
                    if (PRGID_KNJD615P.equals(_param._prgId) && _param._isKwansei) {
                        String kettenAttribute = null;
                        if (toInt(detail._score, 999) < 55) {
                            kettenAttribute = "Paint=(13,70,2),Bold=1"; // 55点未満ピンク
                        }
                        kettenAmikake = null != kettenAttribute;
                        if (null != kettenAttribute) {
                            attributes.add(kettenAttribute);
                        }
                    } else {
                        kettenAmikake = _param._useKetten && ScoreDetail.isFail(_param, detail);
                        if (kettenAmikake) {
                            attributes.add(ATTRIBUTE_KETTEN);
                        }
                    }
                    final String field = getFieldForData(ipdf, new String[] {scoreField + sline,  scoreField + sline + "_2"}, printScore, _param);
                    final String attribute = mkString(attributes, ",");
                    if (!StringUtils.isEmpty(attribute)) {
                        if (isFieldKurikaeshi) {
                            ipdf.VrAttributen(scoreField, line, attribute);
                        } else {
                            ipdf.VrAttribute(field, attribute);
                        }
                    }
                    if (isFieldKurikaeshi) {
                        ipdf.VrsOutn(scoreField, line, printScore);
                    } else if (Param._isDemo) {
                        Form1.setRecordString(ipdf, field, col, line, printScore);
                    } else {
                        Form1.setRecordString(ipdf, field, col, printScore);
                    }
                    if (kettenAmikake) {
                        if (isFieldKurikaeshi) {
                            ipdf.VrAttributen(scoreField, line, ATTRIBUTE_NORMAL);
                        } else {
                            ipdf.VrAttribute(field, ATTRIBUTE_NORMAL);
                        }
                    }
                    if (isPrintKariHyotei) {
                        // 評定
                        final boolean isPrint = !SEMEALL.equals(_param._semester) || SEMEALL.equals(_param._semester) && (pat3isKariHyotei && "1".equals(detail._provFlg) || !pat3isKariHyotei && null == detail._provFlg); // 学年末以外は対象。学年末は「仮評定ならPROV_FLG=1が対象。そうでなければPROV_FLG=NULLが対象。」
                        if (isPrint && null != detail._karihyotei) {
                            Form1.setRecordString(ipdf, "VALUE1_" + sline, col, detail._karihyotei);
                        }
                    }
                    if (PRGID_KNJD615H.equals(_param._prgId)) {
                        //偏差値
                        Form1.setRecordString(ipdf, "GRADING2_" + sline, col, detail.getPrintDeviation(_param, 0));
                    }
                }

                // 欠課
                final boolean isPrintKekka = _param._isPrintAttend;
                final boolean isOutputJisu = PATTERN1.equals(_param._outputPattern) && _param._isOutputStudentJisu && null != detail._jisu && !subclass.getJisu().equals(detail._jisu.toString());
                if (isPrintKekka && null != detail._absent || isOutputJisu) {
                    final String val;
                    if (isOutputJisu) {
                        val = (null == detail._absent ? "0" : String.valueOf(detail._absent.intValue())) + "/" + detail._jisu.toString();
                    } else {
                        val = getKekkaString(_param, detail._absent);
                    }
                    final boolean isLongField = _param._definecode.absent_cov == 3 || _param._definecode.absent_cov == 4;
                    final String field;
                    if (PRGID_KNJD615H.equals(_param._prgId)) {
                        field = "ABSENT2_" + sline;
                    } else if (PATTERN3.equals(_param._outputPattern)) {
                        if (isLongField) {
                            field = "ABSENT" + sline + "_2";
                        } else {
                            field = "ABSENT" + sline + "_1";
                        }
                    } else if (PATTERN5.equals(_param._outputPattern)) {
                        if (isLongField) {
                            field = getFieldForData(ipdf, new String[] {                 "SCORE" + sline + "_2", "SCORE" + sline + "_3"}, val, _param);
                        } else {
                            field = getFieldForData(ipdf, new String[] {"SCORE" + sline, "SCORE" + sline + "_2", "SCORE" + sline + "_3"}, val, _param);
                        }
                    } else if (PATTERN1.equals(_param._outputPattern)) {
                        if (isOutputJisu) {
                            field = getFieldForData(ipdf, new String[] {"kekka2_" + sline, "kekka3_" + sline}, val, _param);
                        } else if (isLongField) {
                            field = "kekka2_" + sline;
                        } else {
                            field = "kekka" + sline;
                        }
                        if (_param._isNaraken) {
                            SvfField svfField = getSvfField(ipdf, field, _param);
                            if (null != svfField) {
                                final String sCharSize = (String) svfField.getAttributeMap().get(SvfField.AttributeSize);
                                if (NumberUtils.isNumber(sCharSize)) {
                                    ipdf.VrAttribute(field, "Size=" + new BigDecimal(Double.parseDouble(sCharSize) - 1).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
                                }
                            }
                        }
                    } else {
                        if (isLongField) {
                            field = "kekka2_" + sline;
                        } else {
                            field = "kekka" + sline;
                        }
                    }
                    if (detail._isKekkaOver) { ipdf.VrAttribute(field, ATTRIBUTE_KEKKAOVER); }
                    if (isOutputJisu) {
                        Form1.setRecordString(ipdf, field, col, val);
                    } else if (Param._isDemo) {
                        Form1.setRecordString(ipdf, field, col, line, val);
                    } else {
                        Form1.setRecordString(ipdf, field, col, val);
                    }
                    if (detail._isKekkaOver) { ipdf.VrAttribute(field, ATTRIBUTE_NORMAL); }
                }
            }

            public static int setRecordString(IPdf ipdf, String field, int gyo, int retsu, String data) {
                if (Param._isDemo) {
                    return ipdf.VrsOutn(field, gyo, data);
                }
                return ipdf.setRecordString(field, gyo, data);
            }

            public static int setRecordString(IPdf ipdf, String field, int gyo, String data) {
                if (Param._isDemo) {
                    return ipdf.VrsOutn(field, gyo, data);
                }
                return ipdf.setRecordString(field, gyo, data);
            }

            //年組、学年ごとに集計した値を取得
            private Map getScoreData(final DB2UDB db2, final String subclassCd, final String grade, final String gradeHrclass) {
                final StringBuffer stb = new StringBuffer();
                final String testkindcd = _param._testKindCd.substring(0, 2);
                final String testitemcd = _param._testKindCd.substring(2, 4);
                final String scoreDiv = _param._testKindCd.substring(4);
                stb.append("WITH ");

                //対象生徒の表 クラスの生徒
                stb.append(" SCHNO_A AS(");
                stb.append("     SELECT  W1.SCHREGNO,W1.YEAR,W1.SEMESTER ");
                stb.append("            ,W1.GRADE, W1.HR_CLASS, W1.COURSECD, W1.MAJORCD, W1.COURSECODE ");
                stb.append("            , 0 AS LEAVE ");
                stb.append("     FROM    SCHREG_REGD_DAT W1 ");
                stb.append("     INNER JOIN V_SEMESTER_GRADE_MST W2 ON W2.YEAR = W1.YEAR AND W2.SEMESTER = W1.SEMESTER AND W2.GRADE = W1.GRADE ");

                stb.append("     WHERE   W1.YEAR = '" + _param._year + "' ");
                if (SEMEALL.equals(_param._semester)) {
                    stb.append("     AND W1.SEMESTER = '" + _param._semeFlg + "' ");
                } else {
                    stb.append("     AND W1.SEMESTER = '" + _param._semester + "' ");
                    stb.append("     AND W1.SCHREGNO NOT IN (SELECT S1.SCHREGNO FROM  SCHREG_BASE_MST S1");
                    stb.append("                    WHERE S1.GRD_DIV IN ('2','3','6','7') AND S1.GRD_DATE < W2.SDATE) ");
                }
                stb.append("         AND W1.GRADE    = '"+ grade +"' ");
                if(!"".equals(gradeHrclass)) {
                    stb.append("         AND W1.HR_CLASS = '"+ gradeHrclass.substring(2) +"' ");
                }
                stb.append(") ");
                //対象講座の表
                stb.append(",CHAIR_A AS(");
                stb.append("     SELECT W1.SCHREGNO, W2.CHAIRCD, ");
                stb.append("            W2.CLASSCD, W2.SCHOOL_KIND, W2.CURRICULUM_CD, W2.SUBCLASSCD AS SUBCLASSCD_ONLY, ");
                stb.append("            W2.SEMESTER, W1.APPDATE, W1.APPENDDATE ");
                stb.append("     FROM   CHAIR_STD_DAT W1 ");
                stb.append("     INNER JOIN CHAIR_DAT W2 ON W2.YEAR = W1.YEAR ");
                stb.append("         AND W2.SEMESTER = W1.SEMESTER ");
                stb.append("         AND W2.CHAIRCD = W1.CHAIRCD ");
                stb.append("     INNER JOIN SEMESTER_MST SEME ON SEME.YEAR = W1.YEAR ");
                stb.append("         AND SEME.SEMESTER = W1.SEMESTER ");
                if ("1".equals(_param._printSubclassLastChairStd)) {
                    stb.append("         AND SEME.EDATE = W1.APPENDDATE ");
                }
                stb.append("     WHERE  W1.YEAR = '" + _param._year + "' ");
                stb.append("        AND W1.SEMESTER <= '" + _param._semester + "' ");
                stb.append("        AND EXISTS( ");
                stb.append("            SELECT 'X' ");
                stb.append("            FROM SCHNO_A W3 ");
                stb.append("                 LEFT JOIN SCHREG_REGD_GDAT WG ON WG.YEAR = W3.YEAR AND WG.GRADE = W3.GRADE ");
                stb.append("            WHERE W3.SCHREGNO = W1.SCHREGNO");
                stb.append("              AND WG.SCHOOL_KIND = W2.SCHOOL_KIND ");
                stb.append("        )");
                stb.append("        AND W2.SUBCLASSCD = '"+ subclassCd +"' ");
                stb.append("     )");
                stb.append(" , REL_COUNT AS (");
                stb.append("   SELECT SUBCLASSCD");
                stb.append("     , CLASSCD ");
                stb.append("     , SCHOOL_KIND ");
                stb.append("     , CURRICULUM_CD ");
                stb.append("     , COUNT(*) AS COUNT ");
                stb.append("   FROM RELATIVEASSESS_MST ");
                stb.append("   WHERE GRADE = '" + _param._grade + "' AND ASSESSCD = '3' ");
                stb.append("   GROUP BY SUBCLASSCD");
                stb.append("     , CLASSCD ");
                stb.append("     , SCHOOL_KIND ");
                stb.append("     , CURRICULUM_CD ");
                stb.append(" ) ");
                //成績データの表（通常科目）
                stb.append(",RECORD_REC AS(");
                stb.append("    SELECT  W3.SCHREGNO, ");
                stb.append("            W3.CLASSCD, W3.SCHOOL_KIND, W3.CURRICULUM_CD, W3.SUBCLASSCD ");
                stb.append("     , W3.SCORE ");
                stb.append("     , W3.CLASS_DEVIATION ");
                stb.append("     , W3.GRADE_DEVIATION ");
                stb.append("     , W3.COURSE_DEVIATION ");
                stb.append("     , W3.MAJOR_DEVIATION ");
                stb.append("     , CASE WHEN VALUE(T2.COUNT, 0) > 0 THEN ");
                stb.append("          (SELECT MAX(L3.ASSESSLEVEL) ");
                stb.append("           FROM RELATIVEASSESS_MST L3 ");
                stb.append("           WHERE L3.GRADE = '" + _param._grade + "' AND L3.ASSESSCD = '3' ");
                stb.append("             AND W3.SCORE BETWEEN L3.ASSESSLOW AND L3.ASSESSHIGH  ");
                stb.append("             AND L3.SUBCLASSCD = W3.SUBCLASSCD ");
                stb.append("             AND L3.CLASSCD = W3.CLASSCD ");
                stb.append("             AND L3.SCHOOL_KIND = W3.SCHOOL_KIND ");
                stb.append("             AND L3.CURRICULUM_CD = W3.CURRICULUM_CD ");
                stb.append("          ) ELSE ");
                if ("1".equals(_param._useAssessCourseMst)) {
                    stb.append("          VALUE((SELECT MAX(L3.ASSESSLEVEL) ");
                    stb.append("           FROM ASSESS_COURSE_MST L3 ");
                    stb.append("           WHERE L3.ASSESSCD = '3' ");
                    stb.append("             AND L3.COURSECD = SCH.COURSECD  ");
                    stb.append("             AND L3.MAJORCD = SCH.MAJORCD  ");
                    stb.append("             AND L3.COURSECODE = SCH.COURSECODE  ");
                    stb.append("             AND W3.SCORE BETWEEN L3.ASSESSLOW AND L3.ASSESSHIGH  ");
                    stb.append("           ),  ");
                    stb.append("           (SELECT MAX(L3.ASSESSLEVEL) ");
                    stb.append("           FROM ASSESS_MST L3 ");
                    stb.append("           WHERE L3.ASSESSCD = '3' ");
                    stb.append("             AND W3.SCORE BETWEEN L3.ASSESSLOW AND L3.ASSESSHIGH  ");
                    stb.append("          )) ");
                } else {
                    stb.append("          (SELECT MAX(L3.ASSESSLEVEL) ");
                    stb.append("           FROM ASSESS_MST L3 ");
                    stb.append("           WHERE L3.ASSESSCD = '3' ");
                    stb.append("             AND W3.SCORE BETWEEN L3.ASSESSLOW AND L3.ASSESSHIGH  ");
                    stb.append("          ) ");
                }
                stb.append("         END AS ASSESS_LEVEL ");
                stb.append("    FROM    RECORD_RANK_SDIV_DAT W3 ");
                stb.append("            INNER JOIN SCHNO_A SCH ON W3.SCHREGNO = SCH.SCHREGNO ");
                stb.append("                   AND SCH.LEAVE = 0 ");
                stb.append("            LEFT JOIN REL_COUNT T2 ");
                stb.append("                   ON T2.SUBCLASSCD = W3.SUBCLASSCD ");
                stb.append("                  AND T2.CLASSCD = W3.CLASSCD ");
                stb.append("                  AND T2.SCHOOL_KIND = W3.SCHOOL_KIND ");
                stb.append("                  AND T2.CURRICULUM_CD = W3.CURRICULUM_CD ");
                stb.append("    WHERE   W3.YEAR = '" + _param._year + "' ");
                stb.append("            AND W3.SEMESTER = '" + _param._semester + "' ");
                stb.append("            AND W3.TESTKINDCD = '" + testkindcd + "' ");
                stb.append("            AND W3.TESTITEMCD = '" + testitemcd + "' ");
                stb.append("            AND W3.SCORE_DIV = '" + scoreDiv + "' ");
                stb.append("            AND W3.SUBCLASSCD = '"+ subclassCd +"' ");
                stb.append("     ) ");
                stb.append(" ,CHAIR_A2 AS ( ");
                stb.append("     SELECT  W2.SCHREGNO, W2.CLASSCD, W2.SCHOOL_KIND, W2.CURRICULUM_CD, W2.SUBCLASSCD_ONLY ");
                stb.append("     FROM    CHAIR_A W2");
                if (!SEMEALL.equals(_param._semester)) {
                    stb.append(" WHERE   W2.SEMESTER = '" + _param._semester + "'");
                }
                stb.append("     GROUP BY W2.SCHREGNO, W2.CLASSCD, W2.SCHOOL_KIND, W2.CURRICULUM_CD, W2.SUBCLASSCD_ONLY ");
                stb.append(" ) ");
                stb.append(",RECORD_GROUP AS( ");
                stb.append("     SELECT ");
                stb.append("              '"+_param._year+"' AS YEAR, ");
                stb.append("              '"+_param._semester+"' AS SEMESTER, ");
                stb.append("              L2.TESTKINDCD, L2.TESTITEMCD, L2.SCORE_DIV, T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD_ONLY AS SUBCLASSCD ");
                stb.append("            , AVG(L2.SCORE) AS AVG ");
                stb.append("            , MAX(L2.SCORE) AS MAX_SCORE ");
                stb.append("            , MIN(L2.SCORE) AS MIN_SCORE ");
                if (_param._testKindCd != null && _param._testKindCd.endsWith("09") || _param._is5dankai) {
                    if (!_param._kettenshaSuuNiKessaShaWoFukumenai) {
                        stb.append("            , COUNT(CASE WHEN RSD.VALUE_DI = '*' THEN 1 ELSE 0 END) AS COUNT ");
                    } else {
                        stb.append("            , COUNT(CASE WHEN L2.SCORE = '1' THEN 1 ELSE 0 END) AS COUNT ");
                    }
                } else {
                    if (!_param._kettenshaSuuNiKessaShaWoFukumenai) {
                        stb.append("            , COUNT(CASE WHEN RSD.VALUE_DI = '*' THEN 1 ELSE 0 END) AS COUNT ");
                    } else {
                        stb.append("            , COUNT(CASE WHEN PERF.PASS_SCORE > 0 ");
                        stb.append("                         THEN CASE WHEN T3.ASSESS_LEVEL = '1'      THEN 1 ELSE 0 END ");
                        stb.append("                         ELSE CASE WHEN L2.SCORE < PERF.PASS_SCORE THEN 1 ELSE 0 END ");
                        stb.append("                    END) AS COUNT ");
                    }
                }
                stb.append("     FROM CHAIR_A2 T1 ");
                stb.append("          INNER JOIN SCHNO_A SCH ");
                stb.append("                  ON SCH.SCHREGNO = T1.SCHREGNO ");
                stb.append("                 AND SCH.LEAVE   = 0 ");
                stb.append("          LEFT JOIN RECORD_RANK_SDIV_DAT L2 ");
                stb.append("                 ON L2.YEAR          = '" + _param._year + "' ");
                stb.append("                AND L2.SEMESTER      = '" + _param._semester+ "' ");
                stb.append("                AND L2.TESTKINDCD    = '" + testkindcd + "' ");
                stb.append("                AND L2.TESTITEMCD    = '" + testitemcd + "' ");
                stb.append("                AND L2.SCORE_DIV     = '" + scoreDiv + "' ");
                stb.append("                AND L2.CLASSCD       = T1.CLASSCD ");
                stb.append("                AND L2.SCHOOL_KIND   = T1.SCHOOL_KIND ");
                stb.append("                AND L2.CURRICULUM_CD = T1.CURRICULUM_CD ");
                stb.append("                AND L2.SUBCLASSCD    = T1.SUBCLASSCD_ONLY ");
                stb.append("                AND L2.SCHREGNO      = T1.SCHREGNO ");
                stb.append("          LEFT JOIN RECORD_SCORE_DAT RSD ");
                stb.append("                 ON RSD.YEAR          = '" + _param._year + "' ");
                stb.append("                AND RSD.SEMESTER      = '" + _param._semester + "' ");
                stb.append("                AND RSD.TESTKINDCD    = '" + testkindcd + "' ");
                stb.append("                AND RSD.TESTITEMCD    = '" + testitemcd + "' ");
                stb.append("                AND RSD.SCORE_DIV     = '" + scoreDiv + "' ");
                stb.append("                AND RSD.CLASSCD       = T1.CLASSCD ");
                stb.append("                AND RSD.SCHOOL_KIND   = T1.SCHOOL_KIND ");
                stb.append("                AND RSD.CURRICULUM_CD = T1.CURRICULUM_CD ");
                stb.append("                AND RSD.SUBCLASSCD    = T1.SUBCLASSCD_ONLY ");
                stb.append("                AND RSD.SCHREGNO      = T1.SCHREGNO ");
                stb.append("          LEFT JOIN PERFECT_RECORD_DAT PERF ");
                stb.append("                 ON PERF.YEAR          = '" + _param._year + "' ");
                stb.append("                AND PERF.SEMESTER      = '" + _param._semester + "' ");
                stb.append("                AND PERF.TESTKINDCD    = '" + testkindcd + "' ");
                stb.append("                AND PERF.TESTITEMCD    = '" + testitemcd + "' ");
                stb.append("                AND PERF.CLASSCD       = T1.CLASSCD ");
                stb.append("                AND PERF.SCHOOL_KIND   = T1.SCHOOL_KIND ");
                stb.append("                AND PERF.CURRICULUM_CD = T1.CURRICULUM_CD ");
                stb.append("                AND PERF.SUBCLASSCD    = T1.SUBCLASSCD_ONLY ");
                stb.append("                AND PERF.GRADE = CASE WHEN DIV = '01' THEN '00' ELSE SCH.GRADE END ");
                stb.append("                AND (     PERF.DIV        IN ('01','02') ");
                stb.append("                      AND PERF.COURSECD   = '0' ");
                stb.append("                      AND PERF.MAJORCD    = '000' ");
                stb.append("                      AND PERF.COURSECODE = '0000' ");
                stb.append("                      OR ");
                stb.append("                          PERF.DIV NOT    IN ('01','02')  ");
                stb.append("                      AND PERF.COURSECD   = SCH.COURSECD ");
                stb.append("                      AND PERF.MAJORCD    = SCH.MAJORCD ");
                stb.append("                      AND PERF.COURSECODE = SCH.COURSECODE ");
                stb.append("                      AND PERF.COURSECD   = SCH.COURSECD ");
                stb.append("                      AND PERF.MAJORCD    = SCH.MAJORCD ");
                stb.append("                      AND PERF.COURSECODE = SCH.COURSECODE ");
                stb.append("                    ) ");
                stb.append("          LEFT JOIN RECORD_REC T3 ");
                stb.append("                   ON T3.CLASSCD       = T1.CLASSCD ");
                stb.append("                  AND T3.SCHOOL_KIND   = T1.SCHOOL_KIND ");
                stb.append("                  AND T3.CURRICULUM_CD = T1.CURRICULUM_CD ");
                stb.append("                  AND T3.SUBCLASSCD    = T1.SUBCLASSCD_ONLY ");
                stb.append("                  AND T3.SCHREGNO      = T1.SCHREGNO");
                stb.append("     GROUP BY ");
                stb.append("              L2.TESTKINDCD, L2.TESTITEMCD, L2.SCORE_DIV, T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD_ONLY ");
                stb.append(" ) ");
                //メイン表
                stb.append(" SELECT  T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD_ONLY AS SUBCLASSCD");
                stb.append("        ,RGRP.AVG ");
                stb.append("        ,RGRP.COUNT ");
                stb.append("        ,RGRP.MAX_SCORE ");
                stb.append("        ,RGRP.MIN_SCORE ");
                stb.append("        ,RAD.STDDEV ");
                //対象生徒・講座の表
                stb.append(" FROM CHAIR_A2 T1 ");
                stb.append(" INNER JOIN RECORD_GROUP RGRP ");
                stb.append("                 ON RGRP.YEAR          = '" + _param._year + "' ");
                stb.append("                AND RGRP.SEMESTER      = '" + _param._semester + "' ");
                stb.append("                AND RGRP.TESTKINDCD    = '" + testkindcd + "' ");
                stb.append("                AND RGRP.TESTITEMCD    = '" + testitemcd + "' ");
                stb.append("                AND RGRP.SCORE_DIV     = '" + scoreDiv + "' ");
                stb.append("                AND RGRP.CLASSCD       = T1.CLASSCD ");
                stb.append("                AND RGRP.SCHOOL_KIND   = T1.SCHOOL_KIND ");
                stb.append("                AND RGRP.CURRICULUM_CD = T1.CURRICULUM_CD ");
                stb.append("                AND RGRP.SUBCLASSCD    = T1.SUBCLASSCD_ONLY ");
                stb.append(" LEFT JOIN SUBCLASS_DETAIL_DAT SDET ");
                stb.append("        ON SDET.YEAR          = '" + _param._year + "' ");
                stb.append("       AND SDET.CLASSCD       = T1.CLASSCD ");
                stb.append("       AND SDET.SCHOOL_KIND   = T1.SCHOOL_KIND ");
                stb.append("       AND SDET.CURRICULUM_CD = T1.CURRICULUM_CD ");
                stb.append("       AND SDET.SUBCLASSCD    = T1.SUBCLASSCD_ONLY ");
                stb.append("       AND SDET.SUBCLASS_SEQ  = '012' ");
                stb.append(" LEFT JOIN RECORD_AVERAGE_SDIV_DAT RAD ");
                stb.append("        ON RAD.YEAR          = '" + _param._year + "' ");
                stb.append("       AND RAD.SEMESTER      = '" + _param._semester + "' ");
                stb.append("       AND RAD.TESTKINDCD    = '" + testkindcd + "' ");
                stb.append("       AND RAD.TESTITEMCD    = '" + testitemcd + "' ");
                stb.append("       AND RAD.SCORE_DIV     = '" + scoreDiv + "' ");
                stb.append("       AND RAD.CLASSCD       = T1.CLASSCD ");
                stb.append("       AND RAD.SCHOOL_KIND   = T1.SCHOOL_KIND ");
                stb.append("       AND RAD.CURRICULUM_CD = T1.CURRICULUM_CD ");
                stb.append("       AND RAD.SUBCLASSCD    = T1.SUBCLASSCD_ONLY ");
                if(!"".equals(gradeHrclass)) {
                    stb.append("       AND RAD.AVG_DIV       = '2' ");
                    stb.append("       AND RAD.GRADE         = '"+ grade +"' ");
                    stb.append("       AND RAD.HR_CLASS      = '"+ gradeHrclass.substring(2) +"' ");
                } else {
                    stb.append("       AND RAD.AVG_DIV       = '1' ");
                    stb.append("       AND RAD.GRADE         = '"+ grade +"' ");
                    stb.append("       AND RAD.HR_CLASS      = '000' ");
                }
                stb.append("       AND RAD.COURSECD || RAD.MAJORCD || RAD.COURSECODE = '00000000' ");
                if (null != _param._takeSemes && !SEMEALL.equals(_param._takeSemes)) {
                    stb.append(" WHERE ");
                    stb.append("     ((SDET.SUBCLASS_REMARK" + _param._takeSemes + " = '1' ");
                    if (_param._takeSemes.equals(_param._knjSchoolMst._semesterDiv)) {
                        stb.append("   OR  SDET.SUBCLASS_REMARK1 IS NULL AND SDET.SUBCLASS_REMARK2 IS NULL AND SDET.SUBCLASS_REMARK3 IS NULL ");
                    }
                    stb.append("      ) ");
                    if (_param._takeSemes.equals(_param._knjSchoolMst._semesterDiv)) {
                        stb.append("   OR SDET.SUBCLASSCD IS NULL ");
                    }
                    stb.append("     ) ");
                }
                stb.append(" ORDER BY T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD_ONLY ");
                final String sql =  stb.toString();

                PreparedStatement ps = null;
                ResultSet rs = null;
                Map resultMap = new HashMap() ;

                try {
                    log.debug(sql);
                    ps = db2.prepareStatement(sql);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        resultMap.put("AVG", defstr(rs.getString("AVG"),"0"));
                        resultMap.put("COUNT", defstr(rs.getString("COUNT"),"0"));
                        resultMap.put("MAX_SCORE", defstr(rs.getString("MAX_SCORE"),"0"));
                        resultMap.put("MIN_SCORE", defstr(rs.getString("MIN_SCORE"),"0"));
                        resultMap.put("STDDEV", defstr(rs.getString("STDDEV"),"0"));
                    }
                } catch (SQLException ex) {
                    log.debug("Exception:", ex);
                } finally {
                    DbUtils.closeQuietly(null, ps, rs);
                    db2.commit();
                }

                return resultMap;
            }

            private String getShukkoTeisu(final DB2UDB db2, final String year, final String semester, final String grade, final String coursecd, final String majorcd) {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT SUM(LESSON) AS LESSON ");
                stb.append("   FROM ATTEND_LESSON_MST ");
                stb.append("  WHERE ");
                stb.append("        YEAR     = '"+ year +"' ");
                stb.append("    AND SEMESTER = '"+ semester +"' ");
                stb.append("    AND GRADE    = '"+ grade +"' ");
                stb.append("    AND COURSECD = '"+ coursecd +"' ");
                stb.append("    AND MAJORCD  = '"+ majorcd +"' ");
                stb.append(" GROUP BY YEAR,SEMESTER,GRADE,COURSECD,MAJORCD ");
                return KnjDbUtils.getOne(KnjDbUtils.query(db2, stb.toString()));
            }

            private String getRankSdiv(final DB2UDB db2, final String field, final String schregno) {
                final String testkindcd = _param._testKindCd.substring(0, 2);
                final String testitemcd = _param._testKindCd.substring(2, 4);
                final String scoreDiv = _param._testKindCd.substring(4);
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT "+field+" ");
                stb.append("   FROM RECORD_RANK_SDIV_DAT ");
                stb.append("  WHERE  ");
                stb.append("        YEAR       = '"+ _param._year +"' ");
                stb.append("    AND SEMESTER   = '"+ _param._semester +"' ");
                stb.append("    AND TESTKINDCD = '"+ testkindcd +"' ");
                stb.append("    AND TESTITEMCD = '"+ testitemcd +"' ");
                stb.append("    AND SCORE_DIV  = '"+ scoreDiv +"' ");
                stb.append("    AND SUBCLASSCD = '"+ ALL9 +"' ");
                stb.append("    AND SCHREGNO   = '"+ schregno +"' ");
                return KnjDbUtils.getOne(KnjDbUtils.query(db2, stb.toString()));
            }

            private String getAssessMark(final DB2UDB db2, final String score) {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ASSESSMARK  ");
                stb.append("   FROM ASSESS_MST");
                stb.append("  WHERE ASSESSCD = '4'");
                stb.append("    AND "+ score +" BETWEEN ASSESSLOW AND ASSESSHIGH");
                return KnjDbUtils.getOne(KnjDbUtils.query(db2, stb.toString()));
            }
        }
    }

    private static class TestItem {

        public String _testitemname;
        public String _sidouinputinf;
        public String _sidouinput;
        public String toString() {
            return "TestItem(" + _testitemname + ", sidouInput=" + _sidouinput + ", sidouInputInf=" + _sidouinputinf + ")";
        }
    }

    private static class SubclassMst implements Comparable<SubclassMst> {
        final String _subclasscd;
        final String _classabbv;
        final String _classname;
        final String _subclassabbv;
        final String _subclassname;
        public SubclassMst(final String subclasscd, final String classabbv, final String classname, final String subclassabbv, final String subclassname) {
            _subclasscd = subclasscd;
            _classabbv = classabbv;
            _classname = classname;
            _subclassabbv = subclassabbv;
            _subclassname = subclassname;
        }
        public int compareTo(final SubclassMst m) {
            return _subclasscd.compareTo(m._subclasscd);
        }

        /**
         *
         * @param param
         * @param courses コースのリスト
         * @return コースのリストの1つでも合併元コースならTrue
         */
        public boolean isSaki(final Param param, final Collection<String> courses) {
            final Collection<String> replaceCourse = getMappedTreeSet(param._combinedCourseListMap, this);
            return replaceCourse.contains("ALL") || hasIntersection(replaceCourse, courses);
        }

        /**
         *
         * @param param
         * @param courses コースのリスト
         * @return コースのリストの1つでも合併先コースならTrue
         */
        public boolean isMoto(final Param param, final Collection<String> courses) {
            final Collection<String> replaceCourse = getMappedTreeSet(param._attendCourseListMap, this);
            return replaceCourse.contains("ALL") || hasIntersection(replaceCourse, courses);
        }
        public static boolean hasIntersection(final Collection<String> courses1, final Collection<String> courses2) {
            for (final String c1 : courses1) {
                if (courses2.contains(c1)) {
                    return true;
                }
            }
            return false;
        }

        public String toString() {
            return "SubclassMst(" + _subclasscd + ":" + _subclassname + ")";
        }
    }

    private static class SchScoreCnt {
        int _cntScore5;
        int _cntScore4;
        int _cntScore3;
        int _cntScore2;
        int _cntScore1;
    }

    protected Param createParam(final HttpServletRequest request, final DB2UDB db2) {
        log.fatal("$Revision: 76403 $ $Date: 2020-09-03 20:18:11 +0900 (木, 03 9 2020) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        Param param = new Param(request, db2);
        return param;
    }

    protected static class Tuple<K, V> implements Comparable<Tuple<K, V>> {
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

    protected static class Param {
        /** 年度 */
        final String _year;
        /** 学期 */
        final String _semester;
        /** LOG-IN時の学期（現在学期）*/
        final String _semeFlg;
        final boolean _isLastSemester;

        /** 学年 */
        final String _grade;
        final String[] _paramClassSelected;
        final String[] _classSelected;
        private boolean _isPrintGroupGrade = false;
        private boolean _isPrintGroupCourse = false;
        private boolean _isPrintGroupHr = false;

        /** 出欠集計日付 */
        final String _sdate;
        final String _date;
        final String _testKindCd;
        final String _scoreDiv;
        final String _schoolKind;
        final String _useSchool_KindField;
        final String SCHOOLCD;
        final String SCHOOLKIND;

        final String _dataSelChk_Ryousei;
        final String _dataSelChk_Suisen;
        final String _dataSelChk_Kaigai;
        final String _dataSelChk_IBCourse;
        final String _dataSelChk_AType;

        /** 総合順位出力 1:学級 2:学年 3:コース 4:学科 */
        final String _outputRank;
        /** 順位の基準点 1:総合点 2:平均点 */
        final String _outputKijun;
        /** フォーム 1:科目固定型 2:科目変動型 3:成績と出欠の記録 4:欠課時数と出欠の記録 5:仮評定と出欠の記録 */
        final String _outputPattern;
        /** 出力順 1:出席番号順 2:学級順位順 */
        final String _outputOrder;
        /** 欠点プロパティ 1,2,設定無し(1,2以外) */
        final String _checkKettenDiv;
        /** 起動元のプログラムＩＤ */
        final String _prgId;
        /** 成績優良者評定平均の基準値===>KNJD615：未使用 */
        final Float _assess;
        /** 空行を詰めて印字 */
        final String _notEmptyLine;

        final String _useAssessCourseMst;
        final String _cmd;

        /** フォーム生徒行数 */
        int _formMaxLine;

        /** 科目数 */
        int _formMaxColumn;
        int _form2MaxColumn; // 右側集計欄のないフォーム
        int _form3MaxColumn; // PATTERN1で利用する右側集計欄があり、通常よりも出力する科目数が多いフォーム
        final String _formname;
        String _formname2; // 右側集計欄のないフォーム
        String _formname3; // PATTERN1で利用する右側集計欄があり、通常よりも出力する科目数が多いフォーム
        private String[] _recordField;

        /** "総合的な学習の時間"を表示しない */
        final boolean _notOutputSougou;
        /** "総合的な学習の時間"を表示しない */
        final boolean _useSlumpSdivDatSlump;
        /** 欠点者数に欠査者を含めない */
        final boolean _kettenshaSuuNiKessaShaWoFukumenai;
        /** 備考欄出力（出欠備考を出力） */
        final boolean _outputBiko;
        /** 生徒の時数を印字する */
        final boolean _isOutputStudentJisu;
        /** 1:全て/2:学期から/3:年間まとめ */
        final String _bikoKind;
        final boolean _isOutputCoursePage;
        final String _printSubclassLastChairStd;
        /** 出力順位開始 */
        final int _rankStart;
        /** 出力順位終了 */
        final int _rankEnd;
        final String _z010;
        final String _takeSemes;
        final String _printKekka0;
        final String _documentroot;
        final String _attendTerm;
        final String _attendTermKekka;
        final String _useAttendSemesHrRemark;
        final String _useSubclassWeightingCourseDat;
        final String _now;
        final boolean _hasATTEND_BATCH_INPUT_HDAT;
        final boolean _hasATTEND_SEMES_REMARK_HR_DAT;

        private String _yearDateS;
        private String _semesterName;
        private TestItem _testItem;

        private static final String FROM_TO_MARK = "\uFF5E";

        final KNJDefineSchool _definecode;  //各学校における定数等設定のクラス

        /** 端数計算共通メソッド引数 */
        final Map _attendParamMap;

        /** 教育課程コードを使用するか */
        final String _useCurriculumcd;

        /** 氏名欄に学籍番号の表示/非表示 1:表示 それ以外:非表示 */
        final String _use_SchregNo_hyoji;

        final String _knjd615vPrintNullRemark;

        private KNJSchoolMst _knjSchoolMst;

        final boolean _isKumamoto;
        final boolean _isKyoto;
        final boolean _isSapporo;
        final boolean _isMieken;
        final boolean _isTokiwagi;
        final boolean _isHagoromo;
        final boolean _isMeikei;
        final boolean _isChiyodaKudan;
        final boolean _isNaraken;
        final boolean _isYamamura;
        final boolean _isKudan;
        final boolean _isKomazawa;
        final boolean _isNichidaiNikou; // 日大二校
        final boolean _isShimaneken; // 島根県
        final boolean _isHirogaku; // 広島国際
        final boolean _isKwansei; // 関西学院
        final String _nendo;
        final boolean _useKetten;
        final boolean _is5dankai;
        final boolean _isOutputDebug;
        final boolean _isOutputDebugQuery;
        final boolean _isOutputDebugKetten;
        final boolean _isOutputDebugFormCreate;

        private String _rankName;
        private String _avgDiv;
        final String _d054Namecd2Max;
        final String _sidouHyoji;
        final Map<String, String> _d055StampMap;
        final List<String> _d065Name1List;
        final Map<String, String> _d001Name1Map;
        final List<String> _d079Name1List;
        final List<String> _d008Namecd2List;
        private Map<String, SubclassMst> _subclassMst;
        private Map<SubclassMst, Set<String>> _combinedCourseListMap;
        private Map<SubclassMst, Set<String>> _attendCourseListMap;
        private boolean _isPrintSakiKamoku;
        private boolean _isNoPrintMoto;

        private final String SUBCLASSCD999999;

        final String _title;
        final String _subtitle;
        private String _item1Name;  // 明細項目名
        final String _item2Name;  // 明細項目名
        final String _item4Name;  // 総合点欄項目名
        final String _item5Name;  // 平均点欄項目名
        final String _form2Item4Name;  // 平均点欄項目名
        final String _form2Item5Name;  // 平均点欄項目名
        private boolean _creditDrop;
        private boolean _isGakunenMatu; // 学年末はTrueを戻します。
        private boolean _hasCompCredit; // 履修単位数/修得単位数なし
        private int _failValue; // 欠点 100段階：30未満 5段階： 2未満
        final String _knjd615vPrintPerfect;
        final boolean _isPrintPerfect;
        final Map<String, PreparedStatement> _psMap = new HashMap<String, PreparedStatement>();
        final Map<String, String> _d053Name1Map;
        private String _currentform;
        private Map<String, Map<String, SvfField>> _formFieldInfoMap = new HashMap();
        private List<String> _svfformModifyKeys = Collections.emptyList();
        private List<String> _formFieldInfoLog = new ArrayList<String>();
        final boolean _isPrintAttend;
        final boolean _knjd615vNotPrintTransInfo;

        final String _ineiWakuPath;

        final String _printreport;
        final String _logindate;
        final String _gdatgradename;

        //日本語・英語切り替え表示用
        final String _staffCd;
        StaffInfo _staffInfo = null;

        private boolean _isHibinyuryoku = false;
        final Map<String, String> _vSchoolMst;

        // 島根県
        private String _keikokutenKubun; // 警告店区分 1:講座 2:類型平均
        private String _ruikeiheikinBunsi; // 類型平均分子　（警告店区分 2:類型平均のみ）
        private String _ruikeiheikinBunbo; // 類型平均分母　（警告店区分 2:類型平均のみ）

        private Set<String> _logOnce = new HashSet<String>();
        private Map<String, File> _createdFiles = new HashMap<String, File>();
        private final Map<String, String> _elapsed = new TreeMap<String, String>();

        private String _knjd615vSelectBikoTermType;
        private String _bikoTermType;

        final String _groupDiv; // (プロパティ設定時のみ。設定なしの場合、1:クラスとして処理) 1:クラス 2:学年 3:コース
        final String _outputValue; // (プロパティ設定時のみ。設定なしの場合、1:SCOREとして処理) 1:指定考査のSCORE 2:偏差値

        final String _knjd615vNameKirikae;

        /**
         * 1なら「授業時数」欄にはMLESSON(出席すべき授業時数）ではなくLESSON(授業時数)を表示
         */
        final String _knjd615vJugyoJisuLesson;

        /**
         * 指定年度の時間割数
         */
        final int _schChrDatCount;

        final String _use_school_Detail_009;

        /**
         * @deprecated 宮部さん作成のKNJD615V_0.frm使用デモ用。デモがすんだらカット
         */
        protected static boolean _isDemo = false;

        Param(final HttpServletRequest request, final DB2UDB db2) {

            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _semeFlg = request.getParameter("SEME_FLG");
            _grade = request.getParameter("GRADE");
            _groupDiv = request.getParameter("GROUP_DIV");
            _paramClassSelected = request.getParameterValues("CLASS_SELECTED");
            _outputOrder = defstr(request.getParameter("OUTPUT_ORDER"), "1");
            _prgId = request.getParameter("PRGID");

            _z010 = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' "));
            log.info(" z010 = " + _z010);
            _isKumamoto = "kumamoto".equals(_z010);
            _isKyoto = "kyoto".equals(_z010);
            _isSapporo = "sapporo".equals(_z010);
            _isMieken = "mieken".equals(_z010);
            _isTokiwagi = "tokiwagi".equals(_z010);
            _isHagoromo = "hagoromo".equals(_z010);
            _isMeikei = "meikei".equals(_z010);
            _isChiyodaKudan = "chiyoda".equals(_z010);
            _isNaraken = "naraken".equals(_z010);
            _isYamamura = "yamamura".equals(_z010);
            _isKudan = "chiyoda".equals(_z010);
            _isKomazawa = "koma".equals(_z010);
            _isNichidaiNikou = "nichi-ni".equals(_z010);
            _isShimaneken = "shimaneken".equals(_z010);
            _isHirogaku = "hirogaku".equals(_z010);
            _isKwansei = "kwansei".equals(_z010);

            if ("2".equals(_groupDiv) || PRGID_KNJD615P.equals(_prgId) && OUTPUT_ORDER_2_SEISEKIJUN.equals(_outputOrder)) {
                _classSelected = new String[] {_grade};
                _isPrintGroupGrade = true;
            } else {
                if ("3".equals(_groupDiv)) {
                    _isPrintGroupCourse = true;
                } else {
                    _isPrintGroupHr = true;
                }
                _classSelected = _paramClassSelected;
            }
            _outputValue = request.getParameter("OUTPUT_VALUE");
            boolean hasSDate = false;
            for (final Enumeration<String> e = request.getParameterNames(); e.hasMoreElements();) {
                final String parameterName = e.nextElement();
                if ("SDATE".equals(parameterName)) {
                    hasSDate = true;
                    break;
                }
            }
            _sdate = hasSDate ? KNJ_EditDate.H_Format_Haifun(request.getParameter("SDATE")) : KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT SDATE FROM SEMESTER_MST WHERE YEAR = '" + _year + "' AND SEMESTER = '1' "));

            final String[] outputDebug = StringUtils.split(getDbPrginfoProperties(db2, "outputDebug"));
            _isOutputDebug = ArrayUtils.contains(outputDebug, "1");
            _isOutputDebugQuery = ArrayUtils.contains(outputDebug, "query");
            _isOutputDebugKetten = ArrayUtils.contains(outputDebug, "ketten");
            _isOutputDebugFormCreate = ArrayUtils.contains(outputDebug, "formCreate");
            log.info(" isOutputDebug = " + ArrayUtils.toString(outputDebug));

            _date = KNJ_EditDate.H_Format_Haifun(request.getParameter("DATE"));
            _testKindCd = request.getParameter("TESTKINDCD");  //テスト・成績種別
            _scoreDiv = _testKindCd.substring(4);
            _outputRank = request.getParameter("OUTPUT_RANK");
            _creditDrop = (request.getParameter("OUTPUT4") != null);
            _documentroot = request.getParameter("DOCUMENTROOT");
            _notOutputSougou = "1".equals(request.getParameter("OUTPUT5"));
            _useSlumpSdivDatSlump = "1".equals(request.getParameter("OUTPUT6"));
            _kettenshaSuuNiKessaShaWoFukumenai = "1".equals(request.getParameter("OUTPUT7"));
            _outputBiko = "1".equals(request.getParameter("OUTPUT_BIKO"));
            _bikoKind = request.getParameter("BIKO_KIND");
            _outputPattern = _isDemo ? PATTERN1 : StringUtils.isBlank(request.getParameter("OUTPUT_PATERN")) ? PATTERN1 : request.getParameter("OUTPUT_PATERN");
            _assess = (request.getParameter("ASSESS") != null) ? new Float(request.getParameter("ASSESS1")) : new Float(4.3);
            _takeSemes = PATTERN2.equals(_outputPattern) ? request.getParameter("TAKESEMES") : null;
            _printKekka0 = request.getParameter("PRINT_KEKKA0");
            _notEmptyLine = request.getParameter("NOT_EMPTY_LINE");
            _useAssessCourseMst = request.getParameter("useAssessCourseMst");
            _cmd = request.getParameter("cmd");
            _staffCd = request.getParameter("PRINT_LOG_STAFFCD");
            try {
                _staffInfo = new StaffInfo(db2, _staffCd);
            } catch (Throwable e) {
                log.warn(" StaffInfo ");
            }

            _dataSelChk_Ryousei  = defstr(request.getParameter("DATA_SELECT1"));
            _dataSelChk_Suisen   = defstr(request.getParameter("DATA_SELECT2"));
            _dataSelChk_Kaigai   = defstr(request.getParameter("DATA_SELECT3"));
            _dataSelChk_IBCourse = defstr(request.getParameter("DATA_SELECT4"));
            _dataSelChk_AType    = defstr(request.getParameter("DATA_SELECT5"));

            if ("1".equals(_takeSemes)) {
                SUBCLASSCD999999 = "99999A";
            } else if ("2".equals(_takeSemes)) {
                SUBCLASSCD999999 = "99999B";
            } else {
                SUBCLASSCD999999 = "999999";
            }
            _recordField = new String[] {};
            if (PRGID_KNJD615H.equals(_prgId)) {
                _formname = "KNJD615H.frm";
                _formMaxLine = 50;
                _formMaxColumn = 19;
            } else if (PRGID_KNJD615P.equals(_prgId)) {
                if (_isKwansei) {
                    _formname = "KNJD615P_KWANSEI.frm";
                    _formMaxLine = 50;
                    _formMaxColumn = 20;
                } else {
                    _formname = "KNJD615P.frm";
                    _formMaxLine = 50;
                    _formMaxColumn = 15;
                }
            } else if (PATTERN2.equals(_outputPattern)) {
                _formname = "KNJD615V_2.frm";
                _formMaxLine = 25;
                _formMaxColumn = 19;
            } else if (PATTERN3.equals(_outputPattern)) {
                _formname = "KNJD615V_3.frm";
                _formMaxLine = 45;
                _formMaxColumn = 17;
            } else if (PATTERN4.equals(_outputPattern)) {
                _formname = "KNJD615V_4.frm";
                _formMaxLine = 45;
                _formMaxColumn = 38;
            } else if (PATTERN5.equals(_outputPattern)) {
                _formname = "KNJD615V_5.frm";
                _formMaxLine = 45;
                _formMaxColumn = 37;
            } else if (PATTERN1.equals(_outputPattern) && _isKomazawa) {
                _formname = "KNJD615V_1_4.frm";
                _formMaxLine = 50;
                _formMaxColumn = 20;
            } else { // PATTERN1.equals(_outputPattern) || "KNJD615L".equals(request.getParameter("PRGID"))
                if (_isDemo) {
                    _formname = "KNJD615V_0.frm";
                } else {
                    _formname = "KNJD615V_1.frm";
                }
                _formname2 = "KNJD615V_1_2.frm";
                _formname3 = "KNJD615V_1_3.frm";
                _formMaxLine = 45;
                _formMaxColumn = 20;
                _form2MaxColumn = 32;
                _form3MaxColumn = 24;
            }
            _hasATTEND_SEMES_REMARK_HR_DAT = KnjDbUtils.setTableColumnCheck(db2, "ATTEND_SEMES_REMARK_HR_DAT", null);
            _hasATTEND_BATCH_INPUT_HDAT = KnjDbUtils.setTableColumnCheck(db2, "ATTEND_BATCH_INPUT_HDAT", null);
            _outputKijun = request.getParameter("OUTPUT_KIJUN");
            _checkKettenDiv = request.getParameter("checkKettenDiv");
            _useCurriculumcd = KnjDbUtils.setTableColumnCheck(db2, "SUBCLASS_MST", "CURRICULUM_CD") ? "1" : "";
            _use_SchregNo_hyoji = request.getParameter("use_SchregNo_hyoji");
            _knjd615vPrintNullRemark = request.getParameter("knjd615vPrintNullRemark");
            _knjd615vPrintPerfect = request.getParameter("knjd615vPrintPerfect");
            _isOutputStudentJisu = "1".equals(request.getParameter("OUTPUT_STUDENT_JISU"));
            _useAttendSemesHrRemark = request.getParameter("useAttendSemesHrRemark");
            _useSubclassWeightingCourseDat = request.getParameter("useSubclassWeightingCourseDat");
            _d053Name1Map = KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, " SELECT NAMECD2, NAME1 FROM NAME_MST WHERE NAMECD1 = 'D053' "), "NAMECD2", "NAME1");
            _printreport = request.getParameter("PRINT_REPORT");
            _logindate = defstr(request.getParameter("LOGIN_DATE"));
            _knjd615vSelectBikoTermType = defstr(request.getParameter("knjd615vSelectBikoTermType"));
            _bikoTermType = defstr(request.getParameter("BIKO_TERM_TYPE"));

            _knjd615vNameKirikae = defstr(request.getParameter("knjd615vNameKirikae"));
            _knjd615vJugyoJisuLesson = request.getParameter("knjd615vJugyoJisuLesson");
            _use_school_Detail_009 = request.getParameter("use_school_Detail_009");
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            SCHOOLCD = request.getParameter("SCHOOLCD");
            SCHOOLKIND = request.getParameter("SCHOOLKIND");

            _schoolKind = KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND GRADE = '" + _grade + "' "));
            try {
                final Map<String, Object> paramMap = new HashMap<String, Object>();
                if (KnjDbUtils.setTableColumnCheck(db2, "SCHOOL_MST", "SCHOOL_KIND")) {
                    paramMap.put("SCHOOL_KIND", _schoolKind);
                }
                _knjSchoolMst = new KNJSchoolMst(db2, _year, paramMap);
            } catch (Exception e) {
                log.warn("学校マスタ取得でエラー", e);
            }
            _isLastSemester = null != _knjSchoolMst._semesterDiv && _knjSchoolMst._semesterDiv.equals(_semester);

            final StringBuffer vSchoolMstSql = new StringBuffer();
            vSchoolMstSql.append(" SELECT * FROM V_SCHOOL_MST WHERE YEAR = '" + _year + "' ");
            if (KnjDbUtils.setTableColumnCheck(db2, "SCHOOL_MST", "SCHOOL_KIND")) {
                vSchoolMstSql.append(" AND SCHOOL_KIND = '" + _schoolKind + "'  ");
            }
            _vSchoolMst = KnjDbUtils.firstRow(KnjDbUtils.query(db2, vSchoolMstSql.toString()));

            if (_isShimaneken) {
                // 島根県
                final StringBuffer schoolDet009Sql = new StringBuffer();
                schoolDet009Sql.append(" SELECT ");
                schoolDet009Sql.append("     SCHOOL_REMARK1 AS KEIKOKUTEN_KUBUN ");
                schoolDet009Sql.append("   , SCHOOL_REMARK2 AS RUIKEIHEIKIN_BUNSI ");
                schoolDet009Sql.append("   , SCHOOL_REMARK3 AS RUIKEIHEIKIN_BUNBO ");
                schoolDet009Sql.append(" FROM SCHOOL_DETAIL_DAT ");
                schoolDet009Sql.append(" WHERE YEAR = '" + _year + "' ");
                schoolDet009Sql.append("   AND SCHOOL_SEQ = '009' ");
                if (_vSchoolMst.containsKey("SCHOOLCD")) {
                    schoolDet009Sql.append(" AND SCHOOLCD = '" + _vSchoolMst.get("SCHOOLCD") + "' ");
                }
                if (_vSchoolMst.containsKey("SCHOOL_KIND")) {
                    schoolDet009Sql.append(" AND SCHOOL_KIND = '" + _vSchoolMst.get("SCHOOL_KIND") + "' ");
                }
                final Map<String, String> schoolDet009 = KnjDbUtils.firstRow(KnjDbUtils.query(db2, schoolDet009Sql.toString()));
                _keikokutenKubun = KnjDbUtils.getString(schoolDet009, "KEIKOKUTEN_KUBUN");
                _ruikeiheikinBunsi = KnjDbUtils.getString(schoolDet009, "RUIKEIHEIKIN_BUNSI");
                _ruikeiheikinBunbo = KnjDbUtils.getString(schoolDet009, "RUIKEIHEIKIN_BUNBO");
                if (_isOutputDebug) {
                    log.info(" 警告点区分: " + _keikokutenKubun + ", 類型平均分子: " + _ruikeiheikinBunsi + ", 類型平均分母: " + _ruikeiheikinBunbo);
                }
            }
            _nendo = KNJ_EditDate.getAutoFormatYear(db2, Integer.parseInt(_year)) + "年度";
            final boolean useOutputCoursePage = "1".equals(request.getParameter("USE_OUTPUT_COURSE_PAGE"));
            _isOutputCoursePage = !useOutputCoursePage || useOutputCoursePage && "1".equals(request.getParameter("OUTPUT_COURSE_PAGE"));
            _printSubclassLastChairStd = request.getParameter("printSubclassLastChairStd");
            _rankStart = NumberUtils.isDigits(request.getParameter("RANK_START")) ? Integer.parseInt(request.getParameter("RANK_START")) : 0;
            _rankEnd = NumberUtils.isDigits(request.getParameter("RANK_END")) ? Integer.parseInt(request.getParameter("RANK_END")) : Integer.MAX_VALUE;

            _useKetten = !_isSapporo && !("1".equals(request.getParameter("useSchoolMstSemesAssesscd")) && "08".equals(_scoreDiv));
            _is5dankai = _isChiyodaKudan && _testKindCd.endsWith("990008");
            _isPrintAttend = !(PRGID_KNJD615P.equals(_prgId) || PATTERN4.equals(_outputPattern));

            // 出欠の情報
            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
            _attendParamMap.put("grade", _grade);
            _attendParamMap.put("absenceDiv", "2");
            _attendParamMap.put("useTestCountflg", "TESTITEM_MST_COUNTFLG_NEW_SDIV");

            final Map d054Max = KnjDbUtils.firstRow(KnjDbUtils.query(db2, " SELECT NAMECD2, NAME1 FROM NAME_MST WHERE NAMECD1 = 'D054' AND NAMECD2 = (SELECT MAX(NAMECD2) AS NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'D054') "));
            _d054Namecd2Max = KnjDbUtils.getString(d054Max, "NAMECD2");
            _sidouHyoji = KnjDbUtils.getString(d054Max, "NAME1");


            _d055StampMap = new TreeMap();
            if (_d055StampMap.isEmpty()) {
                _d055StampMap.putAll(getPrgStampDat(db2));
            }
            if (_d055StampMap.isEmpty()) {
                if (!_isShimaneken) {
                    _d055StampMap.putAll(KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, " SELECT RIGHT('00' || NAMECD2, 2) AS SEQ, NAME1 AS TITLE FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'D055' "), "SEQ", "TITLE"));
                }
            }
            if (_d055StampMap.isEmpty()) {
                if (_isShimaneken) {
                    _d055StampMap.put("01", "校長");
                    _d055StampMap.put("02", "教頭");
                    _d055StampMap.put("03", "教務");
                    _d055StampMap.put("04", "学年主任");
                    _d055StampMap.put("05", "担任");
                }
            }
            _d065Name1List = KnjDbUtils.getColumnDataList(KnjDbUtils.query(db2, " SELECT NAME1 FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'D065' "), "NAME1");
            _d001Name1Map = KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, " SELECT NAMECD2, NAME1 FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'D001' "), "NAMECD2", "NAME1");
            final String d008NameCd1 = "D" + _schoolKind + "08";
            String d008Namecd2CntStr = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT COUNT(*) FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = '" + d008NameCd1 + "' "));
            int d008Namecd2Cnt = Integer.parseInt(StringUtils.defaultIfEmpty(d008Namecd2CntStr, "0"));
            _d008Namecd2List = KnjDbUtils.getColumnDataList(KnjDbUtils.query(db2, " SELECT NAMECD2 FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = '" + (d008Namecd2Cnt > 0 ? d008NameCd1 : "D008") + "' "), "NAMECD2");
            final String d079field = SEMEALL.equals(_semester) ? "NAMESPARE1" : "ABBV" + _semester;
            _d079Name1List = KnjDbUtils.getColumnDataList(KnjDbUtils.query(db2, " SELECT NAME1 FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'D079' AND " + d079field + " = '1'"), "NAME1");
            if (_isOutputDebug) {
                log.info(" D079 = " + _d079Name1List);
            }
            _definecode = createDefineCode(db2);
            //  学期名称、範囲の取得
            _semesterName = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT SEMESTERNAME FROM SEMESTER_MST WHERE YEAR = '" + _year + "' AND SEMESTER = '" + _semester + "' "));  //学期名称
            // 年度の開始日
            _yearDateS = _sdate;
            if (null == _yearDateS) {
                _yearDateS = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT SDATE FROM SEMESTER_MST WHERE YEAR = '" + _year + "' AND SEMESTER = '" + SEMEALL + "' "));
            }
            // テスト名称
            _testItem = getTestItem(db2, _year, _semester, _testKindCd);
            log.debug(" testKindCd = " + _testKindCd + ", testitem = " + _testItem);
            if (_isKwansei && !PRGID_KNJD615P.equals(_prgId)) {
                _item4Name = "総点"; // 得点×単位数の合計
            } else {
                _item4Name = "合計点";
            }
            _item5Name = "平均点";
            _form2Item4Name = _item4Name;
            _form2Item5Name = _item5Name;
            _attendTerm = getAttendTermKekka(db2, _yearDateS);
            if (_isNaraken) {
                final String semeSdate;
                if (SEMEALL.equals(_semester) || _isLastSemester) {
                    semeSdate = _yearDateS;
                } else {
                    semeSdate = KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT SDATE FROM SEMESTER_MST WHERE YEAR = '" + _year + "' AND SEMESTER = '" + _semester + "' "));
                }
                _attendTermKekka = getAttendTermKekka(db2, semeSdate);
            } else {
                _attendTermKekka = getAttendTermKekka(db2, _yearDateS);
            }
            _now = getNow(db2);

            final String ineiWakuPath = _documentroot + "/image/KNJD615_keninwaku2.jpg";
            final File ineiWakuFile = new File(ineiWakuPath);
            log.info(" ineiWakuFile exists? " + ineiWakuFile.exists());
            if (!ineiWakuFile.exists()) {
                _ineiWakuPath = null;
            } else {
                _ineiWakuPath = ineiWakuPath;
            }

            _item2Name = "欠課";
            _isGakunenMatu = SCORE_DIV_09.equals(_scoreDiv);
            _hasCompCredit = SCORE_DIV_09.equals(_scoreDiv);
            _isPrintPerfect = PATTERN1.equals(_outputPattern) && SCORE_DIV_01.equals(_scoreDiv) && "1".equals(_knjd615vPrintPerfect);
            _knjd615vNotPrintTransInfo = _isKudan;
            _failValue = 30;
            if ("2".equals(_outputValue)) {
                _item1Name = "偏差値";
            } else if (SCORE_DIV_01.equals(_scoreDiv) || SCORE_DIV_02.equals(_scoreDiv)) {
                _item1Name = defstr(_d053Name1Map.get(_scoreDiv), SCORE_DIV_02.equals(_scoreDiv) ? "平常点" : "素点");
                _creditDrop = false;
            } else if (SCORE_DIV_08.equals(_scoreDiv)) {
                _item1Name = "評価";
            } else if (SCORE_DIV_09.equals(_scoreDiv)) {
                _item1Name = "評定";
            }
            if (SCORE_DIV_09.equals(_scoreDiv)) {
                _title = _testItem._testitemname + "  成績一覧表（評定）";
            } else {
                _title = _semesterName + " " + _testItem._testitemname + " 成績一覧表";
            }
            _subtitle = getSubtitle();
            if (OUTPUT_RANK1.equals(_outputRank)) {
                _rankName = "学級";
                _avgDiv = "2";
            } else if (OUTPUT_RANK2.equals(_outputRank)) {
                _rankName = "学年";
                _avgDiv = "1";
            } else if (OUTPUT_RANK3.equals(_outputRank)) {
                _rankName = "コース";
                _avgDiv = "3";
            } else if (OUTPUT_RANK4.equals(_outputRank)) {
                _rankName = "学科";
                _avgDiv = "4";
            }
            _rankName = defstr(_rankName);
            log.debug("順位名称=" + _rankName);
            setSubclassMst(db2);
            setPrintSakiKamoku(db2);
            loadNameMstD016(db2);
            _gdatgradename = loadGdatGName(db2, _year, _grade);
            if (_isNaraken) {
                setNarakenKekkaOverparameter(db2);
            }
            _schChrDatCount = toInt(KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT COUNT(*) FROM SCH_CHR_DAT WHERE YEAR = '" + _year + "' ")), 0);
        }

        private void close() {
            if (_isOutputDebug) {
                for (final Map.Entry<String, String> e : _elapsed.entrySet()) {
                    log.info(" elapsed " + e.getKey() + " = " + e.getValue());
                }
            }
            for (final Iterator<PreparedStatement> it = _psMap.values().iterator(); it.hasNext();) {
                final PreparedStatement ps = it.next();
                DbUtils.closeQuietly(ps);
                it.remove();
            }
            for (final File file : _createdFiles.values()) {
                log.info(" file " + file.getAbsolutePath() + (_isOutputDebugFormCreate ?  "" : " deleted? " + file.delete()));
            }
        }

        private void logOnce(final String s) {
            if (!_logOnce.contains(s)) {
                log.info(s);
                _logOnce.add(s);
            }
        }

        private String getSubtitle() {
            final List<String> subtitleList = new ArrayList<String>();
            if (PATTERN4.equals(_outputPattern)) {
                subtitleList.add("成績の記録");
            } else if (PATTERN5.equals(_outputPattern)) {
                subtitleList.add("欠課時数と出欠の記録");
            }
            if (null != _groupDiv) {
                if (_isPrintGroupGrade) {
                    subtitleList.add("学年毎");
                } else if (_isPrintGroupCourse) {
                    subtitleList.add("コース毎");
                } else if (_isPrintGroupHr) {
                    subtitleList.add("クラス毎");
                }
            }
            if (null != _outputValue) {
                if (!PATTERN5.equals(_outputPattern)) {
                    subtitleList.add(defstr(_item1Name));
                }
            }
            final String subtitle = kakko(mkString(subtitleList, "・"));
            return subtitle;
        }

        private void setNarakenKekkaOverparameter(final DB2UDB db2) {
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT COUNT(*) AS COUNT ");
            sql.append(" FROM SCH_CHR_DAT T1 ");
            sql.append(" INNER JOIN CHAIR_DAT T2 ON T2.YEAR = T1.YEAR AND T2.SEMESTER = T1.SEMESTER AND T2.CHAIRCD = T1.CHAIRCD ");
            sql.append(" WHERE T1.YEAR = '" + _year +"' ");

            final String countStr = KnjDbUtils.getOne(KnjDbUtils.query(db2, sql.toString()));
            int count = 0;
            if (NumberUtils.isDigits(countStr)) {
                count = Integer.parseInt(countStr);
            }
            _isHibinyuryoku = count >= 10; // 時間割がある学校を日々入力とする
        }

        private PreparedStatement getPs(final String psKey) {
            return _psMap.get(psKey);
        }

        private static String getDbPrginfoProperties(final DB2UDB db2, final String propName) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJD615V' AND NAME = '" + propName + "' "));
        }

        /** 作成日 */
        public String getNow(final DB2UDB db2) {
            final StringBuffer stb = new StringBuffer();
            final Date date = new Date();
            stb.append(KNJ_EditDate.getAutoFormatDate(db2, new SimpleDateFormat("yyyy-MM-dd").format(date)));
            final SimpleDateFormat sdf = new SimpleDateFormat("H時m分");
            stb.append(sdf.format(date));
            return stb.toString();
        }

        /** 欠課の集計範囲 */
        public String getAttendTermKekka(final DB2UDB db2, final String startDate) {
            return KNJ_EditDate.getAutoFormatDate(db2, startDate) + FROM_TO_MARK + KNJ_EditDate.getAutoFormatDate(db2, _date);
        }

        private DecimalFormat getAbsentFmt() {
            DecimalFormat absentFmt;
            switch (_definecode.absent_cov) {
            case 3:
            case 4:
                absentFmt = new DecimalFormat("0.0");
                break;
            default:
                absentFmt = new DecimalFormat("0");
            }
            return absentFmt;
        }

        /*
         *  クラス内で使用する定数設定
         */
        private KNJDefineSchool createDefineCode(final DB2UDB db2) {
            final KNJDefineSchool definecode = new KNJDefineSchool();
            definecode.defineCode(db2, _year);         //各学校における定数等設定
            log.debug("semesdiv=" + definecode.semesdiv + "   absent_cov=" + definecode.absent_cov + "   absent_cov_late=" + definecode.absent_cov_late);
            return definecode;
        }

        private TestItem getTestItem(final DB2UDB db2, final String year, final String semester, final String testcd) {
            TestItem testitem = new TestItem();
            final String sql = "SELECT TESTITEMNAME, SIDOU_INPUT, SIDOU_INPUT_INF "
                    +   "FROM TESTITEM_MST_COUNTFLG_NEW_SDIV "
                    +  "WHERE YEAR = '" + year + "' "
                    +    "AND SEMESTER = '" + semester + "' "
                    +    "AND TESTKINDCD || TESTITEMCD || SCORE_DIV = '" + testcd + "' ";
            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql));
            testitem._testitemname = KnjDbUtils.getString(row, "TESTITEMNAME");
            testitem._sidouinput = KnjDbUtils.getString(row, "SIDOU_INPUT");
            testitem._sidouinputinf = KnjDbUtils.getString(row, "SIDOU_INPUT_INF");
            return testitem;
        }

        private String getAttendRemarkMonth(final DB2UDB db2, final String date) {
            String rtn = "99";
            if (null == date) {
                return rtn;
            }
            final Calendar cal = Calendar.getInstance();
            cal.setTime(java.sql.Date.valueOf(date));
            int month = cal.get(Calendar.MONTH) + 1;
            if (month < 4) {
                month += 12;
            }
            return String.valueOf(month);
        }

        private void loadNameMstD016(final DB2UDB db2) {
            _isNoPrintMoto = false;
            if (SEMEALL.equals(_semester)) {
                final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, "SELECT NAMECD2, NAMESPARE1, NAMESPARE2 FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'D016' AND NAMECD2 = '01' "));
                if ("Y".equals(KnjDbUtils.getString(row, "NAMESPARE1"))) {
                    _isNoPrintMoto = true;
                }
                log.info("(名称マスタD016):元科目を表示しない = " + _isNoPrintMoto);
            }
        }

        private String loadGdatGName(final DB2UDB db2, final String year, final String grade) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, " select GRADE_NAME1 FROM SCHREG_REGD_GDAT WHERE YEAR = '" + year + "' AND GRADE = '" + grade + "' "));
        }

        /**
         * 合併先科目を印刷するか
         */
        private void setPrintSakiKamoku(final DB2UDB db2) {
            // 初期値：印刷する
            _isPrintSakiKamoku = true;

            // 名称マスタ「D021」「01」から取得する
            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, "SELECT NAMESPARE3 FROM V_NAME_MST WHERE YEAR='" + _year+ "' AND NAMECD1 = 'D021' AND NAMECD2 = '01' "));
            if ("Y".equals(KnjDbUtils.getString(row, "NAMESPARE3"))) {
                _isPrintSakiKamoku = false;
            }

            log.debug("合併先科目を印刷するか：" + _isPrintSakiKamoku);
        }

        private SubclassMst getSubclassMst(final String subclasscd) {
            if (null == _subclassMst.get(subclasscd)) {
                return new SubclassMst(null, null, null, null, null);
            }
            return _subclassMst.get(subclasscd);
        }

        private void setSubclassMst(final DB2UDB db2) {
            _subclassMst = new HashMap<String, SubclassMst>();
            String sql = "";
            sql += " SELECT ";
            sql += "      T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD ";
            sql += "    , T2.CLASSABBV, T2.CLASSNAME, T1.SUBCLASSABBV, VALUE(T1.SUBCLASSORDERNAME2, T1.SUBCLASSNAME) AS SUBCLASSNAME ";
            sql += " FROM SUBCLASS_MST T1 ";
            sql += " LEFT JOIN CLASS_MST T2 ON T2.CLASSCD = T1.CLASSCD AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
            sql += " WHERE ";
            sql += "     T1.SCHOOL_KIND = '" + _schoolKind + "' ";
            for (final Map row : KnjDbUtils.query(db2, sql)) {
                final SubclassMst mst = new SubclassMst(KnjDbUtils.getString(row, "SUBCLASSCD"), KnjDbUtils.getString(row, "CLASSABBV"), KnjDbUtils.getString(row, "CLASSNAME"), KnjDbUtils.getString(row, "SUBCLASSABBV"), KnjDbUtils.getString(row, "SUBCLASSNAME"));
                _subclassMst.put(KnjDbUtils.getString(row, "SUBCLASSCD"), mst);
            }

            _combinedCourseListMap = new TreeMap<SubclassMst, Set<String>>();
            _attendCourseListMap = new TreeMap<SubclassMst, Set<String>>();
            if ("1".equals(_useSubclassWeightingCourseDat)) {
                String sql2 = "";
                sql2 += " SELECT ";
                sql2 += "     COURSECD || MAJORCD || COURSECODE AS COURSE ";
                sql2 += "   , COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || COMBINED_SUBCLASSCD AS COMBINED_SUBCLASSCD ";
                sql2 += "   , ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ATTEND_SUBCLASSCD AS ATTEND_SUBCLASSCD ";
                sql2 += " FROM SUBCLASS_WEIGHTING_COURSE_DAT ";
                sql2 += " WHERE YEAR = '" + _year + "' ";
                sql2 += "   AND FLG = '" + ("99".equals(_testKindCd.substring(0, 2)) ? "2" : "1") + "' ";
                sql2 += "   AND GRADE = '" + _grade + "' ";
                for (final Map row : KnjDbUtils.query(db2, sql2)) {
                    final SubclassMst combined = _subclassMst.get(KnjDbUtils.getString(row, "COMBINED_SUBCLASSCD"));
                    final SubclassMst attend = _subclassMst.get(KnjDbUtils.getString(row, "ATTEND_SUBCLASSCD"));
                    if (null == combined || null == attend) {
                        continue;
                    }
                    getMappedTreeSet(_combinedCourseListMap, combined).add(KnjDbUtils.getString(row, "COURSE"));
                    getMappedTreeSet(_attendCourseListMap, attend).add(KnjDbUtils.getString(row, "COURSE"));
                }

            } else {
                String sql2 = "";
                sql2 += " SELECT ";
                sql2 += "     COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || COMBINED_SUBCLASSCD AS COMBINED_SUBCLASSCD ";
                sql2 += "   , ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ATTEND_SUBCLASSCD AS ATTEND_SUBCLASSCD ";
                sql2 += " FROM SUBCLASS_REPLACE_COMBINED_DAT WHERE YEAR = '" + _year + "' ";
                for (final Map row : KnjDbUtils.query(db2, sql2)) {
                    final SubclassMst combined = _subclassMst.get(KnjDbUtils.getString(row, "COMBINED_SUBCLASSCD"));
                    final SubclassMst attend = _subclassMst.get(KnjDbUtils.getString(row, "ATTEND_SUBCLASSCD"));
                    if (null == combined || null == attend) {
                        continue;
                    }
                    getMappedTreeSet(_combinedCourseListMap, combined).add("ALL");
                    getMappedTreeSet(_attendCourseListMap, attend).add("ALL");
                }
            }
            if (_isOutputDebug) {
                log.info(" combinedCourseListMap = " + _combinedCourseListMap);
                log.info(" attendCourseListMap = " + _attendCourseListMap);
            }
        }

        private Map<String, String> getPrgStampDat(final DB2UDB db2) {

            final Map<String, String> seqTitleMap = new HashMap();

            if (KnjDbUtils.setTableColumnCheck(db2, "PRG_STAMP_DAT", null)) {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT  ");
                stb.append("    RIGHT('00' || T1.SEQ, 2) AS SEQ ");
                stb.append("  , T1.TITLE ");
                stb.append(" FROM PRG_STAMP_DAT T1 ");
                stb.append(" WHERE ");
                stb.append("   T1.YEAR = '" + _year + "' ");
                stb.append("   AND T1.SEMESTER = '9' ");
                if ("1".equals(_useSchool_KindField)) {
                    stb.append("   AND T1.SCHOOLCD = '" + SCHOOLCD + "' ");
                    stb.append("   AND T1.SCHOOL_KIND = '" + SCHOOLKIND + "' ");
                }
                stb.append("   AND T1.PROGRAMID = 'KNJD615V' ");

                seqTitleMap.putAll(KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, stb.toString()), "SEQ", "TITLE"));
            }

            return seqTitleMap;
        }

    }

}
