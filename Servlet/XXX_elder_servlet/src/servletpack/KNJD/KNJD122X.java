// kanji=漢字
/*
 * $Id: a27fd380f10abe443459b2300fd52dda50759242 $
 *
 * 作成日: 2011/03/30 23:12:46 - JST
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2011 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.text.SimpleDateFormat;
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
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;

/**
 * <<クラスの説明>>。
 * @author nakamoto
 * @version $Id: a27fd380f10abe443459b2300fd52dda50759242 $
 */
public class KNJD122X extends AbstractXls {

    private static final Log log = LogFactory.getLog("KNJD122X.class");

    private boolean _hasData;
    private KNJSchoolMst _knjSchoolMst;
    private static final SimpleDateFormat dateSlashFm = new SimpleDateFormat("yyyy/MM/dd");
    private static final SimpleDateFormat dateHyphenFm = new SimpleDateFormat("yyyy-MM-dd");

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

    protected List getXlsDataList() throws SQLException {
        //テスト名の取得
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
                for (int i = 0; i < cols.length; i++) {
                    xlsData.add(rsXls.getString(cols[i]));
                }
                final String score = rsXls.getString(_param.getScoreField());
                if (_param.isShowDi()) {
                    final String scoreDI = rsXls.getString(_param.getScoreField() + "_DI");
                    xlsData.add(scoreDI != null ? scoreDI : score);
                } else {
                    xlsData.add(score);
                }
                //履修単位と修得単位
                if ("9".equals(_param._semester)) {
                    xlsData.add(rsXls.getString("COMP_CREDIT"));
                    xlsData.add(rsXls.getString("GET_CREDIT"));
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

    protected List getHeadData() {
        final List retList = new ArrayList();
        retList.add("※年度");
        retList.add("※学期");
        retList.add("※テスト種別コード");
        retList.add("※テスト項目コード");
        retList.add("※区分（1:素点、2:評価or評定）");
        retList.add("テスト種別名称");
        if ("1".equals(_param._useCurriculumcd)) {
            retList.add("※教科コード");
            retList.add("※学校種別");
            retList.add("※教育課程コード");
        }
        retList.add("※科目コード");
        retList.add("科目名称");
        retList.add("※講座コード");
        retList.add("講座名称");
        retList.add("※講座基準日");
        retList.add("年組番");
        retList.add("氏名");
        retList.add("※学籍番号");
        retList.add("成績");
        //履修単位と修得単位
        if ("9".equals(_param._semester)) {
            retList.add("履修単位");
            retList.add("修得単位");
        }
        return retList;
    }

    protected String[] getCols() {
        final String[] cols;
        if ("1".equals(_param._useCurriculumcd)) {
            cols = new String[]{
                    "YEAR",
                    "SEMESTER",
                    "TESTKINDCD",
                    "TESTITEMCD",
                    "SCORE_DIV",
                    "TESTITEMNAME",
                    "CLASSCD",
                    "SCHOOL_KIND",
                    "CURRICULUM_CD",
                    "SUBCLASSCD",
                    "SUBCLASSNAME",
                    "CHAIRCD",
                    "CHAIRNAME",
                    "CHAIR_DATE",
                    "HR_NAME_NO",
                    "NAME",
                    "SCHREGNO",};
        } else {
            cols = new String[]{
                    "YEAR",
                    "SEMESTER",
                    "TESTKINDCD",
                    "TESTITEMCD",
                    "SCORE_DIV",
                    "TESTITEMNAME",
                    "SUBCLASSCD",
                    "SUBCLASSNAME",
                    "CHAIRCD",
                    "CHAIRNAME",
                    "CHAIR_DATE",
                    "HR_NAME_NO",
                    "NAME",
                    "SCHREGNO",};
        }
        return cols;
    }

    protected String getSql() {
        final StringBuffer stb = new StringBuffer();

        //名簿
        stb.append(" WITH T_CHAIR AS ( ");
        stb.append("     SELECT ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         T1.CLASSCD, ");
            stb.append("         T1.SCHOOL_KIND, ");
            stb.append("         T1.CURRICULUM_CD, ");
        }
        stb.append("         T1.SUBCLASSCD, ");
        stb.append("         MIN(T2.CHAIRCD) AS CHAIRCD, ");
        stb.append("         T2.SCHREGNO ");
        stb.append("     FROM ");
        stb.append("         CHAIR_STD_DAT T2 ");
        stb.append("         INNER JOIN CHAIR_DAT T1 ");
        stb.append("              ON T2.YEAR     = T1.YEAR ");
        stb.append("             AND T2.SEMESTER = T1.SEMESTER ");
        stb.append("             AND T2.CHAIRCD  = T1.CHAIRCD ");
        stb.append("     WHERE ");
        stb.append("             T2.YEAR     = '" + _param._year + "' ");
        stb.append("         AND T2.SEMESTER = '" + _param.getSetSeme() + "' ");
        stb.append("         AND T2.CHAIRCD IN (" + _param.getInState() + ") ");
        stb.append("         AND DATE('" + _param._chairDate.replace('/', '-') + "') BETWEEN T2.APPDATE AND T2.APPENDDATE ");
        stb.append("     GROUP BY ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         T1.CLASSCD, ");
            stb.append("         T1.SCHOOL_KIND, ");
            stb.append("         T1.CURRICULUM_CD, ");
        }
        stb.append("         T1.SUBCLASSCD, ");
        stb.append("         T2.SCHREGNO ");
        stb.append("     ) ");

        //メイン
        stb.append(" SELECT ");
        stb.append("     '" + _param._year + "' AS YEAR, ");
        stb.append("     '" + _param._semester + "' AS SEMESTER, ");
        stb.append("     '" + _param._testKindCd + "' AS TESTKINDCD, ");
        stb.append("     '" + _param._testItemCd + "' AS TESTITEMCD, ");
        stb.append("     '" + _param._testScoreDiv + "' AS SCORE_DIV, ");
        stb.append("     '" + _param._testName + "' AS TESTITEMNAME, ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         T1.CLASSCD, ");
            stb.append("         T1.SCHOOL_KIND, ");
            stb.append("         T1.CURRICULUM_CD, ");
        }
        stb.append("     T1.SUBCLASSCD, ");
        stb.append("     L5.SUBCLASSNAME, ");
        stb.append("     T1.CHAIRCD, ");
        stb.append("     L6.CHAIRNAME, ");
        stb.append("     '" + _param._chairDate + "' AS CHAIR_DATE, ");
        stb.append("     L4.HR_NAME || '-' || right(L7.ATTENDNO, 2) || '番' AS HR_NAME_NO, ");
        stb.append("     L8.NAME, ");
        stb.append("     T1.SCHREGNO, ");
        //素点
        stb.append("     L1.SEM1_INTR_SCORE, ");
        stb.append("     L1.SEM1_TERM_SCORE, ");
        stb.append("     L1.SEM1_TERM2_SCORE, ");
        stb.append("     L1.SEM2_INTR_SCORE, ");
        stb.append("     L1.SEM2_TERM_SCORE, ");
        stb.append("     L1.SEM2_TERM2_SCORE, ");
        stb.append("     L1.SEM3_INTR_SCORE, ");
        stb.append("     L1.SEM3_TERM_SCORE, ");
        //評価・評定
        stb.append("     L1.SEM1_INTR_VALUE, ");
        stb.append("     L1.SEM1_TERM_VALUE, ");
        stb.append("     L1.SEM1_TERM2_VALUE, ");
        stb.append("     L1.SEM1_VALUE, ");
        stb.append("     L1.SEM2_INTR_VALUE, ");
        stb.append("     L1.SEM2_TERM_VALUE, ");
        stb.append("     L1.SEM2_TERM2_VALUE, ");
        stb.append("     L1.SEM2_VALUE, ");
        stb.append("     L1.SEM3_INTR_VALUE, ");
        stb.append("     L1.SEM3_TERM_VALUE, ");
        stb.append("     L1.SEM3_VALUE, ");
        stb.append("     L1.GRAD_VALUE, ");
        //出欠記号
        stb.append("     L1.SEM1_INTR_VALUE_DI, ");
        stb.append("     L1.SEM1_TERM_VALUE_DI, ");
        stb.append("     L1.SEM1_TERM2_VALUE_DI, ");
        stb.append("     L1.SEM1_VALUE_DI, ");
        stb.append("     L1.SEM2_INTR_VALUE_DI, ");
        stb.append("     L1.SEM2_TERM_VALUE_DI, ");
        stb.append("     L1.SEM2_TERM2_VALUE_DI, ");
        stb.append("     L1.SEM2_VALUE_DI, ");
        stb.append("     L1.SEM3_INTR_VALUE_DI, ");
        stb.append("     L1.SEM3_TERM_VALUE_DI, ");
        stb.append("     L1.SEM3_VALUE_DI, ");
        stb.append("     L1.GRAD_VALUE_DI ");
        //履修単位と修得単位
        if ("9".equals(_param._semester)) {
            stb.append("    ,L1.COMP_CREDIT ");
            stb.append("    ,L1.GET_CREDIT ");
        }
        stb.append(" FROM ");
        stb.append("     T_CHAIR T1 ");
        stb.append(" INNER JOIN ");
        stb.append("     SCHREG_BASE_MST L8 ON L8.SCHREGNO = T1.SCHREGNO ");
        stb.append(" INNER JOIN ");
        stb.append("     SCHREG_REGD_DAT L7 ON  L7.SCHREGNO = T1.SCHREGNO ");
        stb.append("                        AND L7.YEAR     = '" + _param._year + "' ");
        stb.append("                        AND L7.SEMESTER = '" + _param.getSetSeme() + "' ");
        stb.append(" LEFT JOIN ");
        stb.append("     SCHREG_REGD_HDAT L4 ON  L4.YEAR     = L7.YEAR ");
        stb.append("                         AND L4.SEMESTER = L7.SEMESTER ");
        stb.append("                         AND L4.GRADE    = L7.GRADE ");
        stb.append("                         AND L4.HR_CLASS = L7.HR_CLASS ");
        stb.append(" LEFT JOIN ");
        stb.append("     RECORD_DAT L1 ON  L1.YEAR       = '" + _param._year + "' ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("            AND L1.CLASSCD = T1.CLASSCD ");
            stb.append("            AND L1.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("            AND L1.CURRICULUM_CD = T1.CURRICULUM_CD ");
        }
        stb.append("                   AND L1.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("                   AND L1.SCHREGNO   = T1.SCHREGNO ");
        stb.append(" LEFT JOIN ");
        stb.append("     SUBCLASS_MST L5 ON L5.SUBCLASSCD = T1.SUBCLASSCD ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("            AND L5.CLASSCD = T1.CLASSCD ");
            stb.append("            AND L5.SCHOOL_KIND = T1.SCHOOL_KIND ");
            stb.append("            AND L5.CURRICULUM_CD = T1.CURRICULUM_CD ");
        }
        stb.append(" LEFT JOIN ");
        stb.append("     CHAIR_DAT L6 ON L6.YEAR     = '" + _param._year + "' ");
        stb.append("                 AND L6.SEMESTER = '" + _param.getSetSeme() + "' ");
        stb.append("                 AND L6.CHAIRCD  = T1.CHAIRCD ");
        stb.append(" ORDER BY ");
        if ("1".equals(_param._useCurriculumcd)) {
            stb.append("         T1.CLASSCD, ");
            stb.append("         T1.SCHOOL_KIND, ");
            stb.append("         T1.CURRICULUM_CD, ");
        }
        stb.append("     T1.SUBCLASSCD, ");
        stb.append("     T1.CHAIRCD, ");
        stb.append("     L7.GRADE, ");
        stb.append("     L7.HR_CLASS, ");
        stb.append("     L7.ATTENDNO, ");
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
        private final String _year;
        private final String _semester;
        private final String _chairDate;
        private final String _testCd;
        private final String _testKindCd;
        private final String _testItemCd;
        private final String _testScoreDiv;
        private final String _selectData;
        private final String[] _selectDatas;
        private final String _tableName;
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final boolean _header;
        private final String _templatePath;
        private final String _testName;
        private final String _maxSemester;
        private final String _useCurriculumcd;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("OUT_YEAR");
            _semester = request.getParameter("OUT_SEMESTER");
            _testCd = request.getParameter("OUT_TESTKIND");
            final String[] test = StringUtils.split(_testCd, "-");
            _testKindCd = test[0];
            _testItemCd = test[1];
            _testScoreDiv = test[2];
            _selectData = request.getParameter("selectdataChaircd");
            _chairDate = request.getParameter("CHAIR_DATE");
            final String useTestCountflg = request.getParameter("useTestCountflg");
            _tableName = null != useTestCountflg && !"".equals(useTestCountflg) ? useTestCountflg : "TESTITEM_MST_COUNTFLG_NEW";
            _selectDatas = StringUtils.split(_selectData, ",");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _header = request.getParameter("HEADER") == null ? false : true;
            _templatePath = request.getParameter("TEMPLATE_PATH");
            _testName = getTestName();
            _maxSemester = getMaxSeme();
            _useCurriculumcd = request.getParameter("useCurriculumcd");
        }

        private String getTestName() throws SQLException {
            String testname = "";

            final String testNameSql = getTestNameSql();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = _db2.prepareStatement(testNameSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    testname = rs.getString("LABEL");
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                _db2.commit();
            }

            return testname;
        }

        private String getTestNameSql() {
            final StringBuffer stb = new StringBuffer();

            //テスト種別
            stb.append(" WITH T_TEST AS ( ");
            stb.append("     SELECT ");
            stb.append("         TESTITEMNAME, ");
            stb.append("         TESTKINDCD, ");
            stb.append("         TESTITEMCD ");
            stb.append("     FROM ");
            stb.append(_tableName);
            stb.append("     WHERE ");
            stb.append("         YEAR = '" + _year + "' ");
            if ("TESTITEM_MST_COUNTFLG_NEW".equals(_tableName)) {
                stb.append("     AND SEMESTER = '" + _semester + "' ");
            }
            stb.append("         AND TESTKINDCD || TESTITEMCD IN ('0101','0201','0202','9900') ");
            stb.append(" ) ");

            stb.append(" , T_TEST_ALL AS ( ");
            //考査素点
            stb.append(" SELECT ");
            stb.append("     TESTITEMNAME AS LABEL, ");
            stb.append("     TESTKINDCD || '-' || TESTITEMCD || '-' || '1' AS VALUE ");
            stb.append(" FROM ");
            stb.append("     T_TEST ");
            stb.append(" WHERE ");
            stb.append("     TESTKINDCD IN ('01','02') ");
            //考査評価
            stb.append(" UNION ALL ");
            stb.append(" SELECT ");
            stb.append("     TESTITEMNAME || '評価' AS LABEL, ");
            stb.append("     TESTKINDCD || '-' || TESTITEMCD || '-' || '2' AS VALUE ");
            stb.append(" FROM ");
            stb.append("     T_TEST ");
            stb.append(" WHERE ");
            stb.append("     TESTKINDCD IN ('01','02') ");
            //学期評価・学年評定
            stb.append(" UNION ALL ");
            stb.append(" SELECT ");
            stb.append("     TESTITEMNAME AS LABEL, ");
            stb.append("     TESTKINDCD || '-' || TESTITEMCD || '-' || '2' AS VALUE ");
            stb.append(" FROM ");
            stb.append("     T_TEST ");
            stb.append(" WHERE ");
            stb.append("     TESTKINDCD IN ('99') ");
            stb.append(" ) ");

            stb.append(" SELECT ");
            stb.append("     LABEL ");
            stb.append(" FROM ");
            stb.append("     T_TEST_ALL ");
            stb.append(" WHERE ");
            stb.append("     VALUE = '" + _testCd + "' ");
            stb.append(" ORDER BY ");
            stb.append("     VALUE ");

            return stb.toString();
        }

        private String getMaxSeme() throws SQLException {
            String maxSeme = "";

            final String sql = "SELECT MAX(SEMESTER) AS MAX_SEMESTER FROM SEMESTER_MST WHERE YEAR = '" + _year + "' AND SEMESTER < '9'";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = _db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    maxSeme = rs.getString("MAX_SEMESTER");
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                _db2.commit();
            }

            return maxSeme;
        }

