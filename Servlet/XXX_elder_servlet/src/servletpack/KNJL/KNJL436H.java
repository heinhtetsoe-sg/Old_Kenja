//kanji=漢字
/*
 *
 * 作成日: 2021/02/17
 * 作成者: ishimine
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJL;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
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
*   ＜ＫＮＪＬ４６４Ｈ 教科別得点平均＞
*/

public class KNJL436H {

    private static final Log log = LogFactory.getLog("KNJL436H.class");

    private boolean _hasData;

    private Param _param;

    private static final String JOGAICOURSE = "100101"; //特別選抜コースコード除外
    private static final String JOGAIKUBUN = "1"; //推薦区分 除外

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
        final Map scoreMap = getScoreMap(db2); //教科別得点Map
        if (scoreMap.isEmpty()) {
            return;
        }
        final Map totalMap = getTotalMap(db2); //合計得点Map

        svf.VrSetForm("KNJL436H.frm", 1);
        svf.VrsOut("PRINT_DATE", "作成日時：" + _param._date.replace("-", "/") + " " + _param._time); //日付
        svf.VrsOut("TITLE", "コース別教科得点平均：" + _param._examYear + "年度　高校入試　" + _param._testNum); //タイトル
        _hasData = true;

        int cnt = 1; //繰り返し回数

