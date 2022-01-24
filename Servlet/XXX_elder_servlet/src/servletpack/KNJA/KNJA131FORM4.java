// kanji=漢字
/*
 * $Id: 54f4e31310026f5dde852dcc06c3ab584d4bde06 $
 *
 * 作成日: 
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJA;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJDefineCode;

/**
 *
 *  学校教育システム 賢者 [学籍管理]  生徒指導要録  中等教育学校用（千代田区立九段）
 *                                                    各教科・科目の学習の記録（後期課程）
 *
 *  2005/12/27 Build yamashiro
 *  2006/04/13 yamashiro・評定および単位がNULLの場合は出力しない（'0'と出力しない）--NO001
 */

public class KNJA131FORM4 extends KNJA131BASE
{
    private static final Log log = LogFactory.getLog(KNJA131FORM4.class);

    private StringBuffer stb;
    private ResultSet rs;
    private int counter = 0;                //行数カウンター
    private String classcd;
    private String schoolKind;
    private String curriculumCd;
    private String classname;

    /**
     *  SVF-FORM 印刷処理
     */
    boolean printSvf( DB2UDB db2, Vrw32alp svf, Map paramap )
    {
        boolean nonedata = false;
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        counter = 0;
        classcd = null;
        classname = null;
        try {
            setSvfForm( svf, paramap );
            printHeader( db2, svf, paramap );  //年次・ホームルーム・整理番号
            printSvfDetail_1( db2, svf, paramap );
            ret = svf.VrPrint();
            nonedata = true;
        } catch( Exception ex ){
            log.debug( "printSvf error!", ex );
        }
        return nonedata;
    }


    /**
     *  SVF-FORM設定
     */
    void setSvfForm( Vrw32alp svf, Map paramap )
    {
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        try {
            ret = svf.VrSetForm("KNJA131_4.frm", 4);
        } catch( Exception ex ){
            log.debug( "svf.VrSetForm error!", ex );
        }
    }


