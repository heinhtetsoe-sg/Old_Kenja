package servletpack.KNJL;

import java.io.IOException;
import java.io.PrintWriter;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJ_Get_Info;

/**
 *
 *	学校教育システム 賢者 [入試管理]
 *
 *					＜ＫＮＪＬ３１５Ｈ＞  入学試験得点分布表
 *
 *	2007/11/19 RTS 作成日
 */

public class KNJL315H {


    private static final Log log = LogFactory.getLog(KNJL315H.class);
    private String param[];
    private StringBuffer stb;
    boolean nonedata = false;

    //*-------------------------------------------------* 
    // 試験科目データテーブルの対象年度、入試制度に     *
    // 紐つく名称マスタデータ(L009)を格納。             *
    // 構成:キー⇒科目見出しエリア出力順,値=NAMECD2     *
    //*-------------------------------------------------* 
	HashMap hkamoku = new HashMap();

	/*----------------------------*
	 * HTTP Get リクエストの処理  *
	 *----------------------------*/
	public void svf_out(HttpServletRequest request, HttpServletResponse response)
	                 throws ServletException, IOException
	{

		Vrw32alp svf = new Vrw32alp(); 			//PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
		DB2UDB db2 = null;						//Databaseクラスを継承したクラス
		param = new String[5];

        log.fatal("$Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $"); // CVSキーワードの取り扱いに注意
		// パラメータの取得
		try {
	        param[0] = request.getParameter("YEAR");         			//年度
			param[1] = request.getParameter("APDIV");   				//入試制度 1:一般（中学）2:(高校一般) 3:(高校推薦)
			param[2] = request.getParameter("TESTDV");   				//入試区分 1:第１回,2:第２回,3:第３回
			param[3] = request.getParameter("CHECK1");   				//受験者を除くフラグ
			param[4] = request.getParameter("TEXT1");   				//XXX点
		} catch( Exception ex ) {
			log.warn("parameter error!",ex);
		}

		// print設定
		PrintWriter outstrm = new PrintWriter (response.getOutputStream());
		response.setContentType("application/pdf");

		// svf設定
		int ret = svf.VrInit();						   	//クラスの初期化
        if (false && 0 != ret) { ret = 0; }
		ret = svf.VrSetSpoolFileStream(response.getOutputStream());   		//PDFファイル名の設定

		// ＤＢ接続
		try {
			db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
			db2.open();
		} catch( Exception ex ) {
			log.error("DB2 open error!",ex);
			return;
		}


		// ＳＶＦ作成処理
		boolean nonedata = false; 						//該当データなしフラグ

		getHeaderData(db2,svf);							//ヘッダーデータ抽出メソッド

		for(int ia=0 ; ia<param.length ; ia++) log.debug("param["+ia+"]="+param[ia]);

		//SVF出力
	    if( printMain(db2,svf) ) nonedata = true;
	    log.debug("nonedata="+nonedata);

	    // 該当データ無し
		if( !nonedata ){
			ret = svf.VrSetForm("MES001.frm", 0);
			ret = svf.VrsOut("note" , "note");
			ret = svf.VrEndPage();
		}

		// 終了処理
		ret = svf.VrQuit();
		db2.commit();
		db2.close();				//DBを閉じる
		outstrm.close();			//ストリームを閉じる 

    }//doGetの括り


	/**ヘッダーデータを抽出*/
	private void getHeaderData(DB2UDB db2,Vrw32alp svf){

		int ret = 0;
        if (false && 0 != ret) { ret = 0; }

        // フォーム
   		ret = svf.VrSetForm("KNJL315H.frm", 1);

   		// 年度
		String sNENDO = convZenkakuToHankaku(param[0]);
		ret = svf.VrsOut("NENDO"  , sNENDO + "年度" );

   		// 入試区分
		try {
			String sql ="";
			if(param[1].equals("1")){
				sql = "SELECT NAME1 AS TEST_NAME FROM NAME_MST WHERE NAMECD1='L004' AND NAMECD2='"+param[2]+"' ";
			} else {
				sql = "SELECT ABBV1 AS TEST_NAME FROM NAME_MST WHERE NAMECD1='L003' AND NAMECD2='"+param[1]+"' ";
			}
			db2.query(sql);
			ResultSet rs = db2.getResultSet();
			while( rs.next() ){
    			ret = svf.VrsOut("TESTDIV"  , rs.getString("TEST_NAME") );
			}
			db2.commit();
		} catch( Exception e ){
			log.warn("testname get error!",e);
		}

		// 作成日
		try {
            KNJ_Get_Info getinfo = new KNJ_Get_Info();		//各情報取得用のクラス
            KNJ_Get_Info.ReturnVal returnval = null;		//各情報を返すためのクラス
			returnval = getinfo.Control(db2);
			ret = svf.VrsOut("DATE" , fomatSakuseiDate(returnval.val3));
    		getinfo = null;
    		returnval = null;
		} catch( Exception e ){
			log.warn("ctrl_date get error!",e);
		}


	}//getHeaderData()の括り


