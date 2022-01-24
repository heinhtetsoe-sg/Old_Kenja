/*
 * $Id: 9c2e365488bfc362503c929d7e40a2fb9e5a05fe $
 *
 * 作成日: 2017/09/26
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJJ;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJJ213 {

    private static final Log log = LogFactory.getLog(KNJJ213.class);

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
        svf.VrSetForm("KNJJ213.frm", 1);

        PreparedStatement ps = null;
        ResultSet rs = null;

        final int maxLine = 6;
        final int maxCol = 3;
        int lineCnt = Integer.parseInt(_param._porow);
        int colCnt = Integer.parseInt(_param._pocol);
        try {
            final String sql = committeeSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                if (colCnt > maxCol) {
                    lineCnt++;
                    colCnt = 1;
                }
                if (lineCnt > maxLine) {
                    svf.VrEndPage();
                    lineCnt = 1;
                    colCnt = 1;
                }

                svf.VrsOutn("ZIPCODE" + colCnt, lineCnt, rs.getString("ZIPCD"));
                final String setAddr1 = null != rs.getString("ADDR1") ? rs.getString("ADDR1") : "";
                final String setAddr2 = null != rs.getString("ADDR2") ? rs.getString("ADDR2") : "";
                final String addrField = KNJ_EditEdit.getMS932ByteLength(setAddr1) > 50 || KNJ_EditEdit.getMS932ByteLength(setAddr2) > 50 ? "_3" : KNJ_EditEdit.getMS932ByteLength(setAddr1) > 40 || KNJ_EditEdit.getMS932ByteLength(setAddr2) > 40 ? "_2" : "_1";
                svf.VrsOutn("ADDRESS" + colCnt + "_1" + addrField, lineCnt, setAddr1);
                svf.VrsOutn("ADDRESS" + colCnt + "_2" + addrField, lineCnt, setAddr2);
                final String setName = rs.getString("NAME") + "　様";
                final String nameField = KNJ_EditEdit.getMS932ByteLength(setName) > 24 ? "_1_2" : "_1";
                svf.VrsOutn("NAME" + colCnt + nameField, lineCnt, setName);
                _hasData = true;
                colCnt++;
            }

            if (_hasData) {
                svf.VrEndPage();
            }
            db2.commit();
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
    }

    private String committeeSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.* ");
        stb.append(" FROM ");
        stb.append("     EVALUATION_COMMITTEE_DAT T1 ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("     AND T1.COMMITTEECD IN (" + _param._committeeInSentence + ") ");
        stb.append(" ORDER BY ");
        stb.append("     T1.POSITION_DIV, ");
        stb.append("     T1.POSITION_CD, ");
        stb.append("     T1.COMMITTEECD ");
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
        final String[] _categoryName;
        final String _committeeInSentence;
        final String _porow;
        final String _pocol;
        final String _prgid;
        final String _ctrlYear;
        final String _ctrlSemester;
        final String _ctrlDate;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _categoryName = request.getParameterValues("category_name");
            _porow = request.getParameter("POROW");
            _pocol = request.getParameter("POCOL");
            _prgid = request.getParameter("PRGID");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            String setInSentence = "";
            String sep = "";
            for (int i = 0; i < _categoryName.length; i++) {
                setInSentence += sep + "'" + _categoryName[i] + "'";
                sep = ",";
            }
            _committeeInSentence = setInSentence;
        }

    }
}

// eof