    /**
     *  SVF-FORM 印刷処理 明細
     *  学習記録データ
     */
    void printSvfDetail_1( DB2UDB db2, Vrw32alp svf, Map paramap )
    {
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        try {
            int p = 0;
            ( ( PreparedStatement )paramap.get("PS_VALUES_RECORD") ).setString( ++p, (String)paramap.get("SCHNO") );    //学籍番号
            ( ( PreparedStatement )paramap.get("PS_VALUES_RECORD") ).setString( ++p, (String)paramap.get("YEAR")  );    //年度
            ( ( PreparedStatement )paramap.get("PS_VALUES_RECORD") ).setString( ++p, (String)paramap.get("SCHNO") );    //学籍番号
            ( ( PreparedStatement )paramap.get("PS_VALUES_RECORD") ).setString( ++p, (String)paramap.get("SCHNO")  );   //学籍番号
            ( ( PreparedStatement )paramap.get("PS_VALUES_RECORD") ).setString( ++p, (String)paramap.get("YEAR")  );    //年度
            ( ( PreparedStatement )paramap.get("PS_VALUES_RECORD") ).setInt( ++p, Integer.parseInt( (String)paramap.get("YEAR") )  );   //年度
//            ( ( PreparedStatement )paramap.get("PS_VALUES_RECORD") ).setString( ++p, (String)paramap.get("SCHNO") );    //学籍番号
//            ( ( PreparedStatement )paramap.get("PS_VALUES_RECORD") ).setInt( ++p, Integer.parseInt( (String)paramap.get("YEAR") )  );   //年度
            rs = ( ( PreparedStatement )paramap.get("PS_VALUES_RECORD") ).executeQuery();

            final String useCurriculumcd = (String) paramap.get("useCurriculumcd");

            String subclasscd = null;   //科目コード
            List outlist = new ArrayList();        //通常科目の教科ごとリスト
            int outlistother[][] = new int[4][4];  //総合学習・小計・留学・合計のリスト
            Map outmap = new HashMap();

            while ( rs.next() ){
                if( doOtherClassAccumu( rs, outlistother, outmap ) )continue;  //教科外単位累積＆合計累積
                //科目コードのブレイク
                final boolean isDifferentSubclasscd;
                if ("1".equals(useCurriculumcd)) {
                    isDifferentSubclasscd = !(classcd.equals( rs.getString("CLASSCD") ) && schoolKind.equals(rs.getString("SCHOOL_KIND")) && curriculumCd.equals(rs.getString("CURRICULUM_CD")) && subclasscd.equals( rs.getString("SUBCLASSCD") ));
                } else {
                    isDifferentSubclasscd = ! subclasscd.equals( rs.getString("SUBCLASSCD") );
                }
                if( subclasscd == null  ||  isDifferentSubclasscd ){
                    if( subclasscd != null )saveSubclassValues( outlist, outmap );   //科目単位の保管処理
                    //教科コードのブレイク
                    final boolean isDifferentClasscd;
                    if ("1".equals(useCurriculumcd)) {
                        isDifferentClasscd = !(classcd.equals( rs.getString("CLASSCD") ) && schoolKind.equals(rs.getString("SCHOOL_KIND")) && curriculumCd.equals(rs.getString("CURRICULUM_CD")));
                    } else {
                        isDifferentClasscd = !classcd.equals( rs.getString("CLASSCD") );
                    }
                    if( classcd == null  || isDifferentClasscd ){
                        if( classcd != null )outClassValues( svf, outlist );   //教科ごとの印刷処理
                        outlist.clear();
                        classcd = rs.getString("CLASSCD");
                        if ("1".equals(useCurriculumcd)) {
                            schoolKind = rs.getString("SCHOOL_KIND");
                            curriculumCd = rs.getString("CURRICULUM_CD");
                        }
                        classname = rs.getString("CLASSNAME");
                    }
                    outmap.clear();
                    outmap.put( "CLASSCD",  rs.getString("CLASSCD") );
                    outmap.put( "SUBCLASSNAME",  rs.getString("SUBCLASSNAME") );
                    subclasscd = rs.getString("SUBCLASSCD");
                }
                //明細の保管
                saveGradeValues( rs, outmap );
            }
            if( subclasscd != null ){
                saveSubclassValues( outlist, outmap );   //科目単位の保管処理
                outClassValues( svf, outlist );   //教科ごとの印刷処理
            }
            rs.close();
            outClassValuesOther( svf, outlistother );   //標準教科以外の印刷処理
        } catch( Exception ex ){
            log.debug( "set_detail study_info error!", ex );
        }
    }


    /**
     *  教科ごとの印刷処理
     */
    public void outClassValues( Vrw32alp svf, List outlist )
    {
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        try {
            //教科ごとに、n行空行を出力する
            int n = 0;
            Map outmap = new HashMap();
            for( int i = 0;  i < n;  i++ ){
                outmap.put( "CLASSCD",  new String( classcd ) );
                outlist.add( new HashMap( outmap ) );
                outmap.clear();
            }
            //教科名をLISTへ挿入
            editClassName( outlist );
            //ページ内に収まらない場合は改ページ
            if( 64 < counter + outlist.size() )for( int i = counter; i < 64; i++ ){
                ret = svf.VrsOut( "CLASSCD",  classcd );        //教科コード
                ret = svf.VrEndRecord();
                counter++;
            }

            //印刷処理
            for( int i = 0; i < outlist.size()  &&  i < 10 ; i++ )svfprintDetail( svf, (HashMap)outlist.get(i) );

        } catch( Exception ex ){
            log.debug("outClassValues", ex );
        }
    }


