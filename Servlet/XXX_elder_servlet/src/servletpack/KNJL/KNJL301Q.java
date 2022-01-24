/*
 * $Id: b01aeab54b5435ee9570f896f518eb4b1de77801 $
 *
 * 作成日: 2017/04/05
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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJL301Q {

    private static final Log log = LogFactory.getLog(KNJL301Q.class);

    private boolean _hasData;
    private final String SCHOOL_KIND_P = "P";
    private final String SCHOOL_KIND_J = "J";

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

            if ("1".equals(_param._taisyou)) {
                printGansyo(db2, svf);
            } else if ("2".equals(_param._taisyou)) {
                printTyousa(db2, svf);
            } else if ("3".equals(_param._taisyou)) {
                printKatsudou(db2, svf);
            }
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

    private void printGansyo(final DB2UDB db2, final Vrw32alp svf) {
        if (SCHOOL_KIND_P.equals(_param._schoolKind)) {
            svf.VrSetForm("KNJL301Q_5.frm", 4);
        } else if (SCHOOL_KIND_J.equals(_param._schoolKind)) {
            svf.VrSetForm("KNJL301Q_4.frm", 4);
        } else {
            svf.VrSetForm("KNJL301Q_1.frm", 4);
        }

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
            int maxLine = (SCHOOL_KIND_J.equals(_param._schoolKind)) ? 6: 7;
            int lineCnt = 1;
            final int pageSu = totalCnt / maxLine;
            final int pageAmari = totalCnt % maxLine;
            final int totalPage = pageSu + (pageAmari > 0 ? 1 : 0);

            final String sql = sqlGansyo("");
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            if (SCHOOL_KIND_J.equals(_param._schoolKind) && "9".equals(_param._testdiv)) {
                svf.VrsOut("TITLE", _param._entexamyear + "年度　入学基準テスト　願書チェックリスト");
            } else {
                svf.VrsOut("TITLE", _param._entexamyear + "年度　" + _param._testdivName + "入試　願書チェックリスト");
            }
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
                String addrField = "";
                if (SCHOOL_KIND_P.equals(_param._schoolKind)) {
                    if (KNJ_EditEdit.getMS932ByteLength(address1) > 50 || KNJ_EditEdit.getMS932ByteLength(address2) > 50) {
                        addrField = "_2";
                    }
                }
                svf.VrsOut("ADDR1_1" + addrField, address1);
                svf.VrsOut("ADDR1_2" + addrField, address2);
                svf.VrsOut("TEL_NO1", telno);
                svf.VrsOut("GUARD_NAME", gname);
                svf.VrsOut("RELATION", relaname);
                svf.VrsOut("JOB_TITLE", "　職　業　");
                svf.VrsOut("CONTACT_NAME", "　備　考　");
                svf.VrsOut("ZIP_NO2", gzipCd);
                String gaddrField = "";
                if (SCHOOL_KIND_P.equals(_param._schoolKind)) {
                    if (KNJ_EditEdit.getMS932ByteLength(gaddress1) > 50 || KNJ_EditEdit.getMS932ByteLength(gaddress2) > 50) {
                        gaddrField = "_2";
                    }
                }
                svf.VrsOut("ADDR2_1" + gaddrField, gaddress1);
                svf.VrsOut("ADDR2_2" + gaddrField, gaddress2);
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
        stb.append("          AND VBASE.SEX = Z002.NAMECD2 ");
        stb.append("     LEFT JOIN NAME_MST L016 ON L016.NAMECD1 = 'L016' ");
        stb.append("          AND VBASE.FS_GRDDIV = L016.NAMECD2 ");
        stb.append("     LEFT JOIN FINSCHOOL_MST FINSCHOOL ON VBASE.FS_CD = FINSCHOOL.FINSCHOOLCD ");
        stb.append("     LEFT JOIN FINSCHOOL_MST SH1 ON VBASE.SH_SCHOOLCD1 = SH1.FINSCHOOLCD ");
        stb.append("     LEFT JOIN FINSCHOOL_MST SH2 ON VBASE.SH_SCHOOLCD2 = SH2.FINSCHOOLCD ");
        stb.append("     LEFT JOIN FINSCHOOL_MST SH3 ON VBASE.SH_SCHOOLCD3 = SH3.FINSCHOOLCD ");
        stb.append("     LEFT JOIN FINSCHOOL_MST SH4 ON VBASE.SH_SCHOOLCD4 = SH4.FINSCHOOLCD ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT ADDR ON VBASE.ENTEXAMYEAR = ADDR.ENTEXAMYEAR ");
        stb.append("          AND VBASE.APPLICANTDIV = ADDR.APPLICANTDIV ");
        stb.append("          AND VBASE.EXAMNO = ADDR.EXAMNO ");
        stb.append("     LEFT JOIN NAME_MST H201 ON H201.NAMECD1 = 'H201' ");
        stb.append("          AND ADDR.RELATIONSHIP = H201.NAMECD2 ");
        stb.append("     LEFT JOIN ENTEXAM_NO_DAT NO_DAT ON VBASE.ENTEXAMYEAR = NO_DAT.ENTEXAMYEAR ");
        stb.append("          AND VBASE.APPLICANTDIV = NO_DAT.APPLICANTDIV ");
        stb.append("          AND VBASE.EXAMNO BETWEEN NO_DAT.EXAMNO_FROM AND NO_DAT.EXAMNO_TO ");
        stb.append("     LEFT JOIN ENTEXAM_COURSE_MST CM ON NO_DAT.ENTEXAMYEAR = CM.ENTEXAMYEAR ");
        stb.append("          AND NO_DAT.APPLICANTDIV = CM.APPLICANTDIV ");
        stb.append("          AND NO_DAT.TESTDIV = CM.TESTDIV ");
        stb.append("          AND NO_DAT.COURSECD = CM.COURSECD ");
        stb.append("          AND NO_DAT.MAJORCD = CM.MAJORCD ");
        stb.append("          AND NO_DAT.EXAMCOURSECD = CM.EXAMCOURSECD ");
        stb.append("     LEFT JOIN ENTEXAM_HALL_YDAT HALL ON NO_DAT.ENTEXAMYEAR = HALL.ENTEXAMYEAR ");
        stb.append("          AND NO_DAT.APPLICANTDIV = HALL.APPLICANTDIV ");
        stb.append("          AND NO_DAT.TESTDIV = HALL.TESTDIV ");
        stb.append("          AND NO_DAT.EXAMHALLCD = HALL.EXAMHALLCD ");
        stb.append("     LEFT JOIN SAT_EXAM_DAT SAT ON SAT.YEAR = '" + _param._loginYear + "' ");
        stb.append("          AND VBASE.JIZEN_BANGOU = SAT.SAT_NO ");
        stb.append("     LEFT JOIN NAME_MST L200 ON L200.NAMECD1 = 'L200' ");
        stb.append("          AND SAT.JUDGE_SAT = L200.NAMECD2 ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT CONFRPT ON VBASE.ENTEXAMYEAR = CONFRPT.ENTEXAMYEAR ");
        stb.append("          AND VBASE.APPLICANTDIV = CONFRPT.APPLICANTDIV ");
        stb.append("          AND VBASE.EXAMNO = CONFRPT.EXAMNO ");
        stb.append("     LEFT JOIN NAME_MST L007 ON L007.NAMECD1   = 'L007' ");
        stb.append("                            AND VBASE.FS_ERACD = L007.NAMECD2 ");
        stb.append(" WHERE ");
        stb.append("     VBASE.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND VBASE.APPLICANTDIV = '" + _param._applicantdiv + "' ");
        if (SCHOOL_KIND_J.equals(_param._schoolKind) || SCHOOL_KIND_P.equals(_param._schoolKind)) {
            stb.append("     AND VBASE.TESTDIV = '" + _param._testdiv + "' ");
        } else {
            stb.append("     AND VBASE.TESTDIV0 = '" + _param._testdiv + "' ");
        }
        if (!"CNT".equals(selectDiv)) {
            stb.append(" ORDER BY ");
            stb.append("     VBASE.EXAMNO ");
        }
        return stb.toString();
    }

    private void printTyousa(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        svf.VrSetForm("KNJL301Q_2.frm", 4);

        PreparedStatement ps = null;
        PreparedStatement psCnt = null;
        ResultSet rs = null;
        ResultSet rsCnt = null;
        final List subclassList = getSubclassList(db2);

        try {
            final String sqlCnt = sqlTyousa("CNT");
            psCnt = db2.prepareStatement(sqlCnt);
            rsCnt = psCnt.executeQuery();
            rsCnt.next();
            final int totalCnt = rsCnt.getInt("CNT");
            int pageCnt = 1;
            int maxLine = 21;
            int lineCnt = 1;
            final int pageSu = totalCnt / maxLine;
            final int pageAmari = totalCnt % maxLine;
            final int totalPage = pageSu + (pageAmari > 0 ? 1 : 0);

            final String sql = sqlTyousa("");
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            svf.VrsOut("TITLE", _param._entexamyear + "年度　" + _param._testdivName + "入試　調査書チェックリスト");
            svf.VrsOut("DATE", KNJ_EditDate.h_format_S(_param._loginDate, "yyyy年MM月dd日"));
            int subclassCnt = 1;
            for (Iterator iterator = subclassList.iterator(); iterator.hasNext();) {
                String subclassName = (String) iterator.next();
                svf.VrsOut("CLASS_NAME" + subclassCnt, subclassName);
                subclassCnt++;
            }

            while (rs.next()) {
                if (lineCnt > maxLine) {
                    lineCnt = 1;
                    pageCnt++;
                }
                svf.VrsOut("PAGE1", String.valueOf(pageCnt));
                svf.VrsOut("PAGE2", String.valueOf(totalPage));
                final String examno = rs.getString("EXAMNO");
                final String name = rs.getString("NAME");
                final String rpt01 = getRpt(rs.getString("CONF1_RPT1"), rs.getString("CONF2_RPT1"), rs.getString("CONFIDENTIAL_RPT01"));
                final String rpt02 = getRpt(rs.getString("CONF1_RPT2"), rs.getString("CONF2_RPT2"), rs.getString("CONFIDENTIAL_RPT02"));
                final String rpt03 = getRpt(rs.getString("CONF1_RPT3"), rs.getString("CONF2_RPT3"), rs.getString("CONFIDENTIAL_RPT03"));
                final String rpt04 = getRpt(rs.getString("CONF1_RPT4"), rs.getString("CONF2_RPT4"), rs.getString("CONFIDENTIAL_RPT04"));
                final String rpt05 = getRpt(rs.getString("CONF1_RPT5"), rs.getString("CONF2_RPT5"), rs.getString("CONFIDENTIAL_RPT05"));
                final String rpt06 = getRpt(rs.getString("CONF1_RPT6"), rs.getString("CONF2_RPT6"), rs.getString("CONFIDENTIAL_RPT06"));
                final String rpt07 = getRpt(rs.getString("CONF1_RPT7"), rs.getString("CONF2_RPT7"), rs.getString("CONFIDENTIAL_RPT07"));
                final String rpt08 = getRpt(rs.getString("CONF1_RPT8"), rs.getString("CONF2_RPT8"), rs.getString("CONFIDENTIAL_RPT08"));
                final String rpt09 = getRpt(rs.getString("CONF1_RPT9"), rs.getString("CONF2_RPT9"), rs.getString("CONFIDENTIAL_RPT09"));
                final String absence1 = rs.getString("ABSENCE1");
                final String absence2 = rs.getString("ABSENCE2");
                final String absence3 = rs.getString("ABSENCE3");
                final String absenceRemark = rs.getString("ABSENCE_REMARK");
                final String[] printAbsenceRemark = KNJ_EditEdit.get_token(absenceRemark, 30, 2);
                final String absenceRemark2 = rs.getString("ABSENCE_REMARK2");
                final String[] printAbsenceRemark2 = KNJ_EditEdit.get_token(absenceRemark2, 30, 2);
                final String absenceRemark3 = rs.getString("ABSENCE_REMARK3");
                final String[] printAbsenceRemark3 = KNJ_EditEdit.get_token(absenceRemark3, 30, 2);
                final String remark1 = rs.getString("REMARK1");
                final String[] printRemark1 = KNJ_EditEdit.get_token(remark1, 40, 5);

                svf.VrsOut("EXAM_NO", examno);
                svf.VrsOut("NAME", name);
                svf.VrsOut("SCORE1", rpt01);
                svf.VrsOut("SCORE2", rpt02);
                svf.VrsOut("SCORE3", rpt03);
                svf.VrsOut("SCORE4", rpt04);
                svf.VrsOut("SCORE5", rpt05);
                svf.VrsOut("SCORE6", rpt06);
                svf.VrsOut("SCORE7", rpt07);
                svf.VrsOut("SCORE8", rpt08);
                svf.VrsOut("SCORE9", rpt09);
                svf.VrsOut("NOTICE1", absence1);
                svf.VrsOut("NOTICE2", absence2);
                svf.VrsOut("NOTICE3", absence3);
                printBunkatsu(svf, printAbsenceRemark, "NOTE1_", "");
                printBunkatsu(svf, printAbsenceRemark2, "NOTE1_", "2");
                printBunkatsu(svf, printAbsenceRemark3, "NOTE1_", "3");
                printBunkatsu(svf, printRemark1, "NOTE2_", "");

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

    private String getRpt(final String val1, final String val2, final String val3) {
        return repNullToBlank(val1) + repNullToBlank(val2) + repNullToBlank(val3);
    }

    private String repNullToBlank(final String val) {
        return (null == val) ? " " : val;
    }

    private List getSubclassList(final DB2UDB db2) throws SQLException {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     NAME1 ");
        stb.append(" FROM ");
        stb.append("     V_NAME_MST ");
        stb.append(" WHERE ");
        stb.append("     YEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND NAMECD1 = 'L008' ");
        stb.append(" ORDER BY ");
        stb.append("     NAMECD2 ");

        PreparedStatement ps = null;
        ResultSet rs = null;
        final List retList = new ArrayList();
        try {
            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();
            while (rs.next()) {
                retList.add(rs.getString("NAME1"));
            }
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String sqlTyousa(final String selectDiv) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        if ("CNT".equals(selectDiv)) {
            stb.append("     COUNT(*) AS CNT ");
        } else {
            stb.append("     VBASE.EXAMNO, ");
            stb.append("     VBASE.NAME, ");
            stb.append("     CONFRPT.CONFIDENTIAL_RPT01, ");
            stb.append("     CONFRPT.CONFIDENTIAL_RPT02, ");
            stb.append("     CONFRPT.CONFIDENTIAL_RPT03, ");
            stb.append("     CONFRPT.CONFIDENTIAL_RPT04, ");
            stb.append("     CONFRPT.CONFIDENTIAL_RPT05, ");
            stb.append("     CONFRPT.CONFIDENTIAL_RPT06, ");
            stb.append("     CONFRPT.CONFIDENTIAL_RPT07, ");
            stb.append("     CONFRPT.CONFIDENTIAL_RPT08, ");
            stb.append("     CONFRPT.CONFIDENTIAL_RPT09, ");
            stb.append("     CONFRPT_D001.REMARK1 AS CONF1_RPT1, ");
            stb.append("     CONFRPT_D001.REMARK2 AS CONF1_RPT2, ");
            stb.append("     CONFRPT_D001.REMARK3 AS CONF1_RPT3, ");
            stb.append("     CONFRPT_D001.REMARK4 AS CONF1_RPT4, ");
            stb.append("     CONFRPT_D001.REMARK5 AS CONF1_RPT5, ");
            stb.append("     CONFRPT_D001.REMARK6 AS CONF1_RPT6, ");
            stb.append("     CONFRPT_D001.REMARK7 AS CONF1_RPT7, ");
            stb.append("     CONFRPT_D001.REMARK8 AS CONF1_RPT8, ");
            stb.append("     CONFRPT_D001.REMARK9 AS CONF1_RPT9, ");
            stb.append("     CONFRPT_D002.REMARK1 AS CONF2_RPT1, ");
            stb.append("     CONFRPT_D002.REMARK2 AS CONF2_RPT2, ");
            stb.append("     CONFRPT_D002.REMARK3 AS CONF2_RPT3, ");
            stb.append("     CONFRPT_D002.REMARK4 AS CONF2_RPT4, ");
            stb.append("     CONFRPT_D002.REMARK5 AS CONF2_RPT5, ");
            stb.append("     CONFRPT_D002.REMARK6 AS CONF2_RPT6, ");
            stb.append("     CONFRPT_D002.REMARK7 AS CONF2_RPT7, ");
            stb.append("     CONFRPT_D002.REMARK8 AS CONF2_RPT8, ");
            stb.append("     CONFRPT_D002.REMARK9 AS CONF2_RPT9, ");
            stb.append("     CONFRPT.ABSENCE_DAYS  AS ABSENCE1, ");
            stb.append("     CONFRPT.ABSENCE_DAYS2 AS ABSENCE2, ");
            stb.append("     CONFRPT.ABSENCE_DAYS3 AS ABSENCE3, ");
            stb.append("     VALUE(CONFRPT.ABSENCE_REMARK, '')  AS ABSENCE_REMARK, ");
            stb.append("     VALUE(CONFRPT.ABSENCE_REMARK2, '') AS ABSENCE_REMARK2, ");
            stb.append("     VALUE(CONFRPT.ABSENCE_REMARK3, '') AS ABSENCE_REMARK3, ");
            stb.append("     CONFRPT.REMARK1 ");
        }
        stb.append(" FROM ");
        stb.append("     V_ENTEXAM_APPLICANTBASE_DAT VBASE ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT CONFRPT ON VBASE.ENTEXAMYEAR = CONFRPT.ENTEXAMYEAR ");
        stb.append("          AND VBASE.APPLICANTDIV = CONFRPT.APPLICANTDIV ");
        stb.append("          AND VBASE.EXAMNO = CONFRPT.EXAMNO ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT CONFRPT_D001 ON VBASE.ENTEXAMYEAR = CONFRPT_D001.ENTEXAMYEAR ");
        stb.append("          AND VBASE.APPLICANTDIV = CONFRPT_D001.APPLICANTDIV ");
        stb.append("          AND VBASE.EXAMNO = CONFRPT_D001.EXAMNO ");
        stb.append("          AND CONFRPT_D001.SEQ = '001' ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT CONFRPT_D002 ON VBASE.ENTEXAMYEAR = CONFRPT_D002.ENTEXAMYEAR ");
        stb.append("          AND VBASE.APPLICANTDIV = CONFRPT_D002.APPLICANTDIV ");
        stb.append("          AND VBASE.EXAMNO = CONFRPT_D002.EXAMNO ");
        stb.append("          AND CONFRPT_D002.SEQ = '002' ");
        stb.append(" WHERE ");
        stb.append("     VBASE.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND VBASE.APPLICANTDIV = '" + _param._applicantdiv + "' ");
        if (SCHOOL_KIND_J.equals(_param._schoolKind) || SCHOOL_KIND_P.equals(_param._schoolKind)) {
            stb.append("     AND VBASE.TESTDIV = '" + _param._testdiv + "' ");
        } else {
            stb.append("     AND VBASE.TESTDIV0 = '" + _param._testdiv + "' ");
        }
        if (!"CNT".equals(selectDiv)) {
            stb.append(" ORDER BY ");
            stb.append("     VBASE.EXAMNO ");
        }
        return stb.toString();
    }

    private void printKatsudou(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrSetForm("KNJL301Q_3.frm", 4);

        PreparedStatement ps = null;
        PreparedStatement psCnt = null;
        ResultSet rs = null;
        ResultSet rsCnt = null;

        try {
            final String sqlCnt = sqlKatsudou("CNT");
            psCnt = db2.prepareStatement(sqlCnt);
            rsCnt = psCnt.executeQuery();
            rsCnt.next();
            final int totalCnt = rsCnt.getInt("CNT");
            int pageCnt = 1;
            int maxLine = 25;
            int lineCnt = 1;
            final int pageSu = totalCnt / maxLine;
            final int pageAmari = totalCnt % maxLine;
            final int totalPage = pageSu + (pageAmari > 0 ? 1 : 0);

            final String sql = sqlKatsudou("");
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            svf.VrsOut("TITLE", _param._entexamyear + "年度　" + _param._testdivName + "入試　自己推薦入試チェックリスト");
            svf.VrsOut("DATE", KNJ_EditDate.h_format_S(_param._loginDate, "yyyy年MM月dd日"));

            while (rs.next()) {
                if (lineCnt > maxLine) {
                    lineCnt = 1;
                    pageCnt++;
                }
                svf.VrsOut("PAGE1", String.valueOf(pageCnt));
                svf.VrsOut("PAGE2", String.valueOf(totalPage));
                final String examno = rs.getString("EXAMNO");
                final String name = rs.getString("NAME");
                final String remark1 = rs.getString("REMARK1");
                final String remark2 = rs.getString("REMARK2");
                final String remark3 = rs.getString("REMARK3");
                final String remark9 = rs.getString("REMARK9");

                final String[] printRemark1 = KNJ_EditEdit.get_token(remark1, 50, 4);
                final String[] printRemark2 = KNJ_EditEdit.get_token(remark2, 50, 4);
                final String[] printRemark3 = KNJ_EditEdit.get_token(remark3, 50, 4);
                final String[] printRemark9 = KNJ_EditEdit.get_token(remark9, 50, 2);

                svf.VrsOut("EXAM_NO", examno);
                svf.VrsOut("NAME", name);
                printBunkatsu(svf, printRemark1, "AO1_", "");
                printBunkatsu(svf, printRemark2, "AO2_", "");
                printBunkatsu(svf, printRemark3, "AO3_", "");
                printBunkatsu(svf, printRemark9, "NOTE", "");

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

    private String sqlKatsudou(final String selectDiv) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        if ("CNT".equals(selectDiv)) {
            stb.append("     COUNT(*) AS CNT ");
        } else {
            stb.append("     VBASE.EXAMNO, ");
            stb.append("     VBASE.NAME, ");
            stb.append("     CONF_DD.REMARK1, ");
            stb.append("     CONF_DD.REMARK2, ");
            stb.append("     CONF_DD.REMARK3, ");
            stb.append("     CONF_DD.REMARK9 ");
        }
        stb.append(" FROM ");
        stb.append("     V_ENTEXAM_APPLICANTBASE_DAT VBASE ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT CONF_DD ON VBASE.ENTEXAMYEAR = CONF_DD.ENTEXAMYEAR ");
        stb.append("          AND VBASE.APPLICANTDIV = CONF_DD.APPLICANTDIV ");
        stb.append("          AND VBASE.EXAMNO = CONF_DD.EXAMNO ");
        stb.append("          AND CONF_DD.SEQ = '003' ");
        stb.append(" WHERE ");
        stb.append("     VBASE.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND VBASE.APPLICANTDIV = '" + _param._applicantdiv + "' ");
        if (SCHOOL_KIND_J.equals(_param._schoolKind) || SCHOOL_KIND_P.equals(_param._schoolKind)) {
            stb.append("     AND VBASE.TESTDIV = '" + _param._testdiv + "' ");
        } else {
            stb.append("     AND VBASE.TESTDIV0 = '" + _param._testdiv + "' ");
        }
        if (!"CNT".equals(selectDiv)) {
            stb.append(" ORDER BY ");
            stb.append("     VBASE.EXAMNO ");
        }
        return stb.toString();
    }

    private void printBunkatsu(final Vrw32alp svf, final String[] printData, final String fieldName, final String setFlg) {
        if (null == printData) {
            return;
        }
        int setCnt = ("2".equals(setFlg)) ? 3 :("3".equals(setFlg)) ? 5: 1;
        for (int i = 0; i < printData.length; i++) {
            final String setStr = printData[i];
            svf.VrsOut(fieldName + setCnt, setStr);
            setCnt++;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 63745 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _applicantdiv;
        final String _testdiv;
        final String _testdivName;
        final String _taisyou;
        final String _entexamyear;
        final String _loginYear;
        final String _loginSemester;
        final String _loginDate;
        final String _prgid;
        final String _schoolKind;
        final String _nameMstTestDiv;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _schoolKind = request.getParameter("SCHOOLKIND");
            _nameMstTestDiv = SCHOOL_KIND_P.equals(_schoolKind) ? "LP24" : SCHOOL_KIND_J.equals(_schoolKind) ? "L024" : "L045";
            _applicantdiv = request.getParameter("APPLICANTDIV");
            _testdiv = request.getParameter("TESTDIV");
            _taisyou = request.getParameter("TAISYOU");
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _loginYear = request.getParameter("LOGIN_YEAR");
            _loginSemester = request.getParameter("LOGIN_SEMESTER");
            _loginDate = request.getParameter("LOGIN_DATE");
            _prgid = request.getParameter("PRGID");
            _testdivName = getTestDivName(db2);
        }

        private String getTestDivName(final DB2UDB db2) throws SQLException {
            String retStr = "";
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     ABBV1 ");
            stb.append(" FROM ");
            stb.append("     NAME_MST ");
            stb.append(" WHERE ");
            stb.append("     NAMECD1 = '" + _nameMstTestDiv + "' ");
            stb.append("     AND NAMECD2 = '" + _testdiv + "' ");

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                rs.next();
                retStr = rs.getString("ABBV1");
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retStr;
        }

    }
}

// eof

