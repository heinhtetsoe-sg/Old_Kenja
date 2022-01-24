/**
 *
 *	学校教育システム 賢者 [出欠管理] 出欠累積データおよび出欠データから累積データを取得する際
 *
 *	2005/02/05 yamashiro 新規作成
 *  2005/04/11 yamashiro 集計日が１日は翌月１日と判断していたが、集計月の１日とする
 *  2005/04/28 yamashiro 集計日が１の場合、翌月の１日と判断する処理を学校別で実行する
 *  2005/05/11 yamashiro 'extends KNJDefineCode'を削除
 *                       近大付属において集計日が1日の場合翌月の１日と判断する仕様を高校と中学で共通とする
 *  2005/05/16 yamashiro 近大付属において集計日が1日の場合翌月の１日と判断する仕様を高校のみとする
 *                       ATTEND_SEMES_DATを読み込む際、APPOINTED_DAYがNULLの場合は処理しない
 *  2006/01/27 yamasihro・2学期制の場合、1月〜3月の出欠集計データのソート順による集計の不具合を修正 --NO001
 *  
 */

package servletpack.KNJC;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.text.DecimalFormat;
import nao_package.db.DB2UDB;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import servletpack.KNJZ.detail.KNJ_Get_Info;
import servletpack.KNJZ.detail.KNJDefineCode;


//public class KNJDivideAttendDate extends KNJDefineCode{
public class KNJDivideAttendDate{

    private static final Log log = LogFactory.getLog(KNJDivideAttendDate.class);
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	private	DecimalFormat dmf1 = new DecimalFormat("00");
    public String date;
    public String month;
	public String enddate;					//05/03/05Modify
	private KNJDefineCode definecode;		//各学校における定数等設定 05/04/28

