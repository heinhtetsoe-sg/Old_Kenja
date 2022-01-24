/*
 * $Id: 188261cf9bd7d9b3fe4eb9c7ee5ab22fb6a16737 $
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

/**
 *  学校教育システム 賢者 [入試管理]
 *
 *                  ＜ＫＮＪＬ３２７Ｒ＞  各種帳票（個人宛）
 **/
public class KNJL327R {

    private static final Log log = LogFactory.getLog(KNJL327R.class);

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
        final List list = Applicant.load(db2, _param);
        for (int line = 0; line < list.size(); line++) {
            final Applicant appl = (Applicant) list.get(line);
            if ("1".equals(_param._form)) {
                print1(svf, appl);
            } else if ("2".equals(_param._form)) {
                print2(svf, appl);
            }
        }
    }

    public void print1(final Vrw32alp svf, final Applicant appl) {
        final boolean isGoukaku = "1".equals(appl._judgement) || "8".equals(appl._judgement); // 8:難関補欠合格

        final String form;
        if ("4".equals(_param._output1)) { // 難関コース補欠合格通知書
            form = "KNJL327R_1_2.frm";
        } else if (isGoukaku) {
            form = "KNJL327R_1.frm";
        } else {
            form = "KNJL327R_2.frm";
        }
        svf.VrSetForm(form, 1);
        svf.VrsOut("DATE", _param._noticedateStr); // 日付
        svf.VrsOut("EXAM_NO", appl._examno); // 受験番号
        svf.VrsOut("SUBJECT", isGoukaku ? appl._sucMajorname : appl._examMajorname); // 学科
        svf.VrsOut("NAME", appl._name); // 氏名

        if ("4".equals(_param._output1)) {
            // 難関コース補欠合格
            svf.VrsOut("PASS_COURSE", appl._gsCourseName); // 難関コース
        } else if (isGoukaku) {
            // 合格
            svf.VrsOut("PASS_COURSE", appl._sucCourseName); // 合格コース
        } else {
            // 不合格
            svf.VrsOut("TEST_KIND", _param._testdivAbbv1); // 試験種別
            svf.VrsOut("COURSE1", appl._examCoursename1); // コース
            svf.VrsOut("COURSE2", appl._examCoursename2); // コース
            svf.VrsOut("COURSE3", appl._examCoursename3); // コース
            svf.VrsOut("COURSE4", appl._examCoursename4); // コース
        }
        svf.VrsOut("SCHOOL_NAME", _param._schoolName); // 学校名
        svf.VrsOut("JOB_NAME", _param._jobName); // 役職名
        svf.VrsOut("PRINCIPAL_NAME", "　" + _param._principalName); // 校長名

        if (null != _param._imageFile) {
            svf.VrsOut("STAMP", _param._imageFile.toString());
        }
        svf.VrEndPage();
        _hasData = true;
    }

    public void print2(final Vrw32alp svf, final Applicant appl) {
        final String FORM1 = "KNJL327R_3.frm";
        final String FORM2 = "KNJL327R_4.frm";
        final String FORM3 = "KNJL327R_5.frm";
        final String FORM4 = "KNJL327R_6.frm";
        final String form;
        if ("1".equals(_param._testdiv)) {
            if ("1".equals(appl._judgeKind)) {
                form = FORM1;
            } else { // if ("2".equals(appl._judgeKind)) {
                form = FORM2;
            }
        } else { // if ("2".equals(_param._testdiv)) {
            if ("1".equals(appl._judgeKind)) {
                form = FORM3;
            } else { // if ("2".equals(appl._judgeKind)) {
                form = FORM4;
            }
        }
        svf.VrSetForm(form, 1);

        svf.VrsOut("DATE", _param._noticedateStr); // 日付
        if (FORM3.equals(form)) {
            svf.VrsOut("TRACKREC", "（" + _param._sakunendo + "年度実績）"); // 授業料実績
        }
        svf.VrsOut("EXAM_NO", appl._examno); // 受験番号
        svf.VrsOut("SUBJECT", appl._sucMajorname); // 学科
        svf.VrsOut("NAME", appl._name); // 氏名
        svf.VrsOut("COURSE", appl._sucCourseName); // コース

        svf.VrsOut("ENTMONEY1", "７０，０００円"); // 入学手続き金
        if (FORM3.equals(form)) {
            svf.VrsOut("ENTMONEY2", "１４０，０００円"); // 入学金
        } else {
            svf.VrsOut("ENTMONEY2", "１６０，０００円"); // 入学金
        }
        svf.VrsOut("CLUBMONEY", "１２，０００円"); // クラブ活動強化費
        if (FORM3.equals(form)) {
            svf.VrsOut("TUITIONMONEY", "３３，３００円"); // 授業料
            svf.VrsOut("TRAININGMONEY", "２００円"); // 実験実習費
            svf.VrsOut("AIRCONMONEY", "７００円"); // 冷暖房費
        } else if (FORM1.equals(form) || FORM2.equals(form) || FORM4.equals(form)) {
            svf.VrsOut("TUITIONMONEY", "３８，０００円"); // 授業料
        } else {
            svf.VrsOut("TUITIONMONEY", "２８，０００円"); // 授業料
            svf.VrsOut("FACILITYMONEY", "７，８００円"); // 施設設備費
        }
        svf.VrsOut("CLASS_MONEY", "５０，０００円"); // 学級費
        svf.VrsOut("STUDENT_MONEY", "１，０００円"); // 生徒会費
        svf.VrsOut("PTA_MONEY", "４００円"); // PTA会費
        svf.VrsOut("TRIP_MONEY", "８，０００円"); // 修学旅行費積立金
        svf.VrsOut("TRIP_MONEY_LIMIT", "（１年次２月まで）"); // 修学旅行費積立金期限
        svf.VrsOut("TRIP_MONEY2", "７，０００円"); // 修学旅行費積立金2
        svf.VrsOut("TRIP_MONEY_LIMIT2", "（１年次３月～２年次８月まで）"); // 修学旅行費積立金期限2

        if (FORM1.equals(form) || FORM2.equals(form) || FORM4.equals(form)) {
            svf.VrsOut("ICT_MONEY", "２，７００円"); // ＩＣＴ教育環境整備費
        }

        svf.VrsOut("SCHOOL_NAME", _param._schoolName); // 学校名
        svf.VrsOut("JOB_NAME", "校長"); // 役職名
        svf.VrsOut("PRINCIPAL_NAME", " " + _param._principalName); // 校長名

        if (null != _param._imageFile) {
            svf.VrsOut("STAMP", _param._imageFile.toString());
        }

        if ("0001".equals(appl._sucCoursecode) || "0002".equals(appl._sucCoursecode)) { //難関コースと特進コース
            svf.VrsOut("BUN1", "《お願い》");
            svf.VrsOut("BUN2", "　本校の奨学生の就学支援金等につきまして、あらかじめ次の事項をご了承くださいますようお願いいたします。");
            svf.VrsOut("BUN3", "　本校では奨学にあたり本校所定の納入すべき額から「国の就学支援金」並びに「広島県の授業料軽減補助」の額を差し引いた");
            svf.VrsOut("BUN4", "後の額に対して奨学補助割合を乗じた額（100円未満の端数切り捨て）により奨学しますので、必ず就学支援金等の交付申請を");
            svf.VrsOut("BUN5", "お願いいたします。");
            svf.VrsOut("BUN6", "　このことにより、授業料等にかかわる奨学する額及び奨学後に納入する授業料等の額は、就学支援金等の交付決定を待って確定");
            svf.VrsOut("BUN7", "することになります。");
        }
        svf.VrEndPage();
        _hasData = true;
    }

    private static class Applicant {
        final String _examno;
        final String _name;
        final String _judgement;
        final String _judgementName;
        final String _sucMajorname;
        final String _sucCoursecode;
        final String _sucCourseName;
        final String _gsCourseName;
        final String _examMajorname;
        final String _examCoursename1;
        final String _examCoursename2;
        final String _examCoursename3;
        final String _examCoursename4;
        final String _judgeKind;
        final String _judgeKindName;

        Applicant(
            final String examno,
            final String name,
            final String judgement,
            final String judgementName,
            final String sucMajorname,
            final String sucCoursecode,
            final String sucCourseName,
            final String gsCourseName,
            final String examMajorname,
            final String examCoursename1,
            final String examCoursename2,
            final String examCoursename3,
            final String examCoursename4,
            final String judgeKind,
            final String judgeKindName
        ) {
            _examno = examno;
            _name = name;
            _judgement = judgement;
            _judgementName = judgementName;
            _sucMajorname = sucMajorname;
            _sucCoursecode = sucCoursecode;
            _sucCourseName = sucCourseName;
            _gsCourseName = gsCourseName;
            _examMajorname = examMajorname;
            _examCoursename1 = examCoursename1;
            _examCoursename2 = examCoursename2;
            _examCoursename3 = examCoursename3;
            _examCoursename4 = examCoursename4;
            _judgeKind = judgeKind;
            _judgeKindName = judgeKindName;
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
                    final String examno = rs.getString("EXAMNO");
                    final String name = rs.getString("NAME");
                    final String judgement = rs.getString("JUDGEMENT");
                    final String judgementName = rs.getString("JUDGEMENT_NAME");
                    final String sucMajorname = rs.getString("SUC_MAJORNAME");
                    final String sucCoursecode = rs.getString("SUC_COURSECODE");
                    final String sucCourseName = rs.getString("SUC_COURSE_NAME");
                    final String gsCourseName = rs.getString("GS_COURSE_NAME");
                    final String examMajorname = rs.getString("EXAM_MAJORNAME");
                    final String examCoursename1 = rs.getString("EXAM_COURSENAME1");
                    final String examCoursename2 = rs.getString("EXAM_COURSENAME2");
                    final String examCoursename3 = rs.getString("EXAM_COURSENAME3");
                    final String examCoursename4 = rs.getString("EXAM_COURSENAME4");
                    final String judgeKind = rs.getString("JUDGE_KIND");
                    final String judgeKindName = rs.getString("JUDGE_KIND_NAME");
                    final Applicant applicant = new Applicant(examno, name, judgement, judgementName, sucMajorname, sucCoursecode, sucCourseName, gsCourseName, examMajorname, examCoursename1, examCoursename2, examCoursename3, examCoursename4, judgeKind, judgeKindName);
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
            stb.append("     BASE.EXAMNO, ");
            stb.append("     BASE.NAME, ");
            stb.append("     BASE.JUDGEMENT, ");
            stb.append("     CASE WHEN BASE.JUDGEMENT IS NOT NULL THEN ");
            stb.append("         CASE WHEN NML013.NAMESPARE1 = '1' THEN '合格' ELSE '不合格' END ");
            stb.append("     END AS JUDGEMENT_NAME, ");
            stb.append("     CASE WHEN NML013.NAMESPARE1 = '1' THEN T6.MAJORNAME END AS SUC_MAJORNAME, ");
            stb.append("     CASE WHEN NML013.NAMESPARE1 = '1' THEN BASE.SUC_COURSECODE END AS SUC_COURSECODE, ");
            stb.append("     CASE WHEN NML013.NAMESPARE1 = '1' THEN T7.EXAMCOURSE_NAME END AS SUC_COURSE_NAME, ");
            stb.append("     CASE WHEN NML013.NAMESPARE1 = '1' AND BASE.JUDGEMENT = '8' THEN GS.EXAMCOURSE_NAME END AS GS_COURSE_NAME, ");
            stb.append("     CASE WHEN BASE.JUDGEMENT IS NOT NULL AND VALUE(NML013.NAMESPARE1, '') <> '1' THEN T6F.MAJORNAME END AS EXAM_MAJORNAME, ");
            stb.append("     CASE WHEN BASE.JUDGEMENT IS NOT NULL AND VALUE(NML013.NAMESPARE1, '') <> '1' THEN T3C.EXAMCOURSE_NAME END AS EXAM_COURSENAME1, ");
            stb.append("     CASE WHEN BASE.JUDGEMENT IS NOT NULL AND VALUE(NML013.NAMESPARE1, '') <> '1' THEN T4C.EXAMCOURSE_NAME END AS EXAM_COURSENAME2, ");
            stb.append("     CASE WHEN BASE.JUDGEMENT IS NOT NULL AND VALUE(NML013.NAMESPARE1, '') <> '1' THEN T5C.EXAMCOURSE_NAME END AS EXAM_COURSENAME3, ");
            stb.append("     CASE WHEN BASE.JUDGEMENT IS NOT NULL AND VALUE(NML013.NAMESPARE1, '') <> '1' THEN T6C.EXAMCOURSE_NAME END AS EXAM_COURSENAME4, ");
            stb.append("     BASE.JUDGE_KIND, ");
            stb.append("     NML031.NAME1 AS JUDGE_KIND_NAME ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_APPLICANTBASE_DAT BASE ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD040 ON BD040.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
            stb.append("         AND BD040.APPLICANTDIV = BASE.APPLICANTDIV ");
            stb.append("         AND BD040.EXAMNO = BASE.EXAMNO ");
            stb.append("         AND BD040.SEQ = '040' ");
            stb.append("     LEFT JOIN ENTEXAM_COURSE_MST GS ON GS.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
            stb.append("         AND GS.APPLICANTDIV = BASE.APPLICANTDIV ");
            stb.append("         AND GS.TESTDIV = BASE.TESTDIV ");
            stb.append("         AND GS.COURSECD = BD040.REMARK1 ");
            stb.append("         AND GS.MAJORCD = BD040.REMARK2 ");
            stb.append("         AND GS.EXAMCOURSECD = BD040.REMARK3 ");
            stb.append("     LEFT JOIN ENTEXAM_WISHDIV_MST T3 ON T3.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
            stb.append("         AND T3.APPLICANTDIV = BASE.APPLICANTDIV ");
            stb.append("         AND T3.TESTDIV = BASE.TESTDIV ");
            stb.append("         AND T3.DESIREDIV = BASE.DESIREDIV ");
            stb.append("         AND T3.WISHNO = '1' ");
            stb.append("     LEFT JOIN ENTEXAM_COURSE_MST T3C ON T3C.ENTEXAMYEAR = T3.ENTEXAMYEAR ");
            stb.append("         AND T3C.APPLICANTDIV = T3.APPLICANTDIV ");
            stb.append("         AND T3C.TESTDIV = T3.TESTDIV ");
            stb.append("         AND T3C.COURSECD = T3.COURSECD ");
            stb.append("         AND T3C.MAJORCD = T3.MAJORCD ");
            stb.append("         AND T3C.EXAMCOURSECD = T3.EXAMCOURSECD ");
            stb.append("     LEFT JOIN ENTEXAM_WISHDIV_MST T4 ON T4.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
            stb.append("         AND T4.APPLICANTDIV = BASE.APPLICANTDIV ");
            stb.append("         AND T4.TESTDIV = BASE.TESTDIV ");
            stb.append("         AND T4.DESIREDIV = BASE.DESIREDIV ");
            stb.append("         AND T4.WISHNO = '2' ");
            stb.append("     LEFT JOIN ENTEXAM_COURSE_MST T4C ON T4C.ENTEXAMYEAR = T4.ENTEXAMYEAR ");
            stb.append("         AND T4C.APPLICANTDIV = T4.APPLICANTDIV ");
            stb.append("         AND T4C.TESTDIV = T4.TESTDIV ");
            stb.append("         AND T4C.COURSECD = T4.COURSECD ");
            stb.append("         AND T4C.MAJORCD = T4.MAJORCD ");
            stb.append("         AND T4C.EXAMCOURSECD = T4.EXAMCOURSECD ");
            stb.append("     LEFT JOIN ENTEXAM_WISHDIV_MST T5 ON T5.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
            stb.append("         AND T5.APPLICANTDIV = BASE.APPLICANTDIV ");
            stb.append("         AND T5.TESTDIV = BASE.TESTDIV ");
            stb.append("         AND T5.DESIREDIV = BASE.DESIREDIV ");
            stb.append("         AND T5.WISHNO = '3' ");
            stb.append("     LEFT JOIN ENTEXAM_COURSE_MST T5C ON T5C.ENTEXAMYEAR = T5.ENTEXAMYEAR ");
            stb.append("         AND T5C.APPLICANTDIV = T5.APPLICANTDIV ");
            stb.append("         AND T5C.TESTDIV = T5.TESTDIV ");
            stb.append("         AND T5C.COURSECD = T5.COURSECD ");
            stb.append("         AND T5C.MAJORCD = T5.MAJORCD ");
            stb.append("         AND T5C.EXAMCOURSECD = T5.EXAMCOURSECD ");
            stb.append("     LEFT JOIN ENTEXAM_WISHDIV_MST W4 ON W4.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
            stb.append("         AND W4.APPLICANTDIV = BASE.APPLICANTDIV ");
            stb.append("         AND W4.TESTDIV = BASE.TESTDIV ");
            stb.append("         AND W4.DESIREDIV = BASE.DESIREDIV ");
            stb.append("         AND W4.WISHNO = '4' ");
            stb.append("     LEFT JOIN ENTEXAM_COURSE_MST T6C ON T6C.ENTEXAMYEAR = W4.ENTEXAMYEAR ");
            stb.append("         AND T6C.APPLICANTDIV = W4.APPLICANTDIV ");
            stb.append("         AND T6C.TESTDIV = W4.TESTDIV ");
            stb.append("         AND T6C.COURSECD = W4.COURSECD ");
            stb.append("         AND T6C.MAJORCD = W4.MAJORCD ");
            stb.append("         AND T6C.EXAMCOURSECD = W4.EXAMCOURSECD ");
            stb.append("     LEFT JOIN MAJOR_MST T6 ON T6.COURSECD = BASE.SUC_COURSECD ");
            stb.append("         AND T6.MAJORCD = BASE.SUC_MAJORCD ");
            stb.append("     LEFT JOIN MAJOR_MST T6F ON T6F.COURSECD = T3.COURSECD ");
            stb.append("         AND T6F.MAJORCD = T3.MAJORCD ");
            stb.append("     LEFT JOIN ENTEXAM_COURSE_MST T7 ON T7.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
            stb.append("         AND T7.APPLICANTDIV = BASE.APPLICANTDIV ");
            stb.append("         AND T7.TESTDIV = BASE.TESTDIV ");
            stb.append("         AND T7.COURSECD = BASE.SUC_COURSECD ");
            stb.append("         AND T7.MAJORCD = BASE.SUC_MAJORCD ");
            stb.append("         AND T7.EXAMCOURSECD = BASE.SUC_COURSECODE ");
            stb.append("     LEFT JOIN NAME_MST NML013 ON NML013.NAMECD1 = 'L013' ");
            stb.append("         AND NML013.NAMECD2 = BASE.JUDGEMENT ");
            stb.append("     LEFT JOIN NAME_MST NML031 ON NML031.NAMECD1 = 'L031' ");
            stb.append("         AND NML031.NAMECD2 = BASE.JUDGE_KIND ");
            stb.append(" WHERE ");
            stb.append("     BASE.ENTEXAMYEAR = '" + param._entexamyear + "' ");
            stb.append("     AND BASE.APPLICANTDIV = '" + param._applicantdiv + "' ");
            stb.append("     AND BASE.TESTDIV = '" + param._testdiv + "' ");
            stb.append("     AND BASE.JUDGEMENT IS NOT NULL ");
            stb.append("     AND BASE.JUDGEMENT NOT IN ('3', '4') ");
            if ("1".equals(param._form)) {
                if ("1".equals(param._output1)) { // 合格・不合格
                } else if ("2".equals(param._output1)) { // 合格のみ
                    stb.append("     AND NML013.NAMESPARE1 = '1' ");
                } else if ("3".equals(param._output1)) { // 不合格のみ
                    stb.append("     AND VALUE(NML013.NAMESPARE1, '') <> '1' ");
                } else if ("4".equals(param._output1)) { // 難関コース補欠合格のみ
                    stb.append("     AND NML013.NAMESPARE1 = '1' ");
                    stb.append("     AND BASE.JUDGEMENT = '8' "); // 8:難関補欠合格
                }
                if ("2".equals(param._output2)) {
                    stb.append("     AND BASE.EXAMNO = '" + param._examno1 + "' ");
                }
            } else if ("2".equals(param._form)) {
                stb.append("     AND NML013.NAMESPARE1 = '1' ");
                stb.append("     AND BASE.JUDGE_KIND IS NOT NULL ");
                if ("2".equals(param._output3)) {
                    stb.append("     AND BASE.EXAMNO = '" + param._examno2 + "' ");
                }
            }
            stb.append(" ORDER BY ");
            stb.append("     BASE.EXAMNO ");
            return stb.toString();
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 72462 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _entexamyear;
        final String _applicantdiv;
        final String _testdiv;
        final String _noticedate;
        final String _form; // 1:通知 2:特別奨学生通知書
        final String _output1; // 出力通知 1:全校 2:指定
        final String _output2; // 出力通知対象 1:全員 2:指定
        final String _examno1; // 出力通知指定
        final String _output3; // 出力奨学生通知書対象 1:全員 2:指定
        final String _examno2; // 出力奨学生指定
        final String _documentroot;
        final String _sakunendo;

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
            _examno1 = request.getParameter("EXAMNO1");
            _output3 = request.getParameter("OUTPUT3");
            _examno2 = request.getParameter("EXAMNO2");
            _documentroot = request.getParameter("DOCUMENTROOT");
            _sakunendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_entexamyear) - 1);

            _applicantdivName = getNameMst(db2, "NAME1", "L003", _applicantdiv);
            _testdivName1 = getNameMst(db2, "NAME1", "L004", _testdiv);
            _testdivAbbv1 = getNameMst(db2, "ABBV1", "L004", _testdiv);
            _imageFile = getImageFile(db2);
            _schoolName = getCertifSchoolDat(db2, "SCHOOL_NAME", false);
            _jobName = StringUtils.defaultString(getCertifSchoolDat(db2, "JOB_NAME", false));
            _principalName = StringUtils.defaultString(getCertifSchoolDat(db2, "PRINCIPAL_NAME", "2".equals(_form)));
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
                sql.append("   AND CERTIF_KINDCD = '106' ");
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

