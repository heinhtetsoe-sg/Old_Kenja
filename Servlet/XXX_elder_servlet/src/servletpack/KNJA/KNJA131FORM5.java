// kanji=漢字
/*
 * $Id: 5742618cd2be484f6e7f4d72a166e17c12741cbc $
 *
 * 作成日: 
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJA;

import java.util.ArrayList;
import java.util.Map;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJDefineCode;
import servletpack.KNJZ.detail.KNJEditString;
import servletpack.KNJZ.detail.KNJObjectAbs;

/**
 *
 *  学校教育システム 賢者 [学籍管理]  生徒指導要録  中等教育学校用（千代田区立九段）
 *                                                    総合的な学習の時間の記録
 *                                                    行動の記録
 *                                                    特別活動の記録
 *                                                    総合所見及び指導上参考となる事項
 *                                                    出欠の記録
 *
 *  2005/12/27 Build yamashiro
 *  2006/04/10 yamashiro テーブルHTRAINREMARK_DATのフィールド長変更による不具合対応 --NO001
 *  2006/04/17 yamashiro 出欠備考の出力処理変更 --NO002
 */

public class KNJA131FORM5 extends KNJA131BASE
{
    private static final Log log = LogFactory.getLog(KNJA131FORM5.class);
    private StringBuffer stb;
    private ResultSet rs;
    public KNJObjectAbs knjobj;         //編集用クラス
    public ArrayList arrlist;

    /**
     *  SVF-FORM 印刷処理
     */
    boolean printSvf( DB2UDB db2, Vrw32alp svf, Map paramap )
    {
        boolean nonedata = false;
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        try {
            setSvfForm( svf, paramap );
            knjobj = new KNJEditString();
            printHeader( db2, svf, paramap );  //年次・ホームルーム・整理番号
            printSvfDetail_1( db2, svf, paramap );  //総合的な学習の時間の記録・総合所見
            //NO001 printSvfDetail_2( db2, svf, paramap );  //行動の記録備考・特別活動の記録・出欠の記録備考
            printSvfDetail_3( db2, svf, paramap );  //行動の記録・特別活動の記録
            printSvfDetail_4( db2, svf, paramap );  //出欠の記録
            ret = svf.VrEndPage();
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
            ret = svf.VrSetForm("KNJA131_5.frm", 1);
        } catch( Exception ex ){
            log.debug( "svf.VrSetForm error!", ex );
        }
    }


    /**
     *  SVF-FORM 印刷処理 明細
     *  総合的な学習の時間の記録・総合所見
     */
    void printSvfDetail_1( DB2UDB db2, Vrw32alp svf, Map paramap )
    {
        //ArrayList arrlist = null;
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        try {
            int p = 0;
            ( ( PreparedStatement )paramap.get("PS_REMARK_RECORD") ).setString( ++p, (String)paramap.get("YEAR")  );    //年度
            ( ( PreparedStatement )paramap.get("PS_REMARK_RECORD") ).setString( ++p, (String)paramap.get("SCHNO") );    //学籍番号
            rs = ( ( PreparedStatement )paramap.get("PS_REMARK_RECORD") ).executeQuery();
            int g = 0;
            while( rs.next() ){
                g = Integer.parseInt( rs.getString("ANNUAL") );

                arrlist = knjobj.retDividString( rs.getString("TOTALSTUDYACT"), 40, 3 );
                if( arrlist != null )for( int i = 0 ; i < arrlist.size() ; i++ )ret = svf.VrsOutn("TOTAL_ACT"   + g ,  i + 1,  (String)arrlist.get(i) ); //学習活動

                arrlist = knjobj.retDividString( rs.getString("VIEWREMARK"),    40, 3 );
                if( arrlist != null )for( int i = 0 ; i < arrlist.size() ; i++ )ret = svf.VrsOutn("TOTAL_VIEW"  + g ,  i + 1,  (String)arrlist.get(i) ); //観点

                arrlist = knjobj.retDividString( rs.getString("TOTALSTUDYVAL"), 88, 3 );
                if( arrlist != null )for( int i = 0 ; i < arrlist.size() ; i++ )ret = svf.VrsOutn("TOTAL_VALUE" + g ,  i + 1,  (String)arrlist.get(i) ); //評価

                arrlist = knjobj.retDividString( rs.getString("TOTALREMARK"),  176, 3 );
                if( arrlist != null )for( int i = 0 ; i < arrlist.size() ; i++ )ret = svf.VrsOutn("TOTALREMARK" + g ,  i + 1,  (String)arrlist.get(i) ); //総合所見

                //NO001 Add
                arrlist = knjobj.retDividString( rs.getString("BEHAVEREC_REMARK"), 40, 2 );
                if( arrlist != null )for( int i = 0 ; i < arrlist.size() ; i++ )ret = svf.VrsOutn("ACT_REMARK"   + g ,  i + 1,  (String)arrlist.get(i) ); //行動の記録備考

                arrlist = knjobj.retDividString( rs.getString("SPECIALACTREMARK"), 44, 3 );
                if( arrlist != null )for( int i = 0 ; i < arrlist.size() ; i++ )ret = svf.VrsOutn("SPECIALACT"   + g ,  i + 1,  (String)arrlist.get(i) ); //特別活動の記録備考

                //NO002 if( rs.getString("ATTENDREC_REMARK") != null )ret = svf.VrsOutn("ATTEND_REMARK1",  g,  rs.getString("ATTENDREC_REMARK") ); //出欠の記録備考
                arrlist = knjobj.retDividString( rs.getString("ATTENDREC_REMARK"), 40, 2 );  //NO002
                if( arrlist != null )for( int i = 0 ; i < arrlist.size() ; i++ )ret = svf.VrsOutn("ATTEND_REMARK" + (i + 1), g,  (String)arrlist.get(i) ); //行動の記録備考  NO002Modify
                //<--- NO001
            }
            rs.close();
        } catch( Exception ex ){
            log.debug( "printSvfDetail_1 error!", ex );
        }
    }


