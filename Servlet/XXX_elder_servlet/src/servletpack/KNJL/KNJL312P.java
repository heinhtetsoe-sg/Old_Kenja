/*
 * $Id: b676b8042136e520db37c7bd76baf088cd6a9f3b $
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
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

public class KNJL312P {

    private static final Log log = LogFactory.getLog(KNJL312P.class);

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
        svf.VrSetForm("KNJL312P.frm", 1);

        final List studentList = getStudentList(db2);
        final int maxCnt = 120;
        final int pageCnt = studentList.size() >= maxCnt ? studentList.size() / maxCnt : 0;
        final int pageAmari = studentList.size() % maxCnt > 0 ? 1 : 0;
        final int allPage = pageCnt + pageAmari;
        setTitle(svf);

        int lineCnt = 1;
        final int maxLine = 30;
        int retsuCnt = 1;
        final int maxRetsu = 4;
        int page = 1;
        for (Iterator itStudent = studentList.iterator(); itStudent.hasNext();) {
            if (lineCnt > maxLine) {
                lineCnt = 1;
                retsuCnt++;
            }
            if (retsuCnt > maxRetsu) {
                lineCnt = 1;
                retsuCnt = 1;
                page++;
                svf.VrEndPage();
                setTitle(svf);
            }
            svf.VrsOut("PAGE1", String.valueOf(page));
            svf.VrsOut("PAGE2", String.valueOf(allPage));

            final Student student = (Student) itStudent.next();
            svf.VrsOutn("EXAM_NO" + retsuCnt, lineCnt, student._examNo);
            svf.VrsOutn("POINT" + retsuCnt, lineCnt, student.getScore());

            lineCnt++;
            _hasData = true;
        }
        if (_hasData) {
            svf.VrEndPage();
        }
    }

    private void setTitle(final Vrw32alp svf) {
        svf.VrsOut("TITLE", KNJ_EditDate.h_format_JP_N(_param._entexamyear + "-04-01") + "度　" + _param._applicantDivName + "入試 " + _param._testDivName + "　入力確認");
        svf.VrsOut("CLASS_NAME", _param._testSubclassName);
        svf.VrsOut("DATE", "実施日：" + KNJ_EditDate.h_format_JP(_param._testDate));
    }

    private List getStudentList(final DB2UDB db2) throws SQLException {
        final List retList = new ArrayList();

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final String sql = sql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String examNo = rs.getString("EXAMNO");
                final String judgeDiv = rs.getString("JUDGEDIV");
                final String attendFlg = rs.getString("ATTEND_FLG");
                final String score = rs.getString("SCORE");

                final Student student = new Student(examNo, judgeDiv, attendFlg, score);
                retList.add(student);
            }

            db2.commit();
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
        return retList;
    }

    private String sql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     RECEPT.EXAMNO, ");
        stb.append("     RECEPT.JUDGEDIV, ");
        stb.append("     SCORE.ATTEND_FLG, ");
        stb.append("     SCORE.SCORE ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_RECEPT_DAT RECEPT ");
        stb.append("     LEFT JOIN ENTEXAM_SCORE_DAT SCORE ON RECEPT.ENTEXAMYEAR = SCORE.ENTEXAMYEAR ");
        stb.append("          AND RECEPT.APPLICANTDIV = SCORE.APPLICANTDIV ");
        stb.append("          AND RECEPT.TESTDIV = SCORE.TESTDIV ");
        stb.append("          AND RECEPT.EXAM_TYPE = SCORE.EXAM_TYPE ");
        stb.append("          AND RECEPT.RECEPTNO = SCORE.RECEPTNO ");
        stb.append("          AND SCORE.TESTSUBCLASSCD = '" + _param._testSubclassCd + "' ");
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
        final String _testSubclassCd;
        final String _testSubclassName;
        final String _entexamyear;
        final String _loginYear;
        final String _loginSemester;
        final String _loginDate;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _testSubclassCd = request.getParameter("TESTSUBCLASSCD");
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _loginYear = request.getParameter("LOGIN_YEAR");
            _loginSemester = request.getParameter("LOGIN_SEMESTER");
            _loginDate = request.getParameter("LOGIN_DATE");
            _applicantDivName = getNameMst(db2, "L003", _applicantDiv, "NAME1");
            final String testNameCd1 = "1".equals(_applicantDiv) ? "L024" : "L004";
            _testDivName = getNameMst(db2, testNameCd1, _testDiv, "NAME1");
            _testDate = getNameMst(db2, testNameCd1, _testDiv, "NAMESPARE1");
            final String subclassName = "1".equals(_applicantDiv) ? "NAME1" : "NAME2";
            _testSubclassName = getNameMst(db2, "L009", _testSubclassCd, subclassName);
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
            stb.append("     AND NAMECD2 = '" + namecd2 + "' ");
            return stb.toString();
        }

    }

    /** パラメータクラス */
    private class Student {
        final String _examNo;
        final String _judgeDiv;
        final String _attendFlg;
        final String _score;
        public Student(
                final String examNo,
                final String judgeDiv,
                final String attendFlg,
                final String score
        ) {
            _examNo = examNo;
            _judgeDiv = judgeDiv;
            _attendFlg = attendFlg;
            _score = score;
        }

        public String getScore() {
            String retStr = "";
            if ("0".equals(_attendFlg) || "4".equals(_judgeDiv)) {
                retStr = "欠";
            } else {
                retStr = _score;
            }
            return retStr;
        }
    }
}

// eof