    /**
     *  科目別明細出力
     */
    public void svfprintDetail( Vrw32alp svf, Map outmap )
    {
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        Set outset = outmap.keySet();
        try {
            String str = null;
            int g = 0;
            for( Iterator i = outset.iterator(); i.hasNext(); ){
                str = (String)i.next();
                if( str.equals("CLASSCD") )
                    ret = svf.VrsOut( "CLASSCD",  (String)outmap.get("CLASSCD") );        //教科コード
                else if( str.equals("CLASSNAME") )
                    ret = svf.VrsOut( "CLASS",  (String)outmap.get("CLASSNAME") );        //教科名
                else if( str.equals("SUBCLASSNAME") )
                    ret = svf.VrsOut( "SUBCLASS",  (String)outmap.get("SUBCLASSNAME") );  //科目名
                else{
                    g = Integer.parseInt( str.substring( 0, 1 ) );  //学年
                    if( ( str.substring( 1, str.length() ) ).equals("VALUE") )
                        ret = svf.VrsOut( "VALUE" + g,  (String)outmap.get(str) );     //学年別評定
                    else{
                        if( g == 0 ) {
                            // ret = svf.VrsOut( "TOTAL_CREDIT1",  (String)outmap.get(str) );  //科目別修得単位数
                        } else {
                            ret = svf.VrsOut( "CREDIT1_" + g,   (String)outmap.get(str) );  //学年別修得単位数
                        }
                    }
                }
            }

            ret = svf.VrEndRecord();
            counter++;
        } catch( Exception ex ){
            log.debug("svfprintDetail", ex );
        }
    }


    /**
     *  教科名の編集処理
     */
    public void editClassName( List outlist )
    {
        Map outmap = new HashMap();
        try {
            final int n = 0;
            // int c = ( classname.length() + n > outlist.size() )? classname.length() + n: outlist.size();
            // int d = ( c - classname.length() ) / 2;
            int d = 0;
            for( int i = 0; i < classname.length(); i++ ){
                if( i + d < outlist.size() ){
                    ( (HashMap)outlist.get( i + d ) ).put( "CLASSNAME",  classname.substring( i, i + 1 ) );
                } else{
                    outmap.put( "CLASSCD",  new String( classcd ) );
                    if( i < classname.length() )outmap.put( "CLASSNAME",  classname.substring( i, i + 1 ) );
                    outlist.add( new HashMap( outmap ) );
                    outmap.clear();
                }
            }
            if( outlist.size() < classname.length() + n ){
                outmap.put( "CLASSCD",  new String( classcd ) );
                outlist.add( new HashMap( outmap ) );
            }
        } catch( Exception ex ){
            log.debug("outClassValues", ex );
        }
    }


    /**
     *  科目別データの保管処理
     */
    public void saveSubclassValues( List outlist, Map outmap )
    {
        try {
            outlist.add( new HashMap(outmap) );
        } catch( Exception ex ){
            log.debug("saveSubclassValues", ex );
        }
    }


    /**
     *  明細データの保管処理
     */
    public void saveGradeValues( ResultSet rs, Map outmap )
    {
        try {
            int g = Integer.parseInt( rs.getString("ANNUAL") );
            if( 3 < g )g -= 3;
            if( rs.getString("GRADES")       != null )outmap.put( g + "VALUE",   String.valueOf(rs.getInt("GRADES")) );  //学年別評定
            if( ! outmap.containsKey("0CREDIT")  &&  rs.getString("CREDIT") != null )outmap.put( "0CREDIT",  rs.getString("CREDIT") );  //学年別単位
            if( 0 < g  &&  rs.getString("GRADE_CREDIT") != null )outmap.put( g + "CREDIT",  rs.getString("GRADE_CREDIT") );  //学年別単位
//log.debug("annual="+g + "   outmap="+outmap);
        } catch( Exception ex ){
            log.debug("saveGradeValues", ex );
        }
    }


