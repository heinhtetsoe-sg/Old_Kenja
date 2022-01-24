package servletpack.KNJL;

import java.io.IOException;
import java.io.PrintWriter;

import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_Get_Info;

/**
 *
 *	学校教育システム 賢者 [入試管理]
 *
 *					＜ＫＮＪＬ３１５＞  入学試験得点分布表
 *
 *	2005/12/29 nakamoto 作成日
 *	                    仕様１：４科目得点分布表において、２科合計○○点以上の受験者を除いた表にする。（除くフラグがＯＮの場合）
 */

public class KNJL315 {


    private static final Log log = LogFactory.getLog(KNJL315.class);

	/*----------------------------*
	 * HTTP Get リクエストの処理  *
	 *----------------------------*/
	public void svf_out(HttpServletRequest request, HttpServletResponse response)
	                 throws ServletException, IOException
	{

		Vrw32alp svf = new Vrw32alp(); 			//PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
		DB2UDB db2 = null;						//Databaseクラスを継承したクラス
		String param[] = new String[5];

	//	パラメータの取得
		try {
	        param[0] = request.getParameter("YEAR");         			//次年度
			param[1] = request.getParameter("TESTDV");   				//入試区分 1:第１回,2:第２回,3:第３回
			param[2] = request.getParameter("APDIV");   				//入試制度 1:一般（中学）
			param[3] = request.getParameter("CHECK1");   				//受験者を除くフラグ
			param[4] = request.getParameter("TEXT1");   				//XXX点
		} catch( Exception ex ) {
			log.warn("parameter error!",ex);
		}

	//	print設定
		PrintWriter outstrm = new PrintWriter (response.getOutputStream());
		response.setContentType("application/pdf");

	//	svf設定
		int ret = svf.VrInit();						   	//クラスの初期化
        if (false && 0 != ret) { ret = 0; }
		ret = svf.VrSetSpoolFileStream(response.getOutputStream());   		//PDFファイル名の設定

	//	ＤＢ接続
		try {
			db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
			db2.open();
		} catch( Exception ex ) {
			log.error("DB2 open error!",ex);
			return;
		}


	//	ＳＶＦ作成処理
		boolean nonedata = false; 								//該当データなしフラグ

		getHeaderData(db2,svf,param);							//ヘッダーデータ抽出メソッド

for(int ia=0 ; ia<param.length ; ia++) log.debug("param["+ia+"]="+param[ia]);

		//SVF出力

	    if( printMain(db2,svf,param) ) nonedata = true;

log.debug("nonedata="+nonedata);

	//	該当データ無し
		if( !nonedata ){
			ret = svf.VrSetForm("MES001.frm", 0);
			ret = svf.VrsOut("note" , "note");
			ret = svf.VrEndPage();
		}

	// 	終了処理
		ret = svf.VrQuit();
		db2.commit();
		db2.close();				//DBを閉じる
		outstrm.close();			//ストリームを閉じる 

    }//doGetの括り


	/**ヘッダーデータを抽出*/
	private void getHeaderData(DB2UDB db2,Vrw32alp svf,String param[]){

		int ret = 0;
        if (false && 0 != ret) { ret = 0; }

	//	フォーム
   		ret = svf.VrSetForm("KNJL315.frm", 1);

	//	次年度
		try {
			ret = svf.VrsOut("NENDO"    , nao_package.KenjaProperties.gengou(Integer.parseInt(param[0])) + "年度" );
		} catch( Exception e ){
			log.warn("jinendo get error!",e);
		}

	//	試験区分
		try {
			String sql = "SELECT NAME AS NAME1 FROM ENTEXAM_TESTDIV_MST WHERE ENTEXAMYEAR='"+param[0]+"' AND TESTDIV='"+param[1]+"' ";
			db2.query(sql);
			ResultSet rs = db2.getResultSet();
			while( rs.next() ){
    			ret = svf.VrsOut("TESTDIV"  , rs.getString("NAME1") );
			}
			db2.commit();
		} catch( Exception e ){
			log.warn("testname get error!",e);
		}

	//	作成日
		try {
    		KNJ_Get_Info getinfo = new KNJ_Get_Info();
    		KNJ_Get_Info.ReturnVal returnval = null;
			returnval = getinfo.Control(db2);
			ret = svf.VrsOut("DATE" , KNJ_EditDate.h_format_JP(returnval.val3) );
    		getinfo = null;
    		returnval = null;
		} catch( Exception e ){
			log.warn("ctrl_date get error!",e);
		}

	//	注
        if (param[3] != null) ret = svf.VrsOut("REMOVE_MSG"  , "注）２科目合計"+param[4]+"点以上除く" );

	}//getHeaderData()の括り


