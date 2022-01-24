/**     プロジェクト/プロパティ/javaコード・スタイル/コード・テンプレート/パターン       ***/

/*
 * $Id: $
 *
 * 作成日: 2021/01/18
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
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

public class KNJD627D {

    private static final Log log = LogFactory.getLog(KNJD627D.class);

    private static final String SEMEALL = "9";
    private static final String SELECT_CLASSCD_UNDER = "90";

    private static final String SDIV9990008 = "9990008"; //学年末評価

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

            if (student._printSubclassMap.size() > 0) {

                //欠課
                SubclassAttendance.load(db2, _param, student);
                setSaishiken(student);

                //通知票
                if (student._printSubclassMap.size() > 0) {
                    printSvfMain(db2, svf, student);
                }
            }
        }
    }

    //基準点未満の科目で欠課実数超過があれば再試験対象外
    private void setSaishiken(final Student student) {
        for (Iterator ite = student._printSubclassMap.keySet().iterator(); ite.hasNext();) {
            final String key = (String) ite.next();
            if (student._attendSubClassMap.containsKey(key)) {
                final SubclassAttendance subAte = (SubclassAttendance) student._attendSubClassMap.get(key);
                if (subAte._isOver) {
                    ite.remove();
                }
            }
        }
    }

    private String add(final String num1, final String num2) {
        if (!NumberUtils.isDigits(num1)) { return num2; }
        if (!NumberUtils.isDigits(num2)) { return num1; }
        return String.valueOf(Integer.parseInt(num1) + Integer.parseInt(num2));
    }

    private void printSvfMain(final DB2UDB db2, final Vrw32alp svf, final Student student) {

        //明細部
        final List<SubclassMst> subclassListAll = subclassListRemoveD026();
        Collections.sort(subclassListAll);

        //印字する教科の設定
        for (Iterator itSubclass = subclassListAll.iterator(); itSubclass.hasNext();) {
            final SubclassMst subclassMst = (SubclassMst) itSubclass.next();
            final String subclassCd = subclassMst._subclasscd;

            final boolean isPrint = student._printSubclassMap.containsKey(subclassCd);
            if (!isPrint) {
                itSubclass.remove();
            }
        }

        final List<List<SubclassMst>> subclassListList = getPageList(subclassListAll, 7);

        for (int i = 0; i < subclassListList.size(); i++) {
            final List<SubclassMst> subclassList = subclassListList.get(i);

            final String form = "KNJD627D.frm";
            svf.VrSetForm(form , 1);

            //ページ
            final String page = "(" + String.valueOf(i + 1) + "/" + subclassListList.size() + ")";
            svf.VrsOut("PAGE1", page);
            svf.VrsOut("PAGE2", page);

            //明細部以外を印字
            printTitle(db2, svf, student);

            //■成績一覧
            int line = 1;
            for (Iterator itSubclass = subclassList.iterator(); itSubclass.hasNext();) {
                final SubclassMst subclassMst = (SubclassMst) itSubclass.next();
                final String subclassCd = subclassMst._subclasscd;

                if (student._printSubclassMap.containsKey(subclassCd)) {
                    final ScoreData scoreData = (ScoreData) student._printSubclassMap.get(subclassCd);

                    //科目名
                    final int length = KNJ_EditEdit.getMS932ByteLength(subclassMst._subclassname);
                    final String field = length > 90 ? "6" : length > 80 ? "5" : length > 70 ? "4" : length > 60 ? "3" : length > 50 ? "2" : "1";
                    svf.VrsOutn("SUBCLASSNAME1_" + field, line, subclassMst._subclassname);
                    svf.VrsOutn("SUBCLASSNAME2_" + field, line, subclassMst._subclassname);

                    //得点
                    final String score = scoreData.score(SDIV9990008);
                    svf.VrsOutn("SCORE1", line, score);
                    svf.VrsOutn("SCORE2", line, score);

                    //単位数
                    final String credit = _param.getCredits(student, subclassCd);
                    svf.VrsOutn("CREDIT1", line, credit);
                    svf.VrsOutn("CREDIT2", line, credit);

                    //受験希望チェック
                    svf.VrsOutn("SURU", line, "□");
                    svf.VrsOutn("SINAI", line, "□");

                    line++;
                    _hasData = true;
                }
            }

            svf.VrEndPage();
        }
    }

    /**
     * listを最大数ごとにグループ化したリストを得る
     * @param list
     * @param max 最大数
     * @return listを最大数ごとにグループ化したリスト
     */
    protected static <T> List<List<T>> getPageList(final List<T> list, final int max) {
        final List<List<T>> rtn = new ArrayList();
        List<T> current = null;
        for (final T o : list) {
            if (null == current || current.size() >= max) {
                current = new ArrayList<T>();
                rtn.add(current);
            }
            current.add(o);
        }
        return rtn;
    }

    private void printTitle(final DB2UDB db2, final Vrw32alp svf, final Student student) {
        //年組番氏名
        final String name = student._gradename + "　" + student._hrname + "　" + student._attendno + "　" + student._name + "　さん";
        final Integer keta = KNJ_EditEdit.getMS932ByteLength(name);
        String attribute = "Hensyu=2"; // 左寄せ
        attribute += ",UnderLine=(0,3,1)"; // 下線
        attribute += ",keta=" + String.valueOf(keta.intValue()); //印字サイズ
        svf.VrsOut("NAME", name);
        svf.VrAttribute("NAME", attribute);


        //出力日
        String[] date = _param._date.split("-", 0);
        if (date != null && date.length == 3) {
            date[1] = String.valueOf(Integer.parseInt(date[1]));
            date[2] = String.valueOf(Integer.parseInt(date[2]));
            svf.VrsOut("DATE", date[0] + "．" + date[1] + "．" + date[2]);
        }


        //学期名称
        String semestername = "";
        Map nameD039 = _param.getNameMst(db2, "D039");
        if (nameD039.size() > 0) {
            Map map = (Map) nameD039.get(_param._semester);
            if (map.size() > 0) semestername = (String)map.get("NAME1");
        }
        svf.VrsOut("SEMESTERNAME", semestername);

        //タイトル
        svf.VrsOut("TITLE1", "再試験通知書");

        //文言
        if (_param._documentMstMap.size() > 0) {
            if (_param._documentMstMap.size() > 0) {
                Map map = (Map) _param._documentMstMap.get("D4");
                if (map != null) {
                    VrsOutnRenban(svf, "REMARK", KNJ_EditEdit.get_token((String)map.get("TEXT"), 82, 7));
                }
            }
        }
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
                student._year = rs.getString("YEAR");
                student._schregno = rs.getString("SCHREGNO");
                student._name = rs.getString("NAME");
                student._schoolKind = rs.getString("SCHOOL_KIND");
                student._gradename = rs.getString("CURRENT_GRADE_NAME");
                student._hrname = rs.getString("CURRENT_HR_CLASS_NAME") + "組" ;
                student._trcd = StringUtils.defaultString(rs.getString("TR_CD1"));
                student._staffname = StringUtils.defaultString(rs.getString("STAFFNAME"));
                student._staffname2 = StringUtils.defaultString(rs.getString("STAFFNAME2"));
                student._attendno = NumberUtils.isDigits(rs.getString("CURRENT_ATTENDNO")) ? String.valueOf(Integer.parseInt(rs.getString("CURRENT_ATTENDNO"))) + "番" : rs.getString("CURRENT_ATTENDNO");
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
        //選択された年組の生徒
        stb.append("WITH SCHNO_A AS( ");
        stb.append("    SELECT DISTINCT ");
        stb.append("            T1.SCHREGNO, ");
        stb.append("            REGDG.GRADE_NAME2 AS CURRENT_GRADE_NAME, ");
        stb.append("            REGDH.HR_CLASS_NAME1 AS CURRENT_HR_CLASS_NAME, ");
        stb.append("            T1.ATTENDNO AS CURRENT_ATTENDNO, ");
        stb.append("            T1.GRADE AS CURRENT_GRADE, ");
        stb.append("            T1.HR_CLASS AS CURRENT_HR_CLASS ");
        stb.append("    FROM    SCHREG_REGD_DAT T1 ");
        stb.append("            INNER JOIN SEMESTER_MST T2 ");
        stb.append("                    ON T2.YEAR     = T1.YEAR ");
        stb.append("                   AND T2.SEMESTER = T1.SEMESTER ");
        stb.append("            LEFT JOIN SCHREG_REGD_GDAT REGDG ");
        stb.append("                   ON REGDG.YEAR  = T1.YEAR ");
        stb.append("                  AND REGDG.GRADE = T1.GRADE ");
        stb.append("            LEFT JOIN SCHREG_REGD_HDAT REGDH ");
        stb.append("                   ON REGDH.YEAR     = T1.YEAR ");
        stb.append("                  AND REGDH.SEMESTER = T1.SEMESTER ");
        stb.append("                  AND REGDH.GRADE    = T1.GRADE ");
        stb.append("                  AND REGDH.HR_CLASS = T1.HR_CLASS ");
        stb.append("    WHERE   T1.YEAR     = '" + _param._loginYear + "' ");
        stb.append("        AND T1.SEMESTER = '" + _param._loginSemester + "' ");
        stb.append("        AND T1.GRADE    = '" + _param._grade + "' ");
        stb.append("        AND T1.HR_CLASS IN " + SQLUtils.whereIn(true, _param._categorySelected));
        //対象生徒の過年度
        stb.append(" ) , SCHNO_HIST_A AS( ");
        stb.append("    SELECT DISTINCT ");
        stb.append("            REGD.YEAR, ");
        stb.append("            REGD.SCHREGNO, ");
        stb.append("            REGD.GRADE, ");
        stb.append("            GDAT.GRADE_CD, ");
        stb.append("            T2.CURRENT_GRADE_NAME, ");
        stb.append("            T2.CURRENT_HR_CLASS_NAME,");
        stb.append("            T2.CURRENT_ATTENDNO, ");
        stb.append("            T2.CURRENT_GRADE, ");
        stb.append("            T2.CURRENT_HR_CLASS ");
        stb.append("    FROM    SCHREG_REGD_DAT REGD ");
        stb.append("            INNER JOIN SCHREG_REGD_GDAT GDAT ");
        stb.append("                    ON GDAT.YEAR  = REGD.YEAR ");
        stb.append("                   AND GDAT.GRADE = REGD.GRADE ");
        stb.append("            INNER JOIN SCHNO_A T2 ");
        stb.append("                    ON T2.SCHREGNO = REGD.SCHREGNO ");
        stb.append("    WHERE   REGD.YEAR <= '" + _param._loginYear + "' ");
        stb.append(" ) , SCHNO_HIST AS( ");
        stb.append("    SELECT DISTINCT ");
        stb.append("            T1.* ");
        stb.append("    FROM    SCHNO_HIST_A T1 ");
        stb.append("    WHERE   T1.YEAR = (SELECT MAX(YEAR) FROM SCHNO_HIST_A T2 WHERE T2.GRADE_CD = T1.GRADE_CD AND T2.SCHREGNO = T1.SCHREGNO ) ");
        //再試験対象年度時の生徒
        stb.append(" ) , SCHNO_TARGET_GRADE AS( ");
        stb.append("    SELECT  T1.*, ");
        stb.append("            T2.CURRENT_GRADE_NAME, ");
        stb.append("            T2.CURRENT_HR_CLASS_NAME, ");
        stb.append("            T2.CURRENT_ATTENDNO, ");
        stb.append("            T2.CURRENT_GRADE, ");
        stb.append("            T2.CURRENT_HR_CLASS ");
        stb.append("    FROM    SCHREG_REGD_DAT T1 ");
        stb.append("            INNER JOIN SCHNO_HIST T2 ");
        stb.append("                    ON T2.YEAR     = T1.YEAR ");
        stb.append("                   AND T2.SCHREGNO = T1.SCHREGNO ");
        stb.append("                   AND T1.GRADE    = '" + _param._targetGrade + "' ");
        stb.append("    WHERE   T1.SEMESTER = (SELECT MAX(SEMESTER) FROM SCHREG_REGD_DAT T3 WHERE T3.YEAR = T2.YEAR AND T3.GRADE = T2.GRADE AND T3.SCHREGNO = T2.SCHREGNO) ");
        //基準点以下の点数が存在する生徒
        stb.append(" ) , SCHNO AS(");
        stb.append("    SELECT DISTINCT ");
        stb.append("            T1.* ");
        stb.append("    FROM    SCHNO_TARGET_GRADE T1 ");
        stb.append("            INNER JOIN RECORD_SCORE_DAT T2 ");
        stb.append("                    ON T2.YEAR       = T1.YEAR ");
        stb.append("                   AND T2.SCHREGNO   = T1.SCHREGNO ");
        stb.append("                   AND T2.SEMESTER || T2.TESTKINDCD || T2.TESTITEMCD || T2.SCORE_DIV = '"+ SDIV9990008 +"' ");
        stb.append("                   AND T2.SCORE      <= '"+ _param._borderScore +"' ");
        stb.append("            INNER JOIN CLASS_MST T3 ");
        stb.append("                    ON T3.CLASSCD = SUBSTR(T2.SUBCLASSCD,1,2) ");
        stb.append("                   AND T3.SCHOOL_KIND   = T2.SCHOOL_KIND ");
        stb.append("            INNER JOIN SUBCLASS_MST T4 ");
        stb.append("                    ON T4.SUBCLASSCD    = T2.SUBCLASSCD ");
        stb.append("                   AND T4.CLASSCD       = T2.CLASSCD ");
        stb.append("                   AND T4.SCHOOL_KIND   = T2.SCHOOL_KIND ");
        stb.append("                   AND T4.CURRICULUM_CD = T2.CURRICULUM_CD ");
        stb.append("    WHERE   T2.CLASSCD <= '" + SELECT_CLASSCD_UNDER + "' ");
        stb.append("        AND T2.SUBCLASSCD NOT LIKE '50%' ");
        stb.append(" ) ");
        //メイン表
        stb.append("     SELECT  REGD.YEAR ");
        stb.append("            ,REGD.SCHREGNO ");
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
        stb.append("            ,REGDH.HR_CLASS_NAME1 ");
        stb.append("            ,FISCALYEAR(BASE.ENT_DATE) AS ENT_YEAR ");
        stb.append("            ,GADDR.GUARD_ZIPCD ");
        stb.append("            ,GADDR.GUARD_ADDR1 ");
        stb.append("            ,GADDR.GUARD_ADDR2 ");
        stb.append("            ,GUARDIAN.GUARD_NAME ");
        stb.append("            ,REGD.CURRENT_GRADE_NAME ");
        stb.append("            ,REGD.CURRENT_HR_CLASS_NAME ");
        stb.append("            ,REGD.CURRENT_ATTENDNO ");
        stb.append("            ,REGD.CURRENT_GRADE ");
        stb.append("            ,REGD.CURRENT_HR_CLASS ");
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
        stb.append("     LEFT JOIN STAFF_MST STF1 ");
        stb.append("                   ON STF1.STAFFCD = REGDH.TR_CD1  ");
        stb.append("     LEFT JOIN MAJOR_MST MAJOR ");
        stb.append("                   ON MAJOR.COURSECD = REGD.COURSECD ");
        stb.append("                  AND MAJOR.MAJORCD  = REGD.MAJORCD ");
        stb.append("     LEFT JOIN COURSECODE_MST COURSE ");
        stb.append("                   ON COURSE.COURSECODE = REGD.COURSECODE ");
        stb.append("     LEFT JOIN STAFF_MST STF2 ");
        stb.append("                   ON STF2.STAFFCD = REGDH.TR_CD2 ");
        stb.append("     LEFT JOIN GUARDIAN_DAT GUARDIAN ");
        stb.append("                   ON GUARDIAN.SCHREGNO = REGD.SCHREGNO  ");
        stb.append("     LEFT JOIN (SELECT SCHREGNO, MAX(ISSUEDATE) AS ISSUEDATE FROM GUARDIAN_ADDRESS_DAT GROUP BY SCHREGNO) L_GADDR ");
        stb.append("                   ON L_GADDR.SCHREGNO = GUARDIAN.SCHREGNO  ");
        stb.append("     LEFT JOIN GUARDIAN_ADDRESS_DAT GADDR ");
        stb.append("                   ON GADDR.SCHREGNO = L_GADDR.SCHREGNO ");
        stb.append("                  AND GADDR.ISSUEDATE = L_GADDR.ISSUEDATE");
        stb.append("     ORDER BY ");
        stb.append("         REGD.CURRENT_GRADE, ");
        stb.append("         REGD.CURRENT_HR_CLASS, ");
        stb.append("         REGD.CURRENT_ATTENDNO ");
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
        String _year;
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
        final Map<String, SubclassAttendance> _attendSubClassMap = new HashMap();
        final Map _printSubclassMap = new TreeMap();

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
//                    final String score = null != rs.getString("VALUE_DI") ? StringUtils.defaultString(rs.getString("VALUE_DI")) : StringUtils.defaultString(rs.getString("SCORE"));
                    final String score = StringUtils.defaultString(rs.getString("SCORE"));
                    scoreData._scoreMap.put(testcd, score);
                    scoreData._avgMap.put(testcd, StringUtils.defaultString(rs.getString("AVG")));
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
            stb.append("         T2.YEAR     = '" + _year + "'  ");
            stb.append("     AND T2.GRADE    = '" + _grade + "'  ");
            stb.append("     AND T2.HR_CLASS = '" + _hrClass + "'  ");
            stb.append("     AND T2.SCHREGNO = '" + _schregno + "'  ");
            stb.append("     AND T2.SEMESTER = (SELECT ");
            stb.append("                          MAX(SEMESTER) ");
            stb.append("                        FROM ");
            stb.append("                          SCHREG_REGD_DAT W2 ");
            stb.append("                        WHERE ");
            stb.append("                          W2.YEAR = '" + _year + "'  ");
            stb.append("                          AND W2.SEMESTER <= '" + _param._semester + "'  ");
            stb.append("                          AND W2.SCHREGNO = T2.SCHREGNO ");
            stb.append("                     ) ");
            //成績明細データの表
            stb.append(" ) ,RECORD00 AS( ");
            stb.append("     SELECT DISTINCT ");
            stb.append("         T1.YEAR, T1.SEMESTER, T1.TESTKINDCD, T1.TESTITEMCD, T1.SCORE_DIV, T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, T1.SCHREGNO ");
            stb.append("     FROM RECORD_SCORE_DAT T1 ");
            stb.append("     INNER JOIN (SELECT SCHREGNO FROM SCHNO GROUP BY SCHREGNO ) T2 ");
            stb.append("             ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("     WHERE ");
            stb.append("         T1.YEAR = '" + _year + "'  ");
            stb.append("         AND (" + divStr + ") ");
            stb.append("         AND T1.SUBCLASSCD NOT LIKE '50%' ");
            stb.append("         AND T1.SUBCLASSCD NOT IN ('" + ALL3 + "', '" + ALL5 + "') ");
            stb.append("         AND T1.CLASSCD <= '" + SELECT_CLASSCD_UNDER + "' ");
            stb.append(" ) ,RECORD0 AS( ");
            stb.append("     SELECT ");
            stb.append("              T1.YEAR, T1.SEMESTER, T1.TESTKINDCD, T1.TESTITEMCD, T1.SCORE_DIV, T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, T1.SCHREGNO ");
            stb.append("            , L1.CREDITS ");
            stb.append("            , CASE WHEN L4.SCORE > L3.SCORE THEN  L4.SCORE ELSE L3.SCORE  END AS SCORE "); //追指導の成績が良ければ学年末として扱う
            stb.append("            , L3.VALUE_DI ");
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
            stb.append("     LEFT JOIN RECORD_SCORE_DAT L3 ");
            stb.append("            ON L3.YEAR          = T1.YEAR ");
            stb.append("           AND L3.SEMESTER      = T1.SEMESTER ");
            stb.append("           AND L3.TESTKINDCD    = T1.TESTKINDCD ");
            stb.append("           AND L3.TESTITEMCD    = T1.TESTITEMCD ");
            stb.append("           AND L3.SCORE_DIV     = T1.SCORE_DIV ");
            stb.append("           AND L3.CLASSCD       = T1.CLASSCD ");
            stb.append("           AND L3.SCHOOL_KIND   = T1.SCHOOL_KIND ");
            stb.append("           AND L3.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("           AND L3.SUBCLASSCD    = T1.SUBCLASSCD ");
            stb.append("           AND L3.SCHREGNO      = T1.SCHREGNO ");
            stb.append("     LEFT JOIN RECORD_AVERAGE_SDIV_DAT T_AVG1 ON T_AVG1.YEAR = T1.YEAR AND T_AVG1.SEMESTER = T1.SEMESTER AND T_AVG1.TESTKINDCD = T1.TESTKINDCD AND T_AVG1.TESTITEMCD = T1.TESTITEMCD AND T_AVG1.SCORE_DIV = T1.SCORE_DIV AND T_AVG1.GRADE = '" + _grade + "' AND T_AVG1.CLASSCD = T1.CLASSCD AND T_AVG1.SCHOOL_KIND = T1.SCHOOL_KIND AND T_AVG1.CURRICULUM_CD = T1.CURRICULUM_CD AND T_AVG1.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("           AND T_AVG1.AVG_DIV    = '1' "); //学年
            stb.append("     LEFT JOIN RECORD_AVERAGE_SDIV_DAT T_AVG2 ON T_AVG2.YEAR = T1.YEAR AND T_AVG2.SEMESTER = T1.SEMESTER AND T_AVG2.TESTKINDCD = T1.TESTKINDCD AND T_AVG2.TESTITEMCD = T1.TESTITEMCD AND T_AVG2.SCORE_DIV = T1.SCORE_DIV AND T_AVG2.GRADE = '" + _grade + "' AND T_AVG2.CLASSCD = T1.CLASSCD AND T_AVG2.SCHOOL_KIND = T1.SCHOOL_KIND AND T_AVG2.CURRICULUM_CD = T1.CURRICULUM_CD AND T_AVG2.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("           AND T_AVG2.AVG_DIV    = '2' "); //クラス
            stb.append("           AND T_AVG2.HR_CLASS   = T2.HR_CLASS ");
            stb.append("     LEFT JOIN RECORD_AVERAGE_SDIV_DAT T_AVG3 ON T_AVG3.YEAR = T1.YEAR AND T_AVG3.SEMESTER = T1.SEMESTER AND T_AVG3.TESTKINDCD = T1.TESTKINDCD AND T_AVG3.TESTITEMCD = T1.TESTITEMCD AND T_AVG3.SCORE_DIV = T1.SCORE_DIV AND T_AVG3.GRADE = '" + _grade + "' AND T_AVG3.CLASSCD = T1.CLASSCD AND T_AVG3.SCHOOL_KIND = T1.SCHOOL_KIND AND T_AVG3.CURRICULUM_CD = T1.CURRICULUM_CD AND T_AVG3.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("           AND T_AVG3.AVG_DIV    = '3' ");
            stb.append("           AND T_AVG3.COURSECD   = T2.COURSECD ");
            stb.append("           AND T_AVG3.MAJORCD    = T2.MAJORCD ");
            stb.append("           AND T_AVG3.COURSECODE = T2.COURSECODE "); //コース
            stb.append("     LEFT JOIN RECORD_AVERAGE_SDIV_DAT T_AVG4 ON T_AVG4.YEAR = T1.YEAR AND T_AVG4.SEMESTER = T1.SEMESTER AND T_AVG4.TESTKINDCD = T1.TESTKINDCD AND T_AVG4.TESTITEMCD = T1.TESTITEMCD AND T_AVG4.SCORE_DIV = T1.SCORE_DIV AND T_AVG4.GRADE = '" + _grade + "' AND T_AVG4.CLASSCD = T1.CLASSCD AND T_AVG4.SCHOOL_KIND = T1.SCHOOL_KIND AND T_AVG4.CURRICULUM_CD = T1.CURRICULUM_CD AND T_AVG4.SUBCLASSCD = T1.SUBCLASSCD ");
            stb.append("           AND T_AVG4.AVG_DIV    = '4' ");
            stb.append("           AND T_AVG4.COURSECD   = T2.COURSECD ");
            stb.append("           AND T_AVG4.MAJORCD    = T2.MAJORCD ");
            stb.append("           AND T_AVG4.COURSECODE = '0000' "); //専攻
            stb.append("     LEFT JOIN RECORD_SLUMP_SDIV_DAT  L4 ");
            stb.append("            ON L4.YEAR          = T1.YEAR ");
            stb.append("           AND L4.SEMESTER      = T1.SEMESTER ");
            stb.append("           AND L4.TESTKINDCD    = T1.TESTKINDCD ");
            stb.append("           AND L4.TESTITEMCD    = T1.TESTITEMCD ");
            stb.append("           AND L4.SCORE_DIV     = T1.SCORE_DIV ");
            stb.append("           AND L4.CLASSCD       = T1.CLASSCD ");
            stb.append("           AND L4.SCHOOL_KIND   = T1.SCHOOL_KIND ");
            stb.append("           AND L4.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("           AND L4.SUBCLASSCD    = T1.SUBCLASSCD ");
            stb.append("           AND L4.SCHREGNO      = T1.SCHREGNO ");
            stb.append(" ) ,RECORD AS( ");
            stb.append("     SELECT ");
            stb.append("       T3.CLASSNAME, ");
            stb.append("       T4.SUBCLASSNAME, ");
            stb.append("       T1.* ");
            stb.append("     FROM RECORD0 T1 ");
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
            stb.append("     WHERE T1.SCORE <= '"+ _param._borderScore +"' "); //基準点以下の科目のみ取得
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

        /**
         * 年度の開始日を取得する
         */
        private Semester loadSemester(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            Semester retSeme = null;
            final String sql = "select"
                    + "   SEMESTER,"
                    + "   SEMESTERNAME,"
                    + "   SDATE,"
                    + "   EDATE"
                    + " from"
                    + "   V_SEMESTER_GRADE_MST"
                    + " where"
                    + "   YEAR='" + _year + "'"
                    + "   AND SEMESTER='" + SEMEALL + "'"
                    + "   AND GRADE='" + _grade + "'"
                    + " order by SEMESTER"
                ;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                if (rs.next()) {
                    retSeme = new Semester(rs.getString("SEMESTER"), rs.getString("SEMESTERNAME"), rs.getString("SDATE"), rs.getString("EDATE"));
                }
            } catch (final Exception ex) {
                log.error("テスト項目のロードでエラー", ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retSeme;
        }

    }

    private static class SubclassAttendance {
        final BigDecimal _lesson;
        final BigDecimal _attend;
        final BigDecimal _sick;
        final BigDecimal _late;
        final BigDecimal _early;
        boolean _isOver;

        public SubclassAttendance(final BigDecimal lesson, final BigDecimal attend, final BigDecimal sick, final BigDecimal late, final BigDecimal early) {
            _lesson = lesson;
            _attend = attend;
            _sick = sick;
            _late = late;
            _early = early;
        }

        public String toString() {
            return "SubclassAttendance(" + _sick == null ? null : sishaGonyu(_sick.toString())  + "/" + _lesson + ")";
        }

        private static void load(final DB2UDB db2,
                final Param param,
                final Student student) {

            PreparedStatement ps = null;
            ResultSet rs = null;
            final Semester semester = student.loadSemester(db2); //対象学年の期間

            param._attendParamMap.put("schregno", "?");
            final String sql = AttendAccumulate.getAttendSubclassSql(
                    student._year,
                    SEMEALL,
                    semester._sdate,
                    semester._edate,
                    param._attendParamMap);
            log.debug("attendSubclassSql = " + sql);
            try {

                ps = db2.prepareStatement(sql);
                ps.setString(1, student._schregno);
                rs = ps.executeQuery();

                while (rs.next()) {
                    if (!SEMEALL.equals(rs.getString("SEMESTER"))) {
                        continue;
                    }
                    final String subclasscd = rs.getString("SUBCLASSCD");

                    final SubclassMst mst = (SubclassMst) param._subclassMstMap.get(subclasscd);
                    if (null == mst) {
                        log.warn("no subclass : " + subclasscd);
                        continue;
                    }
                    final int iclasscd = Integer.parseInt(subclasscd.substring(0, 2));
                    if (Integer.parseInt(KNJDefineSchool.subject_D) <= iclasscd
                            && rs.getBigDecimal("MLESSON").intValue() > 0) {

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

                        final SubclassAttendance subclassAttendance = new SubclassAttendance(lesson, attend, sick2,
                                late, early);

                        //欠課時数上限
                        final Double absent = Double
                                .valueOf(mst.isSaki() ? rs.getString("REPLACED_SICK") : rs.getString("SICK2"));
                        subclassAttendance._isOver = subclassAttendance.judgeOver(absent, absenceHigh);

                        if (!student._attendSubClassMap.containsKey(subclasscd)) {
                            student._attendSubClassMap.put(subclasscd, subclassAttendance);
                        }
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
            _semester = semester;
            _semestername = semestername;
            _sdate = sdate;
            _edate = edate;
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


    private static class DateRange {
        final String _key;
        final String _name;
        final String _sdate;
        final String _edate;
        public DateRange(final String key, final String name, final String sdate, final String edate) {
            _key = key;
            _name = name;
            _sdate = sdate;
            _edate = edate;
        }
        public String toString() {
            return "DateRange(" + _key + ", " + _sdate + ", " + _edate + ")";
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
        final String _targetGrade;
        final String _semester;
        final String _borderScore;

        final String _prgid;
        final String _loginSemester;
        final String _loginYear;
        final String _date;
        final String _nendo;
        final String _schoolKind;
        final String _schoolKindName;
        final Map _stampMap;

        /** 端数計算共通メソッド引数 */
        private final Map _attendParamMap;

        private String _certifSchoolSchoolName;
        private String _certifSchoolJobName;
        private String _certifSchoolPrincipalName;

        private Map<String, SubclassMst> _subclassMstMap;
        private Map<String, Map<String, String>> _creditMstMap;
        private List _d026List = new ArrayList();

        final String _documentroot;
        final String _imagepath;
        final String _schoolLogoImagePath;
        final String _backSlashImagePath;
        final String _whiteSpaceImagePath;
        final String _schoolStampPath;

        private boolean _isNoPrintMoto;
        private boolean _isPrintSakiKamoku;

        private boolean _isOutputDebug;

        private Map _documentMstMap;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {

            _categorySelected = request.getParameterValues("CLASS_SELECTED");
            _grade = request.getParameter("GRADE"); //現在の学年
            _targetGrade = request.getParameter("TARGET_GRADE"); //再試験対象年度
            _semester = request.getParameter("TERM"); //対象学期
            _borderScore = request.getParameter("BORDER_SCORE");

            _prgid = request.getParameter("PRGID");
            _loginSemester = request.getParameter("HID_SEMESTER");
            _loginYear = request.getParameter("HID_YEAR");
            _date = request.getParameter("LOGIN_DATE").replace('/', '-');


            _nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_loginYear)) + "年度";
            loadNameMstD026(db2);
            loadNameMstD016(db2);
            setPrintSakiKamoku(db2);
            _schoolKind = getSchoolKind(db2);
            _schoolKindName = getSchoolKindName(db2);
            _stampMap = getStampNoMap(db2);

            setCertifSchoolDat(db2);

            setSubclassMst(db2);
            setCreditMst(db2);

            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
            _attendParamMap.put("grade", _targetGrade);
            _attendParamMap.put("useTestCountflg", "TESTITEM_MST_COUNTFLG_NEW_SDIV");
            _attendParamMap.put("useCurriculumcd", "1");

            _documentroot = request.getParameter("DOCUMENTROOT");
            final String sqlControlMst = " SELECT IMAGEPATH FROM CONTROL_MST WHERE CTRL_NO = '01' ";
            _imagepath = KnjDbUtils.getOne(KnjDbUtils.query(db2, sqlControlMst));
            _schoolLogoImagePath = getImageFilePath("SCHOOLLOGO.jpg");
            _backSlashImagePath = getImageFilePath("slash_bs.jpg");
            _whiteSpaceImagePath = getImageFilePath("whitespace.png");
            _schoolStampPath = getImageFilePath("SCHOOLSTAMP.bmp");

            _isOutputDebug = "1".equals(getDbPrginfoProperties(db2, "outputDebug"));

            _documentMstMap = getDocumentMst(db2);
        }

        private static String getDbPrginfoProperties(final DB2UDB db2, final String propName) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJD187N' AND NAME = '" + propName + "' "));
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

        private String getCredits(final Student student, final String subclasscd) {
            final String regdKey = student._coursecd + student._majorcd + student._grade + student._coursecode;
            final Map<String, String> subclasscdCreditMap = _creditMstMap.get(regdKey);
            if (null == subclasscdCreditMap) {
                return null;
            }
            final String credits = subclasscdCreditMap.get(subclasscd);
            if (!subclasscdCreditMap.containsKey(subclasscd)) {
                log.info(" no credit_mst : " + subclasscd);
            }
            return credits;
        }

        private void setCreditMst(
                final DB2UDB db2
        ) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            _creditMstMap = new HashMap();
            try {
                String sql = "";
                sql += " SELECT ";
                sql += " T1.COURSECD || T1.MAJORCD || T1.GRADE || T1.COURSECODE AS REGD_KEY, ";
                sql += " T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ";
                sql += " T1.CREDITS ";
                sql += " FROM CREDIT_MST T1 ";
                sql += " WHERE YEAR = '" + _loginYear + "' ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String regdKey = rs.getString("REGD_KEY");
                    if (!_creditMstMap.containsKey(regdKey)) {
                        _creditMstMap.put(regdKey, new TreeMap());
                    }
                    _creditMstMap.get(regdKey).put(rs.getString("SUBCLASSCD"), StringUtils.defaultString(rs.getString("CREDITS"), "0"));
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
