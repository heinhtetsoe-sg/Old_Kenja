/*
 * 作成日: 2020/09/09
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
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJL369I {

    private static final Log log = LogFactory.getLog(KNJL369I.class);

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
        svf.VrSetForm("KNJL369I.frm", 4);

        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final String sql = sql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            final int maxLine = 10;
            int lineCnt = 1;
            int pageCnt = 1;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
            String outputDate = sdf.format(new Date());

            setTitle(svf, pageCnt, outputDate);

            while (rs.next()) {
                final String examno = rs.getString("EXAMNO");
                final String nameKana = rs.getString("NAME_KANA");
                final String name = rs.getString("NAME");
                final String sex = rs.getString("SEX");
                final String birthday = rs.getString("BIRTHDAY");
                final String zipcd = rs.getString("ZIPCD");
                final String address1 = rs.getString("ADDRESS1");
                final String address2 = rs.getString("ADDRESS2");
                final String telno = rs.getString("TELNO");
                final String fsCd = rs.getString("FS_CD");
                final String finschoolName = rs.getString("FINSCHOOL_NAME");
                final String fsDay = rs.getString("FS_DAY");
                final String fsGrddiv = rs.getString("FS_GRDDIV");
                final String gKana = rs.getString("GKANA");
                final String gName = rs.getString("GNAME");

                // 改ページの制御
                if (lineCnt > maxLine) {
                    svf.VrEndPage();
                    lineCnt = 1;
                    pageCnt++;
                    setTitle(svf, pageCnt, outputDate);
                }

                svf.VrsOut("EXAM_NO1", examno);
                svf.VrsOut("KANA", nameKana);
                svf.VrsOut("NAME", name);
                svf.VrsOut("SEX", sex);
                svf.VrsOut("BIRTHDAY", KNJ_EditDate.h_format_thi(birthday, 0));
                svf.VrsOut("ZIP_NO", zipcd);

                String address = StringUtils.defaultString(address1);
                if (address2 != null) {
                    address += "　" + address2;
                }
                final int addressByte = KNJ_EditEdit.getMS932ByteLength(address);
                final String addressFieldStr = addressByte > 78 ? "2" : "1";
                svf.VrsOut("ADDR" + addressFieldStr, address);

                svf.VrsOut("TEL_NO", telno);
                svf.VrsOut("FINSCHOOL_CD", fsCd);
                svf.VrsOut("FINSCHOOL_NAME", finschoolName);

                String fsGrdDate;
                if (fsDay != null) {
                    fsGrdDate = KNJ_EditDate.h_format_thi(fsDay, 0).substring(0, fsDay.length() - 3) + "　" + StringUtils.defaultString(fsGrddiv);
                } else {
                    fsGrdDate = StringUtils.defaultString(fsGrddiv);
                }
                svf.VrsOut("GRD_DATE", fsGrdDate);

                svf.VrsOut("GUARD_KANA", gKana);
                svf.VrsOut("GUARD_NAME", gName);
                svf.VrEndRecord();

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
        final String titleStr = "1".equals(_param._applicantDiv) ? "中学部" : "2".equals(_param._applicantDiv) ? "高等部" : "";
        svf.VrsOut("TITLE", _param._entexamyear + "年度  志願者チェックリスト  出願者  （" + titleStr + "：" + _param._testDivName + "）");

        final String schoolNameStr = "3".equals(_param._sex) ? "男女共" : "1".equals(_param._sex) ? "男子のみ" : "2".equals(_param._sex) ? "女子のみ" : "";
        final String orderNameStr = "1".equals(_param._order) ? "受験番号順" : "2".equals(_param._order) ? "氏名カナ順" : "出身校コード順";
        svf.VrsOut("SCHOOL_NAME", _param._schoolName + "  （" + schoolNameStr + "）（" + orderNameStr + "）");
        svf.VrsOut("PAGE", pageNo + "頁");
        svf.VrsOut("DATE", "作成日時：" + outputDate);
    }

    private String sql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     BASE.EXAMNO, ");
        stb.append("     BASE.NAME_KANA, ");
        stb.append("     BASE.NAME, ");
        stb.append("     Z002.NAME2 AS SEX, ");
        stb.append("     BASE.BIRTHDAY, ");
        stb.append("     ADDR.ZIPCD, ");
        stb.append("     ADDR.ADDRESS1, ");
        stb.append("     ADDR.ADDRESS2,");
        stb.append("     ADDR.TELNO, ");
        stb.append("     BASE.FS_CD, ");
        stb.append("     FS.FINSCHOOL_NAME, ");
        stb.append("     BASE.FS_DAY, ");
        stb.append("     L016.NAME1 AS FS_GRDDIV, ");
        stb.append("     ADDR.GKANA, ");
        stb.append("     ADDR.GNAME ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_APPLICANTBASE_DAT BASE ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT ADDR ON ADDR.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
        stb.append("          AND ADDR.APPLICANTDIV = BASE.APPLICANTDIV ");
        stb.append("          AND ADDR.EXAMNO = BASE.EXAMNO ");
        stb.append("     LEFT JOIN V_NAME_MST Z002 ON Z002.NAMECD1 = 'Z002' ");
        stb.append("          AND BASE.ENTEXAMYEAR = Z002.YEAR ");
        stb.append("          AND BASE.SEX = Z002.NAMECD2 ");
        stb.append("     LEFT JOIN FINSCHOOL_MST FS ON BASE.FS_CD = FS.FINSCHOOLCD ");
        stb.append("     LEFT JOIN V_NAME_MST L007 ON L007.NAMECD1 = 'L007' ");
        stb.append("          AND BASE.ENTEXAMYEAR = L007.YEAR ");
        stb.append("          AND BASE.FS_ERACD = L007.NAMECD2 ");
        stb.append("     LEFT JOIN ENTEXAM_SETTING_MST L016 ON L016.SETTING_CD = 'L016' ");
        stb.append("          AND BASE.ENTEXAMYEAR = L016.ENTEXAMYEAR ");
        stb.append("          AND BASE.APPLICANTDIV = L016.APPLICANTDIV ");
        stb.append("          AND BASE.FS_GRDDIV = L016.SEQ ");
        stb.append(" WHERE ");
        stb.append("     BASE.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND BASE.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND BASE.TESTDIV = '" + _param._testDiv + "' ");
        /**
         * 抽出区分(SEX)は 3:全員、1:男子のみ、2:女子のみ となっており、
         * 1 ～ 3 のいずれかが渡ってくる。
         * 3:全員 以外が選択されたときのみ抽出条件に加える。
         */
        if (!"3".equals(_param._sex)) {
            stb.append("     AND BASE.SEX = '" + _param._sex + "' ");
        }
        stb.append("     AND VALUE(BASE.JUDGEMENT, '') <> '5' ");
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
        private final String _testDivName;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _schoolName = getSchoolName(db2);
            _sex = request.getParameter("SEX");
            _order = request.getParameter("ORDER");
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
            return StringUtils.defaultString(rtn);
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
            return StringUtils.defaultString(rtn);
        }
    }
}

// eof