        private String getSemeGakunenmatu() {
            return Integer.parseInt(_year) < Integer.parseInt(_ctrlYear) ? _maxSemester : _ctrlSemester;
        }

        private String getSetSeme() {
            return !"9".equals(_semester) ? _semester : getSemeGakunenmatu();
        }

        private String getInState() {
            final StringBuffer stb = new StringBuffer();
            String sep = "";
            for (int i = 0; i < _selectDatas.length; i++) {
                stb.append(sep + "'" + _selectDatas[i] + "'");
                sep = ",";
            }
            return stb.toString();
        }

        private String getScoreField() {
            final StringBuffer stb = new StringBuffer();
            if ("9".equals(_semester)) {
                stb.append("GRAD_VALUE");
            } else {
                //学期
                stb.append("SEM" + _semester);
                //テスト種別
                if ("01".equals(_testKindCd) && "01".equals(_testItemCd)) stb.append("_INTR");
                if ("02".equals(_testKindCd) && "01".equals(_testItemCd)) stb.append("_TERM");
                if ("02".equals(_testKindCd) && "02".equals(_testItemCd)) stb.append("_TERM2");
                //区分（1:素点、2:評価or評定）
                if ("1".equals(_testScoreDiv)) stb.append("_SCORE");
                if ("2".equals(_testScoreDiv)) stb.append("_VALUE");
            }
            return stb.toString();
        }

        /**
         * XXX_DI のフィールドを参照するか？
         */
        private boolean isShowDi() {
            return "2".equals(_testScoreDiv);
        }

    }
}

// eof
