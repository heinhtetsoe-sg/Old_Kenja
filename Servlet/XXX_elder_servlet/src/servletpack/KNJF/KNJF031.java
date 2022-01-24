// kanji=漢字
/*
 * $Id: b26e63316263496af9084aa752d52f812a0511aa $
 *
 * 作成日: 2005/06/24
 * 作成者: nakamoto
 *
 * Copyright(C) 2005-2008 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJF;

import java.io.IOException;
import java.io.PrintWriter;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import servletpack.KNJZ.detail.KNJ_EditDate;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 *	学校教育システム 賢者 [保健管理]
 *
 *					＜ＫＮＪＦ０３１＞  保健各種印刷（視力・歯科）（近大）
 *
 *	2005/06/24 nakamoto 作成日
 *	2005/06/29 nakamoto 年組番表示を変更
 *	2005/06/30 nakamoto フォーム印刷を追加
 *	2005/07/22 nakamoto ブランクデータがある場合、SQL0420N発生に対応。
 *	2006/06/02 nakamoto NO001 歯科検診結果:高校フォームをカットし、中学フォームを共通とする。
 *	                    NO002 歯科検診結果:○要注意乳歯・・・0<要注意乳歯の条件を追加。○要観察歯・・・0<要観察歯の条件を追加
 *	2006/06/08 nakamoto NO003 歯科検診結果:その他を追加。
 */

public class KNJF031 {


    private static final Log log = LogFactory.getLog(KNJF031.class);

	/*----------------------------*
	 * HTTP Get リクエストの処理  *
	 *----------------------------*/
	public void svf_out(HttpServletRequest request, HttpServletResponse response)
	                 throws ServletException, IOException
	{

		Vrw32alp svf = new Vrw32alp(); 			//PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
		DB2UDB db2 = null;						//Databaseクラスを継承したクラス
		String param[] = new String[9];

	//	パラメータの取得
		try {
	        param[0] = request.getParameter("YEAR");         				//年度
			param[1] = request.getParameter("GAKKI");   					//学期 1,2,3

	        param[6] = request.getParameter("OUTPUT");         				//1:クラス,2:個人
	        param[7] = request.getParameter("OPT_OUT");         			//1:結果印刷,2:フォーム印刷(年組番氏名のみ印刷)
            //学年・組or学籍番号
    		String classcd[] = request.getParameterValues("CLASS_SELECTED");
			param[2] = "(";
			for( int ia=0 ; ia<classcd.length ; ia++ ){
				if(ia > 0) param[2] = param[2] + ",";
				if (param[6].equals("1")) param[2] = param[2] + "'" + classcd[ia] + "'";
				if (param[6].equals("2")) param[2] = param[2] + "'" + (classcd[ia]).substring(0,(classcd[ia]).indexOf("-")) + "'";
			}
			param[2] = param[2] + ")";

			param[3] = request.getParameter("CHECK1");                      //歯科検診結果
			param[4] = request.getParameter("CHECK2");                      //視力検査結果
			param[5] = request.getParameter("SCHOOL_JUDGE");                //H:高校、J:中学
			param[8] = request.getParameter("CTRL_DATE");                   //処理日付
		} catch( Exception ex ) {
			log.warn("parameter error!",ex);
		}

	//	print設定
		PrintWriter outstrm = new PrintWriter (response.getOutputStream());
		response.setContentType("application/pdf");

	//	svf設定
		svf.VrInit();						   	//クラスの初期化
		svf.VrSetSpoolFileStream(response.getOutputStream());   		//PDFファイル名の設定

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

for(int ia=0 ; ia<param.length ; ia++) log.debug("param["+ia+"]="+param[ia]);

		//SVF出力

        //-----結果印刷-----//

        //歯科検診結果(高校)
//   		if( param[3] != null && param[5].equals("H") && param[7].equals("1") ) //NO001
//    		if( printDentalCheckH(db2,svf,param) ) nonedata = true;//NO001

        //歯科検診結果(中学)
//   		if( param[3] != null && param[5].equals("J") && param[7].equals("1") ) //NO001
   		if( param[3] != null && param[7].equals("1") )
    		if( printDentalCheckJ(db2,svf,param) ) nonedata = true;

        //視力検査結果
   		if( param[4] != null && param[7].equals("1") )
    		if( printEyeTest(db2,svf,param) ) nonedata = true;

        //-----フォーム印刷-----//

        //歯科検診結果(高校)
//   		if( param[3] != null && param[5].equals("H") && param[7].equals("2") ) //NO001
//    		if( printFormOnly(db2,svf,param,"KNJF030_10.frm") ) nonedata = true;//NO001

        //歯科検診結果(中学)
//   		if( param[3] != null && param[5].equals("J") && param[7].equals("2") ) //NO001
   		if( param[3] != null && param[7].equals("2") )
    		if( printFormOnly(db2,svf,param,"KNJF030_11.frm") ) nonedata = true;

        //視力検査結果
   		if( param[4] != null && param[7].equals("2") )
    		if( printFormOnly(db2,svf,param,"KNJF030_12.frm") ) nonedata = true;

log.debug("nonedata="+nonedata);

	//	該当データ無し
		if( !nonedata ){
			svf.VrSetForm("MES001.frm", 0);
			svf.VrsOut("note" , "note");
			svf.VrEndPage();
		}

	// 	終了処理
		svf.VrQuit();
		db2.commit();
		db2.close();				//DBを閉じる
		outstrm.close();			//ストリームを閉じる

    }//doGetの括り


