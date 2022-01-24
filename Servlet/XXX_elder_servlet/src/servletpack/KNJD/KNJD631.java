// kanji=漢字
/*
 * $Id: 40ac5f0505f5fb91c2bccb688b2b021e64a2920e $
 *
 * 作成日: 2007/06/22
 * 作成者: nakamoto
 *
 * Copyright(C) 2004-2007 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJC.KNJDivideAttendDate;
import servletpack.KNJZ.detail.KNJDefineCode;
import servletpack.KNJZ.detail.KNJEditString;
import servletpack.KNJZ.detail.KNJObjectAbs;
import servletpack.KNJZ.detail.KNJSchoolMst;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

import jp.co.alp.kenja.common.dao.SQLUtils;

/**
 * 通知票
 * @author nakamoto
 * @version $Id: 40ac5f0505f5fb91c2bccb688b2b021e64a2920e $
 */
public class KNJD631 {

    private static final Log log = LogFactory.getLog(KNJD631.class);

    /** 中学の表紙。所見および通知票 */
    public String FORM_FILE1 = "KNJD631_3.frm";

    /** 高校の表紙 */
    public String FORM_FILE5 = "KNJD631_1.frm";

    /** 所見の裏 */
    public String FORM_FILE2 = "KNJD631_4.frm";

    // ============================================

    /** 通知票。中学、新カリキュラム */
    public String FORM_FILE3 = "KNJD631_5.frm";

    /** 通知票。中学、移行カリキュラム */
    public String FORM_FILE4 = "KNJD631_6.frm";

    /** 通知票。高校、移行＆旧カリキュラム */
    public String FORM_FILE6 = "KNJD631_2.frm";

    Param _param;
    private KNJObjectAbs _knjobj;            //編集用クラス
    private KNJSchoolMst _knjSchoolMst;

    /**
     * KNJD.classから呼ばれる処理。
     * @param request リクエスト
     * @param response レスポンス
     * @throws Exception I/O例外
     */
    public void svf_out(
        final HttpServletRequest request,
        final HttpServletResponse response
    ) throws Exception {
        dumpParam(request);
        _param = createParam(request);
        _knjobj = new KNJEditString();

        final Vrw32alp svf = new Vrw32alp();    //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                      //Databaseクラスを継承したクラス
        try {
            init(response, svf);

            // DB接続
            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            try {
                _knjSchoolMst = new KNJSchoolMst(db2, _param._year);
            } catch (SQLException e) {
                log.warn("学校マスタ取得でエラー", e);
            }
            _param.loadConstant(db2);
            _param.loadAttendSemesArgument(db2);

            // 印字メイン
            boolean hasData = false;   // 該当データ無しフラグ
            hasData = printMain(db2, svf);
            if (!hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }
        } finally {
            close(svf, db2);
        }
    }

    /** 印刷処理メイン */
    private boolean printMain(
            final DB2UDB db2,
            final Vrw32alp svf
    ) throws Exception {
        boolean rtnflg = false;

        final List students = createStudents(db2);
        log.debug("生徒数=" + students.size());

        final List forms = _param.getForms();
        log.debug("印刷するフォーム:" + forms);

        for (final Iterator it = students.iterator(); it.hasNext();) {
            final Student student = (Student) it.next();

            for (final Iterator itF = forms.iterator(); itF.hasNext();) {
                final Form form = (Form) itF.next();

                form.setForm(svf);
                if (form._hasAdvice) {
                    form.printAdvice(svf, student, _param, db2);
                } else {
                    form.printConstant(svf, student, _param);
                }
            }
            rtnflg = true;
        }

        return rtnflg;
    }

    private Param createParam(final HttpServletRequest request) {
        final String year = request.getParameter("YEAR");
        final String semester = request.getParameter("SEMESTER");
        final String gradeHrClass = request.getParameter("GRADE_HR_CLASS");
        final String printSide = request.getParameter("PRINT_SIDE");
        final String staffcd = request.getParameter("TR_CD1");
        final String documentRoot = request.getParameter("DOCUMENTROOT");
        final String imagePath = request.getParameter("IMAGEPATH");
        final String date = request.getParameter("DATE");
        final String[] schregno = request.getParameterValues("category_selected");
        final String useCurriculumcd = request.getParameter("useCurriculumcd");
        final String useVirus = request.getParameter("useVirus");
        final String useKoudome = request.getParameter("useKoudome");

        return new Param(
                year,
                semester,
                gradeHrClass,
                printSide,
                staffcd,
                documentRoot + '/' + imagePath + '/',
                date,
                schregno,
                useCurriculumcd,
                useVirus,
                useKoudome);
    }

