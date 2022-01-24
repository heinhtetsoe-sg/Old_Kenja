package servletpack.KNJL;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;


/**
 *
 *    学校教育システム 賢者 [入試管理]
 *
 **/

public class KNJL332G {

    private static final Log log = LogFactory.getLog(KNJL332G.class);

    private boolean _hasData;
    private Param _param;

    public void svf_out(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

        log.fatal("$Revision: 65767 $ $Date: 2019-02-18 19:41:20 +0900 (月, 18 2 2019) $"); // CVSキーワードの取り扱いに注意

        Vrw32alp svf     = new Vrw32alp();   //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2       = null;             //Databaseクラスを継承したクラス

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

            //SVF出力
            printMain(db2, svf); //帳票出力のメソッド

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

    private String getVal(String val) {
        //if (null == val) return "0";
        return val;
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        final String[] shdivs = {"1", "2"};

        final Map scoreListMap = TestScore.getTestScoreListMap(db2, _param);

        final String form = "KNJL332G.frm";
        svf.VrSetForm(form, 1);

        svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(db2, _param._loginDate)); //
//        svf.VrsOut("PAGE", null); // ページ
        svf.VrsOut("TITLE", KNJ_EditDate.gengou(db2, Integer.parseInt(_param._entexamyear)) + "年度　" + StringUtils.defaultString(_param._applicantdivname) + "　" + StringUtils.defaultString(_param._testdivname) + "　得点率分布一覧"); // タイトル
        for (int crsi = 0; crsi < _param._courseMapList.size(); crsi++) {
            final String scrsi = String.valueOf(crsi + 1);
            final Map courseMap = (Map) _param._courseMapList.get(crsi);
            final String course = (String) courseMap.get("COURSE");
            final String courseName = (String) courseMap.get("NAME");
            svf.VrsOut("COURSE_NAME" + scrsi, courseName); // コース名
            svf.VrsOut("EXAM_NUM" + scrsi, _param._testdivname); // 選考次

            for (int shdivi = 0; shdivi < shdivs.length; shdivi++) {
                final String shdiv = shdivs[shdivi];

                for (int subi = 0; subi < _param._subclassMapList.size(); subi++) {
                    final String ssubi = String.valueOf(subi + 1);
                    final Map subclassMap = (Map) _param._subclassMapList.get(subi);
                    final String subclasscd = (String) subclassMap.get("CD");
                    final String subclassname = (String) subclassMap.get("NAME");

                    printDist(svf, scoreListMap, course, scrsi, shdiv, ssubi, subclasscd, subclassname);
                }

                printDist(svf, scoreListMap, course, scrsi, shdiv, "6", "SUBCLASSCD_AVG", "平均");
            }
        }

        svf.VrEndPage();
        _hasData = true;
    }

    // 科目の得点分布を印字
    private void printDist(final Vrw32alp svf, final Map scoreListMap, final String course, final String scrsi, final String shdiv, final String ssubi, final String subclasscd, final String subclassname) {

        svf.VrsOut("SUBCLASS_NAME" + scrsi + "_" + shdiv + "_" + ssubi, subclassname); // 科目名

        final List scoreList = (List) scoreListMap.get(TestScore.testscoreKey(course, shdiv, subclasscd));
        if (null == scoreList) {
            return;
        }
        final int subCount = scoreList.size();

        for (int ranki = 0; ranki < 21; ranki++) {
            final int low = ranki == 0 ? 0 : (ranki - 1) * 5 + 1;   // 0, 1,  6, 11, 16, ... 91, 96
            final int high = ranki * 5;                             // 0, 5, 10, 15, 20, ... 95, 100

            int count = 0;
            for (final Iterator it = scoreList.iterator(); it.hasNext();) {
                final TestScore testScore = (TestScore) it.next();
                if (NumberUtils.isNumber(testScore._score)) {
                    final double dscore;
                    if ("SUBCLASSCD_AVG".equals(subclasscd)) {
                        dscore = new BigDecimal(testScore._score).setScale(0, BigDecimal.ROUND_HALF_UP).doubleValue();
                    } else {
                        dscore = Double.parseDouble(testScore._score);
                    }
                    if (low <= dscore && dscore <= high) {
                        count += 1;
                        it.remove();
                    }
                }
            }
            if (count > 0) {
                svf.VrsOutn("SCORE" + scrsi + "_" + shdiv + "_" + ssubi, ranki + 1, String.valueOf(count)); // 分布人数
            }
        }

        svf.VrsOutn("SCORE" + scrsi + "_" + shdiv + "_" + ssubi, 22, String.valueOf(subCount)); // 分布人数 小計
    }

