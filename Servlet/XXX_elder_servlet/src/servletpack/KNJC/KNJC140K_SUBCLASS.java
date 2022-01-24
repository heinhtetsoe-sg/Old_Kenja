/**
 *
 *  学校教育システム 賢者 [出欠管理] 欠席・欠課の要注意者・超過者リスト
 *
 *  2004/12/16 yamashiro
 *  2005/02/04 yamashiro  集計用テーブルから欠課数取得を追加
 *  2005/03/05 yamasihro  要注意者リストをKNJC150に移動 => KNJC150_SUBCLASSはKNJC140_SUBCLASSをextends
 *                        欠課のカウントに保健室欠課および一日欠席(ATTEND_DAT)を追加
 *                        同一科目を３回遅刻・早退すると１回欠課の扱いとする処理を追加
 *  2005/03/17 yamashiro 欠席日数超過者をテーブルに書き込む処理を追加
 *  2005/10/12 yamashiro  編入（データ仕様の変更による）について修正、および学籍異動に転学を追加
 *                           <change specification of 05/09/28>
 *  2005/12/11 yamashiro リアルタイム集計において遅刻・早退が算出されない不具合を修正
 */

package servletpack.KNJC;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import nao_package.svf.Vrw32alp;
import nao_package.db.DB2UDB;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class KNJC140K_SUBCLASS extends KNJC140K_BASE {

    private static final Log log = LogFactory.getLog(KNJC140K_SUBCLASS.class);
    private boolean _hasSchChrDatExecutediv;

    /**
     *  コンストラクター
     */
    KNJC140K_SUBCLASS(final DB2UDB db2, final Vrw32alp svf, final String[] param, final String[] pselect) {
        super(db2, svf, param, pselect);
        _hasSchChrDatExecutediv = setTableColumnCheck(db2, "SCH_CHR_DAT", "EXECUTEDIV");
    }

    private static boolean setTableColumnCheck(final DB2UDB db2, final String tabname, final String colname) {
        final StringBuffer stb = new StringBuffer();
        stb.append(" SELECT 1 FROM ");
        if (StringUtils.isBlank(colname)) {
            stb.append("SYSCAT.TABLES");
        } else {
            stb.append("SYSCAT.COLUMNS");
        }
        stb.append(" WHERE TABNAME = '" + tabname + "' ");
        if (!StringUtils.isBlank(colname)) {
            stb.append(" AND COLNAME = '" + colname + "' ");
        }
        
        PreparedStatement ps = null;
        ResultSet rs = null;
        boolean hasTableColumn = false;
        try {
            ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();
            while (rs.next()) {
                hasTableColumn = true;
            }
        } catch (Exception ex) {
            log.error("exception!", ex);
        } finally {
            DbUtils.closeQuietly(null, ps, rs);
            db2.commit();
        }
        log.fatal(" hasTableColumn " + tabname + (null == colname ? "" :  "." + colname) + " = " + hasTableColumn);
        return hasTableColumn;
    }

    /** 
     *  科目別印刷処理 
     */
    void printSvfMain()    {

        if(log.isTraceEnabled()) { 
            for (int i = 0 ; i < param.length ; i++) {
                log.trace("param[" + i + "]=" + param[i]);
            }
        }

        //SQL作成
        try {
            ps1 = db2.prepareStatement(prestatementSubclassAbsent());                //学級別生徒欠課
        } catch (Exception ex) {
            log.error("error! ", ex);
        }

        //学年単位で印刷処理
        for (int i = 0; i < pselect.length; i++) {
            printSvfGrade(pselect[i]);    //印刷処理
        }

        prestatementClose();

    }//boolean printSvfMain()の括り



    /** 
     *  科目別出力処理 => 学年単位で印刷処理 
     */
    private void printSvfGrade(final String grade) {
        ResultSet rs = null;
        try {
            int pp = 0;
            ps1.setString(++pp, grade);    //学年
            ps1.setString(++pp, grade);    //学年
            rs = ps1.executeQuery();
            svf.VrsOut("GRADE",    String.valueOf(Integer.parseInt(grade)) + "年生");    //学年（改ページ用）
            svf.VrsOut("TITLE",    getPaperTitle());   //05/03/05Modify

            String subclasscd = "";            //科目コードの保存
            while (rs.next()) {
                //科目コードの変わり目
                if (!subclasscd.equals(keyCd(rs)) || 45 <= pline) {
                    prinvSvfGradeSubclass(rs, subclasscd);                    //科目等出力
                    subclasscd = keyCd(rs);
                }
                //生徒欠課
                printSvfGradeSubclassOut(rs);                                    //生徒欠課出力
            }
        } catch (Exception ex) {
            log.error("error! " , ex );
        } finally {
            DbUtils.closeQuietly(rs);
        }

    }//void printSvfGrade()の括り


    /** 
     *  タイトル出力
     *    2005/03/05
     */
    String getPaperTitle()
    {
        return "欠課時数超過者リスト";
    }
    
    private String keyCd(final ResultSet rs) throws SQLException {
        if ("1".equals(param[10])) {
            return rs.getString("CLASSCD") + "-" + rs.getString("SCHOOL_KIND") + "-" + rs.getString("SCHOOL_KIND") + "-" + rs.getString("SUBCLASSCD");
        }
        return rs.getString("SUBCLASSCD");
    }


    /** 
     *  科目別出力処理-->学年ごとの処理-->科目等をSVF-FORMへ出力 
     *    2005/02/04 選択科目は科目名に'*'を付ける
     */
    private void prinvSvfGradeSubclass(final ResultSet rs, final String subclasscd) {

        try {
            if (!rs.getString("SUBCLASSCD").equals(subclasscd)) {
                log.debug(rs.getString("SUBCLASSNAME") + "   pline="+pline);
            }
            //出力行が最終行なら次ページへ出力！
            if (!rs.getString("SUBCLASSCD").equals(subclasscd) && pline == (45 - 1)) {
                svf.VrAttribute("subject1", "Meido=100");
                svf.VrsOut("subject1", "A");            //科目名称
                svf.VrEndRecord();
                pline = 0;
                //svf.VrAttribute("RECORD1", "Print=2");
                //svf.VrEndRecord();
            }

            if (rs.getString("ELECTDIV") != null && Integer.parseInt(rs.getString("ELECTDIV")) == 1 ) { // 05/02/04
                svf.VrsOut("subject1",  "*" + rs.getString("SUBCLASSNAME"));                 //科目名称 05/02/04
            } else {
                svf.VrsOut("subject1",  rs.getString("SUBCLASSNAME"));                     //科目名称
            }
            if (!rs.getString("SUBCLASSCD").equals(subclasscd)) {
                svf.VrsOut("late",    "("+String.valueOf(rs.getInt("CREDITS"))+"単位)");                  //科目単位
                svf.VrsOut("KINTAI",  "欠課");                                                          //勤怠
                final String strx = "   " + String.valueOf( rs.getInt("SUB_CNT") ) + "人";
                svf.VrsOut("total",   "(" + strx.substring( strx.length() - 4, strx.length() ) + ")" ); //人数
            }
            svf.VrEndRecord();
            nonedata = true;
            if (pline >= 45) {
                pline = 0;
            }
            pline++;

        } catch (SQLException ex) {
            log.error("error! ", ex);
        }

    }//prinvSvfGradeSubclass()の括り



    /** 
     *  科目別出力処理-->学年ごとの処理-->長欠生徒明細等をSVF-FORMへ出力 
     */
    private void printSvfGradeSubclassOut(final ResultSet rs) {

        try {
            svf.VrsOut("HR_CLASS", rs.getString("HR_NAME") + "-" + dmf1.format(rs.getInt("ATTENDNO")));  //組・出席番号
            svf.VrsOut("name1",    rs.getString("NAME"));                                                  //生徒名

            final String strx = "   " + String.valueOf(rs.getInt("ABSENT_CNT")) + "時間";

            svf.VrsOut("times1",   "(" + strx.substring(strx.length() - 5, strx.length()) + ")");        //欠課時数

            svf.VrEndRecord();
            nonedata = true;

            if (45 <= pline) {
                pline = 0;
            }
            pline++;
        } catch (SQLException ex) {
            log.error("error! " , ex);
        }

    }//printSvfGradeSubclassOut()の括り

    /** 
     *  PrepareStatement作成-->科目別長欠生徒取得 
     *     2005/02/04 集計用テーブルから欠課数取得を追加
     *     2005/03/05 欠課のカウントに保健室欠課および一日欠席(ATTEND_DAT)を追加
     *     2005/03/05 同一科目を３回遅刻・早退すると１回欠課の扱いとする処理を追加
     *     2005/12/11 リアルタイム集計において遅刻・早退が算出されない不具合を修正
     */
    private String prestatementSubclassAbsent(){
        final String useCurriculumcd = param.length < 10 ? "" : param[10];

        final StringBuffer stb = new StringBuffer();
        try {
            stb.append("WITH SCHNO AS(");
            stb.append(        "SELECT  W1.GRADE, W1.HR_CLASS, W1.SCHREGNO ");
            stb.append(     "FROM    SCHREG_REGD_DAT W1 ");
            stb.append(     "WHERE   W1.YEAR = '" + param[0] + "' AND ");
            stb.append(             "W1.SEMESTER = '" + param[1] + "' AND ");
            stb.append(             "W1.GRADE = ? ");
            stb.append(     "),");

            stb.append(" TEST_COUNTFLG AS ( ");
            stb.append("     SELECT ");
            stb.append("         T1.EXECUTEDATE, ");
            stb.append("         T1.PERIODCD, ");
            stb.append("         T1.CHAIRCD, ");
            stb.append("         '2' AS DATADIV ");
            stb.append("     FROM ");
            stb.append("         SCH_CHR_TEST T1, ");
            stb.append("         TESTITEM_MST_COUNTFLG T2 ");
            stb.append("     WHERE ");
            stb.append("         T2.YEAR       = T1.YEAR ");
            stb.append("         AND T2.TESTKINDCD = T1.TESTKINDCD ");
            stb.append("         AND T2.TESTITEMCD = T1.TESTITEMCD ");
            stb.append("         AND T2.COUNTFLG   = '0' ");
            stb.append(     ") ");

            //対象生徒の時間割データ NO101 元のSCHEDULE_SCHREG
            //05/10/08Modify <change specification of n-times>
            stb.append(", SCHEDULE_SCHREG AS(");
            stb.append(     "SELECT  T2.SCHREGNO, T0.SEMESTER, T1.EXECUTEDATE, T1.PERIODCD, ");
            if ("1".equals(useCurriculumcd)) {
                stb.append(             "T5.CLASSCD, T5.SCHOOL_KIND, T5.CURRICULUM_CD, ");
            }
            stb.append(             "T5.SUBCLASSCD, T4.DI_CD, (CASE WHEN T3.SCHREGNO IS NOT NULL THEN '1' ELSE '0' END) AS IS_OFFDAYS ");
            stb.append(     "FROM    SCH_CHR_DAT T1 ");
            stb.append(     "INNER JOIN CHAIR_DAT T0 ON T1.YEAR = T0.YEAR ");
            stb.append(         "AND T1.SEMESTER = T0.SEMESTER ");
            stb.append(         "AND T1.CHAIRCD = T0.CHAIRCD ");
            stb.append(     "INNER JOIN CHAIR_STD_DAT T2 ON T1.YEAR = T2.YEAR ");
            stb.append(         "AND T1.SEMESTER = T2.SEMESTER ");
            stb.append(         "AND T1.CHAIRCD = T2.CHAIRCD ");
            stb.append(         "AND T1.EXECUTEDATE BETWEEN T2.APPDATE AND T2.APPENDDATE ");
            stb.append(     "INNER JOIN SCHNO SCH ON SCH.SCHREGNO = T2.SCHREGNO ");
            stb.append(     "INNER JOIN CHAIR_DAT T5 ON T5.CHAIRCD = T0.CHAIRCD ");
            stb.append(         "AND T5.YEAR = T1.YEAR ");
            stb.append(         "AND T5.SEMESTER = T1.SEMESTER ");
            stb.append(     "LEFT JOIN SCHREG_TRANSFER_DAT T3 ON T3.SCHREGNO = T2.SCHREGNO ");
            stb.append(         "AND T3.TRANSFERCD = '2' ");
            stb.append(         "AND T1.EXECUTEDATE BETWEEN T3.TRANSFER_SDATE AND T3.TRANSFER_EDATE ");
            stb.append(     "LEFT JOIN ATTEND_DAT T4 ON T4.SCHREGNO = T2.SCHREGNO ");
            stb.append(         "AND T4.ATTENDDATE = T1.EXECUTEDATE ");
            stb.append(         "AND T4.PERIODCD = T1.PERIODCD ");
            stb.append(         "AND T4.CHAIRCD = T1.CHAIRCD ");
            stb.append(     "WHERE   T1.YEAR = '" + param[0] + "' ");
            stb.append(         "AND T1.SEMESTER <= '" + param[1] + "' ");
            stb.append(         "AND T1.EXECUTEDATE BETWEEN '" + param[6] + "' AND '" + param[8] + "' ");
            stb.append(         "AND NOT EXISTS(SELECT  'X' FROM SCHREG_BASE_MST T3 ");    //05/10/07Build NOT EXISTS
            stb.append(                        "WHERE   T3.SCHREGNO = T2.SCHREGNO ");
            stb.append(                            "AND (( ENT_DIV IN('4','5') AND EXECUTEDATE < ENT_DATE ) ");
            stb.append(                              "OR ( GRD_DIV IN('2','3') AND EXECUTEDATE > GRD_DATE )) ) ");
            stb.append("    AND NOT EXISTS(SELECT ");
            stb.append("                       'X' ");
            stb.append("                   FROM ");
            stb.append("                       ATTEND_DAT T4 ");
            stb.append("                   LEFT JOIN ATTEND_DI_CD_DAT L1 ON L1.YEAR = '" + param[0] + "' AND L1.DI_CD = T4.DI_CD ");
            stb.append("                   WHERE ");
            stb.append("                       T4.SCHREGNO = T2.SCHREGNO ");
            stb.append("                       AND T4.ATTENDDATE = T1.EXECUTEDATE ");
            stb.append("                       AND T4.PERIODCD = T1.PERIODCD ");
            stb.append("                       AND L1.REP_DI_CD = '27' "); // 勤怠コードが'27'の入力されている日付は時間割にカウントしない
            stb.append("                  ) ");
            // 勤怠コード'28'は時間割にカウントしない
            stb.append("    AND NOT EXISTS(SELECT ");
            stb.append("                       'X' ");
            stb.append("                   FROM ");
            stb.append("                       ATTEND_DAT T4 ");
            stb.append("                   LEFT JOIN ATTEND_DI_CD_DAT L1 ON L1.YEAR = '" + param[0] + "' AND L1.DI_CD = T4.DI_CD ");
            stb.append("                   WHERE ");
            stb.append("                       T4.SCHREGNO = T2.SCHREGNO ");
            stb.append("                       AND T4.ATTENDDATE = T1.EXECUTEDATE ");
            stb.append("                       AND T4.PERIODCD = T1.PERIODCD ");
            stb.append("                       AND L1.REP_DI_CD = '28' ");
            stb.append("                  ) ");
            //if( definecode.useschchrcountflg) {
                //SCHEDULE_A 表から条件を異動 05/11/25
                stb.append(                   "AND NOT EXISTS(SELECT  'X' FROM SCH_CHR_COUNTFLG T4 ");
                stb.append(                                  "WHERE   T4.EXECUTEDATE = T1.EXECUTEDATE ");
                stb.append(                                      "AND T4.PERIODCD = T1.PERIODCD ");
                stb.append(                                      "AND T4.CHAIRCD = T1.CHAIRCD ");
                stb.append(                                      "AND T4.GRADE||T4.HR_CLASS = SCH.GRADE || SCH.HR_CLASS ");
                stb.append(                                      "AND T4.COUNTFLG = '0')  ");
                stb.append(                   "AND NOT EXISTS(SELECT  'X' FROM TEST_COUNTFLG TEST ");
                stb.append(                                  "WHERE ");
                stb.append(                                      "TEST.EXECUTEDATE  = T1.EXECUTEDATE ");
                stb.append(                                      "AND TEST.PERIODCD = T1.PERIODCD ");
                stb.append(                                      "AND TEST.CHAIRCD  = T1.CHAIRCD ");
                stb.append(                                      "AND TEST.DATADIV  = T1.DATADIV) ");
            //}
            if (_hasSchChrDatExecutediv) {
                stb.append("        AND VALUE(T1.EXECUTEDIV, '0') <> '2' "); // 休講は対象外
            }

            stb.append(     ") ");

            //05/12/11 列にSEMESTERを追加し学期別の集計だけ行う。ペナルティー欠課換算はATTEND_Cへ
            stb.append(", ATTEND_A AS(");
            stb.append(     "SELECT SCHREGNO, ");
            if ("1".equals(useCurriculumcd)) {
                stb.append(             "S1.CLASSCD, S1.SCHOOL_KIND, S1.CURRICULUM_CD, ");
            }
            stb.append(            "SUBCLASSCD, ");
            stb.append(            "SEMESTER, ");
            stb.append(            "SUM(ABSENT_CNT) AS ABSENT_CNT, ");
            stb.append(            "SUM(LATE_EARLY) AS LATE_EARLY ");
            stb.append(     "FROM(  SELECT  SCHREGNO, ");
            if ("1".equals(useCurriculumcd)) {
                stb.append(             "W1.CLASSCD, W1.SCHOOL_KIND, W1.CURRICULUM_CD, ");
            }
            stb.append(                    " SUBCLASSCD, SEMESTER, ");
            stb.append(                    "SUM( VALUE(SICK,0) + VALUE(NOTICE,0) + VALUE(NONOTICE,0) + VALUE(NURSEOFF,0) ");
            if (knjSchoolMst != null && "1".equals(knjSchoolMst._subAbsent))  {
                stb.append(                   "+ VALUE(ABSENT, 0) ");
            }
            if (knjSchoolMst != null && "1".equals(knjSchoolMst._subSuspend)) {
                stb.append(                   "+ VALUE(SUSPEND, 0) ");
            }
            if (knjSchoolMst != null && "1".equals(knjSchoolMst._subVirus)) {
                stb.append(                   "+ VALUE(VIRUS, 0) ");
            }
            if (knjSchoolMst != null && "1".equals(knjSchoolMst._subKoudome)) {
                stb.append(                   "+ VALUE(KOUDOME, 0) ");
            }
            if (knjSchoolMst != null && "1".equals(knjSchoolMst._subMourning)) {
                stb.append(                   "+ VALUE(MOURNING, 0) ");
            }
            if (knjSchoolMst != null && "1".equals(knjSchoolMst._subOffDays)) { 
                stb.append(                   "+ VALUE(OFFDAYS, 0) ");
            }
            stb.append(                    "   ) AS ABSENT_CNT, ");
            stb.append(                       "SUM(VALUE(LATE,0) + VALUE(EARLY,0)) AS LATE_EARLY ");
            stb.append(            "FROM    ATTEND_SUBCLASS_DAT W1 ");
            stb.append(            "WHERE   YEAR = '" + param[0] + "' AND ");
            stb.append(                    "SEMESTER||MONTH <= '" + param[7] + "' AND ");
            stb.append(                    "EXISTS(SELECT  'X' FROM SCHNO W2 WHERE W1.SCHREGNO = W2.SCHREGNO) ");
            stb.append(            "GROUP BY SCHREGNO, ");
            if ("1".equals(useCurriculumcd)) {
                stb.append(             "W1.CLASSCD, W1.SCHOOL_KIND, W1.CURRICULUM_CD, ");
            }
            stb.append(                " SUBCLASSCD, SEMESTER ");
            stb.append(               "UNION ALL ");
            stb.append(            "SELECT SCHREGNO, ");
            if ("1".equals(useCurriculumcd)) {
                stb.append(             "T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, ");
            }
            stb.append(                   "SUBCLASSCD, SEMESTER, ");
            stb.append(                      "SUM(CASE WHEN (CASE WHEN L1.REP_DI_CD IN ('29','30','31') THEN L1.ATSUB_REPL_DI_CD ELSE L1.REP_DI_CD END) IN('4','5','6','14','11','12','13' ");
            if (knjSchoolMst != null && "1".equals(knjSchoolMst._subAbsent)) {
                stb.append(                                       ",'1','8'");
            }
            if (knjSchoolMst != null && "1".equals(knjSchoolMst._subSuspend)) {
                stb.append(                                       ",'2','9'");
            }
            if (knjSchoolMst != null && "1".equals(knjSchoolMst._subVirus)) {
                stb.append(                   "+ VALUE(VIRUS, 0) ");
            }
            if (knjSchoolMst != null && "1".equals(knjSchoolMst._subKoudome)) {
                stb.append(                   "+ VALUE(KOUDOME, 0) ");
            }
            if (knjSchoolMst != null && "1".equals(knjSchoolMst._subMourning)) {
                stb.append(                                       ",'3','10'");
            }
            stb.append(                                           ") ");
            if (knjSchoolMst != null && "1".equals(knjSchoolMst._subOffDays)) {
                stb.append(                        " OR (IS_OFFDAYS = '1')");
            }
            stb.append(                    " THEN 1 ELSE 0 END) AS ABSENT_CNT, ");
            stb.append(                      "SUM(CASE WHEN (CASE WHEN L1.REP_DI_CD IN ('29','30','31') THEN L1.ATSUB_REPL_DI_CD ELSE L1.REP_DI_CD END) IN('15','16','23','24') THEN SMALLINT(VALUE(L1.MULTIPLY, '1')) ELSE 0 END)AS LATE_EARLY ");
            stb.append(            "FROM SCHEDULE_SCHREG T1 ");
            stb.append(            "LEFT JOIN ATTEND_DI_CD_DAT L1 ON L1.YEAR = '" + param[0] + "' AND L1.DI_CD = T1.DI_CD ");
            stb.append(            "WHERE NOT EXISTS(SELECT  'X' FROM SCHREG_TRANSFER_DAT T4 ");    //<change specification of 05/09/28>
            stb.append(                              "WHERE   T4.SCHREGNO = T1.SCHREGNO ");
            stb.append(                                  "AND TRANSFERCD IN('1') AND EXECUTEDATE BETWEEN TRANSFER_SDATE AND TRANSFER_EDATE ) ");

            stb.append(            "GROUP BY SCHREGNO, ");
            if ("1".equals(useCurriculumcd)) {
                stb.append(             "T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, ");
            }
            stb.append(                 " SUBCLASSCD, SEMESTER ");
            stb.append(         ")S1 ");
            stb.append(     "GROUP BY SCHREGNO, ");
            if ("1".equals(useCurriculumcd)) {
                stb.append(             "S1.CLASSCD, S1.SCHOOL_KIND, S1.CURRICULUM_CD, ");
            }
            stb.append(          "SUBCLASSCD,SEMESTER ");    //05/12/11Modify
            stb.append(     ") ");

            //学期内のペナルティ欠課換算を行う 05/12/11 Build
            stb.append(", ATTEND_C AS(");
            stb.append(     "SELECT SCHREGNO, ");
            if ("1".equals(useCurriculumcd)) {
                stb.append(             "T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, ");
            }
            stb.append(            "SUBCLASSCD, ");
            if (param[9] == null) {
                stb.append(        "SUM(ABSENT_CNT) AS ABSENT_CNT ");
            } else {
                stb.append(        "SUM( VALUE(ABSENT_CNT,0) + VALUE(LATE_EARLY,0)/3 ) AS ABSENT_CNT ");
            }
            stb.append(     "FROM   ATTEND_A T1 ");
            stb.append(     "GROUP BY SCHREGNO, ");
            if ("1".equals(useCurriculumcd)) {
                stb.append(             "T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, ");
            }
            stb.append(            "SUBCLASSCD ");
            stb.append(     ") ");

            stb.append(", ATTEND_B AS(");
            stb.append(    "SELECT W1.SCHREGNO, ");
            if ("1".equals(useCurriculumcd)) {
                stb.append(             "W1.CLASSCD, W1.SCHOOL_KIND, W1.CURRICULUM_CD, ");
            }
            stb.append(           "W1.SUBCLASSCD,W2.GRADE,W2.HR_CLASS,ATTENDNO,HR_NAME,ABSENT_CNT,W4.CREDITS ");
            stb.append(    "FROM   ATTEND_C W1, ");    //05/12/11Modify ATTEND_A => ATTEND_C
            stb.append(           "SCHREG_REGD_DAT W2, ");
            stb.append(           "SCHREG_REGD_HDAT W3, ");
            stb.append(           "CREDIT_MST W4 ");
            stb.append(    "WHERE  W2.YEAR = '" + param[0] + "' AND ");
            stb.append(           "W2.SEMESTER = '" + param[1] + "' AND ");
            stb.append(           "W2.GRADE = ? AND ");
            stb.append(              "W2.SCHREGNO = W1.SCHREGNO AND ");
            stb.append(           "W3.YEAR = W2.YEAR AND ");
            stb.append(              "W3.SEMESTER = W2.SEMESTER AND ");
            stb.append(           "W3.GRADE = W2.GRADE AND ");
            stb.append(           "W3.HR_CLASS = W2.HR_CLASS AND ");
            stb.append(              "W4.YEAR = W2.YEAR AND ");
            stb.append(           "W4.GRADE = W2.GRADE AND ");
            stb.append(           "W4.COURSECD = W2.COURSECD AND ");
            stb.append(              "W4.MAJORCD = W2.MAJORCD AND ");
            stb.append(           "W4.COURSECODE = W2.COURSECODE AND ");
            if ("1".equals(useCurriculumcd)) {
                stb.append(           "W4.CLASSCD = W1.CLASSCD AND ");
                stb.append(           "W4.SCHOOL_KIND = W1.SCHOOL_KIND AND ");
                stb.append(           "W4.CURRICULUM_CD = W1.CURRICULUM_CD AND ");
            }
            stb.append(           "W4.SUBCLASSCD = W1.SUBCLASSCD AND ");
            stb.append( prestatementCondition() );
            stb.append(") ");

            stb.append("SELECT  ");
            if ("1".equals(useCurriculumcd)) {
                stb.append(             "W1.CLASSCD, W1.SCHOOL_KIND, W1.CURRICULUM_CD, ");
            }
            stb.append(        "W1.SUBCLASSCD, ELECTDIV, SUBCLASSNAME, SUB_CNT, ");
            stb.append(        "HR_CLASS, ATTENDNO, NAME, HR_NAME, ABSENT_CNT, CREDITS ");
            stb.append(        ",w2.schregno ");    //05/03/17
            stb.append("FROM  ( SELECT  ");
            if ("1".equals(useCurriculumcd)) {
                stb.append(             "T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, ");
            }
            stb.append(                "SUBCLASSCD, ");
            stb.append(                "COUNT(SCHREGNO)AS SUB_CNT ");
            stb.append(        "FROM    ATTEND_B T1 ");
            stb.append(        "GROUP BY ");
            if ("1".equals(useCurriculumcd)) {
                stb.append(             "T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, ");
            }
            stb.append(           " SUBCLASSCD)W1 ");
            stb.append(        "LEFT JOIN ATTEND_B W2 ON ");
            if ("1".equals(useCurriculumcd)) {
                stb.append(          " W2.CLASSCD = W1.CLASSCD AND ");
                stb.append(          " W2.SCHOOL_KIND = W1.SCHOOL_KIND AND ");
                stb.append(          " W2.CURRICULUM_CD = W1.CURRICULUM_CD AND ");
            }
            stb.append(          " W2.SUBCLASSCD = W1.SUBCLASSCD ");
            stb.append(           "LEFT JOIN SUBCLASS_MST W3 ON ");
            if ("1".equals(useCurriculumcd)) {
                stb.append(          " W3.CLASSCD = W1.CLASSCD AND ");
                stb.append(          " W3.SCHOOL_KIND = W1.SCHOOL_KIND AND ");
                stb.append(          " W3.CURRICULUM_CD = W1.CURRICULUM_CD AND ");
            }
            stb.append(          " W3.SUBCLASSCD = W1.SUBCLASSCD ");
            stb.append(           "LEFT JOIN SCHREG_BASE_MST W4 ON W4.SCHREGNO = W2.SCHREGNO ");
            
            stb.append("ORDER BY ");
            if ("1".equals(useCurriculumcd)) {
                stb.append(             "W1.CLASSCD, W1.SCHOOL_KIND, W1.CURRICULUM_CD, ");
            }
            stb.append(        "W1.SUBCLASSCD,HR_CLASS,ATTENDNO");

        } catch (Exception ex) {
            log.error("error! ", ex);
            log.debug("prestatementSubclassAbsent sql=" + stb.toString());
        }
        log.debug("sql=" + stb.toString());
        return stb.toString();

    }//prestatementSubclassAbsent()の括り


    /** 
     *  PrepareStatement作成-->欠課時数超過者の条件部分
     *     2005/03/05
     */
    String prestatementCondition(){
        return
        "INT(W4.CREDITS*(CASE WHEN W2.GRADE='03' THEN 8 ELSE 10 END) + 1 ) <= VALUE(W1.ABSENT_CNT,0)";
    }
}//クラスの括り
