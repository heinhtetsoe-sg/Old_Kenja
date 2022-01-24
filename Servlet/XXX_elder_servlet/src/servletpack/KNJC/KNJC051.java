package servletpack.KNJC;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJ_ClassCodeImp;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_Get_Info;
import servletpack.KNJZ.detail.KNJ_Get_Info.ReturnVal;

/*<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*
 *
 *	学校教育システム 賢者 [出欠管理]
 *
 *					＜ＫＮＪＣ０５１＞  出席簿（科目別）
 *
 *	2004/07/07・新様式としてKNJC050とは別途作成
 *	2004/09/14 yamashiro・SQL文の不具合を修正->ペナルティ欠課選択の場合SQLの不具合が起きる
 *	2004/09/28 yamashiro・単位がnullの場合の不具合を修正
 *						・校時名称が'SHR'の場合は校時を出力しない-->10/06:数字以外は出力しないへ変更
 *	2004/09/29 yamashiro・SHRやLHRの場合総ページ数が０になる不具合を修正
 *						・遅刻コードに'7'を追加
 *	2004/10/06 yamashiro・出欠データ出力において時間割データにないデータは出力しない
 *  2004/10/28 yamashiro・'〜'出力の不具合を修正
 *  2004/11/15 yamashiro・累計処理は時間割データとリンク(学籍番号・日付・校時)したデータを集計する
 *<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<*/

public class KNJC051 {

    private static final Log log = LogFactory.getLog(KNJC051.class);
	PreparedStatement ps1,ps2,ps3,ps4,ps5,ps6;
	KNJ_ClassCodeImp ccimp = null;	//教科コード等定数設定
	int cnt_sum[][];				//出欠の累計用配列
	boolean srs_sum[][];			//出欠の累計'*'用配列 04/10/26Add
	int c_chr[];					//学期別授業時数
    int subclasscredit;             //科目単位数 04/10/17Add
   	String param[] = new String[16];
	boolean nonedata;



	/*----------------------------*
	 * HTTP Get リクエストの処理  *
	 *----------------------------*/
	public void svf_out(HttpServletRequest request, HttpServletResponse response)
	                 throws ServletException, IOException
	{

		Vrw32alp svf = new Vrw32alp(); 	// PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
		DB2UDB db2 = null;				// Databaseクラスを継承したクラス

	// パラメータの取得
	    getParam(request);

	// print svf設定
		setSvfInit(response, svf);

	// ＤＢ接続
		db2 = setDb(request);
		if( openDb(db2) ){
			log.error("db open error");
			return;
		}

	// 印刷処理
		printSvf(db2, svf);

	// 終了処理
		closeSvf(svf);
		closeDb(db2);

    }//doGetの括り



	/* svf print 印刷処理 */
    void printSvf(DB2UDB db2, Vrw32alp svf)
	{
		ccimp = new KNJ_ClassCodeImp(db2);						//教科コード・ページ行数・列数等定数設定のインスタンス
		Set_Head(db2,svf);	//見出し出力のメソッド
		//SQL作成
        setPrepareStatement(db2);
		Set_Detail_1(db2,svf);			//出力処理のメソッド
        Pre_Stat_f();
    }



	/* svf print 印刷処理 */
    void setPrepareStatement(DB2UDB db2)
	{
		try {
			ps1 = db2.prepareStatement(Pre_Stat_1(0));		//生徒情報
//log.debug("ps1="+ps1.toString() );
			ps2 = db2.prepareStatement(Pre_Stat_2(0));		//科目
			ps3 = db2.prepareStatement(Pre_Stat_3());		//出欠詳細
			ps4 = db2.prepareStatement(Pre_Stat_4( ( param[15]==null )? false:true ));		//出欠集計
		} catch( Exception ex ){
			log.error("db2.prepareStatement error!",ex);
		}
    }



	/** 出力処理メインルーチン -> 生徒を、ページ当りの出力行数ごと処理を行う**/
	void Set_Detail_1(DB2UDB db2,Vrw32alp svf){

		ResultSet rs = null;
//log.debug("ps1="+ps1.toString() );

		try {
		//	生徒名等ResultSet作成
			int pi = 0;
			ps1.setString(++pi,param[0]);	//年度
			ps1.setString(++pi,param[1]);	//学期
			ps1.setString(++pi,param[2]);	//講座
			ps1.setString(++pi,param[13]);	//印刷範囲開始日
			ps1.setString(++pi,param[13]);	//印刷範囲開始日
			ps1.setString(++pi,param[14]);	//印刷範囲終了日
			ps1.setString(++pi,param[14]);	//印刷範囲終了日
			ps1.setString(++pi,param[13]);	//印刷範囲開始日
			ps1.setString(++pi,param[14]);	//印刷範囲終了日
			ps1.setString(++pi,param[0]);	//年度
			ps1.setString(++pi,param[0]);	//年度
			ps1.setString(++pi,param[1]);	//学期
			ps1.setString(++pi,param[0]);	//年度
			rs = ps1.executeQuery();

		//	生徒名等SVF出力
			cnt_sum = new int[4][ccimp.svfline];		//学期累計用配列 4項目づつ
			srs_sum = new boolean[2][ccimp.svfline];	//学期累計用配列 04/10/26Add
			Map hm1 = new HashMap();										//学籍番号と行番号の保管
			int schno = 0;
			while( rs.next() ){
				hm1.put(rs.getString("SCHREGNO"),new Integer(++schno));		//行番号に学籍番号を付ける
				Set_Detail_1_1(svf,rs,schno);								//生徒名等出力のメソッド
				if( schno==1 )param[10] = rs.getString("ATTENDNO2");		//開始生徒
				param[11] = rs.getString("ATTENDNO2");						//終了生徒
				if( schno==ccimp.svfline ){
					if( Set_Detail_2(db2,svf,hm1) )nonedata = true;	//科目、出欠の取得と出力のメソッド
					hm1.clear();											//行番号情報を削除
					schno = 0;
					param[10] = null;										//開始生徒
					param[11] = null;										//終了生徒
				}
			}
			if( schno>0 )
				if( Set_Detail_2(db2,svf,hm1) )nonedata = true;		//科目、出欠の取得と出力のメソッド
		} catch( Exception ex ){
			log.warn("error!",ex);
		} finally{
			db2.commit();
			DbUtils.closeQuietly(rs);
		}

	}//Set_Detail_1()の括り