    private static class TestScore {
        String _examno;
        String _shdiv;
        String _course;
        String _receptno;
        String _testsubclasscd;
        String _score;

        private static String testscoreKey(final String course, final String shdiv, final String testsubclasscd) {
            return course + "-" + shdiv + "-" + testsubclasscd;
        }

        private static Map getTestScoreListMap(final DB2UDB db2, final Param param) {
            final Map rtn = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = sql(param);
                log.debug(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final TestScore testscore = new TestScore();
                    testscore._examno = rs.getString("EXAMNO");
                    testscore._shdiv = rs.getString("SHDIV");
                    testscore._course = rs.getString("COURSE");
                    testscore._receptno = rs.getString("RECEPTNO");
                    testscore._testsubclasscd = rs.getString("TESTSUBCLASSCD");
                    testscore._score = rs.getString("SCORE");

                    final String key = testscoreKey(testscore._course, testscore._shdiv, testscore._testsubclasscd);
                    if (null == rtn.get(key)) {
                        rtn.put(key, new ArrayList());
                    }
                    final List scoreList = (List) rtn.get(key);
                    scoreList.add(testscore);
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            try {
                final String sqlAvg = sqlAvg(param);
                log.debug(" sqlAvg = " + sqlAvg);
                ps = db2.prepareStatement(sqlAvg);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final TestScore testscore = new TestScore();
                    testscore._examno = rs.getString("EXAMNO");
                    testscore._shdiv = rs.getString("SHDIV");
                    testscore._course = rs.getString("COURSE");
                    testscore._receptno = rs.getString("RECEPTNO");
                    testscore._testsubclasscd = "SUBCLASSCD_AVG";
                    testscore._score = rs.getString("AVARAGE4");

                    final String key = testscoreKey(testscore._course, testscore._shdiv, testscore._testsubclasscd);
                    if (null == rtn.get(key)) {
                        rtn.put(key, new ArrayList());
                    }
                    final List scoreList = (List) rtn.get(key);
                    scoreList.add(testscore);
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }

        private static String sql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     VBASE.EXAMNO ");
            stb.append("   , VBASE.SHDIV ");
            stb.append("   , VBASE.DAI1_COURSECD || VBASE.DAI1_MAJORCD || VBASE.DAI1_COURSECODE AS COURSE ");
            stb.append("   , TREC.RECEPTNO ");
            stb.append("   , TSCORE.TESTSUBCLASSCD ");
            stb.append("   , TSCORE.SCORE ");
            stb.append(" FROM V_ENTEXAM_APPLICANTBASE_DAT VBASE ");
            stb.append(" INNER JOIN ENTEXAM_RECEPT_DAT TREC ON TREC.ENTEXAMYEAR = VBASE.ENTEXAMYEAR ");
            stb.append("    AND TREC.APPLICANTDIV = VBASE.APPLICANTDIV ");
            stb.append("    AND TREC.EXAMNO = VBASE.EXAMNO ");
            stb.append("    AND TREC.EXAM_TYPE = '1' ");
            stb.append(" INNER JOIN ENTEXAM_SCORE_DAT TSCORE ON TSCORE.ENTEXAMYEAR = VBASE.ENTEXAMYEAR ");
            stb.append("    AND TSCORE.APPLICANTDIV = VBASE.APPLICANTDIV ");
            stb.append("    AND TSCORE.TESTDIV = VBASE.TESTDIV ");
            stb.append("    AND TSCORE.EXAM_TYPE = TREC.EXAM_TYPE ");
            stb.append("    AND TSCORE.RECEPTNO = TREC.RECEPTNO ");
            stb.append(" WHERE ");
            stb.append("     VBASE.ENTEXAMYEAR = '" + param._entexamyear + "' ");
            stb.append("     AND VBASE.APPLICANTDIV = '" + param._applicantdiv + "' ");
            stb.append("     AND VBASE.TESTDIV = '" + param._testdiv + "' ");
            stb.append("     AND TSCORE.SCORE IS NOT NULL ");
            if (param._isSpecialReason) {
                stb.append("     AND VBASE.SPECIAL_REASON_DIV IS NOT NULL ");
            }

            return stb.toString();
        }

        private static String sqlAvg(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     VBASE.EXAMNO ");
            stb.append("   , VBASE.SHDIV ");
            stb.append("   , VBASE.DAI1_COURSECD || VBASE.DAI1_MAJORCD || VBASE.DAI1_COURSECODE AS COURSE ");
            stb.append("   , TREC.RECEPTNO ");
            stb.append("   , TREC.AVARAGE4 ");
            stb.append(" FROM V_ENTEXAM_APPLICANTBASE_DAT VBASE ");
            stb.append(" INNER JOIN ENTEXAM_RECEPT_DAT TREC ON TREC.ENTEXAMYEAR = VBASE.ENTEXAMYEAR ");
            stb.append("    AND TREC.APPLICANTDIV = VBASE.APPLICANTDIV ");
            stb.append("    AND TREC.EXAMNO = VBASE.EXAMNO ");
            stb.append("    AND TREC.EXAM_TYPE = '1' ");
            stb.append(" WHERE ");
            stb.append("     VBASE.ENTEXAMYEAR = '" + param._entexamyear + "' ");
            stb.append("     AND VBASE.APPLICANTDIV = '" + param._applicantdiv + "' ");
            stb.append("     AND VBASE.TESTDIV = '" + param._testdiv + "' ");
            stb.append("     AND TREC.AVARAGE4 IS NOT NULL ");
            if (param._isSpecialReason) {
                stb.append("     AND VBASE.SPECIAL_REASON_DIV IS NOT NULL ");
            }

            return stb.toString();
        }
    }

