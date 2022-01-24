/*
 * $Id: c691efe8dc3fa55285e4e74bddd39c3b522d2aa1 $
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
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

public class KNJD669F {

    private static final Log log = LogFactory.getLog(KNJD669F.class);

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

    private static List getMappedList(final Map map, final Object key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new ArrayList());
        }
        return (List) map.get(key1);
    }

    private static Map getMappedMap(final Map map, final Object key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new HashMap());
        }
        return (Map) map.get(key1);
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        final String nendo = KNJ_EditDate.getAutoFormatYear(db2, Integer.parseInt(_param._year)) + "年度";
        final List subclassList = getSubclassList(db2);

        for (final Iterator iterator = subclassList.iterator(); iterator.hasNext();) {
            final Subclass subclass = (Subclass) iterator.next();

            final List list = getList(db2, subclass);

            final Map chairPrintStudentListMap = getChairPrintStudentListMap(list);

            final Map printChairPage = getPrintChairPageMap(chairPrintStudentListMap, 3);

            for (final Iterator itPage = printChairPage.keySet().iterator(); itPage.hasNext();) {
                final Integer page = (Integer) itPage.next();

                final int studentPerLine = 2; // 1行あたり表示生徒数

                svf.VrSetForm("KNJD669F.frm", 4);
				String setTitle = nendo;
                setTitle += StringUtils.defaultString(_param._testName) + " ";
                setTitle += StringUtils.defaultString(subclass._subclassname);

                final List chaircdList = (List) printChairPage.get(page); // ページごとの表示対象講座コード

                final List scoreRangeList = _param.getScoreRangeList();
            
                for (final Iterator itRange = scoreRangeList.iterator(); itRange.hasNext();) {
                    final ScoreRange scoreRange = (ScoreRange) itRange.next();

                    svf.VrsOut("TITLE", setTitle);

                    final List[] chairStudentListInRange = new ArrayList[chaircdList.size()]; 
                    for (int chi = 0; chi < chaircdList.size(); chi++) {
                        chairStudentListInRange[chi] = new ArrayList();
                        final List chairStudentList = getMappedList(chairPrintStudentListMap, chaircdList.get(chi));
                        for (final Iterator itChairStudent = chairStudentList.iterator(); itChairStudent.hasNext();) {
                            final PrintStudent printStudent = (PrintStudent) itChairStudent.next();
                            if (!NumberUtils.isDigits(printStudent._score)) {
                                continue;
                            }
                            final int iScore = Integer.parseInt(printStudent._score);
                            if (scoreRange._minScore <= iScore && iScore <= scoreRange._maxScore) {
                                chairStudentListInRange[chi].add(printStudent);
                                //itChairStudent.remove();
                            }
                        }
                    }

                    int maxPrintLine = 1; // 最低1行は表示する
                    for (int chi = 0; chi < chaircdList.size(); chi++) {
                        maxPrintLine = Math.max(maxPrintLine, chairStudentListInRange[chi].size() / studentPerLine + (chairStudentListInRange[chi].size() % studentPerLine == 0 ? 0 : 1));
                    }
                    for (int printLineIdx = 0; printLineIdx < maxPrintLine; printLineIdx++) {
                        int nameStartIndex;
                        if (printLineIdx == maxPrintLine - 1) { // 最後の行
                            if (scoreRange._isTensen) { // 点数表示なし
                                nameStartIndex = 2 * studentPerLine + 1; // 点線
                            } else {
                                svf.VrsOut("SCORE", scoreRange._score);
                                nameStartIndex = 0 * studentPerLine + 1; // 実線
                            }
                        } else {
                            nameStartIndex = 1 * studentPerLine + 1; // 線なし
                        }
                        boolean printScore = false;
                        for (int chi = 0; chi < chaircdList.size(); chi++) {
                            final int from = studentPerLine * printLineIdx;
                            if (chairStudentListInRange[chi].size() <= from) {
                                continue;
                            }
                            final int to = Math.min(studentPerLine * (printLineIdx + 1), chairStudentListInRange[chi].size());
                            final List printStudentList = chairStudentListInRange[chi].subList(from, to);
                            for (int studentIdx = 0; studentIdx < printStudentList.size(); studentIdx++) {
                                final PrintStudent printStudent = (PrintStudent) printStudentList.get(studentIdx);
                                svf.VrsOut("TEACHER" + String.valueOf(chi + 1), printStudent._staffname);
                                svf.VrsOut("CHAIR_NAME" + String.valueOf(chi + 1), printStudent._chairname);
                                final String name = "1".equals(printStudent._kakkoFlg) ? "(" + StringUtils.defaultString(printStudent._printName) + ")" : StringUtils.defaultString(printStudent._printName);
                                svf.VrsOut("NAME" + String.valueOf(chi + 1) + "_" + String.valueOf(nameStartIndex + studentIdx), name + StringUtils.defaultString(printStudent._score));
                                printScore = true;
                            }
                        }
                        if (!printScore) {
                            svf.VrsOut("NAME1_" + String.valueOf(nameStartIndex + 0), "\n");
                        }
                        svf.VrEndRecord();
                    }
                    _hasData = true;
                }
                
                svf.VrsOut("SCORE", "欠席");
                for (int chi = 0; chi < chaircdList.size(); chi++) {
                    final List chairStudentList = getMappedList(chairPrintStudentListMap, chaircdList.get(chi));
                    final List kessekiStudentList = new ArrayList();
                    for (final Iterator itKesseki = chairStudentList.iterator(); itKesseki.hasNext();) {
                        final PrintStudent printStudent = (PrintStudent) itKesseki.next();
                        if ("*".equals(printStudent._valueDi)) {
                            kessekiStudentList.add(printStudent);
                        }
                    }
                    if (kessekiStudentList.size() > 0) {
                        int nameStartIndex = 0 * studentPerLine + 1; // 実線
                        svf.VrsOut("NAME" + String.valueOf(chi + 1) + "_" + String.valueOf(nameStartIndex + 1), String.valueOf(kessekiStudentList.size()));
                    }
                }
                svf.VrEndRecord();
                
                for (int chi = 0; chi < chaircdList.size(); chi++) {
                    svf.VrsOut("CLASS_AVE" + String.valueOf(chi + 1), subclass.getKenjouAvg((String) chaircdList.get(chi)));
                }
                svf.VrEndRecord();

                svf.VrsOut("GRADE_AVE", subclass.getAllAvg());
                svf.VrEndRecord();

                svf.VrsOut("DUMMY", "1");
                svf.VrEndRecord();
            }
        }
    }

    public Map getChairPrintStudentListMap(final List list) {
        final Map chairPrintStudentListMap = new TreeMap();
        for (final Iterator ItPrint = list.iterator(); ItPrint.hasNext();) {
            final PrintStudent printStudent = (PrintStudent) ItPrint.next();
            if (null == printStudent._chaircd) {
                continue;
            }
            getMappedList(chairPrintStudentListMap, printStudent._chaircd).add(printStudent);
        }
        return chairPrintStudentListMap;
    }

    public Map getPrintChairPageMap(final Map chairPrintStudentListMap, final int chairMaxPerPage) {
        Integer page = new Integer(1);
        final Map printChairPage = new TreeMap();
        for (final Iterator it = chairPrintStudentListMap.keySet().iterator(); it.hasNext();) {
            final String chaircd = (String) it.next();
            if (getMappedList(printChairPage, page).size() >= chairMaxPerPage) {
                page = new Integer(page.intValue() + 1); // 改ページ
            }
            getMappedList(printChairPage, page).add(chaircd);
        }
        return printChairPage;
    }

    private List getList(final DB2UDB db2, final Subclass subclass) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = scoreSql(subclass);
            //log.info(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String schregno   = rs.getString("SCHREGNO");
                final String kakkoFlg   = rs.getString("KAKKO_FLG");
                final String hrName     = rs.getString("HR_NAME");
                final String hrNameabbv = rs.getString("HR_NAMEABBV");
                final String attendno   = rs.getString("ATTENDNO");
                final String name       = rs.getString("NAME");
                final String chairname  = rs.getString("CHAIRNAME");
                final String chairabbv  = rs.getString("CHAIRABBV");
                final String chaircd    = rs.getString("CHAIRCD");
                final String staffname  = rs.getString("STAFFNAME");
                final String majorRank  = rs.getString("MAJOR_RANK");
                final String score      = rs.getString("SCORE");
                final String valueDi    = rs.getString("VALUE_DI");

                final PrintStudent printStudent = new PrintStudent(schregno, kakkoFlg, hrName, hrNameabbv, attendno, name, chairname, chairabbv, chaircd, staffname, majorRank, score, valueDi);
                retList.add(printStudent);
            }

        } catch (SQLException ex) {
            log.error("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        
        final Map printNamePrintStudentmapMap = new HashMap(); // 名字ごとの生徒マップ
        for (final Iterator it = retList.iterator(); it.hasNext();) {
            final PrintStudent printStudent = (PrintStudent) it.next();
            printStudent._nameSplit = StringUtils.split(StringUtils.replace(printStudent._name, " ", "　"), "　");
            printStudent._printName = null == printStudent._nameSplit || printStudent._nameSplit.length == 0 ? printStudent._name : printStudent._nameSplit[0]; // 表示は生徒の名字
            getMappedMap(printNamePrintStudentmapMap, printStudent._printName).put(printStudent._schregno, printStudent);
        }
        for (final Iterator itName = printNamePrintStudentmapMap.keySet().iterator(); itName.hasNext();) {
            final String printName = (String) itName.next();
            final Map printNamePrintStudentMap = getMappedMap(printNamePrintStudentmapMap, printName);
            if (printNamePrintStudentMap.size() > 1) { // 名字が重複するので名前の頭一文字を追加
                //log.info(" daburi = " + printName);
                for (final Iterator itst = printNamePrintStudentMap.values().iterator(); itst.hasNext();) {
                    final PrintStudent printStudent = (PrintStudent) itst.next();
                    if (printStudent._nameSplit.length > 1 && printStudent._nameSplit[1].length() > 0) {
                        printStudent._printName = printStudent._printName + printStudent._nameSplit[1].substring(0, 1);
                        //log.info("PrintStudent(" + printStudent._schregno + ":" + printStudent._name + ", " + printStudent._chaircd + ":" + printStudent._chairname + ", " + printStudent._score + ")");
                    }
                }
            }
        }
        
        return retList;
    }

    private String scoreSql(final Subclass subclass) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH CHAIR_STAFF AS ( ");
        stb.append(" SELECT ");
        stb.append("     CHSTF.YEAR, ");
        stb.append("     CHSTF.SEMESTER, ");
        stb.append("     CHSTF.CHAIRCD, ");
        stb.append("     CHSTF.STAFFCD, ");
        stb.append("     ROW_NUMBER() OVER(PARTITION BY CHSTF.YEAR, CHSTF.SEMESTER, CHSTF.CHAIRCD ORDER BY (CASE WHEN CHSTF.CHARGEDIV = '1' THEN 0 ELSE 1 END), CHSTF.STAFFCD) AS SEQ, ");
        stb.append("     STF.STAFFNAME ");
        stb.append(" FROM ");
        stb.append("     CHAIR_STF_DAT CHSTF ");
        stb.append("     INNER JOIN STAFF_MST STF ON STF.STAFFCD = CHSTF.STAFFCD ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     REGD.SCHREGNO, ");
        stb.append("     CASE WHEN REGDD.SCHREGNO IS NULL THEN 1 ELSE 0 END AS KAKKO_FLG, ");
        stb.append("     HDAT.HR_NAME, ");
        stb.append("     HDAT.HR_NAMEABBV, ");
        stb.append("     REGD.ATTENDNO, ");
        stb.append("     BASE.NAME, ");
        stb.append("     CHAIR.CHAIRNAME, ");
        stb.append("     CHAIR.CHAIRABBV, ");
        stb.append("     CHAIR.CHAIRCD, ");
        stb.append("     STF1.STAFFNAME, ");
        stb.append("     RANK.MAJOR_RANK, ");
        stb.append("     RANK.SCORE, ");
        stb.append("     TSCORE.VALUE_DI ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_FI_HDAT HDAT ");
        stb.append("     LEFT JOIN SCHREG_REGD_FI_DAT REGD ON HDAT.YEAR = REGD.YEAR ");
        stb.append("          AND HDAT.SEMESTER = REGD.SEMESTER ");
        stb.append("          AND HDAT.GRADE = REGD.GRADE ");
        stb.append("          AND HDAT.HR_CLASS = REGD.HR_CLASS ");
        stb.append("     LEFT JOIN SCHREG_REGD_DAT REGDD ON REGDD.YEAR = REGD.YEAR ");
        stb.append("          AND REGDD.SEMESTER = REGD.SEMESTER ");
        stb.append("          AND REGDD.SCHREGNO = REGD.SCHREGNO ");
        stb.append("          AND REGDD.GRADE = REGD.GRADE ");
        stb.append("          AND REGDD.HR_CLASS = REGD.HR_CLASS ");
        stb.append("     LEFT JOIN SCHREG_BASE_MST BASE ON REGD.SCHREGNO = BASE.SCHREGNO ");
        stb.append("     LEFT JOIN RECORD_SCORE_DAT TSCORE ON HDAT.YEAR = TSCORE.YEAR ");
        stb.append("           AND TSCORE.SEMESTER = '" + _param._semester + "' ");
        stb.append("           AND TSCORE.TESTKINDCD || TSCORE.TESTITEMCD || TSCORE.SCORE_DIV = '" + _param._testcd + "' ");
        if (!ALL3KA.equals(subclass._subclasscd) && !ALL5KA.equals(subclass._subclasscd) && !ALL9KA.equals(subclass._subclasscd)) {
            stb.append("           AND TSCORE.CLASSCD || TSCORE.SCHOOL_KIND || TSCORE.CURRICULUM_CD || TSCORE.SUBCLASSCD = '" + subclass._classcd + subclass._schoolKind + subclass._curriculum_cd + subclass._subclasscd + "' ");
        } else {
            stb.append("           AND TSCORE.SUBCLASSCD = '" + subclass._subclasscd + "' ");
        }
        stb.append("          AND REGD.SCHREGNO = TSCORE.SCHREGNO ");
        stb.append("     LEFT JOIN RECORD_RANK_FI_SDIV_DAT RANK ON HDAT.YEAR = RANK.YEAR ");
        stb.append("           AND RANK.SEMESTER = '" + _param._semester + "' ");
        stb.append("           AND RANK.TESTKINDCD || RANK.TESTITEMCD || RANK.SCORE_DIV = '" + _param._testcd + "' ");
        if (!ALL3KA.equals(subclass._subclasscd) && !ALL5KA.equals(subclass._subclasscd) && !ALL9KA.equals(subclass._subclasscd)) {
            stb.append("           AND RANK.CLASSCD || RANK.SCHOOL_KIND || RANK.CURRICULUM_CD || RANK.SUBCLASSCD = '" + subclass._classcd + subclass._schoolKind + subclass._curriculum_cd + subclass._subclasscd + "' ");
        } else {
            stb.append("           AND RANK.SUBCLASSCD = '" + subclass._subclasscd + "' ");
        }
        stb.append("           AND REGD.SCHREGNO = RANK.SCHREGNO ");
        stb.append("     INNER JOIN CHAIR_STD_DAT STD ON HDAT.YEAR = STD.YEAR ");
        stb.append("          AND HDAT.SEMESTER = STD.SEMESTER ");
        stb.append("          AND REGD.SCHREGNO = STD.SCHREGNO ");
        stb.append("     INNER JOIN CHAIR_DAT CHAIR ON STD.YEAR = CHAIR.YEAR ");
        stb.append("          AND STD.SEMESTER = CHAIR.SEMESTER ");
        stb.append("          AND STD.CHAIRCD = CHAIR.CHAIRCD ");
        stb.append("          AND TSCORE.CLASSCD || TSCORE.SCHOOL_KIND || TSCORE.CURRICULUM_CD || TSCORE.SUBCLASSCD = CHAIR.CLASSCD || CHAIR.SCHOOL_KIND || CHAIR.CURRICULUM_CD || CHAIR.SUBCLASSCD ");
        stb.append("     LEFT JOIN CHAIR_STAFF STF1 ON STF1.YEAR = CHAIR.YEAR ");
        stb.append("          AND STF1.SEMESTER = CHAIR.SEMESTER ");
        stb.append("          AND STF1.CHAIRCD = CHAIR.CHAIRCD ");
        stb.append("          AND STF1.SEQ = 1 ");
        stb.append(" WHERE ");
        stb.append("     HDAT.YEAR = '" + _param._year + "' ");
        stb.append("     AND HDAT.SEMESTER = '" + _param._regdSeme + "' ");
        stb.append("     AND HDAT.RECORD_DIV = '1' ");
        stb.append("     AND HDAT.GRADE = '" + _param._grade + "' ");
        stb.append(" ORDER BY ");
        stb.append("     RANK.MAJOR_RANK, ");
        stb.append("     RANK.SCORE, ");
        stb.append("     HDAT.HR_CLASS, ");
        stb.append("     REGD.ATTENDNO ");
        return stb.toString();
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
                    final Subclass subclass = new Subclass(classcd, schoolKind, curriculumcd, subclasscd, subclassname, subclassabbv, db2, _param._year, _param._semester, _param._grade, _param._testcd);
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
                        final Subclass subclass = new Subclass(classcd, schoolKind, curriculumcd, subclasscd, subclassname, subclassabbv, db2, _param._year, _param._semester, _param._grade, _param._testcd);
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

        private List getScoreRangeList() {
            final List retList = new ArrayList();
            retList.add(new ScoreRange("100", 100, 100, false));
            retList.add(new ScoreRange("95", 95, 99, true));
            retList.add(new ScoreRange("90", 90, 94, false));
            retList.add(new ScoreRange("85", 85, 89, true));
            retList.add(new ScoreRange("80", 80, 84, false));
            retList.add(new ScoreRange("75", 75, 79, true));
            retList.add(new ScoreRange("70", 70, 74, false));
            retList.add(new ScoreRange("65", 65, 69, true));
            retList.add(new ScoreRange("60", 60, 64, false));
            retList.add(new ScoreRange("55", 55, 59, true));
            retList.add(new ScoreRange("50", 50, 54, false));
            retList.add(new ScoreRange("45", 45, 49, true));
            retList.add(new ScoreRange("40", 40, 44, false));
            retList.add(new ScoreRange("35", 35, 39, true));
            retList.add(new ScoreRange("30", 30, 34, false));
            retList.add(new ScoreRange("25", 25, 29, true));
            retList.add(new ScoreRange("20", 20, 24, false));
            retList.add(new ScoreRange("15", 15, 19, true));
            retList.add(new ScoreRange("10", 10, 14, false));
            retList.add(new ScoreRange("5", 5, 9, true));
            retList.add(new ScoreRange("0", 0, 4, false));
            return retList;
        }

    }

    private class ScoreRange {
        final String _score;
        final int _minScore;
        final int _maxScore;
        final boolean _isTensen;

        public ScoreRange(
                final String score,
                final int minScore,
                final int maxScore,
                final boolean isTensen
        ) {
            _score = score;
            _minScore = minScore;
            _maxScore = maxScore;
            _isTensen = isTensen;
        }

    }

    private class Subclass {
        final String _classcd;
        final String _schoolKind;
        final String _curriculum_cd;
        final String _subclasscd;
        final String _subclassname;
        final String _subclassabbv;
        final Map _chairKenjouFiAvgMap;
        final Map _chairKenjouAvgMap;
        final String _allFiAvg;
        final String _allAvg;

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
                final String testCd
        ) throws SQLException {
            _classcd        = classcd;
            _schoolKind     = schoolKind;
            _curriculum_cd  = curriculumcd;
            _subclasscd     = subclasscd;
            _subclassname   = subclassname;
            _subclassabbv   = subclassabbv;
            _chairKenjouAvgMap = getChairKenjouAvgMap(db2, year, semester, grade, testCd, false);
            _chairKenjouFiAvgMap = getChairKenjouAvgMap(db2, year, semester, grade, testCd, true);
            _allAvg = getAllAvg(db2, year, semester, grade, testCd, false);
            _allFiAvg = getAllAvg(db2, year, semester, grade, testCd, true);
        }
        
        public String getKenjouAvg(final String chaircd) {
            final String fiAvg = (String) _chairKenjouFiAvgMap.get(chaircd);
            final String avg = (String) _chairKenjouAvgMap.get(chaircd);
            return StringUtils.defaultString(fiAvg) + (StringUtils.isBlank(avg) ? "" : "(" + avg + ")");
        }
        
        public String getAllAvg() {
            final String str = StringUtils.defaultString(_allFiAvg) + (StringUtils.isBlank(_allAvg) ? "" : "(" + _allAvg + ")");
            if (StringUtils.isBlank(str)) {
                return "\n"; // dummy
            }
            return str;
        }

        private Map getChairKenjouAvgMap(
                final DB2UDB db2,
                final String year,
                final String semester,
                final String grade,
                final String testCd,
                final boolean isFi
        ) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;

            Map rtn = new HashMap();
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     T1.CHAIRCD, ");
                stb.append("     T1.AVG ");
                stb.append(" FROM ");
                if (isFi) {
                    stb.append("     RECORD_AVERAGE_CHAIR_FI_SDIV_DAT T1 ");
                } else {
                    stb.append("     RECORD_AVERAGE_CHAIR_SDIV_DAT T1 ");
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
                stb.append("     AND T1.AVG_DIV = '1' ");
                stb.append("     AND T1.GRADE = '" + grade + "' ");
                stb.append("     AND T1.HR_CLASS = '000' ");
                stb.append("     AND T1.COURSECD = '0' ");
                stb.append("     AND T1.MAJORCD = '000' ");
                stb.append("     AND T1.COURSECODE = '0000' ");

                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String chaircd = rs.getString("CHAIRCD");
                    final String avg = null == rs.getString("AVG") ? "" : rs.getBigDecimal("AVG").setScale(1, BigDecimal.ROUND_HALF_UP).toString();
                    rtn.put(chaircd, avg);
                }
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtn;
        }

        private String getAllAvg(
                final DB2UDB db2,
                final String year,
                final String semester,
                final String grade,
                final String testCd,
                final boolean isFi
        ) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;

            String retstr = "";
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     T1.AVG ");
                stb.append(" FROM ");
                if (isFi) {
                    stb.append("     RECORD_AVERAGE_FI_SDIV_DAT T1 ");
                } else {
                    stb.append("     RECORD_AVERAGE_SDIV_DAT T1 ");
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
                stb.append("     AND T1.AVG_DIV = '1' ");
                stb.append("     AND T1.GRADE = '" + grade + "' ");
                stb.append("     AND T1.HR_CLASS = '000' ");
                stb.append("     AND T1.COURSECD = '0' ");
                stb.append("     AND T1.MAJORCD = '000' ");
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
    }

    private class PrintStudent {
        final String _schregno;
        final String _kakkoFlg;
        final String _hrName;
        final String _hrNameabbv;
        final String _attendno;
        final String _name;
        final String _chairname;
        final String _chairabbv;
        final String _chaircd;
        final String _staffname;
        final String _majorRank;
        final String _score;
        final String _valueDi;
        String[] _nameSplit;
        String _printName;

        public PrintStudent(
                final String schregno,
                final String kakkoFlg,
                final String hrName,
                final String hrNameabbv,
                final String attendno,
                final String name,
                final String chairname,
                final String chairabbv,
                final String chaircd,
                final String staffname,
                final String majorRank,
                final String score,
                final String valueDi
        ) {
            _schregno   = schregno;
            _kakkoFlg   = kakkoFlg;
            _hrName     = hrName;
            _hrNameabbv = hrNameabbv;
            _attendno   = attendno;
            _name       = name;
            _chairname  = chairname;
            _chairabbv  = chairabbv;
            _chaircd    = chaircd;
            _staffname  = staffname;
            _majorRank  = majorRank;
            _score      = score;
            _valueDi    = valueDi;
        }
    }
}

// eof

