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

public class KNJL331G {

    private static final Log log = LogFactory.getLog(KNJL331G.class);

    private boolean _hasData;
    private Param _param;
    private final int MAX_COURSE_CNT = 5;

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

    public void printMain(final DB2UDB db2, final Vrw32alp svf) {

        final Map tokeiHighLowHistMap = TokeiHighLowHist.getTokeiHighLowHistList(db2, _param);

        int listCnt = 1;
        for (Iterator itCourse = _param._courseMapList.iterator(); itCourse.hasNext();) {
            final List courseList = (List) itCourse.next();

            final String form = "KNJL331G.frm";
            svf.VrSetForm(form, 4);
            final String[] shdivs = {"1", "2"};
            for (int shdivi = 0; shdivi < shdivs.length; shdivi++) {
                final String shdiv = shdivs[shdivi];

                svf.VrsOut("DATE", KNJ_EditDate.h_format_JP(db2, _param._loginDate)); //
                svf.VrsOut("TITLE", KNJ_EditDate.gengou(db2, Integer.parseInt(_param._entexamyear)) + "年度　" + StringUtils.defaultString(_param._applicantdivname) + "　" + StringUtils.defaultString(_param._testdivname) + "　最高・最低・平均点一覧"); // タイトル
                svf.VrsOut("SUBTITLE", "（" + StringUtils.defaultString((String) _param._shDivNameMap.get(shdiv)) + "）"); // サブタイトル
                for (int subi = 0; subi < _param._subclassMapList.size(); subi++) {
                    final String ssubi = String.valueOf(subi + 1);
                    final Map subclassMap = (Map) _param._subclassMapList.get(subi);
                    final String subclassname = (String) subclassMap.get("NAME");

                    for (int crsi = 0; crsi < courseList.size(); crsi++) {
                        final String scrsi = String.valueOf(crsi + 1);

                        svf.VrsOut("SUBCLASS_NAME" + scrsi + "_" + ssubi, subclassname); // 科目名
                    }
                    svf.VrsOut("SUBCLASS_NAME6_" + ssubi, subclassname); // 科目名
                }
                for (int crsi = 0; crsi < courseList.size(); crsi++) {
                    final String scrsi = String.valueOf(crsi + 1);
                    final Map courseMap = (Map) courseList.get(crsi);
                    final String courseName = (String) courseMap.get("NAME");
                    svf.VrsOut("COURSE_NAME" + scrsi, courseName); // コース名
                }

                for (int nendoi = Integer.parseInt(_param._entexamyear) - 4; nendoi <= Integer.parseInt(_param._entexamyear); nendoi++) {

                    svf.VrsOut("NENDO", KNJ_EditDate.gengou(db2, nendoi) + "年度"); // 年度
                    for (int sexi = 0; sexi < 3; sexi++) {
                        final String ssexi = String.valueOf(sexi + 1);

                        for (int subi = 0; subi < _param._subclassMapList.size(); subi++) {
                            final String ssubi = String.valueOf(subi + 1);
                            final Map subclassMap = (Map) _param._subclassMapList.get(subi);
                            final String subclasscd = (String) subclassMap.get("CD");

                            for (int crsi = 0; crsi < courseList.size(); crsi++) {
                                final String scrsi = String.valueOf(crsi + 1);
                                final Map courseMap = (Map) courseList.get(crsi);
                                final String course = (String) courseMap.get("COURSE");
                                final String examcoursecd = StringUtils.split(course, "-")[2];

                                final TokeiHighLowHist hist = (TokeiHighLowHist) tokeiHighLowHistMap.get(TokeiHighLowHist.tokeiKey(String.valueOf(nendoi), shdiv, examcoursecd, ssexi, subclasscd));
                                if (null != hist) {
                                    svf.VrsOut("SCORE" + scrsi + "_" + ssubi + "_1_" + ssexi, hist._highscore); // 最高
                                    svf.VrsOut("SCORE" + scrsi + "_" + ssubi + "_2_" + ssexi, hist._lowscore); // 最低
                                    svf.VrsOut("SCORE" + scrsi + "_" + ssubi + "_3_" + ssexi, sishagonyu(hist._avg, 0)); // 平均
                                }
                            }

                            final TokeiHighLowHist histAll = (TokeiHighLowHist) tokeiHighLowHistMap.get(TokeiHighLowHist.tokeiKey(String.valueOf(nendoi), shdiv, "ALL", ssexi, subclasscd));
                            if (null != histAll && _param._courseMapList.size() == listCnt) {
                                svf.VrsOut("AVE" + ssubi + "_" + ssexi, sishagonyu(histAll._avg, 1)); // 平均
                            }
                        }
                    }
                    svf.VrEndRecord();
                    _hasData = true;
                }
            }
            listCnt++;
        }
    }

