/*
 * $Id: 4c29425ac649a158bbe13b2bd90447635ed3cb4f $
 *
 * 作成日: 2017/11/02
 * 作成者: tawada
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

public class KNJL344U {

    private static final Log log = LogFactory.getLog(KNJL344U.class);

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
        svf.VrSetForm("KNJL344U.frm", 1);
        final List printList = getList(db2);
        final int maxLine = 25;
        int printLine = 1;

        setTitle(svf);//ヘッダ
        for (Iterator iterator = printList.iterator(); iterator.hasNext();) {
            final PrintData printData = (PrintData) iterator.next();
            if (printLine > maxLine) {
                printLine = 1;
                svf.VrEndPage();
                setTitle(svf);
            }

            //データ
            svf.VrsOutn("JUDGE", printLine, printData._entDivName);
            svf.VrsOutn("EXAM_NO", printLine, printData._examNo);
            final String nameField = KNJ_EditEdit.getMS932ByteLength(printData._name) > 40 ? "3" : KNJ_EditEdit.getMS932ByteLength(printData._name) > 30 ? "2" : "1";
            svf.VrsOutn("NAME" + nameField, printLine, printData._name);
            final String nameKanaField = KNJ_EditEdit.getMS932ByteLength(printData._nameKana) > 40 ? "3" : KNJ_EditEdit.getMS932ByteLength(printData._nameKana) > 30 ? "2" : "1";
            svf.VrsOutn("KANA" + nameKanaField, printLine, printData._nameKana);
            svf.VrsOutn("CITY_CD", printLine, printData._fsLocationCd);
            svf.VrsOutn("FINSCHOOL_NAME", printLine, printData._districtName);
            printLine++;

            _hasData = true;
        }
        if (_hasData) {
            svf.VrEndPage();
        }
    }

    private void setTitle(final Vrw32alp svf) {
        String setYear = KNJ_EditDate.h_format_JP_N(_param._entExamYear + "/04/01");
        svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._loginDate));
        final Calendar cal = Calendar.getInstance();
        final String sysDate = String.valueOf(cal.get(Calendar.YEAR)) + "-" + String.valueOf(cal.get(Calendar.MONTH)) + "-" + String.valueOf(cal.get(Calendar.DATE));
        final String printDateTime = new SimpleDateFormat("yyyy/MM/dd").format(Date.valueOf(sysDate)) + "　" + cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE);
        svf.VrsOut("DATE", printDateTime);
        final String setTitle = setYear + "度　入学者一覧表（" + _param._applicantdivName + _param._testdivName + "）";
        svf.VrsOut("TITLE", setTitle);
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
                final String entDiv = rs.getString("ENTDIV");
                final String examNo = rs.getString("EXAMNO");
                final String name = rs.getString("NAME");
                final String nameKana = rs.getString("NAME_KANA");
                final String fsLocationCd = rs.getString("FS_LOCATION_CD");
                final String districtName = rs.getString("DISTRICT_NAME");

                final PrintData printData = new PrintData(entDiv, examNo, name, nameKana, fsLocationCd, districtName);
                retList.add(printData);
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
        stb.append("     L012.NAME1 AS ENTDIV, ");
        stb.append("     BASE.EXAMNO, ");
        stb.append("     BASE.NAME, ");
        stb.append("     BASE.NAME_KANA, ");
        stb.append("     FS_DETAIL002.REMARK1 AS FS_LOCATION_CD, ");
        stb.append("     FS_LOCATION.DISTRICT_NAME AS DISTRICT_NAME ");
        stb.append(" FROM ");
        stb.append("     V_ENTEXAM_APPLICANTBASE_DAT BASE ");
        stb.append("     INNER JOIN NAME_MST L013 ON L013.NAMECD1    = 'L013' ");
        stb.append("                             AND BASE.JUDGEMENT  = L013.NAMECD2 ");
        stb.append("                             AND L013.NAMESPARE1 = '1' ");
        stb.append("     LEFT JOIN NAME_MST L012 ON L012.NAMECD1 = 'L012' ");
        stb.append("                            AND BASE.ENTDIV  = L012.NAMECD2 ");
        stb.append("     LEFT JOIN FINSCHOOL_DETAIL_MST FS_DETAIL002 ON BASE.FS_CD = FS_DETAIL002.FINSCHOOLCD ");
        stb.append("                                                AND FS_DETAIL002.FINSCHOOL_SEQ = '002' ");
        stb.append("     LEFT JOIN FINSCHOOL_LOCATION_MST FS_LOCATION ON FS_DETAIL002.REMARK1 = FS_LOCATION.DISTRICTCD ");
        stb.append(" WHERE ");
        stb.append("         BASE.ENTEXAMYEAR  = '" + _param._entExamYear + "' ");
        stb.append("     AND BASE.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND BASE.TESTDIV      = '" + _param._testDiv + "' ");
        stb.append("     AND BASE.ENTDIV       = '1' ");
        stb.append(" ORDER BY ");
        if ("1".equals(_param._sortDiv)) {
            stb.append("     BASE.EXAMNO ");
        } else {
            stb.append("     BASE.NAME_KANA ");
        }

        return stb.toString();
    }

    private class PrintData {
        final String _entDivName;
        final String _examNo;
        final String _name;
        final String _nameKana;
        final String _fsLocationCd;
        final String _districtName;
        public PrintData(
                final String entDivName,
                final String examNo,
                final String name,
                final String nameKana,
                final String fsLocationCd,
                final String districtName
        ) {
            _entDivName    = entDivName;
            _examNo        = examNo;
            _name          = name;
            _nameKana      = nameKana;
            _fsLocationCd  = fsLocationCd;
            _districtName  = districtName;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 56913 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _loginYear;
        private final String _loginSemester;
        private final String _loginDate;
        private final String _applicantDiv;
        private final String _testDiv;
        private final String _entExamYear;
        private final String _applicantdivName;
        private final String _testdivName;
        private final String _sortDiv;
        private final String _schoolName;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _loginYear     = request.getParameter("LOGIN_YEAR");
            _loginSemester = request.getParameter("LOGIN_SEMESTER");
            _loginDate     = request.getParameter("LOGIN_DATE");
            _applicantDiv  = request.getParameter("APPLICANTDIV");
            _testDiv       = request.getParameter("TESTDIV");
            _sortDiv       = request.getParameter("SORT_DIV");
            _entExamYear   = request.getParameter("ENTEXAMYEAR");
            _applicantdivName = getNameMst(db2, "NAME1", "L003", _applicantDiv);
            _testdivName = StringUtils.defaultString(getNameMst(db2, "NAME1", "L004", _testDiv));
            _schoolName    = getSchoolName(db2, _loginYear);

        }

        private String getSchoolName(final DB2UDB db2, final String year) {
            String retSchoolName = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement("SELECT SCHOOLNAME1 FROM SCHOOL_MST WHERE YEAR = '" + year + "' ");
                rs = ps.executeQuery();
                if (rs.next()) {
                    retSchoolName = rs.getString("SCHOOLNAME1");
                }
            } catch (SQLException ex) {
                log.debug("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retSchoolName;
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
