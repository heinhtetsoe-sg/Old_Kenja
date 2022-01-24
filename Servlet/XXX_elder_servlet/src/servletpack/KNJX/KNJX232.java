// kanji=漢字
/*
 * $Id: dac3f087ee923a8798c83e2345ea0f87d90a446e $
 *
 * 作成日: 2011/03/30 20:28:14 - JST
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
 * @version $Id: dac3f087ee923a8798c83e2345ea0f87d90a446e $
 */
public class KNJX232 extends AbstractXls {

    private static final Log log = LogFactory.getLog("KNJX232.class");

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
        if ("1".equals(_param._useCurriculumcd)) {
            retList.add("教科コード");
            retList.add("学校種別");
            retList.add("教育課程コード");
        }
        retList.add("科目コード");
        retList.add("科目名");
        retList.add("講座コード");
        retList.add("講座名");
        retList.add("年組");
        retList.add("出席番号");
        retList.add("氏名");
        retList.add("※学籍番号");
        retList.add("※出欠日付");
        retList.add("※校時コード");
        retList.add("※勤怠コード");
        retList.add("勤怠備考");
        return retList;
    }

    protected String[] getCols() {
        final String[] cols;
        if ("1".equals(_param._useCurriculumcd)) {
            cols = new String[]{
                    "CLASSCD",
                    "SCHOOL_KIND",
                    "CURRICULUM_CD",
                    "SUBCLASSCD",
                    "SUBCLASSNAME",
                    "CHAIRCD",
                    "CHAIRNAME",
                    "HR_NAME",
                    "ATTENDNO",
                    "NAME",
                    "SCHREGNO",
                    "ATTENDDATE",
                    "PERIODCD",
                    "DI_CD",
                    "DI_REMARK",};
        } else {
            cols = new String[]{
                    "SUBCLASSCD",
                    "SUBCLASSNAME",
                    "CHAIRCD",
                    "CHAIRNAME",
                    "HR_NAME",
                    "ATTENDNO",
                    "NAME",
                    "SCHREGNO",
                    "ATTENDDATE",
                    "PERIODCD",
                    "DI_CD",
                    "DI_REMARK",};
        }
        return cols;
    }

    protected String getSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH SCH_INFO AS ( ");
        stb.append("     SELECT ");
        stb.append("         T1.SCHREGNO, ");
        stb.append("         T1.GRADE, ");
        stb.append("         T1.HR_CLASS, ");
        stb.append("         T1.ATTENDNO, ");
        stb.append("         S1.NAME, ");
        stb.append("         S2.HR_NAME ");
        stb.append("     FROM ");
        stb.append("         SCHREG_REGD_DAT T1 ");
        stb.append("     LEFT JOIN  ");
        stb.append("         SCHREG_BASE_MST S1  ON S1.SCHREGNO  = T1.SCHREGNO ");
        stb.append("     LEFT JOIN  ");
        stb.append("         SCHREG_REGD_HDAT S2 ON S2.YEAR      = T1.YEAR ");
        stb.append("                            AND S2.SEMESTER  = T1.SEMESTER ");
        stb.append("                            AND S2.GRADE     = T1.GRADE ");
        stb.append("                            AND S2.HR_CLASS  = T1.HR_CLASS ");
        stb.append("     WHERE ");
        stb.append("         T1.YEAR || T1.SEMESTER = '" + _param._yearSem + "' ");
        if (_param._gradeHrClass != null && !"".equals(_param._gradeHrClass)) {
            stb.append(" AND T1.GRADE || T1.HR_CLASS = '" + _param._gradeHrClass + "' ");
        }
        if (_param._schregNo != null && !"".equals(_param._schregNo)) {
            stb.append(" AND T1.SCHREGNO = '" + _param._schregNo + "' ");
        }

        stb.append(" ), DATE AS ( ");
        stb.append("     SELECT ");
        stb.append("         SDATE, ");
        stb.append("         EDATE ");
        stb.append("     FROM ");
        stb.append("         SEMESTER_MST ");
        stb.append("     WHERE ");
        stb.append("         YEAR || SEMESTER = '" + _param._yearSem + "' ");

        stb.append(" ), CHR_STD AS ( ");
        stb.append("     SELECT DISTINCT ");
        stb.append("         T1.CHAIRCD, ");
        stb.append("         T1.SCHREGNO ");
        stb.append("     FROM ");
        stb.append("         CHAIR_STD_DAT T1, ");
        stb.append("         SCH_INFO T2 ");
        stb.append("     WHERE ");
        stb.append("         T1.YEAR || T1.SEMESTER = '" + _param._yearSem + "' AND ");
        stb.append("         T1.SCHREGNO = T2.SCHREGNO ");

        stb.append(" ), CHR_STF AS ( ");
        stb.append("     SELECT DISTINCT ");
        stb.append("         CHAIRCD ");
        stb.append("     FROM ");
        stb.append("         CHAIR_STF_DAT ");
        stb.append("     WHERE ");
        stb.append("         YEAR || SEMESTER = '" + _param._yearSem + "' ");
        if (_param._staffCd != null && !"".equals(_param._staffCd)) {
            stb.append(" AND STAFFCD = '" + _param._staffCd + "' ");
        }

        stb.append(" ), SUBCLASS AS ( ");
        stb.append("     SELECT ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         T1.CLASSCD, ");
            stb.append("         T1.SCHOOL_KIND, ");
            stb.append("         T1.CURRICULUM_CD, ");
        }
        stb.append("         T1.SUBCLASSCD, ");
        stb.append("         S1.SUBCLASSNAME, ");
        stb.append("         T1.CHAIRCD, ");
        stb.append("         T1.CHAIRNAME, ");
        stb.append("         T2.SCHREGNO ");
        stb.append("     FROM ");
        stb.append("         CHAIR_DAT T1 ");
        stb.append("     LEFT JOIN ");
        stb.append("         SUBCLASS_MST S1 ON T1.SUBCLASSCD = S1.SUBCLASSCD ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         AND T1.CLASSCD = S1.CLASSCD AND T1.SCHOOL_KIND = S1.SCHOOL_KIND AND T1.CURRICULUM_CD = S1.CURRICULUM_CD ");
        }
        stb.append("         , ");
        stb.append("         CHR_STD T2, ");
        stb.append("         CHR_STF T3 ");
        stb.append("     WHERE ");
        stb.append("         T1.YEAR || T1.SEMESTER = '" + _param._yearSem + "' AND ");
        stb.append("         T1.CHAIRCD = T2.CHAIRCD AND ");
        stb.append("         T1.CHAIRCD = T3.CHAIRCD ");
        if (_param._subclassCd != null && !"".equals(_param._subclassCd)) {
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append(" AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '" + _param._subclassCd + "' ");
            } else {
                stb.append(" AND T1.SUBCLASSCD = '" + _param._subclassCd + "' ");
            }
        }
        if (_param._chairCd != null && !"".equals(_param._chairCd)) {
            stb.append(" AND T1.CHAIRCD = '" + _param._chairCd + "' ");
        }
        stb.append(" ), SCHEDULE AS ( ");
        stb.append("     SELECT ");
        stb.append("         T2.CHAIRCD, ");
        stb.append("         T1.ATTENDDATE, ");
        stb.append("         T1.PERIODCD, ");
        stb.append("         T1.DI_CD, ");
        stb.append("         VALUE(L1.NAME1, T1.DI_REMARK) AS DI_REMARK, ");
        stb.append("         T1.SCHREGNO, ");
        stb.append("         T3.GRADE, ");
        stb.append("         T3.HR_CLASS, ");
        stb.append("         T3.ATTENDNO, ");
        stb.append("         T3.NAME, ");
        stb.append("         T3.HR_NAME ");
        stb.append("     FROM ");
        stb.append("         ATTEND_DAT T1 ");
        stb.append("         LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'C901' AND L1.NAMECD2 = T1.DI_REMARK_CD, ");
        stb.append("         SCH_CHR_DAT T2, ");
        stb.append("         SCH_INFO T3, ");
        stb.append("         DATE T4 ");
        stb.append("     WHERE ");
        stb.append("         T1.ATTENDDATE = T2.EXECUTEDATE AND ");
        stb.append("         T1.PERIODCD = T2.PERIODCD AND ");
        stb.append("         T1.SCHREGNO = T3.SCHREGNO AND ");
        stb.append("         T1.ATTENDDATE BETWEEN T4.SDATE AND T4.EDATE ");
        stb.append(" ) ");

        stb.append(" SELECT ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     T2.CLASSCD, ");
            stb.append("     T2.SCHOOL_KIND, ");
            stb.append("     T2.CURRICULUM_CD, ");
        }
        stb.append("     T2.SUBCLASSCD, ");
        stb.append("     T2.SUBCLASSNAME, ");
        stb.append("     T1.CHAIRCD, ");
        stb.append("     T2.CHAIRNAME, ");
        stb.append("     T1.HR_NAME, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     T1.NAME, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.ATTENDDATE, ");
        stb.append("     T1.PERIODCD, ");
        stb.append("     T1.DI_CD, ");
        stb.append("     T1.DI_REMARK ");
        stb.append(" FROM ");
        stb.append("     SCHEDULE T1, ");
        stb.append("     SUBCLASS T2 ");
        stb.append(" WHERE ");
        stb.append("     T1.CHAIRCD = T2.CHAIRCD AND ");
        stb.append("     T1.SCHREGNO = T2.SCHREGNO ");
        stb.append(" ORDER BY ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     T2.CLASSCD, ");
            stb.append("     T2.SCHOOL_KIND, ");
            stb.append("     T2.CURRICULUM_CD, ");
        }
        stb.append("     T2.SUBCLASSCD, ");
        stb.append("     T1.CHAIRCD, ");
        stb.append("     T1.ATTENDDATE, ");
        stb.append("     T1.PERIODCD, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO ");
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
        private final String _subclassCd;
        private final String _chairCd;
        private final String _staffCd;
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final boolean _header;
        private final String _templatePath;
        private final String _useCurriculumcd;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _yearSem = request.getParameter("YEAR");
            _gradeHrClass = request.getParameter("GRADE_HR_CLASS");
            _schregNo = request.getParameter("STUDENT");
            _subclassCd = request.getParameter("SUBCLASSCD");
            _chairCd = request.getParameter("CHAIRCD");
            _staffCd = request.getParameter("STAFFCD");
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
