// kanji=漢字
/*
 * $Id: 8357b6d8d161381828d3fb1fd8225e3d6ce5a5e6 $
 *
 * 作成日: 2004/03/29
 * 作成者: yamashiro
 *
 * Copyright(C) 2004-2007 ALP Okinawa Co.,Ltd. All rights reserved.
 */
/**
 *
 *	学校教育システム 賢者 [成績管理] テスト結果一覧表（HIRO用）
 *
 *	2004/03/29 yamashiro・RANK関数において０による除算による不具合を修正
 *  2004/10/30 yamashiro・TESTITEMCDを指示画面からNULLで受け取った場合プログラムにおいて'01'とする
 *  2005/06/22 yamasihro・共通化のための調整
 */

package servletpack.KNJD;

import java.io.IOException;
import java.util.StringTokenizer;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJD.detail.KNJ_Testname;
import servletpack.KNJZ.detail.KNJServletpacksvfANDdb2;
import servletpack.KNJZ.detail.KNJ_ChairClass;
import servletpack.KNJZ.detail.KNJ_Control;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_Semester;


public class KNJD050 {

    private static final Log log = LogFactory.getLog(KNJD050.class);


	/**
	 *
	 *  KNJD.classから最初に起動されるクラス
	 *
	 */
	public void svf_out(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		Vrw32alp svf = new Vrw32alp(); 		//PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
		DB2UDB db2 = null;					//Databaseクラスを継承したクラス
		boolean nonedata = false; 			//該当データなしフラグ
		KNJServletpacksvfANDdb2 sd = new KNJServletpacksvfANDdb2();    //帳票におけるＳＶＦおよびＤＢ２の設定

	// ＤＢ接続
		db2 = sd.setDb( request );
		if( sd.openDb( db2 ) ){
			log.error("db open error");
			return;
		}

	// パラメータの取得
	    Param param = getParam( db2, request);

	// print svf設定
		sd.setSvfInit( request, response, svf);

	// 印刷処理
		nonedata = printSvf( db2, svf, param );

	// 終了処理
		sd.closeSvf( svf, nonedata );
		sd.closeDb( db2 );
	}


    /**
     *  印刷処理
     */
    private boolean printSvf( DB2UDB db2, Vrw32alp svf, Param param){

		boolean nonedata = false; 			//該当データなしフラグ
		PreparedStatement ps1 = null;
		try {
			ps1 = db2.prepareStatement( Pre_Stat( param.toArray() ) );
			Set_Head( db2, svf, param );												//見出し出力のメソッド
			if( param._12.equals("1") ) nonedata = Out_Hrclass( db2, svf, param, ps1 );	//クラス別出力
			else						nonedata = Out_Total( db2, svf, param, ps1 );	//総合出力
		} catch( Exception ex ){
			log.error("error! ",ex);
		}
		return nonedata;
    }


	/**
     *  印刷処理 クラス別
     */
	private boolean Out_Hrclass( DB2UDB db2, Vrw32alp svf, Param param, PreparedStatement ps1 )
	{
		boolean nonedata = false; 			//該当データなしフラグ
		try {
			StringTokenizer stz1 = new StringTokenizer( param._13, ",", false);	//対象クラス
			StringTokenizer stz2 = new StringTokenizer( param._14, ",", false);	//対象クラス別名
			while (stz1.hasMoreTokens()){
				param._6 = stz1.nextToken();									//対象クラス
				param._15 = param._6;
				if(stz2.hasMoreTokens())	param._8 = stz2.nextToken();		//対象クラス別名
	            param.logDebug(log);
				if( Set_Detail_1( svf, param, ps1 ) ) nonedata = true;			//明細出力のメソッド
			}
		} catch( Exception ex ){
			log.error("error! ",ex);
		}
		return nonedata;
	}



