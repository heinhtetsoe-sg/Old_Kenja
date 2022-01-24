/*
 * $Id: 049c35524822c695a95de1bc1d488dff18c3dacf $
 *
 * 作成日: 2015/08/14
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJF;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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

public class KNJF320 {

    private static final Log log = LogFactory.getLog(KNJF320.class);

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
        svf.VrSetForm("KNJF320.frm", 1);

        PreparedStatement ps = null;
        ResultSet rs = null;
        PreparedStatement psPriInnei = null;
        ResultSet rsPriInnei = null;
        PreparedStatement psInnei = null;
        ResultSet rsInnei = null;

        try {
            final String sql = mainSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                if ("1".equals(_param._ineiPri)) {
                    final String priInneiSql = getInnei(_param._principalCd);
                    psPriInnei = db2.prepareStatement(priInneiSql);
                    rsPriInnei = psPriInnei.executeQuery();
                    rsPriInnei.next();
                    final String stampPriNo = rsPriInnei.getString("STAMP_NO");
                    final String priInnei = _param.getImagePath(stampPriNo);
                    svf.VrsOut("STAFF1BTMC", null != stampPriNo ? priInnei : "");
                }

                if ("1".equals(_param._ineiStf)) {
                    final String staffInneiSql = getInnei(rs.getString("STAFFCD"));
                    psInnei = db2.prepareStatement(staffInneiSql);
                    rsInnei = psInnei.executeQuery();
                    rsInnei.next();
                    final String stampNo = rsInnei.getString("STAMP_NO");
                    final String staffInnei = _param.getImagePath(stampNo);
                    svf.VrsOut("STAFF2BTMC", null != stampNo ? staffInnei : "");
                }

                svf.VrsOut("DATE", _param._ctrlDate.substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(_param._ctrlDate));
                svf.VrsOut("NAME", null != rs.getString("STAFFNAME") ? rs.getString("STAFFNAME") : "");
                svf.VrsOut("EXE_DATE", null != rs.getString("WORK_DATE") ? rs.getString("WORK_DATE").replace('-', '/') : "");

                svf.VrsOut("EXE_PERIOD1", "1".equals(rs.getString("REMARK901_1")) ? "○" : "");
                svf.VrsOut("EXE_PERIOD2", "1".equals(rs.getString("REMARK901_2")) ? "○" : "");
                svf.VrsOut("EXE_PERIOD3", "1".equals(rs.getString("REMARK901_3")) ? "○" : "");

                svf.VrsOut("EXE_PLACE1", "1".equals(rs.getString("REMARK0_1")) ? "○" : "");
                svf.VrsOut("EXE_PLACE2", "1".equals(rs.getString("REMARK0_2")) ? "○" : "");
                svf.VrsOut("EXE_PLACE3", "1".equals(rs.getString("REMARK0_3")) ? "○" : "");
                svf.VrsOut("EXE_PLACE4", "1".equals(rs.getString("REMARK0_4")) ? "○" : "");
                svf.VrsOut("EXE_PLACE5", "1".equals(rs.getString("REMARK0_5")) ? "○" : "");
                svf.VrsOut("EXE_ETC", null != rs.getString("REMARK0_6") ? rs.getString("REMARK0_6") : "");

                svf.VrsOut("ITEM1", "1".equals(rs.getString("REMARK1_1")) ? "○" : "");
                svf.VrsOut("ITEM1_1", "1".equals(rs.getString("REMARK1_2")) ? "○" : "");
                svf.VrsOut("ITEM1_2", "1".equals(rs.getString("REMARK1_3")) ? "○" : "");
                svf.VrsOut("ITEM_ETC1", null != rs.getString("REMARK1_4") ? rs.getString("REMARK1_4") : "");

                svf.VrsOut("ITEM2", "1".equals(rs.getString("REMARK2_1")) ? "○" : "");

                svf.VrsOut("ITEM3", "1".equals(rs.getString("REMARK3_1")) ? "○" : "");

                svf.VrsOut("ITEM4", "1".equals(rs.getString("REMARK4_1")) ? "○" : "");
                svf.VrsOut("ITEM4_1", "1".equals(rs.getString("REMARK4_2")) ? "○" : "");
                svf.VrsOut("ITEM4_2", "1".equals(rs.getString("REMARK4_3")) ? "○" : "");
                svf.VrsOut("ITEM4_3", "1".equals(rs.getString("REMARK4_4")) ? "○" : "");
                svf.VrsOut("ITEM4_4", "1".equals(rs.getString("REMARK4_5")) ? "○" : "");
                svf.VrsOut("ITEM_ETC4", null != rs.getString("REMARK4_6") ? rs.getString("REMARK4_6") : "");

                svf.VrsOut("ITEM5", "1".equals(rs.getString("REMARK5_1")) ? "○" : "");

                svf.VrsOut("ITEM6", "1".equals(rs.getString("REMARK6_1")) ? "○" : "");
                svf.VrsOut("ITEM6_1", "1".equals(rs.getString("REMARK6_2")) ? "○" : "");
                svf.VrsOut("ITEM6_2", "1".equals(rs.getString("REMARK6_3")) ? "○" : "");
                svf.VrsOut("ITEM6_3", "1".equals(rs.getString("REMARK6_4")) ? "○" : "");
                svf.VrsOut("ITEM_ETC6", null != rs.getString("REMARK6_5") ? rs.getString("REMARK6_5") : "");

                if (null != rs.getString("NEWS_STORY")) {
                    final String[] communication1 = get_token(rs.getString("NEWS_STORY"), 40, 12);
                    for (int i = 0; i < communication1.length; i++) {
                        svf.VrsOutn("REPORT", (i + 1), communication1[i]);
                    }
                }

                if (null != rs.getString("SPECIAL_REPORT")) {
                    final String[] communication1 = get_token(rs.getString("SPECIAL_REPORT"), 40, 12);
                    for (int i = 0; i < communication1.length; i++) {
                        svf.VrsOutn("SPECIAL", (i + 1), communication1[i]);
                    }
                }

                _hasData = true;
            }

            if (_hasData) {
                svf.VrEndPage();
            }
            db2.commit();
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            DbUtils.closeQuietly(null, psPriInnei, rsPriInnei);
            DbUtils.closeQuietly(null, psInnei, rsInnei);
        }
    }

    private String mainSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.WORK_DATE, ");
        stb.append("     L901.REMARK1 AS REMARK901_1, ");
        stb.append("     L901.REMARK2 AS REMARK901_2, ");
        stb.append("     L901.REMARK3 AS REMARK901_3, ");
        stb.append("     T1.STAFFCD, ");
        stb.append("     STAFF.STAFFNAME, ");
        stb.append("     L0.REMARK1 AS REMARK0_1, ");
        stb.append("     L0.REMARK2 AS REMARK0_2, ");
        stb.append("     L0.REMARK3 AS REMARK0_3, ");
        stb.append("     L0.REMARK4 AS REMARK0_4, ");
        stb.append("     L0.REMARK5 AS REMARK0_5, ");
        stb.append("     L0.REMARK6 AS REMARK0_6, ");
        stb.append("     L0.REMARK7 AS REMARK0_7, ");
        stb.append("     L1.REMARK1 AS REMARK1_1, ");
        stb.append("     L1.REMARK2 AS REMARK1_2, ");
        stb.append("     L1.REMARK3 AS REMARK1_3, ");
        stb.append("     L1.REMARK4 AS REMARK1_4, ");
        stb.append("     L1.REMARK5 AS REMARK1_5, ");
        stb.append("     L1.REMARK6 AS REMARK1_6, ");
        stb.append("     L2.REMARK1 AS REMARK2_1, ");
        stb.append("     L2.REMARK2 AS REMARK2_2, ");
        stb.append("     L2.REMARK3 AS REMARK2_3, ");
        stb.append("     L2.REMARK4 AS REMARK2_4, ");
        stb.append("     L2.REMARK5 AS REMARK2_5, ");
        stb.append("     L2.REMARK6 AS REMARK2_6, ");
        stb.append("     L3.REMARK1 AS REMARK3_1, ");
        stb.append("     L3.REMARK2 AS REMARK3_2, ");
        stb.append("     L3.REMARK3 AS REMARK3_3, ");
        stb.append("     L3.REMARK4 AS REMARK3_4, ");
        stb.append("     L3.REMARK5 AS REMARK3_5, ");
        stb.append("     L3.REMARK6 AS REMARK3_6, ");
        stb.append("     L4.REMARK1 AS REMARK4_1, ");
        stb.append("     L4.REMARK2 AS REMARK4_2, ");
        stb.append("     L4.REMARK3 AS REMARK4_3, ");
        stb.append("     L4.REMARK4 AS REMARK4_4, ");
        stb.append("     L4.REMARK5 AS REMARK4_5, ");
        stb.append("     L4.REMARK6 AS REMARK4_6, ");
        stb.append("     L5.REMARK1 AS REMARK5_1, ");
        stb.append("     L5.REMARK2 AS REMARK5_2, ");
        stb.append("     L5.REMARK3 AS REMARK5_3, ");
        stb.append("     L5.REMARK4 AS REMARK5_4, ");
        stb.append("     L5.REMARK5 AS REMARK5_5, ");
        stb.append("     L5.REMARK6 AS REMARK5_6, ");
        stb.append("     L6.REMARK1 AS REMARK6_1, ");
        stb.append("     L6.REMARK2 AS REMARK6_2, ");
        stb.append("     L6.REMARK3 AS REMARK6_3, ");
        stb.append("     L6.REMARK4 AS REMARK6_4, ");
        stb.append("     L6.REMARK5 AS REMARK6_5, ");
        stb.append("     L6.REMARK6 AS REMARK6_6, ");
        stb.append("     T1.NEWS_STORY, ");
        stb.append("     T1.SPECIAL_REPORT ");
        stb.append(" FROM ");
        stb.append("     MEDICAL_WORK_RECORD_DAT T1 ");
        stb.append("     LEFT JOIN MEDICAL_WORK_RECORD_DETAIL_DAT L0 ON T1.WORK_DATE = L0.WORK_DATE ");
        stb.append("          AND T1.STAFFCD = L0.STAFFCD ");
        stb.append("          AND T1.WORK_DIV = L0.WORK_DIV ");
        stb.append("          AND L0.SEQ = '000' ");
        stb.append("     LEFT JOIN MEDICAL_WORK_RECORD_DETAIL_DAT L1 ON T1.WORK_DATE = L1.WORK_DATE ");
        stb.append("          AND T1.STAFFCD = L1.STAFFCD ");
        stb.append("          AND T1.WORK_DIV = L1.WORK_DIV ");
        stb.append("          AND L1.SEQ = '001' ");
        stb.append("     LEFT JOIN MEDICAL_WORK_RECORD_DETAIL_DAT L2 ON T1.WORK_DATE = L2.WORK_DATE ");
        stb.append("          AND T1.STAFFCD = L2.STAFFCD ");
        stb.append("          AND T1.WORK_DIV = L2.WORK_DIV ");
        stb.append("          AND L2.SEQ = '002' ");
        stb.append("     LEFT JOIN MEDICAL_WORK_RECORD_DETAIL_DAT L3 ON T1.WORK_DATE = L3.WORK_DATE ");
        stb.append("          AND T1.STAFFCD = L3.STAFFCD ");
        stb.append("          AND T1.WORK_DIV = L3.WORK_DIV ");
        stb.append("          AND L3.SEQ = '003' ");
        stb.append("     LEFT JOIN MEDICAL_WORK_RECORD_DETAIL_DAT L4 ON T1.WORK_DATE = L4.WORK_DATE ");
        stb.append("          AND T1.STAFFCD = L4.STAFFCD ");
        stb.append("          AND T1.WORK_DIV = L4.WORK_DIV ");
        stb.append("          AND L4.SEQ = '004' ");
        stb.append("     LEFT JOIN MEDICAL_WORK_RECORD_DETAIL_DAT L5 ON T1.WORK_DATE = L5.WORK_DATE ");
        stb.append("          AND T1.STAFFCD = L5.STAFFCD ");
        stb.append("          AND T1.WORK_DIV = L5.WORK_DIV ");
        stb.append("          AND L5.SEQ = '005' ");
        stb.append("     LEFT JOIN MEDICAL_WORK_RECORD_DETAIL_DAT L6 ON T1.WORK_DATE = L6.WORK_DATE ");
        stb.append("          AND T1.STAFFCD = L6.STAFFCD ");
        stb.append("          AND T1.WORK_DIV = L6.WORK_DIV ");
        stb.append("          AND L6.SEQ = '006' ");
        stb.append("     LEFT JOIN MEDICAL_WORK_RECORD_DETAIL_DAT L901 ON T1.WORK_DATE = L901.WORK_DATE ");
        stb.append("          AND T1.STAFFCD = L901.STAFFCD ");
        stb.append("          AND T1.WORK_DIV = L901.WORK_DIV ");
        stb.append("          AND L901.SEQ = '901' ");
        stb.append("     LEFT JOIN STAFF_MST STAFF ON T1.STAFFCD = STAFF.STAFFCD ");
        stb.append(" WHERE ");
        stb.append("     T1.WORK_DATE = '" + _param._workDate.replace('/', '-') + "' ");
        stb.append("     AND T1.STAFFCD = '" + _param._staffCd + "' ");
        stb.append("     AND T1.WORK_DIV = '1' ");

        return stb.toString();
    }

    private String getInnei(final String staffCd) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     MAX(STAMP_NO) AS STAMP_NO ");
        stb.append(" FROM ");
        stb.append("     ATTEST_INKAN_DAT ");
        stb.append(" WHERE ");
        stb.append("     STAFFCD = '" + staffCd + "' ");
        return stb.toString();
    }

    private String[] get_token(String strx,int f_len,int f_cnt) {
        final String[] token = KNJ_EditEdit.get_token(strx, f_len, f_cnt);
        if (null == token) {
            return new String[]{};
        }
        return token;
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
        private final String _workDate;
        private final String _year;
        private final String _ctrlYear;
        private final String _ctrlDate;
        private final String _ctrlSemester;
        private final String _printLogStaffCd;
        private final String _staffCd;
        private final String _printLogAddr;
        private final String _documentRoot;
        final String _imagePath;
        private final String _principalCd;
        private final String _ineiPri;
        private final String _ineiStf;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _workDate = request.getParameter("WORK_DATE");
            _year = request.getParameter("YEAR");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _staffCd = request.getParameter("STAFFCD");
            _printLogStaffCd = request.getParameter("PRINT_LOG_STAFFCD");
            _printLogAddr = request.getParameter("PRINT_LOG_REMOTE_ADDR");
            _documentRoot = request.getParameter("DOCUMENTROOT");
            _imagePath = request.getParameter("IMAGEPATH");
            _ineiPri = request.getParameter("INEI_PRI");
            _ineiStf = request.getParameter("INEI_STF");

            _principalCd = getPrincipalCd(db2);
        }

        private String getPrincipalCd(final DB2UDB db2) throws SQLException {
            String retStr = "";
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = getPrincipalSql();
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    retStr = rs.getString("STAFFCD");
                }

            } catch (SQLException ex) {
                log.debug("Exception:", ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retStr;
        }

        private String getPrincipalSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH MAX_DATE AS ( ");
            stb.append(" SELECT ");
            stb.append("     SCHOOL_KIND, ");
            stb.append("     MAX(FROM_DATE) AS FROM_DATE ");
            stb.append(" FROM ");
            stb.append("     STAFF_PRINCIPAL_HIST_DAT ");
            stb.append(" WHERE ");
            stb.append("     SCHOOL_KIND = (SELECT MIN(NAME1) FROM NAME_MST WHERE NAMECD1 = 'A023') ");
            stb.append("     AND FROM_DATE <= '" + _workDate.replace('/',  '-') + "' ");
            stb.append(" GROUP BY ");
            stb.append("     SCHOOL_KIND ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     T1.STAFFCD ");
            stb.append(" FROM ");
            stb.append("     STAFF_PRINCIPAL_HIST_DAT T1, ");
            stb.append("     MAX_DATE T2 ");
            stb.append(" WHERE ");
            stb.append("     T1.SCHOOL_KIND = T2.SCHOOL_KIND ");
            stb.append("     AND T1.FROM_DATE = T2.FROM_DATE ");
            return stb.toString();
        }

        public String getImagePath(final String stampNo) {
            final String path = _documentRoot + "/image/stamp/" + stampNo + ".bmp";
            if (new java.io.File(path).exists()) {
                return path;
            }
            log.warn(" path not exists: " + path);
            return null;
        }

    }
}

// eof