	/**歯科検診結果(高校)*/
	private boolean printDentalCheckH(DB2UDB db2,Vrw32alp svf,String param[])
	{
		boolean nonedata = false;
		try {
			PreparedStatement ps1 = db2.prepareStatement(statementDentalCheckH(param));
			ResultSet rs = ps1.executeQuery();

    		svf.VrSetForm("KNJF030_10.frm", 4);

			while( rs.next() ){
				//年組番・氏名
				//svf.VrsOut("HR_CLASS" , rs.getString("HR_NAME")+" "+String.valueOf(rs.getInt("ATTENDNO"))+"番" );
				svf.VrsOut("HR_CLASS" , String.valueOf(rs.getInt("GRADE")) + "年" +
                                              rs.getString("HR_NAMEABBV") + "組" +
                                              String.valueOf(rs.getInt("ATTENDNO")) + "番" );//---2005.06.29
				svf.VrsOut("NAME"     , rs.getString("NAME") );
				//歯科
                if (rs.getString("CHIRYO") != null)
    				svf.VrsOut("CHECK1"   , "○" );//治療
                if (rs.getString("KANSAT") != null)
    				svf.VrsOut("CHECK2"   , "○" );//観察
                if (rs.getString("SONOTA") != null)
    				svf.VrsOut("CHECK3"   , "○" );//その他

                putGengou1(db2, svf, "ERA_NAME", param);

				svf.VrEndRecord();
				nonedata = true;
			}
			rs.close();
			ps1.close();
			db2.commit();
		} catch( Exception ex ) {
			log.warn("printDentalCheckH read error!",ex);
		}
		return nonedata;

	}//printDentalCheckH()の括り


	/**歯科検診結果(中学)*/
	private boolean printDentalCheckJ(DB2UDB db2,Vrw32alp svf,String param[])
	{
		boolean nonedata = false;
		try {
			PreparedStatement ps1 = db2.prepareStatement(statementDentalCheckJ(param));
log.debug("statementDentalCheckJ start!");
			ResultSet rs = ps1.executeQuery();
log.debug("statementDentalCheckJ end!");

    		svf.VrSetForm("KNJF030_11.frm", 4);

			while( rs.next() ){
				//学校名
    			svf.VrsOut("SCHOOLNAME"  , (param[5].equals("H")) ? "近畿大学附属高等学校" : "近畿大学附属中学校" );
				//年組番・氏名
				//svf.VrsOut("HR_CLASS" , rs.getString("HR_NAME")+" "+String.valueOf(rs.getInt("ATTENDNO"))+"番" );
				svf.VrsOut("HR_CLASS" , String.valueOf(rs.getInt("GRADE")) + "年" +
                                              rs.getString("HR_NAMEABBV") + "組" +
                                              String.valueOf(rs.getInt("ATTENDNO")) + "番" );//---2005.06.29
				svf.VrsOut("NAME"     , rs.getString("NAME") );
				//歯科
                if (rs.getString("SHIRETU2") != null) svf.VrsOut("CHECK1"   , "○" ); //要観察(歯列)
                if (rs.getString("SHIRETU3") != null) svf.VrsOut("CHECK2"   , "○" ); //要精検(歯列)
                if (rs.getString("SHIKOU2") != null) svf.VrsOut("CHECK3"   , "○" );  //要観察(歯垢)
                if (rs.getString("SHIKOU3") != null) svf.VrsOut("CHECK4"   , "○" );  //要精検(歯垢)
                if (rs.getString("SHINIKU2") != null) svf.VrsOut("CHECK5"   , "○" ); //要観察(歯肉)
                if (rs.getString("SHINIKU3") != null) svf.VrsOut("CHECK6"   , "○" ); //要精検(歯肉)
                if (rs.getString("USHI") != null) svf.VrsOut("CHECK7"   , "○" );     //う歯
                if (rs.getString("CHUI") != null) svf.VrsOut("CHECK8"   , "○" );     //要注意乳歯
                if (rs.getString("KANSAT") != null) svf.VrsOut("CHECK9"   , "○" );   //要観察歯
                if (rs.getString("SONOTA") != null) svf.VrsOut("CHECK10"   , "○" );  //その他 NO003Add

                if (rs.getString("SCHREGNO") == null) svf.VrsOut("CHECK11"   , "○" );//未受診

                putGengou1(db2, svf, "ERA_NAME", param);

				svf.VrEndRecord();
				nonedata = true;
			}
			rs.close();
			ps1.close();
			db2.commit();
		} catch( Exception ex ) {
			log.warn("printDentalCheckJ read error!",ex);
		}
		return nonedata;

	}//printDentalCheckJ()の括り