	/** 生徒名等出力 **/
	private void Set_Detail_1_1(Vrw32alp svf, ResultSet rs, int ia) {

		try {
			svf.VrsOutn("HR_NAME",ia,rs.getString("ATTENDNO"));	//学年組出席番号
			svf.VrsOutn("NAME_SHOW",ia,rs.getString("NAME"));		//生徒名
		} catch( SQLException ex ){
			log.warn("svf-out error!",ex);
		}

	}//Set_Detail_1_1()の括り



	/** 科目、出欠の取得と出力 **/
	private boolean Set_Detail_2(DB2UDB db2, Vrw32alp svf, Map hm1) {

		boolean nonedata = false;
		ResultSet rs1 = null;
		ResultSet rs2 = null;
		int pi = 0;

		try {
		//	日付、校時等ResultSet作成
			ps2.setString(++pi,param[13]);	//開始日付
			ps2.setString(++pi,param[14]);	//終了日付
			ps2.setString(++pi,param[2]);	//講座
			rs1 = ps2.executeQuery();
		//	出欠データResultSet作成
			pi = 0;
			ps3.setString(++pi,param[2]);	//講座
			ps3.setString(++pi,param[13]);	//開始日付
			ps3.setString(++pi,param[14]);	//終了日付
			ps3.setString(++pi,param[0]);	//年度
			ps3.setString(++pi,param[10]);	//開始生徒番号
			ps3.setString(++pi,param[11]);	//終了生徒番号
			ps3.setString(++pi,param[0]);	//年度
			ps3.setString(++pi,param[1]);	//学期
			ps3.setString(++pi,param[13]);	//開始日付
			ps3.setString(++pi,param[14]);	//終了日付
			ps3.setString(++pi,param[2]);	//講座
			rs2 = ps3.executeQuery();

		//	日付等SVF出力
			boolean bl1 = false;		//列(日付)出力フラグ
			boolean bl2 = false;		//出欠データ出力フラグ
			int stop = 0;
			int lcount = 0;
			while (rs1.next()) {
				if (bl1) {								//列を出力
					svf.VrEndRecord();
					lcount++;
					nonedata = true;
					bl1 = false;
				}
				Set_Detail_2_1(svf,rs1);				//日付等出力のメソッド
				for (;;) {
					if( stop++ ==2000 )break;			//avoid parmanent roop !
					if( bl2 ){							//出欠データを読込んでいる場合
						if (rs1.getString("EXECUTEDATE").equals(rs2.getString("ATTENDDATE")) && rs1.getString("PERIODCD").equals(rs2.getString("PERIODCD"))) {
							Set_Detail_2_2(svf,rs2,hm1);				//出欠データ出力のメソッド
							bl2 = false;
						} else {
							break;
						}
					} else{
						if (!rs2.next()) {
							break;
						} else {
							bl2 = true;
							if( !rs1.getString("EXECUTEDATE").equals(rs2.getString("ATTENDDATE")) || !rs1.getString("PERIODCD").equals(rs2.getString("PERIODCD"))) {
							    break;	// 04/10/06Modify
							}
						}
					}
				}
				bl1 = true;
			}
			if( bl1 ){			//最終ページに累計を出力
				Set_Detail_4(db2,svf,hm1);		//出欠集計の取得と出力のメソッド
				svf.VrEndRecord();
				lcount++;
				for( ; lcount%ccimp.svfline2>0 ; lcount++ )svf.VrEndRecord();
				nonedata = true;
				svf.VrPrint();
				clr_svf(svf);
			}
		} catch( Exception ex ){
			log.warn("error!",ex);
		} finally{
			db2.commit();
            DbUtils.closeQuietly(rs1);
            DbUtils.closeQuietly(rs2);
		}

		return nonedata;

	}//Set_Detail_2()の括り



	/** 日付、校時等出力 **/
	private void Set_Detail_2_1(Vrw32alp svf,ResultSet rs){

		try {
			svf.VrsOut("day_item_mm", KNJ_EditDate.h_format_S(rs.getString("EXECUTEDATE"),"M"));
			svf.VrsOut("day_item_dd", KNJ_EditDate.h_format_S(rs.getString("EXECUTEDATE"),"d"));
			svf.VrsOut("day_item_week", KNJ_EditDate.h_format_W(rs.getString("EXECUTEDATE")));
		//	if( rs.getString("PERIODNAME")!=null && !rs.getString("PERIODNAME").equals("SHR") )		// 04/09/28Add
		//		svf.VrsOut("day_item_hh", rs.getString("PERIODNAME"));
			if( rs.getString("PERIODNAME")!=null ){		// 04/10/06Modify
				try {
					if( Character.isDigit((rs.getString("PERIODNAME")).charAt(0)) )
						svf.VrsOut("day_item_hh", rs.getString("PERIODNAME"));
				} catch( SQLException ex ){
					log.warn("periodname get error!",ex);
				}
			}


		} catch( SQLException ex ){
			log.warn("svf-out error!",ex);
		}

	}//Set_Detail_2_1()の括り



	/** 出欠データ出力 **/
	private void Set_Detail_2_2(Vrw32alp svf,ResultSet rs,Map hm1){

		try {
			Integer int1 = (Integer)hm1.get(rs.getString("SCHREGNO"));
			if( int1!=null ){
				svf.VrsOutn("attend", int1.intValue(), rs.getString("DI_NAME"));
			}
		} catch( SQLException ex ){
			log.warn("svf-out error!",ex);
		}

	}//Set_Detail_2_2()の括り



