/*
 * $Id: 24ac83f299ebdda912718257a17c351dd450428b $
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

public class KNJMP952 {

    private static final Log log = LogFactory.getLog(KNJMP952.class);

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
        svf.VrSetForm("KNJMP952.frm", 1);
        final List printList = getList(db2);
        for (Iterator iterator = printList.iterator(); iterator.hasNext();) {
            OutGoDat outGoDat = (OutGoDat) iterator.next();
            svf.VrsOut("NENDO", nao_package.KenjaProperties.gengou(Integer.parseInt(outGoDat._year)) + "年度");
            svf.VrsOut("BILL_NAME", outGoDat._outgoLName);
            svf.VrsOut("ITEM_NAME", outGoDat._outgoMName);
            svf.VrsOut("BILL_NO", outGoDat._requestNo);
            svf.VrsOut("TOTAL_MONEY", outGoDat._requestGk);
            svf.VrsOut("RESOLUTION_REASON", outGoDat._requestReason);
            svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(outGoDat._requestDate));
            svf.VrsOut("NAME1", outGoDat._requestStaffName);
            svf.VrsOut("SCHOOL_NAME", _param._schoolName + "　学校長　殿");
            svf.VrsOut("OUTLINE1", "1".equals(outGoDat._outgoCheck1) ? "レ" : "");
            svf.VrsOut("OUTGO_CHECK1_DATE", KNJ_EditDate.h_format_JP(outGoDat._outgoCheck1Date));
            svf.VrsOut("NAME2", outGoDat._outgoStaffName);
            svf.VrsOut("SCHOOL_NAME2", _param._schoolName + "　学校長　殿");
            svf.VrsOut("OUTLINE2", "1".equals(outGoDat._outgoCheck2) ? "レ" : "");
            svf.VrsOut("OUTLINE3", "1".equals(outGoDat._outgoCheck3) ? "レ" : "");
            svf.VrsOut("AJUST1", "1".equals(outGoDat._outgoExpenseFlg) ? "○" : "");
            svf.VrsOut("AJUST2", "2".equals(outGoDat._outgoExpenseFlg) ? "○" : "");
            svf.VrsOut("OUTOGO_DATE", KNJ_EditDate.h_format_JP(outGoDat._outgoDate));
            svf.VrsOut("NUM", outGoDat._outgoCertificateCnt);

            int meisaiTotal = null != outGoDat._requestTesuuryou ? Integer.parseInt(outGoDat._requestTesuuryou) : 0;
            int meisaiTotalTax = 0;
            for (Iterator itMeisai = outGoDat._meisai.iterator(); itMeisai.hasNext();) {
                OutGoMeisai outgoMeisai = (OutGoMeisai) itMeisai.next();
                svf.VrsOutn("NO", Integer.parseInt(outgoMeisai._lineNo), outgoMeisai._lineNo);
                svf.VrsOutn("BILL_NAME1", Integer.parseInt(outgoMeisai._lineNo), outgoMeisai._commodityName);
                svf.VrsOutn("PRICE", Integer.parseInt(outgoMeisai._lineNo), outgoMeisai._commodityPrice);
                svf.VrsOutn("SUM", Integer.parseInt(outgoMeisai._lineNo), outgoMeisai._commodityCnt);
                svf.VrsOutn("SUB_TOTAL", Integer.parseInt(outgoMeisai._lineNo), outgoMeisai._totalPrice);
                final String remarkField = KNJ_EditEdit.getMS932ByteLength(outgoMeisai._remark) > 6 ? "2" : "1";
                svf.VrsOutn("REMARK" + remarkField, Integer.parseInt(outgoMeisai._lineNo), outgoMeisai._remark);
                meisaiTotal += Integer.parseInt(outgoMeisai._totalPrice);
                meisaiTotal += Integer.parseInt(outgoMeisai._totalTax);
                meisaiTotalTax += Integer.parseInt(outgoMeisai._totalTax);
            }
            svf.VrsOutn("BILL_NAME1", 6, "消　費　税");
            svf.VrsOutn("SUB_TOTAL", 6, String.valueOf(meisaiTotalTax));
            svf.VrsOutn("BILL_NAME1", 7, "振込手数料");
            svf.VrsOutn("SUB_TOTAL", 7, null != outGoDat._requestTesuuryou ? outGoDat._requestTesuuryou : "0");
            svf.VrsOutn("SUB_TOTAL", 8, String.valueOf(meisaiTotal));
            svf.VrEndPage();
            _hasData = true;
        }
    }

    private List getList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getOutGoSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String year                    = rs.getString("YEAR");
                final String outgoLcd                = rs.getString("OUTGO_L_CD");
                final String outgoLName              = rs.getString("LEVY_L_NAME");
                final String outgoMcd                = rs.getString("OUTGO_M_CD");
                final String outgoMName              = rs.getString("LEVY_M_NAME");
                final String requestNo               = rs.getString("REQUEST_NO");
                final String requestDate             = rs.getString("REQUEST_DATE");
                final String requestReason           = rs.getString("REQUEST_REASON");
                final String requestStaff            = rs.getString("REQUEST_STAFF");
                final String requestStaffName        = rs.getString("REQUEST_STAFF_NAME");
                final String requestGk               = rs.getString("REQUEST_GK");
                final String requestTesuuryou        = rs.getString("REQUEST_TESUURYOU");
                final String outgoCheck1             = rs.getString("OUTGO_CHECK1");
                final String outgoCheck1Date         = rs.getString("OUTGO_CHECK1_DATE");
                final String outgoCheck1Staff        = rs.getString("OUTGO_CHECK1_STAFF");
                final String outgoStaffName          = rs.getString("OUTGO_STAFF_NAME");
                final String outgoCheck2             = rs.getString("OUTGO_CHECK2");
                final String outgoCheck3             = rs.getString("OUTGO_CHECK3");
                final String outgoDate               = rs.getString("OUTGO_DATE");
                final String outgoExpenseFlg         = rs.getString("OUTGO_EXPENSE_FLG");
                final String outgoCertificateCnt     = rs.getString("OUTGO_CERTIFICATE_CNT");
                final String outgoCancel             = rs.getString("OUTGO_CANCEL");
                final String outgoApproval           = rs.getString("OUTGO_APPROVAL");
                final String kounyuNo                = rs.getString("KOUNYU_NO");
                final String sekouNo                 = rs.getString("SEKOU_NO");
                final String seisanNo                = rs.getString("SEISAN_NO");

                final OutGoDat outgoDat = new OutGoDat(db2, year, outgoLcd, outgoLName, outgoMcd, outgoMName, requestNo, requestDate,
                                                          requestReason, requestStaff, requestStaffName, requestGk, requestTesuuryou, outgoCheck1,
                                                          outgoCheck1Date, outgoCheck1Staff, outgoStaffName, outgoCheck2, outgoCheck3, outgoDate,
                                                          outgoExpenseFlg, outgoCertificateCnt, outgoCancel, outgoApproval, kounyuNo, sekouNo, seisanNo);
                outgoDat.setMeisai(db2);
                retList.add(outgoDat);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String getOutGoSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.*, ");
        stb.append("     STF.STAFFNAME AS REQUEST_STAFF_NAME, ");
        stb.append("     STF2.STAFFNAME AS OUTGO_STAFF_NAME, ");
        stb.append("     L1.LEVY_L_NAME, ");
        stb.append("     L2.LEVY_M_NAME ");
        stb.append(" FROM ");
        stb.append("     LEVY_REQUEST_OUTGO_DAT T1 ");
        stb.append("     LEFT JOIN STAFF_MST STF ON T1.REQUEST_STAFF = STF.STAFFCD ");
        stb.append("     LEFT JOIN STAFF_MST STF2 ON T1.OUTGO_CHECK1_STAFF = STF2.STAFFCD ");
        stb.append("     LEFT JOIN LEVY_L_MST L1 ON T1.YEAR = L1.YEAR ");
        stb.append("          AND T1.OUTGO_L_CD = L1.LEVY_L_CD ");
        stb.append("     LEFT JOIN LEVY_M_MST L2 ON T1.YEAR = L2.YEAR ");
        stb.append("          AND T1.OUTGO_L_CD = L2.LEVY_L_CD ");
        stb.append("          AND T1.OUTGO_M_CD = L2.LEVY_M_CD ");
        stb.append("          AND L2.LEVY_IN_OUT_DIV = '2' ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.OUTGO_L_CD || T1.OUTGO_M_CD = '" + _param._outgoLMcd + "' ");
        if (!"".equals(_param._requestNo)) {
            stb.append("     AND T1.REQUEST_NO = '" + _param._requestNo + "' ");
        }
        stb.append(" ORDER BY ");
        stb.append("     T1.OUTGO_L_CD, ");
        stb.append("     T1.OUTGO_M_CD, ");
        stb.append("     T1.REQUEST_NO ");
        return stb.toString();
    }

    private class OutGoDat {
        private final String _year;
        private final String _outgoLcd;
        private final String _outgoLName;
        private final String _outgoMcd;
        private final String _outgoMName;
        private final String _requestNo;
        private final String _requestDate;
        private final String _requestReason;
        private final String _requestStaff;
        private final String _requestStaffName;
        private final String _requestGk;
        private final String _requestTesuuryou;
        private final String _outgoCheck1;
        private final String _outgoCheck1Date;
        private final String _outgoCheck1Staff;
        private final String _outgoStaffName;
        private final String _outgoCheck2;
        private final String _outgoCheck3;
        private final String _outgoDate;
        private final String _outgoExpenseFlg;
        private final String _outgoCertificateCnt;
        private final String _outgoCancel;
        private final String _outgoApproval;
        private final String _kounyuNo;
        private final String _sekouNo;
        private final String _seisanNo;
        private final List _meisai;

        public OutGoDat(
                final DB2UDB db2,
                final String year,
                final String outgoLcd,
                final String outgoLName,
                final String outgoMcd,
                final String outgoMName,
                final String requestNo,
                final String requestDate,
                final String requestReason,
                final String requestStaff,
                final String requestStaffName,
                final String requestGk,
                final String requestTesuuryou,
                final String outgoCheck1,
                final String outgoCheck1Date,
                final String outgoCheck1Staff,
                final String outgoStaffName,
                final String outgoCheck2,
                final String outgoCheck3,
                final String outgoDate,
                final String outgoExpenseFlg,
                final String outgoCertificateCnt,
                final String outgoCancel,
                final String outgoApproval,
                final String kounyuNo,
                final String sekouNo,
                final String seisanNo
        ) {
            _year                   = year;
            _outgoLcd               = outgoLcd;
            _outgoLName             = outgoLName;
            _outgoMcd               = outgoMcd;
            _outgoMName             = outgoMName;
            _requestNo              = requestNo;
            _requestDate            = requestDate;
            _requestReason          = requestReason;
            _requestStaff           = requestStaff;
            _requestStaffName       = requestStaffName;
            _requestGk              = requestGk;
            _requestTesuuryou       = requestTesuuryou;
            _outgoCheck1            = outgoCheck1;
            _outgoCheck1Date        = outgoCheck1Date;
            _outgoCheck1Staff       = outgoCheck1Staff;
            _outgoStaffName         = outgoStaffName;
            _outgoCheck2            = outgoCheck2;
            _outgoCheck3            = outgoCheck3;
            _outgoDate              = outgoDate;
            _outgoExpenseFlg        = outgoExpenseFlg;
            _outgoCertificateCnt    = outgoCertificateCnt;
            _outgoCancel            = outgoCancel;
            _outgoApproval          = outgoApproval;
            _kounyuNo               = kounyuNo;
            _sekouNo                = sekouNo;
            _seisanNo               = seisanNo;
            _meisai = new ArrayList();
        }

        private void setMeisai(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getMesaiSql();
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    final String lineNo = rs.getString("LINE_NO");
                    final String commodityName = rs.getString("LEVY_S_NAME");
                    final String commodityPrice = rs.getString("COMMODITY_PRICE");
                    final String commodityCnt = rs.getString("COMMODITY_CNT");
                    final String totalPrice = rs.getString("TOTAL_PRICE_ZEINUKI");
                    final String totalTax = rs.getString("TOTAL_TAX");
                    final String remark = rs.getString("REMARK");

                    final OutGoMeisai outgoMeisai = new OutGoMeisai(lineNo, commodityName, commodityPrice, commodityCnt, totalPrice, totalTax, remark);
                    _meisai.add(outgoMeisai);
                }

            } catch (SQLException ex) {
                log.debug("Exception:", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private String getMesaiSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.*, ");
            stb.append("     L1.LEVY_S_NAME ");
            stb.append(" FROM ");
            stb.append("     LEVY_REQUEST_OUTGO_MEISAI_DAT T1 ");
            stb.append("     LEFT JOIN LEVY_S_MST L1 ON T1.YEAR = L1.YEAR ");
            stb.append("          AND T1.OUTGO_L_CD = L1.LEVY_L_CD ");
            stb.append("          AND T1.OUTGO_M_CD = L1.LEVY_M_CD ");
            stb.append("          AND T1.OUTGO_S_CD = L1.LEVY_S_CD ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _year + "' ");
            stb.append("     AND T1.OUTGO_L_CD = '" + _outgoLcd + "' ");
            stb.append("     AND T1.OUTGO_M_CD = '" + _outgoMcd + "' ");
            stb.append("     AND T1.REQUEST_NO = '" + _requestNo + "' ");
            stb.append(" ORDER BY ");
            stb.append("     T1.LINE_NO ");
            return stb.toString();
        }
    }

    private class OutGoMeisai {
        private final String _lineNo;
        private final String _commodityName;
        private final String _commodityPrice;
        private final String _commodityCnt;
        private final String _totalPrice;
        private final String _totalTax;
        private final String _remark;
        public OutGoMeisai(
                final String lineNo,
                final String commodityName,
                final String commodityPrice,
                final String commodityCnt,
                final String totalPrice,
                final String totalTax,
                final String remark
        ) {
            _lineNo         = lineNo;
            _commodityName  = commodityName;
            _commodityPrice = commodityPrice;
            _commodityCnt   = commodityCnt;
            _totalPrice     = totalPrice;
            _totalTax       = totalTax;
            _remark         = remark;
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
        private final String _outgoLMcd;
        private final String _requestNo;
        private final String _prgid;
        private final String _year;
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final String _schoolName;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _outgoLMcd = request.getParameter("OUTGO_L_M_CD");
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

