/*
 * $Id: cac82dc3eaa59b161d2d2a3b1271270ba199095d $
 *
 * 作成日: 2018/02/01
 * 作成者: yogi
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJE;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

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

public class KNJE368 {

    private static final Log log = LogFactory.getLog(KNJE368.class);

    private boolean _hasData;

    private Param _param;
    private static int summaryterm = 5;

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
        String fschoolname = "";
        svf.VrSetForm("KNJE368.frm", 1);

        List collegeList = new ArrayList();
        final List printList = getList(db2);
        final int maxCol = 3;
        final int maxLine = 50;
    	final int entyear = Integer.parseInt(_param._loginYear);
        String beforestatcd = "";
        int printLine = 0;
        int cntCol = 1;
        int pageCnt = 1;

        setTitle(svf, pageCnt);//ヘッダ
        for (Iterator iterator = printList.iterator(); iterator.hasNext();) {
            final PrintData printData = (PrintData) iterator.next();
            if (cntCol >= maxCol) {
            	if (printLine >= maxLine) {
                    svf.VrEndPage();
                    pageCnt++;
                    setTitle(svf, pageCnt);//ヘッダ
                    cntCol = 1;
                    printLine = 0;
            	}
            } else {
            	if (printLine >= maxLine) {
            		cntCol++;
                    printLine = 0;
            	}
            }

            String outpt = "";
            if (printData._statcd != null) {
                if (!printData._statcd.equals(beforestatcd)) {
                	printLine++;
                    if (printData._schoolname != null) {
                        if (30 >= KNJ_EditEdit.getMS932ByteLength(printData._schoolname)) {
        	    	        outpt = "SCHOOL_NAME" + cntCol + "_" + 1;
                        } else {
        	    	        outpt = "SCHOOL_NAME" + cntCol + "_" + 2;
                        }
                        svf.VrsOutn(outpt , printLine, printData._schoolname);
                    } else {
    	    	        outpt = "SCHOOL_NAME" + cntCol + "_" + 1;
                        svf.VrsOutn(outpt , printLine, printData._statcd);
                    }
                }
            } else {
                if (!"".equals(beforestatcd)) {
                    printLine++;
                    outpt = "SCHOOL_NAME" + cntCol + "_" + 1;
                    svf.VrsOutn(outpt , printLine, "");
                }
            }
            beforestatcd = printData._statcd;
            //データ
	    	final String outidx = "NUM" + cntCol + "_" + (entyear - Integer.parseInt(printData._year) + 1);
            svf.VrsOutn(outidx , printLine, printData._cnt);

            _hasData = true;
        }
        if (_hasData) {
            svf.VrEndPage();
        }
    }

    private void setTitle(final Vrw32alp svf, final int page) {
    	final int setttlcol = 3;
    	final int setttlcnt = summaryterm;
    	final int entyear = Integer.parseInt(_param._loginYear);
    	final String datestr = _param._loginDate.replace('-', '/');

	    svf.VrsOut("TITLE", "大学別年間推移");
	    svf.VrsOut("DATE", datestr);
	    svf.VrsOut("PAGE", String.valueOf(page) + "頁");
    	for (int col = 1; col <= setttlcol;col++) {
    	    for (int idx = 1;idx <= setttlcnt;idx++) {
    	    	final String outidx = "YEAR" + col + "_" + idx;
    		    svf.VrsOut(outidx, String.valueOf(entyear - idx + 1) + "年");
    	    }
    	}
    }

    private List getList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final String sql = getSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String year = rs.getString("YEAR");
                final String statcd = rs.getString("STAT_CD");
                final String schoolname = rs.getString("SCHOOL_NAME");
                final String cnt = rs.getString("CNT");

                final PrintData printData = new PrintData(year, statcd, schoolname, cnt);
                retList.add(printData);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String getSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.YEAR AS YEAR, ");
        stb.append("     T1.STAT_CD AS STAT_CD, ");
        stb.append("     CL_MST.SCHOOL_NAME AS SCHOOL_NAME, ");
        stb.append("     count(*) AS CNT ");
        stb.append(" FROM ");
        stb.append("     AFT_GRAD_COURSE_DAT T1 ");
        stb.append("     LEFT JOIN COLLEGE_MST CL_MST ON T1.STAT_CD = CL_MST.SCHOOL_CD ");
        stb.append(" WHERE ");
        stb.append("     INTEGER(YEAR) between INTEGER('" + _param._loginYear + "') - " + summaryterm + " AND INTEGER('" + _param._loginYear + "')");
        stb.append("     AND SENKOU_KIND = '0' AND PLANSTAT = '1' ");
        stb.append(" GROUP BY ");
        stb.append("     T1.YEAR, T1.STAT_CD, CL_MST.SCHOOL_NAME ");
        stb.append(" ORDER BY ");
        stb.append("     SUBSTR(CHAR(DECIMAL(STAT_CD, 12, 0)),1,12), T1.YEAR DESC ");

        return stb.toString();
    }

    private class PrintData {
        final String _year;
        final String _statcd;
        final String _schoolname;
        final String _cnt;

        public PrintData(
                final String year,
                final String statcd,
                final String schoolname,
                final String cnt
        ) {
        	_year = year;
        	_statcd = statcd;
        	_schoolname = schoolname;
        	_cnt = cnt;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 58417 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _loginYear;
        private final String _loginDate;
        private final String _loginSemester;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _loginDate     = request.getParameter("LOGIN_DATE");
            _loginYear     = request.getParameter("LOGIN_YEAR");
            _loginSemester     = request.getParameter("LOGIN_SEMESTER");

        }

        private String getNameMst(final DB2UDB db2, final String field, final String namecd1, final String namecd2) {
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT " + field + " FROM NAME_MST WHERE NAMECD1 = '" + namecd1 + "' AND NAMECD2 = '" + namecd2 + "' ");
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString(field);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return rtn;
        }
    }
}

// eof
