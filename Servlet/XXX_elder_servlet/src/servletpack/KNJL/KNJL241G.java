/*
 * $Id: fa94c82788c9c34a4b58e71d44058e1970e06a94 $
 *
 * 作成日: 2019/02/28
 * 作成者: yogi
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJL241G {

    private static final Log log = LogFactory.getLog(KNJL241G.class);

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

        PreparedStatement ps = null;
        ResultSet rs = null;

        final List examineeList = new ArrayList();
        try {
            final String sql = sql();
            log.info(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final Map data = new HashMap();
                final String cutwk = (String)rs.getString("TESTDIV");
                data.put("USEFLG", cutwk.length() >= 1 ? cutwk.substring(0, 1) : "");
                data.put("NAME", rs.getString("NAME"));
                data.put("FS_CD", rs.getString("FS_CD"));
                data.put("BIRTHDAY", rs.getString("BIRTHDAY"));
                examineeList.add(data);
                svf.VrEndPage();
                _hasData = true;
            }

            db2.commit();
        } catch (SQLException ex) {
            log.error("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
        for (final Iterator it = examineeList.iterator(); it.hasNext();) {

            final Map data = (Map) it.next();
            final Map finschoolMap = _param.getFinSchoolMst(db2, (String) data.get("FS_CD"));

            if ("3".equals((String)data.get("USEFLG"))) {
                svf.VrSetForm("KNJL241G_2.frm", 1);
            } else {
                svf.VrSetForm("KNJL241G_1.frm", 1);
            }

            setKotei(db2, svf, finschoolMap);
            //
            final String bdwareki;
            if (data.get("BIRTHDAY") != null) {
            	bdwareki = KNJ_EditDate.h_format_JP(db2, (String)data.get("BIRTHDAY")) + "生";
            } else {
            	bdwareki = "";
            }
            final int nlen = KNJ_EditEdit.getMS932ByteLength((String) data.get("NAME"));
            final String nfield = nlen > 70 ? "4" : (nlen > 60 ? "3" : (nlen > 50 ? "2" : "1" ));
            svf.VrsOut("NAME" + nfield, (String) data.get("NAME") + "(" + bdwareki + ")");
            svf.VrEndPage();
            _hasData = true;
        }
    }

    private String sql() {
        final StringBuffer stb = new StringBuffer();
//        stb.append(" WITH RECEPT_MAX AS ( ");
//        stb.append(" SELECT ");
//        stb.append("     RECEPT.EXAMNO, ");
//        stb.append("     MAX(RECEPT.RECEPTNO) AS RECEPTNO ");
//        stb.append(" FROM ");
//        stb.append("     V_ENTEXAM_RECEPT_DAT RECEPT ");
//        stb.append(" WHERE ");
//        stb.append("     RECEPT.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
//        stb.append("     AND RECEPT.APPLICANTDIV = '" + _param._applicantdiv + "' ");
//        stb.append("     AND RECEPT.JUDGEDIV = '1' ");
//        stb.append(" GROUP BY ");
//        stb.append("     RECEPT.EXAMNO ");
//        stb.append(" ) ");
        stb.append(" SELECT ");
//        stb.append("     RECEPT_MAX.EXAMNO, ");
        stb.append("     BASE.TESTDIV, ");
        stb.append("     BASE.EXAMNO, ");
        stb.append("     BASE.NAME, ");
        stb.append("     BASE.FS_CD, ");
        stb.append("     BASE.BIRTHDAY ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_APPLICANTBASE_DAT BASE ");
        stb.append(" WHERE ");
        stb.append("     BASE.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND BASE.EXAMNO IN (" + _param._printExamnoInState + ") ");
        stb.append("     AND BASE.APPLICANTDIV = '" + _param._applicantdiv + "' ");
        stb.append("     AND BASE.JUDGEMENT = '1' ");
        stb.append("     AND BASE.PROCEDUREDIV = '1' ");
        stb.append(" ORDER BY ");
        stb.append("     TRANSLATE_H_ZK(BASE.NAME_KANA) ");
        return stb.toString();
    }

    private void setKotei(final DB2UDB db2, final Vrw32alp svf, final Map finschoolMap) {
        svf.VrsOut("FINSCHOOL_ZIP_NO", "〒" + StringUtils.defaultString((String) finschoolMap.get("ZIPCD")));
        svf.VrsOut("FINSCHOOL_ADDR1_" + (String) finschoolMap.get("ADDR_FIELD"), (String) finschoolMap.get("ADDR1"));
        svf.VrsOut("FINSCHOOL_ADDR2_" + (String) finschoolMap.get("ADDR_FIELD"), (String) finschoolMap.get("ADDR2"));
        svf.VrsOut("FINSCHOOL_DIV_NAME", (String) finschoolMap.get("DIST_NAME"));
        final int fnlen = KNJ_EditEdit.getMS932ByteLength((String) finschoolMap.get("NAME"));
        final String setFinNameField = fnlen > 50 ? "5" : fnlen > 42 ? "4" : fnlen > 36 ? "3": fnlen > 30 ? "2": "";
        svf.VrsOut("FINSCHOOL_NAME" +setFinNameField, (String) finschoolMap.get("NAME"));
        svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(db2, _param._printDate));
        svf.VrsOut("SCHOOL_NAME", _param._schoolName);
        svf.VrsOut("STAFF_NAME1", StringUtils.defaultString(_param._jobName) + "　" + StringUtils.defaultString(_param._principalName));
        if (null != _param._schoolStampPath) {
            svf.VrsOut("SCHOOL_STAMP", _param._schoolStampPath);
        }
        svf.VrsOut("ZIP_NO", "〒" + StringUtils.defaultString(_param._certifSchoolDatRemark2));
        svf.VrsOut("ADDR", StringUtils.defaultString(_param._certifSchoolDatRemark4) + StringUtils.defaultString(_param._certifSchoolDatRemark5));
        svf.VrsOut("SCHOOL_NAME2", _param._schoolName);
        svf.VrsOut("TELNO", "Tel　" + StringUtils.defaultString(_param._certifSchoolDatRemark1));
    }

    private static int getMS932Bytecount(String str) {
        int count = 0;
        if (null != str) {
            try {
                count = str.getBytes("MS932").length;
            } catch (Exception e) {
                log.error(e);
            }
        }
        return count;
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 65954 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _entexamyear;
        private final String _applicantdiv;
        private final String _finschoolcd;
        private final String _printDate;
        private final String _hidExamno;
        private final String _printExamno;
        private final String _printExamnoInState;
        private final String _printLogStaffcd;
        private final String _printLogRemoteAddr;

        private String _principalName = "";
        private String _jobName = "";
        private String _schoolName = "";
        private String _schoolNamePath;
        private String _jorh;
        private String _schoolStampPath;
        private String _schoolLogoPath;
        private String _imagePath;
        private String _documentRoot;
        private String _certifSchoolDatRemark1 = "";
        private String _certifSchoolDatRemark2 = "";
        private String _certifSchoolDatRemark4 = "";
        private String _certifSchoolDatRemark5 = "";

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear        = request.getParameter("ENTEXAMYEAR");
            _applicantdiv       = request.getParameter("APPLICANTDIV");
            _finschoolcd        = request.getParameter("FINSCHOOLCD");
            _printDate          = request.getParameter("PRINT_DATE");
            _hidExamno          = request.getParameter("HID_EXAMNO");
            _printExamno        = request.getParameter("PRINT_EXAMNO");
            _printLogStaffcd    = request.getParameter("PRINT_LOG_STAFFCD");
            _printLogRemoteAddr = request.getParameter("PRINT_LOG_REMOTE_ADDR");

            _printExamnoInState = "'" + StringUtils.replace(_printExamno, ",", "','") + "'";

            _documentRoot = request.getParameter("DOCUMENTROOT");
            loadControlMst(db2);
            _jorh = "1".equals(_applicantdiv) ? "J" : "H";
            _schoolStampPath = checkFilePath(_documentRoot + "/" + _imagePath + "/SCHOOLSTAMP_" + _jorh + ".bmp");
            _schoolLogoPath = checkFilePath(_documentRoot + "/" + _imagePath + "/SCHOOLLOGO_" + _jorh + ".jpg");
            _schoolNamePath = checkFilePath(_documentRoot + "/" + _imagePath + "/SCHOOLNAME_" + _jorh + ".jpg");
            setCertifSchoolDat(db2);
        }

        private String checkFilePath(final String path) {
            final boolean exists = new File(path).exists();
            if (!exists) {
                log.info("file not found:" + path);
                return null;
            }
            log.info("exists:" + path);
            return path;
        }

        private void setCertifSchoolDat(final DB2UDB db2) {
            final String certifKindCd;
            if (_applicantdiv.equals("1")) {
                certifKindCd = "105";
            } else {
                certifKindCd = "106";
            }

            final String sql = "SELECT * FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _entexamyear + "' AND CERTIF_KINDCD = '" + certifKindCd + "' ";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                	_principalName = trimLeft(rs.getString("PRINCIPAL_NAME"));
                    _jobName = trimRight(rs.getString("JOB_NAME"));
                    _schoolName = rs.getString("SCHOOL_NAME");
                    _certifSchoolDatRemark1 = null != rs.getString("REMARK1") ? rs.getString("REMARK1") : "";
                    _certifSchoolDatRemark2 = null != rs.getString("REMARK2") ? rs.getString("REMARK2") : "";
                    _certifSchoolDatRemark4 = null != rs.getString("REMARK4") ? rs.getString("REMARK4") : "";
                    _certifSchoolDatRemark5 = null != rs.getString("REMARK5") ? rs.getString("REMARK5") : "";
                }
            } catch (Exception e) {
                log.fatal("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

		private String trimLeft(String s) {
			if (null == s) {
				return s;
			}
			int spaceIdx = -1;
			for (int i = 0; i < s.length(); i++) {
				final char ch = s.charAt(i);
				if (ch != ' ' && ch != '　') {
					break;
				}
				spaceIdx = i;
			}
			if (-1 != spaceIdx) {
				s = s.substring(spaceIdx + 1);
			}
			return s;
		}

		private String trimRight(String s) {
			if (null == s) {
				return s;
			}
			int spaceIdx = -1;
			for (int i = s.length() - 1; i >= 0; i++) {
				final char ch = s.charAt(i);
				if (ch != ' ' && ch != '　') {
					break;
				}
				spaceIdx = i;
			}
			if (-1 != spaceIdx) {
				s = s.substring(0, spaceIdx);
			}
			return s;
		}

        private Map getFinSchoolMst(final DB2UDB db2, final String finschoolcd) {
            final StringBuffer stb = new StringBuffer();
            final Map retMap = new HashMap();

            stb.append(" SELECT ");
            stb.append("     VALUE(NM_L001.NAME1, '') AS NAME1, ");
            stb.append("     VALUE(FIN.FINSCHOOL_NAME, '') AS FINSCHOOL_NAME, ");
            stb.append("     VALUE(FIN.FINSCHOOL_ZIPCD, '') AS FINSCHOOL_ZIPCD, ");
            stb.append("     VALUE(FIN.FINSCHOOL_ADDR1, '') AS FINSCHOOL_ADDR1, ");
            stb.append("     VALUE(FIN.FINSCHOOL_ADDR2, '') AS FINSCHOOL_ADDR2 ");
            stb.append(" FROM ");
            stb.append("     FINSCHOOL_MST FIN ");
            stb.append("     LEFT JOIN NAME_MST NM_L001 ON NM_L001.NAMECD1 = 'L001' ");
            stb.append("          AND FIN.FINSCHOOL_DISTCD = NM_L001.NAMECD2 ");
            stb.append(" WHERE ");
            stb.append("     FIN.FINSCHOOLCD = '" + finschoolcd + "' ");

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    final String setpri = "1".equals(_applicantdiv) ? "学校長　殿" : "学校長　殿";
                    retMap.put("DIST_NAME", rs.getString("NAME1"));
                    retMap.put("NAME", rs.getString("FINSCHOOL_NAME") + setpri);
                    retMap.put("ZIPCD", rs.getString("FINSCHOOL_ZIPCD"));
                    retMap.put("ADDR1", rs.getString("FINSCHOOL_ADDR1"));
                    retMap.put("ADDR2", rs.getString("FINSCHOOL_ADDR2"));
                    int addrField = 1;
                    if (getMS932Bytecount(rs.getString("FINSCHOOL_ADDR1")) > 40 || getMS932Bytecount(rs.getString("FINSCHOOL_ADDR2")) > 40) {
                        addrField = 3;
                    } else if (getMS932Bytecount(rs.getString("FINSCHOOL_ADDR1")) > 30 || getMS932Bytecount(rs.getString("FINSCHOOL_ADDR2")) > 30) {
                        addrField = 2;
                    }
                    retMap.put("ADDR_FIELD", String.valueOf(addrField));
                }
            } catch (Exception e) {
                log.error("getFinSchoolMst Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retMap;
        }

        private void loadControlMst(final DB2UDB db2) {
            final String sql = "SELECT IMAGEPATH, EXTENSION FROM CONTROL_MST WHERE CTRL_NO = '01' ";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    _imagePath = rs.getString("IMAGEPATH");
                }
            } catch (SQLException e) {
                log.error("Exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

    }
}

// eof

