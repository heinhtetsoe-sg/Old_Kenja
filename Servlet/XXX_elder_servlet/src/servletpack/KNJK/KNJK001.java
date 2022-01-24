/*
 * $Id: d772415e7206ad7ac63ff9f711850171c0e8638c $
 *
 * 作成日: 2017/02/03
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJK;


import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;

public class KNJK001 {

    private static final Log log = LogFactory.getLog(KNJK001.class);

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

            for (Iterator iterator = _param._formMap.keySet().iterator(); iterator.hasNext();) {
                final String key = (String) iterator.next();
                final String formName = (String) _param._formMap.get(key);
                if (null != formName && !"".equals(formName)) {
                    printMain(db2, svf, formName);
                    _hasData = true;
                }
            }
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

    private void printMain(final DB2UDB db2, final Vrw32alp svf, final String formName) {
        svf.VrSetForm(formName + ".frm", 1);

        svf.VrEndPage();
    }

    private String sql() {
        final StringBuffer stb = new StringBuffer();
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
        private final String _year;
        private final String _semester;
        private final Map _formMap;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _formMap = new TreeMap();
            if ("on".equals(request.getParameter("OUTPUT1"))) {
                _formMap.put("1", request.getParameter("FORM_NAME1"));
            }
            if ("on".equals(request.getParameter("OUTPUT2"))) {
                _formMap.put("2", request.getParameter("FORM_NAME2"));
            }
            if ("on".equals(request.getParameter("OUTPUT3"))) {
                _formMap.put("3", request.getParameter("FORM_NAME3"));
            }
        }

    }
}

// eof

