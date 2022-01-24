// kanji=漢字
/*
 * $Id: 87cae06d408f09ecc3917bad748fed9d0ef9ca1f $
 *
 * 作成日: 2005/06/21
 * 作成者: yamashiro
 *
 * Copyright(C) 2005-2008 ALP Okinawa Co.,Ltd. All rights reserved.
 */

/**
 *
 *  学校教育システム 賢者 [成績管理]  テスト結果一覧表（TOKIO用）
 *
 *  2005/06/21 yamashiro・成績データは'RECORD_DAT'を使用
 *                      ・取り敢えず、該当講座の該当テスト種別の成績がNULLまたはフラグがNULLの場合は
 *                      「未受験」とする。
 *                      ・順位はテスト成績で付ける（元は偏差値で）
 */

package servletpack.KNJD;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJD.detail.KNJ_Testname;


public class KNJD050T extends KNJD050{

    /** 9900 : 学年/学期成績 */
    private static final String GRAD_SEM_KIND = "9900";

    private static final Log log = LogFactory.getLog(KNJD050T.class);


//  /**
//     *  SVF-FORM 04/10/30Add
//     */
//  void printHeadSvf2( DB2UDB db2, Vrw32alp svf, String param[] )
//  {
//        final String kindCd = param[4] + param[5];
//        if (!GRAD_SEM_KIND.equals(kindCd)) {
//            ResultSet rs = null;
//            try {
//                final String sql = KNJ_Testname.getTestNameSql(param[17], param[0], param[1], kindCd);
//                db2.query(sql);
//                rs = db2.getResultSet();
//                while (rs.next()) {
//                    svf.VrsOut("test1", rs.getString("TESTITEMNAME"));
//                }
//            } catch (final SQLException e) {
//                log.error("テスト名称の読込みでエラー", e);
//            } finally {
//                db2.commit();
//                DbUtils.closeQuietly(null, null, rs);
//            }
//        }
//
//        try {
//          KNJ_Testname test = new KNJ_Testname();
//          KNJ_Testname.ReturnVal returnval = test.getTestNameCountFlg( db2, param[4], param[5], param[3], param[0] );
//          svf.VrsOut("subject1"           ,returnval.val3);           //科目名称
//      } catch( Exception e ){
//          log.debug("printHeadSvf2() test name get error!", e );
//      }
//    }


    /**
     *  preparedstatementパラメーター・マーカーのセット
     */
    void prestatSetParameter( String param[], PreparedStatement ps1 ){
        try {
            if( param[12].equals("1") ){
                int pp = 0;
                ps1.setString( ++pp, param[6] );    //対象講座コード
                ps1.setString( ++pp, param[6] );    //対象講座コード
                ps1.setString( ++pp, param[6] );    //対象講座コード
                ps1.setString( ++pp, param[6] );    //対象講座コード
            }
        } catch( Exception e ){
            log.debug("prestatSetParameter error!", e );
        }
    }


    /**
     *  テスト名称設定
     *  2004/10/30Build
     *  2005/06/21Modify
     */
    void setTestname( DB2UDB db2, Vrw32alp svf, String param[] )
    {
        final String kindCd = param[4] + param[5];
        if (!GRAD_SEM_KIND.equals(kindCd)) {
            ResultSet rs = null;
            try {
                final String sql = KNJ_Testname.getTestNameSql(param[17], param[0], param[1], kindCd);
                db2.query(sql);
                rs = db2.getResultSet();
                while (rs.next()) {
                    svf.VrsOut("test1", rs.getString("TESTITEMNAME"));
                }
            } catch (final SQLException e) {
                log.error("テスト名称の読込みでエラー", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, null, rs);
            }
        }

        try {
            final String useCurriculumcd = param.length >= 19 ? param[18] : null;
            KNJ_Testname test = new KNJ_Testname();
            KNJ_Testname.ReturnVal returnval = test.getTestNameCountFlg( db2, param[4], param[5], param[3], param[0], useCurriculumcd);
            svf.VrsOut( "subject1",  returnval.val3 );      //科目名称
        } catch( Exception e ){
            log.debug("Set_Head() test name get error! ", e );
        }
    }


