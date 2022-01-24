/*
 * $Id: fcca5fae15671ab664380f9444b87f2df2b3236f $
 *
 * 作成日: 2017/11/06
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

public class KNJL341U {

    private static final Log log = LogFactory.getLog(KNJL341U.class);

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
        svf.VrSetForm("KNJL341U.frm", 1);
        final List printList = getList(db2);
        final int poRowMax = 8;
        int poRow = Integer.parseInt(_param._poRow); //行
        int poCol = Integer.parseInt(_param._poCol); //列

        for (Iterator iterator = printList.iterator(); iterator.hasNext();) {
            final PrintData printData = (PrintData) iterator.next();
            if (poCol > 3) {
                poCol = 1;
                poRow++;
                if (poRow > poRowMax) {
                    svf.VrEndPage();
                    poRow = 1;
                }
            }
            svf.VrsOutn("SCHREG_NO" + poCol, poRow, printData._schregNo);
            final String kanaField = KNJ_EditEdit.getMS932ByteLength(printData._kana) > 32 ? "_4" : KNJ_EditEdit.getMS932ByteLength(printData._kana) > 22 ? "_3" : KNJ_EditEdit.getMS932ByteLength(printData._kana) > 16 ? "_2" : "_1";
            svf.VrsOutn("KANA" + poCol + kanaField, poRow, printData._kana);
            final String nameField = KNJ_EditEdit.getMS932ByteLength(printData._name) > 32 ? "_4" : KNJ_EditEdit.getMS932ByteLength(printData._name) > 22 ? "_3" : KNJ_EditEdit.getMS932ByteLength(printData._name) > 16 ? "_2" : "_1";
            svf.VrsOutn("NAME" + poCol + nameField, poRow, printData._name);
            svf.VrsOutn("EXAM_NO" + poCol, poRow, printData._examNo);

            poCol++;
            _hasData = true;
        }
        if (_hasData) {
            svf.VrEndPage();
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
                final String schregNo = rs.getString("SCHREGNO");
                final String name     = rs.getString("NAME");
                final String kana     = rs.getString("NAME_KANA");
                final String examNo   = rs.getString("EXAMNO");

                final PrintData printData = new PrintData(schregNo, name, kana, examNo);
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
        stb.append(" SELECT DISTINCT ");
        stb.append("     D026.REMARK1 AS SCHREGNO, ");
        stb.append("     BASE.NAME, ");
        stb.append("     BASE.NAME_KANA, ");
        stb.append("     BASE.EXAMNO ");
        stb.append(" FROM ");
        stb.append("     V_ENTEXAM_APPLICANTBASE_DAT BASE ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT D026 ");
        stb.append("          ON D026.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ");
        stb.append("         AND D026.APPLICANTDIV = BASE.APPLICANTDIV ");
        stb.append("         AND D026.EXAMNO       = BASE.EXAMNO ");
        stb.append("         AND D026.SEQ          = '026' ");
        stb.append("     INNER JOIN NAME_MST L013 ON L013.NAMECD1    = 'L013' ");
        stb.append("                             AND BASE.JUDGEMENT  = L013.NAMECD2 ");
        stb.append("                             AND L013.NAMESPARE1 = '1' ");
        stb.append(" WHERE ");
        stb.append("         BASE.ENTEXAMYEAR  = '" + _param._entExamYear + "' ");
        stb.append("     AND BASE.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND BASE.TESTDIV      = '" + _param._testDiv + "' ");
        stb.append("     AND BASE.PROCEDUREDIV <> '2' ");
        stb.append("     AND BASE.PROCEDUREDIV IS NOT NULL ");
        stb.append(" ORDER BY ");
        stb.append("     D026.REMARK1 ");

        return stb.toString();
    }

    private class PrintData {
        final String _schregNo;
        final String _name;
        final String _kana;
        final String _examNo;
        public PrintData(
                final String schregNo,
                final String name,
                final String kana,
                final String examNo
        ) {
            _schregNo = schregNo;
            _name     = name;
            _kana     = kana;
            _examNo   = examNo;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 56978 $");
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
        private final String _poRow;
        private final String _poCol;
        private final String _entExamYear;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _loginYear     = request.getParameter("LOGIN_YEAR");
            _loginSemester = request.getParameter("LOGIN_SEMESTER");
            _loginDate     = request.getParameter("LOGIN_DATE");
            _applicantDiv  = request.getParameter("APPLICANTDIV");
            _testDiv       = request.getParameter("TESTDIV");
            _poRow         = request.getParameter("POROW");
            _poCol         = request.getParameter("POCOL");
            _entExamYear   = request.getParameter("ENTEXAMYEAR");

        }
    }
}

// eof
