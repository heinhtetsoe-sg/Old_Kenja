/*
 * $Id: a10efc4bc03f526d4d9648f5c947bc0ad158b1dd $
 *
 * 作成日: 2013/10/10
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Calendar;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 *  学校教育システム 賢者 [入試管理]
 *
 *                  ＜ＫＮＪＬ３２４Ｒ＞  一般入学試験分析表
 **/
public class KNJL324R {

    private static final Log log = LogFactory.getLog(KNJL324R.class);

    private boolean _hasData;

    private static String coursecd1 = "0001";
    private static String coursecd2 = "0002";
    private static String coursecd3 = "0003";
    private static String coursecd4 = "0004";
    private static String coursecdIsNull = "IS NULL";

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
        final String form = "KNJL324R.frm";
        svf.VrSetForm(form, 1);
        svf.VrsOut("DATE", _param._dateStr); // 年度
        svf.VrsOut("NENDO", KenjaProperties.gengou(Integer.parseInt(_param._entexamyear))); // 年度
        svf.VrsOut("TEST", _param._testdivAbbv1); // 年度

        // GS
        printGS(db2, svf, "1");
        // GA
        printGA(db2, svf, "2");
        // GB
        printGB(db2, svf, "3");
        // GC
        printGC(db2, svf, "4");
        // 合計
        printCount(svf, "", ApplicantCount.load(db2, _param, null, null, null));

