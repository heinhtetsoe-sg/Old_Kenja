// kanji=漢字
/*
 * $Id: a16c77bcbd44468eb95c8f295e09bf77da98d26a $
 *
 * 作成日: 2018/12/19
 * 作成者: matsushima
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;


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

public class KNJL512G {

    private static final Log log = LogFactory.getLog(KNJL512G.class);

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
        svf.VrSetForm("KNJL512G.frm", 1);

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final String sql = sql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            String befExamNo = "";
            while (rs.next()) {
                final String fZipcd = rs.getString("FINSCHOOL_ZIPCD");
                final String fAddr1 = rs.getString("FINSCHOOL_ADDR1");
                final String fAddr2 = rs.getString("FINSCHOOL_ADDR2");
                String finschoolName = StringUtils.defaultString(rs.getString("FINSCHOOL_NAME"));
                final String examno = rs.getString("EXAMNO");
                String name = rs.getString("NAME");
                final String[] birthday = KNJ_EditDate.tate_format4(db2, StringUtils.defaultString(rs.getString("BIRTHDAY")));
                String coursename = StringUtils.defaultString(rs.getString("COURSENAME"));
                String majorname = StringUtils.defaultString(rs.getString("MAJORNAME2"));
                String remark1 = StringUtils.defaultString(rs.getString("REMARK1"));

                if (!"".equals(befExamNo) && !befExamNo.equals(examno)) {
                    svf.VrEndPage();
                }

                svf.VrsOut("FINSCHOOL_ZIP_NO", fZipcd); //郵便番号
                final String fAddr1Field = getMS932Bytecount(fAddr1) > 40 ? "_3" : getMS932Bytecount(fAddr1) > 30 ? "_2" : "_1";
                final String fAddr2Field = getMS932Bytecount(fAddr2) > 40 ? "_3" : getMS932Bytecount(fAddr2) > 30 ? "_2" : "_1";
                svf.VrsOut("FINSCHOOL_ADDR1" + fAddr1Field, fAddr1); //住所1
                svf.VrsOut("FINSCHOOL_ADDR2" + fAddr2Field, fAddr2); //住所2
                svf.VrsOut("FINSCHOOL_DIV_NAME", ""); //設置区分
                if(!"".equals(finschoolName)){
                    finschoolName = finschoolName + "長 殿";
                    final String schoolField = getMS932Bytecount(finschoolName) > 50 ? "5" : getMS932Bytecount(finschoolName ) > 42 ? "4" : getMS932Bytecount(finschoolName ) > 36 ? "3" : getMS932Bytecount(finschoolName ) > 30 ? "2" : "";
                    svf.VrsOut("FINSCHOOL_NAME" + schoolField, finschoolName); //中学校名
                }
                svf.VrsOut("CONTENT", ""); //内容物

                final String[] outDate = KNJ_EditDate.tate_format4(db2, _param._outDate);
                if (outDate.length > 2) {
                        svf.VrsOut("DATE", outDate[0] + outDate[1] + "年" + outDate[2] + "月" + outDate[3] + "日"); //発行日
                }

                svf.VrsOut("SCHOOL_NAME", _param._certifSchool._schoolName); //学校名
                svf.VrsOut("STAFF_NAME1", _param._certifSchool._jobName + " " + _param._certifSchool._principalName); //校長名

                if(birthday.length > 2) {
                    name = name + "（" + birthday[0] + birthday[1] + "年" + birthday[2] + "月" + birthday[3] + "日生）";
                }
                final String nameField = getMS932Bytecount(name) > 70 ? "4" : getMS932Bytecount(name) > 60 ? "3" : getMS932Bytecount(name) > 50 ? "2" : "1";
                svf.VrsOut("NAME" + nameField, name); //氏名
                svf.VrsOut("TRANSFER", "許可"); //転入学
                final String[] pDate = KNJ_EditDate.tate_format4(db2, StringUtils.defaultString(_param._permitDate));
                if (pDate.length > 2) {
                    svf.VrsOut("PERMIT_DATE", pDate[0] + pDate[1] + "年" + pDate[2] + "月" + pDate[3] + "日"); //許可年月日
                }

                if(!"".equals(coursename)) {
                    coursename = coursename + "課程";
                }
                final int spaceIdx = majorname.indexOf("　");
                if (0 <= spaceIdx) {
                    if (spaceIdx < majorname.length()) {
                        majorname = majorname.substring(spaceIdx + 1); // スペースより後がコース名
                    }
                }
                if(!"".equals(remark1)) {
                    remark1 = " " + remark1 + "学年相当";
                }
                svf.VrsOut("COURSE", coursename + " " + majorname + remark1); //課程・学科

                final String[] nendo = KNJ_EditDate.tate_format4(db2, _param._entexamyear + "-04-01");
                svf.VrsOut("SPORTS_YEAR", nendo[0] + nendo[1] + "年度");

                befExamNo = examno;
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
        }
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

    private String sql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     FINSCHOOL.FINSCHOOL_ZIPCD, ");
        stb.append("     FINSCHOOL.FINSCHOOL_ADDR1, ");
        stb.append("     FINSCHOOL.FINSCHOOL_ADDR2, ");
        stb.append("     FINSCHOOL.FINSCHOOL_NAME, ");
        stb.append("     FINSCHOOL.PRINCNAME, ");
        stb.append("     VBASE.EXAMNO, ");
        stb.append("     VBASE.NAME, ");
        stb.append("     VBASE.BIRTHDAY, ");
        stb.append("     L1.COURSENAME, ");
        stb.append("     L1.MAJORNAME2, ");
        stb.append("     D021.REMARK1 ");
        stb.append(" FROM ");
        stb.append("     V_ENTEXAM_APPLICANTBASE_DAT VBASE ");
        stb.append("     LEFT JOIN FINSCHOOL_MST FINSCHOOL ON VBASE.FS_CD = FINSCHOOL.FINSCHOOLCD ");
        stb.append("     LEFT JOIN ENTEXAM_COURSE_MST COURSE ON COURSE.ENTEXAMYEAR = COURSE.ENTEXAMYEAR ");
        stb.append("          AND VBASE.APPLICANTDIV   = COURSE.APPLICANTDIV ");
        stb.append("          AND VBASE.TESTDIV        = COURSE.TESTDIV ");
        stb.append("          AND VBASE.ENT_COURSECD   = COURSE.ENTER_COURSECD ");
        stb.append("          AND VBASE.ENT_MAJORCD    = COURSE.ENTER_MAJORCD ");
        stb.append("          AND VBASE.ENT_COURSECODE = COURSE.ENTER_COURSECODE ");
        stb.append("     LEFT JOIN V_COURSE_MAJOR_MST L1 ON L1.YEAR = COURSE.ENTEXAMYEAR ");
        stb.append("          AND L1.COURSECD = COURSE.ENTER_COURSECD ");
        stb.append("          AND L1.MAJORCD  = COURSE.ENTER_MAJORCD ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT D021 ON D021.ENTEXAMYEAR = VBASE.ENTEXAMYEAR ");
        stb.append("          AND D021.APPLICANTDIV = VBASE.APPLICANTDIV ");
        stb.append("          AND D021.EXAMNO       = VBASE.EXAMNO ");
        stb.append("          AND D021.SEQ          = '021' ");
        stb.append(" WHERE ");
        stb.append("     VBASE.ENTEXAMYEAR      = '" + _param._entexamyear + "' ");
        stb.append("     AND VBASE.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND VBASE.TESTDIV      IN ('31','32') ");
        stb.append("     AND VBASE.EXAMNO     IN (" + _param._findschreg + ") ");
        stb.append(" ORDER BY ");
        stb.append("     VBASE.EXAMNO ");
        return stb.toString();
    }

    private static class CertifSchool {
        final String _schoolName;
        final String _jobName;
        final String _principalName;
        public CertifSchool(
                final String schoolName,
                final String jobName,
                final String principalName
        ) {
            _schoolName = schoolName;
            _jobName = jobName;
            _principalName = principalName;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 66420 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        private final String _entexamyear;
        private final String _applicantDiv;
        private final String _outDate;
        private final String _permitDate;
        private final String[] _schregnos;
        private final String _findschreg;
        final CertifSchool _certifSchool;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("YEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _schregnos = request.getParameterValues("CATEGORY_SELECTED"); //学籍番号
            _findschreg = conv_arystr_to_str(_schregnos, ",", "-", 1);
            _outDate = StringUtils.defaultString(request.getParameter("OUTDATE")).replace('/', '-'); //発行日
            _permitDate = StringUtils.defaultString(request.getParameter("PERMIT_DATE")).replace('/', '-'); //許可年月日
            _certifSchool = getCertifSchool(db2);
        }

        private String conv_arystr_to_str(final String[] strary, final String sep, final String delim, final int cutno) {
        	String convgr_hr = "";
        	String sepwk = "";
        	for (int ii = 0;ii < strary.length;ii++) {
        		String cutwkstr[];
        		int idx = 0;
        		if (!"".equals(delim) && cutno > 0) {
        			cutwkstr = StringUtils.split(strary[ii], delim);
        			idx = cutno - 1;
        		} else {
        			cutwkstr = new String[1];
        			cutwkstr[0] = strary[ii];
        		}
        		convgr_hr += sepwk + "'" + cutwkstr[idx] + "'";
        		sepwk = sep;
        	}
        	return convgr_hr;
        }

        private static String getNameMst(final DB2UDB db2, final String field, final String namecd1, final String namecd2) {
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT " + field + " FROM NAME_MST WHERE NAMECD1 = '" + namecd1 + "' AND NAMECD2 = '" + namecd2 + "' ");
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString(field);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return rtn;
        }

        private CertifSchool getCertifSchool(final DB2UDB db2) {
            CertifSchool certifSchool = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement("SELECT * FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _entexamyear + "' AND CERTIF_KINDCD = '106' ");
                rs = ps.executeQuery();
                if (rs.next()) {
                    final String schoolName = rs.getString("SCHOOL_NAME");
                    final String jobName = StringUtils.defaultString(rs.getString("JOB_NAME"));
                    final String principalName = StringUtils.defaultString(rs.getString("PRINCIPAL_NAME"));
                    certifSchool = new CertifSchool(schoolName, jobName, principalName);
                }
            } catch (SQLException ex) {
                log.debug("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return certifSchool;
        }

    }
}

// eof

