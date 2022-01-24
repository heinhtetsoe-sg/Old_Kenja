// kanji=漢字
/*
 * $Id: c8b67dbb597ad80a4af0358d7dee7a982dc37fcb $
 *
 * 作成日: 2011/04/25 16:49:46 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2011 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJM;

import java.util.ArrayList;
import java.util.List;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.AbstractXls;
import servletpack.KNJZ.detail.KNJServletUtils;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: c8b67dbb597ad80a4af0358d7dee7a982dc37fcb $
 */
public class KNJM050 extends AbstractXls {

    private static final Log log = LogFactory.getLog("KNJM050.class");

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
        retList.add("レポート番号");
        retList.add("科目番号");
        retList.add("科目名");
        retList.add("学籍番号");
        retList.add("提出受付日付");
        retList.add("提出受付時間");
        retList.add("添削者職員コード");
        retList.add("評定");
        retList.add("評価返送日付");
        retList.add("評価返送時間");
        return retList;
    }

    protected String[] getCols() {
        final String[] cols = {"SUBCLASSCD",
                "SUBCLASSNAME",
                "SCHREGNO",
                "RECEIPT_DATE",
                "RECEIPT_TIME",
                "STAFFCD",
                "GRAD_VALUE",
                "GRAD_DATE",
                "GRAD_TIME",};
        return cols;
    }

    protected List getXlsDataList() throws SQLException {
        final String sql = getSql();
        final String[] cols = getCols();
        PreparedStatement psXls = null;
        ResultSet rsXls = null;
        final List dataList = new ArrayList();
        try {
            psXls = _db2.prepareStatement(sql);
            rsXls = psXls.executeQuery();
            while (rsXls.next()) {
                final List xlsData = new ArrayList();
                final String stSeq = rsXls.getString("STANDARD_SEQ");
                final String setStSeq = StringUtils.leftPad(stSeq, 2, '0');
                final String set1Gyoume = rsXls.getString("YEAR") + rsXls.getString("SUBCLASSCD") + setStSeq + rsXls.getString("REPRESENT_SEQ");
                xlsData.add(set1Gyoume);
                for (int i = 0; i < cols.length; i++) {
                    xlsData.add(rsXls.getString(cols[i]));
                }
                xlsData.add("DUMMY");
                dataList.add(xlsData);
            }
        } finally {
            DbUtils.closeQuietly(null, psXls, rsXls);
            _db2.commit();
        }
        return dataList;
    }

    protected String getSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     SUBSTR(t1.YEAR,4,1) as YEAR, ");
        stb.append("     t1.STANDARD_SEQ, ");
        stb.append("     t1.REPRESENT_SEQ, ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     t1.CLASSCD || '-' || t1.SCHOOL_KIND || '-' || t1.CURRICULUM_CD || '-' || t1.SUBCLASSCD AS SUBCLASSCD, ");
        } else {
            stb.append("     t1.SUBCLASSCD, ");
        }
        stb.append("     t2.SUBCLASSNAME, ");
        stb.append("     t1.SCHREGNO, ");
        stb.append("     t1.RECEIPT_DATE, ");
        stb.append("     t1.RECEIPT_TIME, ");
        stb.append("     t1.STAFFCD, ");
        stb.append("     t1.GRAD_VALUE, ");
        stb.append("     t1.GRAD_DATE, ");
        stb.append("     t1.GRAD_TIME ");
        stb.append(" FROM ");
        stb.append("     REP_PRESENT_DAT t1 ");
        stb.append("     LEFT JOIN SUBCLASS_MST t2 ON t1.SUBCLASSCD = t2.SUBCLASSCD ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     AND t1.CLASSCD = t2.CLASSCD ");
            stb.append("     AND t1.SCHOOL_KIND = t2.SCHOOL_KIND ");
            stb.append("     AND t1.CURRICULUM_CD = t2.CURRICULUM_CD ");
        }
        stb.append(" WHERE ");
        stb.append("     t1.YEAR = '" + _param._year + "' ");
        if (!"0".equals(_param._subclassName)){
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("     AND t1.CLASSCD || '-' || t1.SCHOOL_KIND || '-' || t1.CURRICULUM_CD || '-' || t1.SUBCLASSCD = '" + _param._subclassName + "' ");
            } else {
                stb.append("     AND t1.SUBCLASSCD = '" + _param._subclassName + "' ");
            }
        }
        stb.append(" ORDER BY ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     t1.CLASSCD, ");
            stb.append("     t1.SCHOOL_KIND, ");
            stb.append("     t1.CURRICULUM_CD, ");
        }
        stb.append("     t1.SUBCLASSCD, ");
        stb.append("     t1.SCHREGNO, ");
        stb.append("     t1.RECEIPT_DATE, ");
        stb.append("     t1.RECEIPT_TIME ");
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
        private final String _subclassName;
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final boolean _header;
        private final String _templatePath;
        private final String _useCurriculumcd;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _subclassName = request.getParameter("SUBCLASSNAME");
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
