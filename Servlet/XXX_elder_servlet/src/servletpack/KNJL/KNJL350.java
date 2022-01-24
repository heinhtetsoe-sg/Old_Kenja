package servletpack.KNJL;

import java.io.IOException;
import java.io.PrintWriter;

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
 *	学校教育システム 賢者 [入試管理]
 *
 *					＜ＫＮＪＬ３５０＞  入試試験統計資料(実数)
 *
 *	2005/02/18 m-yama 作成日
 *  2005/12/29 Modify yamashiro 
 *      ○元の「入学試験統計資料（実数）」KNJL350を「入学試験入学状況調査」へ変更 --NO001
 *        ○地域別内訳（志願者数・受験者数・合格者数・入学手続・入学者数）を追加
 *        ○入試区分を特定した場合、地域別内訳は受付データの入試区分で抽出するが、統計は実数とする
 *        ○実数を指定した場合、地域別内訳、統計ともに実数とする
 *  2006/01/05 Modify yamashiro 
 *      ○実数を指定した場合、総数も地域別も、元の「入学試験統計資料（実数）」の仕様でデータを集計する --NO002
 *      ○回数を指定した場合、集計仕様はKNJL325と同様とし、KNJL325にない項目は仕様書のとおりとする --NO002
 *      ○回数を指定した場合、入学手続者と入学者は試験区分の古い方でカウントする --NO002
 *  2006/01/10 Modify yamashiro 
 *      ○試験回数指定の場合、特待合格および特待入学の集計値の不具合を修正 --NO003
 *        ○特待合格は受付データ(HONORDIV=1)を集計
 *        ○特待入学は基礎データ(HONORDIV=1)を集計
 *  2006/01/19 Modify yamashiro 
 *      ○「前回までの合格者を対象者に含む」の対象集計を、合格者・不合格者・特待合格・手続者数・延期者数・特待入学・入学者とする  --NO004
 *  2007/01/17 Modify m-yama 
 *      ○NO005 延期手続者出力を追加。
 *
 **/

public class KNJL350 {

    private static final Log log = LogFactory.getLog(KNJL350.class);

	/*----------------------------*
	 * HTTP Get リクエストの処理  *
	 *----------------------------*/
	public void svf_out(HttpServletRequest request, HttpServletResponse response)
	                 throws ServletException, IOException
	{

		Vrw32alp svf = new Vrw32alp(); 			//PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
		DB2UDB db2 = null;						//Databaseクラスを継承したクラス
		String param[] = new String[13];

	//	パラメータの取得
		try {
	        param[0] = request.getParameter("YEAR");         //年度
	        param[7] = request.getParameter("TESTDV");       //入試区分 --NO001
	        param[8] = request.getParameter("APDIV");        //入試制度 --NO001
            if( request.getParameter("CHECK1") != null ) param[11] = request.getParameter("CHECK1");  //NO002 前回までの合格者対象者に含める
//param[11] = "on";
		} catch( Exception ex ) {
			log.error("parameter error!");
		}

	//	print設定
		PrintWriter outstrm = new PrintWriter (response.getOutputStream());
		response.setContentType("application/pdf");

	//	svf設定
		int ret = svf.VrInit();						   		//クラスの初期化
        if (false && 0 != ret) { ret = 0; }
		ret = svf.VrSetSpoolFileStream(response.getOutputStream());  		//PDFファイル名の設定

	//	ＤＢ接続
		try {
			db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
			db2.open();
		} catch( Exception ex ) {
			log.error("DB2 open error!");
			return;
		}


	//	ＳＶＦ作成処理
		//NO002 PreparedStatement nd  = null;
		//NO002 PreparedStatement ps  = null;
		//NO002 PreparedStatement ps1 = null;
		//NO002 PreparedStatement ps2 = null;
		//NO002 PreparedStatement ps3 = null;
		//NO002 PreparedStatement ps4 = null;
		//NO002 PreparedStatement ps5 = null;
		//NO002 PreparedStatement ps6 = null;
		//NO002 PreparedStatement ps7 = null;
		//NO002 PreparedStatement ps8 = null;
		//NO002 PreparedStatement ps9 = null;
		boolean nonedata = false; 								//該当データなしフラグ
		setHeader(db2,svf,param);
//for(int i=0 ; i<1 ; i++) log.debug("param["+i+"]="+param[i]);
		//SQL作成
		try {
			//--NO001 nd  = db2.prepareStatement(preStatnd(param));		//入試制度preparestatement
			//NO002 ps  = db2.prepareStatement(preStat(param));			//出願者数preparestatement
			//NO002 ps1 = db2.prepareStatement(preStat1(param));		//受験者数preparestatement
			//NO002 ps2 = db2.prepareStatement(preStat2(param));		//合格者数preparestatement
			//NO002 ps3 = db2.prepareStatement(preStat3(param));		//不合格者数preparestatement
			//NO002 ps4 = db2.prepareStatement(preStat4(param));		//特待合格preparestatement
			//NO002 ps5 = db2.prepareStatement(preStat5(param));		//手続者数preparestatement
			//NO002 ps6 = db2.prepareStatement(preStat6(param));		//延期者数preparestatement
			//NO002 ps7 = db2.prepareStatement(preStat7(param));		//特待入学preparestatement
			//NO002 ps8 = db2.prepareStatement(preStat8(param));		//入学者数preparestatement
			//-NO001 ps9 = db2.prepareStatement(preStat9(param));		//総頁数preparestatement
		} catch( Exception ex ) {
			log.error("DB2 prepareStatement set error!");
		}
		//SVF出力
		//NO002 if (setSvfMain(db2,svf,param,nd,ps,ps1,ps2,ps3,ps4,ps5,ps6,ps7,ps8,ps9)) nonedata = true;	//帳票出力のメソッド
		if (setSvfMain(db2,svf,param)) nonedata = true;	//帳票出力のメソッド
		//NO002 preStatClose(nd,ps,ps1,ps2,ps3,ps4,ps5,ps6,ps7,ps8,ps9);	//preparestatementを閉じる --NO001
        if( param[7].equals("99") )
    		setSvfSubB( db2, svf, param );	//帳票出力のメソッド --NO002 ココでsvf.VrEndPage()
        else
    		setSvfSub( db2, svf, param );	//帳票出力のメソッド --NO001 ココでsvf.VrEndPage()

	//	該当データ無し
		if( !nonedata ){
			ret = svf.VrSetForm("MES001.frm", 0);
			ret = svf.VrsOut("note" , "note");
			ret = svf.VrEndPage();
		}

	// 	終了処理
		ret = svf.VrQuit();
		//NO002 preStatClose(nd,ps,ps1,ps2,ps3,ps4,ps5,ps6,ps7,ps8,ps9);		//preparestatementを閉じる
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
		int ret = 0;
        if (false && 0 != ret) { ret = 0; }
		param[1] = KenjaProperties.gengou(Integer.parseInt(param[0]))+"年度";

	//	作成日(現在処理日)の取得
		try {
			returnval = getinfo.Control(db2);
			param[2] = KNJ_EditDate.h_format_JP(returnval.val3);
		} catch( Exception e ){
			log.error("setHeader set error!");
		}

    //  入試区分 --NO001
        ResultSet rs = null;
        if( ! param[7].equals("99") ){
            try {
                db2.query( "SELECT NAME AS NAME1,CLASSDIV AS NAMESPARE3 FROM ENTEXAM_TESTDIV_MST WHERE ENTEXAMYEAR = '"+param[0]+"' AND TESTDIV = '" + param[7] + "'" );
                rs = db2.getResultSet();
                if (rs.next()) {
                    param[9] = rs.getString("NAME1");
                    param[12] = rs.getString("NAMESPARE3");
                }
                rs.close();
                db2.commit();
            } catch( Exception e ){
                log.error("NAME OF APPLICANTDIV set error!");
            }
        }

    //  入試制度 --NO001
        if( param[8] != null ){
            try {
                db2.query( "SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'L003' AND NAMECD2 = '" + param[8] + "'" );
                rs = db2.getResultSet();
                if( rs.next() )param[10] = rs.getString("NAME1");
                rs.close();
                db2.commit();
            } catch( Exception e ){
                log.error("NAME OF TESTDIV set error!");
            }
        }

		getinfo = null;
		returnval = null;
	}



	/**
     *  svf print 印刷処理
     *  2005/12/29 Modify --NO001
     */
	private boolean setSvfMain(
		DB2UDB db2,
		Vrw32alp svf,
		String param[]
		//NO002 PreparedStatement nd,
		//NO002 PreparedStatement ps,
		//NO002 PreparedStatement ps1,
		//NO002 PreparedStatement ps2,
		//NO002 PreparedStatement ps3,
		//NO002 PreparedStatement ps4,
		//NO002 PreparedStatement ps5,
		//NO002 PreparedStatement ps6,
		//NO002 PreparedStatement ps7,
		//NO002 PreparedStatement ps8,
		//NO002 PreparedStatement ps9
	) {
		boolean nonedata = false;
		int ret = 0;
        if (false && 0 != ret) { ret = 0; }
		//--NO001 int reccnt = 0;				//合計レコード数
		//--NO001 int pagecnt = 1;			//現在ページ数
		//--NO001 int gyo = 1;				//現在ページ数の判断用（行）
		ret = svf.VrSetForm("KNJL350.frm", 1);	//セットフォーム
		//--NO001 setTotalPage(db2,svf,param,ps9);					//総ページ数メソッド
		try {
			setTitle(svf,param);		//見出しメソッド
            /*** NO001
			ResultSet rs = nd.executeQuery();
			String fname = new String();
			while( rs.next() ){
				param[3] = rs.getString("NAME1");
				param[4] = rs.getString("NAMECD2");
				//レコードを出力
				if (reccnt > 0) ret = svf.VrEndRecord();
				//２０行超えたとき、ページ数カウント
				if (gyo > 20) {
					gyo = 1;
					pagecnt++;
				}
				//ヘッダ
				ret = svf.VrsOut("PAGE"		,String.valueOf(pagecnt));		//現在ページ数
				//パラメータの設定
				ps.setString( 1, param[4] );
				ps1.setString( 1, param[4] );
				ps1.setString( 2, param[4] );
				ps2.setString( 1, param[4] );
				ps3.setString( 1, param[4] );
				ps4.setString( 1, param[4] );
				ps5.setString( 1, param[4] );
				ps6.setString( 1, param[4] );
				ps7.setString( 1, param[4] );
				ps8.setString( 1, param[4] );
				ret = svf.VrsOut("ENTORYNAME" ,rs.getString("NAME1"));			//入試制度
            *** */
			//NO002 String fname = null;
            nonedata = true;
            //NO002 fname = "APPICANT";     //--NO001
            //NO002 if (setSvfout1(db2,svf,param,ps,fname)) nonedata = true;			//出願者数メソッド
            //NO002 fname = "EXAMINEE";     //--NO001
            //NO002 if (setSvfout1(db2,svf,param,ps1,fname)) nonedata = true;			//受験者数メソッド
for(int i=0 ; i<param.length ; i++) log.debug("param["+i+"]="+param[i]);
            //NO002 int ketu = Integer.parseInt(param[5]) - Integer.parseInt(param[6]);		//欠席者数
            //NO002 String setketu = null;										//欠席者数
            //NO002 setketu = Integer.toString(ketu);
//log.debug("set"+setketu);
            //NO002 ret = svf.VrsOut("ABSENTEE" ,setketu);							//欠席者数  NO001
            //NO002 fname = "SUCCESS";      //--NO001
            //NO002 if (setSvfout1(db2,svf,param,ps2,fname)) nonedata = true;			//合格者数メソッド
            //NO002 fname = "FAIL";         //--NO001
            //NO002 if (setSvfout1(db2,svf,param,ps3,fname)) nonedata = true;			//不合者数メソッド
            //NO002 fname = "HONORS_SUC";   //--NO001
            //NO002 if (setSvfout1(db2,svf,param,ps4,fname)) nonedata = true;		//特待合格メソッド
            //NO002 fname = "PROCEDURE";    //--NO001
            //NO002 if (setSvfout1(db2,svf,param,ps5,fname)) nonedata = true;		//手続者数メソッド
            //NO002 fname = "ADJOURNMENT";  //--NO001
            //NO002 if (setSvfout1(db2,svf,param,ps6,fname)) nonedata = true;	//延期者数メソッド
            //NO002 fname = "HONORS_ENT";   //--NO001
            //NO002 if (setSvfout1(db2,svf,param,ps7,fname)) nonedata = true;	//特待入学メソッド
            //NO002 fname = "ENTRANCE";     //--NO001
            //NO002 if (setSvfout1(db2,svf,param,ps8,fname)) nonedata = true;		//入学者数メソッド
            /* *** NO001
				//レコード数カウント
				reccnt++;
				//現在ページ数判断用
				gyo++;
			}
			if (nonedata) ret = svf.VrEndPage();
			rs.close();
			db2.commit();
            *** */
		} catch( Exception ex ) {
			log.error("setSvfMain set error!");
		}
		return nonedata;
	}


	/**各データをセット）**/
	private boolean setSvfout1(
		DB2UDB db2,
		Vrw32alp svf,
		String param[],
		PreparedStatement ps1,
		String field	//セットフィールド名
	) {
		boolean nonedata = false;
		int ret = 0;
        if (false && 0 != ret) { ret = 0; }
//log.debug("ps1="+ps1.toString());
		try {
			ResultSet rs = ps1.executeQuery();
			while( rs.next() ){
				//明細
				if(param[5] == null){
					param[5] = rs.getString("CNT");
				}else if(param[6] == null){
					param[6] = rs.getString("CNT");
				}
				ret = svf.VrsOut( field ,rs.getString("CNT"));
				nonedata = true;
//log.debug("fi"+field);
//log.debug("data"+rs.getString("CNT"));
			}
			//出力
			rs.close();
			db2.commit();
		} catch( Exception ex ) {
			log.error("setSvfout1 set error!");
		}
		return nonedata;
	}

	/**  総ページ数をセット ** --NO001
	private void setTotalPage(
		DB2UDB db2,
		Vrw32alp svf,
		String param[],
		PreparedStatement ps9
	) {
		int ret = 0;
		try {
			ResultSet rs = ps9.executeQuery();

			while( rs.next() ){
				if (rs.getString("TEST_CNT") != null)
					ret = svf.VrsOut("TOTAL_PAGE"	,rs.getString("TEST_CNT"));
			}
			rs.close();
			db2.commit();
		} catch( Exception ex ) {
			log.error("setTotalPage set error!");
		}
	}
    *** */

