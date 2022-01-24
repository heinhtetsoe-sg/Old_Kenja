/**     プロジェクト/プロパティ/javaコード・スタイル/コード・テンプレート/パターン       ***/

/*
 * $Id: b6228455cb43a96057f47bf50b0642e9251e226e $
 *
 * 作成日: 2018/08/10
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

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJP725 {

    private static final Log log = LogFactory.getLog(KNJP725.class);

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
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            String befGH = "";
            String befSchregNo = "";
            int lineCnt = 0;
            while (rs.next()) {
                final String grade = rs.getString("GRADE");
                final String hrClass = rs.getString("HR_CLASS");
                final String attendno = rs.getString("ATTENDNO");
                final String schregno = rs.getString("SCHREGNO");
                final String sex = rs.getString("SEX");
                final String name = rs.getString("NAME");
                final String seq = rs.getString("SEQ");
                final String bankcd = rs.getString("BANKCD");
                final String bankName = rs.getString("BANKNAME");
                final String branchcd = rs.getString("BRANCHCD");
                final String branchName = rs.getString("BRANCHNAME");
                final String depositName = rs.getString("DEPOSIT_NAME");
                final String accountno = rs.getString("ACCOUNTNO");
                final String accountname = rs.getString("ACCOUNTNAME");
                final String relationName = rs.getString("RELATION_NAME");

                if (!befGH.equals(grade + hrClass)) {
                    svf.VrSetForm("KNJP725.frm", 4);
                }
                svf.VrsOut("TITLE", KNJ_EditDate.gengou(db2, Integer.parseInt(_param._ctrlYear)) + "年度　個人口座リスト");
                svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(db2, _param._loginDate));

                if (!befSchregNo.equals(schregno)) {
                    lineCnt++;
                    svf.VrsOut("NO", String.valueOf(lineCnt));
                    svf.VrsOut("GRADE", String.valueOf(Integer.parseInt(grade)));
                    svf.VrsOut("HR_CLASS", String.valueOf(Integer.parseInt(hrClass)));
                    svf.VrsOut("ATTENDNO", attendno);
                    svf.VrsOut("SCHREGNO", schregno);
                    svf.VrsOut("SEX", sex);
                    final String nameField = KNJ_EditEdit.getMS932ByteLength(name) > 26 ? "_3" : KNJ_EditEdit.getMS932ByteLength(name) > 20 ? "_2" : "";
                    svf.VrsOut("NAME" + nameField, name);
                }
                svf.VrsOut("SEQ", seq);
                svf.VrsOut("BANKCD", bankcd);
                svf.VrsOut("BANKNAME", bankName);
                svf.VrsOut("BRANCHCD", branchcd);
                svf.VrsOut("BRANCHNAME", branchName);
                svf.VrsOut("DEPOSIT_NAME", depositName);
                svf.VrsOut("ACCOUNTNO", accountno);
                final String accountnameField = KNJ_EditEdit.getMS932ByteLength(accountname) > 20 ? "_2" : "";
                svf.VrsOut("ACCOUNTNAME" + accountnameField, accountname);
                svf.VrsOut("RELATION_NAME", relationName);
                svf.VrEndRecord();
                _hasData = true;
                befGH = grade + hrClass;
                befSchregNo = schregno;
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
    }

    private String getSql() {
        final StringBuffer stb = new StringBuffer();
        if ("0".equals(_param._sOrderFlg)) {
            stb.append(" SELECT ");
            stb.append("     FDAT.GRADE, ");
            stb.append("     FDAT.HR_CLASS, ");
            stb.append("     FDAT.ATTENDNO, ");
            stb.append("     FDAT.SCHREGNO, ");
            stb.append("     Z002.NAME2 AS SEX, ");
            stb.append("     FDAT.NAME, ");
            stb.append("     RBANK.SEQ, ");
            stb.append("     RBANK.BANKCD, ");
            stb.append("     BANK.BANKNAME, ");
            stb.append("     RBANK.BRANCHCD, ");
            stb.append("     BANK.BRANCHNAME, ");
            stb.append("     G203.NAME1 AS DEPOSIT_NAME, ");
            stb.append("     RBANK.ACCOUNTNO, ");
            stb.append("     RBANK.ACCOUNTNAME, ");
            stb.append("     CASE WHEN RBANK.RELATIONSHIP = '00' THEN '本人' ELSE H201.NAME1 END AS RELATION_NAME ");
            stb.append(" FROM ");
            stb.append("     FRESHMAN_DAT FDAT ");
            stb.append("     LEFT JOIN NAME_MST Z002 ON Z002.NAMECD1 = 'Z002' ");
            stb.append("          AND FDAT.SEX = Z002.NAMECD2 ");
            stb.append("     INNER JOIN SCHREG_REGD_GDAT GDAT ON FDAT.ENTERYEAR = CAST((INT(GDAT.YEAR) + 1) AS CHAR(4)) ");
            stb.append("          AND FDAT.GRADE = GDAT.GRADE ");
            stb.append("     LEFT JOIN REGISTBANK_DAT RBANK ON RBANK.SCHOOLCD = '" + _param._schoolcd + "' ");
            stb.append("          AND FDAT.SCHREGNO = RBANK.SCHREGNO ");
            stb.append("     LEFT JOIN BANK_MST BANK ON RBANK.BANKCD = BANK.BANKCD ");
            stb.append("          AND RBANK.BRANCHCD = BANK.BRANCHCD ");
            stb.append("     LEFT JOIN NAME_MST G203 ON G203.NAMECD1 = 'G203' ");
            stb.append("          AND RBANK.DEPOSIT_ITEM = G203.NAMECD2 ");
            stb.append("     LEFT JOIN NAME_MST H201 ON H201.NAMECD1 = 'H201' ");
            stb.append("          AND RBANK.RELATIONSHIP = H201.NAMECD2 ");
            stb.append(" WHERE ");
            stb.append("     FDAT.ENTERYEAR = '" + (Integer.parseInt(_param._ctrlYear) + 1) + "' ");
            if ("1".equals(_param._categoryIsClass)) {
                stb.append("  AND FDAT.GRADE || '-' || FDAT.HR_CLASS IN " + SQLUtils.whereIn(true, _param._categorySelected));
            } else {
                stb.append("  AND FDAT.SCHREGNO IN " + SQLUtils.whereIn(true, _param._categorySelected));
            }
            stb.append(" ORDER BY ");
            stb.append("     FDAT.GRADE, ");
            stb.append("     FDAT.HR_CLASS, ");
            stb.append("     FDAT.ATTENDNO, ");
            stb.append("     RBANK.SEQ ");
        } else {
            stb.append(" SELECT ");
            stb.append("     REGD.GRADE, ");
            stb.append("     REGD.HR_CLASS, ");
            stb.append("     REGD.ATTENDNO, ");
            stb.append("     REGD.SCHREGNO, ");
            stb.append("     Z002.NAME2 AS SEX, ");
            stb.append("     BASE.NAME, ");
            stb.append("     RBANK.SEQ, ");
            stb.append("     RBANK.BANKCD, ");
            stb.append("     BANK.BANKNAME, ");
            stb.append("     RBANK.BRANCHCD, ");
            stb.append("     BANK.BRANCHNAME, ");
            stb.append("     G203.NAME1 AS DEPOSIT_NAME, ");
            stb.append("     RBANK.ACCOUNTNO, ");
            stb.append("     RBANK.ACCOUNTNAME, ");
            stb.append("     CASE WHEN RBANK.RELATIONSHIP = '00' THEN '本人' ELSE H201.NAME1 END AS RELATION_NAME ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT REGD ");
            stb.append("     INNER JOIN SCHREG_BASE_MST BASE ON REGD.SCHREGNO = BASE.SCHREGNO ");
            stb.append("     LEFT JOIN NAME_MST Z002 ON Z002.NAMECD1 = 'Z002' ");
            stb.append("          AND BASE.SEX = Z002.NAMECD2 ");
            stb.append("     INNER JOIN SCHREG_REGD_GDAT GDAT ON REGD.YEAR = GDAT.YEAR ");
            stb.append("          AND REGD.GRADE = GDAT.GRADE ");
            stb.append("     LEFT JOIN REGISTBANK_DAT RBANK ON RBANK.SCHOOLCD = '" + _param._schoolcd + "' ");
            stb.append("          AND REGD.SCHREGNO = RBANK.SCHREGNO ");
            stb.append("     LEFT JOIN BANK_MST BANK ON RBANK.BANKCD = BANK.BANKCD ");
            stb.append("          AND RBANK.BRANCHCD = BANK.BRANCHCD ");
            stb.append("     LEFT JOIN NAME_MST G203 ON G203.NAMECD1 = 'G203' ");
            stb.append("          AND RBANK.DEPOSIT_ITEM = G203.NAMECD2 ");
            stb.append("     LEFT JOIN NAME_MST H201 ON H201.NAMECD1 = 'H201' ");
            stb.append("          AND RBANK.RELATIONSHIP = H201.NAMECD2 ");
            stb.append(" WHERE ");
            stb.append("     REGD.YEAR = '" + _param._ctrlYear + "' ");
            stb.append("     AND REGD.SEMESTER = '" + _param._semester + "' ");
            if ("1".equals(_param._categoryIsClass)) {
                stb.append("  AND REGD.GRADE || '-' || REGD.HR_CLASS IN " + SQLUtils.whereIn(true, _param._categorySelected));
            } else {
                stb.append("  AND REGD.SCHREGNO IN " + SQLUtils.whereIn(true, _param._categorySelected));
            }
            stb.append(" ORDER BY ");
            stb.append("     REGD.GRADE, ");
            stb.append("     REGD.HR_CLASS, ");
            stb.append("     REGD.ATTENDNO, ");
            stb.append("     RBANK.SEQ ");
        }
        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 75206 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _categoryIsClass;
        final String _semester;
        final String _grade;
        final String _sOrderFlg;
        final String _hrClass;
        final String[] _categorySelected;
        final String _ctrlYear;
        final String _ctrlSemester;
        final String _loginDate;
        final String _schoolcd;
        final String _prgid;
        final String _printLogStaffcd;
        final String _printLogRemoteIdent;
        final String _printLogRemoteAddr;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _categoryIsClass = request.getParameter("CATEGORY_IS_CLASS");
            _semester = request.getParameter("SEMESTER");
            final String[] orderwk = StringUtils.split(StringUtils.defaultString(request.getParameter("GRADE"), ""), "-");
            if (orderwk.length > 1) {
                _grade = orderwk[0];
                _sOrderFlg = orderwk[1];
            } else {
                //区切り文字が無い場合は、今まで通りの動作とする。
                _grade = request.getParameter("GRADE");
                _sOrderFlg = "0";
            }
            _hrClass = request.getParameter("HR_CLASS");
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _loginDate = request.getParameter("LOGIN_DATE");
            _schoolcd = request.getParameter("SCHOOLCD");
            _prgid = request.getParameter("PRGID");
            _printLogStaffcd = request.getParameter("PRINT_LOG_STAFFCD");
            _printLogRemoteIdent = request.getParameter("PRINT_LOG_REMOTE_IDENT");
            _printLogRemoteAddr = request.getParameter("PRINT_LOG_REMOTE_ADDR");
        }
    }
}

// eof
