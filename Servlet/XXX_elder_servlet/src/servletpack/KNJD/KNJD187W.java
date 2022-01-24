/**     プロジェクト/プロパティ/javaコード・スタイル/コード・テンプレート/パターン       ***/

/*
 * 作成日: 2021/01/28
 * 作成者: matsushima
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
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
import servletpack.KNJD.KNJD181.SubclassAttendance;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KnjDbUtils;

public class KNJD187W {

    private static final Log log = LogFactory.getLog(KNJD187W.class);

    private static final String SEMEALL = "9";
    private static final String SELECT_CLASSCD_UNDER = "89";

    private static final String SDIV9990008 = "9990008"; //学年末評価

    private static final String ALL3 = "333333";
    private static final String ALL5 = "555555";
    private static final String ALL9 = "999999";

    private static final String HYOTEI_TESTCD = "9990009";

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

        //成績不振者一覧表
        printSvfMain(db2, svf, studentList);
        svf.VrEndPage();
    }

    private String add(final String num1, final String num2) {
        if (!NumberUtils.isDigits(num1)) { return num2; }
        if (!NumberUtils.isDigits(num2)) { return num1; }
        return String.valueOf(Integer.parseInt(num1) + Integer.parseInt(num2));
    }

    private void printSvfMain(final DB2UDB db2, final Vrw32alp svf, final List studentList) {

        final List subclassList = subclassListRemoveD026();
        Collections.sort(subclassList);

        //印字する生徒の設定
        final List outStudentList = new ArrayList(); //印字する生徒リスト
        for (Iterator iterator = studentList.iterator(); iterator.hasNext();) {
            final Student student = (Student) iterator.next();

            if (student._printSubclassMap.containsKey(ALL9)) {
                if (student._printSubclassMap.containsKey(ALL9)) {
                    final ScoreData scoreData = (ScoreData) student._printSubclassMap.get(ALL9);
                    final String avgAll9 = scoreData.avg(SDIV9990008);
                    if(new BigDecimal(avgAll9).setScale(0, BigDecimal.ROUND_HALF_UP).doubleValue() < Integer.parseInt(_param._avarageScore)){
                        //全体平均点(小数第1位を四捨五入)が入力値未満の場合、出力対象
                        outStudentList.add(student);
                        continue;
                    }
                }
            }

            for (Iterator itSubclass = subclassList.iterator(); itSubclass.hasNext();) {
                final SubclassMst subclassMst = (SubclassMst) itSubclass.next();
                final String subclassCd = subclassMst._subclasscd;
                if (student._printSubclassMap.containsKey(subclassCd)) {
                    final ScoreData scoreData = (ScoreData) student._printSubclassMap.get(subclassCd);
                    final String score = scoreData.score(SDIV9990008);
                    if(Integer.parseInt(score) < Integer.parseInt(_param._lowerScore)) {
                        //得点が入力基準点未満の場合、出力対象
                        outStudentList.add(student);
                        break;
                    }
                }
            }
        }

        //印字する科目の設定
        final List outSubclassList = new ArrayList(); //印字する科目リスト
        for (Iterator itSubclass = subclassList.iterator(); itSubclass.hasNext();) {

            final SubclassMst subclassMst = (SubclassMst) itSubclass.next();
            final String subclassCd = subclassMst._subclasscd;

            for (Iterator iterator = outStudentList.iterator(); iterator.hasNext();) {
                final Student student = (Student) iterator.next();
                if (student._printSubclassMap.containsKey(subclassCd)) {
                    outSubclassList.add(subclassMst); //対象生徒が1人以上存在する科目
                    break;
                }
            }
        }

        if(outSubclassList.size() > 0 && outStudentList.size() > 0) {

            final String form = "KNJD187W.frm";
            svf.VrSetForm(form , 4);

            //生徒情報・総点・平均点・順位
            int line = 1;
            for (Iterator iterator = outStudentList.iterator(); iterator.hasNext();) {
                final Student student = (Student) iterator.next();

                if(line == 1) printTitle(db2, svf, student); //初回

                //生徒情報
                svf.VrsOutn("GRADE", line, NumberUtils.isDigits(student._grade) ? String.valueOf(Integer.parseInt(student._grade)) : student._grade); //年
                svf.VrsOutn("HR", line, student._hrname); //組
                svf.VrsOutn("ATTEND_NO", line, student._attendno); //番
                svf.VrsOutn("NAME1", line, student._name); //氏名

                if (student._printSubclassMap.containsKey(ALL9)) {
                    final ScoreData scoreData = (ScoreData) student._printSubclassMap.get(ALL9);
                    //総点
                    svf.VrsOutn("TOTAL_SCORE", line, scoreData.score(SDIV9990008));
                    //平均点
                    final String avgAll9 = scoreData.avg(SDIV9990008);
                    svf.VrsOutn("TOTAL_AVERAGE", line, kirisute(avgAll9, 1)); // 小数第2位を切捨

                    if (NumberUtils.isNumber(avgAll9)) {
                        if (new BigDecimal(avgAll9).setScale(0, BigDecimal.ROUND_HALF_UP).doubleValue() < Integer.parseInt(_param._avarageScore)){
                            svf.VrAttributen("TOTAL_AVERAGE", line, "Paint=(9,50,2),Bold=1");
                        }
                    }

                    //順位
                    final Rank gradeRank = (Rank) scoreData._gradeRankMap.get(SDIV9990008);
                    svf.VrsOutn("TOTAL_RANK", line, gradeRank._rank);
                }

                line++;
                _hasData = true;
            }

            //明細
            for (Iterator itSubclass = outSubclassList.iterator(); itSubclass.hasNext();) {
                final SubclassMst subclassMst = (SubclassMst) itSubclass.next();
                final String subclassCd = subclassMst._subclasscd;

                //科目名
                final String subclassname = subclassMst._subclassname;
                final int length = StringUtils.defaultString(subclassname).length();
                if (length > 10) {
                    svf.VrsOut("SUBCLASS_NAME4_1", subclassname.substring(0, 10));
                    svf.VrsOut("SUBCLASS_NAME4_2", subclassname.substring(10));
                } else {
                    final String field = length > 8 ? "2" : length > 5 ? "2" : "1";
                    svf.VrsOut("SUBCLASS_NAME" + field, subclassname);
                }

                //教科担当名
                if(_param._staffNameMap.containsKey(subclassCd)) {
                    final Map map = (Map) _param._staffNameMap.get(subclassCd);
                    final String staffName = (String) map.get("STAFFNAME");
                    final int trLength = StringUtils.defaultString(staffName).length();
                    if (trLength > 10) {
                        svf.VrsOut("TR_NAME4_1", staffName.substring(0, 10));
                        svf.VrsOut("TR_NAME4_2", staffName.substring(10));
                    } else {
                        final String trField = trLength > 8 ? "2" : trLength > 5 ? "2" : "1";
                        svf.VrsOut("TR_NAME" + trField, staffName);
                    }
                }

                //生徒リスト
                line = 1;
                for (Iterator iterator = outStudentList.iterator(); iterator.hasNext();) {
                    final Student student = (Student) iterator.next();

                    //全体平均点の取得
                    String avgAll9 = "0";
                    if (student._printSubclassMap.containsKey(ALL9)) {
                        final ScoreData scoreData = (ScoreData) student._printSubclassMap.get(ALL9);
                        avgAll9 = scoreData.avg(SDIV9990008);
                    }

                    //全体平均点が入力値未満の場合：TRUE    それ以外：FALSE
                    final boolean averageScoreFlg = new BigDecimal(avgAll9).setScale(0, BigDecimal.ROUND_HALF_UP).doubleValue() < Integer.parseInt(_param._avarageScore);

                    if (student._printSubclassMap.containsKey(subclassCd)) {
                        final ScoreData scoreData = (ScoreData) student._printSubclassMap.get(subclassCd);

                        //得点
                        final String score = scoreData.score(SDIV9990008);
                        if(Integer.parseInt(score) < Integer.parseInt(_param._lowerScore)) {
                            //得点が入力基準点未満の場合、出力・網掛け表示
                            svf.VrsOutn("SCORE", line, score);
                            svf.VrAttributen("SCORE", line, "Paint=(9,50,2),Bold=1");
                        } else if(averageScoreFlg){
                            //全体平均点が入力値未満の場合、得点に関わらず出力
                            svf.VrsOutn("SCORE", line, score);
                        }
                        _hasData = true;
                    }
                    line++;
                }

                svf.VrEndRecord();
            }
        }
    }

    private void printTitle(final DB2UDB db2, final Vrw32alp svf, final Student student) {
        //明細部以外を印字
        svf.VrsOut("TITLE", _param._nendo + "　成績不振者一覧表"); //タイトル
        svf.VrsOut("GRADE_NAME", _param._nendo + "　" + student._gradename); //学年
    }

    private List subclassListRemoveD026() {
        final List retList = new ArrayList(_param._subclassMstMap.values());
        for (final Iterator it = retList.iterator(); it.hasNext();) {
            final SubclassMst subclassMst = (SubclassMst) it.next();
            if (_param._d026List.contains(subclassMst._subclasscd)) {
                it.remove();
            }
            if (_param._isNoPrintMoto && subclassMst.isMoto() || !_param._isPrintSakiKamoku &&  subclassMst.isSaki()) {
                it.remove();
            }
        }
        return retList;
    }

    protected void VrsOutnRenban(final Vrw32alp svf, final String field, final String[] value) {
        if (null != value) {
            for (int i = 0 ; i < value.length; i++) {
                svf.VrsOutn(field, i + 1, value[i]);
            }
        }
    }

    private List getList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getStudentSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final Student student = new Student();
                student._schregno = rs.getString("SCHREGNO");
                student._name = rs.getString("NAME");
                student._schoolKind = rs.getString("SCHOOL_KIND");
                student._gradename = rs.getString("GRADE_NAME2");
                student._hrname = rs.getString("HR_CLASS_NAME1") + "組" ;
                student._trcd = StringUtils.defaultString(rs.getString("TR_CD1"));
                student._staffname = StringUtils.defaultString(rs.getString("STAFFNAME"));
                student._staffname2 = StringUtils.defaultString(rs.getString("STAFFNAME2"));
                student._attendno = NumberUtils.isDigits(rs.getString("ATTENDNO")) ? String.valueOf(Integer.parseInt(rs.getString("ATTENDNO"))) : rs.getString("ATTENDNO");
                student._grade = rs.getString("GRADE");
                student._hrClass = rs.getString("HR_CLASS");
                student._coursecd = rs.getString("COURSECD");
                student._majorcd = rs.getString("MAJORCD");
                student._coursecode = rs.getString("COURSECODE");
                student._course = rs.getString("COURSE");
                student._majorname = rs.getString("MAJORNAME");
                student._coursename = rs.getString("COURSECODENAME");
                student._hrClassName1 = rs.getString("HR_CLASS_NAME1");
                student._entyear = rs.getString("ENT_YEAR");
                student._guard_zipcd = StringUtils.defaultString(rs.getString("GUARD_ZIPCD"));
                student._guard_addr1 = StringUtils.defaultString(rs.getString("GUARD_ADDR1"));
                student._guard_addr2 = StringUtils.defaultString(rs.getString("GUARD_ADDR2"));
                student._guard_name = StringUtils.defaultString(rs.getString("GUARD_NAME"));

                student.setSubclass(db2);
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
        stb.append("WITH SCHNO_A AS(");
        stb.append("    SELECT  T1.YEAR, T1.SEMESTER, T1.GRADE, T1.HR_CLASS, T1.ATTENDNO, T1.SCHREGNO, T1.COURSECD, T1.MAJORCD, T1.COURSECODE ");
        stb.append("    FROM    SCHREG_REGD_DAT T1,SEMESTER_MST T2 ");
        stb.append("    WHERE   T1.YEAR = '" + _param._loginYear + "' ");
        if (SEMEALL.equals(_param._semester)) {
            stb.append("     AND T1.SEMESTER = '" + _param._loginSemester + "' ");
        } else {
            stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
        }
        stb.append("        AND T1.YEAR = T2.YEAR ");
        stb.append("        AND T1.SEMESTER = T2.SEMESTER ");
        stb.append("        AND T1.GRADE = '" + _param._grade + "' ");
        stb.append("        AND T1.HR_CLASS IN " + SQLUtils.whereIn(true, _param._categorySelected));
        stb.append("    ) ");
        //メイン表
        stb.append("     SELECT  REGD.SCHREGNO");
        stb.append("            ,REGD.SEMESTER ");
        stb.append("            ,BASE.NAME ");
        stb.append("            ,REGDG.SCHOOL_KIND ");
        stb.append("            ,REGDG.GRADE_NAME2 ");
        stb.append("            ,REGDH.HR_NAME ");
        stb.append("            ,REGDH.HR_CLASS_NAME1 ");
        stb.append("            ,REGDH.TR_CD1 ");
        stb.append("            ,STF1.STAFFNAME ");
        stb.append("            ,STF2.STAFFNAME AS STAFFNAME2 ");
        stb.append("            ,REGD.ATTENDNO ");
        stb.append("            ,REGDG.GRADE ");
        stb.append("            ,REGDG.GRADE_CD ");
        stb.append("            ,REGD.HR_CLASS ");
        stb.append("            ,REGD.COURSECD ");
        stb.append("            ,REGD.MAJORCD ");
        stb.append("            ,REGD.COURSECODE ");
        stb.append("            ,REGD.COURSECD || REGD.MAJORCD || REGD.COURSECODE AS COURSE");
        stb.append("            ,MAJOR.MAJORNAME ");
        stb.append("            ,COURSE.COURSECODENAME ");
        stb.append("            ,FISCALYEAR(BASE.ENT_DATE) AS ENT_YEAR ");
        stb.append("            ,GADDR.GUARD_ZIPCD ");
        stb.append("            ,GADDR.GUARD_ADDR1 ");
        stb.append("            ,GADDR.GUARD_ADDR2 ");
        stb.append("            ,GUARDIAN.GUARD_NAME ");
        stb.append("     FROM    SCHNO_A REGD ");
        stb.append("     LEFT JOIN SCHREG_REGD_GDAT REGDG ON REGDG.YEAR = REGD.YEAR ");
        stb.append("                  AND REGDG.GRADE = REGD.GRADE ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT REGDH ON REGDH.YEAR = REGD.YEAR ");
        stb.append("                  AND REGDH.SEMESTER = REGD.SEMESTER ");
        stb.append("                  AND REGDH.GRADE = REGD.GRADE ");
        stb.append("                  AND REGDH.HR_CLASS = REGD.HR_CLASS ");
        stb.append("     LEFT JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ");
        stb.append("     LEFT JOIN STAFF_MST STF1 ON STF1.STAFFCD = REGDH.TR_CD1 ");
        stb.append("     LEFT JOIN MAJOR_MST MAJOR ON MAJOR.COURSECD = REGD.COURSECD ");
        stb.append("                  AND MAJOR.MAJORCD = REGD.MAJORCD ");
        stb.append("     LEFT JOIN COURSECODE_MST COURSE ");
        stb.append("            ON COURSE.COURSECODE = REGD.COURSECODE ");
        stb.append("     LEFT JOIN STAFF_MST STF2 ON STF2.STAFFCD = REGDH.TR_CD2 ");
        stb.append("     LEFT JOIN GUARDIAN_DAT GUARDIAN ");
        stb.append("            ON GUARDIAN.SCHREGNO = REGD.SCHREGNO  ");
        stb.append("     LEFT JOIN (SELECT SCHREGNO, MAX(ISSUEDATE) AS ISSUEDATE FROM GUARDIAN_ADDRESS_DAT GROUP BY SCHREGNO) L_GADDR ");
        stb.append("            ON L_GADDR.SCHREGNO = GUARDIAN.SCHREGNO  ");
        stb.append("     LEFT JOIN GUARDIAN_ADDRESS_DAT GADDR ON GADDR.SCHREGNO = L_GADDR.SCHREGNO AND GADDR.ISSUEDATE = L_GADDR.ISSUEDATE ");
        stb.append("     LEFT JOIN RECORD_RANK_SDIV_DAT RANK_SDIV ");
        stb.append("            ON RANK_SDIV.YEAR       = REGD.YEAR ");
        stb.append("           AND RANK_SDIV.SEMESTER || RANK_SDIV.TESTKINDCD || RANK_SDIV.TESTITEMCD || RANK_SDIV.SCORE_DIV = '"+ SDIV9990008 +"' ");
        stb.append("           AND RANK_SDIV.SUBCLASSCD = '"+ ALL9 +"' ");
        stb.append("           AND RANK_SDIV.SCHREGNO   = REGD.SCHREGNO ");
        stb.append("     WHERE   REGD.YEAR = '" + _param._loginYear + "' ");
        if (SEMEALL.equals(_param._semester)) {
            stb.append("     AND REGD.SEMESTER = '" + _param._loginSemester + "' ");
        } else {
            stb.append("     AND REGD.SEMESTER = '" + _param._semester + "' ");
        }
        stb.append("         AND REGD.GRADE = '" + _param._grade + "' ");
        stb.append("         AND REGD.HR_CLASS IN " + SQLUtils.whereIn(true, _param._categorySelected));
        stb.append("     ORDER BY ");
        stb.append("         REGD.GRADE, ");
        stb.append("         RANK_SDIV.GRADE_RANK, ");
        stb.append("         REGD.HR_CLASS, ");
        stb.append("         REGD.ATTENDNO ");
        final String sql = stb.toString();
        log.debug(" student sql = " + sql);

        return stb.toString();
    }

    private static String kirisute(final String val, final int keta) {
        if (!NumberUtils.isNumber(val)) {
            return null;
        }
        return new BigDecimal(val).setScale(keta, BigDecimal.ROUND_DOWN).toString();
    }

    private class Student {
        String _schregno;
        String _name;
        String _schoolKind;
        String _gradename;
        String _hrname;
        String _trcd;
        String _staffname;
        String _staffname2;
        String _attendno;
        String _grade;
        String _hrClass;
        String _coursecd;
        String _majorcd;
        String _coursecode;
        String _course;
        String _majorname;
        String _coursename;
        String _hrClassName1;
        String _entyear;
        String _guard_zipcd;
        String _guard_addr1;
        String _guard_addr2;
        String _guard_name;
        final Map _attendMap = new TreeMap();
        final Map _printSubclassMap = new TreeMap();
        final Map<String, Map<String, SubclassAttendance>> _attendSubClassMap = new HashMap();

        private void setSubclass(final DB2UDB db2) {
            final String scoreSql = prestatementSubclass();
            log.fatal(" scoreSql = " + scoreSql);
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(scoreSql);
                rs = ps.executeQuery();
                while (rs.next()) {

                    final String classcd = rs.getString("CLASSCD");
                    final String classname = rs.getString("CLASSNAME");
                    final String subclassname = rs.getString("SUBCLASSNAME");
                    final String credits = rs.getString("CREDITS");

                    String subclasscd = rs.getString("SUBCLASSCD");
                    if (!"999999".equals(subclasscd)) {
                        subclasscd = rs.getString("CLASSCD") + "-" + rs.getString("SCHOOL_KIND") + "-" + rs.getString("CURRICULUM_CD") + "-" + rs.getString("SUBCLASSCD");
                    }

                    final Rank gradeRank = new Rank(rs.getString("GRADE_RANK"), rs.getString("GRADE_AVG_RANK"), rs.getString("GRADE_COUNT"), rs.getString("GRADE_AVG"), rs.getString("GRADE_HIGHSCORE"));
                    final Rank hrRank = new Rank(rs.getString("CLASS_RANK"), rs.getString("CLASS_AVG_RANK"), rs.getString("HR_COUNT"), rs.getString("HR_AVG"), rs.getString("HR_HIGHSCORE"));
                    final Rank courseRank = new Rank(rs.getString("COURSE_RANK"), rs.getString("COURSE_AVG_RANK"), rs.getString("COURSE_COUNT"), rs.getString("COURSE_AVG"), rs.getString("COURSE_HIGHSCORE"));
                    final Rank majorRank = new Rank(rs.getString("MAJOR_RANK"), rs.getString("MAJOR_AVG_RANK"), rs.getString("MAJOR_COUNT"), rs.getString("MAJOR_AVG"), rs.getString("MAJOR_HIGHSCORE"));

                    final String key = subclasscd;
                    if (!_printSubclassMap.containsKey(key)) {
                        _printSubclassMap.put(key, new ScoreData(classcd, classname, subclasscd, subclassname, credits));
                    }
                    if (null == rs.getString("SEMESTER")) {
                        continue;
                    }

                    final ScoreData scoreData = (ScoreData) _printSubclassMap.get(key);
                    final String testcd = rs.getString("SEMESTER") + rs.getString("TESTKINDCD") + rs.getString("TESTITEMCD") + rs.getString("SCORE_DIV");
                    scoreData._scoreMap.put(testcd, StringUtils.defaultString(rs.getString("SCORE"), "0"));
                    scoreData._avgMap.put(testcd, StringUtils.defaultString(rs.getString("AVG"), "0.0"));
                    scoreData._gradeRankMap.put(testcd, gradeRank);
                    scoreData._hrRankMap.put(testcd, hrRank);
                    scoreData._courceRankMap.put(testcd, courseRank);
                    scoreData._majorRankMap.put(testcd, majorRank);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }

        private String prestatementSubclass() {
            final StringBuffer stb = new StringBuffer();

            final String[] sdivs = {SDIV9990008};
            final StringBuffer divStr = divStr("", sdivs);

            stb.append(" WITH SCHNO AS( ");
            //学籍の表
            stb.append(" SELECT ");
            stb.append("     T2.YEAR, ");
            stb.append("     T2.SEMESTER, ");
            stb.append("     T2.SCHREGNO, ");
            stb.append("     T2.GRADE, ");
            stb.append("     T2.HR_CLASS, ");
            stb.append("     T2.ATTENDNO, ");
            stb.append("     T2.COURSECD, ");
            stb.append("     T2.MAJORCD, ");
            stb.append("     T2.COURSECODE ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT T2 ");
            stb.append(" WHERE ");
            stb.append("         T2.YEAR    = '" + _param._loginYear + "'  ");
            stb.append("     AND T2.GRADE    = '" + _grade + "'  ");
            stb.append("     AND T2.HR_CLASS = '" + _hrClass + "'  ");
            stb.append("     AND T2.SCHREGNO = '" + _schregno + "'  ");
            stb.append("     AND T2.SEMESTER = (SELECT ");
            stb.append("                          MAX(SEMESTER) ");
            stb.append("                        FROM ");
            stb.append("                          SCHREG_REGD_DAT W2 ");
            stb.append("                        WHERE ");
            stb.append("                          W2.YEAR = '" + _param._loginYear + "'  ");
            stb.append("                          AND W2.SEMESTER <= '" + _param._semester + "'  ");
            stb.append("                          AND W2.SCHREGNO = T2.SCHREGNO ");
            stb.append("                     ) ");
            //講座の表
            stb.append(" ) , CHAIR_A AS( ");
            stb.append(" SELECT ");
            stb.append("     S1.SCHREGNO, ");
            stb.append("     S2.CLASSCD, ");
            stb.append("     S2.SCHOOL_KIND, ");
            stb.append("     S2.CURRICULUM_CD, ");
            stb.append("     S2.SUBCLASSCD ");
            stb.append(" FROM ");
            stb.append("     CHAIR_STD_DAT S1, ");
            stb.append("     CHAIR_DAT S2 ");
            stb.append(" WHERE ");
            stb.append("     S1.YEAR         = '" + _param._loginYear + "' ");
            stb.append("     AND S1.SEMESTER <= '" + _param._semester + "' ");
            stb.append("     AND S2.YEAR     = S1.YEAR          ");
            stb.append("     AND S2.SEMESTER <= '" + _param._semester + "' ");
            stb.append("     AND S2.SEMESTER = S1.SEMESTER          ");
            stb.append("     AND S2.CHAIRCD  = S1.CHAIRCD          ");
            stb.append("     AND EXISTS(SELECT ");
            stb.append("                  'X' ");
            stb.append("                FROM ");
            stb.append("                  SCHNO S3 ");
            stb.append("                WHERE ");
            stb.append("                  S3.SCHREGNO = S1.SCHREGNO ");
            stb.append("                GROUP BY ");
            stb.append("                  SCHREGNO ");
            stb.append("             )          ");
            stb.append("     AND SUBCLASSCD <= '" + SELECT_CLASSCD_UNDER + "' ");
            stb.append(" GROUP BY ");
            stb.append("     S1.SCHREGNO, ");
            stb.append("     S2.CLASSCD, ");
            stb.append("     S2.SCHOOL_KIND, ");
            stb.append("     S2.CURRICULUM_CD, ");
            stb.append("     S2.SUBCLASSCD ");
            //成績明細データの表
            stb.append(" ) ,RECORD00 AS( ");
            stb.append("     SELECT DISTINCT ");
            stb.append("         T1.YEAR, T1.SEMESTER, T1.TESTKINDCD, T1.TESTITEMCD, T1.SCORE_DIV, T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, T1.SCHREGNO ");
            stb.append("     FROM RECORD_RANK_SDIV_DAT T1 ");
            stb.append("     INNER JOIN (SELECT ");
            stb.append("                  SCHREGNO ");
            stb.append("                FROM ");
            stb.append("                  SCHNO T2 ");
            stb.append("                GROUP BY ");
            stb.append("                  SCHREGNO ");
            stb.append("             ) T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("     WHERE ");
            stb.append("         T1.YEAR = '" + _param._loginYear + "'  ");
            stb.append("         AND (" + divStr + ") ");
            stb.append("         AND T1.SUBCLASSCD NOT IN ('" + ALL3 + "', '" + ALL5 + "') ");
            stb.append(" ) ,RECORD0 AS( ");
            stb.append("     SELECT ");
            stb.append("              T1.YEAR, T1.SEMESTER, T1.TESTKINDCD, T1.TESTITEMCD, T1.SCORE_DIV, T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, T1.SCHREGNO ");
            stb.append("            , L1.CREDITS ");
            stb.append("            , L2.SCORE ");
            stb.append("            , L2.AVG ");
            stb.append("            , L2.GRADE_RANK ");
            stb.append("            , L2.GRADE_AVG_RANK ");
            stb.append("            , L2.CLASS_RANK ");
            stb.append("            , L2.CLASS_AVG_RANK ");
            stb.append("            , L2.COURSE_RANK ");
            stb.append("            , L2.COURSE_AVG_RANK ");
            stb.append("            , L2.MAJOR_RANK ");
            stb.append("            , L2.MAJOR_AVG_RANK ");
            stb.append("            , T_AVG1.AVG AS GRADE_AVG ");
            stb.append("            , T_AVG1.COUNT AS GRADE_COUNT ");
            stb.append("            , T_AVG1.HIGHSCORE AS GRADE_HIGHSCORE ");
            stb.append("            , T_AVG2.AVG AS HR_AVG ");
            stb.append("            , T_AVG2.COUNT AS HR_COUNT ");
            stb.append("            , T_AVG2.HIGHSCORE AS HR_HIGHSCORE ");
            stb.append("            , T_AVG3.AVG AS COURSE_AVG ");
            stb.append("            , T_AVG3.COUNT AS COURSE_COUNT ");
            stb.append("            , T_AVG3.HIGHSCORE AS COURSE_HIGHSCORE ");
            stb.append("            , T_AVG4.AVG AS MAJOR_AVG ");
            stb.append("            , T_AVG4.COUNT AS MAJOR_COUNT ");
            stb.append("            , T_AVG4.HIGHSCORE AS MAJOR_HIGHSCORE ");
            stb.append("     FROM RECORD00 T1 ");
            stb.append("     INNER JOIN SCHNO T2 ");
            stb.append("             ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("     LEFT JOIN CREDIT_MST L1 ");
            stb.append("            ON L1.YEAR          = T2.YEAR ");
            stb.append("           AND L1.COURSECD      = T2.COURSECD ");
            stb.append("           AND L1.MAJORCD       = T2.MAJORCD ");
            stb.append("           AND L1.COURSECODE    = T2.COURSECODE ");
            stb.append("           AND L1.GRADE         = T2.GRADE ");
            stb.append("           AND L1.CLASSCD       = T1.CLASSCD ");
            stb.append("           AND L1.SCHOOL_KIND   = T1.SCHOOL_KIND ");
            stb.append("           AND L1.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("           AND L1.SUBCLASSCD    = T1.SUBCLASSCD ");
            stb.append("     LEFT JOIN RECORD_RANK_SDIV_DAT L2 ");
            stb.append("            ON L2.YEAR          = T1.YEAR ");
            stb.append("           AND L2.SEMESTER      = T1.SEMESTER ");
            stb.append("           AND L2.TESTKINDCD    = T1.TESTKINDCD ");
            stb.append("           AND L2.TESTITEMCD    = T1.TESTITEMCD ");
            stb.append("           AND L2.SCORE_DIV     = T1.SCORE_DIV ");
            stb.append("           AND L2.CLASSCD       = T1.CLASSCD ");
            stb.append("           AND L2.SCHOOL_KIND   = T1.SCHOOL_KIND ");
            stb.append("           AND L2.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("           AND L2.SUBCLASSCD    = T1.SUBCLASSCD ");
            stb.append("           AND L2.SCHREGNO      = T1.SCHREGNO ");
            stb.append("     LEFT JOIN RECORD_AVERAGE_SDIV_DAT T_AVG1 ON T_AVG1.YEAR = T1.YEAR AND T_AVG1.SEMESTER = T1.SEMESTER AND T_AVG1.TESTKINDCD = T1.TESTKINDCD AND T_AVG1.TESTITEMCD = T1.TESTITEMCD AND T_AVG1.SCORE_DIV = T1.SCORE_DIV AND T_AVG1.GRADE = T2.GRADE AND T_AVG1.CLASSCD = T1.CLASSCD AND T_AVG1.SCHOOL_KIND = T1.SCHOOL_KIND AND T_AVG1.CURRICULUM_CD = T1.CURRICULUM_CD AND T_AVG1.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("           AND T_AVG1.AVG_DIV    = '1' "); //学年
            stb.append("     LEFT JOIN RECORD_AVERAGE_SDIV_DAT T_AVG2 ON T_AVG2.YEAR = T1.YEAR AND T_AVG2.SEMESTER = T1.SEMESTER AND T_AVG2.TESTKINDCD = T1.TESTKINDCD AND T_AVG2.TESTITEMCD = T1.TESTITEMCD AND T_AVG2.SCORE_DIV = T1.SCORE_DIV AND T_AVG2.GRADE = T2.GRADE AND T_AVG2.CLASSCD = T1.CLASSCD AND T_AVG2.SCHOOL_KIND = T1.SCHOOL_KIND AND T_AVG2.CURRICULUM_CD = T1.CURRICULUM_CD AND T_AVG2.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("           AND T_AVG2.AVG_DIV    = '2' "); //クラス
            stb.append("           AND T_AVG2.HR_CLASS   = T2.HR_CLASS ");
            stb.append("     LEFT JOIN RECORD_AVERAGE_SDIV_DAT T_AVG3 ON T_AVG3.YEAR = T1.YEAR AND T_AVG3.SEMESTER = T1.SEMESTER AND T_AVG3.TESTKINDCD = T1.TESTKINDCD AND T_AVG3.TESTITEMCD = T1.TESTITEMCD AND T_AVG3.SCORE_DIV = T1.SCORE_DIV AND T_AVG3.GRADE = T2.GRADE AND T_AVG3.CLASSCD = T1.CLASSCD AND T_AVG3.SCHOOL_KIND = T1.SCHOOL_KIND AND T_AVG3.CURRICULUM_CD = T1.CURRICULUM_CD AND T_AVG3.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("           AND T_AVG3.AVG_DIV    = '3' ");
            stb.append("           AND T_AVG3.COURSECD   = T2.COURSECD ");
            stb.append("           AND T_AVG3.MAJORCD    = T2.MAJORCD ");
            stb.append("           AND T_AVG3.COURSECODE = T2.COURSECODE "); //コース
            stb.append("     LEFT JOIN RECORD_AVERAGE_SDIV_DAT T_AVG4 ON T_AVG4.YEAR = T1.YEAR AND T_AVG4.SEMESTER = T1.SEMESTER AND T_AVG4.TESTKINDCD = T1.TESTKINDCD AND T_AVG4.TESTITEMCD = T1.TESTITEMCD AND T_AVG4.SCORE_DIV = T1.SCORE_DIV AND T_AVG4.GRADE = T2.GRADE AND T_AVG4.CLASSCD = T1.CLASSCD AND T_AVG4.SCHOOL_KIND = T1.SCHOOL_KIND AND T_AVG4.CURRICULUM_CD = T1.CURRICULUM_CD AND T_AVG4.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("           AND T_AVG4.AVG_DIV    = '4' ");
            stb.append("           AND T_AVG4.COURSECD   = T2.COURSECD ");
            stb.append("           AND T_AVG4.MAJORCD    = T2.MAJORCD ");
            stb.append("           AND T_AVG4.COURSECODE = '0000' "); //専攻
            stb.append(" ) ,RECORD AS( ");
            stb.append("     SELECT ");
            stb.append("       T3.CLASSNAME, ");
            stb.append("       T4.SUBCLASSNAME, ");
            stb.append("       T1.* ");
            stb.append("     FROM RECORD0 T1 ");
            stb.append("     INNER JOIN CHAIR_A T5 ");
            stb.append("        ON T5.SUBCLASSCD    = T1.SUBCLASSCD ");
            stb.append("       AND T5.SCHREGNO      = T1.SCHREGNO ");
            stb.append("       AND T5.CLASSCD       = T1.CLASSCD ");
            stb.append("       AND T5.SCHOOL_KIND   = T1.SCHOOL_KIND ");
            stb.append("       AND T5.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("     INNER JOIN CLASS_MST T3 ");
            stb.append("        ON T3.CLASSCD = SUBSTR(T1.SUBCLASSCD,1,2) ");
            stb.append("       AND T3.SCHOOL_KIND   = T1.SCHOOL_KIND ");
            stb.append("     INNER JOIN SUBCLASS_MST T4 ");
            stb.append("        ON T4.SUBCLASSCD    = T1.SUBCLASSCD ");
            stb.append("       AND T4.CLASSCD       = T1.CLASSCD ");
            stb.append("       AND T4.SCHOOL_KIND   = T1.SCHOOL_KIND ");
            stb.append("       AND T4.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("     INNER JOIN SUBCLASS_YDAT SUBY ");
            stb.append("        ON SUBY.YEAR          = T1.YEAR ");
            stb.append("       AND SUBY.SUBCLASSCD    = T4.SUBCLASSCD ");
            stb.append("       AND SUBY.CLASSCD       = T4.CLASSCD ");
            stb.append("       AND SUBY.SCHOOL_KIND   = T4.SCHOOL_KIND ");
            stb.append("       AND SUBY.CURRICULUM_CD = T4.CURRICULUM_CD ");
            stb.append("   UNION ALL ");
            stb.append("     SELECT ");
            stb.append("       CAST(NULL AS VARCHAR(1)) AS CLASSNAME, ");
            stb.append("       CAST(NULL AS VARCHAR(1)) AS SUBCLASSNAME, ");
            stb.append("       T1.* ");
            stb.append("      FROM RECORD0 T1 ");
            stb.append("           LEFT JOIN ( SELECT T1.YEAR, T1.SEMESTER, T1.TESTKINDCD, T1.TESTITEMCD, T1.SCORE_DIV ");
            stb.append("                         FROM RECORD_RANK_SDIV_DAT T1 ");
            stb.append("                              INNER JOIN SCHREG_REGD_DAT REGD ");
            stb.append("                                      ON REGD.SCHREGNO = T1.SCHREGNO ");
            stb.append("                                     AND REGD.YEAR     = T1.YEAR ");
            if (SEMEALL.equals(_param._semester)) {
                stb.append("                                     AND REGD.SEMESTER = '" + _param._loginSemester + "' ");
            } else {
                stb.append("                                     AND REGD.SEMESTER = T1.SEMESTER ");
            }
            stb.append("                        WHERE ");
            stb.append("                              T1.YEAR       = '" + _param._loginYear + "' ");
            stb.append("                          AND T1.SUBCLASSCD = '" + ALL9 + "' ");
            stb.append("                        GROUP BY T1.YEAR, T1.SEMESTER, T1.TESTKINDCD, T1.TESTITEMCD, T1.SCORE_DIV  ");
            stb.append("                     ) T2");
            stb.append("                  ON T2.YEAR       = T1.YEAR ");
            stb.append("                 AND T2.SEMESTER   = T1.SEMESTER ");
            stb.append("                 AND T2.TESTKINDCD = T1.TESTKINDCD ");
            stb.append("                 AND T2.TESTITEMCD = T1.TESTITEMCD ");
            stb.append("                 AND T2.SCORE_DIV  = T1.SCORE_DIV ");
            stb.append("      WHERE T1.SUBCLASSCD = '" + ALL9 + "' ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("       T1.* ");
            stb.append(" FROM RECORD T1 ");
            stb.append(" ORDER BY ");
            stb.append("     T1.CLASSCD, ");
            stb.append("     T1.SCHOOL_KIND, ");
            stb.append("     T1.CURRICULUM_CD, ");
            stb.append("     T1.SUBCLASSCD,  ");
            stb.append("     T1.SEMESTER, ");
            stb.append("     T1.TESTKINDCD, ");
            stb.append("     T1.TESTITEMCD, ");
            stb.append("     T1.SCORE_DIV  ");

            return stb.toString();
        }

        /**
         * 学期+テスト種別のWHERE句を作成
         * @param tab テーブル別名
         * @param sdivs 学期+テスト種別
         * @return 作成した文字列
         */
        private StringBuffer divStr(final String tab, final String[] sdivs) {
            final StringBuffer divStr = new StringBuffer();
            divStr.append(" ( ");
            String or = "";
            for (int i = 0; i < sdivs.length; i++) {
                final String semester = sdivs[i].substring(0, 1);
                final String testkindcd = sdivs[i].substring(1, 3);
                final String testitemcd = sdivs[i].substring(3, 5);
                final String scorediv = sdivs[i].substring(5);
                divStr.append(or).append(" " + tab + "SEMESTER = '" + semester + "' AND " + tab + "TESTKINDCD = '" + testkindcd + "' AND " + tab + "TESTITEMCD = '" + testitemcd + "' AND " + tab + "SCORE_DIV = '" + scorediv + "' ");
                or = " OR ";
            }
            divStr.append(" ) ");
            return divStr;
        }

    }

    private static class ScoreData {
        final String _classcd;
        final String _classname;
        final String _subclasscd;
        final String _subclassname;
        final String _credits;
        final Map _scoreMap = new HashMap(); // 得点
        final Map _avgMap = new HashMap(); // 平均点
        final Map _gradeRankMap = new HashMap(); // 学年順位
        final Map _hrRankMap = new HashMap(); // クラス順位
        final Map _courceRankMap = new HashMap(); // コース順位
        final Map _majorRankMap = new HashMap(); // 専攻順位

        private ScoreData(
                final String classcd,
                final String classname,
                final String subclasscd,
                final String subclassname,
                final String credits
        ) {
            _classcd = classcd;
            _classname = classname;
            _subclasscd = subclasscd;
            _subclassname = subclassname;
            _credits = credits;
        }

        public String score(final String sdiv) {
            return StringUtils.defaultString((String) _scoreMap.get(sdiv), "");
        }

        public String avg(final String sdiv) {
            return StringUtils.defaultString((String) _avgMap.get(sdiv), "");
        }

        public String toString() {
            return "ScoreData(" + _subclasscd + ":" + _subclassname + ", scoreMap = " + _scoreMap + ")";
        }
    }

    private static class Rank {
        final String _rank;
        final String _avgRank;
        final String _count;
        final String _avg;
        final String _highscore;
        public Rank(final String rank, final String avgRank, final String count, final String avg, final String highscore) {
            _rank = rank;
            _avgRank = avgRank;
            _count = count;
            _avg = avg;
            _highscore = highscore;
        }
    }

    private static class SubclassMst implements Comparable<SubclassMst> {
        final String _specialDiv;
        final String _classcd;
        final String _subclasscd;
        final String _classabbv;
        final String _classname;
        final String _subclassname;
        final Integer _classShoworder3;
        final Integer _subclassShoworder3;
        final String _calculateCreditFlg;
        SubclassMst _combined = null;
        List<SubclassMst> _attendSubclassList = new ArrayList();
        public SubclassMst(final String specialDiv, final String classcd, final String subclasscd, final String classabbv, final String classname, final String subclassabbv, final String subclassname,
                final Integer classShoworder3,
                final Integer subclassShoworder3,
                final String calculateCreditFlg) {
            _specialDiv = specialDiv;
            _classcd = classcd;
            _subclasscd = subclasscd;
            _classabbv = classabbv;
            _classname = classname;
            _subclassname = subclassname;
            _classShoworder3 = classShoworder3;
            _subclassShoworder3 = subclassShoworder3;
            _calculateCreditFlg = calculateCreditFlg;
        }
        public boolean isMoto() {
            return null != _combined;
        }
        public boolean isSaki() {
            return !_attendSubclassList.isEmpty();
        }
        public int compareTo(final SubclassMst mst) {
            int rtn;
            rtn = _classShoworder3.compareTo(mst._classShoworder3);
            if (0 != rtn) { return rtn; }
            if (null == _classcd && null == mst._classcd) {
                return 0;
            } else if (null == _classcd) {
                return 1;
            } else if (null == mst._classcd) {
                return -1;
            }
            rtn = _classcd.compareTo(mst._classcd);
            if (0 != rtn) { return rtn; }
            rtn = _subclassShoworder3.compareTo(mst._subclassShoworder3);
            if (0 != rtn) { return rtn; }
            if (null == _subclasscd && null == mst._subclasscd) {
                return 0;
            } else if (null == _subclasscd) {
                return 1;
            } else if (null == mst._subclasscd) {
                return -1;
            }
            return _subclasscd.compareTo(mst._subclasscd);
        }
        public String toString() {
            return "SubclassMst(subclasscd = " + _subclasscd + ")";
        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 75874 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String[] _categorySelected;
        final String _grade;
        final String _semester;

        final String _prgid;
        final String _loginSemester;
        final String _loginYear;
        final String _date;
        final boolean _semes2Flg;
        final boolean _semes3Flg;
        final boolean _semes9Flg;
        final String _lowerScore;
        final String _avarageScore;
        final String _nendo;
        final String _schoolKind;
        final String _schoolKindName;
        private final Map _testItemMap;

        final Map _stampMap;

        private String _certifSchoolSchoolName;
        private String _certifSchoolJobName;
        private String _certifSchoolPrincipalName;

        private Map<String, SubclassMst> _subclassMstMap;
        private List _d026List = new ArrayList();

        final String _documentroot;
        final String _imagepath;
        final String _schoolLogoImagePath;
        final String _backSlashImagePath;
        final String _whiteSpaceImagePath;
        final String _schoolStampPath;

        private boolean _isNoPrintMoto;
        private boolean _isPrintSakiKamoku;

        private Map _staffNameMap;


        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {

            _categorySelected = request.getParameterValues("CLASS_SELECTED");
            _grade = request.getParameter("GRADE");
            _semester = request.getParameter("HID_SEMESTER");
            _semes2Flg = "2".equals(_semester) || "3".equals(_semester) || "9".equals(_semester) ? true : false;
            _semes3Flg = "3".equals(_semester) || "9".equals(_semester) ? true : false;
            _semes9Flg = "9".equals(_semester) ? true : false;
            _lowerScore = request.getParameter("LOWER_SCORE");
            _avarageScore = request.getParameter("AVARAGE_SCORE");

            _prgid = request.getParameter("PRGID");
            _loginSemester = request.getParameter("HID_SEMESTER");
            _loginYear = request.getParameter("HID_YEAR");
            _date = request.getParameter("LOGIN_DATE").replace('/', '-');


            _nendo = KNJ_EditDate.getAutoFormatYear(db2, Integer.parseInt(_loginYear)) + "年度";
            loadNameMstD026(db2);
            loadNameMstD026(db2);
            loadNameMstD016(db2);
            setPrintSakiKamoku(db2);
            _schoolKind = getSchoolKind(db2);
            _schoolKindName = getSchoolKindName(db2);
            _testItemMap = settestItemMap(db2);
            _stampMap = getStampNoMap(db2);

            setCertifSchoolDat(db2);

            setSubclassMst(db2);

            _documentroot = request.getParameter("DOCUMENTROOT");
            final String sqlControlMst = " SELECT IMAGEPATH FROM CONTROL_MST WHERE CTRL_NO = '01' ";
            _imagepath = KnjDbUtils.getOne(KnjDbUtils.query(db2, sqlControlMst));
            _schoolLogoImagePath = getImageFilePath("SCHOOLLOGO.jpg");
            _backSlashImagePath = getImageFilePath("slash_bs.jpg");
            _whiteSpaceImagePath = getImageFilePath("whitespace.png");
            _schoolStampPath = getImageFilePath("SCHOOLSTAMP.bmp");

            _staffNameMap = getStaffNameMap(db2);
        }

        private void setSubclassMst(
                final DB2UDB db2
        ) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            _subclassMstMap = new HashMap();
            try {
                String sql = "";
                sql += " SELECT ";
                sql += " VALUE(T2.SPECIALDIV, '0') AS SPECIALDIV, ";
                sql += " T1.CLASSCD, ";
                sql += " T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ";
                sql += " T2.CLASSABBV, VALUE(T2.CLASSORDERNAME2, T2.CLASSNAME) AS CLASSNAME, T1.SUBCLASSABBV, VALUE(T1.SUBCLASSORDERNAME2, T1.SUBCLASSNAME) AS SUBCLASSNAME, ";
                sql += " COMB1.CALCULATE_CREDIT_FLG, ";
                sql += " VALUE(T2.SHOWORDER3, 999) AS CLASS_SHOWORDER3, ";
                sql += " VALUE(T1.SHOWORDER3, 999) AS SUBCLASS_SHOWORDER3, ";
                sql += " ATT1.COMBINED_CLASSCD || '-' || ATT1.COMBINED_SCHOOL_KIND || '-' || ATT1.COMBINED_CURRICULUM_CD || '-' || ATT1.COMBINED_SUBCLASSCD AS COMBINED_SUBCLASSCD, ";
                sql += " COMB1.ATTEND_CLASSCD || '-' || COMB1.ATTEND_SCHOOL_KIND || '-' || COMB1.ATTEND_CURRICULUM_CD || '-' || COMB1.ATTEND_SUBCLASSCD AS ATTEND_SUBCLASSCD ";
                sql += " FROM SUBCLASS_MST T1 ";
                sql += " LEFT JOIN CLASS_MST T2 ON T2.CLASSCD = T1.CLASSCD AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
                sql += " LEFT JOIN SUBCLASS_REPLACE_COMBINED_DAT ATT1 ON ATT1.YEAR = '" + _loginYear + "' AND ATT1.ATTEND_CLASSCD = T1.CLASSCD AND ATT1.ATTEND_SCHOOL_KIND = T1.SCHOOL_KIND AND ATT1.ATTEND_CURRICULUM_CD = T1.CURRICULUM_CD AND ATT1.ATTEND_SUBCLASSCD = T1.SUBCLASSCD ";
                sql += " LEFT JOIN SUBCLASS_REPLACE_COMBINED_DAT COMB1 ON COMB1.YEAR = '" + _loginYear + "' AND COMB1.COMBINED_CLASSCD = T1.CLASSCD AND COMB1.COMBINED_SCHOOL_KIND = T1.SCHOOL_KIND AND COMB1.COMBINED_CURRICULUM_CD = T1.CURRICULUM_CD AND COMB1.COMBINED_SUBCLASSCD = T1.SUBCLASSCD ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    if (!_subclassMstMap.containsKey(rs.getString("SUBCLASSCD"))) {
                        final SubclassMst mst = new SubclassMst(rs.getString("SPECIALDIV"), rs.getString("CLASSCD"), rs.getString("SUBCLASSCD"), rs.getString("CLASSABBV"), rs.getString("CLASSNAME"), rs.getString("SUBCLASSABBV"), rs.getString("SUBCLASSNAME"), new Integer(rs.getInt("CLASS_SHOWORDER3")), new Integer(rs.getInt("SUBCLASS_SHOWORDER3")), rs.getString("CALCULATE_CREDIT_FLG"));
                        _subclassMstMap.put(rs.getString("SUBCLASSCD"), mst);
                    }
                    final SubclassMst combined = _subclassMstMap.get(rs.getString("COMBINED_SUBCLASSCD"));
                    if (null != combined) {
                        final SubclassMst mst = _subclassMstMap.get(rs.getString("SUBCLASSCD"));
                        mst._combined = combined;
                    }
                    final SubclassMst attend = _subclassMstMap.get(rs.getString("ATTEND_SUBCLASSCD"));
                    if (null != attend) {
                        final SubclassMst mst = _subclassMstMap.get(rs.getString("SUBCLASSCD"));
                        mst._attendSubclassList.add(attend);
                    }
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private String getSchoolKind(final DB2UDB db2) {
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _loginYear + "' AND GRADE = '" + _grade + "' ");

            return KnjDbUtils.getOne(KnjDbUtils.query(db2, sql.toString()));
        }

        private String getSchoolKindName(final DB2UDB db2) {
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT ABBV1 FROM NAME_MST WHERE NAMECD1 = 'A023' AND NAME1 = '" + _schoolKind + "' ");
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, sql.toString()));
        }

        private Map settestItemMap(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map map = new TreeMap();
            try {
                final StringBuffer sql = new StringBuffer();
                sql.append(" SELECT SEMESTER || TESTKINDCD || TESTITEMCD || SCORE_DIV AS TESTITEM, TESTITEMNAME ");
                sql.append(" FROM TESTITEM_MST_COUNTFLG_NEW_SDIV ");
                sql.append(" WHERE YEAR = '" + _loginYear + "' ");
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {

                    final String testitem = rs.getString("TESTITEM");
                    final String testitemname = StringUtils.defaultString(rs.getString("TESTITEMNAME"));
                    if (!map.containsKey(testitem)) {
                        map.put(testitem, testitemname);
                    }
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
            return map;
        }

        private void setCertifSchoolDat(final DB2UDB db2) {
            final String certifKindcd = "J".equals(_schoolKind) ? "103" : "104";
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT SCHOOL_NAME, JOB_NAME, PRINCIPAL_NAME, REMARK2, REMARK4, REMARK5 FROM CERTIF_SCHOOL_DAT ");
            sql.append(" WHERE YEAR = '" + _loginYear + "' AND CERTIF_KINDCD = '"+ certifKindcd +"' ");
            log.debug("certif_school_dat sql = " + sql.toString());

            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql.toString()));

            _certifSchoolSchoolName = StringUtils.defaultString(KnjDbUtils.getString(row, "SCHOOL_NAME"));
            _certifSchoolJobName = StringUtils.defaultString(KnjDbUtils.getString(row, "JOB_NAME"), "校長");
            _certifSchoolPrincipalName = StringUtils.defaultString(KnjDbUtils.getString(row, "PRINCIPAL_NAME"));
        }

        public String getImageFilePath(final String name) {
            final String path = _documentroot + "/" + (null == _imagepath || "".equals(_imagepath) ? "" : _imagepath + "/") + name;
            final boolean exists = new java.io.File(path).exists();
            log.warn(" path " + path + " exists: " + exists);
            if (exists) {
                return path;
            }
            return null;
        }

        private Map getStampNoMap(final DB2UDB db2) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     STAFFCD, ");
            stb.append("     MAX(STAMP_NO) AS STAMP_NO ");
            stb.append(" FROM ");
            stb.append("     ATTEST_INKAN_DAT ");
            stb.append(" GROUP BY ");
            stb.append("     STAFFCD ");

            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map retMap = new HashMap();
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String staffcd = rs.getString("STAFFCD");
                    final String stampNo = rs.getString("STAMP_NO");
                    retMap.put(staffcd, stampNo);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retMap;
        }

        public String getStaffImageFilePath(final String staffCd) {
            final String stampNo = (String) _stampMap.get(staffCd);
            final String path = _documentroot + "/image/stamp/" + stampNo + ".bmp";
            final boolean exists = new java.io.File(path).exists();
            log.warn(" path " + path + " exists: " + exists);
            if (exists) {
                return path;
            }
            return null;
        }

        private void loadNameMstD016(final DB2UDB db2) {
            _isNoPrintMoto = false;
            final String sql = "SELECT NAMECD2, NAMESPARE1, NAMESPARE2 FROM V_NAME_MST WHERE YEAR = '" + _loginYear + "' AND NAMECD1 = 'D016' AND NAMECD2 = '01' ";
            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql));
            if ("Y".equals(KnjDbUtils.getString(row, "NAMESPARE1"))) _isNoPrintMoto = true;
            log.info("(名称マスタD016):元科目を表示しない = " + _isNoPrintMoto);
        }

        /**
         * 合併先科目を印刷するか
         */
        private void setPrintSakiKamoku(final DB2UDB db2) {
            // 初期値：印刷する
            _isPrintSakiKamoku = true;
            // 名称マスタ「D021」「01」から取得する
            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, "SELECT NAMESPARE3 FROM V_NAME_MST WHERE YEAR='" + _loginYear+ "' AND NAMECD1 = 'D021' AND NAMECD2 = '01' "));
            if ("Y".equals(KnjDbUtils.getString(row, "NAMESPARE3"))) {
                _isPrintSakiKamoku = false;
            }
            log.debug("合併先科目を印刷するか：" + _isPrintSakiKamoku);
        }

        private void loadNameMstD026(final DB2UDB db2) {

            final StringBuffer sql = new StringBuffer();
                final String field = SEMEALL.equals(_semester) ? "NAMESPARE1" : "ABBV" + _semester;
                sql.append(" SELECT NAME1 AS SUBCLASSCD FROM V_NAME_MST ");
                sql.append(" WHERE YEAR = '" + _loginYear + "' AND NAMECD1 = 'D026' AND " + field + " = '1'  ");

            _d026List.clear();
            _d026List.addAll(KnjDbUtils.getColumnDataList(KnjDbUtils.query(db2, sql.toString()), "SUBCLASSCD"));
            log.info("非表示科目:" + _d026List);
        }

        private Map getStaffNameMap(final DB2UDB db2) {
            Map rtnMap = new HashMap();
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH C_STAFF AS(  ");
            stb.append("     SELECT YEAR, SEMESTER, CHAIRCD, MIN(STAFFCD) AS STAFFCD ");
            stb.append("       FROM CHAIR_STF_DAT ");
            stb.append("      WHERE YEAR      = '"+ _loginYear +"' ");
            stb.append("        AND SEMESTER  = '"+ _loginSemester +"' ");
            stb.append("        AND CHARGEDIV = '1' ");
            stb.append("   GROUP BY YEAR, SEMESTER, CHAIRCD ");
            stb.append(" ) ");
            stb.append("  SELECT DISTINCT ");
            stb.append("      C_STF.CHAIRCD, ");
            stb.append("      C_STF.STAFFCD, ");
            stb.append("      STF.STAFFNAME, ");
            stb.append("      C_DAT.CLASSCD || '-' || C_DAT.SCHOOL_KIND || '-' || C_DAT.CURRICULUM_CD || '-' || C_DAT.SUBCLASSCD AS SUBCLASSCD ");
            stb.append("  FROM ");
            stb.append("      C_STAFF C_STF ");
            stb.append("      INNER JOIN CHAIR_DAT C_DAT ");
            stb.append("              ON C_DAT.YEAR     = C_STF.YEAR ");
            stb.append("             AND C_DAT.SEMESTER = C_STF.SEMESTER ");
            stb.append("             AND C_DAT.CHAIRCD  = C_STF.CHAIRCD ");
            stb.append("      INNER JOIN CLASS_MST CLASS_M ");
            stb.append("              ON CLASS_M.CLASSCD     = C_DAT.CLASSCD ");
            stb.append("             AND CLASS_M.SCHOOL_KIND = C_DAT.SCHOOL_KIND ");
            stb.append("      INNER JOIN SUBCLASS_MST SUBCLASS_M ");
            stb.append("              ON SUBCLASS_M.SUBCLASSCD    = C_DAT.SUBCLASSCD ");
            stb.append("             AND SUBCLASS_M.CLASSCD       = C_DAT.CLASSCD ");
            stb.append("             AND SUBCLASS_M.SCHOOL_KIND   = C_DAT.SCHOOL_KIND ");
            stb.append("             AND SUBCLASS_M.CURRICULUM_CD = C_DAT.CURRICULUM_CD ");
            stb.append("      INNER JOIN SUBCLASS_YDAT SUBCLASS_Y ");
            stb.append("              ON SUBCLASS_Y.SUBCLASSCD    = C_DAT.SUBCLASSCD ");
            stb.append("             AND SUBCLASS_Y.SUBCLASSCD    = C_DAT.SUBCLASSCD ");
            stb.append("             AND SUBCLASS_Y.CLASSCD       = C_DAT.CLASSCD ");
            stb.append("             AND SUBCLASS_Y.SCHOOL_KIND   = C_DAT.SCHOOL_KIND ");
            stb.append("             AND SUBCLASS_Y.CURRICULUM_CD = C_DAT.CURRICULUM_CD ");
            stb.append("      LEFT JOIN STAFF_MST STF ");
            stb.append("             ON STF.STAFFCD = C_STF.STAFFCD ");
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final Map map = new HashMap();
                    map.put("CHAIRCD", StringUtils.defaultString(rs.getString("CHAIRCD")));
                    map.put("STAFFCD", StringUtils.defaultString(rs.getString("STAFFCD")));
                    map.put("STAFFNAME", StringUtils.defaultString(rs.getString("STAFFNAME")));
                    map.put("SUBCLASSCD", StringUtils.defaultString(rs.getString("SUBCLASSCD")));

                    final String key = StringUtils.defaultString(rs.getString("SUBCLASSCD"));
                    rtnMap.put(key, map);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return rtnMap;
        }

    }
}

// eof
