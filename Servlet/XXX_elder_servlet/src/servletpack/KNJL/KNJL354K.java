// kanji=漢字
/*
 * $Id: b8b789e9d5c288ba0fe608c0fb17fc3415904836 $
 *
 * 作成日: 2005/12/20 11:25:40 - JST
 * 作成者: yamashiro
 *
 * Copyright(C) 2009-2009 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJL;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJEditString;
import servletpack.KNJZ.detail.KNJServletpacksvfANDdb2;
import servletpack.KNJZ.detail.KNJ_EditDate;

/**
 *
 *
 *  学校教育システム 賢者 [入試管理] 出身学校別合格者一覧／指導要録受領一覧
 *
 *  2005/12/20 Build yamashiro
 *  2006/01/15 Modify yamashiro
 *    〇出身学校別合格者一覧 => 合格者はどの出身学校（外部の出身学校）からきたか知りたい帳票
 *        中学は、受験番号'3000番'台は除く   --NO001
 *        高校は、受験番号'5000番'台は除く   --NO001
 *    〇指導要録受領一覧 => 合格したら、出身学校から指導要録を提出してもらうので、受付窓口用の帳票
 *        高校は、受験番号'5000番'台は除く   --NO001
 *  2006/01/22 Modify yamashiro
 *    〇合格者ではなく入学者一覧とする --NO002
 *  2006/01/25 Modify m-yama
 *    〇入学者の条件を追加：入学者（PROCEDUREDIV = '2' && ENTDIV = '2'） --NO003
 *
 */
public class KNJL354K
{
    private static final Log log = LogFactory.getLog(KNJL354K.class);
    private KNJEditString knjeditstringobj = new KNJEditString();


