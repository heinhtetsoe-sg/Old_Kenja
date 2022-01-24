/*
 * $Id: de06d9e3d3c44245877c116316efba8225a0701e $
 *
 * 作成日: 2015/03/31
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJMP;


import java.io.UnsupportedEncodingException;
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
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;

public class KNJMP970 {

    private static final Log log = LogFactory.getLog(KNJMP970.class);

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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws UnsupportedEncodingException {
        svf.VrSetForm("KNJMP970.frm", 1); // TODO: フォーム名
        final List list = getList(db2);
        int row = Integer.parseInt(_param._poRow);    //行
        final int iaMax = 6;
        int col = Integer.parseInt(_param._poCol);    //列
        for (int line = 0; line < list.size(); line++) {
            final PrintData printData = (PrintData) list.get(line);
            if (col > 3) {
                col = 1;
                row++;
                if (row > iaMax) {
                    if (_hasData) {
                        svf.VrEndPage();
                    }
                    row = 1;
                }
            }
            if (!StringUtils.isBlank(printData._zipCd)) {
                svf.VrsOutn("ZIPCODE"    + col, row,"〒" + printData._zipCd); //郵便番号
            }
            final String addr1 = null == printData._addr1 ? "" : printData._addr1;
            final String addr2 = null == printData._addr2 ? "" : printData._addr2;
            int check_len  = addr1.getBytes("MS932").length;
            int check_len2 = addr2.getBytes("MS932").length;
            final String guardName = null == printData._guardName ? "" : printData._guardName;
            if ("1".equals(_param._useAddrField2) && (check_len > 50 || check_len2 > 50)) {
                svf.VrsOutn("ADDRESS"  + col + "_1_3", row,printData._addr1);
                svf.VrsOutn("ADDRESS"  + col + "_2_3", row,printData._addr2);
            } else if (check_len > 40 || check_len2 > 40) {
                svf.VrsOutn("ADDRESS"  + col + "_1_2", row,addr1);
                svf.VrsOutn("ADDRESS"  + col + "_2_2", row,addr2);
                log.debug("40以上" + guardName);
            } else if (check_len > 0 || check_len2 > 0) {
                svf.VrsOutn("ADDRESS"  + col + "_1_1", row,addr1);
                svf.VrsOutn("ADDRESS"  + col + "_2_1", row,addr2);
                log.debug("40以下" + guardName);
            }
            if (!StringUtils.isBlank(guardName)) {
                svf.VrsOutn("NAME" + col + "_1",row ,guardName + "　様");  //名称
            }
            if ("1".equals(_param._schName)) {
                final String hrname = null == printData._hrName ? "" : printData._hrName;
                final String schName = null == printData._schName ? "" : printData._schName;
                if (!StringUtils.isBlank(hrname + schName)) {
                    svf.VrsOutn("NAME" + col + "_2",row ,"(" + hrname + " " + schName + ")");
                }
            }
            col++;
            _hasData = true;
        }
        if (_hasData) {
            svf.VrEndPage();
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
                final String zipCd      = rs.getString("ZIPCD");
                final String addr1      = rs.getString("ADDR1");
                final String addr2      = rs.getString("ADDR2");
                final String guardName  = rs.getString("GUARD_NAME");
                final String grade      = rs.getString("GRADE");
                final String hrClass    = rs.getString("HR_CLASS");
                final String hrName     = rs.getString("HR_NAME");
                final String attendNo   = rs.getString("ATTENDNO");
                final String schName    = rs.getString("SCH_NAME");

                final PrintData printData = new PrintData(zipCd, addr1, addr2, guardName, grade, hrClass, hrName, attendNo, schName);
                retList.add(printData);
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
        stb.append("     T1.SCHREGNO ");
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
        stb.append("     VALUE(T1.GUARD_ZIPCD, '') AS ZIPCD, ");
        stb.append("     T1.GUARD_ADDR1 AS ADDR1, ");
        stb.append("     T1.GUARD_ADDR2 AS ADDR2, ");
        stb.append("     T1.GUARD_NAME, ");
        stb.append("     REGD.GRADE, ");
        stb.append("     REGD.HR_CLASS, ");
        stb.append("     REGDH.HR_NAME, ");
        stb.append("     cast(REGD.ATTENDNO as int) AS ATTENDNO, ");
        stb.append("     BASE.NAME AS SCH_NAME ");
        stb.append(" FROM ");
        stb.append("     GUARDIAN_DAT T1 ");
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
        stb.append(" WHERE ");
        stb.append("     T1.SCHREGNO IN (SELECT I1.SCHREGNO FROM MINOU_T I1) ");
        stb.append(" ORDER BY ");
        if ("1".equals(_param._sort)) {
            stb.append("     T1.SCHREGNO ");
        } else {
            stb.append("     REGD.GRADE, ");
            stb.append("     REGD.HR_CLASS, ");
            stb.append("     REGD.ATTENDNO ");
        }
        return stb.toString();
    }

    private class PrintData {
        private final String _zipCd;
        private final String _addr1;
        private final String _addr2;
        private final String _guardName;
        private final String _grade;
        private final String _hrClass;
        private final String _hrName;
        private final String _attendNo;
        private final String _schName;
        public PrintData(
                final String zipCd,
                final String addr1,
                final String addr2,
                final String guardName,
                final String grade,
                final String hrClass,
                final String hrName,
                final String attendNo,
                final String schName
        ) {
            _zipCd      = zipCd;
            _addr1      = addr1;
            _addr2      = addr2;
            _guardName  = guardName;
            _grade      = grade;
            _hrClass    = hrClass;
            _hrName     = hrName;
            _attendNo   = attendNo;
            _schName    = schName;
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
            if (null != _classSelected) {
                final StringBuffer sbx = new StringBuffer();
                sbx.append("(");
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
                sbx.append(")");
                _gradeHrInState = sbx.toString();
            } else {
                _gradeHrInState = null;
            }

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

