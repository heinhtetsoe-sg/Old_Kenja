/*
 * $Id: 763a8ce08222ddebca83ff3bc600bf8023a80c6b $
 *
 * 作成日: 2017/07/06
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;


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
import servletpack.KNJZ.detail.KNJ_Control;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJL328P {

    private static final Log log = LogFactory.getLog(KNJL328P.class);

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

        final String gengou = KNJ_EditDate.h_format_JP(db2, _param._loginYear + "-12-31");
        final String gengouNen = KNJ_EditDate.tate_format(gengou)[1];
        final String setSyosyo = ("元".equals(gengouNen) ? "1" : StringUtils.defaultString(gengouNen)) + StringUtils.defaultString(_param._certifSchool._syosyoName) + StringUtils.defaultString(_param._documentno) + StringUtils.defaultString(_param._certifSchool._syosyoName2);
        final String tsuchiDateStr = KNJ_EditDate.h_format_JP(db2, _param._tsuchi);
        final List studentList = getStudentList(db2);
        for (Iterator itStudent = studentList.iterator(); itStudent.hasNext();) {
            final Student student = (Student) itStudent.next();
            if (GOUKAKU.equals(student._judgeDiv)) {
                setForm_1(db2, svf);
            } else {
                setForm_2(svf);
            }
			svf.VrsOut("DATE", tsuchiDateStr);
            svf.VrsOut("CERT_NO", setSyosyo);
            if (null != student._zipcd) {
                svf.VrsOut("ZIPNO", "〒" + student._zipcd);
            }

            final int maxAddrLen = Math.max(getMS932ByteLength(student._address1), getMS932ByteLength(student._address2));
            final String addrField = maxAddrLen > 50 ? "3" : maxAddrLen > 40 ? "2" : "1";
            svf.VrsOut("ADDR1_" + addrField, student._address1);
            svf.VrsOut("ADDR2_" + addrField, student._address2);
            final String setName = student._finschoolName + "中学校長 様";
            final String nameField = getMS932ByteLength(setName) > 42 ? "3" : getMS932ByteLength(setName) > 32 ? "2" : "1";
            svf.VrsOut("NAME" + nameField, setName);
            svf.VrsOut("SCHOOL_NAME", _param._certifSchool._schoolName);
            svf.VrsOut("STAFF_NAME", _param._certifSchool._jobName + _param._certifSchool._principalName);

            final String nameStdField = getMS932ByteLength(student._name) > 20 ? "3" : getMS932ByteLength(student._name) > 16 ? "2" : "1";
            svf.VrsOut("STUDENT_NAME" + nameStdField, student._name);
            svf.VrsOut("EXAM_NO", student._examno);
            svf.VrsOut("JUDGE", student._judgeName);
            svf.VrEndPage();
            _hasData = true;
        }
    }

    private void setForm_1(final DB2UDB db2,final Vrw32alp svf) {
        svf.VrSetForm("KNJL328P_1.frm", 1);
        final String shimekiriDay = KNJ_EditEdit.convertZenkakuSuuji(_param._shimekiriStr);
        final String shimekiriWeek = KNJ_EditEdit.convertZenkakuSuuji(KNJ_EditDate.h_format_W(_param._shimekiri));
        final String setText1 = "入学手続きの締切は" + shimekiriDay + "(" + shimekiriWeek + ")となっております";
        final String setText2 = "ので、貴校からも本人にご確認くだされば幸いに存じます。";
        svf.VrsOut("TEXT1", setText1);
        svf.VrsOut("TEXT2", setText2);
    }

    private void setForm_2(final Vrw32alp svf) {
        svf.VrSetForm("KNJL328P_2.frm", 1);

        final String zenDate = KNJ_EditEdit.convertZenkakuSuuji(KNJ_EditDate.h_format_JP_MD(_param._testKoukiDate1));
        final String zenWeek = KNJ_EditEdit.convertZenkakuSuuji(KNJ_EditDate.h_format_W(_param._testKoukiDate1));
        final String setZenDate = zenDate + "(" + zenWeek + ")";

        final String shutuganDate = KNJ_EditEdit.convertZenkakuSuuji(KNJ_EditDate.h_format_JP_MD(_param._shutsugan));
        final String shutuganWeek = KNJ_EditEdit.convertZenkakuSuuji(KNJ_EditDate.h_format_W(_param._shutsugan));
        final String setShutuganDate = shutuganDate + "(" + shutuganWeek + "・必着)";

//        final String setText1 = "　なお、当校の一般入学試験は" + setZenDate + "に予定いたしており"
//                + "ます。一般入学試験を推薦入学試験受験者が受験いただく際には、"
//                + "選抜料を免除させていただきます。また、調査書は既にいただいて"
//                + "おりますので、重ねてご提出の必要はございません。出願期間は" + setShutuganDate
//                + "までとなっておりますので申し添えます。";
//        svf.VrsOut("TEXT1", setText1);
        svf.VrsOut("EXAM_DATE", setZenDate);
        svf.VrsOut("APPLI_DATE", setShutuganDate);
    }

//    private void setTitle(final Vrw32alp svf) {
//        svf.VrsOut("TITLE", KNJ_EditDate.h_format_JP_N(_param._entexamYear + "-04-01") + "度　" + _param._applicantDivName + "入試 " + _param._testDivName + "　結果");
//        svf.VrsOut("SUBTITLE", "受験生度数分布表");
//        svf.VrsOut("DATE", "実施日：" + KNJ_EditDate.h_format_JP(_param._testDate));
//    }

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
                final String fsCd = rs.getString("FS_CD");
                final String finschoolName = rs.getString("FINSCHOOL_NAME");
                final String judgeDiv = rs.getString("JUDGEDIV");
                final String judgeName = rs.getString("JUDGENAME");

                final Student student = new Student(examno, name, zipcd, address1, address2, fsCd, finschoolName, judgeDiv, judgeName);
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
        stb.append("     FINSCHOOL.FINSCHOOL_ZIPCD AS ZIPCD, ");
        stb.append("     FINSCHOOL.FINSCHOOL_ADDR1 AS ADDRESS1, ");
        stb.append("     FINSCHOOL.FINSCHOOL_ADDR2 AS ADDRESS2, ");
        stb.append("     BASE.FS_CD, ");
        stb.append("     FINSCHOOL.FINSCHOOL_NAME, ");
        if (!"1".equals(_param._jizen)) {
            stb.append("     RECEPT.JUDGEDIV, ");
        } else {
            stb.append("     '1' AS JUDGEDIV, ");
        }
        stb.append("     L013.NAME1 AS JUDGENAME ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_RECEPT_DAT RECEPT ");
        stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE ON RECEPT.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
        stb.append("           AND RECEPT.APPLICANTDIV = BASE.APPLICANTDIV ");
        stb.append("           AND RECEPT.EXAMNO = BASE.EXAMNO ");
        stb.append("           AND BASE.FS_CD IS NOT NULL ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT ADDR ON RECEPT.ENTEXAMYEAR = ADDR.ENTEXAMYEAR ");
        stb.append("          AND RECEPT.APPLICANTDIV = ADDR.APPLICANTDIV ");
        stb.append("          AND RECEPT.EXAMNO = ADDR.EXAMNO ");
        stb.append("     LEFT JOIN FINSCHOOL_MST FINSCHOOL ON BASE.FS_CD = FINSCHOOL.FINSCHOOLCD ");
        stb.append("     LEFT JOIN NAME_MST L013 ON L013.NAMECD1 = 'L013' ");
        if (!"1".equals(_param._jizen)) {
            stb.append("          AND RECEPT.JUDGEDIV = L013.NAMECD2 ");
        } else {
            stb.append("          AND L013.NAMECD2 = '1' ");
        }
        stb.append(" WHERE ");
        stb.append("     RECEPT.ENTEXAMYEAR = '" + _param._entexamYear + "' ");
        stb.append("     AND RECEPT.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND RECEPT.TESTDIV = '" + _param._testDiv + "' ");
        stb.append("     AND RECEPT.EXAM_TYPE = '1' ");
        if (!"1".equals(_param._jizen)) {
            stb.append("     AND RECEPT.JUDGEDIV IN ('1', '2') ");
        }
        if (_param._isFinSchoolShitei) {
            stb.append("     AND BASE.FS_CD = '" + _param._finschoolCd + "' ");
        }
        stb.append(" ORDER BY ");
        stb.append("     BASE.FS_CD, ");
        if (!"1".equals(_param._jizen)) {
            stb.append("     RECEPT.JUDGEDIV, ");
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
        log.fatal("$Revision: 71608 $");
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
        final String _testKoukiDate1;
        final String _tsuchi;
        final String _jizen;
        final boolean _isFinSchoolShitei;
        final String _finschoolCd;
        final String _shimekiri;
        final String _shimekiriStr;
        final String _shutsugan;
        final String _documentno;
        final String _entexamYear;
        final String _loginYear;
        final String _loginSemester;
        final String _loginDate;
        final String _documentroot;
        final String _folder;
        final String _extention;
        final String _jisshiDate;
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
            _jisshiDate = KNJ_EditDate.h_format_JP(db2, _testDate);
            _testKoukiDate1 = getNameMst(db2, testNameCd1, "2", "NAMESPARE1");
            _tsuchi = request.getParameter("TSUCHI");
            _jizen = request.getParameter("JIZEN");
            _isFinSchoolShitei = "2".equals(request.getParameter("ALLFLG"));
            _shimekiri = request.getParameter("SHIMEKIRI");
            _shimekiriStr = KNJ_EditDate.h_format_JP(db2, _shimekiri);
            _shutsugan = request.getParameter("SHUTSUGAN");
            _documentno = request.getParameter("DOCUMENTNO");
            _finschoolCd = request.getParameter("FINSCHOOLCD");
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
            CertifSchool retCertifSchoo = new CertifSchool("", "", "", "", "");
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String certifKind = CYUGAKU.equals(applicantDiv) ? "105" : "106";
                final String sql = getCertifSchoolSql(certifKind);
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    retCertifSchoo = new CertifSchool(rs.getString("SCHOOL_NAME"), rs.getString("JOB_NAME"), rs.getString("PRINCIPAL_NAME"), rs.getString("SYOSYO_NAME"), rs.getString("SYOSYO_NAME2"));
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
        final String _syosyoName;
        final String _syosyoName2;

        public CertifSchool(
                final String schoolName,
                final String jobName,
                final String principalName,
                final String syosyoName,
                final String syosyoName2
        ) {
            _schoolName = schoolName;
            _jobName = jobName;
            _principalName = principalName;
            _syosyoName = syosyoName;
            _syosyoName2 = syosyoName2;
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
        final String _fsCd;
        final String _finschoolName;
        final String _judgeDiv;
        final String _judgeName;
        public Student(
                final String examno,
                final String name,
                final String zipcd,
                final String address1,
                final String address2,
                final String fsCd,
                final String finschoolName,
                final String judgeDiv,
                final String judgeName
        ) {
            _examno = examno;
            _name = name;
            _zipcd = zipcd;
            _address1 = address1;
            _address2 = address2;
            _fsCd = fsCd;
            _finschoolName = finschoolName;
            _judgeDiv = judgeDiv;
            _judgeName = judgeName;
        }

    }
}

// eof

