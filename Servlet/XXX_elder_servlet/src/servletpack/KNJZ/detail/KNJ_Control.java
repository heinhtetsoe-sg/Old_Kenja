package servletpack.KNJZ.detail;

import nao_package.db.*;
import java.sql.ResultSet;

/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*
 *
 *         ＜ＫＮＪ＿Ｃｏｎｔｒｏｌ＞ コントロールマスタの値取得
 *
 *<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*/

public class KNJ_Control{


//	ＤＢより今年度、今学期、処理日付、写真データ格納フォルダのパス、写真データの拡張子を取得するメソッド
	public ReturnVal Control(DB2UDB db2){

		String year = new String();			//年度
		String semester = new String();		//学期
		String date = new String();			//処理日付
		String imagepath = new String();	//パス
		String extension = new String();	//拡張子

		try{
			String sql = new String();
			sql = "SELECT CTRL_YEAR,CTRL_SEMESTER,CTRL_DATE,IMAGEPATH,EXTENSION FROM CONTROL_MST WHERE CTRL_NO = '01' ";
			//System.out.println("[KNJ_Control]set_sql sql=" + sql);
			db2.query(sql);
			ResultSet rs = db2.getResultSet();

			if( rs.next() ){
				year = rs.getString("CTRL_YEAR");
				semester = rs.getString("CTRL_SEMESTER");
				date = rs.getString("CTRL_DATE");
				imagepath = rs.getString("IMAGEPATH");
				extension = rs.getString("EXTENSION");
			}

			rs.close();
			System.out.println("[KNJ_Control]set_sql sql ok!");
		} catch( Exception ex ){
			System.out.println("[KNJ_Control]set_sql read error!");
			System.out.println( ex );
		}

		return (new ReturnVal(year,semester,date,imagepath,extension));
	}


//	return値を返す内部クラス
	public static class ReturnVal{

		public final String val1,val2,val3,val4,val5;

		public ReturnVal(String val1,String val2,String val3,String val4,String val5){
			this.val1 = val1;
			this.val2 = val2;
			this.val3 = val3;
			this.val4 = val4;
			this.val5 = val5;
		}
	}



}//クラスの括り
