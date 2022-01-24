/*
 * $Id: 3238dd5b9404556d8cdad0c99c6c98a5631025aa $
 *
 * 作成日: 2011/09/22
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJS;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 *  学校教育システム 賢者 [小学校プログラム] 月間計画表 行事予定表
 *
 */
public class KNJS320 {

    private static final Log log = LogFactory.getLog(KNJS320.class);

    private static final String[] youbi = new String[]{null, "日", "月", "火", "水", "木", "金", "土"};

    private static final int MAX_STUDENT_LINE = 50;
    private static final int MAX_DATE_LINE = 31;
    private static final int MAX_PERIOD_LINE = 8;

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

            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            if (null != db2) {
                db2.commit();
                db2.close();
            }
            svf.VrQuit();
        }

    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        svfPrintHead(db2, svf);
        svfPrintDayList(svf);
    }

    private void svfPrintHead(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrSetForm(_param.getFormName(), 4);
        svf.VrsOut("NENDO", KNJ_EditDate.getAutoFormatYear(db2, Integer.parseInt(_param._ctrlYear)) + "年度　" + _param.getTitle());
        svf.VrsOut("PERIOD", "（" + KNJ_EditDate.getAutoFormatDate(db2, _param._monthSdate) + " \uFF5E " + KNJ_EditDate.getAutoFormatDate(db2, _param._monthEdate) + "）");
        svf.VrsOut("HR_NAME", _param._hrname);
        svf.VrsOut("TEACHER", _param._staffname);
        svf.VrsOut("ymd1", KNJ_EditDate.getAutoFormatDate(db2, _param._ctrlDate));
        if (_param._printForm1) {
            svf.VrsOut("MONTH", Integer.parseInt(_param._targetMonth) + "月");
            svf.VrsOut("EVENTNAME", "行事");
            svf.VrsOut("REMARKNAME", "備考");
        }
        if (_param._printForm2) {
            svf.VrsOut("EVENTNAME1", _param._printEvent ? "行事" : "備考");
        }
        if (_param._printForm3) {
            svf.VrsOut("EVENTNAME", _param._printEvent ? "行事" : "備考");
            for (final Iterator it = _param._periodList.iterator(); it.hasNext();) {
                final Period period = (Period) it.next();
                svf.VrsOut("PERIOD" + period._line, period._name);
            }
        }
    }

    /**
     * 日付ごとの行事・備考を出力(レコード)
     * @param svf
     */
    private void svfPrintDayList(final Vrw32alp svf) {
        int dayLine = 0;
        String preMonth = String.valueOf(Integer.parseInt(_param._targetMonth));
        int monthCnt = 1;
        for (final Iterator dateit = _param._dates.keySet().iterator(); dateit.hasNext();) { // 日付ごとにレコード出力
            final String date = (String) dateit.next();
            final PrintDay printDay = (PrintDay) _param._dates.get(date);

            if (!preMonth.equals(printDay._month)) {
                for (int day = dayLine + 1; day <= MAX_DATE_LINE; day++) {
                    svf.VrsOut("DAY2", String.valueOf(day));
                    svf.VrEndRecord();
                }
                dayLine = 0;
                preMonth = printDay._month;
                monthCnt++;
            }

            dayLine++;
            String flg = "1".equals(printDay._holidayFlg) ? "2" : "1";

            if (_param._printForm2) {
                svf.VrsOut("MONTH" + monthCnt, printDay._month + "月");
            }
            svf.VrsOut("HOLIDAY" + flg, "1".equals(printDay._holidayFlg) ? "*" : "");
            svf.VrsOut("DAY" + flg, printDay._dayOfMonth);
            svf.VrsOut("WEEK" + flg, printDay._youbi);
            if (_param._printForm1) {
                svf.VrsOut("EVENT" + flg, printDay._remark1);
                svf.VrsOut("REMARK" + flg, printDay._remark2);
            }
            if (_param._printForm2 || _param._printForm3) {
                svf.VrsOut("EVENT" + flg, _param._printEvent ? printDay._remark1 : printDay._remark2);
            }
            if (_param._printForm3) {
                for (final Iterator it = _param._periodList.iterator(); it.hasNext();) {
                    final Period period = (Period) it.next();
                    final SchChrDat schChrDat = (SchChrDat) printDay._schChrDatMap.get(period._periodCd);
                    if (null != schChrDat) {
                        svf.VrsOut("CLASS" + flg + "_" + period._line, schChrDat._subclassName);
                    }
                }
            }

            svf.VrEndRecord();
            _hasData = true;
        }
        for (int day = dayLine + 1; day <= MAX_DATE_LINE; day++) {
            svf.VrsOut("DAY2", String.valueOf(day));
            svf.VrEndRecord();
        }
    }

    private static String getDateString(final Calendar cal) {
        final DecimalFormat df = new DecimalFormat("00");
        return cal.get(Calendar.YEAR) + "-" + df.format(cal.get(Calendar.MONTH) + 1) + "-" + df.format(cal.get(Calendar.DATE));
    }

    /**
     * 表示する日
     */
    private static class PrintDay {
        final String _date;
        final String _month;
        final String _dayOfMonth;
        final String _youbi;

        String _holidayFlg;
        String _remark1;
        String _remark2;
        boolean _isPrintTarget = false;

        final Map _schChrDatMap;

        PrintDay(final Calendar cal) {
            _date = getDateString(cal);
            _month = String.valueOf(cal.get(Calendar.MONTH) + 1);
            _dayOfMonth = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
            final int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK); // 1:日 2:月 3:火 4:水 5:木 6:金 7:土
            _youbi = youbi[dayOfWeek];
            _holidayFlg = "";
            _remark1 = "";
            _remark2 = "";
            _schChrDatMap = new HashMap();
        }

        public String toString() {
            return "PrintDay(" + _date + ")";
        }
    }

    /**
     * 校時
     */
    private static class Period {
        final String _periodCd;
        final String _name;
        final int _line;

        Period(final String periodCd, final String name, final int line) {
            _periodCd = periodCd;
            _name = name;
            _line = line;
        }
    }

    /**
     * 時間割
     */
    private static class SchChrDat {
        final String _executeDate;
        final String _periodCd;
        final String _subclassCd;
        final String _subclassName;

        SchChrDat(final String executeDate, final String periodCd, final String subclassCd, final String subclassName) {
            _executeDate = executeDate;
            _periodCd = periodCd;
            _subclassCd = subclassCd;
            _subclassName = subclassName;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 67773 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _ctrlYear;
        final String _ctrlSemester;
        final String _ctrlDate;
        final String _gradeHrclass;
        final String _targetMonth;
        final String _useCurriculumcd;

        final boolean _printForm1; //行事予定表(単月)
        final boolean _printForm2; //行事予定表(３ヶ月分)
        final boolean _printForm3; //月間計画表
        final boolean _printEvent; //行事
        final boolean _printRemark;//備考
        final int _maxMonth;//1 or 3

        final String _semestername;
        final String _hrname;
        final String _staffname;
        final TreeMap _dates = new TreeMap();
        final List _periodList;

        String _monthSdate = null;
        String _monthEdate = null;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _gradeHrclass = request.getParameter("GRADE_HR_CLASS");
            _targetMonth = request.getParameter("TARGET_MONTH");
            _useCurriculumcd = request.getParameter("useCurriculumcd");

            final String targetForm = request.getParameter("TARGET_FORM");
            final String form2Remark = request.getParameter("FORM2_REMARK");
            final String form3Remark = request.getParameter("FORM3_REMARK");
            _printForm1 = "1".equals(targetForm);
            _printForm2 = "2".equals(targetForm);
            _printForm3 = "3".equals(targetForm);
            _printEvent  = (_printForm3) ? "1".equals(form3Remark) : (_printForm2) ? "1".equals(form2Remark) : true;
            _printRemark = (_printForm3) ? "2".equals(form3Remark) : (_printForm2) ? "2".equals(form2Remark) : true;
            _maxMonth = (_printForm2) ? 3 : 1;

            _semestername = getSemestername(db2);
            _hrname = getHrname(db2);
            _staffname = getStaffname(db2);
            setDates(db2);
            setEventDatHolidayFlg(db2);
            _periodList = getPeriodList(db2);
            if (_printForm3) setSchChrDatMap(db2);
        }

        /**
         * 日付の月の最終日付のカレンダーを得る
         * @param date
         * @return
         */
        private Calendar getLastDayOfMonth(final String date, final int no) {
            final Calendar cal = Calendar.getInstance();
            cal.setTime(java.sql.Date.valueOf(date));
            cal.add(Calendar.MONTH, no);
            cal.add(Calendar.DAY_OF_MONTH, -1);
            return cal;
        }

        private void setDates(final DB2UDB db2) {
            final String year = Integer.parseInt(_targetMonth) < 4 ? String.valueOf(Integer.parseInt(_ctrlYear) + 1) : _ctrlYear;
            final String monthStartDay = year + "-" + _targetMonth + "-01";
            final Calendar calSemesSdate = Calendar.getInstance();
            calSemesSdate.setTime(java.sql.Date.valueOf(monthStartDay));
            final Calendar calSemesEdate = getLastDayOfMonth(monthStartDay, _maxMonth);

            _monthSdate = getDateString(calSemesSdate);
            _monthEdate = getDateString(calSemesEdate);

            final Calendar cal = Calendar.getInstance();
            cal.setTime(java.sql.Date.valueOf(monthStartDay));
//            final int currentMonth = cal.get(Calendar.MONTH);
//            while (cal.get(Calendar.MONTH) == currentMonth) {
            while (true) {
                if ((cal.after(calSemesSdate) || cal.equals(calSemesSdate)) && (cal.before(calSemesEdate) || cal.equals(calSemesEdate))) {
                } else {
                    break;
                }
                final PrintDay printDay = new PrintDay(cal);
                printDay._isPrintTarget = (cal.after(calSemesSdate) || cal.equals(calSemesSdate)) && (cal.before(calSemesEdate) || cal.equals(calSemesEdate));
                _dates.put(getDateString(cal), printDay);
                cal.add(Calendar.DATE, 1);
            }
        }

        private void setEventDatHolidayFlg(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("   T1.EXECUTEDATE, T1.HOLIDAY_FLG, VALUE(T1.REMARK1, '') AS REMARK1, VALUE(T1.REMARK2, '') AS REMARK2 ");
                stb.append(" FROM ");
                stb.append("   EVENT_DAT T1 ");
                stb.append(" WHERE ");
                stb.append("   T1.GRADE || T1.HR_CLASS = '" + _gradeHrclass + "' ");
                stb.append("   AND T1.EXECUTEDATE BETWEEN DATE('" + _monthSdate + "') AND DATE('" + _monthEdate + "') ");
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final PrintDay printDay = (PrintDay) _dates.get(rs.getString("EXECUTEDATE"));
                    if (null == printDay) {
                        log.error("printDay無し :" + rs.getString("EXECUTEDATE"));
                    } else {
                        printDay._holidayFlg = rs.getString("HOLIDAY_FLG");
                        printDay._remark1 = rs.getString("REMARK1");
                        printDay._remark2 = rs.getString("REMARK2");
                    }
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private void setSchChrDatMap(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getSchChrDatSql();
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String executeDate = rs.getString("EXECUTEDATE");
                    final String periodCd = rs.getString("PERIODCD");
                    final String subclassCd = rs.getString("SUBCLASSCD");
                    final String subclassName = rs.getString("SUBCLASSNAME");

                    final PrintDay printDay = (PrintDay) _dates.get(executeDate);
                    if (null == printDay) {
                        log.error("printDay無し :" + executeDate);
                    } else {
                        final SchChrDat schChrDat = new SchChrDat(executeDate, periodCd, subclassCd, subclassName);
                        printDay._schChrDatMap.put(periodCd, schChrDat);
                    }
                }
            } catch (SQLException ex) {
                log.debug("Exception:", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private String getSchChrDatSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH T_SCHNO AS ( ");
            stb.append("     SELECT ");
            stb.append("         SCHREGNO ");
            stb.append("     FROM ");
            stb.append("         SCHREG_REGD_DAT ");
            stb.append("     WHERE ");
            stb.append("         YEAR = '" + _ctrlYear + "' ");
            stb.append("         AND SEMESTER ='" + _ctrlSemester + "' ");
            stb.append("         AND GRADE || HR_CLASS ='" + _gradeHrclass + "' ");
            stb.append("     ) ");
            stb.append(" , T_SCH_CHR AS ( ");
            stb.append("     SELECT ");
            stb.append("         T1.EXECUTEDATE, ");
            stb.append("         T1.PERIODCD, ");
            stb.append("         MAX(T1.CHAIRCD) AS CHAIRCD, ");
            stb.append("         T1.YEAR, ");
            stb.append("         T1.SEMESTER ");
            stb.append("     FROM ");
            stb.append("         SCH_CHR_DAT T1, ");
            stb.append("         CHAIR_STD_DAT T2 ");
            stb.append("     WHERE ");
            stb.append("         T1.EXECUTEDATE BETWEEN DATE('" + _monthSdate + "') AND DATE('" + _monthEdate + "') ");
            stb.append("         AND T1.YEAR = '" + _ctrlYear + "' ");
            stb.append("         AND T1.YEAR = T2.YEAR ");
            stb.append("         AND T1.SEMESTER = T2.SEMESTER ");
            stb.append("         AND T1.CHAIRCD = T2.CHAIRCD ");
            stb.append("         AND T1.EXECUTEDATE BETWEEN T2.APPDATE AND T2.APPENDDATE ");
            stb.append("         AND T2.SCHREGNO IN (SELECT SCHREGNO FROM T_SCHNO) ");
            stb.append("     GROUP BY ");
            stb.append("         T1.EXECUTEDATE, ");
            stb.append("         T1.PERIODCD, ");
            stb.append("         T1.YEAR, ");
            stb.append("         T1.SEMESTER ");
            stb.append("     ) ");

            stb.append(" SELECT ");
            stb.append("     T1.EXECUTEDATE, ");
            stb.append("     T1.PERIODCD, ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append("     T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD AS SUBCLASSCD, ");
            } else {
                stb.append("     T2.SUBCLASSCD, ");
            }
            stb.append("     L1.SUBCLASSNAME ");
            stb.append(" FROM ");
            stb.append("     T_SCH_CHR T1 ");
            stb.append("     INNER JOIN CHAIR_DAT T2 ");
            stb.append("         ON  T1.YEAR = T2.YEAR ");
            stb.append("         AND T1.SEMESTER = T2.SEMESTER ");
            stb.append("         AND T1.CHAIRCD = T2.CHAIRCD ");
            stb.append("     LEFT JOIN SUBCLASS_MST L1 ");
            stb.append("         ON  L1.SUBCLASSCD = T2.SUBCLASSCD ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append("         AND L1.CLASSCD = T2.CLASSCD ");
                stb.append("         AND L1.SCHOOL_KIND = T2.SCHOOL_KIND ");
                stb.append("         AND L1.CURRICULUM_CD = T2.CURRICULUM_CD ");
            }
            stb.append(" ORDER BY ");
            stb.append("     T1.EXECUTEDATE, ");
            stb.append("     T1.PERIODCD ");
            return stb.toString();
        }

        private String getSemestername(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            String rtn = "";
            try {
                final String sql = " SELECT SEMESTERNAME FROM SEMESTER_MST T1 WHERE T1.YEAR = '" + _ctrlYear + "' AND T1.SEMESTER = '" + _ctrlSemester + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = null == rs.getString("SEMESTERNAME") ? "" : rs.getString("SEMESTERNAME");
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
                final String sql = " SELECT HR_NAME FROM SCHREG_REGD_HDAT T1 WHERE T1.YEAR = '" + _ctrlYear + "' AND T1.SEMESTER = '" + _ctrlSemester + "' AND T1.GRADE || T1.HR_CLASS = '" + _gradeHrclass + "' ";
                ps = db2.prepareStatement(sql);
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
            String rtn = null;
            try {
                final StringBuffer sql = new StringBuffer();
                sql.append(" SELECT VALUE(T2.STAFFNAME, VALUE(T3.STAFFNAME, T4.STAFFNAME)) AS STAFFNAME FROM SCHREG_REGD_HDAT T1 ");
                sql.append("     LEFT JOIN STAFF_MST T2 ON T2.STAFFCD = TR_CD1 ");
                sql.append("     LEFT JOIN STAFF_MST T3 ON T3.STAFFCD = TR_CD2 ");
                sql.append("     LEFT JOIN STAFF_MST T4 ON T4.STAFFCD = TR_CD3 ");
                sql.append(" WHERE T1.YEAR = '" + _ctrlYear + "' AND T1.SEMESTER = '" + _ctrlSemester + "' AND T1.GRADE || T1.HR_CLASS = '" + _gradeHrclass + "' ");
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString("STAFFNAME");
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }

        private String getTitle() {
            if (_printForm1) return "行事予定表";
            if (_printForm2) return "行事予定表";
            if (_printForm3) return "月間計画表";
            return "";
        }

        private String getFormName() {
            if (_printForm1) return "KNJS320_1.frm";
            if (_printForm2) return "KNJS320_2.frm";
            if (_printForm3) return "KNJS320_3.frm";
            return "";
        }

        /**
         * 校時のリストを得る。
         * @param db2
         * @return
         */
        private List getPeriodList(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final List periodList = new ArrayList();
            int line = 0;
            try {
                final String sql = getPeriodSql();
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String periodCd = rs.getString("NAMECD2");
                    final String name = rs.getString("NAME1");

                    line++;
                    final Period period = new Period(periodCd, name, line);
                    periodList.add(period);
                }
            } catch (SQLException ex) {
                log.debug("Exception:", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return periodList;
        }

        private String getPeriodSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     NAMECD2, ");
            stb.append("     NAME1 ");
            stb.append(" FROM ");
            stb.append("     V_NAME_MST T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _ctrlYear + "' AND ");
            stb.append("     T1.NAMECD1 = 'B001' ");
            stb.append(" ORDER BY ");
            stb.append("     NAMECD2 ");
            return stb.toString();
        }
    }
}

// eof