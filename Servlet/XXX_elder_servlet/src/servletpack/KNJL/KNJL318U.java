/*
 * $Id: 9ce7b1bc73fff8787821101590fce06cb15cc8be $
 *
 * 作成日: 2017/11/01
 * 作成者: yamashiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

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
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJL318U {

    private static final Log log = LogFactory.getLog(KNJL318U.class);

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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        final String form = _param._isPrintName ? "KNJL318U.frm" : "KNJL318U_2.frm";
        svf.VrSetForm(form, 1);
        setTitle(svf);

        final List printStudentList = getList(db2);

        final int maxLine = 40;
        int lineCnt = 1;
        for (Iterator iterator = printStudentList.iterator(); iterator.hasNext();) {
            final Student student = (Student) iterator.next();
            if (lineCnt > maxLine) {
                svf.VrEndPage();
                setTitle(svf);
                lineCnt = 1;
            }
            svf.VrsOutn("JUDGE", lineCnt, student._judgename);
            svf.VrsOutn("EXAM_NO", lineCnt, student._examno);
            final String nameField = KNJ_EditEdit.getMS932ByteLength(student._name) > 30 ? "3" : KNJ_EditEdit.getMS932ByteLength(student._name) > 20 ? "2" : "1";
            svf.VrsOutn("NAME" + nameField, lineCnt, student._name);
            final String kanaField = KNJ_EditEdit.getMS932ByteLength(student._nameKana) > 30 ? "3" : KNJ_EditEdit.getMS932ByteLength(student._nameKana) > 20 ? "2" : "1";
            svf.VrsOutn("KANA" + kanaField, lineCnt, student._nameKana);
            svf.VrsOutn("CITY_CD", lineCnt, student._fsLocationCd);
            final String districtField = KNJ_EditEdit.getMS932ByteLength(student._districtName) > 40 ? "3" : KNJ_EditEdit.getMS932ByteLength(student._districtName) > 30 ? "2" : "1";
            svf.VrsOutn("CITY_NAME" + districtField, lineCnt, student._districtName);
            svf.VrsOutn("FLG", lineCnt, student._slideName);
            int scoreCnt = 1;
            for (Iterator itScore = student._scoreList.iterator(); itScore.hasNext();) {
                final String score = (String) itScore.next();
                svf.VrsOutn("SCORE" + scoreCnt, lineCnt, score);
                scoreCnt++;
            }
            if (student._total4 != null) {
                svf.VrsOutn("SUBTOTAL", lineCnt, student._total4);
            }
            if (student._totalRank4 != null) {
                svf.VrsOutn("RANK", lineCnt, student._totalRank4);
            }

            lineCnt++;
            _hasData = true;
        }
        if (_hasData) {
            svf.VrEndPage();
        }
    }

    private void setTitle(final Vrw32alp svf) {
        svf.VrsOut("TITLE", KNJ_EditDate.h_format_JP_N(_param._entexamyear + "/04/01") + "度　" + _param._applicantName + "　" + _param._testdivName + "　成績一覧表");
        final Calendar cal = Calendar.getInstance();
        final String printDateTime = KNJ_EditDate.h_format_thi(_param._loginDate, 0) + "　" + cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE);
        svf.VrsOut("DATE", printDateTime);
        int kamokuCnt = 1;
        for (Iterator itKamoku = _param._testKamokuList.iterator(); itKamoku.hasNext();) {
            TestKamoku testKamoku = (TestKamoku) itKamoku.next();
            svf.VrsOut("CLASS_NAME" + kamokuCnt, testKamoku._name);
            kamokuCnt++;
        }
    }

    private List getList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String judgename = rs.getString("JUDGENAME");
                final String examno = rs.getString("EXAMNO");
                final String name = rs.getString("NAME");
                final String nameKana = rs.getString("NAME_KANA");
                final String fsLocationCd = rs.getString("FS_LOCATION_CD");
                final String districtName = rs.getString("DISTRICT_NAME");
                final String slideName = rs.getString("SLIDE_NAME");
                final String total4 = rs.getString("TOTAL4");
                final String totalRank4 = rs.getString("TOTAL_RANK4");
                final List scoreList = new ArrayList();
                for (Iterator itTestKamoku = _param._testKamokuList.iterator(); itTestKamoku.hasNext();) {
                    final TestKamoku testKamoku = (TestKamoku) itTestKamoku.next();
                    final String score = rs.getString("SCORE" + testKamoku._cd);
                    scoreList.add(score);
                }

                final Student student = new Student(judgename, examno, name, nameKana, fsLocationCd, districtName, slideName, total4, totalRank4, scoreList);
                retList.add(student);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String getSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     L013.NAME1 AS JUDGENAME, ");
        stb.append("     RECEPT.EXAMNO, ");
        stb.append("     BASE.NAME, ");
        stb.append("     BASE.NAME_KANA, ");
        stb.append("     FS_DETAIL002.REMARK1 AS FS_LOCATION_CD, ");
        stb.append("     FS_LOCATION.DISTRICT_NAME, ");
        stb.append("     CASE WHEN BASE.SLIDE_FLG = '1' ");
        stb.append("          THEN '有' ");
        stb.append("          ELSE '' ");
        stb.append("     END AS SLIDE_NAME, ");
        for (Iterator itTestKamoku = _param._testKamokuList.iterator(); itTestKamoku.hasNext();) {
            final TestKamoku testKamoku = (TestKamoku) itTestKamoku.next();
            stb.append("     SCORE_D" + testKamoku._cd + ".SCORE AS SCORE" + testKamoku._cd + ", ");
        }
        stb.append("     RECEPT.TOTAL4, ");
        stb.append("     RECEPT.TOTAL_RANK4 ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_RECEPT_DAT RECEPT ");
        stb.append("     LEFT JOIN V_ENTEXAM_APPLICANTBASE_DAT BASE ON RECEPT.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
        stb.append("          AND RECEPT.APPLICANTDIV = BASE.APPLICANTDIV ");
        stb.append("          AND RECEPT.EXAMNO = BASE.EXAMNO ");
        stb.append("          AND RECEPT.TESTDIV= BASE.TESTDIV ");
        stb.append("          AND RECEPT.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
        stb.append("     LEFT JOIN NAME_MST L013 ON L013.NAMECD1 = 'L013' ");
        stb.append("          AND BASE.JUDGEMENT = L013.NAMECD2 ");
        stb.append("          AND L013.NAMESPARE1 = '1' ");
        stb.append("     LEFT JOIN FINSCHOOL_DETAIL_MST FS_DETAIL002 ON BASE.FS_CD = FS_DETAIL002.FINSCHOOLCD ");
        stb.append("          AND FS_DETAIL002.FINSCHOOL_SEQ = '002' ");
        stb.append("     LEFT JOIN FINSCHOOL_LOCATION_MST FS_LOCATION ON FS_DETAIL002.REMARK1 = FS_LOCATION.DISTRICTCD ");
        for (Iterator itTestKamoku = _param._testKamokuList.iterator(); itTestKamoku.hasNext();) {
            final TestKamoku testKamoku = (TestKamoku) itTestKamoku.next();
            final String fieldName = "SCORE_D" + testKamoku._cd;
            stb.append("     LEFT JOIN ENTEXAM_SCORE_DAT " + fieldName + " ON RECEPT.ENTEXAMYEAR = " + fieldName + ".ENTEXAMYEAR ");
            stb.append("          AND RECEPT.APPLICANTDIV = " + fieldName + ".APPLICANTDIV ");
            stb.append("          AND RECEPT.TESTDIV = " + fieldName + ".TESTDIV ");
            stb.append("          AND " + fieldName + ".EXAM_TYPE = '1' ");
            stb.append("          AND RECEPT.RECEPTNO = " + fieldName + ".RECEPTNO ");
            stb.append("          AND " + fieldName + ".TESTSUBCLASSCD = '" + testKamoku._cd + "' ");
        }
        stb.append(" WHERE ");
        stb.append("     RECEPT.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND RECEPT.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND RECEPT.TESTDIV = '" + _param._testDiv + "'  ");
        stb.append(" ORDER BY ");
        if ("1".equals(_param._sortDiv)) {
            stb.append("     RECEPT.EXAMNO ");
        } else if ("2".equals(_param._sortDiv)) {
            stb.append("     RECEPT.TOTAL4 DESC, ");
            stb.append("     RECEPT.EXAMNO ");
        } else {
            stb.append("     BASE.NAME_KANA, ");
            stb.append("     RECEPT.EXAMNO ");
        }
        return stb.toString();
    }

    private class Student {
        final String _judgename;
        final String _examno;
        final String _name;
        final String _nameKana;
        final String _fsLocationCd;
        final String _districtName;
        final String _slideName;
        final String _total4;
        final String _totalRank4;
        final List _scoreList;
        public Student(
                final String judgename,
                final String examno,
                final String name,
                final String nameKana,
                final String fsLocationCd,
                final String districtName,
                final String slideName,
                final String total4,
                final String totalRank4,
                final List scoreList
        ) {
            _judgename = judgename;
            _examno = examno;
            _name = name;
            _nameKana = nameKana;
            _fsLocationCd = fsLocationCd;
            _districtName = districtName;
            _slideName = slideName;
            _total4 = total4;
            _totalRank4 = totalRank4;
            _scoreList = scoreList;
        }
    }

    private class TestKamoku {
        final String _cd;
        final String _name;
        public TestKamoku(
                final String cd,
                final String name
        ) {
            _cd = cd;
            _name = name;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 63990 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _applicantDiv;
        final String _testDiv;
        final String _sortDiv;
        final boolean _isPrintName;
        final String _entexamyear;
        final String _loginYear;
        final String _loginSemester;
        final String _loginDate;
        final String _prgid;
        final String _applicantName;
        final String _testdivName;
        private final List _testKamokuList;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _sortDiv = request.getParameter("SORT_DIV");
            _isPrintName = "1".equals(request.getParameter("PRINT_NAME"));
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _loginYear = request.getParameter("LOGIN_YEAR");
            _loginSemester = request.getParameter("LOGIN_SEMESTER");
            _loginDate = request.getParameter("LOGIN_DATE");
            _prgid = request.getParameter("PRGID");
            _applicantName = StringUtils.defaultString(getNameMst(db2, "NAME1", "L003", _applicantDiv));
            _testdivName = StringUtils.defaultString(getNameMst(db2, "NAME1", "L004", _testDiv));
            _testKamokuList = getTestKamokuList(db2);
        }

        private String getNameMst(final DB2UDB db2, final String field, final String namecd1, final String namecd2) {
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT " + field + " FROM NAME_MST WHERE NAMECD1 = '" + namecd1 + "' AND NAMECD2 = '" + namecd2 + "' ");
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString(field);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return rtn;
        }

        private List getTestKamokuList(final DB2UDB db2) {
            final List retList = new ArrayList();
            final String sql = "SELECT NAMECD2, NAME1 FROM NAME_MST WHERE NAMECD1 = 'L009' ORDER BY NAMECD2";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String cd = rs.getString("NAMECD2");
                    final String name = rs.getString("NAME1");
                    final TestKamoku testKamoku = new TestKamoku(cd, name);
                    retList.add(testKamoku);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return retList;
        }

    }
}

// eof
