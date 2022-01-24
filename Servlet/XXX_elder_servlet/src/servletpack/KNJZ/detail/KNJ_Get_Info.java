// kanji=漢字
/*
 * $Id: 34a631c4ced707a271dd25312c17580077fb58ea $
 *
 * 作成日: 2005/08/09
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJZ.detail;

import java.util.*;
import java.sql.ResultSet;
import java.sql.SQLException;

import nao_package.db.DB2UDB;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *  値取得
 */
/*  public ReturnVal Chair_Info()       講座名を取得するメソッド
 *                                      2005/05/19 yamashiro Modify
 *  public ReturnVal ChairStaff()       講座担任名を取得するメソッド
 *                                      2005/10/22 yamashiro Modify
 *  public ReturnVal class_name()       教科名を取得するメソッド
 *  public ReturnVal Control()          
 *  public ReturnVal Grade_Hrclass()    学年・組に分解するメソッド
 *  public ReturnVal hrclass_name()     組名称を取得するメソッド
 *  public ReturnVal Hrclass_Staff()    組名称及び担任名を取得するメソッド
 *  public ReturnVal School_Info()      学期制、学校区分を取得するメソッド
 *  public ReturnVal Semester()         任意の学期名称等を取得するメソッド
 *  public ReturnVal Semester_T()       全学期の名称等を取得するメソッド
 *  public ReturnVal Staff_name()       担任名を取得するメソッド
 *  public ReturnVal Staff_name_show()  職員名を取得するメソッド
 *  public ReturnVal Subclass_name()    科目名を取得するメソッド
 *  public String GradeRecsql()         年次別クラス、担任名、校長名を取得するためのStatementを作成するメソッド
 *  public ReturnVal Testkind_name()    テスト種別名を取得するメソッド 2004/07/22 add by nakamoto
 *  public ReturnVal Testitem_name()    テスト項目名を取得するメソッド 2004/08/03 add by nakamoto
 *  public Map Map_Subclass()           固定科目を取得するメソッド 2004/08/25 add by nakamoto
 *  public ReturnVal getSchoolName()    学校名、校長名を取得するメソッド 2005/08/16 build yamashiro
 *  public ReturnVal getTargetPeriod()  １日出欠集計対象校時を取得するメソッド 2005/08/17 build yamashiro
 *  
 *  2005/12/16 yamashiro メソッドStaff_name() オーバーロードを追加
 */

public class KNJ_Get_Info
{
    private static final Log log = LogFactory.getLog(KNJ_Get_Info.class);


	/** 
     *  学校名、校長名を取得するメソッド 
     *  2005/08/16 Build 
     */
	public ReturnVal getSchoolName( DB2UDB db2, String year )
	{
		KNJ_Get_Info.getSchoolNameClass obj = null;
		try{
			obj = new KNJ_Get_Info.getSchoolNameClass();
			obj.getSchoolName( db2, year );
		} catch( Exception ex ){
			log.error("error! " + ex );
		}
		return ( new ReturnVal( obj.schoolname, obj.staffname, obj.jobname, null, null ) );
	}


	/** 
     *  １日出欠集計対象校時を取得するメソッド 
     *  2005/08/17 Build 
     */
	public ReturnVal getTargetPeriod( DB2UDB db2, 
                                      String year, 
                                      String semes, 
                                      String gradehrclass, 
                                      boolean usefromtoperiod )
	{
		KNJ_Get_Info.getTargetPeriodClass obj = null;
		try{
			obj = new KNJ_Get_Info.getTargetPeriodClass();
			obj.getEditPeriod( db2, year, semes, gradehrclass, usefromtoperiod );
		} catch( Exception ex ){
			log.error("error! " + ex );
		}
		return ( new ReturnVal( obj.editperiod, null, null, null, null ) );
	}