	/**印刷処理メイン*/
	private boolean printMain(DB2UDB db2,Vrw32alp svf,String param[])
	{
		boolean nonedata = false;

		try {
        	//明細データ
       		if( printSubclassScore(db2,svf,param) ){//表２
                printSubclassAverage(db2,svf,param);//表１：各教科
                for (int ia = 1; ia < 3; ia++) {
                    printTotalAverage(db2,svf,param,ia);   //表１：２科型・４科型
                    printTotalScore(db2,svf,param,ia);     //表３：２科型・４科型
                }
       			svf.VrEndPage();
                nonedata = true;
            }
		} catch( Exception ex ) {
			log.warn("printMain read error!",ex);
		}

		return nonedata;

	}//printMain()の括り


	/**明細データ印刷処理(表１：各教科)*/
	private void printSubclassAverage(DB2UDB db2,Vrw32alp svf,String param[])
	{
		int ret = 0;
        if (false && 0 != ret) { ret = 0; }
		try {
log.debug("SubclassAverage start!");
			db2.query(getSqlSubclassAverage(param));
			ResultSet rs = db2.getResultSet();
log.debug("SubclassAverage end!");

			while( rs.next() ){
                //得点分布
       			ret = svf.VrsOut("CLASS1_"+rs.getString("TESTSUBCLASSCD")  , rs.getString("KEI") );//合計
       			ret = svf.VrsOut("CLASS2_"+rs.getString("TESTSUBCLASSCD")  , rs.getString("HEI") );//平均
       			ret = svf.VrsOut("CLASS3_"+rs.getString("TESTSUBCLASSCD")  , rs.getString("NIN") );//人数
			}
			rs.close();
			db2.commit();
		} catch( Exception ex ) {
			log.warn("printSubclassAverage read error!",ex);
		}

	}//printSubclassAverage()の括り


	/**明細データ印刷処理(表１：２科型・４科型)*/
	private void printTotalAverage(DB2UDB db2,Vrw32alp svf,String param[], int ala_flg)
	{
		int ret = 0;
        if (false && 0 != ret) { ret = 0; }
		try {
log.debug("TotalAverage start"+ala_flg);
			db2.query(getSqlTotalAverage(param, ala_flg));
			ResultSet rs = db2.getResultSet();
log.debug("TotalAverage end"+ala_flg);

			while( rs.next() ){
                //合計列
       			ret = svf.VrsOut("TOTAL1_"+String.valueOf(ala_flg)  , rs.getString("KEI_G") );  //合計行
       			ret = svf.VrsOut("TOTAL2_"+String.valueOf(ala_flg)  , rs.getString("HEI_G") );  //平均行
       			ret = svf.VrsOut("TOTAL3_"+String.valueOf(ala_flg)  , rs.getString("NIN") );    //人数行
                //平均列
       			ret = svf.VrsOut("AVERAGE1_"+String.valueOf(ala_flg), rs.getString("KEI_H") );  //合計行
       			ret = svf.VrsOut("AVERAGE2_"+String.valueOf(ala_flg), rs.getString("HEI_H") );  //平均行
       			ret = svf.VrsOut("AVERAGE3_"+String.valueOf(ala_flg), rs.getString("NIN") );    //人数行
			}
			rs.close();
			db2.commit();
		} catch( Exception ex ) {
			log.warn("printTotalAverage read error!",ex);
		}

	}//printTotalAverage()の括り


	/**明細データ印刷処理(表２：各教科)*/
	private boolean printSubclassScore(DB2UDB db2,Vrw32alp svf,String param[])
	{
		boolean nonedata = false;
		int ret = 0;
        if (false && 0 != ret) { ret = 0; }
		try {
log.debug("SubclassScore start!");
			db2.query(getSqlSubclassScore(param));
			ResultSet rs = db2.getResultSet();
log.debug("SubclassScore end!");

            String len = "0";
            int gyo_s = 0;
			while( rs.next() ){
                //教科
                len = rs.getString("TESTSUBCLASSCD");
                gyo_s = ( len.equals("3") || len.equals("4") ) ? 11 : 1 ;
                //得点分布
                for (int gyo = gyo_s; gyo < 23; gyo++) 
        			ret = svf.VrsOutn("MEMBER1_"+len    ,gyo    , rs.getString("CNT"+String.valueOf(gyo)) );

        		nonedata = true;
			}
			rs.close();
			db2.commit();
		} catch( Exception ex ) {
			log.warn("printSubclassScore read error!",ex);
		}
		return nonedata;

	}//printSubclassScore()の括り


