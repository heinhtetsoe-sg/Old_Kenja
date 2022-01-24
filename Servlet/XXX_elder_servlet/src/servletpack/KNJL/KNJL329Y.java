/*
 * $Id: e0a3510c4038085568f221c51222fb98f47c6ec8 $
 *
 * 作成日: 2010/11/09
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;


import java.io.File;
import java.math.BigDecimal;
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
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;

/**
 *
 *  学校教育システム 賢者 [入試管理]
 *
 *                  ＜ＫＮＪＬ３２９Ｙ＞ 結果通知一覧
 **/
public class KNJL329Y {

    private static final Log log = LogFactory.getLog(KNJL329Y.class);

    private boolean _hasData;

    private Param _param;

    private final String SUBCLASSCD_CALC = "CALC";
    private final String SUBCLASSCD_1   = "1";
    private final String SUBCLASSCD_2   = "2";
    private final String SUBCLASSCD_3   = "3";
    private final String SUBCLASSCD_4   = "4";
    private final String SUBCLASSCD_5   = "5";
    private final String SUBCLASSCD_5_2 = "5_2";
    private final String SUBCLASSCD_6   = "6";
    private final String SUBCLASSCD_6_2 = "6_2";
    private final String SUBCLASSCD_9   = "9";

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

            if ("1".equals(_param._outputdiv)) {
                printMain(db2, svf);
            } else {
//                if ("1".equals(param._output1)) {
//                    printMainKNJL327Y(db2, param, svf, 0);
//                } else if ("2".equals(param._output1)) {
                    printMainKNJL327Y(db2, svf, 1);
//                } else if ("3".equals(param._output1)) {
//                    printMainKNJL327Y(db2, param, svf, 2);
//                }
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
        }
        return count;
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {

        final String frm;
        final int flg;
        if ("1".equals(_param._applicantDiv)) {
            frm = "KNJL329Y_1.frm"; // 中学
            flg = 1;
        } else {
            if ("1".equals(_param._testDiv)) {
                frm = "KNJL329Y_2.frm"; // 学特
                flg = 2;
            } else if ("3".equals(_param._testDiv)) {
                frm = "KNJL329Y_4.frm"; // 一般
                flg = 3;
            } else {
                frm = "KNJL329Y_3.frm"; // 推薦
                flg = 3;
            }
        }
        final List finschoolList = getFinschools(db2, flg);
        try {
            for (final Iterator it = finschoolList.iterator(); it.hasNext();) {
                final Finschool finschool = (Finschool) it.next();

                log.info(" finschool cd = " + finschool._finschoolcd);

                svf.VrSetForm(frm, 4);
                boolean iskaiPage = true;
                String subTitle = "";
                for (final Iterator itApp = finschool._applicants.iterator(); itApp.hasNext(); ) {
                    final Applicant applicant = (Applicant) itApp.next();

                    //一般入試の場合、特進再チャレンジ者のみ改ページして別ページに出力する
                    if (_param._isIppan && "1".equals(applicant._selectSubclassDiv) && iskaiPage) {
                        svf.VrSetForm(frm, 4);
                        iskaiPage = false;
                        subTitle = "　特進チャレンジ";
                    }

                    svf.VrsOut("NENDO",    _param._entexamYear + "年度");
                    svf.VrsOut("DATE",     _param._nDate); // 通知日付
                    svf.VrsOut("TITLE",    _param._title);
                    svf.VrsOut("SUBTITLE", _param._subTitle + subTitle);
                    svf.VrsOut("SCHOOLNAME", _param._schoolName);
                    final String finschoolDistcdName = null == finschool._finschoolDistcdName ? "" : finschool._finschoolDistcdName + "立";
                    String finschoolName = null == finschool._finschoolName ? "" : finschool._finschoolName;
                    if (!"1".equals(_param._applicantDiv)) {
                    	if (!finschoolName.contains("中等教育学校")) {
                    		finschoolName += "中学";
                    	}
                    }

                    final int nameLen = getMS932count(applicant._name);
                    if (flg == 1) {
                        svf.VrsOut("FINSCHOOLNAME", finschoolDistcdName + finschoolName);
                        svf.VrsOut("JOBNAME",    _param._jobName);
                        svf.VrsOut("STAFFNAME",  _param._principalName);
                        if (null != _param._schoolStampFile) {
                            svf.VrsOut("SCHOOLSTAMP",  _param._schoolStampFile.toString());
                        }
                        final String nameField = "NAME" + (30 < nameLen ? "3" : 20 < nameLen ? "2" : "1");
                        svf.VrsOut("EXAMNO",           applicant._examno);
                        svf.VrsOut(nameField,          applicant._name);
                        svf.VrsOut("JUDGE",            applicant._judgedivname);

                    } else if (flg == 2) {
                        svf.VrsOut("FINSCHOOL", finschoolDistcdName + finschoolName);
                        final String nameField = "NAME" + (30 < nameLen ? "3" : 20 < nameLen ? "2" : "1");
                        svf.VrsOut("EXAMNO",           applicant._examno);
                        svf.VrsOut(nameField,          applicant._name);
                        svf.VrsOut("DESIRE",           applicant._shDivName);
                        svf.VrsOut("EXAMCOURSE_NAME1", applicant._examcourseName1);
                        final String judgedivname;
                        if ("1".equals(applicant._namespare1) && "5".equals(applicant._judgediv)) {
                            svf.VrsOut("COMMENT",   "※「特別合格」は、特別判定制度による合格。");
                            judgedivname = "特別合格";
                        } else {
                            judgedivname = applicant._judgedivname;
                        }
                        final String judgeField = (6 < getMS932count(judgedivname) ? "_2" : "");
                        final boolean isTokubetsuHanteiGoukakuShibouCourse1 = applicant._sucCourseIsExamCourse1 && "1".equals(_param._testDiv) && "5".equals(applicant._judgediv);
                        if (isTokubetsuHanteiGoukakuShibouCourse1) {
                            svf.VrsOut("JUDGE1" + judgeField,       judgedivname);
                        } else if ("1".equals(applicant._slideFlg) && null != applicant._examcourseCd2 && null != applicant._judgediv && !"1".equals(applicant._judgediv)) {
                            if ("4".equals(applicant._judgediv)) {
                                svf.VrsOut("JUDGE1" + judgeField,       judgedivname);
                            } else {
                                svf.VrsOut("JUDGE1",       "不合格");
                                svf.VrsOut("JUDGE2" + judgeField,       judgedivname);
                                svf.VrsOut("EXAMCOURSE_NAME2", applicant._examcourseName2);
                            }
                        } else {
                            svf.VrsOut("JUDGE1" + judgeField,       judgedivname);
                        }
                        svf.VrsOut("TOTAL_NUMBER", _param._receptCount);

                        if ("1".equals(applicant._namespare1)) {
                            final String remarkField = "REMARK" + (6 < getMS932count(applicant._judgeKindName) ? "2" : "1");
                            svf.VrsOut(remarkField,        applicant._judgeKindName);
                        }
                        if (!"1".equals(applicant._interviewAttendFlg)) {
                            final Map testSubclassCds = new HashMap();
                            testSubclassCds.put(SUBCLASSCD_1, "JAPANESE");
                            testSubclassCds.put(SUBCLASSCD_2, "MATHEMATICS");
                            testSubclassCds.put(SUBCLASSCD_3, "SOCIAL");
                            testSubclassCds.put(SUBCLASSCD_4, "SCIENCE");
                            testSubclassCds.put(SUBCLASSCD_9, "KASANTEN");
                            for (final Iterator itSubclass = testSubclassCds.keySet().iterator(); itSubclass.hasNext(); ) {
                                final String testsubclasscd = (String) itSubclass.next();
                                final String field = (String) testSubclassCds.get(testsubclasscd);
                                final String score = (String) applicant._testScoreMap.get(testsubclasscd);
                                svf.VrsOut(field, score);
                            }
                            svf.VrsOut("ENGLISH1", applicant.getEnglishListeningScore(SUBCLASSCD_5, SUBCLASSCD_6));
                            svf.VrsOut("SUBTOTAL", applicant._total2);
                            svf.VrsOut("TOTAL1", applicant._total1);
                            svf.VrsOut("TOTAL_RANK", applicant._totalRank3);
                            svf.VrsOut("SH_SCHOOL", applicant._shSchoolname);

                            if (!StringUtils.isBlank(applicant._shSchoolRankOfTotalRank3) || !StringUtils.isBlank(applicant._shSchoolApplicantCount)) {
                                svf.VrsOut("SH_RANK", applicant._shSchoolRankOfTotalRank3);
                                svf.VrsOut("ALASH", "/");
                                svf.VrsOut("SH_NUMBER", applicant._shSchoolApplicantCount);
                            }
                        }

                        // 各科目の平均点（KNJL322Yの高校・学特指定時の最下（母集団全体）の列を表示）
                        for (final Iterator itSub = _param._knjl322yHighSubclassMap.entrySet().iterator(); itSub.hasNext();) {
                            final Entry entry = (Entry) itSub.next();
                            final String testSubclassCd = (String) entry.getKey();
                            final String field = (String) entry.getValue();
                            if (null != _param._knjl322yCourse.getAvg(testSubclassCd)) {
                                svf.VrsOut(field, _param._knjl322yCourse.getAvg(testSubclassCd).toString());
                            }
                        }

                    } else if (_param._isIppan) {
                        svf.VrsOut("FINSCHOOL", finschoolDistcdName + finschoolName);
                        final String nameField = "NAME" + (30 < nameLen ? "3" : 20 < nameLen ? "2" : "1");
                        svf.VrsOut("EXAMNO",           applicant._examno);
                        svf.VrsOut(nameField,          applicant._name);
                        //スライド判定での合格・不合格の場合、志望コース２も出力する。（一般入試新規受験者のみスライド判定がある）
                        if ("1".equals(applicant._slideFlg) && ("2".equals(applicant._judgediv) || "3".equals(applicant._judgediv))) {
                            svf.VrsOut("EXAMCOURSE_NAME1", applicant._examcourseName1);
                            svf.VrsOut("JUDGE1",           "不合格");
                            svf.VrsOut("EXAMCOURSE_NAME2", applicant._examcourseName2);
                            svf.VrsOut("JUDGE2",           applicant._judgedivname);
                        } else {
                            svf.VrsOut("EXAMCOURSE_NAME1", applicant._examcourseName1);
                            svf.VrsOut("JUDGE1",           applicant._judgedivname);
                        }
                        if ("1".equals(applicant._namespare1)) {
                            svf.VrsOut("REMARK",           applicant._judgeKindName);
                        }

                    } else if (flg == 3) {
                        svf.VrsOut("FINSCHOOL", finschoolDistcdName + finschoolName);
                        final String nameField = "NAME" + (30 < nameLen ? "3" : 20 < nameLen ? "2" : "1");
                        svf.VrsOut("EXAMNO",           applicant._examno);
                        svf.VrsOut(nameField,          applicant._name);
                        //スライド判定での合格・不合格の場合、志望コース２を出力する。（一般入試新規受験者のみスライド判定がある）
                        if ("1".equals(applicant._slideFlg) && ("2".equals(applicant._judgediv) || "3".equals(applicant._judgediv))) {
                            svf.VrsOut("EXAMCOURSE_NAME1", applicant._examcourseName2);
                        } else {
                            svf.VrsOut("EXAMCOURSE_NAME1", applicant._examcourseName1);
                        }
                        svf.VrsOut("JUDGE1",           applicant._judgedivname);
                        if ("1".equals(applicant._namespare1)) {
                            svf.VrsOut("REMARK",           applicant._judgeKindName);
                        }
                    }
                    svf.VrEndRecord();

                    _hasData = true;
                }
            }

        } catch (Exception ex) {
            log.error("Exception:", ex);
        }
    }

