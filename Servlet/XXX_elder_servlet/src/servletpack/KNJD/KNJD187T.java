// kanji=漢字
package servletpack.KNJD;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 * <<クラスの説明>>。
 * @author nakamoto
 * @version $Id:$
 */
public class KNJD187T {

    private static final Log log = LogFactory.getLog("KNJD187T.class");

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

    //度数分布表印刷　高校のみ
    private void printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException {

        final Map studentMap = getStudentMap(db2); //出力対象生徒Map

        final Map printMap = getPrintMap(db2, false, null); //コース別の度数分布Map
        if(printMap.size() == 0) {
            return;
        }
        //生徒毎のループ
        for(Iterator ite = studentMap.keySet().iterator(); ite.hasNext();) {
            final String schregno = (String)ite.next();
            final String courseKey = (String)studentMap.get(schregno);
            log.debug(schregno + "=" + courseKey);

            if(!printMap.containsKey(courseKey)) {
                return;
            }
            final Course course = (Course)printMap.get(courseKey);
            //全科目平均度数分布Map
            if(course._avgDosuuBunpuMap.size() == 0) {
                course._avgDosuuBunpuMap = getPrintMap(db2, true, course);
            }
            svf.VrSetForm("KNJD187T.frm", 4);
            setTitle(svf);

            int gyo = 1; //行数
            BigDecimal total = new BigDecimal(0);

            //全科目平均点印字
            for(int i = 100; i >= 0; i -= 10) {
                final String num;
                if(course._avgDosuuBunpuMap.containsKey(i)) {
                    num = (String)course._avgDosuuBunpuMap.get(i);
                } else {
                    num = "";
                }
                svf.VrsOutn("NUM2", gyo, num);
                gyo++;
            }

            svf.VrsOut("TOTAL_AVE", course._avgAvg);

            //科目毎のループ
            for(Iterator ite2 = course._subclassMap.keySet().iterator(); ite2.hasNext();) {
                final String subclassKey = (String)ite2.next();
                final SubclassMst subclass = (SubclassMst)course._subclassMap.get(subclassKey);
                final String printSubclassName = StringUtils.defaultString(subclass._subclassAbbv, subclass._subclassName);
                final int keta = printSubclassName.length();
                final String field = keta <= 10 ? "1" : keta <= 14 ? "2" : keta <= 16 ? "3" : "4_1";

                if(keta <= 16) {
                    svf.VrsOut("SUBCLASS_NAME" + field, printSubclassName);
                } else {
                    final String str1 = printSubclassName.substring(0, 16);
                    final String str2 = printSubclassName.substring(16, printSubclassName.length());
                    svf.VrsOut("SUBCLASS_NAME4_1", str1);
                    svf.VrsOut("SUBCLASS_NAME4_2", str2);
                }
                gyo = 1;
                for(int i = 100; i >= 0; i -= 10) {
                    final String num;
                    if(subclass._dosuuBunpuMap.containsKey(i)) {
                        num = (String)subclass._dosuuBunpuMap.get(i);
                    } else {
                        num = "";
                    }
                    svf.VrsOutn("NUM1", gyo, num);
                    gyo++;
                }
                //学年平均
                if(subclass._avg != null) {
                    final double wk = Double.parseDouble(subclass._avg);
                    final double wk2 = ((double)Math.round(wk * 10))/10;
                    final String avg = String.valueOf(wk2);
                    final int keta2 = KNJ_EditEdit.getMS932ByteLength(avg);
                    final String field2 = keta2 <= 4 ? "1" : "2";
                    svf.VrsOut("AVE1_" + field2, avg);

                    final BigDecimal avgBd = new BigDecimal(subclass._avg);
                    total = total.add(avgBd);
                }
                svf.VrEndRecord();
            }
            svf.VrsOut("TOTAL_AVE", total.divide(new BigDecimal(course._subclassMap.size()), 1, BigDecimal.ROUND_HALF_UP).toString());
            svf.VrEndPage();
            _hasData = true;
        }
    }

