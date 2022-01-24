// kanji=漢字
/*
 * $Id: 80f833de3284abbd40100e94f5af6887bcb9206a $
 *
 * 作成日: 2011/04/15 2:53:17 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2011 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJB;

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
 * @version $Id: 80f833de3284abbd40100e94f5af6887bcb9206a $
 */
public class KNJB1602 extends AbstractXls {

    private static final Log log = LogFactory.getLog("KNJB1602.class");

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
        retList.add("※学籍番号");
        retList.add("※年度");
        retList.add("※学期");
        retList.add("※講座コード");
        retList.add("※教科書コード");
        return retList;
    }

    protected String[] getCols() {
        final String[] cols = {"SCHREGNO",
                "YEAR",
                "SEMESTER",
                "CHAIRCD",
                "TEXTBOOKCD",};
        return cols;
    }

    protected String getSql() {
        final StringBuffer stb = new StringBuffer();
        if ("4".equals(_param._outPut)) {
            stb.append(" SELECT ");
            stb.append("     SCHREGNO, ");
            stb.append("     YEAR, ");
            stb.append("     SEMESTER, ");
            stb.append("     CHAIRCD, ");
            stb.append("     TEXTBOOKCD ");
            stb.append(" FROM ");
            stb.append("     SCHREG_TEXTBOOK_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR || SEMESTER = '" + _param._yearSem + "' ");
            if (_param._chairCd != null && !"".equals(_param._chairCd)) {
                stb.append("     AND CHAIRCD = '" + _param._chairCd + "' ");
            }
            stb.append(" ORDER BY ");
            stb.append("     CHAIRCD, ");
            stb.append("     SCHREGNO, ");
            stb.append("     TEXTBOOKCD ");
        } else {
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.YEAR, ");
            stb.append("     T1.SEMESTER, ");
            stb.append("     T1.CHAIRCD, ");
            stb.append("     T1.TEXTBOOKCD ");
            stb.append(" FROM ");
            stb.append("     SCHREG_TEXTBOOK_DAT T1, ");
            stb.append("     SCHREG_REGD_DAT T2 ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR || T1.SEMESTER = '" + _param._yearSem2 + "' ");
            stb.append("     AND T1.YEAR = T2.YEAR ");
            stb.append("     AND T1.SEMESTER = T2.SEMESTER ");
            stb.append("     AND T2.GRADE || T2.HR_CLASS = '" + _param._gradeHrClass + "' ");
            stb.append("     AND T1.SCHREGNO = T2.SCHREGNO ");
            if (_param._student != null && !"".equals(_param._student)) {
                stb.append("     AND T1.SCHREGNO = '" + _param._student + "' ");
            }
            stb.append(" ORDER BY ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.CHAIRCD, ");
            stb.append("     T1.TEXTBOOKCD ");
        }

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
        private final String _outPut;
        private final String _yearSem;
        private final String _chairCd;
        private final String _yearSem2;
        private final String _gradeHrClass;
        private final String _student;
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final boolean _header;
        private final String _templatePath;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _outPut = request.getParameter("OUTPUT");
            _yearSem = request.getParameter("YEAR");
            _chairCd = request.getParameter("CHAIRCD");
            _yearSem2 = request.getParameter("YEAR2");
            _gradeHrClass = request.getParameter("GRADE_HR_CLASS");
            _student = request.getParameter("STUDENT");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _header = request.getParameter("HEADER") == null ? false : true;
            _templatePath = request.getParameter("TEMPLATE_PATH");
        }

    }
}

// eof
