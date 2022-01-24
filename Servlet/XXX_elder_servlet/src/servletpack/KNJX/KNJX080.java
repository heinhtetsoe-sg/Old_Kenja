// kanji=漢字
/*
 * $Id: 1875232f88080ed869897b576205094bcd930926 $
 *
 * 作成日: 2011/02/23 18:04:43 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2011 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJX;

import java.util.ArrayList;
import java.util.List;

import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.AbstractXls;
import servletpack.KNJZ.detail.KNJServletUtils;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: 1875232f88080ed869897b576205094bcd930926 $
 */
public class KNJX080 extends AbstractXls {

    private static final Log log = LogFactory.getLog("KNJX080.class");

    private boolean _hasData;

    Param _param;

    /**
     * @param request リクエスト
     * @param response レスポンス
     */
    public void xls_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {

        _param = createParam(_db2, request);

        //テンプレートの読込&初期化
        setIniTmpBook(_param._templatePath);

        //ヘッダデータ取得
        _headList = getHeadData();

        //出力データ取得
        _dataList = getXlsDataList();

        outPutXls(response, _param._header);
    }

    protected List getHeadData() {
        final List retList = new ArrayList();
        retList.add("※年度");
        retList.add("※学期");
        retList.add("※講座コード");
        retList.add("※群コード");
        retList.add("※対象年");
        retList.add("※対象組");
        return retList;
    }

    protected String[] getCols() {
        final String[] cols = {"YEAR",
                "SEMESTER",
                "CHAIRCD",
                "GROUPCD",
                "TRGTGRADE",
                "TRGTCLASS",};
        return cols;
    }

    protected String getSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     * ");
        stb.append(" FROM ");
        stb.append("     CHAIR_CLS_DAT ");
        stb.append(" WHERE ");
        stb.append("     YEAR || SEMESTER = '" + _param._yearSem + "' ");
        if (_param._chairCd != null && !"".equals(_param._chairCd)) {
            stb.append("     AND CHAIRCD = '" + _param._chairCd + "' ");
        }
        if (_param._trgtCd != null && !"".equals(_param._trgtCd)) {
            stb.append("     AND TRGTGRADE || TRGTCLASS = '" + _param._trgtCd + "' ");
        }
        stb.append(" ORDER BY ");
        stb.append("     CHAIRCD, ");
        stb.append("     TRGTGRADE, ");
        stb.append("     TRGTCLASS ");
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
        private final String _yearSem;
        private final String _chairCd;
        private final String _trgtCd;
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final boolean _header;
        private final String _templatePath;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _yearSem = request.getParameter("YEAR");
            _chairCd = request.getParameter("CHAIRCD");
            _trgtCd = request.getParameter("TRGTCD");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _header = request.getParameter("HEADER") == null ? false : true;
            _templatePath = request.getParameter("TEMPLATE_PATH");
        }

    }
}

// eof
