// kanji=漢字
/*
 * $Id: 5200c154036a694925e4da99f245497afe21ea57 $
 *
 * 作成日: 2011/02/19 1:22:14 - JST
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
 * @version $Id: 5200c154036a694925e4da99f245497afe21ea57 $
 */
public class KNJX020B extends AbstractXls {

    private static final Log log = LogFactory.getLog("KNJX020B.class");

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
        retList.add("内外区分");
        retList.add("※生徒氏名");
        retList.add("生徒氏名表示用");
        retList.add("生徒氏名かな");
        retList.add("生徒氏名英字");
        retList.add("生年月日");
        retList.add("性別");
        retList.add("血液型");
        retList.add("血液RH型");
        retList.add("その他");
        retList.add("国籍");
        retList.add("出身校コード");
        retList.add("出身校卒業年月日");
        retList.add("塾コード");
        retList.add("入学日付");
        retList.add("入学区分");
        retList.add("入学理由");
        retList.add("入学学校");
        retList.add("入学住所");
        retList.add("除籍(卒業)日付");
        retList.add("除籍(卒業)区分");
        retList.add("除籍(卒業)事由");
        retList.add("除籍(卒業)学校");
        retList.add("除籍(卒業)住所");
        retList.add("卒業生台帳番号");
        retList.add("卒業期");
        retList.add("備考１");
        retList.add("備考２");
        retList.add("備考３");
        retList.add("急用連絡先");
        retList.add("急用連絡先名");
        retList.add("急用連絡先続柄");
        retList.add("急用電話番号");
        retList.add("急用連絡先２");
        retList.add("急用連絡先名２");
        retList.add("急用連絡先続柄２");
        retList.add("急用電話番号２");
        return retList;
    }

    protected String[] getCols() {
        final String[] cols = {"SCHREGNO",
                "INOUTCD",
                "NAME",
                "NAME_SHOW",
                "NAME_KANA",
                "NAME_ENG",
                "BIRTHDAY",
                "SEX",
                "BLOODTYPE",
                "BLOOD_RH",
                "HANDICAP",
                "NATIONALITY",
                "FINSCHOOLCD",
                "FINISH_DATE",
                "PRISCHOOLCD",
                "ENT_DATE",
                "ENT_DIV",
                "ENT_REASON",
                "ENT_SCHOOL",
                "ENT_ADDR",
                "GRD_DATE",
                "GRD_DIV",
                "GRD_REASON",
                "GRD_SCHOOL",
                "GRD_ADDR",
                "GRD_NO",
                "GRD_TERM",
                "REMARK1",
                "REMARK2",
                "REMARK3",
                "EMERGENCYCALL",
                "EMERGENCYNAME",
                "EMERGENCYRELA_NAME",
                "EMERGENCYTELNO",
                "EMERGENCYCALL2",
                "EMERGENCYNAME2",
                "EMERGENCYRELA_NAME2",
                "EMERGENCYTELNO2",};
        return cols;
    }

    protected String getSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.INOUTCD, ");
        stb.append("     T1.NAME, ");
        stb.append("     T1.NAME_SHOW, ");
        stb.append("     T1.NAME_KANA, ");
        stb.append("     T1.NAME_ENG, ");
        stb.append("     T1.BIRTHDAY, ");
        stb.append("     T1.SEX, ");
        stb.append("     T1.BLOODTYPE, ");
        stb.append("     T1.BLOOD_RH, ");
        stb.append("     T1.HANDICAP, ");
        stb.append("     T1.NATIONALITY, ");
        stb.append("     T1.FINSCHOOLCD, ");
        stb.append("     T1.FINISH_DATE, ");
        stb.append("     T1.PRISCHOOLCD, ");
        stb.append("     T1.ENT_DATE, ");
        stb.append("     T1.ENT_DIV, ");
        stb.append("     T1.ENT_REASON, ");
        stb.append("     T1.ENT_SCHOOL, ");
        stb.append("     T1.ENT_ADDR, ");
        stb.append("     T1.GRD_DATE, ");
        stb.append("     T1.GRD_DIV, ");
        stb.append("     T1.GRD_REASON, ");
        stb.append("     T1.GRD_SCHOOL, ");
        stb.append("     T1.GRD_ADDR, ");
        stb.append("     T1.GRD_NO, ");
        stb.append("     T1.GRD_TERM, ");
        stb.append("     T1.REMARK1, ");
        stb.append("     T1.REMARK2, ");
        stb.append("     T1.REMARK3, ");
        stb.append("     T1.EMERGENCYCALL, ");
        stb.append("     T1.EMERGENCYNAME, ");
        stb.append("     T1.EMERGENCYRELA_NAME, ");
        stb.append("     T1.EMERGENCYTELNO, ");
        stb.append("     T1.EMERGENCYCALL2, ");
        stb.append("     T1.EMERGENCYNAME2, ");
        stb.append("     T1.EMERGENCYRELA_NAME2, ");
        stb.append("     T1.EMERGENCYTELNO2, ");
        stb.append("     'DUMMY' AS DUMMY ");
        stb.append(" FROM ");
        stb.append("     SCHREG_BASE_MST T1 ");
        stb.append(" ORDER BY ");
        stb.append("     SCHREGNO ");
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
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final boolean _header;
        private final String _templatePath;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _header = request.getParameter("HEADER") == null ? false : true;
            _templatePath = request.getParameter("TEMPLATE_PATH");
        }

    }
}

// eof