	/**明細データ印刷処理(表３：２科型・４科型)*/
	private void printTotalScore(DB2UDB db2,Vrw32alp svf,String param[], int ala_flg)
	{
        int gyo_s = 0;
		try {
log.debug("TotalScore start"+ala_flg);
			db2.query(getSqlTotalScore(param, ala_flg));
			ResultSet rs = db2.getResultSet();
log.debug("TotalScore end"+ala_flg);

            int cnt = 0;
            int rui = 0;
            int gyo2 = 0;
            String len = "0";
            gyo_s = ( 1 == ala_flg ) ? 20 : 1 ;
			while( rs.next() ){
                //得点分布
                for (int gyo = gyo_s; gyo < 61; gyo++) {
                    cnt = rs.getInt("CNT"+String.valueOf(gyo));//人数
                    rui = rui + cnt;//累計
                    gyo2 = (gyo < 31) ? gyo : gyo-30 ;
                    len = (gyo < 31) ? "_1" : "_2" ;

           			svf.VrsOutn("MEMBER"+String.valueOf(ala_flg+1)+len       , gyo2 , String.valueOf(cnt) );
           			svf.VrsOutn("TOTAL_MEMBER"+String.valueOf(ala_flg+1)+len , gyo2 , String.valueOf(rui) );
                }
			}
			rs.close();
			db2.commit();
		} catch( Exception ex ) {
			log.warn("printTotalScore read error!",ex);
		}

	}//printTotalScore()の括り


	/**
	 *	(表１)各教科の合計・平均・人数を取得
	 *
	 */
	private String getSqlSubclassAverage(String param[])
	{
		StringBuffer stb = new StringBuffer();
		try {
            //志願者得点データ・志願者受付データ
			stb.append("WITH EXAM_SCORE AS ( ");
			stb.append("    SELECT W1.EXAM_TYPE, W1.TESTSUBCLASSCD, W1.SCORE, W2.EXAMNO ");
			stb.append("    FROM   ENTEXAM_SCORE_DAT W1, ENTEXAM_RECEPT_DAT W2 ");
			stb.append("    WHERE  W2.ENTEXAMYEAR = '"+param[0]+"' AND ");
			stb.append("           W2.APPLICANTDIV = '"+param[2]+"' AND ");
			stb.append("           W2.TESTDIV = '"+param[1]+"' AND ");
			stb.append("           W1.ENTEXAMYEAR = W2.ENTEXAMYEAR AND ");
			stb.append("           W1.APPLICANTDIV = W2.APPLICANTDIV AND ");
			stb.append("           W1.TESTDIV = W2.TESTDIV AND ");
			stb.append("           W1.EXAM_TYPE = W2.EXAM_TYPE AND ");
			stb.append("           W1.RECEPTNO = W2.RECEPTNO AND ");
			stb.append("           W1.SCORE IS NOT NULL ");
			stb.append("    ) ");

            //メイン
			stb.append("SELECT TESTSUBCLASSCD, ");
			stb.append("       COUNT(*) AS NIN, ");
			stb.append("       SUM(SCORE) AS KEI, ");
			stb.append("       DECIMAL(ROUND(AVG(FLOAT(SCORE))*10,0)/10,5,1) AS HEI ");
			stb.append("FROM   EXAM_SCORE ");
			stb.append("GROUP BY TESTSUBCLASSCD ");
			stb.append("ORDER BY TESTSUBCLASSCD ");
		} catch( Exception e ){
			log.warn("getSqlSubclassAverage error!",e);
		}
		return stb.toString();

	}//getSqlSubclassAverage()の括り


