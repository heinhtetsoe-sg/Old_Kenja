// kanji=漢字
/*
 * $Id: 087579208d12da5be4c36da09c53936aced63dc7 $
 *
 * 作成日: 2011/04/26 21:21:08 - JST
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
 * @version $Id: 087579208d12da5be4c36da09c53936aced63dc7 $
 */
public class KNJB151T_XLS extends AbstractXls {

    private static final Log log = LogFactory.getLog("KNJB151T_XLS.class");

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
        retList.add("学期");
        retList.add("年-組-番");
        retList.add("学籍番号");
        retList.add("氏名");
        retList.add("教科書コード");
        retList.add("教科書名");
        retList.add("発行社");
        retList.add("定価");
        retList.add("講座コード");
        retList.add("講座名");
        retList.add("要/不要");
        return retList;
    }

    protected String[] getCols() {
        final String[] cols = {"YEAR",
                "SEMESTER",
                "HR_ATTEND",
                "SCHREGNO",
                "NAME_SHOW",
                "TEXTBOOKCD",
                "TEXTBOOKNAME",
                "ISSUECOMPANYNAME",
                "TEXTBOOKPRICE",
                "CHAIRCD",
                "CHAIRNAME",
                "YOUFUYOU",};
        return cols;
    }

    protected String getSql() {
        final StringBuffer instate = new StringBuffer();
        String sep = "";
        for (int i = 0; i < _param._categorySelected.length; i++) {
            instate.append(sep + "'" + _param._categorySelected[i] + "'");
            sep = ",";
        }
        if (instate.length() == 0) {
            instate.append("''");
        }
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.YEAR, ");
        stb.append("     T1.SEMESTER, ");
        stb.append("     L7.HR_NAME || REPLACE(CAST(SMALLINT(L6.ATTENDNO) AS CHAR(3)), ' ', '') || '番' AS HR_ATTEND, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     L5.NAME_SHOW, ");
        stb.append("     L1.TEXTBOOKCD, ");
        stb.append("     L3.TEXTBOOKNAME, ");
        stb.append("     L4.ISSUECOMPANYNAME, ");
        stb.append("     L3.TEXTBOOKPRICE, ");
        stb.append("     T1.CHAIRCD, ");
        stb.append("     L2.CHAIRNAME, ");
        stb.append("     '' AS YOUFUYOU ");
        stb.append(" FROM ");
        stb.append("     (SELECT DISTINCT ");
        stb.append("         TT1.YEAR, ");
        stb.append("         TT1.SEMESTER, ");
        stb.append("         TT1.CHAIRCD, ");
        stb.append("         TT1.SCHREGNO ");
        stb.append("     FROM ");
        stb.append("         CHAIR_STD_DAT TT1 ");
        stb.append("     WHERE ");
        stb.append("         TT1.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("         AND TT1.SEMESTER = '" + _param._ctrlSemester + "' ");
        stb.append("         AND TT1.SCHREGNO IN (" + instate.toString() + ") ");
        stb.append("     ) T1 ");
        stb.append("     LEFT JOIN CHAIR_TEXTBOOK_DAT L1 ON L1.YEAR = T1.YEAR ");
        stb.append("          AND L1.SEMESTER = T1.SEMESTER ");
        stb.append("          AND L1.CHAIRCD = T1.CHAIRCD ");
        stb.append("     LEFT JOIN CHAIR_DAT L2 ON L2.CHAIRCD = T1.CHAIRCD ");
        stb.append("          AND L2.YEAR = T1.YEAR ");
        stb.append("          AND L2.SEMESTER = T1.SEMESTER ");
        stb.append("     LEFT JOIN TEXTBOOK_MST L3 ON L3.TEXTBOOKCD = L1.TEXTBOOKCD ");
        stb.append("     LEFT JOIN ISSUECOMPANY_MST L4 ON L4.ISSUECOMPANYCD = L3.ISSUECOMPANYCD ");
        stb.append("     LEFT JOIN SCHREG_BASE_MST L5 ON L5.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_REGD_DAT L6 ON L6.SCHREGNO = T1.SCHREGNO ");
        stb.append("          AND L6.YEAR = T1.YEAR ");
        stb.append("          AND L6.SEMESTER = T1.SEMESTER ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT L7 ON L7.YEAR = T1.YEAR ");
        stb.append("          AND L7.SEMESTER = T1.SEMESTER ");
        stb.append("          AND L7.GRADE = L6.GRADE ");
        stb.append("          AND L7.HR_CLASS = L6.HR_CLASS ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param._ctrlSemester + "' ");
        stb.append("     AND T1.SCHREGNO IN (" + instate.toString() + ") ");
        stb.append("     AND L1.YEAR = T1.YEAR ");
        stb.append("     AND L1.SEMESTER = T1.SEMESTER ");
        stb.append("     AND L1.CHAIRCD = T1.CHAIRCD ");
        stb.append(" ORDER BY ");
        stb.append("     L6.GRADE, ");
        stb.append("     L6.HR_CLASS, ");
        stb.append("     L6.ATTENDNO, ");
        stb.append("     L3.TEXTBOOKDIV, ");
        stb.append("     L1.TEXTBOOKCD ");
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
        private final String[] _categorySelected;
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final boolean _header;
        private final String _templatePath;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _categorySelected = request.getParameterValues("category_selected");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _header = true;
            _templatePath = request.getParameter("TEMPLATE_PATH");
        }

    }
}

// eof
