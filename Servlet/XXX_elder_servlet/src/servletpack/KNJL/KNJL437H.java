//kanji=漢字
/*
 *
 * 作成日: 2021/02/25
 * 作成者: ishimine
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJL;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
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
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;

/**
*
*   ＜ＫＮＪＬ４３７Ｈ 教科別得点平均 中学＞
*/

public class KNJL437H {

    private static final Log log = LogFactory.getLog("KNJL437H.class");

    private boolean _hasData;

    private Param _param;

    private static final String SGZ        = "510001"; //スーパーグローバルZENコースコード
    private static final String DISCOVERY  = "510002"; //ディスカバリーコースコード
    private static final String TEST1  = "1"; //TESTDIV 第1回
    private static final String TEST2  = "2"; //TESTDIV 第2回

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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        final Map scoreMap = getScoreMap(db2); //入試得点Map
        if (scoreMap.isEmpty()) {
            return;
        }

        svf.VrSetForm("KNJL437H.frm", 1);
        svf.VrsOut("TITLE", _param._examName); //タイトル
        svf.VrsOut("NENDO1", _param._examYear + "年度"); //今年度
        svf.VrsOut("NENDO2", _param._zennenYear + "年度"); //前年度
        //コース名
        printCourse(svf, SGZ, 1);
        printCourse(svf, DISCOVERY, 2);

        final String[] yearList = {_param._examYear, _param._zennenYear}; //年度ループ用
        final String[] courseList = {SGZ, DISCOVERY}; //コースループ用
        final String[] testDivList = {TEST1, TEST2}; //入試回数ループ用
        int cntYear = 1; //繰り返し回数 年度

        //今年度、前年度の科目名
        for (final String yearSub : yearList) {
            final Map subclassMap = (Map) _param._testSubclassMap.get(yearSub);
            if (subclassMap != null) {
                int cntSubclass = 1;
                for (Iterator ite = subclassMap.keySet().iterator(); ite.hasNext();) {
                    final String subbclassCd = (String) ite.next();
                    final String subclassName = (String) subclassMap.get(subbclassCd);
                    svf.VrsOut("CLASS_NAME" + cntYear + "_" + cntSubclass, subclassName);
                    cntSubclass++;
                }
            }
            cntYear++;
        }

        cntYear = 1;

