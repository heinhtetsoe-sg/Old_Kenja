/*
 * $Id: 742077dd006233ecd273c9d674ccc54478a63be6 $
 *
 * 作成日: 2016/11/04
 * 作成者: yamashiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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

public class KNJL335F {

    private static final Log log = LogFactory.getLog(KNJL335F.class);

    private static final String SCHOOL_J = "1";
    private static final String SCHOOL_H = "2";
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
        final List printList = getList(db2);
        if (SCHOOL_J.equals(_param._applicantdiv)) {
            printOutJ(db2, svf, printList);
        } else {
            printOutH(db2, svf, printList);
        }
    }

    private void printOutJ(final DB2UDB db2, final Vrw32alp svf, final List printList) {
        svf.VrSetForm("KNJL335F_J1.frm", 1);
        setTitle(db2, svf);
        int renBan = 1;
        int lineCnt = 1;
        for (int line = 0; line < printList.size(); line++) {
            final PrintData printData = (PrintData) printList.get(line);
            if (lineCnt > 50) {
                svf.VrEndPage();
                setTitle(db2, svf);
                lineCnt = 1;
            }
            svf.VrsOutn("NO", lineCnt, String.valueOf(renBan));
            svf.VrsOutn("EXAM_NO", lineCnt, printData._receptno);
            svf.VrsOutn("NAME", lineCnt, printData._name);
            svf.VrsOutn("KANA", lineCnt, printData._nameKana);
            svf.VrsOutn("BIRTHDAY", lineCnt, KNJ_EditDate.h_format_JP(db2, printData._birthday));
            svf.VrsOutn("SCHOOL_NAME", lineCnt, printData._finschoolName);
            svf.VrsOutn("PASS_COURSE_NAME", lineCnt, printData._examcourseName);
            svf.VrsOutn("REMARK", lineCnt, printData._remark);
            renBan++;
            lineCnt++;
            _hasData = true;
        }
        if (lineCnt > 1) {
            svf.VrEndPage();
        }
    }

    private void printOutH(final DB2UDB db2, final Vrw32alp svf, final List printList) {
        svf.VrSetForm("KNJL335F_H1.frm", 4);
        setTitle(db2, svf);
        int renBan = 1;
        for (int line = 0; line < printList.size(); line++) {
            final PrintData printData = (PrintData) printList.get(line);
            svf.VrsOut("NO", String.valueOf(renBan));
            svf.VrsOut("EXAM_NO", printData._receptno);
            svf.VrsOut("NAME", printData._name);
            svf.VrsOut("CITY_NAME", printData._fincity);
            svf.VrsOut("SCHOOL_DUV", printData._finseturitu);
            svf.VrsOut("SCHOOL_NAME", printData._finschoolName);
            svf.VrsOut("PASS_COURSE_NAME", printData._examcourseName);
            svf.VrsOut("REMARK", printData._remark);
            svf.VrEndRecord();
            renBan++;
            _hasData = true;
        }
    }

    private void setTitle(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrsOut("TITLE", KNJ_EditDate.gengou(db2, Integer.parseInt(_param._entexamyear)) + "年度　" + _param._applicantdivName + "入試　入学準備説明会一覧表");
        svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(db2, _param._loginDate));
    }

    private List getList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = sql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String examno = rs.getString("EXAMNO");
                final String receptno = rs.getString("RECEPTNO");
                final String name = rs.getString("NAME");
                final String nameKana = rs.getString("NAME_KANA");
                final String birthday = rs.getString("BIRTHDAY");
                final String fsCd = rs.getString("FS_CD");
                final String finschoolName = rs.getString("FINSCHOOL_NAME");
                final String fincity = rs.getString("FINCITY");
                final String finseturitu = rs.getString("FINSETURITU");
                final String examcourseName = rs.getString("EXAMCOURSE_NAME");
                final String remark = rs.getString("REMARK");
                final PrintData printData = new PrintData(examno, receptno, name, nameKana, birthday, fsCd, finschoolName, fincity, finseturitu, examcourseName, remark);
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

    private String sql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH BASE_T AS ( ");
        stb.append(" SELECT ");
        stb.append("     * ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_APPLICANTBASE_DAT ");
        stb.append(" WHERE ");
        stb.append("     ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND APPLICANTDIV = '" + _param._applicantdiv + "' ");
        stb.append("     AND JUDGEMENT = '1' ");
        stb.append("     AND PROCEDUREDIV = '1' ");
        stb.append("     AND ENTDIV = '1' ");
        stb.append(" ), RECEPT_MIN AS ( ");
        stb.append(" SELECT ");
        stb.append("     BASE_T.EXAMNO, ");
        stb.append("     MIN(RECEPT.RECEPTNO) AS RECEPTNO ");
        stb.append(" FROM ");
        stb.append("     BASE_T, ");
        stb.append("     V_ENTEXAM_APPLICANTBASE_TESTDIV_DAT V_BASE, ");
        stb.append("     V_ENTEXAM_RECEPT_DAT RECEPT ");
        stb.append("     LEFT JOIN NAME_MST L013 ON L013.NAMECD1 = 'L013' ");
        stb.append("          AND RECEPT.JUDGEDIV = L013.NAMECD2 ");
        stb.append(" WHERE ");
        stb.append("     BASE_T.ENTEXAMYEAR = V_BASE.ENTEXAMYEAR ");
        stb.append("     AND BASE_T.APPLICANTDIV = V_BASE.APPLICANTDIV ");
        stb.append("     AND BASE_T.EXAMNO = V_BASE.EXAMNO ");
        stb.append("     AND BASE_T.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR ");
        stb.append("     AND BASE_T.APPLICANTDIV = RECEPT.APPLICANTDIV ");
        stb.append("     AND V_BASE.TESTDIV = RECEPT.TESTDIV ");
        stb.append("     AND RECEPT.EXAM_TYPE = '1' ");
        stb.append("     AND RECEPT.EXAM_TYPE = '1' ");
        stb.append("     AND BASE_T.EXAMNO = RECEPT.EXAMNO ");
        stb.append("     AND L013.NAMESPARE1 = '1' ");
        stb.append(" GROUP BY ");
        stb.append("     BASE_T.EXAMNO ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     BASE_T.EXAMNO, ");
        stb.append("     RECEPT_MIN.RECEPTNO, ");
        stb.append("     BASE_T.NAME, ");
        stb.append("     BASE_T.NAME_KANA, ");
        stb.append("     BASE_T.BIRTHDAY, ");
        stb.append("     BASE_T.FS_CD, ");
        stb.append("     FSCHOOL.FINSCHOOL_NAME, ");
        stb.append("     L001.NAME1 AS FINCITY, ");
        stb.append("     L015.NAME1 AS FINSETURITU, ");
        stb.append("     BASE_T.SUC_COURSECD, ");
        stb.append("     BASE_T.SUC_MAJORCD, ");
        stb.append("     BASE_T.SUC_COURSECODE, ");
        stb.append("     COURSE.EXAMCOURSE_NAME, ");
        if ("1".equals(_param._applicantdiv)) {
            stb.append("     L025.NAME1 AS REMARK ");
        } else {
            stb.append("     L025.NAME2 AS REMARK ");
        }
        stb.append(" FROM ");
        stb.append("     BASE_T ");
        stb.append("     LEFT JOIN RECEPT_MIN ON BASE_T.EXAMNO = RECEPT_MIN.EXAMNO ");
        stb.append("     LEFT JOIN FINSCHOOL_MST FSCHOOL ON BASE_T.FS_CD = FSCHOOL.FINSCHOOLCD ");
        stb.append("     LEFT JOIN NAME_MST L001 ON L001.NAMECD1 = 'L001' ");
        stb.append("          AND FSCHOOL.FINSCHOOL_DISTCD = L001.NAMECD2 ");
        stb.append("     LEFT JOIN NAME_MST L015 ON L015.NAMECD1 = 'L015' ");
        stb.append("          AND FSCHOOL.FINSCHOOL_DIV = L015.NAMECD2 ");
        stb.append("     LEFT JOIN ENTEXAM_COURSE_MST COURSE ON BASE_T.ENTEXAMYEAR = COURSE.ENTEXAMYEAR ");
        stb.append("          AND BASE_T.APPLICANTDIV = COURSE.APPLICANTDIV ");
        stb.append("          AND COURSE.TESTDIV = '1' ");
        stb.append("          AND BASE_T.SUC_COURSECD = COURSE.COURSECD ");
        stb.append("          AND BASE_T.SUC_MAJORCD = COURSE.MAJORCD ");
        stb.append("          AND BASE_T.SUC_COURSECODE = COURSE.EXAMCOURSECD ");
        stb.append("     LEFT JOIN NAME_MST L025 ON L025.NAMECD1 = 'L025' ");
        stb.append("          AND BASE_T.JUDGE_KIND = L025.NAMECD2 ");
        stb.append(" ORDER BY ");
        stb.append("     RECEPT_MIN.RECEPTNO ");
        return stb.toString();
    }

    private class PrintData {
        final String _examno;
        final String _receptno;
        final String _name;
        final String _nameKana;
        final String _birthday;
        final String _fsCd;
        final String _finschoolName;
        final String _fincity;
        final String _finseturitu;
        final String _examcourseName;
        final String _remark;

        public PrintData(
                final String examno,
                final String receptno,
                final String name,
                final String nameKana,
                final String birthday,
                final String fsCd,
                final String finschoolName,
                final String fincity,
                final String finseturitu,
                final String examcourseName,
                final String remark
        ) {
            _examno = examno;
            _receptno = receptno;
            _name = name;
            _nameKana = nameKana;
            _birthday = birthday;
            _fsCd = fsCd;
            _finschoolName = finschoolName;
            _fincity = fincity;
            _finseturitu = finseturitu;
            _examcourseName = examcourseName;
            _remark = remark;

        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 71520 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _applicantdiv;
        private final String _entexamyear;
        private final String _loginYear;
        private final String _loginSemester;
        private final String _loginDate;
        private final String _printLogStaffcd;

        private final String _applicantdivName;
        private final String _dateStr;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _applicantdiv       = request.getParameter("APPLICANTDIV");
            _entexamyear        = request.getParameter("ENTEXAMYEAR");
            _loginYear          = request.getParameter("LOGIN_YEAR");
            _loginSemester      = request.getParameter("LOGIN_SEMESTER");
            _loginDate          = request.getParameter("LOGIN_DATE");
            _printLogStaffcd    = request.getParameter("PRINT_LOG_STAFFCD");

            _applicantdivName = getNameMst(db2, "NAME1", "L003", _applicantdiv);
            _dateStr = getDateStr(db2, _loginDate);
        }

        private String getDateStr(final DB2UDB db2, final String date) {
            if (null == date) {
                return null;
            }
            return KNJ_EditDate.h_format_JP(db2, date);
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