	/**見出し項目をセット**/
	private void setTitle(
		Vrw32alp svf,
		String param[]
	) {
		int ret = 0;
        if (false && 0 != ret) { ret = 0; }
		try {
				ret = svf.VrsOut("NENDO"	 ,param[1]);		//年度
				//--NO001 ret = svf.VrsOut("FIELD1"	 ,param[2]);		//作成日
				ret = svf.VrsOut( "DATE"	 ,param[2]);		//作成日 --NO001

                if( param[7].equals("99") )ret = svf.VrsOut( "TITLE", "（実数）" );	//タイトル --NO001
                else ret = svf.VrsOut( "TESTDIV", param[9] );	//タイトル --NO001

				ret = svf.VrsOut( "ENTEXAM_SYSTEM"	 ,param[10]);		//入試制度 --NO001
		} catch( Exception ex ) {
			log.error("setTitle set error!");
		}

	}
	/**入試制度　取得** --NO001
	private String preStatnd(String param[])
	{
		StringBuffer stb = new StringBuffer();
		try {
			//名称マスタ
			stb.append("SELECT ");
			stb.append("    NAMECD2,NAME1 ");
			stb.append("FROM ");
			stb.append("    V_NAME_MST ");
			stb.append("WHERE ");
			stb.append("    YEAR = '"+param[0]+"' ");
			stb.append("    AND NAMECD1 = 'L003' ");

		} catch( Exception e ){
			log.error("preStatnd error!");
		}
		return stb.toString();

	}//preStatnd()の括り
    *** */


	/** 
     *  出願者数　取得
     **
	private String preStat(String param[])
	{
		StringBuffer stb = new StringBuffer();
		try {
			//基礎データ
			stb.append("SELECT  COUNT(*) AS CNT ");
			stb.append("FROM    ENTEXAM_APPLICANTBASE_DAT ");
			stb.append("WHERE   ENTEXAMYEAR = '"+param[0]+"' ");
//NO001     ---------->
			//stb.append("        AND APPLICANTDIV = ? ");
			stb.append(    "AND APPLICANTDIV = '" + param[8] + "' ");
            //if( ! param[7].equals("99") )
    		//	stb.append("AND TESTDIV = '" + param[7] + "' ");
//NO001     <----------
		} catch( Exception e ){
			log.error( "preStat error!", e );
            log.debug( "ps = " + stb.toString() );
		}
//log.debug( "ps = " + stb.toString() );
		return stb.toString();

	}//preStat()の括り */

	/**受験者数　取得**
	private String preStat1(String param[])
	{
		StringBuffer stb = new StringBuffer();
		try {
			//基礎データ
			stb.append("WITH ATABLE(examno) AS (SELECT ");
			stb.append("    EXAMNO ");
			stb.append("FROM ");
			stb.append("    ENTEXAM_APPLICANTBASE_DAT ");
			stb.append("WHERE ");
			stb.append("    ENTEXAMYEAR = '"+param[0]+"' ");
//NO001     ---------->
			//stb.append("    AND APPLICANTDIV = ? ");
			stb.append(    "AND APPLICANTDIV = '" + param[8] + "' ");
//NO001     <----------
			stb.append("), BTABLE(EXAMNO) AS ( ");
			stb.append("SELECT DISTINCT EXAMNO ");
			stb.append("FROM ENTEXAM_RECEPT_DAT ");
			stb.append("WHERE ENTEXAMYEAR = '"+param[0]+"' ");
//NO001     ---------->
			//stb.append("    AND APPLICANTDIV = ? ");
			stb.append(    "AND APPLICANTDIV = '" + param[8] + "' ) ");
//NO001     <----------
			stb.append("SELECT COUNT(*) AS CNT ");
			stb.append("FROM ATABLE w1,BTABLE w2 ");
			stb.append("WHERE w1.EXAMNO = w2.EXAMNO ");
		} catch( Exception e ){
			log.error( "preStat error!", e );
            log.debug( "ps = " + stb.toString() );
		}
		return stb.toString();

	}//preStat1()の括り*/

	/**合格者数　取得**
	private String preStat2(String param[])
	{
		StringBuffer stb = new StringBuffer();
		try {
			//基礎データ
			stb.append("SELECT ");
			stb.append("    COUNT(*) AS CNT ");
			stb.append("FROM ");
			stb.append("    ENTEXAM_APPLICANTBASE_DAT ");
			stb.append("WHERE ");
			stb.append("    ENTEXAMYEAR = '"+param[0]+"' AND ");
//NO001     ---------->
			stb.append("    ( JUDGEMENT = '1' OR SPECIAL_MEASURES in ('1','2') ) ");    // --NO001
			//stb.append("    JUDGEMENT = '1' OR ");
			//stb.append("    SPECIAL_MEASURES in ('1','2') ");
			//stb.append("        AND APPLICANTDIV = ? ");
			stb.append(    "AND APPLICANTDIV = '" + param[8] + "' ");
            //if( ! param[7].equals("99") )
    		//	stb.append("AND TESTDIV = '" + param[7] + "' ");
//NO001     <----------
		} catch( Exception e ){
			log.error( "preStat error!", e );
            log.debug( "ps = " + stb.toString() );
		}
		return stb.toString();

	}//preStat2()の括り*/

	/**不合格者数　取得**
	private String preStat3(String param[])
	{
		StringBuffer stb = new StringBuffer();
		try {
			//基礎データ
			stb.append("SELECT ");
			stb.append("    COUNT(*) AS CNT ");
			stb.append("FROM ");
			stb.append("    ENTEXAM_APPLICANTBASE_DAT ");
			stb.append("WHERE ");
			stb.append("    ENTEXAMYEAR = '"+param[0]+"' AND ");
			stb.append("    JUDGEMENT = '2' ");
//NO001     ---------->
			//stb.append("        AND APPLICANTDIV = ? ");
			stb.append(    "AND APPLICANTDIV = '" + param[8] + "' ");
            //if( ! param[7].equals("99") )
    		//	stb.append("AND TESTDIV = '" + param[7] + "' ");
//NO001     <----------
		} catch( Exception e ){
			log.error( "preStat error!", e );
            log.debug( "ps = " + stb.toString() );
		}
		return stb.toString();

	}//preStat3()の括り*/

	/**特待合格　取得**
	private String preStat4(String param[])
	{
		StringBuffer stb = new StringBuffer();
		try {
			//基礎データ
			stb.append("WITH ATABLE (EXAMNO) AS (SELECT DISTINCT ");
			stb.append("    EXAMNO ");
			stb.append("FROM ");
			stb.append("    ENTEXAM_RECEPT_DAT ");
			stb.append("WHERE ");
			stb.append("    ENTEXAMYEAR = '"+param[0]+"' AND ");
			stb.append("    HONORDIV = '1' ");
//NO001     ---------->
			//stb.append("        AND APPLICANTDIV = ? ");
			stb.append(    "AND APPLICANTDIV = '" + param[8] + "' ");
            if( ! param[7].equals("99") )   //NO002 REVIVAL
    			stb.append("AND TESTDIV = '" + param[7] + "' ");
//NO001     <----------
			stb.append(") ");
			stb.append("SELECT COUNT(*) AS CNT FROM ATABLE  ");
		} catch( Exception e ){
			log.error( "preStat error!", e );
            log.debug( "ps = " + stb.toString() );
		}
		return stb.toString();

	}//preStat4()の括り*/

	/**手続者数　取得**
	private String preStat5(String param[])
	{
		StringBuffer stb = new StringBuffer();
		try {
			//基礎データ
			stb.append("SELECT ");
			stb.append("    COUNT(*) AS CNT ");
			stb.append("FROM ");
			stb.append("    ENTEXAM_APPLICANTBASE_DAT ");
			stb.append("WHERE ");
			stb.append("    ENTEXAMYEAR = '"+param[0]+"' AND ");
			stb.append("    PROCEDUREDIV = '1' ");
//NO001     ---------->
			//stb.append("        AND APPLICANTDIV = ? ");
			stb.append(    "AND APPLICANTDIV = '" + param[8] + "' ");
            //if( ! param[7].equals("99") )
    		//	stb.append("AND TESTDIV = '" + param[7] + "' ");
//NO001     <----------
		} catch( Exception e ){
			log.error( "preStat error!", e );
            log.debug( "ps = " + stb.toString() );
		}
		return stb.toString();

	}//preStat5()の括り*/

	/**延期者数　取得**
	private String preStat6(String param[])
	{
		StringBuffer stb = new StringBuffer();
		try {
			//受付データ
			stb.append("WITH ATABLE (EXAMNO) AS (SELECT DISTINCT ");
			stb.append("    EXAMNO ");
			stb.append("FROM ");
			stb.append("    ENTEXAM_RECEPT_DAT ");
			stb.append("WHERE ");
			stb.append("    ENTEXAMYEAR = '"+param[0]+"' AND ");
			stb.append("    ADJOURNMENTDIV = '1' ");
//NO001     ---------->
			//stb.append("        AND APPLICANTDIV = ? ");
			stb.append(    "AND APPLICANTDIV = '" + param[8] + "' ");
            //if( ! param[7].equals("99") )
    		//	stb.append("AND TESTDIV = '" + param[7] + "' ");
//NO001     <----------
			stb.append(") ");
			stb.append("SELECT COUNT(*) AS CNT FROM ATABLE  ");
		} catch( Exception e ){
			log.error( "preStat error!", e );
            log.debug( "ps = " + stb.toString() );
		}
		return stb.toString();

	}//preStat6()の括り*/

	/**特待入学　取得**
	private String preStat7(String param[])
	{
		StringBuffer stb = new StringBuffer();
		try {
			//基礎データ
			stb.append("SELECT ");
			stb.append("    COUNT(*) AS CNT ");
			stb.append("FROM ");
			stb.append("    ENTEXAM_APPLICANTBASE_DAT ");
			stb.append("WHERE ");
			stb.append("    ENTEXAMYEAR = '"+param[0]+"' AND ");
			stb.append("    HONORDIV = '1' ");
//NO001     ---------->
			//stb.append("        AND APPLICANTDIV = ? ");
			stb.append(    "AND APPLICANTDIV = '" + param[8] + "' ");
            //if( ! param[7].equals("99") )
    		//	stb.append("AND TESTDIV = '" + param[7] + "' ");
//NO001     <----------
		} catch( Exception e ){
			log.error( "preStat error!", e );
            log.debug( "ps = " + stb.toString() );
		}
		return stb.toString();

	}//preStat7()の括り*/

	/**入学者数　取得**
	private String preStat8(String param[])
	{
		StringBuffer stb = new StringBuffer();
		try {
			//基礎データ
			stb.append("SELECT ");
			stb.append("    COUNT(*) AS CNT ");
			stb.append("FROM ");
			stb.append("    ENTEXAM_APPLICANTBASE_DAT ");
			stb.append("WHERE ");
			stb.append("    ENTEXAMYEAR = '"+param[0]+"' AND ");
			stb.append("    ENTDIV = '1' ");
//NO001     ---------->
			//stb.append("        AND APPLICANTDIV = ? ");
			stb.append(    "AND APPLICANTDIV = '" + param[8] + "' ");
            //if( ! param[7].equals("99") )
    		//	stb.append("AND TESTDIV = '" + param[7] + "' ");
//NO001     <----------
		} catch( Exception e ){
			log.error( "preStat error!", e );
            log.debug( "ps = " + stb.toString() );
		}
		return stb.toString();

	}//preStat8()の括り*/


	/**総ページ数を取得**
	private String preStat9(String param[])
	{
		StringBuffer stb = new StringBuffer();
	//	パラメータ（なし）
		try {
			stb.append("SELECT CASE WHEN MOD(COUNT(*),20) > 0 THEN COUNT(*)/20 + 1 ELSE COUNT(*)/20 END TEST_CNT ");
			stb.append("     FROM   V_NAME_MST ");
			stb.append("     WHERE  YEAR='"+param[0]+"' AND NAMECD1 = 'L003' ");
		} catch( Exception e ){
			log.error("preStat1 error!");
		}
		return stb.toString();

	}//preStat1()の括り*/

	/**PrepareStatement close**
	private void preStatClose(
		PreparedStatement nd,
		PreparedStatement ps,
		PreparedStatement ps1,
		PreparedStatement ps2,
		PreparedStatement ps3,
		PreparedStatement ps4,
		PreparedStatement ps5,
		PreparedStatement ps6,
		PreparedStatement ps7,
		PreparedStatement ps8,
		PreparedStatement ps9
	) {
		try {
			if( nd != null )nd.close();
			if( ps != null )ps.close();
			if( ps1 != null )ps1.close();
			if( ps2 != null )ps2.close();
			if( ps3 != null )ps3.close();
			if( ps4 != null )ps4.close();
			if( ps5 != null )ps5.close();
			if( ps6 != null )ps6.close();
			if( ps7 != null )ps7.close();
			if( ps8 != null )ps8.close();
			if( ps9 != null )ps9.close();
		} catch( Exception e ){
			log.error("preStatClose error!");
		}
	}//preStatClose()の括り*/



