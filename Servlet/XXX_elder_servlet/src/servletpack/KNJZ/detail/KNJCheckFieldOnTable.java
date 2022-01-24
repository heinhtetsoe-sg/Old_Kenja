/**
 *	学校教育システム 賢者 テーブルのフィールド存在チェック
 *
 *      2006/02/21 yamashiro
 */

package servletpack.KNJZ.detail;

import java.sql.ResultSet;
import java.sql.SQLException;
import nao_package.db.DB2UDB;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class KNJCheckFieldOnTable {

    private static final Log log = LogFactory.getLog(KNJCheckFieldOnTable.class);


	public int checkFieldOnTable ( DB2UDB db2, String t, String f )
    {
        int ret = 0;
		ResultSet rs = null;
		try {
            String str = "SELECT NAME FROM SYSIBM.SYSCOLUMNS WHERE TBNAME = '" + t + "' AND NAME = '" + f + "'";
            db2.query( str );
			rs = db2.getResultSet();
            if( rs.next() )ret = 1;
            else           ret = 2;
		} catch( Exception ex ){
			log.error("checkFieldOnTable error!",ex);
        } finally {
			try {
                db2.commit();
				if( rs != null ) rs.close();
			} catch( SQLException ex ){
				log.error( "ResultSet-close error!", ex );
			}
		}
        return ret;
    }


}
