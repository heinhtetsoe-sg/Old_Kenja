// kanji=漢字
/*
 * $Id: a661360f35931f9fd509b986468c1bff95790572 $
 *
 * 作成日: 2011/04/25 14:49:07 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2011 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJM;

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
 * @version $Id: a661360f35931f9fd509b986468c1bff95790572 $
 */
public class KNJM030 extends AbstractXls {

    private static final Log log = LogFactory.getLog("KNJM030.class");

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
        retList.add("年度");
        retList.add("学籍番号");
        retList.add("実施日付");
        retList.add("講座コード");
        retList.add("講座名");
        retList.add("校時コード");
        retList.add("スクーリング回数");
        retList.add("受付日付");
        retList.add("受付時間");
        retList.add("担当者職員コード");
        retList.add("スクーリング種別");
        retList.add("備考");
        return retList;
    }

    protected String[] getCols() {
        final String[] cols = {"YEAR",
                "SCHREGNO",
                "EXECUTEDATE",
                "CHAIRCD",
                "CHAIRNAME",
                "PERIODCD",
                "SCHOOLING_SEQ",
                "RECEIPT_DATE",
                "RECEIPT_TIME",
                "STAFFCD",
                "SCHOOLINGKINDCD",
                "REMARK",};
        return cols;
    }

    protected String getSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     t1.YEAR, ");
        stb.append("     t1.SCHREGNO, ");
        stb.append("     t1.EXECUTEDATE, ");
        stb.append("     t1.CHAIRCD, ");
        stb.append("     t2.CHAIRNAME, ");
        stb.append("     t1.PERIODCD, ");
        stb.append("     t1.SCHOOLING_SEQ, ");
        stb.append("     t1.RECEIPT_DATE, ");
        stb.append("     t1.RECEIPT_TIME, ");
        stb.append("     t1.STAFFCD, ");
        stb.append("     t1.SCHOOLINGKINDCD, ");
        stb.append("     t1.REMARK ");
        stb.append(" FROM ");
        stb.append("     SCH_ATTEND_DAT t1 ");
        stb.append("     LEFT JOIN CHAIR_DAT t2 ON t1.CHAIRCD = t2.CHAIRCD ");
        stb.append("     AND t2.YEAR = '" + _param._year + "' ");
        stb.append("     AND t2.SEMESTER = '" + _param._ctrlSemester + "' ");
        stb.append(" WHERE ");
        stb.append("     t1.YEAR = '" + _param._year + "' ");
        if (!"0".equals(_param._subclassName)){
            stb.append("     AND t1.CHAIRCD = '" + _param._subclassName + "' ");
        }
        if (!"0".equals(_param._schoolingkindCd)){
            stb.append("     AND t1.SCHOOLINGKINDCD = '" + _param._schoolingkindCd + "' ");
        }
        stb.append(" ORDER BY t1.CHAIRCD,t1.SCHOOLINGKINDCD,t1.SCHREGNO,t1.PERIODCD,t1.SCHOOLING_SEQ ");
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
        private final String _subclassName;
        private final String _schoolingkindCd;
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final boolean _header;
        private final String _templatePath;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _subclassName = request.getParameter("SUBCLASSNAME");
            _schoolingkindCd = request.getParameter("SCHOOLINGKINDCD");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _header = request.getParameter("HEADER") == null ? false : true;
            _templatePath = request.getParameter("TEMPLATE_PATH");
        }

    }
}

// eof
