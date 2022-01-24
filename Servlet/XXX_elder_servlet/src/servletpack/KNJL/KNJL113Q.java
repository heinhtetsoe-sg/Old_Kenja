/*
 * $Id: 22c4669cfc1679c771f966f86f52a6df63fb46d4 $
 *
 * 作成日: 2019/01/07
 * 作成者: tawada
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

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJL113Q {

    private static final Log log = LogFactory.getLog(KNJL113Q.class);

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

            printKankyo(db2, svf);
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

    private void printKankyo(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrSetForm("KNJL113Q.frm", 4);

        PreparedStatement ps = null;
        PreparedStatement psCnt = null;
        ResultSet rs = null;
        ResultSet rsCnt = null;

        try {
            final String sqlCnt = sqlGansyo("CNT");
            psCnt = db2.prepareStatement(sqlCnt);
            rsCnt = psCnt.executeQuery();
            rsCnt.next();
            final int totalCnt = rsCnt.getInt("CNT");
            int pageCnt = 1;
            int maxLine = 7;
            int lineCnt = 1;
            final int pageSu = totalCnt / maxLine;
            final int pageAmari = totalCnt % maxLine;
            final int totalPage = pageSu + (pageAmari > 0 ? 1 : 0);

            final String sql = sqlGansyo("");
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            svf.VrsOut("TITLE", _param._entexamyear + "年度　環境調査票照合用チェックリスト");
            svf.VrsOut("DATE", KNJ_EditDate.h_format_S(_param._loginDate, "yyyy年MM月dd日"));

            while (rs.next()) {
                if (lineCnt > maxLine) {
                    lineCnt = 1;
                    pageCnt++;
                }
                svf.VrsOut("PAGE1", String.valueOf(pageCnt));
                svf.VrsOut("PAGE2", String.valueOf(totalPage));
                final String examno = rs.getString("EXAMNO");
                final String examcourseMark = rs.getString("EXAMCOURSE_MARK");
                final String examhallName = rs.getString("EXAMHALL_NAME");
                final String nameKana = rs.getString("NAME_KANA");
                final String name = rs.getString("NAME");
                final String sex = rs.getString("SEX");
                final String birthday = rs.getString("BIRTHDAY");
                final String zipCd = rs.getString("ZIPCD");
                final String address1 = rs.getString("ADDRESS1");
                final String address2 = rs.getString("ADDRESS2");
                final String telno = rs.getString("TELNO");
                final String fsGr = rs.getString("FS_GR");
                final String finschoolName = rs.getString("FINSCHOOL_NAME");
                final String grdDate = rs.getString("FS_WNAME") + rs.getString("FS_Y") + "年"  + rs.getString("FS_M") + "月" + rs.getString("FS_GRDDIV");
                final String gname = rs.getString("GNAME");
                final String relaname = rs.getString("RELANAME");
                final String gzipCd = rs.getString("GZIPCD");
                final String gaddress1 = rs.getString("GADDRESS1");
                final String gaddress2 = rs.getString("GADDRESS2");
                final String gtelno = rs.getString("GTELNO");
                final String gJob = rs.getString("GJOB");
                final String remark1 = rs.getString("REMARK1");
                final String shCd1 = rs.getString("SH_CD1");
                final String shCd2 = rs.getString("SH_CD2");
                final String shCd3 = rs.getString("SH_CD3");
                final String shCd4 = rs.getString("SH_CD4");
                final String shName1 = rs.getString("SH_NAME1");
                final String shName2 = rs.getString("SH_NAME2");
                final String shName3 = rs.getString("SH_NAME3");
                final String shName4 = rs.getString("SH_NAME4");
                final String scholar = rs.getString("SCHOLAR");
                final String scholarToukyuSengan = rs.getString("SCHOLAR_TOUKYU_SENGAN");
                final String dormitory = rs.getString("DORMITORY");
                final String satNo = rs.getString("SAT_NO");
                final String judgeSat = rs.getString("JUDGE_SAT");
                final String absence2 = rs.getString("ABSENCE2");
                final String absence3 = rs.getString("ABSENCE3");

                svf.VrsOut("EXAM_NO", examno);
                svf.VrsOut("DIV1", examcourseMark);
                svf.VrsOut("PLACE", examhallName);
                svf.VrsOut("KANA", nameKana);
                svf.VrsOut("NAME", name);
                svf.VrsOut("SEX", sex);
                svf.VrsOut("BIRTHDAY", birthday);
                svf.VrsOut("GRD", fsGr);
                svf.VrsOut("ATTEND_SCHOOL_NAME", finschoolName);
                svf.VrsOut("GRD_DATE", grdDate);
                svf.VrsOut("ZIP_NO1", zipCd);
                svf.VrsOut("ADDR1_1", address1);
                svf.VrsOut("ADDR1_2", address2);
                svf.VrsOut("TEL_NO1", telno);
                svf.VrsOut("GUARD_NAME", gname);
                svf.VrsOut("RELATION", relaname);
                svf.VrsOut("JOB_TITLE", "　職　業　");
                svf.VrsOut("CONTACT_NAME", "　備　考　");
                svf.VrsOut("ZIP_NO2", gzipCd);
                svf.VrsOut("ADDR2_1", gaddress1);
                svf.VrsOut("ADDR2_2", gaddress2);
                svf.VrsOut("TEL_NO2", gtelno);
                svf.VrsOut("JOB_NAME", gJob);
                svf.VrsOut("EMERGENCY", remark1);
                svf.VrsOut("HOPE_SCHOOL_NO1", shCd1);
                svf.VrsOut("HOPE_SCHOOL_NAME1", shName1);
                svf.VrsOut("HOPE_SCHOOL_NO2", shCd2);
                svf.VrsOut("HOPE_SCHOOL_NAME2", shName2);
                svf.VrsOut("HOPE_SCHOOL_NO3", shCd3);
                svf.VrsOut("HOPE_SCHOOL_NAME3", shName3);
                svf.VrsOut("HOPE_SCHOOL_NO4", shCd4);
                svf.VrsOut("HOPE_SCHOOL_NAME4", shName4);
                svf.VrsOut("SCHOLAR1", scholar);
                svf.VrsOut("SCHOLAR2", scholarToukyuSengan);
                svf.VrsOut("DORMITORY", dormitory);
                svf.VrsOut("SAT_NO", satNo);
                svf.VrsOut("SAT_RANK", judgeSat);
                svf.VrsOut("NOTICE1", absence2);
                svf.VrsOut("NOTICE2", absence3);

                svf.VrEndRecord();
                lineCnt++;
                _hasData = true;
            }

            db2.commit();
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
    }

    private String sqlGansyo(final String selectDiv) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        if ("CNT".equals(selectDiv)) {
            stb.append("     COUNT(*) AS CNT ");
        } else {
            stb.append("     VBASE.EXAMNO, ");
            stb.append("     CM.EXAMCOURSE_MARK, ");
            stb.append("     HALL.EXAMHALL_NAME, ");
            stb.append("     VBASE.NAME, ");
            stb.append("     VBASE.NAME_KANA, ");
            stb.append("     Z002.NAME2 AS SEX, ");
            stb.append("     substr(replace(CAST(VBASE.BIRTHDAY AS VARCHAR(10)), '-', ''), 3, 6) as BIRTHDAY, ");
            stb.append("     ADDR.ZIPCD, ");
            stb.append("     ADDR.ADDRESS1, ");
            stb.append("     ADDR.ADDRESS2, ");
            stb.append("     ADDR.TELNO, ");
            stb.append("     L016.NAME2 AS FS_GR, ");
            stb.append("     FINSCHOOL.FINSCHOOL_NAME, ");
            stb.append("     L007.NAME1 AS FS_WNAME, ");
            stb.append("     VBASE.FS_Y, ");
            stb.append("     VBASE.FS_M, ");
            stb.append("     L016.NAME1 AS FS_GRDDIV, ");
            stb.append("     ADDR.GNAME, ");
            stb.append("     H201.NAME1 AS RELANAME, ");
            stb.append("     ADDR.GZIPCD, ");
            stb.append("     ADDR.GADDRESS1, ");
            stb.append("     ADDR.GADDRESS2, ");
            stb.append("     ADDR.GTELNO, ");
            stb.append("     ADDR.GJOB, ");
            stb.append("     VBASE.REMARK1, ");
            stb.append("     VBASE.SH_SCHOOLCD1 AS SH_CD1, ");
            stb.append("     VBASE.SH_SCHOOLCD2 AS SH_CD2, ");
            stb.append("     VBASE.SH_SCHOOLCD3 AS SH_CD3, ");
            stb.append("     VBASE.SH_SCHOOLCD4 AS SH_CD4, ");
            stb.append("     SH1.FINSCHOOL_NAME AS SH_NAME1, ");
            stb.append("     SH2.FINSCHOOL_NAME AS SH_NAME2, ");
            stb.append("     SH3.FINSCHOOL_NAME AS SH_NAME3, ");
            stb.append("     SH4.FINSCHOOL_NAME AS SH_NAME4, ");
            stb.append("     CONFRPT.ABSENCE_DAYS2 AS ABSENCE2, ");
            stb.append("     CONFRPT.ABSENCE_DAYS3 AS ABSENCE3, ");
            stb.append("     CASE VBASE.SCHOLAR_KIBOU WHEN '1' THEN '特別' WHEN '2' THEN '一般' ELSE '無' END AS SCHOLAR, ");
            stb.append("     VBASE.SCHOLAR_TOUKYU_SENGAN, ");
            stb.append("     CASE VBASE.DORMITORY_FLG WHEN '1' THEN '有' WHEN '2' THEN '無' ELSE '' END AS DORMITORY, ");
            stb.append("     SAT.SAT_NO, ");
            stb.append("     L200.NAME1 AS JUDGE_SAT ");
        }
        stb.append(" FROM ");
        stb.append("     V_ENTEXAM_APPLICANTBASE_DAT VBASE ");
        stb.append("     LEFT JOIN NAME_MST Z002 ON Z002.NAMECD1 = 'Z002' ");
        stb.append("                            AND VBASE.SEX    = Z002.NAMECD2 ");
        stb.append("     LEFT JOIN NAME_MST L016 ON L016.NAMECD1    = 'L016' ");
        stb.append("                            AND VBASE.FS_GRDDIV = L016.NAMECD2 ");
        stb.append("     LEFT JOIN FINSCHOOL_MST FINSCHOOL ON VBASE.FS_CD = FINSCHOOL.FINSCHOOLCD ");
        stb.append("     LEFT JOIN FINSCHOOL_MST SH1 ON VBASE.SH_SCHOOLCD1 = SH1.FINSCHOOLCD ");
        stb.append("     LEFT JOIN FINSCHOOL_MST SH2 ON VBASE.SH_SCHOOLCD2 = SH2.FINSCHOOLCD ");
        stb.append("     LEFT JOIN FINSCHOOL_MST SH3 ON VBASE.SH_SCHOOLCD3 = SH3.FINSCHOOLCD ");
        stb.append("     LEFT JOIN FINSCHOOL_MST SH4 ON VBASE.SH_SCHOOLCD4 = SH4.FINSCHOOLCD ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT ADDR ON VBASE.ENTEXAMYEAR  = ADDR.ENTEXAMYEAR ");
        stb.append("                                             AND VBASE.APPLICANTDIV = ADDR.APPLICANTDIV ");
        stb.append("                                             AND VBASE.EXAMNO       = ADDR.EXAMNO ");
        stb.append("     LEFT JOIN NAME_MST H201 ON H201.NAMECD1      = 'H201' ");
        stb.append("                            AND ADDR.RELATIONSHIP = H201.NAMECD2 ");
        stb.append("     LEFT JOIN ENTEXAM_NO_DAT NO_DAT ON VBASE.ENTEXAMYEAR  = NO_DAT.ENTEXAMYEAR ");
        stb.append("                                    AND VBASE.APPLICANTDIV = NO_DAT.APPLICANTDIV ");
        stb.append("                                    AND VBASE.EXAMNO BETWEEN NO_DAT.EXAMNO_FROM AND NO_DAT.EXAMNO_TO ");
        stb.append("     LEFT JOIN ENTEXAM_COURSE_MST CM ON NO_DAT.ENTEXAMYEAR  = CM.ENTEXAMYEAR ");
        stb.append("                                    AND NO_DAT.APPLICANTDIV = CM.APPLICANTDIV ");
        stb.append("                                    AND NO_DAT.TESTDIV      = CM.TESTDIV ");
        stb.append("                                    AND NO_DAT.COURSECD     = CM.COURSECD ");
        stb.append("                                    AND NO_DAT.MAJORCD      = CM.MAJORCD ");
        stb.append("                                    AND NO_DAT.EXAMCOURSECD = CM.EXAMCOURSECD ");
        stb.append("     LEFT JOIN ENTEXAM_HALL_YDAT HALL ON NO_DAT.ENTEXAMYEAR  = HALL.ENTEXAMYEAR ");
        stb.append("                                     AND NO_DAT.APPLICANTDIV = HALL.APPLICANTDIV ");
        stb.append("                                     AND NO_DAT.TESTDIV      = HALL.TESTDIV ");
        stb.append("                                     AND NO_DAT.EXAMHALLCD   = HALL.EXAMHALLCD ");
        stb.append("     LEFT JOIN SAT_EXAM_DAT SAT ON SAT.YEAR           = '" + _param._loginYear + "' ");
        stb.append("                               AND VBASE.JIZEN_BANGOU = SAT.SAT_NO ");
        stb.append("     LEFT JOIN NAME_MST L200 ON L200.NAMECD1  = 'L200' ");
        stb.append("                            AND SAT.JUDGE_SAT = L200.NAMECD2 ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT CONFRPT ON VBASE.ENTEXAMYEAR  = CONFRPT.ENTEXAMYEAR ");
        stb.append("                                                   AND VBASE.APPLICANTDIV = CONFRPT.APPLICANTDIV ");
        stb.append("                                                   AND VBASE.EXAMNO       = CONFRPT.EXAMNO ");
        stb.append("     LEFT JOIN NAME_MST L007 ON L007.NAMECD1   = 'L007' ");
        stb.append("                            AND VBASE.FS_ERACD = L007.NAMECD2 ");
        stb.append("     INNER JOIN NAME_MST L013 ON L013.NAMECD1    = 'L013' ");
        stb.append("                             AND L013.NAMECD2    = VBASE.JUDGEMENT ");
        stb.append("                             AND L013.NAMESPARE1 = '1' ");
        stb.append(" WHERE ");
        stb.append("         VBASE.ENTEXAMYEAR  = '" + _param._entexamyear + "' ");
        stb.append("     AND VBASE.APPLICANTDIV = '" + _param._applicantdiv + "' ");
        stb.append("     AND VBASE.TESTDIV0 <> '9' ");
        stb.append("     AND value(VBASE.ENTDIV, '')       = '1' "); // 入学区分
        stb.append("     AND value(VBASE.PROCEDUREDIV, '') = '1' "); // 手続き区分
        if (!"CNT".equals(selectDiv)) {
            stb.append(" ORDER BY ");
            stb.append("     VBASE.EXAMNO ");
        }
        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 64583 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _entexamyear;
        final String _applicantdiv;
        final String _loginYear;
        final String _loginDate;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _applicantdiv   = request.getParameter("APPLICANTDIV");
            _entexamyear    = request.getParameter("ENTEXAMYEAR");
            _loginYear      = request.getParameter("LOGIN_YEAR");
            _loginDate      = request.getParameter("LOGIN_DATE");
        }
    }
}

// eof
