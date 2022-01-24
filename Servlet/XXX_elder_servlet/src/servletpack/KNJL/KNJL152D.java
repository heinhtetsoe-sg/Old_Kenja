/*
 * $Id: 95d9e31b2e1eda05e44c6921a3bacffc6e89dfe4 $
 *
 * 作成日: 2018/05/09
 * 作成者: yogi
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
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJL152D {

    private static final Log log = LogFactory.getLog(KNJL152D.class);

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
        svf.VrSetForm("KNJL152D.frm", 1);

        int linemax = 50;
        //
        setTitle(db2, svf);

        List gouakulist = getList(db2);
        int lineno = 1;
        for (Iterator iterator = gouakulist.iterator(); iterator.hasNext();) {
            if (lineno > linemax) {
                svf.VrEndPage();
                setTitle(db2, svf);
                lineno = 1;
            }
            PrintData1 goukakuInfo = (PrintData1)iterator.next();
            printMain1(db2, svf, goukakuInfo, lineno);
            lineno++;
            _hasData = true;
        }
        if (_hasData) {
            svf.VrEndPage();
        }
    }

    private void setTitle(final DB2UDB db2, final Vrw32alp svf) {
        final String prtyear = KNJ_EditDate.h_format_JP_N(db2, _param._loginDate);
        String ttlstr = getSubjectName(db2);
        String examnumstr = getExamNumStr(db2);

        svf.VrsOut("TITLE", prtyear+"度 " + examnumstr + " " + ttlstr + "評価");
        svf.VrsOut("SUBTITLE", "(" + ttlstr + ")");
        final String cutprtdate[] = StringUtils.split(_param._loginDate, "-");
        svf.VrsOut("DATE", prtyear + cutprtdate[1] + "月" + cutprtdate[2] + "日");
        if ("1".equals(_param._eValType)) {
            svf.VrsOut("ITEM1", ttlstr + "評価");
            svf.VrsOut("ITEM2", ttlstr + "所見");
        } else {
            svf.VrsOut("ITEM1", ttlstr + "評価");
            svf.VrsOut("ITEM2", ttlstr + "テーマ");
        }
    }

    private void printMain1(final DB2UDB db2, final Vrw32alp svf, PrintData1 goukakuInfo, int lineno) {

        svf.VrsOutn("EXAM_NO", lineno, goukakuInfo._examNo);
        String nameIdx = "";
        int namelen = KNJ_EditEdit.getMS932ByteLength(goukakuInfo._name);
        if (namelen <= 20) {
            nameIdx = "1";
        } else if (namelen <= 30) {
            nameIdx = "2";
        } else {
            nameIdx = "3";
        }
        svf.VrsOutn("NAME" + nameIdx, lineno, goukakuInfo._name);
        svf.VrsOutn("VALUE", lineno, goukakuInfo._interview_val);
        svf.VrsOutn("VIEW", lineno, goukakuInfo._interview_rmk);
    }

    private String getSubjectName(final DB2UDB db2) {
        String retstr = "";
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final String sql = getSubjectNamesql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                retstr = rs.getString("NAME1");
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retstr;
    }

    private String getExamNumStr(final DB2UDB db2) {
        String retstr = "";
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final String sql = getExamNumStrsql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                retstr = rs.getString("NAME1");
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retstr;
    }
    private List getList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final String sql = getSql1();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String examNo = rs.getString("EXAMNO");
                final String name = rs.getString("NAME");
                final String interview_val = rs.getString("INTERVIEW_VAL");
                final String interview_rmk = rs.getString("INTERVIEW_RMK");

                final PrintData1 printData = new PrintData1(examNo, name, interview_val, interview_rmk);
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

    private String getSubjectNamesql() {
        final StringBuffer stb = new StringBuffer();

        stb.append(" SELECT ");
        stb.append("     NAME1 ");
        stb.append(" FROM ");
        stb.append("     V_NAME_MST ");
        stb.append(" WHERE ");
        stb.append("     YEAR    = '" + _param._entExamYear + "' ");
        stb.append("     AND NAMECD1 = 'L009' ");
        stb.append("     AND NAMECD2 = '" + _param._eValType + "' ");

        return stb.toString();
    }

    private String getExamNumStrsql() {
        final StringBuffer stb = new StringBuffer();

        stb.append(" SELECT ");
        stb.append("     NAME1 ");
        stb.append(" FROM ");
        stb.append("     V_NAME_MST ");
        stb.append(" WHERE ");
        stb.append("     YEAR    = '" + _param._entExamYear + "' ");
        stb.append("     AND NAMECD1 = 'L004' ");
        stb.append("     AND NAMECD2 = '" + _param._testDiv + "' ");

        return stb.toString();
    }

    private String getSql1() {
        final StringBuffer stb = new StringBuffer();

        stb.append(" SELECT ");
        stb.append("     B1.EXAMNO, ");
        stb.append("     B1.NAME, ");
        if ("1".equals(_param._eValType)) {
            stb.append("     I1.INTERVIEW_VALUE AS INTERVIEW_VAL, ");
            stb.append("     I1.INTERVIEW_REMARK AS INTERVIEW_RMK ");
        } else {
            stb.append("     I1.INTERVIEW_VALUE2 AS INTERVIEW_VAL, ");
            stb.append("     I1.INTERVIEW_REMARK2 AS INTERVIEW_RMK ");
        }
        stb.append(" FROM ");
        stb.append("     ENTEXAM_APPLICANTBASE_DAT B1 ");
        stb.append("     LEFT JOIN ENTEXAM_INTERVIEW_DAT I1 ");
        stb.append("       ON I1.ENTEXAMYEAR      = B1.ENTEXAMYEAR ");
        stb.append("       AND I1.APPLICANTDIV     = B1.APPLICANTDIV ");
        stb.append("       AND I1.TESTDIV          = B1.TESTDIV ");
        stb.append("       AND I1.EXAMNO           = B1.EXAMNO ");
        stb.append(" WHERE ");
        stb.append("     B1.ENTEXAMYEAR  = '" + _param._entExamYear + "' ");
        stb.append("     AND B1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND B1.TESTDIV      = '" + _param._testDiv + "' ");
        stb.append(" ORDER BY ");
        stb.append("     EXAMNO ");

        return stb.toString();
    }

    private class PrintData1 {
        final String _examNo;
        final String _name;
        final String _interview_val;
        final String _interview_rmk;
        public PrintData1(
                final String examNo,
                final String name,
                final String interview_val,
                final String interview_rmk
        ) {
            _examNo = examNo;
            _name = name;
            _interview_val = interview_val;
            _interview_rmk = interview_rmk;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 71866 $");
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
        private final String _testDiv;
        private final String _eValType;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _loginYear     = request.getParameter("LOGIN_YEAR");
            _loginSemester = request.getParameter("LOGIN_SEMESTER");
            _loginDate     = request.getParameter("LOGIN_DATE");
            _entExamYear   = request.getParameter("ENTEXAMYEAR");
            _applicantDiv  = request.getParameter("APPLICANTDIV");
            _testDiv       = request.getParameter("TESTDIV");
            _eValType      = request.getParameter("EVALTYPE");
        }

    }
}

// eof