	/**印刷処理メイン*/
	private boolean printMain(DB2UDB db2,Vrw32alp svf)
	{

		try {
        	//明細データ
       		if( printSubclassScore(db2,svf) ){	// 統計表
                printTotalScore(db2,svf);		// 得点分布表
       			svf.VrEndPage();
                nonedata = true;
            }
		} catch( Exception ex ) {
			log.warn("printMain read error!",ex);
		}

		return nonedata;

	}//printMain()の括り

	/**明細データ印刷処理(表：各教科)*/
	private boolean printSubclassScore(DB2UDB db2,Vrw32alp svf)
	{
		int ret = 0;
		String sex_Type = "";
		int area_col = 0;
		if (false && 0 != ret) { ret = 0; }
		try {
		    //指示画面より指定された試験科目の取得
        	String retsql = getSubClass();
		    //指示画面より指定された試験科目の設定
        	setSubClass(db2, svf, retsql);
    		int kamoku_cnt = hkamoku.size();
        	
	        //*----------------------------*
	        //*  各科目欄の編集・出力      *
	        //*----------------------------*
			for(int j=0;j<hkamoku.size();j++){
				//各科目『最高点、最低点、平均点』を設定
				setkekkahyo(db2, svf, (String)hkamoku.get(String.valueOf(j+1)), j+1 );
			}
        	
			// 入試制度が高校一般の場合、加算点エリアの出力を行う
			if(param[1].equals("2")){
				++kamoku_cnt;
				setkekkahyo_kaasn(db2, svf, kamoku_cnt );
			}
	        //*----------------------------*
	        //*  受験者数欄の編集・出力    *
	        //*----------------------------*
			//男子『受験者数欄』を設定
			sex_Type = "1";
			area_col = 1;
			++kamoku_cnt;
			String sSubClass_Count1 = setkekkahyoTotal(db2, svf, sex_Type, area_col, kamoku_cnt);
			//志願者データより欠席者数を取得
			if(sSubClass_Count1 != null){
				setkekkahyoTotal_absent(db2, svf, sSubClass_Count1, sex_Type, area_col);
			}
			//女子『受験者数欄』を設定
			sex_Type = "2";
			area_col = 2;
			String sSubClass_Count2 = setkekkahyoTotal(db2, svf, sex_Type, area_col, kamoku_cnt);
			//志願者データより欠席者数を取得
			if(sSubClass_Count2 != null){
				setkekkahyoTotal_absent(db2, svf, sSubClass_Count2, sex_Type, area_col);
			}
			//全体『受験者数欄』を設定
			sex_Type = "";
			area_col = 3;
			String sSubClass_Count3 = setkekkahyoTotal(db2, svf, sex_Type, area_col, kamoku_cnt);
			//志願者データより欠席者数を取得
			if(sSubClass_Count3 != null){
				setkekkahyoTotal_absent(db2, svf, sSubClass_Count3, sex_Type, area_col);
			}
        	nonedata = true;
        		
		} catch( Exception ex ){
			log.error("printSubclassScore error!",ex);
		}
		return nonedata;

	}//printSubclassScore()の括り

	/**
     *  svf print 統計表：総合欄、受験者数欄データ出力
     */
    private String setkekkahyoTotal(DB2UDB db2, Vrw32alp svf, String selSex, int icol, int total_col)
	{
    	String retTotal_Cnt = null;
    	PreparedStatement ps = null;
    	ResultSet rs = null;
		try{
	    	ps = db2.prepareStatement( getKekkahyo_Total(selSex) );
			rs = ps.executeQuery();
			while( rs.next() ){
				//統計表出力(総合欄)
	            svf.VrsOutn( "HIGH"    + total_col,	icol,	nvlT(rs.getString("MAXSCORE")) );	// 最高点
	            svf.VrsOutn( "LOW"     + total_col,	icol,	nvlT(rs.getString("MINSCORE")) );	// 最低点
	            svf.VrsOutn( "AVERAGE" + total_col, icol,	nvlT(rs.getString("AVERAGE")) );	// 平均点
				//統計表出力(受験者数欄)
	            svf.VrsOutn( "EXAM_COUNT",  icol,	nvlT(rs.getString("TOTAL_CNT")) );			// 受験者数
	            retTotal_Cnt = nvlT(rs.getString("TOTAL_CNT"));
	            nonedata = true;
			}
		} catch( Exception ex ){
			log.error("setkekkahyo error!",ex);
    	} finally {
    		db2.commit();
    		DbUtils.closeQuietly(null, ps, rs);
    	}
		return retTotal_Cnt;
	}

