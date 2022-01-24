/*
 * $Id: 256e13d15b68929bdbb507fb78b136846e5757eb $
 *
 * 作成日: 2017/06/28
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;


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

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;


public class KNJL315P {

    private static final Log log = LogFactory.getLog(KNJL315P.class);

    private static final String SCORE150 = "150";
    private boolean _hasData;

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
        svf.VrSetForm("KNJL315P.frm", 1);
        setTitle(svf);

        int fieldSoeji = 1;
        for (Iterator itSub = _param._subclassMap.keySet().iterator(); itSub.hasNext();) {
            final String subclassCd = (String) itSub.next();
            final Subclass subclass = (Subclass) _param._subclassMap.get(subclassCd);
            final List studentList = getStudentList(db2, subclassCd);
            for (Iterator itStudent = studentList.iterator(); itStudent.hasNext();) {
                final Student student = (Student) itStudent.next();
                subclass.setScoeRangeCnt(student);
            }
            subclass.setRuikeiCnt();
            svf.VrsOut("CLASS_NAME" + fieldSoeji, subclass._subclassName);
            for (Iterator itSubPrint = subclass._scoreMap.keySet().iterator(); itSubPrint.hasNext();) {
                final String range = (String) itSubPrint.next();
                final String[] rangeArray = StringUtils.split(range, "-");
                final PrintData printData = (PrintData) subclass._scoreMap.get(range);
                final String setRange = "0".equals(rangeArray[0]) ? "0" : rangeArray[0] + "\uFF5E" + rangeArray[1];
                svf.VrsOutn("SCORE_RANGE" + fieldSoeji, printData._lineCnt, setRange);
                svf.VrsOutn("POINT_NUM" + fieldSoeji, printData._lineCnt, String.valueOf(printData._cnt));
                svf.VrsOutn("TOTAL_NUM" + fieldSoeji, printData._lineCnt, String.valueOf(printData._ruikeiCnt));
            }
            fieldSoeji++;
            _hasData = true;
        }
        if (_hasData) {
            svf.VrEndPage();
        }
    }

    private void setTitle(final Vrw32alp svf) {
        svf.VrsOut("TITLE", KNJ_EditDate.h_format_JP_N(_param._entexamyear + "-04-01") + "度　" + _param._applicantDivName + "入試 " + _param._testDivName + "　結果");
        svf.VrsOut("SUBTITLE", "受験生度数分布表");
        svf.VrsOut("DATE", "実施日：" + KNJ_EditDate.h_format_JP(_param._testDate));
    }

    private List getStudentList(final DB2UDB db2, final String subclassCd) throws SQLException {
        final List retList = new ArrayList();

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final String sql = sql(subclassCd);
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String examNo = rs.getString("EXAMNO");
                final String score = rs.getString("SCORE");

                final Student student = new Student(examNo, score);
                retList.add(student);
            }

            db2.commit();
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
        return retList;
    }

    private String sql(final String subclassCd) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     RECEPT.EXAMNO, ");
        stb.append("     SCORE.SCORE ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_RECEPT_DAT RECEPT ");
        stb.append("     INNER JOIN ENTEXAM_SCORE_DAT SCORE ON RECEPT.ENTEXAMYEAR = SCORE.ENTEXAMYEAR ");
        stb.append("          AND RECEPT.APPLICANTDIV = SCORE.APPLICANTDIV ");
        stb.append("          AND RECEPT.TESTDIV = SCORE.TESTDIV ");
        stb.append("          AND RECEPT.EXAM_TYPE = SCORE.EXAM_TYPE ");
        stb.append("          AND RECEPT.RECEPTNO = SCORE.RECEPTNO ");
        stb.append("          AND SCORE.TESTSUBCLASSCD = '" + subclassCd + "' ");
        stb.append(" WHERE ");
        stb.append("     RECEPT.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND RECEPT.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND RECEPT.TESTDIV = '" + _param._testDiv + "' ");
        stb.append(" ORDER BY ");
        stb.append("     RECEPT.EXAMNO ");
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
        final String _applicantDiv;
        final String _applicantDivName;
        final String _testDiv;
        final String _testDivName;
        final String _testDate;
        final String _entexamyear;
        final String _loginYear;
        final String _loginSemester;
        final String _loginDate;
        final Map _subclassMap;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _loginYear = request.getParameter("LOGIN_YEAR");
            _loginSemester = request.getParameter("LOGIN_SEMESTER");
            _loginDate = request.getParameter("LOGIN_DATE");
            _applicantDivName = getNameMst(db2, "L003", _applicantDiv, "NAME1");
            final String testNameCd1 = "1".equals(_applicantDiv) ? "L024" : "L004";
            _testDivName = getNameMst(db2, testNameCd1, _testDiv, "NAME1");
            _testDate = getNameMst(db2, testNameCd1, _testDiv, "NAMESPARE1");
            _subclassMap = getSubclassMap(db2);
        }

        private String getNameMst(final DB2UDB db2, final String nameCd1, final String nameCd2, final String fieldName) throws SQLException {
            String retStr = "";
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = getNameMstSql(nameCd1, nameCd2);
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    retStr = rs.getString(fieldName);
                }

                db2.commit();
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retStr;
        }

        private String getNameMstSql(final String namecd1, final String namecd2) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     * ");
            stb.append(" FROM ");
            stb.append("     NAME_MST ");
            stb.append(" WHERE ");
            stb.append("     NAMECD1 = '" + namecd1 + "' ");
            if (!"".equals(namecd2)) {
                stb.append("     AND NAMECD2 = '" + namecd2 + "' ");
            }
            stb.append(" ORDER BY ");
            stb.append("     NAMECD2 ");
            return stb.toString();
        }

        private Map getSubclassMap(final DB2UDB db2) throws SQLException {
            final Map retMap = new TreeMap();
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = getNameMstSql("L009", "");
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                final String subclassName = "1".equals(_applicantDiv) ? "NAME1" : "NAME2";

                while (rs.next()) {
                    final String nameCd2 = rs.getString("NAMECD2");
                    final String name = rs.getString(subclassName);
                    if (null != name && !"".equals(name)) {
                        final Subclass subclass = new Subclass(nameCd2, name);
                        subclass.setScoeRange(db2, _entexamyear, _applicantDiv, _testDiv);
                        retMap.put(nameCd2, subclass);
                    }
                }

            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retMap;
        }

    }

    /** 科目 */
    private class Subclass {
        final String _subclassCd;
        final String _subclassName;
        final Map _scoreMap;

        public Subclass(
                final String subclassCd,
                final String subclassName
        ) {
            _subclassCd = subclassCd;
            _subclassName = subclassName;
            _scoreMap = new HashMap();
        }

        public void setRuikeiCnt() {
            for (Iterator itSubPrint = _scoreMap.keySet().iterator(); itSubPrint.hasNext();) {
                final String range = (String) itSubPrint.next();
                final PrintData printData = (PrintData) _scoreMap.get(range);
                for (Iterator itSubPrint2 = _scoreMap.keySet().iterator(); itSubPrint2.hasNext();) {
                    final String range2 = (String) itSubPrint2.next();
                    final PrintData printData2 = (PrintData) _scoreMap.get(range2);
                    if (printData2._lineCnt >= printData._lineCnt) {
                        printData2._ruikeiCnt += printData._cnt;
                    }
                }
            }
        }

        public void setScoeRange(final DB2UDB db2, final String entexamyear, final String applicantDiv, final String testDiv) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = getPerfectSql(entexamyear, applicantDiv, testDiv);
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                int perfect = 0;
                while (rs.next()) {
                    perfect = rs.getInt("PERFECT");
                }

                int lineCnt = 1;
                for (int score = 0; score < perfect;) {
                    if (score == 0) {
                        final String minScore = String.valueOf(score);
                        final String maxScore = String.valueOf(score);
                        final PrintData printData = new PrintData(lineCnt, 0);
                        _scoreMap.put(minScore + "-" + maxScore, printData);
                        score++;
                    } else {
                        final String minScore = String.valueOf(score);
                        final int maxRange = score + 9;
                        final String maxScore = maxRange > perfect ? String.valueOf(perfect) : String.valueOf(maxRange);
                        final PrintData printData = new PrintData(lineCnt, 0);
                        _scoreMap.put(minScore + "-" + maxScore, printData);
                        score += 10;
                    }
                    lineCnt++;
                }

            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        private String getPerfectSql(final String entexamyear, final String applicantDiv, final String testDiv) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     PERFECT.APPLICANTDIV, ");
            stb.append("     PERFECT.TESTDIV, ");
            stb.append("     MAX(PERFECT.PERFECT) AS PERFECT ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_PERFECT_MST PERFECT ");
            stb.append(" WHERE ");
            stb.append("     PERFECT.ENTEXAMYEAR = '" + entexamyear + "' ");
            stb.append("     AND PERFECT.APPLICANTDIV = '" + applicantDiv + "' ");
            stb.append("     AND PERFECT.TESTDIV = '" + testDiv + "' ");
            stb.append("     AND PERFECT.TESTSUBCLASSCD = '" + _subclassCd + "' ");
            stb.append(" GROUP BY ");
            stb.append("     PERFECT.APPLICANTDIV, ");
            stb.append("     PERFECT.TESTDIV ");
            return stb.toString();
        }

        public void setScoeRangeCnt(final Student student) {

            for (Iterator itScore = _scoreMap.keySet().iterator(); itScore.hasNext();) {
                final String range = (String) itScore.next();
                final String[] rangeArray = StringUtils.split(range, "-");
                if (null == student._score || "".equals(student._score)) {
                    continue;
                }
                if (Integer.parseInt(rangeArray[0]) <= Integer.parseInt(student._score) && Integer.parseInt(rangeArray[1]) >= Integer.parseInt(student._score)) {
                    final PrintData printData = (PrintData) _scoreMap.get(range);
                    printData._cnt++;
                }
            }
        }
    }

    /** 生徒 */
    private class Student {
        final String _examNo;
        final String _score;
        public Student(
                final String examNo,
                final String score
        ) {
            _examNo = examNo;
            _score = score;
        }

    }

    private class PrintData {
        final int _lineCnt;
        int _cnt;
        int _ruikeiCnt;

        public PrintData(
                final int lineCnt,
                final int cnt
        ) {
            _lineCnt = lineCnt;
            _cnt = cnt;
        }
    }
}

// eof