	/**
	 *	(表１)２科型・４科型の合計・平均・人数を取得
	 *
	 */
	private String getSqlTotalAverage(String param[], int ala_flg)
	{
		StringBuffer stb = new StringBuffer();
		try {
            //志願者得点データ・志願者受付データ
			stb.append("WITH EXAM_SCORE AS ( ");
			stb.append("    SELECT W1.EXAM_TYPE, W1.TESTSUBCLASSCD, W1.SCORE, W2.EXAMNO ");
			stb.append("    FROM   ENTEXAM_SCORE_DAT W1, ENTEXAM_RECEPT_DAT W2 ");
			stb.append("    WHERE  W2.ENTEXAMYEAR = '"+param[0]+"' AND ");
			stb.append("           W2.APPLICANTDIV = '"+param[2]+"' AND ");
			stb.append("           W2.TESTDIV = '"+param[1]+"' AND ");
            if (ala_flg == 1) { //２科
    			stb.append("       W1.TESTSUBCLASSCD in ('1','2') AND ");
            } else {            //４科
    			stb.append("       W2.EXAM_TYPE = '"+String.valueOf(ala_flg)+"' AND ");
            }
			stb.append("           W1.ENTEXAMYEAR = W2.ENTEXAMYEAR AND ");
			stb.append("           W1.APPLICANTDIV = W2.APPLICANTDIV AND ");
			stb.append("           W1.TESTDIV = W2.TESTDIV AND ");
			stb.append("           W1.EXAM_TYPE = W2.EXAM_TYPE AND ");
			stb.append("           W1.RECEPTNO = W2.RECEPTNO AND ");
			stb.append("           W1.SCORE IS NOT NULL ");
			stb.append("    ) ");
            //得点がNULLの受験者
            String testsubcnt = (ala_flg == 1) ? "2" : "4" ;
			stb.append(",EXAM_SCORE_NULL AS ( ");
			stb.append("    SELECT EXAMNO ");
			stb.append("    FROM   EXAM_SCORE ");
			stb.append("    GROUP BY EXAMNO ");
			stb.append("    HAVING COUNT(*) < "+testsubcnt+" ");
			stb.append("    ) ");
/*****
            //２科合計○○点以上の受験者
            if (param[3] != null && ala_flg == 2) {
    			stb.append(",EXAM_SCORE_REMOVES AS ( ");
    			stb.append("    SELECT EXAMNO ");
    			stb.append("    FROM   EXAM_SCORE ");
    			stb.append("    WHERE  TESTSUBCLASSCD in ('1','2') ");
    			stb.append("    GROUP BY EXAMNO ");
    			stb.append("    HAVING "+param[4]+" <= SUM(SCORE) ");
    			stb.append("    ) ");
            }
*****/
            //教科毎の人数・合計
			stb.append(",EXAM_SCORE_TESTSUB AS ( ");
			stb.append("    SELECT TESTSUBCLASSCD, ");
			stb.append("           COUNT(*) AS NIN, ");
			stb.append("           SUM(SCORE) AS KEI ");
			stb.append("    FROM   EXAM_SCORE ");
			stb.append("    WHERE  EXAMNO NOT IN (SELECT EXAMNO FROM EXAM_SCORE_NULL) ");
/*****
            if (param[3] != null && ala_flg == 2) {
    			stb.append("       AND EXAMNO NOT IN (SELECT EXAMNO FROM EXAM_SCORE_REMOVES) ");
            }
*****/
			stb.append("    GROUP BY TESTSUBCLASSCD ");
			stb.append("    ) ");

            //メイン
			stb.append("SELECT NIN, ");
			stb.append("       SUM(KEI) AS KEI_G, ");
			stb.append("       DECIMAL(ROUND(FLOAT(SUM(KEI))/COUNT(*)*10,0)/10,9,1) AS KEI_H, ");
			stb.append("       DECIMAL(ROUND(FLOAT(SUM(KEI))/NIN*10,0)/10,5,1) AS HEI_G, ");
			stb.append("       DECIMAL(ROUND(FLOAT(SUM(KEI))/(NIN*COUNT(*))*10,0)/10,5,1) AS HEI_H ");
			stb.append("FROM   EXAM_SCORE_TESTSUB ");
			stb.append("GROUP BY NIN ");
		} catch( Exception e ){
			log.warn("getSqlTotalAverage error!",e);
		}
		return stb.toString();

	}//getSqlTotalAverage()の括り