    /**
     *  SVF-FORM 印刷処理 明細
     *  行動の記録備考・特別活動の記録・出欠の記録備考
     */
    /* *** NO001
    void printSvfDetail_2( DB2UDB db2, Vrw32alp svf, Map paramap )
    {
        //ArrayList arrlist = null;
        int ret = 0;
        try {
            int p = 0;
            ( ( PreparedStatement )paramap.get("PS_REMARK_RECORD_SEC") ).setString( ++p, (String)paramap.get("YEAR")  );    //年度
            ( ( PreparedStatement )paramap.get("PS_REMARK_RECORD_SEC") ).setString( ++p, (String)paramap.get("SCHNO") );    //学籍番号
            rs = ( ( PreparedStatement )paramap.get("PS_REMARK_RECORD_SEC") ).executeQuery();

            int g = 0;
            while( rs.next() ){
                g = Integer.parseInt( rs.getString("ANNUAL") );

                arrlist = knjobj.retDividString( rs.getString("BEHAVEREC_REMARK"), 40, 2 );
                if( arrlist != null )for( int i = 0 ; i < arrlist.size() ; i++ )ret = svf.VrsOutn("ACT_REMARK"   + g ,  i + 1,  (String)arrlist.get(i) ); //行動の記録備考

                arrlist = knjobj.retDividString( rs.getString("SPECIALACTREMARK"), 44, 3 );
                if( arrlist != null )for( int i = 0 ; i < arrlist.size() ; i++ )ret = svf.VrsOutn("SPECIALACT"   + g ,  i + 1,  (String)arrlist.get(i) ); //特別活動の記録備考

                if( rs.getString("ATTENDREC_REMARK") != null )ret = svf.VrsOutn("ATTEND_REMARK1",  g,  rs.getString("ATTENDREC_REMARK") ); //出欠の記録備考
            }
            rs.close();
        } catch( Exception ex ){
            log.debug( "printSvfDetail_2 error!", ex );
        }
    }
    *** */


    /**
     *  SVF-FORM 印刷処理 明細
     *  行動の記録・特別活動の記録
     */
    void printSvfDetail_3( DB2UDB db2, Vrw32alp svf, Map paramap )
    {
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        try {
            int p = 0;
            ( ( PreparedStatement )paramap.get("PS_ACT_RECORD") ).setString( ++p, (String)paramap.get("YEAR")  );   //年度
            ( ( PreparedStatement )paramap.get("PS_ACT_RECORD") ).setString( ++p, (String)paramap.get("SCHNO") );   //学籍番号
            rs = ( ( PreparedStatement )paramap.get("PS_ACT_RECORD") ).executeQuery();

            int g = 0;
            while( rs.next() ){
                if( rs.getString("RECORD") != null  &&  rs.getString("RECORD").equals("1") ){
                    g = Integer.parseInt( rs.getString("ANNUAL") );
                    if( rs.getString("DIV").equals("1") )
                        ret = svf.VrsOutn("ACTION" + g,  Integer.parseInt(rs.getString("CODE")),  "○" ); //行動の記録
                    else
                        ret = svf.VrsOutn("SPECIALACT" + g,  Integer.parseInt(rs.getString("CODE")),  "○" ); //行動の記録
                }
            }
            rs.close();
        } catch( Exception ex ){
            log.debug( "printSvfDetail_3 error!", ex );
        }
    }


