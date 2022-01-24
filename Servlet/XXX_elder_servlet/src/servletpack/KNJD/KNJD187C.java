/**     プロジェクト/プロパティ/javaコード・スタイル/コード・テンプレート/パターン       ***/

/*
 * $Id: e3bcc7c72a4ffe5cc152a5705d2aae71ff6fa0dc $
 *
 * 作成日: 2019/10/25
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

public class KNJD187C {

    private static final Log log = LogFactory.getLog(KNJD187C.class);

    private static final String SEMEALL = "9";
    private static final String SELECT_CLASSCD_UNDER = "89";

    private static final String SDIV1010101 = "1010101"; //1学期 100点法
    private static final String SDIV1990009 = "1990009"; //1学期 評価
    private static final String SDIV2010101 = "2010101"; //2学期 100点法
    private static final String SDIV2990009 = "2990009"; //1学期 評価
    private static final String SDIV9990008 = "9990008"; //100点法
    private static final String SDIV9990009 = "9990009"; //学年評価

    private static final String YEAR1_SCORE_COUNT 		= "YEAR1_SCORE_COUNT";   //1年 100点法 科目数
    private static final String YEAR1_VAL_COUNT 		= "YEAR1_VAL_COUNT";     //1年 評定 科目数
    private static final String YEAR2_SCORE_COUNT 		= "YEAR2_SCORE_COUNT";   //2年 100点法 科目数
    private static final String YEAR2_VAL_COUNT 		= "YEAR2_VAL_COUNT";     //2年 評定 科目数
    private static final String YEAR3_1_SCORE_COUNT 	= "YEAR3_1_SCORE_COUNT"; //3年1学期 100点法 科目数
    private static final String YEAR3_1_VAL_COUNT   	= "YEAR3_1_VAL_COUNT";   //3年1学期 評定 科目数
    private static final String YEAR3_2_SCORE_COUNT	= "YEAR3_2_SCORE_COUNT"; //3年1学期 100点法 科目数
    private static final String YEAR3_2_VAL_COUNT		=   "YEAR3_2_VAL_COUNT"; //3年2学期 評定 科目数

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

            //通知票
            printSvfMain(db2, svf, student);
            svf.VrEndPage();

            _hasData = true;
        }
    }

    private void printSvfMain(final DB2UDB db2, final Vrw32alp svf, final Student student) {
        final String form = "KNJD187C.frm";
        svf.VrSetForm(form , 1);

        //明細部以外を印字
        printTitle(svf, student);

        //明細部
        final List subclassList = subclassListRemoveD026();
        Collections.sort(subclassList);

        //■成績一覧
        int yearLine = 1;
        Map subclassCntMap = new TreeMap();
        for (Iterator it = student._studentHistList.iterator(); it.hasNext();) {
            final StudentHist studentHist = (StudentHist) it.next();
            int line = 1;
            for (Iterator itSubclass = subclassList.iterator(); itSubclass.hasNext();) {
                final SubclassMst subclassMst = (SubclassMst) itSubclass.next();
                final String subclassCd = subclassMst._subclasscd;

                if (!studentHist._printSubclassMap.containsKey(subclassCd)) {
                    continue;
                }

                final ScoreData scoreData = (ScoreData) studentHist._printSubclassMap.get(subclassCd);
                if (_param._isOutputDebug) {
                    log.info(" score = " + scoreData);
                }

                final String yearField = (yearLine == 1) ? "1" : (yearLine == 2) ? "2" : "3";
                final String subClassField = KNJ_EditEdit.getMS932ByteLength(subclassMst._subclassname) > 8 ? "_2" : "_1";
                svf.VrsOutn("SUBCLASS_NAME" + yearField + subClassField, line, subclassMst._subclassname); //教科名

                if(yearLine == 3) {
                    //1学期
                    svf.VrsOutn("SCORE3", line, scoreData.score(SDIV1010101)); //100点法
                    svf.VrsOutn("VAL3", line, scoreData.score(SDIV1990009)); //評価

                    if(!"".equals(scoreData.score(SDIV1010101))) {
                        //科目数(100点法)の加算
                        final int cnt = subclassCntMap.containsKey(YEAR3_1_SCORE_COUNT) ? (Integer)subclassCntMap.get(YEAR3_1_SCORE_COUNT) + 1 : 1;
                        subclassCntMap.put(YEAR3_1_SCORE_COUNT, cnt);
                    }
                    if(!"".equals(scoreData.score(SDIV1990009))) {
                        //科目数(評価)の加算
                        final int cnt = subclassCntMap.containsKey(YEAR3_1_VAL_COUNT) ? (Integer)subclassCntMap.get(YEAR3_1_VAL_COUNT) + 1 : 1;
                        subclassCntMap.put(YEAR3_1_VAL_COUNT, cnt);
                    }

                    //2学期
                    if(_param._semes2Flg) {
                        svf.VrsOutn("SCORE4", line, scoreData.score(SDIV2010101)); //100点法
                        svf.VrsOutn("VAL4", line, scoreData.score(SDIV2990009)); //評価
                        if(!"".equals(scoreData.score(SDIV2010101))) {
                            //科目数(100点法)の加算
                            final int cnt = subclassCntMap.containsKey(YEAR3_2_SCORE_COUNT) ? (Integer)subclassCntMap.get(YEAR3_2_SCORE_COUNT) + 1 : 1;
                            subclassCntMap.put(YEAR3_2_SCORE_COUNT, cnt);
                        }
                        if(!"".equals(scoreData.score(SDIV2990009))) {
                            //科目数(評価)の加算
                            final int cnt = subclassCntMap.containsKey(YEAR3_2_VAL_COUNT) ? (Integer)subclassCntMap.get(YEAR3_2_VAL_COUNT) + 1 : 1;
                            subclassCntMap.put(YEAR3_2_VAL_COUNT, cnt);
                        }
                    }

                } else {
                    final String field = (yearLine == 1) ? "1" : "2";
                    svf.VrsOutn("SCORE" + field, line, scoreData.score(SDIV9990008)); //100点法
                    svf.VrsOutn("VAL" + field, line, scoreData.score(SDIV9990009)); //評価

                    final String scoreKey = (yearLine == 1) ? YEAR1_SCORE_COUNT : YEAR2_SCORE_COUNT;
                    final String valKey = (yearLine == 1) ? YEAR1_VAL_COUNT : YEAR2_VAL_COUNT;
                    if(!"".equals(scoreData.score(SDIV9990008))) {
                        //科目数(100点法)の加算
                        final int cnt = subclassCntMap.containsKey(scoreKey) ? (Integer)subclassCntMap.get(scoreKey) + 1 : 1;
                        subclassCntMap.put(scoreKey, cnt);
                    }
                    if(!"".equals(scoreData.score(SDIV9990009))) {
                        //科目数(評価)の加算
                        final int cnt = subclassCntMap.containsKey(valKey) ? (Integer)subclassCntMap.get(valKey) + 1 : 1;
                        subclassCntMap.put(valKey, cnt);
                    }
                }
                svf.VrEndRecord();
                line++;
            }
            //科目数、合計、平均の印字
            printTotal(svf, studentHist, yearLine, subclassCntMap);
            yearLine++;
        }

        //TOEFL-ITP　最高スコアの印字
        printToeflItp(svf, student);

        svf.VrEndRecord();
    }

    private void printTitle(final Vrw32alp svf, final Student student) {
        //明細部以外を印字

        //ヘッダ
        svf.VrsOut("NENDO", _param._nendo); //年度
        svf.VrsOut("SCHREGNO", student._schregno); //学科名
        final String nameField = KNJ_EditEdit.getMS932ByteLength(student._name) > 30 ? "2" : "1";
        svf.VrsOut("NAME" + nameField, student._name); //氏名

        int yearLine = 1;
        for (Iterator it = student._studentHistList.iterator(); it.hasNext();) {
            final StudentHist studentHist = (StudentHist) it.next();
            final String hrName = studentHist._hrName + studentHist._attendno;
            svf.VrsOut("HR_NAME"+yearLine, hrName); //年組番
            yearLine++;
        }
    }

    private void printTotal(final Vrw32alp svf, final StudentHist studentHist, final int yearLine, final Map subclassCntMap) {
        //科目数、合計、平均の印字
        final String subclassCd = ALL9;
        final ScoreData scoreData = (ScoreData) studentHist._printSubclassMap.get(subclassCd);
        if(scoreData == null) return;
        if(yearLine == 3) {
            //1学期
            if(subclassCntMap.containsKey(YEAR3_1_SCORE_COUNT)) {
                svf.VrsOut("SUBCLASS_TOTAL3_1", String.valueOf((Integer)subclassCntMap.get(YEAR3_1_SCORE_COUNT))); //100点法 科目数
            }
            svf.VrsOut("TOTAL3_1", scoreData.score(SDIV1010101)); //100点法 合計
            svf.VrsOut("AVE3_1", sishaGonyu(scoreData.avg(SDIV1010101))); //100点法 平均
            if(subclassCntMap.containsKey(YEAR3_1_VAL_COUNT)) {
                svf.VrsOut("SUBCLASS_TOTAL3_1", String.valueOf((Integer)subclassCntMap.get(YEAR3_1_VAL_COUNT))); //評価 科目数
            }
            svf.VrsOut("TOTAL3_2", scoreData.score(SDIV1990009)); //評価 合計
            svf.VrsOut("AVE3_2", sishaGonyu(scoreData.avg(SDIV1990009))); //評価 平均

            //2学期
            if(_param._semes2Flg) {
                if(subclassCntMap.containsKey(YEAR3_2_SCORE_COUNT)) {
                    svf.VrsOut("SUBCLASS_TOTAL3_1", String.valueOf((Integer)subclassCntMap.get(YEAR3_2_SCORE_COUNT))); //100点法 科目数
                }
                svf.VrsOut("TOTAL4_1", scoreData.score(SDIV2010101)); //100点法 合計
                svf.VrsOut("AVE4_1", sishaGonyu(scoreData.avg(SDIV2010101))); //100点法 平均
                if(subclassCntMap.containsKey(YEAR3_2_VAL_COUNT)) {
                    svf.VrsOut("SUBCLASS_TOTAL3_1", String.valueOf((Integer)subclassCntMap.get(YEAR3_2_VAL_COUNT))); //評価 科目数
                }
                svf.VrsOut("TOTAL4_2", scoreData.score(SDIV2990009)); //評価 合計
                svf.VrsOut("AVE4_2", sishaGonyu(scoreData.avg(SDIV2990009))); //評価 平均
            }
        } else {
            final String field = (yearLine == 1) ? "1" : "2";
            final String scoreKey = (yearLine == 1) ? YEAR1_SCORE_COUNT : YEAR2_SCORE_COUNT;
            final String valKey = (yearLine == 1) ? YEAR1_VAL_COUNT : YEAR2_VAL_COUNT;
            if(subclassCntMap.containsKey(scoreKey)) {
                svf.VrsOut("SUBCLASS_TOTAL"+field+"_1", String.valueOf((Integer)subclassCntMap.get(scoreKey))); //100点法 科目数
            }
            svf.VrsOut("TOTAL"+field+"_1", scoreData.score(SDIV9990008)); //100点法 合計
            svf.VrsOut("AVE"+field+"_1", sishaGonyu(scoreData.avg(SDIV9990008))); //100点法 平均

            if(subclassCntMap.containsKey(valKey)) {
                svf.VrsOut("SUBCLASS_TOTAL"+field+"_2", String.valueOf((Integer)subclassCntMap.get(valKey))); //評価 科目数
            }
            svf.VrsOut("TOTAL"+field+"_2", scoreData.score(SDIV9990009)); //評価 合計
            svf.VrsOut("AVE"+field+"_2", sishaGonyu(scoreData.avg(SDIV9990009))); //評価 平均
        }
    }


    private void printToeflItp(final Vrw32alp svf, final Student student) {
        //TOEFL-ITP　最高スコアの印字
        int yearLine = 1;
        int toeflScore = 0;
        for (Iterator it = student._studentHistList.iterator(); it.hasNext();) {
            final StudentHist studentHist = (StudentHist) it.next();
            for (Iterator itAftTotal = studentHist._aftTotalStudyList.iterator(); itAftTotal.hasNext();) {
            	final AftTotalStudy aftTotalStudy = (AftTotalStudy) itAftTotal.next();
                svf.VrsOutn("TOEFL_SCORE", yearLine, aftTotalStudy._score);
                if(yearLine == 3) svf.VrsOut("TOEFL_TOTAL", aftTotalStudy._base_score); //TOEFL点
                if(toeflScore < Integer.parseInt(aftTotalStudy._toefl_score)) toeflScore = Integer.parseInt(aftTotalStudy._toefl_score);
                yearLine++;
            }
            svf.VrsOutn("TOEFL_SCORE", 4, String.valueOf(toeflScore)); //推薦用
        }
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

    protected void VrsOutnRenban(final Vrw32alp svf, final String field, final String[] value) {
        if (null != value) {
            for (int i = 0 ; i < value.length; i++) {
                svf.VrsOutn(field, i + 1, value[i]);
            }
        }
    }

    private int getSemeLine(final String semester) {
        final int line;
        if (SEMEALL.equals(semester)) {
            line = 3;
        } else {
            line = Integer.parseInt(semester);
        }
        return line;
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
                student._attendno = NumberUtils.isDigits(rs.getString("ATTENDNO")) ? String.valueOf(Integer.parseInt(rs.getString("ATTENDNO"))) + "番" : rs.getString("ATTENDNO");
                student._grade = rs.getString("GRADE");
                student._hrClass = rs.getString("HR_CLASS");
                student._studentHistList = student.setHistList(db2);
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
        stb.append("        AND T1.YEAR = T2.YEAR ");
        stb.append("        AND T1.SEMESTER = T2.SEMESTER ");
        if ("1".equals(_param._disp)) {
            stb.append("    AND T1.GRADE || T1.HR_CLASS IN " + SQLUtils.whereIn(true, _param._categorySelected));
        } else {
            stb.append("    AND T1.SCHREGNO IN " + SQLUtils.whereIn(true, _param._categorySelected));
        }

        //                      在籍チェック:転学(2)・退学(3)者は除外 但し異動日が学期終了日または異動基準日より小さい場合
        //                                   転入(4)・編入(5)者は除外 但し異動日が学期終了日または異動基準日より大きい場合
        stb.append("        AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST S1 ");
        stb.append("                       WHERE   S1.SCHREGNO = T1.SCHREGNO ");
        stb.append("                           AND ((S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < CASE WHEN T2.EDATE < '" + _param._date + "' THEN T2.EDATE ELSE '" + _param._date + "' END) ");
        stb.append("                             OR (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE > CASE WHEN T2.EDATE < '" + _param._date + "' THEN T2.EDATE ELSE '" + _param._date + "' END)) ) ");
//        //                      異動者チェック：留学(1)・休学(2)者は除外 但し学期終了日または基準日が異動開始日と終了日内にある場合
//        stb.append(        "AND NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT S1 ");
//        stb.append(                       "WHERE   S1.SCHREGNO = T1.SCHREGNO ");
//        stb.append(                           "AND S1.TRANSFERCD IN ('1','2') AND CASE WHEN T2.EDATE < '" + parameter._date + "' THEN T2.EDATE ELSE '" + parameter._date + "' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE) ");
        //                      異動者チェック：休学(2)者は除外 但し学期終了日または基準日が異動開始日と終了日内にある場合
        stb.append("        AND NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT S1 ");
        stb.append("                       WHERE   S1.SCHREGNO = T1.SCHREGNO ");
        stb.append("                           AND S1.TRANSFERCD IN ('2') AND CASE WHEN T2.EDATE < '" + _param._date + "' THEN T2.EDATE ELSE '" + _param._date + "' END BETWEEN S1.TRANSFER_SDATE AND S1.TRANSFER_EDATE) ");
        stb.append("    ) ");
        //メイン表
        stb.append("     SELECT  REGD.SCHREGNO");
        stb.append("            ,REGD.SEMESTER ");
        stb.append("            ,BASE.NAME ");
        stb.append("            ,REGDG.SCHOOL_KIND ");
        stb.append("            ,REGD.ATTENDNO ");
        stb.append("            ,REGDG.GRADE ");
        stb.append("            ,REGD.HR_CLASS ");
        stb.append("     FROM    SCHNO_A REGD ");
        stb.append("     LEFT JOIN SCHREG_REGD_GDAT REGDG ON REGDG.YEAR = REGD.YEAR ");
        stb.append("                  AND REGDG.GRADE = REGD.GRADE ");
        stb.append("     LEFT JOIN SCHREG_REGD_HDAT REGDH ON REGDH.YEAR = REGD.YEAR ");
        stb.append("                  AND REGDH.SEMESTER = REGD.SEMESTER ");
        stb.append("                  AND REGDH.GRADE = REGD.GRADE ");
        stb.append("                  AND REGDH.HR_CLASS = REGD.HR_CLASS ");
        stb.append("     LEFT JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ");
        stb.append("     WHERE   REGD.YEAR = '" + _param._loginYear + "' ");
        if (SEMEALL.equals(_param._semester)) {
            stb.append("     AND REGD.SEMESTER = '" + _param._loginSemester + "' ");
        } else {
            stb.append("     AND REGD.SEMESTER = '" + _param._semester + "' ");
        }
        if ("1".equals(_param._disp)) {
            stb.append("         AND REGD.GRADE || REGD.HR_CLASS IN " + SQLUtils.whereIn(true, _param._categorySelected));
        } else {
            stb.append("         AND REGD.SCHREGNO IN " + SQLUtils.whereIn(true, _param._categorySelected));
        }
        stb.append("     ORDER BY ");
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
        String _attendno;
        String _grade;
        String _hrClass;
        List _studentHistList;

        private List setHistList(final DB2UDB db2) {
            final List retList = new ArrayList();
            final String sql = studentHistSql();
            log.debug(" studentHistSql = " + sql);
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String year = StringUtils.defaultString(rs.getString("YEAR"));
                    final String grade = StringUtils.defaultString(rs.getString("GRADE"));
                    final String hr_class = StringUtils.defaultString(rs.getString("HR_CLASS"));
                    final String hr_name = Integer.parseInt(rs.getString("GRADE_CD")) + "年" + rs.getString("HR_CLASS_NAME1") + "組" ;
                    final String attendno = String.valueOf(Integer.parseInt(rs.getString("ATTENDNO")));
                    final String schregno = StringUtils.defaultString(rs.getString("SCHREGNO"));
                    final StudentHist studentHist = new StudentHist(year, grade, hr_class, hr_name, attendno, schregno);
                    studentHist.setSubclass(db2);
                    studentHist._aftTotalStudyList = studentHist.setAftTotalStudyList(db2);

                    retList.add(studentHist);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
            return retList;
        }

        private String studentHistSql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("   * ");
            stb.append(" FROM ");
            stb.append("   ( ");
            stb.append("    SELECT DISTINCT ");
            stb.append("      REGD.YEAR, ");
            stb.append("      REGD.GRADE, ");
            stb.append("      REGD.HR_CLASS, ");
            stb.append("      GDAT.GRADE_CD, ");
            stb.append("      HDAT.HR_CLASS_NAME1, ");
            stb.append("      REGD.ATTENDNO, ");
            stb.append("      REGD.SCHREGNO ");
            stb.append("    FROM  ");
            stb.append("      SCHREG_REGD_DAT REGD ");
            stb.append("      LEFT JOIN SCHREG_REGD_GDAT GDAT ");
            stb.append("             ON GDAT.YEAR     = REGD.YEAR ");
            stb.append("            AND GDAT.GRADE    = REGD.GRADE ");
            stb.append("      LEFT JOIN SCHREG_REGD_HDAT HDAT ");
            stb.append("             ON HDAT.YEAR     = REGD.YEAR ");
            stb.append("            AND HDAT.SEMESTER = REGD.SEMESTER ");
            stb.append("            AND HDAT.GRADE    = REGD.GRADE ");
            stb.append("            AND HDAT.HR_CLASS = REGD.HR_CLASS ");
            stb.append("    WHERE  ");
            stb.append("          REGD.YEAR    <= '"+ _param._loginYear +"' ");
            stb.append("      AND REGD.SEMESTER = '"+ _param._semester +"'  ");
            stb.append("      AND REGD.SCHREGNO = '"+ _schregno +"'  ");
            stb.append("    ORDER BY YEAR DESC ");
            stb.append("    FETCH FIRST 3 ROWS ONLY "); //3年分取得
            stb.append("   ) ");
            stb.append(" ORDER BY YEAR ");
            return stb.toString();
        }
    }


    private class StudentHist {
    	final String _year;
        final String _grade;
        final String _hrClass;
        final String _hrName;
        final String _attendno;
        final String _schregno;
        final Map _attendMap = new TreeMap();
        final Map _printSubclassMap = new TreeMap();
        final Map _attendSubClassMap = new HashMap();
        List _aftTotalStudyList;

        private StudentHist(
            	final String year,
                final String grade,
                final String hrClass,
                final String hrName,
                final String attendno,
                final String schregno
        ) {
        	_year = year;
            _grade = grade;
            _hrClass = hrClass;
            _hrName = hrName;
            _attendno = attendno;
            _schregno = schregno;
        }

        private void setSubclass(final DB2UDB db2) {
            final String sql = prestatementSubclass();
        	log.fatal(" scoreSql = " + sql);
            if (_param._isOutputDebug) {
            	log.fatal(" scoreSql = " + sql);
            }
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
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

                    final String key = subclasscd;
                    if (!_printSubclassMap.containsKey(key)) {
                    	_printSubclassMap.put(key, new ScoreData(classcd, classname, subclasscd, subclassname, credits));
                    }
                    final ScoreData scoreData = (ScoreData) _printSubclassMap.get(key);
                    final String testcd = rs.getString("SEMESTER") + rs.getString("TESTKINDCD") + rs.getString("TESTITEMCD") + rs.getString("SCORE_DIV");
                    scoreData._scoreMap.put(testcd, StringUtils.defaultString(rs.getString("SCORE")));
                	scoreData._avgMap.put(testcd, StringUtils.defaultString(rs.getString("AVG")));
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }

        private String prestatementSubclass() {

        	final String sdiv1 = (_year.equals(_param._loginYear)) ? SDIV1010101 : SDIV9990008;
        	final String sdiv2 = (_year.equals(_param._loginYear)) ? SDIV1990009 : SDIV9990009;
        	final String sdiv3 = (_year.equals(_param._loginYear)) ? SDIV2010101 : "";
        	final String sdiv4 = (_year.equals(_param._loginYear)) ? SDIV2990009 : "";
            final String[] sdivs = {sdiv1, sdiv2, sdiv3, sdiv4};
            final StringBuffer stb = new StringBuffer();
            final StringBuffer divStr = new StringBuffer();
            divStr.append(" ( ");
            String or = "";
            for (int i = 0; i < sdivs.length; i++) {
                if(!"".equals(sdivs[i])){
                    final String semester = sdivs[i].substring(0, 1);
                    final String testkindcd = sdivs[i].substring(1, 3);
                    final String testitemcd = sdivs[i].substring(3, 5);
                    final String scorediv = sdivs[i].substring(5);
                    divStr.append(or).append(" SEMESTER = '" + semester + "' AND TESTKINDCD = '" + testkindcd + "' AND TESTITEMCD = '" + testitemcd + "' AND SCORE_DIV = '" + scorediv + "' ");
                    or = " OR ";
                }
            }
            divStr.append(" ) ");

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
            stb.append("         T2.YEAR    = '" + _year + "'  ");
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
            stb.append("     S1.YEAR         = '" + _year + "' ");
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
            stb.append("     AND SUBCLASSCD NOT LIKE '50%' ");
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
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + _year + "'  ");
            stb.append("     AND (" + divStr + ") ");
            stb.append("     AND T1.SUBCLASSCD NOT LIKE '50%' ");
            stb.append("     AND T1.SUBCLASSCD NOT IN ('" + ALL3 + "', '" + ALL5 + "') ");

            stb.append(" ) ,RECORD0 AS( ");
            stb.append("     SELECT ");
            stb.append("              T1.YEAR, T1.SEMESTER, T1.TESTKINDCD, T1.TESTITEMCD, T1.SCORE_DIV, T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, T1.SCHREGNO ");
            stb.append("            , L1.CREDITS ");
            stb.append("            , L2.SCORE ");
            stb.append("            , L2.AVG ");
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
            stb.append("                T1.YEAR = '" + _year + "' ");
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

        private List setAftTotalStudyList(final DB2UDB db2) {
            final List retList = new ArrayList();
            final String sql = aftTotalStudySql();
            log.debug(" aftTotalStudySql = " + sql);
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                	final String score = StringUtils.defaultString(rs.getString("SCORE"));
                	final String toefl_score = StringUtils.defaultString(rs.getString("TOEFL_SCORE"));
                	final String base_score = StringUtils.defaultString(rs.getString("BASE_SCORE"));
                    final AftTotalStudy aftTotalStudy = new AftTotalStudy(score, toefl_score, base_score);
                    retList.add(aftTotalStudy);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
            return retList;
        }

        private String aftTotalStudySql() {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("   VALUE(T1.SCORE, 0) AS SCORE, ");
            stb.append("   VALUE(T2.TOEFL_SCORE, 0) AS TOEFL_SCORE, ");
            stb.append("   VALUE(T3.BASE_SCORE, 0) AS BASE_SCORE ");
            /** TODO 3年学力テスト.合計(学力点) 別SQLで実装予定
            stb.append("   CASE WHEN COALESCE(T2.CLASS_SCORE, T2.ABILITY_SCORE, T2.TOEFL_SCORE, T2.QUALIFIED_SCORE, T2.ADJUSTMENT_SCORE) = NULL ");
            stb.append("        THEN NULL ");
            stb.append("        ELSE VALUE(T2.CLASS_SCORE, 0) + ");
            stb.append("             VALUE(T2.ABILITY_SCORE, 0) + ");
            stb.append("             VALUE(T2.TOEFL_SCORE, 0) + ");
            stb.append("             VALUE(T2.QUALIFIED_SCORE, 0) + ");
            stb.append("             VALUE(T2.ADJUSTMENT_SCORE, 0) ");
            stb.append("   END AS SCORE_TOTAL ");
             */
            stb.append(" FROM ");
            stb.append("   AFT_TOTAL_STUDY_TOEFL_DAT T1 ");
            stb.append("   LEFT JOIN AFT_TOTAL_STUDY_BATCH_DAT T2 ");
            stb.append("          ON T2.YEAR     = T1.YEAR ");
            stb.append("         AND T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("   LEFT JOIN TOEFL_MST T3 ");
            stb.append("          ON T3.YEAR     = T1.YEAR ");
            stb.append(" WHERE ");
            stb.append("       T1.YEAR     = '"+ _year +"' ");
            stb.append("   AND T1.SCHREGNO = '"+ _schregno +"' ");

            return stb.toString();
        }

    }

    private static class ScoreData {
        final String _classcd;
        final String _classname;
        final String _subclasscd;
        final String _subclassname;
        final String _credits;
        final Map _scoreMap = new HashMap(); // 得点
        final Map _avgMap = new HashMap(); // 個人の平均点 (999999のみ使用)

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


    private static class AftTotalStudy {
    	final String _score;
    	final String _toefl_score;
    	final String _base_score;

        private AftTotalStudy(
        		final String score,
        		final String toefl_score,
        		final String base_score
        ) {
        	_score = score;
        	_toefl_score = toefl_score;
        	_base_score = base_score;
        }
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
    	log.fatal("$Revision: 75084 $");
    	KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _disp;
        final String[] _categorySelected;
        final String _date;
        final String _gradeHrClass;
        final String _grade;
        final String _loginSemester;
        final String _loginYear;
        final String _prgid;
        final String _semester;
        final String _nendo;
        final boolean _semes2Flg;
        final boolean _semes3Flg;
        final boolean _semes9Flg;
        final String _schoolKind;

        private final Map _testItemMap;

        private String _certifSchoolSchoolName;
        private String _certifSchoolJobName;
        private String _certifSchoolPrincipalName;

        private final List _semesterList;
        private Map _semesterMap;
        private final Map _semesterDetailMap;
        private Map _subclassMstMap;
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
        	_disp = request.getParameter("DISP");
            _categorySelected = request.getParameterValues("CATEGORY_SELECTED");
            _date = request.getParameter("DATE").replace('/', '-');
            _gradeHrClass = request.getParameter("GRADE_HR_CLASS");
            if ("1".equals(_disp)) {
                _grade = request.getParameter("GRADE");
            } else {
                _grade = _gradeHrClass.substring(0, 2);
            }
            _loginSemester = request.getParameter("LOGIN_SEMESTER");
            _loginYear = request.getParameter("YEAR");
            _prgid = request.getParameter("PRGID");
            _semester = request.getParameter("SEMESTER");
            _semes2Flg = "2".equals(_semester) || "3".equals(_semester) || "9".equals(_semester) ? true : false;
            _semes3Flg = "3".equals(_semester) || "9".equals(_semester) ? true : false;
            _semes9Flg = "9".equals(_semester) ? true : false;
            _nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_loginYear)) + "年度";
            loadNameMstD026(db2);
            loadNameMstD016(db2);
            setPrintSakiKamoku(db2);

            _schoolKind = getSchoolKind(db2);

            _testItemMap = settestItemMap(db2);

            setCertifSchoolDat(db2);

            _semesterList = getSemesterList(db2);
            _semesterMap = loadSemester(db2);
            _semesterDetailMap = new HashMap();
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

        private String getSchoolKind(final DB2UDB db2) {
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT WHERE YEAR = '" + _loginYear + "' AND GRADE = '" + _grade + "' ");

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
