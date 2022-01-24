/*
 * $Id: 42607498cffd5085866d582241221ae0c90cfaa0 $
 *
 * 作成日: 2017/11/01
 * 作成者: tawada
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

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJL540A {

    private static final Log log = LogFactory.getLog(KNJL540A.class);
    private static final String CONST_SELALL = "99999";

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
        svf.VrSetForm("KNJL540A.frm", 1);
        final List printList = getList(db2);

        int colCnt = 1;
        final int maxCnt = 50;
        setTitle(svf, db2);
        for (Iterator iterator = printList.iterator(); iterator.hasNext();) {
        	if (colCnt > maxCnt) {
        	    svf.VrEndPage();
        	    colCnt = 1;
                setTitle(svf, db2);
        	}
            final PrintData printData = (PrintData) iterator.next();
            svf.VrsOutn("EXAM_NO", colCnt, printData._receptNo);
            final int nlen = KNJ_EditEdit.getMS932ByteLength(printData._name);
            final String nfield = nlen > 30 ? "2" : "1";
            svf.VrsOutn("NAME" + nfield, colCnt, printData._name);
            final int nklen = KNJ_EditEdit.getMS932ByteLength(printData._nameKana);
            final String nkfield = nklen > 30 ? "2" : "1";
            svf.VrsOutn("KANA" + nkfield, colCnt, printData._nameKana);
            svf.VrsOutn("FINSCHOOL_NAME", colCnt, printData._finschoolName);

            colCnt++;
            _hasData = true;
        }
        if (_hasData) {
            svf.VrEndPage();
        }
    }

    private void setTitle(final Vrw32alp svf, final DB2UDB db2) {
        final String setYear = KNJ_EditDate.h_format_JP_N(db2, _param._ObjYear + "/04/01");
    	svf.VrsOut("TITLE", setYear+"度 " + _param.getNameMst(db2, "L004", _param._testDiv) + "欠席者一覧");
    	svf.VrsOut("SUBTITLE", _param.getHopeCourseCodeStr(db2));
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
                final String examNo = rs.getString("EXAMNO");
                final String receptNo = rs.getString("RECEPTNO");
                final String name = rs.getString("NAME");
                final String nameKana = rs.getString("NAME_KANA");
                final String judgeDiv = rs.getString("JUDGEDIV");
                final String finschoolName = rs.getString("FINSCHOOL_NAME");

                final PrintData printData = new PrintData(examNo, receptNo, name, nameKana, judgeDiv, finschoolName);
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
        stb.append("     RCPT.EXAMNO, ");
        stb.append("     RCPT.RECEPTNO, ");
        stb.append("     BASE.NAME, ");
        stb.append("     BASE.NAME_KANA, ");
        stb.append("     RCPT.JUDGEDIV, ");
        stb.append("     FINS.FINSCHOOL_NAME_ABBV AS FINSCHOOL_NAME ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_RECEPT_DAT RCPT ");
        stb.append("     LEFT JOIN V_ENTEXAM_APPLICANTBASE_DAT BASE ON BASE.ENTEXAMYEAR = RCPT.ENTEXAMYEAR ");
        stb.append("                                         AND BASE.APPLICANTDIV = RCPT.APPLICANTDIV ");
        stb.append("                                         AND BASE.TESTDIV      = RCPT.TESTDIV ");
        stb.append("                                         AND BASE.EXAMNO       = RCPT.EXAMNO ");
        stb.append("     LEFT JOIN ENTEXAM_SCHOOL_MST ENTS ON BASE.ENTEXAMYEAR = ENTS.ENTEXAMYEAR ");
        stb.append("                                         AND BASE.FS_CD = ENTS.ENTEXAM_SCHOOLCD ");
        stb.append("     LEFT JOIN FINSCHOOL_MST FINS ON ENTS.FINSCHOOLCD = FINS.FINSCHOOLCD ");
        if (!CONST_SELALL.equals(_param._hopeCourseCode)) {
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BASEDTL01 ");
            stb.append("       ON BASEDTL01.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
            stb.append("      AND BASEDTL01.APPLICANTDIV = BASE.APPLICANTDIV ");
            stb.append("      AND BASEDTL01.EXAMNO = BASE.EXAMNO ");
            stb.append("      AND BASEDTL01.SEQ = '001' ");
        }
        stb.append(" WHERE ");
        stb.append("         RCPT.ENTEXAMYEAR  = '" + _param._ObjYear + "'  ");
        stb.append("     AND RCPT.APPLICANTDIV = '" + _param._applicantDiv + "'  ");
        stb.append("     AND RCPT.TESTDIV      = '" + _param._testDiv + "' ");
        stb.append("     AND RCPT.EXAM_TYPE    = '" + _param._examType + "' ");
        stb.append("     AND JUDGEDIV = '4' "); //欠席
        if (!CONST_SELALL.equals(_param._hopeCourseCode)) {
            stb.append("         AND BASEDTL01.REMARK10 = '" + _param._hopeCourseCode + "'  ");
        }
        stb.append(" ORDER BY ");
        stb.append("     RCPT.RECEPTNO ");

        return stb.toString();
    }


    private class PrintData {
        final String _examNo;
        final String _receptNo;
        final String _name;
        final String _nameKana;
        final String _judgeDiv;
        final String _finschoolName;
        public PrintData(
                final String examNo,
                final String receptNo,
                final String name,
                final String nameKana,
                final String judgeDiv,
                final String finschoolName
        ) {
        	_examNo = examNo;
        	_receptNo = receptNo;
        	_name = name;
        	_nameKana = nameKana;
        	_judgeDiv = judgeDiv;
        	_finschoolName = finschoolName;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 70250 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _loginYear;
        private final String _loginSemester;
        private final String _loginDate;

        private final String _ObjYear;
        private final String _applicantDiv;
        private final String _testDiv;
        private final String _examType;
        private final String _hopeCourseCode;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _loginYear       = request.getParameter("LOGIN_YEAR");
            _loginSemester   = request.getParameter("LOGIN_SEMESTER");
            _loginDate       = request.getParameter("LOGIN_DATE");
            _ObjYear         = request.getParameter("ENTEXAMYEAR");
            _applicantDiv    = "1";
            _testDiv         = request.getParameter("TESTDIV");
            _examType        = "1";
            _hopeCourseCode  = request.getParameter("HOPE_COURSECODE");
        }
        private String getNameMst(final DB2UDB db2, final String nameCd1, final String nameCd2) {
        	String retStr = "";
        	StringBuffer stb = new StringBuffer();
        	stb.append(" SELECT ");
        	stb.append("   NAME1 ");
        	stb.append(" FROM ");
        	stb.append("   NAME_MST ");
        	stb.append(" WHERE ");
        	stb.append("   NAMECD1 = '" + nameCd1 + "' ");
            stb.append("   AND NAMECD2 = '" + nameCd2 + "' ");
        	stb.append(" ORDER BY ");
        	stb.append("   NAMECD1 ");
        	final String sql =  stb.toString();

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                	retStr = rs.getString("NAME1");
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retStr;
        }
        private String getHopeCourseCodeStr(final DB2UDB db2) {
        	String retStr = "";
        	StringBuffer stb = new StringBuffer();
        	stb.append(" SELECT ");
        	stb.append("   T2.HOPE_NAME ");  // 志望区分
        	stb.append(" FROM ");
        	stb.append("   ENTEXAM_HOPE_COURSE_YDAT T1 ");
        	stb.append("   LEFT JOIN ENTEXAM_HOPE_COURSE_MST T2 ");
        	stb.append("     ON T2.HOPE_COURSECODE = T1.HOPE_COURSECODE ");
        	stb.append(" WHERE ");
        	stb.append("   T1.ENTEXAMYEAR = '" + _ObjYear + "' ");
        	stb.append("   AND T1.HOPE_COURSECODE = '" + _hopeCourseCode + "' ");
        	stb.append(" ORDER BY ");
        	stb.append("  T1.HOPE_COURSECODE ");
        	final String sql =  stb.toString();

        	if (!CONST_SELALL.equals(_hopeCourseCode)) {
	            PreparedStatement ps = null;
	            ResultSet rs = null;
	            try {
	                ps = db2.prepareStatement(sql);
	                rs = ps.executeQuery();
	                while (rs.next()) {
	                	retStr = rs.getString("HOPE_NAME");
	                }
	            } catch (Exception e) {
	                log.error("exception!", e);
	            } finally {
	                DbUtils.closeQuietly(null, ps, rs);
	                db2.commit();
	            }
            }
            return retStr;
        }
    }
}

// eof
