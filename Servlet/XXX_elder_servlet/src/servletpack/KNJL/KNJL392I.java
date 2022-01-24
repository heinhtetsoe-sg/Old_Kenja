/*
 * 作成日: 2020/09/08
 * 作成者: shimoji
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;


import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;

public class KNJL392I {

    private static final Log log = LogFactory.getLog(KNJL392I.class);

    private static final int MAX_LINE = 26;
    private static final String FROM_TO_MARK = "\uFF5E";

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
        if ("2".equals(_param._applicantDiv)) {
            if ("2".equals(_param._testDiv) || "3".equals(_param._testDiv)) {
                //高校入試ならA方式、帰国生は点数入力無しなので、出力しない。
                return;
            }
        } else {
            if ("3".equals(_param._testDiv)) {
                //中学入試なら帰国生は点数入力無しなので、出力しない。
                return;
            }
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        String outputDate = sdf.format(new Date());

        List tblDat = getTblInfo(db2);
        svf.VrSetForm("KNJL392I.frm", 1);
        int tCount = setTitle(svf, tblDat, outputDate);
        if (tCount > 0) {
            _hasData = true;
            final int dataFullPt = ("ALL".equals(_param._outputSubject)) ? 500 : 200; //満点。20 又は 5 で割り切れる値でないとNG。
            final int datsp = ("ALL".equals(_param._outputSubject)) ? 20 : 5; //刻み点。20点刻み(総合計) or 5点刻み(教科別)
            Map graphDat = new LinkedMap();
            getScoreInfo(db2, graphDat, dataFullPt, datsp);

            int maxListCnt = 0;
            int maxSearchIdx;
            for (maxSearchIdx = 0; maxSearchIdx <= dataFullPt / datsp; maxSearchIdx++) {
                if (graphDat.containsKey(String.valueOf(maxSearchIdx))) {
                    List cntLst = (List)graphDat.get(String.valueOf(maxSearchIdx));
                    if (maxListCnt < cntLst.size()) {
                        maxListCnt = cntLst.size();
                    }
                }
            }
            final String barName = maxListCnt > 120 ? "180" : maxListCnt > 60 ? "120" : "60";

            String lowScore = String.valueOf(dataFullPt);
            String highScore = "";
            int idx;
            int line = 1;
            for (idx = 0; idx <= dataFullPt / datsp;idx++) {

                if(line > MAX_LINE && !"ALL".equals(_param._outputSubject)) {
                    //改ページ処理
                    line = 1;
                    svf.VrEndPage();
                    svf.VrSetForm("KNJL392I.frm", 1);
                    setTitle(svf, tblDat, outputDate);
                }

                //得点区間
                svf.VrsOutn("SCORE1", line, lowScore); //下限
                svf.VrsOutn("WAVE", line, (Integer.parseInt(lowScore)) == dataFullPt ? "" : FROM_TO_MARK);
                svf.VrsOutn("SCORE2", line, highScore); //上限

                //人数・グラフ
                if (graphDat.containsKey(String.valueOf(idx))) {
                    List cntLst = (List)graphDat.get(String.valueOf(idx));
                    svf.VrsOutn("NUM", line, String.valueOf(cntLst.size()));
                    svf.VrAttributen("BAR" + barName, line, "Paint=(0,0,1),Keta=" + String.valueOf(cntLst.size())); // ヒストグラム
                }

                highScore = (Integer.parseInt(lowScore)) == dataFullPt ? String.valueOf(dataFullPt - 1) : String.valueOf((Integer.parseInt(highScore)) - datsp);
                lowScore = String.valueOf((Integer.parseInt(lowScore)) - datsp);
                line++;
            }

            //欄外
            line = MAX_LINE + 1;
            svf.VrsOutn("SCORE1", line, "");
            svf.VrsOutn("WAVE", line, "");
            svf.VrsOutn("SCORE2", line, "欠席者");
            int absCnt = getAbsentInfo(db2);
            svf.VrsOutn("NUM", line, String.valueOf(absCnt));
            svf.VrEndPage();
        }

    }

    private List getTblInfo(final DB2UDB db2) {
        List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            final String sql = sql1();
            //log.info(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String dtype = rs.getString("DTYPE");
                final String avg = rs.getString("AVG");
                final String mx_Score = rs.getString("MX_SCORE");
                final String mn_Score = rs.getString("MN_SCORE");
                final String cnt = rs.getString("CNT");
                PrintData1 addwk = new PrintData1(dtype, avg, mx_Score, mn_Score, cnt);
                retList.add(addwk);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
        return retList;
    }

    private int setTitle(final Vrw32alp svf, List tblDat, final String outputDate) {
        final String putSubjName = "ALL".equals(_param._outputSubject) ? "総合計" : _param._subjName;
        svf.VrsOut("TITLE", _param._entexamyear + "年度 " + _param._testDivName + " 得点分布表 (" + putSubjName + ")");
        svf.VrsOut("SCHOOL_NAME", _param._schoolName);
        final String selDivStr1 = "3".equals(_param._sex) ? "全員" : "1".equals(_param._sex) ? "男子のみ" : "2".equals(_param._sex) ? "女子のみ" : "";
        svf.VrsOut("SELECT_DIV1", selDivStr1);
        final String selDivStr2 = "1".equals(_param._passFilter) ? "全員" : "2".equals(_param._passFilter) ? "合格者のみ" : "3".equals(_param._passFilter) ? "不合格者のみ" : "";
        svf.VrsOut("JUDGE_DIV", selDivStr2);
        svf.VrsOut("SELECT_DIV2", "全員");
        svf.VrsOut("DATE", outputDate);

        int tCount = 0;
        int lineCnt = 1;
        for (Iterator ite = tblDat.iterator();ite.hasNext();) {
            PrintData1 prtObj = (PrintData1)ite.next();
            tCount += Integer.parseInt(prtObj._cnt);
            if (!"1".equals(_param._passFilter)) {
                if (!prtObj._dtype.equals(_param._passFilter)) {
                    lineCnt++;
                    continue;
                }
            }
            svf.VrsOutn("AVE", lineCnt, prtObj._avg);
            svf.VrsOutn("MAX", lineCnt, prtObj._mx_Score);
            svf.VrsOutn("MIN", lineCnt, prtObj._mn_Score);
            lineCnt++;
        }
        return tCount;
    }

    private String sql1() {
        final StringBuffer stb = new StringBuffer();
        //全体の捉え方：JUDGEMENTで欠席(4)以外の人。これにSCORE_DATを紐づけて出す。SCOREが紐づかない人は平均算出から除外?0点としてカウント?。グラフの欠席者は欠席者で別途取得。
        //この全体はグラフでも前提となる？ =>前提となる。
        //満点マスタの紐づけはどーする？
        stb.append(" WITH RECEPT_BDAT AS ( ");

        stb.append(sql2(false));

        stb.append(" ) ");

        //共通部分
        StringBuffer stbwk = new StringBuffer();
        stbwk.append("   CASE WHEN COUNT(T1.EXAMNO) = 0 THEN 0.0 ELSE DECIMAL(INT( ((SUM(VALUE(T1.SCORE, 0)) * 1.0 / COUNT(T1.EXAMNO) * 1.0)*100.0) + 0.5 )/100.0, 6,2) END AS AVG, ");
        stbwk.append("   CASE WHEN COUNT(T1.EXAMNO) = 0 THEN 0 ELSE MAX(T1.SCORE) END AS MX_SCORE, ");
        stbwk.append("   CASE WHEN COUNT(T1.EXAMNO) = 0 THEN 0 ELSE MIN(T1.SCORE) END AS MN_SCORE, ");
        stbwk.append("   COUNT(T1.EXAMNO) AS CNT ");
        stbwk.append(" FROM ");
        stbwk.append("   RECEPT_BDAT T1 ");
        stbwk.append(" WHERE ");
        stbwk.append("   T1.SCORE IS NOT NULL ");  //成績が入っていない人はカウントしない。

        // 全員
        stb.append(" SELECT ");
        stb.append("   '1' AS DTYPE, ");

        stb.append(stbwk);

        stb.append(" UNION ");

        // 合格者のみ
        stb.append(" SELECT ");
        stb.append("   '2' AS DTYPE, ");

        stb.append(stbwk);

        stb.append("   AND T1.JUDGEMENT = '1' ");  //合格
        stb.append(" UNION ");
        // 不合格者のみ
        stb.append(" SELECT ");
        stb.append("   '3' AS DTYPE, ");

        stb.append(stbwk);

        stb.append("   AND T1.JUDGEMENT = '2' ");  //不合格
        stb.append(" ORDER BY ");
        stb.append("  DTYPE ");
        return stb.toString();
    }

    private void getScoreInfo(final DB2UDB db2, final Map retMap, final int dataFullPt, final int datsp) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        List subLst = null;

        try {
            final String sql = sql2(false);
            //log.info(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String examno = rs.getString("EXAMNO");
                final String judgement = rs.getString("JUDGEMENT");
                final String receptno = rs.getString("RECEPTNO");
                final String score = rs.getString("SCORE");
                PrintData2 addwk = new PrintData2(examno, judgement, receptno, score);
                final String setIdx = String.valueOf(new BigDecimal((dataFullPt - Integer.parseInt(score)) * 1.0 / datsp * 1.0).setScale(0, BigDecimal.ROUND_CEILING).intValue());
                if (!retMap.containsKey(setIdx)) {
                    subLst = new ArrayList();
                    retMap.put(setIdx,subLst);
                }
                subLst = (List)retMap.get(setIdx);
                subLst.add(addwk);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
    }

    private int getAbsentInfo(final DB2UDB db2) {
        int retCnt = 0;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = sql2(true);
            //log.info(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            if (rs.next()) {
                retCnt = rs.getInt("CNT");
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }
        return retCnt;
    }
    private String sql2(final boolean getAbsentCountOnly) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        if (getAbsentCountOnly) {
            stb.append("     count(T2.EXAMNO) AS CNT ");
        } else {
            stb.append("     T2.EXAMNO, ");
            stb.append("     T1.JUDGEMENT, ");
            stb.append("     T2.RECEPTNO, ");
            if (!"ALL".equals(_param._outputSubject)) {
                stb.append("     T3.SCORE ");
            } else {
                stb.append("     T2.TOTAL1 AS SCORE ");
            }
        }
        stb.append(" FROM ");
        stb.append("     ENTEXAM_APPLICANTBASE_DAT T1 ");
        stb.append("     LEFT JOIN ENTEXAM_RECEPT_DAT T2 ");
        stb.append("       ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("      AND T2.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("      AND T2.EXAMNO = T1.EXAMNO ");
        if (!"ALL".equals(_param._outputSubject)) {
            stb.append("     LEFT JOIN ENTEXAM_SCORE_DAT T3 ");
            stb.append("       ON T3.ENTEXAMYEAR = T2.ENTEXAMYEAR ");
            stb.append("      AND T3.APPLICANTDIV = T2.APPLICANTDIV ");
            stb.append("      AND T3.TESTDIV = T2.TESTDIV ");
            stb.append("      AND T3.EXAM_TYPE = T2.EXAM_TYPE ");
            stb.append("      AND T3.RECEPTNO = T2.RECEPTNO ");
        }
        stb.append(" WHERE ");
        stb.append("     T1.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND T1.APPLICANTDIV = '" + _param._applicantDiv + "' ");
        stb.append("     AND T1.TESTDIV = '" + _param._testDiv + "' ");
        stb.append("     AND VALUE(T1.JUDGEMENT, '') <> '5' "); //5:未受験  B日程志願者のうちA日程または帰国生入試で既に合格している場合は除外
        if (!"ALL".equals(_param._outputSubject)) {
            stb.append("     AND T3.TESTSUBCLASSCD = '" + _param._outputSubject + "' ");
        }
        if (!"3".equals(_param._sex)) {
            stb.append("     AND T1.SEX = '" + _param._sex + "' ");
        }
        if (getAbsentCountOnly) {
            stb.append("     AND T1.JUDGEMENT = '4' ");  //欠席者以外
        } else {
            stb.append("     AND (T1.JUDGEMENT <> '4' OR T1.JUDGEMENT IS NULL) ");  //欠席者以外
            if (!"ALL".equals(_param._outputSubject)) {
                stb.append("     AND T3.SCORE IS NOT NULL ");  //欠席者以外(=出席者=成績がある人)だと、NOTNULL制限しないとグラフに影響が出る。→欠席じゃないけどNULLな人はグラフ対象外。
            } else {
                stb.append("     AND T2.TOTAL1 IS NOT NULL ");  //欠席者以外(=出席者=成績がある人)だと、NOTNULL制限しないと、グラフに影響が出る。。→欠席じゃないけどNULLな人はグラフ対象外。
            }
        }
        return stb.toString();
    }

    private class PrintData1 {
        final String _dtype;
        final String _avg;
        final String _mx_Score;
        final String _mn_Score;
        final String _cnt;
        public PrintData1 (final String dtype, final String avg, final String mx_Score, final String mn_Score, final String cnt)
        {
            _dtype = dtype;
            _avg = avg;
            _mx_Score = mx_Score;
            _mn_Score = mn_Score;
            _cnt = cnt;
        }
    }
    private class PrintData2 {
        final String _examno;
        final String _judgement;
        final String _receptno;
        final String _score;
        public PrintData2 (final String examno, final String judgement, final String receptno, final String score)
        {
            _examno = examno;
            _judgement = judgement;
            _receptno = receptno;
            _score = score;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        private final String _loginYear;
        private final String _entexamyear;
        private final String _applicantDiv;
        private final String _testDiv;
        private final String _schoolName;
        private final String _sex;
        private final String _passFilter;
        private final String _testDivName;
        private final String _outputSubject;
        private final String _subjName;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _loginYear = request.getParameter("LOGIN_YEAR");
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _schoolName = getSchoolName(db2);
            _sex = request.getParameter("SEX");
            _passFilter = request.getParameter("PASSFILTER");
            _testDivName = getTestDivName(db2);
            _outputSubject = request.getParameter("OUTPUT_SUBJECT");
            _subjName = getSubjectName(db2);
        }

        private String getSchoolName(final DB2UDB db2) {
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;

            // 入試制度（APPLICANTDIV） が 1:中学、2:高校 の場合のみ名称を取得する。
            String sqlwk = " SELECT SCHOOLNAME1 FROM SCHOOL_MST WHERE YEAR = '" + _loginYear + "' AND SCHOOLCD = '000000000000'";
            String sql = "1".equals(_applicantDiv) ? (sqlwk + " AND SCHOOL_KIND = 'J' ") : "2".equals(_applicantDiv) ? (sqlwk + " AND SCHOOL_KIND = 'H' ") : "";
            log.debug(" sql =" + sql);

            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString("SCHOOLNAME1");
                }
            } catch (Exception e) {
                log.error("exception!", e);
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
            }
            return rtn;
        }
        private String getSubjectName(final DB2UDB db2) {
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;

            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     coalesce(N1.NAME1, '') AS SUBJNAME ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_TESTSUBCLASSCD_DAT T1 ");
            stb.append("     LEFT JOIN ENTEXAM_SETTING_MST N1 ON N1.ENTEXAMYEAR = T1.ENTEXAMYEAR AND N1.APPLICANTDIV = T1.APPLICANTDIV AND N1.SETTING_CD = 'L009' AND N1.SEQ = T1.TESTSUBCLASSCD ");
            stb.append(" WHERE ");
            stb.append("     T1.ENTEXAMYEAR = '" + _entexamyear + "' ");
            stb.append("     AND T1.APPLICANTDIV = '" + _applicantDiv + "' ");
            stb.append("     AND T1.TESTDIV = '" + _testDiv + "' ");
            stb.append("     AND T1.EXAM_TYPE = '1' ");
            if (!"ALL".equals(_outputSubject)) {
                stb.append("     AND T1.TESTSUBCLASSCD = '" + _outputSubject + "' ");
            }

            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString("SUBJNAME");
                }
            } catch (Exception e) {
                log.error("exception!", e);
            }
            return rtn;
        }
    }
}

// eof

