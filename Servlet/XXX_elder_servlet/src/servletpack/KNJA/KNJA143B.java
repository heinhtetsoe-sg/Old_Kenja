/*
 * $Id: d3a91a45e9b5cd6abec416bf5464428e56437c5a $
 *
 * 作成日: 2009/11/12
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJA;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 *
 *  学校教育システム 賢者 [学籍管理]
 *
 *                  ＜ＫＮＪＡ１４３Ｂ＞  生徒証明書発行台帳
 **/

public class KNJA143B {

    private static final Log log = LogFactory.getLog(KNJA143B.class);

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
        response.setContentType("application/pdf");

            svf.VrInit();
            svf.VrSetSpoolFileStream(response.getOutputStream());

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
            if (null != db2) {
                db2.commit();
                db2.close();
            }
            svf.VrQuit();
        }

    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrSetForm("KNJA143B.frm", 4);
        svf.VrsOut("DATE1", KNJ_EditDate.h_format_JP(_param._ctrlDate));
        svf.VrsOut("NENDO", KNJ_EditDate.h_format_JP_N(_param._year + "-04-01") + "度");
        
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            final String sqlSchreg = sqlSchregRegdDat();
            log.debug(" sql =" + sqlSchreg);
            ps = db2.prepareStatement(sqlSchreg);
            rs = ps.executeQuery();
            while (rs.next()) {
                svf.VrsOut("SCHREGNO", rs.getString("SCHREGNO"));
                svf.VrsOut("NAME", rs.getString("NAME"));
                svf.VrsOut("KANA", rs.getString("NAME_KANA"));
                svf.VrsOut("SEX", rs.getString("SEX"));
                svf.VrsOut("BIRTHDAY", KNJ_EditDate.h_format_JP(rs.getString("BIRTHDAY")));
                if ("1".equals(_param._useAddrField2)) {
                    svf.VrsOut("ADDRESS1_2", rs.getString("ADDR1"));
                    svf.VrsOut("ADDRESS2_2", rs.getString("ADDR2"));
                } else {
                    svf.VrsOut("ADDRESS1", rs.getString("ADDR1"));
                    svf.VrsOut("ADDRESS2", rs.getString("ADDR2"));
                }

                final String hrName = null != rs.getString("HR_NAME") ? rs.getString("HR_NAME") : "";
                final String attendNo = null != rs.getString("ATTENDNO") ? Integer.valueOf(rs.getString("ATTENDNO")).toString() + "番" : "" ;
                svf.VrsOut("REMARK", hrName + attendNo);

                svf.VrEndRecord();
                _hasData = true;
            }
            
            db2.commit();
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
    }

    private String sqlSchregRegdDat() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, T1.NAME, T1.NAME_KANA, T3.NAME2 AS SEX,");
        stb.append("     T1.BIRTHDAY, T4.ADDR1, T4.ADDR2, T5.HR_NAME, T2.ATTENDNO ");
        stb.append(" FROM");
        stb.append("     SCHREG_BASE_MST T1 ");
        stb.append("     INNER JOIN SCHREG_REGD_DAT T2 ON");
        stb.append("         T1.SCHREGNO = T2.SCHREGNO");
        stb.append("     LEFT JOIN NAME_MST T3 ON");
        stb.append("         T3.NAMECD1 = 'Z002'");
        stb.append("         AND T3.NAMECD2 = T1.SEX");
        stb.append("     LEFT JOIN SCHREG_ADDRESS_DAT T4 ON");
        stb.append("         T4.SCHREGNO = T1.SCHREGNO");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT T5 ON");
        stb.append("         T5.YEAR = T2.YEAR ");
        stb.append("         AND T5.SEMESTER = T2.SEMESTER ");
        stb.append("         AND T5.GRADE = T2.GRADE ");
        stb.append("         AND T5.HR_CLASS = T2.HR_CLASS ");
        stb.append(" WHERE");
        stb.append("     T2.YEAR = '" + _param._year + "'");
        if ("2".equals(_param._sinnyuTennyu)) {
            stb.append("     AND T2.SEMESTER = (SELECT MIN(SEMESTER) FROM SCHREG_REGD_DAT ");
            stb.append("                          WHERE SCHREGNO = T2.SCHREGNO AND YEAR = T2.YEAR) ");
        } else {
            stb.append("     AND T2.SEMESTER = '1' ");
        }
        stb.append("     AND T2.SEMESTER <> '9' ");
        if ("2".equals(_param._sinnyuTennyu)) {
            stb.append("     AND T1.ENT_DIV IN ('4', '5')");
            stb.append("     AND T1.ENT_DATE BETWEEN '" + _param._sdate + "' AND '" + _param._edate + "' ");
            stb.append("     AND T1.SCHREGNO IN " + SQLUtils.whereIn(true, _param._selectStudents) +"  ");
        } else {
            stb.append("     AND T2.GRADE = '01'");
            stb.append("     AND T1.ENT_DIV NOT IN ('4', '5')");
        }
        stb.append("     AND (T4.ISSUEDATE IS NULL OR T4.ISSUEDATE = (SELECT MIN(ISSUEDATE)");
        stb.append("            FROM SCHREG_ADDRESS_DAT T_ADDR");
        stb.append("             WHERE SCHREGNO = T1.SCHREGNO ");
        stb.append("               AND '" + _param._year + "' BETWEEN FISCALYEAR(ISSUEDATE) AND FISCALYEAR(EXPIREDATE))) ");
        stb.append(" ORDER BY");
        stb.append("     T1.SCHREGNO");
        return stb.toString();
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
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final String _sinnyuTennyu;

        private final String _year;

        private final String _sdate;
        private final String _edate;
        private final String[] _selectStudents;
        
        private final String _useAddrField2;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _year = request.getParameter("YEAR");
            _sinnyuTennyu = request.getParameter("SINNYU_TENNYU"); // 1:新入生、2:転入・編入生

            if ("2".equals(_sinnyuTennyu)) {
                _sdate = request.getParameter("SDATE").replace('/', '-');
                _edate = request.getParameter("EDATE").replace('/', '-');
                _selectStudents = request.getParameterValues("CATEGORY_SELECTED");
            } else {
                _sdate = null;
                _edate = null;
                _selectStudents = null;
            }
            _useAddrField2 = request.getParameter("useAddrField2");
        }

    }
}

// eof

