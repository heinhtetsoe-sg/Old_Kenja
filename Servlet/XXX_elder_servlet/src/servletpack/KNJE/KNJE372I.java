// kanji=漢字
package servletpack.KNJE;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJE.KNJE070_1.HyoteiHeikin;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 * <<クラスの説明>>。
 * @author nakamoto
 * @version $Id$
 */
public class KNJE372I {

    private static final Log log = LogFactory.getLog("KNJE372I.class");

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

            if("1".equals(_param._disp)) {
                for (int i = 0; i < _param._categorySelected.length; i++) {
                    final GradeHrCls hrClass = Student.load(db2, _param, _param._categorySelected[i]);
                    if (hrClass == null) continue;
                    log.info(" gradeHrclass " + _param._categorySelected[i] + " size = " + hrClass._studentList.size());
                    // 印刷処理
                    printMain(db2, svf, hrClass);
                    _hasData = true;
                }
            } else {
                final GradeHrCls hrClass = Student.load(db2, _param, "");
                if (hrClass != null) {
                    printMain(db2, svf, hrClass);
                    _hasData = true;
                }
            }
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

    private boolean printMain(final DB2UDB db2, final Vrw32alp svf, final GradeHrCls hrClass) throws SQLException {

        final int MaxCnt = 10;
        int count = 1;
        int line  = 0;
        for (final Student student : hrClass._studentList) {
            if(student._declineFlg != null) continue; //辞退者は対象外

            if(count % 10 == 1) {
                svf.VrEndPage();
                svf.VrSetForm("KNJE372I.frm", 1);

                count = 1;
                line = 0;
            }

            if(count % 2 == 1) {
                line++;
            }

            final String fieldCnt = count % 2 == 1 ? "1" : "2";
            final String bangou = String.valueOf(Integer.parseInt(student._attendno)) + "番";
            svf.VrsOutn("HR_NAME" + fieldCnt, line, hrClass._hrName  + "　" + bangou  ); // 年組番
            svf.VrsOutn("DATE" + fieldCnt, line, h_format_Seireki_MD(_param._outputDate.replace("/", "-"))); // 日付

            final int size = KNJ_EditEdit.getMS932ByteLength(student._name);
            final String fieldName = size < 20 ? "1" : size < 30 ? "2" : "3";

            svf.VrsOutn("NAME" + fieldCnt + "_" + fieldName, line, student._name); // 氏名

            if(student._hyoutei != null) {
                svf.VrsOutn("DIV" + fieldCnt, line, student._gaihyo); // 評定段階
                svf.VrsOutn("DIV_RANK" + fieldCnt, line, printScore(student._hyoutei)); // 評定
            }

            svf.VrsOutn("CONV_RANK" + fieldCnt, line, student._convScore._convert_Rank); // 換算値 順位
            svf.VrsOutn("RANK" + fieldCnt + "_1", line, student._convScore._rank1); // 学力試験1 順位
            svf.VrsOutn("RANK" + fieldCnt + "_2", line, student._convScore._rank2); // 学力試験2 順位
            svf.VrsOutn("RANK" + fieldCnt + "_3", line, student._sougouRank); // 総合成績 順位

            count++;

        }
        svf.VrEndPage();
        return true;
    }

    private static class GradeHrCls {
        final String _grade;
        final String _gradeCd;
        final String _hrclass;
        final String _hrName;
        final List<Student> _studentList = new LinkedList();

        GradeHrCls(
                final String grade,
                final String gradeCd,
                final String hrclass,
                final String hrName
                ) {
            _grade = grade;
            _gradeCd = gradeCd;
            _hrclass = hrclass;
            _hrName = hrName;
        }
    }