	/** 学期制、学校区分を取得するメソッド **/
	public ReturnVal School_Info(DB2UDB db2,String year){

		String semesdiv = null;			//年度
		String schooldiv = null;		//学校区分
		try{
			String sql = "SELECT SEMESTERDIV,SCHOOLDIV FROM SCHOOL_MST WHERE YEAR='"+year+"'";
			db2.query(sql);
			ResultSet rs = db2.getResultSet();
			if( rs.next() ){
				semesdiv = rs.getString("SEMESTERDIV");
				schooldiv = rs.getString("SCHOOLDIV");
			}
			db2.commit();
			rs.close();
		} catch( Exception ex ){
			System.out.println("[KNJ_Get_Info]ReturnVal School_Info set_sql read error!");
			System.out.println( ex );
		}
		if( semesdiv==null )semesdiv = "3";
		if( schooldiv==null )schooldiv = "1";
		return (new ReturnVal(semesdiv,schooldiv,null,null,null));
	}


	/** ＤＢより今年度、今学期、処理日付、写真データ格納フォルダのパス、写真データの拡張子を取得するメソッド **/
	public ReturnVal Control(DB2UDB db2){

		String year = new String();			//年度
		String semester = new String();		//学期
		String date = new String();			//処理日付
		String imagepath = new String();	//パス
		String extension = new String();	//拡張子
		try{
			String sql = new String();
			sql = "SELECT CTRL_YEAR,CTRL_SEMESTER,CTRL_DATE,IMAGEPATH,EXTENSION FROM CONTROL_MST WHERE CTRL_NO = '01' ";
			//System.out.println("[KNJ_Get_Info]ReturnVal Control() set_sql sql=" + sql);
			db2.query(sql);
			ResultSet rs = db2.getResultSet();
			if( rs.next() ){
				year = rs.getString("CTRL_YEAR");
				semester = rs.getString("CTRL_SEMESTER");
				date = rs.getString("CTRL_DATE");
				imagepath = rs.getString("IMAGEPATH");
				extension = rs.getString("EXTENSION");
			}
			db2.commit();
			rs.close();
			//System.out.println("[KNJ_Get_Info]ReturnVal Control() set_sql sql ok!");
		} catch( Exception ex ){
			System.out.println("[KNJ_Get_Info]ReturnVal Control() set_sql read error!");
			System.out.println( ex );
		}
		return (new ReturnVal(year,semester,date,imagepath,extension));
	}



	/** ＤＢより該当学期情報を取得するメソッド **/
	public ReturnVal Semester(DB2UDB db2,String year,String semester){

		String name = new String();		//学期名称
		String sdate = new String();	//学期開始日
		String edate = new String();	//学期終了日
		try{
			String sql = new String();
			sql = "SELECT * FROM SEMESTER_MST WHERE YEAR = '" + year + "' AND SEMESTER = '" + semester + "' ";
			//System.out.println("[KNJ_Semester]ReturnVal Semester() set_sql sql=" + sql);
			db2.query(sql);
			ResultSet rs = db2.getResultSet();
			if( rs.next() ){
				name = rs.getString("SEMESTERNAME");
				sdate = rs.getString("SDATE");
				edate = rs.getString("EDATE");
			}
			db2.commit();
			rs.close();
			//System.out.println("[KNJ_Get_Info]ReturnVal Semester() set_sql sql ok!");
		} catch( Exception ex ){
			System.out.println("[KNJ_Get_Info]ReturnVal Semester() set_sql read error!");
			System.out.println( ex );
		}
		return (new ReturnVal(name,sdate,edate,null,null));
	}