	/**視力検査結果*/
	private boolean printEyeTest(DB2UDB db2,Vrw32alp svf,String param[])
	{
		boolean nonedata = false;
		try {
			PreparedStatement ps1 = db2.prepareStatement(statementEyeTest(param));
			ResultSet rs = ps1.executeQuery();

    		svf.VrSetForm("KNJF030_12.frm", 4);

			while( rs.next() ){
				//学校名
    			svf.VrsOut("SCHOOLNAME"  , (param[5].equals("H")) ? "近畿大学附属高等学校" : "近畿大学附属中学校" );
				//年組番・氏名
				//svf.VrsOut("HR_CLASS" , rs.getString("HR_NAME")+" "+String.valueOf(rs.getInt("ATTENDNO"))+"番" );
				svf.VrsOut("HR_CLASS" , String.valueOf(rs.getInt("GRADE")) + "年" +
                                              rs.getString("HR_NAMEABBV") + "組" +
                                              String.valueOf(rs.getInt("ATTENDNO")) + "番" );//---2005.06.29
				svf.VrsOut("NAME"     , rs.getString("NAME") );
				//視力
                if (rs.getString("R_RAGAN") != null || rs.getString("L_RAGAN") != null)
    				svf.VrsOut("CHECK1"   , "○" );
                if (rs.getString("R_KYOSEI") != null || rs.getString("L_KYOSEI") != null)
    				svf.VrsOut("CHECK2"   , "○" );

                putGengou1(db2, svf, "ERA_NAME", param);

				svf.VrEndRecord();
				nonedata = true;
			}
			rs.close();
			ps1.close();
			db2.commit();
		} catch( Exception ex ) {
			log.warn("printEyeTest read error!",ex);
		}
		return nonedata;

	}//printEyeTest()の括り


	/**
	 *	フォーム印刷
	 *
	 *  年組番氏名のみ印刷
	 */
	private boolean printFormOnly(DB2UDB db2,Vrw32alp svf,String param[],String FormId)
	{
		boolean nonedata = false;
		try {
			PreparedStatement ps1 = db2.prepareStatement(statementFormOnly(param));
			ResultSet rs = ps1.executeQuery();

    		svf.VrSetForm(FormId, 4);
            putGengou1(db2, svf, "ERA_NAME", param);

			while( rs.next() ){
				//学校名
    			svf.VrsOut("SCHOOLNAME"  , (param[5].equals("H")) ? "近畿大学附属高等学校" : "近畿大学附属中学校" );
				//年組番・氏名
				svf.VrsOut("HR_CLASS" , String.valueOf(rs.getInt("GRADE")) + "年" +
                                              rs.getString("HR_NAMEABBV") + "組" +
                                              String.valueOf(rs.getInt("ATTENDNO")) + "番" );
				svf.VrsOut("NAME"     , rs.getString("NAME") );

				svf.VrEndRecord();
				nonedata = true;
			}
			rs.close();
			ps1.close();
			db2.commit();
		} catch( Exception ex ) {
			log.warn("printFormOnly read error!",ex);
		}
		return nonedata;

	}//printFormOnly()の括り


