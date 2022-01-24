/**     プロジェクト/プロパティ/javaコード・スタイル/コード・テンプレート/パターン       ***/

/*
 * 作成日: 2021/01/25
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
import java.util.LinkedHashMap;
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
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

public class KNJD627E {

    private static final Log log = LogFactory.getLog(KNJD627E.class);

    private static final String SEMEALL = "9";
    private static final String SELECT_CLASSCD_UNDER = "90";

    private static final String SDIV9990008 = "9990008"; //学年末評価

    private static final String ALL3 = "333333";
    private static final String ALL5 = "555555";

    private static final int FAILING_SCORE = 54;
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
        final Map<String, Map<String, Student>> schregnoMap = getSchregnoMap(db2);
        //欠課
        SubclassAttendance.load(db2, _param, schregnoMap);

        //通知票
        printSvfMain(db2, svf, schregnoMap);
        svf.VrEndPage();
    }

    private String add(final String num1, final String num2) {
        if (!NumberUtils.isDigits(num1)) { return num2; }
        if (!NumberUtils.isDigits(num2)) { return num1; }
        return String.valueOf(Integer.parseInt(num1) + Integer.parseInt(num2));
    }

    private void printSvfMain(final DB2UDB db2, final Vrw32alp svf, final Map schregnoMap) {
        final List subclassList = subclassListRemoveD026();
        Collections.sort(subclassList);

        //印字する科目の設定
        boolean outputFlg = false;
        for (Iterator itSchregno = schregnoMap.keySet().iterator(); itSchregno.hasNext();) {
            String schregno = (String) itSchregno.next();
            Map gradeMap = (Map) schregnoMap.get(schregno);

            for (Iterator itGrade = gradeMap.keySet().iterator(); itGrade.hasNext();) {
                String grade = (String) itGrade.next();
                Student student = (Student) gradeMap.get(grade);

                for (Iterator itSubclass = subclassList.iterator(); itSubclass.hasNext();) {
                    final SubclassMst subclassMst = (SubclassMst) itSubclass.next();
                    final String subclassCd = subclassMst._subclasscd;

                    final boolean isPrint = student._printSubclassMap.containsKey(subclassCd);
                    if (!isPrint) {
                        continue;
                    }

                    //欠課時数超過の科目は、再試験不可
                    if (student._attendSubClassMap.containsKey(subclassCd)) {
                        final SubclassAttendance attendance = student._attendSubClassMap.get(subclassCd);
                        if(attendance._isOver) {
                            itSubclass.remove();
                            continue;
                        }
                    }
                    outputFlg = true; //印字する科目が1つ以上存在
                }
            }
        }

        //印字する科目が存在しない場合
        if(outputFlg == false) {
            return;
        }

        final String form = "KNJD627E.frm";
        svf.VrSetForm(form , 4);

        for (Iterator itSchregno = schregnoMap.keySet().iterator(); itSchregno.hasNext();) {
            String schregno = (String) itSchregno.next();
            Map gradeMap = (Map) schregnoMap.get(schregno);

            //明細部以外を印字
            printTitle(db2, svf, (Student) gradeMap.get(gradeMap.keySet().iterator().next()));

            for (Iterator itGrade = gradeMap.keySet().iterator(); itGrade.hasNext();) {
                String grade = (String) itGrade.next();
                Student student = (Student) gradeMap.get(grade);

                //■成績一覧
                for (Iterator itSubclass = subclassList.iterator(); itSubclass.hasNext();) {
                    final SubclassMst subclassMst = (SubclassMst) itSubclass.next();
                    final String subclassCd = subclassMst._subclasscd;

                    if (student._printSubclassMap.containsKey(subclassCd)) {
                        final ScoreData scoreData = (ScoreData) student._printSubclassMap.get(subclassCd);

                        //科目名
                        final int length = KNJ_EditEdit.getMS932ByteLength(scoreData._subclassname);
                        final String field = length > 28 ? "2" : "1";
                        svf.VrsOut("SUBCLASS_NAME" + field, scoreData._subclassname);

                        //単位数
                        svf.VrsOut("CREDIT", scoreData._credits);

                        //得点
                        final String score = scoreData.score(SDIV9990008);
                        final String slumpScore = scoreData.slumpScore(SDIV9990008);
                        String outSlumpScore = slumpScore;
                        if(Integer.parseInt(slumpScore) <= Integer.parseInt(score)) {
                            outSlumpScore = "変更なし"; //認定前と同じ、又は悪くなった場合
                        }
                        svf.VrsOut("SCORE", score); //認定前点数
                        svf.VrsOut("RESULT", outSlumpScore); //認定結果・点数

                        svf.VrEndRecord();
                        _hasData = true;
                    }
                }
            }

            svf.VrEndRecord();
        }
    }

    private void printTitle(final DB2UDB db2, final Vrw32alp svf, final Student student) {
        //年組番氏名
        svf.VrsOut("HR_NAME", student._grade.substring(1) + "年　" + student._hrname + "　" + student._attendno);
        final String name =  student._name + "　さん";
        final String field = KNJ_EditEdit.getMS932ByteLength(name) > 36 ? "3" : KNJ_EditEdit.getMS932ByteLength(name) > 26 ? "2" : "1";
        svf.VrsOut("NAME" + field, name);

        //学期名称
        String semestername = "";
        Map nameD039 = _param.getNameMst(db2, "D039");
        if(nameD039 != null) {
            Map map = (Map) nameD039.get(_param._semester);
            if(map != null) semestername = (String)map.get("NAME1");
        }
        svf.VrsOut("TITLE", semestername + "再試験結果通知書"); //タイトル

        //文言
        if(_param._documentMstMap.size() > 0) {
            if(_param._documentMstMap.size() > 0) {
                Map map = (Map) _param._documentMstMap.get("D5");
                if(map != null) {
                    svf.VrsOut("TEXT", (String)map.get("TEXT"));
                }
            }
        }

        //出力日
        String[] date = _param._date.split("-", 0);
        if (date != null && date.length == 3) {
            date[1] = String.valueOf(Integer.parseInt(date[1]));
            date[2] = String.valueOf(Integer.parseInt(date[2]));
            svf.VrsOut("DATE", date[0] + "．" + date[1] + "．" + date[2]);
        }

        svf.VrsOut("SCHOOL_NAME", _param._certifSchoolSchoolName); //学校名
        svf.VrsOut("SECTION_NAME", "教　務　部"); //部署名
    }

    private List subclassListRemoveD026() {
        final List retList = new ArrayList(_param._subclassMstMap.values());
        for (final Iterator it = retList.iterator(); it.hasNext();) {
            final SubclassMst subclassMst = (SubclassMst) it.next();
            if (_param._d026List.contains(subclassMst._subclasscd)) {
                it.remove();
            }
            if (_param._isNoPrintMoto &&  subclassMst.isMoto() || !_param._isPrintSakiKamoku &&  subclassMst.isSaki()) {
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

    private Map<String, Map<String, Student>> getSchregnoMap(final DB2UDB db2) {
        Map<String, Map<String, Student>> schregnoMap = new LinkedHashMap<String, Map<String, Student>>();
        Map<String, Student> gradeMap = null;

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getStudentSql();
            log.debug(" getStudentSql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final Student student = new Student();
                student._year = rs.getString("YEAR");
                student._oldYear = rs.getString("OLD_YEAR");
                student._oldGrade = rs.getString("OLD_GRADE");
                student._schregno = rs.getString("SCHREGNO");
                student._name = rs.getString("NAME");
                student._schoolKind = rs.getString("SCHOOL_KIND");
                student._hrname = rs.getString("HR_CLASS_NAME1") + "組" ;
                student._attendno = NumberUtils.isDigits(rs.getString("ATTENDNO")) ? String.valueOf(Integer.parseInt(rs.getString("ATTENDNO"))) + "番" : rs.getString("ATTENDNO");
                student._grade = rs.getString("GRADE");
                student._hrClass = rs.getString("HR_CLASS");

                student.setSubclass(db2);
                if (schregnoMap.containsKey(student._schregno)) {
                    gradeMap = schregnoMap.get(student._schregno);
                } else {
                    gradeMap = new LinkedHashMap<String, Student>();
                    schregnoMap.put(student._schregno, gradeMap);
                }

                gradeMap.put(student._oldGrade, student);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }

        return schregnoMap;
    }

    private String getStudentSql() {
        final StringBuffer stb = new StringBuffer();
        //選択された年組の生徒
        stb.append("WITH SCHNO_A AS( ");
        stb.append("    SELECT ");
        stb.append("            T1.* ");
        stb.append("    FROM    SCHREG_REGD_DAT T1 ");
        stb.append("    WHERE   T1.YEAR     = '" + _param._loginYear + "' ");
        stb.append("        AND T1.SEMESTER = '" + _param._loginSemester + "' ");
        stb.append("        AND T1.GRADE    = '" + _param._grade + "' ");
        stb.append("        AND T1.HR_CLASS IN " + SQLUtils.whereIn(true, _param._categorySelected));
        //対象生徒の過年度
        stb.append(" ) , SCHNO_HIST_A AS( ");
        stb.append("    SELECT DISTINCT ");
        stb.append("            REGD.YEAR, ");
        stb.append("            REGD.SCHREGNO, ");
        stb.append("            REGD.GRADE ");
        stb.append("    FROM    SCHREG_REGD_DAT REGD ");
        stb.append("            INNER JOIN SCHNO_A T2 ");
        stb.append("                    ON T2.SCHREGNO = REGD.SCHREGNO ");
        stb.append("    WHERE   REGD.YEAR < '" + _param._loginYear + "' ");
        //再試験結果が存在する生徒
        stb.append(" ) , SCHNO AS(");
        stb.append("    SELECT DISTINCT ");
        stb.append("            T1.*, ");
        stb.append("            HIST.YEAR AS OLD_YEAR, ");
        stb.append("            HIST.GRADE AS OLD_GRADE ");
        stb.append("    FROM    SCHNO_A T1 ");
        stb.append("            INNER JOIN SCHNO_HIST_A HIST ");
        stb.append("                    ON T1.SCHREGNO  = HIST.SCHREGNO ");
        stb.append("            INNER JOIN RECORD_SLUMP_SDIV_DAT T2 ");
        stb.append("                    ON T2.YEAR       = HIST.YEAR ");
        stb.append("                   AND T2.SEMESTER || T2.TESTKINDCD || T2.TESTITEMCD || T2.SCORE_DIV = '" + SDIV9990008 + "' ");
        stb.append("                   AND T2.SCHREGNO   = HIST.SCHREGNO ");
        stb.append("                   AND T2.SCORE     <= " + String.valueOf(FAILING_SCORE) + " ");
        stb.append("                   AND T2.CLASSCD   <= '" + SELECT_CLASSCD_UNDER +"' ");
        stb.append("            INNER JOIN CLASS_MST T3 ");
        stb.append("                    ON T3.CLASSCD       = T2.CLASSCD ");
        stb.append("                   AND T3.SCHOOL_KIND   = T2.SCHOOL_KIND ");
        stb.append("            INNER JOIN SUBCLASS_MST T4 ");
        stb.append("                    ON T4.SUBCLASSCD    = T2.SUBCLASSCD ");
        stb.append("                   AND T4.CLASSCD       = T2.CLASSCD ");
        stb.append("                   AND T4.SCHOOL_KIND   = T2.SCHOOL_KIND ");
        stb.append("                   AND T4.CURRICULUM_CD = T2.CURRICULUM_CD ");
        stb.append(" ) ");
        //メイン表
        stb.append("     SELECT  REGD.YEAR ");
        stb.append("            ,REGD.OLD_YEAR ");
        stb.append("            ,REGD.OLD_GRADE ");
        stb.append("            ,REGD.SCHREGNO ");
        stb.append("            ,REGD.SEMESTER ");
        stb.append("            ,BASE.NAME ");
        stb.append("            ,REGDG.SCHOOL_KIND ");
        stb.append("            ,VALUE(REGDH.HR_CLASS_NAME1, '') AS HR_CLASS_NAME1 ");
        stb.append("            ,REGD.ATTENDNO ");
        stb.append("            ,REGDG.GRADE ");
        stb.append("            ,REGD.HR_CLASS ");
        stb.append("     FROM    SCHNO REGD ");
        stb.append("     LEFT JOIN SCHREG_REGD_GDAT REGDG ");
        stb.append("                   ON REGDG.YEAR  = REGD.YEAR ");
        stb.append("                  AND REGDG.GRADE = REGD.GRADE ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT REGDH ");
        stb.append("                   ON REGDH.YEAR     = REGD.YEAR ");
        stb.append("                  AND REGDH.SEMESTER = REGD.SEMESTER ");
        stb.append("                  AND REGDH.GRADE    = REGD.GRADE ");
        stb.append("                  AND REGDH.HR_CLASS = REGD.HR_CLASS ");
        stb.append("     LEFT JOIN SCHREG_BASE_MST BASE ");
        stb.append("                   ON BASE.SCHREGNO = REGD.SCHREGNO ");
        stb.append("     ORDER BY ");
        stb.append("         REGD.GRADE, ");
        stb.append("         REGD.HR_CLASS, ");
        stb.append("         REGD.ATTENDNO ");

        return stb.toString();
    }

    private static String sishaGonyu(final String val) {
        if (!NumberUtils.isNumber(val)) {
            return null;
        }
        return new BigDecimal(val).setScale(1, BigDecimal.ROUND_HALF_UP).toString();
    }

    private class Student {
        String _year;
        String _oldYear;
        String _oldGrade;
        String _schregno;
        String _name;
        String _schoolKind;
        String _hrname;
        String _attendno;
        String _grade;
        String _hrClass;
        final Map _attendMap = new TreeMap();
        final Map _printSubclassMap = new TreeMap();
        final Map<String, SubclassAttendance> _attendSubClassMap = new HashMap();

        private void setSubclass(final DB2UDB db2) {
            final String scoreSql = prestatementSubclass();
            log.debug(" scoreSql = " + scoreSql);
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
                    subclasscd = rs.getString("YEAR") + "-" + subclasscd;

                    final String key = subclasscd;
                    if (!_printSubclassMap.containsKey(key)) {
                        _printSubclassMap.put(key, new ScoreData(classcd, classname, subclasscd, subclassname, credits));
                    }
                    if (null == rs.getString("SEMESTER")) {
                        continue;
                    }

                    final ScoreData scoreData = (ScoreData) _printSubclassMap.get(key);
                    final String testcd = rs.getString("SEMESTER") + rs.getString("TESTKINDCD") + rs.getString("TESTITEMCD") + rs.getString("SCORE_DIV");

                    final int moreScore = Integer.parseInt(StringUtils.defaultString(rs.getString("MORE_SCORE"),"0")); //最新を除く試験で一番良い結果
                    final int maxScore = Integer.parseInt(StringUtils.defaultString(rs.getString("MAX_SCORE"),"0")); //最新の追試結果

                    if (moreScore < maxScore) {
                        //最後の追試の結果がよい場合
                        scoreData._scoreMap.put(testcd, String.valueOf(moreScore));
                        scoreData._slumpScoreMap.put(testcd, String.valueOf(maxScore));
                    } else {
                        //追試前の結果がよい場合
                        scoreData._scoreMap.put(testcd, String.valueOf(moreScore));
                        scoreData._slumpScoreMap.put(testcd, "0"); // 0 ⇒ 変更無し
                    }
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
            final StringBuffer divStr = divStr("T1.", sdivs);

            stb.append(" WITH SCHNO AS( ");
            //対象生徒の過年度の最終学期
            stb.append(" SELECT ");
            stb.append("     T2.* ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT T2 ");
            stb.append(" WHERE ");
            stb.append("         T2.YEAR     = '" + _oldYear     + "'  ");
            stb.append("     AND T2.SCHREGNO = '" + _schregno + "'  ");
            stb.append("     AND T2.SEMESTER = (SELECT ");
            stb.append("                          MAX(SEMESTER) ");
            stb.append("                        FROM ");
            stb.append("                          SCHREG_REGD_DAT W2 ");
            stb.append("                        WHERE ");
            stb.append("                          W2.YEAR = '" + _oldYear + "'  ");
            stb.append("                          AND W2.SCHREGNO = T2.SCHREGNO ");
            stb.append("                       ) ");
            //成績明細データの表
            stb.append(" ) ,RECORD00 AS( ");
            stb.append("     SELECT DISTINCT ");
            stb.append("         T1.YEAR, T1.SEMESTER, T1.TESTKINDCD, T1.TESTITEMCD, T1.SCORE_DIV, T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, T1.SCHREGNO ");
            stb.append("     FROM RECORD_SCORE_DAT T1 ");
            stb.append("     INNER JOIN SCHNO T2 ");
            stb.append("             ON T2.YEAR     = T1.YEAR ");
            stb.append("            AND T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("     WHERE ");
            stb.append("         (" + divStr + ") ");
            stb.append("         AND T1.CLASSCD    <= '" + SELECT_CLASSCD_UNDER + "' ");
            stb.append("         AND T1.SUBCLASSCD NOT IN ('" + ALL3 + "', '" + ALL5 + "') ");
            stb.append(" ) ,SCORE AS( ");
            stb.append("     SELECT   T1.YEAR, T1.SEMESTER, T1.TESTKINDCD, T1.TESTITEMCD, T1.SCORE_DIV, T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, T1.SCHREGNO ");
            stb.append("            , T3.SCORE ");
            stb.append("            , 0 AS SLUMP_SEQ ");
            stb.append("     FROM RECORD00 T1 ");
            stb.append("     INNER JOIN RECORD_SCORE_DAT T3 ");
            stb.append("             ON T3.YEAR          = T1.YEAR ");
            stb.append("            AND T3.SEMESTER      = T1.SEMESTER ");
            stb.append("            AND T3.TESTKINDCD    = T1.TESTKINDCD ");
            stb.append("            AND T3.TESTITEMCD    = T1.TESTITEMCD ");
            stb.append("            AND T3.SCORE_DIV     = T1.SCORE_DIV ");
            stb.append("            AND T3.CLASSCD       = T1.CLASSCD ");
            stb.append("            AND T3.SCHOOL_KIND   = T1.SCHOOL_KIND ");
            stb.append("            AND T3.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("            AND T3.SUBCLASSCD    = T1.SUBCLASSCD ");
            stb.append("            AND T3.SCHREGNO      = T1.SCHREGNO ");
            stb.append("     UNION ALL ");
            stb.append("     SELECT   T1.YEAR, T1.SEMESTER, T1.TESTKINDCD, T1.TESTITEMCD, T1.SCORE_DIV, T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, T1.SCHREGNO ");
            stb.append("            , RSSD.SCORE ");
            stb.append("            , RSSD.SLUMP_SEQ ");
            stb.append("     FROM RECORD00 T1 ");
            stb.append("     INNER JOIN RECORD_SLUMP_SEQ_SDIV_DAT RSSD ");
            stb.append("             ON RSSD.YEAR          = T1.YEAR ");
            stb.append("            AND RSSD.SEMESTER      = T1.SEMESTER ");
            stb.append("            AND RSSD.TESTKINDCD    = T1.TESTKINDCD ");
            stb.append("            AND RSSD.TESTITEMCD    = T1.TESTITEMCD ");
            stb.append("            AND RSSD.SCORE_DIV     = T1.SCORE_DIV ");
            stb.append("            AND RSSD.CLASSCD       = T1.CLASSCD ");
            stb.append("            AND RSSD.SCHOOL_KIND   = T1.SCHOOL_KIND ");
            stb.append("            AND RSSD.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("            AND RSSD.SUBCLASSCD    = T1.SUBCLASSCD ");
            stb.append("            AND RSSD.SCHREGNO      = T1.SCHREGNO ");
            stb.append("            AND RSSD.SCORE        <= " + String.valueOf(FAILING_SCORE) + " ");
            stb.append(" ) ,MAX_SCORE_SEQ AS( ");
            stb.append("     SELECT   YEAR, SEMESTER, TESTKINDCD, TESTITEMCD, SCORE_DIV, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, SCHREGNO ");
            stb.append("            , MAX(SLUMP_SEQ) AS SLUMP_SEQ ");
            stb.append("     FROM SCORE ");
            stb.append("     GROUP BY YEAR, SEMESTER, TESTKINDCD, TESTITEMCD, SCORE_DIV, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, SCHREGNO ");
            stb.append(" ) ,MAX_SCORE AS( ");
            stb.append("     SELECT   T1.YEAR, T1.SEMESTER, T1.TESTKINDCD, T1.TESTITEMCD, T1.SCORE_DIV, T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, T1.SCHREGNO ");
            stb.append("            , T1.SCORE ");
            stb.append("     FROM SCORE T1 ");
            stb.append("     INNER JOIN MAX_SCORE_SEQ T2 ");
            stb.append("             ON T2.YEAR          = T1.YEAR ");
            stb.append("            AND T2.SEMESTER      = T1.SEMESTER ");
            stb.append("            AND T2.TESTKINDCD    = T1.TESTKINDCD ");
            stb.append("            AND T2.TESTITEMCD    = T1.TESTITEMCD ");
            stb.append("            AND T2.SCORE_DIV     = T1.SCORE_DIV ");
            stb.append("            AND T2.CLASSCD       = T1.CLASSCD ");
            stb.append("            AND T2.SCHOOL_KIND   = T1.SCHOOL_KIND ");
            stb.append("            AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("            AND T2.SUBCLASSCD    = T1.SUBCLASSCD ");
            stb.append("            AND T2.SCHREGNO      = T1.SCHREGNO ");
            stb.append("            AND T2.SLUMP_SEQ     = T1.SLUMP_SEQ ");
            stb.append(" ) ,MORE_SCORE AS( ");
            stb.append("     SELECT   T2.YEAR, T2.SEMESTER, T2.TESTKINDCD, T2.TESTITEMCD, T2.SCORE_DIV, T2.CLASSCD, T2.SCHOOL_KIND, T2.CURRICULUM_CD, T2.SUBCLASSCD, T2.SCHREGNO ");
            stb.append("            , MAX(T2.SCORE) AS SCORE ");
            stb.append("     FROM MAX_SCORE_SEQ T1 ");
            stb.append("     INNER JOIN SCORE T2 ");
            stb.append("             ON T2.YEAR          = T1.YEAR ");
            stb.append("            AND T2.SEMESTER      = T1.SEMESTER ");
            stb.append("            AND T2.TESTKINDCD    = T1.TESTKINDCD ");
            stb.append("            AND T2.TESTITEMCD    = T1.TESTITEMCD ");
            stb.append("            AND T2.SCORE_DIV     = T1.SCORE_DIV ");
            stb.append("            AND T2.CLASSCD       = T1.CLASSCD ");
            stb.append("            AND T2.SCHOOL_KIND   = T1.SCHOOL_KIND ");
            stb.append("            AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("            AND T2.SUBCLASSCD    = T1.SUBCLASSCD ");
            stb.append("            AND T2.SCHREGNO      = T1.SCHREGNO ");
            stb.append("            AND T2.SLUMP_SEQ    <= (T1.SLUMP_SEQ - 1) ");
            stb.append("     GROUP BY T2.YEAR, T2.SEMESTER, T2.TESTKINDCD, T2.TESTITEMCD, T2.SCORE_DIV, T2.CLASSCD, T2.SCHOOL_KIND, T2.CURRICULUM_CD, T2.SUBCLASSCD, T2.SCHREGNO ");
            stb.append(" ) ,RECORD0 AS( ");
            stb.append("     SELECT ");
            stb.append("              T1.YEAR, T1.SEMESTER, T1.TESTKINDCD, T1.TESTITEMCD, T1.SCORE_DIV, T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, T1.SCHREGNO ");
            stb.append("            , L1.CREDITS ");
            stb.append("            , T3.SCORE AS MAX_SCORE ");
            stb.append("            , T4.SCORE AS MORE_SCORE ");
            stb.append("     FROM RECORD00 T1 ");
            stb.append("     INNER JOIN SCHNO T2 ");
            stb.append("             ON T2.YEAR     = T1.YEAR ");
            stb.append("            AND T2.SCHREGNO = T1.SCHREGNO ");
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
            stb.append("     INNER JOIN MAX_SCORE T3 ");
            stb.append("             ON T3.YEAR          = T1.YEAR ");
            stb.append("            AND T3.SEMESTER      = T1.SEMESTER ");
            stb.append("            AND T3.TESTKINDCD    = T1.TESTKINDCD ");
            stb.append("            AND T3.TESTITEMCD    = T1.TESTITEMCD ");
            stb.append("            AND T3.SCORE_DIV     = T1.SCORE_DIV ");
            stb.append("            AND T3.CLASSCD       = T1.CLASSCD ");
            stb.append("            AND T3.SCHOOL_KIND   = T1.SCHOOL_KIND ");
            stb.append("            AND T3.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("            AND T3.SUBCLASSCD    = T1.SUBCLASSCD ");
            stb.append("            AND T3.SCHREGNO      = T1.SCHREGNO ");
            stb.append("     INNER JOIN MORE_SCORE T4 ");
            stb.append("             ON T4.YEAR          = T1.YEAR ");
            stb.append("            AND T4.SEMESTER      = T1.SEMESTER ");
            stb.append("            AND T4.TESTKINDCD    = T1.TESTKINDCD ");
            stb.append("            AND T4.TESTITEMCD    = T1.TESTITEMCD ");
            stb.append("            AND T4.SCORE_DIV     = T1.SCORE_DIV ");
            stb.append("            AND T4.CLASSCD       = T1.CLASSCD ");
            stb.append("            AND T4.SCHOOL_KIND   = T1.SCHOOL_KIND ");
            stb.append("            AND T4.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("            AND T4.SUBCLASSCD    = T1.SUBCLASSCD ");
            stb.append("            AND T4.SCHREGNO      = T1.SCHREGNO ");
            stb.append(" ) ,RECORD AS( ");
            stb.append("     SELECT ");
            stb.append("       T3.CLASSNAME, ");
            stb.append("       T4.SUBCLASSNAME, ");
            stb.append("       T1.* ");
            stb.append("     FROM RECORD0 T1 ");
            stb.append("     INNER JOIN CLASS_MST T3 ");
            stb.append("        ON T3.CLASSCD     = T1.CLASSCD ");
            stb.append("       AND T3.SCHOOL_KIND = T1.SCHOOL_KIND ");
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
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("       T1.* ");
            stb.append(" FROM RECORD T1 ");
            stb.append(" ORDER BY ");
            stb.append("     T1.YEAR, ");
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

        /**
         * 年度の開始日を取得する
         */
        private Semester loadSemester(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            Semester rtnSeme = null;
            try {
                final String sql = "select"
                        + "   SEMESTER,"
                        + "   SEMESTERNAME,"
                        + "   SDATE,"
                        + "   EDATE"
                        + " from"
                        + "   V_SEMESTER_GRADE_MST"
                        + " where"
                        + "   YEAR         = '" + _oldYear + "' "
                        + "   AND SEMESTER = '" + SEMEALL + "' "
                        + "   AND GRADE    = '" + _oldGrade + "' "
                        + " order by SEMESTER"
                    ;

                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtnSeme = new Semester(rs.getString("SEMESTER"), rs.getString("SEMESTERNAME"), rs.getString("SDATE"), rs.getString("EDATE"));
                }
            } catch (final Exception ex) {
                log.error("テスト項目のロードでエラー", ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }

            return rtnSeme;
        }
    }

    private static class ScoreData {
        final String _classcd;
        final String _classname;
        final String _subclasscd;
        final String _subclassname;
        final String _credits;
        final Map _scoreMap = new HashMap(); // 得点
        final Map _slumpScoreMap = new HashMap(); // 再試験得点

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

        public String slumpScore(final String sdiv) {
           return StringUtils.defaultString((String) _slumpScoreMap.get(sdiv), "");
        }

        public String toString() {
            return "ScoreData(" + _subclasscd + ":" + _subclassname + ", scoreMap = " + _scoreMap + ")";
        }
    }

    private static class SubclassAttendance {
        final BigDecimal _lesson;
        final BigDecimal _attend;
        final BigDecimal _sick;
        final BigDecimal _late;
        final BigDecimal _early;
        final BigDecimal _notice_only;
        final BigDecimal _nonotice_only;
        boolean _isOver;

        public SubclassAttendance(final BigDecimal lesson, final BigDecimal attend, final BigDecimal sick, final BigDecimal late, final BigDecimal early, final BigDecimal notice_only, final BigDecimal nonotice_only) {
            _lesson = lesson;
            _attend = attend;
            _sick = sick;
            _late = late;
            _early = early;
            _notice_only = notice_only;
            _nonotice_only = nonotice_only;
        }

        public String toString() {
            return "SubclassAttendance(" + _sick == null ? null : sishaGonyu(_sick.toString())  + "/" + _lesson + ")";
        }

        private static void load(final DB2UDB db2,
                final Param param,
                final Map schregnoMap) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            for (final Iterator it3 = schregnoMap.keySet().iterator(); it3.hasNext();) {
                final String schregno = (String) it3.next();
                final Map gradeMap = (Map) schregnoMap.get(schregno);

                for (final Iterator it4 = gradeMap.keySet().iterator(); it4.hasNext();) {
                    final String grade = (String) it4.next();
                    final Student student = (Student) gradeMap.get(grade);
                    final Semester studentSeme = student.loadSemester(db2);

                    param._attendParamMap.put("schregno", "?");
                    param._attendParamMap.put("grade", student._oldGrade);

                    final String sql = AttendAccumulate.getAttendSubclassSql(
                        student._oldYear,
                        studentSeme._semester,
                        studentSeme._sdate,
                        studentSeme._edate,
                        param._attendParamMap
                    );
                    log.debug("attendSubclassSql = " + sql);

                    try {
                        ps = db2.prepareStatement(sql);
                        ps.setString(1, student._schregno);
                        rs = ps.executeQuery();

                        while (rs.next()) {
                            if (!SEMEALL.equals(rs.getString("SEMESTER"))) {
                                continue;
                            }
                            final String ySubclass = student._oldYear + "-" + rs.getString("SUBCLASSCD");

                            final SubclassMst mst = (SubclassMst) param._subclassMstMap.get(ySubclass);
                            if (null == mst) {
                                log.warn("no subclass : " + ySubclass);
                                continue;
                            }
                            final int iclasscd = Integer.parseInt(ySubclass.substring(0, 2));
                            if (Integer.parseInt(KNJDefineSchool.subject_D) <= iclasscd && rs.getBigDecimal("MLESSON").intValue() > 0) {

                                final BigDecimal lesson = rs.getBigDecimal("MLESSON");
                                final BigDecimal rawSick = rs.getBigDecimal("SICK1");
                                final BigDecimal sick = rs.getBigDecimal("SICK2");
                                final BigDecimal rawReplacedSick = rs.getBigDecimal("RAW_REPLACED_SICK");
                                final BigDecimal replacedSick = rs.getBigDecimal("REPLACED_SICK");
                                final BigDecimal late = rs.getBigDecimal("LATE");
                                final BigDecimal early = rs.getBigDecimal("EARLY");

                                final BigDecimal sick1 = mst.isSaki() ? rawReplacedSick : rawSick;
                                final BigDecimal attend = lesson.subtract(null == sick1 ? BigDecimal.valueOf(0) : sick1);
                                final BigDecimal sick2 = mst.isSaki() ? replacedSick : sick;

                                final BigDecimal absenceHigh = rs.getBigDecimal("ABSENCE_HIGH");

                                final BigDecimal notice_only = rs.getBigDecimal("NOTICE_ONLY");
                                final BigDecimal nonotice_only = rs.getBigDecimal("NONOTICE_ONLY");

                                final SubclassAttendance subclassAttendance = new SubclassAttendance(lesson, attend, sick2, late, early, notice_only, nonotice_only);

                                //欠課時数上限
                                final Double absent = Double.valueOf(mst.isSaki() ? rs.getString("REPLACED_SICK"): rs.getString("SICK2"));
                                subclassAttendance._isOver = subclassAttendance.judgeOver(absent, absenceHigh);

                                student._attendSubClassMap.put(ySubclass, subclassAttendance);
                            }

                        }

                        DbUtils.closeQuietly(rs);
                    } catch (Exception e) {
                        log.fatal("exception!", e);
                    } finally {
                        DbUtils.closeQuietly(ps);
                        db2.commit();
                    }
                }
            }
        }
        /**
         * 欠課時数超過ならTrueを戻します。
         * @param absent 欠課時数
         * @param absenceHigh 超過対象欠課時数（CREDIT_MST）
         * @return
         */
        private boolean judgeOver(final Double absent, final BigDecimal absenceHigh) {
            if (null == absent || null == absenceHigh) {
                return false;
            }
            if (0.1 > absent.floatValue() || 0.0 == absenceHigh.doubleValue()) {
                return false;
            }
            if (absenceHigh.doubleValue() < absent.doubleValue()) {
                return true;
            }
            return false;
        }
    }


    private static class Semester {
        final String _semester;
        final String _semestername;
        final String _sdate;
        final String _edate;
        public Semester(final String semester, final String semestername, final String sdate, final String edate) {
            _semester     = semester;
            _semestername = semestername;
            _sdate        = sdate;
            _edate        = edate;
        }

        public int compareTo(final Object o) {
            if (!(o instanceof Semester)) {
                return 0;
            }
            Semester s = (Semester) o;
            return _semester.compareTo(s._semester);
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
        final String _nendo;
        final String _schoolKind;

        private String _certifSchoolSchoolName;

        /** 端数計算共通メソッド引数 */
        private final Map _attendParamMap;

        private final Map _semesterDetailMap;
        private Map<String, SubclassMst> _subclassMstMap;
        private List _d026List = new ArrayList();

        private boolean _isNoPrintMoto;
        private boolean _isPrintSakiKamoku;

        private Map _documentMstMap;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {

            _categorySelected = request.getParameterValues("CLASS_SELECTED");
            _grade = request.getParameter("GRADE"); //現在の学年
            _semester = request.getParameter("TERM"); //対象学期

            _prgid = request.getParameter("PRGID");
            _loginSemester = request.getParameter("HID_SEMESTER");
            _loginYear = request.getParameter("HID_YEAR");
            _date = request.getParameter("PRINT_DATE").replace('/', '-');

            _nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_loginYear)) + "年度";
            loadNameMstD026(db2);
            loadNameMstD016(db2);
            setPrintSakiKamoku(db2);
            _schoolKind = getSchoolKind(db2);

            setCertifSchoolDat(db2);

            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
            _attendParamMap.put("useTestCountflg", "TESTITEM_MST_COUNTFLG_NEW_SDIV");
            _attendParamMap.put("useCurriculumcd", "1");

            _semesterDetailMap = new HashMap();
            setSubclassMst(db2);

            _documentMstMap = getDocumentMst(db2);
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
                sql += " YDAT.YEAR || '-' || T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS Y_SUBCLASSCD, ";
                sql += " T2.CLASSABBV, VALUE(T2.CLASSORDERNAME2, T2.CLASSNAME) AS CLASSNAME, T1.SUBCLASSABBV, VALUE(T1.SUBCLASSORDERNAME2, T1.SUBCLASSNAME) AS SUBCLASSNAME, ";
                sql += " COMB1.CALCULATE_CREDIT_FLG, ";
                sql += " VALUE(T2.SHOWORDER3, 999) AS CLASS_SHOWORDER3, ";
                sql += " VALUE(T1.SHOWORDER3, 999) AS SUBCLASS_SHOWORDER3, ";
                sql += " ATT1.COMBINED_CLASSCD || '-' || ATT1.COMBINED_SCHOOL_KIND || '-' || ATT1.COMBINED_CURRICULUM_CD || '-' || ATT1.COMBINED_SUBCLASSCD AS COMBINED_SUBCLASSCD, ";
                sql += " COMB1.ATTEND_CLASSCD || '-' || COMB1.ATTEND_SCHOOL_KIND || '-' || COMB1.ATTEND_CURRICULUM_CD || '-' || COMB1.ATTEND_SUBCLASSCD AS ATTEND_SUBCLASSCD ";
                sql += " FROM SUBCLASS_MST T1 ";
                sql += " INNER JOIN SUBCLASS_YDAT YDAT ";
                sql += "         ON YDAT.YEAR          <= '" + _loginYear + "' ";
                sql += "        AND YDAT.CLASSCD       = T1.CLASSCD ";
                sql += "        AND YDAT.SCHOOL_KIND   = T1.SCHOOL_KIND ";
                sql += "        AND YDAT.CURRICULUM_CD = T1.CURRICULUM_CD ";
                sql += "        AND YDAT.SUBCLASSCD    = T1.SUBCLASSCD ";
                sql += " LEFT JOIN CLASS_MST T2 ON T2.CLASSCD = T1.CLASSCD AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
                sql += " LEFT JOIN SUBCLASS_REPLACE_COMBINED_DAT ATT1 ON ATT1.YEAR = YDAT.YEAR AND ATT1.ATTEND_CLASSCD = T1.CLASSCD AND ATT1.ATTEND_SCHOOL_KIND = T1.SCHOOL_KIND AND ATT1.ATTEND_CURRICULUM_CD = T1.CURRICULUM_CD AND ATT1.ATTEND_SUBCLASSCD = T1.SUBCLASSCD ";
                sql += " LEFT JOIN SUBCLASS_REPLACE_COMBINED_DAT COMB1 ON COMB1.YEAR = YDAT.YEAR AND COMB1.COMBINED_CLASSCD = T1.CLASSCD AND COMB1.COMBINED_SCHOOL_KIND = T1.SCHOOL_KIND AND COMB1.COMBINED_CURRICULUM_CD = T1.CURRICULUM_CD AND COMB1.COMBINED_SUBCLASSCD = T1.SUBCLASSCD ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    if (!_subclassMstMap.containsKey(rs.getString("Y_SUBCLASSCD"))) {
                        final SubclassMst mst = new SubclassMst(rs.getString("SPECIALDIV"), rs.getString("CLASSCD"), rs.getString("Y_SUBCLASSCD"), rs.getString("CLASSABBV"), rs.getString("CLASSNAME"), rs.getString("SUBCLASSABBV"), rs.getString("SUBCLASSNAME"), new Integer(rs.getInt("CLASS_SHOWORDER3")), new Integer(rs.getInt("SUBCLASS_SHOWORDER3")), rs.getString("CALCULATE_CREDIT_FLG"));
                        _subclassMstMap.put(rs.getString("Y_SUBCLASSCD"), mst);
                    }
                    final SubclassMst combined = _subclassMstMap.get(rs.getString("COMBINED_SUBCLASSCD"));
                    if (null != combined) {
                        final SubclassMst mst = _subclassMstMap.get(rs.getString("Y_SUBCLASSCD"));
                        mst._combined = combined;
                    }
                    final SubclassMst attend = _subclassMstMap.get(rs.getString("ATTEND_SUBCLASSCD"));
                    if (null != attend) {
                        final SubclassMst mst = _subclassMstMap.get(rs.getString("Y_SUBCLASSCD"));
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

        private void setCertifSchoolDat(final DB2UDB db2) {
            final String certifKindcd = "J".equals(_schoolKind) ? "103" : "104";
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT SCHOOL_NAME, JOB_NAME, PRINCIPAL_NAME, REMARK2, REMARK4, REMARK5 FROM CERTIF_SCHOOL_DAT ");
            sql.append(" WHERE YEAR = '" + _loginYear + "' AND CERTIF_KINDCD = '"+ certifKindcd +"' ");
            log.debug("certif_school_dat sql = " + sql.toString());

            final Map row = KnjDbUtils.firstRow(KnjDbUtils.query(db2, sql.toString()));

            _certifSchoolSchoolName = StringUtils.defaultString(KnjDbUtils.getString(row, "SCHOOL_NAME"));
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

        private Map getNameMst(final DB2UDB db2, final String namecd1) {
            Map rtnMap = new HashMap();
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT * ");
            stb.append("   FROM NAME_MST ");
            stb.append("  WHERE NAMECD1 = '"+ namecd1 +"' ");

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final Map map = new HashMap();
                    map.put("NAME1", StringUtils.defaultString(rs.getString("NAME1")));
                    map.put("ABBV1", StringUtils.defaultString(rs.getString("ABBV1")));
                    map.put("NAMESPARE1", StringUtils.defaultString(rs.getString("NAMESPARE1")));
                    map.put("NAMESPARE2", StringUtils.defaultString(rs.getString("NAMESPARE2")));
                    map.put("NAMESPARE3", StringUtils.defaultString(rs.getString("NAMESPARE3")));

                    final String key = StringUtils.defaultString(rs.getString("NAMECD2"));
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

        private Map getDocumentMst(final DB2UDB db2) {
            Map rtnMap = new HashMap();
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT * ");
            stb.append("   FROM DOCUMENT_MST ");

            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final Map map = new HashMap();
                    map.put("TITLE", StringUtils.defaultString(rs.getString("TITLE")));
                    map.put("CERTIF_NO", StringUtils.defaultString(rs.getString("CERTIF_NO")));
                    map.put("TEXT", StringUtils.defaultString(rs.getString("TEXT")));
                    map.put("FOOTNOTE", StringUtils.defaultString(rs.getString("FOOTNOTE")));

                    final String key = StringUtils.defaultString(rs.getString("DOCUMENTCD"));
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
