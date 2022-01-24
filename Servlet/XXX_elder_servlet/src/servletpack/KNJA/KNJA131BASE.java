// kanji=漢字
/*
 * $Id: 513d474a6cba3628ebd584f8426c40bc3bfcba8b $
 *
 * 作成日: 
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJA;

import java.util.Map;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJA.detail.KNJ_GradeRecSql;
import servletpack.KNJE.detail.KNJ_StudyrecSqlYear;
import servletpack.KNJZ.detail.KNJDefineCode;
import servletpack.KNJZ.detail.KNJEditString;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_Get_Info;
import servletpack.KNJZ.detail.KNJ_PersonalinfoSql;
import servletpack.KNJZ.detail.KNJ_SchoolinfoSql;

/**
 *
 *  学校教育システム 賢者 [学籍管理]  生徒指導要録  中等教育学校用（千代田区立九段） 様式1(学籍に関する記録)
 *
 *  2005/12/27 Build yamashiro
 */

abstract class KNJA131BASE
{
    private static final Log log = LogFactory.getLog(KNJA131BASE.class);
    KNJEditString editstringobj = new KNJEditString();


    /**
     *  SVF-FORM設定
     */
    abstract void setSvfForm( Vrw32alp svf, Map paramap);


    /**
     *  SVF-FORM 印刷処理
     */
    abstract boolean printSvf( DB2UDB db2, Vrw32alp svf, Map paramap );


    /**
     *  PrepareStatementオブジェクト作成
     */
    abstract void prepareSqlState( DB2UDB db2, Map paramap, KNJDefineCode definecode );


    /**
     *  SQL オブジェクトをクローズ
     */
    abstract void closePrepareState( DB2UDB db2, Map paramap );


    /**
     * 学校・個人・学籍等履歴のdb2.prepareStatementをMapに追加します。
     * @param db2
     * @param paramap
     * @throws Exception
     */
    protected void prepareSqlStateSub(
            final DB2UDB db2, 
            final Map paramap
    ) throws Exception {
        //学校データ
        if( ! paramap.containsKey("PS_SCHOOL_INFO") ){
            paramap.put("PS_SCHOOL_INFO",  db2.prepareStatement( new KNJ_SchoolinfoSql("10000").pre_sql() ) );
        }
        //個人学籍データ
        if( ! paramap.containsKey("PS_SCHREG_INFO") ){
            paramap.put("PS_SCHREG_INFO",  db2.prepareStatement( new KNJ_PersonalinfoSql().sql_info_reg("1111111000") ) );
        }
        //学籍等履歴
        if( ! paramap.containsKey("PS_REGD_RECORD") ){
            String useSchregRegdHdat = (paramap.get("useSchregRegdHdat") == null ? null : (String) paramap.get("useSchregRegdHdat")); 
            paramap.put("PS_REGD_RECORD",  db2.prepareStatement( new KNJ_GradeRecSql().sql_state(useSchregRegdHdat) ) );
        }
    }