	/**
	 *	歯科検診結果(高校)
	 *
	 */
	private String statementDentalCheckH(String param[])
	{
		StringBuffer stb = new StringBuffer();
		try {
            //在籍
			stb.append("WITH SCHNO AS ( ");
			stb.append("    SELECT SCHREGNO,YEAR,SEMESTER,GRADE,HR_CLASS,ATTENDNO ");
			stb.append("    FROM   SCHREG_REGD_DAT ");
			stb.append("    WHERE  YEAR='"+param[0]+"' AND SEMESTER='"+param[1]+"' AND ");
            if (param[6].equals("1")) //1:クラス
    			stb.append("       GRADE||HR_CLASS IN "+param[2]+" ) ");
            if (param[6].equals("2")) //2:個人
    			stb.append("       SCHREGNO IN "+param[2]+" ) ");
            //歯科
			stb.append(",SCHNO_DET AS ( ");
			stb.append("    SELECT SCHREGNO, ");
			stb.append("           REMAINBABYTOOTH,REMAINADULTTOOTH, ");
			stb.append("           OTHERDISEASECD,DENTISTREMARKCD ");
			stb.append("    FROM   MEDEXAM_TOOTH_DAT ");
			stb.append("    WHERE  YEAR='"+param[0]+"' AND ");
			stb.append("           (0 < REMAINBABYTOOTH OR ");
			stb.append("            0 < REMAINADULTTOOTH OR ");
//			stb.append("            1 < INT(VALUE(OTHERDISEASECD,'00')) OR ");//2005.07.01
//			stb.append("            0 < INT(VALUE(DENTISTREMARKCD,'00'))) ");//2005.07.01
			stb.append("           (0 < LENGTH(RTRIM(OTHERDISEASECD)) AND 1 < INT(VALUE(OTHERDISEASECD,'00'))) OR ");//2005.07.22
			stb.append("           (0 < LENGTH(RTRIM(DENTISTREMARKCD)) AND 0 < INT(VALUE(DENTISTREMARKCD,'00')))) ");//2005.07.22
			stb.append("    ) ");

            //メイン
			stb.append("SELECT T1.SCHREGNO,NAME,HR_NAMEABBV,T2.GRADE,T2.HR_CLASS,ATTENDNO, ");
			stb.append("       CASE WHEN 0 < REMAINBABYTOOTH OR 0 < REMAINADULTTOOTH THEN 1 ELSE NULL END AS CHIRYO, ");
//			stb.append("       CASE WHEN 0 < INT(VALUE(DENTISTREMARKCD,'00')) THEN '1' ELSE NULL END AS KANSAT, ");
//			stb.append("       CASE WHEN 1 < INT(VALUE(OTHERDISEASECD,'00')) THEN '1' ELSE NULL END AS SONOTA ");
			stb.append("       CASE WHEN 0 < LENGTH(RTRIM(DENTISTREMARKCD)) AND 0 < INT(VALUE(DENTISTREMARKCD,'00')) THEN '1' ELSE NULL END AS KANSAT, ");//2005.07.22
			stb.append("       CASE WHEN 0 < LENGTH(RTRIM(OTHERDISEASECD)) AND 1 < INT(VALUE(OTHERDISEASECD,'00')) THEN '1' ELSE NULL END AS SONOTA ");//2005.07.22
			stb.append("FROM   SCHNO_DET T1,SCHNO T2 ");
			stb.append("       LEFT JOIN SCHREG_REGD_HDAT T3 ON T3.YEAR=T2.YEAR AND T3.SEMESTER=T2.SEMESTER AND ");
			stb.append("                                        T3.GRADE=T2.GRADE AND T3.HR_CLASS=T2.HR_CLASS ");
			stb.append("       LEFT JOIN SCHREG_BASE_MST T4 ON T4.SCHREGNO=T2.SCHREGNO ");
			stb.append("WHERE  T1.SCHREGNO=T2.SCHREGNO ");
			stb.append("ORDER BY T2.GRADE,T2.HR_CLASS,ATTENDNO ");
		} catch( Exception e ){
			log.warn("statementDentalCheckH error!",e);
		}
		return stb.toString();

	}//statementDentalCheckH()の括り