	/**
     *  svf print 統計表：受験者数欄の欠席者数を設定
     */
    private void setkekkahyoTotal_absent(DB2UDB db2, Vrw32alp svf, String sTotal_Count, String sex_type ,int icol)
	{
    	PreparedStatement ps = null;
    	ResultSet rs = null;
    	int iAbsent_Cnt = 0;
    	int iDesire_Cnt = 0;
    	int iSubclass_Cnt = 0;
    	//*-----------------------------*
    	//* 『欠席者数』の取得・出力    *
    	//*-----------------------------*
		try{
			
			//志願者データより志願者取得
	    	ps = db2.prepareStatement( getKekkahyo_absent(sex_type) );
			rs = ps.executeQuery();
            if( rs.next() ){
            	iDesire_Cnt = rs.getInt("DESIRE_CNT");
                // 欠席者数を算出
                iSubclass_Cnt = Integer.valueOf(sTotal_Count).intValue();
                // 欠席者数を算出する：志願者数-受験者数
                iAbsent_Cnt = iDesire_Cnt - iSubclass_Cnt;
    			//受験者数欄出力
                svf.VrsOutn( "ABSENCE_COUNT", icol,	String.valueOf(iAbsent_Cnt) );	// 欠席者数
            }
			
		} catch( Exception ex ){
			log.error("setkekkahyo_absent error!",ex);
		} finally{
			if( rs != null )try{rs.close();}catch( SQLException ey ){log.error("rs.close error!" + ey );}
       		db2.commit();
		}
	}
    
	/**
     *  svf print 統計表：加算数欄データ出力
     */
    private void setkekkahyo_kaasn(DB2UDB db2, Vrw32alp svf, int icol)
	{
    	PreparedStatement ps1 = null;
    	PreparedStatement ps2 = null;
    	PreparedStatement ps3 = null;
    	ResultSet rs1 = null;
    	ResultSet rs2 = null;
    	ResultSet rs3 = null;
		try{
			// 男子
	    	ps1 = db2.prepareStatement( getKekkahyo_kasan("1") );
			rs1 = ps1.executeQuery();
			while( rs1.next() ){
				//統計表出力
	            svf.VrsOutn( "HIGH" + icol,		1,	nvlT(rs1.getString("MAXSCORE")) );	// 最高点
	            svf.VrsOutn( "LOW" + icol,		1,	nvlT(rs1.getString("MINSCORE")) );	// 最低点
	            svf.VrsOutn( "AVERAGE" + icol,  1,	nvlT(rs1.getString("AVERAGE")) );	// 平均点
			}
			// 女子
	    	ps2 = db2.prepareStatement( getKekkahyo_kasan("2") );
			rs2 = ps2.executeQuery();
			while( rs2.next() ){
				//統計表出力
	            svf.VrsOutn( "HIGH" + icol,		2,	nvlT(rs2.getString("MAXSCORE")) );	// 最高点
	            svf.VrsOutn( "LOW" + icol,		2,	nvlT(rs2.getString("MINSCORE")) );	// 最低点
	            svf.VrsOutn( "AVERAGE" + icol,  2,	nvlT(rs2.getString("AVERAGE")) );	// 平均点
			}
			// 全体
	    	ps3 = db2.prepareStatement( getKekkahyo_kasan("") );
			rs3 = ps3.executeQuery();
			while( rs3.next() ){
				//統計表出力
	            svf.VrsOutn( "HIGH" + icol,		3,	nvlT(rs3.getString("MAXSCORE")) );	// 最高点
	            svf.VrsOutn( "LOW" + icol,		3,	nvlT(rs3.getString("MINSCORE")) );	// 最低点
	            svf.VrsOutn( "AVERAGE" + icol,  3,	nvlT(rs3.getString("AVERAGE")) );	// 平均点
			}
		} catch( Exception ex ){
			log.error("setkekkahyo_kaasn error!",ex);
    	} finally {
    		db2.commit();
    		DbUtils.closeQuietly(null, ps1, rs1);
    		DbUtils.closeQuietly(null, ps2, rs2);
    		DbUtils.closeQuietly(null, ps3, rs3);
    	}
	}
	
