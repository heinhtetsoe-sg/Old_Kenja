/*
 * $Id: 38a71d9e7fc06779748ce0e56767cefe57434eab $
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;

/**
 *
 *  学校教育システム 賢者 [入試管理]
 *
 *                  ＜ＫＮＪＬ３２７Ｙ＞  高校各種通知書
 **/
public class KNJL327Y {

    private static final Log log = LogFactory.getLog(KNJL327Y.class);

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

            if ("1".equals(_param._form)) {
                if ("1".equals(_param._output1)) {
                    printMain(db2, svf, 0);
                } else if ("2".equals(_param._output1)) {
                    printMain(db2, svf, 1);
                } else if ("3".equals(_param._output1)) {
                    printMain(db2, svf, 2);
                }
            } else if ("2".equals(_param._form)) {
                printMain2(db2, svf);
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

    private Map getEntexamPaymentMoneyMstMap(DB2UDB db2) {
        Map map = new HashMap();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final String sql = " SELECT * FROM ENTEXAM_PAYMENT_MONEY_MST WHERE APPLICANTDIV = '" + _param._applicantDiv + "' ";
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String judgeKindCd = rs.getString("JUDGE_KIND");
                final String judgeKindName = rs.getString("JUDGE_KIND_NAME");
                final String entMoneyName = rs.getString("ENT_MONEY_NAME");
                final String facMoneyName = rs.getString("FAC_MONEY_NAME");
                final String lessonMoneyName = rs.getString("LESSON_MONEY_NAME");
                final String facMntMoneyName = rs.getString("FAC_MNT_MONEY_NAME");
                map.put(judgeKindCd, new EntexamPaymentMoneyMst(judgeKindCd, judgeKindName, entMoneyName, facMoneyName, lessonMoneyName, facMntMoneyName));
            }

        } catch (SQLException ex) {
            log.error("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return map;
    }

    private void printMain2(DB2UDB db2, Vrw32alp svf) {

        final List applicants = getApplicants(db2, 3);
        final Map entexamPaymentMoneyMstMap = getEntexamPaymentMoneyMstMap(db2);
        try {
            for (final Iterator it = applicants.iterator(); it.hasNext(); ) {
                final Applicant applicant = (Applicant) it.next();

                if ("8".equals(applicant._judgeKind) && "1".equals(applicant._sportsFlg2)) {
                    svf.VrSetForm("KNJL327Y_3_2.frm", 1);
               } else {
                    svf.VrSetForm("KNJL327Y_3.frm", 1);
                }
                svf.VrsOut("NENDO",     _param._entexamYear + "年度");
                svf.VrsOut("DATE",      _param._ndate);
                svf.VrsOut("TITLE",     _param._title);
                svf.VrsOut("TESTDIV" ,  _param._testdivName);
                svf.VrsOut("SCHOOLADDRESS", _param._schoolAddress);
                svf.VrsOut("SCHOOLNAME",    _param._schoolName);
                svf.VrsOut("JOBNAME",   _param._jobName);
                svf.VrsOut("STAFFNAME", _param._principalName);
                if (null != applicant._finschoolName) {
                    if (applicant._finschoolName.contains("中等教育学校")) {
                        svf.VrsOut("FS_NAME",    applicant._finschoolName);
                    } else {
                        svf.VrsOut("FS_NAME",    applicant._finschoolName + "中学校");
                    }
                }
                if (null != _param._schoolStampFile) {
                    svf.VrsOut("SCHOOLSTAMP", _param._schoolStampFile.toString());
                }
                svf.VrsOut("EXAMNO",   applicant._examno);
                svf.VrsOut("NAME",     applicant._name);

                final EntexamPaymentMoneyMst epmm0 = (EntexamPaymentMoneyMst) entexamPaymentMoneyMstMap.get("0");
                if (null != epmm0) {
                    svf.VrsOut("ENT_MONEY_DEFAULT",    epmm0._entMoneyName);
                    svf.VrsOut("ENT_FACILITY_DFAULT",  epmm0._facMoneyName);
                    svf.VrsOut("EM_TUTITION_DEFAULT",  epmm0._lessonMoneyName);
                    svf.VrsOut("EM_FACILITY_DEFAULT",  epmm0._facMntMoneyName);
                }

                final EntexamPaymentMoneyMst epmm = (EntexamPaymentMoneyMst) entexamPaymentMoneyMstMap.get(applicant._judgeKind);
                if (null != epmm) {
                    svf.VrsOut("JUDGEDIV",     epmm._judgeKindName);
                    svf.VrsOut("ENT_MONEY",    epmm._entMoneyName);
                    svf.VrsOut("ENT_FACILITY", epmm._facMoneyName);
                    svf.VrsOut("EM_TUTITION",  epmm._lessonMoneyName);
                    svf.VrsOut("EM_FACILITY",  epmm._facMntMoneyName);
                }

                if ("5".equals(applicant._judgeKind)) {
                    svf.VrsOut("NOTICE1_1", "※　入学後、毎月の授業料の免除は上記の通りですが、授業料の他に一律に保護者後援会費、");
                    svf.VrsOut("NOTICE1_2", "行事費等（科・コースによって金額が異なります。）を納入いただきます。");
                } else {
                    svf.VrsOut("NOTICE2_1", "※　入学後、毎月の授業料の免除は上記の通りですが、授業料の他に一律に保護者後援会費、");
                    svf.VrsOut("NOTICE2_2", "行事費等（科・コースによって金額が異なります。）を納入いただきます。");
                }

                svf.VrEndPage();
                _hasData = true;
            }

        } catch (Exception ex) {
            log.error("Exception:", ex);
        }
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf, int flg) {

        final List applicants = getApplicants(db2, flg);
        try {
            for (final Iterator it = applicants.iterator(); it.hasNext(); ) {
                final Applicant applicant = (Applicant) it.next();

                final String form;
                final boolean useSlideForm;
                final String sucCoursename;
                final boolean isTokubetsuHanteiGoukakuShibouCourse1 = applicant._sucCourseIsExamCourse1 && "1".equals(_param._testDiv) && "5".equals(applicant._judgediv);
                if (isTokubetsuHanteiGoukakuShibouCourse1) {
                    useSlideForm = false;
                    sucCoursename = applicant._sucExamcourseName;
                } else {
                    useSlideForm = "1".equals(applicant._slideFlg) && null != applicant._examcourseCd2 && !"1".equals(applicant._judgediv); // スライド用フォーム;
                    sucCoursename = applicant._examcourseName1;
                }
                //学特入試で進学コース合格者の場合、特進チャレンジ制度の文章ありフォームを使用
                // (一般入試「希望する」フラグを使用していないので希望していない志願者にも特進チャレンジ生徒の文章が表示される。学特はほぼ一般入試希望するから?)
                if ("1".equals(_param._testDiv) && (("2".equals(_param._desireDiv) || "4".equals(_param._desireDiv)) && "1".equals(applicant._judgediv) || "3".equals(applicant._judgediv) || "5".equals(applicant._judgediv) && (!isTokubetsuHanteiGoukakuShibouCourse1 || "S".equals(applicant._sucCoursemark)))) {
                    form = useSlideForm ? "KNJL327Y_2_2.frm" : "KNJL327Y_1_2.frm";
                } else if ("3".equals(_param._testDiv) && "1".equals(applicant._selectSubclassDiv)) {
                    //一般入試においての特進チャレンジ者の合否通知
                    form = useSlideForm ? "KNJL327Y_2_3.frm" : "KNJL327Y_1_3.frm";
                } else {
                    form = useSlideForm ? "KNJL327Y_2.frm" : "KNJL327Y_1.frm";
                }
                log.info(" form = " + form + " (examno = " + applicant._examno + ", judgediv = " + applicant._judgediv + ")");
                svf.VrSetForm(form, 1);

                svf.VrsOut("NENDO",      _param._entexamYear + "年度");
                svf.VrsOut("DATE",       _param._ndate);
                svf.VrsOut("TITLE",      _param._title);
                svf.VrsOut("TESTDIV" ,   _param._testdivName);
                svf.VrsOut("SCHOOLADDRESS", _param._schoolAddress);
                svf.VrsOut("SCHOOLNAME", _param._schoolName);
                svf.VrsOut("JOBNAME",    _param._jobName);
                svf.VrsOut("STAFFNAME",  _param._principalName);
                if (null != applicant._finschoolName) {
                    if (applicant._finschoolName.contains("中等教育学校")) {
                        svf.VrsOut("FS_NAME",    applicant._finschoolName);
                    } else {
                        svf.VrsOut("FS_NAME",    applicant._finschoolName + "中学校");
                    }
                }

                if (null != _param._schoolStampFile) {
                    svf.VrsOut("SCHOOLSTAMP", _param._schoolStampFile.toString());
                }
                svf.VrsOut("EXAMNO", applicant._examno);
                svf.VrsOut("NAME",   applicant._name);

                final String judge;
				final boolean isTokubetuHanteiGoukaku = applicant._namespare1 == 1 && "1".equals(applicant._shiftDesireFlg) && "5".equals(applicant._judgediv);
                if ("3".equals(_param._testDiv) && "1".equals(applicant._selectSubclassDiv)) {
                    //一般入試においての特進チャレンジ者の合否通知
                    judge = applicant._judgedivname;
                } else if (null != applicant._judgeKind) { // 特奨
				    judge = applicant._judgeKindName;
				    if (isTokubetuHanteiGoukaku) {
				        svf.VrsOut("COMMENT",   "※特別判定制度により合格。");
				    }
				} else {
				    if (isTokubetuHanteiGoukaku) {
				        svf.VrsOut("COMMENT",   "※特別判定制度により合格。");
				        judge = "特別合格";
				    } else {
				        judge = applicant._judgedivname;
				    }
				}
                if (useSlideForm) {
                    svf.VrsOut("COURSE1", applicant._examcourseName1);
                    svf.VrsOut("COURSE2", applicant._examcourseName2);
                    svf.VrsOut("JUDGE1", "不合格");
                    svf.VrsOut("JUDGE2", judge);
                } else {
                    svf.VrsOut("COURSE1", sucCoursename);
                    svf.VrsOut("JUDGE1", judge);
                }
                if (applicant._namespare1 == 1 && "1".equals(applicant._sportsFlg) && null == applicant._judgeKind) {
                    svf.VrsOut("NOTICE",   "但し、特別奨学生を希望されておりましたが、採用にはなりませんでした。");
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
                final String slideFlg = rs.getString("SLIDE_FLG");
                final String sportsFlg = StringUtils.defaultString(rs.getString("SPORTS_FLG"), "");
                final String shiftDesireFlg = rs.getString("SHIFT_DESIRE_FLG");
                final String selectSubclassDiv = rs.getString("SELECT_SUBCLASS_DIV");
                final String examcourseName1 = rs.getString("EXAMCOURSE_NAME1");
                final String sucExamcourseName = rs.getString("SUC_EXAMCOURSE_NAME");
                final String sucCoursemark = rs.getString("SUC_COURSEMARK");
                final String examcourseCd1 = rs.getString("EXAMCOURSECD1");
                final String examcourseName2 = rs.getString("EXAMCOURSE_NAME2");
                final String examcourseCd2 = rs.getString("EXAMCOURSECD2");
                final boolean sucCourseIsExamCourse1 = "1".equals(rs.getString("SUC_COURSE_IS_EXAMCOURSE1"));
                final String judgeKind = rs.getString("JUDGE_KIND");
                final String judgeKindName = rs.getString("JUDGE_KIND_NAME");
                final String finschoolName = rs.getString("FINSCHOOL_NAME");
                final String sportsFlg2 = StringUtils.defaultString(rs.getString("SPORTS_FLG2"), "");

                final Applicant applicant = new Applicant(examno, name, judgediv, judgedivName, namespare1, slideFlg, sportsFlg, shiftDesireFlg, selectSubclassDiv,
                        examcourseName1, examcourseCd1, examcourseName2, examcourseCd2, sucCourseIsExamCourse1, sucExamcourseName, sucCoursemark, judgeKind, judgeKindName, finschoolName, sportsFlg2);
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
        stb.append(" SELECT ");
        stb.append("     T1.EXAMNO ");
        stb.append("     ,T9.NAME ");
        stb.append("     ,T1.JUDGEDIV ");
        stb.append("     ,T4.EXAMCOURSECD AS EXAMCOURSECD1 ");
        stb.append("     ,T5.EXAMCOURSE_NAME AS EXAMCOURSE_NAME1 ");
        stb.append("     ,CMSUC.EXAMCOURSE_NAME AS SUC_EXAMCOURSE_NAME ");
        stb.append("     ,CMSUC.EXAMCOURSE_MARK AS SUC_COURSEMARK ");
        stb.append("     ,CASE WHEN T9.TESTDIV = T1.TESTDIV THEN T7.EXAMCOURSECD END AS EXAMCOURSECD2 ");
        stb.append("     ,CASE WHEN T9.TESTDIV = T1.TESTDIV THEN T8.EXAMCOURSE_NAME END AS EXAMCOURSE_NAME2 ");
        stb.append("     ,CASE WHEN T9.TESTDIV = T1.TESTDIV AND T4.COURSECD || T4.MAJORCD || T4.EXAMCOURSECD = T9.SUC_COURSECD || T9.SUC_MAJORCD || T9.SUC_COURSECODE THEN 1 END AS SUC_COURSE_IS_EXAMCOURSE1 ");
        stb.append("     ,T9.SLIDE_FLG ");
        stb.append("     ,T9.SPORTS_FLG ");
        stb.append("     ,T9.SHIFT_DESIRE_FLG ");
        stb.append("     ,T9.SELECT_SUBCLASS_DIV ");
        stb.append("     ,NML013.NAMESPARE1 ");
        stb.append("     ,NML013.NAME2 AS JUDGEDIV_NAME ");
        stb.append("     ,T9.FS_CD ");
        stb.append("     ,NML001.NAME1 AS FINSCHOOL_DISTCD_NAME ");
        stb.append("     ,T2.FINSCHOOL_NAME ");
        stb.append("     ,T9.JUDGE_KIND ");
        stb.append("     ,NML025.NAME2 AS JUDGE_KIND_NAME ");
        stb.append("     ,T10.REMARK1 AS SPORTS_FLG2 ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_RECEPT_DAT T1 ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DAT T9 ON T9.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("         AND T9.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("         AND T9.EXAMNO = T1.EXAMNO ");
        stb.append("     INNER JOIN NAME_MST NML013 ON NML013.NAMECD1 = 'L013' AND NML013.NAMECD2 = T1.JUDGEDIV ");
        stb.append("     LEFT JOIN FINSCHOOL_MST T2 ON T2.FINSCHOOLCD = T9.FS_CD ");
        stb.append("     INNER JOIN ENTEXAM_APPLICANTDESIRE_DAT T3 ON T3.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("         AND T3.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("         AND T3.EXAMNO = T1.EXAMNO ");
        stb.append("         AND T3.TESTDIV = T1.TESTDIV ");
        stb.append("     LEFT JOIN ENTEXAM_WISHDIV_MST T4 ON T4.ENTEXAMYEAR = T3.ENTEXAMYEAR ");
        stb.append("         AND T4.APPLICANTDIV = T3.APPLICANTDIV ");
        stb.append("         AND T4.TESTDIV = T3.TESTDIV ");
        stb.append("         AND T4.DESIREDIV = T3.DESIREDIV ");
        stb.append("         AND T4.WISHNO = '1' ");
        stb.append("     LEFT JOIN ENTEXAM_COURSE_MST T5 ON T5.ENTEXAMYEAR = T4.ENTEXAMYEAR ");
        stb.append("         AND T5.APPLICANTDIV = T4.APPLICANTDIV ");
        stb.append("         AND T5.TESTDIV = T4.TESTDIV ");
        stb.append("         AND T5.COURSECD = T4.COURSECD ");
        stb.append("         AND T5.MAJORCD = T4.MAJORCD ");
        stb.append("         AND T5.EXAMCOURSECD = T4.EXAMCOURSECD ");
        stb.append("     LEFT JOIN ENTEXAM_WISHDIV_MST T7 ON T7.ENTEXAMYEAR = T3.ENTEXAMYEAR ");
        stb.append("         AND T7.APPLICANTDIV = T3.APPLICANTDIV ");
        stb.append("         AND T7.TESTDIV = T3.TESTDIV ");
        stb.append("         AND T7.DESIREDIV = T3.DESIREDIV ");
        stb.append("         AND T7.WISHNO = '2' ");
        stb.append("     LEFT JOIN ENTEXAM_COURSE_MST T8 ON T8.ENTEXAMYEAR = T7.ENTEXAMYEAR ");
        stb.append("         AND T8.APPLICANTDIV = T7.APPLICANTDIV ");
        stb.append("         AND T8.TESTDIV = T7.TESTDIV ");
        stb.append("         AND T8.COURSECD = T7.COURSECD ");
        stb.append("         AND T8.MAJORCD = T7.MAJORCD ");
        stb.append("         AND T8.EXAMCOURSECD = T7.EXAMCOURSECD ");
        stb.append("     LEFT JOIN ENTEXAM_COURSE_MST CMSUC ON CMSUC.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("         AND CMSUC.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("         AND CMSUC.TESTDIV = T1.TESTDIV ");
        stb.append("         AND CMSUC.COURSECD = T9.SUC_COURSECD ");
        stb.append("         AND CMSUC.MAJORCD = T9.SUC_MAJORCD ");
        stb.append("         AND CMSUC.EXAMCOURSECD = T9.SUC_COURSECODE ");
        stb.append("     LEFT JOIN NAME_MST NML001 ON NML001.NAMECD1 = 'L001' AND NML001.NAMECD2 = T2.FINSCHOOL_DISTCD ");
        stb.append("     LEFT JOIN NAME_MST NML025 ON NML025.NAMECD1 = 'L025' AND NML025.NAMECD2 = T9.JUDGE_KIND ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT T10 ON T10.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("         AND T10.EXAMNO = T1.EXAMNO ");
        stb.append("         AND T10.SEQ = '005' ");
        stb.append(" WHERE ");
        stb.append("     T1.ENTEXAMYEAR = '" + _param._entexamYear + "' ");
        stb.append("     AND T1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND T1.TESTDIV = '" + _param._testDiv + "' ");
        stb.append("     AND T1.EXAM_TYPE = '1' ");
        stb.append("     AND VALUE(T1.JUDGEDIV, '') <> '4' ");
        stb.append("     AND T3.DESIREDIV = '" + _param._desireDiv + "' ");
        if (0 == judge_flg) {
            stb.append("     AND T1.JUDGEDIV IS NOT NULL ");
        } else if (1 == judge_flg) {
            stb.append("     AND NML013.NAMESPARE1 = '1' ");
        } else if (2 == judge_flg) {
            stb.append("     AND VALUE(NML013.NAMESPARE1, '') <> '1' ");
        }
        if (null != _param._examno) {
            stb.append("     AND T1.EXAMNO = '" + _param._examno + "' ");
        }
        if (3 == judge_flg) {
            stb.append("     AND NML013.NAMESPARE1 = '1' ");
            stb.append("     AND T9.JUDGE_KIND IS NOT NULL ");
        }
        stb.append(" ORDER BY ");
        if (_param._isSchoolSort) {
            stb.append("     T9.FS_CD, ");
        }
        stb.append("     T1.EXAMNO ");
        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 72188 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    private static class Applicant {
        final String _name;
        final String _examno;
        final String _judgediv;
        final String _judgedivname;
        final int _namespare1;
        final String _slideFlg;
        final String _sportsFlg;
        final String _shiftDesireFlg;
        final String _selectSubclassDiv;
        final String _examcourseName1;
        final String _examcourseCd1;
        final String _examcourseName2;
        final String _examcourseCd2;
        final boolean _sucCourseIsExamCourse1;
        final String _sucExamcourseName;
        final String _sucCoursemark; // "S"は進学コース
        final String _judgeKind;
        final String _judgeKindName;
        final String _finschoolName;
        final String _sportsFlg2;
        public Applicant(
                final String examno,
                final String name,
                final String judgediv,
                final String judgedivName,
                final int namespare1,
                final String slideFlg,
                final String sportsFlg,
                final String shiftDesireFlg,
                final String selectSubclassDiv,
                final String examcourseName1,
                final String examcourseCd1,
                final String examcourseName2,
                final String examcourseCd2,
                final boolean sucCourseIsExamCourse1,
                final String sucExamcourseName,
                final String sucCoursemark,
                final String judgeKind,
                final String judgeKindName,
                final String finschoolName,
                final String sportsFlg2) {
            _name = name;
            _examno = examno;
            _judgediv = judgediv;
            _judgedivname = judgedivName;
            _namespare1 = namespare1;
            _slideFlg = slideFlg;
            _sportsFlg = sportsFlg;
            _shiftDesireFlg = shiftDesireFlg;
            _selectSubclassDiv = selectSubclassDiv;
            _examcourseName1 = examcourseName1;
            _examcourseCd1 = examcourseCd1;
            _examcourseName2 = examcourseName2;
            _examcourseCd2 = examcourseCd2;
            _sucExamcourseName = sucExamcourseName;
            _sucCoursemark = sucCoursemark;
            _sucCourseIsExamCourse1 = sucCourseIsExamCourse1;
            _judgeKind = judgeKind;
            _judgeKindName = judgeKindName;
            _finschoolName = finschoolName;
            _sportsFlg2 = sportsFlg2;
        }
    }

    private static class EntexamPaymentMoneyMst {
        final String _judgeKindCd;
        final String _judgeKindName;
        final String _entMoneyName;
        final String _facMoneyName;
        final String _lessonMoneyName;
        final String _facMntMoneyName;
        public EntexamPaymentMoneyMst(
                final String judgeKindCd,
                final String judgeKindName,
                final String entMoneyName,
                final String facMoneyName,
                final String lessonMoneyName,
                final String facMntMoneyName) {
            _judgeKindCd = judgeKindCd;
            _judgeKindName = judgeKindName;
            _entMoneyName = entMoneyName;
            _facMoneyName = facMoneyName;
            _lessonMoneyName = lessonMoneyName;
            _facMntMoneyName = facMntMoneyName;
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
        final String _form; // 帳票種類区分
        final boolean _isSchoolSort;
        final String _schoolName;
        final String _schoolAddress;
        final String _jobName;
        final String _principalName;
        final File _schoolStampFile;
        final String _output1; // 1:全て 2:合格者 3:不合格者
        final String _examno;           // 指定：受験番号

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamYear = request.getParameter("YEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _desireDiv = request.getParameter("DESIREDIV");
            _title = getApplicantdivName(db2);
            _testdivName = getTestDivName(db2);
            _ndate = new SimpleDateFormat("yyyy年M月d日").format(Date.valueOf(request.getParameter("NDATE").replace('/', '-')));
            _form = request.getParameter("FORM");
            _isSchoolSort = null != request.getParameter("SCHOOL_SORT" + _form);
            _schoolName = getCertifSchool(db2, "SCHOOL_NAME");
            _schoolAddress = getCertifSchool(db2, "REMARK2");
            _jobName = getCertifSchool(db2, "JOB_NAME");
            _principalName = getCertifSchool(db2, "PRINCIPAL_NAME");
            _schoolStampFile = getSchoolStamp(db2, request.getParameter("DOCUMENTROOT"));
            _output1 = request.getParameter("OUTPUT1");      // 出力範囲
            _examno = "2".equals(request.getParameter("OUTPUT2")) ? request.getParameter("EXAMNO1") :
                "2".equals(request.getParameter("OUTPUT3")) ? request.getParameter("EXAMNO2") : null;
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
                    rtn = rs.getString(field);
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
            return schoolStampFile.exists() ? schoolStampFile : null;
        }
    }
}

// eof
