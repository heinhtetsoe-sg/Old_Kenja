/**     プロジェクト/プロパティ/javaコード・スタイル/コード・テンプレート/パターン       ***/

/*
 * $Id: b215044add70095ff355251a68e04ddec356e561 $
 *
 * 作成日: 2020/04/14
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
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KnjDbUtils;

public class KNJD659M {

    private static final Log log = LogFactory.getLog(KNJD659M.class);

    private static final String SEMEALL = "9";
    private static final String SELECT_CLASSCD_UNDER = "89";

    private static final String ALL3 = "333333";
    private static final String ALL5 = "555555";
    private static final String ALL9 = "999999";

    private static final int SCORE_MAX_LINE = 16;
    private static final int VIEW_MAX_LINE = 5;

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

            if(_param._unitTestMap.size() > 0) {
                printMain(db2, svf);
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

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {
//        final List subclassList = subclassListRemoveD026(); //先科目を考慮
        final List subclassList = new ArrayList(_param._subclassMstMap.values());
        Collections.sort(subclassList);

        final List gradeHrClassList = getGradeHrClassList(db2);

        int grp = 1;
        for (Iterator it = gradeHrClassList.iterator(); it.hasNext();) {
            final GradeHrClass gradeHrClass = (GradeHrClass) it.next();
            final List studentList = getList(db2, gradeHrClass._grade, gradeHrClass._hrClass); //生徒リスト

            //教科毎に出力
            String befClasscd = "";
            for (Iterator itSubclass = subclassList.iterator(); itSubclass.hasNext();) {
                final SubclassMst subclassMst = (SubclassMst) itSubclass.next();

                //改ページ
                if(!"".equals(befClasscd) && !befClasscd.equals(subclassMst._classcd)) svf.VrEndPage();

                //単元表
                String key = gradeHrClass._grade + gradeHrClass._hrClass;
                if(_param._unitTestMap.containsKey(key)) {
                    final Map ghcUnitTestMap = (Map) _param._unitTestMap.get(key);
                    key = subclassMst._subclasscd;
                    if(ghcUnitTestMap.containsKey(key)) {
                        final List unitTestList = (List) ghcUnitTestMap.get(key);

                        final String form = "KNJD659M.frm";
                        svf.VrSetForm(form , 4);

                        //明細部以外を印字
                        printTitle(db2, svf, gradeHrClass, subclassMst, studentList);

                        final int keta = KNJ_EditEdit.getMS932ByteLength(subclassMst._subclassname) / 2;
                        final String[] subclassName = KNJ_EditEdit.get_token(subclassMst._subclassname, 2, keta);

                        //単元表
                        int i = 0;
                        for (Iterator itUnit = unitTestList.iterator(); itUnit.hasNext();) {

                            //科目名称(2文字毎)
                            String subclassname = "";
                            if(subclassName.length > i) {
                                subclassname += subclassName[i++];
                                if(subclassName.length > i) {
                                    subclassname += subclassName[i++];
                                }
                            }

                            final UnitTest unitTest = (UnitTest) itUnit.next();
                            svf.VrsOut("GRPCD", String.valueOf(grp));
                            svf.VrsOut("SUBCLASS_NAME", subclassname); //科目
                            svf.VrsOut("VVIEW_NO", unitTest._viewcd); //観点
                            svf.VrsOut("SMALL_VIEW_NO", unitTest._seq); //小観点
                            svf.VrsOut("VIEW_NAME1", unitTest._unit_l_name); //内容
                            svf.VrsOut("MAG", unitTest._weighting); //倍率

                            //単元表明細
                            printScore(db2, svf, subclassMst, unitTest._viewcd, unitTest._seq, studentList);

                            svf.VrEndRecord();
                        }
                        befClasscd = subclassMst._classcd;
                    }
                }
                grp++;
            }
        }
    }

    private void printTitle(final DB2UDB db2, final Vrw32alp svf, final GradeHrClass gradeHrClass, final SubclassMst subclassMst, final List studentList) {
        //ヘッダ
        svf.VrsOut("TITLE", "観点別評価登録チェックリスト");
        final String subTitle = "学年：" + gradeHrClass._gradename + "　学期：" + _param._semesterName + "　教科：" + subclassMst._classname;
        svf.VrsOut("SUBTITLE", subTitle);

        //生徒情報
        int line = 1;
        for (Iterator iterator = studentList.iterator(); iterator.hasNext();) {
            final Student student = (Student) iterator.next();
            svf.VrsOutn("HR_ABBV", line, gradeHrClass._hrabbv); //クラス
            svf.VrsOutn("NO", line, student._attendno); //No.
            svf.VrsOutn("NAME1", line, student._name); //生徒氏名
            line++;
        }

        //Ⅰ～Ⅴ・評定
        String key = gradeHrClass._grade + gradeHrClass._hrClass;
        if(_param._unitTestMap.containsKey(key)) {
            final Map ghcUnitTestMap = (Map) _param._unitTestMap.get(key);
            key = subclassMst._subclasscd;
            if(ghcUnitTestMap.containsKey(key)) {
                final List unitTestList = (List) ghcUnitTestMap.get(key);
                //観点マップの作成
                final Map viewCdMap = new TreeMap();
                for (Iterator itUnit = unitTestList.iterator(); itUnit.hasNext();) {
                    final UnitTest unitTest = (UnitTest) itUnit.next();
                	int weighting = ("".equals(unitTest._weighting)) ? 0 : Integer.parseInt(unitTest._weighting);
                    if(viewCdMap.containsKey(unitTest._viewcd)) {
                    	//Mapに登録済みの倍率を加算
                        final String val = String.valueOf(viewCdMap.get(unitTest._viewcd));
                    	weighting += ("".equals(val)) ? 0 : Integer.parseInt(val);
                    }
                    viewCdMap.put(unitTest._viewcd, weighting);
                }
                //Ⅰ～Ⅴ の印字
                printTotal(db2, svf, subclassMst, viewCdMap, studentList);
            }
        }
    }

    private void printScore(final DB2UDB db2, final Vrw32alp svf, final SubclassMst subclassMst, final String viewcd, final String seq, final List studentList) {
        final String subclassCd = subclassMst._subclasscd;
        final String viewKey = viewcd + seq; //観点 + 小観点

        //観点別評価
        int line = 1;
        for (Iterator iterator = studentList.iterator(); iterator.hasNext();) {
            final Student student = (Student) iterator.next();
            if (student._printSubclassMap.containsKey(subclassCd)) {
                final ScoreData scoreData = (ScoreData) student._printSubclassMap.get(subclassCd);
                svf.VrsOutn("SCORE", line, scoreData.score(viewKey)); //得点
            }
            line++;
            _hasData = true;
        }
    }

    private void printTotal(final DB2UDB db2, final Vrw32alp svf, final SubclassMst subclassMst, final Map viewCdMap, final List studentList) {
        final String subclassCd = subclassMst._subclasscd;

        //Ⅰ～Ⅴ
        int viewLine = 1;
        for (Iterator it = viewCdMap.keySet().iterator();it.hasNext();) {
            final String viewCd = (String) it.next();
            int line = 1;
            for (Iterator iterator = studentList.iterator(); iterator.hasNext();) {
                final Student student = (Student) iterator.next();
                if (student._printSubclassMap.containsKey(subclassCd)) {
                    //得点・倍率・ランク の取得
                    final Map viewMap = getJvewstatRecord(db2, _param._semester, subclassCd, student._schregno, viewCd);
                    if(viewMap.size() > 0) {
                        final String score = (String) viewMap.get("REMARK4");
                        final String weighting = (String) viewMap.get("REMARK5");
                        final String status = (String) viewMap.get("STATUS");
                        String view = "0";
                        if(!"".equals(weighting)) {
                            double viewScore = Double.parseDouble(score)/Double.parseDouble(weighting);
                            view = sishaGonyu(String.valueOf(viewScore),0);
                        }
                        view += status;
                        svf.VrsOutn("VIEW"+viewLine, line, view);
                    } else {
                        svf.VrsOutn("VIEW"+viewLine, line, "");
                    }
                }
                line++;
            }
            viewLine++;
        }

        //評定
        int line = 1;
        for (Iterator iterator = studentList.iterator(); iterator.hasNext();) {
            final Student student = (Student) iterator.next();
            if (student._printSubclassMap.containsKey(subclassCd)) {
                final String div = getRecordScoreDatOne(db2, _param._semester, subclassCd, student._schregno, "SCORE");
                svf.VrsOutn("DIV", line, div); //評定
            }
            line++;
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

    private List getGradeHrClassList(final DB2UDB db2) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getGradeHrClassSql();
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final GradeHrClass gradeHrClass = new GradeHrClass();

                gradeHrClass._schoolKind = StringUtils.defaultString(rs.getString("SCHOOL_KIND"));
                gradeHrClass._grade = StringUtils.defaultString(rs.getString("GRADE"));
                gradeHrClass._hrClass = StringUtils.defaultString(rs.getString("HR_CLASS"));
                gradeHrClass._gradename = StringUtils.defaultString(rs.getString("GRADE_NAME2"));
                gradeHrClass._hrname = StringUtils.defaultString(rs.getString("HR_CLASS_NAME1")) + "組" ;
                gradeHrClass._hrabbv = rs.getString("HR_NAMEABBV");
                gradeHrClass._hrClassName1 = StringUtils.defaultString(rs.getString("HR_CLASS_NAME1"));
                gradeHrClass._trcd = StringUtils.defaultString(rs.getString("TR_CD1"));
                gradeHrClass._staffname = StringUtils.defaultString(rs.getString("STAFFNAME"));
                retList.add(gradeHrClass);
            }

        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return retList;
    }

    private String getGradeHrClassSql() {
        final StringBuffer stb = new StringBuffer();
        stb.append("WITH SCHNO_A AS(");
        stb.append("    SELECT DISTINCT");
        stb.append("      T1.YEAR, T1.SEMESTER, T1.GRADE, T1.HR_CLASS ");
        stb.append("    FROM ");
        stb.append("      SCHREG_REGD_DAT T1 ");
        stb.append("    WHERE ");
        stb.append("      T1.YEAR = '" + _param._loginYear + "' ");
        if (SEMEALL.equals(_param._semester)) {
            stb.append("      AND T1.SEMESTER = '"+ _param._loginSemester +"' ");
        } else {
            stb.append("      AND T1.SEMESTER = '"+ _param._semester +"' ");
        }
        stb.append("      AND T1.GRADE || T1.HR_CLASS IN " + SQLUtils.whereIn(true, _param._categorySelected));
        stb.append("    ) ");
        //メイン表
        stb.append("     SELECT  REGDG.SCHOOL_KIND ");
        stb.append("            ,REGDG.GRADE ");
        stb.append("            ,REGDH.HR_CLASS ");
        stb.append("            ,REGDG.GRADE_NAME2 ");
        stb.append("            ,REGDH.HR_NAME ");
        stb.append("            ,REGDH.HR_NAMEABBV ");
        stb.append("            ,REGDH.HR_CLASS_NAME1 ");
        stb.append("            ,REGDH.TR_CD1 ");
        stb.append("            ,STF1.STAFFNAME ");
        stb.append("     FROM    SCHNO_A REGD ");
        stb.append("     INNER JOIN SCHREG_REGD_GDAT REGDG ");
        stb.append("             ON REGDG.YEAR  = REGD.YEAR ");
        stb.append("            AND REGDG.GRADE = REGD.GRADE ");
        stb.append("     INNER JOIN SCHREG_REGD_HDAT REGDH ");
        stb.append("             ON REGDH.YEAR     = REGD.YEAR ");
        stb.append("            AND REGDH.SEMESTER = REGD.SEMESTER ");
        stb.append("            AND REGDH.GRADE    = REGD.GRADE ");
        stb.append("            AND REGDH.HR_CLASS = REGD.HR_CLASS ");
        stb.append("     LEFT JOIN STAFF_MST STF1 ");
        stb.append("            ON STF1.STAFFCD = REGDH.TR_CD1 ");
        stb.append("     ORDER BY ");
        stb.append("         REGD.GRADE, ");
        stb.append("         REGD.HR_CLASS ");
        final String sql = stb.toString();
        log.debug(" student sql = " + sql);

        return stb.toString();
    }

    private List getList(final DB2UDB db2, final String grade, final String hrClass) {
        final List retList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            final String sql = getStudentSql(grade, hrClass);
            log.debug(" sql =" + sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();

            while (rs.next()) {
                final Student student = new Student();
                student._schregno = rs.getString("SCHREGNO");
                student._name = rs.getString("NAME");
                student._attendno = NumberUtils.isDigits(rs.getString("ATTENDNO")) ? String.valueOf(Integer.parseInt(rs.getString("ATTENDNO"))) + "番" : rs.getString("ATTENDNO");
                student._grade = rs.getString("GRADE");
                student._hrClass = rs.getString("HR_CLASS");
                student._coursecd = rs.getString("COURSECD");
                student._majorcd = rs.getString("MAJORCD");
                student._coursecode = rs.getString("COURSECODE");
                student._course = rs.getString("COURSE");
                student._majorname = rs.getString("MAJORNAME");
                student._coursename = rs.getString("COURSECODENAME");

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

    private String getStudentSql(final String grade, final String hrClass) {
        final StringBuffer stb = new StringBuffer();
        // 異動者を除外した学籍の表 => 組平均において異動者の除外に使用
        stb.append("WITH SCHNO_A AS(");
        stb.append("    SELECT  T1.YEAR, T1.SEMESTER, T1.GRADE, T1.HR_CLASS, T1.ATTENDNO, T1.SCHREGNO, T1.COURSECD, T1.MAJORCD, T1.COURSECODE ");
        stb.append("    FROM    SCHREG_REGD_DAT T1,SEMESTER_MST T2 ");
        stb.append("    WHERE   T1.YEAR = '" + _param._loginYear + "' ");
        if (SEMEALL.equals(_param._semester)) {
            stb.append("     AND T1.SEMESTER = '" + _param._loginSemester + "' ");
        } else {
            stb.append("     AND T1.SEMESTER = '" + _param._semester + "' ");
        }
        stb.append("     AND T1.YEAR = T2.YEAR ");
        stb.append("     AND T1.SEMESTER = T2.SEMESTER ");
        stb.append("     AND T1.GRADE    = '" + grade + "' ");
        stb.append("     AND T1.HR_CLASS = '" + hrClass + "' ");
        stb.append("    ) ");
        //メイン表
        stb.append("     SELECT  REGD.SCHREGNO");
        stb.append("            ,BASE.NAME ");
        stb.append("            ,REGD.ATTENDNO ");
        stb.append("            ,REGDG.GRADE ");
        stb.append("            ,REGD.HR_CLASS ");
        stb.append("            ,REGD.COURSECD ");
        stb.append("            ,REGD.MAJORCD ");
        stb.append("            ,REGD.COURSECODE ");
        stb.append("            ,REGD.COURSECD || REGD.MAJORCD || REGD.COURSECODE AS COURSE");
        stb.append("            ,MAJOR.MAJORNAME ");
        stb.append("            ,COURSE.COURSECODENAME ");
        stb.append("     FROM    SCHNO_A REGD ");
        stb.append("     INNER JOIN SCHREG_REGD_GDAT REGDG ON REGDG.YEAR = REGD.YEAR ");
        stb.append("                  AND REGDG.GRADE = REGD.GRADE ");
        stb.append("     INNER JOIN SCHREG_REGD_HDAT REGDH ON REGDH.YEAR = REGD.YEAR ");
        stb.append("                  AND REGDH.SEMESTER = REGD.SEMESTER ");
        stb.append("                  AND REGDH.GRADE = REGD.GRADE ");
        stb.append("                  AND REGDH.HR_CLASS = REGD.HR_CLASS ");
        stb.append("     LEFT JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ");
        stb.append("     LEFT JOIN MAJOR_MST MAJOR ON MAJOR.COURSECD = REGD.COURSECD ");
        stb.append("                  AND MAJOR.MAJORCD = REGD.MAJORCD ");
        stb.append("     LEFT JOIN COURSECODE_MST COURSE ");
        stb.append("            ON COURSE.COURSECODE = REGD.COURSECODE ");
        stb.append("     ORDER BY ");
        stb.append("         REGD.GRADE, ");
        stb.append("         REGD.HR_CLASS, ");
        stb.append("         REGD.ATTENDNO ");
        final String sql = stb.toString();
        log.debug(" student sql = " + sql);

        return stb.toString();
    }

    private static String sishaGonyu(final String val, final int keta) {
        if (!NumberUtils.isNumber(val)) {
            return null;
        }
        return new BigDecimal(val).setScale(keta, BigDecimal.ROUND_HALF_UP).toString();
    }

    //明細表Ⅰ～Ⅴを取得
    private Map getJvewstatRecord (final DB2UDB db2, final String semester, final String subclasscd, final String schregno, final String viewCd) {
    	final Map rtnMap = new HashMap();
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT  ");
        stb.append("   T1.STATUS, "); //ランク
        stb.append("   VALUE(T2.REMARK4,0) AS REMARK4, "); //得点(未登録の場合'0')
        stb.append("   CASE WHEN VALUE(T2.REMARK5,1) < 1 THEN 1 ELSE VALUE(T2.REMARK5,1) END AS REMARK5 "); //倍率(未登録、'0'以下の場合、'1')
        stb.append(" FROM  ");
        stb.append("   JVIEWSTAT_RECORD_DAT T1 ");
        stb.append("   LEFT JOIN JVIEWSTAT_RECORD_DETAIL_DAT T2 ");
        stb.append("          ON T2.YEAR          = T1.YEAR ");
        stb.append("         AND T2.SEMESTER      = T1.SEMESTER ");
        stb.append("         AND T2.SCHREGNO      = T1.SCHREGNO ");
        stb.append("         AND T2.CLASSCD       = T1.CLASSCD ");
        stb.append("         AND T2.SCHOOL_KIND   = T1.SCHOOL_KIND ");
        stb.append("         AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
        stb.append("         AND T2.SUBCLASSCD    = T1.SUBCLASSCD ");
        stb.append("         AND T2.VIEWCD        = T1.VIEWCD ");
        stb.append(" WHERE T1.YEAR = '"+ _param._loginYear +"' ");
        stb.append("   AND T1.SEMESTER = '"+ semester +"' ");
        if(ALL9.equals(subclasscd)) {
            stb.append("   AND T1.SUBCLASSCD = '"+ subclasscd +"' ");
        } else {
            stb.append("   AND T1.CLASSCD ||'-'|| T1.SCHOOL_KIND ||'-'|| T1.CURRICULUM_CD ||'-'|| T1.SUBCLASSCD = '"+ subclasscd +"' ");
        }
        stb.append("   AND T1.SCHREGNO = '"+ schregno +"' ");
        stb.append("   AND T1.VIEWCD = '"+ viewCd +"' ");
        final String sql =  stb.toString();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            log.debug(sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                rtnMap.put("STATUS", StringUtils.defaultString(rs.getString("STATUS")));
                rtnMap.put("REMARK4", StringUtils.defaultString(rs.getString("REMARK4")));
                rtnMap.put("REMARK5", StringUtils.defaultString(rs.getString("REMARK5")));
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return rtnMap;
    }

    //RECORD_SCORE_DAT(指定項目)を取得
    private String getRecordScoreDatOne (final DB2UDB db2, final String semester, final String subclasscd, final String schregno, final String column) {
        String rtnStr = "";
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT * FROM RECORD_SCORE_DAT ");
        stb.append(" WHERE YEAR = '"+ _param._loginYear +"' ");
        stb.append("   AND SEMESTER = '"+ semester +"' ");
        stb.append("   AND TESTKINDCD = '99' ");
        stb.append("   AND TESTITEMCD = '00' ");
        stb.append("   AND SCORE_DIV = '08' ");
        if(ALL9.equals(subclasscd)) {
            stb.append("   AND SUBCLASSCD = '"+ subclasscd +"' ");
        } else {
            stb.append("   AND CLASSCD ||'-'|| SCHOOL_KIND ||'-'|| CURRICULUM_CD ||'-'|| SUBCLASSCD = '"+ subclasscd +"' ");
        }
        stb.append("   AND SCHREGNO = '"+ schregno +"' ");

        final String sql =  stb.toString();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            log.debug(sql);
            ps = db2.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                rtnStr = StringUtils.defaultString(rs.getString(column));
            }
        } catch (SQLException ex) {
            log.debug("Exception:", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        return rtnStr.toString();
    }

    /**
     * 年組
     */
    private class GradeHrClass {
        String _schoolKind;
        String _grade;
        String _hrClass;
        String _gradename;
        String _hrname;
        String _hrabbv;
        String _hrClassName1;
        String _trcd;
        String _staffname;
    }

    /**
     * 生徒
     */
    private class Student {
        String _schregno;
        String _name;
        String _attendno;
        String _grade;
        String _hrClass;
        String _coursecd;
        String _majorcd;
        String _coursecode;
        String _course;
        String _majorname;
        String _coursename;
        final Map _attendMap = new TreeMap();
        final Map _printSubclassMap = new TreeMap();

        private void setSubclass(final DB2UDB db2) {
            final String scoreSql = prestatementSubclass();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(scoreSql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String classcd = StringUtils.defaultString(rs.getString("CLASSCD"));
                    final String school_kind = StringUtils.defaultString(rs.getString("SCHOOL_KIND"));
                    final String curriculum_cd = StringUtils.defaultString(rs.getString("CURRICULUM_CD"));
                    final String seq = StringUtils.defaultString(rs.getString("SEQ"));
                    final String viewcd = StringUtils.defaultString(rs.getString("VIEWCD"));
                    final String score = StringUtils.defaultString(rs.getString("SCORE"));
                    final String classname = rs.getString("CLASSNAME");
                    final String subclassname = rs.getString("SUBCLASSNAME");

                    String subclasscd = StringUtils.defaultString(rs.getString("SUBCLASSCD"));
                    if (!"999999".equals(subclasscd)) {
                        subclasscd = classcd + "-" + school_kind + "-" + curriculum_cd + "-" + subclasscd;
                    }

                    final String key = subclasscd;
                    if (!_printSubclassMap.containsKey(key)) {
                        _printSubclassMap.put(key, new ScoreData(classcd, classname, subclasscd, subclassname, seq, viewcd));
                    }

                    final ScoreData scoreData = (ScoreData) _printSubclassMap.get(key);
                    final String viewKey = viewcd + seq;
                    scoreData._scoreMap.put(viewKey, score);
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
            //観点
            stb.append(" ) , T_JVIEW AS( ");
            stb.append("     SELECT DISTINCT ");
            stb.append("         T2.YEAR, ");
            stb.append("         T1.GRADE, ");
            stb.append("         T1.CLASSCD, ");
            stb.append("         T1.SCHOOL_KIND, ");
            stb.append("         T1.CURRICULUM_CD, ");
            stb.append("         T1.SUBCLASSCD, ");
            stb.append("         T1.VIEWCD ");
            stb.append("     FROM ");
            stb.append("         JVIEWNAME_GRADE_MST T1 ");
            stb.append("         INNER JOIN JVIEWNAME_GRADE_YDAT T2 ");
            stb.append("                 ON T2.YEAR          = '" + _param._loginYear + "'  ");
            stb.append("                AND T2.GRADE         = T1.GRADE ");
            stb.append("                AND T2.CLASSCD       = T1.CLASSCD ");
            stb.append("                AND T2.SCHOOL_KIND   = T1.SCHOOL_KIND ");
            stb.append("                AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("                AND T2.SUBCLASSCD    = T1.SUBCLASSCD ");
            stb.append("                AND T2.VIEWCD        = T1.VIEWCD ");
            stb.append("     WHERE ");
            stb.append("         T1.GRADE = '" + _grade + "'  ");
            stb.append("         AND T1.CLASSCD < '90' ");
            stb.append(" ) ,JVEW_UNIT_TEST_SCORE AS( ");
            stb.append("     SELECT ");
            stb.append("         T1.SCHREGNO, ");
            stb.append("         T1.CLASSCD, ");
            stb.append("         T1.SCHOOL_KIND, ");
            stb.append("         T1.CURRICULUM_CD, ");
            stb.append("         T1.SUBCLASSCD, ");
            stb.append("         T1.SEQ, ");
            stb.append("         T1.VIEWCD, ");
            stb.append("         T1.SCORE, ");
            stb.append("         T4.WEIGHTING ");
            stb.append("     FROM ");
            stb.append("       UNIT_TEST_SCORE_DAT T1 ");
            stb.append("       INNER JOIN SCHNO T2 ");
            stb.append("               ON T2.YEAR     = T1.YEAR ");
            stb.append("              AND T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("       INNER JOIN T_JVIEW T3 ");
            stb.append("               ON T3.CLASSCD       = T1.CLASSCD ");
            stb.append("              AND T3.SCHOOL_KIND   = T1.SCHOOL_KIND ");
            stb.append("              AND T3.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("              AND T3.SUBCLASSCD    = T1.SUBCLASSCD ");
            stb.append("              AND T3.VIEWCD        = T1.VIEWCD ");
            stb.append("       LEFT JOIN UNIT_TEST_INPUTSEQ_DAT T4 ");
            stb.append("              ON T4.YEAR          = T2.YEAR ");
            stb.append("             AND T4.GRADE         = T2.GRADE ");
            stb.append("             AND T4.HR_CLASS      = T2.HR_CLASS ");
            stb.append("             AND T4.CLASSCD       = T1.CLASSCD ");
            stb.append("             AND T4.SCHOOL_KIND   = T1.SCHOOL_KIND ");
            stb.append("             AND T4.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("             AND T4.SUBCLASSCD    = T1.SUBCLASSCD ");
            stb.append("             AND T4.SEQ           = T1.SEQ ");
            stb.append("             AND T4.VIEWCD        = T1.VIEWCD ");
            stb.append("     WHERE  ");
            stb.append("       T1.YEAR = '" + _param._loginYear + "'  ");
            stb.append(" ) ,MAIN AS( ");
            stb.append("     SELECT ");
            stb.append("         T3.CLASSNAME, ");
            stb.append("         T4.SUBCLASSNAME, ");
            stb.append("         T1.* ");
            stb.append("     FROM JVEW_UNIT_TEST_SCORE T1 ");
            stb.append("     INNER JOIN CLASS_MST T3 ");
            stb.append("        ON T3.CLASSCD = SUBSTR(T1.SUBCLASSCD,1,2) ");
            stb.append("       AND T3.SCHOOL_KIND   = T1.SCHOOL_KIND ");
            stb.append("     INNER JOIN SUBCLASS_MST T4 ");
            stb.append("        ON T4.SUBCLASSCD    = T1.SUBCLASSCD ");
            stb.append("       AND T4.CLASSCD       = T1.CLASSCD ");
            stb.append("       AND T4.SCHOOL_KIND   = T1.SCHOOL_KIND ");
            stb.append("       AND T4.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("     INNER JOIN SUBCLASS_YDAT SUBY ");
            stb.append("        ON SUBY.YEAR          = '" + _param._loginYear + "' ");
            stb.append("       AND SUBY.SUBCLASSCD    = T4.SUBCLASSCD ");
            stb.append("       AND SUBY.CLASSCD       = T4.CLASSCD ");
            stb.append("       AND SUBY.SCHOOL_KIND   = T4.SCHOOL_KIND ");
            stb.append("       AND SUBY.CURRICULUM_CD = T4.CURRICULUM_CD ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     T1.* ");
            stb.append(" FROM MAIN T1 ");
            stb.append(" ORDER BY ");
            stb.append("     T1.CLASSCD, ");
            stb.append("     T1.SCHOOL_KIND, ");
            stb.append("     T1.CURRICULUM_CD, ");
            stb.append("     T1.SUBCLASSCD,  ");
            stb.append("     T1.VIEWCD, ");
            stb.append("     T1.SEQ ");

            return stb.toString();
        }
    }


    /**
     * 得点
     */
    private static class ScoreData {
        final String _classcd;
        final String _classname;
        final String _subclasscd;
        final String _subclassname;
        final String _seq;
        final String _viewcd;
        final Map _scoreMap = new HashMap(); // 得点

        private ScoreData(
                final String classcd,
                final String classname,
                final String subclasscd,
                final String subclassname,
                final String seq,
                final String viewcd
        ) {
            _classcd = classcd;
            _classname = classname;
            _subclasscd = subclasscd;
            _subclassname = subclassname;
            _seq = seq;
            _viewcd = viewcd;
        }

        public String score(final String key) {
            if(!_scoreMap.containsKey(key)) return "";
            return StringUtils.defaultString((String) _scoreMap.get(key));
        }

        public String toString() {
            return "ScoreData(" + _subclasscd + ":" + _subclassname + ", scoreMap = " + _scoreMap + ")";
        }
    }

    /**
     * 換算表
     */
    private static class Kansan {
        final String _level;
        final String _low;
        final String _high;

        private Kansan(final String level, final String low, final String high) {
            _level = level;
            _low = low;
            _high = high;
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

    private static class UnitTest {
        final String _year;
        final String _grade_hr_class;
        final String _subclasscd;
        final String _subclassname;
        final String _seq;
        final String _unit_l_name;
        final String _viewcd;
        final String _viewflg;
        final String _weighting;
        final String _sort;

        private UnitTest(
                final String year,
                final String grade_hr_class,
                final String subclasscd,
                final String subclassname,
                final String seq,
                final String unit_l_name,
                final String viewcd,
                final String viewflg,
                final String weighting,
                final String sort
        ) {
            _year = year;
            _grade_hr_class = grade_hr_class;
            _subclasscd = subclasscd;
            _subclassname = subclassname;
            _seq = seq;
            _unit_l_name = unit_l_name;
            _viewcd = viewcd;
            _viewflg = viewflg;
            _weighting = weighting;
            _sort = sort;

        }
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 75104 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _loginYear;
        final String _loginSemester;
        final String _grade;
        final String _semester;
        final String[] _categorySelected;
        final String _prgid;

        final String _useCurriculumcd;
        final String _nendo;
        final String _semesterName;
        final String _semeSDate;
        final String _semeEDate;
        final boolean _semes2Flg;
        final boolean _semes3Flg;
        final boolean _semes9Flg;
        final String _schoolKind;
        final String _schoolKindName;
        private Map _testItemMap;
        private Map _unitTestMap = new TreeMap();

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

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _loginYear = request.getParameter("YEAR");
            _loginSemester = request.getParameter("LOGIN_SEMESTER");
            _semester = request.getParameter("SEMESTER");
            _grade = request.getParameter("GRADE");
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _prgid = request.getParameter("PRGID");

            _useCurriculumcd = request.getParameter("useCurriculumcd");
            _nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_loginYear)) + "年度";
            _semesterName = getSemesterMst(db2, _semester, "SEMESTERNAME");
            _semeSDate = getSemesterMst(db2, _semester, "SDATE");
            _semeEDate = getSemesterMst(db2, _semester, "EDATE");
            _semes2Flg = "2".equals(_semester) || "3".equals(_semester) || "9".equals(_semester) ? true : false;
            _semes3Flg = "3".equals(_semester) || "9".equals(_semester) ? true : false;
            _semes9Flg = "9".equals(_semester) ? true : false;
            loadNameMstD026(db2);
            loadNameMstD016(db2);
            setPrintSakiKamoku(db2);
            _schoolKind = getSchoolKind(db2);
            _schoolKindName = getSchoolKindName(db2);
            _testItemMap = settestItemMap(db2);
            _unitTestMap = getUnitTestMap(db2);

            setCertifSchoolDat(db2);
            setSubclassMst(db2);
            setCreditMst(db2);

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
                    _creditMstMap.get(regdKey).put(rs.getString("SUBCLASSCD"), rs.getString("CREDITS"));
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


        //学期情報の取得
        private String getSemesterMst(DB2UDB db2, final String semester, final String column) {
            String rtnStr = "";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("   * ");
                stb.append(" FROM ");
                stb.append("   SEMESTER_MST ");
                stb.append(" WHERE ");
                stb.append("       YEAR     = '"+ _loginYear +"' ");
                stb.append("   AND SEMESTER = '"+ semester +"' ");
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtnStr = StringUtils.defaultString(rs.getString(column));
                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return rtnStr;
        }

        //単元情報の取得
        private Map getUnitTestMap(final DB2UDB db2) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("   T1.YEAR, ");
            stb.append("   T1.GRADE || T1.HR_CLASS AS GRADE_HR_CLASS, ");
            stb.append("   T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("   T3.SUBCLASSNAME, ");
            stb.append("   T1.SEQ, ");
            stb.append("   T1.UNIT_L_NAME, ");
            stb.append("   T2.VIEWCD, ");
            stb.append("   T2.VIEWFLG, ");
            stb.append("   T2.WEIGHTING, ");
            stb.append("   T1.SORT, ");
            stb.append("   T1.UNIT_TEST_DATE ");
            stb.append(" FROM  ");
            stb.append("   UNIT_TEST_DAT T1 ");
            stb.append("   INNER JOIN UNIT_TEST_INPUTSEQ_DAT T2 ");
            stb.append("          ON T2.YEAR           = T1.YEAR ");
            stb.append("          AND T2.GRADE         = T1.GRADE ");
            stb.append("          AND T2.HR_CLASS      = T1.HR_CLASS ");
            stb.append("          AND T2.CLASSCD       = T1.CLASSCD ");
            stb.append("          AND T2.SCHOOL_KIND   = T1.SCHOOL_KIND ");
            stb.append("          AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("          AND T2.SUBCLASSCD    = T1.SUBCLASSCD ");
            stb.append("          AND T2.SEQ           = T1.SEQ ");
            stb.append("   INNER JOIN SUBCLASS_MST T3 ");
            stb.append("           ON T3.CLASSCD       = T1.CLASSCD ");
            stb.append("          AND T3.SCHOOL_KIND   = T1.SCHOOL_KIND ");
            stb.append("          AND T3.CURRICULUM_CD = T1.CURRICULUM_CD ");
            stb.append("          AND T3.SUBCLASSCD    = T1.SUBCLASSCD ");
            stb.append(" WHERE T1.YEAR          = '"+ _loginYear +"' ");
            stb.append("   AND T1.DATA_DIV      = '2' ");
            stb.append("   AND T1.GRADE || T1.HR_CLASS IN " + SQLUtils.whereIn(true, _categorySelected));
            stb.append("   AND T1.UNIT_TEST_DATE  BETWEEN '"+ _semeSDate +"' AND '"+ _semeEDate +"' "); // テスト実施日が指定学期範囲内の単元を対象
            // stb.append("   AND T1.CLASSCD ||'-'|| T1.SCHOOL_KIND ||'-'|| T1.CURRICULUM_CD ||'-'|| T1.SUBCLASSCD = '"+ subclass +"' ");
            stb.append(" ORDER BY ");
            stb.append("   T1.GRADE, ");
            stb.append("   T1.HR_CLASS, ");
            stb.append("   T1.CLASSCD, ");
            stb.append("   T1.SCHOOL_KIND, ");
            stb.append("   T1.CURRICULUM_CD, ");
            stb.append("   T1.SUBCLASSCD, ");
            stb.append("   T2.VIEWCD, ");
            stb.append("   T1.SORT, ");
            stb.append("   T1.SEQ, ");
            stb.append("   T1.UNIT_TEST_DATE ");

            PreparedStatement ps = null;
            ResultSet rs = null;
            Map map = new TreeMap(); //科目⇒UnitTest
            final Map retMap = new TreeMap(); //年組⇒Map(科目⇒UnitTest)
            String befGradeHrClass = "";
            String befSubclassCd = "";
            List list = new ArrayList();
            try {
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String year = StringUtils.defaultString(rs.getString("YEAR"));
                    final String grade_hr_class = StringUtils.defaultString(rs.getString("GRADE_HR_CLASS"));
                    final String subclasscd = StringUtils.defaultString(rs.getString("SUBCLASSCD"));
                    final String subclassname = StringUtils.defaultString(rs.getString("SUBCLASSNAME"));
                    final String seq = StringUtils.defaultString(rs.getString("SEQ"));
                    final String unit_l_name = StringUtils.defaultString(rs.getString("UNIT_L_NAME"));
                    final String viewcd = StringUtils.defaultString(rs.getString("VIEWCD"));
                    final String viewflg = StringUtils.defaultString(rs.getString("VIEWFLG"));
                    final String weighting = StringUtils.defaultString(rs.getString("WEIGHTING"));
                    final String sort = StringUtils.defaultString(rs.getString("SORT"));
                    final UnitTest unitTest = new UnitTest(year, grade_hr_class, subclasscd, subclassname, seq, unit_l_name, viewcd, viewflg, weighting, sort);


                    //年組の切り替わり
                    if(!"".equals(befGradeHrClass) && !grade_hr_class.equals(befGradeHrClass)) {
                        map.put(befSubclassCd, list);
                        retMap.put(befGradeHrClass, map);
                        list = new ArrayList();
                        map = new TreeMap();
                    } else {
                        //科目の切り替わり
                        if(!"".equals(befSubclassCd) && !subclasscd.equals(befSubclassCd)) {
                            map.put(befSubclassCd, list);
                            list = new ArrayList();
                        }
                    }

                    list.add(unitTest);
                    befSubclassCd = subclasscd;
                    befGradeHrClass = grade_hr_class;
                }
                if(!"".equals(befGradeHrClass) && !"".equals(befSubclassCd) && list.size() != 0) {
                    map.put(befSubclassCd, list);
                    retMap.put(befGradeHrClass, map);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            return retMap;
        }
    }
}

// eof
