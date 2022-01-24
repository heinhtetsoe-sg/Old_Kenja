/*
 * $Id: 793e780a4ed5631adf715c4b0dad995ae5862d5f $
 *
 * 作成日: 2015/08/27
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJB;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

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
import servletpack.KNJZ.detail.KNJ_EditDate;


/**
 *
 *    学校教育システム 賢者 [時間割管理]
 *
 *                    ＜ＫＮＪＢ２６１＞  自習状況一覧
 *
 */
public class KNJB261 {

    private static final Log log = LogFactory.getLog(KNJB261.class);

    private static final String FROM_TO_MARK = "\uFF5E";

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
     *  文字数を取得
     */
    private static int getMS932ByteLength(final String str) {
        int ret = 0;
        if (null != str) {
            try {
                ret = str.getBytes("MS932").length;                      //byte数を取得
            } catch (Exception ex) {
                log.error("retStringByteValue error!", ex);
            }
        }
        return ret;
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        final String form = "KNJB261.frm";
        
        final int maxRecord = 50;
        final String monthRange = calendarMonthname(_param._date1) + FROM_TO_MARK + calendarMonthname(_param._date2);
        final List pageList = getPageList(Schedule.getScheduleList(db2, _param), maxRecord);

        for (int pi = 0; pi < pageList.size(); pi++) {
            final List scheduleList = (List) pageList.get(pi);
            
            svf.VrSetForm(form, 4);

            for (int i = 0; i < scheduleList.size(); i++) {
                final Schedule schedule = (Schedule) scheduleList.get(i);
                
                svf.VrsOut("TITLE", KenjaProperties.gengou(Integer.parseInt(_param._ctrlYear)) + "年度　月別自習状況表"); // タイトル
                svf.VrsOut("PERIOD", monthRange); // 期間
                svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._ctrlDate)); // 日付

                svf.VrsOut("MONTH", schedule._month + "月"); // 月
                svf.VrsOut("CHAIR_CD", schedule._chaircd); // 講座コード
                svf.VrsOut("CHAIR_NAME" + (getMS932ByteLength(schedule._chairname) <= 20 ? "1" : getMS932ByteLength(schedule._chairname) <= 30 ? "2" : "3"), schedule._chairname); // 講座名
                svf.VrsOut("TEACHER_NAME", schedule._staffname); // 科目担任
                svf.VrsOut("DAY", schedule._day); // 日
                svf.VrsOut("TIME", schedule._periodAbbv); // 時限
//                svf.VrsOut("REMARK", null); // 備考
                svf.VrEndRecord();
            }
            _hasData = true;
        }
    }
    
    private static String calendarMonthname(final String date) {
        if (!StringUtils.isEmpty(date)) {
            try {
                final Date d = Date.valueOf(date);
                final Calendar cal = Calendar.getInstance();
                cal.setTime(d);
                return String.valueOf(cal.get(Calendar.MONTH) + 1) + "月";
            } catch (Exception e) {
                log.error("exception!", e);
            }
        }
        return "";
    }

    private static List getPageList(final List list, final int max) {
        final List pageList = new ArrayList();
        List current = null;
        for (int i = 0; i < list.size(); i++) {
            Object o = list.get(i);
            if (null == current || current.size() >= max) {
                current = new ArrayList();
                pageList.add(current);
            }
            current.add(o);
        }
        return pageList;
    }
    
    private static class Schedule {
        final String _executedate;
        final String _periodcd;
        final String _periodAbbv;
        final String _chaircd;
        final String _chairname;
        final String _staffcd;
        final String _staffname;
        final String _month;
        final String _day;

        Schedule(
            final String executedate,
            final String periodcd,
            final String periodAbbv,
            final String chaircd,
            final String chairname,
            final String staffcd1,
            final String staffname1,
            final String month,
            final String day
        ) {
            _executedate = executedate;
            _periodcd = periodcd;
            _periodAbbv = periodAbbv;
            _chaircd = chaircd;
            _chairname = chairname;
            _staffcd = staffcd1;
            _staffname = staffname1;
            _month = month;
            _day = day;
        }

        public static List getScheduleList(final DB2UDB db2, final Param param) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = sql(param);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String executedate = rs.getString("EXECUTEDATE");
                    final String periodcd = rs.getString("PERIODCD");
                    final String periodAbbv = rs.getString("PERIOD_ABBV");
                    final String chaircd = rs.getString("CHAIRCD");
                    final String chairname = rs.getString("CHAIRNAME");
                    final String staffcd = rs.getString("STAFFCD");
                    final String staffname = rs.getString("STAFFNAME");
                    final String month = rs.getString("MONTH");
                    final String day = rs.getString("DAY");
                    final Schedule schedule = new Schedule(executedate, periodcd, periodAbbv, chaircd, chairname, staffcd, staffname, month, day);
                    list.add(schedule);
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }

        public static String sql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append("  ");
            stb.append(" WITH CHAIR_STAFF AS ( ");
            stb.append("   SELECT  ");
            stb.append("       T1.YEAR ");
            stb.append("     , T1.SEMESTER ");
            stb.append("     , T1.CHAIRCD ");
            stb.append("     , T1.CHARGEDIV ");
            stb.append("     , STF.STAFFCD ");
            stb.append("     , STF.STAFFNAME ");
            stb.append("   FROM ( ");
            stb.append("     SELECT YEAR, SEMESTER, CHAIRCD, CHARGEDIV, MIN(STAFFCD) AS STAFFCD ");
            stb.append("     FROM CHAIR_STF_DAT T1 ");
            stb.append("     GROUP BY YEAR, SEMESTER, CHARGEDIV, CHAIRCD ");
            stb.append("   ) T1 ");
            stb.append("   LEFT JOIN STAFF_MST STF ON STF.STAFFCD = T1.STAFFCD ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     SCHE.EXECUTEDATE ");
            stb.append("   , SCHE.PERIODCD ");
            stb.append("   , NMB001.ABBV1 AS PERIOD_ABBV ");
            stb.append("   , SCHE.CHAIRCD ");
            stb.append("   , CHR.CHAIRNAME ");
            stb.append("   , STF.STAFFCD AS STAFFCD ");
            stb.append("   , STF.STAFFNAME AS STAFFNAME ");
            stb.append("   , MONTH(SCHE.EXECUTEDATE) AS MONTH ");
            stb.append("   , DAY(SCHE.EXECUTEDATE) AS DAY ");
            stb.append(" FROM SCH_CHR_DAT SCHE ");
            stb.append(" INNER JOIN CHAIR_DAT CHR ON CHR.YEAR = SCHE.YEAR ");
            stb.append("     AND CHR.SEMESTER = SCHE.SEMESTER ");
            stb.append("     AND CHR.CHAIRCD = SCHE.CHAIRCD ");
            stb.append(" LEFT JOIN CHAIR_STAFF STF ON STF.YEAR = CHR.YEAR ");
            stb.append("             AND STF.SEMESTER = CHR.SEMESTER ");
            stb.append("             AND STF.CHAIRCD = CHR.CHAIRCD ");
            stb.append("             AND STF.CHARGEDIV = 1 ");
            stb.append(" LEFT JOIN NAME_MST NMB001 ON NMB001.NAMECD1 = 'B001' ");
            stb.append("     AND NMB001.NAMECD2 = SCHE.PERIODCD ");
            stb.append(" WHERE ");
            stb.append("     SCHE.EXECUTEDATE BETWEEN '" + param._date1 + "' AND '" + param._date2 + "' ");
            stb.append("     AND SCHE.EXECUTEDIV = '3' "); // 自習
            stb.append(" ORDER BY ");
            stb.append("     YEAR(SCHE.EXECUTEDATE) ");
            stb.append("   , MONTH(SCHE.EXECUTEDATE) ");
            stb.append("   , SCHE.CHAIRCD ");
            stb.append("   , SCHE.EXECUTEDATE ");
            stb.append("   , SCHE.PERIODCD ");
            return stb.toString();
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
    private static class Param {
        final String _ctrlYear;
        final String _ctrlSemester;
        final String _ctrlDate;
        final String _date1;
        final String _date2;
        final String _useCurriculumcd;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = null == request.getParameter("CTRL_DATE") ? request.getParameter("CTRL_DATE") : request.getParameter("CTRL_DATE").replace('/', '-');
            _date1 = null == request.getParameter("DATE1") ? request.getParameter("DATE1") : request.getParameter("DATE1").replace('/', '-');
            _date2 = null == request.getParameter("DATE2") ? request.getParameter("DATE2") : request.getParameter("DATE2").replace('/', '-');
            _useCurriculumcd = request.getParameter("useCurriculumcd");
        }
    }
}

// eof

