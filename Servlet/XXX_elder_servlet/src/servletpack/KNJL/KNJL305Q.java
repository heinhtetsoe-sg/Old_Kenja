/*
 * $Id: 37e3d58afac2880c8b5b36abee221100f09d8e7e $
 *
 * 作成日: 2017/12/21
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

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJL305Q {

    private static final Log log = LogFactory.getLog(KNJL305Q.class);

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
        final String formName = ("1".equals(_param._testDiv)) ? "KNJL305Q_1.frm": "KNJL305Q_2.frm";
        svf.VrSetForm(formName, 1);

        final List printList = getList(db2);
        for (Iterator iterator = printList.iterator(); iterator.hasNext();) {
            final PrintData printData = (PrintData) iterator.next();
            final String nameField = KNJ_EditEdit.getMS932ByteLength(printData._name) > 36 ? "2": "1";
            svf.VrsOut("NAME" + nameField, printData._name);//氏名
            svf.VrsOut("EXAM_NO", printData._examNo);//受験番号
            final String gNameField = KNJ_EditEdit.getMS932ByteLength(printData._gName) > 36 ? "2": "1";
            svf.VrsOut("GUARD_NAME" + gNameField, printData._gName);//保護者氏名
            final String fsNameField = KNJ_EditEdit.getMS932ByteLength(printData._fsName) > 36 ? "2": "1";
            svf.VrsOut("FINSCHOOL_NAME" + fsNameField, printData._fsName);//小学校名
            svf.VrsOut("ABSENCE1", printData._syukketsu5);//欠席数（５年）
            svf.VrsOut("ABSENCE2", printData._syukketsu6);//欠席数（６年）
            final String simaiName1Field = KNJ_EditEdit.getMS932ByteLength(printData._simaiName1) > 36 ? "2": "1";
            svf.VrsOut("BRO_NAME1_" + simaiName1Field, printData._simaiName1);//兄弟在籍１
            final String simaiName2Field = KNJ_EditEdit.getMS932ByteLength(printData._simaiName2) > 36 ? "2": "1";
            svf.VrsOut("BRO_NAME2_" + simaiName2Field, printData._simaiName2);//兄弟在籍２

            svf.VrEndPage();
            _hasData = true;
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
                final String name       = rs.getString("NAME");
                final String examNo     = rs.getString("EXAMNO");
                final String gName      = rs.getString("GNAME");
                final String fsName     = rs.getString("FS_NAME");
                final String syukketsu5 = rs.getString("SYUKKETSU5");
                final String syukketsu6 = rs.getString("SYUKKETSU6");
                final String simaiName1 = rs.getString("SIMAI_NAME1");
                final String simaiName2 = rs.getString("SIMAI_NAME2");

                final PrintData printData = new PrintData(name, examNo, gName, fsName, syukketsu5, syukketsu6, simaiName1, simaiName2);
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
        stb.append("     VALUE(BASE.NAME, '') AS NAME, ");
        stb.append("     VALUE(BASE.EXAMNO, '') AS EXAMNO, ");
        stb.append("     VALUE(ADDR.GNAME, '') AS GNAME, ");
        stb.append("     VALUE(FIN.FINSCHOOL_NAME, '') AS FS_NAME, ");
        stb.append("     VALUE(CONF.ABSENCE_DAYS2, 0) AS SYUKKETSU5, ");
        stb.append("     VALUE(CONF.ABSENCE_DAYS3, 0) AS SYUKKETSU6, ");
        stb.append("     VALUE(BASE.SIMAI_NAME1, '') AS SIMAI_NAME1, ");
        stb.append("     VALUE(BASE.SIMAI_NAME2, '') AS SIMAI_NAME2 ");
        stb.append("  ");
        stb.append(" FROM ");
        stb.append("     V_ENTEXAM_APPLICANTBASE_DAT BASE ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT ADDR  ");
        stb.append("          ON BASE.ENTEXAMYEAR  = ADDR.ENTEXAMYEAR ");
        stb.append("         AND BASE.APPLICANTDIV = ADDR.APPLICANTDIV ");
        stb.append("         AND BASE.EXAMNO       = ADDR.EXAMNO ");
        stb.append("     LEFT JOIN FINSCHOOL_MST FIN ");
        stb.append("         ON BASE.FS_CD = FIN.FINSCHOOLCD ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT CONF ");
        stb.append("          ON BASE.ENTEXAMYEAR  = CONF.ENTEXAMYEAR ");
        stb.append("         AND BASE.APPLICANTDIV = CONF.APPLICANTDIV ");
        stb.append("         AND BASE.EXAMNO       = CONF.EXAMNO ");
        stb.append(" WHERE ");
        stb.append("         BASE.ENTEXAMYEAR  = '"+ _param._entexamYear + "' ");
        stb.append("     AND BASE.APPLICANTDIV = '"+ _param._applicantDiv + "' ");
        stb.append("     AND BASE.TESTDIV      = '"+ _param._testDiv + "' ");
        stb.append(" ORDER BY ");
        stb.append("     BASE.EXAMNO ");

        return stb.toString();
    }

    private class PrintData {
        final String _name;
        final String _examNo;
        final String _gName;
        final String _fsName;
        final String _syukketsu5;
        final String _syukketsu6;
        final String _simaiName1;
        final String _simaiName2;
        public PrintData(
                final String name,
                final String examNo,
                final String gName,
                final String fsName,
                final String syukketsu5,
                final String syukketsu6,
                final String simaiName1,
                final String simaiName2
        ) {
            _name       = name;
            _examNo     = examNo;
            _gName      = gName;
            _fsName     = fsName;
            _syukketsu5 = syukketsu5;
            _syukketsu6 = syukketsu6;
            _simaiName1 = simaiName1;
            _simaiName2 = simaiName2;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$$Revision: 71135 $$");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _entexamYear;
        private final String _applicantDiv;
        private final String _testDiv;
        private final String _loginYear;
        private final String _loginSemester;
        private final String _loginDate;
        private final String _schoolKind;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamYear   = request.getParameter("ENTEXAMYEAR");
            _applicantDiv  = request.getParameter("APPLICANTDIV");
            _testDiv       = request.getParameter("TESTDIV");
            _loginYear     = request.getParameter("LOGIN_YEAR");
            _loginSemester = request.getParameter("LOGIN_SEMESTER");
            _loginDate     = request.getParameter("LOGIN_DATE");
            _schoolKind    = request.getParameter("SCHOOLKIND");
        }
    }
}

// eof
