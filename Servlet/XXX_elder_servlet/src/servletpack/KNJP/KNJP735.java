/**     プロジェクト/プロパティ/javaコード・スタイル/コード・テンプレート/パターン       ***/

/*
 * $Id: b217fe21bd09b924ec378178b20dadcde0e47791 $
 *
 * 作成日: 2018/08/09
 * 作成者: yamashiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJP;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJP735 {

    private static final Log log = LogFactory.getLog(KNJP735.class);

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
        svf.VrSetForm("KNJP735.frm", 4);
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getStudentInfoData();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            String befSlip = "";
            int groupNo = 0;
            int lineCnt = 1;
            final Map grdMap = new HashedMap();
            grdMap.put("2", "退学");
            grdMap.put("3", "転学");
            grdMap.put("6", "除籍");
            grdMap.put("7", "転籍");
            while (rs.next()) {
                svf.VrsOut("TITLE", "未入金者一覧");
                svf.VrsOut("PAID_DATE", KNJ_EditDate.h_format_SeirekiJP(StringUtils.replace(_param._paidDate, "/", "-")));
                svf.VrsOut("PAID_LIMIT_MONTH", _param._paidLimitMonth + "月");

                final String schregno = rs.getString("SCHREGNO");
                final String name = rs.getString("NAME");
                final String hrAttendName = rs.getString("HR_ATTEND_NAME");
                final String slipNo = rs.getString("SLIP_NO");
                final int planMoney     = rs.getInt("PLAN_MONEY");
                final int country       = rs.getInt("COUNTRY_MONEY");
                final int countryAdd    = rs.getInt("COUNTRY_ADDMONEY");
                final int pref1         = rs.getInt("PREF_MONEY1");
                final int pref2         = rs.getInt("PREF_MONEY2");
                final int burden1       = rs.getInt("BURDEN_CHARGE1");
                final int burden2       = rs.getInt("BURDEN_CHARGE2");
                final int school1       = rs.getInt("SCHOOL_1");
                final int school2       = rs.getInt("SCHOOL_2");
                final int setPlanMoney = planMoney - country - countryAdd - pref1 - pref2 - burden1 - burden2 - school1 - school2;
                final String paidLimitDate = rs.getString("PAID_LIMIT_DATE");
                String grdDate = "";
                if (!"1".equals(_param._searchDiv)) {
                    final String checkGrdDiv = rs.getString("GRD_DIV");
                    final String checkGrdDate = rs.getString("GRD_DATE");
                    if (grdMap.containsKey(checkGrdDiv) && !StringUtils.isEmpty(checkGrdDate)) {
                        grdDate = checkGrdDate.replace('-', '/');
                    }
                }

                if (!befSlip.equals(slipNo)) {
                    groupNo++;
                    printLineData(svf, groupNo, schregno, name, hrAttendName, slipNo, grdDate);
                }
                if (lineCnt > 50) {
                    printLineData(svf, groupNo, schregno, name, hrAttendName, slipNo, grdDate);
                    lineCnt = 1;
                }
                svf.VrsOut("SHIRO1", String.valueOf(groupNo));
                svf.VrsOut("SHIRO2", String.valueOf(groupNo));
                svf.VrsOut("SHIRO3", String.valueOf(groupNo));
                svf.VrsOut("SHIRO4", String.valueOf(groupNo));
                svf.VrsOut("SHIRO5", String.valueOf(groupNo));
                svf.VrsOut("SHIRO6", String.valueOf(groupNo));
                svf.VrsOut("PAID_LIMIT_DATE", StringUtils.replace(paidLimitDate, "-", "/"));
                svf.VrsOut("PLAN_MONEY", String.valueOf(setPlanMoney));

                svf.VrEndRecord();
                befSlip = slipNo;
                lineCnt++;
                _hasData = true;
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }

    private void printLineData(final Vrw32alp svf, int groupNo, final String schregno, final String name,
            final String hrAttendName, final String slipNo, final String grdDate) {
        svf.VrsOut("NO", String.valueOf(groupNo));
        svf.VrsOut("GHA", hrAttendName);
        svf.VrsOut("SCHREGNO", schregno);
        final String setNameField = KNJ_EditEdit.getMS932ByteLength(name) > 30 ? "_3" : KNJ_EditEdit.getMS932ByteLength(name) > 24 ? "_2" : "";
        svf.VrsOut("NAME" + setNameField, name);
        svf.VrsOut("GRD_DATE", grdDate);
        svf.VrsOut("GHA", hrAttendName);
        svf.VrsOut("SLIP_NO", slipNo);
    }

    //生徒情報取得
    private String getStudentInfoData() {
        int setYear;
        if ("1".equals(_param._searchDiv)) {
            setYear = (Integer.parseInt(_param._ctrlYear) + 1);
        } else {
            setYear = Integer.parseInt(_param._ctrlYear);
        }

        String tableName = "V_SCHREG_BASE_MST";
        if ("1".equals(_param._searchDiv)) {
            tableName = "FRESHMAN_DAT";
        }
        String setLmonth = "";
        if ("12".equals(_param._paidLimitMonth)) {
            setLmonth = "01";
        } else {
            int monthAdd = Integer.parseInt(_param._paidLimitMonth) + 1;
            setLmonth = monthAdd < 10 ? "0" + monthAdd : String.valueOf(monthAdd);
        }
        int setLimitYear;
        if (Integer.parseInt(_param._paidLimitMonth) < 4 || "12".equals(_param._paidLimitMonth)) {
            setLimitYear = setYear + 1;
        } else {
            setLimitYear = setYear;
        }
        final String setLimitDate = setLimitYear + "-" + setLmonth + "-01";

        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH PRINT_T AS ( ");
        stb.append("     SELECT ");
        stb.append("         PLAN_M.SCHOOLCD, ");
        stb.append("         PLAN_M.SCHOOL_KIND, ");
        stb.append("         PLAN_M.YEAR, ");
        stb.append("         PLAN_M.SLIP_NO, ");
        stb.append("         PLAN_M.SCHREGNO, ");
        stb.append("         SUM(VALUE(PLAN_M.PLAN_MONEY, 0)) - SUM(VALUE(PLAN_M.PAID_MONEY, 0)) AS PLAN_MONEY, ");
        stb.append("         SUM(CASE WHEN COL_M.TEXTBOOKDIV IS NOT NULL THEN 1 ELSE 0 END) AS TEXTBOOK_CNT, ");
        stb.append("         MAX(CASE WHEN SL_D.COLLECT_GRP_CD = '0000' THEN 'AAAA' ELSE SL_D.COLLECT_GRP_CD END) AS COLLECT_GRP_SORT, ");
        stb.append("         MAX(GRP_M.COLLECT_GRP_NAME) AS COLLECT_GRP_NAME, ");
        stb.append("         LIMIT_D.PAID_LIMIT_MONTH, ");
        stb.append("         LIMIT_D.PAID_LIMIT_DATE ");
        stb.append("     FROM ");
        stb.append("         COLLECT_SLIP_PLAN_M_DAT PLAN_M ");
        stb.append("         LEFT JOIN COLLECT_SLIP_DAT SL_D ON SL_D.SCHOOLCD    = PLAN_M.SCHOOLCD ");
        stb.append("                                        AND SL_D.SCHOOL_KIND = PLAN_M.SCHOOL_KIND ");
        stb.append("                                        AND SL_D.YEAR        = PLAN_M.YEAR ");
        stb.append("                                        AND SL_D.SLIP_NO     = PLAN_M.SLIP_NO ");
        stb.append("         LEFT JOIN COLLECT_GRP_MST GRP_M ON SL_D.SCHOOLCD        = GRP_M.SCHOOLCD ");
        stb.append("                                        AND SL_D.SCHOOL_KIND     = GRP_M.SCHOOL_KIND ");
        stb.append("                                        AND SL_D.YEAR            = GRP_M.YEAR ");
        stb.append("                                        AND SL_D.COLLECT_GRP_CD  = GRP_M.COLLECT_GRP_CD ");
        stb.append("         INNER JOIN COLLECT_SLIP_PLAN_LIMITDATE_DAT LIMIT_D ON PLAN_M.SCHOOLCD         = LIMIT_D.SCHOOLCD ");
        stb.append("                                                           AND PLAN_M.SCHOOL_KIND      = LIMIT_D.SCHOOL_KIND ");
        stb.append("                                                           AND PLAN_M.YEAR             = LIMIT_D.YEAR ");
        stb.append("                                                           AND PLAN_M.SCHREGNO         = LIMIT_D.SCHREGNO ");
        stb.append("                                                           AND PLAN_M.SLIP_NO          = LIMIT_D.SLIP_NO ");
        stb.append("                                                           AND PLAN_M.PLAN_YEAR        = LIMIT_D.PLAN_YEAR ");
        stb.append("                                                           AND PLAN_M.PLAN_MONTH       = LIMIT_D.PLAN_MONTH ");
        stb.append("                                                           AND LIMIT_D.PAID_LIMIT_DATE < '" + setLimitDate + "' ");
        stb.append("         LEFT JOIN COLLECT_M_MST COL_M ON PLAN_M.SCHOOLCD     = COL_M.SCHOOLCD ");
        stb.append("                                      AND PLAN_M.SCHOOL_KIND  = COL_M.SCHOOL_KIND ");
        stb.append("                                      AND PLAN_M.YEAR         = COL_M.YEAR ");
        stb.append("                                      AND PLAN_M.COLLECT_L_CD = COL_M.COLLECT_L_CD ");
        stb.append("                                      AND PLAN_M.COLLECT_M_CD = COL_M.COLLECT_M_CD ");
        stb.append("     WHERE ");
        stb.append("             PLAN_M.SCHOOLCD    = '" + _param._schoolCd + "' ");
        if (null != _param._schFlg && !"".equals(_param._schFlg)) {
            if (null != _param._selectSchoolKind && !"".equals(_param._selectSchoolKind)) {
                stb.append("         AND PLAN_M.SCHOOL_KIND IN " + SQLUtils.whereIn(true, _param._selectSchoolKindArray) + " ");
            }
        } else {
            stb.append("         AND PLAN_M.SCHOOL_KIND = '" + _param._schoolKind + "' ");
        }
        stb.append("         AND PLAN_M.YEAR        = '" + setYear + "' ");
        stb.append("         AND SL_D.CANCEL_DATE IS NULL ");
        stb.append("     GROUP BY ");
        stb.append("         PLAN_M.SCHOOLCD, ");
        stb.append("         PLAN_M.SCHOOL_KIND, ");
        stb.append("         PLAN_M.YEAR, ");
        stb.append("         PLAN_M.SLIP_NO, ");
        stb.append("         PLAN_M.SCHREGNO, ");
        stb.append("         LIMIT_D.PAID_LIMIT_MONTH, ");
        stb.append("         LIMIT_D.PAID_LIMIT_DATE ");
        stb.append("     HAVING ");
        stb.append("         SUM(VALUE(PLAN_M.PLAN_MONEY, 0)) - SUM(VALUE(PLAN_M.PAID_MONEY, 0)) > 0 ");
        stb.append("         AND SUM(CASE WHEN COL_M.TEXTBOOKDIV IS NOT NULL THEN 1 ELSE 0 END) = 0 ");
        stb.append("     ) ");
        stb.append(" SELECT DISTINCT ");
        stb.append("     GDAT.SCHOOL_KIND, ");
        stb.append("     SCH_R.GRADE || SCH_R.HR_CLASS || SCH_R.ATTENDNO, ");
        stb.append("     MAIN.SCHREGNO, ");
        stb.append("     MAIN.NAME, ");
        if ("1".equals(_param._searchDiv)) {
            stb.append("     VALUE(MAIN.GRADE, '00') || '-' || VALUE(MAIN.HR_CLASS, '000') || '-' || VALUE(MAIN.ATTENDNO, '000') || '番' AS HR_ATTEND_NAME, ");
        } else {
            stb.append("     SCH_RH.HR_NAME || ' ' || SCH_R.ATTENDNO || '番' AS HR_ATTEND_NAME, ");
            stb.append("     MAIN.GRD_DIV, ");
            stb.append("     MAIN.GRD_DATE, ");
        }
        stb.append("     PRINT_T.COLLECT_GRP_SORT, ");
        stb.append("     PRINT_T.SLIP_NO, ");
        stb.append("     VALUE(PRINT_T.PLAN_MONEY, 0) AS PLAN_MONEY, ");
        stb.append("     VALUE(CASE WHEN REDUC_C.OFFSET_FLG = '1' THEN REDUC_C.DECISION_MONEY ELSE 0 END, 0) AS COUNTRY_MONEY, ");
        stb.append("     VALUE(CASE WHEN REDUC_C.ADD_OFFSET_FLG = '1' THEN REDUC_C.ADD_DECISION_MONEY ELSE 0 END, 0) AS COUNTRY_ADDMONEY, ");
        stb.append("     VALUE(CASE WHEN REDUC_D1.OFFSET_FLG = '1' THEN REDUC_D1.DECISION_MONEY ELSE 0 END, 0) AS PREF_MONEY1, ");
        stb.append("     VALUE(CASE WHEN REDUC_D2.OFFSET_FLG = '1' THEN REDUC_D2.DECISION_MONEY ELSE 0 END, 0) AS PREF_MONEY2, ");
        stb.append("     VALUE(BURDEN_1.BURDEN_CHARGE, 0) AS BURDEN_CHARGE1, ");
        stb.append("     VALUE(BURDEN_2.BURDEN_CHARGE, 0) AS BURDEN_CHARGE2, ");
        stb.append("     VALUE(SCHOOL_1.DECISION_MONEY, 0) AS SCHOOL_1, ");
        stb.append("     VALUE(SCHOOL_2.DECISION_MONEY, 0) AS SCHOOL_2, ");
        stb.append("     PRINT_T.PAID_LIMIT_DATE ");
        stb.append(" FROM ");
        stb.append("     " + tableName + " MAIN ");
        stb.append("     LEFT JOIN SCHREG_REGD_DAT SCH_R ON MAIN.SCHREGNO  = SCH_R.SCHREGNO ");
        stb.append("                                    AND SCH_R.YEAR     = '" + setYear + "' ");
        stb.append("                                    AND SCH_R.SEMESTER = '" + _param._ctrlSemester + "' ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT SCH_RH ON SCH_R.YEAR     = SCH_RH.YEAR ");
        stb.append("                                      AND SCH_R.SEMESTER = SCH_RH.SEMESTER ");
        stb.append("                                      AND SCH_R.GRADE    = SCH_RH.GRADE ");
        stb.append("                                      AND SCH_R.HR_CLASS = SCH_RH.HR_CLASS ");
        if ("1".equals(_param._searchDiv)) {
            stb.append("     LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR  = MAIN.ENTERYEAR ");
            stb.append("                                    AND GDAT.GRADE = MAIN.GRADE ");
        } else {
            stb.append("     LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR  = SCH_R.YEAR ");
            stb.append("                                    AND GDAT.GRADE = SCH_R.GRADE ");
        }
        stb.append("     INNER JOIN PRINT_T ON PRINT_T.SCHREGNO = MAIN.SCHREGNO ");
        stb.append("     LEFT JOIN REDUCTION_COUNTRY_PLAN_DAT REDUC_C ON PRINT_T.SCHOOLCD         = REDUC_C.SCHOOLCD ");
        stb.append("                                                 AND PRINT_T.SCHOOL_KIND      = REDUC_C.SCHOOL_KIND ");
        stb.append("                                                 AND PRINT_T.YEAR             = REDUC_C.YEAR ");
        stb.append("                                                 AND PRINT_T.SLIP_NO          = REDUC_C.SLIP_NO ");
        stb.append("                                                 AND PRINT_T.PAID_LIMIT_MONTH = REDUC_C.PLAN_MONTH ");
        stb.append("     LEFT JOIN REDUCTION_PLAN_DAT REDUC_D1 ON PRINT_T.SCHOOLCD          = REDUC_D1.SCHOOLCD ");
        stb.append("                                          AND PRINT_T.SCHOOL_KIND       = REDUC_D1.SCHOOL_KIND ");
        stb.append("                                          AND PRINT_T.YEAR              = REDUC_D1.YEAR ");
        stb.append("                                          AND REDUC_D1.REDUCTION_TARGET = '1' ");
        stb.append("                                          AND PRINT_T.SLIP_NO           = REDUC_D1.SLIP_NO ");
        stb.append("                                          AND PRINT_T.PAID_LIMIT_MONTH  = REDUC_D1.PLAN_MONTH ");
        stb.append("     LEFT JOIN REDUCTION_PLAN_DAT REDUC_D2 ON PRINT_T.SCHOOLCD          = REDUC_D2.SCHOOLCD ");
        stb.append("                                          AND PRINT_T.SCHOOL_KIND       = REDUC_D2.SCHOOL_KIND ");
        stb.append("                                          AND PRINT_T.YEAR              = REDUC_D2.YEAR ");
        stb.append("                                          AND REDUC_D2.REDUCTION_TARGET = '2' ");
        stb.append("                                          AND PRINT_T.SLIP_NO           = REDUC_D2.SLIP_NO ");
        stb.append("                                          AND PRINT_T.PAID_LIMIT_MONTH  = REDUC_D2.PLAN_MONTH ");
        stb.append("     LEFT JOIN REDUCTION_BURDEN_CHARGE_PLAN_DAT BURDEN_1 ON PRINT_T.SCHOOLCD        = BURDEN_1.SCHOOLCD ");
        stb.append("                                                      AND PRINT_T.SCHOOL_KIND       = BURDEN_1.SCHOOL_KIND ");
        stb.append("                                                      AND PRINT_T.YEAR              = BURDEN_1.YEAR ");
        stb.append("                                                      AND BURDEN_1.REDUCTION_TARGET = '1' ");
        stb.append("                                                      AND PRINT_T.SLIP_NO           = BURDEN_1.SLIP_NO ");
        stb.append("                                                      AND PRINT_T.PAID_LIMIT_MONTH  = BURDEN_1.PLAN_MONTH ");
        stb.append("     LEFT JOIN REDUCTION_BURDEN_CHARGE_PLAN_DAT BURDEN_2 ON PRINT_T.SCHOOLCD        = BURDEN_2.SCHOOLCD ");
        stb.append("                                                      AND PRINT_T.SCHOOL_KIND       = BURDEN_2.SCHOOL_KIND ");
        stb.append("                                                      AND PRINT_T.YEAR              = BURDEN_2.YEAR ");
        stb.append("                                                      AND BURDEN_2.REDUCTION_TARGET = '2' ");
        stb.append("                                                      AND PRINT_T.SLIP_NO           = BURDEN_2.SLIP_NO ");
        stb.append("                                                      AND PRINT_T.PAID_LIMIT_MONTH  = BURDEN_2.PLAN_MONTH ");
        stb.append("     LEFT JOIN REDUCTION_SCHOOL_PLAN_DAT SCHOOL_1 ON PRINT_T.SCHOOLCD          = SCHOOL_1.SCHOOLCD ");
        stb.append("                                                 AND PRINT_T.SCHOOL_KIND       = SCHOOL_1.SCHOOL_KIND ");
        stb.append("                                                 AND PRINT_T.YEAR              = SCHOOL_1.YEAR ");
        stb.append("                                                 AND SCHOOL_1.REDUCTION_TARGET = '1' ");
        stb.append("                                                 AND PRINT_T.SLIP_NO           = SCHOOL_1.SLIP_NO ");
        stb.append("                                                 AND PRINT_T.PAID_LIMIT_MONTH  = SCHOOL_1.PLAN_MONTH ");
        stb.append("     LEFT JOIN REDUCTION_SCHOOL_PLAN_DAT SCHOOL_2 ON PRINT_T.SCHOOLCD          = SCHOOL_2.SCHOOLCD ");
        stb.append("                                                 AND PRINT_T.SCHOOL_KIND       = SCHOOL_2.SCHOOL_KIND ");
        stb.append("                                                 AND PRINT_T.YEAR              = SCHOOL_2.YEAR ");
        stb.append("                                                 AND SCHOOL_2.REDUCTION_TARGET = '2' ");
        stb.append("                                                 AND PRINT_T.SLIP_NO           = SCHOOL_2.SLIP_NO ");
        stb.append("                                                 AND PRINT_T.PAID_LIMIT_MONTH  = SCHOOL_2.PLAN_MONTH ");
        stb.append("     LEFT JOIN SCHREG_BASE_MST SCH_B ON MAIN.SCHREGNO = SCH_B.SCHREGNO ");
        stb.append(" WHERE ");
        stb.append("    MAIN.SCHREGNO  IN " + SQLUtils.whereIn(true, StringUtils.split(_param._printSchreg, ":")) + " ");

        stb.append(" ORDER BY ");
        if ("1".equals(_param._sortDiv)) {
            stb.append("   PRINT_T.COLLECT_GRP_SORT, ");
            stb.append("   SCH_R.GRADE || SCH_R.HR_CLASS || SCH_R.ATTENDNO, ");
        } else if ("2".equals(_param._sortDiv)) {
            stb.append("   SCH_R.GRADE || SCH_R.HR_CLASS || SCH_R.ATTENDNO, ");
        } else {
            stb.append("   MAIN.SCHREGNO, ");
        }
        stb.append("   PRINT_T.SLIP_NO, ");
        stb.append("   PRINT_T.PAID_LIMIT_DATE ");

        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 77422 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _searchDiv;
        private final String _hrClassHyoujiFlg;
        private final String _grade;
        private final String _hrClass;
        private final String _entYear;
        private final String _grdYear;
        private final String _schregno;
        private final String _name;
        private final String _nameKana;
        private final String _a028;
        private final String _paidUmu;
        private final String _paidLimitMonth;
        private final String _paidDate;
        private final String _sortDiv;
        private final String _schFlg;
        private final String _selectSchoolKind;
        private final String[] _selectSchoolKindArray;
        private final String _printSchreg;
        private final String _schoolKind;
        private final String _schoolCd;
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _searchDiv = request.getParameter("P_SEARCH_DIV");
            _hrClassHyoujiFlg = request.getParameter("P_HR_CLASS_HYOUJI_FLG");
            _grade = request.getParameter("P_GRADE");
            _hrClass = request.getParameter("P_HR_CLASS");
            _entYear = request.getParameter("P_ENT_YEAR");
            _grdYear = request.getParameter("P_GRD_YEAR");
            _schregno = request.getParameter("P_SCHREGNO");
            _name = request.getParameter("P_NAME");
            _nameKana = request.getParameter("P_NAME_KANA");
            _a028 = request.getParameter("P_A028");
            _paidUmu = request.getParameter("P_PAID_UMU");
            _schFlg = request.getParameter("schFlg");
            _selectSchoolKind = request.getParameter("selectSchoolKind");
            _selectSchoolKindArray = StringUtils.split(_selectSchoolKind, ":");

            _printSchreg = request.getParameter("printSchreg");
            _schoolKind = request.getParameter("schoolKind");

            _paidLimitMonth = request.getParameter("PAID_LIMIT_MONTH");
            _paidDate = request.getParameter("PAID_DATE");
            _sortDiv = request.getParameter("SORT_DIV");

            _schoolCd = request.getParameter("SCHOOLCD");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
        }

    }
}

// eof
