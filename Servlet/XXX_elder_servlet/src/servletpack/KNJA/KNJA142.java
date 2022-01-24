// kanji=漢字
/*
 * $Id: 27e6aa644ac33ce264390e57057c41ca44cfaef8 $
 *
 * 作成日: 2005/03/23 16:07:30 - JST
 * 作成者: nakamoto
 *
 * Copyright(C) 2005-2008 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJA;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nao_package.KenjaProperties;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_Get_Info;

/**
 *
 *	学校教育システム 賢者 [学籍管理]
 *
 *					＜ＫＮＪＡ１４２＞  身分証明書（大宮開成）
 *
 *	2005/03/23 nakamoto 作成日
 *	2005/03/31 nakamoto 仮身分証明書を出力するとき、留年フラグが’１’の生徒は、
 *                      学籍基礎データから氏名、学籍住所データから住所を表示 ---NO003
 *	2005/06/10 nakamoto 仮身分証明書を学籍基礎から出力できるよう修正・・・仮身分証明書(在籍)を追加
 **/

public class KNJA142 {

    private static final Log log = LogFactory.getLog(KNJA142.class);
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    private String _useAddrField2;

	/*----------------------------*
	 * HTTP Get リクエストの処理  *
	 *----------------------------*/
	public void svf_out(HttpServletRequest request, HttpServletResponse response)
	                 throws ServletException, IOException
	{

		Vrw32alp svf = new Vrw32alp(); 			//PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
		DB2UDB db2 = null;						//Databaseクラスを継承したクラス
		String param[] = new String[12];

	//	パラメータの取得
		try {
	        param[0] = request.getParameter("YEAR");         				//年度
	        param[1] = request.getParameter("GAKKI");         				//学期
	        param[2] = request.getParameter("GRADE_HR_CLASS");         		//学年＋組
	        param[3] = request.getParameter("OUTPUT");         			    //フォーム種別---1:仮身分証明書,2:身分証明書,3:仮身分証明書(在籍)
			String sdate = request.getParameter("TERM_SDATE");         		//有効期限(開始)
			String edate = request.getParameter("TERM_EDATE");         		//有効期限(終了)
			param[5] = sdate.replace('/','-');
			param[6] = edate.replace('/','-');
	        param[7] = request.getParameter("DOCUMENTROOT");         		// '/usr/local/deve_oomiya/src'
			// 学籍番号の指定
			String schno[] = request.getParameterValues("category_selected");//学籍番号
			int i = 0;
			param[4] = "(";
			while(i < schno.length){
				if(schno[i] == null ) break;
				if(i > 0) param[4] = param[4] + ",";
				param[4] = param[4] + "'" + schno[i] + "'";
				i++;
			}
			param[4] = param[4] + ")";
            _useAddrField2 = request.getParameter("useAddrField2");
		} catch( Exception ex ) {
			log.error("parameter error!");
		}

	//	print設定
		PrintWriter outstrm = new PrintWriter (response.getOutputStream());
		response.setContentType("application/pdf");

	//	svf設定
		svf.VrInit();						   		//クラスの初期化
		svf.VrSetSpoolFileStream(response.getOutputStream());  		//PDFファイル名の設定

	//	ＤＢ接続
		try {
			db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
			db2.open();
		} catch( Exception ex ) {
			log.error("DB2 open error!");
			return;
		}


	//	ＳＶＦ作成処理
		PreparedStatement ps1 = null;
		PreparedStatement ps2 = null;
		boolean nonedata = false; 								//該当データなしフラグ
		setHeader(db2,svf,param);
for(int i=0 ; i<param.length ; i++) log.debug("param["+i+"]="+param[i]);
		//SQL作成
		try {
			ps1 = db2.prepareStatement(preStat1(param));		//生徒情報
			ps2 = db2.prepareStatement(preStat2(param));		//組名称
		} catch( Exception ex ) {
			log.error("DB2 prepareStatement set error!");
		}
		//SVF出力
		if( setSvfout(db2,svf,param,ps1) ){					//帳票出力のメソッド
			nonedata = true;
		}

log.debug("nonedata = "+nonedata);

	//	該当データ無し
		if( !nonedata ){
			svf.VrSetForm("MES001.frm", 0);
			svf.VrsOut("note" , "note");
			svf.VrEndPage();
		}

	// 	終了処理
		svf.VrQuit();
		preStatClose(ps1,ps2);		//preparestatementを閉じる
		db2.commit();
		db2.close();				//DBを閉じる
		outstrm.close();			//ストリームを閉じる 

    }//doGetの括り



