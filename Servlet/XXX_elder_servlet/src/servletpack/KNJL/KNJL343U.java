/*
 * $Id: e66b832c8e8cc8123d87ca58dd188a1318adf1b5 $
 *
 * 作成日: 2017/11/02
 * 作成者: yamashiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
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

public class KNJL343U {

    private static final Log log = LogFactory.getLog(KNJL343U.class);

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
        svf.VrSetForm("KNJL343U.frm", 1);
        setTitle(svf);

        final List printStudentList = getList(db2);

        final int maxLine = 35;
        int lineCnt = 1;
        for (Iterator iterator = printStudentList.iterator(); iterator.hasNext();) {
            final Student student = (Student) iterator.next();
            if (lineCnt > maxLine) {
                svf.VrEndPage();
                setTitle(svf);
                lineCnt = 1;
            }
            svf.VrsOutn("JUDGE", lineCnt, student._entDivName);
            svf.VrsOutn("EXAM_NO", lineCnt, student._examno);
            final String nameField = KNJ_EditEdit.getMS932ByteLength(student._name) > 30 ? "3" : KNJ_EditEdit.getMS932ByteLength(student._name) > 20 ? "2" : "1";
            svf.VrsOutn("NAME" + nameField, lineCnt, student._name);
            final String kanaField = KNJ_EditEdit.getMS932ByteLength(student._nameKana) > 30 ? "3" : KNJ_EditEdit.getMS932ByteLength(student._nameKana) > 20 ? "2" : "1";
            svf.VrsOutn("KANA" + kanaField, lineCnt, student._nameKana);
            svf.VrsOutn("CITY_CD", lineCnt, student._fsLocationCd);
            svf.VrsOutn("CITY_NAME", lineCnt, student._districtName);

            lineCnt++;
            _hasData = true;
        }
        if (_hasData) {
            svf.VrEndPage();
        }
    }

    private void setTitle(final Vrw32alp svf) {
        svf.VrsOut("TITLE", KNJ_EditDate.h_format_JP_N(_param._entexamyear + "/04/01") + "　" + _param._applicantName + _param._testdivName + "　成績一覧表");
        svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._loginDate));
        final Calendar cal = Calendar.getInstance();
        final String sysDate = String.valueOf(cal.get(Calendar.YEAR)) + "-" + String.valueOf(cal.get(Calendar.MONTH)) + "-" + String.valueOf(cal.get(Calendar.DATE));
        final String printDateTime = new SimpleDateFormat("yyyy/MM/dd").format(Date.valueOf(sysDate)) + "　" + cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE);
        svf.VrsOut("DATE", printDateTime);
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
                final String entDivNme = rs.getString("ENTDIV_NAME");
                final String examno = rs.getString("EXAMNO");
                final String name = rs.getString("NAME");
                final String nameKana = rs.getString("NAME_KANA");
                final String fsLocationCd = rs.getString("FS_LOCATION_CD");
                final String districtName = rs.getString("DISTRICT_NAME");

                final Student student = new Student(entDivNme, examno, name, nameKana, fsLocationCd, districtName);
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
        stb.append("     L012.NAME1 AS ENTDIV_NAME, ");
        stb.append("     BASE.EXAMNO, ");
        stb.append("     BASE.NAME, ");
        stb.append("     BASE.NAME_KANA, ");
        stb.append("     FS_DETAIL002.REMARK1 AS FS_LOCATION_CD, ");
        stb.append("     FS_LOCATION.DISTRICT_NAME ");
        stb.append(" FROM ");
        stb.append("     V_ENTEXAM_APPLICANTBASE_DAT BASE ");
        stb.append("     INNER JOIN NAME_MST L013 ON L013.NAMECD1 = 'L013' ");
        stb.append("          AND BASE.JUDGEMENT = L013.NAMECD2 ");
        stb.append("          AND L013.NAMESPARE1 = '1' ");
        stb.append("     LEFT JOIN NAME_MST L012 ON L012.NAMECD1 = 'L012' ");
        stb.append("          AND BASE.ENTDIV = L012.NAMECD2 ");
        stb.append("     LEFT JOIN FINSCHOOL_DETAIL_MST FS_DETAIL002 ON BASE.FS_CD = FS_DETAIL002.FINSCHOOLCD ");
        stb.append("          AND FS_DETAIL002.FINSCHOOL_SEQ = '002' ");
        stb.append("     LEFT JOIN FINSCHOOL_LOCATION_MST FS_LOCATION ON FS_DETAIL002.REMARK1 = FS_LOCATION.DISTRICTCD ");
        stb.append(" WHERE ");
        stb.append("     BASE.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND BASE.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND BASE.TESTDIV = '" + _param._testDiv + "'  ");
        stb.append("     AND VALUE(BASE.PROCEDUREDIV, '0') = '1' ");
        stb.append("     AND VALUE(BASE.ENTDIV, '0') != '2' ");
        stb.append(" ORDER BY ");
        if ("1".equals(_param._sortDiv)) {
            stb.append("     BASE.EXAMNO ");
        } else {
            stb.append("     BASE.NAME_KANA, ");
            stb.append("     BASE.EXAMNO ");
        }
        return stb.toString();
    }

    private class Student {
        final String _entDivName;
        final String _examno;
        final String _name;
        final String _nameKana;
        final String _fsLocationCd;
        final String _districtName;
        public Student(
                final String entDivNme,
                final String examno,
                final String name,
                final String nameKana,
                final String fsLocationCd,
                final String districtName
        ) {
            _entDivName = entDivNme;
            _examno = examno;
            _name = name;
            _nameKana = nameKana;
            _fsLocationCd = fsLocationCd;
            _districtName = districtName;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 56927 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _applicantDiv;
        final String _testDiv;
        final String _sortDiv;
        final String _entexamyear;
        final String _loginYear;
        final String _loginSemester;
        final String _loginDate;
        final String _prgid;
        final String _applicantName;
        final String _testdivName;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _sortDiv = request.getParameter("SORT_DIV");
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _loginYear = request.getParameter("LOGIN_YEAR");
            _loginSemester = request.getParameter("LOGIN_SEMESTER");
            _loginDate = request.getParameter("LOGIN_DATE");
            _prgid = request.getParameter("PRGID");
            _applicantName = StringUtils.defaultString(getNameMst(db2, "NAME1", "L003", _applicantDiv));
            _testdivName = StringUtils.defaultString(getNameMst(db2, "NAME1", "L004", _testDiv));
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

    }
}

// eof
