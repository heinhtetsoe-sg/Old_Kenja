/*
 * $Id: 55786fb10e6312c4d011c0da04554874e45668e0 $
 *
 * 作成日: 2016/02/16
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJB;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 * 学校教育システム 賢者 [時間割管理]  月別課題状況表
 */
public class KNJB132 {

    private static final Log log = LogFactory.getLog(KNJB132.class);

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

            printMain(db2, svf, _param);
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
    
    private void printMain(final DB2UDB db2, final Vrw32alp svf, final Param param) {
        final Map monthCountMap = getMonthCountMap(db2, param);
        final Map ruikeiMap = new HashMap();

        final String form = "KNJB132.frm";
        svf.VrSetForm(form, 4);
        
        svf.VrsOut("NENDO", KenjaProperties.gengou(Integer.parseInt(param._year)) + "年度　月別課題状況表"); // 年度
        svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(param._loginDate)); // 作成日
        
        svf.VrsOut("TOTAL_NAME", "月計"); // 累計名
        String ruikei = null;
        for (final Iterator mit = param._monthList.iterator(); mit.hasNext();) {
            final String month = (String) mit.next();
            final String i = String.valueOf(Integer.parseInt(month));
            final String count = (String) monthCountMap.get(month);

            svf.VrsOut("KADAI" + i, count); // 欠席
 
            if (NumberUtils.isDigits(count)) {
                if (null == ruikei) {
                    ruikei = "0";
                }
                ruikei = String.valueOf(Integer.parseInt(ruikei) + Integer.parseInt(count));
                ruikeiMap.put(month, ruikei);
            }
        }
        svf.VrEndRecord();
        
        svf.VrsOut("TOTAL_NAME", "累計"); // 累計名
        for (final Iterator mit = param._monthList.iterator(); mit.hasNext();) {
            final String month = (String) mit.next();
            final String i = String.valueOf(Integer.parseInt(month));
            final String count = (String) ruikeiMap.get(month);
                    
            svf.VrsOut("KADAI" + i, count); // 欠席
        }
        svf.VrEndRecord();
        _hasData = true;
    }

    private static Map getMonthCountMap(final DB2UDB db2, final Param param) {
        final Map rtn = new HashMap();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = sql(param);
            log.debug(" sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String month = rs.getString("MONTH");
                final String count = rs.getString("COUNT");
                rtn.put(month, count);
            }
        } catch (Exception ex) {
            log.fatal("exception!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return rtn;
    }

    private static String sql(final Param param) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH T_SCH_CHR_GRADE_HRCLASS AS ( ");
        stb.append(" SELECT DISTINCT ");
        stb.append("    T1.EXECUTEDATE, ");
        stb.append("    T1.PERIODCD, ");
        stb.append("    T1.CHAIRCD, ");
        stb.append("    REGD.GRADE, ");
        stb.append("    REGD.HR_CLASS, ");
        stb.append("    T1.YEAR ");
        stb.append(" FROM SCH_CHR_DAT T1 ");
        stb.append(" INNER JOIN SEMESTER_MST T2 ON T2.YEAR = T1.YEAR ");
        stb.append("     AND T2.SEMESTER <> '9' ");
        stb.append("     AND T1.EXECUTEDATE BETWEEN T2.SDATE AND T2.EDATE ");
        stb.append(" INNER JOIN CHAIR_DAT CHR ON CHR.YEAR = T2.YEAR ");
        stb.append("    AND CHR.SEMESTER = T2.SEMESTER ");
        stb.append("    AND CHR.CHAIRCD = T1.CHAIRCD ");
        stb.append(" INNER JOIN CHAIR_STD_DAT CSTD ON CSTD.YEAR = CHR.YEAR ");
        stb.append("    AND CSTD.SEMESTER = CHR.SEMESTER ");
        stb.append("    AND CSTD.CHAIRCD = CHR.CHAIRCD ");
        stb.append("    AND T1.EXECUTEDATE BETWEEN CSTD.APPDATE AND CSTD.APPENDDATE ");
        stb.append(" INNER JOIN SCHREG_REGD_DAT REGD ON REGD.SCHREGNO = CSTD.SCHREGNO ");
        stb.append("    AND REGD.YEAR = CSTD.YEAR ");
        stb.append("    AND REGD.SEMESTER = CSTD.SEMESTER ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + param._year + "' ");
        stb.append("     AND T1.EXECUTEDATE <= '" + param._date + "' ");
        stb.append(" ), MONTH_SCHEDULE_KADAI AS ( ");
        stb.append(" SELECT DISTINCT ");
        stb.append("    YEAR(T1.EXECUTEDATE) AS NEN, ");
        stb.append("    RIGHT('00' || RTRIM(CAST(MONTH(T1.EXECUTEDATE) AS CHAR(2))), 2) AS MONTH, ");
        stb.append("    T1.EXECUTEDATE, ");
        stb.append("    T1.PERIODCD, ");
        stb.append("    T1.CHAIRCD ");
        stb.append(" FROM T_SCH_CHR_GRADE_HRCLASS T1 ");
        stb.append(" INNER JOIN SCH_CHR_COUNTFLG T3 ON T3.EXECUTEDATE = T1.EXECUTEDATE ");
        stb.append("     AND T3.PERIODCD = T1.PERIODCD ");
        stb.append("     AND T3.CHAIRCD = T1.CHAIRCD ");
        stb.append("     AND T3.GRADE = T1.GRADE ");
        stb.append("     AND T3.HR_CLASS = T1.HR_CLASS ");
        stb.append("     AND T3.LESSON_MODE = '04' "); // 課題
        stb.append(" GROUP BY ");
        stb.append("    T1.EXECUTEDATE, ");
        stb.append("    T1.PERIODCD, ");
        stb.append("    T1.CHAIRCD ");
        stb.append(" )  ");
        stb.append("  SELECT ");
        stb.append("     T1.NEN, MONTH, COUNT(*) AS COUNT ");
        stb.append("  FROM MONTH_SCHEDULE_KADAI T1 ");
        stb.append("  GROUP BY T1.NEN, T1.MONTH ");
        stb.append("  ORDER BY T1.NEN, T1.MONTH ");
        stb.append("  ");
        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 56595 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(request, db2);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _year;
        final String _semester;
        final String _date;
        final String _loginDate;
        final String _month;
        final List _monthList = new ArrayList();

        Param(final HttpServletRequest request, final DB2UDB db2) {
            _year = request.getParameter("YEAR");
            _semester  = request.getParameter("SEMESTER");
            _date = StringUtils.replace(request.getParameter("DATE"), "/", "-");
            _loginDate = request.getParameter("LOGIN_DATE");
            
            Calendar cal = Calendar.getInstance();
            cal.setTime(Date.valueOf(_date));
            final int month = cal.get(Calendar.MONTH) + 1;
            _month = String.valueOf(month);
            
            final DecimalFormat df2 = new DecimalFormat("00");
            for (int m = 4; m <= month + (month <= 3 ? 12 : 0); m++) {
                _monthList.add(df2.format(m - (m > 12 ? 12 : 0)));
            }
        }
    }
}

// eof