	/**
	 *	歯科検診結果(中学)
	 *
	 */
	private String statementDentalCheckJ(String param[])
	{
		StringBuffer stb = new StringBuffer();
		try {
            //在籍
			stb.append("WITH SCHNO AS ( ");
			stb.append("    SELECT SCHREGNO,YEAR,SEMESTER,GRADE,HR_CLASS,ATTENDNO ");
			stb.append("    FROM   SCHREG_REGD_DAT ");
			stb.append("    WHERE  YEAR='"+param[0]+"' AND SEMESTER='"+param[1]+"' AND ");
            if (param[6].equals("1")) //1:クラス
    			stb.append("       GRADE||HR_CLASS IN "+param[2]+" ) ");
            if (param[6].equals("2")) //2:個人
    			stb.append("       SCHREGNO IN "+param[2]+" ) ");
            //歯科
			stb.append(",SCHNO_DET AS ( ");
			stb.append("    SELECT SCHREGNO, ");
			stb.append("           JAWS_JOINTCD,PLAQUECD,GUMCD, ");
			stb.append("           REMAINBABYTOOTH,REMAINADULTTOOTH, ");
			stb.append("           BRACK_BABYTOOTH,BRACK_ADULTTOOTH, ");//NO002Add
			stb.append("           OTHERDISEASECD,DENTISTREMARKCD ");
			stb.append("    FROM   MEDEXAM_TOOTH_DAT ");
			stb.append("    WHERE  YEAR='"+param[0]+"' AND ");
			stb.append("           ('01' < JAWS_JOINTCD OR ");
			stb.append("            '01' < PLAQUECD OR ");
			stb.append("            '01' < GUMCD OR ");
			stb.append("            0 < REMAINBABYTOOTH OR ");
			stb.append("            0 < REMAINADULTTOOTH OR ");
			stb.append("            0 < BRACK_BABYTOOTH OR ");//NO002Add
			stb.append("            0 < BRACK_ADULTTOOTH OR ");//NO002Add
//			stb.append("            '06' = OTHERDISEASECD OR ");
			stb.append("            '01' < OTHERDISEASECD OR ");//NO003Modify
			stb.append("            '01' = DENTISTREMARKCD) ");
			stb.append("    ) ");

            //メイン
			stb.append("SELECT T1.SCHREGNO,NAME,HR_NAMEABBV,T2.GRADE,T2.HR_CLASS,ATTENDNO, ");
			stb.append("       CASE WHEN JAWS_JOINTCD = '02' THEN '1' ELSE NULL END AS SHIRETU2, ");
			stb.append("       CASE WHEN JAWS_JOINTCD = '03' THEN '1' ELSE NULL END AS SHIRETU3, ");
			stb.append("       CASE WHEN PLAQUECD = '02' THEN '1' ELSE NULL END AS SHIKOU2, ");
			stb.append("       CASE WHEN PLAQUECD = '03' THEN '1' ELSE NULL END AS SHIKOU3, ");
			stb.append("       CASE WHEN GUMCD = '02' THEN '1' ELSE NULL END AS SHINIKU2, ");
			stb.append("       CASE WHEN GUMCD = '03' THEN '1' ELSE NULL END AS SHINIKU3, ");
			stb.append("       CASE WHEN 0 < REMAINBABYTOOTH OR 0 < REMAINADULTTOOTH THEN 1 ELSE NULL END AS USHI, ");
//			stb.append("       CASE WHEN OTHERDISEASECD = '06' THEN '1' ELSE NULL END AS CHUI, ");//NO002Modify
//			stb.append("       CASE WHEN DENTISTREMARKCD = '01' THEN '1' ELSE NULL END AS KANSAT ");//NO002Modify
			stb.append("       CASE WHEN OTHERDISEASECD = '06' OR 0 < BRACK_BABYTOOTH THEN '1' ELSE NULL END AS CHUI, ");
			stb.append("       CASE WHEN DENTISTREMARKCD = '01' OR 0 < BRACK_ADULTTOOTH THEN '1' ELSE NULL END AS KANSAT ");
			stb.append("      ,CASE WHEN '01' < OTHERDISEASECD THEN '1' ELSE NULL END AS SONOTA ");//NO003Add
			stb.append("FROM   SCHNO_DET T1,SCHNO T2 ");
			stb.append("       LEFT JOIN SCHREG_REGD_HDAT T3 ON T3.YEAR=T2.YEAR AND T3.SEMESTER=T2.SEMESTER AND ");
			stb.append("                                        T3.GRADE=T2.GRADE AND T3.HR_CLASS=T2.HR_CLASS ");
			stb.append("       LEFT JOIN SCHREG_BASE_MST T4 ON T4.SCHREGNO=T2.SCHREGNO ");
			stb.append("WHERE  T1.SCHREGNO=T2.SCHREGNO ");

			stb.append("UNION ");

            //メイン(未受診)
			stb.append("SELECT T1.SCHREGNO,NAME,HR_NAMEABBV,T2.GRADE,T2.HR_CLASS,ATTENDNO, ");
			stb.append("       CASE WHEN T1.SCHREGNO IS NULL THEN NULL ELSE '1' END AS SHIRETU2, ");
			stb.append("       CASE WHEN T1.SCHREGNO IS NULL THEN NULL ELSE '1' END AS SHIRETU3, ");
			stb.append("       CASE WHEN T1.SCHREGNO IS NULL THEN NULL ELSE '1' END AS SHIKOU2, ");
			stb.append("       CASE WHEN T1.SCHREGNO IS NULL THEN NULL ELSE '1' END AS SHIKOU3, ");
			stb.append("       CASE WHEN T1.SCHREGNO IS NULL THEN NULL ELSE '1' END AS SHINIKU2, ");
			stb.append("       CASE WHEN T1.SCHREGNO IS NULL THEN NULL ELSE '1' END AS SHINIKU3, ");
			stb.append("       CASE WHEN T1.SCHREGNO IS NULL THEN NULL ELSE 1 END AS USHI, ");
			stb.append("       CASE WHEN T1.SCHREGNO IS NULL THEN NULL ELSE '1' END AS CHUI, ");
			stb.append("       CASE WHEN T1.SCHREGNO IS NULL THEN NULL ELSE '1' END AS KANSAT ");
			stb.append("      ,CASE WHEN T1.SCHREGNO IS NULL THEN NULL ELSE '1' END AS SONOTA ");//NO003Add
			stb.append("FROM   SCHNO T2 ");
			stb.append("       LEFT JOIN MEDEXAM_TOOTH_DAT T1 ON T2.SCHREGNO=T1.SCHREGNO ");
			stb.append("       LEFT JOIN SCHREG_REGD_HDAT T3 ON T3.YEAR=T2.YEAR AND T3.SEMESTER=T2.SEMESTER AND ");
			stb.append("                                        T3.GRADE=T2.GRADE AND T3.HR_CLASS=T2.HR_CLASS ");
			stb.append("       LEFT JOIN SCHREG_BASE_MST T4 ON T4.SCHREGNO=T2.SCHREGNO ");
			stb.append("WHERE T1.SCHREGNO IS NULL ");

			stb.append("ORDER BY GRADE,HR_CLASS,ATTENDNO ");
		} catch( Exception e ){
			log.warn("statementDentalCheckJ error!",e);
		}
		return stb.toString();

	}//statementDentalCheckJ()の括り