	/**
     *  印刷処理 総合
     */
	private boolean Out_Total( DB2UDB db2, Vrw32alp svf, Param param, PreparedStatement ps1 )
	{
		boolean nonedata = false; 			//該当データなしフラグ
		try {
		    param.logDebug(log);
			if( Set_Detail_1( svf, param, ps1 ) ) nonedata = true;				//明細出力のメソッド
		} catch( Exception ex ){
			log.error("error! ",ex);
		}
		return nonedata;
	}



	/**
     *  SVF-FORM 見出し等の設定および出力
     */
	private void Set_Head( DB2UDB db2, Vrw32alp svf, Param param){

		svf.VrSetForm("KNJD050.frm", 4);

		svf.VrsOut("ymd2"			,KNJ_EditDate.h_format_JP(param._10));				//実施日
		svf.VrsOut("nendo"		,nao_package.KenjaProperties.gengou
													(Integer.parseInt(param._0)) + "年度");	//年度
		svf.VrsOut("class3" 		,param._9);											//統計対象クラス別名
		if( param._11.equals("1") )		svf.VrsOut("output"	,"番号順");
		else							svf.VrsOut("output"	,"席次順");

	//	ＳＶＦ属性変更--->出力形式がクラス別の場合クラス毎に改ページ
		if( param._12.equals("1") )	svf.VrAttribute("ATTENDCLASSCD","FF=1");

	//	作成日(現在処理日)の取得
		try {
			KNJ_Control control = new KNJ_Control();
			KNJ_Control.ReturnVal returnval = control.Control(db2);
			svf.VrsOut("ymd1"		,KNJ_EditDate.h_format_JP(returnval.val3));			//作成日
		} catch( Exception e ){
			log.debug("Set_Head() ctrl_date get error! ", e );
		}
	//	学期名称の取得
		try {
			KNJ_Semester semester = new KNJ_Semester();
			KNJ_Semester.ReturnVal returnval = 
						semester.Semester(db2,param._0,param._1);
			svf.VrsOut("term"			,returnval.val1);				//学期名称
		} catch( Exception e ){
			log.debug("Set_Head() Semester name get error! ", e );
		}

        setTestname( db2, svf, param.toArray() );   //テスト名称設定 04/10/30Modify
	}


	/** 
     *  テスト名称設定
     *  04/10/30Build
     */
	void setTestname( DB2UDB db2, Vrw32alp svf, String aparam[] )
	{
	    final Param param = Param.set(aparam);
		try {
			KNJ_Testname test = new KNJ_Testname();
			KNJ_Testname.ReturnVal returnval = test.TestName(db2,param._4,param._5,param._3,param._0, param._18);
			svf.VrsOut("test1"			,returnval.val2);			//テスト名称
			svf.VrsOut("subject1"			,returnval.val3);			//科目名称
		} catch( Exception e ){
			log.debug("Set_Head() test name get error! ", e );
		}
    }