	/**
     *  svf print 統計表：各科目エリアデータ出力
     */
    private void setkekkahyo(DB2UDB db2, Vrw32alp svf, String sTestSubClassCd, int icol)
	{
    	PreparedStatement ps1 = null;
    	PreparedStatement ps2 = null;
    	PreparedStatement ps3 = null;
    	ResultSet rs1 = null;
    	ResultSet rs2 = null;
    	ResultSet rs3 = null;
		try{
			// 各教科男子
			ps1 = db2.prepareStatement( getKekkahyo(sTestSubClassCd, "1") );
			rs1 = ps1.executeQuery();
			while( rs1.next() ){
				//統計表出力
	            svf.VrsOutn( "HIGH" + icol,		1,	nvlT(rs1.getString("MAXSCORE")) );	// 最高点
	            svf.VrsOutn( "LOW" + icol,		1,	nvlT(rs1.getString("MINSCORE")) );	// 最低点
	            svf.VrsOutn( "AVERAGE" + icol,  1,	nvlT(rs1.getString("AVERAGE")) );	// 平均点
	            nonedata = true;
			}
			// 各教科女子
			ps2 = db2.prepareStatement( getKekkahyo(sTestSubClassCd, "2") );
			rs2 = ps2.executeQuery();
			while( rs2.next() ){
				//統計表出力
	            svf.VrsOutn( "HIGH" + icol,		2,	nvlT(rs2.getString("MAXSCORE")) );	// 最高点
	            svf.VrsOutn( "LOW" + icol,		2,	nvlT(rs2.getString("MINSCORE")) );	// 最低点
	            svf.VrsOutn( "AVERAGE" + icol,  2,	nvlT(rs2.getString("AVERAGE")) );	// 平均点
	            nonedata = true;
			}
			// 各教科全体
			ps3 = db2.prepareStatement( getKekkahyo(sTestSubClassCd, "") );
			rs3 = ps3.executeQuery();
			while( rs3.next() ){
				//統計表出力
	            svf.VrsOutn( "HIGH" + icol,		3,	nvlT(rs3.getString("MAXSCORE")) );	// 最高点
	            svf.VrsOutn( "LOW" + icol,		3,	nvlT(rs3.getString("MINSCORE")) );	// 最低点
	            svf.VrsOutn( "AVERAGE" + icol,  3,	nvlT(rs3.getString("AVERAGE")) );	// 平均点
	            nonedata = true;
			}
		} catch( Exception ex ){
			log.error("setkekkahyo error!",ex);
    	} finally {
    		db2.commit();
    		DbUtils.closeQuietly(null, ps1, rs1);
    		DbUtils.closeQuietly(null, ps2, rs2);
    		DbUtils.closeQuietly(null, ps3, rs3);
    	}
	}


