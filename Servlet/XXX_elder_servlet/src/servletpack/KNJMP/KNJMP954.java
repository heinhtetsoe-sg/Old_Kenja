/*
 * $Id: 32bb39b15ef80038ba83f2f3f985fd2db357a8f9 $
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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJMP954 {

    private static final Log log = LogFactory.getLog(KNJMP954.class);

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
        svf.VrSetForm("KNJMP954.frm", 1);
        final List printList = getList(db2);
        for (Iterator iterator = printList.iterator(); iterator.hasNext();) {
            SekouDat sekouDat = (SekouDat) iterator.next();
            svf.VrsOut("NENDO", nao_package.KenjaProperties.gengou(Integer.parseInt(sekouDat._year)) + "年度");
            svf.VrsOut("BILL_NAME", sekouDat._sekouLName);
            svf.VrsOut("ITEM_NAME", sekouDat._sekouMName);
            svf.VrsOut("BILL_NO", sekouDat._requestNo);
            svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(sekouDat._requestDate));
            svf.VrsOut("NAME1", sekouDat._requestStaffName);

            svf.VrsOut("BUSINESS_NAME", sekouDat._sekouJigyouName);
            svf.VrsOut("OPERATION_CONTENT", sekouDat._sekouNaiyou);
            svf.VrsOut("OPERATION_PERIOD_FROM", KNJ_EditDate.h_format_JP(sekouDat._sekouDateFrom));
            svf.VrsOut("OPERATION_PERIOD_TO", KNJ_EditDate.h_format_JP(sekouDat._sekouDateTo));
            svf.VrsOut("OPERATION_PLACE", sekouDat._sekouPlace);
            svf.VrsOut("BUDGET", sekouDat._requestGk);
            svf.VrsOut("CONTRACT_METHOD", sekouDat._keiyakuHouhou);

            String[] arr = KNJ_EditEdit.get_token(sekouDat._remark, 70, 4);
            if (arr != null) {
                for (int i = 0; i < arr.length; i++) {
                    svf.VrsOut("MARK" + String.valueOf(i + 1), arr[i]);
                }
            }

            svf.VrEndPage();
            _hasData = true;
        }
    }

    private List getList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getSekouSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String year               = rs.getString("YEAR");
                final String sekouLCd           = rs.getString("SEKOU_L_CD");
                final String sekouLName        = rs.getString("LEVY_L_NAME");
                final String sekouMCd           = rs.getString("SEKOU_M_CD");
                final String sekouMName        = rs.getString("LEVY_M_NAME");
                final String requestNo          = rs.getString("REQUEST_NO");
                final String requestDate        = rs.getString("REQUEST_DATE");
                final String requestReason      = rs.getString("REQUEST_REASON");
                final String requestStaff       = rs.getString("REQUEST_STAFF");
                final String requestStaffName   = rs.getString("REQUEST_STAFF_NAME");
                final String requestGk          = rs.getString("REQUEST_GK");
                final String requestTesuuryou   = rs.getString("REQUEST_TESUURYOU");
                final String sekouJigyouName    = rs.getString("SEKOU_JIGYOU_NAME");
                final String sekouNaiyou        = rs.getString("SEKOU_NAIYOU");
                final String sekouDateFrom      = rs.getString("SEKOU_DATE_FROM");
                final String sekouDateTo        = rs.getString("SEKOU_DATE_TO");
                final String sekouPlace         = rs.getString("SEKOU_PLACE");
                final String keiyakuHouhou      = rs.getString("KEIYAKU_HOUHOU");
                final String remark             = rs.getString("REMARK");

                final SekouDat sekouDat = new SekouDat(year, sekouLCd, sekouLName, sekouMCd, sekouMName, requestNo, requestDate, requestReason, requestStaff, requestStaffName, requestGk, requestTesuuryou, sekouJigyouName, sekouNaiyou, sekouDateFrom, sekouDateTo, sekouPlace, keiyakuHouhou, remark);
                retList.add(sekouDat);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String getSekouSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.*, ");
        stb.append("     STF.STAFFNAME AS REQUEST_STAFF_NAME, ");
        stb.append("     L1.LEVY_L_NAME, ");
        stb.append("     L2.LEVY_M_NAME ");
        stb.append(" FROM ");
        stb.append("     LEVY_REQUEST_SEKOU_DAT T1 ");
        stb.append("     LEFT JOIN STAFF_MST STF ON T1.REQUEST_STAFF = STF.STAFFCD ");
        stb.append("     LEFT JOIN LEVY_L_MST L1 ON T1.YEAR = L1.YEAR ");
        stb.append("          AND T1.SEKOU_L_CD = L1.LEVY_L_CD ");
        stb.append("     LEFT JOIN LEVY_M_MST L2 ON T1.YEAR = L2.YEAR ");
        stb.append("          AND T1.SEKOU_L_CD = L2.LEVY_L_CD ");
        stb.append("          AND T1.SEKOU_M_CD = L2.LEVY_M_CD ");
        stb.append("          AND L2.LEVY_IN_OUT_DIV = '2' ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SEKOU_L_CD || T1.SEKOU_M_CD = '" + _param._sekouLMcd + "' ");
        if (!"".equals(_param._requestNo)) {
            stb.append("     AND T1.REQUEST_NO = '" + _param._requestNo + "' ");
        }
        stb.append(" ORDER BY ");
        stb.append("     T1.SEKOU_L_CD, ");
        stb.append("     T1.SEKOU_M_CD, ");
        stb.append("     T1.REQUEST_NO ");
        return stb.toString();
    }

    private class SekouDat {
        private final String _year;
        private final String _sekouLCd;
        private final String _sekouLName;
        private final String _sekouMCd;
        private final String _sekouMName;
        private final String _requestNo;
        private final String _requestDate;
        private final String _requestReason;
        private final String _requestStaff;
        private final String _requestStaffName;
        private final String _requestGk;
        private final String _requestTesuuryou;
        private final String _sekouJigyouName;
        private final String _sekouNaiyou;
        private final String _sekouDateFrom;
        private final String _sekouDateTo;
        private final String _sekouPlace;
        private final String _keiyakuHouhou;
        private final String _remark;

        public SekouDat(
                final String year,
                final String sekouLCd,
                final String sekouLName,
                final String sekouMCd,
                final String sekouMName,
                final String requestNo,
                final String requestDate,
                final String requestReason,
                final String requestStaff,
                final String requestStaffName,
                final String requestGk,
                final String requestTesuuryou,
                final String sekouJigyouName,
                final String sekouNaiyou,
                final String sekouDateFrom,
                final String sekouDateTo,
                final String sekouPlace,
                final String keiyakuHouhou,
                final String remark
        ) {
            _year               = year;
            _sekouLCd           = sekouLCd;
            _sekouLName         = sekouLName;
            _sekouMCd           = sekouMCd;
            _sekouMName         = sekouMName;
            _requestNo          = requestNo;
            _requestDate        = requestDate;
            _requestReason      = requestReason;
            _requestStaff       = requestStaff;
            _requestStaffName   = requestStaffName;
            _requestGk          = requestGk;
            _requestTesuuryou   = requestTesuuryou;
            _sekouJigyouName    = sekouJigyouName;
            _sekouNaiyou        = sekouNaiyou;
            _sekouDateFrom      = sekouDateFrom;
            _sekouDateTo        = sekouDateTo;
            _sekouPlace         = sekouPlace;
            _keiyakuHouhou      = keiyakuHouhou;
            _remark             = remark;
        }

    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 56595 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _sekouLMcd;
        private final String _requestNo;
        private final String _prgid;
        private final String _year;
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final String _schoolName;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _sekouLMcd = request.getParameter("SEKOU_L_M_CD");
            _requestNo = request.getParameter("REQUEST_NO");
            _prgid = request.getParameter("PRGID");
            _year = request.getParameter("YEAR");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _schoolName = getSchoolName(db2, _year);
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

