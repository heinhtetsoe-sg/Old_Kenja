/*
 * $Id: 4be64b8071dfb2e5971b0a489bc9977140f711fe $
 *
 * 作成日: 2017/04/11
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJMP;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

public class KNJMP959 {

    private static final Log log = LogFactory.getLog(KNJMP959.class);

    private boolean _hasData;
    private int TOTALLINE = 46;

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
        final List list = getList(db2);
        for (Iterator iterator = list.iterator(); iterator.hasNext();) {
            final List pageList = (List) iterator.next();
            svf.VrSetForm("KNJMP959.frm", 4);
            final String setDate = KNJ_EditDate.getNowDateWa(true);
            for (int monthCnt = 4; monthCnt < 18; monthCnt++) {
                int lineCnt = 1;
                int totalDue = 0;
                int totalMoney = 0;
                int totalPaid = 0;
                int totalMinou = 0;
                final int setMonth = monthCnt < 13 ? monthCnt : monthCnt - 12;
                for (Iterator itPage = pageList.iterator(); itPage.hasNext();) {
                    final Student student = (Student) itPage.next();
                    svf.VrsOut("TITLE", KNJ_EditDate.h_format_JP_N(_param._ctrlYear + "-04-01") + "年度　生徒別徴収金台帳");
                    svf.VrsOut("DATE", setDate);
                    svf.VrsOut("HR_NAME", student._hrName);
                    svf.VrsOut("TEACHER_NAME", student._staffName);
                    if (monthCnt == 16) {
                        svf.VrsOut("ITEM1", "納入額計");
                        svf.VrsOutn("MONEY1", lineCnt, String.valueOf(student._totalPay));
                        totalPaid += student._totalPay;
                        svf.VrsOutn("MONEY1", TOTALLINE, String.valueOf(totalPaid));
                        lineCnt++;
                        continue;
                    }
                    if (monthCnt == 17) {
                        svf.VrsOut("ITEM1", "未納額計");
                        svf.VrsOutn("MONEY1", lineCnt, String.valueOf(student._totalMinou));
                        totalMinou += student._totalMinou;
                        svf.VrsOutn("MONEY1", TOTALLINE, String.valueOf(totalMinou));
                        lineCnt++;
                        continue;
                    }
                    svf.VrsOutn("NO", lineCnt, student._attendNo);
                    svf.VrsOutn("NAME1", lineCnt, student._name);
                    svf.VrsOut("ITEM2", String.valueOf(setMonth) + "月");
                    if (monthCnt == 4) {
                        svf.VrsOut("ITEM1", "請求額計");
                        svf.VrsOutn("MONEY1", lineCnt, String.valueOf(student._totalDue));
                        totalMoney += student._totalDue;
                        svf.VrsOutn("MONEY1", TOTALLINE, String.valueOf(totalMoney));
                    }
                    if (monthCnt == 8) {
                        svf.VrsOut("ITEM_TITLE", "納入明細");
                    }
                    if (monthCnt == 9) {
                        svf.VrsOut("ITEM_TITLE", "入");
                    }
                    if (monthCnt == 10) {
                        svf.VrsOut("ITEM_TITLE", "明");
                    }
                    if (monthCnt == 11) {
                        svf.VrsOut("ITEM_TITLE", "細");
                    }
                    final MonthMoney monthMoney = (MonthMoney) student._moneyDueMap.get(String.valueOf(setMonth));
                    if (null != monthMoney) {
                        svf.VrsOutn("MONEY2", lineCnt, String.valueOf(monthMoney._moneyDue));
                        totalDue += monthMoney._moneyDue;
                        svf.VrsOutn("MONEY2", TOTALLINE, String.valueOf(totalDue));
                    }
                    lineCnt++;
                    _hasData = true;
                }
                svf.VrEndRecord();
            }
        }
    }

	private List getList(final DB2UDB db2) {
		final List list = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = sql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            final int maxListCnt = 45;
            int listCnt = 1;
            List setList = new ArrayList();
            String befKey = "";
            while (rs.next()) {
                final String grade = rs.getString("GRADE");
                final String hrClass = rs.getString("HR_CLASS");
                final String hrName = rs.getString("HR_NAME");
                final String attendNo = rs.getString("ATTENDNO");
                final String schregno = rs.getString("SCHREGNO");
                final String name = rs.getString("NAME");
                final String staffName = rs.getString("STAFFNAME");
                final String key = grade + hrClass;
                if (befKey != "" && !befKey.equals(key)) {
                    list.add(setList);
                    setList = new ArrayList();
                    listCnt = 1;
                }
                if (listCnt > maxListCnt) {
                    list.add(setList);
                    setList = new ArrayList();
                    listCnt = 1;
                }
                final Student student = new Student(schregno, grade, hrClass, hrName, attendNo, name, staffName);
                student.setMoneyDue(db2);
                setList.add(student);
                listCnt++;
                befKey = grade + hrClass;
            }
            if (befKey != "") {
                list.add(setList);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
		return list;
	}

    private String sql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     REGD.GRADE, ");
        stb.append("     REGD.HR_CLASS, ");
        stb.append("     HDAT.HR_NAME, ");
        stb.append("     REGD.ATTENDNO, ");
        stb.append("     REGD.SCHREGNO, ");
        stb.append("     BASE.NAME, ");
        stb.append("     STAFF.STAFFNAME ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT REGD ");
        stb.append("     INNER JOIN SCHREG_BASE_MST BASE ON REGD.SCHREGNO = BASE.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT HDAT ON REGD.YEAR = HDAT.YEAR ");
        stb.append("          AND REGD.SEMESTER = HDAT.SEMESTER ");
        stb.append("          AND REGD.GRADE = HDAT.GRADE ");
        stb.append("          AND REGD.HR_CLASS = HDAT.HR_CLASS ");
        stb.append("     LEFT JOIN STAFF_MST STAFF ON HDAT.TR_CD1 = STAFF.STAFFCD ");
        stb.append(" WHERE ");
        stb.append("     REGD.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("     AND REGD.SEMESTER = '" + _param._ctrlSemester + "' ");
        if ("1".equals(_param._output)) {
            stb.append("     AND REGD.GRADE IN " + _param._instate + " ");
        } else {
            stb.append("     AND REGD.GRADE || REGD.HR_CLASS IN " + _param._instate + " ");
        }
        stb.append(" ORDER BY ");
        stb.append("     REGD.GRADE, ");
        stb.append("     REGD.HR_CLASS, ");
        stb.append("     REGD.ATTENDNO, ");
        stb.append("     REGD.SCHREGNO ");
        return stb.toString();
    }

    private class Student {
        final String _schregno;
        final String _grade;
        final String _hrClass;
        final String _hrName;
        final String _attendNo;
        final String _name;
        final String _staffName;
        final Map _moneyDueMap;
        int _totalDue = 0;
        int _totalPay = 0;
        int _totalMinou = 0;
        public Student(
                final String schregno,
                final String grade,
                final String hrClass,
                final String hrName,
                final String attendNo,
                final String name,
                final String staffName
        ) {
            _schregno = schregno;
            _grade = grade;
            _hrClass = hrClass;
            _hrName = hrName;
            _attendNo = attendNo;
            _name = name;
            _staffName = staffName;
            _moneyDueMap = new HashMap();
        }

        public void setMoneyDue(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = sql();
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    final String payMonth = rs.getString("PAY_MONTH");
                    final int moneyDue = rs.getInt("MONEY_DUE");
                    final int paidMoney = rs.getInt("PAID_MONEY");
                    final int minou = rs.getInt("MINOU");
                    final MonthMoney monthMoney = new MonthMoney(moneyDue, paidMoney, minou);
                    _moneyDueMap.put(payMonth, monthMoney);
                    _totalDue += moneyDue;
                    _totalPay += paidMoney;
                    _totalMinou += minou;
                }

            } catch (SQLException ex) {
                log.debug("Exception:", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private String sql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     MONTH(COL_M.PAY_DATE) AS PAY_MONTH, ");
            stb.append("     SUM(VALUE(DUE_M.MONEY_DUE, 0)) AS MONEY_DUE, ");
            stb.append("     SUM(VALUE(PAID_M.PAID_MONEY, 0)) AS PAID_MONEY, ");
            stb.append("     SUM(VALUE(DUE_M.MONEY_DUE, 0)) - SUM(VALUE(PAID_M.PAID_MONEY, 0)) AS MINOU ");
            stb.append(" FROM ");
            stb.append("     COLLECT_MONEY_DUE_M_DAT DUE_M ");
            stb.append("     LEFT JOIN COLLECT_M_MST COL_M ON DUE_M.YEAR = COL_M.YEAR ");
            stb.append("          AND DUE_M.COLLECT_L_CD = COL_M.COLLECT_L_CD ");
            stb.append("          AND DUE_M.COLLECT_M_CD = COL_M.COLLECT_M_CD ");
            stb.append("     LEFT JOIN COLLECT_MONEY_PAID_M_DAT PAID_M ON DUE_M.YEAR = PAID_M.YEAR ");
            stb.append("          AND DUE_M.SCHREGNO = PAID_M.SCHREGNO ");
            stb.append("          AND DUE_M.COLLECT_GRP_CD = PAID_M.COLLECT_GRP_CD ");
            stb.append("          AND DUE_M.COLLECT_L_CD = PAID_M.COLLECT_L_CD ");
            stb.append("          AND DUE_M.COLLECT_M_CD = PAID_M.COLLECT_M_CD ");
            stb.append(" WHERE ");
            stb.append("     DUE_M.YEAR = '" + _param._ctrlYear + "' ");
            stb.append("     AND DUE_M.SCHREGNO = '" + _schregno + "' ");
            stb.append(" GROUP BY ");
            stb.append("     MONTH(COL_M.PAY_DATE) ");
            stb.append("  ");
            return stb.toString();
        }
    }

    private class MonthMoney {
        final int _moneyDue;
        final int _paid;
        final int _minou;
        public MonthMoney(
                final int moneyDue,
                final int paid,
                final int minou
        ) {
            _moneyDue = moneyDue;
            _paid = paid;
            _minou = minou;
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
        final String _output;
        final String _month;
        final String[] _selectSelected;
        final String _instate;
        final String _jhflg;
        final String _ctrlYear;
        final String _ctrlSemester;
        final String _ctrlDate;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _output = request.getParameter("OUTPUT");
            _month = request.getParameter("MONTH");
            _selectSelected = request.getParameterValues("SELECT_SELECTED");
            final StringBuffer sbx = new StringBuffer();
            sbx.append("(");
            for (int ia = 0; ia < _selectSelected.length; ia++) {
                if (_selectSelected[ia] == null) {
                    break;
                }
                if (ia > 0) {
                    sbx.append(",");
                }
                sbx.append("'");
                sbx.append(_selectSelected[ia]);
                sbx.append("'");
            }
            sbx.append(")");
            _instate = sbx.toString();

            _jhflg = request.getParameter("JHFLG");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
        }

    }
}

// eof