	/**
     *  svf print 得点分布表
     */
	private void printTotalScore(DB2UDB db2,Vrw32alp svf)
	{
        int start_Row = 0;
    	PreparedStatement ps1 = null;
    	PreparedStatement ps2 = null;
    	ResultSet rs1 = null;
    	ResultSet rs2 = null;
		try {
			// 対象教科の満点を取得
			for(int i=0;i<hkamoku.size();i++){
				int retPerfect = getperfectTotal(db2, (String)hkamoku.get(String.valueOf(i+1)));
				//得点分布表の開始得点を求める
				start_Row =start_Row + retPerfect;
			}
			// 高校一般の場合、加算点を開始得点に加算する
			if(param[1].equals("2")){
				ps1 = db2.prepareStatement( statementkasanten());
				rs1 = ps1.executeQuery();
				if(rs1.next()){
					int ikasanten = rs1.getInt("KASANTEN");
					start_Row += ikasanten;
				}
			}
			// 得点データ取得
			ps2 = db2.prepareStatement( getSqlTotalScore() );
			rs2 = ps2.executeQuery();

	        boolean makeflg = true;
			int total4 = 0;
	        int maxgyo = 50;		// 最大行数
	        int maxcol = 8;			// 最大行数
	        int gyo = 1;			// 行数
	        int icol = 1;			// 列数
	        int rui_cnt1 = 0;		// 累計(男)
	        int rui_cnt2 = 0;		// 累計(女)
	        int rui_cnttotal = 0;	// 累計(計)
            
			while( rs2.next() ){
				total4 = rs2.getInt("TOTAL4");		// 全科目合計
				while(makeflg){
					// 入試制度が中学の場合
					if(param[1].equals("1")){
						if(start_Row < 101){
							break;
						}
					}
					// 入試制度が高校一般の場合
					if(param[1].equals("2")){
						if(start_Row < 51){
							break;
						}
					}
					// 入試制度が高校推薦の場合
					if(param[1].equals("3")){
						if(start_Row < 1){
							break;
						}
					}
					// 最大行数を超えた場合
					if(gyo > maxgyo){
						++icol;
		       			// 最大列・最大行数を超えた場合
						if(icol > maxcol){
							break;
						} else {
							gyo = 1;
						}
					}
					// 得点
	       			svf.VrsOutn("POINT" + icol,	gyo , String.valueOf(start_Row) );
					if(start_Row == total4){
						// 小計(男)
		       			svf.VrsOutn("B_COUNT" + icol + "_1",	gyo , rs2.getString("CNT1") );
		       			// 累計(男)
		       			rui_cnt1 = rui_cnt1 + Integer.valueOf(rs2.getString("CNT1")).intValue();
		       			svf.VrsOutn("B_COUNT" + icol + "_2",	gyo , String.valueOf(rui_cnt1) );
		       			// 小計(女)
		       			svf.VrsOutn("G_COUNT" + icol + "_1",	gyo , rs2.getString("CNT2") );
		       			// 累計(女)
		       			rui_cnt2 = rui_cnt2 + Integer.valueOf(rs2.getString("CNT2")).intValue();
		       			svf.VrsOutn("G_COUNT" + icol + "_2",	gyo , String.valueOf(rui_cnt2) );
		       			// 小計(計)
		       			svf.VrsOutn("TOTAL_COUNT" + icol + "_1",gyo , rs2.getString("CNT_TOTAL") );
		       			// 累計(計)
		       			rui_cnttotal = rui_cnttotal + Integer.valueOf(rs2.getString("CNT_TOTAL")).intValue();
		       			svf.VrsOutn("TOTAL_COUNT" + icol + "_2",gyo , String.valueOf(rui_cnttotal) );
		       			makeflg = false;
					}
					++gyo;
	       			--start_Row;
				}
       			// 最大列・最大行数を超えた場合
				if(gyo > maxgyo && icol > maxcol){
						break;
				}
				makeflg = true;	
			}
			//-------------------------*
			// 最低点の出力            *
			//-------------------------*
			//最大列・行数を超えていない場合
			if(icol <= maxcol && gyo <= maxgyo){
				makeflg = true;	
				while(makeflg){
					// 入試制度が中学の場合
					if(param[1].equals("1")){
						if(start_Row < 101){
							break;
						}
					}
					// 入試制度が高校一般の場合
					if(param[1].equals("2")){
						if(start_Row < 51){
							break;
						}
					}
					// 入試制度が高校推薦の場合
					if(param[1].equals("3")){
						if(start_Row < 1){
							break;
						}
					}
					// 最大行数を超えた場合
					if(gyo > maxgyo){
						++icol;
		       			// 最大列・最大行数を超えた場合
						if(icol > maxcol){
							break;
						} else {
							gyo = 1;
						}
					}
					// 得点
	       			svf.VrsOutn("POINT" + icol,	gyo , String.valueOf(start_Row) );
					++gyo;
	       			--start_Row;
				}
			}
		} catch( Exception ex ) {
			log.warn("printTotalScore read error!",ex);
    	} finally {
    		db2.commit();
    		DbUtils.closeQuietly(null, ps1, rs1);
    		DbUtils.closeQuietly(null, ps2, rs2);
    	}

	}//printTotalScore()の括り

	/**
     *  svf print 満点データ取得
     */
    private int getperfectTotal(DB2UDB db2, String sTestSubClassCd)
	{
        int perfect_Total = 0;
    	PreparedStatement ps = null;
    	ResultSet rs = null;
		try{
			// 満点マスタより試験科目の満点値を取得
			ps = db2.prepareStatement( getPerfectMst(sTestSubClassCd ));
			rs = ps.executeQuery();
			while( rs.next() ){
				perfect_Total = rs.getInt("PERFECT");	// 満点
			}
		} catch( Exception ex ){
			log.error("setkekkahyo error!",ex);
    	} finally {
    		db2.commit();
    		DbUtils.closeQuietly(null, ps, rs);
    	}
    	return perfect_Total;
	}

