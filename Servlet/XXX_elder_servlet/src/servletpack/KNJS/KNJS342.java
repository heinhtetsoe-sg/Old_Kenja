/*
 *
 * 作成日: 2011/09/22
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJS;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 *  学校教育システム 賢者 [小学校プログラム] 出席簿
 *
 */
public class KNJS342 {

    private static final Log log = LogFactory.getLog(KNJS342.class);

    private static final String[] youbi = new String[]{null, "日", "月", "火", "水", "木", "金", "土"};

    private static final int MAX_DATE_LINE = 31;

    private static final Integer dicdSuspend = new Integer(2);
    private static final Integer dicdMourning = new Integer(3);
    private static final Integer dicdSick = new Integer(4);
    private static final Integer dicdNotice = new Integer(5);
    private static final Integer dicdNonotice = new Integer(6);
    private static final Integer dicdOnedaySuspend = new Integer(9);
    private static final Integer dicdOnedayMourning = new Integer(10);
    private static final Integer dicdOnedaySick = new Integer(11);
    private static final Integer dicdOnedayNotice = new Integer(12);
    private static final Integer dicdOnedayNonotice = new Integer(13);
    private static final Integer dicdLate = new Integer(15);
    private static final Integer dicdEarly = new Integer(16);
    private static final Integer dicdVirus = new Integer(19);
    private static final Integer dicdOnedayVirus = new Integer(20);
    private static final Integer dicdLate2 = new Integer(23);
    private static final Integer dicdLate3 = new Integer(24);
    private static final Integer dicdKoudome = new Integer(25);
    private static final Integer dicdOnedayKoudome = new Integer(26);
    private static final Integer maxCd = new Integer(999);

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