    private static class ConvertScore {
        String _attend_adjustment_score; //出欠調整点
        String _adjustment_score; //調整点
        String _adjustment_Goukei; //調整点合計
        String _convert_Score; //換算値
        String _convert_Rank; //換算値順位
        String _convert_Deviation; //偏差値
        String _recommendation_Department; //推薦学科
        String _proficiency1_Score1; //実力テスト１数学
        String _proficiency1_Score2; //実力テスト１英語
        String _proficiency1_Score3; //実力テスト１選択
        String _proficiency1_Goukei; //実力テスト１合計
        String _rank1; //実力テスト１順位
        String _proficiency2_Score1; //実力テスト２数学
        String _proficiency2_Score2; //実力テスト２英語
        String _proficiency2_Score3; //実力テスト２選択
        String _proficiency2_Goukei; //実力テスト２合計
        String _rank2; //実力テスト2順位

        public ConvertScore(
                final String attend_adjustment_Score,
                final String adjustment_Score,
                final String adjustment_Goukei,
                final String convert_Score,
                final String convert_Rank,
                final String convert_Deviation,
                final String recommendation_Department,
                final String proficiency1_Score1,
                final String proficiency1_Score2,
                final String proficiency1_Score3,
                final String proficiency1_Goukei,
                final String rank1,
                final String proficiency2_Score1,
                final String proficiency2_Score2,
                final String proficiency2_Score3,
                final String proficiency2_Goukei,
                final String rank2
                ) {
            _attend_adjustment_score = attend_adjustment_Score;
            _adjustment_score = adjustment_Score;
            _adjustment_Goukei = adjustment_Goukei;
            _convert_Score = convert_Score;
            _convert_Rank = convert_Rank;
            _convert_Deviation = convert_Deviation;
            _recommendation_Department = recommendation_Department;
            _proficiency1_Score1 = proficiency1_Score1;
            _proficiency1_Score2 = proficiency1_Score2;
            _proficiency1_Score3 = proficiency1_Score3;
            _proficiency1_Goukei = proficiency1_Goukei;
            _rank1 = rank1;
            _proficiency2_Score1 = proficiency2_Score1;
            _proficiency2_Score2 = proficiency2_Score2;
            _proficiency2_Score3 = proficiency2_Score3;
            _proficiency2_Goukei = proficiency2_Goukei;
            _rank2 = rank2;
        }

    }

