package servletpack.KNJZ.detail;

import nao_package.db.*;
import java.sql.ResultSet;

/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*
 *
 *         ＜ＫＮＪ＿Ｓｔａｆｆ＞ 職員名の取得
 *
 *<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*/

public class KNJ_Staff{


//	担任名を取得するメソッド
	public ReturnVal Staff_name(DB2UDB db2,String year,String semester,String grade,String hrclass){

		String name = new String();			//氏名
		String name_show = new String();	//表示氏名
		String name_kana = new String();	//カナ氏名
		String name_eng = new String();		//英語氏名

		try{
			String sql = new String();
			sql = "SELECT "
					+ "STAFFNAME,"
					+ "STAFFNAME_SHOW,"
					+ "STAFFNAME_KANA,"
					+ "STAFFNAME_ENG "
				+ "FROM "
					+ "STAFF_MST W1,"
					+ "SCHREG_REGD_HDAT W2 "
				+ "WHERE "
						+ "W2.YEAR='" + year + "' "
					+ "AND W2.SEMESTER='" + semester + "' "
					+ "AND GRADE='" + grade + "' "
					+ "AND HR_CLASS='" + hrclass + "' "
					+ "AND W1.STAFFCD=W2.TR_CD1";

			//System.out.println("[KNJ_Staff]set_sql sql=" + sql);
			db2.query(sql);
			ResultSet rs = db2.getResultSet();

			if( rs.next() ){
				name = rs.getString("STAFFNAME");
				name_show = rs.getString("STAFFNAME_SHOW");
				name_kana = rs.getString("STAFFNAME_KANA");
				name_eng = rs.getString("STAFFNAME_ENG");
			}
			rs.close();
			System.out.println("[KNJ_Staff]set_sql sql ok!");
		} catch( Exception ex ){
			System.out.println("[KNJ_Staff]set_sql read error!");
			System.out.println( ex );
		}

		return (new ReturnVal(name,name_show,name_kana,name_eng));
	}



//	講座担任名を取得するメソッド
	public ReturnVal ChairStaff(DB2UDB db2,String year,String semester,String chaircd){

		String name = new String();			//氏名
		String name_show = new String();	//表示氏名
		String name_kana = new String();	//カナ氏名
		String name_eng = new String();		//英語氏名

		try{
			String sql = new String();
			sql = "SELECT "
					+ "STAFFNAME,"
					+ "STAFFNAME_SHOW,"
					+ "STAFFNAME_KANA,"
					+ "STAFFNAME_ENG "
				+ "FROM "
					+ "CHAIR_STF_DAT W1,"
					+ "STAFF_MST W2 "
				+ "WHERE "
						+ "W1.YEAR = '" + year + "' "
					+ "AND W1.SEMESTER = '" + semester + "' "
					+ "AND W1.CHAIRCD = '" + chaircd + "' "
					+ "AND W1.STAFFCD = W2.STAFFCD";

			//System.out.println("[KNJ_Staff]set_sql sql=" + sql);
			db2.query(sql);
			ResultSet rs = db2.getResultSet();

			if( rs.next() ){
				name = rs.getString("STAFFNAME");
				name_show = rs.getString("STAFFNAME_SHOW");
				name_kana = rs.getString("STAFFNAME_KANA");
				name_eng = rs.getString("STAFFNAME_ENG");
			}
			rs.close();
			System.out.println("[KNJ_Staff]set_sql sql ok!");
		} catch( Exception ex ){
			System.out.println("[KNJ_Staff]set_sql read error!");
			System.out.println( ex );
		}

		return (new ReturnVal(name,name_show,name_kana,name_eng));
	}



//	担任名を取得するメソッド（職員マスタのみ）
	public ReturnVal Staff_name_show(DB2UDB db2,String staffcd){

		String name = new String();			//氏名
		String name_show = new String();	//表示氏名
		String name_kana = new String();	//カナ氏名
		String name_eng = new String();		//英語氏名

		try{
			String sql = new String();
			sql = "SELECT "
					+ "STAFFNAME,"
					+ "STAFFNAME_SHOW,"
					+ "STAFFNAME_KANA,"
					+ "STAFFNAME_ENG "
				+ "FROM "
					+ "STAFF_MST "
				+ "WHERE "
					+ "STAFFCD='" + staffcd + "' ";

			//System.out.println("[KNJ_Staff]set_sql sql=" + sql);
			db2.query(sql);
			ResultSet rs = db2.getResultSet();

			if( rs.next() ){
				name = rs.getString("STAFFNAME");
				name_show = rs.getString("STAFFNAME_SHOW");
				name_kana = rs.getString("STAFFNAME_KANA");
				name_eng = rs.getString("STAFFNAME_ENG");
			}
			rs.close();
			System.out.println("[KNJ_Staff]set_sql sql ok!");
		} catch( Exception ex ){
			System.out.println("[KNJ_Staff]set_sql read error!");
			System.out.println( ex );
		}

		return (new ReturnVal(name,name_show,name_kana,name_eng));
	}



//	return値を返す内部クラス
	public static class ReturnVal{

		public final String val1,val2,val3,val4;

		public ReturnVal(String val1,String val2,String val3,String val4){
			this.val1 = val1;
			this.val2 = val2;
			this.val3 = val3;
			this.val4 = val4;
		}
	}



}//クラスの括り
