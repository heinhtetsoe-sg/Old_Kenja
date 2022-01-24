package servletpack.KNJL;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 *
 *    学校教育システム 賢者 [入試管理]
 *
 **/

public class KNJL345F {

    private static final Log log = LogFactory.getLog(KNJL345F.class);

    private boolean _hasData;
    private Param _param;

    private final String FROM_TO_MARK = "\uFF5E";

    public void svf_out(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

        Vrw32alp svf     = new Vrw32alp();   //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2       = null;             //Databaseクラスを継承したクラス

        log.fatal("$Revision: 65192 $ $Date: 2019-01-22 11:31:53 +0900 (火, 22 1 2019) $"); // CVSキーワードの取り扱いに注意

        //print設定
        PrintWriter outstrm = new PrintWriter (response.getOutputStream());
        response.setContentType("application/pdf");

        //svf設定
        svf.VrInit();    //クラスの初期化
        svf.VrSetSpoolFileStream(response.getOutputStream()); //PDFファイル名の設定

        //ＤＢ接続
        try {
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();
        } catch (Exception ex) {
            log.error("DB2 open error!", ex);
            return;
        }

        try {
            KNJServletUtils.debugParam(request, log);
            _param = new Param(db2, request);

            printMain(svf, db2);

        } catch (Exception ex) {
            log.error("exception!", ex);
        } finally {
            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note" , "note");
                svf.VrEndPage();
            }

            //終了処理
            svf.VrQuit();
            db2.commit();
            db2.close();       //DBを閉じる
            outstrm.close();   //ストリームを閉じる
        }
    }

    private static Map toMap(final List mapList, final String keyField, final String valueField) {
        final Map rtn = new HashMap();
        for (final Iterator it = mapList.iterator(); it.hasNext();) {
            final Map map = (Map) it.next();
            rtn.put(getString(map, keyField), getString(map, valueField));
        }
        return rtn;
    }

    private static List getMappedList(final Map map, final Object key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new ArrayList());
        }
        return (List) map.get(key1);
    }

    private static Map getMappedHashMap(final Map map, final Object key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new HashMap());