	/**
     *  svf print 地区別内訳印刷処理
     *  2005/12/29 Build NO001
     *  2006/01/05 Modify NO002
     */
	private void setSvfSub( DB2UDB db2,
		                    Vrw32alp svf,
		                    String param[] )
    {
		int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        AreaStatistics_Base areaobj = null;
        //int num = 0;

		try {
            areaobj = new AreaStatistics_1();       //志願者
            areaobj.ps = db2.prepareStatement( areaobj.preStatSub( param ) );
            areaobj.svfprintSub( db2, svf, param, 1 );
            ret = svf.VrsOut( "APPICANT",  String.valueOf( areaobj.totalcnt ) );  //NO002

            areaobj = new AreaStatistics_2();       //受験者
            areaobj.ps = db2.prepareStatement( areaobj.preStatSub( param ) );
            areaobj.svfprintSub( db2, svf, param, 2 );
            ret = svf.VrsOut( "EXAMINEE",  String.valueOf( areaobj.totalcnt ) );  //NO002

            areaobj = new AreaStatistics_3();       //欠席者者
            areaobj.ps = db2.prepareStatement( areaobj.preStatSub( param ) );
            areaobj.svfprintSub( db2, svf, param, 0 );
            ret = svf.VrsOut( "ABSENTEE",  String.valueOf( areaobj.totalcnt ) );  //NO002 欠席者数

            areaobj = new AreaStatistics_4();       //合格者
            areaobj.ps = db2.prepareStatement( areaobj.preStatSub( param ) );
            areaobj.svfprintSub( db2, svf, param, 3 );
            ret = svf.VrsOut( "SUCCESS",  String.valueOf( areaobj.totalcnt ) );  //NO002

            areaobj = new AreaStatistics_4_1();       //合格者(普通)
            areaobj.ps = db2.prepareStatement( areaobj.preStatSub( param ) );
            areaobj.svfprintSub( db2, svf, param, 0 );
            ret = svf.VrsOut( "SUCCESS1",  String.valueOf( areaobj.totalcnt ) );  //NO002

            areaobj = new AreaStatistics_4_2();       //合格者(アップ)
            areaobj.ps = db2.prepareStatement( areaobj.preStatSub( param ) );
            areaobj.svfprintSub( db2, svf, param, 0 );
            ret = svf.VrsOut( "SUCCESS2",  String.valueOf( areaobj.totalcnt ) );  //NO002

            areaobj = new AreaStatistics_4_3();       //合格者(スライド)
            areaobj.ps = db2.prepareStatement( areaobj.preStatSub( param ) );
            areaobj.svfprintSub( db2, svf, param, 0 );
            ret = svf.VrsOut( "SUCCESS3",  String.valueOf( areaobj.totalcnt ) );  //NO002

            areaobj = new AreaStatistics_5();       //不合格者
            areaobj.ps = db2.prepareStatement( areaobj.preStatSub( param ) );
            areaobj.svfprintSub( db2, svf, param, 0 );
            ret = svf.VrsOut( "FAIL",  String.valueOf( areaobj.totalcnt ) );  //NO002

            areaobj = new AreaStatistics_6();       //特待合格者
            areaobj.ps = db2.prepareStatement( areaobj.preStatSub( param ) );
            areaobj.svfprintSub( db2, svf, param, 0 );
log.debug("ret="+String.valueOf( areaobj.totalcnt ));
            ret = svf.VrsOut( "HONORS_SUC",  String.valueOf( areaobj.totalcnt ) );  //NO002

            areaobj = new AreaStatistics_7();       //入学手続者
            areaobj.ps = db2.prepareStatement( areaobj.preStatSub( param ) );
            areaobj.svfprintSub( db2, svf, param, 4 );
            ret = svf.VrsOut( "PROCEDURE",  String.valueOf( areaobj.totalcnt ) );  //NO002

//            areaobj = new AreaStatistics_8();       //延期者
//            areaobj.ps = db2.prepareStatement( areaobj.preStatSub( param ) );
//            areaobj.svfprintSub( db2, svf, param, 0 );  //NO005
//            ret = svf.VrsOut( "ADJOURNMENT",  String.valueOf( areaobj.totalcnt ) );  //NO002

            areaobj = new AreaStatistics_9();       //特待入学者
            areaobj.ps = db2.prepareStatement( areaobj.preStatSub( param ) );
            areaobj.svfprintSub( db2, svf, param, 0 );
            ret = svf.VrsOut( "HONORS_ENT",  String.valueOf( areaobj.totalcnt ) );  //NO002

            areaobj = new AreaStatistics_10();       //入学者
            areaobj.ps = db2.prepareStatement( areaobj.preStatSub( param ) );
            areaobj.svfprintSub( db2, svf, param, 5 );
            ret = svf.VrsOut( "ENTRANCE",  String.valueOf( areaobj.totalcnt ) );  //NO002
            AreaStatistics_10 areaobj10 = (AreaStatistics_10) areaobj;
            areaobj10.svfprintSub2(db2, svf, param);

            if ("2".equals(param[12])) {
                ret = svf.VrsOut("SLIDE_UP",  "スライド合格者数内訳"); //英数特科
                areaobj = new AreaStatistics_11_4();
                areaobj.ps = db2.prepareStatement( areaobj.preStatSub( param ) );
                areaobj.svfprintSub( db2, svf, param, 6 );
            } else if ("1".equals(param[12])) {
                ret = svf.VrsOut("SLIDE_UP",  "アップ合格者数内訳");   //特別進学
                areaobj = new AreaStatistics_11_3();
                areaobj.ps = db2.prepareStatement( areaobj.preStatSub( param ) );
                areaobj.svfprintSub( db2, svf, param, 6 );
            } else {
                ret = svf.VrsOut("SLIDE_UP",  "非正規合格者数内訳"); //特待生選抜
                areaobj = new AreaStatistics_11_4();
                areaobj.ps = db2.prepareStatement( areaobj.preStatSub( param ) );
                areaobj.svfprintSub( db2, svf, param, 6 );
            }

            areaobj = new AreaStatistics_12();  //特別アップ合格
            areaobj.ps = db2.prepareStatement( areaobj.preStatSub( param ) );
            areaobj.svfprintSub( db2, svf, param, 0 );
            ret = svf.VrsOut( "ADJOURNMENT",  String.valueOf( areaobj.totalcnt ) );

            ret = svf.VrEndPage();
		} catch( Exception ex ) {
			log.error( "setSvfSub error!", ex );
		}
	}



	/**
     *  svf print 地区別内訳印刷処理 実数
     *  2006/01/05 Build NO002
     */
	private void setSvfSubB( DB2UDB db2,
		                    Vrw32alp svf,
		                    String param[] )
    {
		int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        AreaStatistics_Base areaobj = null;
        int cnt1 = 0;

		try {
            areaobj = new AreaStatistics_1B();       //志願者
            areaobj.ps = db2.prepareStatement( areaobj.preStatSub( param ) );
            areaobj.svfprintSub( db2, svf, param, 1 );
            ret = svf.VrsOut( "APPICANT",  String.valueOf( areaobj.totalcnt ) );  //NO002
            cnt1 = areaobj.totalcnt;

            areaobj = new AreaStatistics_2B();       //受験者
            areaobj.ps = db2.prepareStatement( areaobj.preStatSub( param ) );
            areaobj.svfprintSub( db2, svf, param, 2 );
                ret = svf.VrsOut( "EXAMINEE",  String.valueOf( areaobj.totalcnt ) );  //NO002

            if( areaobj.totalcnt < cnt1 )
                ret = svf.VrsOut( "ABSENTEE",  String.valueOf( cnt1 - areaobj.totalcnt ) );  //NO002
            else
                ret = svf.VrsOut( "ABSENTEE",  "0" );  //NO002

            areaobj = new AreaStatistics_4B();       //合格者
            areaobj.ps = db2.prepareStatement( areaobj.preStatSub( param ) );
            areaobj.svfprintSub( db2, svf, param, 3 );
            final int tmpTotalcnt = areaobj.totalcnt;

            areaobj = new AreaStatistics_4B_1();       //合格者(普通)
            areaobj.ps = db2.prepareStatement( areaobj.preStatSub( param ) );
            areaobj.svfprintSub( db2, svf, param, 0 );
            ret = svf.VrsOut( "SUCCESS1",  String.valueOf( areaobj.totalcnt ) );  //NO002

            areaobj = new AreaStatistics_4B_2();       //合格者(アップ)
            areaobj.ps = db2.prepareStatement( areaobj.preStatSub( param ) );
            areaobj.svfprintSub( db2, svf, param, 0 );
            ret = svf.VrsOut( "SUCCESS2",  String.valueOf( areaobj.totalcnt ) );  //NO002

            areaobj = new AreaStatistics_4B_3();       //合格者(スライド)
            areaobj.ps = db2.prepareStatement( areaobj.preStatSub( param ) );
            areaobj.svfprintSub( db2, svf, param, 0 );
            ret = svf.VrsOut( "SUCCESS3",  String.valueOf( areaobj.totalcnt ) );  //NO002
            ret = svf.VrsOut( "SUCCESS",  String.valueOf( tmpTotalcnt - areaobj.totalcnt ) ); //合格者(計)

            areaobj = new AreaStatistics_5B();       //不合格者
            areaobj.ps = db2.prepareStatement( areaobj.preStatSub( param ) );
            areaobj.svfprintSub( db2, svf, param, 0 );
            ret = svf.VrsOut( "FAIL",  String.valueOf( areaobj.totalcnt ) );  //NO002

            areaobj = new AreaStatistics_6B();       //特待合格者
            areaobj.ps = db2.prepareStatement( areaobj.preStatSub( param ) );
            areaobj.svfprintSub( db2, svf, param, 0 );
log.debug("ret="+String.valueOf( areaobj.totalcnt ));
            ret = svf.VrsOut( "HONORS_SUC",  String.valueOf( areaobj.totalcnt ) );  //NO002

            areaobj = new AreaStatistics_7B();       //入学手続者
            areaobj.ps = db2.prepareStatement( areaobj.preStatSub( param ) );
            areaobj.svfprintSub( db2, svf, param, 4 );
            ret = svf.VrsOut( "PROCEDURE",  String.valueOf( areaobj.totalcnt ) );  //NO002

//            areaobj = new AreaStatistics_8B();       //延期者
//            areaobj.ps = db2.prepareStatement( areaobj.preStatSub( param ) );
//            areaobj.svfprintSub( db2, svf, param, 0 ); //NO005
//            ret = svf.VrsOut( "ADJOURNMENT",  String.valueOf( areaobj.totalcnt ) );  //NO002

            areaobj = new AreaStatistics_9B();       //特待入学者
            areaobj.ps = db2.prepareStatement( areaobj.preStatSub( param ) );
            areaobj.svfprintSub( db2, svf, param, 0 );
            ret = svf.VrsOut( "HONORS_ENT",  String.valueOf( areaobj.totalcnt ) );  //NO002

            areaobj = new AreaStatistics_10B();       //入学者
            areaobj.ps = db2.prepareStatement( areaobj.preStatSub( param ) );
            areaobj.svfprintSub( db2, svf, param, 5 );
            ret = svf.VrsOut( "ENTRANCE",  String.valueOf( areaobj.totalcnt ) );  //NO002
            AreaStatistics_10B areaobj10b = (AreaStatistics_10B) areaobj;
            areaobj10b.svfprintSub2(db2, svf, param);

            ret = svf.VrsOut("SLIDE_UP",  "スライド／アップ／非正規合格者数内訳");
            areaobj = new AreaStatistics_11B();
            areaobj.ps = db2.prepareStatement( areaobj.preStatSub( param ) );
            areaobj.svfprintSub( db2, svf, param, 6 );

            areaobj = new AreaStatistics_12B();  //特別アップ合格
            areaobj.ps = db2.prepareStatement( areaobj.preStatSub( param ) );
            areaobj.svfprintSub( db2, svf, param, 0 );
            ret = svf.VrsOut( "ADJOURNMENT",  String.valueOf( areaobj.totalcnt ) );

            areaobj = new AreaStatistics_13B();  //特別／繰上合格
            areaobj.ps = db2.prepareStatement( areaobj.preStatSub( param ) );
            areaobj.svfprintSub( db2, svf, param, 0 );
            ret = svf.VrsOut( "SPECIAL_MEASURES",  String.valueOf( areaobj.totalcnt ) );

            ret = svf.VrEndPage();
		} catch( Exception ex ) {
			log.error( "setSvfSub error!", ex );
		}
	}



/**
 *  地区別内訳印刷のクラス 基本
 *  2005/12/29 Build NO001
 */
private abstract class AreaStatistics_Base
{
    PreparedStatement ps;
	ResultSet rs = null;
    abstract String preStatSub( String param[] );
    int totalcnt = 0;   //NO002