	/**
     *  SVF-FORM 印刷処理 明細データ出力
     */
	private boolean Set_Detail_1( Vrw32alp svf, Param param, PreparedStatement ps1 )
	{
		boolean nonedata = false;
		try {
			prestatSetParameter( param.toArray(), ps1 );       //preparedstatementパラメーター・マーカーのセット
			ResultSet rs = ps1.executeQuery();

			int s_flg=0;
			int number=0;
			while( rs.next() ){
				if(s_flg == 0){
					svf.VrsOut("average1" 		,rs.getString("HR_AVG"));		//学級平均
					svf.VrsOut("number1"  		,rs.getString("HR_CNT"));		//学級人数
					svf.VrsOut("average2" 		,rs.getString("SC_AVG"));		//校内平均
					svf.VrsOut("number2"  		,rs.getString("SC_CNT"));		//校内人数
					svf.VrsOut("class1" 			,param._8);						//クラス別名
					svf.VrsOut("ATTENDCLASSCD" 	,param._15);					//講座コード
					s_flg++;
				}
				number++;
				svf.VrsOut("number3"	,String.valueOf(number));				//番号
				svf.VrsOut("class2"	,rs.getString("G_H_ATTENDNO"));     	//学年・組・番号
				svf.VrsOut("name1"	,rs.getString("NAME"));    				//名前
				if(rs.getString("ATTEND_FLG").equalsIgnoreCase("1")){
					svf.VrsOut("score2"	," ");     							//未受験
					svf.VrsOut("score"	,rs.getString("HR_MOD_SCORE"));		//得点
					svf.VrsOut("sekiji2"	,rs.getString("HR_RANK"));			//学級席次
					svf.VrsOut("value2"	,rs.getString("HR_HENSA"));    		//学級偏差値
					svf.VrsOut("sekiji1" 	,rs.getString("SC_RANK")); 			//校内席次
					svf.VrsOut("value1"  	,rs.getString("SC_HENSA"));			//校内偏差値
				} else{
					svf.VrsOut("score2"	,"未受験");     					//未受験
					svf.VrsOut("score"	," ");    							//得点
					svf.VrsOut("sekiji2"	," ");								//学級席次
					svf.VrsOut("value2"	," ");    							//学級偏差値
					svf.VrsOut("sekiji1" 	," "); 								//校内席次
					svf.VrsOut("value1"  	," ");								//校内偏差値
				}
		 		svf.VrEndRecord();
				nonedata = true ; //該当データなしフラグ
			}
			rs.close();
		} catch( Exception e ){
			log.debug("Set_Detail_1() read error! ", e );
		}
		return nonedata;
	}


	/** 
     *  preparedstatementパラメーター・マーカーのセット
     */
	void prestatSetParameter( String aparam[], PreparedStatement ps1 )
	{
	    final Param param = Param.set(aparam);
		try {
			if( param._12.equals("1") ){
				int pp = 0;
				ps1.setString( ++pp, param._6 );	//対象講座コード
				ps1.setString( ++pp, param._6 );	//対象講座コード
				ps1.setString( ++pp, param._6 );	//対象講座コード
			}
		} catch( Exception e ){
			log.debug("prestatSetParameter error!", e );
		}
    }