    private List createStudents(final DB2UDB db2) throws SQLException {
        final List rtn = new ArrayList();
        final String sql = sqlStudents();

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                final String schregno = rs.getString("schregno");
                final String grade = rs.getString("grade");
                final String hrclass = rs.getString("hrclass");
                final String attendno = rs.getString("attendno");
                final String name = rs.getString("name");

                final Student student = new Student(
                        schregno,
                        grade,
                        hrclass,
                        attendno,
                        name
                );

                rtn.add(student);
            }
        } catch (final Exception ex) {
            log.error("生徒のロードでエラー:" + sql, ex);
        } finally {
            db2.commit();
            DbUtils.closeQuietly(null, ps, rs);
        }
        return rtn;
    }

    private void dumpParam(final HttpServletRequest request) {
        log.fatal("$Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
    }

    private void init(final HttpServletResponse response, final Vrw32alp svf) throws IOException {
        response.setContentType("application/pdf");

        svf.VrInit();
        svf.VrSetSpoolFileStream(response.getOutputStream());
    }

    private void close(final Vrw32alp svf, final DB2UDB db2) {
        if (null != svf) {
            svf.VrQuit();
        }
        if (null != db2) {
            db2.commit();
            db2.close();
        }
    }

    private String sqlStudents() {
        final String students = SQLUtils.whereIn(true, _param._schregno);
        return " select"
                + "    T1.SCHREGNO as schregno,"
                + "    T1.GRADE as grade,"
                + "    T1.HR_CLASS as hrclass,"
                + "    T1.ATTENDNO as attendno,"
                + "    T2.NAME as name"
                + " from"
                + "    SCHREG_REGD_DAT T1,"
                + "    SCHREG_BASE_MST T2"
                + " where"
                + "    T1.SCHREGNO = T2.SCHREGNO and"
                + "    T1.YEAR = '" + _param._year + "' and"
                + "    T1.SEMESTER = '" + _param._semester + "' and"
                + "    T1.SCHREGNO in " + students
                + " order by"
                + "    T1.GRADE,"
                + "    T1.HR_CLASS,"
                + "    T1.ATTENDNO";
    }

    private String sqlOpinion(final Student student, final Param param) {
        return " SELECT"
                + "     SEMESTER,"
                + "     SCHREGNO,"
                + "     TOTALSTUDYTIME"
                + " FROM"
                + "     HREPORTREMARK_DAT"
                + " WHERE"
                + "     YEAR = '" + param._year + "' AND"
                + "     SCHREGNO = '" + student._schregno + "'"
                + " ORDER BY"
                + "     SEMESTER";
    }

    /** 
     * 出欠明細(過去)を取得するＳＱＬ文を戻します。
     * ・生徒個人を対象とします。
     * ・任意の生徒の学期別（合計を含める）出欠データ表。
     */
    private String sqlAttendOld(final Student student, final Param param, final String grade) {
        StringBuffer stb = new StringBuffer();
        // 対象生徒の年度（過去学年）
        stb.append("WITH MAX_YEAR AS(");
        stb.append(    "SELECT  MAX(YEAR) AS YEAR ");
        stb.append(    "FROM    SCHREG_REGD_DAT ");
        stb.append(    "WHERE   SCHREGNO = '" + student._schregno + "' AND ");
        stb.append(            "YEAR < '" + param._year + "' AND ");
        stb.append(            "GRADE = '" + grade + "' ");
        stb.append(    "GROUP BY SCHREGNO ");
        stb.append(" ) ");
        // 対象生徒
        stb.append(", SCHNO (SCHREGNO, SEMESTER) AS(");
        stb.append(    "SELECT  T1.SCHREGNO, T1.SEMESTER ");
        stb.append(    "FROM    SCHREG_REGD_DAT T1, SEMESTER_MST T2 ");
        stb.append(    "WHERE   T1.SCHREGNO = '" + student._schregno + "' ");
        stb.append(        "AND EXISTS (SELECT 'X' FROM MAX_YEAR Y1 WHERE Y1.YEAR = T1.YEAR) ");
        stb.append(        "AND T1.YEAR = T2.YEAR ");
        stb.append(        "AND T1.SEMESTER = T2.SEMESTER ");
        stb.append(    "UNION  VALUES( cast('" + student._schregno + "' as varchar(8) ), '9') ");
        stb.append(" ) ");

        // メイン表
        stb.append(   "SELECT  TT0.SEMESTER, ");
        //                     授業日数
        stb.append(           "VALUE(TT7.LESSON,0) AS LESSON, ");
        //                     出停日数
        stb.append(           "VALUE(TT7.SUSPEND,0) AS SUSPEND, ");
        //                     忌引日数
        stb.append(           "VALUE(TT7.MOURNING,0) AS MOURNING, ");
        //                     出席しなければならない日数
        stb.append(           "VALUE(TT7.LESSON,0) - VALUE(TT7.SUSPEND,0) - VALUE(TT7.MOURNING,0) ");
        if ("true".equals(param._useVirus)) {
            stb.append(           " - VALUE(TT7.VIRUS,0) ");
        }
        if ("true".equals(param._useKoudome)) {
            stb.append(           " - VALUE(TT7.KOUDOME,0) ");
        }
        stb.append(           "AS MLESSON, ");
        //                     欠席日数
        stb.append(           "VALUE(TT7.ABSENT,0) AS SICK, ");
        //                     出席日数
        stb.append(           "VALUE(TT7.LESSON,0) - VALUE(TT7.SUSPEND,0) - VALUE(TT7.MOURNING,0) ");
        if ("true".equals(param._useVirus)) {
            stb.append(           " - VALUE(TT7.VIRUS,0) ");
        }
        if ("true".equals(param._useKoudome)) {
            stb.append(           " - VALUE(TT7.KOUDOME,0) ");
        }
        stb.append(           " - VALUE(TT7.ABSENT,0) AS PRESENT, ");
        //                     遅刻・早退回数
        stb.append(           "VALUE(TT7.LATE,0) AS LATE, ");
        stb.append(           "VALUE(TT7.EARLY,0) AS EARLY, ");
        //                     留学日数
        stb.append(           "VALUE(TT7.ABROAD,0) AS TRANSFER_DATE ");
        stb.append(   "FROM    SCHNO TT0 ");

        // 月別集計データから集計した表
        stb.append(   "LEFT JOIN(");
        stb.append(      "SELECT  VALUE(SEMESTER,'9')AS SEMESTER, ");
        stb.append(              "VALUE(SUM(LESSON),0) ");
        if (!"1".equals(_knjSchoolMst._semOffDays)) {
            stb.append(          " - VALUE(SUM(OFFDAYS),0) ");
        }
        stb.append(              " - VALUE(SUM(ABROAD),0) AS LESSON, ");
        stb.append(              "SUM(MOURNING) AS MOURNING, ");
        stb.append(              "SUM(SUSPEND) AS SUSPEND, ");
        if ("true".equals(param._useVirus)) {
            stb.append(              "SUM(VIRUS) AS VIRUS, ");
        } else {
            stb.append(              "0 AS VIRUS, ");
        }
        if ("true".equals(param._useKoudome)) {
            stb.append(              "SUM(KOUDOME) AS KOUDOME, ");
        } else {
            stb.append(              "0 AS KOUDOME, ");
        }
        stb.append(              "SUM( VALUE(SICK,0) + VALUE(NOTICE,0) + VALUE(NONOTICE,0) ");
        if ("1".equals(_knjSchoolMst._semOffDays)) {
            stb.append(          "   + VALUE(OFFDAYS,0) ");
        }
        stb.append(              "   ) AS ABSENT, ");
        stb.append(              "SUM(LATE) AS LATE, ");
        stb.append(              "SUM(EARLY) AS EARLY, ");
        stb.append(              "SUM(ABROAD) AS ABROAD, ");
        stb.append(              "SUM(OFFDAYS) AS OFFDAYS ");
        stb.append(      "FROM    ATTEND_SEMES_DAT W1 ");
        stb.append(      "WHERE   EXISTS (SELECT 'X' FROM MAX_YEAR Y1 WHERE Y1.YEAR = W1.YEAR) AND ");
        stb.append(              "EXISTS (SELECT 'X' FROM SCHNO W2 WHERE W1.SCHREGNO = W2.SCHREGNO)");
        stb.append(      "GROUP BY GROUPING SETS(SEMESTER,()) ");
        stb.append(      ")TT7 ON TT0.SEMESTER = TT7.SEMESTER ");

        stb.append(" ORDER BY TT0.SEMESTER");
        return stb.toString();
    }

    /** 
     * 科目を取得するＳＱＬ文を戻します。
     * ・生徒個人を対象とします。
     * ・任意の生徒の科目表。
     */
    private String sqlSubclassName(final Student student, final Param param, final String grade, final boolean hasHighAdviceOld) {
        StringBuffer stb = new StringBuffer();
        // 対象生徒の年度（過去学年＋今学年）
        stb.append("WITH MAX_YEAR AS(");
        stb.append(    "SELECT  SCHREGNO, GRADE, MAX(YEAR) AS YEAR ");
        stb.append(    "FROM    SCHREG_REGD_DAT ");
        stb.append(    "WHERE   SCHREGNO = '" + student._schregno + "' AND ");
        stb.append(            "YEAR <= '" + param._year + "' AND ");
        stb.append(            "GRADE IN " + grade + " ");
        stb.append(    "GROUP BY SCHREGNO, GRADE ");
        stb.append(" ) ");
        // 読替先科目
        stb.append(", T_REPLACE AS ( ");
        stb.append(     "SELECT  T1.YEAR, ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append(            "T1.COMBINED_CLASSCD || '-' || T1.COMBINED_SCHOOL_KIND || '-' || T1.COMBINED_CURRICULUM_CD || '-' || ");
        }
        stb.append(     "        T1.COMBINED_SUBCLASSCD AS SUBCLASSCD ");
        stb.append(     "FROM    SUBCLASS_REPLACE_COMBINED_DAT T1 ");
        stb.append(     "WHERE   EXISTS (SELECT 'X' FROM MAX_YEAR Y1 WHERE Y1.YEAR = T1.YEAR) ");
        stb.append(     "GROUP BY T1.YEAR, ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append(            "T1.COMBINED_CLASSCD || '-' || T1.COMBINED_SCHOOL_KIND || '-' || T1.COMBINED_CURRICULUM_CD || '-' || ");
        }
        stb.append(              "T1.COMBINED_SUBCLASSCD ");
        stb.append(" ) ");
        // 対象生徒の科目（過去学年＋今学年）
        stb.append(", T_CHAIR AS ( ");
        stb.append("    SELECT  T1.SCHREGNO, ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append(            "T2.CLASSCD, ");
            stb.append(            "T2.SCHOOL_KIND, ");
            stb.append(            "T2.CURRICULUM_CD, ");
            stb.append(            "T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || ");
        }
        stb.append(            "T2.SUBCLASSCD AS SUBCLASSCD ");
        stb.append("      FROM  CHAIR_STD_DAT T1, ");
        stb.append("            CHAIR_DAT T2 ");
        stb.append("     WHERE  T1.YEAR = T2.YEAR ");
        stb.append("       AND  T1.SEMESTER = T2.SEMESTER ");
        stb.append("       AND  T1.CHAIRCD = T2.CHAIRCD ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append("       AND  CLASSCD < '89' ");
        } else {
            stb.append("       AND  SUBSTR(T2.SUBCLASSCD,1,2) < '89' ");
        }
        stb.append("       AND  EXISTS (SELECT 'X' FROM MAX_YEAR Y1 WHERE Y1.YEAR = T1.YEAR AND Y1.SCHREGNO = T1.SCHREGNO) ");
        stb.append("       AND  NOT EXISTS (SELECT 'X' FROM T_REPLACE R1 WHERE R1.YEAR = T2.YEAR AND R1.SUBCLASSCD = ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append(            "T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || ");
        }
        stb.append(                                                                                 "T2.SUBCLASSCD) ");
        stb.append("    GROUP BY T1.SCHREGNO, ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append(            "T2.CLASSCD, ");
            stb.append(            "T2.SCHOOL_KIND, ");
            stb.append(            "T2.CURRICULUM_CD, ");
            stb.append(            "T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || ");
        }
        stb.append(             "T2.SUBCLASSCD ");
        // 過去（2005,2006）の履修科目がないので、RECORD_RANK_DATから科目を抽出する
        if (param.isHighAdviceOld()) { // TODO: 2007/2008年度専用
            stb.append("    UNION ");
            stb.append("    SELECT  T1.SCHREGNO, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(            "T1.CLASSCD, ");
                stb.append(            "T1.SCHOOL_KIND, ");
                stb.append(            "T1.CURRICULUM_CD, ");
                stb.append(            "T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
            }
            stb.append(            "T1.SUBCLASSCD AS SUBCLASSCD ");
            stb.append("      FROM  RECORD_RANK_DAT T1 ");
            stb.append("     WHERE  EXISTS (SELECT 'X' FROM MAX_YEAR Y1 WHERE Y1.YEAR = T1.YEAR AND Y1.SCHREGNO = T1.SCHREGNO) ");
            stb.append("    GROUP BY T1.SCHREGNO, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(            "T1.CLASSCD, ");
                stb.append(            "T1.SCHOOL_KIND, ");
                stb.append(            "T1.CURRICULUM_CD, ");
                stb.append(            "T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
            }
            stb.append(             "T1.SUBCLASSCD ");
        }
        stb.append("    ) ");
        // 教科毎の科目数
        stb.append(", T_SUBCNT AS ( ");
        stb.append("    SELECT  TT1.CLASSCD,  ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append(            "TT1.SCHOOL_KIND, ");
        }
        stb.append("            TT1.ELECTDIV,  ");
        stb.append("            COUNT(DISTINCT TT1.SUBCLASSCD) AS SUBCNT ");
        stb.append("      FROM  ( ");

        stb.append("    SELECT  T3.CLASSCD,  ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append(            "T3.SCHOOL_KIND, ");
        }
        stb.append("            VALUE(T2.ELECTDIV,'0') AS ELECTDIV,  ");
        stb.append("            CASE WHEN T6.SUBCLASSNAME3 IS NOT NULL ");
        stb.append("                 THEN T6.SUBCLASSCD3 ");
        stb.append("                 ELSE ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append(            "T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
        }
        stb.append("                 T1.SUBCLASSCD END AS SUBCLASSCD ");
        stb.append("      FROM  T_CHAIR T1 ");
        stb.append("            INNER JOIN SUBCLASS_MST T2 ON ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append(            "T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || ");
        }
        stb.append(               " T2.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("            INNER JOIN CLASS_MST T3 ON ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append(           " T3.CLASSCD || '-' || T3.SCHOOL_KIND = ");
            stb.append(           " T1.CLASSCD || '-' || T1.SCHOOL_KIND ");
        } else {
            stb.append(           " T3.CLASSCD = SUBSTR(T1.SUBCLASSCD,1,2) ");
        }
        stb.append("        LEFT JOIN ( ");
        stb.append("            SELECT  ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append(            "T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
        }
        stb.append("                    T1.SUBCLASSCD AS SUBCLASSCD, ");
        stb.append("                    T1.SUBCLASSNAME, ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append(            "T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
        }
        stb.append("                    T1.SUBCLASSCD3 AS SUBCLASSCD3, ");
        stb.append("                    CASE WHEN T2.SUBCLASSORDERNAME2 IS NOT NULL ");
        stb.append("                         THEN T2.SUBCLASSORDERNAME2 ");
        stb.append("                         ELSE T2.SUBCLASSNAME END AS SUBCLASSNAME3 ");
        stb.append("              FROM  SUBCLASS_MST T1 ");
        stb.append("                    LEFT JOIN SUBCLASS_MST T2 ON ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append(            "T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || ");
        }
        stb.append(                " T2.SUBCLASSCD=");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append(            "T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
        }
        stb.append(                " T1.SUBCLASSCD3 ");
        stb.append("             WHERE  VALUE(T1.ELECTDIV,'0') NOT IN ('1')  ");
        stb.append("               AND  T1.SUBCLASSCD3 IS NOT NULL ");
        stb.append("            ) T6 ON T6.SUBCLASSCD=");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append(            "T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
        }
        stb.append(" T1.SUBCLASSCD ");
        stb.append("     WHERE  VALUE(T2.ELECTDIV,'0') NOT IN ('1') ");// 選択科目以外

        stb.append("            ) TT1 ");
        stb.append("    GROUP BY TT1.CLASSCD, ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append(            "TT1.SCHOOL_KIND, ");
        }
        stb.append("             TT1.ELECTDIV ");
        stb.append("    ) ");
        stb.append(", T_SUBCNT2 AS ( ");
        stb.append("    SELECT  VALUE(T2.ELECTDIV,'0') AS ELECTDIV,  ");
        stb.append("            COUNT(*) AS SUBCNT ");
        stb.append("      FROM  T_CHAIR T1 ");
        stb.append("            INNER JOIN SUBCLASS_MST T2 ON ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append(            "T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || ");
        }
        stb.append(" T2.SUBCLASSCD = ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append(            "T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
        }
        stb.append(" T1.SUBCLASSCD ");
        stb.append("            INNER JOIN CLASS_MST T3 ON ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append(            "T3.CLASSCD || '-' || T3.SCHOOL_KIND = ");
            stb.append(            "T1.CLASSCD || '-' || T1.SCHOOL_KIND ");
        } else {
            stb.append(" T3.CLASSCD = SUBSTR(T1.SUBCLASSCD,1,2) ");
        }
        stb.append("     WHERE  VALUE(T2.ELECTDIV,'0') IN ('1') ");// 選択科目のみ
        stb.append("    GROUP BY VALUE(T2.ELECTDIV,'0') ");
        stb.append("    ) ");

        stb.append("SELECT  T1.SCHREGNO,  ");
        stb.append("        CASE WHEN VALUE(T2.ELECTDIV,'0') = '1' ");
        stb.append("             THEN VALUE(T2.SHOWORDER3,0) ");
        stb.append("             ELSE 0 END AS SHOWORDER3, ");
        stb.append("        CASE WHEN T6.SUBCLASSNAME3 IS NOT NULL ");
        stb.append("             THEN T6.SUBCLASSCD3 ");
        stb.append("             ELSE T1.SUBCLASSCD END AS SORT_SUBCLASSCD, ");
        stb.append("        CASE WHEN VALUE(T2.ELECTDIV,'0') = '1' ");
        stb.append("             THEN '*' ");
        stb.append("             ELSE T3.CLASSCD END AS CLASSCD, ");
        stb.append("        CASE WHEN VALUE(T2.ELECTDIV,'0') = '1' ");
        stb.append("             THEN '選択講座' ");
        stb.append("             ELSE T3.CLASSABBV END AS CLASSABBV, ");
        stb.append("        CASE WHEN VALUE(T2.ELECTDIV,'0') = '1' ");
        stb.append("             THEN T5.SUBCNT ");
        stb.append("             ELSE T4.SUBCNT END AS SUBCNT, ");
        stb.append("        T1.SUBCLASSCD,  ");
        stb.append("        CASE WHEN T6.SUBCLASSNAME3 IS NOT NULL ");
        stb.append("             THEN T6.SUBCLASSNAME3 ");
        stb.append("             WHEN T2.SUBCLASSORDERNAME2 IS NOT NULL ");
        stb.append("             THEN T2.SUBCLASSORDERNAME2 ");
        stb.append("             ELSE T2.SUBCLASSNAME END AS SUBCLASSNAME, ");
        stb.append("        VALUE(T2.ELECTDIV,'0') AS ELECTDIV ");
        stb.append("  FROM  T_CHAIR T1 ");
        stb.append("        INNER JOIN SUBCLASS_MST T2 ON ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append(            "T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || ");
        }
        stb.append(" T2.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("        INNER JOIN CLASS_MST T3 ON ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append(" T3.CLASSCD || '-' || T3.SCHOOL_KIND = ");
            stb.append(" T1.CLASSCD || '-' || T1.SCHOOL_KIND ");
        } else {
            stb.append(" T3.CLASSCD = SUBSTR(T1.SUBCLASSCD,1,2) ");
        }
        stb.append("        LEFT JOIN T_SUBCNT T4 ON ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append(" T4.CLASSCD || '-' || T4.SCHOOL_KIND = ");
            stb.append(" T2.CLASSCD || '-' || T2.SCHOOL_KIND ");
        } else {
            stb.append(" T4.CLASSCD = SUBSTR(T2.SUBCLASSCD,1,2) ");
        }
        stb.append(" AND T4.ELECTDIV = VALUE(T2.ELECTDIV,'0') ");
        stb.append("        LEFT JOIN T_SUBCNT2 T5 ON T5.ELECTDIV = VALUE(T2.ELECTDIV,'0') ");
        stb.append("        LEFT JOIN ( ");
        stb.append("            SELECT ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append(" T1.CLASSCD || '-' || T1.SCHOOL_KIND  || '-' || T1.CURRICULUM_CD || '-' || ");
        }
        stb.append("                    T1.SUBCLASSCD AS SUBCLASSCD, ");
        stb.append("                    T1.SUBCLASSNAME, ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append(" T1.CLASSCD || '-' || T1.SCHOOL_KIND  || '-' || T1.CURRICULUM_CD || '-' || ");
        }
        stb.append("                    T1.SUBCLASSCD3 AS SUBCLASSCD3, ");
        stb.append("                    CASE WHEN T2.SUBCLASSORDERNAME2 IS NOT NULL ");
        stb.append("                         THEN T2.SUBCLASSORDERNAME2 ");
        stb.append("                         ELSE T2.SUBCLASSNAME END AS SUBCLASSNAME3 ");
        stb.append("              FROM  SUBCLASS_MST T1 ");
        stb.append("                    LEFT JOIN SUBCLASS_MST T2 ON ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append(" T2.CLASSCD || '-' || T2.SCHOOL_KIND  || '-' || T2.CURRICULUM_CD || '-' || ");
        }
        stb.append(" T2.SUBCLASSCD=");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append(" T1.CLASSCD || '-' || T1.SCHOOL_KIND  || '-' || T1.CURRICULUM_CD || '-' || ");
        }
        stb.append(" T1.SUBCLASSCD3 ");
        stb.append("             WHERE  VALUE(T1.ELECTDIV,'0') NOT IN ('1')  ");
        stb.append("               AND  T1.SUBCLASSCD3 IS NOT NULL ");
        stb.append("            ) T6 ON T6.SUBCLASSCD=T1.SUBCLASSCD ");
//        if (hasHighAdviceOld) stb.append("WHERE VALUE(T2.ELECTDIV,'0') NOT IN ('1') ");
//        stb.append("ORDER BY VALUE(T2.ELECTDIV,'0'), 2, T1.SUBCLASSCD ");
        stb.append("ORDER BY VALUE(T2.ELECTDIV,'0'), 2, 3 ");
        return stb.toString();
    }

    /** 
     * 成績を取得するＳＱＬ文を戻します。
     * ・生徒個人・１科目を対象とします。
     * ・任意の生徒・科目の成績表。
     */
    private String sqlScore(final Student student, final Param param, final String grade, final String subclasscd) {
        StringBuffer stb = new StringBuffer();
        // 対象生徒の年度
        stb.append("WITH MAX_YEAR AS( ");
        stb.append("    SELECT  SCHREGNO, GRADE, MAX(YEAR) AS YEAR ");
        stb.append("    FROM    SCHREG_REGD_DAT ");
        stb.append("    WHERE   SCHREGNO = '" + student._schregno + "' AND ");
        stb.append("            YEAR <= '" + param._year + "' AND ");
        stb.append("            GRADE = '" + grade + "' ");
        stb.append("    GROUP BY SCHREGNO, GRADE ");
        stb.append(" ) ");
        // 各学期成績・学年評定の表（RECORD_RANK_DATから取得）
        stb.append(", T_RECORD_RANK AS ( ");
        stb.append("    SELECT  T1.YEAR, T1.SCHREGNO, ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append(" T1.CLASSCD || '-' || T1.SCHOOL_KIND  || '-' || T1.CURRICULUM_CD || '-' || ");
        }
        stb.append("            T1.SUBCLASSCD AS SUBCLASSCD,  ");
        stb.append("            MAX(CASE WHEN T1.SEMESTER = '1' THEN T1.SCORE END) AS SCORE1, ");
        stb.append("            MAX(CASE WHEN T1.SEMESTER = '2' THEN T1.SCORE END) AS SCORE2, ");
        stb.append("            MAX(CASE WHEN T1.SEMESTER = '3' THEN T1.SCORE END) AS SCORE3, ");
        stb.append("            MAX(CASE WHEN T1.SEMESTER = '9' THEN T1.SCORE END) AS SCORE4 ");
        stb.append("      FROM  RECORD_RANK_DAT T1 ");
        stb.append("     WHERE  EXISTS (SELECT 'X' FROM MAX_YEAR Y1 WHERE Y1.YEAR = T1.YEAR AND Y1.SCHREGNO = T1.SCHREGNO) ");
        stb.append("       AND  T1.TESTKINDCD = '99' ");
        stb.append("       AND  T1.TESTITEMCD = '00' ");
        stb.append("       AND  ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append(" T1.CLASSCD || '-' || T1.SCHOOL_KIND  || '-' || T1.CURRICULUM_CD || '-' || ");
        }
        stb.append(             " T1.SUBCLASSCD = '" + subclasscd + "' ");
        stb.append("    GROUP BY T1.YEAR, T1.SCHREGNO, ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append(" T1.CLASSCD || '-' || T1.SCHOOL_KIND  || '-' || T1.CURRICULUM_CD || '-' || ");
        }
        stb.append("             T1.SUBCLASSCD ");
        stb.append("    ) ");
        // 修得単位の表（RECORD_SCORE_DATから取得）
        stb.append(", T_CREDIT AS ( ");
        stb.append("    SELECT  T1.SCHREGNO, ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append(" T1.CLASSCD || '-' || T1.SCHOOL_KIND  || '-' || T1.CURRICULUM_CD || '-' || ");
        }
        stb.append("            T1.SUBCLASSCD AS SUBCLASSCD, T1.GET_CREDIT AS CREDIT ");
        stb.append("      FROM  RECORD_SCORE_DAT T1 ");
        stb.append("     WHERE  EXISTS (SELECT 'X' FROM MAX_YEAR Y1 WHERE Y1.YEAR = T1.YEAR AND Y1.SCHREGNO = T1.SCHREGNO) ");
        stb.append("       AND  T1.SEMESTER = '9' ");
        stb.append("       AND  T1.TESTKINDCD = '99' ");
        stb.append("       AND  T1.TESTITEMCD = '00' ");
        stb.append("       AND  T1.SCORE_DIV = '00' ");
        stb.append("       AND  ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append(" T1.CLASSCD || '-' || T1.SCHOOL_KIND  || '-' || T1.CURRICULUM_CD || '-' || ");
        }
        stb.append("            T1.SUBCLASSCD = '" + subclasscd + "' ");
        stb.append("    ) ");
        // 評定平均の表（RECORD_RANK_DATから取得）
        stb.append(", T_AVG AS ( ");
        stb.append("    SELECT  T1.SCHREGNO,  ");
        stb.append("            MAX(CASE WHEN T1.SEMESTER = '1' THEN DECIMAL(ROUND(FLOAT(T1.AVG)*10,0)/10,5,1) END) AS AVG1, ");
        stb.append("            MAX(CASE WHEN T1.SEMESTER = '2' THEN DECIMAL(ROUND(FLOAT(T1.AVG)*10,0)/10,5,1) END) AS AVG2, ");
        stb.append("            MAX(CASE WHEN T1.SEMESTER = '3' THEN DECIMAL(ROUND(FLOAT(T1.AVG)*10,0)/10,5,1) END) AS AVG3, ");
        stb.append("            MAX(CASE WHEN T1.SEMESTER = '9' THEN DECIMAL(ROUND(FLOAT(T1.AVG)*10,0)/10,5,1) END) AS AVG4 ");
        stb.append("      FROM  RECORD_RANK_DAT T1 ");
        stb.append("     WHERE  EXISTS (SELECT 'X' FROM MAX_YEAR Y1 WHERE Y1.YEAR = T1.YEAR AND Y1.SCHREGNO = T1.SCHREGNO) ");
        stb.append("       AND  T1.TESTKINDCD = '99' ");
        stb.append("       AND  T1.TESTITEMCD = '00' ");
        stb.append("       AND  T1.SUBCLASSCD = '999999' ");
        stb.append("    GROUP BY T1.SCHREGNO ");
        stb.append("    ) ");
        // 合計の表
        stb.append(", T_RECORD_RANK2 AS ( ");
        stb.append("    SELECT  T1.SCHREGNO, ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append(" T1.CLASSCD || '-' || T1.SCHOOL_KIND  || '-' || T1.CURRICULUM_CD || '-' || ");
        }
        stb.append("            T1.SUBCLASSCD AS SUBCLASSCD,  ");
        stb.append("            MAX(CASE WHEN T1.SEMESTER = '1' THEN T1.SCORE END) AS SCORE1, ");
        stb.append("            MAX(CASE WHEN T1.SEMESTER = '2' THEN T1.SCORE END) AS SCORE2, ");
        stb.append("            MAX(CASE WHEN T1.SEMESTER = '3' THEN T1.SCORE END) AS SCORE3, ");
        stb.append("            MAX(CASE WHEN T1.SEMESTER = '9' THEN T1.SCORE END) AS SCORE4 ");
        stb.append("      FROM  RECORD_RANK_DAT T1 ");
        stb.append("     WHERE  EXISTS (SELECT 'X' FROM MAX_YEAR Y1 WHERE Y1.YEAR = T1.YEAR AND Y1.SCHREGNO = T1.SCHREGNO) ");
        stb.append("       AND  T1.TESTKINDCD = '99' ");
        stb.append("       AND  T1.TESTITEMCD = '00' ");
        stb.append("       AND  T1.SUBCLASSCD NOT IN ('333333','555555','999999') ");
        stb.append("    GROUP BY T1.SCHREGNO, ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append(" T1.CLASSCD || '-' || T1.SCHOOL_KIND  || '-' || T1.CURRICULUM_CD || '-' || ");
        }
        stb.append("             T1.SUBCLASSCD ");
        stb.append("    ) ");
        stb.append(", T_CREDIT2 AS ( ");
        stb.append("    SELECT  T1.SCHREGNO, ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append(" T1.CLASSCD || '-' || T1.SCHOOL_KIND  || '-' || T1.CURRICULUM_CD || '-' || ");
        }
        stb.append("            T1.SUBCLASSCD AS SUBCLASSCD, T1.GET_CREDIT AS CREDIT ");
        stb.append("      FROM  RECORD_SCORE_DAT T1 ");
        stb.append("     WHERE  EXISTS (SELECT 'X' FROM MAX_YEAR Y1 WHERE Y1.YEAR = T1.YEAR AND Y1.SCHREGNO = T1.SCHREGNO) ");
        stb.append("       AND  T1.SEMESTER = '9' ");
        stb.append("       AND  T1.TESTKINDCD = '99' ");
        stb.append("       AND  T1.TESTITEMCD = '00' ");
        stb.append("       AND  T1.SCORE_DIV = '00' ");
        stb.append("    ) ");
        // 読替先科目
        stb.append(", T_REPLACE AS ( ");
        stb.append(     "SELECT  T1.YEAR, ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append(" T1.COMBINED_CLASSCD || '-' || T1.COMBINED_SCHOOL_KIND  || '-' || T1.COMBINED_CURRICULUM_CD || '-' || ");
        }
        stb.append(     "        T1.COMBINED_SUBCLASSCD AS SUBCLASSCD ");
        stb.append(     "FROM    SUBCLASS_REPLACE_COMBINED_DAT T1 ");
        stb.append(     "WHERE   EXISTS (SELECT 'X' FROM MAX_YEAR Y1 WHERE Y1.YEAR = T1.YEAR) ");
        stb.append(     "GROUP BY T1.YEAR, ");
        if ("1".equals(param._useCurriculumcd)) {
            stb.append(" T1.COMBINED_CLASSCD || '-' || T1.COMBINED_SCHOOL_KIND  || '-' || T1.COMBINED_CURRICULUM_CD || '-' || ");
        }
        stb.append("              T1.COMBINED_SUBCLASSCD ");
        stb.append(" ) ");
        stb.append(", T_TOTAL AS ( ");
        stb.append("    SELECT  T1.SCHREGNO,  ");
        stb.append("            DECIMAL(ROUND(AVG(FLOAT(T1.SCORE1))*10,0)/10,5,1) AS AVG1, ");
        stb.append("            DECIMAL(ROUND(AVG(FLOAT(T1.SCORE2))*10,0)/10,5,1) AS AVG2, ");
        stb.append("            DECIMAL(ROUND(AVG(FLOAT(T1.SCORE3))*10,0)/10,5,1) AS AVG3, ");
        stb.append("            DECIMAL(ROUND(AVG(FLOAT(T1.SCORE4))*10,0)/10,5,1) AS AVG4, ");
        stb.append("            SUM(T2.CREDIT) AS TOTAL5, ");
        stb.append("            SUM(T1.SCORE4 * T2.CREDIT) AS TOTAL6, ");
        stb.append("            SUM(100 * T2.CREDIT) AS MAXGPOINT ");
        stb.append("      FROM  T_RECORD_RANK2 T1 ");
        stb.append("            LEFT JOIN T_CREDIT2 T2 ON T2.SCHREGNO = T1.SCHREGNO AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("     WHERE  NOT EXISTS (SELECT 'X' FROM T_REPLACE R1 WHERE R1.SUBCLASSCD = T1.SUBCLASSCD) ");
        if (param.isHighAdviceOld()) { // TODO: 2007/2008年度専用
            stb.append("   AND  NOT EXISTS (SELECT 'X' FROM SUBCLASS_MST S1 WHERE S1.ELECTDIV = '1' AND ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(" S1.CLASSCD || '-' || S1.SCHOOL_KIND  || '-' || S1.CURRICULUM_CD || '-' || ");
            }
            stb.append(                                                          "S1.SUBCLASSCD = T1.SUBCLASSCD) ");
        }
        stb.append("    GROUP BY T1.SCHREGNO ");
        stb.append("    ) ");

        stb.append("SELECT  T1.SCHREGNO, T1.SUBCLASSCD,  ");
        stb.append("        T1.SCORE1, ");
        stb.append("        T1.SCORE2, ");
        stb.append("        T1.SCORE3, ");
        stb.append("        T1.SCORE4, ");
        stb.append("        T2.CREDIT, ");
        stb.append("        T1.SCORE4 * CREDIT AS SCORE4_CREDIT ");
//        stb.append("       ,T3.AVG1 ");
//        stb.append("       ,T3.AVG2 ");
//        stb.append("       ,T3.AVG3 ");
//        stb.append("       ,T3.AVG4 ");
        stb.append("       ,CASE WHEN T1.YEAR < '2007' THEN T4.AVG1 ELSE T3.AVG1 END AS AVG1 ");
        stb.append("       ,CASE WHEN T1.YEAR < '2007' THEN T4.AVG2 ELSE T3.AVG2 END AS AVG2 ");
        stb.append("       ,CASE WHEN T1.YEAR < '2007' THEN T4.AVG3 ELSE T3.AVG3 END AS AVG3 ");
        stb.append("       ,CASE WHEN T1.YEAR < '2007' THEN T4.AVG4 ELSE T3.AVG4 END AS AVG4 ");
        stb.append("       ,T4.TOTAL5 ");
        stb.append("       ,T4.TOTAL6 ");
        stb.append("       ,T4.MAXGPOINT ");
        stb.append("  FROM  T_RECORD_RANK T1 ");
        stb.append("        LEFT JOIN T_CREDIT T2 ON T2.SCHREGNO = T1.SCHREGNO AND T2.SUBCLASSCD = T1.SUBCLASSCD ");
        stb.append("        LEFT JOIN T_AVG T3 ON T3.SCHREGNO = T1.SCHREGNO ");
        stb.append("        LEFT JOIN T_TOTAL T4 ON T4.SCHREGNO = T1.SCHREGNO ");
        return stb.toString();
    }

    /** 
     * G値・A値・A値順位を取得するＳＱＬ文を戻します。
     * ・生徒個人を対象とします。
     * ・任意の生徒の表。
     */
    private String sqlMockRank(final Student student, final Param param, final String grade) {
        StringBuffer stb = new StringBuffer();
        // 対象生徒の年度（過去学年＋今学年）
        stb.append("WITH MAX_YEAR AS( ");
        stb.append("    SELECT  SCHREGNO, GRADE, MAX(YEAR) AS YEAR ");
        stb.append("    FROM    SCHREG_REGD_DAT ");
        stb.append("    WHERE   SCHREGNO = '" + student._schregno + "' AND ");
        stb.append("            YEAR <= '" + param._year + "' AND ");
        stb.append("            GRADE IN " + grade + " ");
        stb.append("    GROUP BY SCHREGNO, GRADE ");
        stb.append(" ) ");

        stb.append("SELECT  T1.YEAR, ");
        stb.append("        T1.SCHREGNO, ");
        stb.append("        T1.DATA_DIV, ");// 0=単年、1=累積
        stb.append("        T1.COURSE_DIV, ");// 0=無し、1=文系、2=理系
        stb.append("        T1.GRADE, ");
        stb.append("        T1.SCORE1 AS G_SCORE, ");
        stb.append("        T1.SCORE3 AS A_SCORE, ");
        stb.append("        T1.RANK AS A_RANK, ");
        stb.append("        T1.PERFECT3 AS A_PERFECT, ");
        stb.append("        T1.PERFECT1 AS G_PERFECT ");
        stb.append("FROM    RECORD_MOCK_RANK_DAT T1 ");
        stb.append("WHERE   EXISTS (SELECT 'X' FROM MAX_YEAR Y1 ");
        stb.append("                 WHERE Y1.YEAR = T1.YEAR AND Y1.SCHREGNO = T1.SCHREGNO) ");
        stb.append("AND     T1.SUBCLASSCD='333333' ");
        stb.append("ORDER BY T1.DATA_DIV, T1.GRADE ");
        return stb.toString();
    }

    /** 
     * 実力テストを取得するＳＱＬ文を戻します。
     * ・生徒個人を対象とします。
     * ・CLASSCD：01:国語、02:社会、03:英語、04:数学、05:理科
     */
    private String sqlMockDat(final Student student, final Param param) {
        StringBuffer stb = new StringBuffer();
        // 対象生徒の年度（過去学年＋今学年）
        stb.append("WITH MAX_YEAR AS( ");
        stb.append("    SELECT  SCHREGNO, GRADE, MAX(YEAR) AS YEAR ");
        stb.append("    FROM    SCHREG_REGD_DAT ");
        stb.append("    WHERE   SCHREGNO = '" + student._schregno + "' AND ");
        stb.append("            YEAR <= '" + param._year + "' AND ");
        stb.append("            GRADE IN " + param._gradeIn + " ");
        stb.append("    GROUP BY SCHREGNO, GRADE ");
        stb.append(" ) ");

        stb.append("SELECT  T1.YEAR, ");
        stb.append("        T1.MOCKCD, ");
        stb.append("        T1.SCHREGNO, ");
        stb.append("        T1.MOCK_SUBCLASS_CD AS SUBCLASSCD, ");
        stb.append("        VALUE(N1.NAMECD2, '00') AS CLASSCD, ");
        stb.append("        Y1.GRADE, ");
        stb.append("        T1.SCORE ");
        stb.append("FROM    MAX_YEAR Y1, ");
        stb.append("        MOCK_DAT T1 ");
        stb.append("        LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'D007' AND N1.NAME1 = T1.MOCK_SUBCLASS_CD ");
        stb.append("WHERE   T1.YEAR = Y1.YEAR ");
        stb.append("  AND   T1.SCHREGNO = Y1.SCHREGNO ");
        stb.append("  AND   T1.MOCKCD LIKE '2%' ");
        return stb.toString();
    }

    static private String getOneSvfFieldName(
            final String name, 
            final int sep, 
            final String smallSvfFieldName, 
            final String largeSvfFieldName
    ) {

        return sep < name.length() ? largeSvfFieldName : smallSvfFieldName;

    }

    //--- 内部クラス -------------------------------------------------------
    /**
     * パラメータクラス
     */
    private class Param {
        private final String _year;
        private final String _semester;
        private final String _gradeHrClass;
        private final String _grade;
        private final int _printSide;
        private final String _staffcd;
        private final String _fullImagePath;
        private final String _date;
        private final String[] _schregno;

        private String _attendDate;
        private String _attendMonth;

        /** 校長名 */
        private String _principalName;

        private String _schoolName;
        private String _staffName;
        private File _stamp;

        private final List _gradeAll;
        private final String _gradeIn;
        
        /** 教育課程コードを使用するか */
        private final String _useCurriculumcd;
        private final String _useVirus;
        private final String _useKoudome;
        
        /** 出欠状況取得引数 */
        private String _periodInState;
        private Map _attendSemesMap;
        private Map _hasuuMap;
        private boolean _semesFlg;
        private String _sDate;
        private final String SSEMESTER = "1";

        Param(
                final String year,
                final String semester,
                final String gradeHrClass,
                final String printSide,
                final String staffcd,
                final String fullImagePath,
                final String date,
                final String[] schregno,
                final String useCurriculumcd,
                final String useVirus,
                final String useKoudome
        ) {
            _year = year;
            _semester = semester;
            _gradeHrClass = gradeHrClass;
            _grade = gradeHrClass.substring(0, 2);
            _printSide = Integer.parseInt(printSide);
            _staffcd = staffcd;
            _fullImagePath = fullImagePath;
            _date = KNJ_EditDate.H_Format_Haifun(date);
            _schregno = schregno;
            _gradeAll = createGradeAll();
            _gradeIn = createGradeIn();
            _useCurriculumcd = useCurriculumcd;
            _useVirus = useVirus;
            _useKoudome = useKoudome;
        }

        public List createGradeAll() {
            final List rtn = new ArrayList();

            switch (getGrade()) {
            case 1:
                rtn.add("01");
                break;
            case 2:
                rtn.add("01");
                rtn.add("02");
                break;
            case 3:
                rtn.add("01");
                rtn.add("02");
                rtn.add("03");
                break;
            case 4:
                rtn.add("04");
                break;
            case 5:
                rtn.add("04");
                rtn.add("05");
                break;
            case 6:
                rtn.add("04");
                rtn.add("05");
                rtn.add("06");
                break;
            default:
                log.fatal("想定外の grade");
            }
            log.debug("gradeAll = " + rtn.size());
            return rtn;
        }

        public String createGradeIn() {
            int count = 0;
            StringBuffer stb = new StringBuffer();
            stb.append("(");
            for (final Iterator it = _gradeAll.iterator(); it.hasNext();) {
                final String grade = (String) it.next();
                if (0 < count) stb.append(",");
                stb.append("'");
                stb.append(grade);
                stb.append("'");
                count++;
            }
            stb.append(")");
            log.debug("学年=" + stb.toString());
            return stb.toString();
        }

        public List getForms() {
            final List rtn = new ArrayList();

            if (isJunior()) {
                setJuniroForms(rtn);
            } else {
                setHighForms(rtn);
            }
            return rtn;
        }

        private void setJuniroForms(final List rtn) {
            switch (_printSide) {
            case 1:
                juniorCoverAndAdvice(rtn);
                rtn.add(new FormJuniorOpinion1());
                rtn.add(new FormJuniorOpinion2());
                break;
            case 2:
                juniorCoverAndAdvice(rtn);
                break;
            case 3:
                rtn.add(new FormJuniorOpinion1());
                rtn.add(new FormJuniorOpinion2());
                break;
            case 4:
                rtn.add(new FormJuniorCover());
                break;
            case 5:
                rtn.add(createJuniorAdvice());
                break;
            case 6:
                rtn.add(new FormJuniorOpinion1());
                break;
            case 7:
                rtn.add(new FormJuniorOpinion2());
                break;
            default:
                log.fatal("想定外の printSide");
            }
        }

        private void juniorCoverAndAdvice(final List rtn) {
            rtn.add(new FormJuniorCover());
            rtn.add(createJuniorAdvice());
        }

        private Form createJuniorAdvice() {
            if (isJuniorAdviceMig()) {  // TODO: 2007/2008年度専用です。
                return new FormJuniorAdviceMig();
            } else {
                return new FormJuniorAdviceNew();
            }
        }

        private void setHighForms(final List rtn) {
            switch (_printSide) {
            case 2:
                rtn.add(new FormHighCover());
                rtn.add(createHighAdvice());
                break;
            case 4:
                rtn.add(new FormHighCover());
                break;
            case 5:
                rtn.add(createHighAdvice());
                break;
            default:
                log.fatal("高校での想定外の printSide");
            }
        }

        private Form createHighAdvice() {
            if (isHighAdviceOld()) {  // TODO: 2007/2008年度専用です。
                return new FormHighAdviceOld();
            } else {
                return new FormHighAdviceMig();
            }
        }

        public void loadConstant(final DB2UDB db2) {
            loadPrincipalNameAndSchoolName(db2);
            _staffName = createStaffName(db2);
            _stamp = createStamp();

            loadAttendDate(db2);
        }

        /**
         *  出欠集計端数処理用の日 _attendDate と月 _attendMonth をセットします。
         */
        public void loadAttendDate(final DB2UDB db2) {
            KNJDivideAttendDate obj = new KNJDivideAttendDate();
            obj.getDivideAttendDate(db2, _year, _semester, _date);
            _attendDate = obj.date;
            _attendMonth = obj.month;
        }

        public void loadPrincipalNameAndSchoolName(final DB2UDB db2) {
            _principalName = null;
            _schoolName = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            final String sql = "SELECT PRINCIPAL_NAME, SCHOOL_NAME "
                             + "FROM CERTIF_SCHOOL_DAT "
                             + "WHERE YEAR='" + _year + "' "
                             + "AND CERTIF_KINDCD='" + getCertifKindCd() + "'";
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    _principalName = rs.getString("PRINCIPAL_NAME");
                    _schoolName = rs.getString("SCHOOL_NAME");
                }
            } catch (final Exception ex) {
                log.error("校長名・学校名のロードでエラー:" + sql, ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            log.debug("校長名:" + _principalName);
            log.debug("学校名:" + _schoolName);
        }

        public String createStaffName(final DB2UDB db2) {
            String staffName = null;
            PreparedStatement ps = null;
            ResultSet rs = null;

            final String sql = "SELECT STAFFNAME FROM STAFF_MST WHERE STAFFCD='" + _staffcd + "'";
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    staffName = rs.getString("STAFFNAME");
                }
            } catch (final Exception ex) {
                log.error("担任名のロードでエラー:" + sql, ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }

            log.debug("担任名:" + staffName);
            return staffName;
        }

        public File createStamp() {
            final String fileName = _fullImagePath + getStampName();

            final File f = new File(fileName);
            log.debug("学校長印:" + f);

            return f;
        }

        private String getCertifKindCd() {
            return isJunior() ? "103" : "104";
        }

        private String getStampName() {
            return isJunior() ? "SCHOOLSTAMP_J.jpg" : "SCHOOLSTAMP_H.jpg";
        }

        public int getGrade() {
            return Integer.parseInt(_grade);
        }

        public boolean isJunior() {
            return getGrade() < 4;
        }

        public int getYear() {
            return Integer.parseInt(_year);
        }

        public boolean isJuniorAdviceMig() {
            return (getYear() == 2007 && getGrade() == 2) || 
                    (getYear() == 2007 && getGrade() == 3) || 
                    (getYear() == 2008 && getGrade() == 3);
        }

        public boolean isHighAdviceOld() {
            return (getYear() == 2007 && getGrade() == 5) || 
                    (getYear() == 2007 && getGrade() == 6) || 
                    (getYear() == 2008 && getGrade() == 6);
        }
        
        private KNJDefineCode setClasscode0(final DB2UDB db2) {
            KNJDefineCode definecode = null;
            try {
                definecode = new KNJDefineCode();
                definecode.defineCode(db2, _year);         //各学校における定数等設定
            } catch (Exception ex) {
                log.warn("semesterdiv-get error!", ex);
            }
            return definecode;
        }

        /**
         * 名称マスタ NAMECD1='Z010' NAMECD2='00'読込
         */
        private String setZ010Name1(DB2UDB db2) {
            String name1 = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement("SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' ");
                rs = ps.executeQuery();
                if (rs.next()) {
                    name1 = rs.getString("NAME1");
                }
            } catch (SQLException ex) {
                log.debug("getZ010 exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return name1;
        }

        public void loadAttendSemesArgument(DB2UDB db2) {
            try {
                loadSemester(db2, _year);
                // 出欠の情報
                final KNJDefineCode definecode0 = setClasscode0(db2);
                final String z010Name1 = setZ010Name1(db2);
                _periodInState = AttendAccumulate.getPeiodValue(db2, definecode0, _year, SSEMESTER, _semester);
                _attendSemesMap = AttendAccumulate.getAttendSemesMap(db2, z010Name1, _year);
                _hasuuMap = AttendAccumulate.getHasuuMap(_attendSemesMap, _sDate, _date); // _sDate: 年度開始日, _date: LOGIN_DATE
                _semesFlg = ((Boolean) _hasuuMap.get("semesFlg")).booleanValue();
                
                log.debug(" attendSemesMap = " + _attendSemesMap);
                log.debug(" hasuuMap = " + _hasuuMap);
                log.debug(" semesFlg = " + _semesFlg);
            } catch (Exception e) {
                log.debug("loadAttendSemesArgument exception", e);
            }
        }

        /**
         * 年度の開始日を取得する 
         */
        private void loadSemester(final DB2UDB db2, String year) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map map = new HashMap();
            final List list = new ArrayList();
            try {
                ps = db2.prepareStatement(sqlSemester(year));
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String semester = rs.getString("SEMESTER");
                    final String name = rs.getString("SEMESTERNAME");
                    map.put(semester, name);

                    final String sDate = rs.getString("SDATE");
                    list.add(sDate);
                }
            } catch (final Exception ex) {
                log.error("テスト項目のロードでエラー", ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }

            if (!list.isEmpty()) {
                _sDate = (String) list.get(0);
            }
            log.debug("年度の開始日=" + _sDate);
        }
        
        private String sqlSemester(String year) {
            final String sql;
            sql = "select"
                + "   SEMESTER,"
                + "   SEMESTERNAME,"
                + "   SDATE"
                + " from"
                + "   SEMESTER_MST"
                + " where"
                + "   YEAR='" + year + "'"
                + " order by SEMESTER"
            ;
            return sql;
        }
    }
    
    private class Student {
        private final String _schregno;
        private final String _grade;
        private final String _hrclass;
        private final String _attendno;
        private final String _name;

        Student(
                final String schregno,
                final String grade,
                final String hrclass,
                final String attendno,
                final String name
        ) {
            _schregno = schregno;
            _grade = grade;
            _hrclass = hrclass;
            _attendno = attendno;
            _name = name;
        }

        public int getGrade() {
            return Integer.parseInt(_grade);
        }

        public int getHrclass() {
            return Integer.parseInt(_hrclass);
        }

        public int getAttendno() {
            return Integer.parseInt(_attendno);
        }

        public boolean isJunior() {
            return getGrade() < 4;
        }

        public String toString() {
            return _schregno + ":" + _name;
        }
    }

    private abstract class Form {
        final String _fileName;
        boolean _hasAdvice = false;
        boolean _hasJuniorAdviceNew = false;
        boolean _hasJuniorAdviceMig = false;
        boolean _hasHighAdviceOld = false;
        int _maxLine = 40;

        Form(final String fileName) {
            _fileName = fileName;
        }

        public void printAdvice(
                final Vrw32alp svf,
                final Student student,
                final Param param,
                final DB2UDB db2
        ) {
            setStudentInfo(svf, student);
            setItemName(svf);
        }

        private void setItemName(final Vrw32alp svf) {
            for (int i = 1; i < 4; i++) {
                svf.VrsOut("ITEM" + String.valueOf(i) + "_1", "１学期");
                svf.VrsOut("ITEM" + String.valueOf(i) + "_2", "２学期");
                svf.VrsOut("ITEM" + String.valueOf(i) + "_3", "３学期");
                svf.VrsOut("ITEM" + String.valueOf(i) + "_4", "学年評定");
                svf.VrsOut("ITEM" + String.valueOf(i) + "_5", "単　位");
                svf.VrsOut("ITEM" + String.valueOf(i) + "_6", "各科小計");
            }
        }

        public void printConstant(final Vrw32alp svf, final Student student, final Param param) {
            setStudentInfo(svf, student);
            setSchoolInfo(svf, param);
        }

        public void setStudentInfo(final Vrw32alp svf, final Student student) {
            final int grade = student.isJunior() ? student.getGrade() : student.getGrade() - 3;
            svf.VrsOut("GRADE", String.valueOf(grade));
            svf.VrsOut("HR_CLASS", String.valueOf(student.getHrclass()));
            svf.VrsOut("ATTENDNO", String.valueOf(student.getAttendno()));
            if (null != student._name) {
                final String svfFieldName2 = _hasAdvice ? "NAME1" : "NAME2";
                final String svfFieldName = getOneSvfFieldName(student._name, 10, "NAME1", svfFieldName2);
                svf.VrsOut(svfFieldName, student._name);
            }
        }

        private void setSchoolInfo(final Vrw32alp svf, final Param param) {
            svf.VrsOut("NENDO", param._year + "年度");
            svf.VrsOut("SCHOOLNAME", param._schoolName);
            if (param._stamp.exists()) {
                svf.VrsOut("STAMP", param._stamp.toString());
            }
            if (null != param._staffName) {
                final String svfFieldName = getOneSvfFieldName(param._staffName, 10, "STAFFNAME1", "STAFFNAME2");
                svf.VrsOut(svfFieldName, param._staffName);
            }
            if (null != param._principalName) {
                final String svfFieldName = getOneSvfFieldName(param._principalName, 10, "PRINCIPALNAME1", "PRINCIPALNAME2");
                svf.VrsOut(svfFieldName, param._principalName);
            }
        }

        public void createOpinion(
                final Vrw32alp svf,
                final Student student,
                final Param param,
                final DB2UDB db2
        ) {
                PreparedStatement ps = null;
                ResultSet rs = null;

                final String sql = sqlOpinion(student, param);
                try {
                    log.debug("---------- 通知票所見 schno=" + student._schregno + " ----------");
                    ps = db2.prepareStatement(sql);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        setOpinion(svf, rs.getString("TOTALSTUDYTIME"), rs.getString("SEMESTER"));
                        log.debug("sem=" + rs.getString("SEMESTER"));
                    }
                } catch (final Exception ex) {
                    log.error("通知票所見のロードでエラー:" + sql, ex);
                } finally {
                    db2.commit();
                    DbUtils.closeQuietly(null, ps, rs);
                }
        }

        public void setOpinion(
                final Vrw32alp svf,
                final String rsTotalStudytime,
                final String rsSemester
        ) {
            final ArrayList arrlist = _knjobj.retDividString(rsTotalStudytime, 42, 7);
            if ( arrlist != null ) {
                for( int i = 0 ; i < arrlist.size() ; i++ ){
                    svf.VrsOut("OPINION"+ rsSemester+ "_"+ ( i+1 ),  (String)arrlist.get(i) );
                }
            }
        }

        public void createAttend(
                final Vrw32alp svf,
                final Student student,
                final Param param,
                final DB2UDB db2
        ) {
                PreparedStatement ps = null;
                ResultSet rs = null;

                final String sql = AttendAccumulate.getAttendSemesSql(
                        _param._semesFlg,
                        null,
                        _knjSchoolMst,
                        _param._year,
                        _param.SSEMESTER,
                        _param._semester,
                        (String) _param._hasuuMap.get("attendSemesInState"),
                        _param._periodInState,
                        (String) _param._hasuuMap.get("befDayFrom"),
                        (String) _param._hasuuMap.get("befDayTo"),
                        (String) _param._hasuuMap.get("aftDayFrom"),
                        (String) _param._hasuuMap.get("aftDayTo"),
                        _param._grade,
                        _param._gradeHrClass.substring(2,5),
                        student._schregno,
                        "SEMESTER",
                        _param._useCurriculumcd,
                        _param._useVirus,
                        _param._useKoudome
                );
                
                try {
                    log.debug("---------- 出欠 schno=" + student._schregno + " ----------");
                    ps = db2.prepareStatement(sql);
                    rs = ps.executeQuery();
                    final String len = param.isJunior() ? "" : String.valueOf(student.getGrade() - 3);
                    while (rs.next()) {
                        setAttend(svf, param, rs, len);
                        log.debug("sem=" + rs.getString("SEMESTER") + "  intGrade=" + student.getGrade());
                    }
                } catch (final Exception ex) {
                    log.error("出欠のロードでエラー:" + sql, ex);
                } finally {
                    db2.commit();
                    DbUtils.closeQuietly(null, ps, rs);
                }
        }

        public void createAttendOld(
                final Vrw32alp svf,
                final Student student,
                final Param param,
                final DB2UDB db2,
                final String grade
        ) {
                PreparedStatement ps = null;
                ResultSet rs = null;

                final String sql = sqlAttendOld(student, param, grade);
                try {
                    log.debug("---------- 出欠(過去) schno=" + student._schregno + " ----------");
                    ps = db2.prepareStatement(sql);
                    rs = ps.executeQuery();
                    final int intGrade = Integer.parseInt(grade);
                    final String len = intGrade < 4 ? "" : String.valueOf(intGrade - 3);
                    while (rs.next()) {
                        setAttend(svf, param, rs, len);
                        log.debug("sem=" + rs.getString("SEMESTER") + "  intGrade=" + intGrade);
                    }
                } catch (final Exception ex) {
                    log.error("出欠(過去)のロードでエラー:" + sql, ex);
                } finally {
                    db2.commit();
                    DbUtils.closeQuietly(null, ps, rs);
                }
        }

        public void setAttend(
                final Vrw32alp svf,
                final Param param,
                final ResultSet rs,
                final String len
        ) throws SQLException {
            int i = Integer.parseInt(rs.getString("SEMESTER"));
            if (i == 9) { i = 4; }
          
            svf.VrsOutn("LESSON" + len,   i,  rs.getString("LESSON"));
            svf.VrsOutn("SUSPEND" + len,  i,  String.valueOf(rs.getInt("SUSPEND") + rs.getInt("MOURNING") + ("true".equals(param._useVirus) ? rs.getInt("VIRUS") : 0) + ("true".equals(param._useKoudome) ? rs.getInt("KOUDOME") : 0) ));
            svf.VrsOutn("PRESENT" + len,  i,  rs.getString("MLESSON"));
            svf.VrsOutn("ABSENCE" + len,  i,  rs.getString("SICK"));
            svf.VrsOutn("ATTEND" + len,   i,  rs.getString("PRESENT"));
            svf.VrsOutn("LATE" + len,     i,  rs.getString("LATE"));
            svf.VrsOutn("LEAVE" + len,    i,  rs.getString("EARLY"));
        }

        public void createMockDat(
                final Vrw32alp svf,
                final Student student,
                final Param param,
                final DB2UDB db2
        ) {
                PreparedStatement ps = null;
                ResultSet rs = null;

                final String sql = sqlMockDat(student, param);
                try {
                    log.debug("---------- 実力テスト schno=" + student._schregno + " ----------");
                    ps = db2.prepareStatement(sql);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        final int intGrade = Integer.parseInt(rs.getString("GRADE"));
                        final String glen = String.valueOf(intGrade - 3);

                        if (rs.getString("CLASSCD").equals("01")) {
                            svf.VrsOut("APOINT" + glen + "_1", rs.getString("SCORE"));
                        } else if (rs.getString("CLASSCD").equals("02")) {
                            svf.VrsOut("APOINT" + glen + "_2", rs.getString("SCORE"));
                        } else if (rs.getString("CLASSCD").equals("03")) {
                            svf.VrsOut("APOINT" + glen + "_3", rs.getString("SCORE"));
                        } else if (rs.getString("CLASSCD").equals("04")) {
                            if (intGrade == 6) {
                                svf.VrsOut("APOINT" + glen + "_4", rs.getString("SCORE"));
                            } else {
                                svf.VrsOut("APOINT" + glen + "_2", rs.getString("SCORE"));
                            }
                        } else if (rs.getString("CLASSCD").equals("05")) {
                            svf.VrsOut("APOINT" + glen + "_5", rs.getString("SCORE"));
                        } else {
                            log.debug("実力テストの不正なデータ schno=" + rs.getString("SCHREGNO") + "  mock_subclass_cd=" + rs.getString("SUBCLASSCD"));
                        }
                        log.debug("classcd=" + rs.getString("CLASSCD") + "  intGrade=" + intGrade);
                    }
                } catch (final Exception ex) {
                    log.error("実力テストのロードでエラー:" + sql, ex);
                } finally {
                    db2.commit();
                    DbUtils.closeQuietly(null, ps, rs);
                }
        }

        public void createMockRank(
                final Vrw32alp svf,
                final Student student,
                final Param param,
                final DB2UDB db2
        ) {
                PreparedStatement ps = null;
                ResultSet rs = null;

                final String gradeIn = _hasJuniorAdviceMig ? "('"+param._grade+"')" : param._gradeIn;
                final String sql = sqlMockRank(student, param, gradeIn);
                try {
                    log.debug("---------- G値・A値・順位 schno=" + student._schregno + " ----------");
                    ps = db2.prepareStatement(sql);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        final int intGrade = Integer.parseInt(rs.getString("GRADE"));
                        final String glen = _hasJuniorAdviceMig ? "" : intGrade < 4 ? String.valueOf(intGrade) : String.valueOf(intGrade - 3);
                        if (rs.getString("DATA_DIV").equals("0")) {
                            if (!param.isJunior()) {
                                if (rs.getString("COURSE_DIV").equals("0") && intGrade < 6) {
                                    svf.VrsOut("TOTAL_APOINT" + glen, rs.getString("A_SCORE"));
                                } else if (rs.getString("COURSE_DIV").equals("1") && intGrade == 6) {
                                    svf.VrsOut("TOTAL_APOINT" + glen + "_1", rs.getString("A_SCORE"));
                                } else if (rs.getString("COURSE_DIV").equals("2") && intGrade == 6) {
                                    svf.VrsOut("TOTAL_APOINT" + glen + "_2", rs.getString("A_SCORE"));
                                }
                            } else {
                                svf.VrsOut("GPOINT" + glen, rs.getString("G_SCORE"));
                                svf.VrsOut("MAXGPOINT" + glen, rs.getString("G_PERFECT"));
                            }
                        } else if (rs.getString("DATA_DIV").equals("1")) {
                            if (_hasJuniorAdviceNew && rs.getString("YEAR").equals(param._year)) {
                                svf.VrsOut("TOTAL_GPOINT", rs.getString("G_SCORE"));
                                svf.VrsOut("TOTAL_MAXGPOINT", rs.getString("G_PERFECT"));
                            }
                            if (!param.isJunior()) {
                                svf.VrsOut("GPOINT" + glen, rs.getString("G_SCORE"));
                                svf.VrsOut("MAXGPOINT" + glen, rs.getString("G_PERFECT"));
                                if (rs.getString("COURSE_DIV").equals("0") && intGrade < 6) {
                                    svf.VrsOut("TOTAL_POINT" + glen, rs.getString("A_SCORE"));
                                    svf.VrsOut("TOTAL_MAXPOINT" + glen, rs.getString("A_PERFECT"));
                                    svf.VrsOut("ORDER" + glen, rs.getString("A_RANK"));
                                } else if (rs.getString("COURSE_DIV").equals("1") && intGrade == 6) {
                                    svf.VrsOut("TOTAL_POINT" + glen + "_1", rs.getString("A_SCORE"));
                                    svf.VrsOut("TOTAL_MAXPOINT" + glen + "_1", rs.getString("A_PERFECT"));
                                    svf.VrsOut("ORDER" + glen + "_1", rs.getString("A_RANK"));
                                } else if (rs.getString("COURSE_DIV").equals("2") && intGrade == 6) {
                                    svf.VrsOut("TOTAL_POINT" + glen + "_2", rs.getString("A_SCORE"));
                                    svf.VrsOut("TOTAL_MAXPOINT" + glen + "_2", rs.getString("A_PERFECT"));
                                    svf.VrsOut("ORDER" + glen + "_2", rs.getString("A_RANK"));
                                }
                            }
                        }
                        log.debug("datadiv=" + rs.getString("DATA_DIV") + "  coursediv=" + rs.getString("COURSE_DIV") + "  intGrade=" + intGrade);
                    }
                } catch (final Exception ex) {
                    log.error("G値・A値・A値順位のロードでエラー:" + sql, ex);
                } finally {
                    db2.commit();
                    DbUtils.closeQuietly(null, ps, rs);
                }
        }

        public void createSubclassName(
                final Vrw32alp svf,
                final Student student,
                final Param param,
                final DB2UDB db2
        ) {
                PreparedStatement ps = null;
                ResultSet rs = null;

                final String gradeIn = _hasJuniorAdviceMig ? "('"+param._grade+"')" : param._gradeIn;
                final String sql = sqlSubclassName(student, param, gradeIn, _hasHighAdviceOld);
                try {
                    log.debug("---------- 科目 schno=" + student._schregno + " ----------");
                    ps = db2.prepareStatement(sql);
                    rs = ps.executeQuery();
                    int count = 0;
                    int subCount = 0;
                    int setCount = 0;
                    int setFlg = 0;
                    String classcd = "";
                    String sortSubclasscd = "0";
                    while (rs.next()) {
                        log.debug(rs.getString("CLASSCD") + "：" + rs.getString("CLASSABBV") + "  文字数：" + rs.getString("CLASSABBV").length() + "  科目数：" + rs.getInt("SUBCNT") + " >>> " + rs.getString("SUBCLASSCD") + "：" + rs.getString("SUBCLASSNAME") + "  選択区分：" + rs.getString("ELECTDIV") + "  表示順：" + rs.getString("SHOWORDER3"));

                        if (!rs.getString("SORT_SUBCLASSCD").equals(sortSubclasscd)) {
                            if (!"0".equals(sortSubclasscd)) {
                                svf.VrEndRecord();
                                count++;
                            }
                            sortSubclasscd = rs.getString("SORT_SUBCLASSCD");

                            if (!rs.getString("CLASSCD").equals(classcd)) {
                                classcd = rs.getString("CLASSCD");
                                subCount = 0;
                                setCount = 0;
                                setFlg = 0;
                            }

                            if (param.isJunior()) {
                                svf.VrsOut("SUBCLASSNAME",  rs.getString("SUBCLASSNAME"));
                            } else {
                                svf.VrsOut("CLASSCD", rs.getString("CLASSCD"));
                                if (20 < rs.getString("SUBCLASSNAME").length()) {
                                    svf.VrsOut("SUBCLASSNAME3_1", rs.getString("SUBCLASSNAME").substring(0, 11));
                                    svf.VrsOut("SUBCLASSNAME3_2", rs.getString("SUBCLASSNAME").substring(11));
                                } else if (10 < rs.getString("SUBCLASSNAME").length()) {
                                    svf.VrsOut("SUBCLASSNAME2_1", rs.getString("SUBCLASSNAME").substring(0, 10));
                                    svf.VrsOut("SUBCLASSNAME2_2", rs.getString("SUBCLASSNAME").substring(10));
                                } else {
                                    svf.VrsOut("SUBCLASSNAME1", rs.getString("SUBCLASSNAME"));
                                }

                                int rsSubCount = rs.getInt("SUBCNT") * 2;
                                int rsClassLen = rs.getString("CLASSABBV").length();
                                if (rsSubCount > rsClassLen) {
                                    int keisan1 = (rsSubCount - (rsClassLen + rsClassLen - 1)) / 2;// 開始位置：0,1,2,...と数える
                                    if (keisan1 > 0) {
                                        if (setCount < rsClassLen && (subCount * 2) >= keisan1 && setFlg != 2) {
                                            svf.VrsOut("CLASSNAME1", rs.getString("CLASSABBV").substring(setCount));
                                            setCount++;
                                            setFlg = 1;
                                        }
                                        if (setCount < rsClassLen && (subCount * 2 + 1) >= keisan1 && setFlg != 1) {
                                            svf.VrsOut("CLASSNAME2", rs.getString("CLASSABBV").substring(setCount));
                                            setCount++;
                                            setFlg = 2;
                                        }
                                        log.debug("パターン１");
                                    } else {
                                        int keisan2 = (rsSubCount - rsClassLen) / 2;// 開始位置：0,1,2,...と数える
                                        if (setCount < rsClassLen && (subCount * 2) >= keisan2) {
                                            svf.VrsOut("CLASSNAME1", rs.getString("CLASSABBV").substring(setCount));
                                            setCount++;
                                        }
                                        if (setCount < rsClassLen && (subCount * 2 + 1) >= keisan2) {
                                            svf.VrsOut("CLASSNAME2", rs.getString("CLASSABBV").substring(setCount));
                                            setCount++;
                                        }
                                        log.debug("パターン２");
                                    }
                                } else {
                                    svf.VrsOut("CLASSNAME1", rs.getString("CLASSABBV").substring(setCount));
                                    setCount++;
                                    svf.VrsOut("CLASSNAME2", rs.getString("CLASSABBV").substring(setCount));
                                    setCount++;
                                    log.debug("パターン３");
                                }
                            }
                            subCount++;
                        }

                        final List grades = param._gradeAll;
                        for (final Iterator it = grades.iterator(); it.hasNext();) {
                            final String grade = (String) it.next();
                            if (_hasJuniorAdviceMig && !grade.equals(param._grade)) continue;
                            createScore(svf, student, param, db2, rs, grade);
                            log.debug("成績  学年：" + grade);
                        }
//                        svf.VrEndRecord();
//                        count++;
//                        subCount++;
                    }
                    while (count < _maxLine) {
                        svf.VrEndRecord();
                        count++;
                    }
                    log.debug("maxLine = " + _maxLine);
                } catch (final Exception ex) {
                    log.error("科目のロードでエラー:" + sql, ex);
                } finally {
                    db2.commit();
                    DbUtils.closeQuietly(null, ps, rs);
                }
        }

        private void createScore(
                final Vrw32alp svf,
                final Student student,
                final Param param,
                final DB2UDB db2,
                final ResultSet rsSub,
                final String grade
        ) throws SQLException {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final int intGrade = Integer.parseInt(grade);
            final String len = _hasJuniorAdviceMig ? "" : intGrade < 4 ? String.valueOf(intGrade) + "_" : String.valueOf(intGrade - 3) + "_";

            final String sql = sqlScore(student, param, grade, rsSub.getString("SUBCLASSCD"));
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    setScore(svf, rs, len);
                }
            } catch (final Exception ex) {
                log.error("成績のロードでエラー:" + sql, ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        public void setScore(
                final Vrw32alp svf,
                final ResultSet rs,
                final String len
        ) throws SQLException {
            svf.VrsOut("SCORE" + len + "1", rs.getString("SCORE1"));
            svf.VrsOut("SCORE" + len + "2", rs.getString("SCORE2"));
            svf.VrsOut("SCORE" + len + "3", rs.getString("SCORE3"));
            svf.VrsOut("SCORE" + len + "4", rs.getString("SCORE4"));
            svf.VrsOut("SCORE" + len + "5", rs.getString("CREDIT"));
            svf.VrsOut("SCORE" + len + "6", rs.getString("SCORE4_CREDIT"));
            svf.VrsOut("AVERAGE" + len + "1", rs.getString("AVG1"));
            svf.VrsOut("AVERAGE" + len + "2", rs.getString("AVG2"));
            svf.VrsOut("AVERAGE" + len + "3", rs.getString("AVG3"));
            svf.VrsOut("AVERAGE" + len + "4", rs.getString("AVG4"));
            svf.VrsOut("TOTAL" + len + "5", rs.getString("TOTAL5"));
            svf.VrsOut("TOTAL" + len + "6", rs.getString("TOTAL6"));
        }

        public void setForm(final Vrw32alp svf) {
            svf.VrSetForm(_fileName, 4);
        }

        public String toString() {
            return _fileName;
        }
    }

    // ============================================

    /**
     * 中学・表紙
     */
    private class FormJuniorCover extends Form {
        FormJuniorCover() {
            super(FORM_FILE1);
        }

        public void printConstant(final Vrw32alp svf, final Student student, final Param param) {
            super.printConstant(svf, student, param);
            svf.VrsOut("TITLE1", "成績通知表");
            svf.VrEndRecord();
        }

    }

    /**
     * 中学・通知票・新カリキュラム
     */
    private class FormJuniorAdviceNew extends Form {
        FormJuniorAdviceNew() {
            super(FORM_FILE3);
            _hasAdvice = true;
            _hasJuniorAdviceNew = true;
            _maxLine = 16;
        }

        public void printAdvice(
                final Vrw32alp svf,
                final Student student,
                final Param param,
                final DB2UDB db2
        ) {
            super.printAdvice(svf, student, param, db2);
            super.createOpinion(svf, student, param, db2);
            super.createAttend(svf, student, param, db2);
            super.createMockRank(svf, student, param, db2);
            super.createSubclassName(svf, student, param, db2);
        }
    }

    /**
     * 中学・通知票・移行カリキュラム
     */
    private class FormJuniorAdviceMig extends Form {
        FormJuniorAdviceMig() {
            super(FORM_FILE4);
            _hasAdvice = true;
            _hasJuniorAdviceMig = true;
            _maxLine = 12;
        }

        public void printAdvice(
                final Vrw32alp svf,
                final Student student,
                final Param param,
                final DB2UDB db2
        ) {
            super.setStudentInfo(svf, student);
            setItemName(svf);
            super.createOpinion(svf, student, param, db2);
            super.createAttend(svf, student, param, db2);
            super.createMockRank(svf, student, param, db2);
            super.createSubclassName(svf, student, param, db2);
        }

        private void setItemName(final Vrw32alp svf) {
            svf.VrsOut("ITEM1", "１学期");
            svf.VrsOut("ITEM2", "２学期");
            svf.VrsOut("ITEM3", "３学期");
            svf.VrsOut("ITEM4", "学年評定");
            svf.VrsOut("ITEM5", "単　位");
            svf.VrsOut("ITEM6", "各科小計");
        }
    }

    /**
     * 中学・所見・表
     */
    private class FormJuniorOpinion1 extends Form {
        FormJuniorOpinion1() {
            super(FORM_FILE1);
        }

        public void printConstant(final Vrw32alp svf, final Student student, final Param param) {
            super.printConstant(svf, student, param);
            svf.VrsOut("TITLE2", "保護者所見欄");
            svf.VrEndRecord();
        }
    }

    /**
     * 中学・所見・裏
     */
    private class FormJuniorOpinion2 extends Form {
        FormJuniorOpinion2() {
            super(FORM_FILE2);
        }

        public void printConstant(final Vrw32alp svf, final Student student, final Param param) {
            svf.VrsOut("MARK", "保護者印");
            svf.VrEndRecord();
        }
    }

    // ============================================

    /**
     * 高校・表紙
     */
    private class FormHighCover extends Form {
        FormHighCover() {
            super(FORM_FILE5);
        }

        public void printConstant(final Vrw32alp svf, final Student student, final Param param) {
            super.printConstant(svf, student, param);
            svf.VrEndRecord();
        }
    }

    /**
     * 高校・通知票・移行カリキュラム
     */
    private class FormHighAdviceMig extends Form {
        FormHighAdviceMig() {
            super(FORM_FILE6);
            _hasAdvice = true;
        }

        public void printAdvice(
                final Vrw32alp svf,
                final Student student,
                final Param param,
                final DB2UDB db2
        ) {
            super.printAdvice(svf, student, param, db2);
            super.createAttend(svf, student, param, db2);
            if (4 < student.getGrade()) {
                for (int i = 4; i < student.getGrade(); i++) {
                    String grade = "0" + String.valueOf(i);
                    super.createAttendOld(svf, student, param, db2, grade);
                }
            }
            super.createMockDat(svf, student, param, db2);
            super.createMockRank(svf, student, param, db2);
            super.createSubclassName(svf, student, param, db2);
        }
    }

    /**
     * 高校・通知票・旧カリキュラム
     */
    private class FormHighAdviceOld extends Form {
        FormHighAdviceOld() {
            super(FORM_FILE6);
            _hasAdvice = true;
            _hasHighAdviceOld = true;
        }

        public void printAdvice(
                final Vrw32alp svf,
                final Student student,
                final Param param,
                final DB2UDB db2
        ) {
            super.printAdvice(svf, student, param, db2);
            super.createAttend(svf, student, param, db2);
            if (4 < student.getGrade()) {
                for (int i = 4; i < student.getGrade(); i++) {
                    String grade = "0" + String.valueOf(i);
                    super.createAttendOld(svf, student, param, db2, grade);
                }
            }
            super.createMockDat(svf, student, param, db2);
            super.createMockRank(svf, student, param, db2);
            super.createSubclassName(svf, student, param, db2);
        }
    }
}