	/**
	 *	(表２)各教科の得点分布を取得
	 *
	 */
	private String getSqlSubclassScore(String param[])
	{
		StringBuffer stb = new StringBuffer();
		try {
            //志願者得点データ・志願者受付データ
			stb.append("WITH EXAM_SCORE AS ( ");
			stb.append("    SELECT W1.EXAM_TYPE, W1.TESTSUBCLASSCD, W1.SCORE, W2.EXAMNO ");
			stb.append("    FROM   ENTEXAM_SCORE_DAT W1, ENTEXAM_RECEPT_DAT W2 ");
			stb.append("    WHERE  W2.ENTEXAMYEAR = '"+param[0]+"' AND ");
			stb.append("           W2.APPLICANTDIV = '"+param[2]+"' AND ");
			stb.append("           W2.TESTDIV = '"+param[1]+"' AND ");
			stb.append("           W1.ENTEXAMYEAR = W2.ENTEXAMYEAR AND ");
			stb.append("           W1.APPLICANTDIV = W2.APPLICANTDIV AND ");
			stb.append("           W1.TESTDIV = W2.TESTDIV AND ");
			stb.append("           W1.EXAM_TYPE = W2.EXAM_TYPE AND ");
			stb.append("           W1.RECEPTNO = W2.RECEPTNO AND ");
			stb.append("           W1.SCORE IS NOT NULL ");
			stb.append("    ) ");

            //メイン
			stb.append("SELECT TESTSUBCLASSCD, ");
			stb.append("       SUM(CASE WHEN SCORE = 100 THEN 1 ELSE 0 END) AS CNT1, ");
			stb.append("       SUM(CASE WHEN  95 <= SCORE AND SCORE <=  99 THEN 1 ELSE 0 END) AS CNT2, ");
			stb.append("       SUM(CASE WHEN  90 <= SCORE AND SCORE <=  94 THEN 1 ELSE 0 END) AS CNT3, ");
			stb.append("       SUM(CASE WHEN  85 <= SCORE AND SCORE <=  89 THEN 1 ELSE 0 END) AS CNT4, ");
			stb.append("       SUM(CASE WHEN  80 <= SCORE AND SCORE <=  84 THEN 1 ELSE 0 END) AS CNT5, ");
			stb.append("       SUM(CASE WHEN  75 <= SCORE AND SCORE <=  79 THEN 1 ELSE 0 END) AS CNT6, ");
			stb.append("       SUM(CASE WHEN  70 <= SCORE AND SCORE <=  74 THEN 1 ELSE 0 END) AS CNT7, ");
			stb.append("       SUM(CASE WHEN  65 <= SCORE AND SCORE <=  69 THEN 1 ELSE 0 END) AS CNT8, ");
			stb.append("       SUM(CASE WHEN  60 <= SCORE AND SCORE <=  64 THEN 1 ELSE 0 END) AS CNT9, ");
			stb.append("       SUM(CASE WHEN  55 <= SCORE AND SCORE <=  59 THEN 1 ELSE 0 END) AS CNT10, ");
			stb.append("       SUM(CASE WHEN  50 <= SCORE AND SCORE <=  54 THEN 1 ELSE 0 END) AS CNT11, ");
			stb.append("       SUM(CASE WHEN  45 <= SCORE AND SCORE <=  49 THEN 1 ELSE 0 END) AS CNT12, ");
			stb.append("       SUM(CASE WHEN  40 <= SCORE AND SCORE <=  44 THEN 1 ELSE 0 END) AS CNT13, ");
			stb.append("       SUM(CASE WHEN  35 <= SCORE AND SCORE <=  39 THEN 1 ELSE 0 END) AS CNT14, ");
			stb.append("       SUM(CASE WHEN  30 <= SCORE AND SCORE <=  34 THEN 1 ELSE 0 END) AS CNT15, ");
			stb.append("       SUM(CASE WHEN  25 <= SCORE AND SCORE <=  29 THEN 1 ELSE 0 END) AS CNT16, ");
			stb.append("       SUM(CASE WHEN  20 <= SCORE AND SCORE <=  24 THEN 1 ELSE 0 END) AS CNT17, ");
			stb.append("       SUM(CASE WHEN  15 <= SCORE AND SCORE <=  19 THEN 1 ELSE 0 END) AS CNT18, ");
			stb.append("       SUM(CASE WHEN  10 <= SCORE AND SCORE <=  14 THEN 1 ELSE 0 END) AS CNT19, ");
			stb.append("       SUM(CASE WHEN   5 <= SCORE AND SCORE <=   9 THEN 1 ELSE 0 END) AS CNT20, ");
			stb.append("       SUM(CASE WHEN   1 <= SCORE AND SCORE <=   4 THEN 1 ELSE 0 END) AS CNT21, ");
			stb.append("       SUM(CASE WHEN SCORE = 0 THEN 1 ELSE 0 END) AS CNT22 ");
			stb.append("FROM   EXAM_SCORE ");
			stb.append("GROUP BY TESTSUBCLASSCD ");
		} catch( Exception e ){
			log.warn("getSqlSubclassScore error!",e);
		}
		return stb.toString();

	}//getSqlSubclassScore()の括り