        svf.VrEndPage();
        _hasData = true;
    }

    private void printGS(final DB2UDB db2, final Vrw32alp svf, final String k) {
        final String[] wish = {coursecd1};
        svf.VrsOut("EXAMCOURSE_NAME" + k, _param._examcourseName1); // 受験コース
        final ApplicantCount[] counts = new ApplicantCount [] {
                ApplicantCount.load(db2, _param, "1", wish, coursecd2),
                ApplicantCount.load(db2, _param, "2", wish, coursecd2),
                ApplicantCount.load(db2, _param, "1", wish, coursecd3),
                ApplicantCount.load(db2, _param, "2", wish, coursecd3),
                ApplicantCount.load(db2, _param, "1", wish, coursecd4),
                ApplicantCount.load(db2, _param, "2", wish, coursecd4),
                ApplicantCount.load(db2, _param, "1", wish, coursecdIsNull),
                ApplicantCount.load(db2, _param, "2", wish, coursecdIsNull),
                ApplicantCount.load(db2, _param, null, wish, null)
        };
        for (int i = 0; i < counts.length; i++) {
            printnCount(svf, k, i + 1, counts[i]);
        }
    }

    private void printGA(final DB2UDB db2, final Vrw32alp svf, final String k) {
        final String[] wish = {coursecd2};
        svf.VrsOut("EXAMCOURSE_NAME" + k, _param._examcourseName2); // 受験コース
        final ApplicantCount[] counts = new ApplicantCount [] {
                ApplicantCount.load(db2, _param, "1", wish, coursecd2),
                ApplicantCount.load(db2, _param, "2", wish, coursecd2),
                ApplicantCount.load(db2, _param, "1", wish, coursecd3),
                ApplicantCount.load(db2, _param, "2", wish, coursecd3),
                ApplicantCount.load(db2, _param, "1", wish, coursecd4),
                ApplicantCount.load(db2, _param, "2", wish, coursecd4),
                ApplicantCount.load(db2, _param, "1", wish, coursecdIsNull),
                ApplicantCount.load(db2, _param, "2", wish, coursecdIsNull),
                ApplicantCount.load(db2, _param, null, wish, null)
        };
        for (int i = 0; i < counts.length; i++) {
            printnCount(svf, k, i + 1, counts[i]);
        }
    }

    private void printGB(final DB2UDB db2, final Vrw32alp svf, final String k) {
        final String[] wish = {coursecd3};
        svf.VrsOut("EXAMCOURSE_NAME" + k, _param._examcourseName3); // 受験コース
        final ApplicantCount[] counts = new ApplicantCount [] {
                ApplicantCount.load(db2, _param, "1", wish, coursecd3),
                ApplicantCount.load(db2, _param, "2", wish, coursecd3),
                ApplicantCount.load(db2, _param, "1", wish, coursecd4),
                ApplicantCount.load(db2, _param, "2", wish, coursecd4),
                ApplicantCount.load(db2, _param, "1", wish, coursecdIsNull),
                ApplicantCount.load(db2, _param, "2", wish, coursecdIsNull),
                ApplicantCount.load(db2, _param, null, wish, null)
        };
        for (int i = 0; i < counts.length; i++) {
            printnCount(svf, k, i + 1, counts[i]);
        }
    }

    private void printGC(final DB2UDB db2, final Vrw32alp svf, final String k) {
        final String[] wish = {coursecd4};
        svf.VrsOut("EXAMCOURSE_NAME" + k, _param._examcourseName4); // 受験コース
        final ApplicantCount[] counts = new ApplicantCount [] {
                ApplicantCount.load(db2, _param, "1", wish, coursecd4),
                ApplicantCount.load(db2, _param, "2", wish, coursecd4),
                ApplicantCount.load(db2, _param, "1", wish, coursecdIsNull),
                ApplicantCount.load(db2, _param, "2", wish, coursecdIsNull),
                ApplicantCount.load(db2, _param, null, wish, null)
        };
        for (int i = 0; i < counts.length; i++) {
            printnCount(svf, k, i + 1, counts[i]);
        }
    }

    private void printnCount(final Vrw32alp svf, final String k, final int gyo, final ApplicantCount c) {
        if (null == c) {
            return;
        }
        svf.VrsOutn("APPLICANT" + k, gyo, c._shigansha); // 志願者
        svf.VrsOutn("SELECT_EXAM_PASS" + k, gyo, c._senbatu1Goukaku); // 選抜I合格数
        svf.VrsOutn("FINAL_APPLICANT" + k, gyo, c._saishuShigansha); // 最終志願者数
        svf.VrsOutn("ABSENCE" + k, gyo, c._kesseki); // 欠席
        svf.VrsOutn("EXAMINEE" + k, gyo, c._jukensha); // 受験者
        svf.VrsOutn("PASS" + k + "_1", gyo, c._goukaku1); // 合格者
        svf.VrsOutn("PASS" + k + "_2", gyo, c._goukaku2); // 合格者
        svf.VrsOutn("PASS" + k + "_3", gyo, c._goukaku3); // 合格者
        svf.VrsOutn("PASS" + k + "_4", gyo, c._goukaku4); // 合格者
        svf.VrsOutn("PASS" + k, gyo, c._goukakuTotal); // 合格者合計
        svf.VrsOutn("FAIL" + k, gyo, c._fugoukaku); // 不合格
        svf.VrsOutn("ENTRANCE" + k + "_1", gyo, c._nyugaku1); // 入学手続き数
        svf.VrsOutn("ENTRANCE" + k + "_2", gyo, c._nyugaku2); // 入学手続き数
        svf.VrsOutn("ENTRANCE" + k + "_3", gyo, c._nyugaku3); // 入学手続き数
        svf.VrsOutn("ENTRANCE" + k + "_4", gyo, c._nyugaku4); // 入学手続き数
    }

    private void printCount(final Vrw32alp svf, final String pfx, final ApplicantCount c) {
        if (null == c) {
            return;
        }
        svf.VrsOut(pfx + "TOTAL_APPLICANT", c._shigansha); // 志願者
        svf.VrsOut(pfx + "TOTAL_SELECT_EXAM_PASS", c._senbatu1Goukaku); // 選抜I合格数
        svf.VrsOut(pfx + "TOTAL_FINAL_APPLICANT", c._saishuShigansha); // 最終志願者数
        svf.VrsOut(pfx + "TOTAL_ABSENCE", c._kesseki); // 欠席
        svf.VrsOut(pfx + "TOTAL_EXAMINEE", c._jukensha); // 受験者
        svf.VrsOut(pfx + "TOTAL_PASS1", c._goukaku1); // 合格者
        svf.VrsOut(pfx + "TOTAL_PASS2", c._goukaku2); // 合格者
        svf.VrsOut(pfx + "TOTAL_PASS3", c._goukaku3); // 合格者
        svf.VrsOut(pfx + "TOTAL_PASS4", c._goukaku4); // 合格者
        svf.VrsOut(pfx + "TOTAL_PASS", c._goukakuTotal); // 合格者合計
        svf.VrsOut(pfx + "TOTAL_FAIL", c._fugoukaku); // 不合格
        svf.VrsOut(pfx + "TOTAL_ENTRANCE1", c._nyugaku1); // 入学手続き数
        svf.VrsOut(pfx + "TOTAL_ENTRANCE2", c._nyugaku2); // 入学手続き数
        svf.VrsOut(pfx + "TOTAL_ENTRANCE3", c._nyugaku3); // 入学手続き数
        svf.VrsOut(pfx + "TOTAL_ENTRANCE4", c._nyugaku4); // 入学手続き数
    }

    private static class ApplicantCount {
        final String _shigansha;
        final String _senbatu1Goukaku;
        final String _saishuShigansha;
        final String _kesseki;
        final String _jukensha;
        final String _goukaku1;
        final String _goukaku2;
        final String _goukaku3;
        final String _goukaku4;
        final String _goukakuTotal;
        final String _fugoukaku;
        final String _nyugaku1;
        final String _nyugaku2;
        final String _nyugaku3;
        final String _nyugaku4;

        ApplicantCount(
            final String shigansha,
            final String senbatu1Goukaku,
            final String saishuShigansha,
            final String kesseki,
            final String jukensha,
            final String goukaku1,
            final String goukaku2,
            final String goukaku3,
            final String goukaku4,
            final String goukakuTotal,
            final String fugoukaku,
            final String nyugaku1,
            final String nyugaku2,
            final String nyugaku3,
            final String nyugaku4
        ) {
            _shigansha = shigansha;
            _senbatu1Goukaku = senbatu1Goukaku;
            _saishuShigansha = saishuShigansha;
            _kesseki = kesseki;
            _jukensha = jukensha;
            _goukaku1 = goukaku1;
            _goukaku2 = goukaku2;
            _goukaku3 = goukaku3;
            _goukaku4 = goukaku4;
            _goukakuTotal = goukakuTotal;
            _fugoukaku = fugoukaku;
            _nyugaku1 = nyugaku1;
            _nyugaku2 = nyugaku2;
            _nyugaku3 = nyugaku3;
            _nyugaku4 = nyugaku4;
        }

        private static String zeroToBlank(final String s) {
            return (NumberUtils.isDigits(s) && Integer.parseInt(s) == 0) ? null : s;
        }

        public static ApplicantCount load(final DB2UDB db2, final Param param, final String sex, final String[] wishExamcoursecd, final String befExamcoursecd) {
            ApplicantCount applicantcount = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = sql(param, sex, wishExamcoursecd, befExamcoursecd);
                log.debug(" " + sex + ", " + ArrayUtils.toString(wishExamcoursecd) + ", " + befExamcoursecd + " sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String shigansha = zeroToBlank(rs.getString("SHIGANSHA"));
                    final String senbatu1Goukaku = zeroToBlank(rs.getString("SENBATU1_GOUKAKU"));
                    final String saishuShigansha = zeroToBlank(rs.getString("SAISHU_SHIGANSHA"));
                    final String kesseki = zeroToBlank(rs.getString("KESSEKI"));
                    final String jukensha = zeroToBlank(rs.getString("JUKENSHA"));
                    final String goukaku1 = zeroToBlank(rs.getString("GOUKAKU_1"));
                    final String goukaku2 = zeroToBlank(rs.getString("GOUKAKU_2"));
                    final String goukaku3 = zeroToBlank(rs.getString("GOUKAKU_3"));
                    final String goukaku4 = zeroToBlank(rs.getString("GOUKAKU_4"));
                    final String goukakuTotal = zeroToBlank(rs.getString("GOUKAKU_TOTAL"));
                    final String fugoukaku = zeroToBlank(rs.getString("FUGOUKAKU"));
                    final String nyugaku1 = zeroToBlank(rs.getString("NYUGAKU_1"));
                    final String nyugaku2 = zeroToBlank(rs.getString("NYUGAKU_2"));
                    final String nyugaku3 = zeroToBlank(rs.getString("NYUGAKU_3"));
                    final String nyugaku4 = zeroToBlank(rs.getString("NYUGAKU_4"));
                    applicantcount = new ApplicantCount(shigansha, senbatu1Goukaku, saishuShigansha, kesseki, jukensha, goukaku1, goukaku2, goukaku3, goukaku4, goukakuTotal, fugoukaku, nyugaku1, nyugaku2, nyugaku3, nyugaku4);
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return applicantcount;
        }

        public static String sql(final Param param, final String sex, final String[] wishExamcoursecd, final String befExamcoursecd) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH APPLICANT AS ( ");
            stb.append("     SELECT ");
            stb.append("         BASE.EXAMNO, ");
            stb.append("         BEFC.EXAMCOURSECD AS BEF_EXAMCOURSECD, ");
            stb.append("         W1C.EXAMCOURSECD AS EXAMCOURSECD1, ");
            stb.append("         W2C.EXAMCOURSECD AS EXAMCOURSECD2, ");
            stb.append("         W3C.EXAMCOURSECD AS EXAMCOURSECD3, ");
            stb.append("         BASE.SEX, ");
            stb.append("         BASE.JUDGEMENT, ");
            stb.append("         NML013.NAMESPARE1 AS NML013NAMESPARE1, ");
            stb.append("         SUCC.EXAMCOURSECD AS SUC_EXAMCOURSECD, ");
            stb.append("         BASE.ENTDIV ");
            stb.append("     FROM ");
            stb.append("         ENTEXAM_APPLICANTBASE_DAT BASE ");
            stb.append("         LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT T2 ON T2.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
            stb.append("             AND T2.APPLICANTDIV = BASE.APPLICANTDIV ");
            stb.append("             AND T2.EXAMNO       = BASE.EXAMNO ");
            stb.append("             AND T2.SEQ          = '002' ");
            stb.append("         LEFT JOIN ENTEXAM_COURSE_MST SUCC ON SUCC.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
            stb.append("             AND SUCC.APPLICANTDIV = BASE.APPLICANTDIV ");
            stb.append("             AND SUCC.TESTDIV = BASE.TESTDIV ");
            stb.append("             AND SUCC.COURSECD = BASE.SUC_COURSECD ");
            stb.append("             AND SUCC.MAJORCD = BASE.SUC_MAJORCD ");
            stb.append("             AND SUCC.EXAMCOURSECD = BASE.SUC_COURSECODE ");
            stb.append("         LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT DETAIL ON DETAIL.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
            stb.append("             AND DETAIL.APPLICANTDIV = BASE.APPLICANTDIV ");
            stb.append("             AND DETAIL.EXAMNO       = BASE.EXAMNO ");
            stb.append("             AND DETAIL.SEQ          = '002' ");
            stb.append("         LEFT JOIN ENTEXAM_APPLICANT_BEFORE_DAT BEF ON BEF.ENTEXAMYEAR = BASE.ENTEXAMYEAR  ");
            stb.append("             AND BEF.APPLICANTDIV = BASE.APPLICANTDIV  ");
            stb.append("             AND BEF.TESTDIV = BASE.TESTDIV  ");
            stb.append("             AND BEF.BEFORE_PAGE = DETAIL.REMARK1  ");
            stb.append("             AND BEF.BEFORE_SEQ = DETAIL.REMARK2  ");
            stb.append("         LEFT JOIN ENTEXAM_COURSE_MST BEFC ON BEFC.ENTEXAMYEAR = BASE.ENTEXAMYEAR  ");
            stb.append("             AND BEFC.APPLICANTDIV = BASE.APPLICANTDIV  ");
            stb.append("             AND BEFC.TESTDIV = BASE.TESTDIV  ");
            stb.append("             AND BEFC.COURSECD = BEF.BEFORE_COURSECD  ");
            stb.append("             AND BEFC.MAJORCD = BEF.BEFORE_MAJORCD  ");
            stb.append("             AND BEFC.EXAMCOURSECD = BEF.BEFORE_EXAMCOURSECD  ");
            stb.append("         INNER JOIN ENTEXAM_WISHDIV_MST W1 ON W1.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
            stb.append("             AND W1.APPLICANTDIV = BASE.APPLICANTDIV ");
            stb.append("             AND W1.TESTDIV = BASE.TESTDIV ");
            stb.append("             AND W1.DESIREDIV = BASE.DESIREDIV ");
            stb.append("             AND W1.WISHNO = '1' ");
            stb.append("         INNER JOIN ENTEXAM_COURSE_MST W1C ON W1C.ENTEXAMYEAR = W1.ENTEXAMYEAR ");
            stb.append("             AND W1C.APPLICANTDIV = W1.APPLICANTDIV ");
            stb.append("             AND W1C.TESTDIV = W1.TESTDIV ");
            stb.append("             AND W1C.COURSECD = W1.COURSECD ");
            stb.append("             AND W1C.MAJORCD = W1.MAJORCD ");
            stb.append("             AND W1C.EXAMCOURSECD = W1.EXAMCOURSECD ");
            stb.append("         LEFT JOIN ENTEXAM_WISHDIV_MST W2 ON W2.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
            stb.append("             AND W2.APPLICANTDIV = BASE.APPLICANTDIV ");
            stb.append("             AND W2.TESTDIV = BASE.TESTDIV ");
            stb.append("             AND W2.DESIREDIV = BASE.DESIREDIV ");
            stb.append("             AND W2.WISHNO = '2' ");
            stb.append("         LEFT JOIN ENTEXAM_COURSE_MST W2C ON W2C.ENTEXAMYEAR = W2.ENTEXAMYEAR ");
            stb.append("             AND W2C.APPLICANTDIV = W2.APPLICANTDIV ");
            stb.append("             AND W2C.TESTDIV = W2.TESTDIV ");
            stb.append("             AND W2C.COURSECD = W2.COURSECD ");
            stb.append("             AND W2C.MAJORCD = W2.MAJORCD ");
            stb.append("             AND W2C.EXAMCOURSECD = W2.EXAMCOURSECD ");
            stb.append("         LEFT JOIN ENTEXAM_WISHDIV_MST W3 ON W3.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
            stb.append("             AND W3.APPLICANTDIV = BASE.APPLICANTDIV ");
            stb.append("             AND W3.TESTDIV = BASE.TESTDIV ");
            stb.append("             AND W3.DESIREDIV = BASE.DESIREDIV ");
            stb.append("             AND W3.WISHNO = '3' ");
            stb.append("         LEFT JOIN ENTEXAM_COURSE_MST W3C ON W3C.ENTEXAMYEAR = W3.ENTEXAMYEAR ");
            stb.append("             AND W3C.APPLICANTDIV = W3.APPLICANTDIV ");
            stb.append("             AND W3C.TESTDIV = W3.TESTDIV ");
            stb.append("             AND W3C.COURSECD = W3.COURSECD ");
            stb.append("             AND W3C.MAJORCD = W3.MAJORCD ");
            stb.append("             AND W3C.EXAMCOURSECD = W3.EXAMCOURSECD ");
            stb.append("         LEFT JOIN NAME_MST NML013 ON NML013.NAMECD1 = 'L013' ");
            stb.append("             AND NML013.NAMECD2 = BASE.JUDGEMENT ");
            stb.append("     WHERE ");
            stb.append("         BASE.ENTEXAMYEAR = '" + param._entexamyear + "' ");
            stb.append("         AND BASE.APPLICANTDIV = '" + param._applicantdiv + "' ");
            stb.append("         AND BASE.TESTDIV = '" + param._testdiv + "' ");
            if (null != sex) {
                stb.append("         AND BASE.SEX = '" + sex + "' ");
            }
            if (null != wishExamcoursecd) {
                stb.append("         AND W1C.EXAMCOURSECD IN " + SQLUtils.whereIn(true, wishExamcoursecd) + " ");
            }
            if (coursecdIsNull.equals(befExamcoursecd)) {
                stb.append("         AND BEFC.EXAMCOURSECD IS NULL ");
            } else if (null != befExamcoursecd) {
                stb.append("         AND BEFC.EXAMCOURSECD = '" + befExamcoursecd + "' ");
            }
            stb.append(" )  ");
            stb.append(" SELECT ");
            stb.append("     COUNT(*) AS SHIGANSHA, ");
            stb.append("     SUM(CASE WHEN T1.JUDGEMENT = '4' THEN 1 END) AS SENBATU1_GOUKAKU, ");
            stb.append("     COUNT(*) - ");
            stb.append("     SUM(CASE WHEN T1.JUDGEMENT = '4' THEN 1 ELSE 0 END) AS SAISHU_SHIGANSHA, ");
            stb.append("     SUM(CASE WHEN T1.JUDGEMENT = '3' THEN 1 END) AS KESSEKI, ");
            stb.append("     COUNT(*) ");
            stb.append("     - SUM(CASE WHEN T1.JUDGEMENT = '4' THEN 1 ELSE 0 END) ");
            stb.append("     - SUM(CASE WHEN T1.JUDGEMENT = '3' THEN 1 ELSE 0 END) AS JUKENSHA, ");
            stb.append("     SUM(CASE WHEN T1.JUDGEMENT IS NOT NULL AND T1.SUC_EXAMCOURSECD = '" + coursecd1 + "' THEN 1 END) AS GOUKAKU_1, ");
            stb.append("     SUM(CASE WHEN T1.JUDGEMENT IS NOT NULL AND T1.SUC_EXAMCOURSECD = '" + coursecd2 + "' THEN 1 END) AS GOUKAKU_2, ");
            stb.append("     SUM(CASE WHEN T1.JUDGEMENT IS NOT NULL AND T1.SUC_EXAMCOURSECD = '" + coursecd3 + "' THEN 1 END) AS GOUKAKU_3, ");
            stb.append("     SUM(CASE WHEN T1.JUDGEMENT IS NOT NULL AND T1.SUC_EXAMCOURSECD = '" + coursecd4 + "' THEN 1 END) AS GOUKAKU_4, ");
            stb.append("     SUM(CASE WHEN T1.JUDGEMENT IS NOT NULL AND T1.SUC_EXAMCOURSECD IN  ('" + coursecd1 + "', '" + coursecd2 + "', '" + coursecd3 + "', '" + coursecd4 + "') THEN 1 END) AS GOUKAKU_TOTAL, ");
            stb.append("     SUM(CASE WHEN T1.JUDGEMENT IS NOT NULL AND VALUE(NML013NAMESPARE1, '') <> '1' THEN 1 END) ");
            stb.append("     - SUM(CASE WHEN T1.JUDGEMENT = '4' THEN 1 ELSE 0 END) ");
            stb.append("     - SUM(CASE WHEN T1.JUDGEMENT = '3' THEN 1 ELSE 0 END) AS FUGOUKAKU, ");
            stb.append("     SUM(CASE WHEN T1.JUDGEMENT IS NOT NULL AND T1.SUC_EXAMCOURSECD = '" + coursecd1 + "' AND T1.ENTDIV = '1' THEN 1 END) AS NYUGAKU_1, ");
            stb.append("     SUM(CASE WHEN T1.JUDGEMENT IS NOT NULL AND T1.SUC_EXAMCOURSECD = '" + coursecd2 + "' AND T1.ENTDIV = '1' THEN 1 END) AS NYUGAKU_2, ");
            stb.append("     SUM(CASE WHEN T1.JUDGEMENT IS NOT NULL AND T1.SUC_EXAMCOURSECD = '" + coursecd3 + "' AND T1.ENTDIV = '1' THEN 1 END) AS NYUGAKU_3, ");
            stb.append("     SUM(CASE WHEN T1.JUDGEMENT IS NOT NULL AND T1.SUC_EXAMCOURSECD = '" + coursecd4 + "' AND T1.ENTDIV = '1' THEN 1 END) AS NYUGAKU_4 ");
            stb.append(" FROM ");
            stb.append("     APPLICANT T1 ");
            return stb.toString();
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 72044 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _entexamyear;
        final String _applicantdiv;
        final String _testdiv;
        final String _date;

        final String _applicantdivName;
        final String _testdivAbbv1;
        final String _dateStr;
        final String _examcourseName1;
        final String _examcourseName2;
        final String _examcourseName3;
        final String _examcourseName4;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _applicantdiv = request.getParameter("APPLICANTDIV");
            _testdiv = request.getParameter("TESTDIV");
            _date = request.getParameter("CTRL_DATE");
            _dateStr = getDateStr(db2, _date);

            _applicantdivName = getNameMst(db2, "NAME1", "L003", _applicantdiv);
            _testdivAbbv1 = getNameMst(db2, "ABBV1", "L004", _testdiv);
            _examcourseName1 = getExamCourseName(db2, coursecd1, true);
            _examcourseName2 = getExamCourseName(db2, coursecd2, true);
            _examcourseName3 = getExamCourseName(db2, coursecd3, true);
            _examcourseName4 = getExamCourseName(db2, coursecd4, true);
        }

        private String getDateStr(final DB2UDB db2, final String date) {
            final Calendar cal = Calendar.getInstance();
            final DecimalFormat df = new DecimalFormat("00");
            final int hour = cal.get(Calendar.HOUR_OF_DAY);
            final int minute = cal.get(Calendar.MINUTE);
            return KNJ_EditDate.h_format_JP(db2, date) + "　" + df.format(hour) + "時" + df.format(minute) + "分現在";
        }

        private String getExamCourseName(final DB2UDB db2, final String examcoursecd, final boolean addMajorName) {
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer sql = new StringBuffer();
                sql.append(" SELECT ");
                if (addMajorName) {
                    sql.append("    VALUE(T2.MAJORNAME, '') || VALUE(T1.EXAMCOURSE_NAME, '') AS NAME ");
                } else {
                    sql.append("    VALUE(T1.EXAMCOURSE_NAME, '') AS NAME ");
                }
                sql.append(" FROM ENTEXAM_COURSE_MST T1 ");
                if (addMajorName) {
                    sql.append(" INNER JOIN MAJOR_MST T2 ON T2.COURSECD = T1.COURSECD ");
                    sql.append("     AND T2.MAJORCD = T1.MAJORCD ");
                }
                sql.append(" WHERE T1.ENTEXAMYEAR = '" + _entexamyear + "' ");
                sql.append("   AND T1.APPLICANTDIV = '" + _applicantdiv + "' ");
                sql.append("   AND T1.TESTDIV = '" + _testdiv + "' ");
                sql.append("   AND T1.EXAMCOURSECD = '" + examcoursecd + "' ");
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString("NAME");
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
    }
}

// eof