    /**
     *  SVF-FORM 印刷処理 明細
     *  個人情報  学籍等履歴情報
     */
    void printHeader(
            final DB2UDB db2, 
            final Vrw32alp svf, 
            final Map paramap
    ) {
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        try {
            if( editstringobj == null )editstringobj = new KNJEditString();
        } catch( Exception ex ){
            log.debug( "printSvfDetail_5 error!", ex );
        }
        ResultSet rs = null;        
        try {
            //生徒名取得および印刷
            if( ! paramap.containsKey("SCHNAME") ){
                int p = 0;
                ( ( PreparedStatement )paramap.get("PS_SCHREG_INFO") ).setString( ++p, (String)paramap.get("SCHNO") );  //学籍番号
                ( ( PreparedStatement )paramap.get("PS_SCHREG_INFO") ).setString( ++p, (String)paramap.get("YEAR")  );  //年度
                ( ( PreparedStatement )paramap.get("PS_SCHREG_INFO") ).setString( ++p, (String)paramap.get("GAKKI") );  //学期
                ( ( PreparedStatement )paramap.get("PS_SCHREG_INFO") ).setString( ++p, (String)paramap.get("SCHNO") );  //学籍番号
                ( ( PreparedStatement )paramap.get("PS_SCHREG_INFO") ).setString( ++p, (String)paramap.get("YEAR")  );  //年度
                rs = ( ( PreparedStatement )paramap.get("PS_SCHREG_INFO") ).executeQuery();
                if ( rs.next() )paramap.put("SCHNAME",  rs.getString("NAME") );
            }
            ret = svf.VrsOut( "NAME",  (String)paramap.get("SCHNAME") );
        } catch( Exception ex ){
            log.debug( "printSvfDetail_5 SCHREG_INFO error!", ex );
        }

        try {
            //学校名称の取得および印刷
            if( ! paramap.containsKey("SCHOOLNAME") ){
                int p = 0;
                ( ( PreparedStatement )paramap.get("PS_SCHOOL_INFO") ).setString( ++p, (String)paramap.get("YEAR") );   //年度
                ( ( PreparedStatement )paramap.get("PS_SCHOOL_INFO") ).setString( ++p, (String)paramap.get("YEAR") );   //年度
                rs = ( ( PreparedStatement )paramap.get("PS_SCHOOL_INFO") ).executeQuery();
                if( rs.next() )paramap.put("SCHOOLNAME",  rs.getString("SCHOOLNAME1") );
            }
            //学校名称の出力
            int n1 = 0;
            n1 = editstringobj.retStringByteValue( (String)paramap.get("SCHOOLNAME"), 60 );
            if( 50 < n1 )ret = svf.VrsOut( "SCHOOLNAME2",  (String)paramap.get("SCHOOLNAME") );
            else if(  0 < n1 )ret = svf.VrsOut( "SCHOOLNAME1",  (String)paramap.get("SCHOOLNAME") );
        } catch( Exception ex ){
            log.debug( "printSvfDetail_5 SCHOOLNAME error!", ex );
        }

        Map hmap = null;
        try {
            //学籍履歴の取得および印刷
            int p = 0;
            ( ( PreparedStatement )paramap.get("PS_REGD_RECORD") ).setString( ++p, (String)paramap.get("SCHNO") );  //学籍番号
            ( ( PreparedStatement )paramap.get("PS_REGD_RECORD") ).setString( ++p, (String)paramap.get("SCHNO") );  //学籍番号
            ( ( PreparedStatement )paramap.get("PS_REGD_RECORD") ).setString( ++p, (String)paramap.get("YEAR")  );  //年度
            ( ( PreparedStatement )paramap.get("PS_REGD_RECORD") ).setString( ++p, (String)paramap.get("YEAR")  );  //年度
            rs = ( ( PreparedStatement )paramap.get("PS_REGD_RECORD") ).executeQuery();
            if( hmap == null )hmap = KNJ_Get_Info.getMapForHrclassName( db2 );  //NO003 表示用組

            int i = 0;
            while( rs.next() ){
                i = Integer.parseInt( rs.getString("GRADE") );
                ret = svf.VrsOutn( "HR_NAME",   i,  KNJ_EditEdit.Ret_Num_Str( rs.getString("HR_CLASS"), hmap ) );   //組 04/08/18Modify  NO003 Modify
                //NO003 ret = svf.VrsOutn( "HR_NAME",   i,  KNJ_EditEdit.Ret_Num_Str( rs.getString("HR_CLASS") ) ); //組 04/08/18Modify
                ret = svf.VrsOutn( "ATTENDNO",  i,  String.valueOf( Integer.parseInt( rs.getString("ATTENDNO") ) ) );    //出席番号
            }
            rs.close();
        } catch( Exception ex ){
            log.debug( "printSvfDetail_5 _REGD_RECORD error!", ex );
        }
    }

    /**
     * "[w] * [h]"サイズタイプのパラメータのwもしくはhを整数で返す
     * @param param サイズタイプのパラメータ文字列
     * @param pos split後のインデクス (0:w, 1:h)
     * @return "[w] * [h]"サイズタイプのパラメータのwもしくはhの整数値
     */
    protected static int getParamSizeNum(final String param, final int pos) {
        int num = -1;
        String[] nums = StringUtils.split(param, " * ");
        if (StringUtils.isBlank(param) || !(0 <= pos && pos < nums.length)) {
            num = -1;
        } else {
            try {
                num = Integer.valueOf(nums[pos]).intValue();
            } catch (Exception e) {
                log.error("Exception!", e);
            }
        }
        return num;
    }

    /**
     * <<学習記録データのＳＱＬ>>。
     * @author yamasiro
     * @version $Id: 513d474a6cba3628ebd584f8426c40bc3bfcba8b $
     */
    class StudyrecSql extends KNJ_StudyrecSqlYear {
        
        /**
         * コンストラクタ。
         * @param hyoutei
         * @param atype
         * @param stype
         * @param english
         * @param definecode
         * @param daiken_div_code
         * @param shidouyouroku
         */
        StudyrecSql(
                final String hyoutei,
                final String atype,
                final int stype,
                final boolean english,
                final KNJDefineCode definecode,
                final int daiken_div_code,
                final int shidouyouroku,
                final String useCurriculumcd
        ) {
            super(hyoutei, atype, stype, english, definecode, daiken_div_code, shidouyouroku, useCurriculumcd);
        }

