// kanji=漢字
/*
 * $Id: 7a1e1c3c485341a5b2c282366a5db6a1558a95cc $
 *
 * 作成日: 
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJA;

import java.util.ArrayList;
import java.util.Map;

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
 *  学校教育システム 賢者 [学籍管理]  生徒指導要録  中学校用（千代田区立九段中等教育学校版を基に作成）
 *                                                    総合的な学習の時間の記録
 *                                                    行動の記録
 *                                                    特別活動の記録
 *                                                    総合所見及び指導上参考となる事項
 *                                                    出欠の記録
 *
 *  2006/03/18 Build yamashiro
 *  2006/04/10 yamashiro テーブルHTRAINREMARK_DATのフィールド長変更による不具合対応 --NO001
 */

public class KNJA131JFORM5 extends KNJA131BASE
{
    private static final Log log = LogFactory.getLog(KNJA131JFORM5.class);
    private StringBuffer stb;
    private ResultSet rs;
    private KNJObjectAbs knjobj;            //編集用クラス
    private ArrayList arrlist;
    private KNJA131FORM5 knja131obj = new KNJA131FORM5();  //中等教育学校用オブジェクト

    /**
     *  SVF-FORM 印刷処理
     */
    boolean printSvf( DB2UDB db2, Vrw32alp svf, Map paramap )
    {
        boolean nonedata = false;
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        try {
            knja131obj.knjobj = new KNJEditString();
            knja131obj.printSvfDetail_1( db2, svf, paramap );  //総合的な学習の時間の記録・総合所見
            //NO001 printSvfDetail_2( db2, svf, paramap );             //行動の記録備考・特別活動の記録・出欠の記録備考
            knja131obj.printSvfDetail_3( db2, svf, paramap );  //行動の記録・特別活動の記録
            knja131obj.printSvfDetail_4( db2, svf, paramap );  //出欠の記録
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
            ret = svf.VrSetForm("KNJA131_8.frm", 1);
        } catch( Exception ex ){
            log.debug( "svf.VrSetForm error!", ex );
        }
    }


    /**
     *  SVF-FORM 印刷処理 明細
     *  行動の記録備考・特別活動の記録・出欠の記録備考
     */
    /* *** NO001
    void printSvfDetail_2( DB2UDB db2, Vrw32alp svf, Map paramap )
    {
        int ret = 0;
        try {
            if( knjobj == null )knjobj = new KNJEditString();
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
     *  PrepareStatementオブジェクト作成
     */
    void prepareSqlState( DB2UDB db2, Map paramap, KNJDefineCode definecode )
    {
        try {
            knja131obj.prepareSqlState( db2, paramap, definecode );
        } catch( Exception ex ){
            log.debug( "prepareSqlState error!", ex );
        }
    }


    /**
     *  SQL オブジェクトをクローズ
     */
    void closePrepareState( DB2UDB db2, Map paramap )
    {
        try {
            knja131obj.closePrepareState( db2, paramap );
        } catch( Exception ex ){
            log.debug( "closePrepareState error!", ex );
        }
    }


}
