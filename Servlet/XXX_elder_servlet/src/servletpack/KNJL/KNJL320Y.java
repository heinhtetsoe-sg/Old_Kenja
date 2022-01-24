/*
 * $Id: 904a64ef5d12e18b0b56d3d7ea8befae8deefaf3 $
 *
 * 作成日: 2010/11/01
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditEdit;

/**
 *
 *  学校教育システム 賢者 [入試管理]
 *
 *                  ＜ＫＮＪＬ３２０Ｙ＞  判定資料
 **/
public class KNJL320Y {
    
    private static final Log log = LogFactory.getLog(KNJL320Y.class);
    
    private boolean _hasData;
    
    Param _param;
    
    private final String SUBCLASSCD_1   = "1";
    private final String SUBCLASSCD_2   = "2";
    private final String SUBCLASSCD_3   = "3";
    private final String SUBCLASSCD_4   = "4";
    private final String SUBCLASSCD_5   = "5";
    private final String SUBCLASSCD_6   = "6";
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
            
            if ("1".equals(_param._applicantDiv)) { // 中学
                printMain(db2, svf, false, 0);
            } else if ("2".equals(_param._applicantDiv)) {
                if ("2".equals(_param._special)) {
                    printMain(db2, svf, true, 0); // スポーツ特奨
                } else if ("2".equals(_param._testDiv)) { // 入試区分 = 推薦
                    printMain(db2, svf, false, 1); // 推薦種別 = 英検以外
                    printMain(db2, svf, false, 2); // 推薦種別 = 英検
                } else {
                    printMain(db2, svf, false, 0); // 推薦種別 = 指定無
                }
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
        if (null != str) {
            try {
                count = str.getBytes("MS932").length;
            } catch (Exception e) {
                log.error(e);
            }
        }
        return count;
    }
    
