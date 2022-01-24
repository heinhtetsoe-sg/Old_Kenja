/*
 * $Id: 35587630182f453c73f3495da74da159d23d6192 $
 *
 * 作成日: 2020/10/30
 * 作成者: shimoji
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJL;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;

public class KNJL692H {

    private static final Log log = LogFactory.getLog(KNJL692H.class);

    private boolean _hasData;

    private Param _param;

    private static final int ZYUKENSYA_MEIBO_LINE_MAX = 25;

    private static final int NYUSHI_SEISEKI_ICHIRANHYO_LINE_MAX = 50;

    private static final int GOKAKUSYA_ICHIRAN_COL_MAX = 4;
    private static final int GOKAKUSYA_ICHIRAN_LINE_MAX = 16;

    private static final String TESTSUBCLASSCD_KOKUGO = "1"; // 国語の科目コード
    private static final String TESTSUBCLASSCD_SUGAKU = "2"; // 数学の科目コード
    private static final String TESTSUBCLASSCD_EIGO = "3"; // 英語の科目コード

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
        Date now = new Date();

        if ("1".equals(_param._output)) {
            printZyukensyaMeibo(db2, svf, now);
        } else if ("2".equals(_param._output)) {
            printNyushiSeisekiIchiranhyou(db2, svf, now);
        } else if ("3".equals(_param._output)) {
            printGokakusyaIchiran(db2, svf);
        } else if ("4".equals(_param._output)) {
            printGoukakuTsuchisyo(db2, svf);
        }

        if (_hasData) {
            svf.VrEndPage();
        }
    }

    /**
     * 受験者名簿を出力する。
     *
     * @param db2
     * @param svf
     * @param date 現在日時
     */
    private void printZyukensyaMeibo(final DB2UDB db2, final Vrw32alp svf, Date date) {
        svf.VrSetForm("KNJL692H_1.frm", 1);

        int lineCnt = 1; // 書き込み行数
        int pageCnt = 1; // 書き込んだページ数

        List<Zyukensya> zyukensyaList = getZyukensyaMeibo(db2);

        setZyukensyaMeiboTitle(svf, pageCnt, date);

        for(Zyukensya zyukensya : zyukensyaList) {
            // 改ページの制御
            if (lineCnt > ZYUKENSYA_MEIBO_LINE_MAX) {
                lineCnt = 1;
                pageCnt++;
                svf.VrEndPage();

                setZyukensyaMeiboTitle(svf, pageCnt, date);
            }

            svf.VrsOutn("EXAM_NO", lineCnt, zyukensya._receptNo);

            final int nameByte = KNJ_EditEdit.getMS932ByteLength(zyukensya._name);
            final String nameFieldStr = nameByte > 30 ? "3" : nameByte > 20 ? "2" : "1";
            svf.VrsOutn("NAME" + nameFieldStr, lineCnt, zyukensya._name);

            final int kanaByte = KNJ_EditEdit.getMS932ByteLength(zyukensya._nameKana);
            final String kanaFieldStr = kanaByte > 30 ? "2" : "1";
            svf.VrsOutn("KANA" + kanaFieldStr, lineCnt, zyukensya._nameKana);

            svf.VrsOutn("SEX", lineCnt, zyukensya._sex);

            final int remarkByte = KNJ_EditEdit.getMS932ByteLength(zyukensya._remark);
            final String remarkFieldStr = remarkByte > 40 ? "4" : remarkByte > 30 ? "3" : "1";
            svf.VrsOutn("REMARK" + remarkFieldStr, lineCnt, zyukensya._remark);

            lineCnt++;
            _hasData = true;
        }
    }

    private void setZyukensyaMeiboTitle(final Vrw32alp svf, int pageNo, Date date) {
        svf.VrsOut("TITLE", _param._entexamyear + "年度　" + _param._testDivName + "入試　受験者名簿");
        svf.VrsOut("DATE", new SimpleDateFormat(KNJ_EditDate.h_format_thi(_param._loginDate, 0) + " HH:mm").format(date));
        svf.VrsOut("PAGE", pageNo + "頁");
    }

    /**
     * 未実装 入試成績一覧表を出力する。
     *
     * @param db2
     * @param svf
     * @param date 現在日時
     */
    private void printNyushiSeisekiIchiranhyou(final DB2UDB db2, final Vrw32alp svf, Date date) {
        svf.VrSetForm("KNJL692H_2.frm", 1);

        int lineCnt = 1; // 書き込み行数
        int pageCnt = 1; // 書き込んだページ数

        Map<String, String> confMap = getConfidetialRpt(db2);
        Map<String, String> subClassMap = getTestSubClass(db2);
        List<Map<String, String>> result = getNyushiSeisekiIchiranhyou(db2, confMap);

        setNyushiSeisekiIchiranhyouTitle(svf, pageCnt, date, confMap, subClassMap);

        for(Map<String, String> row : result) {
            // 改ページの制御
            if (lineCnt > NYUSHI_SEISEKI_ICHIRANHYO_LINE_MAX) {
                lineCnt = 1;
                pageCnt++;
                svf.VrEndPage();

                setNyushiSeisekiIchiranhyouTitle(svf, pageCnt, date, confMap, subClassMap);
            }

            svf.VrsOutn("NO", lineCnt, row.get("ROW_NUMBER"));
            svf.VrsOutn("ENT", lineCnt, row.get("ENTDIV"));
            svf.VrsOutn("PROCEDURE", lineCnt, row.get("PROCEDUREDIV"));
            svf.VrsOutn("PASS", lineCnt, row.get("JUDGEMENT"));
            svf.VrsOutn("KIND", lineCnt, row.get("MARK"));
            svf.VrsOutn("EXAM_NO", lineCnt, row.get("RECEPTNO"));

            String name = row.get("NAME");
            final int nameByte = KNJ_EditEdit.getMS932ByteLength(name);
            final String nameFieldStr = nameByte > 30 ? "3" : nameByte > 20 ? "2" : "1";
            svf.VrsOutn("NAME" + nameFieldStr, lineCnt, name);

            String nameKana = row.get("NAME_KANA");
            final int nameKanaByte = KNJ_EditEdit.getMS932ByteLength(nameKana);
            final String nameKanaFieldStr = nameKanaByte > 30 ? "2" : "1";
            svf.VrsOutn("KANA" + nameKanaFieldStr, lineCnt, nameKana);

            svf.VrsOutn("SEX", lineCnt, row.get("SEX"));
            svf.VrsOutn("BIRTHDAY", lineCnt, row.get("BIRTHDAY"));
            svf.VrsOutn("DISTRICT", lineCnt, row.get("FINSCHOOL_DIST_NAME"));
            svf.VrsOutn("FINSCHOOL_NAME", lineCnt, row.get("FINSCHOOL_NAME"));

            svf.VrsOutn("REP1", lineCnt, row.get("CONFIDENTIAL_RPT01"));
            svf.VrsOutn("REP2", lineCnt, row.get("CONFIDENTIAL_RPT02"));
            svf.VrsOutn("REP3", lineCnt, row.get("CONFIDENTIAL_RPT03"));
            svf.VrsOutn("REP4", lineCnt, row.get("CONFIDENTIAL_RPT04"));
            svf.VrsOutn("REP5", lineCnt, row.get("CONFIDENTIAL_RPT05"));
            svf.VrsOutn("TOTAL_REP1", lineCnt, row.get("TOTAL3"));
            svf.VrsOutn("TOTAL_REP2", lineCnt, row.get("TOTAL5"));
            svf.VrsOutn("TOTAL_REP3", lineCnt, row.get("TOTAL_ALL"));
            svf.VrsOutn("SCORE1", lineCnt, row.get("SCORE_KOKUGO"));
            svf.VrsOutn("SCORE2", lineCnt, row.get("SCORE_SUGAKU"));
            svf.VrsOutn("SCORE3", lineCnt, row.get("SCORE_EIGO"));
            svf.VrsOutn("TOTAL_SCORE", lineCnt, row.get("TOTAL"));
            svf.VrsOutn("RANK", lineCnt, row.get("TOTAL_RANK"));
            svf.VrsOutn("INTERVIEW", lineCnt, row.get("INTERVIEW_A"));

            String remark = row.get("REMARK");
            final int remarkByte = KNJ_EditEdit.getMS932ByteLength(remark);
            final String remarkFieldStr = remarkByte > 30 ? "3" : remarkByte > 24 ? "2" : "1";
            svf.VrsOutn("REMARK" + remarkFieldStr, lineCnt, remark);

            lineCnt++;
            _hasData = true;
        }
    }

    private void setNyushiSeisekiIchiranhyouTitle(final Vrw32alp svf, int pageNo, Date date, Map<String, String> confMap, Map<String, String> subclassMap) {
        svf.VrsOut("TITLE", _param._entexamyear + "年度" + _param._testDivName + "入試");
        String subtitle = "1".equals(_param._order)? "受験番号順" : "氏名順(50音順)";
        svf.VrsOut("SUBTITLE", subtitle);
        svf.VrsOut("DATE", KNJ_EditDate.h_format_thi(_param._loginDate, 0) + new SimpleDateFormat(" HH:mm:ss").format(date));
        svf.VrsOut("PAGE", pageNo + "頁");

        svf.VrsOut("REP_NAME1", confMap.get("01"));
        svf.VrsOut("REP_NAME2", confMap.get("02"));
        svf.VrsOut("REP_NAME3", confMap.get("03"));
        svf.VrsOut("REP_NAME4", confMap.get("04"));
        svf.VrsOut("REP_NAME5", confMap.get("05"));
        svf.VrsOut("SUBCLASS_NAME1", subclassMap.get(TESTSUBCLASSCD_KOKUGO));
        svf.VrsOut("SUBCLASS_NAME2", subclassMap.get(TESTSUBCLASSCD_SUGAKU));
        svf.VrsOut("SUBCLASS_NAME3", subclassMap.get(TESTSUBCLASSCD_EIGO));
    }

    /**
     * 合格者一覧（掲示用）を出力する。
     *
     * @param db2
     * @param svf
     */
    private void printGokakusyaIchiran(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrSetForm("KNJL692H_3.frm", 1);

        List<String> gokakusyaList = getGokakusyaIchiran(db2);

        int lineCnt = 0; // 書き込み行数
        int colCnt = 0; // 書き込み列数
        int pageCnt = 0; // 書き込んだページ数
        int index = 0; // 読み取り要素番号

        int indexMax = gokakusyaList.size();

        for(int i = 0; i < indexMax; i++) {
            if (colCnt >= GOKAKUSYA_ICHIRAN_COL_MAX) {
                lineCnt++;

                // 改ページの制御
                if (lineCnt >= GOKAKUSYA_ICHIRAN_LINE_MAX) {
                    lineCnt = 0;
                    colCnt = 0;
                    pageCnt++;

                    svf.VrEndPage();
                } else {
                    colCnt = 0;
                }
            }

            // 初項が行数（0スタート）の増分値が最大行数の数列の計算
            int colIncrements = colCnt * GOKAKUSYA_ICHIRAN_LINE_MAX;
            int pageIncrements = pageCnt * GOKAKUSYA_ICHIRAN_COL_MAX * GOKAKUSYA_ICHIRAN_LINE_MAX;
            index = lineCnt + colIncrements + pageIncrements;
            if (index < indexMax) {
                String receptNo = gokakusyaList.get(index);

                svf.VrsOutn("EXAM_NO" + (colCnt + 1), lineCnt + 1, receptNo);
            } else {
                i--; // 何も出力しない場合はカウントを進めない
            }

            colCnt++;
            _hasData = true;
        }
    }

    /**
     * 合格通知書を出力する。
     *
     * @param db2
     * @param svf
     */
    private void printGoukakuTsuchisyo(final DB2UDB db2, final Vrw32alp svf) {
        svf.VrSetForm("KNJL692H_4.frm", 1);

        List<GokakuTsuchisyo> goukakuTsuchisyoList = getGokakuTsuchisyoList(db2);

        for(GokakuTsuchisyo gokakuTsuchisyo : goukakuTsuchisyoList) {

            svf.VrsOut("EXAM_NO", gokakuTsuchisyo._receptNo);

            final int nameByte = KNJ_EditEdit.getMS932ByteLength(gokakuTsuchisyo._name);
            final String nameFieldStr = nameByte > 30 ? "3" : nameByte > 20 ? "2" : "1";
            svf.VrsOut("NAME" + nameFieldStr, gokakuTsuchisyo._name);

            svf.VrEndPage();
            _hasData = true;
        }
    }

    private List<Zyukensya> getZyukensyaMeibo(final DB2UDB db2) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        List<Zyukensya> zyukensyaList = new ArrayList<Zyukensya>();
        Zyukensya zyukensya = null;

        try {
            final String examineeSql = getExamineeSql();
            log.debug(" sql =" + examineeSql);
            ps = db2.prepareStatement(examineeSql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String receptNo = rs.getString("RECEPTNO");
                final String name = rs.getString("NAME");
                final String nameKana = rs.getString("NAME_KANA");
                final String sex = rs.getString("SEX");
                final String remark = rs.getString("REMARK");

                zyukensya = new Zyukensya(receptNo, name, nameKana, sex, remark);
                zyukensyaList.add(zyukensya);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }

        return zyukensyaList;
    }

    private List<Map<String, String>> getNyushiSeisekiIchiranhyou(final DB2UDB db2, Map<String, String> confMap) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        List<Map<String, String>> resultList = new ArrayList<Map<String, String>>();

        try {
            final String nyushiSeisekiIchiranhyouSql = getNyushiSeisekiIchiranhyouSql(confMap);
            log.debug(" sql =" + nyushiSeisekiIchiranhyouSql);
            ps = db2.prepareStatement(nyushiSeisekiIchiranhyouSql);
            rs = ps.executeQuery();

            ResultSetMetaData rsmd = rs.getMetaData();

            while (rs.next()) {
                Map<String, String> row = new LinkedHashMap<String, String>();

                for(int i = 1; i <= rsmd.getColumnCount(); i++) {
                    String key = rsmd.getColumnLabel(i);
                    String value = rs.getString(key);

                    row.put(key, value);
                }

                resultList.add(row);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }

        return resultList;
    }

    private Map<String, String> getConfidetialRpt(final DB2UDB db2) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        Map<String, String> confidetialRpt = new LinkedHashMap<String, String>();

        try {
            final String confidetialRptSql = getConfidetialRptSql();
            log.debug(" sql =" + confidetialRptSql);
            ps = db2.prepareStatement(confidetialRptSql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String key = rs.getString("SEQ");
                final String value = rs.getString("NAME1");

                confidetialRpt.put(key, value);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }

        return confidetialRpt;
    }

    private Map<String, String> getTestSubClass(final DB2UDB db2) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        Map<String, String> testSubClass = new LinkedHashMap<String, String>();

        try {
            final String testSubClassSql = getTestSubClassSql();
            log.debug(" sql =" + testSubClassSql);
            ps = db2.prepareStatement(testSubClassSql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String key = rs.getString("SEQ");
                final String value = rs.getString("NAME1");

                testSubClass.put(key, value);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }

        return testSubClass;
    }


    private List<String> getGokakusyaIchiran(final DB2UDB db2) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        List<String> gokakusyaList = new ArrayList<String>();

        try {
            final String passExamineeSql = getPassExamineeSql();
            log.debug(" sql =" + passExamineeSql);
            ps = db2.prepareStatement(passExamineeSql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String receptNo = rs.getString("RECEPTNO");

                gokakusyaList.add(receptNo);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }

        return gokakusyaList;
    }


    private List<GokakuTsuchisyo> getGokakuTsuchisyoList(final DB2UDB db2) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        List<GokakuTsuchisyo> gokakuTsuchisyoList = new ArrayList<GokakuTsuchisyo>();
        GokakuTsuchisyo gokakuTsuchisyo = null;

        try {
            final String passExamineeSql = getPassExamineeSql();
            log.debug(" sql =" + passExamineeSql);
            ps = db2.prepareStatement(passExamineeSql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String receptNo = rs.getString("RECEPTNO");
                final String name = rs.getString("NAME");

                gokakuTsuchisyo = new GokakuTsuchisyo(receptNo, name);
                gokakuTsuchisyoList.add(gokakuTsuchisyo);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
        }

        return gokakuTsuchisyoList;
    }


    private String getExamineeSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.TESTDIV, ");
        stb.append("     T2.RECEPTNO, ");
        stb.append("     T1.NAME, ");
        stb.append("     T1.NAME_KANA, ");
        stb.append("     Z002.NAME1 AS SEX, ");
        stb.append("     T3_031.REMARK10 AS REMARK ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_APPLICANTBASE_DAT T1 ");
        stb.append("     LEFT JOIN ENTEXAM_RECEPT_DAT T2 ");
        stb.append("       ON T2.ENTEXAMYEAR  = T1.ENTEXAMYEAR ");
        stb.append("      AND T2.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("      AND T2.TESTDIV      = T1.TESTDIV ");
        stb.append("      AND T2.EXAM_TYPE    = '1' ");
        stb.append("      AND T2.EXAMNO       = T1.EXAMNO ");
        stb.append("     LEFT JOIN V_NAME_MST Z002 ");
        stb.append("       ON Z002.YEAR    = T1.ENTEXAMYEAR ");
        stb.append("      AND Z002.NAMECD1 = 'Z002' ");
        stb.append("      AND Z002.NAMECD2 = T1.SEX ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT T3_031 ");
        stb.append("       ON T3_031.ENTEXAMYEAR  = T1.ENTEXAMYEAR ");
        stb.append("      AND T3_031.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("      AND T3_031.EXAMNO       = T1.EXAMNO ");
        stb.append("      AND T3_031.SEQ          = '031' ");
        stb.append(" WHERE ");
        stb.append("     T1.ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND T1.APPLICANTDIV = '2' "); // 2:高校 固定
        stb.append("     AND T1.TESTDIV = '" + _param._testDiv + "' ");
        //類別　00:全て　以外
        if (!"00".equals(_param._testDiv1)) {
            stb.append("     AND T1.TESTDIV1 = '" + _param._testDiv1 + "' ");
        }
        //受験番号(開始)
        if (!"".equals(_param._receptNoStart)) {
            stb.append(" AND T2.RECEPTNO >= '" + _param._receptNoStart + "' ");
        }
        //受験番号(終了)
        if (!"".equals(_param._receptNoEnd)) {
            stb.append(" AND T2.RECEPTNO <= '" + _param._receptNoEnd + "' ");
        }
        if ("2".equals(_param._filter1)) {
            stb.append(" AND T1.JUDGEMENT = '1' ");
        } else if ("3".equals(_param._filter1)) {
            stb.append(" AND T1.JUDGEMENT = '1' ");
            stb.append(" AND T1.TESTDIV1  = '01' "); //推薦
        } else if ("4".equals(_param._filter1)) {
            stb.append(" AND T1.JUDGEMENT = '1' ");
            stb.append(" AND T1.ENTDIV = '2' ");
        } else if ("5".equals(_param._filter1)) {
            stb.append(" AND T1.JUDGEMENT = '1' ");
            stb.append(" AND T1.ENTDIV = '1' ");
            stb.append(" AND T1.PROCEDUREDIV = '1' ");
        }
        if (!"3".equals(_param._sex)) {
            stb.append(" AND T1.SEX = '" + _param._sex + "' ");
        }
        stb.append(" ORDER BY ");
        if ("2".equals(_param._order)) {
            stb.append(" T1.NAME ");
        } else {
            stb.append(" T2.RECEPTNO ");
        }
        return stb.toString();
    }


    private String getNyushiSeisekiIchiranhyouSql(Map<String, String> confMap) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     ROWNUMBER() OVER() AS ROW_NUMBER, ");
        stb.append("     CASE WHEN BASE.ENTDIV = '1' THEN '〇' ELSE '' END AS ENTDIV, "); // 入学
        stb.append("     CASE WHEN BASE.PROCEDUREDIV = '1' THEN '〇' ELSE '' END AS PROCEDUREDIV, "); // 手続き
        stb.append("     CASE WHEN BASE.JUDGEMENT = '1' THEN '〇' ELSE '' END AS JUDGEMENT, "); // 合格
        stb.append("     CLASSIFY.MARK, "); // 類別
        stb.append("     RECEPT.RECEPTNO, ");
        stb.append("     BASE.NAME, ");
        stb.append("     BASE.NAME_KANA, ");
        stb.append("     Z002.NAME1 AS SEX, ");  // 性別
        stb.append("     VARCHAR_FORMAT(BASE.BIRTHDAY, 'yyyy/MM/dd') AS BIRTHDAY, "); // 生年月日
        stb.append("     L001.NAME1 AS FINSCHOOL_DIST_NAME, ");  // 出身
        stb.append("     FS.FINSCHOOL_NAME, "); // 中学
        stb.append("     CONF.CONFIDENTIAL_RPT01, ");
        stb.append("     CONF.CONFIDENTIAL_RPT02, ");
        stb.append("     CONF.CONFIDENTIAL_RPT03, ");
        stb.append("     CONF.CONFIDENTIAL_RPT04, ");
        stb.append("     CONF.CONFIDENTIAL_RPT05, ");
        stb.append("     CONF.TOTAL3, "); // 3教科
        stb.append("     CONF.TOTAL5, "); // 5教科
        stb.append("     CONF.TOTAL_ALL, "); // 9教科
        stb.append("     SCORE_KOKUGO.SCORE AS SCORE_KOKUGO, "); // 国語
        stb.append("     SCORE_SUGAKU.SCORE AS SCORE_SUGAKU, "); // 数学
        stb.append("     SCORE_EIGO.SCORE AS SCORE_EIGO, "); // 英語
        stb.append("     RECEPT.TOTAL4 AS TOTAL, "); // 合計
        stb.append("     RECEPT.TOTAL_RANK4 AS TOTAL_RANK, "); // 順位
        stb.append("     L027.NAME1 AS INTERVIEW_A, "); // 面接
        stb.append("     REGEXP_REPLACE(VALUE(BD031.REMARK8, '') || '　' || VALUE(BD031.REMARK9, '') || '　' || VALUE(BD031.REMARK10, ''), '(^　+)|(　+$)', '') AS REMARK ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_RECEPT_DAT RECEPT ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DAT BASE ");
        stb.append("            ON BASE.ENTEXAMYEAR  = RECEPT.ENTEXAMYEAR ");
        stb.append("           AND BASE.APPLICANTDIV = RECEPT.APPLICANTDIV ");
        stb.append("           AND BASE.EXAMNO       = RECEPT.EXAMNO ");
        stb.append("     LEFT JOIN ENTEXAM_SETTING_MST L012 ");
        stb.append("            ON L012.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ");
        stb.append("           AND L012.APPLICANTDIV = BASE.APPLICANTDIV ");
        stb.append("           AND L012.SETTING_CD   = 'L012' ");
        stb.append("           AND L012.SEQ          = BASE.ENTDIV ");
        stb.append("     LEFT JOIN ENTEXAM_CLASSIFY_MST CLASSIFY ");
        stb.append("            ON CLASSIFY.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ");
        stb.append("           AND CLASSIFY.APPLICANTDIV = BASE.APPLICANTDIV ");
        stb.append("           AND CLASSIFY.CLASSIFY_CD  = BASE.TESTDIV1 ");
        stb.append("     LEFT JOIN V_NAME_MST Z002 ");
        stb.append("            ON Z002.YEAR    = BASE.ENTEXAMYEAR ");
        stb.append("           AND Z002.NAMECD1 = 'Z002' ");
        stb.append("           AND Z002.NAMECD2 = BASE.SEX ");
        stb.append("     LEFT JOIN FINSCHOOL_MST FS ");
        stb.append("            ON FS.FINSCHOOLCD  = BASE.FS_CD ");
        stb.append("     LEFT JOIN NAME_MST L001 ");
        stb.append("            ON L001.NAMECD1 = 'L001' ");
        stb.append("           AND L001.NAMECD2 = FS.FINSCHOOL_DISTCD ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT CONF ");
        stb.append("            ON CONF.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ");
        stb.append("           AND CONF.APPLICANTDIV = BASE.APPLICANTDIV ");
        stb.append("           AND CONF.EXAMNO       = BASE.EXAMNO ");
        stb.append("     LEFT JOIN ENTEXAM_SCORE_DAT SCORE_KOKUGO ");
        stb.append("            ON SCORE_KOKUGO.ENTEXAMYEAR    = RECEPT.ENTEXAMYEAR ");
        stb.append("           AND SCORE_KOKUGO.APPLICANTDIV   = RECEPT.APPLICANTDIV ");
        stb.append("           AND SCORE_KOKUGO.TESTDIV        = RECEPT.TESTDIV ");
        stb.append("           AND SCORE_KOKUGO.EXAM_TYPE      = RECEPT.EXAM_TYPE ");
        stb.append("           AND SCORE_KOKUGO.RECEPTNO       = RECEPT.RECEPTNO ");
        stb.append("           AND SCORE_KOKUGO.TESTSUBCLASSCD = '" + TESTSUBCLASSCD_KOKUGO + "' "); // 国語
        stb.append("     LEFT JOIN ENTEXAM_SCORE_DAT SCORE_SUGAKU ");
        stb.append("            ON SCORE_SUGAKU.ENTEXAMYEAR    = RECEPT.ENTEXAMYEAR ");
        stb.append("           AND SCORE_SUGAKU.APPLICANTDIV   = RECEPT.APPLICANTDIV ");
        stb.append("           AND SCORE_SUGAKU.TESTDIV        = RECEPT.TESTDIV ");
        stb.append("           AND SCORE_SUGAKU.EXAM_TYPE      = RECEPT.EXAM_TYPE ");
        stb.append("           AND SCORE_SUGAKU.RECEPTNO       = RECEPT.RECEPTNO ");
        stb.append("           AND SCORE_SUGAKU.TESTSUBCLASSCD = '" + TESTSUBCLASSCD_SUGAKU + "' "); // 数学
        stb.append("     LEFT JOIN ENTEXAM_SCORE_DAT SCORE_EIGO ");
        stb.append("            ON SCORE_EIGO.ENTEXAMYEAR    = RECEPT.ENTEXAMYEAR ");
        stb.append("           AND SCORE_EIGO.APPLICANTDIV   = RECEPT.APPLICANTDIV ");
        stb.append("           AND SCORE_EIGO.TESTDIV        = RECEPT.TESTDIV ");
        stb.append("           AND SCORE_EIGO.EXAM_TYPE      = RECEPT.EXAM_TYPE ");
        stb.append("           AND SCORE_EIGO.RECEPTNO       = RECEPT.RECEPTNO ");
        stb.append("           AND SCORE_EIGO.TESTSUBCLASSCD = '" + TESTSUBCLASSCD_EIGO + "' "); // 英語
        stb.append("     LEFT JOIN ENTEXAM_INTERVIEW_DAT INTERVIEW ");
        stb.append("            ON INTERVIEW.ENTEXAMYEAR  = RECEPT.ENTEXAMYEAR ");
        stb.append("           AND INTERVIEW.APPLICANTDIV = RECEPT.APPLICANTDIV ");
        stb.append("           AND INTERVIEW.TESTDIV      = RECEPT.TESTDIV ");
        stb.append("           AND INTERVIEW.EXAMNO       = RECEPT.EXAMNO ");
        stb.append("     LEFT JOIN ENTEXAM_SETTING_MST L027 ");
        stb.append("            ON L027.ENTEXAMYEAR  = INTERVIEW.ENTEXAMYEAR ");
        stb.append("           AND L027.APPLICANTDIV = INTERVIEW.APPLICANTDIV ");
        stb.append("           AND L027.SETTING_CD   = 'L027' ");
        stb.append("           AND L027.SEQ          = INTERVIEW.INTERVIEW_A ");
        stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD031 ");
        stb.append("            ON BD031.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ");
        stb.append("           AND BD031.APPLICANTDIV = BASE.APPLICANTDIV ");
        stb.append("           AND BD031.EXAMNO       = BASE.EXAMNO ");
        stb.append("           AND BD031.SEQ          = '031' ");
        stb.append(" WHERE  ");
        stb.append("     RECEPT.ENTEXAMYEAR  = '" + _param._entexamyear + "' ");
        stb.append(" AND RECEPT.APPLICANTDIV = '2' "); // 2:高校 固定
        stb.append(" AND RECEPT.TESTDIV      = '" + _param._testDiv + "' ");
        //類別　00:全て　以外
        if (!"00".equals(_param._testDiv1)) {
            stb.append(" AND BASE.TESTDIV1       = '" + _param._testDiv1 + "' ");
        }
        //受験番号(開始)
        if (!"".equals(_param._receptNoStart)) {
            stb.append(" AND RECEPT.RECEPTNO >= '" + _param._receptNoStart + "' ");
        }
        //受験番号(終了)
        if (!"".equals(_param._receptNoEnd)) {
            stb.append(" AND RECEPT.RECEPTNO <= '" + _param._receptNoEnd + "' ");
        }
        if ("2".equals(_param._filter2)) {
            stb.append(" AND BASE.JUDGEMENT = '1' ");
        } else if ("3".equals(_param._filter2)) {
            stb.append(" AND BASE.JUDGEMENT = '1' ");
            stb.append(" AND BASE.TESTDIV1  = '01' "); //推薦
        } else if ("4".equals(_param._filter2)) {
            stb.append(" AND BASE.JUDGEMENT = '1' ");
            stb.append(" AND BASE.ENTDIV = '2' ");
        } else if ("5".equals(_param._filter2)) {
            stb.append(" AND BASE.JUDGEMENT = '1' ");
            stb.append(" AND BASE.ENTDIV = '1' ");
            stb.append(" AND BASE.PROCEDUREDIV = '1' ");
        }
        if (!"3".equals(_param._sex)) {
            stb.append(" AND BASE.SEX = '" + _param._sex + "' ");
        }
        stb.append(" ORDER BY ");
        if ("2".equals(_param._order)) {
            stb.append(" BASE.NAME ");
        } else {
            stb.append(" RECEPT.RECEPTNO ");
        }
        return stb.toString();
    }


    private String getConfidetialRptSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     SEQ, ");
        stb.append("     NAME1 ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_SETTING_MST ");
        stb.append(" WHERE ");
        stb.append("     ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND APPLICANTDIV = '2' "); // 2:高校 固定
        stb.append("     AND SETTING_CD = 'L008' ");
        stb.append(" ORDER BY ");
        stb.append("     VALUE(SEQ, 0) ");
        return stb.toString();
    }

    private String getTestSubClassSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     SEQ, ");
        stb.append("     NAME1 ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_SETTING_MST ");
        stb.append(" WHERE ");
        stb.append("     ENTEXAMYEAR = '" + _param._entexamyear + "' ");
        stb.append("     AND APPLICANTDIV = '2' "); // 2:高校 固定
        stb.append("     AND SETTING_CD = 'L009' ");
        stb.append(" ORDER BY ");
        stb.append("     VALUE(SEQ, 0) ");
        return stb.toString();
    }

    private String getPassExamineeSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T2.RECEPTNO, ");
        stb.append("     T1.NAME ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_APPLICANTBASE_DAT T1 ");
        stb.append("     LEFT JOIN ENTEXAM_RECEPT_DAT T2 ");
        stb.append("       ON T2.ENTEXAMYEAR  = T1.ENTEXAMYEAR ");
        stb.append("      AND T2.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("      AND T2.TESTDIV      = T1.TESTDIV ");
        stb.append("      AND T2.EXAM_TYPE    = '1' ");
        stb.append("      AND T2.EXAMNO       = T1.EXAMNO ");
        stb.append(" WHERE ");
        stb.append("     T1.ENTEXAMYEAR      = '" + _param._entexamyear + "' ");
        stb.append("     AND T1.APPLICANTDIV = '2' "); // 2:高校 固定
        stb.append("     AND T1.TESTDIV      = '" + _param._testDiv + "' ");
        //類別　00:全て　以外
        if (!"00".equals(_param._testDiv1)) {
            stb.append("     AND T1.TESTDIV1     = '" + _param._testDiv1 + "' ");
        }
        stb.append("     AND T1.JUDGEMENT    = '1' "); // 1:合格
        if (!"".equals(_param._receptNoStart)) {
            stb.append("     AND T2.RECEPTNO    >= '" + _param._receptNoStart + "' ");
        }
        if (!"".equals(_param._receptNoEnd)) {
            stb.append("     AND T2.RECEPTNO    <= '" + _param._receptNoEnd + "' ");
        }
        if (!"3".equals(_param._sex)) {
            stb.append("     AND T1.SEX          = '" + _param._sex + "' ");
        }
        stb.append(" ORDER BY ");
        if ("2".equals(_param._order)) {
            stb.append(" T1.NAME ");
        } else {
            stb.append(" T2.RECEPTNO ");
        }
        return stb.toString();
    }

    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Revision: $");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    private class Zyukensya {
        private final String _receptNo;
        private final String _name;
        private final String _nameKana;
        private final String _sex;
        private final String _remark;

        Zyukensya (
                String receptNo,
                String name,
                String nameKana,
                String sex,
                String remark
        ) {
            _receptNo = receptNo;
            _name = name;
            _nameKana = nameKana;
            _sex = sex;
            _remark = remark;
        }
    }

    private class GokakuTsuchisyo {
        private final String _receptNo;
        private final String _name;

        GokakuTsuchisyo (String receptNo, String name) {
            _receptNo = receptNo;
            _name = name;
        }
    }

    /** パラメータクラス */
    private static class Param {
        private final String _entexamyear;
        private final String _testDiv;
        private final String _testDiv1;
        private final String _receptNoStart;
        private final String _receptNoEnd;
        private final String _sex;
        private final String _order;
        private final String _output;
        private final String _filter1;
        private final String _filter2;
        private final String _testDivName;
        private final String _loginDate;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _entexamyear = request.getParameter("ENTEXAMYEAR");
            _testDiv = request.getParameter("TESTDIV");
            _testDiv1 = request.getParameter("TESTDIV1");
            String receptNoStart = request.getParameter("RECEPTNO_START");
            if ("".equals(receptNoStart)) {
                _receptNoStart = "";
            } else {
                _receptNoStart = String.format("%04d", Integer.parseInt(receptNoStart));
            }
            String receptNoEnd = request.getParameter("RECEPTNO_END");
            if ("".equals(receptNoEnd)) {
                _receptNoEnd = "";
            } else {
                _receptNoEnd = String.format("%04d", Integer.parseInt(receptNoEnd));
            }
            _sex = request.getParameter("SEX");
            _order = request.getParameter("ORDER");
            _output = request.getParameter("OUTPUT");
            _filter1 = request.getParameter("FILTER1");
            _filter2 = request.getParameter("FILTER2");
            _testDivName = getTestDivName(db2);
            _loginDate = request.getParameter("LOGIN_DATE");
        }

        private String getTestDivName(final DB2UDB db2) {
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;

            String sql = " SELECT TESTDIV_ABBV FROM ENTEXAM_TESTDIV_MST WHERE ENTEXAMYEAR='" + _entexamyear + "' AND APPLICANTDIV = '2' AND TESTDIV = '" + _testDiv + "' ";
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

