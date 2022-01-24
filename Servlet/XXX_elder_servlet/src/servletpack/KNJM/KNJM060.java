// kanji=漢字
/*
 * $Id: b3c61b23d83ad317522f352cbb3e00fa1b20d27b $
 *
 * 作成日: 2011/04/25 18:43:45 - JST
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
 * @version $Id: b3c61b23d83ad317522f352cbb3e00fa1b20d27b $
 */
public class KNJM060 extends AbstractXls {

    private static final Log log = LogFactory.getLog("KNJM060.class");

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
        retList.add("科目コード");
        retList.add("科目名");
        retList.add("学籍番号");
        retList.add("氏名");
        switch (Integer.parseInt(_param._semester)) {
        case 0:
            if ("0".equals(_param._outDiv)) {
                retList.add("前期素点");
                retList.add("後期素点");
                retList.add("前期評価");
                retList.add("後期評価");
                retList.add("学年評定");
            } else if ("1".equals(_param._outDiv)) {
                retList.add("前期素点");
                retList.add("後期素点");
            } else {
                retList.add("前期評価");
                retList.add("後期評価");
                retList.add("学年評定");
            }
            break;
        case 1:
            if ("0".equals(_param._outDiv)) {
                retList.add("前期素点");
                retList.add("前期評価");
            } else if ("1".equals(_param._outDiv)) {
                retList.add("前期素点");
            } else {
                retList.add("前期評価");
            }
            break;
        case 2:
            if ("0".equals(_param._outDiv)) {
                retList.add("後期素点");
                retList.add("後期評価");
            } else if ("1".equals(_param._outDiv)) {
                retList.add("後期素点");
            } else {
                retList.add("後期評価");
            }
            break;
        default:
            break;
        }
        return retList;
    }

    protected String[] getCols() {
        final String[] cols = {"SUBCLASSCD",
                "SUBCLASSNAME",
                "SCHREGNO",
                "NAME",
                "SEM1_TERM_SCORE",
                "SEM2_TERM_SCORE",
                "SEM1_VALUE",
                "SEM2_VALUE",
                "GRAD_VALUE",};
        final String[] cols1 = {"SUBCLASSCD",
                "SUBCLASSNAME",
                "SCHREGNO",
                "NAME",
                "SEM1_TERM_SCORE",
                "SEM2_TERM_SCORE",};
        final String[] cols2 = {"SUBCLASSCD",
                "SUBCLASSNAME",
                "SCHREGNO",
                "NAME",
                "SEM1_VALUE",
                "SEM2_VALUE",
                "GRAD_VALUE",};
        final String[] cols3 = {"SUBCLASSCD",
                "SUBCLASSNAME",
                "SCHREGNO",
                "NAME",
                "SEM1_TERM_SCORE",
                "SEM1_VALUE",};
        final String[] cols4 = {"SUBCLASSCD",
                "SUBCLASSNAME",
                "SCHREGNO",
                "NAME",
                "SEM1_TERM_SCORE",};
        final String[] cols5 = {"SUBCLASSCD",
                "SUBCLASSNAME",
                "SCHREGNO",
                "NAME",
                "SEM1_VALUE",};
        final String[] cols6 = {"SUBCLASSCD",
                "SUBCLASSNAME",
                "SCHREGNO",
                "NAME",
                "SEM2_TERM_SCORE",
                "SEM2_VALUE",};
        final String[] cols7 = {"SUBCLASSCD",
                "SUBCLASSNAME",
                "SCHREGNO",
                "NAME",
                "SEM2_TERM_SCORE",};
        final String[] cols8 = {"SUBCLASSCD",
                "SUBCLASSNAME",
                "SCHREGNO",
                "NAME",
                "SEM2_VALUE",};

        switch (Integer.parseInt(_param._semester)) {
        case 0:
            if ("0".equals(_param._outDiv)) {
                return cols;
            } else if ("1".equals(_param._outDiv)) {
                return cols1;
            } else {
                return cols2;
            }
        case 1:
            if ("0".equals(_param._outDiv)) {
                return cols3;
            } else if ("1".equals(_param._outDiv)) {
                return cols4;
            } else {
                return cols5;
            }
        case 2:
            if ("0".equals(_param._outDiv)) {
                return cols6;
            } else if ("1".equals(_param._outDiv)) {
                return cols7;
            } else {
                return cols8;
            }
        }
        return cols;
    }

    protected String getSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     t1.CLASSCD || '-' || t1.SCHOOL_KIND || '-' || t1.CURRICULUM_CD || '-' || t1.SUBCLASSCD AS SUBCLASSCD, ");
        } else {
            stb.append("     t1.SUBCLASSCD, ");
        }
        stb.append("     t2.SUBCLASSNAME, ");
        stb.append("     t1.SCHREGNO, ");
        stb.append("     t3.NAME, ");
        switch (Integer.parseInt(_param._semester)){
        case 0:
            if ("0".equals(_param._outDiv)) {
                stb.append("     t1.SEM1_TERM_SCORE, ");
                stb.append("     t1.SEM2_TERM_SCORE, ");
                stb.append("     t1.SEM1_VALUE, ");
                stb.append("     t1.SEM2_VALUE, ");
                stb.append("     t1.GRAD_VALUE ");
            } else if ("1".equals(_param._outDiv)) {
                stb.append("     t1.SEM1_TERM_SCORE, ");
                stb.append("     t1.SEM2_TERM_SCORE ");
            } else {
                stb.append("     t1.SEM1_VALUE, ");
                stb.append("     t1.SEM2_VALUE, ");
                stb.append("     t1.GRAD_VALUE ");
            }
            break;
        case 1:
            if ("0".equals(_param._outDiv)) {
                stb.append("     t1.SEM1_TERM_SCORE, ");
                stb.append("     t1.SEM1_VALUE ");
            } else if ("1".equals(_param._outDiv)) {
                stb.append("     t1.SEM1_TERM_SCORE ");
            } else {
                stb.append("     t1.SEM1_VALUE ");
            }
            break;
        case 2:
            if ("0".equals(_param._outDiv)) {
                stb.append("     t1.SEM2_TERM_SCORE, ");
                stb.append("     t1.SEM2_VALUE ");
            } else if ("1".equals(_param._outDiv)) {
                stb.append("     t1.SEM2_TERM_SCORE ");
            } else {
                stb.append("     t1.SEM2_VALUE ");
            }
            break;
        }
        stb.append(" FROM ");
        stb.append("     RECORD_DAT t1 ");
        stb.append("     LEFT JOIN SUBCLASS_MST t2 ON t1.SUBCLASSCD = t2.SUBCLASSCD ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("     AND t1.CLASSCD = t2.CLASSCD ");
            stb.append("     AND t1.SCHOOL_KIND = t2.SCHOOL_KIND ");
            stb.append("     AND t1.CURRICULUM_CD = t2.CURRICULUM_CD ");
        }
        stb.append("     LEFT JOIN SCHREG_BASE_MST t3 ON t1.SCHREGNO = t3.SCHREGNO ");
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
        stb.append(" t1.SUBCLASSCD, ");
        stb.append(" t1.SCHREGNO ");
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
        private final String _semester;
        private final String _outDiv;
        private final String _subclassName;
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final boolean _header;
        private final String _templatePath;
        private final String _useCurriculumcd;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("GAKKI");
            _outDiv = request.getParameter("OUTDIV");
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