	/** 出欠集計取得と印刷 **/
	private void Set_Detail_4(DB2UDB db2,Vrw32alp svf,Map hm1){

		for(int i = 0 ; i < cnt_sum.length ; i++) for(int j = 0 ; j < cnt_sum[i].length ; j++) cnt_sum[i][j] = 0;
		for(int i = 0 ; i < srs_sum.length ; i++) for(int j = 0 ; j < srs_sum[i].length ; j++) srs_sum[i][j] = false;   // 04/10/26Add
        //if( log.isTraceEnabled() ) for(int i = 0 ; i < param.length ; i++ ) log.trace("param["+i+"]=" + param[i]);

		ResultSet rs = null;
		try {
			java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
			Calendar cal1 = Calendar.getInstance();
			cal1.setTime(sdf.parse( param[14] ));	//印刷範囲終了日
			Calendar cal2 = Calendar.getInstance();
			StringTokenizer	scode = new StringTokenizer(param[6],",");	//学期コード
			StringTokenizer	sdate = new StringTokenizer(param[8],",");	//学期開始日
			StringTokenizer	edate = new StringTokenizer(param[9],",");	//学期終了日
			String s1 = null;
			String s2 = null;
			String s3 = null;
			int pi = 0;
			log.debug("ps4="+ps4.toString());
			for( ; ; ){
				if( !scode.hasMoreTokens() )break;
				s1 = scode.nextToken();									//学期コード
				if( s1.equals("9") )break;
				if( sdate.hasMoreTokens() )s2 = sdate.nextToken();		//学期開始日
				if( edate.hasMoreTokens() )s3 = edate.nextToken();		//学期終了日
				cal2.setTime(sdf.parse( s2 ));	//学期開始日
				if( cal1.before(cal2) )break;	//印刷終了日が学期開始日の前なら以降の学期は出力しない！
				cal2.setTime(sdf.parse( s3 ));	//学期終了日
				if( cal1.before(cal2) )			//印刷終了日が学期終了日の前なら集計期間は印刷終了日までとする！
					s3 = sdf.format(cal1.getTime());
				pi = 0;
				//ps4.setString(++pi,param[0]);	//年度
				ps4.setString(++pi,s1);			//学期
				//ps4.setString(++pi,param[2]);	//講座
				//ps4.setString(++pi,s2);			//開始日付
				//ps4.setString(++pi,s2);			//開始日付
				//ps4.setString(++pi,s3);			//終了日付
				//ps4.setString(++pi,s3);			//終了日付
				ps4.setString(++pi,s2);			//開始日付
				ps4.setString(++pi,s3);			//終了日付
				ps4.setString(++pi,s2);			//開始日付
				ps4.setString(++pi,s3);			//終了日付
				//ps4.setString(++pi,param[0]);	//年度
				ps4.setString(++pi,s1);			//学期
				ps4.setString(++pi,param[10]);	//開始生徒
				ps4.setString(++pi,param[11]);	//終了生徒
				log.debug("s1="+s1);
				log.debug("s2="+s2);
				log.debug("s3="+s3);
				log.debug("param[10]="+param[10]);
				log.debug("param[11]="+param[11]);

				rs = ps4.executeQuery();
				while (rs.next()) {
				    Set_Detail_4_1(svf,rs,hm1,s1);		//出欠集計のメソッド
				}
                Set_Detail_4_2(svf,hm1,s1);	                            //出欠集計出力のメソッド  04/10/26Add
		        for (int i = 0 ; i < srs_sum.length ; i++) {
		            for(int j = 0 ; j < srs_sum[i].length ; j++) {
		                srs_sum[i][j] = false;   // 04/10/26Add
		            }
		        }
			}
		} catch( Exception ex ){
			log.warn("ResultSet read error!",ex);
		} finally{
			db2.commit();
			if( rs != null )try {rs.close();} catch( Exception ex ) {log.warn("ResultSet close error!",ex);}
		}

	}//Set_Detail_4()の括り



	/** 出欠集計印刷 04/10/26Modify **/
	void Set_Detail_4_1(Vrw32alp svf,ResultSet rs,Map hm1,String s1){       // 04/10/17Modify

		try {
			Integer int1 = (Integer)hm1.get(rs.getString("SCHREGNO"));
			if( int1!=null ){
				cnt_sum[0][int1.intValue()-1] += rs.getInt("ABSENT");
				cnt_sum[1][int1.intValue()-1] += rs.getInt("LATE");
				cnt_sum[2][int1.intValue()-1] += rs.getInt("EARLY");
				cnt_sum[3][int1.intValue()-1] += rs.getInt("ABSENT2");
				//if( rs.getInt("ABSENT2") > c_chr[Integer.parseInt(s1)-1] / 3 )
				if( rs.getInt("ABSENT2") >= c_chr[Integer.parseInt(s1)-1] / 3 )     // 04/10/26Modify
                    srs_sum[0][int1.intValue()-1] = true;    //欠課時数* 04/10/26Add
			}
		} catch( SQLException ex ){
			log.warn("error!",ex);
		}

	}//Set_Detail_4_1()の括り


	/** 出欠集計出力 04/10/26Add **/
	void Set_Detail_4_2(Vrw32alp svf ,Map hm1 ,String s1){

		try {
            for( int i = 0 ; i < cnt_sum[0].length && i < hm1.size() ; i++ ){
				svf.VrsOutn("TOTAL_ABSENCE"+s1 ,i+1 ,getSumstr(cnt_sum[0][i]) );	//欠席
				svf.VrsOutn("TOTAL_LATE"   +s1 ,i+1 ,getSumstr(cnt_sum[1][i]) );	//遅刻
				svf.VrsOutn("TOTAL_LEAVE"  +s1 ,i+1 ,getSumstr(cnt_sum[2][i]) );	//早退

				if( srs_sum[0][i] == true )
					svf.VrsOutn("TOTAL_TIME"+s1 ,i+1 ,"*" + getSumstr(cnt_sum[3][i]) );	//欠課時数
				else
					svf.VrsOutn("TOTAL_TIME"+s1 ,i+1 ,getSumstr(cnt_sum[3][i]) );	        //欠課時数
			}
		} catch( Exception ex ){
			log.warn("error!",ex);
		}

	}//Set_Detail_4_2()の括り



