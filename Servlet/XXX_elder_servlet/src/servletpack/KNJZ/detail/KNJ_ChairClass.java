/**
 *
 *  講座クラスの取得
 *  2004/11/17 yamashiro Modify
 *  2005/06/22 yamashiro Modify
 */

package servletpack.KNJZ.detail;

import nao_package.db.*;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.util.StringTokenizer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class KNJ_ChairClass{

    private static final Log log = LogFactory.getLog(KNJ_ChairClass.class);


	/**	
      *  対象クラス別名、統計対象クラス別名の取得 
      *     2004/11/17 Modify 講座クラスデータにおける講座コード０に対応
      **/
	public ReturnVal ChairClass(DB2UDB db2,String year,String semester,String chaircd1,String chaircd2){

		PreparedStatement ps;
		String sql = null;
		String trgt_name = null;	//対象クラス別名
		String stat_name = null;	//統計対象クラス別名

		try{
			sql = "SELECT "
					+ "VALUE(W1.GROUPCD,'0') AS GROUPCD,"
					+ "W3.HR_NAMEABBV "
				+ "FROM "
					+ "CHAIR_DAT W1,"
					+ "CHAIR_CLS_DAT W2,"
					+ "SCHREG_REGD_HDAT W3 "
				+ "WHERE "
						+ "W1.YEAR ='" + year + "' "
					+ "AND W1.SEMESTER = '" + semester + "' "
					+ "AND W1.CHAIRCD =? "
					+ "AND W1.YEAR = W2.YEAR "
					+ "AND W1.SEMESTER = W2.SEMESTER "
					+ "AND (W1.CHAIRCD = W2.CHAIRCD OR W1.GROUPCD = W2.GROUPCD) "     // 04/11/17Modify
					+ "AND W1.YEAR = W3.YEAR "
					+ "AND W1.SEMESTER = W3.SEMESTER "
					+ "AND W2.TRGTGRADE = W3.GRADE "
					+ "AND W2.TRGTCLASS = W3.HR_CLASS "
				+ "ORDER BY "
					+ "TRGTGRADE,"
					+ "TRGTCLASS";

			ps = db2.prepareStatement(sql);
//log.debug("ps="+ps.toString());
		//	対象クラス別名の編集
			ResultSet rs = null;
			StringTokenizer stz = new StringTokenizer(chaircd1,",",false);
			String cname = null;
			int count = 0;
			int ia=0;
			while (stz.hasMoreTokens()){
				ps.setString(1,stz.nextToken());
				rs = ps.executeQuery();
				cname = null;
				count = 0;
				while( rs.next() ){
					if( count==0 ){
						if( Integer.parseInt(rs.getString("GROUPCD"))>0 ){
									cname = rs.getString("HR_NAMEABBV") + "*(" + rs.getString("HR_NAMEABBV");
									count++;
						}else		cname = rs.getString("HR_NAMEABBV");
					} else			cname = cname + " " + rs.getString("HR_NAMEABBV");
				}
				rs.close();
				if( count>0 )	cname = cname + ")";
				if( ia==0 )		trgt_name = cname;								//対象クラス別名
				else			trgt_name = trgt_name + "," + cname;			//対象クラス別名
				ia++;
			}
			stat_name = trgt_name;									//統計対象クラス別名
		//	統計対象クラス別名の編集
			if( chaircd2!=null ){
				stz = new StringTokenizer(chaircd2,",",false);
				while (stz.hasMoreTokens()){
					ps.setString(1,stz.nextToken());
					rs = ps.executeQuery();
					cname = null;
					count = 0;
					while( rs.next() ){
						if( count==0 ){
							if( Integer.parseInt(rs.getString("GROUPCD"))>0 ){
										cname = rs.getString("HR_NAMEABBV") + "*(" + rs.getString("HR_NAMEABBV");
										count++;
							}else		cname = rs.getString("HR_NAMEABBV");
						} else			cname = cname + " " + rs.getString("HR_NAMEABBV");
					}
					rs.close();
					if( count>0 )	cname = cname + ")";
					stat_name = stat_name + "," + cname;			//統計対象クラス別名
				}
			}
			ps.close();
			System.out.println("[KNJ_ChairClass]ChairClassSql ok!");
		} catch( Exception ex ){
			System.out.println("[KNJ_ChairClass]ChairClassSql error!");
			System.out.println( ex );
		}

		return (new ReturnVal(trgt_name,stat_name));

	}//ChairClassSqlの括り



	/** <<< return値を返す内部クラス >>> **/
	public static class ReturnVal{

		public final String val1,val2;

		public ReturnVal(String val1,String val2){
			this.val1 = val1;
			this.val2 = val2;
		}
	}//class ReturnValの括り



}//クラスの括り
