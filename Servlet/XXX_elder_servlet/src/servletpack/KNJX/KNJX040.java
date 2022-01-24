// kanji=漢字
/*
 * $Id: c6790abf8f9f08a89b0131ce5448c0f0f4a4a94a $
 *
 * 作成日: 2011/02/21 18:40:27 - JST
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
 * @version $Id: c6790abf8f9f08a89b0131ce5448c0f0f4a4a94a $
 */
public class KNJX040 extends AbstractXls {

    private static final Log log = LogFactory.getLog("KNJX040.class");

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
        if ("1".equals(_param._useSchregRegdHdat)) {
            retList.add("※年度");
            retList.add("※学期");
            retList.add("※学年");
            retList.add("※組");
            retList.add("※年組名称");
            retList.add("※年組略称");
            retList.add("年名称");
            retList.add("組名称1");
            retList.add("組名称2");
            retList.add("HR施設コード");
            retList.add("※担任コード1");
            retList.add("担任コード2");
            retList.add("担任コード3");
            retList.add("副担任コード1");
            retList.add("副担任コード2");
            retList.add("副担任コード3");
        } else {
            retList.add("※年度");
            retList.add("※学期");
            retList.add("※学年");
            retList.add("※組");
            retList.add("※組名称");
            retList.add("※組略称");
            retList.add("HR施設コード");
            retList.add("※担任コード1");
            retList.add("担任コード2");
            retList.add("担任コード3");
            retList.add("副担任コード1");
            retList.add("副担任コード2");
            retList.add("副担任コード3");
        }
        return retList;
    }

    protected String[] getCols() {
        final String[] cols = {"YEAR",
                "SEMESTER",
                "GRADE",
                "HR_CLASS",
                "HR_NAME",
                "HR_NAMEABBV",
                "GRADE_NAME",
                "HR_CLASS_NAME1",
                "HR_CLASS_NAME2",
                "HR_FACCD",
                "TR_CD1",
                "TR_CD2",
                "TR_CD3",
                "SUBTR_CD1",
                "SUBTR_CD2",
                "SUBTR_CD3",};
        final String[] cols2 = {"YEAR",
                "SEMESTER",
                "GRADE",
                "HR_CLASS",
                "HR_NAME",
                "HR_NAMEABBV",
                "HR_FACCD",
                "TR_CD1",
                "TR_CD2",
                "TR_CD3",
                "SUBTR_CD1",
                "SUBTR_CD2",
                "SUBTR_CD3",};
        return "1".equals(_param._useSchregRegdHdat) ? cols : cols2;
    }

    protected String getSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     YEAR, ");
        stb.append("     SEMESTER, ");
        stb.append("     GRADE, ");
        stb.append("     HR_CLASS, ");
        stb.append("     HR_NAME, ");
        stb.append("     HR_NAMEABBV, ");
        if ("1".equals(_param._useSchregRegdHdat)) {
            stb.append(" GRADE_NAME, ");
            stb.append(" HR_CLASS_NAME1, ");
            stb.append(" HR_CLASS_NAME2, ");
        }
        stb.append("     HR_FACCD, ");
        stb.append("     TR_CD1, ");
        stb.append("     TR_CD2, ");
        stb.append("     TR_CD3, ");
        stb.append("     SUBTR_CD1, ");
        stb.append("     SUBTR_CD2, ");
        stb.append("     SUBTR_CD3 ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_HDAT ");
        stb.append(" WHERE ");
        stb.append("     YEAR || SEMESTER = '" + _param._yearSem + "' ");
        stb.append(" ORDER BY ");
        stb.append("     GRADE,HR_CLASS");
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
        private final String _useSchregRegdHdat;
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final boolean _header;
        private final String _templatePath;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _yearSem = request.getParameter("YEAR");
            _useSchregRegdHdat = request.getParameter("useSchregRegdHdat");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _header = request.getParameter("HEADER") == null ? false : true;
            _templatePath = request.getParameter("TEMPLATE_PATH");
        }

    }
}

// eof
