/*
 * $Id: e59aabb78f20c1db5ac893625cb223b25df667f1 $
 *
 * 作成日: 2018/01/10
 * 作成者: yogi
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;


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
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJL303D{

    private static final Log log = LogFactory.getLog(KNJL303D.class);

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
        final String fmtname = _param._outputtype == 2 ? "KNJL303D_2.frm" : "KNJL303D_1.frm";
        svf.VrSetForm(fmtname, 1);

        final String nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_param._entexamyear)) + "年度 ";
        final String ptnname = _param._outputtype == 2 ? "面接用" : "会場用";
        final String loginDateStr = KNJ_EditDate.h_format_JP(db2, _param._loginDate);
        PreparedStatement ps_hall = null;
        ResultSet rs_hall = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
        	final String hallsql = hallsql();
            log.debug(" hallsql =" + hallsql);
            ps_hall = db2.prepareStatement(hallsql);
            rs_hall = ps_hall.executeQuery();

            while (rs_hall.next()) {
                final String hallname = rs_hall.getString("EXAMHALL_NAME");
                final String strtexamno = rs_hall.getString("S_RECEPTNO");
                final String endexamno = rs_hall.getString("E_RECEPTNO");
                final int maxLine = 50;

                final String sql = sql(strtexamno, endexamno);
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                svf.VrsOut("TITLE", nendo + "　" + _param._testdivAbbv1 + "　受験者名簿" + "(" + ptnname + ")");
                svf.VrsOut("PLACE", hallname);
				svf.VrsOut("DATE", loginDateStr);
                svf.VrsOut("PAGE", String.valueOf(1) + "頁"); // ページ

                int lineCnt = 1;
                int pageCnt = 1;
                while (rs.next()) {
                    final String examno = rs.getString("EXAMNO");
                    final String name = rs.getString("NAME");
                    final String finschoolname = rs.getString("FINSCHOOL_NAME_ABBV");
                    final String shdiv_nm = rs.getString("SH_NAME");
                    final String telno = rs.getString("GTELNO");

                    if (lineCnt > maxLine) {
                        svf.VrEndPage();
                        svf.VrsOut("TITLE", nendo + "　" + _param._testdivAbbv1 + "　受験者名簿" + "(" + ptnname + ")");
                        svf.VrsOut("SUBTITLE", hallname);
                        svf.VrsOut("DATE", loginDateStr);
                        svf.VrsOut("PAGE", String.valueOf(pageCnt + 1) + "頁"); // ページ
                        lineCnt = 1;
                        pageCnt = pageCnt + 1;
                    }
                    svf.VrsOutn("EXAM_NO", lineCnt, examno);
                    if (20 >= KNJ_EditEdit.getMS932ByteLength(name)) {
                        svf.VrsOutn("NAME1", lineCnt, name);
                    }else if (30 >= KNJ_EditEdit.getMS932ByteLength(name)) {
                        svf.VrsOutn("NAME2", lineCnt, name);
                    }else {
                        svf.VrsOutn("NAME3", lineCnt, name);
                    }
                    svf.VrsOutn("FINSCHOOL_NAME", lineCnt, finschoolname);
                    svf.VrsOutn("SH_DIV", lineCnt, shdiv_nm);
                    svf.VrsOutn("TEL_NO", lineCnt, telno);

                    lineCnt++;
                    _hasData = true;
                }

                if (_hasData) {
                    svf.VrEndPage();
                }
            }
            db2.commit();
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
    }

    private static int getMS932Bytecount(String str) {
        int count = 0;
        if (null != str) {
            try {
                count = str.getBytes("MS932").length;
            } catch (Exception e) {
                log.error(e);
            }
        }
        return count;
    }

    private String hallsql() {
        final StringBuffer stb = new StringBuffer();

        stb.append(" SELECT ");
        stb.append("     HALL.EXAMHALLCD, ");
        stb.append("     HALL.EXAMHALL_NAME, ");
        stb.append("     HALL.S_RECEPTNO, ");
        stb.append("     HALL.E_RECEPTNO ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_HALL_YDAT HALL ");
        stb.append(" WHERE ");
        stb.append("     HALL.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND HALL.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND HALL.TESTDIV = '" + _param._testDiv + "' ");
        return stb.toString();
    }

    private String sql(final String strtexamno, final String endexamno) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.EXAMNO, ");
        stb.append("     T1.NAME, ");
        stb.append("     T1.FS_CD, ");
        stb.append("     FSCHOOL.FINSCHOOL_NAME_ABBV, ");
        stb.append("     T1.SHDIV, ");
        stb.append("     L006.NAME1 AS SH_NAME, ");
        stb.append("     AD1.GTELNO ");
        stb.append(" FROM ");
        stb.append("     V_ENTEXAM_APPLICANTBASE_DAT T1 ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT AD1 ");
        stb.append("                                 ON T1.ENTEXAMYEAR = AD1.ENTEXAMYEAR ");
        stb.append("                                AND T1.APPLICANTDIV = AD1.APPLICANTDIV ");
        stb.append("                                AND T1.EXAMNO = AD1.EXAMNO ");
        stb.append("     LEFT JOIN FINSCHOOL_MST FSCHOOL ON T1.FS_CD = FSCHOOL.FINSCHOOLCD ");
        stb.append("     LEFT JOIN NAME_MST L006 ON L006.NAMECD1 = 'L006' ");
        stb.append("          AND T1.SHDIV = L006.NAMECD2 ");
        stb.append(" WHERE ");
        stb.append("     T1.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND T1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND T1.TESTDIV = '" + _param._testDiv + "' ");
        stb.append("     AND T1.EXAMNO BETWEEN '" + strtexamno + "' AND '" + endexamno + "' ");

        stb.append(" ORDER BY ");
        if (2 == _param._outputorder) {
            stb.append("     T1.NAME_KANA ");
        } else {
            stb.append("     T1.EXAMNO ");
        }
        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 71866 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        private final String _entexamyear;
        private final String _applicantDiv;
        private final String _testDiv;
        private final String _loginDate;
        final String _applicantdivName;
        final String _testdivAbbv1;
        final int _outputtype;
        final int _outputorder;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _loginDate = request.getParameter("LOGIN_DATE");
            _applicantdivName = getNameMst(db2, "NAME1", "L003", _applicantDiv);
            _testdivAbbv1 = StringUtils.defaultString(getNameMst(db2, "ABBV1", "L004", _testDiv));
            _outputtype = Integer.parseInt(request.getParameter("FORM")); //帳票
            _outputorder = Integer.parseInt(request.getParameter("SORT")); //出力順
        }

        private static String getNameMst(final DB2UDB db2, final String field, final String namecd1, final String namecd2) {
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT " + field + " FROM NAME_MST WHERE NAMECD1 = '" + namecd1 + "' AND NAMECD2 = '" + namecd2 + "' ");
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString(field);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return rtn;
        }

    }
}

// eof

