// kanji=漢字
/*
 * $Id: ad04cda8b181b3312863a9e5be279584529a62a8 $
 *
 * 作成日: 2005/05/18
 * 作成者: yamashiro
 *
 * Copyright(C) 2005-2008 ALP Okinawa Co.,Ltd. All rights reserved.
 */

/**
 *
 *  出欠集計の月別集計範囲日付を取得
 *    2005/05/18 yamashiro
 *    2006/02/02 nakamoto
 *
 */

package servletpack.KNJZ.detail;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import nao_package.db.DB2UDB;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class KNJAttendTerm{

    private static final Log log = LogFactory.getLog(KNJAttendTerm.class);
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	private DecimalFormat dmf = new DecimalFormat("00");
    private Calendar cal = Calendar.getInstance( );
	private	PreparedStatement ps;
	private ResultSet rs;
	public String sdate;
	public String edate;


	/** 
     *
     *  月別集計範囲日付を取得
     *  　賢者共通仕様
     *      処理月が学期開始日を含む場合は範囲FROMを学期開始日とする
     *      処理月が学期終了日を含む場合は範囲TOを学期終了日とする
     *      上述に該当しない場合、１日〜月末とする
     *      2006/02/02 nakamoto 賢者共通仕様に修正
     *
     */
//	public void setMonthTermDate( DB2UDB db2, String year, int month )
	public void setMonthTermDate( DB2UDB db2, String year, String semester, int month, boolean junior )
	{
/***
		sdate = null;
		edate = null;
		setSemesterDate( db2, year, month );	//学期日付の取得
		if( sdate == null )sdate = year + "-" + String.valueOf(dmf.format(month)) + "-" + "01";
		if( edate == null )edate = setMonthMaxDate( year, month );
***/
		sdate = null;
		edate = null;
		setSemesterDate( db2, year, semester, month );	//学期日付の取得
		if( sdate == null )
			if( junior )sdate = setJanFebMar(year,month) + "-" + String.valueOf(dmf.format(month)) + "-" + "01";
			else		sdate = setJanFebMar(year,month) + "-" + String.valueOf(dmf.format(month)) + "-" + "02";
			
		if( edate == null ){
			if( junior )edate = setMonthMaxDate( year, month );
			else		edate = setMonthFirstDate( year, month );
		}
	}


	/** 
     *
     *  月別集計範囲日付を取得
     *  　近大付属仕様
     *      処理月が学期開始日を含む場合は範囲FROMを学期開始日とする
     *      処理月が学期終了日を含む場合は範囲TOを学期終了日とする
     *      上述に該当しない場合、高校は２日〜翌月1日、中学は１日〜月末とする
     *
     */
	public void setMonthTermDateK( DB2UDB db2, String year, int month, boolean junior )
	{
		sdate = null;
		edate = null;
		setSemesterDate( db2, year, month );	//学期日付の取得
		if( sdate == null )
			if( junior )sdate = setJanFebMar(year,month) + "-" + String.valueOf(dmf.format(month)) + "-" + "01";
			else		sdate = setJanFebMar(year,month) + "-" + String.valueOf(dmf.format(month)) + "-" + "02";
			
		if( edate == null ){
			if( junior )edate = setMonthMaxDate( year, month );
			else		edate = setMonthFirstDate( year, month );
		}
	}


	/** 
     *
     *  学期の開始日・終了日取得
     *
     */
	public void setSemesterDate(  DB2UDB db2, String year, int month )
	{
		try {
			if( ps == null ){
				try {
					ps = db2.prepareStatement( prestatementSemesterDate( year ) );
				} catch( Exception ex ) {
		            log.error("error! " + ex );
				}
			}
			int pp = 0;
			ps.setInt( ++pp,  month );
			ps.setInt( ++pp,  month );
			ps.setInt( ++pp,  month );
			ps.setInt( ++pp,  month );
			rs = ps.executeQuery();
			if( rs.next() ){
				if( rs.getString("SDATE") != null )sdate = rs.getString("SDATE");
				if( rs.getString("EDATE") != null )edate = rs.getString("EDATE");
			}
		} catch( Exception ex ) {
            log.error("error! " + ex );
        } finally {
            try {
                if( rs != null )rs.close();
                db2.commit();
            } catch( Exception ex ) {
                log.error("error! ", ex );
            }
		}
	}


	/** 
     *
     *  学期の開始日・終了日取得
     *      2006/02/02 nakamoto add
     *
     */
	public void setSemesterDate(  DB2UDB db2, String year, String semester, int month )
	{
		try {
			if( ps == null ){
				try {
					ps = db2.prepareStatement( prestatementSemesterDate( year, semester ) );
				} catch( Exception ex ) {
		            log.error("error! " + ex );
				}
			}
			int pp = 0;
			ps.setInt( ++pp,  month );
			ps.setInt( ++pp,  month );
			ps.setInt( ++pp,  month );
			ps.setInt( ++pp,  month );
			rs = ps.executeQuery();
			if( rs.next() ){
				if( rs.getString("SDATE") != null )sdate = rs.getString("SDATE");
				if( rs.getString("EDATE") != null )edate = rs.getString("EDATE");
			}
		} catch( Exception ex ) {
            log.error("error! " + ex );
        } finally {
            try {
                if( rs != null )rs.close();
                db2.commit();
            } catch( Exception ex ) {
                log.error("error! ", ex );
            }
		}
	}


	/** 
     *
     *  月末を取得
     *
     */
	public String setMonthMaxDate( String year, int month )
	{
		String date = null;
		try {
			cal.set( Integer.parseInt(setJanFebMar(year,month)), month-1, 1 );	//開始日付
			date = cal.get(Calendar.YEAR) + "-" + ( dmf.format(cal.get(Calendar.MONTH)+1) ) + "-" + dmf.format(cal.getActualMaximum(Calendar.DATE));
        } catch( Exception ex ) {
            log.error("error! ", ex );
		}
		return date;
	}


	/** 
     *
     *  翌月1日を取得
     *
     */
	public String setMonthFirstDate( String year, int month )
	{
		String date = null;
		try {
			cal.set( Integer.parseInt(setJanFebMar(year,month)), month-1, 1 );	//開始日付
			cal.add( Calendar.MONTH, 1 );
			date = cal.get(Calendar.YEAR) + "-" + dmf.format(( cal.get(Calendar.MONTH)+1 )) + "-01";
        } catch( Exception ex ) {
            log.error("error! ", ex );
		}
		return date;
	}


	/** 
     *
     *  年の繰越
     *
     */
	public String setJanFebMar( String year, int month )
	{
		int retyear = Integer.parseInt( year );
		try {
			if( 1 <= month  &&  month <= 3 )
				retyear += 1;
        } catch( Exception ex ) {
            log.error("error! ", ex );
		}
		return String.valueOf( retyear );
	}


	/** 
     *
     *  PrepareStatement作成 学期の開始日・終了日取得
     *
     */
	private String prestatementSemesterDate( String year )
	{
        StringBuffer stb = new StringBuffer();
		try {
			stb.append("SELECT  CASE WHEN MONTH(SDATE) = ?  THEN SDATE ELSE NULL END AS SDATE, ");
			stb.append(        "CASE WHEN MONTH(EDATE) = ?  THEN EDATE ELSE NULL END AS EDATE ");
			stb.append("FROM    SEMESTER_MST ");
			stb.append("WHERE   YEAR = '" + year + "' AND SEMESTER < '9' AND ");
			stb.append(        "(MONTH(SDATE) = ? OR MONTH(EDATE) = ?) ");
		} catch( Exception ex ) {
            log.error("error! " + ex );
		}
		return stb.toString();
	}


	/** 
     *
     *  PrepareStatement作成 学期の開始日・終了日取得
     *      2006/02/02 nakamoto add
     *
     */
	private String prestatementSemesterDate( String year, String semester )
	{
        StringBuffer stb = new StringBuffer();
		try {
			stb.append("SELECT  CASE WHEN MONTH(SDATE) = ?  THEN SDATE ELSE NULL END AS SDATE, ");
			stb.append(        "CASE WHEN MONTH(EDATE) = ?  THEN EDATE ELSE NULL END AS EDATE ");
			stb.append("FROM    SEMESTER_MST ");
			stb.append("WHERE   YEAR = '" + year + "' AND SEMESTER = '" + semester + "' AND ");
			stb.append(        "(MONTH(SDATE) = ? OR MONTH(EDATE) = ?) ");
		} catch( Exception ex ) {
            log.error("error! " + ex );
		}
		return stb.toString();
	}


	/**PrepareStatement close**/
	void prestatementClose()
	{
		try {
			if( ps != null ) ps.close();
		} catch( Exception ex ){
            log.error("error! " + ex );
		}
	}


}
