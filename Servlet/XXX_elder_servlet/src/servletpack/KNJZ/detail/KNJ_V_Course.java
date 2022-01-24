package servletpack.KNJZ.detail;

import nao_package.db.*;
import java.sql.ResultSet;

/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*
 *
 *         ＜ＫＮＪ＿Ｖ＿Ｃｏｕｒｓｅ＞ 課程学科・コース名の取得
 *
 *<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*/

public class KNJ_V_Course{


//	ＤＢより該当課程学科情報を取得するメソッド
	public ReturnVal V_CourseMajor(DB2UDB db2,String year,String coursemajorcd){

		String name = new String();		//課程学科名称

		try{
			String sql = new String();
			sql = "SELECT "
					+ "VALUE(COURSENAME,'') || VALUE(MAJORNAME,'') AS NAME "
				+ "FROM "
					+ "V_COURSE_MAJOR_MST "
				+ "WHERE "
						+ "YEAR 				= '" + year + "' "
					+ "AND COURSECD || MAJORCD 	= '" + coursemajorcd + "' ";
			System.out.println("[KNJ_V_Course]set_sql sql=" + sql);
			db2.query(sql);
			ResultSet rs = db2.getResultSet();

			if( rs.next() ){
				name = rs.getString("NAME");
			}

			rs.close();
			System.out.println("[KNJ_V_Course]set_sql sql ok!");
		} catch( Exception ex ){
			System.out.println("[KNJ_V_Course]set_sql read error!");
			System.out.println( ex );
		}

		return (new ReturnVal(name));
	}



//	ＤＢより該当コース情報を取得するメソッド
	public ReturnVal V_CourseCode(DB2UDB db2,String year,String coursecode){

		String name = new String();		//コース名称

		try{
			String sql = new String();
			sql = "SELECT "
					+ "VALUE(COURSECODENAME,'') AS NAME "
				+ "FROM "
					+ "V_COURSECODE_MST "
				+ "WHERE "
						+ "YEAR 		= '" + year + "' "
					+ "AND COURSECODE 	= '" + coursecode + "' ";
			System.out.println("[KNJ_V_Course]set_sql sql=" + sql);
			db2.query(sql);
			ResultSet rs = db2.getResultSet();

			if( rs.next() ){
				name = rs.getString("NAME");
			}

			rs.close();
			System.out.println("[KNJ_V_Course]set_sql sql ok!");
		} catch( Exception ex ){
			System.out.println("[KNJ_V_Course]set_sql read error!");
			System.out.println( ex );
		}

		return (new ReturnVal(name));
	}



//	return値を返す内部クラス
	public static class ReturnVal{

		public final String val1;

		public ReturnVal(String val1){
			this.val1 = val1;
		}
	}



}//クラスの括り
