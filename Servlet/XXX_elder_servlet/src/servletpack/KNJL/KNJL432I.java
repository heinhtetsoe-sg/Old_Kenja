/*
 * $Id: c9870aacdf8da742317dc9f93b04d749f1994f7a $
 *
 * 作成日: 2020/09/16
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
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;

public class KNJL432I {

    private static final Log log = LogFactory.getLog(KNJL432I.class);

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
        svf.VrSetForm("KNJL432I.frm", 1);
        final List<PrintData> printList = getList(db2);

        final int maxLine = 65;
        final int maxCol = 3;
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

            svf.VrsOutn("RANK" + colCnt, lineCnt, printData._rank);
            svf.VrsOutn("EXAM_NO" + colCnt + "_1", lineCnt, printData._examNo);
            svf.VrsOutn("SEX" + colCnt, lineCnt, printData._sex);
            svf.VrsOutn("SCORE" + colCnt, lineCnt, printData._total1);
            /**
             * TODO JUDGEDIVが 4:欠席 のデータは取得しない仕様となっており、
             * 欠席をだすことができない。
             * 現状は”欠席”を印字する制御を省く。
             */
            // final String interview = "4".equals(printData._judgediv) ? "欠席" : StringUtils.defaultString(printData._interviewA) + StringUtils.defaultString(printData._interviewB) + StringUtils.defaultString(printData._interviewC);
            final String interview = StringUtils.defaultString(printData._interviewA) + StringUtils.defaultString(printData._interviewB) + StringUtils.defaultString(printData._interviewC);
            svf.VrsOutn("INTERVIEW" + colCnt, lineCnt, interview);
            if ("2".equals(_param._testDiv)) {
                svf.VrsOutn("DIV" + colCnt + "_1", lineCnt, printData._total1);
                final String minusDiv = (NumberUtils.isNumber(printData._total1) && Double.parseDouble(printData._total1) > 0.0) ? "〇" : ""; // 〇のみ
                svf.VrsOutn("DIV" + colCnt + "_2", lineCnt, minusDiv);
            } else {
                svf.VrsOutn("DIV" + colCnt + "_1", lineCnt, NumberUtils.isNumber(printData._plus) && Double.parseDouble(printData._plus) == 0.0 ? "" : printData._plus);
                final String minusDiv = (NumberUtils.isNumber(printData._minus) && Double.parseDouble(printData._minus) > 0.0) ? "●" : (NumberUtils.isNumber(printData._plus) && Double.parseDouble(printData._plus) > 0.0) ? "〇" : ""; // ●が優先
                svf.VrsOutn("DIV" + colCnt + "_2", lineCnt, minusDiv);
                svf.VrsOutn("DIV" + colCnt + "_3", lineCnt, (!NumberUtils.isNumber(printData._minus) || Double.parseDouble(printData._minus) == 0.0 ? "" : "-" + StringUtils.defaultString(printData._minus)));
            }
            if ("1".equals(printData._judgeKind)) {
                svf.VrsOutn("DIV" + colCnt + "_4", lineCnt, printData._judgeKindValue);
            } else if ("2".equals(printData._judgeKind) || "3".equals(printData._judgeKind)) {
                svf.VrsOutn("DIV" + colCnt + "_5", lineCnt, printData._judgeKindValue);
            }
            if ("2".equals(printData._remark)) {
                svf.VrsOutn("SECOND_HOPE" + colCnt, lineCnt, "2");
            }

            if ("1".equals(_param._testDiv) && null == printData._recomExamno) {
                svf.VrsOutn("EXAM_NO" + colCnt + "_2", lineCnt, printData._examno2);
            } else {
                svf.VrsOutn("EXAM_NO" + colCnt + "_2", lineCnt, printData._recomExamno);
            }

            lineCnt++;
            _hasData = true;
        }

        if (_hasData) {
            svf.VrEndPage();
        }
    }

    private void setTitle(final Vrw32alp svf, final int pageNo, final String outputDate) {
        svf.VrsOut("TITLE", _param._entexamyear + "年度 入学試験  " + _param._testDivName + "  序列表");

        final String output_div_str = "1".equals(_param._output_div) ? "男女共" : "2".equals(_param._output_div) ? "男子のみ" : "3".equals(_param._output_div) ? "女子のみ" : "";
        final String sort_div_str = "1".equals(_param._sort_div) ? "受験番号順" : "2".equals(_param._sort_div) ? "成績順" : "";
        svf.VrsOut("SUBTITLE", "（" + output_div_str + "）  （" + sort_div_str + "）");

        /**
         * TODO KNJL432I のSVFファイルには学校名を印字するフィールドが設けれているが、
         * KG-132 序列表 の設計書には学校名を出す仕様となっていないため、
         * 学校名は印字しないことにする。
         */
        // svf.VrsOut("SCHOOL_NAME", _param._schoolName);
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
                final String rank = rs.getString("RANK");
                final String receptno = rs.getString("RECEPTNO");
                final String sex = rs.getString("SEX");
                final String total1 = rs.getString("TOTAL1");
                /**
                 * TODO JUDGEDIVが 4:欠席 のデータは取得しない仕様となっており、
                 * 欠席をだすことができない。
                 * 現状は”欠席”を印字する制御を省く。
                 */
                // final String judgediv = rs.getString("JUDGEDIV");
                final String interviewA = rs.getString("INTERVIEW_A");
                final String interviewB = rs.getString("INTERVIEW_B");
                final String interviewC = rs.getString("INTERVIEW_C");
                final String plus = rs.getString("PLUS");
                final String minus = rs.getString("MINUS");
                final String judgeKind = rs.getString("JUDGE_KIND");
                final String judgeKindValue = rs.getString("JUDGE_KIND_VALUE");
                final String remark1 = rs.getString("REMARK1");
                final String recomExamno = rs.getString("RECOM_EXAMNO");
                final String examno2 = rs.getString("EXAMNO2");

                final PrintData printData = new PrintData(
                        rank,
                        receptno,
                        sex,
                        total1,
                        /**
                         * TODO JUDGEDIVが 4:欠席 のデータは取得しない仕様となっており、
                         * 欠席をだすことができない。
                         * 現状は”欠席”を印字する制御を省く。
                         */
                        // judgediv,
                        interviewA,
                        interviewB,
                        interviewC,
                        plus,
                        minus,
                        judgeKind,
                        judgeKindValue,
                        remark1,
                        recomExamno,
                        examno2);
                retList.add(printData);
            }

            return retList;
        } catch (SQLException ex) {
            log.error("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }

        return retList;
    }

    private String getSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        if ("2".equals(_param._testDiv)) {
            stb.append("     INT(CONFR_D.REMARK4) AS RANK, ");
        } else {
            if ("1".equals(_param._output_div)) {
                stb.append("     RECEPT.TOTAL_RANK1 AS RANK, ");
            } else {
                stb.append("     RECEPT.SEX_RANK1 AS RANK, ");
            }
        }
        stb.append("     RECEPT.RECEPTNO, ");
        stb.append("     Z002.NAME2 AS SEX, ");
        if ("2".equals(_param._testDiv)) {
            stb.append("     CONFR_D.REMARK3 AS TOTAL1, ");
        } else {
            stb.append("     RECEPT.TOTAL1, ");
        }
        /**
         * TODO JUDGEDIVが 4:欠席 のデータは取得しない仕様となっており、
         * 欠席をだすことができない。
         * 現状は”欠席”を印字する制御を省く。
         */
        // stb.append("     RECEPT.JUDGEDIV, ");
        stb.append("     LH27_A.NAME1 AS INTERVIEW_A, ");
        stb.append("     LH27_B.NAME1 AS INTERVIEW_B, ");
        stb.append("     LH27_C.NAME1 AS INTERVIEW_C, ");
        stb.append("     CONFR_D.REMARK1 AS PLUS, ");
        stb.append("     CONFR_D.REMARK2 AS MINUS, ");
        stb.append("     BASE.JUDGE_KIND, ");
        stb.append("     L025.ABBV1 AS JUDGE_KIND_VALUE, ");
        stb.append("     BASE_D.REMARK1, ");
        stb.append("     BASE.RECOM_EXAMNO, ");
        stb.append("     L2.EXAMNO2 ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_RECEPT_DAT RECEPT ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DAT BASE ON BASE.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR ");
        stb.append("          AND BASE.APPLICANTDIV = RECEPT.APPLICANTDIV ");
        stb.append("          AND BASE.TESTDIV = RECEPT.TESTDIV ");
        stb.append("          AND BASE.EXAMNO = RECEPT.EXAMNO ");
        stb.append("     LEFT JOIN (SELECT ");
        stb.append("                    RECOM_EXAMNO AS RECOM_EXAMNO2 ");
        stb.append("                  , MAX(EXAMNO) AS EXAMNO2 ");
        stb.append("                FROM ENTEXAM_APPLICANTBASE_DAT B1 ");
        stb.append("                WHERE B1.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("                  AND B1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("                  AND B1.TESTDIV = '2' ");
        stb.append("                  AND B1.RECOM_EXAMNO IS NOT NULL ");
        stb.append("                GROUP BY ");
        stb.append("                  B1.RECOM_EXAMNO ");
        stb.append("     ) L2 ON L2.RECOM_EXAMNO2 = BASE.EXAMNO ");
        stb.append("     LEFT JOIN V_NAME_MST Z002 ON Z002.NAMECD1 = 'Z002' ");
        stb.append("          AND BASE.ENTEXAMYEAR = Z002.YEAR ");
        stb.append("          AND BASE.SEX = Z002.NAMECD2 ");
        stb.append("     LEFT JOIN ENTEXAM_INTERVIEW_DAT INTERVIEW ON INTERVIEW.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR ");
        stb.append("          AND INTERVIEW.APPLICANTDIV = RECEPT.APPLICANTDIV ");
        stb.append("          AND INTERVIEW.TESTDIV = RECEPT.TESTDIV ");
        stb.append("          AND INTERVIEW.EXAMNO = RECEPT.EXAMNO ");
        stb.append("     LEFT JOIN ENTEXAM_SETTING_MST LH27_A ON LH27_A.SETTING_CD = 'LH27' ");
        stb.append("          AND INTERVIEW.ENTEXAMYEAR = LH27_A.ENTEXAMYEAR ");
        stb.append("          AND INTERVIEW.APPLICANTDIV = LH27_A.APPLICANTDIV ");
        stb.append("          AND INTERVIEW.INTERVIEW_A = LH27_A.SEQ ");
        stb.append("     LEFT JOIN ENTEXAM_SETTING_MST LH27_B ON LH27_B.SETTING_CD = 'LH27' ");
        stb.append("          AND INTERVIEW.ENTEXAMYEAR = LH27_B.ENTEXAMYEAR ");
        stb.append("          AND INTERVIEW.APPLICANTDIV = LH27_B.APPLICANTDIV ");
        stb.append("          AND INTERVIEW.INTERVIEW_B = LH27_B.SEQ ");
        stb.append("     LEFT JOIN ENTEXAM_SETTING_MST LH27_C ON LH27_C.SETTING_CD = 'LH27' ");
        stb.append("          AND INTERVIEW.ENTEXAMYEAR = LH27_C.ENTEXAMYEAR ");
        stb.append("          AND INTERVIEW.APPLICANTDIV = LH27_C.APPLICANTDIV ");
        stb.append("          AND INTERVIEW.INTERVIEW_C = LH27_C.SEQ ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT CONFR_D ON CONFR_D.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR ");
        stb.append("          AND CONFR_D.APPLICANTDIV = RECEPT.APPLICANTDIV ");
        stb.append("          AND CONFR_D.EXAMNO = RECEPT.EXAMNO ");
        stb.append("          AND CONFR_D.SEQ = '003' ");
        stb.append("     LEFT JOIN ENTEXAM_SETTING_MST L025 ON L025.SETTING_CD = 'L025' ");
        stb.append("          AND BASE.ENTEXAMYEAR = L025.ENTEXAMYEAR ");
        stb.append("          AND BASE.APPLICANTDIV = L025.APPLICANTDIV ");
        stb.append("          AND BASE.JUDGE_KIND = L025.SEQ ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BASE_D ON BASE_D.ENTEXAMYEAR = BASE.ENTEXAMYEAR ");
        stb.append("          AND BASE_D.APPLICANTDIV = BASE.APPLICANTDIV ");
        stb.append("          AND BASE_D.EXAMNO = BASE.EXAMNO ");
        stb.append("          AND BASE_D.SEQ = '005' ");
        stb.append(" WHERE ");
        stb.append("     RECEPT.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND RECEPT.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND RECEPT.TESTDIV = '" + _param._testDiv + "' ");
        /**
         * 抽出区分(OUTPUT_DIV)は 1:全員、2:男子のみ、3:女子のみ となっており、
         * 1 ～ 3 のいずれかが渡ってくる。
         * 1:全員 以外が選択されたときのみ抽出条件に加える。
         */
        if ("2".equals(_param._output_div)) {
            stb.append("     AND BASE.SEX = '1' ");
        } else if ("3".equals(_param._output_div)) {
            stb.append("     AND BASE.SEX = '2' ");
        }
        stb.append("     AND ( ");
        stb.append("             RECEPT.JUDGEDIV <> '4' ");
        stb.append("             OR RECEPT.JUDGEDIV IS NULL ");
        stb.append("         ) ");
        stb.append(" ORDER BY ");
        /**
         * 並び順(SORT_DIV)は 1:受験番号順、2:総合点順 となっており、
         * 1 ～ 2 のいずれかが渡ってくる。
         * 渡ってきた値に該当する項目でソートする。
         */
        if ("2".equals(_param._sort_div)) {
            /**
             *  2:総合点順の場合は SELECT句の RANK(※) でソートする。
             *
             *  ※
             *  SELECT句の RANK は別名であり、抽出区分(OUTPUT_DIV) の値により扱う項目が変わる。
             *　抽出区分が 1:全員 の場合は TOTAL_RANNK1 の別名。
             *　抽出区分が 1:全員 以外の場合は SEX_RANNK1 の別名。
             */
            stb.append("     RANK, ");
        }
        stb.append("     RECEPT.RECEPTNO ");
        return stb.toString();
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: 77039 $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    private class PrintData {
        final String _rank;
        final String _examNo;
        final String _sex;
        final String _total1;
        /**
         * TODO JUDGEDIVが 4:欠席 のデータは取得しない仕様となっており、
         * 欠席をだすことができない。
         * 現状は”欠席”を印字する制御を省く。
         */
        // final String _judgediv;
        final String _interviewA;
        final String _interviewB;
        final String _interviewC;
        final String _plus;
        final String _minus;
        final String _judgeKind;
        final String _judgeKindValue;
        final String _remark;
        final String _recomExamno;
        final String _examno2;

        public PrintData(
                final String rank,
                final String examNo,
                final String sex,
                final String total1,
                /**
                 * TODO JUDGEDIVが 4:欠席 のデータは取得しない仕様となっており、
                 * 欠席をだすことができない。
                 * 現状は”欠席”を印字する制御を省く。
                 */
                // final String judgediv,
                final String interviewA,
                final String interviewB,
                final String interviewC,
                final String plus,
                final String minus,
                final String judgeKind,
                final String judgeKindValue,
                final String remark,
                final String recomExamno,
                final String examno2
        ) {
            _rank = rank;
            _examNo = examNo;
            _sex = sex;
            _total1 = total1;
            /**
             * TODO JUDGEDIVが 4:欠席 のデータは取得しない仕様となっており、
             * 欠席をだすことができない。
             * 現状は”欠席”を印字する制御を省く。
             */
            // _judgediv = judgediv;
            _interviewA = interviewA;
            _interviewB = interviewB;
            _interviewC= interviewC;
            _plus = plus;
            _minus = minus;
            _judgeKind = judgeKind;
            _judgeKindValue = judgeKindValue;
            _remark = remark;
            _recomExamno = recomExamno;
            _examno2 = examno2;
        }
    }

    /** パラメータクラス */
    private static class Param {
        private final String _entexamyear;
        private final String _applicantDiv;
        private final String _testDiv;
        private final String _output_div;
        private final String _sort_div;
        /**
         * TODO KNJL432I のSVFファイルには学校名を印字するフィールドが設けれているが、
         * KG-132 序列表 の設計書には学校名を出す仕様となっていないため、
         * 学校名は印字しないことにする。
         */
        // private final String _schoolName;
        private final String _testDivName;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _output_div = request.getParameter("OUTPUT_DIV");
            _sort_div = request.getParameter("SORT_DIV");
            /**
             * TODO KNJL432I のSVFファイルには学校名を印字するフィールドが設けれているが、
             * KG-132 序列表 の設計書には学校名を出す仕様となっていないため、
             * 学校名は印字しないことにする。
             */
            // _schoolName = getSchoolName(db2);
            _testDivName = getTestDivName(db2);
        }

        /**
         * TODO KNJL432I のSVFファイルには学校名を印字するフィールドが設けれているが、
         * KG-132 序列表 の設計書には学校名を出す仕様となっていないため、
         * 学校名は印字しないことにする。
         */
        /**
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
        */

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

