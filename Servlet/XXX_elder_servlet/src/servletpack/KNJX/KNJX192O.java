// kanji=漢字
/*
 * $Id: ddfdbd4db50fa3c341c00a72f95e7e348eff112c $
 *
 * 作成日: 2011/03/03 11:12:49 - JST
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
 * @version $Id: ddfdbd4db50fa3c341c00a72f95e7e348eff112c $
 */
public class KNJX192O extends AbstractXls {

    private static final Log log = LogFactory.getLog("KNJX192O.class");

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
        retList.add("特別活動の記録");
        retList.add("欠席の主な理由");
        retList.add("身体状況備考");
        retList.add("本人の長所・推薦事由");
        return retList;
    }

    protected String[] getCols() {
        final String[] cols = {"GRADE",
                "HR_CLASS",
                "ATTENDNO",
                "NAME",
                "SCHREGNO",
                "JOBHUNT_REC",
                "JOBHUNT_ABSENCE",
                "JOBHUNT_HEALTHREMARK",
                "JOBHUNT_RECOMMEND",};
        return cols;
    }

    protected String getSql() {
        final StringBuffer stb = new StringBuffer();

        final String table_regd = ("grd".equals(_param._mode)) ? "GRD_REGD_DAT" : "SCHREG_REGD_DAT";
        final String table_base = ("grd".equals(_param._mode)) ? "GRD_BASE_MST" : "SCHREG_BASE_MST";
        final String table_emp  = ("grd".equals(_param._mode)) ? "GRD_HEXAM_EMPREMARK_DAT" : "HEXAM_EMPREMARK_DAT";

        stb.append(" SELECT ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     T2.NAME, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T3.JOBHUNT_REC, ");
        stb.append("     T3.JOBHUNT_ABSENCE, ");
        stb.append("     T3.JOBHUNT_HEALTHREMARK, ");
        stb.append("     T3.JOBHUNT_RECOMMEND ");
        stb.append(" FROM ");
        stb.append("     " + table_regd + " T1 ");
        stb.append("     LEFT JOIN " + table_base + " T2 ON  T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("     LEFT JOIN " + table_emp + "  T3 ON  T3.SCHREGNO = T1.SCHREGNO ");
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
        private final String _mode;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _yearSem = request.getParameter("YEAR");
            _gradeHrClass = request.getParameter("GRADE_HR_CLASS");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _header = request.getParameter("HEADER") == null ? false : true;
            _templatePath = request.getParameter("TEMPLATE_PATH");
            _mode = request.getParameter("XLS_MODE");
        }

    }
}

// eof