	/**
     *  svf print 地区別内訳印刷処理
     *  2005/12/29 Build NO001
     */
	private void svfprintSub( DB2UDB db2
		                     ,Vrw32alp svf
		                     ,String param[]
                             ,int num )
    {
		int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        int kennai = 0;
        int sex = 0;

        totalcnt = 0;  //NO002

		try {
            rs = ps.executeQuery();
            while ( rs.next() ){
                if( sex != Integer.parseInt( rs.getString("SEX") ) ){
                    kennai = 0;
                    sex = Integer.parseInt( rs.getString("SEX") );
                }
                if( ! rs.getString("FS_AREA_CD").equals("X") ){
                    ret = svf.VrsOutn( "COUNT" + num + "_" + Integer.parseInt( rs.getString("FS_AREA_CD") )
                                        , ( ( Integer.parseInt( rs.getString("SEX") ) == 9 )? 3: Integer.parseInt( rs.getString("SEX") ) )
                                        , rs.getString("CNT") );
                    if( Integer.parseInt( rs.getString("FS_AREA_CD") ) == 1 )kennai = Integer.parseInt( rs.getString("CNT") );  //県内
                } else {
                    ret = svf.VrsOutn( "TOTAL" + num + "_1" 
                                        , ( ( Integer.parseInt( rs.getString("SEX") ) == 9 )? 3: Integer.parseInt( rs.getString("SEX") ) )  //県外合計
                                        , String.valueOf( Integer.parseInt( rs.getString("CNT") ) - kennai ) );
                    ret = svf.VrsOutn( "TOTAL" + num + "_2" 
                                        , ( ( Integer.parseInt( rs.getString("SEX") ) == 9 )? 3: Integer.parseInt( rs.getString("SEX") ) )  //総合計
                                        , rs.getString("CNT") );

                    if( Integer.parseInt( rs.getString("SEX") ) == 9 )totalcnt = Integer.parseInt( rs.getString("CNT") );  //NO002
                }
            }
            rs.close();
            db2.commit();
		} catch( Exception ex ) {
			log.error( "setSvfSub error!", ex );
		}
	}

}


/**
 *  地区別内訳印刷のクラス 志願者
 *  2005/12/29 Build NO001
 */
private class AreaStatistics_1 extends AreaStatistics_Base
{
	/**
     *  地区別 志願者数 取得
     *  2005/12/29 Build NO001
     *  2006/01/05 Modify NO002
     */
	String preStatSub( String param[] )
	{
		StringBuffer stb = new StringBuffer();
		try {
            //志願者データから対象者を抽出 NO002
            stb.append("WITH ");
            stb.append("DESIRE_A AS( ");
            stb.append(   "SELECT  EXAMNO ");
            stb.append(   "FROM    ENTEXAM_DESIRE_DAT ");
			stb.append(   "WHERE   ENTEXAMYEAR = '" + param[0] + "' ");
			stb.append(       "AND APPLICANTDIV = '" + param[8] + "' ");
			stb.append(       "AND APPLICANT_DIV = '1'  ");               //志願者データ：志願者区分
   			stb.append(       "AND TESTDIV = '" + param[7] + "' ");       //志願者データ：入試区分
            stb.append(   "GROUP BY EXAMNO ");
            stb.append(") ");

			stb.append("SELECT  VALUE(SEX,'9') AS SEX ");
            stb.append(       ",VALUE(VALUE(FS_AREA_CD,'08'),'X') AS FS_AREA_CD ");
            stb.append(       ",COUNT(*) AS CNT ");
			stb.append("FROM    ENTEXAM_APPLICANTBASE_DAT T1 ");
            stb.append(       ",DESIRE_A T3 ");
			stb.append("WHERE   T1.ENTEXAMYEAR = '" + param[0] + "' ");
			stb.append(    "AND T1.APPLICANTDIV = '" + param[8] + "' ");
            stb.append(    "AND T3.EXAMNO = T1.EXAMNO ");
            stb.append("GROUP BY GROUPING SETS ((SEX, VALUE(FS_AREA_CD,'08')),(SEX),(VALUE(FS_AREA_CD,'08')),()) ");
            stb.append("ORDER BY SEX,FS_AREA_CD ");
		} catch( Exception e ){
			log.error( "preStat error!", e );
            log.debug( "ps = " + stb.toString() );
		}
		return stb.toString();
	}
}


/**
 *  地区別内訳印刷のクラス 受験者
 *  2005/12/29 Build NO001
 */
private class AreaStatistics_2 extends AreaStatistics_Base
{
	/**
     *  地区別 受験者数 取得
     *  2005/12/29 Build NO001
     *  2006/01/05 Modify NO002
     */
	String preStatSub( String param[] )
	{
		StringBuffer stb = new StringBuffer();
		try {
            //志願者データから対象者を抽出 NO002
            stb.append("WITH ");
            stb.append("DESIRE_A AS( ");
            stb.append(   "SELECT  EXAMNO ");
            stb.append(   "FROM    ENTEXAM_DESIRE_DAT ");
			stb.append(   "WHERE   ENTEXAMYEAR = '" + param[0] + "' ");
			stb.append(       "AND APPLICANTDIV = '" + param[8] + "' ");
			stb.append(       "AND APPLICANT_DIV = '1'  ");               //志願者データ：志願者区分
			stb.append(       "AND EXAMINEE_DIV = '1' ");			      //志願者データ：受験者区分
   			stb.append(       "AND TESTDIV = '" + param[7] + "' ");       //志願者データ：入試区分
            stb.append(   "GROUP BY EXAMNO ");
            stb.append(") ");

			stb.append("SELECT  VALUE(SEX,'9') AS SEX ");
            stb.append(       ",VALUE(VALUE(FS_AREA_CD,'08'),'X') AS FS_AREA_CD ");
            stb.append(       ",COUNT(*) AS CNT ");
			stb.append("FROM    ENTEXAM_APPLICANTBASE_DAT T1 ");
            stb.append(       ",DESIRE_A T3 ");
			stb.append("WHERE   T1.ENTEXAMYEAR = '" + param[0] + "' ");
			stb.append(    "AND T1.APPLICANTDIV = '" + param[8] + "' ");
            stb.append(    "AND EXISTS(SELECT 'X' FROM ENTEXAM_RECEPT_DAT T2 ");
            stb.append(               "WHERE  T2.ENTEXAMYEAR = '" + param[0] + "' ");
            stb.append(                  "AND T2.APPLICANTDIV = '" + param[8] + "' ");
   			stb.append(                  "AND T2.TESTDIV = '" + param[7] + "' ");
            stb.append(                  "AND T2.EXAMNO = T1.EXAMNO ");
            stb.append(               "GROUP BY T2.EXAMNO) ");
            stb.append(    "AND T3.EXAMNO = T1.EXAMNO ");
            stb.append("GROUP BY GROUPING SETS ((SEX, VALUE(FS_AREA_CD,'08')),(SEX),(VALUE(FS_AREA_CD,'08')),()) ");
            stb.append("ORDER BY SEX,FS_AREA_CD ");
		} catch( Exception e ){
			log.error( "preStat error!", e );
            log.debug( "ps = " + stb.toString() );
		}
		return stb.toString();
	}
}


/**
 *  地区別内訳印刷のクラス 欠席者
 *  2006/01/05 Build NO002
 */
private class AreaStatistics_3 extends AreaStatistics_Base
{
	/**
     *  地区別 欠席者数 取得
     *  2006/01/05 Build NO002
     */
	String preStatSub( String param[] )
	{
		StringBuffer stb = new StringBuffer();
		try {
            //志願者データから対象者を抽出 NO002
            stb.append("WITH ");
            stb.append("DESIRE_A AS( ");
            stb.append(   "SELECT  EXAMNO ");
            stb.append(   "FROM    ENTEXAM_DESIRE_DAT ");
			stb.append(   "WHERE   ENTEXAMYEAR = '" + param[0] + "' ");
			stb.append(       "AND APPLICANTDIV = '" + param[8] + "' ");
			stb.append(       "AND APPLICANT_DIV = '1'  ");               //志願者データ：志願者区分
			stb.append(       "AND EXAMINEE_DIV = '2' ");			      //志願者データ：受験者区分
   			stb.append(       "AND TESTDIV = '" + param[7] + "' ");       //志願者データ：入試区分
            stb.append(   "GROUP BY EXAMNO ");
            stb.append(") ");

			stb.append("SELECT  '9' AS SEX ");
            stb.append(       ",'X' AS FS_AREA_CD ");
            stb.append(       ",COUNT(*) AS CNT ");
			stb.append("FROM    ENTEXAM_APPLICANTBASE_DAT T1 ");
            stb.append(       ",DESIRE_A T3 ");
			stb.append("WHERE   T1.ENTEXAMYEAR = '" + param[0] + "' ");
			stb.append(    "AND T1.APPLICANTDIV = '" + param[8] + "' ");
            /*
            stb.append(    "AND EXISTS(SELECT 'X' FROM ENTEXAM_RECEPT_DAT T2 ");
            stb.append(               "WHERE  T2.ENTEXAMYEAR = '" + param[0] + "' ");
            stb.append(                  "AND T2.APPLICANTDIV = '" + param[8] + "' ");
   			stb.append(                  "AND T2.TESTDIV = '" + param[7] + "' ");
            stb.append(                  "AND T2.EXAMNO = T1.EXAMNO ");
            stb.append(               "GROUP BY T2.EXAMNO) ");
            */
            stb.append(    "AND T3.EXAMNO = T1.EXAMNO ");
		} catch( Exception e ){
			log.error( "preStat error!", e );
            log.debug( "ps = " + stb.toString() );
		}
		return stb.toString();
	}
}


/**
 *  地区別内訳印刷のクラス 合格者
 *  2005/12/29 Build NO001
 */
private class AreaStatistics_4 extends AreaStatistics_Base
{
	/**
     *  地区別 合格者数 取得
     *  2005/12/29 Build NO001
     *  2006/01/05 Modify NO002
     */
	String preStatSub( String param[] )
	{
		StringBuffer stb = new StringBuffer();
		try {
			stb.append("SELECT  VALUE(SEX,'9') AS SEX ");
            stb.append(       ",VALUE(VALUE(FS_AREA_CD,'08'),'X') AS FS_AREA_CD ");
            stb.append(       ",COUNT(*) AS CNT ");
			stb.append("FROM    ENTEXAM_APPLICANTBASE_DAT T1 ");
			stb.append("WHERE   T1.ENTEXAMYEAR = '" + param[0] + "' ");
			stb.append(    "AND T1.APPLICANTDIV = '" + param[8] + "' ");
            stb.append(    "AND EXISTS(SELECT 'X' FROM ENTEXAM_RECEPT_DAT T2 ");
            stb.append(               "WHERE  T2.ENTEXAMYEAR = '" + param[0] + "' ");
            stb.append(                  "AND T2.APPLICANTDIV = '" + param[8] + "' ");
            stb.append(                  "AND T2.EXAMNO = T1.EXAMNO ");
            stb.append(                  "AND T2.JUDGEDIV = '1'  ");		//NO002 志願者受付データ：合否区分
   			stb.append(                  "AND T2.TESTDIV = '" + param[7] + "' ");
            stb.append(                  "AND T2.JUDGECLASS not in ('4','6') ");
            stb.append(           "GROUP BY T2.EXAMNO) ");
            //NO004 前回までの合格者を対象に含めない場合
            if( param[11] == null ){
                stb.append("AND NOT EXISTS(SELECT 'X' FROM ENTEXAM_RECEPT_DAT T2 ");
                stb.append(               "WHERE  T2.ENTEXAMYEAR = '" + param[0] + "' ");
                stb.append(                  "AND T2.APPLICANTDIV = '" + param[8] + "' ");
    			stb.append(                  "AND T2.TESTDIV < '" + param[7] + "' ");
                stb.append(                  "AND T2.EXAMNO = T1.EXAMNO ");
			    stb.append(                  "AND T2.JUDGEDIV = '1'  ");		//NO002 志願者受付データ：合否区分
                stb.append(               "GROUP BY T2.EXAMNO) ");
            }
            stb.append("GROUP BY GROUPING SETS ((SEX, VALUE(FS_AREA_CD,'08')),(SEX),(VALUE(FS_AREA_CD,'08')),()) ");
            stb.append("ORDER BY SEX,FS_AREA_CD ");
		} catch( Exception e ){
			log.error( "preStat error!", e );
            log.debug( "ps = " + stb.toString() );
		}
		return stb.toString();
	}
}

/**
 *  地区別内訳印刷のクラス 合格者(普通)
 */
private class AreaStatistics_4_1 extends AreaStatistics_Base
{
    /**
     *  地区別 合格者数(普通)取得
     */
    String preStatSub( String param[] )
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("SELECT  VALUE(SEX,'9') AS SEX ");
            stb.append(       ",VALUE(VALUE(FS_AREA_CD,'08'),'X') AS FS_AREA_CD ");
            stb.append(       ",COUNT(*) AS CNT ");
            stb.append("FROM    ENTEXAM_APPLICANTBASE_DAT T1 ");
            stb.append("WHERE   T1.ENTEXAMYEAR = '" + param[0] + "' ");
            stb.append(    "AND T1.APPLICANTDIV = '" + param[8] + "' ");
            stb.append(    "AND EXISTS(SELECT 'X' FROM ENTEXAM_RECEPT_DAT T2 ");
            stb.append(               "WHERE  T2.ENTEXAMYEAR = '" + param[0] + "' ");
            stb.append(                  "AND T2.APPLICANTDIV = '" + param[8] + "' ");
            stb.append(                  "AND T2.EXAMNO = T1.EXAMNO ");
            stb.append(                  "AND T2.JUDGECLASS in ('1','2','5') ");
            stb.append(                  "AND T2.TESTDIV = '" + param[7] + "' ");
            stb.append(           "GROUP BY T2.EXAMNO) ");
            //NO004 前回までの合格者を対象に含めない場合
            if( param[11] == null ){
                stb.append("AND NOT EXISTS(SELECT 'X' FROM ENTEXAM_RECEPT_DAT T2 ");
                stb.append(               "WHERE  T2.ENTEXAMYEAR = '" + param[0] + "' ");
                stb.append(                  "AND T2.APPLICANTDIV = '" + param[8] + "' ");
                stb.append(                  "AND T2.TESTDIV < '" + param[7] + "' ");
                stb.append(                  "AND T2.EXAMNO = T1.EXAMNO ");
                stb.append(                  "AND T2.JUDGEDIV = '1' ");
                stb.append(               "GROUP BY T2.EXAMNO) ");
            }
            stb.append("GROUP BY GROUPING SETS ((SEX, VALUE(FS_AREA_CD,'08')),(SEX),(VALUE(FS_AREA_CD,'08')),()) ");
            stb.append("ORDER BY SEX,FS_AREA_CD ");
        } catch( Exception e ){
            log.error( "preStat error!", e );
            log.debug( "ps = " + stb.toString() );
        }
        return stb.toString();
    }
}


/**
 *  地区別内訳印刷のクラス 合格者(アップ)
 */
private class AreaStatistics_4_2 extends AreaStatistics_Base
{
    /**
     *  地区別 合格者数(アップ)取得
     */
    String preStatSub( String param[] )
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("SELECT  VALUE(SEX,'9') AS SEX ");
            stb.append(       ",VALUE(VALUE(FS_AREA_CD,'08'),'X') AS FS_AREA_CD ");
            stb.append(       ",COUNT(*) AS CNT ");
            stb.append("FROM    ENTEXAM_APPLICANTBASE_DAT T1 ");
            stb.append("WHERE   T1.ENTEXAMYEAR = '" + param[0] + "' ");
            stb.append(    "AND T1.APPLICANTDIV = '" + param[8] + "' ");
            stb.append(    "AND EXISTS(SELECT 'X' FROM ENTEXAM_RECEPT_DAT T2 ");
            stb.append(               "WHERE  T2.ENTEXAMYEAR = '" + param[0] + "' ");
            stb.append(                  "AND T2.APPLICANTDIV = '" + param[8] + "' ");
            stb.append(                  "AND T2.EXAMNO = T1.EXAMNO ");
            stb.append(                  "AND T2.JUDGECLASS in ('3') ");
            stb.append(                  "AND T2.TESTDIV = '" + param[7] + "' ");
            stb.append(           "GROUP BY T2.EXAMNO) ");
            //NO004 前回までの合格者を対象に含めない場合
            if( param[11] == null ){
                stb.append("AND NOT EXISTS(SELECT 'X' FROM ENTEXAM_RECEPT_DAT T2 ");
                stb.append(               "WHERE  T2.ENTEXAMYEAR = '" + param[0] + "' ");
                stb.append(                  "AND T2.APPLICANTDIV = '" + param[8] + "' ");
                stb.append(                  "AND T2.TESTDIV < '" + param[7] + "' ");
                stb.append(                  "AND T2.EXAMNO = T1.EXAMNO ");
                stb.append(                  "AND T2.JUDGEDIV = '1' ");
                stb.append(               "GROUP BY T2.EXAMNO) ");
            }
            stb.append("GROUP BY GROUPING SETS ((SEX, VALUE(FS_AREA_CD,'08')),(SEX),(VALUE(FS_AREA_CD,'08')),()) ");
            stb.append("ORDER BY SEX,FS_AREA_CD ");
        } catch( Exception e ){
            log.error( "preStat error!", e );
            log.debug( "ps = " + stb.toString() );
        }
        return stb.toString();
    }
}


/**
 *  地区別内訳印刷のクラス 合格者(スライド)
 */
private class AreaStatistics_4_3 extends AreaStatistics_Base
{
    /**
     *  地区別 合格者数(スライド)取得
     */
    String preStatSub( String param[] )
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("SELECT  VALUE(SEX,'9') AS SEX ");
            stb.append(       ",VALUE(VALUE(FS_AREA_CD,'08'),'X') AS FS_AREA_CD ");
            stb.append(       ",COUNT(*) AS CNT ");
            stb.append("FROM    ENTEXAM_APPLICANTBASE_DAT T1 ");
            stb.append("WHERE   T1.ENTEXAMYEAR = '" + param[0] + "' ");
            stb.append(    "AND T1.APPLICANTDIV = '" + param[8] + "' ");
            stb.append(    "AND EXISTS(SELECT 'X' FROM ENTEXAM_RECEPT_DAT T2 ");
            stb.append(               "WHERE  T2.ENTEXAMYEAR = '" + param[0] + "' ");
            stb.append(                  "AND T2.APPLICANTDIV = '" + param[8] + "' ");
            stb.append(                  "AND T2.EXAMNO = T1.EXAMNO ");
            stb.append(                  "AND T2.JUDGECLASS in ('4','6') ");
            stb.append(                  "AND T2.TESTDIV = '" + param[7] + "' ");
            stb.append(           "GROUP BY T2.EXAMNO) ");
            //NO004 前回までの合格者を対象に含めない場合
            if( param[11] == null ){
                stb.append("AND NOT EXISTS(SELECT 'X' FROM ENTEXAM_RECEPT_DAT T2 ");
                stb.append(               "WHERE  T2.ENTEXAMYEAR = '" + param[0] + "' ");
                stb.append(                  "AND T2.APPLICANTDIV = '" + param[8] + "' ");
                stb.append(                  "AND T2.TESTDIV < '" + param[7] + "' ");
                stb.append(                  "AND T2.EXAMNO = T1.EXAMNO ");
                stb.append(                  "AND T2.JUDGEDIV = '1' ");
                stb.append(               "GROUP BY T2.EXAMNO) ");
            }
            stb.append("GROUP BY GROUPING SETS ((SEX, VALUE(FS_AREA_CD,'08')),(SEX),(VALUE(FS_AREA_CD,'08')),()) ");
            stb.append("ORDER BY SEX,FS_AREA_CD ");
        } catch( Exception e ){
            log.error( "preStat error!", e );
            log.debug( "ps = " + stb.toString() );
        }
        return stb.toString();
    }
}


