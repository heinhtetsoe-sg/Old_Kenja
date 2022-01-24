/*
 * 作成日: 2021/03/23
 * 作成者: shimoji
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;


import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJL415M {

    private static final Log log = LogFactory.getLog(KNJL415M.class);

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
        svf.VrSetForm("KNJL415M.frm", 1);

        final String waYear = KNJ_EditDate.h_format_JP_N(db2, _param._examyear + "/04/01");
        final String waDate = KNJ_EditDate.h_format_JP(db2, _param._printDate);

        List<Goukakusyo> goukakusyoList = getGoukakusyoList(db2);

        for (Goukakusyo goukakusyo : goukakusyoList) {
            if (_param._cirtifFramePathExsits) {
                svf.VrsOut("FRAME", _param._cirtifFramePath);
            }

            svf.VrsOut("EXAM_NO", goukakusyo._receptNo);

            final int nameByte = KNJ_EditEdit.getMS932ByteLength(goukakusyo._name);
            final String nameFieldStr = nameByte > 30 ? "2" : "1";
            svf.VrsOut("NAME" + nameFieldStr, goukakusyo._name);

            String text = "　上記の者は、　" + waYear + "度　" + _param._schoolName + "　" + StringUtils.defaultString(goukakusyo._courseCodeName) + "学級";
            svf.VrsOut("TEXT", text);

            svf.VrsOut("DATE", waDate);

            svf.VrsOut("SCHOOL_NAME", _param._schoolName);

            svf.VrsOut("JOB_NAME", _param._jobName);

            svf.VrsOut("PRINCIPAL_NAME", _param._principalName);

            if (_param._schoolStampPathExsits) {
                svf.VrsOut("SCHOOL_STAMP", _param._schoolStampPath);
            }

            svf.VrsOut("EXAM_NO2", goukakusyo._receptNo);

            _hasData = true;
            svf.VrEndPage();
        }
    }

    private List<Goukakusyo> getGoukakusyoList(final DB2UDB db2) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        List<Goukakusyo> goukakusyoList = new ArrayList<Goukakusyo>();

        final String goukakusyoSql = getGoukakusyoSql();
        log.debug(" sql =" + goukakusyoSql);

        try {
            ps = db2.prepareStatement(goukakusyoSql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String receptNo = rs.getString("RECEPTNO");
                final String name = rs.getString("NAME");
                final String courseCodeName = rs.getString("COURSECODENAME");

                final Goukakusyo goukakusyo = new Goukakusyo(receptNo, name, courseCodeName);

                goukakusyoList.add(goukakusyo);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }

        return goukakusyoList;
    }

    private String getGoukakusyoSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     RECEPT.RECEPTNO, ");
        stb.append("     BASE.NAME, ");
        stb.append("     COURSE.COURSECODENAME ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_STD_RECEPT_DAT RECEPT ");
        stb.append("     LEFT JOIN ENTEXAM_STD_APPLICANTBASE_DAT BASE ");
        stb.append("            ON BASE.YEAR   = RECEPT.YEAR ");
        stb.append("           AND BASE.EXAMNO = RECEPT.EXAMNO ");
        stb.append("     LEFT JOIN COURSECODE_MST COURSE ");
        stb.append("            ON COURSE.COURSECODE = RECEPT.COURSE_DIV ");
        stb.append(" WHERE ");
        stb.append("     RECEPT.YEAR             = '" + _param._examyear + "' AND ");
        stb.append("     RECEPT.EXAM_SCHOOL_KIND = '" + _param._examSchoolKind + "' AND ");
        stb.append(SQLUtils.whereIn(true, "RECEPT.APPLICANT_DIV || '-' || RECEPT.COURSE_DIV || '-' || RECEPT.FREQUENCY", _param._categorySelect));
        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    private static class Goukakusyo {
        private final String _receptNo;
        private final String _name;
        private final String _courseCodeName;

        Goukakusyo(
            final String receptNo,
            final String name,
            final String courseCodeName
        ) {
            _receptNo = receptNo;
            _name = name;
            _courseCodeName = courseCodeName;
        }
    }

    /** パラメータクラス */
    private static class Param {
        private final String _examyear;
        private final String _examSchoolKind;
        private final String _printDate;
        private final String _documentRoot;
        private final String[] _categorySelect;
        private final String _imagePath;
        private final String _schoolName;
        private final String _jobName;
        private final String _principalName;
        private final String _schoolStampPath;
        private final boolean _schoolStampPathExsits;
        private final String _cirtifFramePath;
        private final boolean _cirtifFramePathExsits;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _examyear = request.getParameter("EXAM_YEAR");
            _examSchoolKind = request.getParameter("EXAM_SCHOOL_KIND");
            _printDate = request.getParameter("PRINT_DATE");
            _documentRoot = request.getParameter("DOCUMENTROOT");
            _categorySelect = request.getParameterValues("CATEGORY_SELECTED");
            _imagePath = getImagePath(db2);
            Map<String, String> certinfInfo = getCertinfInfo(db2);
            _schoolName = certinfInfo.get("SCHOOL_NAME");
            _jobName = certinfInfo.get("JOB_NAME");
            _principalName = certinfInfo.get("PRINCIPAL_NAME");
            _schoolStampPath = _documentRoot + "/" + _imagePath + "/SCHOOLSTAMP_" + _examSchoolKind + ".bmp";
            _schoolStampPathExsits = new File(_schoolStampPath).exists();
            _cirtifFramePath = _documentRoot + "/" + _imagePath + "/CERT_FRAME.jpg";
            _cirtifFramePathExsits = new File(_cirtifFramePath).exists();
        }

        private String getImagePath(final DB2UDB db2) {
            String rtn = "";

            final String sql = "SELECT IMAGEPATH FROM CONTROL_MST WHERE CTRL_NO = '01' ";
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    rtn = rs.getString("IMAGEPATH");
                }
            } catch (SQLException e) {
                log.error("Exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }

            return rtn;
        }

        private Map<String, String> getCertinfInfo(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            Map<String, String> rtn = new HashMap<String, String>();

            final String certifKindcd = "J".equals(_examSchoolKind) ? "105" : "H".equals(_examSchoolKind) ? "104" : "";
            final String sql = " SELECT SCHOOL_NAME, JOB_NAME, PRINCIPAL_NAME FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _examyear + "' AND CERTIF_KINDCD = '" + certifKindcd + "' ";
            log.debug(" sql =" + sql);

            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn.put("SCHOOL_NAME", rs.getString("SCHOOL_NAME"));
                    rtn.put("JOB_NAME", rs.getString("JOB_NAME"));
                    rtn.put("PRINCIPAL_NAME", rs.getString("PRINCIPAL_NAME"));
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }

            return rtn;
        }
    }
}

// eof

