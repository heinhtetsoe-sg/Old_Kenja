// kanji=漢字
/*
 * $Id: 4400c2a1cb71b912d8c76636a74ae8a3532b059b $
 *
 * 作成日: 2011/04/02 0:24:05 - JST
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
 * @author m-yama
 * @version $Id: 4400c2a1cb71b912d8c76636a74ae8a3532b059b $
 */
public class KNJX060A extends AbstractXls {

    private static final Log log = LogFactory.getLog("KNJX060A.class");

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
        retList.add("※講座コード");
        retList.add("※群コード");
        if ("1".equals(_param._useCurriculumcd)) {
            retList.add("※教科コード");
            retList.add("※学校種別");
            retList.add("※教育課程コード");
        }
        retList.add("※科目コード");
        retList.add("※講座名称");
        retList.add("※講座略称");
        retList.add("履修学期");
        retList.add("週授業回数");
        retList.add("連続枠数");
        retList.add("集計フラグ");
        return retList;
    }

    protected String[] getCols() {
        final String[] cols;
        if ("1".equals(_param._useCurriculumcd)) {
            cols = new String[]{
                    "YEAR",
                    "SEMESTER",
                    "CHAIRCD",
                    "GROUPCD",
                    "CLASSCD",
                    "SCHOOL_KIND",
                    "CURRICULUM_CD",
                    "SUBCLASSCD",
                    "CHAIRNAME",
                    "CHAIRABBV",
                    "TAKESEMES",
                    "LESSONCNT",
                    "FRAMECNT",
                    "COUNTFLG",};
        } else {
            cols = new String[]{
                    "YEAR",
                    "SEMESTER",
                    "CHAIRCD",
                    "GROUPCD",
                    "SUBCLASSCD",
                    "CHAIRNAME",
                    "CHAIRABBV",
                    "TAKESEMES",
                    "LESSONCNT",
                    "FRAMECNT",
                    "COUNTFLG",};
        }
        return cols;
    }

    protected String getSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     * ");
        stb.append(" FROM ");
        stb.append("     CHAIR_DAT ");
        stb.append(" WHERE ");
        stb.append("     YEAR || SEMESTER = '" + _param._yearSem + "' ");
        if (_param._chairCd != null && !"".equals(_param._chairCd)) {
            stb.append("     AND CHAIRCD = '" + _param._chairCd + "' ");
        }
        if (_param._subclassCd != null && !"".equals(_param._subclassCd)) {
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '" + _param._subclassCd + "' ");
            } else {
                stb.append("     AND SUBCLASSCD = '" + _param._subclassCd + "' ");
            }
        }
        stb.append(" ORDER BY ");
        stb.append("     CHAIRCD, ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || ");
        }
        stb.append("     SUBCLASSCD ");
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
        private final String _chairCd;
        private final String _subclassCd;
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final boolean _header;
        private final String _templatePath;
        private final String _useCurriculumcd;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _yearSem = request.getParameter("YEAR");
            _chairCd = request.getParameter("CHAIRCD");
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