	/** 出欠累計の編集 ゼロは否出力 04/10/26Add **/
	String getSumstr(int intsum){

        String strx = "";
		try {
            if( intsum > 0 )strx = String.valueOf(intsum);
		} catch( Exception ex ){
			log.warn("error!",ex);
		}
        return strx;

	}//String getSumstr()の括り



	/**PrepareStatement作成**/
	String Pre_Stat_1(int pdiv){

	//	出席番号、氏名
		StringBuffer stb = new StringBuffer();
		// pdiv==0:生徒の出席番号、氏名等を取得   pdiv!=0:生徒数、単位数を取得
		try {
			if (pdiv == 0) {
				stb.append("SELECT T1.SCHREGNO,T3.NAME,T4.HR_NAMEABBV||'-'||CHAR(INT(T2.ATTENDNO))AS ATTENDNO,");
				stb.append(		  "T2.GRADE||T2.HR_CLASS||T2.ATTENDNO AS ATTENDNO2 ");
			} else {
				stb.append("SELECT COUNT(T1.SCHREGNO)AS COUNT,MAX(CREDITS)AS CREDITS ");
			}
			stb.append("FROM  (SELECT DISTINCT SCHREGNO FROM CHAIR_STD_DAT ");
  			stb.append(		  "WHERE  YEAR=? AND SEMESTER<=? AND CHAIRCD=? AND((APPDATE<=? AND APPENDDATE>=?)OR");
			stb.append(				 "(APPDATE<=? AND APPENDDATE>=?)OR(APPDATE>=? AND APPENDDATE<=?))");
			stb.append(		  "GROUP BY SCHREGNO)AS T1, ");

			if (pdiv == 0) {
				stb.append(	  "SCHREG_REGD_DAT T2,SCHREG_BASE_MST T3,SCHREG_REGD_HDAT T4 ");
			} else {					// 04/09/29Modify
				stb.append(	  "SCHREG_REGD_DAT T2 LEFT JOIN CREDIT_MST T3 ON ");
				stb.append(	  "T3.YEAR=? AND T3.SUBCLASSCD=? AND ");
				stb.append(   "T3.GRADE=T2.GRADE AND T3.COURSECODE=T2.COURSECODE AND ");
				stb.append(   "T3.MAJORCD=T2.MAJORCD AND T3.COURSECD=T2.COURSECD ");
			}

			stb.append("WHERE  T2.YEAR=? AND T2.SCHREGNO=T1.SCHREGNO AND ");
			stb.append(		  "T2.SEMESTER=(SELECT MAX(SEMESTER) FROM SCHREG_REGD_DAT W2 ");
			stb.append(					   "WHERE W2.YEAR=? AND W2.SEMESTER<=? AND W2.SCHREGNO=T2.SCHREGNO)");

			if (pdiv == 0) {
				stb.append(   "AND T2.SCHREGNO=T3.SCHREGNO AND ");
				stb.append(   "T4.YEAR=? AND T4.SEMESTER=T2.SEMESTER AND T4.GRADE=T2.GRADE AND T4.HR_CLASS=T2.HR_CLASS ");
				stb.append("ORDER BY T2.GRADE,T2.HR_CLASS,T2.ATTENDNO ");
			}

		} catch (Exception ex) {
			log.warn("sql-statement error!", ex);
		}
		return stb.toString();



	}//Pre_Stat_1の括り



	/**PrepareStatement作成**/
	String Pre_Stat_2(int pdiv) {

	//	科目日付、校時
		StringBuffer stb = new StringBuffer();
		try {
			if (pdiv == 0) {
				stb.append("SELECT EXECUTEDATE,PERIODCD, ");
				stb.append(		  "Meisyou_Get(W1.PERIODCD,'B001',1)AS PERIODNAME ");
			} else {
				stb.append("SELECT SEMESTER,COUNT(*)AS COUNT ");
			}
			stb.append("FROM   SCH_CHR_DAT W1 ");
			if (pdiv == 0) {
			    stb.append("WHERE  EXECUTEDATE>=? AND EXECUTEDATE<=? AND CHAIRCD=? ");
			} else {
			    stb.append("WHERE  YEAR='"+param[0]+"' AND SEMESTER<='"+param[1]+"' AND CHAIRCD=? AND ");
			    stb.append(       "EXECUTEDATE>=? AND EXECUTEDATE<=? ");
            }
			if (pdiv == 0) {
				stb.append("ORDER BY EXECUTEDATE,PERIODCD");
			} else {
				stb.append("GROUP BY SEMESTER");
			}
		} catch (Exception ex) {
			log.warn("sql-statement error!", ex);
		}
		return stb.toString();

	}//Pre_Stat_2()の括り



	/**PrepareStatement作成**/
	String Pre_Stat_3(){

	//	出欠データ
		StringBuffer stb = new StringBuffer();
		try {
			stb.append("SELECT W2.GRADE||W2.HR_CLASS||W2.ATTENDNO AS ATTENDNO2,W1.SCHREGNO,ATTENDDATE,PERIODCD,DI_CD, ");
			stb.append(		  "Meisyou_Get(W1.DI_CD,'C001',4)AS DI_NAME ");
			stb.append("FROM   ATTEND_DAT W1,SCHREG_REGD_DAT W2 ");
			stb.append("WHERE  CHAIRCD=? AND ATTENDDATE>=? AND ATTENDDATE<=? AND ");
			stb.append(		  "W2.YEAR=? AND W2.GRADE||W2.HR_CLASS||W2.ATTENDNO>=? AND ");
			stb.append(		  "W2.GRADE||W2.HR_CLASS||W2.ATTENDNO<=? AND W2.SCHREGNO=W1.SCHREGNO AND ");
			stb.append(		  "W2.SEMESTER=(SELECT MAX(SEMESTER) FROM SCHREG_REGD_DAT S2 ");
			stb.append(					   "WHERE S2.YEAR=? AND S2.SEMESTER<=? AND S2.SCHREGNO=W2.SCHREGNO) AND ");
			stb.append(		  "EXISTS(SELECT EXECUTEDATE,PERIODCD ");	// 04/10/06 EXISTS...Add
			stb.append(				 "FROM   SCH_CHR_DAT W3 ");
			stb.append(				 "WHERE  EXECUTEDATE>=? AND EXECUTEDATE<=? AND CHAIRCD=? AND ");
			stb.append(						"W3.PERIODCD=W1.PERIODCD AND W3.EXECUTEDATE=W1.ATTENDDATE) ");
			stb.append("ORDER BY ATTENDDATE,PERIODCD ");
		} catch( Exception ex ){
			log.warn("sql-statement error!",ex);
		}
		return stb.toString();

	}//Pre_Stat_3()の括り



