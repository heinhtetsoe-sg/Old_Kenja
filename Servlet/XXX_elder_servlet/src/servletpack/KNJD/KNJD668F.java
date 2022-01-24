/*
 * $Id: 177acf1736992f768463aea2b7f898284a5f5fa7 $
 *
 * 作成日: 2015/12/22
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;


import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

public class KNJD668F {

    private static final Log log = LogFactory.getLog(KNJD668F.class);

    private boolean _hasData;
    private static final String ALL3KA = "333333";
    private static final String ALL5KA = "555555";
    private static final String ALL9KA = "999999";

    private Param _param;

    /**
     * @param request リクエスト
     * @param response レスポンス
     */
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
            ) throws Exception {

        final Vrw32alp svf = new Vrw32alp();
        DB2UDB db2 = null;
        try {
            response.setContentType("application/pdf");

            svf.VrInit();
            svf.VrSetSpoolFileStream(response.getOutputStream());

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);

            _hasData = false;

            printMain(db2, svf);
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {

            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }

            if (null != db2) {
                db2.commit();
                db2.close();
            }
            svf.VrQuit();
        }

    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        final String nendo = KNJ_EditDate.getAutoFormatYear(db2, Integer.parseInt(_param._year)) + "年度";
        final List subclassList = getSubclassList(db2);
        for (Iterator iterator = subclassList.iterator(); iterator.hasNext();) {
            final Subclass subclass = (Subclass) iterator.next();
            
            final String staffname = getStaffname(db2, subclass);

            for (Iterator itPage = subclass._pageMap.keySet().iterator(); itPage.hasNext();) {

                svf.VrSetForm("KNJD668F.frm", 4);
				String setTitle = nendo;
                setTitle += _param._testName;
                setTitle += subclass._subclassname;
                svf.VrsOut("TITLE", setTitle);

                final String key = (String) itPage.next();
                final Map hrMap = (Map) subclass._pageMap.get(key);

                final HrClass firstHr = (HrClass) hrMap.get("1");
                final HrClass secondHr = (HrClass) hrMap.get("2");
                if (!ALL3KA.equals(subclass._subclasscd) && !ALL5KA.equals(subclass._subclasscd) && !ALL9KA.equals(subclass._subclasscd)) {
                    svf.VrsOut("TEACHER", "教科担当：" + staffname);
                }
                svf.VrsOut("CHAIR_NAME1", firstHr._hrName);
                if (null != secondHr) {
                    svf.VrsOut("CHAIR_NAME2", secondHr._hrName);
                }
                for (Iterator itRange = _param._scoreRangeList.iterator(); itRange.hasNext();) {
                    ScoreRange scoreRange = (ScoreRange) itRange.next();
                    final Map firstRangeMap = (Map) firstHr._scoreMap.get(scoreRange._score);
                    final Map secondRangeMap;
                    if (null != secondHr) {
                        secondRangeMap = (Map) secondHr._scoreMap.get(scoreRange._score);
                    } else {
                        secondRangeMap = new HashMap();
                    }

                    final int firstStdCnt = firstRangeMap.size();
                    final int secondStdCnt = secondRangeMap.size();
                    final int maxCnt = firstStdCnt >= secondStdCnt ? firstStdCnt : secondStdCnt == 0 ? 1 : secondStdCnt;
                    int fieldNo = 1;
                    for (int i = 1; i <= maxCnt; i++) {
                        final List firstPrintStd = (List) firstRangeMap.get(String.valueOf(i));
                        final List secondPrintStd = (List) secondRangeMap.get(String.valueOf(i));
                        if (i == maxCnt) {
                            if ("3".equals(scoreRange._fieldDiv)) {
                                fieldNo = 7;
                            } else {
                                svf.VrsOut("SCORE", scoreRange._score);
                                fieldNo = 1;
                            }
                        } else {
                            fieldNo = 4;
                        }
                        int setFieldNo = fieldNo;
                        if (null != firstPrintStd) {
                            for (Iterator itFirst = firstPrintStd.iterator(); itFirst.hasNext();) {
                                final PrintStuedent printStuedent = (PrintStuedent) itFirst.next();
                                svf.VrsOut("NAME1_" + setFieldNo, printStuedent.getStdName());
                                setFieldNo++;
                            }
                        }
                        if (null != secondPrintStd) {
                            setFieldNo = fieldNo;
                            for (Iterator itFirst = secondPrintStd.iterator(); itFirst.hasNext();) {
                                final PrintStuedent printStuedent = (PrintStuedent) itFirst.next();
                                svf.VrsOut("NAME2_" + setFieldNo, printStuedent.getStdName());
                                setFieldNo++;
                            }
                        }
                        if (null != firstPrintStd && 0 == firstPrintStd.size() && (null == secondPrintStd || (null != secondPrintStd && 0 == secondPrintStd.size()))) {
                            svf.VrsOut("NAME1_" + setFieldNo, "\n");
                            svf.VrsOut("NAME2_" + setFieldNo, "\n");
                        }
                        _hasData = true;
                        svf.VrEndRecord();
                    }
                }
                svf.VrsOut("CLASS_AVE1", firstHr._kenjouAvg + "(" + firstHr._allAvg + ")");
                if (null != secondHr) {
                    svf.VrsOut("CLASS_AVE2", secondHr._kenjouAvg + "(" + secondHr._allAvg + ")");
                }
                svf.VrEndRecord();

                svf.VrsOut("GRADE_AVE", subclass._kenjouAvg + "(" + subclass._allAvg + ")");
                svf.VrEndRecord();
            }
        }
    }

    private List getSubclassList(final DB2UDB db2) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;

        final List retList = new ArrayList();
        try {
            String sql = "";
            for (int j = 0; j < _param._categorySelected.length; j++) {
                final String subclassCd = _param._categorySelected[j].substring(7);
                if (ALL3KA.equals(subclassCd) || ALL5KA.equals(subclassCd) || ALL9KA.equals(subclassCd)) {
                    final String classcd        = "";
                    final String schoolKind     = "";
                    final String curriculumcd   = "";
                    final String subclasscd     = subclassCd;
                    final String subclassname   = ALL3KA.equals(subclassCd) ? "3科合計" : ALL5KA.equals(subclassCd) ? "5科合計" : "総合計";
                    final String subclassabbv   = ALL3KA.equals(subclassCd) ? "3科合計" : ALL5KA.equals(subclassCd) ? "5科合計" : "総合計";
                    final Subclass subclass = new Subclass(classcd, schoolKind, curriculumcd, subclasscd, subclassname, subclassabbv, db2, _param._year, _param._semester, _param._grade, _param._testcd, _param);
                    retList.add(subclass);
                } else {
                    sql = "SELECT * FROM SUBCLASS_MST WHERE CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '" + _param._categorySelected[j] + "' ";
                    ps = db2.prepareStatement(sql);
                    rs = ps.executeQuery();
                    if (rs.next()) {
                        final String classcd        = rs.getString("CLASSCD");
                        final String schoolKind     = rs.getString("SCHOOL_KIND");
                        final String curriculumcd   = rs.getString("CURRICULUM_CD");
                        final String subclasscd     = rs.getString("SUBCLASSCD");
                        final String subclassname   = rs.getString("SUBCLASSNAME");
                        final String subclassabbv   = rs.getString("SUBCLASSABBV");
                        final Subclass subclass = new Subclass(classcd, schoolKind, curriculumcd, subclasscd, subclassname, subclassabbv, db2, _param._year, _param._semester, _param._grade, _param._testcd, _param);
                        retList.add(subclass);
                    }
                }
            }
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }

        return retList;
    }

    private String scoreSql(final Subclass subclass, final ScoreRange scoreRange, final String hrClass) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     REGDFI.SCHREGNO, ");
        stb.append("     CASE WHEN REGD.SCHREGNO IS NOT NULL THEN '1' ELSE '0' END AS REGD_SCH_FLG, ");
        stb.append("     HDAT.HR_NAME, ");
        stb.append("     HDAT.HR_NAMEABBV, ");
        stb.append("     REGDFI.ATTENDNO, ");
        stb.append("     BASE.NAME, ");
        stb.append("     RANK.MAJOR_RANK, ");
        stb.append("     RANK.SCORE ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_FI_HDAT HDAT ");
        stb.append("     LEFT JOIN SCHREG_REGD_FI_DAT REGDFI ON HDAT.YEAR = REGDFI.YEAR ");
        stb.append("          AND HDAT.SEMESTER = REGDFI.SEMESTER ");
        stb.append("          AND HDAT.GRADE = REGDFI.GRADE ");
        stb.append("          AND HDAT.HR_CLASS = REGDFI.HR_CLASS ");
        stb.append("     LEFT JOIN SCHREG_REGD_DAT REGD ON REGDFI.YEAR = REGD.YEAR ");
        stb.append("          AND REGDFI.SEMESTER = REGD.SEMESTER ");
        stb.append("          AND REGDFI.GRADE = REGD.GRADE ");
        stb.append("          AND REGDFI.HR_CLASS = REGD.HR_CLASS ");
        stb.append("          AND REGDFI.SCHREGNO = REGD.SCHREGNO ");
        stb.append("     LEFT JOIN SCHREG_BASE_MST BASE ON REGDFI.SCHREGNO = BASE.SCHREGNO ");
        stb.append("     INNER JOIN RECORD_RANK_FI_SDIV_DAT RANK ON HDAT.YEAR = RANK.YEAR ");
        stb.append("           AND RANK.SEMESTER = '" + _param._semester + "' ");
        stb.append("           AND RANK.TESTKINDCD || RANK.TESTITEMCD || RANK.SCORE_DIV = '" + _param._testcd + "' ");
        if (!ALL3KA.equals(subclass._subclasscd) && !ALL5KA.equals(subclass._subclasscd) && !ALL9KA.equals(subclass._subclasscd)) {
            stb.append("           AND RANK.CLASSCD || RANK.SCHOOL_KIND || RANK.CURRICULUM_CD || RANK.SUBCLASSCD = '" + subclass._classcd + subclass._schoolKind + subclass._curriculum_cd + subclass._subclasscd + "' ");
        } else {
            stb.append("           AND RANK.SUBCLASSCD = '" + subclass._subclasscd + "' ");
        }
        stb.append("           AND REGDFI.SCHREGNO = RANK.SCHREGNO ");
        stb.append("           AND RANK.SCORE BETWEEN " + scoreRange._minScore + " AND " + scoreRange._maxScore + " ");
        stb.append(" WHERE ");
        stb.append("     HDAT.YEAR = '" + _param._year + "' ");
        stb.append("     AND HDAT.SEMESTER = '" + _param._regdSeme + "' ");
        stb.append("     AND HDAT.RECORD_DIV = '1' ");
        stb.append("     AND HDAT.GRADE = '" + _param._grade + "' ");
        stb.append("     AND HDAT.HR_CLASS = '" + hrClass + "' ");
        stb.append(" ORDER BY ");
        stb.append("     HDAT.HR_CLASS, ");
        stb.append("     RANK.SCORE DESC, ");
        stb.append("     REGDFI.ATTENDNO, ");
        stb.append("     RANK.MAJOR_RANK ");
        return stb.toString();
    }
    
    private String staffSql(final Subclass subclass) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT DISTINCT ");
        stb.append("     CSTF.STAFFCD, ");
        stb.append("     STF.STAFFNAME ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_FI_HDAT HDAT ");
        stb.append("     LEFT JOIN SCHREG_REGD_FI_DAT REGDFI ON HDAT.YEAR = REGDFI.YEAR ");
        stb.append("          AND HDAT.SEMESTER = REGDFI.SEMESTER ");
        stb.append("          AND HDAT.GRADE = REGDFI.GRADE ");
        stb.append("          AND HDAT.HR_CLASS = REGDFI.HR_CLASS ");
        stb.append("     INNER JOIN CHAIR_STD_DAT CSTD ON CSTD.YEAR = REGDFI.YEAR ");
        stb.append("          AND CSTD.SEMESTER = REGDFI.SEMESTER ");
        stb.append("          AND CSTD.SCHREGNO = REGDFI.SCHREGNO ");
        stb.append("     INNER JOIN CHAIR_DAT CHR ON CSTD.YEAR = CHR.YEAR ");
        stb.append("          AND CSTD.SEMESTER = CHR.SEMESTER ");
        stb.append("          AND CSTD.CHAIRCD = CHR.CHAIRCD ");
        stb.append("          AND CHR.CLASSCD || CHR.SCHOOL_KIND || CHR.CURRICULUM_CD || CHR.SUBCLASSCD = '" + subclass._classcd + subclass._schoolKind + subclass._curriculum_cd + subclass._subclasscd + "' ");
        stb.append("     INNER JOIN CHAIR_STF_DAT CSTF ON CSTF.YEAR = CHR.YEAR ");
        stb.append("          AND CSTF.SEMESTER = CHR.SEMESTER ");
        stb.append("          AND CSTF.CHAIRCD = CHR.CHAIRCD ");
        stb.append("     INNER JOIN STAFF_MST STF ON STF.STAFFCD = CSTF.STAFFCD ");
        stb.append(" WHERE ");
        stb.append("     HDAT.YEAR = '" + _param._year + "' ");
        stb.append("     AND HDAT.SEMESTER = '" + _param._regdSeme + "' ");
        stb.append("     AND HDAT.RECORD_DIV = '1' ");
        stb.append("     AND HDAT.GRADE = '" + _param._grade + "' ");
        stb.append(" ORDER BY ");
        stb.append("     STAFFCD ");
        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 67772 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _semester;
        private final String _grade;
        private final String _schoolKind;
        private final String _testcd;
        private final String[] _categorySelected;
        private final String _year;
        private final String _ctrlSeme;
        private final String _regdSeme;
        private final String _loginDate;
        private final String _prgid;
        private final String _printLogStaffcd;
        private final String _semesterName;
        private final String _gradeName;
        private final String _testName;
        private final List _scoreRangeList;
        private Map _lastNameMap;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _semester           = request.getParameter("SEMESTER");
            _grade              = request.getParameter("GRADE");
            _testcd             = request.getParameter("TESTCD");
            _categorySelected   = request.getParameterValues("CATEGORY_SELECTED");
            _year               = request.getParameter("YEAR");
            _ctrlSeme           = request.getParameter("CTRL_SEME");
            _regdSeme           = "9".equals(_semester) ? _ctrlSeme : _semester;
            _loginDate          = request.getParameter("LOGIN_DATE");
            _prgid              = request.getParameter("PRGID");
            _printLogStaffcd    = request.getParameter("PRINT_LOG_STAFFCD");
            _semesterName = getSemesterName1(db2);
            _gradeName = getGradeName(db2);
            _testName = getTestName(db2);
            _schoolKind = getSchoolKind(db2);
            _scoreRangeList = getScoreRangeList();
            _lastNameMap = new HashMap();
        }

        private String getSchoolKind(final DB2UDB db2) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;

            String retstr = "";
            try {
                final String sql = "SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND GRADE = '" + _grade + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    retstr = rs.getString("SCHOOL_KIND");
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retstr;
        }

        private String getSemesterName1(final DB2UDB db2) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;

            String retstr = "";
            try {
                final String sql = "SELECT SEMESTERNAME FROM SEMESTER_MST WHERE YEAR = '" + _year + "' AND SEMESTER = '" + _semester + "' ORDER BY SEMESTER ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    retstr = rs.getString("SEMESTERNAME");
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retstr;
        }

        private String getGradeName(final DB2UDB db2) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;

            String retstr = "";
            try {
                final String sql = "SELECT GRADE_NAME1 FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND GRADE = '" + _grade + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    retstr = rs.getString("GRADE_NAME1");
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retstr;
        }

        private String getTestName(final DB2UDB db2) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;

            String retstr = "";
            try {
                final String sql = "SELECT TESTITEMNAME FROM TESTITEM_MST_COUNTFLG_NEW_SDIV WHERE YEAR = '" + _year + "' AND SEMESTER = '" + _semester + "' AND TESTKINDCD || TESTITEMCD || SCORE_DIV = '" + _testcd + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    retstr = rs.getString("TESTITEMNAME");
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retstr;
        }

        private List getScoreRangeList() throws SQLException {
            final List retList = new ArrayList();
            retList.add(new ScoreRange("100", 100, 100, "1"));
            retList.add(new ScoreRange("95", 95, 99, "3"));
            retList.add(new ScoreRange("90", 90, 94, "1"));
            retList.add(new ScoreRange("85", 85, 89, "3"));
            retList.add(new ScoreRange("80", 80, 84, "1"));
            retList.add(new ScoreRange("75", 75, 79, "3"));
            retList.add(new ScoreRange("70", 70, 74, "1"));
            retList.add(new ScoreRange("65", 65, 69, "3"));
            retList.add(new ScoreRange("60", 60, 64, "1"));
            retList.add(new ScoreRange("55", 55, 59, "3"));
            retList.add(new ScoreRange("50", 50, 54, "1"));
            retList.add(new ScoreRange("45", 45, 49, "3"));
            retList.add(new ScoreRange("40", 40, 44, "1"));
            retList.add(new ScoreRange("35", 35, 39, "3"));
            retList.add(new ScoreRange("30", 30, 34, "1"));
            retList.add(new ScoreRange("25", 25, 29, "3"));
            retList.add(new ScoreRange("20", 20, 24, "1"));
            retList.add(new ScoreRange("15", 15, 19, "3"));
            retList.add(new ScoreRange("10", 10, 14, "1"));
            retList.add(new ScoreRange("5", 5, 9, "3"));
            retList.add(new ScoreRange("0", 0, 4, "1"));
            return retList;
        }

    }

    private class ScoreRange {
        final String _score;
        final int _minScore;
        final int _maxScore;
        final String _fieldDiv;

        public ScoreRange(
                final String score,
                final int minScore,
                final int maxScore,
                final String fieldDiv
        ) throws SQLException {
            _score = score;
            _minScore = minScore;
            _maxScore = maxScore;
            _fieldDiv = fieldDiv;
        }

    }

    private class Subclass {
        final String _classcd;
        final String _schoolKind;
        final String _curriculum_cd;
        final String _subclasscd;
        final String _subclassname;
        final String _subclassabbv;
        final String _kenjouAvg;
        final String _allAvg;
        final Map _pageMap;

        public Subclass(
                final String classcd,
                final String schoolKind,
                final String curriculumcd,
                final String subclasscd,
                final String subclassname,
                final String subclassabbv,
                final DB2UDB db2,
                final String year,
                final String semester,
                final String grade,
                final String testCd,
                final Param param
        ) throws SQLException {
            _classcd        = classcd;
            _schoolKind     = schoolKind;
            _curriculum_cd  = curriculumcd;
            _subclasscd     = subclasscd;
            _subclassname   = subclassname;
            _subclassabbv   = subclassabbv;
            _kenjouAvg = getKenjouAvg(db2, param, year, semester, grade, testCd, null);
            _allAvg = getAllAvg(db2, year, semester, grade, testCd, "1", "000", "000");
            _pageMap = getPageMap(db2, year, semester, grade, testCd, param);
        }

        private String getKenjouAvg(
                final DB2UDB db2,
                final Param param,
                final String year,
                final String semester,
                final String grade,
                final String testCd,
                final String hrClass
        ) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;

            String retstr = "";
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     T1.HR_CLASS, T1.AVG, T1.SCORE, T1.COUNT ");
                if (null == hrClass) {
                    stb.append("         , T2.RECORD_DIV ");
                }
                stb.append(" FROM ");
                stb.append("     RECORD_AVERAGE_SDIV_DAT T1 ");
                if (null == hrClass) {
                    stb.append("     INNER JOIN SCHREG_REGD_FI_HDAT T2 ON T2.YEAR = T1.YEAR ");
                    stb.append("         AND T2.SEMESTER = '" + param._regdSeme + "' ");
                    stb.append("         AND T2.GRADE = T1.GRADE ");
                    stb.append("         AND T2.HR_CLASS = T1.HR_CLASS ");
                }
                stb.append(" WHERE ");
                stb.append("     T1.YEAR = '" + year + "' ");
                stb.append("     AND T1.SEMESTER = '" + semester + "' ");
                stb.append("     AND T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '" + testCd + "' ");
                if (!ALL3KA.equals(_subclasscd) && !ALL5KA.equals(_subclasscd) && !ALL9KA.equals(_subclasscd)) {
                    stb.append("     AND T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD ||  T1.SUBCLASSCD = '" + _classcd + _schoolKind + _curriculum_cd + _subclasscd + "' ");
                } else {
                    stb.append("     AND T1.SUBCLASSCD = '" + _subclasscd + "' ");
                }
                stb.append("     AND T1.AVG_DIV = '2' ");
                stb.append("     AND T1.GRADE = '" + grade + "' ");
                if (null != hrClass) {
                    stb.append("     AND T1.HR_CLASS = '" + hrClass + "' ");
                }
                stb.append("     AND T1.COURSECD = '0' ");
                stb.append("     AND T1.MAJORCD = '000' ");
                stb.append("     AND T1.COURSECODE = '0000' ");

                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                if (null != hrClass) {
                    if (rs.next()) {
                        retstr = null == rs.getString("AVG") ? "" : rs.getBigDecimal("AVG").setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                    }
                } else {
                    BigDecimal scoreTotal = new BigDecimal(0);
                    BigDecimal countTotal = new BigDecimal(0);
                    while (rs.next()) {
                        final boolean isKenjou = "1".equals(rs.getString("RECORD_DIV")); // 健常
                        log.info(" kenjou avg "  + rs.getString("HR_CLASS") + "(" + isKenjou + ") : score = " + rs.getString("SCORE") + ", count = " + rs.getString("COUNT"));
                        if (isKenjou) {
                            if (null != rs.getBigDecimal("SCORE")) {
                                scoreTotal = scoreTotal.add(rs.getBigDecimal("SCORE"));
                            }
                            if (null != rs.getBigDecimal("COUNT")) {
                                countTotal = countTotal.add(rs.getBigDecimal("COUNT"));
                            }
                        }
                    }
                    if (countTotal.intValue() == 0) {
                        retstr = "";
                    } else {
                        retstr = scoreTotal.divide(countTotal, 1, BigDecimal.ROUND_HALF_UP).toString();
                        log.info(" retstr = "  + retstr);
                    }
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retstr;
        }

        private String getAllAvg(
                final DB2UDB db2,
                final String year,
                final String semester,
                final String grade,
                final String testCd,
                final String avgDiv,
                final String hrClass,
                final String majorCd
        ) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;

            String retstr = "";
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     T1.AVG ");
                stb.append(" FROM ");
                stb.append("     RECORD_AVERAGE_FI_SDIV_DAT T1 ");
                stb.append(" WHERE ");
                stb.append("     T1.YEAR = '" + year + "' ");
                stb.append("     AND T1.SEMESTER = '" + semester + "' ");
                stb.append("     AND T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '" + testCd + "' ");
                if (!ALL3KA.equals(_subclasscd) && !ALL5KA.equals(_subclasscd) && !ALL9KA.equals(_subclasscd)) {
                    stb.append("     AND T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD ||  T1.SUBCLASSCD = '" + _classcd + _schoolKind + _curriculum_cd + _subclasscd + "' ");
                } else {
                    stb.append("     AND T1.SUBCLASSCD = '" + _subclasscd + "' ");
                }
                stb.append("     AND T1.AVG_DIV = '" + avgDiv + "' ");
                stb.append("     AND T1.GRADE = '" + grade + "' ");
                stb.append("     AND T1.HR_CLASS = '" + hrClass + "' ");
                stb.append("     AND T1.COURSECD = '0' ");
                stb.append("     AND T1.MAJORCD = '" + majorCd + "' ");
                stb.append("     AND T1.COURSECODE = '0000' ");

                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    retstr = null == rs.getString("AVG") ? "" : rs.getBigDecimal("AVG").setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retstr;
        }

        private Map getPageMap(
                final DB2UDB db2,
                final String year,
                final String semester,
                final String grade,
                final String testCd,
                final Param param
        ) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;

            final Map retMap = new TreeMap();
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     T1.HR_CLASS, ");
                stb.append("     T1.HR_NAME, ");
                stb.append("     T1.HR_NAMEABBV, ");
                stb.append("     L1.STAFFNAME ");
                stb.append(" FROM ");
                stb.append("     SCHREG_REGD_FI_HDAT T1 ");
                stb.append("     LEFT JOIN STAFF_MST L1 ON T1.TR_CD1 = L1.STAFFCD ");
                stb.append(" WHERE ");
                stb.append("     T1.YEAR = '" + year + "' ");
                stb.append("     AND T1.SEMESTER = '" + semester + "' ");
                stb.append("     AND T1.GRADE = '" + grade + "' ");
                stb.append("     AND T1.RECORD_DIV = '1' ");
                stb.append(" ORDER BY ");
                stb.append("     T1.HR_CLASS ");

                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                int cnt = 1;
                int pageCnt = 1;
                Map pageMap = new HashMap();
                while (rs.next()) {

                    if (cnt > 2) {
                        retMap.put(String.valueOf(pageCnt), pageMap);
                        pageMap = new HashMap();
                        cnt = 1;
                        pageCnt++;
                    }

                    final String hrClass = rs.getString("HR_CLASS");
                    final String hrName = rs.getString("HR_NAME");
                    final String hrNameabbv = rs.getString("HR_NAMEABBV");
                    final String staffName = rs.getString("STAFFNAME");
                    final String kenjouAvg = getKenjouAvg(db2, param, year, semester, grade, testCd, hrClass);
                    final String allAvg = getAllAvg(db2, year, semester, grade, testCd, "2", hrClass, "000");
                    final HrClass hrClass2 = new HrClass(hrClass, hrName, hrNameabbv, staffName, kenjouAvg, allAvg);
                    hrClass2.setScoreMap(db2, this);
                    pageMap.put(String.valueOf(cnt), hrClass2);
                    cnt++;
                }
                if (cnt > 1) {
                    retMap.put(String.valueOf(pageCnt), pageMap);
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retMap;
        }
    }

    private class HrClass {
        final String _hrClass;
        final String _hrName;
        final String _hrNameabbv;
        final String _staffName;
        final String _kenjouAvg;
        final String _allAvg;
        Map _scoreMap;

        public HrClass(
                final String hrClass,
                final String hrName,
                final String hrNameabbv,
                final String staffName,
                final String kenjouAvg,
                final String allAvg
        ) throws SQLException {
            _hrClass = hrClass;
            _hrName = hrName;
            _hrNameabbv = hrNameabbv;
            _staffName = staffName;
            _kenjouAvg = kenjouAvg;
            _allAvg = allAvg;
        }

        public void setScoreMap(
                final DB2UDB db2,
                final Subclass subclass
        ) throws SQLException {
            _scoreMap = new TreeMap();
            for (Iterator iterator = _param._scoreRangeList.iterator(); iterator.hasNext();) {
                final ScoreRange scoreRange = (ScoreRange) iterator.next();
                final Map rangeMap = new HashMap();

                PreparedStatement ps = null;
                ResultSet rs = null;

                try {
                    final String sql = scoreSql(subclass, scoreRange, _hrClass);

                    ps = db2.prepareStatement(sql);
                    rs = ps.executeQuery();
                    int cnt = 1;
                    int pageCnt = 1;
                    List stdList = new ArrayList();
                    while (rs.next()) {
                        final String schregno   = rs.getString("SCHREGNO");
                        final String regdSchFlg = rs.getString("REGD_SCH_FLG");
                        final String hrName     = rs.getString("HR_NAME");
                        final String hrNameabbv = rs.getString("HR_NAMEABBV");
                        final String attendno   = rs.getString("ATTENDNO");
                        final String name       = rs.getString("NAME");
                        final String majorRank  = rs.getString("MAJOR_RANK");
                        final String score      = rs.getString("SCORE");
                        final String[] nameArray = StringUtils.split(StringUtils.replace(name, " ", "　"), "　");
                        final String lastName = nameArray[0];
                        final String firstName = nameArray[1];
                        final String firstName1 = firstName.substring(0, 1);
                        if (_param._lastNameMap.containsKey(lastName)) {
                            _param._lastNameMap.put(lastName, "2");
                        } else {
                            _param._lastNameMap.put(lastName, "1");
                        }

                        final PrintStuedent printStuedent = new PrintStuedent(schregno, regdSchFlg, hrName, hrNameabbv, attendno, firstName, firstName1, lastName, majorRank, score);

                        if (cnt > 3) {
                            rangeMap.put(String.valueOf(pageCnt), stdList);
                            cnt = 1;
                            stdList = new ArrayList();
                            pageCnt++;
                        }
                        stdList.add(printStuedent);

                        cnt++;
                    }
                    rangeMap.put(String.valueOf(pageCnt), stdList);
                    _scoreMap.put(scoreRange._score, rangeMap);
                    _scoreMap.put(scoreRange._score, rangeMap);
                } finally {
                    db2.commit();
                    DbUtils.closeQuietly(null, ps, rs);
                }

            }
        }

    }
    
    public String getStaffname(
            final DB2UDB db2,
            final Subclass subclass
    ) throws SQLException {
        final StringBuffer staffname = new StringBuffer();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final String sql = staffSql(subclass);

            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                if (StringUtils.isBlank(rs.getString("STAFFNAME"))) {
                    continue;
                }
                if (staffname.length() > 0) {
                    staffname.append("　");
                }
                staffname.append(rs.getString("STAFFNAME"));
            }
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
        return staffname.toString();
    }

    private class PrintStuedent {
        final String _schregno;
        final String _regdSchFlg;
        final String _hrName;
        final String _hrNameabbv;
        final String _attendno;
        final String _firstName;
        final String _firstNameHead;
        final String _lastName;
        final String _majorRank;
        final String _score;

        public PrintStuedent(
                final String schregno,
                final String regdSchFlg,
                final String hrName,
                final String hrNameabbv,
                final String attendno,
                final String firstName,
                final String firstNameHead,
                final String lastName,
                final String majorRank,
                final String score
        ) {
            _schregno   = schregno;
            _regdSchFlg = regdSchFlg;
            _hrName     = hrName;
            _hrNameabbv = hrNameabbv;
            _attendno   = attendno;
            _firstName  = firstName;
            _firstNameHead = firstNameHead;
            _lastName   = lastName;
            _majorRank  = majorRank;
            _score      = score;
        }

        public String getStdName() {
            String retStr = "";
            if ("2".equals(_param._lastNameMap.get(_lastName))) {
                retStr = _lastName + _firstNameHead;
            } else {
                retStr = _lastName;
            }
            if ("0".equals(_regdSchFlg)) {
                retStr = "(" + retStr + ")";
            }
            return retStr + _score;
        }
    }
}

// eof

