// kanji=漢字
/*
 * $Id: 7be2c5b226507b1c699c8362e38acb569706821e $
 *
 * 作成日: 2009/12/22 23:52:49 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2009 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: 7be2c5b226507b1c699c8362e38acb569706821e $
 */
public class KNJL312M {

    private static final Log log = LogFactory.getLog("KNJL312M.class");

    private boolean _hasData;
    private static final String FORMNAME = "KNJL312M.frm";
    private static final int MAX_LINE = 40;
    private static final int MAX_RETU = 3;

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
        final List printData = getPrintData(db2);
        int manCnt = 0;
        int womanCnt = 0;
        int totalCnt = 0;
        int totalScore = 0;
        int printCnt = 1;

        svf.VrSetForm(FORMNAME, 1);
        int lineCnt = 1;
        int retuCnt = 1;
        boolean printFlg = false;
        for (final Iterator iter = printData.iterator(); iter.hasNext();) {
            final HallData hallData = (HallData) iter.next();
            manCnt += hallData._manCnt;
            womanCnt += hallData._womanCnt;
            totalCnt += hallData._manCnt;
            totalCnt += hallData._womanCnt;
            svf.VrsOut("SUBCLASS", _param._testPaper._name);
            for (final Iterator iterator = hallData._examData.iterator(); iterator.hasNext();) {
                final Student student = (Student) iterator.next();
                totalScore += !"".equals(student._score) ? Integer.parseInt(student._score) : 0;
                if (printCnt >= printData.size()) {
                    setNote(svf, manCnt, womanCnt, totalCnt, totalScore);
                    printFlg = false;
                }
                if (lineCnt > MAX_LINE) {
                    lineCnt = 1;
                    retuCnt++;
                }
                if (retuCnt > MAX_RETU) {
                    svf.VrEndPage();
                    printFlg = true;
                    lineCnt = 1;
                    retuCnt = 1;
                }
                svf.VrsOut("EXAM_PLACE" + retuCnt, hallData._name);
                svf.VrsOutn("RECEPTNO" + retuCnt, lineCnt, student._receptNo);
                svf.VrsOutn("EXAMNO" + retuCnt, lineCnt, student._examNo);
                svf.VrsOutn("POINT" + retuCnt, lineCnt, student._score);
                lineCnt++;
                printCnt++;
                _hasData = true;
            }
            lineCnt = 1;
            retuCnt++;
        }
        if (!printFlg && _hasData) {
            setNote(svf, manCnt, womanCnt, totalCnt, totalScore);
            svf.VrEndPage();
        }
    }

    private void setNote(final Vrw32alp svf, int manCnt, int womanCnt, int totalCnt, int totalScore) {
        BigDecimal avgDeci = new BigDecimal(totalScore);
        avgDeci = avgDeci.divide(new BigDecimal(totalCnt), 1, BigDecimal.ROUND_HALF_UP);
        svf.VrsOut("NOTE", "男" + manCnt + "名, 女" + womanCnt + "名, 計" + totalCnt + "名, 合計" + totalScore + "点, 平均" + avgDeci.toString() + "点");
    }

    private List getPrintData(final DB2UDB db2) throws SQLException {
        final List retList = new ArrayList();
        for (int i = 0; i < _param._hallDatas.length; i++) {
            final String hallCd = _param._hallDatas[i];
            final String hallSql = getHallData(hallCd);
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(hallSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String setHallCd = rs.getString("EXAMHALLCD");
                    final String name = rs.getString("EXAMHALL_NAME");
                    final String sRecept = rs.getString("S_RECEPTNO");
                    final String eRecept = rs.getString("E_RECEPTNO");
                    final HallData hallData = new HallData(setHallCd, name, sRecept, eRecept);
                    hallData.setExamData(db2);
                    retList.add(hallData);
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }
        return retList;
    }

    private String getHallData(final String hallCd) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     EXAMHALLCD,  ");
        stb.append("     EXAMHALL_NAME, ");
        stb.append("     S_RECEPTNO, ");
        stb.append("     E_RECEPTNO ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_HALL_DAT ");
        stb.append(" WHERE ");
        stb.append("     APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND TESTDIV = '1' ");
        stb.append("     AND EXAM_TYPE = '1' ");
        stb.append("     AND EXAMHALLCD = '" + hallCd + "' ");

        return stb.toString();
    }

    private void closeDb(final DB2UDB db2) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
    }

    public class HallData {
        final String _hallCd;
        final String _name;
        final String _sRecept;
        final String _eRecept;
        final List _examData;
        int _manCnt = 0;
        int _womanCnt = 0;
        int _totalCnt = 0;

        public HallData(
                final String hallCd,
                final String name,
                final String sRecept,
                final String eRecept
        ) {
            _hallCd = hallCd;
            _name = name;
            _sRecept = sRecept;
            _eRecept = eRecept;
            _examData = new ArrayList();
        }

        private void setExamData(final DB2UDB db2) throws SQLException {
            final String sql = getExamSql();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                int seatRenban = 1;
                while (rs.next()) {
                    final String examNo = rs.getString("EXAMNO");
                    final String name = rs.getString("NAME");
                    final String sex = rs.getString("SEX");
                    if ("1".equals(sex)) {
                        _manCnt++;
                    } else {
                        _womanCnt++;
                    }
                    _totalCnt++;
                    final String receptNo = rs.getString("RECEPTNO");
                    final String score = rs.getString("SCORE");
                    final Student student = new Student(examNo, name, sex, seatRenban, receptNo, score);
                    _examData.add(student);
                    seatRenban++;
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private String getExamSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.EXAMNO, ");
            stb.append("     T1.NAME, ");
            stb.append("     T1.SEX, ");
            stb.append("     T2.RECEPTNO, ");
            stb.append("     L1.SCORE ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_APPLICANTBASE_DAT T1 ");
            stb.append("     INNER JOIN ENTEXAM_RECEPT_DAT T2 ON T1.ENTEXAMYEAR = T2.ENTEXAMYEAR ");
            stb.append("          AND T1.APPLICANTDIV = T2.APPLICANTDIV ");
            stb.append("          AND T1.TESTDIV = T2.TESTDIV ");
            stb.append("          AND T1.EXAMNO = T2.EXAMNO ");
            stb.append("     LEFT JOIN ENTEXAM_SCORE_DETAIL_DAT L1 ON T2.ENTEXAMYEAR = L1.ENTEXAMYEAR ");
            stb.append("          AND T2.APPLICANTDIV = L1.APPLICANTDIV ");
            stb.append("          AND T2.TESTDIV = L1.TESTDIV ");
            stb.append("          AND T2.EXAM_TYPE = L1.EXAM_TYPE ");
            stb.append("          AND T2.RECEPTNO = L1.RECEPTNO ");
            stb.append("          AND L1.TESTSUBCLASSCD = '" + _param._testPaper._subclassCd + "' ");
            stb.append("          AND L1.TESTPAPERCD = '" + _param._testPaper._paperCd + "' ");
            stb.append(" WHERE ");
            stb.append("     T1.ENTEXAMYEAR = '" + _param._year + "' ");
            stb.append("     AND T1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
            stb.append("     AND T1.TESTDIV = '1' ");
            stb.append("     AND T2.RECEPTNO BETWEEN '" + _sRecept + "' AND '" + _eRecept + "' ");
            stb.append(" ORDER BY ");
            stb.append("     T2.RECEPTNO ");

            return stb.toString();
        }
    }

    public class Student {
        final String _examNo;
        final String _name;
        final String _sex;
        final int _seatRenban;
        final String _score;
        final String _receptNo;

        public Student(
                final String examNo,
                final String name,
                final String sex,
                final int seatRenban,
                final String receptNo,
                final String score
        ) {
            _examNo = examNo;
            _name = name;
            _sex = sex;
            _score = null != score ? score : "";
            _receptNo = receptNo;
            _seatRenban = seatRenban;
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
        private final TestPaper _testPaper;
        private final String[] _hallDatas;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            final String testPaperCd = request.getParameter("TESTPAPERCD");
            _testPaper = getTestPaper(db2, testPaperCd);
            _hallDatas = request.getParameterValues("CATEGORY_SELECTED");
        }

        private TestPaper getTestPaper(final DB2UDB db2, final String testPaperCd) throws SQLException {
            final String[] testCd = StringUtils.split(testPaperCd, "-");
            final String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'L009' AND NAMECD2 = '" + testCd[0] + "' ";
            PreparedStatement ps = null;
            ResultSet rs = null;
            TestPaper testPaper = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                String name = "";
                while (rs.next()) {
                    name = rs.getString("NAME1");
                }
                testPaper = new TestPaper(testCd[0], testCd[1], name);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return testPaper;
        }

    }

    public class TestPaper {
        final String _subclassCd;
        final String _paperCd;
        final String _name;

        public TestPaper(
                final String subclassCd,
                final String paperCd,
                final String name
        ) {
            _subclassCd = subclassCd;
            _paperCd = paperCd;
            _name = name + '-' + paperCd;
        }

        
    }
}

// eof
