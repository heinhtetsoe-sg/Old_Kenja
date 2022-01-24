// kanji=漢字
/*
 * $Id: dfe97069a3b1b4d62231eca4f47b7ee801650376 $
 *
 * 作成日: 2011/12/29 9:28:24 - JST
 * 作成者: m-yama
 *
 * Copyright(C) 2009-2011 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 * <<クラスの説明>>。
 * @author m-yama
 * @version $Id: dfe97069a3b1b4d62231eca4f47b7ee801650376 $
 */
public class KNJL363C {

    private static final Log log = LogFactory.getLog("KNJL363C.class");

    private boolean _hasData;

    private static final String FORM_FILE = "KNJL363C.frm";

    private static final String APP_JUNIOR = "1";
    private static final String APP_SENIOR = "2";

    private static final String PRINT_GOUKAKU = "1";
    private static final String PRINT_ZENIN = "2";
    private static final String PRINT_EXAMNO = "3";
    private static final String PRINT_SIGAN_ZENIN = "4";

    private static final int MAX_LINE = 6;
    private static final int MAX_ROW = 3;

    Param _param;

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
            init(response, svf);

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);

            _hasData = false;

            printMain(db2, svf);

            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {
            closeDb(db2);
            svf.VrQuit();
        }

    }

    private void init(
            final HttpServletResponse response,
            final Vrw32alp svf
    ) throws IOException {
        response.setContentType("application/pdf");

        svf.VrInit();
        svf.VrSetSpoolFileStream(response.getOutputStream());
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        final List printList = getPrintList(db2);
        int currentLine = Integer.parseInt(_param._line);
        int currentRow = Integer.parseInt(_param._row);
        int page = 1;
        svf.VrSetForm(FORM_FILE, 1);
        for (final Iterator itPrint = printList.iterator(); itPrint.hasNext();) {
            final PrintLabel printLabel = (PrintLabel) itPrint.next();
            if (MAX_ROW < currentRow) {
                currentRow = 1;
                currentLine++;
                if (MAX_LINE < currentLine) {
                    svf.VrEndPage();
                    page++;
                    currentLine = 1;
                }
            }

            svf.VrsOutn("EXAMNO" + currentRow, currentLine, "受験番号 " + printLabel._examNo + " 番");
            svf.VrsOutn("NAME" + currentRow + (getMS932ByteCount(printLabel._name) > 20 ? "_2" : "_1"), currentLine, printLabel._name);
            svf.VrsOutn("TESTDIV" + currentRow, currentLine, _param._nendoNumber + printLabel._applicantDiv);

            currentRow++;

            _hasData = true;
        }

        if (_hasData) {
            svf.VrEndPage();
        }
    }

    private int getMS932ByteCount(final String str) {
        if (null == str) return 0;
        int ret = 0;
        try {
            ret = str.getBytes("MS932").length;
        } catch (Exception e) {
            log.error("exception!", e);
        }
        return ret;
    }

    private List getPrintList(final DB2UDB db2) throws SQLException {
        final List retList = new ArrayList();
        final String sql = getPrintDataSql();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String examNo = rs.getString("EXAMNO");
                final String testDiv = rs.getString("TESTDIV");
                final String name = rs.getString("NAME");
                final PrintLabel printLabel = new PrintLabel(examNo, testDiv, name);
                retList.add(printLabel);
            }
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        
        return retList;
    }

    /** ラベル取得 */
    private String getPrintDataSql() {
        StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     BASE.EXAMNO, ");
        stb.append("     BASE.TESTDIV, ");
        stb.append("     BASE.NAME ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_APPLICANTBASE_DAT BASE ");
        stb.append("     LEFT JOIN ENTEXAM_RECEPT_DAT RECEPT ON RECEPT.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
        stb.append("         AND RECEPT.APPLICANTDIV = BASE.APPLICANTDIV ");
        stb.append("         AND RECEPT.TESTDIV = BASE.TESTDIV ");
        stb.append("         AND RECEPT.EXAMNO = BASE.EXAMNO ");
        stb.append(" WHERE ");
        stb.append("     BASE.ENTEXAMYEAR = '" + _param._year + "' ");
        stb.append("     AND BASE.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND BASE.TESTDIV = '" + _param._testDiv + "' ");
        if (PRINT_GOUKAKU.equals(_param._printType)) {
            stb.append("     AND BASE.JUDGEMENT = '1' ");
        }
        if (PRINT_ZENIN.equals(_param._printType)) {
            stb.append("     AND RECEPT.EXAMNO IS NOT NULL ");
        }
        if (PRINT_EXAMNO.equals(_param._printType)) {
            stb.append("     AND RECEPT.EXAMNO = '" + _param._examNo +"' ");
            if (null != _param._goukakusha) {
                stb.append(" AND BASE.JUDGEMENT = '1' ");
            }
        }
        if (PRINT_SIGAN_ZENIN.equals(_param._printType)) { //志願者全員
        }
        stb.append(" ORDER BY ");
        stb.append("     BASE.EXAMNO ");

        return stb.toString();

    }

    private class PrintLabel {
        private final String _examNo;
        private final String _testDiv;
        private final String _name;
        private final String _applicantDiv;

        public PrintLabel(
                final String examNo,
                final String testDiv,
                final String name
        ) {
            _examNo = examNo;
            _testDiv = testDiv;
            _name = name;
            if (APP_JUNIOR.equals(_param._applicantDiv)) {
                _applicantDiv = "中";
            } else if (APP_SENIOR.equals(_param._applicantDiv)) {
                _applicantDiv = "高";
            } else {
                _applicantDiv = "";
            }
        }
    }

    private void closeDb(final DB2UDB db2) {
        if (null != db2) {
            db2.commit();
            db2.close();
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 64649 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _year;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final String _applicantDiv;
        private final String _testDiv;
        private final String _printType;
        private final String _examNo;
        private final String _goukakusha;
        private final String _line;
        private final String _row;
        private final String _nendoNumber;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _ctrlSemester = request.getParameter("SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _printType = request.getParameter("PRINT_TYPE");
            _examNo = request.getParameter("EXAMNO");
            _goukakusha = request.getParameter("GOUKAKUSHA");
            _line = request.getParameter("LINE");
            _row = request.getParameter("ROW");
            _nendoNumber= KNJ_EditDate.tate_format(KNJ_EditDate.h_format_JP(_year + "-01-01"))[1];
        }

    }
}

// eof
