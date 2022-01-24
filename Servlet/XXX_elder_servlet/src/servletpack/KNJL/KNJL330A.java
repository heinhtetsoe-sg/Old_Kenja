/*
 * $Id: 30fd5f40fba83d379b1b5ec2c68cdb60b33c06f2 $
 *
 * 作成日: 2018/07/25
 * 作成者: yogi
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;

public class KNJL330A {

    private static final Log log = LogFactory.getLog(KNJL330A.class);

    private boolean _hasData;

    private Param _param;

    private String bithdayField;

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
        svf.VrSetForm("KNJL330A.frm", 1);
        final List printList = getList(db2);
        final int maxCnt = 50;
        int printCnt = 1;

        setTitle(db2, svf);//ヘッダ
        printCnt = 1;
        for (Iterator iterator = printList.iterator(); iterator.hasNext();) {
            final PrintData printData = (PrintData) iterator.next();
            if (printCnt > maxCnt) {
                svf.VrEndPage();
                svf.VrSetForm("KNJL330A.frm", 1);
                setTitle(db2, svf);//ヘッダ
                printCnt = 1;
            }

            //受験番号
            svf.VrsOutn("EXAM_NAME", printCnt, printData._tDivName);
            svf.VrsOutn("EXAM_NO", printCnt, printData._receptNo);
            int namelen = KNJ_EditEdit.getMS932ByteLength(printData._name);
            String namefield = namelen > 30 ? "NAME3" : namelen > 20 ? "NAME2" : "NAME1";
            svf.VrsOutn(namefield, printCnt, printData._name);
            svf.VrsOutn("SEX", printCnt, printData._sex);
            svf.VrsOutn("COURSE_NAME", printCnt, printData._courseName);
            svf.VrsOutn("FINSCHOOL_NAME", printCnt, printData._finSchoolName);
            svf.VrsOutn("PAY_FLG", printCnt, ("1".equals(printData._procedureDiv) ? "レ" : ""));

            printCnt++;
            _hasData = true;
        }
        if (_hasData) {
            svf.VrEndPage();
        }
    }

    private void setTitle(final DB2UDB db2, final Vrw32alp svf) {
        String setYear = KNJ_EditDate.h_format_JP_N(db2, _param._entExamYear + "/04/01");
        svf.VrsOut("TITLE", setYear+"度　"+ _param._schoolKindNameList.get(_param._applicantDiv) + "　入学予定者リスト");
        svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(db2, _param._loginDate));
    }

    private List getList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getSql();
            log.fatal(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String tDivName = rs.getString("TDIV_NAME");
                final String receptNo = rs.getString("RECEPTNO");
                final String examNo = rs.getString("RECEPTNO");
                final String name = rs.getString("NAME");
                final String sex = rs.getString("SEX");
                final String courseName = rs.getString("COURSENAME");
                final String finSchoolName = rs.getString("FINSCHOOL_NAME_ABBV");
                final String procedureDiv = rs.getString("PROCEDUREDIV");

                final PrintData printData = new PrintData(tDivName, receptNo, examNo, name, sex, courseName, finSchoolName, procedureDiv);
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
        stb.append("     TDIV.TESTDIV_NAME AS TDIV_NAME, ");
        stb.append("     RCPT.RECEPTNO, ");
        stb.append("     RCPT.EXAMNO, ");
        stb.append("     BASE.NAME, ");
        stb.append("     Z002.ABBV1 AS SEX, ");
        stb.append("     L012.NAME1 AS COURSENAME, ");
        stb.append("     FINS.FINSCHOOL_NAME_ABBV, ");
        stb.append("     BASE.PROCEDUREDIV ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_RECEPT_DAT RCPT ");
        stb.append("     INNER JOIN V_ENTEXAM_APPLICANTBASE_DAT BASE ");
        stb.append("          ON BASE.ENTEXAMYEAR    = RCPT.ENTEXAMYEAR ");
        stb.append("         AND BASE.APPLICANTDIV   = RCPT.APPLICANTDIV ");
        stb.append("         AND BASE.EXAMNO         = RCPT.EXAMNO ");
        stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT AD030 ");
        stb.append("          ON AD030.ENTEXAMYEAR   = RCPT.ENTEXAMYEAR ");
        stb.append("         AND AD030.APPLICANTDIV  = RCPT.APPLICANTDIV ");
        stb.append("         AND AD030.EXAMNO        = RCPT.EXAMNO ");
        stb.append("         AND AD030.SEQ           = '030' ");
        stb.append("         AND AD030.REMARK1       = RCPT.TESTDIV ");
        stb.append("         AND AD030.REMARK2       = RCPT.RECEPTNO ");
        stb.append("     LEFT JOIN ENTEXAM_TESTDIV_MST TDIV ");
        stb.append("       ON TDIV.ENTEXAMYEAR = RCPT.ENTEXAMYEAR ");
        stb.append("       AND TDIV.APPLICANTDIV = RCPT.APPLICANTDIV ");
        stb.append("       AND TDIV.TESTDIV = RCPT.TESTDIV ");
        stb.append("     LEFT JOIN V_NAME_MST Z002 ");
        stb.append("          ON Z002.YEAR       = BASE.ENTEXAMYEAR ");
        stb.append("         AND Z002.NAMECD1    = 'Z002' ");
        stb.append("         AND Z002.NAMECD2    = BASE.SEX ");
        stb.append("     LEFT JOIN FINSCHOOL_MST FINS ON BASE.FS_CD = FINS.FINSCHOOLCD ");
        stb.append("     LEFT JOIN V_NAME_MST L003 ");
        stb.append("          ON L003.YEAR       = BASE.ENTEXAMYEAR ");
        stb.append("         AND L003.NAMECD1    = 'L003' ");
        stb.append("         AND L003.NAMECD2    = BASE.APPLICANTDIV ");
        stb.append("     LEFT JOIN V_NAME_MST L012 ");
        stb.append("          ON L012.YEAR       = BASE.ENTEXAMYEAR ");
        stb.append("         AND L012.NAMECD1    = 'L' || VALUE(L003.NAMESPARE3,'H') || '12' ");
        stb.append("         AND L012.NAMECD2    = BASE.ENTDIV ");
        stb.append(" WHERE ");
        stb.append("         RCPT.ENTEXAMYEAR    = '" + _param._entExamYear + "' ");
        stb.append("     AND RCPT.APPLICANTDIV   = '" + _param._applicantDiv + "' ");
        if (!_param._testDiv.equals("ALL")) {
            stb.append("     AND RCPT.TESTDIV        = '" + _param._testDiv + "' ");
        }
        stb.append("     AND RCPT.EXAM_TYPE      = '1' "); //EXAMTYPEは1固定
        stb.append("     AND BASE.ENTDIV IS NOT NULL ");
        stb.append("     AND BASE.PROCEDUREDIV = '1' ");
        stb.append(" ORDER BY ");
        stb.append("     RCPT.TESTDIV, RCPT.RECEPTNO ");

        return stb.toString();
    }

    private class PrintData {
        final String _tDivName;      //TDIV_NAME
        final String _receptNo;      //RECEPTNO
        final String _examNo;        //EXAMNO
        final String _name;          //NAME
        final String _sex;           //SEX
        final String _courseName;    //COURSENAME
        final String _finSchoolName; //FINSCHOOL_NAME_ABBV
        final String _procedureDiv;  //PROCEDUREDIV

        public PrintData(
                final String tDivName,
                final String receptNo,
                final String examNo,
                final String name,
                final String sex,
                final String courseName,
                final String finSchoolName,
                final String procedureDiv
        ) {
            _tDivName = tDivName;
            _receptNo = receptNo;
            _examNo = examNo;
            _name = name;
            _sex = sex;
            _courseName = courseName;
            _finSchoolName = finSchoolName;
            _procedureDiv = procedureDiv;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 61733 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _loginYear;
        private final String _loginSemester;
        private final String _loginDate;
        private final String _entExamYear;
        private final String _applicantDiv;
        private final String _appDivName;
        private final String _testDiv;

        private Map _schoolKindNameList  = Collections.EMPTY_MAP;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _loginYear      = request.getParameter("LOGIN_YEAR");
            _loginSemester  = request.getParameter("LOGIN_SEMESTER");
            _loginDate      = request.getParameter("LOGIN_DATE");
            _entExamYear    = request.getParameter("ENTEXAMYEAR");
            _applicantDiv   = request.getParameter("APPLICANTDIV");
            _appDivName     = StringUtils.defaultString(getNameMst(db2, "NAME1", "L003", _applicantDiv));
            _testDiv        = request.getParameter("TESTDIV");

            _schoolKindNameList = setSchoolKindNameMap(db2);
        }
        private Map setSchoolKindNameMap(final DB2UDB db2) {
            return KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, "SELECT NAMECD2, NAME1 FROM NAME_MST WHERE NAMECD1 = 'L003' "), "NAMECD2", "NAME1");
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

        private String getTestdivMst(final DB2UDB db2, final String field, final String testdiv) {
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT " + field + " FROM ENTEXAM_TESTDIV_MST WHERE ENTEXAMYEAR = '" + _entExamYear + "' AND APPLICANTDIV = '" + _applicantDiv + "' AND TESTDIV = '" + testdiv + "' ");
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
