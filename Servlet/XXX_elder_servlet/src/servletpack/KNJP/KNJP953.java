/*
 * $Id: 728e5ea60cc76dccd3eb0683cc87ced2f160f62b $
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

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJP953 {

    private static final Log log = LogFactory.getLog(KNJP953.class);

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
        svf.VrSetForm("KNJP953.frm", 1);
        final List printList = getList(db2);
        for (Iterator iterator = printList.iterator(); iterator.hasNext();) {
            KounyuDat kounyuDat = (KounyuDat) iterator.next();
            svf.VrsOut("NENDO", KNJ_EditDate.gengou(db2, Integer.parseInt(kounyuDat._year)) + "年度");
            svf.VrsOut("BILL_NAME", kounyuDat._kounyuLName);

            svf.VrsOut("ITEM_NAME", kounyuDat._kounyuMName);
            svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(db2, kounyuDat._requestDate));
            svf.VrsOut("NAME1", kounyuDat._requestStaffName);

            // 印鑑等
            printStamp(svf);

            svf.VrsOut("ESTIMATE1", kounyuDat._traderName1);
            svf.VrsOut("ESTIMATE2", kounyuDat._traderName2);
            svf.VrsOut("ESTIMATE3", kounyuDat._traderName3);
            svf.VrsOut("ESTIMATE4", kounyuDat._traderName4);

            svf.VrsOut("ESTIMATE_DATE", KNJ_EditDate.h_format_JP(db2, kounyuDat._kounyuMitumoriDate));
            svf.VrsOut("CONTRACT", kounyuDat._keiyakuHouhou);
            svf.VrsOut("PAYMENT_LIMIT", KNJ_EditDate.h_format_JP(db2, kounyuDat._nounyuLimitDate));
            svf.VrsOut("PAYMENT_PLACE", kounyuDat._nounyuPlace);

            String[] arr = KNJ_EditEdit.get_token(kounyuDat._remark, 70, 3);
            if (arr != null) {
                for (int i = 0; i < arr.length; i++) {
                    svf.VrsOut("OTHER" + String.valueOf(i + 1), arr[i]);
                }
            }

            int meisaiTotal = null != kounyuDat._requestTesuuryou ? Integer.parseInt(kounyuDat._requestTesuuryou) : 0;
            for (Iterator itMeisai = kounyuDat._meisai.iterator(); itMeisai.hasNext();) {
                KounyuMeisai kounyuMeisai = (KounyuMeisai) itMeisai.next();
                svf.VrsOutn("BILL_NAME1", Integer.parseInt(kounyuMeisai._lineNo), kounyuMeisai._commodityName);
                svf.VrsOutn("PRICE", Integer.parseInt(kounyuMeisai._lineNo), kounyuMeisai._commodityPrice);
                svf.VrsOutn("SUM", Integer.parseInt(kounyuMeisai._lineNo), kounyuMeisai._commodityCnt);
                svf.VrsOutn("SUB_TOTAL", Integer.parseInt(kounyuMeisai._lineNo), kounyuMeisai._totalPrice);
                final String remarkField = KNJ_EditEdit.getMS932ByteLength(kounyuMeisai._remark) > 40 ? "4_1" : KNJ_EditEdit.getMS932ByteLength(kounyuMeisai._remark) > 30 ? "3" : KNJ_EditEdit.getMS932ByteLength(kounyuMeisai._remark) > 20 ? "2" : "";
                svf.VrsOutn("REMARK" + remarkField, Integer.parseInt(kounyuMeisai._lineNo), kounyuMeisai._remark);
                meisaiTotal += Integer.parseInt(kounyuMeisai._totalPrice);
                meisaiTotal += Integer.parseInt(kounyuMeisai._totalTax);
            }
            svf.VrsOutn("SUB_TOTAL", 6, String.valueOf(meisaiTotal));
            svf.VrsOutn("KOME", 6, "※");
            svf.VrsOutn("REMARK", 6, "※契約後記入");
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
            final String sql = getKounyuSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String year                = rs.getString("YEAR");
                final String kounyuLCd           = rs.getString("KOUNYU_L_CD");
                final String kounyuLName             = rs.getString("LEVY_L_NAME");
                final String kounyuMCd           = rs.getString("KOUNYU_M_CD");
                final String kounyuMName             = rs.getString("LEVY_M_NAME");
                final String requestNo           = rs.getString("REQUEST_NO");
                final String requestDate         = rs.getString("REQUEST_DATE");
                final String requestReason       = rs.getString("REQUEST_REASON");
                final String requestStaff        = rs.getString("REQUEST_STAFF");
                final String requestStaffName        = rs.getString("REQUEST_STAFF_NAME");
                final String requestGk           = rs.getString("REQUEST_GK");
                final String requestTesuuryou    = rs.getString("REQUEST_TESUURYOU");
                final String traderCd1           = rs.getString("TRADER_CD1");
                final String traderName1         = rs.getString("T_NAME1");
                final String traderKakutei1      = rs.getString("TRADER_KAKUTEI1");
                final String traderCd2           = rs.getString("TRADER_CD2");
                final String traderName2         = rs.getString("T_NAME2");
                final String traderKakutei2      = rs.getString("TRADER_KAKUTEI2");
                final String traderCd3           = rs.getString("TRADER_CD3");
                final String traderName3         = rs.getString("T_NAME3");
                final String traderKakutei3      = rs.getString("TRADER_KAKUTEI3");
                final String traderCd4           = rs.getString("TRADER_CD4");
                final String traderName4         = rs.getString("T_NAME4");
                final String traderKakutei4      = rs.getString("TRADER_KAKUTEI4");
                final String kounyuMitumoriDate  = rs.getString("KOUNYU_MITUMORI_DATE");
                final String keiyakuHouhou       = rs.getString("KEIYAKU_HOUHOU");
                final String nounyuLimitDate     = rs.getString("NOUNYU_LIMIT_DATE");
                final String nounyuPlace         = rs.getString("NOUNYU_PLACE");
                final String remark              = rs.getString("REMARK");

                final KounyuDat kounyuDat = new KounyuDat(db2, year, kounyuLCd, kounyuLName, kounyuMCd, kounyuMName, requestNo, requestDate,
                        requestReason, requestStaff, requestStaffName, requestGk, requestTesuuryou, traderCd1, traderName1, traderKakutei1,
                        traderCd2, traderName2, traderKakutei2, traderCd3, traderName3, traderKakutei3, traderCd4, traderName4, traderKakutei4,
                        kounyuMitumoriDate, keiyakuHouhou, nounyuLimitDate, nounyuPlace, remark);
                kounyuDat.setMeisai(db2);
                retList.add(kounyuDat);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String getKounyuSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.*, ");
        stb.append("     STF.STAFFNAME AS REQUEST_STAFF_NAME, ");
        stb.append("     CASE WHEN VALUE(TRADER1.TRADER_NAME, '') <> '' THEN TRADER1.TRADER_NAME ELSE T1.TRADER_NAME1 END AS T_NAME1, ");
        stb.append("     CASE WHEN VALUE(TRADER2.TRADER_NAME, '') <> '' THEN TRADER2.TRADER_NAME ELSE T1.TRADER_NAME2 END AS T_NAME2, ");
        stb.append("     CASE WHEN VALUE(TRADER3.TRADER_NAME, '') <> '' THEN TRADER3.TRADER_NAME ELSE T1.TRADER_NAME3 END AS T_NAME3, ");
        stb.append("     CASE WHEN VALUE(TRADER4.TRADER_NAME, '') <> '' THEN TRADER4.TRADER_NAME ELSE T1.TRADER_NAME4 END AS T_NAME4, ");
        stb.append("     L1.LEVY_L_NAME, ");
        stb.append("     L2.LEVY_M_NAME ");
        stb.append(" FROM ");
        stb.append("     LEVY_REQUEST_KOUNYU_DAT T1 ");
        stb.append("     LEFT JOIN STAFF_MST STF ON T1.REQUEST_STAFF = STF.STAFFCD ");
        stb.append("     LEFT JOIN LEVY_L_MST L1 ON T1.SCHOOLCD = L1.SCHOOLCD ");
        stb.append("          AND T1.SCHOOL_KIND = L1.SCHOOL_KIND ");
        stb.append("          AND T1.YEAR = L1.YEAR ");
        stb.append("          AND T1.KOUNYU_L_CD = L1.LEVY_L_CD ");
        stb.append("     LEFT JOIN LEVY_M_MST L2 ON T1.SCHOOLCD = L2.SCHOOLCD ");
        stb.append("          AND T1.SCHOOL_KIND = L2.SCHOOL_KIND ");
        stb.append("          AND T1.YEAR = L2.YEAR ");
        stb.append("          AND T1.KOUNYU_L_CD = L2.LEVY_L_CD ");
        stb.append("          AND T1.KOUNYU_M_CD = L2.LEVY_M_CD ");
        stb.append("          AND L2.LEVY_IN_OUT_DIV = '2' ");
        stb.append("     LEFT JOIN TRADER_MST TRADER1 ON T1.TRADER_CD1 = TRADER1.TRADER_CD ");
        stb.append("     LEFT JOIN TRADER_MST TRADER2 ON T1.TRADER_CD2 = TRADER2.TRADER_CD ");
        stb.append("     LEFT JOIN TRADER_MST TRADER3 ON T1.TRADER_CD3 = TRADER3.TRADER_CD ");
        stb.append("     LEFT JOIN TRADER_MST TRADER4 ON T1.TRADER_CD4 = TRADER4.TRADER_CD ");
        stb.append(" WHERE ");
        stb.append("         T1.SCHOOLCD = '" + _param._schoolCd + "' ");
        stb.append("     AND T1.SCHOOL_KIND = '" + _param._schoolKind + "' ");
        stb.append("     AND T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.KOUNYU_L_CD || T1.KOUNYU_M_CD = '" + _param._kounyuLMcd + "' ");
        if (!"".equals(_param._requestNo)) {
            stb.append("     AND T1.REQUEST_NO = '" + _param._requestNo + "' ");
        }
        stb.append(" ORDER BY ");
        stb.append("     T1.KOUNYU_L_CD, ");
        stb.append("     T1.KOUNYU_M_CD, ");
        stb.append("     T1.REQUEST_NO ");
        return stb.toString();
    }

    private class KounyuDat {
        private final String _year;
        private final String _kounyuLCd;
        private final String _kounyuLName;
        private final String _kounyuMCd;
        private final String _kounyuMName;
        private final String _requestNo;
        private final String _requestDate;
        private final String _requestReason;
        private final String _requestStaff;
        private final String _requestStaffName;
        private final String _requestGk;
        private final String _requestTesuuryou;
        private final String _traderCd1;
        private final String _traderName1;
        private final String _traderKakutei1;
        private final String _traderCd2;
        private final String _traderName2;
        private final String _traderKakutei2;
        private final String _traderCd3;
        private final String _traderName3;
        private final String _traderKakutei3;
        private final String _traderCd4;
        private final String _traderName4;
        private final String _traderKakutei4;
        private final String _kounyuMitumoriDate;
        private final String _keiyakuHouhou;
        private final String _nounyuLimitDate;
        private final String _nounyuPlace;
        private final String _remark;
        private final List _meisai;
        public KounyuDat(
                final DB2UDB db2,
                final String year,
                final String kounyuLCd,
                final String kounyuLName,
                final String kounyuMCd,
                final String kounyuMName,
                final String requestNo,
                final String requestDate,
                final String requestReason,
                final String requestStaff,
                final String requestStaffName,
                final String requestGk,
                final String requestTesuuryou,
                final String traderCd1,
                final String traderName1,
                final String traderKakutei1,
                final String traderCd2,
                final String traderName2,
                final String traderKakutei2,
                final String traderCd3,
                final String traderName3,
                final String traderKakutei3,
                final String traderCd4,
                final String traderName4,
                final String traderKakutei4,
                final String kounyuMitumoriDate,
                final String keiyakuHouhou,
                final String nounyuLimitDate,
                final String nounyuPlace,
                final String remark
        ) {
            _year               = year;
            _kounyuLCd          = kounyuLCd;
            _kounyuLName        = kounyuLName;
            _kounyuMCd          = kounyuMCd;
            _kounyuMName        = kounyuMName;
            _requestNo          = requestNo;
            _requestDate        = requestDate;
            _requestReason      = requestReason;
            _requestStaff       = requestStaff;
            _requestStaffName   = requestStaffName;
            _requestGk          = requestGk;
            _requestTesuuryou   = requestTesuuryou;
            _traderCd1          = traderCd1;
            _traderName1        = traderName1;
            _traderKakutei1     = traderKakutei1;
            _traderCd2          = traderCd2;
            _traderName2        = traderName2;
            _traderKakutei2     = traderKakutei2;
            _traderCd3          = traderCd3;
            _traderName3        = traderName3;
            _traderKakutei3     = traderKakutei3;
            _traderCd4          = traderCd4;
            _traderName4        = traderName4;
            _traderKakutei4     = traderKakutei4;
            _kounyuMitumoriDate = kounyuMitumoriDate;
            _keiyakuHouhou      = keiyakuHouhou;
            _nounyuLimitDate    = nounyuLimitDate;
            _nounyuPlace        = nounyuPlace;
            _remark             = remark;
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

                    final KounyuMeisai kounyuMeisai = new KounyuMeisai(lineNo, commodityName, commodityPrice, commodityCnt, totalPrice, totalTax, remark);
                    _meisai.add(kounyuMeisai);
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
            stb.append("     LEVY_REQUEST_KOUNYU_MEISAI_DAT T1 ");
            stb.append("     LEFT JOIN LEVY_S_MST L1 ON T1.SCHOOLCD = L1.SCHOOLCD ");
            stb.append("          AND T1.SCHOOL_KIND = L1.SCHOOL_KIND ");
            stb.append("          AND T1.YEAR        = L1.YEAR ");
            stb.append("          AND T1.KOUNYU_L_CD = L1.LEVY_L_CD ");
            stb.append("          AND T1.KOUNYU_M_CD = L1.LEVY_M_CD ");
            stb.append("          AND T1.KOUNYU_S_CD = L1.LEVY_S_CD ");
            stb.append(" WHERE ");
            stb.append("     T1.SCHOOLCD = '" + _param._schoolCd + "' ");
            stb.append("     AND T1.SCHOOL_KIND = '" + _param._schoolKind + "' ");
            stb.append("     AND T1.YEAR = '" + _year + "' ");
            stb.append("     AND T1.KOUNYU_L_CD = '" + _kounyuLCd + "' ");
            stb.append("     AND T1.KOUNYU_M_CD = '" + _kounyuMCd + "' ");
            stb.append("     AND T1.REQUEST_NO = '" + _requestNo + "' ");
            stb.append(" ORDER BY ");
            stb.append("     T1.LINE_NO ");
            return stb.toString();
        }
    }

    private class KounyuMeisai {
        private final String _lineNo;
        private final String _commodityName;
        private final String _commodityPrice;
        private final String _commodityCnt;
        private final String _totalPrice;
        private final String _totalTax;
        private final String _remark;
        public KounyuMeisai(
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
            _commodityPrice = null != commodityPrice ? commodityPrice : "0";
            _commodityCnt   = commodityCnt;
            _totalPrice     = null != totalPrice ? totalPrice : "0";
            _totalTax       = null != totalTax ? totalTax : "0";
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
        log.fatal("$Revision: 63404 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _schoolCd;
        private final String _schoolKind;
        private final String _kounyuLMcd;
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
            _kounyuLMcd = request.getParameter("KOUNYU_L_M_CD");
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
            stb.append("     AND PROGRAMID   = 'KNJP953' ");
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

