/*
 * $Id: a77b7a9bc6f405bbfbc5c6b82a3cdb08e58090f5 $
 *
 * 作成日: 2017/10/25
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
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJL343Q {

    private static final Log log = LogFactory.getLog(KNJL343Q.class);

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
        String formNam1 = "1".equals(_param._applicantdiv) ? "KNJL343Q_1.frm" : "KNJL343Q_3.frm";
        String formNam2 = "1".equals(_param._applicantdiv) ? "KNJL343Q_2.frm" : "KNJL343Q_4.frm";;

        svf.VrSetForm(formNam1, 1);
        final List printList = getList(db2);
        final int maxLine = 4;
        int lineCnt = 1;
        for (Iterator iterator = printList.iterator(); iterator.hasNext();) {
            if (lineCnt > maxLine) {
                svf.VrEndPage();
                lineCnt = 1;
                svf.VrSetForm(formNam2, 1);
                for (int i = 1; i <= maxLine; i++) {
                    if ("1".equals(_param._applicantdiv)) {
                        svf.VrsOutn("DATE", i, KNJ_EditDate.h_format_JP(_param._testDate));
                    } else {
                        svf.VrsOutn("DATE", i, KNJ_EditDate.h_format_JP_N(_param._testDate) + KNJ_EditDate.h_format_JP_MD(_param._testDate) + _param._holidayDiv);
                    }
                }
                svf.VrEndPage();
                svf.VrSetForm(formNam1, 1);
            }
            final Student student = (Student) iterator.next();
            svf.VrsOutn("EXAM_NO", lineCnt, student._examno);
            if ("1".equals(_param._applicantdiv)) {
                final String nameField = KNJ_EditEdit.getMS932ByteLength(student._name) > 30 ? "3" : KNJ_EditEdit.getMS932ByteLength(student._name) > 26 ? "2" : "1";
                svf.VrsOutn("NAME" + nameField, lineCnt, student._name);
                svf.VrsOutn("SEX", lineCnt, student._sex);
                svf.VrsOutn("DATE", lineCnt, KNJ_EditDate.h_format_JP_MD(_param._testDate));
            } else {
                svf.VrsOutn("NAME1", lineCnt, student._name);
                svf.VrsOutn("DATE", lineCnt, KNJ_EditDate.h_format_JP_MD(_param._testDate) + _param._holidayDiv);
            }
            if (null != _param._schoollogoFilePath) {
                svf.VrsOutn("SCHOOL_LOGO", lineCnt, _param._schoollogoFilePath);//校章
            }
            if (null != _param._schoolStampFilePath) {
                svf.VrsOutn("SCHOOLSTAMP", lineCnt, _param._schoolStampFilePath);//学校印
            }
            lineCnt++;
            _hasData = true;
        }
        if (_hasData) {
            svf.VrEndPage();
            svf.VrSetForm(formNam2, 1);
            for (int i = 1; i < lineCnt; i++) {
                if ("1".equals(_param._applicantdiv)) {
                    svf.VrsOutn("DATE", i, KNJ_EditDate.h_format_JP(_param._testDate));
                } else {
                    svf.VrsOutn("DATE", i, KNJ_EditDate.h_format_JP_N(_param._testDate) + KNJ_EditDate.h_format_JP_MD(_param._testDate) + _param._holidayDiv);
                }
            }
            svf.VrEndPage();
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
                final String examno = rs.getString("EXAMNO");
                final String name = rs.getString("NAME");
                final String sex = rs.getString("SEX");

                final Student student = new Student(examno, name, sex);
                retList.add(student);
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
        stb.append(" SELECT ");
        stb.append("     BASE.EXAMNO, ");
        stb.append("     BASE.NAME, ");
        stb.append("     Z002.NAME2 AS SEX ");
        stb.append(" FROM ");
        stb.append("     V_ENTEXAM_APPLICANTBASE_DAT BASE ");
        stb.append("     LEFT JOIN NAME_MST Z002 ON Z002.NAMECD1 = 'Z002' ");
        stb.append("          AND BASE.SEX = Z002.NAMECD2 ");
        stb.append(" WHERE ");
        stb.append("     BASE.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND BASE.APPLICANTDIV = '" + _param._applicantdiv + "' ");
        stb.append("     AND BASE.TESTDIV = '" + _param._testdiv + "' ");
        stb.append(" ORDER BY ");
        stb.append("     BASE.EXAMNO ");
        return stb.toString();
    }

    private class Student {
        final String _examno;
        final String _name;
        final String _sex;
        public Student(
                final String examno,
                final String name,
                final String sex
        ) {
            _examno = examno;
            _name = name;
            _sex = sex;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 57200 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _applicantdiv;
        final String _testdiv;
        final String _entexamyear;
        final String _loginYear;
        final String _loginSemester;
        final String _loginDate;
        final String _schoolkind;
        final String _documentroot;
        final String _imagepath;
        final String _schoollogoFilePath;
        final String _schoolStampFilePath;
        final String _testDate;
        final String _holidayDiv;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _applicantdiv = request.getParameter("APPLICANTDIV");
            _testdiv = request.getParameter("TESTDIV");
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _loginYear = request.getParameter("LOGIN_YEAR");
            _loginSemester = request.getParameter("LOGIN_SEMESTER");
            _loginDate = request.getParameter("LOGIN_DATE");
            _schoolkind = request.getParameter("SCHOOLKIND");
            _documentroot = request.getParameter("DOCUMENTROOT");
            _imagepath = request.getParameter("IMAGEPATH");
            _schoollogoFilePath  = getImageFilePath("SCHOOLBADGE2.jpg");//校章
            String imageFileName = "1".equals(_applicantdiv) ? "SCHOOLSTAMP_J.bmp" : "SCHOOLSTAMP_H_2.bmp";
            _schoolStampFilePath = getImageFilePath(imageFileName);//学校印
            String nameCd1 = "1".equals(_applicantdiv) ? "L024" : "L004";
            _testDate = StringUtils.defaultString(getNameMst(db2, "NAMESPARE1", nameCd1, _testdiv));
            _holidayDiv = StringUtils.defaultString(getNameMst(db2, "NAMESPARE3", nameCd1, _testdiv));
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
    }
}

// eof
