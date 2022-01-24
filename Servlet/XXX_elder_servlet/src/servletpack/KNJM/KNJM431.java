// kanji=漢字
/*
 * $Id: 65a39c7293e71c5b238d3c1f9b92f219efd41aca $
 *
 * 作成日: 2011/03/23 13:36:20 - JST
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
 * @version $Id: 65a39c7293e71c5b238d3c1f9b92f219efd41aca $
 */
public class KNJM431 extends AbstractXls {

    private static final Log log = LogFactory.getLog("KNJM431.class");

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
        retList.add("※学籍番号");
        retList.add("氏名");
        retList.add("クラス");
        retList.add("番号");
        if ("1".equals(_param._useCurriculumcd)) {
            retList.add("※教科コード");
            retList.add("※校種");
            retList.add("※教育課程");
        }
        retList.add("※科目コード");
        retList.add("科目名");
        retList.add("前期素点");
        retList.add("前期評価");
        retList.add("認定素点");
        retList.add("認定評価");
        retList.add("学年評定");
        return retList;
    }

    protected String[] getCols() {
        if ("1".equals(_param._useCurriculumcd)) {
            final String[] cols = {"YEAR",
                    "SCHREGNO",
                    "NAME",
                    "HR_NAME",
                    "ATTENDNO",
                    "CLASSCD",
                    "SCHOOL_KIND",
                    "CURRICULUM_CD",
                    "SUBCLASSCD",
                    "SUBCLASSNAME",
                    "SEM1_TERM_SCORE",
                    "SEM1_VALUE",
                    "SEM2_TERM_SCORE",
                    "SEM2_VALUE",
                    "GRAD_VALUE",};
            return cols;
        } else {
            final String[] cols = {"YEAR",
                    "SCHREGNO",
                    "NAME",
                    "HR_NAME",
                    "ATTENDNO",
                    "SUBCLASSCD",
                    "SUBCLASSNAME",
                    "SEM1_TERM_SCORE",
                    "SEM1_VALUE",
                    "SEM2_TERM_SCORE",
                    "SEM2_VALUE",
                    "GRAD_VALUE",};
            return cols;
        }
    }

    protected String getSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.YEAR, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T2.NAME, ");
        stb.append("     T4.HR_NAME, ");
        stb.append("     T3.ATTENDNO, ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     T1.CLASSCD, ");
            stb.append("     T1.SCHOOL_KIND, ");
            stb.append("     T1.CURRICULUM_CD, ");
        }
        stb.append("     T1.SUBCLASSCD, ");
        stb.append("     T5.SUBCLASSNAME, ");
        stb.append("     T1.SEM1_TERM_SCORE, ");
        stb.append("     T1.SEM1_VALUE, ");
        stb.append("     T1.SEM2_TERM_SCORE, ");
        stb.append("     T1.SEM2_VALUE, ");
        stb.append("     T1.GRAD_VALUE ");
        stb.append(" FROM ");
        stb.append("     RECORD_DAT T1 ");
        stb.append("     LEFT JOIN SCHREG_BASE_MST T2 ON T1.SCHREGNO = T2.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_REGD_DAT T3 ON T1.SCHREGNO = T3.SCHREGNO ");
        stb.append("          AND T1.YEAR = T3.YEAR ");
        stb.append("          AND T3.SEMESTER = '" + _param._ctrlSemester + "' ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT T4 ON T1.YEAR = T4.YEAR ");
        stb.append("          AND T3.SEMESTER = T4.SEMESTER ");
        stb.append("          AND T3.GRADE || T3.HR_CLASS = T4.GRADE || T4.HR_CLASS ");
        stb.append("     LEFT JOIN SUBCLASS_MST T5 ON T1.SUBCLASSCD = T5.SUBCLASSCD ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("          AND T1.CLASSCD = T5.CLASSCD ");
            stb.append("          AND T1.SCHOOL_KIND = T5.SCHOOL_KIND ");
            stb.append("          AND T1.CURRICULUM_CD = T5.CURRICULUM_CD ");
        }
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._ctrlYear + "' ");
        stb.append("     AND T1.TAKESEMES = '0' ");
        if (_param._subclassCd != null && !"".equals(_param._subclassCd)) {
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("     AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '" + _param._subclassCd + "' ");
            } else {
                stb.append("     AND T1.SUBCLASSCD = '" + _param._subclassCd + "' ");
            }
        }
        stb.append(" ORDER BY ");
        stb.append("     T1.SCHREGNO ");
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
        private final String _subclassCd;
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final boolean _header;
        private final String _templatePath;
        private final String _useCurriculumcd;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _subclassCd = request.getParameter("SUBCLASSCD");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _header = request.getParameter("HEADER") == null ? false : true;
            _templatePath = request.getParameter("TEMPLATE_PATH");
            _useCurriculumcd = request.getParameter("useCurriculumcd");
        }

    }
}

// eof