	/**PrepareStatement作成**/
	String Pre_Stat_4(boolean output){

	//	出欠集計
		StringBuffer stb = new StringBuffer();
		try {
           /* ******************************************
			stb.append("WITH ATTEND_A AS(");
			stb.append("SELECT W1.SCHREGNO,W2.GRADE,W2.HR_CLASS,W2.ATTENDNO,ATTENDDATE,PERIODCD,DI_CD ");
			stb.append("FROM   ATTEND_DAT W1,SCHREG_REGD_DAT W2,");
			stb.append(		 "(SELECT DISTINCT SCHREGNO,CHAIRCD FROM CHAIR_STD_DAT ");
  			stb.append(		  "WHERE  YEAR=? AND SEMESTER=? AND CHAIRCD=? AND((APPDATE<=? AND APPENDDATE>=?)OR");
			stb.append(				 "(APPDATE<=? AND APPENDDATE>=?)OR(APPDATE>=? AND APPENDDATE<=?)))AS W3 ");
			stb.append("WHERE  W1.ATTENDDATE>=? AND W1.ATTENDDATE<=? AND ");
			stb.append(       "W2.YEAR=? AND W2.SEMESTER=? AND W2.SCHREGNO=W1.SCHREGNO AND ");
			stb.append(		  "W2.GRADE||W2.HR_CLASS||W2.ATTENDNO>=? AND W2.GRADE||W2.HR_CLASS||W2.ATTENDNO<=? AND ");
			//stb.append(       "W3.YEAR=? AND W3.SEMESTER=? AND W3.CHAIRCD=? AND ");
			stb.append(       "W3.SCHREGNO=W2.SCHREGNO AND W3.CHAIRCD=W1.CHAIRCD) ");
			stb.append("SELECT T1.SCHREGNO,");
         ******************************************* */

           /*
            * 出欠データを時間割データ、講座生徒データとリンクした表
            * 2004/11/15Modify 出欠集計処理と同様にする => 時間割データとリンク
            **/
			stb.append("WITH ATTEND_A AS(");
            stb.append(  "SELECT S2.GRADE,S2.HR_CLASS,S2.ATTENDNO,S2.SCHREGNO,S2.ATTENDDATE,S2.PERIODCD,S2.DI_CD ");
            stb.append(  "FROM  (SELECT T2.SCHREGNO ,T1.EXECUTEDATE, T1.PERIODCD, SUBCLASSCD ");
            stb.append(         "FROM   SCH_CHR_DAT T1,");
            stb.append(                "CHAIR_STD_DAT T2,");
            stb.append(                "CHAIR_DAT T3 ");
            stb.append(         "WHERE  T1.YEAR = '" + param[0] + "' AND ");
            stb.append(                "T1.SEMESTER = ? AND ");
            stb.append(                "T1.CHAIRCD = '" + param[2] + "' AND ");
            stb.append(                "? <= T1.EXECUTEDATE AND T1.EXECUTEDATE <= ? AND ");
            stb.append(                "T1.PERIODCD != '0' AND ");
            stb.append(                "T1.CHAIRCD = T2.CHAIRCD AND ");
            stb.append(                "T1.YEAR = T2.YEAR AND ");
            stb.append(                "T1.SEMESTER = T2.SEMESTER AND ");
            stb.append(                "T1.EXECUTEDATE BETWEEN T2.APPDATE AND T2.APPENDDATE AND ");
            stb.append(                "T1.CHAIRCD = T3.CHAIRCD AND ");
            stb.append(                "T1.YEAR = T3.YEAR AND ");
            stb.append(                "T1.SEMESTER = T3.SEMESTER ");
            stb.append(         "GROUP BY T2.SCHREGNO,T1.EXECUTEDATE,T1.PERIODCD, SUBCLASSCD)S1,");
            stb.append(        "(SELECT W1.SCHREGNO,W2.GRADE,W2.HR_CLASS,W2.ATTENDNO,ATTENDDATE,PERIODCD,DI_CD ");
            stb.append(         "FROM   ATTEND_DAT W1,");
            stb.append(                "SCHREG_REGD_DAT W2 ");
            stb.append(         "WHERE  ? <= W1.ATTENDDATE AND W1.ATTENDDATE <= ? AND ");
            stb.append(                "W2.YEAR = '" + param[0] + "' AND ");
            stb.append(                "W2.SEMESTER = ? AND ");
            stb.append(                "W2.SCHREGNO = W1.SCHREGNO AND ");
            stb.append(                "? <= W2.GRADE||W2.HR_CLASS||W2.ATTENDNO AND W2.GRADE||W2.HR_CLASS||W2.ATTENDNO <= ? )S2 ");
            stb.append(   "WHERE S1.SCHREGNO = S2.SCHREGNO AND ");
            stb.append(         "S1.EXECUTEDATE = S2.ATTENDDATE AND ");
            stb.append(         "S1.PERIODCD = S2.PERIODCD)");

            //メイン表
			if( output ){		//遅刻と早退を欠課に換算する処理を行う場合->遅刻、早退の減算は遅刻を優先！
				stb.append(	  "ABSENT,");
				stb.append(   "CASE WHEN (VALUE(LATE,0)-VALUE(ABSENT2,0)*3)<0 THEN ");
				stb.append(   					"0 ELSE (VALUE(LATE,0)-VALUE(ABSENT2,0)*3) END AS LATE,");
				stb.append(   "CASE WHEN (VALUE(LATE,0)-VALUE(ABSENT2,0)*3)<0 THEN ");
				stb.append(						"EARLY+(VALUE(LATE,0)-VALUE(ABSENT2,0)*3) ELSE EARLY END AS EARLY,");
				stb.append(   "VALUE(ABSENT,0)+VALUE(ABSENT2,0)AS ABSENT2 ");
			} else {
				stb.append(	  "ABSENT,LATE,EARLY,0 AS ABSENT2 ");	// 04/09/14Modify
			}
			stb.append("FROM  (SELECT SCHREGNO, ");
				//１日忌引、１日出停以外の忌引、出停は欠席としてカウントする 04/07/13仕様を確認済み
				//忌引、出停は欠席としてカウントしない 04/07/13仕様変更
			//stb.append(		  "SUM(CASE DI_CD WHEN '3' THEN 1 ELSE NULL END)AS MOURNING, ");
			//stb.append(		  "SUM(CASE DI_CD WHEN '2' THEN 1 ELSE NULL END)AS SUSPEND, ");
			stb.append(		  "SUM(CASE WHEN DI_CD IN('4','5','6','14') THEN 1 ELSE NULL END)AS ABSENT, ");
			stb.append(		  "SUM(CASE DI_CD WHEN '15' THEN 1 WHEN '7' THEN 1 ELSE NULL END)AS LATE, ");
			stb.append(		  "SUM(CASE DI_CD WHEN '16' THEN 1 ELSE NULL END)AS EARLY ");
			stb.append("FROM  ATTEND_A W1 ");
			stb.append("WHERE DI_CD IN('4','5','6','14','15','7','16') ");
			stb.append("GROUP BY SCHREGNO ");
			stb.append(		 ")T1 ");

			if (output) {		//遅刻と早退を欠課に換算する処理を行う場合
				stb.append(		 "LEFT JOIN(SELECT SCHREGNO,COUNT(*)/3 AS ABSENT2 ");
				stb.append(				   "FROM ATTEND_A W1 ");
				stb.append(				   "WHERE DI_CD IN('15','7','16')  ");
				stb.append(				   "GROUP BY SCHREGNO ");
				stb.append(		 ")T2 ON T2.SCHREGNO=T1.SCHREGNO ");
			}
		} catch( Exception ex ){
			log.warn("sql-statement error!",ex);
		}
		return stb.toString();

	}//Pre_Stat_4()の括り



