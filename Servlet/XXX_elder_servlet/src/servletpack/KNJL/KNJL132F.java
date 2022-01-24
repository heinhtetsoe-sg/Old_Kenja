/*
 * $Id: c5caa82ccdb769f6edddbdda8a72d7d1730f0a28 $
 *
 * 作成日: 2016/11/09
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

public class KNJL132F {

    private static final Log log = LogFactory.getLog(KNJL132F.class);

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
        svf.VrSetForm("KNJL132F.frm", 1);
        setTitle(svf);

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final String sql = sql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            int lineCnt = 1;
            while (rs.next()) {
                if (lineCnt > 50) {
                    svf.VrEndPage();
                    setTitle(svf);
                    lineCnt = 1;
                }
                svf.VrsOutn("NAME", lineCnt, rs.getString("NAME"));
                svf.VrsOutn("KANA", lineCnt, rs.getString("NAME_KANA"));
                svf.VrsOutn("PREF_NAME", lineCnt, rs.getString("PREF_NAME"));
                svf.VrsOutn("CITY_NAME", lineCnt, rs.getString("FINCITY"));
                svf.VrsOutn("SCHOOL_DUV", lineCnt, rs.getString("FINDIV"));
                svf.VrsOutn("FINSCHOOL_NAME", lineCnt, rs.getString("FINSCHOOL_NAME"));
                svf.VrsOutn("TEL_NO", lineCnt, rs.getString("FINSCHOOL_TELNO"));

                lineCnt++;
                _hasData = true;
            }
            if (lineCnt > 1) {
                svf.VrEndPage();
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
        stb.append(" WITH RECEPT_MAX AS ( ");
        stb.append(" SELECT ");
        stb.append("     RECEPT.EXAMNO, ");
        stb.append("     MAX(RECEPT.RECEPTNO) AS RECEPTNO ");
        stb.append(" FROM ");
        stb.append("     V_ENTEXAM_RECEPT_DAT RECEPT ");
        stb.append(" WHERE ");
        stb.append("     RECEPT.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND RECEPT.APPLICANTDIV = '" + _param._applicantdiv + "' ");
        stb.append("     AND RECEPT.JUDGEDIV = '1' ");
        stb.append(" GROUP BY ");
        stb.append("     RECEPT.EXAMNO ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     BASE.EXAMNO, ");
        stb.append("     RECEPT_MAX.RECEPTNO, ");
        stb.append("     BASE.NAME, ");
        stb.append("     BASE.NAME_KANA, ");
        stb.append("     COURSE.EXAMCOURSE_NAME, ");
        stb.append("     PREF.PREF_NAME, ");
        stb.append("     L001.NAME1 AS FINCITY, ");
        stb.append("     L015.NAME1 AS FINDIV, ");
        stb.append("     FSCHOOL.FINSCHOOL_NAME, ");
        stb.append("     FSCHOOL.FINSCHOOL_TELNO, ");
        stb.append("     PRINT.PRINTFLG, ");
        stb.append("     PRINT.GET_YOUROKU, ");
        stb.append("     PRINT.GET_MEDEXAM ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_APPLICANTBASE_DAT BASE ");
        stb.append("     INNER JOIN RECEPT_MAX ON BASE.EXAMNO = RECEPT_MAX.EXAMNO ");
        stb.append("     LEFT JOIN FINSCHOOL_MST FSCHOOL ON BASE.FS_CD = FSCHOOL.FINSCHOOLCD ");
        stb.append("     LEFT JOIN PREF_MST PREF ON FSCHOOL.FINSCHOOL_PREF_CD = PREF.PREF_CD ");
        stb.append("     LEFT JOIN NAME_MST L001 ON L001.NAMECD1 = 'L001' ");
        stb.append("          AND FSCHOOL.FINSCHOOL_DISTCD = L001.NAMECD2 ");
        stb.append("     LEFT JOIN NAME_MST L015 ON L015.NAMECD1 = 'L015' ");
        stb.append("          AND FSCHOOL.FINSCHOOL_DIV = L015.NAMECD2 ");
        stb.append("     LEFT JOIN ENTEXAM_COURSE_MST COURSE ON BASE.ENTEXAMYEAR = COURSE.ENTEXAMYEAR ");
        stb.append("          AND BASE.APPLICANTDIV = COURSE.APPLICANTDIV ");
        stb.append("          AND '1' = COURSE.TESTDIV ");
        stb.append("          AND BASE.SUC_COURSECD = COURSE.COURSECD ");
        stb.append("          AND BASE.SUC_MAJORCD = COURSE.MAJORCD ");
        stb.append("          AND BASE.SUC_COURSECODE = COURSE.EXAMCOURSECD ");
        stb.append("     LEFT JOIN ENTEXAM_PRINT_DAT PRINT ON BASE.ENTEXAMYEAR = PRINT.ENTEXAMYEAR ");
        stb.append("          AND BASE.EXAMNO = PRINT.EXAMNO ");
        stb.append("     INNER JOIN ENTEXAM_MONEY_DAT MONEY_D ON BASE.ENTEXAMYEAR = MONEY_D.ENTEXAMYEAR ");
        stb.append("           AND BASE.APPLICANTDIV = MONEY_D.APPLICANTDIV ");
        stb.append("           AND BASE.EXAMNO = MONEY_D.EXAMNO ");
        stb.append("           AND MONEY_D.ENT_PAY_DATE IS NOT NULL ");
        stb.append("           AND MONEY_D.ENT_PAY_MONEY IS NOT NULL ");
        stb.append("           AND MONEY_D.EXP_PAY_DATE IS NOT NULL ");
        stb.append("           AND MONEY_D.EXP_PAY_MONEY IS NOT NULL ");
        stb.append(" WHERE ");
        stb.append("     BASE.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND BASE.APPLICANTDIV = '" + _param._applicantdiv + "' ");
        stb.append("     AND BASE.FS_CD IN (" + _param._finSchoolInState + ") ");
        stb.append("     AND BASE.JUDGEMENT = '1' ");
        stb.append("     AND BASE.ENTDIV = '1' ");
        stb.append("     AND BASE.EXAMNO NOT IN ( ");
        stb.append("                 SELECT ");
        stb.append("                     EXAMNO ");
        stb.append("                 FROM ");
        stb.append("                     ENTEXAM_PRINT_DAT ");
        stb.append("                 WHERE ");
        stb.append("                     ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("                     AND GET_YOUROKU = '1' ");
        stb.append("                     AND GET_MEDEXAM = '1' ");
        stb.append("                 ) ");
        stb.append(" ORDER BY ");
        stb.append("     BASE.FS_CD, ");
        stb.append("     BASE.EXAMNO ");

        return stb.toString();
    }

    private void setTitle(final Vrw32alp svf) {
        svf.VrsOut("TITLE", KenjaProperties.gengou(Integer.parseInt(_param._entexamyear)) + "年度　未提出者一覧");
        svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._ctrlDate));
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 63563 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _entexamyear;
        private final String _applicantdiv;
        private final String _ctrlDate;
        private final String[] _finSchool;
        private final String _finSchoolInState;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear        = request.getParameter("ENTEXAMYEAR");
            _applicantdiv       = request.getParameter("APPLICANTDIV");
            _ctrlDate           = request.getParameter("CTRL_DATE");
            _finSchool          = request.getParameterValues("CLASS_SELECTED");
            _finSchoolInState = "'" + StringUtils.join(_finSchool, "','") + "'";
        }

    }
}

// eof

