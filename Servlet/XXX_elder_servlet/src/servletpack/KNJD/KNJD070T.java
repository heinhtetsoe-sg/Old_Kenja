/**
 *
 *  学校教育システム 賢者 [成績管理]  学習記録報告書（TOKIO用）
 *
 *  2005/06/22 yamashiro・成績データは'RECORD_DAT'を使用
 */

package servletpack.KNJD;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class KNJD070T extends KNJD070 {

    private static final Log log = LogFactory.getLog(KNJD070T.class);

    /** ps2.executeQuery() **/
    ResultSet setResultsetPs2(PreparedStatement ps,String subclasscd){

        ResultSet rs = null;
        try {
            int pp = 0;
            ps.setString(++pp,subclasscd);
            ps.setString(++pp,subclasscd);
            ps.setString(++pp,subclasscd);
            rs = ps.executeQuery();
        } catch( Exception e ){
            log.error("ResultSet setResultsetPs2 error!",e );
        }
        return rs;

    }//ResultSet setResultsetPs2()



    /** ps3.executeQuery() **/
    ResultSet setResultsetPs3(PreparedStatement ps,String subclasscd){

        ResultSet rs = null;
        try {
            int pp = 0;
            ps.setString(++pp,subclasscd);
            ps.setString(++pp,subclasscd);
            rs = ps.executeQuery();
        } catch( Exception e ){
            log.error("[KNJD070]ResultSet setResultsetPs3 error!",e );
        }
        return rs;

    }//ResultSet setResultsetPs3()



    /**
     *
     *  PrepareStatement作成
     *
     */
    String Pre_Stat_2(Param param)
    {
        StringBuffer stb = new StringBuffer();
        try {
            //生徒情報
            stb.append("WITH SCHREG AS(");
            stb.append(   "SELECT  W1.SCHREGNO,ATTENDNO,NAME ");
            stb.append(   "FROM    SCHREG_REGD_DAT W1,SCHREG_BASE_MST W2 ");
            stb.append(   "WHERE   W1.YEAR = '" + param._0 + "' AND ");
            stb.append(           "W1.SEMESTER = '" + param._1 + "' AND ");
            stb.append(           "W1.GRADE = '" + param._2 + "' AND ");
            stb.append(           "W1.HR_CLASS = '" + param._3 + "' AND ");
            stb.append(           "W1.SCHREGNO = W2.SCHREGNO");
            stb.append(   ") ");

            //成績情報
            stb.append(", RECORD_A AS(");
            stb.append(   "SELECT  * ");
            stb.append(   "FROM    RECORD_DAT T1 ");
            stb.append(   "WHERE   T1.YEAR = '" + param._0 + "' AND ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(                    "T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
            }
            stb.append(           "T1.SUBCLASSCD = ? AND ");
            stb.append(           "EXISTS(SELECT 'X' FROM SCHREG T2 WHERE T1.SCHREGNO = T2.SCHREGNO)");
            stb.append(   ") ");

            //成績情報
            stb.append(", RECORD_B AS(");
            stb.append(   "SELECT  W1.SCHREGNO, '1' AS SEMESTER, SEM1_VALUE AS VALUATION ");
            stb.append(   "FROM    RECORD_A W1 ");
            stb.append(   "WHERE   SEM1_VALUE IS NOT NULL ");
            stb.append(   "UNION ");
            stb.append(   "SELECT  W1.SCHREGNO, '2' AS SEMESTER, SEM2_VALUE AS VALUATION ");
            stb.append(   "FROM    RECORD_A W1 ");
            stb.append(   "WHERE   SEM2_VALUE IS NOT NULL ");
            stb.append(   "UNION ");
            stb.append(   "SELECT  W1.SCHREGNO, '3' AS SEMESTER, SEM3_VALUE AS VALUATION ");
            stb.append(   "FROM    RECORD_A W1 ");
            stb.append(   "WHERE   SEM3_VALUE IS NOT NULL ");
            stb.append(   "UNION ");
            stb.append(   "SELECT  W1.SCHREGNO, '9' AS SEMESTER, GRAD_VALUE AS VALUATION ");
            stb.append(   "FROM    RECORD_A W1 ");
            stb.append(   "WHERE   GRAD_VALUE IS NOT NULL ");
            stb.append(   ") ");

            //出欠情報
            stb.append(", ATTEND_A AS(");
            stb.append(   "SELECT  SCHREGNO, VALUE(SEMESTER,'9')AS SEMESTER, ");
            stb.append(           "SUM(LESSON) AS JISU, ");
            stb.append(           "SUM(VALUE(SICK,0) + VALUE(NOTICE,0) + VALUE(NONOTICE,0) + VALUE(NURSEOFF,0)) AS KEKKA, ");
            stb.append(           "SUM(MOURNING) AS MOUNING, ");
            stb.append(           "SUM(SUSPEND) ");
            if ("true".equals(param._useVirus)) {
                stb.append(           " + SUM(VIRUS) ");
            }
            if ("true".equals(param._useKoudome)) {
                stb.append(           " + SUM(KOUDOME) ");
            }
            stb.append(           " AS SUSPEND, ");
            stb.append(           "SUM(LATE) AS LATE, ");
            stb.append(           "SUM(EARLY) AS EARLY ");
            stb.append(   "FROM    ATTEND_SUBCLASS_DAT T1 ");
            stb.append(   "WHERE   YEAR = '" + param._0 + "' AND ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(                    "T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
            }
            stb.append(           "SUBCLASSCD = ? AND ");
            stb.append(           "SEMESTER <= '" + param._1 + "' AND ");
            stb.append(           "EXISTS(SELECT 'X' FROM SCHREG T2 WHERE T1.SCHREGNO = T2.SCHREGNO)");
            stb.append(   "GROUP BY GROUPING SETS( SCHREGNO, (SCHREGNO, SEMESTER) ) ");
            stb.append(   ") ");

            //メインの表
            stb.append("SELECT  T9.SCHREGNO, T9.ATTENDNO, T9.NAME, T9.SEMESTER, ");
            stb.append(        "T1.VALUATION, ");
            stb.append(        "T2.JISU, T2.KEKKA, T2.SUSPEND, T2.MOUNING, T2.LATE, ");
            stb.append(        "T3.REMARK ");
            stb.append("FROM   (SELECT  SCHREGNO, ATTENDNO, NAME, SEMESTER ");
            stb.append(        "FROM    SCHREG W1, SEMESTER_MST W2 ");
            stb.append(        "WHERE   W2.YEAR = '" + param._0 + "' ");
            stb.append(       ")T9 ");
            stb.append(       "LEFT JOIN RECORD_B T1 ON T1.SCHREGNO = T9.SCHREGNO AND T1.SEMESTER = T9.SEMESTER ");
            stb.append(       "LEFT JOIN ATTEND_A T2 ON T2.SCHREGNO = T9.SCHREGNO AND T2.SEMESTER = T9.SEMESTER ");
            stb.append(       "LEFT JOIN STUDYCLASSREMARK_DAT T3 ON T3.YEAR = '" + param._0 + "' AND ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(                    "T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || ");
            }
            stb.append(                                            "T3.SUBCLASSCD = ? AND ");
            stb.append(                                            "T3.SCHREGNO = T9.SCHREGNO ");
            stb.append("ORDER BY ATTENDNO,SEMESTER");

        } catch( Exception e ){
            log.error("Pre_Stat_2 error!",e );
        }
        return stb.toString();
    }


    /**
     *
     *  PrepareStatement作成
     *
     */
    String Pre_Stat_3(Param param, int ptype )
    {
        StringBuffer stb = new StringBuffer();
        try {
            //生徒情報
            stb.append("WITH SCHREG AS(");
            stb.append(   "SELECT  W1.SCHREGNO,ATTENDNO,NAME ");
            stb.append(   "FROM    SCHREG_REGD_DAT W1,SCHREG_BASE_MST W2 ");
            stb.append(   "WHERE   W1.YEAR = '" + param._0 + "' AND ");
            stb.append(           "W1.SEMESTER = '" + param._1 + "' AND ");
            stb.append(           "W1.GRADE = '" + param._2 + "' AND ");
            if( ptype == 0 )
                stb.append(       "W1.HR_CLASS = '" + param._3 + "' AND ");
            stb.append(           "W1.SCHREGNO = W2.SCHREGNO");
            stb.append(   ") ");

            //成績情報
            stb.append(", RECORD_A AS(");
            stb.append(   "SELECT  * ");
            stb.append(   "FROM    RECORD_DAT T1 ");
            stb.append(   "WHERE   T1.YEAR = '" + param._0 + "' AND ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(                    "T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
            }
            stb.append(           "T1.SUBCLASSCD = ? AND ");
            stb.append(           "EXISTS(SELECT 'X' FROM SCHREG T2 WHERE T1.SCHREGNO = T2.SCHREGNO)");
            stb.append(   ") ");

            //成績情報
            stb.append(", RECORD_B AS(");
            stb.append(   "SELECT  '1' AS SEMESTER, ROUND(AVG (FLOAT ( SEM1_VALUE ) ) * 10, 0) / 10 AS VALUATION ");
            stb.append(   "FROM    RECORD_A W1 ");
            stb.append(   "WHERE   SEM1_VALUE IS NOT NULL ");
            stb.append(   "UNION ");
            stb.append(   "SELECT  '2' AS SEMESTER, ROUND(AVG (FLOAT ( SEM2_VALUE ) ) * 10, 0) / 10 AS VALUATION ");
            stb.append(   "FROM    RECORD_A W1 ");
            stb.append(   "WHERE   SEM2_VALUE IS NOT NULL ");
            stb.append(   "UNION ");
            stb.append(   "SELECT  '3' AS SEMESTER, ROUND(AVG (FLOAT ( SEM3_VALUE ) ) * 10, 0) / 10 AS VALUATION ");
            stb.append(   "FROM    RECORD_A W1 ");
            stb.append(   "WHERE   SEM3_VALUE IS NOT NULL ");
            stb.append(   "UNION ");
            stb.append(   "SELECT  '9' AS SEMESTER, ROUND(AVG (FLOAT ( GRAD_VALUE ) ) * 10, 0) / 10 AS VALUATION ");
            stb.append(   "FROM    RECORD_A W1 ");
            stb.append(   "WHERE   GRAD_VALUE IS NOT NULL ");
            stb.append(   ") ");

            //出欠情報
            stb.append(", ATTEND_A AS(");
            stb.append(   "SELECT  SCHREGNO, VALUE(SEMESTER,'9')AS SEMESTER, ");
            stb.append(           "SUM(LESSON) AS JISU, ");
            stb.append(           "SUM(VALUE(SICK,0) + VALUE(NOTICE,0) + VALUE(NONOTICE,0) + VALUE(NURSEOFF,0)) AS KEKKA, ");
            stb.append(           "SUM(MOURNING) AS MOUNING, ");
            stb.append(           "SUM(SUSPEND) ");
            if ("true".equals(param._useVirus)) {
                stb.append(           " + SUM(VIRUS) ");
            }
            if ("true".equals(param._useKoudome)) {
                stb.append(           " + SUM(KOUDOME) ");
            }
            stb.append(           " AS SUSPEND, ");
            stb.append(           "SUM(LATE) AS LATE, ");
            stb.append(           "SUM(EARLY) AS EARLY ");
            stb.append(   "FROM    ATTEND_SUBCLASS_DAT T1 ");
            stb.append(   "WHERE   YEAR = '" + param._0 + "' AND ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(                    "T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
            }
            stb.append(           "SUBCLASSCD = ? AND ");
            stb.append(           "SEMESTER <= '" + param._1 + "' AND ");
            stb.append(           "EXISTS(SELECT 'X' FROM SCHREG T2 WHERE T1.SCHREGNO = T2.SCHREGNO)");
            stb.append(   "GROUP BY GROUPING SETS( SCHREGNO, (SCHREGNO, SEMESTER) ) ");
            stb.append(   ") ");

            //出欠情報
            stb.append(", ATTEND_B AS(");
            stb.append(   "SELECT  SEMESTER, ");
            stb.append(           "ROUND( AVG( FLOAT( JISU ) )    * 10, 0) / 10 AS JISU, ");
            stb.append(           "ROUND( AVG( FLOAT( KEKKA) )    * 10, 0) / 10 AS KEKKA, ");
            stb.append(           "ROUND( AVG( FLOAT( MOUNING ) ) * 10, 0) / 10 AS MOUNING, ");
            stb.append(           "ROUND( AVG( FLOAT( SUSPEND ) ) * 10, 0) / 10 AS SUSPEND, ");
            stb.append(           "ROUND( AVG( FLOAT( LATE ) )    * 10, 0) / 10 AS LATE, ");
            stb.append(           "ROUND( AVG( FLOAT( EARLY ) )   * 10, 0) / 10 AS EARLY ");
            stb.append(   "FROM    ATTEND_A T1 ");
            stb.append(   "GROUP BY SEMESTER");
            stb.append(   ") ");

            //メインの表
            stb.append("SELECT  T9.SEMESTER, ");
            stb.append(        "T1.VALUATION, ");
            stb.append(        "T2.JISU, T2.KEKKA, T2.SUSPEND, T2.MOUNING, T2.LATE ");
            stb.append("FROM   (SELECT  SEMESTER ");
            stb.append(        "FROM    SCHREG W1, SEMESTER_MST W2 ");
            stb.append(        "WHERE   W2.YEAR = '" + param._0 + "' ");
            stb.append(        "GROUP BY W2.SEMESTER");
            stb.append(       ")T9 ");
            stb.append(       "LEFT JOIN RECORD_B T1 ON T1.SEMESTER = T9.SEMESTER ");
            stb.append(       "LEFT JOIN ATTEND_B T2 ON T2.SEMESTER = T9.SEMESTER ");
            stb.append("ORDER BY SEMESTER");

        } catch( Exception e ){
            log.error("Pre_Stat_2 error!",e );
        }
        return stb.toString();

    }


}