    private static String sishagonyu(final String num, final int scale) {
        if (NumberUtils.isNumber(num)) {
            return new BigDecimal(num).setScale(scale, BigDecimal.ROUND_HALF_UP).toString();
        }
        return null;
    }

    private static class TokeiHighLowHist {
        String _entexamyear;
        String _shdiv;
        String _coursecode;
        String _sex;
        String _testsubclasscd;
        String _highscore;
        String _lowscore;
        String _avg;
        String _total;
        String _count;

        public static String tokeiKey(final String entexamyear, final String shdiv, final String coursecode, final String sex, final String testsubclasscd) {
            return entexamyear + "-" + shdiv + "-" + coursecode + "-" + sex + "-" + testsubclasscd;
        }

        public static Map getTokeiHighLowHistList(final DB2UDB db2, final Param param) {
            final Map rtn = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = sql(param);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final TokeiHighLowHist tokeihighlowhist = new TokeiHighLowHist();
                    tokeihighlowhist._entexamyear = rs.getString("ENTEXAMYEAR");
                    tokeihighlowhist._shdiv = rs.getString("SHDIV");
                    tokeihighlowhist._coursecode = rs.getString("COURSECODE");
                    tokeihighlowhist._sex = rs.getString("SEX");
                    tokeihighlowhist._testsubclasscd = rs.getString("TESTSUBCLASSCD");
                    tokeihighlowhist._highscore = rs.getString("HIGHSCORE");
                    tokeihighlowhist._lowscore = rs.getString("LOWSCORE");
                    tokeihighlowhist._avg = rs.getString("AVG");
                    tokeihighlowhist._total = rs.getString("TOTAL");
                    tokeihighlowhist._count = rs.getString("COUNT");
                    rtn.put(tokeiKey(tokeihighlowhist._entexamyear, tokeihighlowhist._shdiv, tokeihighlowhist._coursecode, tokeihighlowhist._sex, tokeihighlowhist._testsubclasscd), tokeihighlowhist);
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
            stb.append(" SELECT ");
            stb.append("     T1.ENTEXAMYEAR ");
            stb.append("   , T1.SHDIV ");
            stb.append("   , T1.COURSECODE ");
            stb.append("   , T1.SEX ");
            stb.append("   , T1.TESTSUBCLASSCD ");
            stb.append("   , T1.HIGHSCORE ");
            stb.append("   , T1.LOWSCORE ");
            stb.append("   , T1.AVG ");
            stb.append("   , T1.TOTAL ");
            stb.append("   , T1.COUNT ");
            stb.append(" FROM ENTEXAM_TOKEI_HIGH_LOW_HISTORY_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.APPLICANTDIV = '" + param._applicantdiv + "' ");
            stb.append("     AND T1.TESTDIV = '" + param._testdiv + "' ");
            // 男女計
            stb.append(" UNION ALL ");
            stb.append(" SELECT ");
            stb.append("     T1.ENTEXAMYEAR ");
            stb.append("   , T1.SHDIV ");
            stb.append("   , T1.COURSECODE ");
            stb.append("   , '3' AS SEX ");
            stb.append("   , T1.TESTSUBCLASSCD ");
            stb.append("   , MAX(T1.HIGHSCORE) AS HIGHSCORE ");
            stb.append("   , MIN(T1.LOWSCORE) AS LOWSCORE ");
            stb.append("   , SUM(T1.TOTAL) * 1.0 / SUM(T1.COUNT) AS AVG ");
            stb.append("   , SUM(T1.TOTAL) AS TOTAL ");
            stb.append("   , SUM(T1.COUNT) AS COUNT ");
            stb.append(" FROM ENTEXAM_TOKEI_HIGH_LOW_HISTORY_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.APPLICANTDIV = '" + param._applicantdiv + "' ");
            stb.append("     AND T1.TESTDIV = '" + param._testdiv + "' ");
            stb.append("     AND T1.SEX IN ('1', '2') ");
            stb.append(" GROUP BY ");
            stb.append("     T1.ENTEXAMYEAR ");
            stb.append("   , T1.SHDIV ");
            stb.append("   , T1.COURSECODE ");
            stb.append("   , T1.TESTSUBCLASSCD ");
            // コース計
            stb.append(" UNION ALL ");
            stb.append(" SELECT ");
            stb.append("     T1.ENTEXAMYEAR ");
            stb.append("   , T1.SHDIV ");
            stb.append("   , 'ALL' AS COURSECODE ");
            stb.append("   , T1.SEX ");
            stb.append("   , T1.TESTSUBCLASSCD ");
            stb.append("   , MAX(T1.HIGHSCORE) AS HIGHSCORE ");
            stb.append("   , MIN(T1.LOWSCORE) AS LOWSCORE ");
            stb.append("   , SUM(T1.TOTAL) * 1.0 / SUM(T1.COUNT) AS AVG ");
            stb.append("   , SUM(T1.TOTAL) AS TOTAL ");
            stb.append("   , SUM(T1.COUNT) AS COUNT ");
            stb.append(" FROM ENTEXAM_TOKEI_HIGH_LOW_HISTORY_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.APPLICANTDIV = '" + param._applicantdiv + "' ");
            stb.append("     AND T1.TESTDIV = '" + param._testdiv + "' ");
            stb.append("     AND T1.SEX IN ('1', '2') ");
            stb.append(" GROUP BY ");
            stb.append("     T1.ENTEXAMYEAR ");
            stb.append("   , T1.SHDIV ");
            stb.append("   , T1.SEX ");
            stb.append("   , T1.TESTSUBCLASSCD ");
            // コース男女計
            stb.append(" UNION ALL ");
            stb.append(" SELECT ");
            stb.append("     T1.ENTEXAMYEAR ");
            stb.append("   , T1.SHDIV ");
            stb.append("   , 'ALL' AS COURSECODE ");
            stb.append("   , '3' AS SEX ");
            stb.append("   , T1.TESTSUBCLASSCD ");
            stb.append("   , MAX(T1.HIGHSCORE) AS HIGHSCORE ");
            stb.append("   , MIN(T1.LOWSCORE) AS LOWSCORE ");
            stb.append("   , SUM(T1.TOTAL) * 1.0 / SUM(T1.COUNT) AS AVG ");
            stb.append("   , SUM(T1.TOTAL) AS TOTAL ");
            stb.append("   , SUM(T1.COUNT) AS COUNT ");
            stb.append(" FROM ENTEXAM_TOKEI_HIGH_LOW_HISTORY_DAT T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.APPLICANTDIV = '" + param._applicantdiv + "' ");
            stb.append("     AND T1.TESTDIV = '" + param._testdiv + "' ");
            stb.append("     AND T1.SEX IN ('1', '2') ");
            stb.append(" GROUP BY ");
            stb.append("     T1.ENTEXAMYEAR ");
            stb.append("   , T1.SHDIV ");
            stb.append("   , T1.TESTSUBCLASSCD ");


            return stb.toString();
        }
    }

