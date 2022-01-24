// kanji=漢字
/*
 * $Id: cea6a0e848df703b95ae10c4d37104ad48766c0c $
 *
 * 作成日: 2011/08/19 14:17:45 - JST
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
 * @version $Id: cea6a0e848df703b95ae10c4d37104ad48766c0c $
 */
public class KNJA070 extends AbstractXls {

    private static final Log log = LogFactory.getLog("KNJA070.class");

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
        retList.add("※学年");
        retList.add("※組");
        retList.add("年組名称");
        retList.add("年組略称");
        retList.add("組名称１");
        retList.add("組名称２");
        retList.add("担当区分1");
        retList.add("担任コード1");
        retList.add("担当開始日");
        retList.add("担当終了日");
        retList.add("担当区分2");
        retList.add("担任コード2");
        retList.add("担当開始日");
        retList.add("担当終了日");
        retList.add("担当区分3");
        retList.add("担任コード3");
        retList.add("担当開始日");
        retList.add("担当終了日");
        retList.add("担当区分4");
        retList.add("副担任コード1");
        retList.add("担当開始日");
        retList.add("担当終了日");
        retList.add("担当区分5");
        retList.add("副担任コード2");
        retList.add("担当開始日");
        retList.add("担当終了日");
        retList.add("担当区分6");
        retList.add("副担任コード3");
        retList.add("担当開始日");
        retList.add("担当終了日");
        retList.add("HR施設コード");
        retList.add("学期授業週数");
        retList.add("学期授業日数");
        return retList;
    }

    protected String[] getCols() {
        final String[] cols = {"YEAR",
                "SEMESTER",
                "GRADE",
                "HR_CLASS",
                "HR_NAME",
                "HR_NAMEABBV",
                "HR_CLASS_NAME1",
                "HR_CLASS_NAME2",
                "TR_DIV1",
                "TR_CD1",
                "TR_FROM_DATE1",
                "TR_TO_DATE1",
                "TR_DIV2",
                "TR_CD2",
                "TR_FROM_DATE2",
                "TR_TO_DATE2",
                "TR_DIV3",
                "TR_CD3",
                "TR_FROM_DATE3",
                "TR_TO_DATE3",
                "SUBTR_DIV1",
                "SUBTR_CD1",
                "SUBTR_FROM_DATE1",
                "SUBTR_TO_DATE1",
                "SUBTR_DIV2",
                "SUBTR_CD2",
                "SUBTR_FROM_DATE2",
                "SUBTR_TO_DATE2",
                "SUBTR_DIV3",
                "SUBTR_CD3",
                "SUBTR_FROM_DATE3",
                "SUBTR_TO_DATE3",
                "HR_FACCD",
                "CLASSWEEKS",
                "CLASSDAYS",};
        return cols;
    }

    protected String getSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     SRH.YEAR, ");
        stb.append("     SRH.SEMESTER, ");
        stb.append("     SRH.GRADE, ");
        stb.append("     SRH.HR_CLASS, ");
        stb.append("     SRH.HR_NAME, ");
        stb.append("     SRH.HR_NAMEABBV, ");
        stb.append("     SRH.HR_CLASS_NAME1, ");
        stb.append("     SRH.HR_CLASS_NAME2, ");
        stb.append("     CASE WHEN SRH.TR_CD1 is not NULL THEN '1' ELSE NULL END AS TR_DIV1, ");
        stb.append("     SRH.TR_CD1, ");
        stb.append("     MAX(T1.FROM_DATE) AS TR_FROM_DATE1, ");
        stb.append("     MAX(T1.TO_DATE) AS TR_TO_DATE1, ");
        stb.append("     CASE WHEN SRH.TR_CD2 is not NULL THEN '2' ELSE NULL END AS TR_DIV2, ");
        stb.append("     SRH.TR_CD2, ");
        stb.append("     MAX(T2.FROM_DATE) AS TR_FROM_DATE2, ");
        stb.append("     MAX(T2.TO_DATE) AS TR_TO_DATE2, ");
        stb.append("     CASE WHEN SRH.TR_CD3 is not NULL THEN '3' ELSE NULL END AS TR_DIV3, ");
        stb.append("     SRH.TR_CD3, ");
        stb.append("     MAX(T3.FROM_DATE) AS TR_FROM_DATE3, ");
        stb.append("     MAX(T3.TO_DATE) AS TR_TO_DATE3, ");
        stb.append("     CASE WHEN SRH.SUBTR_CD1 is not NULL THEN '4' ELSE NULL END AS SUBTR_DIV1, ");
        stb.append("     SRH.SUBTR_CD1, ");
        stb.append("     MAX(S1.FROM_DATE) AS SUBTR_FROM_DATE1, ");
        stb.append("     MAX(S1.TO_DATE) AS SUBTR_TO_DATE1, ");
        stb.append("     CASE WHEN SRH.SUBTR_CD2 is not NULL THEN '5' ELSE NULL END AS SUBTR_DIV2, ");
        stb.append("     SRH.SUBTR_CD2, ");
        stb.append("     MAX(S2.FROM_DATE) AS SUBTR_FROM_DATE2, ");
        stb.append("     MAX(S2.TO_DATE) AS SUBTR_TO_DATE2, ");
        stb.append("     CASE WHEN SRH.SUBTR_CD3 is not NULL THEN '6' ELSE NULL END AS SUBTR_DIV3, ");
        stb.append("     SRH.SUBTR_CD3, ");
        stb.append("     MAX(S3.FROM_DATE) AS SUBTR_FROM_DATE3, ");
        stb.append("     MAX(S3.TO_DATE) AS SUBTR_TO_DATE3, ");
        stb.append("     SRH.HR_FACCD, ");
        stb.append("     SRH.CLASSWEEKS, ");
        stb.append("     SRH.CLASSDAYS, ");
        stb.append("     'DUMMY' AS DUMMY ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_HDAT SRH ");
        stb.append(" LEFT OUTER JOIN STAFF_CLASS_HIST_DAT T1 ON SRH.TR_CD1 = T1.STAFFCD AND SRH.YEAR = T1.YEAR AND SRH.SEMESTER = T1.SEMESTER AND SRH.GRADE = T1.GRADE AND SRH.HR_CLASS = T1.HR_CLASS AND T1.TR_DIV = '1'"); 
        stb.append(" LEFT OUTER JOIN STAFF_CLASS_HIST_DAT T2 ON SRH.TR_CD2 = T2.STAFFCD AND SRH.YEAR = T2.YEAR AND SRH.SEMESTER = T2.SEMESTER AND SRH.GRADE = T2.GRADE AND SRH.HR_CLASS = T2.HR_CLASS AND T2.TR_DIV = '2'");
        stb.append(" LEFT OUTER JOIN STAFF_CLASS_HIST_DAT T3 ON SRH.TR_CD3 = T3.STAFFCD AND SRH.YEAR = T3.YEAR AND SRH.SEMESTER = T3.SEMESTER AND SRH.GRADE = T3.GRADE AND SRH.HR_CLASS = T3.HR_CLASS AND T3.TR_DIV = '3'");
        stb.append(" LEFT OUTER JOIN STAFF_CLASS_HIST_DAT S1 ON SRH.SUBTR_CD1 = S1.STAFFCD AND SRH.YEAR = S1.YEAR AND SRH.SEMESTER = S1.SEMESTER AND SRH.GRADE = S1.GRADE AND SRH.HR_CLASS = S1.HR_CLASS AND S1.TR_DIV = '4'");
        stb.append(" LEFT OUTER JOIN STAFF_CLASS_HIST_DAT S2 ON SRH.SUBTR_CD2 = S2.STAFFCD AND SRH.YEAR = S2.YEAR AND SRH.SEMESTER = S2.SEMESTER AND SRH.GRADE = S2.GRADE AND SRH.HR_CLASS = S2.HR_CLASS AND S2.TR_DIV = '5'");
        stb.append(" LEFT OUTER JOIN STAFF_CLASS_HIST_DAT S3 ON SRH.SUBTR_CD3 = S3.STAFFCD AND SRH.YEAR = S3.YEAR AND SRH.SEMESTER = S3.SEMESTER AND SRH.GRADE = S3.GRADE AND SRH.HR_CLASS = S3.HR_CLASS AND S3.TR_DIV = '6'");
        stb.append(" WHERE ");
        stb.append("     SRH.YEAR || '-' || SRH.SEMESTER = '" + _param._yearSem + "' ");
        stb.append(" GROUP BY");
        stb.append("     SRH.YEAR, ");
        stb.append("     SRH.SEMESTER, ");
        stb.append("     SRH.GRADE,");
        stb.append("     SRH.HR_CLASS,");
        stb.append("     SRH.HR_NAME,");
        stb.append("     SRH.HR_NAMEABBV,");
        stb.append("     SRH.HR_CLASS_NAME1,");
        stb.append("     SRH.HR_CLASS_NAME2,");
        stb.append("     SRH.TR_CD1, ");
        stb.append("     SRH.TR_CD2, ");
        stb.append("     SRH.TR_CD3, ");
        stb.append("     SRH.SUBTR_CD1, ");
        stb.append("     SRH.SUBTR_CD2, ");
        stb.append("     SRH.SUBTR_CD3, ");
        stb.append("     SRH.HR_FACCD, ");
        stb.append("     SRH.CLASSWEEKS, ");
        stb.append("     SRH.CLASSDAYS ");
        stb.append(" ORDER BY ");
        stb.append("     SRH.YEAR,SRH.SEMESTER,SRH.GRADE,SRH.HR_CLASS");
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
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final boolean _header;
        private final String _templatePath;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _yearSem = request.getParameter("PARAM_YEAR_SEMESTER");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _header = request.getParameter("HEADER") == null ? false : true;
            _templatePath = request.getParameter("TEMPLATE_PATH");
        }

    }
}

// eof