	/**
     *  PrepareStatement作成 明細データ
     */
	String Pre_Stat( String aparam[] )
	{
	    final Param param = Param.set(aparam);
		StringBuffer stb = new StringBuffer();
		try {
					//校内の評価平均・偏差値・カウントの共通表
			stb.append("WITH HENSA1 AS("
							+ "SELECT ROUND(AVG(FLOAT(T1.SCORE))*100,0)/100 AS S_AVG,"
								   + "ROUND(STDDEV(FLOAT(T1.SCORE))*100,0)/100 AS S_HENSA,"
								   + "COUNT(T1.SCHREGNO) AS S_CNT "
							+ "FROM   TESTSCORE_DAT T1 "
							+ "WHERE  T1.YEAR='"+param._0+"' AND T1.SEMESTER='"+param._1+"' AND "
                                   + ("1".equals(param._18) ? " T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || " : "")
								   + "T1.SUBCLASSCD='"+param._3+"' AND T1.TESTKINDCD='"+param._4+"' AND "
								   + "T1.TESTITEMCD='"+param._5+"' AND T1.ATTEND_FLG='1' AND "
								   + "T1.CHAIRCD IN "+param._7+"),");

					//学級の評価平均・偏差値・カウントの共通表
			stb.append("HENSA2 AS("
							+ "SELECT ROUND(AVG(FLOAT(T1.SCORE))*100,0)/100 AS S_AVG,"
								   + "ROUND(STDDEV(FLOAT(T1.SCORE))*100,0)/100 AS S_HENSA,"
								   + "COUNT(T1.SCHREGNO) AS S_CNT "
							+ "FROM   TESTSCORE_DAT T1 "
							+ "WHERE  T1.YEAR='"+param._0+"' AND T1.SEMESTER='"+param._1+"' AND "
                                   + ("1".equals(param._18) ? " T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || " : "")
								   + "T1.SUBCLASSCD='"+param._3+"' AND T1.TESTKINDCD='"+param._4+"' AND "
								   + "T1.TESTITEMCD='"+param._5+"' AND T1.ATTEND_FLG='1' AND ");
			if( param._12.equals("1") ) {
				stb.append(		     "T1.CHAIRCD =?) ");
			} else {
				stb.append(		     "T1.CHAIRCD IN "+param._6+")");
			}
				//メイン照会表
			stb.append("SELECT T3.GRADE,T3.HR_CLASS,T3.ATTENDNO,"
							+ "W3.HR_NAMEABBV||'-'||CHAR(INT(T3.ATTENDNO))AS G_H_ATTENDNO,T2.NAME,"
							+ "VALUE(T4.SC_MOD_SCORE,0) AS SC_MOD_SCORE,"
							+ "T4.SC_HENSA,T4.SC_RANK,T4.SC_AVG,T4.SC_CNT,"
							+ "VALUE(T5.HR_MOD_SCORE,0) AS HR_MOD_SCORE,"
							+ "T5.HR_HENSA,T5.HR_RANK,T5.HR_AVG,T5.HR_CNT,T1.ATTEND_FLG "
				     + "FROM   TESTSCORE_DAT T1 "
							+ "INNER JOIN SCHREG_BASE_MST T2 ON T1.SCHREGNO=T2.SCHREGNO "
							+ "INNER JOIN SCHREG_REGD_DAT T3 ON T3.YEAR='"+param._0+"' AND "
												+ "T3.SEMESTER='"+param._1+"' AND T3.SCHREGNO=T1.SCHREGNO "
							+ "INNER JOIN SCHREG_REGD_HDAT W3 ON W3.YEAR='"+param._0+"' AND "
												+ "W3.SEMESTER='"+param._1+"' AND "
												+ "W3.GRADE=T3.GRADE AND W3.HR_CLASS=T3.HR_CLASS "
					//校内の順位
					+ "LEFT JOIN ("
								+ "SELECT "
									+ "T1.SCHREGNO,"
									+ "T1.SCORE AS SC_MOD_SCORE,"
									+ "CASE S_HENSA WHEN 0 THEN 0 "
										+ "ELSE DECIMAL(ROUND((T1.SCORE-S_AVG)/S_HENSA*10*10,0)/10+50,5,1) END AS SC_HENSA,"
									+ "CASE S_HENSA WHEN 0 THEN 0 "
										+ "ELSE RANK() OVER("
											+ "ORDER BY ROUND((T1.SCORE-S_AVG)/case S_HENSA when 0 then 1 else S_HENSA end *10*10,0)/10+50 DESC) END AS SC_RANK,"
							   		+ "T1.CHAIRCD,"
									+ "DECIMAL(ROUND(T2.S_AVG,1),5,1) AS SC_AVG,"
									+ "T2.S_CNT AS SC_CNT "
								+ "FROM "
									+ "TESTSCORE_DAT T1,"
									+ "HENSA1 T2 "
								+ "WHERE "
										+ "T1.YEAR = '" + param._0 + "' "
									+ "AND T1.SEMESTER = '" + param._1 + "' "
									+ "AND "
	                                + ("1".equals(param._18) ? " T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || " : "")
									+ "    T1.SUBCLASSCD = '" + param._3 + "' "
									+ "AND T1.TESTKINDCD = '" + param._4 + "' "
									+ "AND T1.TESTITEMCD = '" + param._5 + "' "
									+ "AND T1.ATTEND_FLG = '1' "
									+ "AND T1.CHAIRCD IN " + param._7
					+ ") T4 ON (T1.SCHREGNO = T4.SCHREGNO)"
					//学級の順位
					+ "LEFT JOIN ("
								+ "SELECT "
									+ "T1.SCHREGNO,"
									+ "T1.SCORE AS HR_MOD_SCORE,"
									+ "CASE S_HENSA WHEN 0 THEN 0 "
										+ "ELSE DECIMAL(ROUND((T1.SCORE-S_AVG)/S_HENSA*10*10,0)/10+50,5,1) END AS HR_HENSA,"
									+ "CASE S_HENSA WHEN 0 THEN 0 "
										+ "ELSE RANK() OVER("
											+ "ORDER BY ROUND((T1.SCORE-S_AVG)/case S_HENSA when 0 then 1 else S_HENSA end *10*10,0)/10+50 DESC) END AS HR_RANK,"
							   		+ "T1.CHAIRCD,"
									+ "DECIMAL(ROUND(T2.S_AVG,1),5,1) AS HR_AVG,"
									+ "T2.S_CNT AS HR_CNT "
								+ "FROM "
									+ "TESTSCORE_DAT T1,"
									+ "HENSA2 T2 "
								+ "WHERE "
										+ "T1.YEAR = '" + param._0 + "' "
	                                + "AND T1.SEMESTER = '" + param._1 + "' "
									+ "AND "
	                                + ("1".equals(param._18) ? " T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || " : "")
									+ "    T1.SUBCLASSCD = '" + param._3 + "' "
									+ "AND T1.TESTKINDCD = '" + param._4 + "' "
									+ "AND T1.TESTITEMCD = '" + param._5 + "' "
									+ "AND T1.ATTEND_FLG = '1' ");
			if( param._12.equals("1") ) {
				stb.append(			  "AND T1.CHAIRCD =? ");
			} else {
				stb.append(			  "AND T1.CHAIRCD IN " + param._6);
			}
			stb.append(") T5 ON (T1.SCHREGNO = T5.SCHREGNO)");
			stb.append(	   "WHERE  T1.YEAR='"+param._0+"' AND T1.SEMESTER='"+param._1+"' AND "
                                + ("1".equals(param._18) ? " T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || " : "")
					            + "T1.SUBCLASSCD='"+param._3+"' AND T1.TESTKINDCD='"+param._4+"' AND "
					            + "T1.TESTITEMCD='"+param._5+"' AND ");
			if( param._12.equals("1") ) {
				stb.append(	      "T1.CHAIRCD =? ");
			} else {
				stb.append(	      "T1.CHAIRCD IN "+param._6+" ");
			}
			if( param._11.equals("2") ) {
				stb.append("ORDER BY T4.SC_RANK,T3.GRADE,T3.HR_CLASS,T3.ATTENDNO,T4.SC_HENSA");
			} else {
				stb.append("ORDER BY T3.GRADE,T3.HR_CLASS,T3.ATTENDNO,T4.SC_HENSA");
			}

		} catch( Exception e ){
			log.debug("Pre_Stat() error! ", e );
		}
		return stb.toString();

	}//Pre_Stat()の括り