	/**
	 *	(表３)２科型・４科型の得点分布を取得
	 *
	 */
	private String getSqlTotalScore(String param[], int ala_flg)
	{
		StringBuffer stb = new StringBuffer();
		try {
            //志願者得点データ・志願者受付データ
			stb.append("WITH EXAM_SCORE AS ( ");
			stb.append("    SELECT W1.EXAM_TYPE, W1.TESTSUBCLASSCD, W1.SCORE, W2.EXAMNO ");
			stb.append("    FROM   ENTEXAM_SCORE_DAT W1, ENTEXAM_RECEPT_DAT W2 ");
			stb.append("    WHERE  W2.ENTEXAMYEAR = '"+param[0]+"' AND ");
			stb.append("           W2.APPLICANTDIV = '"+param[2]+"' AND ");
			stb.append("           W2.TESTDIV = '"+param[1]+"' AND ");
            if (ala_flg == 1) { //２科
    			stb.append("       W1.TESTSUBCLASSCD in ('1','2') AND ");
            } else {            //４科
    			stb.append("       W2.EXAM_TYPE = '"+String.valueOf(ala_flg)+"' AND ");
            }
			stb.append("           W1.ENTEXAMYEAR = W2.ENTEXAMYEAR AND ");
			stb.append("           W1.APPLICANTDIV = W2.APPLICANTDIV AND ");
			stb.append("           W1.TESTDIV = W2.TESTDIV AND ");
			stb.append("           W1.EXAM_TYPE = W2.EXAM_TYPE AND ");
			stb.append("           W1.RECEPTNO = W2.RECEPTNO AND ");
			stb.append("           W1.SCORE IS NOT NULL ");
			stb.append("    ) ");
            //得点がNULLの受験者
            String testsubcnt = (ala_flg == 1) ? "2" : "4" ;
			stb.append(",EXAM_SCORE_NULL AS ( ");
			stb.append("    SELECT EXAMNO ");
			stb.append("    FROM   EXAM_SCORE ");
			stb.append("    GROUP BY EXAMNO ");
			stb.append("    HAVING COUNT(*) < "+testsubcnt+" ");
			stb.append("    ) ");
            //２科合計○○点以上の受験者
            if (param[3] != null && ala_flg == 2) {
    			stb.append(",EXAM_SCORE_REMOVES AS ( ");
    			stb.append("    SELECT EXAMNO ");
    			stb.append("    FROM   EXAM_SCORE ");
    			stb.append("    WHERE  TESTSUBCLASSCD in ('1','2') ");
    			stb.append("    GROUP BY EXAMNO ");
    			stb.append("    HAVING "+param[4]+" <= SUM(SCORE) ");
    			stb.append("    ) ");
            }
            //各受験者の合計得点
			stb.append(",EXAM_SCORE_SUM AS ( ");
			stb.append("    SELECT EXAMNO, SUM(SCORE) AS SCORE ");
			stb.append("    FROM   EXAM_SCORE ");
			stb.append("    WHERE  EXAMNO NOT IN (SELECT EXAMNO FROM EXAM_SCORE_NULL) ");
            if (param[3] != null && ala_flg == 2) {
    			stb.append("       AND EXAMNO NOT IN (SELECT EXAMNO FROM EXAM_SCORE_REMOVES) ");
            }
			stb.append("    GROUP BY EXAMNO ");
			stb.append("    ) ");

            //メイン
			stb.append("SELECT SUM(CASE WHEN 295 <= SCORE AND SCORE <= 300 THEN 1 ELSE 0 END) AS CNT1, ");
			stb.append("       SUM(CASE WHEN 290 <= SCORE AND SCORE <= 294 THEN 1 ELSE 0 END) AS CNT2, ");
			stb.append("       SUM(CASE WHEN 285 <= SCORE AND SCORE <= 289 THEN 1 ELSE 0 END) AS CNT3, ");
			stb.append("       SUM(CASE WHEN 280 <= SCORE AND SCORE <= 284 THEN 1 ELSE 0 END) AS CNT4, ");
			stb.append("       SUM(CASE WHEN 275 <= SCORE AND SCORE <= 279 THEN 1 ELSE 0 END) AS CNT5, ");
			stb.append("       SUM(CASE WHEN 270 <= SCORE AND SCORE <= 274 THEN 1 ELSE 0 END) AS CNT6, ");
			stb.append("       SUM(CASE WHEN 265 <= SCORE AND SCORE <= 269 THEN 1 ELSE 0 END) AS CNT7, ");
			stb.append("       SUM(CASE WHEN 260 <= SCORE AND SCORE <= 264 THEN 1 ELSE 0 END) AS CNT8, ");
			stb.append("       SUM(CASE WHEN 255 <= SCORE AND SCORE <= 259 THEN 1 ELSE 0 END) AS CNT9, ");
			stb.append("       SUM(CASE WHEN 250 <= SCORE AND SCORE <= 254 THEN 1 ELSE 0 END) AS CNT10, ");
			stb.append("       SUM(CASE WHEN 245 <= SCORE AND SCORE <= 249 THEN 1 ELSE 0 END) AS CNT11, ");
			stb.append("       SUM(CASE WHEN 240 <= SCORE AND SCORE <= 244 THEN 1 ELSE 0 END) AS CNT12, ");
			stb.append("       SUM(CASE WHEN 235 <= SCORE AND SCORE <= 239 THEN 1 ELSE 0 END) AS CNT13, ");
			stb.append("       SUM(CASE WHEN 230 <= SCORE AND SCORE <= 234 THEN 1 ELSE 0 END) AS CNT14, ");
			stb.append("       SUM(CASE WHEN 225 <= SCORE AND SCORE <= 229 THEN 1 ELSE 0 END) AS CNT15, ");
			stb.append("       SUM(CASE WHEN 220 <= SCORE AND SCORE <= 224 THEN 1 ELSE 0 END) AS CNT16, ");
			stb.append("       SUM(CASE WHEN 215 <= SCORE AND SCORE <= 219 THEN 1 ELSE 0 END) AS CNT17, ");
			stb.append("       SUM(CASE WHEN 210 <= SCORE AND SCORE <= 214 THEN 1 ELSE 0 END) AS CNT18, ");
			stb.append("       SUM(CASE WHEN 205 <= SCORE AND SCORE <= 209 THEN 1 ELSE 0 END) AS CNT19, ");

			stb.append("       SUM(CASE WHEN 200 <= SCORE AND SCORE <= 204 THEN 1 ELSE 0 END) AS CNT20, ");
			stb.append("       SUM(CASE WHEN 195 <= SCORE AND SCORE <= 199 THEN 1 ELSE 0 END) AS CNT21, ");
			stb.append("       SUM(CASE WHEN 190 <= SCORE AND SCORE <= 194 THEN 1 ELSE 0 END) AS CNT22, ");
			stb.append("       SUM(CASE WHEN 185 <= SCORE AND SCORE <= 189 THEN 1 ELSE 0 END) AS CNT23, ");
			stb.append("       SUM(CASE WHEN 180 <= SCORE AND SCORE <= 184 THEN 1 ELSE 0 END) AS CNT24, ");
			stb.append("       SUM(CASE WHEN 175 <= SCORE AND SCORE <= 179 THEN 1 ELSE 0 END) AS CNT25, ");
			stb.append("       SUM(CASE WHEN 170 <= SCORE AND SCORE <= 174 THEN 1 ELSE 0 END) AS CNT26, ");
			stb.append("       SUM(CASE WHEN 165 <= SCORE AND SCORE <= 169 THEN 1 ELSE 0 END) AS CNT27, ");
			stb.append("       SUM(CASE WHEN 160 <= SCORE AND SCORE <= 164 THEN 1 ELSE 0 END) AS CNT28, ");
			stb.append("       SUM(CASE WHEN 155 <= SCORE AND SCORE <= 159 THEN 1 ELSE 0 END) AS CNT29, ");
			stb.append("       SUM(CASE WHEN 150 <= SCORE AND SCORE <= 154 THEN 1 ELSE 0 END) AS CNT30, ");
			stb.append("       SUM(CASE WHEN 145 <= SCORE AND SCORE <= 149 THEN 1 ELSE 0 END) AS CNT31, ");
			stb.append("       SUM(CASE WHEN 140 <= SCORE AND SCORE <= 144 THEN 1 ELSE 0 END) AS CNT32, ");
			stb.append("       SUM(CASE WHEN 135 <= SCORE AND SCORE <= 139 THEN 1 ELSE 0 END) AS CNT33, ");
			stb.append("       SUM(CASE WHEN 130 <= SCORE AND SCORE <= 134 THEN 1 ELSE 0 END) AS CNT34, ");
			stb.append("       SUM(CASE WHEN 125 <= SCORE AND SCORE <= 129 THEN 1 ELSE 0 END) AS CNT35, ");
			stb.append("       SUM(CASE WHEN 120 <= SCORE AND SCORE <= 124 THEN 1 ELSE 0 END) AS CNT36, ");
			stb.append("       SUM(CASE WHEN 115 <= SCORE AND SCORE <= 119 THEN 1 ELSE 0 END) AS CNT37, ");
			stb.append("       SUM(CASE WHEN 110 <= SCORE AND SCORE <= 114 THEN 1 ELSE 0 END) AS CNT38, ");
			stb.append("       SUM(CASE WHEN 105 <= SCORE AND SCORE <= 109 THEN 1 ELSE 0 END) AS CNT39, ");
			stb.append("       SUM(CASE WHEN 100 <= SCORE AND SCORE <= 104 THEN 1 ELSE 0 END) AS CNT40, ");
			stb.append("       SUM(CASE WHEN  95 <= SCORE AND SCORE <=  99 THEN 1 ELSE 0 END) AS CNT41, ");
			stb.append("       SUM(CASE WHEN  90 <= SCORE AND SCORE <=  94 THEN 1 ELSE 0 END) AS CNT42, ");
			stb.append("       SUM(CASE WHEN  85 <= SCORE AND SCORE <=  89 THEN 1 ELSE 0 END) AS CNT43, ");
			stb.append("       SUM(CASE WHEN  80 <= SCORE AND SCORE <=  84 THEN 1 ELSE 0 END) AS CNT44, ");
			stb.append("       SUM(CASE WHEN  75 <= SCORE AND SCORE <=  79 THEN 1 ELSE 0 END) AS CNT45, ");
			stb.append("       SUM(CASE WHEN  70 <= SCORE AND SCORE <=  74 THEN 1 ELSE 0 END) AS CNT46, ");
			stb.append("       SUM(CASE WHEN  65 <= SCORE AND SCORE <=  69 THEN 1 ELSE 0 END) AS CNT47, ");
			stb.append("       SUM(CASE WHEN  60 <= SCORE AND SCORE <=  64 THEN 1 ELSE 0 END) AS CNT48, ");
			stb.append("       SUM(CASE WHEN  55 <= SCORE AND SCORE <=  59 THEN 1 ELSE 0 END) AS CNT49, ");
			stb.append("       SUM(CASE WHEN  50 <= SCORE AND SCORE <=  54 THEN 1 ELSE 0 END) AS CNT50, ");
			stb.append("       SUM(CASE WHEN  45 <= SCORE AND SCORE <=  49 THEN 1 ELSE 0 END) AS CNT51, ");
			stb.append("       SUM(CASE WHEN  40 <= SCORE AND SCORE <=  44 THEN 1 ELSE 0 END) AS CNT52, ");
			stb.append("       SUM(CASE WHEN  35 <= SCORE AND SCORE <=  39 THEN 1 ELSE 0 END) AS CNT53, ");
			stb.append("       SUM(CASE WHEN  30 <= SCORE AND SCORE <=  34 THEN 1 ELSE 0 END) AS CNT54, ");
			stb.append("       SUM(CASE WHEN  25 <= SCORE AND SCORE <=  29 THEN 1 ELSE 0 END) AS CNT55, ");
			stb.append("       SUM(CASE WHEN  20 <= SCORE AND SCORE <=  24 THEN 1 ELSE 0 END) AS CNT56, ");
			stb.append("       SUM(CASE WHEN  15 <= SCORE AND SCORE <=  19 THEN 1 ELSE 0 END) AS CNT57, ");
			stb.append("       SUM(CASE WHEN  10 <= SCORE AND SCORE <=  14 THEN 1 ELSE 0 END) AS CNT58, ");
			stb.append("       SUM(CASE WHEN   5 <= SCORE AND SCORE <=   9 THEN 1 ELSE 0 END) AS CNT59, ");
			stb.append("       SUM(CASE WHEN   0 <= SCORE AND SCORE <=   4 THEN 1 ELSE 0 END) AS CNT60 ");
			stb.append("FROM   EXAM_SCORE_SUM ");
		} catch( Exception e ){
			log.warn("getSqlTotalScore error!",e);
		}
		return stb.toString();

	}//getSqlTotalScore()の括り



}//クラスの括り
