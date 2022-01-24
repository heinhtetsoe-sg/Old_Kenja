/*
 * $Id: e0c14d3a894a446b6bacff418d5e58fb41a2ca03 $
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
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 *  学校教育システム 賢者 [小学校プログラム] 出席統計
 *
 */
public class KNJS351 {

    private static final Log log = LogFactory.getLog(KNJS351.class);

    private static final Integer dicdSick = new Integer(4);
    private static final Integer dicdNotice = new Integer(5);
    private static final Integer dicdNonotice = new Integer(6);

    private final String SEMEALL = "9";

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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        String form = null;
        final int maxLine = "2".equals(_param._formSelect) ? 45 : 15;
        final String[] semesters;
        if ("2".equals(_param._schoolMstSemesterdiv)) { // 2学期用
            semesters = new String[] {"1", "2", SEMEALL};
            if (maxLine == 45) {
                form = "KNJS351_1_1.frm";
            } else if (maxLine == 15) {
                form = "KNJS351_1_2.frm";
            }
        } else { // 3学期用
            semesters = new String[] {"1", "2", "3", SEMEALL};
            if (maxLine == 45) {
                form = "KNJS351_2_1.frm";
            } else if (maxLine == 15) {
                form = "KNJS351_2_2.frm";
            }
        }

        final List dataListAll = getStudentList(db2, maxLine);

        final List pageList = getPageList(dataListAll, maxLine);

        for (int pi = 0; pi < pageList.size(); pi++) {
            svf.VrSetForm(form, 1);

            svf.VrsOut("nendo", KenjaProperties.gengou(Integer.parseInt(_param._ctrlYear)) + "年度　出席統計"); // 年度
            svf.VrAttribute("nendo", "Hensyu=3"); // 集計範囲
            svf.VrsOut("HR_NAME", _param._hrname); // 組氏名
            svf.VrsOut("teacher3", _param._staffname); // 担任
            svf.VrsOut("ymd1", KNJ_EditDate.h_format_JP(_param._ctrlDate)); // 作成日
            for (int si = 0; si < semesters.length; si++) {
                final String semestername = (String) _param._semesternameMap.get(semesters[si]);
                svf.VrsOut("SEMESTER" + semesters[si], semestername); // 学期
            }
            svf.VrAttribute("RANGE", "X=10000"); // 集計範囲

            final String[] kessekiField = {"SICK", "NOTICE"}; // 病欠, 事故欠
            int fldi = 0;
            for (final Iterator it = _param._sickNoticeNonoticeCds.iterator(); it.hasNext() && fldi <= kessekiField.length;) {
                final Integer dicd = (Integer) it.next();
                final String name = (String) _param._nameMstC001.get(dicd);
                svf.VrsOut(kessekiField[fldi] + "_NAME1", name); // 生徒氏名
                fldi++;
            }

            final List dataList = (List) pageList.get(pi);
            for (int i = 0; i < dataList.size(); i++) {
                final int line = i + 1;
                final Student student = (Student) dataList.get(i);

                svf.VrsOutn("NUMBER", line, NumberUtils.isDigits(student._attendno) ? String.valueOf(Integer.parseInt(student._attendno)) : StringUtils.defaultString(student._attendno)); // 番号
                if (_param._isGakunenKongou && "1".equals(_param._formSelect)) {
                    svf.VrsOutn("SCHREGNO", line, student._schregno); // 学籍番号
                    svf.VrsOutn("name2", line, student._name); // 生徒氏名
                } else {
                    svf.VrsOutn("name", line, student._name); // 生徒氏名
                }

                AttendSemesDat total = null;
                for (int si = 0; si < semesters.length; si++) {

                    final AttendSemesDat att;
                    final String semester = semesters[si];
                    if (SEMEALL.equals(semester)) {
                        att = total;
                    } else {
                        att = (AttendSemesDat) student._semesMap.get(semester);
                    }
                    if (null == att) {
                        continue;
                    }

                    svf.VrsOutn("SUSPEND" + semester, line, String.valueOf(att.suspendMourning()));
                    svf.VrsOutn("LESSON" + semester, line, String.valueOf(att.jugyoNissu()));

                    svf.VrsOutn("APPOINT" + semester, line, att._lesson); // 授業日数
                    svf.VrsOutn("SUSPEND" + semester, line, att.suspendMourning()); // 出停忌引
                    svf.VrsOutn("OFF" + semester, line, att._absent); // 公欠
                    svf.VrsOutn("MUST" + semester, line, att.jugyoNissu()); // 出席すべき日数

                    fldi = 0;
                    for (final Iterator it = _param._sickNoticeNonoticeCds.iterator(); it.hasNext() && fldi <= kessekiField.length;) {
                        final Integer dicd = (Integer) it.next();
                        final Integer count = att.getSickNoticeNonotice(dicd);
                        svf.VrsOutn(kessekiField[fldi] + semester, line, String.valueOf(count));
                        fldi++;
                    }

                    svf.VrsOutn("LESSON" + semester, line, att.shussekiNissu()); // 出席日数
                    svf.VrsOutn("LATE" + semester, line, att._late); // 遅刻
                    svf.VrsOutn("EARLY" + semester, line, att._early); // 早退

                    if (!SEMEALL.equals(semester)) {
                        if (null == total) {
                            total = att;
                        } else {
                            total.add(att);
                        }
                    }
                }

            }
            svf.VrEndPage();
            _hasData = true;
        }
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

