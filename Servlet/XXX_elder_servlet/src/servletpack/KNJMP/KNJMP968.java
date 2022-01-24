/*
 * $Id: 31c2caff66d9dad56e6c7b382d92cf986c201bdd $
 *
 * 作成日: 2015/03/13
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJMP;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;

public class KNJMP968 {

    private static final Log log = LogFactory.getLog(KNJMP968.class);

    private final int MAXLINE = 15;

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
        final List lineList = getList(db2);
        svf.VrSetForm(_param._frmName, 1);

        int zanDaka = 0;
        int lineCnt = 1;
        String befLMcd = "";
        for (Iterator itLine = lineList.iterator(); itLine.hasNext();) {
            LineData lineData = (LineData) itLine.next();
            final String lmCd = lineData._lcd + lineData._mcd;
            if (!"".equals(befLMcd) && !befLMcd.equals(lmCd)) {
                svf.VrEndPage();
                zanDaka = 0;
                lineCnt = 1;
            }
            if (MAXLINE < lineCnt) {
                svf.VrEndPage();
                lineCnt = 1;
            }

            svf.VrsOut("BILL_NAME1_1", lineData._lname);
            svf.VrsOut("CLAUSE", lineData._mname);

            final String[] dateArray = StringUtils.split(lineData._exeDate, "-");
            if (null != dateArray) {
                svf.VrsOutn("DATE", lineCnt, dateArray[1] + "/" + dateArray[2]);
            }
            svf.VrsOutn("REMARK1", lineCnt, lineData._requestReason);
            svf.VrsOutn("BILL_NO", lineCnt, lineData._requestNo);
            if ("1".equals(lineData._yosanDiv)) {
                svf.VrsOutn("BUDGET", lineCnt, lineData._requestGk);
                zanDaka = zanDaka + Integer.parseInt(lineData._requestGk);
            } else {
                svf.VrsOutn("COMP", lineCnt, lineData._requestGk);
                zanDaka = zanDaka - Integer.parseInt(lineData._requestGk);
            }

            svf.VrsOutn("YET", lineCnt, String.valueOf(zanDaka));
            _hasData = true;
            befLMcd = lmCd;
            lineCnt++;
        }
        if (_hasData) {
            svf.VrEndPage();
        }

    }

    private static int getMS932Length(final String s) {
        if (null != s) {
            try {
                return s.getBytes("MS932").length;
            } catch (Exception e) {
                log.fatal("exception!", e);
            }
        }
        return 0;
    }

    private List getList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getPrintDataSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String yosanDiv       = rs.getString("YOSAN_DIV");
                final String year           = rs.getString("YEAR");
                final String lcd            = rs.getString("LCD");
                final String lname          = rs.getString("LNAME");
                final String mcd            = rs.getString("MCD");
                final String mname          = rs.getString("MNAME");
                final String exeDate        = rs.getString("EXE_DATE");
                final String requestReason  = rs.getString("REQUEST_REASON");
                final String requestNo      = rs.getString("REQUEST_NO");
                final String requestGk      = rs.getString("REQUEST_GK");

                final LineData lineData = new LineData(yosanDiv, year, lcd, lname, mcd, mname, exeDate, requestReason, requestNo, requestGk);
                retList.add(lineData);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String getPrintDataSql() {
        final StringBuffer stb = new StringBuffer();
        final String inOutFieldName = "1".equals(_param._printDiv) ? "INCOME" : "OUTGO";
        stb.append(" WITH YOSAN_T AS ( ");
        stb.append(" SELECT ");
        stb.append("     '1' AS YOSAN_DIV, ");
        stb.append("     T1.YEAR, ");
        stb.append("     T1.YOSAN_L_CD AS LCD, ");
        stb.append("     L1.LEVY_L_NAME AS LNAME, ");
        stb.append("     T1.YOSAN_M_CD AS MCD, ");
        stb.append("     L2.LEVY_M_NAME AS MNAME, ");
        stb.append("     T1.REQUEST_DATE AS EXE_DATE, ");
        stb.append("     T1.REQUEST_REASON, ");
        stb.append("     T1.REQUEST_NO, ");
        stb.append("     VALUE(T1.REQUEST_GK, 0) AS REQUEST_GK ");
        stb.append(" FROM ");
        stb.append("     LEVY_REQUEST_YOSAN_DAT T1 ");
        stb.append("     LEFT JOIN LEVY_L_MST L1 ON T1.YEAR = L1.YEAR ");
        stb.append("          AND T1.YOSAN_L_CD = L1.LEVY_L_CD ");
        stb.append("     LEFT JOIN LEVY_M_MST L2 ON T1.YEAR = L2.YEAR ");
        stb.append("          AND T1.YOSAN_L_CD = L2.LEVY_L_CD ");
        stb.append("          AND T1.YOSAN_M_CD = L2.LEVY_M_CD ");
        stb.append("          AND L2.LEVY_IN_OUT_DIV = '" + _param._printDiv + "' ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.YOSAN_DIV = '" + _param._yosanDiv + "' ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     YOSAN_DIV, ");
        stb.append("     YEAR, ");
        stb.append("     LCD, ");
        stb.append("     LNAME, ");
        stb.append("     MCD, ");
        stb.append("     MNAME, ");
        stb.append("     EXE_DATE, ");
        stb.append("     REQUEST_REASON, ");
        stb.append("     REQUEST_NO, ");
        stb.append("     REQUEST_GK ");
        stb.append(" FROM ");
        stb.append("     YOSAN_T ");
        stb.append(" UNION ALL ");
        stb.append(" SELECT ");
        stb.append("     '2' AS YOSAN_DIV, ");
        stb.append("     T1.YEAR, ");
        stb.append("     T1." + inOutFieldName + "_L_CD AS LCD, ");
        stb.append("     L1.LEVY_L_NAME AS LNAME, ");
        stb.append("     T1." + inOutFieldName + "_M_CD AS MCD, ");
        stb.append("     L2.LEVY_M_NAME AS MNAME, ");
        stb.append("     T1." + inOutFieldName + "_DATE AS EXE_DATE, ");
        stb.append("     T1.REQUEST_REASON, ");
        stb.append("     T1.REQUEST_NO, ");
        stb.append("     VALUE(T1.REQUEST_GK, 0) AS REQUEST_GK ");
        stb.append(" FROM ");
        stb.append("     LEVY_REQUEST_" + inOutFieldName + "_DAT T1 ");
        stb.append("     INNER JOIN YOSAN_T I1 ON T1.YEAR = I1.YEAR ");
        stb.append("           AND T1." + inOutFieldName + "_L_CD = I1.LCD ");
        stb.append("           AND T1." + inOutFieldName + "_M_CD = I1.MCD ");
        stb.append("     LEFT JOIN LEVY_L_MST L1 ON T1.YEAR = L1.YEAR ");
        stb.append("          AND T1." + inOutFieldName + "_L_CD = L1.LEVY_L_CD ");
        stb.append("     LEFT JOIN LEVY_M_MST L2 ON T1.YEAR = L2.YEAR ");
        stb.append("          AND T1." + inOutFieldName + "_L_CD = L2.LEVY_L_CD ");
        stb.append("          AND T1." + inOutFieldName + "_M_CD = L2.LEVY_M_CD ");
        stb.append("          AND L2.LEVY_IN_OUT_DIV = '" + _param._printDiv + "' ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND VALUE(T1." + inOutFieldName + "_APPROVAL, '0') = '1' ");
        stb.append("     AND VALUE(T1." + inOutFieldName + "_CANCEL, '0') = '0' ");
        stb.append(" ORDER BY ");
        stb.append("     LCD, ");
        stb.append("     MCD, ");
        stb.append("     YOSAN_DIV, ");
        stb.append("     EXE_DATE ");

        return stb.toString();
    }

    private class ExeMonth {
        public String _hrName;
        private final String _month;
        private final List _lineList;
        public ExeMonth(
                final String month
        ) {
            _month   = month;
            _lineList = new ArrayList();
        }
    }

    private class LineData {
        final String _yosanDiv;
        final String _year;
        final String _lcd;
        final String _lname;
        final String _mcd;
        final String _mname;
        final String _exeDate;
        final String _requestReason;
        final String _requestNo;
        final String _requestGk;

        public LineData(
                final String yosanDiv,
                final String year,
                final String lcd,
                final String lname,
                final String mcd,
                final String mname,
                final String exeDate,
                final String requestReason,
                final String requestNo,
                final String requestGk
        ) {
            _yosanDiv       = yosanDiv;
            _year           = year;
            _lcd            = lcd;
            _lname          = lname;
            _mcd            = mcd;
            _mname          = mname;
            _exeDate        = exeDate;
            _requestReason  = requestReason;
            _requestNo      = requestNo;
            _requestGk      = requestGk;
        }

    }

    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 56595 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _printDiv;
        private final String _prgid;
        private final String _year;
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final String _schoolName;
        private final String _frmName;
        private final String _yosanDiv;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _printDiv = request.getParameter("PRINTDIV");
            _prgid = request.getParameter("PRGID");
            _year = request.getParameter("YEAR");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _schoolName = getSchoolName(db2, _year);
            _frmName = "1".equals(_printDiv) ? "KNJMP968_1.frm" : "KNJMP968_2.frm";
            _yosanDiv = "1".equals(_printDiv) ? "01" : "03";
        }

        private String getSchoolName(final DB2UDB db2, final String year) {
            String retSchoolName = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement("SELECT SCHOOLNAME1 FROM SCHOOL_MST WHERE YEAR = '" + year + "' ");
                rs = ps.executeQuery();
                if (rs.next()) {
                    retSchoolName = rs.getString("SCHOOLNAME1");
                }
            } catch (SQLException ex) {
                log.debug("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retSchoolName;
        }

    }
}

// eof

