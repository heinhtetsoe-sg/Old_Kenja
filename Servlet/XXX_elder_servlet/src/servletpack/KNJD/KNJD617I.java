// kanji=漢字
/*
 * $Id: b9e8c374fb98a1fd958ae5cf1630987d77a7e80d $
 *
 * 作成日: 2018/07/20
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.CsvUtils;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_Get_Info;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.SvfField;
import servletpack.KNJZ.detail.dao.AttendAccumulate;
import servletpack.pdf.IPdf;
import servletpack.pdf.SvfPdf;

/**
 * 成績一覧表（成績判定会議用）を印刷します。
 * 成績処理改訂版。
 * 成績判定会議用をBASEとし、従来の成績一覧表の成績処理改訂版の印刷処理を行います。
 * @author nakamoto
 * @version $Id: b9e8c374fb98a1fd958ae5cf1630987d77a7e80d $
 */
public class KNJD617I {
    private static final Log log = LogFactory.getLog(KNJD617I.class);

    private static final DecimalFormat DEC_FMT1 = new DecimalFormat("0.0");
    private static final DecimalFormat DEC_FMT2 = new DecimalFormat("0");
    private static final String SEMEALL = "9";

    static final String PATTERN1 = "1"; // 科目固定型
    static final String PATTERN2 = "2"; // 科目変動型
    static final String PATTERN3 = "3"; // 科目固定型（仮評定付）
    static final String PATTERN4 = "4"; // 成績の記録
    static final String PATTERN5 = "5"; // 欠時数と出欠の記録

    private static final String OUTPUT_KJUN2 = "2";
    private static final String OUTPUT_KJUN3 = "3";
    private static final String OUTPUT_RANK1 = "1";
    private static final String OUTPUT_RANK2 = "2";
    private static final String OUTPUT_RANK3 = "3";
    private static final String OUTPUT_RANK4 = "4";

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

    private static final String csv = "csv";
    
    private static final String PRGID_KNJD615H = "KNJD615H";
    private static final String PRGID_KNJD617I = "KNJD617I";

//    private final static String DATA_SELECT_ALL = "0";
//    private final static String DATA_SELECT_RYOUSEI = "1";
//    private final static String DATA_SELECT_SUISEN = "2";
//    private final static String DATA_SELECT_KAIGAI = "3";
//    private final static String DATA_SELECT_IB = "4";
//    private final static String DATA_SELECT_A_HOUSHIKI = "6";

//    private final static String SUISEN = "4";
//    private final static String KAIGAI = "5";
//    private final static String A_HOUSHIKI = "1";
//    private final static String IB_COURSECODE = "0002";
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
                final List outputLines = new ArrayList();
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
                for (final Iterator it = param._psMap.values().iterator(); it.hasNext();) {
                    final PreparedStatement ps = (PreparedStatement) it.next();
                    DbUtils.closeQuietly(ps);
                }

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

    public void setOutputCsvLines(
            final DB2UDB db2,
            final Param param,
            final List outputList
    ) throws Exception {

        for (int h = 0; h < param._classSelected.length; h++) { //印刷対象HR組
            final List courses = Course.createCourses(db2, param, param._classSelected[h]);
            log.debug("コース数=" + courses.size());

            final HRInfo hrInfo = new HRInfo(param._classSelected[h]);  //HR組
            log.info(" print hr " + hrInfo._hrclassCd);
            hrInfo._courses = courses;

            hrInfo.load(db2, param, null);

            // 印刷処理
            final Form form = new Form();
            form.outputCsv(db2, outputList, param, hrInfo);
        }
    }

    /**
     *  KNJD.classから最初に起動されます。
     */
    public void printMain(
            final DB2UDB db2,
            final Param param,
            final IPdf ipdf
    ) throws Exception {

//        List bkupReportInfo = new ArrayList();
        _hasData = false;
        for (int h = 0; h < param._classSelected.length; h++) { //印刷対象HR組

            final List courses = Course.createCourses(db2, param, param._classSelected[h]);
            log.debug("コース数=" + courses.size());

            final HRInfo hrInfo = new HRInfo(param._classSelected[h]);  //HR組
            hrInfo._courses = courses;

            hrInfo.load(db2, param, null);
            // 印刷処理
            final Form form = new Form();
            if (form.print(db2, ipdf, param, hrInfo)) {
                _hasData = true;
            }
//            // クラス毎の情報を保持する。
//            hrInfo.bkupInfo(param, bkupReportInfo);
        }
//        if (_hasData && "1".equals(param._printreport)) {
//            printSummary(db2, param, bkupReportInfo, ipdf);
//        }
    }

//    private void printSummary(final DB2UDB db2, final Param param, final List bkupReportInfo, final IPdf ipdf) {
//        //サブクラスコード->対象クラスのリストを作成する。
//        String formname = "KNJD615V_6.frm";
//        ipdf.VrSetForm(formname, 1);
//
//        Map subclasscdlist = createSubclasscdList(bkupReportInfo);
//        Set sclkeys = subclasscdlist.keySet();
//        int colcnt = 1;
//        int pagecnt = 0;
//        boolean bprintoutwait = false;
//        for (final Iterator it = sclkeys.iterator(); it.hasNext();) {
//            List infolist = (List)subclasscdlist.get(it.next());
//            for (final Iterator its = infolist.iterator(); its.hasNext();) {
//                SubClass outinfo = (SubClass)its.next();
//                String scfieldname = "SUBCLASS_NAME" + (colcnt - (pagecnt * 20)) + "_" + ((outinfo._subclassname).length() <= 8 ? "1" : "2");
//                ipdf.VrsOut(scfieldname, outinfo._subclassname);
//                if (bprintoutwait && (colcnt % 20 == 0)) {
//                    printSummarySub(db2, param, bkupReportInfo, ipdf, pagecnt, subclasscdlist);
//                    pagecnt++;
//                    bprintoutwait = false;
//                } else {
//                    bprintoutwait = true;
//                }
//                colcnt++;
//                break; //タイトル部は、List内の先頭データでのみ科目名称を出力(同じ科目で並んでいるので、2番目以降は参照不要)
//            }
//        }
//        if (bprintoutwait) {
//            printSummarySub(db2, param, bkupReportInfo, ipdf, pagecnt, subclasscdlist);
//        }
//    }

//    private void printSummarySub(final DB2UDB db2, final Param param, final List bkupReportInfo, final IPdf ipdf, final int pagecnt, Map subclasscdlist) {
//        //Title
//        ipdf.VrsOut("TITLE", param._gdatgradename + " クラス成績表"); // タイトル
//        ipdf.VrsOut("GRADE", param._testItem._testitemname); // 学期
//        final String printDateTime = KNJ_EditDate.h_format_thi(param._logindate, 0);// + "　" + cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE);
//        ipdf.VrsOut("DATE", printDateTime); // 学期
//
//        Set sclkeys = subclasscdlist.keySet();
//        //クラス毎に処理
//        int linecnt = 1;
//        for (final Iterator it = bkupReportInfo.iterator(); it.hasNext();) {
//            final ReportInfo repoinfo = (ReportInfo) it.next();
//            String hrfieldname = "HR_NAME";
//            ipdf.VrsOutn(hrfieldname, linecnt, repoinfo._hrName);
//            Set sckeys = repoinfo._reportlist._subclasses.keySet();
//            for (final Iterator its = sckeys.iterator(); its.hasNext();) {
//                final SubClass sc = (SubClass)repoinfo._reportlist._subclasses.get(its.next());
//                int cnt = 0;
//                for (final Iterator itt = sclkeys.iterator(); itt.hasNext();) {
//                    String chkcode = (String)itt.next();
//                    if (sc._subclasscode.equals(chkcode)) {
//                        break;
//                    }
//                    cnt++;
//                }
//                if (cnt < sclkeys.size() && (pagecnt) * 20 <= cnt && cnt < (pagecnt+1) * 20) {
//                    final int cntwk = (cnt % 20) + 1 ;//0ベース->1ベースに修正。
//                    //cnt列目に出力
//                    String staffstr = arrangeSomeStaffName(sc._staffname, sc._otherstafflist);
//                    String chfieldname = "CHARGE" + cntwk + "_" + (KNJ_EditEdit.getMS932ByteLength(staffstr) <= 6 ? "1" : "2");
//                    ipdf.VrsOutn(chfieldname, linecnt, staffstr);
//                    //staffstr
//                    String avefieldname = "AVERAGE" + cntwk;
//                    ipdf.VrsOutn(avefieldname, linecnt, sc._scoreaverage);
//                }
//            }
//            linecnt++;
//        }
//        if (linecnt > 1) {
//            ipdf.VrEndPage();
//        }
//    }

//    private String arrangeSomeStaffName(final String staffname, final List otherstafflist) {
//        String retstr;
//        if (otherstafflist.size() == 0) {
//            retstr = staffname;
//        } else {
//            retstr = getmyouji(staffname);
//            for (int cnt = 0;cnt < otherstafflist.size();cnt++) {
//                retstr += "・" + getmyouji((String)otherstafflist.get(cnt));
//            }
//        }
//        return retstr;
//    }

//    private String getmyouji(final String cutstaffname) {
//        String[] staffnamecut;
//        if (cutstaffname.indexOf("　") >= 0) {
//            staffnamecut = StringUtils.split(cutstaffname, "　");
//        } else {
//            staffnamecut = StringUtils.split(cutstaffname, " ");
//        }
//        return staffnamecut[0];
//    }

//    private Map createSubclasscdList(final List bkupReportInfo) {
//        Map retlist = new LinkedMap();
//        for (final Iterator it = bkupReportInfo.iterator(); it.hasNext();) {
//            final ReportInfo repoinfo = (ReportInfo) it.next();
//            List subjectwklist = new ArrayList(repoinfo._reportlist._subclasses.values());
//            for (final Iterator its = subjectwklist.iterator(); its.hasNext();) {
//                final SubClass sc = (SubClass)its.next();
//                if (!retlist.containsKey(sc._subclasscode)) {
//                    List subclassdat = new ArrayList();
//                    subclassdat.add(sc);
//                    retlist.put(sc._subclasscode, subclassdat);
//                } else {
//                    List addwk = (ArrayList)retlist.get(sc._subclasscode);
//                    addwk.add(sc);
//                }
//            }
//        }
//        return retlist;
//    }

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

    private static class Course {
        final String _grade;
        final String _hrclass;
        final String _coursecd;
        final String _name;

        public Course(
                final String grade,
                final String hrclass,
                final String coursecd,
                final String name
        ) {
            _grade = grade;
            _hrclass = hrclass;
            _coursecd = coursecd;
            _name = name;
        }

