package servletpack.KNJD.detail;

import nao_package.db.*;
import java.sql.ResultSet;

/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*
 *
 *         ＜ＫＮＪ＿Ａｓｓｅｓｓ＞ 評定基準等の取得
 *
 *<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*/

public class KNJ_Assess{


	/** 学期、学年の評定単位保留値を取得 */
	public ReturnVal FearvalInfo(DB2UDB db2,String year){

		String semes_fearval = new String();			//学期保留値
		String grade_fearval = new String();			//学年保留値

		try {
			String sql = new String();
			sql = "SELECT "
					+ "VALUE(SEMES_FEARVAL,0) AS SEMES_FEARVAL,"
					+ "VALUE(GRADE_FEARVAL,0) AS GRADE_FEARVAL "
				+ "FROM "
					+ "SCHOOL_MST W1 "
				+ "WHERE "
				 	+ "W1.YEAR = '" + year + "'";
            db2.query(sql);
			ResultSet rs = db2.getResultSet();
			if( rs.next() ){
				semes_fearval = rs.getString("SEMES_FEARVAL");
				grade_fearval = rs.getString("GRADE_FEARVAL");
			}
			rs.close();
		} catch( Exception e ){
			System.out.println("[KNJ_Assess]FearvalInfo error!");
			System.out.println( e );
		}

		return (new ReturnVal(semes_fearval,grade_fearval));
	}


	/** 学期の評定段階を取得 */
	public static int SemesAssesslevel(DB2UDB db2,String year){

		try {
			String sql = new String();
			sql = "SELECT "
					+ "CASE SEMES_ASSESSCD WHEN '0' THEN 100 ELSE ASSESSLEVELCNT END AS ASSESSLEVELCNT "
				+ "FROM "
					+ "SCHOOL_MST W1 "
					+ "LEFT JOIN ASSESS_HDAT W2 ON W2.ASSESSCD = W1.SEMES_ASSESSCD "
				+ "WHERE "
				 	+ "YEAR = '" + year + "'";
            db2.query(sql);
			ResultSet rs = db2.getResultSet();
			if( rs.next() ){
				return rs.getInt("ASSESSLEVELCNT");
			}
			rs.close();
		} catch( Exception e ){
			System.out.println("[KNJ_Assess]FearvalInfo error!");
			System.out.println( e );
		}
		return 0;
	}


	/** 学年末の評定段階を取得 */
	public static int GradeAssesslevel(DB2UDB db2,String year){

		try {
			String sql = new String();
			sql = "SELECT "
					+ "ASSESSLEVELCNT "
				+ "FROM "
					+ "ASSESS_HDAT "
				+ "WHERE "
				 	+ "ASSESSCD = '3'";
            db2.query(sql);
			ResultSet rs = db2.getResultSet();
			if( rs.next() ){
				return rs.getInt("ASSESSLEVELCNT");
			}
			rs.close();
		} catch( Exception e ){
			System.out.println("[KNJ_Assess]FearvalInfo error!");
			System.out.println( e );
		}
		return 0;
	}



	/** return値を返す内部クラス */
	public static class ReturnVal{

		public final String val1,val2;

		public ReturnVal(String val1,String val2){
			this.val1 = val1;
			this.val2 = val2;
		}
	}



}//クラスの括り
