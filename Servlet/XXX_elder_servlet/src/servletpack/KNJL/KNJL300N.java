/*
 * $Id: 127494896ed15a9e410ab533fdf67cd8f961dc1e $
 *
 * 作成日: 2015/09/08
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;

/**
 *  学校教育システム 賢者 [入試管理]
 *
 *                  ＜ＫＮＪＬ３００Ｎ＞  座席ラベル
 **/
public class KNJL300N {

    private static final Log log = LogFactory.getLog(KNJL300N.class);

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
    
    private String sql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     BASE.EXAMNO ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_RECEPT_DAT T1 ");
        stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE ON BASE.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("         AND BASE.EXAMNO = T1.EXAMNO ");
        stb.append(" WHERE ");
        stb.append("     T1.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND T1.APPLICANTDIV = '" + _param._applicantdiv + "' ");
        stb.append("     AND T1.TESTDIV = '" + _param._testdiv + "' ");
        if (null != _param._desirediv) {
            stb.append("     AND BASE.DESIREDIV = '" + _param._desirediv + "' ");
        }
        if (null != _param._noinfSt) {
            stb.append("     AND BASE.EXAMNO >= '" + _param._noinfSt + "' ");
        }
        if (null != _param._noinfEd) {
            stb.append("     AND BASE.EXAMNO <= '" + _param._noinfEd + "' ");
        }
        stb.append(" ORDER BY ");
        stb.append("     BASE.EXAMNO ");
        return stb.toString();
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        final List examnoList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = sql();
            // log.debug(" sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                examnoList.add(rs.getString("EXAMNO"));
            }
        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        if (examnoList.isEmpty()) {
            return;
        }
        
        svf.VrSetForm("KNJL300N.frm", 1);
        final int maxRow = 6;
        final int maxCol = 3;
        int row = _param._porow;
        int col = _param._pocol;
        for (final Iterator it = examnoList.iterator(); it.hasNext();){
            final String examno = (String) it.next();

            if (col > maxCol) {
                row++;
                if (row > maxRow) {
                    svf.VrEndPage();
                    row = 1;
                }
                col = 1;
            }

            svf.VrsOutn("EXAMNO" + String.valueOf(col), row, examno); // 受験番号
            col++;
        }
        svf.VrEndPage();
        _hasData = true;
    }
    
    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 56595 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _entexamyear;
        final String _applicantdiv;
        final String _testdiv;
        final String _desirediv;
        final String _noinfSt;
        final String _noinfEd;
        final int _porow;
        final int _pocol;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _applicantdiv = request.getParameter("APPLICANTDIV");
            _testdiv = request.getParameter("TESTDIV");
            _desirediv = request.getParameter("DESIREDIV");
            _noinfSt = !StringUtils.isBlank(request.getParameter("NOINF_ST")) ? request.getParameter("NOINF_ST") : null;
            _noinfEd = !StringUtils.isBlank(request.getParameter("NOINF_ED")) ? request.getParameter("NOINF_ED") : null;
            _porow = NumberUtils.isDigits(request.getParameter("POROW")) ? Integer.parseInt(request.getParameter("POROW")) : 1;
            _pocol = NumberUtils.isDigits(request.getParameter("POCOL")) ? Integer.parseInt(request.getParameter("POCOL")) : 1;
        }
    }
}

// eof

