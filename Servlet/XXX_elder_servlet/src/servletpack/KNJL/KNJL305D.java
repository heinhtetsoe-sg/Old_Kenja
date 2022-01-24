/*
 * $Id: 8428cad7f521fe3d3593c9825b6273e32fba24b7 $
 *
 * 作成日: 2017/11/01
 * 作成者: tawada
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.io.File;
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

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJL305D {

    private static final Log log = LogFactory.getLog(KNJL305D.class);

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
        if (!"1".equals(_param._testDiv)){
            svf.VrSetForm("KNJL305D_1.frm", 1);
        }else{
            svf.VrSetForm("KNJL305D_2.frm", 1);
        }

        final List printList = getList(db2);

        for (Iterator iterator = printList.iterator(); iterator.hasNext();) {
            final PrintData printData = (PrintData) iterator.next();
            //受験番号
            svf.VrsOut("EXAM_NO", printData._examNo);
            //氏名
            if (20 >= KNJ_EditEdit.getMS932ByteLength(printData._name)){
                svf.VrsOut("NAME1", printData._name);
            }else if (30 >= KNJ_EditEdit.getMS932ByteLength(printData._name)){
                svf.VrsOut("NAME2", printData._name);
            }else{
                svf.VrsOut("NAME3", printData._name);
            }
            //氏名かな
            if (30 >= KNJ_EditEdit.getMS932ByteLength(printData._nameKana)){
                svf.VrsOut("KANA1", printData._nameKana);
            }else if (40 >= KNJ_EditEdit.getMS932ByteLength(printData._nameKana)){
                svf.VrsOut("KANA2", printData._nameKana);
            }else{
                svf.VrsOut("KANA3", printData._nameKana);
            }
            //郵便番号
            svf.VrsOut("ZIP_NO", printData._gzipcd);
            //住所
            final String addrStr = printData._gaddress1 + printData._gaddress2;
            if (40 >= KNJ_EditEdit.getMS932ByteLength(addrStr)){
                svf.VrsOut("ADDR1", addrStr);
            }else if (60 >= KNJ_EditEdit.getMS932ByteLength(addrStr)){
                svf.VrsOut("ADDR2", addrStr);
            }else{
                svf.VrsOut("ADDR3", addrStr);
            }
            //電話番号
            svf.VrsOut("TEL_NO", printData._gtelno);
            //画像3種
            if (!"1".equals(_param._testDiv)){
                String image_path = _param._documentRoot + "/" + _param._imagePath + "/";//イメージファイルパス
                String mark1_fpath = image_path + "MARKSHEET_SAMPLE1.jpg";
                String mark2_fpath = image_path + "MARKSHEET_SAMPLE2.jpg";
                String mark3_fpath = image_path + "MARKSHEET_SAMPLE3.jpg";
                String mark4_fpath = image_path + "MARKSHEET_SAMPLE4.jpg";
                File f1 = new File(mark1_fpath);
                File f2 = new File(mark2_fpath);
                File f3 = new File(mark3_fpath);
                File f4 = new File(mark4_fpath);
                if (f1.exists()) svf.VrsOut("MARK1", mark1_fpath );
                if (f2.exists()) svf.VrsOut("MARK2", mark2_fpath );
                if (f3.exists()) svf.VrsOut("MARK3", mark3_fpath );
                if (f4.exists()) svf.VrsOut("MARK4", mark4_fpath );
            }

            svf.VrEndPage();
            _hasData = true;
        }
    }

    private List getList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        PreparedStatement ps_hall = null;
        ResultSet rs_hall = null;

        try {
     	    final String hallsql = getHallSql();
            log.debug(" hallsql =" + hallsql);
            ps_hall = db2.prepareStatement(hallsql);
            rs_hall = ps_hall.executeQuery();
            while (rs_hall.next()) {
                final String strtexamno = rs_hall.getString("S_RECEPTNO");
                final String endexamno = rs_hall.getString("E_RECEPTNO");

                final String sql = getSql(strtexamno, endexamno);
                log.debug(" sql =" + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    final String examNo = rs.getString("EXAMNO");
                    final String name = rs.getString("NAME");
                    final String nameKana = rs.getString("NAME_KANA");
                    final String gzipcd = rs.getString("GZIPCD");
                    final String gaddress1 = rs.getString("GADDRESS1");
                    final String gaddress2 = rs.getString("GADDRESS2");
                    final String gzipno = rs.getString("GTELNO");

                    final PrintData printData = new PrintData(examNo, name, nameKana, gzipcd, gaddress1, gaddress2, gzipno);
                    retList.add(printData);
                }
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String getHallSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     H1.ENTEXAMYEAR, ");
        stb.append("     H1.APPLICANTDIV, ");
        stb.append("     H1.TESTDIV, ");
        stb.append("     H1.EXAM_TYPE, ");
        stb.append("     H1.EXAMHALLCD, ");
        stb.append("     H1.EXAMHALL_NAME, ");
        stb.append("     H1.CAPA_CNT, ");
        stb.append("     H1.S_RECEPTNO, ");
        stb.append("     H1.E_RECEPTNO ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_HALL_YDAT H1 ");
        stb.append(" WHERE ");
        stb.append("     H1.ENTEXAMYEAR = '" + _param._entExamYear + "'");
        stb.append("     AND H1.APPLICANTDIV = '" + _param._applicantDiv + "'");
        stb.append("     AND H1.TESTDIV = '" + _param._testDiv + "'");
        stb.append("     AND H1.EXAMHALLCD IN " + SQLUtils.whereIn(true, _param._leftlist));
        stb.append(" ORDER BY ");
        stb.append("     H1.EXAMHALLCD ASC");

        return stb.toString();
    }
    private String getSql(final String strtexamno, final String endexamno) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.ENTEXAMYEAR, ");
        stb.append("     T1.APPLICANTDIV, ");
        stb.append("     T1.EXAMNO, ");
        stb.append("     T1.NAME, ");
        stb.append("     T1.NAME_KANA, ");
        stb.append("     AD1.GZIPCD, ");
        stb.append("     AD1.GADDRESS1, ");
        stb.append("     AD1.GADDRESS2, ");
        stb.append("     AD1.GTELNO ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_APPLICANTBASE_DAT T1 ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT AD1 ON T1.EXAMNO = AD1.EXAMNO AND ");
        stb.append("       T1.ENTEXAMYEAR = AD1.ENTEXAMYEAR AND ");
        stb.append("       T1.APPLICANTDIV = AD1.APPLICANTDIV");
        stb.append(" WHERE ");
        stb.append("     T1.ENTEXAMYEAR = '" + _param._entExamYear + "'");
        stb.append("     AND T1.APPLICANTDIV = '" + _param._applicantDiv + "'");
        stb.append("     AND T1.TESTDIV = '" + _param._testDiv + "'");
        stb.append("     AND T1.EXAMNO BETWEEN '" + strtexamno + "' AND '" + endexamno + "' ");
        stb.append(" ORDER BY ");
        stb.append("     T1.EXAMNO ASC");

        return stb.toString();
    }

    private class PrintData {
		final String _examNo;
		final String _name;
		final String _nameKana;
        final String _gzipcd;
        final String _gaddress1;
        final String _gaddress2;
        final String _gtelno;
        public PrintData(
                final String examNo,
                final String name,
                final String nameKana,
                final String gzipcd,
                final String gaddress1,
                final String gaddress2,
                final String gtelno
        ) {
            _examNo = examNo;
            _name = name;
            _nameKana = nameKana;
            _gzipcd = StringUtils.defaultString(gzipcd);
            _gaddress1 = StringUtils.defaultString(gaddress1);
            _gaddress2 = StringUtils.defaultString(gaddress2);
            _gtelno = StringUtils.defaultString(gtelno);
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 68292 $");
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
        private final String[] _leftlist;
        private final String _documentRoot;
        private String _imagePath;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _loginYear     = request.getParameter("LOGIN_YEAR");
            _loginSemester = request.getParameter("LOGIN_SEMESTER");
            _loginDate     = request.getParameter("LOGIN_DATE");
            _entExamYear   = request.getParameter("ENTEXAMYEAR");
            _applicantDiv  = request.getParameter("APPLICANTDIV");
            _testDiv       = request.getParameter("TESTDIV");
            _leftlist    = request.getParameterValues("LEFT_LIST");
            _documentRoot = request.getParameter("DOCUMENTROOT");
            loadControlMst(db2);

        }

        private void loadControlMst(final DB2UDB db2) {
            final String sql = "SELECT IMAGEPATH, EXTENSION FROM CONTROL_MST WHERE CTRL_NO = '01' ";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    _imagePath = rs.getString("IMAGEPATH");
                }
            } catch (SQLException e) {
                log.error("Exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

    }
}

// eof