	/** 事前処理 **/
	private void setHeader(
		DB2UDB db2,
		Vrw32alp svf,
		String param[]
	) {
		KNJ_Get_Info getinfo = new KNJ_Get_Info();
		KNJ_Get_Info.ReturnVal returnval = null;

		svf.VrSetForm("KNJA142.frm", 4);

	//	見出し
		try {
    		param[11] = param[0]+"年度";//年度
		} catch( Exception e ){
			log.error("setHeader set error!");
		}

	//	写真データ
		try {
			returnval = getinfo.Control(db2);
	        param[8] = returnval.val4;      //格納フォルダ
	        param[9] = returnval.val5;      //拡張子
		} catch( Exception e ){
			log.error("setHeader set error!");
		}

		getinfo = null;
		returnval = null;
	}

	/**帳票出力**/
	private boolean setSvfout(
		DB2UDB db2,
		Vrw32alp svf,
		String param[],
		PreparedStatement ps1
	) {
		boolean nonedata = false;
		try {
			ResultSet rs = ps1.executeQuery();

			Calendar cals = Calendar.getInstance();
			Calendar calb = Calendar.getInstance();
			cals.setTime(sdf.parse( param[5] ));				//有効期限(開始)をCalendar calに変換
			int cals_year = cals.get(Calendar.YEAR);			//calsの年を取得
   			int cals_day  = cals.get(Calendar.DAY_OF_YEAR);		//calsの年の何日目を取得
            String birth_age = "";                              //年齢

			String photo = "";                  //顔写真
			String stamp = "SCHOOLSTAMP.bmp";   //学校印
			String photo_check = "";            //写真データ存在チェック用
			String stamp_check = "";            //学校印データ存在チェック用

			while( rs.next() ){
        		svf.VrsOut("SCHOOLDIV","中学校" );                            //学校区分
        		svf.VrsOut("NENDO"	,param[11] );                           //年度
//        		svf.VrsOut("TITLE"	,(param[3].equals("1")) ? "仮身分証明書" : "身分証明書" );//タイトル
        		svf.VrsOut("TITLE"	,(param[3].equals("2")) ? "身分証明書" : "仮身分証明書" );//タイトル---2005.06.10Modify
        		svf.VrsOut("SDATE"	,KNJ_EditDate.h_format_Seireki_N(param[5]) + KNJ_EditDate.h_format_JP_MD(param[5]));   //有効期限(開始)
        		svf.VrsOut("FDATE"	,KNJ_EditDate.h_format_Seireki_N(param[6]) + KNJ_EditDate.h_format_JP_MD(param[6]));   //有効期限(終了)
				svf.VrsOut("SCHREGNO" ,rs.getString("SCHREGNO") );            //学籍番号
                String attendno = (rs.getInt("ATTENDNO") != 0) ? String.valueOf(rs.getInt("ATTENDNO")) : "　";
				svf.VrsOut("CLASS" 	,rs.getString("HR_NAME") + attendno + "番" );//年組番
				svf.VrsOut("NAME" 	,rs.getString("NAME") );                //氏名
                //生年月日・年齢
                if (rs.getString("BIRTHDAY") != null) {
        			calb.setTime(sdf.parse( rs.getString("BIRTHDAY") ));//生年月日をCalendar calに変換
        			int calb_year = calb.get(Calendar.YEAR);			//calbの年を取得
        			int calb_day  = calb.get(Calendar.DAY_OF_YEAR);		//calbの年の何日目を取得
                    if (calb_day <= cals_day) birth_age = "　(" + String.valueOf(cals_year - calb_year) + "才)";
                    else                      birth_age = "　(" + String.valueOf(cals_year - calb_year - 1) + "才)";
    				svf.VrsOut("BIRTHDAY" ,KNJ_EditDate.h_format_Seireki_N(rs.getString("BIRTHDAY")) + KNJ_EditDate.h_format_JP_MD(rs.getString("BIRTHDAY")) + "生" + birth_age );
                }
                //住所１または住所２が２０文字超えたら(50)にセット
                final String addr2 = rs.getString("ADDR2");
                final String addr1 = rs.getString("ADDR1");
                if ("1".equals(_useAddrField2) &&
                        ((addr1 != null && addr1.length() > 25) || 
                         (addr2 != null && addr2.length() > 25))
                        ) {
                    svf.VrsOut("ADDRESS1_3" ,addr1 );  //住所１(60)
                    svf.VrsOut("ADDRESS2_3" ,addr2 );  //住所２(60)
                } else if ((addr1 != null && addr1.length() > 20) || 
                            (addr2 != null && addr2.length() > 20))
                {
    				svf.VrsOut("ADDRESS1_2" ,addr1 );  //住所１(50)
    				svf.VrsOut("ADDRESS2_2" ,addr2 );  //住所２(50)
                } else {
    				svf.VrsOut("ADDRESS1_1" ,addr1 );  //住所１(40)
    				svf.VrsOut("ADDRESS2_1" ,addr2 );  //住所２(40)
                }
                //顔写真
			    photo = "P" + rs.getString("SCHREGNO") + "." + param[9];
                photo_check = param[7] + "/" + param[8] + "/" + photo;
			    File f1 = new File(photo_check);
		        if (f1.exists()) 
		            svf.VrsOut("PHOTO_BMP"    , photo_check );//顔写真
                //学校印
                stamp_check = param[7] + "/" + param[8] + "/" + stamp;
			    File f2 = new File(stamp_check);
		        if (f2.exists()) 
    		        svf.VrsOut("STAMP_BMP"    , stamp_check );//学校印

				svf.VrEndRecord();//１行出力
				nonedata = true;
			}
			rs.close();
    		db2.commit();
		} catch( Exception ex ) {
			log.error("setSvfout set error!");
		}
		return nonedata;
	}