    private static class Student {
        final String _schregno; //学籍番号
        final String _grade; //学年
        final String _hr_Class; //クラス
        final String _attendno; //番号
        final String _hr_Name; //クラス名
        final String _name; //氏名
        final String _declineFlg; //辞退区分
        String _hyoutei; //評定
        String _gaihyo; //概評
        String _sougouRank; //総合成績順位
        final ConvertScore _convScore;
        public Student(
                final String schregno, final String grade,
                final String hr_Class, final String attendno, final String hr_Name, final String name, final ConvertScore convScore, final String declineFlg) {
            _schregno = schregno;
            _grade = grade;
            _hr_Class = hr_Class;
            _attendno = attendno;
            _hr_Name = hr_Name;
            _name = name;
            _convScore = convScore;
            _declineFlg = declineFlg;
        }
        private static GradeHrCls load(final DB2UDB db2, final Param param, final String gradeHrclass) {
            GradeHrCls ghCls = null;

            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH GOUKEI_SCORE AS ( ");
            stb.append(" SELECT ");
            stb.append("     SCHREGNO, ");
            stb.append("     VALUE(PROFICIENCY1_SCORE1,0) + VALUE(PROFICIENCY1_SCORE2,0) + VALUE(PROFICIENCY1_SCORE3,0) AS PROFICIENCY1_GOUKEI, ");
            stb.append("     VALUE(PROFICIENCY2_SCORE1,0) + VALUE(PROFICIENCY2_SCORE2,0) + VALUE(PROFICIENCY2_SCORE3,0) AS PROFICIENCY2_GOUKEI ");
            stb.append(" FROM AFT_SCHREG_CONVERT_SCORE_DAT ");
            stb.append(" WHERE YEAR = '" + param._year + "' ");
            stb.append(" ), GOUKEI_RANK1 AS ( ");
            stb.append(" SELECT ");
            stb.append("    SCHREGNO, ");
            stb.append("    PROFICIENCY1_GOUKEI, ");
            stb.append("    RANK() OVER(ORDER BY PROFICIENCY1_GOUKEI DESC) AS RANK1 ");
            stb.append(" FROM GOUKEI_SCORE ");
            stb.append(" ), GOUKEI_RANK2 AS ( ");
            stb.append(" SELECT ");
            stb.append("    SCHREGNO, ");
            stb.append("    PROFICIENCY2_GOUKEI, ");
            stb.append("    RANK() OVER(ORDER BY PROFICIENCY2_GOUKEI DESC) AS RANK2 ");
            stb.append(" FROM GOUKEI_SCORE ");
            stb.append(" ) ");
            stb.append("SELECT T1.SCHREGNO, T1.GRADE, T1.HR_CLASS, T1.ATTENDNO, BASE.NAME, HDAT.HR_NAME,GDAT.GRADE_CD, ");
            stb.append("     CONVDAT.YEAR, ");
            stb.append("     CONVDAT.SCHREGNO, ");
            stb.append("     CONVDAT.PROFICIENCY1_SCORE1, ");
            stb.append("     CONVDAT.PROFICIENCY1_SCORE2, ");
            stb.append("     CONVDAT.PROFICIENCY1_SCORE3, ");
            stb.append("     CONVDAT.PROFICIENCY2_SCORE1, ");
            stb.append("     CONVDAT.PROFICIENCY2_SCORE2, ");
            stb.append("     CONVDAT.PROFICIENCY2_SCORE3, ");
            stb.append("     CONVDAT.ATTEND_ADJUSTMENT_SCORE, ");
            stb.append("     CONVDAT.ADJUSTMENT_SCORE, ");
            stb.append("     VALUE(CONVDAT.ATTEND_ADJUSTMENT_SCORE,0) + VALUE(CONVDAT.ADJUSTMENT_SCORE,0) AS ADJUSTMENT_GOUKEI, ");
            stb.append("     CONVDAT.CONVERT_SCORE, ");
            stb.append("     CONVDAT.CONVERT_RANK, ");
            stb.append("     CONVDAT.CONVERT_DEVIATION, ");
            stb.append("     CONVDAT.RECOMMENDATION_DEPARTMENT_CD, ");
            stb.append("     RANK1.PROFICIENCY1_GOUKEI, ");
            stb.append("     RANK1.RANK1, ");
            stb.append("     RANK2.PROFICIENCY2_GOUKEI, ");
            stb.append("     RANK2.RANK2, ");
            stb.append("     RECODAT.DECLINE_FLG ");
            stb.append("FROM SCHREG_REGD_DAT T1 ");
            stb.append("INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = T1.SCHREGNO ");
            stb.append("LEFT JOIN SCHREG_REGD_HDAT HDAT ON HDAT.YEAR = T1.YEAR ");
            stb.append("    AND HDAT.SEMESTER = T1.SEMESTER ");
            stb.append("    AND HDAT.GRADE = T1.GRADE ");
            stb.append("    AND HDAT.HR_CLASS = T1.HR_CLASS ");
            stb.append("LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR AND GDAT.GRADE = T1.GRADE ");
            stb.append("LEFT JOIN AFT_SCHREG_CONVERT_SCORE_DAT CONVDAT ON CONVDAT.YEAR = T1.YEAR AND CONVDAT.SCHREGNO = T1.SCHREGNO ");
            stb.append("LEFT JOIN GOUKEI_RANK1 RANK1 ON RANK1.SCHREGNO = T1.SCHREGNO ");
            stb.append("LEFT JOIN GOUKEI_RANK2 RANK2 ON RANK2.SCHREGNO = T1.SCHREGNO ");
            stb.append("LEFT JOIN AFT_SCHREG_RECOMMENDATION_INFO_DAT RECODAT ON RECODAT.YEAR = T1.YEAR AND RECODAT.SCHREGNO = T1.SCHREGNO ");
            stb.append("WHERE   T1.YEAR = '" + param._year + "' ");
            stb.append(    "AND T1.SEMESTER = '" + param._login_semester + "' ");
            if("1".equals(param._disp)) {
                stb.append(    "AND T1.GRADE || T1.HR_CLASS = '" + gradeHrclass + "' ");
            } else {
                stb.append(    "AND T1.SCHREGNO IN " + SQLUtils.whereIn(true, param._categorySelected));
            }
            stb.append(    "AND CONVDAT.CONVERT_SCORE IS NOT NULL "); //換算値がnullの生徒は対象外
            stb.append("ORDER BY T1.ATTENDNO");

            for (final Map<String, String> row : KnjDbUtils.query(db2, stb.toString())) {

                final ConvertScore convScore = new ConvertScore(
                        KnjDbUtils.getString(row, "ATTEND_ADJUSTMENT_SCORE"),
                        KnjDbUtils.getString(row, "ADJUSTMENT_SCORE"),
                        KnjDbUtils.getString(row, "ADJUSTMENT_GOUKEI"),
                        KnjDbUtils.getString(row, "CONVERT_SCORE"),
                        KnjDbUtils.getString(row, "CONVERT_RANK"),
                        KnjDbUtils.getString(row, "CONVERT_DEVIATION"),
                        KnjDbUtils.getString(row, "RECOMMENDATION_DEPARTMENT_CD"),
                        KnjDbUtils.getString(row, "PROFICIENCY1_SCORE1"),
                        KnjDbUtils.getString(row, "PROFICIENCY1_SCORE2"),
                        KnjDbUtils.getString(row, "PROFICIENCY1_SCORE3"),
                        KnjDbUtils.getString(row, "PROFICIENCY1_GOUKEI"),
                        KnjDbUtils.getString(row, "RANK1"),
                        KnjDbUtils.getString(row, "PROFICIENCY2_SCORE1"),
                        KnjDbUtils.getString(row, "PROFICIENCY2_SCORE2"),
                        KnjDbUtils.getString(row, "PROFICIENCY2_SCORE3"),
                        KnjDbUtils.getString(row, "PROFICIENCY2_GOUKEI"),
                        KnjDbUtils.getString(row, "RANK2")
                        );

                final Student student = new Student(
                        KnjDbUtils.getString(row, "SCHREGNO"),
                        KnjDbUtils.getString(row, "GRADE"),
                        KnjDbUtils.getString(row, "HR_CLASS"),
                        KnjDbUtils.getString(row, "ATTENDNO"),
                        KnjDbUtils.getString(row, "HR_NAME"),
                        KnjDbUtils.getString(row, "NAME"),
                        convScore,
                        KnjDbUtils.getString(row, "DECLINE_FLG")
                );


                student.setSougouScore(db2,param); //総合成績
                student.setHyoutei(db2, param); //評定


                if(ghCls == null) {
                    ghCls = new GradeHrCls(KnjDbUtils.getString(row, "GRADE"),
                            KnjDbUtils.getString(row, "GRADE_CD"),
                            KnjDbUtils.getString(row, "HR_CLASS"),
                            KnjDbUtils.getString(row, "HR_NAME")
                    );
                }

                ghCls._studentList.add(student);
            }

            return ghCls;
        }
        private void setHyoutei(final DB2UDB db2,final Param param) {
            KNJDefineSchool defineSchool = new KNJDefineSchool();
            defineSchool.defineCode(db2, param._year);
            final KNJE070_1 knje070_1 = new KNJE070_1(db2, (Vrw32alp) null, defineSchool, (String) null);
            try {
                final List<HyoteiHeikin> hyoteiHeikinList = knje070_1.getHyoteiHeikinList(_schregno, param._year, param._semester, param._knje070Paramap);
                for (final KNJE070_1.HyoteiHeikin heikin : hyoteiHeikinList) {
                    if ("TOTAL".equals(heikin.classkey())) {
                        _hyoutei = heikin.avg();
                        _gaihyo = heikin.gaihyo();
                    }
                }
            } catch (Throwable t) {
                log.warn("KNJE070 failed.", t);
            }
            knje070_1.pre_stat_f();
        }

        private void setSougouScore(final DB2UDB db2,final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH GRADE_SCHREGNO AS ( ");
            stb.append(" SELECT ");
            stb.append("     * ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + param._year + "' AND ");
            stb.append("     SEMESTER = '" + param._login_semester + "' AND ");

            if("1".equals(param._disp)) {
                stb.append("     GRADE = '" + param._grade + "' ");
            } else {
                stb.append("     GRADE = '" + _grade + "' ");
            }
            stb.append(" ), ALLSCORE AS ( ");
            stb.append(" SELECT ");
            stb.append("     T2.YEAR, ");
            stb.append("     T2.SEMESTER, ");
            stb.append("     T2.TESTKINDCD, ");
            stb.append("     T2.TESTITEMCD, ");
            stb.append("     T2.SCORE_DIV, ");
            stb.append("     T2.CLASSCD, ");
            stb.append("     T2.SCHOOL_KIND, ");
            stb.append("     T2.CURRICULUM_CD, ");
            stb.append("     T2.SUBCLASSCD, ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T2.AVG ");
            stb.append(" FROM ");
            stb.append("     GRADE_SCHREGNO T1");
            stb.append(" INNER JOIN ");
            stb.append("     RECORD_RANK_SDIV_DAT T2 ON T2.YEAR = T1.YEAR AND ");
            stb.append("     T2.SEMESTER || T2.TESTKINDCD || T2.TESTITEMCD || T2.SCORE_DIV = '"+ param._semester + param._testKindCd + "' AND ");
            stb.append("     T2.SCHREGNO = T1.SCHREGNO AND ");
            stb.append("     T2.CLASSCD = '99' AND ");
            stb.append("     T2.SUBCLASSCD = '999999' AND ");
            stb.append("     T2.SCHOOL_KIND = 'H' ");
            //1年生以外なら過去の成績を取得
            //固定：9990008 ALL9
            if(!param.isGrade1) {
                stb.append(" UNION ");
                stb.append(" SELECT ");
                stb.append("     T2.YEAR, ");
                stb.append("     T2.SEMESTER, ");
                stb.append("     T2.TESTKINDCD, ");
                stb.append("     T2.TESTITEMCD, ");
                stb.append("     T2.SCORE_DIV, ");
                stb.append("     T2.CLASSCD, ");
                stb.append("     T2.SCHOOL_KIND, ");
                stb.append("     T2.CURRICULUM_CD, ");
                stb.append("     T2.SUBCLASSCD, ");
                stb.append("     T1.SCHREGNO, ");
                stb.append("     T2.AVG ");
                stb.append(" FROM ");
                stb.append("     GRADE_SCHREGNO T1");
                stb.append(" INNER JOIN ");
                stb.append("     RECORD_RANK_SDIV_DAT T2 ON T2.YEAR < '" + param._year + "' AND ");
                stb.append("     T2.SEMESTER || T2.TESTKINDCD || T2.TESTITEMCD || T2.SCORE_DIV = '9990008' AND ");
                stb.append("     T2.SCHREGNO = T1.SCHREGNO AND ");
                stb.append("     T2.CLASSCD = '99' AND ");
                stb.append("     T2.SUBCLASSCD = '999999' AND ");
                stb.append("     T2.SCHOOL_KIND = 'H' ");
            }
            stb.append(" ), ALLSUMSCORE AS ( ");
            stb.append(" SELECT ");
            stb.append("     'ZZZZ' AS YEAR, ");
            stb.append("     'Z' AS SEMESTER, ");
            stb.append("     'ZZ' AS TESTKINDCD, ");
            stb.append("     'ZZ' AS TESTITEMCD, ");
            stb.append("     'ZZ' AS SCORE_DIV, ");
            stb.append("     'ZZ' AS CLASSCD, ");
            stb.append("     'Z' AS SCHOOL_KIND, ");
            stb.append("     '99' AS CURRICULUM_CD, ");
            stb.append("     '999999' AS SUBCLASSCD, ");
            stb.append("     SCHREGNO, ");
            stb.append("     SUM(AVG) AS AVG ");
            stb.append(" FROM ");
            stb.append("    ALLSCORE ");
            stb.append(" GROUP BY ");
            stb.append("    SCHREGNO ");
            stb.append(" ), RANK AS ( ");
            stb.append(" SELECT ");
            stb.append(" *,RANK() OVER(ORDER BY AVG DESC) AS RANK ");
            stb.append(" FROM ALLSUMSCORE ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("    * ");
            stb.append(" FROM RANK ");
            stb.append(" WHERE SCHREGNO = '" + _schregno + "'  ");


            //学年分の成績を取得
            for (final Map<String, String> row : KnjDbUtils.query(db2, stb.toString())) {
                if("ZZZZ".equals(KnjDbUtils.getString(row, "YEAR"))) {
                    _sougouRank = KnjDbUtils.getString(row, "RANK");
                }
            }
        }
    }

    private class AssessDat {
        final String _assesslevel;
        final String _assessmark;
        final String _assesslow;
        final String _assesshigh;
        public AssessDat (final String assesslevel, final String assessmark, final String assesslow, final String assesshigh)
        {
            _assesslevel = assesslevel;
            _assessmark = assessmark;
            _assesslow = assesslow;
            _assesshigh = assesshigh;
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        final Param param = new Param(db2, request);
        log.fatal("$Id$");
        KNJServletUtils.debugParam(request, log);
        return param;
    }

    /** パラメータクラス */
    private class Param {
        final String _year;
        final String _grade;
        final String _useCurriculumcd;
        final String _useSchool_KindField;
        final String _semester;
        final String _login_semester;
        final String _schoolKind;
        final String _schoolCd;
        final String[] _categorySelected;
        final String _schoolName;
        final String _testKindCd;
        final String _gradeCd;
        final boolean isGrade1;
        final boolean isGrade2;
        final boolean isGrade3;
        final String _disp;
        final String _outputDate;
        final Map _knje070Paramap;


        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _grade = request.getParameter("GRADE");
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            _schoolKind = request.getParameter("SCHOOLKIND");
            _schoolCd = request.getParameter("SCHOOLCD");
            _outputDate = request.getParameter("OUTPUTDATE");
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _schoolName = getSchoolName(db2);
            _semester = request.getParameter("SEMESTER");
            _testKindCd = request.getParameter("TESTKINDCD");
            _login_semester = request.getParameter("LOGIN_SEMESTER");
            _disp = request.getParameter("DISP");
            _gradeCd = getGradeCd(db2);
            isGrade1 = "01".equals(_gradeCd);
            isGrade2 = "02".equals(_gradeCd);
            isGrade3 = "03".equals(_gradeCd);
            _knje070Paramap = new KNJE070().createParamMap(request);
            _knje070Paramap.put("DOCUMENTROOT", request.getParameter("DOCUMENTROOT"));
            _knje070Paramap.put("totalOnly", "1");

        }
        private String getSchoolName(final DB2UDB db2) throws SQLException {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT SCHOOLNAME1 FROM SCHOOL_MST WHERE YEAR = '" + _year + "' AND SCHOOLCD = '" + _schoolCd + "' AND SCHOOL_KIND = '" + _schoolKind + "'"));
        }
        private String getGradeCd(final DB2UDB db2) throws SQLException {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT GRADE_CD FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND GRADE = '" + _grade + "'"));
        }
    }

    private String h_format_Seireki_MD(final String date) {
        if (null == date || "".equals(date)) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat();
        String retVal = "";
        sdf.applyPattern("yyyy年M月d日");
        retVal = sdf.format(java.sql.Date.valueOf(date));

        return retVal;
    }

    private String printScore(final String c) {
        if(c == null) return "";
        BigDecimal bd = new BigDecimal(c);
        BigDecimal bd1 = bd.setScale(1, BigDecimal.ROUND_HALF_UP);
        return bd1.toPlainString();
    }

}

// eof