    /**
     *  SVF-FORM 印刷処理 明細
     *  出欠の記録
     */
    void printSvfDetail_4( DB2UDB db2, Vrw32alp svf, Map paramap )
    {
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        try {
            int p = 0;
            ( ( PreparedStatement )paramap.get("PS_ATTEND_RECORD") ).setString( ++p, (String)paramap.get("YEAR")  );    //年度
            ( ( PreparedStatement )paramap.get("PS_ATTEND_RECORD") ).setString( ++p, (String)paramap.get("SCHNO") );    //学籍番号
            rs = ( ( PreparedStatement )paramap.get("PS_ATTEND_RECORD") ).executeQuery();

            int g = 0;
            while( rs.next() ){
                g = Integer.parseInt( rs.getString("ANNUAL") );
                ret = svf.VrsOutn("LESSON",   g,  rs.getString("LESSON")           ); //授業日数
                ret = svf.VrsOutn("SUSPEND",  g,  rs.getString("SUSPEND_MOURNING") ); //出停・忌引
                ret = svf.VrsOutn("ABROAD",   g,  rs.getString("ABROAD")           ); //留学
                ret = svf.VrsOutn("PRESENT",  g,  rs.getString("REQUIREPRESENT")   ); //要出席
                ret = svf.VrsOutn("ATTEND",   g,  rs.getString("PRESENT")          ); //出席
                ret = svf.VrsOutn("ABSENCE",  g,  rs.getString("ABSENT")           ); //欠席
                //ret = svf.VrsOutn("LATE",     g,  rs.getString("LATE")             ); //遅刻
                //ret = svf.VrsOutn("LEAVE",    g,  rs.getString("EARLY")            ); //早退
            }
            rs.close();
        } catch( Exception ex ){
            log.debug( "printSvfDetail_4 error!", ex );
        }
    }


    /**
     *  PrepareStatementオブジェクト作成
     */
    public void prepareSqlState( DB2UDB db2, Map paramap, KNJDefineCode definecode )
    {
        try {
            //06/04/11 if( paramap.containsKey("PS_REMARK_RECORD") )return;

            //総合的な学習の時間の記録・総合所見
            if( ! paramap.containsKey("PS_REMARK_RECORD") )
                paramap.put("PS_REMARK_RECORD",  db2.prepareStatement( prestatRemarkRecord( paramap ) ) );
            //行動の記録備考・特別活動の記録・出欠の記録備考
            //NO001 if( ! paramap.containsKey("PS_REMARK_RECORD_SEC") )
            //NO001     paramap.put("PS_REMARK_RECORD_SEC",  db2.prepareStatement( prestatRemarkRecordSec( paramap ) ) );
            //行動の記録・特別活動の記録
            if( ! paramap.containsKey("PS_ACT_RECORD") )
                paramap.put("PS_ACT_RECORD",  db2.prepareStatement( prestatActRecord( paramap ) ) );
            //出欠の記録
            if( ! paramap.containsKey("PS_ATTEND_RECORD") )
                paramap.put("PS_ATTEND_RECORD",  db2.prepareStatement( prestatAttendRecord( paramap ) ) );
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
            if( ! paramap.containsKey("PS_REMARK_RECORD")     )( ( PreparedStatement )paramap.get("PS_REMARK_RECORD")     ).close();
            //NO001 if( ! paramap.containsKey("PS_REMARK_RECORD_SEC") )( ( PreparedStatement )paramap.get("PS_REMARK_RECORD_SEC") ).close();
            if( ! paramap.containsKey("PS_ACT_RECORD")        )( ( PreparedStatement )paramap.get("PS_ACT_RECORD")        ).close();
            if( ! paramap.containsKey("PS_ATTEND_RECORD")     )( ( PreparedStatement )paramap.get("PS_ATTEND_RECORD")     ).close();
        } catch( Exception ex ){
            log.debug( "closePrepareState error!", ex );
        }
    }


