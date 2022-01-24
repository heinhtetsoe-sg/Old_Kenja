// kanji=漢字
/*
 * $Id: f8f7346e2a53fc6a8c0bcc5c2c4daea5bb36f0c0 $
 *
 * 作成日: 2013/12/27 11:06:07 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2013 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
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

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: f8f7346e2a53fc6a8c0bcc5c2c4daea5bb36f0c0 $
 */
public class KNJL352 {

    private static final Log log = LogFactory.getLog("KNJL352.class");

    private boolean _hasData;
    private static final String EXAM_TYPE4 = "2";
    private static final int MAX_LINE = 20;

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

    private void closeDb(final DB2UDB db2) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        final List printList = getPrintList(db2);
        int linCnt = 0;
        int renban = 1;
        String befPri = "";
        for (final Iterator iter = printList.iterator(); iter.hasNext();) {
            final Student student = (Student) iter.next();

            if (linCnt == 0) {
                linCnt = 1;
                svf.VrSetForm("KNJL352.frm", 1);
                setHead(svf);
            }
            if (!"".equals(befPri) && !befPri.equals(student._prischoolCd)) {
                renban = 1;
                linCnt = 1;
                svf.VrEndPage();
                svf.VrSetForm("KNJL352.frm", 1);
                setHead(svf);
            } else if (linCnt > MAX_LINE) {
                linCnt = 1;
                svf.VrEndPage();
                setHead(svf);
            }

            svf.VrsOutn("NO", linCnt, String.valueOf(renban));
            svf.VrsOutn("EXAMNO", linCnt, student._examno);

            if (student._name != null) {
                final String fieldNo = (30 < getMS932ByteLength(student._name)) ? "3" : (20 < getMS932ByteLength(student._name)) ? "2" : "";
                svf.VrsOutn("NAME" + fieldNo, linCnt, student._name);
            }
            svf.VrsOutn("SEX", linCnt, student._sex);
            svf.VrsOutn("CRAM_NAME", linCnt, student._prischoolName);
            svf.VrsOutn("CLASSROOM_NAME", linCnt, student._kyousituName);

            int receptCnt = 1;
            String judgeMax = "";
            for (final Iterator itKamoku = _param._testDivList.iterator(); itKamoku.hasNext();) {
                final TestDivMst testDivMst = (TestDivMst) itKamoku.next();
                final ReceptDat receptDat = (ReceptDat) student._receptMap.get(testDivMst._testDiv);
                if (null != receptDat) {
                    svf.VrsOutn(receptCnt + "4KEI", linCnt, receptDat._total4);
                    final String judgeMark = receptDat.getJudgeMark();
                    svf.VrsOutn(receptCnt + "GOUHI", linCnt, judgeMark);
                    judgeMax = getJudgeMax(judgeMax, judgeMark);
                }
                receptCnt++;
            }

            svf.VrsOutn("GOUHI", linCnt, judgeMax);
            svf.VrsOutn("PROSEDURE", linCnt, "1".equals(student._procedureDiv) ? "〇" : "");
            svf.VrsOutn("ENTRANCE", linCnt, "1".equals(student._entDiv) ? "〇" : "");
            linCnt++;
            renban++;
            befPri = student._prischoolCd;
            _hasData = true;
        }
        if (_hasData) {
            svf.VrEndPage();
        }
    }
    
    private String getJudgeMax(final String judgeMax, final String judgeMark) {
        if ("".equals(judgeMax)) {
            return judgeMark;
        }
        if ("".equals(judgeMark)) {
            return judgeMax;
        }
        final Map judge = new HashMap();
        judge.put("☆", "4");
        judge.put("◎", "3");
        judge.put("〇", "2");
        judge.put("×", "1");
        final int max = Integer.parseInt((String) judge.get(judgeMax));
        final int mark = Integer.parseInt((String) judge.get(judgeMark));
        final String retStr = max >= mark ? judgeMax : judgeMark;
        return retStr;
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
        svf.VrsOut("NENDO", KenjaProperties.gengou(Integer.parseInt(_param._year)) + "年度　大宮開成中学　入学試験　受験者データ(塾別)");
        svf.VrsOut("PRINT_DAYTIME", _param._dateStr); // 作成日
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
                final String name = rsStudent.getString("NAME");
                final String sex = rsStudent.getString("NAME2");
                final String prischoolCd = rsStudent.getString("PRISCHOOLCD");
                final String prischoolName = rsStudent.getString("PRISCHOOL_NAME");
                final String kyousituName = rsStudent.getString("REMARK2");
                final String procedureDiv = rsStudent.getString("PROCEDUREDIV");
                final String entDiv = rsStudent.getString("ENTDIV");
                final Student student = new Student(db2, examno, name, sex, prischoolCd, prischoolName, kyousituName, procedureDiv, entDiv);
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
        stb.append(" WITH BASE_DETAIL AS ( ");
        stb.append("     SELECT ");
        stb.append("         ENTEXAMYEAR, ");
        stb.append("         EXAMNO, ");
        stb.append("         REMARK1, ");
        stb.append("         REMARK2 ");
        stb.append("     FROM ");
        stb.append("         ENTEXAM_APPLICANTBASE_DETAIL_DAT ");
        stb.append("     WHERE ");
        stb.append("         ENTEXAMYEAR = '" + _param._year + "' ");
        stb.append("         AND SEQ = '008' ");
        stb.append("         AND REMARK1 IS NOT NULL ");
        stb.append("     UNION ALL ");
        stb.append("     SELECT ");
        stb.append("         ENTEXAMYEAR, ");
        stb.append("         EXAMNO, ");
        stb.append("         REMARK3 AS REMARK1, ");
        stb.append("         REMARK4 AS REMARK2 ");
        stb.append("     FROM ");
        stb.append("         ENTEXAM_APPLICANTBASE_DETAIL_DAT ");
        stb.append("     WHERE ");
        stb.append("         ENTEXAMYEAR = '" + _param._year + "' ");
        stb.append("         AND SEQ = '008' ");
        stb.append("         AND REMARK3 IS NOT NULL ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     BASE_D.EXAMNO, ");
        stb.append("     BASE.PROCEDUREDIV, ");
        stb.append("     BASE.ENTDIV, ");
        stb.append("     BASE.NAME, ");
        stb.append("     Z002.NAME2, ");
        stb.append("     PRI.PRISCHOOLCD, ");
        stb.append("     PRI.PRISCHOOL_NAME, ");
        stb.append("     BASE_D.REMARK2 ");
        stb.append(" FROM ");
        stb.append("     BASE_DETAIL BASE_D ");
        stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE ON BASE_D.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
        stb.append("           AND BASE_D.EXAMNO = BASE.EXAMNO ");
        stb.append("     LEFT JOIN NAME_MST Z002 ON Z002.NAMECD1 = 'Z002' ");
        stb.append("          AND BASE.SEX = Z002.NAMECD2 ");
        stb.append("     INNER JOIN PRISCHOOL_MST PRI ON BASE_D.REMARK1 = PRI.PRISCHOOLCD ");
        stb.append(" WHERE ");
        stb.append("     BASE_D.ENTEXAMYEAR = '" + _param._year + "' ");
        if (null != _param._l900Name1) {
            stb.append("     AND PRI.PRISCHOOL_NAME LIKE '%" + _param._l900Name1 + "%' ");
        }
        if (null != _param._l900Name2) {
            stb.append("     AND BASE_D.REMARK2 LIKE '%" + _param._l900Name2 + "%' ");
        }
        stb.append(" ORDER BY ");
        stb.append("     PRI.PRISCHOOLCD, ");
        stb.append("     BASE_D.REMARK2, ");
        stb.append("     BASE_D.EXAMNO ");
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
        private final String _ctrlDate;
        private final String _applicantDiv;
        private final List _testDivList;
        private final String _l900Name1;
        private final String _l900Name2;
        final String _dateStr;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDivList = getTestDivList(db2);
            _ctrlDate = request.getParameter("CTRL_DATE").replace('/', '-');
            _dateStr = getDateStr(_ctrlDate);

            final String l900Sql = "SELECT NAME1, NAME2 FROM NAME_MST WHERE NAMECD1 = 'L900' AND NAMECD2 = '00'";
            PreparedStatement ps = null;
            ResultSet rs = null;
            String name1 = "";
            String name2 = "";
            try {
                ps = db2.prepareStatement(l900Sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    name1 = rs.getString("NAME1");
                    name2 = rs.getString("NAME2");
                }
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            _l900Name1 = name1;
            _l900Name2 = name2;
        }

        private String getDateStr(final String date) {
            final Calendar cal = Calendar.getInstance();
            final DecimalFormat df = new DecimalFormat("00");
            final int hour = cal.get(Calendar.HOUR_OF_DAY);
            final int minute = cal.get(Calendar.MINUTE);
            return KNJ_EditDate.h_format_JP(date) + "　" + df.format(hour) + "時" + df.format(minute) + "分現在";
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
        private final String _name;
        private final String _sex;
        private final String _prischoolCd;
        private final String _prischoolName;
        private final String _kyousituName;
        private final String _procedureDiv;
        private final String _entDiv;
        private final Map _receptMap;

        Student(final DB2UDB db2,
                final String examno,
                final String name,
                final String sex,
                final String prischoolCd,
                final String prischoolName,
                final String kyousituName,
                final String procedureDiv,
                final String entDiv
        ) throws SQLException {
            _examno = examno;
            _name = name;
            _sex = sex;
            _prischoolCd = prischoolCd;
            _prischoolName = prischoolName;
            _kyousituName = kyousituName;
            _procedureDiv = procedureDiv;
            _entDiv = entDiv;
            _receptMap = getRecept(db2, examno);
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
            } else 
            if ("1".equals(_judgeDiv) && ("2".equals(_judgeClass) || "3".equals(_judgeClass))) {
                retSt = "◎";
            } else 
            if ("1".equals(_judgeDiv) && ("1".equals(_judgeClass) || "4".equals(_judgeClass) || "6".equals(_judgeClass))) {
                retSt = "〇";
            } else 
            if ("2".equals(_judgeDiv)) {
                retSt = "×";
            }
            return retSt;
        }
    }
}

// eof
