/*
 * 作成日: 2020/09/08
 * 作成者: shimoji
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJL370I {

    private static final Log log = LogFactory.getLog(KNJL370I.class);

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
        svf.VrSetForm("KNJL370I.frm", 1);

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final String sql = sql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            final int maxLine = 45;
            int lineCnt = 1;
            int pageCnt = 1;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
            String outputDate = sdf.format(new Date());

            setTitle(svf, pageCnt, outputDate);

            while (rs.next()) {
                final String examno = rs.getString("EXAMNO");
                final String name = rs.getString("NAME");
                final String nameKana = rs.getString("NAME_KANA");
                final String sex = rs.getString("SEX");
                final String finschoolName = rs.getString("FINSCHOOL_NAME");
                String recomExamno = rs.getString("RECOM_EXAMNO");
                final String distName = rs.getString("DIST_NAME");

                // 改ページの制御
                if (lineCnt > maxLine) {
                    svf.VrEndPage();
                    lineCnt = 1;
                    pageCnt++;
                    setTitle(svf, pageCnt, outputDate);
                }

                svf.VrsOutn("EXAM_NO1", lineCnt, examno);

                final int nameByte = KNJ_EditEdit.getMS932ByteLength(name);
                final String nameFieldStr = nameByte > 30 ? "3" : nameByte > 20 ? "2" : "1";
                svf.VrsOutn("NAME" + nameFieldStr, lineCnt, name);

                final int kanaByte = KNJ_EditEdit.getMS932ByteLength(nameKana);
                final String kanaFieldStr = kanaByte > 30 ? "3" : kanaByte > 20 ? "2" : "1";
                svf.VrsOutn("KANA" + kanaFieldStr, lineCnt, nameKana);

                svf.VrsOutn("SEX", lineCnt, sex);

                final int finschoolNameByte = KNJ_EditEdit.getMS932ByteLength(finschoolName);
                final String finschoolNameFieldStr = finschoolNameByte > 30 ? "3" : finschoolNameByte > 20 ? "2" : "1";
                svf.VrsOutn("FINSCHOOL_NAME" + finschoolNameFieldStr, lineCnt, finschoolName);

                svf.VrsOutn("EXAM_NO2", lineCnt, recomExamno);

                final int distNameByte = KNJ_EditEdit.getMS932ByteLength(distName);
                final String distNameFieldName = distNameByte > 14 ? "2" : "1";
                svf.VrsOutn("DISTRICT_NAME" + distNameFieldName, lineCnt, distName);

                lineCnt++;
                _hasData = true;
            }

            if (_hasData) {
                svf.VrEndPage();
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
    }

    private void setTitle(final Vrw32alp svf, final int pageNo, final String outputDate) {
        final String sexStr = "1".equals(_param._sex) ? "男子のみ" : "2".equals(_param._sex) ? "女子のみ" : "3".equals(_param._sex) ? "男女共" : "";
        final String orderStr = "1".equals(_param._order) ? "受験番号順" : "2".equals(_param._order) ? "氏名カナ順" : "3".equals(_param._order) ? "出身校コード順" : "";
        final String titleStr = "  志願者名簿(" + sexStr + ")(" + orderStr + ")";
        svf.VrsOut("TITLE", _param._entexamyear + "年度入学試験   " + _param._testDivName + titleStr);
        svf.VrsOut("SCHOOL_NAME", _param._schoolName);
        svf.VrsOut("PAGE", pageNo + "頁");
        svf.VrsOut("DATE", outputDate);
    }

    private String sql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     BASE.EXAMNO, ");
        stb.append("     BASE.NAME, ");
        stb.append("     BASE.NAME_KANA, ");
        stb.append("     Z002.NAME2 AS SEX, ");
        stb.append("     FS.FINSCHOOL_NAME, ");
        stb.append("     RECOM_EXAMNO, ");
        stb.append("     Z003.NAME1 AS DIST_NAME ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_APPLICANTBASE_DAT BASE ");
        stb.append("     LEFT JOIN V_NAME_MST Z002 ON Z002.NAMECD1 = 'Z002' ");
        stb.append("          AND BASE.ENTEXAMYEAR = Z002.YEAR ");
        stb.append("          AND BASE.SEX = Z002.NAMECD2 ");
        stb.append("     LEFT JOIN FINSCHOOL_MST FS ON BASE.FS_CD = FS.FINSCHOOLCD ");
        stb.append("     LEFT JOIN V_NAME_MST Z003 ON Z003.NAMECD1 = 'Z003' ");
        stb.append("          AND BASE.ENTEXAMYEAR = Z003.YEAR ");
        stb.append("          AND FS.DISTRICTCD = Z003.NAMECD2 ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT DETAIL ON DETAIL.SEQ = '005' ");
        stb.append("          AND BASE.APPLICANTDIV = DETAIL.APPLICANTDIV ");
        stb.append("          AND BASE.ENTEXAMYEAR = DETAIL.ENTEXAMYEAR ");
        stb.append("          AND BASE.EXAMNO = DETAIL.EXAMNO ");
        stb.append(" WHERE ");
        stb.append("     BASE.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND BASE.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND BASE.TESTDIV = '" + _param._testDiv + "' ");
        stb.append("     AND VALUE(BASE.JUDGEMENT, '') <> '5' "); //5:未受験  B日程志願者のうちA日程または帰国生入試で既に合格している場合は除外
        /**
         * 抽出区分(SEX)は 3:全員、1:男子のみ、2:女子のみ となっており、
         * 1 ～ 3 のいずれかが渡ってくる。
         * 3:全員 以外が選択されたときのみ抽出条件に加える。
         */
        if (!"3".equals(_param._sex)) {
            stb.append("     AND BASE.SEX = '" + _param._sex + "' ");
        }
        if (("1".equals(_param._testDiv) || "2".equals(_param._testDiv)) && "1".equals(_param._except)) {
            stb.append("     AND NOT EXISTS ( ");
            stb.append("                 SELECT ");
            stb.append("                     'X' ");
            stb.append("                 FROM ");
            stb.append("                     ENTEXAM_APPLICANTBASE_DAT RECOM_BASE ");
            stb.append("                 WHERE ");
            stb.append("                     BASE.ENTEXAMYEAR         = RECOM_BASE.ENTEXAMYEAR ");
            stb.append("                     AND BASE.APPLICANTDIV    = RECOM_BASE.APPLICANTDIV ");
            stb.append("                     AND BASE.RECOM_EXAMNO    = RECOM_BASE.EXAMNO ");
            stb.append("                     AND RECOM_BASE.TESTDIV   = '" + _param._exceptTestDiv + "' ");
            stb.append("                     AND RECOM_BASE.JUDGEMENT = '1' ");
            stb.append("             ) ");
        }
        stb.append(" ORDER BY ");
        /**
         * 並び順(ORDER)は 1:受験者番号、2:指名カナ順、3:出身コード順 となっており、
         * 1 ～ 3 のいずれかが渡ってくる。
         * 渡ってきた値に該当する項目でソートする。
         */
        if ("2".equals(_param._order)) {
            stb.append("     BASE.NAME_KANA, ");
        } else if ("3".equals(_param._order)) {
            stb.append("     BASE.FS_CD, ");
        }
        stb.append("     BASE.EXAMNO ");
        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        private final String _entexamyear;
        private final String _applicantDiv;
        private final String _testDiv;
        private final String _schoolName;
        private final String _sex;
        private final String _order;
        private final String _except;
        private final String _exceptTestDiv;
        private final String _testDivName;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _schoolName = getSchoolName(db2);
            _sex = request.getParameter("SEX");
            _order = request.getParameter("ORDER");
            _except = request.getParameter("EXCEPT");
            _exceptTestDiv = "1".equals(_testDiv) ? "2" : "2".equals(_testDiv) ? "1" : null;
            _testDivName = getTestDivName(db2);
        }

        private String getSchoolName(final DB2UDB db2) {
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;

            // 入試制度（APPLICANTDIV） が 1:中学、2:高校 の場合のみ名称を取得する。
            String sqlwk = " SELECT SCHOOL_NAME FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + _entexamyear + "'";
            String sql = "1".equals(_applicantDiv) ? (sqlwk + " AND CERTIF_KINDCD = '105' ") : "2".equals(_applicantDiv) ? (sqlwk + " AND CERTIF_KINDCD = '106' ") : "";
            log.debug(" sql =" + sql);

            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString("SCHOOL_NAME");
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtn;
        }

        private String getTestDivName(final DB2UDB db2) {
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;

            String sql = " SELECT TESTDIV_ABBV FROM ENTEXAM_TESTDIV_MST WHERE ENTEXAMYEAR='" + _entexamyear + "' AND APPLICANTDIV = '" + _applicantDiv + "' AND TESTDIV = '" + _testDiv + "' ";
            log.debug(" sql =" + sql);

            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString("TESTDIV_ABBV");
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtn;
        }
    }
}

// eof