//            log.info(" create map : key = " + key1);
        }
        return (Map) map.get(key1);
    }

    private static String getString(final Map m, final String field) {
        if (null == m) {
            return null;
        }
        try {
            if (!m.containsKey(field)) {
                throw new IllegalArgumentException("not defined: " + field + " in " + m.keySet());
            }
        } catch (Exception e) {
            log.fatal("exception!", e);
        }
        return (String) m.get(field);
    }

    private static int getMS932Bytecount(String str) {
        int count = 0;
        if (null != str) {
            try {
                count = str.getBytes("MS932").length;
            } catch (Exception e) {
                log.error(e);
            }
        }
        return count;
    }

    private static List getList(final DB2UDB db2, final String sql) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        final List list = new ArrayList();
        try {
            log.info(" sql = " + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            ResultSetMetaData meta = rs.getMetaData();
            while (rs.next()) {
                final Map m = new HashMap();
                list.add(m);
                for (int i = 1; i <= meta.getColumnCount(); i++) {
                    m.put(meta.getColumnLabel(i), rs.getString(i));
                }
            }
        } catch (Exception ex) {
            log.fatal("exception!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return list;
    }

    private static String add(final String s1, final String s2) {
        if (!NumberUtils.isNumber(s1)) { return s2; }
        if (!NumberUtils.isNumber(s2)) { return s1; }
        return String.valueOf(Integer.parseInt(s1) + Integer.parseInt(s2));
    }

    private static String subtract(final String s1, final String s2) {
        if (!NumberUtils.isNumber(s1) && !NumberUtils.isNumber(s2)) {
            return null;
        }
        final int v1 = NumberUtils.isNumber(s1) ? Integer.parseInt(s1) : 0;
        final int v2 = NumberUtils.isNumber(s2) ? Integer.parseInt(s2) : 0;
        return String.valueOf(v1 - v2);
    }

    private void printMain(final Vrw32alp svf, final DB2UDB db2) {

        final List receptList = Recept.getReceptList(db2, _param);
        final Map subclassScoreListMap = new HashMap();
        final Set receptnos = new HashSet();
        for (final Iterator it = receptList.iterator(); it.hasNext();) {
            final Recept recept = (Recept) it.next();
            final Map subclasscdScoreMap = recept.getPrintSubclassMap(_param);
            for (final Iterator scoreit = subclasscdScoreMap.entrySet().iterator(); scoreit.hasNext();) {
                final Map.Entry e = (Map.Entry) scoreit.next();
                final String subclasscd = (String) e.getKey();
                final String scoreStr = (String) e.getValue();
                if (NumberUtils.isNumber(scoreStr)) {
                    getMappedList(subclassScoreListMap, subclasscd).add(new Integer(scoreStr));
                    receptnos.add(recept._receptno);
                }
            }
        }
        log.debug(" print subclasscd = " + subclassScoreListMap.keySet());

        final int scoreMax = 100;
        final int kizami = 5;

        final String form = "KNJL345F_J.frm";
        svf.VrSetForm(form, 1);

        svf.VrsOut("TITLE", KenjaProperties.gengou(Integer.parseInt(_param._entexamyear)) + "年度　" + StringUtils.defaultString(_param._applicantdivName)  + "入試　入試統計資料"); // タイトル
        if (!"ALL".equals(_param._testdiv)) {
            svf.VrsOut("SELECT", _param._testdivName); // 条件
        }
        svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(_param._loginDate)); // 日付

        for (int i = 0; i < Math.min(5, _param._subclassList.size()); i++) {
            final int col = i + 1;
            final Map subclass = (Map) _param._subclassList.get(i);

            log.debug(" subclass " + i + " : "+ subclass);

            svf.VrsOut("SUBCLASS_NAME" + String.valueOf(col), getString(subclass, "SUBCLASSNAME")); // 科目名

            final List subclassScoreList = getMappedList(subclassScoreListMap, getString(subclass, "SUBCLASSCD"));

            final List subclassScoreRemoveList = new ArrayList(subclassScoreList);
            final int totalCount = subclassScoreRemoveList.size();
            int accum = 0;
            for (int j = 0; j < scoreMax / kizami; j++) {
                final int high = scoreMax - j * kizami;
                int low = scoreMax - (j + 1) * kizami + 1;
                if (1 == low) {
                    low = 0;
                }
                final int line = j + 1;

                final int count = getScoreRangeCount(subclassScoreRemoveList, high, low);
                if (count != 0) {
                    accum = accum += count;
                    svf.VrsOutn("DISTRI_NUM" + String.valueOf(col), line, String.valueOf(count)); // 分布
                    svf.VrsOutn("DISTRI_PER" + String.valueOf(col), line, percentage(count, totalCount)); // 分布
                    svf.VrsOutn("ACCUM_NUM" + String.valueOf(col), line, String.valueOf(accum)); // 累積
                    svf.VrsOutn("ACCUM_PER" + String.valueOf(col), line, percentage(accum, totalCount)); // 累積
                }
            }

            svf.VrsOut("AVE" + String.valueOf(col), average(subclassScoreList)); // 科目名
            svf.VrsOut("MAX" + String.valueOf(col), maximum(subclassScoreList)); // 科目名
            svf.VrsOut("MIN" + String.valueOf(col), minimum(subclassScoreList)); // 科目名
        }

        svf.VrEndPage();
        _hasData = true;
    }

    /**
     * 得点が下限以上上限以下の個数を得る
     * @param subclassScoreRemoveList 得点のリスト。カウントされた要素は除去される
     * @param high 上限
     * @param low 下限
     * @return 得点が下限以上上限以下の個数
     */
    private int getScoreRangeCount(final List subclassScoreRemoveList, final int high, final int low) {
        int count = 0;
        for (final Iterator it = subclassScoreRemoveList.iterator(); it.hasNext();) {
            final Integer score = (Integer) it.next();
            if (low <= score.intValue() && score.intValue() <= high) {
                count += 1;
                it.remove();
            }
        }
        return count;
    }

    /**
     * 百分率の値
     * @param a 分子
     * @param b 分母
     * @return 百分率の値
     */
    private static String percentage(final int a, final int b) {
        if (b == 0) {
            return null;
        }
        final BigDecimal bda = new BigDecimal(a);
        final BigDecimal bdb = new BigDecimal(b);
        return bda.multiply(new BigDecimal(100)).divide(bdb, 1, BigDecimal.ROUND_HALF_UP).toString();
    }

    // List<Integer>の最小値
    private static String minimum(final List scoreList) {
        if (scoreList.isEmpty()) {
            return null;
        }
        int rtn = 999999;
        for (int i = 0; i < scoreList.size(); i++) {
            final Integer num = (Integer) scoreList.get(i);
            if (rtn > num.intValue()) {
                rtn = num.intValue();
            }
        }
        return String.valueOf(rtn);
    }

    // List<Integer>の最大値
    private static String maximum(final List scoreList) {
        if (scoreList.isEmpty()) {
            return null;
        }
        int rtn = 0;
        for (int i = 0; i < scoreList.size(); i++) {
            final Integer num = (Integer) scoreList.get(i);
            if (rtn < num.intValue()) {
                rtn = num.intValue();
            }
        }
        return String.valueOf(rtn);
    }

    // List<Integer>の平均値
    private static String average(final List scoreList) {
        if (scoreList.isEmpty()) {
            return null;
        }
        BigDecimal bd = new BigDecimal(0);
        for (int i = 0; i < scoreList.size(); i++) {
            final Integer num = (Integer) scoreList.get(i);
            bd = bd.add(new BigDecimal(num.intValue()));
        }
        final BigDecimal avg = bd.divide(new BigDecimal(scoreList.size()), 1, BigDecimal.ROUND_HALF_UP);
        return avg.toString();
    }

    private static class Recept {
        final String _examno;
        final String _receptno;
        final String _testdiv;
        final String _examType;
        final String _name;
        final String _nameKana;
        final String _score1Subclasscd;
        final String _score2Subclasscd;
        final String _judgediv;
        final String _judgedivName;

        final Map _scoreMap = new HashMap();
        final Map _testdivExamnoMap = new HashMap();
        final Map _testdivJudgedivMap = new HashMap();

        Recept(
            final String examno,
            final String receptno,
            final String testdiv,
            final String examType,
            final String name,
            final String nameKana,
            final String score1Subclasscd,
            final String score2Subclasscd,
            final String judgediv,
            final String judgedivName
        ) {
            _examno = examno;
            _receptno = receptno;
            _testdiv = testdiv;
            _examType = examType;
            _name = name;
            _nameKana = nameKana;
            _score1Subclasscd = score1Subclasscd;
            _score2Subclasscd = score2Subclasscd;
            _judgediv = judgediv;
            _judgedivName = judgedivName;
        }

        public Map getPrintSubclassMap(final Param param) {
            final Map testsubclasscdScoreListMap = new HashMap();
            //log.debug(" _score1Subclasscd = " + _score1Subclasscd + ", " + _score2Subclasscd + "/ " + _testdiv + " : " + _examType);
            for (final Iterator it = _scoreMap.keySet().iterator(); it.hasNext();) {
                final String testsubclasscd = (String) it.next();
                final String score = (String) _scoreMap.get(testsubclasscd);
                if (!NumberUtils.isDigits(score)) {
                    continue;
                }
                if ("1".equals(param._applicantdiv) && "2".equals(_examType)) {
                    if ("1".equals(testsubclasscd) || "2".equals(testsubclasscd)) {
                        getMappedList(testsubclasscdScoreListMap, testsubclasscd).add(score);
                    } else if (null != _score1Subclasscd && _score1Subclasscd.equals(testsubclasscd) ||
                                null != _score2Subclasscd && _score2Subclasscd.equals(testsubclasscd)) {
                        final String location = (String) param._subclassLocationMap.get(testsubclasscd);
                        getMappedList(testsubclasscdScoreListMap, NumberUtils.isDigits(location) ? location : testsubclasscd).add(score);
                    }
                } else {
                    getMappedList(testsubclasscdScoreListMap, testsubclasscd).add(score);
                }
            }
            final Map rtn = new HashMap();
            for (final Iterator it = testsubclasscdScoreListMap.keySet().iterator(); it.hasNext();) {
                final String location = (String) it.next();
                if (NumberUtils.isDigits(location)) {
                    rtn.put(location, sum(getMappedList(testsubclasscdScoreListMap, location)));
                }
            }
            //log.debug(" receptno = " + _receptno + ", scoreListMap = " + testsubclasscdScoreListMap + " -> " + rtn);
            return rtn;
        }

        private static String sum(final List list) {
            if (null == list) {
                return null;
            }
            if (list.size() == 1) {
                return (String) list.get(0);
            }
            String rtn = null;
            for (final Iterator it = list.iterator(); it.hasNext();) {
                final String score = (String) it.next();
                rtn = add(rtn, score);
            }
            return rtn;
        }

        public static List getReceptList(final DB2UDB db2, final Param param) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = sql(param);
                log.info(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String examno = rs.getString("EXAMNO");
                    final String receptno = rs.getString("RECEPTNO");
                    final String testdiv = rs.getString("TESTDIV");
                    final String examType = rs.getString("EXAM_TYPE");
                    final String name = rs.getString("NAME");
                    final String nameKana = rs.getString("NAME_KANA");
                    final String score1Subclasscd = rs.getString("SCORE1_SUBCLASSCD");
                    final String score2Subclasscd = rs.getString("SCORE2_SUBCLASSCD");
                    final String judgediv = rs.getString("JUDGEDIV");
                    final String judgedivName = rs.getString("JUDGEDIV_NAME");
                    final Recept recept = new Recept(examno, receptno, testdiv, examType, name, nameKana, score1Subclasscd, score2Subclasscd, judgediv, judgedivName);
                    list.add(recept);
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }

            try {
                final String sql = testJudgedivSql(param);
                log.debug(" judgediv sql = " + sql);
                ps = db2.prepareStatement(sql);

                for (final Iterator it = list.iterator(); it.hasNext();) {
                    final Recept recept = (Recept) it.next();

                    ps.setString(1, recept._testdiv);
                    ps.setString(2, recept._examno);
                    rs = ps.executeQuery();

                    while (rs.next()) {
                        recept._testdivExamnoMap.put("1", rs.getString("EXAMNO1"));
                        recept._testdivExamnoMap.put("2", rs.getString("EXAMNO2"));
                        recept._testdivExamnoMap.put("3", rs.getString("EXAMNO3"));
                        recept._testdivExamnoMap.put("4", rs.getString("EXAMNO4"));
                        recept._testdivExamnoMap.put("5", rs.getString("EXAMNO5"));
                        recept._testdivJudgedivMap.put("1", rs.getString("JUDGEDIV1"));
                        recept._testdivJudgedivMap.put("2", rs.getString("JUDGEDIV2"));
                        recept._testdivJudgedivMap.put("3", rs.getString("JUDGEDIV3"));
                        recept._testdivJudgedivMap.put("4", rs.getString("JUDGEDIV4"));
                        recept._testdivJudgedivMap.put("5", rs.getString("JUDGEDIV5"));
                    }

                    DbUtils.closeQuietly(rs);

                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }

            try {
                final String sql = scoreSql(param);
                ps = db2.prepareStatement(sql);

                for (final Iterator it = list.iterator(); it.hasNext();) {
                    final Recept recept = (Recept) it.next();

                    ps.setString(1, recept._testdiv);
                    ps.setString(2, recept._receptno);

                    rs = ps.executeQuery();

                    while (rs.next()) {
                        recept._scoreMap.put(rs.getString("TESTSUBCLASSCD"), rs.getString("SCORE"));
                    }

                    DbUtils.closeQuietly(rs);

                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }

        private static String scoreSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     TREC.TESTSUBCLASSCD ");
            stb.append("   , TREC.SCORE ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_SCORE_DAT TREC ");
            stb.append(" WHERE ");
            stb.append("     TREC.ENTEXAMYEAR = '" + param._entexamyear + "' ");
            stb.append("     AND TREC.APPLICANTDIV = '" + param._applicantdiv + "' ");
            stb.append("     AND TREC.TESTDIV = ? ");
            stb.append("     AND TREC.EXAM_TYPE = '1' ");
            stb.append("     AND TREC.RECEPTNO = ? ");
            return stb.toString();
        }

        private static String testJudgedivSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     TREC.EXAMNO ");
            stb.append("   , TRDET012.REMARK1 AS EXAMNO1 ");
            stb.append("   , TRDET012.REMARK2 AS EXAMNO2 ");
            stb.append("   , TRDET012.REMARK3 AS EXAMNO3 ");
            stb.append("   , TRDET012.REMARK4 AS EXAMNO4 ");
            stb.append("   , TRDET012.REMARK5 AS EXAMNO5 ");
            stb.append("   , RECEP1.JUDGEDIV AS JUDGEDIV1 ");
            stb.append("   , RECEP2.JUDGEDIV AS JUDGEDIV2 ");
            stb.append("   , RECEP3.JUDGEDIV AS JUDGEDIV3 ");
            stb.append("   , RECEP4.JUDGEDIV AS JUDGEDIV4 ");
            stb.append("   , RECEP5.JUDGEDIV AS JUDGEDIV5 ");
            stb.append(" FROM ");
            stb.append("     V_ENTEXAM_RECEPT_DAT TREC ");
            stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DAT TAPPL ON TAPPL.ENTEXAMYEAR = TREC.ENTEXAMYEAR ");
            stb.append("         AND TAPPL.EXAMNO = TREC.EXAMNO ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT TRDET010 ON TRDET010.ENTEXAMYEAR = TREC.ENTEXAMYEAR ");
            stb.append("         AND TRDET010.EXAMNO = TREC.EXAMNO ");
            stb.append("         AND TRDET010.SEQ = '010' ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT TRDET011 ON TRDET011.ENTEXAMYEAR = TREC.ENTEXAMYEAR ");
            stb.append("         AND TRDET011.EXAMNO = TREC.EXAMNO ");
            stb.append("         AND TRDET011.SEQ = '011' ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_BUN_DAT TRDET012 ON TRDET012.ENTEXAMYEAR = TREC.ENTEXAMYEAR ");
            stb.append("         AND TRDET012.EXAMNO = TREC.EXAMNO ");
            stb.append("         AND TRDET012.SEQ = '012' ");
            stb.append("     LEFT JOIN V_ENTEXAM_RECEPT_DAT RECEP1 ON RECEP1.ENTEXAMYEAR = TREC.ENTEXAMYEAR AND RECEP1.APPLICANTDIV = TREC.APPLICANTDIV ");
            stb.append("         AND RECEP1.TESTDIV = TRDET010.REMARK1 AND RECEP1.EXAM_TYPE = TRDET011.REMARK1 AND RECEP1.RECEPTNO = TRDET012.REMARK1 ");
            stb.append("     LEFT JOIN V_ENTEXAM_RECEPT_DAT RECEP2 ON RECEP2.ENTEXAMYEAR = TREC.ENTEXAMYEAR AND RECEP2.APPLICANTDIV = TREC.APPLICANTDIV ");
            stb.append("         AND RECEP2.TESTDIV = TRDET010.REMARK2 AND RECEP2.EXAM_TYPE = TRDET011.REMARK2 AND RECEP2.RECEPTNO = TRDET012.REMARK2 ");
            stb.append("     LEFT JOIN V_ENTEXAM_RECEPT_DAT RECEP3 ON RECEP3.ENTEXAMYEAR = TREC.ENTEXAMYEAR AND RECEP3.APPLICANTDIV = TREC.APPLICANTDIV ");
            stb.append("         AND RECEP3.TESTDIV = TRDET010.REMARK3 AND RECEP3.EXAM_TYPE = TRDET011.REMARK3 AND RECEP3.RECEPTNO = TRDET012.REMARK3 ");
            stb.append("     LEFT JOIN V_ENTEXAM_RECEPT_DAT RECEP4 ON RECEP4.ENTEXAMYEAR = TREC.ENTEXAMYEAR AND RECEP4.APPLICANTDIV = TREC.APPLICANTDIV ");
            stb.append("         AND RECEP4.TESTDIV = TRDET010.REMARK4 AND RECEP4.EXAM_TYPE = TRDET011.REMARK4 AND RECEP4.RECEPTNO = TRDET012.REMARK4 ");
            stb.append("     LEFT JOIN V_ENTEXAM_RECEPT_DAT RECEP5 ON RECEP5.ENTEXAMYEAR = TREC.ENTEXAMYEAR AND RECEP5.APPLICANTDIV = TREC.APPLICANTDIV ");
            stb.append("         AND RECEP5.TESTDIV = TRDET010.REMARK5 AND RECEP5.EXAM_TYPE = TRDET011.REMARK5 AND RECEP5.RECEPTNO = TRDET012.REMARK5 ");
            stb.append(" WHERE ");
            stb.append("     TREC.ENTEXAMYEAR = '" + param._entexamyear + "' ");
            stb.append("     AND TREC.APPLICANTDIV = '" + param._applicantdiv + "' ");
            stb.append("     AND TREC.TESTDIV = ? ");
            stb.append("     AND TREC.EXAM_TYPE = '1' ");
            stb.append("     AND TREC.EXAMNO = ? ");
            return stb.toString();
        }

        private static String sql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     TREC.EXAMNO ");
            stb.append("   , VATD.EXAM_TYPE ");
            stb.append("   , NML005.NAME1 AS EXAM_TYPE_NAME ");
            stb.append("   , TREC.RECEPTNO ");
            stb.append("   , TREC.TESTDIV ");
            stb.append("   , TAPPL.NAME ");
            stb.append("   , TAPPL.NAME_KANA ");
            stb.append("   , TREC.TOTAL2 ");
            stb.append("   , TREC.TOTAL4 ");
            stb.append("   , TREC.JUDGEDIV ");
            stb.append("   , NML013.NAME1 AS JUDGEDIV_NAME ");
            stb.append("   , TADET002.REMARK1 AS RECRUIT_NO ");
            stb.append("   , TRDET004.REMARK1 AS SCORE1_SUBCLASSCD ");
            stb.append("   , TRDET004.REMARK2 AS SCORE2_SUBCLASSCD ");
            stb.append(" FROM ");
            stb.append("     V_ENTEXAM_RECEPT_DAT TREC ");
            stb.append("     INNER JOIN ENTEXAM_APPLICANTBASE_DAT TAPPL ON TAPPL.ENTEXAMYEAR = TREC.ENTEXAMYEAR ");
            stb.append("         AND TAPPL.EXAMNO = TREC.EXAMNO ");
            stb.append("     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT TRDET001 ON TRDET001.ENTEXAMYEAR = TREC.ENTEXAMYEAR ");
            stb.append("         AND TRDET001.APPLICANTDIV = TREC.APPLICANTDIV ");
            stb.append("         AND TRDET001.TESTDIV = TREC.TESTDIV ");
            stb.append("         AND TRDET001.EXAM_TYPE = TREC.EXAM_TYPE ");
            stb.append("         AND TRDET001.RECEPTNO = TREC.RECEPTNO ");
            stb.append("         AND TRDET001.SEQ = '001' ");
            stb.append("     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT TRDET004 ON TRDET004.ENTEXAMYEAR = TREC.ENTEXAMYEAR ");
            stb.append("         AND TRDET004.APPLICANTDIV = TREC.APPLICANTDIV ");
            stb.append("         AND TRDET004.TESTDIV = TREC.TESTDIV ");
            stb.append("         AND TRDET004.EXAM_TYPE = TREC.EXAM_TYPE ");
            stb.append("         AND TRDET004.RECEPTNO = TREC.RECEPTNO ");
            stb.append("         AND TRDET004.SEQ = '004' ");
            stb.append("     LEFT JOIN V_ENTEXAM_APPLICANTBASE_TESTDIV_DAT VATD ON VATD.ENTEXAMYEAR = TREC.ENTEXAMYEAR ");
            stb.append("                                    AND VATD.APPLICANTDIV = TREC.APPLICANTDIV ");
            stb.append("                                    AND VATD.TESTDIV = TREC.TESTDIV ");
            stb.append("                                    AND VATD.EXAMNO       = TREC.EXAMNO ");
            stb.append("                                    AND VATD.RECEPTNO       = TREC.RECEPTNO ");
            stb.append("     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT TADET002 ON TADET002.ENTEXAMYEAR = TREC.ENTEXAMYEAR ");
            stb.append("         AND TADET002.EXAMNO = TREC.EXAMNO ");
            stb.append("         AND TADET002.SEQ = '002' ");
            stb.append("     LEFT JOIN NAME_MST NML013 ON NML013.NAMECD1 = 'L013' ");
            stb.append("         AND NML013.NAMECD2 = TREC.JUDGEDIV ");
            stb.append("     LEFT JOIN RECRUIT_DAT TRECR ON TRECR.YEAR = TREC.ENTEXAMYEAR ");
            stb.append("         AND TRECR.RECRUIT_NO = TADET002.REMARK1 ");
            stb.append("     LEFT JOIN NAME_MST NML005 ON NML005.NAMECD1 = 'L005' ");
            stb.append("         AND NML005.NAMECD2 = VATD.EXAM_TYPE ");
            if ("1".equals(param._applicantdiv)) {
                stb.append("     LEFT JOIN NAME_MST NML024 ON NML024.NAMECD1 = 'L024' ");
                stb.append("         AND NML024.NAMECD2 = TREC.TESTDIV ");
            } else {
                stb.append("     LEFT JOIN NAME_MST NML004 ON NML004.NAMECD1 = 'L004' ");
                stb.append("         AND NML004.NAMECD2 = TREC.TESTDIV ");
            }
            stb.append(" WHERE ");
            stb.append("     TREC.ENTEXAMYEAR = '" + param._entexamyear + "' ");
            stb.append("     AND TREC.APPLICANTDIV = '" + param._applicantdiv + "' ");
            if (!"ALL".equals(param._testdiv)) {
                stb.append("     AND TREC.TESTDIV = '" + param._testdiv + "' ");
            }
            stb.append("     AND TREC.EXAM_TYPE = '1' ");
            if ("1".equals(param._applicantdiv)) {
                stb.append("         AND VALUE(NML024.ABBV2, '') <> '1' ");
            }
            stb.append(" ORDER BY ");
            stb.append(" TREC.RECEPTNO ");
            return stb.toString();
        }
    }

    private static class Param {
        final String _entexamyear;
        final String _applicantdiv;  // 1:中学
        final String _testdiv;

        final String _loginDate;
        final boolean _seirekiFlg;
        final String _applicantdivName;
        final String _testdivName;
        final List _subclassList;
        final Map _subclassLocationMap;

        Param(
                final DB2UDB db2,
                final HttpServletRequest request
        ) {
            _entexamyear  = request.getParameter("ENTEXAMYEAR");
            _applicantdiv = request.getParameter("APPLICANTDIV");
            _testdiv      = request.getParameter("TESTDIV");
            _loginDate    = request.getParameter("LOGIN_DATE");

            _seirekiFlg = getSeirekiFlg(db2);
            _applicantdivName = getApplicantDivname(db2);
            _testdivName = getTestDivname(db2);
//            log.debug(" entexam course = " + _entexamcourseList);
            _subclassList = getSubclassList(db2);
            _subclassLocationMap = getNameMstL009(db2);
        }

        private String getApplicantDivname(final DB2UDB db2) {
            final String sql = "SELECT NAMECD2, NAME1 FROM V_NAME_MST WHERE YEAR = '" + _entexamyear + "' AND NAMECD1 = 'L003' AND NAMECD2 = '" + _applicantdiv + "' ";
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString("NAME1");
                }
            } catch (Exception e) {
                log.error("exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }

        private String getTestDivname(final DB2UDB db2) {
            final String sql = "SELECT NAMECD2, NAME1 FROM V_NAME_MST WHERE YEAR = '" + _entexamyear + "' AND NAMECD1 = 'L024' AND NAMECD2 = '" + _testdiv + "' ";
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString("NAME1");
                }
            } catch (Exception e) {
                log.error("exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }

        /* 西暦表示にするのかのフラグ  */
        private boolean getSeirekiFlg(final DB2UDB db2) {
            final String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z012' AND NAMECD2 = '00' AND NAME1 IS NOT NULL ";
            boolean seirekiFlg = false;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    if (rs.getString("NAME1").equals("2")) seirekiFlg = true; //西暦
                }
            } catch (Exception e) {
                log.error("getSeirekiFlg Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return seirekiFlg;
        }

        private String gethiduke(final String inputDate) {
            // 西暦か和暦はフラグで判断
            String date;
            if (null != inputDate) {
                if (_seirekiFlg) {
                    date = inputDate.toString().substring(0, 4) + "年" + KNJ_EditDate.h_format_JP_MD(inputDate);
                } else {
                    date = KNJ_EditDate.h_format_JP(inputDate);
                }
                return date;
            }
            return null;
        }

        private List getSubclassList(final DB2UDB db2) {
            final String field = "1".equals(_applicantdiv) ? "NAME1"  : "NAME2";
            final StringBuffer stb = new StringBuffer();

            stb.append(" SELECT ");
            stb.append("     NAMECD2 AS SUBCLASSCD ");
            stb.append("     , " + field + " AS SUBCLASSNAME "); // 中学はNAME1
            stb.append("     , NAMESPARE1 AS LOCATION ");
            stb.append(" FROM ");
            stb.append("     V_NAME_MST ");
            stb.append(" WHERE ");
            stb.append("     YEAR        = '" + _entexamyear + "' AND ");
            stb.append("     NAMECD1     = 'L009' AND ");
            stb.append("     " + field + " IS NOT NULL ");
//            stb.append("     AND NAMECD2 IN (SELECT ");
//            stb.append("                     TESTSUBCLASSCD ");
//            stb.append("                 FROM ");
//            stb.append("                     ENTEXAM_PERFECT_EXAMTYPE_MST ");
//            stb.append("                 WHERE ");
//            stb.append("                     ENTEXAMYEAR     = '" + _entexamyear + "' ");
//            stb.append("                     AND APPLICANTDIV    = '" + _applicantdiv + "' ");
//            if (!"ALL".equals(_testdiv)) {
//                stb.append("                     AND TESTDIV         = '" + _testdiv + "' ");
//            }
//            stb.append("                 ) ");
            stb.append(" ORDER BY ");
            stb.append("     NAMECD2 ");

            return getList(db2, stb.toString());
        }


        private Map getNameMstL009(final DB2UDB db2) {
            final String field = "1".equals(_applicantdiv) ? "NAME1"  : "NAME2";
            final StringBuffer stb = new StringBuffer();

            stb.append(" SELECT ");
            stb.append("     NAMECD2 AS SUBCLASSCD ");
            stb.append("     , " + field + " AS SUBCLASSNAME "); // 中学はNAME1
            stb.append("     , NAMESPARE1 AS LOCATION ");
            stb.append(" FROM ");
            stb.append("     V_NAME_MST ");
            stb.append(" WHERE ");
            stb.append("     YEAR        = '" + _entexamyear + "' AND ");
            stb.append("     NAMECD1     = 'L009' AND ");
            stb.append("     " + field + " IS NOT NULL ");
            stb.append(" ORDER BY ");
            stb.append("     NAMECD2 ");

            return toMap(getList(db2, stb.toString()), "SUBCLASSCD", "LOCATION");
        }
    }
}//クラスの括り