    public boolean dateIsBetween(final String date, final String sStartDate, final String sEndDate) {
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
    private List getStudentList(final DB2UDB db2, final int MAX_STUDENT_LINE) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        final Map studentsMap = new HashMap();
        final List students = new ArrayList();
        try {
            final String sql = getAttendtSql();
            log.debug(" sqlAttend =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String schregno = rs.getString("SCHREGNO");
                if (null == studentsMap.get(schregno)) {
                    final String gradeName1 = rs.getString("GRADE_NAME1");
                    final Student student = new Student();
                    student._schregno = schregno;
                    student._name = rs.getString("NAME");
                    student._gradeHrclass = rs.getString("GRADE") + rs.getString("HR_CLASS");
                    student._attendno = StringUtils.defaultString(rs.getString("ATTENDNO"), "1");
                    student._schoolKind = rs.getString("SCHOOL_KIND");
                    student._gradeName1 = gradeName1;

                    studentsMap.put(schregno, student);
                    students.add(student);
                }
            }
        } catch (SQLException ex) {
            log.error("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        try {
            final String sql = getAttendSemesDatSql(_param);
            log.debug(" sqlAttendSemesDat =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final Student student = (Student) studentsMap.get(rs.getString("SCHREGNO"));
                if (null == student) {
                    continue;
                }
                final AttendSemesDat attseme = new AttendSemesDat();
                attseme._lesson = rs.getString("LESSON");
                attseme._suspend = rs.getString("SUSPEND");
                attseme._koudome = "true".equals(_param._useKoudome) ? rs.getString("KOUDOME") : null;
                attseme._virus = "true".equals(_param._useVirus) ? rs.getString("VIRUS") : null;
                attseme._mourning = rs.getString("MOURNING");
                attseme._sick = _param._nameMstC001.containsKey(dicdSick) ? rs.getString("SICK") : null;
                attseme._notice = _param._nameMstC001.containsKey(dicdNotice) ? rs.getString("NOTICE") : null;
                attseme._nonotice = _param._nameMstC001.containsKey(dicdNonotice) ? rs.getString("NONOTICE") : null;
                attseme._late = rs.getString("LATE");
                attseme._early = rs.getString("EARLY");
                student._semesMap.put(rs.getString("SEMESTER"), attseme);
            }
        } catch (SQLException ex) {
            log.error("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }

        return students;
    }

    private String getAttendtSql() {
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
        stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
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
        stb.append("     T1.ENT_DIV, ");
        stb.append("     T1.GRD_DIV, ");
        stb.append("     T1.ENT_DIV_NAME, ");
        stb.append("     T1.GRD_DIV_NAME, ");
        stb.append("     T1.ENT_DATE, ");
        stb.append("     T1.GRD_DATE, ");
        stb.append("     T1.GRADE_NAME1, ");
        stb.append("     T1.BYDM004REMARK2 ");
        stb.append(" FROM ");
        stb.append("     SCHREGNOS T1 ");
        stb.append(" ORDER BY ");
        if (_param._isGakunenKongou) {
            stb.append("     T1.GRADE, ");
            stb.append("     T1.HR_CLASS, ");
        }
        stb.append("     T1.ATTENDNO ");
        return stb.toString();
    }

    private String getAttendSemesDatSql(final Param param) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH SCHREGNOS AS (SELECT DISTINCT ");
        if (param._isGakunenKongou) {
            stb.append("     REGD.SCHREGNO ");
        } else {
            stb.append("     T1.SCHREGNO ");
        }
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
        stb.append("     LEFT JOIN SCHREG_REGD_GDAT T3 ON T3.YEAR = REGD.YEAR AND T3.GRADE = REGD.GRADE ");
        stb.append("     LEFT JOIN SCHREG_ENT_GRD_HIST_DAT T2 ON T2.SCHREGNO = REGD.SCHREGNO AND T2.SCHOOL_KIND = T3.SCHOOL_KIND ");
        stb.append("   WHERE ");
        stb.append("     T1.YEAR = '" + param._ctrlYear + "' ");
        stb.append("     AND T1.SEMESTER = '" + param._semester + "' ");
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
        stb.append(" ), ATTEND_SEMES AS ( ");
        stb.append("   SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.SEMESTER, ");
        stb.append("     T1.ABSENT, ");
        stb.append("     T1.LESSON, ");
        stb.append("     T1.SUSPEND, ");
        if ("true".equals(param._useVirus)) {
            stb.append("     T1.VIRUS AS VIRUS, ");
        } else {
            stb.append("     0 AS VIRUS, ");
        }
        if ("true".equals(param._useKoudome)) {
            stb.append("     T1.KOUDOME AS KOUDOME, ");
        } else {
            stb.append("     0 AS KOUDOME, ");
        }
        stb.append("     T1.MOURNING, ");
        stb.append("     T1.SICK, ");
        stb.append("     T1.NOTICE, ");
        stb.append("     T1.NONOTICE, ");
        stb.append("     T1.LATE, ");
        stb.append("     T1.EARLY ");
        stb.append("   FROM ");
        stb.append("     V_ATTEND_SEMES_DAT T1 ");
        stb.append("     INNER JOIN SCHREGNOS T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("   WHERE ");
        stb.append("     T1.YEAR = '" + param._ctrlYear + "' ");
        stb.append("     AND T1.SEMESTER <= '" + param._semester + "' ");
        stb.append(" ) ");
        stb.append("  SELECT ");
        stb.append("    T1.SCHREGNO, ");
        stb.append("    T1.SEMESTER, ");
        stb.append("    SUM(T1.LESSON) AS LESSON, ");
        stb.append("    SUM(T1.ABSENT) AS ABSENT, ");
        stb.append("    SUM(T1.SUSPEND) AS SUSPEND, ");
        stb.append("    SUM(T1.VIRUS) AS VIRUS, ");
        stb.append("    SUM(T1.KOUDOME) AS KOUDOME, ");
        stb.append("    SUM(T1.MOURNING) AS MOURNING, ");
        stb.append("    SUM(T1.SICK) AS SICK, ");
        stb.append("    SUM(T1.NOTICE) AS NOTICE, ");
        stb.append("    SUM(T1.NONOTICE) AS NONOTICE, ");
        stb.append("    SUM(T1.LATE) AS LATE, ");
        stb.append("    SUM(T1.EARLY) AS EARLY ");
        stb.append("  FROM ");
        stb.append("    ATTEND_SEMES T1 ");
        stb.append("  GROUP BY ");
        stb.append("    T1.SCHREGNO, T1.SEMESTER ");

        return stb.toString();
    }

    private static String getDateString(final Calendar cal) {
        final DecimalFormat df = new DecimalFormat("00");
        return cal.get(Calendar.YEAR) + "-" + df.format(cal.get(Calendar.MONTH) + 1) + "-" + df.format(cal.get(Calendar.DATE));
    }

    private static List getMappedList(final Map map, final Object key) {
        if (!map.containsKey(key)) {
            map.put(key, new ArrayList());
        }
        return (List) map.get(key);
    }

    private static Map getMappedMap(final Map map, final Object key) {
        if (!map.containsKey(key)) {
            map.put(key, new TreeMap());
        }
        return (Map) map.get(key);
    }

    /**
     * 生徒
     */
    private static class Student {
        String _schregno;
        String _name;
        String _gradeHrclass;
        String _attendno;
        String _schoolKind;
        String _remark1;
        String _entDate;
        String _entDivName;
        String _grdDate;
        String _grdDivName;
        String _gradeName1;
        final Map _semesMap = new HashMap();

    }

    private static class AttendSemesDat {
        String _lesson;
        String _absent;
        String _suspend;
        String _koudome;
        String _virus;
        String _mourning;
        String _sick;
        String _notice;
        String _nonotice;
        String _late;
        String _early;
        private void add(final AttendSemesDat semes) {
            _lesson   = addInt(_lesson ,semes._lesson);
            _absent   = addInt(_absent, semes._absent);
            _suspend  = addInt(_suspend ,semes._suspend);
            _koudome  = addInt(_koudome ,semes._koudome);
            _virus    = addInt(_virus ,semes._virus);
            _mourning = addInt(_mourning ,semes._mourning);
            _sick     = addInt(_sick ,semes._sick);
            _notice   = addInt(_notice ,semes._notice);
            _nonotice = addInt(_nonotice ,semes._nonotice);
            _late     = addInt(_late ,semes._late);
            _early    = addInt(_early ,semes._early);
        }
        public String shussekiNissu() {
            return subtractInt(jugyoNissu(), addInt(_sick, addInt(_notice, _nonotice)));
        }
        private String suspendMourning() {
            return addInt(_suspend, addInt(_virus, addInt(_koudome, _mourning)));
        }
        private String jugyoNissu() {
            return subtractInt(_lesson, suspendMourning());
        }
        private static String addInt(final String a, final String b) {
            if (!NumberUtils.isDigits(a)) {
                return b;
            }
            if (!NumberUtils.isDigits(b)) {
                return a;
            }
            return new Integer(Double.valueOf(a).intValue() + Double.valueOf(b).intValue()).toString();
        }
        private static String subtractInt(final String a, final String b) {
            if (!NumberUtils.isDigits(a)) {
                if (NumberUtils.isDigits(b)) {
                    return new Integer(- Double.valueOf(b).intValue()).toString();
                } else {
                    return null;
                }
            }
            if (!NumberUtils.isDigits(b)) {
                return a;
            }
            return new Integer(Double.valueOf(a).intValue() - Double.valueOf(b).intValue()).toString();
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
        log.fatal("$Revision: 65812 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _ctrlYear;
        final String _semester;
        final String _hrClassType;
        final String _gakunenKongou;
        final String _gradeHrclass;
        final String _ctrlDate;
        final String _useVirus;
        final String _useKoudome;
        final String _useKekka;
        final String _useKekkaJisu;
        final String _useLateDetail;
        final String _restrictFlg;
        final String _printLogStaffcd;
        final String _formSelect;

        final Map _semesternameMap;
        final String _hrname;
        final String _staffname;
        final boolean _seirekiFlg;
        final String _z010Name1;
        final Map _nameMstC001;
        final Map _nameMstC001Abbv1;
        final Map _nameMstC006;
        final Map _nameMstC007;
        final String _nameMstC00801Abbv1;
        final TreeSet _sickNoticeNonoticeCds;
        final String _useSchool_KindField;
        final String _SCHOOLKIND;
        final String _schoolMstSemesterdiv;

        boolean _isMusashinohigashi = false;
        boolean _isFi = false;
        boolean _isGhr = false;
        boolean _isGakunenKongou = false;
        boolean _isHoutei = false;
        final boolean _isTokubetsuShien;
        final boolean _isOutputDebug;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _semester = request.getParameter("SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _hrClassType = request.getParameter("HR_CLASS_TYPE");
            _gakunenKongou = request.getParameter("GAKUNEN_KONGOU");
            _gradeHrclass = request.getParameter("GRADE_HR_CLASS");
            _useVirus = request.getParameter("useVirus");
            _useKoudome = request.getParameter("useKoudome");
            _useKekka = request.getParameter("useKekka");
            _useKekkaJisu = request.getParameter("useKekkaJisu");
            _useLateDetail = request.getParameter("useLateDetail");
            _restrictFlg = request.getParameter("RESTRICT_FLG");
            _printLogStaffcd = request.getParameter("PRINT_LOG_STAFFCD");
            _formSelect = request.getParameter("FORM_SELECT");
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            _SCHOOLKIND = request.getParameter("SCHOOLKIND");

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

            _semesternameMap = getSemesternameMap(db2);
            _hrname = getHrname(db2);
            _seirekiFlg = getSeirekiFlg(db2);
            _staffname = getStaffname(db2);
            _nameMstC001 = getNameMstMap(db2, "C001", "NAME1");
            _nameMstC001Abbv1 = getNameMstMap(db2, "C001", "ABBV1");
            _nameMstC00801Abbv1 = (String) getNameMstRecord(db2, "C008", "01").get("ABBV1");
            _nameMstC006 = getNameMstMap(db2, "C006", "NAME1");
            _nameMstC007 = getNameMstMap(db2, "C007", "NAME1");
            _schoolMstSemesterdiv = getSchoolMstSemesterdiv(db2);
            _sickNoticeNonoticeCds = new TreeSet();
            if (_sickNoticeNonoticeCds.size() < 2 && _nameMstC001.containsKey(dicdSick)) _sickNoticeNonoticeCds.add(dicdSick);
            if (_sickNoticeNonoticeCds.size() < 2 && _nameMstC001.containsKey(dicdNotice)) _sickNoticeNonoticeCds.add(dicdNotice);
            if (_sickNoticeNonoticeCds.size() < 2 && _nameMstC001.containsKey(dicdNonotice)) _sickNoticeNonoticeCds.add(dicdNonotice);
            _z010Name1 = (String) getNameMstRecord(db2, "Z010", "00").get("NAME1");
            log.debug(" z010 name1 = " + _z010Name1);
            _isMusashinohigashi = "musashinohigashi".equals(_z010Name1);
            _isOutputDebug = "1".equals(getDbPrginfoProperties(db2, "outputDebug"));
        }

        private static String getDbPrginfoProperties(final DB2UDB db2, final String propName) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJS340' AND NAME = '" + propName + "' "));
        }

        private String getString(final Object cd, final Map map) {
            return (String) map.get(cd);
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

        private Map getSemesternameMap(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            Map rtn = new HashMap();
            try {
                final String sql = " SELECT SEMESTER, SEMESTERNAME FROM SEMESTER_MST T1 WHERE T1.YEAR = '" + _ctrlYear + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn.put(rs.getString("SEMESTER"), rs.getString("SEMESTERNAME"));
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
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
                    sql.append(" WHERE T1.YEAR = '" + _ctrlYear + "' AND T1.SEMESTER = '" + _semester + "' ");
                    sql.append("   AND T1.GRADE || T1.HR_CLASS = '" + _gradeHrclass + "' ");
                } else if (_isGhr) {
                    sql.append(" SELECT GHR_NAME AS HR_NAME ");
                    sql.append(" FROM SCHREG_REGD_GHR_HDAT T1 ");
                    sql.append(" WHERE T1.YEAR = '" + _ctrlYear + "' AND T1.SEMESTER = '" + _semester + "' ");
                    sql.append("   AND T1.GHR_CD = '" + _gradeHrclass + "' ");
                } else if (_isGakunenKongou) {
                    sql.append(" SELECT  ");
                    sql.append("   T1.SCHOOL_KIND || '-' || T1.HR_CLASS AS VALUE, ");
                    sql.append("   MAX(T1.HR_CLASS_NAME1) AS HR_NAME ");
                    sql.append(" FROM V_STAFF_HR_DAT T1 ");
                    sql.append(" WHERE ");
                    sql.append("   T1.YEAR = '" + _ctrlYear + "' ");
                    sql.append("   AND T1.SEMESTER = '" + _semester + "' ");
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
                    sql.append(" WHERE T1.YEAR = '" + _ctrlYear + "' AND T1.SEMESTER = '" + _semester + "' ");
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
                    sql.append(" WHERE T1.YEAR = '" + _ctrlYear + "' AND T1.SEMESTER = '" + _semester + "' ");
                    sql.append("   AND T1.GRADE || T1.HR_CLASS = '" + _gradeHrclass + "' ");
                } else if (_isGhr) {
                    sql.append(" SELECT VALUE(T2.STAFFNAME, T3.STAFFNAME, T4.STAFFNAME) AS STAFFNAME ");
                    sql.append(" FROM SCHREG_REGD_GHR_HDAT T1 ");
                    sql.append("     LEFT JOIN STAFF_MST T2 ON T2.STAFFCD = TR_CD1 ");
                    sql.append("     LEFT JOIN STAFF_MST T3 ON T3.STAFFCD = TR_CD2 ");
                    sql.append("     LEFT JOIN STAFF_MST T4 ON T4.STAFFCD = TR_CD3 ");
                    sql.append(" WHERE T1.YEAR = '" + _ctrlYear + "' AND T1.SEMESTER = '" + _semester + "' ");
                    sql.append("   AND T1.GHR_CD = '" + _gradeHrclass + "' ");
                } else if (_isGakunenKongou) {
                    sql.append(" SELECT T1.STAFFCD, T2.STAFFNAME  ");
                    sql.append(" FROM V_STAFF_HR_DAT T1 ");
                    sql.append(" LEFT JOIN STAFF_MST T2 ON T2.STAFFCD = T1.STAFFCD ");
                    sql.append(" WHERE ");
                    sql.append("   T1.YEAR = '" + _ctrlYear + "' ");
                    sql.append("   AND T1.SEMESTER = '" + _semester + "' ");
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
                    sql.append(" WHERE T1.YEAR = '" + _ctrlYear + "' AND T1.SEMESTER = '" + _semester + "' ");
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
                    sql.append(" WHERE T1.YEAR = '" + _ctrlYear + "' AND T1.SEMESTER = '" + _semester + "' ");
                    if (_isGakunenKongou) {
                        sql.append("   AND (T1.GRADE, T1.HR_CLASS) IN ( ");
                        sql.append("     SELECT T1.GRADE, T1.HR_CLASS  ");
                        sql.append("     FROM V_STAFF_HR_DAT T1 ");
                        sql.append("     LEFT JOIN STAFF_MST T2 ON T2.STAFFCD = T1.STAFFCD ");
                        sql.append("     WHERE ");
                        sql.append("       T1.YEAR = '" + _ctrlYear + "' ");
                        sql.append("       AND T1.SEMESTER = '" + _semester + "' ");
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

        private boolean getSeirekiFlg(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            boolean seirekiFlg = false;
            try {
                String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z012' AND NAMECD2 = '00' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    if ("2".equals(rs.getString("NAME1"))) seirekiFlg = true; //西暦
                }
            } catch (Exception e) {
                log.error("getSeirekiFlg Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return seirekiFlg;
        }

        private String getSchoolMstSemesterdiv(final DB2UDB db2) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT SEMESTERDIV FROM SCHOOL_MST WHERE YEAR = '" + _ctrlYear + "' ");
            if ("1".equals(_useSchool_KindField)) {
                stb.append(" AND SCHOOL_KIND = '" + _SCHOOLKIND + "' ");
            }
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, stb.toString()));
        }
    }
}

// eof