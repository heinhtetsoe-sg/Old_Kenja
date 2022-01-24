// kanji=漢字
/*
 * $Id: a080811cc5082b7dd732142e1778b49b4c75dd95 $
 *
 * 作成日: 2011/02/14 10:43:51 - JST
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
 * @version $Id: a080811cc5082b7dd732142e1778b49b4c75dd95 $
 */
public class KNJX010 extends AbstractXls {

    private static final Log log = LogFactory.getLog("KNJX010.class");

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
        retList.add("※職員コード");
        retList.add("※職員氏名");
        retList.add("職員氏名表示用");
        retList.add("職員氏名かな");
        retList.add("職員氏名英字");
        retList.add("戸籍氏名");
        retList.add("戸籍氏名かな");
        retList.add("職名コード");
        retList.add("所属コード");
        retList.add("校務分掌部コード");
        retList.add("授業受持区分");
        retList.add("職員性別");
        retList.add("職員生年月日");
        retList.add("職員郵便番号");
        retList.add("職員住所1");
        retList.add("職員住所2");
        retList.add("職員電話番号");
        retList.add("職員FAX番号");
        retList.add("職員メールアドレス");
        return retList;
    }

    protected String[] getCols() {
        final String[] cols = {"STAFFCD",
                "STAFFNAME",
                "STAFFNAME_SHOW",
                "STAFFNAME_KANA",
                "STAFFNAME_ENG",
                "STAFFNAME_REAL",
                "STAFFNAME_KANA_REAL",
                "JOBCD",
                "SECTIONCD",
                "DUTYSHARECD",
                "CHARGECLASSCD",
                "STAFFSEX",
                "STAFFBIRTHDAY",
                "STAFFZIPCD",
                "STAFFADDR1",
                "STAFFADDR2",
                "STAFFTELNO",
                "STAFFFAXNO",
                "STAFFE_MAIL",};
        return cols;
    }

    protected String getSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     * ");
        stb.append(" FROM ");
        stb.append("     STAFF_MST ");
        stb.append(" ORDER BY ");
        stb.append("     STAFFCD ");
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