        //年度毎のループ
        for (final String year : yearList) {
            int cntTestDiv = 1; //年度間の繰り返し回数 入試回数

            //コース毎のループ
            for (final String course : courseList) {

                //入試回数毎のループ
                for (final String testDiv : testDivList) {
                    final String key = year + "-" + course + "-" + testDiv;
                    int cntSubclass = 1; //繰り返し回数 科目毎

                    if (scoreMap.containsKey(key)) {
                        final Map printScoreMap = (Map) scoreMap.get(key);

                        //科目毎のループ
                        for (Iterator ite = printScoreMap.keySet().iterator(); ite.hasNext();) {
                            final String subclassCd = (String) ite.next();
                            final Score score = (Score) printScoreMap.get(subclassCd);

                            if (score != null) {
                                final String field = "TOTAL".equals(subclassCd) ? "4" : String.valueOf(cntSubclass);
                                svf.VrsOutn("MAX" + cntYear + "_" + field, cntTestDiv, score._max); //最高点
                                svf.VrsOutn("MIN" + cntYear + "_" + field, cntTestDiv, score._min); //最低点
                                svf.VrsOutn("AVE" + cntYear + "_" + field, cntTestDiv, score._avg); //平均点
                                _hasData = true;
                            }
                            cntSubclass++;
                        }
                    }
                    cntTestDiv++;
                }
            }
            cntYear++;
        }
        svf.VrEndPage();
    }

    private void printCourse(final Vrw32alp svf, final String courseCd, final int gyo) {
        final String courseName = (String) _param._courseMap.get(courseCd);
        final int keta = KNJ_EditEdit.getMS932ByteLength(courseName);
        if (keta <= 18) {
            svf.VrsOutn("COURSE_NAME1", gyo, courseName);
        } else {
            svf.VrsOutn("COURSE_NAME1", gyo, courseName.substring(0,9));
            svf.VrsOutn("COURSE_NAME2", gyo, courseName.substring(9,courseName.length()));
        }
    }

    private Map getScoreMap(final DB2UDB db2) throws SQLException {
        Map retMap = new LinkedMap();
        PreparedStatement ps = null;
        ResultSet rs = null;
        final String sql = getScoreSql();
        log.debug(" score sql =" + sql);

        try{
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String year = rs.getString("ENTEXAMYEAR");
                final String testDiv = rs.getString("TESTDIV");
                final String remark1 = rs.getString("REMARK1");
                final String subclassCd = rs.getString("TESTSUBCLASSCD");
                final String max = rs.getString("MAX");
                final String min = rs.getString("MIN");
                final String avg = rs.getString("AVG");

                final String key = year + "-" + remark1 + "-" + testDiv;

                if (!retMap.containsKey(key)) {
                    retMap.put(key, new LinkedMap());
                }

                final Map scoreMap = (Map) retMap.get(key);
                final Score score = new Score(max, min, avg);
                scoreMap.put(subclassCd, score);
            }
        } catch (final SQLException e) {
            log.error("入試得点取得でエラー", e);
            throw e;
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }

        return retMap;
    }

    //入試得点取得 平均は小数点第3位四捨五入
    private String getScoreSql() {
        final StringBuffer stb = new StringBuffer();
        //個人の科目毎成績
        stb.append(" WITH BASE AS ( ");
        stb.append(" SELECT ");
        stb.append("     SCORE.ENTEXAMYEAR, ");
        stb.append("     SCORE.APPLICANTDIV, ");
        stb.append("     SCORE.TESTDIV, ");
        stb.append("     SCORE.EXAM_TYPE, ");
        stb.append("     SCORE.RECEPTNO, ");
        stb.append("     SCORE.TESTSUBCLASSCD, ");
        stb.append("     SCORE.ATTEND_FLG, ");
        stb.append("     SCORE.SCORE, ");
        stb.append("     RD016.REMARK1, ");
        stb.append("     RD016.REMARK2 ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_SCORE_DAT SCORE ");
        stb.append(" LEFT JOIN ");
        stb.append("     ENTEXAM_RECEPT_DETAIL_DAT RD016 ");
        stb.append("      ON RD016.ENTEXAMYEAR = SCORE.ENTEXAMYEAR ");
        stb.append("     AND RD016.APPLICANTDIV = SCORE.APPLICANTDIV ");
        stb.append("     AND RD016.TESTDIV = SCORE.TESTDIV ");
        stb.append("     AND RD016.EXAM_TYPE = SCORE.EXAM_TYPE ");
        stb.append("     AND RD016.RECEPTNO = SCORE.RECEPTNO ");
        stb.append("     AND RD016.SEQ = '016' ");
        stb.append(" WHERE ");
        stb.append("     SCORE.ENTEXAMYEAR IN ('" + _param._zennenYear + "', '" + _param._examYear + "') AND ");
        stb.append("     SCORE.APPLICANTDIV = '" + _param._applicantDiv + "' AND ");
        stb.append("     SCORE.EXAM_TYPE = '" + _param._examType + "' AND ");
        stb.append("     SCORE.TESTDIV IN ('" + TEST1 + "','" + TEST2 + "') AND "); //第一回、第二回が対象
        stb.append("     RD016.REMARK1 IN ('" + SGZ + "'," + DISCOVERY + ") ");
        stb.append(" ORDER BY ");
        stb.append("     SCORE.ENTEXAMYEAR, SCORE.TESTDIV, RD016.REMARK1, SCORE.RECEPTNO ");
        //個人の合計
        stb.append(" ), TOTAL AS ( ");
        stb.append(" SELECT ");
        stb.append("     ENTEXAMYEAR, ");
        stb.append("     TESTDIV, ");
        stb.append("     EXAM_TYPE, ");
        stb.append("     RECEPTNO, ");
        stb.append("     REMARK1, ");
        stb.append("     REMARK2, ");
        stb.append("     SUM(SCORE) AS TOTALSCORE ");
        stb.append(" FROM ");
        stb.append("     BASE ");
        stb.append(" GROUP BY ");
        stb.append("     ENTEXAMYEAR, TESTDIV, EXAM_TYPE, RECEPTNO, REMARK1, REMARK2 ");
        //合計の統計
        stb.append(" ), TOTAL_TOUKEI AS ( ");
        stb.append(" SELECT ");
        stb.append("     ENTEXAMYEAR, ");
        stb.append("     TESTDIV, ");
        stb.append("     REMARK1, ");
        stb.append("     'TOTAL' AS TESTSUBCLASSCD, ");
        stb.append("     MAX(TOTALSCORE) AS TOTALMAX, ");
        stb.append("     MIN(TOTALSCORE) AS TOTALMIN, ");
        stb.append("     DECIMAL(INT(AVG(FLOAT(TOTALSCORE)) * 100 + 0.5) / 100.0, 5, 2) AS TOTALAVG ");
        stb.append(" FROM ");
        stb.append("     TOTAL ");
        stb.append(" GROUP BY ");
        stb.append("     ENTEXAMYEAR, TESTDIV, REMARK1 ");
        stb.append(" ) ");
        //メイン表
        stb.append("  SELECT ");
        stb.append("     ENTEXAMYEAR, ");
        stb.append("     TESTDIV, ");
        stb.append("     REMARK1, ");
        stb.append("     TESTSUBCLASSCD, ");
        stb.append("     MAX(SCORE) AS MAX, ");
        stb.append("     MIN(SCORE) AS MIN, ");
        stb.append("     DECIMAL(INT(AVG(FLOAT(SCORE)) * 100 + 0.5) / 100.0, 5, 2) AS AVG ");
        stb.append(" FROM ");
        stb.append("     BASE ");
        stb.append(" GROUP BY ");
        stb.append("     ENTEXAMYEAR, ");
        stb.append("     TESTDIV, ");
        stb.append("     REMARK1, ");
        stb.append("     TESTSUBCLASSCD ");
        stb.append(" UNION ");
        stb.append("     SELECT * FROM TOTAL_TOUKEI ");
        stb.append(" ORDER BY ");
        stb.append("     ENTEXAMYEAR, ");
        stb.append("     TESTDIV, ");
        stb.append("     REMARK1, ");
        stb.append("     TESTSUBCLASSCD ");

        return stb.toString();
    }

    /** 得点クラス */
    private class Score {
        final String _max; //最高点
        final String _min; //最低点
        final String _avg; //平均点

        public Score(final String max, final String min, final String avg) {
            _max = max;
            _min = min;
            _avg = avg;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _examYear; //入試年度
        final String _zennenYear; //前年度
        final String _applicantDiv; //入試制度
        final String _examType; //受験型
        final String _examName; //入試名称
        final Map _courseMap; //コース名称
        final Map _testSubclassMap; //科目名称

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _examYear = request.getParameter("ENTEXAMYEAR");
            _zennenYear = String.valueOf(Integer.valueOf(_examYear) - 1);
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _examType = request.getParameter("EXAMTYPE");
            _courseMap= getCourseName(db2);
            _testSubclassMap = getTestSubclassMap(db2);
            _examName = KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT EXAMTYPE_NAME FROM ENTEXAM_EXAMTYPE_MST WHERE ENTEXAMYEAR = '" + _examYear + "' AND APPLICANTDIV = '" + _applicantDiv + "' AND EXAM_TYPE = '" + _examType + "' "));
        }

        private Map getTestSubclassMap(final DB2UDB db2) {
            final Map retMap = new LinkedMap();
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT DISTINCT ");
            stb.append("     T1.ENTEXAMYEAR, ");
            stb.append("     T1.TESTSUBCLASSCD, ");
            stb.append("     L1.NAME1 ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_PERFECT_EXAMTYPE_MST T1 ");
            stb.append(" LEFT JOIN ");
            stb.append("     ENTEXAM_SETTING_MST L1 ");
            stb.append("      ON L1.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
            stb.append("     AND L1.APPLICANTDIV = T1.APPLICANTDIV ");
            stb.append("     AND L1.SETTING_CD = 'L009' ");
            stb.append("     AND L1.SEQ = T1.TESTSUBCLASSCD ");
            stb.append(" WHERE ");
            stb.append("     T1.ENTEXAMYEAR IN ('" + _examYear + "', '" + _zennenYear + "') AND ");
            stb.append("     T1.APPLICANTDIV = '" + _applicantDiv + "' AND ");
            stb.append("     T1.TESTDIV = '" + TEST1 + "' AND "); //第一回固定
            stb.append("     T1.EXAM_TYPE = '" + _examType + "' ");
            stb.append(" ORDER BY ");
            stb.append("     T1.ENTEXAMYEAR, T1.TESTSUBCLASSCD ");

            for (final Map<String, String> row : KnjDbUtils.query(db2, stb.toString())) {
                final String examYear = KnjDbUtils.getString(row, "ENTEXAMYEAR");
                final String name = KnjDbUtils.getString(row, "NAME1");
                final String subclassCd = KnjDbUtils.getString(row, "TESTSUBCLASSCD");

                if (!retMap.containsKey(examYear)) {
                    retMap.put(examYear, new LinkedMap());
                }

                final Map subclassMap = (Map) retMap.get(examYear);
                subclassMap.put(subclassCd, name);
            }
            return retMap;
        }

        private Map getCourseName(final DB2UDB db2) {

            final Map retMap = new LinkedMap();
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     COURSECD || MAJORCD || EXAMCOURSECD AS CD, ");
            stb.append("     EXAMCOURSE_NAME || 'コース' AS NAME");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_COURSE_MST ");
            stb.append(" WHERE ");
            stb.append("     ENTEXAMYEAR = '" + _examYear + "' ");
            stb.append("     AND APPLICANTDIV = '" + _applicantDiv + "' ");
            stb.append("     AND TESTDIV = '0' "); //固定
            stb.append(" ORDER BY ");
            stb.append("     CD");

            for (final Map<String, String> row : KnjDbUtils.query(db2, stb.toString())) {
                final String cd = KnjDbUtils.getString(row, "CD");
                final String name = KnjDbUtils.getString(row, "NAME");
                if (!retMap.containsKey(cd)) {
                    retMap.put(cd, name);
                }
            }

            return retMap;
        }
    }
}

// eof
