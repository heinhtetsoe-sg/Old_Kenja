// kanji=漢字
/*
 * $Id: b4ea65c4f08bd963838a88686ddbc22377e661dd $
 *
 * 作成日: 2011/04/05 16:07:45 - JST
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2011 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJA;

import java.util.ArrayList;
import java.util.List;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.AbstractXls;
import servletpack.KNJZ.detail.KNJServletUtils;

/**
 * <<クラスの説明>>。
 * @author nakamoto
 * @version $Id: b4ea65c4f08bd963838a88686ddbc22377e661dd $
 */
public class KNJA263 extends AbstractXls {

    private static final Log log = LogFactory.getLog("KNJA263.class");

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
        retList.add("氏名漢字");
        retList.add("氏名かな");
        retList.add("性別");
        retList.add("年");
        retList.add("組");
        retList.add("番");
        retList.add("課程コード");
        retList.add("学科コード");
        retList.add("コースコード");
        retList.add("評定平均");
        retList.add("成績平均");
        return retList;
    }

    protected String[] getCols() {
        final String[] cols = {"YEAR",
                "SCHREGNO",
                "NAME",
                "NAME_KANA",
                "SEX",
                "GRADE",
                "HR_CLASS",
                "ATTENDNO",
                "COURSECD",
                "MAJORCD",
                "COURSECODE",
                "AVG_ASSEC",
                "AVG_RECORD",};
        return cols;
    }

    protected String getSql() {
        final StringBuffer stb = new StringBuffer();

        stb.append(" WITH BASE_T AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T2.NAME, ");
        stb.append("     T2.NAME_KANA, ");
        stb.append("     T2.SEX, ");
        stb.append("     T1.OLD_GRADE AS GRADE, ");
        stb.append("     T1.OLD_HR_CLASS AS HR_CLASS, ");
        stb.append("     T1.OLD_ATTENDNO AS ATTENDNO, ");
        stb.append("     T1.COURSECD, ");
        stb.append("     T1.MAJORCD, ");
        stb.append("     T1.COURSECODE ");
        stb.append(" FROM ");
        stb.append("     CLASS_FORMATION_DAT T1 ");
        stb.append("     LEFT JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._examYear + "' ");
        stb.append("     AND T1.SEMESTER = '1' ");
        stb.append("     AND T1.OLD_GRADE = '" + _param._grade + "' ");
        stb.append("     AND T1.REMAINGRADE_FLG <> '1' ");
        stb.append("     AND T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("     AND (T2.GRD_DIV is null OR T2.GRD_DIV = '') ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     '" + _param._examYear + "' AS YEAR, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.NAME, ");
        stb.append("     T1.NAME_KANA, ");
        stb.append("     T1.SEX, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     T1.COURSECD, ");
        stb.append("     T1.MAJORCD, ");
        stb.append("     T1.COURSECODE, ");
        if (_param._isRecordRank) {
            stb.append("     MAX(DECIMAL(ROUND(FLOAT(L1.AVG)*10,0)/10,5,1)) AS LEVEL_AVG, ");
        } else {
            stb.append("     SUM(VALUE(L1.GRAD_VALUE, 0)) AS LEVEL_SUM, ");
            stb.append("     COUNT(*) AS LEVEL_CNT, ");
        }
        if (_param._isGakunenSei) {
            if (_param._isRecordRank) {
                stb.append("     MAX(DECIMAL(ROUND(FLOAT(L1.AVG)*10,0)/10,5,1)) AS AVG_ASSEC, ");
            } else {
                stb.append("     DECIMAL(ROUND(FLOAT(SUM(VALUE(L1.GRAD_VALUE, 0)) / COUNT(*))*10,0)/10,5,1) AS AVG_ASSEC, ");
            }
        } else {
            stb.append("     '' AS AVG_ASSEC, ");
        }
        stb.append("     '' AS AVG_RECORD ");
        stb.append(" FROM ");
        stb.append("     BASE_T T1 ");
        if (_param._isRecordRank) {
            stb.append("     LEFT JOIN RECORD_RANK_DAT L1 ON L1.YEAR = '" + _param._ctrlYear + "' ");
            stb.append("          AND L1.SEMESTER = '9' ");
            stb.append("          AND L1.TESTKINDCD = '99' ");
            stb.append("          AND L1.TESTITEMCD = '00' ");
            stb.append("          AND L1.SUBCLASSCD = '999999' ");
            stb.append("          AND L1.SCHREGNO = T1.SCHREGNO ");
        } else {
            stb.append("     LEFT JOIN RECORD_DAT L1 ON L1.YEAR = '" + _param._ctrlYear + "' ");
            stb.append("          AND L1.SCHREGNO = T1.SCHREGNO ");
            stb.append("          AND L1.GRAD_VALUE IS NOT NULL ");
            stb.append(" WHERE ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("     L1.CLASSCD || '-' || L1.SCHOOL_KIND || '-' || L1.CURRICULUM_CD || '-' || ");
            }
            stb.append("     L1.SUBCLASSCD NOT IN ( SELECT DISTINCT ");
            if ("1".equals(_param._useCurriculumcd)) {
                stb.append("     ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ");
            }
            stb.append("                                ATTEND_SUBCLASSCD ");
            stb.append("                            FROM ");
            stb.append("                                SUBCLASS_REPLACE_COMBINED_DAT ");
            stb.append("                            WHERE ");
            stb.append("                                REPLACECD = '1' ");
            stb.append("                                AND YEAR = '" + _param._ctrlYear + "' ");
            stb.append("                          ) ");
        }
        stb.append(" GROUP BY ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.NAME, ");
        stb.append("     T1.NAME_KANA, ");
        stb.append("     T1.SEX, ");
        stb.append("     T1.GRADE, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     T1.COURSECD, ");
        stb.append("     T1.MAJORCD, ");
        stb.append("     T1.COURSECODE ");
        stb.append("  ORDER BY ");
        stb.append("      T1.SCHREGNO ");

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
        private final boolean _isRecordRank;
        private final boolean _isGakunenSei;
        private final String _useCurriculumcd;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _examYear = request.getParameter("XLS_EXAMYEAR");
            _grade = request.getParameter("GRADE");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _header = true;
            _templatePath = request.getParameter("TEMPLATE_PATH");
            final String recordTableDiv = request.getParameter("XLS_RECORD_TABLE_DIV");
            _isRecordRank = "1".equals(recordTableDiv);
            final String schoolDiv = getSchoolDiv();
            _isGakunenSei = "0".equals(schoolDiv);
            _useCurriculumcd = request.getParameter("useCurriculumcd");
        }

        private String getSchoolDiv() throws SQLException {
            String schoolDiv = "";

            final String sql = getSchoolDivSql();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = _db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    schoolDiv = rs.getString("SCHOOLDIV");
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                _db2.commit();
            }

            return schoolDiv;
        }

        private String getSchoolDivSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     SCHOOLDIV ");
            stb.append(" FROM ");
            stb.append("     SCHOOL_MST ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _ctrlYear + "' ");
            return stb.toString();
        }

    }
}

// eof
