package servletpack.KNJL;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import servletpack.KNJZ.detail.KNJ_EditEdit;

/**
 *
 *    学校教育システム 賢者 [入試管理]
 *
 **/

public class KNJL344F {

    private static final Log log = LogFactory.getLog(KNJL344F.class);

    private boolean _hasData;
    private Param _param;

    private final String FROM_TO_MARK = "\uFF5E";

    public void svf_out(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

        Vrw32alp svf     = new Vrw32alp();   //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2       = null;             //Databaseクラスを継承したクラス

        log.fatal("$Revision: 65467 $ $Date: 2019-02-03 16:12:14 +0900 (日, 03 2 2019) $"); // CVSキーワードの取り扱いに注意

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

            if ("1".equals(_param._applicantdiv)) {
                printJ(svf, db2);
            } else if ("2".equals(_param._applicantdiv)) {
                printH(svf, db2);
            }

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

    /**
     * listを最大数ごとにグループ化したリストを得る
     * @param list
     * @param max 最大数
     * @return listを最大数ごとにグループ化したリスト
     */
    private static List getPageList(final List list, final int max) {
        final List rtn = new ArrayList();
        List current = null;
        for (final Iterator it = list.iterator(); it.hasNext();) {
            final Object o = it.next();
            if (null == current || current.size() >= max) {
                current = new ArrayList();
                rtn.add(current);
            }
            current.add(o);
        }
        return rtn;
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

    private void printH(final Vrw32alp svf, final DB2UDB db2) {
        final Map testdivTokeiMap = Tokei.getTestdivTokeiMap(db2, _param);

        final String form = "KNJL344F_H.frm";
        svf.VrSetForm(form, 1);

        svf.VrsOut("TITLE", KenjaProperties.gengou(Integer.parseInt(_param._entexamyear)) + "年度　" + StringUtils.defaultString(_param._applicantdivName) + "入試　応募状況"); // タイトル
        svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(db2, _param._loginDate)); // 日付

        for (int j = 0; j < _param._testdivList.size(); j++) {
            final int line = j + 1;
            final Map testdivMap = (Map) _param._testdivList.get(j);
            svf.VrsOut("EXAM_NAME" + String.valueOf(line), getString(testdivMap, "NAME1")); // 入試名称
        }

        Tokei total = new Tokei(null, null, null, null, null, null, null, null, null);
        for (int j = 0; j < _param._testdivList.size(); j++) {
            final int line = j + 1;
            final Map testdivMap = (Map) _param._testdivList.get(j);
            final String testdiv = getString(testdivMap, "NAMECD2");
            final Tokei tokei = (Tokei) testdivTokeiMap.get(testdiv);
            if (null != tokei) {
                svf.VrsOutn("APPLI_NUM", line, tokei._shigansha); // 志願者数
                svf.VrsOutn("EXAM_NUM", line, tokei._jukensha); // 受験者数
                svf.VrsOutn("PASS_NUM", line, tokei._goukakusha); // 合格者数
                svf.VrsOutn("PRO_NUM", line, tokei._tetsuzukisha); // 入学手続者数
                svf.VrsOutn("DEC_NUM", line, tokei._jitaisha); // 入学辞退者数
                svf.VrsOutn("ENT_NUM", line, tokei._nyugakusha); // 入学者実数
                total = total.addTokei(tokei);
            }
        }

        svf.VrsOut("TOTAL_APPLI_NUM", total._shigansha); // 志願者数総合計
        svf.VrsOut("TOTAL_EXAM_NUM", total._jukensha); // 受験者数総合計
        svf.VrsOut("TOTAL_PASS_NUM", total._goukakusha); // 合格者数総合計
        svf.VrsOut("TOTAL_PRO_NUM", total._tetsuzukisha); // 入学手続者数総合計
        svf.VrsOut("TOTAL_DEC_NUM", total._jitaisha); // 入学辞退者数総合計
        svf.VrsOut("TOTAL_ENT_NUM", total._nyugakusha); // 入学者実数総合計

        final List jitaisha = getJitaiHMapList(db2, _param);
        final List jitaishaColList = getPageList(jitaisha, 15);
        for (int i = 0; i < jitaishaColList.size(); i++) {
            final int col = i + 1;
            final List jitaishaList = (List) jitaishaColList.get(i);
            for (int j = 0; j < jitaishaList.size(); j++) {
                final int line = j + 1;
                final Map jitaiMap = (Map) jitaishaList.get(j);

                //svf.VrsOutn("DIV" + String.valueOf(col), line, null); // 経過番号
                svf.VrsOutn("EXAM_NO" + String.valueOf(col), line, getString(jitaiMap, "EXAMNO")); // 受験番号
                svf.VrsOutn("NAME" + String.valueOf(col), line, getString(jitaiMap, "NAME")); // 氏名
            }
        }

        svf.VrEndPage();
        _hasData = true;
    }

    private void printJ(final Vrw32alp svf, final DB2UDB db2) {
        final Map testdivTokeiMap = Tokei.getTestdivTokeiMap(db2, _param);

        final String form = "KNJL344F_J.frm";
        svf.VrSetForm(form, 1);

        svf.VrsOut("TITLE", KenjaProperties.gengou(Integer.parseInt(_param._entexamyear)) + "年度　" + StringUtils.defaultString(_param._applicantdivName) + "入試　応募状況"); // タイトル
        svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(db2, _param._loginDate)); // 日付

        for (int j = 0; j < _param._testdivList.size(); j++) {
            final int line = j + 1;
            final Map testdivMap = (Map) _param._testdivList.get(j);
            final String nameIdx = KNJ_EditEdit.getMS932ByteLength(getString(testdivMap, "NAME1")) > 8 ? "_2": "";
            svf.VrsOut("EXAM_NAME" + String.valueOf(line) + nameIdx, getString(testdivMap, "NAME1")); // 入試名称
        }

        Tokei total = new Tokei(null, null, null, null, null, null, null, null, null);
        for (int j = 0; j < _param._testdivList.size(); j++) {
            final int line = j + 1;
            final Map testdivMap = (Map) _param._testdivList.get(j);
            final String testdiv = getString(testdivMap, "NAMECD2");
            final Tokei tokei = (Tokei) testdivTokeiMap.get(testdiv);
            if (null != tokei) {
                svf.VrsOutn("APPLI_NUM", line, tokei._shigansha); // 志願者数
                svf.VrsOutn("EXAM_NUM", line, tokei._jukensha); // 受験者数
                svf.VrsOutn("PASS_NUM", line, tokei._goukakusha); // 合格者数
                svf.VrsOutn("PRO_NUM", line, tokei._tetsuzukisha); // 入学手続者数
                svf.VrsOutn("DEC_NUM", line, tokei._jitaisha); // 入学辞退者数
                svf.VrsOutn("ENT_NUM", line, tokei._nyugakusha); // 入学者実数
                total = total.addTokei(tokei);
            }
        }

        svf.VrsOut("TOTAL_APPLI_NUM", total._shigansha); // 志願者数総合計
        svf.VrsOut("TOTAL_EXAM_NUM", total._jukensha); // 受験者数総合計
        svf.VrsOut("TOTAL_PASS_NUM", total._goukakusha); // 合格者数総合計
        svf.VrsOut("TOTAL_PRO_NUM", total._tetsuzukisha); // 入学手続者数総合計
        svf.VrsOut("TOTAL_DEC_NUM", total._jitaisha); // 入学辞退者数総合計
        svf.VrsOut("TOTAL_ENT_NUM", total._nyugakusha); // 入学者実数総合計

        final List jitaisha = getJitaiJMapList(db2, _param);
        final List jitaishaColList = getPageList(jitaisha, 20);
        for (int i = 0; i < jitaishaColList.size(); i++) {
            final int col = i + 1;
            final List jitaishaList = (List) jitaishaColList.get(i);
            for (int j = 0; j < jitaishaList.size(); j++) {
                final int line = j + 1;
                final Map jitaiMap = (Map) jitaishaList.get(j);

                //svf.VrsOutn("DIV" + String.valueOf(col), line, null); // 経過番号
                svf.VrsOutn("EXAM_NO" + String.valueOf(col), line, getString(jitaiMap, "RECEPTNO")); // 受験番号
                svf.VrsOutn("NAME" + String.valueOf(col), line, getString(jitaiMap, "NAME")); // 氏名
            }
        }

        svf.VrEndPage();
        _hasData = true;
    }

