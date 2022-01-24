/**     プロジェクト/プロパティ/javaコード・スタイル/コード・テンプレート/パターン       ***/

/*
 * $Id: 8b44cefcd8dc764ae56ce1188ed367f601057675 $
 *
 * 作成日: 2019/01/15
 * 作成者: yamashiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

public class KNJL330R {

    private static final Log log = LogFactory.getLog(KNJL330R.class);

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
        svf.VrSetForm("KNJL330R.frm", 1);
        final List printList = getList(db2);
        for (Iterator iterator = printList.iterator(); iterator.hasNext();) {
            final Applicant applicant = (Applicant) iterator.next();
            svf.VrsOut("EXAMNO", applicant._receptno);
            svf.VrsOut("NAME", applicant._name);
            svf.VrsOut("BIRTHDAY", KNJ_EditDate.h_format_JP(db2, applicant._birthday));

            svf.VrsOut("DATE", _param._noticedateStr);
            svf.VrsOut("SCHOOLNAME", _param._schoolName); // 学校名
            svf.VrsOut("STAFFNAME", _param._jobName + "　" + _param._principalName); // 職名＋校長名

            svf.VrsOut("NENDO", KNJ_EditDate.gengou(db2, Integer.parseInt(_param._entexamyear)) + "年度");
            if (null != _param._imageFile) {
                svf.VrsOut("STAMP", _param._imageFile.toString());
            }

            svf.VrEndPage();
            _hasData = true;
        }
    }

    private List getList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String receptno = rs.getString("RECEPTNO");
                final String name = rs.getString("NAME");
                final String birthday = rs.getString("BIRTHDAY");
                final Applicant applicant = new Applicant(receptno, name, birthday);
                retList.add(applicant);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String getSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT  ");
        stb.append("     BDETAIL030.REMARK2 AS RECEPTNO, ");
        stb.append("     BASE.NAME, ");
        stb.append("     BASE.BIRTHDAY ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_APPLICANTBASE_DAT BASE ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BDETAIL030 ON BDETAIL030.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
        stb.append("         AND BDETAIL030.APPLICANTDIV = BASE.APPLICANTDIV ");
        stb.append("         AND BDETAIL030.EXAMNO = BASE.EXAMNO ");
        stb.append("         AND BDETAIL030.SEQ = '030' ");
        stb.append("     LEFT JOIN NAME_MST NML013 ON NML013.NAMECD1 = 'L013' ");
        stb.append("         AND NML013.NAMECD2 = BASE.JUDGEMENT ");
        stb.append(" WHERE ");
        stb.append("     BASE.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND BASE.APPLICANTDIV = '" + _param._applicantdiv + "' ");
        stb.append("     AND BASE.ENTDIV = '1' ");
        stb.append("     AND NML013.NAMESPARE1 = '1' ");
        if ("2".equals(_param._output)) {
            stb.append("     AND BDETAIL030.REMARK2 = '" + _param._receptno1 + "' ");
        }
        stb.append(" ORDER BY ");
        stb.append("     BDETAIL030.REMARK2 ");
        return stb.toString();
    }


    private static class Applicant {
        final String _receptno;
        final String _name;
        final String _birthday;

        Applicant(
            final String receptno,
            final String name,
            final String birthday
        ) {
            _receptno = receptno;
            _name = name;
            _birthday = birthday;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 72000 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _entexamyear;
        final String _applicantdiv;
        final String _noticedate;
        final String _form;         // 1:通知 2:特待生決定通知書
        final String _output;      // 出力通知 1:合格＋不合格 2:合格 3:不合格
        final String _receptno1;    // 通知:受験番号指定
        final String _documentroot;

        final String _applicantdivName;
        final String _noticedateStr;
        final File _imageFile;
        final String _schoolName;
        final String _jobName;
        final String _principalName;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _applicantdiv = request.getParameter("APPLICANTDIV");
            _noticedate = request.getParameter("NOTICEDATE").replace('/', '-');
            _noticedateStr = getDateStr(db2, _noticedate);
            _form = request.getParameter("FORM");
            _output = request.getParameter("OUTPUT");
            _receptno1 = request.getParameter("RECEPTNO1");
            _documentroot = request.getParameter("DOCUMENTROOT");

            _applicantdivName = getNameMst(db2, "NAME1", "L003", _applicantdiv);
            _imageFile = getImageFile(db2);
            _schoolName = getCertifSchoolDat(db2, "SCHOOL_NAME", true);
            _jobName = StringUtils.defaultString(getCertifSchoolDat(db2, "JOB_NAME", true));
            _principalName = StringUtils.defaultString(getCertifSchoolDat(db2, "PRINCIPAL_NAME", true));
        }

        private String getDateStr(final DB2UDB db2, final String date) {
            if (null == date) {
                return null;
            }
            return KNJ_EditDate.h_format_JP(db2, date);
        }

        private String getNameMst(final DB2UDB db2, final String field, final String namecd1, final String namecd2) {
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


        private File getImageFile(final DB2UDB db2) {
            if (null == _documentroot) {
                return null;
            }
            String imagepath = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT IMAGEPATH FROM CONTROL_MST WHERE CTRL_NO = '01' ");
                rs = ps.executeQuery();
                while (rs.next()) {
                    imagepath = rs.getString("IMAGEPATH");
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            if (null == imagepath) {
                return null;
            }
            final File file = new File(_documentroot + "/" + imagepath + "/SCHOOLSTAMP.bmp");
            log.fatal(" file = " + file.getAbsolutePath() + ", exists? = " + file.exists());
            if (!file.exists()) {
                return null;
            }
            return file;
        }

        private String getCertifSchoolDat(final DB2UDB db2, final String field, final boolean isTrim) {
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                StringBuffer sql = new StringBuffer();
                sql.append(" SELECT " + field + " ");
                sql.append(" FROM CERTIF_SCHOOL_DAT ");
                sql.append(" WHERE YEAR = '" + _entexamyear + "' ");
                sql.append("   AND CERTIF_KINDCD = '105' ");
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString(field);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            if (isTrim) {
                if (null == rtn) {
                    rtn = "";
                } else {
                    int start = 0;
                    for (int i = 0; i < rtn.length(); i++) {
                        if (rtn.charAt(i) != ' ' && rtn.charAt(i) != '　') {
                            break;
                        }
                        start = i + 1;
                    }
                    rtn = rtn.substring(start);
                }
            }
            return rtn;
        }
    }
}

// eof