    private void setTitle(final Vrw32alp svf) {
        svf.VrsOut("TITLE", "得点分布表");
        svf.VrsOut("NENDO", _param._year + "年度 " + _param._semesterMst.get("SEMESTERNAME"));
        final String nenkumi = StringUtils.defaultString(_param._gradeName) + "　" + StringUtils.defaultString(_param._hrName) + "組";
        svf.VrsOut("HR_NAME", "高校　" + nenkumi);
    }

    private Map getStudentMap(final DB2UDB db2) throws SQLException {
        Map retMap = new LinkedMap();
        PreparedStatement ps = null;
        ResultSet rs = null;
        String sdate = (String)_param._semesterMst.get("SDATE");
        String edate = (String)_param._semesterMst.get("EDATE");

        try{
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("   REGD.SCHREGNO, ");
            stb.append("   REGD.COURSECD, ");
            stb.append("   REGD.MAJORCD, ");
            stb.append("   REGD.COURSECODE, ");
            stb.append("   BASE.NAME ");
            stb.append(" FROM ");
            stb.append("   SCHREG_REGD_DAT REGD ");
            stb.append(" INNER JOIN ");
            stb.append("   SCHREG_BASE_MST BASE ");
            stb.append("    ON BASE.SCHREGNO = REGD.SCHREGNO ");
            stb.append(" WHERE ");
            stb.append("   REGD.YEAR = '" + _param._year + "' ");
            stb.append("   AND REGD.SEMESTER = '" + _param._seme + "' ");
            stb.append("   AND REGD.GRADE || REGD.HR_CLASS = '" + _param._gradeHrclass + "' ");
            stb.append("   AND NOT EXISTS ");
            stb.append("     (SELECT 'X' ");
            stb.append("     FROM SCHREG_TRANSFER_DAT S1 ");
            stb.append("     WHERE S1.SCHREGNO = REGD.SCHREGNO ");
            stb.append("     AND S1.TRANSFERCD IN ('2') ");
            stb.append("     AND TRANSFER_SDATE <= '" + sdate + "' AND S1.TRANSFER_EDATE >= '" + edate + "') ");
            stb.append("   AND REGD.SCHREGNO IN " + SQLUtils.whereIn(true, _param._categorySelected) + " ");
            stb.append(" ORDER BY ");
            stb.append("   REGD.GRADE, REGD.HR_CLASS, REGD.ATTENDNO ");

            log.debug(" sql =" + stb.toString());
            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();
            while (rs.next()) {
                final String schregno = rs.getString("SCHREGNO");
                final String coursecd = rs.getString("COURSECD");
                final String majorcd = rs.getString("MAJORCD");
                final String coursecode = rs.getString("COURSECODE");

                final String courseKey = coursecd + "-" + majorcd + "-" + coursecode;
                if (!retMap.containsKey(courseKey)) {
                    retMap.put(schregno, courseKey);
                }
            }
        } catch (final SQLException e) {
            log.error("生徒の基本情報取得でエラー", e);
            throw e;
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }

        return retMap;

    }

