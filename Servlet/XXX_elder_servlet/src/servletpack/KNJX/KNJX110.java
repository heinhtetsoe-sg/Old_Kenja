// kanji=漢字
/*
 * $Id: ceeba8ba924b016137028b3f22cf959165c5d196 $
 *
 * 作成日: 2011/02/24 16:09:23 - JST
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
 * @version $Id: ceeba8ba924b016137028b3f22cf959165c5d196 $
 */
public class KNJX110 extends AbstractXls {

    private static final Log log = LogFactory.getLog("KNJX110.class");

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
        retList.add("※実施日付");
        retList.add("※校時コード");
        retList.add("※講座コード");
        retList.add("※施設コード");
        return retList;
    }

    protected String[] getCols() {
        final String[] cols = {"EXECUTEDATE",
                "PERIODCD",
                "CHAIRCD",
                "FACCD",};
        return cols;
    }

    protected String getSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     EXECUTEDATE, ");
        stb.append("     PERIODCD, ");
        stb.append("     CHAIRCD, ");
        stb.append("     FACCD ");
        stb.append(" FROM ");
        stb.append("     SCH_FAC_DAT ");
        stb.append(" WHERE ");
        stb.append("     PERIODCD IS NOT NULL ");
        if (_param._executeDate != null && !"".equals(_param._executeDate)) {
            stb.append("     AND EXECUTEDATE = '" + _param._executeDate + "' ");
        }
        if (_param._chairCd != null && !"".equals(_param._chairCd)) {
            stb.append("     AND CHAIRCD = '" + _param._chairCd + "' ");
        }
        if (_param._facCd != null && !"".equals(_param._facCd)) {
            stb.append("     AND FACCD = '" + _param._facCd + "' ");
        }
        stb.append(" ORDER BY ");
        stb.append("     EXECUTEDATE, ");
        stb.append("     PERIODCD, ");
        stb.append("     CHAIRCD, ");
        stb.append("     FACCD ");
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
        private final String _executeDate;
        private final String _chairCd;
        private final String _facCd;
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final boolean _header;
        private final String _templatePath;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _executeDate = request.getParameter("EXECUTEDATE");
            _chairCd = request.getParameter("CHAIRCD");
            _facCd = request.getParameter("FACCD");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _header = request.getParameter("HEADER") == null ? false : true;
            _templatePath = request.getParameter("TEMPLATE_PATH");
        }

    }
}

// eof