	/** ＤＢより該当年度全学期情報を取得するメソッド **/
	//	全学期を','を区切り文字とした文字列として編集するので、使用する際はStringtoknizerで個々の値を取り出す！
	public ReturnVal Semester_T(DB2UDB db2,String year){

		StringBuffer code = new StringBuffer(8);		//学期コード
		StringBuffer name = new StringBuffer(20);		//学期名称
		StringBuffer sdate = new StringBuffer(44);		//学期開始日
		StringBuffer edate = new StringBuffer(44);		//学期終了日
		try{
			StringBuffer sql = new StringBuffer();
			sql.append("SELECT * FROM SEMESTER_MST WHERE YEAR = '");
			sql.append(year);
			sql.append("' ORDER BY SEMESTER");
			//System.out.println("[KNJ_Get_Info]ReturnVal Semester_T() set_sql sql=" + sql);
			db2.query(sql.toString());
			ResultSet rs = db2.getResultSet();
			boolean first = true;
			while( rs.next() ){
				if( !first ){
					code.append(",");
					name.append(",");
					sdate.append(",");
					edate.append(",");
				}
				if( rs.getString("SEMESTER")!=null ) code.append(rs.getString("SEMESTER"));
				if( rs.getString("SEMESTERNAME")!=null ) name.append(rs.getString("SEMESTERNAME"));
				if( rs.getString("SDATE")!=null ) sdate.append(rs.getString("SDATE"));
				if( rs.getString("EDATE")!=null ) edate.append(rs.getString("EDATE"));
				first=false;
			}
			db2.commit();
			rs.close();
			//System.out.println("[KNJ_Get_Info]ReturnVal Semester_T() set_sql sql ok!");
		} catch( Exception ex ){
			System.out.println("[KNJ_Get_Info]ReturnVal Semester_T() set_sql read error!");
			System.out.println( ex );
		}
		return (new ReturnVal(code.toString(),name.toString(),sdate.toString(),edate.toString(),null));
	}

