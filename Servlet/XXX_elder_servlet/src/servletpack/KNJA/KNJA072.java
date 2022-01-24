/*
 * $Id: ae6b650d644ea5aefc29d49e86e45d05cc602a5a $
 *
 * 作成日: 2009/10/26
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

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 *
 *   学校教育システム 賢者 [学籍管理]
 *
 *                   ＜ＫＮＪＡ０７２＞  クラス一覧
 */

public class KNJA072 {

    private static final Log log = LogFactory.getLog(KNJA072.class);

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
        svf.VrSetForm("KNJA072.frm", 4);
        svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._loginDate));
        svf.VrsOut("NENDO", KNJ_EditDate.h_format_JP_N(_param._year + "-04-01") + "度");
        svf.VrsOut("SEMESTER", _param._semesterName);

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final String sql = sql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {

                svf.VrsOut("GRADE", String.valueOf(Integer.parseInt(rs.getString("GRADE"))));
                svf.VrsOut("HR_CLASS", rs.getString("HR_CLASS"));
                svf.VrsOut("HR_NAME", rs.getString("HR_NAME"));
                svf.VrsOut("HR_NAMEABBV", rs.getString("HR_NAMEABBV"));

                final String trCd1Field = rs.getString("TR_NAME1") != null && rs.getString("TR_NAME1").getBytes().length > 20 ? "TR_CD1_1" : "TR_CD1";
                final String trCd2Field = rs.getString("TR_NAME2") != null && rs.getString("TR_NAME2").getBytes().length > 20 ? "TR_CD2_1" : "TR_CD2";
                final String trCd3Field = rs.getString("TR_NAME3") != null && rs.getString("TR_NAME3").getBytes().length > 20 ? "TR_CD3_1" : "TR_CD3";
                svf.VrsOut(trCd1Field, rs.getString("TR_NAME1"));
                svf.VrsOut(trCd2Field, rs.getString("TR_NAME2"));
                svf.VrsOut(trCd3Field, rs.getString("TR_NAME3"));
                svf.VrsOut("MALE", rs.getString("MAN_COUNT"));
                svf.VrsOut("FEMALE", rs.getString("WOMAN_COUNT"));
                svf.VrsOut("TOTAL", rs.getString("TOTAL_COUNT"));

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

    private String sql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT");
        stb.append("     T1.GRADE");
        stb.append("     ,T1.HR_CLASS");
        stb.append("     ,T1.HR_NAME");
        stb.append("     ,T1.HR_NAMEABBV");
        stb.append("     ,T2.STAFFNAME AS TR_NAME1");
        stb.append("     ,T3.STAFFNAME AS TR_NAME2");
        stb.append("     ,T4.STAFFNAME AS TR_NAME3");
        stb.append("     ,COUNT(DISTINCT T5.SCHREGNO) AS TOTAL_COUNT");
        stb.append("     ,SUM(CASE WHEN T6.SEX = '1' THEN 1 ELSE 0 END) AS MAN_COUNT");
        stb.append("     ,SUM(CASE WHEN T6.SEX = '2' THEN 1 ELSE 0 END) AS WOMAN_COUNT");
        stb.append(" FROM");
        stb.append("     SCHREG_REGD_HDAT T1");
        stb.append("     LEFT JOIN STAFF_MST T2 ON T2.STAFFCD = T1.TR_CD1");
        stb.append("     LEFT JOIN STAFF_MST T3 ON T3.STAFFCD = T1.TR_CD2");
        stb.append("     LEFT JOIN STAFF_MST T4 ON T4.STAFFCD = T1.TR_CD3");
        stb.append("     LEFT JOIN SCHREG_REGD_DAT T5 ON");
        stb.append("         T1.GRADE = T5.GRADE");
        stb.append("         AND T1.HR_CLASS = T5.HR_CLASS");
        stb.append("         AND T1.YEAR = T5.YEAR");
        stb.append("         AND T1.SEMESTER = T5.SEMESTER");
        stb.append("     LEFT JOIN SCHREG_BASE_MST T6 ON");
        stb.append("         T5.SCHREGNO = T6.SCHREGNO");
        if ("1".equals(_param._use_prg_schoolkind)) {
            if (!"".equals(_param._schoolKindInState)) {
                stb.append(" INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR AND GDAT.GRADE = T1.GRADE ");
                stb.append("   AND GDAT.SCHOOL_KIND IN (" + _param._schoolKindInState + ") ");
            }
        } else if ("1".equals(_param._useSchool_KindField) && !StringUtils.isBlank(_param._SCHOOLKIND)) {
            stb.append(" INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR AND GDAT.GRADE = T1.GRADE ");
            stb.append("   AND GDAT.SCHOOL_KIND = '" + _param._SCHOOLKIND + "' ");
        }
        stb.append(" WHERE");
        stb.append("     T1.YEAR = '" + _param._year + "'");
        stb.append("     AND T1.SEMESTER = '" + _param._semester + "'");
        stb.append(" GROUP BY");
        stb.append("     T1.GRADE, T1.HR_CLASS, T1.HR_NAME, T1.HR_NAMEABBV,");
        stb.append("     T2.STAFFNAME, T3.STAFFNAME, T4.STAFFNAME");
        stb.append(" ORDER BY");
        stb.append("     T1.GRADE, T1.HR_CLASS");
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
        final String _year;
        final String _semester;
        final String _loginDate;
        final String _useSchool_KindField;
        final String _SCHOOLKIND;
        final String _use_prg_schoolkind;
        final String _selectSchoolKind;
        final String _schoolKindInState;

        String _semesterName;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _loginDate = request.getParameter("CTRL_DATE");
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            _SCHOOLKIND = request.getParameter("SCHOOLKIND");
            _use_prg_schoolkind = request.getParameter("use_prg_schoolkind");
            _selectSchoolKind = request.getParameter("selectSchoolKind");
            _schoolKindInState = getSchoolKindInState();

            setSemesterName(db2);
        }

        private String getSchoolKindInState() {
            String retStr = "";
            if (!"1".equals(_use_prg_schoolkind)) {
                return retStr;
            }
            if (null == _selectSchoolKind || "".equals(_selectSchoolKind)) {
                return retStr;
            }
            final String[] strSplit = StringUtils.split(_selectSchoolKind, ":");
            String sep = "";
            for (int i = 0; i < strSplit.length; i++) {
                retStr += sep + "'" + strSplit[i] + "'";
                sep = ",";
            }
            return retStr;
        }

        private void setSemesterName(DB2UDB db2) {
            _semesterName = null;
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                ps = db2.prepareStatement("SELECT SEMESTERNAME FROM SEMESTER_MST WHERE YEAR = '" + _year + "' AND SEMESTER = '" + _semester +"' ");
                rs = ps.executeQuery();
                if (rs.next()) {
                    _semesterName = rs.getString("SEMESTERNAME");
                }
            } catch (SQLException e) {
                log.error("Exception:", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
        }
    }
}

// eof