    /**
     *  KNJD.classから最初に起動されるメソッド
     *
     */
    public void svf_out(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
    {
        Vrw32alp svf = new Vrw32alp();      //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        DB2UDB db2 = null;                  //Databaseクラスを継承したクラス
        Map paramap = new HashMap();        //HttpServletRequestからの引数
        boolean nonedata = false;           //該当データなしフラグ
        KNJServletpacksvfANDdb2 sd = new KNJServletpacksvfANDdb2();    //帳票におけるＳＶＦおよびＤＢ２の設定

    // ＤＢ接続
        db2 = sd.setDb( request );
        if( sd.openDb( db2 ) ){
            log.error("db open error");
            return;
        }

    // パラメータの取得
        getParam( db2, request, paramap );

    // print svf設定
        sd.setSvfInit( request, response, svf );

    // 印刷処理
        nonedata = printSvf( db2, svf, paramap );

    // 終了処理
        sd.closeSvf( svf, nonedata );
        sd.closeDb( db2 );
    }


    /** 
     *  get parameter doGet()パラメータ受け取り 
     *
     *  引数の説明
     *      "YEAR",       年度                 
     */
    private void getParam( DB2UDB db2, HttpServletRequest request, Map paramap )
    {
        try {
            paramap.put( "YEAR",       request.getParameter("YEAR")   );  //年度
            paramap.put( "PDIV",       request.getParameter("OUTPUT") );  //帳票種別
            paramap.put( "JHFLG",      request.getParameter("JHFLG")  );  //1:中学 2:高校
            paramap.put( "SPECIAL_REASON_DIV", request.getParameter("SPECIAL_REASON_DIV")); // 特別理由
        } catch( Exception ex ) {
            log.error("request.getParameter error!",ex);
        } finally {
log.debug("paramap="+paramap);
        }
    }


    public void setInfluenceName(DB2UDB db2, Vrw32alp svf, Map paramap) {
        PreparedStatement ps;
        ResultSet rs;
        try {
            ps = db2.prepareStatement("SELECT NAME2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMECD2 = '" + paramap.get("SPECIAL_REASON_DIV") + "' ");
            rs = ps.executeQuery();
            while (rs.next()) {
                String name2 = rs.getString("NAME2");
                svf.VrsOut("VIRUS", name2);
            }
            ps.close();
            rs.close();
        } catch (SQLException e) {
            log.error(e);
        } finally {
            db2.commit();
        }
    }

    /**
     *  印刷処理
     *
     */
    private boolean printSvf( DB2UDB db2, Vrw32alp svf, Map paramap )
    {
        boolean nonedata = false;                               //該当データなしフラグ
        PreparedStatement arrps[] = new PreparedStatement[3];
        List_Base listobj = null;

        try {
            if( ( (String)paramap.get("PDIV") ).equals("1") )
                listobj = new List_1();  //出身学校別合格者一覧印刷
            else
                listobj = new List_2();  //指導要録受領一覧印刷

            listobj.setSvfformFormat( svf );  //SVF-FORM設定
            listobj.setHeadItem( paramap );   //帳票見出し項目設定
            arrps[0] = db2.prepareStatement( listobj.prestatDetail( paramap ) );    //priparedstatement作成
            if( listobj.printsvfMain( db2, svf, paramap, arrps ) )nonedata = true;  //印刷処理

        } catch( Exception ex ){
            log.error("error! ",ex);
        } finally{
            try {
                for( int i = 0 ; i < arrps.length ; i++ )if( arrps[i] != null )arrps[i].close();
            } catch( SQLException ex ){
                log.error("error! ",ex);
            }
        }
        return nonedata;
    }



/**
 *  内部クラス  abstractクラス
 *
 */
abstract class List_Base
{
    /**
     *  帳票見出し項目設定
     */
    void setHeadItem( Map paramap )
    {
        String str = null;
        try {
            if( ! paramap.containsKey("NENDO") ){
                str = nao_package.KenjaProperties.gengou( Integer.parseInt( (String)paramap.get("YEAR") ) ) + "年度";
                paramap.put( "NENDO",  str );  //年度
            }
        } catch( Exception ex ){
            log.error("error! ",ex);
        }

        try {
            if( ! paramap.containsKey( "NOW_DATE" ) ){
                paramap.put( "NOW_DATE",  KNJ_EditDate.getNowDateWa( false ) );  //作成日
            }
        } catch( Exception ex ){
            log.error("error! ",ex);
        }
    }


    /**
     *  見出し等印刷  共通項目
     */
    void printsvfOutHead( DB2UDB db2, Vrw32alp svf, Map paramap )
    {

        try {
            svf.VrsOut( "NENDO",     (String)paramap.get("NENDO") );     //年度
            svf.VrsOut( "DATE",      (String)paramap.get("NOW_DATE") );  //作成日
            setInfluenceName(db2, svf, paramap);
        } catch( Exception ex ){
            log.error("error! ",ex);
        }
    }

    /**
     *  SVF-FORM設定
     */
    abstract void setSvfformFormat( Vrw32alp svf );


    /**
     *  priparedstatement作成
     */
    abstract String prestatDetail( Map paramap );


    /**
     *  印刷処理
     */
    abstract boolean printsvfMain( DB2UDB db2, Vrw32alp svf, Map paramap, PreparedStatement arrps[] );
}


/**
 *  内部クラス　出身学校別合格者一覧を印刷
 *
 */
class List_1 extends List_Base
{
    /**
     *  SVF-FORM設定  出身学校別合格者一覧
     */
    void setSvfformFormat( Vrw32alp svf )
    {
        try {
             svf.VrSetForm( "KNJL354_1.frm", 4 );
        } catch( Exception ex ){
            log.error("error! ",ex);
        }
    }


    /**
     *  priparedstatement作成  出身学校別合格者一覧
     */
    String prestatDetail( Map paramap )
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("SELECT  FS_CD, FINSCHOOL_NAME, T1.NAME ");
            stb.append("FROM    ENTEXAM_APPLICANTBASE_DAT T1  ");
            stb.append("INNER JOIN FINSCHOOL_MST T2 ON T2.FINSCHOOLCD = T1.FS_CD  ");  //NO001
            stb.append("WHERE   ENTEXAMYEAR = '" + (String)paramap.get("YEAR") + "'  ");
            if (!"9".equals(paramap.get("SPECIAL_REASON_DIV"))) {
                stb.append("    AND T1.SPECIAL_REASON_DIV = '" + paramap.get("SPECIAL_REASON_DIV") +"' ");
            }
            stb.append(    "AND VALUE(ENTDIV,'0') = '2'  ");   //NO002
            stb.append(    "AND VALUE(PROCEDUREDIV,'0') = '2'  ");   //NO003
            if( ( (String)paramap.get("JHFLG") ).equals("1") )                   //NO001によりIF〜を追加
                stb.append("AND NOT T1.EXAMNO BETWEEN '3000' AND '3999' ");
            else
                stb.append("AND NOT T1.EXAMNO BETWEEN '5000' AND '5999' ");
            stb.append("ORDER BY T1.NATPUBPRIDIV,T1.LOCATIONCD,T1.FS_CD  ");
        } catch( Exception ex ){
            log.error("error! ",ex);
            log.debug("prestatDetail_1 ps = " + stb.toString() );
        }
        return stb.toString();
    }


    /**
     *  印刷処理  出身学校別合格者一覧
     */
    boolean printsvfMain( DB2UDB db2, Vrw32alp svf, Map paramap, PreparedStatement arrps[] )
    {
        boolean nonedata = false;                               //該当データなしフラグ
        ResultSet rs = null;

        try {
            rs = arrps[0].executeQuery();
            String fscd = null;
            int cnt = 0;
            int num = 0;
            int i = 0;
            printsvfOutHead( db2, svf, paramap );

            while( rs.next() ){
                if( cnt == 5  ||  ! rs.getString("FS_CD").equals( fscd ) ){
                    if( fscd != null ){
                        svf.VrEndRecord();
                        nonedata = true;
                    }
                    if( ! rs.getString("FS_CD").equals( fscd ) ){
                        if( 31 > knjeditstringobj.retStringByteValue( rs.getString("FINSCHOOL_NAME"), 31 ) )i = 1;
                        else i = 2;
                        svf.VrsOut( "SCHOOLNAME" + i, rs.getString("FINSCHOOL_NAME") );  //学校名
                        fscd = rs.getString("FS_CD");  //出身学校コードの保管
                    }
                    cnt = 0;
                    svf.VrsOut( "SCHOOLCD", fscd );  //出身学校コード=>グループサプレスのため
                    svf.VrsOut( "NUMBER", String.valueOf( ++num ) );  //連番
                }
                svf.VrsOut( "NAME" + ( ++cnt ), rs.getString("NAME") );  //生徒名
            }
            if( fscd != null ){
                svf.VrEndRecord();
                nonedata = true;
            }
        } catch( Exception ex ){
            log.error("error! ",ex);
        } finally{
            try {
                if( rs != null )rs.close();
            } catch( SQLException ex ){
                log.error("error! ",ex);
            }
        }
        return nonedata;
    }
}