	/** 初期化処理 **/
	private void clr_svf(Vrw32alp svf){

	//	ＳＶＦフィールドを初期化
		for( int ib=1 ; ib<3 ; ib++ ){
			for( int ia=1 ; ia<ccimp.svfline+1 ; ia++ ){
				svf.VrsOutn("TOTAL_ABSENCE"+ib	,ia ,"");	//欠席
				svf.VrsOutn("TOTAL_LATE"+ib		,ia ,"");	//遅刻
				svf.VrsOutn("TOTAL_LEAVE"+ib		,ia ,"");	//早退
				svf.VrsOutn("TOTAL_TIME"+ib		,ia ,"");	//時数
			}
		}

		for( int ia=1 ; ia<ccimp.svfline+1 ; ia++ ){
			svf.VrsOutn("HR_NAME"		,ia	,"");		//学年組出席番号
			svf.VrsOutn("NAME_SHOW"	,ia	,"");		//生徒名
		}

		for( int ia=1 ; ia<ccimp.svfline+1 ; ia++ ) svf.VrsOutn("attend", ia, "");


	}//clr_svf()の括り



	/**PrepareStatement close**/
	void Pre_Stat_f()
	{
		try {
			if( ps1 != null )ps1.close();
			if( ps2 != null )ps2.close();
			if( ps3 != null )ps3.close();
			if( ps4 != null )ps4.close();
			if( ps5 != null )ps5.close();
			if( ps6 != null )ps6.close();
		} catch( Exception ex ){
			log.warn("Preparedstatement-close error!",ex);
		}
	}//Pre_Stat_fの括り



