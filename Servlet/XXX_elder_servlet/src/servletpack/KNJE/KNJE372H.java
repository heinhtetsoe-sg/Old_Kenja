// kanji=漢字
package servletpack.KNJE;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJE.KNJE070_1.HyoteiHeikin;
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 * <<クラスの説明>>。
 * @author nakamoto
 * @version $Id$
 */
public class KNJE372H {

    private static final Log log = LogFactory.getLog("KNJE372H.class");

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

            for (int i = 0; i < _param._categorySelected.length; i++) {

                final GradeHrCls hrClass = Student.load(db2, _param, _param._categorySelected[i]);
                if(hrClass == null) continue;
                log.info(" gradeHrclass " + _param._categorySelected[i] + " size = " + hrClass._studentList.size());

                // 印刷処理
                _hasData = printMain(db2, svf,hrClass);
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

        svf.VrSetForm("KNJE372H.frm", 1);
        svf.VrsOut("TITLE", "成績換算値一覧表"); // タイトル
        svf.VrsOut("HR_NAME", hrClass._hrName); // 年組
        svf.VrsOut("MOCK_NAME1", "第1回 校内学力試験"); // テスト名
        svf.VrsOut("MOCK_NAME2", "第2回 校内学力試験"); // テスト名
        svf.VrsOut("MOCK_SUBCLASS_NAME1_1", "国語"); // 科目名
        svf.VrsOut("MOCK_SUBCLASS_NAME1_2", "英語"); // 科目名
        svf.VrsOut("MOCK_SUBCLASS_NAME1_3", "選択"); // 科目名
        svf.VrsOut("MOCK_SUBCLASS_NAME2_1", "国語"); // 科目名
        svf.VrsOut("MOCK_SUBCLASS_NAME2_2", "英語"); // 科目名
        svf.VrsOut("MOCK_SUBCLASS_NAME2_3", "選択"); // 科目名

        int line = 1; // 印字ライン

        for (final Student student : hrClass._studentList) {
            svf.VrsOutn("NO", line, String.valueOf(Integer.parseInt(student._attendno))); // 出席番号
            svf.VrsOutn("NAME", line, student._name); // 氏名


            if(student._hyoutei != null) {
                final String printhyoutei = printScore(student._hyoutei);
                svf.VrsOutn("DEVI", line, printhyoutei); // 評定値
                svf.VrsOutn("DEVI_LETTER", line, student._gaihyo); // 評定マーク
            }

            svf.VrsOutn("ATTEND", line, student._convScore._attend_adjustment_score); // 出欠
            svf.VrsOutn("ATTEND_ADJUST", line, student._convScore._adjustment_score); // 調整
            svf.VrsOutn("ATTEND_TOTAL", line, student._convScore._adjustment_Goukei.equals("0") ? "" : student._convScore._adjustment_Goukei); // 合計

            svf.VrsOutn("CONV", line, printScore(student._convScore._convert_Score)); // 換算値
            svf.VrsOutn("CONV_RANK", line, student._convScore._convert_Rank); // 換算値順位

            //校内学力試験
            svf.VrsOutn("MOCK_SCORE1_1", line, student._convScore._proficiency1_Score1);
            svf.VrsOutn("MOCK_SCORE1_2", line, student._convScore._proficiency1_Score2);
            svf.VrsOutn("MOCK_SCORE1_3", line, student._convScore._proficiency1_Score3);
            svf.VrsOutn("MOCK_TOTAL1", line, student._convScore._proficiency1_Goukei);
            svf.VrsOutn("MOCK_RANK1", line, student._convScore._rank1);
            svf.VrsOutn("MOCK_SCORE2_1", line, student._convScore._proficiency2_Score1);
            svf.VrsOutn("MOCK_SCORE2_2", line, student._convScore._proficiency2_Score2);
            svf.VrsOutn("MOCK_SCORE2_3", line, student._convScore._proficiency2_Score3);
            svf.VrsOutn("MOCK_TOTAL2", line, student._convScore._proficiency2_Goukei);
            svf.VrsOutn("MOCK_RANK2", line, student._convScore._rank2);


            svf.VrsOutn("TOTAL_SCORE1", line, printScore(student._sougouScore1));
            if(_param.isGrade2 || _param.isGrade3) {
                svf.VrsOutn("TOTAL_SCORE2", line, printScore(student._sougouScore2));
            }
            if(_param.isGrade3) {
                svf.VrsOutn("TOTAL_SCORE3", line, printScore(student._sougouScore3));
            }
            svf.VrsOutn("TOTAL_TOTAL1", line, printScore(student._sougouGoukei));
            svf.VrsOutn("TOTAL_RANK1", line, student._sougouRank);

            //推薦学科
            String gakka = "";
            if(student._convScore._recommendation_Department != null) {
                if(_param._department_S_Map.containsKey(student._convScore._recommendation_Department)) {
                    gakka = "専)" + StringUtils.defaultString((String)_param._department_S_Map.get(student._convScore._recommendation_Department));
                } else if(_param._department_H_Map.containsKey(student._convScore._recommendation_Department)){
                    gakka = "併)" + StringUtils.defaultString((String)_param._department_H_Map.get(student._convScore._recommendation_Department));
                }
            }
            svf.VrsOutn("RECOMMEND", line, gakka);

            line++;
        }

        //帳票下段 各項目の計
        printTotal(svf,hrClass._hyouteiTotal,"DEVI_LETTER"); // 評定値
        printTotal(svf,(Total)hrClass._proficiencyTotalMap.get("CONV"),"CONV"); // 換算値
        printTotal(svf,(Total)hrClass._proficiencyTotalMap.get("1_1"),"MOCK_SCORE1_1"); // 学力試験1_1
        printTotal(svf,(Total)hrClass._proficiencyTotalMap.get("1_2"),"MOCK_SCORE1_2"); // 学力試験1_2
        printTotal(svf,(Total)hrClass._proficiencyTotalMap.get("1_3"),"MOCK_SCORE1_3"); // 学力試験1_3
        printTotal(svf,(Total)hrClass._proficiencyTotalMap.get("1_Z"),"MOCK_TOTAL1"); // 学力試験1合計
        printTotal(svf,(Total)hrClass._proficiencyTotalMap.get("2_1"),"MOCK_SCORE2_1"); // 学力試験2_1
        printTotal(svf,(Total)hrClass._proficiencyTotalMap.get("2_2"),"MOCK_SCORE2_2"); // 学力試験2_2
        printTotal(svf,(Total)hrClass._proficiencyTotalMap.get("2_3"),"MOCK_SCORE2_3"); // 学力試験2_3
        printTotal(svf,(Total)hrClass._proficiencyTotalMap.get("2_Z"),"MOCK_TOTAL2"); // 学力試験2合計
        printTotal(svf,(Total)hrClass._sougouTotalMap.get("1"),"TOTAL_SCORE1"); // 総合成績　1年
        printTotal(svf,(Total)hrClass._sougouTotalMap.get("2"),"TOTAL_SCORE2"); // 総合成績　2年
        printTotal(svf,(Total)hrClass._sougouTotalMap.get("3"),"TOTAL_SCORE3"); // 総合成績　3年
        printTotal(svf,(Total)hrClass._sougouTotalMap.get("Z"),"TOTAL_TOTAL1"); // 総合成績　合計

        svf.VrEndPage();
        return true;
    }

