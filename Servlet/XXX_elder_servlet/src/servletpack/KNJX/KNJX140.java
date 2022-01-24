// kanji=漢字
/*
 * $Id: 1d9318909d78b5f86832a585ccec75b9d1072af8 $
 *
 * 作成日: 2011/02/24 17:01:08 - JST
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
 * @version $Id: 1d9318909d78b5f86832a585ccec75b9d1072af8 $
 */
public class KNJX140 extends AbstractXls {

    private static final Log log = LogFactory.getLog("KNJX140.class");

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
        retList.add("※ＳＥＱ（パターン）");
        retList.add("※タイトル");
        retList.add("※曜日コード");
        retList.add("※校時コード");
        retList.add("※講座コード");
        retList.add("時間割担当（職員コード）");
        return retList;
    }

    protected String[] getCols() {
        final String[] cols = {"YEAR",
                "SEMESTER",
                "BSCSEQ",
                "TITLE",
                "DAYCD",
                "PERIODCD",
                "CHAIRCD",
                "STAFFCD",};
        return cols;
    }

    protected String getSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T2.YEAR, ");
        stb.append("     T2.SEMESTER, ");
        stb.append("     T2.BSCSEQ, ");
        stb.append("     TITLE, ");
        stb.append("     T2.DAYCD, ");
        stb.append("     T2.PERIODCD, ");
        stb.append("     T2.CHAIRCD, ");
        stb.append("     STAFFCD ");
        stb.append(" FROM ");
        stb.append("     SCH_PTRN_HDAT T1, ");
        stb.append("     SCH_PTRN_DAT T2 ");
        stb.append("     LEFT JOIN SCH_PTRN_STF_DAT T3 ON T3.YEAR=T2.YEAR ");
        stb.append("          AND T3.SEMESTER=T2.SEMESTER ");
        stb.append("          AND T3.BSCSEQ=T2.BSCSEQ ");
        stb.append("          AND T3.DAYCD=T2.DAYCD ");
        stb.append("          AND T3.PERIODCD=T2.PERIODCD ");
        stb.append("          AND T3.CHAIRCD=T2.CHAIRCD ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR||T1.SEMESTER = '" + _param._yearSem + "' ");
        stb.append("     AND T1.BSCSEQ = " + _param._seq + " ");
        stb.append("     AND T2.YEAR = T1.YEAR ");
        stb.append("     AND T2.SEMESTER = T1.SEMESTER ");
        stb.append("     AND T2.BSCSEQ = T1.BSCSEQ ");
        stb.append(" ORDER BY ");
        stb.append("     T2.DAYCD, ");
        stb.append("     T2.PERIODCD, ");
        stb.append("     T2.CHAIRCD ");

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
        private final String _seq;
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final boolean _header;
        private final String _templatePath;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _yearSem = request.getParameter("YEAR");
            _seq = request.getParameter("SEQ");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _header = request.getParameter("HEADER") == null ? false : true;
            _templatePath = request.getParameter("TEMPLATE_PATH");
        }

    }
}

// eof