	/**	ＤＢより担任名を取得するメソッド **/
	public ReturnVal Staff_name(DB2UDB db2,String year,String semester,String grade,String hr_class){

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
					+ "AND W1.STAFFCD=W2.TR_CD1 "
					+ "AND GRADE || HR_CLASS = '" + grade + hr_class + "' ";
			if( !semester.equals("9") )	sql = sql				//学期指定の場合
					+ "AND SEMESTER = '" + semester + "'";
			else						sql = sql				//学年指定の場合
					+ "AND SEMESTER = (SELECT "
										+ "MAX(SEMESTER) "
									+ "FROM "
										+ "SCHREG_REGD_HDAT W3 "
									+ "WHERE "
											+ "W2.YEAR = W3.YEAR "
										+ "AND W2.GRADE || W2.HR_CLASS = W3.GRADE || W3.HR_CLASS)";

			//System.out.println("[KNJ_Get_Info]ReturnVal Staff_name() set_sql sql=" + sql);
			db2.query(sql);
			ResultSet rs = db2.getResultSet();
			if( rs.next() ){
				name = rs.getString("STAFFNAME");
				name_show = rs.getString("STAFFNAME_SHOW");
				name_kana = rs.getString("STAFFNAME_KANA");
				name_eng = rs.getString("STAFFNAME_ENG");
			}
			db2.commit();
			rs.close();
			//System.out.println("[KNJ_Get_Info]ReturnVal Staff_name() Staff_name ok!");
		} catch( Exception ex ){
			System.out.println("[KNJ_Get_Info]ReturnVal Staff_name() Staff_name error!");
			System.out.println( ex );
		}
		return (new ReturnVal(name,name_show,name_kana,name_eng,null));
	}


	/**	
     *  ＤＢより担任名を取得するメソッド 
     *  2005/12/16 Build
     */
	public List Staff_name( DB2UDB db2, String year, String semester, String grade_hr_class )
    {
        List list = new LinkedList();
		try{
            StringBuffer stb = new StringBuffer();
            stb.append("SELECT  (SELECT STAFFNAME FROM STAFF_MST T2 WHERE T2.STAFFCD = T1.TR_CD1) AS TR_NAME1 ");
            stb.append(       ",(SELECT STAFFNAME FROM STAFF_MST T2 WHERE T2.STAFFCD = T1.TR_CD2) AS TR_NAME2 ");
            stb.append(       ",(SELECT STAFFNAME FROM STAFF_MST T2 WHERE T2.STAFFCD = T1.TR_CD3) AS TR_NAME3 ");
            stb.append("FROM    SCHREG_REGD_HDAT T1 ");
            stb.append("WHERE   T1.YEAR = '" + year + "' ");
            stb.append(    "AND T1.GRADE||T1.HR_CLASS = '" + grade_hr_class + "' ");
			if( !semester.equals("9") )
                stb.append("AND T1.SEMESTER = '" + semester + "' ");
            else{
                stb.append("AND T1.SEMESTER = (SELECT  MAX(SEMESTER) ");
                stb.append(                   "FROM    SCHREG_REGD_HDAT ");
                stb.append(                  "WHERE    T1.YEAR = '" + year + "' ");
                stb.append(                       "AND T1.GRADE||T1.HR_CLASS = '" + grade_hr_class + "')");
            }
			db2.query( stb.toString() );
			ResultSet rs = db2.getResultSet();
			if( rs.next() ){
                if( rs.getString("TR_NAME1") != null )list.add( rs.getString("TR_NAME1") );
                if( rs.getString("TR_NAME2") != null )list.add( rs.getString("TR_NAME2") );
                if( rs.getString("TR_NAME3") != null )list.add( rs.getString("TR_NAME3") );
			}
			db2.commit();
			rs.close();
		} catch( Exception ex ){
			log.debug("List Staff_name() Staff_name error!", ex );
		}
		return list;
	}



	/**	ＤＢより組名称及び担任名を取得するメソッド **/
	public ReturnVal Hrclass_Staff(DB2UDB db2,String year,String semester,String grade,String hr_class){

		String hrclass_name = new String();		//組名称
		String hrclass_abbv = new String();		//組略称
		String staff_name = new String();		//担任名
		String classweeks = new String();		//授業週数
		String classdays = new String();		//授業日数
		try{
			String sql = new String();
			sql = "SELECT "
					+ "HR_NAME,"
					+ "HR_NAMEABBV,"
					+ "STAFFNAME,"
					+ "CLASSWEEKS,"
					+ "CLASSDAYS "
				+ "FROM "
					+ "SCHREG_REGD_HDAT W2 "
					+ "LEFT JOIN STAFF_MST W1 ON W1.STAFFCD=W2.TR_CD1 "
				+ "WHERE "
						+ "YEAR = '" + year + "' "
					+ "AND GRADE || HR_CLASS = '" + grade + hr_class + "' ";
			if( !semester.equals("9") )	sql = sql				//学期指定の場合
					+ "AND SEMESTER = '" + semester + "'";
			else						sql = sql				//学年指定の場合
					+ "AND SEMESTER = (SELECT "
										+ "MAX(SEMESTER) "
									+ "FROM "
										+ "SCHREG_REGD_HDAT W3 "
									+ "WHERE "
											+ "W2.YEAR = W3.YEAR "
										+ "AND W2.GRADE || W2.HR_CLASS = W3.GRADE || W3.HR_CLASS)";
			//System.out.println("[KNJ_Get_Info]ReturnVal Hrclass_Staff() set_sql sql=" + sql);
			db2.query(sql);
			ResultSet rs = db2.getResultSet();
			if( rs.next() ){
				hrclass_name = rs.getString("HR_NAME");
				hrclass_abbv = rs.getString("HR_NAMEABBV");
				staff_name = rs.getString("STAFFNAME");
				classweeks = rs.getString("CLASSWEEKS");
				classdays = rs.getString("CLASSDAYS");
			}
			db2.commit();
			rs.close();
			//System.out.println("[KNJ_Get_Info]ReturnVal Hrclass_Staff() Hrclass_Staff ok!");
		} catch( Exception ex ){
			System.out.println("[KNJ_Get_Info]ReturnVal Hrclass_Staff() Hrclass_Staff error!");
			System.out.println( ex );
		}
		return (new ReturnVal(hrclass_name,hrclass_abbv,staff_name,classweeks,classdays));
	}

	/**
     *  組(学年を除外)名称のマップを取得するメソッド 
     */
	// 2006/04/21 yamashiro
    public static Map getMapForHrclassName (DB2UDB db2) {
        Map rmap = null;
        try {
             String sql = "SELECT NAMECD2, NAME1 FROM NAME_MST WHERE NAMECD1 = 'A021' ORDER BY NAMECD2";
             db2.query (sql);
             ResultSet rs = db2.getResultSet();
             while (rs.next()) {
                 if (rmap == null) rmap = new HashMap();
                 rmap.put (rs.getString("NAMECD2"), rs.getString("NAME1"));
             }
             db2.commit();
             if (rs != null) rs.close();
        } catch (SQLException e) {
             log.error("SQLException", e);
        } catch (Exception e) {
             log.error("Exception", e);
        }
        return rmap;
    }


	/** *****************************************************************************
	*	return値を返す内部クラス
	******************************************************************************* */
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