    private void printMain(final DB2UDB db2, final Vrw32alp svf, final boolean isSports, int suisenEikenFlg) {
        
        String form = null;
        int MAX_LINE = 0;
        if ("1".equals(_param._applicantDiv)) {
            form = "KNJL320Y_1.frm"; // 中学用
            MAX_LINE = 20;
        } else if ("2".equals(_param._applicantDiv)) {
            if (isSports) {
                form = "KNJL320Y_3.frm"; // スポーツ特別奨学生
                MAX_LINE = 20;
            } else {
                form = "KNJL320Y_2.frm"; // 高校用
                MAX_LINE = 50;
            }
        }
        svf.VrSetForm(form, 4);
        
        final List applicants = getApplicants(db2, isSports, suisenEikenFlg);
        final String maxPage = String.valueOf(getMaxPage(applicants, MAX_LINE));
        int page = 1;
        int line = 1;
        int count = 1;
        for (final Iterator it = applicants.iterator(); it.hasNext();) {
            final Applicant appl = (Applicant) it.next();
            
            if (MAX_LINE < line) {
                page += 1;
                line = 1;
            }
            
            svf.VrsOut("NENDO", _param._entexamYear + "年度");
            svf.VrsOut("TITLE", _param._title);
            svf.VrsOut("SUBTITLE", _param._subTitle);
            final String coursename = "9".equals(_param._desireDiv) ? "全て" : appl._coursename1;
            svf.VrsOut("SORT", (coursename == null ? "" : coursename) + " / " + ("1".equals(_param._sort) ? "成績順" : "受験番号順"));
            svf.VrsOut("DATE", _param._dateString);
            svf.VrsOut("PAGE1", String.valueOf(page));
            svf.VrsOut("PAGE2", maxPage);
            
            final String no;
            if ("1".equals(_param._sort)) {
                if ("1".equals(appl._attendAllFlg)) {
                    no = appl._rank;
                } else {
                    no = null;
                }
            } else {
                no = String.valueOf(count);
            }
            svf.VrsOut("NO", no);
            svf.VrsOut("EXAMNO", appl._examno);
            if ("1".equals(_param._outputName)) {
                int ms932count = getMS932count(appl._name);
                svf.VrsOut("NAME" + (30 < ms932count ? "3" : 20 < ms932count ? "2" : "1"), appl._name);
            }
            svf.VrsOut("SEX", appl._sexname);
            
            if ("1".equals(_param._applicantDiv)) {
                final Map testSubclassCds = new HashMap();
                testSubclassCds.put("JAPANESE",   SUBCLASSCD_1);
                testSubclassCds.put("ARITHMETIC", SUBCLASSCD_2);
                for (Iterator itSubclass = testSubclassCds.keySet().iterator(); itSubclass.hasNext(); ) {
                    final String field = (String) itSubclass.next();
                    final String testsubclasscd = (String) testSubclassCds.get(field);
                    final TestScore testScore = (TestScore) appl._testScoreMap.get(testsubclasscd);
                    if (null != testScore) {
                        svf.VrsOut(field, testScore._score);
                    }
                }
                final TestScore testScore2 = (TestScore) appl._testScoreMap.get(SUBCLASSCD_2);
                if (null != testScore2) {
                    svf.VrsOut("CALC", testScore2._score3);
                }
                
                svf.VrsOut("ABSENCE", appl._absenceDays);
                svf.VrsOut("INTERVIEW", appl._interviewValue);
                svfOutRenban(svf, KNJ_EditEdit.get_token(appl._remark1, 40, 4), "REMARK1_");
                svfOutRenban(svf, KNJ_EditEdit.get_token(appl._remark2, 40, 2), "REMARK2_");
                svfOutRenban(svf, KNJ_EditEdit.get_token(appl._interviewRemark, 70, 2), "INTERVIEW_REMARK");
                svf.VrsOut("JUDGE", appl._judgedivname);
            } else if ("2".equals(_param._applicantDiv)) {
                if (isSports) {
                    svf.VrsOut("DESIRE", appl._shdivname);
                    svf.VrsOut("FINSCHOOL_ABBV", appl._finschoolNameAbbv);
                    svfOutRenban(svf, KNJ_EditEdit.get_token(appl._activity, 20, 2), "ACTIVITY");
                    svfOutRenban(svf, KNJ_EditEdit.get_token(appl._results, 80, 2), "RESULT");
                    svfOutRenban(svf, KNJ_EditEdit.get_token(appl._section, 20, 2), "SECTION");
                    svf.VrsOut("CONFRPT", appl._averageAll);
                    svf.VrsOut("EXAMCOURSE_NAME", appl._coursename1);
                    svf.VrsOut("JUDGENAME", appl._judgekindname);
                } else {
                    final Map testSubclassCds = new HashMap();
                    testSubclassCds.put("JAPANESE",    SUBCLASSCD_1);
                    testSubclassCds.put("MATHEMATICS", SUBCLASSCD_2);
                    testSubclassCds.put("SOCIAL",      SUBCLASSCD_3);
                    testSubclassCds.put("SCIENCE",     SUBCLASSCD_4);
                    testSubclassCds.put("ENGLISH1",    SUBCLASSCD_5);
                    testSubclassCds.put("LISTENING",   SUBCLASSCD_6);
                        
                    for (Iterator itSubclass = testSubclassCds.keySet().iterator(); itSubclass.hasNext(); ) {
                        final String field = (String) itSubclass.next();
                        final String testsubclasscd = (String) testSubclassCds.get(field);
                        final TestScore testScore = (TestScore) appl._testScoreMap.get(testsubclasscd);
                        if (null != testScore) {
                            final String score = "1".equals(_param._rateDiv) ? testScore._score2 : testScore._score;
                            svf.VrsOut(field, score);
                        }
                    }
                    final TestScore testScore9 = (TestScore) appl._testScoreMap.get(SUBCLASSCD_9);
                    if (null != testScore9) {
                        svf.VrsOut("KASANTEN", testScore9._score);
                    }
                    svf.VrsOut("FINSCHOOL_ABBV", appl._finschoolNameAbbv);
                    if ("1".equals(appl._attendAllFlg)) {
                        String subtotal = "";
                        String total1 = "";
                        final boolean isIppan = "3".equals(_param._testDiv);
                        if ("1".equals(_param._rateDiv)) { // 傾斜出力あり
                            if (isIppan) {
                                subtotal = appl._total3;
                                total1 = appl._total4;
                            } else {
                                subtotal = appl._total4;
                                total1 = appl._total3;
                            }
                        } else if ("2".equals(_param._rateDiv)){
                            if (isIppan) {
                                subtotal = appl._total1;
                                total1 = appl._total2;
                            } else {
                                subtotal = appl._total2;
                                total1 = appl._total1;
                            }
                        }
                        svf.VrsOut("SUBTOTAL", subtotal);
                        svf.VrsOut("TOTAL1", total1);
                    }
                    svf.VrsOut("CONFRPT", appl._averageAll);
                    svf.VrsOut("DESIRE", appl._shdivname);
                    svf.VrsOut("EXAMCOURSE_NAME2", appl._courseabbv2);
                    svf.VrsOut("SH_SCHOOL", appl._shSchoolName);
                    svf.VrsOut("ABSENCE", appl._absenceDays);
                    if ("1".equals(_param._outputRemark1)) {
                        if (getMS932count(appl._remark1) <= 40 * 2) {
                            svfOutRenban(svf, KNJ_EditEdit.get_token(appl._remark1, 40, 2), "REMARK2_");
                        } else {
                            svfOutRenban(svf, KNJ_EditEdit.get_token(appl._remark1, 40, 4), "REMARK1_");
                        }
                    }
                    svf.VrsOut("INTERVIEW", appl._interviewValue);
                    svf.VrsOut("SPECIAL", appl._judgekindname);
                }
            }
            _hasData = true;
            
            svf.VrEndRecord();
            line += 1;
            count += 1;
        }
    }