    private static List getJitaiHMapList(final DB2UDB db2, final Param param) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T1.EXAMNO ");
        stb.append("     , T1.NAME ");
        stb.append(" FROM ENTEXAM_APPLICANTBASE_DAT T1 ");
        stb.append(" WHERE ");
        stb.append("     T1.ENTEXAMYEAR = '" + param._entexamyear + "' ");
        stb.append("     AND T1.APPLICANTDIV = '" + param._applicantdiv + "' ");
        stb.append("     AND T1.ENTDIV = '2' ");
        stb.append(" ORDER BY ");
        stb.append("     T1.EXAMNO ");

        return getList(db2, stb.toString());
    }


    private static List getJitaiJMapList(final DB2UDB db2, final Param param) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     T2.RECEPTNO ");
        stb.append("     , T1.NAME ");
        stb.append(" FROM ENTEXAM_APPLICANTBASE_DAT T1 ");
        stb.append(" INNER JOIN V_ENTEXAM_RECEPT_DAT T2 ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
        stb.append("     AND T2.APPLICANTDIV = T1.APPLICANTDIV ");
        stb.append("     AND T2.EXAMNO = T1.EXAMNO ");
        stb.append("     AND T2.JUDGEDIV = '1' ");
        stb.append("     AND (T2.TESTDIV <> '5' OR T2.TESTDIV = '5' AND VALUE(T1.GENERAL_FLG, '') <> '1') ");
        stb.append(" WHERE ");
        stb.append("     T1.ENTEXAMYEAR = '" + param._entexamyear + "' ");
        stb.append("     AND T1.APPLICANTDIV = '" + param._applicantdiv + "' ");
        stb.append("     AND T1.ENTDIV = '2' ");
        stb.append(" ORDER BY ");
        stb.append("     T2.RECEPTNO ");

        return getList(db2, stb.toString());
    }

    private static class Tokei {
        final String _entexamyear;
        final String _applicantdiv;
        final String _testdiv;
        final String _shigansha;
        final String _jukensha;
        final String _goukakusha;
        final String _tetsuzukisha;
        final String _jitaisha;
        final String _nyugakusha;

        Tokei(
            final String entexamyear,
            final String applicantdiv,
            final String testdiv,
            final String shigansha,
            final String jukensha,
            final String goukakusha,
            final String tetsuzukisha,
            final String jitaisha,
            final String nyugakusha
        ) {
            _entexamyear = entexamyear;
            _applicantdiv = applicantdiv;
            _testdiv = testdiv;
            _shigansha = shigansha;
            _jukensha = jukensha;
            _goukakusha = goukakusha;
            _tetsuzukisha = tetsuzukisha;
            _jitaisha = jitaisha;
            _nyugakusha = nyugakusha;
        }

        public Tokei addTokei(final Tokei tokei) {
            return new Tokei(null, null, null
                    , add(_shigansha, tokei._shigansha)
                    , add(_jukensha, tokei._jukensha)
                    , add(_goukakusha, tokei._goukakusha)
                    , add(_tetsuzukisha, tokei._tetsuzukisha)
                    , add(_jitaisha, tokei._jitaisha)
                    , add(_nyugakusha, tokei._nyugakusha)
                    );

        }

        public static Map getTestdivTokeiMap(final DB2UDB db2, final Param param) {
            final Map rtn = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = sql(param);
                log.debug(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String entexamyear = rs.getString("ENTEXAMYEAR");
                    final String applicantdiv = rs.getString("APPLICANTDIV");
                    final String testdiv = rs.getString("TESTDIV");
                    final String shigansha = rs.getString("SHIGANSHA");
                    final String jukensha = rs.getString("JUKENSHA");
                    final String goukakusha = rs.getString("GOUKAKUSHA");
                    final String tetsuzukisha = rs.getString("TETSUZUKISHA");
                    final String jitaisha = rs.getString("JITAISHA");
                    final String nyugakusha = rs.getString("NYUGAKUSHA");
                    final Tokei tokei = new Tokei(entexamyear, applicantdiv, testdiv, shigansha, jukensha, goukakusha, tetsuzukisha, jitaisha, nyugakusha);
                    rtn.put(testdiv, tokei);
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }

        public static String sql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            if ("1".equals(param._applicantdiv)) {
                stb.append(" WITH APPLICANTBASE_TESTDIV AS ( ");
                stb.append(" SELECT DISTINCT ");
                stb.append("     T1.ENTEXAMYEAR ");
                stb.append("     , T1.APPLICANTDIV ");
                stb.append("     , T1.EXAMNO ");
                stb.append("     , T1.TESTDIV ");
                stb.append("     , T1.RECEPTNO ");
                stb.append(" FROM V_ENTEXAM_APPLICANTBASE_TESTDIV_DAT T1 ");
                stb.append(" WHERE ");
                stb.append("     T1.ENTEXAMYEAR = '" + param._entexamyear + "' ");
                stb.append("     AND T1.APPLICANTDIV = '" + param._applicantdiv + "' ");
                stb.append(" ), T_MAIN AS ( ");
                stb.append(" SELECT  ");
                stb.append("     T1.ENTEXAMYEAR ");
                stb.append("     , T1.APPLICANTDIV ");
                stb.append("     , T1.EXAMNO ");
                stb.append("     , T1.TESTDIV ");
                stb.append("     , T1.RECEPTNO AS SHIGAN ");
                stb.append("     , T2.RECEPTNO AS RECEPT_DAT_RECEPTNO ");
                stb.append("     , T2.JUDGEDIV ");
                stb.append("     , APPBASE.PROCEDUREDIV ");
                stb.append("     , APPBASE.ENTDIV ");
                stb.append("     , APPBASE.GENERAL_FLG ");
                stb.append(" FROM APPLICANTBASE_TESTDIV T1 ");
                stb.append(" INNER JOIN ENTEXAM_APPLICANTBASE_DAT APPBASE ON APPBASE.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
                stb.append("     AND APPBASE.EXAMNO = T1.EXAMNO ");
                stb.append(" LEFT JOIN V_ENTEXAM_RECEPT_DAT T2 ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
                stb.append("     AND T2.APPLICANTDIV = APPBASE.APPLICANTDIV ");
                stb.append("     AND T2.TESTDIV = T1.TESTDIV ");
                stb.append("     AND T2.EXAMNO = APPBASE.EXAMNO ");
                stb.append("     AND T2.EXAM_TYPE = '1' ");
                stb.append(" ) ");
            } else if ("2".equals(param._applicantdiv)) {
                stb.append(" WITH T_MAIN AS ( ");
                stb.append(" SELECT  ");
                stb.append("     APPBASE.ENTEXAMYEAR ");
                stb.append("     , APPBASE.APPLICANTDIV ");
                stb.append("     , APPBASE.EXAMNO ");
                stb.append("     , CASE WHEN T2.TESTDIV IN ('4', '5') THEN '45' ELSE T2.TESTDIV END AS TESTDIV ");
                stb.append("     , APPBASE.EXAMNO AS SHIGAN ");
                stb.append("     , T2.RECEPTNO AS RECEPT_DAT_RECEPTNO ");
                stb.append("     , T2.JUDGEDIV ");
                stb.append("     , APPBASE.PROCEDUREDIV ");
                stb.append("     , APPBASE.ENTDIV ");
                stb.append("     , APPBASE.GENERAL_FLG ");
                stb.append(" FROM ENTEXAM_APPLICANTBASE_DAT APPBASE ");
                stb.append(" LEFT JOIN V_ENTEXAM_RECEPT_DAT T2 ON T2.ENTEXAMYEAR = APPBASE.ENTEXAMYEAR ");
                stb.append("     AND T2.APPLICANTDIV = APPBASE.APPLICANTDIV ");
                stb.append("     AND T2.EXAMNO = APPBASE.EXAMNO ");
                stb.append("     AND T2.EXAM_TYPE = '1' ");
                stb.append(" WHERE ");
                stb.append("     APPBASE.ENTEXAMYEAR = '" + param._entexamyear + "' ");
                stb.append("     AND APPBASE.APPLICANTDIV = '" + param._applicantdiv + "' ");
                stb.append(" ) ");
            }
            stb.append(" SELECT ");
            stb.append("     T1.ENTEXAMYEAR ");
            stb.append("     , T1.APPLICANTDIV ");
            stb.append("     , T1.TESTDIV ");
            if ("1".equals(param._applicantdiv)) {
                stb.append("     , COUNT(SHIGAN) AS SHIGANSHA ");
                stb.append("     , COUNT(CASE WHEN VALUE(JUDGEDIV, '') <> '4' THEN RECEPT_DAT_RECEPTNO END) AS JUKENSHA ");
                stb.append("     , COUNT(CASE WHEN (T1.TESTDIV <> '5' OR T1.TESTDIV = '5' AND VALUE(T1.GENERAL_FLG, '') <> '1') AND JUDGEDIV = '1' THEN RECEPT_DAT_RECEPTNO END) AS GOUKAKUSHA ");
                stb.append("     , COUNT(CASE WHEN (T1.TESTDIV <> '5' OR T1.TESTDIV = '5' AND VALUE(T1.GENERAL_FLG, '') <> '1') AND JUDGEDIV = '1' AND PROCEDUREDIV = '1' THEN RECEPT_DAT_RECEPTNO END) AS TETSUZUKISHA ");
                stb.append("     , COUNT(CASE WHEN (T1.TESTDIV <> '5' OR T1.TESTDIV = '5' AND VALUE(T1.GENERAL_FLG, '') <> '1') AND JUDGEDIV = '1' AND ENTDIV = '2' THEN RECEPT_DAT_RECEPTNO END) AS JITAISHA ");
                stb.append("     , COUNT(CASE WHEN (T1.TESTDIV <> '5' OR T1.TESTDIV = '5' AND VALUE(T1.GENERAL_FLG, '') <> '1') AND JUDGEDIV = '1' AND ENTDIV = '1' THEN RECEPT_DAT_RECEPTNO END) AS NYUGAKUSHA ");
            } else if ("2".equals(param._applicantdiv)) {
                stb.append("     , COUNT(CASE WHEN (T1.TESTDIV <> '3' OR T1.TESTDIV = '3' AND VALUE(T1.GENERAL_FLG, '') <> '1') THEN SHIGAN END) AS SHIGANSHA ");
                stb.append("     , COUNT(CASE WHEN (T1.TESTDIV <> '3' OR T1.TESTDIV = '3' AND VALUE(T1.GENERAL_FLG, '') <> '1') AND VALUE(JUDGEDIV, '') <> '4' THEN RECEPT_DAT_RECEPTNO END) AS JUKENSHA ");
                stb.append("     , COUNT(CASE WHEN (T1.TESTDIV <> '3' OR T1.TESTDIV = '3' AND VALUE(T1.GENERAL_FLG, '') <> '1') AND JUDGEDIV = '1' THEN RECEPT_DAT_RECEPTNO END) AS GOUKAKUSHA ");
                stb.append("     , COUNT(CASE WHEN (T1.TESTDIV <> '3' OR T1.TESTDIV = '3' AND VALUE(T1.GENERAL_FLG, '') <> '1') AND JUDGEDIV = '1' AND PROCEDUREDIV = '1' THEN RECEPT_DAT_RECEPTNO END) AS TETSUZUKISHA ");
                stb.append("     , COUNT(CASE WHEN (T1.TESTDIV <> '3' OR T1.TESTDIV = '3' AND VALUE(T1.GENERAL_FLG, '') <> '1') AND JUDGEDIV = '1' AND ENTDIV = '2' THEN RECEPT_DAT_RECEPTNO END) AS JITAISHA ");
                stb.append("     , COUNT(CASE WHEN (T1.TESTDIV <> '3' OR T1.TESTDIV = '3' AND VALUE(T1.GENERAL_FLG, '') <> '1') AND JUDGEDIV = '1' AND ENTDIV = '1' THEN RECEPT_DAT_RECEPTNO END) AS NYUGAKUSHA ");
            }
            stb.append(" FROM T_MAIN T1 ");
            stb.append(" GROUP BY ");
            stb.append("     T1.ENTEXAMYEAR ");
            stb.append("     , T1.APPLICANTDIV ");
            stb.append("     , T1.TESTDIV ");
            return stb.toString();
        }
    }

    private static class Param {
        final String _entexamyear;
        final String _applicantdiv;

        final String _loginDate;
        final boolean _seirekiFlg;
        final String _applicantdivName;
        final List _testdivList;

        Param(
                final DB2UDB db2,
                final HttpServletRequest request
        ) {
            _entexamyear  = request.getParameter("ENTEXAMYEAR");
            _applicantdiv = request.getParameter("APPLICANTDIV");
            _loginDate    = request.getParameter("LOGIN_DATE");

            _seirekiFlg = getSeirekiFlg(db2);
            _applicantdivName = getApplicantDivname(db2);
            _testdivList = getTestDivnameList(db2);
//            log.debug(" entexam course = " + _entexamcourseList);
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

        private List getTestDivnameList(final DB2UDB db2) {
            final String cd1 = "1".equals(_applicantdiv) ? "L024" : "L004";
            String sql = "";
            if ("1".equals(_applicantdiv)) {
                sql += "SELECT NAMECD2, NAME1 FROM V_NAME_MST WHERE YEAR = '" + _entexamyear + "' AND NAMECD1 = '" + cd1 + "' ORDER BY int(NAMESPARE3) ";
            } else {
                sql  = "SELECT NAMECD2, NAME1 FROM V_NAME_MST ";
                sql += " WHERE YEAR = '" + _entexamyear + "' AND NAMECD1 = '" + cd1 + "' AND VALUE(ABBV2, '') <> '1'  ";
                sql += " UNION ALL ";
                sql += "SELECT DISTINCT '45' AS NAMECD2, '帰国生' AS NAME1 FROM V_NAME_MST ";
                sql += " WHERE YEAR = '" + _entexamyear + "' AND NAMECD1 = '" + cd1 + "' AND VALUE(ABBV2, '') = '1'  ";
                sql += " ORDER BY NAMECD2 ";
            }
            return getList(db2, sql);
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
    }
}//クラスの括り