    /**
     *  PrepareStatement作成
     *  成績のテーブルはRECORD_DAT
     */
    String Pre_Stat( String param[] )
    {
        final String useCurriculumcd = param.length >= 19 ? param[18] : null;

        StringBuffer stb = new StringBuffer();

        try {
            String field = null;
            String fieldName = "";
            if (param[4].equals("01")) {
                fieldName = "_INTR_SCORE";
            } else {
                fieldName = param[5].equals("01") ? "_TERM_SCORE" : "_TERM2_SCORE";
            }
            field = "SEM" + param[1] + fieldName;

            stb.append("WITH ");
            //名簿の表
            stb.append(" T_MEIBO AS ( ");
            stb.append(" SELECT DISTINCT ");
            stb.append("     T1.CHAIRCD, ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.APPDATE, ");
            stb.append("     T1.APPENDDATE ");
            stb.append(" FROM ");
            stb.append("     CHAIR_DAT T0, ");
            stb.append("     CHAIR_STD_DAT T1, ");
            stb.append("     SCH_CHR_TEST T2 ");
            stb.append(" WHERE ");
            stb.append("     T0.YEAR=T1.YEAR AND ");
            stb.append("     T0.SEMESTER=T1.SEMESTER AND ");
            stb.append("     T0.CHAIRCD=T1.CHAIRCD AND ");
            if ("1".equals(useCurriculumcd)) {
                stb.append(" T0.CLASSCD || '-' || T0.SCHOOL_KIND || '-' || T0.CURRICULUM_CD || '-' || ");
            }
            stb.append("     T0.SUBCLASSCD='" + param[3] + "' AND ");
            stb.append("     T1.YEAR='" + param[0] + "' AND ");
            stb.append("     T1.SEMESTER='" + param[1] + "' AND ");
            stb.append("     T1.CHAIRCD IN " + param[7] + " AND ");
            stb.append("     T2.EXECUTEDATE BETWEEN T1.APPDATE AND ");
            stb.append("     T1.APPENDDATE AND ");
            stb.append("     T2.CHAIRCD=T1.CHAIRCD AND ");
            stb.append("     T2.TESTKINDCD='" + param[4] + "' AND ");
            stb.append("     T2.TESTITEMCD='" + param[5] + "' AND ");
            stb.append("     T2.YEAR=T1.YEAR AND ");
            stb.append("     T2.SEMESTER=T1.SEMESTER ");
            stb.append(" ) ");
                        //成績データの表
            stb.append(",RECORD_A AS(");
            stb.append(" SELECT ");
            stb.append("     T2.CHAIRCD, ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1." + field + " AS SCORE ");
            stb.append(" FROM ");
            stb.append("     RECORD_DAT T1 ");
            stb.append("     INNER JOIN T_MEIBO T2 ON T2.SCHREGNO=T1.SCHREGNO ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR='" + param[0] + "' AND ");
            if ("1".equals(useCurriculumcd)) {
                stb.append(" T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || ");
            }
            stb.append("     T1.SUBCLASSCD='" + param[3] + "' AND ");
            stb.append("     T1." + field + " is not null ");
            stb.append(")");
                        //統計対象全体の標準偏差値
            stb.append(",HENSA_SC AS(");
            stb.append(     "SELECT  ROUND( AVG( FLOAT( SCORE ) ) * 100, 0 ) / 100 AS S_AVG, ");
            stb.append(             "ROUND( STDDEV( FLOAT( SCORE ) ) * 100, 0 ) / 100 AS S_HENSA, ");
            stb.append(             "COUNT(SCHREGNO) AS S_CNT ");
            stb.append(     "FROM    RECORD_A ");
            stb.append(")");
                        //対象講座の標準偏差値
            stb.append(",HENSA_HR AS(");
            stb.append(     "SELECT  ROUND( AVG( FLOAT( SCORE ) ) * 100, 0 ) / 100 AS S_AVG, ");
            stb.append(             "ROUND( STDDEV( FLOAT( SCORE ) ) * 100, 0 ) / 100 AS S_HENSA, ");
            stb.append(             "COUNT( SCHREGNO ) AS S_CNT ");
            stb.append(     "FROM    RECORD_A ");
            if( param[12].equals("1") )
                stb.append( "WHERE   CHAIRCD = ? ");
            else
                stb.append( "WHERE   CHAIRCD IN " + param[6] );
            stb.append(")");
                        //テスト日の出欠の表
            stb.append(",TEST_ATTEND AS (");
            stb.append(     "SELECT  T2.SCHREGNO, ");
            stb.append(         " CASE WHEN ATTEND_DI.ATSUB_REPL_DI_CD IS NOT NULL THEN ATTEND_DI.ATSUB_REPL_DI_CD ELSE ATTEND_DI.REP_DI_CD END AS DI_CD ");
            stb.append(     "FROM    SCH_CHR_TEST T1,ATTEND_DAT T2 ");
            stb.append("          LEFT JOIN ATTEND_DI_CD_DAT ATTEND_DI ON ATTEND_DI.YEAR = T2.YEAR AND ATTEND_DI.DI_CD = T2.DI_CD ");
            stb.append(     "WHERE   T1.YEAR = '" + param[0] + "' AND ");
            stb.append(             "T1.SEMESTER = '" + param[1] + "' AND ");
            stb.append(             "T1.TESTKINDCD = '" + param[4] + "' AND ");
            stb.append(             "T1.TESTITEMCD = '" + param[5] + "' AND ");
            if( param[12].equals("1") )
                stb.append(         "T1.CHAIRCD = ? AND ");
            else
                stb.append(         "T1.CHAIRCD IN " + param[6] + " AND ");
            stb.append(             "T2.YEAR = '" + param[0] + "' AND ");
            stb.append(             "T2.CHAIRCD = T1.CHAIRCD AND ");
            stb.append(             "T2.CHAIRCD = T1.CHAIRCD AND ");
            stb.append(             "T2.ATTENDDATE = T1.EXECUTEDATE AND ");
            stb.append(             "T2.PERIODCD = T1.PERIODCD AND ");
            stb.append(             "CASE WHEN ATTEND_DI.ATSUB_REPL_DI_CD IS NOT NULL THEN ATTEND_DI.ATSUB_REPL_DI_CD ELSE ATTEND_DI.REP_DI_CD END IN('1','2','3','4','5','6','8','9','10','11','12','13','14') ");
            stb.append(")");

                        //メインの表
            stb.append("SELECT  T3.GRADE,T3.HR_CLASS,T3.ATTENDNO, ");
            stb.append(        "W3.HR_NAMEABBV || '-' || CHAR( INT( T3.ATTENDNO ) )AS G_H_ATTENDNO, ");
            stb.append(        "T2.NAME, ");
            stb.append(        "VALUE( T4.SC_MOD_SCORE, 0 ) AS SC_MOD_SCORE, ");
            stb.append(        "T4.SC_HENSA, ");
            stb.append(        "T4.SC_RANK, ");
            stb.append(        "T4.SC_AVG, ");
            stb.append(        "T4.SC_CNT, ");
            stb.append(        "VALUE(T5.HR_MOD_SCORE,0) AS HR_MOD_SCORE, ");
            stb.append(        "T5.HR_HENSA, ");
            stb.append(        "T5.HR_RANK, ");
            stb.append(        "T5.HR_AVG, ");
            stb.append(        "T5.HR_CNT, ");
            stb.append(        "CASE WHEN T6.SCHREGNO IS NULL OR 0 < VALUE(T5.HR_MOD_SCORE,0) THEN 1 ELSE 0 END AS ATTEND_FLG ");
            stb.append("FROM    RECORD_A T1 ");
            stb.append(        "INNER JOIN SCHREG_BASE_MST T2 ON T1.SCHREGNO = T2.SCHREGNO ");
            stb.append(        "INNER JOIN SCHREG_REGD_DAT T3 ON T3.YEAR = '" + param[0] + "' AND ");
            stb.append(                                         "T3.SEMESTER = '" + param[1] + "' AND ");
            stb.append(                                         "T3.SCHREGNO = T1.SCHREGNO ");
            stb.append(        "INNER JOIN SCHREG_REGD_HDAT W3 ON W3.YEAR = '" + param[0] + "' AND ");
            stb.append(                                          "W3.SEMESTER = '" + param[1] + "' AND ");
            stb.append(                                          "W3.GRADE = T3.GRADE AND ");
            stb.append(                                          "W3.HR_CLASS = T3.HR_CLASS ");
                                //統計対象全体の席次および偏差値
            stb.append(        "LEFT JOIN (");
            stb.append(            "SELECT  T1.SCHREGNO, ");
            stb.append(                    "SCORE AS SC_MOD_SCORE, ");
            stb.append(                    "CASE S_HENSA WHEN 0 THEN 0 ELSE DECIMAL( ROUND( ( (10 * ( SCORE - S_AVG )) / S_HENSA + 50 )* 10 , 0 ) / 10 , 5, 1 ) END AS SC_HENSA, ");
            stb.append(                    "RANK() OVER( ORDER BY SCORE DESC )AS SC_RANK, ");
            stb.append(                    "T1.CHAIRCD, ");
            stb.append(                    "DECIMAL( ROUND( T2.S_AVG, 1 ), 5, 1 ) AS SC_AVG, ");
            stb.append(                    "T2.S_CNT AS SC_CNT ");
            stb.append(            "FROM    RECORD_A T1, ");
            stb.append(                    "HENSA_SC T2 ");
            stb.append(        ")T4 ON T1.SCHREGNO = T4.SCHREGNO ");

                                //対象講座の席次および偏差値
            stb.append(        "LEFT JOIN (");
            stb.append(            "SELECT  T1.SCHREGNO, ");
            stb.append(                    "SCORE AS HR_MOD_SCORE, ");
            stb.append(                    "CASE S_HENSA WHEN 0 THEN 0 ELSE DECIMAL( ROUND( ( (10 * ( SCORE - S_AVG )) / S_HENSA + 50 ) * 10 , 0 ) / 10, 5, 1 ) END AS HR_HENSA, ");
            stb.append(                    "RANK() OVER( ORDER BY SCORE DESC )AS HR_RANK, ");
            stb.append(                    "CHAIRCD, ");
            stb.append(                    "DECIMAL( ROUND( S_AVG, 1 ), 5, 1 ) AS HR_AVG, ");
            stb.append(                    "S_CNT AS HR_CNT ");
            stb.append(            "FROM    RECORD_A T1, ");
            stb.append(                    "HENSA_HR T2 ");
            if( param[12].equals("1") ) {
                stb.append(        "WHERE   CHAIRCD = ? ");
            } else {
                stb.append(        "WHERE   CHAIRCD IN " + param[6] );
            }
            stb.append(        ")T5 ON T1.SCHREGNO = T5.SCHREGNO ");

            stb.append(        "LEFT JOIN TEST_ATTEND T6 ON T6.SCHREGNO = T1.SCHREGNO ");

            if( param[12].equals("1") ) {
                stb.append( "WHERE   T1.CHAIRCD = ? ");
            } else {
                stb.append( "WHERE   T1.CHAIRCD IN " + param[6] + " ");
            }

            if( param[11].equals("2") ) {
                stb.append("ORDER BY T4.SC_RANK, T3.GRADE, T3.HR_CLASS, T3.ATTENDNO, T4.SC_HENSA");
            } else {
                stb.append("ORDER BY T3.GRADE, T3.HR_CLASS, T3.ATTENDNO, T4.SC_HENSA");
            }

        } catch( Exception e ){
            log.debug("Pre_Stat() error! ", e );
        }

        return stb.toString();

    }//pre_stat_fの括り


}
