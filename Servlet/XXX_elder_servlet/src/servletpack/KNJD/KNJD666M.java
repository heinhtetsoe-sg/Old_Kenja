/**     プロジェクト/プロパティ/javaコード・スタイル/コード・テンプレート/パターン       ***/

/*
 *
 * 作成日: 2021/02/26
 * 作成者: ishimine
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KnjDbUtils;

public class KNJD666M {

    private static final Log log = LogFactory.getLog(KNJD666M.class);
    private boolean _hasData;
    private Param _param;

    private static final String PER_DIV_SUBCLASS = "01";
    private static final String PER_DIV_GRADE = "02";
    private static final String PER_DIV_COURSE = "03";

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
        final Map<String, Student> studentMap = getStudentMap(db2);
        if (studentMap.isEmpty()) return;

        final Map bunpuMap = getBunpuMap(db2);

        for (Iterator iterator = studentMap.keySet().iterator(); iterator.hasNext();) {
            final String key = (String) iterator.next();
            final Student student = studentMap.get(key);
            svf.VrSetForm("KNJD666M.frm", 1);

            printTitle(svf, student);
            printScore(svf, student._scoreMap, bunpuMap);
            svf.VrEndPage();
            _hasData = true;
        }
    }

    private void printScore(final Vrw32alp svf, final Map<String, Score> scoreMap, final Map<String, Bunpu> bunpuMap) {
        int retsu = 1;
        final int maxRetsu = 14; //最大列

        //科目毎のループ
        for (final Iterator ite = scoreMap.keySet().iterator(); ite.hasNext();) {
            if(retsu > maxRetsu) break; //15科目以上は無視

            final String subclassKey = (String)ite.next();
            final Score score = scoreMap.get(subclassKey);

            svf.VrsOutn("SUBCLASS_NAME", retsu, score._subclassabbv); //科目名
            svf.VrsOutn("GRADE_MAX", retsu, score._highscore);        //学年最高点
            svf.VrsOutn("GRADE_AVE", retsu, score._avg);              //学年平均点
            svf.VrsOutn("SELF_SCORE", retsu, score._score);           //本人得点

            int perfect = 100;
            if (score._perfect != null) {
                perfect = Integer.parseInt(score._perfect); //満点
            }

            //比較元の学年平均は小数点第一位を四捨五入
            final String markAvg;
            if (score._avg != null) {
                final BigDecimal wk = new BigDecimal(score._avg).setScale(0, BigDecimal.ROUND_HALF_UP);
                markAvg = wk.toString();
            } else {
                markAvg = null;
            }

            //得点分布
            if (bunpuMap.containsKey(subclassKey)) {
                final Bunpu bunpu = bunpuMap.get(subclassKey);
                if (bunpu != null) {
                    boolean printMark = false; //学年平均印字フラグ

                    for (int idx = 0; idx <= 17; idx++) {
                        final int low = Integer.parseInt(String.valueOf(bunpu.lowList.get(idx)));
                        if (perfect < 100 && low > perfect) { //満点が100点未満で満点より大きい領域は斜線
                            svf.VrsOutn("SLASH" + (idx + 1), retsu, _param._slashImagePath);
                        } else {
                            String mark = "";
                            if (!printMark) {
                                if (markAvg != null && low <= Integer.parseInt(markAvg)) {
                                    mark =  "*"; //学年平均点の箇所はマーク
                                    printMark = true;
                                }
                            }
                            svf.VrsOutn("NUM" + (idx + 1), retsu, mark + String.valueOf(bunpu.numList.get(idx)) );
                        }
                    }
                    svf.VrsOutn("EXAM_NUM", retsu, bunpu._count); //受験者数
                }
            }
            retsu++;
        }
    }

    private void printTitle(final Vrw32alp svf, final Student student) {
        final String kousyu = "H".equals(_param._schoolKind) ? "高" : "中";
        svf.VrsOut("TITLE", kousyu + Integer.parseInt(student._grade_Cd)  +  "　" + _param._semesterName + "　　" + _param._testName + " 結果報告書");
        svf.VrsOut("DATE", "発行日：　" + _param._wareki);
        svf.VrsOut("HR_NAME", kousyu + Integer.parseInt(student._grade_Cd) + student._hr_Class_Name1 + "組" + student._attendno.substring(1,3) + "番");
        svf.VrsOut("NAME", student._name);
    }

    private Map getStudentMap(final DB2UDB db2) {
        final Map<String, Student> retMap = new LinkedMap();
        PreparedStatement ps = null;
        ResultSet rs = null;
        final String sql = getStudentSql();
        log.debug(" sql =" + sql);
        try {

            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String schregno = rs.getString("SCHREGNO");
                if (!retMap.containsKey(schregno)) {
                    final String name = rs.getString("NAME");
                    final String grade_Cd = rs.getString("GRADE_CD");
                    final String hr_Class_Name1 = rs.getString("HR_CLASS_NAME1");
                    final String attendno = rs.getString("ATTENDNO");
                    retMap.put(schregno, new Student(schregno, name, grade_Cd, hr_Class_Name1, attendno));
                }

                final Student student = retMap.get(schregno);

                final String subclasskey = rs.getString("SUBCLASSKEY");
                final String score = rs.getString("SCORE");
                final String avg = rs.getString("AVG");
                final String highscore = rs.getString("HIGHSCORE");
                final String subclassabbv = rs.getString("SUBCLASSABBV");
                final String perfect = rs.getString("PERFECT");
                final Score subclassScore = new Score(subclasskey, score, avg, highscore, subclassabbv, perfect);

                student._scoreMap.put(subclasskey, subclassScore);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retMap;
    }

    private String getStudentSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" WITH SCHREGNO AS ( ");
        stb.append(" SELECT ");
        stb.append("     REGD.SCHREGNO, ");
        stb.append("     REGD.YEAR, ");
        stb.append("     REGD.SEMESTER, ");
        stb.append("     REGD.GRADE, ");
        stb.append("     REGD.ATTENDNO, ");
        stb.append("     REGD.COURSECD, ");
        stb.append("     REGD.MAJORCD, ");
        stb.append("     REGD.COURSECODE, ");
        stb.append("     BASE.NAME, ");
        stb.append("     GDAT.GRADE_CD, ");
        stb.append("     HDAT.HR_CLASS_NAME1 ");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT REGD ");
        stb.append(" INNER JOIN ");
        stb.append("     SCHREG_BASE_MST BASE ");
        stb.append("      ON BASE.SCHREGNO = REGD.SCHREGNO ");
        stb.append(" INNER JOIN ");
        stb.append("     SCHREG_REGD_GDAT GDAT ");
        stb.append("      ON GDAT.YEAR = REGD.YEAR ");
        stb.append("     AND GDAT.GRADE = REGD.GRADE ");
        stb.append("     AND GDAT.SCHOOL_KIND = '" + _param._schoolKind + "'" );
        stb.append(" INNER JOIN ");
        stb.append("     SCHREG_REGD_HDAT HDAT ");
        stb.append("      ON HDAT.YEAR = REGD.YEAR ");
        stb.append("     AND HDAT.SEMESTER = REGD.SEMESTER ");
        stb.append("     AND HDAT.GRADE = REGD.GRADE ");
        stb.append("     AND HDAT.HR_CLASS = REGD.HR_CLASS ");
        stb.append(" WHERE ");
        stb.append("     REGD.YEAR = '" + _param._year + "' AND ");
        stb.append("     REGD.SEMESTER = '" + _param._semester + "' AND ");
        stb.append("     REGD.GRADE || REGD.HR_CLASS = '"+ _param._gradeHrclass + "' AND ");
        stb.append("     REGD.SCHREGNO IN " + SQLUtils.whereIn(true, _param._categorySelected));
        stb.append(" ) ");
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T1.NAME, ");
        stb.append("     T1.GRADE_CD, ");
        stb.append("     T1.HR_CLASS_NAME1, ");
        stb.append("     T1.ATTENDNO, ");
        stb.append("     SCORE.CLASSCD || '-' || SCORE.SCHOOL_KIND || '-' || SCORE.CURRICULUM_CD || '-' || SCORE.SUBCLASSCD AS SUBCLASSKEY, ");
        stb.append("     RANK.SCORE, ");
        stb.append("     DECIMAL(INT(AVE.AVG * 10 + 0.5) / 10.0, 5, 1) AS AVG, ");
        stb.append("     AVE.HIGHSCORE, ");
        stb.append("     SUB.SUBCLASSABBV, ");
        stb.append("     CASE WHEN PER_SUBCLASS.PERFECT IS NOT NULL THEN PER_SUBCLASS.PERFECT WHEN PER_GRADE.PERFECT IS NOT NULL THEN PER_GRADE.PERFECT ELSE PER_COURSE.PERFECT END AS PERFECT ");
        stb.append(" FROM ");
        stb.append("     RECORD_SCORE_DAT SCORE ");
        stb.append(" INNER JOIN ");
        stb.append("     SCHREGNO T1 ");
        stb.append("      ON T1.YEAR = SCORE.YEAR ");
        stb.append("     AND T1.SEMESTER = SCORE.SEMESTER ");
        stb.append("     AND T1.SCHREGNO = SCORE.SCHREGNO ");
        stb.append(" LEFT JOIN ");
        stb.append("     RECORD_RANK_SDIV_DAT RANK ");
        stb.append("      ON RANK.YEAR = SCORE.YEAR ");
        stb.append("     AND RANK.SEMESTER = SCORE.SEMESTER ");
        stb.append("     AND RANK.TESTKINDCD = SCORE.TESTKINDCD ");
        stb.append("     AND RANK.TESTITEMCD = SCORE.TESTITEMCD ");
        stb.append("     AND RANK.SCORE_DIV = SCORE.SCORE_DIV ");
        stb.append("     AND RANK.CLASSCD = SCORE.CLASSCD ");
        stb.append("     AND RANK.SCHOOL_KIND = SCORE.SCHOOL_KIND ");
        stb.append("     AND RANK.CURRICULUM_CD = SCORE.CURRICULUM_CD ");
        stb.append("     AND RANK.SUBCLASSCD = SCORE.SUBCLASSCD ");
        stb.append("     AND RANK.SCHREGNO = SCORE.SCHREGNO ");
        stb.append(" LEFT JOIN ");
        stb.append("     RECORD_AVERAGE_SDIV_DAT AVE ");
        stb.append("      ON AVE.YEAR = SCORE.YEAR ");
        stb.append("     AND AVE.SEMESTER = SCORE.SEMESTER ");
        stb.append("     AND AVE.TESTKINDCD = SCORE.TESTKINDCD ");
        stb.append("     AND AVE.TESTITEMCD = SCORE.TESTITEMCD ");
        stb.append("     AND AVE.SCORE_DIV = SCORE.SCORE_DIV ");
        stb.append("     AND AVE.CLASSCD = SCORE.CLASSCD ");
        stb.append("     AND AVE.SCHOOL_KIND = SCORE.SCHOOL_KIND ");
        stb.append("     AND AVE.CURRICULUM_CD = SCORE.CURRICULUM_CD ");
        stb.append("     AND AVE.SUBCLASSCD = SCORE.SUBCLASSCD ");
        stb.append("     AND AVE.AVG_DIV = '3' "); //コース平均
        stb.append("     AND AVE.GRADE = T1.GRADE ");
        stb.append("     AND AVE.HR_CLASS = '000' ");
        stb.append("     AND AVE.COURSECD = T1.COURSECD ");
        stb.append("     AND AVE.MAJORCD = T1.MAJORCD ");
        stb.append("     AND AVE.COURSECODE = T1.COURSECODE ");
        stb.append(" LEFT JOIN ");
        stb.append("     SUBCLASS_MST SUB ");
        stb.append("      ON SUB.CLASSCD = SCORE.CLASSCD ");
        stb.append("     AND SUB.SCHOOL_KIND = SCORE.SCHOOL_KIND ");
        stb.append("     AND SUB.CURRICULUM_CD = SCORE.CURRICULUM_CD ");
        stb.append("     AND SUB.SUBCLASSCD = SCORE.SUBCLASSCD ");
        stb.append(" LEFT JOIN ");
        stb.append("     PERFECT_RECORD_DAT PER_SUBCLASS ");
        stb.append("      ON PER_SUBCLASS.YEAR = SCORE.YEAR ");
        stb.append("     AND PER_SUBCLASS.SEMESTER = SCORE.SEMESTER ");
        stb.append("     AND PER_SUBCLASS.TESTKINDCD = SCORE.TESTKINDCD ");
        stb.append("     AND PER_SUBCLASS.TESTITEMCD = SCORE.TESTITEMCD ");
        stb.append("     AND PER_SUBCLASS.CLASSCD = SCORE.CLASSCD ");
        stb.append("     AND PER_SUBCLASS.SCHOOL_KIND = SCORE.SCHOOL_KIND ");
        stb.append("     AND PER_SUBCLASS.CURRICULUM_CD = SCORE.CURRICULUM_CD ");
        stb.append("     AND PER_SUBCLASS.SUBCLASSCD = SCORE.SUBCLASSCD ");
        stb.append("     AND PER_SUBCLASS.DIV = '" + PER_DIV_SUBCLASS + "' "); // 01:科目
        stb.append("     AND PER_SUBCLASS.GRADE = '00' ");
        stb.append("     AND PER_SUBCLASS.COURSECD = '0' ");
        stb.append("     AND PER_SUBCLASS.MAJORCD = '000' ");
        stb.append("     AND PER_SUBCLASS.COURSECODE = '0000' ");
        stb.append(" LEFT JOIN ");
        stb.append("     PERFECT_RECORD_DAT PER_GRADE ");
        stb.append("      ON PER_GRADE.YEAR = SCORE.YEAR ");
        stb.append("     AND PER_GRADE.SEMESTER = SCORE.SEMESTER ");
        stb.append("     AND PER_GRADE.TESTKINDCD = SCORE.TESTKINDCD ");
        stb.append("     AND PER_GRADE.TESTITEMCD = SCORE.TESTITEMCD ");
        stb.append("     AND PER_GRADE.CLASSCD = SCORE.CLASSCD ");
        stb.append("     AND PER_GRADE.SCHOOL_KIND = SCORE.SCHOOL_KIND ");
        stb.append("     AND PER_GRADE.CURRICULUM_CD = SCORE.CURRICULUM_CD ");
        stb.append("     AND PER_GRADE.SUBCLASSCD = SCORE.SUBCLASSCD ");
        stb.append("     AND PER_GRADE.DIV = '" + PER_DIV_GRADE + "' "); // 02:学年
        stb.append("     AND PER_GRADE.GRADE = T1.GRADE ");
        stb.append("     AND PER_GRADE.COURSECD = '0' ");
        stb.append("     AND PER_GRADE.MAJORCD = '000' ");
        stb.append("     AND PER_GRADE.COURSECODE = '0000' ");
        stb.append(" LEFT JOIN ");
        stb.append("     PERFECT_RECORD_DAT PER_COURSE ");
        stb.append("      ON PER_COURSE.YEAR = SCORE.YEAR ");
        stb.append("     AND PER_COURSE.SEMESTER = SCORE.SEMESTER ");
        stb.append("     AND PER_COURSE.TESTKINDCD = SCORE.TESTKINDCD ");
        stb.append("     AND PER_COURSE.TESTITEMCD = SCORE.TESTITEMCD ");
        stb.append("     AND PER_COURSE.CLASSCD = SCORE.CLASSCD ");
        stb.append("     AND PER_COURSE.SCHOOL_KIND = SCORE.SCHOOL_KIND ");
        stb.append("     AND PER_COURSE.CURRICULUM_CD = SCORE.CURRICULUM_CD ");
        stb.append("     AND PER_COURSE.SUBCLASSCD = SCORE.SUBCLASSCD ");
        stb.append("     AND PER_COURSE.DIV = '" + PER_DIV_COURSE + "' "); // 03:コース
        stb.append("     AND PER_COURSE.GRADE = T1.GRADE ");
        stb.append("     AND PER_COURSE.COURSECD = T1.COURSECD ");
        stb.append("     AND PER_COURSE.MAJORCD = T1.MAJORCD ");
        stb.append("     AND PER_COURSE.COURSECODE = T1.COURSECODE ");
        stb.append(" WHERE ");
        stb.append("     SCORE.YEAR = '" + _param._year + "' AND ");
        stb.append("     SCORE.SEMESTER = '" + _param._semester + "' AND ");
        stb.append("     SCORE.TESTKINDCD || SCORE.TESTITEMCD || SCORE.SCORE_DIV = '" + _param._testCd + "' AND ");
        stb.append("     SCORE.CLASSCD || '-' || SCORE.SCHOOL_KIND || '-' || SCORE.CURRICULUM_CD || '-' || SCORE.SUBCLASSCD IN " + SQLUtils.whereIn(true, _param._categorySelectedSub) + "AND ");
        stb.append("     RANK.SCORE IS NOT NULL ");
        stb.append(" ORDER BY ");
        stb.append("     T1.ATTENDNO, SUBCLASSKEY ");

        return stb.toString();
    }

    private class Student {
        final String _schregno;
        final String _name;
        final String _grade_Cd;
        final String _hr_Class_Name1;
        final String _attendno;
        final Map<String, Score> _scoreMap = new LinkedMap();

        public Student(final String schregno, final String name, final String grade_Cd, final String hr_Class_Name1,
                final String attendno) {
            _schregno = schregno;
              _name = name;
            _grade_Cd = grade_Cd;
            _hr_Class_Name1 = hr_Class_Name1;
            _attendno = attendno;
        }
    }

    private Map getBunpuMap(final DB2UDB db2) {
        final Map<String, Bunpu> retMap = new LinkedMap();
        PreparedStatement ps = null;
        ResultSet rs = null;
        final String sql = getBunpuSql();
        log.debug(" bunpu sql =" + sql);
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final String subclasskey = rs.getString("SUBCLASSKEY");
                final String bunpu00 = rs.getString("BUNPU00");
                final String bunpu01 = rs.getString("BUNPU01");
                final String bunpu02 = rs.getString("BUNPU02");
                final String bunpu03 = rs.getString("BUNPU03");
                final String bunpu04 = rs.getString("BUNPU04");
                final String bunpu05 = rs.getString("BUNPU05");
                final String bunpu06 = rs.getString("BUNPU06");
                final String bunpu07 = rs.getString("BUNPU07");
                final String bunpu08 = rs.getString("BUNPU08");
                final String bunpu09 = rs.getString("BUNPU09");
                final String bunpu10 = rs.getString("BUNPU10");
                final String bunpu11 = rs.getString("BUNPU11");
                final String bunpu12 = rs.getString("BUNPU12");
                final String bunpu13 = rs.getString("BUNPU13");
                final String bunpu14 = rs.getString("BUNPU14");
                final String bunpu15 = rs.getString("BUNPU15");
                final String bunpu16 = rs.getString("BUNPU16");
                final String bunpu17 = rs.getString("BUNPU17");
                final String count = rs.getString("COUNT");

                final Bunpu  bunpu = new Bunpu(subclasskey, count);

                bunpu.numList.addAll(
                        Arrays.asList(bunpu00, bunpu01, bunpu02, bunpu03, bunpu04, bunpu05, bunpu06, bunpu07, bunpu08,
                                bunpu09, bunpu10, bunpu11, bunpu12, bunpu13, bunpu14, bunpu15, bunpu16, bunpu17));

                retMap.put(subclasskey, bunpu);
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retMap;
    }

    //得点分布表
    private String getBunpuSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ");
        stb.append("     SCORE.CLASSCD || '-' || SCORE.SCHOOL_KIND || '-' || SCORE.CURRICULUM_CD || '-' || SCORE.SUBCLASSCD AS SUBCLASSKEY, ");
        stb.append("     SUM(CASE WHEN RANK.SCORE = 100 THEN 1 ELSE 0 END) AS BUNPU00, ");
        stb.append("     SUM(CASE WHEN RANK.SCORE BETWEEN 95 AND 99 THEN 1 ELSE 0 END) AS BUNPU01, ");
        stb.append("     SUM(CASE WHEN RANK.SCORE BETWEEN 90 AND 94 THEN 1 ELSE 0 END) AS BUNPU02, ");
        stb.append("     SUM(CASE WHEN RANK.SCORE BETWEEN 85 AND 89 THEN 1 ELSE 0 END) AS BUNPU03, ");
        stb.append("     SUM(CASE WHEN RANK.SCORE BETWEEN 80 AND 84 THEN 1 ELSE 0 END) AS BUNPU04, ");
        stb.append("     SUM(CASE WHEN RANK.SCORE BETWEEN 75 AND 79 THEN 1 ELSE 0 END) AS BUNPU05, ");
        stb.append("     SUM(CASE WHEN RANK.SCORE BETWEEN 70 AND 74 THEN 1 ELSE 0 END) AS BUNPU06, ");
        stb.append("     SUM(CASE WHEN RANK.SCORE BETWEEN 65 AND 69 THEN 1 ELSE 0 END) AS BUNPU07, ");
        stb.append("     SUM(CASE WHEN RANK.SCORE BETWEEN 60 AND 64 THEN 1 ELSE 0 END) AS BUNPU08, ");
        stb.append("     SUM(CASE WHEN RANK.SCORE BETWEEN 55 AND 59 THEN 1 ELSE 0 END) AS BUNPU09, ");
        stb.append("     SUM(CASE WHEN RANK.SCORE BETWEEN 50 AND 54 THEN 1 ELSE 0 END) AS BUNPU10, ");
        stb.append("     SUM(CASE WHEN RANK.SCORE BETWEEN 45 AND 49 THEN 1 ELSE 0 END) AS BUNPU11, ");
        stb.append("     SUM(CASE WHEN RANK.SCORE BETWEEN 40 AND 44 THEN 1 ELSE 0 END) AS BUNPU12, ");
        stb.append("     SUM(CASE WHEN RANK.SCORE BETWEEN 35 AND 39 THEN 1 ELSE 0 END) AS BUNPU13, ");
        stb.append("     SUM(CASE WHEN RANK.SCORE BETWEEN 30 AND 34 THEN 1 ELSE 0 END) AS BUNPU14, ");
        stb.append("     SUM(CASE WHEN RANK.SCORE BETWEEN 25 AND 29 THEN 1 ELSE 0 END) AS BUNPU15, ");
        stb.append("     SUM(CASE WHEN RANK.SCORE BETWEEN 20 AND 24 THEN 1 ELSE 0 END) AS BUNPU16, ");
        stb.append("     SUM(CASE WHEN RANK.SCORE < 20 THEN 1 ELSE 0 END) AS BUNPU17, ");
        stb.append("     SUM(CASE WHEN SCORE.VALUE_DI IS NULL THEN 1 ELSE 0 END) AS COUNT ");
        stb.append(" FROM ");
        stb.append("     RECORD_SCORE_DAT SCORE ");
        stb.append(" LEFT JOIN  ");
        stb.append("     RECORD_RANK_SDIV_DAT RANK ");
        stb.append("      ON RANK.YEAR = SCORE.YEAR ");
        stb.append("     AND RANK.SEMESTER = SCORE.SEMESTER ");
        stb.append("     AND RANK.TESTKINDCD = SCORE.TESTKINDCD ");
        stb.append("     AND RANK.TESTITEMCD = SCORE.TESTITEMCD ");
        stb.append("     AND RANK.SCORE_DIV = SCORE.SCORE_DIV ");
        stb.append("     AND RANK.CLASSCD = SCORE.CLASSCD ");
        stb.append("     AND RANK.SCHOOL_KIND = SCORE.SCHOOL_KIND ");
        stb.append("     AND RANK.CURRICULUM_CD = SCORE.CURRICULUM_CD ");
        stb.append("     AND RANK.SUBCLASSCD = SCORE.SUBCLASSCD ");
        stb.append("     AND RANK.SCHREGNO = SCORE.SCHREGNO ");
        stb.append(" WHERE ");
        stb.append("     SCORE.YEAR = '" + _param._year + "' AND ");
        stb.append("     SCORE.SEMESTER = '" + _param._semester + "' AND ");
        stb.append("     SCORE.TESTKINDCD || SCORE.TESTITEMCD || SCORE.SCORE_DIV = '" + _param._testCd + "' AND ");
        stb.append("     SCORE.CLASSCD || '-' || SCORE.SCHOOL_KIND || '-' || SCORE.CURRICULUM_CD || '-' || SCORE.SUBCLASSCD IN " + SQLUtils.whereIn(true, _param._categorySelectedSub));
        stb.append(" GROUP BY ");
        stb.append("     SCORE.CLASSCD, ");
        stb.append("     SCORE.SCHOOL_KIND, ");
        stb.append("     SCORE.CURRICULUM_CD, ");
        stb.append("     SCORE.SUBCLASSCD ");
        stb.append(" ORDER BY ");
        stb.append("     SUBCLASSKEY ");

        return stb.toString();
    }

    private class Score {
        final String _subclasskey;
        final String _score;
        final String _avg;
        final String _highscore;
        final String _subclassabbv;
        final String _perfect;

        public Score(final String subclasskey, final String score, final String avg,
                final String highscore, final String subclassabbv, final String perfect) {
            _subclasskey = subclasskey;
            _score = score;
            _avg = avg;
            _highscore = highscore;
            _subclassabbv = subclassabbv;
            _perfect = perfect;
        }
    }

    private class Bunpu {
        final String _subclasskey;
        final String _count;
        final LinkedList numList = new LinkedList(); //得点分布
        final LinkedList lowList; //学年平均*を印字する判定用

        public Bunpu(final String subclasskey, final String count) {
            _subclasskey = subclasskey;
            _count = count;
            lowList = lowListSet();
        }

        public LinkedList lowListSet() {
            final LinkedList retList = new LinkedList();
            retList.add(100);
            for (int low = 95; low >= 20; low -= 5) {
                retList.add(low);
            }
            retList.add(0);
            return retList;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String[] _categorySelected; //対象生徒
        final String[] _categorySelectedSub; //対象科目
        final String _gradeHrclass;
        final String _semester;
        final String _semesterName;
        final String _testCd;
        final String _year;
        final String _schoolKind;
        final String _documentRoot;
        final String _wareki;
        final String _testName;
        final String _imagepath;
        final String _slashImagePath;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException, UnsupportedEncodingException {
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _categorySelectedSub = request.getParameterValues("CATEGORY_SELECTED_SUB");
            _gradeHrclass = request.getParameter("GRADE_HR_CLASS");
            _semester = request.getParameter("SEMESTER");
            _year = request.getParameter("LOGIN_YEAR");
            _testCd = request.getParameter("TESTCD");
            _schoolKind = request.getParameter("SCHOOL_KIND");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
            String outputDate = sdf.format(new Date());
            _wareki = KNJ_EditDate.h_format_JP(db2, outputDate);

            _documentRoot = request.getParameter("DOCUMENTROOT");
            final String sqlControlMst = " SELECT IMAGEPATH FROM CONTROL_MST WHERE CTRL_NO = '01' ";
            _imagepath = KnjDbUtils.getOne(KnjDbUtils.query(db2, sqlControlMst));
            _slashImagePath = getImageFilePath("slash.jpg");

            _semesterName = KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT SEMESTERNAME FROM SEMESTER_MST WHERE YEAR = '" + _year + "' AND SEMESTER = '" + _semester + "'"));
            _testName = KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT TESTITEMNAME FROM TESTITEM_MST_COUNTFLG_NEW_SDIV WHERE YEAR = '" + _year + "' AND SEMESTER = '" + _semester + "' AND TESTKINDCD || TESTITEMCD || SCORE_DIV = '" + _testCd + "'"));
        }

        public String getImageFilePath(final String name) {
            final String path = _documentRoot + "/" + (null == _imagepath || "".equals(_imagepath) ? "" : _imagepath + "/") + name;
            final boolean exists = new java.io.File(path).exists();
            log.warn(" path " + path + " exists: " + exists);
            if (exists) {
                return path;
            }
            return null;
        }
    }
}
// eof
