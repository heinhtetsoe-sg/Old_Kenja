// kanji=漢字
package servletpack.KNJD;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;

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
 */
public class KNJD187V {

    private static final Log log = LogFactory.getLog("KNJD187V.class");

    private boolean _hasData;
    private Param _param;
    private final String CHUUKAN_TESTKINDCD = "01";
    private final String CHUUKAN_TESTITEMCD = "01";
    private final String CHUUKAN_SCORE_DIV  = "08";
    private final String KIMATSU_TESTKINDCD = "99";
    private final String KIMATSU_TESTITEMCD = "00";
    private final String KIMATSU_SCORE_DIV  = "08";
    private final String SUBCLASSCD_ALL3 = "333333";
    private final String SUBCLASSCD_ALL5 = "555555";
    private final String SUBCLASSCD_ALL9 = "999999";

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

            _hasData = printMain(db2, svf);
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

    //得点分布表印刷 中学のみ
    private boolean printMain(final DB2UDB db2, final Vrw32alp svf) throws SQLException {
        // 期末
        if(_param._kimatsu) {
            final Map printMap = getPrintMap(db2, false, null); //コース別の度数分布Map
            if(printMap.size() == 0) {
                return false;
            }

            for(Iterator ite = printMap.keySet().iterator(); ite.hasNext();) {
                final String gradeKey = (String)ite.next();
                final Grade grade = (Grade)printMap.get(gradeKey);
                //全科目平均度数分布Map
                if(grade._avgDosuuBunpuMap.size() == 0) {
                    grade._avgDosuuBunpuMap = getPrintMap(db2, true, grade);
                }
                svf.VrSetForm("KNJD187V.frm", 4);
                printTitle(svf, null);
                final String gradeName = KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT GRADE_NAME2 FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _param._year + "' AND GRADE = '" + gradeKey + "' AND SCHOOL_KIND = 'J'"));
                svf.VrsOut("GRADE_NAME", "中学　" + StringUtils.defaultString(gradeName, ""));
                printAllSubclassAvgNum(svf, grade._avgDosuuBunpuMap); //全科目平均点の人数
                svf.VrsOut("NOTICE2", getKessekiAvg(grade)); //欠席平均
                svf.VrsOut("TOTAL_AVE", grade._avgAvg); //学年平均

                //科目毎のループ
                for(Iterator ite2 = grade._subclassMap.keySet().iterator(); ite2.hasNext();) {
                    final String subclassKey = (String)ite2.next();
                    final SubclassMst subclass = (SubclassMst)grade._subclassMap.get(subclassKey);
                    final int keta = KNJ_EditEdit.getMS932ByteLength(subclass._subclassName);
                    final String field = keta <= 20 ? "1" : keta <= 28 ? "2" : keta <= 32 ? "3" : "4_1";
                    svf.VrsOut("SUBCLASS_NAME" + field, subclass._subclassName);
                    int gyo = 1; //行数
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

                    svf.VrsOut("NOTICE1", subclass._kesseki.equals("0") ? "" : subclass._kesseki); //欠席
                    printGradeAvg(svf, subclass._avg); //学年平均

                    _hasData = true;
                    svf.VrEndRecord();
                }
                svf.VrEndPage();
            }

        } else {
            //中間
            if(_param._studentMap.isEmpty()) {
                log.error("対象生徒なし");
                return false;

            }
            final Map printMap = getPrintMap(db2, false, null); //コース別の度数分布Map
            if(printMap.size() == 0) {
                return false;
            }
            final int border = 54;
            for(Iterator ite = _param._studentMap.keySet().iterator(); ite.hasNext();) {
                final String schregno = (String)ite.next();
                final Student student = (Student)_param._studentMap.get(schregno);
                final Grade grade = (Grade)printMap.get(student._grade);
                //全科目平均度数分布Map
                if(grade._avgDosuuBunpuMap.size() == 0) {
                    grade._avgDosuuBunpuMap = getPrintMap(db2, true, grade);
                }
                svf.VrSetForm("KNJD187V.frm", 4);
                printTitle(svf, student);
                printAllSubclassAvgNum(svf, grade._avgDosuuBunpuMap); //全科目平均点の人数
                svf.VrsOut("NOTICE2", getKessekiAvg(grade)); //欠席平均
                svf.VrsOut("TOTAL_AVE", grade._avgAvg); //学年平均の平均

                //本人成績 平均
                int cnt = 0;
                BigDecimal total = new BigDecimal(0);
                for(Iterator iteScore = grade._subclassMap.keySet().iterator(); iteScore.hasNext();) {
                    final String scoreKey = (String)iteScore.next();
                    final String score = (String)student._scoreMap.get(scoreKey);
                    final BigDecimal scoreBd = score != null ? new BigDecimal(score) : new BigDecimal("0");
                    total = total.add(scoreBd);
                    cnt++;
                }
                if(total.compareTo(new BigDecimal(0)) > 0) {
                    final BigDecimal avg = total.divide(new BigDecimal(cnt), 0, BigDecimal.ROUND_HALF_UP);
                    svf.VrsOut("SCORE2", avg.toString());
                    if (avg.compareTo(new BigDecimal(border)) <= 0) {
                        svf.VrAttribute("SCORE2", "Paint=(9,70,2),Bold=1");
                    }
                }

                //科目毎のループ
                for(Iterator ite2 = grade._subclassMap.keySet().iterator(); ite2.hasNext();) {
                    final String subclassKey = (String)ite2.next();
                    final SubclassMst subclass = (SubclassMst)grade._subclassMap.get(subclassKey);
                    final int keta = KNJ_EditEdit.getMS932ByteLength(subclass._subclassName);
                    final String field = keta <= 20 ? "1" : keta <= 28 ? "2" : keta <= 32 ? "3" : "4_1";
                    svf.VrsOut("SUBCLASS_NAME" + field, subclass._subclassName);
                    int gyo = 1;
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

                    svf.VrsOut("NOTICE1", subclass._kesseki.equals("0") ? "" : subclass._kesseki); //欠席
                    printGradeAvg(svf, subclass._avg); //学年平均


                    //本人成績
                    final String kesseki = (String)student._kessekiMap.get(subclassKey);
                    if("*".equals(kesseki)) {
                        svf.VrsOut("SCORE1", "*");
                    } else {
                        if(student._scoreMap.containsKey(subclassKey)) {
                            final String score = (String)student._scoreMap.get(subclassKey);
                            if(Integer.valueOf(score) <= border) { //54点以下は網掛け
                                svf.VrAttribute("SCORE1", "Paint=(9,70,2),Bold=1");
                                svf.VrsOut("SCORE1", score);
                                svf.VrAttribute("SCORE1", "Paint=(0,0,0),Bold=0");
                            } else {
                                svf.VrsOut("SCORE1", score);
                            }
                        }
                    }

                    _hasData = true;
                    svf.VrEndRecord();
                }
                svf.VrEndPage();
            }
        }
        return true;
    }

    private void printTitle(final Vrw32alp svf, final Student student) {
        if(_param._kimatsu) {
            svf.VrsOut("TITLE", "得点分布表");
            svf.VrsOut("NENDO", _param._year + "年度 " + _param._semesterName2);
        } else {
            svf.VrsOut("TITLE", "得点分布表");
            svf.VrsOut("NENDO", _param._year + "年度 " + _param._semesterName1);
            svf.VrsOut("GRADE_NAME", "中学　" + student._gradeName);
            svf.VrsOut("HR_NAME", student._hrclassName + "組 " + Integer.valueOf(student._attendno).toString() + "番 氏名 " + student._name);
            svf.VrsOut("FOOTER", StringUtils.defaultString(_param._schoolname1) + "　　　担任　 " + StringUtils.defaultString(student._staffname));
        }

    }

    //全科目平均点の人数
    private void printAllSubclassAvgNum(final Vrw32alp svf, final Map printMap) {
        int cnt = 0; //繰り返し回数
        int gyo = 1; //行数

        for(int i = 100; i >= 0; i -= 5) {
            final String num;
            if(printMap.containsKey(i)) {
                num = (String)printMap.get(i);
            } else {
                num = "";
            }

            if(cnt == 0) { //100点の時だけフィールドが違う
                svf.VrsOut("NUM2", num);
                cnt++;
                continue;
            } else {
                String field = cnt % 2 == 1 ? "1" : "2";
                svf.VrsOutn("NUM3_" + field, gyo, num);
            }
            cnt++;
            if(cnt % 2 == 1) {
                gyo++;
            }
        }
    }

    //欠席平均
    private String getKessekiAvg(final Grade grade) {
        BigDecimal total = new BigDecimal(0);
        for(Iterator ite = grade._subclassMap.keySet().iterator(); ite.hasNext();) {
            final String key = (String)ite.next();
            final SubclassMst subclass = (SubclassMst)grade._subclassMap.get(key);
            total = total.add(new BigDecimal(subclass._kesseki));
        }
        final String str = total.divide(new BigDecimal(grade._subclassMap.size()), 0, BigDecimal.ROUND_HALF_UP).toString();
        return str.equals("0") ? "" : str;
    }

    //学年平均
    private void printGradeAvg(final Vrw32alp svf, final String score) {
        if(score != null) {
            BigDecimal avg = new BigDecimal(score);
            svf.VrsOut("AVE1_1", avg.setScale(0, BigDecimal.ROUND_HALF_UP).toString());
        }
    }

    private Map getPrintMap(final DB2UDB db2, final boolean avg, final Grade gradeAvg) throws SQLException {
        Map retMap = new LinkedMap();
        PreparedStatement ps = null;
        ResultSet rs = null;
        Grade gradeClass = null;
        SubclassMst subclass = null;
        Student student = null;
        try{
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH REGD AS ( ");
            stb.append(" SELECT ");
            stb.append("   REGD.YEAR, ");
            stb.append("   REGD.SEMESTER, ");
            stb.append("   REGD.GRADE, ");
            stb.append("   REGD.HR_CLASS, ");
            stb.append("   REGD.ATTENDNO, ");
            stb.append("   BASE.NAME, ");
            stb.append("   REGD.SCHREGNO ");
            stb.append(" FROM ");
            stb.append("   SCHREG_REGD_DAT REGD ");
            stb.append(" INNER JOIN ");
            stb.append("   SCHREG_BASE_MST BASE ");
            stb.append("    ON BASE.SCHREGNO = REGD.SCHREGNO ");
            stb.append(" WHERE ");
            stb.append("   REGD.YEAR         = '" + _param._year + "' ");
            stb.append("   AND REGD.SEMESTER = '" + _param._seme + "' ");
            if(!_param._kimatsu) {
                stb.append("   AND REGD.GRADE = '" + _param._grade + "' ");
            }

            stb.append(" ), DI_T AS ( ");
            stb.append(" SELECT ");
            stb.append("   REGD.YEAR, ");
            stb.append("   REGD.SEMESTER, ");
            stb.append("   REGD.GRADE, ");
            stb.append("   REGD.HR_CLASS, ");
            stb.append("   REGD.ATTENDNO, ");
            stb.append("   REGD.NAME, ");
            stb.append("   REGD.SCHREGNO, ");
            stb.append("   SEISEKI.CLASSCD, ");
            stb.append("   SEISEKI.SCHOOL_KIND, ");
            stb.append("   SEISEKI.CURRICULUM_CD, ");
            stb.append("   SEISEKI.SUBCLASSCD, ");
            stb.append("   NULL AS SCORE, ");
            stb.append("   SEISEKI.VALUE_DI ");
            stb.append(" FROM ");
            stb.append("   REGD ");
            stb.append(" LEFT JOIN ");
            stb.append("   RECORD_SCORE_DAT SEISEKI ");
            stb.append("    ON SEISEKI.YEAR          = REGD.YEAR ");
            if(_param._kimatsu) {
                stb.append("   AND SEISEKI.SEMESTER   = '" + _param._semester2  + "'");
                stb.append("   AND SEISEKI.TESTKINDCD = '" + KIMATSU_TESTKINDCD + "'");
                stb.append("   AND SEISEKI.TESTITEMCD = '" + KIMATSU_TESTITEMCD + "'");
                stb.append("   AND SEISEKI.SCORE_DIV  = '" + KIMATSU_SCORE_DIV  + "'");
            } else {
                stb.append("   AND SEISEKI.SEMESTER   = '" + _param._semester1  + "'");
                stb.append("   AND SEISEKI.TESTKINDCD = '" + CHUUKAN_TESTKINDCD + "'");
                stb.append("   AND SEISEKI.TESTITEMCD = '" + CHUUKAN_TESTITEMCD + "'");
                stb.append("   AND SEISEKI.SCORE_DIV  = '" + CHUUKAN_SCORE_DIV  + "'");
            }
            stb.append("   AND SEISEKI.SCHREGNO      = REGD.SCHREGNO ");
            stb.append("   AND SEISEKI.CLASSCD      <= '90' ");
            stb.append("   AND SEISEKI.VALUE_DI      = '*' ");

            stb.append(" ), SCORE_T AS ( ");
            stb.append(" SELECT ");
            stb.append("   REGD.YEAR, ");
            stb.append("   REGD.SEMESTER, ");
            stb.append("   REGD.GRADE, ");
            stb.append("   REGD.HR_CLASS, ");
            stb.append("   REGD.ATTENDNO, ");
            stb.append("   REGD.NAME, ");
            stb.append("   REGD.SCHREGNO, ");
            stb.append("   RANK.CLASSCD, ");
            stb.append("   RANK.SCHOOL_KIND, ");
            stb.append("   RANK.CURRICULUM_CD, ");
            stb.append("   RANK.SUBCLASSCD, ");
            stb.append("   RANK.SCORE, ");
            stb.append("   NULL AS VALUE_DI ");
            stb.append(" FROM ");
            stb.append("   REGD ");
            stb.append(" LEFT JOIN ");
            stb.append("   RECORD_RANK_SDIV_DAT RANK ");
            stb.append("    ON RANK.YEAR          = REGD.YEAR ");
            if(_param._kimatsu) {
                stb.append("   AND RANK.SEMESTER   = '" + _param._semester2  + "'");
                stb.append("   AND RANK.TESTKINDCD = '" + KIMATSU_TESTKINDCD + "'");
                stb.append("   AND RANK.TESTITEMCD = '" + KIMATSU_TESTITEMCD + "'");
                stb.append("   AND RANK.SCORE_DIV  = '" + KIMATSU_SCORE_DIV  + "'");
            } else {
                stb.append("   AND RANK.SEMESTER   = '" + _param._semester1  + "'");
                stb.append("   AND RANK.TESTKINDCD = '" + CHUUKAN_TESTKINDCD + "'");
                stb.append("   AND RANK.TESTITEMCD = '" + CHUUKAN_TESTITEMCD + "'");
                stb.append("   AND RANK.SCORE_DIV  = '" + CHUUKAN_SCORE_DIV  + "'");
            }
            stb.append("   AND RANK.SCHREGNO      = REGD.SCHREGNO ");
            stb.append("   AND RANK.CLASSCD      <= '90' ");
            stb.append("   AND RANK.SUBCLASSCD NOT IN ('" + SUBCLASSCD_ALL3 + "', '" + SUBCLASSCD_ALL5 + "', '" + SUBCLASSCD_ALL9 + "') ");
            stb.append("   AND RANK.SCORE IS NOT NULL ");
            stb.append(" ), DI_SCORE AS ( ");
            stb.append(" SELECT ");
            stb.append("   YEAR, ");
            stb.append("   SEMESTER, ");
            stb.append("   GRADE, ");
            stb.append("   HR_CLASS, ");
            stb.append("   ATTENDNO, ");
            stb.append("   NAME, ");
            stb.append("   SCHREGNO, ");
            stb.append("   CLASSCD, ");
            stb.append("   SCHOOL_KIND, ");
            stb.append("   CURRICULUM_CD, ");
            stb.append("   SUBCLASSCD, ");
            stb.append("   SCORE, ");
            stb.append("   VALUE_DI ");
            stb.append(" FROM ");
            stb.append("   DI_T ");
            stb.append(" UNION ALL ");
            stb.append(" SELECT ");
            stb.append("   YEAR, ");
            stb.append("   SEMESTER, ");
            stb.append("   GRADE, ");
            stb.append("   HR_CLASS, ");
            stb.append("   ATTENDNO, ");
            stb.append("   NAME, ");
            stb.append("   SCHREGNO, ");
            stb.append("   CLASSCD, ");
            stb.append("   SCHOOL_KIND, ");
            stb.append("   CURRICULUM_CD, ");
            stb.append("   SUBCLASSCD, ");
            stb.append("   SCORE, ");
            stb.append("   VALUE_DI ");
            stb.append(" FROM ");
            stb.append("   SCORE_T ");

            stb.append(" ), MAIN AS ( ");
            stb.append(" SELECT ");
            stb.append("   DI_SCORE.YEAR, ");
            stb.append("   DI_SCORE.SEMESTER, ");
            stb.append("   DI_SCORE.GRADE, ");
            stb.append("   DI_SCORE.HR_CLASS, ");
            stb.append("   DI_SCORE.ATTENDNO, ");
            stb.append("   DI_SCORE.NAME, ");
            stb.append("   DI_SCORE.SCHREGNO, ");
            stb.append("   DI_SCORE.CLASSCD, ");
            stb.append("   DI_SCORE.SCHOOL_KIND, ");
            stb.append("   DI_SCORE.CURRICULUM_CD, ");
            stb.append("   DI_SCORE.SUBCLASSCD, ");
            stb.append("   DI_SCORE.SCORE, ");
            stb.append("   DI_SCORE.VALUE_DI, ");
            stb.append("   SUBCLASS.SUBCLASSNAME ");
            stb.append(" FROM ");
            stb.append("   DI_SCORE ");
            stb.append(" LEFT JOIN ");
            stb.append("   SUBCLASS_MST SUBCLASS ");
            stb.append("    ON SUBCLASS.CLASSCD       = DI_SCORE.CLASSCD ");
            stb.append("   AND SUBCLASS.SCHOOL_KIND   = DI_SCORE.SCHOOL_KIND ");
            stb.append("   AND SUBCLASS.CURRICULUM_CD = DI_SCORE.CURRICULUM_CD ");
            stb.append("   AND SUBCLASS.SUBCLASSCD    = DI_SCORE.SUBCLASSCD ");
            stb.append(" WHERE ");
            stb.append("   NOT EXISTS  ");
            stb.append("   (SELECT 'X' ");
            stb.append("    FROM SUBCLASS_REPLACE_COMBINED_DAT ");
            stb.append("    WHERE YEAR                 = '" + _param._year + "' ");
            stb.append("      AND ATTEND_CLASSCD       = SUBCLASS.CLASSCD ");
            stb.append("      AND ATTEND_SCHOOL_KIND   = SUBCLASS.SCHOOL_KIND ");
            stb.append("      AND ATTEND_CURRICULUM_CD = SUBCLASS.CURRICULUM_CD ");
            stb.append("      AND ATTEND_SUBCLASSCD    = SUBCLASS.SUBCLASSCD) ");
            stb.append(" ORDER BY ");
            stb.append("   DI_SCORE.GRADE, SUBCLASS.SUBCLASSCD, DI_SCORE.SCORE ");
            stb.append(" ) ");

            //学年全科目平均
            if(avg) {
                stb.append(" SELECT ");
                stb.append("   MAIN.SCHREGNO, ");
                stb.append("   FLOAT(SUM(VALUE(MAIN.SCORE, 0))) / " + gradeAvg._subclassMap.size() + " AS AVG ");
                stb.append(" FROM MAIN ");
                stb.append(" WHERE ");
                stb.append("   MAIN.GRADE = '" + gradeAvg._grade + "' AND (MAIN.SCORE IS NOT NULL OR MAIN.VALUE_DI IS NOT NULL) ");
                stb.append(" GROUP BY ");
                stb.append("   MAIN.GRADE, MAIN.SCHREGNO ");
                stb.append(" ORDER BY ");
                stb.append("   AVG");
            } else {
                stb.append(" SELECT * FROM MAIN WHERE SCORE IS NOT NULL OR VALUE_DI IS NOT NULL");
            }

            log.debug(" sql =" + stb.toString());
            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();
            BigDecimal total = new BigDecimal(0);
            int cnt = 1;
            while (rs.next()) {
                if(avg) {
                    final String allAvg = rs.getString("AVG");
                    retMap = setDosuuBunpu(retMap, (int)Double.parseDouble(allAvg), 5);
                    BigDecimal wk = new BigDecimal(allAvg);
                    total = total.add(wk);
                    cnt++;
                } else {
                    final String schregno = rs.getString("SCHREGNO");
                    final String grade = rs.getString("GRADE");
                    final String classcd = rs.getString("CLASSCD");
                    final String school_Kind = rs.getString("SCHOOL_KIND");
                    final String curriculum_Cd = rs.getString("CURRICULUM_CD");
                    final String subclasscd = rs.getString("SUBCLASSCD");
                    final String score = rs.getString("SCORE");
                    final String kesseki = rs.getString("VALUE_DI");
                    final String subclassname = rs.getString("SUBCLASSNAME");

                    final String subclassKey = classcd + "-" + school_Kind + "-" + curriculum_Cd + "-" + subclasscd;

                    if(_param._studentMap != null) {
                        if(_param._studentMap.containsKey(schregno)) {
                            student = (Student)_param._studentMap.get(schregno);
                            if(!student._scoreMap.containsKey(subclassKey)) {
                                if(kesseki == null) {
                                    student._scoreMap.put(subclassKey, score);
                                }
                            }
                            if(!student._kessekiMap.containsKey(subclassKey)) {
                                student._kessekiMap.put(subclassKey, kesseki);
                            }
                        }
                    }

                    if(retMap.containsKey(grade)) {
                        gradeClass = (Grade)retMap.get(grade);
                    } else {
                        gradeClass = new Grade(grade);
                        retMap.put(grade, gradeClass);
                    }

                    if(gradeClass._subclassMap.containsKey(subclassKey)) {
                        subclass = (SubclassMst)gradeClass._subclassMap.get(subclassKey);
                    } else {
                        subclass = new SubclassMst(classcd, school_Kind, curriculum_Cd, subclasscd, subclassname);
                        subclass.getGradeAvg(db2, grade); //学年平均
                        gradeClass._subclassMap.put(subclassKey, subclass);
                    }
                    if("*".equals(kesseki)) {
                        int wk =Integer.parseInt(subclass._kesseki) + 1;
                        subclass._kesseki = String.valueOf(wk);
                    } else {
                        subclass._dosuuBunpuMap = setDosuuBunpu(subclass._dosuuBunpuMap, Integer.parseInt(score), 10);
                    }
                }
            }

            if(avg) {
                gradeAvg._avgAvg = total.divide(new BigDecimal(cnt), 0, BigDecimal.ROUND_HALF_UP).toString();
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

    private class Student {
        final String _schregno;
        final String _grade;
        final String _gradeName;
        final String _hrclassName;
        final String _attendno;
        final String _name;
        final String _staffname;
        final Map _scoreMap;
        final Map _kessekiMap;

        public Student(final String schregno, final String grade, final String gradeName, final String hrclassName, final String attendno, final String name, final String staffname) {
            _schregno = schregno;
            _grade = grade;
            _gradeName = gradeName;
            _hrclassName = hrclassName;
            _attendno = attendno;
            _name = name;
            _staffname = staffname;
            _scoreMap = new LinkedMap();
            _kessekiMap = new LinkedMap();
        }
    }

    private class Grade {
        final String _grade;
        final Map _subclassMap;
        Map _avgDosuuBunpuMap;
        String _avgAvg;

        public Grade(final String grade) {
            _grade = grade;
            _subclassMap = new LinkedMap();
            _avgDosuuBunpuMap = new LinkedMap();
        }
    }

    private Map setDosuuBunpu(final Map retMap, final int score, final int kizami) {
        for(int low = 0; low <= 100; low += kizami) {
            final int high = low + kizami;
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
        Map _dosuuBunpuMap;
        String _avg;
        String _kesseki = "0";

        public SubclassMst(final String classCd, final String schoolKind, final String curriculumCd, final String subclassCd, final String subclassName) {
            _classCd = classCd;
            _schoolKind = schoolKind;
            _curriculumCd = curriculumCd;
            _subclassCd = subclassCd;
            _subclassName = subclassName;
            _dosuuBunpuMap = new LinkedMap();
        }

        public void getGradeAvg(final DB2UDB db2, final String grade) throws SQLException {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     AVG ");
            stb.append(" FROM ");
            stb.append("     RECORD_AVERAGE_SDIV_DAT ");
            stb.append(" WHERE ");
            stb.append("     YEAR = '" + _param._year + "' AND ");
            if(_param._kimatsu) {
                stb.append("     SEMESTER   = '" + _param._semester2  + "' AND ");
                stb.append("     TESTKINDCD = '" + KIMATSU_TESTKINDCD + "' AND ");
                stb.append("     TESTITEMCD = '" + KIMATSU_TESTITEMCD + "' AND ");
                stb.append("     SCORE_DIV  = '" + KIMATSU_SCORE_DIV  + "' AND ");
            } else {
                stb.append("     SEMESTER   = '" + _param._semester1  + "' AND ");
                stb.append("     TESTKINDCD = '" + CHUUKAN_TESTKINDCD + "' AND ");
                stb.append("     TESTITEMCD = '" + CHUUKAN_TESTITEMCD + "' AND ");
                stb.append("     SCORE_DIV  = '" + CHUUKAN_SCORE_DIV  + "' AND ");
            }
            stb.append("     CLASSCD       = '" + _classCd      + "' AND ");
            stb.append("     SCHOOL_KIND   = '" + _schoolKind   + "' AND ");
            stb.append("     CURRICULUM_CD = '" + _curriculumCd + "' AND ");
            stb.append("     SUBCLASSCD    = '" + _subclassCd   + "' AND ");
            stb.append("     AVG_DIV       = '1'                     AND ");
            stb.append("     GRADE         = '" + grade         + "' AND ");
            stb.append("     HR_CLASS      = '000'                   AND ");
            stb.append("     COURSECD      = '0'                     AND ");
            stb.append("     MAJORCD       = '000'                   AND ");
            stb.append("     COURSECODE    = '0000' ");

            _avg = KnjDbUtils.getOne(KnjDbUtils.query(db2, stb.toString()));
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
        final String _year;
        final String _semester1; //中間用　学期
        final String _semester2; //期末用　学期
        final String _loginSemester;
        final String _seme;
        final String _disp; //1:クラス 2:個人
        final String _div; //1:中間 2:期末
        final String _grade;
        final String _hrclass;
        final String _categorySelected[];
        final String _semesterName1;
        final String _semesterName2;
        final String _schoolname1;
        final boolean _kimatsu;
        final Map _studentMap; //出力対象生徒

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("LOGIN_YEAR");
            _semester1 = request.getParameter("SEMESTER1");
            _semester2 = request.getParameter("SEMESTER2");
            _loginSemester = request.getParameter("LOGIN_SEMESTER");
            _disp = request.getParameter("DISP");
            _div = request.getParameter("DIV");
            _grade = request.getParameter("GRADE");
            _hrclass = request.getParameter("HR_CLASS");
            _categorySelected = request.getParameterValues("CLSS_OR_STDNTS_SELECTED");
            _semesterName1 = KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT SEMESTERNAME FROM SEMESTER_MST WHERE YEAR = '" + _year + "' AND SEMESTER = '" + _semester1 + "'"));
            _semesterName2 = KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT SEMESTERNAME FROM SEMESTER_MST WHERE YEAR = '" + _year + "' AND SEMESTER = '" + _semester2 + "'"));
            _schoolname1 = KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT SCHOOLNAME1 FROM SCHOOL_MST WHERE YEAR = '" + _year + "' AND SCHOOL_KIND IN (SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _year + "' AND GRADE = '" + _grade + "' ) "));
            _kimatsu = "1".equals(_disp) && "2".equals(_div);
            if(_kimatsu) {
                _seme = "9".equals(_semester2) ? _loginSemester : _semester2;
                _studentMap = null;
            } else {
                _seme = "9".equals(_semester1) ? _loginSemester : _semester1;
                _studentMap = getStudentMap(db2); //出力対象生徒Map
            }
        }

        public Map getStudentMap(final DB2UDB db2) throws SQLException {
            Map retMap = new LinkedMap();
            PreparedStatement ps = null;
            ResultSet rs = null;

            try{
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("   REGD.SCHREGNO, ");
                stb.append("   BASE.NAME, ");
                stb.append("   REGD.GRADE, ");
                stb.append("   REGDG.GRADE_NAME2, ");
                stb.append("   REGDH.HR_CLASS_NAME1, ");
                stb.append("   REGD.ATTENDNO, ");
                stb.append("   STAFF.STAFFNAME ");
                stb.append(" FROM ");
                stb.append("   SCHREG_REGD_DAT REGD ");
                stb.append(" INNER JOIN ");
                stb.append("   SCHREG_BASE_MST BASE ");
                stb.append("    ON BASE.SCHREGNO = REGD.SCHREGNO ");
                stb.append(" INNER JOIN ");
                stb.append("   SCHREG_REGD_HDAT REGDH ");
                stb.append("    ON REGDH.YEAR     = REGD.YEAR ");
                stb.append("   AND REGDH.SEMESTER = REGD.SEMESTER ");
                stb.append("   AND REGDH.GRADE    = REGD.GRADE ");
                stb.append("   AND REGDH.HR_CLASS = REGD.HR_CLASS ");
                stb.append(" INNER JOIN ");
                stb.append("   SCHREG_REGD_GDAT REGDG ");
                stb.append("    ON REGDG.YEAR        = REGD.YEAR ");
                stb.append("   AND REGDG.GRADE       = REGD.GRADE ");
                stb.append("   AND REGDG.SCHOOL_KIND = 'J' ");
                stb.append(" LEFT JOIN ");
                stb.append("   STAFF_MST STAFF ");
                stb.append("    ON STAFF.STAFFCD = REGDH.TR_CD1 ");
                stb.append(" WHERE ");
                stb.append("   REGD.YEAR         = '" + _year  + "' ");
                stb.append("   AND REGD.SEMESTER = '" + _seme  + "' ");
                stb.append("   AND REGD.GRADE    = '" + _grade + "' ");
                if("1".equals(_disp) && "1".equals(_div)) {
                    stb.append("   AND REGD.HR_CLASS IN" + SQLUtils.whereIn(true, _categorySelected));
                } else if("2".equals(_disp)){
                    stb.append("   AND REGD.SCHREGNO IN" + SQLUtils.whereIn(true, _categorySelected));
                }
                stb.append(" ORDER BY ");
                stb.append("   REGD.GRADE, REGD.HR_CLASS, REGD.ATTENDNO ");

                log.debug(" sql =" + stb.toString());
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String schregno = rs.getString("SCHREGNO");
                    final String name = rs.getString("NAME");
                    final String grade = rs.getString("GRADE");
                    final String gradeName = rs.getString("GRADE_NAME2");
                    final String hrclassName = rs.getString("HR_CLASS_NAME1");
                    final String attendno = rs.getString("ATTENDNO");
                    final String staffname = rs.getString("STAFFNAME");

                    final Student student = new Student(schregno, grade, gradeName, hrclassName, attendno, name, staffname);
                    if (!retMap.containsKey(schregno)) {
                        retMap.put(schregno, student);
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
    }
}

// eof