	/**
     *  PrepareStatement close
     **/
	private void Pre_Stat_F( PreparedStatement ps1 )
	{
		try {
			ps1.close();
		} catch( Exception e ){
			log.debug("Pre_Stat_F() error! ", e );
		}
	}


	/** 
     *  get parameter doGet()パラメータ受け取り 
	 */
    private Param getParam(  DB2UDB db2, HttpServletRequest request){

        final Param param = new Param();
		try {
		    //printname = request.getParameter("PRINTNAME");   			    //プリンタ名
	        param._0 = request.getParameter("YEAR");         	//年度
			param._1 = request.getParameter("SEMESTER");   		//学期
			//param._2 = request.getParameter("CLASSCD");         //教科
			param._3 = request.getParameter("SUBCLASSCD");    	//科目
			param._4 = request.getParameter("TESTKINDCD") != null ? request.getParameter("TESTKINDCD").substring(0, 2) : "";    //試験種類
            param._5 = request.getParameter("TESTKINDCD") != null ? request.getParameter("TESTKINDCD").substring(2) : "";    //試験種別
			param._10 = request.getParameter("STARTDAY");      	//実施日
			param._11 = request.getParameter("OUT1");			//出力順
			param._12 = request.getParameter("OUT2");			//出力形式 1:クラス別 2:総合
            param._17 = request.getParameter("COUNTFLG");
            param._18 = request.getParameter("useCurriculumcd");

			//出力対象受講クラスコードの編集
			if( param._12.equals("1") ){
				//出力形式がクラス別の場合は、後でトークンで区切って処理！
				param._13 = request.getParameter("ATTENDCLASSCD1");
			} else{
				//出力形式が総合の場合、出力対象受講クラスコードをＳＱＬ用に編集
				int ia=0;
				StringTokenizer stz = new StringTokenizer(request.getParameter("ATTENDCLASSCD1"),",",false);
				while (stz.hasMoreTokens()){
					ia++;
					String strz = stz.nextToken();
					if(ia==1){
						param._6 = "('" + strz;										//対象クラス
						param._15 = strz;											//対象クラス表示用
					} else{
						param._6 = param._6 + "','" + strz;							//対象クラス
						if(ia<4)	param._15 = param._15 + "," + strz;				//対象クラス表示用
					}
				}
				param._6 = param._6 + "')";
				if(ia>3)	param._15 = param._15 + "他" + (ia-3);
			}
		} catch( Exception ex ) {
			log.error("request.getParameter error!",ex);
		}

	//  統計対象受講クラスコードをＳＱＬ用に編集
		try {
			String strx = request.getParameter("ATTENDCLASSCD1");
			if(request.getParameter("ATTENDCLASSCD1").length() > 0)
				strx = strx + "," + request.getParameter("ATTENDCLASSCD2");
			StringTokenizer stz = new StringTokenizer(strx,",",false);
			if(stz.hasMoreTokens())	param._7 = "('" + stz.nextToken();
			while (stz.hasMoreTokens())	param._7 = param._7 + "','" + stz.nextToken();
			param._7 = param._7 + "')";										//統計クラス
		} catch( Exception e ){
			log.debug("Set_Head() test name get error! ", e );
		}

	// 	クラス別名の編集
		try {
			KNJ_ChairClass chairclass = new KNJ_ChairClass();
			KNJ_ChairClass.ReturnVal returnval = chairclass.ChairClass(db2,param._0,param._1,
								request.getParameter("ATTENDCLASSCD1"),request.getParameter("ATTENDCLASSCD2"));
			if( param._12.equals("1") )		param._14 = returnval.val1;			//対象クラス別名
			else							param._8 = returnval.val1;			//対象クラス別名
			param._9 = returnval.val2;											//統計対象クラス別名
		} catch( Exception e ){
			log.debug("trgeclass error! ", e );
		}
		return param;
    }
    
