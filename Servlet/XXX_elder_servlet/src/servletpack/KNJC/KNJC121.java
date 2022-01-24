// kanji=漢字
/*
 * $Id: 2c880eca62ca83485bfc970eb883822ea79bb3db $
 *
 */
package servletpack.KNJC;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

/*
 *  学校教育システム 賢者 [出欠管理] 出席状況集計票
 */

public class KNJC121 {

    private static final Log log = LogFactory.getLog(KNJC121.class);
    private static final String SPECIAL_ALL = "999";
    private static final String SPECIAL_SUBCLASSCD = "SPECIAL";

    private static final String AMIKAKE_ATTR1 = "Paint=(1,60,1),Bold=1";
    private static final String AMIKAKE_ATTR2 = "Paint=(1,80,1),Bold=1";
    private static final Integer i0 = new Integer(0);
    private static final BigDecimal bd0 = new BigDecimal(0);

    public void svf_out (
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws ServletException {

        final Vrw32alp svf = new Vrw32alp();  // PDF生成クラスのインスタンス生成(ＡＬＰ仕様）

        try {
            response.setContentType("application/pdf");
            svf.VrInit();                             //クラスの初期化
            svf.VrSetSpoolFileStream(response.getOutputStream());         //PDFファイル名の設定
        } catch (Exception ex) {
            log.error("svf instancing exception! ", ex);
        }

        // ＤＢ接続
        DB2UDB db2 = null;
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);    //Databaseクラスを継承したクラス
            db2.open();
        } catch (Exception ex) {
            log.error("db2 instancing exception! ", ex);
            return;
        }

        log.info(" $Revision: 76365 $ ");
        KNJServletUtils.debugParam(request, log);

