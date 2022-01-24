/**     プロジェクト/プロパティ/javaコード・スタイル/コード・テンプレート/パターン       ***/

/*
 * $Id: 1e5df5c08f4dc32fdfcebd1eadc2ce40ad1fcddc $
 *
 * 作成日: 2018/04/05
 * 作成者: yamashiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJP;

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

public class KNJP806 {

    private static final Log log = LogFactory.getLog(KNJP806.class);

    private final int TEISHUTSU = 1;
    private final int HIKAE = 2;
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

            if ("1".equals(_param._syoseki)) {
                printMainSyoseki(db2, svf, TEISHUTSU);
                printMainSyoseki(db2, svf, HIKAE);
            }

            if ("1".equals(_param._uchiwake)) {
                printMainUchiwake(db2, svf);
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

    private void printMainSyoseki(final DB2UDB db2, final Vrw32alp svf, final int printDiv) {
        for (int hrCnt = 0; hrCnt < _param._category_selected.length; hrCnt++) {
            final String gradeHr = _param._category_selected[hrCnt];

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getSqlSyoseki(gradeHr);
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                String befTextBookDiv = "";
                int totalMoney = 0;
                int rowCnt = 1;
                while (rs.next()) {
                    final String textBookDiv = rs.getString("TEXTBOOKDIV");
                    final String hrName = rs.getString("HR_NAME");
                    final String staffName = rs.getString("STAFFNAME");
                    final String textbookName = rs.getString("TEXTBOOKNAME");
                    final String companyName = rs.getString("ISSUECOMPANYNAME");
                    final String unitPrice = rs.getString("TEXTBOOKUNITPRICE");
                    final String paidCnt = rs.getString("PAID_CNT");
                    final String paidMoney = rs.getString("PAID_MONEY");
                    if (!befTextBookDiv.equals(textBookDiv)) {
                        if (!befTextBookDiv.equals("")) {
                            svf.VrsOutn("TOTAL", 21, String.valueOf(totalMoney));
                            svf.VrEndPage();
                        }

                        svf.VrSetForm("KNJP806_2.frm", 1);
                        totalMoney = 0;
                        rowCnt = 1;
                    }
                    if (printDiv == HIKAE) {
                        svf.VrsOut("COPY_NAME1", "〇");
                        svf.VrsOut("COPY_NAME2", "控");
                    }
                    svf.VrsOut("TITLE", "書籍入金票");
                    svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(db2, _param._printDay));
                    svf.VrsOut("HR_NAME", hrName);
                    svf.VrsOut("TEACHER_NAME", staffName);

                    final String bookField = KNJ_EditEdit.getMS932ByteLength(textbookName) > 30 ? "2" : "1";
                    svf.VrsOutn("BOOK_NAME" + bookField, rowCnt, textbookName);
                    final String companyField = KNJ_EditEdit.getMS932ByteLength(companyName) > 24 ? "2" : "1";
                    svf.VrsOutn("PUBLISHER_NAME" + companyField, rowCnt, companyName);

                    svf.VrsOutn("PRICE", rowCnt, unitPrice);
                    svf.VrsOutn("NUM", rowCnt, paidCnt);
                    svf.VrsOutn("TOTAL", rowCnt, paidMoney);
                    if (null != paidMoney && !"".equals(paidMoney)) {
                        totalMoney += Integer.parseInt(paidMoney);
                    }
                    rowCnt++;
                    befTextBookDiv = textBookDiv;
                    _hasData = true;
                }
                if (_hasData) {
                    svf.VrsOutn("TOTAL", 21, String.valueOf(totalMoney));
                    svf.VrEndPage();
                }

            } catch (SQLException ex) {
                log.debug("Exception:", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

    }

    private String getSqlSyoseki(final String gradeHr) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH SCH_T AS ( ");
        stb.append(" SELECT ");
        stb.append("     REGD.YEAR, ");
        stb.append("     REGD.GRADE, ");
        stb.append("     REGDG.SCHOOL_KIND, ");
        stb.append("     REGD.HR_CLASS, ");
        stb.append("     REGDH.HR_NAME, ");
        stb.append("     REGDH.HR_NAMEABBV, ");
        stb.append("     STAFF.STAFFNAME, ");
        stb.append("     REGD.ATTENDNO, ");
        stb.append("     REGD.SCHREGNO, ");
        stb.append("     BASE.NAME, ");
        stb.append("     BASE.NAME_KANA ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT REGD ");
        stb.append("     LEFT JOIN SCHREG_BASE_MST BASE ON REGD.SCHREGNO = BASE.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT REGDH ON REGD.YEAR = REGDH.YEAR ");
        stb.append("          AND REGD.SEMESTER = REGDH.SEMESTER ");
        stb.append("          AND REGD.GRADE = REGDH.GRADE ");
        stb.append("          AND REGD.HR_CLASS = REGDH.HR_CLASS ");
        stb.append("     LEFT JOIN SCHREG_REGD_GDAT REGDG ON REGD.YEAR = REGDG.YEAR ");
        stb.append("          AND REGD.GRADE = REGDG.GRADE ");
        stb.append("     LEFT JOIN STAFF_MST STAFF ON REGDH.TR_CD1 = STAFF.STAFFCD ");
        stb.append(" WHERE ");
        stb.append("     REGD.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("     AND REGD.SEMESTER = '" + _param._ctrlSemester + "' ");
        stb.append("     AND REGD.GRADE || REGD.HR_CLASS = '" + gradeHr + "' ");
        stb.append(" ), COL_M_T AS ( ");
        stb.append(" SELECT ");
        stb.append("     COL_GD.SCHOOLCD, ");
        stb.append("     COL_GD.SCHOOL_KIND, ");
        stb.append("     COL_GD.YEAR, ");
        stb.append("     COL_GD.COLLECT_L_CD, ");
        stb.append("     COL_GD.COLLECT_M_CD, ");
        stb.append("     COL_M.COLLECT_M_NAME ");
        stb.append(" FROM ");
        stb.append("     COLLECT_GRP_DAT COL_GD ");
        stb.append("     LEFT JOIN COLLECT_SLIP_DAT COL_SLIP ON COL_GD.SCHOOLCD = COL_SLIP.SCHOOLCD ");
        stb.append("          AND COL_GD.SCHOOL_KIND = COL_SLIP.SCHOOL_KIND ");
        stb.append("          AND COL_GD.YEAR = COL_SLIP.YEAR ");
        stb.append("          AND (COL_GD.COLLECT_GRP_CD = COL_SLIP.COLLECT_GRP_CD OR COL_SLIP.COLLECT_GRP_CD = '0000') ");
        stb.append("          AND COL_SLIP.CANCEL_DATE IS NULL ");
        stb.append("     LEFT JOIN COLLECT_SLIP_M_DAT COL_SLIPM ON COL_SLIP.SCHOOLCD = COL_SLIPM.SCHOOLCD ");
        stb.append("          AND COL_SLIP.SCHOOL_KIND = COL_SLIPM.SCHOOL_KIND ");
        stb.append("          AND COL_SLIP.YEAR = COL_SLIPM.YEAR ");
        stb.append("          AND COL_SLIP.SLIP_NO = COL_SLIPM.SLIP_NO ");
        stb.append("          AND COL_GD.COLLECT_L_CD = COL_SLIPM.COLLECT_L_CD ");
        stb.append("          AND COL_GD.COLLECT_M_CD = COL_SLIPM.COLLECT_M_CD ");
        stb.append("     INNER JOIN COLLECT_M_MST COL_M ON COL_SLIPM.SCHOOLCD = COL_M.SCHOOLCD ");
        stb.append("           AND COL_SLIPM.SCHOOL_KIND = COL_M.SCHOOL_KIND ");
        stb.append("           AND COL_SLIPM.YEAR = COL_M.YEAR ");
        stb.append("           AND COL_SLIPM.COLLECT_L_CD = COL_M.COLLECT_L_CD ");
        stb.append("           AND COL_SLIPM.COLLECT_M_CD = COL_M.COLLECT_M_CD ");
        stb.append("           AND COL_M.TEXTBOOKDIV IS NOT NULL ");
        stb.append(" WHERE ");
        stb.append("     COL_GD.SCHOOLCD = '" + _param._schoolCd + "' ");
        stb.append("     AND EXISTS( ");
        stb.append("             SELECT ");
        stb.append("                 'x' ");
        stb.append("             FROM ");
        stb.append("                 SCH_T ");
        stb.append("             WHERE ");
        stb.append("                 SCH_T.SCHOOL_KIND = COL_GD.SCHOOL_KIND ");
        stb.append("                 AND SCH_T.YEAR = COL_GD.YEAR ");
        stb.append("                 AND SCH_T.SCHREGNO = COL_SLIP.SCHREGNO ");
        stb.append("         ) ");
        stb.append(" GROUP BY ");
        stb.append("     COL_GD.SCHOOLCD, ");
        stb.append("     COL_GD.SCHOOL_KIND, ");
        stb.append("     COL_GD.YEAR, ");
        stb.append("     COL_GD.COLLECT_L_CD, ");
        stb.append("     COL_GD.COLLECT_M_CD, ");
        stb.append("     COL_M.COLLECT_M_NAME ");
        stb.append(" ), SLIP_T AS ( ");
        stb.append(" SELECT ");
        stb.append("     SCH_T.SCHREGNO, ");
        stb.append("     COL_M_T.SCHOOLCD, ");
        stb.append("     COL_M_T.SCHOOL_KIND, ");
        stb.append("     COL_M_T.YEAR, ");
        stb.append("     COL_SLIPM.SLIP_NO ");
        stb.append(" FROM ");
        stb.append("     SCH_T ");
        stb.append("     LEFT JOIN COL_M_T ON SCH_T.YEAR = COL_M_T.YEAR ");
        stb.append("     LEFT JOIN COLLECT_SLIP_DAT COL_SLIP ON COL_M_T.SCHOOLCD = COL_SLIP.SCHOOLCD ");
        stb.append("          AND COL_M_T.SCHOOL_KIND = COL_SLIP.SCHOOL_KIND ");
        stb.append("          AND COL_M_T.YEAR = COL_SLIP.YEAR ");
        stb.append("          AND SCH_T.SCHREGNO = COL_SLIP.SCHREGNO ");
        stb.append("          AND COL_SLIP.CANCEL_DATE IS NULL ");
        stb.append("     LEFT JOIN COLLECT_SLIP_M_DAT COL_SLIPM ON COL_SLIP.SCHOOLCD = COL_SLIPM.SCHOOLCD ");
        stb.append("          AND COL_SLIP.SCHOOL_KIND = COL_SLIPM.SCHOOL_KIND ");
        stb.append("          AND COL_SLIP.YEAR = COL_SLIPM.YEAR ");
        stb.append("          AND COL_SLIP.SLIP_NO = COL_SLIPM.SLIP_NO ");
        stb.append("          AND COL_M_T.COLLECT_L_CD = COL_SLIPM.COLLECT_L_CD ");
        stb.append("          AND COL_M_T.COLLECT_M_CD = COL_SLIPM.COLLECT_M_CD ");
        stb.append(" WHERE ");
        stb.append("      COL_SLIPM.SLIP_NO IS NOT NULL ");
        stb.append(" GROUP BY ");
        stb.append("     SCH_T.SCHREGNO, ");
        stb.append("     COL_M_T.SCHOOLCD, ");
        stb.append("     COL_M_T.SCHOOL_KIND, ");
        stb.append("     COL_M_T.YEAR, ");
        stb.append("     COL_SLIPM.SLIP_NO ");
        stb.append(" ), MAIN_T AS ( ");
        stb.append(" SELECT ");
        stb.append("     SCH_T.GRADE, ");
        stb.append("     SCH_T.HR_CLASS, ");
        stb.append("     SCH_T.HR_NAME, ");
        stb.append("     SCH_T.HR_NAMEABBV, ");
        stb.append("     SCH_T.SCHREGNO, ");
        stb.append("     SCH_T.STAFFNAME, ");
        stb.append("     TEXT_M.TEXTBOOKDIV, ");
        stb.append("     SLIP_TEXT.TEXTBOOKCD, ");
        stb.append("     TEXT_M.TEXTBOOKNAME, ");
        stb.append("     SLIP_TEXT.TEXTBOOKUNITPRICE, ");
        stb.append("     COMPANY.ISSUECOMPANYNAME ");
        stb.append(" FROM ");
        stb.append("     SCH_T ");
        stb.append("     LEFT JOIN COL_M_T ON SCH_T.YEAR = COL_M_T.YEAR ");
        stb.append("     LEFT JOIN COLLECT_M_MST COL_M ON COL_M_T.SCHOOLCD = COL_M.SCHOOLCD ");
        stb.append("           AND COL_M_T.SCHOOL_KIND = COL_M.SCHOOL_KIND ");
        stb.append("           AND COL_M_T.YEAR = COL_M.YEAR ");
        stb.append("           AND COL_M_T.COLLECT_L_CD = COL_M.COLLECT_L_CD ");
        stb.append("           AND COL_M_T.COLLECT_M_CD = COL_M.COLLECT_M_CD ");
        stb.append("     LEFT JOIN SLIP_T ON COL_M_T.SCHOOLCD = SLIP_T.SCHOOLCD ");
        stb.append("          AND COL_M_T.SCHOOL_KIND = SLIP_T.SCHOOL_KIND ");
        stb.append("          AND COL_M_T.YEAR = SLIP_T.YEAR ");
        stb.append("          AND SCH_T.SCHREGNO = SLIP_T.SCHREGNO ");
        stb.append("     INNER JOIN COLLECT_SLIP_PLAN_PAID_M_DAT PAIDM ON SLIP_T.SCHOOLCD = PAIDM.SCHOOLCD ");
        stb.append("           AND SLIP_T.SCHOOL_KIND = PAIDM.SCHOOL_KIND ");
        stb.append("           AND SLIP_T.YEAR = PAIDM.YEAR ");
        stb.append("           AND SLIP_T.SLIP_NO = PAIDM.SLIP_NO ");
        stb.append("           AND COL_M_T.COLLECT_L_CD = PAIDM.COLLECT_L_CD ");
        stb.append("           AND COL_M_T.COLLECT_M_CD = PAIDM.COLLECT_M_CD ");
        stb.append("           AND PAIDM.PLAN_PAID_MONEY_DATE BETWEEN '" + StringUtils.replace(_param._paidFday, "/", "-") + "' AND '" + StringUtils.replace(_param._paidTday, "/", "-") + "' ");
        stb.append("     INNER JOIN COLLECT_SLIP_TEXTBOOK_DAT SLIP_TEXT ON SLIP_T.SCHOOLCD = SLIP_TEXT.SCHOOLCD ");
        stb.append("           AND SLIP_T.SCHOOL_KIND = SLIP_TEXT.SCHOOL_KIND ");
        stb.append("           AND SLIP_T.YEAR = SLIP_TEXT.YEAR ");
        stb.append("           AND SLIP_T.SLIP_NO = SLIP_TEXT.SLIP_NO ");
        stb.append("     LEFT JOIN TEXTBOOK_MST TEXT_M ON SLIP_TEXT.TEXTBOOKCD = TEXT_M.TEXTBOOKCD ");
        stb.append("     LEFT JOIN ISSUECOMPANY_MST COMPANY ON TEXT_M.ISSUECOMPANYCD = COMPANY.ISSUECOMPANYCD ");
        stb.append(" GROUP BY ");
        stb.append("     SCH_T.GRADE, ");
        stb.append("     SCH_T.HR_CLASS, ");
        stb.append("     SCH_T.HR_NAME, ");
        stb.append("     SCH_T.HR_NAMEABBV, ");
        stb.append("     SCH_T.SCHREGNO, ");
        stb.append("     SCH_T.STAFFNAME, ");
        stb.append("     TEXT_M.TEXTBOOKDIV, ");
        stb.append("     SLIP_TEXT.TEXTBOOKCD, ");
        stb.append("     TEXT_M.TEXTBOOKNAME, ");
        stb.append("     SLIP_TEXT.TEXTBOOKUNITPRICE, ");
        stb.append("     COMPANY.ISSUECOMPANYNAME ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     MAIN_T.GRADE, ");
        stb.append("     MAIN_T.HR_CLASS, ");
        stb.append("     MAIN_T.HR_NAME, ");
        stb.append("     MAIN_T.HR_NAMEABBV, ");
        stb.append("     MAIN_T.STAFFNAME, ");
        stb.append("     MAIN_T.TEXTBOOKDIV, ");
        stb.append("     MAIN_T.TEXTBOOKCD, ");
        stb.append("     MAIN_T.TEXTBOOKNAME, ");
        stb.append("     MAIN_T.TEXTBOOKUNITPRICE, ");
        stb.append("     MAIN_T.ISSUECOMPANYNAME, ");
        stb.append("     COUNT(MAIN_T.TEXTBOOKCD) AS PAID_CNT, ");
        stb.append("     SUM(MAIN_T.TEXTBOOKUNITPRICE) AS PAID_MONEY ");
        stb.append(" FROM ");
        stb.append("     MAIN_T ");
        stb.append(" GROUP BY ");
        stb.append("     MAIN_T.GRADE, ");
        stb.append("     MAIN_T.HR_CLASS, ");
        stb.append("     MAIN_T.HR_NAME, ");
        stb.append("     MAIN_T.HR_NAMEABBV, ");
        stb.append("     MAIN_T.STAFFNAME, ");
        stb.append("     MAIN_T.TEXTBOOKDIV, ");
        stb.append("     MAIN_T.TEXTBOOKCD, ");
        stb.append("     MAIN_T.TEXTBOOKNAME, ");
        stb.append("     MAIN_T.TEXTBOOKUNITPRICE, ");
        stb.append("     MAIN_T.ISSUECOMPANYNAME ");
        stb.append(" ORDER BY ");
        stb.append("     MAIN_T.TEXTBOOKDIV, ");
        stb.append("     MAIN_T.TEXTBOOKCD, ");
        stb.append("     MAIN_T.GRADE, ");
        stb.append("     MAIN_T.HR_CLASS ");
        return stb.toString();
    }

    private void printMainUchiwake(final DB2UDB db2, final Vrw32alp svf) {
        for (int hrCnt = 0; hrCnt < _param._category_selected.length; hrCnt++) {
            final String gradeHr = _param._category_selected[hrCnt];

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getSqlUchiwake(gradeHr);
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                String befLMcd = "";
                int totalMoney = 0;
                while (rs.next()) {
                    final String hrNameabbv = rs.getString("HR_NAMEABBV");
                    final String attendno = rs.getString("ATTENDNO");
                    final String staffName = rs.getString("STAFFNAME");
                    final String schregNo = rs.getString("SCHREGNO");
                    final String name = rs.getString("NAME");
                    final String nameKana = rs.getString("NAME_KANA");
                    final String lCd = rs.getString("COLLECT_L_CD");
                    final String mCd = rs.getString("COLLECT_M_CD");
                    final String mName = StringUtils.defaultString(rs.getString("COLLECT_M_NAME"));
                    final String paidMoney = rs.getString("PAID_MONEY");
                    final String paidDate = rs.getString("PAID_DATE");
                    if (!befLMcd.equals(lCd + mCd)) {
                        if (!befLMcd.equals("")) {
                            svf.VrsOut("TOTAL_MONEY", String.valueOf(totalMoney));
                            svf.VrEndRecord();
                        }

                        svf.VrSetForm("KNJP806.frm", 4);
                        totalMoney = 0;
                    }
                    svf.VrsOut("TITLE", "入金者名簿");
                    svf.VrsOut("SUBTITLE", "(" + mName + ")");

                    svf.VrsOut("NO", attendno);
                    svf.VrsOut("HR_NAME", hrNameabbv);
                    final String nameField = KNJ_EditEdit.getMS932ByteLength(name) > 16 ? "2" : "1";
                    svf.VrsOut("NAME" + nameField, name);
                    final String kanaField = KNJ_EditEdit.getMS932ByteLength(nameKana) > 24 ? "2" : "1";
                    svf.VrsOut("KANA" + kanaField, nameKana);
                    svf.VrsOut("SCHREGNO", schregNo);
                    String setItemName = KNJ_EditEdit.getMS932ByteLength(mName) > 0 ? mName + "代" : "";
                    svf.VrsOut("ITEM", setItemName);
                    svf.VrsOut("MONEY", paidMoney);
                    svf.VrsOut("DATE", KNJ_EditDate.h_format_SeirekiJP(paidDate));
                    final String[] staffArray = StringUtils.split(staffName, "　");
                    svf.VrsOut("CHARGE", staffArray[0]);
                    if (null != paidMoney && !"".equals(paidMoney)) {
                        totalMoney += Integer.parseInt(paidMoney);
                    }
                    befLMcd = lCd + mCd;
                    svf.VrEndRecord();
                    _hasData = true;
                }
                if (_hasData) {
                    svf.VrsOut("TOTAL_MONEY", String.valueOf(totalMoney));
                    svf.VrEndRecord();
                }

            } catch (SQLException ex) {
                log.debug("Exception:", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

    }

    private String getSqlUchiwake(final String gradeHr) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH SCH_T AS ( ");
        stb.append(" SELECT ");
        stb.append("     REGD.YEAR, ");
        stb.append("     REGD.GRADE, ");
        stb.append("     REGDG.SCHOOL_KIND, ");
        stb.append("     REGD.HR_CLASS, ");
        stb.append("     REGDH.HR_NAME, ");
        stb.append("     REGDH.HR_NAMEABBV, ");
        stb.append("     STAFF.STAFFNAME, ");
        stb.append("     REGD.ATTENDNO, ");
        stb.append("     REGD.SCHREGNO, ");
        stb.append("     BASE.NAME, ");
        stb.append("     BASE.NAME_KANA ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT REGD ");
        stb.append("     LEFT JOIN SCHREG_BASE_MST BASE ON REGD.SCHREGNO = BASE.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT REGDH ON REGD.YEAR = REGDH.YEAR ");
        stb.append("          AND REGD.SEMESTER = REGDH.SEMESTER ");
        stb.append("          AND REGD.GRADE = REGDH.GRADE ");
        stb.append("          AND REGD.HR_CLASS = REGDH.HR_CLASS ");
        stb.append("     LEFT JOIN SCHREG_REGD_GDAT REGDG ON REGD.YEAR = REGDG.YEAR ");
        stb.append("          AND REGD.GRADE = REGDG.GRADE ");
        stb.append("     LEFT JOIN STAFF_MST STAFF ON REGDH.TR_CD1 = STAFF.STAFFCD ");
        stb.append(" WHERE ");
        stb.append("     REGD.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("     AND REGD.SEMESTER = '" + _param._ctrlSemester + "' ");
        stb.append("     AND REGD.GRADE || REGD.HR_CLASS = '" + gradeHr + "' ");
        stb.append(" ), COL_M_T AS ( ");
        stb.append(" SELECT ");
        stb.append("     COL_GD.SCHOOLCD, ");
        stb.append("     COL_GD.SCHOOL_KIND, ");
        stb.append("     COL_GD.YEAR, ");
        stb.append("     COL_GD.COLLECT_L_CD, ");
        stb.append("     COL_GD.COLLECT_M_CD, ");
        stb.append("     COL_M.COLLECT_M_NAME ");
        stb.append(" FROM ");
        stb.append("     COLLECT_GRP_DAT COL_GD ");
        stb.append("     LEFT JOIN COLLECT_SLIP_DAT COL_SLIP ON COL_GD.SCHOOLCD = COL_SLIP.SCHOOLCD ");
        stb.append("          AND COL_GD.SCHOOL_KIND = COL_SLIP.SCHOOL_KIND ");
        stb.append("          AND COL_GD.YEAR = COL_SLIP.YEAR ");
        stb.append("          AND (COL_GD.COLLECT_GRP_CD = COL_SLIP.COLLECT_GRP_CD OR COL_SLIP.COLLECT_GRP_CD = '0000') ");
        stb.append("          AND COL_SLIP.CANCEL_DATE IS NULL ");
        stb.append("     LEFT JOIN COLLECT_SLIP_M_DAT COL_SLIPM ON COL_SLIP.SCHOOLCD = COL_SLIPM.SCHOOLCD ");
        stb.append("          AND COL_SLIP.SCHOOL_KIND = COL_SLIPM.SCHOOL_KIND ");
        stb.append("          AND COL_SLIP.YEAR = COL_SLIPM.YEAR ");
        stb.append("          AND COL_SLIP.SLIP_NO = COL_SLIPM.SLIP_NO ");
        stb.append("          AND COL_GD.COLLECT_L_CD = COL_SLIPM.COLLECT_L_CD ");
        stb.append("          AND COL_GD.COLLECT_M_CD = COL_SLIPM.COLLECT_M_CD ");
        stb.append("     INNER JOIN COLLECT_M_MST COL_M ON COL_SLIPM.SCHOOLCD = COL_M.SCHOOLCD ");
        stb.append("           AND COL_SLIPM.SCHOOL_KIND = COL_M.SCHOOL_KIND ");
        stb.append("           AND COL_SLIPM.YEAR = COL_M.YEAR ");
        stb.append("           AND COL_SLIPM.COLLECT_L_CD = COL_M.COLLECT_L_CD ");
        stb.append("           AND COL_SLIPM.COLLECT_M_CD = COL_M.COLLECT_M_CD ");
        stb.append("           AND COL_M.TEXTBOOKDIV IS NOT NULL ");
        stb.append(" WHERE ");
        stb.append("     COL_GD.SCHOOLCD = '" + _param._schoolCd + "' ");
        stb.append("     AND EXISTS( ");
        stb.append("             SELECT ");
        stb.append("                 'x' ");
        stb.append("             FROM ");
        stb.append("                 SCH_T ");
        stb.append("             WHERE ");
        stb.append("                 SCH_T.SCHOOL_KIND = COL_GD.SCHOOL_KIND ");
        stb.append("                 AND SCH_T.YEAR = COL_GD.YEAR ");
        stb.append("                 AND SCH_T.SCHREGNO = COL_SLIP.SCHREGNO ");
        stb.append("         ) ");
        stb.append(" GROUP BY ");
        stb.append("     COL_GD.SCHOOLCD, ");
        stb.append("     COL_GD.SCHOOL_KIND, ");
        stb.append("     COL_GD.YEAR, ");
        stb.append("     COL_GD.COLLECT_L_CD, ");
        stb.append("     COL_GD.COLLECT_M_CD, ");
        stb.append("     COL_M.COLLECT_M_NAME ");
        stb.append(" ), SLIP_T AS ( ");
        stb.append(" SELECT ");
        stb.append("     SCH_T.SCHREGNO, ");
        stb.append("     COL_M_T.SCHOOLCD, ");
        stb.append("     COL_M_T.SCHOOL_KIND, ");
        stb.append("     COL_M_T.YEAR, ");
        stb.append("     COL_SLIPM.SLIP_NO ");
        stb.append(" FROM ");
        stb.append("     SCH_T ");
        stb.append("     LEFT JOIN COL_M_T ON SCH_T.YEAR = COL_M_T.YEAR ");
        stb.append("     LEFT JOIN COLLECT_SLIP_DAT COL_SLIP ON COL_M_T.SCHOOLCD = COL_SLIP.SCHOOLCD ");
        stb.append("          AND COL_M_T.SCHOOL_KIND = COL_SLIP.SCHOOL_KIND ");
        stb.append("          AND COL_M_T.YEAR = COL_SLIP.YEAR ");
        stb.append("          AND SCH_T.SCHREGNO = COL_SLIP.SCHREGNO ");
        stb.append("          AND COL_SLIP.CANCEL_DATE IS NULL ");
        stb.append("     LEFT JOIN COLLECT_SLIP_M_DAT COL_SLIPM ON COL_SLIP.SCHOOLCD = COL_SLIPM.SCHOOLCD ");
        stb.append("          AND COL_SLIP.SCHOOL_KIND = COL_SLIPM.SCHOOL_KIND ");
        stb.append("          AND COL_SLIP.YEAR = COL_SLIPM.YEAR ");
        stb.append("          AND COL_SLIP.SLIP_NO = COL_SLIPM.SLIP_NO ");
        stb.append("          AND COL_M_T.COLLECT_L_CD = COL_SLIPM.COLLECT_L_CD ");
        stb.append("          AND COL_M_T.COLLECT_M_CD = COL_SLIPM.COLLECT_M_CD ");
        stb.append(" WHERE ");
        stb.append("      COL_SLIP.SLIP_NO IS NOT NULL ");
        stb.append(" GROUP BY ");
        stb.append("     SCH_T.SCHREGNO, ");
        stb.append("     COL_M_T.SCHOOLCD, ");
        stb.append("     COL_M_T.SCHOOL_KIND, ");
        stb.append("     COL_M_T.YEAR, ");
        stb.append("     COL_SLIPM.SLIP_NO ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     SCH_T.GRADE, ");
        stb.append("     SCH_T.HR_CLASS, ");
        stb.append("     SCH_T.HR_NAME, ");
        stb.append("     SCH_T.HR_NAMEABBV, ");
        stb.append("     SCH_T.STAFFNAME, ");
        stb.append("     SCH_T.ATTENDNO, ");
        stb.append("     SCH_T.SCHREGNO, ");
        stb.append("     SCH_T.NAME, ");
        stb.append("     SCH_T.NAME_KANA, ");
        stb.append("     COL_M_T.COLLECT_L_CD, ");
        stb.append("     COL_M_T.COLLECT_M_CD, ");
        stb.append("     COL_M.COLLECT_M_NAME, ");
        stb.append("     SUM(PAIDM.PLAN_PAID_MONEY) AS PAID_MONEY, ");
        stb.append("     MAX(PAIDM.PLAN_PAID_MONEY_DATE) AS PAID_DATE ");
        stb.append(" FROM ");
        stb.append("     SCH_T ");
        stb.append("     LEFT JOIN COL_M_T ON SCH_T.YEAR = COL_M_T.YEAR ");
        stb.append("     LEFT JOIN COLLECT_M_MST COL_M ON COL_M_T.SCHOOLCD = COL_M.SCHOOLCD ");
        stb.append("           AND COL_M_T.SCHOOL_KIND = COL_M.SCHOOL_KIND ");
        stb.append("           AND COL_M_T.YEAR = COL_M.YEAR ");
        stb.append("           AND COL_M_T.COLLECT_L_CD = COL_M.COLLECT_L_CD ");
        stb.append("           AND COL_M_T.COLLECT_M_CD = COL_M.COLLECT_M_CD ");
        stb.append("     LEFT JOIN SLIP_T ON COL_M_T.SCHOOLCD = SLIP_T.SCHOOLCD ");
        stb.append("          AND COL_M_T.SCHOOL_KIND = SLIP_T.SCHOOL_KIND ");
        stb.append("          AND COL_M_T.YEAR = SLIP_T.YEAR ");
        stb.append("          AND SCH_T.SCHREGNO = SLIP_T.SCHREGNO ");
        stb.append("     LEFT JOIN COLLECT_SLIP_PLAN_PAID_M_DAT PAIDM ON SLIP_T.SCHOOLCD = PAIDM.SCHOOLCD ");
        stb.append("          AND SLIP_T.SCHOOL_KIND = PAIDM.SCHOOL_KIND ");
        stb.append("          AND SLIP_T.YEAR = PAIDM.YEAR ");
        stb.append("          AND SLIP_T.SLIP_NO = PAIDM.SLIP_NO ");
        stb.append("          AND COL_M_T.COLLECT_L_CD = PAIDM.COLLECT_L_CD ");
        stb.append("          AND COL_M_T.COLLECT_M_CD = PAIDM.COLLECT_M_CD ");
        stb.append(" GROUP BY ");
        stb.append("     SCH_T.GRADE, ");
        stb.append("     SCH_T.HR_CLASS, ");
        stb.append("     SCH_T.HR_NAME, ");
        stb.append("     SCH_T.HR_NAMEABBV, ");
        stb.append("     SCH_T.STAFFNAME, ");
        stb.append("     SCH_T.ATTENDNO, ");
        stb.append("     SCH_T.SCHREGNO, ");
        stb.append("     SCH_T.NAME, ");
        stb.append("     SCH_T.NAME_KANA, ");
        stb.append("     COL_M_T.COLLECT_L_CD, ");
        stb.append("     COL_M_T.COLLECT_M_CD, ");
        stb.append("     COL_M.COLLECT_M_NAME ");
        stb.append(" ORDER BY ");
        stb.append("     COL_M_T.COLLECT_L_CD, ");
        stb.append("     COL_M_T.COLLECT_M_CD, ");
        stb.append("     SCH_T.GRADE, ");
        stb.append("     SCH_T.HR_CLASS, ");
        stb.append("     SCH_T.ATTENDNO ");
        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 70205 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final String _schoolCd;
        private final String _syoseki;
        private final String _paidFday;
        private final String _paidTday;
        private final String _printDay;
        private final String _uchiwake;
        final String[] _category_selected;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _schoolCd = request.getParameter("SCHOOLCD");
            _syoseki = request.getParameter("SYOSEKI");
            _paidFday = request.getParameter("PAID_FDAY");
            _paidTday = request.getParameter("PAID_TDAY");
            _printDay = request.getParameter("PRINT_DAY");
            _uchiwake = request.getParameter("UCHIWAKE");
            _category_selected = request.getParameterValues("CLASS_SELECTED");
        }
    }
}

// eof