/**
 *  学校名、校長名を取得するクラス
 *  2005/08/16 Build
 */
private class getSchoolNameClass
{
	private String schoolname = null;			//学校名
	private String staffname = null;			//校長名
	private String jobname = null;				//校長職名

	private void getSchoolName( DB2UDB db2, String year )
	{
		ResultSet rs = null;
		try{
			String sql = "SELECT SCHOOLNAME1, T2.STAFFNAME, T2.JOBNAME "
					   + "FROM   SCHOOL_MST T1 "
					   + "LEFT JOIN(SELECT  W1.YEAR,STAFFNAME,JOBNAME "
						         + "FROM STAFF_YDAT W1 "
						         + "INNER JOIN STAFF_MST W2 ON W2.STAFFCD = W1.STAFFCD "
						         + "LEFT JOIN JOB_MST W3 ON W3.JOBCD = W2.JOBCD "
						         + "WHERE 	W1.YEAR = '" + year + "' AND W2.JOBCD = '0001' "
						         + ")T2 ON T1.YEAR = T2.YEAR "
					   + "WHERE T1.YEAR = '" + year + "' ";
			db2.query(sql);
			rs = db2.getResultSet();
			if( rs.next() ){
				schoolname = rs.getString("SCHOOLNAME1");
				staffname = rs.getString("STAFFNAME");
				jobname = rs.getString("JOBNAME");
			}
		} catch( Exception ex ){
			log.error("error! " + ex );
		} finally{
			try{ if( rs != null )rs.close(); } catch( Exception ex ){ log.error("error! " + ex ); }
			db2.commit();
		}
	}
}


/**
 *  １日出欠集計対象校時を取得するクラス
 *  2005/08/17 Build
 */
private class getTargetPeriodClass
{
	String editperiod = null;
	private void getEditPeriod( DB2UDB db2, 
                                String year, 
                                String semes, 
                                String gradehrclass, 
                                boolean usefromtoperiod )
	{
		StringBuffer stb = null;
		ResultSet rs = null;
		try {
			stb = new StringBuffer();
			stb.append(    "SELECT  NAMECD2 ");
			if( usefromtoperiod ){
				stb.append("FROM    NAME_MST W1, COURSE_MST W2 ");
				stb.append("WHERE   NAMECD1 = 'B001' ");
				stb.append(        "AND S_PERIODCD <= NAMECD2 AND NAMECD2 <= E_PERIODCD ");
				stb.append(        "AND COURSECD IN(SELECT MIN(COURSECD) FROM SCHREG_REGD_DAT W3 ");
				stb.append(                        "WHERE  W3.YEAR = '" + year + "' ");
				stb.append(                               "AND W3.SEMESTER = '" + semes + "' ");
				stb.append(                               "AND W3.GRADE || W3.HR_CLASS = '" + gradehrclass + "') ");
			} else{
				stb.append("FROM    NAME_MST W1 ");
				stb.append("WHERE   NAMECD1 = 'B001' ");
			}
			stb.append("ORDER BY NAMECD2");
			db2.query( stb.toString() );
			rs = db2.getResultSet();
			int i = 0;
			stb.delete( 0, stb.length() );

			while ( rs.next() ){
				if( i++ == 0 )stb.append("(");
				else          stb.append(",");
				stb.append( "'" ).append( rs.getString("NAMECD2") ).append( "'" );
			}
		} catch( Exception ex ){
			log.warn("periodname-get error!",ex);
		} finally{
			try{ if( rs != null )rs.close(); } catch( Exception ex ){ log.error("error! " + ex ); }
			db2.commit();
			if( stb != null ) stb.append(")");
			if( 0 < stb.length() )	editperiod = stb.toString();
			else                    editperiod = "('1','2','3','4','5','6','7','8','9')";
		}
	}
}

}//クラスの括り
