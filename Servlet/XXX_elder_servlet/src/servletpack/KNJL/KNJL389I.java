/*
 * 作成日: 2020/09/15
 * 作成者: shimoji
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

public class KNJL389I {

    private static final Log log = LogFactory.getLog(KNJL389I.class);

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
        svf.VrSetForm("KNJL389I.frm", 1);
        final List<PrintData> printList = getList(db2);

        final int maxLine = 50;
        final int maxCol = 6;
        int lineCnt = 1;
        int colCnt = 1;
        int pageCnt = 1;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String outputDate = sdf.format(new Date());

        setTitle(svf, pageCnt, outputDate);

        for (PrintData printData : printList) {
            // 改列の制御
            if (lineCnt > maxLine) {
                lineCnt = 1;

                // 改ページの制御
                if (colCnt >= maxCol) {
                    svf.VrEndPage();
                    colCnt = 1;
                    pageCnt++;
                    setTitle(svf, pageCnt, outputDate);
                } else {
                    colCnt++;
                }
            }

            svf.VrsOutn("EXAM_NO" + colCnt, lineCnt, printData._examNo);

            // JUDGEMENTが 4:欠席 か ATTEND_FLGが 1:欠席 の場合は欠席を印字する。
            String score = ("4".equals(printData._judgement) || "1".equals(printData._attendFlg)) ? "欠席" : printData._score;
            svf.VrsOutn("SCORE" + colCnt, lineCnt, score);

            lineCnt++;
            _hasData = true;
        }

        if (_hasData) {
            svf.VrEndPage();
        }
    }

    private void setTitle(final Vrw32alp svf, final int pageNo, final String outputDate) {
        svf.VrsOut("TITLE", _param._entexamyear + "年度 入学試験  " + _param._testDivName + "  科目別得点確認票");

        final String subTitleStr = "3".equals(_param._sex) ? "男女共" : "1".equals(_param._sex) ? "男子のみ" : "2".equals(_param._sex) ? "女子のみ" : "";
        final String orderNameStr = "1".equals(_param._order) ? "受験番号順" : "得点順";
        svf.VrsOut("SUBTITLE", "【" + StringUtils.defaultString(_param._testSubClassName) + "】 （" + subTitleStr + "）（" + orderNameStr + "）");

        svf.VrsOut("SCHOOL_NAME", _param._schoolName);
        svf.VrsOut("PAGE", pageNo + "頁");
        svf.VrsOut("DATE", outputDate);
    }

    private List<PrintData> getList(final DB2UDB db2) {
        final List<PrintData> retList = new ArrayList<PrintData>();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final String sql = getSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String examNo = rs.getString("EXAMNO");
                final String score = rs.getString("SCORE");
                final String judgement = rs.getString("JUDGEMENT");
                final String attendFlg = rs.getString("ATTEND_FLG");

                final PrintData printData = new PrintData(examNo, score, judgement, attendFlg);
                retList.add(printData);
            }

            return retList;
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
        stb.append(" SELECT  ");
        stb.append("     BASE.EXAMNO, ");
        stb.append("     BASE.JUDGEMENT, ");
        stb.append("     SCORE.TESTSUBCLASSCD, ");
        stb.append("     CASE WHEN BASE.JUDGEMENT = '4' OR SCORE.ATTEND_FLG = '1' THEN -1 ELSE SCORE.SCORE END AS SCORE, ");
        stb.append("     SCORE.ATTEND_FLG ");
        stb.append(" FROM  ");
        stb.append("     ENTEXAM_APPLICANTBASE_DAT BASE ");
        stb.append("     LEFT JOIN ENTEXAM_SCORE_DAT SCORE ON SCORE.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
        stb.append("          AND SCORE.APPLICANTDIV = BASE.APPLICANTDIV ");
        stb.append("          AND SCORE.TESTDIV = BASE.TESTDIV ");
        stb.append("          AND SCORE.RECEPTNO = BASE.EXAMNO ");
        stb.append("          AND SCORE.EXAM_TYPE = '1' ");
        stb.append("          AND SCORE.TESTSUBCLASSCD = '" + _param._testSubClassCd + "' ");
        stb.append(" WHERE ");
        stb.append("     BASE.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND BASE.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND BASE.TESTDIV = '" + _param._testDiv + "' ");
        stb.append("     AND VALUE(BASE.JUDGEMENT, '') <> '5' "); //B日程志願者のうちA日程または帰国生入試で既に合格している場合は除外
        /**
         * 抽出区分(SEX)は 3:全員、1:男子のみ、2:女子のみ となっており、
         * 1 ～ 3 のいずれかが渡ってくる。
         * 3:全員 以外が選択されたときのみ抽出条件に加える。
         */
        if (!"3".equals(_param._sex)) {
            stb.append("     AND BASE.SEX = '" + _param._sex + "' ");
        }
        stb.append(" ORDER BY ");
        /**
         * 並び順(ORDER)は 1:受験番号順、2:得点順 となっており、
         * 1 ～ 2 のいずれかが渡ってくる。
         * 渡ってきた値に該当する項目でソートする。
         */
        if ("2".equals(_param._order)) {
            stb.append("     SCORE DESC, ");
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

    private class PrintData {
        final String _examNo;
        final String _score;
        final String _judgement;
        final String _attendFlg;

        public PrintData(
                final String examNo,
                final String score,
                final String judgement,
                final String attendFlg
        ) {
            _examNo = examNo;
            _score = score;
            _judgement = judgement;
            _attendFlg = attendFlg;
        }
    }

    /** パラメータクラス */
    private static class Param {
        private final String _entexamyear;
        private final String _applicantDiv;
        private final String _testDiv;
        private final String _testSubClassCd;
        private final String _sex;
        private final String _order;
        private final String _schoolName;
        private final String _testDivName;
        private final String _testSubClassName;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _testSubClassCd = request.getParameter("OUTPUT_SUBJECT");
            _sex = request.getParameter("SEX");
            _order = request.getParameter("ORDER");
            _schoolName = getSchoolName(db2);
            _testDivName = getTestDivName(db2);
            _testSubClassName = getTestSubClassName(db2);
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

        private String getTestSubClassName(final DB2UDB db2) {
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;

            String sql = getTestSubClassNameSql();
            log.debug(" sql =" + sql);

            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString("TESTSUBCLASSNAME");
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtn;
        }

        private String getTestSubClassNameSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("    L009.NAME1 AS TESTSUBCLASSNAME ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_TESTSUBCLASSCD_DAT SUBCLASS ");
            stb.append("     LEFT JOIN ENTEXAM_SETTING_MST L009 ON L009.SETTING_CD = 'L009' ");
            stb.append("          AND L009.ENTEXAMYEAR = SUBCLASS.ENTEXAMYEAR ");
            stb.append("          AND L009.APPLICANTDIV = SUBCLASS.APPLICANTDIV ");
            stb.append("          AND L009.SEQ = SUBCLASS.TESTSUBCLASSCD ");
            stb.append(" WHERE ");
            stb.append("     SUBCLASS.ENTEXAMYEAR = '" + _entexamyear + "' ");
            stb.append("     AND SUBCLASS.APPLICANTDIV = '" + _applicantDiv + "' ");
            stb.append("     AND SUBCLASS.TESTDIV = '" + _testDiv + "' ");
            stb.append("     AND SUBCLASS.EXAM_TYPE = '1' ");
            stb.append("     AND SUBCLASS.TESTSUBCLASSCD = '" + _testSubClassCd + "' ");
            return stb.toString();
        }
    }
}

// eof