	/**生徒情報**/
	private String preStat1(String param[])
	{
		StringBuffer stb = new StringBuffer();
	//	パラメータ（学籍番号）
		try {
            /*---NO003---OLD↓---
            //住所
			stb.append("WITH SCH_ADDR AS ( ");
			stb.append("    SELECT SCHREGNO,MAX(ISSUEDATE) AS ISSUEDATE ");
			stb.append("    FROM   SCHREG_ADDRESS_DAT ");
			stb.append("    GROUP BY SCHREGNO ) ");

            //メイン
			stb.append("SELECT T1.SCHREGNO, ");
			stb.append("       VALUE(T1.ATTENDNO,'0') AS ATTENDNO, ");
			stb.append("       T2.NAME, ");
			stb.append("       T2.BIRTHDAY, ");
            if (param[3].equals("1") && (param[2].substring(0,2)).equals("01")) {//仮身分証明書:新入生
    			stb.append("   T2.ADDR1, ");
    			stb.append("   T2.ADDR2 ");
            } else {
    			stb.append("   T3.ADDR1, ");
    			stb.append("   T3.ADDR2 ");
            }
            if (param[3].equals("1")) //仮身分証明書
    			stb.append("FROM   CLASS_FORMATION_DAT T1 ");
            if (param[3].equals("2")) //身分証明書
    			stb.append("FROM   SCHREG_REGD_DAT T1 ");
            if (param[3].equals("1") && (param[2].substring(0,2)).equals("01")) {//仮身分証明書:新入生
    			stb.append("       INNER JOIN FRESHMAN_DAT T2 ON T2.ENTERYEAR=T1.YEAR AND T2.SCHREGNO=T1.SCHREGNO ");
            } else {
    			stb.append("       INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO=T1.SCHREGNO ");
    			stb.append("       INNER JOIN SCHREG_ADDRESS_DAT T3 ON T3.SCHREGNO=T1.SCHREGNO ");
    			stb.append("       INNER JOIN SCH_ADDR T4 ON T4.SCHREGNO=T3.SCHREGNO AND T4.ISSUEDATE=T3.ISSUEDATE ");
            }
            ---NO003---OLD↑---*/
            //---NO003---NEW↓---
            //住所
			stb.append("WITH SCH_ADDR1 AS ( ");
			stb.append("    SELECT SCHREGNO,MAX(ISSUEDATE) AS ISSUEDATE ");
			stb.append("    FROM   SCHREG_ADDRESS_DAT ");
			stb.append("    WHERE  SCHREGNO IN "+param[4]+" ");
			stb.append("    GROUP BY SCHREGNO ) ");
			stb.append(",SCH_ADDR2 AS ( ");
			stb.append("    SELECT SCHREGNO,ISSUEDATE,ADDR1,ADDR2 ");
			stb.append("    FROM   SCHREG_ADDRESS_DAT ");
			stb.append("    WHERE  SCHREGNO IN "+param[4]+" ) ");
			stb.append(",SCH_ADDR AS ( ");
			stb.append("    SELECT W2.SCHREGNO,W2.ADDR1,W2.ADDR2 ");
			stb.append("    FROM   SCH_ADDR1 W1,SCH_ADDR2 W2 ");
			stb.append("    WHERE  W1.SCHREGNO=W2.SCHREGNO AND ");
			stb.append("           W1.ISSUEDATE=W2.ISSUEDATE ) ");

            //メイン
			stb.append("SELECT T1.SCHREGNO, ");
			stb.append("       VALUE(T1.ATTENDNO,'0') AS ATTENDNO, ");
            stb.append("       T4.HR_NAME, ");
            if (param[3].equals("1")) {//仮身分証明書
    			stb.append("       CASE WHEN T1.GRADE = '01' AND VALUE(T1.REMAINGRADE_FLG,'0') = '0' ");
    			stb.append("            THEN T0.NAME ELSE T2.NAME END AS NAME, ");
    			stb.append("       CASE WHEN T1.GRADE = '01' AND VALUE(T1.REMAINGRADE_FLG,'0') = '0' ");
    			stb.append("            THEN T0.BIRTHDAY ELSE T2.BIRTHDAY END AS BIRTHDAY, ");
    			stb.append("       CASE WHEN T1.GRADE = '01' AND VALUE(T1.REMAINGRADE_FLG,'0') = '0' ");
    			stb.append("            THEN T0.ADDR1 ELSE T3.ADDR1 END AS ADDR1, ");
    			stb.append("       CASE WHEN T1.GRADE = '01' AND VALUE(T1.REMAINGRADE_FLG,'0') = '0' ");
    			stb.append("            THEN T0.ADDR2 ELSE T3.ADDR2 END AS ADDR2 ");
    			stb.append("FROM   CLASS_FORMATION_DAT T1 ");
            }
//            if (param[3].equals("2")) {//身分証明書
            if (param[3].equals("2") || param[3].equals("3")) {//身分証明書---仮身分証明書(在籍)---2005.06.10Modify
    			stb.append("       T2.NAME, T2.BIRTHDAY, T3.ADDR1, T3.ADDR2 ");
    			stb.append("FROM   SCHREG_REGD_DAT T1 ");
            }
   			stb.append("       LEFT JOIN FRESHMAN_DAT T0 ON T0.ENTERYEAR=T1.YEAR AND T0.SCHREGNO=T1.SCHREGNO ");
   			stb.append("       LEFT JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO=T1.SCHREGNO ");
   			stb.append("       LEFT JOIN SCH_ADDR T3 ON T3.SCHREGNO=T1.SCHREGNO ");
            stb.append("       LEFT JOIN SCHREG_REGD_HDAT T4 ON T4.YEAR=T1.YEAR AND T4.SEMESTER=T1.SEMESTER AND T4.GRADE=T1.GRADE AND T4.HR_CLASS=T1.HR_CLASS ");//NO004
            //---NO003---NEW↑---
			stb.append("WHERE  T1.YEAR='"+param[0]+"' AND ");
			stb.append("       T1.SEMESTER='"+param[1]+"' AND ");
			stb.append("       T1.SCHREGNO IN "+param[4]+" ");
			stb.append("ORDER BY ");
            stb.append("       T1.GRADE, ");
            stb.append("       T1.HR_CLASS, ");
            stb.append("       T1.ATTENDNO ");
		} catch( Exception e ){
			log.error("preStat1 error!");
		}
		return stb.toString();

	}//preStat1()の括り


	/**組名称**/
	private String preStat2(String param[])
	{
		StringBuffer stb = new StringBuffer();
		try {
			stb.append("SELECT HR_NAME, HR_NAMEABBV ");
   			stb.append("FROM   SCHREG_REGD_HDAT ");
			stb.append("WHERE  YEAR='"+param[0]+"' AND ");
			stb.append("       SEMESTER='"+param[1]+"' AND ");
			stb.append("       GRADE||HR_CLASS = '"+param[2]+"' ");
		} catch( Exception e ){
			log.error("preStat2 error!");
		}
		return stb.toString();

	}//preStat2()の括り


	/**PrepareStatement close**/
	private void preStatClose(
		PreparedStatement ps1,
		PreparedStatement ps2
	) {
		try {
			ps1.close();
			ps2.close();
		} catch( Exception e ){
			log.error("preStatClose error!");
		}
	}//preStatClose()の括り



}//クラスの括り
