// kanji=漢字
/*
 * $Id: 9a62031f16220e8dc678a08df42cb74823acb224 $
 *
 * 作成日: 2011/02/22 12:18:33 - JST
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
 * @version $Id: 9a62031f16220e8dc678a08df42cb74823acb224 $
 */
public class KNJX160B extends AbstractXls {

    private static final Log log = LogFactory.getLog("KNJX160B.class");

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
        retList.add("※学籍番号");
        retList.add("※続柄");
        retList.add("保護者氏名");
        retList.add("保護者氏名かな");
        retList.add("性別");
        retList.add("生年月日");
        retList.add("郵便番号");
        retList.add("住所1");
        retList.add("住所2");
        retList.add("電話番号");
        retList.add("FAX番号");
        retList.add("E-MAIL");
        retList.add("職種コード");
        retList.add("勤務先名称");
        retList.add("勤務先電話番号");
        retList.add("保証人続柄");
        retList.add("保証人氏名");
        retList.add("保証人氏名かな");
        retList.add("保証人性別");
        retList.add("保証人郵便番号");
        retList.add("保証人住所1");
        retList.add("保証人住所2");
        retList.add("保証人電話番号");
        retList.add("保証人職種コード");
        retList.add("兼ねている公職");
        return retList;
    }

    protected String[] getCols() {
        final String[] cols = {"GRADE",
                "HR_CLASS",
                "ATTENDNO",
                "NAME",
                "SCHREGNO",
                "RELATIONSHIP",
                "GUARD_NAME",
                "GUARD_KANA",
                "GUARD_SEX",
                "GUARD_BIRTHDAY",
                "GUARD_ZIPCD",
                "GUARD_ADDR1",
                "GUARD_ADDR2",
                "GUARD_TELNO",
                "GUARD_FAXNO",
                "GUARD_E_MAIL",
                "GUARD_JOBCD",
                "GUARD_WORK_NAME",
                "GUARD_WORK_TELNO",
                "GUARANTOR_RELATIONSHIP",
                "GUARANTOR_NAME",
                "GUARANTOR_KANA",
                "GUARANTOR_SEX",
                "GUARANTOR_ZIPCD",
                "GUARANTOR_ADDR1",
                "GUARANTOR_ADDR2",
                "GUARANTOR_TELNO",
                "GUARANTOR_JOBCD",
                "PUBLIC_OFFICE",};
        return cols;
    }

    protected String getSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     T2.NAME, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T3.RELATIONSHIP, ");
        stb.append("     T3.GUARD_NAME, ");
        stb.append("     T3.GUARD_KANA, ");
        stb.append("     T3.GUARD_SEX, ");
        stb.append("     T3.GUARD_BIRTHDAY, ");
        stb.append("     T3.GUARD_ZIPCD, ");
        stb.append("     T3.GUARD_ADDR1, ");
        stb.append("     T3.GUARD_ADDR2, ");
        stb.append("     T3.GUARD_TELNO, ");
        stb.append("     T3.GUARD_FAXNO, ");
        stb.append("     T3.GUARD_E_MAIL, ");
        stb.append("     T3.GUARD_JOBCD, ");
        stb.append("     T3.GUARD_WORK_NAME, ");
        stb.append("     T3.GUARD_WORK_TELNO, ");
        stb.append("     T3.GUARANTOR_RELATIONSHIP, ");
        stb.append("     T3.GUARANTOR_NAME, ");
        stb.append("     T3.GUARANTOR_KANA, ");
        stb.append("     T3.GUARANTOR_SEX, ");
        stb.append("     T3.GUARANTOR_ZIPCD, ");
        stb.append("     T3.GUARANTOR_ADDR1, ");
        stb.append("     T3.GUARANTOR_ADDR2, ");
        stb.append("     T3.GUARANTOR_TELNO, ");
        stb.append("     T3.GUARANTOR_JOBCD, ");
        stb.append("     T3.PUBLIC_OFFICE ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT T1 ");
        stb.append("     LEFT JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN GUARDIAN_DAT T3 ON T3.SCHREGNO = T1.SCHREGNO ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR || T1.SEMESTER = '" + _param._yearSem + "' ");
        stb.append("     AND T3.SCHREGNO = T1.SCHREGNO ");
        if (_param._gradeHrClass != null && !"".equals(_param._gradeHrClass)) {
            stb.append("     AND T1.GRADE || T1.HR_CLASS = '" + _param._gradeHrClass + "' ");
        }
        stb.append(" ORDER BY ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO, ");
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
        private final String _yearSem;
        private final String _gradeHrClass;
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final boolean _header;
        private final String _templatePath;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _yearSem = request.getParameter("YEAR");
            _gradeHrClass = request.getParameter("GRADE_HR_CLASS");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _header = request.getParameter("HEADER") == null ? false : true;
            _templatePath = request.getParameter("TEMPLATE_PATH");
        }

    }
}

// eof
