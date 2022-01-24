/*
 * $Id: dc5cd39d8f35241a5b8c10c41a65c1e29137e1e1 $
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
import java.util.HashMap;
import java.util.Map;

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

public class KNJL304D{

    private static final Log log = LogFactory.getLog(KNJL304D.class);

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
        final String fmtname = "KNJL304D.frm";
        svf.VrSetForm(fmtname, 1);
        final String nendo = hankakuToZenkaku(_param._entexamyear) + "年度 ";
        final String titleStr = nendo + "入学試験 " + "(" + _param._testdivAbbv1 + ")";
        final String footerStr = "広 島 工 業 大 学 高 等 学 校";
        PreparedStatement ps_hall = null;
        ResultSet rs_hall = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
        	final String hallsql = hallsql();
            log.fatal(" hallsql =" + hallsql);
            ps_hall = db2.prepareStatement(hallsql);
            rs_hall = ps_hall.executeQuery();

            while (rs_hall.next()) {
                final String strtexamno = rs_hall.getString("S_RECEPTNO");
                final String endexamno = rs_hall.getString("E_RECEPTNO");
                final int maxLine = 6;

                final String sql = sql(strtexamno, endexamno);
                log.fatal(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                int lineCnt = 1;
                int pageCnt = 1;
                int colCnt = 1;
                while (rs.next()) {
                    final String examno = rs.getString("EXAMNO");

                    if (lineCnt > maxLine) {
                        lineCnt = 1;
                        if (2 == colCnt) {
                            svf.VrEndPage();
                            pageCnt = pageCnt + 1;
                        	colCnt = 1;
                        }else {
                        	colCnt = 2;
                        }
                    }
                    svf.VrsOutn("NENDO" + colCnt, lineCnt, titleStr);
                    svf.VrsOutn("EXAM_NO" + colCnt, lineCnt, hankakuToZenkaku(examno));
                    svf.VrsOutn("SCHOOL_NAME" + colCnt, lineCnt, footerStr);

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
        stb.append("     AND HALL.EXAMHALLCD IN " + SQLUtils.whereIn(true, _param._leftlist));
        stb.append(" ORDER BY HALL.EXAMHALLCD ASC");
        return stb.toString();
    }

    private String sql(final String strtexamno, final String endexamno) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.EXAMNO ");
        stb.append(" FROM ");
        stb.append("     V_ENTEXAM_APPLICANTBASE_DAT T1 ");
        stb.append(" WHERE ");
        stb.append("     T1.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND T1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND T1.TESTDIV = '" + _param._testDiv + "' ");
        stb.append("     AND T1.EXAMNO BETWEEN '" + strtexamno + "' AND '" + endexamno + "' ");

        stb.append(" ORDER BY ");
        stb.append("     T1.EXAMNO ");
        return stb.toString();
    }

    /** 半角数字から全角数字に変換 **/
    private static String hankakuToZenkaku(final String name) {
        if (null == name) {
            return null;
        }
        final Map henkanMap = new HashMap();
        henkanMap.put("1", "１");
        henkanMap.put("2", "２");
        henkanMap.put("3", "３");
        henkanMap.put("4", "４");
        henkanMap.put("5", "５");
        henkanMap.put("6", "６");
        henkanMap.put("7", "７");
        henkanMap.put("8", "８");
        henkanMap.put("9", "９");
        henkanMap.put("0", "０");
        final StringBuffer sb = new StringBuffer();
        for (int i = 0; i < name.length(); i++) {
            final String key = name.substring(i, i + 1);
            final String val = henkanMap.containsKey(key) ? (String) henkanMap.get(key) : key;
            sb.append(val);
        }
        return sb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 70493 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        private final String _entexamyear;
        private final String _applicantDiv;
        private final String _testDiv;
        private final String _loginDate;
        private final String _loginYear;
        private final String[] _leftlist;
        final String _applicantdivName;
        final String _testdivAbbv1;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _loginDate = request.getParameter("LOGIN_DATE");
            _loginYear = request.getParameter("LOGIN_YEAR");
            _leftlist = request.getParameterValues("LEFT_LIST");
            _applicantdivName = getNameMst(db2, "NAME1", "L003", _applicantDiv);
            _testdivAbbv1 = StringUtils.defaultString(getNameMst(db2, "ABBV1", "L004", _testDiv));
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

