// kanji=漢字
/*
 * $Id: 2c86e93cb6d47c7167a3ac33d92b5a1ecd2d378c $
 *
 * 作成日: 2011/03/30 22:10:30 - JST
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
 * @author nakamoto
 * @version $Id: 2c86e93cb6d47c7167a3ac33d92b5a1ecd2d378c $
 */
public class KNJX_H110 extends AbstractXls {

    private static final Log log = LogFactory.getLog("KNJX_H110.class");

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
        retList.add("学年");
        retList.add("クラス");
        retList.add("出席番号");
        retList.add("氏名");
        retList.add("※年度");
        retList.add("※学籍番号");
        retList.add("※取得日付");
        retList.add("※科目コード");
        retList.add("科目名称");
        retList.add("※区分コード");
        retList.add("区分名称");
        retList.add("※資格内容コード");
        retList.add("資格内容");
        retList.add("備考");
        retList.add("単位数");
        return retList;
    }

    protected String[] getCols() {
        final String[] cols = {"GRADE",
                "HR_CLASS",
                "ATTENDNO",
                "NAME",
                "YEAR",
                "SCHREGNO",
                "REGDDATE",
                "SUBCLASSCD",
                "SUBCLASSNAME",
                "CONDITION_DIV",
                "CONDITION_DIV_NAME",
                "CONTENTS",
                "CONTENTS_NAME",
                "REMARK",
                "CREDITS",};
        return cols;
    }

    protected String getSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     T2.NAME, ");
        stb.append("     T3.YEAR, ");
        stb.append("     T3.SCHREGNO, ");
        stb.append("     T3.REGDDATE, ");
        stb.append("     T3.SUBCLASSCD, ");
        stb.append("     L1.SUBCLASSNAME, ");
        stb.append("     T3.CONDITION_DIV, ");
        stb.append("     '増加単位認定' AS CONDITION_DIV_NAME, ");
        stb.append("     T3.CONTENTS, ");
        stb.append("     N1.NAME1 AS CONTENTS_NAME, ");
        stb.append("     T3.REMARK, ");
        stb.append("     T3.CREDITS ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT T1 ");
        stb.append("     LEFT JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_QUALIFIED_DAT T3 ");
        stb.append("          ON T3.YEAR = T1.YEAR ");
        stb.append("         AND T3.SCHREGNO = T1.SCHREGNO ");
        stb.append("         AND T3.CONDITION_DIV = '1' ");
        stb.append("     LEFT JOIN SUBCLASS_MST L1 ON L1.SUBCLASSCD = T3.SUBCLASSCD ");
        stb.append("     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'H305' AND N1.NAMECD2 = T3.CONTENTS ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR || T1.SEMESTER = '" + _param._yearSem + "' ");
        stb.append("     AND T3.SCHREGNO = T1.SCHREGNO ");
        if (_param._gradeHrClass != null && !"".equals(_param._gradeHrClass)) {
            stb.append("    AND T1.GRADE || T1.HR_CLASS = '" + _param._gradeHrClass + "' ");
        }
        if (_param._schregNo != null && !"".equals(_param._schregNo)) {
            stb.append("    AND T1.SCHREGNO = '" + _param._schregNo + "' ");
        }
        stb.append(" ORDER BY ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     T3.SCHREGNO, ");
        stb.append("     T3.REGDDATE, ");
        stb.append("     T3.SUBCLASSCD, ");
        stb.append("     T3.SEQ ");
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
        private final String _schregNo;
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final boolean _header;
        private final String _templatePath;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _yearSem = request.getParameter("YEAR");
            _gradeHrClass = request.getParameter("GRADE_HR_CLASS");
            _schregNo = request.getParameter("STUDENT");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _header = request.getParameter("HEADER") == null ? false : true;
            _templatePath = request.getParameter("TEMPLATE_PATH");
        }

    }
}

// eof