/**
 *  地区別内訳印刷のクラス 不合格者
 *  2006/01/05 Build NO002
 */
private class AreaStatistics_5 extends AreaStatistics_Base
{
	/**
     *  地区別 不合格者数 取得
     *  2006/01/05 Build NO002
     */
	String preStatSub( String param[] )
	{
		StringBuffer stb = new StringBuffer();
		try {
			stb.append("SELECT  '9' AS SEX ");
            stb.append(       ",'X' AS FS_AREA_CD ");
            stb.append(       ",COUNT(*) AS CNT ");
			stb.append("FROM    ENTEXAM_APPLICANTBASE_DAT T1 ");
			stb.append("WHERE   T1.ENTEXAMYEAR = '" + param[0] + "' ");
			stb.append(    "AND T1.APPLICANTDIV = '" + param[8] + "' ");
            stb.append(    "AND EXISTS(SELECT 'X' FROM ENTEXAM_RECEPT_DAT T2 ");
            stb.append(               "WHERE  T2.ENTEXAMYEAR = '" + param[0] + "' ");
            stb.append(                  "AND T2.APPLICANTDIV = '" + param[8] + "' ");
            stb.append(                  "AND T2.EXAMNO = T1.EXAMNO ");
            stb.append(                  "AND T2.JUDGEDIV = '2'  ");		//NO002 志願者受付データ：合否区分
   			stb.append(                  "AND T2.TESTDIV = '" + param[7] + "' ");
            stb.append(           "GROUP BY T2.EXAMNO) ");
            //NO004 前回までの合格者を対象に含めない場合
            if( param[11] == null ){
                stb.append("AND NOT EXISTS(SELECT 'X' FROM ENTEXAM_RECEPT_DAT T2 ");
                stb.append(               "WHERE  T2.ENTEXAMYEAR = '" + param[0] + "' ");
                stb.append(                  "AND T2.APPLICANTDIV = '" + param[8] + "' ");
    			stb.append(                  "AND T2.TESTDIV < '" + param[7] + "' ");
                stb.append(                  "AND T2.EXAMNO = T1.EXAMNO ");
			    stb.append(                  "AND T2.JUDGEDIV = '1'  ");		//NO002 志願者受付データ：合否区分
                stb.append(               "GROUP BY T2.EXAMNO) ");
            }
		} catch( Exception e ){
			log.error( "preStat error!", e );
            log.debug( "ps = " + stb.toString() );
		}
		return stb.toString();
	}
}


/**
 *  地区別内訳印刷のクラス 特待合格者
 *  2006/01/05 Build NO002
 */
private class AreaStatistics_6 extends AreaStatistics_Base
{
	/**
     *  地区別 特待合格者数 取得
     *  2006/01/05 Build NO002
     */
	String preStatSub( String param[] )
	{
		StringBuffer stb = new StringBuffer();
		try {
			stb.append("SELECT  '9' AS SEX ");
            stb.append(       ",'X' AS FS_AREA_CD ");
            stb.append(       ",COUNT(*) AS CNT ");
			stb.append("FROM    ENTEXAM_APPLICANTBASE_DAT T1 ");
			stb.append("WHERE   T1.ENTEXAMYEAR = '" + param[0] + "' ");
			stb.append(    "AND T1.APPLICANTDIV = '" + param[8] + "' ");
            stb.append(    "AND EXISTS(SELECT 'X' FROM ENTEXAM_RECEPT_DAT T2 ");
            stb.append(               "WHERE  T2.ENTEXAMYEAR = '" + param[0] + "' ");
            stb.append(                  "AND T2.APPLICANTDIV = '" + param[8] + "' ");
            stb.append(                  "AND T2.EXAMNO = T1.EXAMNO ");
            stb.append(                  "AND T2.HONORDIV = '1' ");		//志願者受付データ：特待区分 NO003
            //NO003 stb.append(                  "AND T2.HONORDIV = '2' ");		//志願者受付データ：特待区分
   			stb.append(                  "AND T2.TESTDIV = '" + param[7] + "' ");
            stb.append(           "GROUP BY T2.EXAMNO) ");
            //NO004 前回までの合格者を対象に含めない場合
            if( param[11] == null ){
                stb.append("AND NOT EXISTS(SELECT 'X' FROM ENTEXAM_RECEPT_DAT T2 ");
                stb.append(               "WHERE  T2.ENTEXAMYEAR = '" + param[0] + "' ");
                stb.append(                  "AND T2.APPLICANTDIV = '" + param[8] + "' ");
    			stb.append(                  "AND T2.TESTDIV < '" + param[7] + "' ");
                stb.append(                  "AND T2.EXAMNO = T1.EXAMNO ");
			    stb.append(                  "AND T2.JUDGEDIV = '1'  ");		//NO002 志願者受付データ：合否区分
                stb.append(               "GROUP BY T2.EXAMNO) ");
            }
		} catch( Exception e ){
			log.error( "preStat error!", e );
            log.debug( "ps = " + stb.toString() );
		}
		return stb.toString();
	}
}


/**
 *  地区別内訳印刷のクラス 手続者
 *  2005/12/29 Build NO001
 */
private class AreaStatistics_7 extends AreaStatistics_Base
{
	/**
     *  地区別 手続者数 取得
     *  2005/12/29 Build NO001
     *  2005/01/05 Modify NO002
     */
	String preStatSub( String param[] )
	{
		StringBuffer stb = new StringBuffer();
		try {
			stb.append("SELECT  VALUE(SEX,'9') AS SEX ");
            stb.append(       ",VALUE(VALUE(FS_AREA_CD,'08'),'X') AS FS_AREA_CD ");
            stb.append(       ",COUNT(*) AS CNT ");
			stb.append("FROM    ENTEXAM_APPLICANTBASE_DAT T1 ");
			stb.append("WHERE   ENTEXAMYEAR = '" + param[0] + "' ");
			stb.append(    "AND APPLICANTDIV = '" + param[8] + "' ");
			stb.append(    "AND PROCEDUREDIV = '1' ");
            stb.append(    "AND EXISTS(SELECT 'X' FROM ENTEXAM_RECEPT_DAT T2 ");
            stb.append(               "WHERE  T2.ENTEXAMYEAR = '" + param[0] + "' ");
            stb.append(                  "AND T2.APPLICANTDIV = '" + param[8] + "' ");
            stb.append(                  "AND T2.TESTDIV = '" + param[7] + "' ");
            stb.append(                  "AND T2.JUDGEDIV = '1'  ");		//NO002 志願者受付データ：合否区分
            stb.append(                  "AND T2.EXAMNO = T1.EXAMNO ");
            stb.append(               "GROUP BY T2.EXAMNO) ");
            //NO002 前回までの合格者を対象に含めない場合
            if( param[11] == null ){
                stb.append("AND NOT EXISTS(SELECT 'X' FROM ENTEXAM_RECEPT_DAT T2 ");
                stb.append(               "WHERE  T2.ENTEXAMYEAR = '" + param[0] + "' ");
                stb.append(                  "AND T2.APPLICANTDIV = '" + param[8] + "' ");
    			stb.append(                  "AND T2.TESTDIV < '" + param[7] + "' ");
                stb.append(                  "AND T2.EXAMNO = T1.EXAMNO ");
			    stb.append(                  "AND T2.JUDGEDIV = '1'  ");		//NO002 志願者受付データ：合否区分
                stb.append(               "GROUP BY T2.EXAMNO) ");
            }
            stb.append("GROUP BY GROUPING SETS ((SEX, VALUE(FS_AREA_CD,'08')),(SEX),(VALUE(FS_AREA_CD,'08')),()) ");
            stb.append("ORDER BY SEX,FS_AREA_CD ");
		} catch( Exception e ){
			log.error( "preStat error!", e );
            log.debug( "ps = " + stb.toString() );
		}
		return stb.toString();
	}
}


/**
 *  地区別内訳印刷のクラス 延期者
 *  2006/01/05 Build NO002
 */
private class AreaStatistics_8 extends AreaStatistics_Base
{
	/**
     *  地区別 延期者数 取得
     *  2006/01/05 Build NO002
     */
	String preStatSub( String param[] )
	{
		StringBuffer stb = new StringBuffer();
		try {

			stb.append("SELECT  VALUE(SEX,'9') AS SEX ");
            stb.append(       ",VALUE(VALUE(FS_AREA_CD,'08'),'X') AS FS_AREA_CD ");
            stb.append(       ",COUNT(*) AS CNT ");
			stb.append("FROM    ENTEXAM_APPLICANTBASE_DAT T1 ");
			stb.append("WHERE   T1.ENTEXAMYEAR = '" + param[0] + "' ");
			stb.append(    "AND T1.APPLICANTDIV = '" + param[8] + "' ");
            stb.append(    "AND EXISTS(SELECT 'X' FROM ENTEXAM_RECEPT_DAT T2 ");
            stb.append(               "WHERE  T2.ENTEXAMYEAR = '" + param[0] + "' ");
            stb.append(                  "AND T2.APPLICANTDIV = '" + param[8] + "' ");
            stb.append(                  "AND T2.EXAMNO = T1.EXAMNO ");
            stb.append(                  "AND T2.ADJOURNMENTDIV = '1' ");  //志願者受付データ：延期区分
   			stb.append(                  "AND T2.TESTDIV = '" + param[7] + "' ");
            stb.append(           "GROUP BY T2.EXAMNO) ");
            //NO004 前回までの合格者を対象に含めない場合
            if( param[11] == null ){
                stb.append("AND NOT EXISTS(SELECT 'X' FROM ENTEXAM_RECEPT_DAT T2 ");
                stb.append(               "WHERE  T2.ENTEXAMYEAR = '" + param[0] + "' ");
                stb.append(                  "AND T2.APPLICANTDIV = '" + param[8] + "' ");
    			stb.append(                  "AND T2.TESTDIV < '" + param[7] + "' ");
                stb.append(                  "AND T2.EXAMNO = T1.EXAMNO ");
			    stb.append(                  "AND T2.JUDGEDIV = '1'  ");		//NO002 志願者受付データ：合否区分
                stb.append(               "GROUP BY T2.EXAMNO) ");
            }
            stb.append("GROUP BY GROUPING SETS ((SEX, VALUE(FS_AREA_CD,'08')),(SEX),(VALUE(FS_AREA_CD,'08')),()) ");
            stb.append("ORDER BY SEX,FS_AREA_CD ");
		} catch( Exception e ){
			log.error( "preStat error!", e );
            log.debug( "ps = " + stb.toString() );
		}
		return stb.toString();
	}
}


/**
 *  地区別内訳印刷のクラス 特待入学者
 *  2006/01/05 Build NO002
 */
private class AreaStatistics_9 extends AreaStatistics_Base
{
	/**
     *  地区別 特待入学者数 取得
     *  2006/01/05 Build NO002
     */
	String preStatSub( String param[] )
	{
		StringBuffer stb = new StringBuffer();
		try {
			stb.append("SELECT  '9' AS SEX ");
            stb.append(       ",'X' AS FS_AREA_CD ");
            stb.append(       ",COUNT(*) AS CNT ");
			stb.append("FROM    ENTEXAM_APPLICANTBASE_DAT T1 ");
			stb.append("WHERE   T1.ENTEXAMYEAR = '" + param[0] + "' ");
			stb.append(    "AND T1.APPLICANTDIV = '" + param[8] + "' ");
            stb.append(    "AND T1.HONORDIV = '1' ");  //志願者受付データ：特待区分  NO003
            stb.append(    "AND EXISTS(SELECT 'X' FROM ENTEXAM_RECEPT_DAT T2 ");
            stb.append(               "WHERE  T2.ENTEXAMYEAR = '" + param[0] + "' ");
            stb.append(                  "AND T2.APPLICANTDIV = '" + param[8] + "' ");
            stb.append(                  "AND T2.EXAMNO = T1.EXAMNO ");
            //NO003 stb.append(                  "AND T2.HONORDIV = '1' ");  //志願者受付データ：特待区分
   			stb.append(                  "AND T2.TESTDIV = '" + param[7] + "' ");
            stb.append(           "GROUP BY T2.EXAMNO) ");
            //NO004 前回までの合格者を対象に含めない場合
            if( param[11] == null ){
                stb.append("AND NOT EXISTS(SELECT 'X' FROM ENTEXAM_RECEPT_DAT T2 ");
                stb.append(               "WHERE  T2.ENTEXAMYEAR = '" + param[0] + "' ");
                stb.append(                  "AND T2.APPLICANTDIV = '" + param[8] + "' ");
    			stb.append(                  "AND T2.TESTDIV < '" + param[7] + "' ");
                stb.append(                  "AND T2.EXAMNO = T1.EXAMNO ");
			    stb.append(                  "AND T2.JUDGEDIV = '1'  ");		//NO002 志願者受付データ：合否区分
                stb.append(               "GROUP BY T2.EXAMNO) ");
            }
		} catch( Exception e ){
			log.error( "preStat error!", e );
            log.debug( "ps = " + stb.toString() );
		}
		return stb.toString();
	}
}


/**
 *  地区別内訳印刷のクラス 入学者
 *  2005/12/29 Build NO001
 */