    private static class Param {
        final String _entexamyear;
        final String _loginDate;
        final String _applicantdiv;
        final String _testdiv;
        final boolean _isSpecialReason;

        final String _applicantdivname;
        final String _testdivname;
        final List _subclassMapList;
        final List _courseMapList;
        final  boolean _seirekiFlg;

        Param(
                final DB2UDB db2,
                final HttpServletRequest request
        ) {
            _entexamyear  = request.getParameter("YEAR");
            _applicantdiv = request.getParameter("APPLICANTDIV");
            _testdiv      = request.getParameter("TESTDIV");
            _loginDate    = request.getParameter("LOGIN_DATE");
            _isSpecialReason = "1".equals(request.getParameter("SPECIAL_REASON_DIV"));

            _seirekiFlg = getSeirekiFlg(db2);
            _applicantdivname = getApplicantdivName(db2);
            _testdivname = getTestdivName(db2);
            _subclassMapList = getSubclassMapList(db2);
            _courseMapList = getCourseMapList(db2);
        }

        private String getApplicantdivName(DB2UDB db2) {
            String rtn = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'L003' AND NAMECD2 = '" + _applicantdiv + "'");
                rs = ps.executeQuery();
                if (rs.next() && null != rs.getString("NAME1")) {
                  rtn = rs.getString("NAME1");
                }
            } catch (SQLException e) {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }

        private String getTestdivName(DB2UDB db2) {
            String rtn = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'L004' AND NAMECD2 = '" + _testdiv + "'");
                rs = ps.executeQuery();
                if (rs.next() && null != rs.getString("NAME1")) {
                  rtn = rs.getString("NAME1");
                }
            } catch (SQLException e) {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }


        private List getSubclassMapList(DB2UDB db2) {
            List rtn = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT NAMECD2, NAME1 FROM V_NAME_MST WHERE YEAR = '" + _entexamyear + "' AND NAMECD1 = 'L009' ORDER BY NAMECD2 ");
                rs = ps.executeQuery();
                while (rs.next()) {
                    final Map m = new HashMap();
                    m.put("NAME", rs.getString("NAME1"));
                    m.put("CD", rs.getString("NAMECD2"));
                    rtn.add(m);
                }
            } catch (SQLException e) {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }

        private List getCourseMapList(DB2UDB db2) {
            List rtn = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(" SELECT COURSECD || MAJORCD || EXAMCOURSECD AS COURSE, EXAMCOURSE_NAME, EXAMCOURSE_ABBV FROM ENTEXAM_COURSE_MST WHERE ENTEXAMYEAR = '" + _entexamyear + "' AND APPLICANTDIV = '" + _applicantdiv + "' AND TESTDIV = '" + _testdiv + "' ORDER BY COURSECD, MAJORCD, EXAMCOURSECD ");
                rs = ps.executeQuery();
                while (rs.next()) {
                    final Map m = new HashMap();
                    m.put("COURSE", rs.getString("COURSE"));
                    m.put("NAME", rs.getString("EXAMCOURSE_NAME"));
                    m.put("ABBV", rs.getString("EXAMCOURSE_ABBV"));
                    rtn.add(m);
                }
            } catch (SQLException e) {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtn;
        }

        /* 西暦表示にするのかのフラグ  */
        private boolean getSeirekiFlg(final DB2UDB db2) {
            final String sql = "SELECT NAME1 FROM NAME_MST WHERE NAMECD1='Z012' AND NAMECD2='00' AND NAME1 IS NOT NULL ";
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

    }
}//クラスの括り