	/**
	 *	得点分布表出力対象データの取得
	 */
	private String getSqlTotalScore()
	{
		StringBuffer stb = new StringBuffer();
		try {
			stb.append("SELECT  ");
			stb.append("       W1.TOTAL4, ");
			stb.append("       COUNT(CASE WHEN W2.SEX='1' THEN '1' END) AS CNT1, ");
			stb.append("       COUNT(CASE WHEN W2.SEX='2' THEN '1' END) AS CNT2, ");
			stb.append("       COUNT(CASE WHEN W2.SEX IN('1','2') THEN '1' END) AS CNT_TOTAL ");
            stb.append("FROM  ENTEXAM_RECEPT_DAT W1 ");
			stb.append("      INNER JOIN ENTEXAM_APPLICANTBASE_DAT W2 ON ");
			stb.append("            W1.ENTEXAMYEAR = W2.ENTEXAMYEAR AND  ");
			stb.append("            W1.EXAMNO = W2.EXAMNO  ");
            //抽出条件
            stb.append("WHERE ");
            stb.append("W1.ENTEXAMYEAR = '" + param[0] + "'  AND ");	// 入試年度
            stb.append("W1.APPLICANTDIV = '" + param[1] + "' AND ");	// 入試制度
            stb.append("W1.TESTDIV = '" + param[2] + "' AND ");			// 入試区分
			// 点数指定がある場合
            if(param[3] != null && param[3].equals("1")){
                stb.append("W1.TOTAL4 < " + param[4] + " AND ");		// 全科目合計
            }
            stb.append("W1.TOTAL4 IS NOT NULL ");						// 全科目合計
            stb.append("GROUP BY ");
            stb.append("W1.TOTAL4 ");									// 全科目合計
            stb.append("ORDER BY ");
            stb.append("W1.TOTAL4 DESC ");								// 全科目合計
		} catch( Exception e ){
			log.warn("getSqlTotalScore error!",e);
		}
		return stb.toString();

	}//getSqlTotalScore()の括り
	
	/**
     *  統計表 総合欄、受験者数欄の取得
     */
    private String getKekkahyo_Total(String sex)
	{
        if( stb == null ) stb = new StringBuffer();
		else              stb.delete(0,stb.length());
		try{
            stb.append("SELECT ");
            stb.append("COUNT(*) AS TOTAL_CNT, ");
            stb.append("MAX(W1.TOTAL4) AS MAXSCORE, ");
            stb.append("MIN(W1.TOTAL4) AS MINSCORE, ");
            stb.append("ROUND(AVG(FLOAT(W1.TOTAL4))*10,0)/10 AS AVERAGE ");
            //テーブル
            stb.append("FROM  ENTEXAM_RECEPT_DAT W1 ");
			stb.append("      INNER JOIN ENTEXAM_APPLICANTBASE_DAT W2 ON ");
			stb.append("            W1.ENTEXAMYEAR = W2.ENTEXAMYEAR AND  ");
			stb.append("            W1.EXAMNO = W2.EXAMNO  ");
            if(sex.length() > 0){
    			stb.append("        AND W2.SEX = '" + sex + "'");
            }
            //抽出条件
            stb.append("WHERE ");
            stb.append("W1.ENTEXAMYEAR = '" + param[0] + "'  AND ");	// 入試年度
            stb.append("W1.APPLICANTDIV = '" + param[1] + "' AND ");	// 入試制度
            stb.append("W1.TESTDIV = '" + param[2] + "' AND ");			// 入試区分
            stb.append("W1.TOTAL4 IS NOT NULL ");						// 全科目合計

		} catch( Exception ex ){
			log.error("sql statement error!"+ex );
		}
		return stb.toString();
	}

	/**
     *  統計表 各科目欄取得
     *  (最高点・最低点・平均点・受験者数)
     */
    private String getKekkahyo(String sTestSubClassCd, String sex)
	{
		StringBuffer stb = new StringBuffer();
		try{
            stb.append("SELECT ");
            stb.append("COUNT(*) AS SUBCLASS_CNT, ");
            stb.append("MAX(W1.SCORE) AS MAXSCORE, ");
            stb.append("MIN(W1.SCORE) AS MINSCORE, ");
            stb.append("ROUND(AVG(FLOAT(SCORE))*10,0)/10 AS AVERAGE ");
            //テーブル
            stb.append("FROM  ENTEXAM_SCORE_DAT W1 ");
			stb.append("      INNER JOIN ENTEXAM_RECEPT_DAT W2 ON ");
			stb.append("            W2.ENTEXAMYEAR = W1.ENTEXAMYEAR AND  ");
			stb.append("            W2.APPLICANTDIV = W1.APPLICANTDIV AND  ");
			stb.append("            W2.EXAM_TYPE = W1.EXAM_TYPE AND  ");
			stb.append("            W2.TESTDIV = W1.TESTDIV AND  ");
			stb.append("            W2.RECEPTNO = W1.RECEPTNO  ");
			stb.append("      INNER JOIN ENTEXAM_APPLICANTBASE_DAT W3 ON ");
			stb.append("            W2.ENTEXAMYEAR = W3.ENTEXAMYEAR AND  ");
			stb.append("            W2.EXAMNO = W3.EXAMNO  ");
            if(sex.length() > 0){
    			stb.append("        AND W3.SEX = '" + sex + "'");
            }
            //抽出条件
            stb.append("WHERE ");
            stb.append("W1.ENTEXAMYEAR = '" + param[0] + "'  AND ");		// 入試年度
            stb.append("W1.APPLICANTDIV = '" + param[1] + "' AND ");		// 入試制度
            stb.append("W1.TESTDIV = '" + param[2] + "' AND ");				// 入試区分
            stb.append("W1.ATTEND_FLG = '1' AND ");							// 出欠フラグ(1:受験)
            stb.append("W1.TESTSUBCLASSCD = '" + sTestSubClassCd + "'");	// 試験科目コード 
            
		} catch( Exception ex ){
			log.error("sql statement error!"+ex );
		}
		return stb.toString();
	}
    
