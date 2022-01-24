// kanji=漢字
/*
 * $Id: 5592386262e4b528305d042e01f3584a99e050f5 $
 *
 * 作成日: 2008/06/13 10:48:06 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2008-2012 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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

import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: 5592386262e4b528305d042e01f3584a99e050f5 $
 */
public class KNJD625A {
    private static final Log log = LogFactory.getLog(KNJD625A.class);

    private static final int RETSUSU = 6;
    private static final int MAXLINE = 120;

    Param _param;

    /**
     * KNJD.classから呼ばれる処理
     * @param request リクエスト
     * @param response レスポンス
     * @throws Exception I/O例外
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
            boolean hasData = false;
            hasData = printMain(db2, svf);
            if (!hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
        } finally {
            close(db2, svf);
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

    private void close(final DB2UDB db2, final Vrw32alp svf) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
        if (null != svf) {
            svf.VrQuit();
        }
    }

    private boolean printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException {

        boolean hasData = false;
        final List printData = new ArrayList();
        for (int i = 0; i < _param._subclassCd.length; i++) {
            final String subclassCd = _param._subclassCd[i];
            Subclass subclass = new Subclass(db2, subclassCd);
            ResultSet rs = null;
            PreparedStatement ps = null;
            try {
                final String sql = getPrintData(subclassCd);
                log.debug(sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String schregno = rs.getString("SCHREGNO");
                    final String name = rs.getString("NAME");
                    final String hrName = rs.getString("HR_NAMEABBV");
                    final String attendNo = rs.getString("ATTENDNO");
                    final Student student = new Student(schregno, name, hrName, attendNo);

                    final String chairCd = rs.getString("CHAIRCD");
                    final String chairName = rs.getString("CHAIRNAME");

                    final String score = rs.getString("SCORE");

                    subclass.setChair(chairCd, chairName);
                    subclass.setData(chairCd, student, score);
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            printData.add(subclass);
        }

        hasData = printLineData(svf, printData);

        return hasData;
    }

    private void printsvfHead(final Vrw32alp svf, final Subclass subclass, final int pageCnt, final int retu) {
        svf.VrSetForm("KNJD625.frm", 4);
        svf.VrsOut("NENDO", _param.changePrintYear(_param._year));
        svf.VrsOut("SEMESTER", _param._semesterName);
        svf.VrsOut("TESTNAME", _param._testname);
        svf.VrsOut("SUBCLASS", subclass._subclassName);
        final String changePrintDate = _param.changePrintDate(_param._loginDate);
        svf.VrsOut("DATE", changePrintDate);
        int cnt = 1;
        final List chairList = getChairList(subclass);
        for (int retuTotal = RETSUSU * (pageCnt - 1); retuTotal < retu; retuTotal++) {
            if (retuTotal < chairList.size()) {
                final Chair chair = (Chair) chairList.get(retuTotal);
                if (chair != null) {
                    svf.VrsOut("CHAIRNAME" + cnt, chair._chairName);
                }
            }
            cnt++;
        }
    }

    private List getChairList(final Subclass subclass) {
        final List retList = new ArrayList();
        for (final Iterator itSub = subclass._chairMap.keySet().iterator(); itSub.hasNext();) {
            final String subKey = (String) itSub.next();
            final Chair chair = (Chair) subclass._chairMap.get(subKey);
            retList.add(chair);
        }
        return retList;
    }

    private int getMaxScoreCnt(final List chairList, final String score) {
        int retCnt = 0;
        for (final Iterator iter = chairList.iterator(); iter.hasNext();) {
            final Chair chair = (Chair) iter.next();
            final List scoreList = (List) chair._scoreMap.get(score);
            if (null != scoreList) {
                retCnt = scoreList.size() > retCnt ? scoreList.size() : retCnt;
            }
        }
        return retCnt;
    }

    private boolean printLineData(final Vrw32alp svf, final List printList) {

        boolean hasData = false;
        for (final Iterator itPrint = printList.iterator(); itPrint.hasNext();) {
            final Subclass subclass = (Subclass) itPrint.next();

            final Map printDataMap = new TreeMap();

            log.debug(subclass);
            if (subclass._chairMap.size() > 0) {

                final List chairList = getChairList(subclass);
                final int page1 = chairList.size() / RETSUSU;
                final int page2 = chairList.size() % RETSUSU;
                final int page3 = page2 > 0 ? 1 : 0;
                final int totalPageCnt = page1 + page3;

                for (int pageCnt = 1; pageCnt <= totalPageCnt; pageCnt++) {

                    final List printLineList = new ArrayList();
                    final int retu = RETSUSU * pageCnt;
                    // 得点でループ
                    for (int i = 100; i >= 0; i--) {
                        final String score = String.valueOf(i);
                        final int maxCnt = getMaxScoreCnt(chairList, score);

                        if (maxCnt > 0) {
                            for (int cnt = 0; cnt < maxCnt; cnt++) {
                                final LineData lineData = new LineData(score);
                                int fieldCnt = 1;
                                for (int retuTotal = RETSUSU * (pageCnt - 1); retuTotal < retu; retuTotal++) {
                                    boolean dataSet = false;
                                    if (retuTotal < chairList.size()) {
                                        final Chair chair = (Chair) chairList.get(retuTotal);
                                        if (chair != null && chair._scoreMap.containsKey(score)) {
                                            final List studentList = (List) chair._scoreMap.get(score);
                                            if (null != studentList && studentList.size() >= (cnt + 1)) {
                                                final Student student = (Student) studentList.get(cnt);
                                                
                                                PrintData printData = new PrintData(String.valueOf(fieldCnt), student);
                                                lineData.setData(printData);
                                                dataSet = true;
                                            }
                                        }
                                    }
                                    if (!dataSet) {
                                        PrintData printData = new PrintData(String.valueOf(fieldCnt), new Student());
                                        lineData.setData(printData);
                                    }
                                    fieldCnt++;
                                }
                                printLineList.add(lineData);
                            }
                        } else {
                            final LineData lineData = new LineData(score);
                            for (int retsuCnt = 1; retsuCnt <= RETSUSU; retsuCnt++) {
                                PrintData printData = new PrintData(String.valueOf(retsuCnt), new Student());
                                lineData.setData(printData);
                            }
                            printLineList.add(lineData);
                        }
                    }
                    final LineData lineData = new LineData("");
                    int totalCnt = 1;
                    for (int retuTotal = RETSUSU * (pageCnt - 1); retuTotal < retu; retuTotal++) {
                        if (retuTotal < chairList.size()) {
                            final Chair chair = (Chair) chairList.get(retuTotal);
                            if (chair != null) {
                                PrintData printData = new PrintData(String.valueOf(totalCnt), String.valueOf(chair._totalCnt));
                                lineData.setData(printData);
                            }
                        }
                        totalCnt++;
                    }
                    printLineList.add(lineData);
                    printDataMap.put(String.valueOf(pageCnt), printLineList);
                }

                for (final Iterator itPage = printDataMap.keySet().iterator(); itPage.hasNext();) {

                    final String keyPage = (String) itPage.next();
                    final List pageList = (List) printDataMap.get(keyPage);

                    final int retu = RETSUSU * Integer.parseInt(keyPage);
                    final int pageCnt = Integer.parseInt(keyPage);

                    printsvfHead(svf, subclass, pageCnt, retu);
                    for (final Iterator itPageList = pageList.iterator(); itPageList.hasNext();) {
                        final LineData lineData = (LineData) itPageList.next();
                        final String score = lineData._score;
                        svf.VrsOut("SCORE", score);
//                        log.debug(lineData);
                        for (final Iterator itLine = lineData._data.iterator(); itLine.hasNext();) {
                            final PrintData printData = (PrintData) itLine.next();
//                            log.debug(printData);
                            if (!score.equals("")) {
                                final Student student = (Student) printData._student;
                                if (!student._schregno.equals("")) {
                                    svf.VrsOut("HR_CLASS" + printData._field, student._hrNameAbbv + "(" + student._attendNo + ")");
                                    svf.VrsOut("NAME" + printData._field, student._name);
                                }
                                svf.VrsOut("SCORE" + printData._field, score);
                            } else {
                                final String totalCnt = (String) printData._student;
                                svf.VrsOut("SCORE", score);
                                svf.VrsOut("ITEM", "合 計");
                                svf.VrsOut("TOTAL" + printData._field, totalCnt);
                            }
                        }
                        svf.VrEndRecord();
                    }
                    hasData = true;
                }

            }
        }

        return hasData;
    }

    private class LineData {
        final String _score;
        final List _data;

        public LineData(final String score) {
            _score = score;
            _data = new ArrayList();
        }

        public void setData(final PrintData printData) {
            _data.add(printData);
        }

        public String toString() {
            return _score;
        }
    }

    private class PrintData {
        final String _field;
        final Object _student;

        public PrintData(final String field, final Object obj) {
            _field = field;
            _student = obj;
        }

        public String toString() {
            return _field + ":" + _student;
        }
    }

    private class Subclass {
        final String _subclassCd;
        final String _subclassName;
        final Map _chairMap;

        public Subclass(final DB2UDB db2, final String subclassCd) throws SQLException {
            String subclassName = "";
            final String sql = "SELECT * FROM SUBCLASS_MST WHERE SUBCLASSCD = '" + subclassCd + "'";
            ResultSet rs = null;
            try {
                db2.query(sql);
                rs = db2.getResultSet();
                while (rs.next()) {
                    subclassName = rs.getString("SUBCLASSNAME");
                }
                _subclassCd = subclassCd;
                _subclassName = subclassName;
                _chairMap = new TreeMap();
            } finally {
                DbUtils.closeQuietly(rs);
                db2.commit();
            }
        }

        public void setChair(final String chairCd, final String chairName) {
            if (!_chairMap.containsKey(chairCd)) {
                final Chair chair = new Chair(chairCd, chairName);
                _chairMap.put(chair._chairCd, chair);
            }
        }

        public void setData(final String chairCd, final Student student, final String score) {
            final Chair chair = (Chair) _chairMap.get(chairCd);
            chair.setScoreOfStudent(score, student);
        }

        public String toString() {
            return "科目 :" + _subclassCd + " " + _subclassName;
        }
    }

    private class Chair {
        final String _chairCd;
        final String _chairName;
        final Map _scoreMap;
        int _totalCnt = 0;

        Chair(final String chairCd, final String chairName) {
            _chairCd = chairCd;
            _chairName = chairName;
            _scoreMap = new TreeMap();
        }

        public void setScoreOfStudent(String score, Student student) {
            if (!_scoreMap.containsKey(score)) {
                final List studentList = new ArrayList();
                _scoreMap.put(score, studentList);
            }
            final List studentList = (List) _scoreMap.get(score);
            studentList.add(student);
            _totalCnt++;
        }

        public String toString() {
            return "講座 :" + _chairCd + " " + _chairName + " 生徒数:" + _totalCnt;
        }
    }

    private class Student {
        final String _schregno;
        final String _name;
        final String _hrNameAbbv;
        final String _attendNo;

        Student() {
            _schregno = "";
            _name = "";
            _hrNameAbbv = "";
            _attendNo = "";
        }

        Student(final String schregno, final String name, final String hrNameAbbv, final String attendNo) {
            _schregno = schregno;
            _name = name;
            _hrNameAbbv = hrNameAbbv;
            _attendNo = attendNo;
        }

        public String toString() {
            return "生徒 :" + _schregno
                    + " " + _hrNameAbbv + _attendNo + "番 "
                    + _name;
        }
    }

    private String getPrintData(final String subclassCd) {
        final StringBuffer stb = new StringBuffer();

        stb.append(" WITH T_RECORD AS ( ");
        stb.append("     SELECT ");
        stb.append("         SUBCLASSCD, ");
        stb.append("         SCHREGNO, ");
        stb.append("         CHAIRCD, ");
        stb.append("         SCORE ");
        stb.append("     FROM ");
        stb.append("         RECORD_SCORE_DAT ");
        stb.append("     WHERE ");
        stb.append("         YEAR = '" + _param._year + "' AND ");
        stb.append("         SEMESTER = '" + _param._semester + "' AND ");
        stb.append("         TESTKINDCD || TESTITEMCD = '" + _param._testcd + "' AND ");
        stb.append("         SUBCLASSCD = '" + subclassCd + "' AND ");
        stb.append("         SCORE_DIV = '01' AND ");
        stb.append("         SCORE IS NOT NULL ");
        stb.append("     ) ");

        stb.append(" SELECT ");
        stb.append("     L1.SUBCLASSCD, ");
        stb.append("     L1.CHAIRCD, ");
        stb.append("     L2.CHAIRNAME, ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.HR_NAMEABBV, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     T1.NAME, ");
        stb.append("     L1.SCORE ");
        stb.append(" FROM ");
        stb.append("     V_SCHREG_INFO T1 ");
        stb.append("     INNER JOIN T_RECORD L1 ON T1.SCHREGNO = L1.SCHREGNO ");
        stb.append("     INNER JOIN CHAIR_DAT L2 ON T1.YEAR = L2.YEAR ");
        stb.append("           AND T1.SEMESTER = L2.SEMESTER ");
        stb.append("           AND L1.CHAIRCD = L2.CHAIRCD ");
        stb.append(" WHERE ");
        stb.append("     T1.YEAR = '" + _param._year + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
        stb.append("     AND T1.GRADE = '" + _param._grade + "' ");
        stb.append(" ORDER BY ");
        stb.append("     L1.CHAIRCD, ");
        stb.append("     L1.SCORE DESC, ");
        stb.append("     T1.HR_CLASS, ");
        stb.append("     T1.ATTENDNO ");

        return stb.toString();
    }

    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        dumpParam(request, param);
        return param;
    }

    /** パラメータダンプ */
    private void dumpParam(final HttpServletRequest request, final Param param) {
        log.fatal("$Revision: 56595 $"); // CVSキーワードの取り扱いに注意
        if (log.isDebugEnabled()) {
            final Enumeration enums = request.getParameterNames();
            while (enums.hasMoreElements()) {
                final String name = (String) enums.nextElement();
                final String[] values = request.getParameterValues(name);
                log.debug("parameter:name=" + name + ", value=[" + StringUtils.join(values, ',') + "]");
            }
        }
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _semester;
        private final String _semesterName;
        private final String _ctrlSemester;
        private final String _grade;
        private final String _testcd;
        private final String _schooldiv;
        private final String[] _subclassCd;
        private final String _testname;
        private final String _loginDate;

        private boolean _seirekiFlg;
        Param(final DB2UDB db2, final HttpServletRequest request) throws Exception {
            _year = request.getParameter("YEAR");
            _semester = request.getParameter("SEMESTER");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _grade = request.getParameter("GRADE");
            _testcd = request.getParameter("TESTCD");
            _schooldiv = (3 >= Integer.parseInt(_grade)) ? "J" : "H";
            _subclassCd = request.getParameterValues("CATEGORY_SELECTED");
            _loginDate = request.getParameter("LOGIN_DATE");

            // DBより取得
            _semesterName = getSemesterName(db2, _year, _semester);
            _testname = getTestName(db2, _year, _semester, _testcd);
            setSeirekiFlg(db2);
        }

        private String getSemesterName(
                final DB2UDB db2,
                final String year,
                final String semester
        ) throws Exception {
            String rtnVal = "";

            final String sql = "SELECT SEMESTERNAME FROM SEMESTER_MST WHERE YEAR = '" + year + "' AND SEMESTER = '" + semester + "'";
            db2.query(sql);
            final ResultSet rs = db2.getResultSet();
            try {
                rs.next();
                rtnVal = rs.getString("SEMESTERNAME");
            } finally {
                DbUtils.closeQuietly(rs);
                db2.commit();
            }
            return rtnVal;
        }

        private String getTestName(
                final DB2UDB db2,
                final String year,
                final String semester,
                final String testcd
        ) throws Exception {
            String rtnVal = "";
            final String sql = "SELECT TESTITEMNAME FROM TESTITEM_MST_COUNTFLG_NEW WHERE YEAR = '" + year + "'"
                + " AND SEMESTER = '" + semester + "' AND TESTKINDCD || TESTITEMCD = '" + testcd + "'";

            db2.query(sql);
            final ResultSet rs = db2.getResultSet();
            try {
                rs.next();
                rtnVal = rs.getString("TESTITEMNAME");
            } finally {
                DbUtils.closeQuietly(rs);
                db2.commit();
            }
            return rtnVal;
        }

        private void setSeirekiFlg(final DB2UDB db2) {
            try {
                _seirekiFlg = false;
                String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1='Z012' AND NAMECD2='00' AND NAME1 IS NOT NULL ";
                PreparedStatement ps = db2.prepareStatement(sql);
                ResultSet rs = ps.executeQuery();
                while( rs.next() ){
                    if (rs.getString("NAME1").equals("2")) _seirekiFlg = true; //西暦
                }
                ps.close();
                rs.close();
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                db2.commit();
            }
        }

        public String changePrintDate(final String date) {
            if (null != date) {
                if (_seirekiFlg) {
                    return date.substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(date);
                } else {
                    return KNJ_EditDate.h_format_JP(date);
                }
            } else {
                return "";
            }
        }

        public String changePrintYear(final String year) {
            if (_seirekiFlg) {
                return year + "年度";
            } else {
                return nao_package.KenjaProperties.gengou(Integer.parseInt(year)) + "年度";
            }
        }
    }

}