        /**
         * {@inheritDoc}
         */
        protected String pre_sqlAbraod() {
            if (log.isDebugEnabled()) {log.debug("これはKNJA131BASE.pre_sqlAbraod()です。");}
            StringBuffer stb = new StringBuffer();
            stb.append(" UNION");
            stb.append(" SELECT  ");
            if (definecode.schooldiv.equals("1")) {
                stb.append(" VALUE(YEAR,'0') ");
            } else {
                stb.append(" VALUE(ANNUAL,'0') ");
            }           
            stb.append("  AS ANNUAL");
            stb.append("            ,'AA' AS CLASSCD");
            if ("1".equals(_useCurriculumcd)) {
                stb.append(" ,'AAAA' AS SCHOOL_KIND ");
                stb.append(" ,'AAAA' AS CURRICULUM_CD ");
            }
            stb.append("            ,'abroad' AS CLASSNAME");
            stb.append("            ,'AAAA' AS SUBCLASSCD,'abroad' AS SUBCLASSNAME");
            stb.append("            ,0 AS GRADES,0 AS AVG_GRADES,'' AS ASSESS_LEVEL,0 AS GRADE_CREDIT,SUM(ABROAD_CREDITS) AS CREDIT");
            stb.append("            ,'0' AS SCHOOLCD");
            if (1 == _shidouyouroku) {
                stb.append("        ,0 AS SHOWORDERCLASS, 0 AS SHOWORDERSUBCLASS");
            }
            stb.append("     FROM(");
            stb.append("          SELECT  ABROAD_CREDITS,INT(FISCALYEAR(TRANSFER_SDATE)) AS TRANSFER_YEAR");
            stb.append("          FROM ");
            stb.append(                   tname2);
            stb.append("          WHERE   SCHREGNO = ? AND TRANSFERCD = '1'");
            stb.append("     )ST1,");
            stb.append("     (");
            stb.append("      SELECT  ");
            if (definecode.schooldiv.equals("1")) {
                stb.append("      YEAR");
            } else {
                stb.append("      ANNUAL,MAX(YEAR) AS YEAR");
            }           
            stb.append("          FROM ");
            stb.append(                   tname3);
            stb.append("          WHERE   SCHREGNO = ? AND YEAR <= ? AND '03' < GRADE");
            stb.append("      GROUP BY ");
            if (definecode.schooldiv.equals("1")) {
                stb.append("      YEAR");
            } else {
                stb.append("      ANNUAL");
            }           
            stb.append("     )ST2 ");
            stb.append("     WHERE  ST1.TRANSFER_YEAR <= ? AND INTEGER(ST2.YEAR) = ST1.TRANSFER_YEAR");
            stb.append(" GROUP BY GROUPING SETS (");
            if (definecode.schooldiv.equals("1")) {
                stb.append(" YEAR ");
            } else {
                stb.append(" ANNUAL ");
            }           
            stb.append("   , ())");
            if (2 > stype) {              //----->学年別/合計
                stb.append(" HAVING ANNUAL IS NOT NULL ");
            }
            return stb.toString();
        }

        /**
         * {@inheritDoc}
         */
        protected String pre_sql_Common() {
            if (log.isDebugEnabled()) {log.debug("これはKNJA131BASE.pre_sql_Common()です。");}
            StringBuffer stb = new StringBuffer();
            stb.append("SELECT  SCHREGNO, YEAR, ANNUAL, CLASSCD ");
            if ("1".equals(_useCurriculumcd)) {
                stb.append(" ,SCHOOL_KIND ");
                stb.append(" ,CURRICULUM_CD ");
            }
            stb.append("       ,SUBCLASSCD, VALUATION AS GRADES ");
            stb.append(       ",CASE WHEN T1.ADD_CREDIT IS NOT NULL THEN VALUE(T1.GET_CREDIT,0) + VALUE(T1.ADD_CREDIT,0) ELSE T1.GET_CREDIT END AS CREDIT ");
            stb.append("       ,CLASSNAME,CLASSNAME_ENG,SUBCLASSNAME,SUBCLASSNAME_ENG ");
            stb.append("       ,SCHOOLCD ");
            stb.append("FROM   " + tname1 + " T1 ");
            stb.append("WHERE   T1.SCHREGNO = ? AND YEAR <= ?");
            stb.append("    AND '03' < T1.ANNUAL");
            stb.append("    AND (CLASSCD BETWEEN '" + KNJDefineCode.subject_D+"' AND '" + KNJDefineCode.subject_U+"' OR CLASSCD = '" + KNJDefineCode.subject_T+"') ");
            if (_shidouyouroku == 0) {
                stb.append("AND T1.SCHOOLCD = '0' ");  // 本校区分が本校のみ。
            }
            if (_daiken_div_code == 1) {
                stb.append("UNION ");
                stb.append("SELECT  SCHREGNO, YEAR, ANNUAL, CLASSCD ");
                if ("1".equals(_useCurriculumcd)) {
                    stb.append(" ,SCHOOL_KIND ");
                    stb.append(" ,CURRICULUM_CD ");
                }
                stb.append("       ,SUBCLASSCD, VALUATION AS GRADES ");
                stb.append(       ",CASE WHEN T1.ADD_CREDIT IS NOT NULL THEN VALUE(T1.GET_CREDIT,0) + VALUE(T1.ADD_CREDIT,0) ELSE T1.GET_CREDIT END AS CREDIT ");
                stb.append("       ,CLASSNAME,CLASSNAME_ENG,SUBCLASSNAME,SUBCLASSNAME_ENG ");
                stb.append("       ,SCHOOLCD ");
                stb.append("FROM   " + tname1 + " T1 ");
                stb.append("WHERE   T1.SCHREGNO = ? AND YEAR <= ?");
                stb.append("    AND T1.SCHOOLCD = '2' ");  // 本校区分が資格
                stb.append("    AND T1.GET_CREDIT IS NOT NULL ");
            }
            return stb.toString();
        }
    }

}
