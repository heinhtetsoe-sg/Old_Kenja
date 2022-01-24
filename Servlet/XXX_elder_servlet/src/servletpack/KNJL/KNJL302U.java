/*
 * $Id: b6ace221b7424a9e42cc6c0c127da7c3c8c25c18 $
 *
 * 作成日: 2017/10/31
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

public class KNJL302U {

    private static final Log log = LogFactory.getLog(KNJL302U.class);

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
        svf.VrSetForm("KNJL302U.frm", 1);
        final List printList = getList(db2);
        final int maxLine = 50;
        int printLine = 1;
        String befHall = "";

        setTitle(svf);//ヘッダ
        for (Iterator iterator = printList.iterator(); iterator.hasNext();) {
            final PrintData printData = (PrintData) iterator.next();
            if (!"".equals(befHall) && !befHall.equals(printData._examHallCd)) {
                printLine = 1;
                svf.VrEndPage();
                setTitle(svf);
            }
            if (printLine > maxLine) {
                printLine = 1;
                svf.VrEndPage();
                setTitle(svf);
            }
            svf.VrsOut("HALL_NAME", printData._examHallName);//会場名

            //データ
            svf.VrsOutn("EXAM_NO", printLine, printData._examNo);
            final String nameField = KNJ_EditEdit.getMS932ByteLength(printData._name) > 30 ? "3" : KNJ_EditEdit.getMS932ByteLength(printData._name) > 20 ? "2" : "1";
            svf.VrsOutn("NAME" + nameField, printLine, printData._name);
            final String nameKanaField = KNJ_EditEdit.getMS932ByteLength(printData._finSchoolName) > 40 ? "3" : KNJ_EditEdit.getMS932ByteLength(printData._finSchoolName) > 30 ? "2" : "1";
            svf.VrsOutn("FINSCHOOL_NAME" + nameKanaField, printLine, printData._finSchoolName);
            printLine++;
            befHall = printData._examHallCd;

            _hasData = true;
        }
        if (_hasData) {
            svf.VrEndPage();
        }
    }

    private void setTitle(final Vrw32alp svf) {
        String setYear = KNJ_EditDate.h_format_JP_N(_param._entExamYear + "/04/01");
        final Calendar cal = Calendar.getInstance();
        final String printDateTime = KNJ_EditDate.h_format_thi(_param._loginDate, 0) + "　" + cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE);
        svf.VrsOut("DATE", printDateTime);
        svf.VrsOut("TITLE", setYear + "度　" + _param._applicantdivName + "　" + _param._testdivName + "　出欠者リスト");
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
                final String examNo = rs.getString("EXAMNO");
                final String name = rs.getString("NAME");
                final String nameKana = rs.getString("NAME_KANA");
                final String finSchoolName = rs.getString("FINSCHOOL_NAME");
                final String examHallCd = rs.getString("EXAMHALLCD");
                final String examHallName = rs.getString("EXAMHALL_NAME");

                final PrintData printData = new PrintData(examNo, name, nameKana, finSchoolName, examHallCd, examHallName);
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
        stb.append("     BASE.EXAMNO, ");
        stb.append("     BASE.NAME, ");
        stb.append("     BASE.NAME_KANA, ");
        stb.append("     FIN_M.FINSCHOOL_NAME, ");
        stb.append("     HALL.EXAMHALLCD, ");
        stb.append("     HALL.EXAMHALL_NAME ");
        stb.append(" FROM ");
        stb.append("     V_ENTEXAM_APPLICANTBASE_DAT BASE ");
        stb.append("     LEFT JOIN ENTEXAM_HALL_YDAT HALL ");
        stb.append("                                 ON BASE.ENTEXAMYEAR  = HALL.ENTEXAMYEAR ");
        stb.append("                                AND BASE.APPLICANTDIV = HALL.APPLICANTDIV ");
        stb.append("                                AND BASE.TESTDIV      = HALL.TESTDIV ");
        stb.append("                                AND HALL.EXAM_TYPE    = '1' ");
        stb.append("                                AND HALL.EXAMHALLCD  IN " + _param._hallSelectedIn + " ");
        stb.append("     LEFT JOIN FINSCHOOL_YDAT FIN_Y ");
        stb.append("                                 ON BASE.ENTEXAMYEAR = FIN_Y.YEAR ");
        stb.append("                                AND BASE.FS_CD       = FIN_Y.FINSCHOOLCD ");
        stb.append("     LEFT JOIN FINSCHOOL_MST FIN_M ");
        stb.append("                                 ON FIN_Y.FINSCHOOLCD = FIN_M.FINSCHOOLCD ");
        stb.append(" WHERE ");
        stb.append("         BASE.ENTEXAMYEAR  = '" + _param._entExamYear + "' ");
        stb.append("     AND BASE.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND BASE.TESTDIV      = '" + _param._testDiv + "' ");
        stb.append("     AND VALUE(BASE.JUDGEMENT, '0') <> '5' ");
        stb.append("     AND BASE.EXAMNO BETWEEN HALL.S_RECEPTNO AND HALL.E_RECEPTNO ");
        stb.append(" ORDER BY ");
        stb.append("     HALL.EXAMHALLCD, ");
        if ("1".equals(_param._sortDiv)) {
            stb.append("     BASE.EXAMNO, ");
            stb.append("     BASE.NAME_KANA ");
        } else {
            stb.append("     BASE.NAME_KANA, ");
            stb.append("     BASE.EXAMNO ");
        }

        return stb.toString();
    }

    private class PrintData {
        final String _examNo;
        final String _name;
        final String _nameKana;
        final String _finSchoolName;
        final String _examHallCd;
        final String _examHallName;
        public PrintData(
                final String examNo,
                final String name,
                final String nameKana,
                final String finSchoolName,
                final String examHallCd,
                final String examHallName
        ) {
            _examNo = examNo;
            _name = name;
            _nameKana = nameKana;
            _finSchoolName = finSchoolName;
            _examHallCd = examHallCd;
            _examHallName = examHallName;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 56949 $");
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
        private final String _hallSelectedIn;

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

            final String[] hallSelected = request.getParameterValues("HALL_SELECTED");
            _hallSelectedIn = getHallSelectedIn(hallSelected);
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

        private String getHallSelectedIn(final String[] hallSelected) {
            StringBuffer stb = new StringBuffer();
            stb.append("(");
            for (int i = 0; i < hallSelected.length; i++) {
                if (0 < i) stb.append(",");
                stb.append("'" + hallSelected[i] + "'");
            }
            stb.append(")");
            return stb.toString();
        }

    }
}

// eof
