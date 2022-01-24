/**     プロジェクト/プロパティ/javaコード・スタイル/コード・テンプレート/パターン       ***/

/*
 * $Id: 2d1d88501df8bec85e73ec3b836cf4bf026d9b2c $
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

public class KNJP807 {

    private static final Log log = LogFactory.getLog(KNJP807.class);

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

            if ("1".equals(_param._nyuukin)) {
                printMainNyuukin(db2, svf);
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

    private void printMainNyuukin(final DB2UDB db2, final Vrw32alp svf) {
        for (int hrCnt = 0; hrCnt < _param._category_selected.length; hrCnt++) {
            final String gradeHr = _param._category_selected[hrCnt];

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getSqlNyuukin(gradeHr);
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                int rowCnt = 1;
                while (rs.next()) {
                    final String hrName = rs.getString("HR_NAME");
                    final String staffName = rs.getString("STAFFNAME");
                    final String mName = rs.getString("COLLECT_M_NAME");
                    final String mPrice = rs.getString("COLLECT_M_MONEY");
                    final String paidCnt = rs.getString("PAID_CNT");
                    final String paidMoney = rs.getString("PAID_MONEY");
                    svf.VrSetForm("KNJP807_1.frm", 1);

                    svf.VrsOut("DATE1", KNJ_EditDate.h_format_JP(_param._printDay));
                    svf.VrsOut("HR_NAME1", hrName);
                    svf.VrsOut("TEACHER_NAME1", staffName);

                    final String mNameField = KNJ_EditEdit.getMS932ByteLength(mName) > 16 ? "2" : "1";
                    svf.VrsOutn("ITEM1_" + mNameField, rowCnt, mName);

                    svf.VrsOutn("PRICE1", rowCnt, mPrice);
                    svf.VrsOutn("NUM1", rowCnt, paidCnt);
                    svf.VrsOutn("SUBTOTAL1", rowCnt, paidMoney);
                    svf.VrsOutn("SUBTOTAL1", 8, paidMoney);

                    svf.VrsOut("DATE2", KNJ_EditDate.h_format_JP(_param._printDay));
                    svf.VrsOut("HR_NAME2", hrName);
                    svf.VrsOut("TEACHER_NAME2", staffName);

                    final String mNameField2 = KNJ_EditEdit.getMS932ByteLength(mName) > 16 ? "2" : "1";
                    svf.VrsOutn("ITEM2_" + mNameField2, rowCnt, mName);

                    svf.VrsOutn("PRICE2", rowCnt, mPrice);
                    svf.VrsOutn("NUM2", rowCnt, paidCnt);
                    svf.VrsOutn("SUBTOTAL2", rowCnt, paidMoney);
                    svf.VrsOutn("SUBTOTAL2", 8, paidMoney);

                    rowCnt++;
                    _hasData = true;
                }
                if (_hasData) {
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

    private String getSqlNyuukin(final String gradeHr) {
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
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     SCH_T.HR_NAME, ");
        stb.append("     SCH_T.STAFFNAME, ");
        stb.append("     COL_M.COLLECT_M_NAME, ");
        stb.append("     COL_M.COLLECT_M_MONEY, ");
        stb.append("     COUNT(COL_M.COLLECT_M_CD) AS PAID_CNT, ");
        stb.append("     SUM(PAIDM.PLAN_PAID_MONEY) AS PAID_MONEY ");
        stb.append(" FROM ");
        stb.append("     SCH_T ");
        stb.append("     INNER JOIN COLLECT_SLIP_PLAN_PAID_M_DAT PAIDM ON PAIDM.SCHOOLCD = '" + _param._schoolCd + "' ");
        stb.append("           AND SCH_T.SCHOOL_KIND = PAIDM.SCHOOL_KIND ");
        stb.append("           AND SCH_T.YEAR = PAIDM.YEAR ");
        stb.append("           AND PAIDM.COLLECT_L_CD || PAIDM.COLLECT_M_CD = '" + _param._collectMCd + "' ");
        stb.append("           AND SCH_T.SCHREGNO = PAIDM.SCHREGNO ");
        stb.append("           AND PAIDM.PLAN_PAID_MONEY_DATE BETWEEN '" + StringUtils.replace(_param._paidFday, "/", "-") + "' AND '" + StringUtils.replace(_param._paidTday, "/", "-") + "' ");
        stb.append("     INNER JOIN COLLECT_M_MST COL_M ON PAIDM.SCHOOLCD = COL_M.SCHOOLCD ");
        stb.append("           AND PAIDM.SCHOOL_KIND = COL_M.SCHOOL_KIND ");
        stb.append("           AND PAIDM.YEAR = COL_M.YEAR ");
        stb.append("           AND PAIDM.COLLECT_L_CD = COL_M.COLLECT_L_CD ");
        stb.append("           AND PAIDM.COLLECT_M_CD = COL_M.COLLECT_M_CD ");
        stb.append(" GROUP BY ");
        stb.append("     SCH_T.HR_NAME, ");
        stb.append("     SCH_T.STAFFNAME, ");
        stb.append("     COL_M.COLLECT_M_NAME, ");
        stb.append("     COL_M.COLLECT_M_MONEY ");

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

                        svf.VrSetForm("KNJP807_2.frm", 4);
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
        stb.append("     COL_M.SCHOOLCD, ");
        stb.append("     COL_M.SCHOOL_KIND, ");
        stb.append("     COL_M.YEAR, ");
        stb.append("     COL_M.COLLECT_L_CD, ");
        stb.append("     COL_M.COLLECT_M_CD, ");
        stb.append("     COL_M.COLLECT_M_NAME ");
        stb.append(" FROM ");
        stb.append("     COLLECT_M_MST COL_M ");
        stb.append(" WHERE ");
        stb.append("     COL_M.SCHOOLCD = '" + _param._schoolCd + "' ");
        stb.append("     AND COL_M.SCHOOL_KIND = '" + _param._schoolKind + "' ");
        stb.append("     AND COL_M.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("     AND COL_M.COLLECT_L_CD || COL_M.COLLECT_M_CD = '" + _param._collectMCd + "' ");
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
        log.fatal("$Revision: 59645 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final String _collectMCd;
        private final String _schoolCd;
        private final String _schoolKind;
        private final String _nyuukin;
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
            _schoolKind = request.getParameter("SCHOOL_KIND");
            _collectMCd = request.getParameter("COLLECT_M_CD");
            _nyuukin = request.getParameter("NYUUKIN");
            _paidFday = request.getParameter("PAID_FDAY");
            _paidTday = request.getParameter("PAID_TDAY");
            _printDay = request.getParameter("PRINT_DAY");
            _uchiwake = request.getParameter("UCHIWAKE");
            _category_selected = request.getParameterValues("CLASS_SELECTED");
        }
    }
}

// eof