private class AreaStatistics_10 extends AreaStatistics_Base
{
	/**
     *  地区別 入学者数 取得
     *  2005/12/29 Build NO001
     *  2005/01/05 Modify NO002
     */
	String preStatSub( String param[] )
	{
		StringBuffer stb = new StringBuffer();
		try {
			stb.append("SELECT  VALUE(SEX,'9') AS SEX ");
            stb.append(       ",VALUE(VALUE(FS_AREA_CD,'08'),'X') AS FS_AREA_CD ");
            stb.append(       ",COUNT(*) AS CNT ");
			stb.append("FROM    ENTEXAM_APPLICANTBASE_DAT T1 ");
			stb.append("WHERE   ENTEXAMYEAR = '" + param[0] + "' ");
			stb.append(    "AND APPLICANTDIV = '" + param[8] + "' ");
			stb.append(    "AND ENTDIV = '1' ");
            stb.append(    "AND EXISTS(SELECT 'X' FROM ENTEXAM_RECEPT_DAT T2 ");
            stb.append(               "WHERE  T2.ENTEXAMYEAR = '" + param[0] + "' ");
            stb.append(                  "AND T2.APPLICANTDIV = '" + param[8] + "' ");
            stb.append(                  "AND T2.TESTDIV = '" + param[7] + "' ");
            stb.append(                  "AND T2.JUDGEDIV = '1'  ");		//NO002 志願者受付データ：合否区分
            stb.append(                  "AND T2.EXAMNO = T1.EXAMNO ");
            stb.append(               "GROUP BY T2.EXAMNO) ");
            //NO002 前回までの合格者を対象に含めない場合
            if( param[11] == null ){
                stb.append("AND NOT EXISTS(SELECT 'X' FROM ENTEXAM_RECEPT_DAT T2 ");
                stb.append(               "WHERE  T2.ENTEXAMYEAR = '" + param[0] + "' ");
                stb.append(                  "AND T2.APPLICANTDIV = '" + param[8] + "' ");
    			stb.append(                  "AND T2.TESTDIV < '" + param[7] + "' ");
                stb.append(                  "AND T2.EXAMNO = T1.EXAMNO ");
			    stb.append(                  "AND T2.JUDGEDIV = '1'  ");		//NO002 志願者受付データ：合否区分
                stb.append(               "GROUP BY T2.EXAMNO) ");
            }
            stb.append("GROUP BY GROUPING SETS ((SEX, VALUE(FS_AREA_CD,'08')),(SEX),(VALUE(FS_AREA_CD,'08')),()) ");
            stb.append("ORDER BY SEX,FS_AREA_CD ");
		} catch( Exception e ){
			log.error( "preStat error!", e );
            log.debug( "ps = " + stb.toString() );
		}
		return stb.toString();
	}
    
    /** 表上部の特科、普通クラスの人数を印字する */
    private String preStatSub2 (String param[]) {
        StringBuffer stb = new StringBuffer();
        stb.append("SELECT  ENTCLASS ");
        stb.append(       ",COUNT(*) AS CNT ");
        stb.append("FROM    ENTEXAM_APPLICANTBASE_DAT T1 ");
        stb.append("WHERE   ENTEXAMYEAR = '" + param[0] + "' ");
        stb.append(    "AND APPLICANTDIV = '" + param[8] + "' ");
        stb.append(    "AND ENTDIV = '1' ");
        stb.append(    "AND EXISTS(SELECT 'X' FROM ENTEXAM_RECEPT_DAT T2 ");
        stb.append(               "WHERE  T2.ENTEXAMYEAR = '" + param[0] + "' ");
        stb.append(                  "AND T2.APPLICANTDIV = '" + param[8] + "' ");
        stb.append(                  "AND T2.TESTDIV = '" + param[7] + "' ");
        stb.append(                  "AND T2.JUDGEDIV = '1'  ");        //NO002 志願者受付データ：合否区分
        stb.append(                  "AND T2.EXAMNO = T1.EXAMNO ");
        stb.append(               "GROUP BY T2.EXAMNO) ");
        //NO002 前回までの合格者を対象に含めない場合
        if( param[11] == null ){
            stb.append("AND NOT EXISTS(SELECT 'X' FROM ENTEXAM_RECEPT_DAT T2 ");
            stb.append(               "WHERE  T2.ENTEXAMYEAR = '" + param[0] + "' ");
            stb.append(                  "AND T2.APPLICANTDIV = '" + param[8] + "' ");
            stb.append(                  "AND T2.TESTDIV < '" + param[7] + "' ");
            stb.append(                  "AND T2.EXAMNO = T1.EXAMNO ");
            stb.append(                  "AND T2.JUDGEDIV = '1'  ");        //NO002 志願者受付データ：合否区分
            stb.append(               "GROUP BY T2.EXAMNO) ");
        }
        stb.append("GROUP BY ENTCLASS ");
        return stb.toString();
    }
    
    private void svfprintSub2( DB2UDB db2
            ,Vrw32alp svf
            ,String param[])

    {
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }

        totalcnt = 0;  //NO002
        int[] ent=new int[2];
        // ENTCLASS1 -> '普通' -> ENTRANCE2
        // ENTCLASS2 -> '特科' -> ENTRANCE1
        String[] conv=new String[]{"2","1"};
        try {
            ps = db2.prepareStatement(preStatSub2(param));
            rs = ps.executeQuery();
            while ( rs.next() ){
                String entclass = rs.getString("ENTCLASS");
                if (entclass != null) {
                    ent[Integer.parseInt(entclass)-1] = Integer.parseInt(rs.getString("CNT"));
                }
            }
            rs.close();
            db2.commit();
        } catch( Exception ex ) {
            log.error( "setSvfSub error!", ex );
        }
        for(int i=0; i<2; i++) {
            ret = svf.VrsOut("ENTRANCE"+conv[i], String.valueOf(ent[i]));
        }
    }    
}


/**
 *  地区別内訳印刷のクラス アップ合格者
 */
private class AreaStatistics_11_3 extends AreaStatistics_Base
{
    String preStatSub( String param[] )
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("SELECT  VALUE(SEX,'9') AS SEX ");
            stb.append(       ",VALUE(VALUE(FS_AREA_CD,'08'),'X') AS FS_AREA_CD ");
            stb.append(       ",COUNT(*) AS CNT ");
            stb.append("FROM    ENTEXAM_APPLICANTBASE_DAT T1 ");
            stb.append("WHERE   T1.ENTEXAMYEAR = '" + param[0] + "' ");
            stb.append(    "AND T1.APPLICANTDIV = '" + param[8] + "' ");
            stb.append(    "AND EXISTS(SELECT 'X' FROM ENTEXAM_RECEPT_DAT T2 ");
            stb.append(               "WHERE  T2.ENTEXAMYEAR = '" + param[0] + "' ");
            stb.append(                  "AND T2.APPLICANTDIV = '" + param[8] + "' ");
            stb.append(                  "AND T2.EXAMNO = T1.EXAMNO ");
            stb.append(                  "AND T2.JUDGECLASS in ('3') "); //3:アップ
            stb.append(                  "AND T2.TESTDIV = '" + param[7] + "' ");
            stb.append(           "GROUP BY T2.EXAMNO) ");
            //NO004 前回までの合格者を対象に含めない場合
            if( param[11] == null ){
                stb.append("AND NOT EXISTS(SELECT 'X' FROM ENTEXAM_RECEPT_DAT T2 ");
                stb.append(               "WHERE  T2.ENTEXAMYEAR = '" + param[0] + "' ");
                stb.append(                  "AND T2.APPLICANTDIV = '" + param[8] + "' ");
                stb.append(                  "AND T2.TESTDIV < '" + param[7] + "' ");
                stb.append(                  "AND T2.EXAMNO = T1.EXAMNO ");
                stb.append(                  "AND T2.JUDGEDIV = '1' ");
                stb.append(               "GROUP BY T2.EXAMNO) ");
            }
            stb.append("GROUP BY GROUPING SETS ((SEX, VALUE(FS_AREA_CD,'08')),(SEX),(VALUE(FS_AREA_CD,'08')),()) ");
            stb.append("ORDER BY SEX,FS_AREA_CD ");
        } catch( Exception e ){
            log.error( "preStat error!", e );
            log.debug( "ps = " + stb.toString() );
        }
        return stb.toString();
    }
}


/**
 *  地区別内訳印刷のクラス スライド合格者
 */
private class AreaStatistics_11_4 extends AreaStatistics_Base
{
    String preStatSub( String param[] )
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("SELECT  VALUE(SEX,'9') AS SEX ");
            stb.append(       ",VALUE(VALUE(FS_AREA_CD,'08'),'X') AS FS_AREA_CD ");
            stb.append(       ",COUNT(*) AS CNT ");
            stb.append("FROM    ENTEXAM_APPLICANTBASE_DAT T1 ");
            stb.append("WHERE   T1.ENTEXAMYEAR = '" + param[0] + "' ");
            stb.append(    "AND T1.APPLICANTDIV = '" + param[8] + "' ");
            stb.append(    "AND EXISTS(SELECT 'X' FROM ENTEXAM_RECEPT_DAT T2 ");
            stb.append(               "WHERE  T2.ENTEXAMYEAR = '" + param[0] + "' ");
            stb.append(                  "AND T2.APPLICANTDIV = '" + param[8] + "' ");
            stb.append(                  "AND T2.EXAMNO = T1.EXAMNO ");
            stb.append(                  "AND T2.JUDGECLASS in ('4','6') "); //4:スライド
            stb.append(                  "AND T2.TESTDIV = '" + param[7] + "' ");
            stb.append(           "GROUP BY T2.EXAMNO) ");
            //NO004 前回までの合格者を対象に含めない場合
            if( param[11] == null ){
                stb.append("AND NOT EXISTS(SELECT 'X' FROM ENTEXAM_RECEPT_DAT T2 ");
                stb.append(               "WHERE  T2.ENTEXAMYEAR = '" + param[0] + "' ");
                stb.append(                  "AND T2.APPLICANTDIV = '" + param[8] + "' ");
                stb.append(                  "AND T2.TESTDIV < '" + param[7] + "' ");
                stb.append(                  "AND T2.EXAMNO = T1.EXAMNO ");
                stb.append(                  "AND T2.JUDGEDIV = '1' ");
                stb.append(               "GROUP BY T2.EXAMNO) ");
            }
            stb.append("GROUP BY GROUPING SETS ((SEX, VALUE(FS_AREA_CD,'08')),(SEX),(VALUE(FS_AREA_CD,'08')),()) ");
            stb.append("ORDER BY SEX,FS_AREA_CD ");
        } catch( Exception e ){
            log.error( "preStat error!", e );
            log.debug( "ps = " + stb.toString() );
        }
        return stb.toString();
    }
}


/**
 *  地区別内訳印刷のクラス 特別アップ合格者
 */
private class AreaStatistics_12 extends AreaStatistics_Base
{
    String preStatSub( String param[] )
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("SELECT  VALUE(SEX,'9') AS SEX ");
            stb.append(       ",VALUE(VALUE(FS_AREA_CD,'08'),'X') AS FS_AREA_CD ");
            stb.append(       ",COUNT(*) AS CNT ");
            stb.append("FROM    ENTEXAM_APPLICANTBASE_DAT T1 ");
            stb.append("WHERE   T1.ENTEXAMYEAR = '" + param[0] + "' ");
            stb.append(    "AND T1.APPLICANTDIV = '" + param[8] + "' ");
            stb.append(    "AND T1.SPECIAL_MEASURES = '3' ");
            stb.append(    "AND EXISTS(SELECT 'X' FROM ENTEXAM_RECEPT_DAT T2 ");
            stb.append(               "WHERE  T2.ENTEXAMYEAR = '" + param[0] + "' ");
            stb.append(                  "AND T2.APPLICANTDIV = '" + param[8] + "' ");
            stb.append(                  "AND T2.TESTDIV = '" + param[7] + "' ");
            stb.append(                  "AND T2.EXAMNO = T1.EXAMNO ");
            stb.append(           "GROUP BY T2.EXAMNO) ");
            //NO004 前回までの合格者を対象に含めない場合
            if( param[11] == null ){
                stb.append("AND NOT EXISTS(SELECT 'X' FROM ENTEXAM_RECEPT_DAT T2 ");
                stb.append(               "WHERE  T2.ENTEXAMYEAR = '" + param[0] + "' ");
                stb.append(                  "AND T2.APPLICANTDIV = '" + param[8] + "' ");
                stb.append(                  "AND T2.TESTDIV < '" + param[7] + "' ");
                stb.append(                  "AND T2.EXAMNO = T1.EXAMNO ");
                stb.append(                  "AND T2.JUDGEDIV = '1' ");
                stb.append(               "GROUP BY T2.EXAMNO) ");
            }
            stb.append("GROUP BY GROUPING SETS ((SEX, VALUE(FS_AREA_CD,'08')),(SEX),(VALUE(FS_AREA_CD,'08')),()) ");
            stb.append("ORDER BY SEX,FS_AREA_CD ");
        } catch( Exception e ){
            log.error( "preStat error!", e );
            log.debug( "ps = " + stb.toString() );
        }
        return stb.toString();
    }
}



//==実数==実数==実数==実数==実数==実数==実数==実数==実数==実数==実数==実数==実数==


/**
 *  地区別内訳印刷のクラス 志願者
 *  2006/01/05 Build NO002
 */
private class AreaStatistics_1B extends AreaStatistics_Base
{
	/**
     *  地区別 志願者数 取得
     *  2005/12/29 Modify NO001
     *  2006/01/05 Modify NO002
     */
	String preStatSub( String param[] )
	{
		StringBuffer stb = new StringBuffer();
		try {
			//基礎データ
			stb.append("SELECT  COUNT(*) AS CNT ");
			stb.append(       ",VALUE(SEX,'9') AS SEX ");                            //NO002
            stb.append(       ",VALUE(VALUE(FS_AREA_CD,'08'),'X') AS FS_AREA_CD ");  //NO002
            stb.append(       ",COUNT(*) AS CNT ");
			stb.append("FROM    ENTEXAM_APPLICANTBASE_DAT ");
			stb.append("WHERE   ENTEXAMYEAR = '"+param[0]+"' ");
			//NO001 stb.append("        AND APPLICANTDIV = ? ");
			stb.append(    "AND APPLICANTDIV = '" + param[8] + "' "); //NO001
            stb.append("GROUP BY GROUPING SETS ((SEX, VALUE(FS_AREA_CD,'08')),(SEX),(VALUE(FS_AREA_CD,'08')),()) "); //NO002
            stb.append("ORDER BY SEX,FS_AREA_CD ");  //NO002
		} catch( Exception e ){
			log.error( "preStat error!", e );
            log.debug( "ps = " + stb.toString() );
		}
		return stb.toString();
	}
}


/**
 *  地区別内訳印刷のクラス 受験者
 *  2006/01/05 Build NO002
 */
