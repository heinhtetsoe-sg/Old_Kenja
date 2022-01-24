/*
 * $Id: 33b7a088d8c269b5ee0c871e28e20a456a3fd33d $
 *
 * 作成日: 2015/04/01
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJP;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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

public class KNJP969 {

    private static final Log log = LogFactory.getLog(KNJP969.class);

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
        svf.VrSetForm("KNJP969.frm", 4); // TODO: フォーム名
        final List list = getList(db2);
        int totalMinou = 0;
        int totalMinouCnt = 0;
        String befGrade = "";
        String befGradeHr = "";
        String setNenKumi = "";
        for (int line = 0; line < list.size(); line++) {
            final PrintSchregData printSchregData = (PrintSchregData) list.get(line);
            if ("1".equals(_param._outPut)) {
                if (!"".equals(befGrade) && !befGrade.equals(printSchregData._grade)) {
                    svf.VrsOut("FOOTER", setNenKumi + ",人数" + totalMinouCnt + "名,未納額計");
                    svf.VrsOut("TOTAL_UNPAID", String.valueOf(totalMinou));
                    svf.VrEndRecord();
                    svf.VrSetForm("KNJP969.frm", 4); // TODO: フォーム名
                    totalMinou = 0;
                    totalMinouCnt = 0;
                } else if (!"".equals(befGradeHr)) {
                    svf.VrEndRecord();
                }
            } else {
                if (!"".equals(befGradeHr) && !befGradeHr.equals(printSchregData._grade + printSchregData._hrClass)) {
                    svf.VrsOut("FOOTER", setNenKumi + ",人数" + totalMinouCnt + "名,未納額計");
                    svf.VrsOut("TOTAL_UNPAID", String.valueOf(totalMinou));
                    svf.VrEndRecord();
                    svf.VrSetForm("KNJP969.frm", 4); // TODO: フォーム名
                    totalMinou = 0;
                    totalMinouCnt = 0;
                } else if (!"".equals(befGradeHr)) {
                    svf.VrEndRecord();
                }
            }
            svf.VrsOut("PRINT_DATE", KNJ_EditDate.h_format_JP(_param._ctrlDate));
            svf.VrsOut("TITLE", nao_package.KenjaProperties.gengou(Integer.parseInt(_param._ctrlYear)) + "年度　未納者一覧");
            setNenKumi = "1".equals(_param._outPut) ? Integer.parseInt(printSchregData._grade) + "学年" : printSchregData._hrNameAbbv;
            svf.VrsOut("HR_NAME1", setNenKumi);
            svf.VrsOut("SCHOOL_NAME", "指定納期限 " + KNJ_EditDate.h_format_JP(_param._limitDate));
            svf.VrsOut("ATTENDNO", printSchregData._hrNameAbbv + "-" + printSchregData._attendno);
            svf.VrsOut("NAME", printSchregData._schName);
            svf.VrsOut("LIMIT", printSchregData._payDate.replace('-', '/'));
            svf.VrsOut("COLLECT", printSchregData._moneyDue);
            svf.VrsOut("UNPAID", printSchregData._minouGk);
            totalMinou += Integer.parseInt(printSchregData._minouGk);
            totalMinouCnt++;
            if (line == list.size() - 1) {
                svf.VrsOut("FOOTER", setNenKumi + ",人数" + totalMinouCnt + "名,未納額計");
                svf.VrsOut("TOTAL_UNPAID", String.valueOf(totalMinou));
            }
            befGrade = printSchregData._grade;
            befGradeHr = printSchregData._grade + printSchregData._hrClass;
            _hasData = true;
        }
        if (list.size() > 0) {
            svf.VrEndRecord();
        }
    }

    private List getList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getPrintsql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String schregno   = rs.getString("SCHREGNO");
                final String grade      = rs.getString("GRADE");
                final String hrClass    = rs.getString("HR_CLASS");
                final String hrNameAbbv = rs.getString("HR_NAMEABBV");
                final String attendno   = rs.getString("ATTENDNO");
                final String schName    = rs.getString("SCH_NAME");
                final String payDate    = rs.getString("PAY_DATE");
                final String moneyDue   = rs.getString("MONEY_DUE");
                final String minouGk    = rs.getString("MINOU_GK");
                final PrintSchregData printSchregData = new PrintSchregData(schregno, grade, hrClass, hrNameAbbv, attendno, schName, payDate, moneyDue, minouGk);
                retList.add(printSchregData);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String getPrintsql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH MINOU_T AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.PAY_DATE, ");
        stb.append("     SUM(CASE WHEN VALUE(L1.PAID_MONEY, 0) > 0 ");
        stb.append("              THEN VALUE(L1.PAID_MONEY, 0) ");
        stb.append("              ELSE VALUE(L2.PAID_MONEY, 0) ");
        stb.append("         END ");
        stb.append("     ) AS PAID_MONEY, ");
        stb.append("     SUM(CASE WHEN VALUE(T1.MONEY_DUE, 0) > 0 ");
        stb.append("              THEN VALUE(T1.MONEY_DUE, 0) ");
        stb.append("              ELSE VALUE(L3.MONEY_DUE, 0) ");
        stb.append("         END ");
        stb.append("     ) AS MONEY_DUE ");
        stb.append(" FROM ");
        stb.append("     COLLECT_MONEY_DUE_M_DAT T1 ");
        stb.append("     LEFT JOIN COLLECT_MONEY_PAID_M_DAT L1 ON L1.YEAR = T1.YEAR ");
        stb.append("          AND L1.SCHREGNO = T1.SCHREGNO ");
        stb.append("          AND L1.COLLECT_L_CD = T1.COLLECT_L_CD ");
        stb.append("          AND L1.COLLECT_M_CD = T1.COLLECT_M_CD ");
        stb.append("     LEFT JOIN COLLECT_MONEY_DUE_S_DAT L3 ON L3.YEAR = T1.YEAR ");
        stb.append("          AND L3.SCHREGNO = T1.SCHREGNO ");
        stb.append("          AND L3.COLLECT_L_CD = T1.COLLECT_L_CD ");
        stb.append("          AND L3.COLLECT_M_CD = T1.COLLECT_M_CD ");
        stb.append("     LEFT JOIN COLLECT_MONEY_PAID_S_DAT L2 ON L2.YEAR = L3.YEAR ");
        stb.append("          AND L2.SCHREGNO = L3.SCHREGNO ");
        stb.append("          AND L2.COLLECT_L_CD = L3.COLLECT_L_CD ");
        stb.append("          AND L2.COLLECT_M_CD = L3.COLLECT_M_CD ");
        stb.append("          AND L2.COLLECT_S_CD = L3.COLLECT_S_CD ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("     AND T1.PAY_DATE < '" + _param._limitDate.replace('/', '-') + "' ");
        stb.append(" GROUP BY ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.PAY_DATE ");
        stb.append(" HAVING ");
        stb.append("     SUM(CASE WHEN VALUE(L1.PAID_MONEY, 0) > 0 ");
        stb.append("              THEN VALUE(L1.PAID_MONEY, 0) ");
        stb.append("              ELSE VALUE(L2.PAID_MONEY, 0) ");
        stb.append("         END ");
        stb.append("     ) <  ");
        stb.append("     SUM(CASE WHEN VALUE(T1.MONEY_DUE, 0) > 0 ");
        stb.append("              THEN VALUE(T1.MONEY_DUE, 0) ");
        stb.append("              ELSE VALUE(L3.MONEY_DUE, 0) ");
        stb.append("         END ");
        stb.append("     ) ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     REGD.GRADE, ");
        stb.append("     REGD.HR_CLASS, ");
        stb.append("     REGDH.HR_NAMEABBV, ");
        stb.append("     REGD.ATTENDNO, ");
        stb.append("     BASE.NAME AS SCH_NAME, ");
        stb.append("     T1.PAY_DATE, ");
        stb.append("     T1.MONEY_DUE, ");
        stb.append("     VALUE(T1.MONEY_DUE, 0) - VALUE(T1.PAID_MONEY, 0) AS MINOU_GK ");
        stb.append(" FROM ");
        stb.append("     MINOU_T T1 ");
        stb.append("     INNER JOIN SCHREG_REGD_DAT REGD ON REGD.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("           AND REGD.SEMESTER = '" + _param._ctrlSemester + "' ");
        stb.append("           AND T1.SCHREGNO = REGD.SCHREGNO ");
        if ("1".equals(_param._outPut)) {
            stb.append("           AND REGD.GRADE = '" + _param._grade + "' ");
        } else {
            stb.append("           AND REGD.GRADE || '-' || REGD.HR_CLASS IN " + _param._gradeHrInState);
        }
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT REGDH ON REGD.YEAR = REGDH.YEAR ");
        stb.append("          AND REGD.SEMESTER = REGDH.SEMESTER ");
        stb.append("          AND REGD.GRADE = REGDH.GRADE ");
        stb.append("          AND REGD.HR_CLASS = REGDH.HR_CLASS ");
        stb.append("     LEFT JOIN SCHREG_BASE_MST BASE ON T1.SCHREGNO = BASE.SCHREGNO ");
        stb.append(" ORDER BY ");
        stb.append("     REGD.GRADE, ");
        stb.append("     REGD.HR_CLASS, ");
        stb.append("     REGD.ATTENDNO, ");
        stb.append("     T1.PAY_DATE ");

        return stb.toString();
    }

    private class PrintSchregData {
        final String _schregno;
        final String _grade;
        final String _hrClass;
        final String _hrNameAbbv;
        final String _attendno;
        final String _schName;
        final String _payDate;
        final String _moneyDue;
        final String _minouGk;
        public PrintSchregData(
                final String schregno,
                final String grade,
                final String hrClass,
                final String hrNameAbbv,
                final String attendno,
                final String schName,
                final String payDate,
                final String moneyDue,
                final String minouGk
        ) {
            _schregno   = schregno;
            _grade      = grade;
            _hrClass    = hrClass;
            _hrNameAbbv = hrNameAbbv;
            _attendno   = attendno;
            _schName    = schName;
            _payDate    = payDate;
            _moneyDue   = moneyDue;
            _minouGk    = minouGk;
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
        private final String _semester;
        private final String _outPut;
        private final String _grade;
        private final String[] _classSelected;
        private final String _gradeHrInState;
        private final String _sort;
        private final String _schName;
        private final String _poRow;
        private final String _poCol;
        private final String _limitDate;
        private final String _prgid;
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final String _useAddrField2;
        private final String _schoolName;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _outPut = request.getParameter("OUTPUT");
            _grade = request.getParameter("GRADE");
            //リストToリスト
            _classSelected = request.getParameterValues("CATEGORY_SELECTED");
            final StringBuffer sbx = new StringBuffer();
            sbx.append("(");
            if ("2".equals(_outPut)) {
                for (int ia = 0; ia < _classSelected.length; ia++) {
                    if (_classSelected[ia] == null) {
                        break;
                    }
                    if (ia > 0) {
                        sbx.append(",");
                    }
                    sbx.append("'");
                    sbx.append(_classSelected[ia]);
                    sbx.append("'");
                }
            }
            sbx.append(")");
            _gradeHrInState = sbx.toString();

            _sort = request.getParameter("SORT");
            _schName = request.getParameter("SCHNAME");
            _poRow = request.getParameter("POROW");
            _poCol = request.getParameter("POCOL");
            _limitDate = request.getParameter("LIMIT_DATE");
            _prgid = request.getParameter("PRGID");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _useAddrField2 = request.getParameter("useAddrField2");
            _schoolName = getSchoolName(db2, _year);
        }

        private String getSchoolName(final DB2UDB db2, final String year) {
            String retSchoolName = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement("SELECT SCHOOLNAME1 FROM SCHOOL_MST WHERE YEAR = '" + year + "' ");
                rs = ps.executeQuery();
                if (rs.next()) {
                    retSchoolName = rs.getString("SCHOOLNAME1");
                }
            } catch (SQLException ex) {
                log.debug("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retSchoolName;
        }
    }
}

// eof

