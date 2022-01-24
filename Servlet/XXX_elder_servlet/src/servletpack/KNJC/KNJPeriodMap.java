// kanji=漢字
/*
 * $Id: e8c4e997bae3a893c79c2ff43a682b361bd78b8a $
 *
 * 作成日: 2005/04/19
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJC;

import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.util.Map;
import java.util.HashMap;
import nao_package.db.DB2UDB;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/*
 *
 *  学校教育システム 賢者 [出欠管理]  校時マップ
 *
 *  2005/04/19 yamashiro
 *
 */

public class KNJPeriodMap
{

    private static final Log log = LogFactory.getLog(KNJPeriodMap.class);

    public Map periodmeishomap;
    public Map periodnumbermap;


    /** 
     *  対象校時および名称取得
     */
    public void PeriodMap( DB2UDB db2 )
    {
    //  校時名称
        StringBuffer stb = null;
        periodmeishomap = new HashMap(16);       //対象校時
        periodnumbermap = new HashMap(16);       //対象校時
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            stb = new StringBuffer();
            stb.append("SELECT  NAMECD2, ");
            stb.append(        "CASE WHEN NAME2 IS NULL THEN NAME1 ELSE NAME2 END AS NAME ");
            stb.append("FROM    NAME_MST W1 ");
            stb.append("WHERE   NAMECD1 = 'B001' ");
            stb.append("ORDER BY NAMECD2");
            ps = db2.prepareStatement( stb.toString() );
            rs = ps.executeQuery();

            for( int i = 0 ; rs.next() ; i++ ){
                periodmeishomap.put( new Integer( i + 1 ), rs.getString("NAME") );     //１６校時SEQ.NUMBERをキーにした校時名称のマップ
                //periodmeishomap.put( rs.getString("NAMECD2"), rs.getString("NAME") );//１６校時CODEをキーにした校時名称のマップ
                periodnumbermap.put( rs.getString("NAMECD2"), new Integer( i + 1 ) );  //１６校時CODEをキーにしたSEQ.NUMBERのマップ
            }
        } catch( Exception ex ){
            log.warn("periodname-get error!",ex);
        } finally{
            db2.commit();
            if( rs != null )try {rs.close();} catch( Exception ex ) {log.warn("periodname get-ResultSet error!",ex);}
            if( ps != null )try {ps.close();} catch( Exception ex ) {log.warn("ResultSet error!",ex);}
        }
    }

}
