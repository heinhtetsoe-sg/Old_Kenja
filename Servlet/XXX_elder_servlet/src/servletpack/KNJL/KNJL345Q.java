/*
 * $Id: ccc404ee9c50863bdd44ce91d3b997d7f9cac5f8 $
 *
 * 作成日: 2017/10/26
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

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJL345Q {

    private static final Log log = LogFactory.getLog(KNJL345Q.class);

    private boolean _hasData;

    private final String SUISEN = "1";
    private final String SENBATSU = "2";
    private final String NAIBU = "9";

    private final String SCHOOL_KIND_P = "P";

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
        String formName1 = "";
        final String formName2 = SCHOOL_KIND_P.equals(_param._schoolkind) ? "KNJL345Q_2_2.frm" : "KNJL345Q_2.frm";
        final List printList = getList(db2);
        for (Iterator iterator = printList.iterator(); iterator.hasNext();) {
            final SchoolData schoolData = (SchoolData) iterator.next();
            if (SCHOOL_KIND_P.equals(_param._schoolkind)) {
                if (SUISEN.equals(_param._testDiv)) {
                    formName1 = schoolData._passCnt == 0 ? "KNJL345Q_1_5.frm" : "KNJL345Q_1_6.frm";
                } else {
                    formName1 = schoolData._passCnt == 0 ? "KNJL345Q_1_7.frm" : "KNJL345Q_1_8.frm";
                }
            } else {
                if (SUISEN.equals(_param._testDiv)) {
                    formName1 = schoolData._passCnt == 0 ? "KNJL345Q_1_1.frm" : "KNJL345Q_1_2.frm";
                } else {
                    formName1 = schoolData._passCnt == 0 ? "KNJL345Q_1_3.frm" : "KNJL345Q_1_4.frm";
                }
            }
            svf.VrSetForm(formName1, 1);
            svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(db2, _param._noticeDate));
            svf.VrsOut("FINSCHOOL_CD", SCHOOL_KIND_P.equals(_param._schoolkind) ? "" : schoolData._fsCd);
            final String wkfsName = SCHOOL_KIND_P.equals(_param._schoolkind) ? "園長" : "校長";
            svf.VrsOut("NAME1", schoolData._fsName + "　" + wkfsName + "　殿");
            svf.VrsOut("NOTICE_DATE", KNJ_EditDate.h_format_S(_param._sendDate, "M月d日"));
            svf.VrsOut("EXAM_DATE", KNJ_EditDate.h_format_S(_param._senbatsuTestDate, "M月d日"));
            final String printDate = KNJ_EditDate.h_format_JP_N(db2, _param._entexamyear + "/04/01") + "度";
            svf.VrsOut("RESULT", printDate);
            svf.VrsOut("SCHOOL_NAME3", "駿台甲府中学校");
            svf.VrsOut("TELNO", "TEL　" + KNJ_EditEdit.convertZenkakuSuuji(_param._certifSchool._remark1));
            svf.VrsOut("GREETING", _param._certifSchool._remark10); //季節の挨拶
            schoolInfoPrint(svf);

            svf.VrEndPage();

            svf.VrSetForm(formName2, 4);
            for (Iterator itStudent = schoolData._studentList.iterator(); itStudent.hasNext();) {
                Student student = (Student) itStudent.next();
                svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(db2, _param._noticeDate));
                svf.VrsOut("FINSCHOOL_CD", SCHOOL_KIND_P.equals(_param._schoolkind) ? "" : schoolData._fsCd);
                svf.VrsOut("FINSCHOOL_NAME", schoolData._fsName + "　" + wkfsName + "　殿");
                svf.VrsOut("TITLE", printDate + _param._testdivAbbv1 + "入学試験結果");
                svf.VrsOut("NOTICE_DATE", printDate);
                svf.VrsOut("TEST_DIV_NAME", _param._testdivAbbv1);
                schoolInfoPrint(svf);
                if (null != _param._staffStampFilePath) {
                    svf.VrsOut("STAFFSTAMP", _param._staffStampFilePath);
                }
                svf.VrsOut("EXAM_NO", student._examno);
                final String nameField = KNJ_EditEdit.getMS932ByteLength(student._name) > 40 ? "3" : KNJ_EditEdit.getMS932ByteLength(student._name) > 30 ? "2" : "1";
                svf.VrsOut("NAME" + nameField, student._name);
                svf.VrsOut("JUDGE", student._judge);
                svf.VrEndRecord();
            }
            _hasData = true;
        }
    }

    private void schoolInfoPrint(final Vrw32alp svf) {
        svf.VrsOut("SCHOOL_NAME1", _param._certifSchool._remark6 + _param._certifSchool._remark7);
        svf.VrsOut("SCHOOL_NAME2", _param._certifSchool._schoolName);
        svf.VrsOut("STAFF_NAME", _param._certifSchool._jobName + _param._certifSchool._principalName);
    }

    private List getList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getSelectSchoolSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String fsCd = StringUtils.defaultString(rs.getString("FS_CD"));
                final String fsName = StringUtils.defaultString(rs.getString("FINSCHOOL_NAME"));
                final int passCnt = rs.getInt("PASSCNT");

                final SchoolData schoolData = new SchoolData(fsCd, fsName, passCnt);
                schoolData.setStudent(db2);
                retList.add(schoolData);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String getSelectSchoolSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     VBASE.FS_CD, ");
        stb.append("     FINSCHOOL.FINSCHOOL_NAME, ");
        stb.append("     SUM(CASE WHEN VBASE.JUDGEMENT IN ('1', '3') ");
        stb.append("              THEN 1 ");
        stb.append("              ELSE 0 ");
        stb.append("         END) AS PASSCNT ");
        stb.append(" FROM ");
        stb.append("     V_ENTEXAM_APPLICANTBASE_DAT VBASE ");
        stb.append("     LEFT JOIN FINSCHOOL_MST FINSCHOOL ON VBASE.FS_CD = FINSCHOOL.FINSCHOOLCD ");
        stb.append(" WHERE ");
        stb.append("     VBASE.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND VBASE.APPLICANTDIV = '" + _param._applicantdiv + "' ");
        stb.append("     AND VBASE.TESTDIV = '" + _param._testDiv + "' ");
        stb.append(" GROUP BY ");
        stb.append("     VBASE.FS_CD, ");
        stb.append("     FINSCHOOL.FINSCHOOL_NAME ");
        stb.append(" ORDER BY ");
        stb.append("     VBASE.FS_CD ");
        return stb.toString();
    }

    private class SchoolData {
        final String _fsCd;
        final String _fsName;
        final int _passCnt;
        final List _studentList;
        public SchoolData(
                final String fsCd,
                final String fsName,
                final int passCnt
        ) {
            _fsCd = fsCd;
            _fsName = fsName;
            _passCnt = passCnt;
            _studentList = new ArrayList();
        }
        public void setStudent(final DB2UDB db2) {
            final String studentSql = getStudentSsql();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(studentSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String examno = rs.getString("EXAMNO");
                    final String name = rs.getString("NAME");
                    final String judge = rs.getString("JUDGEMENT");
                    final Student student = new Student(examno, name, judge);
                    _studentList.add(student);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }

        }
        private String getStudentSsql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     VBASE.EXAMNO, ");
            stb.append("     VBASE.NAME, ");
            stb.append("     L013.NAME1 AS JUDGEMENT ");
            stb.append(" FROM ");
            stb.append("     V_ENTEXAM_APPLICANTBASE_DAT VBASE ");
            stb.append("     LEFT JOIN NAME_MST L013 ON L013.NAMECD1 = 'L013' ");
            stb.append("          AND VBASE.JUDGEMENT = L013.NAMECD2 ");
            stb.append(" WHERE ");
            stb.append("     VBASE.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
            stb.append("     AND VBASE.APPLICANTDIV = '" + _param._applicantdiv + "' ");
            stb.append("     AND VBASE.TESTDIV = '" + _param._testDiv + "' ");
            stb.append("     AND VBASE.FS_CD = '" + _fsCd + "' ");
            stb.append(" ORDER BY ");
            stb.append("     VBASE.EXAMNO ");
            return stb.toString();
        }
    }

    private class Student {
        final String _examno;
        final String _name;
        final String _judge;
        public Student(
                final String examno,
                final String name,
                final String judge
        ) {
            _examno = examno;
            _name = name;
            _judge = judge;
        }
    }

    private class CertifSchool {
        final String _schoolName;
        final String _jobName;
        final String _principalName;
        final String _remark1;
        final String _remark6;
        final String _remark7;
        final String _remark10;
        public CertifSchool(
                final String schoolName,
                final String jobName,
                final String principalName,
                final String remark1,
                final String remark6,
                final String remark7,
                final String remark10
        ) {
            _schoolName = schoolName;
            _jobName = jobName;
            _principalName = principalName;
            _remark1 = remark1;
            _remark6 = remark6;
            _remark7 = remark7;
            _remark10 = remark10;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 70266 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _applicantdiv;
        final String _testDiv;
        final String _noticeDate;
//        final String _taisyou;
//        final String _jizen;
//        final String _output;
        final String _entexamyear;
        final String _loginYear;
        final String _loginSemester;
        final String _loginDate;
//        final String _examno;
        final String _schoolkind;
        final CertifSchool _certifSchool;
        final String _passCdIn;
        final String _testDate;
        final String _sendDate;
        final String _senbatsuTestDate;
        final String _testdivAbbv1;
        final String _documentroot;
        final String _imagepath;
        final String _staffStampFilePath;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _applicantdiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _noticeDate = request.getParameter("NOTICEDATE");
//            _taisyou = request.getParameter("TAISYOU");
//            _jizen = request.getParameter("JIZEN");
//            _output = request.getParameter("OUTPUT");
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _loginYear = request.getParameter("LOGIN_YEAR");
            _loginSemester = request.getParameter("LOGIN_SEMESTER");
            _loginDate = request.getParameter("LOGIN_DATE");
//            _examno = request.getParameter("EXAMNO");
            _schoolkind = request.getParameter("SCHOOLKIND");
            _sendDate = request.getParameter("SEND_DATE");
            _certifSchool = getCertifSchool(db2);
            _passCdIn = getPassCd(db2);
            final String namemstcd1 = SCHOOL_KIND_P.equals(_schoolkind) ? "LP24" : "L024";
            _testDate = StringUtils.defaultString(getNameMst(db2, "NAMESPARE1", namemstcd1, _testDiv));
            _senbatsuTestDate = StringUtils.defaultString(getNameMst(db2, "NAMESPARE1", namemstcd1, SENBATSU));
            _testdivAbbv1 = StringUtils.defaultString(getNameMst(db2, "ABBV1", namemstcd1, _testDiv));
            _documentroot = request.getParameter("DOCUMENTROOT");
            _imagepath = request.getParameter("IMAGEPATH");
            _staffStampFilePath = getImageFilePath(SCHOOL_KIND_P.equals(_schoolkind) ? "PRINCIPALSTAMP_P.bmp" : "PRINCIPALSTAMP_J.bmp");//校長印
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

        private CertifSchool getCertifSchool(final DB2UDB db2) {
            CertifSchool certifSchool = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            final String certifSearchCd = SCHOOL_KIND_P.equals(_schoolkind) ? "145": "105";
            try {
                ps = db2.prepareStatement("SELECT * FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _entexamyear + "' AND CERTIF_KINDCD = '" + certifSearchCd + "' ");
                rs = ps.executeQuery();
                if (rs.next()) {
                    final String schoolName = rs.getString("SCHOOL_NAME");
                    final String jobName = rs.getString("JOB_NAME");
                    final String principalName = rs.getString("PRINCIPAL_NAME");
                    final String remark1 = rs.getString("REMARK1");
                    final String remark6 = rs.getString("REMARK6");
                    final String remark7 = rs.getString("REMARK7");
                    final String remark10 = rs.getString("REMARK10");
                    certifSchool = new CertifSchool(schoolName, jobName, principalName, remark1, remark6, remark7, remark10);
                }
            } catch (SQLException ex) {
                log.debug("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return certifSchool;
        }

        private String getPassCd(final DB2UDB db2) {
            String retStr = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L013' AND NAMESPARE1 = '1' ");
                rs = ps.executeQuery();
                String sep = "";
                while (rs.next()) {
                    retStr += sep + "'" + rs.getString("NAMECD2") + "'";
                    sep = ",";
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return "".equals(retStr) ? "''" : retStr;
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