        //年度毎のループ
        final String[] yearList = {_param._examYear,_param._zennenYear};
        for (final String year : yearList) {
            int gyo = 1; //印字行

            //コース毎のループ
            for (Iterator courseIte = _param._courseMap.keySet().iterator(); courseIte.hasNext();) {
                final String courseKey = (String) courseIte.next();
                if (JOGAICOURSE.equals(courseKey)) {
                    continue;
                }

                final String courseName = (String) _param._courseMap.get(courseKey);

                //専併区分毎のループ
                for (Iterator shIte = _param._shDivMap.keySet().iterator(); shIte.hasNext();) {
                    final String shKey = (String) shIte.next();
                    if(JOGAIKUBUN.equals(shKey)) {
                        continue;
                    }

                    final String shName = (String) _param._shDivMap.get(shKey);

                    //コース名
                    final String courseShName = courseName + shName;
                    final int keta = KNJ_EditEdit.getMS932ByteLength(courseShName);
                    if (keta <= 18) {
                        svf.VrsOutn("COURSE_NAME2", gyo, courseShName);
                    } else if (keta <= 36) {
                        svf.VrsOutn("COURSE_NAME1", gyo, courseShName.substring(0,9));
                        svf.VrsOutn("COURSE_NAME2", gyo, courseShName.substring(9,courseShName.length()));
                    } else {
                        svf.VrsOutn("COURSE_NAME1", gyo, courseShName.substring(0,9));
                        svf.VrsOutn("COURSE_NAME2", gyo, courseShName.substring(9,18));
                        svf.VrsOutn("COURSE_NAME3", gyo, courseShName.substring(18,courseShName.length()));
                    }

                    int retsu = 1; //印字列

                    //科目毎のループ
                    for (Iterator subclassIte = _param._testSubclassMap.keySet().iterator(); subclassIte.hasNext();) {
                        final String subclassKey = (String) subclassIte.next();
                        final String scoreKey = year + "-" + courseKey + "-" + shKey + "-" + subclassKey;
                        printScore(svf, cnt, retsu, gyo, scoreMap, scoreKey);
                        retsu++;
                    }

                    //合計
                    final String scoreKey = year + "-" + courseKey + "-" + shKey;
                    printScore(svf, cnt, retsu, gyo, totalMap, scoreKey);
                    gyo++;

                }
            }
            cnt++;
        }
        svf.VrEndPage();
    }

    private void printScore(final Vrw32alp svf, final int cnt, final int retsu, final int gyo, final Map printMap, final String scoreKey) {
        if (printMap.containsKey(scoreKey)) {
            final Score score = (Score) printMap.get(scoreKey);
            svf.VrsOutn("MAX" + cnt + "_" + retsu, gyo, score._max); //最高点
            svf.VrsOutn("MIN" + cnt + "_" + retsu, gyo, score._min); //最低点
            svf.VrsOutn("AVE" + cnt + "_" + retsu, gyo, score._avg); //平均点
        }
    }

    private Map getScoreMap(final DB2UDB db2) throws SQLException {
        Map retMap = new LinkedMap();
        PreparedStatement ps = null;
        ResultSet rs = null;

        final StringBuffer stb = new StringBuffer();
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
        stb.append("     SCORE.TESTDIV = '" + _param._testDiv + "' AND ");
        stb.append("     RD016.REMARK1 IS NOT NULL AND ");
        stb.append("     RD016.REMARK2 IS NOT NULL AND ");
        stb.append("     RD016.REMARK1 <> '" + JOGAICOURSE + "' AND ");
        stb.append("     RD016.REMARK2 <> '" + JOGAIKUBUN + "' ");
        stb.append(" ORDER BY ");
        stb.append("     SCORE.ENTEXAMYEAR, SCORE.RECEPTNO ");
        stb.append(" )");
        stb.append(" SELECT ");
        stb.append("     ENTEXAMYEAR, ");
        stb.append("     REMARK1, ");
        stb.append("     REMARK2, ");
        stb.append("     TESTSUBCLASSCD, ");
        stb.append("     MAX(SCORE) AS MAX, ");
        stb.append("     MIN(SCORE) AS MIN, ");
        stb.append("     DECIMAL(INT(AVG(FLOAT(SCORE)) * 10 + 0.5) / 10.0, 5, 2) AS AVG ");
        stb.append(" FROM ");
        stb.append("     BASE ");
        stb.append(" GROUP BY ");
        stb.append("     ENTEXAMYEAR, REMARK1, REMARK2, TESTSUBCLASSCD ");
        stb.append(" ORDER BY ");
        stb.append("     ENTEXAMYEAR, REMARK1, REMARK2, TESTSUBCLASSCD ");

        log.debug(" score sql =" + stb.toString());

        try{
            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();

            while (rs.next()) {
                final String year = rs.getString("ENTEXAMYEAR");
                final String remark1 = rs.getString("REMARK1");
                final String remark2 = rs.getString("REMARK2");
                final String subclassCd = rs.getString("TESTSUBCLASSCD");
                final String max = rs.getString("MAX");
                final String min = rs.getString("MIN");
                final String avg = rs.getString("AVG");

                final String key = year + "-" + remark1 + "-" + remark2 + "-" + subclassCd;

                if (!retMap.containsKey(key)) {
                    final Score score = new Score(max, min, avg);
                    retMap.put(key, score);
                }
            }

        } catch (final SQLException e) {
            log.error("教科別の得点取得でエラー", e);
            throw e;
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }

        return retMap;

    }

    private Map getTotalMap(final DB2UDB db2) throws SQLException {
        Map retMap = new LinkedMap();
        PreparedStatement ps = null;
        ResultSet rs = null;

        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH BASE AS (SELECT ");
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
        stb.append("     SCORE.TESTDIV = '" + _param._testDiv + "' AND ");
        stb.append("     RD016.REMARK1 IS NOT NULL AND ");
        stb.append("     RD016.REMARK2 IS NOT NULL AND ");
        stb.append("     RD016.REMARK1 <> '" + JOGAICOURSE + "' AND ");
        stb.append("     RD016.REMARK2 <> '" + JOGAIKUBUN + "' ");
        stb.append(" ORDER BY ");
        stb.append("     SCORE.ENTEXAMYEAR, ");
        stb.append("     SCORE.RECEPTNO ");
        stb.append(" ), BASE_TOTAL AS (SELECT ");
        stb.append("     ENTEXAMYEAR, ");
        stb.append("     RECEPTNO, ");
        stb.append("     REMARK1, ");
        stb.append("     REMARK2, ");
        stb.append("     SUM(SCORE) AS TOTAL ");
        stb.append(" FROM ");
        stb.append("     BASE ");
        stb.append(" GROUP BY ");
        stb.append("     ENTEXAMYEAR, ");
        stb.append("     RECEPTNO, ");
        stb.append("     REMARK1, ");
        stb.append("     REMARK2 ");
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     ENTEXAMYEAR, ");
        stb.append("     REMARK1, ");
        stb.append("     REMARK2, ");
        stb.append("     MAX(TOTAL) AS MAX, ");
        stb.append("     MIN(TOTAL) AS MIN, ");
        stb.append("     DECIMAL(INT(AVG(FLOAT(TOTAL)) * 10 + 0.5) / 10.0, 5, 2) AS AVG ");
        stb.append(" FROM ");
        stb.append("     BASE_TOTAL ");
        stb.append(" GROUP BY ");
        stb.append("     ENTEXAMYEAR, ");
        stb.append("     REMARK1, ");
        stb.append("     REMARK2 ");
        stb.append(" ORDER BY ");
        stb.append("     ENTEXAMYEAR, ");
        stb.append("     REMARK1, ");
        stb.append("     REMARK2 ");

        log.debug(" total sql =" + stb.toString());

        try{
            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();

            while (rs.next()) {
                final String year = rs.getString("ENTEXAMYEAR");
                final String remark1 = rs.getString("REMARK1");
                final String remark2 = rs.getString("REMARK2");
                final String max = rs.getString("MAX");
                final String min = rs.getString("MIN");
                final String avg = rs.getString("AVG");

                final String key = year + "-" + remark1 + "-" + remark2;

                if (!retMap.containsKey(key)) {
                    final Score score = new Score(max, min, avg);
                    retMap.put(key, score);
                }
            }

        } catch (final SQLException e) {
            log.error("合計得点取得でエラー", e);
            throw e;
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }

        return retMap;

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
        final String _testDiv; //入試区分
        final String _date; //日付
        final String _time; //時間
        final String _testNum; //回数
        final Map _courseMap; //コース名称
        final Map _shDivMap; //ループ用専併区分
        final Map _testSubclassMap; //ループ用受験科目

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _examYear = request.getParameter("ENTEXAMYEAR");
            _zennenYear = String.valueOf(Integer.valueOf(_examYear) - 1);
            _applicantDiv = request.getParameter("APPLICANTDIV");
            _testDiv = request.getParameter("TESTDIV");
            _date = request.getParameter("LOGIN_DATE");
            _testNum = getTestNum(db2);
            _courseMap= getCourseName(db2);
            _shDivMap = getShDivMap(db2);
            _testSubclassMap = getTestSubclassMap(db2);

            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            _time = sdf.format(new Date());
        }

        private String getTestNum(final DB2UDB db2) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT NAME1 FROM ENTEXAM_SETTING_MST WHERE ENTEXAMYEAR = '" + _examYear + "' AND SETTING_CD = 'L004' "));
        }

        private Map getTestSubclassMap(final DB2UDB db2) {
            final Map retMap = new LinkedMap();
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     SEQ, ");
            stb.append("     NAME1 AS NAME");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_SETTING_MST ");
            stb.append(" WHERE ");
            stb.append("     ENTEXAMYEAR = '" + _examYear + "' ");
            stb.append("     AND APPLICANTDIV = '" + _applicantDiv + "' ");
            stb.append("     AND SETTING_CD = 'L009' ");
            stb.append(" ORDER BY ");
            stb.append("     SEQ");

            for (final Map<String, String> row : KnjDbUtils.query(db2, stb.toString())) {
                final String seq = KnjDbUtils.getString(row, "SEQ");
                final String name = KnjDbUtils.getString(row, "NAME");
                if (!retMap.containsKey(seq)) {
                    retMap.put(seq, name);
                }
            }

            return retMap;
        }

        private Map getCourseName(final DB2UDB db2) {

            final Map retMap = new LinkedMap();
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     COURSECD || MAJORCD || EXAMCOURSECD AS CD, ");
            stb.append("     EXAMCOURSE_NAME AS NAME");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_COURSE_MST ");
            stb.append(" WHERE ");
            stb.append("     ENTEXAMYEAR = '" + _examYear + "' ");
            stb.append("     AND APPLICANTDIV = '" + _applicantDiv + "' ");
            stb.append("     AND TESTDIV = '0' ");
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

        private Map getShDivMap(final DB2UDB db2) {
            final Map retMap = new LinkedMap();
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     T1.SEQ, ");
            stb.append("     T1.NAME1 ");
            stb.append(" FROM ");
            stb.append("     ENTEXAM_SETTING_MST T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.ENTEXAMYEAR = '" + _examYear + "' ");
            stb.append("     AND T1.APPLICANTDIV = '" + _applicantDiv + "' ");
            stb.append("     AND T1.SETTING_CD = 'L006' ");
            stb.append(" ORDER BY ");
            stb.append("     T1.SEQ ");

            for (final Map<String, String> row : KnjDbUtils.query(db2, stb.toString())) {
                final String seq = KnjDbUtils.getString(row, "SEQ");
                final String name = KnjDbUtils.getString(row, "NAME1");
                if (!retMap.containsKey(seq)) {
                    retMap.put(seq, name);
                }
            }

            return retMap;
        }
    }
}

// eof