	/**
     *  統計表 加算点欄取得
     *  (最高点・最低点・平均点・受験者数)
     */
    private String getKekkahyo_kasan(String sex)
	{
		StringBuffer stb = new StringBuffer();
		try{
            stb.append("SELECT ");
            stb.append("MAX(KASANTEN_ALL) AS MAXSCORE, ");
            stb.append("MIN(KASANTEN_ALL) AS MINSCORE, ");
            stb.append("ROUND(AVG(FLOAT(KASANTEN_ALL))*10,0)/10 AS AVERAGE ");
            //テーブル
            stb.append("FROM  ENTEXAM_RECEPT_DAT W1 ");
            stb.append(      "INNER JOIN ENTEXAM_APPLICANTCONFRPT_DAT W2 ON ");
            stb.append(             "W2.ENTEXAMYEAR = W1.ENTEXAMYEAR AND ");
            stb.append(             "W2.EXAMNO = W1.EXAMNO ");
			stb.append("      INNER JOIN ENTEXAM_APPLICANTBASE_DAT W3 ON ");
			stb.append("            W1.ENTEXAMYEAR = W3.ENTEXAMYEAR AND  ");
			stb.append("            W1.EXAMNO = W3.EXAMNO  ");
            if(sex.length() > 0){
    			stb.append(  "AND W3.SEX = '" + sex + "'");
            }
            //抽出条件
            stb.append("WHERE ");
            stb.append("W1.ENTEXAMYEAR = '" + param[0] + "'  AND ");	// 入試年度
            stb.append("W1.APPLICANTDIV = '" + param[1] + "' AND ");	// 入試制度
            stb.append("W1.TESTDIV = '" + param[2] + "' AND ");			// 入試区分
            stb.append("W1.TOTAL4 IS NOT NULL AND ");					// 4科目合計
            stb.append("W2.KASANTEN_ALL IS NOT NULL");					// 加算点

		} catch( Exception ex ){
			log.error("sql statement error!"+ex );
		}
		return stb.toString();
	}

	/**
     *  統計表 受験者数欄取得
     */
    private String getKekkahyo_absent(String sex)
	{
        if( stb == null ) stb = new StringBuffer();
		else              stb.delete(0,stb.length());
		try{
            stb.append("SELECT ");
            stb.append("COUNT(*) AS DESIRE_CNT ");
            //テーブル
            stb.append("FROM  ENTEXAM_DESIRE_DAT W1");
			stb.append("      INNER JOIN ENTEXAM_APPLICANTBASE_DAT W2 ON ");
			stb.append("            W1.ENTEXAMYEAR = W2.ENTEXAMYEAR AND  ");
			stb.append("            W1.EXAMNO = W2.EXAMNO  ");
            if(sex.length() > 0){
    			stb.append(  "AND W2.SEX = '" + sex + "'");
            }
            //抽出条件
            stb.append("WHERE ");
            stb.append("W1.ENTEXAMYEAR = '" + param[0] + "'  AND ");	// 入試年度
            stb.append("W1.APPLICANTDIV = '" + param[1] + "' AND ");	// 入試制度
            stb.append("W1.TESTDIV = '" + param[2] + "' AND ");			// 入試区分
            stb.append("W1.APPLICANT_DIV = '1' ");						// 志願者区分(1:有り)

		} catch( Exception ex ){
			log.error("sql statement error!"+ex );
		}
		return stb.toString();
	}
	
	/**
     *  得点分布表 各科目の満点値取得
     */
    private String getPerfectMst(String testSubClasscd)
	{
        if( stb == null ) stb = new StringBuffer();
		else              stb.delete(0,stb.length());
		try{
            stb.append("SELECT ");
            stb.append("PERFECT ");		// 満点
            //テーブル
            stb.append("FROM  ENTEXAM_PERFECT_MST ");
            //抽出条件
            stb.append("WHERE ");
            stb.append("ENTEXAMYEAR = '" + param[0] + "'  AND ");	// 入試年度
            stb.append("APPLICANTDIV = '" + param[1] + "' AND ");	// 入試制度
            stb.append("TESTDIV = '" + param[2] + "' AND ");		// 入試区分
            stb.append("TESTSUBCLASSCD = '" + testSubClasscd + "'");// 試験科目

		} catch( Exception ex ){
			log.error("sql statement error!"+ex );
		}
		return stb.toString();
	}
	
