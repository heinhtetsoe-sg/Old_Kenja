// kanji=漢字
/*
 * $Id: ea722b3c07d72d2bb9d70807ed8939f9c84b59f8 $
 *
 * 作成日: 2011/04/12 15:34:38 - JST
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2011 ALP Okinawa Co.,Ltd. All rights reserved.
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
 * @version $Id: ea722b3c07d72d2bb9d70807ed8939f9c84b59f8 $
 */
public class KNJX100O extends AbstractXls {

    private static final Log log = LogFactory.getLog("KNJX100O.class");

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
        retList.add("出欠確認者コード");
        return retList;
    }

    protected String[] getCols() {
        final String[] cols = {"EXECUTEDATE",
                "PERIODCD",
                "CHAIRCD",
                "ATTESTOR",};
        return cols;
    }

    protected String getSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.EXECUTEDATE, ");
        stb.append("     T1.PERIODCD, ");
        stb.append("     T1.CHAIRCD, ");
        stb.append("     T1.ATTESTOR ");
        stb.append(" FROM ");
        stb.append("     SCH_CHR_DAT T1 ");
        stb.append("     INNER JOIN SEMESTER_MST L1 ON L1.YEAR = T1.YEAR AND L1.SEMESTER = T1.SEMESTER ");
        stb.append("     INNER JOIN CHAIR_DAT L2 ON L2.YEAR = T1.YEAR AND L2.SEMESTER = T1.SEMESTER AND L2.CHAIRCD = T1.CHAIRCD ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR || '-' || T1.SEMESTER = '" + _param._yearSem + "' ");
        if (_param._executeDate != null && !"".equals(_param._executeDate)) {
            stb.append("     AND T1.EXECUTEDATE = '" + _param._executeDate + "' ");
        }
        if (_param._chairCd != null && !"".equals(_param._chairCd)) {
            stb.append("     AND T1.CHAIRCD = '" + _param._chairCd + "' ");
        }
        stb.append(" ORDER BY ");
        stb.append("     T1.EXECUTEDATE, ");
        stb.append("     T1.PERIODCD, ");
        stb.append("     T1.CHAIRCD ");
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
        private final String _executeDate;
        private final String _chairCd;
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final boolean _header;
        private final String _templatePath;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _yearSem = request.getParameter("YEAR_SEME");
            _executeDate = request.getParameter("EXECUTEDATE");
            _chairCd = request.getParameter("CHAIRCD");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _header = request.getParameter("HEADER") == null ? false : true;
            _templatePath = request.getParameter("TEMPLATE_PATH");
        }

    }
}

// eof