    private void svfOutRenban(final Vrw32alp svf, final String[] arr, final String field) {
        if (null != arr) {
            for (int i = 0; i < arr.length; i++) {
                svf.VrsOut(field + String.valueOf(i + 1), arr[i]);
            }
        }
    }
    
    private int getMaxPage(final List applicants, final int MAX_LINE) {
        final int p = applicants.size() / MAX_LINE;
        return (applicants.size() % MAX_LINE == 0 ? p : p + 1);
    }
    
    private List getApplicants(final DB2UDB db2, final boolean isSports, final int suisenEikenFlg) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        final List applicants = new ArrayList();
        final Map receptnoMap = new HashMap();
        try {
            final String sql = getApplicantSql(isSports, suisenEikenFlg);
//             log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            
            while (rs.next()) {
                
                final String absenceDays = formatKeta(rs.getString("ABSENCE_DAYS")) + "," + formatKeta(rs.getString("ABSENCE_DAYS2")) + "," + formatKeta(rs.getString("ABSENCE_DAYS3"));
                final String averageAll = NumberUtils.isNumber(rs.getString("AVERAGE_ALL")) ? String.valueOf(new BigDecimal(rs.getString("AVERAGE_ALL")).setScale(0)) : null;
                
                final Applicant applicant = new Applicant(
                        rs.getString("EXAMNO"),
                        rs.getString("NAME"),
                        rs.getString("SEXNAME"),
                        rs.getString("INTERVIEW_VALUE"),
                        rs.getString("INTERVIEW_REMARK"),
                        rs.getString("JUDGEDIV"),
                        rs.getString("JUDGEDIVNAME"),
                        absenceDays,
                        rs.getString("REMARK1"),
                        rs.getString("REMARK2"),
                        rs.getString("FINSCHOOL_NAME_ABBV"),
                        rs.getString("KASANTEN_ALL"),
                        rs.getString("TOTAL1"),
                        rs.getString("TOTAL2"),
                        rs.getString("TOTAL3"),
                        rs.getString("TOTAL4"),
                        averageAll,
                        rs.getString("SHDIVNAME"),
                        rs.getString("RECOM_KIND"),
                        rs.getString("RECOM_KINDNAME"),
                        rs.getString("EXAMCOURSECD"),
                        rs.getString("COURSENAME1"),
                        rs.getString("COURSEABBV1"),
                        rs.getString("COURSENAME2"),
                        rs.getString("COURSEABBV2"),
                        rs.getString("SH_SCHOOL_NAME"),
                        rs.getString("JUDGEKINDNAME"),
                        rs.getString("JUDGEKINDABBV"),
                        rs.getString("ATTEND_ALL_FLG"),
                        rs.getString("ACTIVITY"),
                        rs.getString("RESULTS"),
                        rs.getString("SECTION"),
                        rs.getString("RANK"));
                applicants.add(applicant);
                receptnoMap.put(rs.getString("RECEPTNO"), applicant);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        
        try {
            final String sql = getScoreSql();
//            log.debug(" score sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            
            while (rs.next()) {
                final String testsubclasscd = rs.getString("TESTSUBCLASSCD");
                final String score = rs.getString("SCORE");
                final String score2 = rs.getString("SCORE2");
                final String score3 = rs.getString("SCORE3");
                Applicant applicant = (Applicant) receptnoMap.get(rs.getString("RECEPTNO"));
                if (null == applicant) {
                    continue;
                }
                applicant.addScore(testsubclasscd, new TestScore(score, score2, score3));
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return applicants;
    }
    
    private String formatKeta(final String absenceDays) {
        if (!NumberUtils.isDigits(absenceDays)) {
            return "   ";
        }
        final int absenceDaysInt = Integer.parseInt(absenceDays);
        return (absenceDaysInt < 10 ? "  " : (absenceDaysInt < 100 ? " " : "")) + String.valueOf(absenceDaysInt); 
    }
    
    private static class Applicant {
        final String _examno;
        final String _name;
        final String _sexname;
        final String _interviewValue;
        final String _interviewRemark;
        final String _judgediv;
        final String _judgedivname;
        final String _absenceDays;
        final String _remark1;
        final String _remark2;
        final String _finschoolNameAbbv;
        final String _kasantenAll;
        final String _total1;
        final String _total2;
        final String _total3;
        final String _total4;
        final String _averageAll;
        final String _shdivname;
        final String _recomKind;
        final String _recomKindName;
        final String _coursecd;
        final String _coursename1;
        final String _courseabbv1;
        final String _coursename2;
        final String _courseabbv2;
        final String _shSchoolName;
        final String _judgekindname;
        final String _judgekindAbbv;
        final String _attendAllFlg;
        final String _activity;
        final String _results;
        final String _section;
        final String _rank;
        final Map _testScoreMap = new HashMap();
        
        Applicant(
                final String examno,
                final String name,
                final String sexname,
                final String interviewValue,
                final String interviewRemark,
                final String judgediv,
                final String judgedivname,
                final String absenceDaysTotal,
                final String remark1,
                final String remark2,
                final String finschoolNameAbbv,
                final String kasantenAll,
                final String total1,
                final String total2,
                final String total3,
                final String total4,
                final String averageAll,
                final String shdivname,
                final String recomKind,
                final String recomKindName,
                final String coursecd,
                final String coursename1,
                final String courseabbv1,
                final String coursename2,
                final String courseabbv2,
                final String shSchoolName,
                final String judgekindname,
                final String judgekindAbbv,
                final String attendAllFlg,
                final String activity,
                final String results,
                final String section,
                final String rank
        ) {
            _examno = examno;
            _name = name;
            _sexname = sexname;
            _interviewValue = interviewValue;
            _interviewRemark = interviewRemark;
            _judgediv = judgediv;
            _judgedivname = judgedivname;
            _absenceDays = absenceDaysTotal;
            _remark1 = remark1;
            _remark2 = remark2;
            _finschoolNameAbbv = finschoolNameAbbv;
            _kasantenAll = kasantenAll;
            _total1 = total1;
            _total2 = total2;
            _total3 = total3;
            _total4 = total4;
            _averageAll = averageAll;
            _shdivname = shdivname;
            _recomKind = recomKind;
            _recomKindName = recomKindName;
            _coursecd = coursecd;
            _coursename1 = coursename1;
            _courseabbv1 = courseabbv1;
            _coursename2 = coursename2;
            _courseabbv2 = courseabbv2;
            _shSchoolName = shSchoolName;
            _judgekindname = judgekindname;
            _judgekindAbbv = judgekindAbbv;
            _attendAllFlg = attendAllFlg;
            _activity = activity;
            _results = results;
            _section = section;
            _rank = rank;
        }
        
        public void addScore(final String testSubclassCd, final TestScore testScore) {
            _testScoreMap.put(testSubclassCd, testScore);
        }
    }
    
    private String getApplicantSql(boolean isSports, int suisenEikenFlg) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH MAIN AS (");
        stb.append(" SELECT ");
        stb.append("     T1.ENTEXAMYEAR, ");
        stb.append("     T1.APPLICANTDIV, ");
        stb.append("     T2.TESTDIV, ");
        stb.append("     T1.EXAMNO, ");
        stb.append("     T2.RECEPTNO, ");
        stb.append("     T1.NAME, ");
        stb.append("     NMZ002.NAME2 AS SEXNAME, ");
        stb.append("     T11.INTERVIEW_VALUE, ");
        stb.append("     T11.INTERVIEW_REMARK, ");
        stb.append("     T2.JUDGEDIV, ");
        stb.append("     NML013.NAME1 AS JUDGEDIVNAME, ");
        stb.append("     T4.ABSENCE_DAYS AS ABSENCE_DAYS, ");
        stb.append("     T4.ABSENCE_DAYS2 AS ABSENCE_DAYS2, ");
        stb.append("     T4.ABSENCE_DAYS3 AS ABSENCE_DAYS3, ");
        stb.append("     T1.SPORTS_FLG, ");
        stb.append("     T1.REMARK1, ");
        stb.append("     T1.REMARK2, ");
        stb.append("     T5.FINSCHOOL_NAME_ABBV, ");
        stb.append("     T4.KASANTEN_ALL, ");
        stb.append("     T2.TOTAL1, ");
        stb.append("     T2.TOTAL2, ");
        stb.append("     T2.TOTAL3, ");
        stb.append("     T2.TOTAL4, ");
        stb.append("     T4.AVERAGE_ALL, ");
        stb.append("     NML006.NAME1 AS SHDIVNAME, ");
        stb.append("     T6.RECOM_KIND, ");
        stb.append("     NML023.NAME1 AS RECOM_KINDNAME, ");
        stb.append("     T71.EXAMCOURSECD, ");
        stb.append("     T81.EXAMCOURSE_NAME AS COURSENAME1, ");
        stb.append("     T81.EXAMCOURSE_ABBV AS COURSEABBV1, ");
        stb.append("     (CASE WHEN T1.TESTDIV = '" + _param._testDiv + "' AND VALUE(T1.SLIDE_FLG, '') = '1' THEN T82.EXAMCOURSE_NAME ELSE '' END) AS COURSENAME2, ");
        stb.append("     (CASE WHEN T1.TESTDIV = '" + _param._testDiv + "' AND VALUE(T1.SLIDE_FLG, '') = '1' THEN T82.EXAMCOURSE_ABBV ELSE '' END) AS COURSEABBV2, ");
        stb.append("     T9.FINSCHOOL_NAME AS SH_SCHOOL_NAME, ");
        stb.append("     NML025.NAME1 AS JUDGEKINDNAME, ");
        stb.append("     NML025.ABBV1 AS JUDGEKINDABBV, ");
        stb.append("     T2.ATTEND_ALL_FLG, ");
        stb.append("     T10.ACTIVITY, ");
        stb.append("     T10.RESULTS, ");
        stb.append("     T10.SECTION, ");
        if ("2".equals(_param._applicantDiv)) {
            if ("2".equals(_param._testDiv)) {
                stb.append("     RANK() OVER(PARTITION BY VALUE(T2.ATTEND_ALL_FLG, '0') ORDER BY VALUE(T4.AVERAGE_ALL, -1) DESC) AS RANK ");
            } else {
                final String field;
                if ("3".equals(_param._testDiv)) {
                    if ("1".equals(_param._rateDiv)) {
                        field = " T2.TOTAL4 ";
                    } else {
                        field = " T2.TOTAL2 ";
                    }
                } else {
                    if ("1".equals(_param._rateDiv)) {
                        field = " T2.TOTAL3 ";
                    } else {
                        field = " T2.TOTAL1 ";
                    }
                }
                stb.append("     RANK() OVER(PARTITION BY VALUE(T2.ATTEND_ALL_FLG, '0') ORDER BY VALUE(" + field + ", -1) DESC) AS RANK ");
            }
        } else if ("1".equals(_param._applicantDiv)) {
            stb.append("     RANK() OVER(PARTITION BY VALUE(T2.ATTEND_ALL_FLG, '0') ORDER BY VALUE(T2.TOTAL2, -1) DESC) AS RANK ");
        }
        stb.append(" FROM ");
        stb.append("     ENTEXAM_APPLICANTBASE_DAT T1 ");
        stb.append("     INNER JOIN ENTEXAM_RECEPT_DAT T2 ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("         AND T2.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("         AND T2.EXAMNO = T1.EXAMNO ");
        stb.append("         AND T2.EXAM_TYPE = '1' ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT T4 ON T4.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("         AND T4.EXAMNO = T1.EXAMNO ");
        stb.append("     LEFT JOIN FINSCHOOL_MST T5 ON T5.FINSCHOOLCD = T1.FS_CD ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTDESIRE_DAT T6 ON T6.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("         AND T6.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("         AND T6.TESTDIV = T2.TESTDIV ");
        stb.append("         AND T6.EXAMNO = T1.EXAMNO ");
        stb.append("     LEFT JOIN ENTEXAM_WISHDIV_MST T71 ON T71.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("         AND T71.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("         AND T71.TESTDIV = T6.TESTDIV ");
        stb.append("         AND T71.DESIREDIV = T6.DESIREDIV ");
        stb.append("         AND T71.WISHNO = '1' ");
        stb.append("     LEFT JOIN ENTEXAM_COURSE_MST T81 ON T81.ENTEXAMYEAR = T71.ENTEXAMYEAR ");
        stb.append("         AND T81.APPLICANTDIV = T71.APPLICANTDIV ");
        stb.append("         AND T81.TESTDIV = T71.TESTDIV ");
        stb.append("         AND T81.COURSECD = T71.COURSECD ");
        stb.append("         AND T81.MAJORCD = T71.MAJORCD ");
        stb.append("         AND T81.EXAMCOURSECD = T71.EXAMCOURSECD ");
        stb.append("     LEFT JOIN ENTEXAM_WISHDIV_MST T72 ON T72.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("         AND T72.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("         AND T72.TESTDIV = T6.TESTDIV ");
        stb.append("         AND T72.DESIREDIV = T6.DESIREDIV ");
        stb.append("         AND T72.WISHNO = '2' ");
        stb.append("     LEFT JOIN ENTEXAM_COURSE_MST T82 ON T82.ENTEXAMYEAR = T72.ENTEXAMYEAR ");
        stb.append("         AND T82.APPLICANTDIV = T72.APPLICANTDIV ");
        stb.append("         AND T82.TESTDIV = T72.TESTDIV ");
        stb.append("         AND T82.COURSECD = T72.COURSECD ");
        stb.append("         AND T82.MAJORCD = T72.MAJORCD ");
        stb.append("         AND T82.EXAMCOURSECD = T72.EXAMCOURSECD ");
        stb.append("     LEFT JOIN FINHIGHSCHOOL_MST T9 ON T9.FINSCHOOLCD = T1.SH_SCHOOLCD ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTACTIVITY_DAT T10 ON T10.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("         AND T10.EXAMNO = T1.EXAMNO ");
        stb.append("     LEFT JOIN ENTEXAM_INTERVIEW_DAT T11 ON T11.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("         AND T11.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("         AND T11.TESTDIV = T2.TESTDIV ");
        stb.append("         AND T11.EXAMNO = T1.EXAMNO ");
        stb.append("     LEFT JOIN NAME_MST NMZ002 ON NMZ002.NAMECD1 = 'Z002' AND NMZ002.NAMECD2 = T1.SEX ");
        stb.append("     LEFT JOIN NAME_MST NML006 ON NML006.NAMECD1 = 'L006' AND NML006.NAMECD2 = T6.SHDIV ");
        stb.append("     LEFT JOIN NAME_MST NML013 ON NML013.NAMECD1 = 'L013' AND NML013.NAMECD2 = T2.JUDGEDIV ");
        stb.append("     LEFT JOIN NAME_MST NML023 ON NML023.NAMECD1 = 'L023' AND NML023.NAMECD2 = T6.RECOM_KIND ");
        stb.append("     LEFT JOIN NAME_MST NML025 ON NML025.NAMECD1 = 'L025' AND NML025.NAMECD2 = T1.JUDGE_KIND ");
        stb.append(" WHERE ");
        stb.append("     T1.ENTEXAMYEAR = '" + _param._entexamYear + "' ");
        stb.append("     AND T1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND T2.TESTDIV = '" + _param._testDiv + "' ");
        stb.append("     AND VALUE(T2.JUDGEDIV, '') <> '4' ");
        if (!"9".equals(_param._desireDiv)) {
            stb.append("     AND T6.DESIREDIV = '" + _param._desireDiv + "' ");
        }
        if ("2".equals(_param._applicantDiv)) {
            if ("1".equals(_param._inout)) {
                stb.append("     AND SUBSTR(T1.EXAMNO, 1, 1) <> '6' ");
            } else if ("2".equals(_param._inout)) {
                stb.append("     AND SUBSTR(T1.EXAMNO, 1, 1) = '6' ");
            }
            if ("2".equals(_param._kikoku)) {
                stb.append("     AND VALUE(T1.INTERVIEW_ATTEND_FLG, '0')  = '1' ");
            } else {
                stb.append("     AND VALUE(T1.INTERVIEW_ATTEND_FLG, '0') != '1' ");
            }
        }
        if (isSports) {
            stb.append("     AND T1.SPORTS_FLG = '1' ");
        }
        if (1 == suisenEikenFlg) {
            stb.append("     AND NOT (VALUE(T6.RECOM_KIND, '') = '3') "); // 推薦種別 = 英検以外
        } else if (2 == suisenEikenFlg) {
            stb.append("     AND (VALUE(T6.RECOM_KIND, '') = '3') "); // 推薦種別 = 英検
        }
        stb.append(" ) ");
        stb.append(" SELECT  ");
        stb.append("     T1.* ");
        stb.append(" FROM ");
        stb.append("     MAIN T1 ");
        stb.append(" ORDER BY ");
        if ("1".equals(_param._sort)) {
            stb.append("     VALUE(ATTEND_ALL_FLG, '0') DESC, ");
            stb.append("     RANK, ");
        }
        stb.append("     T1.EXAMNO ");
        return stb.toString();
    }
    
    private static class TestScore {
        final String _score;
        final String _score2;
        final String _score3;
        TestScore(final String score, final String score2, final String score3) {
            _score = score;
            _score2 = score2;
            _score3 = score3;
        }
    }
    
    private String getScoreSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T3.RECEPTNO, ");
        stb.append("     T3.TESTSUBCLASSCD, ");
        stb.append("     T3.SCORE, ");
        stb.append("     T3.SCORE2, ");
        stb.append("     T3.SCORE3 ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_SCORE_DAT T3 ");
        stb.append(" WHERE ");
        stb.append("     T3.ENTEXAMYEAR = '" + _param._entexamYear + "' ");
        stb.append("     AND T3.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND T3.TESTDIV = '" + _param._testDiv + "' ");
        stb.append("     AND T3.EXAM_TYPE = '1' ");
        return stb.toString();
    }
    
    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 56595 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _entexamYear;
        final String _applicantDiv;
        final String _testDiv;
        final String _title;
        final String _subTitle;
        final String _dateString;
        final String _outputName;
        final String _outputRemark1;
        final String _sort;
        final String _special;
        final String _inout; // 1:外部生のみ、2:内部生のみ、3:全て
        final String _kikoku; //対象者(帰国生)(高校のみ) 1:帰国生除く 2:帰国生のみ
        final String _desireDiv; // 志願区分
        final String _rateDiv; // 傾斜配点出力 1:あり 2:なし
        
        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamYear = request.getParameter("YEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _title = getSchoolName(db2);
            _subTitle = getTestDivName(db2);
            _outputName = request.getParameter("OUTPUT_NAME");
            _outputRemark1 = request.getParameter("OUTPUT_REMARK1");
            _sort = request.getParameter("SORT");
            _special = request.getParameter("SPECIAL");
            _inout = request.getParameter("INOUT");
            _kikoku = request.getParameter("KIKOKU");
            _desireDiv = request.getParameter("DESIREDIV");
            _rateDiv = request.getParameter("RATE_DIV");
            final Calendar cal = Calendar.getInstance();
            _dateString = new SimpleDateFormat("yyyy年M月d日").format(Date.valueOf(request.getParameter("LOGIN_DATE"))) + cal.get(Calendar.HOUR_OF_DAY) + "時" + cal.get(Calendar.MINUTE) + "分";
        }
        
        private String getSchoolName(DB2UDB db2) {
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
    }
}

// eof
