/*
 * $Id: b1bbcf2bd8f075ebb7a998f497273bc002ed65e3 $
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

public class KNJMP955 {

    private static final Log log = LogFactory.getLog(KNJMP955.class);

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
        svf.VrSetForm("KNJMP955.frm", 1);
        final List printList = getList(db2);
        for (Iterator iterator = printList.iterator(); iterator.hasNext();) {
            SeisanDat seisanDat = (SeisanDat) iterator.next();
            final String[] setDate = KNJ_EditDate.tate3_format(KNJ_EditDate.h_format_JP(seisanDat._requestDate));
            String genNen = setDate[0] + setDate[1];
            if (null == seisanDat._requestDate) {
                genNen = "";
            }
            svf.VrsOut("NENDO", genNen);
            svf.VrsOut("BILL_NAME", seisanDat._seisanLName);
            svf.VrsOut("ITEM_NAME", seisanDat._seisanMName);
            svf.VrsOut("BILL_NO", seisanDat._outGoRequestNo);
            svf.VrsOut("TITLE", seisanDat._seisanTitle);
            svf.VrsOut("PRESENTEE", seisanDat._genkinStaffName);

            String[] arr = KNJ_EditEdit.get_token(seisanDat._seisanNaiyou, 78, 5);
            if (arr != null) {
                for (int i = 0; i < arr.length; i++) {
                    svf.VrsOut("ADJUST" + String.valueOf(i + 1), arr[i]);
                }
            }

            svf.VrsOut("RECEIPT_DATE", KNJ_EditDate.h_format_JP(seisanDat._juryouDate));
            svf.VrsOut("RECEIPT", seisanDat._juryouGk);
            svf.VrsOut("PAY_DATE", KNJ_EditDate.h_format_JP(seisanDat._siharaiDate));
            svf.VrsOut("PAY", seisanDat._siharaiGk);
            svf.VrsOut("REST", seisanDat._zanGk);
            svf.VrsOut("REMARK", seisanDat._remark);
            svf.VrsOut("ADJUST_DATE", KNJ_EditDate.h_format_JP(seisanDat._requestDate));
            svf.VrsOut("NAME1", seisanDat._requestStaffName);
            svf.VrsOut("NAME2", seisanDat._suitouStaffName);
            svf.VrsOut("DEPOSIT_DATE", KNJ_EditDate.h_format_JP(seisanDat._incomeDate));

            svf.VrEndPage();
            _hasData = true;
        }
    }

    private List getList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getSeisanSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String year               = rs.getString("YEAR");
                final String seisanLCd          = rs.getString("SEISAN_L_CD");
                final String seisanLName        = rs.getString("LEVY_L_NAME");
                final String seisanMCd          = rs.getString("SEISAN_M_CD");
                final String seisanMName        = rs.getString("LEVY_M_NAME");
                final String requestNo          = rs.getString("REQUEST_NO");
                final String requestDate        = rs.getString("REQUEST_DATE");
                final String requestReason      = rs.getString("REQUEST_REASON");
                final String requestStaff       = rs.getString("REQUEST_STAFF");
                final String requestStaffName   = rs.getString("REQUEST_STAFF_NAME");
                final String seisanTitle        = rs.getString("SEISAN_TITLE");
                final String genkinJuryouStaff  = rs.getString("GENKIN_JURYOU_STAFF");
                final String genkinStaffName    = rs.getString("GENKIN_JURYOU_STAFF_NAME");
                final String seisanNaiyou       = rs.getString("SEISAN_NAIYOU");
                final String juryouGk           = rs.getString("JURYOU_GK");
                final String juryouDate         = rs.getString("JURYOU_DATE");
                final String siharaiGk          = rs.getString("SIHARAI_GK");
                final String siharaiDate        = rs.getString("SIHARAI_DATE");
                final String zanGk              = rs.getString("ZAN_GK");
                final String remark             = rs.getString("REMARK");
                final String seisanApproval     = rs.getString("SEISAN_APPROVAL");
                final String suitouStaff        = rs.getString("SUITOU_STAFF");
                final String suitouStaffName    = rs.getString("SUITOU_STAFF_NAME");
                final String incomeDate         = rs.getString("INCOME_DATE");
                final String outGoRequestNo     = rs.getString("OUTGO_REQUEST_NO");

                final SeisanDat seisanDat = new SeisanDat(year, seisanLCd, seisanLName, seisanMCd, seisanMName, requestNo, requestDate, requestReason, requestStaff, requestStaffName, seisanTitle, genkinJuryouStaff, genkinStaffName, seisanNaiyou, juryouGk, juryouDate, siharaiGk, siharaiDate, zanGk, remark, seisanApproval, suitouStaff, suitouStaffName, incomeDate, outGoRequestNo);
                retList.add(seisanDat);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String getSeisanSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.*, ");
        stb.append("     STF.STAFFNAME AS REQUEST_STAFF_NAME, ");
        stb.append("     STF2.STAFFNAME AS GENKIN_JURYOU_STAFF_NAME, ");
        stb.append("     STF3.STAFFNAME AS SUITOU_STAFF_NAME, ");
        stb.append("     OUTGO.REQUEST_NO AS OUTGO_REQUEST_NO, ");
        stb.append("     L1.LEVY_L_NAME, ");
        stb.append("     L2.LEVY_M_NAME ");
        stb.append(" FROM ");
        stb.append("     LEVY_REQUEST_SEISAN_DAT T1 ");
        stb.append("     LEFT JOIN STAFF_MST STF ON T1.REQUEST_STAFF = STF.STAFFCD ");
        stb.append("     LEFT JOIN STAFF_MST STF2 ON T1.GENKIN_JURYOU_STAFF = STF2.STAFFCD ");
        stb.append("     LEFT JOIN STAFF_MST STF3 ON T1.SUITOU_STAFF = STF3.STAFFCD ");
        stb.append("     LEFT JOIN LEVY_L_MST L1 ON T1.YEAR = L1.YEAR ");
        stb.append("          AND T1.SEISAN_L_CD = L1.LEVY_L_CD ");
        stb.append("     LEFT JOIN LEVY_M_MST L2 ON T1.YEAR = L2.YEAR ");
        stb.append("          AND T1.SEISAN_L_CD = L2.LEVY_L_CD ");
        stb.append("          AND T1.SEISAN_M_CD = L2.LEVY_M_CD ");
        stb.append("          AND L2.LEVY_IN_OUT_DIV = '2' ");
        stb.append("     LEFT JOIN LEVY_REQUEST_OUTGO_DAT OUTGO ON T1.YEAR = OUTGO.YEAR ");
        stb.append("          AND T1.SEISAN_L_CD = OUTGO.OUTGO_L_CD ");
        stb.append("          AND T1.SEISAN_M_CD = OUTGO.OUTGO_M_CD ");
        stb.append("          AND T1.REQUEST_NO = OUTGO.SEISAN_NO ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SEISAN_L_CD || T1.SEISAN_M_CD = '" + _param._seisanLMcd + "' ");
        if (!"".equals(_param._requestNo)) {
            stb.append("     AND T1.REQUEST_NO = '" + _param._requestNo + "' ");
        }
        stb.append(" ORDER BY ");
        stb.append("     T1.SEISAN_L_CD, ");
        stb.append("     T1.SEISAN_M_CD, ");
        stb.append("     T1.REQUEST_NO ");
        return stb.toString();
    }

    private class SeisanDat {
        private final String _year;
        private final String _seisanLCd;
        private final String _seisanLName;
        private final String _seisanMCd;
        private final String _seisanMName;
        private final String _requestNo;
        private final String _requestDate;
        private final String _requestReason;
        private final String _requestStaff;
        private final String _requestStaffName;
        private final String _seisanTitle;
        private final String _genkinJuryouStaff;
        private final String _genkinStaffName;
        private final String _seisanNaiyou;
        private final String _juryouGk;
        private final String _juryouDate;
        private final String _siharaiGk;
        private final String _siharaiDate;
        private final String _zanGk;
        private final String _remark;
        private final String _seisanApproval;
        private final String _suitouStaff;
        private final String _suitouStaffName;
        private final String _incomeDate;
        private final String _outGoRequestNo;

        public SeisanDat(
                final String year,
                final String seisanLCd,
                final String seisanLName,
                final String seisanMCd,
                final String seisanMName,
                final String requestNo,
                final String requestDate,
                final String requestReason,
                final String requestStaff,
                final String requestStaffName,
                final String seisanTitle,
                final String genkinJuryouStaff,
                final String genkinStaffName,
                final String seisanNaiyou,
                final String juryouGk,
                final String juryouDate,
                final String siharaiGk,
                final String siharaiDate,
                final String zanGk,
                final String remark,
                final String seisanApproval,
                final String suitouStaff,
                final String suitouStaffName,
                final String incomeDate,
                final String outGoRequestNo
        ) {
            _year               = year;
            _seisanLCd          = seisanLCd;
            _seisanLName        = seisanLName;
            _seisanMCd          = seisanMCd;
            _seisanMName        = seisanMName;
            _requestNo          = requestNo;
            _requestDate        = requestDate;
            _requestReason      = requestReason;
            _requestStaff       = requestStaff;
            _requestStaffName   = requestStaffName;
            _seisanTitle        = seisanTitle;
            _genkinJuryouStaff  = genkinJuryouStaff;
            _genkinStaffName    = genkinStaffName;
            _seisanNaiyou       = seisanNaiyou;
            _juryouGk           = juryouGk;
            _juryouDate         = juryouDate;
            _siharaiGk          = siharaiGk;
            _siharaiDate        = siharaiDate;
            _zanGk              = zanGk;
            _remark             = remark;
            _seisanApproval     = seisanApproval;
            _suitouStaff        = suitouStaff;
            _suitouStaffName    = suitouStaffName;
            _incomeDate         = incomeDate;
            _outGoRequestNo     = outGoRequestNo;
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
        private final String _seisanLMcd;
        private final String _requestNo;
        private final String _prgid;
        private final String _year;
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final String _schoolName;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _seisanLMcd = request.getParameter("SEISAN_L_M_CD");
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