	/**
	 *	視力検査結果
	 *
	 */
	private String statementEyeTest(String param[])
	{
		StringBuffer stb = new StringBuffer();
		try {
            //在籍
			stb.append("WITH SCHNO AS ( ");
			stb.append("    SELECT SCHREGNO,YEAR,SEMESTER,GRADE,HR_CLASS,ATTENDNO ");
			stb.append("    FROM   SCHREG_REGD_DAT ");
			stb.append("    WHERE  YEAR='"+param[0]+"' AND SEMESTER='"+param[1]+"' AND ");
            if (param[6].equals("1")) //1:クラス
    			stb.append("       GRADE||HR_CLASS IN "+param[2]+" ) ");
            if (param[6].equals("2")) //2:個人
    			stb.append("       SCHREGNO IN "+param[2]+" ) ");
            //視力
			stb.append(",SCHNO_DET AS ( ");
			stb.append("    SELECT SCHREGNO,R_BAREVISION,L_BAREVISION,R_VISION,L_VISION ");
			stb.append("    FROM   MEDEXAM_DET_DAT ");
			stb.append("    WHERE  YEAR='"+param[0]+"' AND ");
			stb.append("           ((0 < LENGTH(RTRIM(R_BAREVISION)) AND R_BAREVISION < '0.7') OR ");
			stb.append("            (0 < LENGTH(RTRIM(L_BAREVISION)) AND L_BAREVISION < '0.7') OR ");
			stb.append("            (0 < LENGTH(RTRIM(R_VISION)) AND R_VISION < '0.7') OR ");
			stb.append("            (0 < LENGTH(RTRIM(L_VISION)) AND L_VISION < '0.7') ) ");
			stb.append("    ) ");

            //メイン
			stb.append("SELECT T1.SCHREGNO,NAME,HR_NAMEABBV,T2.GRADE,T2.HR_CLASS,ATTENDNO, ");
			stb.append("       CASE WHEN 0 < LENGTH(RTRIM(R_BAREVISION)) AND R_BAREVISION < '0.7' THEN R_BAREVISION ELSE NULL END AS R_RAGAN, ");
			stb.append("       CASE WHEN 0 < LENGTH(RTRIM(L_BAREVISION)) AND L_BAREVISION < '0.7' THEN L_BAREVISION ELSE NULL END AS L_RAGAN, ");
			stb.append("       CASE WHEN 0 < LENGTH(RTRIM(R_VISION)) AND R_VISION < '0.7' THEN R_VISION ELSE NULL END AS R_KYOSEI, ");
			stb.append("       CASE WHEN 0 < LENGTH(RTRIM(L_VISION)) AND L_VISION < '0.7' THEN L_VISION ELSE NULL END AS L_KYOSEI ");
			stb.append("FROM   SCHNO_DET T1,SCHNO T2 ");
			stb.append("       LEFT JOIN SCHREG_REGD_HDAT T3 ON T3.YEAR=T2.YEAR AND T3.SEMESTER=T2.SEMESTER AND ");
			stb.append("                                        T3.GRADE=T2.GRADE AND T3.HR_CLASS=T2.HR_CLASS ");
			stb.append("       LEFT JOIN SCHREG_BASE_MST T4 ON T4.SCHREGNO=T2.SCHREGNO ");
			stb.append("WHERE  T1.SCHREGNO=T2.SCHREGNO ");
			stb.append("ORDER BY T2.GRADE,T2.HR_CLASS,ATTENDNO ");
		} catch( Exception e ){
			log.warn("statementEyeTest error!",e);
		}
		return stb.toString();

	}//statementEyeTest()の括り