    private List getFinschools(final DB2UDB db2, final int flg) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        final List finschoolList = new ArrayList();
        final Map receptnoMap = new HashMap();
        try {
            final String sql = getApplicantSql(flg);
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {

                final String finschoolCd = null == rs.getString("FS_CD") ? "" : rs.getString("FS_CD");
                Finschool finschool = null;
                for (final Iterator it = finschoolList.iterator(); it.hasNext();) {
                    final Finschool fs = (Finschool) it.next();
                    if (fs._finschoolcd.equals(finschoolCd)) {
                        finschool = fs;
                    }
                }
                if (null == finschool) {
                    final String finschoolDistcdName = rs.getString("FINSCHOOL_DISTCD_NAME");
                    final String finschoolName = rs.getString("FINSCHOOL_NAME");
                    finschool = new Finschool(finschoolCd, finschoolDistcdName, finschoolName);
                    finschoolList.add(finschool);
                }

                final String examno = rs.getString("EXAMNO");
                final String name = rs.getString("NAME");
                final String judgediv = rs.getString("JUDGEDIV");
                final String judgedivName = rs.getString("JUDGEDIV_NAME");
                final String namespare1 = rs.getString("NAMESPARE1");
                final String examcourseCd1 = rs.getString("EXAMCOURSECD1");
                final String examcourseName1 = rs.getString("EXAMCOURSE_NAME1");
                final String examcourseCd2 = 2 == flg || 3 == flg ? rs.getString("EXAMCOURSECD2") : "";
                final String examcourseName2 = 2 == flg || 3 == flg ? rs.getString("EXAMCOURSE_NAME2") : "";
                final String judgeKindName = 2 == flg || 3 == flg ? rs.getString("JUDGEKIND_NAME") : "";
                final String slideFlg = rs.getString("SLIDE_FLG");
                final String interviewAttendFlg = rs.getString("INTERVIEW_ATTEND_FLG");
                final String shiftDesireFlg = rs.getString("SHIFT_DESIRE_FLG");
                final String selectSubclassDiv = rs.getString("SELECT_SUBCLASS_DIV");
                final String shDiv = rs.getString("SHDIV");
                final String shDivname = rs.getString("SHDIVNAME");
                final String shSchoolcd = 2 == flg ? rs.getString("SH_SCHOOLCD") : "";
                final String shSchoolname = 2 == flg ? rs.getString("SH_SCHOOLNAME") : "";
                final String total1 = rs.getString("TOTAL1");
                final String total2 = rs.getString("TOTAL2");
                final String totalRank3 = 2 == flg ? rs.getString("TOTAL_RANK2") : "";
                final String sucExamcourseName = rs.getString("SUC_EXAMCOURSE_NAME");
                final boolean sucCourseIsExamCourse1 = "1".equals(rs.getString("SUC_COURSE_IS_EXAMCOURSE1"));

                final Applicant applicant = new Applicant(examno, name, judgediv, judgedivName, namespare1,
                        examcourseCd1, examcourseName1, examcourseCd2, examcourseName2,
                        judgeKindName, slideFlg, interviewAttendFlg, shiftDesireFlg, selectSubclassDiv, shDiv, shDivname, shSchoolcd, shSchoolname, total1, total2, totalRank3,
                        sucExamcourseName, sucCourseIsExamCourse1);
                finschool._applicants.add(applicant);

                if ((2 == flg)) {
                    final String receptno = rs.getString("RECEPTNO");
                    if (receptnoMap.containsKey(receptno)) {
                        log.debug("_");
                    } else {
                        receptnoMap.put(receptno, applicant);
                    }
                }
            }

        } catch (SQLException ex) {
            log.error("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }

        if (2 == flg) {
            try {
                final String sql = getScoreSql();
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    final String testsubclasscd = rs.getString("TESTSUBCLASSCD");
                    final String score = rs.getString("SCORE");
                    final Applicant applicant = (Applicant) receptnoMap.get(rs.getString("RECEPTNO"));
                    if (null == applicant) {
                        continue;
                    }
                    applicant.addScore(testsubclasscd, score);
                }

            } catch (SQLException ex) {
                log.error("Exception:", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }

            try {
                final String sql = getShSchoolRankSql();
                log.debug(" shSchoolRanksql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    final Applicant applicant = (Applicant) receptnoMap.get(rs.getString("RECEPTNO"));
                    if (null == applicant) {
                        continue;
                    }
                    applicant._shSchoolRankOfTotalRank3 = rs.getString("RANK_BY_SHSCHOOLCD");
                    applicant._shSchoolApplicantCount = rs.getString("COUNT");
                }

            } catch (SQLException ex) {
                log.error("Exception:", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
        return finschoolList;
    }

    private String getApplicantSql(final int flg) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.EXAMNO ");
        stb.append("     ,T1.NAME ");
        stb.append("     ,T10.RECEPTNO ");
        stb.append("     ,T10.JUDGEDIV ");
        stb.append("     ,NML013_2.NAME2 AS JUDGEDIV_NAME ");
        stb.append("     ,NML013_2.NAMESPARE1 ");
        stb.append("     ,T1.SELECT_SUBCLASS_DIV ");
        stb.append("     ,T4.EXAMCOURSECD AS EXAMCOURSECD1 ");
        stb.append("     ,T5.EXAMCOURSE_NAME AS EXAMCOURSE_NAME1 ");
        if (flg == 2 || flg == 3) {
            stb.append("     ,T7.EXAMCOURSECD AS EXAMCOURSECD2 ");
            stb.append("     ,CASE WHEN T1.TESTDIV = '" + _param._testDiv + "' AND VALUE(T1.SLIDE_FLG, '') = '1' THEN T8.EXAMCOURSE_NAME END AS EXAMCOURSE_NAME2 ");
        }
        stb.append("     ,T1.FS_CD ");
        stb.append("     ,NML001.NAME1 AS FINSCHOOL_DISTCD_NAME ");
        stb.append("     ,T2.FINSCHOOL_NAME ");
        if (flg == 2 || flg == 3) {
            stb.append("     ,(CASE WHEN VALUE(T1.SPORTS_FLG, '') = '1' AND T1.JUDGE_KIND IS NULL THEN '非特奨' ");
            stb.append("            WHEN T1.JUDGE_KIND IS NOT NULL THEN NML025.NAME2 END) AS JUDGEKIND_NAME ");
        }
        stb.append("     ,T1.SHDIV ");
        stb.append("     ,NML006.NAME1 AS SHDIVNAME ");
        if (flg == 2) {
            stb.append("     ,T1.SH_SCHOOLCD ");
            stb.append("     ,T9.FINSCHOOL_NAME AS SH_SCHOOLNAME ");
        }
        stb.append("     ,CASE WHEN T1.TESTDIV = '" + _param._testDiv + "' THEN T1.SLIDE_FLG END AS SLIDE_FLG ");
        stb.append("     ,T1.SHIFT_DESIRE_FLG ");
        stb.append("     ,T1.INTERVIEW_ATTEND_FLG ");
        stb.append("     ,T10.TOTAL1 ");
        stb.append("     ,T10.TOTAL2 ");
        if (flg == 2) {
            stb.append("     ,T10.TOTAL_RANK2 ");
        }
        stb.append("     ,CMSUC.EXAMCOURSE_NAME AS SUC_EXAMCOURSE_NAME ");
        stb.append("     ,CASE WHEN T1.TESTDIV = T10.TESTDIV AND T4.COURSECD || T4.MAJORCD || T4.EXAMCOURSECD = T1.SUC_COURSECD || T1.SUC_MAJORCD || T1.SUC_COURSECODE THEN 1 END AS SUC_COURSE_IS_EXAMCOURSE1 ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_APPLICANTBASE_DAT T1 ");
        stb.append("     LEFT JOIN FINSCHOOL_MST T2 ON T2.FINSCHOOLCD = T1.FS_CD ");
        stb.append("     INNER JOIN ENTEXAM_RECEPT_DAT T10 ON T10.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("         AND T10.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("         AND T10.EXAM_TYPE = '1' ");
        stb.append("         AND T10.EXAMNO = T1.EXAMNO ");
        stb.append("     INNER JOIN ENTEXAM_APPLICANTDESIRE_DAT T3 ON T3.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("         AND T3.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("         AND T3.TESTDIV = T10.TESTDIV ");
        stb.append("         AND T3.EXAMNO = T1.EXAMNO ");

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
        if (flg == 2 || flg == 3) {

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

            stb.append("     LEFT JOIN FINHIGHSCHOOL_MST T9 ON T9.FINSCHOOLCD = T1.SH_SCHOOLCD ");
        }
        stb.append("     LEFT JOIN NAME_MST NML001 ON NML001.NAMECD1 = 'L001' AND NML001.NAMECD2 = T2.FINSCHOOL_DISTCD ");
        stb.append("     LEFT JOIN NAME_MST NML006 ON NML006.NAMECD1 = 'L006' AND NML006.NAMECD2 = T1.SHDIV ");
        stb.append("     LEFT JOIN NAME_MST NML013_2 ON NML013_2.NAMECD1 = 'L013' AND NML013_2.NAMECD2 = T10.JUDGEDIV ");
        stb.append("     LEFT JOIN NAME_MST NML025 ON NML025.NAMECD1 = 'L025' AND NML025.NAMECD2 = T1.JUDGE_KIND ");
        stb.append("     LEFT JOIN ENTEXAM_COURSE_MST CMSUC ON CMSUC.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("         AND CMSUC.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("         AND CMSUC.TESTDIV = T1.TESTDIV ");
        stb.append("         AND CMSUC.COURSECD = T1.SUC_COURSECD ");
        stb.append("         AND CMSUC.MAJORCD = T1.SUC_MAJORCD ");
        stb.append("         AND CMSUC.EXAMCOURSECD = T1.SUC_COURSECODE ");
        stb.append(" WHERE ");
        stb.append("     T1.ENTEXAMYEAR = '" + _param._entexamYear + "' ");
        stb.append("     AND T1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND T10.TESTDIV = '" + _param._testDiv + "' ");
        if (flg == 1) {
            stb.append("     AND T10.JUDGEDIV IS NOT NULL ");
        }
        if (null != _param._finschoolCd) {
            stb.append("     AND T1.FS_CD = '" + _param._finschoolCd + "' ");
        }
        stb.append(" ORDER BY ");
        stb.append("     T1.FS_CD ");
        if (_param._isIppan) {
            stb.append("     ,VALUE(T1.SELECT_SUBCLASS_DIV,'0') ");
        }
        stb.append("     ,T1.EXAMNO ");
        if (flg == 2 || flg == 3) {
            stb.append("     ,T4.COURSECD ");
            stb.append("     ,T4.MAJORCD ");
            stb.append("     ,T4.EXAMCOURSECD ");
        }
        return stb.toString();
    }

    private String getScoreSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T3.RECEPTNO, ");
        stb.append("     T3.TESTSUBCLASSCD, ");
        stb.append("     T3.SCORE ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_SCORE_DAT T3 ");
        stb.append(" WHERE ");
        stb.append("     T3.ENTEXAMYEAR = '" + _param._entexamYear + "' ");
        stb.append("     AND T3.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND T3.TESTDIV = '" + _param._testDiv + "' ");
        stb.append("     AND T3.EXAM_TYPE = '1' ");
        if ("1".equals(_param._applicantDiv)) { // 中学入試：科目「計算」の得点
            stb.append(" UNION ALL ");
            stb.append(" SELECT ");
            stb.append("     T3.RECEPTNO, ");
            stb.append("     '" + SUBCLASSCD_CALC + "' AS TESTSUBCLASSCD, ");
            stb.append("     T3.SCORE3 AS SCORE");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_SCORE_DAT T3 ");
            stb.append(" WHERE ");
            stb.append("     T3.ENTEXAMYEAR = '" + _param._entexamYear + "' ");
            stb.append("     AND T3.APPLICANTDIV = '" + _param._applicantDiv + "' ");
            stb.append("     AND T3.TESTDIV = '" + _param._testDiv + "' ");
            stb.append("     AND T3.EXAM_TYPE = '1' ");
            stb.append("     AND T3.TESTSUBCLASSCD = '2' ");
        }
        if ("2".equals(_param._applicantDiv)) { // 高校入試：科目「英語」、「リスニング」の得点
            stb.append(" UNION ALL ");
            stb.append(" SELECT ");
            stb.append("     T3.RECEPTNO, ");
            stb.append("     '" + SUBCLASSCD_5_2 + "' AS TESTSUBCLASSCD, ");
            stb.append("     T3.SCORE2 AS SCORE");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_SCORE_DAT T3 ");
            stb.append(" WHERE ");
            stb.append("     T3.ENTEXAMYEAR = '" + _param._entexamYear + "' ");
            stb.append("     AND T3.APPLICANTDIV = '" + _param._applicantDiv + "' ");
            stb.append("     AND T3.TESTDIV = '" + _param._testDiv + "' ");
            stb.append("     AND T3.EXAM_TYPE = '1' ");
            stb.append("     AND T3.TESTSUBCLASSCD = '5' ");
            stb.append(" UNION ALL ");
            stb.append(" SELECT ");
            stb.append("     T3.RECEPTNO, ");
            stb.append("     '" + SUBCLASSCD_6_2 + "' AS TESTSUBCLASSCD, ");
            stb.append("     T3.SCORE2 AS SCORE");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_SCORE_DAT T3 ");
            stb.append(" WHERE ");
            stb.append("     T3.ENTEXAMYEAR = '" + _param._entexamYear + "' ");
            stb.append("     AND T3.APPLICANTDIV = '" + _param._applicantDiv + "' ");
            stb.append("     AND T3.TESTDIV = '" + _param._testDiv + "' ");
            stb.append("     AND T3.EXAM_TYPE = '1' ");
            stb.append("     AND T3.TESTSUBCLASSCD = '6' ");
        }
        return stb.toString();
    }

    /**
     * 高校入試学特用 併願校内順位 / 併願校受験数
     */
    private String getShSchoolRankSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH DATA0 AS ( ");
        stb.append("     SELECT T1.RECEPTNO, T2.EXAMNO, T1.TOTAL_RANK2, T2.SH_SCHOOLCD ");
        stb.append("     FROM ENTEXAM_RECEPT_DAT T1 ");
        stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DAT T2 ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("         AND T2.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("         AND T2.EXAMNO = T1.EXAMNO ");
        stb.append("     WHERE ");
        stb.append("         T1.ENTEXAMYEAR = '" + _param._entexamYear + "' ");
        stb.append("         AND T1.APPLICANTDIV = '2' ");
        stb.append("         AND T1.TESTDIV = '1' ");
        stb.append("         AND T1.EXAM_TYPE = '1' ");
        stb.append("         AND T1.TOTAL_RANK2 IS NOT NULL ");
        stb.append("         AND T2.SH_SCHOOLCD IS NOT NULL ");
        stb.append(" ), COUNTS AS ( ");
        stb.append("     SELECT SH_SCHOOLCD, COUNT(*) AS COUNT ");
        stb.append("     FROM DATA0 ");
        stb.append("     GROUP BY SH_SCHOOLCD ");
        stb.append(" ) ");
        stb.append(" SELECT T1.RECEPTNO, T1.EXAMNO, T1.TOTAL_RANK2, T1.SH_SCHOOLCD, ");
        stb.append("        T2.COUNT, ");
        stb.append("        RANK() OVER(PARTITION BY T1.SH_SCHOOLCD ORDER BY TOTAL_RANK2) AS RANK_BY_SHSCHOOLCD ");
        stb.append(" FROM DATA0 T1 ");
        stb.append(" LEFT JOIN COUNTS T2 ON T2.SH_SCHOOLCD = T1.SH_SCHOOLCD ");
        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 71747 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    private static class Finschool {
        final String _finschoolcd;
        final String _finschoolName;
        final String _finschoolDistcdName;
        final List _applicants = new ArrayList();
        public Finschool(String finschoolcd, String finschoolDistcdName, String finschoolName) {
            _finschoolcd = finschoolcd;
            _finschoolName = finschoolName;
            _finschoolDistcdName = finschoolDistcdName;
        }
    }

    private static class Applicant {
        final String _name;
        final String _examno;
        final String _judgediv;
        final String _judgedivname;
        final String _namespare1;
        final String _examcourseCd1;
        final String _examcourseName1;
        final String _examcourseCd2;
        final String _examcourseName2;
        final String _judgeKindName;
        final String _slideFlg;
        final String _interviewAttendFlg;
        final String _shiftDesireFlg;
        final String _selectSubclassDiv;
        final String _shDiv;
        final String _shDivName;
        final String _total1;
        final String _total2;
        final String _shSchoolcd;
        final String _shSchoolname;
        final String _totalRank3;
        final Map _testScoreMap = new HashMap();
        final String _sucExamcourseName;
        final boolean _sucCourseIsExamCourse1;
        String _shSchoolApplicantCount;
        String _shSchoolRankOfTotalRank3;
        public Applicant(
                final String examno,
                final String name,
                final String judgediv,
                final String judgedivName,
                final String namespare1,
                final String examcourseCd1,
                final String examcourseName1,
                final String examcourseCd2,
                final String examcourseName2,
                final String judgeKindName,
                final String slideFlg,
                final String interviewAttendFlg,
                final String shiftDesireFlg,
                final String selectSubclassDiv,
                final String shDiv,
                final String shDivName,
                final String shSchoolcd,
                final String shSchoolname,
                final String total1,
                final String total2,
                final String totalRank3,
                final String sucExamcourseName,
                final boolean sucCourseIsExamCourse1) {
            _name = name;
            _examno = examno;
            _judgediv = judgediv;
            _judgedivname = judgedivName;
            _namespare1 = namespare1;
            _examcourseCd1 = examcourseCd1;
            _examcourseName1 = examcourseName1;
            _examcourseCd2 = examcourseCd2;
            _examcourseName2 = examcourseName2;
            _judgeKindName = judgeKindName;
            _slideFlg = slideFlg;
            _interviewAttendFlg = interviewAttendFlg;
            _shiftDesireFlg = shiftDesireFlg;
            _selectSubclassDiv = selectSubclassDiv;
            _total1 = total1;
            _total2 = total2;
            _totalRank3 = totalRank3;
            _shDiv = shDiv;
            _shDivName = shDivName;
            _shSchoolcd = shSchoolcd;
            _shSchoolname = shSchoolname;
            _sucExamcourseName = sucExamcourseName;
            _sucCourseIsExamCourse1 = sucCourseIsExamCourse1;
        }


        /**
         * 英語とリスニングの合計点を得る
         * @return
         */
        public String getEnglishListeningScore(final String englishSubclassCd, final String listeningSubclassCd) {
            final String scoreEnglish = (String) _testScoreMap.get(englishSubclassCd);
            final String scoreListening = (String) _testScoreMap.get(listeningSubclassCd);
            if (scoreEnglish == null && scoreListening == null) {
                return null;
            }
            final int scoreEnglishInt = null == scoreEnglish || "".equals(scoreEnglish) ? 0 : Integer.parseInt(scoreEnglish);
            final int scoreListeningInt = null == scoreListening || "".equals(scoreListening) ? 0 : Integer.parseInt(scoreListening);
            return String.valueOf(scoreEnglishInt + scoreListeningInt);
        }

        public void addScore(final String testSubclassCd, final String score) {
            _testScoreMap.put(testSubclassCd, score);
        }
    }


    /** パラメータクラス */
    private class Param {
        final String _entexamYear;
        final String _applicantDiv;
        final String _testDiv;
        final String _title;
        final String _subTitle;
        final String _outputdiv;
        final String _dateString;
        final String _schoolName;
        final String _schoolAddress;
        final String _jobName;
        final String _principalName;
        final File _schoolStampFile;
        final String _finschoolCd;
        final String _nDate;
        final boolean _isIppan;
        KNJL322Y.Course _knjl322yCourse = null;
        Map _knjl322yHighSubclassMap = null;
        String _receptCount = null;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamYear = request.getParameter("YEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _title = getApplicantdivName(db2);
            _subTitle = getTestDivName(db2);
            _outputdiv = request.getParameter("OUTPUTDIV");
            final SimpleDateFormat sdf = new SimpleDateFormat("yyyy年M月d日");
            _dateString = sdf.format(Date.valueOf(request.getParameter("LOGIN_DATE")));
            _schoolName = getCertifSchool(db2, "SCHOOL_NAME");
            _schoolAddress = getCertifSchool(db2, "REMARK2");
            _jobName = getCertifSchool(db2, "JOB_NAME");
            _principalName = getCertifSchool(db2, "PRINCIPAL_NAME");

            _schoolStampFile = getSchoolStamp(db2, request.getParameter("DOCUMENTROOT"));
            final String output = request.getParameter("OUTPUT");      // 出力範囲
            _finschoolCd = "2".equals(output) ? request.getParameter("FS_CD") : null; // 指定：学校番号
            _nDate = sdf.format(Date.valueOf(request.getParameter("NDATE").replace('/', '-')));
            _isIppan = ("2".equals(_applicantDiv) && "3".equals(_testDiv)) ? true : false;

            // 学特の平均点表示
            if ("2".equals(_applicantDiv) && "1".equals(_testDiv)) {
                final KNJL329Y.KNJL322Y knjl322y = new KNJL322Y();
                knjl322y._param = knjl322y.createParam(db2, _entexamYear, _applicantDiv, _testDiv);
                _knjl322yCourse = knjl322y.getCourseAverage(db2);
                _knjl322yHighSubclassMap = knjl322y.getHighSubclassMap(_knjl322yCourse);
                _receptCount = getReceptDatCount(db2, _entexamYear, _applicantDiv, _testDiv);
            }
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
            return schoolStampFile.exists() ? schoolStampFile : null;
        }

        private String getReceptDatCount(final DB2UDB db2, final String entexamyear, final String applicantdiv, final String testdiv) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            String count = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT COUNT(*) AS COUNT ");
                stb.append(" FROM ENTEXAM_RECEPT_DAT T1 ");
                stb.append(" WHERE T1.ENTEXAMYEAR = '" + entexamyear + "' ");
                stb.append("   AND T1.APPLICANTDIV = '" + applicantdiv + "' ");
                stb.append("   AND T1.TESTDIV = '" + testdiv + "' ");
                stb.append("   AND VALUE(T1.JUDGEDIV, '') <> '4' "); // 欠席者を除く
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    count = rs.getString("COUNT");
                }
            } catch (SQLException e) {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return count;
        }
    }

    /**
     * KNJL.KNJL322Y (rev. 1.14)をコピーして修正
     */
    public static class KNJL322Y {

        private Param _param;

        private final String TEST_SUBCLASSCD_TOTAL1 = "TOTAL1";
        private final String TEST_SUBCLASSCD_TOTAL2 = "TOTAL2";
        private final String TEST_SUBCLASSCD_TOTAL3 = "TOTAL3";
        private final String TEST_SUBCLASSCD_TOTAL4 = "TOTAL4";
//        private final String TEST_SUBCLASSCD_NAISIN = "NAISIN";
        private final String TEST_SUBCLASSCD_ENGLISH_LISTENING = "ENGLISH_LISTENING";

        private final String SUBCLASS_ALL_COURSECD = "ACOURSECD";
        private final String SUBCLASS_ALL_MAJORCD = "AMAJORCD";
        private final String SUBCLASS_ALL_EXAMCOURSECD = "AEXAMCOURSECD";

        private Map loadExamCourses(final DB2UDB db2, final String testDiv) {
            final Map examCourses = new TreeMap();

            final String courseCd = SUBCLASS_ALL_COURSECD;
            final String majorCd = SUBCLASS_ALL_MAJORCD;
            final String examCourseCd = SUBCLASS_ALL_EXAMCOURSECD;
            final String examCourseName = _param._subTitle;
            final Course course = new Course(courseCd, majorCd, examCourseCd, examCourseName);
            examCourses.put(getCode(testDiv, courseCd, majorCd, examCourseCd), course);
            return examCourses;
        }

        private String getCode(final String testDiv, final String courseCd, final String majorCd, final String examCourseCd) {
            return testDiv + courseCd + majorCd + examCourseCd;
        }

        private Map getHighSubclassMap(final Course course) {
            final Map svfSubclassField = new HashMap();
            svfSubclassField.put("1", "JAPANESE_AVE");
            svfSubclassField.put("2", "MATHEMATICS_AVE");
            svfSubclassField.put("3", "SOCIAL_AVE");
            svfSubclassField.put("4", "SCIENCE_AVE");
            svfSubclassField.put(TEST_SUBCLASSCD_ENGLISH_LISTENING, "ENGLISH1_AVE");
            svfSubclassField.put(TEST_SUBCLASSCD_TOTAL2, "SUBTOTAL_AVE");
            return svfSubclassField;
        }

        private Course getCourseAverage(final DB2UDB db2) {
            final Map examCourses = loadExamCourses(db2, _param._testDiv);
            loadAvg(db2, examCourses, getAvgSql());
            loadAvg(db2, examCourses, getAvgTotalSql());
            return (Course) examCourses.values().iterator().next();
        }

        private void loadAvg(final DB2UDB db2, final Map examCourses, final String sql) {

            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {

                    final String testDiv = rs.getString("TESTDIV");
                    final String courseCd = rs.getString("COURSECD");
                    final String majorCd = rs.getString("MAJORCD");
                    final String examCourseCd = rs.getString("EXAMCOURSECD");

                    final Course course = (Course) examCourses.get(getCode(testDiv, courseCd, majorCd, examCourseCd));
                    if (null == course) {
                        continue; // 読み込んだ指定コースのみ平均点を設定する
                    }

                    final String sum = rs.getString("SUM");
                    final String count = rs.getString("COUNT");
                    if (!NumberUtils.isNumber(sum) || !NumberUtils.isNumber(count)) {
                        continue;
                    }
                    final String testSubclassCd = rs.getString("TESTSUBCLASSCD");
                    final BigDecimal avg  = new BigDecimal(sum).divide(new BigDecimal(count), 1, BigDecimal.ROUND_HALF_UP);
                    course.addAvg(testSubclassCd, avg);
                    log.debug(" course = " + course._examCourseName + " : testsubcl = " + testSubclassCd + " , " + sum + " / " + count + " = " + avg);
                }

            } catch (SQLException ex) {
                log.debug("Exception:", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private String getAvgSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH MAIN AS ( ");
            stb.append(" SELECT  ");
            stb.append("     T1.ENTEXAMYEAR, ");
            stb.append("     T1.APPLICANTDIV, ");
            stb.append("     T1.TESTDIV, ");
            stb.append("     T3.DESIREDIV, ");
            stb.append("     T4.COURSECD, ");
            stb.append("     T4.MAJORCD, ");
            stb.append("     T4.EXAMCOURSECD, ");
            stb.append("     T2.EXAM_TYPE, ");
            stb.append("     T1.EXAMNO, ");
            stb.append("     T2.RECEPTNO ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_APPLICANTDESIRE_DAT T1 ");
            stb.append("     INNER JOIN ENTEXAM_RECEPT_DAT T2 ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
            stb.append("         AND T2.APPLICANTDIV = T1.APPLICANTDIV ");
            stb.append("         AND T2.TESTDIV = T1.TESTDIV ");
            stb.append("         AND T2.EXAM_TYPE = '1' ");
            stb.append("         AND T2.EXAMNO = T1.EXAMNO ");
            stb.append("     LEFT JOIN ENTEXAM_WISHDIV_MST T3 ON T3.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
            stb.append("         AND T3.APPLICANTDIV = T1.APPLICANTDIV ");
            stb.append("         AND T3.TESTDIV = T1.TESTDIV ");
            stb.append("         AND T3.DESIREDIV = T1.DESIREDIV ");
            stb.append("         AND T3.WISHNO = '1' ");
            stb.append("     LEFT JOIN ENTEXAM_COURSE_MST T4 ON T4.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
            stb.append("         AND T4.APPLICANTDIV = T1.APPLICANTDIV ");
            stb.append("         AND T4.TESTDIV = T1.TESTDIV ");
            stb.append("         AND T4.COURSECD = T3.COURSECD ");
            stb.append("         AND T4.MAJORCD = T3.MAJORCD ");
            stb.append("         AND T4.EXAMCOURSECD = T3.EXAMCOURSECD ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT T5 ON T5.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
            stb.append("         AND T5.EXAMNO = T1.EXAMNO ");
            stb.append(" WHERE ");
            stb.append("     T1.ENTEXAMYEAR = '" + _param._entexamYear + "' ");
            stb.append("     AND T1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
            stb.append("     AND VALUE(T2.JUDGEDIV, '') <> '4' ");
            stb.append(" ), SCORES AS ( ");
            // 科目ごと
            stb.append(" SELECT  ");
            stb.append("     T1.TESTDIV, ");
            stb.append("     T1.EXAMNO, ");
            stb.append("     T1.RECEPTNO, ");
            stb.append("     T1.DESIREDIV, ");
            stb.append("     T1.COURSECD, ");
            stb.append("     T1.MAJORCD, ");
            stb.append("     T1.EXAMCOURSECD, ");
            stb.append("     T3.TESTSUBCLASSCD, ");
            stb.append("     T3.SCORE ");
            stb.append(" FROM ");
            stb.append("     MAIN T1 ");
            stb.append("     INNER JOIN ENTEXAM_SCORE_DAT T3 ON T3.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
            stb.append("         AND T3.APPLICANTDIV = T1.APPLICANTDIV ");
            stb.append("         AND T3.TESTDIV = T1.TESTDIV ");
            stb.append("         AND T3.EXAM_TYPE = T1.EXAM_TYPE ");
            stb.append("         AND T3.RECEPTNO = T1.RECEPTNO ");
            stb.append(" WHERE ");
            stb.append("     T3.SCORE IS NOT NULL ");
            if ("2".equals(_param._applicantDiv)) { // 英語・リスニングは別途計算する
                stb.append("     AND T3.TESTSUBCLASSCD NOT IN ('5', '6') ");
            }
            // 高校・英語
            if ("2".equals(_param._applicantDiv)) {
                // 指定入試区分全体の英語 (傾斜配点が"あり"でも500点満点表示 = SCORE2を使用しない)
                stb.append(" UNION ALL ");
                stb.append(" SELECT  ");
                stb.append("     T1.TESTDIV, ");
                stb.append("     T1.EXAMNO, ");
                stb.append("     T1.RECEPTNO, ");
                stb.append("     T1.DESIREDIV, ");
                stb.append("     T1.COURSECD, ");
                stb.append("     T1.MAJORCD, ");
                stb.append("     T1.EXAMCOURSECD, ");
                stb.append("     '" + TEST_SUBCLASSCD_ENGLISH_LISTENING + "' AS TESTSUBCLASSCD, ");
                stb.append("     VALUE(T3.SCORE, 0) + VALUE(T4.SCORE, 0) AS SCORE ");
                stb.append(" FROM ");
                stb.append("     MAIN T1 ");
                stb.append("     LEFT JOIN ENTEXAM_SCORE_DAT T3 ON T3.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
                stb.append("         AND T3.APPLICANTDIV = T1.APPLICANTDIV ");
                stb.append("         AND T3.TESTDIV = T1.TESTDIV ");
                stb.append("         AND T3.EXAM_TYPE = T1.EXAM_TYPE ");
                stb.append("         AND T3.RECEPTNO = T1.RECEPTNO ");
                stb.append("         AND T3.TESTSUBCLASSCD = '5' ");
                stb.append("     LEFT JOIN ENTEXAM_SCORE_DAT T4 ON T4.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
                stb.append("         AND T4.APPLICANTDIV = T1.APPLICANTDIV ");
                stb.append("         AND T4.TESTDIV = T1.TESTDIV ");
                stb.append("         AND T4.EXAM_TYPE = T1.EXAM_TYPE ");
                stb.append("         AND T4.RECEPTNO = T1.RECEPTNO ");
                stb.append("         AND T4.TESTSUBCLASSCD = '6' ");
                stb.append(" WHERE ");
                stb.append("     VALUE(T3.SCORE, -1) <> -1 OR  VALUE(T4.SCORE, -1) <> -1 ");
                // 英語
                stb.append(" UNION ALL ");
                stb.append(" SELECT  ");
                stb.append("     T1.TESTDIV, ");
                stb.append("     T1.EXAMNO, ");
                stb.append("     T1.RECEPTNO, ");
                stb.append("     T1.DESIREDIV, ");
                stb.append("     T1.COURSECD, ");
                stb.append("     T1.MAJORCD, ");
                stb.append("     T1.EXAMCOURSECD, ");
                stb.append("     T3.TESTSUBCLASSCD, ");
                stb.append("     T3.SCORE AS SCORE ");
                stb.append(" FROM ");
                stb.append("     MAIN T1 ");
                stb.append("     INNER JOIN ENTEXAM_SCORE_DAT T3 ON T3.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
                stb.append("         AND T3.APPLICANTDIV = T1.APPLICANTDIV ");
                stb.append("         AND T3.TESTDIV = T1.TESTDIV ");
                stb.append("         AND T3.EXAM_TYPE = T1.EXAM_TYPE ");
                stb.append("         AND T3.RECEPTNO = T1.RECEPTNO ");
                stb.append(" WHERE ");
                stb.append("     T3.TESTSUBCLASSCD = '5' ");
                stb.append("     AND T3.SCORE IS NOT NULL ");
                // リスニング
                stb.append(" UNION ALL ");
                stb.append(" SELECT  ");
                stb.append("     T1.TESTDIV, ");
                stb.append("     T1.EXAMNO, ");
                stb.append("     T1.RECEPTNO, ");
                stb.append("     T1.DESIREDIV, ");
                stb.append("     T1.COURSECD, ");
                stb.append("     T1.MAJORCD, ");
                stb.append("     T1.EXAMCOURSECD, ");
                stb.append("     T3.TESTSUBCLASSCD, ");
                stb.append("     T3.SCORE AS SCORE ");
                stb.append(" FROM ");
                stb.append("     MAIN T1 ");
                stb.append("     INNER JOIN ENTEXAM_SCORE_DAT T3 ON T3.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
                stb.append("         AND T3.APPLICANTDIV = T1.APPLICANTDIV ");
                stb.append("         AND T3.TESTDIV = T1.TESTDIV ");
                stb.append("         AND T3.EXAM_TYPE = T1.EXAM_TYPE ");
                stb.append("         AND T3.RECEPTNO = T1.RECEPTNO ");
                stb.append(" WHERE ");
                stb.append("     T3.TESTSUBCLASSCD = '6' ");
                stb.append("     AND T3.SCORE IS NOT NULL ");
            }
            stb.append(" ) ");
            // テスト区分各コースの各科目ごとの合計
            stb.append(" SELECT ");
            stb.append("     T1.TESTDIV, ");
            stb.append("     T1.COURSECD, ");
            stb.append("     T1.MAJORCD, ");
            stb.append("     T1.EXAMCOURSECD, ");
            stb.append("     T1.TESTSUBCLASSCD, ");
            stb.append("     SUM(SCORE) AS SUM, ");
            stb.append("     COUNT(*) AS COUNT ");
            stb.append(" FROM ");
            stb.append("     SCORES T1 ");
            stb.append(" GROUP BY ");
            stb.append("     T1.TESTDIV, ");
            stb.append("     T1.COURSECD, ");
            stb.append("     T1.MAJORCD, ");
            stb.append("     T1.EXAMCOURSECD, ");
            stb.append("     T1.TESTSUBCLASSCD ");
            // テスト区分全コースの各科目ごとの合計（使用するのは高校のみ）
            stb.append(" UNION ALL ");
            stb.append(" SELECT  ");
            stb.append("     T1.TESTDIV, ");
            stb.append("     '" + SUBCLASS_ALL_COURSECD + "' AS COURSECD, ");
            stb.append("     '" + SUBCLASS_ALL_MAJORCD + "' AS MAJORCD, ");
            stb.append("     '" + SUBCLASS_ALL_EXAMCOURSECD + "' AS EXAMCOURSECD, ");
            stb.append("     T1.TESTSUBCLASSCD, ");
            stb.append("     SUM(SCORE) AS SUM, ");
            stb.append("     COUNT(*) AS COUNT ");
            stb.append(" FROM ");
            stb.append("     SCORES T1 ");
            stb.append(" GROUP BY ");
            stb.append("     T1.TESTDIV,");
            stb.append("     T1.TESTSUBCLASSCD ");
            return stb.toString();
        }

        private String getAvgTotalSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH MAIN AS ( ");
            stb.append(" SELECT  ");
            stb.append("     T1.ENTEXAMYEAR, ");
            stb.append("     T1.APPLICANTDIV, ");
            stb.append("     T1.TESTDIV, ");
            stb.append("     T3.DESIREDIV, ");
            stb.append("     T4.COURSECD, ");
            stb.append("     T4.MAJORCD, ");
            stb.append("     T4.EXAMCOURSECD, ");
            stb.append("     T2.EXAM_TYPE, ");
            stb.append("     T1.EXAMNO, ");
            stb.append("     T2.RECEPTNO, ");
            stb.append("     T2.ATTEND_ALL_FLG, ");
            stb.append("     T2.TOTAL1, ");
            stb.append("     T2.TOTAL2, ");
            stb.append("     T2.TOTAL3, ");
            stb.append("     T2.TOTAL4, ");
            stb.append("     T5.AVERAGE_ALL ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_APPLICANTDESIRE_DAT T1 ");
            stb.append("     INNER JOIN ENTEXAM_RECEPT_DAT T2 ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
            stb.append("         AND T2.APPLICANTDIV = T1.APPLICANTDIV ");
            stb.append("         AND T2.TESTDIV = T1.TESTDIV ");
            stb.append("         AND T2.EXAM_TYPE = '1' ");
            stb.append("         AND T2.EXAMNO = T1.EXAMNO ");
            stb.append("     LEFT JOIN ENTEXAM_WISHDIV_MST T3 ON T3.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
            stb.append("         AND T3.APPLICANTDIV = T1.APPLICANTDIV ");
            stb.append("         AND T3.TESTDIV = T1.TESTDIV ");
            stb.append("         AND T3.DESIREDIV = T1.DESIREDIV ");
            stb.append("         AND T3.WISHNO = '1' ");
            stb.append("     LEFT JOIN ENTEXAM_COURSE_MST T4 ON T4.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
            stb.append("         AND T4.APPLICANTDIV = T1.APPLICANTDIV ");
            stb.append("         AND T4.TESTDIV = T1.TESTDIV ");
            stb.append("         AND T4.COURSECD = T3.COURSECD ");
            stb.append("         AND T4.MAJORCD = T3.MAJORCD ");
            stb.append("         AND T4.EXAMCOURSECD = T3.EXAMCOURSECD ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT T5 ON T5.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
            stb.append("         AND T5.EXAMNO = T1.EXAMNO ");
            stb.append(" WHERE ");
            stb.append("     T1.ENTEXAMYEAR = '" + _param._entexamYear + "' ");
            stb.append("     AND T1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
            stb.append("     AND VALUE(T2.JUDGEDIV, '') <> '4' ");
            stb.append(" ), SCORES AS ( ");
            // 合計点1
            stb.append(" SELECT  ");
            stb.append("     T1.TESTDIV, ");
            stb.append("     T1.EXAMNO, ");
            stb.append("     T1.RECEPTNO,     ");
            stb.append("     T1.DESIREDIV, ");
            stb.append("     T1.COURSECD, ");
            stb.append("     T1.MAJORCD, ");
            stb.append("     T1.EXAMCOURSECD, ");
            stb.append("     '" + TEST_SUBCLASSCD_TOTAL1 + "' AS TESTSUBCLASSCD, ");
            stb.append("     T1.TOTAL1 AS SCORE, ");
            stb.append("     T1.ATTEND_ALL_FLG");
            stb.append(" FROM ");
            stb.append("     MAIN T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.TOTAL1 IS NOT NULL ");
            stb.append("     AND T1.ATTEND_ALL_FLG = '1' ");
            // 合計点2
            stb.append(" UNION ALL ");
            stb.append(" SELECT  ");
            stb.append("     T1.TESTDIV, ");
            stb.append("     T1.EXAMNO, ");
            stb.append("     T1.RECEPTNO,     ");
            stb.append("     T1.DESIREDIV, ");
            stb.append("     T1.COURSECD, ");
            stb.append("     T1.MAJORCD, ");
            stb.append("     T1.EXAMCOURSECD, ");
            stb.append("     '" + TEST_SUBCLASSCD_TOTAL2 + "' AS TESTSUBCLASSCD, ");
            stb.append("     T1.TOTAL2 AS SCORE, ");
            stb.append("     T1.ATTEND_ALL_FLG");
            stb.append(" FROM ");
            stb.append("     MAIN T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.TOTAL2 IS NOT NULL ");
            stb.append("     AND T1.ATTEND_ALL_FLG = '1' ");
            // 合計点3
            stb.append(" UNION ALL ");
            stb.append(" SELECT  ");
            stb.append("     T1.TESTDIV, ");
            stb.append("     T1.EXAMNO, ");
            stb.append("     T1.RECEPTNO,     ");
            stb.append("     T1.DESIREDIV, ");
            stb.append("     T1.COURSECD, ");
            stb.append("     T1.MAJORCD, ");
            stb.append("     T1.EXAMCOURSECD, ");
            stb.append("     '" + TEST_SUBCLASSCD_TOTAL3 + "' AS TESTSUBCLASSCD, ");
            stb.append("     T1.TOTAL3 AS SCORE, ");
            stb.append("     T1.ATTEND_ALL_FLG");
            stb.append(" FROM ");
            stb.append("     MAIN T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.TOTAL3 IS NOT NULL ");
            stb.append("     AND T1.ATTEND_ALL_FLG = '1' ");
            // 合計点4
            stb.append(" UNION ALL ");
            stb.append(" SELECT  ");
            stb.append("     T1.TESTDIV, ");
            stb.append("     T1.EXAMNO, ");
            stb.append("     T1.RECEPTNO,     ");
            stb.append("     T1.DESIREDIV, ");
            stb.append("     T1.COURSECD, ");
            stb.append("     T1.MAJORCD, ");
            stb.append("     T1.EXAMCOURSECD, ");
            stb.append("     '" + TEST_SUBCLASSCD_TOTAL4 + "' AS TESTSUBCLASSCD, ");
            stb.append("     T1.TOTAL4 AS SCORE, ");
            stb.append("     T1.ATTEND_ALL_FLG");
            stb.append(" FROM ");
            stb.append("     MAIN T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.TOTAL4 IS NOT NULL ");
            stb.append("     AND T1.ATTEND_ALL_FLG = '1' ");
//            // 内申点
//            stb.append(" UNION ALL ");
//            stb.append(" SELECT  ");
//            stb.append("     T1.TESTDIV, ");
//            stb.append("     T1.EXAMNO, ");
//            stb.append("     T1.RECEPTNO,     ");
//            stb.append("     T1.DESIREDIV, ");
//            stb.append("     T1.COURSECD, ");
//            stb.append("     T1.MAJORCD, ");
//            stb.append("     T1.EXAMCOURSECD, ");
//            stb.append("     '" + TEST_SUBCLASSCD_NAISIN + "' AS TESTSUBCLASSCD, ");
//            stb.append("     T1.AVERAGE_ALL AS SCORE, ");
//            stb.append("     T1.ATTEND_ALL_FLG");
//            stb.append(" FROM ");
//            stb.append("     MAIN T1 ");
//            stb.append(" WHERE ");
//            stb.append("     T1.AVERAGE_ALL IS NOT NULL ");
            stb.append(" ) ");

            stb.append(" SELECT ");
            stb.append("     T1.TESTDIV, ");
            stb.append("     T1.COURSECD, ");
            stb.append("     T1.MAJORCD, ");
            stb.append("     T1.EXAMCOURSECD, ");
            stb.append("     T1.TESTSUBCLASSCD, ");
            stb.append("     SUM(SCORE) AS SUM, ");
            stb.append("     COUNT(*) AS COUNT ");
            stb.append(" FROM ");
            stb.append("     SCORES T1 ");
            stb.append(" GROUP BY ");
            stb.append("     T1.TESTDIV, ");
            stb.append("     T1.COURSECD, ");
            stb.append("     T1.MAJORCD, ");
            stb.append("     T1.EXAMCOURSECD, ");
            stb.append("     T1.TESTSUBCLASSCD ");
            if ("2".equals(_param._applicantDiv)) {
                // 合計点1
                stb.append(" UNION ALL ");
                stb.append(" SELECT  ");
                stb.append("     T1.TESTDIV, ");
                stb.append("     '" + SUBCLASS_ALL_COURSECD + "' AS COURSECD, ");
                stb.append("     '" + SUBCLASS_ALL_MAJORCD + "' AS MAJORCD, ");
                stb.append("     '" + SUBCLASS_ALL_EXAMCOURSECD + "' AS EXAMCOURSECD, ");
                stb.append("     T1.TESTSUBCLASSCD, ");
                stb.append("     SUM(SCORE) AS SUM, ");
                stb.append("     COUNT(*) AS COUNT ");
                stb.append(" FROM ");
                stb.append("     SCORES T1 ");
                stb.append(" GROUP BY ");
                stb.append("     T1.TESTDIV,");
                stb.append("     T1.TESTSUBCLASSCD ");
            }
            return stb.toString();
        }

        /** パラメータ取得処理 */
        private Param createParam(final DB2UDB db2, final String year, final String applicantdiv, final String testdiv) {
            final Param param = new Param(db2, year, applicantdiv, testdiv);
            //log.fatal("KNJL322Y.Revision: 1.14");
            return param;
        }

        private class Course {
            final String _courseCd;
            final String _majorCd;
            final String _examCourseCd;
            final String _examCourseName;
            final Map _avgs = new HashMap();
            public Course(final String courseCd, final String majorCd, final String examCourseCd, final String examCourseName) {
                _courseCd = courseCd;
                _majorCd = majorCd;
                _examCourseCd = examCourseCd;
                _examCourseName = examCourseName;
            }
            public void addAvg(final String testSubclassCd, final BigDecimal avg) {
                _avgs.put(testSubclassCd, avg);
            }
            public BigDecimal getAvg(final String testSubclassCd) {
                return (BigDecimal) _avgs.get(testSubclassCd);
            }
            public String toString() {
                return "[" + _courseCd + ":"+  _majorCd + ":" + _examCourseCd + ":" + _examCourseName + "]";
            }
        }

        /** パラメータクラス */
        private static class Param {
            final String _entexamYear;
            final String _applicantDiv;
            final String _testDiv;
            final String _subTitle;
            final Map _testDivNames;

            Param(final DB2UDB db2, final String year, final String applicantdiv, final String testdiv) {
                _entexamYear = year;
                _applicantDiv = applicantdiv;
                _testDiv = testdiv;
                _testDivNames = getTestDivNames(db2);
                _subTitle = (String) _testDivNames.get(_testDiv);
            }

            private Map getTestDivNames(DB2UDB db2) {
                Map map = new HashMap();
                PreparedStatement ps = null;
                ResultSet rs = null;
                final String namecd1 = "1".equals(_applicantDiv) ? "L024" : "L004";
                try {
                    ps = db2.prepareStatement(" SELECT NAMECD2, NAME1 FROM NAME_MST WHERE NAMECD1 = '" + namecd1 + "' ");
                    rs = ps.executeQuery();
                    while (rs.next() && null != rs.getString("NAME1")) {
                        final String testDiv = rs.getString("NAMECD2");
                        final String testDivName = rs.getString("NAME1");
                        map.put(testDiv, testDivName);
                    }
                } catch (SQLException e) {
                    log.error("exception!", e);
                } finally {
                    DbUtils.closeQuietly(null, ps, rs);
                    db2.commit();
                }
                return map;
            }
        }
    }

    private void printMainKNJL327Y(final DB2UDB db2, final Vrw32alp svf, int flg) {

        final List applicants = ApplicantKNJL327Y.getApplicants(db2, _param, flg);
        try {
            for (final Iterator it = applicants.iterator(); it.hasNext(); ) {
                final ApplicantKNJL327Y applicant = (ApplicantKNJL327Y) it.next();

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
                final String form;
                //学特入試で進学コース合格者の場合、特進チャレンジ制度の文章ありフォームを使用
                // (一般入試「希望する」フラグを使用していないので希望していない志願者にも特進チャレンジ生徒の文章が表示される。学特はほぼ一般入試希望するから?)
                if ("1".equals(_param._testDiv) && (("2".equals(applicant._desirediv) || "4".equals(applicant._desirediv)) && "1".equals(applicant._judgediv) || "3".equals(applicant._judgediv) || "5".equals(applicant._judgediv) && (!isTokubetsuHanteiGoukakuShibouCourse1 || "S".equals(applicant._sucCoursemark)))) {
                    form = useSlideForm ? "KNJL327Y_2_2.frm" : "KNJL327Y_1_2.frm";
                } else if ("3".equals(_param._testDiv) && "1".equals(applicant._selectSubclassDiv)) {
                    //一般入試においての特進チャレンジ者の合否通知
                    form = useSlideForm ? "KNJL327Y_2_3.frm" : "KNJL327Y_1_3.frm";
                } else {
                    form = useSlideForm ? "KNJL327Y_2.frm" : "KNJL327Y_1.frm";
                }
                svf.VrSetForm(form, 1);

                svf.VrsOut("NENDO",      _param._entexamYear + "年度");
                svf.VrsOut("DATE",       _param._nDate);
                svf.VrsOut("TITLE",      _param._title);
                svf.VrsOut("TESTDIV" ,   _param._subTitle);
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
                if ("3".equals(_param._testDiv) && "1".equals(applicant._selectSubclassDiv)) {
                    //一般入試においての特進チャレンジ者の合否通知
                    judge = applicant._judgedivname;
                } else if (null != applicant._judgeKind) { // 特奨
                    judge = applicant._judgeKindName;
                } else {
                    if (applicant._namespare1 == 1 && "1".equals(applicant._shiftDesireFlg) && "5".equals(applicant._judgediv)) {
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

    private static class ApplicantKNJL327Y {
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
        final String _desirediv;
        public ApplicantKNJL327Y(
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
                final String desirediv) {
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
            _desirediv = desirediv;
        }

        private static List getApplicants(final DB2UDB db2, final Param _param, final int judge_flg) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            final List applicants = new ArrayList();
            try {
                final String sql = getApplicantSql(_param, judge_flg);
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
                    final String sportsFlg = rs.getString("SPORTS_FLG");
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
                    final String desirediv = rs.getString("DESIREDIV");

                    final ApplicantKNJL327Y applicant = new ApplicantKNJL327Y(examno, name, judgediv, judgedivName, namespare1, slideFlg, sportsFlg, shiftDesireFlg, selectSubclassDiv,
                            examcourseName1, examcourseCd1, examcourseName2, examcourseCd2, sucCourseIsExamCourse1, sucExamcourseName, sucCoursemark, judgeKind, judgeKindName, finschoolName, desirediv);
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

        private static String getApplicantSql(final Param _param, final int judge_flg) {
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
            stb.append("     ,T3.DESIREDIV ");
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
            stb.append(" WHERE ");
            stb.append("     T1.ENTEXAMYEAR = '" + _param._entexamYear + "' ");
            stb.append("     AND T1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
            stb.append("     AND T1.TESTDIV = '" + _param._testDiv + "' ");
            stb.append("     AND T1.EXAM_TYPE = '1' ");
            stb.append("     AND VALUE(T1.JUDGEDIV, '') <> '4' ");
            if (0 == judge_flg) {
                stb.append("     AND T1.JUDGEDIV IS NOT NULL ");
            } else if (1 == judge_flg) {
                stb.append("     AND NML013.NAMESPARE1 = '1' ");
            } else if (2 == judge_flg) {
                stb.append("     AND VALUE(NML013.NAMESPARE1, '') <> '1' ");
            }
            if (null != _param._finschoolCd) {
                stb.append("     AND T9.FS_CD = '" + _param._finschoolCd + "' ");
            }
//            if (3 == judge_flg) {
//                stb.append("     AND T9.JUDGE_KIND IS NOT NULL ");
//            }
            stb.append(" ORDER BY ");
            stb.append("     T9.FS_CD, T1.EXAMNO ");
            return stb.toString();
        }
    }

}

// eof
