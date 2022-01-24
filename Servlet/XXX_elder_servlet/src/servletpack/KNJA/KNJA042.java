// kanji=漢字
/*
 * $Id: a0f8b5ed2e64a4ddc9299e0d6071b324f78b946e $
 *
 * 作成日: 2011/03/08 15:37:18 - JST
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2011 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJA;

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
 * @version $Id: a0f8b5ed2e64a4ddc9299e0d6071b324f78b946e $
 */
public class KNJA042 extends AbstractXls {

    private static final Log log = LogFactory.getLog("KNJA042.class");

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
        retList.add("学期");
        retList.add("氏名");
        retList.add("氏名かな");
        retList.add("性別");
        retList.add("学年");
        retList.add("組");
        retList.add("出席番号");
        retList.add("課程コード");
        retList.add("学科コード");
        retList.add("コースコード");
        retList.add("留年フラグ");
        retList.add("旧学籍番号");
        retList.add("旧学年");
        retList.add("旧組");
        retList.add("旧出席番号");
        retList.add("成績");
        return retList;
    }

    protected String[] getCols() {
        final String[] cols = {"YEAR",
                "SCHREGNO",
                "SEMESTER",
                "NAME",
                "NAME_KANA",
                "SEX",
                "GRADE",
                "HR_CLASS",
                "ATTENDNO",
                "COURSECD",
                "MAJORCD",
                "COURSECODE",
                "REMAINGRADE_FLG",
                "OLD_SCHREGNO",
                "OLD_GRADE",
                "OLD_HR_CLASS",
                "OLD_ATTENDNO",
                "SCORE",};
        return cols;
    }

    protected String getSql() {
        final StringBuffer stb = new StringBuffer();

        stb.append(" SELECT ");
        stb.append("    t1.YEAR, ");
        stb.append("    t1.SCHREGNO, ");
        stb.append("    t1.SEMESTER, ");
        stb.append("    CASE WHEN t2.NAME is null OR t2.NAME = '' THEN t3.NAME ELSE t2.NAME END AS NAME, ");
        stb.append("    CASE WHEN t2.NAME_KANA is null OR t2.NAME_KANA = '' THEN t3.NAME_KANA ELSE t2.NAME_KANA END AS NAME_KANA, ");
        stb.append("    CASE WHEN t2.SEX is null OR t2.SEX = '' THEN t3.SEX ELSE t2.SEX END AS SEX, ");
        stb.append("    t1.GRADE, ");
        stb.append("    t1.HR_CLASS, ");
        stb.append("    t1.ATTENDNO, ");
        stb.append("    t1.COURSECD, ");
        stb.append("    t1.MAJORCD, ");
        stb.append("    t1.COURSECODE, ");
        stb.append("    t1.REMAINGRADE_FLG, ");
        stb.append("    t1.OLD_SCHREGNO, ");
        stb.append("    t1.OLD_GRADE, ");
        stb.append("    t1.OLD_HR_CLASS, ");
        stb.append("    t1.OLD_ATTENDNO, ");
        stb.append("    t1.SCORE ");
        stb.append(" FROM ");
        stb.append("    CLASS_FORMATION_DAT t1 ");
        stb.append("    LEFT JOIN SCHREG_BASE_MST t2 ON t2.SCHREGNO = t1.SCHREGNO ");
        stb.append("    LEFT JOIN FRESHMAN_DAT t3 ON t3.SCHREGNO = t1.SCHREGNO ");
        stb.append("    AND t3.ENTERYEAR = t1.YEAR ");
        stb.append(" WHERE ");
        stb.append("    t1.YEAR = '" + _param._examYear + "' ");
        stb.append("    AND t1.SEMESTER = '1' ");
        if (_param._grade != null && !"99".equals(_param._grade)){
            stb.append("    AND t1.GRADE = '" + _param._grade + "' ");
        }
        stb.append(" ORDER BY ");
        stb.append("    t1.GRADE,t1.HR_CLASS,t1.ATTENDNO ");

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
        private final String _examYear;
        private final String _grade;
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final boolean _header;
        private final String _templatePath;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _examYear = request.getParameter("XLS_EXAMYEAR");
            _grade = request.getParameter("GRADE");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _header = true;
            _templatePath = request.getParameter("TEMPLATE_PATH");
        }

    }
}

// eof
