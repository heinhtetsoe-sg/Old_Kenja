/*
 * $Id: 6ae022278b45b599c6be103f457133c3688e1e18 $
 *
 * 作成日: 2017/06/29
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

public class KNJL329P {

    private static final Log log = LogFactory.getLog(KNJL329P.class);

    private static final String MENSETU_NASHI = "1";
    private static final String MENSETU_ARI = "2";

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
        final List studentList = getStudentList(db2);
        svf.VrSetForm("KNJL329P.frm", 1);

        final int maxCnt = 50;
        final int pageCnt = studentList.size() >= maxCnt ? studentList.size() / maxCnt : 0;
        final int pageAmari = studentList.size() % maxCnt > 0 ? 1 : 0;
        final int allPage = pageCnt + pageAmari;
        final String title = KNJ_EditDate.h_format_JP_N(_param._entexamYear + "-04-01") + "度　" + _param._applicantDivName + "入試 " + _param._testDivName + "　受験番号／成績順チェックリスト";
        final String subTitle = "1".equals(_param._orderDiv) ? "（受験番号順）" : "（成績順）";
        final String printDate = "印刷日：" + KNJ_EditDate.h_format_JP(_param._loginDate);

        int noCnt = 1;
        int lineCnt = 1;
        final int maxLine = 50;
        int page = 1;
        for (Iterator itStudent = studentList.iterator(); itStudent.hasNext();) {
            if (lineCnt > maxLine) {
                lineCnt = 1;
                page++;
                svf.VrEndPage();
            }

            //ヘッダー部
            svf.VrsOut("PAGE1", String.valueOf(page));
            svf.VrsOut("PAGE2", String.valueOf(allPage));
            svf.VrsOut("TITLE", title);
            svf.VrsOut("SUBTITLE", subTitle);
            svf.VrsOut("DATE", printDate);

            //データ部
            final Student student = (Student) itStudent.next();
            svf.VrsOutn("NO", lineCnt, String.valueOf(noCnt));
            svf.VrsOutn("EXAM_NO1", lineCnt, student._examNo);
            final String nameField = getMS932ByteLength(student._name) > 30 ? "3" : getMS932ByteLength(student._name) > 20 ? "2" : "1";
            svf.VrsOutn("NAME" + nameField, lineCnt, student._name);
            svf.VrsOutn("SEX", lineCnt, student._sexName);
            if (MENSETU_NASHI.equals(_param._mensetu)) {
                svf.VrsOutn("RANK", lineCnt, student._totalRank4);
            } else {
                svf.VrsOutn("RANK", lineCnt, student._totalRank2);
            }
            if ("1".equals(student._judgediv)) {
            	svf.VrsOutn("REMARK1", lineCnt, "＊");
            }

            noCnt++;
            lineCnt++;
            _hasData = true;
        }
        if (_hasData) {
            svf.VrEndPage();
        }
    }

    private List getStudentList(final DB2UDB db2) throws SQLException {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = studentSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String receptNo = rs.getString("RECEPTNO");
                final String examNo = rs.getString("EXAMNO");
                final String name = rs.getString("NAME");
                final String sexName = rs.getString("SEX_NAME");
                final String totalRank4 = rs.getString("TOTAL_RANK4");
                final String totalRank2 = rs.getString("TOTAL_RANK2");
                final String judgediv = rs.getString("JUDGEDIV");

                final Student student = new Student(receptNo, examNo, name, sexName, totalRank4, totalRank2, judgediv);
                retList.add(student);
            }
            db2.commit();
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
        return retList;
    }

    private String studentSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     RECEPT.RECEPTNO, ");
        stb.append("     RECEPT.EXAMNO, ");
        stb.append("     BASE.NAME, ");
        stb.append("     Z002.ABBV1 AS SEX_NAME, ");
        stb.append("     RECEPT.TOTAL_RANK4, ");
        stb.append("     RECEPT.TOTAL_RANK2, ");
        stb.append("     RECEPT.JUDGEDIV ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_RECEPT_DAT RECEPT ");
        stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE ON BASE.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR ");
        stb.append("         AND BASE.APPLICANTDIV = RECEPT.APPLICANTDIV ");
        stb.append("         AND BASE.EXAMNO = RECEPT.EXAMNO ");
        stb.append("     LEFT JOIN NAME_MST Z002 ON Z002.NAMECD1 = 'Z002' ");
        stb.append("         AND Z002.NAMECD2 = BASE.SEX ");
        stb.append(" WHERE ");
        stb.append("     RECEPT.ENTEXAMYEAR = '" + _param._entexamYear + "' ");
        stb.append("     AND RECEPT.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND RECEPT.TESTDIV = '" + _param._testDiv + "' ");
        stb.append(" ORDER BY ");
        if ("1".equals(_param._orderDiv)) {
        } else if ("1".equals(_param._mensetu)) {
            stb.append("     RECEPT.TOTAL_RANK4, ");
        } else {
            stb.append("     RECEPT.TOTAL_RANK2, ");
        }
        stb.append("     RECEPT.EXAMNO ");

        return stb.toString();
    }

    /**
     *  文字数を取得
     */
    private static int getMS932ByteLength(final String str)
    {
        int ret = 0;
        if (null != str) {
            try {
                ret = str.getBytes("MS932").length;
            } catch (Exception ex) {
                log.error("retStringByteValue error!", ex);
            }
        }
        return ret;
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 57819 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _entexamYear;
        final String _applicantDiv;
        final String _testDiv;
        final String _mensetu;
        final String _orderDiv;
        final String _loginYear;
        final String _loginSemester;
        final String _loginDate;

        final String _applicantDivName;
        final String _testDivName;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamYear = request.getParameter("ENTEXAMYEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _mensetu = request.getParameter("MENSETU");
            _orderDiv = request.getParameter("ORDERDIV");
            _loginYear = request.getParameter("LOGIN_YEAR");
            _loginSemester = request.getParameter("LOGIN_SEMESTER");
            _loginDate = request.getParameter("LOGIN_DATE");

            _applicantDivName = getNameMst(db2, "L003", _applicantDiv, "NAME1");
            final String testNameCd1 = "1".equals(_applicantDiv) ? "L024" : "L004";
            _testDivName = getNameMst(db2, testNameCd1, _testDiv, "NAME1");
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

    /** 生徒クラス */
    private class Student {
        final String _receptNo;
        final String _examNo;
        final String _name;
        final String _sexName;
        final String _totalRank4;
        final String _totalRank2;
        final String _judgediv;

        public Student(
                final String receptNo,
                final String examNo,
                final String name,
                final String sexName,
                final String totalRank4,
                final String totalRank2,
                final String judgediv
        ) throws SQLException {
            _receptNo = receptNo;
            _examNo = examNo;
            _name = name;
            _sexName = sexName;
            _totalRank4 = totalRank4;
            _totalRank2 = totalRank2;
            _judgediv = judgediv;
        }
    }
}

// eof

