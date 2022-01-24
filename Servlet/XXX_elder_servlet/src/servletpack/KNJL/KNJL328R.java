/*
 * $Id: 48b3286879eb074c5c84a33e765bf7947c6d5c52 $
 *
 * 作成日: 2013/10/10
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

/**
 *  学校教育システム 賢者 [入試管理]
 *
 *                  ＜ＫＮＪＬ３２７Ｒ＞  各種帳票（個人宛）
 **/
public class KNJL328R {

    private static final Log log = LogFactory.getLog(KNJL328R.class);

    private boolean _hasData;
    private final String PASS_A = "5";
    private final String PASS_B = "6";
    private final String PASS_C = "7";

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
		final List list = Applicant.load(db2, _param);
		for (int line = 0; line < list.size(); line++) {
			final Applicant appl = (Applicant) list.get(line);
	        if ("1".equals(_param._form)) {
	            print1(db2, svf, appl);
	        } else if ("2".equals(_param._form)) {
	            print2(db2, svf, appl);
	        }
		}
    }

    public void print1(final DB2UDB db2, final Vrw32alp svf, final Applicant appl) {
        final boolean isGoukaku = "1".equals(appl._judgementDiv);

        final String form;
        form = "KNJL328R_1.frm";
        svf.VrSetForm(form, 1);
        svf.VrsOut("DATE", _param._noticedateStr);
        svf.VrsOut("EXAM_NO", appl._receptno);
        final String nameField = KNJ_EditEdit.getMS932ByteLength(appl._name) > 30 ? "3" : KNJ_EditEdit.getMS932ByteLength(appl._name) > 18 ? "2" : "1";
        svf.VrsOut("NAME" + nameField, appl._name);

        svf.VrsOut("NENDO", KNJ_EditDate.gengou(db2, Integer.parseInt(_param._entexamyear)) + "年度広島国際学院中学校");
        svf.VrsOut("TEST_DIV", _param._testdivName1);
        svf.VrsOut("JUDGE", appl._judgementName);

        if (isGoukaku) {
            // 合格
        } else {
            // 不合格
        }
        svf.VrsOut("SCHOOL_NAME", _param._schoolName); // 学校名
        svf.VrsOut("PRINCIPAL_NAME", _param._jobName + "　" + _param._principalName); // 職名＋校長名

        if (null != _param._imageFile) {
            svf.VrsOut("STAMP", _param._imageFile.toString());
        }
        svf.VrEndPage();
        _hasData = true;
    }

    public void print2(final DB2UDB db2, final Vrw32alp svf, final Applicant appl) {
        final String form;
        if (PASS_A.equals(appl._judgement)) {
            form = "KNJL328R_3.frm";
        } else if (PASS_B.equals(appl._judgement)) {
            form = "KNJL328R_4.frm";
        } else {
            form = "KNJL328R_5.frm";
        }
        svf.VrSetForm(form, 1);

        svf.VrsOut("DATE", _param._noticedateStr); // 日付
        svf.VrsOut("EXAM_NO", appl._receptno); // 受験番号
        svf.VrsOut("NAME1", appl._name + "　様"); // 氏名

        svf.VrsOut("SCHOOL_NAME", _param._schoolName); // 学校名
        svf.VrsOut("JOB_NAME", _param._jobName + "　" + _param._principalName); // 職名＋校長名

        final String startDate = KNJ_EditDate.h_format_JP(db2, _param._entexamyear + "-04-01");
        final String endDate = KNJ_EditDate.h_format_JP(db2, (Integer.parseInt(_param._entexamyear) + 1) + "-03-31");
        svf.VrsOut("PERIOD", startDate + "\uFF5E" + endDate);


        if (null != _param._imageFile) {
            svf.VrsOut("STAMP", _param._imageFile.toString());
        }

        svf.VrEndPage();
        _hasData = true;
    }

    private static class Applicant {
        final String _receptno;
        final String _name;
        final String _judgement;
        final String _judgementDiv;
        final String _judgementName;

        Applicant(
            final String receptno,
            final String name,
            final String judgement,
            final String judgementDiv,
            final String judgementName
        ) {
            _receptno = receptno;
            _name = name;
            _judgement = judgement;
            _judgementDiv = judgementDiv;
            _judgementName = judgementName;
        }

        public static List load(final DB2UDB db2, final Param param) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = sql(param);
                log.debug(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String receptno = rs.getString("RECEPTNO");
                    final String name = rs.getString("NAME");
                    final String judgement = rs.getString("JUDGEDIV");
                    final String judgementDiv = rs.getString("JUDGEMENT_DIV");
                    final String judgementName = rs.getString("JUDGEMENT_NAME");
                    final Applicant applicant = new Applicant(receptno, name, judgement, judgementDiv, judgementName);
                    list.add(applicant);
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }

        public static String sql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT  ");
            stb.append("     RECEPT.RECEPTNO, ");
            stb.append("     BASE.NAME, ");
            stb.append("     RECEPT.JUDGEDIV, ");
            stb.append("     CASE WHEN RECEPT.JUDGEDIV IS NOT NULL THEN ");
            stb.append("         CASE WHEN NML013.NAMESPARE1 = '1' THEN '1' ELSE '2' END ");
            stb.append("     END AS JUDGEMENT_DIV, ");
            stb.append("     CASE WHEN RECEPT.JUDGEDIV IS NOT NULL THEN ");
            stb.append("         CASE WHEN NML013.NAMESPARE1 = '1' THEN '合格' ELSE '不合格' END ");
            stb.append("     END AS JUDGEMENT_NAME ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_RECEPT_DAT RECEPT ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DAT BASE ON RECEPT.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
            stb.append("         AND RECEPT.APPLICANTDIV = BASE.APPLICANTDIV ");
            stb.append("         AND RECEPT.EXAMNO = BASE.EXAMNO ");
            stb.append("     LEFT JOIN NAME_MST NML013 ON NML013.NAMECD1 = 'L013' ");
            stb.append("         AND NML013.NAMECD2 = RECEPT.JUDGEDIV ");
            stb.append(" WHERE ");
            stb.append("     RECEPT.ENTEXAMYEAR = '" + param._entexamyear + "' ");
            stb.append("     AND RECEPT.APPLICANTDIV = '" + param._applicantdiv + "' ");
            stb.append("     AND RECEPT.TESTDIV = '" + param._testdiv + "' ");
            stb.append("     AND RECEPT.EXAM_TYPE = '1' ");
            stb.append("     AND RECEPT.JUDGEDIV IS NOT NULL ");
            stb.append("     AND RECEPT.JUDGEDIV NOT IN ('3', '4') ");
            if ("1".equals(param._form)) {
                if ("1".equals(param._output1)) { // 合格・不合格
                } else if ("2".equals(param._output1)) { // 合格のみ
                    stb.append("     AND NML013.NAMESPARE1 = '1' ");
                } else if ("3".equals(param._output1)) { // 不合格のみ
                    stb.append("     AND VALUE(NML013.NAMESPARE1, '') <> '1' ");
                }
                if ("2".equals(param._output2)) {
                    stb.append("     AND RECEPT.RECEPTNO = '" + param._receptno1 + "' ");
                }
            } else if ("2".equals(param._form)) {
                stb.append("     AND NML013.NAMESPARE1 = '1' ");
                stb.append("     AND RECEPT.HONORDIV = '1' ");
                if ("2".equals(param._output3)) {
                    stb.append("     AND RECEPT.RECEPTNO = '" + param._receptno2 + "' ");
                }
            }
            stb.append(" ORDER BY ");
            stb.append("     RECEPT.RECEPTNO ");
            return stb.toString();
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 64600 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _entexamyear;
        final String _applicantdiv;
        final String _testdiv;
        final String _noticedate;
        final String _form;         // 1:通知 2:特待生決定通知書
        final String _output1;      // 出力通知 1:合格＋不合格 2:合格 3:不合格
        final String _output2;      // 出力通知対象 1:全員 2:指定
        final String _receptno1;    // 通知:受験番号指定
        final String _output3;      // 特待生決定通知書対象 1:全員 2:指定
        final String _receptno2;    // 特待生決定:受験番号指定
        final String _documentroot;

        final String _applicantdivName;
        final String _testdivName1;
        final String _testdivAbbv1;
        final String _noticedateStr;
        final File _imageFile;
        final String _schoolName;
        final String _jobName;
        final String _principalName;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _applicantdiv = request.getParameter("APPLICANTDIV");
            _testdiv = request.getParameter("TESTDIV");
            _noticedate = request.getParameter("NOTICEDATE").replace('/', '-');
            _noticedateStr = getDateStr(db2, _noticedate);
            _form = request.getParameter("FORM");
            _output1 = request.getParameter("OUTPUT1");
            _output2 = request.getParameter("OUTPUT2");
            _receptno1 = request.getParameter("RECEPTNO1");
            _output3 = request.getParameter("OUTPUT3");
            _receptno2 = request.getParameter("RECEPTNO2");
            _documentroot = request.getParameter("DOCUMENTROOT");

            _applicantdivName = getNameMst(db2, "NAME1", "L003", _applicantdiv);
            _testdivName1 = getNameMst(db2, "NAME1", "L024", _testdiv);
            _testdivAbbv1 = getNameMst(db2, "ABBV1", "L024", _testdiv);
            _imageFile = getImageFile(db2);
            _schoolName = getCertifSchoolDat(db2, "SCHOOL_NAME", false);
            _jobName = StringUtils.defaultString(getCertifSchoolDat(db2, "JOB_NAME", false));
            _principalName = StringUtils.defaultString(getCertifSchoolDat(db2, "PRINCIPAL_NAME", true));
        }

        private String getDateStr(final DB2UDB db2, final String date) {
            if (null == date) {
                return null;
            }
            return KNJ_EditDate.h_format_JP(db2, date);
        }

        private String getExamCourseName(final DB2UDB db2, final String field, final String examcoursecd) {
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                StringBuffer sql = new StringBuffer();
                sql.append(" SELECT " + field + " ");
                sql.append(" FROM ENTEXAM_COURSE_MST ");
                sql.append(" WHERE ENTEXAMYEAR = '" + _entexamyear + "' ");
                sql.append("   AND APPLICANTDIV = '" + _applicantdiv + "' ");
                sql.append("   AND TESTDIV = '" + _testdiv + "' ");
                sql.append("   AND EXAMCOURSECD = '" + examcoursecd + "' ");
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString(field);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return rtn;
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

