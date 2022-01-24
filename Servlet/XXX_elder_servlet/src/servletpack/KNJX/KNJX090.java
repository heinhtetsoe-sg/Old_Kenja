// kanji=漢字
/*
 * $Id: a8236051ebba15dfeea681c1325d27778cf87cf6 $
 *
 * 作成日: 2011/02/24 11:32:18 - JST
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
 * @version $Id: a8236051ebba15dfeea681c1325d27778cf87cf6 $
 */
public class KNJX090 extends AbstractXls {

    private static final Log log = LogFactory.getLog("KNJX090.class");

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
        retList.add("学年");
        retList.add("組");
        retList.add("出席番号");
        retList.add("氏名");
        retList.add("※学籍番号");
        retList.add("※適用開始日付");
        retList.add("※適用終了日付");
        retList.add("座席行");
        retList.add("座席列");
        return retList;
    }

    protected String[] getCols() {
        final String[] cols = {"YEAR",
                "SEMESTER",
                "CHAIRCD",
                "GRADE",
                "HR_CLASS",
                "ATTENDNO",
                "NAME",
                "SCHREGNO",
                "APPDATE",
                "APPENDDATE",
                "ROW",
                "COLUMN",};
        return cols;
    }

    protected String getSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.YEAR, ");
        stb.append("     T1.SEMESTER, ");
        stb.append("     T1.CHAIRCD, ");
        stb.append("     L1.GRADE, ");
        stb.append("     L1.HR_CLASS, ");
        stb.append("     L1.ATTENDNO, ");
        stb.append("     L2.NAME, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.APPDATE, ");
        stb.append("     T1.APPENDDATE, ");
        stb.append("     T1.ROW, ");
        stb.append("     T1.COLUMN ");
        stb.append(" FROM ");
        stb.append("     CHAIR_STD_DAT T1 ");
        stb.append(" LEFT JOIN ");
        stb.append("     SCHREG_REGD_DAT L1 ON  L1.YEAR     = T1.YEAR ");
        stb.append("                        AND L1.SEMESTER = T1.SEMESTER ");
        stb.append("                        AND L1.SCHREGNO = T1.SCHREGNO ");
        stb.append(" LEFT JOIN ");
        stb.append("     SCHREG_BASE_MST L2 ON L2.SCHREGNO = T1.SCHREGNO ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR || T1.SEMESTER = '" + _param._yearSem + "' ");
        if (_param._chairCd != null && !"".equals(_param._chairCd)) {
            stb.append("     AND T1.CHAIRCD = '" + _param._chairCd + "' ");
        }
        if (_param._schregNo != null && !"".equals(_param._schregNo)) {
            stb.append("     AND T1.SCHREGNO = '" + _param._schregNo + "' ");
        }
        if (_param._appDate != null && !"".equals(_param._appDate)) {
            stb.append("     AND T1.APPDATE = '" + _param._appDate.replace('/', '-') + "' ");
        }
        stb.append(" ORDER BY ");
        stb.append("     CHAIRCD, ");
        stb.append("     SCHREGNO, ");
        stb.append("     APPDATE ");

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
        private final String _schregNo;
        private final String _appDate;
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final boolean _header;
        private final String _templatePath;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _yearSem = request.getParameter("YEAR");
            _chairCd = request.getParameter("CHAIRCD");
            _schregNo = request.getParameter("SCHREGNO");
            _appDate = request.getParameter("APPDATE");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _header = request.getParameter("HEADER") == null ? false : true;
            _templatePath = request.getParameter("TEMPLATE_PATH");
        }

    }
}

// eof