	/** SVF-FORM **/
	void Set_Head(DB2UDB db2,Vrw32alp svf){

		int semesdiv = 0;
		StringBuffer stb = new StringBuffer();
		KNJ_Get_Info getinfo = new KNJ_Get_Info();
		KNJ_Get_Info.ReturnVal returnval2 = null;

	//	ＳＶＦフォームの設定->学期制による
		try {
			returnval2 = getinfo.School_Info(db2,param[0]);
			semesdiv = Integer.parseInt(returnval2.val1);				//学期制
		} catch( Exception ex ){
			log.warn("semesterdiv-get error!",ex);
		}
		//log.trace("svfline="+ccimp.svfline);
		if( ccimp.svfline>50 ){
			if( semesdiv>2 )svf.VrSetForm("KNJC051_2.frm", 4);	//３学期制用 ６０行
			else			svf.VrSetForm("KNJC051_1.frm", 4);	//２学期制用 ６０行
		} else {
			//svf.VrSetForm("KNJC051K.frm", 4);						//３学期制用 ５０行
			svf.VrSetForm("KNJC051K_2.frm", 4);						//３学期制用 ５０行 04/10/20Modify
		}
		this.c_chr = new int[semesdiv+1];									//学期別授業時数のインスタンス変数

	//	ＳＶＦ固定項目出力
		svf.VrsOut("nendo",nao_package.KenjaProperties.gengou(Integer.parseInt(param[0]))+"年度");
		//svf.VrsOut("scope_day",KNJ_EditDate.h_format_JP(param[13])+"〜"+KNJ_EditDate.h_format_JP(param[14]));  														//印刷範囲
		svf.VrsOut("scope_day",KNJ_EditDate.h_format_JP(param[13])+" \uFF5E "+KNJ_EditDate.h_format_JP(param[14])); //印刷範囲 04/10/28Modify
		//if( param[7].equals("1") )	svf.VrsOut("PARA_NAME"    , "学期間");
		//else						svf.VrsOut("PARA_NAME"    , "印刷範囲");

	//	作成日(現在処理日)
		try {
			returnval2 = getinfo.Control(db2);
			svf.VrsOut("TODAY",KNJ_EditDate.h_format_JP(returnval2.val3));
		} catch( Exception ex ){
			log.warn("now date-get error!",ex);
		}

	//	学期名称
		try {
			returnval2 = getinfo.Semester(db2,param[0],param[1]);
			svf.VrsOut("SEMESTER",returnval2.val1);
		} catch( Exception ex ){
			log.warn("semester name-get error!",ex);
		}

	//	講座名、科目名
		try {
			returnval2 = Chair_Info(db2,param[0],param[1],param[2]);
			svf.VrsOut("class", returnval2.val1);
			stb.append(returnval2.val2);						//科目名
			//ret = svf.VrsOut("CLASSNAME", returnval2.val2);
		} catch( Exception ex ){
			log.warn("chairname-get error!",ex);
		}

	//	担任名
		try {
			returnval2 = ChairStaff(db2,param[0],param[1],param[2]);
			svf.VrsOut("STAFFNAME", returnval2.val1);
		} catch( Exception ex ){
			log.warn("hr staffname-get error!",ex);
		}

	//	全学期
		try {
			returnval2 = getinfo.Semester_T(db2,param[0]);
			param[6] = returnval2.val1;		//学期コード
			param[8] = returnval2.val3;		//学期開始日
			param[9] = returnval2.val4;		//学期終了日
			StringTokenizer	stsemes = new StringTokenizer(returnval2.val2,",");	//学期名称
			for( int ia=1 ; ia<=semesdiv && stsemes.hasMoreTokens() ; ia++ )
									svf.VrsOut("SEMESTER"+ia, stsemes.nextToken());
		} catch( Exception ex ){
			log.warn("all semester-get error!",ex);
		}

	//	総ページ数
		try {
			ResultSet rs = null;
			int c_sch = 0;						//生徒数
			//int c_chr[] = new int[semesdiv+1];	//学期別授業時数 最後の要素は合計を入れる
			for( int ia=0 ; ia<c_chr.length ; ia++ )c_chr[ia] = 0;
			int t_page = 0;						//総ページ数
			//生徒数取得
			int pi = 0;
			ps5 = db2.prepareStatement(Pre_Stat_1(1));		//生徒情報
			ps5.setString(++pi, param[0]);	//年度
			ps5.setString(++pi, param[1]);	//学期
			ps5.setString(++pi, param[2]);	//講座
			ps5.setString(++pi, param[13]);	//印刷範囲開始日
			ps5.setString(++pi, param[13]);	//印刷範囲開始日
			ps5.setString(++pi, param[14]);	//印刷範囲終了日
			ps5.setString(++pi, param[14]);	//印刷範囲終了日
			ps5.setString(++pi, param[13]);	//印刷範囲開始日
			ps5.setString(++pi, param[14]);	//印刷範囲終了日
			ps5.setString(++pi, param[0]);	//年度
			ps5.setString(++pi, param[4]);	//科目コード
			ps5.setString(++pi, param[0]);	//年度
			ps5.setString(++pi, param[0]);	//年度
			ps5.setString(++pi, param[1]);	//学期
			rs = ps5.executeQuery();
			if( rs.next() ){
				c_sch = rs.getInt(1);			//生徒数
				stb.append(" ( ");
				if( rs.getString("CREDITS") != null ){			// 04/09/28Add
					stb.append(rs.getString("CREDITS"));
                    subclasscredit = rs.getInt("CREDITS");      // 04/10/17Add
				} else
					stb.append(" ");
				stb.append(" 単位 )");
			}
			svf.VrsOut("CLASSNAME", stb.toString());
			stb = null;
			//時数取得
			pi = 0;
			ps6 = db2.prepareStatement(Pre_Stat_2(1));		//科目情報
			ps6.setString(++pi,param[2]);	//講座
			ps6.setString(++pi,param[13]);	//開始日付
			ps6.setString(++pi,param[14]);	//終了日付
			rs = ps6.executeQuery();
			while (rs.next()) {
			    c_chr[rs.getInt("SEMESTER")-1] = rs.getInt("COUNT");
			}
			for( int ia=0 ; ia<c_chr.length-1 ; ia++) {
			    c_chr[semesdiv] += c_chr[ia];
			}
			for( int ia=0 ; ia<c_chr.length-1 ; ia++ ) {
			    this.c_chr[ia] = c_chr[ia];//学期別授業時数をインスタンス変数へ代入
			}
			t_page = (( c_chr[semesdiv]%ccimp.svfline2>0 )? 
							c_chr[semesdiv]/ccimp.svfline2+1 : c_chr[semesdiv]/ccimp.svfline2)
					* (( c_sch%ccimp.svfline>0 )? 
							c_sch/ccimp.svfline+1 : c_sch/ccimp.svfline);
			svf.VrlOut("TOTAL_PAGE", t_page);

		} catch( Exception ex ){
			log.warn("semesterdiv-get error!",ex);
		}

		//ps1 = null;
		//ps2 = null;
		getinfo = null;
		returnval2 = null;

	}//ReturnVal Set_Head()の括り


	/* get parameter doGet()パラメータ受け取り */
    void getParam(HttpServletRequest request){
	    param = new String[18];
		try {
	        param[0] = request.getParameter("YEAR");         	//年度
			param[1] = request.getParameter("SEMESTER");   		//学期
			param[2] = request.getParameter("ATTENDCLASSCD");   //受講コード
			param[3] = request.getParameter("CLASSCD");   	    //教科コード
			param[4] = request.getParameter("SUBCLASSCD");   	//科目コード
			param[5] = request.getParameter("GROUPCD");   	    //群コード
		//	日付型を変換
			KNJ_EditDate editdate = new KNJ_EditDate();							//クラスのインスタンス作成
			param[13] = editdate.h_format_sec(request.getParameter("DATE"));	//印刷範囲開始
			param[14] = editdate.h_format_sec(request.getParameter("DATE2"));	//印刷範囲終了

			//param[7] = request.getParameter("OUTPUT2");   	        			//累計指定
			//param[12] = request.getParameter("GRADE_HR_CLASS");					//対象クラス
			if( request.getParameter("OUTPUT3")!=null )param[15] = "on";		//遅刻を欠課に換算 null:無
		} catch( Exception ex ) {
			log.error("get parameter error!" + ex);
		}
for( int i=0 ; i<param.length ; i++ )log.debug("param["+i+"]="+param[i]);
    }


