/**     プロジェクト/プロパティ/javaコード・スタイル/コード・テンプレート/パターン       ***/

/*
 *
 * 作成日: 2021/01/25
 * 作成者: ishimine
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

public class KNJD627B {

    private static final Log log = LogFactory.getLog(KNJD627B.class);

    private static final String SEMEALL = "9";
    private static final String SELECT_CLASSCD_UNDER = "90";

    private static final String SDIV010101 = "010101"; //中間
    private static final String SDIV990008 = "990008"; //期末

    private static final String CHUKAN1  = "1-010101"; //1学期中間
    private static final String KIMATSU1 = "1-990008"; //1学期期末
    private static final String CHUKAN2  = "2-010101"; //2学期中間
    private static final String KIMATSU2 = "2-990008"; //2学期期末
    private static final String KIMATSU9 = "9-990008"; //学年末

    private static final String ALL3 = "333333";
    private static final String ALL5 = "555555";
    private static final String ALL9 = "999999";

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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
        final List studentList = getList(db2);
        for (Iterator iterator = studentList.iterator(); iterator.hasNext();) {
            final Student student = (Student) iterator.next();

            if ("1".equals(_param._printKind)) {
                print1Main(db2, svf, student);
            } else {
                print2Main(db2, svf, student);
            }
        }
    }

    private String add(final String num1, final String num2) {
        if (!NumberUtils.isDigits(num1)) { return num2; }
        if (!NumberUtils.isDigits(num2)) { return num1; }
        return String.valueOf(Integer.parseInt(num1) + Integer.parseInt(num2));
    }

    //総合成績個人票印刷
    private void print1Main(final DB2UDB db2, final Vrw32alp svf, final Student student) {
        svf.VrSetForm("KNJD627B_1.frm" , 1);

        //明細部以外を印字
        print1Title(svf, student);

        //不足点
        final int husokuten = -1800;
        int husokuten1 = husokuten;
        int husokuten2 = husokuten;

        //総点
        int totalPoint1 = 0;
        int totalPoint2 = 0;

        //修得単位
        int credit1 = 0;
        int credit2 = 0;

        if (student._year1 != null) {
            final Souten souten1 = (Souten)student._soutenMap.get(student._year1);
            if (souten1 != null) {
                totalPoint1 = Integer.valueOf(souten1._totalPoint);
                husokuten1 += totalPoint1;
                credit1 = Integer.valueOf(souten1._totalCredit);
            }
        }
        printSouten(svf, totalPoint1, "1"); //1年次総点

        if (student._year2 != null) {
            final Souten souten2 = (Souten)student._soutenMap.get(student._year2);
            if (souten2 != null) {
                svf.VrsOut("RANK", souten2._summaryGradeRank + "番"); //1・2年順位
                totalPoint2 = Integer.valueOf(souten2._totalPoint);
                husokuten2 += totalPoint2;
                credit2 = Integer.valueOf(souten2._totalCredit);
            }
        }
        printSouten(svf, totalPoint2, "2"); //2年次総点

        svf.VrsOut("SCORE3", String.valueOf(totalPoint1 + totalPoint2) + "点"); //1・2年総点
        svf.VrsOut("CREDIT", String.valueOf(credit1 + credit2) + "単位"); //修得単位

        final int printHusoku;
        if (husokuten2 >= 0) {
            printHusoku = husokuten1 + husokuten2;
        } else if (husokuten2 < 0 && husokuten1 >= 0){
            printHusoku = husokuten2;
        } else {
            printHusoku = husokuten1 + husokuten2;
        }
        svf.VrsOut("LACK", String.valueOf(printHusoku) + "点"); //不足点

        int retsu = 1;
        int gyo = 1;
        String gradeFlg = "";
        for (Iterator ite = student._kettenMap.keySet().iterator(); ite.hasNext();) {
            final String key = (String)ite.next();
            final String year = key.substring(0,4);

            if (year.equals(student._year1) && gyo == 4) {
                continue; //あふれた項目は印字しない
            }

            if (year.equals(student._year2) && gradeFlg.equals("")) {
                gyo = 4;
                retsu = 1;
                gradeFlg = "1";
            }
            final Map kettenMap = (Map)student._kettenMap.get(key);
            final String subclassabbv = (String)kettenMap.get("SUBCLASSABBV");
            final String credits = (String)kettenMap.get("CREDITS") ;
            final int keta = KNJ_EditEdit.getMS932ByteLength(subclassabbv);
            final String field = keta <= 14 ? "1" : keta <= 20 ? "2" : "3";

            svf.VrsOutn("LOST_SUBCLASS_NAME" + retsu + "_" + field, gyo, subclassabbv);
            svf.VrsOutn("LOST_CREDIT" + retsu, gyo, credits != null ? "(" + credits + ")" : "");

            retsu++;
            if (retsu > 3) {
                retsu = 1;
                gyo++;
            }
        }
        svf.VrEndPage();
        _hasData = true;
    }

    private void printSouten(final Vrw32alp svf, final int point, final String field) {
         String miman = "";
         if (point < 1800) {
             miman = "*";
         }
         svf.VrsOut("SCORE" + field, miman + point + "点"); //各学年総点
    }

    private void print2Main(final DB2UDB db2, final Vrw32alp svf, final Student student) {
        svf.VrSetForm("KNJD627B_2.frm" , 1);

        //明細部以外を印字
        print2Title(svf, student);

        if (student._year1 != null) {
            printMeisai(svf, student, student._printSubclassMap1, "1", student._year1);
        }
        if (student._year2 != null) {
            printMeisai(svf, student, student._printSubclassMap2, "2", student._year2);
        }
        if (student._year3 != null) {
            printMeisai(svf, student, student._printSubclassMap3, "3", student._year3);
        }
        svf.VrEndPage();
        _hasData = true;
    }

    private void printMeisai(final Vrw32alp svf, final Student student, final Map subclassMap, final String gradeField, final String year) {
        int retsu = 1;
        final String semester[] = {"1", "2", "9"};
        final String testcd[] = {SDIV010101,SDIV990008};
        int totalTani = 0; //単位の合計
        //講座毎のループ
        for (Iterator ite = subclassMap.keySet().iterator(); ite.hasNext();) {
            int gyo = 1;
            final String key = (String)ite.next();
            final Map<String, String> printSubclassMap = (Map)subclassMap.get(key);

            svf.VrsOutn("SUBCLASS_NAME" + gradeField + "_1", retsu, printSubclassMap.get("SUBCLASSABBV"));
            svf.VrsOutn("CREDIT" + gradeField, retsu, printSubclassMap.get("CREDITS"));

            String tani = (String)printSubclassMap.get("CREDITS");
            if (tani != null) {
                totalTani += Integer.valueOf(tani);
            }

            final String subclassKey = printSubclassMap.get("SUBCLASSKEY");

            //学期毎のループ
            for (String seme : semester) {
                if (seme.equals("1") || seme.equals("2")) {
                    for (String tcd : testcd) {
                        final String scoreKey = year + "-" + seme + "-" + tcd + "-" + subclassKey;
                        if (student._scoreMap.containsKey(scoreKey)) {
                            final Score score = (Score)student._scoreMap.get(scoreKey);
                            if (!"1".equals(score._passflg)) {
                                svf.VrAttributen("SCORE" + gradeField + "_" + String.valueOf(gyo), retsu, "Palette=9");
                                svf.VrsOutn("SCORE" + gradeField + "_" + String.valueOf(gyo), retsu, score._score);
                                svf.VrAttributen("SCORE" + gradeField + "_" + String.valueOf(gyo), retsu, "Palette=1");
                            } else {
                                svf.VrsOutn("SCORE" + gradeField + "_" + String.valueOf(gyo), retsu, score._score);
                            }
                        }
                        gyo++;
                    }
                } else {
                    final String scoreKey = year + "-" + seme + "-" + SDIV990008 + "-" + subclassKey;
                    if (student._scoreMap.containsKey(scoreKey)) {
                        final Score score = (Score) student._scoreMap.get(scoreKey);
                        if (!"1".equals(score._passflg)) {
                            svf.VrAttributen("SCORE" + gradeField + "_" + String.valueOf(gyo), retsu, "Palette=9");
                            svf.VrsOutn("SCORE" + gradeField + "_" + String.valueOf(gyo), retsu, score._score);
                            svf.VrAttributen("SCORE" + gradeField + "_" + String.valueOf(gyo), retsu, "Palette=1");
                        } else {
                            svf.VrsOutn("SCORE" + gradeField + "_" + String.valueOf(gyo), retsu, score._score);
                        }
                    }
                }
            }
            retsu++;
        }
        svf.VrsOut("TOTAL_CREDIT" + gradeField, String.valueOf(totalTani));

        //総点、平均点、序列
        final String semeTestCd[] = {CHUKAN1,KIMATSU1,CHUKAN2,KIMATSU2,KIMATSU9};
        int gyo = 1;
        for (String cd : semeTestCd) {
            final String scoreKey = year + "-" + cd + "-" + "99-H-99-999999";

            if (student._scoreMap.containsKey(scoreKey)) {
                final Score score = (Score) student._scoreMap.get(scoreKey);
                svf.VrsOutn("TOTAL_SCORE" + gradeField, gyo, score._total_Point);
                svf.VrsOutn("TOTAL_AVE" + gradeField, gyo, sishaGonyu(score._avg));
                svf.VrsOutn("TOTAL_RANK" + gradeField, gyo, score._grade_Rank);
            }
            gyo++;
        }

        //出席の記録
        gyo = 1;
        for (String seme : semester) {
            final Map attendMap = (Map)student._attendMap.get(year + "-" + seme);
            if (attendMap != null) {
                final String lesson = attendMap.get("LESSON").toString();
                if (Integer.valueOf(lesson) > 0) {
                    svf.VrsOutn("LESSON" + gradeField, gyo, lesson);
                    svf.VrsOutn("SUSPEND" + gradeField, gyo, attendMap.get("SUSPEND").toString());
                    svf.VrsOutn("ABSENCE" + gradeField, gyo, attendMap.get("SICK").toString());
                    svf.VrsOutn("PRESENT" + gradeField, gyo, attendMap.get("PRESENT").toString());

                    final Map attendSubclassMap = (Map)student._attendSubclassMap.get(year + "-" + seme);
                    if (attendSubclassMap != null) {
                        svf.VrsOutn("LATE" + gradeField, gyo, attendSubclassMap.get("LATE").toString());
                        svf.VrsOutn("KEKKA" + gradeField, gyo, attendSubclassMap.get("SICK2").toString());
                    } else {
                        svf.VrsOutn("LATE" + gradeField, gyo, "0");
                        svf.VrsOutn("KEKKA" + gradeField, gyo, "0");
                    }
                }
            }
            gyo++;
        }
    }

    private void print1Title(final Vrw32alp svf, final Student student) {
        final String[] date = _param._date.split("-");
        svf.VrsOut("DATE", date[0] + "年 " + Integer.valueOf(date[1]).toString() + "月 " + Integer.valueOf(date[2]).toString() + "日");
        svf.VrsOut("HR_NAME", student._hrclassName3 + " " + Integer.valueOf(student._attendNo3).toString() + "番");
        svf.VrsOut("NAME", student._name);
        svf.VrsOut("SCHOOL_NAME", _param._certifSchoolSchoolName);
        svf.VrsOut("PRINCIPAL_NAME", _param._certifSchoolJobName + " " + _param._certifSchoolPrincipalName);
    }

    private void print2Title(final Vrw32alp svf, final Student student) {
        svf.VrsOut("NAME", student._name);
        svf.VrsOut("BIRTHDAY", student._birthday.replace("-", "/"));
        svf.VrsOut("FINSCHOOL_NAME", student._finSchoolName);
        svf.VrsOut("CLUB_NAME", student._club);
        svf.VrsOut("ENGLISH_SKILL", student._shikaku);

        if (student._year1 != null) {
            printGrade(svf, "1", student._hrclassName1, student._attendNo1, student._staffName1, student._courseName1);
        }
        if (student._year2 != null) {
            printGrade(svf, "2", student._hrclassName2, student._attendNo2, student._staffName2, student._courseName2);
        }
        if (student._year3 != null) {
            printGrade(svf, "3", student._hrclassName3, student._attendNo3, student._staffName3, student._courseName3);
        }
    }

    private void printGrade(final Vrw32alp svf, final String gradeField, final String hrclassName, final String attendNo, final String staffName, final String courseName) {
        svf.VrsOut("HR_NAME" + gradeField, hrclassName + " " + Integer.valueOf(attendNo).toString() + "番");
        final int keta = KNJ_EditEdit.getMS932ByteLength(staffName);
        final String field = keta <= 20 ? "1" : keta <= 30 ? "2" : "3";
        svf.VrsOut("TR_NAME" + gradeField + "_" + field, staffName);
        svf.VrsOut("COURSE_NAME" + gradeField, courseName);
    }

    private List getList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        final String sql = getStudentSql();
        log.debug(" sql =" + sql);
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final Student student = new Student();
                student._schregno = rs.getString("SCHREGNO");
                student._name = rs.getString("NAME");
                student._birthday = rs.getString("BIRTHDAY");
                student._finSchoolName = rs.getString("FINSCHOOL_NAME");
                student.setGradeHrclass(db2);

                //総合成績個人票印刷
                if ("1".equals(_param._printKind)){
                    student.setSouten(db2);
                    student.setKetten(db2);
                }
                //3ヵ年個人成績表印刷
                else {
                    student.setClub(db2);
                    student.setShikaku(db2);

                    final String date = _param._date;
                    final List yearList = new ArrayList(); //対象年度

                    //各学年の出欠、講座データ取得
                    if (student._year1 != null) {
                        student.loadAttendance(db2, _param, student._year1, SEMEALL, date, new HashMap(_param._attendParamMap));
                        student.setSubclass(db2, student._year1, student._grade1, student._courseKey1, "1");
                        yearList.add(student._year1);
                    }
                    if (student._year2 != null) {
                        student.loadAttendance(db2, _param, student._year2, SEMEALL, date, new HashMap(_param._attendParamMap));
                        student.setSubclass(db2, student._year2, student._grade2, student._courseKey2, "2");
                        yearList.add(student._year2);
                    }
                    if (student._year3 != null) {
                        student.loadAttendance(db2, _param, student._year3, SEMEALL, date, new HashMap(_param._attendParamMap));
                        student.setSubclass(db2, student._year3, student._grade3, student._courseKey3, "3");
                        yearList.add(student._year3);
                    }
                    student.setScore(db2, yearList);
                }

                retList.add(student);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String getStudentSql() {
        final StringBuffer stb = new StringBuffer();
        //選択された年組の生徒
        stb.append(" SELECT ");
        stb.append("     T1.SCHREGNO, ");
        stb.append("     T2.NAME, ");
        stb.append("     T2.BIRTHDAY, ");
        stb.append("     SCHOOL.FINSCHOOL_NAME");
        stb.append(" FROM ");
        stb.append("     SCHREG_REGD_DAT T1 ");
        stb.append(" INNER JOIN ");
        stb.append("     SCHREG_BASE_MST T2 ");
        stb.append("      ON T2.SCHREGNO = T1.SCHREGNO ");
        stb.append(" INNER JOIN ");
        stb.append("     FINSCHOOL_MST SCHOOL ");
        stb.append("      ON SCHOOL.FINSCHOOLCD = T2.FINSCHOOLCD ");
        stb.append(" WHERE   T1.YEAR     = '" + _param._loginYear + "' ");
        stb.append("     AND T1.SEMESTER = '" + _param._loginSemester + "' ");
        stb.append("     AND T1.GRADE    = '" + _param._grade + "' ");
        stb.append("     AND T1.HR_CLASS = '" + _param._hrclass + "' ");
        stb.append("     AND T1.SCHREGNO IN " + SQLUtils.whereIn(true, _param._studentsSelected));
        stb.append(" ORDER BY T1.ATTENDNO ");
        final String sql = stb.toString();
        log.debug(" student sql = " + sql);

        return stb.toString();
    }

    private static String sishaGonyu(final String val) {
        if (!NumberUtils.isNumber(val)) {
            return null;
        }
        return new BigDecimal(val).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
    }

    private class Student {
        String _schregno;
        String _name;
        String _year1;
        String _year2;
        String _year3;
        String _grade1;
        String _grade2;
        String _grade3;
        String _gradeCd1;
        String _gradeCd2;
        String _gradeCd3;
        String _hrclass1;
        String _hrclass2;
        String _hrclass3;
        String _hrclassName1;
        String _hrclassName2;
        String _hrclassName3;
        String _attendNo1;
        String _attendNo2;
        String _attendNo3;
        String _courseKey1;
        String _courseKey2;
        String _courseKey3;
        String _courseName1;
        String _courseName2;
        String _courseName3;
        String _staffName1;
        String _staffName2;
        String _staffName3;
        String _birthday;
        String _finSchoolName;
        String _club;
        String _shikaku;
        String _hrClass;
        final Map _attendMap = new TreeMap();
        final Map _attendSubclassMap = new TreeMap();
        final Map _printSubclassMap1 = new TreeMap();
        final Map _printSubclassMap2 = new TreeMap();
        final Map _printSubclassMap3 = new TreeMap();
        final Map _scoreMap = new TreeMap();
        final Map _soutenMap = new TreeMap();
        final Map _kettenMap = new TreeMap();

        private void setGradeHrclass(final DB2UDB db2) {
            final String ghrSql = getGradeHrclassSql();
            log.debug(" ghrSql = " + ghrSql);
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                ps = db2.prepareStatement(ghrSql);
                rs = ps.executeQuery();

                while (rs.next()) {
                    final String year = rs.getString("YEAR");
                    final String grade = rs.getString("GRADE");
                    final String gradeCd = rs.getString("GRADE_CD");
                    final String hrclass = rs.getString("HR_CLASS");
                    final String attendNo = rs.getString("ATTENDNO");
                    final String hrclassName = rs.getString("HR_NAME");
                    final String staffName = rs.getString("STAFFNAME");
                    final String courseCd = rs.getString("COURSECD");
                    final String majorCd = rs.getString("MAJORCD");
                    final String courseCode = rs.getString("COURSECODE");
                    final String courseCodeName = rs.getString("COURSECODENAME");
                    final String courseKey  = courseCd + majorCd + courseCode;

                    if ("01".equals(gradeCd)) {
                        _year1 = year;
                        _grade1 = grade;
                        _gradeCd1 = gradeCd;
                        _hrclass1 = hrclass;
                        _attendNo1 = attendNo;
                        _hrclassName1 = hrclassName;
                        _staffName1 = staffName;
                        _courseKey1 = courseKey;
                        _courseName1 = courseCodeName;
                    } else if ("02".equals(gradeCd)) {
                        _year2 = year;
                        _grade2 = grade;
                        _gradeCd2 = gradeCd;
                        _hrclass2 = hrclass;
                        _attendNo2 = attendNo;
                        _hrclassName2 = hrclassName;
                        _staffName2 = staffName;
                        _courseKey2 = courseKey;
                        _courseName2 = courseCodeName;
                    } else if ("03".equals(gradeCd)) {
                        _year3 = year;
                        _grade3 = grade;
                        _gradeCd3 = gradeCd;
                        _hrclass3 = hrclass;
                        _attendNo3 = attendNo;
                        _hrclassName3 = hrclassName;
                        _staffName3 = staffName;
                        _courseKey3 = courseKey;
                        _courseName3 = courseCodeName;
                    }
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }

        private String getGradeHrclassSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH SCHNO_HIST_A AS( ");
            stb.append("     SELECT DISTINCT ");
            stb.append("         REGD.YEAR, ");
            stb.append("         REGD.SEMESTER, ");
            stb.append("         REGD.SCHREGNO, ");
            stb.append("         REGD.GRADE, ");
            stb.append("         GDAT.GRADE_CD, ");
            stb.append("         REGD.COURSECD, ");
            stb.append("         REGD.MAJORCD, ");
            stb.append("         REGD.COURSECODE, ");
            stb.append("         REGD.HR_CLASS, ");
            stb.append("         REGD.ATTENDNO, ");
            stb.append("         COURSE.COURSECODENAME ");
            stb.append("     FROM ");
            stb.append("         SCHREG_REGD_DAT REGD ");
            stb.append("     INNER JOIN ");
            stb.append("         SCHREG_REGD_GDAT GDAT ");
            stb.append("          ON GDAT.YEAR  = REGD.YEAR ");
            stb.append("         AND GDAT.GRADE = REGD.GRADE ");
            stb.append("     LEFT JOIN ");
            stb.append("         V_COURSECODE_MST COURSE ");
            stb.append("          ON COURSE.YEAR  = REGD.YEAR ");
            stb.append("         AND COURSE.COURSECODE = REGD.COURSECODE ");
            stb.append("     WHERE ");
            stb.append("         REGD.YEAR <= '" + _param._loginYear + "' ");
            stb.append("         AND REGD.SCHREGNO = '" + _schregno + "'     ");
            stb.append(" ) , SCHNO_HIST AS( ");
            stb.append("     SELECT  ");
            stb.append("         T1.* ");
            stb.append("     FROM    SCHNO_HIST_A T1 ");
            stb.append("     WHERE   T1.YEAR || T1.SEMESTER = (SELECT MAX(YEAR) || MAX(SEMESTER) FROM SCHNO_HIST_A T2 WHERE T2.GRADE_CD = T1.GRADE_CD ) ");
            stb.append(" ) ");
            stb.append("     SELECT ");
            stb.append("         T6.YEAR, ");
            stb.append("         T6.GRADE, ");
            stb.append("         T6.GRADE_CD, ");
            stb.append("         T6.HR_CLASS, ");
            stb.append("         T6.ATTENDNO, ");
            stb.append("         T6.COURSECD, ");
            stb.append("         T6.MAJORCD, ");
            stb.append("         T6.COURSECODE, ");
            stb.append("         T6.COURSECODENAME, ");
            stb.append("         REGDH.HR_NAME, ");
            stb.append("         STF.STAFFNAME ");
            stb.append("     FROM ");
            stb.append("         SCHNO_HIST T6 ");
            stb.append("     LEFT JOIN ");
            stb.append("         SCHREG_REGD_HDAT REGDH ");
            stb.append("          ON REGDH.YEAR = T6.YEAR ");
            stb.append("         AND REGDH.SEMESTER = T6.SEMESTER ");
            stb.append("         AND REGDH.GRADE = T6.GRADE ");
            stb.append("         AND REGDH.HR_CLASS = T6.HR_CLASS ");
            stb.append("     LEFT JOIN ");
            stb.append("         STAFF_MST STF ");
            stb.append("          ON STF.STAFFCD = REGDH.TR_CD1 ");
            stb.append("     ORDER BY ");
            stb.append("         T6.GRADE ");

            return stb.toString();
        }

        private void setSouten(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final String sql = getSoutenSql();

            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while(rs.next()) {
                    final String year = rs.getString("YEAR");
                    final String totalCredit = rs.getString("TOTAL_CREDIT");
                    final String totalPoint = rs.getString("TOTAL_POINT");
                    final String summaryCredit = rs.getString("SUMMARY_CREDIT");
                    final String summaryPoint = rs.getString("SUMMARY_POINT");
                    final String summaryGradeRank = rs.getString("SUMMARY_GRADE_RANK");

                    if (!_soutenMap.containsKey(year)) {
                        final Souten wk = new Souten(totalCredit, totalPoint, summaryCredit, summaryPoint, summaryGradeRank);
                        _soutenMap.put(year, wk);
                    }
                }

            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }

        //1,2年総合成績 RECORD_RANK_SDIV_SOUTEN_DAT
        private String getSoutenSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("   YEAR, ");
            stb.append("   TOTAL_CREDIT, ");
            stb.append("   TOTAL_POINT, ");
            stb.append("   SUMMARY_CREDIT, ");
            stb.append("   SUMMARY_POINT, ");
            stb.append("   SUMMARY_GRADE_RANK ");
            stb.append(" FROM ");
            stb.append("   RECORD_RANK_SDIV_SOUTEN_DAT ");
            stb.append(" WHERE  ");
            stb.append("   YEAR = '" + _year1 + "' ");
            stb.append("   AND SEMESTER = '" + SEMEALL + "' ");
            stb.append("   AND TESTKINDCD || TESTITEMCD || SCORE_DIV = '" + SDIV990008 + "' ");
            stb.append("   AND SCHREGNO = '" + _schregno + "'  ");
            stb.append(" UNION ");
            stb.append(" SELECT ");
            stb.append("   YEAR, ");
            stb.append("   TOTAL_CREDIT, ");
            stb.append("   TOTAL_POINT, ");
            stb.append("   SUMMARY_CREDIT, ");
            stb.append("   SUMMARY_POINT, ");
            stb.append("   SUMMARY_GRADE_RANK ");
            stb.append(" FROM ");
            stb.append("   RECORD_RANK_SDIV_SOUTEN_DAT ");
            stb.append(" WHERE  ");
            stb.append("   YEAR = '" + _year2 + "' ");
            stb.append("   AND SEMESTER = '" + SEMEALL + "' ");
            stb.append("   AND TESTKINDCD || TESTITEMCD || SCORE_DIV = '" + SDIV990008 + "' ");
            stb.append("   AND SCHREGNO = '" + _schregno + "'  ");
            stb.append(" ORDER BY ");
            stb.append("   YEAR ");

            final String sql = stb.toString();
            log.debug(" souten sql = " + sql);

            return stb.toString();
        }

        private void setKetten(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final String sql = getKettenSql();

            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while(rs.next()) {
                    final String year = rs.getString("YEAR");
                    final String passflg = rs.getString("PASSFLG");

                    if ("0".equals(passflg)) {
                        final Map ketten = getMappedMap(_kettenMap, year + "-" + rs.getString("SUBCLASSCD"));
                        ketten.put("SUBCLASSABBV", rs.getString("SUBCLASSABBV"));
                        ketten.put("CREDITS", rs.getString("CREDITS"));
                    }
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }

        private String getKettenSql() {

            final StringBuffer stb = new StringBuffer();
            //1年次の欠点
            stb.append(" SELECT ");
            stb.append("     RANK.YEAR, ");
            stb.append("     RANK.SEMESTER, ");
            stb.append("     RANK.TESTKINDCD, ");
            stb.append("     RANK.TESTITEMCD, ");
            stb.append("     RANK.SCORE_DIV, ");
            stb.append("     RANK.CLASSCD, ");
            stb.append("     RANK.SCHOOL_KIND, ");
            stb.append("     RANK.CURRICULUM_CD, ");
            stb.append("     RANK.SUBCLASSCD, ");
            stb.append("     RANK.SCORE, ");
            stb.append("     PERFECT.PASS_SCORE, ");
            stb.append("     CASE WHEN PERFECT.PASS_SCORE <= RANK.SCORE THEN '1' ELSE '0' END AS PASSFLG, ");
            stb.append("     PERFECT.PASS_SCORE, ");
            stb.append("     SUBCLASS.SUBCLASSABBV, ");
            stb.append("     CREDIT.CREDITS ");
            stb.append(" FROM ");
            stb.append("     RECORD_RANK_SDIV_DAT RANK ");
            stb.append(" LEFT JOIN ");
            stb.append("     PERFECT_RECORD_DAT PERFECT ");
            stb.append("      ON PERFECT.YEAR = RANK.YEAR ");
            stb.append("     AND PERFECT.SEMESTER = RANK.SEMESTER ");
            stb.append("     AND PERFECT.TESTKINDCD = RANK.TESTKINDCD ");
            stb.append("     AND PERFECT.TESTITEMCD = RANK.TESTITEMCD ");
            stb.append("     AND PERFECT.CLASSCD = RANK.CLASSCD ");
            stb.append("     AND PERFECT.SCHOOL_KIND = RANK.SCHOOL_KIND ");
            stb.append("     AND PERFECT.CURRICULUM_CD = RANK.CURRICULUM_CD ");
            stb.append("     AND PERFECT.SUBCLASSCD = RANK.SUBCLASSCD ");
            stb.append("     AND PERFECT.DIV = '01' ");
            stb.append(" LEFT JOIN ");
            stb.append("     SUBCLASS_MST SUBCLASS ");
            stb.append("      ON SUBCLASS.CLASSCD = RANK.CLASSCD ");
            stb.append("     AND SUBCLASS.SCHOOL_KIND = RANK.SCHOOL_KIND ");
            stb.append("     AND SUBCLASS.CURRICULUM_CD = RANK.CURRICULUM_CD ");
            stb.append("     AND SUBCLASS.SUBCLASSCD = RANK.SUBCLASSCD ");
            stb.append(" LEFT JOIN ");
            stb.append("     CREDIT_MST CREDIT ");
            stb.append("      ON CREDIT.YEAR = RANK.YEAR ");
            stb.append("     AND CREDIT.COURSECD || CREDIT.MAJORCD || CREDIT.COURSECODE = '" + _courseKey1 + "' ");
            stb.append("     AND CREDIT.GRADE = '" + _grade1 + "' ");
            stb.append("     AND CREDIT.CLASSCD = RANK.CLASSCD ");
            stb.append("     AND CREDIT.SCHOOL_KIND = RANK.SCHOOL_KIND ");
            stb.append("     AND CREDIT.CURRICULUM_CD = RANK.CURRICULUM_CD ");
            stb.append("     AND CREDIT.SUBCLASSCD = RANK.SUBCLASSCD ");
            stb.append(" WHERE ");
            stb.append("     RANK.SCHREGNO = '" + _schregno + "' ");
            stb.append("     AND RANK.YEAR = '" + _year1 + "' ");
            stb.append("     AND RANK.SEMESTER = '" + SEMEALL + "' ");
            stb.append("     AND RANK.CLASSCD <= '" + SELECT_CLASSCD_UNDER + "' ");
            stb.append("     AND RANK.TESTKINDCD || RANK.TESTITEMCD || RANK.SCORE_DIV = '" + SDIV990008 + "' ");
            stb.append("     AND RANK.SUBCLASSCD NOT IN ('" + ALL3 + "','" + ALL5 + "','" + ALL9 + "') ");
            stb.append("     AND RANK.SCHOOL_KIND = 'H' ");
            stb.append(" UNION ");
            //2年次の欠点
            stb.append(" SELECT ");
            stb.append("     RANK.YEAR, ");
            stb.append("     RANK.SEMESTER, ");
            stb.append("     RANK.TESTKINDCD, ");
            stb.append("     RANK.TESTITEMCD, ");
            stb.append("     RANK.SCORE_DIV, ");
            stb.append("     RANK.CLASSCD, ");
            stb.append("     RANK.SCHOOL_KIND, ");
            stb.append("     RANK.CURRICULUM_CD, ");
            stb.append("     RANK.SUBCLASSCD, ");
            stb.append("     RANK.SCORE, ");
            stb.append("     PERFECT.PASS_SCORE, ");
            stb.append("     CASE WHEN PERFECT.PASS_SCORE <= RANK.SCORE THEN '1' ELSE '0' END AS PASSFLG, ");
            stb.append("     PERFECT.PASS_SCORE, ");
            stb.append("     SUBCLASS.SUBCLASSABBV, ");
            stb.append("     CREDIT.CREDITS ");
            stb.append(" FROM ");
            stb.append("     RECORD_RANK_SDIV_DAT RANK ");
            stb.append(" LEFT JOIN ");
            stb.append("     PERFECT_RECORD_DAT PERFECT ");
            stb.append("      ON PERFECT.YEAR = RANK.YEAR ");
            stb.append("     AND PERFECT.SEMESTER = RANK.SEMESTER ");
            stb.append("     AND PERFECT.TESTKINDCD = RANK.TESTKINDCD ");
            stb.append("     AND PERFECT.TESTITEMCD = RANK.TESTITEMCD ");
            stb.append("     AND PERFECT.CLASSCD = RANK.CLASSCD ");
            stb.append("     AND PERFECT.SCHOOL_KIND = RANK.SCHOOL_KIND ");
            stb.append("     AND PERFECT.CURRICULUM_CD = RANK.CURRICULUM_CD ");
            stb.append("     AND PERFECT.SUBCLASSCD = RANK.SUBCLASSCD ");
            stb.append("     AND PERFECT.DIV = '01' ");
            stb.append(" LEFT JOIN ");
            stb.append("     SUBCLASS_MST SUBCLASS ");
            stb.append("      ON SUBCLASS.CLASSCD = RANK.CLASSCD ");
            stb.append("     AND SUBCLASS.SCHOOL_KIND = RANK.SCHOOL_KIND ");
            stb.append("     AND SUBCLASS.CURRICULUM_CD = RANK.CURRICULUM_CD ");
            stb.append("     AND SUBCLASS.SUBCLASSCD = RANK.SUBCLASSCD ");
            stb.append(" LEFT JOIN ");
            stb.append("     CREDIT_MST CREDIT ");
            stb.append("      ON CREDIT.YEAR = RANK.YEAR ");
            stb.append("     AND CREDIT.COURSECD || CREDIT.MAJORCD || CREDIT.COURSECODE = '" + _courseKey2 + "' ");
            stb.append("     AND CREDIT.GRADE = '" + _grade2 + "' ");
            stb.append("     AND CREDIT.CLASSCD = RANK.CLASSCD ");
            stb.append("     AND CREDIT.SCHOOL_KIND = RANK.SCHOOL_KIND ");
            stb.append("     AND CREDIT.CURRICULUM_CD = RANK.CURRICULUM_CD ");
            stb.append("     AND CREDIT.SUBCLASSCD = RANK.SUBCLASSCD ");
            stb.append(" WHERE ");
            stb.append("     RANK.SCHREGNO = '" + _schregno + "' ");
            stb.append("     AND RANK.YEAR = '" + _year2 + "' ");
            stb.append("     AND RANK.SEMESTER = '" + SEMEALL + "' ");
            stb.append("     AND RANK.CLASSCD <= '" + SELECT_CLASSCD_UNDER + "' ");
            stb.append("     AND RANK.TESTKINDCD || RANK.TESTITEMCD || RANK.SCORE_DIV = '" + SDIV990008 + "' ");
            stb.append("     AND RANK.SUBCLASSCD NOT IN ('" + ALL3 + "','" + ALL5 + "','" + ALL9 + "') ");
            stb.append("     AND RANK.SCHOOL_KIND = 'H' ");

            return stb.toString();
        }

        private void setClub(final DB2UDB db2) {

            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     LISTAGG(CLUBNAME, ',') AS CLUBNAME ");
            stb.append(" FROM ");
            stb.append("     SCHREG_CLUB_HIST_DAT HIST ");
            stb.append(" INNER JOIN ");
            stb.append("     CLUB_MST CLUB ");
            stb.append("      ON CLUB.SCHOOLCD = HIST.SCHOOLCD ");
            stb.append("     AND CLUB.SCHOOL_KIND = HIST.SCHOOL_KIND ");
            stb.append("     AND CLUB.CLUBCD = HIST.CLUBCD ");
            stb.append(" WHERE ");
            stb.append("     HIST.SCHREGNO = '" + _schregno + "' ");
            stb.append("     AND ('" + _param._date + "' BETWEEN HIST.SDATE AND HIST.EDATE OR '" + _param._date + "' >= HIST.SDATE AND HIST.EDATE IS NULL) ");
            stb.append("     AND HIST.SCHOOLCD = '" + _param._schoolCd + "' ");
            stb.append(" GROUP BY ");
            stb.append("     HIST.SCHREGNO ");

            _club = KnjDbUtils.getOne(KnjDbUtils.query(db2, stb.toString()));
        }

        private void setShikaku(final DB2UDB db2) {

            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH BASE AS ( ");
            stb.append(" SELECT ");
            stb.append("     HOBBY.SCHREGNO, ");
            stb.append("     T1.QUALIFIED_NAME || T2.NAME1 AS SHIKAKU, ");
            stb.append("     HOBBY.SCORE ");
            stb.append(" FROM ");
            stb.append("     SCHREG_QUALIFIED_HOBBY_DAT HOBBY ");
            stb.append(" LEFT JOIN ");
            stb.append("     QUALIFIED_MST T1 ON T1.QUALIFIED_CD = HOBBY.QUALIFIED_CD ");
            stb.append(" LEFT JOIN ");
            stb.append("     NAME_MST T2 ON T2.NAMECD1 = 'H312' AND T2.NAMECD2 = HOBBY.RANK ");
            stb.append(" WHERE ");
            stb.append("     SCHREGNO = '" + _schregno + "' ");
            stb.append("     AND '" + _param._date + "' >= HOBBY.REGDDATE ");
            stb.append(" ORDER BY SEQ ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("    LISTAGG( ");
            stb.append("        CASE WHEN SCORE IS NOT NULL THEN SHIKAKU || '(' || SCORE || ')' ");
            stb.append("             ELSE SHIKAKU ");
            stb.append("        END, ',') AS SHIKAKU ");
            stb.append(" FROM ");
            stb.append("    BASE ");
            stb.append(" GROUP BY ");
            stb.append("    SCHREGNO ");

            _shikaku = KnjDbUtils.getOne(KnjDbUtils.query(db2, stb.toString()));
        }

        private String setSubclassSql(final String year, final String grade, final String courseKey) {

            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT DISTINCT ");
            stb.append("     STD.YEAR, ");
            stb.append("     STD.CHAIRCD, ");
            stb.append("     CHAIR.CLASSCD || '-' || CHAIR.SCHOOL_KIND || '-' || CHAIR.CURRICULUM_CD || '-' || CHAIR.SUBCLASSCD AS SUBCLASSKEY, ");
            stb.append("     SUBCLASS.SUBCLASSABBV, ");
            stb.append("     CREDIT.CREDITS, ");
            stb.append("     CASE WHEN SAKI.YEAR IS NOT NULL THEN '1' END AS SAKI, ");
            stb.append("     CASE WHEN MOTO.YEAR IS NOT NULL THEN '1' END AS MOTO ");
            stb.append(" FROM ");
            stb.append("     CHAIR_STD_DAT STD ");
            stb.append(" INNER JOIN ");
            stb.append("     CHAIR_DAT CHAIR ");
            stb.append("      ON CHAIR.YEAR = STD.YEAR ");
            stb.append("     AND CHAIR.SEMESTER = STD.SEMESTER ");
            stb.append("     AND CHAIR.CHAIRCD = STD.CHAIRCD ");
            stb.append("     AND CHAIR.CLASSCD <= '" + SELECT_CLASSCD_UNDER + "' ");
            stb.append(" INNER JOIN ");
            stb.append("     SUBCLASS_MST SUBCLASS ");
            stb.append("      ON SUBCLASS.CLASSCD = CHAIR.CLASSCD ");
            stb.append("     AND SUBCLASS.SCHOOL_KIND = CHAIR.SCHOOL_KIND ");
            stb.append("     AND SUBCLASS.CURRICULUM_CD = CHAIR.CURRICULUM_CD ");
            stb.append("     AND SUBCLASS.SUBCLASSCD = CHAIR.SUBCLASSCD ");
            stb.append(" LEFT JOIN ");
            stb.append("     CREDIT_MST CREDIT ");
            stb.append("      ON CREDIT.YEAR = STD.YEAR ");
            stb.append("     AND CREDIT.COURSECD || CREDIT.MAJORCD || CREDIT.COURSECODE = '" + courseKey + "' ");
            stb.append("     AND CREDIT.GRADE = '" + grade + "' ");
            stb.append("     AND CREDIT.CLASSCD = CHAIR.CLASSCD ");
            stb.append("     AND CREDIT.SCHOOL_KIND = CHAIR.SCHOOL_KIND ");
            stb.append("     AND CREDIT.CURRICULUM_CD = CHAIR.CURRICULUM_CD ");
            stb.append("     AND CREDIT.SUBCLASSCD = CHAIR.SUBCLASSCD ");
            stb.append(" LEFT JOIN ");
            stb.append("     SUBCLASS_REPLACE_COMBINED_DAT SAKI ");
            stb.append("      ON SAKI.YEAR = STD.YEAR ");
            stb.append("     AND SAKI.COMBINED_CLASSCD = SUBCLASS.CLASSCD ");
            stb.append("     AND SAKI.COMBINED_SCHOOL_KIND = SUBCLASS.SCHOOL_KIND ");
            stb.append("     AND SAKI.COMBINED_CURRICULUM_CD = SUBCLASS.CURRICULUM_CD ");
            stb.append("     AND SAKI.COMBINED_SUBCLASSCD = SUBCLASS.SUBCLASSCD ");
            stb.append(" LEFT JOIN ");
            stb.append("     SUBCLASS_REPLACE_COMBINED_DAT MOTO ");
            stb.append("      ON MOTO.YEAR = STD.YEAR ");
            stb.append("     AND MOTO.ATTEND_CLASSCD = SUBCLASS.CLASSCD ");
            stb.append("     AND MOTO.ATTEND_SCHOOL_KIND = SUBCLASS.SCHOOL_KIND ");
            stb.append("     AND MOTO.ATTEND_CURRICULUM_CD = SUBCLASS.CURRICULUM_CD ");
            stb.append("     AND MOTO.ATTEND_SUBCLASSCD = SUBCLASS.SUBCLASSCD ");
            stb.append(" WHERE ");
            stb.append("     STD.SCHREGNO = '" + _schregno + "' ");
            stb.append("     AND STD.YEAR = '" + year + "' ");
            stb.append(" ORDER BY ");
            stb.append("     STD.YEAR, SUBCLASSKEY ");

            return stb.toString();
        }

        private void setScore(final DB2UDB db2, final List yearList) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final String sql = setScoreSql(yearList);

            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while(rs.next()) {
                    final String year = rs.getString("YEAR");
                    final String semester = rs.getString("SEMESTER");
                    final String testkindcd = rs.getString("TESTKINDCD");
                    final String testitemcd = rs.getString("TESTITEMCD");
                    final String score_Div = rs.getString("SCORE_DIV");
                    final String classcd = rs.getString("CLASSCD");
                    final String school_Kind = rs.getString("SCHOOL_KIND");
                    final String curriculum_Cd = rs.getString("CURRICULUM_CD");
                    final String subclasscd = rs.getString("SUBCLASSCD");
                    final String score = rs.getString("SCORE");
                    final String passflg = rs.getString("PASSFLG");
                    final String total_Point = rs.getString("TOTAL_POINT");
                    final String grade_Rank = rs.getString("GRADE_RANK");
                    final String avg = rs.getString("AVG");
                    final String key = year + "-" + semester + "-" + testkindcd + testitemcd + score_Div + "-" + classcd + "-" + school_Kind + "-" + curriculum_Cd + "-" + subclasscd;

                    if (!_scoreMap.containsKey(key)) {
                        final Score wk = new Score(score, passflg, total_Point, grade_Rank, avg);
                        _scoreMap.put(key, wk);
                    }
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }

        private String setScoreSql(final List yearList) {

            final String[] year = (String[])yearList.toArray(new String[yearList.size()]);
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     RANK.YEAR, ");
            stb.append("     RANK.SEMESTER, ");
            stb.append("     RANK.TESTKINDCD, ");
            stb.append("     RANK.TESTITEMCD, ");
            stb.append("     RANK.SCORE_DIV, ");
            stb.append("     RANK.CLASSCD, ");
            stb.append("     RANK.SCHOOL_KIND, ");
            stb.append("     RANK.CURRICULUM_CD, ");
            stb.append("     RANK.SUBCLASSCD, ");
            stb.append("     RANK.SCORE, ");
            stb.append("     PERFECT.PASS_SCORE, ");
            stb.append("     CASE WHEN PERFECT.PASS_SCORE <= RANK.SCORE THEN '1' ELSE '0' END AS PASSFLG, ");
            stb.append("     SOUTEN.TOTAL_POINT, ");
            stb.append("     SOUTEN.GRADE_RANK, ");
            stb.append("     RANK.AVG ");
            stb.append(" FROM ");
            stb.append("     RECORD_RANK_SDIV_DAT RANK ");
            stb.append(" LEFT JOIN ");
            stb.append("     PERFECT_RECORD_DAT PERFECT ");
            stb.append("      ON PERFECT.YEAR = RANK.YEAR ");
            stb.append("     AND PERFECT.SEMESTER = RANK.SEMESTER ");
            stb.append("     AND PERFECT.TESTKINDCD = RANK.TESTKINDCD ");
            stb.append("     AND PERFECT.TESTITEMCD = RANK.TESTITEMCD ");
            stb.append("     AND PERFECT.CLASSCD = RANK.CLASSCD ");
            stb.append("     AND PERFECT.SCHOOL_KIND = RANK.SCHOOL_KIND ");
            stb.append("     AND PERFECT.CURRICULUM_CD = RANK.CURRICULUM_CD ");
            stb.append("     AND PERFECT.SUBCLASSCD = RANK.SUBCLASSCD ");
            stb.append("     AND PERFECT.DIV = '01' ");
            stb.append(" LEFT JOIN ");
            stb.append("     RECORD_RANK_SDIV_SOUTEN_DAT SOUTEN ");
            stb.append("      ON SOUTEN.YEAR = RANK.YEAR ");
            stb.append("     AND SOUTEN.SEMESTER = RANK.SEMESTER ");
            stb.append("     AND SOUTEN.TESTKINDCD = RANK.TESTKINDCD ");
            stb.append("     AND SOUTEN.TESTITEMCD = RANK.TESTITEMCD ");
            stb.append("     AND SOUTEN.SCORE_DIV = RANK.SCORE_DIV ");
            stb.append("     AND SOUTEN.CLASSCD = RANK.CLASSCD ");
            stb.append("     AND SOUTEN.SCHOOL_KIND = RANK.SCHOOL_KIND ");
            stb.append("     AND SOUTEN.CURRICULUM_CD = RANK.CURRICULUM_CD ");
            stb.append("     AND SOUTEN.SUBCLASSCD = RANK.SUBCLASSCD ");
            stb.append("     AND SOUTEN.SCHREGNO = RANK.SCHREGNO ");
            stb.append(" WHERE ");
            stb.append("     RANK.SCHREGNO = '" + _schregno + "' ");
            stb.append("     AND RANK.YEAR IN " +  SQLUtils.whereIn(true, year) + " ");
            stb.append("     AND (RANK.CLASSCD <= '" + SELECT_CLASSCD_UNDER + "' OR RANK.SUBCLASSCD = '" + ALL9 + "') ");
            stb.append("     AND RANK.TESTKINDCD || RANK.TESTITEMCD || RANK.SCORE_DIV IN ('" + SDIV010101 + "','" + SDIV990008 + "') ");
            stb.append("     AND RANK.SUBCLASSCD NOT IN ('" + ALL3 + "','" + ALL5 + "') ");
            stb.append("     AND RANK.SCHOOL_KIND = 'H' ");

            return stb.toString();
        }

        private void loadAttendance(
                final DB2UDB db2,
                final Param param,
                final String year,
                final String semester,
                final String date,
                final Map attendParamMap
        ) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final String sql = AttendAccumulate.getAttendSemesSql(year, semester, null, date, attendParamMap);
            log.debug(" attend sql = " + sql);

            try {
                ps = db2.prepareStatement(sql);

                ps.setString(1, _schregno);
                rs = ps.executeQuery();

                while (rs.next()) {
                    final Map semes = getMappedMap(_attendMap, year + "-" + rs.getString("SEMESTER"));

                    semes.put("LESSON", rs.getString("LESSON"));
                    semes.put("MLESSON", rs.getString("MLESSON"));
                    semes.put("SUSPEND", rs.getString("SUSPEND"));
                    semes.put("MOURNING", rs.getString("MOURNING"));
                    semes.put("SICK_ONLY", rs.getString("SICK_ONLY"));
                    semes.put("NOTICE_ONLY", rs.getString("NOTICE_ONLY"));
                    semes.put("SICK_NOTICE", add(rs.getString("SICK_ONLY"), rs.getString("NOTICE_ONLY")));
                    final String putWk = add(add(rs.getString("SICK_ONLY"), rs.getString("NOTICE_ONLY")),
                            rs.getString("NONOTICE_ONLY"));
                    semes.put("SICK", putWk);
                    final String putWk2 = add(rs.getString("SUSPEND"), rs.getString("MOURNING"));
                    semes.put("SUSPEND", putWk2);
                    semes.put("PRESENT", rs.getString("PRESENT"));
                    semes.put("VIRUS", rs.getString("VIRUS"));
                    semes.put("LATE", rs.getString("LATE"));
                    semes.put("EARLY", rs.getString("EARLY"));
                    semes.put("M_KEKKA_JISU", rs.getString("M_KEKKA_JISU"));
                }
                DbUtils.closeQuietly(rs);
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }

            try {
                final String sql2 = AttendAccumulate.getAttendSubclassSql(year, semester, null, date,
                        _param._attendParamMap);
                log.debug(" attend sql = " + sql2);
                ps = db2.prepareStatement(sql2);
                ps.setString(1, _schregno);
                rs = ps.executeQuery();

                while (rs.next()) {
                    final Map semes = getMappedMap(_attendSubclassMap, year + "-" + rs.getString("SEMESTER"));
                    if (semes.containsKey("LATE")) {
                        final String late = (String) semes.get("LATE");
                        final int add = Integer.valueOf(late) + Integer.valueOf(rs.getString("LATE"));
                        semes.put("LATE", String.valueOf(add));
                    } else {
                        semes.put("LATE", rs.getString("LATE"));
                    }

                    if (semes.containsKey("SICK2")) {
                        final String sick = (String) semes.get("SICK2");
                        final int add = Integer.valueOf(sick) + Integer.valueOf(rs.getString("SICK2"));
                        semes.put("SICK2", String.valueOf(add));
                    } else {
                        semes.put("SICK2", rs.getString("SICK2"));
                    }

                }

            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }

        private void setSubclass(final DB2UDB db2, final String year, final String grade, final String courseKey, final String gradeCd) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final String sql = setSubclassSql(year, grade, courseKey);
            log.debug(" attend sql = " + sql);

            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();

                while (rs.next()) {

                    final Map subclass;
                    if ("1".equals(gradeCd)) {
                        subclass = getMappedMap(_printSubclassMap1, year + "-" + rs.getString("SUBCLASSKEY"));
                    } else if ("2".equals(gradeCd)) {
                        subclass = getMappedMap(_printSubclassMap2, year + "-" + rs.getString("SUBCLASSKEY"));
                    } else {
                        subclass = getMappedMap(_printSubclassMap3, year + "-" + rs.getString("SUBCLASSKEY"));
                    }

                    subclass.put("YEAR", rs.getString("YEAR"));
                    subclass.put("CHAIRCD", rs.getString("CHAIRCD"));
                    subclass.put("SUBCLASSKEY", rs.getString("SUBCLASSKEY"));
                    subclass.put("SUBCLASSABBV", rs.getString("SUBCLASSABBV"));
                    subclass.put("CREDITS", rs.getString("CREDITS"));
                    subclass.put("SAKI", rs.getString("SAKI"));
                    subclass.put("MOTO", rs.getString("MOTO"));
                }
                DbUtils.closeQuietly(rs);
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }
    }

    private class Souten {
        final String _totalCredit;
        final String _totalPoint;
        final String _summaryCredit;
        final String _summaryPoint;
        final String _summaryGradeRank;

        public Souten(final String totalCredit, final String totalPoint, final String summaryCredit, final String summaryPoint, final String summaryGradeRank) {
            _totalCredit = totalCredit;
            _totalPoint = totalPoint;
            _summaryCredit = summaryCredit;
            _summaryPoint = summaryPoint;
            _summaryGradeRank = summaryGradeRank;
        }
    }

    private class Score {
        final String _score;
        final String _passflg;
        final String _total_Point;
        final String _grade_Rank;
        final String _avg;

        public Score(final String score, final String passflg, final String total_Point, final String grade_Rank, final String avg) {
            _score = score;
            _passflg = passflg;
            _total_Point = total_Point;
            _grade_Rank = grade_Rank;
            _avg = avg;
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
        final String[] _studentsSelected;
        final String _grade;
        final String _semester;
        final String _hrclass;
        final String _prgid;
        final String _loginSemester;
        final String _loginYear;
        final String _date;
        final String _schoolCd;
        final String _documentRoot;
        final String _printKind;

        private String _certifSchoolSchoolName;
        private String _certifSchoolJobName;
        private String _certifSchoolPrincipalName;
        private boolean _isOutputDebug;

        private final Map _attendParamMap;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {

            _studentsSelected = request.getParameterValues("STUDENTS_SELECTED");
            _grade = request.getParameter("HID_GRADE");
            _hrclass = request.getParameter("HID_HR_CLASS");
            _semester = request.getParameter("HID_SEMESTER");
            _prgid = request.getParameter("PRGID");
            _loginSemester = request.getParameter("HID_SEMESTER");
            _loginYear = request.getParameter("HID_YEAR");
            _date = request.getParameter("LOGIN_DATE").replace('/', '-');
            _printKind = request.getParameter("PRINT_TARGET_KIND");
            _schoolCd = request.getParameter("HID_SCHOOLCD");
            setCertifSchoolDat(db2);
            _documentRoot = request.getParameter("DOCUMENTROOT");

            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
            _attendParamMap.put("schregno", "?");
            _attendParamMap.put("useCurriculumcd", "1");
            _attendParamMap.put("useTestCountflg", "TESTITEM_MST_COUNTFLG_NEW_SDIV");
            _attendParamMap.put("absenceDiv", "1");
        }

        private void setCertifSchoolDat(final DB2UDB db2) {
            final String certifKindcd = "104";
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT SCHOOL_NAME, JOB_NAME, PRINCIPAL_NAME, REMARK2, REMARK4, REMARK5 FROM CERTIF_SCHOOL_DAT ");
            sql.append(" WHERE YEAR = '" + _loginYear + "' AND CERTIF_KINDCD = '"+ certifKindcd +"' ");
            log.debug("certif_school_dat sql = " + sql.toString());

            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql.toString()));

            _certifSchoolSchoolName = StringUtils.defaultString(KnjDbUtils.getString(row, "SCHOOL_NAME"));
            _certifSchoolJobName = StringUtils.defaultString(KnjDbUtils.getString(row, "JOB_NAME"), "校長");
            _certifSchoolPrincipalName = StringUtils.defaultString(KnjDbUtils.getString(row, "PRINCIPAL_NAME"));
        }

    }

    private static Map getMappedMap(final Map map, final String key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new TreeMap());
        }
        return (Map) map.get(key1);
    }
}

// eof