/**
 *  内部クラス　指導要録受領一覧を印刷
 *
 */
class List_2 extends List_Base
{
    /**
     *  SVF-FORM設定  指導要録受領一覧
     */
    void setSvfformFormat( Vrw32alp svf )
    {
        try {
             svf.VrSetForm( "KNJL354_2.frm", 4 );
        } catch( Exception ex ){
            log.error("error! ",ex);
        }
    }


    /**
     *  priparedstatement作成  指導要録受領一覧
     */
    String prestatDetail( Map paramap )
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("WITH SCHOOL_DATA AS(");
            stb.append(    "SELECT  T1.FS_CD,MIN(LOCATIONCD) AS LOCATIONCD,MIN(NATPUBPRIDIV) AS NATPUBPRIDIV ");
            stb.append(    "FROM    ENTEXAM_APPLICANTBASE_DAT T1 ");
            stb.append(    "WHERE   ENTEXAMYEAR = '" + (String)paramap.get("YEAR") + "'  ");
            if (!"9".equals(paramap.get("SPECIAL_REASON_DIV"))) {
                stb.append("    AND T1.SPECIAL_REASON_DIV = '" + paramap.get("SPECIAL_REASON_DIV") +"' ");
            }
            stb.append(        "AND VALUE(ENTDIV,'0') = '2'  ");   //NO002
            stb.append(        "AND VALUE(PROCEDUREDIV,'0') = '2'  ");   //NO003

            if( ( (String)paramap.get("JHFLG") ).equals("1") )                   //NO001によりIF〜を追加
                stb.append(    "AND NOT T1.EXAMNO BETWEEN '3000' AND '3999' ");
            else
                stb.append(    "AND NOT T1.EXAMNO BETWEEN '5000' AND '5999' ");
            stb.append(    "GROUP BY T1.FS_CD ");
            stb.append(    ") ");
            stb.append("SELECT  FINSCHOOL_NAME,T3.NAME1 AS NATPUBPRIDIV ,T4.NAME1 AS LOCATION ");
            stb.append("FROM    SCHOOL_DATA T1 ");
            stb.append("INNER JOIN FINSCHOOL_MST T2 ON T2.FINSCHOOLCD = T1.FS_CD ");  //NO001
            stb.append("LEFT JOIN NAME_MST T3 ON T3.NAMECD1 = 'L004' AND T3.NAMECD2 = T1.NATPUBPRIDIV AND T3.NAMECD2 = '1' ");
            stb.append("LEFT JOIN NAME_MST T4 ON T4.NAMECD1 = 'L007' AND T4.NAMECD2 = T1.LOCATIONCD ");
            stb.append("ORDER BY T1.NATPUBPRIDIV,T1.LOCATIONCD,T1.FS_CD");
        } catch( Exception ex ){
            log.error("error! ",ex);
            log.debug("prestatDetail_1 ps = " + stb.toString() );
        }
        return stb.toString();
    }


    /**
     *  印刷処理  指導要録受領一覧
     */
    boolean printsvfMain( DB2UDB db2, Vrw32alp svf, Map paramap, PreparedStatement arrps[] )
    {
        boolean nonedata = false;                               //該当データなしフラグ
        ResultSet rs = null;

        try {
            rs = arrps[0].executeQuery();
            int num = 0;
            int i = 0;
            printsvfOutHead( db2, svf, paramap );

            while( rs.next() ){
                if( 31 > knjeditstringobj.retStringByteValue( rs.getString("FINSCHOOL_NAME"), 31 ) )i = 1;
                else i = 2;
                svf.VrsOut( "SCHOOLNAME" + i, rs.getString("FINSCHOOL_NAME") );  //学校名
                svf.VrsOut( "NUMBER", String.valueOf( ++num ) );  //連番
                if( rs.getString("NATPUBPRIDIV") != null )svf.VrsOut( "LOCATION", rs.getString("NATPUBPRIDIV") );  //国公立および所在地
                else
                if( rs.getString("LOCATION") != null )svf.VrsOut( "LOCATION", rs.getString("LOCATION") );  //国公立および所在地
                svf.VrEndRecord();
                nonedata = true;
            }
        } catch( Exception ex ){
            log.error("error! ",ex);
        } finally{
            try {
                if( rs != null )rs.close();
            } catch( SQLException ex ){
                log.error("error! ",ex);
            }
        }
        return nonedata;
    }

}


    /**
     */
}