    private class Param {
        final String _entexamyear;
        final String _loginDate;
        final String _applicantdiv;
        final String _testdiv;

        final String _applicantdivname;
        final String _testdivname;
        final Map _shDivNameMap;
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

            _seirekiFlg = getSeirekiFlg(db2);
            _applicantdivname = getApplicantdivName(db2);
            _testdivname = getTestdivName(db2);
            _shDivNameMap = getShDivNameMap(db2);
            _subclassMapList = getSubclassMapList(db2);
            _courseMapList = getCourseMapList(db2);
        }

        private String getApplicantdivName(DB2UDB db2) {
            String rtn = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = " SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'L003' AND NAMECD2 = '" + _applicantdiv + "'";
                ps = db2.prepareStatement(sql);
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
                final String sql = " SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'L004' AND NAMECD2 = '" + _testdiv + "'";
                ps = db2.prepareStatement(sql);
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

        private Map getShDivNameMap(DB2UDB db2) {
            Map rtn = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = " SELECT NAMECD2, NAME1 FROM V_NAME_MST WHERE YEAR = '" + _entexamyear + "' AND NAMECD1 = 'L006' ORDER BY NAMECD2 ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn.put(rs.getString("NAMECD2"), rs.getString("NAME1"));
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
                final String sql = " SELECT NAMECD2, NAME1 FROM V_NAME_MST WHERE YEAR = '" + _entexamyear + "' AND NAMECD1 = 'L009' ORDER BY NAMECD2 ";
                ps = db2.prepareStatement(sql);
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
                final String sql = " SELECT COURSECD || '-' || MAJORCD || '-' || EXAMCOURSECD AS COURSE, EXAMCOURSE_NAME, EXAMCOURSE_ABBV FROM ENTEXAM_COURSE_MST WHERE ENTEXAMYEAR = '" + _entexamyear + "' AND APPLICANTDIV = '" + _applicantdiv + "' AND TESTDIV = '" + _testdiv + "' ORDER BY COURSECD, MAJORCD, EXAMCOURSECD ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                int cnt = 1;
                List setList = new ArrayList();
                while (rs.next()) {
                    if (cnt > MAX_COURSE_CNT) {
                        rtn.add(setList);
                        cnt = 1;
                        setList = new ArrayList();
                    }
                    final Map m = new HashMap();
                    m.put("COURSE", rs.getString("COURSE"));
                    m.put("NAME", rs.getString("EXAMCOURSE_NAME"));
                    m.put("ABBV", rs.getString("EXAMCOURSE_ABBV"));
                    setList.add(m);
                    cnt++;
                }
                rtn.add(setList);
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
