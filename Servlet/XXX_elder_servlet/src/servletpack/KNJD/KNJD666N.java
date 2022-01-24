/**     プロジェクト/プロパティ/javaコード・スタイル/コード・テンプレート/パターン       ***/

/*
 * $Id: c53737ed954f2bce779b1331d9417844354d8da8 $
 *
 * 作成日: 2019/10/30
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
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;

public class KNJD666N {

    private static final Log log = LogFactory.getLog(KNJD666N.class);

    private static final String SEMEALL = "9";
    private static final String SELECT_CLASSCD_UNDER = "89";

    private static final String ALL3 = "333333";
    private static final String ALL5 = "555555";
    private static final String ALL9 = "999999";

    private static final String HYOTEI_TESTCD = "9990009";

    private static final int MAX_LINE = 50;

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

        if (!"1".equals(_param._sort)) {
        	svf.VrSetForm("KNJD666N.frm" , 1);
        } else {
        	svf.VrSetForm("KNJD666N_2.frm" , 1);
        }
        //明細部以外を印字
        printTitle(svf);

        String befGradeHrClass = "";
        int line = 1;
        for (Iterator iterator = studentList.iterator(); iterator.hasNext();) {
            final Student student = (Student) iterator.next();


            //ページ切り替え
            if("1".equals(_param._sort) && !befGradeHrClass.equals(student._grade + student._hrClass) && !"".equals(befGradeHrClass) || line > MAX_LINE) {
                line = 1;
                svf.VrEndPage();
                svf.VrSetForm("KNJD666N_2.frm" , 1);
                //明細部以外を印字
                printTitle(svf);
            }

            //成績一覧
            printSvfMain(db2, svf, student, line);

            svf.VrEndRecord();

            line++;
            befGradeHrClass = student._grade + student._hrClass;
            _hasData = true;
        }
        svf.VrEndPage();
    }

    private void printTitle(final Vrw32alp svf) {
        //明細部以外を印字

        //ヘッダ
        String tiele = _param._nendo +"　"+ _param._semesterName +"　"+ _param._testkindName +"　"+ "成績一覧";
        svf.VrsOut("TITLE", tiele); //タイトル
        String subject = _param._gradeName +"　"+_param._majorName+"　ソート区分：" + _param._sortKbn;
        svf.VrsOut("SUBJECT", subject); //条件
        svf.VrsOut("SCHOOL_NAME", _param._certifSchoolSchoolName); //学校名

        //フッター
        Calendar cl = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd(E) HH:mm");
        String footer = sdf.format(cl.getTime()) + "　(出力 "+ _param._printLogStaffcd + "　" + _param._printLogStaffName +")";
        svf.VrsOut("FOOTER", footer);
    }


    private void printSvfMain(final DB2UDB db2, final Vrw32alp svf, final Student student, final int line) {

        svf.VrsOutn("HR_NAME", line, student._hrClass.replaceFirst("^0+", "")); //組
        svf.VrsOutn("NO", line, student._attendno); //番号
        final String nameField = KNJ_EditEdit.getMS932ByteLength(student._name) > 30 ? "3" : KNJ_EditEdit.getMS932ByteLength(student._name) > 20 ? "2" : "1";
        svf.VrsOutn("NAME"+nameField, line, student._name); //氏名
        svf.VrsOutn("FINSCHOOL_NAME", line, student._finschoolName); //出身中学校


        //明細部
        final List subclassList = subclassListRemoveD026();
        Collections.sort(subclassList);

        //■成績一覧
        int idx = 1;
        for (Iterator itSubclass = subclassList.iterator(); itSubclass.hasNext();) {
            final SubclassMst subclassMst = (SubclassMst) itSubclass.next();

            final String subclassCd = subclassMst._subclasscd;

            if (!_param._printSublassMap.containsKey(subclassCd)) {
                continue;
            }
            final String subclassName = (String) _param._printSublassMap.get(subclassCd);
            svf.VrsOut("SUBCLASS_NAME"+ idx, subclassName); //教科名

            if (student._printSubclassMap.containsKey(subclassCd)) {
                final ScoreData scoreData = (ScoreData) student._printSubclassMap.get(subclassCd);
                if(scoreData != null) {
                    svf.VrsOut("CREDIT"+ idx, scoreData.credits(_param._sdiv)); //単位
                    svf.VrsOutn("VALUE"+ idx, line, scoreData.score(_param._sdiv)); //評価
                }
            }

            if ("1".equals(_param._sort)) {
            	final Map resultMap = getScoreData2(db2, subclassCd, student._grade, student._hrClass);
    	            svf.VrsOutn("VALUE"+ idx, 51, (String)resultMap.get("CLASS_SCORE")); //クラス合計
    	            svf.VrsOutn("VALUE"+ idx, 52, (String)resultMap.get("CLASS_AVG")); //クラス平均
    	            svf.VrsOutn("VALUE"+ idx, 53, (String)resultMap.get("SUBCLASS_AVG")); //科目平均
            }
            idx++;
        }

        //■成績一覧 合計の印字
        printTotal(svf, student, line);

    }

    private void printTotal(final Vrw32alp svf, final Student student, final int line) {
        //■成績一覧 合計の印字
        final String subclassCd = "999999";
        final ScoreData scoreData = (ScoreData) student._printSubclassMap.get(subclassCd);
        if(scoreData == null) return;

        svf.VrsOutn("AVERAGE", line, sishaGonyu(scoreData.avg(_param._sdiv))); //平均
        svf.VrsOutn("GRADE_RANK", line, scoreData.gradeRank(_param._sdiv)); //学年順位
        svf.VrsOutn("COURSE_RANK", line, scoreData.courseRank(_param._sdiv)); //コース順位
        svf.VrsOutn("COURSE_NAME", line, student._coursecodename); //コース名
    }

    private List subclassListRemoveD026() {
        final List retList = new ArrayList(_param._subclassMstMap.values());
        for (final Iterator it = retList.iterator(); it.hasNext();) {
            final SubclassMst subclassMst = (SubclassMst) it.next();
            if (_param._d026List.contains(subclassMst._subclasscd)) {
                it.remove();
            }
            if (_param._isNoPrintMoto &&  subclassMst._isMoto || !_param._isPrintSakiKamoku &&  subclassMst._isSaki) {
                it.remove();
            }
        }
        return retList;
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
                student._hrname = rs.getString("HR_NAME");
                student._staffname = StringUtils.defaultString(rs.getString("STAFFNAME"));
                student._staffname2 = StringUtils.defaultString(rs.getString("STAFFNAME2"));
                student._attendno = StringUtils.defaultString(rs.getString("ATTENDNO"));
                student._grade = rs.getString("GRADE");
                student._hrClass = rs.getString("HR_CLASS");
                student._coursecd = rs.getString("COURSECD");
                student._majorcd = rs.getString("MAJORCD");
                student._course = rs.getString("COURSE");
                student._majorname = rs.getString("MAJORNAME");
                student._coursecodename = rs.getString("COURSECODEABBV1");
                student._hrClassName1 = rs.getString("HR_CLASS_NAME1");
                student._entyear = rs.getString("ENT_YEAR");
                student._finschoolName = rs.getString("FINSCHOOL_NAME");
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
        stb.append("    AND T1.GRADE || T1.HR_CLASS IN " + SQLUtils.whereIn(true, _param._categorySelected));
        if (!"ALL".equals(_param._coursecode)) {
            stb.append("    AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE = '" + _param._coursecode + "' ");
        }
        stb.append("    ) ");
        //メイン表
        stb.append("     SELECT  REGD.SCHREGNO");
        stb.append("            ,REGD.SEMESTER ");
        stb.append("            ,BASE.NAME ");
        stb.append("            ,REGDG.SCHOOL_KIND ");
        stb.append("            ,REGDG.GRADE_NAME2 ");
        stb.append("            ,REGDH.HR_NAME ");
        stb.append("            ,STF1.STAFFNAME ");
        stb.append("            ,STF2.STAFFNAME AS STAFFNAME2 ");
        stb.append("            ,REGD.ATTENDNO ");
        stb.append("            ,REGD.GRADE ");
        stb.append("            ,REGD.HR_CLASS ");
        stb.append("            ,REGD.COURSECD ");
        stb.append("            ,REGD.MAJORCD ");
        stb.append("            ,REGD.COURSECD || REGD.MAJORCD || REGD.COURSECODE AS COURSE");
        stb.append("            ,MAJOR.MAJORNAME ");
        stb.append("            ,COURSECODE.COURSECODEABBV1 ");
        stb.append("            ,REGDH.HR_CLASS_NAME1 ");
        stb.append("            ,FISCALYEAR(BASE.ENT_DATE) AS ENT_YEAR ");
        stb.append("            ,FINSCH.FINSCHOOL_NAME ");
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
        stb.append("     LEFT JOIN COURSECODE_MST COURSECODE ON COURSECODE.COURSECODE = REGD.COURSECODE ");
        stb.append("     LEFT JOIN STAFF_MST STF2 ON STF2.STAFFCD = REGDH.TR_CD2 ");
        stb.append("     LEFT JOIN FINSCHOOL_MST FINSCH ON FINSCH.FINSCHOOLCD = BASE.FINSCHOOLCD ");
        if(!"1".equals(_param._sort)) {
            //出力順：組番号順 以外
            stb.append("     LEFT JOIN RECORD_RANK_SDIV_DAT SDIV ");
            stb.append("            ON SDIV.YEAR          = REGD.YEAR ");
            stb.append("           AND SDIV.SEMESTER || SDIV.TESTKINDCD || SDIV.TESTITEMCD || SDIV.SCORE_DIV = '" + _param._sdiv + "' ");
            stb.append("           AND SDIV.CLASSCD       = '99' ");
            stb.append("           AND SDIV.SCHOOL_KIND   = REGDG.SCHOOL_KIND ");
            stb.append("           AND SDIV.CURRICULUM_CD = '99' ");
            stb.append("           AND SDIV.SUBCLASSCD    = '999999' ");
            stb.append("           AND SDIV.SCHREGNO      = REGD.SCHREGNO ");
        }
        stb.append("     WHERE   REGD.YEAR = '" + _param._loginYear + "' ");
        if (SEMEALL.equals(_param._semester)) {
            stb.append("     AND REGD.SEMESTER = '" + _param._loginSemester + "' ");
        } else {
            stb.append("     AND REGD.SEMESTER = '" + _param._semester + "' ");
        }
        stb.append("         AND REGD.GRADE || REGD.HR_CLASS IN " + SQLUtils.whereIn(true, _param._categorySelected));
        stb.append("     ORDER BY ");
        if("2".equals(_param._sort)) {
            //出力順：10段階評価平均順
            stb.append("         SDIV.AVG DESC, ");
        } else if("3".equals(_param._sort)) {
            //出力順：学年順位
            stb.append("         SDIV.GRADE_RANK, ");
        } else if("4".equals(_param._sort)) {
            //出力順：コース順位
            stb.append("         SDIV.COURSE_RANK, ");
            stb.append("         SDIV.GRADE_RANK, ");
        }
        stb.append("         REGD.GRADE, ");
        stb.append("         REGD.HR_CLASS, ");
        stb.append("         REGD.ATTENDNO ");
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
        String _schoolKind;
        String _gradename;
        String _hrname;
        String _staffname;
        String _staffname2;
        String _attendno;
        String _grade;
        String _hrClass;
        String _coursecd;
        String _majorcd;
        String _course;
        String _majorname;
        String _coursecodename;
        String _hrClassName1;
        String _entyear;
        String _finschoolName;
        final Map _attendMap = new TreeMap();
        final Map _printSubclassMap = new TreeMap();
        final Map _attendSubClassMap = new HashMap();

        private void setSubclass(final DB2UDB db2) {
            final String scoreSql = prestatementSubclass();
            if (_param._isOutputDebug) {
                log.info(" scoreSql = " + scoreSql);
            }
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(scoreSql);
                rs = ps.executeQuery();
                while (rs.next()) {

                    final String classcd = rs.getString("CLASSCD");
                    final String classname = rs.getString("CLASSNAME");
                    final String subclassname = rs.getString("SUBCLASSNAME");

                    String subclasscd = rs.getString("SUBCLASSCD");
                    if (!"999999".equals(subclasscd)) {
                        subclasscd = rs.getString("CLASSCD") + "-" + rs.getString("SCHOOL_KIND") + "-" + rs.getString("CURRICULUM_CD") + "-" + rs.getString("SUBCLASSCD");
                    }

                    final String key = subclasscd;
                    if (!_printSubclassMap.containsKey(key)) {
                        _printSubclassMap.put(key, new ScoreData(classcd, classname, subclasscd, subclassname));
                    }
                    final ScoreData scoreData = (ScoreData) _printSubclassMap.get(key);
                    final String testcd = rs.getString("SEMESTER") + rs.getString("TESTKINDCD") + rs.getString("TESTITEMCD") + rs.getString("SCORE_DIV");
                    scoreData._creditsMap.put(testcd, StringUtils.defaultString(rs.getString("CREDITS")));
                    scoreData._scoreMap.put(testcd, StringUtils.defaultString(rs.getString("SCORE")));
                    scoreData._avgMap.put(testcd, StringUtils.defaultString(rs.getString("AVG")));
                    scoreData._gradeRankMap.put(testcd, StringUtils.defaultString(rs.getString("GRADE_RANK")));
                    scoreData._courseRankMap.put(testcd, StringUtils.defaultString(rs.getString("COURSE_RANK")));
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
            stb.append("         T2.YEAR     = '" + _param._loginYear + "'  ");
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
            stb.append("     AND T2.COURSECD || T2.MAJORCD || T2.COURSECODE = '" + _course + "' ");
            //成績明細データの表
            stb.append(" ) ,RECORD00_BASE AS( ");
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
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _param._loginYear + "'  ");
            stb.append("     AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '" + _param._sdiv + "' ");
            stb.append("     AND T1.SUBCLASSCD NOT LIKE '50%' ");
            stb.append("     AND T1.SUBCLASSCD NOT IN ('" + ALL3 + "', '" + ALL5 + "') ");
            stb.append(" UNION ");
            stb.append("     SELECT DISTINCT ");
            stb.append("         T1.YEAR, T1.SEMESTER, T1.TESTKINDCD, T1.TESTITEMCD, T1.SCORE_DIV, T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, T1.SCHREGNO ");
            stb.append("     FROM RECORD_SCORE_DAT T1 ");
            stb.append("     INNER JOIN (SELECT ");
            stb.append("                  SCHREGNO ");
            stb.append("                FROM ");
            stb.append("                  SCHNO T2 ");
            stb.append("                GROUP BY ");
            stb.append("                  SCHREGNO ");
            stb.append("             ) T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _param._loginYear + "'  ");
            stb.append("     AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '" + _param._sdiv + "' ");
            stb.append("     AND T1.SUBCLASSCD NOT LIKE '50%' ");
            stb.append("     AND (T1.SCORE IS NOT NULL OR T1.VALUE_DI IS NOT NULL) ");
            stb.append(" ) ,RECORD00 AS( ");
            stb.append("     SELECT DISTINCT ");
            stb.append("         T1.YEAR, T1.SEMESTER, T1.TESTKINDCD, T1.TESTITEMCD, T1.SCORE_DIV, T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, T1.SCHREGNO ");
            stb.append("     FROM RECORD00_BASE T1 ");
            stb.append(" ) ,RECORD0 AS( ");
            stb.append("     SELECT ");
            stb.append("              T1.YEAR, T1.SEMESTER, T1.TESTKINDCD, T1.TESTITEMCD, T1.SCORE_DIV, T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, T1.SCHREGNO ");
            stb.append("            , L1.CREDITS ");
            stb.append("            , CASE WHEN L3.VALUE_DI IS NOT NULL THEN L3.VALUE_DI ELSE CAST(L2.SCORE AS VARCHAR(3)) END AS SCORE ");
            stb.append("            , L2.AVG ");
            stb.append("            , L2.GRADE_RANK ");
            stb.append("            , L2.COURSE_RANK ");
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
            stb.append("   UNION ALL ");
            stb.append("     SELECT ");
            stb.append("       CAST(NULL AS VARCHAR(1)) AS CLASSNAME, ");
            stb.append("       CAST(NULL AS VARCHAR(1)) AS SUBCLASSNAME, ");
            stb.append("       T1.* ");
            stb.append("      FROM RECORD0 T1 ");
            stb.append("      LEFT JOIN ( ");
            stb.append("            SELECT   T1.YEAR, T1.SEMESTER, T1.TESTKINDCD, T1.TESTITEMCD, T1.SCORE_DIV ");
            stb.append("            FROM RECORD_RANK_SDIV_DAT T1 ");
            stb.append("            INNER JOIN SCHREG_REGD_DAT REGD ON REGD.SCHREGNO = T1.SCHREGNO ");
            stb.append("                AND REGD.YEAR = T1.YEAR ");
            if (SEMEALL.equals(_param._semester)) {
                stb.append("            AND REGD.SEMESTER = '" + _param._loginSemester + "' ");
            } else {
                stb.append("            AND REGD.SEMESTER = T1.SEMESTER ");
            }
            stb.append("            WHERE ");
            stb.append("                T1.YEAR = '" + _param._loginYear + "' ");
            stb.append("                AND T1.SUBCLASSCD = '" + ALL9 + "' ");
            stb.append("            GROUP BY T1.YEAR, T1.SEMESTER, T1.TESTKINDCD, T1.TESTITEMCD, T1.SCORE_DIV  ");
            stb.append("      ) T2 ON T2.YEAR = T1.YEAR ");
            stb.append("          AND T2.SEMESTER = T1.SEMESTER ");
            stb.append("          AND T2.TESTKINDCD = T1.TESTKINDCD ");
            stb.append("          AND T2.TESTITEMCD = T1.TESTITEMCD ");
            stb.append("          AND T2.SCORE_DIV = T1.SCORE_DIV ");
            stb.append("      WHERE T1.SUBCLASSCD = '" + ALL9 + "' ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("       T1.* ");
            stb.append(" FROM RECORD T1 ");
            stb.append(" ORDER BY ");
            stb.append("     T1.SUBCLASSCD ");
            stb.append("    ,T1.SCHOOL_KIND ");
            stb.append("    ,T1.CURRICULUM_CD ");

            return stb.toString();
        }

    }

    private static class ScoreData {
        final String _classcd;
        final String _classname;
        final String _subclasscd;
        final String _subclassname;
        final Map _creditsMap = new HashMap(); // 単位数
        final Map _scoreMap = new HashMap(); // 得点
        final Map _avgMap = new HashMap(); // 個人の平均点 (999999のみ使用)
        final Map _gradeRankMap = new HashMap(); // 学年順位 (999999のみ使用)
        final Map _courseRankMap = new HashMap(); // コース順位 (999999のみ使用)

        private ScoreData(
                final String classcd,
                final String classname,
                final String subclasscd,
                final String subclassname
        ) {
            _classcd = classcd;
            _classname = classname;
            _subclasscd = subclasscd;
            _subclassname = subclassname;
        }

        public String credits(final String sdiv) {
            return StringUtils.defaultString((String) _creditsMap.get(sdiv), "");
        }

        public String score(final String sdiv) {
            return StringUtils.defaultString((String) _scoreMap.get(sdiv), "");
        }

        public String avg(final String sdiv) {
            return StringUtils.defaultString((String) _avgMap.get(sdiv), "");
        }

        public String gradeRank(final String sdiv) {
            return StringUtils.defaultString((String) _gradeRankMap.get(sdiv), "");
        }

        public String courseRank(final String sdiv) {
            return StringUtils.defaultString((String) _courseRankMap.get(sdiv), "");
        }
    }


    //組番号順を選択した場合のみ表示される、"クラス合計", "クラス平均", "科目平均"
    private Map getScoreData2 (final DB2UDB db2, final String subclassCd, final String grade, final String hr_class) {
        final StringBuffer stb = new StringBuffer();

        stb.append(" SELECT ");
        stb.append("   T1.SCORE AS CLASS_SCORE ");
        stb.append("   , CAST(ROUND(T1.AVG, 1) AS DECIMAL(5, 1)) AS CLASS_AVG ");
        stb.append("   , CAST(ROUND(T2.AVG, 1) AS DECIMAL(5, 1)) AS SUBCLASS_AVG ");
        stb.append(" FROM ");
        stb.append("   RECORD_AVERAGE_SDIV_DAT T1 ");
        stb.append("   LEFT JOIN RECORD_AVERAGE_SDIV_DAT T2 ");
        stb.append("     ON T1.YEAR = T2.YEAR ");
        stb.append("     AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = T2.SEMESTER || T2.TESTKINDCD || ");
        stb.append("     T2.TESTITEMCD || T2.SCORE_DIV ");
        stb.append("     AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = T2.CLASSCD ");
        stb.append("      || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD ");
        stb.append("     AND T1.GRADE = T2.GRADE ");
        stb.append("   	 AND T1.AVG_DIV = '2' ");
        stb.append("     AND T2.AVG_DIV = '1' ");

        stb.append(" WHERE ");
        stb.append("   T1.YEAR = '"+ _param._loginYear +"' ");
        stb.append("   AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '"+ _param._semester + _param._testkindItemcd +"'");
        stb.append("   AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '"+ subclassCd +"' ");
        stb.append("   AND T1.GRADE = '"+ grade +"' ");
        stb.append("   AND T1.HR_CLASS = '"+ hr_class +"' ");
        stb.append("   AND T2.HR_CLASS = '000' ");
        if (_param._coursecode.equals("ALL")) {
            stb.append("   AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE = '00000000' ");
        } else {
            stb.append("   AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE = '"+ _param._coursecode +"' ");
        }

        final String sql =  stb.toString();

        PreparedStatement ps = null;
        ResultSet rs = null;
        Map resultMap = new HashMap() ;

        try {
        	log.debug(sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            rs.next();
            resultMap.put("CLASS_SCORE", StringUtils.defaultString(rs.getString("CLASS_SCORE")));
            resultMap.put("CLASS_AVG", StringUtils.defaultString(rs.getString("CLASS_AVG")));
            resultMap.put("SUBCLASS_AVG", StringUtils.defaultString(rs.getString("SUBCLASS_AVG")));

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }

        return resultMap;
    }



    private static class Semester {
        final String _semester;
        final String _semestername;
        final DateRange _dateRange;
        final List _testItemList;
        final List _semesterDetailList;
        public Semester(final String semester, final String semestername, final String sdate, final String edate) {
            _semester = semester;
            _semestername = semestername;
            _dateRange = new DateRange(_semester, _semestername, sdate, edate);
            _testItemList = new ArrayList();
            _semesterDetailList = new ArrayList();
        }
        public int getTestItemIdx(final TestItem testItem) {
            return _testItemList.indexOf(testItem);
        }
        public int getSemesterDetailIdx(final SemesterDetail semesterDetail) {
//          log.debug(" semesterDetail = " + semesterDetail + " , " + _semesterDetailList.indexOf(semesterDetail));
          return _semesterDetailList.indexOf(semesterDetail);
        }

        public int compareTo(final Object o) {
            if (!(o instanceof Semester)) {
                return 0;
            }
            Semester s = (Semester) o;
            return _semester.compareTo(s._semester);
        }
    }

    private static class SubclassMst implements Comparable {
        final String _specialDiv;
        final String _classcd;
        final String _subclasscd;
        final String _classabbv;
        final String _classname;
        final String _subclassname;
        final Integer _classShoworder3;
        final Integer _subclassShoworder3;
        final boolean _isSaki;
        final boolean _isMoto;
        final String _calculateCreditFlg;
        public SubclassMst(final String specialDiv, final String classcd, final String subclasscd, final String classabbv, final String classname, final String subclassabbv, final String subclassname,
                final Integer classShoworder3,
                final Integer subclassShoworder3,
                final boolean isSaki, final boolean isMoto, final String calculateCreditFlg) {
            _specialDiv = specialDiv;
            _classcd = classcd;
            _subclasscd = subclasscd;
            _classabbv = classabbv;
            _classname = classname;
            _subclassname = subclassname;
            _classShoworder3 = classShoworder3;
            _subclassShoworder3 = subclassShoworder3;
            _isSaki = isSaki;
            _isMoto = isMoto;
            _calculateCreditFlg = calculateCreditFlg;
        }
        public int compareTo(final Object o) {
            final SubclassMst mst = (SubclassMst) o;
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

    private static class SemesterDetail implements Comparable {
        final Semester _semester;
        final String _cdSemesterDetail;
        final String _semestername;
        final String _sdate;
        final String _edate;
        public SemesterDetail(Semester semester, String cdSemesterDetail, String semestername, final String sdate, final String edate) {
            _semester = semester;
            _cdSemesterDetail = cdSemesterDetail;
            _semestername = StringUtils.defaultString(semestername);
            _sdate = sdate;
            _edate = edate;
        }
        public int compareTo(final Object o) {
            if (!(o instanceof SemesterDetail)) {
                return 0;
            }
            SemesterDetail sd = (SemesterDetail) o;
            int rtn;
            rtn = _semester.compareTo(sd._semester);
            if (rtn != 0) {
                return rtn;
            }
            rtn = _cdSemesterDetail.compareTo(sd._cdSemesterDetail);
            return rtn;
        }
        public String toString() {
            return "SemesterDetail(" + _semester._semester + ", " + _cdSemesterDetail + ", " + _semestername + ")";
        }
    }

    private static class TestItem {
        final String _year;
        final Semester _semester;
        final String _testkindcd;
        final String _testitemcd;
        final String _scoreDiv;
        final String _testitemname;
        final String _testitemabbv1;
        final String _sidouInput;
        final String _sidouInputInf;
        final SemesterDetail _semesterDetail;
        boolean _isGakunenKariHyotei;
        int _printKettenFlg; // -1: 表示しない（仮評定）、1: 値が1をカウント（学年評定）、2:換算した値が1をカウント（評価等）
        public TestItem(final String year, final Semester semester, final String testkindcd, final String testitemcd, final String scoreDiv,
                final String testitemname, final String testitemabbv1, final String sidouInput, final String sidouInputInf,
                final SemesterDetail semesterDetail) {
            _year = year;
            _semester = semester;
            _testkindcd = testkindcd;
            _testitemcd = testitemcd;
            _scoreDiv = scoreDiv;
            _testitemname = testitemname;
            _testitemabbv1 = testitemabbv1;
            _sidouInput = sidouInput;
            _sidouInputInf = sidouInputInf;
            _semesterDetail = semesterDetail;
        }
        public String getTestcd() {
            return _semester._semester +_testkindcd +_testitemcd + _scoreDiv;
        }
        public String toString() {
            return "TestItem(" + _semester._semester + _testkindcd + _testitemcd + "(" + _scoreDiv + "))";
        }
    }


    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 72468 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _loginYear;
        final String _loginSemester;
        final String _ctrlDate;
        final String _prgid;
        final String _printLogStaffcd;

        final String _semester;
        final String _testkindItemcd;
        final String _grade;
        final String _coursecode;
        final String[] _categorySelected;
        final String _sort;
        final String _sortKbn;

        final String _nendo;
        final String _semesterName;
        final String _schoolKind;
        final String _sdiv;
        final String _testkindName;
        final String _gradeName;
        final String _majorName;
        final String _printLogStaffName;

        final boolean _semes2Flg;
        final boolean _semes3Flg;
        final boolean _semes9Flg;
        private final Map _testItemMap;
        private final Map _printSublassMap;

        private String _certifSchoolSchoolName;
        private String _certifSchoolJobName;
        private String _certifSchoolPrincipalName;

        /** 端数計算共通メソッド引数 */
        private final Map _attendParamMap;

        private final List _semesterList;
        private Map _semesterMap;
        private final Map _semesterDetailMap;
        private Map _subclassMstMap;
        private List _d026List = new ArrayList();
        private Map _attendRanges;

        final String _documentroot;
        final String _imagepath;
        final String _schoolLogoImagePath;
        final String _backSlashImagePath;
        final String _whiteSpaceImagePath;
        final String _schoolStampPath;

        private boolean _isNoPrintMoto;
        private boolean _isPrintSakiKamoku;

        private boolean _isOutputDebug;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _loginYear = request.getParameter("YEAR");
            _loginSemester = request.getParameter("LOGIN_SEMESTER");
            _ctrlDate = request.getParameter("CTRL_DATE").replace('/', '-');
            _prgid = request.getParameter("PRGID");
            _printLogStaffcd = request.getParameter("PRINT_LOG_STAFFCD");

            _semester = request.getParameter("SEMESTER");
            _testkindItemcd = request.getParameter("TESTKIND_ITEMCD");
            _grade = request.getParameter("GRADE");
            _coursecode = request.getParameter("COURSECODE");
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _sort = request.getParameter("SORT");
            _sortKbn = ("1".equals(_sort)) ? "組番号順" : ("2".equals(_sort)) ? "10段階評価平均順" : ("3".equals(_sort)) ? "学年順位" : "コース区分";

            _nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_loginYear)) + "年度";
            _semesterName = getSemesterName(db2);
            _schoolKind = request.getParameter("SCHOOL_KIND");
            _sdiv = _semester + _testkindItemcd;
            _testkindName = getTestitemName(db2);
            _gradeName = getGradeName(db2);
            _majorName = getMajorName(db2);
            _printLogStaffName = getStaffName(db2);

            _semes2Flg = "2".equals(_semester) || "3".equals(_semester) || "9".equals(_semester) ? true : false;
            _semes3Flg = "3".equals(_semester) || "9".equals(_semester) ? true : false;
            _semes9Flg = "9".equals(_semester) ? true : false;
            loadNameMstD026(db2);
            loadNameMstD016(db2);
            setPrintSakiKamoku(db2);

            _testItemMap = settestItemMap(db2);
            _printSublassMap = setPrintSubClassMap(db2);

            setCertifSchoolDat(db2);

            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
            _attendParamMap.put("grade", _grade);
            _attendParamMap.put("useTestCountflg", "TESTITEM_MST_COUNTFLG_NEW_SDIV");
            _attendParamMap.put("useCurriculumcd", "1");

            _semesterList = getSemesterList(db2);
            _semesterMap = loadSemester(db2);
            _semesterDetailMap = new HashMap();
            _attendRanges = new HashMap();
            for (final Iterator it = _semesterMap.keySet().iterator(); it.hasNext();) {
                final String semester = (String) it.next();
                final Semester oSemester = (Semester) _semesterMap.get(semester);
                _attendRanges.put(semester, oSemester._dateRange);
            }
            setSubclassMst(db2);

            _documentroot = request.getParameter("DOCUMENTROOT");
            final String sqlControlMst = " SELECT IMAGEPATH FROM CONTROL_MST WHERE CTRL_NO = '01' ";
            _imagepath = KnjDbUtils.getOne(KnjDbUtils.query(db2, sqlControlMst));
            _schoolLogoImagePath = getImageFilePath("SCHOOLLOGO.jpg");
            _backSlashImagePath = getImageFilePath("slash_bs.jpg");
            _whiteSpaceImagePath = getImageFilePath("whitespace.png");
            _schoolStampPath = getImageFilePath("SCHOOLSTAMP.bmp");

            _isOutputDebug = "1".equals(getDbPrginfoProperties(db2, "outputDebug"));
        }

        private static String getDbPrginfoProperties(final DB2UDB db2, final String propName) {
            return KnjDbUtils.getOne(KnjDbUtils.query(db2, " SELECT VALUE FROM PRGINFO_PROPERTIES WHERE PROGRAMID = 'KNJD187N' AND NAME = '" + propName + "' "));
        }

        /**
         * 年度の開始日を取得する
         */
        private Map loadSemester(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map map = new TreeMap();
            try {
                final String sql = "select"
                        + "   SEMESTER,"
                        + "   SEMESTERNAME,"
                        + "   SDATE,"
                        + "   EDATE"
                        + " from"
                        + "   V_SEMESTER_GRADE_MST"
                        + " where"
                        + "   YEAR='" + _loginYear + "'"
                        + "   AND GRADE='" + _grade + "'"
                        + " order by SEMESTER"
                    ;

                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    map.put(rs.getString("SEMESTER"), new Semester(rs.getString("SEMESTER"), rs.getString("SEMESTERNAME"), rs.getString("SDATE"), rs.getString("EDATE")));
                }
            } catch (final Exception ex) {
                log.error("テスト項目のロードでエラー", ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return map;
        }

        private void setSubclassMst(
                final DB2UDB db2
        ) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            _subclassMstMap = new LinkedMap();
            try {
                String sql = "";
                sql += " WITH REPL AS ( ";
                sql += " SELECT '1' AS DIV, COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || COMBINED_SUBCLASSCD AS SUBCLASSCD, CAST(NULL AS VARCHAR(1)) AS CALCULATE_CREDIT_FLG FROM SUBCLASS_REPLACE_COMBINED_DAT WHERE YEAR = '" + _loginYear + "' GROUP BY COMBINED_CLASSCD || '-' || COMBINED_SCHOOL_KIND || '-' || COMBINED_CURRICULUM_CD || '-' || COMBINED_SUBCLASSCD ";
                sql += " UNION ";
                sql += " SELECT '2' AS DIV, ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ATTEND_SUBCLASSCD AS SUBCLASSCD, MAX(CALCULATE_CREDIT_FLG) AS CALCULATE_CREDIT_FLG FROM SUBCLASS_REPLACE_COMBINED_DAT WHERE YEAR = '" + _loginYear + "' GROUP BY ATTEND_CLASSCD || '-' || ATTEND_SCHOOL_KIND || '-' || ATTEND_CURRICULUM_CD || '-' || ATTEND_SUBCLASSCD ";
                sql += " ) ";
                sql += " SELECT ";
                sql += " VALUE(T2.SPECIALDIV, '0') AS SPECIALDIV, ";
                sql += " T1.CLASSCD, ";
                sql += " T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ";
                sql += " T2.CLASSABBV, VALUE(T2.CLASSORDERNAME2, T2.CLASSNAME) AS CLASSNAME, T1.SUBCLASSABBV, VALUE(T1.SUBCLASSORDERNAME2, T1.SUBCLASSNAME) AS SUBCLASSNAME, ";
                sql += " CASE WHEN L1.SUBCLASSCD IS NOT NULL THEN 1 END AS IS_SAKI, ";
                sql += " CASE WHEN L2.SUBCLASSCD IS NOT NULL THEN 1 END AS IS_MOTO, ";
                sql += " L2.CALCULATE_CREDIT_FLG, ";
                sql += " VALUE(T2.SHOWORDER3, 999) AS CLASS_SHOWORDER3, ";
                sql += " VALUE(T1.SHOWORDER3, 999) AS SUBCLASS_SHOWORDER3 ";
                sql += " FROM SUBCLASS_MST T1 ";
                sql += " LEFT JOIN REPL L1 ON L1.DIV = '1' AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = L1.SUBCLASSCD ";
                sql += " LEFT JOIN REPL L2 ON L2.DIV = '2' AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = L2.SUBCLASSCD ";
                sql += " LEFT JOIN CLASS_MST T2 ON T2.CLASSCD = T1.CLASSCD AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final boolean isSaki = "1".equals(rs.getString("IS_SAKI"));
                    final boolean isMoto = "1".equals(rs.getString("IS_MOTO"));
                    final SubclassMst mst = new SubclassMst(rs.getString("SPECIALDIV"), rs.getString("CLASSCD"), rs.getString("SUBCLASSCD"), rs.getString("CLASSABBV"), rs.getString("CLASSNAME"), rs.getString("SUBCLASSABBV"), rs.getString("SUBCLASSNAME"), new Integer(rs.getInt("CLASS_SHOWORDER3")), new Integer(rs.getInt("SUBCLASS_SHOWORDER3")), isSaki, isMoto, rs.getString("CALCULATE_CREDIT_FLG"));
                    _subclassMstMap.put(rs.getString("SUBCLASSCD"), mst);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        private String getSemesterName(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            String rtn = "";
            try {
                final StringBuffer sql = new StringBuffer();
                sql.append(" SELECT SEMESTERNAME ");
                sql.append(" FROM SEMESTER_MST ");
                sql.append(" WHERE YEAR = '" + _loginYear + "' AND SEMESTER = '"+ _semester +"' ");
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {

                    rtn = StringUtils.defaultString(rs.getString("SEMESTERNAME"));
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
            return rtn;
        }

        private String getTestitemName(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            String rtn = "";
            try {
                final StringBuffer sql = new StringBuffer();
                sql.append(" SELECT TESTITEMNAME ");
                sql.append(" FROM TESTITEM_MST_COUNTFLG_NEW_SDIV ");
                sql.append(" WHERE YEAR = '" + _loginYear + "' AND SEMESTER || TESTKINDCD || TESTITEMCD || SCORE_DIV = '"+ _sdiv +"' ");
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {

                    rtn = StringUtils.defaultString(rs.getString("TESTITEMNAME"));
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
            return rtn;
        }

        private String getGradeName(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            String rtn = "";
            try {
                final StringBuffer sql = new StringBuffer();
                sql.append(" SELECT GRADE_NAME2 ");
                sql.append(" FROM SCHREG_REGD_GDAT ");
                sql.append(" WHERE YEAR = '" + _loginYear + "' AND GRADE = '"+ _grade +"' ");
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {

                    rtn = StringUtils.defaultString(rs.getString("GRADE_NAME2"));
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
            return rtn;
        }

        private String getMajorName(final DB2UDB db2) {
            if("ALL".equals(_coursecode)) {
                return "全て";
            }
            final String coursecd = _coursecode.substring(0, 1);
            final String majorcd = _coursecode.substring(1, 4);
            PreparedStatement ps = null;
            ResultSet rs = null;
            String rtn = "";
            try {
                final StringBuffer sql = new StringBuffer();
                sql.append(" SELECT MAJORNAME ");
                sql.append(" FROM MAJOR_MST ");
                sql.append(" WHERE COURSECD = '" + coursecd + "' AND MAJORCD = '"+ majorcd +"' ");
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {

                    rtn = StringUtils.defaultString(rs.getString("MAJORNAME"));
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
            return rtn;
        }

        private String getStaffName(final DB2UDB db2) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            String rtn = "";
            try {
                final StringBuffer sql = new StringBuffer();
                sql.append(" SELECT STAFFNAME ");
                sql.append(" FROM STAFF_MST ");
                sql.append(" WHERE STAFFCD = '" + _printLogStaffcd + "' ");
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {

                    rtn = StringUtils.defaultString(rs.getString("STAFFNAME"));
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
            return rtn;
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

        private Map setPrintSubClassMap(final DB2UDB db2) {
            //TODO
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map map = new TreeMap();
            try {
                final StringBuffer sql = new StringBuffer();
                sql.append(" WITH SUBCLASS_GET_DAT AS ( ");
                sql.append(" SELECT ");
                sql.append("   SDIV.CLASSCD || '-' || SDIV.SCHOOL_KIND || '-' || SDIV.CURRICULUM_CD || '-' || SDIV.SUBCLASSCD AS SUBCLASSCD, ");
                sql.append("   T2.SUBCLASSABBV ");
                sql.append(" FROM ");
                sql.append("   SCHREG_REGD_DAT REGD ");
                sql.append("   INNER JOIN RECORD_SCORE_DAT SDIV ");
                sql.append("           ON SDIV.YEAR     = REGD.YEAR ");
                sql.append("          AND SDIV.SCHREGNO = REGD.SCHREGNO ");
                sql.append("          AND SDIV.SEMESTER || SDIV.TESTKINDCD || SDIV.TESTITEMCD || SDIV.SCORE_DIV = '" + _sdiv + "' ");
                sql.append("          AND SDIV.SUBCLASSCD <= '" + SELECT_CLASSCD_UNDER + "' ");
                sql.append("   LEFT JOIN SUBCLASS_MST T2 ");
                sql.append("          ON T2.CLASSCD       = SDIV.CLASSCD ");
                sql.append("         AND T2.SCHOOL_KIND   = SDIV.SCHOOL_KIND ");
                sql.append("         AND T2.CURRICULUM_CD = SDIV.CURRICULUM_CD ");
                sql.append("         AND T2.SUBCLASSCD    = SDIV.SUBCLASSCD ");
                sql.append(" WHERE ");
                sql.append("   REGD.YEAR = '" + _loginYear + "' ");
                sql.append("   AND REGD.GRADE      = '" + _grade + "' ");
                sql.append("   AND REGD.SEMESTER   = '" + _semester + "' ");
                sql.append(" UNION ");
                sql.append(" SELECT ");
                sql.append("   SDIV.CLASSCD || '-' || SDIV.SCHOOL_KIND || '-' || SDIV.CURRICULUM_CD || '-' || SDIV.SUBCLASSCD AS SUBCLASSCD, ");
                sql.append("   T2.SUBCLASSABBV ");
                sql.append(" FROM ");
                sql.append("   SCHREG_REGD_DAT REGD ");
                sql.append("   INNER JOIN RECORD_RANK_SDIV_DAT SDIV ");
                sql.append("           ON SDIV.YEAR     = REGD.YEAR ");
                sql.append("          AND SDIV.SCHREGNO = REGD.SCHREGNO ");
                sql.append("          AND SDIV.SEMESTER || SDIV.TESTKINDCD || SDIV.TESTITEMCD || SDIV.SCORE_DIV = '" + _sdiv + "' ");
                sql.append("          AND SDIV.SUBCLASSCD <= '" + SELECT_CLASSCD_UNDER + "' ");
                sql.append("   LEFT JOIN SUBCLASS_MST T2 ");
                sql.append("          ON T2.CLASSCD       = SDIV.CLASSCD ");
                sql.append("         AND T2.SCHOOL_KIND   = SDIV.SCHOOL_KIND ");
                sql.append("         AND T2.CURRICULUM_CD = SDIV.CURRICULUM_CD ");
                sql.append("         AND T2.SUBCLASSCD    = SDIV.SUBCLASSCD ");
                sql.append(" WHERE ");
                sql.append("   REGD.YEAR = '" + _loginYear + "' ");
                sql.append("   AND REGD.GRADE      = '" + _grade + "' ");
                sql.append("   AND REGD.SEMESTER   = '" + _semester + "' ");
                sql.append(" ) ");
                sql.append(" SELECT DISTINCT SUBCLASSCD, SUBCLASSABBV FROM SUBCLASS_GET_DAT ");
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String subClassCd = rs.getString("SUBCLASSCD");
                    final String subClassName = StringUtils.defaultString(rs.getString("SUBCLASSABBV"));
                    if (!map.containsKey(subClassCd)) {
                        map.put(subClassCd, subClassName);
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

        private List getSemesterList(DB2UDB db2) {
            final List list = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final String sql = " SELECT * FROM SEMESTER_MST WHERE YEAR = '" + _loginYear + "' ORDER BY SEMESTER ";
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String semester = rs.getString("SEMESTER");
                    final String semestername = rs.getString("SEMESTERNAME");
                    final String sdate = rs.getString("SDATE");
                    final String edate = rs.getString("EDATE");
                    Semester semes = new Semester(semester, semestername, sdate, edate);
                    list.add(semes);
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return list;
        }
    }
}

// eof