private class AreaStatistics_2B extends AreaStatistics_Base
{
	/**
     *  地区別 受験者数 取得
     *  2005/12/29 Modify NO001
     *  2006/01/05 Modify NO002
     */
	String preStatSub( String param[] )
	{
		StringBuffer stb = new StringBuffer();
		try {
			//基礎データ
			//NO002 stb.append("WITH ATABLE(examno) AS (SELECT ");
			stb.append("WITH ATABLE AS (SELECT ");  //NO002
			stb.append("    EXAMNO ");
            stb.append(   ",SEX, FS_AREA_CD ");  //NO002
			stb.append("FROM ");
			stb.append("    ENTEXAM_APPLICANTBASE_DAT ");
			stb.append("WHERE ");
			stb.append("    ENTEXAMYEAR = '"+param[0]+"' ");
			//NO001 stb.append("    AND APPLICANTDIV = ? ");
			stb.append(    "AND APPLICANTDIV = '" + param[8] + "' ");  //NO001
			stb.append("), BTABLE(EXAMNO) AS ( ");
			stb.append("SELECT DISTINCT EXAMNO ");
			stb.append("FROM ENTEXAM_RECEPT_DAT ");
			stb.append("WHERE ENTEXAMYEAR = '"+param[0]+"' ");
			//NO001 stb.append("    AND APPLICANTDIV = ? ");
			stb.append(    "AND APPLICANTDIV = '" + param[8] + "' ) ");  //NO001

			stb.append("SELECT COUNT(*) AS CNT ");
			stb.append(       ",VALUE(SEX,'9') AS SEX ");                            //NO002
            stb.append(       ",VALUE(VALUE(FS_AREA_CD,'08'),'X') AS FS_AREA_CD ");  //NO002
			stb.append("FROM ATABLE w1,BTABLE w2 ");
			stb.append("WHERE w1.EXAMNO = w2.EXAMNO ");
            stb.append("GROUP BY GROUPING SETS ((SEX, VALUE(FS_AREA_CD,'08')),(SEX),(VALUE(FS_AREA_CD,'08')),()) "); //NO002
            stb.append("ORDER BY SEX,FS_AREA_CD ");  //NO002
		} catch( Exception e ){
			log.error( "preStat error!", e );
            log.debug( "ps = " + stb.toString() );
		}
		return stb.toString();
	}
}


/**
 *  地区別内訳印刷のクラス 欠席者
 *  2006/01/05 Build NO002
 */
private class AreaStatistics_3B extends AreaStatistics_Base
{
	/**
     *  地区別 欠席者数 取得
     *  2005/12/29 Modify NO001
     *  2006/01/05 Modify NO002
     */
	String preStatSub( String param[] )
	{
		StringBuffer stb = new StringBuffer();
		try {
		} catch( Exception e ){
			log.error( "preStat error!", e );
            log.debug( "ps = " + stb.toString() );
		}
		return stb.toString();
	}
}


/**
 *  地区別内訳印刷のクラス 合格者
 *  2006/01/05 Build NO002
 */
private class AreaStatistics_4B extends AreaStatistics_Base
{
	/**
     *  地区別 合格者数 取得
     *  2005/12/29 Modify NO001
     *  2006/01/05 Modify NO002
     */
	String preStatSub( String param[] )
	{
		StringBuffer stb = new StringBuffer();
		try {
			//基礎データ
			stb.append("SELECT ");
			stb.append("    COUNT(*) AS CNT ");
			stb.append(   ",VALUE(SEX,'9') AS SEX ");                              //NO002
            stb.append(   ",VALUE(VALUE(FS_AREA_CD,'08'),'X') AS FS_AREA_CD ");    //NO002
			stb.append("FROM ");
			stb.append("    ENTEXAM_APPLICANTBASE_DAT ");
			stb.append("WHERE ");
			stb.append("    ENTEXAMYEAR = '"+param[0]+"' AND ");
//NO001     ---------->
			stb.append("    ( JUDGEMENT = '1' OR SPECIAL_MEASURES in ('1','2') ) ");    // --NO001
			//stb.append("    JUDGEMENT = '1' OR ");
			//stb.append("    SPECIAL_MEASURES in ('1','2') ");
			//stb.append("        AND APPLICANTDIV = ? ");
			stb.append(    "AND APPLICANTDIV = '" + param[8] + "' ");
//NO001     <----------
            stb.append(    "AND ((ENTEXAMYEAR, EXAMNO) IN (SELECT ENTEXAMYEAR, EXAMNO ");
            stb.append(    "FROM  ENTEXAM_RECEPT_DAT ");
            stb.append(    "WHERE (ENTEXAMYEAR,EXAMNO, TESTDIV) IN ");
            stb.append(         "(SELECT MAX(ENTEXAMYEAR) AS ENTEXAMYEAR, ");
            stb.append(                 "EXAMNO, ");
            stb.append(            "MAX(TESTDIV) AS MAXTESTDIV ");
            stb.append(            "FROM ENTEXAM_RECEPT_DAT ");
            stb.append(            "WHERE ENTEXAMYEAR='" + param[0] + "' AND JUDGEDIV = '1' ");
            stb.append(            "GROUP BY EXAMNO) ");
            stb.append(    ") ");
            stb.append(    "OR SPECIAL_MEASURES IN ('1','2')) ");
            stb.append("GROUP BY GROUPING SETS ((SEX, VALUE(FS_AREA_CD,'08')),(SEX),(VALUE(FS_AREA_CD,'08')),()) "); //NO002
            stb.append("ORDER BY SEX,FS_AREA_CD ");  //NO002
		} catch( Exception e ){
			log.error( "preStat error!", e );
            log.debug( "ps = " + stb.toString() );
		}
		return stb.toString();
	}
}


/**
 *  地区別内訳印刷のクラス 合格者(普通)
 */
private class AreaStatistics_4B_1 extends AreaStatistics_Base
{
    String preStatSub( String param[] )
    {
        StringBuffer stb = new StringBuffer();
        try {
            //基礎データ
            stb.append("SELECT ");
            stb.append("    COUNT(*) AS CNT ");
            stb.append(   ",VALUE(SEX,'9') AS SEX ");                              //NO002
            stb.append(   ",VALUE(VALUE(FS_AREA_CD,'08'),'X') AS FS_AREA_CD ");    //NO002
            stb.append("FROM ");
            stb.append("    ENTEXAM_APPLICANTBASE_DAT ");
            stb.append("WHERE ");
            stb.append( "((ENTEXAMYEAR, EXAMNO) IN (SELECT ENTEXAMYEAR, EXAMNO ");
            stb.append(    "FROM  ENTEXAM_RECEPT_DAT ");
            stb.append(    "WHERE (ENTEXAMYEAR,EXAMNO, TESTDIV) IN ");
            stb.append(         "(SELECT MAX(ENTEXAMYEAR) AS ENTEXAMYEAR, ");
            stb.append(                 "EXAMNO, ");
            stb.append(            "MAX(TESTDIV) AS MAXTESTDIV ");
            stb.append(            "FROM ENTEXAM_RECEPT_DAT ");
            stb.append(            "WHERE ENTEXAMYEAR='" + param[0] + "' AND JUDGEDIV = '1' ");
            stb.append(            "GROUP BY EXAMNO) ");
            stb.append(    "AND JUDGECLASS IN ('1','2','5')) ");
            stb.append(  "OR SPECIAL_MEASURES IN ('1','2') ");
            stb.append(  "AND ENTEXAMYEAR = '" + param[0] + "') ");
            stb.append(  "AND APPLICANTDIV = '" + param[8] + "' ");
            stb.append("GROUP BY GROUPING SETS ((SEX, VALUE(FS_AREA_CD,'08')),(SEX),(VALUE(FS_AREA_CD,'08')),()) "); //NO002
            stb.append("ORDER BY SEX,FS_AREA_CD ");  //NO002
        } catch( Exception e ){
            log.error( "preStat error!", e );
            log.debug( "ps = " + stb.toString() );
        }
        return stb.toString();
    }
}

/**
 *  地区別内訳印刷のクラス 合格者(普通)
 */
private class AreaStatistics_4B_2 extends AreaStatistics_Base
{
    String preStatSub( String param[] )
    {
        StringBuffer stb = new StringBuffer();
        try {
            //基礎データ
            stb.append("SELECT ");
            stb.append("    COUNT(*) AS CNT ");
            stb.append(   ",VALUE(SEX,'9') AS SEX ");                              //NO002
            stb.append(   ",VALUE(VALUE(FS_AREA_CD,'08'),'X') AS FS_AREA_CD ");    //NO002
            stb.append("FROM ");
            stb.append("    ENTEXAM_APPLICANTBASE_DAT ");
            stb.append("WHERE ");
            stb.append( "(ENTEXAMYEAR, EXAMNO) IN (SELECT ENTEXAMYEAR, EXAMNO ");
            stb.append(    "FROM  ENTEXAM_RECEPT_DAT ");
            stb.append(    "WHERE (ENTEXAMYEAR,EXAMNO, TESTDIV) IN ");
            stb.append(         "(SELECT MAX(ENTEXAMYEAR) AS ENTEXAMYEAR, ");
            stb.append(                 "EXAMNO, ");
            stb.append(            "MAX(TESTDIV) AS MAXTESTDIV ");
            stb.append(            "FROM ENTEXAM_RECEPT_DAT ");
            stb.append(            "WHERE ENTEXAMYEAR='" + param[0] + "' AND JUDGEDIV = '1' ");
            stb.append(            "GROUP BY EXAMNO) ");
            stb.append(    "AND JUDGECLASS IN ('3')) ");
            stb.append(    "AND APPLICANTDIV = '" + param[8] + "' ");
            stb.append("GROUP BY GROUPING SETS ((SEX, VALUE(FS_AREA_CD,'08')),(SEX),(VALUE(FS_AREA_CD,'08')),()) "); //NO002
            stb.append("ORDER BY SEX,FS_AREA_CD ");  //NO002
        } catch( Exception e ){
            log.error( "preStat error!", e );
            log.debug( "ps = " + stb.toString() );
        }
        return stb.toString();
    }
}


/**
 *  地区別内訳印刷のクラス 合格者(普通)
 */
private class AreaStatistics_4B_3 extends AreaStatistics_Base
{
    String preStatSub( String param[] )
    {
        StringBuffer stb = new StringBuffer();
        try {
            //基礎データ
            stb.append("SELECT ");
            stb.append("    COUNT(*) AS CNT ");
            stb.append(   ",VALUE(SEX,'9') AS SEX ");                              //NO002
            stb.append(   ",VALUE(VALUE(FS_AREA_CD,'08'),'X') AS FS_AREA_CD ");    //NO002
            stb.append("FROM ");
            stb.append("    ENTEXAM_APPLICANTBASE_DAT ");
            stb.append("WHERE ");
            stb.append( "(ENTEXAMYEAR, EXAMNO) IN (SELECT ENTEXAMYEAR, EXAMNO ");
            stb.append(    "FROM  ENTEXAM_RECEPT_DAT ");
            stb.append(    "WHERE (ENTEXAMYEAR,EXAMNO, TESTDIV) IN ");
            stb.append(         "(SELECT MAX(ENTEXAMYEAR) AS ENTEXAMYEAR, ");
            stb.append(                 "EXAMNO, ");
            stb.append(            "MAX(TESTDIV) AS MAXTESTDIV ");
            stb.append(            "FROM ENTEXAM_RECEPT_DAT ");
            stb.append(            "WHERE ENTEXAMYEAR='" + param[0] + "' AND JUDGEDIV = '1' ");
            stb.append(            "GROUP BY EXAMNO) ");
            stb.append(    "AND JUDGECLASS IN ('4','6')) ");
            stb.append(    "AND APPLICANTDIV = '" + param[8] + "' ");
            stb.append("GROUP BY GROUPING SETS ((SEX, VALUE(FS_AREA_CD,'08')),(SEX),(VALUE(FS_AREA_CD,'08')),()) "); //NO002
            stb.append("ORDER BY SEX,FS_AREA_CD ");  //NO002
        } catch( Exception e ){
            log.error( "preStat error!", e );
            log.debug( "ps = " + stb.toString() );
        }
        return stb.toString();
    }
}


/**
 *  地区別内訳印刷のクラス 不合格者
 *  2006/01/05 Build NO002
 */
private class AreaStatistics_5B extends AreaStatistics_Base
{
	/**
     *  地区別 不合格者数 取得
     *  2005/12/29 Modify NO001
     *  2006/01/05 Modify NO002
     */
	String preStatSub( String param[] )
	{
		StringBuffer stb = new StringBuffer();
		try {
			//基礎データ
			stb.append("SELECT ");
			stb.append("    COUNT(*) AS CNT ");
			stb.append(   ",'9' AS SEX ");             //NO002
            stb.append(   ",'X' AS FS_AREA_CD ");      //NO002
			stb.append("FROM ");
			stb.append("    ENTEXAM_APPLICANTBASE_DAT ");
			stb.append("WHERE ");
			stb.append("    ENTEXAMYEAR = '"+param[0]+"' AND ");
			stb.append("    JUDGEMENT = '2' ");
			//NO001 stb.append("        AND APPLICANTDIV = ? ");
			stb.append(    "AND APPLICANTDIV = '" + param[8] + "' ");  //NO001
		} catch( Exception e ){
			log.error( "preStat error!", e );
            log.debug( "ps = " + stb.toString() );
		}
		return stb.toString();
	}
}


/**
 *  地区別内訳印刷のクラス 特待合格者
 *  2006/01/05 Build NO002
 */
private class AreaStatistics_6B extends AreaStatistics_Base
{
	/**
     *  地区別 特待合格者数 取得
     *  2005/12/29 Modify NO001
     *  2006/01/05 Modify NO002
     */
	String preStatSub( String param[] )
	{
		StringBuffer stb = new StringBuffer();
		try {
			//基礎データ
			stb.append("WITH ATABLE (EXAMNO) AS (SELECT DISTINCT ");
			stb.append("    EXAMNO ");
			stb.append("FROM ");
			stb.append("    ENTEXAM_RECEPT_DAT ");
			stb.append("WHERE ");
			stb.append("    ENTEXAMYEAR = '"+param[0]+"' AND ");
			stb.append("    HONORDIV = '1' ");
			//NO001 stb.append("        AND APPLICANTDIV = ? ");
			stb.append(    "AND APPLICANTDIV = '" + param[8] + "' ");  //NO001
			stb.append(") ");
			//NO002 stb.append("SELECT COUNT(*) AS CNT FROM ATABLE  ");
			stb.append("SELECT ");
			stb.append("    COUNT(*) AS CNT ");
			stb.append(   ",'9' AS SEX ");             //NO002
            stb.append(   ",'X' AS FS_AREA_CD ");      //NO002
			stb.append("FROM ");
			stb.append("    ATABLE ");
		} catch( Exception e ){
			log.error( "preStat error!", e );
            log.debug( "ps = " + stb.toString() );
		}
		return stb.toString();
	}
}


/**
 *  地区別内訳印刷のクラス 手続者
 *  2006/01/05 Build NO002
 */
private class AreaStatistics_7B extends AreaStatistics_Base
{
	/**
     *  地区別 手続者数 取得
     *  2005/12/29 Modify NO001
     *  2006/01/05 Modify NO002
     */
	String preStatSub( String param[] )
	{
		StringBuffer stb = new StringBuffer();
		try {
			//基礎データ
			stb.append("SELECT ");
			stb.append("    COUNT(*) AS CNT ");
			stb.append(   ",VALUE(SEX,'9') AS SEX ");                           //NO002
            stb.append(   ",VALUE(VALUE(FS_AREA_CD,'08'),'X') AS FS_AREA_CD "); //NO002
			stb.append("FROM ");
			stb.append("    ENTEXAM_APPLICANTBASE_DAT ");
			stb.append("WHERE ");
			stb.append("    ENTEXAMYEAR = '"+param[0]+"' AND ");
			stb.append("    PROCEDUREDIV = '1' ");
			//NO001 stb.append("        AND APPLICANTDIV = ? ");
			stb.append(    "AND APPLICANTDIV = '" + param[8] + "' ");  //NO001
            stb.append("GROUP BY GROUPING SETS ((SEX, VALUE(FS_AREA_CD,'08')),(SEX),(VALUE(FS_AREA_CD,'08')),()) "); //NO002
            stb.append("ORDER BY SEX,FS_AREA_CD ");  //NO002
		} catch( Exception e ){
			log.error( "preStat error!", e );
            log.debug( "ps = " + stb.toString() );
		}
		return stb.toString();
	}
}