    /**
     *  priparedstatement作成  総合的な学習の時間の記録・総合所見
     */
    private String prestatRemarkRecord( Map paramap )
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("SELECT  ANNUAL, TOTALSTUDYACT, VIEWREMARK, TOTALSTUDYVAL,TOTALREMARK ");
            stb.append(       ",BEHAVEREC_REMARK, SPECIALACTREMARK, ATTENDREC_REMARK ");  //NO001
            stb.append("FROM    HTRAINREMARK_DAT T1 ");
            stb.append("WHERE   YEAR <= ? ");
            stb.append(    "AND SCHREGNO = ? ");
            //NO001 stb.append("ORDER BY ANNUAL ");
        } catch( Exception ex ){
            log.error("error! ",ex);
            log.debug("prestatRemarkRecord ps = " + stb.toString() );
        }
        return stb.toString();
    }


    /**
     *  priparedstatement作成  行動の記録備考・特別活動の記録備考・出欠の記録備考
     */
    /* *** NO001
    private String prestatRemarkRecordSec( Map paramap )
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("SELECT  ANNUAL, BEHAVEREC_REMARK, SPECIALACTREMARK, ATTENDREC_REMARK ");
            stb.append("FROM    HTRAINREMARK_DAT T1 ");
            stb.append("WHERE   YEAR <= ? ");
            stb.append(    "AND SCHREGNO = ? ");
            stb.append("ORDER BY ANNUAL ");
        } catch( Exception ex ){
            log.error("error! ",ex);
            log.debug("prestatRemarkRecordSec ps = " + stb.toString() );
        }
        return stb.toString();
    }
    *** */


    /**
     *  priparedstatement作成  行動の記録・特別活動の記録
     */
    private String prestatActRecord( Map paramap )
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("SELECT  DIV, CODE, ANNUAL, RECORD ");
            stb.append("FROM    BEHAVIOR_DAT T1 ");
            stb.append("WHERE   YEAR <= ? ");
            stb.append(    "AND SCHREGNO = ? ");
            //NO001 stb.append("ORDER BY DIV, CODE, ANNUAL ");
        } catch( Exception ex ){
            log.error("error! ",ex);
            log.debug("prestatActRecord ps = " + stb.toString() );
        }
        return stb.toString();
    }


    /**
     *  priparedstatement作成  出欠の記録
     *  SEM_OFFDAYS='1'の場合、休学日数は「授業日数」「要出席日数」「欠席日数」に含める。
     */
    private String prestatAttendRecord( Map paramap )
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("SELECT  ANNUAL, ");
            stb.append(        "VALUE(CLASSDAYS,0) AS CLASSDAYS, ");
            stb.append(        "CASE WHEN S1.SEM_OFFDAYS = '1' ");
            stb.append(             "THEN VALUE(CLASSDAYS,0) - VALUE(ABROAD,0) ");
            stb.append(             "ELSE VALUE(CLASSDAYS,0) - VALUE(OFFDAYS,0) - VALUE(ABROAD,0) ");
            stb.append(             "END AS LESSON, ");
            stb.append(        "VALUE(SUSPEND,0) + VALUE(MOURNING,0) AS SUSPEND_MOURNING, ");
            stb.append(        "VALUE(SUSPEND,0) AS SUSPEND, ");
            stb.append(        "VALUE(MOURNING,0) AS MOURNING, ");
            stb.append(        "VALUE(ABROAD,0) AS ABROAD, ");
            stb.append(        "CASE WHEN S1.SEM_OFFDAYS = '1' ");
            stb.append(             "THEN VALUE(REQUIREPRESENT,0) + VALUE(OFFDAYS,0) ");
            stb.append(             "ELSE VALUE(REQUIREPRESENT,0) ");
            stb.append(             "END AS REQUIREPRESENT, ");
            stb.append(        "VALUE(PRESENT,0) AS PRESENT, ");
            stb.append(        "CASE WHEN S1.SEM_OFFDAYS = '1' ");
            stb.append(             "THEN VALUE(SICK,0) + VALUE(ACCIDENTNOTICE,0) + VALUE(NOACCIDENTNOTICE,0) + VALUE(OFFDAYS,0) ");
            stb.append(             "ELSE VALUE(SICK,0) + VALUE(ACCIDENTNOTICE,0) + VALUE(NOACCIDENTNOTICE,0) ");
            stb.append(             "END AS ABSENT ");
            stb.append("FROM    SCHREG_ATTENDREC_DAT T1 ");
            stb.append(        "LEFT JOIN SCHOOL_MST S1 ON S1.YEAR = T1.YEAR ");
            stb.append("WHERE   T1.YEAR <= ? ");
            stb.append(    "AND SCHREGNO = ? ");
            //NO001 stb.append("ORDER BY ANNUAL ");
        } catch( Exception ex ){
            log.error("error! ",ex);
            log.debug("prestatAttendRecord ps = " + stb.toString() );
        }
        return stb.toString();
    }


}