    private void printTotal(final Vrw32alp svf, final Total total, final String fieldName) {
        if(total != null) {
            svf.VrsOutn(fieldName, 51, total._count); // 受験者数
            svf.VrsOutn(fieldName, 52, "".equals(total._max) ? "" : printScore(total._max)); // 最高点
            svf.VrsOutn(fieldName, 53, "".equals(total._min) ? "" : printScore(total._min)); // 最低点
            svf.VrsOutn(fieldName, 54, "".equals(total._avg) ? "" : printScore(total._avg)); // 最低点
            svf.VrsOutn(fieldName, 55, "".equals(total._stdDev) ? "" : printScore(total._stdDev)); // 標準偏差
        }
    }

    private static class GradeHrCls {
        final String _grade;
        final String _gradeCd;
        final String _hrclass;
        final String _hrName;
        final List<Student> _studentList = new LinkedList();
        final Map<String, Total> _sougouTotalMap = new HashMap<String, Total>(); // 総合成績の計
        final Map<String, Total> _proficiencyTotalMap = new HashMap<String, Total>();// 学力テストの計
        Total _hyouteiTotal; //評定値の計

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

        private void setSougouTotal(final DB2UDB db2, final Param param, final String gradeHrclass) {

            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH SCHREGNO AS (SELECT ");
              stb.append("     T1.SCHREGNO, ");
              stb.append("     T1.GRADE, ");
              stb.append("     T1.HR_CLASS, ");
              stb.append("     T1.ATTENDNO, ");
              stb.append("     BASE.NAME, ");
              stb.append("     HDAT.HR_NAME, ");
              stb.append("     GDAT.GRADE_CD ");
              stb.append(" FROM ");
              stb.append("     SCHREG_REGD_DAT T1 INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = T1.SCHREGNO LEFT JOIN SCHREG_REGD_HDAT HDAT ON HDAT.YEAR = T1.YEAR     AND HDAT.SEMESTER = T1.SEMESTER     AND HDAT.GRADE = T1.GRADE     AND HDAT.HR_CLASS = T1.HR_CLASS LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR AND GDAT.GRADE = T1.GRADE ");
              stb.append(" WHERE ");
            stb.append("    T1.YEAR = '" + param._year + "' AND");
            stb.append("    T1.SEMESTER = '" + param._login_semester + "' AND");
            stb.append("    T1.GRADE || T1.HR_CLASS = '" + gradeHrclass + "' ");
              stb.append(" ORDER BY ");
              stb.append("     T1.ATTENDNO ");
            stb.append(" ), maxGY as ( "); //過年度用
            stb.append(" SELECT ");
            stb.append("     T1.schregno, ");
            stb.append("     T1.grade, ");
            stb.append("     MAX(T1.year) as year ");
            stb.append(" FROM ");
            stb.append("     schreg_regd_dat T1 ");
            stb.append(" INNER JOIN ");
            stb.append("     SCHREGNO T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append(" GROUP BY ");
            stb.append("     T1.schregno, ");
            stb.append("     T1.grade ");
              stb.append(" ), HRCLASSSCORE AS ( ");
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
              stb.append("     SCHREGNO T1 INNER JOIN RECORD_RANK_SDIV_DAT T2 ON T2.SCHREGNO = T1.SCHREGNO AND ");
             stb.append("     T2.YEAR = '" + param._year + "' AND ");
             stb.append("     T2.SEMESTER || T2.TESTKINDCD || T2.TESTITEMCD || T2.SCORE_DIV = '"+ param._semester + param._testKindCd + "' AND ");
             stb.append("     T2.CLASSCD = '99' AND ");
             stb.append("     T2.SUBCLASSCD = '999999' AND ");
             stb.append("     T2.SCHOOL_KIND = 'H' ");
              if(!param.isGrade1) { //過年度
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
                  stb.append("     maxGY T1");
                  stb.append(" INNER JOIN RECORD_RANK_SDIV_DAT T2 ON T2.SCHREGNO = T1.SCHREGNO AND ");
                  stb.append("     T2.YEAR < '" + param._year + "' AND ");
                  stb.append("     T2.YEAR = T1.YEAR AND ");
                  stb.append("     T2.SEMESTER || T2.TESTKINDCD || T2.TESTITEMCD || T2.SCORE_DIV = '9990008' AND ");
                  stb.append("     T2.CLASSCD = '99' AND ");
                  stb.append("     T2.SUBCLASSCD = '999999' AND ");
                  stb.append("     T2.SCHOOL_KIND = 'H' ");
              }
              stb.append(" )  ");
              stb.append(" SELECT ");
              stb.append("     YEAR, ");
              stb.append("     COUNT(YEAR) AS COUNT, ");
              stb.append("     MAX(AVG) AS MAX, ");
              stb.append("     MIN(AVG) AS MIN, ");
              stb.append("     AVG(AVG) AS AVG, ");
              stb.append("     DECIMAL(ROUND(STDDEV(FLOAT(AVG))*10,0)/10,5,1) AS STDDIV ");
              stb.append(" FROM ");
              stb.append("     HRCLASSSCORE ");
              stb.append(" GROUP BY ");
              stb.append("     YEAR ");

              String grade1Count = "";
              String grade2Count = "";
              String grade3Count = "";
              String grade1Max = "";
              String grade2Max = "";
              String grade3Max = "";
              String grade1Min = "";
              String grade2Min = "";
              String grade3Min = "";
              String grade1Avg = "";
              String grade2Avg = "";
              String grade3Avg = "";
              String grade1StdDiv = "";
              String grade2StdDiv = "";
              String grade3StdDiv = "";

              //学年分の成績を取得 帳票右下
            for (final Map<String, String> row : KnjDbUtils.query(db2, stb.toString())) {
                if(param.isGrade3) {
                    if(String.valueOf(Integer.parseInt(param._year) - 2).equals(KnjDbUtils.getString(row, "YEAR"))) {
                        grade1Count = KnjDbUtils.getString(row, "COUNT");
                        grade1Max = KnjDbUtils.getString(row, "MAX");
                        grade1Min = KnjDbUtils.getString(row, "MIN");
                        grade1Avg = KnjDbUtils.getString(row, "AVG");
                        grade1StdDiv = KnjDbUtils.getString(row, "STDDIV");
                    }
                    if(String.valueOf(Integer.parseInt(param._year) - 1).equals(KnjDbUtils.getString(row, "YEAR"))) {
                        grade2Count = KnjDbUtils.getString(row, "COUNT");
                        grade2Max = KnjDbUtils.getString(row, "MAX");
                        grade2Min = KnjDbUtils.getString(row, "MIN");
                        grade2Avg = KnjDbUtils.getString(row, "AVG");
                        grade2StdDiv = KnjDbUtils.getString(row, "STDDIV");
                    }
                    if(param._year.equals(KnjDbUtils.getString(row, "YEAR"))) {
                        grade3Count = KnjDbUtils.getString(row, "COUNT");
                        grade3Max = KnjDbUtils.getString(row, "MAX");
                        grade3Min = KnjDbUtils.getString(row, "MIN");
                        grade3Avg = KnjDbUtils.getString(row, "AVG");
                        grade3StdDiv = KnjDbUtils.getString(row, "STDDIV");
                    }
                } else if(param.isGrade2) {
                    if(String.valueOf(Integer.parseInt(param._year) - 1).equals(KnjDbUtils.getString(row, "YEAR"))) {
                        grade1Count = KnjDbUtils.getString(row, "COUNT");
                        grade1Max = KnjDbUtils.getString(row, "MAX");
                        grade1Min = KnjDbUtils.getString(row, "MIN");
                        grade1Avg = KnjDbUtils.getString(row, "AVG");
                        grade1StdDiv = KnjDbUtils.getString(row, "STDDIV");
                    }
                    if(param._year.equals(KnjDbUtils.getString(row, "YEAR"))) {
                        grade2Count = KnjDbUtils.getString(row, "COUNT");
                        grade2Max = KnjDbUtils.getString(row, "MAX");
                        grade2Min = KnjDbUtils.getString(row, "MIN");
                        grade2Avg = KnjDbUtils.getString(row, "AVG");
                        grade2StdDiv = KnjDbUtils.getString(row, "STDDIV");
                    }
                } else if(param.isGrade1) {
                    if(param._year.equals(KnjDbUtils.getString(row, "YEAR"))) {
                        grade1Count = KnjDbUtils.getString(row, "COUNT");
                        grade1Max = KnjDbUtils.getString(row, "MAX");
                        grade1Min = KnjDbUtils.getString(row, "MIN");
                        grade1Avg = KnjDbUtils.getString(row, "AVG");
                        grade1StdDiv = KnjDbUtils.getString(row, "STDDIV");
                    }
                }
            }

            Total Total1 = new Total(grade1Count,grade1Max,grade1Min,grade1Avg,grade1StdDiv);
            Total Total2 = new Total(grade2Count,grade2Max,grade2Min,grade2Avg,grade2StdDiv);
            Total Total3 = new Total(grade3Count,grade3Max,grade3Min,grade3Avg,grade3StdDiv);

            _sougouTotalMap.put("1", Total1);
            _sougouTotalMap.put("2", Total2);
            _sougouTotalMap.put("3", Total3);
        }

        private void setSougouGoukeiTotal(final DB2UDB db2,final Param param, final String gradeHrclass) {
            final StringBuffer stb = new StringBuffer();


            stb.append(" WITH GRADE_SCHREGNO AS (SELECT ");
            stb.append("     * ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + param._year + "' AND ");
            stb.append("     SEMESTER = '" + param._login_semester + "' AND ");
            stb.append("     GRADE || HR_CLASS = '" + gradeHrclass + "' ");
            stb.append(" ), maxGY as ( "); //過年度用
            stb.append(" SELECT ");
            stb.append("     T1.schregno, ");
            stb.append("     T1.grade, ");
            stb.append("     MAX(T1.year) as year ");
            stb.append(" FROM ");
            stb.append("     schreg_regd_dat T1 ");
            stb.append(" INNER JOIN ");
            stb.append("     GRADE_SCHREGNO T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append(" GROUP BY ");
            stb.append("     T1.schregno, ");
            stb.append("     T1.grade ");
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
                stb.append("     maxGY T1");
                stb.append(" INNER JOIN ");
                stb.append("     RECORD_RANK_SDIV_DAT T2 ON T2.YEAR < '" + param._year + "' AND ");
                stb.append("     T2.SEMESTER || T2.TESTKINDCD || T2.TESTITEMCD || T2.SCORE_DIV = '9990008' AND ");
                stb.append("     T2.YEAR = T1.YEAR AND ");
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
            stb.append(" )");
              stb.append(" SELECT ");
              stb.append("     YEAR, ");
              stb.append("     COUNT(YEAR) AS COUNT, ");
              stb.append("     MAX(AVG) AS MAX, ");
              stb.append("     MIN(AVG) AS MIN, ");
              stb.append("     AVG(AVG) AS AVG, ");
              stb.append("     DECIMAL(ROUND(STDDEV(FLOAT(AVG))*10,0)/10,5,1) AS STDDIV ");
              stb.append(" FROM ");
              stb.append("     ALLSUMSCORE ");
              stb.append(" GROUP BY ");
              stb.append("     YEAR ");


            for (final Map<String, String> row : KnjDbUtils.query(db2, stb.toString())) {

                if("ZZZZ".equals(KnjDbUtils.getString(row, "YEAR"))) {

                    Total TotalZ = new Total(KnjDbUtils.getString(row, "COUNT"),
                            KnjDbUtils.getString(row, "MAX"),
                            KnjDbUtils.getString(row, "MIN"),
                            KnjDbUtils.getString(row, "AVG"),
                            KnjDbUtils.getString(row, "STDDIV")
                        );
                    _sougouTotalMap.put("Z", TotalZ);
                }
            }
        }

        //換算値　学力試験１、２の計
        private void setConvertTotal(final DB2UDB db2, final Param param, final String gradeHrclass) {
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
            stb.append(" ), KEKKA AS ( ");
            stb.append("SELECT T1.SCHREGNO, T1.GRADE, T1.HR_CLASS, T1.ATTENDNO, BASE.NAME, HDAT.HR_NAME,GDAT.GRADE_CD, ");
            stb.append("     CONVDAT.YEAR, ");
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
            stb.append("     RANK2.RANK2 ");
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
            stb.append("WHERE   T1.YEAR = '" + param._year + "' ");
            stb.append(    "AND T1.SEMESTER = '" + param._login_semester + "' ");
            stb.append(    "AND T1.GRADE || T1.HR_CLASS = '" + gradeHrclass + "' ");
            stb.append("ORDER BY T1.ATTENDNO");
            stb.append(" ) ");
            stb.append("SELECT ");
            stb.append("     COUNT(PROFICIENCY1_SCORE1) AS PROFICIENCY1_SCORE1_COUNT, ");
            stb.append("     MAX(PROFICIENCY1_SCORE1) AS PROFICIENCY1_SCORE1_MAX, ");
            stb.append("     MIN(PROFICIENCY1_SCORE1) AS PROFICIENCY1_SCORE1_MIN, ");
            stb.append("     AVG(CAST(PROFICIENCY1_SCORE1 AS double)) AS PROFICIENCY1_SCORE1_AVG, ");
            stb.append("     DECIMAL(ROUND(STDDEV(FLOAT(PROFICIENCY1_SCORE1))*10,0)/10,5,1) AS PROFICIENCY1_SCORE1_STDDEV, ");
            stb.append("     COUNT(PROFICIENCY1_SCORE2) AS PROFICIENCY1_SCORE2_COUNT, ");
            stb.append("     MAX(PROFICIENCY1_SCORE2) AS PROFICIENCY1_SCORE2_MAX, ");
            stb.append("     MIN(PROFICIENCY1_SCORE2) AS PROFICIENCY1_SCORE2_MIN, ");
            stb.append("     AVG(CAST(PROFICIENCY1_SCORE2 AS double)) AS PROFICIENCY1_SCORE2_AVG, ");
            stb.append("     DECIMAL(ROUND(STDDEV(FLOAT(PROFICIENCY1_SCORE2))*10,0)/10,5,1) AS PROFICIENCY1_SCORE2_STDDEV, ");
            stb.append("     COUNT(PROFICIENCY1_SCORE3) AS PROFICIENCY1_SCORE3_COUNT, ");
            stb.append("     MAX(PROFICIENCY1_SCORE3) AS PROFICIENCY1_SCORE3_MAX, ");
            stb.append("     MIN(PROFICIENCY1_SCORE3) AS PROFICIENCY1_SCORE3_MIN, ");
            stb.append("     AVG(CAST(PROFICIENCY1_SCORE3 AS double)) AS PROFICIENCY1_SCORE3_AVG, ");
            stb.append("     DECIMAL(ROUND(STDDEV(FLOAT(PROFICIENCY1_SCORE3))*10,0)/10,5,1) AS PROFICIENCY1_SCORE3_STDDEV, ");
            stb.append("     COUNT(PROFICIENCY2_SCORE1) AS PROFICIENCY2_SCORE1_COUNT, ");
            stb.append("     MAX(PROFICIENCY2_SCORE1) AS PROFICIENCY2_SCORE1_MAX, ");
            stb.append("     MIN(PROFICIENCY2_SCORE1) AS PROFICIENCY2_SCORE1_MIN, ");
            stb.append("     AVG(CAST(PROFICIENCY2_SCORE1 AS double)) AS PROFICIENCY2_SCORE1_AVG, ");
            stb.append("     DECIMAL(ROUND(STDDEV(FLOAT(PROFICIENCY2_SCORE1))*10,0)/10,5,1) AS PROFICIENCY2_SCORE1_STDDEV, ");
            stb.append("     COUNT(PROFICIENCY2_SCORE2) AS PROFICIENCY2_SCORE2_COUNT, ");
            stb.append("     MAX(PROFICIENCY2_SCORE2) AS PROFICIENCY2_SCORE2_MAX, ");
            stb.append("     MIN(PROFICIENCY2_SCORE2) AS PROFICIENCY2_SCORE2_MIN, ");
            stb.append("     AVG(CAST(PROFICIENCY2_SCORE2 AS double)) AS PROFICIENCY2_SCORE2_AVG, ");
            stb.append("     DECIMAL(ROUND(STDDEV(FLOAT(PROFICIENCY2_SCORE2))*10,0)/10,5,1) AS PROFICIENCY2_SCORE2_STDDEV, ");
            stb.append("     COUNT(PROFICIENCY2_SCORE3) AS PROFICIENCY2_SCORE3_COUNT, ");
            stb.append("     MAX(PROFICIENCY2_SCORE3) AS PROFICIENCY2_SCORE3_MAX, ");
            stb.append("     MIN(PROFICIENCY2_SCORE3) AS PROFICIENCY2_SCORE3_MIN, ");
            stb.append("     AVG(CAST(PROFICIENCY2_SCORE3 AS double)) AS PROFICIENCY2_SCORE3_AVG, ");
            stb.append("     DECIMAL(ROUND(STDDEV(FLOAT(PROFICIENCY2_SCORE3))*10,0)/10,5,1) AS PROFICIENCY2_SCORE3_STDDEV, ");
            stb.append("     COUNT(PROFICIENCY1_GOUKEI) AS PROFICIENCY1_GOUKEI_COUNT, ");
            stb.append("     MAX(PROFICIENCY1_GOUKEI) AS PROFICIENCY1_GOUKEI_MAX, ");
            stb.append("     MIN(PROFICIENCY1_GOUKEI) AS PROFICIENCY1_GOUKEI_MIN, ");
            stb.append("     AVG(CAST(PROFICIENCY1_GOUKEI AS double)) AS PROFICIENCY1_GOUKEI_AVG, ");
            stb.append("     DECIMAL(ROUND(STDDEV(FLOAT(PROFICIENCY1_GOUKEI))*10,0)/10,5,1) AS PROFICIENCY1_GOUKEI_STDDEV, ");
            stb.append("     COUNT(PROFICIENCY2_GOUKEI) AS PROFICIENCY2_GOUKEI_COUNT, ");
            stb.append("     MAX(PROFICIENCY2_GOUKEI) AS PROFICIENCY2_GOUKEI_MAX, ");
            stb.append("     MIN(PROFICIENCY2_GOUKEI) AS PROFICIENCY2_GOUKEI_MIN, ");
            stb.append("     AVG(CAST(PROFICIENCY2_GOUKEI AS double)) AS PROFICIENCY2_GOUKEI_AVG, ");
            stb.append("     DECIMAL(ROUND(STDDEV(FLOAT(PROFICIENCY2_GOUKEI))*10,0)/10,5,1) AS PROFICIENCY2_GOUKEI_STDDEV, ");
            stb.append("     COUNT(CONVERT_SCORE) AS CONVERT_SCORE_COUNT, ");
            stb.append("     MAX(CONVERT_SCORE) AS CONVERT_SCORE_MAX, ");
            stb.append("     MIN(CONVERT_SCORE) AS CONVERT_SCORE_MIN, ");
            stb.append("     AVG(CAST(CONVERT_SCORE AS double)) AS CONVERT_SCORE_AVG, ");
            stb.append("     DECIMAL(ROUND(STDDEV(FLOAT(CONVERT_SCORE))*10,0)/10,5,1) AS CONVERT_SCORE_STDDEV ");
            stb.append(" FROM  ");
            stb.append("     KEKKA  ");
            stb.append(" WHERE  ");
            stb.append("     YEAR IS NOT NULL  ");
            stb.append(" GROUP BY  ");
            stb.append("     YEAR ");


            for (final Map<String, String> row : KnjDbUtils.query(db2, stb.toString())) {
                    Total total1_1 = new Total(KnjDbUtils.getString(row, "PROFICIENCY1_SCORE1_COUNT"),
                            KnjDbUtils.getString(row, "PROFICIENCY1_SCORE1_MAX"),
                            KnjDbUtils.getString(row, "PROFICIENCY1_SCORE1_MIN"),
                            KnjDbUtils.getString(row, "PROFICIENCY1_SCORE1_AVG"),
                            KnjDbUtils.getString(row, "PROFICIENCY1_SCORE1_STDDEV")
                        );

                    Total total1_2 = new Total(KnjDbUtils.getString(row, "PROFICIENCY1_SCORE2_COUNT"),
                            KnjDbUtils.getString(row, "PROFICIENCY1_SCORE2_MAX"),
                            KnjDbUtils.getString(row, "PROFICIENCY1_SCORE2_MIN"),
                            KnjDbUtils.getString(row, "PROFICIENCY1_SCORE2_AVG"),
                            KnjDbUtils.getString(row, "PROFICIENCY1_SCORE2_STDDEV")
                        );

                    Total total1_3 = new Total(KnjDbUtils.getString(row, "PROFICIENCY1_SCORE3_COUNT"),
                            KnjDbUtils.getString(row, "PROFICIENCY1_SCORE3_MAX"),
                            KnjDbUtils.getString(row, "PROFICIENCY1_SCORE3_MIN"),
                            KnjDbUtils.getString(row, "PROFICIENCY1_SCORE3_AVG"),
                            KnjDbUtils.getString(row, "PROFICIENCY1_SCORE3_STDDEV")
                        );
                    Total total2_1 = new Total(KnjDbUtils.getString(row, "PROFICIENCY2_SCORE1_COUNT"),
                            KnjDbUtils.getString(row, "PROFICIENCY2_SCORE1_MAX"),
                            KnjDbUtils.getString(row, "PROFICIENCY2_SCORE1_MIN"),
                            KnjDbUtils.getString(row, "PROFICIENCY2_SCORE1_AVG"),
                            KnjDbUtils.getString(row, "PROFICIENCY2_SCORE1_STDDEV")
                        );

                    Total total2_2 = new Total(KnjDbUtils.getString(row, "PROFICIENCY2_SCORE2_COUNT"),
                            KnjDbUtils.getString(row, "PROFICIENCY2_SCORE2_MAX"),
                            KnjDbUtils.getString(row, "PROFICIENCY2_SCORE2_MIN"),
                            KnjDbUtils.getString(row, "PROFICIENCY2_SCORE2_AVG"),
                            KnjDbUtils.getString(row, "PROFICIENCY2_SCORE2_STDDEV")
                        );

                    Total total2_3 = new Total(KnjDbUtils.getString(row, "PROFICIENCY2_SCORE3_COUNT"),
                            KnjDbUtils.getString(row, "PROFICIENCY2_SCORE3_MAX"),
                            KnjDbUtils.getString(row, "PROFICIENCY2_SCORE3_MIN"),
                            KnjDbUtils.getString(row, "PROFICIENCY2_SCORE3_AVG"),
                            KnjDbUtils.getString(row, "PROFICIENCY2_SCORE3_STDDEV")
                        );

                    Total total1_Z = new Total(KnjDbUtils.getString(row, "PROFICIENCY1_GOUKEI_COUNT"),
                            KnjDbUtils.getString(row, "PROFICIENCY1_GOUKEI_MAX"),
                            KnjDbUtils.getString(row, "PROFICIENCY1_GOUKEI_MIN"),
                            KnjDbUtils.getString(row, "PROFICIENCY1_GOUKEI_AVG"),
                            KnjDbUtils.getString(row, "PROFICIENCY1_GOUKEI_STDDEV")
                        );

                    Total total2_Z = new Total(KnjDbUtils.getString(row, "PROFICIENCY2_GOUKEI_COUNT"),
                            KnjDbUtils.getString(row, "PROFICIENCY2_GOUKEI_MAX"),
                            KnjDbUtils.getString(row, "PROFICIENCY2_GOUKEI_MIN"),
                            KnjDbUtils.getString(row, "PROFICIENCY2_GOUKEI_AVG"),
                            KnjDbUtils.getString(row, "PROFICIENCY2_GOUKEI_STDDEV")
                        );
                    Total totalConv = new Total(KnjDbUtils.getString(row, "CONVERT_SCORE_COUNT"),
                            KnjDbUtils.getString(row, "CONVERT_SCORE_MAX"),
                            KnjDbUtils.getString(row, "CONVERT_SCORE_MIN"),
                            KnjDbUtils.getString(row, "CONVERT_SCORE_AVG"),
                            KnjDbUtils.getString(row, "CONVERT_SCORE_STDDEV")
                        );


                    _proficiencyTotalMap.put("1_1", total1_1); //学力試験１国語の計
                    _proficiencyTotalMap.put("1_2", total1_2); //学力試験１英語の計
                    _proficiencyTotalMap.put("1_3", total1_3); //学力試験１選択の計
                    _proficiencyTotalMap.put("2_1", total2_1); //学力試験２国語の計
                    _proficiencyTotalMap.put("2_2", total2_2); //学力試験２英語の計
                    _proficiencyTotalMap.put("2_3", total2_3); //学力試験２選択の計
                    _proficiencyTotalMap.put("1_Z", total1_Z); //学力試験１合計の計
                    _proficiencyTotalMap.put("2_Z", total2_Z); //学力試験２合計の計
                    _proficiencyTotalMap.put("CONV", totalConv); // 換算値の計
            }
            log.info("");
        }
        private void setHyouteiTotal(final DB2UDB db2, final Param param, final String gradeHrclass) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH GRADE_SCHREGNO AS ( ");
            stb.append(" SELECT ");
            stb.append("     * ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + param._year + "' AND ");
            stb.append("     SEMESTER = '" + param._login_semester + "' AND ");
            stb.append("     GRADE || HR_CLASS = '" + gradeHrclass + "' ");
            stb.append(" ), maxGY as ( "); //過年度用
            stb.append(" SELECT ");
            stb.append("     T1.schregno, ");
            stb.append("     T1.grade, ");
            stb.append("     MAX(T1.year) as year ");
            stb.append(" FROM ");
            stb.append("     schreg_regd_dat T1 ");
            stb.append(" INNER JOIN ");
            stb.append("     GRADE_SCHREGNO T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append(" GROUP BY ");
            stb.append("     T1.schregno, ");
            stb.append("     T1.grade ");
            stb.append("), HYOUTEI AS ( ");
            stb.append(" SELECT "); //当年度評定
            stb.append("     RANK.* ");
            stb.append(" FROM ");
            stb.append("     GRADE_SCHREGNO T1 ");
            stb.append(" LEFT JOIN RECORD_RANK_SDIV_DAT RANK ON RANK.YEAR = T1.YEAR AND ");
            stb.append("     RANK.SCHREGNO = T1.SCHREGNO AND ");
            stb.append("     RANK.CLASSCD = '99' AND ");
            stb.append("     RANK.SUBCLASSCD = '999999' AND ");
            stb.append("     RANK.SEMESTER || RANK.TESTKINDCD || RANK.TESTITEMCD || RANK.SCORE_DIV = '"+ param._semester + param._testKindCd.substring(0,4) + "09' ");
            stb.append(" UNION ");
            stb.append(" SELECT "); //過年度評定
            stb.append("     RANK.* ");
            stb.append(" FROM ");
            stb.append("     maxGY T1 ");
            stb.append(" LEFT JOIN RECORD_RANK_SDIV_DAT RANK ON RANK.YEAR < '" + param._year + "' AND ");
            stb.append("     RANK.YEAR = T1.YEAR AND ");
            stb.append("     RANK.SCHREGNO = T1.SCHREGNO AND ");
            stb.append("     RANK.CLASSCD = '99' AND ");
            stb.append("     RANK.SUBCLASSCD = '999999' AND ");
            stb.append("     RANK.SEMESTER || RANK.TESTKINDCD || RANK.TESTITEMCD || RANK.SCORE_DIV = '9990009'");
            stb.append(" ), HYOUTEIAVG AS ( ");
            stb.append(" SELECT ");
            stb.append("     MAX(YEAR) AS YEAR, ");
            stb.append("     SCHREGNO, ");
            stb.append("     AVG(AVG) AS AVG ");
            stb.append(" FROM  ");
            stb.append("     HYOUTEI ");
            stb.append(" WHERE  ");
            stb.append("     YEAR IS NOT NULL ");
            stb.append(" GROUP BY  ");
            stb.append("     SCHREGNO ");
            stb.append(" )");
            stb.append(" SELECT ");
            stb.append("     COUNT(AVG) AS COUNT, ");
            stb.append("     MAX(AVG) AS MAX, ");
            stb.append("     MIN(AVG) AS MIN, ");
            stb.append("     AVG(AVG) AS AVG, ");
            stb.append("     DECIMAL(ROUND(STDDEV(FLOAT(AVG))*10,0)/10,5,1) AS STDDEV ");
            stb.append(" FROM  ");
            stb.append("     HYOUTEIAVG ");
            stb.append(" GROUP BY  ");
            stb.append("     YEAR ");

            for (final Map<String, String> row : KnjDbUtils.query(db2, stb.toString())) {
                Total totalHyoutei = new Total(KnjDbUtils.getString(row, "COUNT"),
                        KnjDbUtils.getString(row, "MAX"),
                        KnjDbUtils.getString(row, "MIN"),
                        KnjDbUtils.getString(row, "AVG"),
                        KnjDbUtils.getString(row, "STDDEV")
                    );

                _hyouteiTotal = totalHyoutei; //評定値の計
            }
        }
    }

    private static class Total {
        final String _count;
        final String _max;
        final String _min;
        final String _avg;
        final String _stdDev;

        Total(
                final String count,
                final String max,
                final String min,
                final String avg,
                final String stdDev
                ) {
            _count = count;
            _max = max;
            _min = min;
            _avg = avg;
            _stdDev = stdDev;
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
        String _sougouScore1; //1年総合成績
        String _sougouScore2; //2年総合成績
        String _sougouScore3; //3年総合成績
        String _sougouGoukei; //総合成績合計
        String _sougouRank; //総合成績順位
        String _hyoutei; //評定
        String _gaihyo; //概評
        final ConvertScore _convScore;
        public Student(
                final String schregno, final String grade,
                final String hr_Class, final String attendno, final String hr_Name, final String name, final ConvertScore convScore) {
            _schregno = schregno;
            _grade = grade;
            _hr_Class = hr_Class;
            _attendno = attendno;
            _hr_Name = hr_Name;
            _name = name;
            _convScore = convScore;
        }
        private static GradeHrCls load(final DB2UDB db2, final Param param, final String gradeHrclass) {
            GradeHrCls ghCls = null;

            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH GRADE_SCHREGNO AS ( ");
            stb.append(" SELECT ");
            stb.append("     * ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + param._year + "' AND ");
            stb.append("     SEMESTER = '" + param._login_semester + "' AND ");
            stb.append("     GRADE = '" + param._grade + "' ");
            stb.append(" ), GOUKEI_SCORE AS ( ");
            stb.append(" SELECT ");
            stb.append("     AFT_SCORE.SCHREGNO, ");
            stb.append("     CASE WHEN AFT_SCORE.PROFICIENCY1_SCORE1 IS NOT NULL ");
            stb.append("               OR AFT_SCORE.PROFICIENCY1_SCORE2 IS NOT NULL ");
            stb.append("               OR AFT_SCORE.PROFICIENCY1_SCORE3 IS NOT NULL ");
            stb.append("          THEN VALUE(AFT_SCORE.PROFICIENCY1_SCORE1, 0) + VALUE(AFT_SCORE.PROFICIENCY1_SCORE2, 0) + VALUE(AFT_SCORE.PROFICIENCY1_SCORE3, 0) ");
            stb.append("          ELSE NULL ");
            stb.append("     END AS PROFICIENCY1_GOUKEI, ");
            stb.append("     CASE WHEN AFT_SCORE.PROFICIENCY2_SCORE1 IS NOT NULL ");
            stb.append("               OR AFT_SCORE.PROFICIENCY2_SCORE2 IS NOT NULL ");
            stb.append("               OR AFT_SCORE.PROFICIENCY2_SCORE3 IS NOT NULL ");
            stb.append("          THEN VALUE(AFT_SCORE.PROFICIENCY2_SCORE1, 0) + VALUE(AFT_SCORE.PROFICIENCY2_SCORE2, 0) + VALUE(AFT_SCORE.PROFICIENCY2_SCORE3, 0) ");
            stb.append("          ELSE NULL ");
            stb.append("     END AS PROFICIENCY2_GOUKEI ");
            stb.append(" FROM ");
            stb.append("    GRADE_SCHREGNO ");
            stb.append("    INNER JOIN AFT_SCHREG_CONVERT_SCORE_DAT AFT_SCORE ON GRADE_SCHREGNO.YEAR = AFT_SCORE.YEAR ");
            stb.append("          AND GRADE_SCHREGNO.SCHREGNO = AFT_SCORE.SCHREGNO ");
            stb.append(" ), GOUKEI_RANK1 AS ( ");
            stb.append(" SELECT ");
            stb.append("    SCHREGNO, ");
            stb.append("    PROFICIENCY1_GOUKEI, ");
            stb.append("    RANK() OVER(ORDER BY PROFICIENCY1_GOUKEI DESC) AS RANK1 ");
            stb.append(" FROM ");
            stb.append("    GOUKEI_SCORE ");
            stb.append(" WHERE ");
            stb.append("     PROFICIENCY1_GOUKEI IS NOT NULL ");
            stb.append(" ), GOUKEI_RANK2 AS ( ");
            stb.append(" SELECT ");
            stb.append("    SCHREGNO, ");
            stb.append("    PROFICIENCY2_GOUKEI, ");
            stb.append("    RANK() OVER(ORDER BY PROFICIENCY2_GOUKEI DESC) AS RANK2 ");
            stb.append(" FROM ");
            stb.append("    GOUKEI_SCORE ");
            stb.append(" WHERE ");
            stb.append("     PROFICIENCY2_GOUKEI IS NOT NULL ");
            stb.append(" ) ");
            stb.append("SELECT T1.SCHREGNO, T1.GRADE, T1.HR_CLASS, T1.ATTENDNO, BASE.NAME, HDAT.HR_NAME,GDAT.GRADE_CD, ");
            stb.append("     CONVDAT.YEAR, ");
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
            stb.append("     RANK2.RANK2 ");
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
            stb.append("WHERE   T1.YEAR = '" + param._year + "' ");
            stb.append(    "AND T1.SEMESTER = '" + param._login_semester + "' ");
            stb.append(    "AND T1.GRADE || T1.HR_CLASS = '" + gradeHrclass + "' ");
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
                        convScore
                );


                student.setSougouScore(db2,param); //総合成績
                student.setHyoutei(db2, param); //評定


                if(ghCls == null) {
                    ghCls = new GradeHrCls(KnjDbUtils.getString(row, "GRADE"),
                            KnjDbUtils.getString(row, "GRADE_CD"),
                            KnjDbUtils.getString(row, "HR_CLASS"),
                            KnjDbUtils.getString(row, "HR_NAME")
                    );
                    ghCls.setSougouTotal(db2, param, gradeHrclass);
                    ghCls.setSougouGoukeiTotal(db2, param, gradeHrclass);
                    ghCls.setConvertTotal(db2, param, gradeHrclass);
                    ghCls.setHyouteiTotal(db2, param, gradeHrclass);
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
            stb.append("     GRADE = '" + param._grade + "' ");
            stb.append(" ), maxGY as ( "); //過年度用
            stb.append(" SELECT ");
            stb.append("     T1.schregno, ");
            stb.append("     T1.grade, ");
            stb.append("     MAX(T1.year) as year ");
            stb.append(" FROM ");
            stb.append("     schreg_regd_dat T1 ");
            stb.append(" INNER JOIN ");
            stb.append("     GRADE_SCHREGNO T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append(" GROUP BY ");
            stb.append("     T1.schregno, ");
            stb.append("     T1.grade ");
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
                stb.append("     maxGY T1");
                stb.append(" INNER JOIN ");
                stb.append("     RECORD_RANK_SDIV_DAT T2 ON T2.YEAR < '" + param._year + "' AND ");
                stb.append("     T2.YEAR = T1.YEAR AND ");
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
            stb.append(" *,'0' AS RANK ");
            stb.append(" FROM ALLSCORE ");
            stb.append(" WHERE SCHREGNO = '" + _schregno + "'  ");
            stb.append(" UNION ");
            stb.append(" SELECT ");
            stb.append("    * ");
            stb.append(" FROM RANK ");
            stb.append(" WHERE SCHREGNO = '" + _schregno + "'  ");


            //学年分の成績を取得
            for (final Map<String, String> row : KnjDbUtils.query(db2, stb.toString())) {

                if(param.isGrade3) {
                    if(String.valueOf(Integer.parseInt(param._year) - 2).equals(KnjDbUtils.getString(row, "YEAR"))) {
                        _sougouScore1 = KnjDbUtils.getString(row, "AVG");
                    }
                    if(String.valueOf(Integer.parseInt(param._year) - 1).equals(KnjDbUtils.getString(row, "YEAR"))) {
                        _sougouScore2 = KnjDbUtils.getString(row, "AVG");
                    }
                    if(param._year.equals(KnjDbUtils.getString(row, "YEAR"))) {
                        _sougouScore3 = KnjDbUtils.getString(row, "AVG");
                    }
                } else if(param.isGrade2) {
                    if(String.valueOf(Integer.parseInt(param._year) - 1).equals(KnjDbUtils.getString(row, "YEAR"))) {
                        _sougouScore1 = KnjDbUtils.getString(row, "AVG");
                    }
                    if(param._year.equals(KnjDbUtils.getString(row, "YEAR"))) {
                        _sougouScore2 = KnjDbUtils.getString(row, "AVG");
                    }
                } else if(param.isGrade1) {
                    if(param._year.equals(KnjDbUtils.getString(row, "YEAR"))) {
                        _sougouScore1 = KnjDbUtils.getString(row, "AVG");
                    }
                }

                if("ZZZZ".equals(KnjDbUtils.getString(row, "YEAR"))) {
                    _sougouGoukei = KnjDbUtils.getString(row, "AVG");
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
        final Map _department_S_Map;
        final Map _department_H_Map;
        final Map _knje070Paramap;


        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("YEAR");
            _grade = request.getParameter("GRADE");
            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _useSchool_KindField = request.getParameter("useSchool_KindField");
            _schoolKind = request.getParameter("SCHOOLKIND");
            _schoolCd = request.getParameter("SCHOOLCD");
            _categorySelected = request.getParameterValues("CLASS_SELECTED");
            _schoolName = getSchoolName(db2);
            _semester = request.getParameter("SEMESTER");
            _testKindCd = request.getParameter("TESTKINDCD");
            _login_semester = request.getParameter("CTRL_SEMESTER");
            _gradeCd = getGradeCd(db2);
            isGrade1 = "01".equals(_gradeCd);
            isGrade2 = "02".equals(_gradeCd);
            isGrade3 = "03".equals(_gradeCd);
            _department_S_Map = getDepartmentName(db2,"1");
            _department_H_Map = getDepartmentName(db2,"2");
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

        //学科名称取得　flg 1:専願 2:併願
        private Map getDepartmentName(final DB2UDB db2,final String flg) {
            final Map retMap = new LinkedMap();
            StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            if("1".equals(flg)) {
                stb.append("   DEPARTMENT_S, ");
            } else {
                stb.append("   DEPARTMENT_H, ");
            }
            stb.append("   DEPARTMENT_ABBV ");
            stb.append(" FROM ");
            stb.append("   AFT_RECOMMENDATION_LIMIT_MST ");
            stb.append(" WHERE ");
            stb.append("   YEAR = '" + _year + "' ");

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String department_Abbv = rs.getString("DEPARTMENT_ABBV");

                    if("1".equals(flg)) {
                        final String department_S = rs.getString("DEPARTMENT_S");
                        if(department_S != null) {
                            if(!retMap.containsKey(department_S)) {
                                retMap.put(department_S,department_Abbv);
                            }
                        }
                    } else {
                        final String department_H = rs.getString("DEPARTMENT_H");
                        if(department_H != null) {
                            if(!retMap.containsKey(department_H)) {
                                retMap.put(department_H,department_Abbv);
                            }
                        }
                    }
                }
            } catch (SQLException ex) {
                log.debug("getDepartmentInfo exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return retMap;
        }

    }

    private String printScore(final String c) {
        if(c == null) return "";
        if(!c.contains(".")) return c;
        BigDecimal bd = new BigDecimal(c);
        BigDecimal bd1 = bd.setScale(1, BigDecimal.ROUND_HALF_UP);
        return bd1.toPlainString();
    }

}

// eof