    /**
     * すべての生徒をページと出席番号で1ページごとに分割したリストのマップを得る。
     * @param studentList すべての生徒のリスト
     * @param splitCount 1ページに表示する生徒数
     * @return
     */
    private TreeMap<Integer, List<Student>> split(final Param param, final List<Student> studentList, final int splitCount) {
        final TreeMap<Integer, List<Student>> pageListMap = new TreeMap();
        for (final Student s : studentList) {
            final Integer page;
            if (param._isGakunenKongou) {
                page = new Integer(s._page);
            } else {
                page = new Integer(Integer.parseInt(s._attendno) / splitCount + (Integer.parseInt(s._attendno) % splitCount == 0 ? 0 : 1));
            }
            getMappedList(pageListMap, page).add(s);
        }
        return pageListMap;
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {

        final int MAX_STUDENT_LINE;
        final String form;
        if ("1".equals(_param._formSelect)) {
            if ("1".equals(_param._knjs342FORM_DIV)) {
                form = "KNJS342_6.frm";
            } else {
                form = "KNJS342_4.frm";
            }
            MAX_STUDENT_LINE = 15;
        } else {
            if ("1".equals(_param._knjs342FORM_DIV)) {
                form = "KNJS342_5.frm";
            } else {
                form = "KNJS342_2.frm";
            }
            MAX_STUDENT_LINE = 50;
        }
        log.info(" form = " + form);

        final List<Student> allStudentList = getStudentList(db2, MAX_STUDENT_LINE);
        if (allStudentList.size() == 0) {
            return;
        }
        final TreeMap<Integer, List<Student>> pageListMap = split(_param, allStudentList, MAX_STUDENT_LINE);

        final int maxPage = ((Integer) pageListMap.lastKey()).intValue();
        for (final Integer page : pageListMap.keySet()) {
            final List<Student> studentList = pageListMap.get(page);

            final boolean isLastPage = page.intValue() == maxPage;

            svf.VrSetForm(form, 4);

            svfPrintHead(svf, studentList, page.intValue(), allStudentList, MAX_STUDENT_LINE);

            svfPrintAttendSemesDat(svf, studentList);

            svfPrintDayList(svf, studentList, isLastPage, allStudentList, MAX_STUDENT_LINE);
        }
    }

    private void svfPrintHead(final Vrw32alp svf, final List<Student> studentList, final int page, final List<Student> allStudentList, final int MAX_STUDENT_LINE) {
        svf.VrsOut("TITLE", _param._semestername + "　健康観察簿（" + Integer.parseInt(_param._targetMonth) + "月）"); // タイトル
        svf.VrsOut("HR_NAME", _param._hrname); // 年組名称
        final int teacherNamelen = getMS932ByteLength(_param._staffname);
        svf.VrsOut("TEACHER_NAME" + (teacherNamelen <= 20 ? "1" : teacherNamelen <= 30 ? "2" : "3"), _param._staffname); // 担任名
        for (int si = 1; si <= 5; si++) {
            Integer[] dicds = {};
            switch (si) {
            case 1:
                dicds = new Integer[_param._sickNoticeNonoticeCds.size()];
                _param._sickNoticeNonoticeCds.toArray(dicds);
                break;
            case 2:
                //遅刻
                dicds = new Integer[] {dicdLate};
                break;
            case 3:
                //早退
                dicds = new Integer[] {dicdEarly};
                break;
            case 4:
                //忌引
                dicds = new Integer[] {dicdMourning};
                break;
            case 5:
                //出停
                dicds = new Integer[] {dicdSuspend};
                break;
            }
            final Set<String> markSet = new HashSet<String>();
            final StringBuffer stb = new StringBuffer();
            for (int di = 0; di < dicds.length; di++) {
                final String mark = StringUtils.defaultString(_param._nameMstC001Abbv1.get(dicds[di]));
                if (markSet.contains(mark)) {
                    continue;
                }
                if (stb.length() != 0) {
                    stb.append(" ");
                }
                stb.append(mark);
                markSet.add(mark);
            }
            svf.VrsOut("MARK_SAMPLE" + String.valueOf(si), stb.toString()); // 記号サンプル
        }
        int cnt = 0;
        for (final Integer diCd : _param._sickNoticeNonoticeCds) {
            cnt += 1;
            svf.VrsOut("KESSEKI_NAME" + String.valueOf(cnt), _param._nameMstC001Name1.get(diCd));
        }

        Integer minlesson = null;
        Integer maxlesson = null;
        int zaigakuCount = 0;
        int kessekiCount = 0;
        for (final Student student : allStudentList) {
            final AttendSemesDat semes = student._attendSemesDat;
            if (semes._lesson > 0) {
                maxlesson = new Integer(null == maxlesson ? semes._lesson : Math.max(maxlesson.intValue(), semes._lesson));
                minlesson = new Integer(null == minlesson ? semes._lesson : Math.min(minlesson.intValue(), semes._lesson));
                zaigakuCount++;
                kessekiCount += semes._sick;
                kessekiCount += semes._notice;
                kessekiCount += semes._nonotice;
            }
        }

        svf.VrsOut("LESSON", null == maxlesson ? "" : maxlesson.toString()); // 授業日数
        svf.VrsOut("STUDENT_NUM", String.valueOf(zaigakuCount)); // 在学児童数
        svf.VrsOut("STUDENT_ANSENT", String.valueOf(kessekiCount)); // 全月欠席児童数
        svf.VrsOut("STUDENT_ATTEND", String.valueOf(zaigakuCount * (null == maxlesson ? 0 : maxlesson.intValue()) - kessekiCount)); // 出席児童数

        for (int line = 1; line <= MAX_STUDENT_LINE; line++) {
            svf.VrsOutn("NO", line, String.valueOf((page - 1) * MAX_STUDENT_LINE + line));
        }

        for (final Student student : studentList) {
            final String name;
            if (_param._isGakunenKongou && "1".equals(_param._formSelect)) {
                name = StringUtils.defaultString(student._gradeName1) + " " + StringUtils.defaultString(student._name);
            } else {
                name = student._name;
            }

            final int ketaName = getMS932ByteLength(name);
            svf.VrsOutn(ketaName <= 20 ? "NAME1" : ketaName <= 30 ? "NAME2" : "NAME3", student._line, name);
        }
    }

    /**
     * 生徒ごとの合計・出欠備考・クラスの合計を出力
     * @param svf
     * @param studentList
     * @param total 累積
     */
    private void svfPrintAttendSemesDat(final Vrw32alp svf, final List<Student> studentList) {
        for (final Student student : studentList) {
            final AttendSemesDat semes = student._attendSemesDat;
            final int line = student._line;

            int absence = 0;
            int kekkaLine = 0;
            for (final Integer dicd : _param._sickNoticeNonoticeCds) {
                final Integer count = semes.getSickNoticeNonotice(dicd);
                kekkaLine += 1;
                if (kekkaLine == 1) {
                    svf.VrsOutn("SICK", line, String.valueOf(count)); // 病欠
                } else if (kekkaLine == 2) {
                    svf.VrsOutn("NOTICE", line, String.valueOf(count)); // 事故欠
                }
                absence += count.intValue();
            }

            svf.VrsOutn("ATTEND_DAY", line, String.valueOf(semes.jugyoNissu() - absence)); // 出席日数
            svf.VrsOutn("MOURINING", line, String.valueOf(semes._mourning)); // 忌引
            svf.VrsOutn("SUSPEND", line, String.valueOf(semes.shuttei())); // 出停
            svf.VrsOutn("LATE", line, String.valueOf(semes._late)); // 遅刻
            svf.VrsOutn("EARLY", line, String.valueOf(semes._early)); // 早退
        }
    }

    /**
     * 日付ごとの勤怠を出力(レコード)
     * @param svf
     * @param studentList
     * @param isLastPage
     * @param allStudentList
     */
    private void svfPrintDayList(final Vrw32alp svf, final List<Student> studentList, final boolean isLastPage, final List<Student> allStudentList, final int MAX_STUDENT_LINE) {
        if (isLastPage) {
            printTotal(svf, studentList);
        }
        String schoolKind = Student.getStudentSchoolKind(allStudentList);
        log.debug(" schoolKind = " + schoolKind);
        final Map dates = getMappedMap(_param._schoolKindDates, schoolKind);
        if (dates.isEmpty()) {
            createSchoolKindDates(_param._dates, _param._schoolKindDates, schoolKind);
        }
        int dayLine = 0;
        for (final Iterator dateit = dates.keySet().iterator(); dateit.hasNext();) { // 日付ごとにレコード出力
            final String date = (String) dateit.next();
            final PrintDay printDay = (PrintDay) dates.get(date);
            dayLine += 1;
            final boolean isAmikakeTargetDay = "1".equals(printDay._holidayFlg) || !printDay._isPrintTargetSemester;

            if (isAmikakeTargetDay) {
                svf.VrsOut("DAY2", printDay._dayOfMonth); // 日付
                svf.VrsOut("DAY_WEEK2", printDay._youbi); // 曜日
            } else {
                svf.VrsOut("DAY1", printDay._dayOfMonth); // 日付
                svf.VrsOut("DAY_WEEK1", printDay._youbi); // 曜日

                printMark(svf, studentList, MAX_STUDENT_LINE, date, printDay._remark1, isAmikakeTargetDay);

                if (isLastPage) {
                    if (getCalendar(date).get(Calendar.DATE) > Integer.parseInt(_param._targetDay) || !_param._isGakunenKongou && isAmikakeTargetDay) {
                        // 表示しない
                    } else {
                        final DiCdAccumulate diCdAcc = new DiCdAccumulate();
                        int regdCount = 0;
                        for (final Student student : allStudentList) {

                            final boolean isZaiseki = between(date, student._entDate, student._grdDate);
                            final boolean isSubeki;
                            if (student._isHoumonsei && !student._eventSchregDatList.isEmpty()) {
                                isSubeki = !student._eventSchregDatHolidayList.contains(date);
                            } else {
                                isSubeki = isZaiseki && !isAmikakeTargetDay;
                            }

                            if (isSubeki) {
                                regdCount += 1;
                                final AttendDayDat attendDayDat = student._attendDayDatMap.get(date);
                                if (null != attendDayDat) {
                                    for (final Iterator it = attendDayDat._diCdMarkMap.keySet().iterator(); it.hasNext();) {
                                        final Integer diCd = (Integer) it.next();
                                        getMappedList(diCdAcc._diListMap, diCd).add(attendDayDat._schregno);
                                    }
                                }
                            }
                        }

                        final int studentsRegdCount = regdCount;
                        final int suspend = diCdAcc.count(dicdSuspend) + diCdAcc.count(dicdOnedaySuspend);
                        final int mourning = diCdAcc.count(dicdMourning) + diCdAcc.count(dicdOnedayMourning);
                        final int koudome = diCdAcc.count(dicdKoudome) + diCdAcc.count(dicdOnedayKoudome);
                        final int virus = diCdAcc.count(dicdVirus) + diCdAcc.count(dicdOnedayVirus);
                        final int suspendMourning = suspend + mourning + koudome + virus;
                        final int sick = diCdAcc.count(dicdSick) + diCdAcc.count(dicdOnedaySick);
                        final int notice = diCdAcc.count(dicdNotice) + diCdAcc.count(dicdOnedayNotice);
                        final int nonotice = diCdAcc.count(dicdNonotice) + diCdAcc.count(dicdOnedayNonotice);
                        int absence = 0;

                            absence += sick;
                            absence += notice;
                            absence += nonotice;

                        svf.VriOut("ATTEND_NUM", studentsRegdCount - suspendMourning - absence); // 出席数
                        svf.VriOut("ABSENT_NUM", absence); // 欠席数
                    }
                }
            }
            svf.VrEndRecord();
        }
        for (int di = dayLine; di < 31; di++) {
            svf.VrsOut("DAY3", "-"); // 日付
            svf.VrEndRecord();
        }
        _hasData = true;
    }

    public void printTotal(final Vrw32alp svf, final List<Student> studentList) {
        final AttendSemesDat total = new AttendSemesDat();
        for (final Student student : studentList) {
            total.add(student._attendSemesDat);
        }
        int line = 0;
        for (final Integer dicd : _param._sickNoticeNonoticeCds) {
            final Integer totalSickNoticeNonotice = total.getSickNoticeNonotice(dicd);
            final String totalSickNoticeNonoticeStr = null == totalSickNoticeNonotice ? "" : totalSickNoticeNonotice.toString();
            line += 1;
            if (line == 1) {
                svf.VrsOut("TOTAL_SICK", totalSickNoticeNonoticeStr); // 病欠
            } else if (line == 2) {
                svf.VrsOut("TOTAL_NOTICE", totalSickNoticeNonoticeStr); // 事故欠
            }
        }

        svf.VrsOut("TOTAL_MOURINING", String.valueOf(total._mourning)); // 忌引
        svf.VrsOut("TOTAL_SUSPEND", String.valueOf(total.shuttei())); // 出停
        svf.VrsOut("TOTAL_LATE", String.valueOf(total._late)); // 遅刻
        svf.VrsOut("TOTAL_EARLY", String.valueOf(total._early)); // 早退
    }

    private void printMark(final Vrw32alp svf, final List<Student> studentList, final int MAX_STUDENT_LINE, final String date, final String remark1, final boolean isAmikakeTargetDay) {
        final String holidayAmikakeAttribute = "Paint=(0,80,2)";
        for (final Student student : studentList) {
            final String field = "ATTEND_MAERK" + student._line;

            Boolean isAmikake = null;
            if (student._isHoumonsei) {
                  // 訪問生
                if (student._eventSchregDatList.isEmpty()) {
                    // 時間割がない -> 基本による
                    isAmikake = null;
                } else {
                    if (student._eventSchregDatHolidayList.contains(date)) {
                        // 時間割があり時間割が休日 -> 休日
                        isAmikake = Boolean.TRUE;
                    } else {
                        // 時間割があり時間割が休日ではない -> 登校日
                        isAmikake = Boolean.FALSE;
                    }
                }
            }
            if (null == isAmikake) {
                if (isAmikakeTargetDay) {
                    isAmikake = Boolean.TRUE; // 基本は網掛け
                } else {
                    isAmikake = Boolean.FALSE; // 基本は網掛け無し
                }
            }
            if (isAmikake.booleanValue()) {
                svf.VrAttribute(field, holidayAmikakeAttribute);
            } else {
                final AttendDayDat attendDayDat = student._attendDayDatMap.get(date);
                if (null != attendDayDat) {
                    svf.VrsOut(field, attendDayDat.getDiMark(_param));
                }
            }
        }
    }

    public boolean between(final String date, final String sStartDate, final String sEndDate) {
        final Calendar cal = getCalendar(date);
        final Calendar startDate = getCalendar(sStartDate);
        final Calendar endDate = getCalendar(sEndDate);
        return (cal.equals(startDate) || cal.after(startDate)) && (cal.equals(endDate) || cal.before(endDate));
    }

    private static Calendar getCalendar(final String date) {
        final Calendar cal = Calendar.getInstance();
        cal.setTime(Date.valueOf(date));
        return cal;
    }

    /**
     * 生徒のリストを得る。
     * @param db2
     * @return
     */
    private List<Student> getStudentList(final DB2UDB db2, final int MAX_STUDENT_LINE) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        final Map<String, Student> studentsMap = new HashMap();
        final List<Student> students = new ArrayList<Student>();
        try {
            final String sql = getAttendDayDatSql();
            log.info(" sqlAttendDayDat =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            final String entGrdCheckSdate = _param._ctrlYear + "-04-01";
            final String entGrdCheckEdate = String.valueOf(1 + Integer.parseInt(_param._ctrlYear)) + "-03-31";

            int cnt = 0;
            while (rs.next()) {
                final String schregno = rs.getString("SCHREGNO");
                if (null == studentsMap.get(schregno)) {
                    final String remark1 = rs.getString("REMARK1");
                    final String entDiv = rs.getString("ENT_DIV");
                    final String grdDiv = rs.getString("GRD_DIV");
                    String entDivName = null;
                    String grdDivName = null;
                    final String entDate = rs.getString("ENT_DATE");
                    final String grdDate = rs.getString("GRD_DATE");
                    final String gradeName1 = rs.getString("GRADE_NAME1");
                    if (null != entDiv && !Arrays.asList(new String[] {"1", "2", "3"}).contains(entDiv) && between(entDate, entGrdCheckSdate, entGrdCheckEdate)) {
                        entDivName = rs.getString("ENT_DIV_NAME");
                    }
                    if (null != grdDiv && !Arrays.asList(new String[] {"1", "4"}).contains(grdDiv) && between(grdDate, entGrdCheckSdate, entGrdCheckEdate)) {
                        grdDivName = rs.getString("GRD_DIV_NAME");
                    }
                    final boolean isHoumonsei = "1".equals(rs.getString("BYDM004REMARK2"));
                    cnt += 1;
                    final int page = 1 + cnt / MAX_STUDENT_LINE + (cnt % MAX_STUDENT_LINE == 0 ? -1 : 0);
                    final int line = cnt % MAX_STUDENT_LINE + MAX_STUDENT_LINE * (cnt % MAX_STUDENT_LINE == 0 ? 1 : 0);
                    final Student student = new Student(schregno, rs.getString("NAME"), rs.getString("GRADE") + rs.getString("HR_CLASS"), null == rs.getString("ATTENDNO") ? "1" : rs.getString("ATTENDNO"), rs.getString("SCHOOL_KIND"), page, line, remark1, entDivName, entDate, grdDivName, grdDate, gradeName1, isHoumonsei);
                    studentsMap.put(schregno, student);
                    students.add(student);
                }
                final Student student = (Student) studentsMap.get(schregno);
                final String attenddate = rs.getString("ATTENDDATE");

                // 出欠備考データ
                if (null != attenddate && null != rs.getString("DI_CD")) {
                    final Integer diCd = Integer.valueOf(rs.getString("DI_CD"));
                    if (null == student._attendDayDatMap.get(attenddate)) {
                        student._attendDayDatMap.put(attenddate, new AttendDayDat(schregno, attenddate));
                    }
                    final AttendDayDat attendDayDat = student._attendDayDatMap.get(attenddate);
                    final Integer sublCd = null == rs.getString("SUBL_CD") ? maxCd : Integer.valueOf(rs.getString("SUBL_CD"));
                    final Integer submCd = null == rs.getString("SUBM_CD") ? maxCd : Integer.valueOf(rs.getString("SUBM_CD"));
                    final String sublAbbv1 = _param._nameMstC006Abbv1.get(sublCd);
                    final String submAbbv1 = _param._nameMstC007Abbv1.get(submCd);

                    attendDayDat.setDiCdMark(diCd, _param._nameMstC001Abbv1.get(diCd), sublCd, sublAbbv1, submCd, submAbbv1);
                    student.setAttendRemark(attenddate, diCd, sublCd, submCd);
                }
            }
        } catch (SQLException ex) {
            log.error("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        if (_param._isMusashinohigashi) {
            try {
                final String sql = getAbsenceRemarkSql();
                log.debug(" sqlAbsenceRemark =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    final String schregno = rs.getString("SCHREGNO");
                    final String absencedate = rs.getString("ABSENCE_DATE");
                    if (null == studentsMap.get(schregno) || null == absencedate) {
                        continue;
                    }
                    final Student student = (Student) studentsMap.get(schregno);
                    final String remark = rs.getString("REMARK");
                    student._absenceRemark.put(absencedate, remark);
                }
            } catch (SQLException ex) {
                log.error("Exception:", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
        try {
            final String sql = getAttendSemesDatSql();
            log.debug(" sqlAttendSemesDat =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final Student student = studentsMap.get(rs.getString("SCHREGNO"));
                if (null != student) {
                    student._attendSemesDat._lesson = rs.getInt("LESSON");
                    student._attendSemesDat._suspend = rs.getInt("SUSPEND");
                    student._attendSemesDat._koudome = "true".equals(_param._useKoudome) ? rs.getInt("KOUDOME") : 0;
                    student._attendSemesDat._virus = "true".equals(_param._useVirus) ? rs.getInt("VIRUS") : 0;
                    student._attendSemesDat._mourning = rs.getInt("MOURNING");
                    student._attendSemesDat._sick = _param._nameMstC001Name1.containsKey(dicdSick) ? rs.getInt("SICK") : 0;
                    student._attendSemesDat._notice = _param._nameMstC001Name1.containsKey(dicdNotice) ? rs.getInt("NOTICE") : 0;
                    student._attendSemesDat._nonotice = _param._nameMstC001Name1.containsKey(dicdNonotice) ? rs.getInt("NONOTICE") : 0;
                    student._attendSemesDat._late = rs.getInt("LATE");
                    student._attendSemesDat._early = rs.getInt("EARLY");
                    student._attendSemesDat._kekkaJisu = rs.getInt("KEKKA_JISU");
                }
            }
        } catch (SQLException ex) {
            log.error("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        try {
            ps = null;
            for (final Student student : students) {
                if (student._isHoumonsei) {
                    student._eventSchregDatList = new ArrayList();
                    student._eventSchregDatHolidayList = new ArrayList();

                    if (null == ps) {
                        final String sql = getEventSchregDatSql(_param);
                        log.debug(" eventSchregDat sql =" + sql);
                        ps = db2.prepareStatement(sql);
                    }
                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();

                    while (rs.next()) {
                        final String executedate = rs.getString("EXECUTEDATE");
                        student._eventSchregDatList.add(executedate);
                        if ("1".equals(rs.getString("HOLIDAY_FLG")) ) {
                            student._eventSchregDatHolidayList.add(executedate);
                        }
                    }
                    DbUtils.closeQuietly(rs);
                }
            }
        } catch (SQLException ex) {
            log.error("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }

        return students;
    }

    private String getAttendDayDatSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH SCHREGNOS AS (SELECT DISTINCT ");
        if (_param._isGhr) {
            stb.append("     T1.SCHREGNO, ");
            stb.append("     BASE.NAME, ");
            stb.append("     T1.GHR_ATTENDNO AS ATTENDNO, ");
        } else if (_param._isGakunenKongou) {
            stb.append("     REGD.SCHREGNO, ");
            stb.append("     BASE.NAME, ");
            stb.append("     REGD.ATTENDNO, ");
        } else {
            stb.append("     T1.SCHREGNO, ");
            stb.append("     BASE.NAME, ");
            stb.append("     T1.ATTENDNO, ");
        }
        stb.append("     REGD.GRADE, ");
        stb.append("     REGD.HR_CLASS, ");
        stb.append("     T2.ENT_DIV, ");
        stb.append("     T2.GRD_DIV, ");
        stb.append("     T4.SCHOOL_KIND, ");
        stb.append("     T4.GRADE_NAME1, ");
        stb.append("     NMA002.NAME1 AS ENT_DIV_NAME, ");
        stb.append("     NMA003.NAME1 AS GRD_DIV_NAME, ");
        stb.append("     VALUE(T2.ENT_DATE, '1900-01-01') AS ENT_DATE, ");
        stb.append("     VALUE(T2.GRD_DATE, '9999-12-31') AS GRD_DATE, ");
        stb.append("     BYDM004.BASE_REMARK2 AS BYDM004REMARK2");
        if (_param._isFi) {
            stb.append(" FROM SCHREG_REGD_FI_DAT T1 ");
            stb.append("     LEFT JOIN SCHREG_REGD_DAT REGD ON REGD.SCHREGNO = T1.SCHREGNO AND REGD.YEAR = T1.YEAR AND REGD.SEMESTER = T1.SEMESTER ");
        } else if (_param._isGhr) {
            stb.append(" FROM SCHREG_REGD_GHR_DAT T1 ");
            stb.append("     LEFT JOIN SCHREG_REGD_DAT REGD ON REGD.SCHREGNO = T1.SCHREGNO AND REGD.YEAR = T1.YEAR AND REGD.SEMESTER = T1.SEMESTER ");
        } else if (_param._isGakunenKongou) {
            stb.append(" FROM V_STAFF_HR_DAT T1 ");
            stb.append("     LEFT JOIN SCHREG_REGD_HDAT HD ON HD.YEAR = T1.YEAR AND HD.SEMESTER = T1.SEMESTER AND HD.GRADE = T1.GRADE AND HD.HR_CLASS = T1.HR_CLASS ");
            stb.append("     LEFT JOIN SCHREG_REGD_DAT REGD ON REGD.YEAR = T1.YEAR AND REGD.SEMESTER = T1.SEMESTER AND REGD.GRADE = HD.GRADE AND REGD.HR_CLASS = HD.HR_CLASS ");
        } else {
            stb.append(" FROM SCHREG_REGD_DAT T1 ");
            stb.append("     LEFT JOIN SCHREG_REGD_DAT REGD ON REGD.SCHREGNO = T1.SCHREGNO AND REGD.YEAR = T1.YEAR AND REGD.SEMESTER = T1.SEMESTER ");
        }
        stb.append("     LEFT JOIN SCHREG_REGD_GDAT T4 ON T4.YEAR = REGD.YEAR AND T4.GRADE = REGD.GRADE ");
        stb.append("     LEFT JOIN SCHREG_ENT_GRD_HIST_DAT T2 ON T2.SCHREGNO = REGD.SCHREGNO AND T2.SCHOOL_KIND = T4.SCHOOL_KIND ");
        stb.append("     INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ");
        stb.append("     LEFT JOIN NAME_MST NMA002 ON NMA002.NAMECD1 = 'A002' AND NMA002.NAMECD2 = T2.ENT_DIV ");
        stb.append("     LEFT JOIN NAME_MST NMA003 ON NMA003.NAMECD1 = 'A003' AND NMA003.NAMECD2 = T2.GRD_DIV ");
        stb.append("     LEFT JOIN SCHREG_BASE_YEAR_DETAIL_MST BYDM004 ON BYDM004.SCHREGNO = REGD.SCHREGNO AND BYDM004.YEAR = REGD.YEAR AND BYDM004.BASE_SEQ = '004' ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param._targetSemes + "' ");
        if (_param._isGhr) {
            stb.append("     AND T1.GHR_CD = '" + _param._gradeHrclass + "' ");
        } else if (_param._isGakunenKongou) {
            stb.append("     AND T1.SCHOOL_KIND || '-' || T1.HR_CLASS = '" + _param._gradeHrclass + "' ");
            if ("1".equals(_param._restrictFlg)) {
                stb.append("     AND T1.STAFFCD = '" + _param._printLogStaffcd + "' ");
            }
        } else {
            stb.append("     AND T1.GRADE || T1.HR_CLASS = '" + _param._gradeHrclass + "' ");
        }
        stb.append(" ) ");

        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.NAME, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     T1.SCHOOL_KIND, ");
        stb.append("     SEME_R.REMARK1, ");
        stb.append("     T1.ENT_DIV, ");
        stb.append("     T1.GRD_DIV, ");
        stb.append("     T1.ENT_DIV_NAME, ");
        stb.append("     T1.GRD_DIV_NAME, ");
        stb.append("     T1.ENT_DATE, ");
        stb.append("     T1.GRD_DATE, ");
        stb.append("     T1.GRADE_NAME1, ");
        stb.append("     T1.BYDM004REMARK2, ");
        stb.append("     T2.ATTENDDATE, ");
        stb.append("     T2.DI_CD, ");
        stb.append("     T4.SUBL_CD, ");
        stb.append("     T5.SUBM_CD ");
        stb.append(" FROM ");
        stb.append("     SCHREGNOS T1 ");
        stb.append("     LEFT JOIN ATTEND_DAY_DAT T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("         AND T2.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("         AND MONTH(T2.ATTENDDATE) = " + Integer.parseInt(_param._targetMonth) + " ");
        stb.append("         AND DAY(T2.ATTENDDATE) <= " + Integer.parseInt(_param._targetDay) + " ");
        stb.append("         AND T2.ATTENDDATE BETWEEN T1.ENT_DATE AND T1.GRD_DATE ");
        stb.append("     LEFT JOIN ATTEND_DAY_SUBL_DAT T4 ON T4.SCHREGNO = T2.SCHREGNO ");
        stb.append("       AND T4.ATTENDDATE = T2.ATTENDDATE ");
        stb.append("       AND T4.DI_CD = T2.DI_CD ");
        stb.append("       AND T4.YEAR = T2.YEAR ");
        stb.append("     LEFT JOIN ATTEND_DAY_SUBM_DAT T5 ON T5.SCHREGNO = T2.SCHREGNO ");
        stb.append("       AND T5.ATTENDDATE = T2.ATTENDDATE ");
        stb.append("       AND T5.DI_CD = T2.DI_CD ");
        stb.append("       AND T5.YEAR = T2.YEAR ");
        stb.append("       AND T5.SUBL_CD = T4.SUBL_CD ");
        stb.append("     LEFT JOIN ATTEND_SEMES_REMARK_DAT SEME_R ON SEME_R.COPYCD = '0' ");
        stb.append("          AND SEME_R.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("          AND SEME_R.MONTH = '" + _param._targetMonth + "' ");
        stb.append("          AND SEME_R.SEMESTER = '" + _param._targetSemes + "' ");
        stb.append("          AND SEME_R.SCHREGNO = T1.SCHREGNO ");
        stb.append(" ORDER BY ");
        if (_param._isGakunenKongou) {
            stb.append("     T1.GRADE, ");
            stb.append("     T1.HR_CLASS, ");
        }
        stb.append("     T1.ATTENDNO, T2.ATTENDDATE ");
        return stb.toString();
    }

    private String getAbsenceRemarkSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH SCHREGNOS AS (SELECT DISTINCT ");
        if (_param._isGakunenKongou) {
            stb.append("     REGD.SCHREGNO, ");
        } else {
            stb.append("     T1.SCHREGNO, ");
        }
        stb.append("     VALUE(T2.ENT_DATE, '1900-01-01') AS ENT_DATE, ");
        stb.append("     VALUE(T2.GRD_DATE, '9999-12-31') AS GRD_DATE ");
        if (_param._isFi) {
            stb.append(" FROM SCHREG_REGD_FI_DAT T1 ");
            stb.append("     LEFT JOIN SCHREG_REGD_DAT REGD ON REGD.SCHREGNO = T1.SCHREGNO AND REGD.YEAR = T1.YEAR AND REGD.SEMESTER = T1.SEMESTER ");
        } else if (_param._isGhr) {
            stb.append(" FROM SCHREG_REGD_GHR_DAT T1 ");
            stb.append("     LEFT JOIN SCHREG_REGD_DAT REGD ON REGD.SCHREGNO = T1.SCHREGNO AND REGD.YEAR = T1.YEAR AND REGD.SEMESTER = T1.SEMESTER ");
        } else if (_param._isGakunenKongou) {
            stb.append(" FROM V_STAFF_HR_DAT T1 ");
            stb.append("     LEFT JOIN SCHREG_REGD_HDAT HD ON HD.YEAR = T1.YEAR AND HD.SEMESTER = T1.SEMESTER AND HD.GRADE = T1.GRADE AND HD.HR_CLASS = T1.HR_CLASS ");
            stb.append("     LEFT JOIN SCHREG_REGD_DAT REGD ON REGD.YEAR = T1.YEAR AND REGD.SEMESTER = T1.SEMESTER AND REGD.GRADE = HD.GRADE AND REGD.HR_CLASS = HD.HR_CLASS ");
        } else {
            stb.append(" FROM SCHREG_REGD_DAT T1 ");
            stb.append("     LEFT JOIN SCHREG_REGD_DAT REGD ON REGD.SCHREGNO = T1.SCHREGNO AND REGD.YEAR = T1.YEAR AND REGD.SEMESTER = T1.SEMESTER ");
        }
        stb.append("     LEFT JOIN SCHREG_REGD_GDAT T4 ON T4.YEAR = REGD.YEAR AND T4.GRADE = REGD.GRADE ");
        stb.append("     LEFT JOIN SCHREG_ENT_GRD_HIST_DAT T2 ON T2.SCHREGNO = REGD.SCHREGNO AND T2.SCHOOL_KIND = T4.SCHOOL_KIND ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param._targetSemes + "' ");
        if (_param._isGhr) {
            stb.append("     AND T1.GHR_CD = '" + _param._gradeHrclass + "' ");
        } else if (_param._isGakunenKongou) {
            stb.append("     AND T1.SCHOOL_KIND || '-' || T1.HR_CLASS = '" + _param._gradeHrclass + "' ");
            if ("1".equals(_param._restrictFlg)) {
                stb.append("     AND T1.STAFFCD = '" + _param._printLogStaffcd + "' ");
            }
        } else {
            stb.append("     AND T1.GRADE || T1.HR_CLASS = '" + _param._gradeHrclass + "' ");
        }
        stb.append(" ) ");

        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     REM.ABSENCE_DATE, ");
        stb.append("     REM.REMARK ");
        stb.append(" FROM ");
        stb.append("     SCHREGNOS T1 ");
        stb.append("     LEFT JOIN ATTEND_ABSENCE_REMARK_DAT REM ON REM.SCHREGNO = T1.SCHREGNO ");
        stb.append("         AND MONTH(REM.ABSENCE_DATE) = " + Integer.parseInt(_param._targetMonth) + " ");
        stb.append("         AND DAY(REM.ABSENCE_DATE) <= " + Integer.parseInt(_param._targetDay) + " ");
        stb.append("         AND REM.ABSENCE_DATE BETWEEN T1.ENT_DATE AND T1.GRD_DATE ");
        stb.append(" ORDER BY ");
        stb.append("     REM.ABSENCE_DATE ");
        return stb.toString();
    }

    private String getAttendSemesDatSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH SCHREGNOS AS (SELECT DISTINCT ");
        if (_param._isGakunenKongou) {
            stb.append("     REGD.SCHREGNO, ");
        } else {
            stb.append("     T1.SCHREGNO, ");
        }
        stb.append("     REGD.GRADE, ");
        stb.append("     REGD.COURSECD, ");
        stb.append("     REGD.MAJORCD, ");
        stb.append("     VALUE(T2.ENT_DATE, '1900-01-01') AS ENT_DATE, ");
        stb.append("     VALUE(T2.GRD_DATE, '9999-12-31') AS GRD_DATE ");
        if (_param._isFi) {
            stb.append(" FROM SCHREG_REGD_FI_DAT T1 ");
            stb.append("     LEFT JOIN SCHREG_REGD_DAT REGD ON REGD.SCHREGNO = T1.SCHREGNO AND REGD.YEAR = T1.YEAR AND REGD.SEMESTER = T1.SEMESTER ");
        } else if (_param._isGhr) {
            stb.append(" FROM SCHREG_REGD_GHR_DAT T1 ");
            stb.append("     LEFT JOIN SCHREG_REGD_DAT REGD ON REGD.SCHREGNO = T1.SCHREGNO AND REGD.YEAR = T1.YEAR AND REGD.SEMESTER = T1.SEMESTER ");
        } else if (_param._isGakunenKongou) {
            stb.append(" FROM V_STAFF_HR_DAT T1 ");
            stb.append("     LEFT JOIN SCHREG_REGD_HDAT HD ON HD.YEAR = T1.YEAR AND HD.SEMESTER = T1.SEMESTER AND HD.GRADE = T1.GRADE AND HD.HR_CLASS = T1.HR_CLASS ");
            stb.append("     LEFT JOIN SCHREG_REGD_DAT REGD ON REGD.YEAR = T1.YEAR AND REGD.SEMESTER = T1.SEMESTER AND REGD.GRADE = HD.GRADE AND REGD.HR_CLASS = HD.HR_CLASS ");
        } else {
            stb.append(" FROM SCHREG_REGD_DAT T1 ");
            stb.append("     LEFT JOIN SCHREG_REGD_DAT REGD ON REGD.SCHREGNO = T1.SCHREGNO AND REGD.YEAR = T1.YEAR AND REGD.SEMESTER = T1.SEMESTER ");
        }
        stb.append("     LEFT JOIN SCHREG_REGD_GDAT T3 ON T3.YEAR = REGD.YEAR AND T3.GRADE = REGD.GRADE ");
        stb.append("     LEFT JOIN SCHREG_ENT_GRD_HIST_DAT T2 ON T2.SCHREGNO = REGD.SCHREGNO AND T2.SCHOOL_KIND = T3.SCHOOL_KIND ");
        stb.append("   WHERE ");
        stb.append("     T1.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param._targetSemes + "' ");
        if (_param._isGhr) {
            stb.append("     AND T1.GHR_CD = '" + _param._gradeHrclass + "' ");
        } else if (_param._isGakunenKongou) {
            stb.append("     AND T1.SCHOOL_KIND || '-' || T1.HR_CLASS = '" + _param._gradeHrclass + "' ");
            if ("1".equals(_param._restrictFlg)) {
                stb.append("     AND T1.STAFFCD = '" + _param._printLogStaffcd + "' ");
            }
        } else {
            stb.append("     AND T1.GRADE || T1.HR_CLASS = '" + _param._gradeHrclass + "' ");
        }
        stb.append(" ), SCHEDULES AS (SELECT ");
        stb.append("     T3.SCHREGNO, ");
        stb.append("     COUNT(DISTINCT T1.EXECUTEDATE) AS LESSON ");
        stb.append("   FROM ");
        stb.append("     SCH_CHR_DAT T1 ");
        stb.append("     INNER JOIN CHAIR_DAT T2 ON T2.YEAR = T1.YEAR AND T2.SEMESTER = T1.SEMESTER AND T2.CHAIRCD = T1.CHAIRCD ");
        stb.append("     INNER JOIN CHAIR_STD_DAT T3 ON T3.YEAR = T2.YEAR AND T3.SEMESTER = T2.SEMESTER AND T3.CHAIRCD = T2.CHAIRCD AND T1.EXECUTEDATE BETWEEN T3.APPDATE AND T3.APPENDDATE ");
        stb.append("     INNER JOIN SCHREGNOS T4 ON T4.SCHREGNO = T3.SCHREGNO ");
        stb.append("   WHERE ");
        stb.append("     T1.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("     AND MONTH(T1.EXECUTEDATE) = " + Integer.parseInt(_param._targetMonth) + " ");
        stb.append("     AND T1.EXECUTEDATE BETWEEN T4.ENT_DATE AND T4.GRD_DATE ");
        stb.append("     AND T1.SEMESTER = '" + _param._targetSemes + "' ");
        stb.append("     AND DAY(T1.EXECUTEDATE) BETWEEN " + String.valueOf(_param._attendDayDatSday) + " AND " + Integer.parseInt(_param._targetDay) + " ");
        stb.append("   GROUP BY ");
        stb.append("     T3.SCHREGNO ");
        stb.append(" ), ATTEND_SEMES AS ( ");
        stb.append("   SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     VALUE(T1.LESSON,0) AS LESSON, ");
        stb.append("     VALUE(T1.SUSPEND,0) AS SUSPEND, ");
        if ("true".equals(_param._useVirus)) {
            stb.append("     VALUE(T1.VIRUS,0) AS VIRUS, ");
        } else {
            stb.append("     0 AS VIRUS, ");
        }
        if ("true".equals(_param._useKoudome)) {
            stb.append("     VALUE(T1.KOUDOME,0) AS KOUDOME, ");
        } else {
            stb.append("     0 AS KOUDOME, ");
        }
        stb.append("     VALUE(T1.MOURNING,0) AS MOURNING, ");
        stb.append("     VALUE(T1.SICK,0) AS SICK, ");
        stb.append("     VALUE(T1.NOTICE,0) AS NOTICE, ");
        stb.append("     VALUE(T1.NONOTICE,0) AS NONOTICE, ");
        stb.append("     VALUE(T1.LATE,0) AS LATE, ");
        stb.append("     VALUE(T1.EARLY,0) AS EARLY, ");
        stb.append("     VALUE(T1.KEKKA_JISU,0) AS KEKKA_JISU ");
        stb.append("   FROM ");
        stb.append("     V_ATTEND_SEMES_DAT T1 ");
        stb.append("     INNER JOIN SCHREGNOS T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("   WHERE ");
        stb.append("     T1.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("     AND T1.MONTH = '" + _param._targetMonth + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param._targetSemes + "' ");
        stb.append("     AND INT(T1.APPOINTED_DAY) = " + String.valueOf(_param._attendSemesAppointedDay) + " ");
        stb.append("   UNION ALL ");
        stb.append("     SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     MAX(VALUE(T4.LESSON,0)) AS LESSON, ");
        stb.append("     SUM(CASE WHEN T1.DI_CD IN ('2', '9') THEN 1 ELSE 0 END) AS SUSPEND, ");
        if ("true".equals(_param._useVirus)) {
            stb.append("     SUM(CASE WHEN T1.DI_CD IN ('19', '20') THEN 1 ELSE 0 END) AS VIRUS, ");
        } else {
            stb.append("     0 AS VIRUS, ");
        }
        if ("true".equals(_param._useKoudome)) {
            stb.append("     SUM(CASE WHEN T1.DI_CD IN ('25', '26') THEN 1 ELSE 0 END) AS KOUDOME, ");
        } else {
            stb.append("     0 AS KOUDOME, ");
        }
        stb.append("     SUM(CASE WHEN T1.DI_CD IN ('3', '10') THEN 1 ELSE 0 END) AS MOURNING, ");
        stb.append("     SUM(CASE WHEN T1.DI_CD IN ('4', '11') THEN 1 ELSE 0 END) AS SICK, ");
        stb.append("     SUM(CASE WHEN T1.DI_CD IN ('5', '12') THEN 1 ELSE 0 END) AS NOTICE, ");
        stb.append("     SUM(CASE WHEN T1.DI_CD IN ('6', '13') THEN 1 ELSE 0 END) AS NONOTICE, ");
        stb.append("     SUM(CASE WHEN T1.DI_CD IN ('15','23','24') THEN 1 ELSE 0 END) AS LATE, ");
        stb.append("     SUM(CASE WHEN T1.DI_CD IN ('16') THEN 1 ELSE 0 END) AS EARLY, ");
        stb.append("     0 AS KEKKA_JISU ");
        stb.append("   FROM ");
        stb.append("     ATTEND_DAY_DAT T1 ");
        stb.append("     INNER JOIN SCHREGNOS T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN SCHEDULES T4 ON T4.SCHREGNO = T1.SCHREGNO ");
        stb.append("   WHERE ");
        stb.append("     T1.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("     AND MONTH(T1.ATTENDDATE) = " + Integer.parseInt(_param._targetMonth) + " ");
        stb.append("     AND DAY(T1.ATTENDDATE) BETWEEN " + String.valueOf(_param._attendDayDatSday) + " AND " + Integer.parseInt(_param._targetDay) + " ");
        stb.append("   GROUP BY ");
        stb.append("     T1.SCHREGNO ");
        stb.append(" ) ");
        stb.append("  SELECT ");
        stb.append("    T1.SCHREGNO, ");
        stb.append("    SUM(T1.LESSON) AS LESSON, ");
        stb.append("    SUM(T1.SUSPEND) AS SUSPEND, ");
        stb.append("    SUM(T1.VIRUS) AS VIRUS, ");
        stb.append("    SUM(T1.KOUDOME) AS KOUDOME, ");
        stb.append("    SUM(T1.MOURNING) AS MOURNING, ");
        stb.append("    SUM(T1.SICK) AS SICK, ");
        stb.append("    SUM(T1.NOTICE) AS NOTICE, ");
        stb.append("    SUM(T1.NONOTICE) AS NONOTICE, ");
        stb.append("    SUM(T1.LATE) AS LATE, ");
        stb.append("    SUM(T1.EARLY) AS EARLY, ");
        stb.append("    SUM(T1.KEKKA_JISU) AS KEKKA_JISU ");
        stb.append("  FROM ");
        stb.append("    ATTEND_SEMES T1 ");
        stb.append("  GROUP BY ");
        stb.append("    T1.SCHREGNO ");

        return stb.toString();
    }

    private String getEventSchregDatSql(final Param param) {

        final String minDate = param._dates.firstKey();
        final String maxDate = param._dates.lastKey();

        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.EXECUTEDATE, ");
        stb.append("     T1.HOLIDAY_FLG ");
        stb.append("  FROM ");
        stb.append("    EVENT_SCHREG_DAT T1 ");
        stb.append("  WHERE ");
        stb.append("     T1.SCHREGNO = ? ");
        stb.append("     AND T1.EXECUTEDATE BETWEEN '" + minDate + "' AND '" + maxDate + "' ");

        return stb.toString();
    }

    private static String getDateString(final Calendar cal) {
        final DecimalFormat df = new DecimalFormat("00");
        return cal.get(Calendar.YEAR) + "-" + df.format(cal.get(Calendar.MONTH) + 1) + "-" + df.format(cal.get(Calendar.DATE));
    }

    private static <A, B> List<B> getMappedList(final Map<A, List<B>> map, final A key) {
        if (!map.containsKey(key)) {
            map.put(key, new ArrayList<B>());
        }
        return map.get(key);
    }

    private static <A, B, C> Map<B, C> getMappedMap(final Map<A, Map<B, C>> map, final A key) {
        if (!map.containsKey(key)) {
            map.put(key, new TreeMap());
        }
        return map.get(key);
    }

    private static void createSchoolKindDates(final TreeMap<String, PrintDay> datesOrigin, final Map<String, Map<String, PrintDay>> schoolKindDates, final String schoolKind) {
        final Map<String, PrintDay> dates = getMappedMap(schoolKindDates, schoolKind);
        for (final String key : datesOrigin.keySet()) {
            final PrintDay printDay = datesOrigin.get(key);
            dates.put(key, new PrintDay(printDay));
        }
    }

    /**
     *  文字数を取得
     */
    private static int getMS932ByteLength(final String str) {
        return KNJ_EditEdit.getMS932ByteLength(str);
    }

    /**
     * 表示する日
     */
    private static class PrintDay {
        final String _date;
        final String _dayOfMonth;
        final String _youbi;
        String _holidayFlg;
        String _remark1;
        boolean _isPrintTargetSemester = false;
        PrintDay(final Calendar cal) {
            _date = getDateString(cal);
            _dayOfMonth = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
            final int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK); // 1:日 2:月 3:火 4:水 5:木 6:金 7:土
            _youbi = youbi[dayOfWeek];
            _holidayFlg = "";
            _remark1 = "";
        }
        PrintDay(final PrintDay printDay) {
            _date = printDay._date;
            _dayOfMonth = printDay._dayOfMonth;
            _youbi = printDay._youbi;
            _holidayFlg = printDay._holidayFlg;
            _remark1 = printDay._remark1;
            _isPrintTargetSemester = printDay._isPrintTargetSemester;
        }
        public String toString() {
            return "PrintDay(" + _date + (null == _holidayFlg ? "" : ",holiday")  + ")";
        }
    }

    /**
     * 生徒
     */
    private static class Student {
        final String _schregno;
        final String _name;
        final String _gradeHrclass;
        final String _attendno;
        final String _schoolKind;
        final String _remark1;
        final String _entDate;
        final String _entDivName;
        final String _grdDate;
        final String _grdDivName;
        final String _gradeName1;
        final boolean _isHoumonsei;
        final int _page;
        final int _line;
        final Map<String, AttendDayDat> _attendDayDatMap = new HashMap();
        final Map _absenceRemark = new TreeMap();
        final Map _diCdMap = new TreeMap();
        final AttendSemesDat _attendSemesDat = new AttendSemesDat();
        private List _eventSchregDatList = Collections.EMPTY_LIST;
        private List _eventSchregDatHolidayList = Collections.EMPTY_LIST;
        Student(final String schregno, final String name, final String gradeHrclass, final String attendno, final String schoolKind, final int page, final int line, final String remark1, final String entDivName, final String entDate, final String grdDivName, final String grdDate, final String gradeName1, final boolean isHoumonsei) {
            _schregno = schregno;
            _name = name;
            _gradeHrclass = gradeHrclass;
            _attendno = attendno;
            _schoolKind = schoolKind;
            _page = page;
            _line = line;
            _remark1 = remark1;
            _entDate  = entDate;
            _entDivName = entDivName;
            _grdDate  = grdDate;
            _grdDivName = grdDivName;
            _gradeName1 = gradeName1;
            _isHoumonsei = isHoumonsei;
        }

        /**
         * 備考をセットする
         * @param attendddate 日付
         * @param sublCd SUBL_CD
         * @param submCd SUBM_CD
         */
        private void setAttendRemark(final String attenddate, final Integer diCd, final Integer sublCd, final Integer submCd) {
            getMappedList(getMappedMap(getMappedMap(_diCdMap, diCd), sublCd), submCd).add(attenddate);
        }

        public static String getStudentSchoolKind(final List<Student> allStudentList) {
            String schoolKind = null;
            for (final Student student : allStudentList) {
                if (null != student._schoolKind) {
                    schoolKind = student._schoolKind;
                    break;
                }
            }
            return schoolKind;
        }

    }

    private static class AttendDayDat {
        private static final Integer LATE = new Integer(15);
        private static final Integer EARLY = new Integer(16);

        final String _schregno;
        final String _date;
        final TreeMap<Integer, String> _diCdMarkMap = new TreeMap(); // 基本的に要素の数は1個。遅刻と早退の場合のみ2個。
        final TreeMap<Integer, String> _lcdMarkMap = new TreeMap();
        final TreeMap<Integer, String> _mcdMarkMap = new TreeMap();
        AttendDayDat(final String schregno, final String date) {
            _schregno = schregno;
            _date = date;
        }
        public void setDiCdMark(final Integer diCd, final String mark, final Integer lcd, final String lmark, final Integer mcd, final String mmark) {
            if (!maxCd.equals(mcd)) {
                _mcdMarkMap.put(mcd, mmark);
            } else if (!maxCd.equals(lcd)) {
                _lcdMarkMap.put(lcd, lmark);
            }
            _diCdMarkMap.put(diCd, mark);
        }
        public String getDiMark(final Param param) {
            final String rtn;
            if (!_mcdMarkMap.isEmpty() && !maxCd.equals(_mcdMarkMap.firstKey())) {
                rtn = StringUtils.defaultString(_mcdMarkMap.get(_mcdMarkMap.firstKey()));
            } else if (!_lcdMarkMap.isEmpty() && !maxCd.equals(_lcdMarkMap.firstKey())) {
                rtn = StringUtils.defaultString(_lcdMarkMap.get(_lcdMarkMap.firstKey()));
            } else {
                if (_diCdMarkMap.containsKey(LATE) && _diCdMarkMap.containsKey(EARLY)) {
                    rtn = param._nameMstC00801Abbv1;
                } else if (!_diCdMarkMap.isEmpty()) {
                    rtn = _diCdMarkMap.get(_diCdMarkMap.firstKey());
                } else {
                    rtn = "";
                }
            }
            return rtn;
        }
    }

    /**
     * 勤怠コードごとの累積
     */
    private static class DiCdAccumulate {
        final Map _diListMap = new HashMap();
        public int count(final Integer diCd) {
            return getMappedList(_diListMap, diCd).size();
        }
    }

    private static class AttendSemesDat {
        int _lesson;
        int _suspend;
        int _koudome;
        int _virus;
        int _mourning;
        int _sick;
        int _notice;
        int _nonotice;
        int _late;
        int _early;
        int _kekkaJisu;
        private void add(final AttendSemesDat semes) {
            _lesson += semes._lesson;
            _suspend += semes._suspend;
            _koudome += semes._koudome;
            _virus += semes._virus;
            _mourning += semes._mourning;
            _sick += semes._sick;
            _notice += semes._notice;
            _nonotice += semes._nonotice;
            _late += semes._late;
            _early += semes._early;
            _kekkaJisu += semes._kekkaJisu;
        }
        private int shuttei() {
            return _suspend + _virus + _koudome;
        }
        private int suspendMourning() {
            return shuttei() + _mourning;
        }
        private int jugyoNissu() {
            return _lesson - suspendMourning();
        }
        private Integer getSickNoticeNonotice(final Integer dicd) {
            if (dicdSick.equals(dicd)) {
                return new Integer(_sick);
            } else if (dicdNotice.equals(dicd)) {
                return new Integer(_notice);
            } else if (dicdNonotice.equals(dicd)) {
                return new Integer(_nonotice);
            }
            return null;
        }

    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _ctrlYear;
        final String _ctrlSemester;
        final String _hrClassType;
        final String _gakunenKongou;
        final String _gradeHrclass;
        final String _targetMonth;
        final String _targetSemes;
        final String _targetDay;
        final String _ctrlDate;
        final String _useVirus;
        final String _useKoudome;
        final String _restrictFlg;
        final String _printLogStaffcd;
        final String _formSelect; // 15行フォーム
        final String _formDiv;
        final String _knjs342FORM_DIV;
        final String _gradeHrclassSchoolkind;
        final String _C001;

        final String _semestername;
        final String _hrname;
        final String _staffname;
        final boolean _seirekiFlg;
        final String _z010Name1;
        final TreeMap<String, PrintDay> _dates = new TreeMap();
        final Map _schoolKindDates = new HashMap();
        final Map<Integer, String> _nameMstC001Name1;
        final Map<Integer, String> _nameMstC001Abbv1;
        final String[] _nameMstC001Abbv1Remark;
        final Map<Integer, String> _nameMstC006Name1;
        final Map<Integer, String> _nameMstC007Name1;
        final Map<Integer, String> _nameMstC006Abbv1;
        final Map<Integer, String> _nameMstC007Abbv1;
        final String _nameMstC00801Abbv1;
        final TreeSet<Integer> _sickNoticeNonoticeCds;

        String _monthSdate = null;
        String _monthEdate = null;
        int _attendSemesAppointedDay = 0;
        int _attendDayDatSday = 0;
        boolean _isMusashinohigashi = false;
        boolean _hasEventDatHrClassDiv;
        boolean _isFi = false;
        boolean _isGhr = false;
        boolean _isGakunenKongou = false;
        boolean _isHoutei = false;
        final boolean _isTokubetsuShien;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _hrClassType = request.getParameter("HR_CLASS_TYPE");
            _gakunenKongou = request.getParameter("GAKUNEN_KONGOU");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _gradeHrclass = request.getParameter("GRADE_HR_CLASS");
            final String[] targetMonth = StringUtils.split(request.getParameter("TARGET_MONTH"), "-");
            _targetMonth = new DecimalFormat("00").format(Integer.parseInt(targetMonth[0]));
            _targetSemes = targetMonth[1];
            _targetDay = request.getParameter("TARGET_DAY");
            _useVirus = request.getParameter("useVirus");
            _useKoudome = request.getParameter("useKoudome");
            _restrictFlg = request.getParameter("RESTRICT_FLG");
            _printLogStaffcd = request.getParameter("PRINT_LOG_STAFFCD");
            _formSelect = request.getParameter("FORM_SELECT");
            _formDiv = request.getParameter("FORM_DIV");
            _knjs342FORM_DIV = request.getParameter("knjs342FORM_DIV");

            _isTokubetsuShien = "1".equals(request.getParameter("useSpecial_Support_Hrclass"));
            if ("2".equals(_hrClassType) && "1".equals(request.getParameter("useFi_Hrclass"))) {
                _isFi = true;
            } else if ("2".equals(_hrClassType) && _isTokubetsuShien) {
                _isGhr = true;
            } else if ("1".equals(_hrClassType) && "1".equals(_gakunenKongou) && _isTokubetsuShien) {
                _isGakunenKongou = true;
            } else {
                _isHoutei = true;
            }
            log.debug(" fi? " + _isFi + ", ghr? " + _isGhr + ", gakunenKongou? " + _isGakunenKongou);

            if (_isGhr || _isGakunenKongou) {
                _C001 = "C001";
                _gradeHrclassSchoolkind = null;
            } else {
                _gradeHrclassSchoolkind = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _ctrlYear + "' AND GRADE = '" + _gradeHrclass.substring(0, 2) + "' "));
                final List cdList = KnjDbUtils.query(db2, " SELECT * FROM V_NAME_MST WHERE YEAR = '" + _ctrlYear + "' AND NAMECD1 = 'C" + _gradeHrclassSchoolkind + "01' ");
                if (cdList.isEmpty()) {
                    _C001 = "C001";
                } else {
                    _C001 = "C" + _gradeHrclassSchoolkind + "01";
                }
            }
            _semestername = getSemestername(db2);
            _hrname = getHrname(db2);
            _seirekiFlg = KNJ_EditDate.isSeireki(db2);
            setDates(db2, _dates);
            _staffname = getStaffname(db2);
            _hasEventDatHrClassDiv = KnjDbUtils.setTableColumnCheck(db2, "EVENT_DAT", "HR_CLASS_DIV");
            setEventDatHolidayFlg(db2, _dates, _schoolKindDates);
            _nameMstC001Name1 = getNameMstMap(db2, _C001, "NAME1");
            _nameMstC001Abbv1 = getNameMstMap(db2, _C001, "ABBV1");
            _nameMstC00801Abbv1 = (String) getNameMstRecord(db2, "C008", "01").get("ABBV1");
            _nameMstC001Abbv1Remark = getC001Abbv1Remark1(_nameMstC001Name1, _nameMstC001Abbv1);
            _nameMstC006Name1 = getNameMstMap(db2, "C006", "NAME1");
            _nameMstC007Name1 = getNameMstMap(db2, "C007", "NAME1");
            _nameMstC006Abbv1 = getNameMstMap(db2, "C006", "ABBV1");
            _nameMstC007Abbv1 = getNameMstMap(db2, "C007", "ABBV1");
            _sickNoticeNonoticeCds = new TreeSet();
            if (_sickNoticeNonoticeCds.size() < 2 && _nameMstC001Name1.containsKey(dicdSick)) _sickNoticeNonoticeCds.add(dicdSick);
            if (_sickNoticeNonoticeCds.size() < 2 && _nameMstC001Name1.containsKey(dicdNotice)) _sickNoticeNonoticeCds.add(dicdNotice);
            if (_sickNoticeNonoticeCds.size() < 2 && _nameMstC001Name1.containsKey(dicdNonotice)) _sickNoticeNonoticeCds.add(dicdNonotice);
            _z010Name1 = (String) getNameMstRecord(db2, "Z010", "00").get("NAME1");
            log.debug(" z010 name1 = " + _z010Name1);
            setAttendSemesDayDat(db2);
            _isMusashinohigashi = "musashinohigashi".equals(_z010Name1);
        }

        private String getString(final Map map, final Object cd) {
            return (String) map.get(cd);
        }
        private String[] getC001Abbv1Remark1(final Map nameMstC001, final Map nameMstC001Abbv1) {
            final StringBuffer stb1 = new StringBuffer();
            String comma1 = "", space = "  ";
            final Integer[] cds = {dicdMourning, dicdSuspend, dicdSick, dicdNotice, dicdNonotice, dicdLate, dicdEarly};
            for (int i = 0; i < cds.length; i++) {
                if (null != nameMstC001.get(cds[i]) && null != nameMstC001Abbv1.get(cds[i])) {
                    stb1.append(comma1).append(getString(nameMstC001, cds[i]) + "・・・" + getString(nameMstC001Abbv1, cds[i]));
                    comma1 = space;
                }
            }
            if (null != nameMstC001.get(dicdLate) && null != nameMstC001.get(dicdEarly) && null != _nameMstC00801Abbv1) {
                stb1.append(comma1).append(getString(nameMstC001, dicdLate) + "・" + getString(nameMstC001, dicdEarly) + "・・・" + _nameMstC00801Abbv1);
                comma1 = space;
            }
            if (stb1.length() > 0) {
                stb1.insert(0, "欠席：");
            }

            final StringBuffer stb2 = new StringBuffer();

            final String[] rtn = { stb1.toString(), stb2.toString() };
            return rtn;
        }

        /**
         * 日付の月の最終日付のカレンダーを得る
         * @param date
         * @return
         */
        private Calendar getLastDayOfMonth(final String date) {
            final Calendar cal = Calendar.getInstance();
            cal.setTime(java.sql.Date.valueOf(date));
            cal.add(Calendar.MONTH, 1);
            cal.add(Calendar.DAY_OF_MONTH, -1);
            return cal;
        }

        private void setDates(final DB2UDB db2, final TreeMap dates) {

            final String monthStartDay = (Integer.parseInt(_ctrlYear) + (Integer.parseInt(_targetMonth) < 4 ? 1 : 0)) + "-" + _targetMonth + "-01";
            final Calendar calSemesSdate = getCalendar(monthStartDay);
            final Calendar calSemesEdate = getLastDayOfMonth(monthStartDay);

            _monthSdate = getDateString(calSemesSdate);
            _monthEdate = getDateString(calSemesEdate);

            boolean checkSemester = false; // 指定月が学期によって分かれているか
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" WITH SEMESTER_DATES AS ( ");
                stb.append(" SELECT  T1.SEMESTER, T1.SDATE AS DATE FROM SEMESTER_MST T1 WHERE T1.YEAR = '" + _ctrlYear + "' AND T1.SEMESTER <> '9' ");
                stb.append(" UNION ALL ");
                stb.append(" SELECT  T1.SEMESTER, T1.EDATE AS DATE FROM SEMESTER_MST T1 WHERE T1.YEAR = '" + _ctrlYear + "' AND T1.SEMESTER <> '9' ");
                stb.append(" ) ");
                stb.append(" SELECT CASE WHEN MONTH(DATE) < 10 THEN '0' || CAST(MONTH(DATE) AS CHAR(1)) ");
                stb.append("             ELSE CAST(MONTH(DATE) AS CHAR(2)) ");
                stb.append("        END AS MONTH ");
                stb.append(" FROM SEMESTER_DATES ");
                stb.append(" GROUP BY MONTH(DATE) ");
                stb.append(" HAVING COUNT(*) > 1 ");
                // log.debug(" sql ="  + stb.toString());
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    if (_targetMonth.equals(rs.getString("MONTH"))) {
                        checkSemester = true;
                        break;
                    }
                }
            } catch (Exception e) {
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }

            if (checkSemester) {
                try {
                    final String sql = " SELECT "
                        + " CASE WHEN T1.SDATE < '" + _monthSdate + "' THEN '" + _monthSdate + "' ELSE T1.SDATE END AS SDATE, "
                        + " CASE WHEN T1.EDATE > '" + _monthEdate + "' THEN '" + _monthEdate + "' ELSE T1.EDATE END AS EDATE "
                        + " FROM SEMESTER_MST T1 WHERE T1.SEMESTER = '" + _targetSemes + "' AND  T1.YEAR = '" + _ctrlYear + "' ";
                    log.debug(" sql ="  + sql);
                    ps = db2.prepareStatement(sql);
                    rs = ps.executeQuery();
                    if (rs.next()) {
                        if (null != rs.getString("SDATE")) {
                            _monthSdate = rs.getString("SDATE");
                        }
                        if (null != rs.getString("EDATE")) {
                            _monthEdate = rs.getString("EDATE");
                        }
                    }
                } catch (Exception e) {
                    log.error("exception!", e);
                } finally {
                    DbUtils.closeQuietly(null, ps, rs);
                    db2.commit();
                }
            }
            calSemesSdate.setTime(java.sql.Date.valueOf(_monthSdate));
            calSemesEdate.setTime(java.sql.Date.valueOf(_monthEdate));

            final Calendar cal = Calendar.getInstance();
            cal.setTime(java.sql.Date.valueOf(monthStartDay));
            final int currentMonth = cal.get(Calendar.MONTH);
            while (cal.get(Calendar.MONTH) == currentMonth) {
                final PrintDay printDay = new PrintDay(cal);
                printDay._isPrintTargetSemester = (cal.after(calSemesSdate) || cal.equals(calSemesSdate)) && (cal.before(calSemesEdate) || cal.equals(calSemesEdate));
                dates.put(getDateString(cal), printDay);
                cal.add(Calendar.DATE, 1);
            }
        }

        private void setEventDatHolidayFlg(final DB2UDB db2, final TreeMap<String, PrintDay> datesOrigin, final Map<String, Map<String, PrintDay>> schoolKindDates) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String minDate = datesOrigin.firstKey();
                final String maxDate = datesOrigin.lastKey();

                final StringBuffer stb = new StringBuffer();
                if (_isGhr || _isGakunenKongou) {
                    stb.append(" SELECT ");
                    stb.append("   T1.SCHOOL_KIND, T1.GRADE, T1.HR_CLASS, T1.EXECUTEDATE, T1.HOLIDAY_FLG, VALUE(T1.REMARK1, '') AS REMARK1 ");
                    stb.append(" FROM ");
                    stb.append("   EVENT_MST T1 ");
                    stb.append(" WHERE ");
                    stb.append("   T1.DATA_DIV = '1' ");
                    stb.append("   AND T1.EXECUTEDATE BETWEEN '" + minDate + "' AND '" + maxDate + "' ");
                    stb.append("   AND T1.GRADE = '00' ");
                    stb.append("   AND T1.COURSECD = '0' ");
                    stb.append("   AND T1.MAJORCD = '000' ");
                    if (_hasEventDatHrClassDiv) {
                        stb.append("   AND T1.HR_CLASS_DIV = '1' ");
                    }
                    stb.append(" ORDER BY ");
                    stb.append("   T1.EXECUTEDATE ");
                } else {
                    stb.append(" SELECT ");
                    stb.append("   T2.SCHOOL_KIND, T1.GRADE, T1.HR_CLASS, T1.EXECUTEDATE, T1.HOLIDAY_FLG, VALUE(T1.REMARK1, '') AS REMARK1 ");
                    stb.append(" FROM ");
                    stb.append("   EVENT_DAT T1 ");
                    stb.append("   LEFT JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = '" + _ctrlYear + "' AND T2.GRADE = T1.GRADE ");
                    stb.append(" WHERE ");
                    stb.append("   T1.EXECUTEDATE BETWEEN '" + minDate + "' AND '" + maxDate + "' ");
                    stb.append("   AND T1.GRADE || T1.HR_CLASS = '" + _gradeHrclass + "' ");
                    if (_hasEventDatHrClassDiv) {
                        if (_isFi) {
                            stb.append("   AND T1.HR_CLASS_DIV = '2' ");
                        } else {
                            stb.append("   AND T1.HR_CLASS_DIV = '1' ");
                        }
                    }
                    stb.append(" ORDER BY ");
                    stb.append("   T1.GRADE, T1.HR_CLASS, T1.EXECUTEDATE ");
                }

                log.debug(" holiday sql = " + stb.toString());
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                final Map<String, List<String>> notHolidayHrMap = new HashMap();
                while (rs.next()) {
                    final String schoolKind = rs.getString("SCHOOL_KIND");
                    if (null == schoolKindDates.get(schoolKind)) {
                        createSchoolKindDates(datesOrigin, schoolKindDates, schoolKind);
                    }

                    final Map dates = getMappedMap(schoolKindDates, schoolKind);
                    final PrintDay printDate = (PrintDay) dates.get(rs.getString("EXECUTEDATE"));
                    if (null == printDate) {
                        log.error("PrintDate無し :" + rs.getString("EXECUTEDATE"));
                    }
                    final String holidayFlg = rs.getString("HOLIDAY_FLG");
                    final String remark1 = rs.getString("REMARK1");
                    if (!"1".equals(holidayFlg)) {
                        if (_isGhr || _isGakunenKongou) {
                        } else {
                            getMappedList(notHolidayHrMap, rs.getString("EXECUTEDATE")).add(rs.getString("GRADE") + rs.getString("HR_CLASS"));
                        }
                        if (!StringUtils.isBlank(remark1)) {
                            printDate._remark1 = remark1;
                        }
                    } else {
                        printDate._holidayFlg = holidayFlg;
                        printDate._remark1 = remark1;
                    }
                }
                for (final String schoolKind : schoolKindDates.keySet()) {
                    final Map<String, PrintDay> dates = getMappedMap(schoolKindDates, schoolKind);
                    for (final String executedate: notHolidayHrMap.keySet()) {
                        final List<String> notHolidayHrList = getMappedList(notHolidayHrMap, executedate);
                        log.debug(" check execuedate " + executedate + " = " + notHolidayHrList);
                        if (notHolidayHrList.size() > 0) {
                            final PrintDay printDate = dates.get(executedate);
                            if ("1".equals(printDate._holidayFlg)) {
                                log.debug(" 全てのクラスが休みではないので休日設定をOFF :" + notHolidayHrList);
                                printDate._holidayFlg = null;
                            }
                        }
                    }
                }
                for (final String schoolKind : schoolKindDates.keySet()) {
                    log.debug(" schoolKind = " + schoolKind + " / dates = " + getMappedMap(schoolKindDates, schoolKind));
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private Map getNameMstRecord(final DB2UDB db2, final String namecd1, final String namecd2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map rtn = new HashMap();
            try {
                final String sql = " SELECT * FROM V_NAME_MST T1 WHERE T1.NAMECD1 = '" + namecd1 + "' AND  T1.NAMECD2 = '" + namecd2 + "' AND T1.YEAR = '" + _ctrlYear + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final ResultSetMetaData meta = rs.getMetaData();
                    for (int coli = 1; coli <= meta.getColumnCount(); coli++) {
                        rtn.put(meta.getColumnName(coli), rs.getString(coli));
                    }
                }

            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }

        private Map getNameMstMap(final DB2UDB db2, final String namecd1, final String field) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map rtn = new HashMap();
            try {
                final String sqlNameMstC001 = " SELECT NAMECD2, " + field + " FROM V_NAME_MST T1 WHERE T1.NAMECD1 = '" + namecd1 + "' AND  T1.YEAR = '" + _ctrlYear + "' ";
                ps = db2.prepareStatement(sqlNameMstC001);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn.put(Integer.valueOf(rs.getString("NAMECD2")), rs.getString(field));
                }

            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }

        private String getSemestername(final DB2UDB db2) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT SEMESTERNAME FROM SEMESTER_MST T1 WHERE T1.YEAR = '" + _ctrlYear + "' AND T1.SEMESTER = '" + _targetSemes + "' "));
        }

        private String getHrname(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            String rtn = null;
            try {
                StringBuffer sql = new StringBuffer();
                if (_isFi) {
                    sql.append(" SELECT HR_NAME ");
                    sql.append(" FROM SCHREG_REGD_FI_HDAT T1 ");
                    sql.append(" WHERE T1.YEAR = '" + _ctrlYear + "' AND T1.SEMESTER = '" + _targetSemes + "' ");
                    sql.append("   AND T1.GRADE || T1.HR_CLASS = '" + _gradeHrclass + "' ");
                } else if (_isGhr) {
                    sql.append(" SELECT GHR_NAME AS HR_NAME ");
                    sql.append(" FROM SCHREG_REGD_GHR_HDAT T1 ");
                    sql.append(" WHERE T1.YEAR = '" + _ctrlYear + "' AND T1.SEMESTER = '" + _targetSemes + "' ");
                    sql.append("   AND T1.GHR_CD = '" + _gradeHrclass + "' ");
                } else if (_isGakunenKongou) {
                    sql.append(" SELECT  ");
                    sql.append("   T1.SCHOOL_KIND || '-' || T1.HR_CLASS AS VALUE, ");
                    sql.append("   MAX(T1.HR_CLASS_NAME1) AS HR_NAME ");
                    sql.append(" FROM V_STAFF_HR_DAT T1 ");
                    sql.append(" WHERE ");
                    sql.append("   T1.YEAR = '" + _ctrlYear + "' ");
                    sql.append("   AND T1.SEMESTER = '" + _targetSemes + "' ");
                    sql.append("   AND T1.SCHOOL_KIND || '-' || T1.HR_CLASS = '" + _gradeHrclass + "' ");
                    if ("1".equals(_restrictFlg)) {
                        sql.append("   AND T1.STAFFCD = '" + _printLogStaffcd + "' ");
                    }
                    sql.append(" GROUP BY ");
                    sql.append("   T1.SCHOOL_KIND || '-' || T1.HR_CLASS ");
                    sql.append(" ORDER BY ");
                    sql.append("   T1.SCHOOL_KIND || '-' || T1.HR_CLASS ");
                } else {
                    sql.append(" SELECT HR_NAME ");
                    sql.append(" FROM SCHREG_REGD_HDAT T1 ");
                    sql.append(" WHERE T1.YEAR = '" + _ctrlYear + "' AND T1.SEMESTER = '" + _targetSemes + "' ");
                    sql.append("   AND T1.GRADE || T1.HR_CLASS = '" + _gradeHrclass + "' ");
                }
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString("HR_NAME");
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }

        private String getStaffname(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            String staffname = null;
            try {
                final StringBuffer sql = new StringBuffer();
                if (_isFi) {
                    sql.append(" SELECT VALUE(T2.STAFFNAME, T3.STAFFNAME, T4.STAFFNAME) AS STAFFNAME ");
                    sql.append(" FROM SCHREG_REGD_FI_HDAT T1 ");
                    sql.append("     LEFT JOIN STAFF_MST T2 ON T2.STAFFCD = TR_CD1 ");
                    sql.append("     LEFT JOIN STAFF_MST T3 ON T3.STAFFCD = TR_CD2 ");
                    sql.append("     LEFT JOIN STAFF_MST T4 ON T4.STAFFCD = TR_CD3 ");
                    sql.append(" WHERE T1.YEAR = '" + _ctrlYear + "' AND T1.SEMESTER = '" + _targetSemes + "' ");
                    sql.append("   AND T1.GRADE || T1.HR_CLASS = '" + _gradeHrclass + "' ");
                } else if (_isGhr) {
                    sql.append(" SELECT VALUE(T2.STAFFNAME, T3.STAFFNAME, T4.STAFFNAME) AS STAFFNAME ");
                    sql.append(" FROM SCHREG_REGD_GHR_HDAT T1 ");
                    sql.append("     LEFT JOIN STAFF_MST T2 ON T2.STAFFCD = TR_CD1 ");
                    sql.append("     LEFT JOIN STAFF_MST T3 ON T3.STAFFCD = TR_CD2 ");
                    sql.append("     LEFT JOIN STAFF_MST T4 ON T4.STAFFCD = TR_CD3 ");
                    sql.append(" WHERE T1.YEAR = '" + _ctrlYear + "' AND T1.SEMESTER = '" + _targetSemes + "' ");
                    sql.append("   AND T1.GHR_CD = '" + _gradeHrclass + "' ");
                } else if (_isGakunenKongou) {
                    sql.append(" SELECT T1.STAFFCD, T2.STAFFNAME  ");
                    sql.append(" FROM V_STAFF_HR_DAT T1 ");
                    sql.append(" LEFT JOIN STAFF_MST T2 ON T2.STAFFCD = T1.STAFFCD ");
                    sql.append(" WHERE ");
                    sql.append("   T1.YEAR = '" + _ctrlYear + "' ");
                    sql.append("   AND T1.SEMESTER = '" + _targetSemes + "' ");
                    sql.append("   AND T1.SCHOOL_KIND || '-' || T1.HR_CLASS = '" + _gradeHrclass + "' ");
                    if ("1".equals(_restrictFlg)) {
                        sql.append("   AND T1.STAFFCD = '" + _printLogStaffcd + "' ");
                    }
                    sql.append(" ORDER BY ");
                    sql.append("   T1.STAFFCD ");
                } else {
                    sql.append(" SELECT VALUE(T2.STAFFNAME, T3.STAFFNAME, T4.STAFFNAME) AS STAFFNAME ");
                    sql.append(" FROM SCHREG_REGD_HDAT T1 ");
                    sql.append("     LEFT JOIN STAFF_MST T2 ON T2.STAFFCD = TR_CD1 ");
                    sql.append("     LEFT JOIN STAFF_MST T3 ON T3.STAFFCD = TR_CD2 ");
                    sql.append("     LEFT JOIN STAFF_MST T4 ON T4.STAFFCD = TR_CD3 ");
                    sql.append(" WHERE T1.YEAR = '" + _ctrlYear + "' AND T1.SEMESTER = '" + _targetSemes + "' ");
                    sql.append("   AND T1.GRADE || T1.HR_CLASS = '" + _gradeHrclass + "' ");
                }

                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    staffname = rs.getString("STAFFNAME");
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }

            try {
                if (_isGakunenKongou || _isHoutei) {
                    final StringBuffer sql = new StringBuffer();
                    sql.append(" SELECT T1.TR_DIV, T1.FROM_DATE, T1.TO_DATE, T1.STAFFCD, T2.STAFFNAME, T1.GRADE, T1.HR_CLASS ");
                    sql.append(" FROM STAFF_CLASS_HIST_DAT T1 ");
                    sql.append(" INNER JOIN STAFF_MST T2 ON T2.STAFFCD = T1.STAFFCD ");
                    sql.append(" WHERE T1.YEAR = '" + _ctrlYear + "' AND T1.SEMESTER = '" + _ctrlSemester + "' ");
                    sql.append("   AND T1.FROM_DATE <= '" + _monthSdate + "' ");
                    sql.append("   AND VALUE(T1.TO_DATE, '9999-12-31') >= '" + _monthEdate + "' ");
                    if (_isGakunenKongou) {
                        sql.append("   AND (T1.GRADE, T1.HR_CLASS) IN ( ");
                        sql.append("     SELECT T1.GRADE, T1.HR_CLASS  ");
                        sql.append("     FROM V_STAFF_HR_DAT T1 ");
                        sql.append("     LEFT JOIN STAFF_MST T2 ON T2.STAFFCD = T1.STAFFCD ");
                        sql.append("     WHERE ");
                        sql.append("       T1.YEAR = '" + _ctrlYear + "' ");
                        sql.append("       AND T1.SEMESTER = '" + _ctrlSemester + "' ");
                        sql.append("       AND T1.SCHOOL_KIND || '-' || T1.HR_CLASS = '" + _gradeHrclass + "' ");
                        if ("1".equals(_restrictFlg)) {
                            sql.append("   AND T1.STAFFCD = '" + _printLogStaffcd + "' ");
                        }
                        sql.append("   ) ");
                    } else {
                        sql.append("   AND T1.GRADE || T1.HR_CLASS = '" + _gradeHrclass + "' ");
                    }
                    sql.append(" ORDER BY T1.TR_DIV, T1.FROM_DATE, VALUE(T1.TO_DATE, '9999-12-31'), T1.STAFFCD, T1.GRADE, T1.HR_CLASS ");

                    ps = db2.prepareStatement(sql.toString());
                    rs = ps.executeQuery();

                    String rsStaffname = null;

                    String firstTrDiv = null;
                    while (rs.next()) {

                        if (null == firstTrDiv) {
                            firstTrDiv = rs.getString("TR_DIV"); // 最小のTR_DIV
                        } else {
                            if (!firstTrDiv.equals(rs.getString("TR_DIV"))) { // 最小のTR_DIVのみ処理
                                break;
                            }
                        }
                        log.debug(" trDiv = " + rs.getString("TR_DIV") + ", fromDate = " + rs.getString("FROM_DATE") + ", toDate = " + rs.getString("TO_DATE") + ", staffcd = " + rs.getString("STAFFCD") + ", (grade || hrClass = " + rs.getString("GRADE") + " || " + rs.getString("HR_CLASS") + ")");
                        rsStaffname = rs.getString("STAFFNAME");
                    }
                    if (null != rsStaffname) {
                        // STAFF_CLASS_HIST_DATがあれば優先して表示
                        staffname = rsStaffname;
                    }
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return staffname;
        }

        private void setAttendSemesDayDat(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            _attendSemesAppointedDay = 0;
            _attendDayDatSday = 0;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     CASE WHEN INT(VALUE(MAX(APPOINTED_DAY),'99')) <= " + _targetDay + " THEN INT(MAX(APPOINTED_DAY)) ELSE 0 END AS ATTEND_SEMES_APPOINTED_DAY, ");
                stb.append("     CASE WHEN INT(VALUE(MAX(APPOINTED_DAY),'99')) <= " + _targetDay + " THEN INT(MAX(APPOINTED_DAY)) + 1 ELSE 1 END AS ATTEND_DAY_DAT_SDAY ");
                stb.append("   FROM ");
                stb.append("     ATTEND_SEMES_DAT T1 ");
                stb.append("   WHERE ");
                stb.append("     T1.YEAR = '" + _ctrlYear + "' ");
                stb.append("     AND T1.MONTH = '" + _targetMonth + "' ");
                stb.append("     AND T1.SEMESTER = '" + _targetSemes + "' ");

                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    _attendSemesAppointedDay = rs.getInt("ATTEND_SEMES_APPOINTED_DAY");
                    _attendDayDatSday = rs.getInt("ATTEND_DAY_DAT_SDAY");
                }
            } catch (Exception e) {
                log.error("getSeirekiFlg Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            if (0 == _attendDayDatSday) {
                _attendDayDatSday = 1;
            }
        }
    }
}

// eof