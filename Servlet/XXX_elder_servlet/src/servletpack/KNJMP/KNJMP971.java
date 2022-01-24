/*
 * $Id: d5010c0bcd118f04d525a237c1502a72566e6276 $
 *
 * 作成日: 2015/04/01
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJMP;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

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

public class KNJMP971 {

    private static final Log log = LogFactory.getLog(KNJMP971.class);

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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws ParseException {
        svf.VrSetForm("KNJMP971.frm", 1);
        final List list = getList(db2);
        for (int line = 0; line < list.size(); line++) {
            final PrintSchregData printSchregData = (PrintSchregData) list.get(line);
            svf.VrsOut("GUARD_NAME", printSchregData._guardName + "　様");
            svf.VrsOut("NAME", "(" + printSchregData._hrName + printSchregData._attendno + "番　" + printSchregData._schName + "　様)");
            svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._printDate));

            int certifLine = 1;
            for (Iterator itCertif = _param._certifSchool.iterator(); itCertif.hasNext();) {
                CertifSchool certifSchool = (CertifSchool) itCertif.next();
                if ("129".equals(certifSchool._kindCd)) {
                    svf.VrsOut("TANTOU", "担当");
                    svf.VrsOut("SPARE2", certifSchool._remark1 + "　" + certifSchool._remark2);
                    svf.VrsOut("SPARE4", certifSchool._remark3 + "　" + certifSchool._remark4);
                } else {
                    final String setCertif = certifSchool._schoolName + "　" + certifSchool._jobName + "　" + certifSchool._principalName;
                    svf.VrsOut("JOBNAME" + certifLine, setCertif);
                    certifLine++;
                }
            }
            svf.VrsOut("KOUIN", "(公印省略)");
            svf.VrsOut("TITLE", nao_package.KenjaProperties.gengou(Integer.parseInt(_param._ctrlYear)) + "年度　" + _param._documentMst._title);
            String[] arr = KNJ_EditEdit.get_token(_param._documentMst._text, 80, 6);
            if (arr != null) {
                for (int i = 0; i < arr.length; i++) {
                    svf.VrsOut("REMARK" + String.valueOf(i + 1), arr[i]);
                }
            }

            int minouLine = 1;
            int minouTotal = 0;
            for (Iterator itMinou = printSchregData._minouList.iterator(); itMinou.hasNext();) {
                MinouData minouData = (MinouData) itMinou.next();
                svf.VrsOut("ITEMNAME" + minouLine, minouData._mName);
                svf.VrsOut("CHARGE" + minouLine, minouData._minouGk);
                minouTotal += Integer.parseInt(minouData._minouGk);
                minouLine++;
            }

            svf.VrsOut("TOTAL_CHARGE", String.valueOf(minouTotal));

            SimpleDateFormat formatYoubi = new SimpleDateFormat("(E)", Locale.JAPAN);
            Date youbi = DateFormat.getDateInstance().parse(_param._tokusokuDate);
            svf.VrsOut("LIMIT_DATE", KNJ_EditDate.h_format_JP(_param._tokusokuDate) + formatYoubi.format(youbi));

            svf.VrEndPage();
            _hasData = true;
        }
    }

    private List getList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getSchregSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String schregno   = rs.getString("SCHREGNO");
                final String grade      = rs.getString("GRADE");
                final String hrClass    = rs.getString("HR_CLASS");
                final String hrName     = rs.getString("HR_NAME");
                final String attendno   = rs.getString("ATTENDNO");
                final String schName    = rs.getString("SCH_NAME");
                final String guardName  = rs.getString("GUARD_NAME");
                final PrintSchregData printSchregData = new PrintSchregData(schregno, grade, hrClass, hrName, attendno, schName, guardName);
                printSchregData.setMinouList(db2);
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

    private String getSchregSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     REGD.SCHREGNO, ");
        stb.append("     REGD.GRADE, ");
        stb.append("     REGD.HR_CLASS, ");
        stb.append("     REGDH.HR_NAME, ");
        stb.append("     REGD.ATTENDNO, ");
        stb.append("     BASE.NAME AS SCH_NAME, ");
        stb.append("     GUARD.GUARD_NAME AS GUARD_NAME ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT REGD ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT REGDH ON REGD.YEAR = REGDH.YEAR ");
        stb.append("          AND REGD.SEMESTER = REGDH.SEMESTER ");
        stb.append("          AND REGD.GRADE = REGDH.GRADE ");
        stb.append("          AND REGD.HR_CLASS = REGDH.HR_CLASS ");
        stb.append("     LEFT JOIN SCHREG_BASE_MST BASE ON REGD.SCHREGNO = BASE.SCHREGNO ");
        stb.append("     LEFT JOIN GUARDIAN_DAT GUARD ON REGD.SCHREGNO = GUARD.SCHREGNO ");
        stb.append(" WHERE ");
        stb.append("     REGD.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("           AND REGD.SEMESTER = '" + _param._semester + "' ");
        stb.append("           AND REGD.SCHREGNO IN " + _param._schregInState);
        stb.append(" ORDER BY ");
        stb.append("     REGD.GRADE, ");
        stb.append("     REGD.HR_CLASS, ");
        stb.append("     REGD.ATTENDNO ");

        return stb.toString();
    }

    private class PrintSchregData {
        private final String _schregno;
        private final String _grade;
        private final String _hrClass;
        private final String _hrName;
        private final String _attendno;
        private final String _schName;
        private final String _guardName;
        private final List _minouList;
        public PrintSchregData(
                final String schregno,
                final String grade,
                final String hrClass,
                final String hrName,
                final String attendno,
                final String schName,
                final String guardName
        ) {
            _schregno   = schregno;
            _grade      = grade;
            _hrClass    = hrClass;
            _hrName     = hrName;
            _attendno   = attendno;
            _schName    = schName;
            _guardName  = guardName;
            _minouList = new ArrayList();
        }

        public void setMinouList(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = getMinouSql();
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    final String schregno   = rs.getString("SCHREGNO");
                    final String collectLCd = rs.getString("COLLECT_L_CD");
                    final String collectMCd = rs.getString("COLLECT_M_CD");
                    final String mName      = rs.getString("COLLECT_M_NAME");
                    final String paidMoney  = rs.getString("PAID_MONEY");
                    final String moneyDue   = rs.getString("MONEY_DUE");
                    final String minouGk    = rs.getString("MINOU_GK");
                    final MinouData minouData = new MinouData(schregno, collectLCd, collectMCd, mName, paidMoney, moneyDue, minouGk);
                    _minouList.add(minouData);
                }

            } catch (SQLException ex) {
                log.debug("Exception:", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }

        }

        private String getMinouSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.COLLECT_L_CD, ");
            stb.append("     T1.COLLECT_M_CD, ");
            stb.append("     MMST.COLLECT_M_NAME, ");
            stb.append("     SUM(CASE WHEN VALUE(L1.PAID_MONEY, 0) > 0 ");
            stb.append("              THEN VALUE(L1.PAID_MONEY, 0) ");
            stb.append("              ELSE VALUE(L2.PAID_MONEY, 0) ");
            stb.append("         END ");
            stb.append("     ) AS PAID_MONEY, ");
            stb.append("     SUM(CASE WHEN VALUE(T1.MONEY_DUE, 0) > 0 ");
            stb.append("              THEN VALUE(T1.MONEY_DUE, 0) ");
            stb.append("              ELSE VALUE(L3.MONEY_DUE, 0) ");
            stb.append("         END ");
            stb.append("     ) AS MONEY_DUE, ");
            stb.append("     SUM(CASE WHEN VALUE(T1.MONEY_DUE, 0) > 0 ");
            stb.append("              THEN VALUE(T1.MONEY_DUE, 0) ");
            stb.append("              ELSE VALUE(L3.MONEY_DUE, 0) ");
            stb.append("         END ");
            stb.append("     ) -  ");
            stb.append("     SUM(CASE WHEN VALUE(L1.PAID_MONEY, 0) > 0 ");
            stb.append("              THEN VALUE(L1.PAID_MONEY, 0) ");
            stb.append("              ELSE VALUE(L2.PAID_MONEY, 0) ");
            stb.append("         END ");
            stb.append("     ) AS MINOU_GK ");
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
            stb.append("     LEFT JOIN COLLECT_M_MST MMST ON T1.YEAR = MMST.YEAR ");
            stb.append("          AND T1.COLLECT_L_CD = MMST.COLLECT_L_CD ");
            stb.append("          AND T1.COLLECT_M_CD = MMST.COLLECT_M_CD ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _param._ctrlYear + "' ");
            stb.append("     AND T1.SCHREGNO = '" + _schregno + "' ");
            stb.append("     AND T1.PAY_DATE < '" + _param._limitDate.replace('/', '-') + "' ");
            stb.append(" GROUP BY ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.COLLECT_L_CD, ");
            stb.append("     T1.COLLECT_M_CD, ");
            stb.append("     MMST.COLLECT_M_NAME ");
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
            stb.append(" ORDER BY ");
            stb.append("     T1.COLLECT_L_CD DESC, ");
            stb.append("     T1.COLLECT_M_CD DESC ");

            return stb.toString();
        }
    }

    private class MinouData {
        private final String _schregno;
        private final String _collectLCd;
        private final String _collectMCd;
        private final String _mName;
        private final String _paidMoney;
        private final String _moneyDue;
        private final String _minouGk;
        public MinouData(
                final String schregno,
                final String collectLCd,
                final String collectMCd,
                final String mName,
                final String paidMoney,
                final String moneyDue,
                final String minouGk
        ) {
            _schregno   = schregno;
            _collectLCd = collectLCd;
            _collectMCd = collectMCd;
            _mName      = mName;
            _paidMoney  = paidMoney;
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
        private final String _semester;
        private final String _grade;
        private final String[] _schregSelected;
        private final String _schregInState;
        private final String _sort;
        private final String _schName;
        private final String _poRow;
        private final String _poCol;
        private final String _limitDate;
        private final String _tokusokuDate;
        private final String _printDate;
        private final String _prgid;
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final String _useAddrField2;
        private final String _schoolName;
        private final List _certifSchool;
        private final DocumentMst _documentMst;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _semester = request.getParameter("SEMESTER");
            _grade = request.getParameter("GRADE");
            //リストToリスト
            _schregSelected = request.getParameterValues("CATEGORY_SELECTED");
            final StringBuffer sbx = new StringBuffer();
            sbx.append("(");
            for (int ia = 0; ia < _schregSelected.length; ia++) {
                if (_schregSelected[ia] == null) {
                    break;
                }
                if (ia > 0) {
                    sbx.append(",");
                }
                sbx.append("'");
                sbx.append(_schregSelected[ia]);
                sbx.append("'");
            }
            sbx.append(")");
            _schregInState = sbx.toString();

            _sort = request.getParameter("SORT");
            _schName = request.getParameter("SCHNAME");
            _poRow = request.getParameter("POROW");
            _poCol = request.getParameter("POCOL");
            _limitDate = request.getParameter("LIMIT_DATE");
            _tokusokuDate = request.getParameter("TOKUSOKU_DATE");
            _printDate = request.getParameter("PRINT_DATE");
            _prgid = request.getParameter("PRGID");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _useAddrField2 = request.getParameter("useAddrField2");
            _schoolName = getSchoolName(db2, _ctrlYear);
            _certifSchool = getCertifSchool(db2, _ctrlYear);
            _documentMst = getDocumentMst(db2, _ctrlYear);
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

        private List getCertifSchool(final DB2UDB db2, final String year) {
            final List retList = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement("SELECT * FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + year + "' AND CERTIF_KINDCD IN ('126', '127', '128', '129') ORDER BY CERTIF_KINDCD DESC");
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String kindCd = rs.getString("CERTIF_KINDCD");
                    final String schoolName = rs.getString("SCHOOL_NAME");
                    final String jobName = rs.getString("JOB_NAME");
                    final String principalName = rs.getString("PRINCIPAL_NAME");
                    final String remark1 = rs.getString("REMARK1");
                    final String remark2 = rs.getString("REMARK2");
                    final String remark3 = rs.getString("REMARK3");
                    final String remark4 = rs.getString("REMARK4");
                    final CertifSchool certifSchool = new CertifSchool(kindCd, schoolName, jobName, principalName, remark1, remark2, remark3, remark4);
                    retList.add(certifSchool);
                }
            } catch (SQLException ex) {
                log.debug("CERTIF_SCHOOL_DAT exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retList;
        }

        private DocumentMst getDocumentMst(final DB2UDB db2, final String year) {
            DocumentMst documentMst = new DocumentMst("", "");
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement("SELECT * FROM DOCUMENT_MST WHERE DOCUMENTCD = 'A1'");
                rs = ps.executeQuery();
                if (rs.next()) {
                    final String title = rs.getString("TITLE");
                    final String text = rs.getString("TEXT");
                    documentMst = new DocumentMst(title, text);
                }
            } catch (SQLException ex) {
                log.debug("DOCUMENT_MST exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return documentMst;
        }
    }
    private class CertifSchool {
        private final String _kindCd;
        private final String _schoolName;
        private final String _jobName;
        private final String _principalName;
        private final String _remark1;
        private final String _remark2;
        private final String _remark3;
        private final String _remark4;
        public CertifSchool(
                final String kindCd,
                final String schoolName,
                final String jobName,
                final String principalName,
                final String remark1,
                final String remark2,
                final String remark3,
                final String remark4
        ) {
            _kindCd         = kindCd;
            _schoolName     = schoolName;
            _jobName        = jobName;
            _principalName  = principalName;
            _remark1        = remark1;
            _remark2        = remark2;
            _remark3        = remark3;
            _remark4        = remark4;
        }
    }
    private class DocumentMst {
        private final String _title;
        private final String _text;
        public DocumentMst(
                final String title,
                final String text
        ) {
            _title  = title;
            _text   = text;
        }
    }
}

// eof

