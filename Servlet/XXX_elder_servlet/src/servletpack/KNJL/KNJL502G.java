// kanji=漢字
/*
 * $Id: 9538a150fc9aac9dd9480777a3dd41bbb5f62f39 $
 *
 * 作成日: 2018/12/19
 * 作成者: matsushima
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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

public class KNJL502G {

    private static final Log log = LogFactory.getLog(KNJL502G.class);

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
        String frm = "KNJL502G.frm"; //中学新卒用
        if("31".equals(_param._testDiv) || "32".equals(_param._testDiv)){
            frm = "KNJL502G_2.frm"; //一般転編入用
        }
        svf.VrSetForm(frm, 1);

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final String sql = sql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            String befExamNo = "";
            while (rs.next()) {
                final String examno = rs.getString("EXAMNO");
                final String name = rs.getString("NAME");
                final String nameKana = rs.getString("NAME_KANA");
                final String course1 = rs.getString("COURSE1");
                final String course2 = rs.getString("COURSE2");
                final String testName = StringUtils.defaultString(rs.getString("TEST_NAME"), "");
                final String shName = StringUtils.defaultString(rs.getString("SH_NAME"), "");
                final String finschoolName = rs.getString("FINSCHOOL_NAME");
                final String heigankou = rs.getString("HEIGANKOU");
                final String scholarship = StringUtils.defaultString(rs.getString("SCHOLARSHIP"));
                final String consulDate = StringUtils.defaultString(rs.getString("CONSUL_DATE"));
                final String consulResult = rs.getString("CONSUL_RESULT");
                final String semiResult = rs.getString("SEMI_RESULT");
                final String tenhenGradeName = StringUtils.defaultString(rs.getString("GRADE_NAME1"), "");
                final String total5 = rs.getString("TOTAL5");
                final String totalAll = rs.getString("TOTAL_ALL");
                final String absenceDays = rs.getString("ABSENCE_DAYS3");
                final String totalstudytime = rs.getString("TOTALSTUDYTIME");
                final String hope = rs.getString("HOPE");
                final String shSchoolJudgeDay = rs.getString("SH_SCHOOL_JUDGEDAY");
                final String spRemark = StringUtils.defaultString(rs.getString("SP_REMARK"), "");
                String interviewDate = StringUtils.defaultString(rs.getString("INTERVIEW_DATE"), "");
                final String interviewStartTimeH = StringUtils.defaultString(rs.getString("INTERVIEW_STARTTIME_H"), "");
                final String interviewStartTimeD = StringUtils.defaultString(rs.getString("INTERVIEW_STARTTIME_D"), "");
                final String interviewPlace = StringUtils.defaultString(rs.getString("INTERVIEW_PLACE"), "");

                if (!"".equals(befExamNo) && !befExamNo.equals(examno)) {
                    svf.VrEndPage();
                }

                if (!"".equals(interviewDate)) {
                    interviewDate = StringUtils.replace(interviewDate, "/", "-");
                    final String[] dswk = KNJ_EditDate.tate_format4(db2, interviewDate);
                    if (dswk.length > 2) {
                        svf.VrsOut("INTERVIEW_DATE", dswk[2] + "月" + dswk[3] + "日"); //面接日
                    }
                }
                if (!"".equals(interviewStartTimeH) && !"".equals(interviewStartTimeD)) {
                    svf.VrsOut("INTERVIEW_TIME", interviewStartTimeH + ":" + interviewStartTimeD); //開始時間
                }
                svf.VrsOut("INTERVIEW_PLACE", interviewPlace); //面接会場
                svf.VrsOut("EXAM_NO", examno); //選考番号
                final String nameField = getMS932Bytecount(name) > 30 ? "3" : getMS932Bytecount(name) > 20 ? "2" : "1";
                svf.VrsOut("NAME" + nameField, name); //氏名
                svf.VrsOut("KANA", nameKana); //ふりがな
                svf.VrsOut("FINSCHOOL_NAME", finschoolName); //出身校名
                svf.VrsOut("HOPE1", course1); //第1希望
                svf.VrsOut("HOPE2", course2); //第2希望
                svf.VrsOut("APPLICANT_DIV", testName + shName); //出願形態
                if("1".equals(scholarship)) {
                    svf.VrsOut("SCHOLARSHIP", "あり"); //特待生希望
                }else {
                    svf.VrsOut("SCHOLARSHIP", "なし"); //特待生希望
                }
                final int heiganlen = KNJ_EditEdit.getMS932ByteLength(heigankou);
                final String heiganfield = heiganlen > 34 ? "2" : "";
                svf.VrsOut("HIGHSCHOOL_NAME"+heiganfield, heigankou); //併願高校名
                svf.VrsOut("HIGHSCHOOL_JUDGE_DATE", KNJ_EditDate.h_format_JP(db2, shSchoolJudgeDay));//合格発表日
                svf.VrsOut("CONSUL_DATE", KNJ_EditDate.h_format_JP_MD(consulDate)); //入試相談日
                svf.VrsOut("CONSUL_RESULT", consulResult); //相談結果
                svf.VrsOut("SEMI_RESULT", semiResult); //ゼミ結果
                svf.VrsOut("RECORD5", total5); //5科
                svf.VrsOut("RECORD9", totalAll); //9科
                svf.VrsOut("NOTICE", absenceDays); //欠席
                final String clubIdx = KNJ_EditEdit.getMS932ByteLength(totalstudytime) > 20 ? "2": "";
                svf.VrsOut("CLUB_NAME" + clubIdx, totalstudytime); //クラブ活動

                if ((hope != null) && !"".equals(hope)) {
                    final String[] hp_wk = KNJ_EditEdit.get_token(hope, 74, 7);
                    int lpcnt = 0;
                    for (lpcnt = 0;lpcnt < hp_wk.length;lpcnt++) {
                        if (hp_wk[lpcnt] != null && !"".equals(hp_wk[lpcnt])) {
                            svf.VrsOutn("HOPE", lpcnt+1, hp_wk[lpcnt]);
                        }
                    }
                }

                if(!"".equals(tenhenGradeName) && ("31".equals(_param._testDiv) || "32".equals(_param._testDiv))){
                    final String lastStr = "31".equals(_param._testDiv) ? "在学" : "退学";
                    svf.VrsOut("STATE", tenhenGradeName + "　" + lastStr); //状態
                }

                if (!"".equals(spRemark)) {
                    final String[] spr_wk = KNJ_EditEdit.get_token(spRemark, 24, 6);
                    int lpcnt = 0;
                    for (lpcnt = 0;lpcnt < spr_wk.length;lpcnt++) {
                        if (spr_wk[lpcnt] != null && !"".equals(spr_wk[lpcnt])) {
                            svf.VrsOutn("SP_NOTE", lpcnt+1, spr_wk[lpcnt]);
                        }
                    }
                }
                befExamNo = examno;
                _hasData = true;
            }

            if (_hasData) {
                svf.VrEndPage();
            }
            db2.commit();
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
    }

    private static int getMS932Bytecount(String str) {
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

    private String sql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     VBASE.EXAMNO, ");
        stb.append("     VBASE.NAME, ");
        stb.append("     VBASE.NAME_KANA, ");
        stb.append("     VBASE.BIRTHDAY, ");
        stb.append("     COURSE1.EXAMCOURSE_NAME AS COURSE1, ");
        stb.append("     COURSE2.EXAMCOURSE_NAME AS COURSE2, ");
        stb.append("     VBASE.TESTDIV, ");
        stb.append("     L004.NAME1 AS TEST_NAME, ");
        stb.append("     VBASE.SHDIV, ");
        stb.append("     L006.NAME1 AS SH_NAME, ");
        stb.append("     CASE WHEN FINSCHOOL.FINSCHOOLCD IS NULL THEN S005.REMARK1 ELSE FINSCHOOL.FINSCHOOL_NAME END AS FINSCHOOL_NAME, ");
        stb.append("     S016.REMARK2 AS SH_SCHOOL_JUDGEDAY, ");
        stb.append("     VBASE.APPLICANTDIV, ");
        stb.append("     VBASE.HEIGANKOU, ");
        stb.append("     S029.REMARK1 AS SCHOLARSHIP, ");
        stb.append("     S031.REMARK10 AS SP_REMARK, ");
        stb.append("     S031.REMARK6 AS INTERVIEW_DATE, ");
        stb.append("     S031.REMARK7 AS INTERVIEW_STARTTIME_H, ");
        stb.append("     S031.REMARK8 AS INTERVIEW_STARTTIME_D, ");
        stb.append("     L050.NAME1 AS INTERVIEW_PLACE, ");
        stb.append("     CONF_S004.REMARK1 AS CONSUL_DATE, ");
        stb.append("     CONF_S004.REMARK2 AS CONSUL_RESULT, ");
        stb.append("     CONF_S004.REMARK3 AS SEMI_RESULT, ");
        stb.append("     GDAT.GRADE_NAME1, ");
        stb.append("     CONFRPT.TOTAL5, ");
        stb.append("     CONFRPT.TOTAL_ALL, ");
        stb.append("     CONFRPT.ABSENCE_DAYS3, ");
        stb.append("     CONFRPT.TOTALSTUDYTIME, ");
        stb.append("     S032.REMARK10 AS HOPE ");
        stb.append(" FROM ");
        stb.append("     V_ENTEXAM_APPLICANTBASE_DAT VBASE ");
        stb.append("     LEFT JOIN NAME_MST L004 ON L004.NAMECD1 = 'L004' ");
        stb.append("          AND VBASE.TESTDIV = L004.NAMECD2 ");
        stb.append("     LEFT JOIN NAME_MST L006 ON L006.NAMECD1 = 'L006' ");
        stb.append("          AND VBASE.SHDIV = L006.NAMECD2 ");
        stb.append("     LEFT JOIN FINSCHOOL_MST FINSCHOOL ON VBASE.FS_CD = FINSCHOOL.FINSCHOOLCD ");
        stb.append("     LEFT JOIN ENTEXAM_COURSE_MST COURSE1 ON VBASE.ENTEXAMYEAR = COURSE1.ENTEXAMYEAR ");
        stb.append("          AND VBASE.APPLICANTDIV = COURSE1.APPLICANTDIV ");
        stb.append("          AND VBASE.TESTDIV = COURSE1.TESTDIV ");
        stb.append("          AND VBASE.DAI1_COURSECD = COURSE1.COURSECD ");
        stb.append("          AND VBASE.DAI1_MAJORCD = COURSE1.MAJORCD ");
        stb.append("          AND VBASE.DAI1_COURSECODE = COURSE1.EXAMCOURSECD ");
        stb.append("     LEFT JOIN ENTEXAM_COURSE_MST COURSE2 ON VBASE.ENTEXAMYEAR = COURSE2.ENTEXAMYEAR ");
        stb.append("          AND VBASE.APPLICANTDIV = COURSE2.APPLICANTDIV ");
        stb.append("          AND VBASE.TESTDIV = COURSE2.TESTDIV ");
        stb.append("          AND VBASE.DAI2_COURSECD = COURSE2.COURSECD ");
        stb.append("          AND VBASE.DAI2_MAJORCD = COURSE2.MAJORCD ");
        stb.append("          AND VBASE.DAI2_COURSECODE = COURSE2.EXAMCOURSECD ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT S005 ON S005.ENTEXAMYEAR  = VBASE.ENTEXAMYEAR ");
        stb.append("                                                    AND S005.APPLICANTDIV = VBASE.APPLICANTDIV ");
        stb.append("                                                    AND S005.EXAMNO       = VBASE.EXAMNO ");
        stb.append("                                                    AND S005.SEQ          = '005' ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT S016 ON S016.ENTEXAMYEAR  = VBASE.ENTEXAMYEAR ");
        stb.append("          AND S016.APPLICANTDIV = VBASE.APPLICANTDIV ");
        stb.append("          AND S016.EXAMNO       = VBASE.EXAMNO ");
        stb.append("          AND S016.SEQ          = '016' ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT S029 ON S029.ENTEXAMYEAR  = VBASE.ENTEXAMYEAR ");
        stb.append("          AND S029.APPLICANTDIV = VBASE.APPLICANTDIV ");
        stb.append("          AND S029.EXAMNO       = VBASE.EXAMNO ");
        stb.append("          AND S029.SEQ          = '029' ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT S031 ON S031.ENTEXAMYEAR  = VBASE.ENTEXAMYEAR ");
        stb.append("          AND S031.APPLICANTDIV = VBASE.APPLICANTDIV ");
        stb.append("          AND S031.EXAMNO       = VBASE.EXAMNO ");
        stb.append("          AND S031.SEQ          = '031' ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT S032 ON S032.ENTEXAMYEAR  = VBASE.ENTEXAMYEAR ");
        stb.append("          AND S032.APPLICANTDIV = VBASE.APPLICANTDIV ");
        stb.append("          AND S032.EXAMNO       = VBASE.EXAMNO ");
        stb.append("          AND S032.SEQ          = '032' ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT CONFRPT ON CONFRPT.ENTEXAMYEAR  = VBASE.ENTEXAMYEAR ");
        stb.append("          AND CONFRPT.APPLICANTDIV = VBASE.APPLICANTDIV ");
        stb.append("          AND CONFRPT.EXAMNO       = VBASE.EXAMNO ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT CONF_S004 ON CONF_S004.ENTEXAMYEAR  = CONFRPT.ENTEXAMYEAR ");
        stb.append("          AND CONF_S004.APPLICANTDIV = CONFRPT.APPLICANTDIV ");
        stb.append("          AND CONF_S004.EXAMNO       = CONFRPT.EXAMNO ");
        stb.append("          AND CONF_S004.SEQ          = '004' ");
        stb.append("     LEFT JOIN ENTEXAM_AID_RISSHI_DAT AID ON AID.ENTEXAMYEAR = VBASE.ENTEXAMYEAR ");
        stb.append("          AND AID.APPLICANTDIV = VBASE.APPLICANTDIV ");
        stb.append("          AND AID.EXAMNO       = VBASE.EXAMNO ");
        stb.append("          AND substr(AID.AID_TESTDIV, 2, 1) || substr(AID.EXAMCD, 4, 1) = VBASE.TESTDIV ");
        stb.append("     LEFT JOIN NAME_MST L050 ON L050.NAMECD1 = 'L050' ");
        stb.append("          AND L050.NAMECD2 = S031.REMARK9 ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT S021 ON S021.ENTEXAMYEAR  = VBASE.ENTEXAMYEAR ");
        stb.append("          AND S021.APPLICANTDIV = VBASE.APPLICANTDIV ");
        stb.append("          AND S021.EXAMNO       = VBASE.EXAMNO ");
        stb.append("          AND S021.SEQ          = '021' ");
        stb.append("     LEFT JOIN SCHREG_REGD_GDAT GDAT ");
        stb.append("       ON GDAT.YEAR = S021.ENTEXAMYEAR ");
        stb.append("      AND GDAT.GRADE = S021.REMARK1 ");
        stb.append(" WHERE ");
        stb.append("     VBASE.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND VBASE.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND VBASE.TESTDIV = '" + _param._testDiv + "' ");
        stb.append(" ORDER BY ");
        stb.append("     VBASE.EXAMNO ");
        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 71209 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        private final String _entexamyear;
        private final String _applicantDiv;
        private final String _testDiv;
        private final String _loginDate;
        final String _applicantdivName;
        final String _testdivAbbv1;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("YEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _loginDate = request.getParameter("LOGIN_DATE");
            _applicantdivName = getNameMst(db2, "NAME1", "L003", _applicantDiv);
            _testdivAbbv1 = StringUtils.defaultString(getNameMst(db2, "ABBV1", "L004", _testDiv));
            //final String z010Name2 = StringUtils.defaultString(getNameMst(db2, "NAME2", "Z010", "00"));
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