	/* print設定 */
    private void setSvfInit(HttpServletResponse response ,Vrw32alp svf){
		response.setContentType("application/pdf");
		svf.VrInit();											//クラスの初期化
		try {
			svf.VrSetSpoolFileStream(response.getOutputStream());   	//PDFファイル名の設定
 		} catch( java.io.IOException ex ){
			log.info("db new error:" + ex);
		}
   }


	/* svf close */
    private void closeSvf(Vrw32alp svf){
		if( !nonedata ){
		 	svf.VrSetForm("MES001.frm", 0);
			svf.VrsOut("note" , "");
			svf.VrEndPage();
		}
		svf.VrQuit();
    }


	/* DB set */
	private DB2UDB setDb(HttpServletRequest request)throws ServletException, IOException{
		DB2UDB db2 = null;
		try {
			db2 = new DB2UDB(request.getParameter("DBNAME")	, "db2inst1", "db2inst1", DB2UDB.TYPE2);	//Databaseクラスを継承したクラス
	//		db2 = new DB2UDB("KINH0826"	, "db2inst1", "db2inst1", DB2UDB.TYPE2);	//Databaseクラスを継承したクラス
		} catch( Exception ex ){
			log.info("db new error:" + ex);
			if( db2 != null)db2.close();
		}
		return db2;
	}


	/* DB open */
	private boolean openDb(DB2UDB db2){
		try {
			db2.open();
		} catch( Exception ex ){
			log.error("db open error!"+ex );
			return true;
		}//try-cathの括り
		return false;
	}//private boolean Open_db()


	/* DB close */
	private void closeDb(DB2UDB db2){
		try {
			db2.commit();
			db2.close();
		} catch( Exception ex ){
			log.error("db close error!"+ex );
		}//try-cathの括り
	}//private Close_Db()
	
    /**
     *  講座担任名を取得するメソッド
     *  2005/10/22 yamashiro 正担任を条件に追加
     */
    public ReturnVal ChairStaff(DB2UDB db2,String year,String semester,String chaircd){

        String name = null;         //氏名
        String name_show = null;    //表示氏名
        String name_kana = null;    //カナ氏名
        String name_eng = null;     //英語氏名
        try{
            StringBuffer stb = new StringBuffer();
            stb.append("SELECT  STAFFNAME, STAFFNAME_SHOW, STAFFNAME_KANA, STAFFNAME_ENG ");
            stb.append("FROM    CHAIR_STF_DAT W1, STAFF_MST W2 ");
            stb.append("WHERE   W1.YEAR = '" + year + "' ");
            stb.append(    "AND W1.SEMESTER = '" + semester + "' ");
            stb.append(    "AND W1.CHAIRCD = '" + chaircd + "' ");
            stb.append(    "AND W1.STAFFCD = W2.STAFFCD ");
            stb.append(    "AND W1.CHARGEDIV = 1 ");     //05/10/22
            stb.append("ORDER BY W1.STAFFCD");           //05/10/22
            //System.out.println("[KNJ_Staff]ReturnVal ChairStaff() set_sql sql=" + sql);
            db2.query( stb.toString() );
            ResultSet rs = db2.getResultSet();
            if( rs.next() ){
                name = rs.getString("STAFFNAME");
                name_show = rs.getString("STAFFNAME_SHOW");
                name_kana = rs.getString("STAFFNAME_KANA");
                name_eng = rs.getString("STAFFNAME_ENG");
            }
            db2.commit();
            rs.close();
            //System.out.println("[KNJ_Get_Info]ReturnVal ChairStaff() set_sql sql ok!");
        } catch( Exception ex ){
            System.out.println("[KNJ_Get_Info]ReturnVal ChairStaff() set_sql read error!");
            System.out.println( ex );
        }
        return (new ReturnVal(name,name_show,name_kana,name_eng,null));
    }
    
    /**
     *  講座名、科目名を取得するメソッド 
     *  2005/05/19 yamashiro Modify : 科目略称を追加
     */
    public ReturnVal Chair_Info(DB2UDB db2,String year,String semes,String chaircd){

        String chairname = null;
        String subclassname = null;
        String subclassabbv = null;  //05/05/19Add
        try{
            StringBuffer stb = new StringBuffer();
            stb.append("SELECT CHAIRNAME,SUBCLASSNAME,SUBCLASSABBV FROM CHAIR_DAT W1,SUBCLASS_MST W2 ");  //05/05/19Modify
            stb.append("WHERE  W1.YEAR='"+year+"' AND W1.SEMESTER='"+semes+"' AND ");
            stb.append(       "W1.CHAIRCD='"+chaircd+"' AND W1.SUBCLASSCD=W2.SUBCLASSCD");
            db2.query(stb.toString());
            ResultSet rs = db2.getResultSet();
            if( rs.next() ){
                chairname = rs.getString("CHAIRNAME");
                subclassname = rs.getString("SUBCLASSNAME");
                subclassabbv = rs.getString("SUBCLASSABBV");  //05/05/19Add
            }
            db2.commit();
            rs.close();
            stb = null;
        } catch( Exception ex ){
            System.out.println("[KNJ_Get_Info]ReturnVal Chair_Info set_sql read error!");
            System.out.println( ex );
        }
        return (new ReturnVal(chairname,subclassname,subclassabbv,null,null));  //05/05/19Modify
    }

}//クラスの括り
