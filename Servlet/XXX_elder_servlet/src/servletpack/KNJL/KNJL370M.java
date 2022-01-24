// kanji=漢字
/*
 * $Id: 59dfaebfabb815d381d4afdd68474c955449a2ea $
 *
 * 作成日: 2009/12/24 1:37:07 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2009 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: 59dfaebfabb815d381d4afdd68474c955449a2ea $
 */
public class KNJL370M {

    private static final Log log = LogFactory.getLog("KNJL370M.class");

    private boolean _hasData;
    private static final String FORMNAME = "KNJL370M.frm";
    private static final int MAX_LINE = 50;

    Param _param;

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
            init(response, svf);

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);

            _hasData = false;

            printMain(db2, svf);

            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            closeDb(db2);
            svf.VrQuit();
        }

    }

    private void init(
            final HttpServletResponse response,
            final Vrw32alp svf
    ) throws IOException {
        response.setContentType("application/pdf");

        svf.VrInit();
        svf.VrSetSpoolFileStream(response.getOutputStream());
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        final List printKamoku = getPrintKamoku(db2);
        final List printStudentData = getPrintData(db2);
        printOut(db2, svf, printStudentData, printKamoku);
    }

    private List getPrintKamoku(final DB2UDB db2) throws SQLException {
        final List retList = new ArrayList();
        final String kamokuSql = getKamokuSql();
        PreparedStatement psKamoku = null;
        ResultSet rsKamoku = null;
        try {
            psKamoku = db2.prepareStatement(kamokuSql);
            rsKamoku = psKamoku.executeQuery();
            while (rsKamoku.next()) {
                final String testSubclassCd = rsKamoku.getString("TESTSUBCLASSCD");
                final String testPaperCd = rsKamoku.getString("TESTPAPERCD");
                final String name = rsKamoku.getString("TESTPAPERNAME");
                final String orderCd = rsKamoku.getString("ORDERCD");
                final TestKamoku kamoku = new TestKamoku(testSubclassCd,
                                                         testPaperCd,
                                                         name,
                                                         orderCd);
                retList.add(kamoku);
            }
        } finally {
            DbUtils.closeQuietly(null, psKamoku, rsKamoku);
            db2.commit();
        }
        return retList;
    }

    private String getKamokuSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH MAIN_T AS ( ");
        stb.append(" SELECT ");
        stb.append("     T1.NAMECD2 AS TESTSUBCLASSCD, ");
        stb.append("     L1.TESTPAPERCD, ");
        stb.append("     T1.NAME1 || L1.TESTPAPERCD AS TESTPAPERNAME ");
        stb.append(" FROM ");
        stb.append("     V_NAME_MST T1 ");
        stb.append("     LEFT JOIN ENTEXAM_PERFECT_DETAIL_MST L1 ON T1.NAMECD2 = L1.TESTSUBCLASSCD ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.NAMECD1 = 'L009' ");
        stb.append(" UNION ");
        stb.append(" SELECT ");
        stb.append("     T1.NAMECD2 AS TESTSUBCLASSCD, ");
        stb.append("     '9' AS TESTPAPERCD, ");
        stb.append("     T1.NAME1 AS TESTPAPERNAME ");
        stb.append(" FROM ");
        stb.append("     V_NAME_MST T1 ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.NAMECD1 = 'L009' ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     TESTSUBCLASSCD, ");
        stb.append("     TESTPAPERCD, ");
        stb.append("     TESTPAPERNAME, ");
        stb.append("     ROW_NUMBER() OVER(ORDER BY TESTSUBCLASSCD, TESTPAPERCD) AS ORDERCD ");
        stb.append(" FROM ");
        stb.append("     MAIN_T ");
        stb.append(" ORDER BY ");
        stb.append("     TESTSUBCLASSCD, ");
        stb.append("     TESTPAPERCD ");

        return stb.toString();
    }

    private void printOut(final DB2UDB db2, final Vrw32alp svf, final List printStudent, final List printKamoku) throws SQLException {
        setHead(svf, printKamoku);
        int lineCnt = 1;
        int totalCnt = 1;
        boolean lastPrintFlg = false;
        for (final Iterator itPrint = printStudent.iterator(); itPrint.hasNext();) {
            final Student student = (Student) itPrint.next();
            if (totalCnt >= printStudent.size()) {
                setTotal(db2, svf, printKamoku);
                lastPrintFlg = true;
            }
            if (lineCnt > MAX_LINE) {
                setTotalTitle(db2, svf);
                svf.VrEndPage();
                setHead(svf, printKamoku);
                lineCnt = 1;
            }
            svf.VrsOutn("EXAM_NO", lineCnt, student._examNo);
            svf.VrsOutn("RANK", lineCnt, student._totalRank4);
            svf.VrsOutn("TOTAL_SCORE", lineCnt, student._total4);
            for (final Iterator itKamoku = printKamoku.iterator(); itKamoku.hasNext();) {
                final TestKamoku kamoku = (TestKamoku) itKamoku.next();
                final String key = kamoku._testSubclassCd + kamoku._testPaperCd;
                final ScoreData scoreData = (ScoreData) student._scoreDatas.get(key);
                if (null != scoreData) {
                    svf.VrsOutn("SCORE" + kamoku._orderCd, lineCnt, scoreData._score);
                    if ("9".equals(kamoku._testPaperCd)) {
                        svf.VrsOutn("Z1SUBJECT" + kamoku._testSubclassCd, lineCnt, scoreData._stdScore);
                    }
                }
            }
            svf.VrsOutn("DIV", lineCnt, student._natpubpriName);
            svf.VrsOutn("SCHOOL_NAME", lineCnt, student._fsName);
            svf.VrsOutn("CITY", lineCnt, student._fsAreaDivName);
            svf.VrsOutn("SCHOOL_ADD", lineCnt, student._fsAreaCdName);
            svf.VrsOutn("Z1", lineCnt, student._judgeDevRank);
            svf.VrsOutn("Z2", lineCnt, student._linkJudgeDevRank);
            svf.VrsOutn("Z1TOTAL", lineCnt, student._judgeDev);
            svf.VrsOutn("Z2TOTAL", lineCnt, student._linkJudgeDev);
            lineCnt++;
            _hasData = true;
        }

        if (!lastPrintFlg) {
            setTotal(db2, svf, printKamoku);
        }
    }

    private void setHead(final Vrw32alp svf, final List printKamoku) {
        svf.VrSetForm(FORMNAME, 1);
        svf.VrsOut("YEAR", _param.changeYear(_param._year) + "度");
        svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._ctrlDate));
        final String sort;
        if ("2".equals(_param._orderDiv)) {
            sort = "総点順";
        } else if ("3".equals(_param._orderDiv)) {
            sort = "Z1順";
        } else if ("4".equals(_param._orderDiv)) {
            sort = "Z2順";
        } else {
            sort = "受験番号順";
        }
        svf.VrsOut("SORT", sort);
        for (final Iterator itKamokuTitle = printKamoku.iterator(); itKamokuTitle.hasNext();) {
            final TestKamoku kamoku = (TestKamoku) itKamokuTitle.next();
            svf.VrsOut("SUBJECT" + kamoku._orderCd, kamoku._name);
        }
    }

    private void setTotal(final DB2UDB db2, final Vrw32alp svf, final List printKamoku) throws SQLException {
        setTotalTitle(db2, svf);
        final Map getTotalDataMap = getTotalAvgDev(db2, "9");
        svf.VrsOutn("TOTAL_SCORE", 51, (String) getTotalDataMap.get("AVG_SCORE"));
        svf.VrsOutn("TOTAL_SCORE", 52, (String) getTotalDataMap.get("STD_SCORE"));
        for (final Iterator itKamoku = printKamoku.iterator(); itKamoku.hasNext();) {
            final TestKamoku kamoku = (TestKamoku) itKamoku.next();
            if ("9".equals(kamoku._testPaperCd)) {
                final Map getDataMap = getTotalAvgDev(db2, kamoku._testSubclassCd);
                svf.VrsOutn("SCORE" + kamoku._orderCd, 51, (String) getDataMap.get("AVG_SCORE"));
                svf.VrsOutn("SCORE" + kamoku._orderCd, 52, (String) getDataMap.get("STD_SCORE"));
            }
        }
        svf.VrsOutn("TOTAL_SCORE", 53, getTotalJuken(db2, "1"));
        svf.VrsOutn("TOTAL_SCORE", 54, getTotalJuken(db2, "2"));
        svf.VrEndPage();
    }

    private void setTotalTitle(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        svf.VrsOutn("TOTALNAME", 1, "平均");
        svf.VrsOutn("TOTALNAME", 2, "標準偏差");
        svf.VrsOutn("TOTALNAME", 3, "受験者数");
        svf.VrsOutn("TOTALNAME", 4, "欠席者");
    }

    private Map getTotalAvgDev(final DB2UDB db2, final String testSubclassCd) throws SQLException {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        if (!"9".equals(testSubclassCd)) {
            stb.append("     TESTSUBCLASSCD, ");
        }
        stb.append("     ROUND(STDDEV(SCORE),2) AS STD_SCORE, ");
        stb.append("     ROUND(AVG(DOUBLE(SCORE)) * 10, 0) / 10 AS AVG_SCORE ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_SCORE_DAT ");
        stb.append(" WHERE ");
        stb.append("     ENTEXAMYEAR = '" + _param._year + "' ");
        stb.append("     AND APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND TESTDIV = '1' ");
        stb.append("     AND EXAM_TYPE = '1' ");
        if (!"9".equals(testSubclassCd)) {
            stb.append("     AND TESTSUBCLASSCD = '" + testSubclassCd + "' ");
            stb.append(" GROUP BY ");
            stb.append("     TESTSUBCLASSCD ");
        }

        Map retMap = new HashMap();
        retMap.put("STD_SCORE", "");
        retMap.put("AVG_SCORE", "");
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();
            while (rs.next()) {
                retMap.put("STD_SCORE", rs.getString("STD_SCORE"));
                retMap.put("AVG_SCORE", rs.getString("AVG_SCORE"));
            }
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retMap;
    }

    private String getTotalJuken(final DB2UDB db2, final String examType) throws SQLException {
        final String sql = "SELECT "
                         + "    COUNT(*) AS CNT "
                         + " FROM "
                         + "    ENTEXAM_DESIRE_DAT "
                         + " WHERE "
                         + "    ENTEXAMYEAR = '" + _param._year + "' "
                         + "    AND APPLICANTDIV = '" + _param._applicantDiv + "' "
                         + "    AND TESTDIV = '1' "
                         + "    AND EXAM_TYPE = '1' "
                         + "    AND EXAMINEE_DIV = '" + examType + "' ";
        PreparedStatement ps = null;
        ResultSet rs = null;
        String retVal = "0";
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
               retVal = rs.getString("CNT");
            }
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retVal;
    }

    private List getPrintData(final DB2UDB db2) throws SQLException {
        final List retList = new ArrayList();
        final String studentSql = getStudentSql();
        PreparedStatement psStudent = null;
        ResultSet rsStudent = null;
        try {
            psStudent = db2.prepareStatement(studentSql);
            rsStudent = psStudent.executeQuery();
            while (rsStudent.next()) {
                final String examNo = rsStudent.getString("EXAMNO");
                final String name = rsStudent.getString("NAME");
                final String nameKana = rsStudent.getString("NAME_KANA");
                final String fsCd = rsStudent.getString("FS_CD");
                final String fsName = rsStudent.getString("FS_NAME");
                final String natpubpriDiv = rsStudent.getString("FS_NATPUBPRIDIV");
                final String natpubpriName = rsStudent.getString("NATPUBPRI_NAME");
                final String fsAreaDiv = rsStudent.getString("FS_AREA_DIV");
                final String fsAreaDivName = rsStudent.getString("AREA_DIV_NAME");
                final String fsAreaCd = rsStudent.getString("FS_AREA_CD");
                final String fsAreaCdName = rsStudent.getString("AREA_NAME");
                final String total4 = rsStudent.getString("TOTAL4");
                final String totalRank4 = rsStudent.getString("TOTAL_RANK4");
                final String judgeDev = rsStudent.getString("JUDGE_DEVIATION");
                final String judgeDevRank = rsStudent.getString("JUDGE_DEVIATION_RANK");
                final String linkJudgeDev = rsStudent.getString("LINK_JUDGE_DEVIATION");
                final String linkJudgeDevRank = rsStudent.getString("LINK_JUDGE_DEVIATION_RANK");
                final Student student = new Student(examNo,
                                                    name,
                                                    nameKana,
                                                    fsCd,
                                                    fsName,
                                                    natpubpriDiv,
                                                    natpubpriName,
                                                    fsAreaDiv,
                                                    fsAreaDivName,
                                                    fsAreaCd,
                                                    fsAreaCdName,
                                                    total4,
                                                    totalRank4,
                                                    judgeDev,
                                                    judgeDevRank,
                                                    linkJudgeDev,
                                                    linkJudgeDevRank);
                student.setScoreDatas(db2);
                retList.add(student);
            }
        } finally {
            DbUtils.closeQuietly(null, psStudent, rsStudent);
            db2.commit();
        }
        return retList;
    }

    private String getStudentSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.EXAMNO, ");
        stb.append("     T1.NAME, ");
        stb.append("     T1.NAME_KANA, ");
        stb.append("     T1.FS_CD, ");
        stb.append("     T1.FS_NAME, ");
        stb.append("     T1.FS_NATPUBPRIDIV, ");
        stb.append("     NATP.NATPUBPRI_NAME, ");
        stb.append("     T1.FS_AREA_DIV, ");
        stb.append("     AREA_D.AREA_DIV_NAME, ");
        stb.append("     T1.FS_AREA_CD, ");
        stb.append("     ARE_M.AREA_NAME, ");
        stb.append("     RECEPT.TOTAL4, ");
        stb.append("     RECEPT.TOTAL_RANK4, ");
        stb.append("     RECEPT.JUDGE_DEVIATION, ");
        stb.append("     RECEPT.JUDGE_DEVIATION_RANK, ");
        stb.append("     RECEPT.LINK_JUDGE_DEVIATION, ");
        stb.append("     RECEPT.LINK_JUDGE_DEVIATION_RANK ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_APPLICANTBASE_DAT T1 ");
        stb.append("     LEFT JOIN ENTEXAM_RECEPT_DAT RECEPT ON T1.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR ");
        stb.append("          AND T1.APPLICANTDIV = RECEPT.APPLICANTDIV ");
        stb.append("          AND T1.TESTDIV = RECEPT.TESTDIV ");
        stb.append("          AND RECEPT.EXAM_TYPE = '1' ");
        stb.append("          AND T1.EXAMNO = RECEPT.RECEPTNO ");
        stb.append("          AND T1.EXAMNO = RECEPT.EXAMNO ");
        stb.append("     LEFT JOIN ENTEXAM_NATPUBPRI_MST NATP ON T1.FS_NATPUBPRIDIV = NATP.NATPUBPRI_CD ");
        stb.append("     LEFT JOIN ENTEXAM_AREA_DIV_MST AREA_D ON T1.FS_NATPUBPRIDIV = AREA_D.NATPUBPRI_CD ");
        stb.append("          AND T1.FS_AREA_DIV = AREA_D.AREA_DIV_CD ");
        stb.append("     LEFT JOIN ENTEXAM_AREA_MST ARE_M ON T1.FS_NATPUBPRIDIV = ARE_M.NATPUBPRI_CD ");
        stb.append("          AND T1.FS_AREA_DIV = ARE_M.AREA_DIV_CD ");
        stb.append("          AND T1.FS_AREA_CD = ARE_M.AREA_CD ");
        stb.append(" WHERE ");
        stb.append("     T1.ENTEXAMYEAR = '" + _param._year + "' ");
        stb.append("     AND T1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND T1.TESTDIV = '1' ");
        stb.append("     AND T1.SHDIV = '1' ");
        stb.append("     AND T1.DESIREDIV = '1' ");
        stb.append(" ORDER BY ");
        if ("2".equals(_param._orderDiv)) {
            stb.append("     VALUE(RECEPT.TOTAL4, 0) DESC, ");
            stb.append("     T1.EXAMNO ");
        } else if ("3".equals(_param._orderDiv)) {
            stb.append("     VALUE(RECEPT.JUDGE_DEVIATION_RANK, 999999), ");
            stb.append("     T1.EXAMNO ");
        } else if ("4".equals(_param._orderDiv)) {
            stb.append("     VALUE(RECEPT.LINK_JUDGE_DEVIATION_RANK, 999999), ");
            stb.append("     T1.EXAMNO ");
        } else {
            stb.append("     T1.EXAMNO ");
        }

        return stb.toString();
    }

    private void closeDb(final DB2UDB db2) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
    }

    private class TestKamoku {
        final String _testSubclassCd;
        final String _testPaperCd;
        final String _name;
        final String _orderCd;

        public TestKamoku(final String testSubclassCd, final String testPaperCd, final String name, final String orderCd) {
            _testSubclassCd = testSubclassCd;
            _testPaperCd = testPaperCd;
            _name = name;
            _orderCd = orderCd;
        }

        /**
         * {@inheritDoc}
         */
        public String toString() {
            return _name;
        }
    }

    public class Student {
        final String _examNo;
        final String _name;
        final String _nameKana;
        final String _fsCd;
        final String _fsName;
        final String _natpubpriDiv;
        final String _natpubpriName;
        final String _fsAreaDiv;
        final String _fsAreaDivName;
        final String _fsAreaCd;
        final String _fsAreaCdName;
        final String _total4;
        final String _totalRank4;
        final String _judgeDev;
        final String _judgeDevRank;
        final String _linkJudgeDev;
        final String _linkJudgeDevRank;
        final Map _scoreDatas;

        public Student(
                final String examNo,
                final String name,
                final String nameKana,
                final String fsCd,
                final String fsName,
                final String natpubpriDiv,
                final String natpubpriName,
                final String fsAreaDiv,
                final String fsAreaDivName,
                final String fsAreaCd,
                final String fsAreaCdName,
                final String total4,
                final String totalRank4,
                final String judgeDev,
                final String judgeDevRank,
                final String linkJudgeDev,
                final String linkJudgeDevRank
        ) {
            _examNo = examNo;
            _name = name;
            _nameKana = nameKana;
            _fsCd = fsCd;
            _fsName = fsName;
            _natpubpriDiv = natpubpriDiv;
            _natpubpriName = natpubpriName;
            _fsAreaDiv = fsAreaDiv;
            _fsAreaDivName = fsAreaDivName;
            _fsAreaCd = fsAreaCd;
            _fsAreaCdName = fsAreaCdName;
            _scoreDatas = new HashMap();
            _total4 = total4;
            _totalRank4 = totalRank4;
            _judgeDev = judgeDev;
            _judgeDevRank = judgeDevRank;
            _linkJudgeDev = linkJudgeDev;
            _linkJudgeDevRank = linkJudgeDevRank;
        }

        private void setScoreDatas(final DB2UDB db2) throws SQLException {
            final String sql = getScoreDataSql();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String testSubclassCd = rs.getString("TESTSUBCLASSCD");
                    final String testPaperCd = rs.getString("TESTPAPERCD");
                    final String score = rs.getString("SCORE");
                    final String stdScore = rs.getString("STD_SCORE");
                    final ScoreData scoreData = new ScoreData(testSubclassCd, testPaperCd, score, stdScore);
                    _scoreDatas.put(testSubclassCd + testPaperCd, scoreData);
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private String getScoreDataSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     TESTSUBCLASSCD, ");
            stb.append("     '9' AS TESTPAPERCD, ");
            stb.append("     SCORE, ");
            stb.append("     DECIMAL(STD_SCORE, 5, 1) AS STD_SCORE ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_SCORE_DAT ");
            stb.append(" WHERE ");
            stb.append("     ENTEXAMYEAR = '" + _param._year + "' ");
            stb.append("     AND APPLICANTDIV = '" + _param._applicantDiv + "' ");
            stb.append("     AND TESTDIV = '1' ");
            stb.append("     AND EXAM_TYPE = '1' ");
            stb.append("     AND RECEPTNO = '" + _examNo + "' ");
            stb.append(" UNION ");
            stb.append(" SELECT ");
            stb.append("     TESTSUBCLASSCD, ");
            stb.append("     TESTPAPERCD, ");
            stb.append("     SCORE, ");
            stb.append("     CAST(NULL AS DECIMAL) STD_SCORE ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_SCORE_DETAIL_DAT ");
            stb.append(" WHERE ");
            stb.append("     ENTEXAMYEAR = '" + _param._year + "' ");
            stb.append("     AND APPLICANTDIV = '" + _param._applicantDiv + "' ");
            stb.append("     AND TESTDIV = '1' ");
            stb.append("     AND EXAM_TYPE = '1' ");
            stb.append("     AND RECEPTNO = '" + _examNo + "' ");

            return stb.toString();
        }
    }

    private class ScoreData {
        final String _testSubclassCd;
        final String _testPaperCd;
        final String _score;
        final String _stdScore;

        public ScoreData(
                final String testSubclassCd,
                final String testPaperCd,
                final String score,
                final String stdScore
        ) {
            _testSubclassCd = testSubclassCd;
            _testPaperCd = testPaperCd;
            _score = score;
            _stdScore = stdScore;
        }
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
        private final String _applicantDiv;
        private final String _orderDiv;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _orderDiv = request.getParameter("ORDER_DIV");
        }

        private String changeYear(final String year) {
            return KNJ_EditDate.h_format_JP_N(year + "-01-01");
        }
    }
}

// eof