        boolean hasData = false;
        try {
            Param param = new Param(request, db2);

            // 印刷処理
            hasData = printMain(db2, svf, param);

        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            // 終了処理
            if (!hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note" , "note");
                svf.VrEndPage();
            }
            svf.VrQuit();
            db2.close();
        }
    }

    private static int getMS932ByteLength(final String s) {
        return KNJ_EditEdit.getMS932ByteLength(s);
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

    /**
     *  印刷処理
     */
    private boolean printMain(final DB2UDB db2, final Vrw32alp svf, final Param param) {
        boolean nonedata = false;

        final List studentList = Student.loadStudentList(db2, param);
        // 生徒がいなければ処理をスキップ
        if (studentList.size() == 0) {
            return false;
        }
        setAttendData(db2, param, studentList);

        final int MAX_LINE_PER_PAGE = 25;

        int maxPage = 0;
        for (final Iterator it2 = studentList.iterator(); it2.hasNext();) {
            final Student student = (Student) it2.next();
            maxPage += student.getPage(MAX_LINE_PER_PAGE, param);
        }

        int page = 1;

        final String nendo = KNJ_EditDate.getAutoFormatYear(db2, Integer.parseInt(param._year)) + "年度";
        final String loginDateString = param.getDateString(db2, param._loginDate);
        final String dateFromToString = param.getDateString(db2, param._attendStartDate) + "　から　" + param.getDateString(db2, param._attendEndDate) + "　まで";

        for (final Iterator it2 = studentList.iterator(); it2.hasNext();) {
            final Student student = (Student) it2.next();
            log.debug(student);

            if (!student.hasData()) {
                continue;
            }
            if (param._isOutputDebug) {
                log.info(" student " + student._schregno + " " + student._name);
            }
            nonedata = true;

            final List subclasscdList = new ArrayList(student._subclassAttendanceMap.keySet());
            if (0 != param._attendSubclassSpecialMstSize) {
                subclasscdList.add("SPECIAL");
            }
            final List subclasscdPageList = getPageList(subclasscdList, MAX_LINE_PER_PAGE);
            for (int pi = 0; pi < subclasscdPageList.size(); pi++) {
                final List pageSubclasscdList = (List) subclasscdPageList.get(pi);

                svf.VrSetForm("KNJC121.frm", 1);
                svf.VrsOut("NENDO", nendo);
                svf.VrsOut("DATE", loginDateString);
                svf.VrsOut("PERIOD", dateFromToString);

                // 一覧表枠外の文言
                if ("1".equals(param._knjc121AbsenceHighCheckPattern)) {
                    svf.VrAttribute("NOTE1",  AMIKAKE_ATTR1);
                    svf.VrsOut("NOTE1",  " ");
                    svf.VrsOut("NOTE2",  "：欠課超過、欠席超過");

                    svf.VrAttribute("NOTE5",  AMIKAKE_ATTR2);
                    svf.VrsOut("NOTE5",  " ");
                    svf.VrsOut("NOTE6",  "：欠課注意、欠席注意");

                } else {
                    final String comment = param.TYUI.equals(param._tyuiOrTyouka) ? "注意" : "超過";
                    svf.VrAttribute("NOTE1",  AMIKAKE_ATTR1);
                    svf.VrsOut("NOTE1",  " ");
                    svf.VrsOut("NOTE2",  "：未履修" + comment + ",特活進級" + comment);

                    svf.VrAttribute("NOTE3",  AMIKAKE_ATTR2);
                    svf.VrsOut("NOTE3",  " ");
                    svf.VrsOut("NOTE4",  "：未修得" + comment);

                    if (param.TYUI.equals(param._tyuiOrTyouka)) {
                        svf.VrAttribute("NOTE5",  AMIKAKE_ATTR2);
                    } else {
                        svf.VrAttribute("NOTE5",  AMIKAKE_ATTR1);
                    }
                    svf.VrsOut("NOTE5",  " ");
                    svf.VrsOut("NOTE6", "：欠席" + comment);
                }

                svf.VrsOut("SCHREG_NO", student._schregno);
                svf.VrsOut("ATTEND_NO", student._hrName + Integer.valueOf(student._attendNo).toString() + "番");
                svf.VrsOut("NAME", student._name);
                svf.VrsOut("GRADE", String.valueOf(Integer.valueOf(student._grade)));
                svf.VrsOut("PAGE", String.valueOf(page) + "/" + maxPage);

                printDayAttendance(svf, param, student._daysAttendance);

                final int kekkaScale = ("3".equals(param._knjSchoolMst._absentCov) || "4".equals(param._knjSchoolMst._absentCov)) ? 1 : 0;

                String className = "";

                if (param._printMinyuryoku) {
                    svf.VrsOut("UNINPUT", "未入力数");
                }

                int specialLine = -1;
                for (int subi = 0; subi < pageSubclasscdList.size(); subi++) {
                    final String subclasscd = (String) pageSubclasscdList.get(subi);
                    final int line = subi + 1;
                    if ("SPECIAL".equals(subclasscd)) {
                        specialLine = line;
                        break;
                    }

                    final SubclassAttendance sa = (SubclassAttendance) student._subclassAttendanceMap.get(subclasscd);

                    if (param._isOutputDebug) {
                        log.info(" subclass " + subclasscd + " : " + sa._subclassName);
                    }

                    if (!className.equals(sa._className)) {
                        final String classField = (getMS932ByteLength(sa._className) > 10) ? "CLASS2" : "CLASS";
                        svf.VrsOutn(classField, line, sa._className);
                    }
                    final String subclassField = (getMS932ByteLength(sa._subclassName) > 26) ? "SUBCLASS2" : "SUBCLASS";
                    svf.VrsOutn(subclassField, line, sa._subclassName);
                    if (param._subclsSemsMap.containsKey(sa._subclassCd)) {
                        SubClsDetDat012 ssobj = (SubClsDetDat012)param._subclsSemsMap.get(sa._subclassCd);
                        final String fstidxStr = ssobj.getFirstRemarkFlg();
                        if (param._isOutputDebug) {
                            log.info(" subclass detail 012 = " + ssobj + " / " + fstidxStr);
                        }
                        if (!"".equals(fstidxStr)) {
                            svf.VrsOutn("TERM", line, (String)param._semesterMap.get(fstidxStr));
                        }
                    }
                    svf.VrsOutn("CREDIT", line, sa._credit);

                    svf.VrsOutn("CLASS_COUNT",  line, String.valueOf(sa._lesson));
                    svf.VrsOutn("SUS_MOUR_COUNT",  line, String.valueOf(sa._mourning + sa._suspend + sa._virus + sa._koudome));
                    svf.VrsOutn("MUST_COUNT",  line, String.valueOf(sa._mlesson));
                    svf.VrsOutn("ABSENCE_COUNT",  line, String.valueOf(sa._absent1));
                    svf.VrsOutn("ATTEND_COUNT",  line, String.valueOf(sa._attend));

                    svf.VrsOutn("LATE_COUNT",  line, String.valueOf(sa._late));
                    svf.VrsOutn("EARLY_COUNT",  line, String.valueOf(sa._early));

                    String attrLevel = null;
                    if (sa._absent2.intValue() != 0) {
                        final BigDecimal absHigh1;
                        final BigDecimal absHigh2;
                        if ("1".equals(param._knjc121AbsenceHighCheckPattern)) {
                            absHigh1 = sa._compAbsenceHigh;
                            absHigh2 = sa._compAbsenceHighWarn;
                        } else {
                            absHigh1 = param.TYUI.equals(param._tyuiOrTyouka) ? sa._compAbsenceHighWarn : sa._compAbsenceHigh;
                            absHigh2 = param.TYUI.equals(param._tyuiOrTyouka) ? sa._getAbsenceHighWarn : sa._getAbsenceHigh;
                        }
                        if (0 < absHigh1.intValue() && absHigh1.compareTo(sa._absent2) < 0) {
                            svf.VrAttributen("ABSENT_COUNT",  line, AMIKAKE_ATTR1);
                            attrLevel = "C";
                        } else if (0 < absHigh2.intValue() && absHigh2.compareTo(sa._absent2) < 0) {
                            svf.VrAttributen("ABSENT_COUNT",  line, AMIKAKE_ATTR2);
                            attrLevel = "B";
                        }
                        //log.debug(" " + sa._subclassCd + " : " + sa._absent2 + "(" + absHigh1 + " , " + absHigh2 + ")");
                    }
                    if (null == attrLevel) {
                        attrLevel = "A";
                    }
                    svf.VrsOutn("ABSENT_COUNT",  line, sa._absent2.setScale(kekkaScale, BigDecimal.ROUND_HALF_UP).toString());

                    if (param._printMinyuryoku) {
                        svf.VrsOut("UNINPUT", "未入力数");
                        svf.VrsOutn("UNINPUT_COUNT",  line, String.valueOf(sa._uninput));
                    }
                    if ("1".equals(param._knjc121AbsenceHighCheckPattern) && !"1".equals(param._knjc121AbsenceHighCheckPattern_notPrint)) {
                        svf.VrsOutn("REMARK", line, attrLevel);
                    }

                    className = sa._className == null ? "" : sa._className;
                }

                if (specialLine >= 0) {
                    final SpecialSubclassAttendance ssa = student._specialSubclassAttendance;
                    if (0 != param._attendSubclassSpecialMstSize) {
                        if ("1".equals(param._printAttendSubclassSpecialEachGroup)) {
                            for (final Iterator it = ssa._specialSubclassKekka.keySet().iterator(); it.hasNext();) {

                                final String specialGroupCd = (String) it.next();

                                if (param._isOutputDebug) {
                                    log.info(" specialGroupCd " + specialGroupCd);
                                }

                                final String classNameSp = param.getSpecialGroupName(specialGroupCd);
                                final String classField = (getMS932ByteLength(classNameSp) > 10) ? "CLASS2" : "CLASS";
                                svf.VrsOutn(classField, specialLine, classNameSp);

                                final BigDecimal lesson = SpecialSubclassAttendance.getJisu(ssa._specialSubclassLesson, specialGroupCd, param).setScale(0, BigDecimal.ROUND_HALF_UP);
                                final BigDecimal suspend = SpecialSubclassAttendance.getJisu(ssa._specialSubclassSuspend, specialGroupCd, param).setScale(0, BigDecimal.ROUND_HALF_UP);
                                final BigDecimal mourning = SpecialSubclassAttendance.getJisu(ssa._specialSubclassMourning, specialGroupCd, param).setScale(0, BigDecimal.ROUND_HALF_UP);
                                final BigDecimal mlesson = SpecialSubclassAttendance.getJisu(ssa._specialSubclassMLesson, specialGroupCd, param).setScale(0, BigDecimal.ROUND_HALF_UP);
                                final BigDecimal absent2 = SpecialSubclassAttendance.getJisu(ssa._specialSubclassKekka, specialGroupCd, param).setScale(0, BigDecimal.ROUND_HALF_UP);

                                svf.VrsOutn("CLASS_COUNT",  specialLine, String.valueOf(lesson));
                                svf.VrsOutn("SUS_MOUR_COUNT",  specialLine, String.valueOf(suspend.add(mourning)));
                                svf.VrsOutn("MUST_COUNT",  specialLine, String.valueOf(mlesson));

                                if (absent2.doubleValue() != 0) {
                                    final Map groupAbsenceHigh;
                                    if ("1".equals(param._knjc121AbsenceHighCheckPattern)) {
                                        groupAbsenceHigh = ssa._groupCompAbsenceHigh;
                                    } else {
                                        groupAbsenceHigh = param.TYUI.equals(param._tyuiOrTyouka) ? ssa._groupCompAbsenceHighWarn : ssa._groupCompAbsenceHigh;
                                    }
                                    final BigDecimal compAbsenceHigh = SpecialSubclassAttendance.getAbsenceHigh(specialGroupCd, groupAbsenceHigh);
                                    if (compAbsenceHigh.doubleValue() == 0 || 0 < compAbsenceHigh.doubleValue() && compAbsenceHigh.doubleValue() < absent2.doubleValue()) {
                                        svf.VrAttributen("ABSENT_COUNT",  specialLine, AMIKAKE_ATTR1);
                                    }
                                }
                                svf.VrsOutn("ABSENT_COUNT",  specialLine, String.valueOf(absent2));
                                specialLine += 1;
                            }
                        } else {
                            final String specialGroupCd = SPECIAL_ALL;

                            final String classNameSp = param.getSpecialGroupName(specialGroupCd);
                            final String classField = (getMS932ByteLength(classNameSp) > 10) ? "CLASS2" : "CLASS";
                            svf.VrsOutn(classField, specialLine, classNameSp);
                            final BigDecimal lesson = ssa.getTotalJisu(ssa._specialSubclassLesson, param, false).setScale(0, BigDecimal.ROUND_HALF_UP);
                            final BigDecimal suspend = ssa.getTotalJisu(ssa._specialSubclassSuspend, param, false).setScale(0, BigDecimal.ROUND_HALF_UP);
                            final BigDecimal mourning = ssa.getTotalJisu(ssa._specialSubclassMourning, param, false).setScale(0, BigDecimal.ROUND_HALF_UP);
                            final BigDecimal mlesson = ssa.getTotalJisu(ssa._specialSubclassMLesson, param, false).setScale(0, BigDecimal.ROUND_HALF_UP);
                            final BigDecimal absent2 = ssa.getTotalJisu(ssa._specialSubclassKekka, param, false).setScale(0, BigDecimal.ROUND_HALF_UP);

                            svf.VrsOutn("CLASS_COUNT",  specialLine, lesson.toString());
                            svf.VrsOutn("SUS_MOUR_COUNT",  specialLine, String.valueOf(suspend.add(mourning)));
                            svf.VrsOutn("MUST_COUNT",  specialLine, mlesson.toString());

                            if (absent2.intValue() != 0) {
                                final Map groupAbsenceHigh;
                                if ("1".equals(param._knjc121AbsenceHighCheckPattern)) {
                                    groupAbsenceHigh = ssa._groupCompAbsenceHigh;
                                } else {
                                    groupAbsenceHigh = param.TYUI.equals(param._tyuiOrTyouka) ? ssa._groupCompAbsenceHighWarn : ssa._groupCompAbsenceHigh;
                                }
                                final BigDecimal compAbsenceHigh = SpecialSubclassAttendance.getAbsenceHigh(specialGroupCd, groupAbsenceHigh);
                                if (compAbsenceHigh.intValue() == 0 || 0 < compAbsenceHigh.intValue() && compAbsenceHigh.compareTo(absent2) < 0) {
                                    svf.VrAttributen("ABSENT_COUNT",  specialLine, AMIKAKE_ATTR1);
                                }
                            }
                            svf.VrsOutn("ABSENT_COUNT",  specialLine, String.valueOf(absent2));
                            specialLine += 1;
                        }
                    }
                }

                svf.VrEndPage();
                page += 1;
            }
        }
        return nonedata;
    }

    private void printDayAttendance(final Vrw32alp svf, final Param param, final DayAttendance da) {
        svf.VrsOut("CLASS_DAYS",  String.valueOf(da._lessonDays));
        svf.VrsOut("SUS_MOUR_DAYS",  String.valueOf(da._mourningDays + da._suspendDays + da._virusDays + da._koudomeDays));
        svf.VrsOut("ABROAD",  String.valueOf(da._abroadDays));
        svf.VrsOut("MUST_DAYS",  String.valueOf(da._mlessonDays));

        String bunsi = null;
        String bunbo = null;
        String attr = null;
        if ("1".equals(param._knjc121AbsenceHighCheckPattern)) {
            bunsi = param._knjSchoolMst._kessekiOutBunsi;
            bunbo = param._knjSchoolMst._kessekiOutBunbo;
            attr = AMIKAKE_ATTR1;
            if (isDayAttendanceAbsenceHighOver(da, bunsi, bunbo)) {
                svf.VrAttribute("ABSENCE_DAYS", attr);
            } else {
                bunsi = param._knjSchoolMst._kessekiWarnBunsi;
                bunbo = param._knjSchoolMst._kessekiWarnBunbo;
                attr = AMIKAKE_ATTR2;
                if (isDayAttendanceAbsenceHighOver(da, bunsi, bunbo)) {
                    svf.VrAttribute("ABSENCE_DAYS", attr);
                }
            }
        } else {
            if (param.TYOUKA.equals(param._tyuiOrTyouka)) {
                bunsi = param._knjSchoolMst._kessekiOutBunsi;
                bunbo = param._knjSchoolMst._kessekiOutBunbo;
                attr = AMIKAKE_ATTR1;
            } else if (param.TYUI.equals(param._tyuiOrTyouka)) {
                bunsi = param._knjSchoolMst._kessekiWarnBunsi;
                bunbo = param._knjSchoolMst._kessekiWarnBunbo;
                attr = AMIKAKE_ATTR2;
            }
            if (isDayAttendanceAbsenceHighOver(da, bunsi, bunbo)) {
                svf.VrAttribute("ABSENCE_DAYS", attr);
            }
        }
        svf.VrsOut("ABSENCE_DAYS",  String.valueOf(da._sickDays));
        svf.VrsOut("ATTEND_DAYS",  String.valueOf(da._attendDays));
        svf.VrsOut("LATE_DAYS",  String.valueOf(da._lateDays));
        svf.VrsOut("EARLY_DAYS",  String.valueOf(da._earlyDays));
    }

    private boolean isDayAttendanceAbsenceHighOver(final DayAttendance da, final String bunsi, final String bunbo) {
        if (null == bunsi || null == bunbo) {
            return false;
        }
        final BigDecimal sick = new BigDecimal(da._sickDays);
        final BigDecimal absenceHigh = new BigDecimal(da._mlessonDays).multiply(new BigDecimal(bunsi)).divide(new BigDecimal(bunbo), 0, BigDecimal.ROUND_CEILING);
        return sick.compareTo(absenceHigh) > 0;
    }

    private static List getMappedList(final Map m, final Object key) {
        if (null == m.get(key)) {
            m.put(key, new ArrayList());
        }
        return (List) m.get(key);
    }

    /**
     * 生徒と1日出欠、科目別出欠のデータを取得する。
     * @param db2
     * @return 生徒データのリスト
     */
    private void setAttendData(final DB2UDB db2, final Param param, final List studentList) {
        PreparedStatement ps = null;
        String sql = null;
        try {
            final Map studentMap = new HashMap();
            for (final Iterator it = studentList.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                studentMap.put(student._schregno, student);
            }

            boolean nonedata = false;

            // 出欠の情報
            final String sdate = param.getSDate(db2, param._year, param._attendStartDate);

            // 1日単位
            param._attendParamMap.put("schregno", "?");
            sql = AttendAccumulate.getAttendSemesSql(
                    param._year,
                    "9",
                    sdate,
                    param._attendEndDate,
                    param._attendParamMap
            );
            log.debug("get AttendSemes sql = " + sql);

            ps = db2.prepareStatement(sql);

            // 1日単位
            for (final Iterator it = studentList.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();

                for (final Iterator rit = KnjDbUtils.query(db2, ps, new Object[] {student._schregno}).iterator(); rit.hasNext();) {
                    final Map row = (Map) rit.next();
                    if (!"9".equals(KnjDbUtils.getString(row, "SEMESTER"))) {
                        continue;
                    }
                    student._daysAttendance.add(row);
                    nonedata = true;
                }
            }
            DbUtils.closeQuietly(ps);

            if (!nonedata) {
                return;
            }

            // 時数単位
            param._attendParamMap.put("schregno", "?");
            sql = AttendAccumulate.getAttendSubclassSql(
                    param._year,
                    "9",
                    sdate,
                    param._attendEndDate,
                    param._attendParamMap
                    );

            log.debug("get AttendSubclass sql = " + sql);
            ps = db2.prepareStatement(sql);

            // 時数単位
            for (final Iterator it = studentList.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();

                ps.setString(1, student._schregno);
                for (final Iterator rit = KnjDbUtils.query(db2, ps, new Object[] {student._schregno}).iterator(); rit.hasNext();) {
                    final Map row = (Map) rit.next();
                    if (!"9".equals(KnjDbUtils.getString(row, "SEMESTER"))) {
                        continue;
                    }
                    if (getMappedList(param._courseNotPrintSubclasscdListMap, student._grade + student._course).contains(KnjDbUtils.getString(row, "SUBCLASSCD"))) {
                        continue;
                    }

                    if (KnjDbUtils.getString(row, "SPECIAL_GROUP_CD") != null) {

                        final String specialGroupCd = KnjDbUtils.getString(row, "SPECIAL_GROUP_CD");
                        int lessonMinutes = KnjDbUtils.getInt(row, "SPECIAL_LESSON_MINUTES0", i0).intValue();
                        int suspendMinutes = KnjDbUtils.getInt(row, "SPECIAL_SUSPEND_MINUTES", i0).intValue();
                        int mourningMinutes = KnjDbUtils.getInt(row, "SPECIAL_MOURNING_MINUTES", i0).intValue();
                        int mlessonMinutes = KnjDbUtils.getInt(row, "SPECIAL_LESSON_MINUTES", i0).intValue();
                        int kekkaMinutes = 0;
                        final String subclassCd = KnjDbUtils.getString(row, "SUBCLASSCD");
//                        if ("1".equals(param._useCurriculumcd)) {
//                            subclassCd = StringUtils.split(KnjDbUtils.getString(row, "SUBCLASSCD"), "-")[3];
//                        } else {
//                            subclassCd = KnjDbUtils.getString(row, "SUBCLASSCD");
//                        }
                        if (param._subClassC005.containsKey(subclassCd)) { // TODOO ??
                            String is = (String) param._subClassC005.get(subclassCd);
                            if ("1".equals(is)) {
                                kekkaMinutes = KnjDbUtils.getInt(row, "SPECIAL_SICK_MINUTES3", i0).intValue();
                            } else if ("2".equals(is)) {
                                kekkaMinutes = KnjDbUtils.getInt(row, "SPECIAL_SICK_MINUTES2", i0).intValue();
                            }
                        } else {
                            kekkaMinutes = KnjDbUtils.getInt(row, "SPECIAL_SICK_MINUTES1", i0).intValue();
                        }
                        // log.fatal(" subclassCd = " + subclassCd + ", C005 = " + param._subClassC005 + ", contains = " + param._subClassC005.containsKey(subclassCd));
                        // log.fatal(" specialSubclassCd = " + specialGroupCd + ", minutes += " + kekkaMinutes + "(1,2,3) = (" + KnjDbUtils.getInt(row, "SPECIAL_SICK_MINUTES1") + ", " + KnjDbUtils.getInt(row, "SPECIAL_SICK_MINUTES2") + ", " + KnjDbUtils.getInt(row, "SPECIAL_SICK_MINUTES3") + ")");
                        student._specialSubclassAttendance.add(specialGroupCd, lessonMinutes, suspendMinutes, mourningMinutes, mlessonMinutes, kekkaMinutes);

                    } else {

                        final String subclasscd = KnjDbUtils.getString(row, "SUBCLASSCD");
                        if (!"1".equals(param._semester) && param._zenkiKamokuSubclasscdList.contains(subclasscd)) {
                            if (param._isOutputDebug) {
                                log.info(student._schregno + " skip zenki kamoku " + subclasscd);
                            }
                            continue;
                        }

                        if (student._subclassAttendanceMap.get(subclasscd)== null) {
                            final SubclassAttendance sa = new SubclassAttendance(subclasscd,
                                    param.getClassName(subclasscd),
                                    param.getSubclassName(subclasscd),
                                    param.getCredit(KnjDbUtils.getString(row, "SCHREGNO"), subclasscd)
                                    );
                            student._subclassAttendanceMap.put(subclasscd, sa);
                        }
                        SubclassAttendance sa = (SubclassAttendance) student._subclassAttendanceMap.get(subclasscd);
                        sa.add(row, param);
                    }
                }
            }
            DbUtils.closeQuietly(ps);

            final boolean isHoutei = param.isHoutei();
            // 欠課数上限値科目
            if (isHoutei) {
                sql = getHouteiJisuSql(null, param, false, null);
            } else {
                sql = getJituJisuSql(null, param, false, null);
            }
            log.debug("get AbsenceHighSubclass sql = " + sql);
            ps = db2.prepareStatement(sql);

            for (final Iterator it = studentList.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();

                for (final Iterator rit = KnjDbUtils.query(db2, ps, new Object[] {student._schregno}).iterator(); rit.hasNext();) {
                    final Map row = (Map) rit.next();
                    final String subclassCd = KnjDbUtils.getString(row, "SUBCLASSCD");
                    if (subclassCd == null || student._subclassAttendanceMap.get(subclassCd) == null) {
                        continue;
                    }

                    final SubclassAttendance sa = (SubclassAttendance) student._subclassAttendanceMap.get(subclassCd);

                    if (NumberUtils.isNumber(KnjDbUtils.getString(row, "ABSENCE_HIGH"))) {
                        sa._compAbsenceHigh = KnjDbUtils.getBigDecimal(row, "ABSENCE_HIGH", bd0);
                    }
                    if (NumberUtils.isNumber(KnjDbUtils.getString(row, "ABSENCE_HIGH_WARN"))) {
                        sa._compAbsenceHighWarn = KnjDbUtils.getBigDecimal(row, "ABSENCE_HIGH_WARN", bd0);
                    }
                    if (NumberUtils.isNumber(KnjDbUtils.getString(row, "GET_ABSENCE_HIGH"))) {
                        sa._getAbsenceHigh = KnjDbUtils.getBigDecimal(row, "GET_ABSENCE_HIGH", bd0);
                    }
                    if (NumberUtils.isNumber(KnjDbUtils.getString(row, "GET_ABSENCE_HIGH_WARN"))) {
                        sa._getAbsenceHighWarn = KnjDbUtils.getBigDecimal(row, "GET_ABSENCE_HIGH_WARN", bd0);
                    }
                }
            }
            DbUtils.closeQuietly(ps);

            // 欠課数上限値
            if (isHoutei) {
                sql = getHouteiJisuSql(null, param, true, null);
            } else {
                sql = getJituJisuSql(null, param, true, null);
            }
            log.debug("get AbsenceHigh sql = " + sql);
            ps = db2.prepareStatement(sql);

            for (final Iterator it = studentList.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();

                for (final Iterator rit = KnjDbUtils.query(db2, ps, new Object[] {student._schregno}).iterator(); rit.hasNext();) {
                    final Map row = (Map) rit.next();
                    final String groupcd = KnjDbUtils.getString(row, "SPECIAL_GROUP_CD");
                    student._specialSubclassAttendance._groupCompAbsenceHigh.put(groupcd, KnjDbUtils.getBigDecimal(row, "ABSENCE_HIGH", bd0));
                    student._specialSubclassAttendance._groupCompAbsenceHighWarn.put(groupcd, KnjDbUtils.getBigDecimal(row, "ABSENCE_HIGH_WARN", bd0));
                    //log.debug(" student = " + student._schregno + ":" + student._name + " " + rs.getDouble("ABSENCE_HIGH"));
                }
            }
            DbUtils.closeQuietly(ps);

            if (param._printMinyuryoku) {
                for (final Iterator rit = KnjDbUtils.query(db2, sqlUninput(param)).iterator(); rit.hasNext();) {
                    final Map row = (Map) rit.next();
                    final Student student = (Student) studentMap.get(KnjDbUtils.getString(row, "SCHREGNO"));
                    if (student == null) {
                        continue;
                    }
                    final SubclassAttendance sa = (SubclassAttendance) student._subclassAttendanceMap.get(KnjDbUtils.getString(row, "SUBCLASSCD"));
                    if (sa == null) {
                        continue;
                    }
                    sa._uninput = KnjDbUtils.getInt(row, "UNINPUT_COUNT", i0).intValue();
                }
            }
        } catch (Exception ex) {
            log.error("exception!", ex);
        } finally {
            DbUtils.closeQuietly(ps);
            db2.commit();
        }
    }

    private String sqlUninput(final Param param) {
        final StringBuffer stb = new StringBuffer();
        stb.append("SELECT ");
        stb.append("    T5.SCHREGNO, ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append("    T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || ");
        }
        stb.append("    T3.SUBCLASSCD AS SUBCLASSCD, ");
        stb.append("    COUNT(*) AS UNINPUT_COUNT ");
        stb.append("FROM ");
        stb.append("    SCH_CHR_DAT T1 ");
        stb.append("    INNER JOIN V_SEMESTER_GRADE_MST T2 ON T2.YEAR = T1.YEAR ");
        stb.append("        AND T1.EXECUTEDATE BETWEEN T2.SDATE AND T2.EDATE ");
        stb.append("        AND T2.SEMESTER <> '9' ");
        stb.append("        AND T2.GRADE = '" + param._gradeHrclass.substring(0, 2) + "' ");
        stb.append("    INNER JOIN CHAIR_DAT T3 ON T3.YEAR = T2.YEAR ");
        stb.append("        AND T3.SEMESTER = T2.SEMESTER ");
        stb.append("        AND T3.CHAIRCD = T1.CHAIRCD ");
        stb.append("    INNER JOIN CHAIR_STD_DAT T4 ON T4.YEAR = T2.YEAR ");
        stb.append("        AND T4.SEMESTER = T2.SEMESTER ");
        stb.append("        AND T4.CHAIRCD = T1.CHAIRCD ");
        stb.append("        AND T1.EXECUTEDATE BETWEEN T4.APPDATE AND T4.APPENDDATE ");
        stb.append("    INNER JOIN SCHREG_REGD_DAT T5 ON T5.YEAR = T2.YEAR ");
        stb.append("        AND T5.SEMESTER = T2.SEMESTER ");
        stb.append("        AND T5.SCHREGNO = T4.SCHREGNO ");
        stb.append("        AND T5.SCHREGNO in " + SQLUtils.whereIn(true, param._schregno));
        stb.append("    LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T5.YEAR ");
        stb.append("        AND GDAT.GRADE = T5.GRADE ");
        stb.append("    LEFT JOIN SCHREG_ENT_GRD_HIST_DAT EGHIST ON EGHIST.SCHREGNO = T5.SCHREGNO ");
        stb.append("        AND EGHIST.SCHOOL_KIND = GDAT.SCHOOL_KIND ");
        if (!"1".equals(param._knjc121NotCheckSchChrHrateDatExecuted)) {
            stb.append("    LEFT JOIN SCH_CHR_HRATE_DAT T6 ON T6.EXECUTEDATE = T1.EXECUTEDATE ");
            stb.append("        AND T6.PERIODCD = T1.PERIODCD ");
            stb.append("        AND T6.CHAIRCD = T1.CHAIRCD ");
            stb.append("        AND T6.GRADE = T5.GRADE ");
            stb.append("        AND T6.HR_CLASS = T5.HR_CLASS ");
        }
        stb.append("WHERE ");
        stb.append("    T1.YEAR = '" + param._year + "' ");
        if ("1".equals(param._knjc121NotCheckSchChrHrateDatExecuted)) {
            stb.append("    AND (T1.EXECUTED = '0') ");
        } else {
            stb.append("    AND (T1.EXECUTED = '0' OR T1.EXECUTED = '1' AND VALUE(T6.EXECUTED,'0') = '0') ");
        }
        stb.append("    AND T1.EXECUTEDATE BETWEEN '" + param._attendStartDate + "' AND '" + param._attendEndDate + "' ");
        stb.append("    AND (EGHIST.ENT_DATE IS NULL OR EGHIST.ENT_DATE <= T1.EXECUTEDATE)");
        stb.append("    AND (EGHIST.GRD_DATE IS NULL OR EGHIST.GRD_DIV <> '4' AND T1.EXECUTEDATE <= EGHIST.GRD_DATE) ");
        stb.append("GROUP BY ");
        stb.append("    T5.SCHREGNO, ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append("    T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || ");
        }
        stb.append("    T3.SUBCLASSCD ");
        log.debug(" uninput sql = " + stb.toString());
        return stb.toString();
    }

    private static class Param {
        final String TYUI = "1";
        final String TYOUKA = "2";

        final String _year;
        final String _semester;
        final String _attendStartDate;
        final String _attendEndDate;
        final String _loginDate;
        final String _gradeHrclass;
        final String[] _schregno;
        final boolean _printMinyuryoku; // 出欠未入力を出力する
        final boolean _printSakikamoku; // 合併先科目を出力する
        final String _tyuiOrTyouka; // true:注意 or false:超過
        /** 欠課換算前の遅刻・早退を表示する */
        final String _chikokuHyoujiFlg;

        private Map _classNameMap;
        private Map _subclassNameMap;
        private List _zenkiKamokuSubclasscdList;
        private Map _schregCreditMap;
        private KNJSchoolMst _knjSchoolMst;
        /** C005：欠課換算法修正 */
        private Map _subClassC005 = new HashMap();
        /** 特別活動グループ名称 */
        private Map _specialGroupNames = new HashMap();
        final boolean _isSeireki;

        /** 注意週数学期 */
        private String _warnSemester;
        /** 単位マスタの警告数は単位が回数か */
        private boolean _absenceWarnIsUnitCount;
        /** 特別活動は各グループごとに表示する */
        final String _printAttendSubclassSpecialEachGroup;
        /** 教育課程コードを使用するか */
        final String _useCurriculumcd;

        /** 学期名称 **/
        private final Map _semesterMap;
        /** 科目別対象学期情報 **/
        private final Map _subclsSemsMap;

        /**
         * 値が'1'の場合、
         * ・指示画面で欠課数上限値注意・超過を選択しない（ラジオボタンを表示しない）
         * ・履修上限値の注意・超過で網掛けをする。備考に、超過は「C」、注意は「B」、それ以外は「A」を表示する
         * ・一覧表枠外の網掛けの説明文言を表示しない
         * */
        final String _knjc121AbsenceHighCheckPattern;

        /**
         * 値が'1'の場合、
         * ・備考に、A, B, Cを表示しない。
         * */
        final String _knjc121AbsenceHighCheckPattern_notPrint;

        /**
         * 値が'1'の場合、
         * ・出欠未入力の判定にSCH_CHR_HRATE_DATを含めない
         */
        final String _knjc121NotCheckSchChrHrateDatExecuted;

        private int _attendSubclassSpecialMstSize;
        private Map _courseNotPrintSubclasscdListMap;

        final Map _attendParamMap;
        boolean _isOutputDebug;

        Param(final HttpServletRequest request, final DB2UDB db2) {
            _year = request.getParameter("CTRL_YEAR");
            _semester  = request.getParameter("CTRL_SEMESTER");
            _attendStartDate = request.getParameter("SDATE").replace('/', '-');
            _attendEndDate = request.getParameter("EDATE").replace('/', '-');
            _loginDate = request.getParameter("CTRL_DATE").replace('/', '-');

            _gradeHrclass = request.getParameter("GRADE_HRCLASS");
            _schregno = request.getParameterValues("CATEGORY_SELECTED");
            _printMinyuryoku = "on".equals(request.getParameter("PRINT_MINYURYOKU"));
            _printSakikamoku = !"1".equals(request.getParameter("USE_PRINT_SAKIKAMOKU")) || "1".equals(request.getParameter("USE_PRINT_SAKIKAMOKU")) && "on".equals(request.getParameter("PRINT_SAKIKAMOKU"));
            _tyuiOrTyouka = request.getParameter("TYUI_TYOUKA");
            _chikokuHyoujiFlg = request.getParameter("chikokuHyoujiFlg");
            _printAttendSubclassSpecialEachGroup = request.getParameter("printAttendSubclassSpecialEachGroup");
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _knjc121AbsenceHighCheckPattern = request.getParameter("knjc121AbsenceHighCheckPattern");
            _knjc121AbsenceHighCheckPattern_notPrint = request.getParameter("knjc121AbsenceHighCheckPattern_notPrint");
            _knjc121NotCheckSchChrHrateDatExecuted = request.getParameter("knjc121NotCheckSchChrHrateDatExecuted");
            _isSeireki = KNJ_EditDate.isSeireki(db2);

            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
            _attendParamMap.put("grade", _gradeHrclass.substring(0, 2));
            _attendParamMap.put("hrClass", _gradeHrclass.substring(2));
            _isOutputDebug = "1".equals(getDbPrginfoProperties(db2, "outputDebug"));

            _semesterMap = setSemesterName(db2);
            _subclsSemsMap = setSubclsSemesMap(db2);

            try {
                setSubclassNameMap(db2);
                setClassNameMap(db2);
                setZenkiKamokuList(db2);
                setSchregSubclassCreditMap(db2);
                loadNameMstC005(db2);
                loadNameMstC042(db2);
                setWarnSemester(db2);
                setSpecialGroupName(db2);
                setAttendSubclassSpecialMstSize(db2);
                setSubclassReplaceCombinedDatSakiKamoku(db2);

                final Map smParamMap = new HashMap();
                if (KnjDbUtils.setTableColumnCheck(db2, "SCHOOL_MST", "SCHOOL_KIND")) {
                    final String schoolKind = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND GRADE = '" + _gradeHrclass.substring(0, 2) + "' "));
                    smParamMap.put("SCHOOL_KIND", schoolKind);
                }
                _knjSchoolMst = new KNJSchoolMst(db2, _year, smParamMap);
                log.info(" KNJSchoolMst.lesson_flg = " + _knjSchoolMst._jugyouJisuFlg);
            } catch (Exception ex) {
                log.error("Param load exception!", ex);
            } finally {
                db2.commit();
            }
        }

        private static String getDbPrginfoProperties(final DB2UDB db2, final String propName) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJC121' AND NAME = '" + propName + "' "));
        }

        public boolean isHoutei() {
            if ("3".equals(_knjSchoolMst._jugyouJisuFlg)) {
                return "9".equals(_semester) || null != _knjSchoolMst._semesterDiv && _knjSchoolMst._semesterDiv.equals(_semester);
            }
            return !"2".equals(_knjSchoolMst._jugyouJisuFlg);
        }

        public String getClassName(final String subclassCd) {
            if ("1".equals(_useCurriculumcd)) {
                final String[] cds = StringUtils.split(subclassCd, "-");
                return (String) _classNameMap.get(cds[0] + "-" + cds[1]);
            } else {
                return (String) _classNameMap.get(subclassCd.substring(0, 2));
            }
        }

        private String getSubclassName(final String subclassCd) {
            return (String) _subclassNameMap.get(subclassCd);
        }

        private void setWarnSemester(final DB2UDB db2) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT T1.YEAR, T1.SEMESTER, T1.SDATE, T1.EDATE, T2.SEMESTER AS NEXT_SEMESTER, T2.SDATE AS NEXT_SDATE ");
            stb.append(" FROM V_SEMESTER_GRADE_MST T1 ");
            stb.append(" LEFT JOIN V_SEMESTER_GRADE_MST T2 ON T2.YEAR = T1.YEAR ");
            stb.append("     AND INT(T2.SEMESTER) = INT(T1.SEMESTER) + 1 ");
            stb.append("     AND T2.GRADE = T1.GRADE ");
            stb.append(" WHERE T1.YEAR = '" + _year + "' ");
            stb.append("     AND T1.SEMESTER <> '9' ");
            stb.append("     AND T1.GRADE = '" + _gradeHrclass.substring(0, 2) + "' ");
            stb.append("     AND (('" + _attendEndDate + "' BETWEEN T1.SDATE AND T1.EDATE) ");
            stb.append("          OR (T1.EDATE < '" + _attendEndDate + "' AND '" + _attendEndDate + "' < VALUE(T2.SDATE, '9999-12-30'))) ");
            stb.append(" ORDER BY T1.SEMESTER ");

            for (final Iterator rit = KnjDbUtils.query(db2, stb.toString()).iterator(); rit.hasNext();) {
                final Map row = (Map) rit.next();
                _warnSemester = KnjDbUtils.getString(row, "SEMESTER");
            }
        }

        private void setAttendSubclassSpecialMstSize(final DB2UDB db2) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT COUNT(*) AS COUNT ");
            stb.append(" FROM ATTEND_SUBCLASS_SPECIAL_MST T1 ");
            stb.append(" WHERE T1.SPECIAL_GROUP_CD <> '999' ");
            _attendSubclassSpecialMstSize = 0;
            for (final Iterator rit = KnjDbUtils.query(db2, stb.toString()).iterator(); rit.hasNext();) {
                final Map row = (Map) rit.next();
                _attendSubclassSpecialMstSize = KnjDbUtils.getInt(row, "COUNT", i0).intValue();
            }
        }

        private void setSubclassReplaceCombinedDatSakiKamoku(final DB2UDB db2) {
            _courseNotPrintSubclasscdListMap = new HashMap();
            if (!_printSakikamoku) {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT DISTINCT ");
                if ("1".equals(_useCurriculumcd)) {
                    stb.append(" T1.COMBINED_CLASSCD || '-' || T1.COMBINED_SCHOOL_KIND || '-' || T1.COMBINED_CURRICULUM_CD || '-' || ");
                }
                stb.append(" T1.COMBINED_SUBCLASSCD AS COMBINED_SUBCLASSCD, ");
                stb.append(" T2.GRADE, T2.COURSECD || T2.MAJORCD || T2.COURSECODE AS COURSE ");
                stb.append(" FROM SUBCLASS_REPLACE_COMBINED_DAT T1 ");
                stb.append(" INNER JOIN CREDIT_MST T2 ON T2.YEAR = T1.YEAR ");
                if ("1".equals(_useCurriculumcd)) {
                    stb.append("     AND T2.CLASSCD = T1.ATTEND_CLASSCD ");
                    stb.append("     AND T2.SCHOOL_KIND = T1.ATTEND_SCHOOL_KIND ");
                    stb.append("     AND T2.CURRICULUM_CD = T1.ATTEND_CURRICULUM_CD ");
                }
                stb.append("     AND T2.SUBCLASSCD = T1.ATTEND_SUBCLASSCD ");
                stb.append(" WHERE T1.YEAR = '" + _year + "' ");

                for (final Iterator rit = KnjDbUtils.query(db2, stb.toString()).iterator(); rit.hasNext();) {
                    final Map row = (Map) rit.next();
                    getMappedList(_courseNotPrintSubclasscdListMap, KnjDbUtils.getString(row, "GRADE") + KnjDbUtils.getString(row, "COURSE")).add(KnjDbUtils.getString(row, "COMBINED_SUBCLASSCD"));
                }
            }
            log.info(" not print subclasscd list = " + _courseNotPrintSubclasscdListMap);
        }

        /**
         * 欠課換算法修正
         * @param db2
         * @throws SQLException
         */
        private void loadNameMstC005(final DB2UDB db2) {
            final String sql = "SELECT NAME1 AS SUBCLASSCD, NAMESPARE1 FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'C005'";
            for (final Iterator rit = KnjDbUtils.query(db2, sql).iterator(); rit.hasNext();) {
                final Map row = (Map) rit.next();
                final String subclassCd = KnjDbUtils.getString(row, "SUBCLASSCD");
                final String is = KnjDbUtils.getString(row, "NAMESPARE1");
                log.debug("(名称マスタ C005):科目コード=" + subclassCd);
                _subClassC005.put(subclassCd, is);
            }
        }

        /**
         * 単位マスタの警告数は単位が回数か
         * @param db2
         * @throws SQLException
         */
        private void loadNameMstC042(final DB2UDB db2) {
            final String sql = "SELECT NAMESPARE1 FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'C042' AND NAMECD2 = '01' ";
            for (final Iterator rit = KnjDbUtils.query(db2, sql).iterator(); rit.hasNext();) {
                final Map row = (Map) rit.next();
                _absenceWarnIsUnitCount = "1".equals(KnjDbUtils.getString(row, "NAMESPARE1"));
                log.debug("(名称マスタ C042):科目コード=" + _absenceWarnIsUnitCount);
            }
        }

        private String getSDate(final DB2UDB db2, final String year, final String defaultSdate) {
            String sdate = defaultSdate;
            String sql = "SELECT SDATE FROM V_SEMESTER_GRADE_MST WHERE SEMESTER = '1' AND YEAR = '" + year + "' AND GRADE = '" + _gradeHrclass.substring(0, 2) + "' ";
            for (final Iterator rit = KnjDbUtils.query(db2, sql).iterator(); rit.hasNext();) {
                final Map row = (Map) rit.next();
                sdate = KnjDbUtils.getString(row, "SDATE");
            }

            if (sdate.compareTo(defaultSdate) <= 0) {
                sdate = defaultSdate;
            }

            return sdate;
        }

        private String getStaffNameString(final String staffName1, final String staffName2, final String staffName3) {
            String[] staffNames = {staffName1, staffName2, staffName3};
            StringBuffer stb = new StringBuffer();
            String comma = "";
            for (int i = 0; i < staffNames.length; i++) {
                if (staffNames[i] != null) {
                    stb.append(comma + staffNames[i]);
                    comma = "、";
                }
            }
            return stb.toString();
        }

        private String getDateString(final DB2UDB db2, final String date) {
            String nen = "";
            if (null != date) {
                if (_isSeireki) {
                    nen = date.substring(0, 4) + "年";
                } else {
                    final String[] gengoArr = KNJ_EditDate.tate_format4(db2, date);
                    if (null != gengoArr) {
                        nen = gengoArr[0] + gengoArr[1] + "年";
                    }
                }
            }

            final int month = Integer.parseInt(date.substring(5, 7));
            final int day = Integer.parseInt(date.substring(8, 10));

            final String monthStr = ((month < 10) ? " " : "")  + String.valueOf(month);
            final String dayStr = ((day < 10) ? " " : "")  + String.valueOf(day);

            return  nen + String.valueOf(monthStr) + "月" + String.valueOf(dayStr) + "日";
        }

        private void setSubclassNameMap(final DB2UDB db2) {
            _subclassNameMap = new TreeMap();

            final StringBuffer stb = new StringBuffer();
            stb.append("SELECT ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append("   CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || ");
            }
            stb.append("   SUBCLASSCD AS SUBCLASSCD, SUBCLASSNAME ");
            stb.append("FROM ");
            stb.append("   SUBCLASS_MST ");

            for (final Iterator rit = KnjDbUtils.query(db2, stb.toString()).iterator(); rit.hasNext();) {
                final Map row = (Map) rit.next();
                _subclassNameMap.put(KnjDbUtils.getString(row, "SUBCLASSCD"), KnjDbUtils.getString(row, "SUBCLASSNAME"));
            }
        }

        private void setSchregSubclassCreditMap(final DB2UDB db2) {
            _schregCreditMap = new TreeMap();

            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT T1.SCHREGNO, ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append("   T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || ");
            }
            stb.append("        T2.SUBCLASSCD AS SUBCLASSCD, T2.CREDITS ");
            stb.append(" FROM SCHREG_REGD_DAT T1");
            stb.append("     LEFT JOIN CREDIT_MST T2 ON");
            stb.append("         T1.YEAR = T2.YEAR");
            stb.append("         AND T1.COURSECD = T2.COURSECD");
            stb.append("         AND T1.MAJORCD = T2.MAJORCD");
            stb.append("         AND T1.COURSECODE = T2.COURSECODE");
            stb.append("         AND T1.GRADE = T2.GRADE");
            stb.append(" WHERE");
            stb.append("     T1.GRADE || T1.HR_CLASS = '" + _gradeHrclass + "'");
            stb.append("     AND T1.YEAR = '" + _year + "'");
            stb.append("     AND T1.SEMESTER = '" + _semester + "'");

            for (final Iterator rit = KnjDbUtils.query(db2, stb.toString()).iterator(); rit.hasNext();) {
                final Map row = (Map) rit.next();
                if (KnjDbUtils.getString(row, "SCHREGNO") == null || KnjDbUtils.getString(row, "SUBCLASSCD") == null) {
                    log.error(" schregno = " + KnjDbUtils.getString(row, "SCHREGNO") + " ,  subclasscd = " + KnjDbUtils.getString(row, "SUBCLASSCD"));
                    continue;
                }
                setSchregSubClassCredit(KnjDbUtils.getString(row, "SCHREGNO"), KnjDbUtils.getString(row, "SUBCLASSCD"), KnjDbUtils.getString(row, "CREDITS"));
            }
        }

        private void setSchregSubClassCredit(final String schregno, final String subclassCd, final String credit) {
            if (_schregCreditMap.get(schregno) == null) {
                _schregCreditMap.put(schregno, new TreeMap());
            }
            Map subclassCreditMap = (Map) _schregCreditMap.get(schregno);
            subclassCreditMap.put(subclassCd, credit);
        }

        private String getCredit(final String schregno, final String subclassCd) {
            if (subclassCd == null || _schregCreditMap.get(schregno) == null) {
                return null;
            }
            Map subclassCreditMap = (Map) _schregCreditMap.get(schregno);
            if (subclassCreditMap.get(subclassCd) == null) {
                return null;
            }
            return (String) subclassCreditMap.get(subclassCd);
        }


        private void setClassNameMap(final DB2UDB db2) {
            _classNameMap = new TreeMap();

            final StringBuffer stb = new StringBuffer();
            stb.append("SELECT ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append("   T1.CLASSCD || '-' || T1.SCHOOL_KIND AS CLASSCD ");
            } else {
                stb.append("   T1.CLASSCD ");
            }
            stb.append("   , CLASSNAME ");
            stb.append("FROM ");
            stb.append("   CLASS_MST T1 ");

            for (final Iterator rit = KnjDbUtils.query(db2, stb.toString()).iterator(); rit.hasNext();) {
                final Map row = (Map) rit.next();
                _classNameMap.put(KnjDbUtils.getString(row, "CLASSCD"), KnjDbUtils.getString(row, "CLASSNAME"));
            }
        }

        private void setZenkiKamokuList(final DB2UDB db2) {
            if (KnjDbUtils.setTableColumnCheck(db2, "SUBCLASS_DETAIL_DAT", null)) {
                final String sqlZenkiKamoku = " SELECT " + ("1".equals(_useCurriculumcd) ? " CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS SUBCLASSCD " : " SUBCLASSCD ")  + " FROM SUBCLASS_DETAIL_DAT WHERE YEAR = '" + _year + "' AND SUBCLASS_SEQ = '012' AND SUBCLASS_REMARK1 = '1' ";
                _zenkiKamokuSubclasscdList = KnjDbUtils.getColumnDataList(KnjDbUtils.query(db2, sqlZenkiKamoku), "SUBCLASSCD");
            } else {
                _zenkiKamokuSubclasscdList = new ArrayList();
            }
        }


        /**
         * 特別活動グループ名称取得処理
         * @param db2
         */
        private void setSpecialGroupName(final DB2UDB db2) {
            final String sql = "SELECT * FROM ATTEND_SUBCLASS_SPECIAL_MST ";
            for (final Iterator rit = KnjDbUtils.query(db2, sql).iterator(); rit.hasNext();) {
                final Map row = (Map) rit.next();
                _specialGroupNames.put(KnjDbUtils.getString(row, "SPECIAL_GROUP_CD"), KnjDbUtils.getString(row, "SPECIAL_GROUP_NAME"));
            }
        }

        public String getSpecialGroupName(final String specialGroupCd) {
            if (SPECIAL_ALL.equals(specialGroupCd)) {
                return "特別活動";
            }
            return (String) _specialGroupNames.get(specialGroupCd);
        }

        /**
         * 学期名称取得
         * @param db2
         */
        private Map setSemesterName(final DB2UDB db2) {
            Map retMap = new HashMap();
            final String sql = " SELECT SEMESTER,SEMESTERNAME FROM SEMESTER_MST WHERE YEAR = '" + _year + "' ";
            for (final Iterator rit = KnjDbUtils.query(db2, sql).iterator(); rit.hasNext();) {
                final Map row = (Map) rit.next();
                retMap.put(KnjDbUtils.getString(row, "SEMESTER"), KnjDbUtils.getString(row, "SEMESTERNAME"));
            }
            return retMap;
        }
        /**
         * 学期名称取得
         * @param db2
         */
        private Map setSubclsSemesMap(final DB2UDB db2) {
            Map retMap = new HashMap();
            String sql = " SELECT ";
            sql += " CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS SUBCLASSCD, ";
            sql += " SUBCLASS_REMARK1, ";
            sql += " SUBCLASS_REMARK2, ";
            sql += " SUBCLASS_REMARK3, ";
            sql += " SUBCLASS_REMARK4 ";
            sql += " FROM SUBCLASS_DETAIL_DAT ";
            sql += " WHERE YEAR = '" + _year + "' ";
            sql += " AND SUBCLASS_SEQ = '012' ";
            sql += " AND (SUBCLASS_REMARK1 <> '' OR SUBCLASS_REMARK2 <> '' OR SUBCLASS_REMARK3 <> '' OR SUBCLASS_REMARK4 <> '') ";
            log.debug("sql = " + sql);
            for (final Iterator rit = KnjDbUtils.query(db2, sql).iterator(); rit.hasNext();) {
                final Map row = (Map) rit.next();
                SubClsDetDat012 addwk = new SubClsDetDat012(KnjDbUtils.getString(row, "SUBCLASSCD"),
                                                             KnjDbUtils.getString(row, "SUBCLASS_REMARK1"),KnjDbUtils.getString(row, "SUBCLASS_REMARK2"),
                                                             KnjDbUtils.getString(row, "SUBCLASS_REMARK3"),KnjDbUtils.getString(row, "SUBCLASS_REMARK4"));
                retMap.put(KnjDbUtils.getString(row, "SUBCLASSCD"), addwk);
            }
            return retMap;
        }
    }

    private static class Student {
        final String _schregno;
        final String _hrName;
        final String _attendNo;
        final String _name;
        final String _sex;
        final String _grade;
        final String _course;
        final Map _subclassAttendanceMap;
        DayAttendance _daysAttendance;
        SpecialSubclassAttendance _specialSubclassAttendance;

        public static List loadStudentList(final DB2UDB db2, final Param param) {
            final List studentList = new ArrayList();

            try {
                // HRの生徒を取得
                final String sql = sqlSchregRegdDat(param);
                //log.debug("schreg_regd_dat sql = " + sql);
                for (final Iterator rit = KnjDbUtils.query(db2, sql).iterator(); rit.hasNext();) {
                    final Map row = (Map) rit.next();
                    final Student st = new Student(
                            KnjDbUtils.getString(row, "SCHREGNO"),
                            KnjDbUtils.getString(row, "HR_NAME"),
                            KnjDbUtils.getString(row, "ATTENDNO"),
                            KnjDbUtils.getString(row, "NAME"),
                            KnjDbUtils.getString(row, "SEX"),
                            KnjDbUtils.getString(row, "GRADE"),
                            KnjDbUtils.getString(row, "COURSE"));
                    studentList.add(st);
                }
            } catch (Exception e) {
                log.fatal("exception!", e);
            }
            return studentList;
        }

        /** 学生を得るSQL */
        private static String sqlSchregRegdDat(final Param param) {
            StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.GRADE, ");
            stb.append("     T3.HR_NAME, ");
            stb.append("     T1.ATTENDNO, ");
            stb.append("     T2.NAME, ");
            stb.append("     T2.SEX, ");
            stb.append("     T1.COURSECD || T1.MAJORCD || T1.COURSECODE AS COURSE ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT T1");
            stb.append("     INNER JOIN SCHREG_BASE_MST T2 ON ");
            stb.append("         T1.SCHREGNO = T2.SCHREGNO ");
            stb.append("     INNER JOIN SCHREG_REGD_HDAT T3 ON ");
            stb.append("         T1.YEAR = T3.YEAR ");
            stb.append("         AND T1.SEMESTER = T3.SEMESTER ");
            stb.append("         AND T1.GRADE = T3.GRADE ");
            stb.append("         AND T1.HR_CLASS = T3.HR_CLASS ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.SEMESTER = '" + param._semester + "' ");
            stb.append("     AND T1.GRADE || T1.HR_CLASS = '" + param._gradeHrclass + "' ");
            stb.append("     AND T1.SCHREGNO IN " + SQLUtils.whereIn(true, param._schregno) + " ");
            stb.append(" ORDER BY ");
            stb.append("     T1.ATTENDNO ");
            return stb.toString();
        }

        public Student(
                final String schregno,
                final String hrName,
                final String attendNo,
                final String name,
                final String sex,
                final String grade,
                final String course) {
            _schregno = schregno;
            _hrName = hrName;
            _attendNo = attendNo;
            _name = name;
            _sex = sex;
            _grade = grade;
            _course = course;
            _daysAttendance = new DayAttendance();
            _subclassAttendanceMap = new TreeMap();
            _specialSubclassAttendance = new SpecialSubclassAttendance();
        }

        public int getPage(final int maxLinePerPage, final Param param) {
            if (!hasData()) {
                return 0;
            }

            final int size1 = _subclassAttendanceMap.size();
            final int size2 = 0 == param._attendSubclassSpecialMstSize ? 0 : "1".equals(param._printAttendSubclassSpecialEachGroup) ? 1 : _specialSubclassAttendance.size();
            final int size = size1 + size2;
            int page = size / maxLinePerPage + (size % maxLinePerPage == 0 ? 0 : 1);
            return page;
        }

        public boolean hasData() {
            return _subclassAttendanceMap.size() != 0;
        }

        public String toString() {
            DecimalFormat df3 = new DecimalFormat("00");
            String attendNo = df3.format(Integer.valueOf(_attendNo).intValue());
            String space = "";
            for (int i=_name.length(); i<7; i++) {
                space += "  ";
            }
            StringBuffer stb = new StringBuffer(_schregno + ":" + attendNo + " , " + _name + space);
//            stb.append("\n" + _daysAttendance.toString());
//            for (Iterator it = _subclassAttendanceMap.keySet().iterator(); it.hasNext();) {
//                String subclassCd = (String) it.next();
//                SubclassAttendance sa = (SubclassAttendance) _subclassAttendanceMap.get(subclassCd);
//                stb.append("\n" + sa.toString());
//            }
            return stb.toString();
        }
    }

    /** 1日出欠カウント */
    private static class DayAttendance {
        /** 授業日数 */
        private int _lessonDays;
        /** 忌引日数 */
        private int _mourningDays;
        /** 出停日数 */
        private int _suspendDays;
        private int _virusDays;
        private int _koudomeDays;
        /** 留学日数 */
        private int _abroadDays;
        /** 出席すべき日数 */
        private int _mlessonDays;
        /** 欠席日数 */
        private int _sickDays;
        /** 出席日数 */
        private int _attendDays;
        /** 遅刻日数 */
        private int _lateDays;
        /** 早退日数 */
        private int _earlyDays;

        /**
         * @param rs
         * @throws SQLException
         */
        public void add(Map row) {
            int lesson   = KnjDbUtils.getInt(row, "LESSON", i0).intValue(); // 授業日数
            int sick     = KnjDbUtils.getInt(row, "SICK", i0).intValue(); // 病欠日数
            int special  = KnjDbUtils.getInt(row, "MOURNING", i0).intValue() + KnjDbUtils.getInt(row, "SUSPEND", i0).intValue() + KnjDbUtils.getInt(row, "VIRUS", i0).intValue() + KnjDbUtils.getInt(row, "KOUDOME", i0).intValue(); // 特別欠席
            int mlesson  = lesson - special; // 出席すべき日数
            _lessonDays   += lesson;
            _mourningDays += KnjDbUtils.getInt(row, "MOURNING", i0).intValue();
            _suspendDays  += KnjDbUtils.getInt(row, "SUSPEND", i0).intValue();
            _virusDays  += KnjDbUtils.getInt(row, "VIRUS", i0).intValue();
            _koudomeDays  += KnjDbUtils.getInt(row, "KOUDOME", i0).intValue();
            _abroadDays   += KnjDbUtils.getInt(row, "TRANSFER_DATE", i0).intValue();
            _mlessonDays  += mlesson;
            _sickDays     += sick;
            _attendDays   += mlesson - sick; // 出席日数 = 出席すべき日数 - 欠席日数
            _lateDays     += KnjDbUtils.getInt(row, "LATE", i0).intValue();
            _earlyDays    += KnjDbUtils.getInt(row, "EARLY", i0).intValue();
        }

        public String toString() {
            DecimalFormat df5 = new DecimalFormat("000");
            return
            "LESSON=" + df5.format(_lessonDays)
            + ", MOR=" + df5.format(_mourningDays)
            + ", SSP=" + df5.format(_suspendDays)
            + ", ABR=" + df5.format(_abroadDays)
            + ", MLS=" + df5.format(_mlessonDays)
            + ", SCK=" + df5.format(_sickDays)
            + ", ATE=" + df5.format(_attendDays)
            + ", LAT=" + df5.format(_lateDays)
            + ", EAL=" + df5.format(_earlyDays);
        }
    }

    /** 出欠カウント */
    private static class SubclassAttendance {
        /** 科目コード */
        private String _subclassCd;
        /** 教科名 */
        private String _className;
        /** 科目名 */
        private String _subclassName;
        /** 単位数 */
        private String _credit;

        /** 授業時数 */
        private int _lesson;
        /** 忌引時数 */
        private int _mourning;
        /** 出停時数 */
        private int _suspend;
        private int _virus;
        private int _koudome;
        /** 留学時数 */
        private int _abroad;
        /** 出席すべき時数 */
        private int _mlesson;
        /** 欠席時数 */
        private int _absent1;
        /** 出席時数 */
        private int _attend;

        /** 欠課時数+ペナルティー  */
        private BigDecimal _absent2 = new BigDecimal(0);
        /** 遅刻時数 */
        private int _late;
        /** 早退時数 */
        private int _early;
        /** 未出欠時数 */
        private int _uninput;

        /** 履修上限値 */
        private BigDecimal _compAbsenceHigh = new BigDecimal(0);
        private BigDecimal _compAbsenceHighWarn = new BigDecimal(0);

        /** 修得上限値 */
        private BigDecimal _getAbsenceHigh = new BigDecimal(0);
        private BigDecimal _getAbsenceHighWarn = new BigDecimal(0);

        public SubclassAttendance(final String subclassCd, final String className, final String subclassName, final String credit) {
            _subclassCd = subclassCd;
            _className = className;
            _subclassName = subclassName;
            _credit = credit;
        }

        /**
         * 時数単位
         * @param rs
         * @throws SQLException
         */
        public void add(final Map row, final Param param) {
            int lesson   = KnjDbUtils.getInt(row, "LESSON", i0).intValue(); // 授業時数
            int absent1   = KnjDbUtils.getInt(row, "SICK1", i0).intValue(); // 欠課時数
            int mlesson  = KnjDbUtils.getInt(row, "MLESSON", i0).intValue(); // 出席すべき時数
            _lesson   += lesson;
            _mourning += KnjDbUtils.getInt(row, "MOURNING", i0).intValue();
            _suspend  += KnjDbUtils.getInt(row, "SUSPEND", i0).intValue();
            _virus += KnjDbUtils.getInt(row, "VIRUS", i0).intValue();
            _koudome  += KnjDbUtils.getInt(row, "KOUDOME", i0).intValue();
            _mlesson  += mlesson;
            _absent1  += absent1;
            _attend   += mlesson - absent1; // 出席時数 = 出席すべき時数 - 欠課時数

            _late     += "1".equals(param._chikokuHyoujiFlg) ? KnjDbUtils.getInt(row, "LATE", i0).intValue() : KnjDbUtils.getInt(row, "LATE2", i0).intValue();
            _early    += "1".equals(param._chikokuHyoujiFlg) ? KnjDbUtils.getInt(row, "EARLY", i0).intValue() : KnjDbUtils.getInt(row, "EARLY2", i0).intValue();
            if ("1".equals(KnjDbUtils.getString(row, "IS_COMBINED_SUBCLASS"))) {
                _absent2  = _absent2.add(KnjDbUtils.getBigDecimal(row, "REPLACED_SICK", bd0)); // 欠課
            } else {
                _absent2  = _absent2.add(KnjDbUtils.getBigDecimal(row, "SICK2", bd0)); // 欠課
            }
        }

        public String toString() {
            final DecimalFormat df5 = new DecimalFormat("000");
            return "SUBCLASSCD = " + _subclassCd
            + ", LES=" + df5.format(_lesson)
            + ", SM=" + df5.format(_mourning+_suspend)
            + ", MLE=" + df5.format(_mlesson)
            + ", AB1=" + df5.format(_absent1)
            + ", AB2=" + df5.format(_absent2.doubleValue())
            + ", ATT=" + df5.format(_attend)
            + ", LAT=" + df5.format(_late)
            + ", EAR=" + df5.format(_early)
            + ", UNINPUT=" + df5.format(_uninput);
        }
    }

    private static String getHouteiJisuSql(final String subclassCd, final Param param, final boolean isGroup, final String groupCd) {
        final String tableName = isGroup ? "V_CREDIT_SPECIAL_MST" : "V_CREDIT_MST";
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T2.SCHREGNO, ");
        if (isGroup) {
            stb.append("     T1.SPECIAL_GROUP_CD, ");
        } else {
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("    T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
            }
            stb.append("     T1.SUBCLASSCD AS SUBCLASSCD, ");
        }
        stb.append("     T1.ABSENCE_HIGH, ");
        stb.append("     VALUE(T1.ABSENCE_HIGH, 0) ");
        if (param._absenceWarnIsUnitCount) {
            final String sem = "1".equals(param._warnSemester) ? "" : param._warnSemester;
            stb.append("      - VALUE(ABSENCE_WARN" + sem + ", 0) ");
        } else {
            stb.append("      - VALUE(ABSENCE_WARN_RISHU_SEM" + param._warnSemester + ", 0) ");
        }
        stb.append("       AS ABSENCE_HIGH_WARN, ");
        stb.append("     T1.GET_ABSENCE_HIGH, ");
        stb.append("     VALUE(T1.GET_ABSENCE_HIGH, 0) ");
        if (param._absenceWarnIsUnitCount) {
            final String sem = "1".equals(param._warnSemester) ? "" : param._warnSemester;
            stb.append("      - VALUE(ABSENCE_WARN" + sem + ", 0) ");
        } else {
            stb.append("      - VALUE(ABSENCE_WARN_SHUTOKU_SEM" + param._warnSemester + ", 0) ");
        }
        stb.append("       AS GET_ABSENCE_HIGH_WARN ");
        stb.append(" FROM ");
        stb.append("     " + tableName + " T1 ");
        stb.append("     INNER JOIN SCHREG_REGD_DAT T2 ON ");
        stb.append("       T2.GRADE = T1.GRADE AND ");
        stb.append("       T2.COURSECD = T1.COURSECD AND ");
        stb.append("       T2.MAJORCD = T1.MAJORCD AND ");
        stb.append("       T2.COURSECODE = T1.COURSECODE AND ");
        stb.append("       T2.YEAR = T1.YEAR AND ");
        stb.append("       T2.SEMESTER = '" + param._semester + "' ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + param._year + "' ");
        if (isGroup) {
            if (null != groupCd) {
                stb.append("     AND T1.SPECIAL_GROUP_CD = '" + groupCd + "' ");
            }
        } else {
            if (null != subclassCd) {
                stb.append("     AND ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append("    T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
                }
                stb.append("        T1.SUBCLASSCD = '" + subclassCd + "' ");
            }
        }
        stb.append("     AND T2.SCHREGNO = ? ");
        return stb.toString();
    }

    private static String getJituJisuSql(final String subclassCd, final Param param, final boolean isGroup, final String groupCd) {
        final String tableName = isGroup ? "SCHREG_ABSENCE_HIGH_SPECIAL_DAT" : "SCHREG_ABSENCE_HIGH_DAT";
        final String tableName2 = isGroup ? "V_CREDIT_SPECIAL_MST" : "V_CREDIT_MST";
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T2.SCHREGNO, ");
        if (isGroup) {
            stb.append("     T1.SPECIAL_GROUP_CD, ");
        } else {
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("    T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
            }
            stb.append("     T1.SUBCLASSCD AS SUBCLASSCD, ");
        }
        stb.append("     T1.COMP_ABSENCE_HIGH AS ABSENCE_HIGH, ");
        stb.append("     VALUE(T1.COMP_ABSENCE_HIGH, 0) ");
        if (param._absenceWarnIsUnitCount) {
            final String sem = "1".equals(param._warnSemester) ? "" : param._warnSemester;
            stb.append("      - VALUE(T3.ABSENCE_WARN" + sem + ", 0) ");
        } else {
            stb.append("      - VALUE(T3.ABSENCE_WARN_RISHU_SEM" + param._warnSemester + ", 0) ");
        }
        stb.append("        AS ABSENCE_HIGH_WARN, ");
        stb.append("     T1.GET_ABSENCE_HIGH, ");
        stb.append("     VALUE(T1.GET_ABSENCE_HIGH, 0) ");
        if (param._absenceWarnIsUnitCount) {
            final String sem = "1".equals(param._warnSemester) ? "" : param._warnSemester;
            stb.append("      - VALUE(T3.ABSENCE_WARN" + sem + ", 0) ");
        } else {
            stb.append("      - VALUE(T3.ABSENCE_WARN_SHUTOKU_SEM" + param._warnSemester + ", 0) ");
        }
        stb.append("        AS GET_ABSENCE_HIGH_WARN ");
        stb.append(" FROM ");
        stb.append("     " + tableName + " T1 ");
        stb.append("     INNER JOIN SCHREG_REGD_DAT T2 ON ");
        stb.append("       T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("       AND T2.YEAR = T1.YEAR ");
        stb.append("       AND T2.SEMESTER = '" + param._semester + "' ");
        stb.append("     LEFT JOIN " + tableName2 + " T3 ON ");
        if (isGroup) {
            stb.append("       T3.SPECIAL_GROUP_CD = T1.SPECIAL_GROUP_CD ");
        } else {
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("    T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || ");
            }
            stb.append("       T3.SUBCLASSCD = ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("    T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
            }
            stb.append("           T1.SUBCLASSCD ");
        }
        stb.append("       AND T3.COURSECD = T2.COURSECD ");
        stb.append("       AND T3.MAJORCD = T2.MAJORCD ");
        stb.append("       AND T3.GRADE = T2.GRADE ");
        stb.append("       AND T3.COURSECODE = T2.COURSECODE ");
        stb.append("       AND T3.YEAR = T1.YEAR ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + param._year + "' ");
        stb.append("     AND T1.DIV = '2' ");
        if (isGroup) {
            if (null != groupCd) {
                stb.append("     AND T1.SPECIAL_GROUP_CD = '" + groupCd + "' ");
            }
        } else {
            if (null != subclassCd) {
                stb.append("     AND ");
                if ("1".equals(param._useCurriculumcd)) {
                    stb.append("    T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
                }
                stb.append("        T1.SUBCLASSCD = '" + subclassCd + "' ");
            }
        }
        stb.append("     AND T1.SCHREGNO = ? ");
        return stb.toString();
    }

    private static class SpecialSubclassAttendance {
        final Map _specialSubclassLesson;
        final Map _specialSubclassSuspend;
        final Map _specialSubclassMourning;
        final Map _specialSubclassMLesson;
        final Map _specialSubclassKekka;
        final Map _groupCompAbsenceHigh;
        final Map _groupCompAbsenceHighWarn;

        SpecialSubclassAttendance() {
            _specialSubclassLesson = new TreeMap();
            _specialSubclassSuspend = new TreeMap();
            _specialSubclassMourning = new TreeMap();
            _specialSubclassMLesson = new TreeMap();
            _specialSubclassKekka = new TreeMap();
            _groupCompAbsenceHigh = new HashMap();
            _groupCompAbsenceHighWarn = new HashMap();
        }

        public int size() {
            final Set keySet = new HashSet();
            keySet.addAll(_specialSubclassLesson.keySet());
            keySet.addAll(_specialSubclassSuspend.keySet());
            keySet.addAll(_specialSubclassMourning.keySet());
            keySet.addAll(_specialSubclassMLesson.keySet());
            keySet.addAll(_specialSubclassKekka.keySet());
            return keySet.size();
        }

        public void add(final String specialSubclassCd, final int lessonMinutes, final int suspendMinutes, final int mourningMinutes, final int mlessonMinutes, final int kekkaMinutes) {
            if (specialSubclassCd != null) {
                addMinutes(_specialSubclassLesson, specialSubclassCd, lessonMinutes);
                addMinutes(_specialSubclassSuspend, specialSubclassCd, suspendMinutes);
                addMinutes(_specialSubclassMourning, specialSubclassCd, mourningMinutes);
                addMinutes(_specialSubclassMLesson, specialSubclassCd, mlessonMinutes);
                addMinutes(_specialSubclassKekka, specialSubclassCd, kekkaMinutes);
            }
        }

        private static void addMinutes(final Map specialSubclassMinutes, final String specialSubclassCd, final int minutes) {
            if (!specialSubclassMinutes.containsKey(specialSubclassCd)) {
                specialSubclassMinutes.put(specialSubclassCd, new Integer(0));
            }
            int totalKekkaMinutes = ((Integer) specialSubclassMinutes.get(specialSubclassCd)).intValue();
            totalKekkaMinutes += minutes;
            specialSubclassMinutes.put(specialSubclassCd, new Integer(totalKekkaMinutes));
        }

        public static BigDecimal getAbsenceHigh(final String groupCd, final Map absenceHighMap) {
            if (null == absenceHighMap.get(groupCd)) {
                return new BigDecimal(0);
            }
            return (BigDecimal) absenceHighMap.get(groupCd);
        }

        private static BigDecimal getJisu(final Map subclassGroupMinutes, final String specialGroupCd, final Param param) {
            final int minutes = ((Integer) subclassGroupMinutes.get(specialGroupCd)).intValue();
            return getSpecialAttendExe(minutes, param);
        }

        private BigDecimal getTotalJisu(final Map subclassGroupMinutes, final Param param, final boolean debug) {
            BigDecimal totalJisu = new BigDecimal(0);
            for (final Iterator it = _specialSubclassKekka.keySet().iterator(); it.hasNext();) {
                final String specialGroupCd = (String) it.next();
                final BigDecimal jisu = getJisu(subclassGroupMinutes, specialGroupCd, param);
                if (debug) {
                    log.debug(" specialGroupCd = " + specialGroupCd + ", minutes = " + subclassGroupMinutes + ", jisu = " + jisu);
                }
                totalJisu = totalJisu.add(jisu);
            }
            return totalJisu;
        }

        /**
         * 欠課時分を欠課時数に換算した値を得る
         * @param kekka 欠課時分
         * @return 欠課時分を欠課時数に換算した値
         */
        private static BigDecimal getSpecialAttendExe(final int kekka, final Param param) {
            final int jituJifun = (param._knjSchoolMst._jituJifunSpecial == null) ? 50 : Integer.parseInt(param._knjSchoolMst._jituJifunSpecial);
            final BigDecimal bigD = new BigDecimal(kekka).divide(new BigDecimal(jituJifun), 10, BigDecimal.ROUND_DOWN);
            int hasu = 0;
            final String retSt = bigD.toString();
            final int retIndex = retSt.indexOf(".");
            if (retIndex > 0) {
                hasu = Integer.parseInt(retSt.substring(retIndex + 1, retIndex + 2));
            }
            final BigDecimal rtn;
            if ("1".equals(param._knjSchoolMst._tokubetuKatudoKansan)) { // 特活換算：二捨三入 (五捨六入)
                rtn = bigD.setScale(0, hasu < 6 ? BigDecimal.ROUND_FLOOR : BigDecimal.ROUND_CEILING); // hasu < 6 ? 0 : 1;
            } else if ("2".equals(param._knjSchoolMst._tokubetuKatudoKansan)) { // 特活換算：四捨五入
                rtn = bigD.setScale(0, BigDecimal.ROUND_UP);
            } else if ("3".equals(param._knjSchoolMst._tokubetuKatudoKansan)) { // 特活換算：切り上げ
                rtn = bigD.setScale(0, BigDecimal.ROUND_CEILING);
            } else if ("4".equals(param._knjSchoolMst._tokubetuKatudoKansan)) { // 特活換算：切り下げ
                rtn = bigD.setScale(0, BigDecimal.ROUND_FLOOR);
            } else if ("0".equals(param._knjSchoolMst._tokubetuKatudoKansan)) { // 換算無し
                rtn = bigD;
            } else {
                rtn = bigD.setScale(0, hasu < 6 ? BigDecimal.ROUND_FLOOR : BigDecimal.ROUND_CEILING); // hasu < 6 ? 0 : 1;
            }
            return rtn;
        }

    }

    private static class SubClsDetDat012 {
        final String _subclassCd;
        final String _remark1;
        final String _remark2;
        final String _remark3;
        final String _remark4;
        SubClsDetDat012(final String subclassCd, final String remark1, final String remark2, final String remark3, final String remark4) {
            _subclassCd = subclassCd;
            _remark1 = remark1;
            _remark2 = remark2;
            _remark3 = remark3;
            _remark4 = remark4;
        }
        private String getFirstRemarkFlg() {
            String retStr = "";
            if (!"".equals(StringUtils.defaultString(_remark1, ""))) {
                retStr = "1";
            } else if (!"".equals(StringUtils.defaultString(_remark2, ""))) {
                retStr = "2";
            } else if (!"".equals(StringUtils.defaultString(_remark3, ""))) {
                retStr = "3";
            } else if (!"".equals(StringUtils.defaultString(_remark4, ""))) {
                retStr = "4";
            }
            return retStr;
        }
    }

}
