/*
 * $Id: 38c8c4d36faa8a5777bf17c4cd5a59dd01e05211 $
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

public class KNJP954 {

    private static final Log log = LogFactory.getLog(KNJP954.class);

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
        svf.VrSetForm("KNJP954.frm", 1);
        final List printList = getList(db2);
        for (Iterator iterator = printList.iterator(); iterator.hasNext();) {
            SekouDat sekouDat = (SekouDat) iterator.next();
            svf.VrsOut("NENDO", nao_package.KenjaProperties.gengou(Integer.parseInt(sekouDat._year)) + "年度");
            svf.VrsOut("BILL_NAME", sekouDat._sekouLName);
            svf.VrsOut("ITEM_NAME", sekouDat._sekouMName);
            svf.VrsOut("BILL_NO", sekouDat._requestNo);
            svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(db2, sekouDat._requestDate));
            svf.VrsOut("NAME1", sekouDat._requestStaffName);

            // 印鑑等
            printStamp(svf);

            svf.VrsOut("BUSINESS_NAME", sekouDat._sekouJigyouName);
            svf.VrsOut("OPERATION_CONTENT", sekouDat._sekouNaiyou);
            svf.VrsOut("OPERATION_PERIOD_FROM", KNJ_EditDate.h_format_JP(db2, sekouDat._sekouDateFrom));
            svf.VrsOut("OPERATION_PERIOD_TO", KNJ_EditDate.h_format_JP(db2, sekouDat._sekouDateTo));
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
        stb.append("     LEFT JOIN LEVY_L_MST L1 ON T1.SCHOOLCD    = L1.SCHOOLCD ");
        stb.append("                            AND T1.SCHOOL_KIND = L1.SCHOOL_KIND ");
        stb.append("                            AND T1.YEAR        = L1.YEAR ");
        stb.append("                            AND T1.SEKOU_L_CD  = L1.LEVY_L_CD ");
        stb.append("     LEFT JOIN LEVY_M_MST L2 ON T1.SCHOOLCD    = L2.SCHOOLCD ");
        stb.append("                            AND T1.SCHOOL_KIND = L2.SCHOOL_KIND ");
        stb.append("                            AND T1.YEAR        = L2.YEAR ");
        stb.append("                            AND T1.SEKOU_L_CD  = L2.LEVY_L_CD ");
        stb.append("                            AND T1.SEKOU_M_CD  = L2.LEVY_M_CD ");
        stb.append("                            AND L2.LEVY_IN_OUT_DIV = '2' ");
        stb.append(" WHERE ");
        stb.append("         T1.SCHOOLCD    = '" + _param._schoolCd + "' ");
        stb.append("     AND T1.SCHOOL_KIND = '" + _param._schoolKind + "' ");
        stb.append("     AND T1.YEAR        = '" + _param._year + "' ");
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
        private final String _sekouLMcd;
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
            _sekouLMcd = request.getParameter("SEKOU_L_M_CD");
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
            stb.append("     AND PROGRAMID   = 'KNJP954' ");
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