    private static class Param {
        String _0;
        String _1;
        String _2;
        String _3;
        String _4;
        String _5;
        String _6;
        String _7;
        String _8;
        String _9;
        String _10;
        String _11;
        String _12;
        String _13;
        String _14;
        String _15;
        String _16;
        String _17;
        String _18;
        
        static Param set(String[] array) {
            final Param param = new Param();
            param._0 = array[0];
            param._1 = array[1];
            param._2 = array[2];
            param._3 = array[3];
            param._4 = array[4];
            param._5 = array[5];
            param._6 = array[6];
            param._7 = array[7];
            param._8 = array[8];
            param._9 = array[9];
            param._10 = array[10];
            param._11 = array[11];
            param._12 = array[12];
            param._13 = array[13];
            param._14 = array[14];
            param._15 = array[15];
            param._16 = array[16];
            param._17 = array[17];
            param._18 = array[18];
            return param;
        }
        
        String[] toArray() {
            return new String[] {_0,_1,_2,_3,_4,_5,_6,_7,_8,_9,_10,_11,_12,_13,_14,_15,_16,_17,_18};
        }
        
        void logDebug(Log log) {
            final String[] param = toArray();
            for(int i = 0 ; i < param.length ; i++ )log.debug("param[" + i + "] = " + param[i] );
        }
    }


}
