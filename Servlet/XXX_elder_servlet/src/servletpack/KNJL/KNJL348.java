// kanji=漢字
/*
 * $Id: a65f404160b5a66ce2976212cb670a3b8c877a7e $
 *
 * 作成日: 2013/11/20 11:09:53 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2013 ALP Okinawa Co.,Ltd. All rights reserved.
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

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: a65f404160b5a66ce2976212cb670a3b8c877a7e $
 */
public class KNJL348 {

    private static final Log log = LogFactory.getLog("KNJL348.class");

    private boolean _hasData;
    private static final String EXAM_TYPE4 = "2";
    private static final int MAX_LINE = 16;

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
        final List printList = getPrintList(db2);
        int linCnt = 0;
        int renban = 1;
        for (final Iterator iter = printList.iterator(); iter.hasNext();) {
            final Student student = (Student) iter.next();

            if (linCnt == 0) {
                linCnt = 1;
                svf.VrSetForm("KNJL348.frm", 1);
                setHead(svf);
            }
            if (linCnt > MAX_LINE) {
                linCnt = 1;
                svf.VrEndPage();
                svf.VrSetForm("KNJL348.frm", 1);
                setHead(svf);
            }

            svf.VrsOutn("NO", linCnt, String.valueOf(renban));
            if (student._recomExamno1 != null) {
                svf.VrsOutn("EXAM_NO2", linCnt, student._examno);
                svf.VrsOutn("EXAM_NO3", linCnt, student._recomExamno1);
            } else {
                svf.VrsOutn("EXAM_NO1", linCnt, student._examno);
            }
            if (student._name != null) {
                final String fieldNo = (16 < getMS932ByteLength(student._name)) ? "3" : (12 < getMS932ByteLength(student._name)) ? "2" : "1";
                svf.VrsOutn("NAME1_" + fieldNo, linCnt, student._name);
            }
            svf.VrsOutn("BROSIS", linCnt, student._sex);
            svf.VrsOutn("HOPE", linCnt, "1".equals(student._daiiti) ? "〇" : "");
            if (student._siblingName != null) {
                final String fieldNo = (16 < getMS932ByteLength(student._siblingName)) ? "3" : (12 < getMS932ByteLength(student._siblingName)) ? "2" : "1";
                svf.VrsOutn("NAME2_" + fieldNo, linCnt, student._siblingName);
            }
            if (student._siblingClass != null) {
                if (12 < getMS932ByteLength(student._siblingClass)) {
                    final String hrNameArray[] = KNJ_EditEdit.get_token(student._siblingClass, 12, 2);
                    if (null != hrNameArray) {
                        for (int i = 0; i < hrNameArray.length; i++) {
                            svf.VrsOutn("HR_NAME2_" + (i + 1), linCnt, hrNameArray[i]);
                        }
                    }
                } else {
                    svf.VrsOutn("HR_NAME1", linCnt, student._siblingClass);
                }
            }

            int receptCnt = 1;
            for (final Iterator itKamoku = _param._testDivList.iterator(); itKamoku.hasNext();) {
                final TestDivMst testDivMst = (TestDivMst) itKamoku.next();
                final ReceptDat receptDat = (ReceptDat) student._receptMap.get(testDivMst._testDiv);
                final ReceptDat receptDat1 = (ReceptDat) student._receptMap1.get(testDivMst._testDiv);
                final ReceptDat receptDat2 = (ReceptDat) student._receptMap2.get(testDivMst._testDiv);
                final ReceptDat receptDat3 = (ReceptDat) student._receptMap3.get(testDivMst._testDiv);
                final DesireDat desireDat = (DesireDat) student._desireMap.get(testDivMst._testDiv);
                final DesireDat desireDat1 = (DesireDat) student._desireMap1.get(testDivMst._testDiv);
                final DesireDat desireDat2 = (DesireDat) student._desireMap2.get(testDivMst._testDiv);
                final DesireDat desireDat3 = (DesireDat) student._desireMap3.get(testDivMst._testDiv);
                String dMark = "";
                if (null != desireDat) {
                    dMark = desireDat.getJudgeMark();
                }
                if (null != desireDat1) {
                    dMark = desireDat1.getJudgeMark();
                }
                if (null != desireDat2) {
                    dMark = desireDat2.getJudgeMark();
                }
                if (null != desireDat3) {
                    dMark = desireDat3.getJudgeMark();
                }
                String rMark = "";
                String rTotal = "";
                if (null != receptDat) {
                    rMark = receptDat.getJudgeMark();
                    rTotal = receptDat._total4;
                }
                if (null != receptDat1) {
                    rMark = receptDat1.getJudgeMark();
                    rTotal = receptDat1._total4;
                }
                if (null != receptDat2) {
                    rMark = receptDat2.getJudgeMark();
                    rTotal = receptDat2._total4;
                }
                if (null != receptDat3) {
                    rMark = receptDat3.getJudgeMark();
                    rTotal = receptDat3._total4;
                }
                svf.VrsOutn("SCORE" + receptCnt, linCnt, rTotal);

                final String baseTestdiv  = (String) student._baseTestdivMap.get(testDivMst._testDiv);
                final String baseTestdiv1 = (String) student._baseTestdivMap1.get(testDivMst._testDiv);
                final String baseTestdiv2 = (String) student._baseTestdivMap2.get(testDivMst._testDiv);
                final String baseTestdiv3 = (String) student._baseTestdivMap3.get(testDivMst._testDiv);
                String judge = "";
                if (null != baseTestdiv || null != baseTestdiv1 || null != baseTestdiv2 || null != baseTestdiv3) {
                    judge = "出";
                }
                if (!"".equals(rMark) || !"".equals(dMark)) {
                    judge = (!"".equals(rMark)) ? rMark : ("".equals(rTotal) || null == rTotal) ? dMark : "";
                }
                svf.VrsOutn("JUDGE" + receptCnt, linCnt, judge);
                receptCnt++;
            }
            final String[] heigan = KNJ_EditEdit.get_token(student._heigan, 30, 2);
            if (null != heigan) {
                for (int i = 0; i < heigan.length; i++) {
                    svf.VrsOutn("MORE_SCHOOL" + (i + 1), linCnt, heigan[i]);
                }
            }
            linCnt++;
            renban++;
            _hasData = true;
        }
        if (_hasData) {
            svf.VrEndPage();
        }
    }

    private int getMS932ByteLength(final String s) {
        int rtn = 0;
        if (null != s) {
            try {
                rtn = s.getBytes("MS932").length;
            } catch (Exception e) {
                log.error("exception!", e);
            }
        }
        return rtn;
    }

    private void setHead(final Vrw32alp svf) {
        svf.VrsOut("TITLE1", "中学　" + KenjaProperties.gengou(Integer.parseInt(_param._year)) + "年度入試");
        svf.VrsOut("TITLE2", "在校生　弟・妹受験　一覧");
        int linCnt = 1;
        for (final Iterator itKamoku = _param._testDivList.iterator(); itKamoku.hasNext();) {
            final TestDivMst testDivMst = (TestDivMst) itKamoku.next();
            svf.VrsOut("TEST_DATE" + linCnt, KNJ_EditDate.h_format_JP_MD(testDivMst._testDay));
            svf.VrsOut("TEST_NAME" + linCnt, testDivMst._abbv);
            linCnt++;
        }
    }

    private List getPrintList(final DB2UDB db2) throws SQLException {
        final List retList = new ArrayList();
        final String studentSql = getStudentSql();
        PreparedStatement psStudent = null;
        ResultSet rsStudent = null;
        try {
            psStudent = db2.prepareStatement(studentSql);
            rsStudent = psStudent.executeQuery();
            while (rsStudent.next()) {
                final String examno = rsStudent.getString("EXAMNO");
                final String recomExamno1 = rsStudent.getString("RECOM_EXAMNO1");
                final String recomExamno2 = rsStudent.getString("RECOM_EXAMNO2");
                final String recomExamno3 = rsStudent.getString("RECOM_EXAMNO3");
                final String name = rsStudent.getString("NAME");
                final String sex = rsStudent.getString("NAME2");
                final String daiiti = rsStudent.getString("REMARK1");
                final String siblingName = rsStudent.getString("REMARK2");
                final String siblingClass = rsStudent.getString("REMARK3");
                final String heigan = rsStudent.getString("REMARK4");
                final Student student = new Student(db2, examno, recomExamno1, recomExamno2, recomExamno3, name, sex, daiiti, siblingName, siblingClass, heigan);
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
        stb.append("     BASE_D.EXAMNO, ");
        stb.append("     BASE.RECOM_EXAMNO1, ");
        stb.append("     BASE.RECOM_EXAMNO2, ");
        stb.append("     BASE.RECOM_EXAMNO3, ");
        stb.append("     BASE.NAME, ");
        stb.append("     Z002.NAME2, ");
        stb.append("     BASE_D.REMARK1, ");
        stb.append("     BASE_D.REMARK2, ");
        stb.append("     BASE_D.REMARK3, ");
        stb.append("     BASE_D.REMARK4 ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_APPLICANTBASE_DETAIL_DAT BASE_D ");
        stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE ON BASE_D.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
        stb.append("           AND BASE_D.EXAMNO = BASE.EXAMNO ");
        stb.append("     LEFT JOIN NAME_MST Z002 ON Z002.NAMECD1 = 'Z002' ");
        stb.append("          AND BASE.SEX = Z002.NAMECD2 ");
        stb.append(" WHERE ");
        stb.append("     BASE_D.ENTEXAMYEAR = '" + _param._year + "' ");
        stb.append("     AND BASE_D.SEQ = '005' ");
        stb.append("     AND BASE_D.REMARK2 IS NOT NULL ");
        stb.append(" ORDER BY ");
        stb.append("     BASE.NAME_KANA, ");
        stb.append("     BASE_D.EXAMNO ");
        return stb.toString();
    }

    private void closeDb(final DB2UDB db2) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 64530 $");
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
        private final List _testDivList;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDivList = getTestDivList(db2);
        }

        private List getTestDivList(final DB2UDB db2) throws SQLException {
            final List retList = new ArrayList();
            final String testDivsSql = getTestDivSql();
            PreparedStatement psTestDiv = null;
            ResultSet rsTestDiv = null;
            try {
                psTestDiv = db2.prepareStatement(testDivsSql);
                rsTestDiv = psTestDiv.executeQuery();
                while (rsTestDiv.next()) {
                    final TestDivMst divMst = new TestDivMst(rsTestDiv.getString("TESTDIV"), rsTestDiv.getString("NAME"), rsTestDiv.getString("ABBV"), rsTestDiv.getString("TESTDAY"));
                    retList.add(divMst);
                }
            } finally {
                DbUtils.closeQuietly(null, psTestDiv, rsTestDiv);
                db2.commit();
            }
            return retList;
        }

        private String getTestDivSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     TESTDIV, ");
            stb.append("     NAME, ");
            stb.append("     ABBV, ");
            stb.append("     TESTDAY ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_TESTDIV_MST ");
            stb.append(" WHERE ");
            stb.append("     ENTEXAMYEAR = '" + _year + "' ");
            stb.append(" ORDER BY ");
            stb.append("     SHOWORDER, ");
            stb.append("     TESTDAY, ");
            stb.append("     TESTDIV ");
            return stb.toString();
        }

    }

    /** TESTDIV_MST */
    private class TestDivMst {
        private final String _testDiv;
        private final String _name;
        private final String _abbv;
        private final String _testDay;

        TestDivMst(
                final String testDiv,
                final String name,
                final String abbv,
                final String testDay
        ) throws SQLException {
            _testDiv = testDiv;
            _name = name;
            _abbv = abbv;
            _testDay = testDay;
        }

    }

    /** 生徒 */
    private class Student {
        private final String _examno;
        private final String _recomExamno1;
        private final String _recomExamno2;
        private final String _recomExamno3;
        private final String _name;
        private final String _sex;
        private final String _daiiti;
        private final String _siblingName;
        private final String _siblingClass;
        private final String _heigan;
        private final Map _receptMap;
        private final Map _receptMap1;
        private final Map _receptMap2;
        private final Map _receptMap3;
        private final Map _desireMap;
        private final Map _desireMap1;
        private final Map _desireMap2;
        private final Map _desireMap3;
        private final Map _baseTestdivMap;
        private final Map _baseTestdivMap1;
        private final Map _baseTestdivMap2;
        private final Map _baseTestdivMap3;

        Student(final DB2UDB db2,
                final String examno,
                final String recomExamno1,
                final String recomExamno2,
                final String recomExamno3,
                final String name,
                final String sex,
                final String daiiti,
                final String siblingName,
                final String siblingClass,
                final String heigan
        ) throws SQLException {
            _examno = examno;
            _recomExamno1 = recomExamno1;
            _recomExamno2 = recomExamno2;
            _recomExamno3 = recomExamno3;
            _name = name;
            _sex = sex;
            _daiiti = daiiti;
            _siblingName = siblingName;
            _siblingClass = siblingClass;
            _heigan = heigan;
            _receptMap = getRecept(db2, examno);
            _receptMap1 = getRecept(db2, recomExamno1);
            _receptMap2 = getRecept(db2, recomExamno2);
            _receptMap3 = getRecept(db2, recomExamno3);
            _desireMap = getDesire(db2, examno);
            _desireMap1 = getDesire(db2, recomExamno1);
            _desireMap2 = getDesire(db2, recomExamno2);
            _desireMap3 = getDesire(db2, recomExamno3);
            _baseTestdivMap = getBaseTestdiv(db2, examno);
            _baseTestdivMap1 = getBaseTestdiv(db2, recomExamno1);
            _baseTestdivMap2 = getBaseTestdiv(db2, recomExamno2);
            _baseTestdivMap3 = getBaseTestdiv(db2, recomExamno3);
        }

        private Map getBaseTestdiv(final DB2UDB db2, final String examno) throws SQLException {
            final Map retMap = new HashMap();
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     EXAMNO, ");
            stb.append("     TESTDIV0, ");
            stb.append("     TESTDIV1, ");
            stb.append("     TESTDIV2, ");
            stb.append("     TESTDIV3, ");
            stb.append("     TESTDIV4, ");
            stb.append("     TESTDIV5, ");
            stb.append("     TESTDIV6 ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_APPLICANTBASE_DAT ");
            stb.append(" WHERE ");
            stb.append("     ENTEXAMYEAR = '" + _param._year + "' ");
            stb.append("     AND EXAMNO = '" + examno + "' ");
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String examNo = rs.getString("EXAMNO");
                    final String testdiv0 = rs.getString("TESTDIV0");
                    final String testdiv1 = rs.getString("TESTDIV1");
                    final String testdiv2 = rs.getString("TESTDIV2");
                    final String testdiv3 = rs.getString("TESTDIV3");
                    final String testdiv4 = rs.getString("TESTDIV4");
                    final String testdiv5 = rs.getString("TESTDIV5");
                    final String testdiv6 = rs.getString("TESTDIV6");
                    if (null != testdiv0) retMap.put(testdiv0, examNo);
                    if (null != testdiv1) retMap.put(testdiv1, examNo);
                    if (null != testdiv2) retMap.put(testdiv2, examNo);
                    if (null != testdiv3) retMap.put(testdiv3, examNo);
                    if (null != testdiv4) retMap.put(testdiv4, examNo);
                    if (null != testdiv5) retMap.put(testdiv5, examNo);
                    if (null != testdiv6) retMap.put(testdiv6, examNo);
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retMap;
        }

        private Map getRecept(final DB2UDB db2, final String examno) throws SQLException {
            final Map retMap = new HashMap();
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     RECEPTNO, ");
            stb.append("     TESTDIV, ");
            stb.append("     TOTAL4, ");
            stb.append("     JUDGEDIV, ");
            stb.append("     HONORDIV, ");
            stb.append("     JUDGECLASS ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_RECEPT_DAT ");
            stb.append(" WHERE ");
            stb.append("     ENTEXAMYEAR = '" + _param._year + "' ");
            stb.append("     AND APPLICANTDIV = '" + _param._applicantDiv + "' ");
            stb.append("     AND EXAM_TYPE = '" + EXAM_TYPE4 + "' ");
            stb.append("     AND EXAMNO = '" + examno + "' ");
            PreparedStatement psRecept = null;
            ResultSet rsRecept = null;
            try {
                psRecept = db2.prepareStatement(stb.toString());
                rsRecept = psRecept.executeQuery();
                while (rsRecept.next()) {
                    final String receptno = rsRecept.getString("RECEPTNO");
                    final String testDiv = rsRecept.getString("TESTDIV");
                    final String total4 = rsRecept.getString("TOTAL4");
                    final String judgeDiv = rsRecept.getString("JUDGEDIV");
                    final String honorDiv = rsRecept.getString("HONORDIV");
                    final String judgeClass = rsRecept.getString("JUDGECLASS");
                    final ReceptDat receptDat = new ReceptDat(receptno, testDiv, total4, judgeDiv, honorDiv, judgeClass);
                    retMap.put(testDiv, receptDat);
                }
            } finally {
                DbUtils.closeQuietly(null, psRecept, rsRecept);
                db2.commit();
            }
            return retMap;
        }

        private Map getDesire(final DB2UDB db2, final String examno) throws SQLException {
            final Map retMap = new HashMap();
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     EXAMNO, ");
            stb.append("     TESTDIV, ");
            stb.append("     EXAMINEE_DIV ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_DESIRE_DAT ");
            stb.append(" WHERE ");
            stb.append("     ENTEXAMYEAR = '" + _param._year + "' ");
            stb.append("     AND APPLICANTDIV = '" + _param._applicantDiv + "' ");
            stb.append("     AND EXAMNO = '" + examno + "' ");
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String examNo = rs.getString("EXAMNO");
                    final String testDiv = rs.getString("TESTDIV");
                    final String examineeDiv = rs.getString("EXAMINEE_DIV");
                    final DesireDat desireDat = new DesireDat(examNo, testDiv, examineeDiv);
                    retMap.put(testDiv, desireDat);
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retMap;
        }

    }

    /** ENTEXAM_RECEPT_DAT */
    private class ReceptDat {
        private final String _receptno;
        private final String _testDiv;
        private final String _total4;
        private final String _judgeDiv;
        private final String _honorDiv;
        private final String _judgeClass;

        ReceptDat(
                final String receptno,
                final String testDiv,
                final String total4,
                final String judgeDiv,
                final String honorDiv,
                final String judgeClass
        ) throws SQLException {
            _receptno = receptno;
            _testDiv = testDiv;
            _total4 = total4;
            _judgeDiv = judgeDiv;
            _honorDiv = honorDiv;
            _judgeClass = judgeClass;
        }

        private String getJudgeMark() {
            String retSt = "";
            if ("1".equals(_judgeDiv) && "1".equals(_honorDiv)) {
                retSt = "☆";
            }
            if ("1".equals(_judgeDiv) && ("2".equals(_judgeClass) || "3".equals(_judgeClass))) {
                retSt = retSt + "◎";
            }
            if ("1".equals(_judgeDiv) && ("1".equals(_judgeClass) || "4".equals(_judgeClass) || "6".equals(_judgeClass))) {
                retSt = retSt + "〇";
            }
            if ("2".equals(_judgeDiv)) {
                retSt = retSt + "×";
            }
            return retSt;
        }
    }

    /** ENTEXAM_DESIRE_DAT */
    private class DesireDat {
        private final String _examNo;
        private final String _testDiv;
        private final String _examineeDiv;

        DesireDat(
                final String examNo,
                final String testDiv,
                final String examineeDiv
        ) throws SQLException {
            _examNo = examNo;
            _testDiv = testDiv;
            _examineeDiv = examineeDiv;
        }

        private String getJudgeMark() {
            String retSt = "";
            if ("1".equals(_examineeDiv)) {
                retSt = "出";
            }
            if ("2".equals(_examineeDiv)) {
                retSt = "▲";
            }
            return retSt;
        }
    }
}

// eof
