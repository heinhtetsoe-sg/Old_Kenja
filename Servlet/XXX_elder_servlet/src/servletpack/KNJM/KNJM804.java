/*
 * $Id: 95de4ccc685453b1c8c8347526f6979e010d00bc $
 *
 * 作成日: 2012/10/18
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJM;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

/**
 * 受講料の各種通知書
 */
public class KNJM804 {

    private static final Log log = LogFactory.getLog(KNJM804.class);

    private static final String OUT_FUCHOUSHU = "1"; // 受講料の不徴収について
    private static final String OUT_GENMEN = "2"; // 授業料減免承認決定通知書
    private static final String OUT_HENKOU = "3";  // 徴収期間変更承認通知書

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

            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            if (null != db2) {
                db2.commit();
                db2.close();
            }
            svf.VrQuit();
        }
    }

    private static int getMS932ByteLength(final String s) {
        if (null != s) {
            try {
                return s.getBytes("MS932").length;
            } catch (final Exception e) {
                log.error("exception!", e);
            }
        }
        return 0;
    }

    private String hankakuToZenkaku(final String s) {
        if (null == s) {
            return s;
        }
        final StringBuffer sb = new StringBuffer();
        for (int i = 0; i < s.length(); i++) {
            final char ch;
            if ('0' <= s.charAt(i) && s.charAt(i) <= '9') {
                ch = (char) (s.charAt(i) - '0' + '０');
            } else {
                ch = s.charAt(i);
            }
            sb.append(ch);
        }
        return sb.toString();
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {

        for (int i = 0; i < _param._schregnos.length; i++) {
            if (OUT_FUCHOUSHU.equals(_param._outputNo)) {
                printMain6(db2, svf, _param._schregnos[i]);
            } else if (OUT_GENMEN.equals(_param._outputNo)) {
                printMain4(db2, svf, _param._schregnos[i]);
            } else if (OUT_HENKOU.equals(_param._outputNo)) {
                printMain5(db2, svf, _param._schregnos[i]);
            }
        }
    }

    // 受講料の不徴収について
    private void printMain6(final DB2UDB db2, final Vrw32alp svf, final String schregno) {

        svf.VrSetForm("KNJM804_3.frm", 1);

        svf.VrsOut("DATE", StringUtils.defaultString(KNJ_EditDate.h_format_JP(_param._date)));
        svf.VrsOut("NENDO", KenjaProperties.gengou(Integer.parseInt(_param._year)) + "度");
        svf.VrsOut("PRINCIPAL", _param._principalname);

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getSql(schregno);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                svf.VrsOut("NO", rs.getString("SCHREGNO"));
                svf.VrsOut("APPLI_NAME", rs.getString("NAME"));
                if (getMS932ByteLength(rs.getString("NAME")) > 20) {
                    svf.VrsOut("NAME2", rs.getString("NAME"));
                } else {
                    svf.VrsOut("NAME", rs.getString("NAME"));
                }
            }
        } catch (final Exception ex) {
            log.error("set_detail1 read error!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }

        try {
            final String sql = getMoneyDueSql(schregno, "01");
            log.fatal(" monye_due sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                svf.VrsOut("APPLI_DATE", KNJ_EditDate.h_format_JP(rs.getString("SINSEI_DATE")));
                final String changeDateF = rs.getString("CHANGE_DATE_F");
                if (null != changeDateF) {
                    svf.VrsOut("APPLI_FPERIOD", KNJ_EditDate.h_format_JP_M(changeDateF));
                }
                final String changeDateT = rs.getString("CHANGE_DATE_T");
                if (null != changeDateT) {
                    svf.VrsOut("APPLI_TPERIOD", KNJ_EditDate.h_format_JP_M(changeDateT));
                }
                final String[] token = KNJ_EditEdit.get_token(rs.getString("CHANGE_REMARK"), 20, 2);
                if (null != token) {
                    for (int i = 0; i < token.length; i++) {
                        svf.VrsOut("APPLI_REASON" + (i + 1), token[i]);
                    }
                }
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        svf.VrEndPage();
        _hasData = true;
    }

    // 授業料減免承認決定通知書
    private void printMain4(final DB2UDB db2, final Vrw32alp svf, final String schregno) {

        svf.VrSetForm("KNJM804_1.frm", 1);

        svf.VrsOut("DATE", StringUtils.defaultString(KNJ_EditDate.h_format_JP(_param._date)));
        svf.VrsOut("PRINCIPAL", _param._principalname);

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getSql(schregno);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                svf.VrsOut("NO", rs.getString("SCHREGNO"));
                svf.VrsOut("APPLI_NAME", rs.getString("NAME"));
            }
        } catch (final Exception ex) {
            log.error("set_detail1 read error!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }

        try {
            final String sql = getMoneyDueSql(schregno, "02");
            log.fatal(" monye_due sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                svf.VrsOut("EXPRICE", rs.getString("GENMEN_MONEY"));
                svf.VrsOut("SUBJECT", rs.getString("GENMEN_CNT"));
                final String changeDateF = rs.getString("CHANGE_DATE_F");
                if (null != changeDateF) {
                    svf.VrsOut("EX_FYEAR", changeDateF.substring(0, 4));
                    svf.VrsOut("EX_FMONTH", String.valueOf(Integer.parseInt(changeDateF.substring(5, 7))));
                }
                final String changeDateT = rs.getString("CHANGE_DATE_T");
                if (null != changeDateT) {
                    svf.VrsOut("EX_TYEAR", changeDateT.substring(0, 4));
                    svf.VrsOut("EX_TMONTH", String.valueOf(Integer.parseInt(changeDateT.substring(5, 7))));
                }
                svf.VrsOut("REMARK", rs.getString("CHANGE_REMARK"));
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        List flist = new ArrayList();
        flist.add("ERA_NAME");
        flist.add("ERA_NAME2");
        flist.add("ERA_NAME3");
        flist.add("ERA_NAME4");
        flist.add("ERA_NAME5");
        flist.add("ERA_NAME6");
        putGengou2(db2, svf, flist);
        svf.VrEndPage();
        _hasData = true;
    }

    // 徴収期間変更承認通知書
    private void printMain5(final DB2UDB db2, final Vrw32alp svf, final String schregno) {

        svf.VrSetForm("KNJM804_2.frm", 1);

        svf.VrsOut("DATE", StringUtils.defaultString(KNJ_EditDate.h_format_JP(_param._date)));
        svf.VrsOut("PRINCIPAL", _param._principalname);

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getSql(schregno);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                svf.VrsOut("NO", rs.getString("SCHREGNO"));
                svf.VrsOut("APPLI_NAME", rs.getString("NAME"));
            }
        } catch (final Exception ex) {
            log.error("set_detail1 read error!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }

        try {
            final String sql = getMoneyDueSql(schregno, "03");
            log.fatal(" monye_due sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String changeDateF = rs.getString("CHANGE_DATE_F");
                if (null != changeDateF) {
                    svf.VrsOut("COL_YEAR", changeDateF.substring(0, 4));
                    svf.VrsOut("COL_MONTH", String.valueOf(Integer.parseInt(changeDateF.substring(5, 7))));
                    svf.VrsOut("COL_DAY", String.valueOf(Integer.parseInt(changeDateF.substring(8))));
                }
                svf.VrsOut("REMARK", rs.getString("CHANGE_REMARK"));
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        List flist = new ArrayList();
        flist.add("ERA_NAME");
        flist.add("ERA_NAME2");
        flist.add("ERA_NAME3");
        flist.add("ERA_NAME4");
        flist.add("ERA_NAME5");
        flist.add("ERA_NAME6");
        putGengou2(db2, svf, flist);
        svf.VrEndPage();
        _hasData = true;
    }

    private String getSql(final String schregno) {
        final StringBuffer stb = new StringBuffer();
        if ("00000".equals(_param._gradeHrclass)) { // 新入生
            stb.append("SELECT ");
            stb.append("  t0.SCHREGNO, ");
            stb.append("  t0.NAME ");
            stb.append(" FROM ");
            stb.append("   FRESHMAN_DAT t0 ");
            stb.append(" WHERE ");
            stb.append("  t0.ENTERYEAR = '" + _param._year + "' ");
            stb.append("  AND t0.SCHREGNO = '" + schregno + "' ");
        } else {
            stb.append("SELECT ");
            stb.append("  t0.SCHREGNO, ");
            stb.append("  t0.NAME ");
            stb.append(" FROM ");
            stb.append("   SCHREG_BASE_MST t0 ");
            stb.append(" WHERE ");
            stb.append("  t0.SCHREGNO = '" + schregno + "' ");
        }
        return stb.toString();
    }

    public String getMoneyDueSql(final String schregno, final String changeCd) {

        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     * ");
        stb.append(" FROM ");
        stb.append("     COLLECT_MONEY_DUE_M_DAT T1  ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SCHREGNO = '" + schregno + "' ");
        stb.append("     AND T1.COLLECT_GRP_CD = '" + _param._collectGrpCd + "' ");
        stb.append("     AND T1.CHANGE_CD = '" + changeCd + "' ");
        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 63784 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _year; // ログイン年度 or ログイン年度 + 1
//        private final String _ctrlyear;
//        private final String _semester;
        private final String _collectGrpCd;
        private final String _gradeHrclass;
        private final String _outputNo;
        private final String[] _schregnos;
        private final String _date;
        private String _principalname;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
//            _ctrlyear = request.getParameter("CTRL_YEAR");
//            _semester = request.getParameter("SEMESTER");
            _collectGrpCd = request.getParameter("COLLECT_GRP_CD");
            _gradeHrclass = request.getParameter("GRADE_HR_CLASS");
            _outputNo = request.getParameter("OUTPUT_NO");
            _date = request.getParameter("OUTPUTDATE").replace('/', '-'); // 納入期限
            final String[] src = request.getParameterValues("category_name");
            final String[] dst = new String[src.length];
            for (int i = 0; i < src.length; i++) {
                dst[i] = StringUtils.split(src[i], "-")[0];
            }
            _schregnos = dst;
            loadCertifSchoolDat(db2);
        }

        public void loadCertifSchoolDat(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = "SELECT * FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _year + "' AND CERTIF_KINDCD = '123' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    _principalname = rs.getString("PRINCIPAL_NAME");
                }
            } catch (Exception e) {
                log.error("setHeader name_mst error!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }
    }

    private void putGengou2(final DB2UDB db2, final Vrw32alp svf, final List fieldList) {
        //元号(記入項目用)
        String[] dwk;
        if (_param._date.indexOf('/') >= 0) {
            dwk = StringUtils.split(_param._date, '/');
        } else if (_param._date.indexOf('-') >= 0) {
            dwk = StringUtils.split(_param._date, '-');
        } else {
            //ありえないので、固定値で設定。
            dwk = new String[1];
        }
        if (dwk.length >= 3) {
            final String gengou = KNJ_EditDate.gengou(db2, Integer.parseInt(dwk[0]), Integer.parseInt(dwk[1]), Integer.parseInt(dwk[2]));
            for (final Iterator it = fieldList.iterator(); it.hasNext();) {
                final String setFieldStr = (String) it.next();
                svf.VrsOut(setFieldStr, gengou);
            }
        }
    }
}

// eof

