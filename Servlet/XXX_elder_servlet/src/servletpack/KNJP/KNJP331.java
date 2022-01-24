// kanji=漢字
/*
 * $Id: cad853af2e9701c325d60205fb07aca4be071e91 $
 *
 * 作成日: 2011/01/05 17:27:12 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2011 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJP;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: cad853af2e9701c325d60205fb07aca4be071e91 $
 */
public class KNJP331 {

    private static final Log log = LogFactory.getLog("KNJP331.class");
    private static final String FORM_NAME1 = "KNJP331.frm";
    private static final String FORM_NAME2 = "KNJP331_2.frm";
    private static final String OUTPUT_CLASS = "1";
    private static final String OUTPUT_SCHREG = "2";

    private boolean _hasData;

    Param _param;

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
            init(response, svf);

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);

            _hasData = false;

            printMain(db2, svf);

            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            closeDb(db2);
            svf.VrQuit();
        }

    }

    private void init(
            final HttpServletResponse response,
            final Vrw32alp svf
    ) throws IOException {
        response.setContentType("application/pdf");

        svf.VrInit();
        svf.VrSetSpoolFileStream(response.getOutputStream());
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        final List printList = getPrintData(db2);
        for (final Iterator iter = printList.iterator(); iter.hasNext();) {
            final PrintData printData = (PrintData) iter.next();
            if ("0".equals(printData._overMoney)) {
                svf.VrSetForm(FORM_NAME2, 1);
            } else {
                svf.VrSetForm(FORM_NAME1, 1);
            }
            svf.VrsOut("DATE", _param._printDate);
            svf.VrsOut("GUARDIANNAME", printData._guarantorName);
            svf.VrsOut("HR_NAME", printData._hrName);
            svf.VrsOut("ATTENDNO", printData._attendno);
            svf.VrsOut("NAME", printData._name);
            svf.VrsOut("NENDO", _param._nendo);
            svf.VrsOut("MONEY", printData._total);
            svf.VrsOut("BANKMONTH", "3月");
            svf.VrsOut("BASIC", printData._bMin + "-" + printData._bMax);
            svf.VrsOut("BASICMONEY", printData._paidMoney);
            svf.VrsOut("ADD", printData._aMin + "-" + printData._aMax);
            svf.VrsOut("ADDMONEY", printData._addPaidMoney);
            svf.VrsOut("MONEY2", printData._overMoney);
            svf.VrsOut("ITEM", "振込額");
            svf.VrsOut("TOTAL", printData._total);

            svf.VrEndPage();
            _hasData = true;
        }
    }

    private List getPrintData(final DB2UDB db2) throws SQLException {
        final List retList = new ArrayList();
        final String sql = getPrintDataSql();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String guarantorName = rs.getString("GUARANTOR_NAME");
                final String hrName = rs.getString("HR_NAME");
                final String attendno = rs.getString("ATTENDNO");
                final String grdClass = rs.getString("GRD_CLASS");
                final String name = rs.getString("NAME");
                final String paidMoney = rs.getString("PAID_MONEY");
                final String addPaidMoney = rs.getString("ADD_PAID_MONEY");
                final String overMoney = rs.getString("OVER_MONEY");
                final String total = rs.getString("TOTAL");
                final String bMin = rs.getString("B_MIN");
                final String bMax = rs.getString("B_MAX");
                final String aMin = rs.getString("A_MIN");
                final String aMax = rs.getString("A_MAX");
                final PrintData printData = new PrintData(
                                                    guarantorName,
                                                    hrName,
                                                    attendno,
                                                    grdClass,
                                                    name,
                                                    paidMoney,
                                                    addPaidMoney,
                                                    overMoney,
                                                    total,
                                                    bMin,
                                                    bMax,
                                                    aMin,
                                                    aMax);
                retList.add(printData);
            }
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String getPrintDataSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH BASE_MONEY AS ( ");
        stb.append(" SELECT ");
        stb.append("     SCHREGNO, ");
        stb.append("     SUM(PAID_MONEY) AS PAID_MONEY, ");
        stb.append("     MIN(PLAN_YEAR || PLAN_MONTH) AS MIN_MONTH, ");
        stb.append("     MAX(PLAN_YEAR || PLAN_MONTH) AS MAX_MONTH ");
        stb.append(" FROM ");
        stb.append("     REDUCTION_COUNTRY_PLAN_DAT ");
        stb.append(" WHERE ");
        stb.append("     YEAR = '" + _param._year + "' ");
        stb.append("     AND PAID_YEARMONTH = '" + Integer.parseInt(_param._year) + "12' ");
        stb.append("     AND VALUE(PLAN_LOCK_FLG, '0') = '0' ");
        stb.append("     AND VALUE(PLAN_CANCEL_FLG, '0') = '0' ");
        stb.append(" GROUP BY ");
        stb.append("     SCHREGNO ");
        stb.append(" ), ADD_MONEY AS ( ");
        stb.append(" SELECT ");
        stb.append("     SCHREGNO, ");
        stb.append("     SUM(ADD_PAID_MONEY) AS ADD_PAID_MONEY, ");
        stb.append("     MIN(PLAN_YEAR || PLAN_MONTH) AS MIN_MONTH, ");
        stb.append("     MAX(PLAN_YEAR || PLAN_MONTH) AS MAX_MONTH ");
        stb.append(" FROM ");
        stb.append("     REDUCTION_COUNTRY_PLAN_DAT ");
        stb.append(" WHERE ");
        stb.append("     YEAR = '" + _param._year + "' ");
        stb.append("     AND ADD_PAID_YEARMONTH = '" + Integer.parseInt(_param._year) + "12' ");
        stb.append("     AND VALUE(ADD_PLAN_LOCK_FLG, '0') = '0' ");
        stb.append("     AND VALUE(ADD_PLAN_CANCEL_FLG, '0') = '0' ");
        stb.append(" GROUP BY ");
        stb.append("     SCHREGNO ");
        stb.append(" ), MONEY_DUE_T AS ( ");
        stb.append(" SELECT ");
        stb.append("     SCHREGNO, ");
        stb.append("     SUM(MONEY_DUE) AS MONEY_DUE ");
        stb.append(" FROM ");
        stb.append("     MONEY_DUE_M_DAT ");
        stb.append(" WHERE ");
        stb.append("    YEAR = '" + _param._year + "' ");
        stb.append("    AND EXPENSE_M_CD IN ('11', '12', '13') ");
        stb.append(" GROUP BY ");
        stb.append("     SCHREGNO ");
        stb.append(" ), REDUC_T AS ( ");
        stb.append(" SELECT ");
        stb.append("     SCHREGNO, ");
        stb.append("     SUM(CASE WHEN REDUC_DEC_FLG_1 = '1' ");
        stb.append("              THEN VALUE(REDUCTIONMONEY_1, 0) ");
        stb.append("              ELSE 0 ");
        stb.append("         END ");
        stb.append("         + ");
        stb.append("         CASE WHEN REDUC_DEC_FLG_2 = '1' ");
        stb.append("              THEN VALUE(REDUCTIONMONEY_2, 0) ");
        stb.append("              ELSE 0 ");
        stb.append("         END ");
        stb.append("     ) AS REDUCTIONMONEY ");
        stb.append(" FROM ");
        stb.append("     REDUCTION_DAT ");
        stb.append(" WHERE ");
        stb.append("    YEAR = '" + _param._year + "' ");
        stb.append("    AND (REDUC_DEC_FLG_1 = '1' ");
        stb.append("         OR ");
        stb.append("         REDUC_DEC_FLG_2 = '1') ");
        stb.append(" GROUP BY ");
        stb.append("     SCHREGNO ");
        stb.append(" ), REDUC_COUNTRY_T AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     SUM(CASE WHEN VALUE(T1.PLAN_CANCEL_FLG, '0') = '0' ");
        stb.append("              THEN VALUE(T1.PLAN_MONEY, 0) ");
        stb.append("              ELSE 0 ");
        stb.append("         END ");
        stb.append("         + ");
        stb.append("         CASE WHEN VALUE(T1.ADD_PLAN_CANCEL_FLG, '0') = '0' ");
        stb.append("              THEN VALUE(T1.ADD_PLAN_MONEY, 0) ");
        stb.append("              ELSE 0 ");
        stb.append("         END ");
        stb.append("     ) AS REDUCTION_C_MONEY ");
        stb.append(" FROM ");
        stb.append("     REDUCTION_COUNTRY_PLAN_DAT T1 ");
        stb.append(" WHERE ");
        stb.append("    T1.YEAR = '" + _param._year + "' ");
        stb.append("    AND (VALUE(T1.PLAN_CANCEL_FLG, '0') = '0' ");
        stb.append("         OR ");
        stb.append("         VALUE(T1.ADD_PLAN_CANCEL_FLG, '0') = '0') ");
        stb.append(" GROUP BY ");
        stb.append("     T1.SCHREGNO ");
        stb.append(" ), SCH_T AS ( ");
        stb.append(" SELECT DISTINCT ");
        stb.append("     SCHREGNO ");
        stb.append(" FROM ");
        stb.append("     BASE_MONEY ");
        stb.append(" UNION ");
        stb.append(" SELECT DISTINCT ");
        stb.append("     SCHREGNO ");
        stb.append(" FROM ");
        stb.append("     ADD_MONEY ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     t5.GUARANTOR_NAME, ");
        stb.append("     t3.HR_NAME, ");
        stb.append("     t2.ATTENDNO, ");
        stb.append("     t2.GRADE || t2.HR_CLASS || t2.ATTENDNO AS GRD_CLASS, ");
        stb.append("     t4.NAME, ");
        stb.append("     VALUE(BASE_M.PAID_MONEY, 0) AS PAID_MONEY, ");
        stb.append("     VALUE(ADD_M.ADD_PAID_MONEY, 0) AS ADD_PAID_MONEY, ");
        stb.append("     VALUE(OVER_M.TOTAL_BURDEN_CHARGE, 0) AS OVER_MONEY, ");
        stb.append("     VALUE(BASE_M.PAID_MONEY, 0) + VALUE(ADD_M.ADD_PAID_MONEY, 0) + VALUE(OVER_M.TOTAL_BURDEN_CHARGE, 0) AS TOTAL, ");
        stb.append("     VALUE(SUBSTR(BASE_M.MIN_MONTH, 5), '') AS B_MIN, ");
        stb.append("     VALUE(SUBSTR(BASE_M.MAX_MONTH, 5), '') AS B_MAX, ");
        stb.append("     VALUE(SUBSTR(ADD_M.MIN_MONTH, 5), '') AS A_MIN, ");
        stb.append("     VALUE(SUBSTR(ADD_M.MAX_MONTH, 5), '') AS A_MAX ");
        stb.append(" FROM ");
        stb.append("     SCH_T ");
        stb.append("     LEFT JOIN BASE_MONEY BASE_M ON SCH_T.SCHREGNO = BASE_M.SCHREGNO ");
        stb.append("     LEFT JOIN ADD_MONEY ADD_M ON SCH_T.SCHREGNO = ADD_M.SCHREGNO ");
        stb.append("     LEFT JOIN REDUCTION_BURDEN_CHARGE_DAT OVER_M ON OVER_M.YEAR = '" + _param._year + "' ");
        stb.append("          AND SCH_T.SCHREGNO = OVER_M.SCHREGNO ");
        stb.append("     INNER JOIN SCHREG_REGD_DAT t2 ON t2.SCHREGNO = SCH_T.SCHREGNO ");
        stb.append("           AND t2.YEAR = '" + _param._year + "' ");
        stb.append("           AND t2.SEMESTER = '" + _param._semester + "' ");
        if (OUTPUT_CLASS.equals(_param._output)) {
            stb.append("           AND t2.GRADE || t2.HR_CLASS IN " + _param._inState + " ");
        }
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT t3 ON t3.GRADE || t3.HR_CLASS = t2.GRADE || t2.HR_CLASS ");
        stb.append("          AND t3.YEAR = t2.YEAR ");
        stb.append("          AND t3.SEMESTER = t2.SEMESTER ");
        stb.append("     LEFT JOIN SCHREG_BASE_MST t4 ON t4.SCHREGNO = SCH_T.SCHREGNO ");
        stb.append("     INNER JOIN GUARDIAN_DAT t5 ON t5.SCHREGNO = SCH_T.SCHREGNO ");
        if (OUTPUT_SCHREG.equals(_param._output)) {
            stb.append(" WHERE ");
            stb.append("     SCH_T.SCHREGNO IN "+ _param._inState +" ");
        }
        stb.append(" ORDER BY ");
        stb.append("     GRD_CLASS ");

        return stb.toString();
    }

    private void closeDb(final DB2UDB db2) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
    }

    private class PrintData {
        private final String _guarantorName;
        private final String _hrName;
        private final String _attendno;
        private final String _grdClass;
        private final String _name;
        private final String _paidMoney;
        private final String _addPaidMoney;
        private final String _overMoney;
        private final String _total;
        private final String _bMin;
        private final String _bMax;
        private final String _aMin;
        private final String _aMax;
        /**
         * コンストラクタ。
         */
        public PrintData(
                final String guarantorName,
                final String hrName,
                final String attendno,
                final String grdClass,
                final String name,
                final String paidMoney,
                final String addPaidMoney,
                final String overMoney,
                final String total,
                final String bMin,
                final String bMax,
                final String aMin,
                final String aMax
        ) {
            _guarantorName =  guarantorName;
            _hrName        =  hrName;
            _attendno      =  attendno;
            _grdClass      =  grdClass;
            _name          =  name;
            _paidMoney     =  paidMoney;
            _addPaidMoney  =  addPaidMoney;
            _overMoney     =  overMoney;
            _total         =  total;
            _bMin          =  bMin;
            _bMax          =  bMax;
            _aMin          =  aMin;
            _aMax          =  aMax;
        }
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
        private final String _year;
        private final String _nendo;
        private final String _semester;
        private final String _date;
        private final String _printDate;
        private final String _output;
        private String _inState;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _nendo = KenjaProperties.gengou(Integer.parseInt(_year)) + "年度";
            _semester = request.getParameter("SEMESTER");
            _date = request.getParameter("DATE1");
            _printDate = KNJ_EditDate.h_format_JP(_date);
            _output = request.getParameter("OUTPUT");
            final String[] selected = request.getParameterValues("SELECT_SELECTED");
            String sep = "";
            _inState = "(";
            for (int i = 0; i < selected.length; i++) {
                _inState += sep + "'" + selected[i] + "'";
                sep = ",";
            }
            _inState += ")";
        }

    }
}

// eof
