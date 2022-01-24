/**     プロジェクト/プロパティ/javaコード・スタイル/コード・テンプレート/パターン       ***/

/*
 * $Id: bcebacbc6cb27d63ed5efeb8ac7feb239af5fa5b $
 *
 * 作成日: 2018/11/12
 * 作成者: yamashiro
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

public class KNJL056E {

    private static final Log log = LogFactory.getLog(KNJL056E.class);

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
        svf.VrSetForm("KNJL056E.frm", 1);
        final List printList = getList(db2);
        int renban = 1;
        int lineCnt = 1;
        int printPage = 1;
        final int maxLine = 40;
        final int pageCnt = printList.size() / maxLine;
        final int pageAmari = (printList.size() % maxLine) > 0 ? 1 : 0;
        final int totalPage = pageCnt + pageAmari;
        for (Iterator iterator = printList.iterator(); iterator.hasNext();) {
            final PrintData printData = (PrintData) iterator.next();

            if (maxLine < lineCnt) {
                svf.VrEndPage();
                lineCnt = 1;
                printPage++;
            }
            svf.VrsOut("DATE", KNJ_EditDate.getAutoFormatDate(db2, _param._ctrlDate));
            svf.VrsOut("TITLE", KNJ_EditDate.getAutoFormatYear(db2, Integer.parseInt(_param._year)) + "年度　" + _param._testdivName + "　成績入力チェックリスト");
            svf.VrsOut("SUB_TITLE", "会場名：" + _param._examhallName + "　教科名：" + _param._subclassName);
            svf.VrsOut("PAGE1", String.valueOf(printPage));
            svf.VrsOut("PAGE2", String.valueOf(totalPage));

            svf.VrsOutn("NO", lineCnt, String.valueOf(renban));
            svf.VrsOutn("EXAM_NO", lineCnt, printData._examno);
            svf.VrsOutn("NAME1", lineCnt, printData._name);
            svf.VrsOutn("FINSCHOOL_NAME", lineCnt, printData._finschoolNameAbbv);
            svf.VrsOutn("SCORE", lineCnt, printData._score);
            _hasData = true;
            renban++;
            lineCnt++;
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
            final String sql = getPrintSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String examhallgroupOrder = rs.getString("EXAMHALLGROUP_ORDER");
                final String examno = rs.getString("EXAMNO");
                final String name = rs.getString("NAME");
                final String finschoolNameAbbv = StringUtils.defaultString(rs.getString("FINSCHOOL_NAME_ABBV"));
                final String score = StringUtils.defaultString(rs.getString("SCORE"));

                final PrintData printData = new PrintData(examhallgroupOrder, examno, name, finschoolNameAbbv, score);
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

    private String getPrintSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     HALLGD.EXAMHALLGROUP_ORDER, ");
        stb.append("     BASE.EXAMNO, ");
        stb.append("     BASE.NAME, ");
        stb.append("     FSCHOOL.FINSCHOOL_NAME_ABBV, ");
        stb.append("     SCORE.SCORE ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_APPLICANTBASE_DAT BASE ");
        stb.append("     INNER JOIN ENTEXAM_HALL_GROUP_DAT HALLGD ");
        stb.append("          ON BASE.ENTEXAMYEAR      = HALLGD.ENTEXAMYEAR ");
        stb.append("         AND BASE.APPLICANTDIV     = HALLGD.APPLICANTDIV ");
        stb.append("         AND BASE.TESTDIV          = HALLGD.TESTDIV ");
        stb.append("         AND HALLGD.EXAMHALL_TYPE  = '2' ");
        stb.append("         AND HALLGD.EXAMHALLCD     = '" + _param._examhallcd + "' ");
        stb.append("         AND BASE.EXAMNO           = HALLGD.EXAMNO ");
        stb.append("     LEFT JOIN FINSCHOOL_MST FSCHOOL ");
        stb.append("          ON BASE.FS_CD = FSCHOOL.FINSCHOOLCD ");
        stb.append("     LEFT JOIN ENTEXAM_SCORE_DAT SCORE ");
        stb.append("          ON BASE.ENTEXAMYEAR      = SCORE.ENTEXAMYEAR ");
        stb.append("         AND BASE.APPLICANTDIV     = SCORE.APPLICANTDIV ");
        stb.append("         AND BASE.TESTDIV          = SCORE.TESTDIV ");
        stb.append("         AND SCORE.EXAM_TYPE       = '1' ");
        stb.append("         AND BASE.EXAMNO           = SCORE.RECEPTNO ");
        stb.append("         AND SCORE.TESTSUBCLASSCD  = '" + _param._subclasscd + "' ");
        stb.append(" WHERE ");
        stb.append("         BASE.ENTEXAMYEAR            = '" + _param._year + "' ");
        stb.append("     AND BASE.APPLICANTDIV           = '" + _param._applicantdiv + "' ");
        stb.append("     AND BASE.TESTDIV                = '" + _param._testdiv + "' ");
        stb.append("     AND VALUE(BASE.JUDGEMENT, '0') != '3' ");
        stb.append(" ORDER BY ");
        stb.append("     HALLGD.EXAMHALLGROUPCD, ");
        stb.append("     int(HALLGD.EXAMHALLGROUP_ORDER), ");
        stb.append("     BASE.EXAMNO ");

        return stb.toString();
    }

    private class PrintData {
        final String _examhallgroupOrder;
        final String _examno;
        final String _name;
        final String _finschoolNameAbbv;
        final String _score;

        public PrintData(
                final String examhallgroupOrder,
                final String examno,
                final String name,
                final String finschoolNameAbbv,
                final String score
        ) {
            _examhallgroupOrder = examhallgroupOrder;
            _examno = examno;
            _name = name;
            _finschoolNameAbbv = finschoolNameAbbv;
            _score = score;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 64200 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        private final String _applicantdiv;
        private final String _applicantdivName;
        private final String _testdiv;
        private final String _testdivName;
        private final String _examhallcd;
        private final String _examhallName;
        private final String _subclasscd;
        private final String _subclassName;
        private final String _prgid;
        private final String _year;
        private final String _ctrlYear;
        private final String _ctrlSemester;
        private final String _ctrlDate;
        private final String _printLogStaffcd;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _applicantdiv = request.getParameter("APPLICANTDIV");
            _testdiv = request.getParameter("TESTDIV");
            _examhallcd = request.getParameter("EXAMHALLCD");
            _subclasscd = request.getParameter("SUBCLASSCD");
            _prgid = request.getParameter("PRGID");
            _year = request.getParameter("YEAR");
            _ctrlYear = request.getParameter("CTRL_YEAR");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE");
            _printLogStaffcd = request.getParameter("PRINT_LOG_STAFFCD");
            _applicantdivName = getNameMst(db2, "L003", _applicantdiv);
            _testdivName = getNameMst(db2, "L004", _testdiv);
            _examhallName = getHallName(db2);
            _subclassName = getNameMst(db2, "L009", _subclasscd);
        }

        private String getNameMst(final DB2UDB db2, final String namecd1, final String namecd2) {
            String retStr = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     NAME1 ");
                stb.append(" FROM ");
                stb.append("     V_NAME_MST ");
                stb.append(" WHERE ");
                stb.append("     YEAR    = '" + _year + "' ");
                stb.append("     AND NAMECD1 = '" + namecd1 + "' ");
                stb.append("     AND NAMECD2 = '" + namecd2 + "' ");
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    retStr = rs.getString("NAME1");
                }
            } catch (SQLException ex) {
                log.debug("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retStr;
        }

        private String getHallName(final DB2UDB db2) {
            String retStr = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     EXAMHALL_NAME ");
                stb.append(" FROM ");
                stb.append("     ENTEXAM_HALL_YDAT ");
                stb.append(" WHERE ");
                stb.append("     ENTEXAMYEAR    = '" + _year + "' ");
                stb.append("     AND APPLICANTDIV = '" + _applicantdiv + "' ");
                stb.append("     AND TESTDIV = '" + _testdiv + "' ");
                stb.append("     AND EXAM_TYPE = '2' ");
                stb.append("     AND EXAMHALLCD = '" + _examhallcd + "' ");
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    retStr = rs.getString("EXAMHALL_NAME");
                }
            } catch (SQLException ex) {
                log.debug("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retStr;
        }

    }
}

// eof