	/**
	 *	フォーム印刷
	 *
	 *  年組番氏名のみ印刷
	 */
	private String statementFormOnly(String param[])
	{
		StringBuffer stb = new StringBuffer();
		try {
            //在籍
			stb.append("WITH SCHNO AS ( ");
			stb.append("    SELECT SCHREGNO,YEAR,SEMESTER,GRADE,HR_CLASS,ATTENDNO ");
			stb.append("    FROM   SCHREG_REGD_DAT ");
			stb.append("    WHERE  YEAR='"+param[0]+"' AND SEMESTER='"+param[1]+"' AND ");
            if (param[6].equals("1")) //1:クラス
    			stb.append("       GRADE||HR_CLASS IN "+param[2]+" ) ");
            if (param[6].equals("2")) //2:個人
    			stb.append("       SCHREGNO IN "+param[2]+" ) ");

            //メイン
			stb.append("SELECT T2.SCHREGNO,NAME,HR_NAMEABBV,T2.GRADE,T2.HR_CLASS,ATTENDNO ");
			stb.append("FROM   SCHNO T2 ");
			stb.append("       LEFT JOIN SCHREG_REGD_HDAT T3 ON T3.YEAR=T2.YEAR AND T3.SEMESTER=T2.SEMESTER AND ");
			stb.append("                                        T3.GRADE=T2.GRADE AND T3.HR_CLASS=T2.HR_CLASS ");
			stb.append("       LEFT JOIN SCHREG_BASE_MST T4 ON T4.SCHREGNO=T2.SCHREGNO ");
			stb.append("ORDER BY T2.GRADE,T2.HR_CLASS,ATTENDNO ");
		} catch( Exception e ){
			log.warn("statementFormOnly error!",e);
		}
		return stb.toString();

	}//statementFormOnly()の括り

    private void putGengou1(final DB2UDB db2, final Vrw32alp svf, final String field, final String param[]) {
        //元号(記入項目用)
        if (!StringUtils.isEmpty(param[8])) {
            final String setDate = param[8].replace('/', '-');
            final String[] gengouArray = KNJ_EditDate.tate_format4(db2, setDate);
            svf.VrsOut(field, gengouArray[0]);
        }
    }

}//クラスの括り
