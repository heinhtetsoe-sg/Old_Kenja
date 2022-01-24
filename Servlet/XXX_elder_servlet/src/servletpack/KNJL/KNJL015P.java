/*
 * $Id: e7b5068107f76889332e38c5d38c3e2d54b33e9b $
 *
 * 作成日: 2017/07/07
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

public class KNJL015P {

    private static final Log log = LogFactory.getLog(KNJL015P.class);

    private static final String NAME_AND_SCHOOL = "1";
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
        svf.VrSetForm("KNJL015P.frm", 4);
        final List studentList = getStudentList(db2);

        final int maxCnt = 35;
        final int pageCnt = studentList.size() >= maxCnt ? studentList.size() / maxCnt : 0;
        final int pageAmari = studentList.size() % maxCnt > 0 ? 1 : 0;
        final int allPage = pageCnt + pageAmari;
        setTitle(svf);

        int lineCnt = 1;
        final int maxLine = 35;
        int page = 1;
        for (Iterator itStudent = studentList.iterator(); itStudent.hasNext();) {
            if (lineCnt > maxLine) {
                lineCnt = 1;
                page++;
                setTitle(svf);
            }
            svf.VrsOut("PAGE1", String.valueOf(page));
            svf.VrsOut("PAGE2", String.valueOf(allPage));

            final Student student = (Student) itStudent.next();

            svf.VrsOut("EXAM_NO1", student._receptno);
            svf.VrsOut("FIX_NO", student._zenkiExamno);
            final String nameField = getMS932ByteLength(student._name) > 30 ? "1_2" : "1_1";
            svf.VrsOut("NAME" + nameField, student._name);
            final String nameFinSchoolField = getMS932ByteLength(student._finschoolName) > 30 ? "1_2" : "1_1";
            svf.VrsOut("FINSCHOOL_NAME" + nameFinSchoolField, student._finschoolName);

            svf.VrsOut("EXAM_NO2", student._receptno2);
            final String name2Field = getMS932ByteLength(student._name2) > 30 ? "2_2" : "2_1";
            svf.VrsOut("NAME" + name2Field, student._name2);
            final String nameFinSchoolField2 = getMS932ByteLength(student._finschoolName2) > 30 ? "2_2" : "2_1";
            svf.VrsOut("FINSCHOOL_NAME" + nameFinSchoolField2, student._finschoolName2);

            svf.VrEndRecord();
            lineCnt++;
            _hasData = true;
        }
    }

    private void setTitle(final Vrw32alp svf) {
        svf.VrsOut("TITLE", KNJ_EditDate.h_format_JP_N(_param._entexamYear + "-04-01") + "度　" + _param._applicantDivName + "入試 " + _param._testDivName + "　重複志願者名簿");
        svf.VrsOut("SUBTITLE", _param._getSubTitle());
        svf.VrsOut("DATE", "印刷日：" + KNJ_EditDate.h_format_JP(_param._loginDate));
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
                final String receptno = rs.getString("RECEPTNO");
                final String examno = rs.getString("EXAMNO");
                final String name = rs.getString("NAME");
                final String finschoolName = rs.getString("FINSCHOOL_NAME");
                final String zenkiExamno = rs.getString("ZENKI_EXAMNO");
                final String receptno2 = rs.getString("RECEPTNO2");
                final String examno2 = rs.getString("EXAMNO2");
                final String name2 = rs.getString("NAME2");
                final String finschoolName2 = rs.getString("FINSCHOOL_NAME2");

                final Student student = new Student(receptno, examno, name, finschoolName, zenkiExamno, receptno2, examno2, name2, finschoolName2);
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
        stb.append("     T1.RECEPTNO, ");
        stb.append("     T1.EXAMNO, ");
        stb.append("     T2.NAME, ");
        stb.append("     FIN_M.FINSCHOOL_NAME, ");
        stb.append("     DETAIL012.REMARK1 AS ZENKI_EXAMNO, ");
        stb.append("     RECEPT2.RECEPTNO AS RECEPTNO2, ");
        stb.append("     BASE2.EXAMNO AS EXAMNO2, ");
        stb.append("     BASE2.NAME AS NAME2, ");
        stb.append("     FIN_M2.FINSCHOOL_NAME AS FINSCHOOL_NAME2 ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_RECEPT_DAT T1 ");
        stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DAT T2 ");
        stb.append("              ON T2.ENTEXAMYEAR  = T1.ENTEXAMYEAR ");
        stb.append("             AND T2.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("             AND T2.EXAMNO       = T1.EXAMNO ");
        stb.append("     LEFT JOIN FINSCHOOL_MST FIN_M ");
        stb.append("              ON T2.FS_CD        = FIN_M.FINSCHOOLCD ");
        stb.append("     INNER JOIN ENTEXAM_RECEPT_DAT RECEPT2 ");
        stb.append("              ON T1.ENTEXAMYEAR  = RECEPT2.ENTEXAMYEAR ");
        stb.append("             AND T1.APPLICANTDIV = RECEPT2.APPLICANTDIV ");
        stb.append("             AND RECEPT2.TESTDIV = '2' ");
        stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE2 ");
        stb.append("              ON RECEPT2.ENTEXAMYEAR  = BASE2.ENTEXAMYEAR ");
        stb.append("             AND RECEPT2.APPLICANTDIV = BASE2.APPLICANTDIV ");
        stb.append("             AND RECEPT2.EXAMNO       = BASE2.EXAMNO ");
        stb.append("             AND T2.NAME              = BASE2.NAME ");
        if ("1".equals(_param._whereDiv)) {
            stb.append("             AND T2.FS_CD             = BASE2.FS_CD ");
        }
        stb.append("     LEFT JOIN FINSCHOOL_MST FIN_M2 ");
        stb.append("              ON BASE2.FS_CD        = FIN_M2.FINSCHOOLCD ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT DETAIL012 ");
        stb.append("           ON T1.ENTEXAMYEAR  = DETAIL012.ENTEXAMYEAR ");
        stb.append("          AND T1.APPLICANTDIV = DETAIL012.APPLICANTDIV ");
        stb.append("          AND T1.EXAMNO       = DETAIL012.EXAMNO ");
        stb.append("          AND DETAIL012.SEQ   = '012' ");
        stb.append(" WHERE ");
        stb.append("         T1.ENTEXAMYEAR    = '" + _param._entexamYear + "' ");
        stb.append("     AND T1.APPLICANTDIV   = '" + _param._applicantDiv + "' ");
        stb.append("     AND T1.TESTDIV        = '" + _param._testDiv + "' ");
        if (!_param._isKakuteiDisp) {
            stb.append("     AND DETAIL012.EXAMNO IS NULL ");
        }
        stb.append(" ORDER BY ");
        stb.append("     T1.RECEPTNO ");
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
        final String _whereDiv;
        final boolean _isKakuteiDisp;
        final String _entexamYear;
        final String _loginYear;
        final String _loginSemester;
        final String _loginDate;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _whereDiv = request.getParameter("WHERE_DIV");
            _isKakuteiDisp = "1".equals(request.getParameter("KAKUTEI_DISP"));
            _entexamYear = request.getParameter("ENTEXAMYEAR");
            _loginYear = request.getParameter("LOGIN_YEAR");
            _loginSemester = request.getParameter("LOGIN_SEMESTER");
            _loginDate = request.getParameter("LOGIN_DATE");
            _applicantDivName = getNameMst(db2, "L003", _applicantDiv, "NAME1");
            final String testNameCd1 = "1".equals(_applicantDiv) ? "L024" : "L004";
            _testDivName = getNameMst(db2, testNameCd1, _testDiv, "NAME1");
            _testDate = getNameMst(db2, testNameCd1, _testDiv, "NAMESPARE1");
        }

        public String _getSubTitle() {
            if (NAME_AND_SCHOOL.equals(_whereDiv)) {
                return "(名前＋出身学校)";
            } else {
                return "(名前)";
            }
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
        final String _receptno;
        final String _examno;
        final String _name;
        final String _finschoolName;
        final String _zenkiExamno;
        final String _receptno2;
        final String _examno2;
        final String _name2;
        final String _finschoolName2;

        public Student(
                final String receptno,
                final String examno,
                final String name,
                final String finschoolName,
                final String zenkiExamno,
                final String receptno2,
                final String examno2,
                final String name2,
                final String finschoolName2
        ) throws SQLException {
            _receptno = receptno;
            _examno = examno;
            _name = name;
            _finschoolName = finschoolName;
            _zenkiExamno = zenkiExamno;
            _receptno2 = receptno2;
            _examno2 = examno2;
            _name2 = name2;
            _finschoolName2 = finschoolName2;
        }
    }
}

// eof