        private static List createCourses(final DB2UDB db2, final Param param, final String gradeHrclass) {
            final List rtn = new ArrayList();

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

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String grade = rs.getString("GRADE");
                    final String hrclass = rs.getString("HR_CLASS");
                    final String coursecd = rs.getString("COURSECD");
                    final String name = rs.getString("COURSECODENAME");

                    final Course course = new Course(
                            grade,
                            hrclass,
                            coursecd,
                            name
                    );

                    rtn.add(course);
                }
            } catch (final Exception ex) {
                log.error("コースのロードでエラー:" + sql, ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtn;
        }

        public String toString() {
            return _coursecd + ":" + _name;
        }
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * <<学級のクラス>>。
     */
    private static class HRInfo implements Comparable {
        final String _hrclassCd;
        private List _courses;
        private String _staffName;
        private String _hrName;
        private List _students = Collections.EMPTY_LIST;
        private Map _studentMap = Collections.EMPTY_MAP;
        private Set _courseSet = new HashSet();
        private Set _majorSet = new HashSet();
        private final Map _subclasses = new TreeMap();
        private List _ranking;
        private BigDecimal _avgHrTotalScore;  // 総合点の学級平均
        private BigDecimal _avgHrAverageScore;  // 平均点の学級平均
        private BigDecimal _perHrPresent;  // 学級の出席率
        private BigDecimal _perHrSick;  // 学級の欠席率
        private String _HrCompCredits;  // 学級の履修単位数
        private String _HrMLesson;  // 学級の授業日数
        private String _avgHrTotal;   // 総合点の学級平均
        private String _avgHrAverage; // 平均点の学級平均
        private int _avgHrCount; // 総合点の学級の母集団の数
        private String _avgGradeAverage; // 平均点の学級平均
        private String _avgGradeTotal; // 総合点の学年平均
        private String _avgCourseAverage;
        private String _avgCourseTotal; // 総合点のコース平均
        private String _avgMajorAverage;
        private String _avgMajorTotal; // 総合点の学科平均
        private String _maxHrTotal;   // 総合点の最高点
        private String _minHrTotal;   // 総合点の最低点
        private String _failHrTotal;  // 欠点の数

        public HRInfo(final String hrclassCd) {
            _hrclassCd = hrclassCd;
        }

        public void load(
                final DB2UDB db2,
                final Param param,
                final String courseCd
        ) {
            loadHRClassStaff(db2, param);
            loadStudents(db2, param, courseCd);
            loadStudentsInfo(db2, param);
            if (param._isOutputDebug) {
            	log.info(" load attend.");
            }
            loadAttend(db2, param);
            loadAttendRemark(db2, param);
            if (param._isOutputDebug) {
            	log.info(" load hrclassAverage.");
            }
            loadHrclassAverage(db2, param, courseCd);
            if (param._isOutputDebug) {
            	log.info(" load rank.");
            }
            loadRank(db2, param);
            if (param._isOutputDebug) {
            	log.info(" load scoreDetail.");
            }
            loadScoreDetail(db2, param);
            _ranking = createRanking(param);
            log.debug("RANK:" + _ranking);
            setSubclassAverage(param);
            setHrTotal();  // 学級平均等の算出
            setHrTotalMaxMin();
            setHrTotalFail();
            setSubclassGradeAverage(db2, param, "1");
            setSubclassGradeAverage(db2, param, "3");
            if (param._hasJudgementItem) {
                loadPreviousCredits(db2, param);  // 前年度までの修得単位数取得
                loadPreviousMirisyu(db2, param);  // 前年度までの未履修（必須科目）数
                loadQualifiedCredits(db2, param);  // 今年度の資格認定単位数
            }
        }

        private void loadAttendRemark(final DB2UDB db2, final Param param) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {

                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     SD.SCHREGNO, ");
                stb.append("     RMK.SEMESTER, ");
                stb.append("     RMK.MONTH, ");
                stb.append("     RMK.REMARK1 AS REMARK ");
                stb.append(" FROM ");
                stb.append("     SCHREG_REGD_DAT SD ");
                stb.append(" INNER JOIN ATTEND_SEMES_REMARK_DAT RMK ON SD.YEAR = RMK.YEAR ");
                stb.append("      AND SD.SCHREGNO = RMK.SCHREGNO ");
                if ("2".equals(param._bikoKind)) {
                    stb.append("      AND RMK.SEMESTER >= '" + (SEMEALL.equals(param._semester) ? param._semeFlg : param._semester) + "' ");
                }
                stb.append("      AND INT(RMK.MONTH) + CASE WHEN INT(RMK.MONTH) < 4 THEN 12 ELSE 0 END <= " + param.getAttendRemarkMonth(db2, param._date) + " ");
                stb.append(" WHERE ");
                stb.append("     SD.YEAR = '" + param._year + "' ");
                stb.append("     AND SD.SEMESTER = '" + (SEMEALL.equals(param._semester) ? param._semeFlg : param._semester) + "' ");
                stb.append("     AND SD.GRADE = '" + _hrclassCd.substring(0, 2) + "' AND SD.HR_CLASS = '" + _hrclassCd.substring(2) + "' ");
                stb.append(" ORDER BY SD.SCHREGNO, RMK.SEMESTER, INT(RMK.MONTH) + CASE WHEN INT(RMK.MONTH) < 4 THEN 12 ELSE 0 END  ");

                final String sql = stb.toString();
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final Student student = getStudent(rs.getString("SCHREGNO"));
                    if (null == student) {
                        continue;
                    }
                    if (null == rs.getString("REMARK") && !"1".equals(param._knjd615vPrintNullRemark)) {
                        continue;
                    }
                    if ("3".equals(param._bikoKind) || null == student._attendSemesRemarkDatRemark1) {
                        student._attendSemesRemarkDatRemark1 = "";
                    } else {
                        student._attendSemesRemarkDatRemark1 += null == rs.getString("REMARK") ? "" : " ";
                    }
                    student._attendSemesRemarkDatRemark1 += StringUtils.defaultString(rs.getString("REMARK"));
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private void loadHRClassStaff(
                final DB2UDB db2,
                final Param param
        ) {
            final KNJ_Get_Info.ReturnVal returnval = param._getinfo.Hrclass_Staff(
                    db2,
                    param._year,
                    param._semester,
                    _hrclassCd,
                    ""
            );
            _staffName = returnval.val3;
            _hrName = returnval.val1;
        }

        private void loadStudents(
                final DB2UDB db2,
                final Param param,
                final String courseCd
        ) {
            _students = new LinkedList();
            _studentMap = new HashMap();
            try {

                final StringBuffer stb = new StringBuffer();

                stb.append(" SELECT ");
                stb.append("    W1.SCHREGNO, ");
                stb.append("    W1.ATTENDNO, ");
//                if (PRGID_KNJD615H.equals(param._prgId)) {
//                    stb.append("    CASE WHEN DOMI.SCHREGNO IS NOT NULL THEN '寮' ELSE '' END AS RYOU, ");
//                    stb.append("    CASE WHEN BASE_DE.BASE_REMARK1 = '" + KAIGAI + "' THEN '海' ELSE '' END AS KAIGAI, ");
//                    stb.append("    CASE WHEN BASE_DE.BASE_REMARK1 = '" + SUISEN + "' THEN '推' ELSE '' END AS SUISEN, ");
//                    stb.append("    CASE WHEN W1.COURSECODE = '" + IB_COURSECODE + "' THEN 'IB' ELSE '' END AS IB, ");
//                    stb.append("    CASE WHEN BASE_DE.BASE_REMARK2 = '" + A_HOUSHIKI + "' THEN 'Ａ' ELSE '' END AS AHOUSHIKI, ");
//                } else {
                    stb.append("    '' AS RYOU, ");
                    stb.append("    '' AS KAIGAI, ");
                    stb.append("    '' AS SUISEN, ");
                    stb.append("    '' AS IB, ");
                    stb.append("    '' AS AHOUSHIKI, ");
//                }
                stb.append("    COURSECD || MAJORCD AS MAJOR, ");
                stb.append("    COURSECD || MAJORCD || COURSECODE AS COURSE ");
                stb.append(" FROM ");
                stb.append("    SCHREG_REGD_DAT W1 ");
                stb.append("    INNER JOIN SCHREG_BASE_MST W3 ON W3.SCHREGNO = W1.SCHREGNO ");
                stb.append("    LEFT JOIN SCHREG_BASE_DETAIL_MST BASE_DE ON W1.SCHREGNO = BASE_DE.SCHREGNO ");
                stb.append("         AND BASE_DE.BASE_SEQ = '014' ");
//                if (PRGID_KNJD615H.equals(param._prgId)) {
//                	stb.append("    LEFT JOIN ( ");
//                	stb.append("            SELECT ");
//                	stb.append("                * ");
//                	stb.append("            FROM ");
//                	stb.append("                SCHREG_DOMITORY_HIST_DAT ");
//                	stb.append("            WHERE ");
//                	stb.append("                '" + param._logindate + "' BETWEEN DOMI_ENTDAY AND VALUE(DOMI_OUTDAY, '9999-12-31') ");
//                	stb.append("    ) DOMI ON W1.SCHREGNO = DOMI.SCHREGNO ");
//                }
                stb.append(" WHERE ");
                stb.append("    W1.YEAR = '" + param._year + "' ");
                if (!SEMEALL.equals(param._semester)) {
                    stb.append("    AND W1.SEMESTER = '" + param._semester + "' ");
                } else {
                    stb.append("    AND W1.SEMESTER = '" + param._semeFlg + "' ");
                }
                stb.append("    AND W1.GRADE||W1.HR_CLASS = '" + _hrclassCd + "' ");
//                if (PRGID_KNJD615H.equals(param._prgId)) {
//                	if (DATA_SELECT_RYOUSEI.equals(param._dataSelect)) {
//                		stb.append("    AND DOMI.SCHREGNO IS NOT NULL ");
//                	}
//                	if (DATA_SELECT_KAIGAI.equals(param._dataSelect)) {
//                		stb.append("    AND BASE_DE.BASE_REMARK1 = '" + KAIGAI + "' ");
//                	}
//                	if (DATA_SELECT_SUISEN.equals(param._dataSelect)) {
//                		stb.append("    AND BASE_DE.BASE_REMARK1 = '" + SUISEN + "' ");
//                	}
//                	if (DATA_SELECT_IB.equals(param._dataSelect)) {
//                		stb.append("    AND W1.COURSECODE = '" + IB_COURSECODE + "' ");
//                	}
//                	if (DATA_SELECT_A_HOUSHIKI.equals(param._dataSelect)) {
//                		stb.append("    AND BASE_DE.BASE_REMARK2 = '" + A_HOUSHIKI + "' ");
//                	}
//                }
                stb.append(" ORDER BY ");
                stb.append("    W1.ATTENDNO");

                final String sql = stb.toString();

                int gnum = 0;
                final Integer zero = new Integer(0);
                for (final Iterator it = KnjDbUtils.query(db2, sql).iterator(); it.hasNext();) {
                	final Map row = (Map) it.next();
                	if ("1".equals(param._notEmptyLine)) {
                        gnum = _students.size() + 1;
                	} else {
                        gnum = KnjDbUtils.getInt(row, "ATTENDNO", zero).intValue();
                	}
                    final Student student = new Student(KnjDbUtils.getString(row, "SCHREGNO"), this, gnum, KnjDbUtils.getString(row, "RYOU"), KnjDbUtils.getString(row, "KAIGAI"), KnjDbUtils.getString(row, "SUISEN"), KnjDbUtils.getString(row, "IB"), KnjDbUtils.getString(row, "AHOUSHIKI"));
                    _students.add(student);
                    _studentMap.put(student._schregno, student);
                    _courseSet.add(KnjDbUtils.getString(row, "COURSE"));
                    _majorSet.add(KnjDbUtils.getString(row, "MAJOR"));
                }
            } catch (Exception e) {
                log.error("Exception", e);
            }
        }

        private void loadStudentsInfo(
                final DB2UDB db2,
                final Param param
        ) {
            PreparedStatement ps = null;

            try {
                final StringBuffer stb = new StringBuffer();

                stb.append("SELECT  W1.SCHREGNO, W1.ATTENDNO, W3.NAME, W6.HR_NAME, ");
                stb.append("        CASE WHEN W4.GRD_DATE IS NOT NULL THEN W4.GRD_DATE ELSE W4.ENT_DATE END AS KBN_DATE1, ");
                stb.append("        CASE WHEN W4.GRD_DATE IS NOT NULL THEN (SELECT NAME1 FROM NAME_MST S1 WHERE S1.NAMECD1 = 'A003' AND S1.NAMECD2 = W4.GRD_DIV) ");
                stb.append("             ELSE (SELECT NAME1 FROM NAME_MST S1 WHERE S1.NAMECD1 = 'A002' AND S1.NAMECD2 = W4.ENT_DIV) END AS KBN_NAME1, ");
                stb.append("        W5.TRANSFER_SDATE AS KBN_DATE2,");
                stb.append("        (SELECT NAME1 FROM NAME_MST S1 WHERE S1.NAMECD1='A004' AND S1.NAMECD2 = W5.TRANSFERCD) AS KBN_NAME2 ");
//                if (param._isTokiwagi) {
//                    stb.append("     , NMA044.ABBV1 AS SCHOLARSHIP_ABBV1 ");
//                }
                stb.append("FROM    SCHREG_REGD_DAT W1 ");
                stb.append("INNER  JOIN SCHREG_REGD_HDAT  W6 ON W6.YEAR = W1.YEAR AND W6.SEMESTER = W1.SEMESTER AND W6.GRADE = W1.GRADE AND W6.HR_CLASS = W1.HR_CLASS ");
                stb.append("INNER  JOIN V_SEMESTER_GRADE_MST    W2 ON W2.YEAR = W1.YEAR AND W2.SEMESTER = '" + param._semester + "' AND W2.GRADE = W1.GRADE ");
                stb.append("INNER  JOIN SCHREG_BASE_MST W3 ON W3.SCHREGNO = W1.SCHREGNO ");
                stb.append("LEFT   JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = W1.YEAR AND GDAT.GRADE = W1.GRADE ");
                stb.append("LEFT   JOIN SCHREG_ENT_GRD_HIST_DAT W4 ON W4.SCHREGNO = W1.SCHREGNO ");
                stb.append("                              AND W4.SCHOOL_KIND = GDAT.SCHOOL_KIND ");
                stb.append("                              AND ((W4.GRD_DIV IN('2','3') AND W4.GRD_DATE <= W2.EDATE) ");
                stb.append("                                OR (W4.ENT_DIV IN('4','5') AND W4.ENT_DATE >= W2.SDATE)) ");
                stb.append("LEFT   JOIN SCHREG_TRANSFER_DAT W5 ON W5.SCHREGNO = W1.SCHREGNO ");
                stb.append("                                  AND (W5.TRANSFERCD IN ('1','2') AND W2.EDATE BETWEEN W5.TRANSFER_SDATE AND W5.TRANSFER_EDATE) ");
//                if (param._isTokiwagi) {
//                    stb.append("     LEFT JOIN SEMESTER_MST SEME ON SEME.YEAR = W1.YEAR AND SEME.SEMESTER = W1.SEMESTER ");
//                    stb.append("     LEFT JOIN SCHREG_SCHOLARSHIP_HIST_DAT TSCHOL ON TSCHOL.SCHREGNO = W1.SCHREGNO ");
//                    stb.append("          AND ( ");
//                    stb.append("              TSCHOL.FROM_DATE BETWEEN SEME.SDATE AND SEME.EDATE ");
//                    stb.append("           OR TSCHOL.TO_DATE BETWEEN SEME.SDATE AND SEME.EDATE ");
//                    stb.append("           OR SEME.SDATE BETWEEN TSCHOL.FROM_DATE AND VALUE(TSCHOL.TO_DATE, '9999-12-31') ");
//                    stb.append("           OR SEME.EDATE BETWEEN TSCHOL.FROM_DATE AND VALUE(TSCHOL.TO_DATE, '9999-12-31') ");
//                    stb.append("              ) ");
//                    stb.append("          AND TSCHOL.SCHOLARSHIP IS NOT NULL ");
//                    stb.append("     LEFT JOIN V_NAME_MST NMA044 ON NMA044.YEAR = W1.YEAR ");
//                    stb.append("          AND NMA044.NAMECD1 = 'A044' ");
//                    stb.append("          AND NMA044.NAMECD2 = TSCHOL.SCHOLARSHIP ");
//                }
                stb.append("WHERE   W1.YEAR = '" + param._year + "' ");
                stb.append("    AND W1.SCHREGNO = ? ");
                if (!SEMEALL.equals(param._semester)) {
                    stb.append("AND W1.SEMESTER = '" + param._semester + "' ");
                } else {
                    stb.append("AND W1.SEMESTER = '" + param._semeFlg + "' ");
                }

                ps = db2.prepareStatement(stb.toString());

                for (final Iterator it = _students.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    final Map rs = KnjDbUtils.firstRow(KnjDbUtils.query(db2, ps, new Object[] {student._schregno}));
                    if (!rs.isEmpty()) {
                        TransInfo transInfo = null;
                        final String d1 = KnjDbUtils.getString(rs, "KBN_DATE1");
                        final String d2 = KnjDbUtils.getString(rs, "KBN_DATE2");
                        if (null != d1) {
                            final String n1 = KnjDbUtils.getString(rs, "KBN_NAME1");
                            final String dateStr = KNJ_EditDate.h_format_JP(db2, d1);
                            transInfo = new TransInfo(d1, dateStr, n1);
                        } else if (null != d2) {
                            final String n2 = KnjDbUtils.getString(rs, "KBN_NAME2");
                            final String dateStr = KNJ_EditDate.h_format_JP(db2, d2);
                            transInfo = new TransInfo(d2, dateStr, n2);
                        }
                        if (null == transInfo) {
                            transInfo = new TransInfo(null, null, null);
                        }
                        student._attendNo = KnjDbUtils.getString(rs, "ATTENDNO");
                        student._name = KnjDbUtils.getString(rs, "NAME");
                        student._transInfo = transInfo;
//                        if (param._isTokiwagi) {
//                            student._scholarshipName = null == KnjDbUtils.getString(rs, "SCHOLARSHIP_ABBV1") ? "" : "(" + KnjDbUtils.getString(rs, "SCHOLARSHIP_ABBV1") + ")";
//                        }
                    }
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }

        private Student getStudent(String code) {
            if (code == null) {
                return null;
            }
            return (Student) _studentMap.get(code);
        }

        private void loadAttend(
                final DB2UDB db2,
                final Param param
        ) {
            PreparedStatement ps = null;
            try {
                final String psKey = "ATTENDSEMES";
                if (null == param._psMap.get(psKey)) {
                    param._attendParamMap.put("schregno", "?");
                    final String sql = AttendAccumulate.getAttendSemesSql(
                            param._year,
                            param._semester,
                            param._sdate,
                            param._date,
                            param._attendParamMap
                    );
                    log.debug(" sql = " + sql);

                    param._psMap.put(psKey, db2.prepareStatement(sql));
                }
                ps = (PreparedStatement) param._psMap.get(psKey);

                final Integer zero = new Integer(0);

                for (final Iterator it = _students.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    for (final Iterator rit = KnjDbUtils.query(db2, ps, new Object[] {student._schregno}).iterator(); rit.hasNext();) {
                    	final Map row = (Map) rit.next();
                        if (!SEMEALL.equals(KnjDbUtils.getString(row, "SEMESTER"))) {
                            continue;
                        }
                        final BigDecimal tochuKekkaBd = KnjDbUtils.getBigDecimal(row, "TOCHU_KEKKA", null);
                        final AttendInfo attendInfo = new AttendInfo(
                                KnjDbUtils.getInt(row, "LESSON", zero).intValue(),
                                KnjDbUtils.getInt(row, "MLESSON", zero).intValue(),
                                KnjDbUtils.getInt(row, "SUSPEND", zero).intValue(),
                                KnjDbUtils.getInt(row, "MOURNING", zero).intValue(),
                                KnjDbUtils.getInt(row, "SICK", zero).intValue(),
                                KnjDbUtils.getInt(row, "PRESENT", zero).intValue(),
                                KnjDbUtils.getInt(row, "LATE", zero).intValue(),
                                KnjDbUtils.getInt(row, "EARLY", zero).intValue(),
                                KnjDbUtils.getInt(row, "TRANSFER_DATE", zero).intValue(),
                                null == tochuKekkaBd ? new Double(0) : new Double(tochuKekkaBd.doubleValue())
                        );
                        student._attendInfo = attendInfo;
                    }
                }
            } catch (Exception e) {
                log.error("Exception", e);
            }
        }

        private void loadHrclassAverage(
                final DB2UDB db2,
                final Param param,
                final String courseCd
        ) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String course = SQLUtils.whereIn(true, toArray(_courseSet));
                final String major = SQLUtils.whereIn(true, toArray(_majorSet));
                if (course == null || major == null) {
                    log.debug("warning:PARAMETER(couse or major) is NULL .");
                    return;
                }
                final String sql = sqlHrclassAverage(param, _hrclassCd, course, major, courseCd);
                log.debug(" avg sql = " + sql);
                ps = db2.prepareStatement(sql);

                rs = ps.executeQuery();
                while (rs.next()) {
                    final String avgTotal = rs.getString("AVG_HR_TOTAL");
                    final String avgAveage = rs.getString("AVG_HR_AVERAGE");
                    if ("HR".equals(rs.getString("FLG"))) {
                        _avgHrTotal = avgTotal;
                        _avgHrAverage = avgAveage;
                        _avgHrCount = rs.getInt("COUNT");
                    } else if ("GRADE".equals(rs.getString("FLG"))) {
                        _avgGradeTotal = avgTotal;
                        _avgGradeAverage = avgAveage;
                    } else if ("COURSE".equals(rs.getString("FLG"))) {
                        _avgCourseTotal = avgTotal;
                        _avgCourseAverage = avgAveage;
                    } else if ("MAJOR".equals(rs.getString("FLG"))) {
                        _avgMajorTotal = avgTotal;
                        _avgMajorAverage = avgAveage;
                    }
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        private String[] toArray(final Set set) {
            final List list = new ArrayList(set);
            final String[] arr = new String[set.size()];
            for (int i = 0; i < list.size(); i++) {
                arr[i] = (String) list.get(i);
            }
            return arr;
        }

        /**
         * SQL 総合点・平均点の学級平均を取得するSQL
         */
        private String sqlHrclassAverage(final Param param, final String hrClass, final String course, final String major, final String courseCd) {
            final StringBuffer stb = new StringBuffer();

            stb.append("WITH ");

            //対象生徒の表 クラスの生徒
            stb.append(" SCHNO_A AS(");
            stb.append("     SELECT  W1.SCHREGNO,W1.SEMESTER ");
            stb.append("            ,W1.GRADE,W1.COURSECD, W1.MAJORCD, W1.COURSECODE ");
            stb.append("            , 0 AS LEAVE ");
            stb.append("            ,CASE WHEN W1.GRADE||W1.HR_CLASS = '" + hrClass + "' THEN '1' ELSE '0' END AS IS_HR ");
            stb.append("            ,CASE WHEN W1.COURSECD||W1.MAJORCD||W1.COURSECODE IN " + course + " THEN '1' ELSE '0' END AS IS_COURSE ");
            stb.append("            ,CASE WHEN W1.COURSECD||W1.MAJORCD IN " + major + " THEN '1' ELSE '0' END AS IS_MAJOR ");
            stb.append("     FROM    SCHREG_REGD_DAT W1 ");
            stb.append("     INNER JOIN V_SEMESTER_GRADE_MST W2 ON W2.YEAR = W1.YEAR AND W2.SEMESTER = W1.SEMESTER AND W2.GRADE = W1.GRADE ");

            stb.append("     WHERE   W1.YEAR = '" + param._year + "' ");
            if (SEMEALL.equals(param._semester)) {
                stb.append("     AND W1.SEMESTER = '" + param._semeFlg + "' ");
            } else {
                stb.append("     AND W1.SEMESTER = '" + param._semester + "' ");
                stb.append("     AND NOT EXISTS(SELECT 'X'  FROM  SCHREG_BASE_MST S1");
                stb.append("                    WHERE S1.SCHREGNO = W1.SCHREGNO AND S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < W2.SDATE) ");
            }
            stb.append("         AND W1.GRADE = '" + hrClass.substring(0, 2) + "' ");
            stb.append(") ");

            stb.append("SELECT 'HR' AS FLG ");
            stb.append("       ,DECIMAL(ROUND(AVG(FLOAT(W3.SCORE))*10,0)/10,5,1) AS AVG_HR_TOTAL ");
            stb.append("       ,DECIMAL(ROUND(AVG(FLOAT(W3.AVG))*10,0)/10,5,1) AS AVG_HR_AVERAGE ");
            stb.append("       ,COUNT(W3.SCORE) AS COUNT ");
            stb.append("  FROM  RECORD_RANK_SDIV_DAT W3 ");
            stb.append(" WHERE  W3.YEAR = '" + param._year + "' ");
            stb.append("   AND  W3.SEMESTER = '" + param._semester + "' ");
            stb.append("   AND  W3.TESTKINDCD || W3.TESTITEMCD || W3.SCORE_DIV = '" + param._testKindCd + "' ");
            stb.append("   AND  W3.SUBCLASSCD = '" + param.SUBCLASSCD999999 + "' ");
            stb.append("   AND  EXISTS(SELECT 'X' FROM SCHNO_A W1 ");
            stb.append("                WHERE W3.SCHREGNO = W1.SCHREGNO AND W1.LEAVE = 0 AND W1.IS_HR = '1') ");
            stb.append("UNION ALL ");
            stb.append("SELECT 'GRADE' AS FLG ");
            stb.append("       ,DECIMAL(ROUND(AVG(FLOAT(W3.SCORE))*10,0)/10,5,1) AS AVG_HR_TOTAL ");
            stb.append("       ,DECIMAL(ROUND(AVG(FLOAT(W3.AVG))*10,0)/10,5,1) AS AVG_HR_AVERAGE ");
            stb.append("       ,COUNT(W3.SCORE) AS COUNT ");
            stb.append("  FROM  RECORD_RANK_SDIV_DAT W3 ");
            stb.append(" WHERE  W3.YEAR = '" + param._year + "' ");
            stb.append("   AND  W3.SEMESTER = '" + param._semester + "' ");
            stb.append("   AND  W3.TESTKINDCD || W3.TESTITEMCD || W3.SCORE_DIV = '" + param._testKindCd + "' ");
            stb.append("   AND  W3.SUBCLASSCD = '" + param.SUBCLASSCD999999 + "' ");
            stb.append("   AND  EXISTS(SELECT 'X' FROM SCHNO_A W1 ");
            stb.append("                WHERE W3.SCHREGNO = W1.SCHREGNO AND W1.LEAVE = 0) ");
            stb.append("UNION ALL ");
            stb.append("SELECT 'COURSE' AS FLG ");
            stb.append("       ,DECIMAL(ROUND(AVG(FLOAT(W3.SCORE))*10,0)/10,5,1) AS AVG_HR_TOTAL ");
            stb.append("       ,DECIMAL(ROUND(AVG(FLOAT(W3.AVG))*10,0)/10,5,1) AS AVG_HR_AVERAGE ");
            stb.append("       ,COUNT(W3.SCORE) AS COUNT ");
            stb.append("  FROM  RECORD_RANK_SDIV_DAT W3 ");
            stb.append(" WHERE  W3.YEAR = '" + param._year + "' ");
            stb.append("   AND  W3.SEMESTER = '" + param._semester + "' ");
            stb.append("   AND  W3.TESTKINDCD || W3.TESTITEMCD || W3.SCORE_DIV = '" + param._testKindCd + "' ");
            stb.append("   AND  W3.SUBCLASSCD = '" + param.SUBCLASSCD999999 + "' ");
            stb.append("   AND  EXISTS(SELECT 'X' FROM SCHNO_A W1 ");
            stb.append("                WHERE W3.SCHREGNO = W1.SCHREGNO AND W1.LEAVE = 0 AND W1.IS_COURSE = '1') ");
            stb.append("UNION ALL ");
            stb.append("SELECT 'MAJOR' AS FLG ");
            stb.append("       ,DECIMAL(ROUND(AVG(FLOAT(W3.SCORE))*10,0)/10,5,1) AS AVG_HR_TOTAL ");
            stb.append("       ,DECIMAL(ROUND(AVG(FLOAT(W3.AVG))*10,0)/10,5,1) AS AVG_HR_AVERAGE ");
            stb.append("       ,COUNT(W3.SCORE) AS COUNT ");
            stb.append("  FROM  RECORD_RANK_SDIV_DAT W3 ");
            stb.append(" WHERE  W3.YEAR = '" + param._year + "' ");
            stb.append("   AND  W3.SEMESTER = '" + param._semester + "' ");
            stb.append("   AND  W3.TESTKINDCD || W3.TESTITEMCD || W3.SCORE_DIV = '" + param._testKindCd + "' ");
            stb.append("   AND  W3.SUBCLASSCD = '" + param.SUBCLASSCD999999 + "' ");
            stb.append("   AND  EXISTS(SELECT 'X' FROM SCHNO_A W1 ");
            stb.append("                WHERE W3.SCHREGNO = W1.SCHREGNO AND W1.LEAVE = 0 AND W1.IS_MAJOR = '1') ");

            return stb.toString();
        }

        private void loadRank(
                final DB2UDB db2,
                final Param param
        ) {
            PreparedStatement ps = null;

            try {
                final StringBuffer stb = new StringBuffer();

                stb.append("WITH ");

                //対象生徒の表 クラスの生徒
                stb.append(" SCHNO_A AS(");
                stb.append("     SELECT  W1.SCHREGNO,W1.SEMESTER ");
                stb.append("            ,W1.GRADE,W1.COURSECD, W1.MAJORCD, W1.COURSECODE ");
                stb.append("             , 0 AS LEAVE ");
                stb.append("     FROM    SCHREG_REGD_DAT W1 ");
                stb.append("     INNER JOIN V_SEMESTER_GRADE_MST W2 ON W2.YEAR = W1.YEAR AND W2.SEMESTER = W1.SEMESTER AND W2.GRADE = W1.GRADE ");

                stb.append("     WHERE   W1.YEAR = '" + param._year + "' ");
                if (SEMEALL.equals(param._semester)) {
                    stb.append("     AND W1.SEMESTER = '" + param._semeFlg + "' ");
                } else {
                    stb.append("     AND W1.SEMESTER = '" + param._semester + "' ");
                    stb.append("     AND NOT EXISTS(SELECT 'X'  FROM  SCHREG_BASE_MST S1");
                    stb.append("                    WHERE S1.SCHREGNO = W1.SCHREGNO AND S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < W2.SDATE) ");
                }
                stb.append("         AND W1.SCHREGNO = ? ");
                stb.append(") ");

                //メイン表
                stb.append("SELECT  W3.SCHREGNO ");
                if (OUTPUT_KJUN2.equals(param._outputKijun)) {
                    stb.append("   ,CLASS_AVG_RANK AS CLASS_RANK");
                    stb.append("   ,GRADE_AVG_RANK AS GRADE_RANK");
                    stb.append("   ,COURSE_AVG_RANK AS COURSE_RANK");
                } else if (OUTPUT_KJUN3.equals(param._outputKijun)) {
                    stb.append("   ,CLASS_DEVIATION_RANK AS CLASS_RANK");
                    stb.append("   ,GRADE_DEVIATION_RANK AS GRADE_RANK");
                    stb.append("   ,COURSE_DEVIATION_RANK AS COURSE_RANK");
                } else {
                    stb.append("   ,CLASS_RANK ");
                    stb.append("   ,GRADE_RANK ");
                    stb.append("   ,COURSE_RANK ");
                }
                stb.append("       ," + param._rankFieldName + "  AS TOTAL_RANK ");
                stb.append("       ,W3.SCORE AS TOTAL_SCORE ");
                stb.append("       ,DECIMAL(ROUND(FLOAT(W3.AVG)*10,0)/10,5,1) AS TOTAL_AVG ");
                stb.append("  FROM  RECORD_RANK_SDIV_DAT W3 ");
                stb.append(" WHERE  W3.YEAR = '" + param._year + "' ");
                stb.append("   AND  W3.SEMESTER = '" + param._semester + "' ");
                stb.append("   AND  W3.TESTKINDCD || W3.TESTITEMCD || W3.SCORE_DIV = '" + param._testKindCd + "' ");
                stb.append("   AND  W3.SUBCLASSCD = '" + param.SUBCLASSCD999999 + "' ");
                stb.append("   AND  EXISTS(SELECT 'X' FROM SCHNO_A W1 ");
                stb.append("                WHERE W3.SCHREGNO = W1.SCHREGNO AND W1.LEAVE = 0) ");

                ps = db2.prepareStatement(stb.toString());

                for (final Iterator it = _students.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();
                    ps.setString(1, student._schregno);

                    ResultSet rs = null;
                    try {
                        rs = ps.executeQuery();
                        if (rs.next()) {
                            student._classRank = rs.getInt("CLASS_RANK");
                            student._gradeRank = rs.getInt("GRADE_RANK");
                            student._courseRank = rs.getInt("COURSE_RANK");
                            student._rank = rs.getInt("TOTAL_RANK");
                            student._scoreSum = rs.getString("TOTAL_SCORE");
                            student._scoreAvg = rs.getString("TOTAL_AVG");
                        }
                    } catch (Exception e) {
                        log.error("Exception", e);
                    } finally {
                        DbUtils.closeQuietly(rs);
                    }
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }

        private void loadScoreDetail(
                final DB2UDB db2,
                final Param param
        ) {
            PreparedStatement ps = null;

            final Integer zero = new Integer(0);
            try {
                final String psKey = "SCORE_DETAIL";
                if (null == param._psMap.get(psKey)) {
                    final String sql = sqlStdSubclassDetail(param);
                    if (param._isOutputDebug) {
                        log.info(" subclass detail sql = " + sql);
                    } else {
                        log.debug(" subclass detail sql = " + sql);
                    }

                    param._psMap.put(psKey, db2.prepareStatement(sql));
                }
                ps = (PreparedStatement) param._psMap.get(psKey);


                for (final Iterator it = KnjDbUtils.query(db2, ps, new Object[] {_hrclassCd.substring(0, 2), _hrclassCd.substring(2)}).iterator(); it.hasNext();) {
                	final Map row = (Map) it.next();
                    if (param._enablePringFlg && "1".equals(KnjDbUtils.getString(row, "PRINT_FLG"))) {
                        continue;
                    }
                    final Student student = getStudent(KnjDbUtils.getString(row, "SCHREGNO"));
                    if (student == null) {
                        continue;
                    }

                    final String subclassCd = KnjDbUtils.getString(row, "SUBCLASSCD");
                    final String classCd = subclassCd == null ? "" : subclassCd.substring(1, 3);
                    if (classCd.compareTo(KNJDefineSchool.subject_U) <= 0 || classCd.equals(KNJDefineSchool.subject_T)) {
                        final ScoreDetail scoreDetail = new ScoreDetail(
                                getSubClass(row, _subclasses),
                                KnjDbUtils.getString(row, "SCORE"),
                                KnjDbUtils.getString(row, "SCORE_DI"),
                                KnjDbUtils.getString(row, "SUPP_SCORE"),
                                KnjDbUtils.getString(row, "ASSESS_LEVEL"),
                                KnjDbUtils.getString(row, "KARI_HYOUTEI"),
                                KnjDbUtils.getString(row, "PASS_SCORE"),
                                KnjDbUtils.getString(row, "PROV_FLG"),
                                (Integer) KnjDbUtils.getInt(row, "REPLACEMOTO", zero),
                                (String) KnjDbUtils.getString(row, "PRINT_FLG"),
                                KnjDbUtils.getString(row, "SLUMP"),
                                KnjDbUtils.getString(row, "SLUMP_MARK"),
                                KnjDbUtils.getString(row, "SLUMP_SCORE"),
                                (Integer) KnjDbUtils.getInt(row, "COMP_CREDIT", zero),
                                (Integer) KnjDbUtils.getInt(row, "GET_CREDIT", zero),
                                (Integer) KnjDbUtils.getInt(row, "CREDITS", zero)
                        );
                        student._scoreDetails.put(scoreDetail._subClass._subclasscode, scoreDetail);
                    }
                }
            } catch (Exception e) {
                log.error("Exception", e);
            }

            try {
                final String psKey = "ATTENDSUBCLASS";
                if (null == param._psMap.get(psKey)) {
                    param._attendParamMap.put("schregno", "?");

                    final String sql = AttendAccumulate.getAttendSubclassSql(
                            param._year,
                            param._semester,
                            param._sdate,
                            param._date,
                            param._attendParamMap
                            );
                    //log.debug(" attend subclass sql = " + sql);
                    param._psMap.put(psKey, db2.prepareStatement(sql));
                }
                ps = (PreparedStatement) param._psMap.get(psKey);

                log.info("load attendSubclass");
                long attendSubclassStart = System.currentTimeMillis();
                long attendSubclassAcc = 0;
                for (final Iterator sit = _students.iterator(); sit.hasNext();) {
                    final Student student = (Student) sit.next();

                    long attendSubclassAccStart = System.currentTimeMillis();
                    final List rowList = KnjDbUtils.query(db2, ps, new Object[] {student._schregno});
                    attendSubclassAcc += (System.currentTimeMillis() - attendSubclassAccStart);

                    for (final Iterator it2 = rowList.iterator(); it2.hasNext();) {
                    	final Map row = (Map) it2.next();
                        if (!SEMEALL.equals(KnjDbUtils.getString(row, "SEMESTER"))) {
                            continue;
                        }

                        ScoreDetail scoreDetail = null;
                        for (final Iterator it = student._scoreDetails.keySet().iterator(); it.hasNext();) {
                            final String subclasscd = (String) it.next();
                            if (subclasscd.substring(1).equals(KnjDbUtils.getString(row, "SUBCLASSCD"))) {
                                scoreDetail = (ScoreDetail) student._scoreDetails.get(subclasscd);
                                break;
                            }
                        }
                        if (null == scoreDetail) {
                            SubClass subClass = null;
                            for (final Iterator it = _subclasses.keySet().iterator(); it.hasNext();) {
                                final String subclasscd = (String) it.next();
                                if (subclasscd.substring(1).equals(KnjDbUtils.getString(row, "SUBCLASSCD"))) {
                                    subClass = (SubClass) _subclasses.get(subclasscd);
                                    scoreDetail = new ScoreDetail(subClass, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
                                    student._scoreDetails.put(subclasscd, scoreDetail);
                                    break;
                                }
                            }
                            if (null == scoreDetail) {
                                // log.fatal(" no detail " + student._schregno + ", " + KnjDbUtils.getString(row, "SUBCLASSCD"));
                                continue;
                            }
                        }

//                        final String specialGroupCd = KnjDbUtils.getString(row, "SPECIAL_GROUP_CD");
//                        final Integer specialAbsentMinutes = (Integer) KnjDbUtils.getInt(row, "SPECIAL_SICK_MINUTES3", null);
//                        if (specialGroupCd != null && specialAbsentMinutes != null) {
//                            if (!student._spGroupAbsentMinutes.containsKey(specialGroupCd)) {
//                                student._spGroupAbsentMinutes.put(specialGroupCd, new Integer(0));
//                            }
//                            int minute = ((Integer) student._spGroupAbsentMinutes.get(specialGroupCd)).intValue();
//                            student._spGroupAbsentMinutes.put(specialGroupCd, new Integer(minute + specialAbsentMinutes.intValue()));
//                        }


//                        if (param._isHagoromo) {
//                            final Integer integerMlesson = KnjDbUtils.getInt(row, "MLESSON", null);
//    						if (null != integerMlesson) {
//    							if (null == scoreDetail._subClass._jisu) {
//    								scoreDetail._subClass._jisu = integerMlesson;
//    							} else if (scoreDetail._subClass._jisu.intValue() < integerMlesson.intValue()) {
//                                    scoreDetail._subClass._jisu = integerMlesson;
//    							}
//    						}
//                        } else {
                            final Integer integerMlesson = KnjDbUtils.getInt(row, "MLESSON", zero);
    						if (0 != integerMlesson.intValue()) {
    							if (null == scoreDetail._subClass._jisu) {
    								scoreDetail._subClass._jisu = integerMlesson;
    							} else if (scoreDetail._subClass._jisu.intValue() < integerMlesson.intValue()) {
                                    scoreDetail._subClass._jisu = integerMlesson;
    							}
    						}
//                        }
                        scoreDetail._jisu = (Integer) KnjDbUtils.getInt(row, "MLESSON", null);
                        scoreDetail._absenceHigh = KnjDbUtils.getBigDecimal(row, "ABSENCE_HIGH", null);
                        
                        if (null != scoreDetail._replacemoto && scoreDetail._replacemoto.intValue() == -1) {
                            scoreDetail._ketsuji = Double.valueOf(KnjDbUtils.getString(row, "RAW_REPLACED_SICK"));
                        } else {
                            scoreDetail._ketsuji = Double.valueOf(KnjDbUtils.getString(row, "SICK1"));
                            if (null != scoreDetail._ketsuji) {
                                student._totalKekka += scoreDetail._ketsuji.doubleValue();
                            }
                        }
                        scoreDetail._isOver = scoreDetail.judgeOver(scoreDetail._ketsuji, scoreDetail._jisu);
                    }
                }
                long attendSubclassEnd = System.currentTimeMillis();
                log.info(" load attendSubclass elapsed time = " + (attendSubclassEnd - attendSubclassStart) + "[ms] ( query time = " + attendSubclassAcc + "[ms] / student count = " + _students.size() + ")");

            } catch (Exception e) {
                log.error("Exception", e);
            }
        }

        /**
         *  PrepareStatement作成 --> 成績・評定・欠時データの表
         */
        private String sqlStdSubclassDetail(final Param param) {
            final StringBuffer stb = new StringBuffer();

            stb.append("WITH ");

            //対象生徒の表 クラスの生徒
            stb.append(" SCHNO_A AS(");
            stb.append("     SELECT  W1.SCHREGNO,W1.YEAR,W1.SEMESTER ");
            stb.append("            ,W1.GRADE, W1.HR_CLASS, W1.COURSECD, W1.MAJORCD, W1.COURSECODE ");
            stb.append("             , 0 AS LEAVE ");
            stb.append("     FROM    SCHREG_REGD_DAT W1 ");
            stb.append("     INNER JOIN V_SEMESTER_GRADE_MST W2 ON W2.YEAR = W1.YEAR AND W2.SEMESTER = W1.SEMESTER AND W2.GRADE = W1.GRADE ");

            stb.append("     WHERE   W1.YEAR = '" + param._year + "' ");
            if (SEMEALL.equals(param._semester)) {
                stb.append("     AND W1.SEMESTER = '" + param._semeFlg + "' ");
            } else {
                stb.append("     AND W1.SEMESTER = '" + param._semester + "' ");
                stb.append("     AND NOT EXISTS(SELECT 'X'  FROM  SCHREG_BASE_MST S1");
                stb.append("                    WHERE S1.SCHREGNO = W1.SCHREGNO AND S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < W2.SDATE) ");
            }
            stb.append("         AND W1.GRADE = ? AND W1.HR_CLASS = ? ");
            stb.append(") ");

            //対象講座の表
            stb.append(",CHAIR_A AS(");
            stb.append("     SELECT W1.SCHREGNO, W2.CHAIRCD, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(" W2.CLASSCD, W2.SCHOOL_KIND, W2.CURRICULUM_CD, ");
                stb.append(" W2.CLASSCD || '-' || W2.SCHOOL_KIND || '-' || W2.CURRICULUM_CD || '-' || ");
            }
            stb.append("            W2.SUBCLASSCD AS SUBCLASSCD, W2.SEMESTER, W1.APPDATE, W1.APPENDDATE ");
            stb.append("     FROM   CHAIR_STD_DAT W1 ");
            stb.append("     INNER JOIN CHAIR_DAT W2 ON W2.YEAR = W1.YEAR ");
            stb.append("         AND W2.SEMESTER = W1.SEMESTER ");
            stb.append("         AND W2.CHAIRCD = W1.CHAIRCD ");
            stb.append("     WHERE  W1.YEAR = '" + param._year + "' ");
            stb.append("        AND W1.SEMESTER <= '" + param._semester + "' ");
            stb.append("        AND EXISTS(SELECT 'X' FROM SCHNO_A W3 WHERE W3.SCHREGNO = W1.SCHREGNO)");
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

            stb.append(",CREDITS_A AS(");
            stb.append("    SELECT  SCHREGNO, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("            CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || ");
            }
            stb.append("            SUBCLASSCD AS SUBCLASSCD, CREDITS ");
            stb.append("    FROM    CREDIT_MST T1, SCHNO_A T2 ");
            stb.append("    WHERE   T1.YEAR = '" + param._year + "' ");
            stb.append("        AND T1.GRADE = T2.GRADE ");
            stb.append("        AND T1.COURSECD = T2.COURSECD ");
            stb.append("        AND T1.MAJORCD = T2.MAJORCD ");
            stb.append("        AND T1.COURSECODE = T2.COURSECODE ");
            stb.append("        AND EXISTS(SELECT 'X' FROM CHAIR_A T3 WHERE T3.SUBCLASSCD = ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("            T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
            }
            stb.append("                    T1.SUBCLASSCD AND T3.SCHREGNO = T2.SCHREGNO)");
            stb.append(") ");

            // 単位数の表
            stb.append(",CREDITS_B AS(");
            stb.append("    SELECT  T1.SCHREGNO, T1.SUBCLASSCD, T1.CREDITS");
            stb.append("    FROM    CREDITS_A T1");
            stb.append("    WHERE   NOT EXISTS(SELECT 'X' FROM SUBCLASS_REPLACE_COMBINED_DAT T2 ");
            stb.append("                       WHERE  T2.YEAR = '" + param._year + "' ");
            stb.append("                          AND T2.CALCULATE_CREDIT_FLG = '2'");
            stb.append("                          AND ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("            T2.COMBINED_CLASSCD || '-' || T2.COMBINED_SCHOOL_KIND || '-' || T2.COMBINED_CURRICULUM_CD || '-' || ");
            }
            stb.append("                              T2.COMBINED_SUBCLASSCD = T1.SUBCLASSCD)");
            stb.append("    UNION SELECT T1.SCHREGNO, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("            T2.COMBINED_CLASSCD || '-' || T2.COMBINED_SCHOOL_KIND || '-' || T2.COMBINED_CURRICULUM_CD || '-' || ");
            }
            stb.append("                 COMBINED_SUBCLASSCD AS SUBCLASSCD, SUM(T1.CREDITS) AS CREDITS");
            stb.append("    FROM    CREDITS_A T1, SUBCLASS_REPLACE_COMBINED_DAT T2 ");
            stb.append("    WHERE   T2.YEAR = '" + param._year + "' ");
            stb.append("        AND T2.CALCULATE_CREDIT_FLG = '2'");
            stb.append("        AND ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("            T2.ATTEND_CLASSCD || '-' || T2.ATTEND_SCHOOL_KIND || '-' || T2.ATTEND_CURRICULUM_CD || '-' || ");
            }
            stb.append("            T2.ATTEND_SUBCLASSCD = T1.SUBCLASSCD");
            stb.append("    GROUP BY SCHREGNO, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("            COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || ");
            }
            stb.append("             COMBINED_SUBCLASSCD");
            stb.append(") ");

            stb.append("   , REL_COUNT AS (");
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

            //成績データの表（通常科目）
            stb.append(",RECORD_REC AS(");
            stb.append("    SELECT  W3.SCHREGNO, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("            W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || ");
            }
            stb.append("     W3.SUBCLASSCD AS SUBCLASSCD ");
            stb.append("     , W3.SCORE ");
            stb.append("     , CASE WHEN VALUE(T2.COUNT, 0) > 0 THEN ");
            stb.append("          (SELECT MAX(L3.ASSESSLEVEL) ");
            stb.append("           FROM RELATIVEASSESS_MST L3 ");
            stb.append("           WHERE L3.GRADE = '" + param._grade + "' AND L3.ASSESSCD = '3' ");
            stb.append("             AND W3.SCORE BETWEEN L3.ASSESSLOW AND L3.ASSESSHIGH  ");
            stb.append("             AND L3.SUBCLASSCD = W3.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     AND L3.CLASSCD = W3.CLASSCD ");
                stb.append("     AND L3.SCHOOL_KIND = W3.SCHOOL_KIND ");
                stb.append("     AND L3.CURRICULUM_CD = W3.CURRICULUM_CD ");
            }
            stb.append("          ) ELSE ");
            stb.append("          (SELECT MAX(L3.ASSESSLEVEL) ");
            stb.append("           FROM ASSESS_MST L3 ");
            stb.append("           WHERE L3.ASSESSCD = '3' ");
            stb.append("             AND W3.SCORE BETWEEN L3.ASSESSLOW AND L3.ASSESSHIGH  ");
            stb.append("          ) ");
            stb.append("         END AS ASSESS_LEVEL ");
            stb.append("    FROM    RECORD_RANK_SDIV_DAT W3 ");
            stb.append("    INNER JOIN SCHNO_A W1 ON W3.SCHREGNO = W1.SCHREGNO ");
            stb.append("       AND W1.LEAVE = 0 ");
            stb.append("        LEFT JOIN REL_COUNT T2 ON T2.SUBCLASSCD = W3.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     AND T2.CLASSCD = W3.CLASSCD ");
                stb.append("     AND T2.SCHOOL_KIND = W3.SCHOOL_KIND ");
                stb.append("     AND T2.CURRICULUM_CD = W3.CURRICULUM_CD ");
            }
            stb.append("    WHERE   W3.YEAR = '" + param._year + "' AND ");
            stb.append("            W3.SEMESTER = '" + param._semester + "' AND ");
            stb.append("            W3.TESTKINDCD || W3.TESTITEMCD || W3.SCORE_DIV = '" + param._testKindCd + "' ");
            stb.append("     ) ");

            //成績データの表（通常科目）
            stb.append(",RECORD_SCORE AS(");
            stb.append("    SELECT  W3.SCHREGNO, ");
            stb.append("            W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || W3.SUBCLASSCD AS SUBCLASSCD ");
            stb.append("           ,W3.COMP_CREDIT ");
            stb.append("           ,W3.GET_CREDIT ");
            stb.append("    FROM    RECORD_SCORE_DAT W3 ");
            stb.append("    WHERE   W3.YEAR = '" + param._year + "' AND ");
            stb.append("            W3.SEMESTER = '" + param._semester + "' AND ");
            stb.append("            W3.TESTKINDCD || W3.TESTITEMCD || W3.SCORE_DIV = '" + param._testKindCd + "' AND ");
            stb.append("            EXISTS(SELECT 'X' FROM SCHNO_A W1 ");
            stb.append("                   WHERE W3.SCHREGNO = W1.SCHREGNO AND W1.LEAVE = 0) ");
            stb.append("     ) ");

            //仮評定の表
            stb.append(",RECORD_KARI_HYOUTEI AS(");
            stb.append("    SELECT  W3.SCHREGNO, ");
            stb.append("            W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || W3.SUBCLASSCD AS SUBCLASSCD ");
            stb.append("     , W3.SCORE AS KARI_HYOUTEI ");
            stb.append("     , T2.PROV_FLG ");
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
            stb.append("    WHERE   W3.YEAR = '" + param._year + "' AND ");
            stb.append("            W3.SEMESTER = '" + param._semester + "' AND ");
            stb.append("            W3.TESTKINDCD || W3.TESTITEMCD = '" + param._testKindCd.substring(0, 4) + "' AND ");
            stb.append("            W3.SCORE_DIV = '" + SCORE_DIV_09 + "' ");
            stb.append("     ) ");

            //成績不振科目データの表
            stb.append(",RECORD_SLUMP AS(");
            stb.append("    SELECT  W3.SCHREGNO, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("            W3.CLASSCD || '-' || W3.SCHOOL_KIND || '-' || W3.CURRICULUM_CD || '-' || ");
            }
            stb.append("            W3.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("            CASE WHEN W1.SIDOU_INPUT = '1' THEN W3.SLUMP END AS SLUMP, ");
            stb.append("            CASE WHEN W1.SIDOU_INPUT = '1' AND W1.SIDOU_INPUT_INF = '1' THEN W3.MARK END AS SLUMP_MARK, ");
            stb.append("            CASE WHEN W1.SIDOU_INPUT = '1' AND W1.SIDOU_INPUT_INF = '2' THEN ");
            stb.append("         CASE WHEN VALUE(T2.COUNT, 0) > 0 THEN ");
            stb.append("          (SELECT MAX(L3.ASSESSLEVEL) ");
            stb.append("           FROM RELATIVEASSESS_MST L3 ");
            stb.append("           WHERE L3.GRADE = '" + param._grade + "' AND L3.ASSESSCD = '3' ");
            stb.append("             AND W3.SCORE BETWEEN L3.ASSESSLOW AND L3.ASSESSHIGH  ");
            stb.append("             AND L3.SUBCLASSCD = W3.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     AND L3.CLASSCD = W3.CLASSCD ");
                stb.append("     AND L3.SCHOOL_KIND = W3.SCHOOL_KIND ");
                stb.append("     AND L3.CURRICULUM_CD = W3.CURRICULUM_CD ");
            }
            stb.append("          ) ELSE ");
            stb.append("          (SELECT MAX(L3.ASSESSLEVEL) ");
            stb.append("           FROM ASSESS_MST L3 ");
            stb.append("           WHERE L3.ASSESSCD = '3' ");
            stb.append("             AND W3.SCORE BETWEEN L3.ASSESSLOW AND L3.ASSESSHIGH  ");
            stb.append("          ) ");
            stb.append("         END ");
            stb.append("        END AS SLUMP_SCORE ");
            stb.append("    FROM    RECORD_SLUMP_SDIV_DAT W3 ");
            stb.append("    INNER JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV W1 ON W1.YEAR = W3.YEAR ");
            stb.append("            AND W3.SEMESTER = W1.SEMESTER ");
            stb.append("            AND W3.TESTKINDCD = W1.TESTKINDCD ");
            stb.append("            AND W3.TESTITEMCD = W1.TESTITEMCD ");
            stb.append("            AND W3.SCORE_DIV = W1.SCORE_DIV ");
            stb.append("        LEFT JOIN REL_COUNT T2 ON T2.SUBCLASSCD = W3.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("     AND T2.CLASSCD = W3.CLASSCD ");
                stb.append("     AND T2.SCHOOL_KIND = W3.SCHOOL_KIND ");
                stb.append("     AND T2.CURRICULUM_CD = W3.CURRICULUM_CD ");
            }
            stb.append("    WHERE   W3.YEAR = '" + param._year + "' AND ");
            stb.append("            W3.SEMESTER = '" + param._semester + "' AND ");
            stb.append("            W3.TESTKINDCD || W3.TESTITEMCD || W3.SCORE_DIV = '" + param._testKindCd + "' AND ");
            stb.append("            EXISTS(SELECT 'X' FROM SCHNO_A W1 ");
            stb.append("                   WHERE W3.SCHREGNO = W1.SCHREGNO AND W1.LEAVE = 0) ");
            stb.append("     ) ");

            stb.append(" ,CHAIR_A2 AS ( ");
            stb.append("     SELECT  W2.SCHREGNO, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(" W2.CLASSCD, W2.SCHOOL_KIND, W2.CURRICULUM_CD, ");
            }
            stb.append("             W2.SUBCLASSCD, ");
            stb.append("             MIN(W22.STAFFCD) AS STAFFCD ");
            stb.append("     FROM    CHAIR_A W2");
            stb.append("     LEFT JOIN CHAIR_STF W22 ON W22.SEMESTER = W2.SEMESTER ");
            stb.append("         AND W22.CHAIRCD = W2.CHAIRCD ");
            if (!SEMEALL.equals(param._semester)) {
                stb.append(" WHERE   W2.SEMESTER = '" + param._semester + "'");
            }
            stb.append("     GROUP BY W2.SCHREGNO,");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(" W2.CLASSCD, W2.SCHOOL_KIND, W2.CURRICULUM_CD, ");
            }
            stb.append("              W2.SUBCLASSCD");
            stb.append(" ) ");

            stb.append(" ,CHAIR_A3 AS ( ");
            stb.append("     SELECT  W3.SCHREGNO, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(" W3.CLASSCD, W3.SCHOOL_KIND, W3.CURRICULUM_CD, ");
            }
            stb.append("             W3.SUBCLASSCD, ");
            stb.append("             MIN(W33.STAFFCD) AS STAFFCD ");
            stb.append("     FROM    CHAIR_A W3");
            stb.append("     LEFT JOIN CHAIR_A2 WA2 ON WA2.SCHREGNO = W3.SCHREGNO AND WA2.SUBCLASSCD = W3.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(" AND WA2.CLASSCD = W3.CLASSCD AND WA2.SCHOOL_KIND = W3.SCHOOL_KIND AND WA2.CURRICULUM_CD = W3.CURRICULUM_CD ");
            }
            stb.append("     LEFT JOIN CHAIR_STF W33 ON W33.SEMESTER = W3.SEMESTER ");
            stb.append("         AND W33.CHAIRCD = W3.CHAIRCD ");
            stb.append(" WHERE WA2.STAFFCD IS NOT NULL AND W33.STAFFCD <> WA2.STAFFCD ");
            if (!SEMEALL.equals(param._semester)) {
                stb.append("    AND W3.SEMESTER = '" + param._semester + "'");
            }
            stb.append("     GROUP BY W3.SCHREGNO,");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(" W3.CLASSCD, W3.SCHOOL_KIND, W3.CURRICULUM_CD, ");
            }
            stb.append("              W3.SUBCLASSCD");
            stb.append(" ) ");
            stb.append(" ,CHAIR_A4 AS ( ");
            stb.append("     SELECT  W4.SCHREGNO, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(" W4.CLASSCD, W4.SCHOOL_KIND, W4.CURRICULUM_CD, ");
            }
            stb.append("             W4.SUBCLASSCD, ");
            stb.append("             MIN(W44.STAFFCD) AS STAFFCD ");
            stb.append("     FROM    CHAIR_A W4");
            stb.append("       LEFT JOIN CHAIR_A2 WA2 ON WA2.SCHREGNO = W4.SCHREGNO ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(" AND WA2.CLASSCD = W4.CLASSCD AND WA2.SCHOOL_KIND = W4.SCHOOL_KIND AND WA2.CURRICULUM_CD = W4.CURRICULUM_CD ");
            }
            stb.append("         AND WA2.SUBCLASSCD = W4.SUBCLASSCD ");
            stb.append("       LEFT JOIN CHAIR_A3 WA3 ON WA3.SCHREGNO = W4.SCHREGNO ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(" AND WA3.CLASSCD = W4.CLASSCD AND WA3.SCHOOL_KIND = W4.SCHOOL_KIND AND WA3.CURRICULUM_CD = W4.CURRICULUM_CD ");
            }
            stb.append("         AND WA3.SUBCLASSCD = W4.SUBCLASSCD ");
            stb.append("       LEFT JOIN CHAIR_STF W44 ON W44.SEMESTER = W4.SEMESTER ");
            stb.append("         AND W44.CHAIRCD = W4.CHAIRCD ");
            stb.append("     WHERE WA2.STAFFCD IS NOT NULL AND W44.STAFFCD <> WA3.STAFFCD AND W44.STAFFCD <> WA2.STAFFCD ");
            if (!SEMEALL.equals(param._semester)) {
                stb.append(" AND   W4.SEMESTER = '" + param._semester + "'");
            }
            stb.append("     GROUP BY W4.SCHREGNO,");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(" W4.CLASSCD, W4.SCHOOL_KIND, W4.CURRICULUM_CD, ");
            }
            stb.append("              W4.SUBCLASSCD");
            stb.append(" ) ");
            stb.append(" ,COMBINED_SUBCLASS AS ( ");
            stb.append("    SELECT ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("            COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || ");
            }
            stb.append("           COMBINED_SUBCLASSCD AS SUBCLASSCD");
            stb.append("    FROM   SUBCLASS_REPLACE_COMBINED_DAT");
            stb.append("    WHERE  YEAR = '" + param._year + "'");
            stb.append("    GROUP BY ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("            COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || ");
            }
            stb.append("           COMBINED_SUBCLASSCD");
            stb.append(" ) ");

            stb.append(" ,ATTEND_SUBCLASS AS ( ");
            stb.append("    SELECT ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("            ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ");
            }
            stb.append("           ATTEND_SUBCLASSCD AS SUBCLASSCD, MAX(PRINT_FLG2) AS PRINT_FLG");
            stb.append("    FROM   SUBCLASS_REPLACE_COMBINED_DAT");
            stb.append("    WHERE  YEAR = '" + param._year + "'");
            stb.append("    GROUP BY ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("            ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ");
            }
            stb.append("           ATTEND_SUBCLASSCD");
            stb.append(" ) ");

            //メイン表
            stb.append(" SELECT  value(SUBM.ELECTDIV,'0') || T1.SUBCLASSCD AS SUBCLASSCD,T1.SCHREGNO ");
            stb.append("        ,T3.SCORE AS SCORE ");
            stb.append("        ,W4.VALUE_DI AS SCORE_DI ");
            stb.append("        ,T3.ASSESS_LEVEL ");
            stb.append("        ,SUPP.SCORE AS SUPP_SCORE ");
            stb.append("        ,T33.COMP_CREDIT ");
            stb.append("        ,T33.GET_CREDIT ");
            stb.append("        ,KARIHYO.KARI_HYOUTEI ");
            stb.append("        ,KARIHYO.PROV_FLG ");
            stb.append("        ,T11.CREDITS ");
            if (param._isPrintPerfect) {
                stb.append("        ,VALUE(PERF.PERFECT, 100) as PERFECT ");
            } else {
                stb.append("        ,case when PERF.DIV IS NULL then 100 else PERF.PERFECT end as PERFECT ");
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
            stb.append("    , STFM2.STAFFNAME AS STAFF2 ");
            stb.append("    , STFM3.STAFFNAME AS STAFF3 ");
            //対象生徒・講座の表
            stb.append(" FROM CHAIR_A2 T1 ");
            stb.append(" INNER JOIN SCHNO_A SCH ON SCH.SCHREGNO = T1.SCHREGNO ");
            //成績の表
            stb.append(" LEFT JOIN RECORD_REC T3 ON T3.SUBCLASSCD = T1.SUBCLASSCD AND T3.SCHREGNO = T1.SCHREGNO");
            stb.append(" LEFT JOIN RECORD_SCORE T33 ON T33.SUBCLASSCD = T1.SUBCLASSCD AND T33.SCHREGNO = T1.SCHREGNO");
            stb.append(" LEFT JOIN RECORD_KARI_HYOUTEI KARIHYO ON KARIHYO.SUBCLASSCD = T1.SUBCLASSCD AND KARIHYO.SCHREGNO = T1.SCHREGNO");
            //合併先科目の表
            stb.append(" LEFT JOIN COMBINED_SUBCLASS T9 ON T9.SUBCLASSCD = T1.SUBCLASSCD");
            //合併元科目の表
            stb.append(" LEFT JOIN ATTEND_SUBCLASS T10 ON T10.SUBCLASSCD = T1.SUBCLASSCD");

            stb.append(" LEFT JOIN CREDITS_A T6 ON T6.SCHREGNO = T1.SCHREGNO AND T6.SUBCLASSCD = T1.SUBCLASSCD");
            stb.append(" LEFT JOIN CREDITS_B T11 ON T11.SUBCLASSCD = T1.SUBCLASSCD AND T11.SCHREGNO = T1.SCHREGNO");
            stb.append(" LEFT JOIN SUBCLASS_MST SUBM ON SUBM.CLASSCD || '-' || SUBM.SCHOOL_KIND || '-' || SUBM.CURRICULUM_CD || '-' || SUBM.SUBCLASSCD = T1.SUBCLASSCD");
            stb.append(" LEFT JOIN CLASS_MST CM ON CM.CLASSCD || '-' || CM.SCHOOL_KIND = T1.CLASSCD || '-' || T1.SCHOOL_KIND ");
            //成績不振科目データの表
            stb.append(" LEFT JOIN RECORD_SLUMP K1 ON K1.SCHREGNO = T1.SCHREGNO AND K1.SUBCLASSCD = T1.SUBCLASSCD");
            stb.append(" LEFT JOIN STAFF_MST STFM ON STFM.STAFFCD = T1.STAFFCD ");
            stb.append(" LEFT JOIN SUBCLASS_DETAIL_DAT SDET ON SDET.YEAR = '" + param._year + "' AND SDET.CLASSCD || '-' || SDET.SCHOOL_KIND || '-' || SDET.CURRICULUM_CD || '-' || ");
            stb.append("     SDET.SUBCLASSCD = T1.SUBCLASSCD AND ");
            stb.append("     SDET.SUBCLASS_SEQ = '012' ");
            stb.append(" LEFT JOIN RECORD_SCORE_DAT W4 ON W4.YEAR = '" + param._year + "' AND W4.SEMESTER = '" + param._semester + "' AND W4.TESTKINDCD || W4.TESTITEMCD || W4.SCORE_DIV = '" + param._testKindCd + "' ");
            stb.append("     AND W4.CLASSCD || '-' || W4.SCHOOL_KIND || '-' || W4.CURRICULUM_CD || '-' || W4.SUBCLASSCD = T1.SUBCLASSCD AND W4.SCHREGNO = T1.SCHREGNO ");
            stb.append(" LEFT JOIN PERFECT_RECORD_DAT PERF ON PERF.YEAR = '" + param._year + "' AND PERF.SEMESTER = '" + param._semester + "' AND PERF.TESTKINDCD || PERF.TESTITEMCD = '" + param._testKindCd.substring(0, 4) + "' ");
            stb.append("     AND PERF.CLASSCD || '-' || PERF.SCHOOL_KIND || '-' || PERF.CURRICULUM_CD || '-' || PERF.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("     AND PERF.GRADE = CASE WHEN DIV = '01' THEN '00' ELSE SCH.GRADE END ");
            stb.append("     AND PERF.COURSECD || PERF.MAJORCD || PERF.COURSECODE = CASE WHEN PERF.DIV IN ('01','02') THEN '00000000' ELSE SCH.COURSECD || SCH.MAJORCD || SCH.COURSECODE END ");
            stb.append(" LEFT JOIN SUPP_EXA_SDIV_DAT SUPP ON SUPP.YEAR = '" + param._year + "' ");
            stb.append("             AND SUPP.SEMESTER = '" + param._semester + "' ");
            stb.append("             AND SUPP.TESTKINDCD || SUPP.TESTITEMCD || SUPP.SCORE_DIV = '" + param._testKindCd + "' ");
            stb.append("             AND SUPP.CLASSCD || '-' || SUPP.SCHOOL_KIND || '-' || SUPP.CURRICULUM_CD || '-' || SUPP.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("             AND SUPP.SCHREGNO = T1.SCHREGNO ");
            stb.append("             AND SUPP.SCORE_FLG = '2' ");
            stb.append(" LEFT JOIN CHAIR_A3 CA3 ON CA3.SCHREGNO = T1.SCHREGNO ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(" AND CA3.CLASSCD = T1.CLASSCD AND CA3.SCHOOL_KIND = T1.SCHOOL_KIND AND CA3.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append("     AND CA3.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append(" LEFT JOIN STAFF_MST STFM2 ON STFM2.STAFFCD = CA3.STAFFCD ");
            stb.append(" LEFT JOIN CHAIR_A4 CA4 ON CA4.SCHREGNO = T1.SCHREGNO ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(" AND CA4.CLASSCD = T1.CLASSCD AND CA4.SCHOOL_KIND = T1.SCHOOL_KIND AND CA4.CURRICULUM_CD = T1.CURRICULUM_CD ");
            }
            stb.append("     AND CA4.SUBCLASSCD = T1.SUBCLASSCD ");
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
            stb.append(" ORDER BY T1.SUBCLASSCD, T1.SCHREGNO");

            return stb.toString();
        }

        /**
         * 科目クラスの取得（教科名・科目名・単位・授業時数）
         * @param rs 生徒別科目別明細
         * @return 科目のクラス
         */
        private SubClass getSubClass(
                final Map row,
                final Map subclasses
        ) {
        	final Integer zero = new Integer(0);
            String subclasscode = null;
            int credit = 0;
            int perfect = 0;
            try {
                subclasscode = KnjDbUtils.getString(row, "SUBCLASSCD");
                if (KnjDbUtils.getString(row, "CREDITS") != null) { credit = KnjDbUtils.getInt(row, "CREDITS", zero).intValue(); }
                if (KnjDbUtils.getString(row, "PERFECT") != null) { perfect = KnjDbUtils.getInt(row, "PERFECT", zero).intValue(); }
            } catch (Exception e) {
                 log.error("Exception", e);
            }
            //科目クラスのインスタンスを更新して返す
            SubClass subclass;
            if (!subclasses.containsKey(subclasscode)) {
                //科目クラスのインスタンスを作成して返す
                String classabbv = null;
                String subclassabbv = null;
                String subclassname = null;
                String staffname = null;
                List otherstafflist = new ArrayList();
                boolean electdiv = false;
                try {
                    classabbv = KnjDbUtils.getString(row, "CLASSNAME");
                    subclassabbv = KnjDbUtils.getString(row, "SUBCLASSABBV");
                    subclassname = KnjDbUtils.getString(row, "SUBCLASSNAME");
                    staffname = KnjDbUtils.getString(row, "STAFFNAME");
                    if ("1".equals(KnjDbUtils.getString(row, "ELECTDIV"))) {
                        electdiv = true;
                    }
                    if (!"".equals(StringUtils.defaultString(KnjDbUtils.getString(row, "STAFF2")))) {
                        otherstafflist.add(KnjDbUtils.getString(row, "STAFF2"));
                    }
                    if (!"".equals(StringUtils.defaultString(KnjDbUtils.getString(row, "STAFF3")))) {
                        otherstafflist.add(KnjDbUtils.getString(row, "STAFF3"));
                    }
                } catch (Exception e) {
                     log.error("Exception", e);
                }
                subclass = new SubClass(subclasscode, classabbv, subclassabbv, subclassname, electdiv, credit,  perfect, staffname, otherstafflist);
                subclasses.put(subclasscode, subclass);
            } else {
                subclass = (SubClass) subclasses.get(subclasscode);
                int[] maxMin = setMaxMin(subclass._maxcredit, subclass._mincredit, credit);
                subclass._maxcredit = maxMin[0];
                subclass._mincredit = maxMin[1];
                int[] maxMinP = setMaxMin(subclass._maxperfect, subclass._minperfect, perfect);
                subclass._maxperfect = maxMinP[0];
                subclass._minperfect = maxMinP[1];
            }
            return subclass;
        }

        // 前年度までの修得単位数計
        private void loadPreviousCredits(
                final DB2UDB db2,
                final Param param
        ) {
            PreparedStatement ps = null;

            try {
                final StringBuffer stb = new StringBuffer();

                stb.append(" SELECT SUM(CASE WHEN T1.ADD_CREDIT IS NOT NULL THEN VALUE(T1.GET_CREDIT,0) + VALUE(T1.ADD_CREDIT,0) ELSE T1.GET_CREDIT END) AS CREDIT");
                stb.append(" FROM   SCHREG_STUDYREC_DAT T1");
                stb.append(" WHERE  T1.SCHREGNO = ?");
                stb.append("    AND T1.YEAR < '" + param._year + "'");
                stb.append("    AND ((T1.SCHOOLCD = '0' AND (CLASSCD BETWEEN '" + KNJDefineSchool.subject_D + "' AND '" + KNJDefineSchool.subject_U + "' OR CLASSCD = '" + KNJDefineSchool.subject_T + "'))");
                stb.append("      OR T1.SCHOOLCD != '0')");

                final String sql = stb.toString();
                ps = db2.prepareStatement(sql);

                final Integer zero = new Integer(0);

                for (final Iterator it = _students.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, ps, new Object[] { student._schregno}));

                    student._previousCredits = KnjDbUtils.getInt(row, "CREDIT", zero).intValue();
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }

        // 前年度までの未履修（必須科目）数
        private void loadPreviousMirisyu(
                final DB2UDB db2,
                final Param param
        ) {
            PreparedStatement ps = null;

            try {
                final StringBuffer stb = new StringBuffer();

                stb.append(" SELECT COUNT(*) AS COUNT");
                stb.append(" FROM   SCHREG_STUDYREC_DAT T1");
                stb.append(" INNER JOIN SUBCLASS_MST T2 ON ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append("    T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
                }
                stb.append("            T1.SUBCLASSCD = ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append("    T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || ");
                }
                stb.append("            T2.SUBCLASSCD");
                stb.append(" WHERE  T1.SCHREGNO = ?");
                stb.append("    AND T1.YEAR < '" + param._year + "'");
                stb.append("    AND ((T1.SCHOOLCD = '0' AND (CLASSCD BETWEEN '" + KNJDefineSchool.subject_D + "' AND '" + KNJDefineSchool.subject_U + "' OR CLASSCD = '" + KNJDefineSchool.subject_T + "'))");
                stb.append("      OR T1.SCHOOLCD != '0')");
                stb.append("    AND VALUE(T2.ELECTDIV,'0') <> '1'");
                stb.append("    AND VALUE(T1.COMP_CREDIT,0) = 0");

                final String sql = stb.toString();
                ps = db2.prepareStatement(sql);

                final Integer zero = new Integer(0);

                for (final Iterator it = _students.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();


                    final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, ps, new Object[] { student._schregno}));

                    student._previousMirisyu = KnjDbUtils.getInt(row, "COUNT", zero).intValue();

                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }

        // 今年度の資格認定単位数
        private void loadQualifiedCredits(
                final DB2UDB db2,
                final Param param
        ) {
            PreparedStatement ps = null;

            try {
                final StringBuffer stb = new StringBuffer();

                stb.append(" SELECT SUM(T1.CREDITS) AS CREDITS");
                stb.append(" FROM SCHREG_QUALIFIED_DAT T1");
                stb.append(" WHERE  T1.SCHREGNO = ?");
                stb.append("    AND T1.YEAR < '" + param._year + "'");

                final String sql = stb.toString();
                ps = db2.prepareStatement(sql);

                final Integer zero = new Integer(0);

                for (final Iterator it = _students.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();

                    final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, ps, new Object[] { student._schregno}));

                    student._qualifiedCredits = KnjDbUtils.getInt(row, "CREDITS", zero).intValue();

                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }

        /**
         * 欠点の算出
         */
        private void setHrTotalFail() {
            int countFail = 0;
            for (final Iterator itS = _students.iterator(); itS.hasNext();) {
                final Student student = (Student) itS.next();
                if (null != student._total) {
                    if (0 < student._total._countFail) {
                        countFail += student._total._countFail;
                    }
                }
            }
            if (0 < countFail) {
                _failHrTotal = String.valueOf(countFail);
            }
        }

        /**
         * 最高点・最低点の算出
         */
        private void setHrTotalMaxMin() {
            int totalMax = 0;
            int totalMin = Integer.MAX_VALUE;
            int countT = 0;
            for (final Iterator itS = _students.iterator(); itS.hasNext();) {
                final Student student = (Student) itS.next();
                if (null == student._scoreSum) {
                    continue;
                }
                countT++;
                final int totalInt = Integer.parseInt(student._scoreSum);
                //最高点
                totalMax = Math.max(totalMax, totalInt);
                //最低点
                totalMin = Math.min(totalMin, totalInt);
//              log.debug("total="+total+", totalMax="+totalMax+", totalMin="+totalMin);
            }
            if (0 < countT) {
                _maxHrTotal = String.valueOf(totalMax);
                _minHrTotal = String.valueOf(totalMin);
            }
        }

        /**
         * Studentクラスの成績から科目別学級平均および合計を算出し、SubClassクラスのフィールドにセットする。
         */
        private void setSubclassAverage(final Param param) {
            final Map map = new HashMap();

            for (final Iterator itS = _students.iterator(); itS.hasNext();) {
                final Student student = (Student) itS.next();
                for (final Iterator itD = student._scoreDetails.values().iterator(); itD.hasNext();) {
                    final ScoreDetail detail = (ScoreDetail) itD.next();
                    final String scorevalue = detail._score;
                    if (!param._isPrintSakiKamoku && null != detail._subClass && param.getSubclassMst(detail._subClass.keySubclasscd())._isSaki) {
                        continue;
                    } else if (param._isNoPrintMoto && null != detail._subClass && param.getSubclassMst(detail._subClass.keySubclasscd())._isMoto) {
                        continue;
                    }
                	final boolean useD001 = param._d065Name1List.contains(detail._subClass.keySubclasscd());
                	if (useD001) {
                		continue;
                	}
                    if (null == map.get(detail._subClass)) {
                        map.put(detail._subClass, new int[5]);
                    }
                    final int[] arr = (int[]) map.get(detail._subClass);
                    if (null != scorevalue && StringUtils.isNumeric(scorevalue)) {
                        final int v = Integer.parseInt(scorevalue);
                        arr[0] += v;
                        arr[1]++;
                        //最高点
                        if (arr[2] < v) {
                            arr[2] = v;
                        }
                        //最低点
                        if (arr[3] > v || arr[1] == 1) {
                            arr[3] = v;
                        }
                    }
                    //欠点（赤点）
                    if (ScoreDetail.isFailCount(param, detail)) {
                        arr[4]++;
                    }
                }
            }

            for (final Iterator it = _subclasses.values().iterator(); it.hasNext();) {
                final SubClass subclass = (SubClass) it.next();
                if (map.containsKey(subclass)) {
                    final int[] val = (int[]) map.get(subclass);
                    if (0 != val[1]) {
                        final double d = Math.round(val[0] * 10.0 / val[1]) / 10.0;
                        subclass._scoreaverage = DEC_FMT1.format(d);
                        subclass._scoretotal = String.valueOf(val[0]);
                        subclass._scoreCount = String.valueOf(val[1]);
                        subclass._scoreMax = String.valueOf(val[2]);
                        subclass._scoreMin = String.valueOf(val[3]);
                        if (0 != val[4]) {
                            subclass._scoreFailCnt = String.valueOf(val[4]);
                        }
                    }
                }
            }
        }

        /**
         * 学級平均の算出
         */
        private void setHrTotal() {
            int totalT = 0;
            int countT = 0;
            double totalA = 0;
            int countA = 0;
            int mlesson = 0;
            int present = 0;
            int sick = 0;
            int[] arrc = {0,0};  // 履修単位
            int[] arrj = {0,0};  // 授業日数
            for (final Iterator itS = _students.iterator(); itS.hasNext();) {
                final Student student = (Student) itS.next();
                final Total totalObj = student._total;
                if (null != totalObj) {
                    if (0 < totalObj._count) {
                        totalT += totalObj._total;
                        countT++;
                    }
                    if (0< totalObj._count) {
                        totalA += totalObj._total;
                        countA += totalObj._count;
                    }
                }
                final AttendInfo attend = student._attendInfo;
                if (null != attend) {
                    mlesson += attend._mLesson;
                    present += attend._present;
                    sick += attend._sick;
                    arrj = setMaxMin(arrj[0], arrj[1], attend._mLesson);
                }
                arrc = setMaxMin(arrc[0], arrc[1], student._compCredit);
            }
            if (0 < countT) {
                final double avg = (float) totalT / (float) countT;
                _avgHrTotalScore = new BigDecimal(avg);
            }
            if (0 < countA) {
                final double avg = (float) totalA / (float) countA;
                _avgHrAverageScore = new BigDecimal(avg);
            }
            if (0 < mlesson) {
                _perHrPresent = new BigDecimal((float) present / (float) mlesson * 100);
                _perHrSick = new BigDecimal((float) sick / (float) mlesson * 100);
            }
            if (0 < arrc[0]) {
                _HrCompCredits = arrc[0] + "単位";
            }
            if (0 < arrj[0]) {
                _HrMLesson = arrj[0] + "日";
            }
        }

        /**
         * 科目の学年平均得点
         * @param db2
         * @throws SQLException
         */
        private void setSubclassGradeAverage(final DB2UDB db2, final Param param, final String avgDiv) {
            final StringBuffer stb = new StringBuffer();
            stb.append("SELECT ");
            stb.append("    VALUE(T2.ELECTDIV, '0') AS ELECTDIV, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("    T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
            }
            stb.append("    T1.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("    T1.AVG ");
            stb.append("FROM ");
            stb.append("    RECORD_AVERAGE_SDIV_DAT T1 ");
            stb.append("    LEFT JOIN SUBCLASS_MST T2 ON ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("    T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || ");
            }
            stb.append("        T2.SUBCLASSCD = ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("    T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
            }
            stb.append("        T1.SUBCLASSCD ");
            stb.append("WHERE ");
            stb.append("    T1.YEAR = '" + param._year + "'");
            stb.append("    AND T1.SEMESTER = '" + param._semester + "' ");
            stb.append("    AND T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '" + param._testKindCd + "'");
            stb.append("    AND T1.AVG_DIV = '" + avgDiv + "' ");
            stb.append("    AND T1.GRADE = '" + param._grade + "' ");
            stb.append("    AND T1.SUBCLASSCD <> '" + param.SUBCLASSCD999999 + "' ");
            if ("2".equals(avgDiv)) {
                stb.append("    AND T1.HR_CLASS = '" + _hrclassCd.substring(2) + "' ");
                stb.append("    ORDER BY HR_CLASS ");

            } else if ("3".equals(avgDiv)) {
                final String[] coursecds = new String[_courses.size()];
                for (int i = 0; i < _courses.size(); i++) {
                    final Course course = (Course) _courses.get(i);
                    coursecds[i] = course._coursecd;
                }
                stb.append("    AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE IN " + SQLUtils.whereIn(true, coursecds) + " ");
                stb.append("    ORDER BY T1.COURSECD || T1.MAJORCD || T1.COURSECODE ");
                if (SQLUtils.whereIn(true, coursecds) == null) {
                    log.debug("warning:PARAMETER(cousecds) is NULL .");
                    return;
                }
            } else if ("4".equals(avgDiv)) {
                final String[] majorcds = new String[_courses.size()];
                for (int i = 0; i < _courses.size(); i++) {
                    final Course course = (Course) _courses.get(i);
                    majorcds[i] = course._coursecd.substring(0, course._coursecd.length() - 4);
                }
                stb.append("    AND T1.COURSECD || T1.MAJORCD IN " + SQLUtils.whereIn(true, majorcds) + " ");
                stb.append("    ORDER BY T1.COURSECD || T1.MAJORCD ");
                if (SQLUtils.whereIn(true, majorcds) == null) {
                    log.debug("warning:PARAMETER(majorcds) is NULL .");
                    return;
                }
            }

            final String sql = stb.toString();
            log.debug(" gradeAverage sql = " + sql);
            try {
                for (final Iterator it = KnjDbUtils.query(db2, sql).iterator(); it.hasNext();) {
                	final Map row = (Map) it.next();
                    final String subclassCd = KnjDbUtils.getString(row, "SUBCLASSCD");
                    final String electDiv = KnjDbUtils.getString(row, "ELECTDIV");

                    final SubClass subclass = (SubClass) _subclasses.get(electDiv + subclassCd);
                    final BigDecimal subclassGradeAvg = KnjDbUtils.getBigDecimal(row, "AVG", null);
                    if (subclass == null || subclassGradeAvg == null) {
                        //log.debug("subclass => " + subclass + " , gradeAvg => " + subclassGradeAvg);
                        continue;
                    }
                    //log.debug("subclass => " + subclass._subclassabbv + " , gradeAvg => " + subclassGradeAvg);
                    subclass._scoresubaverage = Form.sishaGonyu(subclassGradeAvg);
                    if ("1".equals(avgDiv)) {
                        subclass._scoresubaverageGrade = Form.sishaGonyu(subclassGradeAvg);
                    } else if ("3".equals(avgDiv)) {
                        subclass._scoresubaverageCourse = Form.sishaGonyu(subclassGradeAvg);
                    }
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
        }

        /**
         * 順位の算出
         */
        private List createRanking(final Param param) {
            final List list = new LinkedList();
            for (final Iterator itS = _students.iterator(); itS.hasNext();) {
                final Student student = (Student) itS.next();
                student._total = new Total(param, student);
                final Total total = student._total;
                if (0 < total._count) {
                    list.add(total);
                }
            }

            Collections.sort(list);
            return list;
        }

        private int rank(final Student student) {
            final Total total = student._total;
            if (0 >= total._count) {
                return -1;
            }
            return 1 + _ranking.indexOf(total);
        }

        private static int[] setMaxMin(
                int maxInt,
                int minInt,
                int tergetInt
        ) {
            if (0 < tergetInt) {
                if (maxInt < tergetInt){ maxInt = tergetInt; }
                if (0 == minInt) {
                    minInt = tergetInt;
                } else {
                    if (minInt > tergetInt){ minInt = tergetInt; }
                }
            }
            return new int[]{maxInt, minInt};
        }

        public int compareTo(final Object o) {
            if (!(o instanceof HRInfo)) return -1;
            final HRInfo that = (HRInfo) o;
            return _hrclassCd.compareTo(that._hrclassCd);
        }

        public String toString() {
            return _hrName + "[" + _staffName + "]";
        }
//        public void bkupInfo(final Param param, final List bkupReportInfo) {
//            ReportDetailInfo adddetailwk = null;
//            if (OUTPUT_RANK1.equals(param._outputRank)) {
//                adddetailwk = new ReportDetailInfo(_avgHrAverage, _avgHrTotal, _subclasses);
//            } else if (OUTPUT_RANK2.equals(param._outputRank)) {
//                adddetailwk = new ReportDetailInfo(_avgGradeAverage, _avgGradeTotal, _subclasses);
//            } else if (OUTPUT_RANK3.equals(param._outputRank)) {
//                adddetailwk = new ReportDetailInfo(_avgCourseAverage, _avgCourseTotal, _subclasses);
//            } else if (OUTPUT_RANK4.equals(param._outputRank)) {
//                adddetailwk = new ReportDetailInfo(_avgMajorAverage, _avgMajorTotal, _subclasses);
//            }
//            ReportInfo addwk = new ReportInfo(_hrclassCd, _hrName, _courses, adddetailwk);
//            bkupReportInfo.add(addwk);
//        }
    }

//    private static class ReportInfo {
//        final String _hrclass;
//        final String _hrName;
//        final List _courses;
//        final ReportDetailInfo _reportlist;
//        ReportInfo(final String hrclass, final String hrName, final List courses, final ReportDetailInfo reportlist) {
//            _hrclass = hrclass;
//            _hrName = hrName;
//            _courses = courses;
//            _reportlist = reportlist;
//        }
//    }

//    private static class ReportDetailInfo{
//        final Map _subclasses;
//        final String _gavg;
//        final String _gtotal;
//        ReportDetailInfo(final String gavg, final String gtotal, final Map subclasses) {
//            _gavg = gavg;
//            _gtotal = gtotal;
//            _subclasses = new TreeMap(subclasses);
//        }
//    }

    //--- 内部クラス -------------------------------------------------------
    private static class TransInfo {
        final String _date;
        final String _dateStr;
        final String _name;

        public TransInfo(
                final String date,
                final String dateStr,
                final String name
        ) {
            _date = date;
            _dateStr = dateStr;
            _name = name;
        }

        public String toString() {
            if (null == _date && null == _name) {
                return "";
            }

            final StringBuffer sb = new StringBuffer();
            if (null != _dateStr) {
                sb.append(_dateStr);
            }
            if (null != _name) {
                sb.append(_name);
            }
            return sb.toString();
        }
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * <<生徒のクラス>>。
     */
    private static class Student implements Comparable {
        final int _gnum;  // 行番号
        final String _schregno;  // 学籍番号
//        final String _ryou;
//        final String _kaigai;
//        final String _suisen;
//        final String _ib;
//        final String _aHouShiki;
        final HRInfo _hrInfo;
        private String _attendNo;
        private String _name;
        private TransInfo _transInfo;
        private AttendInfo _attendInfo = new AttendInfo(0, 0, 0, 0, 0, 0, 0, 0, 0, new Double(0));
        private String _scoreSum;
        private String _scoreAvg;
        private int _classRank;
        private int _gradeRank;
        private int _courseRank;
        private int _rank;
        private final Map _scoreDetails = new TreeMap();
        private Total _total;
        private int _compCredit;  // 今年度の履修単位数
        private int _getCredit;  // 今年度の修得単位数
        private int _qualifiedCredits;  // 今年度の認定単位数
        private int _previousCredits;  // 前年度までの修得単位数
        private int _previousMirisyu;  // 前年度までの未履修（必須科目）数
        private boolean _isGradePoor;  // 成績不振者
        private boolean _isAttendPerfect;  // 皆勤者
        private boolean _isKekkaOver;  // 欠時数超過が1科目でもある者
//        private Map _spGroupAbsentMinutes = new HashMap(); // 特活グループコードごとの欠時分
//        private int _specialAbsent; // 特活欠時数
        private String _attendSemesRemarkDatRemark1;
//        private String _scholarshipName;
        double _totalKekka; //科目別欠時の合計

        Student(
                final String code,
                final HRInfo hrInfo,
                final int gnum,
                final String ryou,
                final String kaigai,
                final String suisen,
                final String ib,
                final String aHouShiki
        ) {
            _gnum = gnum;
            _schregno = code;
            _hrInfo = hrInfo;
//            _ryou = ryou;
//            _kaigai = kaigai;
//            _suisen = suisen;
//            _ib = ib;
//            _aHouShiki = aHouShiki;
        }

        /**
         * 出席番号順にソートします。
         * {@inheritDoc}
         */
        public int compareTo(final Object o) {
            if (!(o instanceof Student)) return -1;
            final Student that = (Student) o;
            int rtn;
            rtn = _hrInfo.compareTo(that._hrInfo);
            if (0 != rtn) return rtn;
            rtn = _attendNo.compareTo(that._attendNo);
            return rtn;
        }

        public String toString() {
            return _attendNo + ":" + _name;
        }

        /**
         * @return 欠時超過が1科目でもあるなら true を戻します。
         */
        public boolean isKekkaOver(final Param param) {
            return null != getKekkaOverKamokuCount(param);
        }

        public String getKekkaOverKamokuCount(final Param param) {
            int count = 0;
            for (final Iterator itD = _scoreDetails.values().iterator(); itD.hasNext();) {
                final ScoreDetail detail = (ScoreDetail) itD.next();
                if (!param._isPrintSakiKamoku && null != detail._subClass && param.getSubclassMst(detail._subClass.keySubclasscd())._isSaki) {
                    continue;
                } else if (param._isNoPrintMoto && null != detail._subClass && param.getSubclassMst(detail._subClass.keySubclasscd())._isMoto) {
                    continue;
                }
                if (detail._isOver) {
                    count += 1;
                }
            }
            return count == 0 ? null : String.valueOf(count);
        }

        public String getKettenKamokuCount(final Param param) {
            int count = 0;
            for (final Iterator itD = _scoreDetails.values().iterator(); itD.hasNext();) {
                final ScoreDetail detail = (ScoreDetail) itD.next();
                if (!param._isPrintSakiKamoku && null != detail._subClass && param.getSubclassMst(detail._subClass.keySubclasscd())._isSaki) {
                    continue;
                } else if (param._isNoPrintMoto && null != detail._subClass && param.getSubclassMst(detail._subClass.keySubclasscd())._isMoto) {
                    continue;
                }
                if (ScoreDetail.isFailCount(param, detail)) {
                    count += 1;
                }
            }
            return count == 0 ? null : String.valueOf(count);
        }

        public String getKettenTanni(final Param param) {
            int credit = 0;
            for (final Iterator itD = _scoreDetails.values().iterator(); itD.hasNext();) {
                final ScoreDetail detail = (ScoreDetail) itD.next();
                if (!param._isPrintSakiKamoku && null != detail._subClass && param.getSubclassMst(detail._subClass.keySubclasscd())._isSaki) {
                    continue;
                } else if (param._isNoPrintMoto && null != detail._subClass && param.getSubclassMst(detail._subClass.keySubclasscd())._isMoto) {
                    continue;
                }
                if (ScoreDetail.isFailCount(param, detail) && null != detail._credits) {
                    credit += detail._credits.intValue();
                }
            }
            return credit == 0 ? null : String.valueOf(credit);
        }

        public String getRemark(final Param param) {
            String remark = "";
            if (null != getKaikin()) {
                remark += getKaikin() + " ";
            }
            if (param._outputBiko && null != _attendSemesRemarkDatRemark1) {
                remark += _attendSemesRemarkDatRemark1 + " ";
            }
            remark += _transInfo.toString();  // 備考
            return remark;
        }

        public String getKaikin() {
            String remark = "";
            if (_attendInfo._sick == 0 && _attendInfo._late == 0 && _attendInfo._early == 0 && _totalKekka == 0) {
                remark = "皆勤";
            }
            if (_attendInfo._late > 10 || _attendInfo._early > 10 || _totalKekka > 10) {
                remark = "×";
            }
            return remark;
        }

        public String getPrintAttendno() {
            return NumberUtils.isDigits(_attendNo) ? String.valueOf(Integer.parseInt(_attendNo)) : _attendNo;
        }

        private static class ClassRankComparator implements Comparator {
            public int compare(final Object o1, final Object o2) {
                final Student s1 = (Student) o1;
                final Student s2 = (Student) o2;
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
                if (s1._rank != s2._rank) {
                    if (s1._rank == 0) {
                        cmp = 1;
                    } else if (s2._rank == 0) {
                        cmp = -1;
                    } else {
                        cmp = s1._rank - s2._rank; // _rankの昇順
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
		final Double _tochuKekka;

        AttendInfo(
                final int lesson,
                final int mLesson,
                final int suspend,
                final int mourning,
                final int sick,
                final int present,
                final int late,
                final int early,
                final int transDays,
                final Double tochuKekka
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
            _tochuKekka = tochuKekka;
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
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * <<科目のクラスです>>。
     */
    private static class SubClass {
        final String _classabbv;
        final String _classcode;
        final String _subclasscode;
        final String _subclassabbv;
        final String _subclassname;
        final boolean _electdiv; // 選択科目
        final String _staffname;  // 科目担当者名
        private int _maxcredit;  // 単位
        private int _mincredit;  // 単位
        private Integer _jisu;  // 授業時数
        private String _scoreaverage;  // 学級平均
        private String _scoresubaverage;  // 学年平均
        private String _scoresubaverageGrade;  // 学年平均
        private String _scoresubaverageCourse;  // コース平均
        private String _scoretotal;  // 学級合計
        private String _scoreCount;  // 学級人数
        private String _scoreMax;  // 最高点
        private String _scoreMin;  // 最低点
        private String _scoreFailCnt;  // 欠点者数
        private int _maxperfect;  // 満点
        private int _minperfect;  // 満点
        private List _otherstafflist; //他の先生(MAX2人)


        SubClass(
                final String subclasscode,
                final String classabbv,
                final String subclassabbv,
                final String subclassname,
                final boolean electdiv,
                final int credit,
                final int perfect,
                final String staffname,
                final List otherstafflist
        ) {
            _classabbv = classabbv;
            // (subclasscodeは頭1桁+科目コードなので教科コードは2文字目から2桁)
            _classcode = subclasscode.substring(1, 3);
            _subclasscode = subclasscode;
            _subclassabbv = subclassabbv;
            _subclassname = subclassname;
            _electdiv = electdiv;
            _maxcredit = credit;  // 単位
            _mincredit = credit;  // 単位
            _maxperfect = perfect;  // 満点
            _minperfect = perfect;  // 満点
            _staffname = staffname;
            _otherstafflist = otherstafflist;
        }

        public boolean equals(final Object obj) {
            if (!(obj instanceof SubClass)) return false;
            final SubClass that = (SubClass) obj;
            return _subclasscode.equals(that._subclasscode);
        }

        public int hashCode() {
            return _subclasscode.hashCode();
        }

        public String toString() {
            return "["+_classabbv + " , " +_subclasscode + " , " +_subclassabbv + " , " +_electdiv + " , " +_maxcredit + " , " +_mincredit + " , " +_jisu +"]";
        }

        public String keySubclasscd() {
            return _subclasscode.substring(1);
        }

        public String getPrintCredit(final Param param) {
            final StringBuffer stb = new StringBuffer();
            if (0 != _maxcredit) {
                if (_maxcredit == _mincredit) {
                    stb.append(_maxcredit);
                } else {
                    if (param._isPrintPerfect) {
                        stb.append(String.valueOf(_mincredit) + Param.FROM_TO_MARK + String.valueOf(_maxcredit));
                    } else {
                        stb.append(String.valueOf(_mincredit) + " " + Param.FROM_TO_MARK + " " + String.valueOf(_maxcredit));
                    }
                }
            }
            if (param._isPrintPerfect) {
                if (0 != _maxperfect) {
                    if (_maxperfect == _minperfect) {
                        stb.append("(").append(_maxperfect).append(")");
                    } else {
                        stb.append("(*)");
                    }
                }
            }
            return stb.toString();
        }

        public String getJisu() {
            if (null == _jisu) {
                return "";
            }
            return _jisu.toString();
        }
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * <<生徒別科目別データのクラスです>>。
     */
    private static class ScoreDetail {
        final SubClass _subClass;
        Double _ketsuji;
        Integer _jisu;
        final String _score;
        final String _scoreDi;
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
        boolean _isOver;
        final String _slump;
        final String _slumpMark;
        final String _slumpScore;

        ScoreDetail(
                final SubClass subClass,
                final String score,
                final String scoreDi,
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
                final Integer credits
        ) {
            _subClass = subClass;
            _score = score;
            _scoreDi = scoreDi;
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
        }

        /**
         * 欠時数超過ならTrueを戻します。
         * @param absent 欠時数
         * @param absenceHigh 超過対象欠時数（CREDIT_MST）
         * @return
         */
        private boolean judgeOver(final Double absent, final Integer jisu) {
            if (null == absent || null == jisu) {
                return false;
            }
            if (0.1 > absent.floatValue() || 0.0 == jisu.doubleValue()) {
                return false;
            }
            final BigDecimal absenceHigh = new BigDecimal(jisu).divide(new BigDecimal(3), 1, BigDecimal.ROUND_HALF_UP);
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
            return param._testKindCd != null && param._testKindCd.endsWith("09");
        }

        private static boolean isFail(final Param param, final ScoreDetail detail) {
        	final boolean useD001 = param._d065Name1List.contains(detail._subClass.keySubclasscd());
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
            		rtn = new Boolean("*".equals(detail._scoreDi) || "1".equals(detail._score));
            	} else {
            		final boolean setPassScore = NumberUtils.isDigits(detail._passScore);
            		rtn = new Boolean("*".equals(detail._scoreDi) || !setPassScore && "1".equals(detail._assessLevel) || setPassScore && NumberUtils.isDigits(detail._score) && Integer.parseInt(detail._score) < Integer.parseInt(detail._passScore));
            	}
            }
            if (param._isOutputDebug) {
            	if (rtn.booleanValue()) {
            		log.info(" isFail " + rtn + " <- " + param._useSlumpSdivDatSlump + " / " + detail._scoreDi + " / assessLevel = " + detail._assessLevel + " / score = " + detail._score + " / passScore = " + detail._passScore + "");
            	}
            }
			return rtn.booleanValue();
        }

        private static boolean isFailCount(final Param param, final ScoreDetail detail) {
        	final boolean useD001 = param._d065Name1List.contains(detail._subClass.keySubclasscd());
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
                return (!param._kettenshaSuuNiKessaShaWoFukumenai && "*".equals(detail._scoreDi)) || "1".equals(detail._score);
            }
            final boolean setPassScore = NumberUtils.isDigits(detail._passScore);
			return (!param._kettenshaSuuNiKessaShaWoFukumenai && "*".equals(detail._scoreDi)) || !setPassScore && "1".equals(detail._assessLevel) || setPassScore && NumberUtils.isDigits(detail._score) && Integer.parseInt(detail._score) < Integer.parseInt(detail._passScore);
        }

        public String toString() {
            return (_subClass + " , " + _ketsuji + " , " + _jisu + " , " + _score + " , " + _replacemoto + " , " + _print_flg + " , " + _compCredit + " , " + _getCredit + " , " + _absenceHigh + " , " + _credits + " , " + _isOver);
        }
    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * <<生徒別総合成績データのクラスです>>。
     */
    private static class Total implements Comparable {
        final int _total;  // 総合点
        final int _count;  // 件数（成績）
        final BigDecimal _avgBigDecimal;  // 平均点
        final int _countFail;  //欠点科目数

        /**
         * 生徒別総合点・件数・履修単位数・修得単位数・特別活動欠時数を算出します。
         * @param student
         */
        Total(final Param param, final Student student) {

            int total = 0;
            int count = 0;

            int compCredit = 0;
            int getCredit = 0;

            int countFail = 0;

            for (final Iterator it = student._scoreDetails.values().iterator(); it.hasNext();) {
                final ScoreDetail detail = (ScoreDetail) it.next();
                if (!param._isPrintSakiKamoku && null != detail._subClass && param.getSubclassMst(detail._subClass.keySubclasscd())._isSaki) {
                    continue;
                } else if (param._isNoPrintMoto && null != detail._subClass && param.getSubclassMst(detail._subClass.keySubclasscd())._isMoto) {
                    continue;
                }

                final String scoreValue = detail._score;
                if (isAddTotal(detail._replacemoto, param)) {
                	if (null != scoreValue && StringUtils.isNumeric(scoreValue)) {
                		total += Integer.parseInt(scoreValue);
                		count++;
                	}
                	if (ScoreDetail.isFailCount(param, detail)) {
                		countFail++;
                	}
                }

                final Integer c = detail.getCompCredit();
                if (null != c) {
                    compCredit += c.intValue();
                }

                final Integer g = detail.getGetCredit();
                if (null != g) {
                    getCredit += g.intValue();
                }
            }

//            int specialAbsent = 0;
//            for (final Iterator it = student._spGroupAbsentMinutes.values().iterator(); it.hasNext();) {
//                final Integer groupAbsentMinutes = (Integer) it.next();
//                specialAbsent += getSpecialAttendExe(param, groupAbsentMinutes.intValue());
//            }

            _total = total;
            _count = count;
            if (0 < count) {
                final double avg = (float) total / (float) count;
                _avgBigDecimal = new BigDecimal(avg);
            } else {
                _avgBigDecimal = null;
            }
            if (0 < compCredit) {
                student._compCredit = compCredit;
            }
            if (0 < getCredit) {
                student._getCredit = getCredit;
            }
//            student._specialAbsent = specialAbsent;
            _countFail = countFail;
        }

        /**
         * 欠時分を欠時数に換算した値を得る
         * @param kekka 欠時分
         * @return 欠時分を欠時数に換算した値
         */
        private int getSpecialAttendExe(final Param param, final int kekka) {
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

        /**
         * @param replacemoto
         * @param knjdObj
         * @return 成績総合計に組み入れる場合Trueを戻します。
         */
        private boolean isAddTotal(
                final Integer replacemoto,
                final Param param
        ) {
            if (param._isGakunenMatu && null != replacemoto && 0 < replacemoto.intValue()) { return false; }
            return true;
        }

        /**
         * {@inheritDoc}
         */
        public int compareTo(final Object o) {
            if (!(o instanceof Total)) return -1;
            final Total that = (Total) o;

            return that._avgBigDecimal.compareTo(this._avgBigDecimal);
        }

        /**
         * {@inheritDoc}
         */
        public boolean equals(final Object o) {
            if (!(o instanceof Total)) return false;
            final Total that = (Total) o;
            return that._avgBigDecimal.equals(this._avgBigDecimal);
        }

        /**
         * {@inheritDoc}
         */
        public String toString() {
            return _avgBigDecimal.toString();
        }
    }

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

        public boolean print(final DB2UDB db2, final IPdf ipdf, final Param param, final HRInfo hrInfo) {
            boolean hasData = false;
            if ("2".equals(param._outputPattern)) {
                if (Form2.print2(db2, ipdf, param, hrInfo)) {
                    hasData = true;
                }
            } else {
                final List studentsAll = new ArrayList(hrInfo._students);
                if ("2".equals(param._outputOrder)) {
                    Collections.sort(studentsAll, new Student.ClassRankComparator());
                }

                final List studentListList = getStudentListList(studentsAll, param._formMaxLine);
                for (final Iterator it = studentListList.iterator(); it.hasNext();) {
                    final List studentList = (List) it.next();
                    if (Form1.print(db2, ipdf, param, hrInfo, studentList)) {
                        hasData = true;
                    }
                }
            }
            return hasData;
        }

        public boolean outputCsv(final DB2UDB db2, final List outputList, final Param param, final HRInfo hrInfo) {
            boolean hasData = false;
            if ("2".equals(param._outputPattern)) {
                throw new IllegalArgumentException("not implemented pattern:" + param._outputPattern);
            } else {
                final List studentsAll = new ArrayList(hrInfo._students);
                if ("2".equals(param._outputOrder)) {
                    Collections.sort(studentsAll, new Student.ClassRankComparator());
                }
                if (Form1.outputCsv(db2, outputList, param, hrInfo, studentsAll)) {
                    hasData = true;
                }
            }
            return hasData;
        }

        private static String getKekkaString(final Param param, final Double absent) {
            return null == absent ? "" : 0.0 == absent.doubleValue() && !"1".equals(param._printKekka0)  ? "" : param.getAbsentFmt().format(absent.floatValue());
        }

        private static String sishaGonyu(final BigDecimal bd) {
            return null == bd ? null : bd.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
        }

        private static List newLine(final List listList) {
            final List line = line();
            listList.add(line);
            return line;
        }

        private static List line() {
            return line(0);
        }

        private static List line(final int size) {
            final List line = new ArrayList();
            for (int i = 0; i < size; i++) {
                line.add(null);
            }
            return line;
        }

        private static int currentColumn(final List lineList) {
            int max = 0;
            for (final Iterator it = lineList.iterator(); it.hasNext();) {
                final List line = (List) it.next();
                max = Math.max(max, line.size());
            }
            return max;
        }

        private static List setSameSize(final List list, final int max) {
            for (int i = list.size(); i < max; i++) {
                list.add(null);
            }
            return list;
        }

        private static List getStudentListList(final List students, final int count) {
            final List rtn = new ArrayList();
            List current = null;
            int page = 0;
            for (final Iterator it = students.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                final int cpage = student._gnum / count + (student._gnum % count != 0 ? 1 : 0);
                if (null == current || page < cpage) {
                    current = new ArrayList();
                    rtn.add(current);
                }
                current.add(student);
                page = cpage;
            }
            return rtn;
        }

        private static List getSubClassListList(final Param param, final Collection subclasses, final int count) {
            final List rtn = new ArrayList();
            List current = null;
            for (final Iterator it = subclasses.iterator(); it.hasNext();) {
                final SubClass subClass = (SubClass) it.next();
                final boolean notOutputColumn = "90".equals(subClass._classcode) && param._notOutputSougou;
                if (notOutputColumn) {
                    continue;
                }
                if (null == current || current.size() >= count) {
                    current = new ArrayList();
                    rtn.add(current);
                }
                current.add(subClass);
            }
            return rtn;
        }

        public static List getMappedList(final Map map, final Object key1) {
            if (!map.containsKey(key1)) {
                map.put(key1, new ArrayList());
            }
            return (List) map.get(key1);
        }

        public static Map getMappedHashMap(final Map map, final Object key1) {
            if (!map.containsKey(key1)) {
                map.put(key1, new HashMap());
            }
            return (Map) map.get(key1);
        }

        private static int getMS932ByteLength(final String s) {
            int len = 0;
            try {
                if (null != s) {
                    len = s.getBytes("MS932").length;
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return len;
        }

        private static String zeroToNull(final int num) {
            return num == 0 ? null : String.valueOf(num);
        }

        private static String nullToBlank(final Object o) {
            return null == o ? null : o.toString();
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

        private static void printAttendInfo(final IPdf ipdf, final Param param, final AttendInfo attendInfo, final int line, final Student student) {
            final String fieldLesson;
            String fieldMLesson = "";
            final String fieldSusMour;
            final String fieldSick;
            final String fieldPresent;
            final String fieldLate;
            final String fieldEarly;
            String fieldAbroad = "";
            String fieldKekka = "";
            if (PRGID_KNJD617I.equals(param._prgId)) {
                fieldLesson = "LESSON";
                fieldSusMour = "SUSPEND1";
                fieldPresent = "ATTEND1";
                fieldSick = "ABSENCE1";
                fieldLate = "TOTAL_LATE1";
                fieldEarly = "LEAVE1";
                fieldKekka = "TOTAL_KEKKA1";
            } else if (PATTERN2.equals(param._outputPattern)) {
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
            ipdf.VrsOutn(fieldLesson,  line, zeroToNull(attendInfo._lesson));      // 授業日数
            ipdf.VrsOutn(fieldSusMour, line, zeroToNull(attendInfo._suspend + attendInfo._mourning));      // 出席停止
            if (!PRGID_KNJD617I.equals(param._prgId)) {
                ipdf.VrsOutn(fieldAbroad,  line, zeroToNull(attendInfo._transDays));        // 留学
                ipdf.VrsOutn(fieldMLesson, line, zeroToNull(attendInfo._mLesson));      // 出席すべき日数
            }
            ipdf.VrsOutn(fieldPresent, line, attendInfo._lesson == 0 ? null : String.valueOf(attendInfo._present));      // 出席日数 (0は表示する)
            ipdf.VrsOutn(fieldSick,    line, zeroToNull(attendInfo._sick));       // 欠席日数
            ipdf.VrsOutn(fieldLate,    line, zeroToNull(attendInfo._late));      // 遅刻回数
            ipdf.VrsOutn(fieldEarly,   line, zeroToNull(attendInfo._early));        // 早退回数
            if (PRGID_KNJD617I.equals(param._prgId)) {
                ipdf.VrsOutn(fieldKekka,   line, getKekkaString(param, attendInfo._tochuKekka));        // 欠時
            }
        }

        private static boolean isAlpPdf(final IPdf ipdf) {
            return "1".equals(ipdf.getParameter("AlpPdf"));
        }

        private static void setForm(final IPdf ipdf, final String formname, final int n, final Param param) {
            log.info(" form = " + formname);
            ipdf.VrSetForm(formname, n);

            param._currentform = formname;
            if (ipdf instanceof SvfPdf) {
                if (null != param._currentform && null == param._formFieldInfoMap.get(param._currentform)) {
                    param._formFieldInfoMap.put(param._currentform, SvfField.getSvfFormFieldInfoMapGroupByName(((SvfPdf) ipdf).getVrw32alp()));
                    //debugFormInfo(param);
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
            final Map fieldMap = getMappedHashMap(param._formFieldInfoMap, param._currentform);
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
            if (param._isOutputDebug && !getMappedList(param._formFieldInfoMap, ".log").contains(logVal)) {
                log.warn(logVal);
                getMappedList(param._formFieldInfoMap, ".log").add(logVal);
            }
            return keta;
        }

        private static class Form2 {

            private static List getPageList(final List list, final Param param) {
                final List rtn = new ArrayList();
                List current = null;
                int currentLines = 0;
                for (final Iterator it = list.iterator(); it.hasNext();) {
                    final Student o = (Student) it.next();
                    final int scoreDetailListSize = getScoreDetailList(param, o).size();
                    final int studentLine = scoreDetailListSize / param._formMaxColumn + (scoreDetailListSize % param._formMaxColumn == 0 ? 0 : 1);
                    if (null == current || currentLines + Math.max(1, studentLine) > param._formMaxLine) {
                        current = new ArrayList();
                        currentLines = 0;
                        rtn.add(current);
                    }
                    current.add(o);
                    currentLines += studentLine;
                }
                return rtn;
            }

            public static boolean print2(final DB2UDB db2, final IPdf ipdf, final Param param, final HRInfo hrInfo) {
                boolean hasData = false;

                final List studentsAll = new ArrayList(hrInfo._students);
                if ("2".equals(param._outputOrder)) {
                    Collections.sort(studentsAll, new Student.ClassRankComparator());
                }

                final List pageList = getPageList(studentsAll, param);

                for (int pi = 0; pi < pageList.size(); pi++) {
                    setForm(ipdf, param._formname, 1, param);

                    final List students = (List) pageList.get(pi);
                    printHeader2(db2, ipdf, param, hrInfo);

                    int line = 0;
                    for (int j = 0; j < students.size(); j++) {
                        line += 1;
                        final Student student = (Student) students.get(j);

                        // 学籍番号表記
                        String nameNo = "";
                        if ("1".equals(param._use_SchregNo_hyoji)) {
                            ipdf.VrsOutn("SCHREG_NO", line, student._schregno); // 学籍番号
                            nameNo = "2"; // 学籍番号表示用の氏名フィールド
                        }

                        ipdf.VrsOutn("ATTENDNO", line, student.getPrintAttendno()); // 出席番号
                        ipdf.VrsOutn("NAME" + nameNo, line, student._name); // 生徒氏名

                        ipdf.VrsOutn("TOTAL", line, student._scoreSum); // 総合点
                        ipdf.VrsOutn("AVERAGE", line, student._scoreAvg); // 平均点
                        final String classRank = 0 >= student._classRank ? "" : String.valueOf(student._classRank);
                        ipdf.VrsOutn("RANK1", line, classRank); // 学級順位
                        final String rank = 0 >= student._rank ? "" : String.valueOf(student._rank);
                        ipdf.VrsOutn("RANK2", line, rank); // 指定順位
                        ipdf.VrsOutn("FAIL1", line, student.getKettenKamokuCount(param)); // 欠点科目数
                        printAttendInfo(ipdf, param, student._attendInfo, line, student);
                        printRemark(ipdf, param, student, line);

                        int col = 0;
                        final List detaillist = getScoreDetailList(param, student);
                        for (int si = 0; si < detaillist.size(); si++) {
                            final ScoreDetail detail = (ScoreDetail) detaillist.get(si);
                            if (col >= param._formMaxColumn) {
                                line += 1;
                                col = 0;
                            }
                            col++;
                            final String scol = String.valueOf(col);
                            if (ScoreDetail.isFail(param, detail)) { ipdf.VrAttributen("GRADING" + scol, line, ATTRIBUTE_KETTEN); }
                            final String printScore;
                            if (param._isMeikei && "*".equals(detail._scoreDi)) {
                            	printScore = StringUtils.defaultString(detail._suppScore, detail._scoreDi);
                            } else {
                            	final boolean useD001 = param._d065Name1List.contains(detail._subClass.keySubclasscd());
                            	printScore = StringUtils.defaultString(detail._scoreDi, useD001 ? (String) param._d001Name1Map.get(detail._score) : detail._score);
                            }
                            ipdf.VrsOutn("GRADING" + scol, line, printScore); // 成績
                            if (ScoreDetail.isFail(param, detail)) { ipdf.VrAttributen("GRADING" + scol, line, ATTRIBUTE_NORMAL); }
                            ipdf.VrsOutn("SUBJECT" + scol, line, detail._subClass._subclassabbv); // 科目名

                             // 単位数
                            ipdf.VrsOutn("CREDIT" + scol, line, nullToBlank(detail._credits));
                            ipdf.VrsOutn("TOTALLESSON" + scol, line, nullToBlank(detail._jisu)); // 総時数
                            final String pf = (param._definecode.absent_cov == 3 || param._definecode.absent_cov == 4) ? "_2" : "";
                            if (detail._isOver) { ipdf.VrAttributen("KEKKA" + scol + pf, line, ATTRIBUTE_KEKKAOVER); }
                            ipdf.VrsOutn("KEKKA" + scol + pf, line, getKekkaString(param, detail._ketsuji)); // 欠時数
                            if (detail._isOver) { ipdf.VrAttributen("KEKKA" + scol + pf, line, ATTRIBUTE_NORMAL); }
                            hasData = true;
                        }
                    }
                    ipdf.VrEndPage();
                }

                return hasData;
            }

            private static List getScoreDetailList(final Param param, final Student student) {
                final List detaillist = new ArrayList(student._scoreDetails.values());
                Collections.sort(detaillist, new ScoreDetailComparator());
                final List rtn = new ArrayList();
                for (int si = 0; si < detaillist.size(); si++) {
                    final ScoreDetail detail = (ScoreDetail) detaillist.get(si);
                    if (param._notOutputSougou && "90".equals(detail._subClass._classcode)) {
                        continue;
                    }
                    if (!param._isPrintSakiKamoku && param.getSubclassMst(detail._subClass.keySubclasscd())._isSaki) {
                        continue;
                    } else if (param._isNoPrintMoto && param.getSubclassMst(detail._subClass.keySubclasscd())._isMoto) {
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

            public static class ScoreDetailComparator implements Comparator {
                public int compare(final Object o1, final Object o2) {
                    final ScoreDetail s1 = (ScoreDetail) o1;
                    final ScoreDetail s2 = (ScoreDetail) o2;
                    return s1._subClass._subclasscode.substring(1).compareTo(s2._subClass._subclasscode.substring(1));
                }
            }

            private static void printHeader2(final DB2UDB db2, final IPdf ipdf, final Param param, final HRInfo hrInfo) {
                if (isAlpPdf(ipdf)) {
                    ipdf.VrsOut("TITLE", KNJ_EditDate.gengou(db2, Integer.parseInt(param._year)) + "年度　" + StringUtils.defaultString(param._semesterName) + "  " + StringUtils.defaultString(param._testItem._testitemname) + "　成績一覧表　"); // 年度
                } else {
                    ipdf.VrsOut("NENDO", KNJ_EditDate.gengou(db2, Integer.parseInt(param._year)) + "年度"); // 年度
                    ipdf.VrsOut("TERM", param._semesterName); // 学期
                    ipdf.VrsOut("TEST", param._testItem._testitemname); // テスト名
                }
                ipdf.VrsOut("CLASSNAME", hrInfo._hrName); // クラス名

                ipdf.VrAttribute("NOTE1", ATTRIBUTE_KEKKAOVER);
                ipdf.VrsOut("NOTE1", " ");
                ipdf.VrsOut("NOTE2", "：欠時数超過");
                ipdf.VrAttribute("NOTE3", ATTRIBUTE_KETTEN);
                ipdf.VrsOut("NOTE3", " ");
                ipdf.VrsOut("NOTE4", "：欠点科目");

                ipdf.VrsOut("DATE2", param.getTermKekka(db2)); // 出欠集計範囲

                ipdf.VrsOut("DETAIL1", "科目名"); // 項目
                ipdf.VrsOut("DETAIL2", "総時数"); // 項目

                ipdf.VrsOut("DETAIL1_1", param._item1Name); // 項目
                ipdf.VrsOut("DETAIL1_2", "単位数"); // 項目
                ipdf.VrsOut("DETAIL2_1", "欠時"); // 項目

                ipdf.VrsOut("T_TOTAL", param._form2Item4Name); // 項目
                ipdf.VrsOut("T_AVERAGE", param._form2Item5Name); // 項目
                ipdf.VrsOut("T_RANK1", "学級順位"); // 項目
                ipdf.VrsOut("T_RANK2", param._rankName + "順位"); // 項目

                ipdf.VrsOut("DATE3", param.getTermKekka(db2)); // 出欠の記録の集計範囲

                ipdf.VrsOut("ymd1", param.getNow(db2)); // 作成日
                ipdf.VrsOut("HR_TEACHER" + (getMS932ByteLength(hrInfo._staffName) > 30 ? "2" : ""), hrInfo._staffName);  //担任名
                for (int i = 1; i <= 8; i++) {
                    final String name1 = (String) param._d055Name1Map.get("0" + String.valueOf(i));
                    if (StringUtils.isBlank(name1)) {
                        continue;
                    }
                    final String field = "JOB" + String.valueOf(i) + (getMS932ByteLength(name1) > 8  ? "_2" : "_1");
                    ipdf.VrsOut(field, name1);
                }
            }
        }

        static class Form1 {

            private static boolean print(
                    final DB2UDB db2,
                    final IPdf ipdf,
                    final Param param,
                    final HRInfo hrInfo,
                    final List stulist
            ) {
                boolean hasData = false;
                setForm(ipdf, param._formname, 4, param);

                final List printSubclassList = new ArrayList(hrInfo._subclasses.values());
                for (final Iterator it = printSubclassList.iterator(); it.hasNext();) {
                    final SubClass subclass = (SubClass) it.next();
                    if (!param._isPrintSakiKamoku && param.getSubclassMst(subclass.keySubclasscd())._isSaki) {
                        it.remove();
                        if (param._isOutputDebug) {
                        	log.info(" skip saki " + subclass.keySubclasscd());
                        }
                    } else if (param._isNoPrintMoto && param.getSubclassMst(subclass.keySubclasscd())._isMoto) {
                        it.remove();
                        if (param._isOutputDebug) {
                        	log.info(" skip moto " + subclass.keySubclasscd());
                        }
                    }
                }
                
                if (param._isOutputDebug) {
                	log.info(" printSubclassList size = " + printSubclassList.size());
                }

                final List subclassListList;
                if (null != param._formname2) {
                    subclassListList = getSubClassListList(param, printSubclassList, param._form2MaxColumn);
                    if (subclassListList.size() > 0) {
                        final List lastSubclassList = (List) subclassListList.get(subclassListList.size() - 1);
                        log.debug(" oosugidesu! " + lastSubclassList.size() + " / " + param._formMaxColumn);
                        if (lastSubclassList.size() > param._formMaxColumn) {

                            // 最後のページの科目が多すぎて集計欄が表示できないので、集計欄のみ表示するためのダミーを追加
                            final List dummy = new ArrayList();
                            subclassListList.add(dummy);
                        }
                    }
                } else {
                    subclassListList = getSubClassListList(param, printSubclassList, param._formMaxColumn);
                }

                for (int pi = 0, pages = subclassListList.size(); pi < pages; pi++) {
                    final List subclassList = (List) subclassListList.get(pi);

                    log.info(" subclassList page = " + String.valueOf(pi + 1) + " / " + subclassListList.size());

                    final int maxCol;
                    if (pi < pages - 1 && null != param._formname2) {
                        // 右側集計欄は最後のページのみ
                        setForm(ipdf, param._formname2, 4, param);
                        maxCol = param._form2MaxColumn;
                    } else {
                        setForm(ipdf, param._formname, 4, param);
                        maxCol = param._formMaxColumn;
                    }
                    ipdf.addRecordField(param._recordField);
                    printHeader(db2, ipdf, param, hrInfo);
                    for (int sti = 0; sti < stulist.size(); sti++) {
                        final Student student = (Student) stulist.get(sti);
                        final int line = sti + 1; //gnumToLine(param, student._gnum);
                        printStudentName(ipdf, param, line, student);

                        if (pi == pages - 1) {
                            printStudentTotal(ipdf, param, line, student);
                            if (!PATTERN5.equals(param._outputPattern)) {
                                printHrInfo(ipdf, param, hrInfo);
                            }
                        }
                        printRemark(ipdf, param, line, student);

                    }

                    //教科毎の科目数をカウント
                    final Map classSubclassMap = new TreeMap();
                    int subClassCnt = 1;
                    String befClassCd = "";
                    for (int subi = 0, size = subclassList.size(); subi < size; subi++) {
                        final SubClass subclass = (SubClass) subclassList.get(subi);

                        if (!"".equals(befClassCd) && !befClassCd.equals(subclass._classcode)) {
                            subClassCnt = 1;
                        }

                        classSubclassMap.put(subclass._classcode, String.valueOf(subClassCnt));
                        subClassCnt++;
                        befClassCd = subclass._classcode;
                    }
                    befClassCd = "";
                    int subClassOrder = 1;
                    for (int subi = 0, size = subclassList.size(); subi < size; subi++) {

                        final SubClass subclass = (SubClass) subclassList.get(subi);
                        log.debug("p=" + pi + ", i=" + subi + ", subclasscd=" + subclass._subclasscode + " " + subclass._subclassabbv);
                        final boolean pat3isKariHyotei = PATTERN3.equals(param._outputPattern) && pat3SubclassIsKariHyotei(param, subclass, stulist);
                        final int abbvLen = KNJ_EditEdit.getMS932ByteLength(subclass._classabbv);
                        final int abbvStrCnt = StringUtils.defaultString(subclass._classabbv).length();
                        if (!"".equals(befClassCd) && !befClassCd.equals(subclass._classcode)) {
                            subClassOrder = 1;
                        }
                        final ClassAbbvFieldSet abbvFieldSet = setClassAbbv(Integer.parseInt((String)classSubclassMap.get(subclass._classcode)), subClassOrder, subclass._classabbv, abbvLen, abbvStrCnt);
                        printSubclass(ipdf, param, subi + 1, subclass, pat3isKariHyotei, abbvFieldSet);

                        for (int sti = 0; sti < stulist.size(); sti++) {
                            final Student student = (Student) stulist.get(sti);
                            final int line = sti + 1; // gnumToLine(param, student._gnum);
                            final ScoreDetail detail = (ScoreDetail) student._scoreDetails.get(subclass._subclasscode);
                            if (null != detail) {
                                printDetail(ipdf, param, student._schregno, detail, line, subi + 1, subclass, pat3isKariHyotei);
                            }
                        }

                        //学級平均・合計
                        printSubclassStat(ipdf, param, subi + 1, subclass);

                        ipdf.VrEndRecord();
                        subClassOrder++;
                        befClassCd = subclass._classcode;
                        hasData = true;
                    }
                    if (PATTERN3.equals(param._outputPattern) || PATTERN4.equals(param._outputPattern) || PATTERN5.equals(param._outputPattern) || PATTERN1.equals(param._outputPattern)) {
                        for (int i = subclassList.size(); i < maxCol; i++) {
                            //教科名
                            Form1.setRecordString(ipdf, "credit1", i + 1, "DUMMY");
                            ipdf.VrAttribute("credit1", "X=10000");
                            ipdf.VrEndRecord();
                            hasData = true;
                        }
                    }
                }
                return hasData;
            }

            public static boolean outputCsv(final DB2UDB db2, final List outputList, final Param param, final HRInfo hrInfo, final List stulist) {

                boolean hasData = false;

                final List printSubclassList = new ArrayList(hrInfo._subclasses.values());
                for (final Iterator it = printSubclassList.iterator(); it.hasNext();) {
                    final SubClass subclass = (SubClass) it.next();
                    if (!param._isPrintSakiKamoku && param.getSubclassMst(subclass.keySubclasscd())._isSaki) {
                        it.remove();
                    } else if (param._isNoPrintMoto && param.getSubclassMst(subclass.keySubclasscd())._isMoto) {
                        it.remove();
                    }
                }

                param._formMaxLine = printSubclassList.size();
                param._formMaxColumn = stulist.size();

                final List headerLineList = new ArrayList();
                final List header1Line = newLine(headerLineList);
                final String title = KNJ_EditDate.gengou(db2, Integer.parseInt(param._year)) + "年度　" + param._title;
                if (PATTERN4.equals(param._outputPattern)) {
                    header1Line.addAll(Arrays.asList(new String[] {"", title + "（成績の記録）"}));
                } else if (PATTERN5.equals(param._outputPattern)) {
                    header1Line.addAll(Arrays.asList(new String[] {"", title + "（欠時数と出欠の記録）"}));
                } else {
                    header1Line.addAll(Arrays.asList(new String[] {"", title}));
                }

                final List header2Line = newLine(headerLineList);
                header2Line.addAll(Arrays.asList(new String[] {hrInfo._hrName, "", "出欠集計範囲：" + param.getTermKekka(db2), "", "", "", "", "", "担任：" + StringUtils.defaultString(hrInfo._staffName)}));

                final List blockStudentName = new ArrayList();
                List nameLine0 = newLine(blockStudentName);
                List nameLine1 = newLine(blockStudentName);
                List nameLineRyou;
                List nameLineKaigai;
                List nameLineSuisen;
                List nameLineIb;
                List nameLineAhoushiki;
                if (PRGID_KNJD615H.equals(param._prgId)) {
                	nameLineRyou = newLine(blockStudentName);
                	nameLineKaigai = newLine(blockStudentName);
                	nameLineSuisen = newLine(blockStudentName);
                	nameLineIb = newLine(blockStudentName);
                	nameLineAhoushiki = newLine(blockStudentName);
                } else {
                	nameLineRyou = new ArrayList();
                	nameLineKaigai = new ArrayList();
                	nameLineSuisen = new ArrayList();
                	nameLineIb = new ArrayList();
                	nameLineAhoushiki = new ArrayList();
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
                    nameLine0.add("");
                }
                final int headerSize = nameLine0.size();
                setSameSize(nameLine1, headerSize);
                setSameSize(nameLineRyou, headerSize);
                setSameSize(nameLineKaigai, headerSize);
                setSameSize(nameLineSuisen, headerSize);
                setSameSize(nameLineIb, headerSize);
                setSameSize(nameLineAhoushiki, headerSize);

                for (final Iterator its = stulist.iterator(); its.hasNext();) {

                    final Student student = (Student) its.next();

                    nameLine0.add(student.getPrintAttendno());
//                    if (param._isTokiwagi) {
//                        nameLine1.add(StringUtils.defaultString(student._name) + StringUtils.defaultString(student._scholarshipName));
//                    } else {
                        nameLine1.add(student._name);
//                        if (PRGID_KNJD615H.equals(param._prgId)) {
//                            nameLineRyou.add(student._ryou);
//                            nameLineKaigai.add(student._kaigai);
//                            nameLineSuisen.add(student._suisen);
//                            nameLineIb.add(student._ib);
//                            nameLineAhoushiki.add(student._aHouShiki);
//                        }
//                    }
                }

                if (PRGID_KNJD615H.equals(param._prgId)) {
                    nameLine0.add("合計");
                    nameLine0.add("学級平均");
                    nameLine0.add(StringUtils.defaultString(param._rankName) + "平均");
                    nameLine0.add("欠点者数");
                    nameLine0.add("最高点");
                    nameLine0.add("最低点");
                } else if (PATTERN1.equals(param._outputPattern)) {
                    nameLine0.add("合計");
                    nameLine0.add("学級平均");
                    nameLine0.add(StringUtils.defaultString(param._rankName) + "平均");
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

                final List blockSubclassList = new ArrayList();

                String classabbvbefore = null;

                for (int coli = 0, size = printSubclassList.size(); coli < size; coli++) {

                    final List line1 = newLine(blockSubclassList);
                    List line2 = null;
                    List line3 = null;

                    final SubClass subclass = (SubClass) printSubclassList.get(coli);

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
                        line3 = setSameSize(newLine(blockSubclassList), line1.size());

                        line1.add(param._item1Name);
                        line3.add("欠時");
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
                        line3.add("欠時");
                    }

                    for (final Iterator its = stulist.iterator(); its.hasNext();) {

                        final Student student = (Student) its.next();

                        // 欠時
                        List scoreLine = null;
                        List absenceLine = null;
                        if (PRGID_KNJD615H.equals(param._prgId)) {
                            scoreLine = line1;
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
                        if (student._scoreDetails.containsKey(subclass._subclasscode)) {
                            final ScoreDetail detail = (ScoreDetail) student._scoreDetails.get(subclass._subclasscode);

                            if (null != scoreLine) {
                            	final boolean useD001 = param._d065Name1List.contains(subclass.keySubclasscd());
                                final String aster;
                                if (!useD001 && "990009".equals(param._testKindCd) && param._creditDrop && "1".equals(detail._score)) {
                                    aster = "*";
                                } else {
                                    aster = "";
                                }
                                final String printScore;
                                if (null == detail._score) {
                                	if (param._isMeikei && "*".equals(detail._scoreDi)) {
                                		printScore = StringUtils.defaultString(detail._suppScore, detail._scoreDi);
                                	} else {
                                		printScore = StringUtils.defaultString(detail._scoreDi);
                                	}
                                } else {
                                    printScore = StringUtils.defaultString(useD001 ? (String) param._d001Name1Map.get(detail._score) : detail._score);
                                }
                                scoreLine.add(aster + printScore);
                                if (!PRGID_KNJD615H.equals(param._prgId) && PATTERN3.equals(param._outputPattern)) {
                                    // 評定
                                    final boolean isPrint = !SEMEALL.equals(param._semester) || SEMEALL.equals(param._semester) && (pat3isKariHyotei && "1".equals(detail._provFlg) || !pat3isKariHyotei && null == detail._provFlg); // 学年末以外は対象。学年末は「仮評定ならPROV_FLG=1が対象。そうでなければPROV_FLG=NULLが対象。」
                                    line2.add(isPrint ? detail._karihyotei : "");
                                }
                            }

                            final boolean isOutputJisu = PATTERN1.equals(param._outputPattern) && param._isOutputStudentJisu && null != detail._jisu && !subclass.getJisu().equals(detail._jisu.toString());
                            if (null != absenceLine) {
                                final String val;
                                if (isOutputJisu) {
                                    val = (null == detail._ketsuji ? "0" : String.valueOf(detail._ketsuji.intValue())) + "/" + detail._jisu.toString();
                                } else {
                                    val = getKekkaString(param, detail._ketsuji);
                                }
                                absenceLine.add(val);
                            }
                        } else {
                            if (null != scoreLine) {
                                scoreLine.add("");
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
                    
                	final boolean useD001 = param._d065Name1List.contains(subclass.keySubclasscd());
                	if (!useD001) {
                        //学級平均・合計
                        if (PRGID_KNJD615H.equals(param._prgId)) {
                            line1.add(StringUtils.defaultString(subclass._scoretotal) + "/" + StringUtils.defaultString(subclass._scoreCount));
                            line1.add(subclass._scoreaverage);
                            line1.add(subclass._scoresubaverage);
                            line1.add(subclass._scoreFailCnt);
                            line1.add(subclass._scoreMax);
                            line1.add(subclass._scoreMin);
                        } else if (PATTERN3.equals(param._outputPattern)) {
                            line1.add(subclass._scoretotal);
                            line1.add(subclass._scoreCount);
                            line1.add(subclass._scoreaverage);
                            line1.add(subclass._scoresubaverage);
                            line1.add(param._useKetten ? subclass._scoreFailCnt : "");
                            line1.add(subclass.getJisu());
                        } else if (PATTERN4.equals(param._outputPattern)) {
                            line1.add(subclass._scoretotal);
                            line1.add(subclass._scoreCount);
                            line1.add(subclass._scoreaverage);
                            line1.add(subclass._scoresubaverage);
                            line1.add(param._useKetten ? subclass._scoreFailCnt : "");
                        } else if (PATTERN5.equals(param._outputPattern)) {
                            line1.add(subclass.getJisu());
                        } else if (PATTERN1.equals(param._outputPattern)) {
                            if (!StringUtils.isBlank(subclass._scoretotal) || !StringUtils.isBlank(subclass._scoreCount)) {
                                line1.add(StringUtils.defaultString(subclass._scoretotal) + "/" + StringUtils.defaultString(subclass._scoreCount));
                            } else {
                                line1.add("");
                            }
                            line1.add(subclass._scoreaverage);
                            line1.add(subclass._scoresubaverage);
                            line1.add(param._useKetten ? subclass._scoreFailCnt : "");
                            line1.add(subclass._scoreMax);
                            line1.add(subclass._scoreMin);
                        }
                	}

                    hasData = true;
                    classabbvbefore = subclass._classabbv;
                }

                final boolean outputScoreColumns = PATTERN1.equals(param._outputPattern) || PATTERN3.equals(param._outputPattern) || PATTERN4.equals(param._outputPattern);
                final boolean outputAttendColumns = PATTERN1.equals(param._outputPattern) || PATTERN3.equals(param._outputPattern) || PATTERN5.equals(param._outputPattern);
                final int scoreColumns = outputScoreColumns ? 5 : 0;
                final int attendColumns = outputAttendColumns ? 8 : 0;
                final int totalColumns = scoreColumns + attendColumns + 1;
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
                if (PATTERN1.equals(param._outputPattern) || PATTERN3.equals(param._outputPattern) || PATTERN4.equals(param._outputPattern) || PATTERN5.equals(param._outputPattern)) {
                    columnsStudentTotalHeader[j++].add("備考");
                }
                final List[] columnsStudentTotal = new List[totalColumns];
                for (int i = 0; i < columnsStudentTotal.length; i++) {
                    columnsStudentTotal[i] = new ArrayList();
                }
                for (final Iterator its = stulist.iterator(); its.hasNext();) {

                    final Student student = (Student) its.next();

                    int i = 0;
                    final AttendInfo attendInfo = student._attendInfo;
                    final String scoreAvg = student._scoreAvg; // param._isSapporo && !param._testKindCd.startsWith("99") ? "" : student._scoreAvg;
                    final String classRank = 0 >= student._classRank ? "" : String.valueOf(student._classRank);
                    final String rank = 0 >= student._rank ? "" : String.valueOf(student._rank);
                    final String countFail = !param._useKetten || 0 >= student._total._countFail ? "" : String.valueOf(student._total._countFail);
                    if (outputScoreColumns) {
                        columnsStudentTotal[i++].add(student._scoreSum);
                        columnsStudentTotal[i++].add(scoreAvg);
                        columnsStudentTotal[i++].add(classRank);
                        columnsStudentTotal[i++].add(rank);
                        columnsStudentTotal[i++].add(countFail);
                    }
                    if (outputAttendColumns) {
                        columnsStudentTotal[i++].add(zeroToNull(attendInfo._lesson));
                        columnsStudentTotal[i++].add(zeroToNull(attendInfo._suspend + attendInfo._mourning));
                        columnsStudentTotal[i++].add(zeroToNull(attendInfo._transDays));
                        columnsStudentTotal[i++].add(zeroToNull(attendInfo._mLesson));
                        columnsStudentTotal[i++].add(zeroToNull(attendInfo._sick));
                        columnsStudentTotal[i++].add(String.valueOf(attendInfo._present));
                        columnsStudentTotal[i++].add(zeroToNull(attendInfo._late));
                        columnsStudentTotal[i++].add(zeroToNull(attendInfo._early));
                    }

                    columnsStudentTotal[i++].add(student.getRemark(param));

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
                    String gavg = null;
                    String gtotal = null;
                    if (OUTPUT_RANK1.equals(param._outputRank)) {
                        gavg = hrInfo._avgHrAverage;
                        gtotal = hrInfo._avgHrTotal;
                    } else if (OUTPUT_RANK2.equals(param._outputRank)) {
                        gavg = hrInfo._avgGradeAverage;
                        gtotal = hrInfo._avgGradeTotal;
                    } else if (OUTPUT_RANK3.equals(param._outputRank)) {
                        gavg = hrInfo._avgCourseAverage;
                        gtotal = hrInfo._avgCourseTotal;
                    } else if (OUTPUT_RANK4.equals(param._outputRank)) {
                        gavg = hrInfo._avgMajorAverage;
                        gtotal = hrInfo._avgMajorTotal;
                    }

                    if (PRGID_KNJD615H.equals(param._prgId)) {
                        setSameSize(columnStudentTotalFooterList[0], 6);
                        columnStudentTotalFooterList[0].set(1, null == hrInfo._avgHrTotal ? "" : hrInfo._avgHrTotal);
                        setSameSize(columnStudentTotalFooterList[1], 2).set(1, StringUtils.defaultString(hrInfo._avgHrAverage));
                        setSameSize(columnStudentTotalFooterList[4], 4).set(3, StringUtils.defaultString(hrInfo._failHrTotal));
                        columnStudentTotalFooterList[0].set(4, null == hrInfo._maxHrTotal ? "" : hrInfo._maxHrTotal);
                        columnStudentTotalFooterList[0].set(5, null == hrInfo._minHrTotal ? "" : hrInfo._minHrTotal);
                    } else if (PATTERN1.equals(param._outputPattern)) {
                        setSameSize(columnStudentTotalFooterList[0], 7);
                        columnStudentTotalFooterList[0].set(1, null == hrInfo._avgHrTotal ? "" : hrInfo._avgHrTotal);
                        columnStudentTotalFooterList[0].set(2, null != gtotal ? "" : gtotal);
                        columnStudentTotalFooterList[0].set(3, hrInfo._failHrTotal); // param._isSapporo || null == hrInfo._failHrTotal ? "" : hrInfo._failHrTotal);
                        columnStudentTotalFooterList[0].set(4, null == hrInfo._maxHrTotal ? "" : hrInfo._maxHrTotal);
                        columnStudentTotalFooterList[0].set(5, null == hrInfo._minHrTotal ? "" : hrInfo._minHrTotal);
                        setSameSize(columnStudentTotalFooterList[1], 3);
                        columnStudentTotalFooterList[1].set(1, hrInfo._avgHrAverage); // param._isSapporo && !param._testKindCd.startsWith("99") || null == hrInfo._avgHrAverage ? "" : hrInfo._avgHrAverage);
                        columnStudentTotalFooterList[1].set(2, gavg); // param._isSapporo && !param._testKindCd.startsWith("99") || null == gavg ? "" : gavg);

                        setSameSize(columnStudentTotalFooterList[5], 8);
                        columnStudentTotalFooterList[5].set(7, "出席率 " + (null == hrInfo._perHrPresent ? "" : sishaGonyu(hrInfo._perHrPresent) + "%"));
                        setSameSize(columnStudentTotalFooterList[6], 8);
                        columnStudentTotalFooterList[6].set(7, "欠席率 " + (null == hrInfo._perHrSick ? "" : sishaGonyu(hrInfo._perHrSick) + "%"));
                    } else if (PATTERN3.equals(param._outputPattern)) {
                        setSameSize(columnStudentTotalFooterList[0], 3).set(2, StringUtils.defaultString(hrInfo._avgHrTotal));
                        setSameSize(columnStudentTotalFooterList[1], 3).set(2, StringUtils.defaultString(hrInfo._avgHrAverage));
                        setSameSize(columnStudentTotalFooterList[4], 5).set(4, StringUtils.defaultString(hrInfo._failHrTotal));
                    } else if (PATTERN4.equals(param._outputPattern)) {
                        columnStudentTotalFooterList[0].add(StringUtils.defaultString(hrInfo._avgHrTotal));
                        columnStudentTotalFooterList[0].add(0 >= hrInfo._avgHrCount ? "" : String.valueOf(hrInfo._avgHrCount));
                        columnStudentTotalFooterList[0].add(StringUtils.defaultString(hrInfo._avgHrAverage));
                        columnStudentTotalFooterList[0].add(StringUtils.defaultString(gavg));
                        columnStudentTotalFooterList[0].add(StringUtils.defaultString(hrInfo._failHrTotal));
                    }
                    joinColumnListArray(columnsStudentTotalAll, columnStudentTotalFooterList);
                }


                final List columnList = new ArrayList();
                columnList.addAll(blockStudentName);
                columnList.addAll(blockSubclassList);
                columnList.addAll(columnsStudentTotalAll);

                outputList.addAll(headerLineList);
                outputList.addAll(columnListToLines(columnList));
                newLine(outputList); // ブランク
                newLine(outputList); // ブランク

                return hasData;
            }

            private static List columnListToLines(final List columnList) {
                final List lines = new ArrayList();
                int maxLine = 0;
                for (int ci = 0; ci < columnList.size(); ci++) {
                    final List column = (List) columnList.get(ci);
                    maxLine = Math.max(maxLine, column.size());
                }
                for (int li = 0; li < maxLine; li++) {
                    lines.add(line(columnList.size()));
                }
                for (int ci = 0; ci < columnList.size(); ci++) {
                    final List column = (List) columnList.get(ci);
                    for (int li = 0; li < column.size(); li++) {
                        ((List) lines.get(li)).set(ci, column.get(li));
                    }
                }
                return lines;
            }

            private static List setColumnList(final List columnsList, final int column) {
                for (int i = columnsList.size(); i < column; i++) {
                    columnsList.add(new ArrayList());
                }
                return columnsList;
            }

            private static List joinColumnListArray(final List columnsList, final List[] columnStudentFooterList) {
                for (int i = 0; i < columnStudentFooterList.length; i++) {
                    ((List) columnsList.get(i)).addAll(columnStudentFooterList[i]);
                }
                return columnsList;
            }

            /**
             * ページ見出し・項目・欄外記述を印刷します。
             * @param ipdf
             * @param hrInfo
             */
            private static void printHeader(
                    final DB2UDB db2,
                    final IPdf ipdf,
                    final Param param,
                    final HRInfo hrInfo
            ) {
                ipdf.VrsOut("ymd1", param.getNow(db2)); // 作成日
//                if (Param._isDemo) {
//                    ipdf.VrsOut("DATE", "出欠集計範囲：" + param.getTermKekka(db2));  // 欠時の集計範囲
//                } else {
                    ipdf.VrsOut("DATE", param.getTermKekka(db2));  // 欠時の集計範囲
//                }
                if (!PATTERN3.equals(param._outputPattern) && !PATTERN4.equals(param._outputPattern) && !PATTERN5.equals(param._outputPattern)) {
//                    if (Param._isDemo) {
//                        ipdf.VrsOut("DATE2", "出欠の記録(" + param.getTermKekka(db2) + ")");
//                    } else {
                        ipdf.VrsOut("DATE2", param.getTermKekka(db2));
//                    }
                }
                if (param._isPrintPerfect) {
                    ipdf.VrsOut("ITEM_NAME_CREDIT", "単位数(満点)");
                }

                ipdf.VrsOut("HR_TEACHER" + (getMS932ByteLength(hrInfo._staffName) > 30 ? "2" : ""), hrInfo._staffName);  //担任名
                ipdf.VrsOut("HR_NAME", hrInfo._hrName);  //組名称
                if (param._hasCompCredit) {
                    ipdf.VrsOut("credit20", hrInfo._HrCompCredits);  // 履修単位
                }
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
//                    if (param._isSapporo && !param._testKindCd.startsWith("99")) {
//                        ipdf.VrAttribute("ITEM5", "X=10000");
//                    }
                    if (PATTERN1.equals(param._outputPattern)) {
                        ipdf.VrsOut("ITEM6", param._item1Name + "・" + param._item2Name);
                    }
//                    if (Param._isDemo) {
//                        ipdf.VrsOut("ITEM8", StringUtils.defaultString(param._rankName) + "平均");
//                    } else {
                        ipdf.VrsOut("ITEM8", param._rankName);
//                    }
                }
                if (PATTERN1.equals(param._outputPattern)) {
                    if (param._useKetten) {
                        ipdf.VrsOut("ITEM9", "欠点科目数");
                        ipdf.VrsOut("ITEM10", "欠点者数");
                    }
//                    if (Param._isDemo) {
//                        ipdf.VrsOut("ITEM11", "修得単位数");
//                    }
                }

                // 一覧表枠外の文言
                if (!PATTERN3.equals(param._outputPattern) && !PATTERN4.equals(param._outputPattern) && !PATTERN5.equals(param._outputPattern)) {
//                    if (Param._isDemo) {
//
//                    } else {
                        ipdf.VrAttribute("NOTE1",  ATTRIBUTE_KEKKAOVER);
//                    }
                    ipdf.VrsOut("NOTE1",  " " );
//                    if (Param._isDemo) {
//                        ipdf.VrsOut("NOTE2",  " ：欠時数超過者" );
//                    } else {
                        ipdf.VrsOut("NOTE2",  "：欠時数超過者" );
//                    }
                    if (param._useKetten) {
//                        if (Param._isDemo) {
//
//                        } else {
                            ipdf.VrAttribute("NOTE3",  ATTRIBUTE_KETTEN);
//                        }
                        ipdf.VrsOut("NOTE3",  " " );
//                        if (Param._isDemo) {
//                            ipdf.VrsOut("NOTE4",  " ：欠点" );
//                        } else {
                            ipdf.VrsOut("NOTE4",  "：欠点" );
//                        }
                    }
                }

                ipdf.VrsOut("TITLE", KNJ_EditDate.gengou(db2, Integer.parseInt(param._year)) + "年度　" + param._title);
                if (PATTERN4.equals(param._outputPattern)) {
                    ipdf.VrsOut("SUBTITLE", "（成績の記録）");
                } else if (PATTERN5.equals(param._outputPattern)) {
                    ipdf.VrsOut("SUBTITLE", "（欠時数と出欠の記録）");
                }
//                if (param._isMieken) {
//                    ipdf.VrsOut("NAME_TITLE", "名前");
//                }

                for (int i = 1; i <= 8; i++) {
                    final String name1 = (String) param._d055Name1Map.get("0" + String.valueOf(i));
                    if (StringUtils.isBlank(name1)) {
                        continue;
                    }
                    final String field = "JOB" + String.valueOf(i) + (getMS932ByteLength(name1) > 8  ? "_2" : "_1");
//                    if (Param._isDemo) {
//                        ipdf.VrsOutn(field, i, name1);
//                    } else {
                        if (hasField(ipdf, "JOB_IMG" + String.valueOf(i), param)) {
                            if (null != param._ineiWakuPath) {
                                ipdf.VrImageOut("JOB_IMG" + String.valueOf(i), param._ineiWakuPath);
                            }
                        }
                        ipdf.VrsOut(field, name1);
//                    }
                }
            }

            /**
             * 学級データの印字処理(学級平均)
             * @param ipdf
             */
            private static void printHrInfo(
                    final IPdf ipdf,
                    final Param param,
                    final HRInfo hrInfo
            ) {
                final int col = -1;

                if (PRGID_KNJD617I.equals(param._prgId)) {
                    if (null != hrInfo._avgHrTotal) {
                        svfsetString1(ipdf, "TOTAL1", param._formMaxLine + 2, "", col, hrInfo._avgHrTotal); // 学級合計
                    }
                    if (null != hrInfo._avgGradeTotal) {
                        svfsetString1(ipdf, "TOTAL1", param._formMaxLine + 3, "", col, hrInfo._avgGradeTotal); // 学年合計
                    }
                    if (null != hrInfo._avgCourseTotal) {
                        svfsetString1(ipdf, "TOTAL1", param._formMaxLine + 4, "", col, hrInfo._avgCourseTotal); // コース合計
                    }
                    //学級合計の母集団の生徒数
                    if (0 < hrInfo._avgHrCount) {
                        if (PATTERN4.equals(param._outputPattern)) {
                            svfsetString1(ipdf, "TOTAL1", param._formMaxLine + 2, "", col, String.valueOf(hrInfo._avgHrCount)); // 学級合計の母集団の生徒数
                        }
                    }
                    // 学級平均
                    if (null != hrInfo._avgHrAverage) {
                        svfsetString1(ipdf, "AVERAGE1", param._formMaxLine + 2, "", col, hrInfo._avgHrAverage); // 学級平均
                    }
                    if (null != hrInfo._avgGradeAverage) {
                        svfsetString1(ipdf, "AVERAGE1", param._formMaxLine + 3, "", col, hrInfo._avgGradeAverage); // 学年平均
                    }
                    if (null != hrInfo._avgCourseAverage) {
                        svfsetString1(ipdf, "AVERAGE1", param._formMaxLine + 4, "", col, hrInfo._avgCourseAverage); // コース平均
                    }
                    //欠点者数
                    if (null != hrInfo._failHrTotal) {
                        svfsetString1(ipdf, "TOTAL1", param._formMaxLine + 5, "", col, hrInfo._failHrTotal); // 欠点者数
                    }
                    //最高点
                    if (null != hrInfo._maxHrTotal) {
                        svfsetString1(ipdf, "TOTAL1", param._formMaxLine + 6, "", col, hrInfo._maxHrTotal); // 最高点
                    }
                    //最低点
                    if (null != hrInfo._minHrTotal) {
                        svfsetString1(ipdf, "TOTAL1", param._formMaxLine + 7, "", col, hrInfo._minHrTotal); // 最低点
                    }
                }
            }

            /**
             * 生徒別総合点・平均点・順位を印刷します。
             * @param ipdf
             */
            private static void printStudentTotal(
                    final IPdf ipdf,
                    final Param param,
                    final int line,
                    final Student student
            ) {

                if (!PATTERN5.equals(param._outputPattern)) {
                    if (null != student._scoreSum) {
                        ipdf.VrsOutn("TOTAL1", line, student._scoreSum);  //総合点
//                        if (param._isSapporo && !param._testKindCd.startsWith("99")) {
//                        } else {
                            ipdf.VrsOutn("AVERAGE1", line, student._scoreAvg);  //平均点
//                        }
                    }
                    //順位（学級）
                    if (1 <= student._classRank) {
                        ipdf.VrsOutn("CLASS_RANK1", line, String.valueOf(student._classRank));
                    }
                    if (PRGID_KNJD617I.equals(param._prgId)) {
                        //順位（学年）
                        if (1 <= student._gradeRank) {
                            ipdf.VrsOutn("GRADE_RANK1", line, String.valueOf(student._gradeRank));
                        }
                        //順位（コース）
                        if (1 <= student._courseRank) {
                            ipdf.VrsOutn("COURSE_RANK1", line, String.valueOf(student._courseRank));
                        }
                    }
                    //順位（学年orコース）
                    if (1 <= student._rank) {
                        ipdf.VrsOutn("RANK1", line, String.valueOf(student._rank));
                    }
                    //欠点科目数
                    if (param._useKetten) {
                        if (0 < student._total._countFail) {
                            ipdf.VrsOutn("FAIL1", line, String.valueOf(student._total._countFail));
                        }
                    }
                }
                if (!PATTERN4.equals(param._outputPattern)) {
                    printAttendInfo(ipdf, param, student._attendInfo, line, student);
                }
            }

            /**
             * 生徒の氏名・備考を印字
             * @param ipdf
             * @param hrInfo
             * @param stulist：List hrInfo._studentsのsublist
             */
            private static void printStudentName(
                    final IPdf ipdf,
                    final Param param,
                    final int line,
                    final Student student
            ) {
                ipdf.VrsOutn("NUMBER", line, student.getPrintAttendno());  // 出席番号
                // 学籍番号表記
                String nameNo = "1";
                String name = student._name;
//                if (param._isTokiwagi) {
//                    name = StringUtils.defaultString(name) + StringUtils.defaultString(student._scholarshipName);
//                }
                final int namelen = getMS932ByteLength(name);
                if ("1".equals(param._use_SchregNo_hyoji)) {
                    ipdf.VrsOutn("SCHREG_NO", line, student._schregno); // 学籍番号
                    nameNo = "2" + (getMS932ByteLength(name) > 16 ? "_2" : "_1"); // 学籍番号表示用の氏名フィールド
                }
//                if (PRGID_KNJD615H.equals(param._prgId)) {
//                    final String nameField = KNJ_EditEdit.getMS932ByteLength(name) > 16 ? "3_1" : KNJ_EditEdit.getMS932ByteLength(name) > 16 ? "2_1" : "1_1";
//                    ipdf.VrsOutn("name" + nameField, line, name);    // 氏名
//                    ipdf.VrsOutn("DORMITORY1", line, student._ryou);
//                    ipdf.VrsOutn("OVERSEA1", line, student._kaigai);
//                    ipdf.VrsOutn("RECOMMEND1", line, student._suisen);
//                    ipdf.VrsOutn("IB1", line, student._ib);
//                    ipdf.VrsOutn("METHOD1", line, student._aHouShiki);
//                } else if (PATTERN3.equals(param._outputPattern)) {
                if (PATTERN3.equals(param._outputPattern)) {
                    ipdf.VrsOutn("NAME" + nameNo, line, name);    // 氏名
//                } else if (Param._isDemo) {
//                    ipdf.VrsOutn("name", line, name);    // 氏名
                } else {
//                    if (param._isTokiwagi) {
//                        final String field = "NAME" + nameNo;
//                        final int fieldKeta;
//                        if ("name2_2".equals(field)) {
//                            fieldKeta = 20;
//                        } else if ("name2_1".equals(field)) {
//                            fieldKeta = 16;
//                        } else {
//                            fieldKeta = 20;
//                        }
//                        ipdf.VrsOutn(field, line, StringUtils.defaultString(student._name) + StringUtils.repeat(" ", fieldKeta - namelen) + StringUtils.defaultString(student._scholarshipName));    // 氏名
//                    } else {
                        ipdf.VrsOutn("NAME" + nameNo, line, name);    // 氏名
//                    }
                }
            }

            private static void printRemark(final IPdf ipdf, final Param param, final int line, final Student student) {
                final String remark = student.getRemark(param);
                final int keta = getMS932ByteLength(remark);
                if (PATTERN1.equals(param._outputPattern)) {
                    if (keta > 40) {
//                        if (Param._isDemo) {
//                            final String[] tokens = KNJ_EditEdit.get_token(remark, 40, 2);
//                            if (null != tokens) {
//                                for (int i = 0; i < Math.min(2, tokens.length); i++) {
//                                    ipdf.VrsOutn("REMARK2_" + String.valueOf(i + 1), line, tokens[i]);  // 備考
//                                }
//                            }
//                        } else {
                            ipdf.VrsOutn("REMARK2_1", line, remark);  // 備考
//                        }
                    } else {
                        ipdf.VrsOutn("REMARK" + (keta <= 20 ? "1" : keta <= 30 ? "1_2" : "1_3"), line, remark);  // 備考
                    }
                } else if (PATTERN3.equals(param._outputPattern)) {
                    ipdf.VrsOutn("REMARK" + (keta <= 20 ? "" : keta <= 30 ? "1_2" : "2_1"), line, remark);  // 備考
                } else if (PATTERN4.equals(param._outputPattern)){
                    ipdf.VrsOutn("REMARK1", line, remark);  // 備考
                } else if (PATTERN5.equals(param._outputPattern)) {
                    ipdf.VrsOutn("REMARK1" + (keta <= 70 ? "" : "_2"), line, remark);  // 備考
                }
            }

            /**
             * 該当科目名および科目別成績等を印字する処理
             * @param ipdf
             * @param subclass
             * @param line：科目の列番号
             * @param stulist：List hrInfo._studentsのsublist
             * @return
             */
            private static void printSubclass(
                    final IPdf ipdf,
                    final Param param,
                    final int col,
                    final SubClass subclass,
                    final boolean pat3isKariHyotei,
                    final ClassAbbvFieldSet abbvFieldSet
            ) {
                //教科名
                if (PATTERN5.equals(param._outputPattern) || PATTERN4.equals(param._outputPattern)) {
                    Form1.setRecordString(ipdf, "GRPCD", col, subclass._classcode);
                    Form1.setRecordString(ipdf, "course" + abbvFieldSet._fieldNumer, col, abbvFieldSet._setAbbv);
                } else {
                    Form1.setRecordString(ipdf, "course1", col, subclass._classabbv);
                }
                //科目名
                if (PATTERN4.equals(param._outputPattern) || PATTERN5.equals(param._outputPattern)) {
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
                    final String subclassfield = PATTERN3.equals(param._outputPattern) ? "SUBCLASS" : "subject1";
                    if (subclass._electdiv) { ipdf.VrAttribute(subclassfield, ATTRIBUTE_ELECTDIV); }
                    Form1.setRecordString(ipdf, subclassfield, col, subclass._subclassabbv);
                    if (subclass._electdiv) { ipdf.VrAttribute(subclassfield, ATTRIBUTE_NORMAL); }
                }
                //単位数
                final String creditStr = subclass.getPrintCredit(param);
                final int creditStrLen = getMS932ByteLength(creditStr);
                final int creditFieldKeta = getFieldKeta(ipdf, "credit1", param);
                final String creditField = (creditFieldKeta < creditStrLen && creditFieldKeta < getFieldKeta(ipdf, "credit1_2", param)) ? "credit1_2" :"credit1";
                Form1.setRecordString(ipdf, creditField, col, creditStr);
                Form1.setRecordString(ipdf, fieldClassTeacher(ipdf, param, subclass._staffname), col, subclass._staffname);
                if (PATTERN1.equals(param._outputPattern)) {
                    //授業時数
                    Form1.setRecordString(ipdf, "lesson1", col, subclass.getJisu());
                }
                //項目名
                if (!PATTERN4.equals(param._outputPattern) && !PATTERN5.equals(param._outputPattern)) {
                    final int mojisu = KNJ_EditEdit.getMS932ByteLength(param._item1Name);
                    final int field1Keta = getFieldKeta(ipdf, "ITEM1", param);
                    final String fieldname;
                    if (field1Keta < mojisu && field1Keta < getFieldKeta(ipdf, "ITEM1_2", param)) {
                        fieldname = "ITEM1_2";
                    } else {
                        fieldname = "ITEM1";
                    }
                    Form1.setRecordString(ipdf, fieldname, col, param._item1Name);
                }
                if (PATTERN3.equals(param._outputPattern)) {
                    Form1.setRecordString(ipdf, "ITEM2_2", col, pat3isKariHyotei ? "仮評定" : "評定");
                    Form1.setRecordString(ipdf, "ITEM3", col, "欠時");
                } else if (!PATTERN4.equals(param._outputPattern) && !PATTERN5.equals(param._outputPattern)) {
                    Form1.setRecordString(ipdf, "ITEM2", col, param._item2Name);
                }
            }

            /**
             * パターン3（仮評定付き）で科目が仮評定を表示するならtrue、そうでなければfalse
             * @param param
             * @param subclass 科目
             * @param studentList 生徒のリスト
             * @return パターン3（仮評定付き）で科目が仮評定を表示するならtrue、そうでなければfalse
             */
            private static boolean pat3SubclassIsKariHyotei(final Param param, final SubClass subclass, final List studentList) {
                if (!"9".equals(param._semester)) {
                    return true; // 学年末以外は仮評定を表示
                }
                int valueCount = 0;
                int provFlgCount = 0;
                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();
                    final ScoreDetail detail = (ScoreDetail) student._scoreDetails.get(subclass._subclasscode);
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
                final boolean isKari = valueCount == 0 || provFlgCount > 0;
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

            public static void printSubclassStat(final IPdf ipdf, final Param param, final int col, final SubClass subclass) {
            	final boolean useD001 = param._d065Name1List.contains(subclass.keySubclasscd());
            	if (useD001) {
            		return;
            	}
                if (PRGID_KNJD617I.equals(param._prgId)) {
                    Form1.setRecordString(ipdf, "AVE_CLASS", col, subclass._scoreaverage);
                    Form1.setRecordString(ipdf, "AVE_GRADE", col, subclass._scoresubaverageGrade);
                    Form1.setRecordString(ipdf, "AVE_COURSE", col, subclass._scoresubaverageCourse);
                    if (!StringUtils.isBlank(subclass._scoretotal) || !StringUtils.isBlank(subclass._scoreCount)) {
                        Form1.setRecordString(ipdf, "TOTAL_SUBCLASS", col, StringUtils.defaultString(subclass._scoretotal) + "/" + StringUtils.defaultString(subclass._scoreCount));
                    }
                    Form1.setRecordString(ipdf, "MAX_SCORE", col, subclass._scoreMax);
                    Form1.setRecordString(ipdf, "MIN_SCORE", col, subclass._scoreMin);
                    if (param._useKetten) {
                        Form1.setRecordString(ipdf, "FAIL_STD", col, subclass._scoreFailCnt);
                    }
                } else if (PRGID_KNJD615H.equals(param._prgId)) {
                    Form1.setRecordString(ipdf, "TOTAL_SUBCLASS1", col, subclass._scoretotal);
                    Form1.setRecordString(ipdf, "TOTAL_SUBCLASS2", col, subclass._scoreCount);
                    Form1.setRecordString(ipdf, "TOTAL_SUBCLASS3", col, subclass._scoreaverage);
                    Form1.setRecordString(ipdf, "TOTAL_SUBCLASS4", col, subclass._scoresubaverage);
                    Form1.setRecordString(ipdf, "TOTAL_SUBCLASS5", col, subclass._scoreFailCnt);
                    Form1.setRecordString(ipdf, "TOTAL_SUBCLASS6", col, subclass.getJisu());
                    Form1.setRecordString(ipdf, "MAX_SCORE", col, subclass._scoreMax);
                    Form1.setRecordString(ipdf, "MIN_SCORE", col, subclass._scoreMin);
                } else if (PATTERN3.equals(param._outputPattern)) {
                    Form1.setRecordString(ipdf, "TOTAL_SUBCLASS1", col, subclass._scoretotal);
                    Form1.setRecordString(ipdf, "TOTAL_SUBCLASS2", col, subclass._scoreCount);
                    Form1.setRecordString(ipdf, "TOTAL_SUBCLASS3", col, subclass._scoreaverage);
                    Form1.setRecordString(ipdf, "TOTAL_SUBCLASS4", col, subclass._scoresubaverage);
                    if (param._useKetten) {
                        Form1.setRecordString(ipdf, "TOTAL_SUBCLASS5", col, subclass._scoreFailCnt);
                    }
                    Form1.setRecordString(ipdf, "TOTAL_SUBCLASS6", col, subclass.getJisu());
                } else if (PATTERN4.equals(param._outputPattern)) {
                    Form1.setRecordString(ipdf, "TOTAL_SUBCLASS", col, subclass._scoretotal);
                    Form1.setRecordString(ipdf, "NUM", col, subclass._scoreCount);
                    Form1.setRecordString(ipdf, "AVE_CLASS2", col, subclass._scoreaverage);
                    Form1.setRecordString(ipdf, "AVE_SUBCLASS2", col, subclass._scoresubaverage);
                    if (param._useKetten) {
                        Form1.setRecordString(ipdf, "FAIL_STD", col, subclass._scoreFailCnt);
                    }
                } else if (PATTERN5.equals(param._outputPattern)) {
                    Form1.setRecordString(ipdf, "APPOINT_TIMES", col, subclass.getJisu());
                } else if (PATTERN1.equals(param._outputPattern)) {
                    Form1.setRecordString(ipdf, "AVE_CLASS", col, subclass._scoreaverage);
                    Form1.setRecordString(ipdf, "AVE_SUBCLASS", col, subclass._scoresubaverage);
                    if (!StringUtils.isBlank(subclass._scoretotal) || !StringUtils.isBlank(subclass._scoreCount)) {
                        Form1.setRecordString(ipdf, "TOTAL_SUBCLASS", col, StringUtils.defaultString(subclass._scoretotal) + "/" + StringUtils.defaultString(subclass._scoreCount));
                    }
                    Form1.setRecordString(ipdf, "MAX_SCORE", col, subclass._scoreMax);
                    Form1.setRecordString(ipdf, "MIN_SCORE", col, subclass._scoreMin);
                    if (param._useKetten) {
                        Form1.setRecordString(ipdf, "FAIL_STD", col, subclass._scoreFailCnt);
                    }
                }
            }

            /**
             * 生徒別科目別素点・評定・欠時数等を印刷します。
             * @param ipdf
             * @param line 生徒の行番
             */
            private static void printDetail(
                    final IPdf ipdf,
                    final Param param,
                    final String schregno,
                    final ScoreDetail detail,
                    final int line,
                    final int col,
                    final SubClass subclass,
                    final boolean pat3isKariHyotei
            ) {
            	final boolean useD001 = param._d065Name1List.contains(subclass.keySubclasscd());
                final boolean isPrintScore = !PATTERN5.equals(param._outputPattern);
                final boolean isPrintKariHyotei = PATTERN3.equals(param._outputPattern);
                if (isPrintScore) {
                    final String scoreField = PATTERN3.equals(param._outputPattern) ? "GRADING1_" : "SCORE";
                    if (param._useKetten && ScoreDetail.isFail(param, detail)) { ipdf.VrAttribute(scoreField + String.valueOf(line) + "", ATTRIBUTE_KETTEN); }
                    final String aster;
                    if (!useD001 && "990009".equals(param._testKindCd) && param._creditDrop && "1".equals(detail._score)) {
                        aster = "*";
                    } else {
                        aster = "";
                    }
                    final String printScore;
                    if (null == detail._score) {
                    	if (param._isMeikei && "*".equals(detail._scoreDi)) {
                    		printScore = StringUtils.defaultString(detail._suppScore, detail._scoreDi);
                    	} else {
                    		printScore = StringUtils.defaultString(detail._scoreDi);
                    	}
                    } else {
                        printScore = StringUtils.defaultString(useD001 ? (String) param._d001Name1Map.get(detail._score) : detail._score);
                    }
//                    if (Param._isDemo) {
//                        Form1.setRecordString(ipdf, scoreField + String.valueOf(line) + "", col, line, aster + printScore);
//                    } else {
                        Form1.setRecordString(ipdf, scoreField + String.valueOf(line) + "", col, aster + printScore);
//                    }
                    if (param._useKetten && ScoreDetail.isFail(param, detail)) { ipdf.VrAttribute(scoreField + String.valueOf(line) + "", ATTRIBUTE_NORMAL); }
                    if (isPrintKariHyotei) {
                        // 評定
                        final boolean isPrint = !SEMEALL.equals(param._semester) || SEMEALL.equals(param._semester) && (pat3isKariHyotei && "1".equals(detail._provFlg) || !pat3isKariHyotei && null == detail._provFlg); // 学年末以外は対象。学年末は「仮評定ならPROV_FLG=1が対象。そうでなければPROV_FLG=NULLが対象。」
                        if (isPrint && null != detail._karihyotei) {
                            Form1.setRecordString(ipdf, "VALUE1_" + String.valueOf(line) + "", col, detail._karihyotei);
                        }
                    }
                }

                // 欠時
                final boolean isPrintKekka = !PATTERN4.equals(param._outputPattern);
                final boolean isOutputJisu = PATTERN1.equals(param._outputPattern) && param._isOutputStudentJisu && null != detail._jisu && !subclass.getJisu().equals(detail._jisu.toString());
                if (isPrintKekka && null != detail._ketsuji || isOutputJisu) {
                    final String fieldHead;
                    String pf = "";
                    final String val;
                    if (isOutputJisu) {
                        val = (null == detail._ketsuji ? "0" : String.valueOf(detail._ketsuji.intValue())) + "/" + detail._jisu.toString();
                    } else {
                        val = getKekkaString(param, detail._ketsuji);
                    }
                    final boolean isLongField = param._definecode.absent_cov == 3 || param._definecode.absent_cov == 4;
                    final int vallen = getMS932ByteLength(val);
                    if (PRGID_KNJD615H.equals(param._prgId)) {
                        fieldHead = "ABSENT2_";
                    } else if (PATTERN3.equals(param._outputPattern)) {
                        fieldHead = "ABSENT";
                        if (isLongField) {
                            pf = "_2";
                        } else {
                            pf = "_1";
                        }
                    } else if (PATTERN5.equals(param._outputPattern)) {
                    	fieldHead = "SCORE";
                    	if (vallen > 5 && hasField(ipdf, "SCORE1_3", param)) {
                            pf = "_3";
                    	} else if (vallen > 3 || isLongField) {
                    		pf = "_2";
                    	}
                    } else if (PATTERN1.equals(param._outputPattern)) {
                        if (isOutputJisu) {
                            if (vallen > 5) {
                                fieldHead = "kekka3_";
                            } else {
                                fieldHead = "kekka2_";
                            }
                        } else if (isLongField) {
                            fieldHead = "kekka2_";
                        } else {
                            fieldHead = "kekka";
                        }
                    } else {
                        if (isLongField) {
                            fieldHead = "kekka2_";
                        } else {
                            fieldHead = "kekka";
                        }
                    }
                    final String field = fieldHead + String.valueOf(line) + pf;
                    if (detail._isOver) { ipdf.VrAttribute(field, ATTRIBUTE_KEKKAOVER); }
                    if (isOutputJisu) {
                        Form1.setRecordString(ipdf, field, col, val);
//                    } else if (Param._isDemo) {
//                        Form1.setRecordString(ipdf, field, col, line, val);
                    } else {
                        Form1.setRecordString(ipdf, field, col, val);
                    }
                    if (detail._isOver) { ipdf.VrAttribute(field, ATTRIBUTE_NORMAL); }
                }
            }

            public static int setRecordString(IPdf ipdf, String field, int gyo, int retsu, String data) {
//                if (Param._isDemo) {
//                    return ipdf.VrsOutn(field, gyo, data);
//                }
                return ipdf.setRecordString(field, gyo, data);
            }

            public static int setRecordString(IPdf ipdf, String field, int gyo, String data) {
//                if (Param._isDemo) {
//                    return ipdf.VrsOutn(field, gyo, data);
//                }
                return ipdf.setRecordString(field, gyo, data);
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

    private static class SubclassMst {
        final String _subclasscode;
        final String _classabbv;
        final String _classname;
        final String _subclassabbv;
        final String _subclassname;
        final boolean _isSaki;
        final boolean _isMoto;
        public SubclassMst(final String subclasscode, final String classabbv, final String classname, final String subclassabbv, final String subclassname,
                final boolean isSaki, final boolean isMoto) {
            _subclasscode = subclasscode;
            _classabbv = classabbv;
            _classname = classname;
            _subclassabbv = subclassabbv;
            _subclassname = subclassname;
            _isSaki = isSaki;
            _isMoto = isMoto;
        }
    }

    protected Param createParam(final HttpServletRequest request, final DB2UDB db2) {
        log.fatal("$Revision: 72702 $ $Date: 2020-03-03 21:50:17 +0900 (火, 03 3 2020) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        Param param = new Param(request, db2);
        return param;
    }

    protected static class Param {
        /** 年度 */
        final String _year;
        /** 学期 */
        final String _semester;
        /** LOG-IN時の学期（現在学期）*/
        final String _semeFlg;

        /** 学年 */
        final String _grade;
        final String[] _classSelected;
        /** 出欠集計日付 */
        final String _sdate;
        final String _date;
        final String _testKindCd;
        final String _scoreDiv;

        final String _dataSelect;

        /** 総合順位出力 1:学級 2:学年 3:コース 4:学科 */
        final String _outputRank;
        /** 順位の基準点 1:総合点 2:平均点 */
        final String _outputKijun;
        /** フォーム 1:科目固定型 2:科目変動型 3:成績と出欠の記録 4:欠時数と出欠の記録 5:仮評定と出欠の記録 */
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

        final String _cmd;

        /** フォーム生徒行数 */
        int _formMaxLine;

        /** 科目数 */
        int _formMaxColumn;
        int _form2MaxColumn; // 右側集計欄のないフォーム
        final String _formname;
        String _formname2; // 右側集計欄のないフォーム
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
        final String _schoolName;
        final String _takeSemes;
        final String _printKekka0;
        final String _documentroot;

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

        private KNJ_Get_Info _getinfo;

//        final boolean _isKumamoto;
//        final boolean _isKyoto;
//        final boolean _isHosei;
//        final boolean _isSapporo;
//        final boolean _isMieken;
//        final boolean _isTokiwagi;
//        final boolean _isHagoromo;
        final boolean _isMeikei;
        final boolean _useKetten;
        final boolean _isOutputDebug;

        private String _rankName;
        private String _rankFieldName;
        private String _avgDiv;
        final String _d054Namecd2Max;
        final String _sidouHyoji;
        final Map _d055Name1Map;
        final List _d065Name1List;
        final Map _d001Name1Map;
        private Map _subclassMst;
        private boolean _isPrintSakiKamoku;
        private boolean _isNoPrintMoto;

        private final String SUBCLASSCD999999;

        private String _title;
        private String _item1Name;  // 明細項目名
        final String _item2Name;  // 明細項目名
        final String _item4Name;  // 総合点欄項目名
        final String _item5Name;  // 平均点欄項目名
        final String _form2Item4Name;  // 平均点欄項目名
        final String _form2Item5Name;  // 平均点欄項目名
        private boolean _creditDrop;
        private boolean _isGakunenMatu; // 学年末はTrueを戻します。
        private boolean _hasCompCredit; // 履修単位数/修得単位数なし
        final boolean _hasJudgementItem; // 判定会議資料用の項目あり
        final boolean _enablePringFlg; // 合併元科目の「印刷しない」フラグを使用する場合はTrueを戻します。
        private int _failValue; // 欠点 100段階：30未満 5段階： 2未満
        final String _knjd615vPrintPerfect;
        final boolean _isPrintPerfect;
        final Map _psMap = new HashMap();
        final Map _d053Name1Map;
        private String _currentform;
        private Map _formFieldInfoMap = new HashMap();

        final String _ineiWakuPath;

        final String _printreport;
        final String _logindate;
        final String _gdatgradename;
//        /**
//         * @deprecated 宮部さん作成のKNJD615V_0.frm使用デモ用。デモがすんだらカット
//         */
//        protected static boolean _isDemo = false;

        Param(final HttpServletRequest request, final DB2UDB db2) {

            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _semeFlg = request.getParameter("SEME_FLG");
            _grade = request.getParameter("GRADE");
            _classSelected = request.getParameterValues("CLASS_SELECTED");
            _sdate = KNJ_EditDate.H_Format_Haifun(request.getParameter("SDATE"));
            _date = KNJ_EditDate.H_Format_Haifun(request.getParameter("DATE"));
            _testKindCd = request.getParameter("TESTKINDCD");  //テスト・成績種別
            _outputRank = request.getParameter("OUTPUT_RANK");
            _creditDrop = (request.getParameter("OUTPUT4") != null);
            _documentroot = request.getParameter("DOCUMENTROOT");
            _notOutputSougou = "1".equals(request.getParameter("OUTPUT5"));
            _useSlumpSdivDatSlump = "1".equals(request.getParameter("OUTPUT6"));
            _kettenshaSuuNiKessaShaWoFukumenai = "1".equals(request.getParameter("OUTPUT7"));
            _outputBiko = "1".equals(request.getParameter("OUTPUT_BIKO"));
            _bikoKind = request.getParameter("BIKO_KIND");
            _outputPattern = StringUtils.isBlank(request.getParameter("OUTPUT_PATERN")) ? PATTERN1 : request.getParameter("OUTPUT_PATERN"); // _isDemo ? PATTERN1 : StringUtils.isBlank(request.getParameter("OUTPUT_PATERN")) ? PATTERN1 : request.getParameter("OUTPUT_PATERN");
            _outputOrder = StringUtils.defaultString(request.getParameter("OUTPUT_ORDER"), "1");
            _assess = (request.getParameter("ASSESS") != null) ? new Float(request.getParameter("ASSESS1")) : new Float(4.3);
            _takeSemes = PATTERN2.equals(_outputPattern) ? request.getParameter("TAKESEMES") : null;
            _printKekka0 = request.getParameter("PRINT_KEKKA0");
            _notEmptyLine = request.getParameter("NOT_EMPTY_LINE");
            _cmd = request.getParameter("cmd");
            _prgId = request.getParameter("PRGID");
            _dataSelect = StringUtils.defaultString(request.getParameter("DATA_SELECT"));
            if ("1".equals(_takeSemes)) {
                SUBCLASSCD999999 = "99999A";
            } else if ("2".equals(_takeSemes)) {
                SUBCLASSCD999999 = "99999B";
            } else {
                SUBCLASSCD999999 = "999999";
            }
            _recordField = new String[] {};
            if (PRGID_KNJD617I.equals(_prgId)) {
                _formname = "KNJD617I.frm";
                _formMaxLine = 45;
                _formMaxColumn = 20;
            } else if (PRGID_KNJD615H.equals(_prgId)) {
                _formname = "KNJD615H.frm";
                _formMaxLine = 50;
                _formMaxColumn = 20;
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
            } else { // PATTERN1.equals(_outputPattern) || "KNJD615L".equals(request.getParameter("PRGID"))
//                if (_isDemo) {
//                    _formname = "KNJD615V_0.frm";
//                } else {
                    _formname = "KNJD615V_1.frm";
//                }
                _formname2 = "KNJD615V_1_2.frm";
                _formMaxLine = 45;
                _formMaxColumn = 20;
                _form2MaxColumn = 32;
            }
            _outputKijun = request.getParameter("OUTPUT_KIJUN");
            _checkKettenDiv = request.getParameter("checkKettenDiv");
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _use_SchregNo_hyoji = request.getParameter("use_SchregNo_hyoji");
            _knjd615vPrintNullRemark = request.getParameter("knjd615vPrintNullRemark");
            _knjd615vPrintPerfect = request.getParameter("knjd615vPrintPerfect");
            _isOutputStudentJisu = "1".equals(request.getParameter("OUTPUT_STUDENT_JISU"));
            _d053Name1Map = KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, " SELECT NAMECD2, NAME1 FROM NAME_MST WHERE NAMECD1 = 'D053' "), "NAMECD2", "NAME1");
            _printreport = request.getParameter("PRINT_REPORT");
            _logindate = StringUtils.defaultString(request.getParameter("LOGIN_DATE"));

            try {
                _knjSchoolMst = new KNJSchoolMst(db2, _year);
            } catch (Exception e) {
                log.warn("学校マスタ取得でエラー", e);
            }
            _getinfo = new KNJ_Get_Info();
            _schoolName = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' "));
            log.info(" schoolName = " + _schoolName);
//            _isKumamoto = "kumamoto".equals(_schoolName);
//            _isKyoto = "kyoto".equals(_schoolName);
//            _isHosei = "HOUSEI".equals(_schoolName);
//            _isSapporo = "sapporo".equals(_schoolName);
//            _isMieken = "mieken".equals(_schoolName);
//            _isTokiwagi = "tokiwagi".equals(_schoolName);
//            _isHagoromo = "hagoromo".equals(_schoolName);
            _isMeikei = "meikei".equals(_schoolName);
            _useKetten = true; // !_isSapporo;

            // 出欠の情報
            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
            _attendParamMap.put("grade", _grade);
            _attendParamMap.put("absenceDiv", "2");
            _attendParamMap.put("useTestCountflg", "TESTITEM_MST_COUNTFLG_NEW_SDIV");

            _isOutputDebug = "1".equals(getDbPrginfoProperties(db2, "outputDebug"));
            log.info(" isOutputDebug = " + _isOutputDebug);

            final Map d054Max = KnjDbUtils.firstRow(KnjDbUtils.query(db2, " SELECT NAMECD2, NAME1 FROM NAME_MST WHERE NAMECD1 = 'D054' AND NAMECD2 = (SELECT MAX(NAMECD2) AS NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'D054') "));
            _d054Namecd2Max = KnjDbUtils.getString(d054Max, "NAMECD2");
            _sidouHyoji = KnjDbUtils.getString(d054Max, "NAME1");

            _d055Name1Map = KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, " SELECT NAMECD2, NAME1 FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'D055' "), "NAMECD2", "NAME1");
            _d065Name1List = KnjDbUtils.getColumnDataList(KnjDbUtils.query(db2, " SELECT NAME1 FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'D065' "), "NAME1");
            _d001Name1Map = KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, " SELECT NAMECD2, NAME1 FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'D001' "), "NAMECD2", "NAME1");
            _definecode = createDefineCode(db2);
            //  学期名称、範囲の取得
            final KNJ_Get_Info.ReturnVal returnval = _getinfo.Semester(db2, _year, _semester);
            _semesterName = StringUtils.defaultString(returnval.val1);  //学期名称
            // 年度の開始日
            _yearDateS = _sdate;
            if (null == _yearDateS) {
                final KNJ_Get_Info.ReturnVal returnval1 = _getinfo.Semester(db2, _year, SEMEALL);
                _yearDateS = returnval1.val2;
            }
            // テスト名称
            _testItem = getTestItem(db2, _year, _semester, _testKindCd);
            log.debug(" testKindCd = " + _testKindCd + ", testitem = " + _testItem);
            _item4Name = "合計点";
            _item5Name = "平均点";
            _form2Item4Name = _item4Name;
            _form2Item5Name = _item5Name;

            final String ineiWakuPath = _documentroot + "/image/KNJD615_keninwaku2.jpg";
            final File ineiWakuFile = new File(ineiWakuPath);
            log.info(" ineiWakuFile exists? " + ineiWakuFile.exists());
            if (!ineiWakuFile.exists()) {
                _ineiWakuPath = null;
            } else {
                _ineiWakuPath = ineiWakuPath;
            }

            _item2Name = "欠時";
            _hasJudgementItem = false;
            _enablePringFlg = false;
            _scoreDiv = _testKindCd.substring(4);
            _isGakunenMatu = SCORE_DIV_09.equals(_scoreDiv);
            _hasCompCredit = SCORE_DIV_09.equals(_scoreDiv);
            _isPrintPerfect = PATTERN1.equals(_outputPattern) && SCORE_DIV_01.equals(_scoreDiv) && "1".equals(_knjd615vPrintPerfect);
            _failValue = 30;
            if (SCORE_DIV_01.equals(_scoreDiv) || SCORE_DIV_02.equals(_scoreDiv)) {
                final String sotenName = PRGID_KNJD617I.equals(_prgId) ? "得点" : "素点";
                _item1Name = StringUtils.defaultString((String) _d053Name1Map.get(_scoreDiv), SCORE_DIV_02.equals(_scoreDiv) ? "平常点" : sotenName);
                _creditDrop = false;
                _title = _semesterName + " " + _testItem._testitemname + " 成績一覧表";
            } else if (SCORE_DIV_08.equals(_scoreDiv)) {
                _item1Name = "評価";
                _title = _semesterName + " " + _testItem._testitemname + " 成績一覧表";
            } else if (SCORE_DIV_09.equals(_scoreDiv)) {
                _item1Name = "評定";
                _title = _testItem._testitemname + "  成績一覧表（評定）";
            }

            if (OUTPUT_RANK1.equals(_outputRank)) {
                _rankName = "学級";
                if (OUTPUT_KJUN2.equals(_outputKijun)) {
                    _rankFieldName = "CLASS_AVG_RANK";
                } else {
                    _rankFieldName = "CLASS_RANK";
                }
                _avgDiv = "2";
            } else if (OUTPUT_RANK2.equals(_outputRank) || PRGID_KNJD617I.equals(_prgId)) {
                _rankName = "学年";
                if (OUTPUT_KJUN2.equals(_outputKijun)) {
                    _rankFieldName = "GRADE_AVG_RANK";
                } else {
                    _rankFieldName = "GRADE_RANK";
                }
                _avgDiv = "1";
            } else if (OUTPUT_RANK3.equals(_outputRank)) {
                _rankName = "コース";
                if (OUTPUT_KJUN2.equals(_outputKijun)) {
                    _rankFieldName = "COURSE_AVG_RANK";
                } else {
                    _rankFieldName = "COURSE_RANK";
                }
                _avgDiv = "3";
            } else if (OUTPUT_RANK4.equals(_outputRank)) {
                _rankName = "学科";
                if (OUTPUT_KJUN2.equals(_outputKijun)) {
                    _rankFieldName = "MAJOR_AVG_RANK";
                } else {
                    _rankFieldName = "MAJOR_RANK";
                }
                _avgDiv = "4";
            }
            log.debug("順位名称=" + _rankName);
            log.debug("rankFieldName=" + _rankFieldName);
            log.debug("avgDiv=" + _avgDiv);
            setSubclassMst(db2);
            setPrintSakiKamoku(db2);
            loadNameMstD016(db2);
            _gdatgradename = loadGdatGName(db2, _year, _grade);
        }

        private static String getDbPrginfoProperties(final DB2UDB db2, final String propName) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJD617I' AND NAME = '" + propName + "' "));
        }

        /** 作成日 */
        public String getNow(final DB2UDB db2) {
            final StringBuffer stb = new StringBuffer();
            final Date date = new Date();
            final SimpleDateFormat sdfY = new SimpleDateFormat("yyyy");
            stb.append(KNJ_EditDate.gengou(db2, Integer.parseInt(sdfY.format(date))));
            final SimpleDateFormat sdf = new SimpleDateFormat("年M月d日H時m分");
            stb.append(sdf.format(date));
            return stb.toString();
        }

        /** 欠時の集計範囲 */
        public String getTermKekka(final DB2UDB db2) {
            return KNJ_EditDate.h_format_JP(db2, _yearDateS) + FROM_TO_MARK + KNJ_EditDate.h_format_JP(db2, _date);
        }

        private DecimalFormat getAbsentFmt() {
            DecimalFormat absentFmt;
            switch (_definecode.absent_cov) {
            case 0:
            case 1:
            case 2:
                absentFmt = new DecimalFormat("0");
                break;
            default:
//                absentFmt = new DecimalFormat("0.0");
                absentFmt = new DecimalFormat("0");
            }
            return absentFmt;
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

        private TestItem getTestItem(
                final DB2UDB db2,
                final String year,
                final String semester,
                final String testcd
        ) {
            TestItem testitem = new TestItem();
            try {
                final String sql = "SELECT TESTITEMNAME, SIDOU_INPUT, SIDOU_INPUT_INF "
                                 +   "FROM TESTITEM_MST_COUNTFLG_NEW_SDIV "
                                 +  "WHERE YEAR = '" + year + "' "
                                 +    "AND SEMESTER = '" + semester + "' "
                                 +    "AND TESTKINDCD || TESTITEMCD || SCORE_DIV = '" + testcd + "' ";
                final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql));
                testitem._testitemname = KnjDbUtils.getString(row, "TESTITEMNAME");
                testitem._sidouinput = KnjDbUtils.getString(row, "SIDOU_INPUT");
                testitem._sidouinputinf = KnjDbUtils.getString(row, "SIDOU_INPUT_INF");
            } catch (Exception e) {
                log.error("Exception", e);
            }
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
                return new SubclassMst(null, null, null, null, null, false, false);
            }
            return (SubclassMst) _subclassMst.get(subclasscd);
        }

        private void setSubclassMst(
                final DB2UDB db2
        ) {
            _subclassMst = new HashMap();
            try {
                String sql = "";
                sql += " WITH REPL AS ( ";
                sql += " SELECT DISTINCT '1' AS DIV, COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || COMBINED_SUBCLASSCD AS SUBCLASSCD FROM SUBCLASS_REPLACE_COMBINED_DAT WHERE YEAR = '" + _year + "' ";
                sql += " UNION ";
                sql += " SELECT DISTINCT '2' AS DIV, ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ATTEND_SUBCLASSCD AS SUBCLASSCD FROM SUBCLASS_REPLACE_COMBINED_DAT WHERE YEAR = '" + _year + "' ";
                sql += " ) ";
                sql += " SELECT ";
                sql += " T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ";
                sql += " T1.SUBCLASSCD AS SUBCLASSCD, T2.CLASSABBV, T2.CLASSNAME, T1.SUBCLASSABBV, VALUE(T1.SUBCLASSORDERNAME2, T1.SUBCLASSNAME) AS SUBCLASSNAME, ";
                sql += " CASE WHEN L1.SUBCLASSCD IS NOT NULL THEN 1 END AS IS_SAKI, ";
                sql += " CASE WHEN L2.SUBCLASSCD IS NOT NULL THEN 1 END AS IS_MOTO ";
                sql += " FROM SUBCLASS_MST T1 ";
                sql += " LEFT JOIN REPL L1 ON L1.DIV = '1' AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = L1.SUBCLASSCD ";
                sql += " LEFT JOIN REPL L2 ON L2.DIV = '2' AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = L2.SUBCLASSCD ";
                sql += " LEFT JOIN CLASS_MST T2 ON ";
                if ("1".equals(_useCurriculumcd)) {
                    sql += " T2.CLASSCD = T1.CLASSCD ";
                    sql += " AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
                } else {
                    sql += " T2.CLASSCD = SUBSTR(T1.SUBCLASSCD, 1, 2) ";
                }
                for (final Iterator it = KnjDbUtils.query(db2, sql).iterator(); it.hasNext();) {
                	final Map row = (Map) it.next();
                    final boolean isSaki = "1".equals(KnjDbUtils.getString(row, "IS_SAKI"));
                    final boolean isMoto = "1".equals(KnjDbUtils.getString(row, "IS_MOTO"));
                    final SubclassMst mst = new SubclassMst(KnjDbUtils.getString(row, "SUBCLASSCD"), KnjDbUtils.getString(row, "CLASSABBV"), KnjDbUtils.getString(row, "CLASSNAME"), KnjDbUtils.getString(row, "SUBCLASSABBV"), KnjDbUtils.getString(row, "SUBCLASSNAME"), isSaki, isMoto);
                    _subclassMst.put(KnjDbUtils.getString(row, "SUBCLASSCD"), mst);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            }
        }
    }

}