	/** 
     *  パラメータセット 2005/02/04
     *      date  :attend_semes_datの最終集計日の翌日をセット
     *      month :attend_semes_datの最終集計学期＋月をセット
     *      引数について  String year      :処理年度
     *                    String gakki     :処理学期
     *                    String attendenddate :出欠データ集計期間終了日
     */
    public void getDivideAttendDate( DB2UDB db2, String year, String gakki, String attendenddate )
    {
		setClasscode( db2, year );     //各学校における定数等設定 05/04/28
log.debug("schoolmark = " + definecode.schoolmark );

	    Calendar cal1 = Calendar.getInstance();
	    Calendar cal2 = Calendar.getInstance();
		ResultSet rs = null;
log.debug("attendenddate="+attendenddate + "   year="+year + "   gakki="+gakki);

	//	学期範囲の取得
        if( attendenddate == null ||  attendenddate.equals("0") ){
            try {
                KNJ_Get_Info getinfo = new KNJ_Get_Info();		//各情報取得用のクラス
                KNJ_Get_Info.ReturnVal returnval = null;		//各情報を返すためのクラス
                returnval = getinfo.Semester( db2, year, gakki );
                attendenddate = returnval.val3;									//学期期間TO
            } catch( Exception ex ){
                log.warn("term1 svf-out error!",ex);
            } finally {
                if( attendenddate == null ) attendenddate = ( Integer.parseInt(year) + 1 ) + "-03-31";
				enddate = attendenddate;	//05/03/05Modify
            }
        }

		try {
		    cal1.setTime( sdf.parse( attendenddate ) );	//出欠集計範囲の終わり
 		} catch( Exception ex ){
			log.error("getParam2 error:" + ex);
		}

		try {
            StringBuffer stb = new StringBuffer();  //--NO001 StringBufferへ変更
            //NO001 Modify
            stb.append("SELECT  SEMESTER || MONTH AS SM, ");
            stb.append(        "MAX((CASE WHEN MONTH BETWEEN '01' AND '03' THEN RTRIM(CHAR(INT(YEAR)+1)) ELSE YEAR END ) || '-' || MONTH || '-' || APPOINTED_DAY) ");
            stb.append("FROM    ATTEND_SEMES_DAT ");
            stb.append("WHERE   YEAR = '" + year + "' AND ");
            stb.append(        "(CASE WHEN MONTH BETWEEN '01' AND '03' THEN RTRIM(CHAR(INT(YEAR)+1)) ELSE YEAR END ) || MONTH  ");
            stb.append(                       "<='"+ String.valueOf( cal1.get(Calendar.YEAR) ) + "" + String.valueOf( dmf1.format( cal1.get(Calendar.MONTH) + 1 )) + "' ");
            stb.append("GROUP BY SEMESTER, MONTH ");
            //NO001 stb.append("ORDER BY SEMESTER DESC, MONTH DESC ");
            stb.append("ORDER BY (CASE WHEN INT(MONTH) < 4 THEN CHAR(INT(SEMESTER) + 1 ) ELSE SEMESTER END )||MONTH DESC ");  //--NO001
            db2.query( stb.toString() );
            /*  NO001 Delete
            db2.query("SELECT  SEMESTER || MONTH AS SM, "
            +                 "MAX((CASE WHEN MONTH BETWEEN '01' AND '03' THEN RTRIM(CHAR(INT(YEAR)+1)) ELSE YEAR END ) || '-' || MONTH || '-' || APPOINTED_DAY)"
            +         "FROM    ATTEND_SEMES_DAT "
            +         "WHERE   YEAR = '" + year + "' AND "
            +                 "(CASE WHEN MONTH BETWEEN '01' AND '03' THEN RTRIM(CHAR(INT(YEAR)+1)) ELSE YEAR END ) || MONTH "
            +                       "<='"+ String.valueOf( cal1.get(Calendar.YEAR) ) + "" + String.valueOf( dmf1.format( cal1.get(Calendar.MONTH) + 1 )) + "' "
            +         "GROUP BY SEMESTER, MONTH "
            +         "ORDER BY SEMESTER DESC, MONTH DESC ");
            */
			rs = db2.getResultSet();
            while ( rs.next() ){
				if( rs.getString(2) == null )continue;      // 05/05/16 NULLの場合は処理回避
		        cal2.setTime( sdf.parse( rs.getString(2) ) );

				if( definecode.schoolmark.equals("KIN") )	//    05/04/28 Modify 05/05/16 Revive
				//if( 3 <= definecode.schoolmark.length()  &&  definecode.schoolmark.substring(0,3).equals("KIN") )	//    05/04/28 Modify 05/05/11Modify
                	if( cal2.get( Calendar.DATE ) == 1 )	//    集計日が１の場合、翌月の１日と判断する
                		cal2.add( Calendar.MONTH, 1 );		//    処理を学校別で実行する


//log.debug("cal2="+sdf.format(cal2.getTime()));
                if( cal2.after(cal1) ) continue;   //集計テーブルの集計締日が指示画面の締日より大きければ前の月を読む
                date = sdf.format( cal2.getTime() );
                month = rs.getString(1);
                break;
            }
		} catch( Exception ex ) {
			log.warn("ResultSet-read error!",ex);
		} finally{
			try {
				if( rs != null )rs.close();
                db2.commit();
			} catch( SQLException ex ) {
				log.warn("ResultSet-close error!",ex);
			}
		}

		try {
            //集計テーブルの集計締日の翌日をセット
            if( date != null ){
                cal2.setTime( sdf.parse( date ) );
                cal2.add( Calendar.DATE, 1 );
                date = sdf.format( cal2.getTime() );
            }
            //集計テーブルのデータが存在しない場合は該当学期の開始日をセット
            if( date == null ){
                KNJ_Get_Info getinfo = new KNJ_Get_Info();
                KNJ_Get_Info.ReturnVal returnval = null;
                try {
                    returnval = getinfo.Semester( db2, year, ( gakki.equals("9") )? "1" : gakki );
                    date = returnval.val2;					//学期期間FROM
                } catch( Exception ex ){
                    log.warn("term1 svf-out error!",ex);
                } finally {
                    if( date == null ) date = year + "-04-01";
                    month = "003";
                }
            }
            //集計テーブル締月をセット
 		} catch( Exception ex ){
			log.error("getParam2 error:" + ex);
		}
log.debug("date="+date + "   month="+month);
    }


	/**
     *  クラス内で使用する定数設定
     *    2004/04/28
     */
	private void setClasscode( DB2UDB db2, String year )
	{
		try {
			definecode = new KNJDefineCode();
			definecode.defineCode( db2, year );     	//各学校における定数等設定
		} catch( Exception ex ){
			log.warn("semesterdiv-get error!",ex);
		}
	}


}//クラスの括り
