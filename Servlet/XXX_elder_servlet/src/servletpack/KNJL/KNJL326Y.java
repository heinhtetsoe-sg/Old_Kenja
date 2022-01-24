/*
 * $Id: 6b203722b63efda8d4e8bc0c40b8508a697e0598 $
 *
 * 作成日: 2010/11/09
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;


import java.io.File;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;

/**
 *
 *  学校教育システム 賢者 [入試管理]
 *
 *                  ＜ＫＮＪＬ３２６Ｙ＞  中学各種通知書
 **/
public class KNJL326Y {

    private static final Log log = LogFactory.getLog(KNJL326Y.class);

    private boolean _hasData;

    Param _param;

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

            if ("0".equals(_param._output1)) {
                printMain(db2, svf, 0);
            } else if ("1".equals(_param._output1)) {
                printMain(db2, svf, 1);
            } else if ("2".equals(_param._output1)) {
                printMain(db2, svf, 2);
            }

            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            if (null != db2) {
                db2.commit();
                db2.close();
            }
            svf.VrQuit();
        }
    }

    private int getMS932count(String str) {
        int count = 0;
        try {
            count = str.getBytes("MS932").length;
        } catch (Exception e) {
            log.error(e);
        }
        return count;
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf, int flg) {

        final List applicants = getApplicants(db2, flg);
        try {
            for (final Iterator it = applicants.iterator(); it.hasNext(); ) {
                final Applicant applicant = (Applicant) it.next();

                String frm = null;
                if (applicant._namespare1 == 1) {
                    frm = "KNJL326Y_1.frm"; // 合格通知
                } else {
                    if ("2".equals(_param._testDiv)) {
                        frm = "KNJL326Y_2_2.frm"; // 不合格通知 再受験コメント有　第一回一般入試の時
                    } else if ("1".equals(_param._testDiv) || (("3".equals(_param._testDiv) || "5".equals(_param._testDiv)) && applicant._unPassCnt < 2)) {
                            frm = "KNJL326Y_2.frm"; // 不合格通知 再受験コメント有
                    } else if ("3".equals(_param._testDiv) || "4".equals(_param._testDiv) || "5".equals(_param._testDiv)) {
                        frm = "KNJL326Y_3.frm"; // 不合格通知 再受験コメント無
                    }
                }
                if (null == frm) {
                    continue;
                }

                svf.VrSetForm(frm, 1);

                svf.VrsOut("NENDO",      _param._entexamYear + "年度");
                svf.VrsOut("DATE",       _param._ndate);
                svf.VrsOut("TESTDIV",    _param._testdivName);
                svf.VrsOut("SCHOOLNAME", _param._schoolName);
                svf.VrsOut("JOBNAME",    _param._jobName);
                svf.VrsOut("STAFFNAME",  _param._principalName);
                if (null != _param._schoolStampFile) {
                    svf.VrsOut("SCHOOLSTAMP",  _param._schoolStampFile.toString());
                }
                svf.VrsOut("EXAMNO",    applicant._examno);
                svf.VrsOut("NAME",      applicant._name);
                svf.VrsOut("JUDGEMENT", applicant._judgedivname);

                final String numStr = ("3".equals(_param._testDiv) || "5".equals(_param._testDiv)) ? "第三回": "第二回";
                svf.VrsOut("NUM", numStr);

                if (applicant._namespare1 != 1) {
                    svf.VrsOut("LIMITDAY", _param._limitDate);
                    svf.VrsOut("APPDAY", _param._appDate); //出願日
                }
                svf.VrEndPage();
                _hasData = true;
            }

        } catch (Exception ex) {
            log.error("Exception:", ex);
        }
    }

    private List getApplicants(final DB2UDB db2, final int judge_flg) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        final List applicants = new ArrayList();
        try {
            final String sql = getApplicantSql(judge_flg);
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {

                final String examno = rs.getString("EXAMNO");
                final String name = rs.getString("NAME");
                final String judgediv = rs.getString("JUDGEDIV");
                final String judgedivName = rs.getString("JUDGEDIV_NAME");
                final int namespare1 = null == rs.getString("NAMESPARE1") ? -1 : Integer.parseInt(rs.getString("NAMESPARE1"));
                final int unPassCnt = Integer.parseInt(rs.getString("UNPASS_CNT"));

                final Applicant applicant = new Applicant(examno, name, judgediv, judgedivName, namespare1, unPassCnt);
                applicants.add(applicant);
            }

        } catch (SQLException ex) {
            log.error("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return applicants;
    }

    private String getApplicantSql(final int judge_flg) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH UNPASS_CNT AS ( ");
        stb.append("     SELECT ");
        stb.append("         T1.EXAMNO, ");
        stb.append("         count(*) as UNPASS_CNT ");
        stb.append("     FROM ");
        stb.append("         ENTEXAM_RECEPT_DAT T1 ");
        stb.append("         LEFT JOIN V_NAME_MST L013 ON L013.YEAR    = T1.ENTEXAMYEAR ");
        stb.append("                                  AND L013.NAMECD1 = 'L013' ");
        stb.append("                                  AND L013.NAMECD2 = T1.JUDGEDIV ");
        stb.append("     WHERE ");
        stb.append("             T1.ENTEXAMYEAR  = '" + _param._entexamYear + "' ");
        stb.append("         AND T1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("         AND T1.EXAM_TYPE    = '1' ");
        stb.append("         AND T1.JUDGEDIV     = '2' "); // 不合格
        stb.append("     GROUP BY ");
        stb.append("         T1.EXAMNO ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     T1.EXAMNO ");
        stb.append("     ,T3.NAME ");
        stb.append("     ,T1.JUDGEDIV ");
        stb.append("     ,NML013.NAMESPARE1 ");
        stb.append("     ,NML013.NAME1 AS JUDGEDIV_NAME ");
        stb.append("     ,T3.FS_CD ");
        stb.append("     ,NML001.NAME1 AS FINSCHOOL_DISTCD_NAME ");
        stb.append("     ,T2.FINSCHOOL_NAME ");
        stb.append("     ,value(UNPASS.UNPASS_CNT, 0) as UNPASS_CNT ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_RECEPT_DAT T1 ");
        stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DAT T3 ON T3.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("         AND T3.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("         AND T3.EXAMNO = T1.EXAMNO ");
        stb.append("     INNER JOIN ENTEXAM_APPLICANTDESIRE_DAT T4 ON T4.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("         AND T4.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("         AND T4.TESTDIV = T1.TESTDIV ");
        stb.append("         AND T4.EXAMNO = T1.EXAMNO ");
        stb.append("     INNER JOIN NAME_MST NML013 ON NML013.NAMECD1 = 'L013' AND NML013.NAMECD2 = T1.JUDGEDIV ");
        stb.append("     LEFT JOIN FINSCHOOL_MST T2 ON T2.FINSCHOOLCD = T3.FS_CD ");
        stb.append("     LEFT JOIN NAME_MST NML001 ON NML001.NAMECD1 = 'L001' AND NML001.NAMECD2 = T2.FINSCHOOL_DISTCD ");
        stb.append("     LEFT JOIN UNPASS_CNT UNPASS ON UNPASS.EXAMNO = T1.EXAMNO ");
        stb.append(" WHERE ");
        stb.append("     T1.ENTEXAMYEAR = '" + _param._entexamYear + "' ");
        stb.append("     AND T1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND T1.TESTDIV = '" + _param._testDiv + "' ");
        stb.append("     AND T1.EXAM_TYPE = '1' ");
        stb.append("     AND VALUE(T1.JUDGEDIV, '') <> '4' ");
        stb.append("     AND T4.DESIREDIV = '" + _param._desireDiv + "' ");
        if (0 == judge_flg) {
            stb.append("     AND T1.JUDGEDIV IS NOT NULL ");
        } else if (1 == judge_flg) {
            stb.append("     AND NML013.NAMESPARE1 = '1' ");
        } else if (2 == judge_flg) {
            stb.append("     AND T1.JUDGEDIV IS NOT NULL AND VALUE(NML013.NAMESPARE1, '') <> '1' ");
        }
        if (null != _param._examno) {
            stb.append("     AND T1.EXAMNO = '" + _param._examno + "' ");
        }
        stb.append(" ORDER BY ");
        stb.append("     T1.EXAMNO ");
        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 71479 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    private static class Applicant {
        final String _name;
        final String _examno;
        final String _judgediv;
        final String _judgedivname;
        final int _namespare1;
        final int _unPassCnt;
        public Applicant(
                final String examno,
                final String name,
                final String judgediv,
                final String judgedivName,
                final int namespare1,
                final int unPassCnt) {
            _name = name;
            _examno = examno;
            _judgediv = judgediv;
            _judgedivname = judgedivName;
            _namespare1 = namespare1;
            _unPassCnt = unPassCnt;
        }
    }


    /** パラメータクラス */
    private class Param {
        final String _entexamYear;
        final String _applicantDiv;
        final String _testDiv;
        final String _desireDiv;
        final String _title;
        final String _testdivName;
        final String _ndate;
        final String _limitDate;
        final String _appDate;
        final String _schoolName;
        final String _jobName;
        final String _principalName;
        final File _schoolStampFile;
        final String _output1;      // 出力範囲
        final String _examno;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamYear = request.getParameter("YEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _desireDiv = request.getParameter("DESIREDIV");
            _title = getApplicantdivName(db2);
            _testdivName = getTestDivName(db2);
            final SimpleDateFormat sdf = new SimpleDateFormat("yyyy年M月d日");
            _ndate = sdf.format(Date.valueOf(request.getParameter("NDATE").replace('/', '-')));
            final String limitDate = request.getParameter("LDATE");
            if (null == limitDate) {
                _limitDate = "";
            } else {
                final SimpleDateFormat sdf2 = new SimpleDateFormat("M月d日");
                final Date date = Date.valueOf(limitDate.replace('/', '-'));
                final Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                final int week = cal.get(Calendar.DAY_OF_WEEK);
                _limitDate = sdf2.format(date) + "（" + (0 <= week ? new String[]{null, "日", "月", "火", "水", "木", "金", "土"}[week] : "") + "）";
            }
            final String appDate = request.getParameter("APP_DATE");
            if (null == appDate) {
                _appDate = "";
            } else {
                final SimpleDateFormat sdf2 = new SimpleDateFormat("M月d日");
                final Date date = Date.valueOf(appDate.replace('/', '-'));
                final Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                final int week = cal.get(Calendar.DAY_OF_WEEK);
                _appDate = sdf2.format(date) + "（" + (0 <= week ? new String[]{null, "日", "月", "火", "水", "木", "金", "土"}[week] : "") + "）";
            }
            _schoolName = getCertifSchool(db2, "SCHOOL_NAME");
            _jobName = getCertifSchool(db2, "JOB_NAME");
            _principalName = getCertifSchool(db2, "PRINCIPAL_NAME");
            _schoolStampFile = getSchoolStamp(db2, request.getParameter("DOCUMENTROOT"));
            _output1 = request.getParameter("OUTPUT1");
            _examno = "2".equals(request.getParameter("OUTPUT2")) ? request.getParameter("EXAMNO") : null;           // 指定：受験番号
        }

        private String getApplicantdivName(DB2UDB db2) {
            String schoolName = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'L003' AND NAMECD2 = '" + _applicantDiv + "'");
                rs = ps.executeQuery();
                if (rs.next() && null != rs.getString("NAME1")) {
                  schoolName = rs.getString("NAME1");
                }
            } catch (SQLException e) {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return schoolName;
        }

        private String getTestDivName(DB2UDB db2) {
            String testDivName = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            final String namecd1 = "1".equals(_applicantDiv) ? "L024" : "L004";
            try {
                ps = db2.prepareStatement(" SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = '" + namecd1 + "' AND NAMECD2 = '" + _testDiv + "'");
                rs = ps.executeQuery();
                if (rs.next() && null != rs.getString("NAME1")) {
                  testDivName = rs.getString("NAME1");
                }
            } catch (SQLException e) {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return testDivName;
        }

        private String getCertifSchool(DB2UDB db2, final String field) {
            String rtn = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String cd = "1".equals(_applicantDiv) ? "105" : "106";
                final String sql = " SELECT " + field + " FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _entexamYear + "' AND CERTIF_KINDCD = '" + cd + "' ";
//                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = null == rs.getString(field) ? "" : rs.getString(field);
                }
            } catch (SQLException ex) {
                log.debug("Exception:", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }

        private File getSchoolStamp(final DB2UDB db2, final String documentRoot) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            String imagepath = "";
            String extension = "";
            try {
                ps = db2.prepareStatement(" SELECT IMAGEPATH, EXTENSION FROM CONTROL_MST WHERE CTRL_NO = '01' ");
                rs = ps.executeQuery();
                if (rs.next()) {
                    imagepath = rs.getString("IMAGEPATH");
                    extension = rs.getString("EXTENSION");
                }
            } catch (SQLException e) {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            final String jh = "1".equals(_applicantDiv) ? "J" : "H";
            final File schoolStampFile = new File(documentRoot + "/" + imagepath + "/SCHOOLSTAMP" + jh + "." + extension);
//            log.debug(" stamp file = " + schoolStampFile.toString());
            return schoolStampFile.exists() ? schoolStampFile : null;
        }
    }
}

// eof