    private Map getPrintMap(final DB2UDB db2, final boolean avg, final Course courseAvg) throws SQLException {
        Map retMap = new LinkedMap();
        PreparedStatement ps = null;
        ResultSet rs = null;
        Course course = null;
        SubclassMst subclass = null;
        String sdate = (String)_param._semesterMst.get("SDATE");
        String edate = (String)_param._semesterMst.get("EDATE");

        try{
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH SCORE AS ( ");
            stb.append(" SELECT ");
            stb.append("   REGD.YEAR, ");
            stb.append("   REGD.SEMESTER, ");
            stb.append("   REGD.GRADE, ");
            stb.append("   REGD.HR_CLASS, ");
            stb.append("   REGD.ATTENDNO, ");
            stb.append("   BASE.NAME, ");
            stb.append("   REGD.SCHREGNO, ");
            stb.append("   REGD.COURSECD, ");
            stb.append("   REGD.MAJORCD, ");
            stb.append("   REGD.COURSECODE, ");
            stb.append("   RANK.CLASSCD, ");
            stb.append("   RANK.SCHOOL_KIND, ");
            stb.append("   RANK.CURRICULUM_CD, ");
            stb.append("   RANK.SUBCLASSCD, ");
            stb.append("   RANK.SCORE, ");
            stb.append("   SUBCLASS.SUBCLASSNAME, ");
            stb.append("   SUBCLASS.SUBCLASSABBV ");
            stb.append(" FROM ");
            stb.append("   SCHREG_REGD_DAT REGD ");
            stb.append(" INNER JOIN ");
            stb.append("   SCHREG_BASE_MST BASE ");
            stb.append("    ON BASE.SCHREGNO = REGD.SCHREGNO ");
            stb.append(" INNER JOIN ");
            stb.append("   RECORD_RANK_SDIV_DAT RANK ");
            stb.append("    ON RANK.YEAR = REGD.YEAR ");
            stb.append("   AND RANK.SEMESTER = '" + _param._semester + "' ");
            stb.append("   AND RANK.TESTKINDCD = '" + _param._testkindcd + "' ");
            stb.append("   AND RANK.TESTITEMCD = '" + _param._testitemcd + "' ");
            stb.append("   AND RANK.SCORE_DIV = '" + _param._scoreDiv + "' ");
            stb.append("   AND RANK.SCHREGNO = REGD.SCHREGNO ");
            stb.append("   AND RANK.CLASSCD <= '90' ");
            stb.append("   AND RANK.SUBCLASSCD NOT IN ('333333', '555555', '999999') ");
            stb.append(" INNER JOIN ");
            stb.append("   SUBCLASS_MST SUBCLASS ");
            stb.append("    ON SUBCLASS.CLASSCD = RANK.CLASSCD ");
            stb.append("   AND SUBCLASS.SCHOOL_KIND = RANK.SCHOOL_KIND ");
            stb.append("   AND SUBCLASS.CURRICULUM_CD = RANK.CURRICULUM_CD ");
            stb.append("   AND SUBCLASS.SUBCLASSCD = RANK.SUBCLASSCD ");
            stb.append(" WHERE ");
            stb.append("   REGD.YEAR = '" + _param._year + "' AND ");
            stb.append("   REGD.SEMESTER = '" + _param._seme + "' AND ");
            stb.append("   REGD.GRADE = '" + _param._gradeHrclass.substring(0,2) + "' ");
            stb.append("   AND NOT EXISTS  ");
            stb.append("       (SELECT 'X' ");
            stb.append("        FROM SUBCLASS_REPLACE_COMBINED_DAT ");
            stb.append("        WHERE YEAR = '" + _param._year + "' ");
            stb.append("          AND ATTEND_CLASSCD = SUBCLASS.CLASSCD ");
            stb.append("          AND ATTEND_SCHOOL_KIND = SUBCLASS.SCHOOL_KIND ");
            stb.append("          AND ATTEND_CURRICULUM_CD = SUBCLASS.CURRICULUM_CD ");
            stb.append("          AND ATTEND_SUBCLASSCD = SUBCLASS.SUBCLASSCD) ");
            stb.append("   AND NOT EXISTS  ");
            stb.append("       (SELECT 'X' ");
            stb.append("        FROM SCHREG_TRANSFER_DAT S1 ");
            stb.append("        WHERE   S1.SCHREGNO = REGD.SCHREGNO ");
            stb.append("          AND S1.TRANSFERCD IN ('2') "); //休学者除く
            stb.append("          AND TRANSFER_SDATE <= '" + sdate+ "' AND S1.TRANSFER_EDATE >= '" + edate + "') ");
            stb.append(" ORDER BY ");
            stb.append("   REGD.COURSECD, REGD.MAJORCD, REGD.COURSECODE, SUBCLASS.SUBCLASSCD, RANK.SCORE ");
            stb.append(" ) ");
            //学年全科目平均
            if(avg) {
                stb.append(" SELECT ");
                stb.append("   SCORE.SCHREGNO, ");
                stb.append("   SCORE.COURSECD, ");
                stb.append("   SCORE.MAJORCD, ");
                stb.append("   SCORE.COURSECODE, ");
                stb.append("   FLOAT(SUM(SCORE.SCORE)) / " + courseAvg._subclassMap.size() + " AS AVG ");
                stb.append(" FROM SCORE ");
                stb.append(" WHERE ");
                stb.append("   SCORE.COURSECD = '" + courseAvg._coursecd + "' AND ");
                stb.append("   SCORE.MAJORCD = '" + courseAvg._majorcd + "' AND ");
                stb.append("   SCORE.COURSECODE = '" + courseAvg._coursecode + "' ");
                stb.append(" GROUP BY ");
                stb.append("     SCORE.SCHREGNO, SCORE.COURSECD, SCORE.MAJORCD, SCORE.COURSECODE ");
                stb.append(" ORDER BY ");
                stb.append("     AVG, SCORE.SCHREGNO, SCORE.COURSECD, SCORE.MAJORCD, SCORE.COURSECODE ");
            } else {
                stb.append(" SELECT * FROM SCORE");
            }

            log.debug(" sql =" + stb.toString());
            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();
            BigDecimal total = new BigDecimal(0);
            int cnt = 1;
            while (rs.next()) {
                if(avg) {
                    final String allAvg = rs.getString("AVG");
                    retMap = setDosuuBunpu(retMap, (int)Double.parseDouble(allAvg));
                    BigDecimal wk = new BigDecimal(allAvg);
                    total = total.add(wk);
                    cnt++;
                } else {
                    final String coursecd = rs.getString("COURSECD");
                    final String majorcd = rs.getString("MAJORCD");
                    final String coursecode = rs.getString("COURSECODE");
                    final String classcd = rs.getString("CLASSCD");
                    final String school_Kind = rs.getString("SCHOOL_KIND");
                    final String curriculum_Cd = rs.getString("CURRICULUM_CD");
                    final String subclasscd = rs.getString("SUBCLASSCD");
                    final String score = rs.getString("SCORE");
                    final String subclassname = rs.getString("SUBCLASSNAME");
                    final String subclassabbv = rs.getString("SUBCLASSABBV");

                    final String key = coursecd + "-" + majorcd + "-" + coursecode;
                    if(retMap.containsKey(key)) {
                        course = (Course)retMap.get(key);
                    } else {
                        course = new Course(coursecd, majorcd, coursecode);
                        retMap.put(key, course);
                    }

                    final String subclassKey = classcd + "-" + school_Kind + "-" + curriculum_Cd + "-" + subclasscd;
                    if(course._subclassMap.containsKey(subclassKey)) {
                        subclass = (SubclassMst)course._subclassMap.get(subclassKey);
                    } else {
                        subclass = new SubclassMst(classcd, school_Kind, curriculum_Cd, subclasscd, subclassname, subclassabbv);
                        subclass.getGradeAvg(db2, course); //学年平均
                        course._subclassMap.put(subclassKey, subclass);
                    }
                    subclass._dosuuBunpuMap = setDosuuBunpu(subclass._dosuuBunpuMap, Integer.parseInt(score));
                }
            }

            if(avg) {
                courseAvg._avgAvg = total.divide(new BigDecimal(cnt), 1, BigDecimal.ROUND_HALF_UP).toString();
            }
        } catch (final SQLException e) {
            log.error("度数分布表の基本情報取得でエラー", e);
            throw e;
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }

        return retMap;

    }

    private class Course {
        final String _coursecd;
        final String _majorcd;
        final String _coursecode;
        final Map _subclassMap;
        Map _avgDosuuBunpuMap;
        String _avgAvg;

        public Course(final String coursecd, final String majorcd, final String coursecode) {
            _coursecd = coursecd;
            _majorcd = majorcd;
            _coursecode = coursecode;
            _subclassMap = new LinkedMap();
            _avgDosuuBunpuMap = new LinkedMap();
        }
    }

    private Map setDosuuBunpu(final Map retMap, final int score) {
        for(int low = 0; low <= 100; low += 10) {
            final int high = low + 10;
            if(low <= score && score < high) {
                if(retMap.containsKey(low)) {
                    String wk =(String)retMap.get(low);
                    int wk2 = (Integer.parseInt(wk) + 1);
                    retMap.put(low, String.valueOf(wk2));
                } else {
                    retMap.put(low, "1");
                }
            }
        }
        return retMap;
    }

    private class SubclassMst {
        final String _classCd;
        final String _schoolKind;
        final String _curriculumCd;
        final String _subclassCd;
        final String _subclassName;
        final String _subclassAbbv;
        Map _dosuuBunpuMap;
        String _avg;

        public SubclassMst(final String classCd, final String schoolKind, final String curriculumCd, final String subclassCd, final String subclassName, final String subclassAbbv) {
            _classCd = classCd;
            _schoolKind = schoolKind;
            _curriculumCd = curriculumCd;
            _subclassCd = subclassCd;
            _subclassName = subclassName;
            _subclassAbbv = subclassAbbv;
            _dosuuBunpuMap = new LinkedMap();
        }

        public void getGradeAvg(final DB2UDB db2, final Course course) throws SQLException {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     AVG ");
            stb.append(" FROM ");
            stb.append("     RECORD_AVERAGE_SDIV_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _param._year + "' AND ");
            stb.append("     SEMESTER = '" + _param._semester + "' AND ");
            stb.append("     TESTKINDCD = '" + _param._testkindcd + "' AND ");
            stb.append("     TESTITEMCD = '" + _param._testitemcd + "' AND ");
            stb.append("     SCORE_DIV = '" + _param._scoreDiv + "' AND ");
            stb.append("     CLASSCD = '" + _classCd + "' AND ");
            stb.append("     SCHOOL_KIND = '" + _schoolKind + "' AND ");
            stb.append("     CURRICULUM_CD = '" + _curriculumCd + "' AND ");
            stb.append("     SUBCLASSCD = '" + _subclassCd + "' AND ");
            stb.append("     AVG_DIV = '3' AND "); //コース
            stb.append("     GRADE = '" + _param._gradeHrclass.substring(0, 2) + "' AND ");
            stb.append("     HR_CLASS = '000' AND ");
            stb.append("     COURSECD = '" + course._coursecd + "' AND ");
            stb.append("     MAJORCD = '" + course._majorcd + "' AND ");
            stb.append("     COURSECODE = '" + course._coursecode + "' ");

            _avg = KnjDbUtils.getOne(KnjDbUtils.query(db2, stb.toString()));
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Id:$");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _year;
        final String _semester;
        final String _loginSemester;
        final String _seme;
        final String _testcd;
        final String _testkindcd;
        final String _testitemcd;
        final String _scoreDiv;
        final String _gradeHrclass;
        final String[] _categorySelected;
        final String _gradeName;
        final String _hrName;
        final Map _semesterMst;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("CTRL_YEAR");
            _semester = request.getParameter("SEMESTER");
            _loginSemester = request.getParameter("CTRL_SEMESTER");
            _gradeHrclass = request.getParameter("GRADE_HRCLASS");
            _categorySelected = request.getParameterValues("category_selected");
            _semesterMst = setSemesterMst(db2);

            _seme = "9".equals(_semester) ? _loginSemester : _semester;
            _testcd = request.getParameter("TESTCD");
            _testkindcd = _testcd.substring(0, 2);
            _testitemcd = _testcd.substring(2, 4);
            _scoreDiv = _testcd.substring(4);
            _gradeName = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT GRADE_NAME2 FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND GRADE = '" + _gradeHrclass.substring(0,2) + "' AND SCHOOL_KIND = 'H'"));
            _hrName = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT HR_CLASS_NAME1 FROM SCHREG_REGD_HDAT WHERE YEAR = '" + _year + "' AND SEMESTER = '" + _seme + "' AND GRADE || HR_CLASS = '" + _gradeHrclass + "'"));
        }

        private Map setSemesterMst(final DB2UDB db2) throws SQLException {
            final Map retMap = new LinkedMap();
            PreparedStatement ps = null;
            ResultSet rs = null;

            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     SEMESTERNAME, SDATE, EDATE ");
            stb.append(" FROM ");
            stb.append("     SEMESTER_MST ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _year + "' AND ");
            stb.append("     SEMESTER = '" + _semester + "' ");

            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    retMap.put("SEMESTERNAME", rs.getString("SEMESTERNAME"));
                    retMap.put("SDATE", rs.getString("SDATE"));
                    retMap.put("EDATE", rs.getString("EDATE"));
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retMap;
        }
    }
}

// eof

