/*
 * 作成日: 2020/09/15
 * 作成者: matsushima
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJL403I {

    private static final Log log = LogFactory.getLog(KNJL403I.class);

    private boolean _hasData;

    private int MAX_LINE = 10;

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

        final List schoolList = getList(db2);
        for (Iterator iterator = schoolList.iterator(); iterator.hasNext();) {
            final Student student = (Student) iterator.next();

            //メイン
            printSvfMain(db2, svf, student);
            svf.VrEndPage();
        }
    }

    private void printSvfMain(final DB2UDB db2, final Vrw32alp svf, final Student student) {
        final String form = "KNJL403I.frm";
        svf.VrSetForm(form , 1);

        svf.VrsOut("DATE", _param._outputDate); //日付
        svf.VrsOut("TITLE", _param._documentTitle); //タイトル

        VrsOutnRenban(svf, "NOTE1", KNJ_EditEdit.get_token(_param._documentText, 76, 12)); //文言

        svf.VrsOut("NENDO", _param._nendo); //年度
        svf.VrsOut("SUBTITLE", "生徒氏名表記・字体 確認票"); //サブタイトル

        final String examno = student._examno.substring(student._examno.length() - 4);
        svf.VrsOut("EXAM_NO", examno); //受験番号
        final String nameField = KNJ_EditEdit.getMS932ByteLength(student._name) > 30 ? "3" : KNJ_EditEdit.getMS932ByteLength(student._name) > 22 ? "2" : "1";
        svf.VrsOut("NAME" + nameField, student._name); //氏名
        svf.VrEndRecord();

        _hasData = true;
    }

    protected void VrsOutnRenban(final Vrw32alp svf, final String field, final String[] value) {
        if (null != value) {
            for (int i = 0 ; i < value.length; i++) {
                svf.VrsOutn(field, i + 1, value[i]);
            }
        }
    }

    private List getList(final DB2UDB db2) {
        final List retList = new ArrayList();
        final String sql = getStudnetSql();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String examno = StringUtils.defaultString(rs.getString("EXAMNO"));
                final String name = StringUtils.defaultString(rs.getString("NAME"));
                final Student student = new Student(examno, name);
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

    private String getStudnetSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT  ");
        stb.append("   T1.EXAMNO, ");
        stb.append("   T1.NAME ");
        stb.append(" FROM ");
        stb.append("   ENTEXAM_APPLICANTBASE_DAT T1 ");
        stb.append("   INNER JOIN ENTEXAM_RECEPT_DAT T2 ");
        stb.append("           ON T2.ENTEXAMYEAR  = T1.ENTEXAMYEAR ");
        stb.append("          AND T2.APPLICANTDIV = T1.APPLICANTDIV  ");
        stb.append("          AND T2.TESTDIV      = '"+ _param._testDiv +"' ");
        stb.append("          AND T2.EXAM_TYPE    = '1' ");
        stb.append("          AND T2.EXAMNO       = T1.EXAMNO ");
        stb.append("          AND T2.JUDGEDIV     = '1' ");  //合格者のみ
        stb.append(" WHERE ");
        stb.append("       T1.ENTEXAMYEAR  = '"+ _param._entexamyear +"' ");
        stb.append("   AND T1.APPLICANTDIV = '"+ _param._applicantDiv +"' ");
        stb.append(" ORDER BY ");
        stb.append("   T1.EXAMNO ");

        return stb.toString();
    }


    private static class Student {
        final String _examno;
        final String _name;

        private Student(
                final String examno,
                final String name
        ) {
            _examno = examno;
            _name = name;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 76882 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        private final String _loginYear;
        private final String _entexamyear;
        private final String _applicantDiv;
        private final String _testDiv;
        private final String _preamble;

        private final String _outputDate;
        private final String _nendo;
        private final String _documentTitle;
        private final String _documentText;

        private String _testDivName;
        private String _capacity;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _loginYear = request.getParameter("LOGIN_YEAR");
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _preamble = request.getParameter("PREAMBLE");

            //作成日時
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
            final String date = sdf.format(new Date());
            final String[] outoutDate = date.split("/");
            _outputDate = outoutDate[0] + "年" + outoutDate[1] + "月" + outoutDate[2] + "日";

            _nendo = _entexamyear + "年度";
            _documentTitle = getDocument(db2, "TITLE");
            _documentText = getDocument(db2, "TEXT");

            getTestDivMst(db2);
        }

        private String getTestDivMst(final DB2UDB db2) {
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;

            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("   * ");
            stb.append(" FROM ");
            stb.append("   ENTEXAM_TESTDIV_MST ");
            stb.append(" WHERE ");
            stb.append("       ENTEXAMYEAR  = '"+ _entexamyear +"' ");
            stb.append("   AND APPLICANTDIV = '"+ _applicantDiv +"' ");
            stb.append("   AND TESTDIV      = '"+ _testDiv +"' ");

            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    _testDivName = rs.getString("TESTDIV_ABBV");
                    _capacity = rs.getString("CAPACITY");
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return rtn;
        }

        private String getDocument(final DB2UDB db2, final String field) {
            String rtnStr = "";
            PreparedStatement ps = null;
            ResultSet rs = null;

            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("   * ");
            stb.append(" FROM ");
            stb.append("   DOCUMENT_MST ");
            stb.append(" WHERE ");
            stb.append("   DOCUMENTCD = '"+ _preamble +"' ");

            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtnStr = rs.getString(field);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return rtnStr;
        }

    }
}

// eof

