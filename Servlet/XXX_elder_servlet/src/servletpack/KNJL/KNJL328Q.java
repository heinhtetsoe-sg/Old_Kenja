/*
 * $Id: febcfaf574b8790cc3d64249c4e20d07afb378bd $
 *
 * 作成日: 2017/04/07
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;


import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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

public class KNJL328Q {

    private static final Log log = LogFactory.getLog(KNJL328Q.class);

    private boolean _hasData;
    private String SCHOLAR_TOKUBETU = "1";
    private String SCHOLAR_IPPAN = "2";

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
        svf.VrSetForm("KNJL328Q.frm", 1);

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {

            final String sql = sql("");
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String examno = rs.getString("EXAMNO");
                final String name = rs.getString("NAME");
                final String hope = rs.getString("SCHOLAR_KIBOU");
                final String toukyu = rs.getString("TOUKYU");

                svf.VrsOut("NENDO", KNJ_EditDate.gengou(db2, Integer.parseInt(_param._entexamyear)) + "年度");
                svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(db2, _param._loginDate));
                if (null != _param._schoollogoFilePath) {
                    svf.VrsOut("SCHOOL_LOGO", _param._schoollogoFilePath);
                }
                svf.VrsOut("SCHOOL_NAME", _param._cerifSchoolSchoolName);
                svf.VrsOut("STAFF_NAME", _param._cerifSchoolJobName + _param._cerifSchoolPrincipalName);
                if (null != _param._schoollogoStampFilePath) {
                    svf.VrsOut("SCHOOLSTAMP", _param._schoollogoStampFilePath);
                }

                svf.VrsOut("EXAM_NO", examno);
                svf.VrsOut("NAME", name);
                svf.VrsOut("COURSE_NAME", "普通科");
                svf.VrsOut("HOPE", hope + "奨学生");
                svf.VrsOut("RANK1", toukyu);

                svf.VrEndPage();
                _hasData = true;
            }

            db2.commit();
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
    }

    private String sql(final String selectDiv) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     VBASE.EXAMNO, ");
        stb.append("     VBASE.NAME, ");
        stb.append("     CASE WHEN VBASE.SCHOLAR_KIBOU = '" + SCHOLAR_TOKUBETU + "' THEN '特別' ");
        stb.append("          WHEN VBASE.SCHOLAR_KIBOU = '" + SCHOLAR_IPPAN + "' THEN '一般' ");
        stb.append("          ELSE '無' ");
        stb.append("     END AS SCHOLAR_KIBOU, ");
        stb.append("     L025.NAME1 AS TOUKYU ");
        stb.append(" FROM ");
        stb.append("     V_ENTEXAM_APPLICANTBASE_DAT VBASE ");
        stb.append("     LEFT JOIN NAME_MST L025 ON L025.NAMECD1 = 'L025' ");
        stb.append("          AND VBASE.SCHOLAR_TOUKYU_SENGAN = L025.NAMECD2 ");
        stb.append(" WHERE ");
        stb.append("     VBASE.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND VBASE.APPLICANTDIV = '" + _param._applicantdiv + "' ");
        stb.append("     AND VBASE.TESTDIV0 = '2' ");
        stb.append("     AND VBASE.GENERAL_FLG = '1' ");
        stb.append("     AND VBASE.SCHOLAR_SAIYOU = '1' ");
        stb.append(" ORDER BY ");
        stb.append("     VBASE.EXAMNO ");
        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 70972 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _applicantdiv;
        final String _entexamyear;
        final String _loginYear;
        final String _loginSemester;
        final String _loginDate;
        final String _prgid;
        private String _cerifSchoolSchoolName;
        private String _cerifSchoolJobName;
        private String _cerifSchoolPrincipalName;
        final String _documentroot;
        final String _imagepath;
        final String _schoollogoFilePath;
        final String _schoollogoStampFilePath;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _applicantdiv = request.getParameter("APPLICANTDIV");
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _loginYear = request.getParameter("LOGIN_YEAR");
            _loginSemester = request.getParameter("LOGIN_SEMESTER");
            _loginDate = request.getParameter("LOGIN_DATE");
            _prgid = request.getParameter("PRGID");
            _documentroot = request.getParameter("DOCUMENTROOT");
            _imagepath = request.getParameter("IMAGEPATH");
            _schoollogoFilePath = getImageFilePath("SCHOOLBADGE.jpg");
            _schoollogoStampFilePath = getImageFilePath("SCHOOLSTAMP_H.bmp");
            setCertifSchoolDat(db2);
        }

        private void setCertifSchoolDat(final DB2UDB db2) {
            StringBuffer sql = new StringBuffer();
            sql.append(" SELECT SCHOOL_NAME, JOB_NAME, PRINCIPAL_NAME FROM CERTIF_SCHOOL_DAT ");
            sql.append(" WHERE YEAR = '" + _entexamyear + "' AND CERTIF_KINDCD = '106' ");
            log.debug("certif_school_dat sql = " + sql.toString());
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    _cerifSchoolSchoolName = rs.getString("SCHOOL_NAME");
                    _cerifSchoolJobName = rs.getString("JOB_NAME");
                    _cerifSchoolPrincipalName = rs.getString("PRINCIPAL_NAME");
                }
            } catch (Exception ex) {
                log.error("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            _cerifSchoolSchoolName = StringUtils.defaultString(_cerifSchoolSchoolName);
            _cerifSchoolJobName = StringUtils.defaultString(_cerifSchoolJobName, "校長");
            _cerifSchoolPrincipalName = StringUtils.defaultString(_cerifSchoolPrincipalName);
        }

        /**
         * 写真データファイルの取得
         */
        private String getImageFilePath(final String filename) {
            if (null == _documentroot || null == _imagepath || null == filename) {
                return null;
            } // DOCUMENTROOT
            final StringBuffer path = new StringBuffer();
            path.append(_documentroot).append("/").append(_imagepath).append("/").append(filename);
            final File file = new File(path.toString());
            if (!file.exists()) {
                log.warn("画像ファイル無し:" + path);
                return null;
            } // 写真データ存在チェック用
            return path.toString();
        }

    }
}

// eof

