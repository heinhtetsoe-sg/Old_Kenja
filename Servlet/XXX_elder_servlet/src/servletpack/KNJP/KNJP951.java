/*
 * $Id: 821ebc41d34a5145f3262c9b94ab52fc8bd134b2 $
 *
 * 作成日: 2015/03/13
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJP;


import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJP951 {

    private static final Log log = LogFactory.getLog(KNJP951.class);

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
        svf.VrSetForm("KNJP951.frm", 1);
        final List printList = getList(db2);
        for (Iterator iterator = printList.iterator(); iterator.hasNext();) {
            IncomeDat incomeDat = (IncomeDat) iterator.next();
//            svf.VrsOut("NENDO", KNJ_EditDate.getAutoFormatYear(db2, Integer.parseInt(incomeDat._year)) + "年度");
            svf.VrsOut("NENDO", incomeDat._year + "年度");
            svf.VrsOut("BILL_NAME", incomeDat._incomeLName);
            svf.VrsOut("BILL_NO", incomeDat._requestNo);

            // 印鑑等
            printStamp(svf);

            svf.VrsOut("TOTAL_MONEY", "\\" + incomeDat._requestGk);
            svf.VrsOut("RESOLUTION_REASON", incomeDat._requestReason);
//            svf.VrsOut("DATE", KNJ_EditDate.getAutoFormatDate(db2, incomeDat._requestDate));
            svf.VrsOut("DATE", KNJ_EditDate.h_format_SeirekiJP(incomeDat._requestDate));
            int meisaiTotal = 0;
            for (Iterator itMeisai = incomeDat._meisai.iterator(); itMeisai.hasNext();) {
                IncomeMeisai incomeMeisai = (IncomeMeisai) itMeisai.next();
                svf.VrsOutn("NO", Integer.parseInt(incomeMeisai._lineNo), incomeMeisai._lineNo);
                final String billField = KNJ_EditEdit.getMS932ByteLength(incomeMeisai._commodityName) > 32 ? "3_1" : KNJ_EditEdit.getMS932ByteLength(incomeMeisai._commodityName) > 28 ? "2" : "1";
                svf.VrsOutn("BILL_NAME" + billField, Integer.parseInt(incomeMeisai._lineNo), incomeMeisai._commodityName);
                svf.VrsOutn("PRICE", Integer.parseInt(incomeMeisai._lineNo), incomeMeisai._commodityPrice);
                svf.VrsOutn("SUM", Integer.parseInt(incomeMeisai._lineNo), incomeMeisai._commodityCnt);
                svf.VrsOutn("SUB_TOTAL", Integer.parseInt(incomeMeisai._lineNo), incomeMeisai._totalPrice);
                final String remarkField = KNJ_EditEdit.getMS932ByteLength(incomeMeisai._remark) > 10 ? "3_1" : KNJ_EditEdit.getMS932ByteLength(incomeMeisai._remark) > 6 ? "2" : "";
                svf.VrsOutn("REMARK" + remarkField, Integer.parseInt(incomeMeisai._lineNo), incomeMeisai._remark);
                meisaiTotal += Integer.parseInt(incomeMeisai._totalPrice);
            }
            svf.VrsOutn("SUB_TOTAL", 7, String.valueOf(meisaiTotal));
            svf.VrEndPage();
            _hasData = true;
        }
    }

    /**
     * 印鑑
     */
    private void printStamp(final Vrw32alp svf) {
        for (Iterator iterator = _param._stampData.keySet().iterator(); iterator.hasNext();) {
            final String key = (String) iterator.next();
            final StampData stampData = (StampData) _param._stampData.get(key);

            // 役職
            svf.VrsOut("JOB_NAME" + (Integer.parseInt(stampData._seq)), stampData._title);

            // 印鑑
            svf.VrsOut("STAMP" + (Integer.parseInt(stampData._seq)), _param.getStampImageFile(stampData._stampName));
        }
    }

    private List getList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getIncomeSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String year                    = rs.getString("YEAR");
                final String incomeLcd               = rs.getString("INCOME_L_CD");
                final String incomeLName             = rs.getString("LEVY_L_NAME");
                final String incomeMcd               = rs.getString("INCOME_M_CD");
                final String incomeMName             = rs.getString("LEVY_M_NAME");
                final String requestNo               = rs.getString("REQUEST_NO");
                final String requestDate             = rs.getString("REQUEST_DATE");
                final String requestReason           = rs.getString("REQUEST_REASON");
                final String requestStaff            = rs.getString("REQUEST_STAFF");
                final String requestStaffName        = rs.getString("REQUEST_STAFF_NAME");
                final String requestGk               = rs.getString("REQUEST_GK");
                final String collectLcd              = rs.getString("COLLECT_L_CD");
                final String collectMcd              = rs.getString("COLLECT_M_CD");
                final String collectScd              = rs.getString("COLLECT_S_CD");
                final String incomeApproval          = rs.getString("INCOME_APPROVAL");
                final String incomeCancel            = rs.getString("INCOME_CANCEL");
                final String incomeDiv               = rs.getString("INCOME_DIV");
                final String incomeDate              = rs.getString("INCOME_DATE");
                final String incomeNo                = rs.getString("INCOME_NO");
                final String incomeStaff             = rs.getString("INCOME_STAFF");
                final String incomeStaffName         = rs.getString("INCOME_STAFF_NAME");
                final String incomeCertificateCnt    = rs.getString("INCOME_CERTIFICATE_CNT");

                final IncomeDat incomeDat = new IncomeDat(db2, year, incomeLcd, incomeLName, incomeMcd, incomeMName, requestNo, requestDate,
                                                          requestReason, requestStaff, requestStaffName, requestGk, collectLcd, collectMcd,
                                                          collectScd, incomeApproval, incomeCancel, incomeDiv, incomeDate, incomeNo,
                                                          incomeStaff, incomeStaffName, incomeCertificateCnt);
                incomeDat.setMeisai(db2);
                retList.add(incomeDat);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String getIncomeSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.*, ");
        stb.append("     STF.STAFFNAME AS REQUEST_STAFF_NAME, ");
        stb.append("     STF2.STAFFNAME AS INCOME_STAFF_NAME, ");
        stb.append("     L1.LEVY_L_NAME, ");
        stb.append("     L2.LEVY_M_NAME ");
        stb.append(" FROM ");
        stb.append("     LEVY_REQUEST_INCOME_DAT T1 ");
        stb.append("     LEFT JOIN STAFF_MST STF ON T1.REQUEST_STAFF = STF.STAFFCD ");
        stb.append("     LEFT JOIN STAFF_MST STF2 ON T1.INCOME_STAFF = STF2.STAFFCD ");
        stb.append("     LEFT JOIN LEVY_L_MST L1 ON T1.SCHOOLCD = L1.SCHOOLCD ");
        stb.append("          AND T1.SCHOOL_KIND = L1.SCHOOL_KIND ");
        stb.append("          AND T1.YEAR = L1.YEAR ");
        stb.append("          AND T1.INCOME_L_CD = L1.LEVY_L_CD ");
        stb.append("     LEFT JOIN LEVY_M_MST L2 ON T1.SCHOOLCD = L2.SCHOOLCD ");
        stb.append("          AND T1.SCHOOL_KIND = L2.SCHOOL_KIND ");
        stb.append("          AND T1.YEAR = L2.YEAR ");
        stb.append("          AND T1.INCOME_L_CD = L2.LEVY_L_CD ");
        stb.append("          AND T1.INCOME_M_CD = L2.LEVY_M_CD ");
        stb.append("          AND L2.LEVY_IN_OUT_DIV = '1' ");
        stb.append(" WHERE ");
        stb.append("     T1.SCHOOLCD = '" + _param._schoolCd + "' ");
        stb.append("     AND T1.SCHOOL_KIND = '" + _param._schoolKind + "' ");
        stb.append("     AND T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.INCOME_L_CD || T1.INCOME_M_CD = '" + _param._incomeLMcd + "' ");
        if (!"".equals(_param._requestNo)) {
            stb.append("     AND T1.REQUEST_NO = '" + _param._requestNo + "' ");
        }
        stb.append(" ORDER BY ");
        stb.append("     T1.INCOME_L_CD, ");
        stb.append("     T1.INCOME_M_CD, ");
        stb.append("     T1.REQUEST_NO ");
        return stb.toString();
    }

    private class IncomeDat {
        private final String _year;
        private final String _incomeLcd;
        private final String _incomeLName;
        private final String _incomeMcd;
        private final String _incomeMName;
        private final String _requestNo;
        private final String _requestDate;
        private final String _requestReason;
        private final String _requestStaff;
        private final String _requestStaffName;
        private final String _requestGk;
        private final String _collectLcd;
        private final String _collectMcd;
        private final String _collectScd;
        private final String _incomeApproval;
        private final String _incomeCancel;
        private final String _incomeDiv;
        private final String _incomeDate;
        private final String _incomeNo;
        private final String _incomeStaff;
        private final String _incomeStaffName;
        private final String _incomeCertificateCnt;
        private final List _meisai;
        public IncomeDat(
                final DB2UDB db2,
                final String year,
                final String incomeLcd,
                final String incomeLName,
                final String incomeMcd,
                final String incomeMName,
                final String requestNo,
                final String requestDate,
                final String requestReason,
                final String requestStaff,
                final String requestStaffName,
                final String requestGk,
                final String collectLcd,
                final String collectMcd,
                final String collectScd,
                final String incomeApproval,
                final String incomeCancel,
                final String incomeDiv,
                final String incomeDate,
                final String incomeNo,
                final String incomeStaff,
                final String incomeStaffName,
                final String incomeCertificateCnt
        ) {
            _year                    = year;
            _incomeLcd               = incomeLcd;
            _incomeLName             = incomeLName;
            _incomeMcd               = incomeMcd;
            _incomeMName             = incomeMName;
            _requestNo               = requestNo;
            _requestDate             = requestDate;
            _requestReason           = requestReason;
            _requestStaff            = requestStaff;
            _requestStaffName        = requestStaffName;
            _requestGk               = requestGk;
            _collectLcd              = collectLcd;
            _collectMcd              = collectMcd;
            _collectScd              = collectScd;
            _incomeApproval          = incomeApproval;
            _incomeCancel            = incomeCancel;
            _incomeDiv               = incomeDiv;
            _incomeDate              = incomeDate;
            _incomeNo                = incomeNo;
            _incomeStaff             = incomeStaff;
            _incomeStaffName         = incomeStaffName;
            _incomeCertificateCnt    = incomeCertificateCnt;
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
                    final String commodityName = rs.getString("COMMODITY_NAME");
                    final String commodityPrice = rs.getString("COMMODITY_PRICE");
                    final String commodityCnt = rs.getString("COMMODITY_CNT");
                    final String totalPrice = rs.getString("TOTAL_PRICE");
                    final String remark = rs.getString("REMARK");

                    final IncomeMeisai incomeMeisai = new IncomeMeisai(lineNo, commodityName, commodityPrice, commodityCnt, totalPrice, remark);
                    _meisai.add(incomeMeisai);
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
            stb.append("     * ");
            stb.append(" FROM ");
            stb.append("     LEVY_REQUEST_INCOME_MEISAI_DAT ");
            stb.append(" WHERE ");
            stb.append("     SCHOOLCD = '" + _param._schoolCd + "' ");
            stb.append("     AND SCHOOL_KIND = '" + _param._schoolKind + "' ");
            stb.append("     AND YEAR = '" + _year + "' ");
            stb.append("     AND INCOME_L_CD = '" + _incomeLcd + "' ");
            stb.append("     AND INCOME_M_CD = '" + _incomeMcd + "' ");
            stb.append("     AND REQUEST_NO = '" + _requestNo + "' ");
            stb.append(" ORDER BY ");
            stb.append("     LINE_NO ");
            return stb.toString();
        }
    }

    private class IncomeMeisai {
        private final String _lineNo;
        private final String _commodityName;
        private final String _commodityPrice;
        private final String _commodityCnt;
        private final String _totalPrice;
        private final String _remark;
        public IncomeMeisai(
                final String lineNo,
                final String commodityName,
                final String commodityPrice,
                final String commodityCnt,
                final String totalPrice,
                final String remark
        ) {
            _lineNo         = lineNo;
            _commodityName  = commodityName;
            _commodityPrice = commodityPrice;
            _commodityCnt   = commodityCnt;
            _totalPrice     = totalPrice;
            _remark         = remark;
        }
    }

    private class StampData {
        private final String _seq;
        private final String _title;
        private final String _stampName;
        public StampData(
                final String seq,
                final String title,
                final String stampName
                ) {
            _seq       = seq;
            _title     = title;
            _stampName = stampName;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 63750 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _schoolCd;
        private final String _schoolKind;
        private final String _incomeLMcd;
        private final String _requestNo;
        private final String _prgid;
        private final String _year;
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final String _schoolName;
        /** 写真データ格納フォルダ */
        private final String _imageDir;
        /** 写真データの拡張子 */
        private final String _imageExt;
        /** 陰影保管場所(陰影出力に関係する) */
        private final String _documentRoot;
        private final Map _stampData;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _schoolCd = request.getParameter("SCHOOLCD");
            _schoolKind = request.getParameter("SCHOOL_KIND");
            _incomeLMcd = request.getParameter("INCOME_L_M_CD");
            _requestNo = request.getParameter("REQUEST_NO");
            _prgid = request.getParameter("PRGID");
            _year = request.getParameter("YEAR");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _schoolName = getSchoolName(db2, _year);
            _imageDir = "image/stamp";
            _imageExt = "bmp";
            _documentRoot = request.getParameter("DOCUMENTROOT"); // 陰影保管場所 NO001
            _stampData = getStampData(db2);
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

        private Map getStampData(final DB2UDB db2) throws SQLException {
            final Map retMap = new TreeMap();
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
                    final StampData stampData = new StampData(rs.getString("SEQ"), rs.getString("TITLE"), stampNo);
                    retMap.put(rs.getString("SEQ"), stampData);
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                DbUtils.closeQuietly(null, psStampNo, rsStampNo);
                db2.commit();
            }
            return retMap;
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
            stb.append("         YEAR        = '" + _year + "' ");
            stb.append("     AND SEMESTER    = '9' ");
            stb.append("     AND SCHOOLCD    = '" + _schoolCd + "' ");
            stb.append("     AND SCHOOL_KIND = '" + _schoolKind + "' ");
            stb.append("     AND PROGRAMID   = 'KNJP951' ");
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

        /**
         * 写真データファイルの取得
         */
        private String getStampImageFile(final String filename) {
            if (null == filename) {
                return null;
            }
            if (null == _documentRoot) {
                return null;
            } // DOCUMENTROOT
            if (null == _imageDir) {
                return null;
            }
            if (null == _imageExt) {
                return null;
            }
            final StringBuffer stb = new StringBuffer();
            stb.append(_documentRoot);
            stb.append("/");
            stb.append(_imageDir);
            stb.append("/");
            stb.append(filename);
            stb.append(".");
            stb.append(_imageExt);
            File file1 = new File(stb.toString());
            if (!file1.exists()) {
                return null;
            } // 写真データ存在チェック用
            return stb.toString();
        }

    }
}

// eof