/**
 *  地区別内訳印刷のクラス 延期者
 *  2006/01/05 Build NO002
 */
private class AreaStatistics_8B extends AreaStatistics_Base
{
	/**
     *  地区別 延期者数 取得
     *  2005/12/29 Modify NO001
     *  2006/01/05 Modify NO002
     */
	String preStatSub( String param[] )
	{
		StringBuffer stb = new StringBuffer();
		try {
			//受付データ
/* NO005
			stb.append("WITH ATABLE (EXAMNO) AS (SELECT DISTINCT ");
			stb.append("    EXAMNO ");
			stb.append("FROM ");
			stb.append("    ENTEXAM_RECEPT_DAT ");
			stb.append("WHERE ");
			stb.append("    ENTEXAMYEAR = '"+param[0]+"' AND ");
			stb.append("    ADJOURNMENTDIV = '1' ");
			//NO01 stb.append("        AND APPLICANTDIV = ? ");
			stb.append(    "AND APPLICANTDIV = '" + param[8] + "' ");  //NO001
			stb.append(") ");
			//NO002 stb.append("SELECT COUNT(*) AS CNT FROM ATABLE  ");
			stb.append("SELECT ");
			stb.append("    COUNT(*) AS CNT ");
			stb.append(   ",'9' AS SEX ");             //NO002
            stb.append(   ",'X' AS FS_AREA_CD ");      //NO002
			stb.append("FROM ");
			stb.append("    ATABLE ");
*/
			stb.append("WITH ATABLE (ENTEXAMYEAR,EXAMNO) AS (SELECT DISTINCT ");
			stb.append("    ENTEXAMYEAR,EXAMNO ");
			stb.append("FROM ");
			stb.append("    ENTEXAM_RECEPT_DAT ");
			stb.append("WHERE ");
			stb.append("    ENTEXAMYEAR = '"+param[0]+"' AND ");
			stb.append("    ADJOURNMENTDIV = '1' ");
			stb.append(    "AND APPLICANTDIV = '" + param[8] + "' ");  //NO001
			stb.append(") ");
			stb.append("SELECT ");
			stb.append("    COUNT(*) AS CNT ");
			stb.append(   ",VALUE(T2.SEX,'9') AS SEX ");
            stb.append(   ",VALUE(VALUE(T2.FS_AREA_CD,'08'),'X') AS FS_AREA_CD ");
			stb.append("FROM ");
			stb.append("    ATABLE T1 ");
			stb.append("    LEFT JOIN ENTEXAM_APPLICANTBASE_DAT T2 ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ");
			stb.append("         AND T2.EXAMNO = T1.EXAMNO ");
            stb.append("GROUP BY GROUPING SETS ((T2.SEX, VALUE(T2.FS_AREA_CD,'08')),(T2.SEX),(VALUE(T2.FS_AREA_CD,'08')),()) ");
            stb.append("ORDER BY SEX, FS_AREA_CD ");
		} catch( Exception e ){
			log.error( "preStat error!", e );
            log.debug( "ps = " + stb.toString() );
		}
		return stb.toString();
	}
}


/**
 *  地区別内訳印刷のクラス 特待入学者
 *  2006/01/05 Build NO002
 */
private class AreaStatistics_9B extends AreaStatistics_Base
{
	/**
     *  地区別 特待入学者数 取得
     *  2005/12/29 Modify NO001
     *  2006/01/05 Modify NO002
     */
	String preStatSub( String param[] )
	{
		StringBuffer stb = new StringBuffer();
		try {
			//基礎データ
			stb.append("SELECT ");
			stb.append("    COUNT(*) AS CNT ");
			stb.append(   ",'9' AS SEX ");             //NO002
            stb.append(   ",'X' AS FS_AREA_CD ");      //NO002
			stb.append("FROM ");
			stb.append("    ENTEXAM_APPLICANTBASE_DAT ");
			stb.append("WHERE ");
			stb.append("    ENTEXAMYEAR = '"+param[0]+"' AND ");
			stb.append("    HONORDIV = '1' ");
			//NO001 stb.append("        AND APPLICANTDIV = ? ");
			stb.append(    "AND APPLICANTDIV = '" + param[8] + "' ");  //NO001
		} catch( Exception e ){
			log.error( "preStat error!", e );
            log.debug( "ps = " + stb.toString() );
		}
		return stb.toString();
	}
}


/**
 *  地区別内訳印刷のクラス 入学者
 *  2006/01/05 Build NO002
 */
private class AreaStatistics_10B extends AreaStatistics_Base
{
	/**
     *  地区別 入学者数 取得
     *  2005/12/29 Modify NO001
     *  2006/01/05 Modify NO002
     */
	String preStatSub( String param[] )
	{
		StringBuffer stb = new StringBuffer();
		try {
			//基礎データ
			stb.append("SELECT ");
			stb.append("    COUNT(*) AS CNT ");
			stb.append(   ",VALUE(SEX,'9') AS SEX ");                           //NO002
            stb.append(   ",VALUE(VALUE(FS_AREA_CD,'08'),'X') AS FS_AREA_CD "); //NO002
			stb.append("FROM ");
			stb.append("    ENTEXAM_APPLICANTBASE_DAT ");
			stb.append("WHERE ");
			stb.append("    ENTEXAMYEAR = '"+param[0]+"' AND ");
			stb.append("    ENTDIV = '1' ");
			//NO001 stb.append("        AND APPLICANTDIV = ? ");
			stb.append(    "AND APPLICANTDIV = '" + param[8] + "' ");  //NO001
            stb.append("GROUP BY GROUPING SETS ((SEX, VALUE(FS_AREA_CD,'08')),(SEX),(VALUE(FS_AREA_CD,'08')),()) "); //NO002
            stb.append("ORDER BY SEX,FS_AREA_CD ");  //NO002
		} catch( Exception e ){
			log.error( "preStat error!", e );
            log.debug( "ps = " + stb.toString() );
		}
		return stb.toString();
	}
    
    /** 表上部の特科、普通クラスの人数を印字する */
    private String preStatSub2 (String param[]) {
        StringBuffer stb = new StringBuffer();
        stb.append(" SELECT ENTCLASS, ");
        stb.append("        COUNT(*) AS CNT ");
        stb.append(" FROM ");
        stb.append("     ENTEXAM_APPLICANTBASE_DAT ");
        stb.append(" WHERE ");
        stb.append("     ENTEXAMYEAR = '"+param[0]+"' AND ");
        stb.append("     APPLICANTDIV = '"+param[8]+"' AND ");
        stb.append("     ENTDIV = '1' ");
        stb.append(" GROUP BY ");
        stb.append("     ENTCLASS ");
        return stb.toString();
    }
    
    private void svfprintSub2( DB2UDB db2
            ,Vrw32alp svf
            ,String param[])
    {
        int ret = 0;
        if (false && 0 != ret) { ret = 0; }
        totalcnt = 0;  //NO002
        int[] ent=new int[2];
        // ENTCLASS1 -> '普通' -> ENTRANCE2
        // ENTCLASS2 -> '特科' -> ENTRANCE1
        String[] conv=new String[]{"2","1"};
        try {
            ps = db2.prepareStatement(preStatSub2(param));
            rs = ps.executeQuery();
            while ( rs.next() ){
                String entclass = rs.getString("ENTCLASS");
                if (entclass != null) {
                    ent[Integer.parseInt(entclass)-1] = Integer.parseInt(rs.getString("CNT"));
                }
            }
            rs.close();
            db2.commit();
        } catch( Exception ex ) {
            log.error( "setSvfSub error!", ex );
        }
        for(int i=0; i<2; i++) {
            ret = svf.VrsOut("ENTRANCE"+conv[i], String.valueOf(ent[i]));
        }
    }    
}


/**
 *  地区別内訳印刷のクラス スライド・アップ合格者
 */
private class AreaStatistics_11B extends AreaStatistics_Base
{
    String preStatSub( String param[] )
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("SELECT ");
            stb.append("    COUNT(*) AS CNT ");
            stb.append(   ",VALUE(SEX,'9') AS SEX ");
            stb.append(   ",VALUE(VALUE(FS_AREA_CD,'08'),'X') AS FS_AREA_CD ");
            stb.append("FROM ");
            stb.append("    ENTEXAM_APPLICANTBASE_DAT ");
            stb.append("WHERE ");
            stb.append( "(ENTEXAMYEAR, EXAMNO) IN ");
            stb.append(   "(SELECT ENTEXAMYEAR, EXAMNO ");
            stb.append(    "FROM  ENTEXAM_RECEPT_DAT ");
            stb.append(    "WHERE (ENTEXAMYEAR,EXAMNO, TESTDIV) IN ");
            stb.append(         "(SELECT MAX(ENTEXAMYEAR) AS ENTEXAMYEAR, ");
            stb.append(                 "EXAMNO, ");
            stb.append(                 "MAX(TESTDIV) AS MAXTESTDIV ");
            stb.append(            "FROM ENTEXAM_RECEPT_DAT ");
            stb.append(            "WHERE ENTEXAMYEAR='" + param[0] + "' AND JUDGEDIV = '1' ");
            stb.append(            "GROUP BY EXAMNO) ");
            stb.append(    "AND JUDGECLASS IN ('3','4','6')) ");
            stb.append(  "AND APPLICANTDIV = '" + param[8] + "' ");
            stb.append("GROUP BY GROUPING SETS ((SEX, VALUE(FS_AREA_CD,'08')),(SEX),(VALUE(FS_AREA_CD,'08')),()) ");
            stb.append("ORDER BY SEX,FS_AREA_CD ");
        } catch( Exception e ){
            log.error( "preStat error!", e );
            log.debug( "ps = " + stb.toString() );
        }
        return stb.toString();
    }
}


/**
 *  地区別内訳印刷のクラス 特別アップ合格者
 */
private class AreaStatistics_12B extends AreaStatistics_Base
{
    String preStatSub( String param[] )
    {
        StringBuffer stb = new StringBuffer();
        try {
            //重複出願者については両方の受験番号で「特別アップ合格」となる。
            //但し、その場合のカウントは１とする。
            //カウントするのは「特別アップ」の対象となった試験での１カウントのみ。
            stb.append("WITH MAX_TESTDIV_SINGAKU AS ( ");
            stb.append("    SELECT ");
            stb.append("        EXAMNO, ");
            stb.append("        MAX(TESTDIV) AS TESTDIV ");
            stb.append("    FROM ");
            stb.append("        ENTEXAM_RECEPT_DAT ");
            stb.append("    WHERE ");
            stb.append("        ENTEXAMYEAR = '" + param[0] + "' ");
            stb.append("        AND APPLICANTDIV = '" + param[8] + "' ");
            stb.append("        AND JUDGEDIV = '1' ");
            stb.append("        AND JUDGECLASS IN ('1','4','6') ");
            stb.append("    GROUP BY ");
            stb.append("        EXAMNO ");
            stb.append(") ");

            stb.append("SELECT  VALUE(SEX,'9') AS SEX ");
            stb.append(       ",VALUE(VALUE(FS_AREA_CD,'08'),'X') AS FS_AREA_CD ");
            stb.append(       ",COUNT(*) AS CNT ");
            stb.append("FROM    ENTEXAM_APPLICANTBASE_DAT T1 ");
            stb.append("        LEFT JOIN MAX_TESTDIV_SINGAKU L0 ON L0.EXAMNO = T1.EXAMNO ");
            stb.append("        LEFT JOIN MAX_TESTDIV_SINGAKU L1 ON L1.EXAMNO = T1.RECOM_EXAMNO1 ");
            stb.append("        LEFT JOIN MAX_TESTDIV_SINGAKU L2 ON L2.EXAMNO = T1.RECOM_EXAMNO2 ");
            stb.append("        LEFT JOIN MAX_TESTDIV_SINGAKU L3 ON L3.EXAMNO = T1.RECOM_EXAMNO3 ");
            stb.append("WHERE   T1.ENTEXAMYEAR = '" + param[0] + "' ");
            stb.append(    "AND T1.APPLICANTDIV = '" + param[8] + "' ");
            stb.append(    "AND T1.SPECIAL_MEASURES = '3' ");
            stb.append("    AND ( ");
            stb.append("        VALUE(L0.TESTDIV, '0') > VALUE(L1.TESTDIV, '0') AND ");
            stb.append("        VALUE(L0.TESTDIV, '0') > VALUE(L2.TESTDIV, '0') AND ");
            stb.append("        VALUE(L0.TESTDIV, '0') > VALUE(L3.TESTDIV, '0') ");
            stb.append("        ) ");
            stb.append("GROUP BY GROUPING SETS ((SEX, VALUE(FS_AREA_CD,'08')),(SEX),(VALUE(FS_AREA_CD,'08')),()) ");
            stb.append("ORDER BY SEX,FS_AREA_CD ");
        } catch( Exception e ){
            log.error( "preStat error!", e );
            log.debug( "ps = " + stb.toString() );
        }
        return stb.toString();
    }
}


/**
 *  地区別内訳印刷のクラス 特別／繰上合格者
 */
private class AreaStatistics_13B extends AreaStatistics_Base
{
    String preStatSub( String param[] )
    {
        StringBuffer stb = new StringBuffer();
        try {
            stb.append("SELECT  VALUE(SEX,'9') AS SEX ");
            stb.append(       ",VALUE(VALUE(FS_AREA_CD,'08'),'X') AS FS_AREA_CD ");
            stb.append(       ",COUNT(*) AS CNT ");
            stb.append("FROM    ENTEXAM_APPLICANTBASE_DAT T1 ");
            stb.append("WHERE   T1.ENTEXAMYEAR = '" + param[0] + "' ");
            stb.append(    "AND T1.APPLICANTDIV = '" + param[8] + "' ");
            stb.append(    "AND T1.SPECIAL_MEASURES IN ('1','2') ");
            stb.append("GROUP BY GROUPING SETS ((SEX, VALUE(FS_AREA_CD,'08')),(SEX),(VALUE(FS_AREA_CD,'08')),()) ");
            stb.append("ORDER BY SEX,FS_AREA_CD ");
        } catch( Exception e ){
            log.error( "preStat error!", e );
            log.debug( "ps = " + stb.toString() );
        }
        return stb.toString();
    }
}


}//クラスの括り
