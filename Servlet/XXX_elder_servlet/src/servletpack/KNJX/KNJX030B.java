// kanji=漢字
/*
 * $Id: 21f35814b8b4c06a66840f768f14d763c487c288 $
 *
 * 作成日: 2011/02/21 12:01:04 - JST
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
 * @version $Id: 21f35814b8b4c06a66840f768f14d763c487c288 $
 */
public class KNJX030B extends AbstractXls {

    private static final Log log = LogFactory.getLog("KNJX030B.class");

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
        retList.add("※有効期間開始年月日");
        retList.add("有効期間終了年月日");
        retList.add("郵便番号");
        retList.add("地区コード");
        retList.add("※住所1");
        retList.add("住所2");
        retList.add("住所1英字");
        retList.add("住所2英字");
        retList.add("電話番号");
        retList.add("FAX番号");
        retList.add("E-Mail");
        return retList;
    }

    protected String[] getCols() {
        final String[] cols = {"SCHREGNO",
                "ISSUEDATE",
                "EXPIREDATE",
                "ZIPCD",
                "AREACD",
                "ADDR1",
                "ADDR2",
                "ADDR1_ENG",
                "ADDR2_ENG",
                "TELNO",
                "FAXNO",
                "EMAIL",};
        return cols;
    }

    protected String getSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.ISSUEDATE, ");
        stb.append("     T1.EXPIREDATE, ");
        stb.append("     T1.ZIPCD, ");
        stb.append("     T1.AREACD, ");
        stb.append("     T1.ADDR1, ");
        stb.append("     T1.ADDR2, ");
        stb.append("     T1.ADDR1_ENG, ");
        stb.append("     T1.ADDR2_ENG, ");
        stb.append("     T1.TELNO, ");
        stb.append("     T1.FAXNO, ");
        stb.append("     T1.EMAIL ");
        stb.append(" FROM ");
        stb.append("     SCHREG_ADDRESS_DAT T1, ");
        stb.append("     SCHREG_REGD_DAT T2 ");
        stb.append("     LEFT JOIN SCHREG_BASE_MST T3 ON T2.SCHREGNO = T3.SCHREGNO ");
        stb.append(" WHERE ");
        stb.append("     T1.SCHREGNO = T2.SCHREGNO AND ");
        stb.append("     T2.YEAR || T2.SEMESTER = '" + _param._yearSem + "' ");
        if(_param._gradeHrClass != null && !"".equals(_param._gradeHrClass)){
            stb.append("     AND T2.GRADE || T2.HR_CLASS = '" + _param._gradeHrClass + "' ");
        }
        stb.append(" ORDER BY ");
        stb.append("     T2.GRADE, ");
        stb.append("     T2.HR_CLASS, ");
        stb.append("     T2.ATTENDNO, ");
        stb.append("     T1.ISSUEDATE ");
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
        private final String _gradeHrClass;
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final boolean _header;
        private final String _templatePath;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _gradeHrClass = request.getParameter("GRADE_HR_CLASS");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _yearSem = _ctrlYear + _ctrlSemester;
            _ctrlDate = request.getParameter("CTRL_DATE");
            _header = request.getParameter("HEADER") == null ? false : true;
            _templatePath = request.getParameter("TEMPLATE_PATH");
        }

    }
}

// eof
