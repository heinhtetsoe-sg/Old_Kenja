/*
 * $Id: 8331aaf8473e67fc7f3c53db4e450cfb636ba45c $
 *
 * 作成日: 2017/07/06
 * 作成者: maesiro
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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_Control;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJL327P {

    private static final Log log = LogFactory.getLog(KNJL327P.class);

    private static final String CYUGAKU = "1";
    private static final String KOUKOU = "2";

    private static final String KENGAI_ZENKI = "1";
    private static final String KENNAI_ZENKI = "2";
    private static final String KENNAI_KOUKI = "3";

    private static final String KENNAI_SUISEN = "1";
    private static final String KENNAI_IPPAN = "2";

    private static final String GOUKAKU = "1";
    private static final String FUGOUKAKU = "2";

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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        String strdir1 = "ryoma.jpg";
        String strdir2 = _param._documentroot + "/" + _param._folder + "/" + strdir1;
        File f1 = new File(strdir2);   //写真データ存在チェック用

        final List studentList = getStudentList(db2);
        for (Iterator itStudent = studentList.iterator(); itStudent.hasNext();) {
            if (KOUKOU.equals(_param._applicantDiv)) {
                if (GOUKAKU.equals(_param._resalt)) {
                    setForm_3(svf);
                } else {
                    setForm_4(svf);
                }
            }

            final Student student = (Student) itStudent.next();
            //写真
            if (f1.exists()) {
                svf.VrsOut("PIC", strdir2);
            } else {
                System.out.println("not jpg fail");
            }
            if (null != student._zipcd) {
            	svf.VrsOut("ZIPNO", "〒" + student._zipcd);
            }

            final int maxAddrLen = getMS932ByteLength(student._address1) > getMS932ByteLength(student._address2) ? getMS932ByteLength(student._address1) : getMS932ByteLength(student._address2);
            final String addrField = maxAddrLen > 50 ? "3" : maxAddrLen > 40 ? "2" : "1";
            svf.VrsOut("ADDR1_" + addrField, student._address1);
            svf.VrsOut("ADDR2_" + addrField, student._address2);
            final String setName = student._name + " 様";
            final String nameField = getMS932ByteLength(setName) > 36 ? "3" : getMS932ByteLength(setName) > 26 ? "2" : "1";
            svf.VrsOut("NAME" + nameField, setName);
            svf.VrsOut("EXAM_NO", student._examno);
            svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._tsuchi));
            svf.VrsOut("SCHOOL_NAME", _param._certifSchool._schoolName);
            svf.VrsOut("STAFF_NAME", _param._certifSchool._jobName + _param._certifSchool._principalName);

            svf.VrsOut("TOTAL", student._total4);
            svf.VrsOut("RANK1", student._totalRank4);
            svf.VrsOut("RANK2", _param._passingMark9._syutsuganSu);
            svf.VrEndPage();
            _hasData = true;
        }
    }

    private void setForm_3(final Vrw32alp svf) {
        svf.VrSetForm("KNJL327P_3.frm", 1);
        final String zenkakuNendo = KNJ_EditEdit.convertZenkakuSuuji(KNJ_EditDate.h_format_JP_N(_param._entexamYear + "-04-01"));
        final String setText1 = "　この度は、" + zenkakuNendo + "度土佐塾高等学校入学試験を受験して"
                + "いただきありがとうございました。";
        svf.VrsOut("TEXT1", setText1);

        final String oriDate = KNJ_EditEdit.convertZenkakuSuuji(KNJ_EditDate.h_format_JP_MD(_param._oridate));
        final String oriWeek = KNJ_EditEdit.convertZenkakuSuuji(KNJ_EditDate.h_format_W(_param._oridate));
        final String oriTime = KNJ_EditEdit.convertZenkakuSuuji(_param._orihour + "時" + _param._oriminute + "分");
        final String oriText = oriDate + "(" + oriWeek + ")" + oriTime;
        final String setText2 = "　つきましては、" + oriText + "から本校で行う合格者の説明会には、"
                + "保護者同伴で必ず出席され、入学手続きをおとりくださるようお願い申し上げます。";
        svf.VrsOut("TEXT2", setText2);
    }

    private void setForm_4(final Vrw32alp svf) {
        svf.VrSetForm("KNJL327P_4.frm", 1);
        final String zenkakuNendo = KNJ_EditEdit.convertZenkakuSuuji(KNJ_EditDate.h_format_JP_N(_param._entexamYear + "-04-01"));
        final String setText1 = "　この度は、" + zenkakuNendo + "度土佐塾高等学校入学試験を受験して"
                + "いただきありがとうございました。";
        svf.VrsOut("TEXT1", setText1);
    }

    private List getStudentList(final DB2UDB db2) throws SQLException {
        final List retList = new ArrayList();

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final String sql = getStudentsql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String examno = rs.getString("EXAMNO");
                final String name = rs.getString("NAME");
                final String zipcd = rs.getString("ZIPCD");
                final String address1 = rs.getString("ADDRESS1");
                final String address2 = rs.getString("ADDRESS2");
                final String total2 = rs.getString("TOTAL2");
                final String totalRank2 = rs.getString("TOTAL_RANK2");
                final String total4 = rs.getString("TOTAL4");
                final String totalRank4 = rs.getString("TOTAL_RANK4");

                final Student student = new Student(examno, name, zipcd, address1, address2, total2, totalRank2, total4, totalRank4);
                retList.add(student);
            }

            db2.commit();
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
        return retList;
    }

    private String getStudentsql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     BASE.EXAMNO, ");
        stb.append("     BASE.NAME, ");
        stb.append("     ADDR.ZIPCD, ");
        stb.append("     ADDR.ADDRESS1, ");
        stb.append("     ADDR.ADDRESS2, ");
        stb.append("     RECEPT.TOTAL2, ");
        stb.append("     RECEPT.TOTAL_RANK2, ");
        stb.append("     RECEPT.TOTAL4, ");
        stb.append("     RECEPT.TOTAL_RANK4 ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_RECEPT_DAT RECEPT ");
        stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE ON RECEPT.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
        stb.append("           AND RECEPT.APPLICANTDIV = BASE.APPLICANTDIV ");
        stb.append("           AND RECEPT.EXAMNO = BASE.EXAMNO ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT ADDR ON RECEPT.ENTEXAMYEAR = ADDR.ENTEXAMYEAR ");
        stb.append("          AND RECEPT.APPLICANTDIV = ADDR.APPLICANTDIV ");
        stb.append("          AND RECEPT.EXAMNO = ADDR.EXAMNO ");
        stb.append(" WHERE ");
        stb.append("     RECEPT.ENTEXAMYEAR = '" + _param._entexamYear + "' ");
        stb.append("     AND RECEPT.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND RECEPT.TESTDIV = '" + _param._testDiv + "' ");
        stb.append("     AND RECEPT.EXAM_TYPE = '1' ");
        if ("1".equals(_param._jizen) || "1".equals(_param._jizenUnpass)) {
            //全員出力
        } else if (_param._isExamnoShitei) {
            stb.append("     AND RECEPT.EXAMNO = '" + _param._textexamno + "' ");
        } else if (GOUKAKU.equals(_param._resalt)) {
            stb.append("     AND RECEPT.JUDGEDIV = '1' ");
        } else if (FUGOUKAKU.equals(_param._resalt)) {
            stb.append("     AND RECEPT.JUDGEDIV = '2' ");
        }
        stb.append(" ORDER BY ");
        if ("1".equals(_param._order)) {
            stb.append("     RECEPT.TOTAL_RANK4, ");
        }
        stb.append("     BASE.EXAMNO ");
        return stb.toString();
    }

    /**
     *  文字数を取得
     */
    private static int getMS932ByteLength(final String str)
    {
        int ret = 0;
        if (null != str) {
            try {
                ret = str.getBytes("MS932").length;
            } catch (Exception ex) {
                log.error("retStringByteValue error!", ex);
            }
        }
        return ret;
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 57839 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _applicantDiv;
        final String _applicantDivName;
        final String _testDiv;
        final String _testDivName;
        final String _testDivAbbv;
        final String _testDate;
        final String _tsuchi;
        final String _resalt;
        final String _jizen;
        final String _jizenUnpass;
        final String _oridate;
        final String _orihour;
        final String _oriminute;
        final String _order;
        final boolean _isExamnoShitei;
        final String _textexamno;
        final String _entexamYear;
        final String _loginYear;
        final String _loginSemester;
        final String _loginDate;
        final String _documentroot;
        final String _folder;
        final String _extention;
        final CertifSchool _certifSchool;
        final PassingMark _passingMark9;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _entexamYear = request.getParameter("ENTEXAMYEAR");
            _loginYear = request.getParameter("LOGIN_YEAR");
            _loginSemester = request.getParameter("LOGIN_SEMESTER");
            _loginDate = request.getParameter("LOGIN_DATE");
            _documentroot = request.getParameter("DOCUMENTROOT");
            KNJ_Control imagepath_extension = new KNJ_Control();            //取得クラスのインスタンス作成
            KNJ_Control.ReturnVal returnval = imagepath_extension.Control(db2);
            _folder = returnval.val4;                                           //写真データ格納フォルダ
            _extention = returnval.val5;                                          //写真データの拡張子
            _applicantDivName = getNameMst(db2, "L003", _applicantDiv, "NAME1");
            final String testNameCd1 = "1".equals(_applicantDiv) ? "L024" : "L004";
            _testDivName = getNameMst(db2, testNameCd1, _testDiv, "NAME1");
            _testDivAbbv = getNameMst(db2, testNameCd1, _testDiv, "ABBV1");
            _testDate = getNameMst(db2, testNameCd1, _testDiv, "NAMESPARE1");
            _tsuchi = request.getParameter("TSUCHI");
            _resalt = request.getParameter("RESALT");
            _jizen = request.getParameter("JIZEN");
            _jizenUnpass = request.getParameter("JIZEN_UNPASS");
            _oridate = request.getParameter("ORIDATE");
            _orihour = request.getParameter("ORIHOUR");
            _oriminute = request.getParameter("ORIMINUTE");
            _order = request.getParameter("ORDER");
            _isExamnoShitei = "2".equals(request.getParameter("ALLFLG"));
            _textexamno = request.getParameter("TEXTEXAMNO");
            _certifSchool = getCertifSchool(db2, _applicantDiv);
            _passingMark9 = getPassingMark(db2);
        }

        private String getNameMst(final DB2UDB db2, final String nameCd1, final String nameCd2, final String fieldName) throws SQLException {
            String retStr = "";
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = getNameMstSql(nameCd1, nameCd2);
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    retStr = rs.getString(fieldName);
                }

                db2.commit();
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retStr;
        }

        private String getNameMstSql(final String namecd1, final String namecd2) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     * ");
            stb.append(" FROM ");
            stb.append("     NAME_MST ");
            stb.append(" WHERE ");
            stb.append("     NAMECD1 = '" + namecd1 + "' ");
            if (!"".equals(namecd2)) {
                stb.append("     AND NAMECD2 = '" + namecd2 + "' ");
            }
            stb.append(" ORDER BY ");
            stb.append("     NAMECD2 ");
            return stb.toString();
        }

        private CertifSchool getCertifSchool(final DB2UDB db2, final String applicantDiv) throws SQLException {
            CertifSchool retCertifSchoo = new CertifSchool("", "", "");
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String certifKind = CYUGAKU.equals(applicantDiv) ? "105" : "106";
                final String sql = getCertifSchoolSql(certifKind);
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    retCertifSchoo = new CertifSchool(rs.getString("SCHOOL_NAME"), rs.getString("JOB_NAME"), rs.getString("PRINCIPAL_NAME"));
                }

                db2.commit();
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retCertifSchoo;
        }

        private String getCertifSchoolSql(final String certifKind) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     * ");
            stb.append(" FROM ");
            stb.append("     CERTIF_SCHOOL_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _entexamYear + "' ");
            stb.append("     AND CERTIF_KINDCD = '" + certifKind + "' ");
            return stb.toString();
        }

        private PassingMark getPassingMark(DB2UDB db2) throws SQLException {
            PassingMark passingMark = new PassingMark("", "");
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = getPassingMarkSql();
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    passingMark = new PassingMark(rs.getString("BORDER_SCORE"), rs.getString("SUCCESS_CNT_SPECIAL"));
                }

                db2.commit();
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
            return passingMark;
        }

        private String getPassingMarkSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     * ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_PASSINGMARK_MST ");
            stb.append(" WHERE ");
            stb.append("     ENTEXAMYEAR = '" + _entexamYear + "' ");
            stb.append("     AND APPLICANTDIV = '" + _applicantDiv + "' ");
            stb.append("     AND TESTDIV = '" + _testDiv + "' ");
            stb.append("     AND EXAM_TYPE = '1' ");
            stb.append("     AND SHDIV = '9' ");
            stb.append("     AND COURSECD = '0' ");
            stb.append("     AND MAJORCD = '000' ");
            stb.append("     AND EXAMCOURSECD = '0000' ");
            return stb.toString();
        }

    }

    /** 証明書学校 */
    private class CertifSchool {
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

    private class PassingMark {
        final String _borderScore;
        final String _syutsuganSu;

        public PassingMark(
                final String borderScore,
                final String syutsuganSu
        ) {
            _borderScore = borderScore;
            _syutsuganSu = syutsuganSu;
        }

    }

    /** 生徒 */
    private class Student {
        final String _examno;
        final String _name;
        final String _zipcd;
        final String _address1;
        final String _address2;
        final String _total2;
        final String _totalRank2;
        final String _total4;
        final String _totalRank4;

        public Student(
                final String examno,
                final String name,
                final String zipcd,
                final String address1,
                final String address2,
                final String total2,
                final String totalRank2,
                final String total4,
                final String totalRank4
        ) {
            _examno = examno;
            _name = name;
            _zipcd = zipcd;
            _address1 = address1;
            _address2 = address2;
            _total2 = total2;
            _totalRank2 = totalRank2;
            _total4 = total4;
            _totalRank4 = totalRank4;
        }

    }
}

// eof

