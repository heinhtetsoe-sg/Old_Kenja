/*
 * 作成日: 2011/09/22
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJS;

import java.io.File;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_EditKinsoku;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.SvfField;
import servletpack.KNJZ.detail.SvfForm;

/**
 *  学校教育システム 賢者 [小学校プログラム] 出席簿
 *
 */
public class KNJS340 {

    private static final Log log = LogFactory.getLog(KNJS340.class);

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

    private static final String FORM_DIV3 = "3"; // A3縦/A4横（出席統計有）
    private static final String FORM_DIV4 = "4"; // A4横2（出席統計有）
    private static final String FORM_DIV5 = "5"; // A4横3（出席統計有）
    private static final String FORM_DIV6 = "6"; // A4横4（4種類）
    private static final String FORM_DIV6_1 = "6_1"; // A4横(7日以上連・断続欠席者調査あり)
    private static final String FORM_DIV6_2 = "6_2"; // A4横(7日以上連・断続欠席者調査なし)
    private static final String FORM_DIV6_3 = "6_3"; // A4横(7日以上連・断続欠席者調査あり・訪問生用)
    private static final String FORM_DIV6_4 = "6_4"; // A4横(7日以上連・断続欠席者調査なし・訪問生用)

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

            if (null != _param) {
                _param.close();
            }
            if (null != db2) {
                db2.commit();
                db2.close();
            }
            svf.VrQuit();
        }

    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {

        final StudentGroup group = StudentGroup.getStudentGroup(db2, _param);
        log.info(" student size = " + group._allStudentList.size());
        if (group._allStudentList.size() == 0) {
            return;
        }

        final Form form = new Form(_param, svf);

        form.print(db2, group);

        if (form._hasData) {
            _hasData = true;
        }
    }

    private static class Form {
        final Param _param;
        final Vrw32alp _svf;
        private boolean _hasData;
        String _currentFormname;
        Map<String, SvfField> _fieldInfoMap;
        Set _notFoundField = new TreeSet();
        final String holidayAmikakeAttribute = "Paint=(0,80,2)";

        final int MAX_STUDENT_LINE;
        String _formname;
        final Map<String, String> _createFileMap = new HashMap<String, String>();

        private Form(final Param param, final Vrw32alp svf) {
            _param = param;
            _svf = svf;

            if (FORM_DIV3.equals(_param._formDiv)) {
                if ("1".equals(_param._formSelect)) {
                    _formname = "KNJS340_9.frm";
                    MAX_STUDENT_LINE = 15;
                } else {
                    _formname = "KNJS340_10.frm";
                    MAX_STUDENT_LINE = 50;
                }
            } else if(FORM_DIV4.equals(_param._formDiv)) {
                _formname = "KNJS340_11.frm";
                MAX_STUDENT_LINE = 10;
            } else if(FORM_DIV5.equals(_param._formDiv)) {
                _formname = "KNJS340_12.frm";
                MAX_STUDENT_LINE = 10;
            } else if(FORM_DIV6.equals(_param._knjs340FORM_DIV)) {
                _formname = "KNJS340_13.frm";
                MAX_STUDENT_LINE = 10;
            } else if ("1".equals(_param._formSelect)) {
                _formname = "2".equals(_param._formDiv) ? "KNJS340_8.frm" : "KNJS340_7.frm";
                MAX_STUDENT_LINE = 15;
            } else {
                _formname = "2".equals(_param._formDiv) ? "KNJS340_6.frm" : "KNJS340_5.frm";
                MAX_STUDENT_LINE = 50;
            }
            setConfigForm(_formname);
            log.info(" form = " + _formname);
        }

        public void print(final DB2UDB db2, final StudentGroup group) {

            final TreeMap<String, PrintDay> dates = StudentGroup.getStudentDates(_param, group._allStudentList);

            Map<String, String> vacationRow = null;
            List<String> vacationDateList = null;
            for (final String schoolKind : group.getSchoolKindList()) {
                final List<Map<String, String>> list = _param._schoolKindVacationRowList.get(schoolKind);
                if (list == null) {
                    continue;
                }
                for (final Map<String, String> row : list) {
                    final String sdate = KnjDbUtils.getString(row, "SDATE");
                    final String edate = KnjDbUtils.getString(row, "EDATE");
                    if (null != sdate && null != edate) {
                        final Range vacationRange = new Range(sdate, edate);
                        final List<String> dateList = new ArrayList<String>();
                        for (final String date : dates.keySet()) {
                            if (vacationRange.contains(date)) {
                                dateList.add(date);
                            }
                        }
                        if (!dateList.isEmpty()) {
                            vacationRow = row;
                            if (null == vacationDateList) {
                                vacationDateList = new ArrayList<String>();
                                vacationDateList.addAll(dateList);
                            } else {
                                vacationDateList.retainAll(dateList);
                            }
                        }
                    }
                }
            }
            if (null == vacationDateList) {
                vacationDateList = new ArrayList<String>();
            }
            Collections.sort(vacationDateList);
            final List<LinkedList<String>> vacationRangeList = new ArrayList<LinkedList<String>>();
            for (final String date : vacationDateList) {
                boolean isAdd = false;
                for (final LinkedList<String> vacationRange : vacationRangeList) {
                    if (diffIs1day(date, vacationRange.get(0))) {
                        vacationRange.addFirst(date);
                        isAdd = true;
                    } else if (diffIs1day(vacationRange.get(vacationRange.size() - 1), date)) {
                        vacationRange.add(date);
                        isAdd = true;
                    }
                }
                if (!isAdd) {
                    final LinkedList<String> l = new LinkedList<String>();
                    l.add(date);
                    vacationRangeList.add(l);
                }
            }
            if (vacationRangeList.size() > 0) {
                vacationDateList = vacationRangeList.get(0);
            } else {
                vacationDateList = null;
            }

            int cnt = 0;
            for (final Student student : group._allStudentList) {
                cnt += 1;
                final int page = 1 + cnt / MAX_STUDENT_LINE + (cnt % MAX_STUDENT_LINE == 0 ? -1 : 0);
                final int line = cnt % MAX_STUDENT_LINE + MAX_STUDENT_LINE * (cnt % MAX_STUDENT_LINE == 0 ? 1 : 0);
                student._page = page;
                student._line = line;
            }

            final TreeMap<Integer, List<Student>> pageListMap = getPageListMap(_param, group._allStudentList, MAX_STUDENT_LINE);
            if (_param._isOutputDebug) {
                log.info(" pageListMap = " + pageListMap.size());
            }
            final int maxPage = pageListMap.lastKey().intValue();
            for (final Integer page : pageListMap.keySet()) {
                final List<Student> pageStudentList = pageListMap.get(page);

                final boolean isLastPage = page.intValue() == maxPage;

                if ((FORM_DIV4.equals(_param._formDiv) || FORM_DIV5.equals(_param._formDiv)) || FORM_DIV6_1.equals(_param._formDiv) || FORM_DIV6_3.equals(_param._formDiv) && isLastPage) {
                    final List<List<MonthAttendTopData>> attendMonthAbsencePageList = getAttendMonthAbsencePageList(group._allStudentList);

                    log.info(" attendMonthAbsencePageList size = " + attendMonthAbsencePageList.size());

                    for (int amapi = 0; amapi < Math.max(1, attendMonthAbsencePageList.size()); amapi++) {

                        setForm();

                        final List<MonthAttendTopData> printAttendMonthAbsenceList = amapi < attendMonthAbsencePageList.size() ? attendMonthAbsencePageList.get(amapi) : null;

                        printHead(pageStudentList, page.intValue(), group._allStudentList, dates, isLastPage, printAttendMonthAbsenceList);

                        printAttendSemesDat(db2, pageStudentList);

                        if (isLastPage) {
                            printTotal(group._allStudentList);
                        }

                        printDayList(pageStudentList, isLastPage, group, dates, vacationRow, vacationDateList);
                    }

                } else {

                    setForm();

                    printHead(pageStudentList, page.intValue(), group._allStudentList, dates, isLastPage, null);

                    printAttendSemesDat(db2, pageStudentList);

                    if (isLastPage) {
                        printTotal(group._allStudentList);
                    }

                    printDayList(pageStudentList, isLastPage, group, dates, vacationRow, vacationDateList);
                }
            }
        }

        private void setConfigForm(final String formname) {
            final String FLG_REMOVE_SHUSSEKISUBEKI_NINZU = "FLG_REMOVE_SHUSSEKISUBEKI_NINZU";
            final String FLG_REMOVE_SHUSSEKI_NINZU = "FLG_REMOVE_SHUSSEKI_NINZU";
            final List<String> flgs = new ArrayList<String>();
            if (_param._notPrintSyussekiSubekiNinzu) {
                // 雲雀丘の中高は出席すべき人数を表示しない
                flgs.add(FLG_REMOVE_SHUSSEKISUBEKI_NINZU);
            }
            if (_param._notPrintSyussekiNinzu) {
                flgs.add(FLG_REMOVE_SHUSSEKI_NINZU);
            }
            if (flgs.isEmpty()) {
                return;
            }
            final String key = formname + mkString(flgs, "|").toString();
            if (_createFileMap.containsKey(key)) {
                _formname = _createFileMap.get(key);
                return;
            }
            log.info(" config key = " + key);
            final String path = _svf.getPath(formname);
            if (null == path) {
                return;
            }
            final File filepath = new File(path);
            if (!filepath.exists()) {
                return;
            }
            try {
                SvfForm svfForm = new SvfForm(filepath);
                if (svfForm.readFile()) {
                    if (flgs.contains(FLG_REMOVE_SHUSSEKISUBEKI_NINZU)) {
                        for (final String mongon : Arrays.asList("出席しなければならない人数")) {
                            for (final SvfForm.KoteiMoji koteiMoji : svfForm.getKoteiMojiListWithText(mongon)) {
                                svfForm.removeKoteiMoji(koteiMoji);
                            }
                        }
                        for (final String fieldname : Arrays.asList("TOTAL_LESSON", "TOTAL_LESSON_2")) {
                            final SvfForm.Field field = svfForm.getField(fieldname);
                            if (null != field) {
                                svfForm.removeField(field);
                            }
                        }
                    }
                    if (flgs.contains(FLG_REMOVE_SHUSSEKI_NINZU)) {
                        for (final String mongon : Arrays.asList("出席人数")) {
                            for (final SvfForm.KoteiMoji koteiMoji : svfForm.getKoteiMojiListWithText(mongon)) {
                                svfForm.removeKoteiMoji(koteiMoji);
                            }
                        }
                        for (final String fieldname : Arrays.asList("TOTAL_ATTEND", "TOTAL_ATTEND_2")) {
                            final SvfForm.Field field = svfForm.getField(fieldname);
                            if (null != field) {
                                svfForm.removeField(field);
                            }
                        }
                    }
                    final File file = svfForm.writeTempFile();
                    if (file.exists()) {
                        _formname = file.getName();
                        _param._createdFiles.add(file);
                        _createFileMap.put(key, _formname);
                    }
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
        }

        private List<List<MonthAttendTopData>> getAttendMonthAbsencePageList(final List<Student> studentList) {
            final List<MonthAttendTopData> attendMonthAbsenceList = new ArrayList<MonthAttendTopData>();
            for (final Student student : studentList) {
                attendMonthAbsenceList.addAll(student._monthAttendTopDataList);
            }
            Collections.sort(attendMonthAbsenceList);
            final List<List<MonthAttendTopData>> pageList = new ArrayList<List<MonthAttendTopData>>();
            List<MonthAttendTopData> current = null;
            final int remarkSizePerPage = 4; // 1ページごとの備考の行数
            for (final MonthAttendTopData o : attendMonthAbsenceList) {
                if (null == current || current.size() >= remarkSizePerPage) {
                    current = new ArrayList<MonthAttendTopData>();
                    pageList.add(current);
                }
                current.add(o);
            }
            return pageList;
        }

        private void setForm() {
            _svf.VrSetForm(_formname, 4);
            if (null == _currentFormname || !_currentFormname.equals(_formname)) {
                _currentFormname = _formname;
                _fieldInfoMap = SvfField.getSvfFormFieldInfoMapGroupByName(_svf);
            }
        }

        private int getFieldKeta(final String field) {
            int rtn = 0;
            try {
                SvfField f = _fieldInfoMap.get(field);
                rtn = f._fieldLength;
            } catch (Throwable t) {
            }
            return rtn;
        }

        private int VrsOut(final String field, final String data) {
            if (null == field) {
                return -1;
            }
            if (!_fieldInfoMap.containsKey(field)) {
                if (_param._isOutputDebugField && !_notFoundField.contains(field)) {
                    log.warn(" no such field : " + field + " ( " + data + ")");
                    _notFoundField.add(field);
                }
            } else {
                if (_param._isOutputDebugFieldAll) {
                    log.info(" VrsOut(" + (null == field ? null : "\"" + field + "\"") + ", " + (null == data ? "null" : "\"" + data + "\"") + ")");
                }
            }
            return _svf.VrsOut(field, data);
        }

        private int VriOut(final String field, final int data) {
            if (null == field) {
                return -1;
            }
            if (!_fieldInfoMap.containsKey(field)) {
                if (_param._isOutputDebugField && !_notFoundField.contains(field)) {
                    log.warn(" no such field : " + field + " ( " + data + ")");
                    _notFoundField.add(field);
                }
            } else {
                if (_param._isOutputDebugFieldAll) {
                    log.info(" VriOut(" + (null == field ? null : "\"" + field + "\"") + ", " + data + ")");
                }
            }
            return _svf.VriOut(field, data);
        }

        private int VrAttribute(final String field, final String attribute) {
            if (null == field) {
                return -1;
            }
            if (!_fieldInfoMap.containsKey(field)) {
                if (_param._isOutputDebugField && !_notFoundField.contains(field)) {
                    log.warn(" no such field : " + field + " ( " + attribute + ")");
                    _notFoundField.add(field);
                }
            } else {
                if (_param._isOutputDebugFieldAll) {
                    log.info(" VrAttribute(" + (null == field ? null : "\"" + field + "\"") + ", " + (null == attribute ? "null" : "\"" + attribute + "\"") + ")");
                }
            }
            return _svf.VrAttribute(field, attribute);
        }

        private int VrsOutn(final String field, final int gyo, final String data) {
            if (null == field) {
                return -1;
            }
            if (!_fieldInfoMap.containsKey(field)) {
                if (_param._isOutputDebugField && !_notFoundField.contains(field)) {
                    log.warn(" no such field : " + field + " ( " + data + ")");
                    _notFoundField.add(field);
                }
            } else {
                if (_param._isOutputDebugFieldAll) {
                    log.info(" VrsOutn(" + (null == field ? null : "\"" + field + "\"") + ", " + gyo + ", " + (null == data ? "null" : "\"" + data + "\"") + ")");
                }
            }
            return _svf.VrsOutn(field, gyo, data);
        }

        /**
         * すべての生徒をページと出席番号で1ページごとに分割したリストのマップを得る。
         * @param studentList すべての生徒のリスト
         * @param splitCount 1ページに表示する生徒数
         * @return
         */
        private TreeMap<Integer, List<Student>> getPageListMap(final Param param, final List<Student> studentList, final int splitCount) {
            final TreeMap<Integer, List<Student>> pageListMap = new TreeMap<Integer, List<Student>>();
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

        private void printHead(final List<Student> studentList, final int page, final List<Student> allStudentList, final TreeMap<String, PrintDay> dates, final boolean isLastPage, final List<MonthAttendTopData> attendMonthAbsenceList) {
            if (_param._isMusashinohigashi) {
                for (int si = 0; si < studentList.size(); si++) {
                    final Student student = studentList.get(si);
                    if (null != student && NumberUtils.isDigits(student._attendno)) {
                        VrsOutn("NUMBER", si + 1, String.valueOf(Integer.parseInt(student._attendno)));
                    }
                }
            } else {
                for (int line = 1; line <= MAX_STUDENT_LINE; line++) {
                    VrsOutn("NUMBER", line, String.valueOf((page - 1) * MAX_STUDENT_LINE + line));
                }
            }
            int i = 1;
            for (final Integer dicd : _param._sickNoticeNonoticeCds) {
                VrsOut("KESSEKI_NAME" + i,  Param.getString(_param._nameMstC001Name1, dicd.toString()));
                if (FORM_DIV3.equals(_param._formDiv) || FORM_DIV4.equals(_param._formDiv) || FORM_DIV5.equals(_param._formDiv) || FORM_DIV6.equals(_param._knjs340FORM_DIV)) {
                    VrsOut("KESSEKI_NAME" + String.valueOf(i + 2),  Param.getString(_param._nameMstC001Name1, dicd.toString()));
                }
                i += 1;
            }

            for (final Student student : studentList) {
                final String field, name;
                if (_param._isGakunenKongou && "1".equals(_param._formSelect)) {
                    field = "NAME" + student._line + "_2";
                    name = StringUtils.defaultString(student._gradeName1) + " " + StringUtils.defaultString(student._name);
                } else {
                    field = "NAME" + student._line;
                    name = student._name;
                }
                VrsOut(field, name);
            }
            for (final Student student : studentList) {
                VrsOutn("TRANSFER", student._line, student.getTransferRemark(_param, "date,name", null));
            }
            VrsOut("HR_NAME", _param._hrname);
            VrsOut("TEACHER", _param._staffname);
            VrsOut("year2", _param._nendo);
            VrsOut("ymd1", _param._ctrlDateFormatted);

            if(FORM_DIV5.equals(_param._formDiv)) {
                VrsOut("TITLE", Integer.parseInt(_param._targetMonth) + "月");
            } else {
                VrsOut("TITLE", _param._semestername + "　出席簿（" + Integer.parseInt(_param._targetMonth) + "月）");
            }

            VrsOut("DATE", _param._monthRange);

            if (FORM_DIV3.equals(_param._formDiv) || FORM_DIV4.equals(_param._formDiv) || FORM_DIV5.equals(_param._formDiv) || FORM_DIV6.equals(_param._knjs340FORM_DIV)) {
                final Student student = studentList.get(0);
                String coursename = "";
                String schoolname = "";
                if (null != student) {
                    coursename = StringUtils.defaultString(student._coursename);
                    schoolname = StringUtils.defaultString(student._schoolname);
                }
                VrsOut("SUBTITLE", _param._yearMonthE + "　" + coursename + "　" + _param._hrname);
                VrsOut("SCHOOL_NAME", schoolname);
                VrsOut("TEACHER1", _param._staffname);
            }

            int minlesson = Integer.MAX_VALUE;
            int maxlesson = 0;
            int cnt = 0;
            for (final Student student : allStudentList) {
                final AttendSemesDat semes = student._attendSemesDat;
                if (semes._lesson > 0) {
                    minlesson = Math.min(minlesson, semes._lesson);
                    maxlesson = Math.max(maxlesson, semes._lesson);
                    cnt++;
                }
            }
            if (FORM_DIV3.equals(_param._formDiv)) {
                // 授業日数
                VrsOut("TOTAL_PRESENT", String.valueOf(maxlesson));
                if (null != dates && !dates.isEmpty()) {
                    final PrintDay firstDayOfMonth = dates.get(dates.firstKey());
                    if (_param._isOutputDebug) {
                        log.info(" firstDayOfMonth = " + firstDayOfMonth);
                    }
                    final List<Student> zaisekiList = new ArrayList<Student>();
                    final List<Student> shussekiList = new ArrayList<Student>();
                    final List<Student> allKessekiList = new ArrayList<Student>();
                    final List<String> tennyugakuList = new ArrayList<String>();
                    final List<String> tentaigakuList = new ArrayList<String>();
                    for (final Student student : allStudentList) {
                        if (student.isZaiseki(firstDayOfMonth._date) == Boolean.TRUE) {
                        //if (student.isZaiseki(lastDayOfMonth._date) == Boolean.TRUE) {
                            zaisekiList.add(student);
                        }
                        boolean isShusseki = false; // 1日以上出席がある
                        boolean isAllKesseki = true; // 全て欠席
                        String tennyuEntDate = null; // 転入日付
                        String tentaiGrdDate = null; // 転退学日付
                        final List tennyuEntDateList = student.getTennyuEntDateList();
                        final List tentaiGrdDateList = student.getTentaiGrdDateList();
                        if (dates.size() == 0) {
                            isAllKesseki = false;
                        }
                        boolean hasSubeki = false;
                        for (final String date : dates.keySet()) {
                            final PrintDay printDate = dates.get(date);
                            if (!"1".equals(printDate._holidayFlg)) {
                                hasSubeki = true;
                            }
                            if (!isShusseki && !"1".equals(printDate._holidayFlg) && student.isShusseki(date) && !student.isKyugakuKesseki(_param, date)) {
                                isShusseki = true;
                            }
                            if (isAllKesseki && !"1".equals(printDate._holidayFlg) && !student.isKesseki(date) && !student.isKyugakuKesseki(_param, date)) {
                                isAllKesseki = false;
                            }
                            if (null == tennyuEntDate && tennyuEntDateList.contains(date)) {
                                tennyuEntDate = date;
                            }
                            if (null == tentaiGrdDate && tentaiGrdDateList.contains(date)) {
                                tentaiGrdDate = date;
                            }
                        }
                        if (isShusseki) {
                            shussekiList.add(student);
                        }
                        if (hasSubeki && isAllKesseki) {
                            allKessekiList.add(student);
                        }
                        if (null != tennyuEntDate) {
                            tennyugakuList.add(student._schregno + "(" + tennyuEntDate + ")");
                        }
                        if (null != tentaiGrdDate) {
                            tentaigakuList.add(student._schregno + "(" + tentaiGrdDate + ")");
                        }
                    }
                    // 在籍児童生徒数 (月末)
                    VrsOut("NUM1", String.valueOf(zaisekiList.size()));
                    // 出席児童生徒
                    VrsOut("NUM2", String.valueOf(shussekiList.size()));
                    // 全欠児童生徒
                    VrsOut("NUM3", String.valueOf(allKessekiList.size()));
                    // 転入学児童生徒
                    VrsOut("NUM4", String.valueOf(tennyugakuList.size()));
                    // 転退学児童生徒
                    VrsOut("NUM5", String.valueOf(tentaigakuList.size()));
                }
            } else if (FORM_DIV4.equals(_param._formDiv) || FORM_DIV5.equals(_param._formDiv) || FORM_DIV6.equals(_param._knjs340FORM_DIV)) {
                // 印鑑
                int stmpidx =  FORM_DIV6.equals(_param._knjs340FORM_DIV) ? 5 : 1 ;
                for (final String kstr : _param._stampMap.keySet()) {
                    final StampData sdat = _param._stampMap.get(kstr);
                    final int nlen = KNJ_EditEdit.getMS932ByteLength(sdat._title);
                    final String nfield = nlen > 8 ? "2" : "1";
                    VrsOut("JOB"+stmpidx+"_"+nfield, sdat._title);
                    if (sdat._fileName != null && !"".equals(sdat._fileName)) {
                        String pictStr = _param.getStampImageFile(sdat._fileName);
                        VrsOut("STAMP"+stmpidx, pictStr);
                    } else {
                        String pictStr = _param.getStampImageFile("KNJD615_keninwaku2");
                        VrsOut("STAMP"+stmpidx, pictStr);
                    }
                    if(FORM_DIV6.equals(_param._knjs340FORM_DIV)) {
                        stmpidx--;
                    }else {
                        stmpidx++;
                    }
                }
                // 授業日数
                if (isLastPage) {
                    VrsOut("TOTAL_PRESENT", String.valueOf(maxlesson));
                }
                String fstDayOfLastMonth = null;
                String lastDayOfLastMonth = null;
                if (null != dates && !dates.isEmpty()) {
                    final PrintDay fstDayOfMonth = dates.get(dates.firstKey());
                    final PrintDay lastDayOfMonth = dates.get(dates.lastKey());
                    if (_param._isOutputDebug) {
                        log.info(" fstDayOfMonth = " + fstDayOfMonth);
                        log.info(" lastDayOfMonth = " + lastDayOfMonth);
                    }
                    final List lastMonthLeave = new ArrayList();
                    if ("04".equals(_param._targetMonth)) {
                        final String slash = _param.getImageFile("slash_bs", "jpg");
                        if (null != slash) {
                            VrsOut("SLASH", slash);
                        }
                    } else {
                        final Calendar calLastDayOfLastMonth = Calendar.getInstance();
                        calLastDayOfLastMonth.setTime(Date.valueOf(fstDayOfMonth._date));
                        calLastDayOfLastMonth.add(Calendar.DAY_OF_MONTH, -1);
                        final Calendar calfirstDayOfLastMonth = Calendar.getInstance();
                        calfirstDayOfLastMonth.setTime(calLastDayOfLastMonth.getTime());
                        calfirstDayOfLastMonth.set(Calendar.DAY_OF_MONTH, 1);
                        final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                        fstDayOfLastMonth = df.format(calfirstDayOfLastMonth.getTime());
                        lastDayOfLastMonth = df.format(calLastDayOfLastMonth.getTime());
                        for (final Student student : allStudentList) {
                            final List tengakuGrdDateList = student.getGrdDateList("3", fstDayOfLastMonth, lastDayOfLastMonth);
                            final List taigakuGrdDateList = student.getGrdDateList("2", fstDayOfLastMonth, lastDayOfLastMonth);
                            final List leaveGrdDateList = student.getLeaveGrdDateList(fstDayOfLastMonth, lastDayOfLastMonth);
                            if (!tengakuGrdDateList.isEmpty() || !taigakuGrdDateList.isEmpty() || !leaveGrdDateList.isEmpty()) {
                                lastMonthLeave.add(student._schregno + " " + tengakuGrdDateList.toString() + " " + taigakuGrdDateList.toString() + " " + leaveGrdDateList.toString());
                            }
                        }
                    }

                    final List zengetsuZaisekiList = new ArrayList();
                    final List hennyugakuList = new ArrayList();
                    final List tennyugakuList = new ArrayList();
                    final List tengakuList = new ArrayList();
                    final List taigakuList = new ArrayList();
                    final List leaveList = new ArrayList();
                    final List zaisekiList = new ArrayList();
                    //当年度の生徒がallStudentListに登録されている。
                    for (final Student student : allStudentList) {
                        if (!"04".equals(_param._targetMonth)) {
                            if (student.isZaiseki(lastDayOfLastMonth) == Boolean.TRUE) {
                                zengetsuZaisekiList.add(student);
                            }
                        }
                        //指定月に在籍しているか()、チェック
                        if (student.isZaiseki(lastDayOfMonth._date) == Boolean.TRUE) {
                            zaisekiList.add(student);
                        }
                        String tennyuEntDate = null;  // 転入日付
                        String hennyuEntDate = null;  // 編入日付
                        String tengakuGrdDate = null; // 転学日付
                        String taigakuGrdDate = null; // 退学日付
                        String leaveGrdDate = null;   // 退去日付
                        final List tennyuEntDateList = student.getEntDateList("4", fstDayOfMonth._date, lastDayOfMonth._date);
                        final List hennyuEntDateList = student.getEntDateList("5", fstDayOfMonth._date, lastDayOfMonth._date);
                        final List tengakuGrdDateList = student.getGrdDateList("3", fstDayOfMonth._date, lastDayOfMonth._date);
                        final List taigakuGrdDateList = student.getGrdDateList("2", fstDayOfMonth._date, lastDayOfMonth._date);
                        final List leaveGrdDateList = student.getLeaveGrdDateList(fstDayOfMonth._date, lastDayOfMonth._date);
                        for (final String date : dates.keySet()) {
                            if (null == tennyuEntDate && tennyuEntDateList.contains(date)) {
                                tennyuEntDate = date;
                            }
                            if (null == hennyuEntDate && hennyuEntDateList.contains(date)) {
                                hennyuEntDate = date;
                            }
                            if (null == tengakuGrdDate && tengakuGrdDateList.contains(date)) {
                                tengakuGrdDate = date;
                            }
                            if (null == taigakuGrdDate && taigakuGrdDateList.contains(date)) {
                                taigakuGrdDate = date;
                            }
                            if (null == leaveGrdDate && leaveGrdDateList.contains(date)) {
                                leaveGrdDate = date;
                            }
                        }
                        if (null != tennyuEntDate) {
                            tennyugakuList.add(student._schregno + "(" + tennyuEntDate + ")");
                        }
                        if (null != hennyuEntDate) {
                            hennyugakuList.add(student._schregno + "(" + hennyuEntDate + ")");
                        }
                        if (null != tengakuGrdDate) {
                            tengakuList.add(student._schregno + "(" + tengakuGrdDate + ")");
                        }
                        if (null != taigakuGrdDate) {
                            taigakuList.add(student._schregno + "(" + taigakuGrdDate + ")");
                        }
                        if (null != leaveGrdDate) {
                            leaveList.add(student._schregno + "(" + leaveGrdDate + ")");
                        }
                    }
                    if (isLastPage) {
                        if (!"04".equals(_param._targetMonth)) {
                            // 前月末在籍者数 去った者の数
                            VrsOut("NUM7", String.valueOf(lastMonthLeave.size()));
                            // 前月末在籍者数 実人員
                            VrsOut("NUM8", String.valueOf(zengetsuZaisekiList.size()));
                        }
                        // 編入者数(異動者数)
                        VrsOut("NUM1", String.valueOf(hennyugakuList.size()));
                        // 転入者数(異動者数)
                        VrsOut("NUM2", String.valueOf(tennyugakuList.size()));
                        // 転学者数(異動者数)
                        VrsOut("NUM3", String.valueOf(tengakuList.size()));
                        // 退学者等の数(異動者数)
                        VrsOut("NUM4", String.valueOf(taigakuList.size()));
                        // 去った者の数(月末在籍者数)
                        VrsOut("NUM5", String.valueOf(leaveList.size()));
                        // 実人員(月末在籍者数)
                        VrsOut("NUM6", String.valueOf(zaisekiList.size()));
                        // 出席延べ日数、欠席延べ日数
                        printDefferedTotalList(allStudentList, dates);

                        if(FORM_DIV6_2.equals(_param._formDiv) || FORM_DIV6_4.equals(_param._formDiv)) {
                            VrsOut("BLANK", _param._whiteSpaceImagePath);
                        }

                        else if (null != attendMonthAbsenceList) {
                            //7日以上連・断続欠席者調査
                            printContAbsenceList(attendMonthAbsenceList);
                        }
                    }
                }
            } else {
                if (minlesson > 0 && cnt > 0) {
                    VrsOut("TOTAL_PRESENT", minlesson == maxlesson ? String.valueOf(maxlesson) : String.valueOf(minlesson) + "～" + String.valueOf(maxlesson));
                } else {
                    VrsOut("TOTAL_PRESENT", "");
                }
            }

            if (!( FORM_DIV4.equals(_param._formDiv) || FORM_DIV5.equals(_param._formDiv) || FORM_DIV6.equals(_param._knjs340FORM_DIV))) {
                for (int j = 0; j < _param._nameMstC001Abbv1Remark.length; j++) {
                    VrsOut("ATTEND_MARK" + String.valueOf(j + 1), _param._nameMstC001Abbv1Remark[j]);
                }
            }
        }

        /**
         * 生徒ごとの合計・出欠備考・クラスの合計を出力
         * @param svf
         * @param studentList
         * @param total 累積
         */
        private void printAttendSemesDat(final DB2UDB db2, final List<Student> studentList) {
            final List<String> migishitaRemarkList = new ArrayList<String>();
            final int remarkField1Keta = getFieldKeta("REMARK1");
            for (final Student student : studentList) {
                final AttendSemesDat semes = student._attendSemesDat;

                int absence = 0;
                final int line = student._line;
                VrsOut("ALL_LESSON" + line, String.valueOf(semes._lesson));
                if (FORM_DIV4.equals(_param._formDiv) || FORM_DIV5.equals(_param._formDiv) || FORM_DIV6.equals(_param._knjs340FORM_DIV)) {
                    VrsOut("SUSPEND" + line, String.valueOf(semes._suspend));
                    VrsOut("MOURNING" + line, String.valueOf(semes._mourning + semes._koudome + semes._virus));
                } else {
                    VrsOut("SUSPEND" + line, String.valueOf(semes.suspendMourning()));
                }
                VrsOut("LESSON" + line, String.valueOf(semes.jugyoJisu()));
                int i = 2;
                for (final Iterator<Integer> it = _param._sickNoticeNonoticeCds.iterator(); it.hasNext() && i <= 4;) {
                    i += 1;
                    final Integer dicd = it.next();
                    final Integer count = semes.getSickNoticeNonotice(dicd);
                    VrsOut("KESSEKI" + i + "_" + line, String.valueOf(count));
                    absence += count.intValue();
                }
                VrsOut("PRESENT" + line, String.valueOf(semes._lesson));
                VrsOut("ACSENCE" + line, String.valueOf(absence));
                VrsOut("ATTEND" + line, String.valueOf(semes.jugyoJisu() - absence));
                VrsOut("TOTAL_LATE" + line, String.valueOf(semes._late));
                VrsOut("LEAVE" + line, String.valueOf(semes._early));
                VrsOut("KEKKA" + line, String.valueOf(semes._kekkaJisu));

                if (FORM_DIV3.equals(_param._formDiv)) {
                    final String remark = student.getAttendRemark(db2, _param);
                    if (!StringUtils.isBlank(remark)) {
                        migishitaRemarkList.addAll(KNJ_EditKinsoku.getTokenList(StringUtils.defaultString(student._name) + "：" + remark, remarkField1Keta));
                    }
                } else if (FORM_DIV4.equals(_param._formDiv) || FORM_DIV5.equals(_param._formDiv) || FORM_DIV6.equals(_param._knjs340FORM_DIV)) {
                    final String remark = student.getTransferRemark(_param, "date,name", null);
                    if (!StringUtils.isBlank(remark)) {
                        migishitaRemarkList.addAll(KNJ_EditKinsoku.getTokenList(StringUtils.defaultString(student._name) + "：" + remark, remarkField1Keta));
                    }
                    VrsOut("ST_REMARK" + line, student.getAttendRemark(db2, _param));
                } else {
                    // 生徒毎の右の出欠の備考
                    final String remarkN;
                    if (_param._isHibarigaoka) {
                        remarkN = student._absenceRemarkHibari;
                    } else {
                        remarkN = student.getAttendRemark(db2, _param);
                    }
                    if (_param._isOutputDebug) {
                        log.info(" remark " + student._schregno + " (" + student._attendno + ") = " + remarkN);
                    }
                    VrsOut("REMARK" + line, remarkN);
                }
                // 訪問生の出席日数
                if (FORM_DIV6_3.equals(_param._formDiv) || FORM_DIV6_4.equals(_param._formDiv)) {
                    final int cnt = student.houmonSyussekiCnt();
                    VrsOut("ATTEND" + line, String.valueOf(cnt));
                }
            }
            if (FORM_DIV3.equals(_param._formDiv) || FORM_DIV4.equals(_param._formDiv) || FORM_DIV5.equals(_param._formDiv) || FORM_DIV6.equals(_param._knjs340FORM_DIV)) {
                // フォーム右下の備考
                for (int i = 0; i < migishitaRemarkList.size(); i++) {
                    final String gyo = String.valueOf(i + 1);
                    VrsOut("REMARK" + gyo, migishitaRemarkList.get(i));
                }
            }
        }

        /**
         * ATTEND_SEMES_DAT縦計（下部表示）
         * @param svf
         * @param studentList
         */
        private void printTotal(final List<Student> studentList) {

            final AttendSemesDat total = new AttendSemesDat();
            for (final Student student : studentList) {
                total.add(student._attendSemesDat);
            }

            //        final int absence = total._sick + total._notice + total._nonotice;
            int absence = 0;
            int sline = 51;
            VrsOut("SUSPEND" + sline, String.valueOf(total.suspendMourning()));
            sline += 1;
            if (!_param._notPrintSyussekiSubekiNinzu) {
                VrsOut("LESSON" + sline, String.valueOf(total.jugyoJisu()));
            }
            sline += 1;
            int i = 3;
            for (final Integer dicd : _param._sickNoticeNonoticeCds) {
                final Integer totalSickNoticeNonotice = total.getSickNoticeNonotice(dicd);
                VrsOut("KESSEKI" + i + "_" + sline, String.valueOf(totalSickNoticeNonotice));
                absence += totalSickNoticeNonotice.intValue();
                i += 1;
                sline += 1;
            }
            for (int j = i; j <= 4; j++) {
                sline += 1;
            }
            VrsOut("ACSENCE" + sline, String.valueOf(absence));
            sline += 1;
            if (!_param._notPrintSyussekiNinzu) {
                VrsOut("ATTEND" + sline, String.valueOf(total.jugyoJisu() - absence));
            }
            sline += 1;
            VrsOut("TOTAL_LATE" + sline, String.valueOf(total._late));
            sline += 1;
            VrsOut("LEAVE" + sline, String.valueOf(total._early));
        }

        /**
         * 日付ごとの勤怠を出力(レコード)
         * @param svf
         * @param studentList
         * @param isLastPage
         * @param allStudentList
         */
        private void printDayList(final List<Student> studentList, final boolean isLastPage, final StudentGroup group, final Map<String, PrintDay> dates, final Map<String, String> vacationRow, final List<String> vacationDateList) {
            final Map<String, String> vacationNameMap = new TreeMap<String, String>();
            if (null != vacationDateList) {
                log.info(" vacationRangeList = " + vacationDateList + ", row = " + vacationRow);
                String name = StringUtils.defaultString(KnjDbUtils.getString(vacationRow, "NAME"));
                final String lline = StringUtils.repeat("-", (vacationDateList.size() - name.length()) / 2);
                final String rline = StringUtils.repeat("-", vacationDateList.size() - (lline + name).length());
                name = lline + name + rline;
                for (int i = 0; i < Math.min(name.length(), vacationDateList.size()); i++) {
                    vacationNameMap.put(vacationDateList.get(i), name.substring(i, i + 1));
                }
            }

            int dayLine = 0;
            for (final String date : dates.keySet()) { // 日付ごとにレコード出力
                final PrintDay printDay = dates.get(date);
                dayLine += 1;
                VrsOut("DAY", printDay._dayOfMonth);
                VrsOut("WEEKDAY", StringUtils.defaultString(vacationNameMap.get(date), youbi(date, dates, group)));
                if (FORM_DIV5.equals(_param._formDiv)) {
                    final String grp;
                    if (null != vacationDateList && vacationDateList.contains(date)) {
                        grp = "VAC";
                    } else {
                        grp = String.valueOf(printDay._dayOfMonth);
                    }
                    VrsOut("WEEKDAY_GRP", grp);
                }

                final boolean isAmikakeTargetDay = "1".equals(printDay._holidayFlg) || !printDay._isPrintTargetSemester;

                Boolean isPrintAmikakeFooter = null;
                for (final Student student : group._allStudentList) {
                    if (student.isZaiseki(date) == Boolean.FALSE) {
                    } else if (student._isHoumonsei) {
                        if (getHoumonseiStudentIsAmikake(date, student) == Boolean.FALSE) {
                            isPrintAmikakeFooter = Boolean.FALSE;
                            break;
                        }
                    }
                }

                final boolean eventNameFieldIsUpper = "1".equals(_param._formSelect) || FORM_DIV3.equals(_param._formDiv) || FORM_DIV4.equals(_param._formDiv) || FORM_DIV5.equals(_param._formDiv) || FORM_DIV6.equals(_param._knjs340FORM_DIV);
                if (eventNameFieldIsUpper) {
                    // 日付ごとの備考はフォームの上に表示
                    if (null != printDay._remark1) {
                        String remark = printDay._remark1;
                        final String field;
                        if (printDay._remark1.length() <= 8) {
                            field = "EVENT_NAME";
                        } else if (printDay._remark1.length() <= 10) {
                            field = "EVENT_NAME_2";
                        } else {
                            if (printDay._remark1.length() > 16) {
                                remark = printDay._remark1.substring(0, 16);
                            }
                            field = "EVENT_NAME_3";
                        }
                        VrsOut(field, remark);
                    }
                }

                printMark(studentList, date, isAmikakeTargetDay);

                if (!_param._isGakunenKongou && isAmikakeTargetDay && Boolean.FALSE != isPrintAmikakeFooter) {
                    final String[] totalfields = {"TOTAL_SUSPEND", "TOTAL_LESSON", "TOTAL_KESSEKI1_1", "TOTAL_KESSEKI2_1", "TOTAL_ABSENCE", "TOTAL_ATTEND", "TOTAL_LATE", "TOTAL_EARLY"};
                    if (eventNameFieldIsUpper) {
                        // 日付ごとの備考はフォームの上に表示、下は網掛け
                        for (int i = 0; i < totalfields.length; i++) {
                            VrAttribute(totalfields[i], holidayAmikakeAttribute);
                        }
                    } else {
                        if (StringUtils.isBlank(printDay._remark1)) {
                            // 備考がなければ網掛け
                            for (int i = 0; i < totalfields.length; i++) {
                                VrAttribute(totalfields[i], holidayAmikakeAttribute);
                            }
                        } else {
                            // 備考表示
                            if (printDay._remark1.length() > totalfields.length) {
                                // フィールド切り替え
                                int splitlen = 2;
                                int splitedlen = printDay._remark1.length() / splitlen + (printDay._remark1.length() % splitlen == 0 ? 0 : 1);
                                for (int i = 0; i < Math.min(totalfields.length, splitedlen); i++) {
                                    VrsOut("EVENT_NAME1_" + String.valueOf(i + 1), String.valueOf(printDay._remark1.substring(i * splitlen, Math.min((i + 1) * splitlen, printDay._remark1.length()))));
                                }
                            } else {
                                for (int i = 0; i < Math.min(totalfields.length, printDay._remark1.length()); i++) {
                                    VrsOut(totalfields[i], String.valueOf(printDay._remark1.charAt(i)));
                                }
                            }
                        }
                    }
                }

                if (isLastPage) {
                    if (getCalendar(date).get(Calendar.DATE) > Integer.parseInt(_param._targetDay) || !_param._isGakunenKongou && isAmikakeTargetDay && Boolean.FALSE != isPrintAmikakeFooter) {
                        // 表示しない
                    } else {
                        final DiCdAccumulate diCdAcc = new DiCdAccumulate();
                        int regdCount = 0;
                        int kyugakuKesseki = 0;
                        for (final Student student : group._allStudentList) {

                            final boolean isZaiseki = Boolean.FALSE == student.isZaiseki(date) ? false : true;
                            final boolean isSubeki;
                            if (student._isHoumonsei && !student._eventSchregDatList.isEmpty()) {
                                isSubeki = !student._eventSchregDatHolidayList.contains(date);
                            } else if (isAmikakeTargetDay) {
                                isSubeki = false;
                            } else {
                                isSubeki = isZaiseki && !isAmikakeTargetDay;
                            }

                            //log.debug(" " + date + ": " + i + " student " + student._schregno + " : " + student._attendno + "[" + student._entDate + ", " + student._grdDate + "] regd = " + between);
                            if (isSubeki) {
                                regdCount += 1;
                                final AttendDayDat attendDayDat = student._attendDayDatMap.get(date);
                                if (null != attendDayDat) {
                                    for (final Integer diCd : attendDayDat._diCdMarkMap.keySet()) {
                                        getMappedList(diCdAcc._diListMap, diCd).add(attendDayDat._schregno);
                                    }
                                }
                                if (student.isKyugakuKesseki(_param, date)) {
                                    kyugakuKesseki += 1;
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
                        final int late = diCdAcc.count(dicdLate) + diCdAcc.count(dicdLate2) + diCdAcc.count(dicdLate3);
                        final int early = diCdAcc.count(dicdEarly);

                        VriOut("TOTAL_SUSPEND", suspendMourning);
                        VriOut("TOTAL_LESSON", studentsRegdCount - suspendMourning);

                        int i = 0;
                        if (i <= 2 && _param._sickNoticeNonoticeCds.contains(dicdSick)) {
                            i += 1;
                            VriOut("TOTAL_KESSEKI" + i + "_1", sick);
                            absence += sick;
                        }
                        if (i <= 2 && _param._sickNoticeNonoticeCds.contains(dicdNotice)) {
                            i += 1;
                            VriOut("TOTAL_KESSEKI" + i + "_1", notice);
                            absence += notice;
                        }
                        if (i <= 2 && _param._sickNoticeNonoticeCds.contains(dicdNonotice)) {
                            i += 1;
                            VriOut("TOTAL_KESSEKI" + i + "_1", nonotice);
                            absence += nonotice;
                        }
                        VriOut("TOTAL_ABSENCE", kyugakuKesseki + absence);
                        VriOut("TOTAL_ATTEND", studentsRegdCount - kyugakuKesseki - suspendMourning - absence);
                        VriOut("TOTAL_LATE", late);
                        VriOut("TOTAL_EARLY", early);
                    }
                }
                _svf.VrEndRecord();
                _hasData = true;
            }
            for (int day = dayLine + 1; day <= MAX_DATE_LINE; day++) {
                VrsOut("DAY2", String.valueOf(day));
                VrAttribute("DAY2", "X=10000");
                _svf.VrEndRecord();
            }
        }

        private void printMark(final List<Student> studentList, final String date, final boolean isAmikakeTargetDayDefault) {
            int maxStudentLine = 0;
            for (final Student student : studentList) {
                final String field = "DI_REMARK" + student._line;
                final String entGrdField = "CHANGE" + student._line; // FORM_DIV3.equals(_param._formDiv)
                final String entGrdFieldMark = "CHANGE_MARK" + student._line; // FORM_DIV3.equals(_param._formDiv)
                final String entGrdDate = student.getTransferRemark(_param, "date", date);
                final String entGrdName = student.getTransferRemark(_param, "name", date);

                boolean isNotZaiseki = false;
                Boolean isAmikake = null;
                if (student.isZaiseki(date) == Boolean.FALSE) {
                    isAmikake = Boolean.TRUE;
                    isNotZaiseki = true;
                } else if (student._isHoumonsei) {
                    isAmikake = getHoumonseiStudentIsAmikake(date, student);
                }
                if (_param._isOutputDebug) {
                    if (FORM_DIV3.equals(_param._formDiv) || FORM_DIV4.equals(_param._formDiv) || FORM_DIV5.equals(_param._formDiv) || FORM_DIV6.equals(_param._knjs340FORM_DIV)) {
                        if (isNotZaiseki) {
                            log.info(" notZaiseki [" + date + "] " + student.toString());
                        }
                    }
                }
                if (null == isAmikake) {
                    if (isAmikakeTargetDayDefault) {
                        isAmikake = Boolean.TRUE; // 基本は網掛け
                    } else {
                        isAmikake = Boolean.FALSE; // 基本は網掛け無し
                    }
                }
                if (FORM_DIV3.equals(_param._formDiv) || FORM_DIV4.equals(_param._formDiv) || FORM_DIV5.equals(_param._formDiv) || FORM_DIV6.equals(_param._knjs340FORM_DIV)) {
                    if (isNotZaiseki) {
                        VrAttribute(entGrdField, "UnderLine=(0,3,5),Keta=4");
                    }
                    String diMark = "";
                    if (isAmikake.booleanValue()) {
                        VrAttribute(field, holidayAmikakeAttribute);
                    } else {
                        final AttendDayDat attendDayDat = student._attendDayDatMap.get(date);
                        if (null != attendDayDat) {
                            if(attendDayDat._diCdMarkMap.containsKey(0)) {
                                if(FORM_DIV6_3.equals(_param._formDiv) || FORM_DIV6_4.equals(_param._formDiv)) {
                                    diMark = attendDayDat.getDiMark(_param);
                                }
                            }else {
                                diMark = attendDayDat.getDiMark(_param);
                            }
                        }
                    }
                    boolean print = false;
                    if (null != entGrdDate) {
                        //log.info(" entGrdDate " + student._schregno + " " + entGrdDate + ", " + entGrdName);
                        if (entGrdDate.equals(date)) {
                            if (FORM_DIV4.equals(_param._formDiv) || FORM_DIV5.equals(_param._formDiv) || FORM_DIV6.equals(_param._knjs340FORM_DIV)) {
                                VrsOut(field, entGrdName); // 縦書き
                                print = true;
                            } else {
                                VrsOut(entGrdField, entGrdName);
                                final String mark;
                                if (StringUtils.isBlank(diMark) && !isAmikake.booleanValue()) {
                                    mark = "◎";
                                } else {
                                    mark = diMark;
                                }
                                VrsOut(entGrdFieldMark, mark);
                                print = true;
                            }
                        }
                    }
                    if (false == print) {

                        VrsOut(field, diMark);
                    }
                } else if (isAmikake.booleanValue()) {
                    VrAttribute(field, holidayAmikakeAttribute);
                } else {
                    String diMark = "";
                    final AttendDayDat attendDayDat = student._attendDayDatMap.get(date);
                    if (null != attendDayDat) {
                        diMark = attendDayDat.getDiMark(_param);
                    }
                    VrsOut(field, diMark);
                }

                maxStudentLine = student._line;
            }
            if (isAmikakeTargetDayDefault) {
                for (int line = maxStudentLine + 1; line <= MAX_STUDENT_LINE; line++) {
                    VrAttribute("DI_REMARK" + line, holidayAmikakeAttribute);
                }
            }
        }

        private Boolean getHoumonseiStudentIsAmikake(final String date, final Student student) {
            Boolean isAmikake;
            // 訪問生
//                    if (!student._eventSchregDatList.contains(date)) {
//                        // 時間割がない -> 基本による
//                        isAmikake = null;
//                    } else if (student._eventSchregDatHolidayList.contains(date)) {
//                        // 時間割があり時間割が休日 -> 休日
//                        isAmikake = Boolean.TRUE;
//                    } else {
//                        // 時間割があり時間割が休日ではない -> 登校日
//                        isAmikake = Boolean.FALSE;
//                    }
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
            return isAmikake;
        }

        private String youbi(final String targetDate, final Map<String, PrintDay> dates, final StudentGroup group) {
            if (FORM_DIV5.equals(_param._formDiv)) {
                if (group.isHeisa(targetDate)) {
                    return "臨";
                }
                for (final Student student : group._allStudentList) {
                    if ("1".equals(student._entDiv) && targetDate.equals(student._entDate)) {
                        return student._entDivName;
                    } else if ("1".equals(student._grdDiv) && targetDate.equals(student._grdDate)) {
                        return student._grdDivName;
                    }
                }
                boolean dayOffFlg = false; //休フラグ
                for (final String date : dates.keySet()) { // 日付ごとにレコード出力
                    final PrintDay printDay = dates.get(date);
                    String youbi = printDay._youbi;
                    if (dayOffFlg == true && "".equals(printDay._event)) {
                        youbi = "休";
                        dayOffFlg = false;
                    } else {
                        if (!"".equals(printDay._event)) {

                            if (!"祝".equals(printDay._event)) dayOffFlg = false;

                            if ("日".equals(youbi) && "祝".equals(printDay._event)) {
                                //日曜かつ祝日の場合、"祝"と出力
                                youbi = "祝";
                                dayOffFlg = true; //次の平日に"休"と出力
                            } else {
                                //行事が設定されている場合、行事を出力
                                youbi = printDay._event;
                            }
                        } else if (!"土".equals(youbi) && !"日".equals(youbi)) {
                            //"土","日" 以外の場合、空白を出力
                            youbi = "";
                        }
                    }
                    if (targetDate.equals(date)) {
                        return youbi;
                    }
                }
            } else {
                final PrintDay printDay = dates.get(targetDate);
                return printDay == null ? null : printDay._youbi;
            }
            return null;
        }

        /**
         * 出席延べ人数、欠席のべ人数を算出。
         * @param svf
         * @param studentList
         * @param isLastPage
         * @param allStudentList
         */
        private void printDefferedTotalList(final List<Student> allStudentList, final Map<String, PrintDay> dates) {
            //svfPrintDayList()関数の月集計処理バージョン。
            //svfPrintDayList()関数だと日別処理で、かつ出力処理がRECORD内の処理となっていて、その前段階での出力としたいため、別口の処理とした。
            //svfPrintDayList()関数を変更する場合は、こちらも修正が必要か、判断が必要。
            //isLastPageについては上位で判断するため、省いている。
            int totalAbsence = 0;
            int totalAttend = 0;
            for (final String date : dates.keySet()) { // 日付ごとにレコード出力
                final PrintDay printDay = dates.get(date);

                final boolean isAmikakeTargetDay = "1".equals(printDay._holidayFlg) || !printDay._isPrintTargetSemester;
                //printMark(svf, studentList, MAX_STUDENT_LINE, date, printDay._remark1, isAmikakeTargetDay);
                if (getCalendar(date).get(Calendar.DATE) > Integer.parseInt(_param._targetDay) || !_param._isGakunenKongou && isAmikakeTargetDay) {
                    // 表示しない
                } else {
                    final DiCdAccumulate diCdAcc = new DiCdAccumulate();
                    int regdCount = 0;
                    for (final Student student : allStudentList) {

                        final boolean isZaiseki = Boolean.FALSE == student.isZaiseki(date) ? false : true;
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
                                for (final Integer diCd : attendDayDat._diCdMarkMap.keySet()) {
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
                    int i = 0;
                    if (i <= 2 && _param._sickNoticeNonoticeCds.contains(dicdSick)) {
                        i += 1;
                        VriOut("TOTAL_KESSEKI" + i + "_1", sick);
                        absence += sick;
                    }
                    if (i <= 2 && _param._sickNoticeNonoticeCds.contains(dicdNotice)) {
                        i += 1;
                        VriOut("TOTAL_KESSEKI" + i + "_1", notice);
                        absence += notice;
                    }
                    if (i <= 2 && _param._sickNoticeNonoticeCds.contains(dicdNonotice)) {
                        i += 1;
                        VriOut("TOTAL_KESSEKI" + i + "_1", nonotice);
                        absence += nonotice;
                    }
                    totalAbsence += absence;
                    totalAttend += studentsRegdCount - suspendMourning - absence;
                }
            }
            VrsOut("LESSON_TOTAL", String.valueOf(totalAttend));
            VrsOut("KESSEKI_TOTAL", String.valueOf(totalAbsence));
        }

        private void printContAbsenceList(final List<MonthAttendTopData> attendMonthAbsenceList) {
            //リストデータを出力
            int cnt = 1;
            for (final MonthAttendTopData printdat : attendMonthAbsenceList) {
                if ("F1".equals(printdat._diCd)) {
                    VrsOutn("RESEARCH_KIND", cnt, "病・事故欠");
                } else {
                    VrsOutn("RESEARCH_KIND", cnt, StringUtils.defaultString(printdat._type, ""));
                }
                VrsOutn("RESEARCH_DAY", cnt, StringUtils.defaultString(printdat._abTotal, ""));
                final int nlen = KNJ_EditEdit.getMS932ByteLength(printdat._name);
                final String nfield = nlen > 20 ? "2" : "1";
                VrsOutn("RESEARCH_NAME" + nfield, cnt, StringUtils.defaultString(printdat._name, ""));
                VrsOutn("RESEARCH_REASON", cnt, StringUtils.defaultString(printdat._remark, ""));
                final int glen = KNJ_EditEdit.getMS932ByteLength(StringUtils.defaultString(printdat._guardName, ""));
                final String gfield = glen > 20 ? "2" : "1";
                VrsOutn("RESEARCH_GUARD_NAME" + gfield, cnt, StringUtils.defaultString(printdat._guardName, ""));
                VrsOutn("RESEARCH_PROCESS", cnt, StringUtils.defaultString(printdat._treatment, ""));
                cnt++;
            }
        }
    }

    private static class Range {
        long _start;
        long _end;
        Range(final long start, final long end) {
            _start = start;
            _end = end;
        }
        Range(final String sdate, final String edate) {
            this(dateToLong(sdate), dateToLong(edate));
        }
        public static long dateToLong(final String date) {
            return 365 * Student.cal(date, null).get(Calendar.YEAR) + Student.cal(date, null).get(Calendar.DAY_OF_YEAR);
        }

        public boolean contains(final String date) {
            return _start <= dateToLong(date) && dateToLong(date) <= _end;
        }
        public boolean contains(final Range checkRange) {
            if (_end < checkRange._start || _start > checkRange._end) {
                return false;
            }
            if (_start <= checkRange._start && checkRange._start <= _end
             || _start <= checkRange._end   && checkRange._end <= _end
             || checkRange._start <= _start  && _start <= checkRange._end
             || checkRange._start <= _end    && _end   <= checkRange._end
             ) {
                return true;
            }
            log.warn(" unknown state : (" + this + ") , (" + checkRange + ")");
            return false;
        }
        @Override
        public int hashCode() {
            return (int) (_start * 365 * _end);
        }
        @Override
        public String toString() {
            return "Range(" + _start + ", " + _end + ")";
        }
    }

    private static boolean diffIs1day(final String date1, final String date2) {
        if (null == date1 || null == date2) {
            return false;
        }
        final Calendar cal1 = getCalendar(date1);
        cal1.add(Calendar.DATE, 1);
        return cal1.equals(getCalendar(date2));
    }

    private static Calendar getCalendar(final String date) {
        final Calendar cal = Calendar.getInstance();
        cal.setTime(Date.valueOf(date));
        return cal;
    }

    private static int toInt(final String v) {
        if (NumberUtils.isDigits(v)) {
            return Integer.parseInt(v);
        }
        return 0;
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

    private static <A, B, C> TreeMap<B, C> getMappedMap(final Map<A, TreeMap<B, C>> map, final A key) {
        if (!map.containsKey(key)) {
            map.put(key, new TreeMap<B, C>());
        }
        return map.get(key);
    }

    private static StringBuffer mkString(final List<String> list, final String comma) {
        final StringBuffer stb = new StringBuffer();
        String comma0 = "";
        for (final String s : list) {
            if (StringUtils.isBlank(s)) {
                continue;
            }
            stb.append(comma0).append(s);
            comma0 = comma;
        }
        return stb;
    }

    private static String formatDate(final Param param, final DB2UDB db2, final String date) {
        return param._seirekiFlg ? date.substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(date) : KNJ_EditDate.h_format_JP(db2, date);
    }

    private static void createSchoolKindDates(final TreeMap<String, PrintDay> datesOrigin, final Map<String, TreeMap<String, PrintDay>> schoolKindDates, final String schoolKind) {
        final Map<String, PrintDay> dates = getMappedMap(schoolKindDates, schoolKind);
        for (final String key : datesOrigin.keySet()) {
            final PrintDay printDay = datesOrigin.get(key);
            dates.put(key, new PrintDay(printDay));
        }
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
        String _event;
        boolean _isPrintTargetSemester = false;
        PrintDay(final Calendar cal) {
            _date = getDateString(cal);
            _dayOfMonth = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
            final int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK); // 1:日 2:月 3:火 4:水 5:木 6:金 7:土
            _youbi = youbi[dayOfWeek];
            _holidayFlg = "";
            _remark1 = "";
            _event = "";
        }
        PrintDay(final PrintDay printDay) {
            _date = printDay._date;
            _dayOfMonth = printDay._dayOfMonth;
            _youbi = printDay._youbi;
            _holidayFlg = printDay._holidayFlg;
            _remark1 = printDay._remark1;
            _event =  printDay._event;
            _isPrintTargetSemester = printDay._isPrintTargetSemester;
        }
        public String toString() {
            return "PrintDay(" + _date + (null == _holidayFlg ? "" : ",holiday")  + ")";
        }
    }

    private static class StudentGroup {
        final Param _param;
        final List<Student> _allStudentList;
        List<Map<String, String>> _heisaRowList = Collections.EMPTY_LIST;
        StudentGroup(final Param param, final List<Student> studentList) {
            _param = param;
            _allStudentList = studentList;
        }

        /**
         * 生徒のリストを得る。
         * @param db2
         * @return
         */
        private static StudentGroup getStudentGroup(final DB2UDB db2, final Param param) {

            final List<Student> studentList = Student.getStudentList(db2, param);

            for (final Student student : studentList) {

                final String psKey = "ATTEND_DAY_DAT";
                if (null == param.getPs(psKey)) {
                    final String sql = getAttendDayDatSql(param);
                    if (param._isOutputDebug) {
                        log.info(" sqlAttendDayDat =" + sql);
                    }
                    param.setPs(db2, psKey, sql);
                }

                for (final Map row : KnjDbUtils.query(db2, param.getPs(psKey), new Object[] {student._schregno})) {
                    final String attenddate = KnjDbUtils.getString(row, "ATTENDDATE");

                    student._attendSemesRemarkDatRemark1 = KnjDbUtils.getString(row, "REMARK1");

                    // 出欠備考データ
                    if (null != attenddate && null != KnjDbUtils.getString(row, "DI_CD")) {
                        final Integer diCd = Integer.valueOf(KnjDbUtils.getString(row, "DI_CD"));
                        if (null == student._attendDayDatMap.get(attenddate)) {
                            student._attendDayDatMap.put(attenddate, new AttendDayDat(student._schregno, attenddate));
                        }
                        final AttendDayDat attendDayDat = student._attendDayDatMap.get(attenddate);
                        //A4横2指定でのみ、略称ではなく名称を取得する。
                        if (FORM_DIV4.equals(param._formDiv) || FORM_DIV5.equals(param._formDiv)) {
                            attendDayDat.setDiCdMark(diCd, param._nameMstC001Name1.get(diCd.toString()));
                        } else {
                            attendDayDat.setDiCdMark(diCd, param._nameMstC001Abbv1.get(diCd.toString()));
                        }
                        final Integer sublCd = null == KnjDbUtils.getString(row, "SUBL_CD") ? maxCd : Integer.valueOf(KnjDbUtils.getString(row, "SUBL_CD"));
                        final Integer submcd = null == KnjDbUtils.getString(row, "SUBM_CD") ? maxCd : Integer.valueOf(KnjDbUtils.getString(row, "SUBM_CD"));
                        // 0：出席は備考に表示しない
                        if(diCd == 0) {
                            continue;
                        }
                        student.setAttendRemark(attenddate, diCd, sublCd, submcd);
                    }
                }
            }

            if (param._isMusashinohigashi || param._isHibarigaoka) {
                for (final Student student : studentList) {

                    final String psKey = "ABSENCE_REMARK";
                    if (null == param.getPs(psKey)) {
                        final String sql = getAbsenceRemarkSql(param);
                        if (param._isOutputDebug) {
                            log.info(" sql absenceremark =" + sql);
                        }
                        param.setPs(db2, psKey, sql);
                    }

                    final List<Map<String, String>> absenceRowList = KnjDbUtils.query(db2, param.getPs(psKey), new Object[] {student._schregno, student._entDate, student._grdDate});
                    final List<String> remarkList = new ArrayList<String>();
                    for (final Map<String, String> row : absenceRowList) {
                        final String absencedate = KnjDbUtils.getString(row, "ABSENCE_DATE");
                        if (null == absencedate) {
                            continue;
                        }
                        final String remark = KnjDbUtils.getString(row, "REMARK");
                        remarkList.add(remark);
                        if (param._isMusashinohigashi) {
                            student._absenceRemark.put(absencedate, remark);
                        }
                    }
                    student._absenceRemarkHibari = mkString(remarkList, "、").toString();

                }
            }

            for (final Student student : studentList) {

                final String psKey = "PS_ATTEND_SEMES_DAT";
                if (null == param.getPs(psKey)) {
                    final String sql = getAttendSemesDatSql(param);
                    if (param._isOutputDebug) {
                        log.info(" sqlAttendSemesDat =" + sql);
                    }
                    param.setPs(db2, psKey, sql);
                }

                for (final Map<String, String> row : KnjDbUtils.query(db2, param.getPs(psKey), new Object[] {student._schregno, student._entDate, student._grdDate})) {

                    student._attendSemesDat._lesson = toInt(KnjDbUtils.getString(row, "LESSON"));
                    student._attendSemesDat._suspend = toInt(KnjDbUtils.getString(row, "SUSPEND"));
                    student._attendSemesDat._koudome = "true".equals(param._useKoudome) ? toInt(KnjDbUtils.getString(row, "KOUDOME")) : 0;
                    student._attendSemesDat._virus = "true".equals(param._useVirus) ? toInt(KnjDbUtils.getString(row, "VIRUS")) : 0;
                    student._attendSemesDat._mourning = toInt(KnjDbUtils.getString(row, "MOURNING"));
                    student._attendSemesDat._sick = param._nameMstC001Name1.containsKey(dicdSick.toString()) ? toInt(KnjDbUtils.getString(row, "SICK")) : 0;
                    student._attendSemesDat._notice = param._nameMstC001Name1.containsKey(dicdNotice.toString()) ? toInt(KnjDbUtils.getString(row, "NOTICE")) : 0;
                    student._attendSemesDat._nonotice = param._nameMstC001Name1.containsKey(dicdNonotice.toString()) ? toInt(KnjDbUtils.getString(row, "NONOTICE")) : 0;
                    student._attendSemesDat._late = toInt(KnjDbUtils.getString(row, "LATE"));
                    student._attendSemesDat._early = toInt(KnjDbUtils.getString(row, "EARLY"));
                    student._attendSemesDat._kekkaJisu = toInt(KnjDbUtils.getString(row, "KEKKA_JISU"));
                }
            }

            for (final Student student : studentList) {
                if (student._isHoumonsei) {
                    student._eventSchregDatList = new ArrayList<String>();
                    student._eventSchregDatHolidayList = new ArrayList();

                    final String psKey = "EVENT_SCHREG_DAT";

                    if (null == param.getPs(psKey)) {
                        final String sql = getEventSchregDatSql(param);
                        log.debug(" eventSchregDat sql =" + sql);
                        param.setPs(db2, psKey, sql);
                    }

                    for (final Map<String, String> row : KnjDbUtils.query(db2, param.getPs(psKey), new Object[] { student._schregno})) {
                        final String executedate = KnjDbUtils.getString(row, "EXECUTEDATE");
                        student._eventSchregDatList.add(executedate);
                        if ("1".equals(KnjDbUtils.getString(row, "HOLIDAY_FLG")) ) {
                            student._eventSchregDatHolidayList.add(executedate);
                        }
                    }
                }
            }

            if (param._hasSCHREG_ENT_GRD_HIST_COMEBACK_DAT) {
                for (final Student student : studentList) {

                    student._schregEntGrdHistComebackDatList = new ArrayList();

                    final String psKey = "SCHREG_ENT_GRD_HIST_COMEBACK_DAT";

                    if (null == param.getPs(psKey)) {
                        String sql = "";
                        sql += " SELECT ";
                        sql += "      T1.COMEBACK_DATE ";
                        sql += "    , T1.ENT_DIV ";
                        sql += "    , VALUE(T1.ENT_DATE, '1900-01-01') AS ENT_DATE ";
                        sql += "    , T2.NAME1 AS ENT_DIV_NAME ";
                        sql += "    , T1.GRD_DIV ";
                        sql += "    , VALUE(T1.GRD_DATE, '9999-12-31') AS GRD_DATE ";
                        sql += "    , VALUE(T1.TENGAKU_SAKI_ZENJITU, '9999-12-31') AS TENGAKU_SAKI_ZENJITU ";
                        sql += "    , T3.NAME1 AS GRD_DIV_NAME ";
                        sql += " FROM SCHREG_ENT_GRD_HIST_COMEBACK_DAT T1 ";
                        sql += " LEFT JOIN NAME_MST T2 ON T2.NAMECD1 = 'A002' AND T2.NAMECD2 = T1.ENT_DIV ";
                        sql += " LEFT JOIN NAME_MST T3 ON T3.NAMECD1 = 'A003' AND T3.NAMECD2 = T1.GRD_DIV ";
                        sql += " WHERE T1.SCHREGNO = ? ";
                        sql += "   AND (FISCALYEAR(T1.ENT_DATE) = '" + param._ctrlYear + "' ";
                        sql += "     OR FISCALYEAR(T1.GRD_DATE) = '" + param._ctrlYear + "' ";
                        sql += "     OR FISCALYEAR(T1.TENGAKU_SAKI_ZENJITU) = '" + param._ctrlYear + "' ";
                        sql += "       ) ";
                        sql += " ORDER BY T1.COMEBACK_DATE ";
                        log.debug(" eventSchregDat sql =" + sql);
                        param.setPs(db2, psKey, sql);
                    }

                    final String[] keys = {"ENT_DIV", "ENT_DATE", "ENT_DIV_NAME", "GRD_DIV", "GRD_DATE", "GRD_DIV_NAME", };

                    for (final Map<String, String> row : KnjDbUtils.query(db2, param.getPs(psKey), new Object[] { student._schregno})) {
                        final Map<String, String> m = new HashMap<String, String>();
                        for (final String key : keys) {
                            m.put(key, KnjDbUtils.getString(row, key));
                        }
                        student._schregEntGrdHistComebackDatList.add(m);
                    }
                }
            }

            for (final Student student : studentList) {

                for (final Iterator<String> dit = student._attendDayDatMap.keySet().iterator(); dit.hasNext();) {
                    final String date = dit.next();
                    Boolean isZaiseki = student.isZaiseki(date);
                    if (null != isZaiseki && !isZaiseki.booleanValue()) {
                        if (param._isOutputDebug) {
                            log.info(" student " + student._schregno + " remove attend " + date);
                        }
                        dit.remove();
                        student.removeAttendRemark(date);
                    }
                }
            }

            if ("1".equals(param._printZaisekiOnly)) {
                final TreeSet<String> dates = new TreeSet<String>(getStudentDates(param, studentList).keySet());

                final Range monthDateRange = new Range(dates.first(), dates.last());

                for (final Iterator<Student> it = studentList.iterator(); it.hasNext();) {
                    final Student student = it.next();

                    boolean zaiseki = false;

                    if (zaiseki == false) {
                        zaiseki = new Range(student._entDate, student._grdDate).contains(monthDateRange);
                        if (param._isOutputDebug) {
                            log.info(" student " + student._schregno + " (" + student._attendno + ") zaiseki = " + zaiseki + " (" + student._entDate + ", " + student._grdDate + ") in (" + dates.first() + ", " + dates.last() + ")");
                        }
                    }
                    if (zaiseki == false) {
                        for (final Iterator<Map<String, String>> eit = student._schregEntGrdHistComebackDatList.iterator(); eit.hasNext() && zaiseki == false;) {
                            final Map row = eit.next();
                            final String entDate = KnjDbUtils.getString(row, "ENT_DATE");
                            final String grdDate = KnjDbUtils.getString(row, "GRD_DATE");
                            zaiseki = new Range(entDate, grdDate).contains(monthDateRange);
                            if (param._isOutputDebug) {
                                log.info(" student " + student._schregno + " (" + student._attendno + ") entgrdhist zaiseki = " + zaiseki + " (" + student._entDate + ", " + student._grdDate + ") in (" + dates.first() + ", " + dates.last() + ")");
                            }
                        }
                    }
                    if (zaiseki == false) {
                        it.remove();
                        if (param._isOutputDebug) {
                            log.info(" not print student : " + student + " (" + student._entDate + ", " + student._grdDate + "), (" + student._schregEntGrdHistComebackDatList + ")");
                        }
                    }
                }
            }

            if (FORM_DIV4.equals(param._formDiv) || FORM_DIV5.equals(param._formDiv) || FORM_DIV6_1.equals(param._formDiv) || FORM_DIV6_3.equals(param._formDiv)) {
                getContAbsenceList(db2, param, studentList);
            }

            setKyugaku(db2, param, studentList);

            if (param._isOutputDebug) {
                log.info(" students size = " + String.valueOf(studentList.size()));
            }
            final StudentGroup group = new StudentGroup(param, studentList);
            group.setHeisa(db2);
            return group;
        }

        private void setHeisa(final DB2UDB db2) {
            final TreeSet<String> gradeHrclasses = new TreeSet<String>();
            for (final Student student : _allStudentList) {
                gradeHrclasses.add(student._gradeHrclass);
            }
            final String[] gradeHrclassesArray = new String[gradeHrclasses.size()];

            final StringBuilder sql = new StringBuilder();
            sql.append(" SELECT ");
            sql.append("   T1.INPUT_TYPE ");
            sql.append(" , T2.GRADE ");
            sql.append(" , T2.HR_CLASS ");
            sql.append(" , T1.FROM_DATE ");
            sql.append(" , T1.TO_DATE ");
            sql.append(" FROM ATTEND_BATCH_INPUT_HDAT T1 ");
            sql.append(" INNER JOIN ATTEND_BATCH_INPUT_HR_DAT T2 ON T2.YEAR = T1.YEAR AND T2.SEQNO = T1.SEQNO ");
            sql.append(" WHERE ");
            sql.append("   T1.YEAR = '" + _param._ctrlYear  + "' ");
            sql.append("   AND T2.GRADE || T2.HR_CLASS IN " + SQLUtils.whereIn(true, gradeHrclasses.toArray(gradeHrclassesArray))  + " ");

            _heisaRowList = KnjDbUtils.query(db2, sql.toString());
        }

        private boolean isHeisa(final String date) {
            final List<Map<String, String>> heisaRangeList = new ArrayList<Map<String, String>>();
            for (final Map<String, String> heisaRow : _heisaRowList) {
                final Range range = new Range(KnjDbUtils.getString(heisaRow, "FROM_DATE"), KnjDbUtils.getString(heisaRow, "TO_DATE"));
                if (range.contains(date)) {
                    heisaRangeList.add(heisaRow);
                }
            }
            final boolean isHeisa = heisaRangeList.size() > 0;
            if (_param._isOutputDebug) {
                if (isHeisa) {
                    log.info(" heisa row = " + heisaRangeList);
                }
            }
            return isHeisa;
        }

        private List<String> getSchoolKindList() {
            final List<String> schoolKindList = new ArrayList<String>();
            for (final Student student : _allStudentList) {
                if (null != student._schoolKind && !schoolKindList.contains(student._schoolKind)) {
                    schoolKindList.add(student._schoolKind);
                }
            }
            return schoolKindList;
        }

        private static String getAttendDayDatSql(final Param param) {
            final StringBuffer stb = new StringBuffer();

            stb.append(" WITH SCHREGNOS (SCHREGNO) AS ( ");
            stb.append("   VALUES(?) ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     SEME_R.REMARK1, ");
            stb.append("     T2.ATTENDDATE, ");
            stb.append("     T2.DI_CD, ");
            stb.append("     T4.SUBL_CD, ");
            stb.append("     T5.SUBM_CD ");
            stb.append(" FROM ");
            stb.append("     SCHREGNOS T1 ");
            stb.append("     LEFT JOIN ATTEND_DAY_DAT T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("         AND T2.YEAR = '" + param._ctrlYear + "' ");
            stb.append("         AND MONTH(T2.ATTENDDATE) = " + Integer.parseInt(param._targetMonth) + " ");
            stb.append("         AND DAY(T2.ATTENDDATE) <= " + Integer.parseInt(param._targetDay) + " ");
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
            stb.append("          AND SEME_R.YEAR = '" + param._ctrlYear + "' ");
            stb.append("          AND SEME_R.MONTH = '" + param._targetMonth + "' ");
            stb.append("          AND SEME_R.SEMESTER = '" + param._targetSemes + "' ");
            stb.append("          AND SEME_R.SCHREGNO = T1.SCHREGNO ");
            stb.append(" ORDER BY ");
            stb.append("     T2.ATTENDDATE ");
            return stb.toString();
        }

        private static String getAbsenceRemarkSql(final Param param) {
            final StringBuffer stb = new StringBuffer();

            stb.append(" WITH SCHREGNOS (SCHREGNO) AS ( ");
            stb.append("   VALUES(?) ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     REM.ABSENCE_DATE, ");
            stb.append("     REM.REMARK ");
            stb.append(" FROM ");
            stb.append("     SCHREGNOS T1 ");
            stb.append("     LEFT JOIN ATTEND_ABSENCE_REMARK_DAT REM ON REM.SCHREGNO = T1.SCHREGNO ");
            stb.append("         AND FISCALYEAR(REM.ABSENCE_DATE) = '" + param._ctrlYear + "' ");
            stb.append("         AND MONTH(REM.ABSENCE_DATE) = " + Integer.parseInt(param._targetMonth) + " ");
            stb.append("         AND DAY(REM.ABSENCE_DATE) <= " + Integer.parseInt(param._targetDay) + " ");
            stb.append("         AND REM.ABSENCE_DATE BETWEEN ? AND ? ");
            stb.append(" ORDER BY ");
            stb.append("     REM.ABSENCE_DATE ");
            return stb.toString();
        }

        private static String getAttendSemesDatSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH SCHREGNOS (SCHREGNO) AS ( ");
            stb.append("   VALUES(?) ");
            stb.append(" ), SCHEDULES AS (SELECT ");
            stb.append("     T3.SCHREGNO, ");
            stb.append("     COUNT(DISTINCT T1.EXECUTEDATE) AS LESSON ");
            stb.append("   FROM ");
            stb.append("     SCH_CHR_DAT T1 ");
            stb.append("     INNER JOIN CHAIR_DAT T2 ON T2.YEAR = T1.YEAR AND T2.SEMESTER = T1.SEMESTER AND T2.CHAIRCD = T1.CHAIRCD ");
            stb.append("     INNER JOIN CHAIR_STD_DAT T3 ON T3.YEAR = T2.YEAR AND T3.SEMESTER = T2.SEMESTER AND T3.CHAIRCD = T2.CHAIRCD AND T1.EXECUTEDATE BETWEEN T3.APPDATE AND T3.APPENDDATE ");
            stb.append("     INNER JOIN SCHREGNOS T4 ON T4.SCHREGNO = T3.SCHREGNO ");
            stb.append("   WHERE ");
            stb.append("     T1.YEAR = '" + param._ctrlYear + "' ");
            stb.append("     AND MONTH(T1.EXECUTEDATE) = " + Integer.parseInt(param._targetMonth) + " ");
            stb.append("     AND T1.EXECUTEDATE BETWEEN ? AND ? ");
            stb.append("     AND T1.SEMESTER = '" + param._targetSemes + "' ");
            stb.append("     AND DAY(T1.EXECUTEDATE) BETWEEN " + String.valueOf(param._attendDayDatSday) + " AND " + Integer.parseInt(param._targetDay) + " ");
            stb.append("   GROUP BY ");
            stb.append("     T3.SCHREGNO ");
            stb.append(" ), ATTEND_SEMES AS ( ");
            stb.append("   SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     VALUE(T1.LESSON,0) AS LESSON, ");
            stb.append("     VALUE(T1.SUSPEND,0) AS SUSPEND, ");
            if ("true".equals(param._useVirus)) {
                stb.append("     VALUE(T1.VIRUS,0) AS VIRUS, ");
            } else {
                stb.append("     0 AS VIRUS, ");
            }
            if ("true".equals(param._useKoudome)) {
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
            stb.append("     T1.YEAR = '" + param._ctrlYear + "' ");
            stb.append("     AND T1.MONTH = '" + param._targetMonth + "' ");
            stb.append("     AND T1.SEMESTER = '" + param._targetSemes + "' ");
            stb.append("     AND INT(T1.APPOINTED_DAY) = " + String.valueOf(param._attendSemesAppointedDay) + " ");
            stb.append("   UNION ALL ");
            stb.append("     SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     MAX(VALUE(T4.LESSON,0)) AS LESSON, ");
            stb.append("     SUM(CASE WHEN T1.DI_CD IN ('2', '9') THEN 1 ELSE 0 END) AS SUSPEND, ");
            if ("true".equals(param._useVirus)) {
                stb.append("     SUM(CASE WHEN T1.DI_CD IN ('19', '20') THEN 1 ELSE 0 END) AS VIRUS, ");
            } else {
                stb.append("     0 AS VIRUS, ");
            }
            if ("true".equals(param._useKoudome)) {
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
            stb.append("     T1.YEAR = '" + param._ctrlYear + "' ");
            stb.append("     AND MONTH(T1.ATTENDDATE) = " + Integer.parseInt(param._targetMonth) + " ");
            stb.append("     AND DAY(T1.ATTENDDATE) BETWEEN " + String.valueOf(param._attendDayDatSday) + " AND " + Integer.parseInt(param._targetDay) + " ");
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

        private static String getEventSchregDatSql(final Param param) {

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

        private static void getContAbsenceList(final DB2UDB db2, final Param param, final List<Student> studentList) {

            final Map<String, Student> studentMap = new HashMap<String, Student>();
            for (final Student student : studentList) {
                studentMap.put(student._schregno, student);
            }

            for (final Student student : studentMap.values()) {
                final String psKey = "PS_ABSENCD_MONTH_REMARK_DAT";
                if (null == param.getPs(psKey)) {
                    final String sql = getAbsencdMonthRemarkDatSql(param);
                    if (param._isOutputDebug) {
                        log.info(" getAbsencdMonthRemarkDatSql =" + sql);
                    }
                    param.setPs(db2, psKey, sql);
                }
                student._monthAttendTopDataList = new ArrayList();

                for (final Map row : KnjDbUtils.query(db2, param.getPs(psKey), new Object[] {student._schregno})) {
                    final int seq = Integer.parseInt(KnjDbUtils.getString(row, "SEQ"));
                    final String diCd = KnjDbUtils.getString(row, "DI_CD");
                    final String type = KnjDbUtils.getString(row, "AB_TYPE");
                    final String ab_total = KnjDbUtils.getString(row, "AB_TOTAL");
                    final String schregno = KnjDbUtils.getString(row, "SCHREGNO");
                    final String name = KnjDbUtils.getString(row, "NAME");
                    final String remark = KnjDbUtils.getString(row, "REMARK");
                    final String guardname = KnjDbUtils.getString(row, "GUARD_NAME");
                    final String treatment = KnjDbUtils.getString(row, "TREATMENT");
                    student._monthAttendTopDataList.add(new MonthAttendTopData(seq, diCd, type, ab_total, schregno, name, remark, guardname, treatment));
                }
            }
        }

        private static String getAbsencdMonthRemarkDatSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("   T1.SEQ, ");
            stb.append("   NC001.NAME1 AS AB_TYPE, ");
            stb.append("   T1.DI_CD, ");
            stb.append("   T1.TOTAL_DAY AS AB_TOTAL, ");
            stb.append("   T1.SCHREGNO, ");
            stb.append("   T2.NAME, ");
            stb.append("   T1.REMARK, ");
            stb.append("   T3.GUARD_NAME, ");
            stb.append("   T1.TREATMENT ");
            stb.append(" FROM ");
            stb.append("   ATTEND_ABSENCE_MONTH_REMARK_DAT T1 ");
            stb.append("   INNER JOIN SCHREG_BASE_MST T2 ");
            stb.append("      ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("   LEFT JOIN GUARDIAN_DAT T3 ");
            stb.append("     ON T3.SCHREGNO = T1.SCHREGNO ");
            stb.append("   LEFT JOIN NAME_MST NC001 ");
            stb.append("     ON NC001.NAMECD1 = 'C001' ");
            stb.append("    AND NC001.NAMECD2 = T1.DI_CD ");
            stb.append(" WHERE ");
            stb.append("   T1.YEAR = '" + param._ctrlYear + "' ");
            stb.append("   AND T1.MONTH = '" + param._targetMonth + "' ");
            stb.append("   AND T1.SCHREGNO = ? ");
            stb.append(" ORDER BY ");
            stb.append("   INT(T1.TOTAL_DAY) DESC, ");
            stb.append("   T1.SCHREGNO, ");
            stb.append("   T1.SEQ ");
            return stb.toString();
        }

        private static void setKyugaku(final DB2UDB db2, final Param param, final List<Student> studentList) {
            final String psKey = "PS_KYUGAKU";
            String sql = "";
            sql += " SELECT TRANSFER_SDATE, TRANSFER_EDATE ";
            sql += " FROM SCHREG_TRANSFER_DAT ";
            sql += " WHERE SCHREGNO = ? ";
            sql += "   AND TRANSFERCD = '2' ";
            sql += "   AND (TRANSFER_SDATE BETWEEN '" + param._dates.firstKey() + "' AND '" + param._dates.lastKey() + "' ";
            sql += "     OR TRANSFER_EDATE BETWEEN '" + param._dates.firstKey() + "' AND '" + param._dates.lastKey() + "' ";
            sql += "     OR TRANSFER_SDATE <= '" + param._dates.firstKey() + "' AND '" + param._dates.lastKey() + "' <= TRANSFER_EDATE ";
            sql += "   ) ";
            sql += " ORDER BY TRANSFER_SDATE ";
            param.setPs(db2, psKey, sql);
            if (param._isOutputDebug) {
                log.info(" kyugaku sql = " + sql);
            }
            for (final Student student : studentList) {
                student._kyugakuDateRangeList = KnjDbUtils.query(db2, param.getPs(psKey), new Object[] {student._schregno});
                if (param._isOutputDebug) {
                    if (student._kyugakuDateRangeList.size() > 0) {
                        log.info(" kyugaku " + student._schregno + " = " + student._kyugakuDateRangeList);
                    }
                }
            }
        }

        private static TreeMap<String, PrintDay> getStudentDates(final Param param, final List<Student> allStudentList) {
            final String schoolKind = getStudentSchoolKind(allStudentList);
            log.debug(" schoolKind = " + schoolKind);
            final TreeMap<String, PrintDay> dates = getPrintDates(param, schoolKind);
            return dates;
        }

        private static TreeMap<String, PrintDay> getPrintDates(final Param param, final String schoolKind) {
            final TreeMap<String, PrintDay> dates = getMappedMap(param._schoolKindDates, schoolKind);
            if (dates.isEmpty()) {
                createSchoolKindDates(param._dates, param._schoolKindDates, schoolKind);
            }
            if (param._isOutputDebug) {
                log.info(" dates size = " + dates.size());
            }
            return dates;
        }

        private static String getStudentSchoolKind(final List<Student> allStudentList) {
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

    /**
     * 生徒
     */
    private static class Student {
        final String _schregno;
        final String _name;
        final String _gradeHrclass;
        final String _attendno;
        final String _schoolKind;
        final String _entDate;
        final String _entDiv;
        final String _entDivName;
        final String _grdDate;
        final String _tengakuSakiZenjitu;
        final String _grdDiv;
        final String _grdDivName;
        final String _gradeName1;
        final String _coursename;
        final String _schoolname;
        String _absenceRemarkHibari = "";
        final boolean _isHoumonsei;
        int _page;
        int _line;
        final Map<String, AttendDayDat> _attendDayDatMap = new HashMap<String, AttendDayDat>();
        final Map<String, String> _absenceRemark = new TreeMap();
        final Map<Integer, TreeMap<Integer, TreeMap<Integer, List<String>>>> _diCdMap = new TreeMap();
        String _attendSemesRemarkDatRemark1;
        final AttendSemesDat _attendSemesDat = new AttendSemesDat();
        private List<String> _eventSchregDatList = Collections.EMPTY_LIST;
        private List<String> _eventSchregDatHolidayList = Collections.EMPTY_LIST;
        private List<Map<String, String>> _schregEntGrdHistComebackDatList = Collections.EMPTY_LIST;
        private List<MonthAttendTopData> _monthAttendTopDataList = Collections.EMPTY_LIST;
        private List<Map<String, String>> _kyugakuDateRangeList = Collections.EMPTY_LIST;
        Student(final String schregno, final String name, final String gradeHrclass, final String attendno, final String schoolKind, final String entDiv, final String entDivName, final String entDate, final String grdDiv, final String grdDivName, final String grdDate, final String tengakuSakiZenjitu, final String gradeName1, final String coursename, final String schoolname, final boolean isHoumonsei) {
            _schregno = schregno;
            _name = name;
            _gradeHrclass = gradeHrclass;
            _attendno = attendno;
            _schoolKind = schoolKind;
            _entDate  = entDate;
            _entDiv = entDiv;
            _entDivName = entDivName;
            _grdDate  = grdDate;
            _tengakuSakiZenjitu = tengakuSakiZenjitu;
            _grdDiv = grdDiv;
            _grdDivName = grdDivName;
            _gradeName1 = gradeName1;
            _coursename = coursename;
            _schoolname = schoolname;
            _isHoumonsei = isHoumonsei;
        }

        public String simpleMonthDate(final String date) {
            if (null == date) {
                return "";
            }
            final Calendar cal = cal(date, null);
            return String.valueOf(cal.get(Calendar.MONTH) + 1) + "/" + String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
        }
        /**
         * 異動備考を得る
         * @param param
         * @param getType <code>"date"</code> or <code>"name"</code> or <code>"date,name"</code>
         * @return 異動備考
         */
        public String getTransferRemark(final Param param, final String getType, final String date) {
            if (null != _entDate && sameYearMonth(_entDate, param._monthSdate) && (null == date || _entDate.equals(date))) {
                final boolean isTennyuOrHennnyu = "4".equals(_entDiv) || "5".equals(_entDiv);
                if ("date,name".equals(getType)) {
                    if (isTennyuOrHennnyu) {
                        return simpleMonthDate(_entDate) + StringUtils.defaultString(_entDivName);
                    }
                } else {
                    if (FORM_DIV5.equals(param._formDiv) && isTennyuOrHennnyu || !FORM_DIV5.equals(param._formDiv) && null != _entDiv) {
                        if ("date".equals(getType)) {
                            return _entDate;
                        } else if ("name".equals(getType)) {
                            return StringUtils.defaultString(_entDivName);
                        }
                    }
                }
            }
            if (null != _grdDiv && !"4".equals(_grdDiv) && !(param._notPrintEntGrdRemarkGrddiv1 && "1".equals(_grdDiv))) {
                if (null != _grdDate && sameYearMonth(_grdDate, param._monthSdate) && (null == date || _grdDate.equals(date))) {
                    final boolean isTengaku = "3".equals(_grdDiv);
                    final String name = FORM_DIV5.equals(param._formDiv) && isTengaku ? "去" : StringUtils.defaultString(_grdDivName);
                    if ("date,name".equals(getType)) {
                        return simpleMonthDate(_grdDate) + name;
                    } else if ("date".equals(getType)) {
                        return _grdDate;
                    } else if ("name".equals(getType)) {
                        return name;
                    }
                }
                if (FORM_DIV5.equals(param._formDiv) && null != _tengakuSakiZenjitu && sameYearMonth(_tengakuSakiZenjitu, param._monthSdate) && (null == date || _tengakuSakiZenjitu.equals(date))) {
                    if ("date,name".equals(getType)) {
                        return simpleMonthDate(_tengakuSakiZenjitu) + "転学";
                    } else if ("date".equals(getType)) {
                        return _tengakuSakiZenjitu;
                    } else if ("name".equals(getType)) {
                        return "転学";
                    }
                }
            }
            for (final Map<String, String> e : _schregEntGrdHistComebackDatList) {
                final String entDate = e.get("ENT_DATE");
                final String grdDate = e.get("GRD_DATE");
                final String tengakuSakiZenjitu = e.get("TENGAKU_SAKI_ZENJITU");
                final String entDiv = e.get("ENT_DIV");
                final String grdDiv = e.get("GRD_DIV");
                final String entDivName = e.get("ENT_DIV_NAME");
                final String grdDivName = e.get("GRD_DIV_NAME");
                if (null != entDate && sameYearMonth(entDate, param._monthSdate) && (null == date || entDate.equals(date))) {
                    final boolean isTennyuOrHennnyu = "4".equals(entDiv) || "5".equals(entDiv);
                    if ("date,name".equals(getType)) {
                        if (isTennyuOrHennnyu) {
                            return simpleMonthDate(entDate) + StringUtils.defaultString(entDivName);
                        }
                    } else {
                        if (FORM_DIV5.equals(param._formDiv) && isTennyuOrHennnyu || !FORM_DIV5.equals(param._formDiv) && null != entDiv) {
                            if ("date".equals(getType)) {
                                return entDate;
                            } else if ("name".equals(getType)) {
                                return StringUtils.defaultString(entDivName);
                            }
                        }
                    }
                }
                if (null != grdDiv && !"4".equals(grdDiv) && !(param._notPrintEntGrdRemarkGrddiv1 && "1".equals(grdDiv))) {
                    if (null != grdDate && sameYearMonth(grdDate, param._monthSdate) && (null == date || grdDate.equals(date))) {
                        final boolean isTengaku = "3".equals(grdDiv);
                        final String name = FORM_DIV5.equals(param._formDiv) && isTengaku ? "去" : StringUtils.defaultString(grdDivName);
                        if ("date,name".equals(getType)) {
                            return simpleMonthDate(grdDate) + name;
                        } else if ("date".equals(getType)) {
                            return grdDate;
                        } else if ("name".equals(getType)) {
                            return name;
                        }
                    }
                }
                if (FORM_DIV5.equals(param._formDiv) && null != tengakuSakiZenjitu && sameYearMonth(tengakuSakiZenjitu, param._monthSdate) && (null == date || tengakuSakiZenjitu.equals(date))) {
                    if ("date,name".equals(getType)) {
                        return simpleMonthDate(tengakuSakiZenjitu) + "転学";
                    } else if ("date".equals(getType)) {
                        return tengakuSakiZenjitu;
                    } else if ("name".equals(getType)) {
                        return "転学";
                    }
                }
            }
            return null;
        }

        private boolean sameYearMonth(final String date1, final String date2) {
            final Calendar cal1 = cal(date1, null);
            final Calendar cal2 = cal(date2, null);
            return cal2.get(Calendar.MONTH) == cal1.get(Calendar.MONTH) && cal2.get(Calendar.YEAR) == cal1.get(Calendar.YEAR);
        }

        private static Calendar cal(final String date, final String defDate) {
            final Calendar cal = Calendar.getInstance();
            if (null != date) {
                cal.setTime(Date.valueOf(date));
            } else if (null != defDate) {
                cal.setTime(Date.valueOf(defDate));
            }
            return cal;
        }

        private static boolean between(final String date, final String sdate, final String edate) {
            final Calendar cal = cal(date, null);
            return lessEqual(cal(sdate, null), cal) && lessEqual(cal, cal(edate, null));
        }

        private static boolean lessEqual(final Calendar cal1, final Calendar cal2) {
            return cal1.before(cal2) || cal1.equals(cal2);
        }

        private static boolean greaterEqual(final Calendar cal1, final Calendar cal2) {
            return cal1.after(cal2) || cal1.equals(cal2);
        }

        private boolean isKyugakuKesseki(final Param param, final String date) {
            Map<String, String> schoolMst = param.getSchoolMst(_schoolKind);
            if (!"1".equals(KnjDbUtils.getString(schoolMst, "SEM_OFFDAYS"))) {
                return false;
            }
            if (null == date) {
                return false;
            }
            for (final Map<String, String> kyugaku : _kyugakuDateRangeList) {
                final String sdate = KnjDbUtils.getString(kyugaku, "TRANSFER_SDATE");
                final String edate = KnjDbUtils.getString(kyugaku, "TRANSFER_EDATE");
                final boolean inRange = sdate.compareTo(date) <= 0 && date.compareTo(edate) <= 0;
                if (inRange) {
                    return true;
                }
            }
            return false;
        }

        public boolean isShusseki(final String date) {
            if (null == _attendDayDatMap.get(date)) {
                return true;
            }
            return _attendDayDatMap.get(date).isShusseki();
        }

        // isShussekiの反対ではない
        public boolean isKesseki(final String date) {
            if (null == _attendDayDatMap.get(date)) {
                return false;
            }
            return _attendDayDatMap.get(date).isKesseki();
        }

        public List<String> getEntDateList(final String chkEntDiv, final String fstDayOfMonth, final String lastDayOfMonth) {
            final List<String> list = new ArrayList<String>();
            if (null != _entDate) {
                if (chkEntDiv.equals(_entDiv)) {
                    if (_entDate != null && between(_entDate, fstDayOfMonth, lastDayOfMonth)) {
                        list.add(_entDate);
                    }
                }
            }
            for (final Map e : _schregEntGrdHistComebackDatList) {
                final String entDate = KnjDbUtils.getString(e, "ENT_DATE");
                if (null != entDate && between(entDate, fstDayOfMonth, lastDayOfMonth)) {
                    if (chkEntDiv.equals(KnjDbUtils.getString(e, "ENT_DIV"))) {
                        list.add(entDate);
                    }
                }
            }
            return list;
        }

        public List<String> getTennyuEntDateList() {
            final String entDivTennyu = "4";
            final List<String> list = new ArrayList<String>();
            if (null != _entDate) {
                if (entDivTennyu.equals(_entDiv)) {
                    list.add(_entDate);
                }
            }
            for (final Map e : _schregEntGrdHistComebackDatList) {
                final String entDate = KnjDbUtils.getString(e, "ENT_DATE");
                if (null != entDate) {
                    if (entDivTennyu.equals(KnjDbUtils.getString(e, "ENT_DIV"))) {
                        list.add(entDate);
                    }
                }
            }
            return list;
        }

        public List<String> getGrdDateList(final String chkGrdDiv, final String fstDayOfMonth, final String lastDayOfMonth) {
            final List<String> list = new ArrayList<String>();
            if (null != _grdDate) {
                if (chkGrdDiv.equals(_grdDiv)) {
                    if (_grdDate != null && between(_grdDate, fstDayOfMonth, lastDayOfMonth)) {
                        list.add(_grdDate);
                    }
                }
            }
            for (final Map e : _schregEntGrdHistComebackDatList) {
                final String grdDate = KnjDbUtils.getString(e, "GRD_DATE");
                if (null != grdDate && between(grdDate, fstDayOfMonth, lastDayOfMonth)) {
                    if (chkGrdDiv.equals(KnjDbUtils.getString(e, "GRD_DIV"))) {
                        list.add(grdDate);
                    }
                }
            }
            return list;
        }

        public List<String> getLeaveGrdDateList(final String fstDayOfMonth, final String lastDayOfMonth) {
            final String grdDivTengaku2 = "4";
            final String grdDivTaigaku3 = "5";
            final List<String> list = new ArrayList<String>();
            if (null != _grdDate && _grdDiv != null) {
                if (!grdDivTengaku2.equals(_grdDiv) && !grdDivTaigaku3.equals(_grdDiv)) {
                    if (between(_grdDate, fstDayOfMonth, lastDayOfMonth)) {
                        list.add(_grdDate);
                    }
                }
            }
            for (final Map e : _schregEntGrdHistComebackDatList) {
                final String grdDate = KnjDbUtils.getString(e, "GRD_DATE");
                final String grdDiv = KnjDbUtils.getString(e, "GRD_DIV");
                if (null != grdDate && grdDiv != null) {
                    if (!grdDivTengaku2.equals(grdDiv) && !grdDivTaigaku3.equals(grdDiv)) {
                        if (between(grdDate, fstDayOfMonth, lastDayOfMonth)) {
                            list.add(grdDate);
                        }
                    }
                }
            }
            return list;
        }

        public List<String> getTentaiGrdDateList() {
            final String grdDivTengaku2 = "2";
            final String grdDivTaigaku3 = "3";
            final List<String> list = new ArrayList<String>();
            if (null != _grdDate) {
                if (grdDivTengaku2.equals(_grdDiv) || grdDivTaigaku3.equals(_grdDiv)) {
                    list.add(_grdDate);
                }
            }
            for (final Map<String, String> e : _schregEntGrdHistComebackDatList) {
                final String grdDate = KnjDbUtils.getString(e, "GRD_DATE");
                if (null != grdDate) {
                    if (grdDivTengaku2.equals(KnjDbUtils.getString(e, "GRD_DIV")) || grdDivTaigaku3.equals(KnjDbUtils.getString(e, "GRD_DIV"))) {
                        list.add(grdDate);
                    }
                }
            }
            return list;
        }

        public Boolean isZaiseki(final String date) {
            Boolean isZaiseki = null;
            if (null != _entDate) {
                if (null != _grdDate && cal(date, null).after(cal(_grdDate, null))) {
                    isZaiseki = Boolean.FALSE;
                } else if (between(date, _entDate, StringUtils.defaultString(_grdDate, "9999-12-31"))) {
                    isZaiseki = Boolean.TRUE;
                }
            }
            //log.info(" _entDate = " + _entDate + ", _grdDate = " + _grdDate + " => isZaiseki = " + isZaiseki);
            if (isZaiseki == null) {
                final int size = _schregEntGrdHistComebackDatList.size();
                if (size == 0) {
                    if (null != _entDate && cal(date, null).before(cal(_entDate, null))) {
                        isZaiseki = Boolean.FALSE;
                    }
                } else {
                    for (int ei = 0; ei < size && isZaiseki != Boolean.TRUE; ei++) {
                        final Map<String, String> e = _schregEntGrdHistComebackDatList.get(ei);
                        final String entDate = e.get("ENT_DATE");
                        final String grdDate = e.get("GRD_DATE");
                        if (null != entDate && null != grdDate) {
                            if (between(date, entDate, grdDate)) {
                                isZaiseki = Boolean.TRUE;
                            }
                        }
                    }
                    if (isZaiseki != Boolean.TRUE) {
                        isZaiseki = Boolean.FALSE;
                    }
                }
            }
            return isZaiseki;
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

        private void removeAttendRemark(final String attenddate) {
            for (final Iterator<Integer> diCdIt = _diCdMap.keySet().iterator(); diCdIt.hasNext();) {
                final Integer diCd = diCdIt.next();
                final Map<Integer, TreeMap<Integer, List<String>>> sublCdMap = _diCdMap.get(diCd);

                for (final Iterator<Integer> sublCdIt = sublCdMap.keySet().iterator(); sublCdIt.hasNext();) {
                    final Integer sublCd = sublCdIt.next();
                    final Map<Integer, List<String>> submCdMap = sublCdMap.get(sublCd);

                    for (final Iterator<Integer> submCdIt = submCdMap.keySet().iterator(); submCdIt.hasNext();) {
                        final Integer submCd = submCdIt.next();
                        final List<String> attenddateList = submCdMap.get(submCd);

                        attenddateList.remove(attenddate);

                        if (attenddateList.size() == 0) {
                            submCdIt.remove();
                        }
                    }

                    if (submCdMap.size() == 0) {
                        sublCdIt.remove();
                    }
                }

                if (sublCdMap.size() == 0) {
                    diCdIt.remove();
                }
            }
        }

        /**
         * 帳票右に表示する備考を得る
         * @param semeMonth 学期・月
         * @param param
         * @return
         */
        public String getAttendRemark(final DB2UDB db2, final Param param) {
            final String comma = "、";
            final List<String> remarkList = new ArrayList<String>();
            for (final Map<String, String> row : _kyugakuDateRangeList) {
                final String sdate = KnjDbUtils.getString(row, "TRANSFER_SDATE");
                final String edate = KnjDbUtils.getString(row, "TRANSFER_EDATE");
                remarkList.add(KNJ_EditDate.h_format_JP(db2, sdate) + "～" + KNJ_EditDate.h_format_JP(db2, edate) + " 休学");
            }
            if (param._remarkOption._addAttendSemesRemarkDatRemark1) {
                if (null != _attendSemesRemarkDatRemark1) {
                    remarkList.add(_attendSemesRemarkDatRemark1);
                }
            }
            if (param._remarkOption._addEntGrd) {
                if (null != _entDiv && !ArrayUtils.contains(new String[] {"1", "2", "3"}, _entDiv)) {
                    if (FORM_DIV3.equals(param._formDiv)) {
                        if(_entDate.contains(param._ctrlYear)) {
                            remarkList.add(shortDate(_entDate) + StringUtils.defaultString(_entDivName));
                        }
                    } else {
                        remarkList.add(shortDate(_entDate) + StringUtils.defaultString(_entDivName));
                    }
                }
                if (null != _grdDiv && !ArrayUtils.contains(new String[] {"1", "4"}, _grdDiv)) {
                    if (FORM_DIV3.equals(param._formDiv)) {
                        if(_grdDate.contains(param._ctrlYear)) {
                            remarkList.add(shortDate(_grdDate) + StringUtils.defaultString(_grdDivName));
                        }
                    } else {
                        remarkList.add(shortDate(_grdDate) + StringUtils.defaultString(_grdDivName));
                    }
                }
            }
            if (param._remarkOption._addAbsenreRemark) {
                for (final Iterator remit = _absenceRemark.entrySet().iterator(); remit.hasNext();) {
                    final Map.Entry e = (Map.Entry) remit.next();
                    final String remark = (String) e.getValue();
                    if (!StringUtils.isEmpty(remark)) {
                        remarkList.add(remark);
                    }
                }
            }
            if (param._remarkOption._addDicdDetail) {
                for (final Iterator<Integer> diit = _diCdMap.keySet().iterator(); diit.hasNext();) {
                    final Integer diCd = diit.next();
                    final TreeMap<Integer, TreeMap<Integer, List<String>>> sublDatMap = _diCdMap.get(diCd);
                    final String c001name = StringUtils.defaultString(param._nameMstC001Name1.get(diCd.toString()));

                    final StringBuffer stbs = getDetailRemark(param, comma, sublDatMap);
                    if (0 != stbs.length()) {
                        stbs.insert(0, "（").append("）");
                    }
                    stbs.insert(0, c001name);
                    remarkList.add(stbs.toString());
                }
            }
            return mkString(remarkList, comma).toString();
        }

        private StringBuffer getDetailRemark(final Param param, final String comma, final Map<Integer, TreeMap<Integer, List<String>>> sublDatMap) {
            final List<String> stbs = new ArrayList<String>();

            if (sublDatMap.containsKey(maxCd)) {
                // ATTEND_DAY_SUBL_DATは使用無
                final TreeMap<Integer, List<String>> submDatMap = sublDatMap.get(maxCd);
                stbs.add(remark(submDatMap, maxCd, "").toString());
            } else {
                for (final Integer sublCd : sublDatMap.keySet()) {
                    if (null == sublCd) {
                        continue;
                    }
                    final String sublname = param._nameMstC006.get(sublCd.toString()); // SUBL_CDの名称
                    final TreeMap<Integer, List<String>> submDatMap = sublDatMap.get(sublCd);
                    if (submDatMap.containsKey(maxCd)) { // ATTEND_DAY_SUBM_DATは使用無
                        stbs.add(remark(submDatMap, maxCd, sublname).toString());
                    } else {
                        for (final Integer submCd : submDatMap.keySet()) {
                            if (null == submCd) {
                                continue;
                            }
                            final String submname = param._nameMstC007.get(submCd.toString()); // SUBM_CDの名称
                            stbs.add(remark(submDatMap, submCd, submname).toString());
                        }
                    }
                }
            }
            return mkString(stbs, comma);
        }

        private static String shortDate(final String date) {
            if (null == date) {
                return "";
            }
            final Calendar cal = getCalendar(date);
            return String.valueOf(cal.get(Calendar.MONTH) + 1) + "/" + String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
        }

        private static StringBuffer remark(final Map subDatMap, final Integer key, final String name) {
            final StringBuffer stb = new StringBuffer();
            final List<String> dayList = (List) subDatMap.get(key);
            if (null != dayList && dayList.size() >= 1) {
                stb.append(dayList.size());
                if (!"".equals(name)) {
                    stb.insert(0, "（").append("）");
                }
            }
            stb.insert(0, name);
            return stb;
        }

        public static List<Student> getStudentList(final DB2UDB db2, final Param param) {
            final List<Student> studentList = new ArrayList<Student>();
            final String studentSql = getStudentSql(param);
            if (param._isOutputDebug) {
                log.info(" sqlAttendDayDat =" + studentSql);
            }

            final String entGrdCheckSdate = param._ctrlYear + "-04-01";
            final String entGrdCheckEdate = String.valueOf(1 + Integer.parseInt(param._ctrlYear)) + "-03-31";

            for (final Map<String, String> row : KnjDbUtils.query(db2, studentSql)) {
                final String schregno = KnjDbUtils.getString(row, "SCHREGNO");

                final String entDiv = KnjDbUtils.getString(row, "ENT_DIV");
                final String grdDiv = KnjDbUtils.getString(row, "GRD_DIV");
                String entDivName = null;
                String grdDivName = null;
                final String entDate = KnjDbUtils.getString(row, "ENT_DATE");
                final String grdDate = KnjDbUtils.getString(row, "GRD_DATE");
                final String tengakuSakiZenjitu = KnjDbUtils.getString(row, "TENGAKU_SAKI_ZENJITU");
                final String gradeName1 = KnjDbUtils.getString(row, "GRADE_NAME1");
                final String coursename = KnjDbUtils.getString(row, "COURSENAME");
                final String schoolname = KnjDbUtils.getString(row, "SCHOOLNAME");
                if (null != entDiv && Student.between(entDate, entGrdCheckSdate, entGrdCheckEdate)) {
                    entDivName = KnjDbUtils.getString(row, "ENT_DIV_NAME");
                }
                if (null != grdDiv && Student.between(grdDate, entGrdCheckSdate, entGrdCheckEdate)) {
                    grdDivName = KnjDbUtils.getString(row, "GRD_DIV_NAME");
                }
                final boolean isHoumonsei = "1".equals(KnjDbUtils.getString(row, "BYDM004REMARK2"));
                final String gradeHrClass = KnjDbUtils.getString(row, "GRADE") + KnjDbUtils.getString(row, "HR_CLASS");
                final String attendno = null == KnjDbUtils.getString(row, "ATTENDNO") ? "1" : KnjDbUtils.getString(row, "ATTENDNO");
                final Student student = new Student(schregno, KnjDbUtils.getString(row, "NAME"), gradeHrClass, attendno, KnjDbUtils.getString(row, "SCHOOL_KIND"), entDiv, entDivName, entDate, grdDiv, grdDivName, grdDate, tengakuSakiZenjitu, gradeName1, coursename, schoolname, isHoumonsei);

                // DIV6_3、6_4は訪問生のみの出力
                if(FORM_DIV6_3.equals(param._formDiv) || FORM_DIV6_4.equals(param._formDiv)) {
                    if(!student._isHoumonsei) continue;
                }
                studentList.add(student);
            }
            return studentList;
        }

        private static String getStudentSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT DISTINCT ");
            if (param._isGhr) {
                stb.append("     T1.SCHREGNO, ");
                stb.append("     BASE.NAME, ");
                stb.append("     T1.GHR_ATTENDNO AS ATTENDNO, ");
            } else if (param._isGakunenKongou) {
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
            stb.append("     EGHIST.ENT_DIV, ");
            stb.append("     EGHIST.GRD_DIV, ");
            stb.append("     GDAT.SCHOOL_KIND, ");
            stb.append("     GDAT.GRADE_NAME1, ");
            stb.append("     COURSE_M.COURSENAME, ");
            stb.append("     SCHOOL_M.SCHOOLNAME1 AS SCHOOLNAME, ");
            stb.append("     NMA002.NAME1 AS ENT_DIV_NAME, ");
            stb.append("     NMA003.NAME1 AS GRD_DIV_NAME, ");
            stb.append("     VALUE(EGHIST.ENT_DATE, '1900-01-01') AS ENT_DATE, ");
            stb.append("     VALUE(EGHIST.GRD_DATE, '9999-12-31') AS GRD_DATE, ");
            stb.append("     VALUE(EGHIST.TENGAKU_SAKI_ZENJITU, '9999-12-31') AS TENGAKU_SAKI_ZENJITU, ");
            stb.append("     BYDM004.BASE_REMARK2 AS BYDM004REMARK2");
            if (param._isFi) {
                stb.append(" FROM SCHREG_REGD_FI_DAT T1 ");
                stb.append("     LEFT JOIN SCHREG_REGD_DAT REGD ON REGD.SCHREGNO = T1.SCHREGNO AND REGD.YEAR = T1.YEAR AND REGD.SEMESTER = T1.SEMESTER ");
            } else if (param._isGhr) {
                stb.append(" FROM SCHREG_REGD_GHR_DAT T1 ");
                stb.append("     LEFT JOIN SCHREG_REGD_DAT REGD ON REGD.SCHREGNO = T1.SCHREGNO AND REGD.YEAR = T1.YEAR AND REGD.SEMESTER = T1.SEMESTER ");
            } else if (param._isGakunenKongou) {
                stb.append(" FROM V_STAFF_HR_DAT T1 ");
                stb.append("     LEFT JOIN SCHREG_REGD_HDAT HD ON HD.YEAR = T1.YEAR AND HD.SEMESTER = T1.SEMESTER AND HD.GRADE = T1.GRADE AND HD.HR_CLASS = T1.HR_CLASS ");
                stb.append("     LEFT JOIN SCHREG_REGD_DAT REGD ON REGD.YEAR = T1.YEAR AND REGD.SEMESTER = T1.SEMESTER AND REGD.GRADE = HD.GRADE AND REGD.HR_CLASS = HD.HR_CLASS ");
            } else {
                stb.append(" FROM SCHREG_REGD_DAT T1 ");
                stb.append("     LEFT JOIN SCHREG_REGD_DAT REGD ON REGD.SCHREGNO = T1.SCHREGNO AND REGD.YEAR = T1.YEAR AND REGD.SEMESTER = T1.SEMESTER ");
            }
            stb.append("     LEFT JOIN COURSE_MST COURSE_M ON COURSE_M.COURSECD = REGD.COURSECD ");
            stb.append("     LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = REGD.YEAR AND GDAT.GRADE = REGD.GRADE ");
            stb.append("     LEFT JOIN SCHOOL_MST SCHOOL_M ON SCHOOL_M.YEAR = GDAT.YEAR ");
            if (param._hasSCHOOL_MST_SCHOOL_KIND) {
                stb.append("     AND SCHOOL_M.SCHOOL_KIND = GDAT.SCHOOL_KIND ");
            }
            stb.append("     LEFT JOIN SCHREG_ENT_GRD_HIST_DAT EGHIST ON EGHIST.SCHREGNO = REGD.SCHREGNO AND EGHIST.SCHOOL_KIND = GDAT.SCHOOL_KIND ");
            stb.append("     INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ");
            stb.append("     LEFT JOIN NAME_MST NMA002 ON NMA002.NAMECD1 = 'A002' AND NMA002.NAMECD2 = EGHIST.ENT_DIV ");
            stb.append("     LEFT JOIN NAME_MST NMA003 ON NMA003.NAMECD1 = 'A003' AND NMA003.NAMECD2 = EGHIST.GRD_DIV ");
            stb.append("     LEFT JOIN SCHREG_BASE_YEAR_DETAIL_MST BYDM004 ON BYDM004.SCHREGNO = REGD.SCHREGNO AND BYDM004.YEAR = REGD.YEAR AND BYDM004.BASE_SEQ = '004' ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._ctrlYear + "' ");
            stb.append("     AND T1.SEMESTER = '" + param._targetSemes + "' ");
            if (param._isGhr) {
                stb.append("     AND T1.GHR_CD = '" + param._gradeHrclass + "' ");
            } else if (param._isGakunenKongou) {
                stb.append("     AND T1.SCHOOL_KIND || '-' || T1.HR_CLASS = '" + param._gradeHrclass + "' ");
                if ("1".equals(param._restrictFlg)) {
                    stb.append("     AND T1.STAFFCD = '" + param._printLogStaffcd + "' ");
                }
            } else {
                stb.append("     AND T1.GRADE || T1.HR_CLASS = '" + param._gradeHrclass + "' ");
            }
            stb.append(" ORDER BY ");
            if (param._isGhr) {
                stb.append("     T1.GHR_ATTENDNO ");
            } else if (param._isGakunenKongou) {
                stb.append("     REGD.GRADE, ");
                stb.append("     REGD.HR_CLASS, ");
                stb.append("     REGD.ATTENDNO ");
            } else {
                stb.append("     T1.ATTENDNO ");
            }
            return stb.toString();
        }

        public String toString() {
            return "Student(" + _gradeHrclass + "-" + _attendno + ", " + _schregno + ", " + _name + ", ent=(" + _entDiv + ", " + _entDate + "), grd = (" + _grdDiv + ", " + _grdDate  + "))";
        }

        public int houmonSyussekiCnt() {
            int cnt = 0;
            for(String date : _attendDayDatMap.keySet()) {
                final AttendDayDat attendDayDat =_attendDayDatMap.get(date);
                if (attendDayDat._diCdMarkMap.containsKey(0)) {
                    cnt++;
                }
            }
            return cnt;
        }
    }

    private static class AttendDayDat {

        final String _schregno;
        final String _date;
        final TreeMap<Integer, String> _diCdMarkMap = new TreeMap<Integer, String>(); // 基本的に要素の数は1個。遅刻と早退の場合のみ2個。
        AttendDayDat(final String schregno, final String date) {
            _schregno = schregno;
            _date = date;
        }
        public boolean isShusseki() {
            if (_diCdMarkMap.isEmpty()) {
                return true;
            }
            if (_diCdMarkMap.size() == 2 && (_diCdMarkMap.containsKey(dicdLate) && _diCdMarkMap.containsKey(dicdEarly))) {
                return true;
            }
            if (_diCdMarkMap.size() >= 2) {
                log.info(" unknown diCd state : " + _diCdMarkMap);
                return true;
            }
            final Integer diCd = _diCdMarkMap.keySet().iterator().next();
            final Integer[] shussekiDicds = {
                    //dicdSuspend, dicdMourning, dicdOnedaySuspend, dicdOnedayMourning,
                    //dicdSick, dicdNotice, dicdNonotice, dicdOnedaySick, dicdOnedayNotice, dicdOnedayNonotice,
                    dicdLate, dicdEarly, dicdLate2, dicdLate3,
                    //dicdVirus, dicdOnedayVirus, dicdKoudome, dicdOnedayKoudome,
            };
            return ArrayUtils.contains(shussekiDicds, diCd);
        }
        public boolean isKesseki() {
            if (_diCdMarkMap.isEmpty()) {
                return false;
            }
            if (_diCdMarkMap.size() == 2 && (_diCdMarkMap.containsKey(dicdLate) && _diCdMarkMap.containsKey(dicdEarly))) {
                return false;
            }
            if (_diCdMarkMap.size() >= 2) {
                log.info(" unknown diCd state : " + _diCdMarkMap);
                return true;
            }
            final Integer diCd = _diCdMarkMap.keySet().iterator().next();
            final Integer[] kessekiDicds = {
                    //dicdSuspend, dicdMourning, dicdOnedaySuspend, dicdOnedayMourning,
                    dicdSick, dicdNotice, dicdNonotice, dicdOnedaySick, dicdOnedayNotice, dicdOnedayNonotice,
                    //dicdLate, dicdEarly, dicdLate2, dicdLate3,
                    dicdVirus, dicdOnedayVirus, dicdKoudome, dicdOnedayKoudome,
            };
            return ArrayUtils.contains(kessekiDicds, diCd);
        }
        public void setDiCdMark(final Integer diCd, final String mark) {
            _diCdMarkMap.put(diCd, mark);
        }
        public String getDiMark(final Param param) {
            final String rtn;
            if (_diCdMarkMap.containsKey(dicdLate) && _diCdMarkMap.containsKey(dicdEarly)) {
                if (FORM_DIV4.equals(param._formDiv) || FORM_DIV5.equals(param._formDiv)) {
                    rtn = param._nameMstC00801.get("NAME1");
                } else {
                    rtn = param._nameMstC00801.get("ABBV1");
                }
            } else if (!_diCdMarkMap.isEmpty()) {
                rtn = _diCdMarkMap.get(_diCdMarkMap.firstKey());
            } else {
                rtn = "";
            }
            if (param._isOutputDebug) {
                log.info("schregno = " + _schregno + ", date = " + _date + ", diCdMap = " + _diCdMarkMap + " rtn = " + rtn);
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
        private int suspendMourning() {
            return _suspend + _virus + _koudome + _mourning;
        }
        private int jugyoJisu() {
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

    private static class StampData {
        private final String _seq;
        private final String _title;
        private final String _stampName;
        private final String _fileName;
        public StampData(
                final String seq,
                final String title,
                final String stampName,
                final String fileName
                ) {
            _seq = seq;
            _title = title;
            _stampName = stampName;
            _fileName = fileName;
        }
    }

    private static class MonthAttendTopData implements Comparable<MonthAttendTopData> {
        private final Integer _seq;
        private final String _diCd;
        private final String _type;
        private final String _abTotal;
        private final String _schregNo;
        private final String _name;
        private final String _remark;
        private final String _guardName;
        private final String _treatment;
        public MonthAttendTopData(
                final int seq,
                final String diCd,
                final String type,
                final String abTotal,
                final String schregNo,
                final String name,
                final String remark,
                final String guardName,
                final String treatment
                ) {
            _seq = new Integer(seq);
            _diCd = diCd;
            _type = type;
            _abTotal = abTotal;
            _schregNo = schregNo;
            _name = name;
            _remark = remark;
            _guardName = guardName;
            _treatment = treatment;
        }
        public int compareTo(final MonthAttendTopData omatd) {
            int rtn;
            final Integer abTotal1 = !NumberUtils.isDigits(_abTotal) ? new Integer(-1) : Integer.valueOf(_abTotal);
            final Integer abTotal2 = !NumberUtils.isDigits(omatd._abTotal) ? new Integer(-1) : Integer.valueOf(omatd._abTotal);
            rtn = - abTotal1.compareTo(abTotal2); // 降順
            if (rtn != 0) { return rtn; }
            rtn = _schregNo.compareTo(omatd._schregNo);
            if (rtn != 0) { return rtn; }
            rtn = _seq.compareTo(omatd._seq);
            return rtn;
        }
    }

    private static class RemarkOption {
        boolean _addAttendSemesRemarkDatRemark1 = false;
        boolean _addEntGrd = false;
        boolean _addAbsenreRemark = false;
        boolean _addDicdDetail = false;
    }

    /** パラメータ取得処理 */
    private static Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
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
        final String _formSelect;  // 1:15行で印刷
        final String _formDiv;  // 1:A3横、2:A4縦、3:A3縦/A4横(出席統計有)
        final String _printZaisekiOnly; // 1:未在籍者は詰めて印字する
        final String _knjs340FORM_DIV;  // 帳票タイプ6の切り分け用

        final String _semestername;
        final String _hrname;
        final String _staffname;
        final boolean _seirekiFlg;
        final String _nendo;
        final String _ctrlDateFormatted;
        final String _monthRange;
        final String _yearMonthE;
        final String _z010Name1;
        final TreeMap<String, PrintDay> _dates = new TreeMap<String, PrintDay>();
        final Map<String, TreeMap<String, PrintDay>> _schoolKindDates = new HashMap<String, TreeMap<String, PrintDay>>();
        final String _gradeHrclassSchoolkind;
        final String _C001;
        final String _C006;
        final String _C007;
        final String _C008;
        final Map<String, String> _nameMstC001Name1;
        final Map<String, String> _nameMstC001Abbv1;
        final String[] _nameMstC001Abbv1Remark;
        final Map<String, String> _nameMstC006;
        final Map<String, String> _nameMstC007;
        final Map<String, String> _nameMstC00801;
        final TreeSet<Integer> _sickNoticeNonoticeCds;
        final Map<String, Map<String, String>> _schoolMstMap;
        final Map<String, List<Map<String, String>>> _schoolKindVacationRowList;

        String _monthSdate = null;
        String _monthEdate = null;
        int _attendSemesAppointedDay = 0;
        int _attendDayDatSday = 0;
        final boolean _isMusashinohigashi;
        final boolean _isHibarigaoka;
        boolean _hasEVENT_DAT_HR_CLASS_DIV;
        boolean _isFi = false;
        boolean _isGhr = false;
        boolean _isGakunenKongou = false;
        boolean _isHoutei = false;
        final boolean _isTokubetsuShien;
        final boolean _hasSCHREG_ENT_GRD_HIST_COMEBACK_DAT;
        final boolean _hasSCHOOL_MST_SCHOOL_KIND;
        final boolean _hasAPPOINTED_DAY_MST_SCHOOL_KIND;
        final boolean _hasEVENT_DAT_EVENT_ABBV;
        final boolean _notPrintEntGrdRemarkGrddiv1;
        final boolean _notPrintSyussekiSubekiNinzu;
        final boolean _notPrintSyussekiNinzu;
        final boolean _isOutputDebug;
        final boolean _isOutputDebugField;
        final boolean _isOutputDebugFieldAll;

        Map<String, StampData> _stampMap;

        private final String _documentRoot;
        private final String _imageDir;
        private final String _imageExt;
        private final String _schoolKind;

        private final RemarkOption _remarkOption = new RemarkOption();
        private final Map<String, PreparedStatement> _psMap;
        final String _whiteSpaceImagePath;
        final String _imagepath;
        private final List<File> _createdFiles = new ArrayList<File>();

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
            _printZaisekiOnly = request.getParameter("PRINT_ZAISEKI_ONLY");
            _knjs340FORM_DIV = request.getParameter("knjs340FORM_DIV");

            if(FORM_DIV6.equals(_knjs340FORM_DIV)) {
                String div = request.getParameter("FORM_DIV");
                _formDiv = "1".equals(div) ? FORM_DIV6_1 : "2".equals(div) ? FORM_DIV6_2 : "3".equals(div) ? FORM_DIV6_3 : FORM_DIV6_4;
            } else {
                _formDiv = request.getParameter("FORM_DIV");
            }

            _hasSCHOOL_MST_SCHOOL_KIND = KnjDbUtils.setTableColumnCheck(db2, "SCHOOL_MST", "SCHOOL_KIND");
            _hasAPPOINTED_DAY_MST_SCHOOL_KIND = KnjDbUtils.setTableColumnCheck(db2, "APPOINTED_DAY_MST", "SCHOOL_KIND");
            _hasEVENT_DAT_EVENT_ABBV = KnjDbUtils.setTableColumnCheck(db2, "EVENT_DAT", "EVENT_ABBV");

            _isTokubetsuShien = "1".equals(request.getParameter("useSpecial_Support_Hrclass"));
            final String[] outputDebug = StringUtils.split(getDbPrginfoProperties(db2, "outputDebug"));
            _isOutputDebug = ArrayUtils.contains(outputDebug, "1");
            _isOutputDebugFieldAll = ArrayUtils.contains(outputDebug, "fieldAll");
            _isOutputDebugField = _isOutputDebugFieldAll || ArrayUtils.contains(outputDebug, "field");
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

            _semestername = getSemestername(db2);
            _hrname = getHrname(db2);
            _seirekiFlg = getSeirekiFlg(db2);
            _ctrlDateFormatted = formatDate(this, db2, _ctrlDate);
            setDates(db2, _dates);
            final String nendo = _ctrlYear + "-06-01"; //令和元年を表示するため
            _nendo = _seirekiFlg ? _ctrlYear + "年度" : (KNJ_EditDate.h_format_JP_N(db2, nendo) + "度");
            _monthRange = formatDate(this, db2, _monthSdate) + "～" + formatDate(this, db2, _monthEdate);
            _yearMonthE = _seirekiFlg ? _monthEdate.substring(0, 4) + "年" + KNJ_EditDate.h_format_S(_monthEdate, "M") + "月" : KNJ_EditDate.h_format_JP_M(db2, _monthEdate);
            _staffname = getStaffname(db2);
            _hasEVENT_DAT_HR_CLASS_DIV = KnjDbUtils.setTableColumnCheck(db2, "EVENT_DAT", "HR_CLASS_DIV");
            _hasSCHREG_ENT_GRD_HIST_COMEBACK_DAT = KnjDbUtils.setTableColumnCheck(db2, "SCHREG_ENT_GRD_HIST_COMEBACK_DAT", null);
            _z010Name1 = (String) getNameMstRecord(db2, "Z010", "00").get("NAME1");
            setEventDatHolidayFlg(db2, _dates, _schoolKindDates);
            if (_isGhr || _isGakunenKongou) {
                _C001 = "C001";
                _C006 = "C006";
                _C007 = "C007";
                _C008 = "C008";
                _gradeHrclassSchoolkind = null;
            } else {
                // 名称マスタは校種があれば校種を優先する
                _gradeHrclassSchoolkind = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _ctrlYear + "' AND GRADE = '" + _gradeHrclass.substring(0, 2) + "' "));
                _C001 =  getNameCd1(db2,"C001");
                _C006 =  getNameCd1(db2,"C006");
                _C007 =  getNameCd1(db2,"C007");
                _C008 =  getNameCd1(db2,"C008");
            }
            _nameMstC001Name1 = getNameMstMap(db2, _C001, "NAME1");
            _nameMstC001Abbv1 = getNameMstMap(db2, _C001, "ABBV1");
            _nameMstC00801 = getNameMstRecord(db2, _C008, "01");
            _nameMstC001Abbv1Remark = getC001Abbv1Remark1(_nameMstC001Name1, _nameMstC001Abbv1);
            _nameMstC006 = getNameMstMap(db2, _C006, "NAME1");
            _nameMstC007 = getNameMstMap(db2, _C007, "NAME1");
            _sickNoticeNonoticeCds = new TreeSet();
            if (_sickNoticeNonoticeCds.size() < 2 && _nameMstC001Name1.containsKey(dicdSick.toString())) _sickNoticeNonoticeCds.add(dicdSick);
            if (_sickNoticeNonoticeCds.size() < 2 && _nameMstC001Name1.containsKey(dicdNotice.toString())) _sickNoticeNonoticeCds.add(dicdNotice);
            if (_sickNoticeNonoticeCds.size() < 2 && _nameMstC001Name1.containsKey(dicdNonotice.toString())) _sickNoticeNonoticeCds.add(dicdNonotice);
            if (_isOutputDebug) {
                log.info(" sickNoticeNonoticeCds = " + _sickNoticeNonoticeCds);
            }
            log.info(" z010 name1 = " + _z010Name1);
            setAttendSemesDayDat(db2);
            _isMusashinohigashi = "musashinohigashi".equals(_z010Name1);
            _isHibarigaoka      = "hibarigaoka".equals(_z010Name1);
            _notPrintEntGrdRemarkGrddiv1 = _isMusashinohigashi;
            _notPrintSyussekiSubekiNinzu = _isHibarigaoka && ("H".equals(_gradeHrclassSchoolkind) || "J".equals(_gradeHrclassSchoolkind)); // 雲雀丘の中高は出席すべき人数を表示しない
            _notPrintSyussekiNinzu = _isHibarigaoka && ("H".equals(_gradeHrclassSchoolkind) || "J".equals(_gradeHrclassSchoolkind)); // 雲雀丘の中高は出席人数を表示しない
            _documentRoot = request.getParameter("DOCUMENTROOT"); // img保管場所 NO001
            _imageDir = "image";
            _imageExt = "jpg";
            if (FORM_DIV4.equals(_formDiv) || FORM_DIV5.equals(_formDiv) || FORM_DIV6.equals(_knjs340FORM_DIV)) {
                _stampMap = getStampData(db2);
            } else {
                //利用しないパターンでは、空で作成しておく。
                _stampMap = new LinkedMap();
            }
            _schoolKind = request.getParameter("SCHOOL_KIND");

            if (_hasSCHOOL_MST_SCHOOL_KIND) {
                _schoolMstMap = KnjDbUtils.getKeyMap(KnjDbUtils.query(db2, "SELECT T1.* FROM SCHOOL_MST T1 WHERE T1.YEAR = '" + _ctrlYear + "' "), "SCHOOL_KIND");
            } else {
                _schoolMstMap = KnjDbUtils.getKeyMap(KnjDbUtils.query(db2, "SELECT '00' AS SCHOOL_KIND, T1.* FROM SCHOOL_MST T1 WHERE T1.YEAR = '" + _ctrlYear + "' "), "SCHOOL_KIND");
            }

            if (FORM_DIV3.equals(_formDiv)) {
                _remarkOption._addEntGrd = true;
                _remarkOption._addDicdDetail = true;
            } else if (FORM_DIV4.equals(_formDiv) || FORM_DIV5.equals(_formDiv) || FORM_DIV6.equals(_knjs340FORM_DIV)) {
                _remarkOption._addAttendSemesRemarkDatRemark1 = true;
                _remarkOption._addDicdDetail = true;
            } else if (_isTokubetsuShien) {
                _remarkOption._addAttendSemesRemarkDatRemark1 = true;
            } else if (_isMusashinohigashi) {
                _remarkOption._addAttendSemesRemarkDatRemark1 = true;
                _remarkOption._addEntGrd = true;
                _remarkOption._addAbsenreRemark = true;
            } else {
                _remarkOption._addAttendSemesRemarkDatRemark1 = true;
                _remarkOption._addDicdDetail = true;
            }
            _schoolKindVacationRowList = getSchoolKindVacationRowList(db2);
            if (_isOutputDebug) {
                log.info(" attendRemark addRemark = " + _remarkOption._addAttendSemesRemarkDatRemark1 + ", addEntGrd = " + _remarkOption._addEntGrd + ", addAbsenceRemark = " + _remarkOption._addAbsenreRemark + ", addDicdDetail = " + _remarkOption._addDicdDetail);
            }

            _psMap = new HashMap<String, PreparedStatement>();

            final String sqlControlMst = " SELECT IMAGEPATH FROM CONTROL_MST WHERE CTRL_NO = '01' ";
            _imagepath = KnjDbUtils.getOne(KnjDbUtils.query(db2, sqlControlMst));
            _whiteSpaceImagePath = getImageFilePath("whitespace.png");
        }

        private Map<String, List<Map<String, String>>> getSchoolKindVacationRowList(final DB2UDB db2) {
            final Map<String, List<Map<String, String>>> schoolKindVacationRowList = new TreeMap<String, List<Map<String, String>>>();
            String sql = "";
            sql += " SELECT SCHOOL_KIND, BEFORE_SPRING_VACATION_SDATE AS SDATE, BEFORE_SPRING_VACATION_EDATE AS EDATE ";
            sql += "      , VALUE((SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'S001' AND NAMECD2 = '01'), '春休み（始業式前）') AS NAME ";
            sql += "   FROM HOLIDAY_BASE_MST ";
            sql += "   WHERE YEAR = '" + _ctrlYear + "' ";
            sql += "  UNION ALL ";
            sql += " SELECT SCHOOL_KIND, SUMMER_VACATION_SDATE AS SDATE, SUMMER_VACATION_EDATE AS EDATE ";
            sql += "      , VALUE((SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'S001' AND NAMECD2 = '02'), '夏休み') AS NAME ";
            sql += "   FROM HOLIDAY_BASE_MST  ";
            sql += "   WHERE YEAR = '" + _ctrlYear + "' ";
            sql += "  UNION ALL ";
            sql += " SELECT SCHOOL_KIND, AUTUMN_VACATION_SDATE AS SDATE, AUTUMN_VACATION_EDATE AS EDATE ";
            sql += "      , VALUE((SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'S001' AND NAMECD2 = '03'), '秋休み') AS NAME ";
            sql += "   FROM HOLIDAY_BASE_MST  ";
            sql += "   WHERE YEAR = '" + _ctrlYear + "' ";
            sql += "  UNION ALL ";
            sql += " SELECT SCHOOL_KIND, WINTER_VACATION_SDATE AS SDATE, WINTER_VACATION_EDATE AS EDATE ";
            sql += "      , VALUE((SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'S001' AND NAMECD2 = '04'), '冬休み') AS NAME ";
            sql += "   FROM HOLIDAY_BASE_MST  ";
            sql += "   WHERE YEAR = '" + _ctrlYear + "' ";
            sql += "  UNION ALL ";
            sql += " SELECT SCHOOL_KIND, AFTER_SPRING_VACATION_SDATE AS SDATE, AFTER_SPRING_VACATION_EDATE AS EDATE ";
            sql += "      , VALUE((SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'S001' AND NAMECD2 = '05'), '春休み（終了式後）') AS NAME ";
            sql += "   FROM HOLIDAY_BASE_MST ";
            sql += "   WHERE YEAR = '" + _ctrlYear + "' ";
            log.info(" vacation sql = " + sql);
            for (final Map<String, String> row : KnjDbUtils.query(db2, sql)) {
                getMappedList(schoolKindVacationRowList, KnjDbUtils.getString(row, "SCHOOL_KIND")).add(row);
            }
            return schoolKindVacationRowList;
        }

        private Map<String, String> getSchoolMst(final String schoolKind) {
            if (!_hasSCHOOL_MST_SCHOOL_KIND) {
                return _schoolMstMap.get("00");
            }
            return _schoolMstMap.get(schoolKind);
        }

        private PreparedStatement getPs(final String psKey) {
            return _psMap.get(psKey);
        }

        private void setPs(final DB2UDB db2, final String psKey, final String sql) {
            try {
                _psMap.put(psKey, db2.prepareStatement(sql));
            } catch (Exception e) {
                log.error("exception!", e);
            }
        }

        private void close() {
            for (final PreparedStatement ps : _psMap.values()) {
                DbUtils.closeQuietly(ps);
            }
            for (final File file : _createdFiles) {
                log.info(" delete file " + file.getAbsolutePath() + ":" + file.delete());
            }
        }

        private static String getDbPrginfoProperties(final DB2UDB db2, final String propName) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJS340' AND NAME = '" + propName + "' "));
        }

        private static String getString(final Map map, final Object cd) {
            return (String) map.get(cd);
        }
        private String[] getC001Abbv1Remark1(final Map nameMstC001, final Map nameMstC001Abbv1) {
            final StringBuffer stb1 = new StringBuffer();
            String comma1 = "", space = "  ";
            final Integer[] cds = {dicdMourning, dicdSuspend, dicdSick, dicdNotice, dicdNonotice, dicdLate, dicdEarly};
            for (int i = 0; i < cds.length; i++) {
                if (null != nameMstC001.get(cds[i].toString()) && null != nameMstC001Abbv1.get(cds[i].toString())) {
                    stb1.append(comma1).append(getString(nameMstC001, cds[i].toString()) + "・・・" + getString(nameMstC001Abbv1, cds[i].toString()));
                    comma1 = space;
                }
            }
            if (null != nameMstC001.get(dicdLate.toString()) && null != nameMstC001.get(dicdEarly.toString()) && null != _nameMstC00801) {
                stb1.append(comma1).append(getString(nameMstC001, dicdLate.toString()) + "・" + getString(nameMstC001, dicdEarly.toString()) + "・・・" + _nameMstC00801.get("ABBV1"));
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

        private void setDates(final DB2UDB db2, final TreeMap<String, PrintDay> dates) {

            final String monthStartDay = (Integer.parseInt(_ctrlYear) + (Integer.parseInt(_targetMonth) < 4 ? 1 : 0)) + "-" + _targetMonth + "-01";
            final Calendar calSemesSdate = getCalendar(monthStartDay);
            final Calendar calSemesEdate = getLastDayOfMonth(monthStartDay);

            _monthSdate = getDateString(calSemesSdate);
            _monthEdate = getDateString(calSemesEdate);

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
            final List<String> months = KnjDbUtils.getColumnDataList(KnjDbUtils.query(db2, stb.toString()), "MONTH");
            final boolean checkSemester = months.contains(_targetMonth);  // 指定月が学期によって分かれているか

            if (checkSemester) {
                final String sql = " SELECT "
                        + " CASE WHEN T1.SDATE < '" + _monthSdate + "' THEN '" + _monthSdate + "' ELSE T1.SDATE END AS SDATE, "
                        + " CASE WHEN T1.EDATE > '" + _monthEdate + "' THEN '" + _monthEdate + "' ELSE T1.EDATE END AS EDATE "
                        + " FROM SEMESTER_MST T1 WHERE T1.SEMESTER = '" + _targetSemes + "' AND  T1.YEAR = '" + _ctrlYear + "' ";
                log.debug(" sql ="  + sql);
                final Map<String, String> row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql));
                if (null != KnjDbUtils.getString(row, "SDATE")) {
                    _monthSdate = KnjDbUtils.getString(row, "SDATE");
                }
                if (null != KnjDbUtils.getString(row, "EDATE")) {
                    _monthEdate = KnjDbUtils.getString(row, "EDATE");
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

        private void setEventDatHolidayFlg(final DB2UDB db2, final TreeMap<String, PrintDay> datesOrigin, final Map<String, TreeMap<String, PrintDay>> schoolKindDates) {
            final String minDate = datesOrigin.firstKey();
            final String maxDate = datesOrigin.lastKey();

            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH MAIN AS( ");
            if (_isGhr || _isGakunenKongou) {
                    stb.append(" SELECT ");
                    stb.append("   T2.SCHOOL_KIND, T1.GRADE, T1.HR_CLASS, T1.EXECUTEDATE, T1.HOLIDAY_FLG, VALUE(T1.REMARK1, '') AS REMARK1, ");
                    if (_hasEVENT_DAT_EVENT_ABBV) {
                        stb.append("   VALUE(CASE WHEN HOLIDAY_FLG = '1' THEN T1.EVENT_ABBV END, '') AS EVENT_ABBV ");
                    } else {
                        stb.append("   '' AS EVENT_ABBV ");
                    }
                    stb.append(" FROM ");
                    stb.append("   EVENT_DAT T1 ");
                    stb.append("   LEFT JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = '" + _ctrlYear + "' AND T2.GRADE = T1.GRADE ");
                    stb.append(" WHERE ");
                    stb.append("   T1.EXECUTEDATE BETWEEN '" + minDate + "' AND '" + maxDate + "' ");
                    stb.append(" AND (T1.GRADE, T1.HR_CLASS) IN (");
                    if (_isGhr) {
                        stb.append(" SELECT REGD.GRADE, REGD.HR_CLASS ");
                        stb.append(" FROM SCHREG_REGD_GHR_DAT T1 ");
                        stb.append("     LEFT JOIN SCHREG_REGD_DAT REGD ON REGD.SCHREGNO = T1.SCHREGNO AND REGD.YEAR = T1.YEAR AND REGD.SEMESTER = T1.SEMESTER ");
                        stb.append(" WHERE ");
                        stb.append("     T1.YEAR = '" + _ctrlYear + "' ");
                        stb.append("     AND T1.SEMESTER = '" + _targetSemes + "' ");
                        stb.append("     AND T1.GHR_CD = '" + _gradeHrclass + "' ");

                    } else { // if (_isGakunenKongou) {
                        stb.append(" SELECT REGD.GRADE, REGD.HR_CLASS ");
                        stb.append(" FROM V_STAFF_HR_DAT T1 ");
                        stb.append("     LEFT JOIN SCHREG_REGD_HDAT HD ON HD.YEAR = T1.YEAR AND HD.SEMESTER = T1.SEMESTER AND HD.GRADE = T1.GRADE AND HD.HR_CLASS = T1.HR_CLASS ");
                        stb.append("     LEFT JOIN SCHREG_REGD_DAT REGD ON REGD.YEAR = T1.YEAR AND REGD.SEMESTER = T1.SEMESTER AND REGD.GRADE = HD.GRADE AND REGD.HR_CLASS = HD.HR_CLASS ");
                        stb.append(" WHERE ");
                        stb.append("     T1.YEAR = '" + _ctrlYear + "' ");
                        stb.append("     AND T1.SEMESTER = '" + _targetSemes + "' ");
                        stb.append("     AND T1.SCHOOL_KIND || '-' || T1.HR_CLASS = '" + _gradeHrclass + "' ");
                        if ("1".equals(_restrictFlg)) {
                            stb.append("     AND T1.STAFFCD = '" + _printLogStaffcd + "' ");
                        }
                    }
                    stb.append(" ) ");
            } else {
                stb.append(" SELECT ");
                stb.append("   T2.SCHOOL_KIND, T1.GRADE, T1.HR_CLASS, T1.EXECUTEDATE, T1.HOLIDAY_FLG, VALUE(T1.REMARK1, '') AS REMARK1, ");
                if (_hasEVENT_DAT_EVENT_ABBV) {
                    stb.append("   VALUE(CASE WHEN HOLIDAY_FLG = '1' THEN T1.EVENT_ABBV END, '') AS EVENT_ABBV ");
                } else {
                    stb.append("   '' AS EVENT_ABBV ");
                }
                stb.append(" FROM ");
                stb.append("   EVENT_DAT T1 ");
                stb.append("   LEFT JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = '" + _ctrlYear + "' AND T2.GRADE = T1.GRADE ");
                stb.append(" WHERE ");
                stb.append("   T1.EXECUTEDATE BETWEEN '" + minDate + "' AND '" + maxDate + "' ");
                stb.append("   AND T1.GRADE || T1.HR_CLASS = '" + _gradeHrclass + "' ");
                if (_hasEVENT_DAT_HR_CLASS_DIV) {
                    if (_isFi) {
                        stb.append("   AND T1.HR_CLASS_DIV = '2' ");
                    } else {
                        stb.append("   AND T1.HR_CLASS_DIV = '1' ");
                    }
                }
            }
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("   SCHOOL_KIND, ");
            stb.append("   GRADE, ");
            stb.append("   HR_CLASS, ");
            stb.append("   EXECUTEDATE, ");
            stb.append("   HOLIDAY_FLG, ");
            stb.append("   VALUE(REMARK1, '') AS REMARK1, ");
            stb.append("   VALUE(EVENT_ABBV, '') AS EVENT_ABBV ");
            stb.append(" FROM MAIN ");
            if(FORM_DIV5.equals(_formDiv)) {
                //祝日の取得
                stb.append(" UNION ");
                stb.append(" SELECT ");
                stb.append("   T3.SCHOOL_KIND, ");
                stb.append("   '" + _gradeHrclass.substring(0, 2) + "' AS GRADE, ");
                stb.append("   '" + _gradeHrclass.substring(2) + "' AS HR_CLASS, ");
                stb.append("   T2.EXECUTEDATE, ");
                stb.append("   '1' AS HOLIDAY_FLG, ");
                stb.append("   '' AS REMARK1, ");
                stb.append("   CASE WHEN HOLIDAY_NAME IS NOT NULL THEN '祝' ELSE '' END AS EVENT_ABBV ");
                stb.append(" FROM ( ");
                stb.append("     SELECT I.* ");
                stb.append("          , RIGHT('0000' || TRIM(CAST(INT(I.YEAR) + CASE WHEN INT(I.HOLIDAY_MONTH) <= 3 THEN 1 ELSE 0 END AS VARCHAR(4))), 4) || '-' || I.HOLIDAY_MONTH || '-' || I.HOLIDAY_DAY AS EXECUTEDATE ");
                stb.append("     FROM PUBLIC_HOLIDAY_MST I ");
                stb.append("     WHERE I.YEAR = '" + _ctrlYear + "' ");
                stb.append("     ) T2 ");
                stb.append("      LEFT JOIN SCHREG_REGD_GDAT T3 ON T3.YEAR = T2.YEAR AND T3.GRADE = '" + _gradeHrclass.substring(0, 2) + "' ");
                stb.append(" WHERE T2.HOLIDAY_KIND = '1' ");
                stb.append(" AND T2.EXECUTEDATE BETWEEN '" + minDate + "' AND '" + maxDate + "' ");
                stb.append(" AND NOT EXISTS (SELECT 'X' FROM MAIN T1 WHERE T1.EXECUTEDATE = T2.EXECUTEDATE ");
                stb.append("                                           AND T1.HOLIDAY_FLG = '1'");
                stb.append("                                           AND T1.EVENT_ABBV = '祝' ");
                stb.append("                ) ");
            }
            stb.append(" ORDER BY ");
            stb.append("   EXECUTEDATE ");

            if (_isOutputDebug) {
                log.info(" holiday sql = " + stb.toString());
            } else {
                log.debug(" holiday sql = " + stb.toString());
            }
            final Map<String, List<String>> notHolidayHrMap = new HashMap<String, List<String>>();
            for (final Map<String, String> rs : KnjDbUtils.query(db2, stb.toString())) {
                final String schoolKind = KnjDbUtils.getString(rs, "SCHOOL_KIND");
                if (null == schoolKindDates.get(schoolKind)) {
                    createSchoolKindDates(datesOrigin, schoolKindDates, schoolKind);
                }

                final Map<String, PrintDay> dates = getMappedMap(schoolKindDates, schoolKind);
                final PrintDay printDate = dates.get(KnjDbUtils.getString(rs, "EXECUTEDATE"));
                if (null == printDate) {
                    log.error("PrintDate無し :" + KnjDbUtils.getString(rs, "EXECUTEDATE"));
                }
                final String holidayFlg = KnjDbUtils.getString(rs, "HOLIDAY_FLG");
                final String remark1 = KnjDbUtils.getString(rs, "REMARK1");
                final String event =  KnjDbUtils.getString(rs, "EVENT_ABBV");
                if (!"1".equals(holidayFlg)) {
                    if (_isGhr || _isGakunenKongou) {
                    } else {
                        getMappedList(notHolidayHrMap, KnjDbUtils.getString(rs, "EXECUTEDATE")).add(KnjDbUtils.getString(rs, "GRADE") + KnjDbUtils.getString(rs, "HR_CLASS"));
                    }
                    if (!StringUtils.isBlank(remark1)) {
                        printDate._remark1 = remark1;
                    }
                    if (!StringUtils.isBlank(event)) {
                        printDate._event = event;
                    }
                } else {
                    printDate._holidayFlg = holidayFlg;
                    printDate._remark1 = remark1;
                    printDate._event = event;
                }
            }
            for (final String schoolKind : schoolKindDates.keySet()) {
                final Map<String, PrintDay> dates = getMappedMap(schoolKindDates, schoolKind);
                for (final String executedate : notHolidayHrMap.keySet()) {
                    final List<String> notHolidayHrList = getMappedList(notHolidayHrMap, executedate);
                    log.debug(" check execuedate " + executedate + " = " + notHolidayHrList);
                    if (notHolidayHrList.size() > 0) {
                        final PrintDay printDate = (PrintDay) dates.get(executedate);
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
        }

        private Map<String, String> getNameMstRecord(final DB2UDB db2, final String namecd1, final String namecd2) {
            return KnjDbUtils.firstRow(KnjDbUtils.query(db2, " SELECT * FROM V_NAME_MST T1 WHERE T1.NAMECD1 = '" + namecd1 + "' AND  T1.NAMECD2 = '" + namecd2 + "' AND T1.YEAR = '" + _ctrlYear + "' "));
        }

        private Map<String, String> getNameMstMap(final DB2UDB db2, final String namecd1, final String field) {
            return KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, " SELECT NAMECD2, " + field + " FROM V_NAME_MST T1 WHERE T1.NAMECD1 = '" + namecd1 + "' AND  T1.YEAR = '" + _ctrlYear + "' "), "NAMECD2", field);
        }

        private String getSemestername(final DB2UDB db2) {
            return StringUtils.defaultString(KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT SEMESTERNAME FROM SEMESTER_MST T1 WHERE T1.YEAR = '" + _ctrlYear + "' AND T1.SEMESTER = '" + _targetSemes + "' ")));
        }

        private String getHrname(final DB2UDB db2) {
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
            return KnjDbUtils.getString(KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql.toString())), "HR_NAME");
        }

        private String getStaffname(final DB2UDB db2) {
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

                staffname = KnjDbUtils.getString(KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql.toString())), "STAFFNAME");
            } catch (Exception e) {
                log.error("exception!", e);
            }

            if (_isGakunenKongou || _isHoutei || FORM_DIV3.equals(_formDiv)) {
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

                String rsStaffname = null;

                String firstTrDiv = null;
                for (final Map<String, String> rs : KnjDbUtils.query(db2, sql.toString())) {
                    final String trDiv = KnjDbUtils.getString(rs, "TR_DIV");
                    if (null == firstTrDiv) {
                        firstTrDiv = trDiv; // 最小のTR_DIV
                    } else {
                        if (!firstTrDiv.equals(trDiv)) { // 最小のTR_DIVのみ処理
                            break;
                        }
                    }
                    log.debug(" trDiv = " + trDiv + ", fromDate = " + KnjDbUtils.getString(rs, "FROM_DATE") + ", toDate = " + KnjDbUtils.getString(rs, "TO_DATE") + ", staffcd = " + KnjDbUtils.getString(rs, "STAFFCD") + ", (grade || hrClass = " + KnjDbUtils.getString(rs, "GRADE") + " || " + KnjDbUtils.getString(rs, "HR_CLASS") + ")");
                    rsStaffname = KnjDbUtils.getString(rs, "STAFFNAME");
                }
                if (null != rsStaffname) {
                    // STAFF_CLASS_HIST_DATがあれば優先して表示
                    staffname = rsStaffname;
                }
            }
            return staffname;
        }

        private boolean getSeirekiFlg(final DB2UDB db2) {
            return "2".equals(KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z012' AND NAMECD2 = '00' ")));
        }

        private void setAttendSemesDayDat(final DB2UDB db2) {
            _attendSemesAppointedDay = 0;
            _attendDayDatSday = 0;
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     CASE WHEN INT(VALUE(MAX(APPOINTED_DAY),'99')) <= " + _targetDay + " THEN INT(MAX(APPOINTED_DAY)) ELSE 0 END AS ATTEND_SEMES_APPOINTED_DAY, ");
            stb.append("     CASE WHEN INT(VALUE(MAX(APPOINTED_DAY),'99')) <= " + _targetDay + " THEN INT(MAX(APPOINTED_DAY)) + 1 ELSE 1 END AS ATTEND_DAY_DAT_SDAY ");
            stb.append("   FROM ");
            stb.append("     APPOINTED_DAY_MST T1 ");
            stb.append("   WHERE ");
            stb.append("     T1.YEAR = '" + _ctrlYear + "' ");
            stb.append("     AND T1.MONTH = '" + _targetMonth + "' ");
            stb.append("     AND T1.SEMESTER = '" + _targetSemes + "' ");
            if (_gradeHrclassSchoolkind != null && _hasAPPOINTED_DAY_MST_SCHOOL_KIND) {
                stb.append("     AND T1.SCHOOL_KIND = '" + _gradeHrclassSchoolkind + "' ");
            }

            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, stb.toString()));
            _attendSemesAppointedDay = KnjDbUtils.getInt(row, "ATTEND_SEMES_APPOINTED_DAY", new Integer(0)).intValue();
            _attendDayDatSday = KnjDbUtils.getInt(row, "ATTEND_DAY_DAT_SDAY", new Integer(0)).intValue();
            if (0 == _attendDayDatSday) {
                _attendDayDatSday = 1;
            }
        }

        private String getStampData() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     SEQ, ");
            stb.append("     FILE_NAME, ");
            stb.append("     TITLE ");
            stb.append(" FROM ");
            stb.append("     PRG_STAMP_DAT ");
            stb.append(" WHERE ");
            stb.append("         YEAR        = '" + _ctrlYear + "' ");
            stb.append("     AND SEMESTER    = '9' ");
            stb.append("     AND SCHOOLCD    = '000000000000' ");
            if ("2".equals(_hrClassType)) {
                //実クラス
                stb.append("     AND SCHOOL_KIND = '" + _schoolKind + "' ");
            } else {
                //法廷クラス
                stb.append("     AND SCHOOL_KIND = 'H' ");
            }
            stb.append("     AND PROGRAMID   = 'KNJS340' ");
            stb.append("     AND TITLE       IS NOT NULL ");
            stb.append(" ORDER BY ");
            if(FORM_DIV6.equals(_knjs340FORM_DIV)) {
                stb.append("     SEQ");
            }else {
                stb.append("     SEQ DESC");
            }
            return stb.toString();
        }

        private String getMaxStampNo(final String staffcd) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     MAX(STAMP_NO) AS STAMP_NO ");
            stb.append(" FROM ");
            stb.append("     ATTEST_INKAN_DAT ");
            stb.append(" WHERE ");
            stb.append("     STAFFCD = '" + staffcd + "' ");

            return stb.toString();
        }

        private Map getStampData(final DB2UDB db2) throws SQLException {
            final Map retMap = new LinkedMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            PreparedStatement psStampNo = null;
            ResultSet rsStampNo = null;
            final String stamPSql = getStampData();
            try {
                ps = db2.prepareStatement(stamPSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String stampNoSql = getMaxStampNo(rs.getString("FILE_NAME"));
                    psStampNo = db2.prepareStatement(stampNoSql);
                    rsStampNo = psStampNo.executeQuery();
                    rsStampNo.next();
                    final String stampNo = rsStampNo.getString("STAMP_NO");
                    final StampData stampData = new StampData(rs.getString("SEQ"), rs.getString("TITLE"), stampNo, rs.getString("FILE_NAME"));
                    retMap.put(rs.getString("SEQ"), stampData);
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                DbUtils.closeQuietly(null, psStampNo, rsStampNo);
                db2.commit();
            }
            if (retMap.isEmpty()) {
                if(FORM_DIV6.equals(_knjs340FORM_DIV)) {
                    retMap.put("5", new StampData("5", "校長", null, null));
                    retMap.put("4", new StampData("4", "副校長", null, null));
                    retMap.put("3", new StampData("3", "教頭", null, null));
                    retMap.put("2", new StampData("2", "係", null, null));
                    retMap.put("1", new StampData("1", "担任", null, null));
                }else {
                    //SQLと同じように、SEQの大きい値から格納。nullなら、固定のjpgを出力。
                    retMap.put("5", new StampData("5", "担任", null, null));
                    retMap.put("4", new StampData("4", "係", null, null));
                    retMap.put("3", new StampData("3", "教頭", null, null));
                    retMap.put("2", new StampData("2", "副校長", null, null));
                    retMap.put("1", new StampData("1", "校長", null, null));
                }
            }
            return retMap;
        }

        /**
         * 写真データファイルの取得
         */
        private String getStampImageFile(final String filename) {
            return getImageFile(filename, _imageExt);
        }

        /**
         * 写真データファイルの取得
         */
        private String getImageFile(final String filename, final String imageExt) {
            if (null == filename) {
                return null;
            }
            if (null == _documentRoot) {
                return null;
            } // DOCUMENTROOT
            if (null == _imageDir) {
                return null;
            }
            if (null == imageExt) {
                return null;
            }
            final StringBuffer stb = new StringBuffer();
            stb.append(_documentRoot);
            stb.append("/");
            stb.append(_imageDir);
            stb.append("/");
            stb.append(filename);
            stb.append(".");
            stb.append(imageExt);
            File file1 = new File(stb.toString());
            if (!file1.exists()) {
                log.warn("not exist file:" + stb.toString());
                return null;
            } // 写真データ存在チェック用
            return stb.toString();
        }

        public String getImageFilePath(final String name) {
            final String path = _documentRoot + "/" + (null == _imagepath || "".equals(_imagepath) ? "" : _imagepath + "/") + name;
            final boolean exists = new java.io.File(path).exists();
            log.warn(" path " + path + " exists: " + exists);
            if (exists) {
                return path;
            }
            return null;
        }

        public String getNameCd1(final DB2UDB db2, final String cd) {
            final List cdList = KnjDbUtils.query(db2, " SELECT * FROM V_NAME_MST WHERE YEAR = '" + _ctrlYear + "' AND NAMECD1 = 'C" + _gradeHrclassSchoolkind + cd.substring(2, 4) + "' ");
            if (cdList.isEmpty()) {
                return cd;
            } else {
                return "C" + _gradeHrclassSchoolkind + cd.substring(2, 4);
            }
        }
    }
}

// eof