    /**
     * 半角数字を全角数字に変換する
     */
    private String convZenkakuToHankaku(String s) {
        StringBuffer sb = new StringBuffer(s);
        for (int i = 0; i < s.length(); i++) {
          char c = s.charAt(i);
          if (c >= '0' && c <= '9') {
            sb.setCharAt(i, (char) (c - '0' + 0xff10));
          }
        }
        return sb.toString();
    }

    /**
     * 日付をフォーマットYYYY年MM月DD日に設定する
     */
    private String fomatSakuseiDate(String cnvDate) {

    	String retDate = "";
    	try {
			DateFormat foramt = new SimpleDateFormat("yyyy-MM-dd"); 
			//文字列よりDate型へ変換
			Date date1 = foramt.parse(cnvDate); 
			// 年月日のフォーマットを指定
			SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy'年'MM'月'dd'日'");
			// Date型より文字列へ変換
			retDate = sdf1.format(date1);
		} catch( Exception e ){
			log.error("setHeader set error!");
		}
		return retDate;
    }

    
	/**
     *  入試科目テーブルより、入試年度・入試制度に紐つく
     *  入試科目コードを取得する
     */
    private String getSubClass()
	{
		stb = new StringBuffer();
        
		try{
            stb.append("SELECT  T1.NAMECD2, T1.NAME1 ");
            stb.append(      "FROM    ENTEXAM_TESTSUBCLASSCD_DAT W1 ");
			stb.append(      "    LEFT JOIN NAME_MST T1 ON T1.NAMECD1='L009' ");
			stb.append(      "         AND T1.NAMECD2 = W1.TESTSUBCLASSCD  ");
            stb.append(      "WHERE   W1.ENTEXAMYEAR  = '" + param[0] + "' AND ");
            stb.append(      "        W1.APPLICANTDIV = '" + param[1] + "'");
			stb.append(" ORDER BY  ");
			stb.append("    W1.SHOWORDER ");


		} catch( Exception ex ){
			log.error("sql statement error!"+ex );
		}
		return stb.toString();
	}

	/**
     *  名称マスターから名称コードが最大の加算点を取得
     */
    private String statementkasanten()
	{
        if( stb == null ) stb = new StringBuffer();
		else              stb.delete(0,stb.length());
		try{
            stb.append(" SELECT ");
            stb.append("     max(NAME1) as KASANTEN ");
            stb.append(" FROM ");
            stb.append("     V_NAME_MST ");
            stb.append(" WHERE ");
            stb.append("     YEAR='" + param[0] + "' AND ");
            stb.append("     NAMECD1='L014' AND ");
            stb.append("     NAME1 is not null ");
			
		} catch( Exception ex ){
			log.error("sql statement error!"+ex );
		}
		return stb.toString();
	}


	/**
     *  入試科目テーブルより、入試年度・入試制度に紐つく
     *  入試科目コードを取得するし上段の統計表の表題の設定を行う
     */
    private void setSubClass(DB2UDB db2, Vrw32alp svf, String stb)
	{
		PreparedStatement ps = null;
    	ResultSet rs = null;
		try{
	    	int idx_kamoku = 1;
			// SQL発行
			ps = db2.prepareStatement(stb.toString());
            rs = ps.executeQuery();
			while( rs.next() ){
	            svf.VrsOutn( "SUBCLASSNAME", idx_kamoku, nvlT(rs.getString("NAME1")) );
				hkamoku.put(String.valueOf(idx_kamoku)	,rs.getString("NAMECD2"));
				++idx_kamoku;
			}
		    // 試験制度が高校(一般)の場合、帳票の試験科目名称の最後に
		    // 『加算』を設定する
		    if(param[1].equals("2")){
	            svf.VrsOutn( "SUBCLASSNAME", idx_kamoku, "加算" );
	            ++idx_kamoku;
		    }
            svf.VrsOutn( "SUBCLASSNAME", idx_kamoku, "総合" );
            svf.VrsOutn( "SUBCLASSNAME", 6, "受験者数" );

		} catch( Exception ex ){
            log.error("setSUBCLASS error!",ex);
    	} finally {
    		db2.commit();
    		DbUtils.closeQuietly(null, ps, rs);
    	}
	}

	/**
	 * NULL値を""として返す。
	 */
	private String nvlT(String val) {

		if (val == null) {
			return "";
		} else {
			return val.trim();
		}
	}


}//クラスの括り
