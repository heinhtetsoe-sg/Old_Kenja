// kanji=漢字
/*
 * $Id: a9034ed4647d1dddf87f50aab9c9bc74d7ec87ea $
 *
 * 作成日: 2012/03/15 17:38:02 - JST
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2012 ALP Okinawa Co.,Ltd. All rights reserved.
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
 * @author nakamoto
 * @version $Id: a9034ed4647d1dddf87f50aab9c9bc74d7ec87ea $
 */
public class KNJX170A extends AbstractXls {

    private static final Log log = LogFactory.getLog("KNJX170A.class");

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
        retList.add("※※年度");
        retList.add("※群コード");
        retList.add("※群名称");
        retList.add("※群略称");
        retList.add("備考");
        retList.add("表示順");
        return retList;
    }

    protected String[] getCols() {
        final String[] cols = {"YEAR",
                "GROUPCD",
                "GROUPNAME",
                "GROUPABBV",
                "REMARK",
                "SHOWORDER",};
        return cols;
    }

    protected String getSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.YEAR, ");
        stb.append("     T1.GROUPCD, ");
        stb.append("     T1.GROUPNAME, ");
        stb.append("     T1.GROUPABBV, ");
        stb.append("     T1.REMARK, ");
        stb.append("     T1.SHOWORDER ");
        stb.append(" FROM ");
        stb.append("     V_ELECTCLASS_MST T1 ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
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
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final boolean _header;
        private final String _templatePath;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _header = request.getParameter("HEADER") == null ? false : true;
            _templatePath = request.getParameter("TEMPLATE_PATH");
        }

    }
}

// eof