    /**
     *  総合学習・小計・留学・合計の単位数累積処理
     */
    public boolean doOtherClassAccumu( ResultSet rs, int outlistother[][], Map outmap )
    {
        boolean retboo = false;
        try {
            int g = Integer.parseInt( rs.getString("ANNUAL") );
            if( 3 < g )g -= 3;
            int i = ( rs.getString("CLASSNAME").equals("sogo")   )? 0:
                    ( rs.getString("CLASSNAME").equals("total")  )? 1:
                    ( rs.getString("CLASSNAME").equals("abroad") )? 2: 3;

//log.debug("g="+g+"    i="+i+"   credit="+rs.getString("CREDIT")+"   grade_credit="+rs.getString("GRADE_CREDIT"));
            if( i < 3 ){
                if( g == 0 ){
                    if( rs.getString("CREDIT")       != null ){
                        outlistother[i][g] += Integer.parseInt( rs.getString("CREDIT") );  //学年別単位
                        outlistother[3][g] += Integer.parseInt( rs.getString("CREDIT") );  //学年別単位
                    }
                } else{
                    //NO001 if( rs.getString("GRADE_CREDIT") != null ){
                    if( rs.getString("CREDIT") != null ){  //NO001
                        outlistother[i][g] += Integer.parseInt( rs.getString("CREDIT") );  //学年別単位  NO001
                        outlistother[3][g] += Integer.parseInt( rs.getString("CREDIT") );  //学年別単位  NO001
                        //NO001 outlistother[i][g] += Integer.parseInt( rs.getString("GRADE_CREDIT") );  //学年別単位
                        //NO001 outlistother[3][g] += Integer.parseInt( rs.getString("GRADE_CREDIT") );  //学年別単位
                    }
                }
                retboo = true;
            }

        } catch( Exception ex ){
            log.debug("prepareSqlState", ex );
        }
        return retboo;
    }


    /**
     *  標準教科以外の印刷処理
     */
    public void outClassValuesOther( Vrw32alp svf, int outlistother[][] )
    {
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        try {
            classcd = "99";
            //ページ内に収まらない場合は改ページ
            if( 64 <  counter + outlistother.length )for( int i = counter; i < 64; i++ ){
                ret = svf.VrsOut( "CLASSCD",  classcd );        //教科コード
                ret = svf.VrEndRecord();
                counter++;
            }
            for( int i = ( counter % 64 ); i < 60; i++ ){
                ret = svf.VrsOut( "CLASSCD",  classcd );        //教科コード
                ret = svf.VrEndRecord();
                counter++;
            }
            //印刷処理
//for( int l = 0; l < outlistother.length; l++ )for( int m = 0; m < outlistother[l].length; m++ )log.debug("outlistother["+l+"]["+m+"]="+outlistother[l][m]);
            for( int i = 0; i < outlistother.length; i++ ){
                ret = svf.VrsOut( "ITEM",  ( i == 0 )? "総合的な学習の時間": ( i == 1 )? "小     計": ( i == 2 )? "留     学": "合     計" );  //科目名
                for( int j = 0; j < outlistother[i].length; j++ ){
                    if( 0 < outlistother[i][j] ){  //--NO001 0は出力しない条件を追加
                        if( j == 0 ) {
                            // ret = svf.VrsOut( "TOTAL_CREDIT2",  String.valueOf( outlistother[i][j] ) );  //修得単位数計
                        } else {
                            ret = svf.VrsOut( "CREDIT2_" + j,  String.valueOf( outlistother[i][j] ) );  //学年別修得単位数
                        }
                    }
                }
                ret = svf.VrEndRecord();
                counter++;
            }
        } catch( Exception ex ){
            log.debug("outClassValues", ex );
        }
    }


    /**
     *  PrepareStatementオブジェクト作成
     */
    public void prepareSqlState( DB2UDB db2, Map paramap, KNJDefineCode definecode )
    {
        try {
            if( paramap.containsKey("PS_VALUES_RECORD") )return;
            //成績データ
            if( ! paramap.containsKey("PS_VALUES_RECORD") ){
                
                StudyrecSql obj = new StudyrecSql("grade","hyde",2,false,definecode,0,1, (String) paramap.get("useCurriculumcd"));
                paramap.put("PS_VALUES_RECORD",  db2.prepareStatement( obj.pre_sql() ) );
                obj = null;
            }
            prepareSqlStateSub(db2, paramap);
        } catch( Exception ex ){
            log.debug("prepareSqlState", ex );
        }
    }


    /**
     *  PrepareStatement close
     */
    public void closePrepareState( DB2UDB db2, Map paramap )
    {
        try {
            if( ! paramap.containsKey("PS_VALUES_RECORD")     )( ( PreparedStatement )paramap.get("PS_VALUES_RECORD")     ).close();
        } catch( Exception ex ){
            log.debug( "closePrepareState error!", ex );
        }
    }


}
